/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/06/30  V1.00.00    Pino      program initial                           *
*  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *
*  109-07-22    yanghan       修改了字段名称            *                                                                            *
*  109/08/26  V1.01.04   Wilson     card_no -> vd_card_no                     *  
*  109-10-19  V1.00.05    shiyuqi       updated for project coding standard     *
*  109/10/30  V1.00.06    Wilson    檔名日期改營業日                                                                                      *
*  111/02/14  V1.00.07    Ryan      big5 to MS950                                           *
*  112/05/05  V1.00.08    Wilson    mark update_tsc_notify_log not found      *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊VD卡退卡資料檔(DCRT)媒體接收處理程式*/
public class TscD032A extends AccessDAO {
	private boolean debug = false;

	private final String progname = "悠遊VD卡退卡資料檔(DCRT)媒體接收處理程式 112/05/05 V1.00.08";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommIps comips = new CommIps();

	String hCallBatchSeqno = "";

	String hTempNotifyDate = "";
	String hBusiBusinessDate = "";
	String hTempSystemDate = "";
	String hTempNotifyTime = "";
	String hTnlgPerformFlag = "";
	String hTnlgNotifyDate = "";
	String hTnlgCheckCode = "";
	String hTnlgProcFlag = "";
	String hTnlgRowid = "";
	String hTnlgFileName = "";
	String hDccgPurchaseDate = "";
	String hDccgCardNo = "";
	String hTardBalanceDate = "";
	String hTardBlackltSDate = "";
	String hTardBlackltEDate = "";
	int hTempDiffDays = 0;
	String hTardAutoloadFlag = "";
	String hTardTscSignFlag = "";
	String hDccgTscCardNo = "";
	String hOrgdTsccDataSeqno = "";
	String hOrgdOrgData = "";
	String hOrgdRptRespCode = "";
	String hTempBatchNo = "";
	String hTempBatchSeq = "";
	String hDccgBillType = "";
	String hDccgTransactionCode = "";
	String hDccgTscTxCode = "";
	String hDccgPurchaseTime = "";
	String hDccgMerchantChiName = "";
	double hDccgDestinationAmt = 0;
	String hDccgBillDesc = "";
	String hDccgTrafficCd = "";
	String hDccgTrafficAbbr = "";
	String hDccgAddrCd = "";
	String hDccgAddrAbbr = "";
	String hDccgPostFlag = "";
	String hDccgTsccDataSeqno = "";
	String hDccgReturnSource = "";
	double hDccgServiceAmt = 0;
	String hDccgTrafficCdNew = "";
	String hDccgTrafficAbbrNew = "";
	String hDccgAddrCdNew = "";
	String hDccgAddrAbbrNew = "";
	String fixBillType = "";
	double hPostTotRecord = 0;
	double hPostTotAmt = 0;
	String hBiunConfFlag = "";
	double hAmt = 0;
	double hAmtS = 0;
	int hCnt = 0;
	int hErrCnt = 0;
	String hTnlhAcctType = "";
	String hTnlhAcctKey = "";
	String hTnlhCardNo = "";
	String hTnlhIdPSeqno = "";

	String hTnlhMajorChiName = "";
	double hTnlhPrebalanceAmt = 0;
	double hTnlhRemainAmt = 0;
	double hBpcdNetTtlBp = 0;
	String hTnlhProcessStatus = "";
	String hDccgFileName = "";
	String hDccgTscError = "";
	String hDccgTscNotiDate = "";
	String hDccgTscRespCode = "";
	String hDccgMerchantNo = "";
	String hDccgMerchantCategory = "";
	String hDccgDestinationCurrency = "";
	String hDccgBatchNo = "";
	int hDccgSeqNo = 0;
	int hMloaPeriod = 0;
	int hMloaInterest = 0;
	double hMloaRtnRate = 0;

	int hTnlgRecordCnt = 0;
	int forceFlag = 0;
	int totalCnt = 0;
	int succCnt = 0;
	int rptCnt = 0;
	String tmpstr = "";
	String tmpstr1 = "";
	String tmpstr2 = "";
	String fileSeq = "";
	String temstr1 = "";
	String hSign = "";
	String hSigns = "";
	String hPSeqno = "";
	String hAcctType = "";

	Buf1 dtl = new Buf1();

	private String hBusinssChiDate = "";
	private String hVouchChiDate = "";
	private String hCurpId = "";
	private String hCur1Id = "";

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (comm.isAppActive(javaProgram)) {
				comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			hTempNotifyDate = "";
			fileSeq = "01";
			forceFlag = 0;
			
			selectPtrBusinday();

			if (args.length == 0) {
		        hTempNotifyDate = hBusiBusinessDate;
		    }
			else if (args.length == 1) {
				if ((args[0].length() == 1) && (args[0].equals("Y")))
					forceFlag = 1;
				if (args[0].length() == 8) {
					String sGArgs0 = "";
					sGArgs0 = args[0];
					sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
					hTempNotifyDate = sGArgs0;
					hBusiBusinessDate = hTempNotifyDate;
				}
				if (args[0].length() == 2) {
					showLogMessage("I", "", String.format("參數(一) 不可兩碼"));
				}
			}
			else if (args.length == 2) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hTempNotifyDate = sGArgs0;
				hBusiBusinessDate = hTempNotifyDate;
				if ((args[1].length() == 1) && (args[1].equals("Y")))
					forceFlag = 1;
				if (args[1].length() == 2) {
					String sGArgs1 = "";
					sGArgs1 = args[1];
					sGArgs1 = Normalizer.normalize(sGArgs1, java.text.Normalizer.Form.NFKD);
					fileSeq = sGArgs1;
				}
				if (args[1].length() != 1 && args[1].length() != 2) {
					showLogMessage("I", "", String.format("參數(二) 為[force_flag] or [seq(nn)] "));
				}
			}
			else if (args.length == 3) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hTempNotifyDate = sGArgs0;
				hBusiBusinessDate = hTempNotifyDate;
				if (args[1].equals("Y"))
					forceFlag = 1;
				if (args[2].length() != 2) {
					showLogMessage("I", "", String.format("file seq 必須兩碼"));
				}
				String sGArgs2 = "";
				sGArgs2 = args[2];
				sGArgs2 = Normalizer.normalize(sGArgs2, java.text.Normalizer.Form.NFKD);
				fileSeq = sGArgs2;
			}
			else {
				comc.errExit("Usage : TscD032A [[notify_date][force_flag]] [force_flag][seq]", "");
			}

			tmpstr1 = String.format("DCRT.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
			hTnlgFileName = tmpstr1;
			showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

			fixBillType = "TSCC";
			hDccgBillType = fixBillType;

			deleteBilPostcntl();
			deleteTscDccgAll();
			deleteTscOrgdataLog();

            selectPtrBillunit();

			hPostTotRecord = hPostTotAmt = 0;
			fileOpen();
            updateTscNotifyLogA();

			backupRtn();

			showLogMessage("I", "",
					String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
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
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";
		sqlCmd = "select business_date,";
		sqlCmd += "substr(to_char(to_number(business_date)- 19110000,'0000000'),2,7) h_businss_chi_date,";
		sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) h_vouch_chi_date,";
		sqlCmd += " decode( cast(? as varchar(8))" + ", ''"
				+ ", to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13')" + ", 1" + ", sysdate"
				+ ", sysdate - 1 days)" + ", 'yyyymmdd')" + ", ?) h_temp_notify_date,";
		sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_system_date,";
		sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += "fetch first 1 rows only";
		setString(1, hTempNotifyDate);
		setString(2, hTempNotifyDate);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
			hBusinssChiDate = getValue("h_businss_chi_date");
			hVouchChiDate = getValue("h_vouch_chi_date");
			hTempNotifyDate = hTempNotifyDate.length() == 0 ? hBusiBusinessDate : hTempNotifyDate;
			//hTempNotifyDate = getValue("h_temp_notify_date");
			hTempSystemDate = getValue("h_temp_system_date");
			hTempNotifyTime = getValue("h_temp_notify_time");
		}

		showLogMessage("I", "", "888 BUSINESS=" + hBusiBusinessDate);
	}

	/***********************************************************************/
	int selectTscNotifyLogA() throws Exception {
		/* proc_flag = 0:收檔中 1: 已收檔 2: 已處理 3: 已回應 */
		hTnlgPerformFlag = "";
		hTnlgNotifyDate = "";
		hTnlgCheckCode = "";
		hTnlgProcFlag = "";
		hTnlgRowid = "";

		sqlCmd = "select perform_flag,";
		sqlCmd += " notify_date,";
		sqlCmd += " check_code,";
		sqlCmd += " proc_flag,";
		sqlCmd += " rowid rowid";
		sqlCmd += " from tsc_notify_log  ";
		sqlCmd += "where file_name = ? ";
		setString(1, hTnlgFileName);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTnlgPerformFlag = getValue("perform_flag");
			hTnlgNotifyDate = getValue("notify_date");
			hTnlgCheckCode = getValue("check_code");
			hTnlgProcFlag = getValue("proc_flag");
			hTnlgRowid = getValue("rowid");
		} else {
			comcr.errRtn(String.format("未有[%s]檔案記錄 , 請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
		}

		if (hTnlgPerformFlag.toCharArray()[0] != 'Y') {
			comcr.errRtn(String.format("通知檔收檔發生問題,[%s]暫不可處理 , 請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
		}
		if (hTnlgProcFlag.toCharArray()[0] == '0') {
			comcr.errRtn(String.format("通知檔收檔中[%s] , 請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
		}
		if (hTnlgProcFlag.toCharArray()[0] >= '2') {
			comcr.errRtn(String.format("[%s]退卡資料檔已處理過 , 請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
		}
		if (!hTnlgCheckCode.equals("0000")) {
			showLogMessage("I", "", String.format("[%s]退卡資料檔整檔處理失敗  , 錯誤代碼[%s]", hTnlgFileName, hTnlgCheckCode));
			return (1);
		}
		return (0);
	}

	/***********************************************************************/
	int selectTscOrgdataLog() throws Exception {
		sqlCmd = " select count(*) h_cnt,";
		sqlCmd += " sum(decode(rpt_resp_code, '0000', 0, 1)) h_err_cnt ";
		sqlCmd += " from tsc_orgdata_log  ";
		sqlCmd += " where file_name  = ? ";
		setString(1, hTnlgFileName);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_tsc_orgdata_log not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hCnt = getValueInt("h_cnt");
			hErrCnt = getValueInt("h_err_cnt");
		}

		return (hErrCnt);
	}

	/***********************************************************************/
	void backupRtn() throws Exception {
		tmpstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
		tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
		tmpstr2 = String.format("%s/media/tsc/backup/%s", comc.getECSHOME(), hTnlgFileName);
		comc.fileRename(tmpstr1, tmpstr2);
	}

	/***********************************************************************/
    void deleteTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
    }
    
	/***********************************************************************/
	void deleteBilPostcntl() throws Exception {
		daoTable = "bil_postcntl";
		whereStr = "where this_close_date = ?  ";
		whereStr += "and batch_unit      = substr(?,1,2)  ";
		whereStr += "and mod_pgm         = ? ";
		setString(1, hTempNotifyDate);
		setString(2, fixBillType);
		setString(3, javaProgram);
		deleteTable();
	}

	/***********************************************************************/
	void deleteTscDccgAll() throws Exception {
		daoTable = "tsc_Dccg_all a";
		whereStr = "where to_number(a.tscc_data_seqno) in (select b.tscc_data_seqno from tsc_orgdata_log b "
				+ " where b.file_name       = ?  ";
		whereStr += "and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
		setString(1, hTnlgFileName);
		deleteTable();

	}

	/***********************************************************************/
	void selectPtrBillunit() throws Exception {
		hBiunConfFlag = "";

		sqlCmd = "select conf_flag ";
		sqlCmd += " from ptr_billunit  ";
		sqlCmd += "where bill_unit = substr(?,1,2) ";
		setString(1, fixBillType);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_billunit not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBiunConfFlag = getValue("conf_flag");
		}

	}

	/***********************************************************************/
	void updateTscNotifyLogA() throws Exception {
		daoTable = " tsc_notify_log";
		updateSQL = " proc_flag  = '2',";
		updateSQL += " proc_date  = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += " proc_time  = to_char(sysdate, 'hh24miss'),";
		updateSQL += " mod_pgm    = ?,";
		updateSQL += " mod_time   = sysdate";
		whereStr = " where file_name  = ? ";
		setString(1, javaProgram);
		setString(2, hTnlgFileName);
		updateTable();
//		if (notFound.equals("Y")) {
//			comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
//		}

	}

	/***********************************************************************/
	void fileOpen() throws Exception {
		String str600 = "";
		tmpstr1 = String.format("%s", hTnlgFileName);
		temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", String.format("Open File=[%s]", tmpstr1));
		int f = openInputText(temstr1);
		if (f == -1) {
			comcr.errRtn(String.format("[%s]檔案不存在", temstr1), "", hCallBatchSeqno);
		}
        selectBilPostcntl();
		int br = openInputText(temstr1, "MS950");
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
				continue;

			totalCnt++;

			initTscDccgAll();

			splitBuf1(str600);
			if ((totalCnt % 3000) == 0 || totalCnt == 1)
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

			hOrgdTsccDataSeqno = comcr.getTSCCSeq();

			tmpstr1 = hOrgdTsccDataSeqno;
			hDccgTsccDataSeqno = tmpstr1;

			hOrgdOrgData = str600;
			if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
				hOrgdRptRespCode = "0205";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			hDccgTscCardNo = comc.rtrim(dtl.tscCardNo);

			if (selectTscVdCard() != 0) {
				hOrgdRptRespCode = "0301";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseDate));
			hDccgPurchaseDate = tmpstr1;
			if (!comc.commDateCheck(tmpstr1)) {
				hOrgdRptRespCode = "0203";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseTime));
			hDccgPurchaseTime = tmpstr1;
			if (!comc.commTimeCheck(tmpstr1)) {
				hOrgdRptRespCode = "0204";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmtSign));
			hSign = tmpstr1;
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmt));
			if (!comc.commDigitCheck(tmpstr1)) {
				hOrgdRptRespCode = "0202";
				insertTscOrgdataLog();
				continue;
			}
			hDccgDestinationAmt = comcr.str2double(tmpstr1);
			/*************************************************************************/
			hPostTotRecord++;
			hPostTotAmt = hPostTotAmt + hDccgDestinationAmt;
			/*************************************************************************/
			hDccgTrafficCd = comc.rtrim(dtl.trafficCd);
			hDccgTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
			hDccgAddrCd = comc.rtrim(dtl.addrCd);
			hDccgAddrAbbr = comc.rtrim(dtl.addrAbbr);
			hDccgReturnSource = comc.rtrim(dtl.returnSource);
			tmpstr1 = String.format("%s", comc.rtrim(dtl.serviceAmtSign));
			hSigns = tmpstr1;
			tmpstr1 = String.format("%s", comc.rtrim(dtl.serviceAmt));
			if (!comc.commDigitCheck(tmpstr1)) {
				hOrgdRptRespCode = "0202";
				insertTscOrgdataLog();
				continue;
			}
			hDccgServiceAmt = comcr.str2double(tmpstr1);
			/*************************************************************************/
			/* 20150303 新增 */
			hDccgTrafficCdNew = comc.rtrim(dtl.trafficCdNew);
			hDccgTrafficAbbrNew = comc.rtrim(dtl.trafficAbbrNew);
			hDccgAddrCdNew = comc.rtrim(dtl.addrCdNew);
			hDccgAddrAbbrNew = comc.rtrim(dtl.addAbbrNew);
			hDccgTscTxCode = comc.rtrim(dtl.tscTxCode);
			/**************************************************************************/

			tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 162);
			tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));

			if (!comc.subMS950String(str600.getBytes("MS950"), 162, 162 + 16).equals(tmpstr2)) {
				hOrgdRptRespCode = "0205";
				showLogMessage("I", "", String.format("HASH values error [%s]", hOrgdRptRespCode));
			}

			/*************************************************************************/
			hOrgdRptRespCode = "0000";

			insertTscOrgdataLog();
            insertTscDccgAll();
			if (hOrgdRptRespCode.equals("0000")) {
				succCnt++;
				updateTscVdCard();
			}
		}
        if (totalCnt > 0)
            insertBilPostcntl();

		if (br != -1)
			closeInputText(br);
	}

	/***********************************************************************/
	int selectTscVdCard() throws Exception {
		hTardBalanceDate = "";
		hTardBlackltSDate = "";
		hTardBlackltEDate = "";
		hTardAutoloadFlag = "";
		hTardTscSignFlag = "";
		hTempDiffDays = 0;

		sqlCmd = "select vd_card_no,";
		sqlCmd += " balance_date,";
		sqlCmd += " blacklt_s_date,";
		sqlCmd += " blacklt_e_date,";
		sqlCmd += " days_between(to_date( ?, 'yyyymmdd') , to_date( ?, 'yyyymmdd')) h_temp_diff_days,";
		sqlCmd += " autoload_flag,";
		sqlCmd += " tsc_sign_flag ";
		sqlCmd += " from tsc_vd_card  ";
		sqlCmd += "where tsc_card_no = ? ";
		setString(1, hTempSystemDate.length() == 0 ? null : hTempSystemDate);
		setString(2, hDccgPurchaseDate.length() == 0 ? null : hDccgPurchaseDate);
		setString(3, hDccgTscCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hDccgCardNo = getValue("vd_card_no");
			hTardBalanceDate = getValue("balance_date");
			hTardBlackltSDate = getValue("blacklt_s_date");
			hTardBlackltEDate = getValue("blacklt_e_date");
			hTempDiffDays = getValueInt("h_temp_diff_days");
			hTardAutoloadFlag = getValue("autoload_flag");
			hTardTscSignFlag = getValue("tsc_sign_flag");
		} else
			return (1);
		return (0);
	}

	/***********************************************************************/
	void insertTscOrgdataLog() throws Exception {

		setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
		setValue("file_iden", "DCRT");
		setValue("notify_date", hTempNotifyDate);
		setValue("file_name", hTnlgFileName);
		setValue("org_data", hOrgdOrgData);
		setValue("rpt_resp_code", hOrgdRptRespCode);
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_orgdata_log";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_orgdata_log duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertTscDccgAll() throws Exception {
		/* (悠遊卡退費)請款金額+交通業者簡稱+交易地點簡稱 */
		tmpstr1 = String.format("%s%s", "悠遊卡退費", hDccgTrafficAbbr);
		hDccgSeqNo = totalCnt;

		hAmt = hDccgDestinationAmt * -1;
		hAmtS = hDccgServiceAmt * -1;
		if (hSign.equals("-")) {
			tmpstr1 = String.format("%s%s", "悠遊卡退費請款", hDccgTrafficAbbr);
			hDccgTransactionCode = "05";

			hAmt = hDccgDestinationAmt;
			/* h_Dccg_destination_amt = 0; */
		}
		hDccgMerchantChiName = tmpstr1;
		hDccgBillDesc = tmpstr1;
		if (hSigns.equals("-")) {
			hAmtS = hDccgServiceAmt;
		}
		if (!hDccgReturnSource.equals("AVM")) {
			hDccgPostFlag = "Y";
		}
		if (hDccgTransactionCode.equals("06") == false) {
			hDccgMerchantNo = "EASY8003";
		}
		setValue("batch_no", hTempBatchNo);
		setValueInt("seq_no", hDccgSeqNo);
		setValue("card_no", hDccgCardNo);
		setValue("tsc_card_no", hDccgTscCardNo);
		setValue("bill_type", hDccgBillType);
		setValue("txn_code", hDccgTransactionCode);
		setValue("tsc_tx_code", hDccgTscTxCode);
		setValue("purchase_date", hDccgPurchaseDate);
		setValue("purchase_time", hDccgPurchaseTime);
		setValue("mcht_no", hDccgMerchantNo);
		setValue("mcht_category", "4100");
		setValue("mcht_chi_name", hDccgMerchantChiName);
		setValueDouble("dest_amt", hDccgDestinationAmt);
		setValue("dest_curr", "901");
		setValue("bill_desc", hDccgBillDesc);
		setValue("traffic_cd", hDccgTrafficCd);
		setValue("traffic_abbr", hDccgTrafficAbbr);
		setValue("addr_cd", hDccgAddrCd);
		setValue("addr_abbr", hDccgAddrAbbr);
		setValue("post_flag", hDccgPostFlag);
		setValue("file_name", hTnlgFileName);
		setValue("tsc_error", hOrgdRptRespCode);
		setValue("crt_date", hTempSystemDate);
		setValue("tscc_data_seqno", hDccgTsccDataSeqno);
		setValue("return_source", hDccgReturnSource);
		setValueDouble("service_amt", hDccgServiceAmt);
		setValue("traffic_cd_new", hDccgTrafficCdNew);
		setValue("traffic_abbr_new", hDccgTrafficAbbrNew);
		setValue("addr_cd_new", hDccgAddrCdNew);
		setValue("addr_abbr_new", hDccgAddrAbbrNew);
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_Dccg_all";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_Dccg_all duplicate!", hTempBatchNo, hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void updateTscVdCard() throws Exception {
		daoTable = "tsc_vd_card";
		updateSQL = " return_flag    = 'Y',";
		updateSQL += " return_date    = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += " return_time    = to_char(sysdate, 'hh24miss'),";
		updateSQL += " return_amt     = ?,";
		updateSQL += " return_fee     = ?,";
		updateSQL += " traffic_cd     = ?,";
		updateSQL += " traffic_abbr   = ?,";
		updateSQL += " addr_cd        = ?,";
		updateSQL += " addr_abbr      = ?,";
		updateSQL += " oppost_source  = ?,";
		updateSQL += " mod_pgm        = ?,";
		updateSQL += " mod_time       = sysdate";
		whereStr = "where tsc_card_no = ? ";
		setDouble(1, hAmt);
		setDouble(2, hAmtS);
		setString(3, hDccgTrafficCd);
		setString(4, hDccgTrafficAbbr);
		setString(5, hDccgAddrCd);
		setString(6, hDccgAddrAbbr);
		setString(7, hDccgReturnSource);
		setString(8, javaProgram);
		setString(9, hDccgTscCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertBilPostcntl() throws Exception {
		setValue("batch_date", hBusiBusinessDate);
		setValue("batch_unit", comc.getSubString(hDccgBillType, 0, 2));
		setValue("batch_seq", hTempBatchSeq);
		setValue("batch_no", hTempBatchNo);
		setValueDouble("tot_record", hPostTotRecord);
		setValueDouble("tot_amt", hPostTotAmt);
		setValue("confirm_flag_p", hBiunConfFlag.equals("N") ? "Y" : "N");
		setValue("confirm_flag", hBiunConfFlag);
		setValue("this_close_date", hTempNotifyDate);
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "bil_postcntl";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_bil_postcntl duplicate!", hTempBatchNo, hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void initTscDccgAll() throws Exception {
		hDccgBatchNo = "";
		hDccgSeqNo = 0;
		hDccgCardNo = "";
		hDccgTscCardNo = "";
		hDccgBillType = fixBillType;
		hDccgTransactionCode = "06";
		hDccgTscTxCode = "";
		hDccgPurchaseDate = "";
		hDccgPurchaseTime = "";
		hDccgMerchantNo = "EASY8002";
		hDccgMerchantCategory = "4100";
		hDccgMerchantChiName = "";
		hDccgDestinationAmt = 0;
		hDccgDestinationCurrency = "901";
		hDccgBillDesc = "";
		hDccgTrafficCd = "";
		hDccgTrafficAbbr = "";
		hDccgAddrCd = "";
		hDccgAddrAbbr = "";
		hDccgPostFlag = "N";
		hDccgFileName = "";
		hDccgTscError = "";
		hDccgTscNotiDate = "";
		hDccgTscRespCode = "";
		hDccgReturnSource = "";
		hDccgServiceAmt = 0;
		hDccgTrafficCdNew = "";
		hDccgTrafficAbbrNew = "";
		hDccgAddrCdNew = "";
		hDccgAddrAbbrNew = "";
	}

	/***********************************************************************/
	class Buf1 {
		String type;
		String attri;
		String tscCardNo;
		String purchaseDate;
		String purchaseTime;
		String destinationAmtSign;
		String destinationAmt;
		String trafficCd;
		String addrCd;
		String trafficAbbr;
		String addrAbbr;
		String returnSource;
		String serviceAmtSign;
		String serviceAmt;
		String trafficCdNew;
		String addrCdNew;
		String trafficAbbrNew;
		String addAbbrNew;
		String tscTxCode;
		String filler2;
		String hashValue;
		String filler1;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(type, 1);
			rtn += comc.fixLeft(attri, 2);
			rtn += comc.fixLeft(tscCardNo, 20);
			rtn += comc.fixLeft(purchaseDate, 8);
			rtn += comc.fixLeft(purchaseTime, 6);
			rtn += comc.fixLeft(destinationAmtSign, 1);
			rtn += comc.fixLeft(destinationAmt, 12);
			rtn += comc.fixLeft(trafficCd, 3);
			rtn += comc.fixLeft(addrCd, 3);
			rtn += comc.fixLeft(trafficAbbr, 10);
			rtn += comc.fixLeft(addrAbbr, 10);
			rtn += comc.fixLeft(returnSource, 3);
			rtn += comc.fixLeft(serviceAmtSign, 1);
			rtn += comc.fixLeft(serviceAmt, 12);
			rtn += comc.fixLeft(trafficCdNew, 8);
			rtn += comc.fixLeft(addrCdNew, 10);
			rtn += comc.fixLeft(trafficAbbrNew, 20);
			rtn += comc.fixLeft(addAbbrNew, 20);
			rtn += comc.fixLeft(tscTxCode, 4);
			rtn += comc.fixLeft(filler2, 8);
			rtn += comc.fixLeft(hashValue, 16);
			rtn += comc.fixLeft(filler1, 2);
			return rtn;
		}
	}

	/***********************************************************************/
	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		dtl.type = comc.subMS950String(bytes, 0, 1);
		dtl.attri = comc.subMS950String(bytes, 1, 2);
		dtl.tscCardNo = comc.subMS950String(bytes, 3, 20);
		dtl.purchaseDate = comc.subMS950String(bytes, 23, 8);
		dtl.purchaseTime = comc.subMS950String(bytes, 31, 6);
		dtl.destinationAmtSign = comc.subMS950String(bytes, 37, 1);
		dtl.destinationAmt = comc.subMS950String(bytes, 38, 12);
		dtl.trafficCd = comc.subMS950String(bytes, 50, 3);
		dtl.addrCd = comc.subMS950String(bytes, 53, 3);
		dtl.trafficAbbr = comc.subMS950String(bytes, 56, 10);
		dtl.addrAbbr = comc.subMS950String(bytes, 66, 10);
		dtl.returnSource = comc.subMS950String(bytes, 76, 3);
		dtl.serviceAmtSign = comc.subMS950String(bytes, 79, 1);
		dtl.serviceAmt = comc.subMS950String(bytes, 80, 12);
		dtl.trafficCdNew = comc.subMS950String(bytes, 92, 8);
		dtl.addrCdNew = comc.subMS950String(bytes, 100, 10);
		dtl.trafficAbbrNew = comc.subMS950String(bytes, 110, 20);
		dtl.addAbbrNew = comc.subMS950String(bytes, 130, 20);
		dtl.tscTxCode = comc.subMS950String(bytes, 150, 4);
		dtl.filler2 = comc.subMS950String(bytes, 154, 8);
		dtl.hashValue = comc.subMS950String(bytes, 162, 16);
		dtl.filler1 = comc.subMS950String(bytes, 178, 2);
	}

	/***********************************************************************/
	void selectBilPostcntl() throws Exception {
		hTempBatchSeq = "";
		sqlCmd = "select nvl(substr( to_char( decode( max(batch_seq), 0, 0, max(batch_seq))+1, '0000'), 2, 4),'0000') h_temp_batch_seq ";
		sqlCmd += " from bil_postcntl ";
		sqlCmd += "where batch_unit = substr(?,1,2) ";
		sqlCmd += "  and batch_date = ? ";
		setString(1, fixBillType);
		setString(2, hBusiBusinessDate);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_bil_postcntl not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hTempBatchSeq = getValue("h_temp_batch_seq");
		}

		hTempBatchNo = hBusiBusinessDate + comc.getSubString(hDccgBillType, 0, 2) + hTempBatchSeq;
		hDccgBatchNo = hTempBatchNo;
		showLogMessage("I", "", "888 BATCH_NO=" + hTempBatchNo + "," + hBusiBusinessDate);

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TscD032A proc = new TscD032A();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
