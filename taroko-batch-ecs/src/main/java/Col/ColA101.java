/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/03/20  V1.00.00    Rou        program initial                          *
*  109/06/15  V1.00.01    Sunny      error msg , use h_eflg_file_date         *
*  109/07/13  V1.00.02    Sunny      cancel crt_date,use file_date            * 
*  109/07/14  V1.00.03    Sunny      use fileMove                             *
*  109/12/07  V1.00.04    shiyuqi       updated for project coding standard   *
*  110/04/07  V1.00.05    Justin     fix get string bugs                      *
*  110/07/14  V1.00.06    Sunny      fix system_id=COL_FTP_GET                *
*  110/09/16  V1.00.07    Sunny      更正處理程式邏輯 & 增加公用元件CommCol          *
*  110/09/22  V1.00.08    Sunny      增加寫入錯誤報表ptr_batch_rpt                *
*  112/03/14  V1.00.09    Sunny      修改檔案名稱                                                                 *
*  112/05/12  V1.00.10    Sunny      檔案格式處理三個日期(協商申請日/止息日/檔案日期)         *
*  112/06/02  V1.00.11    Sunny      調整處理多筆轉入的時序問題                                         *
*  112/06/11  V1.00.12    Sunny      調整錯誤報表顯示欄位(增加三個日期)                             *
*  112/06/20  V1.00.13    Sunny      增加檢核檔案內容日期合理性
*  112/08/09  V1.00.14    Sunny      增加ID項下信用卡數處理                                          *
*  112/10/02  V1.00.15    Ryan       日期轉西元改為共用方法處理                                        *
*  112/10/04  V1.00.16    Sunny      增加參數all，區分轉檔時處理的檔名                            *
******************************************************************************/

package Col;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;
import com.CommCol;

public class ColA101 extends AccessDAO {
	public final boolean debug = false;
	private static final String FILE_NAME = "IDNF.txt";
	private static final String FILE_NAME2 = "IDNF_ALL.txt";
	private String progname = "處理前置債協申請檔  112/10/04  V1.00.16  ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommDate comd = new CommDate();
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
	String hApplyDate="";
	String hInterestBaseDate = "";
	String hFileDate = "";

	String hIdnoIdPSeqno = "";
	String hIdnoChiName = "";
	String hCreditCardFlag = ""; //增加信用卡數
	String hIdPSeqno = "";
	String hNotifyDate = "";
	String hRecolReason = "";
	String fileStatus = "1"; // 整個檔案都是同一個狀態

	String tmpstr = "";
	String temstr1 = "";

	int forceFlag;
	int totalCnt1;
	int warningCnt;

	int totalCnt;
	int insertCnt;
	int deleteCnt;
	int warnCnt;

	/* error report */
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String rptName1 = "ColA101R1";
	String rptDesc1 = "前置債協申請檔處理錯誤報表";
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
				comc.errExit("Usage : ColA101 file_date [force_flag] ", "");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commCol = new CommCol(getDBconnect(), getDBalias());
				
			forceFlag = 0;
			if ((args.length == 2) && (args[1].equals("Y"))) {
				forceFlag = 1;
			}
			

			hEflgFileDate = "";
			if ((args.length >= 1) && (args[0].length() == 8)) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hEflgFileDate = sGArgs0;
			}
			selectPtrBusinday();

//         // get searchDate
//         			String searchDate = (args.length == 0) ? "" : args[0].trim();
//         			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
//         			searchDate = getProgDate(searchDate, "D");
//         			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

			if (hEflgFileDate.length() == 0) {
				hEflgFileDate = hBusiBusinessDate;
			}

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
	void selectEcsFtpLog() throws Exception {

		sqlCmd = "select proc_code,";
		sqlCmd += "rowid as rowid ";
		sqlCmd += "from ecs_ftp_log ";
		sqlCmd += "where system_id = 'COL_FTP_GET' ";
		sqlCmd += "and trans_resp_code = 'Y' ";
		sqlCmd += "and proc_code in ('0', '1', '9','Y') ";
		sqlCmd += "and file_name = ? ";
		sqlCmd += "and file_date = ? ";
		sqlCmd += "order by trans_seqno desc fetch first 1 row only";
		setString(1, hEflgFileName);
		setString(2, hEflgFileDate);

//		showLogMessage("I", "", String.format("處理檔案2[%s]", hEflgFileName));
//		showLogMessage("I", "", String.format("處理日期2[%s]", hEflgFileDate));

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hEflgProcCode = getValue("proc_code");
			hEflgRowid = getValue("rowid");

			switch (hEflgProcCode.substring(0, 1)) {
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
			comcr.errRtn(String.format("[%s]FTP檔案－無轉入記錄可處理", FILE_NAME), "", hCallBatchSeqno);
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
	 * 檔案格式: IDNF.TXT 收前置債協系統之前置債協申請檔 01 INPUT-RECORD1. 02 IN-ID1 PIC X(10).
	 IDNF.txt
		範例：S212****90097042409705191120509
		1.	身分證字號 _X(10)
		2.	協商申請日_X(7)
		3.	止息日_X(7)
		4.	系統日_X(7)
	 */

	void readFile() throws Exception {
		String str10 = "";

		printHeader(); // 寫error報表抬頭
		totalCnt = 0;

		while (true) {
			str10 = readTextFile(fptr1);
			
			if (endFile[fptr1].equals("Y"))
				break;
			
			//初始化
			hIdnoIdNo="";
			hApplyDate = "";
			hInterestBaseDate="";
			hFileDate="";
			
			//hIdnoIdNo = str10;
			hIdnoIdNo = comc.subMS950String(str10.getBytes("MS950"), 0, 10);
			hApplyDate = comc.subMS950String(str10.getBytes("MS950"), 10, 7);
			hInterestBaseDate = comc.subMS950String(str10.getBytes("MS950"), 17, 7);
			hFileDate = comc.subMS950String(str10.getBytes("MS950"), 24, 7);
			
			//轉為西元年
			if (!commStr.empty(hApplyDate))
//			hApplyDate   = String.valueOf((Integer.parseInt(hApplyDate) + 19110000));
			hApplyDate = comd.tw2adDate(hApplyDate);
//			showLogMessage("I", "", String.format("ApplyDate[%s]", hApplyDate));
			
			if (!commStr.empty(hInterestBaseDate))
//			hInterestBaseDate = String.valueOf((Integer.parseInt(hInterestBaseDate) + 19110000));	
			hInterestBaseDate = comd.tw2adDate(hInterestBaseDate);
//			showLogMessage("I", "", String.format("InterestBaseDate[%s]", hInterestBaseDate));
			
			if (!commStr.empty(hFileDate))
//			hFileDate = String.valueOf((Integer.parseInt(hFileDate) + 19110000));
			hFileDate = comd.tw2adDate(hFileDate);
//			showLogMessage("I", "", String.format("FileDate[%s]", hFileDate));
			
//			showLogMessage("I", "", String.format("ID = [%s]", hIdnoIdNo));
			if(debug)
			showLogMessage("I", "",
					String.format("DEBUG : ID[%s] status[%s] ApplyDate[%s] InterestBaseDate[%s] FileDate[%s]", hIdnoIdNo, fileStatus, hApplyDate, hInterestBaseDate, hFileDate));
			
			totalCnt++;
			
			/*資料檢核*/
			
			if(hIdnoIdNo.trim().length()<10) {
				errStr = "[ID長度不正確，格式有誤，跳過不處理]";
				showLogMessage("W", "",
						String.format("WARNING X:ID[%s] status[%s] %s", hIdnoIdNo, fileStatus, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細				
				continue;
			}
		
			if (!comm.checkDateFormat(hApplyDate, "yyyyMMdd")||
				!comm.checkDateFormat(hInterestBaseDate, "yyyyMMdd")||
				!comm.checkDateFormat(hFileDate, "yyyyMMdd")
					) {
				errStr = "[日期格式有誤]";
//				showLogMessage("W", "", String.format("WARNING X:ID[%s] 檔案狀態[%s] 狀態日期[%s] [%s]", hIdnoIdNo, hStatus,
//						hNotifyDate, errStr));
				if(debug)
				showLogMessage("I", "",
						String.format("DEBUG : ID[%s] status[%s] ApplyDate[%s] InterestBaseDate[%s] FileDate[%s]", hIdnoIdNo, fileStatus, hApplyDate, hInterestBaseDate, hFileDate));
				
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細
				continue;
			}

			// 檢查卡人檔，確認是否存在
			hIdnoIdPSeqno = commCol.selectCrdIdno(hIdnoIdNo); // 取得id_p_seqno(1)

			// 卡人檔不存在，檢查身分證變更檔(crd_chg_id)，確認是否為舊ID
			if (hIdnoIdPSeqno.equals("-1")) {

				// 身分證變更檔,取得id_p_seqno(2)
				hIdnoIdPSeqno = commCol.selectCrdChgId(hIdnoIdNo);

				errStr = "[卡人檔無此ID,非本行卡友，跳過不處理]";
				showLogMessage("W", "",
						String.format("WARNING X:ID[%s] status[%s] %s", hIdnoIdNo, fileStatus, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細

				continue;
			}

			// 確認ID為卡友(ID存在)，再確認是否已存在col_liac_nego_t(依id_p_seqno,日期,狀態)，為0表示已存在資料。
			
			/* sunny 暫時先mark
			if (commCol.selectColLiacNegoT(hIdnoIdPSeqno, fileStatus).equals("-1")) {
				// 確認為卡友且不存在於暫存檔col_liac_nego_t，則insert col_liac_nego_t
				hIdnoChiName = commCol.selectCrdIdnoName(hIdnoIdNo); // 取得中文姓名
				insertColLiacNegoT();
			} else {

				errStr = "[ID資料已存在暫存檔待處理，重複轉檔，跳過不處理]";
				showLogMessage("W", "",
						String.format("WARNING X:ID[%s] status[%s] %s", hIdnoIdNo, fileStatus, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細

				continue;
			}
			*/
			
			/*上一段的作業先提出來*/
			
			hIdnoChiName = commCol.selectCrdIdnoName(hIdnoIdNo); // 取得中文姓名
			
			//判斷ID項下是否有卡片
			if(commCol.selectCrdCardCnt(hIdnoIdPSeqno).equals("0"))
				hCreditCardFlag="N"; // 表示名下目前無信用卡
			else
				hCreditCardFlag="Y"; // 表示有信用卡
		
			
			insertColLiacNegoT();
			
		}
		closeInputText(fptr1);

		renameFile(hEflgFileName);
	}

	/***********************************************************************/
	void insertColLiacNegoT() throws Exception {

		daoTable = "col_liac_nego_t";

		setValue("file_date", hEflgFileDate);              //檔案日期(原值放hEflgFileDate-營業日) //檔案內容中的hFileDate
		setValue("liac_txn_code", "A");
		setValue("liac_status", fileStatus);
		setValue("id_p_seqno", hIdnoIdPSeqno);
		setValue("id_no", hIdnoIdNo);
		setValue("chi_name", hIdnoChiName);
		setValue("apply_date", hApplyDate);                 //協商申請日
		setValue("interest_base_date", hInterestBaseDate);  //止息日
		setValue("nego_s_date", hInterestBaseDate);         //協商開始日（TCB先設定同止息日)
		setValue("query_date", hEflgFileDate);
		setValue("notify_date", hApplyDate);		        //通知日期
		//setValue("credit_card_flag", "Y");
		setValue("credit_card_flag", hCreditCardFlag);	    //ID項下是否有信用卡
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("proc_flag", "0");
		setValue("proc_date", sysDate);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);

		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_col_liac_nego_t duplicate!", "", hCallBatchSeqno);
		} else {
			//showLogMessage("I", "", String.format("ID[%s] insert col_liac_nego_t success! ", hIdnoIdNo));
			if(debug)
			showLogMessage("I", "", String.format("ID[%s] status[1] ApplyDate[%s] InterestBaseDate[%s] FileDate[%s]", hIdnoIdNo,hApplyDate,hInterestBaseDate,hEflgFileDate));
			insertCnt++;
		}
	}

	/***********************************************************************/
	/* 強迫轉檔時，進行強制刪除 */
	void deleteColLiacNegoT() throws Exception {
		daoTable = "col_liac_nego_t";
		whereStr = "where file_date = ?  ";
		whereStr += "and liac_status = '1' ";
		setString(1, hEflgFileDate);
		deleteCnt = deleteTable();
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
		buf = comcr.insertStr(buf, "協商申請日", 12);
		buf = comcr.insertStr(buf, "止息基準日", 24);
		buf = comcr.insertStr(buf, "檔案處理日", 36);
		buf = comcr.insertStr(buf, "錯誤原因", 48);
		lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", buf));
		
		//buf = "\n";
		
		// 表頭分隔線=====
		buf = "";
        for (int i = 0; i < 80; i++){
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
		buf = comcr.insertStr(buf, hApplyDate, 12);
		buf = comcr.insertStr(buf, hInterestBaseDate, 24);
		buf = comcr.insertStr(buf, hFileDate, 36);
		buf = comcr.insertStr(buf, errStr, 48);
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
	void updateEcsFtpLog() throws Exception {
		daoTable = "ECS_FTP_LOG";
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
			showLogMessage("I", "", "ERROR : 檔案 " + filename + "更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + filename + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ColA101 proc = new ColA101();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
