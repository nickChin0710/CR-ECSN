/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-09  V1.00.00  yash       program initial                            *
* 109-04-21  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package ipsm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ipsq0100 extends BaseEdit {
  String mExFileCode = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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

  // for query use only
  private boolean getWhereStr() throws Exception {
	sqlParm.clear();
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and  notify_date >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }

    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and  notify_date <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }

    if (empty(wp.itemStr("ok_flag")) == false) {

      if (wp.itemStr("ok_flag").equals("1")) {
        wp.whereStr += " and  record_fail = 0 ";

      } else if (wp.itemStr("ok_flag").equals("2")) {
        wp.whereStr += " and  record_fail > 0 ";
      }
    }

    if (empty(wp.itemStr("ex_file_iden")) == false) {
      wp.whereStr += " and  file_iden like :ex_file_iden ";
      setString("ex_file_iden", wp.itemStr("ex_file_iden") + "%");
    }

    if (empty(wp.itemStr("ex_tran_type")) == false) {

      if (!wp.itemStr("ex_tran_type").equals("0")) {
        wp.whereStr += " and  tran_type = :ex_tran_type ";
        setString("ex_tran_type", wp.itemStr("ex_tran_type"));
      }
    }
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " file_iden" + ", file_name" + ", tran_type" + ", notify_date" + ", resp_code"
        + ", record_cnt" + ", record_fail" + ", proc_flag" + ", check_code" + ", perform_flag";

    wp.daoTable = "ips_notify_log";
    wp.whereOrder = " order by notify_date";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExFileCode = wp.itemStr("file_code");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExFileCode = wp.itemStr("kk_file_code");
    if (empty(mExFileCode)) {
      mExFileCode = itemKk("data_k1");
    }

    wp.selectSQL =
        "hex(rowid) as rowid, mod_seqno " + ", file_code " + ", xxx" + ", crt_date" + ", crt_user";
    wp.daoTable = "ips_notify_log";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  file_code = :file_code ";
    setString("file_code", mExFileCode);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, file_code=" + mExFileCode);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    //
    // Ipsq0030_func func = new Ipsq0030_func(wp);
    //
    // rc = func.dbSave(is_action);
    // ddd(func.getMsg());
    // if (rc != 1) {
    // err_alert(func.getMsg());
    // }
    // this.sql_commit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    // try {
    // if (wp.respHtml.indexOf("_detl") > 0)
    // wp.optionKey = wp.item_ss("kk_file_code");
    // else
    // wp.optionKey = wp.item_ss("ex_file_code");
    // this.dddw_list("dddw_file_code", "ips_file_resp", "file_code", "", "where 1=1 group by
    // file_code order by file_code");
    // }
    // catch(Exception ex) {}
  }

}