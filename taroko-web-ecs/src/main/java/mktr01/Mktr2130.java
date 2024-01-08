/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE      Version    AUTHOR      DESCRIPTION                           *
 *   ---------   --------  ----------  -------------------------------------- *
 *   111-02-27   V1.00.01   jiangyingdong        initial                      *
 ******************************************************************************/
package mktr01;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;
import taroko.com.TarokoFileAccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Mktr2130 extends BaseAction implements InfaceExcel {
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

        dddwSelect();
    }

    @Override
    public void dddwSelect() {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("ex_mkt_member");
        try {
            this.dddwList("dddw_mkt_member", "MKT_MEMBER","trim(STAFF_BRANCH_NO)", "trim(MEMBER_NAME)", "where 1=1 order by STAFF_BRANCH_NO");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void queryFunc() throws Exception {
        wp.setQueryMode();

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        // 前端传过来的参数名
        String exIssueDate = "ex_issue_date"; // 發卡年月
        String exMktMember = "ex_mkt_member"; // 聯名機構
        String exPromoteDept = "ex_promote_dept"; // 推廣員工ID

        String optionalWhere = "";

        if (wp.itemEmpty(exIssueDate)) {
            alertErr2("查詢年月不可空白");
            return;
        } else {
            optionalWhere += sqlCol(wp.itemStr(exIssueDate), "substr(a.issue_date, 1, 6)", "=");
        }
        if (!wp.itemEmpty(exMktMember)) {
            optionalWhere += sqlCol(wp.itemStr(exMktMember), "b.staff_branch_no", "=");
        }
        if (!wp.itemEmpty(exPromoteDept)) {
            optionalWhere += sqlCol(wp.itemStr(exPromoteDept), "a.member_id", "=");
        }

        wp.pageControl();

        wp.sqlCmd = "select"
                + " b.staff_branch_no||'_'||member_name staff_branch_no_member_name"
                + " ,a.static_month"
                + " ,a.member_id"
                + " ,a.card_no"
                + " ,b.staff_branch_no"
                + " ,b.issue_date"
                + " ,decode(b.activate_flag, '1', 'N', '2', 'Y') activate_flag"
                + " ,decode(b.current_code, '0', '0.正常', '1', '1.一般停用', '2', '2.挂失', '3', '3.強停', '4', '4.其他', '5', '5.偽卡') current_code"
                + " ,c.id_no"
                + " ,c.chi_name"
                + " ,c.home_area_code1"
                + " ,c.home_tel_no1"
                + " ,c.home_area_code1||'_'||c.home_tel_no1 home_area_code1_tel_no1"
                + " ,c.cellar_phone"
                + " ,nvl(d.member_name, '') member_name"
                + " ,feedback_amt"
                + " ,(select nvl(sum(purchase_amt), 0) from mkt_member_cardlist where card_no = a.card_no and substr(issue_date, 1, 6) = substr(a.issue_date, 1, 6)) dest_amt"
                + " ,(select nvl(sum(purchase_amt), 0) from mkt_member_cardlist where card_no = a.card_no and substr(issue_date, 1, 4) = substr(a.issue_date, 1, 4)) year_dest_amt"
                + " from mkt_member_cardlist as a"
                + " left join crd_card as b on a.card_no = b.card_no"
                + " left join crd_idno as c on b.id_p_seqno = c.id_p_seqno"
                + " left join mkt_member as d on b.staff_branch_no = d.staff_branch_no"
                + " where 1=1"
                + " and substr(a.issue_date, 1, 6) = substr(a.static_month, 1, 6)"
                + optionalWhere
                + " order by a.staff_branch_no";

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
        String staticMonth1 = "kk_static_month1";
        String staticMonth2 = "kk_static_month2";
        String promoteDept = "kk_promote_dept";

        if (wp.itemEmpty(staticMonth1) && wp.itemEmpty(staticMonth2)) {
            alertErr2("查詢年月起迄不可全部空白");
            return;
        }

        if (!this.chkStrend(wp.itemStr(staticMonth1), wp.itemStr(staticMonth2))) {
            alertErr2("查詢年月起迄格式錯誤");
            return;
        }

        if (wp.itemEmpty(promoteDept)) {
            alertErr2("推廣單位不可空白");
            return;
        }

        wp.pageControl();

        LocalDateTime now = LocalDateTime.now();
        String acctDate1 = now.format(DateTimeFormatter.ofPattern("yyyy")) + "01";
        String acctDate2 = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

        wp.sqlCmd = "select a.static_month, a.promote_dept, b.office_name as promote_name, a.introduce_emp_no, a.card_no, a.chi_name,"
                + " a.issue_date, (c.group_code||'_'||c.group_name) as group_code, a.first_purchase_date, a.last_purchase_date,"
                + " (a.amt_bl + a.amt_ca + a.amt_it + a.amt_id + a.amt_ot + a.amt_ao) as total_signed_amount "
                + " from mkt_issue_reward as a"
                + " inner join mkt_office_d as b on a.promote_dept = b.office_code and b.apr_flag = 'Y' and b.corp_no = '53021481' "
                + " inner join ptr_group_code as c on a.group_code = c.group_code"
                + " inner join mkt_card_consume as d on a.card_no = d.card_no"
                + " where 1=1 "
                + sqlCol(wp.itemStr(staticMonth1), "a.static_month", ">=")
                + sqlCol(wp.itemStr(staticMonth2), "a.static_month", "<=")
                + sqlCol(wp.itemStr(promoteDept), "a.promote_dept")
                + sqlCol(acctDate1, "d.acct_month", " >=") + sqlCol(acctDate2, "d.acct_month", " <=")
                + " order by 1 desc ";

        wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";
        logSql();
        pageQuery();

        wp.setListCount(1);
        if (sqlRowNum <= 0) {
            alertErr2("此條件查無資料");
            return;
        }
        wp.setPageValue();
    }

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        wp.pageControl();

        String promoteDept = "data_k1";

        LocalDateTime now = LocalDateTime.now();
        String acctDate1 = now.format(DateTimeFormatter.ofPattern("yyyy")) + "01";
        String acctDate2 = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

        wp.sqlCmd = "select a.static_month, a.promote_dept, b.office_name as promote_name, a.introduce_emp_no, a.card_no, a.chi_name,"
                + " a.issue_date, (c.group_code||'_'||c.group_name) as group_code, a.first_purchase_date, a.last_purchase_date,"
                + " (a.amt_bl + a.amt_ca + a.amt_it + a.amt_id + a.amt_ot + a.amt_ao) as total_signed_amount "
                + " from mkt_issue_reward as a"
                + " left join mkt_office_d as b on a.promote_dept = b.office_code and b.apr_flag = 'Y' and b.corp_no = '53021481' "
                + " inner join ptr_group_code as c on a.group_code = c.group_code"
                + " inner join mkt_card_consume as d on a.card_no = d.card_no"
                + " where 1=1 "
                + sqlCol(wp.itemStr(promoteDept), "a.promote_dept")
                + sqlCol(acctDate1, "d.acct_month", " >=") + sqlCol(acctDate2, "d.acct_month", " <=")
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

//        // 前端传过来的参数名
//        String exIssueDate = "ex_issue_date"; // 發卡年月
//        String exMktMember = "ex_mkt_member"; // 聯名機構
//        String exPromoteDept = "ex_promote_dept"; // 推廣員工ID
//
//        ArrayList<Object> params = new ArrayList<Object>();
//
//        String optionalWhere = "";
//
//        if (wp.itemEmpty(exIssueDate)) {
//            alertErr2("查詢年月不可空白");
//            return;
//        } else {
//            optionalWhere += " and substr(a.issue_date, 1, 6) = ?";
//            params.add(wp.itemStr(exIssueDate));
//        }
//        if (!wp.itemEmpty(exMktMember)) {
//            optionalWhere += " and b.staff_branch_no = ?";
//            params.add(wp.itemStr(exMktMember));
//        }
//        if (!wp.itemEmpty(exPromoteDept)) {
//            optionalWhere += " and a.member_id = ?";
//            params.add(wp.itemStr(exPromoteDept));
//        }
//
//        String sqlStr = "select"
//                + " a.static_month"
//                + " ,a.member_id"
//                + " ,a.card_no"
//                + " ,b.staff_branch_no"
//                + " ,b.issue_date"
//                + " ,decode(b.activate_flag, '1', 'N', '2', 'Y') activate_flag"
//                + " ,decode(b.current_code, '0', '0.正常', '1', '1.一般停用', '2', '2.挂失', '3', '3.強停', '4', '4.其他', '5', '5.偽卡') current_code"
//                + " ,c.id_no"
//                + " ,c.chi_name"
//                + " ,c.home_area_code1"
//                + " ,c.home_tel_no1"
//                + " ,c.cellar_phone"
//                + " ,nvl(d.member_name, '') member_name"
//                + " ,feedback_amt"
//                + " ,(select nvl(sum(purchase_amt), 0) from mkt_member_cardlist where card_no = a.card_no and substr(issue_date, 1, 4) = substr(a.issue_date, 1, 4)) year_amt"
//                + " from mkt_member_cardlist as a"
//                + " left join crd_card as b on a.card_no = b.card_no"
//                + " left join crd_idno as c on b.id_p_seqno = c.id_p_seqno"
//                + " left join mkt_member as d on b.staff_branch_no = d.staff_branch_no"
//                + " where 1=1"
//                + " and substr(a.issue_date, 1, 6) = substr(a.static_month, 1, 6)"
//                + optionalWhere
//                + " order by a.staff_branch_no";
//
//        sqlSelect(sqlStr, params.toArray(new Object[params.size()])); // sql執行
//        params.clear();
//        if (sqlRowNum <= 0) {
//            alertErr2("無資料可列印");
//            wp.respHtml = "TarokoErrorPDF";
//            return;
//        }
//        String fileName = null;
//        int file = 0;
//        wp.listCount[0] = sqlRowNum;
//        TarokoFileAccess tf = new TarokoFileAccess(wp);
//
//        // 判斷頁面為主頁面 or detl頁面
//        if (wp.itemEq("pageType", "cond")) {
//            fileName = "mktr2130-" + commString.mid(commDate.sysDatetime(), 4) + ".csv";
//            file = tf.openOutputText(fileName, "MS950");
//            String lsData = "No,聯名機構,推廣員工ID,卡號,持卡人ID,持卡人姓名,開戶日,住家電話,手機,開卡注記,卡片狀態,回饋金額,當月消費金額,當年度消費金額";
//            tf.writeTextFile(file, lsData + wp.newLine);
//
//            for (int i = 0; i < sqlRowNum; i++) {
//                lsData = (i+1)
//                        + ",\"" + sqlStr("staff_branch_no") + "_" + sqlStr("member_name") + "\""
//                        + ",\"" + sqlStr("member_id") + "\""
//                        + ",\"" + sqlStr("card_no") + "\""
//                        + ",\"" + sqlStr("id_no") + "\""
//                        + ",\"" + sqlStr("chi_name") + "\""
//                        + ",\"" + sqlStr("issue_date") + "\""
//                        + ",\"" + sqlStr("home_area_code1") + "_" + sqlStr("home_tel_no1") + "\""
//                        + ",\"" + sqlStr("cellar_phone") + "\""
//                        + ",\"" + sqlStr("activate_flag") + "\""
//                        + ",\"" + sqlStr("current_code") + "\""
//                        + ",\"" + sqlStr("feedback_amt") + "\""
//                        + ",\"" + sqlStr("dest_amt") + "\""
//                        + ",\"" + sqlStr("year_dest_amt") + "\"";
//                tf.writeTextFile(file, lsData + wp.newLine);
//            }
//        }
//
//        tf.closeOutputText(file);
//        wp.setDownload(fileName);
        try {
            wp.reportId = "mktr2130";
            TarokoExcel xlsx = new TarokoExcel();
            wp.fileMode = "N";
            xlsx.excelTemplate = "mktr2130.xlsx";
            wp.pageRows = 99999;
            queryFunc();
            wp.setListCount(1);
            xlsx.processExcelSheet(wp);
            xlsx.outputExcel();
        } catch (Exception ex) {
            wp.expMethod = "xlsPrint";
            wp.expHandle(ex);
        }
    }

    @Override
    public void logOnlineApprove() throws Exception {
    }
}
