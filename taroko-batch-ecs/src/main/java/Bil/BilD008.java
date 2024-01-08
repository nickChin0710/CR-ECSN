/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 106/06/01  V1.00.00    Edson      program initial                          *
* 107/02/12  V1.00.01    mega       BECS-1070125-010 installment_kind 歸類調整 *
* 109/11/26  V1.00.02    shiyuqi    updated for project coding standard      *  
* 111/09/22  V1.00.03    JeffKung   updated for TCB                          *
*****************************************************************************/

package Bil;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*分期付款轉換處理程式*/
public class BilD008 extends AccessDAO {
    private String progname = "分期付款轉換處理程式   111/09/22 V1.00.03 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String rptIdR1 = "BIL_D008R0";
    String rptName1 = "郵購/分期請款媒體入檔合計表";
    String prgmId = "BilD008";

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq111 = 0;
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hBusiOnlineDate = "";
    String hBusiBusinessDate = "";
    String hTempSysdate = "";
    String hBiunConfFlag = "";
    String hBiunAuthFlag = "";
    int hPostBatchSeq = 0;
    String hCurpInstallmentSource = "";
    String hConpProductName = "";
    String hConpMchtNo = "";
    String hConpContractNo = "";
    int hConpInstallCurrTerm = 0;
    double hConpCurrPostAmt = 0;
    String hPbtbBinType = "";
    String hConpRowid = "";
    String hCurpReferenceNo = "";
    String hTempKindAmt = "";
    int hTempTxnCode = 0;
    String hPbtbCurrCode = "";
    String hPostBatchNo = "";
    String hCurpAcexterDesc = "";
    String hTempStrseq = "";
    int hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hPostConfirmFlag = "";
    String hPrintName = "";
    String hRptName = "";

    String[] hBityAcctCode = new String[5];
    String[] hBityAcctItem = new String[5];
    String[] hBityFeesState = new String[5];
    double[] hBityFeesFixAmt = new double[5];
    double[] hBityFeesPercent = new double[5];
    double[] hBityFeesMin = new double[5];
    double[] hBityFeesMax = new double[5];
    String[] hBityFeesBillType = new String[5];
    String[] hBityFeesTxnCode = new String[5];
    String[] hBityExterDesc = new String[5];
    String[] hBityInterestMode = new String[5];
    String[] hBityAdvWkday = new String[5];
    String[] hBityBalanceState = new String[5];
    String[] hBityCollectionMode = new String[5];
    String[] hBityCashAdvState = new String[5];
    String[] hBityEntryAcct = new String[5];
    String[] hBityChkErrBill = new String[5];
    String[] hBityDoubleChk = new String[5];
    String[] hBityFormatChk = new String[5];
    String[] hBitySignFlag = new String[5];
    double[] hBityMerchFee = new double[5];
    String[] hPcodQueryType = new String[5];
    String[] hPcodChiShortName = new String[5];
    String[] hPcodEngShortName = new String[5];
    String[] hPcodItemOrderNormal = new String[5];
    String[] hPcodItemOrderBackDate = new String[5];
    String[] hPcodItemOrderRefund = new String[5];
    String[] hPcodItemClassNormal = new String[5];
    String[] hPcodItemClassBackDate = new String[5];
    String[] hPcodItemClassRefund = new String[5];
    String[] hPcodAcctMethod = new String[5];

    int[] hTempCnt = new int[5];
    String[] hConpKindAmt = new String[5];
    double[] hTempAmt = new double[5];

    String hModUser = "";
    String tmpstr     = "";
    int bilContpostCnt = 0;
    int totalCnt = 0;
    // *******************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : BilD008 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();

            selectPtrBillunit();
            selectPtrBilltype();
            selectBilPostcntl();

            selectBilContpost1();
            printReport();

            String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptIdR1, hTempSysdate);
            filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
            //只出線上報表
            comcr.insertPtrBatchRpt(lpar1);
            //comc.writeReport(filename, lpar1);
            comcr.insertEcsReportLog(prgmId, rptIdR1, rptName1, sysDate, "2", "2");

            tmpstr = String.format("%8sOI%04d", hBusiOnlineDate, hPostBatchSeq);
            hPostBatchNo = tmpstr;
            hPostTotRecord = 0;
            hPostTotAmt = 0;
            selectBilContpost();
            insertBilPostcntl();

            showLogMessage("I", "", String.format("Total process [%d] records\n", totalCnt));
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
        sqlCmd = "select online_date,";
        sqlCmd += "business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_temp_sysdate ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiOnlineDate = getValue("online_date");
            hBusiBusinessDate = getValue("business_date");
            hTempSysdate = getValue("h_temp_sysdate");
        }
        hModUser = comc.commGetUserID();
    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";
        hBiunAuthFlag = "";
        sqlCmd = "select conf_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = 'OI' ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
         }

    }

    /***********************************************************************/
    void selectPtrBilltype() throws Exception {
        int ptrBilltypeCnt = 0;
        /*** 由4類增為5類 ***/
        for (int inta = 0; inta < 5; inta++) {
            hBityAcctCode[inta] = "";
            hBityAcctItem[inta] = "";
            hBityFeesState[inta] = "";
            hBityFeesFixAmt[inta] = 0;
            hBityFeesPercent[inta] = 0;
            hBityFeesMin[inta] = 0;
            hBityFeesMax[inta] = 0;
            hBityFeesBillType[inta] = "";
            hBityFeesTxnCode[inta] = "";
            hBityExterDesc[inta] = "";
            hBityInterestMode[inta] = "";
            hBityAdvWkday[inta] = "";
            hBityBalanceState[inta] = "";
            hBityCollectionMode[inta] = "";
            hBityCashAdvState[inta] = "";
            hBityEntryAcct[inta] = "";
            hBityChkErrBill[inta] = "";
            hBityDoubleChk[inta] = "";
            hBityFormatChk[inta] = "";
            hBitySignFlag[inta] = "";
            hBityMerchFee[inta] = 0;
            hPcodQueryType[inta] = "";
            hPcodChiShortName[inta] = "";
            hPcodEngShortName[inta] = "";
            hPcodItemOrderNormal[inta] = "";
            hPcodItemOrderBackDate[inta] = "";
            hPcodItemOrderRefund[inta] = "";
            hPcodItemClassNormal[inta] = "";
            hPcodItemClassBackDate[inta] = "";
            hPcodItemClassRefund[inta] = "";
            hPcodAcctMethod[inta] = "";
        }

        sqlCmd = "select b.acct_code,";
        sqlCmd += "b.acct_item,";
        sqlCmd += "b.fees_state,";
        sqlCmd += "b.fees_fix_amt,";
        sqlCmd += "b.fees_percent,";
        sqlCmd += "b.fees_min,";
        sqlCmd += "b.fees_max,";
        sqlCmd += "b.fees_bill_type,";
        sqlCmd += "b.fees_txn_code,";
        sqlCmd += "b.exter_desc,";
        sqlCmd += "b.interest_mode,";
        sqlCmd += "b.adv_wkday,";
        sqlCmd += "b.balance_state,";
        // sqlCmd += "b.collection_mode,";
        sqlCmd += "b.cash_adv_state,";
        sqlCmd += "b.entry_acct,";
        sqlCmd += "b.chk_err_bill,";
        sqlCmd += "b.double_chk,";
        sqlCmd += "b.format_chk,";
        sqlCmd += "b.merch_fee,";
        sqlCmd += "b.sign_flag,";
        sqlCmd += "a.query_type,";
        sqlCmd += "a.chi_short_name,";
        sqlCmd += "a.eng_short_name,";
        sqlCmd += "a.item_order_normal,";
        sqlCmd += "a.item_order_back_date,";
        sqlCmd += "a.item_order_refund,";
        sqlCmd += "a.item_class_normal,";
        sqlCmd += "a.item_class_back_date,";
        sqlCmd += "a.item_class_refund,";
        sqlCmd += "a.acct_method ";
        sqlCmd += " from ptr_actcode a,ptr_billtype b  ";
        sqlCmd += "where a.acct_code = b.acct_code  ";
        sqlCmd += "  and  b.bill_type = 'OICU'  ";
        /*** 原 IN0、PF1、PO2、RI3 ***/
        /*** 新IF0、IN1、PF2、PO3、RI4 ***/
        sqlCmd += "and  b.txn_code in ('PF','IN','PO','RI','IF') ORDER by b.txn_code ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hBityAcctCode[i] = getValue("acct_code", i);
            hBityAcctItem[i] = getValue("acct_item", i);
            hBityFeesState[i] = getValue("fees_state", i);
            hBityFeesFixAmt[i] = getValueDouble("fees_fix_amt", i);
            hBityFeesPercent[i] = getValueDouble("fees_percent", i);
            hBityFeesMin[i] = getValueDouble("fees_min", i);
            hBityFeesMax[i] = getValueDouble("fees_max", i);
            hBityFeesBillType[i] = getValue("fees_bill_type", i);
            hBityFeesTxnCode[i] = getValue("fees_txn_code", i);
            hBityExterDesc[i] = getValue("exter_desc", i);
            hBityInterestMode[i] = getValue("interest_mode", i);
            hBityAdvWkday[i] = getValue("adv_wkday", i);
            hBityBalanceState[i] = getValue("balance_state", i);
            // h_bity_collection_mode[i] = getValue("collection_mode", i);
            hBityCashAdvState[i] = getValue("cash_adv_state", i);
            hBityEntryAcct[i] = getValue("entry_acct", i);
            hBityChkErrBill[i] = getValue("chk_err_bill", i);
            hBityDoubleChk[i] = getValue("double_chk", i);
            hBityFormatChk[i] = getValue("format_chk", i);
            hBitySignFlag[i] = getValue("sign_flag", i);
            hBityMerchFee[i] = getValueDouble("merch_fee", i);
            hPcodQueryType[i] = getValue("query_type", i);
            hPcodChiShortName[i] = getValue("chi_short_name", i);
            hPcodEngShortName[i] = getValue("eng_short_name", i);
            hPcodItemOrderNormal[i] = getValue("item_order_normal", i);
            hPcodItemOrderBackDate[i] = getValue("item_order_back_date", i);
            hPcodItemOrderRefund[i] = getValue("item_order_refund", i);
            hPcodItemClassNormal[i] = getValue("item_class_normal", i);
            hPcodItemClassBackDate[i] = getValue("item_class_back_date", i);
            hPcodItemClassRefund[i] = getValue("item_class_refund", i);
            hPcodAcctMethod[i] = getValue("acct_method", i);
        }

        ptrBilltypeCnt = recordCnt;

        /*** 由4類增為5類 ***/
        if (ptrBilltypeCnt != 5) {
            stderr = String.format("select ptr_billtype not found error\n");
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectBilPostcntl() throws Exception {
        hPostBatchSeq = 0;
        sqlCmd = "select max(batch_seq) h_post_batch_seq ";
        sqlCmd += " from bil_postcntl  ";
        sqlCmd += "where batch_unit = 'OI'  ";
        sqlCmd += "and batch_date = ? ";
        setString(1, hBusiOnlineDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_bil_postcntl not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPostBatchSeq = getValueInt("h_post_batch_seq");
        }

        hPostBatchSeq++;
    }

    /***********************************************************************/
    void selectBilContpost1() throws Exception {
        for (int inta = 0; inta < 3; inta++) {
            hTempCnt[inta] = 0;
            hConpKindAmt[inta] = "";
            hTempAmt[inta] = 0;
        }
        sqlCmd = "select count(*) h_temp_cnt,";
        sqlCmd += "decode(kind_amt,'1','0','2','1','3','1','2') h_conp_kind_amt,";
        sqlCmd += "sum(CURR_POST_AMT) h_temp_amt ";
        sqlCmd += " from bil_contpost  ";
        sqlCmd += "where decode(post_flag,'','N',post_flag) != 'Y'  ";
        sqlCmd += "  and CURR_POST_AMT > 0 group by decode(kind_amt,'1','0','2','1','3','1','2') ";
        int recordCnt = selectTable();
        
        for (int i = 0; i < recordCnt; i++) {
            hTempCnt[i]      = getValueInt("h_temp_cnt", i);
            hConpKindAmt[i] = getValue("h_conp_kind_amt", i);
            hTempAmt[i]      = getValueDouble("h_temp_amt", i);
        }

        bilContpostCnt = recordCnt;
    }

    /***********************************************************************/
    void printReport() {
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: " + rptIdR1, 1);

        szTmp = String.format("%22s", "郵購/分期 請款媒體入檔合計表");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        szTmp = String.format("%8d", comcr.str2long(hBusiBusinessDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 70);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate + sysTime, ++rptSeq111, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "筆   數 金         額 交易別   批          號 過帳日期", 26);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate + sysTime, ++rptSeq111, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "======= ============= ======== ============== ========", 26);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate + sysTime, ++rptSeq111, "0", buf));

        for (int inta = 0; inta < 3; inta++) {
            buf = "";
            szTmp = "請款媒體入檔合計:";
            buf = comcr.insertStr(buf, szTmp, 02);
            szTmp = comcr.commFormat("3z,3z", hTempCnt[inta]);
            buf = comcr.insertStr(buf, szTmp, 25);
            szTmp = comcr.commFormat("3$,3$,3$", hTempAmt[inta]);
            buf = comcr.insertStr(buf, szTmp, 34);

            switch (comcr.str2int(hConpKindAmt[inta])) {
            case 0:
                buf = comcr.insertStr(buf, "本金", 48);
                break;
            case 1:
                buf = comcr.insertStr(buf, "手續費", 48);
                break;
            case 2:
                buf = comcr.insertStr(buf, "利息", 48);
                break;
            }

            szTmp = String.format("%8sOI%04d", hBusiOnlineDate, hPostBatchSeq);
            buf = comcr.insertStr(buf, szTmp, 57);
            szTmp = String.format("%8d", comcr.str2long(hBusiOnlineDate));
            buf = comcr.insertStr(buf, szTmp, 72);
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate + sysTime, ++rptSeq111, "0", buf));
        }
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate + sysTime, ++rptSeq111, "0", ""));
    }

    /***********************************************************************/
    void selectBilContpost() throws Exception {
        PreparedStatement ips = getInsertBilCurPostPs();
        /*** 需依select_ptr_billtype()結果將txn_code排序編號 ***/
        /*** 原 IN0、PF1、PO2、RI3 ***/
        /*** 新IF0、IN1、PF2、PO3、RI4 ***/
        sqlCmd = "select ";
        sqlCmd += "b.kind_amt ,";
        sqlCmd += "decode(b.kind_amt,'5',0,'3',2,'4',4,decode(b.contract_kind,'1',1,3)) h_temp_txn_code,";
        sqlCmd += "decode(b.kind_amt,'3','F',decode(b.contract_kind,'1',decode(upper(b.cps_flag),'Y','C','C','N','N','I',''),'P')) h_curp_installment_source,";
        sqlCmd += "b.product_name,";
        sqlCmd += "b.contract_no,";
        sqlCmd += "b.install_curr_term ,";
        sqlCmd += "b.mcht_no,";
        sqlCmd += "b.curr_post_amt,";
        sqlCmd += "b.rowid  as rowid,";
        sqlCmd += "c.bin_type ";
        sqlCmd += " from crd_card     c,bil_contpost b ";
        sqlCmd += "where c.card_no     = b.card_no      ";
        sqlCmd += "  and decode(b.post_flag,'','N',b.post_flag) != 'Y' ";

        //int recordCnt = selectTable();
        
        
        //for (int i = 0; i < recordCnt; i++) {
        	
        openCursor();
    	while (fetchTable()) {
            hTempKindAmt = getValue("kind_amt");
            hTempTxnCode = getValueInt("h_temp_txn_code");
            hCurpInstallmentSource = getValue("h_curp_installment_source");
            hConpProductName = getValue("product_name");
            hConpMchtNo = getValue("mcht_no");
            hConpContractNo = getValue("contract_no");
            hConpInstallCurrTerm = getValueInt("install_curr_term");
            hConpCurrPostAmt = getValueDouble("curr_post_amt");
            hConpRowid = getValue("rowid");
            hPbtbCurrCode = "901";
            hPbtbBinType = getValue("bin_type");

            totalCnt++;
            if (totalCnt % 5000 == 0 || totalCnt == 1)
                showLogMessage("I", "", "Current Process record=" + totalCnt);
            
            if (hConpCurrPostAmt > 0) {
                hPostTotRecord++;
                hPostTotAmt = hPostTotAmt + hConpCurrPostAmt;
            }

            hCurpAcexterDesc = hBityExterDesc[hTempTxnCode];
            /*** 需依select_ptr_billtype()結果將txn_code排序編號 ***/
            /*** 原 IN0、PF1、PO2、RI3 ***/
            /*** 新 IF0、IN1、PF2、PO3、RI4 ***/
            if ((hTempTxnCode == 2) || (hTempTxnCode == 4) || (hTempTxnCode == 0)) {
                hCurpAcexterDesc = String.format("%-20.20s%-19.19s"
                                    , hBityExterDesc[hTempTxnCode], hConpProductName);
            }
            selectBilPostseq();
            insertBilCurpost(ips);
            deleteBilContpost();
        }
    	closeCursor();
        ips.close();
    }

    PreparedStatement getInsertBilCurPostPs() {
        sqlCmd = "insert into bil_curpost ";
        sqlCmd += "(reference_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "card_no,";
        sqlCmd += "film_no,";
        sqlCmd += "acq_member_id,";
        sqlCmd += "purchase_date,";
        sqlCmd += "dest_amt,";//8
        sqlCmd += "dest_curr,";//9
        sqlCmd += "source_amt,";//10
        sqlCmd += "source_curr,";//11
        sqlCmd += "reference_no_fee_f,";//12
        sqlCmd += "mcht_eng_name,";//13
        sqlCmd += "mcht_city,";
        sqlCmd += "mcht_country,";
        sqlCmd += "mcht_category,";
        sqlCmd += "mcht_zip,";
        sqlCmd += "mcht_state,";
        sqlCmd += "settl_amt,";
        sqlCmd += "auth_code,";
        sqlCmd += "ptr_merchant_no,";
        sqlCmd += "mcht_no,";
        sqlCmd += "mcht_chi_name,";
        sqlCmd += "contract_no,";
        sqlCmd += "contract_seq_no,";
        sqlCmd += "contract_amt,";
        sqlCmd += "post_amt,";
        sqlCmd += "term,";
        sqlCmd += "total_term,";
        sqlCmd += "batch_no,";
        sqlCmd += "acct_code,";
        sqlCmd += "acct_item,";
        sqlCmd += "acct_eng_short_name,";
        sqlCmd += "acct_chi_short_name,";
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "acexter_desc,";
        sqlCmd += "entry_acct,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "collection_mode,";
        sqlCmd += "fees_state,";
        sqlCmd += "cash_adv_state,";
        sqlCmd += "this_close_date,";
        sqlCmd += "valid_flag,";
        sqlCmd += "acct_type,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "major_card_no,";
        sqlCmd += "curr_code,";
        sqlCmd += "promote_dept,";
        sqlCmd += "issue_date,";
        sqlCmd += "prod_no,";
        sqlCmd += "group_code,";
        sqlCmd += "bin_type,";
        sqlCmd += "p_seqno,";
        sqlCmd += "major_id_p_seqno,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "tx_convt_flag,";
        sqlCmd += "acctitem_convt_flag,";
        sqlCmd += "format_chk_ok_flag,";
        sqlCmd += "double_chk_ok_flag,";
        sqlCmd += "err_chk_ok_flag,";
        sqlCmd += "sign_flag,";
        sqlCmd += "source_code,";
        sqlCmd += "install_tot_term,";
        sqlCmd += "install_per_amt,";
        sqlCmd += "install_first_amt,";
        sqlCmd += "install_fee,";
        sqlCmd += "payment_type,";
        sqlCmd += "installment_source,";
        sqlCmd += "deduct_bp,";
        sqlCmd += "installment_kind,";
        sqlCmd += "contract_flag,";
        sqlCmd += "merge_flag,";
        sqlCmd += "v_card_no,";
        sqlCmd += "pos_entry_mode,";
        sqlCmd += "ec_ind,";
        sqlCmd += "electronic_term_ind,";
        sqlCmd += "dc_amount,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        // ****************************************************************************
        sqlCmd += " select ";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "'OICU',";
        sqlCmd += "decode(cast(? as int),0,'IF',1,'IN',2,'PF',3,'PO',4,'RI','XX'),";
        sqlCmd += "b.card_no,";
        sqlCmd += "nvl(a.film_no,''),";
        sqlCmd += "nvl(a.acquirer_member_id,''),";
        sqlCmd += "nvl(decode(a.purchase_date,'',a.first_post_date,a.purchase_date),''),";
        sqlCmd += "b.curr_post_amt,";//8
        sqlCmd += "cast(? as varchar(20)),";//h_pbtb_curr_code 9
        sqlCmd += "decode(cast(? as int), 1, decode(nvl(decode(a.installment_kind, 'F', e.source_curr, f.source_curr), cast(? as varchar(20))), '901', b.curr_post_amt, nvl(decode(b.install_curr_term, ' 1', decode(a.installment_kind, 'F', e.source_amt, f.source_amt) - (trunc(decode(a.installment_kind, 'F', e.source_amt, f.source_amt) * a.unit_price / decode(a.installment_kind, 'F', e.dest_amt, f.dest_amt), CASE WHEN decode(a.installment_kind, 'F', e.dest_amt, f.dest_amt) <= decode(a.installment_kind, 'F', e.source_amt, f.source_amt) THEN 0 ELSE 2 END) * (a.install_tot_term - 1)), trunc(decode(a.installment_kind, 'F', e.source_amt, f.source_amt) * a.unit_price / decode(a.installment_kind, 'F', e.dest_amt, f.dest_amt), CASE WHEN decode(a.installment_kind, 'F', e.dest_amt, f.dest_amt) <= decode(a.installment_kind, 'F', e.source_amt, f.source_amt) THEN 0 ELSE 2 END)), b.curr_post_amt)), b.curr_post_amt), ";
        sqlCmd += "decode(cast(? as int), 1, nvl(decode(a.installment_kind, 'F', e.source_curr, f.source_curr), cast(? as varchar(20))), cast(? as varchar(20))), ";
        sqlCmd += "nvl(decode(a.installment_kind,'F',e.reference_no_fee_f,f.reference_no_fee_f),''),";
        sqlCmd += "b.mcht_eng_name,";//13
        sqlCmd += "b.mcht_city,";
        sqlCmd += "b.mcht_country,";
        sqlCmd += "b.mcht_category,";
        sqlCmd += "b.mcht_zip,";
        sqlCmd += "b.mcht_state,";
        sqlCmd += "'0',";
        sqlCmd += "nvl(a.auth_code,''),";
        sqlCmd += "nvl(a.ptr_mcht_no,''),";
        sqlCmd += "b.mcht_no,";
        sqlCmd += "b.mcht_chi_name,";
        sqlCmd += "b.contract_no,";
        sqlCmd += "b.contract_seq_no,";
        sqlCmd += "nvl(decode(b.kind_amt,'2',a.extra_fees,decode(a.refund_apr_flag,'Y',(a.qty-a.refund_qty)*a.tot_amt,a.qty*a.tot_amt)),0),";
        sqlCmd += "nvl(decode(b.kind_amt,'2',a.extra_fees,a.install_tot_term*a.unit_price+a.remd_amt+a.first_remd_amt),0), ";
        sqlCmd += "b.install_curr_term,";
        sqlCmd += "nvl(decode(b.kind_amt,'2',0,a.install_tot_term),0),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as VARGRAPHIC(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as VARGRAPHIC(60)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "d.acct_type,";
        sqlCmd += "d.stmt_cycle,";
        sqlCmd += "c.major_card_no,";
        sqlCmd += "'901', ";  // bin_table curr_code
        sqlCmd += "c.promote_dept,";
        sqlCmd += "c.issue_date,";
        sqlCmd += "b.product_no,";
        sqlCmd += "c.group_code,";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "c.p_seqno,";
        sqlCmd += "c.major_id_p_seqno,";
        sqlCmd += "c.acno_p_seqno,";
        sqlCmd += "c.id_p_seqno,";
        sqlCmd += "'Y',";
        sqlCmd += "'Y',";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "c.source_code,";
        sqlCmd += "nvl(decode(b.kind_amt,'2',0,a.install_tot_term),0),";
        sqlCmd += "nvl(decode(b.kind_amt,'2',0,a.unit_price),0),";
        sqlCmd += "nvl(decode(b.kind_amt,'2',0,a.first_remd_amt+a.unit_price),0),";
        sqlCmd += "nvl(decode(b.kind_amt,'2',0,a.clt_fees_amt),0),";
        sqlCmd += "nvl(case when(a.redeem_point>0)or(a.redeem_point>0) ";
        sqlCmd += " then '2' else a.payment_type end,''),";
        sqlCmd += "cast(? as varchar(20)),";
        sqlCmd += "nvl(decode(b.kind_amt,'2',0,a.redeem_point),0),";
        sqlCmd += "nvl(a.installment_kind,''),";
        sqlCmd += "'P',";
        sqlCmd += "nvl(decode(a.merge_flag,'','N',a.merge_flag),''),";
        sqlCmd += "nvl(a.v_card_no,''),";
        sqlCmd += "nvl(decode(a.installment_kind,'F',e.pos_entry_mode,f.pos_entry_mode),''),";
        sqlCmd += "nvl(decode(a.installment_kind,'F',e.ec_ind        ,f.ec_ind        ),''),";
        sqlCmd += "nvl(decode(a.installment_kind,'F',e.electronic_term_ind,f.electronic_term_ind),''),";
        sqlCmd += "b.curr_post_amt,";
        sqlCmd += "sysdate,";
        sqlCmd += "cast(? as varchar(20)) ";
        sqlCmd += "from  bil_contpost b left join bil_contract a on a.contract_no = b.contract_no "
                + "and a.contract_seq_no = b.contract_seq_no "
                + "left join crd_card c     on c.card_no      = b.card_no "
                + "left join act_acno d     on d.acno_p_seqno = c.acno_p_seqno "
                + "left join bil_bill e     on a.reference_no = e.reference_no "
                + "left join bil_curpost f  on a.reference_no = f.reference_no ";
        sqlCmd += " where b.contract_no       = ? ";
        sqlCmd += "   and b.install_curr_term = ? ";
        sqlCmd += "   and b.kind_amt          = ? ";
        PreparedStatement ips = initPs(sqlCmd);

        return ips;
    }

    /***********************************************************************/
    void selectBilPostseq() throws Exception {
        hTempStrseq = "";
        sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_temp_strseq ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempStrseq = getValue("h_temp_strseq");
        }
        else
        {
         comcr.errRtn("select_bil_postseq not found!", "", hCallBatchSeqno);
        }
        hCurpReferenceNo = hBusiOnlineDate.substring(2, 4) + hTempStrseq;
        
    }

    /***********************************************************************/
    void insertBilCurpost(PreparedStatement pPS) throws Exception {

        pPS.setString(1 , hCurpReferenceNo);
        
        pPS.setInt(2    , hTempTxnCode);
        
        pPS.setString(3 , hPbtbCurrCode);
        pPS.setInt(4    , hTempTxnCode);
        pPS.setString(5 , hPbtbCurrCode);
        pPS.setInt(6    , hTempTxnCode);
        pPS.setString(7 , hPbtbCurrCode);
        pPS.setString(8 , hPbtbCurrCode);
        
        pPS.setString(9 , hPostBatchNo);
 
        pPS.setString(10 , hBityAcctCode[hTempTxnCode]           );
        pPS.setString(11 , hBityAcctItem[hTempTxnCode]           );
        pPS.setString(12 , hPcodEngShortName[hTempTxnCode]      );
        pPS.setString(13 , hPcodChiShortName[hTempTxnCode]      );
        pPS.setString(14, hPcodItemOrderNormal[hTempTxnCode]   );
        pPS.setString(15, hPcodItemOrderBackDate[hTempTxnCode]);
        pPS.setString(16, hPcodItemOrderRefund[hTempTxnCode]   );
        pPS.setString(17, hCurpAcexterDesc);
        pPS.setString(18, hBityEntryAcct[hTempTxnCode]          );
        pPS.setString(19, hPcodItemClassNormal[hTempTxnCode]   );
        pPS.setString(20, hPcodItemClassBackDate[hTempTxnCode]);
        pPS.setString(21, hPcodItemClassRefund[hTempTxnCode]   );
        pPS.setString(22, hBityCollectionMode[hTempTxnCode]     );
        pPS.setString(23, hBityFeesState[hTempTxnCode]          );
        pPS.setString(24, hBityCashAdvState[hTempTxnCode]      );
        pPS.setString(25, hBusiOnlineDate);
        pPS.setString(26, hBiunAuthFlag);
        pPS.setString(27, hPbtbBinType);
        pPS.setString(28, hBityFormatChk[hTempTxnCode]          );
        pPS.setString(29, hBityDoubleChk[hTempTxnCode]          );
        pPS.setString(30, hBityChkErrBill[hTempTxnCode]        );
        pPS.setString(31, hBitySignFlag[hTempTxnCode]           );
        pPS.setString(32, hCurpInstallmentSource);
        pPS.setString(33, prgmId                                      );

        pPS.setString(34, hConpContractNo);
        pPS.setInt   (35, hConpInstallCurrTerm);
        pPS.setString(36, hTempKindAmt);
        
		try {
			pPS.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
			showLogMessage("I", "", "TXN_CODE=" + hTempTxnCode);
			showLogMessage("I", "", hCurpReferenceNo);
			showLogMessage("I", "", hTempTxnCode + "");
			showLogMessage("I", "", hPbtbCurrCode);
			showLogMessage("I", "", hTempTxnCode + "");
			showLogMessage("I", "", hPbtbCurrCode);
			showLogMessage("I", "", hTempTxnCode + "");
			showLogMessage("I", "", hPbtbCurrCode);
			showLogMessage("I", "", hPbtbCurrCode);
			showLogMessage("I", "", hPostBatchNo);
			showLogMessage("I", "", hBityAcctCode[hTempTxnCode]);
			showLogMessage("I", "", hBityAcctItem[hTempTxnCode]);
			showLogMessage("I", "", hPcodEngShortName[hTempTxnCode]);
			showLogMessage("I", "", hPcodChiShortName[hTempTxnCode]);
			showLogMessage("I", "", hPcodItemOrderNormal[hTempTxnCode]);
			showLogMessage("I", "", hPcodItemOrderBackDate[hTempTxnCode]);
			showLogMessage("I", "", hPcodItemOrderRefund[hTempTxnCode]);
			showLogMessage("I", "", hCurpAcexterDesc);
			showLogMessage("I", "", hBityEntryAcct[hTempTxnCode]);
			showLogMessage("I", "", hPcodItemClassNormal[hTempTxnCode]);
			showLogMessage("I", "", hPcodItemClassBackDate[hTempTxnCode]);
			showLogMessage("I", "", hPcodItemClassRefund[hTempTxnCode]);
			showLogMessage("I", "", hBityCollectionMode[hTempTxnCode]);
			showLogMessage("I", "", hBityFeesState[hTempTxnCode]);
			showLogMessage("I", "", hBityCashAdvState[hTempTxnCode]);
			showLogMessage("I", "", hBusiOnlineDate);
			showLogMessage("I", "", hBiunAuthFlag);
			showLogMessage("I", "", hPbtbBinType);
			showLogMessage("I", "", hBityFormatChk[hTempTxnCode]);
			showLogMessage("I", "", hBityDoubleChk[hTempTxnCode]);
			showLogMessage("I", "", hBityChkErrBill[hTempTxnCode]);
			showLogMessage("I", "", hBitySignFlag[hTempTxnCode]);
			showLogMessage("I", "", hCurpInstallmentSource);
			showLogMessage("I", "", "pram=" + prgmId);
			showLogMessage("I", "", "h_conp_contract_no=" + hConpContractNo);
			showLogMessage("I", "", "CURR+" + hConpInstallCurrTerm + "");
			showLogMessage("I", "", "AMT=" + hTempKindAmt);
			showLogMessage("I", "", "888 insert curpost=[" + hCurpReferenceNo + "," + hConpContractNo
					+ "]Exception is :" + e.getMessage());
			throw new Exception(e);
		}
    }

    void insertNccc300Dtl() throws Exception {
        sqlCmd = "insert into bil_nccc300_dtl ";
        sqlCmd += "(reference_no,";
        sqlCmd += "card_no,";
        sqlCmd += "SETTLEMENT_AMT,";
        sqlCmd += "pos_pin_capability,";
        sqlCmd += "query_type,";
        sqlCmd += "limit_end_date,";
        sqlCmd += "pos_entry_mode,";
        sqlCmd += "ec_ind,";
        sqlCmd += "electronic_term_ind,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "b.card_no,";
        sqlCmd += "0,";
        sqlCmd += "0,";
        sqlCmd += "?,";
        sqlCmd += "b.limit_end_date,";
        sqlCmd += "nvl(decode(a.installment_kind,'',e.pos_entry_mode,f.pos_entry_mode),''),";
        sqlCmd += "nvl(decode(a.installment_kind,'',e.ec_ind        ,f.ec_ind        ),''),";
        sqlCmd += "nvl(decode(a.installment_kind,'',e.electronic_term_ind,f.electronic_term_ind),''),";
        sqlCmd += "sysdate,";
        sqlCmd += "?";
        sqlCmd += "from  bil_contpost b " 
                + "left join bil_contract a on a.contract_no     = b.contract_no "
                +                        " and a.contract_seq_no = b.contract_seq_no  "
                + "left join crd_card     c on c.card_no         = b.card_no "
                + "left join act_acno     d on d.acno_p_seqno         = c.acno_p_seqno "
                + "left join bil_bill     e on a.reference_no    = e.reference_no "
                + "left join bil_curpost  f on a.reference_no    = f.reference_no ";
        sqlCmd += " where b.rowid    = ? ";

        setString(1, hCurpReferenceNo);
        setString(2, hPcodQueryType[hTempTxnCode]);
        setString(3, prgmId);
        setRowId(4, hConpRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert" + daoTable + "duplicate!", "", hCurpReferenceNo);
        }
    }

    /***********************************************************************/
    void deleteBilContpost() throws Exception {
        daoTable = "bil_contpost";
        whereStr = "where rowid  =? ";
        setRowId(1, hConpRowid);
        deleteTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("delete_bil_contpost not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        setValue("batch_date"   , hBusiOnlineDate);
        setValue("batch_unit"   , "OI");
        setValueInt("batch_seq" , hPostBatchSeq);
        setValueInt("tot_record", hPostTotRecord);
        setValueDouble("tot_amt", hPostTotAmt);
        setValue("confirm_flag_p" , hPostConfirmFlag.toUpperCase(Locale.TAIWAN).equals("Y") ? "N" : "Y");
        setValue("confirm_flag"   , hBiunConfFlag);
        setValue("this_close_date", hBusiOnlineDate);
        setValue("batch_no", hPostBatchNo);
        setValue("mod_user", hModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm" , prgmId);
        daoTable = "bil_postcntl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilD008 proc = new BilD008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
