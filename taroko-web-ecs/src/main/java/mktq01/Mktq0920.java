/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                  DESCRIPTION                 *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/04/07  V1.00.00    machao                Initial          *
 ******************************************************************************/
package mktq01;

import ofcapp.BaseAction;

public class Mktq0920 extends BaseAction {
    private String lsWhere = "";

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
        } else if (eqIgno(wp.buttonCode, "C")) {
            // -資料處理-
            procFunc();
        }
    }

    @Override
    public void dddwSelect() {
    }

    @Override
    public void queryFunc() throws Exception {
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        if (!getWhereStr()) {
            return;
        }

        wp.selectSQL = " hex(a.rowid) as SER_NUM, a.id_no, b.chi_name, a.id_p_seqno, a.apply_date, " +
                " a.cancel_date, a.update_date ";
        wp.daoTable += " mkt_state_internet_h a " +
                " left join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
        wp.whereOrder += " order by a.id_no asc ";

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr("查無資料");
            return;
        }
        wp.setPageValue();
    }

    @Override
    public void querySelect() throws Exception {
    }

    @Override
    public void dataRead() throws Exception {
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

    boolean getWhereStr() {
        String exId = wp.itemStr("ex_id_no");
        String exApplyDateS = wp.itemStr("ex_Apply_date_s");
        String exApplyDateE = wp.itemStr("ex_Apply_date_e");

        if (empty(exId) && empty(exApplyDateS) && empty(exApplyDateE)) {
            alertErr("查詢條件不可全部空白");
            return false;
        }

        lsWhere = " where 1 = 1 ";
        if (!empty(exId)) {
            if (exId.length() != 10) {
                alertErr("身分證號長度必須為10位");
                return false;
            } else {
                lsWhere += " and a.id_no = ? ";
                setString(exId);
            }
        }
        if (!empty(exApplyDateS)) {
        	lsWhere += " and a.Apply_date >= ? ";
            setString(exApplyDateS);
        }
        if (!empty(exApplyDateE)) {
        	lsWhere += " and a.Apply_date <= ? ";
            setString(exApplyDateE);
        }

        wp.whereStr = lsWhere;
        return true;
    }
}
