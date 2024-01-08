/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00  Edson       program initial                           *
*  107/12/04  V1.00.01  Lai         modify vouch                              *
*  109/11/26  V1.00.02  shiyuqi     updated for project coding standard       *  
*  111/09/22  V1.00.03  JeffKung    updated for TCB                           * 
*  112/05/31  V1.00.04  JeffKung    有利率時放次期入帳                                                   *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

/*分期付款產生當期暫存檔處理程式*/
public class BilD003 extends AccessDAO {
	private String progname = "分期付款產生當期暫存檔處理程式   112/05/31 V1.00.04 ";

	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommRoutine comr = null;

	String prgmId = "BilD003";
	String prgmName = "分期付款產生當期暫存檔處理程式";
	String buf = "";
	String szTmp = "";
	String stderr = "";
	String hCallBatchSeqno = "";

	String hEffcMonth = "";
	int lastMonthDays = 0;
	int yearDays = 0;
	String hBusiBusinessDate = "";
	String hTempBusinessDate = "";
	String hTempSysdate = "";
	String hTempChiDate = "";
	String hTempVouchChiDate = "";
	String hPreBusinessDate = "";
	int hTempBilBillunitCnt = 0;
	int hContPostCycleDd = 0;
	int hPre29Dd = 0;
	int hPre30Dd = 0;
	int hPre31Dd = 0;
	String hTempContractNo = "";
	String hContContractNo = "";
	String hContContractSeqNo = "";
	String hContContractKind = "";
	String hContRealCardNo = "";
	String hContAcctType = "";
	String hContPSeqno = "";
	String hContProductNo = "";
	String hContProductName = "";
	String hContMchtNo = "";
	String hContPtrMchtNo = "";
	String hContMchtChiName = "";
	String hContMchtEngName = "";
	int hContQty = 0;
	int hContRefundQty = 0;
	double hContTotAmt = 0;
	double hContRemdAmt = 0;
	int hContInstallCurrTerm = 0;
	String hContAllPostFlag = "";
	String hContRefundFlag = "";
	String hContRefundAprFlag = "";
	String hContConfirmFlag = "";
	String hContLimitEndDate = "";
	double hContCltFeesAmt = 0;
	double hContCltRemdAmt = 0;
	String hContCltForcedPostFlag = "";
	String hContForcedPostFlag = "";
	String hContFeeFlag = "";
	String hContReferenceNo = "";
	String hContInstallmentKind = "";
	double hTempTotAmt = 0;
	String hContNewProcDate = "";
	String hContNewItFlag = "";
	String hContCpsFlag = "";
	String hContPurchaseDate = "";
	String hContFirstPostDate = "";
	String hContFirstPostKind = "";
	String hContYearFeesDate = "";
	String hContModPgm = "";
	String hContRowid = "";
	String hMercMchtType = "";
	String hConpMchtEngName = "";
	String hConpMchtCity = "";
	String hConpMchtCountry = "";
	String hConpMchtCategory = "";
	String hConpMchtZip = "";
	String hConpMchtState = "";
	String hMercInstallmentDelay = "";
	int crdCardCnt = 0;
	String hCardCurrentCode = "";
	String hCardPSeqno = "";
	String hCardAcctType = "";
	String hConpMchtChiName = "";
	double hConpCurrPostAmt = 0;
	String hConpInstallCurrTerm = "";
	String hConpMchtNo = "";
	String hMercMchtChiName = "";
	String hMercMchtAddress = "";
	String hMercMchtZip = "";
	String hMercMchtFax1 = "";
	String hMercMchtFax11 = "";
	String hMercMchtAcctName = "";
	String hMercAssignAcct = "";
	String hMercClrBankId = "";
	String hMercBankName = "";
	String hMercOthBankId = "";
	String hMercOthBankAcct = "";
	int hTempContractKind = 0;
	double hContRedeemAmt = 0;
	double hContFeesFixAmt = 0;
	double hContFeesRate = 0;
	double hContExchangeAmt = 0;
	String hContBatchNo = "";
	double hProdFeesMinAmt = 0;
	double hProdFeesMaxAmt = 0;
	String hPrintName = "";
	String hRptName = "";
	String hCurpTxConvtFlag = "";
	String hVouchCdKind = "";
	String hTAcNo = "";
	int hTSeqno = 0;
	String hTDbcr = "";
	String hTMemo3Kind = "";
	String hTMemo3Flag = "";
	String hTDrFlag = "";
	String hTCrFlag = "";
	double hContExtraFees = 0;
	double hContUnitPrice = 0;
	double hContFirstRemdAmt = 0;
	int hContInstallTotTerm = 0;
	String hConpKindAmt = "";
	double hContCltUnitPrice = 0;
	double hContTransRate = 0;
	double hProdTransRate = 0;
	int hContCltInstallTotTerm = 0;
	double hAgenRevolvingInterest1 = 0;

	double hCurpPostAmt = 0;

	int hPreDd = 0;
	int totalCnt = 0;
	int runTerm = 0;
	int bilContpostCnt = 0;
	int pageCnt = 0;
	double totalAmt = 0;
	String rateEffcMonth = "201509";
	String newMethod = "";
	String tmpstr = "";
	double oldTransRate = 0;
	long longAmt = 0;
	long longAmt1 = 0;
	double[][] dtlAmt = new double[3][];
	int[] hInCnt = new int[250];
	double[] hInAmt = new double[250];
	int[] hPoCnt = new int[250];
	double[] hPoAmt = new double[250];
	int[] hInstTerm = new int[250];

	int vouchCnt = 0;
	String hStmtInstFlag = "";

// ******************************************************************************
	public int mainProcess(String[] args) {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0 && args.length != 1 && args.length != 2) {
				comc.errExit("Usage : BilD003 [[business_date] [contract_no]]", "");
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			hBusiBusinessDate = "";
			hTempContractNo = "";
			if (args.length >= 1) {
				if (args[0].length() == 8) {
					hBusiBusinessDate = args[0];
				} else {
					hTempContractNo = args[0];
				}
			}

			if (args.length == 2)
				hTempContractNo = args[1];

			selectPtrBusinday();

			hContPostCycleDd = comcr.str2int(hBusiBusinessDate.substring(6));
			hPreDd = comcr.str2int(hPreBusinessDate.substring(6));

			/*** 給定不會生效的預設值 ***/
			hPre29Dd = 99;
			hPre30Dd = 99;
			hPre31Dd = 99;

			if (hContPostCycleDd == 1) {
				/*** 每月一號時要處理上個月末未處理的資料 ***/
				if (hPreDd == 28) {
					hPre29Dd = 29;
					hPre30Dd = 30;
					hPre31Dd = 31;
				} else if (hPreDd == 29) {
					hPre30Dd = 30;
					hPre31Dd = 31;
				} else if (hPreDd == 30) {
					hPre31Dd = 31;
				}
			}

			selectPtrActgeneral();
			hEffcMonth = rateEffcMonth;
			sqlCmd = "select to_number(to_char(last_day(add_months(to_date(?,'yyyymm'),-1)),'dd')),";
			sqlCmd += "to_number(to_char(to_date((substr(?,1,4) || '1231'),'yyyymmdd'),'DDD')) year_days ";
			sqlCmd += " from dual ";
			setString(1, hEffcMonth);
			setString(2, hEffcMonth);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				lastMonthDays = getValueInt("last_month_days");
				yearDays = getValueInt("year_days");
			}
			yearDays = 365;
			newMethod = "N";
			if (hEffcMonth.substring(0, 6).compareTo(hBusiBusinessDate.substring(0, 6)) <= 0)
				newMethod = "Y";

			stderr = String.format(" Process month=[%s] last_m_days=[%d] [%s][%d] [%d]\n", rateEffcMonth, lastMonthDays,
					hBusiBusinessDate, hContPostCycleDd, yearDays);
			showLogMessage("I", "", stderr);

			selectBilBillunit();
			totalCnt = 0;

			selectBilContract();
			showLogMessage("I", "", String.format("合約檔處理筆數 [%d]", totalCnt));
			showLogMessage("I", "", "執行結束");
			
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/**********************************************************************/
	void selectPtrBusinday() throws Exception {
		sqlCmd = "select decode(cast(? as varchar(10)),'',business_date,?) h_busi_business_date,";
		sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(10)),'',business_date,?),'yyyymmdd'),1),'yyyymmdd') h_temp_business_date,";
		sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_temp_sysdate,";
		sqlCmd += "substr(to_char(to_number(business_date)- 19110000,'0000000'),2,7) h_temp_chi_date,";
		sqlCmd += "substr(to_char(to_number(vouch_date)- 19110000,'0000000'),2,7) h_temp_vouch_chi_date,";
		sqlCmd += "to_char(to_date(decode(cast(? as varchar(10)),'',business_date,?),'yyyymmdd')-1 days,'yyyymmdd') h_pre_business_date ";
		sqlCmd += " from ptr_businday ";
		setString(1, hBusiBusinessDate);
		setString(2, hBusiBusinessDate);
		setString(3, hBusiBusinessDate);
		setString(4, hBusiBusinessDate);
		setString(5, hBusiBusinessDate);
		setString(6, hBusiBusinessDate);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("h_busi_business_date");
			hTempBusinessDate = getValue("h_temp_business_date");
			hTempSysdate = getValue("h_temp_sysdate");
			hTempChiDate = getValue("h_temp_chi_date");
			hTempVouchChiDate = getValue("h_temp_vouch_chi_date");
			hPreBusinessDate = getValue("h_pre_business_date");
		} else {
			comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	int selectPtrActgeneral() throws Exception {
		sqlCmd = "select max(round(revolving_interest1*365/100,2)) h_agen_revolving_interest1 ";
		sqlCmd += " from ptr_actgeneral_n ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hAgenRevolvingInterest1 = getValueDouble("h_agen_revolving_interest1");
		} else {
			comcr.errRtn("select ptr_actgeneral_n not found!", "", hCallBatchSeqno);
		}

		return 0;
	}

	/***********************************************************************/
	void selectBilBillunit() throws Exception {
		hTempBilBillunitCnt = 0;
		sqlCmd = "select to_number(substr(decode(indelv_mx,'','9999',indelv_mx),2,3)) h_temp_bil_billunit_cnt ";
		sqlCmd += " from ptr_billunit  ";
		sqlCmd += "where bill_unit = 'OI' ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select ptr_billunit not found!", "OI", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hTempBilBillunitCnt = getValueInt("h_temp_bil_billunit_cnt");
		}
	}

	/***********************************************************************/
	void selectBilContract() throws Exception {
		int int1a, tempCurrTerm;
		double doubleAmt;
		long longAmt;
		long longAmt1;

		sqlCmd = "select ";
		sqlCmd += "contract_no,";
		sqlCmd += "contract_seq_no,";
		sqlCmd += "contract_kind,";
		sqlCmd += "card_no,";
		sqlCmd += "acct_type,";
		sqlCmd += "acno_p_seqno,";
		sqlCmd += "product_no,";
		sqlCmd += "product_name,";
		sqlCmd += "mcht_no,";
		sqlCmd += "ptr_mcht_no,";
		sqlCmd += "mcht_chi_name,";
		sqlCmd += "mcht_eng_name,";
		sqlCmd += "unit_price,";
		sqlCmd += "qty,";
		sqlCmd += "refund_qty,";
		sqlCmd += "tot_amt,";
		sqlCmd += "install_tot_term*unit_price+remd_amt+first_remd_amt as h_curp_post_amt,";
		sqlCmd += "install_tot_term,";
		sqlCmd += "remd_amt,";
		sqlCmd += "first_remd_amt,";
		sqlCmd += "extra_fees,";
		sqlCmd += "install_curr_term,";
		sqlCmd += "all_post_flag,";
		sqlCmd += "refund_flag,";
		sqlCmd += "refund_apr_flag,";
		sqlCmd += "decode(apr_flag,'','N',apr_flag) h_m_cont_confirm_flag,";
		sqlCmd += "limit_end_date,";
		sqlCmd += "clt_fees_amt,";
		sqlCmd += "clt_unit_price,";
		sqlCmd += "clt_install_tot_term,";
		sqlCmd += "clt_remd_amt,";
		sqlCmd += "decode(clt_forced_post_flag,'','N',clt_forced_post_flag) h_m_cont_clt_forced_post_flag,";
		sqlCmd += "decode(forced_post_flag,'','N',forced_post_flag) h_m_cont_forced_post_flag,";
		sqlCmd += "decode(fee_flag,'','L',fee_flag) h_m_cont_fee_flag,";
		sqlCmd += "reference_no,";
		sqlCmd += "decode(installment_kind,'','N',installment_kind) h_m_cont_installment_kind,";
		sqlCmd += "decode(refund_apr_flag,'Y',(qty-refund_qty)*tot_amt,qty*tot_amt) h_m_temp_tot_amt,";
		sqlCmd += "new_proc_date,";
		sqlCmd += "decode(install_tot_term,1,'N',decode(new_it_flag,'','N',new_it_flag)) h_m_cont_new_it_flag,";
		sqlCmd += "cps_flag,";
		sqlCmd += "purchase_date,";
		sqlCmd += "first_post_date,";
		sqlCmd += "trans_rate,";
		sqlCmd += "year_fees_date,";
		sqlCmd += "first_post_kind,";
		sqlCmd += "mod_pgm,";
		sqlCmd += "rowid  as rowid ";
		sqlCmd += "from bil_contract ";
		/*** 分期交易或是有授權碼的郵購交易 ***/
		sqlCmd += "where ((decode(auth_code,'','N',auth_code) not in ('N','REJECT','P','reject') ";
		sqlCmd += "and contract_kind = '2' ) ";  
		sqlCmd += "or contract_kind = '1') ";
		/*** 第零期且自動出貨且未填關帳週期日且已放行 ***/
		sqlCmd += "and ((install_curr_term = 0 ";
		sqlCmd += "and decode(auto_delv_flag,'','N',auto_delv_flag) = 'Y' ";
		sqlCmd += "and post_cycle_dd  = 0 ";
		sqlCmd += "and apr_date != '') ";
		/*** 第零期且非自動出貨但已出貨放行且未填關帳週期 ***/
		sqlCmd += "or (install_curr_term = 0 ";
		sqlCmd += "and decode(auto_delv_flag,'','N',auto_delv_flag) != 'Y' ";
		sqlCmd += "and delv_date != '' ";
		sqlCmd += "and post_cycle_dd = 0 ";
		sqlCmd += "and delv_confirm_date != '') ";
		/*** 已填關帳週期且未全部入帳 ***/
		sqlCmd += "or (post_cycle_dd  = ? ";
		sqlCmd += "and decode(all_post_flag,'','N',all_post_flag)!= 'Y' ";
		sqlCmd += "and install_curr_term != install_tot_term ) ";
		/*** CB 強迫一次入帳 ***/
		sqlCmd += "or (forced_post_from  = '1' and forced_post_flag  = 'Y' ) ";
		/*** 若h_pre_29_dd=99 此行不會生效 ***/
		sqlCmd += "or (post_cycle_dd  = ? ";
		sqlCmd += "and decode(all_post_flag,'','N',all_post_flag)!= 'Y' ";
		sqlCmd += "and install_curr_term != install_tot_term ) ";
		/*** 若h_pre_30_dd=99 此行不會生效 ***/
		sqlCmd += "or (post_cycle_dd  = ? ";
		sqlCmd += "and decode(all_post_flag,'','N',all_post_flag)!= 'Y' ";
		sqlCmd += "and install_curr_term != install_tot_term ) ";
		/*** 若h_pre_31_dd=99 此行不會生效 ***/
		sqlCmd += "or (post_cycle_dd  = ? ";
		sqlCmd += "and decode(all_post_flag,'','N',all_post_flag)!= 'Y' ";
		sqlCmd += "and install_curr_term != install_tot_term )) ";
		/*** 排除RC轉帳分 ***/
		sqlCmd += "and decode(mcht_no,'',' ', mcht_no) not in (select mcht_no ";
		sqlCmd += "from bil_merchant ";
		sqlCmd += "where decode(trans_flag,'','N',trans_flag) in ('y','Y')) ";
		/*** 有指定分期交易的合約編號時使用 ***/
		sqlCmd += "and contract_no = decode(cast(? as varchar(10)),'',contract_no,?) ";
		sqlCmd += "and decode(last_update_date,'','20000101',last_update_date) < ? ";

		setInt(1, hContPostCycleDd);
		setInt(2, hPre29Dd);
		setInt(3, hPre30Dd);
		setInt(4, hPre31Dd);
		setString(5, hTempContractNo);
		setString(6, hTempContractNo);
		setString(7, hBusiBusinessDate);

		openCursor();
		while (fetchTable()) {
			hContContractNo = getValue("contract_no");
			hContContractSeqNo = getValue("contract_seq_no");
			hContContractKind = getValue("contract_kind");
			hContRealCardNo = getValue("card_no");
			hContAcctType = getValue("acct_type");
			hContPSeqno = getValue("acno_p_seqno");
			hContProductNo = getValue("product_no");
			hContProductName = getValue("product_name");
			hContMchtNo = getValue("mcht_no");
			hContPtrMchtNo = getValue("ptr_mcht_no");
			hContMchtChiName = getValue("mcht_chi_name");
			hContMchtEngName = getValue("mcht_eng_name");
			hContUnitPrice = getValueDouble("unit_price");
			hContQty = getValueInt("qty");
			hContRefundQty = getValueInt("refund_qty");
			hContTotAmt = getValueDouble("tot_amt");
			hContInstallTotTerm = getValueInt("install_tot_term");
			hContRemdAmt = getValueDouble("remd_amt");
			hContFirstRemdAmt = getValueDouble("first_remd_amt");
			hContExtraFees = getValueDouble("extra_fees");
			hContInstallCurrTerm = getValueInt("install_curr_term");
			hContAllPostFlag = getValue("all_post_flag");
			hContRefundFlag = getValue("refund_flag");
			hContRefundAprFlag = getValue("refund_apr_flag");
			hContConfirmFlag = getValue("h_m_cont_confirm_flag");
			hContLimitEndDate = getValue("limit_end_date");
			hContCltFeesAmt = getValueDouble("clt_fees_amt");
			hContCltUnitPrice = getValueDouble("clt_unit_price");
			hContCltInstallTotTerm = getValueInt("clt_install_tot_term");
			hContCltRemdAmt = getValueDouble("clt_remd_amt");
			hContCltForcedPostFlag = getValue("h_m_cont_clt_forced_post_flag");
			hContForcedPostFlag = getValue("h_m_cont_forced_post_flag");
			hContFeeFlag = getValue("h_m_cont_fee_flag");
			hContReferenceNo = getValue("reference_no");
			hContInstallmentKind = getValue("h_m_cont_installment_kind");
			hTempTotAmt = getValueDouble("h_m_temp_tot_amt");
			hCurpPostAmt = getValueDouble("h_curp_post_amt");
			hContNewProcDate = getValue("new_proc_date");
			hContNewItFlag = getValue("h_m_cont_new_it_flag");
			hContCpsFlag = getValue("cps_flag");
			hContPurchaseDate = getValue("purchase_date");
			hContFirstPostDate = getValue("first_post_date");
			hContTransRate = getValueDouble("trans_rate");
			hContYearFeesDate = getValue("year_fees_date");
			hContFirstPostKind = getValue("first_post_kind");
			hContModPgm = getValue("mod_pgm");
			hContRowid = getValue("rowid");

			totalCnt++;

			if (totalCnt % 5000 == 0 || totalCnt == 1)
				showLogMessage("I", "", "Current Process record=" + totalCnt);

			/*** default 首期入帳 ***/
			if (hContInstallCurrTerm == 0 && hContFirstPostKind.length() == 0) {
				
				//有利率的就放次期入帳
				if (hContTransRate != 0) {
					hContFirstPostKind = "0";
				} else {
					hContFirstPostKind = "1";
				}
				
				selectBilMerchant(hContMchtNo);
			}

			/*** 新制分期、未入帳且未處理 ***/
			if ((hContNewItFlag.equals("Y")) && (hContInstallCurrTerm == 0) && (hContNewProcDate.length() == 0)) {
				/*** 若已入過帳 ***/
				if (hContFirstPostDate.length() != 0) {
					/*** 直接更新合約檔 ***/
					updateBilContract();
				} else {
					/*** 若未入過帳，取得每期手續費 寫入當期合約帳單檔 ***/
					hConpCurrPostAmt = hContCltUnitPrice;
					hConpKindAmt = "3";
					hConpInstallCurrTerm = "00";
					insertBilContpost(3);

					if (hContExtraFees > 0) {
						/*** 若有額外費用，再取得額外費用，寫入當期合約帳單檔 ***/
						hConpCurrPostAmt = hContExtraFees;
						hConpKindAmt = "2";
						hConpInstallCurrTerm = "00";
						insertBilContpost(2);
					}

					
					if (hContFirstPostKind.equals("1")) {
						updateBilContract();
					} else {
						updateBilContract(); //次期入帳
						continue;
					}
				}
			}

			/*** 將期數+1 ***/
			hContInstallCurrTerm++;

			/*2023/03/25退貨交易比對成功,將未入帳的部分一次入帳
			//新制分期且已退貨放行，且全部數量皆退貨 
			if ((hContRefundFlag.equals("Y")) && (hContRefundAprFlag.equals("Y")) && (hContRefundQty == hContQty)) {
				// 將期合約檔更新為全部入帳完畢 
				hContAllPostFlag = "Y";
				hContInstallCurrTerm = hContInstallTotTerm;
				if (hContFirstPostKind.equals("1")) {
					updateBilContract1();
				} else {
					updateBilContract();
				}

				continue;
			}
            */
			
			if (hContInstallmentKind.equals("N")) /*** bilm0110郵購分期 ***/
			{
				selectBilMerchant(hContMchtNo);
			} else if (hContInstallmentKind.equals("F")) /*** 簽單分期 ***/
			{
				selectBilBill();
			} else {
				selectBilCurpost();
			}

			/*** 分期產品利率預設0 ***/
			hProdTransRate = 0;
			/*** 若為新制分期 ***/
			if (hContNewItFlag.equals("Y")) {
				/*** 若合約檔的分期利率不為0 或已經有計算過費用年百分率 ***/
				if (hContTransRate != 0  || "".equals(hContYearFeesDate)==false ) {
					/*** 使用合約檔的利率作為產品檔的利率來使用 ***/
					hProdTransRate = hContTransRate;
				} else {
					/*** 若分期交易種類為A自動分期或D指定卡片分期 ***/
					if ((hContInstallmentKind.equals("A")) || (hContInstallmentKind.equals("D"))) {
						/*** 由特店代號與產品編號取得分期利率 ***/
						if (selectBilProd1() != 0) {
							showLogMessage("I", "", String.format("select bil_prod_1 not found error\n"));
							showLogMessage("I","",String.format("mcht_no=%s\n", hContPtrMchtNo));
						}
					}
					/*** 若cps_flag為C即收單註記為NCCC ***/
					else if (hContInstallmentKind.equals("C")) {
						/*** 由特店代號與產品編號及活動期限取得NCCC分期利率 ***/
						if (selectBilProdNccc() != 0) {
							stderr = String.format("select bil_prod_nccc not found error\n");
							showLogMessage("I", "", "select bil_prod_nccc not found error, curr= " + totalCnt +"\n");
							showLogMessage("I", "",String.format("mcht_no[%s],contract_no[%s]\n", hContMchtNo, hContContractNo));
						}
					} else {
						/*** 由特店代號與產品編號取得手續費率最大值與最小值與分期利率 ***/
						if (selectBilProd() != 0) {
							showLogMessage("I", "", "select bil_prod not found error\n");
							showLogMessage("I", "", String.format("mcht_no[%s]\n", hContMchtNo));
						}
					}
				}
			}

			/*** 取得該帳戶下的有效卡數 ***/
			selectCrdCard1();
			/*** 取得該消費卡號的卡片狀態與p_seqno ***/
			selectCrdCard();
			/*** 取得該帳戶的M_code，若>=bil_billunit的OI設定的INDELV_MX末三碼 ***/
			/*** 或該消費卡號狀態碼不為0 ***/
			if (((int1a = comr.getMcode(hCardAcctType, hCardPSeqno)) >= hTempBilBillunitCnt)
					|| (!hCardCurrentCode.equals("0"))) {
				/*** 將剩餘的期數一次到期 ***/
				hContForcedPostFlag = "Y";
				hContCltForcedPostFlag = "Y";
				hContConfirmFlag = "Y";
				
			}
			
			if ((hContRefundFlag.equals("Y")) && (hContRefundAprFlag.equals("Y"))) {
				runTerm = hContInstallTotTerm;  //若收到退貨交易，則入帳迄期 = 總期
			} else if ((hCardCurrentCode.equals("0")) && (!hContForcedPostFlag.equals("Y"))) {
				/*** 若卡片為有效卡且未註記強迫一次入帳，則入帳迄期 = 當期 ***/
				runTerm = hContInstallCurrTerm;      
			} else {
				runTerm = hContInstallTotTerm;  //若卡片失效或註記強迫一次入帳，則入帳迄期 = 總期 
			}

			/*** 指定入帳起期 = 當期 ***/
			tempCurrTerm = hContInstallCurrTerm;

			/*** 依 入帳起期 至 入帳迄期 將該入的期數依序入帳 ***/
			for (int intb = tempCurrTerm; intb <= runTerm; intb++) {
				/*** 取得當前期數給合約檔 ***/
				hContInstallCurrTerm = intb;
				/*** 取得每期金額 ***/
				hConpCurrPostAmt = hContUnitPrice;
				/*** 取得當前期數給當期合約入帳檔 ***/
				tmpstr = String.format("%2d", intb);
				hConpInstallCurrTerm = tmpstr;

				/*** 如果是首期 ***/
				if (intb == 1) {
					/*** 若有首期額外費用且非新制分期交易 ***/
					if ((hContExtraFees > 0) && (!hContNewItFlag.equals("Y"))) {
						/*** 將首期額外費用另外新增至當期合約入帳檔 ***/
						hConpCurrPostAmt = hContExtraFees;
						hConpKindAmt = "2";
						insertBilContpost(7);
					}
					/*** 應付金額應為每期金額再加上首期餘數 ***/
					hConpCurrPostAmt = hContUnitPrice + hContFirstRemdAmt;
				}

				/*** 若為最後一期 ***/
				if (intb == hContInstallTotTerm) {
					/*** 應付金額應為每期金額再加上餘額 ***/
					hConpCurrPostAmt = hContUnitPrice + hContRemdAmt;
					/*** 註記已全部入帳 ***/
					hContAllPostFlag = "Y";
				}
				/*** 1為分期金額 ***/
				hConpKindAmt = "1";

				/*** 若當期入帳金額>0 則寫入當日入帳檔 ***/
				if (hConpCurrPostAmt > 0)
					insertBilContpost(1);

				/*** 當期且為新制分期且總期數>1且分期產品檔分期利率>0 ***/
				if ((intb == tempCurrTerm) && (hContNewItFlag.equals("Y")) && (hContInstallTotTerm > 1)
						&& (hProdTransRate > 0)) {
					/*** 計算剩餘期數金額 ***/
					doubleAmt = hContUnitPrice * (hContInstallTotTerm - hContInstallCurrTerm + 1);
					/*** 若為首期需再加上首期餘額與餘額 ***/
					if (intb == 1)
						doubleAmt = doubleAmt + hContFirstRemdAmt + hContRemdAmt;
					/*** 若為最後一期需再加上餘額 ***/
					if (intb == hContInstallTotTerm)
						doubleAmt = doubleAmt + hContRemdAmt;
					/*** 計算分期利息 將剩餘期數金額 乘以 分期產品分期利率 先除以100 再除以12 ***/
					oldTransRate = hProdTransRate;
					if (newMethod.equals("Y") && hProdTransRate > hAgenRevolvingInterest1) {
						hProdTransRate = hAgenRevolvingInterest1;
					}

					longAmt = (long) (doubleAmt * hProdTransRate / 1200 + 0.5); //TCB四捨五入
					
					//201509利率變換時以日數算,應該是用不到了
					if (newMethod.equals("Y") && hEffcMonth.equals(hBusiBusinessDate.substring(6))) {
						longAmt = (long) (doubleAmt * oldTransRate * (lastMonthDays - hContPostCycleDd) / yearDays
								/ 100);
						longAmt1 = (long) (doubleAmt * hProdTransRate * (hContPostCycleDd) / yearDays / 100);
						longAmt = longAmt + longAmt1;
						
					}

					hConpCurrPostAmt = longAmt;
					/*** 4為分期利息 ***/
					hConpKindAmt = "4";
					/*** 若當期入帳金額>0 則寫入當日入帳檔 ***/
					if (hConpCurrPostAmt > 0)
						insertBilContpost(4);
				}

				/*** 若是舊製分期且客戶手續費每期金額>0 ***/
				if ((hContCltInstallTotTerm >= hContInstallCurrTerm) && (!hContNewItFlag.equals("Y"))
						&& (hContCltUnitPrice > 0)) {
					/*** 取得當期入帳金額 = 客戶手續費每期金額 ***/
					hConpCurrPostAmt = hContCltUnitPrice;
					/*** 若註記為F首期且本次為首期入帳 則當期入帳金額 = 客戶手續費每期金額 + 客戶手續費尾數 ***/
					if ((hContFeeFlag.equals("F")) && (hContInstallCurrTerm == 1))
						hConpCurrPostAmt = hContCltUnitPrice + hContCltRemdAmt;
					/*** 若註記為L尾期且本次為尾期入帳 則當期入帳金額 = 客戶手續費每期金額 + 客戶手續費尾數 ***/
					if ((hContFeeFlag.equals("L")) && (hContInstallCurrTerm == hContInstallTotTerm))
						hConpCurrPostAmt = hContCltUnitPrice + hContCltRemdAmt;

					/*** 註記為3手續費用 ***/
					hConpKindAmt = "3";
					insertBilContpost(8);
				}
			}
			if (hContFirstPostKind.equals("1")) {
				updateBilContract();
			} else {
				updateBilContract();
			}
		}

		closeCursor();
	}

	/***********************************************************************/
	void selectBilBill() throws Exception {
		sqlCmd += " select decode(decode(source_curr, 'TWD', '901', source_curr), '901', decode(substr(decode(mcht_country,'', 'TW',mcht_country), 1, 2), 'TW', decode(trim(mcht_chi_name), '', mcht_eng_name, decode(substrb(mcht_chi_name, 1, 4), '　　', mcht_eng_name, mcht_chi_name)), decode(trim(mcht_eng_name), '', mcht_chi_name, mcht_eng_name)), decode(trim(mcht_eng_name), '', mcht_chi_name, mcht_eng_name)) as h_cont_mcht_chi_name,";
		sqlCmd += "        mcht_eng_name,  ";
		sqlCmd += "        mcht_city, ";
		sqlCmd += "        mcht_country,  ";
		sqlCmd += "        mcht_category, ";
		sqlCmd += "        mcht_zip, ";
		sqlCmd += "        mcht_state ";
		sqlCmd += " from   bil_bill ";
		sqlCmd += " where  reference_no = ? ";
		setString(1, hContReferenceNo);
		if (selectTable() > 0) {
			hContMchtChiName = getValue("h_cont_mcht_chi_name");
			hConpMchtEngName = getValue("mcht_eng_name");
			hConpMchtCity = getValue("mcht_city");
			hConpMchtCountry = getValue("mcht_country");
			hConpMchtCategory = getValue("mcht_category");
			hConpMchtZip = getValue("mcht_zip");
			hConpMchtState = getValue("mcht_state");
		}

	}

	/***********************************************************************/
	int selectBilMerchant(String iMchtNo) throws Exception {
		hMercMchtType = "";
		hMercInstallmentDelay = "Y";
		hStmtInstFlag = "";

		sqlCmd = "select mcht_type,";
		sqlCmd += "decode(trim(mcht_chi_name),'',mcht_eng_name, decode(substrb(mcht_chi_name,1,4),'　　',mcht_eng_name, mcht_chi_name)) h_cont_mcht_chi_name,";
		sqlCmd += "mcht_eng_name,";
		sqlCmd += "mcht_city,";
		sqlCmd += "mcht_country,";
		sqlCmd += "mcc_code,";
		sqlCmd += "mcht_zip,";
		sqlCmd += "mcht_state,";
		sqlCmd += "stmt_inst_flag,";
		sqlCmd += "decode(installment_delay,'','Y',installment_delay) h_merc_installment_delay ";
		sqlCmd += " from bil_merchant  ";
		sqlCmd += "where mcht_no = ? ";
		setString(1, iMchtNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hMercMchtType = getValue("mcht_type");
			hContMchtChiName = getValue("h_cont_mcht_chi_name");
			hConpMchtEngName = getValue("mcht_eng_name");
			hConpMchtCity = getValue("mcht_city");
			hConpMchtCountry = getValue("mcht_country");
			hConpMchtCategory = getValue("mcc_code");
			hConpMchtZip = getValue("mcht_zip");
			hConpMchtState = getValue("mcht_state");
			hStmtInstFlag = getValue("stmt_inst_flag");
			hMercInstallmentDelay = getValue("h_merc_installment_delay");
		
		} else {
			
			return 1;
		}

		return 0;
	}

	/***********************************************************************/
	void insertBilContpost(int idx) throws Exception {
		
		//若金額<=0則不寫資料
		if ((hConpCurrPostAmt > 0)==false) {
			return;
		}
		
		switch (comcr.str2int(hConpKindAmt)) {
		case 1:
			if ((hContContractKind.equals("2")) && (hConpKindAmt.equals("1")))
				tmpstr = String.format("%s(%s)", comc.fixLeft(hContMchtChiName, 18),
						comc.fixLeft(hContProductName, 20));

			if ((hContContractKind.equals("1")) && (hConpKindAmt.equals("1"))) {
				tmpstr = String.format("%s(%s%10.0f)分%2d期第%2.2s期", comc.fixLeft(hContMchtChiName, 8),
						comc.fixLeft(hContProductName, 8), hCurpPostAmt, hContInstallTotTerm, hConpInstallCurrTerm);

				if (hContInstallTotTerm == 1)
					tmpstr = String.format("%s", comc.fixLeft(hContMchtChiName, 40));
			}
			break;
		case 2:
			tmpstr = String.format("%s(%s%10.0f)額外費用", comc.fixLeft(hContMchtChiName, 12),
					comc.fixLeft(hContProductName, 8), hCurpPostAmt);
			break;
		case 3:
			if (hContCltInstallTotTerm > 1) {
				tmpstr = String.format("%s(%s%10.0f)分%2d期第%2.2s期費用", comc.fixLeft(hContMchtChiName, 8),
						comc.fixLeft(hContProductName, 4), hCurpPostAmt, hContInstallTotTerm, hConpInstallCurrTerm);
			} else {
				tmpstr = String.format("%s(%s%10.0f)手續費", comc.fixLeft(hContMchtChiName, 14),
						comc.fixLeft(hContProductName, 8), hCurpPostAmt);
			}
			break;
		case 4:
			tmpstr = String.format("%s(%s%10.0f)分%2d期第%2.2s期利息", comc.fixLeft(hContMchtChiName, 8),
					comc.fixLeft(hContProductName, 4), hCurpPostAmt, hContInstallTotTerm, hConpInstallCurrTerm);
			break;
		case 5:
			tmpstr = String.format("%s(%s%10.0f)分%2d期第%2.2s期利息", comc.fixLeft(hContMchtChiName, 8),
					comc.fixLeft(hContProductName, 4), hCurpPostAmt, hContInstallTotTerm, hConpInstallCurrTerm);
			break;
		}
		hConpMchtChiName = tmpstr;

		setValue("contract_no", hContContractNo);
		setValue("contract_seq_no", hContContractSeqNo);
		setValue("card_no", hContRealCardNo);
		setValue("product_no", hContProductNo);
		setValue("product_name", hContProductName);
		setValue("mcht_no", hContMchtNo);
		setValue("mcht_chi_name", hConpMchtChiName);
		setValue("mcht_eng_name", hConpMchtEngName);
		setValueDouble("curr_post_amt", hConpCurrPostAmt);
		if (hConpInstallCurrTerm.trim().equals(""))
			hConpInstallCurrTerm = "0";
		int tmpInt = Integer.parseInt(hConpInstallCurrTerm.trim());
		setValueInt("install_curr_term", tmpInt);
		setValue("limit_end_date", hContLimitEndDate);
		setValue("kind_amt", hConpKindAmt);
		setValue("post_flag", "T");
		setValue("mcht_city", hConpMchtCity);
		setValue("mcht_country", hConpMchtCountry);
		setValue("mcht_category", hConpMchtCategory);
		setValue("mcht_zip", hConpMchtZip);
		setValue("mcht_state", hConpMchtState);
		setValue("new_it_flag", hContNewItFlag);
		setValue("cps_flag", hContCpsFlag);
		setValue("contract_kind", hContContractKind);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		daoTable = "bil_contpost";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert bil_contpost duplicate", "", hContContractNo);
		}
	}

	/**********************************************************************/
	void updateBilContract() throws Exception {

		daoTable = "bil_contract";
		updateSQL = " install_curr_term    = ?,";
		updateSQL += " post_cycle_dd        = decode(post_cycle_dd,0,?,post_cycle_dd),";
		updateSQL += " forced_post_flag     = ?,";
		updateSQL += " clt_forced_post_flag = ?,";
		updateSQL += " apr_flag             = ?,";
		updateSQL += " all_post_flag        = ?,";
		updateSQL += " first_post_date      = ?,";
		updateSQL += " new_proc_date     = decode(new_proc_date,'',?,new_proc_date),";
		updateSQL += " last_update_date  = ?,";
		updateSQL += " new_it_flag       = ?,";
		updateSQL += " first_post_kind   = ?,";
		updateSQL += " mod_pgm           = ?,";
		updateSQL += " mod_time          = sysdate";
		whereStr = "where contract_no  = ?  and  contract_seq_no = ? ";
		setLong(1, hContInstallCurrTerm);
		setInt(2, hContPostCycleDd);
		setString(3, hContForcedPostFlag);
		setString(4, hContCltForcedPostFlag);
		setString(5, hContConfirmFlag);
		setString(6, hContAllPostFlag);
		
		if (hContFirstPostDate.length() != 0) {
			setString(7, hContFirstPostDate);
		} else {
			
			if ("1".equals(hContFirstPostKind)) {
			setString(7, hBusiBusinessDate);  //當期入帳
			} else {
			setString(7, hTempBusinessDate);  //次期入帳
			}
		}
		setString(8, hBusiBusinessDate);
		setString(9, hBusiBusinessDate);
		setString(10, hContNewItFlag);
		setString(11, hContFirstPostKind);
		setString(12, prgmId);
		setString(13, hContContractNo);
		setString(14, hContContractSeqNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update bil_contract not found!", "", hContContractNo);
		}
	}

	/***********************************************************************/
	void selectBilCurpost() throws Exception {
		sqlCmd = "select decode(decode(source_curr,'TWD','901',source_curr), "
				+ "'901',decode(substr(decode(mcht_country,'','TW',mcht_country),1,2), "
				+ "'TW',decode(trim(mcht_chi_name),''," + "mcht_eng_name, decode(substrb(mcht_chi_name,1,4),'　　',"
				+ "mcht_eng_name, mcht_chi_name)), " + "decode(trim(mcht_eng_name),'',mcht_chi_name,mcht_eng_name)), "
				+ "decode(trim(mcht_eng_name),'',mcht_chi_name,mcht_eng_name)) h_cont_mcht_chi_name,";
		sqlCmd += "mcht_eng_name,";
		sqlCmd += "mcht_city,";
		sqlCmd += "mcht_country,";
		sqlCmd += "mcht_category,";
		sqlCmd += "mcht_zip,";
		sqlCmd += "mcht_state ";
		sqlCmd += " from bil_curpost  ";
		sqlCmd += "where reference_no = ? ";
		setString(1, hContReferenceNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hContMchtChiName = getValue("h_cont_mcht_chi_name");
			hConpMchtEngName = getValue("mcht_eng_name");
			hConpMchtCity = getValue("mcht_city");
			hConpMchtCountry = getValue("mcht_country");
			hConpMchtCategory = getValue("mcht_category");
			hConpMchtZip = getValue("mcht_zip");
			hConpMchtState = getValue("mcht_state");
		}

	}

	/***********************************************************************/
	int selectBilProd1() throws Exception {

		hProdFeesMinAmt = 0;
		hProdFeesMaxAmt = 0;
		hProdTransRate = 0;

		sqlCmd = "select trans_rate, ";
		sqlCmd += "      fees_min_amt,  ";
		sqlCmd += "      fees_max_amt ";
		sqlCmd += " from bil_prod  ";
		sqlCmd += "where mcht_no    = ?  ";
		sqlCmd += "  and product_no = ?  ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, hContPtrMchtNo);
		setString(2, hContProductNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hProdTransRate = getValueDouble("trans_rate");
			hProdFeesMinAmt = getValueDouble("fees_min_amt");
			hProdFeesMaxAmt = getValueDouble("fees_max_amt");
		} else {
			return 1;
		}

		return 0;
	}

	/**********************************************************************/
	int selectBilProdNccc() throws Exception {
		hProdTransRate = 0;
		sqlCmd = "select trans_rate ";
		sqlCmd += " from bil_prod_nccc  ";
		sqlCmd += "where mcht_no     = ?  ";
		sqlCmd += "  and product_no  = ?  ";
		sqlCmd += "  and start_date <= ?  ";
		sqlCmd += "  and end_date   >= ? ";
		setString(1, hContMchtNo);
		setString(2, hContProductNo);
		setString(3, hContPurchaseDate);
		setString(4, hContPurchaseDate);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hProdTransRate = getValueDouble("trans_rate");
		} else {
			return 1;
		}

		return 0;
	}

	/**********************************************************************/
	int selectBilProd() throws Exception {
		hProdFeesMinAmt = 0;
		hProdFeesMaxAmt = 0;
		hProdTransRate = 0;
		sqlCmd = "select fees_min_amt ,";
		sqlCmd += "fees_max_amt ,";
		sqlCmd += "trans_rate ";
		sqlCmd += " from bil_prod  ";
		sqlCmd += "where mcht_no = ?  ";
		sqlCmd += "and product_no = ?  ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, hContMchtNo);
		setString(2, hContProductNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hProdFeesMinAmt = getValueDouble("fees_min_amt");
			hProdFeesMaxAmt = getValueDouble("fees_max_amt");
			hProdTransRate = getValueDouble("trans_rate");
		} else {
			return 1;
		}

		return 0;
	}

	/***********************************************************************/
	void selectCrdCard1() throws Exception {
		crdCardCnt = 0;
		sqlCmd = "select count(*) crd_card_cnt ";
		sqlCmd += " from crd_card  ";
		sqlCmd += "where acct_type     = ?  ";
		sqlCmd += "  and acno_p_seqno  = ?  ";
		sqlCmd += "  and (current_code = '0' or reissue_status in ('1','2'))  ";
		setString(1, hContAcctType);
		setString(2, hContPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select crd_card not found!", "", hContPSeqno);
		}
		if (recordCnt > 0) {
			crdCardCnt = getValueInt("crd_card_cnt");
		}
		
	}

	/***********************************************************************/
	void selectCrdCard() throws Exception {
		hCardCurrentCode = "";
		hCardPSeqno = "";
		hCardAcctType = "";

		sqlCmd = "select (case when current_code = '5' and ? = 0 then '1' else '0' end) as h_card_current_code,";
		sqlCmd += "acct_type,p_seqno "; 
		sqlCmd += " from crd_card  ";
		sqlCmd += "where card_no = ? ";
		setInt(1, crdCardCnt);
		setString(2, hContRealCardNo);
		int recordCnt = selectTable();
		
		if (recordCnt > 0) {
			
			//不在這支程式做一次到期的判斷
			//hCardCurrentCode = getValue("h_card_current_code");
			hCardCurrentCode = "0";
			hCardAcctType = getValue("acct_type");
			hCardPSeqno = getValue("p_seqno");
		}

	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		BilD003 proc = new BilD003();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
	/***********************************************************************/
}
