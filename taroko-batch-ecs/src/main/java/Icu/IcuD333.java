/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
 *  111/01/27  V1.00.00    Ryan      program initial                                  *
 *  111/02/14  V1.00.01    Ryan      big5 to MS950                                    *
 *  111/02/20  V1.00.02    Alex      檔案不要換行 , 傳送A檔								  *
 *  111/03/02  V1.00.03    Alex      close input text file                            *
 **************************************************************************************/

package Icu;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxBill;
import com.CommDate;
import com.CommFTP;

public class IcuD333 extends AccessDAO {
	private final String progname = "FallBack使用-讀取IcuD03檔案後計算已授權未請款金額並回寫檔案傳送至財金  111/03/02 V1.00.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	FscIcud03 fsIcud03 = new FscIcud03();
	protected com.CommString zzstr = new com.CommString();
	String modUser = "";
	String hBusiBusinessDate = "";
	String temstr = "";
	String tempId = "";
	int debug = 0;
	int fi;
	int totalFile = 0;
	String fileName1 = "", fileName2 = "" ,fileNameTxt = "AUTHNOTPROC.TXT";
	String yyyymmdd = "";
	String nn = "";
	int serialNo = 0;
	
	String cardCardNo = "";
	String tcbBin = "";

	String errorFileName = "";

	int fileCnt1 = 0;
	int filehHeadCnt = 0;
	int totalCnt = 0;
	ArrayList<String> dataList = new ArrayList<String>();
	DecimalFormat doubleFmt = new DecimalFormat("000000000.00");
	protected final String dt1Str = "mod_acdr,issuer_code,card_type,card_seqno,status_cfq,line_of_credit_amt,unpay_amt,auth_not_deposit,"
			+ "acct_jrnl_bal,jrnl_bal_sign,unpay_amt_cash,open_date,status_cfq_date,locamt_cash_rate,locamt_cash_day";

	protected final int[] dt1Ength = { 1, 8, 2, 7, 1, 9, 11, 11, 11, 1, 11, 8, 8, 2, 2 };

	protected String[] dt1 = new String[] {};

	public int mainProcess(String[] args) {

		try {
			dt1 = dt1Str.split(",");
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			commitDataBase();
			openFile();
			selectCcaAuthTxlog2();
			commitDataBase();
			procFTP2();
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
	/***********************************************************************/
	
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());
		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		for (String file : listOfFiles) {
			if (file.length() < 19)
				continue;
			if (file.substring(0, 19).equals("M00600000.ICACTQNA.")) {
				//--A檔只傳送
				fileName1 = file ;
				procFTP3();
				continue;
			}				
			if (!file.substring(0, 19).equals("M00600000.ICACTQND."))
				continue;
			fileCount++;
			readFile(file);
			procFTP();
		}
		if (fileCount < 1) {
			showLogMessage("D", "", "無檔案可處理  " + "");
		}
		return (0);
	}
	

	/***********************************************************************/
	void readFile(String file) throws Exception {
		String rec = "";
		fileName1 = file;
		int fi;
		fileName2 = comc.getECSHOME() + "/media/icu/" + fileName1;

		int f = openInputText(fileName2);
		if (f == -1) {
			return;
		}
		closeInputText(f);
		setConsoleMode("N");
		fi = openInputText(fileName2, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return;
		}

		showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/icu ]");
		showLogMessage("I", "", " Process file =[" + fileName1 + "]");

		dataList.clear();
		while (true) {
			rec = readTextFile(fi); // read file data
//			System.out.println(rec.length() + "rec");
			if (endFile[fi].equals("Y"))
				break;
			
			if (rec.trim().length() >= 93) {
				totalCnt ++;
				moveData(processDataRecord(getFieldValue(rec, dt1Ength), dt1));			
				processDisplay(1000);		
			}
		}
		closeInputText(fi);
//		renameFile(fileName1);
		outPutFile();
		
		
	}


	/*************************************************************************/
	int moveData(Map<String, Object> map) throws Exception {
		fsIcud03.initData();
		serialNo++;
		fsIcud03.modAcdr = (String) map.get("mod_acdr");
		fsIcud03.issuerCode = (String) map.get("issuer_code");
		fsIcud03.cardType = (String) map.get("card_type");
		fsIcud03.cardSeqno = (String) map.get("card_seqno");
		fsIcud03.statusCfq = (String) map.get("status_cfq");
		fsIcud03.lineOfCreditAmt = (String) map.get("line_of_credit_amt");
		fsIcud03.unpayAmt = (String) map.get("unpay_amt");
		fsIcud03.authNotDeposit = (String) map.get("auth_not_deposit");
		fsIcud03.acctJrnlBal = (String) map.get("acct_jrnl_bal");
		fsIcud03.jrnlBalSign = (String) map.get("jrnl_bal_sign");
		fsIcud03.unpayAmtCash = (String) map.get("unpay_amt_cash");
		fsIcud03.openDate = (String) map.get("open_date");
		fsIcud03.statusCfqDate = (String) map.get("status_cfq_date");
		fsIcud03.locamtCashRate =(String) map.get("locamt_cash_rate");
		fsIcud03.locamtCashDay = (String) map.get("locamt_cash_day");
		
		fsIcud03.modUser = modUser;
		fsIcud03.modTime = sysDate + sysTime;
		fsIcud03.modPgm = "IcuD333";
		
		selectFscBinGroup();
		selectCcaAuthTxlog();
		updateccaAuthTxlog();
		dataList.add(getFileData());
//		dataList.add(getFileData() +  "\r\n");
		return 1;
	}
	
	
	/***********************************************************************/
	String getFileData() {
		StringBuilder tempBuf = new StringBuilder();
		tempBuf.append(fsIcud03.modAcdr);
		tempBuf.append(fsIcud03.issuerCode);
		tempBuf.append(fsIcud03.cardType);
		tempBuf.append(fsIcud03.cardSeqno);
		tempBuf.append(fsIcud03.statusCfq);
		tempBuf.append(fsIcud03.lineOfCreditAmt);
		tempBuf.append(fsIcud03.unpayAmt);
		tempBuf.append(fsIcud03.authNotDeposit);
		tempBuf.append(fsIcud03.acctJrnlBal);
		tempBuf.append(fsIcud03.jrnlBalSign);
		tempBuf.append(fsIcud03.unpayAmtCash);
		tempBuf.append(fsIcud03.openDate);
		tempBuf.append(fsIcud03.statusCfqDate);
		tempBuf.append(fsIcud03.locamtCashRate);
		tempBuf.append(fsIcud03.locamtCashDay);
		return tempBuf.toString();
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void outPutFile() throws Exception {
		int outPutFile = openOutputText(comc.getECSHOME() + "/media/cca/" + fileName1, "MS950");
		if(outPutFile<0) {
			comcr.errRtn("更新"+fileName1+"檔案失敗", "", "");
			return;
		}
		for(int i = 0 ; i < dataList.size() ; i++) {
			writeTextFile(outPutFile, dataList.get(i).toString());
		}
		
		closeOutputText(outPutFile);
	}

	/*************************************************************************/
	void selectFscBinGroup() throws Exception {
		tcbBin = "";
		cardCardNo = "";
		sqlCmd = " select tcb_bin ";
		sqlCmd += " from fsc_bin_group ";
		sqlCmd += " where fisc_code = ? ";
		setString(1,fsIcud03.cardType);
		int recordCnt = selectTable();
		if (!notFound.equals("Y")) {
			tcbBin = getValue("tcb_bin");
			cardCardNo = tcbBin + fsIcud03.cardSeqno; 
		}
	}
	
	/*************************************************************************/
	void selectCcaAuthTxlog() throws Exception {
		double tempAmt = 0;
		double authNotDeposit = 0;
		sqlCmd = " select sum(nt_amt) as temp_amt ";
		sqlCmd += " from cca_auth_txlog ";
		sqlCmd += " where card_no like ? and mod_pgm not like 'Cnv%' and cacu_amount = 'Y' ";
		setString(1,cardCardNo+"%");
		int recordCnt = selectTable();
		if (!notFound.equals("Y")) {
			tempAmt = getValueDouble("temp_amt");
		}
		authNotDeposit = (zzstr.ss2Num(fsIcud03.authNotDeposit)/100) + tempAmt;
		fsIcud03.authNotDeposit = doubleFmt.format(authNotDeposit).replace(".", "");
	}
	
	/*************************************************************************/
	void selectCcaAuthTxlog2() throws Exception {
		double tempAmt = 0;
		String tempCardNo = "";
		int outPutFile = openOutputText(comc.getECSHOME() + "/media/cca/" + fileNameTxt, "MS950");
		if(outPutFile<0) {
			comcr.errRtn("產生AUTHNOTPROC.TXT檔案失敗", "", "");
			return;
		}
		sqlCmd = " select substr(card_no,1,13) as temp_card_no , sum(nt_amt) as temp_amt ";
		sqlCmd += " from cca_auth_txlog ";
		sqlCmd += " where mod_pgm not like 'Cnv%' and cacu_amount = 'Y' and status_code <> 'Y' group by substr(card_no,1,13) ";
		
		openCursor();
		while(fetchTable()) {
			tempAmt = getValueDouble("temp_amt");
			tempCardNo = getValue("temp_card_no");
			writeTextFile(outPutFile, tempCardNo + doubleFmt.format(tempAmt).replace(".", "")+"\r\n");
		}
		closeOutputText(outPutFile);
		closeCursor();
//		int recordCnt = selectTable();
//		for(int i = 0 ; i<recordCnt ; i++) {
//			tempAmt = getValueDouble("temp_amt");
//			tempCardNo = getValue("temp_card_no",i);
//			
//			writeTextFile(outPutFile, tempCardNo + doubleFmt.format(tempAmt).replace(".", "")+"\r\n");
//			writeTextFile(outPutFile, tempCardNo + doubleFmt.format(tempAmt).replace(".", ""));
//		}
//		closeOutputText(outPutFile);
	}

	/***********************************************************************/
	void updateccaAuthTxlog() throws Exception {
		daoTable = "cca_auth_txlog";
		updateSQL = "status_code = 'Y' ";
		whereStr = "where card_no like ? and mod_pgm not like 'Cnv%' "; 
		setString(1, cardCardNo+"%");
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("cca_auth_txlog not found , cardCardNo = [ %s ]",cardCardNo));
		}
	}


	/************************************************************************/
	public void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName;

		
		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!" + tmpstr2);
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");

	}

	/************************************************************************/
	private Map<String,Object> processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		int j = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;

	}


	/***********************************************************************/
	public String[] getFieldValue(String rec, int[] parm) {
		int x = 0;
		int y = 0;
		byte[] bt = null;
		String[] ss = new String[parm.length];
		try {
			bt = rec.getBytes("MS950");
		} catch (Exception e) {
			showLogMessage("I", "", comc.getStackTraceString(e));
		}
		for (int i : parm) {
			try {
				ss[y] = new String(bt, x, i, "MS950");
			} catch (Exception e) {
				showLogMessage("I", "", comc.getStackTraceString(e));
			}
			y++;
			x = x + i;
		}

		return ss;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD333 proc = new IcuD333();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class FscIcud03 extends hdata.BaseBin {
		public String modAcdr = "";
		public String issuerCode = "";
		public String cardType = "";
		public String cardSeqno = "";
		public String statusCfq = "";
		public String lineOfCreditAmt = "";
		public String unpayAmt = "";
		public String authNotDeposit = "";
		public String acctJrnlBal = "";
		public String jrnlBalSign = "";
		public String unpayAmtCash = "";
		public String openDate = "";
		public String statusCfqDate = "";
		public String locamtCashRate = "";
		public String locamtCashDay = "";
	
		@Override
		public void initData() {
			modAcdr = "";
			issuerCode = "";
			cardType = "";
			cardSeqno = "";
			statusCfq = "";
			lineOfCreditAmt = "";
			unpayAmt = "";
			authNotDeposit = "";
			acctJrnlBal = "";
			jrnlBalSign = "";
			unpayAmtCash = "";
			openDate = "";
			statusCfqDate = "";
			locamtCashRate = "";
			locamtCashDay = "";
		}

	}
	
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/cca", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileName1 + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2FISC", "mput " + fileName1);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileName1 + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileName1);
		}
	}
	
	void procFTP2() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/cca", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileNameTxt + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fileNameTxt);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileNameTxt + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileNameTxt);
		}
	}
	
	void procFTP3() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/icu", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileName1 + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fileName1);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileName1 + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileName1);
		}
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
	
}
