/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-24  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package mktm05;

import ofcapp.BaseAction;

public class Mktm5010 extends BaseAction {
  String mangrId = "", dataKK2 = "";

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("建檔日期:起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "id_no")
        + sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "crt_date")
        + sqlCol(wp.itemStr("ex_mangr_id"), "mangr_id")
        + sqlCol(wp.itemStr("ex_mangr_name"), "mangr_cname");

    if (!wp.itemEq("ex_apr_flag", "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " id_no , " + " mangr_id , " + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " apr_user , " + " apr_date , "
        + " apr_flag , " + " mangr_cname ";

    wp.daoTable = " mkt_ds_mangr ";
    wp.whereOrder = " order by mangr_id Asc";

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
    mangrId = wp.itemStr("data_k1");
    dataKK2 = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mangrId)) {
      mangrId = itemkk("mangr_id");
    }

    wp.selectSQL = " id_no , " + " mangr_cname , " + " mangr_id , " + " apr_date , "
        + " apr_flag , " + " apr_user , " + " crt_date , " + " crt_user , " + " mod_user , "
        + " mod_pgm , " + " to_char(mod_time,'yyyymmdd') as mod_date , " + " mod_seqno , "
        + " hex(rowid) as rowid ";

    wp.daoTable = " mkt_ds_mangr ";
    wp.whereStr = " where 1=1 " + sqlCol(mangrId, "mangr_id");
    wp.whereOrder = " order by apr_flag ";
    pageSelect();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {

    Mktm5010Func func = new Mktm5010Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    int llCnt = 0, llOk = 0, llErr = 0;
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsMangrId = wp.itemBuff("mangr_id");
    String[] lsAprFlag = wp.itemBuff("apr_flag");
    wp.listCount[0] = wp.itemRows("mangr_id");
    this.optNumKeep(wp.itemRows("mangr_id"), aaOpt);
    if (checkApproveZz() == false) {
      return;
    }

    Mktm5010Func func = new Mktm5010Func();
    func.setConn(wp);

    int rr = -1;
    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;
      llCnt++;

      func.varsSet("mangr_id", lsMangrId[rr]);
      func.varsSet("apr_user", wp.itemStr("approval_user"));
      func.varsSet("apr_flag", lsAprFlag[rr]);

      if (func.dataProc() == 1) {
        llOk++;
        wp.colSet(rr, "ok_flag", "V");
        sqlCommit(1);
        continue;
      } else {
        llErr++;
        wp.colSet(rr, "ok_flag", "X");
        sqlCommit(-1);
        continue;
      }

    }

    if (llCnt == 0) {
      alertErr2("請選擇覆核資料");
      return;
    } else {
      alertMsg("覆核完成 , 成功:" + llOk + " 失敗:" + llErr);
    }

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "mktm5010_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
