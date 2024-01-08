/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/08/27  V1.00.01    JeffKung  移除不必要的程式碼                        *
*  109-09-04  V1.00.02  yanghan    解决Portability Flaw: Locale Dependent Comparison问题    * 
*  109-09-14  V1.00.03    JeffKung  帳單中文若USER有輸入,以客戶輸入的說明寫入       *
*  109-11-11  V1.01.04  yanghan       修改了變量名稱和方法名稱    
*  109-12-24  V1.01.05  yanghan       修改了變量名稱和方法名稱            *        * 
******************************************************************************/

package Dbb;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*其他費用轉換作業*/
public class DbbA001 extends AccessDAO {
    private String progname = "VD 線上加檔批次轉換作業  109/12/24  V1.01.05";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hTempUser = "";
    int debug = 0;

    String prgmId = "DbbA001";
    String rptName = "DBB_A001R0";
    List<Map<String, Object>> lpar111 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;

    String hBusinessDate = "";
    String hSystemDate = "";
    String hDcurCardNo = "";
    String hBiunConfFlag = "";
    String hBiunAuthFlag = "";
    String hFixBillType = "";
    String hBatchSeq = "";
    String hDothBillType = "";
    String hDothTxnCode = "";
    String hDothCardNo = "";
    int hDothSeqNo = 0;
    double hDothDestAmt = 0;
    String hAmtChar = "";
    String hDothDestCurr = "";
    String hDothPurchaseDate = "";
    String hDothChiDesc = "";
    String hDothBillDesc = "";
    String hDothDeptFlag = "";
    String hDothAprFlag = "";
    String hDothPostFlag = "";
    String hDothRowid = "";
    String hTempX08 = "";
    String hDcurReferenceNo = "";
    String hDcurPSeqno = "";
    String hDcurAcctKey = "";
    String hTtttAcctNo = "";
    String hBityBillType = "";
    String hBityTxnCode = "";
    String hBitySignFlag = "";
    String hBityAcctCode = "";
    String hBityAcctItem = "";
    String hBityFeesState = "";
    double hBityFeesFixAmt = 0;
    String hBityFeesPercent = "";
    String hBityFeesMin = "";
    String hBityFeesMax = "";
    String hBityFeesBillType = "";
    String hBityFeesTxnCode = "";
    String hBityInterDesc = "";
    String hBityExterDesc = "";
    String hBityInterestMode = "";
    String hBityAdvWkday = "";
    String hBityBalanceState = "";
    String hBityCollectionMode = "";
    String hBityCashAdvState = "";
    String hBityEntryAcct = "";
    String hBityChkErrBill = "";
    String hBityDoubleChk = "";
    String hBityFormatChk = "";
    String hBityMerchFee = "";
    String hDcurTxnCode = "";
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
    String hDcurSignFlag = "";
    String hDcurAcctCode = "";
    String hPostBatchDate = "";
    String hPostBatchUnit = "";
    int hPostBatchSeq = 0;
    String hPostBatchNo = "";
    int hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hPostConfirmFlagp = "";
    String hPostConfirmFlag = "";
    String hPostThisCloseDate = "";
    String hDcurModUser = "";
    String hDcurModPgm = "";
    String hDcurBillType = "";
    String hDcurTransactionSeqNo = "";
    String hDcurFilmNo = "";
    String hDcurAcqMemberId = "";
    String hDcurPurchaseTime = "";
    String hDcurPurchaseDate = "";
    double hDcurDestAmt = 0;
    String hDcurDestCurr = "";
    double hDcurSourceAmt = 0;
    String hDcurSourceCurr = "";
    String hDcurMchtEngName = "";
    String hDcurMchtCity = "";
    String hDcurMchtCountry = "";
    String hDcurMchtCategory = "";
    String hDcurMchtZIp = "";
    String hDcurMchtState = "";
    String hDcurTmpRequestFlag = "";
    String hDcurUsageCode = "";
    String hDcurReasonCode = "";
    String hDcurSettlementFlag = "";
    double hDcurSettlAmt = 0;
    String hDcurTmpServiceCode = "";
    String hDcurAuthorization = "";
    String hDcurPosTermCapability = "";
    String hDcurPosPinCapability = "";
    String hDcurPosEntryMode = "";
    String hDcurProcessDate = "";
    String hDcurReimbursementAttr = "";
    String hDcurEcInd = "";
    String hDcurFirstConversionDate = "";
    String hDcurSecondConversionDate = "";
    String hDcurMchtNo = "";
    String hDcurMchtChiName = "";
    String hDcurElectronicTermInd = "";
    String hDcurTransactionSource = "";
    String hDcurAcquireDate = "";
    String hDcurContractNo = "";
    String hDcurGoodsName = "";
    String hDcurOriginalNo = "";
    String hDcurTelephoneNo = "";
    String hDcurTerm = "";
    String hDcurTotalTerm = "";
    String hDcurProdName = "";
    String hDcurBatchNo = "";
    String hDcurExchangeRate = "";
    String hDcurExchangeDate = "";
    String hDcurAcctItem = "";
    String hDcurAcctEngShortName = "";
    String hDcurAcctChiShortName = "";
    String hDcurItemOrderNormal = "";
    String hDcurItemOrderBackDate = "";
    String hDcurItemOrderRefund = "";
    String hDcurAcexterDesc = "";
    String hDcurEntryAcct = "";
    String hDcurItemClassNormal = "";
    String hDcurItemClassBackDate = "";
    String hDcurItemClassRefund = "";
    String hDcurAcctMethod = "";
    String hDcurInterestMode = "";
    String hDcurAdvWkday = "";
    String hDcurCollectionMode = "";
    String hDcurFeesState = "";
    double hDcurFeesFixAmt = 0;
    String hDcurFeesPercent = "";
    String hDcurFeesMin = "";
    String hDcurFeesMax = "";
    String hDcurFeesBillType = "";
    String hDcurFeesTxnCode = "";
    String hDcurBalanceState = "";
    String hDcurCashAdvState = "";
    String hDcurMerchFee = "";
    String hDcurThisCloseDate = "";
    String hDcurManualUpdFlag = "";
    String hDcurValidFlag = "";
    String hDcurDoubtType = "";
    String hDcurDuplicatedFlag = "";
    String hDcurRskType = "";
    String hDcurAcctType = "";
    String hDcurAcctStatus = "";
    String hDcurStmtCycle = "";
    String hDcurPayByStageFlag = "";
    String hDcurAutopayAcctNo = "";
    String hDcurMajorCardNo = "";
    String hDcurCurrentCode = "";
    String hDcurOppostDate = "";
    String hDcurPromoteDept = "";
    String hDcurIssueDate = "";
    String hDcurProdNo = "";
    String hDcurGroupCode = "";
    String hDcurBinType = "";
    String hDcurIdPSeqno = "";
    String hDcurReferenceNoOriginal = "";
    String hDcurFeesReferenceNo = "";
    String hDcurTxConvtFlag = "";
    String hDcurAcctitemConvtFlag = "";
    String hDcurFormatChkOkFlag = "";
    String hDcurDoubleChkOkFlag = "";
    String hDcurErrChkOkFlag = "";
    String hDcurSourceCode = "";
    String hDcurQueryType = "";
    String hDcurCurrPostFlag = "";
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
    String hCardPSeqno = "";
    String hCardBlockStatus = "";
    String hCardBlockDate = "";
    String hCardIdPSeqno = "";
    String hCardMajorIdPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    String hSystemDatef = "";

    int pCount = 0;
    double pSum = 0;
    String sBillUnit = "";
    String hTAddItem = "";
    String hTempX16 = "";
    String hTAcctStatus = "";
    String hPrintName = "";
    String hRptName = "";
    String hDcurModTime = "";
    long hDcurModSeqno = 0;
    String tempX10 = "";
    String tempX14 = "";
    String hPostAuthFlag = "";
    String tempX16 = "";
    int totalTransactionCnt = 0;
    double totalTransactionAmt = 0;
    int totalCount = 0;
    double totalAmt = 0;
    String tempX20 = "";
    String tempX40 = "";

    // ***********************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                String errMsg2 = "        1.batch_no : 批次號碼\n";
                errMsg2 += "            a.yyyymmdd:西元日期\n";
                errMsg2 += "            b.請款來源前二碼:'NC','OB','OU'\n";
                errMsg2 += "            c.序號: 4 碼\n";
                comc.errExit("Usage : DbbA001 callbatch_seqno", errMsg2);
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
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

            hFixBillType = "OKOL";

            commonRtn();
            hModPgm = javaProgram;
            hDcurModPgm = hModPgm;
            hDcurModTime = hModTime;
            hDcurModUser = hModUser;
            hDcurModSeqno = hModSeqno;

            selectPtrBillunit();

            hPostBatchDate = hBusinessDate;
            hPostBatchUnit = sBillUnit;
            tempX10 = String.format("%8s%2s", hPostBatchDate, hPostBatchUnit);
            tempX14 = String.format("%10s%4s", tempX10, hBatchSeq);
            hPostBatchNo = tempX14;

            hDcurBatchNo = tempX14;

            printFirstRtn();

            selectDbbOthexp();

            if (totalCount > 0) {
                hPostBatchSeq = comcr.str2int(hBatchSeq);
                hPostTotRecord = totalCount;
                hPostTotAmt = totalAmt;

                hPostConfirmFlag = hBiunConfFlag;
                hPostAuthFlag = hBiunAuthFlag;
                hPostConfirmFlagp = "N";
                if (hPostConfirmFlag.toUpperCase(Locale.TAIWAN).equals("Y") == false) {
                    hPostConfirmFlagp = "Y";
                }

                insertBilPostcntl();
            }

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCount + "]";
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
    void commonRtn() throws Exception {
        sqlCmd = "select online_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("online_date");
        } else {
            comcr.errRtn("fetch prt_businday  error", "", hCallBatchSeqno);
        }
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
        }
        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {
        sBillUnit = hFixBillType.substring(0, 2);
        hBiunConfFlag = "";
        hBiunAuthFlag = "";
        sqlCmd = "select conf_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, hFixBillType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

        hBatchSeq = "";
        sqlCmd = "select substr(to_char(nvl(max(batch_seq),0) + 1,'0000'),2,4) h_batch_seq ";
        sqlCmd += " from bil_postcntl  ";
        sqlCmd += "where batch_unit = substr(?,1,2)  ";
        sqlCmd += "and batch_date = ? ";
        setString(1, hFixBillType);
        setString(2, hBusinessDate);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hBatchSeq = getValue("h_batch_seq");
        }
    }

    /***********************************************************************/
    void printFirstRtn() throws Exception {
        int pCount = 0;
        double pSum = 0;
        String hTAddItem = "";

        hSystemDatef = "";
        sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDatef = getValue("h_system_date_f");
        }

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: " + rptName, 1);

        szTmp = String.format("%22s", "加檔統計表");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        buf = comcr.insertStr(buf, chinDate, 70);
        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "批    號:", 1);
        buf = comcr.insertStr(buf, hDcurBatchNo, 10);
        buf = comcr.insertStr(buf, "加檔日期:", 60);
        szTmp = String.format("%8d", comcr.str2long(hBusinessDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 68);

        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "筆  數 金         額 ", 30);
        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "====== ============= ", 30);
        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        sqlCmd = "select ";
        sqlCmd += "count(*) p_count,";
        sqlCmd += "sum(dest_amt) p_sum,";
        sqlCmd += "add_item ";
        sqlCmd += "from dbb_othexp ";
        sqlCmd += "where decode(post_flag,'','N',post_flag) != 'Y' ";
        sqlCmd += "and decode(apr_flag,'','N',apr_flag) = 'Y' ";
        sqlCmd += "group by add_item ";
        openCursor();
        while (fetchTable()) {
            pCount = getValueInt("p_count");
            pSum = getValueDouble("p_sum");
            hTAddItem = getValue("add_item");

            hTempX16 = "";
            sqlCmd = "select exter_desc ";
            sqlCmd += " from ptr_billtype  ";
            sqlCmd += "where bill_type = ?  ";
            sqlCmd += "and txn_code = ? ";
            setString(1, hFixBillType);
            setString(2, hTAddItem);
            recordCnt = selectTable();
            if (recordCnt > 0) {
                hTempX16 = getValue("exter_desc");
            }

            tempX16 = hTempX16;

            buf = "";
            buf = comcr.insertStr(buf, tempX16, 12);
            szTmp = comcr.commFormat("3z,3z", pCount);
            totalTransactionCnt = totalTransactionCnt + pCount;
            buf = comcr.insertStr(buf, szTmp, 29);
            szTmp = comcr.commFormat("3$,3$,3$", pSum);
            buf = comcr.insertStr(buf, szTmp, 39);
            totalTransactionAmt = totalTransactionAmt + pSum;
            lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));
        }
        closeCursor();

        buf = "";
        buf = comcr.insertStr(buf, "總  計:", 12);
        szTmp = comcr.commFormat("3z,3z", totalTransactionCnt);
        buf = comcr.insertStr(buf, szTmp, 29);
        szTmp = comcr.commFormat("3$,3$,3$", totalTransactionAmt);
        buf = comcr.insertStr(buf, szTmp, 39);
        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptName, hSystemDatef);
        comc.writeReport(filename, lpar111);

    }

    /***********************************************************************/
    void selectDbbOthexp() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "card_no,";
        sqlCmd += "seq_no,";
        sqlCmd += "dest_amt,";
        sqlCmd += "dest_curr,";
        sqlCmd += "purchase_date,";
        sqlCmd += "chi_desc,";
        sqlCmd += "bill_desc,";
        sqlCmd += "dept_flag,";
        sqlCmd += "apr_flag,";
        sqlCmd += "post_flag,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from dbb_othexp ";
        sqlCmd += "where decode(post_flag,'','N',post_flag) != 'Y' ";
        sqlCmd += "  and decode(apr_flag ,'','N',apr_flag)   = 'Y' ";
        openCursor();
        while (fetchTable()) {
            hDothBillType = getValue("bill_type");
            hDothTxnCode = getValue("txn_code");
            hDothCardNo = getValue("card_no");
            hDothSeqNo = getValueInt("seq_no");
            hDothDestAmt = getValueDouble("dest_amt");
            hDothDestCurr = getValue("dest_curr");
            hDothPurchaseDate = getValue("purchase_date");
            hDothChiDesc = getValue("chi_desc");
            hDothBillDesc = getValue("bill_desc");
            hDothDeptFlag = getValue("dept_flag");
            hDothAprFlag = getValue("apr_flag");
            hDothPostFlag = getValue("post_flag");
            hDothRowid = getValue("rowid");

            totalCount = totalCount + 1;

            if (hDothTxnCode.length() == 0 || hDothTxnCode.equals("06") || hDothTxnCode.equals("25")
                    || hDothTxnCode.equals("27") || hDothTxnCode.equals("29"))
                totalAmt = totalAmt - hDothDestAmt;
            else
                totalAmt = totalAmt + hDothDestAmt;

            hDcurBillType = hDothBillType;
            hDcurTxnCode = hDothTxnCode;
            hDcurCardNo = hDothCardNo;
            tempX10 = String.format("%-10d", hDothSeqNo);
            hDcurTransactionSource = tempX10;
            hDcurDestAmt = hDothDestAmt;
            hDcurSourceAmt = hDothDestAmt;
            hDcurDestCurr = hDothDestCurr;
            hDcurPurchaseDate = hDothPurchaseDate;
            hDcurThisCloseDate = hDothPurchaseDate;

            hDcurAcexterDesc = hDothBillDesc;

            hDcurBatchNo = tempX14;

            chkCrdCard();
            /* 預借現金 source = dest */
            hDcurDestCurr = "901";
            hDcurSourceCurr = hDcurDestCurr;

            selectActAcno();
            chkPtrBilltype();
            chkPtrActcode();

            hDcurTxConvtFlag = "Y";
            hDcurAcctitemConvtFlag = "Y";

            hTempX08 = "";
            sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_temp_x08 ";
            sqlCmd += " from dual ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hTempX08 = getValue("h_temp_x08");
            }
            tempX10 = String.format("%2.2s%s", hBusinessDate.substring(2), hTempX08);
            hDcurReferenceNo = tempX10;
            insertDbbCurpost();
            daoTable = "dbb_othexp";
            updateSQL = " post_flag    = 'Y',";
            updateSQL += " reference_no = ?, ";
            updateSQL += " from_code    = 'B1',";
            updateSQL += " process_flag = 'N',";
            updateSQL += " p_seqno      = ?, ";
            updateSQL += " acct_no      = ?, ";
            updateSQL += " mod_time     = sysdate ";
            whereStr = "where rowid =? ";
            setString(1, hDcurReferenceNo);
            setString(2, hDcurPSeqno);
            setString(3, hTtttAcctNo);
            setRowId(4, hDothRowid);
            updateTable();
            if (notFound.equals("Y")) {
                String stderr = "update_dbb_othexp not found!";
                comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
            }
        }
        closeCursor();
    }
    /*************************************************************************/
    void initCrdCard() {
        hCardBlockStatus = "";
        hCardBlockDate = "";
        hCardPSeqno = "";
        hCardMajorIdPSeqno = "";
        hCardIdPSeqno = "";
        hCardCardType = "";
        hCardBinNo = "";
        hCardBinType = "";
        hCardGroupCode = "";
        hCardSourceCode = "";
        hCardCurrentCode = "";
        hCardIssueDate = "";
        hCardMajorCardNo = "";
        hCardPromoteDept = "";
        hCardOppostDate = "";
        hCardProdNo = "";
    }

    /***********************************************************************/
    void chkCrdCard() throws Exception {
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
        sqlCmd += "p_seqno,";
        sqlCmd += "acct_no,";
        sqlCmd += "major_id_p_seqno, ";
        sqlCmd += "id_p_seqno ";
        sqlCmd += " from dbc_card  ";
        sqlCmd += "where card_no  = ? ";
        setString(1, hDcurCardNo);
        int recordCnt = selectTable();
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
            hCardBinNo    = getValue("bin_no");
            hCardBinType  = getValue("bin_type");
            hCardPSeqno   = getValue("p_seqno");
            hTtttAcctNo = getValue("acct_no");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
            hCardIdPSeqno       = getValue("id_p_seqno");
        }

        hDcurSourceCode = hCardSourceCode;
        hDcurMajorCardNo = hCardMajorCardNo;
        hDcurCurrentCode = hCardCurrentCode;
        hDcurOppostDate = hCardOppostDate;
        hDcurIssueDate = hCardIssueDate;
        hDcurPromoteDept = hCardPromoteDept;
        hDcurProdNo = hCardProdNo;
        hDcurGroupCode = hCardGroupCode;
        hDcurBinType = hCardBinType;
        hDcurPSeqno = hCardPSeqno;
        hDcurIdPSeqno = hCardIdPSeqno;
    }

    /***********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        hAcnoAcctStatus = "";
        hAcnoStmtCycle = "";
        hAcnoPayByStageFlag = "";
        hAcnoAutopayAcctNo = "";

        sqlCmd = "select acct_type,";
        sqlCmd += "acct_key,";
        sqlCmd += "acct_status,";
        sqlCmd += "stmt_cycle,";
        sqlCmd += "pay_by_stage_flag,";
        sqlCmd += "autopay_acct_no ";
        sqlCmd += " from dba_acno  ";
        sqlCmd += "where p_seqno  = ? ";
        setString(1, hDcurPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
        }

        hDcurAcctType = hAcnoAcctType;
        hDcurAcctKey = hAcnoAcctKey;
        hDcurAcctStatus = hAcnoAcctStatus;
        hDcurStmtCycle = hAcnoStmtCycle;
        hDcurPayByStageFlag = hAcnoPayByStageFlag;
        hDcurAutopayAcctNo = hAcnoAutopayAcctNo;
    }

    /***********************************************************************/
    void chkPtrActcode() throws Exception {
        sqlCmd = "select ";
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
        sqlCmd += " from ptr_actcode  ";
        sqlCmd += "where acct_code = ? ";
        setString(1, hDcurAcctCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actcode not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hPcodChiShortName = getValue("chi_short_name");
            hPcodEngShortName = getValue("eng_short_name");
            hPcodItemOrderNormal = getValue("item_order_normal");
            hPcodItemOrderBackDate = getValue("item_order_back_date");
            hPcodItemOrderRefund = getValue("item_order_refund");
            hPcodItemClassNormal = getValue("item_class_normal");
            hPcodItemClassBackDate = getValue("item_class_back_date");
            hPcodItemClassRefund = getValue("item_class_refund");
            hPcodAcctMethod = getValue("acct_method");
            hPcodQueryType = getValue("query_type");
        }

        hDcurQueryType = hPcodQueryType;
        hDcurAcctChiShortName = hPcodChiShortName;
        hDcurAcctEngShortName = hPcodEngShortName;
        hDcurItemClassNormal = hPcodItemClassNormal;
        hDcurItemClassBackDate = hPcodItemClassBackDate;
        hDcurItemClassRefund = hPcodItemClassRefund;
        hDcurItemOrderNormal = hPcodItemOrderNormal;
        hDcurItemOrderBackDate = hPcodItemOrderBackDate;
        hDcurItemOrderRefund = hPcodItemOrderRefund;
        hDcurAcctMethod = hPcodAcctMethod;
    }

    /***********************************************************************/
    void chkPtrBilltype() throws Exception {
        sqlCmd = "select bill_type,";
        sqlCmd += "txn_code,";
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
        sqlCmd += "inter_desc,";
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
        setString(1, hFixBillType);
        setString(2, hDcurTxnCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billtype not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBityBillType = getValue("bill_type");
            hBityTxnCode = getValue("txn_code");
            hBitySignFlag = getValue("sign_flag");
            hBityAcctCode = getValue("acct_code");
            hBityAcctItem = getValue("acct_item");
            hBityFeesState = getValue("fees_state");
            hBityFeesFixAmt = getValueDouble("fees_fix_amt");
            hBityFeesPercent = getValue("fees_percent");
            hBityFeesMin = getValue("fees_min");
            hBityFeesMax = getValue("fees_max");
            hBityFeesBillType = getValue("fees_bill_type");
            hBityFeesTxnCode = getValue("fees_txn_code");
            hBityInterDesc = getValue("inter_desc");
            hBityExterDesc = getValue("exter_desc");
            hBityInterestMode = getValue("interest_mode");
            hBityAdvWkday = getValue("adv_wkday");
            hBityBalanceState = getValue("balance_state");
            hBityCashAdvState = getValue("cash_adv_state");
            hBityEntryAcct = getValue("entry_acct");
            hBityChkErrBill = getValue("chk_err_bill");
            hBityDoubleChk = getValue("double_chk");
            hBityFormatChk = getValue("format_chk");
            hBityMerchFee = getValue("merch_fee");
        }

        hDcurBillType = hBityBillType;
        hDcurTxnCode = hBityTxnCode;
        hDcurSignFlag = hBitySignFlag;
        hDcurAcctCode = hBityAcctCode;
        hDcurAcctItem = hBityAcctItem;
        hDcurFeesState = hBityFeesState;
        hDcurFeesFixAmt = hBityFeesFixAmt;
        hDcurFeesPercent = hBityFeesPercent;
        hDcurFeesMin = hBityFeesMin;
        hDcurFeesMax = hBityFeesMax;
        hDcurFeesBillType = hBityFeesBillType;
        hDcurFeesTxnCode = hBityFeesTxnCode;
        hDcurInterestMode = hBityInterestMode;
        if (hDcurAcexterDesc.length() == 0) {
            hDcurAcexterDesc = hBityExterDesc;
        }

        if (hDcurTxnCode.equals("AF") || hDcurTxnCode.equals("LF")) {
            tempX20 = hBityExterDesc;
            tempX40 = String.format("%-20s%-19s", tempX20, hDothCardNo);
            hDcurAcexterDesc = tempX40;
        }

        if (hDothBillType.equals("OSSG") || hDothBillType.equals("OKOL"))
            hDcurMchtChiName = hBityExterDesc;

        //以客戶輸入的帳單說明寫入
        if (!(hDothBillDesc.length() == 0))  {
            tempX20 = hBityExterDesc;
            tempX40 = String.format("%-20s%-20s", tempX20, hDothBillDesc);
            hDcurAcexterDesc = tempX40;
            hDcurMchtChiName = tempX40;
        }
        
        hDcurAdvWkday = hBityAdvWkday;
        hDcurBalanceState = hBityBalanceState;
        hDcurCollectionMode = hBityCollectionMode;
        hDcurCashAdvState = hBityCashAdvState;
        hDcurFormatChkOkFlag = hBityFormatChk;
        hDcurDoubleChkOkFlag = hBityDoubleChk;
        hDcurErrChkOkFlag = hBityChkErrBill;
        hDcurMerchFee = hBityMerchFee;
        hDcurEntryAcct = hBityEntryAcct;
    }

    /***********************************************************************/
    void insertDbbCurpost() throws Exception {
        hDcurValidFlag = hBiunAuthFlag;

        setValue("reference_no", hDcurReferenceNo);
        setValue("bill_type", hDcurBillType);
        setValue("txn_code", hDcurTxnCode);
        setValue("card_no", hDcurCardNo);
        setValue("film_no", hDcurFilmNo);
        setValue("acq_member_id", hDcurAcqMemberId);
        setValue("purchase_date", hDcurPurchaseDate);
        setValueDouble("dest_amt", hDcurDestAmt);
        setValue("dest_curr", hDcurDestCurr);
        setValueDouble("source_amt", hDcurSourceAmt);
        setValue("source_curr", hDcurSourceCurr);
        setValue("mcht_eng_name", hDcurMchtEngName);
        setValue("mcht_city", hDcurMchtCity);
        setValue("mcht_country", hDcurMchtCountry);
        setValue("mcht_category", hDcurMchtCategory);
        setValue("mcht_zip", hDcurMchtZIp);
        setValue("mcht_state", hDcurMchtState);
        setValueDouble("settl_amt", hDcurSettlAmt);
        setValue("auth_code", hDcurAuthorization);
        setValue("pos_entry_mode", hDcurPosEntryMode);
        setValue("process_date", hDcurProcessDate);
        setValue("mcht_no", hDcurMchtNo);
        setValue("mcht_chi_name", hDcurMchtChiName);
        setValue("acquire_date", hDcurAcquireDate);
        setValue("contract_no", hDcurContractNo);
        setValue("term", hDcurTerm);
        setValue("total_term", hDcurTotalTerm);
        setValue("batch_no", hDcurBatchNo);
        setValue("sign_flag", hDcurSignFlag);
        setValue("acct_code", hDcurAcctCode);
        setValue("acct_item", hDcurAcctItem);
        setValue("acct_eng_short_name", hDcurAcctEngShortName);
        setValue("acct_chi_short_name", hDcurAcctChiShortName);
        setValue("item_order_normal", hDcurItemOrderNormal);
        setValue("item_order_back_date", hDcurItemOrderBackDate);
        setValue("item_order_refund", hDcurItemOrderRefund);
        setValue("acexter_desc", hDcurAcexterDesc);
        setValue("entry_acct", hDcurEntryAcct);
        setValue("item_class_normal", hDcurItemClassNormal);
        setValue("item_class_back_date", hDcurItemClassBackDate);
        setValue("item_class_refund", hDcurItemClassRefund);
        setValue("collection_mode", hDcurCollectionMode);
        setValue("fees_state", hDcurFeesState);
        setValue("cash_adv_state", hDcurCashAdvState);
        setValue("this_close_date", hDcurThisCloseDate);
        setValue("manual_upd_flag", hDcurManualUpdFlag);
        setValue("valid_flag", hDcurValidFlag);
        setValue("doubt_type", hDcurDoubtType);
        setValue("duplicated_flag", hDcurDuplicatedFlag);
        setValue("rsk_type", hDcurRskType);
        setValue("acct_type", hDcurAcctType);
        setValue("stmt_cycle", hDcurStmtCycle);
        setValue("major_card_no", hDcurMajorCardNo);
        setValue("promote_dept", hDcurPromoteDept);
        setValue("issue_date", hDcurIssueDate);
        setValue("prod_no"   , hDcurProdNo);
        setValue("group_code", hDcurGroupCode);
        setValue("bin_type"  , hDcurBinType);
        setValue("p_seqno"   , hDcurPSeqno);
        setValue("major_id_p_seqno"     , hCardMajorIdPSeqno);
        setValue("id_p_seqno"           , hDcurIdPSeqno);
        setValue("reference_no_original", hDcurReferenceNoOriginal);
        setValue("fees_reference_no"    , hDcurFeesReferenceNo);
        setValue("tx_convt_flag"        , hDcurTxConvtFlag);
        setValue("acctitem_convt_flag"  , hDcurAcctitemConvtFlag);
        setValue("format_chk_ok_flag"   , hDcurFormatChkOkFlag);
        setValue("double_chk_ok_flag"   , hDcurDoubleChkOkFlag);
        setValue("err_chk_ok_flag", hDcurErrChkOkFlag);
        setValue("source_code", hDcurSourceCode);
        setValue("curr_post_flag", hDcurCurrPostFlag);
        setValue("mod_user", hDcurModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hDcurModPgm);
        setValueLong("mod_seqno", hDcurModSeqno);
        daoTable = "dbb_curpost";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_curpost duplicate", "", comcr.hCallBatchSeqno);
        }

        // **insert bil_nccc300_dtl
        setValue("reference_no", hDcurReferenceNo);
        setValue("card_no", hDcurCardNo);
        setValue("tmp_request_flag", hDcurTmpRequestFlag);
        setValue("usage_code", hDcurUsageCode);
        setValue("reason_code", hDcurReasonCode);
        setValue("settlement_flag", hDcurSettlementFlag);
        setValueDouble("SETTLEMENT_AMT", hDcurSettlAmt);
        setValue("tmp_service_code", hDcurTmpServiceCode);
        setValue("pos_term_capability", hDcurPosTermCapability);
        setValue("pos_pin_capability", hDcurPosPinCapability);
        setValue("pos_entry_mode", hDcurPosEntryMode);
        setValue("reimbursement_attr", hDcurReimbursementAttr);
        setValue("ec_ind", hDcurEcInd);
        setValue("second_conversion_date", hDcurSecondConversionDate);
        setValue("electronic_term_ind", hDcurElectronicTermInd);
        setValue("transaction_source", hDcurTransactionSource);
        setValue("original_no", hDcurOriginalNo);
        setValue("batch_no", hDcurBatchNo);
        setValue("exchange_rate", hDcurExchangeRate);
        setValue("exchange_date", hDcurExchangeDate);
        setValue("query_type", hDcurQueryType);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hCurpModPgm);
        daoTable = "bil_nccc300_dtl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_curpost duplicate in insert_bill_curpost()", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        hPostThisCloseDate = hBusinessDate;
        setValue("batch_date", hPostBatchDate);
        setValue("batch_unit", hPostBatchUnit);
        setValueInt("batch_seq", hPostBatchSeq);
        setValue("batch_no", hPostBatchNo);
        setValueInt("tot_record", hPostTotRecord);
        setValueDouble("tot_amt", hPostTotAmt);
        setValue("confirm_flag_p", hPostConfirmFlagp);
        setValue("confirm_flag", hPostConfirmFlag);
        setValue("this_close_date", hPostThisCloseDate);
        setValue("mod_user", hDcurModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", hDcurModPgm);
        setValueLong("mod_seqno", hDcurModSeqno);
        daoTable = "bil_postcntl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate", "", comcr.hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbbA001 proc = new DbbA001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
