/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE      Version    AUTHOR      DESCRIPTION                           *
 *   ---------   --------  ----------  -------------------------------------- *
 *   110-08-11   V1.00.01   Bo yang    initial                            *
 *   111-07-05   V1.00.02   Zuwei.su   刪除table【MKT_PROMOTE】改用【MKT_OFFICE_D】              *
 *   112-08-15   V1.00.03   Zuwei Su   增查詢條件: 機構統編(選單式)，推廣單位:調整字串” 推廣單位統編” ，fix bug             *
 ******************************************************************************/
package mktq01;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoFileAccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mktq0150 extends BaseAction implements InfaceExcel {
    taroko.base.CommDate commDate = new taroko.base.CommDate();

    @Override
    public void userAction() throws Exception {
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "Q1")) {
            /* detl页查詢功能 */
            strAction = "Q1";
            queryRead1();
        } else if (eqIgno(wp.buttonCode, "R")) {
            // -資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {
            /* 新增功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
            saveFunc();
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
        } else if (eqIgno(wp.buttonCode, "XLS")) {
            // -EXCEL-
            strAction = "XLS";
            xlsPrint();
        }

    }

    @Override
    public void dddwSelect() {
        try {
            // if ((wp.respHtml.equals("mktq0150"))) {
            wp.initOption = "--";
            wp.optionKey = "";
            if (wp.colStr("ex_corp_no").length() > 0) {
                wp.optionKey = wp.colStr("ex_corp_no");
            }
            if (wp.colStr("kk_corp_no").length() > 0) {
                wp.optionKey = wp.colStr("kk_corp_no");
            }
            this.dddwList("dddw_mkt_office_m", "mkt_office_m", "corp_no", "office_m_name",
                    "where 1=1 order by corp_no");
            // }
        } catch (Exception ex) {
        }
    }

    @Override
    public void queryFunc() throws Exception {
        wp.setQueryMode();

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        String exCorpNo = "ex_corp_no";
        String exStaticMonth1 = "ex_static_month1";
        String exStaticMonth2 = "ex_static_month2";
        String exPromoteDept = "ex_promote_dept";

        if (wp.itemEmpty(exStaticMonth1) && wp.itemEmpty(exStaticMonth2)) {
            alertErr2("查詢年月起迄不可全部空白");
            return;
        }

        if (!this.chkStrend(wp.itemStr(exStaticMonth1), wp.itemStr(exStaticMonth2))) {
            alertErr2("查詢年月起迄格式錯誤");
            return;
        }

        wp.pageControl();

        LocalDateTime now = LocalDateTime.now();
        String acctDate1 = now.format(DateTimeFormatter.ofPattern("yyyy")) + "01";
        String acctDate2 = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

        wp.sqlCmd = "select a.promote_dept, b.office_name as promote_name, count(*) as issue_cnt,"
                + " sum(case when a.oppost_date != '' then 1 else 0 end) as oppost_cnt,"
                + " sum(case when a.activate_flag = 2 then 1 else 0 end) as activate_cnt,"
                + " sum(case when a.circulate_cnt = 1 then 1 else 0 end) as circulate_cnt,"
                + " sum(case when a.valid_cnt = 1 then 1 else 0 end) as valid_cnt,"
                + " sum(a.amt_bl + a.amt_ca + a.amt_it + a.amt_id + a.amt_ot + a.amt_ao) as signed_amount,"
                + " (select sum(a1.amt_bl + a1.amt_ca + a1.amt_it + a1.amt_id + a1.amt_ot + a1.amt_ao) from mkt_issue_reward a1"
                + " inner join mkt_card_consume b on a1.card_no = b.card_no "
                + sqlCol(acctDate1, "b.acct_month", " >=") + sqlCol(acctDate2, "b.acct_month", " <=")
                + " where a1.promote_dept = a.promote_dept) as total_signed_amount "
                + " from mkt_issue_reward as a"
                + " left join mkt_office_d as b on a.promote_dept = b.office_code and b.apr_flag = 'Y' "
                + " where 1=1 "
                + sqlCol(wp.itemStr(exStaticMonth1), "a.static_month", ">=")
                + sqlCol(wp.itemStr(exStaticMonth2), "a.static_month", "<=")
                + sqlCol(wp.itemStr(exPromoteDept), "a.promote_dept")
                + sqlCol(wp.itemStr(exCorpNo), "b.corp_no")
                + " group by a.promote_dept, b.office_name order by 1 desc ";

        wp.pageCountSql = "select count(1) from (" + wp.sqlCmd + ")";
        logSql();
        pageQuery();

        wp.setListCount(1);
        if (sqlRowNum <= 0) {
            alertErr2("此條件查無資料");
            return;
        }
        wp.setPageValue();
    }

    public void queryRead1() throws Exception {
        dataRead();
//        String staticMonth1 = "kk_static_month1";
//        String staticMonth2 = "kk_static_month2";
//        String promoteDept = "kk_promote_dept";
//
//        if (wp.itemEmpty(staticMonth1) && wp.itemEmpty(staticMonth2)) {
//            alertErr2("查詢年月起迄不可全部空白");
//            return;
//        }
//
//        if (!this.chkStrend(wp.itemStr(staticMonth1), wp.itemStr(staticMonth2))) {
//            alertErr2("查詢年月起迄格式錯誤");
//            return;
//        }
//
//        if (wp.itemEmpty(promoteDept)) {
//            alertErr2("推廣單位不可空白");
//            return;
//        }
//
//        wp.pageControl();
//
//        LocalDateTime now = LocalDateTime.now();
//        String acctDate1 = now.format(DateTimeFormatter.ofPattern("yyyy")) + "01";
//        String acctDate2 = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
//
//        wp.sqlCmd = "select a.static_month, a.promote_dept, b.office_name as promote_name, a.introduce_emp_no, a.card_no, a.chi_name,"
//                + " a.issue_date, (c.group_code||'_'||c.group_name) as group_code, a.first_purchase_date, a.last_purchase_date,"
//                + " (a.amt_bl + a.amt_ca + a.amt_it + a.amt_id + a.amt_ot + a.amt_ao) as total_signed_amount "
//                + " from mkt_issue_reward as a"
//                + " inner join mkt_office_d as b on a.promote_dept = b.office_code and b.apr_flag = 'Y' and b.corp_no = '53021481' "
//                + " inner join ptr_group_code as c on a.group_code = c.group_code"
//                + " inner join mkt_card_consume as d on a.card_no = d.card_no"
//                + " where 1=1 "
//                + sqlCol(wp.itemStr(staticMonth1), "a.static_month", ">=")
//                + sqlCol(wp.itemStr(staticMonth2), "a.static_month", "<=")
//                + sqlCol(wp.itemStr(promoteDept), "a.promote_dept")
//                + sqlCol(acctDate1, "d.acct_month", " >=") + sqlCol(acctDate2, "d.acct_month", " <=")
//                + " order by 1 desc ";
//
//        wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";
//        logSql();
//        pageQuery();
//
//        wp.setListCount(1);
//        if (sqlRowNum <= 0) {
//            alertErr2("此條件查無資料");
//            return;
//        }
//        wp.setPageValue();
    }

    @Override
    public void querySelect() throws Exception {
        wp.itemSet("kk_promote_dept", wp.itemStr("data_k1"));
        wp.itemSet("kk_static_month1", wp.itemStr("data_k2"));
        wp.itemSet("kk_static_month2", wp.itemStr("data_k3"));
        wp.itemSet("kk_corp_no", wp.itemStr("data_k4"));
        wp.colSet("kk_promote_dept", wp.itemStr("data_k1"));
        wp.colSet("kk_static_month1", wp.itemStr("data_k2"));
        wp.colSet("kk_static_month2", wp.itemStr("data_k3"));
        wp.colSet("kk_corp_no", wp.itemStr("data_k4"));
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        wp.pageControl();

//        String promoteDept = "data_k1";

//        LocalDateTime now = LocalDateTime.now();
//        String acctDate1 = now.format(DateTimeFormatter.ofPattern("yyyy")) + "01";
//        String acctDate2 = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

        wp.sqlCmd = "select a.static_month, a.promote_dept, b.office_name as promote_name, a.introduce_emp_no, a.card_no, a.chi_name,"
                + " a.issue_date, (c.group_code||'_'||c.group_name) as group_code, a.first_purchase_date, a.last_purchase_date,"
                + " (a.amt_bl + a.amt_ca + a.amt_it + a.amt_id + a.amt_ot + a.amt_ao) as total_signed_amount "
                + " from mkt_issue_reward as a"
                + " left join mkt_office_d as b on a.promote_dept = b.office_code and b.apr_flag = 'Y' "
                + " left join ptr_group_code as c on a.group_code = c.group_code"
                + " left join mkt_card_consume as d on a.card_no = d.card_no"
                + " where 1=1 "
                + sqlCol(wp.itemStr("kk_promote_dept"), "a.promote_dept")
                + sqlCol(wp.itemStr("kk_static_month1"), "a.static_month", " >=") + sqlCol(wp.itemStr("kk_static_month2"), "a.static_month", " <=")
//              + sqlCol(acctDate1, "a.static_month", " >=") + sqlCol(acctDate2, "a.static_month", " <=")
                + sqlCol(wp.itemStr("kk_corp_no"), "b.corp_no")
                + " order by 1 desc ";

        wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";
        logSql();
        pageQuery();

        if (sqlRowNum <= 0) {
            alertErr2("此條件查無資料");
            return;
        }

        wp.setListCount(1);
        wp.setPageValue();
    }

    @Override
    public void saveFunc() throws Exception {
    }

    @Override
    public void procFunc() throws Exception {
    }

    @Override
    public void initButton() {
    }

    @Override
    public void initPage() {
    }

    @Override
    public void xlsPrint() throws Exception {
        log("csvFunction: started--------");

        String fileName = null;
        int file = 0;

        // 根據SER_NUM行數判斷是否有數據
        int rows = wp.itemRows("SER_NUM");
        if (rows <= 0) {
            alertErr2("無資料可列印");
            wp.respHtml = "TarokoErrorPDF";
            return;
        }
        wp.listCount[0] = rows;
        TarokoFileAccess tf = new TarokoFileAccess(wp);

        // 判斷頁面為主頁面 or detl頁面
        if (wp.itemEq("pageType", "cond")) {
            fileName = "mktq0150-" + commString.mid(commDate.sysDatetime(), 4) + ".csv";
            file = tf.openOutputText(fileName, "MS950");
            String lsData = "No,推廣單位,推廣單位名稱,期間發卡數,期間停卡數,流通卡數,開卡數,有效卡數,期間簽帳金額,本年度累計簽帳金額(已請款)";
            tf.writeTextFile(file, lsData + wp.newLine);

            for (int i = 0; i < rows; i++) {
                lsData = wp.itemStr(i, "SER_NUM") + "," + wp.itemStr(i, "promote_dept") + ","
                        + wp.itemStr(i, "promote_name") + "," + wp.itemStr(i, "issue_cnt") + ","
                        + wp.itemStr(i, "oppost_cnt") + "," + wp.itemStr(i, "circulate_cnt") + ","
                        + wp.itemStr(i, "activate_cnt") + "," + wp.itemStr(i, "valid_cnt") + ","
                        + wp.itemStr(i, "signed_amount") + "," + wp.itemStr(i, "total_signed_amount");
                tf.writeTextFile(file, lsData + wp.newLine);
            }
        } else if (wp.itemEq("pageType", "detl")) {
            fileName = "mktq0150_detl-" + commString.mid(commDate.sysDatetime(), 4) + ".csv";
            file = tf.openOutputText(fileName, "MS950");
            String lsData = "No,年月,推廣單位統編,推廣單位名稱,推廣企業員工ID,發卡卡號,持卡人姓名,發卡日期,團體代號,最早消費日,最晚消費日,本年度累計簽帳金額(已請款)";
            tf.writeTextFile(file, lsData + wp.newLine);

            for (int i = 0; i < rows; i++) {
                String chiName = wp.itemStr(i, "chi_name");
                String introduceEmpNo = wp.itemStr(i, "introduce_emp_no");

                lsData = wp.itemStr(i, "SER_NUM") + "," + wp.itemStr(i, "static_month") + ","
                        + wp.itemStr(i, "promote_dept") + "," + wp.itemStr(i, "promote_name") + ",";

                // 判斷推廣企業員工ID是否需要隱藏顯示
                if (introduceEmpNo.length() > 0) {
                    lsData += introduceEmpNo.replaceFirst("^[0-9]{6}", "******") + ",\t"
                            + wp.itemStr(i, "card_no") + ",";
                } else {
                    lsData += introduceEmpNo + ",\t" + wp.itemStr(i, "card_no") + ",";
                }

                // 判斷持卡人姓名是否需要隱藏顯示
                if (chiName.length() > 1) {
                    lsData += chiName.replaceAll("(?<=[\\w\\W])[^\\s]", "*") + ",";
                } else {
                    lsData += chiName + ",";
                }

                lsData += wp.itemStr(i, "issue_date") + "," + wp.itemStr(i, "group_code") + ","
                        + wp.itemStr(i, "first_purchase_date") + "," + wp.itemStr(i, "last_purchase_date") + ","
                        + wp.itemStr(i, "total_signed_amount");
                tf.writeTextFile(file, lsData + wp.newLine);
            }
        }

        tf.closeOutputText(file);
        wp.setDownload(fileName);
    }

    @Override
    public void logOnlineApprove() throws Exception {
    }
}
