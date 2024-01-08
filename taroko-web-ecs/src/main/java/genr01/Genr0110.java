/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 112-03-14  V1.00.00  Jiang Yingdong   program init                         *
 * 112-03-17  V1.00.01  Jiang Yingdong   error fix                            *
 ******************************************************************************/
package genr01;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDF2;

import java.io.InputStream;

public class Genr0110 extends BaseReport {

    InputStream inExcelFile = null;
    String mProgName = "genr0110";
    int sun = 0;

    String condWhere = "";

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;

        strAction = wp.buttonCode;
        log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml="
                + wp.respHtml);
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            // is_action="new";
            // clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page */
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
            strAction = "XLS";
            // wp.setExcelMode();
            xlsPrint();
        } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
            strAction = "PDF";
            // wp.setExcelMode();
            pdfPrint();
        } else if (eqIgno(wp.buttonCode, "PDF2")) { // -PDF-
            strAction = "PDF2";
            pdfPrint();
        }

        dddwSelect();
//    initButton();
    }

    @Override
    public void clearFunc() throws Exception {
        wp.resetInputData();
        wp.resetOutputData();
    }

    private boolean getWhereStr() throws Exception {
        String acNo1 = wp.itemStr("ex_ac_no_1");
        String acNo = wp.itemStr("ac_no_2");
        String txDateS = wp.itemStr("tx_date_s");
        String txDateE = wp.itemStr("tx_date_e");

        String lsWhere = " where 1=1";

        if (empty(acNo1) == false) {
            lsWhere += " and v.ac_no like :ac_no_1||'%'";
            setString("ac_no_1", acNo1);
        }
        if (empty(acNo) == false) {
            lsWhere += " and v.ac_no = :ac_no";
            setString("ac_no", acNo);
        }
        if (empty(txDateS) == false) {
			lsWhere += " and v.tx_date >= :tx_date_s";
			setString("tx_date_s", txDateS);
		}
        if (empty(txDateE) == false) {
			lsWhere += " and v.tx_date <= :tx_date_e";
			setString("tx_date_e", txDateE);
		}
        lsWhere += " group by v.tx_date, v.ac_no, j.ac_full_name";
        wp.whereStr = lsWhere;
        setParameter();
        return true;
    }

    @Override
    public void queryFunc() throws Exception {
        if (getWhereStr() == false)
            return;
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryRead();
    }

    public void queryExcel() throws Exception {
        if (getWhereStr() == false)
            return;
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryReadExcel();
    }

    public void queryPdf() throws Exception {
        if (getWhereStr() == false)
            return;
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();
        queryReadPdf();
    }

    private void setParameter() throws Exception {


    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        if (getWhereStr() == false)
            return;
        wp.selectSQL = "v.tx_date"
                + ", decode(substr(v.ac_no, 1, 1), '1', '資產', '2', '負債', '4', '收入', '5', '費用') ac_no_1"
                + ", j.ac_full_name"
                + ", sum(case when v.dbcr='D' then v.amt else 0 end) amt_D"
                + ", sum(case when v.dbcr='C' then v.amt else 0 end) amt_C"
                + ", v.ac_no";
		wp.daoTable = "gen_vouch_h v "
                + " left join ptr_currcode p on v.curr=p.curr_code_gl"
                + " left join gen_acct_m j on v.ac_no=j.ac_no";
        wp.whereOrder = " order by v.tx_date";

        wp.pageCountSql = String.format("select count(*) from (select %s from %s %s %s)", wp.selectSQL, wp.daoTable, wp.whereStr, wp.whereOrder);

        if (strAction.equals("XLS")) {
            selectNoLimit();
        }
        pageQuery();
        wp.setListCount(1);
//        sun = wp.selectCnt;
        if (sqlRowNum <= 0) {
            alertErr2("此條件查無資料");
            return;
        }
//        for (int ii = 0; ii < sun; ii++) {
//            if (wp.getValue("ac_no", ii).equals("10000000")) {
//                wp.setValue("ac_brief_name", " 庫存現金 ", ii);
//            }
//
//            if (wp.getValue("dbcr", ii).equals("貸")) {
//                String indent = "__";
//                wp.colSet(ii, "indent", indent);
//
//                String amtC = wp.getValue("amt", ii);
//                wp.setValue("amt_C", amtC, ii);
//            }
//            if (wp.getValue("dbcr", ii).equals("借")) {
//                String amtD = wp.getValue("amt", ii);
//                wp.setValue("amt_D", amtD, ii);
//            }
//
//        }

        wp.listCount[1] = wp.dataCnt;
        wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
        wp.setPageValue();
    }


    public void queryReadPdf() throws Exception {
        wp.pageControl();

        if (getWhereStr() == false)
            return;

        wp.selectSQL = "" + "v.tx_date , " + "v.refno , " + " brno, "
                + "p.curr_chi_name , "
                + "decode(v.dbcr,'D','借','C','貸','') as dbcr, "
                + "decode(v.dbcr,'D','轉帳支出','C','轉帳收入','') as cond_2, "
                + "v.dbcr as dbcc, "
                + "v.ac_no, " + "j.ac_brief_name, " + "v.sign_flag , " + "v.amt , "
                + "v.memo1 , " + " v.ias24_id, " + " v.acct_no, " + " v.acct_name, " + "v.mod_user,  " + "v.mod_pgm  "
        ;

        if (wp.itemStr("post_flag").equals("Y")) {
            wp.daoTable = "gen_vouch_h v left join ptr_currcode p on v.curr=p.curr_code_gl "
                    + "left join gen_acct_m J on v.ac_no=j.ac_no ";
        } else {
            wp.daoTable = "gen_vouch v left join ptr_currcode p on v.curr=p.curr_code_gl "
                    + "left join gen_acct_m J on v.ac_no=j.ac_no ";
        }

        wp.whereOrder = " order by v.tx_date,v.refno"
                + ",dbcc desc ,seqno";

        pageQuery();
        wp.setListCount(1);
        sun = wp.selectCnt;
        if (sqlRowNum <= 0) {
            alertErr2("此條件查無資料");
            return;
        }
        for (int ii = 0; ii < sun; ii++) {
            if (!empty(wp.getValue("acct_no", ii)) && !empty(wp.getValue("acct_name", ii))) {
                wp.colSet("acctNM", wp.getValue("acct_no", ii) + "-" + wp.getValue("acct_name", ii));
            }
            String dbcr = "";
            String refno = wp.getValue("refno", ii);
            String txDate = wp.getValue("tx_Date", ii);
            if (wp.getValue("dbcr", ii).equals("借")) {
                dbcr = "D";
            } else {
                dbcr = "C";
            }
            String[] acNo = SelectGenVouch(refno, dbcr, txDate);
            if (acNo.length == 1) {
                wp.setValue("ac_no_DC1", acNo[0], ii);
            } else if (acNo.length == 2) {
                wp.setValue("ac_no_DC1", acNo[0], ii);
                wp.setValue("ac_no_DC2", acNo[1], ii);
            } else if (acNo.length == 3) {
                wp.setValue("ac_no_DC1", acNo[0], ii);
                wp.setValue("ac_no_DC2", acNo[1], ii);
                wp.setValue("ac_no_DC3", acNo[2], ii);
            } else if (acNo.length >= 4) {
                wp.setValue("ac_no_DC1", acNo[0], ii);
                wp.setValue("ac_no_DC2", acNo[1], ii);
                wp.setValue("ac_no_DC3", acNo[2], ii);
                wp.setValue("ac_no_DC4", acNo[3], ii);
            }

            String txY = txDate.substring(0, 4);
            int txDateYY = Integer.parseInt(txY) - 1911;
            String txDateY = String.valueOf(txDateYY);
            wp.setValue("txDateY", txDateY, ii);
            String txDateM = txDate.substring(4, 6);
            wp.setValue("txDateM", txDateM, ii);
            String txDateD = txDate.substring(6, 8);
            wp.setValue("txDateD", txDateD, ii);

            if (wp.getValue("mod_pgm", ii).equals("genp0110") ||
                    wp.getValue("mod_pgm", ii).equals("genp0120")) {
                String userNameK = SelectSecUser(wp.getValue("mod_user", ii));
                wp.setValue("userNameK", userNameK, ii);
            } else {
                wp.setValue("userNameJ1", "中山", ii);
                wp.setValue("userNameJ2", "記帳", ii);
            }
            if (wp.getValue("mod_pgm", ii).equals("genp0120")) {
                String modUser = SelectSecUser(wp.getValue("mod_user", ii));
                wp.setValue("mod_user", modUser, ii);
            } else {
                wp.setValue("mod_user", "", ii);
            }

            if (!empty(wp.getValue("ias24_id", ii))) {
                wp.setValue("checkY", "V", ii);
                String IAS24 = wp.getValue("ias24_id", ii);
                wp.setValue("ias24_id", IAS24, ii);
            } else {
                wp.setValue("checkN", "V", ii);
            }
        }
        wp.listCount[1] = wp.dataCnt;
        wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
        wp.setPageValue();
    }

    private String SelectSecUser(String modUser) {
        String lsSql = " select usr_cname from sec_user where usr_id = ? ";
        sqlSelect(lsSql, new Object[]{modUser});

        return sqlStr("usr_cname");
    }

    private String[] SelectGenVouch(String refno, String dbcr, String txdate) {
        String lsSql = " select ac_no from gen_vouch where "
                + "refno = ?  and dbcr <> ? "
                + "and tx_date = ? ";
        sqlSelect(lsSql, new Object[]{refno, dbcr, txdate});

        String[] acNo = new String[sqlRowNum];
        for (int i = 0; i < sqlRowNum; i++) {
            acNo[i] = sqlStr(i, "ac_no");
        }
        return acNo;
    }

    public void queryReadExcel() throws Exception {
        wp.pageControl();

        if (getWhereStr() == false)
            return;

        wp.selectSQL = "" + "v.tx_date , " + "v.refno , " + " brno, "
                + "p.curr_chi_name , "
                + "decode(v.dbcr,'D','借','C','貸','') as dbcr, "
                + "v.dbcr as dbcc, "
                + "decode(v.dbcr,'D',v.amt) amt_D, "
                + "decode(v.dbcr,'C',v.amt) amt_C, "
                + "v.ac_no, " + "j.ac_brief_name, "
                + "v.memo1 , " + " v.ias24_id, " + " v.acct_no, " + " v.acct_name, " + "v.mod_pgm  "
        ;

        if (wp.itemStr("post_flag").equals("Y")) {
            wp.daoTable = "gen_vouch_h v left join ptr_currcode p on v.curr=p.curr_code_gl "
                    + "left join gen_acct_m J on v.ac_no=j.ac_no ";
        } else {
            wp.daoTable = "gen_vouch v left join ptr_currcode p on v.curr=p.curr_code_gl "
                    + "left join gen_acct_m J on v.ac_no=j.ac_no ";
        }

        wp.whereOrder = " order by v.tx_date,v.refno"
                + ",dbcc desc ,seqno";

        if (strAction.equals("XLS")) {
            selectNoLimit();
        }
        pageQuery();
        wp.setListCount(1);
        sun = wp.selectCnt;
        if (sqlRowNum <= 0) {
            alertErr2("此條件查無資料");
            return;
        }
        for (int ii = 0; ii < sun; ii++) {
            if (!wp.getValue("amt_C", ii).equals("0") && wp.getValue("amt_D", ii).equals("0")) {
                wp.setValue("amt_D", " ", ii);
            } else {
                wp.setValue("amt_C", " ", ii);
            }
            if (wp.getValue("ac_no", ii).equals("10000000")) {
                wp.setValue("ac_brief_name", " 庫存現金 ", ii);
            }
            if (wp.getValue("dbcr", ii).equals("貸")) {
                wp.colSet(ii, "ac_brief_name", "__" + wp.getValue("ac_brief_name", ii));
            }

        }
        wp.listCount[1] = wp.dataCnt;
        wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
        wp.setPageValue();
    }

    void xlsPrint() {
        try {
            log("xlsFunction: started--------");
            wp.reportId = mProgName;

            // ===================================
            TarokoExcel xlsx = new TarokoExcel();
            wp.fileMode = "N";
            xlsx.excelTemplate = "genr0110_excel.xlsx";
            xlsx.sheetName[0] = "明細";
            wp.pageRows = 99999;
            queryRead();
            wp.setListCount(1);
            log("Detl: rowcnt:" + wp.listCount[0]);
            xlsx.processExcelSheet(wp);
            xlsx.outputExcel();
            xlsx = null;
            log("xlsFunction: ended-------------");

        } catch (Exception ex) {
            wp.expMethod = "xlsPrint";
            wp.expHandle(ex);
        }
    }

    void pdfPrint() throws Exception {
        if (eqIgno(strAction, "PDF")) {
            wp.reportId = "Genr0110_明細";
            wp.pageRows = 9999;
            queryExcel();
            wp.setListCount(1);
            TarokoPDF pdf = new TarokoPDF();
            wp.fileMode = "N";
            pdf.excelTemplate = "genr0110_excel.xlsx";
            pdf.sheetNo = 0;
            pdf.procesPDFreport(wp);
            pdf = null;
            return;
        } else if (eqIgno(strAction, "PDF2")) {
            wp.reportId = "Genr0110_傳票";
            wp.pageRows = 9999;
            queryPdf();
// 	    wp.setListCount(1);
            TarokoPDF2 pdf = new TarokoPDF2();
// 	    pdf.fixHeader[1] = "cond_1";
            wp.fileMode = "Y";
            pdf.excelTemplate = "genr0110_pdf.xlsx";
            pdf.sheetNo = 0;
            pdf.pageCount = 1;
            pdf.procesPDFreport(wp);
            pdf = null;
            return;
        }
    }

    @Override
    public void querySelect() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void dddwSelect() {
        try {
        } catch (Exception ex) {
        }
    }

}
