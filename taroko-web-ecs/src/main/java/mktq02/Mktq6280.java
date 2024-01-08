/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 112/03/24  V1.00.00    Yang Bo              Program Initial              *
 * 112/04/18  V1.00.01    Grace Huang          配合mkt_imfstp_list(_t)增active_seq欄位  *
 * 112/12/04  V1.00.02    Zuwei Su     增加”活動序號”欄位為查詢條件,取消”資料類別”欄位,活動代號droplist讀取邏輯  *
 ***************************************************************************/
package mktq02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktq6280 extends BaseEdit {
    private final String PROGNAME = "首刷禮活動匯入名單查詢作業 112/03/24 V1.00.00";
    String kk1;
    //String orgTabName = "mkt_imchannel_list";
    String orgTabName = "mkt_imfstp_list";
    String controlTabName = "";
    int qFrom = 0;

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) {//-資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
            strAction = "A";
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {/*  更新功能 */
            strAction = "U";
            updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
            deleteFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page*/
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "AJAX")) {/* AJAX */
            strAction = "AJAX";
            wfAjaxFunc2();
        }

        dddwSelect();
        initButton();
    }

    @Override
    public void queryFunc() throws Exception {
        if (queryCheck() != 0) {
            return;
        }
        wp.whereStr = "WHERE 1=1 "
                + sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
                + sqlCol(wp.itemStr("ex_active_seq"), "a.active_seq")
                + sqlCol(wp.itemStr("ex_list_data"), "a.list_data", "%like%")
                + sqlCol(wp.itemStr("ex_list_flag"), "a.list_flag");

        //-page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        if (wp.colStr("org_tab_name").length() > 0) {
            controlTabName = wp.colStr("org_tab_name");
        } else {
            controlTabName = orgTabName;
        }

        wp.pageControl();

        wp.selectSQL = " hex(a.rowid) as rowid, "
                + "a.active_code,"
                + "a.active_seq,"
                + "a.list_data,"
                + "a.list_flag,"
                + "to_char(MOD_TIME,'yyyy/mm/dd hh24:mi:ss') as mod_time";
        wp.daoTable = controlTabName + " a ";
        wp.whereOrder = " order by active_code, active_seq, list_data ";

        pageQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        commActiveCode("comm_active_code");
        commListFlag("comm_list_flag");
        wp.setPageValue();
        sqlParm.clear();
    }

    @Override
    public void querySelect() throws Exception {
        kk1 = itemKk("data_k1");
        qFrom = 1;
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        if (controlTabName.length() == 0) {
            if (wp.colStr("control_tab_name").length() == 0) {
                controlTabName = orgTabName;
            } else {
                controlTabName = wp.colStr("control_tab_name");
            }
        } else {
            if (wp.colStr("control_tab_name").length() != 0) {
                controlTabName = wp.colStr("control_tab_name");
            }
        }
        wp.selectSQL = " hex(a.rowid) as rowid,"
                + "a.active_code,"
                + "a.active_seq,"
                + "a.list_data,"
                + "a.list_flag,"
                + "'' as id_no,"
                + "'' as chi_name,"
                + "a.acct_type,"
                + "a.card_no,"
                + "a.ori_card_no,"
                + "to_char(mod_time,'yyyy/mm/dd hh24:mi:ss') as mod_time,"
                + "a.mod_pgm,"
                + "a.id_p_seqno";
        wp.daoTable = controlTabName + " a ";
        wp.whereStr = " where 1=1 ";
        if (qFrom == 1) {
            wp.whereStr = wp.whereStr + sqlRowId(kk1, "a.rowid");
        }

        pageSelect();
        if (sqlNotFind()) {
            alertErr("查無資料, key= " + "[" + kk1 + "]");
            return;
        }
        commListFlag("comm_list_flag");
        commActiveCode("comm_active_code");
        commIdNo("comm_id_no");
        commChiName("comm_chi_name");
        sqlParm.clear();
    }

    @Override
    public void saveFunc() throws Exception {
    }

    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
    }

    @Override
    public void initPage() {
//        wp.colSet("ex_query_table", "2");
        wp.itemSet("ex_query_table", "2");
    }

    @Override
    public void dddwSelect() {
        String lsSql;
        if ((wp.respHtml.equals("mktq6280"))) {
            wp.initOption = "--";
            wp.optionKey = "";
//            if (wp.colStr("ex_active_code").length() > 0) {
//                wp.optionKey = wp.colStr("ex_active_code");
//            }

//            if (wp.itemStr("ex_query_table").equals("1")) {
//                lsSql = procDynamicDddwActiveCode("mkt_fstp_parm");
//            } else {
//                lsSql = procDynamicDddwActiveCode("mkt_fstp_parm_t");
//            }

            lsSql = "SELECT distinct ACTIVE_CODE as db_code, ACTIVE_CODE as db_desc "
                    + "FROM MKT_IMFSTP_LIST "
                    + "order by active_code";
            wp.optionKey = wp.colStr("ex_active_code");
            dddwList("dddw_active_code", lsSql);
        }
    }

    public int queryCheck() throws Exception {
//        if (itemKk("ex_query_table").equals("1")) {
//            orgTabName = "mkt_imfstp_list";
//        } else {
//            orgTabName = "mkt_imfstp_list_t";
//        }

        controlTabName = orgTabName.toUpperCase();
        wp.colSet("control_tab_name", controlTabName);

        String sql1;
        if (wp.itemStr("ex_id_no").length() == 10) {
            sql1 = "select a.id_p_seqno, a.chi_name "
                    + " from crd_idno a, act_acno b "
                    + " where id_no = ? "
                    + "   and id_no_code = '0' "
                    + "   and a.id_p_seqno = b.id_p_seqno ";
            setString(1, wp.itemStr("ex_id_no").toUpperCase());
            sqlSelect(sql1);
            if (sqlRowNum <= 0) {
                alertErr(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
                return (1);
            }
            wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
            wp.colSet("ex_p_seqno", "");
            return (0);
        } else if (wp.itemStr("ex_id_no").length() == 11) {
            sql1 = "select a.p_seqno, a.id_p_seqno, a.corp_p_seqno, b.card_indicator "
                    + " from act_acno a,ptr_acct_type b "
                    + " where a.acct_key = ? "
                    + "   and a.acct_type = b.acct_type ";
            setString(1, wp.itemStr("ex_id_no").toUpperCase());
            sqlSelect(sql1);
            if (sqlRowNum <= 0) {
                alertErr(" 查無此帳戶查詢碼[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
                return (1);
            }

            sql1 = "select chi_name from crd_idno where id_p_seqno = ? ";
            setString(1, sqlStr("id_p_seqno"));
            sqlSelect(sql1);
            wp.colSet("ex_chi_name", sqlStr("chi_name"));
            wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
            wp.colSet("ex_p_seqno", "");

            return (0);
        }

        return (0);
    }

    public String procDynamicDddwActiveCode(String s1) {
        return " select b.active_code as db_code, "
                + " max(b.active_code||' '||b.active_name) as db_desc "
                + " from " + s1 + " b "
                + " where b.list_cond = 'Y' "
                + " group by b.active_code "
                + " order by b.active_code ";
    }

    public void wfAjaxFunc2() throws Exception {
        if (selectAjaxFunc20(wp.itemStr("ax_win_query_table")) != 0) {
            return;
        }

        for (int ii = 0; ii < sqlRowNum; ii++) {
            wp.addJSON("ajaxj_active_code", sqlStr(ii, "active_code"));
            wp.addJSON("ajaxj_active_name", sqlStr(ii, "active_name"));
        }
    }

    int selectAjaxFunc20(String s1) {
        if (s1.equals("1")) {
            wp.sqlCmd = " select "
                    + " active_code, "
                    + " max(active_name) as active_name "
                    + " from mkt_fstp_parm "
                    + " where list_cond = 'Y' "
                    + " group by active_code "
                    + " order by active_code ";
        } else {
            wp.sqlCmd = " select "
                    + " active_code, "
                    + " max(active_name) as active_name "
                    + " from mkt_fstp_parm_t "
                    + " where list_cond = 'Y' "
                    + " group by active_code "
                    + " order by active_code ";
        }

        this.sqlSelect();
        if (sqlRowNum <= 0) {
            alertErr("查無資料");
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public void commActiveCode(String s1) {
        String columnData;
        String sql1;
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            columnData = "";
            if (wp.colStr(ii, "active_code").length() == 0) {
                wp.colSet(ii, s1, columnData);
                continue;
            }

            sql1 = "select active_name as column_active_name "
                    + " from mkt_fstp_parm "
                    + " where 1 = 1 "
                    + " and active_code = ? "
                    + " union "
                    + " select active_name as column_active_name "
                    + " from mkt_fstp_parm_t "
                    + " where 1 = 1 "
                    + " and active_code = ? ";
            setString(1, wp.colStr(ii, "active_code"));
            setString(2, wp.colStr(ii, "active_code"));
            sqlSelect(sql1);

            if (sqlRowNum > 0) {
                columnData = columnData + sqlStr("column_active_name");
            }
            wp.colSet(ii, s1, columnData);
        }
        return;
    }

    // ************************************************************************
    public void commIdNo(String s1) {
        String columnData;
        String sql1;
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            columnData = "";
            if (wp.colStr(ii, "id_p_seqno").length() == 0) {
                wp.colSet(ii, s1, columnData);
                continue;
            }

            sql1 = "select id_no as column_id_no "
                    + " from crd_idno "
                    + " where 1 = 1 "
                    + " and id_p_seqno = ? ";
            setString(1, wp.colStr(ii, "id_p_seqno"));
            sqlSelect(sql1);

            if (sqlRowNum > 0) {
                columnData = columnData + sqlStr("column_id_no");
            }
            wp.colSet(ii, s1, columnData);
        }
    }

    // ************************************************************************
    public void commChiName(String s1) {
        String columnData;
        String sql1;
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            columnData = "";
            if (wp.colStr(ii, "id_p_seqno").length() == 0) {
                wp.colSet(ii, s1, columnData);
                continue;
            }

            sql1 = "select chi_name as column_chi_name "
                    + " from crd_idno "
                    + " where 1 = 1 "
                    + " and id_p_seqno = ? ";
            setString(1, wp.colStr(ii, "id_p_seqno"));
            sqlSelect(sql1);

            if (sqlRowNum > 0) {
                columnData = columnData + sqlStr("column_chi_name");
            }
            wp.colSet(ii, s1, columnData);
        }
    }

    // ************************************************************************
    public void commListFlag(String s1) {
        String[] cde = {"1", "2", "3", "4", "5"};
        String[] txt = {"身份證號", "卡號", "一卡通卡號", "悠遊卡號", "愛金卡號"};
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            for (int inti = 0; inti < cde.length; inti++) {
                String s2 = s1.substring(5);
                if (wp.colStr(ii, s2).equals(cde[inti])) {
                    wp.colSet(ii, s1, txt[inti]);
                    break;
                }
            }
        }
    }
}
