/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00  Edson       program initial                           *
*  109/04/10  V1.01.00  Lai         BECS-1070126-012 簽單分期merchant_chi_name*
*  109/11/26  V1.00.01  shiyuqi     updated for project coding standard       * 
*  109/12/01  V1.00.02  JeffKung    updated for TCB                           *
*  109/12/30  V1.00.03  Zuwei       “兆豐國際商銀”改為”合作金庫銀行”           *
*  111/09/22  V1.00.04  JeffKung    updated for TCB                           *
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

/*長期循環轉帳單分期付款產生當期暫存檔處理 */
public class BilD005 extends AccessDAO {
    private String progname = "長期循環轉帳單分期付款產生當期暫存檔處理   111/09/22 V1.00.04 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;

    String prgmId = "BilD005";
    String prgmName = "長期循環轉帳單分期付款產生當期暫存檔處理";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hEffcMonth = "";
    int lastMonthDays = 0;
    int yearDays = 0;
    String hEffcMonthNext = "";
    String hBusiBusinessDate = "";
    String hTempPrevYyyymm = "";
    String hTempThisYyyymm = "";
    String hTempNextYyyymm = "";
    String hWdayStmtCycle = "";
    String hWdayThisDelaypayDate = "";
    int hTempBilBillunitCnt = 0;
    String hTempContractNo = "";
    String hContContractNo = "";
    String hContContractSeqNo = "";
    String hContContractKind = "";
    String hContAcctKey = "";
    String hContCardNo = "";
    String hContAcctType = "";
    String hContPSeqno = "";
    String hContProductNo = "";
    String hContProductName = "";
    String hContMchtNo = "";
    String hContPtrMchtNo = "";
    String hContMchtChiName = "";
    String hContMchtEngName = "";
    String hContQty = "";
    String hContRefundQty = "";
    double hContTotAmt = 0;
    int hContInstallTotTerm = 0;
    double hContRemdAmt = 0;
    double hContFirstRemdAmt = 0;
    String hContAllPostFlag = "";
    String hContRefundFlag = "";
    String hContRefundAprFlag = "";
    String hContAprFlag = "";
    String hContLimitEndDate = "";
    double hContCltFeesAmt = 0;
    double hContCltUnitPrice = 0;
    double hContCltRemdAmt = 0;
    String hContCltForcedPostFlag = "";
    String hContForcedPostFlag = "";
    String hContFeeFlag = "";
    String hContReferenceNo = "";
    String hContInstallmentKind = "";
    String hContNewProcDate = "";
    String hContNewItFlag = "";
    String hContCpsFlag = "";
    String hContPurchaseDate = "";
    String hContFirstPostDate = "";
    String hContModPgm = "";
    String hContRowid = "";
    String hMercMchtType = "";
    String hConpMchtEngName = "";
    String hConpMchtCity = "";
    String hConpMchtCountry = "";
    String hConpMchtCategory = "";
    String hConpMchtZip = "";
    String hConpMchtState = "";
    int crdCardCnt = 0;
    String hCardCurrentCode = "";
    int hContInstallCurrTerm = 0;
    int hTempPostCycleDd = 0;
    String hConpMchtChiName = "";
    double hConpCurrPostAmt = 0;
    String hConpInstallCurrTerm = "";
    String hTempLastMinPayDate = "";
    String hPrintName = "";
    String hRptName = "";
    String hJrnlInterestDate = "";
    double doubleAmt1 = 0;
    String hBillPostDate = "";
    String hBillReferenceNo = "";
    String hConpKindAmt = "";
    double hProdTransRate = 0;
    double hContTransRate = 0;
    double hDebtEndBal = 0;
    double hDebtBegBal = 0;
    double hAgenRevolvingInterest1 = 0;
    double oldTransRate = 0;
    int hContCltInstallTotTerm = 0;
    int hContPostCycleDd = 0;
    double hContUnitPrice = 0;
    double hContExtraFees = 0;
    String hCardAcctPSeqno = "";
    String hWdayThisInterestDate = "";
    String hWdayNextInterestDate = "";
    String hWdayNextAcctMonth = "";
    String hAcnoNewCycleMonth = "";
    String hAcnoLastInterestDate = "";
    double hCurpPostAmt = 0;

    String rateEffcMonth = "201509";
    int r1Cnt = 0;
    int pageCntR1 = 0;
    int totalCnt = 0;
    int lineCnt = 0;
    int tmpRcSDd = 0;
    int tmpItemPostDd = 0;
    int oldDays = 0;
    int runTerm = 0;
    int hTempMinDdBeg = 0;
    int hTempMinDdEnd = 0;
    double doubleAmt2 = 0;
    long tempRcDays1 = 0;
    String newMethod = "";
    String swNew = "";
    String tmpstr = "";
    String tmprcsdate = "";
    String tmpRcEDate = "";
    String hTempForcedPostFlag = "";
    private String hCardAcctType = "";

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
                comc.errExit("Usage : BilD005 [[business_date] [contract_no]]", "");
            }

            // 固定要做的

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
            if (selectPtrWorkday() != 0) {
                /*** 本程式需在BilD003前之關帳日執行 ***/
                showLogMessage("I", "",String.format("Today is not cycle date[%s],執行結束 \n", hBusiBusinessDate));
                return 0;
            }

            hTempPostCycleDd = comcr.str2int(hWdayStmtCycle);

            selectPtrActgeneral();
            hEffcMonth = rateEffcMonth;
            hEffcMonthNext = "";
            sqlCmd = "select to_number(to_char(last_day(add_months(to_date(?,'yyyymm'),-1)),'dd')) as last_month_days,";
            sqlCmd += "to_number(to_char(to_date((substr(?,1,4) || '1231'),'yyyymmdd'),'DDD')) year_days,";
            sqlCmd += "to_char(add_months(to_date(? ,'yyyymm'),1),'yyyymm') h_effc_month_next ";
            sqlCmd += " from dual ";
            setString(1, hEffcMonth);
            setString(2, hEffcMonth);
            setString(3, hEffcMonth);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                lastMonthDays = getValueInt("last_month_days");
                yearDays = getValueInt("year_days");
                hEffcMonthNext = getValue("h_effc_month_next");
            }
            yearDays = 365;
            newMethod = "N";
            if (hEffcMonth.compareTo(hBusiBusinessDate.substring(0, 6)) <= 0)
                newMethod = "Y";

            showLogMessage("I", "", String.format(" Process month=[%s] last_m_days=[%d] [%s][%d] [%d]\n"
                    , rateEffcMonth, lastMonthDays, hBusiBusinessDate
                    , hTempPostCycleDd, yearDays));

            selectBilBillunit();

            totalCnt = 0;
            selectBilContract();

            showLogMessage("I", "", String.format("RC合約檔處理筆數 [%d]\n", totalCnt));
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
        sqlCmd = "select decode(cast(? as varchar(10)),'',business_date,?) as h_busi_business_date,";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(10)),'',business_date,?),'yyyymmdd'),-1),'yyyymm') h_temp_prev_yyyymm,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(10)),'',business_date,?),'yyyymmdd'),'yyyymm') h_temp_this_yyyymm,";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(10)),'',business_date,?),'yyyymmdd'),1),'yyyymm') h_temp_next_yyyymm ";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        setString(5, hBusiBusinessDate);
        setString(6, hBusiBusinessDate);
        setString(7, hBusiBusinessDate);
        setString(8, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hTempPrevYyyymm = getValue("h_temp_prev_yyyymm");
            hTempThisYyyymm = getValue("h_temp_this_yyyymm");
            hTempNextYyyymm = getValue("h_temp_next_yyyymm");
        } else {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int selectPtrWorkday() throws Exception {
        hWdayStmtCycle = "";
        sqlCmd = "select stmt_cycle, ";
        sqlCmd += "       this_delaypay_date, ";
        sqlCmd += "       this_interest_date, ";
        sqlCmd += "       next_interest_date, ";
        sqlCmd += "       next_acct_month, ";
        sqlCmd += "       this_delaypay_date ";
        sqlCmd += " from ptr_workday  ";
        sqlCmd += "where next_close_date = ? ";
        setString(1, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hWdayStmtCycle = getValue("stmt_cycle");
            hWdayThisDelaypayDate = getValue("this_delaypay_date");
            hWdayThisInterestDate = getValue("this_interest_date");
            hWdayNextInterestDate = getValue("next_interest_date");
            hWdayNextAcctMonth = getValue("next_acct_month");
            hWdayThisDelaypayDate = getValue("this_delaypay_date");
        } else {
            return 1;
        }

        return 0;
    }

    /***********************************************************************/
    int selectPtrActgeneral() throws Exception {
        sqlCmd = "select max(round(revolving_interest1*365/100,2)) h_agen_revolving_interest1 ";
        sqlCmd += " from ptr_actgeneral_n ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAgenRevolvingInterest1 = getValueDouble("h_agen_revolving_interest1");
        } else {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
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
            comcr.errRtn("select_ptr_billunit not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempBilBillunitCnt = getValueInt("h_temp_bil_billunit_cnt");
        }

    }

    /***********************************************************************/
    int selectBilMerchant() throws Exception {
        hMercMchtType = "";
        sqlCmd = "select mcht_type,";
        sqlCmd += "decode(trim(mcht_chi_name),'',mcht_eng_name, ";
        sqlCmd += "decode(substrb(mcht_chi_name,1,4),'　　',mcht_eng_name, mcht_chi_name)) h_cont_mcht_chi_name,";
        sqlCmd += "mcht_eng_name,";
        sqlCmd += "mcht_city,";
        sqlCmd += "mcht_country,";
        sqlCmd += "mcc_code,";
        sqlCmd += "mcht_zip,";
        sqlCmd += "mcht_state ";
        sqlCmd += " from bil_merchant  ";
        sqlCmd += "where mcht_no = ? ";
        setString(1, hContMchtNo);
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
        } else {
            return 1;
        }
        return 0;
    }

    /**********************************************************************/
    void selectCrdCard() throws Exception {
        hCardCurrentCode = "";
        hCardAcctType = "";
        hCardAcctPSeqno = "";

        //偽冒(5)沒有其他卡視為正常卡
        sqlCmd = "select (case when current_code = '5' and ? = 0 then '1' else '0' end) as h_card_current_code,";
        sqlCmd += "acct_type, p_seqno as acct_p_seqno ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setInt(1, crdCardCnt);
        setString(2, hContCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardCurrentCode = getValue("h_card_current_code");
            hCardAcctType = getValue("acct_type");
            hCardAcctPSeqno = getValue("acct_p_seqno");
        }

    }

    /**********************************************************************/
    int selectBilProd() throws Exception {
        sqlCmd = "select trans_rate ";
        sqlCmd += " from bil_prod  ";
        sqlCmd += "where mcht_no = ?  ";
        sqlCmd += "  and product_no = decode(cast(? as varchar(10)),'0',lpad(?,8,'0'), ?)  ";
        setString(1, hContMchtNo);
        setString(2, hMercMchtType);
        setString(3, hContProductNo);
        setString(4, hContProductNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hProdTransRate = getValueDouble("trans_rate");
        } else {
            return 1;
        }
        return 0;
    }

    /***********************************************************************/
    void selectActDebt() throws Exception {
        /*** 取得未平帳的期末餘額總和 ***/
        hDebtEndBal = 0;
        sqlCmd = "select sum(end_bal) h_debt_end_bal ";
        sqlCmd += " from act_debt  ";
        sqlCmd += "where p_seqno         = ?  ";
        sqlCmd += "  and acct_type       = ?  ";
        sqlCmd += "  and ACCT_CODE       = 'IT'  ";
        sqlCmd += "  and contract_no     = ?  ";
        sqlCmd += "  and contract_seq_no = ? ";
        setString(1, hCardAcctPSeqno);
        setString(2, hContAcctType);
        setString(3, hContContractNo);
        setString(4, hContContractSeqNo);
        
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_debt not found!", "", hContContractNo);
        }
        
        if (recordCnt > 0) {
            hDebtEndBal = getValueDouble("h_debt_end_bal");
        }

    }

    /***********************************************************************/
    void selectBilBill() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "post_date,";
        sqlCmd += "reference_no ";
        sqlCmd += "from bil_bill ";
        sqlCmd += "where p_seqno     = ? ";
        sqlCmd += "  and acct_type   = ? ";
        sqlCmd += "  and bill_type   = 'OICU' ";
        sqlCmd += "  and txn_code    = 'IN' ";
        sqlCmd += "  and contract_no = ? ";
        sqlCmd += "order by post_date,reference_no ";
        setString(1, hCardAcctPSeqno);
        setString(2, hContAcctType);
        setString(3, hContContractNo);
        int recordCnt = selectTable();
        
        for (int i = 0; i < recordCnt; i++) {
            hBillPostDate = getValue("post_date", i);
            hBillReferenceNo = getValue("reference_no", i);

            sqlCmd = "select min(interest_date) h_jrnl_interest_date ";
            sqlCmd += " from act_jrnl  ";
            sqlCmd += "where acct_date = (select max(ACCT_DATE) from act_jrnl where p_seqno  = ?  ";
            sqlCmd += "  and reference_no = ?  ";
            sqlCmd += "  and tran_class   = 'D'  ";
            sqlCmd += "  and tran_type    = 'DEBT'  ";
            sqlCmd += "  and acct_code    = 'IT'  ";
            sqlCmd += "  and dr_cr        = 'D'  ";
            sqlCmd += "  and item_bal     = 0)  ";
            sqlCmd += "  and p_seqno      = ?  ";
            sqlCmd += "  and tran_class   = 'P' ";
            setString(1, hCardAcctPSeqno);
            setString(2, hBillReferenceNo);
            setString(3, hCardAcctPSeqno);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hJrnlInterestDate = getValue("h_jrnl_interest_date");
            }
        }
    }

    /**********************************************************************/
    /***
     * 取得已平帳的最新一期的期初餘額
     * 
     * @throws Exception
     */
    void selectLastActDebt() throws Exception {
        hDebtBegBal = 0;
        sqlCmd = "select nvl(sum(beg_bal),0) h_debt_beg_bal ";
        sqlCmd += " from (select sum(beg_bal) as beg_bal from act_debt  ";
        sqlCmd += "where p_seqno         = ?  ";
        sqlCmd += "  and acct_type       = ?  ";
        sqlCmd += "  and acct_code       = 'IT'  ";
        sqlCmd += "  and contract_no     = ?  ";
        sqlCmd += "  and contract_seq_no = ?  ";
        sqlCmd += "  and end_bal = 0 union select nvl(sum(beg_bal),0) as beg_bal from act_debt_hst a ,act_jrnl b where a.p_seqno  = ?  ";
        sqlCmd += "  and a.acct_type     = ?  ";
        sqlCmd += "  and b.p_seqno       = a.p_seqno  ";
        sqlCmd += "  and a.reference_no  = b.reference_no  ";
        sqlCmd += "  and a.acct_code     = 'IT'  ";
        sqlCmd += "  and contract_no     = ?  ";
        sqlCmd += "  and contract_seq_no = ?  ";
        sqlCmd += "  and b.acct_date > ?||?  ";
        sqlCmd += "  and b.acct_date <=?||?  ";
        sqlCmd += "  and tran_class  = 'D'  ";
        sqlCmd += "  and tran_type   = 'DEBT'  ";
        sqlCmd += "  and b.acct_code = 'IT'  ";
        sqlCmd += "  and dr_cr       = 'D'  ";
        sqlCmd += "  and item_bal    = 0 ) ";
        setString(1, hCardAcctPSeqno);
        setString(2, hContAcctType);
        setString(3, hContContractNo);
        setString(4, hContContractSeqNo);
        setString(5, hCardAcctPSeqno);
        setString(6, hContAcctType);
        setString(7, hContContractNo);
        setString(8, hContContractSeqNo);
        setString(9, hTempPrevYyyymm);
        setString(10, hWdayStmtCycle);
        setString(11, hTempThisYyyymm);
        setString(12, hWdayStmtCycle);
        int recordCnt = selectTable();
  
        if (notFound.equals("Y")) {
            comcr.errRtn("select_last_act_debt() not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hDebtBegBal = getValueDouble("h_debt_beg_bal");
        }
        
    }

	/**********************************************************************/
	void insertBilContpost(int idx) throws Exception {
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
		setValue("card_no", hContCardNo);
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
			comcr.errRtn("insert_bil_contpost duplicate", "", hContContractNo);
		}
	}
/***********************************************************************/
    void selectBilContract() throws Exception {
        int int1a, tempCurrTerm, tempRcDays1, tempRcDays2;

        long longAmt1, lonAmt2;
        double tempDouble;
        double tempDouble1;

        sqlCmd = "select ";
        sqlCmd += "contract_no,";
        sqlCmd += "contract_seq_no,";
        sqlCmd += "contract_kind,";
        sqlCmd += "card_no,";
        sqlCmd += "b.acct_type,";
        sqlCmd += "a.acct_key,";
        sqlCmd += "b.p_seqno,";
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
        sqlCmd += "decode(b.apr_flag,'','N',b.apr_flag) h_cont_apr_flag,";
        sqlCmd += "limit_end_date,";
        sqlCmd += "clt_fees_amt,";
        sqlCmd += "clt_unit_price,";
        sqlCmd += "clt_install_tot_term,";
        sqlCmd += "clt_remd_amt,";
        sqlCmd += "decode(clt_forced_post_flag,'','N',clt_forced_post_flag) h_cont_clt_forced_post_flag,";
        sqlCmd += "decode(forced_post_flag,'','N',forced_post_flag) h_cont_forced_post_flag,";
        sqlCmd += "decode(fee_flag,'','L',fee_flag) h_cont_fee_flag,";
        sqlCmd += "reference_no,";
        sqlCmd += "decode(installment_kind,'','N',installment_kind) h_cont_installment_kind,";
        sqlCmd += "new_proc_date,";
        sqlCmd += "decode(install_tot_term,1,'N',decode(new_it_flag,'','N',new_it_flag)) h_cont_new_it_flag,";
        sqlCmd += "cps_flag,";
        sqlCmd += "purchase_date,";
        sqlCmd += "first_post_date,";
        sqlCmd += "trans_rate,";
        sqlCmd += "b.mod_pgm,";
        sqlCmd += "post_cycle_dd,";
        sqlCmd += "b.rowid  as rowid, ";
        sqlCmd += "new_cycle_month, ";
        sqlCmd += "last_interest_date ";
        sqlCmd += " from bil_contract b, act_acno a ";
        sqlCmd += "where contract_kind = '1' ";
        sqlCmd += "  and b.acct_type = a.acct_type ";
        sqlCmd += "  and b.acno_p_seqno = a.acno_p_seqno ";
        sqlCmd += "  and ((install_curr_term = 0 ";
        sqlCmd += "  and decode(auto_delv_flag,'','N',auto_delv_flag) = 'Y' ";
        sqlCmd += "  and post_cycle_dd = 0 ";
        sqlCmd += "  and b.apr_date != '') ";
        sqlCmd += "  or (install_curr_term = 0 ";
        sqlCmd += "  and decode(auto_delv_flag,'','N',auto_delv_flag) != 'Y' ";
        sqlCmd += "  and delv_date  != '' ";
        sqlCmd += "  and post_cycle_dd = 0 ";
        sqlCmd += "  and delv_confirm_date != '') ";
        sqlCmd += "  or (decode(all_post_flag,'','N',all_post_flag)!= 'Y' ";
        sqlCmd += "  and install_curr_term != install_tot_term )) ";
        sqlCmd += "  and contract_no = decode(cast(? as varchar(10)),'',contract_no,?) ";
        sqlCmd += "  and decode(mcht_no,'',' ',mcht_no) in (select mcht_no ";
        sqlCmd += " from bil_merchant ";
        sqlCmd += "where decode(trans_flag,'','N',trans_flag) in ('y','Y')) ";
        sqlCmd += "  and a.stmt_cycle = ?  ";
        sqlCmd += "  and decode(last_update_date,'','20000101',last_update_date) < ? ";
        setString(1, hTempContractNo);
        setString(2, hTempContractNo);
        setString(3, hWdayStmtCycle);
        setString(4, hBusiBusinessDate);

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hContContractNo = getValue("contract_no");
            hContContractSeqNo = getValue("contract_seq_no");
            hContContractKind = getValue("contract_kind");
            hContCardNo = getValue("card_no");
            hContAcctType = getValue("acct_type");
            hContAcctKey = getValue("acct_key");
            hContPSeqno = getValue("p_seqno");
            hContProductNo = getValue("product_no");
            hContProductName = getValue("product_name");
            hContMchtNo = getValue("mcht_no");
            hContPtrMchtNo = getValue("ptr_mcht_no");
            hContMchtChiName = getValue("mcht_chi_name");
            hContMchtEngName = getValue("mcht_eng_name");
            hContUnitPrice = getValueDouble("unit_price");
            hContQty = getValue("qty");
            hContRefundQty = getValue("refund_qty");
            hContTotAmt = getValueDouble("tot_amt");
            hCurpPostAmt = getValueDouble("h_curp_post_amt");
            hContInstallTotTerm = getValueInt("install_tot_term");
            hContRemdAmt = getValueDouble("remd_amt");
            hContFirstRemdAmt = getValueDouble("first_remd_amt");
            hContExtraFees = getValueDouble("extra_fees");
            hContInstallCurrTerm = getValueInt("install_curr_term");
            hContAllPostFlag = getValue("all_post_flag");
            hContRefundFlag = getValue("refund_flag");
            hContRefundAprFlag = getValue("refund_apr_flag");
            hContAprFlag = getValue("h_cont_apr_flag");
            hContLimitEndDate = getValue("limit_end_date");
            hContCltFeesAmt = getValueDouble("clt_fees_amt");
            hContCltUnitPrice = getValueDouble("clt_unit_price");
            hContCltInstallTotTerm = getValueInt("clt_install_tot_term");
            hContCltRemdAmt = getValueDouble("clt_remd_amt");
            hContCltForcedPostFlag = getValue("h_cont_clt_forced_post_flag");
            hContForcedPostFlag = getValue("h_cont_forced_post_flag");
            hContFeeFlag = getValue("h_cont_fee_flag");
            hContReferenceNo = getValue("reference_no");
            hContInstallmentKind = getValue("h_cont_installment_kind");
            hContNewProcDate = getValue("new_proc_date");
            hContNewItFlag = getValue("h_cont_new_it_flag");
            hContCpsFlag = getValue("cps_flag");
            hContPurchaseDate = getValue("purchase_date");
            hContFirstPostDate = getValue("first_post_date");
            hContTransRate = getValueDouble("trans_rate");
            hContModPgm = getValue("mod_pgm");
            hContPostCycleDd = getValueInt("post_cycle_dd");
            hContRowid = getValue("rowid");
            hAcnoNewCycleMonth = getValue("new_cycle_month");
            hAcnoLastInterestDate = getValue("last_interest_date");

            totalCnt++;
            if (totalCnt % 5000 == 0 || totalCnt == 1)
                showLogMessage("D", "", "Current Process record=" + totalCnt);
            
            hContInstallCurrTerm++;

            swNew = "N";
            if ((hContInstallCurrTerm == 1) || (hContPostCycleDd == 0)) {
                hContPostCycleDd = comcr.str2int(hContPurchaseDate.substring(6));
            }

            /*** 若無法查詢到交易特店資料 ***/
            if (selectBilMerchant() != 0) {
                stderr = String.format("select bil_merchant not found error\n");
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }

            /*** 取得該帳戶下的有效卡數 ***/
            selectCrdCard1();

            /*** 取得該消費卡號的卡片狀態與acct_p_seqno ***/
            selectCrdCard();

            hTempForcedPostFlag = hContForcedPostFlag;

            int1a = comr.getMcode(hCardAcctType, hCardAcctPSeqno);
            
            /*TCB點掉一次到期的判斷
            if ((int1a >= hTempBilBillunitCnt) || (!hCardCurrentCode.equals("0"))) {
                hContForcedPostFlag = "Y";
                hContCltForcedPostFlag = "Y";
                hContAprFlag = "Y";
                
            } 
            */
            
            //if ((hCardCurrentCode.equals("0")) && (!hContForcedPostFlag.equals("Y")))
            
            if (!hContForcedPostFlag.equals("Y")) 
                runTerm = hContInstallCurrTerm;
            else
                runTerm = hContInstallTotTerm;

            tempCurrTerm = hContInstallCurrTerm;
            
            for (int intb = tempCurrTerm; intb <= runTerm; intb++) {
                hContInstallCurrTerm = intb;
                hConpCurrPostAmt = hContUnitPrice;
                tmpstr = String.format("%2d", intb);
                hConpInstallCurrTerm = tmpstr;

                if (intb == 1) {
                    if ((hContExtraFees <= 0) && (hContNewItFlag.equals("Y"))) {
                        hConpCurrPostAmt = hContExtraFees;
                        hConpKindAmt = "2";
                        insertBilContpost(2);
                    }
                    hConpCurrPostAmt = hContUnitPrice + hContFirstRemdAmt;
                }

                if (intb == hContInstallTotTerm) {
                    hConpCurrPostAmt = hContUnitPrice + hContRemdAmt;
                    hContAllPostFlag = "Y";
                }
                hConpKindAmt = "1";
                if (hConpCurrPostAmt > 0)
                    insertBilContpost(1);

                if (intb == tempCurrTerm) {
                    hProdTransRate = 0;
                    hProdTransRate = hContTransRate;
                    
                    /*TCB一律用bil_contract裡的利率
                    if (hContTransRate != 0) {
                        hProdTransRate = hContTransRate;
                    } else {
                        if (selectBilProd() != 0) {
                            stderr = String.format("select bil_prod_2 not found error\n");
                            showLogMessage("I", "", "    err card no=" + hContCardNo);
                            comcr.errRtn(stderr, hContMchtNo + "," + hMercMchtType + "," + hContProductNo, hCallBatchSeqno);

                        }
                    }
                    */
                    
                    /*** 若利率大於0 ***/
                    if (hProdTransRate > 0) {
                        
                        /*** 取得本期起迄 ***/
                        /*** 取得上次關帳日 ***/
                        if ((hAcnoNewCycleMonth.length() > 0)
                                && (hWdayNextAcctMonth.equals(hAcnoNewCycleMonth))) {
                            tmprcsdate = String.format("%s", hAcnoLastInterestDate);
                        } else {
                            tmprcsdate = String.format("%s", hWdayThisInterestDate);
                        }
                        if (intb == 1) {
                            /*** 若首期消費日比上次關帳日更早，表示已單獨出過0期利息，不再重覆該時段計息 ***/
                            if (hContPurchaseDate.compareTo(tmprcsdate) > 0) {
                                tmprcsdate = String.format("%s", hContPurchaseDate);
                            }
                        }

                        /*** 計算剩餘金額 ***/
                        doubleAmt1 = hContUnitPrice * (hContInstallTotTerm - hContInstallCurrTerm + 1);
                        /*** 若為首期需再加上首期餘額與餘額 ***/
                        if (intb == 1)
                            doubleAmt1 = doubleAmt1 + hContFirstRemdAmt + hContRemdAmt;
                        /*** 若為最後一期需再加上餘額 ***/
                        else if (intb == hContInstallTotTerm)
                            doubleAmt1 = doubleAmt1 + hContRemdAmt;

                        /*** 若人工登記一次到期 ***/
                        if (hTempForcedPostFlag.equals("Y")) {
                            selectActJrnl();
                            
                        } else {
                            tmpRcEDate = String.format("%s%s", hTempThisYyyymm, hWdayStmtCycle);
                        }
                        
                        /*** 計算天數 ***/
                        tempRcDays1 = comcr.calDays(tmprcsdate, tmpRcEDate);
                        
                        tmpRcSDd = comcr.str2int(tmprcsdate.substring(6));
                        tmpItemPostDd = comcr.str2int(tmpRcEDate.substring(6));

                        lonAmt2 = 0;

                        /*  TCB出完帳的就走正常信用卡分期未繳的利息算法

                        if (tempCurrTerm == intb && tempCurrTerm != 1) {
                            getLastMinPayDate();
                            
                            if (tmprcsdate.compareTo(hTempLastMinPayDate) <  0 &&
                                tmpRcEDate.compareTo(hTempLastMinPayDate) >= 0) {
                                
                                //加算上期金額的天數利息，計算至繳款日 
                                tempRcDays2 = comcr.calDays(tmprcsdate, hTempLastMinPayDate);

                                hTempMinDdBeg = lastMonthDays - comcr.str2int(tmprcsdate.substring(6));
                                hTempMinDdEnd = comcr.str2int(hTempLastMinPayDate.substring(6));
                                
                                selectLastActDebt();

                                doubleAmt2 = hDebtBegBal;
                                
                                if (swNew.equals("N")) {
                                    oldTransRate = hProdTransRate;
                                    if (newMethod.equals("Y") && hProdTransRate > hAgenRevolvingInterest1) {
                                        hProdTransRate = hAgenRevolvingInterest1;
                                    }
                                }
                                swNew = "Y";

                                lonAmt2 = (long) (doubleAmt2 * tempRcDays2 * hProdTransRate / 100 / 365);
                                if (hEffcMonth.compareTo(hTempLastMinPayDate.substring(6)) > 0)
                                    lonAmt2 = (long) (doubleAmt2 * tempRcDays2 * hContTransRate / 100 / 365);

                                if (newMethod.equals("Y") && hEffcMonth.equals(hBusiBusinessDate.substring(0, 6))
                                        && hEffcMonth.compareTo(hTempLastMinPayDate.substring(0, 6)) <= 0) {
                                    tempDouble = (doubleAmt2 * oldTransRate * hTempMinDdBeg / yearDays / 100);
                                    tempDouble1 = (doubleAmt2 * hProdTransRate * hTempMinDdEnd / yearDays / 100);
                                    lonAmt2 = (long) (tempDouble + tempDouble1);
                                    
                                }

                            } else {
                                
                                selectActDebt();
                                
                                // 未繳足MP，取得act_debt剩餘本金加計一個月的利息 
                                if (hDebtEndBal > 0) {
                                    // 判斷之前的期數未繳則再加計剩餘本金 
                                    doubleAmt1 = doubleAmt1 + hDebtEndBal;
                                    
                                    if (lineCnt > 64) {
                                        printHeader();
                                    }
                                    tmpstr = String.format("未繳足MP(未全部入帳)");
                                    printDetail();
                                }
                            }
                        }
                
                        */

                        if (swNew.equals("N")) {
                            oldTransRate = hProdTransRate;
                            if (newMethod.equals("Y") && hProdTransRate > hAgenRevolvingInterest1) {
                                hProdTransRate = hAgenRevolvingInterest1;
                            }
                        }

                        longAmt1 = (long) (doubleAmt1 * tempRcDays1 * hProdTransRate / 100 / 365 + 0.5);
                        
                        if (tempRcDays1 > 0) {
                            if (newMethod.equals("Y") && hEffcMonth.equals(hBusiBusinessDate.substring(0, 6))
                                    && hEffcMonth.compareTo(tmpRcEDate.substring(0, 6)) <= 0) {
                                if (hContInstallCurrTerm == 1) {
                                    oldDays = lastMonthDays - tmpRcSDd;
                                    
                                    if ((oldDays + hTempPostCycleDd) > lastMonthDays)
                                        oldDays = tempRcDays1 - tmpItemPostDd;
                                    
                                    tempDouble = (doubleAmt1 * oldTransRate * oldDays / yearDays / 100);
                                    
                                } else
                                    tempDouble = (doubleAmt1 * oldTransRate * (tempRcDays1 - tmpItemPostDd) / yearDays / 100);
                                
                                tempDouble1 = (doubleAmt1 * hProdTransRate * (tmpItemPostDd) / yearDays / 100);
                                longAmt1 = (long) (tempDouble + tempDouble1 + 0.5); //四捨五入
                                
                            }
                        }
                        hConpCurrPostAmt = longAmt1 + lonAmt2;

                        hConpKindAmt = "5";
                        if (hConpCurrPostAmt > 0)
                            insertBilContpost(5);
                    }
                }

                if ((hContCltInstallTotTerm >= hContInstallCurrTerm) &&
                    (hContCltUnitPrice > 0)) {
                    hConpCurrPostAmt = hContCltUnitPrice;
                    if ((hContFeeFlag.equals("F")) && (hContInstallCurrTerm == 1))
                        hConpCurrPostAmt = hContCltUnitPrice + hContCltRemdAmt;
                    if ((hContFeeFlag.equals("L")) && (hContInstallCurrTerm == hContInstallTotTerm))
                        hConpCurrPostAmt = hContCltUnitPrice + hContCltRemdAmt;

                    hConpKindAmt = "3";
                    insertBilContpost(3);
                }
            }
            updateBilContract();
        }

        closeCursor(cursorIndex);
    }

    /***********************************************************************/
    void selectCrdCard1() throws Exception {
        crdCardCnt = 0;
        sqlCmd = "select count(*) crd_card_cnt ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where acct_type  = ?  ";
        sqlCmd += "and acno_p_seqno  = ?  ";
        sqlCmd += "and (current_code = '0' or reissue_status in ('1','2'))  ";
        setString(1, hContAcctType);
        setString(2, hContPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found!", "", hContPSeqno);
        }
        if (recordCnt > 0) {
            crdCardCnt = getValueInt("crd_card_cnt");
        }

    }

    /***********************************************************************/
    void selectActJrnl() throws Exception {
        hJrnlInterestDate = "";

        sqlCmd = "select interest_date ";
        sqlCmd += " from ACT_JRNL  ";
        sqlCmd += "where p_seqno     = ?  ";
        sqlCmd += "  and acct_type   = ?  ";
        sqlCmd += "  and tran_class  = 'P'  ";
        sqlCmd += "  and tran_type not in ('REFU','MIST','COMA','COMB','BON1','BON2','WAIP','BACK','COBO')  ";
        sqlCmd += "  and crt_date        > ?  ";
        sqlCmd += "  and transaction_amt >= ?  ";
        sqlCmd += "ORDER BY CRT_DATE DESC,CRT_TIME DESC,ENQ_SEQNO DESC ";
        setString(1, hCardAcctPSeqno);
        setString(2, hContAcctType);
        setString(3, tmprcsdate);
        setDouble(4, doubleAmt1);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hJrnlInterestDate = getValue("interest_date");
            tmpRcEDate = hJrnlInterestDate;
        } else {
            tmpRcEDate = String.format("%s", hWdayNextInterestDate);
        }

    }

    /***********************************************************************/
    int getLastMinPayDate() throws Exception {
        hTempLastMinPayDate = "";

        sqlCmd = "select max(last_min_pay_date) h_temp_last_min_pay_date ";
        sqlCmd += " from (select decode(last_min_pay_date,'',?,last_min_pay_date) as last_min_pay_date from act_acct  ";
        sqlCmd += "where p_seqno   = ?  ";
        sqlCmd += "  and acct_type = ?  ";
        sqlCmd += "union select decode(last_min_pay_date,'',?,last_min_pay_date) as last_min_pay_date from act_acct_hst    where p_seqno   = ?  ";
        sqlCmd += "  and acct_type = ?  ) ";
        setString(1, hContPurchaseDate);
        setString(2, hCardAcctPSeqno);
        setString(3, hContAcctType);
        setString(4, hContPurchaseDate);
        setString(5, hCardAcctPSeqno);
        setString(6, hContAcctType);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempLastMinPayDate = getValue("h_temp_last_min_pay_date");
        } else {
            return 1;
        }
        return 0;
    }

    /***********************************************************************/
    void updateBilContract() throws Exception {
        daoTable = "bil_contract";
        updateSQL = " install_curr_term    = ?,";
        updateSQL += " post_cycle_dd        = decode(post_cycle_dd,0,?,post_cycle_dd),";
        updateSQL += " forced_post_flag     = ?,";
        updateSQL += " clt_forced_post_flag = ?,";
        updateSQL += " apr_flag             = ?,";
        updateSQL += " all_post_flag        = ?,";
        updateSQL += " first_post_date      = decode(first_post_date,'',"
                + "decode(cast(? as varchar(10)),1,?,first_post_date),first_post_date),";
        updateSQL += " new_proc_date        = decode(new_proc_date,'',?,new_proc_date),";
        updateSQL += " last_update_date     = ?,";
        updateSQL += " new_it_flag          = ?,";
        updateSQL += " mod_pgm              = ?,";
        updateSQL += " mod_time             = sysdate";
        whereStr = "where rowid           = ? ";
        setInt(1, hContInstallCurrTerm);
        setInt(2, hTempPostCycleDd);
        setString(3, hContForcedPostFlag);
        setString(4, hContCltForcedPostFlag);
        setString(5, hContAprFlag);
        setString(6, hContAllPostFlag);
        setInt(7, hContInstallCurrTerm);
        setString(8, hBusiBusinessDate);
        setString(9, hBusiBusinessDate);
        setString(10, hBusiBusinessDate);
        setString(11, hContNewItFlag);
        setString(12, prgmId);
        setRowId(13, hContRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_bil_contract not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilD005 proc = new BilD005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
