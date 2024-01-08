/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109/11/23  V1.00.01   shiyuqi       updated for project coding standard    * 
 *  111/09/22  V1.00.02    JeffKung    updated for TCB                         *
 ******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*格式查核*/
public class BilA003 extends AccessDAO {
    private String progname = "格式查核   111/09/22  V1.00.02 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int tmpInt = 0;
    String hCallErrorDesc = "";

    String prgmId = "BilA003";
    String prgmName = "格式查核";
    String rptName = "BIL_A003R1";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String buf = "";
    String szTmp = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String iFileName = "";
    String iPostDate = "";
    String hMCurpModPgm = "";
    String hMCurpModTime = "";
    String hMCurpModUser = "";
    long hMCurpModSeqno = 0;

    String hBusinessDate = "";
    String hSystemDate = "";
    String hSystemDateF = "";
    String hMCurpReferenceNo = "";
    String hMCurpBillType = "";
    String hMCurpTransactionCode = "";
    String hMCurpCardNo = "";
    String hMCurpFilmNo = "";
    String hMCurpSignFlag = "";
    String hMCurpPurchaseDate = "";
    String hMCurpDestinationAmtChar = "";
    double hMCurpDestinationAmt = 0;
    String hMCurpDestinationCurrency = "";
    String hMCurpSourceAmtChar = "";
    double hMCurpSourceAmt = 0;
    String hMCurpSourceCurrency = "";
    String hMCurpMerchantEngName = "";
    String hMCurpMerchantCity = "";
    String hMCurpMerchantCountry = "";
    String hMCurpMerchantCategory = "";
    String hMCurpMerchantZip = "";
    String hMCurpMerchantState = "";
    String hMCurpUsageCode = "";
    String hMCurpReasonCode = "";
    String hMCurpAuthorization = "";
    String hMCurpBatchNo = "";
    String hMCurpProcessDate = "";
    String hMCurpMerchantNo = "";
    String hMCurpMerchantChiName = "";
    String hMCurpContractNo = "";
    String hMCurpOriginalNo = "";
    int hMCurpTerm = 0;
    int hMCurpTotalTerm = 0;
    String hMCurpAcctEngShortName = "";
    String hMCurpAcctChiShortName = "";
    String hMCurpDoubtType = "";
    String hMCurpAcctType = "";
    String hMCurpPromoteDept = "";
    String hMCurpProdNo = "";
    String hMCurpGroupCode = "";
    String hMCurpPSeqno = "";
    String hMCurpId = "";
    String hMCurpThisCloseDate = "";
    String hMCurpReferenceNoOriginal = "";
    String hMCurpCurrCode = "";
    String hMCurpRowid = "";
    double hCurpSourceAmtChar = 0;
    double hTempdig = 0;
    String hCurpDestinationAmtChar = "";
    String hCurpPurchaseDate = "";
    String hTempstr = "";
    String hCurpDoubtType = "";
    String hCurpRowid = "";
    String hPrintName = "";
    String hRptName = "";

    int totalCnt = 0;
    int pageCnt = 0;
    int lineCnt = 0;
    int indexCnt = 0;
    int pagePositiveCnt = 0;
    int totalPositiveCnt = 0;
    int pageNegativeCnt = 0;
    int totalNegativeCnt = 0;
    double pagePDestinationAmt = 0;
    double pagePSourceAmt = 0;
    double totalPDestinationAmt = 0;
    double totalPSourceAmt = 0;
    double pageNDestinationAmt = 0;
    double pageNSourceAmt = 0;
    double totalNDestinationAmt = 0;
    double totalNSourceAmt = 0;
    // *********************************************************

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
                comc.errExit("Usage : BilA003 callbatch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commonRtn();
            showLogMessage("I", "", "Process_date = " + hBusinessDate);

            hMCurpModPgm = javaProgram;
            hMCurpModTime = hModTime;
            hMCurpModUser = hModUser;
            hMCurpModSeqno = hModSeqno;

            selectBilCurpost();

            //改為線上報表
            //String filename = comc.getECSHOME() + "/reports/" + rptName + "_" + hSystemDateF;
            //comc.writeReportForTest(filename, lpar1);
            //showLogMessage("I", "", "產生報表 : " + filename);
            comcr.insertPtrBatchRpt(lpar1);
            
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]");
            
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
        sqlCmd += "from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusinessDate = getValue("business_date");
        }
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date, ";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') as h_system_date_f ";
        sqlCmd += "from dual ";
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemDateF = getValue("h_system_date_f");
        }
        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }

    /***********************************************************************/
    void selectBilCurpost() throws Exception {
        int indexCnt = 0;
        sqlCmd = "select ";
        sqlCmd += "reference_no,";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "card_no,";
        sqlCmd += "film_no,";
        sqlCmd += "purchase_date,";
        sqlCmd += "sign_flag,";
        sqlCmd += "dest_amt,";
        sqlCmd += "dest_curr,";
        sqlCmd += "source_amt,";
        sqlCmd += "source_curr,";
        sqlCmd += "mcht_eng_name,";
        sqlCmd += "mcht_city,";
        sqlCmd += "mcht_country,";
        sqlCmd += "mcht_category,";
        sqlCmd += "mcht_zip,";
        sqlCmd += "mcht_state,";
        sqlCmd += "auth_code,";
        sqlCmd += "bil_curpost.batch_no,";
        sqlCmd += "process_date,";
        sqlCmd += "bil_curpost.mcht_no,";
        sqlCmd += "mcht_chi_name,";
        sqlCmd += "contract_no,";
        sqlCmd += "term,";
        sqlCmd += "total_term,";
        sqlCmd += "acct_eng_short_name,";
        sqlCmd += "acct_chi_short_name,";
        sqlCmd += "doubt_type,";
        sqlCmd += "acct_type,";
        sqlCmd += "decode(curr_code,'','901',curr_code) as curr_code,";
        sqlCmd += "promote_dept,";
        sqlCmd += "prod_no,";
        sqlCmd += "group_code,";
        sqlCmd += "p_seqno,";
        sqlCmd += "nvl(uf_idno_id(id_p_seqno),'') as id,";
        sqlCmd += "bil_curpost.this_close_date,";
        sqlCmd += "reference_no_original,";
        sqlCmd += "bil_curpost.rowid  as rowid ";
        sqlCmd += " from bil_curpost , bil_postcntl ";
        sqlCmd += "where decode(format_chk_ok_flag,'','N',format_chk_ok_flag) in ('Y','y') ";
        sqlCmd += "  and decode(confirm_flag_p    ,'','N',confirm_flag_p)     in ('Y','y') ";
        sqlCmd += "  and decode(manual_upd_flag   ,'','N',manual_upd_flag)    != 'Y' ";
        sqlCmd += "  and decode(curr_post_flag    ,'','N',curr_post_flag)     != 'Y' ";
        sqlCmd += "  and doubt_type  = '' ";
        sqlCmd += "  and batch_date  = substr(bil_curpost.batch_no,1,8) ";
        sqlCmd += "  and batch_unit  = substr(bil_curpost.batch_no,9,2) ";
        sqlCmd += "  and batch_seq   = substr(bil_curpost.batch_no,11,4) ";
        sqlCmd += "order by bil_curpost.batch_no ";
        openCursor();
        while (fetchTable()) {
            hMCurpReferenceNo = getValue("reference_no");
            hMCurpBillType = getValue("bill_type");
            hMCurpTransactionCode = getValue("txn_code");
            hMCurpCardNo = getValue("card_no");
            hMCurpFilmNo = getValue("film_no");
            hMCurpSignFlag = getValue("sign_flag");
            hMCurpPurchaseDate = getValue("purchase_date");
            hMCurpDestinationAmt = getValueDouble("dest_amt");
            hMCurpDestinationCurrency = getValue("dest_curr");
            hMCurpSourceAmt = getValueDouble("source_amt");
            hMCurpSourceCurrency = getValue("source_curr");
            hMCurpMerchantEngName = getValue("mcht_eng_name");
            hMCurpMerchantCity = getValue("mcht_city");
            hMCurpMerchantCountry = getValue("mcht_country");
            hMCurpMerchantCategory = getValue("mcht_category");
            hMCurpMerchantZip = getValue("mcht_zip");
            hMCurpMerchantState = getValue("mcht_state");
            hMCurpAuthorization = getValue("authorization");
            hMCurpBatchNo = getValue("batch_no");
            hMCurpProcessDate = getValue("process_date");
            hMCurpMerchantNo = getValue("mcht_no");
            hMCurpMerchantChiName = getValue("mcht_chi_name");
            hMCurpContractNo = getValue("contract_no");
            hMCurpTerm = getValueInt("term");
            hMCurpTotalTerm = getValueInt("total_term");
            hMCurpAcctEngShortName = getValue("acct_eng_short_name");
            hMCurpAcctChiShortName = getValue("acct_chi_short_name");
            hMCurpDoubtType = getValue("doubt_type");
            hMCurpAcctType = getValue("acct_type");
            hMCurpPromoteDept = getValue("promote_dept");
            hMCurpProdNo = getValue("prod_no");
            hMCurpGroupCode = getValue("group_code");
            hMCurpPSeqno = getValue("p_seqno");
            hMCurpId = getValue("id");
            hMCurpThisCloseDate = getValue("this_close_date");
            hMCurpReferenceNoOriginal = getValue("reference_no_original");
            hMCurpCurrCode = getValue("curr_code");
            hMCurpRowid = getValue("rowid");

            // destination_amt_char & source_amt_char欄位已被刪除，改抓dest_amt &
            // source_amt
            hMCurpDestinationAmtChar = String.valueOf(hMCurpDestinationAmt);
            hMCurpSourceAmtChar = String.valueOf(hMCurpSourceAmt);

            totalCnt++;
            if (totalCnt % 5000 == 0 || totalCnt == 1) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
                commitDataBase();
            }
            
            /* debug
            showLogMessage("D", "","888 Card=[" + hMCurpCardNo + "]" + hMCurpTransactionCode + ",cnt=" + totalCnt);
            showLogMessage("D", "", "     amt=" + hMCurpSourceAmtChar + ",film=" + hMCurpFilmNo);
            */

            chkCurpost();
            if (hMCurpDoubtType.length() != 0) {
                if (indexCnt == 0) {
                	lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                    printHeader();
                }

                indexCnt++;
                printDetail();

                if (indexCnt >= 25) {
                    printFooter();
                    indexCnt = 0;
                }
            }
        }
        closeCursor();
        if (indexCnt != 0)
            printFooter();
    }

    /***********************************************************************/
    void chkCurpost() throws Exception {

        hMCurpDoubtType = "";

        /* debug
        showLogMessage("D", "", "  Date =[" + hMCurpPurchaseDate + "]");
        */

        try {
            hTempdig = Double.valueOf(hMCurpSourceAmtChar);
        } catch (Exception ex) {
            hMCurpDoubtType = "0001";
        }

        try {
            hTempdig = Double.valueOf(hMCurpDestinationAmtChar);
        } catch (Exception ex) {
            hMCurpDoubtType = "0001";
        }

        if (hTempdig == 0 && hMCurpCurrCode.equals("901"))
            hMCurpDoubtType = "0001";

        if (comc.isThisDateValid(hMCurpPurchaseDate, "yyyyMMdd") == false) {
            hMCurpDoubtType = "0002";
        }
        if (comcr.str2long(hBusinessDate) < comcr.str2long(hMCurpPurchaseDate))
            hMCurpDoubtType = "0002";

        if (hMCurpFilmNo.length() == 0)
            hMCurpDoubtType = "0003";

        //20220601: 若是目的地幣別為空值, 判斷特店國別碼為"TW"則將目的地幣別改為'901', 
        //                 或是原始幣別是'901', 也改成'901', 
        //                 非上述原因則報錯, 寫入格式錯誤報表
        if (hMCurpDestinationCurrency.length() == 0) {
        	if (hMCurpMerchantCountry.length()>= 2 && "TW".equals(hMCurpMerchantCountry.toUpperCase(Locale.TAIWAN).substring(0, 2))) {
        		hMCurpDestinationCurrency = "901";
        	} else if ("901".equals(hMCurpSourceCurrency)) {
        		hMCurpDestinationCurrency = "901";
        	} else {
        		hMCurpDoubtType = "0004"; 
        	}
        }
        if (hMCurpDestinationCurrency.length() == 0) {
            hMCurpDoubtType = "0004";
        }

        /* debug
        showLogMessage("D", "", "  update =" + hMCurpDoubtType);
        */

        if (hMCurpDoubtType.length() != 0) {
            daoTable   = "bil_curpost";
            updateSQL  = " doubt_type         = ?,";
            updateSQL += " mod_time           = sysdate, ";
            updateSQL += " manual_upd_flag    = 'N',";
            updateSQL += " format_chk_ok_flag = 'Y', ";
            updateSQL += " rsk_rsn = decode(payment_type,'I','I3','') "; //若是分期交易rsk_rsn放I3
            whereStr   = "where rowid         = ? ";
            setString(1, hMCurpDoubtType);
            setRowId(2, hMCurpRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_bil_curpost not found!", "", hMCurpReferenceNo);
            }
        } else {
            daoTable   = "bil_curpost";
            updateSQL  = " format_chk_ok_flag = 'N',";
            updateSQL += " dest_curr = ? , ";
            updateSQL += " mod_time           = sysdate ,";
            updateSQL += " manual_upd_flag    = 'N'";
            whereStr   = "where rowid         = ? ";
            setString(1, hMCurpDestinationCurrency);
            setRowId(2, hMCurpRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_bil_curpost not found!", "", hMCurpReferenceNo);
            }
        }
    }

    /***********************************************************************/
    void printHeader() {
        String reportH1 = "卡               號 參 考 號碼 微    縮    影   號"
                + "  碼 消費日期 代碼 消費金額(台幣) 消費金額(原幣) 幣別 入帳日期 批          號 原因";
        String reportL1 = "=================== ========== ==================="
                + "==== ======== ==== ============== ============== ==== ======== ============== ====";

        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: BIL_A003R1", 1);
        buf = comcr.insertStrCenter(buf, "信用卡  格式查核錯誤明細表", 132);
        buf = comcr.insertStr(buf, "頁    次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 122);
        lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日期:", 1);
        buf = comcr.insertStr(buf, chinDate, 11);
        lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, reportH1, 1);
        lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, reportL1, 1);
        lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printDetail() {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, hMCurpCardNo, 1);
        buf = comcr.insertStr(buf, hMCurpReferenceNo, 21);
        buf = comcr.insertStr(buf, hMCurpFilmNo, 32);

        if (hMCurpPurchaseDate.substring(0, 1).equals("2")) {
            szTmp = String.format("%8d", comcr.str2long(hMCurpPurchaseDate) - 19110000);
            buf = comcr.insertStr(buf, szTmp, 56);
        } else
            buf = comcr.insertStr(buf, hMCurpPurchaseDate, 56);

        buf = comcr.insertStr(buf, hMCurpTransactionCode, 65);
        buf = comcr.insertStr(buf, hMCurpDestinationAmtChar, 70);
        buf = comcr.insertStr(buf, hMCurpSourceAmtChar, 85);
        buf = comcr.insertStr(buf, hMCurpDestinationCurrency, 100);
        szTmp = String.format("%8d", comcr.str2long(hMCurpThisCloseDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 105);
        buf = comcr.insertStr(buf, hMCurpBatchNo, 114);
        buf = comcr.insertStr(buf, hMCurpDoubtType, 129);
        lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "1", buf));

        if (hMCurpSignFlag.equals("-")) {
            pagePositiveCnt++;
            totalPositiveCnt++;
            pagePDestinationAmt += hMCurpDestinationAmt;
            pagePSourceAmt += hMCurpSourceAmt;
            totalPDestinationAmt += hMCurpDestinationAmt;
            totalPSourceAmt += hMCurpSourceAmt;
        } else {
            pageNegativeCnt++;
            totalNegativeCnt++;
            pageNDestinationAmt += hMCurpDestinationAmt;
            pageNSourceAmt += hMCurpSourceAmt;
            totalNDestinationAmt += hMCurpDestinationAmt;
            totalNSourceAmt += hMCurpSourceAmt;
        }
    }

    /***********************************************************************/
    void printFooter() {

        buf = "";
        buf = comcr.insertStr(buf, "備註欄 1:金額錯誤 2:日期 3:微縮影號 4:幣別 ", 1);
        lpar1.add(comcr.putReport(rptName, rptName, sysDate, ++rptSeq, "2", buf));

        pagePDestinationAmt = 0;
        pageNDestinationAmt = 0;
        pageNSourceAmt = 0;
        pageNegativeCnt = 0;
        pagePositiveCnt = 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA003 proc = new BilA003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
