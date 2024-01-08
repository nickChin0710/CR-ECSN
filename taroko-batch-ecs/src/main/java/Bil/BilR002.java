/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  111/09/22  V1.00.01    shiyuqi   updated for project coding standard       * 
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

/*列印問題交易表/產生問題交易資料檔程式*/
public class BilR002 extends AccessDAO {
    private String progname = "列印問題交易表-產生問題交易資料檔程式   111/09/22  V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String fname1 = "";
    String rptName1 = "信用卡-列印問題交易表";
    String rptId1 = "BIL_R002R1";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq1 = 0;
    String buf = "";
    String szTmp = "";
    String hCallBatchSeqno = "";

    String hSystemDateF = "";
    String hCurpBillType = "";
    String hCurpTxnCode = "";
    String hCurpBatchNo = "";
    String hCurpReferenceNo = "";
    String hCurpCardNo = "";
    String hCurpFilmNo = "";
    String hCurpPurchaseDate = "";
    double hCurpDestAmt = 0;
    String hCurpDestCurr = "";
    double hCurpSourceAmt = 0;
    String hCurpSourceCurr = "";
    String hCurpMchtEngName = "";
    String hCurpMchtCity = "";
    String hCurpMchtCountry = "";
    String hCurpMchtCategory = "";
    String hCurpMchtZip = "";
    String hCurpMchtState = "";
    String hCurpAuthCode = "";
    String hCurpProcessDate = "";
    String hCurpMchtNo = "";
    String hCurpMchtChiName = "";
    String hCurpContractNo = "";
    String hCurpTerm = "";
    String hCurpTotalTerm = "";
    String hCurpAcctEngShortName = "";
    String hCurpAcctChiShortName = "";
    String hCurpRskType = "";
    String hCurpAcctType = "";
    String hCurpAcctStatus = "";
    String hCurpCurrCode = "";
    String hCurpOppostDate = "";
    String hCurpPromoteDept = "";
    String hCurpProdNo = "";
    String hCurpGroupCode = "";
    String hCurpPSeqno = "";
    String hCurpThisCloseDate = "";
    String hCurpReferenceNoOriginal = "";
    int hR001ReportSeq = 0;
    String hCardOppostReason = "";
    String hCardOppostDate = "";
    String hPrintName = "";
    String hRptName = "";

    String tempX14 = "";
    String hBatchNoFrom = "";
    String hBusinssDate = "";
    String hBatchNoEnd = "";
    int totalCnt = 0;
    int lineCnt = 0;
    int pageCnt = 0;
    int indexCnt = 0;
    double pagePDestAmt = 0;
    double pagePSourceAmt = 0;
    double pageNDestAmt = 0;
    double pageNSourceAmt = 0;
    int pageNegativeCnt = 0;
    int pagePositiveCnt = 0;
    double totalPDestAmt = 0;
    double totalPSourceAmt = 0;
    double totalNDestAmt = 0;
    double totalNSourceAmt = 0;
    int totalPositiveCnt = 0;
    int totalNegativeCnt = 0;

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
                comc.errExit("Usage : BilR002 batch_no", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();

            if (args.length == 0) {
                tempX14 = String.format("%s000000", hBusinssDate);
                hBatchNoFrom = tempX14;
                tempX14 = String.format("%szzzzzz", hBusinssDate);
                hBatchNoEnd = tempX14;

            }
            if (args.length == 1) {
                hBatchNoFrom = args[0];
                tempX14 = String.format("%szzzzzz", hBusinssDate);
                hBatchNoEnd = tempX14;
            }
            if (args.length == 2) {
                hBatchNoFrom = args[0];
                hBatchNoEnd = args[1];
            }

            checkOpen();

            selectBilR001r1();
            selectBilCurpost();

            if (pageCnt > 0) {
                lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "\f"));
            }
            
            comcr.insertPtrBatchRpt(lpar1);
            //comc.writeReport(fname1, lpar1);
            //comcr.lpRtn(rptName1, hSystemDateF);
            
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
    void commonRtn() throws Exception {
        hBusinssDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void checkOpen() throws Exception {
        hSystemDateF = "";
        sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += " from dual ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hSystemDateF = getValue("h_system_date_f");
        }

        fname1 = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptName1, hSystemDateF);

        fname1 = Normalizer.normalize(fname1, java.text.Normalizer.Form.NFKD);

    }

    /***********************************************************************/
    void selectBilR001r1() throws Exception {
        hR001ReportSeq = 0;

        sqlCmd = "select max(report_seq) h_r001_report_seq ";
        sqlCmd += " from bil_r001r1  ";
        sqlCmd += "where report_type ='0002' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hR001ReportSeq = getValueInt("h_r001_report_seq");
        }

    }

    /***********************************************************************/
    void selectBilCurpost() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "a.bill_type,";
        sqlCmd += "a.txn_code,";
        sqlCmd += "a.batch_no,";
        sqlCmd += "a.reference_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.film_no,";
        sqlCmd += "a.purchase_date,";
        sqlCmd += "a.dest_amt,";
        sqlCmd += "a.dest_curr,";
        sqlCmd += "a.source_amt,";
        sqlCmd += "a.source_curr,";
        sqlCmd += "a.mcht_eng_name,";
        sqlCmd += "a.mcht_city,";
        sqlCmd += "a.mcht_country,";
        sqlCmd += "a.mcht_category,";
        sqlCmd += "a.mcht_zip,";
        sqlCmd += "a.mcht_state,";
        sqlCmd += "a.auth_code,";
        sqlCmd += "a.process_date,";
        sqlCmd += "a.mcht_no,";
        sqlCmd += "a.mcht_chi_name,";
        sqlCmd += "a.contract_no,";
        sqlCmd += "a.term,";
        sqlCmd += "a.total_term,";
        sqlCmd += "a.acct_eng_short_name,";
        sqlCmd += "a.acct_chi_short_name,";
        sqlCmd += "a.rsk_type,";
        sqlCmd += "a.acct_type,";
        sqlCmd += "b.acct_status,";
        sqlCmd += "a.curr_code,";
        sqlCmd += "c.oppost_date,";
        sqlCmd += "a.promote_dept,";
        sqlCmd += "a.prod_no,";
        sqlCmd += "a.group_code,";
        sqlCmd += "a.acno_p_seqno,";
        sqlCmd += "a.this_close_date,";
        sqlCmd += "a.reference_no_original ";
        sqlCmd += "from bil_curpost a, act_acno b, crd_card c ";
        sqlCmd += "where rsk_type in ('2','3') ";
        sqlCmd += "and decode(curr_post_flag,'','N',curr_post_flag) != 'Y' ";
        sqlCmd += " and b.acno_p_seqno = a.acno_p_seqno ";
        sqlCmd += " and c.card_no = a.card_no ";
        sqlCmd += "order by batch_no,rsk_type,card_no ";
        openCursor();
        while (fetchTable()) {
            hCurpBillType = getValue("bill_type");
            hCurpTxnCode = getValue("txn_code");
            hCurpBatchNo = getValue("batch_no");
            hCurpReferenceNo = getValue("reference_no");
            hCurpCardNo = getValue("card_no");
            hCurpFilmNo = getValue("film_no");
            hCurpPurchaseDate = getValue("purchase_date");
            hCurpDestAmt = getValueDouble("dest_amt");
            hCurpDestCurr = getValue("dest_curr");
            hCurpSourceAmt = getValueDouble("source_amt");
            hCurpSourceCurr = getValue("source_curr");
            hCurpMchtEngName = getValue("mcht_eng_name");
            hCurpMchtCity = getValue("mcht_city");
            hCurpMchtCountry = getValue("mcht_country");
            hCurpMchtCategory = getValue("mcht_category");
            hCurpMchtZip = getValue("mcht_zip");
            hCurpMchtState = getValue("mcht_state");
            hCurpAuthCode = getValue("auth_code");
            hCurpProcessDate = getValue("process_date");
            hCurpMchtNo = getValue("mcht_no");
            hCurpMchtChiName = getValue("mcht_chi_name");
            hCurpContractNo = getValue("contract_no");
            hCurpTerm = getValue("term");
            hCurpTotalTerm = getValue("total_term");
            hCurpAcctEngShortName = getValue("acct_eng_short_name");
            hCurpAcctChiShortName = getValue("acct_chi_short_name");
            hCurpRskType = getValue("rsk_type");
            hCurpAcctType = getValue("acct_type");
            hCurpAcctStatus = getValue("acct_status");
            hCurpCurrCode = getValue("curr_code");
            hCurpOppostDate = getValue("oppost_date");
            hCurpPromoteDept = getValue("promote_dept");
            hCurpProdNo = getValue("prod_no");
            hCurpGroupCode = getValue("group_code");
            hCurpPSeqno = getValue("acno_p_seqno");
            hCurpThisCloseDate = getValue("this_close_date");
            hCurpReferenceNoOriginal = getValue("reference_no_original");

            totalCnt++;

            if (lineCnt == 0) {
                printHeader();
                tempX14 = hCurpBatchNo;
            }
            if (!hCurpBatchNo.equals(tempX14)) {
                printFooter();
                if (pageCnt > 0)
                    lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "\f"));
                
                //分頁控制
                lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
                tempX14 = hCurpBatchNo;
            }

            if (indexCnt >= 12) {
                if (pageCnt > 0)
                    lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "\f"));
                
                //分頁控制
                lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
            }

            printDetail();
        }
        closeCursor();

        if (indexCnt != 0)
            printFooter();

    }

    /***********************************************************************/
    void printHeader() throws Exception {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptId1, 1);
        buf = comcr.insertStrCenter(buf, "信用卡  消費帳單系統列問交明細表", 132);
        buf = comcr.insertStr(buf, "頁  次 :", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 120);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "批     號:", 1);
        buf = comcr.insertStr(buf, hCurpBatchNo, 12);
        buf = comcr.insertStrCenter(buf, "屬問題交易", 132);
        buf = comcr.insertStr(buf, "印表日 :", 110);
        buf = comcr.insertStr(buf, chinDate, 120);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "入帳日期 :", 1);
        szTmp = String.format("%8d", comcr.str2long(hCurpThisCloseDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 12);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf + "\n"));

        buf = "";
        buf = comcr.insertStr(buf, "*****  停   用  *****", 112);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "卡            號", 1);
        buf = comcr.insertStr(buf, "參考號碼", 18);
        buf = comcr.insertStr(buf, "微   縮   影   號   碼", 29);
        buf = comcr.insertStr(buf, "消費日期", 53);
        buf = comcr.insertStr(buf, "代碼", 62);
        buf = comcr.insertStr(buf, "消費金額(台幣)", 68);
        buf = comcr.insertStr(buf, "消費金額(原幣)", 84);
        buf = comcr.insertStr(buf, "/幣別", 99);
        buf = comcr.insertStr(buf, "授權碼", 105);
        buf = comcr.insertStr(buf, "類  別", 112);
        buf = comcr.insertStr(buf, "原因", 119);
        buf = comcr.insertStr(buf, "日    期", 124);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "商店代號", 1);
        buf = comcr.insertStr(buf, "團代", 15);
        buf = comcr.insertStr(buf, "商  店  名  稱", 21);
        buf = comcr.insertStr(buf, "商店所在地城市", 62);
        buf = comcr.insertStr(buf, "商店所在地國別", 78);
        buf = comcr.insertStr(buf, "商店類別", 93);
        buf = comcr.insertStr(buf, "風管疑異碼", 105);
        buf = comcr.insertStr(buf, "是否轉催收", 122);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, hCurpCardNo, 1);
        buf = comcr.insertStr(buf, hCurpReferenceNo, 18);
        buf = comcr.insertStr(buf, hCurpFilmNo, 29);
        if (hCurpPurchaseDate.length() > 0) {
            szTmp = String.format("%8d", comcr.str2long(hCurpPurchaseDate) - 19110000);
        }
        buf = comcr.insertStr(buf, szTmp, 54);
        buf = comcr.insertStr(buf, hCurpTxnCode, 63);
        szTmp = comcr.commFormat("3$,3$,3$.2$", hCurpDestAmt);
        buf = comcr.insertStr(buf, szTmp, 68);
        szTmp = comcr.commFormat("3$,3$,3$.2$", hCurpSourceAmt);
        buf = comcr.insertStr(buf, szTmp, 84);
        buf = comcr.insertStr(buf, hCurpSourceCurr, 100);
        buf = comcr.insertStr(buf, hCurpAuthCode, 107);
        buf = comcr.insertStr(buf, hCurpCurrCode, 114);

        hCardOppostReason = "";
        hCardOppostDate = "";
        sqlCmd = "select oppost_reason,";
        sqlCmd += "oppost_date ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no  = ? ";
        setString(1, hCurpCardNo);
        if (selectTable() > 0) {
            hCardOppostReason = getValue("oppost_reason");
            hCardOppostDate = getValue("oppost_date");
            
            buf = comcr.insertStr(buf, hCardOppostReason, 120);
            if (hCardOppostDate.length() > 0) {
                szTmp = String.format("%8d", comcr.str2long(hCardOppostDate) - 19110000);
                buf = comcr.insertStr(buf, szTmp, 124);
            }
            
        } else {
        	if (hCurpOppostDate.length() > 0) {
                szTmp = String.format("%8d", comcr.str2long(hCurpOppostDate) - 19110000);
                buf = comcr.insertStr(buf, szTmp, 125);
            }
        }
        
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        if ((!hCurpTxnCode.equals("06")) && (!hCurpTxnCode.equals("17")) && (!hCurpTxnCode.equals("25"))
                && (!hCurpTxnCode.equals("27")) && (!hCurpTxnCode.equals("28"))
                && (!hCurpTxnCode.equals("29"))) {
            pagePositiveCnt++;
            totalPositiveCnt++;
            pagePDestAmt = pagePDestAmt + hCurpDestAmt;
            pagePSourceAmt = pagePSourceAmt + hCurpSourceAmt;
            totalPDestAmt = totalPDestAmt + hCurpDestAmt;
            totalPSourceAmt = totalPSourceAmt + hCurpSourceAmt;
        } else {
            pageNegativeCnt++;
            totalNegativeCnt++;
            pageNDestAmt = pageNDestAmt + hCurpDestAmt;
            pageNSourceAmt = pageNSourceAmt + hCurpSourceAmt;
            totalNDestAmt = totalNDestAmt + hCurpDestAmt;
            totalNSourceAmt = totalNSourceAmt + hCurpSourceAmt;
        }

        buf = "";
        buf = comcr.insertStr(buf, hCurpMchtNo, 1);
        buf = comcr.insertStr(buf, hCurpGroupCode, 15);
        buf = comcr.insertStr(buf, hCurpMchtChiName, 21);
        buf = comcr.insertStr(buf, hCurpMchtCity, 62);
        buf = comcr.insertStr(buf, hCurpMchtCountry, 78);
        buf = comcr.insertStr(buf, hCurpMchtCategory, 95);
        buf = comcr.insertStr(buf, hCurpRskType, 105);
        if (hCurpRskType.equals("2")) {
            buf = comcr.insertStr(buf, "停卡", 105);
        }
        if (hCurpRskType.equals("3")) {
            buf = comcr.insertStr(buf, "監控", 105);
        }
        if (hCurpAcctStatus.equals("3")) {
            szTmp = "Y";
        } else {
            szTmp = "N";
        }
        buf = comcr.insertStr(buf, szTmp, 126);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));
    }

    /***********************************************************************/
    void printFooter() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "正值:筆數:", 1);
        szTmp = String.format("%6d", pagePositiveCnt);
        buf = comcr.insertStr(buf, szTmp, 13);
        buf = comcr.insertStr(buf, "台幣金額 :", 25);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", pagePDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "負值:筆數:", 1);
        szTmp = String.format("%6d", pageNegativeCnt);
        buf = comcr.insertStr(buf, szTmp, 13);
        buf = comcr.insertStr(buf, "台幣金額 :", 25);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", pageNDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId1, rptName1, sysDate, ++rptSeq1, "0", buf));

        pagePDestAmt = 0;
        pagePSourceAmt = 0;
        pageNDestAmt = 0;
        pageNSourceAmt = 0;
        pageNegativeCnt = 0;
        pagePositiveCnt = 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilR002 proc = new BilR002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
