/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/03/25  V1.00.00    Rou        program initial                          *
*  109/07/13  V1.00.01    Sunny      cancel crt_date,use file_date,error msg  *
*  109/07/14  V1.00.02    Sunny      use fileMove                             *
*  109/12/07  V1.00.03    shiyuqi       updated for project coding standard   *
*  110/04/07  V1.00.04    Justin     fix get string bugs                      *
*  110/07/14  V1.00.05    Sunny      fix system_id=COL_FTP_GET                *
*  110/09/16  V1.00.06    Sunny      更正處理程式邏輯 & 增加公用元件CommCol        *
*  110/09/22  V1.00.07    Sunny      增加寫入錯誤報表ptr_batch_rpt                *  
*  112/03/14  V1.00.08    Sunny      修改檔案名稱                                                                 *
*  112/05/16  V1.00.09    Sunny      增加前置協商狀態T視同復催                                           *
*  112/06/21  V1.00.11    Sunny      增加檢核檔案內容日期合理性
*  112/08/09  V1.00.12    Sunny      增加ID項下信用卡數處理                                          *
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

public class ColA104 extends AccessDAO {
	public final boolean debug = false;
	private static final String FILE_NAME = "IDNZ0046.txt";
	private static final String FILE_NAME2 = "IDNZ0046_ALL.txt";
	private String progname = "處理前置協商復催N、T/毁諾X/還清Z申請檔   112/08/09  V1.00.12 ";
	CommFunction comm = new CommFunction();
	CommString commStr = new CommString();
	CommCrd comc = new CommCrd();
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
	String hIdnoIdPSeqno = "";
	String hIdnoChiName = "";
	String hIdPSeqno = "";
	String hStatus = "";
	String hReason = "";
	String hNotifyDate = "";
	String hRecolReason = "";
	String fileStatus = ""; // 檔案區分不同的狀態，依檔案註記為主。

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
	String rptName1 = "ColA104R1";
	String rptDesc1 = "前置協商復催N/毁諾X/還清Z申請檔處理錯誤報表";
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
				comc.errExit("Usage : ColA104 file_date [force_flag] ", "");
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
		buf = comcr.insertStr(buf, "協商狀態", 12);
		buf = comcr.insertStr(buf, "轉換協商狀態", 22);
		buf = comcr.insertStr(buf, "協商狀態日期", 36);
		buf = comcr.insertStr(buf, "錯誤原因", 50);
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

		if (lineCnt > 25) {
			lpar1.add(comcr.putReport(rptName1, rptDesc1, sysDate, ++rptSeq1, "0", "##PPP"));
			lineCnt = 0;
		}
		if (lineCnt == 0) {
			printHeader();
		}

		buf = "";
		buf = comcr.insertStr(buf, hIdnoIdNo, 1);
		buf = comcr.insertStr(buf, hStatus, 12); /* 協商狀態，顯示檔案裡的原值 */
		buf = comcr.insertStr(buf, fileStatus, 22); /* 轉換後協商狀態 */
		buf = comcr.insertStr(buf, hNotifyDate, 36); /* 協商狀態日期 */
		buf = comcr.insertStr(buf, errStr, 50); /* 錯誤原因 */
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
	 * IDNZ0046.TXT 收前置債協系統之復催/毁諾/還清申請檔
	 *
	 * 01 INPUT-RECORD3. 02 IN-ID3 PIC X(10). 02 IN-PGM-MARK3 PIC X(03). 02
	 * IN-Z46-DATE PIC X(07). 02 IN-Z46-STATUS PIC X(01). 復催N/毁諾X/還清Z
	 */

	void readFile() throws Exception {
		String str21 = null;

		printHeader(); // 寫error報表抬頭
		totalCnt = 0;

		while (true) {
			str21 = readTextFile(fptr1);
			if (endFile[fptr1].equals("Y"))
				break;

//            hIdnoIdNo = str21.substring(0, 10);
//            hNotifyDate = str21.substring(13, 20);
//            hStatus = str21.substring(20, 21);

			hIdnoIdNo = "";
			hNotifyDate = "";
			hStatus = "";
			fileStatus = "";
			hReason = "";
			errStr = "";

			byte[] tmpBytes = str21.getBytes("MS950");
			hIdnoIdNo = comc.subMS950String(tmpBytes, 0, 10);
			hNotifyDate = comc.subMS950String(tmpBytes, 13, 7);
			hStatus = comc.subMS950String(tmpBytes, 20, 1);
			
			//轉為西元年
			if (!commStr.empty(hNotifyDate))
		     hNotifyDate   = String.valueOf((Integer.parseInt(hNotifyDate) + 19110000));
			
			totalCnt++;
			
			if(debug)showLogMessage("I", "", String.format("ID = [%s]", hIdnoIdNo));
			
			/*資料檢核*/

			if (hIdnoIdNo.trim().length() < 10) {
				errStr = "[ID長度不正確，格式有誤，跳過不處理]";
				showLogMessage("W", "",
						String.format("WARNING X:ID[%s] status[%s] [%s]", hIdnoIdNo, fileStatus, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細
				continue;
			}

			if (!comm.checkDateFormat(hNotifyDate, "yyyyMMdd")) {
				errStr = "[狀態日期格式錯誤]";
				showLogMessage("W", "", String.format("WARNING X:ID[%s] 檔案狀態[%s] 狀態日期[%s] [%s]", hIdnoIdNo, hStatus,
						hNotifyDate, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細
				continue;
			}

			/*
			 * 轉換資料 復催N轉為狀態4 毁諾X轉為狀態5 還清Z轉為狀態6
			 */

			switch (hStatus) {
			case "N":
				fileStatus = "4";
				hReason = "01";   //協商終止
				break;
			//20230516 sunny add
			case "T":
				fileStatus = "4"; //債務人主動撤案，終止協商
				hReason = "55";
				break;
			case "X": 
				fileStatus = "5";
				hReason = "00"; //毀諾
				break;
			case "Z":
				fileStatus = "6";
				hReason = "99"; //履約完畢
				break;
			default:
				fileStatus = "";
//				comcr.errRtn(String.format("ID = [%s] Status=[%s]，檔案的協商狀態無法辨識，程式錯誤!!", hIdnoIdNo, hStatus), "",
//						hCallBatchSeqno);
			}
			
			/*資料檢核*/

			if (fileStatus.equals("")) {
				errStr = "[協商狀態不為N/X/Z，無法辨識處理]";
				showLogMessage("W", "", String.format("WARNING X:ID[%s] 檔案狀態[%s] 轉換後的status[%s] 狀態日期[%s] [%s]",
						hIdnoIdNo, hStatus, fileStatus, hNotifyDate, errStr));
				errorCnt++;
				printDetail(); // 寫入錯誤報表明細	
				continue;
			}

			
//			hIdPSeqno = selectCrdIdno(hIdnoIdNo);
//			if (hIdPSeqno.equals("1"))
//				continue;
//			else
//			    selectColLiacNegoT(hStatus);
//			     //insertColLiacNegoT();

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
	
			// 確認為卡友且不存在於暫存檔col_liac_nego_t，則insert col_liac_nego_t
			hIdnoChiName = commCol.selectCrdIdnoName(hIdnoIdNo); // 取得中文姓名
			insertColLiacNegoT();

		}
		closeInputText(fptr1);

		renameFile(hEflgFileName);
	}

//	/***********************************************************************/
//	int selectColLiacNegoT(String hStatus) throws Exception {
//
//		String hLiacStatus;
//		sqlCmd = "select liac_status ";
//		sqlCmd += "from col_liac_nego_t ";
//		sqlCmd += "where id_no = ? ";
//		sqlCmd += "and id_p_seqno = ? ";
//		sqlCmd += "and liac_txn_code = 'A' ";
//		sqlCmd += "and liac_status = ?";
//		sqlCmd += "fetch first 1 row only ";
//		setString(1, hIdnoIdNo);
//		setString(2, hIdnoIdPSeqno);
//		setString(3, hStatus);
//
//		int recordCnt = selectTable();
//		if (recordCnt > 0) {
//			hLiacStatus = getValue("liac_status");
//			switch (hLiacStatus) {
//			case "4":
//				showLogMessage("W", "",
//						String.format("WARNING X:ID[%s] status[%s] 停催資料(復催N)重複通知，跳過不處理", hIdnoIdNo, hLiacStatus));
//				warnCnt++;
//			case "5":
//				showLogMessage("W", "",
//						String.format("WARNING X:ID[%s] status[%s] 停催資料(毀諾X)重複通知，跳過不處理", hIdnoIdNo, hLiacStatus));
//				warnCnt++;
//			case "6":
//				showLogMessage("W", "",
//						String.format("WARNING X:ID[%s] status[%s] 停催資料(還清Z)重複通知，跳過不處理", hIdnoIdNo, hLiacStatus));
//				warnCnt++;
//			}
//		} else
//			insertColLiacNegoT();
//
//		return 0;
//	}

	/***********************************************************************/
	void insertColLiacNegoT() throws Exception {

//		hNotifyDate = String.valueOf((Integer.parseInt(hNotifyDate) + 19110000));

		daoTable = "col_liac_nego_t";

		setValue("file_date", hEflgFileDate);
		setValue("liac_txn_code", "A");
		setValue("id_p_seqno", hIdnoIdPSeqno);
		setValue("id_no", hIdnoIdNo);
		setValue("chi_name", hIdnoChiName);
		setValue("query_date", hEflgFileDate);
		setValue("notify_date", hNotifyDate);
		setValue("liac_status", fileStatus); // 更新轉換後的狀態資訊(N->4,X->5,Z->6)
		setValue("recol_reason", hReason);
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("proc_flag", "0");
		setValue("proc_date", sysDate);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);

//		switch (hStatus) {
//		case "N":
//			setValue("liac_status", "4");
//			setValue("recol_reason", "01");
//			break;
//		case "X":
//			setValue("liac_status", "5");
//			setValue("recol_reason", "00");
//			break;
//		case "Z":
//			setValue("liac_status", "6");
//			setValue("recol_reason", "99");
//			break;
//		}

		insertTable();
		if (dupRecord.equals("Y"))
			comcr.errRtn("insert_col_liac_nego_t duplicate!", "", hCallBatchSeqno);

		else {
			if(debug)
			showLogMessage("I", "", String.format("ID[%s] insert col_liac_nego_t success! ", hIdnoIdNo));
			insertCnt++;
		}
	}

	/***********************************************************************/
	void deleteColLiacNegoT() throws Exception {
		daoTable = "col_liac_nego_t";
		whereStr = "where file_date = ?  ";
		whereStr += "and liac_status in ('4','5','6')";
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
		ColA104 proc = new ColA104();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
