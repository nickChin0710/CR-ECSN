/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;
/**
 * 2019-0731   JH    order by
 * */
import ofcapp.BaseAction;

public class Rskm0950 extends BaseAction {
  String adjYymm = "", adjLocFlag = "";

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
      // -CallBatch-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C2")) {
      // -Copy-
      procFuncCopy();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_adj_yymm"), "adj_yymm");

    if (!wp.itemEq("ex_adj_loc_flag", "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_adj_loc_flag"), "adj_loc_flag");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " A.* , decode(A.adj_loc_flag,'1','調高','2','調低') as tt_adj_loc_flag ";
    wp.daoTable = " rsk_r001_parm A ";
    wp.whereOrder = " order by A.adj_yymm Desc, A.adj_loc_flag ";
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
  }



  @Override
  public void querySelect() throws Exception {
    adjYymm = wp.itemStr("data_k1");
    adjLocFlag = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    if (empty(adjYymm))
      adjYymm = itemkk("adj_yymm");
    if (empty(adjLocFlag))
      adjLocFlag = itemkk("adj_loc_flag");

    if (empty(adjYymm)) {
      errmsg("額度覆核月份:不可空白");
      return;
    }

    if (empty(adjLocFlag)) {
      errmsg("調額類別:不可空白");
      return;
    }

    wp.selectSQL = " A.* , hex(A.rowid) as rowid , to_char(A.mod_time,'yyyymmdd') as mod_date , "
        + " decode(A.adj_loc_flag,'1','調高','2','調低') as tt_adj_loc_flag ";
    wp.daoTable = " rsk_r001_parm A ";
    wp.whereStr = " where 1=1 " + sqlCol(adjYymm, "A.adj_yymm") + sqlCol(adjLocFlag, "A.adj_loc_flag");
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {

    if (checkApproveZz() == false)
      return;

    rskm02.Rskm0950Func func = new rskm02.Rskm0950Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      saveAfter(false);
    }
  }

  @Override
  public void procFunc() throws Exception {
    if (wp.iempty("apr_date")) {
      alertErr2("主管未覆核, 不可執行");
      return;
    }

    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    batch.debug = true;

    // --callbatch
    rc = batch.callBatch("RskR002 " + wp.itemStr("adj_yymm") + " " + wp.itemStr("adj_loc_flag"));
    if (rc != 1) {
      alertErr2("callbatch 失敗");
      return;
    }

    alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());

  }

  void procFuncCopy() throws Exception {
    rskm02.Rskm0950Func func = new rskm02.Rskm0950Func();
    func.setConn(wp);

    rc = func.dataCopy();
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      dataReadCopy();
      alertMsg("複製完成");
    }

  }

  public void dataReadCopy() throws Exception {
    wp.selectSQL = " A.* , hex(A.rowid) as rowid , to_char(A.mod_time,'yyyymmdd') as mod_date ";
    wp.daoTable = " rsk_r001_parm A ";
    wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("copy_adj_yymm"), "A.adj_yymm")
        + sqlCol(wp.itemStr("adj_loc_flag"), "A.adj_loc_flag");
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "rskm0950_detl")) {
      btnModeAud();
    }
    if (eqIgno(strAction, "new")) {
      wp.colSet("kk_adj_loc_flag", "1");
      wp.colSet("adj_user1", "08017");
      wp.colSet("adj_limit_e1", "50000");
      wp.colSet("adj_limit_e2", "100000");
      wp.colSet("adj_limit_e3", "150000");
      wp.colSet("adj_limit_e4", "200000");
      wp.colSet("adj_limit_e5", "300000");
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
