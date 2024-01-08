/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  106/06/01  V1.00.00    Edson     program initial                         *
*  107/06/21  V1.33.01    林志鴻    RECS-s1070713-058 溢繳款額度不可分期    *
*  107/10/09  V1.34.01    Atom      RECS-s1070907-085 chk_card_auto MCC-CODE  *
*  108/03/04  V1.35.01    陳君暘    RECS-s1071222-132 acct_jrnl_bal calculate *
*  108/03/21  V1.35.01    Brian     Transfer to java                        *
*  108/07/15  V1.36.01    陳君暘     RECS-s1071222-132 remove end_bal_op, lk   *
*  108/10/29  V1.37.02    陳君暘     RECS-s1080605-058 installment_kind = 'O'  *
*  109/01/15  V1.37.02    Brian     update to V1.37.02                      *
*  109/11/24  V1.00.03    shiyuqi       updated for project coding standard *
*  109/11/30  V1.00.04    JeffKung     updated for TCB                      *
*  111/09/22  V1.00.05    Justin    弱點修正                                                                 *
*  111/12/01  V1.00.06    JeffKung  二代帳務檔資料問題調整程式,增加分期金額的檢核
*  112/05/23  V1.00.07    JeffKung  fixBug tmpRskRsn=I4計算錯誤                          * 
*  112/12/05  V1.00.08    JeffKung  保留EDC分期,其餘部分拆到另一支程式                *
****************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;

/*清算交易EDC分期比對處理*/
public class BilA030 extends AccessDAO {
    private String progname = "清算交易EDC分期比對處理   112/12/05  V1.00.08" ;
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallErrorDesc = "";
    int tmpInt = 0;
    String hTempUser = "";

    String prgmId  = "BilA030";
    String prgmName = "清算交易EDC分期比對處理";

    String errMsg = "";
    String buf = "";
    String szTmp = "";
    String hModUser = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;

    String hPgcdAutoInstallment = "";
    String hPgcdAssignInstallment = "";
    String hBusinessDate = "";
    String hSystemDate = "";
    String hSystemTime = "";
    String hSystemDateF = "";
    String hCurpReferenceNo = "";
    String hCurpBillType = "";
    String hCurpTransactionCode = "";
    String hCurpMerchantNo = "";
    String hCurpCardNo = "";
    String hCurpPurchaseDate = "";
    String hCurpFilmNo = "";
    String hCurpGroupCode = "";
    String hCurpAcctType = "";
    String hCurpStmtCycle = "";
    String hCurpAcquirerMemberId = "";
    String hCurpPaymentType = "";
    String hCurpMerchantEngName = "";
    String hCurpMerchantChiName = "";
    String hCurpMerchantCity = "";
    String hCurpMerchantCountry = "";
    String hCurpMerchantCategory = "";
    String hCurpMerchantZip = "";
    String hCurpMerchantState = "";
    String hCurpAuthorization = "";
    String hCurpBatchNo = "";
    double hCurpDestinationAmt = 0;
    int hCurpInstallTotTerm = 0;
    int hCurpInstallPerAmt = 0;
    int hCurpInstallFirstAmt = 0;
    int hCurpInstallFee = 0;
    String hCurpRskType = "";
    String hCurpRskType2 = "";
    String hCurpDoubtType = "";
    int hCurpDeductBp = 0;
    double hCurpCashPayAmt = 0;
    double hCurpAmtMccrNum = 0;
    double hCurpAmtIccrNum = 0;
    String hCurpAmtMccr = "";
    String hCurpAmtIccr = "";
    String hCurpCardSw = "";
    String hCurpPSeqno = "";
    String hCurpAcnoPSeqno = "";
    String hCurpCurrCode = "";
    double hCurpDcAmount = 0;
    String hCurpVCardNo = "";
    String hCurpRowid = "";
    String hBityAutoInstallment = "";
    String hCardAutoInstallment = "";
    String hCurpSettlFlag = "";
    String hCurpIdPSeqno = "";
    String hCurpEcsCusMchtNo = "";
    String hBankNo = "";
    String hPrintName = "";
    String hRptName = "";
    String hAcnoStmtCycle = "";
    String hAcnoAutoInstallment = "";
    String hAcnoCardIndicator = "";
    double hAcnoLineOfCreditAmt = 0;
    long hTempLineOfCreditBal = 0;
    double hAcctAcctJrnlBal = 0;
    double hTempUnpostInstFee = 0;
    double hTempCpsInstFee = 0;
    double hTempBilInstFee = 0;
    double hPaccInstCrdtamt = 0;
    String hBlogContractNo = "";
    String hBlogCardNo = "";
    String hBlogMerchantNo = "";
    String hBlogProductNo = "";
    String hContFeeFlag = "";
    double hBlogRemdAmtOld = 0;
    double hContFirstRemdAmt = 0;
    double hBlogRemdAmtTail = 0;
    double hContCltFeesAmt = 0;
    double hContCltRemdAmt = 0;
    double hBlogRedeemAmt = 0;
    String hContRowid = "";
    int hContAdCount = 0;
    double hBlogBackAmt = 0;
    double hBlogRemdAmtNew = 0;
    String hAutoInstallment = "";
    String hDtlFlag = "";
    double hMtkiAmt1From = 0;
    double hMtkiAmt1To = 0;
    int hMtkiTerm1 = 0;
    double hMtkiAmt2From = 0;
    double hMtkiAmt2To = 0;
    int hMtkiTerm2 = 0;
    double hMtkiAmt3From = 0;
    int hMtkiTerm3 = 0;
    int tempInt = 0;
    int hSeqNo = 0;
    String hTempCardType = "";
    String hMtkiMerchantNo = "";
    String hMercMerchantChiName = "";
    String hCurrentCode = "";
    String hPaccUnonFlag = "";
    String hPainMerchantNo = "";
    int hAsinInstallmentTerm = 0;
    String hProdProductNo = "";
    double hProdTransRate = 0;
    double hProdYearFeesRate = 0;
    String tempX10 = "";
    String hContProductName = "";
    double hContFeesFixAmt = 0;
    double hContFeesRate = 0;
    int hContAgainstNum = 0;
    String hInstallmentFlag = "";
    double hProdCltFeesFixAmt = 0;
    double hProdCltInterestRate = 0;
    String hContProductNo = "";
    String hContPtrMerchantNo = "";
    double hProdCltFeesMinAmt = 0;
    double hProdCltFeesMaxAmt = 0;
    String hContAcctKey = "";
    String hContAcctType = "";
    String hContAcnoPSeqno = "";
    String hContPSeqno = "";
    String hContGpNo = "";
    String hContIdPSeqno = "";
    String hContCardNo = "";
    String hContStmtCycle = "";
    String hContContractNo = "";
    int hContContractSeqNo = 0;
    String hContBackCardNo = "";
    String hContNewCardNo = "";
    String hContMerchantNo = "";
    String hContMerchantChiName = "";
    String hContContractKind = "";
    String hContAllocateFlag = "";
    String hContCvv2 = "";
    double hContExchangeAmt = 0;
    double hContUnitPrice = 0;
    int hContQty = 0;
    double hContTotAmt = 0;
    int hContInstallTotTerm = 0;
    double hContRemdAmt = 0;
    String hContAutoDelvFlag = "";
    double hContExtraFees = 0;
    String hContFirstPostDate = "";
    int hContPostCycleDd = 0;
    int hContInstallCurrTerm = 0;
    String hContAllPostFlag = "";
    String hContRefundFlag = "";
    int hContRefundQty = 0;
    String hContRefundConfirmFlag = "";
    String hContRefundConfirmDate = "";
    String hContConfirmDate = "";
    String hContConfirmFlag = "";
    String hContReceiveName = "";
    String hContReceiveTel = "";
    String hContReceiveTel1 = "";
    String hContVoucherHead = "";
    String hContUniformNo = "";
    String hContZipCode = "";
    String hContReceiveAddress = "";
    String hContDelvDate = "";
    String hContDelvConfirmFlag = "";
    String hContDelvConfirmDate = "";
    String hContRegisterNo = "";
    String hContAuthorization = "";
    String hContDelvBatchNo = "";
    String hContForcedPostFlag = "";
    int hContInstallBackTerm = 0;
    String hContInstallBackTermFlag = "";
    String hContDevFlag20 = "";
    String hContPrtFlag21 = "";
    String hContCpsFlag = "";
    String hContLimitEndDate = "";
    String hContFilmNo = "";
    String hContReferenceNo = "";
    int hContCltInstallTotTerm = 0;
    String hContInstallmentKind = "";
    String hContFirstPostKind = "";
    double hTmpContTotAmt = 0;
    String hTmpContFirstPostDate = "";
    double hBlogCurrTerm = 0;
    double hBlogUnitPriceOld = 0;
    double hContCltUnitPrice = 0;
    double hBlogRedeemPoint = 0;
    double hBlogBackTerm = 0;
    double hBlogUnitPriceNew = 0;
    double hCurpIssueFee = 0;
    String swReserveType = "0";
    String hPainTwdLimitFlag = "";
    String hPainInsurance12TermFlag = "";

    String tempDoubtType = "";
    String tempRskFlag = "";
    String tempRskRsn = "";
    String tempRskType2 = "";
    String hTempstr = "";
    String hDebitFlag = "";
    String swAutoBack = "";
    String wsPSeqno = "";
    String hPbtbDcCurrCode = "";
    String hCurpContractFlag = "";
    double hCurpInstallPerAmt1 = 0;
    int pageCntR2 = 0;
    int pageCntR3 = 0;
    int pageCntR7 = 0;
    int pageCntR8 = 0;
    int cntBack = 0;
    int r2Cnt = 0;
    int r3Cnt = 0;
    int r7Cnt = 0;
    int r8Cnt = 0;
    int maxTerm = 0;
    int currTerm = 0;
    int cntContract = 0;
    private long totalCnt = 0;
    private String hCurpSourceCurrency = "";
    private double hAutxDestinationAmt =0;
    private String hCallBatchSeqno;

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname + "[" + args.length + "]");
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : BilA030 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            showLogMessage("I", "", "Process_date = " + hBusinessDate);

            selectBilCurpost();

            showLogMessage("I", "", String.format("    總筆數=[%d],分期=[%d],退貨=[%d]", totalCnt, cntContract, cntBack));

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }

        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_system_time, ";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += "from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemTime = getValue("h_system_time");
            hSystemDateF = getValue("h_system_date_f");
        }
        hModUser = comc.commGetUserID();
    }
    /***********************************************************************/
    void selectBilCurpost() throws Exception
    {
        sqlCmd = "select ";
        sqlCmd += "a.reference_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "a.mcht_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.purchase_date,";
        sqlCmd += "a.film_no,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "a.acno_p_seqno  ,";
        sqlCmd += "a.id_p_seqno, ";
        sqlCmd += "acq_member_id,";
        sqlCmd += "payment_type,";
        sqlCmd += "mcht_eng_name,";
        sqlCmd += "decode(" + "decode(source_curr,'TWD','901',source_curr),'901',"
                + "decode(substr(decode(mcht_country,'','TW',mcht_country),1,2),'TW',"
                + "decode(trim(mcht_chi_name)       ,'',mcht_eng_name,"
                + "decode(substrb(mcht_chi_name,1,4),'　　',mcht_eng_name,mcht_chi_name)),"
                + "decode(trim(mcht_eng_name)       ,'',mcht_chi_name,mcht_eng_name)),"
                + "decode(trim(mcht_eng_name),'',mcht_chi_name,mcht_eng_name)) h_curp_mcht_chi_name,";
        sqlCmd += "mcht_city,";
        sqlCmd += "mcht_country,";
        sqlCmd += "mcht_category,";
        sqlCmd += "mcht_zip,";
        sqlCmd += "mcht_state,";
        sqlCmd += "auth_code,";
        sqlCmd += "a.batch_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "install_tot_term,";
        sqlCmd += "install_per_amt,";
        sqlCmd += "install_first_amt,";
        sqlCmd += "install_fee,";
        sqlCmd += "rsk_type,";
        sqlCmd += "doubt_type,";
        sqlCmd += "deduct_bp,";
        sqlCmd += "cash_pay_amt,";
        sqlCmd += "a.amt_mccr , ";
        sqlCmd += "a.amt_iccr , ";
        sqlCmd += "a.issue_fee,";
        sqlCmd += "a.bin_type,";  // card_sw
        sqlCmd += "decode(a.curr_code,'','901',a.curr_code) as curr_code,";
        sqlCmd += "dc_amount,";
        sqlCmd += "a.v_card_no,";
        sqlCmd += "source_curr,";
        sqlCmd += "b.auto_installment,";
        sqlCmd += "b.p_seqno, ";
        sqlCmd += "a.settl_flag,";
        sqlCmd += "a.stmt_cycle,";
        sqlCmd += "a.ecs_cus_mcht_no,";
        sqlCmd += "a.rowid  as rowid ";
        sqlCmd += " from bil_postcntl, bil_curpost a  ";
        sqlCmd += "inner join crd_card   b  on b.card_no   = a.card_no ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and a.batch_no = batch_date||batch_unit||trim(to_char(batch_seq,'0000')) ";
        sqlCmd += "  and decode(contract_flag ,'','N',contract_flag)   = 'N' ";
        sqlCmd += "  and decode(curr_post_flag,'','N',curr_post_flag) != 'Y' ";  //尚未入帳
        sqlCmd += "  and payment_type in ('I','E','1','2') ";  //分期及紅利折抵交易
        /* 合格 */
        sqlCmd += "  and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('N','n') ";
        sqlCmd += "  and decode(double_chk_ok_flag,'','N',double_chk_ok_flag) in ('N','n') ";
        sqlCmd += "  and decode(err_chk_ok_flag   ,'','N',err_chk_ok_flag )   in ('N','n') "; 
        sqlCmd += "  and decode(confirm_flag_p,'','N',confirm_flag_p) in ('Y','y') ";
        sqlCmd += "order by a.p_seqno, txn_code, a.batch_no ";

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
        	
            hCurpReferenceNo = getValue("reference_no");
            hCurpBillType = getValue("bill_type");
            hCurpTransactionCode = getValue("txn_code");
            hCurpMerchantNo = getValue("mcht_no");
            hCurpCardNo = getValue("card_no");
            hCurpPurchaseDate = getValue("purchase_date");
            hCurpFilmNo = getValue("film_no");
            hCurpGroupCode = getValue("group_code");
            hCurpAcctType = getValue("acct_type");
            hCurpAcnoPSeqno = getValue("acno_p_seqno");
            hCurpPSeqno = getValue("p_seqno"  );
            hCurpAcquirerMemberId = getValue("acq_member_id");
            hCurpPaymentType = getValue("payment_type");
            hCurpMerchantEngName = getValue("mcht_eng_name");
            hCurpMerchantChiName = getValue("h_curp_mcht_chi_name");
            hCurpMerchantCity = getValue("mcht_city");
            hCurpMerchantCountry = getValue("mcht_country");
            hCurpMerchantCategory = getValue("mcht_category");
            hCurpMerchantZip = getValue("mcht_zip");
            hCurpMerchantState = getValue("mcht_state");
            hCurpAuthorization = getValue("auth_code");
            hCurpBatchNo = getValue("batch_no");
            hCurpDestinationAmt = getValueDouble("dest_amt");
            hCurpInstallTotTerm = getValueInt("install_tot_term");
            hCurpInstallPerAmt = getValueInt("install_per_amt");
            hCurpInstallFirstAmt = getValueInt("install_first_amt");
            hCurpInstallFee = getValueInt("install_fee");
            hCurpRskType = getValue("rsk_type");
            hCurpDoubtType = getValue("doubt_type");
            hCurpDeductBp = getValueInt("deduct_bp");
            hCurpCashPayAmt = getValueDouble("cash_pay_amt");
            hCurpAmtMccr = getValue("amt_mccr");
            hCurpAmtIccr = getValue("amt_iccr");
            hCurpAmtMccrNum = comc.str2double(hCurpAmtMccr);
            hCurpAmtIccrNum = comc.str2double(hCurpAmtIccr);
            hCurpIssueFee = getValueDouble("issue_fee");
            hCurpCardSw = getValue("bin_type");
            hCurpCurrCode = getValue("curr_code");
            hCurpDcAmount = getValueDouble("dc_amount");
            hCurpVCardNo = getValue("v_card_no");
            hCurpSourceCurrency = getValue("source_curr");
            hCurpRowid = getValue("rowid");
            hCardAutoInstallment = getValue("auto_installment");
            hCurpStmtCycle = getValue("stmt_cycle");
            hCurpSettlFlag = getValue("settl_flag");
            hCurpIdPSeqno = getValue("id_p_seqno");
            hCurpEcsCusMchtNo = getValue("ecs_cus_mcht_no");
            hCurpInstallPerAmt1 = hCurpInstallPerAmt;

            hCurpContractFlag = "P";
            tempDoubtType = hCurpDoubtType;
            tempRskFlag = "";
            tempRskRsn = "";
            tempRskType2 = "";
            totalCnt++;
            if(totalCnt % 5000 == 0 || totalCnt == 1) {
                String msg = "BilA030  Process record=[" + totalCnt + "]";
                showLogMessage("I", "", msg);
            }

            //紅利折抵交易 , 財金(1~2)
            if ("1".equals(hCurpPaymentType) || "2".equals(hCurpPaymentType) ) {
                
            	insertBilPaymentType();
                updateBilCurpost(4);
                continue;
                
            }
            
            if ("I".equals(hCurpPaymentType) || "E".equals(hCurpPaymentType) ) {

                selectBinNo();
                
            	//若是EDC前端分期,要先檢查格式的正確性
            	if (formatCheckForInstTxn() == false ) {
            		updateBilCurpostRsk(9);
            		continue;
            	};
            	
            	hContInstallmentKind = "C"; //NCCC分期交易
                hContFirstPostKind = "1";  //當期入帳
                
                if(hCurpTransactionCode.equals("05") || hCurpTransactionCode.equals("26")) {
                    insertBilContract();
                }
                if(hCurpTransactionCode.equals("06") || hCurpTransactionCode.equals("25")) {
                    insertBilBackLog();
                }
            	
                if (tempRskFlag.length() > 0) {
                	updateBilCurpostRsk(1);
                } else {
                	updateBilCurpost(4);
                }
                    
            	
            }
            
            wsPSeqno = hCurpPSeqno;
        }
        closeCursor(cursorIndex);

    }
    
    /*EDC前端分期的格式查核  */
    boolean formatCheckForInstTxn() throws Exception {
    	boolean formatCheckForInstTxn = true;
    	
    	//雙幣卡非台幣交易不可分期
    	if (hPbtbDcCurrCode.equals("901") == false &&
            hCurpCurrCode.equals("901")   == false &&
            hCurpCurrCode.equals("TWD")   == false ) {
    		tempRskFlag = "9";
    		tempRskRsn = "I1";
    		formatCheckForInstTxn = false;

    		return formatCheckForInstTxn;
        }
    	
    	//疑異交易不可分期
        if ( hCurpRskType.length() != 0 
            && hCurpRskType.compareTo("1") >= 0 
            && hCurpRskType.compareTo("3") <= 0 ) {
        	
        	tempRskFlag = hCurpRskType;
    		tempRskRsn = "I2";
    		formatCheckForInstTxn = false;

    		return formatCheckForInstTxn;
        }
        
        //不合格交易不可分期
        if ( hCurpDoubtType.length() != 0 ) {
        	tempRskRsn = "I3";
    		formatCheckForInstTxn = false;

    		return formatCheckForInstTxn;
        }
    	
        //不合格交易不可分期
        if ( hCurpInstallTotTerm==0 || hCurpCashPayAmt==0 ) {
        	tempDoubtType = "0001";
            tempRskRsn = "I3";
    		formatCheckForInstTxn = false;

    		return formatCheckForInstTxn;
        }

        //正向才檢查各項欄位算出來的總金額
        if(hCurpTransactionCode.equals("05") || hCurpTransactionCode.equals("26")) {

        	int tempLong = (int) (hCurpCashPayAmt / hCurpInstallTotTerm);
        	hCurpInstallPerAmt = tempLong;
        	hContUnitPrice = hCurpInstallPerAmt;
        	hContFirstRemdAmt = hCurpInstallFirstAmt - hCurpInstallPerAmt;
        	hContTotAmt = hCurpInstallTotTerm * hContUnitPrice
        				+ hContFirstRemdAmt + (hCurpDestinationAmt - hCurpCashPayAmt);
 
        	//showLogMessage("I","",String.format("hContTotAmt=%f,hCurpDestinationAmt=%f",hContTotAmt,hCurpDestinationAmt));
        
        	//不合格交易不可分期-金額計算錯誤
        	if (Double.compare(hCurpDestinationAmt,hContTotAmt) == 0) {
        		;
        	} else {
        		tempDoubtType = "0001";
        		tempRskRsn = "I4";
        		formatCheckForInstTxn = false;

        		return formatCheckForInstTxn;
        	}
        }

    	return formatCheckForInstTxn;
    }
    
    /***********************************************************************/
    void updateBilCurpost(int idx) throws Exception {
        daoTable   = "bil_curpost";
        updateSQL  = " contract_flag = ?,";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where rowid    = ? ";
        setString(1, hCurpContractFlag);
        setString(2, prgmId);
        setRowId(3, hCurpRowid);
        updateTable();
        if (notFound.equals("Y")) {
            showLogMessage("E","",String.format("update_bil_curpost not found, referenceNo= [%s]", hCurpReferenceNo));
        }
    }

    /***********************************************************************/
    void updateBilCurpostRsk(int idx) throws Exception {
        
        daoTable   = "bil_curpost";
        updateSQL  = " rsk_type      = ?,";
        updateSQL += " rsk_rsn       = ?,";
        updateSQL += " doubt_type    = ?,";
        
        //20231013若是格式錯,將格式檢核on
        if ("".equals(tempDoubtType)==false) {
        	updateSQL += " format_chk_ok_flag = 'Y', ";
        }
        	
        updateSQL += " contract_flag = 'P',";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " mod_pgm       = ?";
        whereStr   = "where rowid    = ? ";
        setString(1, tempRskFlag);
        setString(2, tempRskRsn);
        setString(3, tempDoubtType);
        setString(4, prgmId);
        setRowId( 5, hCurpRowid);
        updateTable();
        if (notFound.equals("Y")) {
        	showLogMessage("E","",String.format("update_bil_curpost not found, referenceNo= [%s]", hCurpReferenceNo));
        }
    }

    /***********************************************************************/
    void insertBilPaymentType() throws Exception {
        setValue("REFERENCE_NO", hCurpReferenceNo);
        setValue("TX_CODE", hCurpTransactionCode);
        setValue("PAYMENT_TYPE", hCurpPaymentType);
        setValue("MCHT_NO", hCurpMerchantNo);
        setValue("CARD_NO", hCurpCardNo);
        setValue("PURCHASE_DATE", hCurpPurchaseDate);
        setValueDouble("DEST_AMT", hCurpDestinationAmt);
        setValueInt("DEDUCT_BP", hCurpDeductBp);
        setValueInt("DEDUCT_AMT", (int) (hCurpDestinationAmt - hCurpCashPayAmt));
        setValueDouble("CASH_PAY_AMT", hCurpCashPayAmt);
        setValue("mod_user", hModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        daoTable = "bil_payment_type";
        insertTable();
        if (dupRecord.equals("Y")) {
            showLogMessage("E","",String.format("insert bilPaymentType duplicate,referenceNo=[%s]",hCurpReferenceNo));
        }
    }

    /***********************************************************************/
    void selectBinNo() throws Exception {
        hAutoInstallment = "";
        hDebitFlag = "";
        hPbtbDcCurrCode = "";
        sqlCmd = "select auto_installment  ";
        sqlCmd += "     , decode(c.debit_flag  ,'','N'  ,c.debit_flag)   h_debit_flag ";
        sqlCmd += "     , decode(c.dc_curr_code,'','901',c.dc_curr_code) h_pbtb_dc_curr_code ";
        sqlCmd += "  from ptr_bintable c,  crd_card  a ";
        sqlCmd += " where a.card_no    = ? ";
        sqlCmd += "   and c.bin_no || c.bin_no_2_fm || '0000' <= a.card_no  ";
        sqlCmd += "   and c.bin_no || c.bin_no_2_to || '9999' >= a.card_no  ";
        setString(1, hCurpCardNo);
        tmpInt = selectTable();

        if(notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found 1!", "", hCurpCardNo);
        }
        if (tmpInt > 0) {
            hAutoInstallment = getValue("auto_installment");
            hDebitFlag = getValue("h_debit_flag");
            hPbtbDcCurrCode = getValue("h_pbtb_dc_curr_code");
        }

    }

    /***********************************************************************/
    void insertBilContract() throws Exception {
        double tempDouble = 0;
        int tempLong = 0;

        initBilContract();

        sqlCmd = "select substr(to_char(bil_contractseq.nextval,'0000000000'),2,10) temp_x10 ";
        sqlCmd += "  from dual ";
        int tmpInt = selectTable();
        if (tmpInt > 0) {
            tempX10 = getValue("temp_x10");
        }

        hContContractNo = tempX10;

        hContContractSeqNo = 1;
        hContCardNo = hCurpCardNo;
        
        //預約分期成功,ptr_mcht_no放預約參數指定的特店代號
        if ("D".equals(hContInstallmentKind)) {
        	hContMerchantNo = hCurpMerchantNo;
        	hContPtrMerchantNo = hPainMerchantNo;
        } else {
        	hContMerchantNo = hCurpMerchantNo;
        	hContPtrMerchantNo = hCurpMerchantNo;
        }
        
        hContMerchantChiName = hCurpMerchantChiName;
        hContProductNo = String.format("%02d", hCurpInstallTotTerm);
        hContAuthorization = hCurpAuthorization;
        
        hContCpsFlag = "N";  //default
        
        hContProductName = "分期交易NCCC";
        
        //自行收單分期交易
        if (hCurpSettlFlag.equals("9") && hContInstallmentKind.equals("C") && hCurpPaymentType.equals("I"))
        {
        	hContProductName = "分期交易ONUS";
            hContInstallmentKind = "O";
            hContCpsFlag = "Y";
        }
        
        if(hContInstallmentKind.equals("C") || hContInstallmentKind.equals("O")) {
            sqlCmd = "select product_name,";
            sqlCmd += "fees_fix_amt,";
            sqlCmd += "interest_rate,";
            sqlCmd += "against_num,";
            sqlCmd += "installment_flag,";
            sqlCmd += "clt_fees_fix_amt,";
            sqlCmd += "clt_interest_rate ";
            sqlCmd += " from bil_prod_nccc  ";
            sqlCmd += "where product_no  = ?  ";
            sqlCmd += "  and mcht_no     = ?  ";
            sqlCmd += "  and START_DATE <= ?  ";
            sqlCmd += "  and end_date   >= ? ";
            setString(1, hContProductNo);
            setString(2, hCurpMerchantNo);
            setString(3, hCurpPurchaseDate);
            setString(4, hCurpPurchaseDate);
            tmpInt = selectTable();
            if (notFound.equals("Y")) {
            	showLogMessage("E","","select_bil_prod_nccc not found!"+ hCurpMerchantNo + "," + hContProductNo);
                hContFeesFixAmt = 0;
                hContFeesRate = 0;
                hContAgainstNum = 0;
                hInstallmentFlag = "";
                hProdCltFeesFixAmt = 0;
                hProdCltInterestRate = 0;
            }
            if (tmpInt > 0) {
                hContProductName = getValue("product_name");
                hContFeesFixAmt = getValueDouble("fees_fix_amt");
                hContFeesRate = getValueDouble("interest_rate");
                hContAgainstNum = getValueInt("against_num");
                hInstallmentFlag = getValue("installment_flag");
                hProdCltFeesFixAmt = getValueDouble("clt_fees_fix_amt");
                hProdCltInterestRate = getValueDouble("clt_interest_rate");
            }

        } 
        
        tempDouble = hCurpCashPayAmt * hProdCltInterestRate / 100 + hProdCltFeesFixAmt;
        tempLong = (int) (tempDouble + 0.5);
        hCurpInstallFee = tempLong;

        if (hCurpInstallFee > hProdCltFeesMaxAmt)
            hCurpInstallFee = (int) hProdCltFeesMaxAmt;
        
        if (hCurpInstallFee < hProdCltFeesMinAmt)
            hCurpInstallFee = (int) hProdCltFeesMinAmt;

        hContAutoDelvFlag = "Y";
        hContAcctType = hCurpAcctType;
        hContPSeqno = hCurpPSeqno;
        hContAcnoPSeqno = hCurpAcnoPSeqno;
        hContIdPSeqno = hCurpIdPSeqno;
        hContStmtCycle = hCurpStmtCycle;

        hContContractKind = "1";
        hContConfirmDate = hSystemDate;
        hContDelvDate = hSystemDate;
        hContDelvConfirmDate = hSystemDate;
        hContDelvConfirmFlag = "Y";
        hContConfirmFlag = "Y";
        hContFeeFlag = "F";
        hContFilmNo = hCurpFilmNo;
        hContReferenceNo = hCurpReferenceNo;
        hContFirstRemdAmt = 0;
        hContQty = 1;
        hContInstallTotTerm = hCurpInstallTotTerm;

        tempLong = (int) (hCurpCashPayAmt / hCurpInstallTotTerm);
        hCurpInstallPerAmt = tempLong;
        hContUnitPrice = hCurpInstallPerAmt;
        hContFirstRemdAmt = hCurpInstallFirstAmt - hCurpInstallPerAmt;
        hContTotAmt = hContInstallTotTerm * hContUnitPrice
                + hContFirstRemdAmt + (hCurpDestinationAmt - hCurpCashPayAmt);
        
        if (comc.getSubString(hCurpPaymentType, 0, 1).equals("I") ||
                comc.getSubString(hCurpPaymentType, 0, 1).equals("E")) {
            hContCltInstallTotTerm = 1;
            hContCltFeesAmt = hCurpInstallFee;
            hContCltUnitPrice = hContCltFeesAmt;
            hContCltRemdAmt = 0;
        }
        if (comc.getSubString(hInstallmentFlag, 0, 1).toUpperCase(Locale.TAIWAN).equals("Y")) {
            hContCltInstallTotTerm = (int) hCurpInstallTotTerm;
            hContCltFeesAmt = hCurpInstallFee;
            tempInt = (int) (hCurpInstallFee / hCurpInstallTotTerm);
            hContCltUnitPrice = tempInt;
            hContCltRemdAmt = hCurpInstallFee - (hCurpInstallTotTerm * tempInt);
        }

        cntContract++;
        hCurpContractFlag = "Y";
        
        if ("C".equals(hContInstallmentKind)) {
            hContCpsFlag = "C";
        } 
        
        setValue("contract_no"            , hContContractNo);
        setValueInt("contract_seq_no"     , hContContractSeqNo);
        setValue("purchase_date"          , hCurpPurchaseDate);
        setValue("card_no"                , hContCardNo);
        setValue("back_card_no"           , hContBackCardNo);
        setValue("new_card_no"            , hContNewCardNo);
        setValue("acct_type"              , hContAcctType);
        setValue("acno_p_seqno"           , hContAcnoPSeqno);
        setValue("p_seqno"                , hContPSeqno);
        setValue("id_p_seqno"             , hContIdPSeqno);
        setValue("stmt_cycle"             , hContStmtCycle);
        setValue("product_no"             , hContProductNo);
        setValue("product_name"           , hContProductName);
        setValue("mcht_no"                , hContMerchantNo);
        setValue("ptr_mcht_no"            , hContPtrMerchantNo);
        setValue("mcht_chi_name"          , hContMerchantChiName);
        setValue("contract_kind"          , hContContractKind);
        setValue("allocate_flag"          , hContAllocateFlag);
        setValue("cvv2"                   , hContCvv2);
        setValueDouble("exchange_amt"     , hContExchangeAmt);
        setValueInt("against_num"         , hContAgainstNum);
        setValueDouble("unit_price"       , hContUnitPrice);
        setValueInt("qty"                 , hContQty);
        setValueDouble("tot_amt"          , hContTotAmt);
        setValueInt("install_tot_term"    , hContInstallTotTerm);
        setValueDouble("remd_amt"         , hContRemdAmt);
        setValue("auto_delv_flag"         , hContAutoDelvFlag);
        setValueDouble("fees_fix_amt"     , hContFeesFixAmt);
        setValueDouble("fees_rate"        , hContFeesRate);
        setValueDouble("extra_fees"       , hContExtraFees);
        setValue("first_post_date"        , hContFirstPostDate);
        setValueInt("post_cycle_dd"       , hContPostCycleDd);
        setValueInt("install_curr_term"   , hContInstallCurrTerm);
        setValue("all_post_flag"          , hContAllPostFlag);
        setValue("refund_flag"            , hContRefundFlag);
        setValueInt("refund_qty"          , hContRefundQty);
        setValue("refund_apr_flag"        , hContRefundConfirmFlag);
        setValue("refund_apr_date"        , hContRefundConfirmDate);
        setValue("apr_date"               , hContConfirmDate);
        setValue("apr_flag"               , hContConfirmFlag);
        setValue("receive_name"           , hContReceiveName);
        setValue("receive_tel"            , hContReceiveTel);
        setValue("receive_tel1"           , hContReceiveTel);
        setValue("voucher_head"           , hContVoucherHead);
        setValue("uniform_no"             , hContUniformNo);
        setValue("zip_code"               , hContZipCode);
        setValue("receive_address"        , hContReceiveAddress);
        setValue("delv_date"              , hContDelvDate);
        setValue("delv_confirm_flag"      , hContDelvConfirmFlag);
        setValue("delv_confirm_date"      , hContDelvConfirmDate);
        setValue("register_no"            , hContRegisterNo);
        setValue("auth_code"              , hContAuthorization);
        setValue("delv_batch_no"          , hContDelvBatchNo);
        setValue("forced_post_flag"       , hContForcedPostFlag);
        setValueInt("install_back_term"   , hContInstallBackTerm);
        setValue("install_back_term_flag" , hContInstallBackTermFlag);
        setValue("dev_flag_20"            , hContDevFlag20);
        setValue("prt_flag_21"            , hContPrtFlag21);
        setValue("cps_flag"               , hContCpsFlag);
        setValue("limit_end_date"         , hContLimitEndDate);
        setValue("fee_flag"               , hContFeeFlag);
        setValue("film_no"                , hContFilmNo);
        setValue("reference_no"           , hContReferenceNo);
        setValue("payment_type"           , hCurpPaymentType);
        setValueDouble("first_remd_amt"   , hContFirstRemdAmt);
        setValueDouble("clt_fees_amt"     , hContCltFeesAmt);
        setValueDouble("clt_unit_price"   , hContCltUnitPrice);
        setValueInt("clt_install_tot_term", hContCltInstallTotTerm);
        setValueDouble("clt_remd_amt", hContCltRemdAmt);
        setValue("batch_no"          , hCurpBatchNo);
        setValue("acquirer_member_id", hCurpAcquirerMemberId);
        setValue("mcht_eng_name"     , hCurpMerchantEngName);
        setValue("installment_kind"  , hContInstallmentKind);
        setValueInt("redeem_point"   , hCurpDeductBp);
        setValueDouble("redeem_amt"  , hCurpDestinationAmt - hCurpCashPayAmt);
        setValue("redeem_kind"       , hCurpDeductBp == 0 ? "0" : "2");
        setValue("new_it_flag"       , "Y");
        setValue("v_card_no"         , hCurpVCardNo);
        setValue("forced_post_flag"  , "N");
        setValue("clt_forced_post_flag"  , "N");
        setValueDouble("trans_rate"  , hProdTransRate);
        setValueDouble("year_fees_rate"  , hProdYearFeesRate);
        setValue("year_fees_date"    , hBusinessDate);
        if (hProdTransRate == 0) {
        	setValue("first_post_kind" , "1");  //利率=0,當期入帳
        } else {
        	setValue("first_post_kind" , "0"); //利率不等於0,次期入帳
        }
        
        setValue("mod_user"          , hModUser);
        setValue("mod_time"          , sysDate + sysTime);
        setValue("mod_pgm"           , prgmId);
        daoTable = "bil_contract";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_contract duplicate", hContContractNo
                    , hCallBatchSeqno);
        }
        
        //卡號授權碼特店代號比對
        if (hContInstallmentKind.equals("C") || hContInstallmentKind.equals("O")) {
            daoTable = "bil_install_log";
            updateSQL  = " apr_flag     = 'Y',";
            updateSQL += " refund_flag  = 'N'";
            whereStr   = "where auth_id_resp_38  = ?  ";
            whereStr  += "  and card_no          = ?  ";
            whereStr  += "  and mcht_id_42       = ? ";
            setString(1, hCurpAuthorization);
            setString(2, hContCardNo);
            setString(3, hCurpMerchantNo);
            updateTable();
            if (notFound.equals("Y")) {
            	;
            }
        }
    }
    /***********************************************************************/
    int insertBilBackLog() throws Exception {
        hBlogContractNo = "";
        hBlogCardNo = "";
        hBlogMerchantNo = "";
        hBlogProductNo = "";
        hContFeeFlag = "";
        hBlogCurrTerm = 0;
        hBlogUnitPriceOld = 0;
        hBlogRemdAmtOld = 0;
        hBlogRemdAmtTail = 0;
        hContCltFeesAmt = 0;
        hContCltUnitPrice = 0;
        hContCltRemdAmt = 0;
        hContFirstRemdAmt = 0;
        hTmpContTotAmt = 0;
        hBlogRedeemAmt = 0;
        hBlogRedeemPoint = 0;
        hTmpContFirstPostDate = "";
        
        int parmIdx = 0;

        sqlCmd = "select contract_no,";
        sqlCmd += "card_no,";
        sqlCmd += "mcht_no,";
        sqlCmd += "product_no,";
        sqlCmd += "decode(fee_flag,'','L', fee_flag) h_cont_fee_flag,";
        sqlCmd += "install_curr_term,";
        sqlCmd += "unit_price,";
        sqlCmd += "first_remd_amt,";
        sqlCmd += "first_remd_amt,";
        sqlCmd += "remd_amt,";
        sqlCmd += "clt_fees_amt,";
        sqlCmd += "clt_unit_price,";
        sqlCmd += "clt_remd_amt,";
        sqlCmd += "tot_amt,";
        sqlCmd += "first_post_date,";
        sqlCmd += "redeem_amt,";
        sqlCmd += "redeem_point,";
        sqlCmd += "cps_flag,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_contract  ";
        sqlCmd += "where card_no       = ?  ";
        sqlCmd += "  and auth_code     = ?  ";
        
        //如果沒有帶入特店代號就不比對
        if("".equals(hCurpMerchantNo)==false) {
        	sqlCmd += "  and ( mcht_no         = ? or mcht_no = ? )  ";
        }

        sqlCmd += "  and refund_apr_date = '' ";
        sqlCmd += "  and contract_seq_no = 1  ";
        sqlCmd += "  and contract_no  in  ";
        sqlCmd += "      (select max(contract_no)  ";
        sqlCmd += "       from bil_contract where card_no       = ?  ";
        sqlCmd += "                         and auth_code       = ?  ";
        if("".equals(hCurpMerchantNo)==false) {
        	sqlCmd += "                     and ( mcht_no         = ? or mcht_no = ? ) ";
        }
        sqlCmd += "                         and refund_apr_date = '' ";
        sqlCmd += "                         and contract_seq_no = 1) ";
        
        setString(++parmIdx, hCurpCardNo);
        setString(++parmIdx, hCurpAuthorization);
        if("".equals(hCurpMerchantNo)==false) {
        	setString(++parmIdx, comc.getSubString(hCurpMerchantNo,0,9));
        	setString(++parmIdx, hCurpMerchantNo);
        }
        setString(++parmIdx, hCurpCardNo);
        setString(++parmIdx, hCurpAuthorization);
        if("".equals(hCurpMerchantNo)==false) {
        	setString(++parmIdx, comc.getSubString(hCurpMerchantNo,0,9));
        	setString(++parmIdx, hCurpMerchantNo);
        }
        
        int tmpInt = selectTable();
        if (notFound.equals("Y")) {
            tempRskFlag = "9";
            tempRskRsn = "I5";
            return 1;
        }
        if (tmpInt > 0) {
            hBlogContractNo = getValue("contract_no");
            hBlogCardNo = getValue("card_no");
            hBlogMerchantNo = getValue("mcht_no");
            hBlogProductNo = getValue("product_no");
            hContFeeFlag = getValue("h_cont_fee_flag");
            hBlogCurrTerm = getValueDouble("install_curr_term");
            hBlogUnitPriceOld = getValueDouble("unit_price");
            hBlogRemdAmtOld = getValueDouble("first_remd_amt");
            hContFirstRemdAmt = getValueDouble("first_remd_amt");
            hBlogRemdAmtTail = getValueDouble("remd_amt");
            hContCltFeesAmt = getValueDouble("clt_fees_amt");
            hContCltUnitPrice = getValueDouble("clt_unit_price");
            hContCltRemdAmt = getValueDouble("clt_remd_amt");
            hTmpContTotAmt = getValueDouble("tot_amt");
            hTmpContFirstPostDate = getValue("first_post_date");
            hBlogRedeemAmt = getValueDouble("redeem_amt");
            hBlogRedeemPoint = getValueDouble("redeem_point");
            hContCpsFlag = getValue("cps_flag");
            hContRowid = getValue("rowid");
        }

        if ((hCurpDestinationAmt != hTmpContTotAmt) ||
                (hCurpDeductBp != hBlogRedeemPoint)) {
            tempRskFlag = "9";
            tempRskRsn = "I6";
            return 1;
        }

        daoTable   = "bil_contract";
        updateSQL  = " refund_batch_no  = ?,";
        updateSQL += " refund_flag      = 'Y',";
        updateSQL += " refund_apr_flag  = 'Y',";
        updateSQL += " refund_qty       = 1,";
        //updateSQL += " unit_price       = 0,";
        //updateSQL += " remd_amt         = 0,";
        //updateSQL += " first_remd_amt   = 0,";
        //updateSQL += " redeem_amt       = 0,";
        //updateSQL += " redeem_point     = 0,";
        updateSQL += " refund_reference_no = ?,";
        updateSQL += " refund_apr_date     = ?,";
        updateSQL += " post_cycle_dd       = ? ";
        whereStr = "where rowid            = ? ";
        setString(1, hCurpBatchNo);
        setString(2, hCurpReferenceNo);
        setString(3, hSystemDate);
        setString(4, comc.getSubString(hBusinessDate,6,8));  //異動入帳日,後續BilD003會接著把剩餘期數入帳
        setRowId(5, hContRowid);
        updateTable();
        if (notFound.equals("Y")) {
            showLogMessage("E","","update_bil_contract not found!,referenceNo=["+ hCurpReferenceNo +"]");
        }

        hBlogBackTerm = 0;
        hBlogUnitPriceNew = 0;
        hBlogRemdAmtNew = 0;
        if (comc.getSubString(hContFeeFlag, 0, 1).equals("F")) {
            hBlogBackAmt = (hBlogUnitPriceOld - hBlogUnitPriceNew) * hBlogCurrTerm
                    + hContFirstRemdAmt;
        } else {
            hBlogBackAmt = (hBlogUnitPriceOld - hBlogUnitPriceNew) * hBlogCurrTerm
                    + (hBlogRemdAmtOld - hBlogRemdAmtNew);
        }

        //要保留改成"P",讓交易說明顯示在帳單上
        //hCurpContractFlag = "Y"; 
        
        
        if (hBlogCurrTerm >= 0) {
            if (hBlogCurrTerm == 0) {
                hBlogBackAmt = 0;
            }

            cntBack++;
            setValue("contract_no", hBlogContractNo);
            setValueInt("contract_seq_no", 1);
            setValue("card_no", hBlogCardNo);
            setValue("product_no", hBlogProductNo);
            setValue("back_kind", "1");
            setValue("mcht_no", hBlogMerchantNo);
            setValueInt("refund_qty", 1);
            setValueDouble("back_amt", hBlogBackAmt);
            setValueDouble("curr_term", hBlogCurrTerm);
            setValueDouble("back_term", hBlogBackTerm);
            setValue("contract_kind", "1");
            setValueDouble("unit_price_old", hBlogUnitPriceOld);
            setValueDouble("unit_price_new", hBlogUnitPriceNew);
            setValueDouble("remd_amt_old"  , hBlogRemdAmtOld);
            setValueDouble("remd_amt_new"  , hBlogRemdAmtNew);
            setValueDouble("remd_amt_tail" , hBlogRemdAmtTail);
            setValueDouble("clt_fees_amt"  , hContCltFeesAmt);
            setValueDouble("clt_unit_price", hContCltUnitPrice);
            setValueDouble("clt_remd_amt"  , hContCltRemdAmt);
            setValue("post_flag", "Y");
            setValue("cps_flag" , hContCpsFlag);
            setValue("fee_flag" , hContFeeFlag);
            setValueDouble("redeem_point", hBlogRedeemPoint);
            setValueDouble("redeem_amt"  , hBlogRedeemAmt);
            setValue("mod_user"          , hModUser);
            setValue("mod_time"          , sysDate + sysTime);
            setValue("mod_pgm"           , prgmId);
            daoTable = "bil_back_log";
            insertTable();
            if (dupRecord.equals("Y")) {
                showLogMessage("E","","insert_bil_back_log duplicate,contractNo=["+ hBlogContractNo + "]");
            }
        }

        return 0;
    }

    /**********************************************************************************/
    void initBilContract() {
        hContContractNo = "";
        hContContractSeqNo = 0;
        hContCardNo = "";
        hContBackCardNo = "";
        hContNewCardNo = "";
        hContAcctType = "";
        hContAcctKey = "";
        hContIdPSeqno = "";
        hContPSeqno = "";
        hContAcnoPSeqno = "";
        hContStmtCycle = "";
        hContProductNo = "";
        hContProductName = "";
        hContMerchantNo = "";
        hContMerchantChiName = "";
        hContContractKind = "";
        hContAllocateFlag = "";
        hContCvv2 = "";
        hContExchangeAmt = 0;
        hContAgainstNum = 0;
        hContUnitPrice = 0;
        hContQty = 0;
        hContTotAmt = 0;
        hContInstallTotTerm = 0;
        hContRemdAmt = 0;
        hContAutoDelvFlag = "";
        hContFeesFixAmt = 0;
        hContFeesRate = 0;
        hContExtraFees = 0;
        hContFirstPostDate = "";
        hContPostCycleDd = 0;
        hContInstallCurrTerm = 0;
        hContAllPostFlag = "N";
        hContRefundFlag = "";
        hContRefundQty = 0;
        hContRefundConfirmFlag = "";
        hContRefundConfirmDate = "";
        hContConfirmDate = "";
        hContConfirmFlag = "";
        hContReceiveName = "";
        hContReceiveTel = "";
        hContReceiveTel1 = "";
        hContVoucherHead = "";
        hContUniformNo = "";
        hContZipCode = "";
        hContReceiveAddress = "";
        hContDelvDate = "";
        hContDelvConfirmFlag = "";
        hContDelvConfirmDate = "";
        hContRegisterNo = "";
        hContAuthorization = "";
        hContDelvBatchNo = "";
        hContForcedPostFlag = "";
        hContInstallBackTerm = 0;
        hContInstallBackTermFlag = "";
        hContDevFlag20 = "";
        hContPrtFlag21 = "";
        hContLimitEndDate = "";
        hContCltFeesAmt = 0;
        hContCltUnitPrice = 0;
        hContCltInstallTotTerm = 0;
        hContCltRemdAmt = 0;
        hInstallmentFlag = "";
        hProdCltFeesFixAmt = 0;
        hProdCltInterestRate = 0;
        hProdCltFeesMinAmt = 0;
        hProdCltFeesMaxAmt = 0;
        hProdTransRate = 0;
        hProdYearFeesRate = 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA030 proc = new BilA030();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
