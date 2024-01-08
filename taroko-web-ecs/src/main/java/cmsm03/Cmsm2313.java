/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 112/05/05  V1.00.00  Ryan      Program Initial & Naming rule update     *
 ******************************************************************************/
package cmsm03;

import ofcapp.BaseAction;

public class Cmsm2313 extends BaseAction {
    String kk1 = "", kk2 = "";

    @Override
    public void userAction() throws Exception {
        switch (wp.buttonCode) {
            case "C":
                procFunc();
                break;
            default:
                defaultAction();
        }
    }

    @Override
    public void dddwSelect() {
    }

    @Override
    public void queryFunc() throws Exception {

        String lsWhere = " where 1=1 "
                + sqlCol(wp.itemStr("ex_card_no"), "card_no");
        String lsIdno = wp.itemStr("ex_idno");
        if (wp.itemEmpty("ex_card_no") && notEmpty(lsIdno)) {
            lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1" + sqlCol(lsIdno, "id_no") + " )";
        }

        wp.whereStr = lsWhere;
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        wp.selectSQL = "hex(rowid) as rowid ,"
                + " crt_date, crt_time," 
        		+" card_no, v_card_no," +
                " opt_ver_cnt," +
                " wallet_id," +
                " mod_pgm, hex(rowid) as rowid  "
        ;
        wp.daoTable = "hce_apply_data ";
        wp.whereOrder = " order by crt_date, crt_time, card_no ";

        pageQuery();
        wp.setListCount(1);
        if (sqlRowNum <= 0) {
            alertErr("此條件查無資料");
            return;
        }
        wp.setPageValue();

    }

    @Override
    public void querySelect() throws Exception {
        kk1 = wp.itemStr("data_k1"); //rowid
        kk2 = wp.itemStr("data_k2");
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        if (empty(kk1)) {
            kk1 = wp.itemStr("rowid");
        }
        if (empty(kk1)) {
            alertErr("請由查詢資料選取");
            return;
        }
        if (empty(kk2)) {
            kk2 = wp.itemStr("card_no");
        }

        wp.selectSQL = "hex(A.rowid) as rowid , A.*"
        ;
        wp.daoTable = "HCE_APPLY_DATA A";
        wp.whereStr = " where A.rowid =? ";

        setRowid(1, kk1);
        pageSelect();
        if (sqlNotFind()) {
            alertErr("查無資料, key=[%s]" + kk2);
        }
    }

    @Override
    public void saveFunc() throws Exception {
    }

    @Override
    public void procFunc() throws Exception {
        if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
  	      return;
  	    }

        Cmsm2313Func func = new Cmsm2313Func();
        func.setConn(wp);
        int li_rc = func.dataProc();
        sqlCommit(li_rc);
        if (li_rc != 1) {
            alertErr(func.getMsg());
            return;
        }
        alertMsg("HCE ID&V檢核次數重置 成功");
        dataRead();
    }

    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
    }

    @Override
    public void initPage() {
    }
}
