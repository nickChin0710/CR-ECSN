/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/06/29  V1.00.00    Ryan       program initial                          *
*  112/07/18  V1.00.01    Ryan       寫入檔案的金額要轉正                                                                                    *
*  112/07/24  V1.00.02    Ryan       寫入檔案的金額右靠左補0                          *
*  112/08/21  V1.00.03    Grace      C02: dba_acaj.adjust_type=FD10 (維持不變)    *
*                                    D04: 存摺摘要(摘要代號)改為VDDS                  *
*  112/08/24  V1.00.04    Ryan       修改 (消費-退貨) > 0  不產檔 VDD04_REQ_YYYYMMDD 檔案                         *
*  112/12/19  V1.00.05  Zuwei Su    errRtn改為 show message & return 1  *  
********************************************************************************/

package Mkt;

import java.text.Normalizer;
import java.util.ArrayList;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class MktC940 extends AccessDAO {

	public final boolean debugD = false;

	private String progname = "數位帳戶VD卡回饋及產檔處理 112/08/24  V1.00.04 ";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
	CommDate comDate = new CommDate();
	CommCrdRoutine comcr = null;

	private static final String PATH_FOLDER = "/media/mkt";
	private static final String FILE_NAME_AP4 = "VDD04_REQ.YYYYMMDD";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	int debug = 0;

	Buf1 data = new Buf1();

	private String hProcDate = "";

	int idNoCnt = 0;
	
	private int fptr1 = -1;
	private long totCnt = 0;

	private String fmtFileNameAP4 = "";
	
	String headerTmpBuf = "";
	ArrayList<String> bodyTmpBuf = new ArrayList<String>();
	String footerTmpBuf = "";
	int writeCnt = 0;
	long tolCashPayAmt = 0;
	
	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 1) {
//				comc.errExit("Usage : MktC940 [sysdate ex:yyyymmdd] ", "");
                showLogMessage("I", "", "Usage : MktC940 [sysdate ex:yyyymmdd] " );
                return 1;
			}

			// 固定要做的

			if (!connectDataBase()) {
//				comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error " );
                return 1;
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			String sGArgs0 = "";
			if (args.length == 0){
				hProcDate = selectPtrBusinDay();
			} else if (args.length == 1 && args[0].length() == 8) {
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hProcDate = sGArgs0;
			}
			showLogMessage("I", "", String.format("輸入參數1 = [%s]", sGArgs0));
			showLogMessage("I", "", String.format("本日營業日 = [%s]", hProcDate));
			
			getIdNo();
			selectMktDigitalactOpen();

			if(bodyTmpBuf.size()>0) {
				fileOpenAP4();
				writeTextAP4();
				closeOutputText(fptr1);
				ftpProcAP4();
				renameFileAP4();
				
			}
			showLogMessage("I", "", String.format("Process records = [%d]", totCnt));

			// ==============================================
			// 固定要做的
			comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
			comcr.callbatchEnd();
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	private String selectPtrBusinDay() throws Exception {
		sqlCmd = " select business_date from ptr_businday where 1=1 ";

		if (selectTable() <= 0) {
			comc.errExit("PTR_BUSINDAY 無资料!!", "");
		}

		return getValue("business_date");
	}
	
	double getDebitSeq() throws Exception {
		double seqno = 0;

		sqlCmd = "select dba_txnseq.nextval as nextval from dual";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("GetDebitSeq() not found!", "", "");
		}

		seqno = getValueDouble("nextval");

		return (seqno);
	}
	
	/***
	 * 讀取數存帳戶VD卡活動回饋參數檔.活動代碼、數存帳戶開戶ID檔.身分證號
	 * @return
	 * @throws Exception
	 */
	private int getIdNo() throws Exception {
		extendField = "parm_id.";
		sqlCmd = " select b.id_no from mkt_digacctvd_parm a, mkt_digitalact_open b, dbc_idno c ";
		sqlCmd += " where ? between to_char(to_date(a.ACTIVE_DATE_S,'yyyymmdd') + 1 month ,'yyyymm') ";
		sqlCmd += " and to_char(to_date(a.ACTIVE_DATE_E,'yyyymmdd') + 1 month ,'yyyymm') ";
		sqlCmd += " and b.OPEN_DATE between a.ACTIVE_DATE_S and ACTIVE_DATE_E ";
		sqlCmd += " and b.ID_NO = c.ID_NO ";
		setString(1,comStr.left(hProcDate, 6));
		idNoCnt = selectTable();
		showLogMessage("I", "", String.format("取得ID筆數 = [%d]", idNoCnt));
		return idNoCnt;
	}

	/***
	 * 取得VD卡一般消費/退貨累計金額(由dbb_bill.sign_flag識別消費(‘+’)、退貨(‘-‘));
	 ***/
	private void selectMktDigitalactOpen() throws Exception {
		for(int i = 0 ;i<idNoCnt ;i++) {
			sqlCmd = "Select c.id_no, c.digital_actno, a.sign_flag, a.cash_pay_amt ,d.p_seqno ,d.card_no ";
			sqlCmd += " From dbb_bill a, dbc_idno b, mkt_digitalact_open c, dbc_card d ";
			sqlCmd += " Where substr(a.purchase_date,1,6) = ? ";
			sqlCmd += " And a.ID_P_SEQNO = b.id_p_seqno ";
			sqlCmd += " And b.id_no = c.id_no ";
			sqlCmd += " And c.id_no = ? ";
			sqlCmd += " And c.digital_actno = d.acct_no ";
			sqlCmd += " And NOT a.mcht_no in ";
			sqlCmd += " (select data_code  from mkt_mchtgp_data ";
			sqlCmd += " where table_name = 'MKT_MCHT_GP' and data_key = 'MKTNCUS00') ";
			sqlCmd += " and NOT substr(a.mcht_chi_name, 1, 2) in ('f%', 'G%', 'd%', 'M%', 'b%', 'e%', 'V%', 'A%', '$%', '#%') ";
			setString(1, comDate.monthAdd(hProcDate, -1));
			setString(2, getValue("parm_id.id_no",i));
			openCursor();
			while (fetchTable()) {
				data.initData();
				data.idNo = getValue("id_no");
				data.digitalActno = getValue("digital_actno");
				data.signFlag = getValue("sign_flag");
				data.pSeqno = getValue("p_seqno");
				data.cardNo = getValue("card_no");
				data.cashPayAmt += "-".equals(data.signFlag)?getValueLong("cash_pay_amt") * -1 : getValueLong("cash_pay_amt");
			}
			closeCursor();
			
			long sumCashPayAmt = Math.round(data.cashPayAmt * 0.01);
			if(sumCashPayAmt<0) {
				bodyTmpBuf.add(data.bodyText(sumCashPayAmt));
				tolCashPayAmt += sumCashPayAmt;
				writeCnt++;
			}else{
				insertDbaAcaj(sumCashPayAmt);
			}
			totCnt++;
			if (totCnt % 1000 == 0 || totCnt == 1)
				showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));
		}
	}
	
	private void writeTextAP4() throws Exception {
	    //表頭
	    headerTmpBuf = data.headerText();
		writeTextFile(fptr1, headerTmpBuf);
		
		//明細
		for(int i = 0; i<bodyTmpBuf.size() ;i++)
			writeTextFile(fptr1, bodyTmpBuf.get(i));
		
		//表尾
		footerTmpBuf = data.footerText();
		writeTextFile(fptr1, footerTmpBuf);
	}
	
	
	void insertDbaAcaj(long sumCashPayAmt) throws Exception{
    	extendField = "ACAJ.";
        setValue("ACAJ.CRT_DATE", hProcDate);
        setValue("ACAJ.CRT_TIME", sysTime);
        setValue("ACAJ.DEDUCT_DATE", "");  
        setValue("ACAJ.DEDUCT_SEQ", "");  
        setValue("ACAJ.P_SEQNO", data.pSeqno);  
        setValue("ACAJ.ACCT_TYPE", "90");  
        setValue("ACAJ.ACCT_NO", data.digitalActno);  
        setValue("ACAJ.ADJUST_TYPE", "FD10");  
        setValue("ACAJ.REFERENCE_NO", "");  
        setValue("ACAJ.POST_DATE", hProcDate); 
        setValueDouble("ACAJ.ORGINAL_AMT", 0); 
        setValueLong("ACAJ.DR_AMT", sumCashPayAmt); 
        setValueDouble("ACAJ.CR_AMT", 0); 
        setValueDouble("ACAJ.BEF_AMT", 0); 
        setValueDouble("ACAJ.AFT_AMT", 0); 
        setValueDouble("ACAJ.BEF_D_AMT", 0); 
        setValueDouble("ACAJ.AFT_D_AMT", 0); 
        setValue("ACAJ.ACCT_CODE", ""); 
        setValue("ACAJ.FUNC_CODE", "U"); 
        setValue("ACAJ.CARD_NO", data.cardNo); 
        setValue("ACAJ.CASH_TYPE", ""); 
        setValue("ACAJ.VALUE_TYPE", "1"); 
        setValue("ACAJ.TRANS_ACCT_TYPE", ""); 
        setValue("ACAJ.TRANS_ACCT_KEY", ""); 
        setValue("ACAJ.ITEM_POST_DATE", ""); 
        setValue("ACAJ.PURCHASE_DATE", hProcDate); 
        setValue("ACAJ.INTEREST_DATE", hProcDate); 
        setValue("ACAJ.ADJ_REASON_CODE", ""); 
        setValue("ACAJ.ADJ_COMMENT", "數存戶VD卡回饋入數存戶"); 
        setValue("ACAJ.C_DEBT_KEY", ""); 
        setValue("ACAJ.DEBIT_ITEM", ""); 
        setValue("ACAJ.JRNL_DATE", ""); 
        setValue("ACAJ.JRNL_TIME", ""); 
        setValue("ACAJ.PAYMENT_TYPE", ""); 
        setValue("ACAJ.BATCH_NO_NEW", ""); 
        setValue("ACAJ.PROC_FLAG", "N"); 
        setValue("ACAJ.JOB_CODE", ""); 
        setValue("ACAJ.VOUCH_JOB_CODE", ""); 
        setValueDouble("ACAJ.DEDUCT_AMT", 0); 
        setValue("ACAJ.DEDUCT_PROC_CODE", ""); 
        setValue("ACAJ.DEDUCT_PROC_DATE", ""); 
        setValue("ACAJ.DEDUCT_PROC_TIME", ""); 
        setValue("ACAJ.DEDUCT_PROC_TYPE", ""); 
        setValue("ACAJ.FROM_CODE", "1"); 
        setValue("ACAJ.TXN_CODE", ""); 
        setValue("ACAJ.MCHT_NO", ""); 
        setValue("ACAJ.VOUCH_FLAG", ""); 
        setValue("ACAJ.CHG_DATE", hProcDate); 
        setValue("ACAJ.CHG_USER", javaProgram); 
        setValue("ACAJ.APR_FLAG", "Y"); 
        setValue("ACAJ.APR_DATE", ""); 
        setValue("ACAJ.APR_USER", ""); 
        setValue("ACAJ.RSK_CTRL_SEQNO", ""); 
        setValue("ACAJ.MOD_TIME", sysDate + sysTime);
        setValue("ACAJ.MOD_USER", javaProgram);
        setValue("ACAJ.MOD_PGM", javaProgram);
        setValueInt("ACAJ.MOD_SEQNO", 1);
        daoTable = "DBA_ACAJ";
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "insert DBA_ACAJ error ,pSeqno = " +data.pSeqno);
			return;
		}
	}

	/*******************************************************************/
	private void fileOpenAP4() throws Exception {
		fmtFileNameAP4 = FILE_NAME_AP4.replace("YYYYMMDD", hProcDate);

		String temstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
		String fileName = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		fptr1 = openOutputText(fileName, "MS950");
		if (fptr1 == -1) {
			comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
		}
	}
	

	/*******************************************************************/
	private void ftpProcAP4() throws Exception {

		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

		/**********
		 * COMM_FTP common function usage
		 ****************************************/
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
		for (int inti = 0; inti < 1; inti++) {
			commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
			commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
			commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
			commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), PATH_FOLDER);
			commFTP.hEflgModPgm = javaProgram;

			showLogMessage("I", "", "mput " + fmtFileNameAP4 + " 開始傳送....");
			int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fmtFileNameAP4);

			if (errCode != 0) {
				showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileNameAP4 + " 資料" + " errcode:" + errCode);
				if (inti == 0)
					break;
			}

		}
	}

	void renameFileAP4() throws Exception {
		String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
		String tmpstr2 = String.format("%s%s/backup/%s.%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4,sysDate+sysTime);

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fmtFileNameAP4 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fmtFileNameAP4 + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		MktC940 proc = new MktC940();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String idNo;
		String digitalActno = "";
		String signFlag = "";
		long cashPayAmt = 0;
		String pSeqno = "";
		String cardNo = "";
		
		void initData() {
			idNo = "";
			digitalActno = "";
			signFlag = "";
			cashPayAmt = 0;
			pSeqno = "";
			cardNo = "";
		}

		/***
		 * AW-AP4-CRYYYYMMDD.01 表頭
		 * 
		 * @return
		 * @throws Exception
		 */
		String headerText() throws Exception {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(comc.fixLeft("1", 1)) // 首筆資料 固定值: 1
					.append(comc.fixLeft("006", 3)) // 銀行代碼 固定值: 006
					.append(comc.fixLeft(hProcDate, 8)) // 營業日
					.append(comc.fixLeft(" ", 188)) // 保留欄位 空白
				    .append(LINE_SEPERATOR);
			return strBuf.toString();
		}

		/***
		 * 明細
		 * 
		 * @return
		 * @throws Exception
		 */
		String bodyText(long sumCashPayAmt) throws Exception {
			StringBuffer strBuf = new StringBuffer();
			String debitSeq = String.format("%010.0f", getDebitSeq());
			strBuf.append(comc.fixLeft("2", 1)) // 明細資料 固定值: 2
					.append(comc.fixLeft("006", 3)) // 銀行代碼 固定值: 006
					.append(comc.fixLeft("D04", 3)) // 交易代碼 固定值: D04 (帳戶扣款)
					.append(comc.fixLeft(debitSeq, 10)) // getDebitSeq();參閱DbaA001欄位作法
					.append(comc.fixLeft(hProcDate, 8)) // 交易日期 西元年月日
					.append(comc.fixLeft(String.format("%012d",sumCashPayAmt<0?sumCashPayAmt * -1:sumCashPayAmt), 12)) // 金額 計算後的 “現金回饋金額”
					.append(comc.fixLeft(digitalActno, 13)) // 金融帳號 digital_actno
					.append(comc.fixLeft(cardNo, 16)) // VD卡號  
					.append(comc.fixLeft(debitSeq, 15)) // 解圈扣款流水號 getDebitSeq();
					.append(comc.fixLeft(idNo, 10)) // 法人戶統一編號 / 帳戶歸屬的ID
					.append(comc.fixLeft(" ", 2)) // 處理回覆碼
					.append(comc.fixLeft(" ", 16)) // 票證外顯卡號
					.append(comc.fixLeft(" ", 4)) // 特店類別碼
					.append(comc.fixLeft(" ", 15)) // 特店代碼
					.append(comc.fixLeft(" ", 6)) // 授權碼
					.append(comc.fixLeft(" ", 30)) // 保留
					.append(comc.fixLeft("VDDS", 4)) // 摘要代號 固定值: VDRD (Visa退貨)-->VDDS (20230821, grace) 
					.append(comc.fixLeft(" ", 16)) // 交易英文說明
					.append(comc.fixLeft("數存戶VD卡回饋", 16)) // 交易中文說明 固定值: 數存戶VD卡回饋
					.append(LINE_SEPERATOR);
			return strBuf.toString();
		}

		/***
		 * AW-AP4-CRYYYYMMDD.01 表尾
		 * 
		 * @return
		 * @throws Exception
		 */
		String footerText() throws Exception {
			StringBuffer strBuf = new StringBuffer();
			strBuf.append(comc.fixLeft("3", 1)) // 尾筆資料 固定值: 3
					.append(comc.fixLeft("006", 3)) // 銀行代碼 固定值: 006
					.append(comc.fixLeft(hProcDate, 8)) // 營業日
					.append(comc.fixLeft(String.format("%010d", writeCnt), 10)) // 總筆數
					.append(comc.fixLeft(String.format("%014d", tolCashPayAmt<0?tolCashPayAmt*-1:tolCashPayAmt), 14)) // 總金額
					.append(comc.fixLeft(" ", 164)); // 保留
			return strBuf.toString();
		}
	}
}
