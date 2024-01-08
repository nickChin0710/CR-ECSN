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

/*列印不合格帳單表/產生不合格帳單資料檔程式*/
public class BilR001 extends AccessDAO {
    private String progname = "列印不合格帳單表/產生不合格帳單資料檔程式   111/09/22  V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilR001";
    String prgmName = "列印不合格帳單表/產生不合格帳單資料檔程式";
    
    String rptId = "BIL_R001R1";
    String rptName = "信用卡不合格帳單明細表";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String errMsg = "";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";
    String iFileName = "";

    String hSystemDateF = "";
    String hCurpBillType = "";
    String hCurpTxnCode = "";
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
    // String h_curp_usage_code = "";
    // String h_curp_reason_code = "";
    String hCurpAuthCode = "";
    String hCurpProcessDate = "";
    String hCurpMchtNo = "";
    String hCurpMchtChiName = "";
    String hCurpContractNo = "";
    // String h_curp_goods_name = "";
    // String h_curp_original_no = "";
    // String h_curp_telephone_no = "";
    int hCurpTerm = 0;
    int hCurpTotalTerm = 0;
    // String h_curp_prod_name = "";
    String hCurpAcctEngShortName = "";
    String hCurpAcctChiShortName = "";
    String hCurpRskType = "";
    String hCurpDoubtType = "";
    String hCurpAcctType = "";
    // String h_curp_acct_key = "";
    // String h_curp_acct_status = "";
    // String h_curp_block_status = "";
    // String h_curp_block_date = "";
    // String h_curp_pay_by_stage_flag = "";
    // String h_curp_autopay_acct_no = "";
    String hCurpCurrCode = "";
    // String h_curp_oppost_date = "";
    String hCurpPromoteDept = "";
    String hCurpProdNo = "";
    String hCurpGroupCode = "";
    String hCurpBinType = "";
    String hCurpPSeqno = "";
    String hCurpIdPSeqno = "";
    String hCurpThisCloseDate = "";
    String hCurpBatchNo = "";
    String hCurpReferenceNo = "";
    // String h_busi_business_date = "";
    int hR001ReportSeq = 0;
    String hPrintName = "";
    String hRptName = "";

    String tempX14 = "";
    String hBatchNoFrom = "";
    String hBatchNoEnd = "";
    String hBusinssDate = "";
    String tempBatchNo = "";
    int totalCnt = 0;
    int indexCnt = 0;
    int pageCnt = 0;
    double pagePDestAmt = 0;
    double pagePSourceAmt = 0;
    double pageNDestAmt = 0;
    double pageNSourceAmt = 0;
    double totalNDestAmt = 0;
    double totalPDestAmt = 0;
    double totalPSourceAmt = 0;
    double totalNSourceAmt = 0;
    int lineCnt = 0;
    int totalPositiveCnt = 0;
    int totalNegativeCnt = 0;
    int pageNegativeCnt = 0;
    int pagePositiveCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 2) {
                String errMsg2 = "        1.batch_no : 批次號碼\n";
                errMsg2 += "                     a.yyyymmdd:西元日期\n";
                errMsg2 += "                     b.請款來源前二碼:'NC','OB','OU'\n";
                errMsg2 += "                     c.序號: 4 碼\n";
                comc.errExit("Usage : BilR001 batch_no_f batch_no_e", errMsg2);
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

            selectBilR001R1();
            selectBilCurpost();

            if (pageCnt > 0) {
                hSystemDateF = "";
                sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') as h_system_date_f from dual";
                if (selectTable() > 0) {
                    hSystemDateF = getValue("h_system_date_f");
                }

                //改成線上報表
                //String filename = String.format("%s/reports/%s_%s", comc.getECSHOME(), rptName, hSystemDateF);
                //filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
                //comc.writeReport(filename, lpar1);
                comcr.insertPtrBatchRpt(lpar1);
            }

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
    void selectBilR001R1() throws Exception {
        hR001ReportSeq = 0;

        sqlCmd = "select max(report_seq) h_r001_report_seq ";
        sqlCmd += " from bil_r001r1  ";
        sqlCmd += "where report_type ='0001' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hR001ReportSeq = getValueInt("h_r001_report_seq");
        }

    }

    /***********************************************************************/
    void selectBilCurpost() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "bill_type,";
        sqlCmd += "txn_code,";
        sqlCmd += "card_no,";
        sqlCmd += "film_no,";
        sqlCmd += "purchase_date,";
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
        sqlCmd += "process_date,";
        sqlCmd += "mcht_no,";
        sqlCmd += "mcht_chi_name,";
        sqlCmd += "contract_no,";
        sqlCmd += "term,";
        sqlCmd += "total_term,";
        sqlCmd += "acct_eng_short_name,";
        sqlCmd += "acct_chi_short_name,";
        sqlCmd += "rsk_type,";
        sqlCmd += "doubt_type,";
        sqlCmd += "acct_type,";
        sqlCmd += "curr_code,";
        sqlCmd += "promote_dept,";
        sqlCmd += "prod_no,";
        sqlCmd += "group_code,";
        sqlCmd += "bin_type,";
        sqlCmd += "acno_p_seqno,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "this_close_date,";
        sqlCmd += "batch_no,";
        sqlCmd += "rsk_type,";
        sqlCmd += "reference_no ";
        sqlCmd += "from bil_curpost ";
        sqlCmd += "where rsk_type='1' ";
        sqlCmd += "and decode(curr_post_flag,'','N',curr_post_flag) != 'Y' ";
        sqlCmd += "order by batch_no,card_no ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCurpBillType = getValue("bill_type", i);
            hCurpTxnCode = getValue("txn_code", i);
            hCurpCardNo = getValue("card_no", i);
            hCurpFilmNo = getValue("film_no", i);
            hCurpPurchaseDate = getValue("purchase_date", i);
            hCurpDestAmt = getValueDouble("dest_amt", i);
            hCurpDestCurr = getValue("dest_curr", i);
            hCurpSourceAmt = getValueDouble("source_amt", i);
            hCurpSourceCurr = getValue("source_curr", i);
            hCurpMchtEngName = getValue("mcht_eng_name", i);
            hCurpMchtCity = getValue("mcht_city", i);
            hCurpMchtCountry = getValue("mcht_country", i);
            hCurpMchtCategory = getValue("mcht_category", i);
            hCurpMchtZip = getValue("mcht_zip", i);
            hCurpMchtState = getValue("mcht_state", i);
            hCurpAuthCode = getValue("auth_code", i);
            hCurpProcessDate = getValue("process_date", i);
            hCurpMchtNo = getValue("mcht_no", i);
            hCurpMchtChiName = getValue("mcht_chi_name", i);
            hCurpContractNo = getValue("contract_no", i);
            hCurpTerm = getValueInt("term", i);
            hCurpTotalTerm = getValueInt("total_term", i);
            hCurpAcctEngShortName = getValue("acct_eng_short_name", i);
            hCurpAcctChiShortName = getValue("acct_chi_short_name", i);
            hCurpRskType = getValue("rsk_type", i);
            hCurpDoubtType = getValue("doubt_type", i);
            hCurpAcctType = getValue("acct_type", i);
            hCurpCurrCode = getValue("curr_code", i);
            hCurpPromoteDept = getValue("promote_dept", i);
            hCurpProdNo = getValue("prod_no", i);
            hCurpGroupCode = getValue("group_code", i);
            hCurpBinType = getValue("bin_type", i);
            hCurpPSeqno = getValue("acno_p_seqno", i);
            hCurpIdPSeqno = getValue("id_p_seqno", i);
            hCurpThisCloseDate = getValue("this_close_date", i);
            hCurpBatchNo = getValue("batch_no", i);
            hCurpReferenceNo = getValue("reference_no", i);

            totalCnt++;

            if (indexCnt == 0) {
                tempBatchNo = hCurpBatchNo;
                printHeader();
            }

            if (tempBatchNo.equals(hCurpBatchNo) == false) {
                printFooter();
                //分頁控制
                lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
                tempBatchNo = hCurpBatchNo;
            }
            if (indexCnt > 12) {
            	//分頁控制
                lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
                tempBatchNo = hCurpBatchNo;
            }

            printDetail();
        }

        if (indexCnt != 0)
            printFooter();

    }

    /***********************************************************************/
    void printDetail() {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, hCurpCardNo, 1);
        buf = comcr.insertStr(buf, hCurpReferenceNo, 18);
        buf = comcr.insertStr(buf, hCurpFilmNo, 31);
        buf = comcr.insertStr(buf, comc.getSubString(hCurpPurchaseDate, 4), 58);
        buf = comcr.insertStr(buf, hCurpTxnCode, 70);
        szTmp = comcr.commFormat("3$,3$,3$.2$", hCurpDestAmt);
        buf = comcr.insertStr(buf, szTmp, 76);
        szTmp = comcr.commFormat("3$,3$,3$.2$", hCurpSourceAmt);
        buf = comcr.insertStr(buf, szTmp, 96);
        buf = comcr.insertStr(buf, hCurpSourceCurr, 113);
        buf = comcr.insertStr(buf, hCurpAuthCode, 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

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
        buf = comcr.insertStr(buf, hCurpMchtCity, 59);
        buf = comcr.insertStr(buf, hCurpMchtCountry, 85);
        buf = comcr.insertStr(buf, hCurpMchtCategory, 100);
        buf = comcr.insertStr(buf, hCurpRskType, 128);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printHeader() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptId, 1);
        buf = comcr.insertStrCenter(buf, "信用卡  不合格帳單明細表", 132);
        buf = comcr.insertStr(buf, "頁次:", 110);
        szTmp = String.format("%4d", pageCnt);
        buf = comcr.insertStr(buf, szTmp, 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "印表日 :", 1);
        buf = comcr.insertStr(buf, chinDate, 10);
        buf = comcr.insertStr(buf, "入帳日 :", 90);
        szTmp = String.format("%07d", comcr.str2long(hCurpThisCloseDate) - 19110000);
        buf = comcr.insertStr(buf, szTmp, 100);
        buf = comcr.insertStr(buf, "批號:", 110);
        buf = comcr.insertStr(buf, hCurpBatchNo, 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "卡            號", 1);
        buf = comcr.insertStr(buf, "參考號碼", 21);
        buf = comcr.insertStr(buf, "微   縮   影   號   碼", 30);
        buf = comcr.insertStr(buf, "消費日期", 56);
        buf = comcr.insertStr(buf, "代碼", 68);
        buf = comcr.insertStr(buf, "消費金額(台幣)", 76);
        buf = comcr.insertStr(buf, "消費金額(原幣)", 96);
        buf = comcr.insertStr(buf, "/幣別", 112);
        buf = comcr.insertStr(buf, "授權碼", 118);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "商店代號", 1);
        buf = comcr.insertStr(buf, "團代", 15);
        buf = comcr.insertStr(buf, "商  店  名  稱", 21);
        buf = comcr.insertStr(buf, "商店所在地城市", 59);
        buf = comcr.insertStr(buf, "商店所在地國別", 85);
        buf = comcr.insertStr(buf, "商店類別", 100);
        buf = comcr.insertStr(buf, "風管疑異", 126);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
    }

    /***********************************************************************/
    void printFooter() {
        buf = "";
        buf = comcr.insertStr(buf, "正值:筆數:", 1);
        szTmp = String.format("%6d", pagePositiveCnt);
        buf = comcr.insertStr(buf, szTmp, 13);
        buf = comcr.insertStr(buf, "台幣金額 :", 25);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", pagePDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "負值:筆數:", 1);
        szTmp = String.format("%6d", pageNegativeCnt);
        buf = comcr.insertStr(buf, szTmp, 13);
        buf = comcr.insertStr(buf, "台幣金額 :", 25);
        szTmp = comcr.commFormat("3$,3$,3$,3$.2$", pageNDestAmt);
        buf = comcr.insertStr(buf, szTmp, 40);
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", ""));

        pagePDestAmt = 0;
        pagePSourceAmt = 0;
        pageNDestAmt = 0;
        pageNSourceAmt = 0;
        pageNegativeCnt = 0;
        pagePositiveCnt = 0;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilR001 proc = new BilR001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
