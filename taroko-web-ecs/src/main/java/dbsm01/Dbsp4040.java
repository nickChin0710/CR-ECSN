/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-05  V1.00.01  Alex       add initButton                             *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                           *
******************************************************************************/
package dbsm01;

import ofcapp.BaseAction;

public class Dbsp4040 extends BaseAction {

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = "";

    lsWhere = " where 1=1 and nvl(A.acct_post_flag,'N')<>'Y' and nvl(A.debit_flag,'N')='Y' "
        + sqlCol(wp.itemStr("ex_appr_no"), "A.appr_no");

    sum(lsWhere);

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  void sum(String lsWhere) {
    String sql1 = " select " + " sum(A.adj_amt) as tl_adj_amt " + " from cms_acaj A " + lsWhere;
    sqlSelect(sql1);
    if (sqlNum("tl_adj_amt") > 0) {
      wp.colSet("tl_adj_amt", "" + sqlNum("tl_adj_amt"));
    } else {
      wp.colSet("tl_adj_amt", "0");
    }
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        " A.* , " + " hex(A.rowid) as rowid , " + " uf_acno_key2(p_seqno,'Y') as acct_key ";
    wp.daoTable = "cms_acaj A";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;

    Dbsp4040Func func = new Dbsp4040Func();
    func.setConn(wp);

    String[] lsRowid = wp.itemBuff("rowid");
    String[] lsOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;

    for (int ii = 0; ii < lsRowid.length; ii++) {
      if (this.checkBoxOptOn(ii, lsOpt) == false)
        continue;

      func.varsSet("rowid", lsRowid[ii]);

      if (func.dataProc() == 1) {
        llOk++;
        wp.colSet(ii, "ok_flag", "V");
        sqlCommit(1);
        continue;
      } else {
        llErr++;
        wp.colSet(ii, "ok_flag", "X");
        this.dbRollback();
        continue;
      }
    }

    alertMsg("執行完畢 , 成功:" + llOk + " 失敗:" + llErr);

  }

  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
