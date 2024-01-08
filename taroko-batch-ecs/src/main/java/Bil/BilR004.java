/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  108/07/22  V1.00.01    David     BIL_R004R1改送至報表簽核系統並更名為CCDCD010 *
*  111/09/22  V1.00.02    shiyuqi   updated for project coding standard       *
*  112/12/05  V1.00.03    JeffKung  取消ID及姓名的隱碼 
******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*列印產生催收戶,分期付期戶新的請款報表程式*/
public class BilR004 extends AccessDAO {
    private String progname = "列印產生催收戶,分期付期戶新的請款報表程式  112/12/05 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilR004";
    String prgmName = "列印產生催收戶,分期付期戶新的請款報表程式";
    String rptName = "催收戶、呆帳戶、分期償還戶新請款報表";
    String rptId = "BIL_R004R1";
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    int rptSeq = 0;
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

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
    String hCurpTerm = "";
    String hCurpTotalTerm = "";
    // String h_curp_prod_name = "";
    String hCurpAcctEngShortName = "";
    String hCurpAcctChiShortName = "";
    String hCurpDoubtType = "";
    String hCurpAcctType = "";
    // String h_curp_acct_key = "";
    String hCurpAcctStatus = "";
    // String h_curp_block_status = "";
    // String h_curp_block_date = "";
    String hCurpPayByStageFlag = "";
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
    int hR001ReportSeq = 0;
    String hCurpUsageCode = "";
    String hCurpReasonCode = "";
    String hCurpGoodsName = "";
    String hCurpOriginalNo = "";
    String hCurpTelephoneNo = "";
    String hAcnoCreditActNo = "";
    String hAcnoIdPSeqno = "";
    String hIdnoId = "";
    String hIdnoChiName = "";
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
                comc.errExit("Usage : BilR004 batch_no_f batch_no_e", errMsg2);
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

            selectBilR001r1();
            selectBilCurpost();

            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "]"+ pageCnt);

            if (pageCnt > 0) {
                hSystemDateF = "";
                sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') as h_system_date_f from dual";
                if (selectTable() > 0) {
                    hSystemDateF = getValue("h_system_date_f");
                }

                String ftpName = String.format("%s.%s", rptName, chinDate);
                String filename = String.format("%s/reports/%s.%s", comc.getECSHOME(), rptName, chinDate);
                filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
                //改為線上報表
                //comc.writeReport(filename, lpar1);
                comcr.insertPtrBatchRpt(lpar1);
                
                //ftpMput(ftpName);
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
    void selectBilR001r1() throws Exception {
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
        sqlCmd += "b.bill_type,";
        sqlCmd += "b.txn_code,";
        sqlCmd += "b.card_no,";
        sqlCmd += "b.film_no,";
        sqlCmd += "b.purchase_date,";
        sqlCmd += "b.dest_amt,";
        sqlCmd += "b.dest_curr,";
        sqlCmd += "b.source_amt,";
        sqlCmd += "b.source_curr,";
        sqlCmd += "b.mcht_eng_name,";
        sqlCmd += "b.mcht_city,";
        sqlCmd += "b.mcht_country,";
        sqlCmd += "b.mcht_category,";
        sqlCmd += "b.mcht_zip,";
        sqlCmd += "b.mcht_state,";
        // sqlCmd += "usage_code,";
        // sqlCmd += "reason_code,";
        sqlCmd += "b.auth_code,";
        sqlCmd += "b.process_date,";
        sqlCmd += "b.mcht_no,";
        sqlCmd += "b.mcht_chi_name,";
        sqlCmd += "b.contract_no,";
        // sqlCmd += "goods_name,";
        // sqlCmd += "original_no,";
        // sqlCmd += "telephone_no,";
        sqlCmd += "b.term,";
        sqlCmd += "b.total_term,";
        // sqlCmd += "prod_name,";
        sqlCmd += "b.acct_eng_short_name,";
        sqlCmd += "b.acct_chi_short_name,";
        sqlCmd += "b.doubt_type,";
        sqlCmd += "b.acct_type,";
        // sqlCmd += "acct_key,";
        sqlCmd += "a.acct_status,";
        // sqlCmd += "block_status,";
        // sqlCmd += "block_date,";
        sqlCmd += "a.pay_by_stage_flag,";
        // sqlCmd += "autopay_acct_no,";
        sqlCmd += "b.curr_code,";
        // sqlCmd += "oppost_date,";
        sqlCmd += "b.promote_dept,";
        sqlCmd += "b.prod_no,";
        sqlCmd += "b.group_code,";
        sqlCmd += "b.bin_type,";
        sqlCmd += "b.acno_p_seqno,";
        sqlCmd += "b.id_p_seqno,";
        sqlCmd += "b.this_close_date,";
        sqlCmd += "b.batch_no,";
        sqlCmd += "b.reference_no ";
        sqlCmd += "from bil_curpost b ";
        sqlCmd += " join act_acno a on  a.acno_p_seqno=b.acno_p_seqno ";
        sqlCmd += " where 1=1 ";
        sqlCmd += "and (decode(a.acct_status,'','N',a.acct_status) in ('3','4') ";
        sqlCmd += "or (decode(a.pay_by_stage_flag,'','N',a.pay_by_stage_flag) != 'N' and ";
        sqlCmd += "decode(a.pay_by_stage_flag,'','N',a.pay_by_stage_flag) != '00' ) ) ";
        sqlCmd += "and decode(b.curr_post_flag,'','N',b.curr_post_flag) != 'Y' ";
        sqlCmd += "and b.contract_flag = 'P' ";
        sqlCmd += "and b.acct_code in ('BL','CA','IT','ID','AO') ";
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
            // h_curp_usage_code = getValue("usage_code", i);
            // h_curp_reason_code = getValue("reason_code", i);
            hCurpAuthCode = getValue("auth_code", i);
            hCurpProcessDate = getValue("process_date", i);
            hCurpMchtNo = getValue("mcht_no", i);
            hCurpMchtChiName = getValue("mcht_chi_name", i);
            hCurpContractNo = getValue("contract_no", i);
            // h_curp_goods_name = getValue("goods_name", i);
            // h_curp_original_no = getValue("original_no", i);
            // h_curp_telephone_no = getValue("telephone_no", i);
            hCurpTerm = getValue("term", i);
            hCurpTotalTerm = getValue("total_term", i);
            // h_curp_prod_name = getValue("prod_name", i);
            hCurpAcctEngShortName = getValue("acct_eng_short_name", i);
            hCurpAcctChiShortName = getValue("acct_chi_short_name", i);
            hCurpDoubtType = getValue("doubt_type", i);
            hCurpAcctType = getValue("acct_type", i);
            // h_curp_acct_key = getValue("acct_key", i);
            hCurpAcctStatus = getValue("acct_status", i);
            // h_curp_block_status = getValue("block_status", i);
            // h_curp_block_date = getValue("block_date", i);
            hCurpPayByStageFlag = getValue("pay_by_stage_flag", i);
            // h_curp_autopay_acct_no = getValue("autopay_acct_no", i);
            hCurpCurrCode = getValue("curr_code", i);
            // h_curp_oppost_date = getValue("oppost_date", i);
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

            if (!tempBatchNo.equals(hCurpBatchNo)) {
                printFooter();
                //分頁控制
                lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
                printHeader();
                indexCnt = 0;
                tempBatchNo = hCurpBatchNo;
            }
            if (indexCnt > 28) {
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
    void printHeader() {
        pageCnt++;
        buf = "";
        buf = comcr.insertStr(buf, rptId, 1);
        buf = comcr.insertStrCenter(buf, "催收戶,呆帳戶,分期還款戶新的請款表", 132);
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
        buf = comcr.insertStr(buf, "授信帳碼", 21);
        buf = comcr.insertStr(buf, "身分證號碼", 30);
        buf = comcr.insertStr(buf, "姓      名", 41);
        buf = comcr.insertStr(buf, "參考號碼", 52);
        buf = comcr.insertStr(buf, "消費日期", 63);
        buf = comcr.insertStr(buf, "代碼", 72);
        buf = comcr.insertStr(buf, "消費金額(台幣)", 77);
        buf = comcr.insertStr(buf, "消費金額(原幣)", 97);
        buf = comcr.insertStr(buf, "/幣別", 113);
        buf = comcr.insertStr(buf, "分期數", 119);
        buf = comcr.insertStr(buf, "催/呆", 126);
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

        pagePDestAmt = 0;
        pagePSourceAmt = 0;
        pageNDestAmt = 0;
        pageNSourceAmt = 0;
        pageNegativeCnt = 0;
        pagePositiveCnt = 0;
    }

    /***********************************************************************/
    void printDetail() throws Exception {
        lineCnt++;
        indexCnt++;

        buf = "";
        buf = comcr.insertStr(buf, hCurpCardNo, 1);

        hAcnoCreditActNo = "";
        hAcnoIdPSeqno = "";
        sqlCmd = "select credit_act_no,";
        sqlCmd += "id_p_seqno ";
        sqlCmd += " from act_acno  ";
        sqlCmd += "where acno_p_seqno  = ? ";
        setString(1, hCurpPSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcnoCreditActNo = getValue("credit_act_no");
            hAcnoIdPSeqno = getValue("id_p_seqno");
        }

        hIdnoId = "";
        hIdnoChiName = "";
        
        /*取消ID及姓名的隱碼 (20231205)
        sqlCmd = "select uf_hi_idno(id_no) as h_idno_id, ";
        sqlCmd += " uf_hi_cname(chi_name) h_idno_chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += " where id_p_seqno  = ? ";
        */
        
        sqlCmd = "select id_no as h_idno_id, ";
        sqlCmd += " chi_name as h_idno_chi_name ";
        sqlCmd += " from crd_idno  ";
        sqlCmd += " where id_p_seqno  = ? ";
        setString(1, hAcnoIdPSeqno);
        recordCnt = selectTable();
        if (recordCnt > 0) {
            hIdnoId = getValue("h_idno_id");
            hIdnoChiName = getValue("h_idno_chi_name");
        }

        buf = comcr.insertStr(buf, hAcnoCreditActNo, 21);
        buf = comcr.insertStr(buf, hIdnoId, 30);
        buf = comcr.insertStr(buf, hIdnoChiName, 41);
        buf = comcr.insertStr(buf, hCurpReferenceNo, 52);
        buf = comcr.insertStr(buf, comc.getSubString(hCurpPurchaseDate,4), 63);
        buf = comcr.insertStr(buf, hCurpTxnCode, 72);
        szTmp = comcr.commFormat("3$,3$,3$.2$", hCurpDestAmt);
        buf = comcr.insertStr(buf, szTmp, 77);
        szTmp = comcr.commFormat("3$,3$,3$.2$", hCurpSourceAmt);
        buf = comcr.insertStr(buf, szTmp, 97);
        buf = comcr.insertStr(buf, hCurpSourceCurr, 114);
        buf = comcr.insertStr(buf, hCurpTerm, 119);
        
        if (hCurpAcctStatus.equals("3")) {
            buf = comcr.insertStr(buf, "催", 127);
        } else if (hCurpAcctStatus.equals("4")) {
            buf = comcr.insertStr(buf, "呆", 127);
        } else {
        	buf = comcr.insertStr(buf, hCurpPayByStageFlag, 127);
        }
        
        lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        if ((!hCurpTxnCode.equals("06")) && (!hCurpTxnCode.equals("17")) && (!hCurpTxnCode.equals("25"))
                && (!hCurpTxnCode.equals("26")) && (!hCurpTxnCode.equals("27")) && (!hCurpTxnCode.equals("28"))
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

    }
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "RPQS_FTP"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "RPQS_FTP";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilR004 proc = new BilR004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
