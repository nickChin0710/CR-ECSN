/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/12/16  V1.00.01   Zuwei Su      Initial                              *
***************************************************************************/
package mktm01;

import java.util.ArrayList;
import java.util.List;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktm0520 extends BaseEdit {
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    busi.ecs.CommRoutine comr = null;
    Mktm0520Func func = null;
    String rowid;
    String orgTabName = "crd_employee_a";
    String controlTaName = "";
    int qFrom = 0;

    // ************************************************************************

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        switch (wp.buttonCode) {
            case "X":
                /* 轉換顯示畫面 */
                strAction = "new";
                clearFunc();
                break;
            case "Q":
                /* 查詢功能 */
                strAction = "Q";
                queryFunc();
                break;
            case "R":
                // -資料讀取-
                strAction = "R";
                dataRead();
                break;
            case "A":
                /* 新增功能 */
                strAction = "A";
                saveFunc();
                break;
            case "U":
                /* 更新功能 */
                strAction = "U";
                saveFunc();
                break;
            case "D":
                /* 刪除功能 */
                strAction = "D";
                saveFunc();
                break;
            case "R2":
                /* 參數頁面 資料讀取 */
                strAction = "R2";
                dataRead();
                break;
            case "M":
                /* 瀏覽功能 :skip-page */
                queryRead();
                break;
            case "S":
                /* 動態查詢 */
                querySelect();
                break;
            case "L":
                /* 清畫面 */
                strAction = "";
                clearFunc();
                break;
            default:
                break;
        }

        dddwSelect();
        initButton();
    }

    // ************************************************************************
    @Override
    public void queryFunc() throws Exception {
        // -page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
    }

    // ************************************************************************
    @Override
    public void queryRead() throws Exception {
        String aprFlag = "Y";
        if (wp.itemEq("ex_apr_flag", "Y")) {
            controlTaName = "crd_employee_a";
            aprFlag = "Y";
        } else {
            controlTaName = "crd_employee_a_t";
            aprFlag = "N";
        }

        wp.pageControl();

        wp.selectSQL = " "
                + "hex(rowid) as rowid, "
                + "corp_no, "
                + "a.subsidiary_no, "
                + "a.employ_no, "
                + "a.chi_name, "
                + "a.id, "
                + "a.acct_no, "
                + "a.unit_no, "
                + "a.unit_name, "
                + "a.subunit_no, "
                + "a.subunit_name, "
                + "a.position_id, "
                + "a.position_name, "
                + "a.status_id, "
                + "a.status_name, "
                + "a.description, "
                + "a.file_name, "
                + "a.apr_user, "
                + "a.apr_flag, "
                + "a.apr_date, "
                + "a.crt_user, "
                + "a.crt_date, "
                + "a.mod_user, "
                + "a.mod_time";

        wp.daoTable = controlTaName + " a ";
        wp.whereStr = " where a.apr_flag = ? " ;
        setString(aprFlag);
        if (!wp.itemEmpty("ex_corp_no")) {
            wp.whereStr += " and a.corp_no = ? ";
            setString(wp.itemStr("ex_corp_no"));
        }
        if (!wp.itemEmpty("ex_office_code")) {
            wp.whereStr += " and a.subsidiary_no = ? ";
            setString(wp.itemStr("ex_office_code"));
        }
        if (!wp.itemEmpty("ex_employ_no")) {
            wp.whereStr += " and a.employ_no = ? ";
            setString(wp.itemStr("ex_employ_no"));
        }

        pageQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

//        commFileFag("comm_file_flag");
//        commAprFlag("comm_apr_flag");

        // list_wkdata();
        wp.setPageValue();
    }

    // ************************************************************************
    @Override
    public void querySelect() throws Exception {
        rowid = itemKk("data_k1");
        qFrom = 1;
        dataRead();
    }

    // ************************************************************************
    @Override
    public void dataRead() throws Exception {
        String sql = ""
                + "hex(a.rowid) as rowid, "
                + "a.corp_no, "
                + "b.office_m_name, "
                + "a.subsidiary_no, "
                + "c.office_name, "
                + "a.employ_no, "
                + "a.chi_name, "
                + "a.id, "
                + "a.acct_no, "
                + "a.unit_no, "
                + "a.unit_name, "
                + "a.subunit_no, "
                + "a.subunit_name, "
                + "a.position_id, "
                + "a.position_name, "
                + "a.status_id, "
                + "a.status_name, "
                + "a.description, "
                + "a.file_name, "
                + "a.apr_user, "
                + "a.apr_flag, "
                + "a.apr_date, "
                + "a.crt_user, "
                + "a.crt_date, "
                + "a.mod_user, "
                + "to_char(a.mod_time, 'yyyymmdd') as mod_time";

        wp.whereStr = "where 1=1 ";
        if (qFrom == 1) {
            String aprFlag = "Y";
            if (wp.itemEq("apr_flag", "Y")) {
                wp.daoTable = "crd_employee_a a ";
                aprFlag = "Y";
                wp.selectSQL = sql + ", '' as error_code, '' as error_desc";
            } else {
                wp.daoTable = "crd_employee_a_t a ";
                aprFlag = "N";
                wp.selectSQL = sql + ", a.error_code, a.error_desc ";
            }
            wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
            wp.daoTable += " left join mkt_office_m b on a.corp_no = b.corp_no ";
            wp.daoTable += " left join mkt_office_d c on a.subsidiary_no = c.office_code ";
            pageSelect();
            if (sqlNotFind()) {
                alertErr2("查無資料, key= " + "[" + rowid + "]");
                return;
            }
        } else if (qFrom == 0) {
            wp.daoTable = "crd_employee_a_t a ";
            wp.daoTable += " left join mkt_office_m b on a.corp_no = b.corp_no ";
            wp.daoTable += " left join mkt_office_d c on a.subsidiary_no = c.office_code ";
            wp.selectSQL = sql + ", a.error_code, a.error_desc ";
            String whereStr = "where 1=1 ";
            List<Object> paramList = new ArrayList<>();
            if (!wp.itemEmpty("ex_corp_no")) {
                whereStr += " and a.corp_no = ? ";
                paramList.add(wp.itemStr("ex_corp_no"));
            }
            if (!wp.itemEmpty("ex_office_code")) {
                whereStr += " and a.subsidiary_no = ? ";
                paramList.add(wp.itemStr("ex_office_code"));
            }
            if (!wp.itemEmpty("ex_employ_no")) {
                whereStr += " and a.employ_no = ? ";
                paramList.add(wp.itemStr("ex_employ_no"));
            }
            Object[] params = paramList.toArray();
            wp.whereStr = whereStr;
            pageSelect(params);
            if (sqlNotFind()) {
                wp.daoTable = "crd_employee_a a ";
                wp.daoTable += " left join mkt_office_m b on a.corp_no = b.corp_no ";
                wp.daoTable += " left join mkt_office_d c on a.subsidiary_no = c.office_code ";
                wp.selectSQL = sql + ", '' as error_code, '' as error_desc ";
                wp.whereStr = whereStr;
                pageSelect(params);
                if (sqlNotFind()) {
                    alertErr2("查無資料");
                    return;
                }
            }
        }
    }

    // ************************************************************************
    public void saveFunc() throws Exception {
        Mktm0520Func func = new Mktm0520Func(wp);

        rc = func.dbSave(strAction);
        if (rc != 1)
            alertErr2(func.getMsg());
        log(func.getMsg());
        this.sqlCommit(rc);
    }

    // ************************************************************************
    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
    }

    // ************************************************************************
    @Override
    public void dddwSelect() {
        try {
            wp.initOption = "--";
            wp.optionKey = "";
            if (wp.colStr("ex_corp_no").length() > 0) {
                wp.optionKey = wp.colStr("ex_corp_no");
            }
            this.dddwList("dddw_mkt_office_m", "mkt_office_m", "corp_no", "office_m_name",
                    "where 1=1 order by corp_no");

            wp.initOption = "--";
            wp.optionKey = "";
            if (wp.colStr("ex_office_code").length() > 0) {
                wp.optionKey = wp.colStr("ex_office_code");
            }
            this.dddwList("dddw_mkt_office_d", "mkt_office_d", "office_code", "office_name",
                    "where 1=1 order by office_code");
        } catch (Exception ex) {
        }
    }

    // ************************************************************************
//    public void commFileFag(String cde1) throws Exception {
//        String[] cde = {"Y", "N"};
//        String[] txt = {"成功", "失敗"};
//        String columnData = "";
//        for (int ii = 0; ii < wp.selectCnt; ii++) {
//            for (int inti = 0; inti < cde.length; inti++) {
//                String s2 = cde1.substring(5, cde1.length());
//                if (wp.colStr(ii, s2).equals(cde[inti])) {
//                    wp.colSet(ii, cde1, txt[inti]);
//                    break;
//                }
//            }
//        }
//        return;
//    }

    // ************************************************************************
//    public void commAprFlag(String cde1) throws Exception {
//        String[] cde = {"Y", "N", "X", "T"};
//        String[] txt = {"已覆核", "待覆核", "不同意匯入", "失敗"};
//        String columnData = "";
//        for (int ii = 0; ii < wp.selectCnt; ii++) {
//            for (int inti = 0; inti < cde.length; inti++) {
//                String txt1 = cde1.substring(5, cde1.length());
//                if (wp.colStr(ii, txt1).equals(cde[inti])) {
//                    wp.colSet(ii, cde1, txt[inti]);
//                    break;
//                }
//            }
//        }
//        return;
//    }

    // ************************************************************************
    @Override
    public void initPage() {
        return;
    }
    // ************************************************************************

} // End of class
