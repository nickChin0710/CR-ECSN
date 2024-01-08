/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-01  V1.00.01  ryan       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
******************************************************************************/
package mktp01;

import busi.SqlPrepare;
import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Mktp0720 extends BaseProc {

  int rr = -1;
  String msg = "";
 // String kk1 = "", kk2 = "";
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {

    try {
      wp.optionKey = wp.itemStr("ex_mcht_group_old");
      dddwList("dddw_mcht_group_old", "mkt_mcht_group", "mcht_group_id", "mcht_group_desc",
          "where 1=1 and apr_date<>'' ");
      wp.optionKey = wp.itemStr("ex_mcht_group_new");
      dddwList("dddw_mcht_group_new", "mkt_mcht_group", "mcht_group_id", "mcht_group_desc",
          "where 1=1 and apr_date<>'' ");

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // for query use only
  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_crt_date1");
    String lsDate2 = wp.itemStr("ex_crt_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[設定日期-起迄]  輸入錯誤");
      return -1;
    }
    lsDate1 = wp.itemStr("ex_mcht_no1");
    lsDate2 = wp.itemStr("ex_mcht_no2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[特店代號-起迄]  輸入錯誤");
      return -1;
    }

    wp.whereStr = " where 1=1 and bil_mcht_group_t.mcht_no = bil_merchant.mcht_no ";
    if (empty(wp.itemStr("ex_crt_date1")) == false) {
      wp.whereStr += " and bil_mcht_group_t.crt_date >= :ex_crt_date1 ";
      setString("ex_crt_date1", wp.itemStr("ex_crt_date1"));
    }
    if (empty(wp.itemStr("ex_crt_date2")) == false) {
      wp.whereStr += " and bil_mcht_group_t.crt_date <= :ex_crt_date2 ";
      setString("ex_crt_date2", wp.itemStr("ex_crt_date2"));
    }

    if (empty(wp.itemStr("ex_mcht_no1")) == false) {
      wp.whereStr += " and bil_mcht_group_t.mcht_no >= :ex_mcht_no1 ";
      setString("ex_mcht_no1", wp.itemStr("ex_mcht_no1"));
    }
    if (empty(wp.itemStr("ex_mcht_no2")) == false) {
      wp.whereStr += " and bil_mcht_group_t.mcht_no <= :ex_mcht_no2 ";
      setString("ex_mcht_no2", wp.itemStr("ex_mcht_no2"));
    }


    if (empty(wp.itemStr("ex_mcht_group_old")) == false) {
      wp.whereStr += " and bil_mcht_group_t.mcht_group_id = :ex_mcht_group_old ";
      setString("ex_mcht_group_old", wp.itemStr("ex_mcht_group_old"));
    }
    if (empty(wp.itemStr("ex_mcht_group_new")) == false) {
      wp.whereStr += " and bil_mcht_group_t.mcht_group_id_new = :ex_mcht_group_new ";
      setString("ex_mcht_group_new", wp.itemStr("ex_mcht_group_new"));
    }
    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and bil_mcht_group_t.crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " bil_mcht_group_t.mcht_group_id_new, " + " bil_merchant.mcht_chi_name,  "
        + " bil_merchant.mcht_no,  " + " bil_merchant.mcht_group_id, "
        + " bil_mcht_group_t.crt_user,  " + " bil_mcht_group_t.crt_date ";
    wp.daoTable = " bil_mcht_group_t,bil_merchant ";
    wp.whereOrder = "  ";
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }


  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {

    busi.SqlPrepare sp = new SqlPrepare();
    String[] aaOpt = wp.itemBuff("opt");
    String[] aaMchtNo = wp.itemBuff("mcht_no");

    wp.listCount[0] = aaMchtNo.length;
    // -update-
    for (rr = 0; rr < aaMchtNo.length; rr++) {
      if (!checkBoxOptOn(rr, aaOpt)) {
        continue;
      }
      // -Update bil_merchant-
      sp.sql2Update("bil_merchant");
      sp.ppstr("mcht_group_id", wp.itemStr("mcht_group_id_new"));
      sp.ppstr("mod_user", wp.itemStr("crt_user"));
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
      sp.sql2Where(" where mcht_no=?", aaMchtNo[rr]);
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        wp.colSet(rr, "ok_flag", "!");
        alertErr("Update bil_merchant err");
        sqlCommit(0);
        return;
      }
      // -Delete bil_mcht_group_t-
      String sqlDelete = " delete from bil_mcht_group_t where mcht_no =:ls_mcht_no ";
      setString("ls_mcht_no", aaMchtNo[rr]);
      sqlExec(sqlDelete);
      if (sqlRowNum <= 0) {
        wp.colSet(rr, "ok_flag", "!");
        alertErr("delete bil_mcht_group_t err");
        sqlCommit(0);
        return;
      }
    }
    sqlCommit(1);
    queryFunc();
    errmsg("覆核處理成功");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void listWkdata() {

  }
}
