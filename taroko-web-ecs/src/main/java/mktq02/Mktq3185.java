/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                  DESCRIPTION                 *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/10/07  V1.00.00  Zuwei Su    Initial                                   *
 *  112/11/17  V1.00.01  Zuwei Su    fix                                   *
 ******************************************************************************/
package mktq02;

import ofcapp.BaseAction;

public class Mktq3185 extends BaseAction {
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
    public void dddwSelect() {}

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
        

        wp.selectSQL = " ltrim(rtrim(a.chi_name)) as chi_name,a.id_no,a.group_code||'_'||b.group_name as group_desc,a.card_no,a.CELLAR_PHONE,TOT_AMT ";
        wp.daoTable += " mkt_thsr_upgrade_list a LEFT JOIN ptr_group_code b ON a.group_code=b.group_code       "
                + " LEFT JOIN crd_idno c ON a.id_p_seqno=c.id_p_seqno                                          ";
        wp.whereOrder += " ORDER BY use_month,id_no ";

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr("查無資料");
            return;
        }
        wp.setPageValue();
      if (!getWhereStr()) {
            return;
      }
      // 當月總筆數和總金額
      String countSql = "select count(1) as total, sum(TOT_AMT) as totalAmt "
              + " from mkt_thsr_upgrade_list a "
              + " LEFT JOIN ptr_group_code b ON a.group_code=b.group_code  "
              + " LEFT JOIN crd_idno c ON a.id_p_seqno=c.id_p_seqno"
              + lsWhere ;
      sqlSelect(countSql);
      wp.colSet("total", sqlInt("total"));
      wp.colSet("totalAmt", sqlNum("totalAmt"));
    }

    @Override
    public void querySelect() throws Exception {}

    @Override
    public void dataRead() throws Exception {}

    @Override
    public void saveFunc() throws Exception {}

    @Override
    public void procFunc() throws Exception {}

    @Override
    public void initButton() {}

    @Override
    public void initPage() {}

    boolean getWhereStr() {
        String exId = wp.itemStr("ex_id_no");
        String exUseMonth = wp.itemStr("ex_use_month");
        String exCardNo = wp.itemStr("ex_card_no");

        lsWhere = " where 1 = 1 ";
        if (empty(exUseMonth)) {
            alertErr2("查詢年月不可為空");
            return false;
        }
        lsWhere += " and use_month = :use_month ";
        setString("use_month",exUseMonth);
        if (!empty(exId)) {
            if (exId.length() != 10) {
                alertErr("身分證號長度必須為10位");
                return false;
            }
            lsWhere += " and a.id_no = :id_no ";
            setString("id_no",exId);
        }
        if (!empty(exCardNo)) {
            lsWhere += " and a.card_no = :card_no ";
            setString("card_no",exCardNo);
        }

        wp.whereStr = lsWhere;
        return true;
    }
}
