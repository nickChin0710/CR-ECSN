/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/28  V1.00.01    shiyuqi   updated for project coding standard       * 
*  109/11/30  V1.00.02    JeffKung  updated for TCB                           *
*  111/06/16  V1.00.03    Justin    弱點修正                                                                     *
*  112/01/09  V1.00.04    JeffKung  getCardNo()取欠款最大的卡號                                *
*  112/07/03  V1.00.05    JeffKung  typo src_amt fixed                        *
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*整合各來源費用處理程式*/
public class BilF001 extends AccessDAO {
    private String progname = "整合各來源費用處理程式   112/07/03  V1.00.05";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int totalCnt = 0;
    String hTempUser = "";

    String rptId111   = "BIL_F001R0";
    String rptName111 = "各項費用統計表(一)(系統產生)";

    List<Map<String, Object>> lpar111 = new ArrayList<Map<String, Object>>();

    int rptSeq111 = 0;

    String buf = "";
    String hCallBatchSeqno = "";

    String hPrintName = "";
    String hRptName = "";
    String hSystemDateF = "";
    String hBusiBusinessDate = "";
    String hPbtbCurrCode = "";
    String hPbtbBinType = "";
    String hPbtbBinNo = "";
    String hPcodAcctCode = "";
    String hPcodChiShortName = "";
    String hPcodEngShortName = "";
    String hPcodItemOrderNormal = "";
    String hPcodItemOrderBackDate = "";
    String hPcodItemOrderRefund = "";
    String hPcodItemClassNormal = "";
    String hPcodItemClassBackDate = "";
    String hPcodItemClassRefund = "";
    String hPcodAcctMethod = "";
    String hPcodQueryType = "";
    String hPaccCurrCode = "";
    String hSyseRealCardNo = "";
    String hSyseBillType = "";
    String hSyseTransactionCode = "";
    String hSysePurchaseDate = "";
    String hSyseSourceType = "";
    String hSyseAcctType = "";
    String hSyseAcctKey = "";
    String hSysePSeqno = "";
    double hSyseSourceAmt = 0;
    String hSyseMerchantNo = "";
    String hSyseInstallmentKind = "";
    String hSysePtrMerchantNo = "";
    String hCurpDestinationAmtChar = "";
    double hSyseDestinationAmt = 0;
    String hCurpDestinationCurrency = "";
    String hSyseBillDesc = "";
    String hSysePostFlag = "";
    String hSyseAoFlag = "";
    String hSyseAuthorization = "";
    String hSyseMerchantCategory = "";
    String hSyseMergeFlag = "";
    String hSyseModPgm = "";
    String hSyseModTime = "";
    String hSyseCurrCode = "";
    double hSyseDcDestinationAmt = 0;
    String hSyseRefKey = "";
    String hSyseRowid = "";
    String hBiunConfFlag = "";
    String hBiunAuthFlag = "";
    int hPostBatchSeq = 0;
    int hTempPCount = 0;
    double hTempPSum = 0;
    String hTempTransactionCode = "";
    String hBityExterDesc = "";
    String hBitySignFlag = "";
    String hBityAcctCode = "";
    String hCurpMerchantChiName = "";
    String hCurpMerchantEngName = "";
    String hCurpMerchantZip = "";
    String hCurpMerchantCountry = "";
    String hCurpMerchantCategory = "";
    String hCurpMerchantState = "";
    String hCurpMerchantCity = "";
    String hBityBillType = "";
    String hBityAcctItem = "";
    String hBityFeesState = "";
    double hBityFeesFixAmt = 0;
    String hBityFeesBillType = "";
    String hBityFeesTxnCode = "";
    String hBityInterestMode = "";
    String hBityAdvWkday = "";
    String hBityBalanceState = "";
    String hBityCollectionMode = "";
    String hBityCashAdvState = "";
    String hBityEntryAcct = "";
    String hBityChkErrBill = "";
    String hBityDoubleChk = "";
    String hBityFormatChk = "";
    String hAcnoAcctPSeqno = "";
    String hCardMajorCardNo = "";
    String hCardCurrentCode = "";
    String hCardIssueDate = "";
    String hCardOppostDate = "";
    String hCardPromoteDept = "";
    String hCardProdNo = "";
    String hCardGroupCode = "";
    String hCardSourceCode = "";
    String hCardCardType = "";
    String hCardBinNo = "";
    String hCardBinType = "";
    String hCardAcctPSeqno = "";
    String hCardPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoBlockStatus = "";
    String hAcnoBlockDate = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    String hCurpReferenceNo = "";
    String tempBillType = "";
    String hCurpBatchNo = "";
    double hPostTotAmt = 0;
    String hCurpSourceAmtChar = "";
    String hCurpSourceCurrency = "";
    String hCurpFilmNo = "";
    String hCurpAcexterDesc = "";
    String hCurpContractFlag = "";
    String hCardCardNo = "";
    String hCardIdPSeqno = "";
    String hCardMajorIdPSeqno = "";
    String hCardId = "";
    String hIdnoChiName = "";
    String hMercMerchantChiName = "";
    String hMercMerchantAddress = "";
    String hMercMerchantZip = "";
    String hMercMerchantFax1 = "";
    String hMercMerchantFax11 = "";
    String hMercMerchantAcctName = "";
    String hMercAssignAcct = "";
    String hMercOthBankId = "";
    String hMercClrBankId = "";
    String hMercOthBankName = "";
    String hMercBankName = "";
    String hMercOthBankAcct = "";
    double hCurpDestinationAmt = 0;
    double hCurpDcExchangeRate = 0;
    double hBityFeesPercent = 0;
    double hThisBusiAddAmt = 0;
    long hSrcPgmPostseq = 0;
    String hPostNote = "各來源費用新增金額 by bill_type,txn_code";
    int hBityFeesMin = 0;
    int hBityFeesMax = 0;
    int hBityMerchFee = 0;

    String[] aPcodAcctCode = new String[250];
    String[] aPcodChiShortName = new String[250];
    String[] aPcodEngShortName = new String[250];
    String[] aPcodItemClassNormal = new String[250];
    String[] aPcodItemClassBackDate = new String[250];
    String[] aPcodItemClassRefund = new String[250];
    String[] aPcodItemOrderNormal = new String[250];
    String[] aPcodItemOrderBackDate = new String[250];
    String[] aPcodItemOrderRefund = new String[250];
    String[] aPcodAcctMethod = new String[250];
    String[] aPcodQueryType = new String[250];
    String[] aPbtbCurrCode = new String[250];
    String[] aPbtbBinType = new String[250];
    String[] aPbtbBinNo = new String[250];

    int ptrBintableCnt = 0;
    int ptrActcodeCnt = 0;
    int hPostTotRecord = 0;
    int totalTransactionCnt = 0;
    int totalTransactionAmt = 0;
    int totCnt = 0;

    private String hCardAcctType = "";
    // ***********************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname + "," + args.length);
            // =====================================
            if (comm.isAppActive2(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length != 0 && args.length != 1) {
				        exceptExit = -1;
                comc.errExit("Usage : BilF001 batch_seq", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

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

            selectPtrBusinday();

            selectPtrBintable();
            selectPtrActcode();

            selectPtrAcctType();

            deleteBilSysexp();

            //String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptId111, hSystemDateF);
            //filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
            //comcr.insertPtrBatchRpt(lpar111);
            //comc.writeReport(filename, lpar111);

            // ==============================================
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

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hSystemDateF = "";
        hBusiBusinessDate = "";

        sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_f,";
        sqlCmd += "BUSINESS_DATE ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hSystemDateF = getValue("h_system_date_f");
            hBusiBusinessDate = getValue("BUSINESS_DATE");
        }

    }

    /***********************************************************************/
    void selectPtrBintable() throws Exception {
        for (int i = 0; i < 250; i++) {
            aPbtbCurrCode[i] = "";
            aPbtbBinType[i] = "";
            aPbtbBinNo[i] = "";
        }

        sqlCmd  = "select bin_type,";
        sqlCmd += " bin_no ";
        sqlCmd += " from ptr_bintable ";
        sqlCmd += "group by bin_type, bin_no ";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aPbtbCurrCode[i] = "901";
            aPbtbBinType[i]  = getValue("bin_type", i);
            aPbtbBinNo[i]    = getValue("bin_no", i);
        }

        ptrBintableCnt = recordCnt;
    }

    /***********************************************************************/
    void selectPtrActcode() throws Exception {
        for (int i = 0; i < 250; i++) {
            aPcodAcctCode[i] = "";
            aPcodChiShortName[i] = "";
            aPcodEngShortName[i] = "";
            aPcodItemOrderNormal[i] = "";
            aPcodItemOrderBackDate[i] = "";
            aPcodItemOrderRefund[i] = "";
            aPcodItemClassNormal[i] = "";
            aPcodItemClassBackDate[i] = "";
            aPcodItemClassRefund[i] = "";
            aPcodAcctMethod[i] = "";
            aPcodQueryType[i] = "";
        }
        sqlCmd = "select acct_code,";
        sqlCmd += "chi_short_name,";
        sqlCmd += "eng_short_name,";
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "acct_method,";
        sqlCmd += "query_type ";
        sqlCmd += " from ptr_actcode ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            aPcodAcctCode[i] = getValue("acct_code", i);
            aPcodChiShortName[i] = getValue("chi_short_name", i);
            aPcodEngShortName[i] = getValue("eng_short_name", i);
            aPcodItemOrderNormal[i] = getValue("item_order_normal", i);
            aPcodItemOrderBackDate[i] = getValue("item_order_back_date", i);
            aPcodItemOrderRefund[i] = getValue("item_order_refund", i);
            aPcodItemClassNormal[i] = getValue("item_class_normal", i);
            aPcodItemClassBackDate[i] = getValue("item_class_back_date", i);
            aPcodItemClassRefund[i] = getValue("item_class_refund", i);
            aPcodAcctMethod[i] = getValue("acct_method", i);
            aPcodQueryType[i] = getValue("query_type", i);
        }

        ptrActcodeCnt = recordCnt;

    }

    /***********************************************************************/
    void selectPtrAcctType() throws Exception {

        daoTable    = "ptr_acct_type";
        extendField = "pacc.";
        sqlCmd = "select ";
        sqlCmd += "curr_code ";
        sqlCmd += "from ptr_acct_type ";
        sqlCmd += "group by curr_code ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hPaccCurrCode = getValue("pacc.curr_code", i);

            selectBilSysexp();
        }
    }

    /***********************************************************************/
    void selectBilSysexp() throws Exception {

        tempBillType = "";
        hPostTotRecord = 0;
        totalTransactionCnt = 0;
        totalTransactionAmt = 0;

        sqlCmd  = "select ";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.bill_type,";
        sqlCmd += "a.txn_code,";
        sqlCmd += "a.purchase_date,";
        sqlCmd += "substr(a.src_type,1,1) h_syse_source_type,";
        sqlCmd += "b.acct_type,";
        sqlCmd += "a.p_seqno,";
        sqlCmd += "a.src_amt,";
        sqlCmd += "a.mcht_no,";
        sqlCmd += "a.installment_kind,";
        sqlCmd += "a.ptr_mcht_no,";
        sqlCmd += "to_char(a.dest_amt) h_curp_destination_amt_char,";
        sqlCmd += "a.dest_amt,";
        sqlCmd += "decode(a.dest_curr,'','901',a.dest_curr) h_curp_destination_currency,";
        sqlCmd += "a.bill_desc,";
        sqlCmd += "a.post_flag,";
        sqlCmd += "a.ao_flag,";
        sqlCmd += "a.auth_code,";
        sqlCmd += "a.mcht_category,";
        sqlCmd += "decode(a.merge_flag,'','N',a.merge_flag) h_syse_merge_flag,";
        sqlCmd += "a.mod_pgm,";
        sqlCmd += "to_char(a.mod_time,'hh24miss') h_syse_mod_time,";
        sqlCmd += "decode(a.curr_code, '','901', a.curr_code) h_syse_curr_code,";
        sqlCmd += "a.dc_dest_amt,";
        sqlCmd += "a.ref_key,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from bil_sysexp a, ptr_acct_type b ";
        sqlCmd += "where decode(a.post_flag,'','N', a.post_flag) != 'Y' ";
        sqlCmd += "  and b.acct_type  = a.acct_type ";
        sqlCmd += "  and (a.p_seqno  <> '' or card_no <> '') ";
        sqlCmd += "  and b.curr_code  = ? ";
        sqlCmd += "order by a.bill_type ";
        setString(1, hPaccCurrCode);
        int nCur = openCursor();
        while (fetchTable(nCur)) {
            hSyseRealCardNo = getValue("card_no");
            hSyseBillType = getValue("bill_type");
            hSyseTransactionCode = getValue("txn_code");
            hSysePurchaseDate = getValue("purchase_date");
            hSyseSourceType = getValue("h_syse_source_type");
            hSyseAcctType = getValue("acct_type");
            hSysePSeqno = getValue("p_seqno");
            hSyseSourceAmt = getValueDouble("src_amt");
            hSyseMerchantNo = getValue("mcht_no");
            hSyseInstallmentKind = getValue("installment_kind");
            hSysePtrMerchantNo = getValue("ptr_mcht_no");
            hCurpDestinationAmtChar = getValue("h_curp_destination_amt_char");
            hSyseDestinationAmt = getValueDouble("dest_amt");
            hCurpDestinationCurrency = getValue("h_curp_destination_currency");
            hSyseBillDesc = getValue("bill_desc");
            hSysePostFlag = getValue("post_flag");
            hSyseAoFlag = getValue("ao_flag");
            hSyseAuthorization = getValue("auth_code");
            hSyseMerchantCategory = getValue("mcht_category");
            hSyseMergeFlag = getValue("h_syse_merge_flag");
            hSyseModPgm = getValue("mod_pgm");
            hSyseModTime = getValue("h_syse_mod_time");
            hSyseCurrCode = getValue("h_syse_curr_code");
            hSyseDcDestinationAmt = getValueDouble("dc_dest_amt");
            hSyseRefKey = getValue("ref_key");
            hSyseRowid = getValue("rowid");

            totCnt++;
            if (totCnt % 10000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process 1 record=[%d]\n", totCnt));

            /* debug
   			showLogMessage("D", "", "888 card=["+ totCnt +"]"+ hSyseRealCardNo +","+ hSyseBillType);
            */

            if (!hSyseBillType.equals(tempBillType)) {
                if (tempBillType.length() != 0)
                    insertBilPostcntl();

                tempBillType = hSyseBillType;
                selectPtrBillunit();
                selectBilPostcntl();
                printHead();
                printFirstRtn();
                hPostTotRecord = 0;
                hPostTotAmt = 0;
            }

            hPostTotRecord++;

            if((hSyseTransactionCode.equals("06")) || (hSyseTransactionCode.equals("25")) ||
               (hSyseTransactionCode.equals("27")) || (hSyseTransactionCode.equals("29")) ||
               (hSyseTransactionCode.equals("66")) || (hSyseTransactionCode.equals("85")) ||
               (hSyseTransactionCode.equals("87")) || (hSyseTransactionCode.equals("20")))
                hPostTotAmt = hPostTotAmt - hSyseDestinationAmt;
            else
                hPostTotAmt = hPostTotAmt + hSyseDestinationAmt;

            if (hSyseRealCardNo.length() == 0) {
                hAcnoAcctPSeqno = hSysePSeqno;
                getCardNo();
            }
    
            selectCrdCard();
            selectActAcno();

            searchPtrBintable();
            selectPtrBilltype();
            procSpecDesc();
            searchPtrActcode();

            insertBilCurpost();
            updateBilSysexp();
        }
        closeCursor(nCur);

        if (hPostTotRecord > 0) {
            showLogMessage("I", "", String.format("總共筆數 :[%7d]", hPostTotRecord));
            insertBilPostcntl();

            buf = "";
            buf = comcr.insertStr(buf, "總  計:", 12);
            String szTmp = comcr.commFormat("3z,3z", totalTransactionCnt);
            buf = comcr.insertStr(buf, szTmp, 29);
            szTmp = comcr.commFormat("3$,3$,3$", totalTransactionAmt);
            buf = comcr.insertStr(buf, szTmp, 39);
            lpar111.add(comcr.putReport(rptId111, rptName111, sysDate, ++rptSeq111, "0", buf));
        }
    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";
        hBiunAuthFlag = "";

        sqlCmd = "select conf_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, hSyseBillType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", hSyseBillType, hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

    }

    /***********************************************************************/
    void selectBilPostcntl() throws Exception {
        hPostBatchSeq = 0;
        sqlCmd = "select to_number(substr(to_char(nvl(max(batch_seq),0) + 1,'0000'),2,4)) h_post_batch_seq ";
        sqlCmd += " from bil_postcntl  ";
        sqlCmd += "where batch_unit = substr(?,1,2)  ";
        sqlCmd += "and batch_date = ? ";
        setString(1, hSyseBillType);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_bil_postcntl not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPostBatchSeq = getValueInt("h_post_batch_seq");
        }

        String tmpStr = String.format("%8.8s%2.2s%04d", hBusiBusinessDate, hSyseBillType, hPostBatchSeq);
        hCurpBatchNo = tmpStr;
    }

    /***********************************************************************/
    void printHead() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: " + rptId111, 1);

        String szTmp = String.format("%22s", "各項費用統計表(一)(系統產生)");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        buf = comcr.insertStr(buf, chinDate, 70);
        lpar111.add(comcr.putReport(rptId111, rptName111, sysDate, ++rptSeq111, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "幣    別:", 1);
        buf = comcr.insertStr(buf, hPaccCurrCode, 10);
        buf = comcr.insertStr(buf, "產生日期:", 60);
        szTmp = String.format("%7d", comcr.str2long(hBusiBusinessDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 70);
        lpar111.add(comcr.putReport(rptId111, rptName111, sysDate, ++rptSeq111, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "   筆  數 金         額 批          號 ", 32);
        lpar111.add(comcr.putReport(rptId111, rptName111, sysDate, ++rptSeq111, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "   ====== ============= ============== ", 32);
        lpar111.add(comcr.putReport(rptId111, rptName111, sysDate, ++rptSeq111, "0", buf));
    }

    /***********************************************************************/
    void printFirstRtn() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "count(*) h_temp_p_count,";
        sqlCmd += "sum(decode(a.txn_code,'06',a.dest_amt*(-1)," + "'25',a.dest_amt*(-1)," + "'27',a.dest_amt*(-1),"
                          + "'29',a.dest_amt*(-1)," + "'26',a.dest_amt*(-1),a.dest_amt )  ) h_temp_p_sum,";
        sqlCmd += "a.txn_code  h_temp_transaction_code ";
        sqlCmd += " from bil_sysexp a, ptr_acct_type b ";
        sqlCmd += "where decode(a.post_flag,'','N',a.post_flag) != 'Y' ";
        sqlCmd += " and a.bill_type = ? ";
        sqlCmd += " and a.acct_type = b.acct_type ";
        sqlCmd += " and b.curr_code = ? ";
        sqlCmd += " group by a.txn_code ";
        setString(1, hSyseBillType);
        setString(2, hPaccCurrCode);
        int recordCnt = selectTable();
        
        /* debug
            showLogMessage("D", "", " 888 first rtn=[" + hSyseBillType + "]" + recordCnt);
        */
        
        for (int i = 0; i < recordCnt; i++) {
            hTempPCount = getValueInt("h_temp_p_count", i);
            hTempPSum = getValueDouble("h_temp_p_sum", i);
            hTempTransactionCode = getValue("h_temp_transaction_code", i);
            
            /* debug
                showLogMessage("D", "", "888 first  card=[" + hTempTransactionCode + "]");
            */

			hBityExterDesc = "";

			sqlCmd = "select EXTER_DESC,";
			sqlCmd += "ACCT_CODE ";
			sqlCmd += " from ptr_billtype  ";
			sqlCmd += "where bill_type = ?  ";
			sqlCmd += "  and txn_code  = ? ";
			setString(1, hSyseBillType);
			setString(2, hTempTransactionCode);
			if (selectTable() > 0) {
				hBityExterDesc = getValue("EXTER_DESC");
				hBityAcctCode = getValue("ACCT_CODE");
			} else {
				comcr.errRtn("select_ptr_billtype 2 not found!", hTempTransactionCode, hSyseBillType);
			}
			
			showLogMessage("I", "", String.format("reports [%s] [%s] \n", hBityExterDesc, hBityAcctCode));

			hThisBusiAddAmt = hTempPSum;
			if ((Arrays.asList("AF", "CF", "PF", "AI").contains(hBityAcctCode)) && (hThisBusiAddAmt != 0))
				insertThisActPostLog();
            
            buf = "";
            buf = comcr.insertStr(buf, hBityExterDesc, 12);
            buf = comcr.insertStr(buf, hBityAcctCode, 32);
            String szTmp = comcr.commFormat("3z,3z", hTempPCount);
            buf = comcr.insertStr(buf, szTmp, 34);
            szTmp = comcr.commFormat("3$,3$,3$", hTempPSum);
            buf = comcr.insertStr(buf, szTmp, 44);
            totalTransactionCnt = totalTransactionCnt + hTempPCount;
            totalTransactionAmt = (int) (totalTransactionAmt + hTempPSum);
            buf = comcr.insertStr(buf, hCurpBatchNo, 56);
            lpar111.add(comcr.putReport(rptId111, rptName111, sysDate, ++rptSeq111, "0", buf));
        }
    }

    /***********************************************************************/
    void insertThisActPostLog() throws Exception {
             
        hSrcPgmPostseq = 0;
        daoTable    = "act_post_log";
        selectSQL   = " nvl(max(SRC_PGM_POSTSEQ), 0) + 1 as h_src_pgm_postseq";
        whereStr  = " where BUSINESS_DATE    = ? ";
        whereStr += "  and CURR_CODE         = ? ";      
        whereStr += "  and ACCT_CODE         = ? ";      
        whereStr += "  and SRC_PGM           = ? ";      
        setString(1, hBusiBusinessDate);
        setString(2, hPaccCurrCode);
        setString(3, hBityAcctCode);
        setString(4, javaProgram);
        int m = selectTable();
        hSrcPgmPostseq = getValueLong("h_src_pgm_postseq");

        daoTable    = "act_post_log";
        extendField = "post.";
        setValue("post.BUSINESS_DATE", hBusiBusinessDate);
        setValue("post.CURR_CODE", hPaccCurrCode);
        setValue("post.ACCT_CODE", hBityAcctCode);
        setValue("post.SRC_PGM",javaProgram);
        setValueLong("post.SRC_PGM_POSTSEQ", hSrcPgmPostseq);
        setValue("post.POST_TYPE","A1");
        hThisBusiAddAmt = convAmt(hThisBusiAddAmt);
        setValueDouble("post.POST_TYPE_AMT", hThisBusiAddAmt);
        setValue("post.POST_NOTE", hPostNote);
        setValue("post.BILL_TYPE", hSyseBillType);
        setValue("post.TXN_CODE", hTempTransactionCode);
        setValue("post.ACCT_TYPE", hSyseAcctType);
        setValue("post.MOD_TIME",sysDate + sysTime);	
        setValue("post.MOD_PGM",javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) 
           { 
            comcr.errRtn("insert_this act_post_log ERROR ", hBusiBusinessDate +" "+
                    hPaccCurrCode +" "+ hBityAcctCode +" "+javaProgram+ hSrcPgmPostseq, hCallBatchSeqno);
           } 
            
    }
    
    /***********************************************************************/
    public double convAmt(double cvtAmt) throws Exception
    {
      long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
      double cvtDouble =  ((double) cvtLong) / 100;
      return cvtDouble;
    }


    /**********************************************************************
     ** bil_sysexp.card_no若為空值，則抓取 act_debt 欠款最高的那一張卡號，
     ** 但若又act_debt 欠款最高為0，則抓任一筆卡號
     */
    
    void getCardNo() throws Exception {
    	
    	int recordCnt = 0;
    	hSyseRealCardNo = "";
    	
    	sqlCmd = "select p_seqno, card_no, sum(end_bal) AS end_bal, sum(beg_bal) AS beg_bal ";
        sqlCmd += " from act_debt   ";
        sqlCmd += "where p_seqno     = ?  ";
        sqlCmd += "  and curr_code = decode(cast(? as varchar(10)),'901',curr_code,?)  ";
        sqlCmd += "  GROUP BY p_seqno,card_no ORDER BY end_bal,beg_bal DESC  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hAcnoAcctPSeqno);
        setString(2, hSyseCurrCode);
        setString(3, hSyseCurrCode);
        recordCnt = selectTable();

        if (recordCnt > 0) {
            hSyseRealCardNo = getValue("card_no");
            return;
        }
        
        
        sqlCmd = "select card_no ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where p_seqno     = ?  ";
        sqlCmd += "  and curr_code = decode(cast(? as varchar(10)),'901',curr_code,?)  ";
        sqlCmd += "order by issue_date desc ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hAcnoAcctPSeqno);
        setString(2, hSyseCurrCode);
        setString(3, hSyseCurrCode);
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	showLogMessage("E", "", "select_crd_card not found! p_seqno=[" +hAcnoAcctPSeqno +"],curr_code=["+ 
                           hSyseCurrCode + "]");
        }
        
        if (recordCnt > 0) {
            hSyseRealCardNo = getValue("card_no");
        }

    }

    /***********************************************************************/
    void selectCrdCard() throws Exception {
        hCardMajorCardNo = "";
        hCardCurrentCode = "";
        hCardIssueDate = "";
        hCardOppostDate = "";
        hCardPromoteDept = "";
        hCardProdNo = "";
        hCardGroupCode = "";
        hCardSourceCode = "";
        hCardCardType = "";
        hCardBinNo = "";
        hCardBinType = "";
        hCardAcctPSeqno = "";
        hCardIdPSeqno = "";

        sqlCmd = "select major_card_no,";
        sqlCmd += "current_code,";
        sqlCmd += "issue_date,";
        sqlCmd += "oppost_date,";
        sqlCmd += "promote_dept,";
        sqlCmd += "prod_no,";
        sqlCmd += "group_code,";
        sqlCmd += "source_code,";
        sqlCmd += "card_type,";
        sqlCmd += "bin_no,";
        sqlCmd += "bin_type,";
        sqlCmd += "acct_type,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "p_seqno,";
        sqlCmd += "major_id_p_seqno, ";
        sqlCmd += "id_p_seqno ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no  = ? ";
        setString(1, hSyseRealCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_crd_card not found!", "", hSyseRealCardNo);
        }
        if (recordCnt > 0) {
            hCardMajorCardNo = getValue("major_card_no");
            hCardCurrentCode = getValue("current_code");
            hCardIssueDate = getValue("issue_date");
            hCardOppostDate = getValue("oppost_date");
            hCardPromoteDept = getValue("promote_dept");
            hCardProdNo = getValue("prod_no");
            hCardGroupCode = getValue("group_code");
            hCardSourceCode = getValue("source_code");
            hCardCardType = getValue("card_type");
            hCardBinType = getValue("bin_type");
            hCardBinNo = getValue("bin_no");
            hCardAcctType = getValue("acct_type");
            hCardAcctPSeqno = getValue("p_seqno");
            hCardPSeqno = getValue("acno_p_seqno");
            hCardIdPSeqno = getValue("id_p_seqno");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
        }

    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        hAcnoAcctStatus = "";
        hAcnoStmtCycle = "";
        hAcnoBlockStatus = "";
        hAcnoBlockDate = "";
        hAcnoPayByStageFlag = "";
        hAcnoAutopayAcctNo = "";
        hCurpReferenceNo = "";

        sqlCmd = "select acct_type,";
        sqlCmd += "acct_key,";
        sqlCmd += "acct_status,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "pay_by_stage_flag,";
        sqlCmd += "autopay_acct_no,";
        sqlCmd += "substr(?,3,2)||substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_curp_reference_no ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hCardAcctPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acno not found!", "", hCardAcctPSeqno);
        }
        if (recordCnt > 0) {
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
            hCurpReferenceNo = getValue("h_curp_reference_no");

            hAcnoBlockStatus = "";
            hAcnoBlockDate = "";
            sqlCmd = "select block_status, " + "block_date " + "from cca_card_acct ";
            sqlCmd += " where acno_p_seqno = ? and acct_type = ? " + " and decode(debit_flag,'','N',debit_flag) = 'N' ";
            sqlCmd += "fetch first 1 rows only";
            setString(1, hCardAcctPSeqno);
            setString(2, hCardAcctType);
            if (selectTable() > 0) {
                hAcnoBlockStatus = getValue("block_status");
                hAcnoBlockDate = getValue("block_date");
            }
        }

    }

    /***********************************************************************/
    int searchPtrBintable() throws Exception {
        for (int i = 0; i < ptrBintableCnt; i++) {
            if (aPbtbBinNo[i].equals(hCardBinNo)) {
                hPbtbCurrCode = aPbtbCurrCode[i];
                hPbtbBinType = aPbtbBinType[i];
                return (0);
            }
        }
        return (1);
    }

    /***********************************************************************/
    void selectPtrBilltype() throws Exception {
        hBityBillType = "";
        hBitySignFlag = "";
        hBityAcctCode = "";
        hBityAcctItem = "";
        hBityFeesState = "";
        hBityFeesFixAmt = 0;
        hBityFeesPercent = 0;
        hBityFeesMin = 0;
        hBityFeesMax = 0;
        hBityFeesBillType = "";
        hBityFeesTxnCode = "";
        hBityExterDesc = "";
        hBityInterestMode = "";
        hBityAdvWkday = "";
        hBityBalanceState = "";
        hBityCollectionMode = "";
        hBityCashAdvState = "";
        hBityEntryAcct = "";
        hBityChkErrBill = "";
        hBityDoubleChk = "";
        hBityFormatChk = "";
        hBityMerchFee = 0;

        sqlCmd = "select bill_type,";
        sqlCmd += "sign_flag,";
        sqlCmd += "acct_code,";
        sqlCmd += "acct_item,";
        sqlCmd += "fees_state,";
        sqlCmd += "fees_fix_amt,";
        sqlCmd += "fees_percent,";
        sqlCmd += "fees_min,";
        sqlCmd += "fees_max,";
        sqlCmd += "fees_bill_type,";
        sqlCmd += "fees_txn_code,";
        sqlCmd += "exter_desc,";
        sqlCmd += "interest_mode,";
        sqlCmd += "adv_wkday,";
        sqlCmd += "balance_state,";
        sqlCmd += "cash_adv_state,";
        sqlCmd += "entry_acct,";
        sqlCmd += "chk_err_bill,";
        sqlCmd += "double_chk,";
        sqlCmd += "format_chk,";
        sqlCmd += "merch_fee ";
        sqlCmd += " from ptr_billtype  ";
        sqlCmd += "where bill_type = ?  ";
        sqlCmd += "and txn_code = ? ";
        setString(1, hSyseBillType);
        setString(2, hSyseTransactionCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billtype not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBityBillType = getValue("bill_type");
            hBitySignFlag = getValue("sign_flag");
            hBityAcctCode = getValue("acct_code");
            hBityAcctItem = getValue("acct_item");
            hBityFeesState = getValue("fees_state");
            hBityFeesFixAmt = getValueDouble("fees_fix_amt");
            hBityFeesPercent = getValueDouble("fees_percent");
            hBityFeesMin = getValueInt("fees_min");
            hBityFeesMax = getValueInt("fees_max");
            hBityFeesBillType = getValue("fees_bill_type");
            hBityFeesTxnCode = getValue("fees_txn_code");
            hBityExterDesc = getValue("exter_desc");
            hBityInterestMode = getValue("interest_mode");
            hBityAdvWkday = getValue("adv_wkday");
            hBityBalanceState = getValue("balance_state");
            hBityCashAdvState = getValue("cash_adv_state");
            hBityEntryAcct = getValue("entry_acct");
            hBityChkErrBill = getValue("chk_err_bill");
            hBityDoubleChk = getValue("double_chk");
            hBityFormatChk = getValue("format_chk");
            hBityMerchFee = getValueInt("merch_fee");
        }

    }

    /***********************************************************************/
    void procSpecDesc() throws Exception {
        String tmpStr = "";
        hCurpAcexterDesc = hBityExterDesc;

        if (hSyseTransactionCode.equals("AF") || hSyseTransactionCode.equals("LF")) {
            tmpStr = String.format("%-20.20s%19s", hBityExterDesc, hSyseRealCardNo);
            hCurpAcexterDesc = tmpStr;
        }

        if (hCurpMerchantChiName.length() == 0)
            hCurpMerchantChiName = hSyseBillDesc;

        if (hSyseBillType.equals("OSSG") || hSyseBillType.equals("OKOL") || hSyseBillType.equals("INTX"))
            hCurpMerchantChiName = hBityExterDesc;

        if (hSyseTransactionCode.equals("OI")) {
            if (hSyseBillDesc.length() != 0)
                hCurpAcexterDesc = hSyseBillDesc;
            tmpStr = String.format("(%s(%-20.20s)", hBityExterDesc, hCurpAcexterDesc);
            hCurpMerchantChiName = tmpStr;
        }

        if (hSyseTransactionCode.equals("BO") || hSyseBillType.equals("INBO"))
            selectMktVendor();

        if (hSyseTransactionCode.equals("TF")) {
            hCurpMerchantChiName = hSyseBillDesc;
        }
    }

    /***********************************************************************/
    void selectMktVendor() throws Exception {
        sqlCmd = "select substrb(vendor_name,1,20) h_curp_merchant_chi_name ";
        sqlCmd += " from mkt_vendor  ";
        sqlCmd += "where vendor_no = ? ";
        setString(1, hSyseMerchantNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCurpMerchantChiName = getValue("h_curp_merchant_chi_name");
        }

    }

    /***********************************************************************/
    int searchPtrActcode() throws Exception {
        for (int i = 0; i < ptrActcodeCnt; i++) {
            if (aPcodAcctCode[i].equals(hBityAcctCode)) {
                hPcodChiShortName = aPcodChiShortName[i];
                hPcodEngShortName = aPcodEngShortName[i];
                hPcodItemClassNormal = aPcodItemClassNormal[i];
                hPcodItemClassBackDate = aPcodItemClassBackDate[i];
                hPcodItemClassRefund = aPcodItemClassRefund[i];
                hPcodItemOrderNormal = aPcodItemOrderNormal[i];
                hPcodItemOrderBackDate = aPcodItemOrderBackDate[i];
                hPcodItemOrderRefund = aPcodItemOrderRefund[i];
                hPcodAcctMethod = aPcodAcctMethod[i];
                hPcodQueryType = aPcodQueryType[i];
                return (0);
            }
        }
        return (1);
    }

    /***********************************************************************/
    void insertBilCurpost() throws Exception {
        String str600 = "";
        long tempLong = 0;
        if (hSyseMergeFlag.equals("Y")) {
            hBityFormatChk = "N";
            hBityDoubleChk = "N";
            hBityChkErrBill = "N";
            hBityCashAdvState = "N";
        }

        hCurpFilmNo = "";
        if ((hBityFormatChk.substring(0, 1).equals("Y")) && (hSyseBillType.equals("OUCU"))
                && (hSyseTransactionCode.equals("07"))) {
            str600 = String.format("07  %-8.8s%-6.6s", hSysePurchaseDate, hSyseModTime);
            hCurpFilmNo = str600;
        }

        hCurpContractFlag = "P";

        hCurpSourceCurrency = "901";
        if (hSyseCurrCode.length() > 0) {
            hCurpSourceCurrency = hSyseCurrCode;
        }
        hCurpDestinationAmt = hSyseDestinationAmt;
        if (!hSyseCurrCode.equals("901")) {
            hCurpDestinationCurrency = "901";
            if (!hSyseModPgm.equals("CycA070")) {
                selectPtrCurrRate();
                tempLong = (long) (hCurpDcExchangeRate * hCurpDestinationAmt + 0.5);
                hCurpDestinationAmt = tempLong;
            }
        }
        hCurpDestinationAmtChar = String.format("%.2f", hCurpDestinationAmt);
        hCurpSourceAmtChar = String.format("%.2f", hSyseSourceAmt);

        setValue("reference_no", hCurpReferenceNo);
        setValue("bill_type", hBityBillType);
        setValue("txn_code", hSyseTransactionCode);
        setValue("sign_flag", hBitySignFlag);
        setValue("bin_type", hCardBinType);
        setValue("card_no", hSyseRealCardNo);
        setValue("film_no", hCurpFilmNo);
        setValue("purchase_date", hSysePurchaseDate);
        setValueDouble("dest_amt", hCurpDestinationAmt);
        setValue("dest_curr", hCurpDestinationCurrency);
        setValueDouble("source_amt", hSyseSourceAmt == 0 ? hCurpDestinationAmt : hSyseSourceAmt);
        setValue("source_curr", hCurpSourceCurrency);
        setValue("mcht_eng_name", hCurpMerchantEngName);
        setValue("mcht_city", hCurpMerchantCity);
        setValue("mcht_country", hCurpMerchantCountry);
        setValue("mcht_category", hSyseMerchantCategory);
        setValue("mcht_zip", hCurpMerchantZip);
        setValue("auth_code", hSyseAuthorization);
        setValue("merchant_state", hCurpMerchantState);
        setValue("mcht_no", hSyseMerchantNo);
        setValue("mcht_chi_name", hCurpMerchantChiName);
        setValue("batch_no", hCurpBatchNo);
        setValue("acct_code", hBityAcctCode);
        setValue("acct_item", hBityAcctItem);
        setValue("acct_eng_short_name", hPcodEngShortName);
        setValue("acct_chi_short_name", hPcodChiShortName);
        setValue("item_order_normal", hPcodItemOrderNormal);
        setValue("item_order_back_date", hPcodItemOrderBackDate);
        setValue("item_order_refund", hPcodItemOrderRefund);
        setValue("acexter_desc", hCurpAcexterDesc);
        setValue("entry_acct", hBityEntryAcct);
        setValue("item_class_normal", hPcodItemClassNormal);
        setValue("item_class_back_date", hPcodItemClassBackDate);
        setValue("item_class_refund", hPcodItemClassRefund);
        setValue("fees_state", hSyseAoFlag.equals("1") ? "Y" : hBityFeesState);
        setValue("cash_adv_state", hBityCashAdvState);
        setValue("this_close_date", hBusiBusinessDate);
        setValue("acct_type", hAcnoAcctType);
        setValue("stmt_cycle", hAcnoStmtCycle);
        setValue("major_card_no", hCardMajorCardNo);
        setValue("promote_dept", hCardPromoteDept);
        setValue("issue_date", hCardIssueDate);
        setValue("prod_no", hCardProdNo);
        setValue("group_code", hCardGroupCode);
        setValue("acno_p_seqno", hCardAcctPSeqno);
        setValue("p_seqno"  , hCardAcctPSeqno);
        setValue("id_p_seqno", hCardIdPSeqno);
        setValue("major_id_p_seqno",hCardMajorIdPSeqno);
        setValue("tx_convt_flag", "Y");
        setValue("acctitem_convt_flag", "Y");
        setValue("format_chk_ok_flag", hBityFormatChk);
        setValue("double_chk_ok_flag", hBityDoubleChk);
        setValue("err_chk_ok_flag", hBityChkErrBill);
        setValue("source_code", hCardSourceCode);
        setValue("valid_flag", hBiunAuthFlag);
        setValue("contract_flag", hCurpContractFlag);
        setValue("merge_flag", hSyseMergeFlag);
        setValue("installment_kind", hSyseInstallmentKind);
        setValue("ptr_merchant_no", hSysePtrMerchantNo);
        setValue("curr_code", hSyseCurrCode);
        setValueDouble("dc_amount", hSyseDcDestinationAmt);
        setValueDouble("dc_exchange_rate", hCurpDcExchangeRate);
        setValue("bin_type", hPbtbBinType);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hSyseAoFlag.equals("1") ? javaProgram + "a" : javaProgram);
        daoTable = "bil_curpost";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_curpost duplicate", "", hCallBatchSeqno);
        }

        // **insert bil_nccc300_dtl
        setValue("reference_no", hCurpReferenceNo);
        setValue("card_no", hSyseRealCardNo);
        setValue("transaction_source", hSyseSourceType);
        setValue("batch_no", hCurpBatchNo);
        setValue("query_type", hPcodQueryType);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hSyseAoFlag.equals("1") ? javaProgram + "a" : javaProgram);
        daoTable = "bil_nccc300_dtl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_nccc300_dtl duplicate in insert_bill_curpost()", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectPtrCurrRate() throws Exception {

        hCurpDcExchangeRate = 0;
        sqlCmd = "select exchange_rate ";
        sqlCmd += " from ptr_curr_rate  ";
        sqlCmd += "where curr_code = ? ";
        setString(1, hSyseCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_curr_rate not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCurpDcExchangeRate = getValueDouble("exchange_rate");
        }
    }


    /***********************************************************************/
    void updateBilSysexp() throws Exception {
        daoTable = "bil_sysexp";
        updateSQL = "post_flag    = 'T' ";
        whereStr = "where rowid  = ?   ";
        setRowId(1, hSyseRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_bil_sysexp not found!", "", hCallBatchSeqno);
        }

        sqlCmd = "insert into bil_sysexp_hst  ";
        sqlCmd += "(card_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "purchase_date,";
        sqlCmd += "src_type   ,";
        sqlCmd += "acct_type,";
        sqlCmd += "p_seqno ,";
        sqlCmd += "mcht_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "dest_curr,";
        sqlCmd += "src_amt,";
        sqlCmd += "bill_desc,";
        sqlCmd += "post_flag,";
        sqlCmd += "ao_flag,";
        sqlCmd += "auth_code,";
        sqlCmd += "mcht_category,";
        sqlCmd += "merge_flag,";
        sqlCmd += "installment_kind,";
        sqlCmd += "ptr_mcht_no,";
        sqlCmd += "curr_code,";
        sqlCmd += "dc_dest_amt,";
        sqlCmd += "mod_user,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_seqno,";
        sqlCmd += "ref_key,";
        sqlCmd += "reference_no,";
        sqlCmd += "crt_date,";
        sqlCmd += "data_src)";
        sqlCmd += " select ";
        sqlCmd += "?,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "purchase_date,";
        sqlCmd += "src_type,";
        sqlCmd += "acct_type,";
        sqlCmd += "p_seqno ,";
        sqlCmd += "mcht_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "dest_curr,";
        sqlCmd += "src_amt,";
        sqlCmd += "bill_desc,";
        sqlCmd += "post_flag,";
        sqlCmd += "ao_flag,";
        sqlCmd += "auth_code,";
        sqlCmd += "mcht_category,";
        sqlCmd += "merge_flag,";
        sqlCmd += "installment_kind,";
        sqlCmd += "ptr_mcht_no,";
        sqlCmd += "curr_code,";
        sqlCmd += "decode(dc_dest_amt,0,dest_amt,dc_dest_amt),";
        sqlCmd += "decode(curr_code,'','901',curr_code),";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_seqno,";
        sqlCmd += "ref_key,  ";
        sqlCmd += "?,";
        sqlCmd += "?,";
        sqlCmd += "?";
        sqlCmd += "from bil_sysexp where rowid  = ? ";
        setString(1, hSyseRealCardNo);
        setString(2, hCurpReferenceNo);
        setString(3, hBusiBusinessDate);
        setString(4, "1");
        setRowId(5, hSyseRowid);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        setValue("batch_date", hBusiBusinessDate);
        
        setFunction("batch_unit", "substr(?,1,2)");
        setValue("batch_unit", tempBillType);
        
        setValueInt("batch_seq", hPostBatchSeq);
        setValue("batch_no", hCurpBatchNo);
        setValueInt("tot_record", hPostTotRecord);
        setValueDouble("tot_amt", hPostTotAmt);
        setValue("confirm_flag_p",
                hBiunConfFlag.toUpperCase(Locale.TAIWAN).equals("Y") ? (hSyseAoFlag.equals("1") ? "Y" : "N") : "Y");
        setValue("confirm_flag", hBiunConfFlag);
        setValue("this_close_date", hBusiBusinessDate);
        setValue("mod_user", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "bil_postcntl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate!"
                    , String.format("batch_date[%s] batch_unit[%s] batch_seq[%s]", hBusiBusinessDate, tempBillType.substring(0, 2), hPostBatchSeq)
                    , hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteBilSysexp() throws Exception {
        daoTable = "bil_sysexp";
        whereStr = "where post_flag = 'T' ";
        deleteTable();
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilF001 proc = new BilF001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
