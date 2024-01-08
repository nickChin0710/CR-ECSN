/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/03/24  V1.00.00    Rou        program initial                          *
*  109/07/13  V1.00.01    Sunny      cancel crt_date,use file_date,error msg  *
*  109/07/14  V1.00.02    Sunny      use fileMove                             *
*  109/12/07  V1.00.03    shiyuqi       updated for project coding standard   *
*  110/04/07  V1.00.04    Justin     fix get string bugs                      *
*  110/07/14  V1.00.05    Sunny      fix system_id=COL_FTP_GET                *
*  110/09/16  V1.00.06    Sunny      更正處理程式邏輯 & 增加公用元件CommCol          *
*  110/09/22  V1.00.07    Sunny      增加寫入錯誤報表ptr_batch_rpt                *
*  112/03/14  V1.00.08    Sunny      修改檔案名稱                                                                 *
*  112/05/22  V1.00.09    Sunny      修改檔案格式，增加簽約成功日期                                    *
*  112/06/07  V1.00.10    Ryan       簽約完成日增加判斷資料長度                                          *
*  112/06/07  V1.00.10    Ryan       hContractDate初始化                                                *
*  112/06/11  V1.00.11    Sunny      錯誤報表顯示簽約成功日期                                              *
*  112/06/20  V1.00.12    Sunny      增加檢核檔案內容日期合理性
*  112/08/09  V1.00.12    Sunny      增加ID項下信用卡數處理                                           *
*  112/10/04  V1.00.13    Sunny      增加參數all，區分轉檔時處理的檔名                            *
******************************************************************************/

package Col;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;
import com.CommCol;

public class ColA103 extends AccessDAO {
	public final boolean debug = false;
	private static final String FILE_NAME = "IDNZ0098.txt";
	private static final String FILE_NAME2 = "IDNZ0098_ALL.txt";
	private String progname = "處理前置簽約完成通知檔 112/06/20  V1.00.12 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commStr = new CommString();
	CommCrdRoutine comcr = null;
	CommCol commCol = null;

	String hCallBatchSeqno = "";

	String hBusiBusinessDate = "";
	String hTempSystime = "";
	String hEflgProcCode = "";
	String hEflgRowid = "";
	String hEflgFileName = "";
	String hEflgFileDate = "";

	int errorCnt = 0;
	String hEflgProcDesc = "";
	String hIdnoIdNo = "";
	String hContractDate = "";
	String hIdnoIdPSeqno = "";
	String hIdnoChiName = "";
	String hCreditCardFlag = ""; //增加信用卡數
	String hIdPSeqno = "";
	String hStatus = "";
	String hNotifyDate = "";
	String hRecolReason = "";
	String fileStatus = "3"; // 整個檔案都是同一個狀態
	
	
	String hLiacStatus="";
	String hLiacContractDate="";

	String tmpstr = "";
	String temstr1 = "";
	int forceFlag = 0;
	int totalCnt1 = 0;
	int warningCnt = 0;

	int totalCnt;
	int insertCnt;
	int deleteCnt;
	int warnCnt;

	/* error report */
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String rptName1 = "ColA103R1";
	String rptDesc1 = "前置簽約完成通知檔處理錯誤報表";
	String buf = "";
	String szTmp = "";
	String errStr = "";
	int rptSeq1 = 0;
	int pageCnt = 0;
	int lineCnt = 0;
	int warnFlag = 0;

	private int fptr1 = 0;

	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (comm.isAppActive(javaProgram)) {
				comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
			}
			// 檢查參數
			if (args.length != 0 && args.length != 1 && args.length != 2) {
				comc.errExit("Usage : ColA103 file_date [force_flag] ", "");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commCol = new CommCol(getDBconnect(), getDBalias());

			forceFlag = 0;
			if ((args.length == 2) && (args[1].equals("Y")))
				forceFlag = 1;
			hEflgFileDate = "";
			if ((args.length >= 1) && (args[0].length() == 8)) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hEflgFileDate = sGArgs0;
			}
			selectPtrBusinday();
			if (hEflgFileDate.length() == 0)
				hEflgFileDate = hBusiBusinessDate;

			hEflgFileName = FILE_NAME;
			
			//如果參數1為all，則令檔名為FILE_NAME2的值
			if ((args.length >= 1) && (args[0].equals("all"))) {				
				hEflgFileName = FILE_NAME2;
			}
			
			showLogMessage("I", "", String.format("處理日期[%s]...", hEflgFileDate));
			selectEcsFtpLog();
			showLogMessage("I", "", String.format("處理檔案[%s]...", hEflgFileName));
			totalCnt1 = 0;
			fileOpen();
			errorCnt = warningCnt = 0;
			readFile(); // 主要處理(讀檔，寫入暫存檔，寫入錯誤報表明細)
			printTailer(); // 寫出錯誤報表檔尾
			comcr.insertPtrBatchRpt(lpar1); /* 寫入ptr_batch_rpt online報表 */

			hEflgProcDesc = tmpstr;

			// ==============================================
			// 固定要做的
			updateEcsFtpLog();
			showLogMessage("I", "", "程式執行結束,累計處理[" + totalCnt + "]筆,新增 [" + insertCnt + "]筆,警示[" + warnCnt + "]筆,錯誤["
					+ errorCnt + "]筆");
			commitDataBase();
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		sqlCmd = "select business_date,";
		sqlCmd += "to_char(sysdate,'hh24miss') h_temp_systime ";
		sqlCmd += "from ptr_businday ";
		sqlCmd += "fetch first 1 row only ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
			hTempSystime = getValue("h_temp_systime");
		}

	}

	/***********************************************************************/

	void printHeader() throws Exception {

		buf = "";
		pageCnt++;
		buf = comcr.insertStr(buf, "報表名稱: " + rptName1, 1);
		buf = comcr.insertStr(buf, rptDesc1, 47);
		buf = comcr.insertStr(buf, "頁    次:", 93);
		szTmp = String.format("%4d", pageCnt);
		buf = comcr.insertStr(buf, szTmp, 101);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "印表日期:", 93);
		buf = comcr.insertStr(buf, chinDate, 101);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "轉入日期:", 1);
		szTmp = String.format("%8d", comcr.str2long(hBusiBusinessDate));
		buf = comcr.insertStr(buf, szTmp, 10);
		buf = comcr.insertStr(buf, "檔案名稱:", 25);
		buf = comcr.insertStr(buf, hEflgFileName, 36);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "身份證號", 1);
		buf = comcr.insertStr(buf, "簽約成功日", 12);
		buf = comcr.insertStr(buf, "錯誤原因", 24);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
		// buf = "\n";

		// 表頭分隔線=====
		buf = "";
		for (int i = 0; i < 80; i++) {
			buf += "=";
		}
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/
	void printDetail() throws Exception {
		String tmpstr = "";

		lineCnt++;
//		if (lineCnt >= 31) {
//			printHeader();
//			lineCnt = 0;
//		}
		if (lineCnt > 25) {
			lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", "##PPP"));
			lineCnt = 0;
		}
		if (lineCnt == 0) {
			printHeader();
		}

		buf = "";
		buf = comcr.insertStr(buf, hIdnoIdNo, 1);
		buf = comcr.insertStr(buf, hContractDate, 12); //簽約完成日(如為空值會預設為系統處理日期)
		buf = comcr.insertStr(buf, errStr, 24);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/
	void printTailer() throws Exception {
		buf = "\n";
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));

		buf = "";
		buf = comcr.insertStr(buf, "失  敗: ", 10);
		szTmp = comcr.commFormat("3z,3z,3z", errorCnt);
		buf = comcr.insertStr(buf, szTmp, 20);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
	}

	/***********************************************************************/
	void selectEcsFtpLog() throws Exception {

		sqlCmd = "select proc_code,";
		sqlCmd += "rowid as rowid ";
		sqlCmd += "from ecs_ftp_log ";
		sqlCmd += "where system_id = 'COL_FTP_GET' ";
		sqlCmd += "and trans_resp_code = 'Y' ";
		sqlCmd += "and proc_code in ('0', '1', '9', 'Y') ";
		sqlCmd += "and file_name = ? ";
		sqlCmd += "and file_date = ? ";
		setString(1, hEflgFileName);
		setString(2, hEflgFileDate);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hEflgProcCode = getValue("proc_code");
			hEflgRowid = getValue("rowid");

			switch (hEflgProcCode) {
			case "0":
				return;

			case "9":
				showLogMessage("I", "", String.format("[%s]FTP檔案－資料重轉入處理", hCallBatchSeqno));
				return;

			case "Y":
				exceptExit = 0;
				comcr.errRtn(String.format("[%s]FTP檔案－資料已處理完畢", hEflgFileName), "", hCallBatchSeqno);
			case "1":
//            	exceptExit = 0;
//            	comcr.errRtn(String.format("[%s]FTP檔案－資料已轉入完畢, 不需再轉入", hEflgFileName), "", hCallBatchSeqno);

				/*
				 * 強迫轉檔 當程式第1個參數為指定日期，第2個參數為Y時，則會將指定日期的資料從col_liac_nego_t刪除，並且重新讀取資料檔進行轉入。
				 */
				if (forceFlag == 0) {
					exceptExit = 0;
					comcr.errRtn(String.format("[%s]FTP檔案－資料已轉入完畢, 不需再轉入\"", hEflgFileName), "", hCallBatchSeqno);
				} else {
					showLogMessage("I", "", String.format("[%s]FTP檔案－資料強制轉入處理", hEflgFileName));
					deleteColLiacNegoT();
					showLogMessage("I", "", String.format("強制刪除暫存檔的筆數 [ %d]", deleteCnt));
					return;
				}
			}
		} else {
			exceptExit = 0;
			comcr.errRtn(String.format("[%s]無轉入記錄可處理", FILE_NAME), "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void fileOpen() throws Exception {
		temstr1 = String.format("%s/media/col/%s", comc.getECSHOME(), hEflgFileName);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

		fptr1 = openInputText(temstr1, "MS950");
		if (fptr1 == -1) {
			comcr.errRtn(String.format("error: [%s] 檔案不存在", temstr1), "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/

	/*
	 * IDNZ0098.TXT 收前置債協系統之簽約完成通知申請檔
	 *
	 * 01 INPUT-RECORD4. 02 IN-BRNO4 PIC X(04). 02 IN-CARDNO4 PIC X(16). 02 IN-ID4
	 * PIC X(10). 02 IN-TYPE4 PIC X(01). 02 IN-PGM-MARK4 PIC X(03).
	 */
	
	/* 20230522 修訂檔案格式，此檔應包含呆帳戶，增加第5欄簽約完成日
	 * 信用卡主機格式
		範例1：0020042194311       A120912262NZ001101026
		範例2：31444057590662143100P146972651NZ001070514
		1.	LnAccount_X(20)  0-19
		2.	身分證字號_ X(10)  20-29
		3.	代碼_X(1)        30-30
		4.	固定Z00_X(3)     31-33
		5.	日期_X(7)：簽約完成日，有日期給YYYMMDD，否則給空白X(7) 34-40
	 * 
	 */

	void readFile() throws Exception {
		String str21 = null;

		printHeader(); // 寫error報表抬頭
		totalCnt = 0;

		while (true) {
			str21 = readTextFile(fptr1);
			if (endFile[fptr1].equals("Y"))
				break;
			
			//初始化
			hIdnoIdNo="";
			hContractDate = "";
			
			hIdnoIdNo = comc.subMS950String(str21.getBytes("MS950"), 20, 10);     // 身份證字號(ID)
			if(str21.length()>=41) {
				hContractDate = comc.subMS950String(str21.getBytes("MS950"), 34, 7); // 簽約完成日
			}
			//轉為西元年
			if (!commStr.empty(hContractDate))
			{
				hContractDate = String.valueOf((Integer.parseInt(hContractDate) + 19110000));
			}
			else
			{
			    hContractDate = hEflgFileDate;
			    showLogMessage("I", "", String.format("ID[%s] ContractDate[] 簽約完成日為空值，代入FileDate[%s]", hIdnoIdNo,hEflgFileDate));
			}
			
			totalCnt++;
			
			if(debug)
			showLogMessage("I", "", String.format("[debug] ID[%s] ContractDate[%s]", hIdnoIdNo, hContractDate));
			
			
			
			/*資料檢核*/
			
			if(hIdnoIdNo.trim().length()<10) {
				errStr = "[ID長度不正確，格式有誤，跳過不處理]";
				showLogMessage("W", "",
						String.format("WARNING X:ID[%s] status[%s] [%s]", hIdnoIdNo, fileStatus, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細				
				continue;
			}
			
			if (!comm.checkDateFormat(hContractDate, "yyyyMMdd")) {
				errStr = "[狀態日期格式錯誤]";
				showLogMessage("W", "", String.format("WARNING X:ID[%s] 檔案狀態[%s] 狀態日期[%s] [%s]", hIdnoIdNo, hStatus,
						hContractDate, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細
				continue;
			}

//            hIdPSeqno = selectCrdIdno(hIdnoIdNo);
//            if (hIdPSeqno.equals("1"))
//            	continue;
//            
//            else
//            	  selectColLiacNegoT();
//                insertColLiacNegoT();

			// 檢查卡人檔，確認是否存在
			hIdnoIdPSeqno = commCol.selectCrdIdno(hIdnoIdNo); // 取得id_p_seqno(1)

			// 卡人檔不存在，檢查身分證變更檔(crd_chg_id)，確認是否為舊ID
			if (hIdnoIdPSeqno.equals("-1")) {

				// 身分證變更檔,取得id_p_seqno(2)
				hIdnoIdPSeqno = commCol.selectCrdChgId(hIdnoIdNo);

				errStr = "[卡人檔無此ID,非本行卡友，跳過不處理]";
				showLogMessage("W", "",
						String.format("WARNING X:ID[%s] status[%s] [%s]", hIdnoIdNo, fileStatus, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細

				continue;
			}

			// 確認ID為卡友(ID存在)，再確認是否已存在col_liac_nego_t(依id_p_seqno,日期,狀態)，為0表示已存在資料。

			/* sunny 暫時先mark
//            if (commCol.selectColLiacNegoT(hIdnoIdPSeqno,hEflgFileDate,fileStatus).equals("-1"))
			if (commCol.selectColLiacNegoT(hIdnoIdPSeqno, fileStatus).equals("-1")) {
				// 確認為卡友且不存在於暫存檔col_liac_nego_t，則insert col_liac_nego_t
				hIdnoChiName = commCol.selectCrdIdnoName(hIdnoIdNo); // 取得中文姓名
				insertColLiacNegoT();
			} else {
				errStr = "[ID資料已存在暫存檔待處理，重複轉檔，跳過不處理]";
				showLogMessage("W", "",
						String.format("WARNING X:ID[%s] status[%s] [%s]", hIdnoIdNo, fileStatus, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細

				continue;
			}
			*/
			
			//檢核簽約成功日期是否相同, -1表不同。
			if (selectColLiacNegoT().equals("0") && hLiacContractDate.compareTo(hLiacContractDate) == 0)
			{ 
				errStr = "[ID資料(簽約日相同)已存在暫存檔待處理，重複轉檔，跳過不處理]";
				showLogMessage("W", "", String.format("WARNING X:ID[%s] status[3] ContractDate[%s] liacContractDate[%s] [%s]", hIdnoIdNo, hLiacStatus,hLiacContractDate,errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細
		    	continue;
			}
			else
			{
				//無簽約資料或簽約日期不同，一律寫入temp檔
				hIdnoChiName = commCol.selectCrdIdnoName(hIdnoIdNo); // 取得中文姓名
				
				//判斷ID項下是否有卡片
				if(commCol.selectCrdCardCnt(hIdnoIdPSeqno).equals("0"))
					hCreditCardFlag="N"; // 表示名下目前無信用卡
				else
					hCreditCardFlag="Y"; // 表示有信用卡
			
				insertColLiacNegoT(); 
			}			
		}
		closeInputText(fptr1);

		renameFile(hEflgFileName);
	}

	/***********************************************************************/
//    String selectCrdIdno(String hIdNo) throws Exception {
//    	
//        sqlCmd  = "select id_p_seqno, chi_name ";
//        sqlCmd += "from crd_idno ";
//        sqlCmd += "where id_no = ? ";
//        setString(1, hIdNo);
//        
//        if (selectTable() > 0) {
//        	hIdnoIdPSeqno = getValue("id_p_seqno");
//        	hIdnoChiName = getValue("chi_name");
//        	showLogMessage("I", "", String.format("select crd_idno.id_p_seqno = [%s]", hIdnoIdPSeqno));
//        	return hIdnoIdPSeqno;
//        }
//        else {
//        	String tmp = selectCrdChgId(hIdNo);
//        	return tmp;
//        }       
//    }
//    
//    /***********************************************************************/
//    String selectCrdChgId(String hOldIdNo) throws Exception {
//    	
//    	sqlCmd  = "select a.id_p_seqno as old_id_p_seqno, a.chi_name ";
//    	sqlCmd += "from crd_chg_id a, crd_idno b ";
//    	sqlCmd += "where a.id_no = b.id_no ";
//    	sqlCmd += "and a.old_id_no = ? ";
//    	setString(1, hOldIdNo);
//    	int recordCnt = selectTable();
//    	if (recordCnt > 0) {   		
//    		hIdnoIdPSeqno = getValue("old_id_p_seqno");
//    		hIdnoChiName = getValue("chi_name");
//    		showLogMessage("I", "", String.format("select crd_chg_id.id_p_seqno = [%s]", hIdnoIdPSeqno));
//    		return hIdnoIdPSeqno;
//    	}
//    	else {
//    		showLogMessage("W", "", String.format("WARNING X:ID[%s]非本行卡友，跳過不處理", hOldIdNo)); 
//    		warnCnt ++;
//    		return "1";
//    	}       
//    }
//    
//    /***********************************************************************/
////    int selectColLiacNegoT() throws Exception {
////    	
////    	String hLiacStatus;
////    	sqlCmd = "select liac_status ";
////        sqlCmd += "from col_liac_nego_t ";
////        sqlCmd += "where id_no = ? ";
////        sqlCmd += "and id_p_seqno = ? ";
////        sqlCmd += "and liac_txn_code = 'A' ";
////        sqlCmd += "fetch first 1 row only ";
////        setString(1, hIdnoIdNo);
////        setString(2, hIdnoIdPSeqno);  	
////        
////        int recordCnt = selectTable();
////        if (recordCnt > 0) {
////        	hLiacStatus = getValue("liac_status");
////        	if (hLiacStatus.equals("3")) {
////        		showLogMessage("W", "", String.format("WARNING X:ID[%s] status[%s] 前置簽約完成重複通知，跳過不處理", hIdnoIdNo, hLiacStatus));
////        		warnCnt ++;
////    		}
//////        	else {
//////        		showLogMessage("W", "", String.format("WARNING X:ID[%s] status[%s] file-status[%s] 協商狀態不一致，跳過不處理", hIdnoIdNo, hLiacStatus, fileStatus));       
//////        		warnCnt ++;
//////        	}
////        } 
////        else
////        	insertColLiacNegoT();
////        
////        return 0;
////    }
//	
	
	/***********************************************************************/
	/*@return 0 有資料 -1 表沒有資料*/
	
	String selectColLiacNegoT() throws Exception {
		
        String hLiacStatus="";
		String hLiacContractDate="";
		
		//showLogMessage("W", "", String.format("WARNING X1: selectColLiacNegoT()判斷 "));
		
		sqlCmd = "select liac_status,contract_date ";
	    sqlCmd += "from col_liac_nego_t ";
	    sqlCmd += "where id_no = ? ";
	    sqlCmd += "and id_p_seqno = ? ";
	    sqlCmd += "and liac_status = '3' ";
	    sqlCmd += "and contract_date = ? ";
	    sqlCmd += "and liac_txn_code = 'A' ";
	    sqlCmd += "fetch first 1 row only ";
	    setString(1, hIdnoIdNo);
	    setString(2, hIdnoIdPSeqno);
	    setString(3, hContractDate); 
	    
	    int recordCnt = selectTable();
	    if (recordCnt > 0) {
	    	hLiacStatus = getValue("liac_status");
	    	hLiacContractDate = getValue("contract_date");
	    	//showLogMessage("W", "", String.format("WARNING X1: selectColLiacNegoT()判斷 "));
	    	return "0"; /*有資料*/
	    } 
	    //showLogMessage("W", "", String.format("WARNING X2: selectColLiacNegoT()判斷 "));
	    return "-1"; /*無資料*/    
	}
	
	/***********************************************************************/
	void insertColLiacNegoT() throws Exception {

		daoTable = "col_liac_nego_t";

		setValue("file_date", hEflgFileDate);
		setValue("liac_txn_code", "A");
		setValue("liac_status", "3");
		setValue("id_p_seqno", hIdnoIdPSeqno);
		setValue("id_no", hIdnoIdNo);
		setValue("chi_name", hIdnoChiName);
		setValue("query_date", hEflgFileDate);
		setValue("notify_date", hContractDate);
		setValue("contract_date", hContractDate);
//		setValue("credit_card_flag", "");
		setValue("credit_card_flag", hCreditCardFlag);	    //ID項下是否有信用卡
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("proc_flag", "0");
		setValue("proc_date", sysDate);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);

		insertTable();
		if (dupRecord.equals("Y"))
			comcr.errRtn("insert_col_liac_nego_t duplicate!", "", hCallBatchSeqno);

		else {
			//showLogMessage("I", "", String.format("ID[%s] insert col_liac_nego_t success! ", hIdnoIdNo));
			if(debug)
			showLogMessage("I", "", String.format("ID[%s] liac_status[3] contract_date[%s] insert col_liac_nego_t success! ", hIdnoIdNo,hContractDate));
			insertCnt++;
		}
	}

	/***********************************************************************/
	void deleteColLiacNegoT() throws Exception {
		daoTable = "col_liac_nego_t";
		whereStr = "where file_date = ?  ";
		whereStr += "and liac_status = '3' ";
		setString(1, hEflgFileDate);
		deleteCnt = deleteTable();

	}

	/***********************************************************************/
	void updateEcsFtpLog() throws Exception {
		daoTable = "ecs_ftp_log ";
		updateSQL = "proc_code = 'Y', ";
		updateSQL += " mod_time = sysdate,";
		updateSQL += " mod_pgm  = ? ";
		whereStr = "where rowid = ? ";
		setString(1, javaProgram);
		setRowId(2, hEflgRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_ecs_ftp_log not found!", "", hCallBatchSeqno);
		}

	}

	/************************************************************************/
	void renameFile(String filename) throws Exception {

		String tmpstr1 = comc.getECSHOME() + "/media/col/" + filename;
		String tmpstr2 = comc.getECSHOME() + "/media/col/backup/" + filename + "." + hEflgFileDate;

		// 使用comc.fileMove能覆蓋同檔名
		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案 [" + filename + "] 更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + filename + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ColA103 proc = new ColA103();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
