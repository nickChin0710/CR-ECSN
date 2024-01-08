/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  111/08/10  V1.00.00    JeffKung     program initial                        *
*  112/08/14  V1.00.01    JeffKung     fix date                               *                                                                           *
*******************************************************************************/

package Dbb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*TSCC 交易別/會計科目轉換作業*/
public class DbbE020 extends AccessDAO {
    private String progname = "VD悠遊卡 交易別/會計科目轉換作業  112/08/14 V1.00.02";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hTempUser = "";
    int debug = 0;

    String prgmId = "DbbE020";
    String prgmName = "VD悠遊卡 交易別/會計科目轉換作業 ";
    String rptName = "Dbb_E020";
    List<Map<String, Object>> lpar111 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String buf = "";
    String szTmp = "";
    String stderr = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
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
    String hDccgBatchNo = "";
    int hDccgSeqNo = 0;
    String hDccgCardNo = "";
    String hDccgTscCardNo = "";
    String hDccgBillType = "";
    String hDccgTxnCode = "";
    String hDccgTscTxCode = "";
    String hDccgPurchaseDate = "";
    String hDccgPurchaseTime = "";
    String hDccgMchtNo = "";
    String hDccgMchtCategory = "";
    String hDccgMchtChiName = "";
    double hDccgDestAmt = 0;
    String hDccgDestCurr = "";
    String hDccgBillDesc = "";
    String hDccgTrafficCd = "";
    String hDccgTrafficAbbr = "";
    String hDccgAddrCd = "";
    String hDccgAddrAbbr = "";
    String hDccgPostFlag = "";
    String hDccgFileName = "";
    String hDccgTscError = "";
    String hDccgRowid = "";
    String hTempX08 = "";
    String hCurpReferenceNo = "";
    String hCardIdPSeqno = "";
    String hCardMajorIdPSeqno = "";
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
    String hCurpPosPinCapability = "";
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
    String hCurpBinType = "";
    String hCurpPSeqno = "";
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
    String hCardBlockStatus = "";
    String hCardBlockDate = "";
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
    // String h_tard_card_no = "";
    // String h_tard_current_code = "";
    // String h_tard_new_beg_date = "";
    String hTardNewEndDate = "";
    String hTardAutoloadFlag = "";
    String hTardBalanceDate = "";
    String hTardReturnDate = "";
    String hTardOppostDate = "";
    String hTardLockDate = "";
    // String h_tard_blacklt_flag = "";
    // String h_tard_blacklt_s_date = "";
    // String h_tard_blacklt_e_date = "";

    String hBiunAuthFlag = "";
    int totCnt = 0;
    int errCnt = 0;

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
                comc.errExit("Usage : DbbE020 batch_seq", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            //開啟debug
            for (int argi=0; argi < args.length ; argi++ ) {
            	  if (args[argi].equals("debug")) {
            		  debug=1;
            	  }
            }
            commonRtn();
            hCurpModPgm = prgmId;
            hCurpModTime = hModTime;
            hCurpModUser = hModUser;
            hCurpModSeqno = hModSeqno;

            //不要印報表
            //printFirstRtn();

            selectTscDccgAll();

            int rgtCnt = totCnt - errCnt;
            
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "程式執行結束,筆數=[" + rgtCnt + "]" + totCnt);

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***
     * @throws Exception
     ************************************************************/
    void commonRtn() throws Exception {
        sqlCmd =  "select business_date , ";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate = getValue("h_system_date");
        } else {
            stderr = "select_prt_businday  not found";
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }

        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }

    /***********************************************************************/
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

        szTmp = String.format("%24s", "TSCC VD請款媒體入檔合計表");
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
        sqlCmd += "sum(dest_amt) p_sum,";
        sqlCmd += "batch_no ";
        sqlCmd += "from tsc_dccg_all ";
        sqlCmd += "group by batch_no ";
        recordCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "888 select 1 rtn=[" + recordCnt + "]");
        for (int i = 0; i < recordCnt; i++) {
            pCount = getValueInt("p_count", i);
            pSum = getValueDouble("p_sum", i);
            pBatchNo = getValue("batch_no", i);
            hTThisCloseDate = hSystemDate;

            buf = "";
            szTmp = comcr.commFormat("3z,3z", pCount);
            buf = comcr.insertStr(buf, szTmp, 19);
            szTmp = comcr.commFormat("3$,3$,3$", pSum);
            buf = comcr.insertStr(buf, szTmp, 29);
            buf = comcr.insertStr(buf, pBatchNo, 41);
            szTmp = String.format("%8d", comcr.str2long(hTThisCloseDate) - 19110000);
            buf = comcr.insertStr(buf, szTmp, 56);
            lpar111.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "1", buf));
        }

        String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptName, hSystemDateF);
        filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
        //comc.writeReportForTest(filename, lpar111);

    }

    /***********************************************************************/
    //20220816 sql checked
    void selectTscDccgAll() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "batch_no,";
        sqlCmd += "seq_no,";
        sqlCmd += "card_no,";
        sqlCmd += "tsc_card_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "tsc_tx_code,";
        sqlCmd += "purchase_date,";
        sqlCmd += "purchase_time,";
        sqlCmd += "mcht_no,";
        sqlCmd += "mcht_category,";
        sqlCmd += "mcht_chi_name,";
        sqlCmd += "dest_amt,";
        sqlCmd += "dest_curr,";
        sqlCmd += "bill_desc,";
        sqlCmd += "traffic_cd,";
        sqlCmd += "traffic_abbr,";
        sqlCmd += "addr_cd,";
        sqlCmd += "addr_abbr,";
        sqlCmd += "post_flag,";
        sqlCmd += "file_name,";
        sqlCmd += "tsc_error,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from tsc_dccg_all ";
        sqlCmd += "where post_flag    = 'N' ";
        sqlCmd += "  and ( tsc_error != '' or tsc_error = '0000') ";
        
        openCursor();

		totCnt = 0;
		while (fetchTable()) {
            hDccgBatchNo = getValue("batch_no");
            hDccgSeqNo = getValueInt("seq_no");
            hDccgCardNo = getValue("card_no");
            hDccgTscCardNo = getValue("tsc_card_no");
            hDccgBillType = getValue("bill_type");
            hDccgTxnCode = getValue("txn_code");
            hDccgTscTxCode = getValue("tsc_tx_code");
            hDccgPurchaseDate = getValue("purchase_date");
            hDccgPurchaseTime = getValue("purchase_time");
            hDccgMchtNo = getValue("mcht_no");
            hDccgMchtCategory = getValue("mcht_category");
            hDccgMchtChiName = getValue("mcht_chi_name");
            hDccgDestAmt = getValueDouble("dest_amt");
            hDccgDestCurr = getValue("dest_curr");
            hDccgBillDesc = getValue("bill_desc");
            hDccgTrafficCd = getValue("traffic_cd");
            hDccgTrafficAbbr = getValue("traffic_abbr");
            hDccgAddrCd = getValue("addr_cd");
            hDccgAddrAbbr = getValue("addr_abbr");
            hDccgPostFlag = getValue("post_flag");
            hDccgFileName = getValue("file_name");
            hDccgTscError = getValue("tsc_error");
            hDccgRowid = getValue("rowid");

            totCnt++;

            hCurpBatchNo = hDccgBatchNo;
            hCurpCardNo = hDccgCardNo;
            hCurpBillType = hDccgBillType;
            hCurpTxnCode = hDccgTxnCode;
            
            hCurpMchtEngName = "";
            //要轉成對應給etabs的摘要代碼
            if ("DCCG".equals(comc.getSubString(hDccgFileName, 0,4)) ) {
            	if("05".equals(hDccgTxnCode)) {
            		hCurpMchtEngName = "VDUA-DCCG";  //悠遊加值
            	}
            } else if ("DCPR".equals(comc.getSubString(hDccgFileName, 0,4)) ) {
            	if("05".equals(hDccgTxnCode)) {
            		hCurpMchtEngName = "VDUQ-DCPR";  //悠遊收款
            	} 
            } else if ("DCRT".equals(comc.getSubString(hDccgFileName, 0,4)) ) {
            	if("05".equals(hDccgTxnCode)) {
            		hCurpMchtEngName = "VDUV-DCRT";  //悠遊收款
            	} else {
            		hCurpMchtEngName = "VDUC-DCRT";  //悠遊退卡
            	}
            } else if ("DCBD".equals(comc.getSubString(hDccgFileName, 0,4)) ) {
            	if("05".equals(hDccgTxnCode)) {
            		hCurpMchtEngName = "VDUV-DCBD";  //悠遊收款
            	} else {
            		hCurpMchtEngName = "VDUL-DCBD";  //悠遊掛失
            	}
            } else if ("DCBP".equals(comc.getSubString(hDccgFileName, 0,4)) ) {
            	if("05".equals(hDccgTxnCode)) {
            		hCurpMchtEngName = "VDUV-DCBP";  //悠遊收款
            	} else {
            		hCurpMchtEngName = "VDUR-DCBP";  //悠遊退款
            	}
            }
            
            hCurpPurchaseDate = hDccgPurchaseDate;
            hCurpPurchaseTime = hDccgPurchaseTime;
            hCurpFilmNo = String.format("%-4.4s%-8.8s%-6.6s", hDccgTscTxCode
                                          , hDccgPurchaseDate, hDccgPurchaseTime);
            hCurpFilmNo = String.format("%-16.16s%03d", hDccgTscCardNo, hDccgSeqNo);
            hCurpMchtNo = hDccgMchtNo;
            hCurpMchtCategory = hDccgMchtCategory;
            hCurpMchtChiName = hDccgMchtChiName;
            hCurpMchtCountry = "TW";
            hCurpMchtCity = "taipei";
            hCurpAcexterDesc = hDccgBillDesc;

            hCurpDestAmt = hDccgDestAmt;
            hCurpDestAmtChar = String.format("%9.0f", hDccgDestAmt);

            hCurpAcquireDate = hBusinessDate;
            hCurpThisCloseDate = hBusinessDate;
            hCurpProcessDate = hBusinessDate;

            chkDbcCard();

            hCurpDestCurr = hDccgDestCurr;
            hCurpSourceCurr = hCurpDestCurr;
            hCurpSourceAmtChar = hCurpDestAmtChar;
            hCurpSourceAmt = hCurpDestAmt;

            int rtn = selectDbaAcno();
            //if(rtn != 0)   continue;
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
            hCurpReferenceNo = String.format("%2.2s%s", hBusinessDate.substring(2), hTempX08);

            hCurpAcexterDesc = hDccgBillDesc;

            insertDbbCurpost();
            
            daoTable = "tsc_dccg_all";
            updateSQL = "reference_no = ?,";
            updateSQL += " post_flag   = 'Y'";
            whereStr = "where rowid  = ? ";
            setString(1, hCurpReferenceNo);
            setRowId(2, hDccgRowid);
            updateTable();
            if (notFound.equals("Y")) {
                String errMsg = "update_tsc_dccg_all not found!";
                comcr.errRtn(errMsg, "", hCallBatchSeqno);
            }

            daoTable   = "tsc_dccg_pre";
            updateSQL  = " reference_no  = ? ,";
            updateSQL += " id_p_seqno    = ? ,";
            updateSQL += " post_date     = ?";
            whereStr   = "where tsc_card_no   = ?  ";
            whereStr  += "  and file_name     = ?  ";
            whereStr  += "  and purchase_date = ?  ";
            whereStr  += "  and dest_amt      = ?  ";
            whereStr  += "  and reference_no != '' ";
            setString(1, hCurpReferenceNo);
            setString(2, hCardIdPSeqno);
            setString(3, hBusinessDate);
            setString(4, hDccgTscCardNo);
            setString(5, hDccgFileName);
            setString(6, hDccgPurchaseDate);
            setDouble(7, hDccgDestAmt);
            updateTable();
        }
		
		closeCursor();
    }

    /**********************************************************************/
    void initDbcCard() {
        hCardPSeqno = "";
        hCardIdPSeqno = "";
        hCardMajorIdPSeqno = "";
        hCardCardType = "";
        hCardBinNo = "";
        hCardBinType = "";
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

    /**********************************************************************/
    //20220816 sql checked
    void chkDbcCard() throws Exception {
        initDbcCard();
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
        sqlCmd += "block_date,";
        sqlCmd += "major_id_p_seqno,";
        sqlCmd += "id_p_seqno ";
        sqlCmd += " from dbc_card  ";
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
            hCardBlockDate = getValue("block_date");
            hCardIdPSeqno = getValue("id_p_seqno");
            hCardMajorIdPSeqno = getValue("major_id_p_seqno");
        }

        hCurpBinType = hCardBinType;
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
        hCurpMajorIdPSeqno = hCardMajorIdPSeqno;
        hCurpBlockDate = hCardBlockDate;
    }

    /***********************************************************************/
    //20220816 sql checked
    int selectDbaAcno() throws Exception {
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
        sqlCmd += "pay_by_stage_flag,";
        sqlCmd += "autopay_acct_no ";
        sqlCmd += " from dba_acno  ";
        sqlCmd += "where p_seqno  = ? ";
        setString(1, hCurpPSeqno);
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoAcctType = getValue("acct_type");
            hAcnoAcctKey = getValue("acct_key");
            hAcnoAcctStatus = getValue("acct_status");
            hAcnoStmtCycle = getValue("stmt_cycle");
            hAcnoPayByStageFlag = getValue("pay_by_stage_flag");
            hAcnoAutopayAcctNo = getValue("autopay_acct_no");
        } else {
            stderr = String.format("fetch_dba_acno  error1[%s]", hCurpCardNo);
            errCnt++;
            return(1);
        }

        hCurpAcctType = hAcnoAcctType;
        hCurpAcctKey = hAcnoAcctKey;
        hCurpAcctStatus = hAcnoAcctStatus;
        hCurpStmtCycle = hAcnoStmtCycle;
        hCurpPayByStageFlag = hAcnoPayByStageFlag;
        hCurpAutopayAcctNo = hAcnoAutopayAcctNo;

        return(0);
    }

    /***********************************************************************/
    //20220816 sql checked
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
        setString(1, hDccgBillType);
        setString(2, hDccgTxnCode);
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
            hBityCashAdvState = getValue("cash_adv_state");
            hBityEntryAcct = getValue("entry_acct");
            hBityChkErrBill = getValue("chk_err_bill");
            hBityDoubleChk = getValue("double_chk");
            hBityFormatChk = getValue("format_chk");
            hBityMerchFee = getValue("merch_fee");
        } else {
            stderr = String.format(" bill_type =[%s]\n", hDccgBillType);
            showLogMessage("I", "", stderr);
            stderr = String.format(" txn_code  =[%s]\n", hDccgTxnCode);
            showLogMessage("I", "", stderr);
            stderr = String.format("fetch_ptr_billtype  error1\n");
            comcr.errRtn(stderr, "", hCallBatchSeqno);

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
    //20220816 sql checked
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
            stderr = String.format(" acct_code =[%s]\n", hCurpAcctCode);
            showLogMessage("I", "", stderr);
            stderr = String.format("select_prt_actcode not found");
            comcr.errRtn(stderr, "", hCallBatchSeqno);

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
    void insertDbbCurpost() throws Exception {
        hCurpValidFlag = hBiunAuthFlag;
        
        /*
        if (hDccgMchtNo.equals("EASY8003")) {
            chkRskRtn();
        }
        */

        setValue("reference_no"    , hCurpReferenceNo);
        setValue("bill_type"       , hCurpBillType);
        setValue("txn_code"        , hCurpTxnCode);
        setValue("bin_type"        , hCurpBinType);
        setValue("card_no"         , hCurpCardNo);
        setValue("film_no"         , hCurpFilmNo);
        setValue("purchase_date"   , hCurpPurchaseDate);
        setValueDouble("dest_amt"  , hCurpDestAmt);
        setValue("dest_curr"       , hCurpDestCurr);
        setValueDouble("source_amt", hCurpSourceAmt);
        setValue("source_curr"     , hCurpSourceCurr);
        setValue("curr_code"       , "901");
        setValueDouble("dc_dest_amt" , hCurpDestAmt);
        setValueDouble("cash_pay_amt", hCurpDestAmt);
        setValue("mcht_eng_name"   , hCurpMchtEngName);
        setValue("mcht_city"       , hCurpMchtCity);
        setValue("mcht_country"    , hCurpMchtCountry);
        setValue("mcht_category"   , hCurpMchtCategory);
        setValue("mcht_zip"        , hCurpMchtZip);
        setValue("mcht_state"      , hCurpMchtState);
        setValue("auth_code"       , hCurpAuthorization);
        setValue("pos_entry_mode"  , hCurpPosEntryMode);
        setValue("process_date"    , hCurpProcessDate);
        setValue("mcht_no"         , hCurpMchtNo);
        setValue("mcht_chi_name"   , hCurpMchtChiName);
        setValue("acquire_date"    , hCurpAcquireDate);
        setValue("contract_no"     , hCurpContractNo);
        setValueInt("term"         , comcr.str2int(hCurpTerm));
        setValueInt("total_term"   , comcr.str2int(hCurpTotalTerm));
        setValue("batch_no"        , hCurpBatchNo);
        setValue("sign_flag"       , hCurpSignFlag);
        setValue("acct_code"       , hCurpAcctCode);
        setValue("acct_item"       , hCurpAcctItem);
        setValue("acct_eng_short_name"  , hCurpAcctEngShortName);
        setValue("acct_chi_short_name"  , hCurpAcctChiShortName);
        setValue("item_order_normal"    , hCurpItemOrderNormal);
        setValue("item_order_back_date" , hCurpItemOrderBackDate);
        setValue("item_order_refund"    , hCurpItemOrderRefund);
        setValue("acexter_desc"         , hCurpAcexterDesc);
        setValue("entry_acct"           , hCurpEntryAcct);
        setValue("item_class_normal"    , hCurpItemClassNormal);
        setValue("item_class_back_date" , hCurpItemClassBackDate);
        setValue("item_class_refund"    , hCurpItemClassRefund);
        setValue("collection_mode"      , hCurpCollectionMode);
        setValue("fees_state"           , hCurpFeesState);
        setValue("cash_adv_state"       , hCurpCashAdvState);
        setValue("this_close_date"      , hCurpThisCloseDate);
        setValue("manual_upd_flag"      , hCurpManualUpdFlag);
        setValue("valid_flag"           , hCurpValidFlag);
        setValue("doubt_type"           , hCurpDoubtType);
        setValue("duplicated_flag"      , hCurpDuplicatedFlag);
        setValue("rsk_type"             , hCurpRskType);
        setValue("acct_type"            , hCurpAcctType);
        setValue("stmt_cycle"           , hCurpStmtCycle);
        setValue("major_card_no"        , hCurpMajorCardNo);
        setValue("promote_dept"         , hCurpPromoteDept);
        setValue("issue_date"           , hCurpIssueDate);
        setValue("prod_no"              , hCurpProdNo);
        setValue("group_code"           , hCurpGroupCode);
        setValue("p_seqno"              , hCurpPSeqno);
        setValue("id_p_seqno"           , hCurpIdPSeqno);
        setValue("major_id_p_seqno"     , hCurpMajorIdPSeqno);
        setValue("reference_no_original", "");
        setValue("fees_reference_no"    , "");
        setValue("tx_convt_flag"        , hCurpTxConvtFlag);
        setValue("acctitem_convt_flag"  , hCurpAcctitemConvtFlag);
        setValue("format_chk_ok_flag"   , hCurpFormatChkOkFlag);
        setValue("double_chk_ok_flag"   , hCurpDoubleChkOkFlag);
        setValue("err_chk_ok_flag"      , hCurpErrChkOkFlag);
        setValue("source_code"          , hCurpSourceCode);
        setValue("curr_post_flag"       , hCurpCurrPostFlag);
        setValue("mod_user"             , hCurpModUser);
        setValue("mod_time"             , sysDate + sysTime);
        setValue("mod_pgm"              , prgmId);
        setValueDouble("mod_seqno"      , hCurpModSeqno);

        setValue("ecs_platform_kind", "ET");
        setValue("ecs_cus_mcht_no", "006ET00001");
        
        daoTable = "dbb_curpost";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_curpost duplicate", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void initTscVDCard() {
        hTardNewEndDate = "";
        hTardAutoloadFlag = "";
        hTardBalanceDate = "";
        hTardReturnDate = "";
        hTardOppostDate = "";
        hTardLockDate = "";
    }

    /*************************************************************************/
    void chkRskRtn() throws Exception {
    	
    	hCurpRskType = "";

        selectTscVDCard();

        tempInt = 0;
        sqlCmd = "select (days(sysdate) - days(to_date(? ,'yyyymmdd'))) as temp_int ";
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
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hTardNewEndDate)) > 0) {
            hCurpRskType = "2";
            return;
        }
        if (!hTardAutoloadFlag.equals("Y") && hDccgDestAmt != 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hTardOppostDate)) > 0
                && hTardOppostDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hTardBalanceDate)) > 0
                && hTardBalanceDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hTardReturnDate)) > 0
                && hTardReturnDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
        if ((comcr.str2long(hCurpPurchaseDate) - comcr.str2long(hTardLockDate)) > 0
                && hTardLockDate.length() > 0) {
            hCurpRskType = "2";
            return;
        }
    }

    /***********************************************************************/
    //20220816 sql checked
    void selectTscVDCard() throws Exception {
        initTscVDCard();

        sqlCmd = "select ";
        sqlCmd += "new_end_date,";
        sqlCmd += "autoload_flag,";
        sqlCmd += "balance_date,";
        sqlCmd += "return_date,";
        sqlCmd += "oppost_date,";
        sqlCmd += "lock_date ";
        sqlCmd += " from tsc_vd_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hDccgTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTardNewEndDate = getValue("new_end_date");
            hTardAutoloadFlag = getValue("autoload_flag");
            hTardBalanceDate = getValue("balance_date");
            hTardReturnDate = getValue("return_date");
            hTardOppostDate = getValue("oppost_date");
            hTardLockDate = getValue("lock_date");
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbbE020 proc = new DbbE020();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
