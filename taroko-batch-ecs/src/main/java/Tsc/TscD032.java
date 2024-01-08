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
public class TscD032 extends AccessDAO {
	private boolean debug = false;

	private final String progname = "悠遊VD卡退卡資料檔(DCRT)媒體接收處理程式 111/02/14 V1.00.07";
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
	String hCgecPurchaseDate = "";
	String hCgecCardNo = "";
	String hTardBalanceDate = "";
	String hTardBlackltSDate = "";
	String hTardBlackltEDate = "";
	int hTempDiffDays = 0;
	String hTardAutoloadFlag = "";
	String hTardTscSignFlag = "";
	String hCgecTscCardNo = "";
	String hOrgdTsccDataSeqno = "";
	String hOrgdOrgData = "";
	String hOrgdRptRespCode = "";
	String hTempBatchNo = "";
	String hTempBatchSeq = "";
	String hCgecBillType = "";
	String hCgecTransactionCode = "";
	String hCgecTscTxCode = "";
	String hCgecPurchaseTime = "";
	String hCgecMerchantChiName = "";
	double hCgecDestinationAmt = 0;
	String hCgecBillDesc = "";
	String hCgecTrafficCd = "";
	String hCgecTrafficAbbr = "";
	String hCgecAddrCd = "";
	String hCgecAddrAbbr = "";
	String hCgecPostFlag = "";
	String hCgecTsccDataSeqno = "";
	String hCgecReturnSource = "";
	double hCgecServiceAmt = 0;
	String hCgecTrafficCdNew = "";
	String hCgecTrafficAbbrNew = "";
	String hCgecAddrCdNew = "";
	String hCgecAddrAbbrNew = "";
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
	String hMloaBatchNo = "";
	int hMloaRec = 0;
	String hMloaAcctNo = "";
	double hBpcdNetTtlBp = 0;
	String hTnlhProcessStatus = "";
	String hCgecFileName = "";
	String hCgecTscError = "";
	String hCgecTscNotiDate = "";
	String hCgecTscRespCode = "";
	String hCgecMerchantNo = "";
	String hCgecMerchantCategory = "";
	String hCgecDestinationCurrency = "";
	String hCgecBatchNo = "";
	int hCgecSeqNo = 0;
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
//			if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
//				comc.errExit("Usage : TscD032 [[notify_date][force_flag]] [force_flag][seq]", "");
//			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			// h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
			// "";
			// comcr.callbatch(0, 0, 0);
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
				comc.errExit("Usage : TscD032 [[notify_date][force_flag]] [force_flag][seq]", "");
			}

			tmpstr1 = String.format("DCRT.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
			hTnlgFileName = tmpstr1;
			showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

			/*
			 * if(force_flag==0) { if(select_tsc_notify_log_a()!=0) {
			 * update_tsc_notify_log_a(); backup_rtn(); db_commit(); return(0); } }
			 */

			if (selectTscOrgdataLog() != 0) {
				backupRtn();
				comcr.errRtn(String.format("tsc_f032 檢核有錯本程式不執行.."), "", hCallBatchSeqno);
			}

			fixBillType = "TSCC";
			hCgecBillType = fixBillType;

			deleteBilPostcntl();
			deleteTscCgecAll();
			deleteTscCgecAll();
//第二階段上線須解除mark
//            select_ptr_billunit();
//            select_batch_no();

			hPostTotRecord = hPostTotAmt = 0;
			fileOpen();
//第二階段上線須解除mark
//            update_tsc_notify_log_a();

			backupRtn();

			showLogMessage("I", "",
					String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
			// ==============================================
			// 固定要做的
			// comcr.callbatch(1, 0, 0);
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
			hTempNotifyDate = getValue("h_temp_notify_date");
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
		comc.fileRename(temstr1, tmpstr2);
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
	void deleteTscCgecAll() throws Exception {
		daoTable = "tsc_cgec_all a";
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
	void selectBatchNo() throws Exception {
		hMloaBatchNo = "";
		hMloaRec = 0;

		sqlCmd = "select substr(to_char( to_number( decode( max(batch_no)" + ", ''"
				+ ",  to_char(sysdate,'yyyymmdd')||'00'" + ", max(batch_no)))+1,'   0000000000')"
				+ ", 2, 10) h_mloa_batch_no ";
		sqlCmd += " from mkt_loan  ";
		sqlCmd += "where substr(batch_no,1,8) = to_char(sysdate,'yyyymmdd') ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hMloaBatchNo = getValue("h_mloa_batch_no");
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
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
		}

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
//第二階段上線須解除mark
//        select_bil_postcntl();
		int br = openInputText(temstr1, "MS950");
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
				continue;

			totalCnt++;

			initTscCgecAll();

			splitBuf1(str600);
			if ((totalCnt % 3000) == 0 || totalCnt == 1)
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

			hOrgdTsccDataSeqno = comcr.getTSCCSeq();

			tmpstr1 = hOrgdTsccDataSeqno;
			hCgecTsccDataSeqno = tmpstr1;

			hOrgdOrgData = str600;
			if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
				hOrgdRptRespCode = "0205";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			hCgecTscCardNo = comc.rtrim(dtl.tscCardNo);

			if (selectTscVdCard() != 0) {
				hOrgdRptRespCode = "0301";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseDate));
			hCgecPurchaseDate = tmpstr1;
			if (!comc.commDateCheck(tmpstr1)) {
				hOrgdRptRespCode = "0203";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseTime));
			hCgecPurchaseTime = tmpstr1;
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
			hCgecDestinationAmt = comcr.str2double(tmpstr1);
			/*************************************************************************/
			hPostTotRecord++;
			hPostTotAmt = hPostTotAmt + hCgecDestinationAmt;
			/*************************************************************************/
			hCgecTrafficCd = comc.rtrim(dtl.trafficCd);
			hCgecTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
			hCgecAddrCd = comc.rtrim(dtl.addrCd);
			hCgecAddrAbbr = comc.rtrim(dtl.addrAbbr);
			hCgecReturnSource = comc.rtrim(dtl.returnSource);
			tmpstr1 = String.format("%s", comc.rtrim(dtl.serviceAmtSign));
			hSigns = tmpstr1;
			tmpstr1 = String.format("%s", comc.rtrim(dtl.serviceAmt));
			if (!comc.commDigitCheck(tmpstr1)) {
				hOrgdRptRespCode = "0202";
				insertTscOrgdataLog();
				continue;
			}
			hCgecServiceAmt = comcr.str2double(tmpstr1);
			/*************************************************************************/
			/* 20150303 新增 */
			hCgecTrafficCdNew = comc.rtrim(dtl.trafficCdNew);
			hCgecTrafficAbbrNew = comc.rtrim(dtl.trafficAbbrNew);
			hCgecAddrCdNew = comc.rtrim(dtl.addrCdNew);
			hCgecAddrAbbrNew = comc.rtrim(dtl.addAbbrNew);
			/**************************************************************************/
			/* 原96改162 */
			tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 162);
			tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
			/* 原96改162 */
			if (!comc.subMS950String(str600.getBytes("MS950"), 162, 162 + 16).equals(tmpstr2)) {
				hOrgdRptRespCode = "0205";
				showLogMessage("I", "", String.format("HASH values error [%s]", hOrgdRptRespCode));
				/*
				 * insert_tsc_orgdata_log(); continue;
				 */
			}
			/*************************************************************************/
			if (selectTscVdCard() != 0) {
				hOrgdRptRespCode = "0301";
				insertTscOrgdataLog();
				continue;
			}
			/*************************************************************************/
			hOrgdRptRespCode = "0000";

			insertTscOrgdataLog();
//第二階段上線須解除mark
//            insert_tsc_cgec_all();
			if (hOrgdRptRespCode.equals("0000")) {
				succCnt++;
				updateTscVdCard();
				/*** 若為開啟自動加值的非記名卡 ***/
//第二階段上線須解除mark
//                if (h_tard_autoload_flag.equals("Y") &&
//                    h_tard_tsc_sign_flag.equals("N")) {
//                    /*** 查詢已回饋基金金額 ***/
//                    select_tsc_nsign_loan_hst();
//                    /*** 若餘轉與基金相等 且 基金回饋大於0 ***/
//                    if (h_sign.equals("-") == false &&
//                        h_tnlh_remain_amt == h_cgec_destination_amt &&
//                        h_tnlh_prebalance_amt > 0) {
//                        /*** 加檔 寫入正向費用 ***/
//                        insert_bil_sysexp();
//                        /*** 已餘額轉置 已線上加檔 ***/
//                        h_tnlh_process_status = "3";
//                        update_tsc_nsign_loan_hst();
//                    } else if(h_tnlh_prebalance_amt > 0) {
//                        if (h_tnlh_prebalance_amt > h_cgec_destination_amt) {
//                             h_tnlh_prebalance_amt = h_cgec_destination_amt;
//
//                             //起 P-61 分錄(reference bil_a008) 金額為 (h_tnlh_prebalance_amt - h_cgec_destination_amt)
//                             vouch_rtn();
//                        }
//                          
//                        if (h_tnlh_prebalance_amt > 0)  insert_bil_sysexp();
//
//                        h_tnlh_process_status = "5";
//                        update_tsc_nsign_loan_hst();
//                    }
//                }
			}
		}
//第二階段上線須解除mark
//        if (total_cnt > 0)
//            insert_bil_postcntl();

		if (br != -1)
			closeInputText(br);
	}

	/**********************************************************************/
	@SuppressWarnings("unused")
	void vouchRtn() throws Exception {
		String hTAcNo = "";
		String hTMemo3Kind = "";
		String hTMemo3Flag = "";
		String hTDbcr = "";
		String hTCrFlag = "";
		String hTDrFlag = "";
		String tMemo3 = "";
		String tMemo2 = "";
		int hTSeqno = 0;
		double feeP1 = 0;
		double feeP05 = 0;
		double feeM1 = 0;
		double feeM05 = 0;
		double tempDouble = 0;
		int vouchCnt = 0;

		String hVouchCdKind = "P-61";

		comcr.hGsvhModPgm = javaProgram;
		/** OP --- > 1 OI --> 6 **/
		String tempVouchType = "1";

		comcr.hVoucSysRem = hVouchCdKind;

		comcr.startVouch(tempVouchType, hVouchCdKind);

		sqlCmd = "select ";
		sqlCmd += "gen_sys_vouch.ac_no,";
		sqlCmd += "gen_sys_vouch.dbcr_seq,";
		sqlCmd += "gen_sys_vouch.dbcr,";
		sqlCmd += "gen_acct_m.memo3_kind,";
		sqlCmd += "decode(gen_acct_m.memo3_flag,'','N',gen_acct_m.memo3_flag) h_t_memo3_flag,";
		sqlCmd += "decode(gen_acct_m.dr_flag   ,'','N',gen_acct_m.dr_flag)    h_t_dr_flag,";
		sqlCmd += "decode(gen_acct_m.cr_flag   ,'','N',gen_acct_m.cr_flag)    h_t_cr_flag ";
		sqlCmd += " from gen_sys_vouch,gen_acct_m ";
		sqlCmd += "where std_vouch_cd        = ? ";
		sqlCmd += "  and gen_sys_vouch.ac_no = gen_acct_m.ac_no ";
		sqlCmd += "order by gen_sys_vouch.dbcr_seq,decode(dbcr,'D','A',dbcr) ";
		setString(1, hVouchCdKind);
		int tmpInt = selectTable();
		for (int i = 0; i < tmpInt; i++) {
			hTSeqno = 0;
			comcr.hGsvhMemo2 = "";
			hTDbcr = "";
			hTMemo3Kind = "";
			hTMemo3Flag = "";
			hTDrFlag = "";
			hTCrFlag = "";
			comcr.hVoucIdNo = "";

			hTAcNo = getValue("ac_no", i);
			hTSeqno = getValueInt("dbcr_seq", i);
			hTDbcr = getValue("dbcr", i);
			hTMemo3Kind = getValue("memo3_kind", i);
			hTMemo3Flag = getValue("h_t_memo3_flag", i);
			hTDrFlag = getValue("h_t_dr_flag", i);
			hTCrFlag = getValue("h_t_cr_flag", i);

			tMemo3 = String.format("%3.3s年%2.2s月%2.2s日", hBusinssChiDate.substring(0, 3),
					hBusinssChiDate.substring(3, 5), hBusinssChiDate.substring(5));
			comcr.hGsvhMemo1 = tMemo3;

			comcr.hGsvhMemo2 = "TSCC  請款";
			double totalAmt = 0;
			switch (hTSeqno) {
			case 2:
			case 5:
				totalAmt = hTnlhPrebalanceAmt - hCgecDestinationAmt;
				break;
			default:
				totalAmt = 0;
			}

			comcr.hGsvhMemo3 = "";
			if (hTMemo3Flag.equals("Y")) {
				if (((hTDbcr.equals("D")) && (hTCrFlag.equals("Y")))
						|| ((hTDbcr.equals("C")) && (hTDrFlag.equals("Y")))) {
					vouchCnt++;
				}
				if (hTMemo3Kind.equals("1")) {
					comcr.hGsvhMemo3 = hCgecTscCardNo;
				} else if (hTMemo3Kind.equals("2")) {
					tMemo3 = String.format("%4.4s%-12.12s", "ID :", hCurpId);
					comcr.hVoucIdNo = hCur1Id;
					comcr.hGsvhMemo3 = tMemo3;
				} else if (hTMemo3Kind.equals("3")) {
					tMemo3 = String.format("%6.6s%6.6s%02d", hVouchChiDate.substring(1), comcr.hVoucRefno, vouchCnt);
					comcr.hGsvhMemo3 = tMemo3;
				}
			}
			if (totalAmt > 0) {
				comcr.hGsvhModUser = javaProgram;
				if (comcr.detailVouch(hTAcNo, hTSeqno, totalAmt, "901") != 0) {
					comcr.errRtn("fetch vouch error 002 error", "", comcr.hCallBatchSeqno);
				}
			}
		}

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
		setString(2, hCgecPurchaseDate.length() == 0 ? null : hCgecPurchaseDate);
		setString(3, hCgecTscCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hCgecCardNo = getValue("vd_card_no");
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
	void insertTscCgecAll() throws Exception {
		/* (悠遊卡退費)請款金額+交通業者簡稱+交易地點簡稱 */
		tmpstr1 = String.format("%s%s%s", "悠遊卡退費", hCgecTrafficAbbr, hCgecAddrAbbr);
		hCgecSeqNo = totalCnt;

		hAmt = hCgecDestinationAmt * -1;
		hAmtS = hCgecServiceAmt * -1;
		if (hSign.equals("-")) {
			tmpstr1 = String.format("%s%s%s", "悠遊卡退費請款", hCgecTrafficAbbr, hCgecAddrAbbr);
			hCgecTransactionCode = "05";

			hAmt = hCgecDestinationAmt;
			/* h_cgec_destination_amt = 0; */
		}
		hCgecMerchantChiName = tmpstr1;
		hCgecBillDesc = tmpstr1;
		if (hSigns.equals("-")) {
			hAmtS = hCgecServiceAmt;
		}
		if (!hCgecReturnSource.equals("AVM")) {
			hCgecPostFlag = "Y";
		}
		if (hCgecTransactionCode.equals("06") == false) {
			hCgecMerchantNo = "EASY8003";
		}
		setValue("batch_no", hTempBatchNo);
		setValueInt("seq_no", hCgecSeqNo);
		setValue("card_no", hCgecCardNo);
		setValue("tsc_card_no", hCgecTscCardNo);
		setValue("bill_type", hCgecBillType);
		setValue("txn_code", hCgecTransactionCode);
		setValue("tsc_tx_code", hCgecTscTxCode);
		setValue("purchase_date", hCgecPurchaseDate);
		setValue("purchase_time", hCgecPurchaseTime);
		setValue("mcht_no", hCgecMerchantNo);
		setValue("mcht_category", "4100");
		setValue("mcht_chi_name", hCgecMerchantChiName);
		setValueDouble("dest_amt", hCgecDestinationAmt);
		setValue("dest_curr", "901");
		setValue("bill_desc", hCgecBillDesc);
		setValue("traffic_cd", hCgecTrafficCd);
		setValue("traffic_abbr", hCgecTrafficAbbr);
		setValue("addr_cd", hCgecAddrCd);
		setValue("addr_abbr", hCgecAddrAbbr);
		setValue("post_flag", hCgecPostFlag);
		setValue("file_name", hTnlgFileName);
		setValue("tsc_error", hOrgdRptRespCode);
		setValue("crt_date", hTempSystemDate);
		setValue("tscc_data_seqno", hCgecTsccDataSeqno);
		setValue("return_source", hCgecReturnSource);
		setValueDouble("service_amt", hCgecServiceAmt);
		setValue("traffic_cd_new", hCgecTrafficCdNew);
		setValue("traffic_abbr_new", hCgecTrafficAbbrNew);
		setValue("addr_cd_new", hCgecAddrCdNew);
		setValue("addr_abbr_new", hCgecAddrAbbrNew);
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		daoTable = "tsc_cgec_all";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_tsc_cgec_all duplicate!", hTempBatchNo, hCallBatchSeqno);
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
		setString(3, hCgecTrafficCd);
		setString(4, hCgecTrafficAbbr);
		setString(5, hCgecAddrCd);
		setString(6, hCgecAddrAbbr);
		setString(7, hCgecReturnSource);
		setString(8, javaProgram);
		setString(9, hCgecTscCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void selectTscNsignLoanHst() throws Exception {
		hTnlhPrebalanceAmt = 0;

		sqlCmd = "select t.acct_type,";
		sqlCmd += " a.acct_key,";
		sqlCmd += " t.card_no,";
		sqlCmd += " a.id_p_seqno,";
		sqlCmd += "nvl(uf_idno_id(a.id_p_seqno),'') as id,";
		sqlCmd += "uf_hi_idno(uf_idno_id(a.id_p_seqno)) h_cur1_id,";
		sqlCmd += " (select chi_name from crd_idno where id_p_seqno = t.major_id_p_seqno) major_chi_name,";
		sqlCmd += " t.prebalance_amt,";
		sqlCmd += " t.remain_amt ";
		sqlCmd += " from tsc_nsign_loan_hst t, act_acno a  ";
		sqlCmd += "where t.tsc_card_no = ? ";
		sqlCmd += "  and t.id_p_seqno = a.id_p_seqno ";
		setString(1, hCgecTscCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTnlhAcctType = getValue("acct_type");
			hTnlhAcctKey = getValue("acct_key");
			hTnlhCardNo = getValue("card_no");
			hTnlhIdPSeqno = getValue("id_p_seqno");
			hCurpId = getValue("id");
			hCur1Id = getValue("h_cur1_id");
			hTnlhMajorChiName = getValue("major_chi_name");
			hTnlhPrebalanceAmt = getValueDouble("prebalance_amt");
			hTnlhRemainAmt = getValueDouble("remain_amt");
		}

	}

	/***********************************************************************/
	void selectCrdCard() throws Exception {
		hPSeqno = "";
		hAcctType = "";
		sqlCmd = "select acct_type, acno_p_seqno";
		sqlCmd += " from crd_card  ";
		sqlCmd += "where card_no = ? ";
		setString(1, hTnlhCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hAcctType = getValue("acct_type");
			hPSeqno = getValue("acno_p_seqno");
		}
	}

	/***********************************************************************/
	void insertBilSysexp() throws Exception {
		selectCrdCard();
		setValue("card_no", hTnlhCardNo);
		setValue("acct_type", hAcctType);
		setValue("acno_p_seqno", hPSeqno);
		setValue("bill_type", "INDS");
		setValue("txn_code", "05");
		setValue("purchase_date", hBusiBusinessDate);
		setValueDouble("dest_amt", (double) (int) hTnlhPrebalanceAmt);
		setValue("dest_curr", "901");
		setValueDouble("src_amt", (double) (int) hTnlhPrebalanceAmt);
		setValue("bill_desc", "沖正" + comc.getSubString(hBusiBusinessDate, 4, 4 + 2) + "/"
				+ comc.getSubString(hBusiBusinessDate, 6, 6 + 2) + "悠遊卡退費");
		setValue("post_flag", "U");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "bil_sysexp";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_bil_sysexp duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void updateTscNsignLoanHst() throws Exception {
		daoTable = " tsc_nsign_loan_hst";
		updateSQL = " process_status     = ?, ";
		updateSQL += " mod_time           = sysdate ";
		updateSQL += " mod_pgm            = ? ";
		updateSQL += " balance_amt        = ? ";
		whereStr = " where tsc_card_no  = ? ";
		setString(1, hTnlhProcessStatus);
		setString(2, javaProgram);
		setDouble(3, hCgecDestinationAmt);
		setString(4, hCgecTscCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_nsign_loan_hst not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertBilPostcntl() throws Exception {
		setValue("batch_date", hBusiBusinessDate);
		setValue("batch_unit", comc.getSubString(hCgecBillType, 0, 2));
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
	void initTscCgecAll() throws Exception {
		hCgecBatchNo = "";
		hCgecSeqNo = 0;
		hCgecCardNo = "";
		hCgecTscCardNo = "";
		hCgecBillType = fixBillType;
		hCgecTransactionCode = "06";
		hCgecTscTxCode = "";
		hCgecPurchaseDate = "";
		hCgecPurchaseTime = "";
		hCgecMerchantNo = "EASY8002";
		hCgecMerchantCategory = "4100";
		hCgecMerchantChiName = "";
		hCgecDestinationAmt = 0;
		hCgecDestinationCurrency = "901";
		hCgecBillDesc = "";
		hCgecTrafficCd = "";
		hCgecTrafficAbbr = "";
		hCgecAddrCd = "";
		hCgecAddrAbbr = "";
		hCgecPostFlag = "N";
		hCgecFileName = "";
		hCgecTscError = "";
		hCgecTscNotiDate = "";
		hCgecTscRespCode = "";
		hCgecReturnSource = "";
		hCgecServiceAmt = 0;
		hCgecTrafficCdNew = "";
		hCgecTrafficAbbrNew = "";
		hCgecAddrCdNew = "";
		hCgecAddrAbbrNew = "";
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
			rtn += comc.fixLeft(filler2, 12);
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
		dtl.filler2 = comc.subMS950String(bytes, 150, 12);
		dtl.hashValue = comc.subMS950String(bytes, 162, 16);
		dtl.filler1 = comc.subMS950String(bytes, 178, 2);
	}

	/***********************************************************************/
	void selectBilPostcntl() throws Exception {
		hTempBatchSeq = "";
		sqlCmd = "select substr( to_char( decode( max(batch_seq), 0, 0, max(batch_seq))+1, '0000'), 2, 4) h_temp_batch_seq ";
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

		hTempBatchNo = hBusiBusinessDate + comc.getSubString(hCgecBillType, 0, 2) + hTempBatchSeq;
		hCgecBatchNo = hTempBatchNo;
		showLogMessage("I", "", "888 BATCH_NO=" + hTempBatchNo + "," + hBusiBusinessDate);

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TscD032 proc = new TscD032();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
