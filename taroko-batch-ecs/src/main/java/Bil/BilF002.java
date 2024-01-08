/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/30  V1.00.01    shiyuqi       updated for project coding standard   * 
*  111/06/16  V1.00.02    Justin    弱點修正                                  *
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

/*其他費用轉換作業*/
public class BilF002 extends AccessDAO {
    private String progname = "其他費用轉換作業  111/06/16  V1.00.02  ";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hTempUser = "";
    int debug = 0;
    int totalCnt = 0;

    String prgmId = "BilF002";
    String prgmName = "其他費用轉換作業";
  //String rptName = "BIL_F002R0";
    String rptId   = "BIL_F002R0";
    String rptName = "加檔統計表";
    List<Map<String, Object>> lpar111 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    int errCnt = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    long hSrcPgmPostseq = 0;
    String hPostNote = "其他費用加檔金額 by bill_type,txn_code";
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;

    String hSystemDateF = "";
    String hBusinessDate = "";
    String hSystemDate = "";
    String hCurrencyCode = "";
    String hPbtbBinType = "";
    String hCurpCardNo = "";
    String hBiunConfFlag = "";
    // String h_biun_auth_flag = "";
    String hFixBillType = "";
    String hBatchSeq = "";
    String hOtheBillType = "";
    String hOtheTxCode = "";
    String hOtheCardNo = "";
    int hOtheSeqNo = 0;
    double hOtheDestAmt = 0;
    String hOtheDestCurr = "";
    String hOthePurchaseDate = "";
    String hOtheChiDesc = "";
    String hOtheBillDesc = "";
    String hOtheDeptFlag = "";
    String hOthePostFlag = "";
    String hOtheCurrCode = "";
    double hOtheDcDestAmt = 0;
    String hOtheRowid = "";
    String hTempX08 = "";
    String hCurpReferenceNo = "";
    String hBityBillType = "";
    String hBityTxnCode = "";
    String hBitySignFlag = "";
    String hBityAcctCode = "";
    String hBityAcctItem = "";
    String hBityFeesState = "";
    double hBityFeesFixAmt = 0;
    double hBityFeesPercent = 0;
    double hBityFeesMin = 0;
    double hBityFeesMax = 0;
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
    double hBityMerchFee = 0;
    String hCurpBillType = "";
    String hCurpTxnCode = "";
    String hPcodAcctCode = "";
    String hPcodChiShortName = "";
    String hPcodChiLongName = "";
    String hPcodEngShortName = "";
    String hPcodEngLongName = "";
    String hPcodItemOrderNormal = "";
    String hPcodItemOrderBackDate = "";
    String hPcodItemOrderRefund = "";
    String hPcodItemClassNormal = "";
    String hPcodItemClassBackDate = "";
    String hPcodItemClassRefund = "";
    String hPcodInterRateCode = "";
    String hPcodPartRev = "";
    String hPcodRevolve = "";
    String hPcodAcctMethod = "";
    String hPcodInterestMethod = "";
    String hPcodUrge1St = "";
    String hPcodUrge2St = "";
    String hPcodUrge3St = "";
    String hPcodOccupy = "";
    String hPcodReceivables = "";
    String hPcodQueryType = "";
    String hCurpSignFlag = "";
    String hCurpAcctCode = "";
    String hPostBatchDate = "";
    String hPostBatchUnit = "";
    int hPostBatchSeq = 0;
    String hPostBatchNo = "";
    int hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hPostConfirmFlagP = "";
    String hPostConfirmFlag = "";
    String hPostThisCloseDate = "";
    String hCurpTransactionSeqNo = "";
    String hCurpFilmNo = "";
    String hCurpAcquirerMemberId = "";
    String hCurpPurchaseTime = "";
    String hCurpPurchaseDate = "";
    String hCurpDestAmtChar = "";
    double hCurpDestAmt = 0;
    String hCurpDestCurr = "";
    String hCurpSourceAmtChar = "";
    double hCurpSourceAmt = 0;
    String hCurpSourceCurr = "";
    String hCurpMchtEngName = "";
    String hCurpMchtCity = "";
    String hCurpMchtCountry = "";
    String hCurpMchtCategory = "";
    String hCurpMchtZip = "";
    String hCurpMchtState = "";
    String hCurpTmpRequestFlag = "";
    String hCurpUsageCode = "";
    String hCurpReasonCode = "";
    String hCurpTmpServiceCode = "";
    String hCurpAuthorization = "";
    String hCurpPosTermCapability = "";
    String hCurpPosEntryMode = "";
    String hCurpProcessDate = "";
    String hCurpReimbursementAttr = "";
    String hCurpEcInd = "";
    String hCurpFirstConversionDate = "";
    String hCurpSecondConversionDate = "";
    String hCurpMchtNo = "";
    String hCurpMchtChiName = "";
    String hCurpElectronicTermInd = "";
    String hCurpTransactionSource = "";
    String hCurpAcquireDate = "";
    String hCurpContractNo = "";
    String hCurpGoodsName = "";
    String hCurpOriginalNo = "";
    String hCurpTelephoneNo = "";
    String hCurpTerm = "";
    String hCurpTotalTerm = "";
    String hCurpProdName = "";
    String hCurpBatchNo = "";
    String hCurpExchangeRate = "";
    String hCurpExchangeDate = "";
    String hCurpAcctItem = "";
    String hCurpAcctEngShortName = "";
    String hCurpAcctChiShortName = "";
    String hCurpItemOrderNormal = "";
    String hCurpItemOrderBackDate = "";
    String hCurpItemOrderRefund = "";
    String hCurpAcexterDesc = "";
    String hCurpEntryAcct = "";
    String hCurpItemClassNormal = "";
    String hCurpItemClassBackDate = "";
    String hCurpItemClassRefund = "";
    String hCurpAcctMethod = "";
    String hCurpInterestMode = "";
    String hCurpAdvWkday = "";
    String hCurpCollectionMode = "";
    String hCurpFeesState = "";
    double hCurpFeesFixAmt = 0;
    double hCurpFeesPercent = 0;
    double hCurpFeesMin = 0;
    double hCurpFeesMax = 0;
    String hCurpFeesBillType = "";
    String hCurpFeesTxnCode = "";
    String hCurpBalanceState = "";
    String hCurpCashAdvState = "";
    double hCurpMerchFee = 0;
    String hCurpThisCloseDate = "";
    String hCurpManualUpdFlag = "";
    // String h_curp_valid_flag = "";
    String hCurpDoubtType = "";
    String hCurpDuplicatedFlag = "";
    String hCurpRskType = "";
    String hCurpAcctType = "";
    String hCurpAcctKey = "";
    String hCurpAcctStatus = "";
    String hCurpStmtCycle = "";
    String hCurpPayByStageFlag = "";
    String hCurpAutopayAcctNo = "";
    String hCurpMajorCardNo = "";
    String hCurpCurrentCode = "";
    String hCurpOppostDate = "";
    String hCurpPromoteDept = "";
    String hCurpIssueDate = "";
    String hCurpContractFlag = "";
    String hCurpProdNo = "";
    String hCurpGroupCode = "";
    String hCurpBinType = "";
    String hCurpPSeqno = "";
    String hCurpGpNo = "";
    String hCurpIdPSeqno = "";
    String hCurpMajorIdPSeqno = "";
    String hCurpReferenceNoOriginal = "";
    String hCurpFeesReferenceNo = "";
    String hCurpTxConvtFlag = "";
    String hCurpAcctitemConvtFlag = "";
    String hCurpFormatChkOkFlag = "";
    String hCurpDoubleChkOkFlag = "";
    String hCurpErrChkOkFlag = "";
    String hCurpSourceCode = "";
    String hCurpQueryType = "";
    String hCurpCurrPostFlag = "";
    double hCurpDcExchangeRate = 0;
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
    String hCardGpNo = "";
    String hCardIdPSeqno = "";
    String hCardMajorIdPSeqno = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    String tempBillType = "";
    String hTempCurrCode = "";
    int pCount = 0;
    double pSum = 0;
    String hTBillType = "";
    String hTAddItem = "";
    String hTAcctStatus = "";
    String hPrintName = "";
    String hRptName = "";

    String[] hTotAcctType = new String[20];
    String[] hTotCurrCode = new String[20];

    int rowsRun = 0;
    int totalCount = 0;
    int totalTransactionCnt = 0;
    int totCnt = 0;
    int fileCount = 0;
    double totalAmt = 0;
    double totalTransactionAmt = 0;
    double hThisBusiAddAmt = 0;
    String swFptr111 = "";
    String hTempAcctType = "";
    String tempX14 = "";
    String tempX20 = "";
    String tempX40 = "";
    String tempX10 = "";
    String sBillUnit = "";
    // ************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname + "," + args.length);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                String errMsg2 = "        1.batch_no : 批次號碼\n";
                errMsg2 += "            a.yyyymmdd:西元日期\n";
                errMsg2 += "            b.請款來源前二碼:'NC','OB','OU'\n";
                errMsg2 += "            c.序號: 4 碼\n";
				exceptExit = -1;
                comc.errExit("Usage : BilF002 callbatch_seqno", errMsg2);
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hSystemDateF = "";
            sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
            sqlCmd += " from dual ";
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hSystemDateF = getValue("h_system_date_f");
            }

            hFixBillType = "OKOL";
            showLogMessage("I", "", "888 111 select TYPE =[" + hFixBillType + "]");

            commonRtn();
            
            hModPgm = javaProgram;
            hCurpModPgm = hModPgm;
            hCurpModTime = hModTime;
            hCurpModUser = hModUser;
            hCurpModSeqno = hModSeqno;

            sqlCmd = "select a.acct_type,";
            sqlCmd += "b.CURR_CODE ";
            sqlCmd += " from bil_othexp a, ptr_acct_type b  ";
            sqlCmd += "where decode(a.post_flag,'','N',a.post_flag) != 'Y'  ";
            sqlCmd += "and (a.apr_user != '' or a.apr_flag = 'Y') ";
            sqlCmd += "and a.acct_type = b.acct_type group by a.acct_type,b.CURR_CODE ";
            recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                hTotAcctType[i] = getValue("acct_type", i);
                hTotCurrCode[i] = getValue("CURR_CODE", i);
            }
            // rows_run = recordCnt;

            swFptr111 = "N";
            rowsRun = 1;
            for (int int1 = 0; int1 < rowsRun; int1++) {

                hTempAcctType = hTotAcctType[int1];
                hTempCurrCode = hTotCurrCode[int1];

                totalCount = 0;
                totalTransactionCnt = 0;
                totalTransactionAmt = 0;

                selectBilOthexp();

                if (totalCount > 0) {
                    swFptr111 = "Y";
                    chkBillunitEnd();
                    buf = "";
                    buf = comcr.insertStr(buf, "總  計:", 12);
                    szTmp = comcr.commFormat("3z,3z", totalTransactionCnt);
                    buf = comcr.insertStr(buf, szTmp, 29);
                    szTmp = comcr.commFormat("3$,3$,3$", totalTransactionAmt);
                    buf = comcr.insertStr(buf, szTmp, 39);
                    lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", buf));
                    //String filename = String.format("%s/reports/%s_%s%1d", comc.getECSHOME(), rptId, hSystemDateF,
                    //        fileCount);
                    //filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
                    comcr.insertPtrBatchRpt(lpar111);
                    //comc.writeReportForTest(filename, lpar111);
                }
            }

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
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
        sqlCmd = "select BUSINESS_DATE ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("BUSINESS_DATE");
        } else {
            stderr = "select_prt_businday  not found";
            comcr.errRtn(stderr, "", hCallBatchSeqno);
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
    void selectBilOthexp() throws Exception {
        initBilOthexp();
        initBilCurpost();

        sqlCmd = "select ";
        sqlCmd += "a.bill_type,";
        sqlCmd += "a.tx_code,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.seq_no,";
        sqlCmd += "a.dest_amt,";
        sqlCmd += "decode(a.dest_curr,'','901',a.dest_curr) h_othe_dest_curr,";
        sqlCmd += "a.purchase_date,";
        sqlCmd += "a.chi_desc,";
        sqlCmd += "a.bill_desc,";
        sqlCmd += "a.dept_flag,";
        sqlCmd += "a.post_flag,";
        sqlCmd += "decode(a.curr_code,'','901',a.curr_code) h_othe_curr_code,";
        sqlCmd += "a.dc_dest_amt,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from bil_othexp a ";
        sqlCmd += "where decode(post_flag,'','N',post_flag) != 'Y' ";
        sqlCmd += "  and apr_user   != '' ";
        sqlCmd += "  and card_no    != '' ";
        sqlCmd += "  and bill_type  != '' ";
        openCursor();
        while (fetchTable()) {
            hOtheBillType = getValue("bill_type");
            hOtheTxCode = getValue("tx_code");
            hOtheCardNo = getValue("card_no");
            if (debug == 1)
                showLogMessage("I", "", "888 select card =[" + hOtheCardNo + "]");
            hOtheSeqNo = getValueInt("seq_no");
            hOtheDestAmt = getValueDouble("dest_amt");
            hOtheDestCurr = getValue("h_othe_dest_curr");
            hOthePurchaseDate = getValue("purchase_date");
            hOtheChiDesc = getValue("chi_desc");
            hOtheBillDesc = getValue("bill_desc");
            hOtheDeptFlag = getValue("dept_flag");
            hOthePostFlag = getValue("post_flag");
            hOtheCurrCode = getValue("h_othe_curr_code");
            hOtheDcDestAmt = getValueDouble("dc_dest_amt");
            hOtheRowid = getValue("rowid");

            if (totalCount == 0) {
                tempBillType = hOtheBillType;
                hFixBillType = hOtheBillType;
                chkBillunit();
            }

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Bil Process 1 record=[%d]\n", totCnt));

            if (hOtheBillType.equals(tempBillType) == false) {
                chkBillunitEnd();
                tempBillType = hOtheBillType;
                hFixBillType = hOtheBillType;
                chkBillunit();
                totalCount = 0;
                totalAmt = 0;
            }

            totalCount = totalCount + 1;

            hCurpSourceCurr = hOtheDestCurr;
            hCurpDestCurr = hOtheDestCurr;
            hCurpSourceAmt = hOtheDestAmt;
            hCurpDestAmt = hOtheDestAmt;

            /* 外幣的destination_amt, 各程式自己算 */
            if (hOtheCurrCode.equals("901") == false) {
                hCurpSourceCurr = hOtheCurrCode;
                hCurpSourceAmt = hOtheDcDestAmt;
                hCurpDestCurr = "901";
                selectPtrCurrRate();
            } else {
                hOtheDcDestAmt = hOtheDestAmt;
            }

            if (hOtheTxCode.length() == 0 || hOtheTxCode.equals("06") || hOtheTxCode.equals("25")
                    || hOtheTxCode.equals("27") || hOtheTxCode.equals("29"))
                totalAmt = totalAmt - hOtheDestAmt;
            else
                totalAmt = totalAmt + hOtheDestAmt;

            hCurpBillType = hOtheBillType;
            hCurpTxnCode = hOtheTxCode;
            hCurpCardNo = hOtheCardNo;
            hCurpTransactionSource = String.format("%-10d", hOtheSeqNo);

            hCurpPurchaseDate = hOthePurchaseDate;
            hCurpThisCloseDate = hOthePurchaseDate;

            hCurpAcexterDesc = hOtheBillDesc;

            hCurpBatchNo = tempX14;

            chkCrdCard();
            selectPtrBintable();

            selectActAcno();
            chkPtrBilltype();
            chkPtrActcode();

            hCurpTxConvtFlag = "Y";
            hCurpAcctitemConvtFlag = "Y";

            hTempX08 = "";
            sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_temp_x08 ";
            sqlCmd += " from dual ";
            int recCnt = selectTable();
            if (recCnt > 0) {
                hTempX08 = getValue("h_temp_x08");
            }
            /* %2.2s 強制 補 2 碼 */
            hCurpReferenceNo = String.format("%2.2s%s", hBusinessDate.substring(2), hTempX08);
            insertBilCurpost();
            daoTable = "bil_othexp";
            updateSQL = " post_flag  = 'Y',";
            updateSQL += " mod_time   = sysdate ";
            whereStr = "where rowid = ? ";
            setRowId(1, hOtheRowid);
            updateTable();
            if (notFound.equals("Y")) {
                stderr = "update_bil_othexp not found!";
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }

            sqlCmd = "insert into bil_sysexp_hst  ";
            sqlCmd += "(bill_type,";
            sqlCmd += "txn_code,";
            sqlCmd += "add_item,";
            sqlCmd += "card_no,";
            sqlCmd += "p_seqno ,";
            sqlCmd += "acct_type,";
            sqlCmd += "corp_no,";
            sqlCmd += "seq_no,";
            sqlCmd += "dest_amt,";
            sqlCmd += "dest_curr,";
            sqlCmd += "purchase_date,";
            sqlCmd += "chi_desc,";
            sqlCmd += "bill_desc,";
            sqlCmd += "dept_flag,";
            sqlCmd += "post_flag,";
            sqlCmd += "mod_user,";
            sqlCmd += "mod_time,";
            sqlCmd += "mod_pgm,";
            sqlCmd += "mod_seqno,";
            sqlCmd += "key_no,";
            sqlCmd += "dc_dest_amt,";
            sqlCmd += "curr_code,";
            sqlCmd += "id_code,";
            sqlCmd += "reference_no,";
            sqlCmd += "crt_date,";
            sqlCmd += "data_src)";
            
            sqlCmd += " select ";
            sqlCmd += "a.bill_type,";
            sqlCmd += "a.tx_code,";
            sqlCmd += "a.add_item,";
            sqlCmd += "a.card_no,";
            sqlCmd += "b.acno_p_seqno  ,";
            sqlCmd += "a.acct_type,";
            sqlCmd += "a.corp_no,";
            sqlCmd += "a.seq_no,";
            sqlCmd += "a.dest_amt,";
            sqlCmd += "a.dest_curr,";
            sqlCmd += "a.purchase_date,";
            sqlCmd += "a.chi_desc,";
            sqlCmd += "a.bill_desc,";
            sqlCmd += "a.dept_flag,";
            sqlCmd += "a.post_flag,";
            sqlCmd += "a.mod_user,";
            sqlCmd += "a.mod_time,";
            sqlCmd += "a.mod_pgm,";
            sqlCmd += "a.mod_seqno,";
            sqlCmd += "a.key_no,";
            sqlCmd += "a.dc_dest_amt,";
            sqlCmd += "decode(a.curr_code,'','901',a.curr_code),";
            sqlCmd += "'',";
            sqlCmd += "?,";
            sqlCmd += "?,";
            sqlCmd += "'2' ";
            sqlCmd += "  from bil_othexp a, crd_card b ";
            sqlCmd += " where a.rowid     = ? ";
            sqlCmd += "   and b.card_no = a.card_no ";
            setString(1, hCurpReferenceNo);
            setString(2, hBusinessDate);
            setRowId( 3, hOtheRowid);
            insertTable();
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_bil_sysexp_hst duplicate", "", hCallBatchSeqno);
            }
            daoTable = "bil_othexp";
            whereStr = "where rowid  = ? ";
            setRowId(1, hOtheRowid);
            deleteTable();
            if (notFound.equals("Y")) {
                String stderr = "delete_bil_othexp    not found!";
                comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
            }
        }
        closeCursor();
    }

    /***********************************************************************/
    void chkBillunitEnd() throws Exception {
        hPostBatchSeq = comcr.str2int(hBatchSeq);
        hPostTotRecord = totalCount;
        hPostTotAmt = totalAmt;

        hPostConfirmFlag = hBiunConfFlag;
        // h_post_auth_flag = h_biun_auth_flag;
        hPostConfirmFlagP = "N";
        if (hPostConfirmFlag.toUpperCase(Locale.TAIWAN).equals("Y") == false) {
            hPostConfirmFlagP = "Y";
        }

        insertBilPostcntl();
    }

    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        hPostThisCloseDate = hBusinessDate;

        setValue("batch_date", hPostBatchDate);
        setValue("batch_unit", hPostBatchUnit);
        setValueInt("batch_seq", hPostBatchSeq);
        setValue("batch_no", hPostBatchNo);
        setValueDouble("tot_record", hPostTotRecord);
        setValueDouble("tot_amt", hPostTotAmt);
        setValue("confirm_flag_p", hPostConfirmFlagP);
        setValue("confirm_flag", hPostConfirmFlag);
        setValue("this_close_date", hPostThisCloseDate);
        setValue("mod_user", hCurpModUser);
        setValue("mod_pgm", prgmId);
        setValue("mod_time", sysDate + sysTime);
        setValueLong("mod_seqno", hCurpModSeqno);
        daoTable = "bil_postcntl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectPtrCurrRate() throws Exception {

        hCurpDcExchangeRate = 0;
        sqlCmd = "select exchange_rate ";
        sqlCmd += " from ptr_curr_rate  ";
        sqlCmd += "where curr_code = ? ";
        setString(1, hOtheCurrCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            stderr = "select_" + daoTable + " not found!";
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCurpDcExchangeRate = getValueDouble("exchange_rate");
        }
    }

    /***********************************************************************/
    void selectPtrBintable() throws Exception {
        hCurrencyCode = "";
        hPbtbBinType = "";
        sqlCmd  = "select ";
        sqlCmd += "bin_type ";
        sqlCmd += "  from ptr_bintable  ";
        sqlCmd += " where bin_no = ? ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hCardBinNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCurrencyCode = "901";
            hPbtbBinType = getValue("bin_type");
        }
    }

    /**********************************************************************/
    void initCrdCard() {
        hCardPSeqno = "";
        hCardGpNo = "";
        hCardIdPSeqno = "";
        hCardMajorIdPSeqno = "";
        hCardCardType = "";
        hCardBinType = "";
        hCardBinNo = "";
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
        initCrdCard();
        sqlCmd = "select major_card_no,";
        sqlCmd += "current_code,";
        sqlCmd += "issue_date,";
        sqlCmd += "oppost_date,";
        sqlCmd += "promote_dept,";
        sqlCmd += "prod_no,";
        sqlCmd += "group_code,";
        sqlCmd += "source_code,";
        sqlCmd += "card_type,";
        sqlCmd += "bin_no,  ";
        sqlCmd += "bin_type,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "p_seqno  ,";
        sqlCmd += "id_p_seqno, ";
        sqlCmd += "major_id_p_seqno ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no  = ? ";
        setString(1, hCurpCardNo);
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
            hCardBinNo = getValue("bin_no");
            hCardBinType = getValue("bin_type");
            hCardPSeqno = getValue("acno_p_seqno");
            hCardGpNo = getValue("p_seqno");
            hCardIdPSeqno = getValue("id_p_seqno");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
        }

        hCurpSourceCode = hCardSourceCode;
        hCurpMajorCardNo = hCardMajorCardNo;
        hCurpCurrentCode = hCardCurrentCode;
        hCurpOppostDate = hCardOppostDate;
        hCurpIssueDate = hCardIssueDate;
        hCurpPromoteDept = hCardPromoteDept;
        hCurpProdNo = hCardProdNo;
        hCurpGroupCode = hCardGroupCode;
        hCurpBinType = hCardBinType;
        hCurpPSeqno = hCardPSeqno;
        hCurpGpNo = hCardGpNo;
        hCurpIdPSeqno = hCardIdPSeqno;
        hCurpMajorIdPSeqno = hCardMajorIdPSeqno;

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
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno  = ? ";
        setString(1, hCurpPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
        }

        hCurpAcctType = hAcnoAcctType;
        hCurpAcctKey = hAcnoAcctKey;
        hCurpAcctStatus = hAcnoAcctStatus;
        hCurpStmtCycle = hAcnoStmtCycle;
        hCurpPayByStageFlag = hAcnoPayByStageFlag;
        hCurpAutopayAcctNo = hAcnoAutopayAcctNo;
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
        // sqlCmd += "collection_mode,";
        sqlCmd += "cash_adv_state,";
        sqlCmd += "entry_acct,";
        sqlCmd += "chk_err_bill,";
        sqlCmd += "double_chk,";
        sqlCmd += "format_chk,";
        sqlCmd += "merch_fee ";
        sqlCmd += " from ptr_billtype  ";
        sqlCmd += "where bill_type = ?  ";
        sqlCmd += "and txn_code = ? ";
        setString(1, hCurpBillType);
        setString(2, hCurpTxnCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billtype not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBityBillType = getValue("bill_type");
            hBityTxnCode = getValue("txn_code");
            hBitySignFlag = getValue("sign_flag");
            hBityAcctCode = getValue("acct_code");
            hBityAcctItem = getValue("acct_item");
            hBityFeesState = getValue("fees_state");
            hBityFeesFixAmt = getValueDouble("fees_fix_amt");
            hBityFeesPercent = getValueDouble("fees_percent");
            hBityFeesMin = getValueDouble("fees_min");
            hBityFeesMax = getValueDouble("fees_max");
            hBityFeesBillType = getValue("fees_bill_type");
            hBityFeesTxnCode = getValue("fees_txn_code");
            hBityInterDesc = getValue("inter_desc");
            hBityExterDesc = getValue("exter_desc");
            hBityInterestMode = getValue("interest_mode");
            hBityAdvWkday = getValue("adv_wkday");
            hBityBalanceState = getValue("balance_state");
            // h_bity_collection_mode = getValue("collection_mode");
            hBityCashAdvState = getValue("cash_adv_state");
            hBityEntryAcct = getValue("entry_acct");
            hBityChkErrBill = getValue("chk_err_bill");
            hBityDoubleChk = getValue("double_chk");
            hBityFormatChk = getValue("format_chk");
            hBityMerchFee = getValueDouble("merch_fee");
        }

        hCurpBillType = hBityBillType;
        hCurpTxnCode = hBityTxnCode;
        hCurpSignFlag = hBitySignFlag;
        hCurpAcctCode = hBityAcctCode;
        hCurpAcctItem = hBityAcctItem;
        hCurpFeesState = hBityFeesState;
        hCurpFeesFixAmt = hBityFeesFixAmt;
        hCurpFeesPercent = hBityFeesPercent;
        hCurpFeesMin = hBityFeesMin;
        hCurpFeesMax = hBityFeesMax;
        hCurpFeesBillType = hBityFeesBillType;
        hCurpFeesTxnCode = hBityFeesTxnCode;
        hCurpInterestMode = hBityInterestMode;
        if (hCurpAcexterDesc.length() == 0) {
            hCurpAcexterDesc = hBityExterDesc;
        }

        tempX20 = "";
        tempX40 = "";

        if (hCurpTxnCode.equals("AF") || hCurpTxnCode.equals("LF")) {
            tempX20 = hBityExterDesc;
            tempX40 = String.format("%-20.20s%-19.19s", tempX20, hOtheCardNo);
            hCurpAcexterDesc = tempX40;
        }

        if (hOtheBillType.equals("OSSG") || hOtheBillType.equals("BTAO") || hOtheBillType.equals("OKOL"))
            hCurpMchtChiName = hBityExterDesc;

        if (hCurpTxnCode.equals("OI") || hCurpTxnCode.equals("05") || hCurpTxnCode.equals("HC")) {
            tempX20 = hBityExterDesc;
            tempX40 = String.format("%-20.20s%-20.20s", tempX20, hOtheBillDesc);
            hCurpAcexterDesc = tempX40;
            hCurpMchtChiName = tempX40;
        }
        hCurpAdvWkday = hBityAdvWkday;
        hCurpBalanceState = hBityBalanceState;
        hCurpCollectionMode = hBityCollectionMode;
        hCurpCashAdvState = hBityCashAdvState;
        hCurpFormatChkOkFlag = hBityFormatChk;
        hCurpDoubleChkOkFlag = hBityDoubleChk;
        hCurpErrChkOkFlag = hBityChkErrBill;
        hCurpMerchFee = hBityMerchFee;
        hCurpEntryAcct = hBityEntryAcct;
    }

    /***********************************************************************/
    void chkPtrActcode() throws Exception {
        sqlCmd = "select acct_code,";
        sqlCmd += "chi_short_name,";
        sqlCmd += "chi_long_name,";
        sqlCmd += "eng_short_name,";
        sqlCmd += "eng_long_name,";
        sqlCmd += "item_order_normal,";
        sqlCmd += "item_order_back_date,";
        sqlCmd += "item_order_refund,";
        sqlCmd += "item_class_normal,";
        sqlCmd += "item_class_back_date,";
        sqlCmd += "item_class_refund,";
        sqlCmd += "inter_rate_code,";
        sqlCmd += "part_rev,";
        sqlCmd += "revolve,";
        sqlCmd += "acct_method,";
        sqlCmd += "interest_method,";
        sqlCmd += "urge_1st,";
        sqlCmd += "urge_2st,";
        sqlCmd += "urge_3st,";
        sqlCmd += "occupy,";
        sqlCmd += "receivables,";
        sqlCmd += "query_type ";
        sqlCmd += " from ptr_actcode  ";
        sqlCmd += "where acct_code = ? ";
        setString(1, hCurpAcctCode);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPcodAcctCode = getValue("acct_code");
            hPcodChiShortName = getValue("chi_short_name");
            hPcodChiLongName = getValue("chi_long_name");
            hPcodEngShortName = getValue("eng_short_name");
            hPcodEngLongName = getValue("eng_long_name");
            hPcodItemOrderNormal = getValue("item_order_normal");
            hPcodItemOrderBackDate = getValue("item_order_back_date");
            hPcodItemOrderRefund = getValue("item_order_refund");
            hPcodItemClassNormal = getValue("item_class_normal");
            hPcodItemClassBackDate = getValue("item_class_back_date");
            hPcodItemClassRefund = getValue("item_class_refund");
            hPcodInterRateCode = getValue("inter_rate_code");
            hPcodPartRev = getValue("part_rev");
            hPcodRevolve = getValue("revolve");
            hPcodAcctMethod = getValue("acct_method");
            hPcodInterestMethod = getValue("interest_method");
            hPcodUrge1St = getValue("urge_1st");
            hPcodUrge2St = getValue("urge_2st");
            hPcodUrge3St = getValue("urge_3st");
            hPcodOccupy = getValue("occupy");
            hPcodReceivables = getValue("receivables");
            hPcodQueryType = getValue("query_type");
        }

        hCurpQueryType = hPcodQueryType;
        hCurpAcctChiShortName = hPcodChiShortName;
        hCurpAcctEngShortName = hPcodEngShortName;
        hCurpItemClassNormal = hPcodItemClassNormal;
        hCurpItemClassBackDate = hPcodItemClassBackDate;
        hCurpItemClassRefund = hPcodItemClassRefund;
        hCurpItemOrderNormal = hPcodItemOrderNormal;
        hCurpItemOrderBackDate = hPcodItemOrderBackDate;
        hCurpItemOrderRefund = hPcodItemOrderRefund;
        hCurpAcctMethod = hPcodAcctMethod;
    }

    /***********************************************************************/
    void insertBilCurpost() throws Exception {
        // h_curp_valid_flag = h_biun_auth_flag;
        if (hCurpBillType.equals("BTAO")) {
            hCurpMchtNo = "0800056868";
        }

        hCurpContractFlag = "P";

        hCurpDestAmtChar = String.format("%10.0f", hCurpDestAmt);
        hCurpSourceAmt = hOtheDcDestAmt;
        hCurpSourceAmtChar = String.format("%10.0f", hCurpSourceAmt);
        hCurpSourceCurr = hOtheCurrCode;

if(totCnt == 2478)
   showLogMessage("I", "", "h_curp_dest_amt = " + hCurpDestAmt + ", h_curp_source_amt = "
                    + hCurpSourceAmt + ", h_othe_dc_dest_amt = " + hOtheDcDestAmt);
        setValue("reference_no", hCurpReferenceNo);
        setValue("bill_type", hCurpBillType);
        setValue("txn_code", hCurpTxnCode);
        setValue("card_no", hCurpCardNo);
        setValue("film_no", hCurpFilmNo);
        setValue("purchase_date", hCurpPurchaseDate);
        setValueDouble("dest_amt", hCurpDestAmt);
        setValue("dest_curr", hCurpDestCurr);
        setValueDouble("source_amt", hCurpSourceAmt);
        setValue("source_curr", hCurpSourceCurr);
        setValue("mcht_eng_name", hCurpMchtEngName);
        setValue("mcht_city", hCurpMchtCity);
        setValue("mcht_country", hCurpMchtCountry);
        setValue("mcht_category", hCurpMchtCategory);
        setValue("mcht_zip", hCurpMchtZip);
        setValue("mcht_state", hCurpMchtState);
        setValue("auth_code", hCurpAuthorization);
        setValue("pos_entry_mode", hCurpPosEntryMode);
        setValue("process_date", hCurpProcessDate);
        setValue("mcht_no", hCurpMchtNo);
        setValue("mcht_chi_name", hCurpMchtChiName);
        setValue("acquire_date", hCurpAcquireDate);
        setValue("contract_no", hCurpContractNo);
        setValueInt("term", 0);
        setValueInt("total_term", 0);
        setValue("batch_no", hCurpBatchNo);
        setValue("sign_flag", hCurpSignFlag);
        setValue("acct_code", hCurpAcctCode);
        setValue("acct_item", hCurpAcctItem);
        setValue("acct_eng_short_name", hCurpAcctEngShortName);
        setValue("acct_chi_short_name", hCurpAcctChiShortName);
        setValue("item_order_normal", hCurpItemOrderNormal);
        setValue("item_order_back_date", hCurpItemOrderBackDate);
        setValue("item_order_refund", hCurpItemOrderRefund);
        setValue("acexter_desc", hCurpAcexterDesc);
        setValue("entry_acct", hCurpEntryAcct);
        setValue("item_class_normal", hCurpItemClassNormal);
        setValue("item_class_back_date", hCurpItemClassBackDate);
        setValue("item_class_refund", hCurpItemClassRefund);
        setValue("collection_mode", hCurpCollectionMode);
        setValue("fees_state", hCurpFeesState);
        setValue("cash_adv_state", hCurpCashAdvState);
        setValue("this_close_date", hCurpThisCloseDate);
        setValue("manual_upd_flag", hCurpManualUpdFlag);
        // setValue ("valid_flag" , h_curp_valid_flag);
        setValue("doubt_type", hCurpDoubtType);
        setValue("duplicated_flag", hCurpDuplicatedFlag);
        setValue("rsk_type", hCurpRskType);
        setValue("acct_type", hCurpAcctType);
        setValue("stmt_cycle", hCurpStmtCycle);
        setValue("major_card_no", hCurpMajorCardNo);
        setValue("promote_dept", hCurpPromoteDept);
        setValue("issue_date", hCurpIssueDate);
        setValue("prod_no", hCurpProdNo);
        setValue("group_code", hCurpGroupCode);
        setValue("bin_type", hCurpBinType);
        setValue("acno_p_seqno" , hCurpPSeqno);
        setValue("p_seqno"   , hCurpGpNo);
        setValue("id_p_seqno", hCurpIdPSeqno);
        setValue("major_id_p_seqnon", hCurpMajorIdPSeqno);
        // setValue ("reference_no_original", "");
        // setValue ("fees_reference_no" , "");
        setValue("tx_convt_flag", hCurpTxConvtFlag);
        setValue("acctitem_convt_flag", hCurpAcctitemConvtFlag);
        setValue("format_chk_ok_flag", hCurpFormatChkOkFlag);
        setValue("double_chk_ok_flag", hCurpDoubleChkOkFlag);
        setValue("err_chk_ok_flag", hCurpErrChkOkFlag);
        setValue("source_code", hCurpSourceCode);
        setValue("curr_code", hOtheCurrCode);
        setValueDouble("dc_amount", hOtheDcDestAmt);
        setValue("curr_post_flag", hCurpCurrPostFlag);
        setValue("mod_user", hCurpModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        setValueLong("mod_seqno", hCurpModSeqno);
        daoTable = "bil_curpost";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_curpost duplicate", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void chkBillunit() throws Exception {
        selectPtrBillunit();

        hPostBatchDate = hBusinessDate;
        hPostBatchUnit = sBillUnit;
        tempX10 = String.format("%8s%2.2s", hPostBatchDate, hPostBatchUnit);
        tempX14 = String.format("%10s%4s", tempX10, hBatchSeq);
        hPostBatchNo = tempX14;

        hCurpBatchNo = tempX14;

        printFirstRtn();

    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {
        showLogMessage("I", "", "888 select TYPE =[" + hFixBillType + "]");
        sBillUnit = hFixBillType.substring(0, 2);
        hBiunConfFlag = "";
        // h_biun_auth_flag = "";
        sqlCmd = "select conf_flag ";
        // sqlCmd += "auth_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, hFixBillType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
            // h_biun_auth_flag = getValue("auth_flag");
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
        //String h_t_add_item = "";
        //String h_t_bill_type = "";
        hTAddItem = "";
        hTBillType = "";
        
        fileCount++;

        if (fileCount > 1) {
            buf = "";
            buf = comcr.insertStr(buf, "總  計:", 12);
            szTmp = comcr.commFormat("3z,3z", totalTransactionCnt);
            buf = comcr.insertStr(buf, szTmp, 29);
            szTmp = comcr.commFormat("3$,3$,3$", totalTransactionAmt);
            buf = comcr.insertStr(buf, szTmp, 39);
            lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", buf));
            String filename = String.format("%s/reports/%s_%s%1d", comc.getECSHOME(), rptId, hSystemDateF,
                    fileCount);
            filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
            comcr.insertPtrBatchRpt(lpar111);
            comc.writeReportForTest(filename, lpar111);
            rptSeq = 0;
            totalTransactionCnt = 0;
            totalTransactionAmt = 0;
        }

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: " + rptId, 1);

        szTmp = String.format("%22s", "加檔統計表");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        buf = comcr.insertStr(buf, chinDate, 70);
        lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "幣    別:", 1);
        buf = comcr.insertStr(buf, hTempCurrCode, 10);
        lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "批    號:", 1);
        buf = comcr.insertStr(buf, hCurpBatchNo, 10);
        buf = comcr.insertStr(buf, "加檔日期:", 60);
        szTmp = String.format("%8d", comcr.str2long(hBusinessDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 68);

        lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "筆  數 金         額 ", 30);
        lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "====== ============= ", 30);
        lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        sqlCmd = "select ";
        sqlCmd += "count(*) p_count,";
        sqlCmd += "sum(a.dest_amt) p_sum,";
      //sqlCmd += "a.bill_type,";
      //sqlCmd += "a.add_item ";
        sqlCmd += "a.bill_type h_t_bill_type,";
        sqlCmd += "a.add_item h_t_add_item ";
        sqlCmd += "from bil_othexp a, ptr_acct_type b ";
        sqlCmd += "where decode(a.post_flag,'','N',a.post_flag) != 'Y' ";
        sqlCmd += "and a.apr_user != '' ";
        sqlCmd += "and a.acct_type = b.acct_type ";
        sqlCmd += "and a.bill_type = ? ";
        sqlCmd += "and b.curr_code = ? ";
        sqlCmd += "group by a.bill_type,a.add_item ";
      //sqlCmd += "group by h_t_bill_type,h_t_add_item ";
        setString(1, tempBillType);
        setString(2, hTempCurrCode);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            pCount = getValueInt("p_count", i);
            pSum = getValueDouble("p_sum", i);
          //h_t_bill_type = getValue("a.bill_type", i);
          //h_t_add_item = getValue("a.add_item", i);
            hTBillType = getValue("h_t_bill_type", i);
            hTAddItem = getValue("h_t_add_item", i);

            String hTempX16 = "";
            if (hTBillType.equals("BTAO")) {
                sqlCmd = "select exter_desc, ";
                sqlCmd += "acct_code ";
                sqlCmd += " from ptr_billtype  ";
                sqlCmd += "where bill_type = ?  ";
                sqlCmd += "fetch first 1 rows only ";
                setString(1, hTBillType);
                int recCnt = selectTable();
                if (recCnt > 0) {
                    hTempX16 = getValue("exter_desc");
                    hBityAcctCode = getValue("acct_code");
                }
            } else {
                sqlCmd = "select exter_desc, ";
                sqlCmd += "acct_code ";
                sqlCmd += " from ptr_billtype  ";
                sqlCmd += "where bill_type = ?  ";
                sqlCmd += "and txn_code = ? ";
                setString(1, hTBillType);
                setString(2, hTAddItem);
                int recCnt = selectTable();
                if (recCnt > 0) {
                    hTempX16 = getValue("exter_desc");
                    hBityAcctCode = getValue("acct_code");
                }
            }

            hThisBusiAddAmt = pSum;
            if ( (Arrays.asList("AF","CF","PF","AI").contains(hBityAcctCode))   &&
                 (!Arrays.asList("06","25","26","27","29").contains(hTAddItem)) &&
                 (hThisBusiAddAmt != 0) )
               insertThisActPostLog();

            buf = "";
            buf = comcr.insertStr(buf, hTempX16, 12);
            szTmp = comcr.commFormat("3z,3z", pCount);
            totalTransactionCnt = totalTransactionCnt + pCount;
            buf = comcr.insertStr(buf, szTmp, 29);
            szTmp = comcr.commFormat("3$,3$,3$", pSum);
            buf = comcr.insertStr(buf, szTmp, 39);
            totalTransactionAmt = totalTransactionAmt + pSum;
            lpar111.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", buf));
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
        setString(1, hBusinessDate);
        setString(2, hTempCurrCode);
        setString(3, hBityAcctCode);
        setString(4, javaProgram);
        int m = selectTable();
        hSrcPgmPostseq = getValueLong("h_src_pgm_postseq");

        daoTable    = "act_post_log";
        extendField = "post.";
        setValue("post.BUSINESS_DATE", hBusinessDate);
        setValue("post.CURR_CODE", hTempCurrCode);
        setValue("post.ACCT_CODE", hBityAcctCode);
        setValue("post.SRC_PGM",javaProgram);
        setValueLong("post.SRC_PGM_POSTSEQ", hSrcPgmPostseq);
        setValue("post.POST_TYPE","A1");
        hThisBusiAddAmt = convAmt(hThisBusiAddAmt);
        setValueDouble("post.POST_TYPE_AMT", hThisBusiAddAmt);
        setValue("post.POST_NOTE", hPostNote);
        setValue("post.BILL_TYPE", hTBillType);
        setValue("post.TXN_CODE", hTAddItem);
        setValue("post.ACCT_TYPE", hTempAcctType);
        setValue("post.MOD_TIME",sysDate + sysTime);	
        setValue("post.MOD_PGM",javaProgram);
        insertTable();
        if (dupRecord.equals("Y")) 
           { 
            comcr.errRtn("insert_this act_post_log ERROR ", hBusinessDate +" "+
                    hTempCurrCode +" "+ hBityAcctCode +" "+javaProgram+ hSrcPgmPostseq, hCallBatchSeqno);
           } 

    }
    
    /***********************************************************************/
    public double  convAmt(double cvtAmt) throws Exception
    {
      long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.000001);
      double cvtDouble =  ((double) cvtLong) / 100;
      return cvtDouble;
    }

    void initBilOthexp() {
        hOtheBillType = "";
        hOtheTxCode = "";
        hOtheCardNo = "";
        hOtheSeqNo = 0;
        hOtheDestAmt = 0;
        hOtheDestCurr = "";
        hOthePurchaseDate = "";
        hOtheChiDesc = "";
        hOtheBillDesc = "";
        hOtheDeptFlag = "";
        hOthePostFlag = "";
        hOtheCurrCode = "";
        hOtheDcDestAmt = 0;
    }

    void initBilCurpost() {
        hCurpSourceCode = "";
        hCurpQueryType = "";
        hCurpReferenceNo = "";
        hCurpBillType = "";
        hCurpTxnCode = "";
        hCurpCardNo = "";
        hCurpFilmNo = "";
        hCurpAcquirerMemberId = "";
        hCurpPurchaseDate = "";
        hCurpPurchaseTime = "";
        hCurpDestAmtChar = "";
        hCurpDestAmt = 0;
        hCurpDestCurr = "";
        hCurpSourceAmtChar = "";
        hCurpSourceAmt = 0;
        hCurpSourceCurr = "";
        hCurpMchtEngName = "";
        hCurpMchtCity = "";
        hCurpMchtCountry = "";
        hCurpMchtCategory = "";
        hCurpMchtZip = "";
        hCurpMchtState = "";
        hCurpTmpRequestFlag = "";
        hCurpUsageCode = "";
        hCurpReasonCode = "";
        hCurpTmpServiceCode = "";
        hCurpAuthorization = "";
        hCurpPosTermCapability = "";
        hCurpPosEntryMode = "";
        hCurpProcessDate = "";
        hCurpReimbursementAttr = "";
        hCurpEcInd = "";
        hCurpFirstConversionDate = "";
        hCurpSecondConversionDate = "";
        hCurpMchtNo = "";
        hCurpMchtChiName = "";
        hCurpElectronicTermInd = "";
        hCurpTransactionSource = "";
        hCurpAcquireDate = "";
        hCurpContractNo = "";
        hCurpGoodsName = "";
        hCurpOriginalNo = "";
        hCurpTelephoneNo = "";
        hCurpTerm = "";
        hCurpTotalTerm = "";
        hCurpProdName = "";
        hCurpBatchNo = "";
        hCurpExchangeRate = "";
        hCurpExchangeDate = "";
        hCurpSignFlag = "";
        hCurpAcctCode = "";
        hCurpAcctItem = "";
        hCurpAcctEngShortName = "";
        hCurpAcctChiShortName = "";
        hCurpAcexterDesc = "";
        hCurpEntryAcct = "";
        hCurpItemOrderNormal = "";
        hCurpItemOrderBackDate = "";
        hCurpItemOrderRefund = "";
        hCurpItemClassNormal = "";
        hCurpItemClassBackDate = "";
        hCurpItemClassRefund = "";
        hCurpAcctMethod = "";
        hCurpInterestMode = "";
        hCurpAdvWkday = "";
        hCurpCollectionMode = "";
        hCurpFeesState = "";
        hCurpFeesFixAmt = 0;
        hCurpFeesPercent = 0;
        hCurpFeesMin = 0;
        hCurpFeesMax = 0;
        hCurpFeesBillType = "";
        hCurpFeesTxnCode = "";
        hCurpBalanceState = "";
        hCurpCashAdvState = "";
        hCurpMerchFee = 0;
        hCurpThisCloseDate = "";
        hCurpManualUpdFlag = "";
        // h_curp_valid_flag = "";
        hCurpDoubtType = "";
        hCurpDuplicatedFlag = "";
        hCurpRskType = "";
        hCurpAcctType = "";
        hCurpAcctKey = "";
        hCurpAcctStatus = "";
        hCurpStmtCycle = "";
        hCurpPayByStageFlag = "";
        hCurpAutopayAcctNo = "";
        hCurpMajorCardNo = "";
        hCurpCurrentCode = "";
        hCurpOppostDate = "";
        hCurpIssueDate = "";
        hCurpPromoteDept = "";
        hCurpProdNo = "";
        hCurpGroupCode = "";
        hCurpBinType = "";
        hCurpPSeqno = "";
        hCurpGpNo = "";
        hCurpIdPSeqno = "";
        hCurpMajorIdPSeqno = "";
        hCurpReferenceNoOriginal = "";
        hCurpFeesReferenceNo = "";
        hCurpTxConvtFlag = "";
        hCurpAcctitemConvtFlag = "";
        hCurpFormatChkOkFlag = "";
        hCurpDoubleChkOkFlag = "";
        hCurpErrChkOkFlag = "";
        hCurpCurrPostFlag = "";
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilF002 proc = new BilF002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
