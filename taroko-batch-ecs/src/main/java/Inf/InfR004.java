/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version      AUTHOR                DESCRIPTION                *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  110/08/27   V1.00.00     Yang Bo                  initial	              *
 *  112/03/14   V1.00.01   Zuwei Su   日期轉民國年(7碼),欄位長度調整                      *
 *  112/07/19   V1.00.02   Ryan       By cycle結帳日 +1日  產生各cycle資料, 檔名為結帳日, 其他非結帳日出空檔                      *
 *  112/09/12   V1.00.03   Ryan       空檔檔名日期-1                       *
 *****************************************************************************/

package Inf;

import java.nio.file.Paths;

import com.*;

public class InfR004 extends BaseBatch {
	private final String progname = "產生送CRM Account歸戶帳單資料檔程式 112/09/12 V1.00.03";
	private static final String CRM_FOLDER = "media/crm/";
	private static final String DATA_FORM = "CCTOLS1";
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
    CommDate commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommTxInf commTxInf = null;
	CommCol commCol = null;
	String isFileName = "";
	private int ilFile004;
	String hSysDate = "";
	String hIdNo = "";
	String hCardNo = "";
	String hLastTtl = "";
	String hPaymentAmt = "";
	String hDebitAmt = "";
	String hBalCa = "";
	String hFinanceCharge = "";
	String hTtlAmt = "";
	String hStmtMp = "";
	String hDueAmt = "";
	String hAmtMin = "";
	String hCloseDate = "";
	String hLastPayDate = "";
	String hAcctDate = "";
	String hAcctType = "";
	String hStmtCycle = "";

	public static void main(String[] args) {
		InfR004 proc = new InfR004();
		proc.mainProcess(args);
		proc.systemExit();
	}

	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfR004 [business_date]");
			errExit(1);
		}
		dbConnect();
		if (liArg == 1) {
			hSysDate = args[0];
		}
		if (empty(hSysDate)) {
			hSysDate = hBusiDate;
		}
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		commTxInf = new CommTxInf(getDBconnect(), getDBalias());
		commCol = new CommCol(getDBconnect(), getDBalias());
		
		// 產生Header檔
		dateTime(); // update the system date and time
		String searchDate = (args.length == 0) ? hSysDate : args[0].trim();
//		String searchDate2 = searchDate;
		
		searchDate = getProgDate(searchDate, "D");
//		searchDate2 = getProgDate(searchDate2, "M");

		boolean isWorkDay = selectPtrWorkday(searchDate);
		
		if(isWorkDay == true)
			searchDate = commString.left(searchDate, 6) + hStmtCycle;

		// convert YYYYMMDD into YYMMDD
		String fileNameSearchDate = searchDate.substring(2);
		// convert YYYYMM into YYMM
//		String fileNameSearchDate2 = searchDate2.substring(2);	
		
		if(isWorkDay == true) {
			showLogMessage("I", "",String.format("今日結帳日 + 1日 = [%s]",hSysDate));
			showLogMessage("I", "",String.format("cycle = [%s]",hStmtCycle));
		}else {
			showLogMessage("I", "",String.format("今日為非結帳 +1日 = [%s],產生空檔",hSysDate));
			fileNameSearchDate = commDate.dateAdd(searchDate, 0, 0, -1).substring(2);
		}
		isFileName = "CCTOLS1_" + fileNameSearchDate + ".DAT";
		checkOpen();
		if(isWorkDay == true)
			selectDataType();
		closeOutputText(ilFile004);
		
		String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
//		String datFileName2 = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate2, CommTxInf.DAT_EXTENSION);
		
		String fileFolder =  Paths.get(comc.getECSHOME(), CRM_FOLDER).toString();
//		String fileFolder2 =  Paths.get(comc.getECSHOME(), CRM_FOLDER).toString();
		
		boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), totalCnt);
		if (isGenerated == false) {
			comc.errExit("產生HDR檔錯誤!", "");
		}
		String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
//		String hdrFileName2 = datFileName2.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
		
		//檢核searchDate是否為每月最後一天營業日
//		boolean isLastBusinday = commCol.isLastBusinday(searchDate);
		
		//每月最後一個營業日多產生一份
//		if(isLastBusinday) {
//			copyFile(datFileName,fileFolder,datFileName2,fileFolder2);
//			boolean isGenerated2 = commTxInf.generateTxtCrmHdr(fileFolder2, datFileName2, searchDate, sysDate, sysTime.substring(0,4), totalCnt);
//			if (isGenerated2 == false) {
//				comc.errExit("每月最後一個營業日產生HDR檔錯誤!", "");
//			}
//		}
		
		procFTP(fileFolder, datFileName, hdrFileName);
//		if(isLastBusinday)
//			procFTP(fileFolder2, datFileName2, hdrFileName2);
		endProgram();
	}

	void selectDataType() throws Exception {

		sqlCmd = "select i.id_no, (select card_no from crd_card where p_seqno = cu.p_seqno order by CURRENT_CODE fetch first 1 rows only) as card_no, cu.stmt_last_ttl, cu.stmt_payment_amt, " +
				"(cu.stmt_new_amt + cu.stmt_adjust_amt) as debit_amt, cu.billed_beg_bal_ca, " +
				"(cu.billed_beg_bal_ri + cu.billed_beg_bal_pf + cu.billed_beg_bal_lf) as finance_charge, " +
				"cu.stmt_this_ttl_amt, cu.stmt_mp, cu.stmt_over_due_amt, " +
				"(cu.min_pay + cu.stmt_mp + cu.stmt_over_due_amt) as amt_min, " +
				"w.this_close_date, w.this_lastpay_date, w.this_acct_month, " +
				"decode(cu.CURR_CODE,'840', '606','392', '607', decode(cu.ACCT_TYPE,'01','106','306') ) as acct_type " +
				"from act_curr_hst cu " +
				"left join act_acno a ON cu.p_seqno = a.p_seqno " +
				"left join crd_idno i ON a.id_p_seqno = i.id_p_seqno " +
				"left join ptr_workday w ON cu.stmt_cycle = w.stmt_cycle " +
				"where cu.acct_month = to_char(add_months(to_date(w.this_close_date,'yyyymmdd'),-1),'yyyymm') " +
				" and  a.acno_flag <>'2' and cu.stmt_cycle = ? " +
				"order by cu.acct_type, cu.stmt_cycle, cu.p_seqno";
		setString(1,hStmtCycle);
		openCursor();
		while (fetchTable()) {
			hIdNo = commString.rpad(colSs("id_no"), 16);
	    hCardNo = commString.rpad(colSs("card_no"), 16); 
			hLastTtl = commString.rpad(colSs("stmt_last_ttl"), 14);
			hPaymentAmt = commString.rpad(colSs("stmt_payment_amt"), 14);
			hDebitAmt = commString.rpad(colSs("debit_amt"), 14);
			hBalCa = commString.rpad(colSs("billed_beg_bal_ca"), 14);
			hFinanceCharge = commString.rpad(colSs("finance_charge"), 14);
			hTtlAmt = commString.rpad(colSs("stmt_this_ttl_amt"), 14);
			hStmtMp = commString.rpad(colSs("stmt_mp"), 14);
			hDueAmt = commString.rpad(colSs("stmt_over_due_amt"), 14);
			hAmtMin = commString.rpad(colSs("amt_min"), 14);
			hCloseDate = commString.rpad(commDate.toTwDate(colSs("this_close_date")), 7);
			hLastPayDate = commString.rpad(commDate.toTwDate(colSs("this_lastpay_date")), 7);
			hAcctDate = commString.rpad(commDate.toTwDate(colSs("this_acct_month")), 7);
			hAcctType = commString.rpad(colSs("acct_type"), 3);
			writeTextFile();
		}
		closeCursor();
	}

	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crm/%s", comc.getECSHOME(), isFileName);
		ilFile004 = openOutputText(lsTemp, "big5");
		if (ilFile004 < 0) {
			printf("CCTOLS1 產檔失敗 ! ");
			errExit(1);
		}
	}

	void writeTextFile() throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String tmpStr = "", newLine = "\r\n";
		tempBuf.append(comc.fixLeft(hIdNo, 16));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hCardNo, 16));
		tempBuf.append("\006");
		tempBuf.append("001");
		tempBuf.append("\006");
		tempBuf.append(" ");
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hLastTtl, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hPaymentAmt, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hDebitAmt, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hBalCa, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hFinanceCharge, 14));
        tempBuf.append("\006");
        tempBuf.append(" ");
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hTtlAmt, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hStmtMp, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hDueAmt, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hAmtMin, 14));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hCloseDate, 7));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hLastPayDate, 7));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hAcctDate, 7));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft(hAcctType, 3));
		tempBuf.append("\006");
		tempBuf.append(comc.fixLeft("", 63));
		tempBuf.append(newLine);
		totalCnt++;
		this.writeTextFile(ilFile004, tempBuf.toString());
	}
	
	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CRM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRM", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}
	
	void copyFile(String datFileName1, String fileFolder1 ,String datFileName2, String fileFolder2) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder2, datFileName2).toString();

		if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
			comc.errExit("ERROR : 檔案[" + datFileName2 + "]copy失敗!", "");
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已copy至 [" + tmpstr2 + "]");
	}

	public int insertEcsNotifyLog(String fileName) throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", comr.getObjectOwner("3", javaProgram));
		setValue("obj_type", "3");
		setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", commFTP.hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ecs_notify_log";

		insertTable();

		return (0);
	}
	
	private boolean selectPtrWorkday(String searchDate) throws Exception {
		extendField = "workday.";
		sqlCmd = "select stmt_cycle from ptr_workday where to_char(to_date(this_close_date,'yyyymmdd') + 1 days , 'yyyymmdd') = ? ";
		setString(1,searchDate);
		int n = selectTable();
		if(n > 0) {
			hStmtCycle = getValue("workday.stmt_cycle");
			return true;
		}
		return false;
	}

}
