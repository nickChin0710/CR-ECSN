/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/27  V1.00.01    shiyuqi       updated for project coding standard   * 
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

/*IPSS 交易別/會計科目轉換作業*/
public class BilE021 extends AccessDAO {
    private String progname = "IPSS 交易別/會計科目轉換作業 109/11/27  V1.00.01";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hTempUser = "";
    int debug = 1;
    int totalCnt = 0;

    String prgmId = "BilE021";
    String prgmName = "IPSS 交易別/會計科目轉換作業";
    String rptName = "BIL_E021";
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
    String iFileName = "";
    String iPostDate = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    long hCurpModSeqno = 0;
    String hCallRProgramCode = "";
    String hBusinessDate = "";
    String hSystemDate = "";
    String hCurrencyCode = "";
    String hPbtbBinType = "";
    String hCurpCardNo = "";
    String hIcgeIpsCardNo = "";
    String hIcgeTxnType = "";
    String hIcgeTxnDate = "";
    String hIcgeTxnTime = "";
    String hIcgeTxnDateR = "";
    String hIcgeTxnTimeR = "";
    String hIcgeTrafficCd = "";
    String hIcgeTrafficCdSub = "";
    String hIcgeTrafficEqup = "";
    String hIcgeTrafficAbbr = "";
    String hIcgeAddrCd = "";
    double hIcgeTxnAmt = 0;
    String hIcgeTxnBal = "";
    String hIcgeBatchNo = "";
    int hIcgeSeqNo = 0;
    String hIcgeCardNo = "";
    String hIcgeBillType = "";
    String hIcgeTxnCode = "";
    String hIcgeMchtNo = "";
    String hIcgeMchtCategory = "";
    String hIcgeMchtChiName = "";
    String hIcgeBillDesc = "";
    String hIcgePostFlag = "";
    String hIcgeFileName = "";
    String hIcgeRowid = "";
    String hTempX08 = "";
    String hCurpReferenceNo = "";
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
    String hCurpBillType = "";
    String hCurpTxnCode = "";
    String hCurpTransactionSeqNo = "";
    String hCurpCardNoExt = "";
    String hCurpBinType = "";
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
    String hCurpSettlementFlag = "";
    String hCurpSettlementAmtChar = "";
    double hCurpSettlementAmt = 0;
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
    String hCurpFeesPercent = "";
    String hCurpFeesMin = "";
    String hCurpFeesMax = "";
    String hCurpFeesBillType = "";
    String hCurpFeesTxnCode = "";
    String hCurpBalanceState = "";
    String hCurpCashAdvState = "";
    String hCurpMerchFee = "";
    String hCurpThisCloseDate = "";
    String hCurpManualUpdFlag = "";
    String hCurpValidFlag = "";
    String hCurpDoubtType = "";
    String hCurpDuplicatedFlag = "";
    String hCurpRskType = "";
    String hCurpAcctType = "";
    String hCurpAcctKey = "";
    String hCurpAcctStatus = "";
    String hCurpStmtCycle = "";
    String hCurpBlockStatus = "";
    String hCurpBlockDate = "";
    String hCurpPayByStageFlag = "";
    String hCurpAutopayAcctNo = "";
    String hCurpMajorCardNo = "";
    String hCurpCurrentCode = "";
    String hCurpOppostDate = "";
    String hCurpPromoteDept = "";
    String hCurpIssueDate = "";
    String hCurpProdNo = "";
    String hCurpGroupCode = "";
    String hCurpCardSw = "";
    String hCurpPSeqno = "";
    String hCurpAcnoPSeqno = "";
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
    String hCardMajorCardNo = "";
    String hCardCurrentCode = "";
    String hCardIssueDate = "";
    String hCardOppostDate = "";
    String hCardPromoteDept = "";
    String hCardProdNo = "";
    String hCardGroupCode = "";
    String hCardSourceCode = "";
    String hCardCardType = "";
    String hCardBinType = "";
    String hCardBinNo = "";
    String hCardPSeqno = "";
    String hCardAcnoPSeqno = "";
    String hCardBlockStatus = "";
    String hCardBlockDate = "";
    String hCardIdPSeqno = "";
    String hCardMajorIdPSeqno = "";
    String hCardNewEndDate = "";
    String hAcnoAcctType = "";
    String hAcnoAcctKey = "";
    String hAcnoAcctStatus = "";
    String hAcnoStmtCycle = "";
    String hAcnoBlockStatus = "";
    String hAcnoBlockDate = "";
    String hAcnoPayByStageFlag = "";
    String hAcnoAutopayAcctNo = "";
    String hSystemDateF = "";
    int pCount = 0;
    double pSum = 0;
    String pBatchNo = "";
    String hTThisCloseDate = "";
    String hPrintName = "";
    String hRptName = "";
    int tempInt = 0;
    String hIardCardNo = "";
    String hIardCurrentCode = "";
    String hIardNewBegDate = "";
    String hIardNewEndDate = "";
    String hIardAutoloadFlag = "";
    String hIardBalanceDate = "";
    String hIardReturnDate = "";
    String hIardOppostDate = "";
    String hIardLockDate = "";
    String hIardBlackltFlag = "";
    String hBiunAuthFlag = "";
    int totCnt = 0;

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
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : BilE021 batch_seq", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (hCallBatchSeqno.equals(comc.getECSHOME())) {
                hCallBatchSeqno = "no-call";
            }

            hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 0);
                sqlCmd = "select user_id ";
                sqlCmd += " from ptr_callbatch  ";
                sqlCmd += "where batch_seqno = ?  ";
                setString(1, hCallBatchSeqno);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hTempUser = getValue("user_id");
                }
            }
            if (hTempUser.length() == 0) {
                hModUser = comc.commGetUserID();
                hTempUser = hModUser;
            }

            commonRtn();
            hModPgm = javaProgram;
            hCurpModPgm = hModPgm;
            hCurpModTime = hModTime;
            hCurpModUser = hModUser;
            hCurpModSeqno = hModSeqno;

            //printFirstRtn();

            selectIpsCgecAll();
            showLogMessage("I", "", String.format("程式執行結束,總筆數=[%d]\n", totCnt));
            // ==============================================
            // 固定要做的
            if (hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1);
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
    void commonRtn() throws Exception {
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
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

    /**********************************************************************/
    void printFirstRtn() throws Exception {
        int pCount = 0;
        double pSum = 0;
        String hTThisCloseDate = "";
        String pBatchNo = "";

        hSystemDateF = "";
        sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDateF = getValue("h_system_date_f");
        }

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: " + rptName, 1);

        szTmp = String.format("%22s", "IPSS 請款媒體入檔合計表");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        buf = comcr.insertStr(buf, chinDate, 70);
        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "筆  數 金         額 批          號 過帳日期", 20);
        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "====== ============= ============== ========", 20);
        lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        sqlCmd = "select ";
        sqlCmd += "count(*) p_count,";
        sqlCmd += "sum(txn_amt) p_sum,";
        sqlCmd += "batch_no ";
        sqlCmd += "from ips_cgec_all ";
        sqlCmd += "group by batch_no ";
        recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            pCount = getValueInt("p_count", i);
            pSum = getValueDouble("p_sum", i);
            pBatchNo = getValue("batch_no", i);
            hTThisCloseDate = hBusinessDate;

            buf = "";
            szTmp = comcr.commFormat("3z,3z", pCount);
            buf = comcr.insertStr(buf, szTmp, 19);
            szTmp = comcr.commFormat("3$,3$,3$", pSum);
            buf = comcr.insertStr(buf, szTmp, 29);
            buf = comcr.insertStr(buf, pBatchNo, 41);
            szTmp = String.format("%8d", comcr.str2long(hTThisCloseDate));
            buf = comcr.insertStr(buf, szTmp, 56);
            lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "1", buf));
        }
        String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptName, hSystemDateF);
        filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
        comc.writeReportForTest(filename, lpar111);
    }

    /***********************************************************************/
    void selectIpsCgecAll() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "ips_card_no,";
        sqlCmd += "txn_type,";
        sqlCmd += "txn_date,";
        sqlCmd += "txn_time,";
        sqlCmd += "txn_date_r,";
        sqlCmd += "txn_time_r,";
        sqlCmd += "traffic_cd,";
        sqlCmd += "traffic_cd_sub,";
        sqlCmd += "traffic_equp,";
        sqlCmd += "traffic_abbr,";
        sqlCmd += "addr_cd,";
        sqlCmd += "txn_amt,";
        sqlCmd += "txn_bal,";
        sqlCmd += "batch_no,";
        sqlCmd += "seq_no,";
        sqlCmd += "card_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "mcht_no,";
        sqlCmd += "mcht_category,";
        sqlCmd += "mcht_chi_name,";
        sqlCmd += "bill_desc,";
        sqlCmd += "post_flag,";
        sqlCmd += "file_name,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += "from ips_cgec_all ";
        sqlCmd += "where post_flag = 'N' ";
        int recordCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "888 select 1 cnt=[" + recordCnt + "]");
        for (int i = 0; i < recordCnt; i++) {
            hIcgeIpsCardNo = getValue("ips_card_no", i);
            hIcgeTxnType = getValue("txn_type", i);
            hIcgeTxnDate = getValue("txn_date", i);
            hIcgeTxnTime = getValue("txn_time", i);
            hIcgeTxnDateR = getValue("txn_date_r", i);
            hIcgeTxnTimeR = getValue("txn_time_r", i);
            hIcgeTrafficCd = getValue("traffic_cd", i);
            hIcgeTrafficCdSub = getValue("traffic_cd_sub", i);
            hIcgeTrafficEqup = getValue("traffic_equp", i);
            hIcgeTrafficAbbr = getValue("traffic_abbr", i);
            hIcgeAddrCd = getValue("addr_cd", i);
            hIcgeTxnAmt = getValueDouble("txn_amt", i);
            hIcgeTxnBal = getValue("txn_bal", i);
            hIcgeBatchNo = getValue("batch_no", i);
            hIcgeSeqNo = getValueInt("seq_no", i);
            hIcgeCardNo = getValue("card_no", i);
            hIcgeBillType = getValue("bill_type", i);
            hIcgeTxnCode = getValue("txn_code", i);
            hIcgeMchtNo = getValue("mcht_no", i);
            hIcgeMchtCategory = getValue("mcht_category", i);
            hIcgeMchtChiName = getValue("mcht_chi_name", i);
            hIcgeBillDesc = getValue("bill_desc", i);
            hIcgePostFlag = getValue("post_flag", i);
            hIcgeFileName = getValue("file_name", i);
            hIcgeRowid = getValue("rowid", i);

            totCnt++;

            //金額為0時不新增到bil_curpost
            if (hIcgeTxnAmt==0) {
            	updateIpsCgecAll();
            	continue;
            }
            hCurpBatchNo = hIcgeBatchNo;
            hCurpCardNo = hIcgeCardNo;
            hCurpBillType = hIcgeBillType;
            hCurpTxnCode = hIcgeTxnCode;
            hCurpPurchaseDate = hIcgeTxnDate;
            hCurpPurchaseTime = hIcgeTxnTime;
            hCurpFilmNo = String.format("%-8.8s%-8.8s%-6.6s", hIcgeIpsCardNo, hIcgeTxnDate, hIcgeTxnTime);
            hCurpFilmNo = String.format("%-16.16s%03d", hIcgeIpsCardNo, hIcgeSeqNo);
            hCurpMchtNo = hIcgeMchtNo;
            hCurpMchtCategory = hIcgeMchtCategory;
            hCurpMchtChiName = hIcgeMchtChiName;
            hCurpMchtCountry = "TW";
            hCurpMchtCity = "taipei";
            hCurpAcexterDesc = hIcgeBillDesc;

            hCurpDestAmt = hIcgeTxnAmt;
            if (hIcgeTxnType.length() > 0 && hIcgeTxnAmt < 0 && hIcgeTxnCode.equals("05")) {
                hCurpDestAmt = hIcgeTxnAmt * -1;
            }

            hCurpDestAmtChar = String.format("%9.0f", hCurpDestAmt);

            hCurpAcquireDate = hBusinessDate;
            hCurpThisCloseDate = hBusinessDate;
            hCurpProcessDate = hBusinessDate;

            chkCrdCard();
            if (selectPtrBintable() == false)
                break;

            hCurpDestCurr = hCurrencyCode;
            hCurpSourceAmt = hCurpDestAmt;
            hCurpSourceCurr = hCurpDestCurr;
            hCurpSourceAmtChar = hCurpDestAmtChar;

            selectActAcno();
            chkPtrBilltype();
            chkPtrActcode();

            hCurpTxConvtFlag = "Y";
            hCurpAcctitemConvtFlag = "Y";

            hTempX08 = "";
            sqlCmd = "select substr(to_char(bil_postseq.nextval,'0000000000'),4,8) h_temp_x08 ";
            sqlCmd += " from dual ";
            int recordCnt1 = selectTable();
            if (recordCnt1 > 0) {
                hTempX08 = getValue("h_temp_x08");
            }
            hCurpReferenceNo = String.format("%2.2s%s", hBusinessDate.substring(2)
                                                         , hTempX08);

            hCurpAcexterDesc = hIcgeBillDesc;


            insertBilCurpost();

            updateIpsCgecAll();
        }
    }
        
    /***********************************************************************/
    void updateIpsCgecAll() throws Exception {
    	 
         daoTable   = "ips_cgec_all";
         updateSQL  = " reference_no = ?,  ";
         updateSQL += " post_flag    = 'Y' ";
         whereStr   = "where rowid   = ? ";
         setString(1, hCurpReferenceNo);
         setRowId(2, hIcgeRowid);
         updateTable();
         if (notFound.equals("Y")) {
             stderr = "update_ips_cgec_all not found!";
             comcr.errRtn(stderr, "", hCallBatchSeqno);
         }
     }
    
    /***********************************************************************/
    boolean selectPtrBintable() throws Exception {
        hCurrencyCode = "";
        hPbtbBinType = "";
        sqlCmd  = "select bin_type ";
        sqlCmd += "  from ptr_bintable  ";
        sqlCmd += " where 1=1 ";
        sqlCmd += "   and bin_no || bin_no_2_fm || '0000' <= ?  ";
        sqlCmd += "   and bin_no || bin_no_2_to || '9999' >= ?  ";
        setString(1, hCurpCardNo);
        setString(2, hCurpCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCurrencyCode = "901";
            // h_pbtb_bin_type = getValue("bin_type");
        } else {
            return false;
        }
        return true;
    }

    /**********************************************************************/
    void initCrdCard() {
        hCardMajorCardNo = "";
        hCardBlockStatus = "";
        hCardBlockDate = "";
        hCardAcnoPSeqno = "";
        hCardPSeqno = "";
        hCardIdPSeqno = "";
        hCardMajorIdPSeqno = "";
        hCardCardType = "";
        hCardBinType = "";
        hCardBinNo = "";
        hCardGroupCode = "";
        hCardSourceCode = "";
        hCardCurrentCode = "";
        hCardNewEndDate = "";
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
        sqlCmd += "bin_no,   ";
        sqlCmd += "bin_type, ";
        sqlCmd += "p_seqno,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "major_id_p_seqno,";
        // sqlCmd += "block_status,";
        sqlCmd += "block_date,";
        sqlCmd += "id_p_seqno ";
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
            hCardPSeqno = getValue("p_seqno");
            hCardAcnoPSeqno = getValue("acno_p_seqno");
            // h_card_block_status = getValue("block_status");
            hCardBlockDate = getValue("block_date");
            hCardIdPSeqno = getValue("id_p_seqno");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
        }

        hCurpBinType = hCardBinType;
        hCurpCardSw = hCardBinType;
        hCurpMajorCardNo = hCardMajorCardNo;
        hCurpCurrentCode = hCardCurrentCode;
        hCurpOppostDate = hCardOppostDate;
        hCurpIssueDate = hCardIssueDate;
        hCurpPromoteDept = hCardPromoteDept;
        hCurpProdNo = hCardProdNo;
        hCurpGroupCode = hCardGroupCode;
        hCurpSourceCode = hCardSourceCode;
        hCurpPSeqno = hCardPSeqno;
        hCurpIdPSeqno = hCardIdPSeqno;
        hCurpAcnoPSeqno = hCardAcnoPSeqno;
        hCurpMajorIdPSeqno = hCardMajorIdPSeqno;

    }

    /**********************************************************************/
    void selectActAcno() throws Exception {
        hAcnoAcctType = "";
        hAcnoAcctKey = "";
        hAcnoAcctStatus = "";
        hAcnoStmtCycle = "";
        hAcnoBlockStatus = "";
        hAcnoBlockDate = "";
        hAcnoPayByStageFlag = "";
        hAcnoAutopayAcctNo = "";

        sqlCmd = "select acct_type,";
        sqlCmd += "acct_key,";
        sqlCmd += "acct_status,";
        sqlCmd += "stmt_cycle,";
        // sqlCmd += "block_status,";
        // sqlCmd += "block_date,";
        sqlCmd += "pay_by_stage_flag,";
        sqlCmd += "autopay_acct_no ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno  = ? ";
        setString(1, hCurpAcnoPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            // h_acno_block_status = getValue("block_status");
            // h_acno_block_date = getValue("block_date");
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
        sqlCmd += "  and txn_code  = ? ";
        setString(1, hIcgeBillType);
        setString(2, hIcgeTxnCode);
        int recordCnt = selectTable();
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
            // h_bity_collection_mode = getValue("collection_mode");
            hBityCashAdvState = getValue("cash_adv_state");
            hBityEntryAcct = getValue("entry_acct");
            hBityChkErrBill = getValue("chk_err_bill");
            hBityDoubleChk = getValue("double_chk");
            hBityFormatChk = getValue("format_chk");
            hBityMerchFee = getValue("merch_fee");
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
        hCurpAcexterDesc = hBityExterDesc;
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
        } else {
            stderr = String.format(" acct_code =[%s][%s][%s]\n", hCurpAcctCode
                                   , hIcgeBillType, hIcgeTxnCode);
            showLogMessage("I", "", stderr);
            stderr = String.format("select_prt_actcode not found");
            comcr.errRtn(stderr, hCurpAcctCode, hCallBatchSeqno);
        }

        hCurpAcctChiShortName = hPcodChiShortName;
        hCurpAcctEngShortName = hPcodEngShortName;
        hCurpItemClassNormal = hPcodItemClassNormal;
        hCurpItemClassBackDate = hPcodItemClassBackDate;
        hCurpItemClassRefund = hPcodItemClassRefund;
        hCurpItemOrderNormal = hPcodItemOrderNormal;
        hCurpItemOrderBackDate = hPcodItemOrderBackDate;
        hCurpItemOrderRefund = hPcodItemOrderRefund;
        hCurpAcctMethod = hPcodAcctMethod;
        hCurpQueryType = hPcodQueryType;
    }

    /***********************************************************************/
    void insertBilCurpost() throws Exception {
        hCurpValidFlag = hBiunAuthFlag;

        //if (hIcgeMchtNo.equals("EASY8003")) {
        //    chkRskRtn();
        //}

        setValue("reference_no", hCurpReferenceNo);
        setValue("bill_type", hCurpBillType);
        setValue("txn_code", hCurpTxnCode);
        setValue("card_no", hCurpCardNo);
        setValue("bin_type", hCurpBinType);
        setValue("film_no", hCurpFilmNo);
        setValue("purchase_date", hCurpPurchaseDate);
        setValueDouble("dest_amt", hCurpDestAmt);
        setValue("dest_curr", hCurpDestCurr);
        setValueDouble("source_amt", hCurpSourceAmt);
        setValue("source_curr", hCurpSourceCurr);
        setValue("curr_code","901");
        setValueDouble("dc_amount", hCurpDestAmt);
        setValueDouble("cash_pay_amt", hCurpDestAmt);
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
        setValueInt("term", comcr.str2int(hCurpTerm));
        setValueInt("total_term", comcr.str2int(hCurpTotalTerm));
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
        setValue("valid_flag", hCurpValidFlag);
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
        setValue("p_seqno", hCurpPSeqno);
        setValue("acno_p_seqno", hCurpAcnoPSeqno);
        setValue("id_p_seqno", hCurpIdPSeqno);
        setValue("major_id_p_seqno", hCurpMajorIdPSeqno);
        setValue("tx_convt_flag", hCurpTxConvtFlag);
        setValue("acctitem_convt_flag", hCurpAcctitemConvtFlag);
        setValue("format_chk_ok_flag", hCurpFormatChkOkFlag);
        setValue("double_chk_ok_flag", hCurpDoubleChkOkFlag);
        setValue("err_chk_ok_flag", hCurpErrChkOkFlag);
        setValue("source_code", hCurpSourceCode);
        setValue("curr_post_flag", hCurpCurrPostFlag);
        setValue("mod_user", hCurpModUser);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        setValueDouble("mod_seqno", hCurpModSeqno);
        
        setValue("ecs_platform_kind", "EP");
        setValue("ecs_cus_mcht_no", "006EP00001");
        daoTable = "bil_curpost";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_curpost duplicate", "", hCallBatchSeqno);
        }

        // **insert bil_nccc300_dtl
        setValue("reference_no", hCurpReferenceNo);
        setValue("card_no", hCurpCardNo);
        setValue("tmp_request_flag", hCurpTmpRequestFlag);
        setValue("usage_code", hCurpUsageCode);
        setValue("reason_code", hCurpReasonCode);
        setValue("tmp_service_code", hCurpTmpServiceCode);
        setValue("pos_term_capability", hCurpPosTermCapability);
        setValue("pos_entry_mode", hCurpPosEntryMode);
        setValue("reimbursement_attr", hCurpReimbursementAttr);
        setValue("ec_ind", hCurpEcInd);
        setValue("second_conversion_date", hCurpSecondConversionDate);
        setValue("electronic_term_ind", hCurpElectronicTermInd);
        setValue("transaction_source", hCurpTransactionSource);
        setValue("original_no", hCurpOriginalNo);
        setValue("batch_no", hCurpBatchNo);
        setValue("exchange_rate", hCurpExchangeRate);
        setValue("exchange_date", hCurpExchangeDate);
        setValue("query_type", hCurpQueryType);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", prgmId);
        daoTable = "bil_nccc300_dtl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_nccc300_dtl duplicate in insert_bill_curpost()", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void chkRskRtn() throws Exception {

        selectIpsCard();

        tempInt = 0;
        sqlCmd = "select (sysdate - to_date(? ,'yyyymmdd')) as temp_int ";
        sqlCmd += " from dual ";
        setString(1, hCurpPurchaseDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }
        if (tempInt > 90) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hIardNewEndDate)) > 0) {
            hCurpRskType = "2";
            return;
        }
        if (!hIardAutoloadFlag.equals("Y") && hIcgeTxnAmt != 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hIardOppostDate)) > 0
                && hIardOppostDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hIardBalanceDate)) > 0
                && hIardBalanceDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hIardReturnDate)) > 0
                && hIardReturnDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hIardLockDate)) > 0
                && hIardLockDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
        /*
         * if ((h_card_block_status.equals("11")) ||
         * (h_card_block_status.equals("12"))) { if
         * ((comcr.str2long(h_curp_purchase_date) >
         * comcr.str2long(h_card_block_date)) &&
         * (comcr.str2long(h_curp_purchase_date) <=
         * comcr.str2long(h_card_new_end_date))) { h_curp_rsk_type = "4";
         * return; } }
         */
    }

    /**********************************************************************/
    void selectIpsCard() throws Exception {
        hIardCardNo = "";
        hIardCurrentCode = "";
        hIardNewBegDate = "";
        hIardNewEndDate = "";
        hIardAutoloadFlag = "";
        hIardBalanceDate = "";
        hIardReturnDate = "";
        hIardOppostDate = "";
        hIardLockDate = "";
        hIardBlackltFlag = "";

        sqlCmd = "select card_no,";
        sqlCmd += "current_code,";
        sqlCmd += "new_beg_date,";
        sqlCmd += "new_end_date,";
        sqlCmd += "autoload_flag,";
        sqlCmd += "balance_date,";
        sqlCmd += "return_date,";
        sqlCmd += "oppost_date,";
        sqlCmd += "lock_date,";
        sqlCmd += "blacklt_flag ";
        sqlCmd += " from ips_card  ";
        sqlCmd += "where ips_card_no = ? ";
        setString(1, hIcgeIpsCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIardCardNo = getValue("card_no");
            hIardCurrentCode = getValue("current_code");
            hIardNewBegDate = getValue("new_beg_date");
            hIardNewEndDate = getValue("new_end_date");
            hIardAutoloadFlag = getValue("autoload_flag");
            hIardBalanceDate = getValue("balance_date");
            hIardReturnDate = getValue("return_date");
            hIardOppostDate = getValue("oppost_date");
            hIardLockDate = getValue("lock_date");
            hIardBlackltFlag = getValue("blacklt_flag");
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilE021 proc = new BilE021();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
