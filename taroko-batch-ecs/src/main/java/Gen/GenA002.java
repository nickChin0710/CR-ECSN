/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  108/07/22  V1.00.01    David     GEN_A002R1改送至報表簽核系統並更名為CCDCD011 *
*  109/11/19  V1.00.02  yanghan       修改了變量名稱和方法名稱                                                                              *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  109/12/30  V1.00.04    Zuwei       “卡務中心”改為”信用卡部”   *
*  110/08/11  V1.00.05  Lai         M:8197                                    *
*  111/11/16  V1.00.06  Zuwei        sync from mega & 增補MEGA 于110/08/11之修訂                                 *
*  111/11/17  V1.00.07  Zuwei        sync from mega                                 *
*  111/11/18  V1.00.08  Grace        miner modify                            *
*  111/11/18  V1.00.09  Zuwei        資料未經過檢核(GenA001)不拋出異常，只showmessage，程式退出    *
*  111/11/21  V1.00.10  Zuwei        CCDCD011改送至報表簽核系統並更名為GEN_A002R1，binary處理取消    *
*  111/11/22  V1.00.10  Grace/Zuwei  調整報表寫入ptr_batch_rpt, 調整程式, 重新命名(rptIdRx, lparx),extract function & format code & extract update table code to a function & code refactor   *
*                                    fix 'TOHOST.txt' dir for match ftpProc() local Dir   *   
*                                    增加產生ACB0055功能                                                                                                 *   
*  112/01/05  V1.00.11  Zuwei        remark callGA003   *
*  112/01/18  V1.00.12  Zuwei        R2報表 report header 重複 
*              R3報表, 1. 科子細目代號, 應為 ac_no;
*                    2. 不同幣別時, "筆數", "合計" 應init 為 0   
*                    3.明細時, "筆數", "借方金額"、"貸方金額" 不需累計  *
*  112/02/01  V1.00.13  Zuwei        ACB0055.txt, 設定幣別只得為'00'者; R2 錯誤修訂，R3金額筆數右靠
*                                    R1 金額欄位增寬2，R2借貸金額欄位錯位，R3換頁時多個空白行，R2 的借貸方金額, 沒被放入for - loop，order by修改
*  112/02/23  V1.00.14  Zuwei        ACB0055.txt更名為A401_ACCOUNT.TXT, ’資料來源’欄位取mod_pgm前8碼，傳送目錄改為NCR2TCB
*  112/06/07  V1.00.15  Ryan        檔案名稱異動、檔案目錄異動 、檔案的第一個欄位改為1,異動VOUCH_CLOSE_FLAG 
*  112/12/11  V1.00.16  Zuwei Su    errExit改為 show message & exit program  *  
*  112/12/19  V1.00.17  Zuwei Su    errRtn改為 show message & return 1  
*  113/01/04  V1.00.18  Ryan        modify checkApproval *  
******************************************************************************/

package Gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.Big52CNS.CnsResult;
//import com.sun.org.apache.xpath.internal.operations.String;
import com.*;

@SuppressWarnings("unchecked")
/* 日結軋帳處理程式 */
public class GenA002 extends AccessDAO {
	private final String PROGNAME = "日結軋帳處理程式  113/01/04  V1.00.18";
	
	private static final  String A401_ACCOUNT01 = "A401_ACCOUNT01.txt"; 
	private static final  String A401_ACCOUNT02 = "A401_ACCOUNT02.txt"; 
	private static final  String FILE_PATH = "/media/gen/"; 
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrdRoutine comcr = null;
	CommCpi comcpi = new CommCpi();

	final int DEBUG = 0;
	int totCnt = 0;
	String tempStr = "";

	String prgmId = "GenA002";
	String rtpName1 = "交易序號使用明細表-帳務";
	String rtpName2 = "簽帳卡 連線業務轉帳日記帳";
	String rtpName3 = "簽帳卡 連線業務科目別軋對表";
//    String rtpName4 = "信用卡會計業務刪除及修改交易明細表";
	String rtpName40 = "信用卡會計業務刪除及修改交易明細表(依套號)";
	String rtpName41 = "信用卡會計業務刪除及修改交易明細表(依科目)";
	String rtpName5 = "TOHOST 檔案";
	String rptIdR1 = "GEN_A002R1";
//    String rptIdR1 = "CCDCD011";
	String rptIdR2 = "GEN_A002R2";
	String rptIdR3 = "GEN_A002R3";
	String rptIdR4 = "CCDCD004";
	String rptIdR5 = "TOHOST";
	String fileName = "";
	int actCnt = 0;
//    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();	//rename to lpar1
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
//    List<Map<String, Object>> lpar0 = new ArrayList<Map<String, Object>>();	//rename to lpar40
//    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();	//rename to lpar41
	List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar3 = new ArrayList<Map<String, Object>>();
//    List<Map<String, Object>> lpar4 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar40 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar41 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar5 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar6 = new ArrayList<Map<String, Object>>();
	String hCallBatchSeqno = "";
	String errCode = "";
	String errDesc = "";
	String procDesc = "";
	int rptSeq = 0;
	int rptSeq1 = 0;
	int rptSeq2 = 0;
	int rptSeq3 = 0;
	int rptSeq4 = 0;
	int rptSeq5 = 0;
	int errCnt = 0;
	String errMsg = "";

	String hTempUser = "";
	String hTempManager = "";
	String hTempName = "";
	int tempInt = 0;
	String hBusinessDate = "";
	String hTempX01 = "";
	String hVouchDate = "";
	String hSystemDate = "";
	String hSystemDatef = "";
	String hVoucTxDate = "";
	String hVoucRefno = "";
	String hVoucCurr = "";
	String hVoucUserId = "";
	String hVoucManager = "";
	String hVoucJrnStatus = "";
	String hVoucBrno = "";
	String hVoucModPgm = "";
	String hVoucModLog = "";
	String hDate = "";
	String hTime = "";
	int hVoucVoucherCnt = 0;
	double hAmtDr = 0;
	double hAmtCr = 0;
	String hEngName = "";
	String hTempX10 = "";
	String hTempX11 = "";
	String hVoucAcNo = "";
	String hVoucDbcr = "";
	String hVoucMemo1 = "";
	String hVoucMemo2 = "";
	String hVoucMemo3 = "";
	int hVoucSeqno = 0;
	String hTempX30 = "";
	String hAllCurr = "";
	String hTempCurr = "";
	String tempCurr = "";
	int hCntDr = 0;
	int hCntCr = 0;
	String hVoucDept = "";
	String hVoucDepno = "";
	double hVoucAmt = 0;
	String hVoucModUser = "";
	String hVoucIdNo = "";
	String hVoucIfrsFlag = "";
	long hModSeqno = 0;
	String hErrorCode = "";
	String hErrorDesc = "";
	String hRtn0Curr = "";
	String hRtn0ModUser = "";
	String hPrintName = "";
	String hPgeaHandUserId = "";
	String hEmplEmployNo = "";
	String hPgeaHandManagerId = "";
	String hEmplChiName = "";
	String hPgeaR6UserId = "";
	String hPgeaR6ManagerId = "";

	String buf = "";
	int cntR1 = 0;
	int cntR2 = 0;
	int cntR3 = 0;
	int sCntDr = 0;
	int sCntCr = 0;
	double sumAmt2Dr = 0;
	double sumAmt2Cr = 0;
	double sAmtDr = 0;
	double sAmtCr = 0;
	private String hCallErrorDesc;
	private String hModUser;
	private String hModTime;
	private String hModPgm;
	private String hVoucModTime;
	private long hVoucModSeqno;
	private int pageCnt = 0;
	private int pageCnt1 = 0;
	private int pageCnt2 = 0;
	private int pageCnt3 = 0;
	private int totalCount;
	private int lineCnt;
	private int tmpCount;
	private int tmpInt = 0;
	private String hRtn0Brno;
	private int pageCnt0 = 0;

	private String reportH1 = "　                分 錄                       交易";
	private String reportH = "交易日期 交易序號 分錄筆數   交易狀態 交易作業名稱  交易幣別  交   易   金   額    交   易   鍵   值   經辦人 主管及處理時間";
	private String reportL = "======= ======== ======== ======== ============ ======== =================== ==================== ====== ====================";
//	private String reportL = "======== ======== ======== ======== ============ ======== =================== ==================== ====== ====================";
//	private String reportH2 = "確認單號碼 幣別 科子細目代號   名     稱                     借    方  金   額 貸    方  金   額 櫃  員 主  管 備  註";
	private String reportH2 = "確認單號碼 幣別 科子細目代號   名     稱                       借    方  金   額 貸    方  金   額 櫃  員 主  管 備  註";	
	private String reportL2 = "========== ==== ============================================ ================= ================= ====== ====== ======";
	private String reportH3 = "筆數 借    方  金   額 科子細目代號   名     稱             貸    方  金   額 筆數";
	private String reportL3 = "==== ================= ==================================== ================= ====";
	private String reportH4 = "部門別 套  號 序號 幣別 科    目                借貸別        金    額 摘要一               摘要二               經辦姓名  解覆核主管";
	private String reportH0 = "會計套號            交易狀態            交易代號              幣別                 交易金額     經辦代號            解覆核主管";
	private String reportL0 = "=====================================================================================================================================";
	private String reportT01 = "製表：                           覆核：                           會計：                           經副襄理：";
	private String reportT02 = "附註：請會計核對本報表「刪除及修改交易明細表」所列之交易序號等內容與業務部門列印之「刪除及修改交易明細表」及所附之「確認單」";
	private String reportT03 = "或「工作單」、相關原始憑證是否相符、齊全，並由會計簽核本表。";
	private String tempX3;
	private String tempX31;
	private String tempX06;
	private String hRtn0Refno;
	private String hRtn0ModPgm;
	private String hRtn0ModLog;
	private String hRtn0Manager;
	private double hRtn0Amt;
	private int lineCnt0;
	private int totalCount0;

	String hEflgFileName = "";
	String hEflgProcCode = "";
	String hEflgProcDesc = "";
	String hEflgRowid = "";
	String hEflgRefIpCode = "";
	String tmpstr = "";
	String filename1 = "";
	String filename2 = "";
	String filename3 = "";
	String filename4 = "";
	byte[] ibmChi = { 0x0E, 0x0F, 0x00 };

	// ************************************************************************

	public static void main(String[] args) throws Exception {
		GenA002 proc = new GenA002();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	// ************************************************************************

	public int mainProcess(String[] args) {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME + "," + args.length);
			// =====================================
			if (args.length > 2) {
//				comc.errExit("Usage : gen_a002 [date] callbatch_seqno", "");
                showLogMessage("I", "", "Usage : gen_a002 [date] callbatch_seqno");
                return 0;
			}

			// 固定要做的

			if (!connectDataBase()) {
//				comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error");
                return 1;
			}

			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

			String checkHome = comc.getECSHOME();
			if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
				comcr.hCallBatchSeqno = "no-call";
			}

			comcr.hCallRProgramCode = javaProgram;
			hTempUser = "";
			if (comcr.hCallBatchSeqno.length() == 20) {
				comcr.callbatch(0, 0, 1);
				selectSQL = " user_id ";
				daoTable = "ptr_callbatch";
				whereStr = "WHERE batch_seqno   = ?  ";

				setString(1, comcr.hCallBatchSeqno);
				int recCnt = selectTable();
				hTempUser = getValue("user_id");
			}
			if (hTempUser.length() == 0) {
				hTempUser = comc.commGetUserID();
			}

			// =====================================
			if(checkApproval()==1) {
				updateSQL = " VOUCH_CHK_FLAG   = 'N' , " + " VOUCH_CLOSE_FLAG = decode(VOUCH_CLOSE_FLAG,'N','1','1','Y',VOUCH_CLOSE_FLAG) , " + " mod_pgm          = ?     ";
				daoTable = "ptr_businday";
//				setString(1, fileName.equals(A401_ACCOUNT01) ? "1" : "Y");
				setString(1, javaProgram);
				int n = updateTable();
				showLogMessage("I", "", "checkApproval()==1 : GenA001 檢核有誤 或 無傳票待軋帳 !! ,update = " + n);
				finalProcess();
				return 0;
			}
				
			// =====================================
			// 取日期
			commonRtn();
			hModPgm = prgmId;
			hVoucModPgm = hModPgm;
			hVoucModTime = hModTime;
			hVoucModUser = hModUser;
			hVoucModSeqno = hModSeqno;
			if (args.length > 0) {
				if (args[0].length() == 8) {
					String sGArgs0 = "";
					sGArgs0 = args[0];
					sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
					hBusinessDate = sGArgs0;
				}
			}

			showLogMessage("I", "", "處理日期=[" + hBusinessDate + "]" + Integer.toString(args.length));

			// ================================
			sqlCmd = "select to_char(sysdate,'yyyymmdd')  h_system_date, ";
			sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
			sqlCmd += "  from dual ";
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hSystemDate = getValue("h_system_date");
				hSystemDatef = getValue("h_system_date_f");
			}
			// ================================
			tempInt = 0;
			selectSQL = "count(*) temp_int";
			daoTable = "ptr_holiday";
			whereStr = "where holiday = ?";
			setString(1, hBusinessDate);

			if (selectTable() > 0) {
				tempInt = getValueInt("temp_int");
			}

			if (tempInt > 0) {
				String err1 = "錯誤: 營業日為假日!!";
				String err2 = "";
//				comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
				showLogMessage("I", "", err1 );
                return 1;
			}
			// =============================================
			if (checkGenA001() == -1) {
				return -1;
			}
			// =============================================
//			callGenA003();

			/*
			 * for test lai
			 */
			// =============================================
			insertGenVouchH();
			// ==================================================

//            String filename2 = String.format("%s/NUACJR08", comc.getIBMftp());
//            openBinaryOutput(filename2);
			selectGenVouch1(); // GEN_A002R1
			selectGenVouch2(); // GEN_A002R2
			selectAllCurrCode(); // GEN_A002R3

			// comc.writeReport("GEN_A002R3.txt", lpar3); //刪除 --> remark by grace, move to
			// selectGenVouch3()

			writeRtn(); // TOHOST(lpar5), 含lpar40 (CCDCD004_0)
			/* CCDCD004 */
			writeReport();
			/* ACB0055 */
			writeReportACB0055();
			/* update table info */
			updateTableData();

//remark by grace, move to selectGenVouch1() (start)-------------------------
//            comcr.deletePtrBatchRpt(rptIdR1, sysDate);
//            comcr.insertPtrBatchRpt(lpar);
//            String filename1 = "GEN_A002R1_" + hSystemDatef;
//          //String filename = "CCDCD011." + chinDate;
//            comc.writeReport(comc.getECSHOME() + "/reports/" + filename1, lpar); // for test
//            ftpProc(filename1, "GEN_A002R1"); //暫不ftp, for check , grace
//            //ftpProc(filename1, "CCDCD011");
//            remark by grace, move to selectGenVouch1() (end)-------------------------

			// ==================================================

			// 固定要做的

			comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
			showLogMessage("I", "", comcr.hCallErrorDesc);

			if (comcr.hCallBatchSeqno.length() == 20)
				comcr.callbatch(1, 0, 1); // 1: 結束

			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	private int checkApproval() throws Exception {
		selectSQL = "crt_user";
		daoTable = "gen_user_log";
		whereStr = "where progran_cd = ? ";
		setString(1, prgmId);

		hTempManager = selectTable() > 0 ? getValue("crt_user") : "";
		showLogMessage("I", "", " 1. Manager=[" + hTempManager + "]" + prgmId);
		// ----------------------------------
		selectSQL = "usr_cname";
		daoTable = "sec_user";
		whereStr = "where usr_id = ?";
		setString(1, hTempUser);

		hTempName = selectTable() > 0 ? getValue("usr_cname") : "";

		selectSQL = "usr_cname";
		daoTable = "sec_user";
		whereStr = "where usr_id = ?";
		setString(1, hTempManager);

		hTempManager = selectTable() > 0 ? getValue("usr_cname") : "";
		// ----------------------------------
		if (hTempManager.length() == 0) {
			String err1 = "資料未經過覆核(gen_user_log)!!" + hTempUser + "," + hTempManager;
			String err2 = "";
//			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
			showLogMessage("I", "", err1 );
			return 1;
		}
		return 0;
	}

	private int checkGenA001() throws Exception {
		selectSQL = "decode(VOUCH_CHK_FLAG , '' ,'N' ,VOUCH_CHK_FLAG) as VOUCH_CHK_FLAG, "
				+ " decode(VOUCH_CLOSE_FLAG , '' ,'N' ,VOUCH_CLOSE_FLAG) as VOUCH_CLOSE_FLAG ";
		daoTable = "ptr_businday";

		if (selectTable() > 0) {
			hTempX01 = getValue("VOUCH_CHK_FLAG");
		}
		if (DEBUG == 1)
			showLogMessage("I", "", " 888 chk=[" + hTempX01 + "]");

		if (hTempX01.compareTo("Y") != 0) {
			String err1 = "資料未經過檢核 (GenA001)";
			String err2 = hTempX01;
//                comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
			String errMsg = String.format("Error: [%s],[%s]\n", err1, err2);
			showLogMessage("I", "", errMsg + ":" + comcr.hCallBatchSeqno);
			return -1;
		}
		if("N".equals(getValue("VOUCH_CLOSE_FLAG"))) {
			fileName = A401_ACCOUNT01;
		}
		if("1".equals(getValue("VOUCH_CLOSE_FLAG"))) {
			fileName = A401_ACCOUNT02;
		}
		return 0;
	}

	private void callGenA003() throws Exception {
		try {
			String[] newArgs = { comcr.hCallBatchSeqno };
			GenA003 genA003 = new GenA003();
			genA003.mainProcess(newArgs);
		} catch (Exception ex) {
			String err1 = "無法執行 GenA003 ERROR!";
			String err2 = "";
//			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
            showLogMessage("I", "", err1);
            exitProgram(1);
		}
		showLogMessage("I", "", "end GenA003 & continue GenA002...............");
	}

	private void insertGenVouchH() throws Exception {
		sqlCmd = "insert into gen_vouch_h (AC_NO,AMT,APR_USER,BRNO,CRT_USER,CURR,CURR_CODE_DC,DBCR,DEPNO,DEPT,ID_NO,IFRS_FLAG,JRN_STATUS,KEY_VALUE,MEMO1,MEMO2,MEMO3,MOD_PGM,MOD_LOG,MOD_SEQNO,MOD_TIME,MOD_USER,POST_FLAG,REFNO,SEQNO,SIGN_FLAG,SYS_REM,TX_DATE,VOUCHER_CNT) "
				+ "   select AC_NO,AMT,APR_USER,BRNO,CRT_USER,CURR,CURR_CODE_DC,DBCR,DEPNO,DEPT,ID_NO,IFRS_FLAG,JRN_STATUS,KEY_VALUE,MEMO1,MEMO2,MEMO3,MOD_PGM,MOD_LOG,MOD_SEQNO,MOD_TIME,MOD_USER,POST_FLAG,REFNO,SEQNO,SIGN_FLAG,SYS_REM,TX_DATE,VOUCHER_CNT from gen_vouch ";
		sqlCmd += " where decode(post_flag, '', 'N', post_flag) = 'Y' ";
		daoTable = "gen_vouch_h";
		insertTable();

		daoTable = "gen_vouch";
		whereStr = "where DECODE(post_flag, '', 'N', post_flag) = 'Y'  ";
		deleteTable();
	}

	private void updateTableData() throws Exception {
		// ================================================
		daoTable = "gen_vouch";
		updateSQL = "post_flag = 'Y' ";
		whereStr = "where DECODE(post_flag,'', 'N', post_flag) in ('N','n') ";
		whereStr += "  and tx_date  <= ? ";
		setString(1, hBusinessDate);
		updateTable();
		// =========================================
		updateSQL = " VOUCH_CHK_FLAG   = 'N' , " + " VOUCH_CLOSE_FLAG = ? , " + " mod_pgm          = ?     ";
		daoTable = "ptr_businday";
		setString(1, fileName.equals(A401_ACCOUNT01) ? "1" : "Y");
		setString(2, javaProgram);
		updateTable();

		// =========================================
		daoTable = "gen_user_log ";
		updateSQL = "crt_user = '' ";
		whereStr = "where progran_cd = ? ";
		setString(1, prgmId);
		updateTable();
	}

	private void writeReport() throws Exception {
		String buf = "";
		rptSeq = 0;
		if (tmpCount < 1) {
			pageCnt1 = 0;
			hVoucBrno = "3144";
			hRtn0Brno = "3144";
//                printHead1();
//                printHead0();
			printHead41();
			printHead40();
			buf = "本日無資料";
//                lpar1.add(comcr.putReport(prgmId, "CCDCD004_1", sysDate + sysTime, rptSeq, "0", buf+"\r"));
//                lpar0.add(comcr.putReport(prgmId, "CCDCD004_0", sysDate + sysTime, rptSeq, "0", buf+"\r"));
			lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq, "0", buf + "\r"));
			lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq, "0", buf + "\r"));
			rptSeq++;
		}
		buf = "";
		buf = reportL0 + "\r\n";
//            lpar1.add(comcr.putReport(prgmId, "CCDCD004_1", sysDate + sysTime, rptSeq, "0", buf+"\r"));
//            lpar0.add(comcr.putReport(prgmId, "CCDCD004_0", sysDate + sysTime, rptSeq, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		rptSeq++;

		buf = "";
		buf = comcr.insertStr(buf, reportT01 + "\r\n", 8);
//            lpar1.add(comcr.putReport(prgmId, "CCDCD004_1", sysDate + sysTime, rptSeq, "0", buf+"\r"));
//            lpar0.add(comcr.putReport(prgmId, "CCDCD004_0", sysDate + sysTime, rptSeq, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		rptSeq++;

		buf = "";
		buf = comcr.insertStr(buf, reportT02, 4);
//            lpar1.add(comcr.putReport(prgmId, "CCDCD004_1", sysDate + sysTime, rptSeq, "0", buf+"\r"));
//            lpar0.add(comcr.putReport(prgmId, "CCDCD004_0", sysDate + sysTime, rptSeq, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		rptSeq++;

		buf = "";
		buf = comcr.insertStr(buf, reportT03, 10);
//            lpar1.add(comcr.putReport(prgmId, "CCDCD004_1", sysDate + sysTime, rptSeq, "0", buf+"\r"));
//            lpar1.add(comcr.putReport(prgmId, "CCDCD004_1", sysDate + sysTime, rptSeq, "0", "\f"));
//            lpar0.add(comcr.putReport(prgmId, "CCDCD004_0", sysDate + sysTime, rptSeq, "0", buf+"\r"));
//            lpar0.add(comcr.putReport(prgmId, "CCDCD004_0", sysDate + sysTime, rptSeq, "0", "\f"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq, "0", "\f"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq, "0", buf + "\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq, "0", "\f"));
		rptSeq++;

		// comc.writeReport("CCDCD004_0", lpar0);
		// comc.writeReport("CCDCD004_1", lpar1);
		// grace 暫remark, 改為2個報表, 不合併 -----------------------------------
		comcr.deletePtrBatchRpt(rptIdR4, sysDate);
		comcr.insertPtrBatchRpt(lpar40);	
		comcr.insertPtrBatchRpt(lpar41);	
//      filename  = "CCDCD004." + comcr.formatDate(hBusinessDate, 1);
		filename4 = "CCDCD004." + comcr.formatDate(hBusinessDate, 1);
//      lpar0.addAll(lpar1);
		lpar40.addAll(lpar41);
//      comc.writeReport(comc.getECSHOME() + "/reports/" + filename, lpar0);
		comc.writeReport(comc.getECSHOME() + FILE_PATH + filename4, lpar40);
		// =========================================
//            ftpProc(filename4, "CCDCD004");	//暫不ftp, for check, grace
		// ==============================================
	}

	/***********************************************************************/
	void selectGenVouch1() throws Exception {
		selectSQL = "tx_date, ";
		selectSQL += "refno, ";
		selectSQL += "curr , ";
		selectSQL += "crt_user, ";
		selectSQL += "apr_user, ";
		selectSQL += "jrn_status, ";
		selectSQL += "decode(brno,'','3144',brno) h_vouc_brno, ";
		selectSQL += "mod_pgm, ";
		selectSQL += "mod_log, ";
		selectSQL += "to_char(to_number(to_char(mod_time,'yyyymmdd'))-19110000) h_date, ";
		selectSQL += "max(to_char(mod_time,'hh24:mi')) h_time, ";
		selectSQL += "count(refno) h_vouc_voucher_cnt, ";
		selectSQL += "sum(decode(dbcr,'D',amt,0)) h_amt_dr, ";
		selectSQL += "sum(decode(dbcr,'C',amt,0)) h_amt_cr ";
		daoTable = "gen_vouch ";
		whereStr = "where DECODE(post_flag, '', 'N', post_flag) in ('N','n')  ";
		whereStr += "  and jrn_status  in ('3','1')  ";
		whereStr += "  and tx_date     <= ? ";
		whereStr += "group by ";
		whereStr += "tx_date, ";
		whereStr += "refno, ";
		whereStr += "curr , ";
		whereStr += "crt_user, ";
		whereStr += "apr_user, ";
		whereStr += "jrn_status, ";
		whereStr += "decode(brno,'','3144',brno), ";
		whereStr += "mod_pgm, ";
		whereStr += "mod_log, ";
		whereStr += "to_char(to_number(to_char(mod_time,'yyyymmdd'))-19110000) ";
		whereStr += "order by decode(brno,'','3144',brno), ";
		whereStr += "tx_date,curr,refno ";

		setString(1, hBusinessDate);

		int recordCnt = selectTable();
		if (DEBUG == 1)
			showLogMessage("I", "", "ALL CNT 1=[" + recordCnt + "]" + hBusinessDate);

		String buf = "";
//		printHead();
		if (notFound.equals("Y")) {
//            lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "1",
			lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "1",
					"\n\n　             無交易序號使用 \r\n"));
			return;
		}
		lineCnt = 0;
		totalCount = 0;
		cntR1 = 0;
		for (int i = 0; i < recordCnt; i++) {
			hVoucTxDate = getValue("tx_date", i);
			hVoucRefno = getValue("refno", i);
			hVoucCurr = getValue("curr", i);
			hVoucUserId = getValue("crt_user", i);
			hVoucManager = getValue("apr_user", i);
			hVoucJrnStatus = getValue("jrn_status", i);
			hVoucBrno = getValue("h_vouc_brno", i);
			hVoucModPgm = getValue("mod_pgm", i);
			hVoucModLog = getValue("mod_log", i);
			hDate = getValue("h_date", i);
			hTime = getValue("h_time", i);
			hVoucVoucherCnt = getValueInt("h_vouc_voucher_cnt", i);
			hAmtDr = getValueDouble("h_amt_dr", i);
			hAmtCr = getValueDouble("h_amt_cr", i);
			if (DEBUG == 1)
				showLogMessage("I", "", "888 REF No=[" + hVoucRefno + "]");

			printRtn();
			// from mainprocess(), move by grace (start)----------------
			comcr.deletePtrBatchRpt(rptIdR1, sysDate);
			comcr.insertPtrBatchRpt(lpar1);
			String filename1 = "GEN_A002R1_" + hSystemDatef;
//          String filename = "CCDCD011." + chinDate;
			comc.writeReport(comc.getECSHOME() + FILE_PATH + filename1, lpar1);
//            from mainprocess(), move by grace (end)----------------
		}
		if (DEBUG == 1)
			showLogMessage("I", "", "ALL CNT 1 END=[" + totalCount + "]" + cntR1);

	}

// *************************************************************************************
	private void printRtn() throws Exception {
		String buf = "";

		lineCnt++;
		totalCount++;
		cntR1++;

		if (totalCount == 1)
			tempX3 = hVoucBrno;
		if (DEBUG == 1)
			showLogMessage("I", "", "  777 =[" + totalCount + "]" + lineCnt + "," + hVoucBrno + "," + tempX3);
		if (!hVoucBrno.equals(tempX3)) {
//     if(lineCnt > 2 ) lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate+sysTime, rptSeq++, "1", "\f"));
			if (lineCnt > 2)
				lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "1", "\f"));
			printHead();
			lineCnt = 0;
			tempX3 = hVoucBrno;
		}
		if (totalCount == 1 || lineCnt > 25) {
			if (totalCount == 1)
				tempCurr = hVoucCurr;

//      if(lineCnt > 2) lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate+sysTime, rptSeq++, "1", "\f"));
			if (lineCnt > 2)
				lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "1", "\f"));
			printHead();
			if (totalCount != 1)
				lineCnt = 0;
		}
		if (DEBUG == 1)
			showLogMessage("I", "", "  7772=[" + totalCount + "]" + lineCnt + "," + hVoucBrno + "," + tempX3 + ","
					+ hVoucCurr + "," + tempCurr);
		if (!tempCurr.equals(hVoucCurr)) {
//      lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate+sysTime, rptSeq++, "1", "\f"));
			lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "1", "\f"));
			tempCurr = hVoucCurr;
			printHead();
			lineCnt = 0;
		}

		buf = "";
		buf = comcr.insertStr(buf, comcr.formatDate(hVoucTxDate, 1), 1);
		buf = comcr.insertStr(buf, hVoucRefno, 10);
		tempStr = String.format("%3d", hVoucVoucherCnt);
		buf = comcr.insertStr(buf, tempStr, 22);
		buf = comcr.insertStr(buf, "未經覆核", 28);
		if (hVoucJrnStatus.equals("3")) {
			buf = comcr.insertStr(buf, "已經覆核", 28);
		} else if (hVoucJrnStatus.equals("2")) {
			buf = comcr.insertStr(buf, "取消覆核", 28);
		}

		if (hVoucModLog.equals("D"))
			comcr.insertStr(buf, "已被刪除", 28);
		if (hVoucModLog.equals("U"))
			comcr.insertStr(buf, "已被修改", 28);

		String tempX04 = comc.getSubString(hVoucModPgm, 0, 4);
		if (tempX04.equals("genp") || tempX04.equals("w_ge")) {
			buf = comcr.insertStr(buf, "自由格式輸入", 37);
		} else {
			buf = comcr.insertStr(buf, "R6自動起帳", 37);
			hVoucManager = "system";
		}
		// ============================================

		sqlCmd = "select CURR_ENG_NAME  h_eng_name ";
		sqlCmd += "  from ptr_currcode  ";
		sqlCmd += " where curr_code_gl = ? ";
		setString(1, hVoucCurr);
		int cnt = selectTable();
		hEngName = cnt > 0 ? getValue("h_eng_name") : "";

		// buf = comcr.insertStr(buf, "TWD" , 47);
		buf = comcr.insertStr(buf, hEngName, 54);
//		tempStr = String.format("%14.14s", comcr.formatNumber(hAmtDr + "", 2, 2)); // 1:不加'$',2:加'$'
		tempStr = String.format("%16.16s", comcr.formatNumber(hAmtDr + "", 2, 2)); // 1:不加'$',2:加'$'		
//		buf = comcr.insertStr(buf, tempStr, 64);
		buf = comcr.insertStr(buf, tempStr, 60);
		buf = comcr.insertStr(buf, "    ", 85);

		String tempX06 = "";
		String fun = hVoucRefno.substring(2, 3);
		tempX06 = comcr.getVouchTypeCnName(fun);
//            if (fun.equals("0"))
//                tempX06 = "共用";
//            else if (fun.equals("1"))
//                tempX06 = "作業";
//            else if (fun.equals("2"))
//                tempX06 = "風管";
//            else if (fun.equals("3"))
//                tempX06 = "催收";
//            else if (fun.equals("4"))
//                tempX06 = "發卡";
//            else if (fun.equals("5"))
//                tempX06 = "客服";
//            else if (fun.equals("6"))
//                tempX06 = "行銷";
//            else if (fun.equals("7"))
//                tempX06 = "授權";
//            else
//                tempX06 = "共用";

		if (hVoucManager.toLowerCase().equals("system")) {
			buf = comcr.insertStr(buf, tempX06, 100);
		} else {
			sqlCmd = "select usr_cname  h_temp_x10 ";
			sqlCmd += "  from sec_user   ";
			sqlCmd += " where usr_id = ? ";
			setString(1, hVoucUserId);
			cnt = selectTable();
			hTempX10 = cnt > 0 ? getValue("h_temp_x10") : hVoucUserId;
			buf = comcr.insertStr(buf, hTempX10, 100);
		}

		if (hVoucManager.toLowerCase().equals("system")) {
			hTempX10 = hTempManager;
		} else {
			sqlCmd = "select usr_cname  h_temp_x10 ";
			sqlCmd += "  from sec_user   ";
			sqlCmd += " where usr_id = ? ";
			setString(1, hVoucManager);
			cnt = selectTable();
			hTempX10 = cnt > 0 ? getValue("h_temp_x10") : hVoucManager;
		}

		tmpstr = String.format("%08d", comcr.str2long(hDate));

		buf = comcr.insertStr(buf, hTempX10, 107);
		buf = comcr.insertStr(buf, hDate, 114);
		buf = comcr.insertStr(buf, hTime, 122);

//            lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "1", buf+"\r"));
		lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "1", buf + "\r"));
	}

	// ************************************************************************
	void selectGenVouch2() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "tx_date, ";
		sqlCmd += "refno, ";
		sqlCmd += "curr , ";
		sqlCmd += "crt_user, ";
		sqlCmd += "apr_user, ";
		sqlCmd += "jrn_status, ";
		sqlCmd += "decode(brno,'','3144',brno) h_vouc_brno, ";
		sqlCmd += "mod_pgm, ";
		sqlCmd += "ac_no, ";
		sqlCmd += "dbcr , ";
		sqlCmd += "memo1, ";
		sqlCmd += "memo2, ";
		sqlCmd += "memo3, ";
		sqlCmd += "seqno , ";
		sqlCmd += "to_char(to_number(to_char(mod_time,'yyyymmdd'))-19110000) h_date, ";
		sqlCmd += "to_char(mod_time,'hh24:mi') h_time, ";
		sqlCmd += "count(refno) h_vouc_voucher_cnt, ";
		sqlCmd += "sum(decode(dbcr,'D',amt,0)) h_amt_dr, ";
		sqlCmd += "sum(decode(dbcr,'C',amt,0)) h_amt_cr ";
		sqlCmd += " from gen_vouch ";
		sqlCmd += "where decode(post_flag, '', 'N', post_flag)     in ('N','n')  ";
		sqlCmd += "  and decode(mod_log  , '', '1', mod_log  ) not in ('D','U')  ";
		sqlCmd += "  and jrn_status  in ('3')  ";
		sqlCmd += "  and tx_date  <= ? ";
		sqlCmd += "group by ";
		sqlCmd += "tx_date, ";
		sqlCmd += "refno, ";
		sqlCmd += "curr , ";
		sqlCmd += "crt_user, ";
		sqlCmd += "apr_user, ";
		sqlCmd += "jrn_status, ";
		sqlCmd += "decode(brno,'','3144',brno), ";
		sqlCmd += "mod_pgm, ";
		sqlCmd += "ac_no, ";
		sqlCmd += "dbcr , ";
		sqlCmd += "memo1, ";
		sqlCmd += "memo2, ";
		sqlCmd += "memo3, ";
		sqlCmd += "seqno , ";
		sqlCmd += "to_char(to_number(to_char(mod_time,'yyyymmdd'))-19110000), ";
		sqlCmd += "to_char(mod_time,'hh24:mi') ";
		sqlCmd += "order by tx_date,curr,refno,decode(dbcr,'D','A'),seqno ";

		setString(1, hBusinessDate);
		int recordCnt = selectTable();

		if (DEBUG == 1)
			showLogMessage("I", "", "ALL CNT 2=[" + recordCnt + "]" + hBusinessDate);
		rptSeq = 0;
		if (notFound.equals("Y")) {
			printHead2();
			buf = "";
			buf = comcr.insertStr(buf, "  合     計 : TWD 借方金額  $0  貸方金額  $0", 10);
			lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "1", buf + "\r"));
			return;
		}
		if (DEBUG == 1)
			showLogMessage("I", "", "888 rtn 2 cnt=[" + recordCnt + "]");
		lineCnt = 0;
		totalCount = 0;
		cntR2 = 0;
		for (int i = 0; i < recordCnt; i++) {
			hVoucTxDate = getValue("tx_date", i);
			hVoucRefno = getValue("refno", i);
			hVoucCurr = getValue("curr", i);
			hVoucUserId = getValue("crt_user", i);
			hVoucManager = getValue("apr_user", i);
			hVoucJrnStatus = getValue("jrn_status", i);
			hVoucBrno = getValue("h_vouc_brno", i);
			hVoucModPgm = getValue("mod_pgm", i);
			hVoucAcNo = getValue("ac_no", i);
			hVoucDbcr = getValue("dbcr", i);
			hVoucMemo1 = getValue("memo1", i);
			hVoucMemo2 = getValue("memo2", i);
			hVoucMemo3 = getValue("memo3", i);
			hVoucSeqno = getValueInt("seqno", i);
			hDate = getValue("h_date", i);
			hTime = getValue("h_time", i);
			hVoucVoucherCnt = getValueInt("h_vouc_voucher_cnt", i);
			hAmtDr = getValueDouble("h_amt_dr", i);
			hAmtCr = getValueDouble("h_amt_cr", i);

			printRtn2();
		}

		if (cntR2 > 0)
			printSum2();
		// add by grace
		comcr.deletePtrBatchRpt(rptIdR2, sysDate);
		comcr.insertPtrBatchRpt(lpar2);
		String filename2 = "GEN_A002R2_" + hSystemDatef;
		comc.writeReport(comc.getECSHOME() + FILE_PATH + filename2, lpar2); // for test

	}

// ************************************************************************
	void printRtn2() throws Exception {
		lineCnt++;
		totalCount++;
		cntR2++;

		if (totalCount == 1 || lineCnt > 25) {
			if (totalCount == 1)
				tempCurr = hVoucCurr;

			if (lineCnt > 2)
				lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "1", "\f"));
			printHead2();
			if (totalCount != 1)
				lineCnt = 0;
		}
		if (!tempCurr.equals(hVoucCurr)) {
			lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "1", "\f"));
			tempCurr = hVoucCurr;
			printHead2();
			lineCnt = 0;
		}
//		hAmtDr = getValueDouble("h_amt_dr");
//		hAmtCr = getValueDouble("h_amt_cr");

		sumAmt2Dr += hAmtDr;
		sumAmt2Cr += hAmtCr;

		sqlCmd = "select AC_BRIEF_NAME  h_temp_x30 ";
		sqlCmd += "  from gen_acct_m  ";
		sqlCmd += " where ac_no = ?  ";
		setString(1, hVoucAcNo);

		hTempX30 = selectTable() > 0 ? getValue("h_temp_x30") : "";

		sqlCmd = "select CURR_ENG_NAME  h_eng_name ";
		sqlCmd += "  from ptr_currcode  ";
		sqlCmd += " where curr_code_gl = ? ";
		// setString(1, hTempCurr);
		setString(1, hVoucCurr);
		hEngName = selectTable() > 0 ? getValue("h_eng_name") : "";

		buf = "";
		buf = comcr.insertStr(buf, hVoucRefno, 1);
//		buf = comcr.insertStr(buf, hVoucCurr, 12);
		buf = comcr.insertStr(buf, hEngName, 12);
		buf = comcr.insertStr(buf, hVoucAcNo, 17);
		buf = comcr.insertStr(buf, hTempX30, 26);
		if (hAmtDr != 0) {
			tmpstr = String.format("%18.18s", comcr.formatNumber(hAmtDr + "", 2, 2));
			buf = comcr.insertStr(buf, tmpstr, 62);
		}
		if (hAmtCr != 0) {
			tmpstr = String.format("%18.18s", comcr.formatNumber(hAmtCr + "", 2, 2));
			buf = comcr.insertStr(buf, tmpstr, 80);
		}

		String tempX06 = "";
		String fun = hVoucRefno.substring(2, 3);
		tempX06 = comcr.getVouchTypeCnName(fun);
//  if (fun.equals("0"))
//      tempX06 = "共用";
//  else if (fun.equals("1"))
//      tempX06 = "作業";
//  else if (fun.equals("2"))
//      tempX06 = "風管";
//  else if (fun.equals("3"))
//      tempX06 = "催收";
//  else if (fun.equals("4"))
//      tempX06 = "發卡";
//  else if (fun.equals("5"))
//      tempX06 = "客服";
//  else if (fun.equals("6"))
//      tempX06 = "行銷";
//  else if (fun.equals("7"))
//      tempX06 = "授權";
//  else
//      tempX06 = "共用";

		if (hVoucManager.toLowerCase().equals("system")) {
			buf = comcr.insertStr(buf, tempX06, 100);
		} else {
			sqlCmd = "select usr_cname  h_temp_x10 ";
			sqlCmd += "  from sec_user   ";
			sqlCmd += " where usr_id = ? ";
			setString(1, hVoucUserId);
			hTempX10 = selectTable() > 0 ? getValue("h_temp_x10") : "";
			buf = comcr.insertStr(buf, hTempX10, 100);
		}

		if (hVoucManager.toLowerCase().equals("system")) {
			hTempX10 = hTempManager;
		} else {
			sqlCmd = "select usr_cname  h_temp_x10 ";
			sqlCmd += "  from sec_user  ";
			sqlCmd += " where usr_id =? ";
			setString(1, hVoucManager);
			hTempX10 = selectTable() > 0 ? getValue("h_temp_x10") : "";
		}
		buf = comcr.insertStr(buf, hTempX10, 105);

		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "1", buf + "\r"));

	}

// ************************************************************************
	void selectAllCurrCode() throws Exception {

		sqlCmd = "select DECODE(curr, '', '00', curr)  h_all_curr ";
		sqlCmd += "  from gen_vouch  ";
		sqlCmd += " where decode(post_flag, '', 'N', post_flag)  in ('N','n')  ";
		sqlCmd += "   and decode(mod_log  , '', '1', mod_log  ) not in ('D','U')  ";
		sqlCmd += "   and jrn_status  in ('3')  ";
		sqlCmd += "   and tx_date     <= ? group by DECODE(curr, '', '00', curr) ";
		setString(1, hBusinessDate);
		int recordCnt = selectTable();

		for (int i = 0; i < recordCnt; i++) {
			hTempCurr = getValue("h_all_curr", i);
			selectGenVouch3();
			if (i < recordCnt - 1) {
				// R3, 換頁時多個空白行
				lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", "\r"));
			}
		}
	}

	void selectGenVouch3() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "curr , ";
		sqlCmd += "ac_no, ";
		sqlCmd += "sum(decode(dbcr,'D',1,0)) h_cnt_dr, ";
		sqlCmd += "sum(decode(dbcr,'C',1,0)) h_cnt_cr, ";
		sqlCmd += "sum(decode(dbcr,'D',amt,0)) h_amt_dr, ";
		sqlCmd += "sum(decode(dbcr,'C',amt,0)) h_amt_cr ";
		sqlCmd += " from gen_vouch ";
		sqlCmd += "where decode(post_flag, '', 'N', post_flag) in ('N','n')  ";
		sqlCmd += "  and decode(mod_log  , '', '1', mod_log  ) not in ('D','U')  ";
		sqlCmd += "  and jrn_status  in ('3')   ";
		sqlCmd += "  and tx_date     <= ? ";
		sqlCmd += "  and decode(curr, '', '00', curr)  = ? ";
		sqlCmd += "group by ";
		sqlCmd += "curr , ";
		sqlCmd += "ac_no ";
		sqlCmd += "order by curr ,ac_no ";

		setString(1, hBusinessDate);
		setString(2, hTempCurr);
		int recordCnt = selectTable();

		if (DEBUG == 1)
			showLogMessage("I", "", "ALL CNT 3=[" + recordCnt + "]" + hBusinessDate);
		String buf = "";
		printHead3();
		if (notFound.equals("Y")) {
			buf = "";
			buf = comcr.insertStr(buf, "  合 計 : 借方  筆數 0  金額  $0        貸方 筆數 0   金額  $0", 10);
			lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "1", buf + "\r"));
			return;
		}
		// 不同幣別時, "筆數", "合計" 應init 為 0
		sAmtCr = 0;
		sAmtDr = 0;
		sCntDr = 0;
		sCntCr = 0;
		for (int i = 0; i < recordCnt; i++) {
			hVoucCurr = getValue("tx_date", i);
			hVoucAcNo = getValue("ac_no", i);
			hCntDr = getValueInt("h_cnt_dr", i);
			hCntCr = getValueInt("h_cnt_cr", i);
			hAmtDr = getValueDouble("h_amt_dr", i);
			hAmtCr = getValueDouble("h_amt_cr", i);
			sAmtDr += hAmtDr;
			sAmtCr += hAmtCr;
			sCntDr += hCntDr;
			sCntCr += hCntCr;

			sqlCmd = "select substrb(AC_BRIEF_NAME,1,26)  h_temp_x30 ";
			sqlCmd += "  from gen_acct_m  ";
			sqlCmd += " where ac_no = ? ";
			setString(1, hVoucAcNo);
			selectTable();

			buf = "";
			tmpstr = String.format("%5.5s", sCntDr);
			buf = comcr.insertStr(buf, tmpstr, 1);
//			if (hAmtDr != 0) {
				tmpstr = String.format("%18.18s", comcr.formatNumber(hAmtDr + "", 2, 2));
				buf = comcr.insertStr(buf, tmpstr, 6);
//			}
			buf = comcr.insertStr(buf, hVoucAcNo, 24);
			buf = comcr.insertStr(buf, hTempX30, 33);
//			if (hAmtCr != 0) {
				tmpstr = String.format("%18.18s", comcr.formatNumber(hAmtCr + "", 2, 2));
				buf = comcr.insertStr(buf, tmpstr, 61);
//			}
			tmpstr = String.format("%5.5s", sCntCr);
			buf = comcr.insertStr(buf, tmpstr, 79);
			lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
		}

		printSum3();
		// from mainprocess(), move by grace (start)------------------------
		comcr.deletePtrBatchRpt(rptIdR3, sysDate);
		comcr.insertPtrBatchRpt(lpar3);

		String filename3 = "GEN_A002R3_" + hSystemDatef;
		comc.writeReport(comc.getECSHOME() + FILE_PATH + filename3, lpar3); // for test
//        from mainprocess(), move by grace (end)------------------------
	}

	// ************************************************************************
	void writeRtn() throws Exception {
		pageCnt = 0;
		totalCount = 0;
		lineCnt = 0;
		tmpCount = 0;

		sqlCmd = "select ";
		sqlCmd += "decode(brno,'','3144',brno) h_vouc_brno, ";
		sqlCmd += "tx_date, ";
		sqlCmd += "dept , ";
		sqlCmd += "depno , ";
		sqlCmd += "refno , ";
		sqlCmd += "curr , ";
		sqlCmd += "seqno , ";
		sqlCmd += "ac_no , ";
		sqlCmd += "dbcr, ";
		sqlCmd += "amt , ";
		sqlCmd += "crt_user, ";
		sqlCmd += "apr_user, ";
		sqlCmd += "mod_user, ";
		sqlCmd += "mod_pgm, ";
		sqlCmd += "mod_log, ";
		sqlCmd += "id_no, ";
		sqlCmd += "voucher_cnt, ";
		sqlCmd += "memo1 , ";
		sqlCmd += "memo2 , ";
		sqlCmd += "memo3, ";
		sqlCmd += "DECODE(ifrs_flag, '', 'B', ifrs_flag) h_vouc_ifrs_flag ";
		sqlCmd += " from gen_vouch ";
		sqlCmd += "where DECODE(post_flag, '', 'N', post_flag) in ('N','n')  ";
		sqlCmd += "  and jrn_status  in ('3')  ";
		sqlCmd += "  and tx_date     <= ? ";
		sqlCmd += "order by decode(brno,'','3144',brno),tx_date,refno,seqno ";
		setString(1, hBusinessDate);
		int recordCnt = selectTable();

		if (DEBUG == 1)
			showLogMessage("I", "", "888 write rtn cnt=[" + recordCnt + "]");
		totCnt = recordCnt;

		String buf = "";
		int countAll = 0;
		double allAmt = 0;
		lineCnt0 = 0;
		totalCount0 = 0;
		lineCnt = 0;
		totalCount = 0;
		for (int i = 0; i < recordCnt; i++) {
			hVoucBrno = getValue("h_vouc_brno", i);
			hVoucTxDate = getValue("tx_date", i);
			hVoucDept = getValue("dept", i);
			hVoucDepno = getValue("depno", i);
			hVoucRefno = getValue("refno", i);
			hVoucCurr = getValue("curr", i);
			hVoucSeqno = getValueInt("seqno", i);
			hVoucAcNo = getValue("ac_no", i);
			hVoucDbcr = getValue("dbcr", i);
			hVoucAmt = getValueDouble("amt", i);
			hVoucUserId = getValue("crt_user", i);
			hVoucManager = getValue("apr_user", i);
			hVoucModUser = getValue("mod_user", i);
			hVoucModPgm = getValue("mod_pgm", i);
			hVoucModLog = getValue("mod_log", i);
			hVoucIdNo = getValue("id_no", i);
			hVoucVoucherCnt = getValueInt("voucher_cnt", i);
			hVoucMemo1 = getValue("memo1", i);
			hVoucMemo2 = getValue("memo2", i);
			hVoucMemo3 = getValue("memo3", i);
			hVoucIfrsFlag = getValue("h_vouc_ifrs_flag", i);

			if (DEBUG == 1)
				showLogMessage("I", "", "888 write refno  =[" + hVoucRefno + "]" + hVoucAcNo + "," + hVoucBrno);
			if (hVoucModLog.compareTo("D") != 0 && hVoucModLog.compareTo("U") != 0) {
				countAll++;
				buf = comcr.insertStr(buf, hVoucBrno, 1);
				tmpstr = String.format("%08d", comcr.str2long(hVoucTxDate) - 19110000);
				buf = comcr.insertStr(buf, tmpstr, 4);
				buf = comcr.insertStr(buf, "08", 12);
				buf = comcr.insertStr(buf, hVoucDepno, 14);
				buf = comcr.insertStr(buf, hVoucRefno, 15);
				buf = comcr.insertStr(buf, hVoucCurr, 21);
				String sVoucSeqno = String.format("%04d", hVoucSeqno);
				buf = comcr.insertStr(buf, sVoucSeqno, 23);
				buf = comcr.insertStr(buf, hVoucAcNo, 27);
//                if(h_vouc_ac_no.length() > 6) {
//                if (hVoucAcNo.substring(0, 6).equals("175101")) {
//                    buf = comcr.insertStr(buf, "17510100", 27);
//                     }
//                }
				buf = comcr.insertStr(buf, hVoucDbcr, 36);
				String mk = hVoucAmt > 0 ? "+" : "-";
				buf = comcr.insertStr(buf, mk, 37);
				allAmt += hVoucAmt;
				tmpstr = String.format("%17.2f", hVoucAmt);
				buf = comcr.insertStr(buf, tmpstr + "", 38);
// if(DEBUG == 1) showLogMessage("I", "", "  887 amt=["+h_vouc_amt+"]"+String.format("%9f",all_amt)+","+tmpstr );
// if(DEBUG == 1) showLogMessage("I", "", "  888 write=["+ buf +"]");

				selectPtrGenA002();
				selectCrdEmployee();
				selectCrdEmployee1();

				buf = comcr.insertStr(buf, hEmplEmployNo, 54);
				buf = comcr.insertStr(buf, hEmplChiName, 59);
//                tmpstr = h_vouc_user_id;
//                if(tmpstr.length() > 5)    tmpstr = h_vouc_user_id.substring(1,6);
//                buf = comcr.insertStr(buf, tmpstr          , 54);
//
//                tmpstr = h_vouc_manager;
//                if(tmpstr.length() > 5)    tmpstr = h_vouc_manager.substring(1,6);
//                buf = comcr.insertStr(buf, tmpstr          , 59);

				buf = comcr.insertStr(buf, hVoucIdNo, 64);
				buf = comcr.insertStr(buf, hVoucIfrsFlag, 75);
				buf = comcr.insertStr(buf, hVoucMemo1, 76);
				buf = comcr.insertStr(buf, hVoucMemo2, 96);
				buf = comcr.insertStr(buf, hVoucMemo3, 116);
				if (buf.length() < 135)
					buf = comcr.insertStr(buf, " ", 135);
//                lpar4.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
				lpar5.add(comcr.putReport(rptIdR5, rtpName5, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

				/****** for MQ *********/
				buf = "";
				buf = comcr.insertStr(buf, hVoucBrno, 1);
				// buf = comcr.insertStr(buf, comcr.formatDate(h_vouc_tx_date, 1), 4);
				tmpstr = String.format("%08d", comcr.str2long(hVoucTxDate) - 19110000);
				buf = comcr.insertStr(buf, tmpstr, 4);
				buf = comcr.insertStr(buf, "08", 12);
				buf = comcr.insertStr(buf, hVoucDepno, 14);
				buf = comcr.insertStr(buf, hVoucRefno, 15);
				buf = comcr.insertStr(buf, hVoucCurr, 21);
				sVoucSeqno = String.format("%04d", hVoucSeqno);
				buf = comcr.insertStr(buf, sVoucSeqno, 23);
				buf = comcr.insertStr(buf, hVoucAcNo, 27);

//                if(hVoucAcNo.length() > 6) {
//                if (hVoucAcNo.substring(0, 6).equals("175101")) {
//                    buf = comcr.insertStr(buf, "17510100", 27);
//                     }
//                }
				buf = comcr.insertStr(buf, hVoucDbcr, 36);
				mk = hVoucAmt > 0 ? "+" : "-";
				buf = comcr.insertStr(buf, mk, 37);
				allAmt += hVoucAmt;
				tmpstr = String.format("%17.2f", hVoucAmt);
				buf = comcr.insertStr(buf, tmpstr + "", 38);

//                selectPtrGenA002();
//                selectCrdEmployee();
//                selectCrdEmployee1();

				buf = comcr.insertStr(buf, hEmplEmployNo, 54);
				buf = comcr.insertStr(buf, hEmplChiName, 59);
				buf = comcr.insertStr(buf, hVoucIdNo, 64);
				buf = comcr.insertStr(buf, hVoucIfrsFlag, 75);

				/// SUP///////////////////////////////////////////////////////
				// for to ascii - > ebcdic lai
//                Big52CNS b2cns = new Big52CNS();
//                CharFormatConverter cfc = new CharFormatConverter();
//
//                String tmpBuf = fixLeft(buf, 75);
//                byte[] tempX22 = tmpBuf.getBytes();
//                tempX22 = tmpBuf.getBytes("Cp1047");
//                writeBinFile(tempX22, tempX22.length);
//
//                if (DEBUG == 1)
//                    showLogMessage("I", "", "888 222 memo1 =[" + hVoucMemo1 + "]");
//
//                byte[] tempX20 = new byte[20];
//                byte[] cns_bytes = b2cns.convCns((hVoucMemo1).getBytes("big5")).data;
//                if (strlen(cns_bytes) > 20)
//                    cns_bytes[19] = 0x0f;
//                System.arraycopy(cns_bytes, 0, tempX20, 0, 20);
//                writeBinFile(tempX20, tempX20.length);
//                if (DEBUG == 1) {
//                    showLogMessage("I", "", "  888 h_vouc_memo1=" + hVoucMemo1 + ", Len=" + tempX20.length);
//                    showLogMessage("I", "", " Conv data(EBCDIC)=" + comcpi.toHex(tempX20));
//// showLogMessage("I", "", " Conv data(EBCDIC)="+comcpi.toHex(buf.getBytes()));
//                }
//
//                cns_bytes = b2cns.convCns((hVoucMemo2).getBytes("big5")).data;
//                if (strlen(cns_bytes) > 20)
//                    cns_bytes[19] = 0x0f;
//                System.arraycopy(cns_bytes, 0, tempX20, 0, 20);
//                 writeBinFile(tempX20, tempX20.length);
//
//                cns_bytes = b2cns.convCns((hVoucMemo3).getBytes("big5")).data;
//                if (strlen(cns_bytes) > 20)
//                    cns_bytes[19] = 0x0f;
//                System.arraycopy(cns_bytes, 0, tempX20, 0, 20);
//                writeBinFile(tempX20, tempX20.length);

			} else {
				tmpCount = tmpCount + 1;
				printRtn1();
				if ((tmpCount > 1) && (hVoucRefno.compareTo(hRtn0Refno) != 0)) {
					printRtn0();
					hRtn0Amt = 0;
				}
				if (hVoucDbcr.equals("D"))
					hRtn0Amt = hRtn0Amt + hVoucAmt;
				hRtn0Brno = hVoucBrno;
				hRtn0Refno = hVoucRefno;
				hRtn0ModLog = hVoucModLog;
				hRtn0ModPgm = hVoucModPgm;
				hRtn0Curr = hVoucCurr;
				hRtn0ModUser = hVoucModUser;
				hRtn0Manager = hVoucManager;
			}
		}

		if (tmpCount > 0)
			printRtn0();
		allAmt = allAmt / 2;

		buf = "";
		buf = comcr.insertStr(buf, hVoucBrno, 1);
		tmpstr = String.format("%08d", comcr.str2long(hVoucTxDate) - 19110000);
		buf = comcr.insertStr(buf, tmpstr, 4);
		buf = comcr.insertStr(buf, "08", 12);
		String ctall = "0000" + countAll;
		buf = comcr.insertStr(buf, ctall.substring(ctall.length() - 4), 23);
		buf = comcr.insertStr(buf, "99990000", 27);
		tmpstr = String.format("%17.2f", allAmt);
		buf = comcr.insertStr(buf, tmpstr, 37);
		if (buf.length() < 135)
			buf = comcr.insertStr(buf, " ", 135);
//        lpar4.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar5.add(comcr.putReport(rptIdR5, rtpName5, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		if (buf.getBytes("big5").length < 135)
			buf = comcr.insertStr(buf, " ", 135);

//        byte[] tempByte = null;
//        buf = fixLeft(buf, 135);
//        tempByte = buf.getBytes("Cp1047");
//
//        writeBinFile(tempByte, tempByte.length);
//
//        buf = "999";
//
//        buf = fixLeft(buf, 135);
//        tempByte = buf.getBytes("Cp1047");
//
//        writeBinFile(tempByte, tempByte.length);

//        lpar4.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "0", "999\r"));
		lpar5.add(comcr.putReport(rptIdR5, rtpName5, sysDate + sysTime, rptSeq++, "0", "999\r"));
//		String filename5 = String.format("%s/media/gen/TOHOST.txt", comc.getECSHOME());	//目錄得同步
		String filename5 = "TOHOST.txt";	
		filename5 = Normalizer.normalize(filename5, java.text.Normalizer.Form.NFKD);

		// add by grace, for test (start) ----------------
		comcr.deletePtrBatchRpt(rptIdR5, sysDate);
		comcr.insertPtrBatchRpt(lpar5);
//      add by grace, for test (end) ----------------

//		comc.writeReport(filename5, lpar5);
		comc.writeReport(comc.getECSHOME() + FILE_PATH + filename5, lpar5);	//對應 FTPproc() local dir, renameFile()
		ftpProc(filename5, rptIdR5, "NCR2EMP", "NCR2EMP"); 
//        ftd2.close();
//        closeBinaryOutput();
//      String filename2 = String.format("%s/NUACJR08", comc.GetIBMftp());
//      comc.writeReport(filename2, lpar6);

//        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		/*
		 * lai test
		 */
//        if (commFTP.mqSend("NUACJR08", 135) != 0) {
//            comcr.errRtn("MQ無法傳送檔案[NUVDPA01]到IBM", "", comcr.hCallBatchSeqno);
//        }


	}

    // ************************************************************************
    void writeReportACB0055() throws Exception {
        pageCnt = 0;
        totalCount = 0;
        lineCnt = 0;
        tmpCount = 0;

        sqlCmd = "select ";
        sqlCmd += "decode(brno,'','3144',brno) h_vouc_brno, ";
        sqlCmd += "tx_date, ";
        sqlCmd += "dept , ";
        sqlCmd += "depno , ";
        sqlCmd += "refno , ";
        sqlCmd += "curr , ";
        sqlCmd += "seqno , ";
        sqlCmd += "ac_no , ";
        sqlCmd += "dbcr, ";
        sqlCmd += "amt , ";
        sqlCmd += "crt_user, ";
        sqlCmd += "apr_user, ";
        sqlCmd += "mod_user, ";
        sqlCmd += "mod_pgm, ";
        sqlCmd += "mod_log, ";
        sqlCmd += "id_no, ";
        sqlCmd += "voucher_cnt, ";
        sqlCmd += "memo1 , ";
        sqlCmd += "memo2 , ";
        sqlCmd += "memo3, ";
        sqlCmd += "DECODE(ifrs_flag, '', 'B', ifrs_flag) h_vouc_ifrs_flag ";
        sqlCmd += " from gen_vouch ";
        sqlCmd += "where DECODE(post_flag, '', 'N', post_flag) in ('N','n')  ";
        sqlCmd += "  and jrn_status  in ('3')  ";
        sqlCmd += "  and curr = '00' "; // 幣別只得為'00'者
        sqlCmd += "  and tx_date     <= ? ";
        sqlCmd += "order by decode(brno,'','3144',brno),tx_date,refno,seqno ";
        setString(1, hBusinessDate);
        int recordCnt = selectTable();

        if (DEBUG == 1)
            showLogMessage("I", "", "A401_ACCOUNT write rtn cnt=[" + recordCnt + "]");
        totCnt = recordCnt;

        lineCnt0 = 0;
        totalCount0 = 0;
        lineCnt = 0;
        totalCount = 0;
        String buf = "";
        int countAll = 0;
        double allAmt = 0;
        String rptIdR = "A401_ACCOUNT";
        List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < recordCnt; i++) {
            hVoucBrno = getValue("h_vouc_brno", i);
            hVoucTxDate = getValue("tx_date", i);
            hVoucDept = getValue("dept", i);
            hVoucDepno = getValue("depno", i);
            hVoucRefno = getValue("refno", i);
            hVoucCurr = getValue("curr", i);
            hVoucSeqno = getValueInt("seqno", i);
            hVoucAcNo = getValue("ac_no", i);
            hVoucDbcr = getValue("dbcr", i);
            hVoucAmt = getValueDouble("amt", i);
            hVoucUserId = getValue("crt_user", i);
            hVoucManager = getValue("apr_user", i);
            hVoucModUser = getValue("mod_user", i);
            hVoucModPgm = getValue("mod_pgm", i);
            hVoucModLog = getValue("mod_log", i);
            hVoucIdNo = getValue("id_no", i);
            hVoucVoucherCnt = getValueInt("voucher_cnt", i);
            hVoucMemo1 = getValue("memo1", i);
            hVoucMemo2 = getValue("memo2", i);
            hVoucMemo3 = getValue("memo3", i);
            hVoucIfrsFlag = getValue("h_vouc_ifrs_flag", i);

            if (DEBUG == 1)
                showLogMessage("I", "", "A401_ACCOUNT write refno  =[" + hVoucRefno + "]" + hVoucAcNo + "," + hVoucBrno);
            if (hVoucModLog.compareTo("D") != 0 && hVoucModLog.compareTo("U") != 0) {
                countAll++;
                buf = comcr.insertStr(buf, "1", 1);
                buf = comcr.insertStr(buf, " ", 2);
                tmpstr = String.format("%07d", comcr.str2long(hVoucTxDate) - 19110000);
                tmpstr = StringUtils.rightPad(tmpstr, 7);
                buf = comcr.insertStr(buf, tmpstr, 3);
                buf = comcr.insertStr(buf, " ", 10);
                buf = comcr.insertStr(buf, StringUtils.rightPad(hVoucBrno, 4), 11);
                buf = comcr.insertStr(buf, " ", 15);
                buf = comcr.insertStr(buf, "1", 16);
                buf = comcr.insertStr(buf, " ", 17);
                buf = comcr.insertStr(buf, StringUtils.rightPad(hVoucAcNo, 9), 18);
                buf = comcr.insertStr(buf, " ", 27);
                buf = comcr.insertStr(buf, StringUtils.rightPad(hVoucCurr, 2), 28);
                buf = comcr.insertStr(buf, " ", 30);
                buf = comcr.insertStr(buf, StringUtils.rightPad(hVoucDbcr, 1), 31);
                buf = comcr.insertStr(buf, " ", 32);
//                String mk = hVoucAmt > 0 ? "+" : "-";
//                buf = comcr.insertStr(buf, mk, 37);
                allAmt += hVoucAmt;
                tmpstr = String.format("%.2f", hVoucAmt).replace(".", "");
                tmpstr = StringUtils.leftPad(tmpstr, 17, "0");
                buf = comcr.insertStr(buf, tmpstr, 33);
                buf = comcr.insertStr(buf, " ", 50);
                buf = comcr.insertStr(buf, "R", 51);
                buf = comcr.insertStr(buf, " ", 52);
                buf = comcr.insertStr(buf, StringUtils.rightPad(hVoucModPgm, 8).substring(0, 8), 53); // 資料來源
                buf = comcr.insertStr(buf, " ", 61);
                tmpstr = hVoucMemo1 + " " + hVoucMemo2 + " " + hVoucMemo3;
                tmpstr = StringUtils.rightPad(tmpstr, 62);
                buf = comcr.insertStr(buf, tmpstr, 62);
                buf = comcr.insertStr(buf, " ", 124);
                buf = comcr.insertStr(buf, StringUtils.rightPad("", 36), 125); // 其他使用區
//                if (buf.length() < 160)
//                    buf = comcr.insertStr(buf, " ", 135);
                lpar.add(comcr.putReport(rptIdR5, rtpName5, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
            } else {
                tmpCount = tmpCount + 1;
//                printRtn1();
                if ((tmpCount > 1) && (hVoucRefno.compareTo(hRtn0Refno) != 0)) {
//                    printRtn0();
                    hRtn0Amt = 0;
                }
                if (hVoucDbcr.equals("D"))
                    hRtn0Amt = hRtn0Amt + hVoucAmt;
                hRtn0Brno = hVoucBrno;
                hRtn0Refno = hVoucRefno;
                hRtn0ModLog = hVoucModLog;
                hRtn0ModPgm = hVoucModPgm;
                hRtn0Curr = hVoucCurr;
                hRtn0ModUser = hVoucModUser;
                hRtn0Manager = hVoucManager;
            }
        }

        if (buf.getBytes("big5").length < 160)
            buf = comcr.insertStr(buf, " ", 160);

//        lpar.add(comcr.putReport(rptIdR, rtpName5, sysDate + sysTime, rptSeq++, "0", "999\r"));
           
        fileName = Normalizer.normalize(fileName, java.text.Normalizer.Form.NFKD);

        // add by grace, for test (start) ----------------
//        comcr.deletePtrBatchRpt(rptIdR, sysDate);
//        comcr.insertPtrBatchRpt(lpar);
//      add by grace, for test (end) ----------------

        comc.writeReport(comc.getECSHOME() + FILE_PATH + fileName, lpar);   //對應 FTPproc() local dir, renameFile()
        ftpProc(fileName, rptIdR, "NCR2TCB", "NCR2TCB"); 
    }

// ************************************************************************
	private int strlen(byte[] tempX20) {

		int rtnInt = 0;
		for (int i = 0; i < tempX20.length; i++) {
			if (tempX20[i] == 0x20) {
				break;
			}
			rtnInt++;
		}

		return rtnInt;
	}

	// ************************************************************************
	void printHead() {
		pageCnt1++;
		String buf = "";

//        buf = comcr.insertStr(buf, "合作金庫商業銀行總管理處信用卡部", 52);
//        lpar.add(comcr.putReport(rptId_r1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf+"\r"));

		buf = "";
//        buf = comcr.insertStr(buf, "報表名稱: GEN_A002R1", 1);
//        buf = comcr.insertStr(buf, "報表名稱: CCDCD011", 1);
		String szTmp = String.format("   BRN:%4.4s    FORM:%s", "3144", rptIdR1);
		buf = comcr.insertStr(buf, szTmp, 1);
//        buf = comcr.insertStr(buf, "*** 交易序號使用明細表-帳務 ***", 52);
		buf = comcr.insertStr(buf, "    交易序號使用明細表-帳務    ", 52);
		buf = comcr.insertStr(buf, "頁    次:", 100);
		buf = comcr.insertStr(buf, pageCnt1 + "", 112);
//        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "單    位:", 1);
		buf = comcr.insertStr(buf, hVoucBrno, 11);
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 6), 56);
		buf = comcr.insertStr(buf, "製表日期:", 100);
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 112);

//        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
//        lpar.add(comcr.putReport(rptId_r1, rtpName1, sysDate + sysTime, rptSeq++, "0", REPORT_H_1));
//        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", reportH+"\r"));
//        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", reportL+"\r"));
		lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", reportH + "\r"));
		lpar1.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", reportL + "\r"));
	}

	// ***************************************************************************************
	private void printHead40() { // ori name: printHead0()
		pageCnt0++;

		String buf = "";
		buf = comcr.insertStr(buf, "BRN:", 4);
		buf = comcr.insertStr(buf, hRtn0Brno, 8);
//        buf = comcr.insertStr(buf, "FORM:CCDCD004", 12);
		buf = comcr.insertStr(buf, "合作金庫商業銀行", 58);
		buf = comcr.insertStr(buf, "日期:", 113);
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 119);
//        lpar0.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "FORM:CCDCD004_0", 4);
		buf = comcr.insertStr(buf, "信用卡會計業務刪除及修改交易明細表(依套號)", 50);
//        lpar0.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "報表用途:核對用", 4);
		buf = comcr.insertStr(buf, "使用單位:各分行會計", 35);
		buf = comcr.insertStr(buf, "保管期限:十五年", 79);
		buf = comcr.insertStr(buf, "頁次:", 113);
		buf = comcr.insertStr(buf, String.format("%4d", pageCnt0), 120);
//        lpar0.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 6) + "\r\n", 60);
//        lpar0.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
		buf = reportL0;
//        lpar0.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
		buf = reportH0;
//        lpar0.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
		buf = reportL0;
//        lpar0.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
	}

	// ***************************************************************************************
	private void printHead41() { // ori name: printHead1()
		if (DEBUG == 1)
			showLogMessage("I", "", "    step 2 print_head_1 PG=[" + lineCnt + "]" + pageCnt);
		pageCnt++;

		String buf = "";
		buf = comcr.insertStr(buf, "BRN:", 4);
		buf = comcr.insertStr(buf, hVoucBrno, 8);
//        buf = comcr.insertStr(buf, "FORM:CCDCD004_1", 12);
		buf = comcr.insertStr(buf, "合作金庫商業銀行", 58);
		buf = comcr.insertStr(buf, "日期:", 113);
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 119);
//        lpar1.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "FORM:CCDCD004_1", 4);
		buf = comcr.insertStr(buf, "信用卡會計業務刪除及修改交易明細表(依科目)", 50);
//        lpar1.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "報表用途:核對用", 4);
		buf = comcr.insertStr(buf, "使用單位:各分行會計", 35);
		buf = comcr.insertStr(buf, "保管期限:十五年", 79);
		buf = comcr.insertStr(buf, "頁次:", 113);
		buf = comcr.insertStr(buf, String.format("%4d", pageCnt), 120);
//        lpar1.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 6) + "\r\n", 60);
//        lpar1.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
		buf = reportL0;
//        lpar1.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
		buf = reportH4;
//        lpar1.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
		buf = reportL0;
//        lpar1.add(comcr.putReport(prgmId, "CCDCD004", sysDate + sysTime, rptSeq++, "0", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
	}

	// ***************************************************************************************
	void printHead2() {
		pageCnt2++;

		String buf = "";
		buf = comcr.insertStr(buf, "合作金庫商業銀行總管理處信用卡部", 52);
		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "報表名稱: GEN_A002R2", 1);
		buf = comcr.insertStr(buf, "*** 簽帳卡 連線業務轉帳日記帳 ***", 48);
		buf = comcr.insertStr(buf, "頁    次:", 100);
		buf = comcr.insertStr(buf, pageCnt2 + "", 112);
		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 6), 56);
		buf = comcr.insertStr(buf, "製表日期:", 100);
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 112);
		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = " 確認單號碼 幣別 科子細目代號   名     稱                     借    方  金   額 貸    方  金   額 櫃  員 主  管 備  註";
		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = " ========== ==== ============================================ ================= ================= ====== ====== ======";
		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
	}

	// ***************************************************************************************
	void printHead3() throws Exception {
		pageCnt3++;

		String buf = "";
		buf = comcr.insertStr(buf, "合作金庫商業銀行總管理處信用卡部", 28);
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "報表名稱: GEN_A002R3", 1);
		buf = comcr.insertStr(buf, "** 簽帳卡 連線業務科目別軋對表 **", 25);
		buf = comcr.insertStr(buf, "頁    次:", 60);
		buf = comcr.insertStr(buf, pageCnt3 + "", 70);
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "部 門 別:", 1);
		buf = comcr.insertStr(buf, "銀行部", 11);
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 6), 30);
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		sqlCmd = "select CURR_ENG_NAME  h_eng_name ";
		sqlCmd += "  from ptr_currcode  ";
		sqlCmd += " where curr_code_gl = ? ";
		setString(1, hTempCurr);
		hEngName = selectTable() > 0 ? getValue("h_eng_name") : "";

		buf = comcr.insertStr(buf, "幣    別:", 01);
		buf = comcr.insertStr(buf, hTempCurr, 11);
		buf = comcr.insertStr(buf, hEngName, 15);
		buf = comcr.insertStr(buf, "製表日期:", 60);
		buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 70);
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = " 筆數 借    方  金   額 科子細目代號   名     稱             貸    方  金   額 筆數";
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = " ==== ================= ==================================== ================= ====";
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "0", buf + "\r"));
	}

	// ***************************************************************************************
	void printSum2() {
		String buf = "";
		buf = comcr.insertStr(buf, "合   計:", 2);
//		buf = comcr.insertStr(buf, "TWD", 12);
		buf = comcr.insertStr(buf, "   ", 12);
		buf = comcr.insertStr(buf, comcr.formatNumber(sumAmt2Dr + "", 2, 2), 62);
		buf = comcr.insertStr(buf, comcr.formatNumber(sumAmt2Cr + "", 2, 2), 80);
		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "0", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "經副襄理              會計                覆核:", 1);
		buf = comcr.insertStr(buf, hTempManager, 48);
		buf = comcr.insertStr(buf, "經辦:", 60);
		buf = comcr.insertStr(buf, hTempName, 66);
		lpar2.add(comcr.putReport(rptIdR2, rtpName2, sysDate + sysTime, rptSeq++, "2", buf + "\r"));
		sumAmt2Dr = 0;
		sumAmt2Cr = 0;
	}

	// ***************************************************************************************
	void printSum3() {
		String buf = "";
		buf = comcr.insertStr(buf, sCntDr + "", 1);
		if (sAmtDr != 0) {
			buf = comcr.insertStr(buf, comcr.formatNumber(sAmtDr + "", 2, 2), 6);
		}
		buf = comcr.insertStr(buf, "******     合     計    ******", 28);
		if (sAmtCr != 0) {
			buf = comcr.insertStr(buf, comcr.formatNumber(sAmtCr + "", 2, 2), 61);
		}
		buf = comcr.insertStr(buf, sCntCr + "", 79);
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "2", buf + "\r"));

		buf = "";
		buf = comcr.insertStr(buf, "經副襄理              會計                營業:", 1);
		buf = comcr.insertStr(buf, hTempManager, 48);
		buf = comcr.insertStr(buf, "經辦:", 60);
		buf = comcr.insertStr(buf, hTempName, 66);
		lpar3.add(comcr.putReport(rptIdR3, rtpName3, sysDate + sysTime, rptSeq++, "2", buf + "\r"));
	}

	// ******************************************************************************
	private void printRtn0() throws Exception {
		lineCnt0++;
		totalCount0++;

		if (DEBUG == 1)
			showLogMessage("I", "", "prt CNT 0=[" + lineCnt0 + "]" + hRtn0Refno + "," + hRtn0ModLog);

		if (totalCount0 == 1)
			tempX3 = hRtn0Brno;

		if (!hRtn0Brno.equals(tempX3)) {
			if (lineCnt0 > 2)
//         lpar0.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "1", "\f"));
				lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "1", "\f"));
//      printHead0();
			printHead40();
			lineCnt0 = 0;
			tempX3 = hRtn0Brno;
		}
		if (totalCount0 == 1 || lineCnt0 > 58) {
			if (lineCnt0 > 2)
//         lpar0.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "1", "\f"));
				lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "1", "\f"));
//      printHead0();
			printHead40();
			if (totalCount0 != 1)
				lineCnt0 = 0;
		}

		String buf = "";
		buf = comcr.insertStr(buf, hRtn0Refno, 2);
		if (hRtn0ModLog.equals("D"))
			buf = comcr.insertStr(buf, "已被刪除", 20);
		if (hRtn0ModLog.equals("U")) {
			buf = comcr.insertStr(buf, "已被修改", 20);
		}
		buf = comcr.insertStr(buf, hRtn0ModPgm, 40);

		sqlCmd = "select CURR_ENG_NAME h_eng_name ";
		sqlCmd += "  from ptr_currcode ";
		sqlCmd += " where curr_code_gl = ? ";
		setString(1, hRtn0Curr);
		hEngName = selectTable() > 0 ? getValue("h_eng_name") : "";

		// buf = comcr.insertStr(buf, h_rtn0_curr , 64);
		buf = comcr.insertStr(buf, hEngName, 64);
		tmpstr = String.format("%17.2f", hRtn0Amt);
		buf = comcr.insertStr(buf, tmpstr, 75);
		buf = comcr.insertStr(buf, hRtn0ModUser, 97);

		String fun = hRtn0Refno.substring(2, 3);
		tempX06 = comcr.getVouchTypeCnName(fun);
//        if (fun.equals("0"))
//            tempX06 = "共用";
//        else if (fun.equals("1"))
//            tempX06 = "作業";
//        else if (fun.equals("2"))
//            tempX06 = "作業";
//        else if (fun.equals("3"))
//            tempX06 = "風管";
//        else if (fun.equals("4"))
//            tempX06 = "催收";
//        else if (fun.equals("5"))
//            tempX06 = "發卡";
//        else if (fun.equals("6"))
//            tempX06 = "客服";
//        else if (fun.equals("7"))
//            tempX06 = "行銷";
//        else if (fun.equals("8"))
//            tempX06 = "授權";
//        else
//            tempX06 = "共用";

		if (hRtn0ModUser.toLowerCase().equals("system")) {
			buf = comcr.insertStr(buf, tempX06, 104);
		} else {
			sqlCmd = "select usr_cname";
			sqlCmd += " from sec_user ";
			sqlCmd += " where usr_id =?";
			setString(1, hVoucModUser);
			hTempX10 = selectTable() > 0 ? getValue("usr_cname") : "";
			buf = comcr.insertStr(buf, hTempX10, 104);
		}
		buf = comcr.insertStr(buf, hRtn0Manager, 117);
		if (hRtn0Manager.toLowerCase().equals("system")) {
			hTempX10 = hTempManager;
		} else {
			hTempX11 = hRtn0ModUser;
			sqlCmd = "select usr_cname";
			sqlCmd += "  from sec_user ";
			sqlCmd += " where usr_id =?";
			setString(1, hTempX11);
			hTempX10 = selectTable() > 0 ? getValue("usr_cname") : "";
		}
		buf = comcr.insertStr(buf, hTempX10, 124);

//        lpar0.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "1", buf+"\r"));
		lpar40.add(comcr.putReport(rptIdR4, rtpName40, sysDate + sysTime, rptSeq++, "1", buf + "\r"));
	}

	// *************************************************************************************
	private void printRtn1() throws Exception {
		lineCnt++;
		totalCount++;

		if (DEBUG == 1)
			showLogMessage("I", "", "prt CNT 1=[" + lineCnt + "]" + totalCount + "," + tempX31 + "," + hVoucBrno);

		if (totalCount == 1) {
			tempX31 = hVoucBrno;
		}

		if (!hVoucBrno.equals(tempX31)) {
			if (lineCnt > 2)
//         lpar1.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "1", "\f"));
				lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "1", "\f"));
			if (DEBUG == 1)
				showLogMessage("I", "", "    step 1=[" + lineCnt + "]");
//        printHead1();
			printHead41();
			lineCnt = 0;
			tempX31 = hVoucBrno;
		}
		if (totalCount == 1 || lineCnt > 58) {
			if (lineCnt > 2)
//         lpar1.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "1", "\f"));
				lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "1", "\f"));
//      printHead1();
			printHead41();
			if (totalCount0 != 1)
				lineCnt = 0;
		}

		String buf = "";
		buf = comcr.insertStr(buf, hVoucDepno, 3);
		buf = comcr.insertStr(buf, hVoucRefno, 8);
		buf = comcr.insertStr(buf, String.format("%04d", hVoucSeqno), 15);

		sqlCmd = "select CURR_ENG_NAME h_eng_name ";
		sqlCmd += "  from ptr_currcode     ";
		sqlCmd += " where curr_code_gl = ? ";
		setString(1, hVoucCurr);
		hEngName = selectTable() > 0 ? getValue("h_eng_name") : "";

		buf = comcr.insertStr(buf, hVoucCurr, 21);
		buf = comcr.insertStr(buf, "TWD", 21);
		buf = comcr.insertStr(buf, hEngName, 21);
		buf = comcr.insertStr(buf, hVoucAcNo, 25);

		sqlCmd = "select substrb(AC_BRIEF_NAME,1,26) h_temp_x30";
		sqlCmd += "  from gen_acct_m ";
		sqlCmd += " where ac_no = ?  ";
		setString(1, hVoucAcNo);
		hTempX30 = selectTable() > 0 ? getValue("h_temp_x30") : "";

		buf = comcr.insertStr(buf, hTempX30, 34);
		buf = comcr.insertStr(buf, hVoucDbcr, 51);
		tmpstr = String.format("%17.2f", hVoucAmt);
		buf = comcr.insertStr(buf, tmpstr, 54);
		buf = comcr.insertStr(buf, hVoucMemo1, 72);
		buf = comcr.insertStr(buf, hVoucMemo2, 93);

		String fun = hVoucRefno.substring(2, 3);
		tempX06 = comcr.getVouchTypeCnName(fun);
//        if (fun.equals("0"))
//            tempX06 = "共用";
//        else if (fun.equals("1"))
//            tempX06 = "作業";
//        else if (fun.equals("2"))
//            tempX06 = "作業";
//        else if (fun.equals("3"))
//            tempX06 = "風管";
//        else if (fun.equals("4"))
//            tempX06 = "催收";
//        else if (fun.equals("5"))
//            tempX06 = "發卡";
//        else if (fun.equals("6"))
//            tempX06 = "客服";
//        else if (fun.equals("7"))
//            tempX06 = "行銷";
//        else if (fun.equals("8"))
//            tempX06 = "授權";
//        else
//            tempX06 = "共用";

		if (hVoucModUser.toLowerCase().equals("system")) {
			buf = comcr.insertStr(buf, tempX06, 114);
		} else {
			sqlCmd = "select usr_cname ";
			sqlCmd += " from sec_user  ";
			sqlCmd += " where usr_id = ? ";
			setString(1, hVoucModUser);
			hTempX10 = selectTable() > 0 ? getValue("usr_cname") : "";
			buf = comcr.insertStr(buf, hTempX10, 114);
		}

		if (hVoucManager.toLowerCase().equals("system")) {
			hTempX10 = hTempManager;
		} else {
			hTempX11 = hVoucManager;
			sqlCmd = "select usr_cname ";
			sqlCmd += "  from sec_user  ";
			sqlCmd += " where usr_id = ? ";
			setString(1, hTempX11);
			hTempX10 = selectTable() > 0 ? getValue("usr_cname") : "";
		}
		buf = comcr.insertStr(buf, hTempX10, 124);

//        lpar1.add(comcr.putReport(prgmId, "", sysDate + sysTime, rptSeq++, "1", buf+"\r"));
		lpar41.add(comcr.putReport(rptIdR4, rtpName41, sysDate + sysTime, rptSeq++, "1", buf + "\r"));

	}

	// ************************************************************************
	private void selectPtrGenA002() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "hand_user_id,";
		sqlCmd += "hand_manager_id,";
		sqlCmd += "r6_user_id,";
		sqlCmd += "r6_manager_id ";
		sqlCmd += " from ptr_gen_a002 ";
		sqlCmd += "where mod_time in (select max(mod_time) from ptr_gen_a002)";
		int cnt = selectTable();
		if (cnt > 0) {
			hPgeaHandUserId = getValue("hand_user_id");
			hPgeaHandManagerId = getValue("hand_manager_id");
			hPgeaR6UserId = getValue("r6_user_id");
			hPgeaR6ManagerId = getValue("r6_manager_id");
		} else {

			hPgeaHandUserId = "";
			hPgeaHandManagerId = "";
			hPgeaR6UserId = "";
			hPgeaR6ManagerId = "";
		}
	}

	// ************************************************************************
	private void selectCrdEmployee() throws Exception {
//        hPgeaHandUserId = "123";
//        /*
//         * lai
//         */
//        selectSQL = " decode(unit_no,'138',cast(? as varchar(10)) ,cast(? as varchar(10))) h_empl_employ_no ";
//        daoTable = "crd_employee ";
//        whereStr = "WHERE employ_no = '0' || " + " decode(substr(cast(? as varchar(10)),1,1),'1','A','2','B',"
//                + "  substr(cast(? as varchar(10)),1,1)) || substr(cast(? as varchar(10)),2,4) ";
//        setString(1, hPgeaHandUserId);
//        setString(2, hVoucUserId);
//        setString(3, hVoucUserId);
//        setString(4, hVoucUserId);
//        setString(5, hVoucUserId);
//
//        tmpInt = selectTable();
//        if (tmpInt > 0)
//            hEmplEmployNo = getValue("h_empl_employ_no");
//        else
		hEmplEmployNo = hPgeaR6UserId;

		tmpstr = hEmplEmployNo;
		if (tmpstr.length() > 5)
			tmpstr = hEmplEmployNo.substring(1, 6);
		hEmplEmployNo = tmpstr;

	}

	// ************************************************************************
	private void selectCrdEmployee1() throws Exception {
//        selectSQL = " decode(unit_no,'138',cast(? as varchar(10)) ,cast(? as varchar(10))) h_empl_employ_no ";
//        daoTable = "crd_employee ";
//        whereStr = "WHERE employ_no = '0' || " + " decode(substr(cast(? as varchar(10)),1,1),'1','A','2','B',"
//                + "  substr(cast(? as varchar(10)),1,1)) || substr(cast(? as varchar(10)),2,4) ";
//
//        setString(1, hPgeaHandManagerId);
//        setString(2, hVoucManager);
//        setString(3, hVoucManager);
//        setString(4, hVoucManager);
//        setString(5, hVoucManager);
//        hEmplChiName = selectTable() > 0 ? getValue("h_empl_chi_name") : hPgeaR6ManagerId;
		hEmplChiName = hPgeaHandManagerId;

		tmpstr = hEmplChiName;
		if (tmpstr.length() > 5)
			tmpstr = hEmplChiName.substring(1, 6);
		hEmplChiName = tmpstr;

	}

	// ************************************************************************
	// create by davidfu
	void commonRtn() throws Exception {
		selectSQL = "business_date,vouch_date ";
		daoTable = "ptr_businday";

		if (selectTable() > 0) {
			hBusinessDate = getValue("business_date");
			hVouchDate = getValue("vouch_date");
		}

		// =============================
		selectSQL = "to_char(sysdate,'yyyymmdd') date1";
		daoTable = "dual";

		if (selectTable() > 0) {
			hSystemDate = getValue("date1");
		}

		hModSeqno = comcr.getModSeq();
		hModUser = comc.commGetUserID();
		hModTime = hSystemDate;
	}

	// ************************************************************************
	private void ftpProc(String filename, String groupID, String systemId, String refIpCode) throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = systemId; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEflgGroupId = groupID; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEriaLocalDir = comc.getECSHOME() + FILE_PATH;	//相關目錄皆同步
		commFTP.hEflgModPgm = javaProgram;

		String hEflgRefIpCode = refIpCode; //"NCR2TCB";

		System.setProperty("user.dir", commFTP.hEriaLocalDir);

		showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

		String tmpChar = "put " + filename;

		int errCode = commFTP.ftplogName(hEflgRefIpCode, tmpChar);

		if (errCode != 0) {
			showLogMessage("I", "", "檔案傳送 " + hEflgRefIpCode + " 有誤(error), 請通知相關人員處理");
			showLogMessage("I", "", "gen_a002執行完成 傳送EMP失敗[" + filename + "]");
			commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
			return;
		}

		showLogMessage("I", "", "FTP完成.....");

		// 刪除檔案 put 不用刪除
		// select_ecs_ftp_log();
		renameFile(filename);
	}

	// ************************************************************************
	public void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + FILE_PATH + removeFileName;
		String tmpstr2 = comc.getECSHOME() + FILE_PATH + "backup/" + removeFileName + "." + sysDate;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	String null2space(String p1, int len) {
		int inta;
		String r1 = "";

		for (inta = 0; inta < len - 2; inta++) {
			if (p1.charAt(inta) == '\0') {
				r1 = r1 + " ";
			} else {
				r1 = r1 + p1.substring(inta, inta + 1);
			}
		}

		r1 = r1 + ibmChi[1];

		return r1;
	}

// ************************************************************************
	/*** @return *********************************************************************/
//    byte[] convCns(String p1, byte[] p2) throws Exception {
//        Big52CNS cnvCns = new Big52CNS();
//        CnsResult cncResult = cnvCns.convCns(p1.getBytes("big5"));
//        p2 = cncResult.data;
//        return p2;
//    }

	/*******************************************************************************/
	String fixLeft(String str, int len) throws UnsupportedEncodingException {
		int size = (Math.floorDiv(len, 100) + 1) * 100;
		String spc = "";
		for (int i = 0; i < size; i++)
			spc += " ";
		if (str == null)
			str = "";
		str = str + spc;
		byte[] bytes = str.getBytes("MS950");
		byte[] vResult = new byte[len];
		System.arraycopy(bytes, 0, vResult, 0, len);

		return new String(vResult, "MS950");
	}

} // End of class FetchSample