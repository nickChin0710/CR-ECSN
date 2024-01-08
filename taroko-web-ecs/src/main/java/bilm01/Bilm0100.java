/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-03  V1.00.00  yash       program initial                            *
* 107-03-13  V1.00.01  Andy       Update dddw_list merchant UI               *
* 108-12-10  V1.00.02  Andy       Update                                     *
* 108-12-13  V1.00.03  Andy       Update                                     *
* 108-12-20  V1.00.04  Andy       Update                                     *
* 108-12-25  V1.00.04  Andy       Update Fix Bug                             * 
* 109-04-23  V1.00.05  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package bilm01;


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0100 extends BaseEdit {
  String mExSeqNo = "";

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
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      strAction = "U";
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
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";
    if (empty(wp.itemStr("ex_merchant")) == false) {
      wp.whereStr += " and  mcht_no = :ex_merchant ";
      setString("ex_merchant", wp.itemStr("ex_merchant"));
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

    wp.selectSQL = " seq_no" + ", start_date" + ", end_date" + ", reserve_type" + ", amt_from"
        + ", amt_to" + ", mcht_no" + ", deny_mx" + ", twd_limit_flag" + ", apr_user" + ", apr_date"
        + ", mod_user" + ", to_char(mod_time,'yyyymmdd') as mod_time" + ", crt_date" + ", crt_user";

    wp.daoTable = "ptr_assign_installment";
    wp.whereOrder = " order by seq_no";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();

  }

  void listWkdata() throws Exception {
    String reserveType = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // wk_reserve_type 預約類別
      reserveType = wp.colStr(ii, "reserve_type");

      if (reserveType.equals("1")) {
        reserveType = "稅款(排除綜所稅)";
      } else if (reserveType.equals("2")) {
        reserveType = "綜所稅";
      } else if (reserveType.equals("3")) {
        reserveType = "學雜費";
      } else if (reserveType.equals("4")) {
          reserveType = "保費";
      }
      wp.colSet(ii, "reserve_type", reserveType);
    }
  }

  @Override
  public void querySelect() throws Exception {
    mExSeqNo = wp.itemStr("ex_merchant");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExSeqNo = wp.itemStr("kk_seq_no");
    if (empty(mExSeqNo)) {
      mExSeqNo = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", seq_no " + ", start_date" + ", end_date"
        + ", reserve_type" + ", amt_from" + ", amt_to" + ", mcht_no" + ", deny_mx"
        + ", twd_limit_flag" + ", apr_user" + ", apr_date" + ", mod_user" + ", mod_time"
        + ", crt_date" + ", crt_user";
    wp.daoTable = "ptr_assign_installment";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  seq_no = :seq_no ";
    setString("seq_no", mExSeqNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, seq_no=" + mExSeqNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }
    System.out.println("is_action :" + strAction);
    if (strAction.equals("A")) {
      // 20191210 no:1924 SA1521 提出修改
      String lsSql =
          "select count(*) ct  from ptr_assign_installment " + "where mcht_no =:mcht_no ";
      setString("mcht_no", wp.itemStr("mcht_no"));
      sqlSelect(lsSql);
      if (sqlNum("ct") >= 1) {
        alertErr("已有相同特店資料,不可新增!!");
        return;
      }
      //
      if (this.toInt(getSysDate()) > this.toInt(wp.itemStr("start_date"))) {
        alertErr("輸入起日不可小於系統日");
        return;
      }
    }
    if (strAction.equals("U")) {
      // 20191210 no:1924 SA1521 提出修改
      String lsSql =
          "select count(*) ct  from ptr_assign_installment " + "where hex(rowid) != :rowid and mcht_no =:mcht_no ";
      setString("rowid", wp.itemStr("rowid"));
      setString("mcht_no", wp.itemStr("mcht_no"));
      sqlSelect(lsSql);
      if (sqlNum("ct") >= 1) {
        alertErr("已有相同特店資料,不可修改!!");
        return;
      }
    }

    if (this.toInt(wp.itemStr("end_date")) < this.toInt(wp.itemStr("start_date"))) {
      alertErr("登錄日期迄需大於起日");
      return;
    }

    int qty = 0;
    // start_date
    if (empty(wp.itemStr("rowid"))) {
      // insert
      String lsSql =
          "select count(*) as qty  from ptr_assign_installment where reserve_type = :reserve_type  and :start_date between start_date and end_date";
      setString("reserve_type", wp.itemStr("reserve_type"));
      setString("start_date", wp.itemStr("start_date"));
      sqlSelect(lsSql);
      qty = (int) sqlNum("qty");
      if (qty > 0) {
        alertErr("活動期間 重疊");
        return;
      }
    } else {
      String lsSql = "select count(*) as qty  from ptr_assign_installment "
          + "where hex(rowid) != :rowid  and  reserve_type = :reserve_type  "
          + "and :start_date between start_date and end_date";
      setString("rowid", wp.itemStr("rowid"));
      setString("reserve_type", wp.itemStr("reserve_type"));
      setString("start_date", wp.itemStr("start_date"));
      sqlSelect(lsSql);

      qty = (int) sqlNum("qty");
      if (qty > 0) {
        alertErr("活動期間 重疊");
        return;
      }
    }

    if (wp.itemNum("amt_from") > wp.itemNum("amt_to")) {
      alertErr("金額起不能大於迄值");
      return;
    }

    String lsSql3 = "select count(*) as mqty   from bil_merchant where mcht_no=:merchant_no ";
    setString("merchant_no", wp.itemStr("mcht_no"));
    sqlSelect(lsSql3);

    double mqty = sqlNum("mqty");
    if (mqty <= 0) {
      alertErr("特店代號 不存在，請先到bilm0070維護");
      return;
    }



    Bilm0100Func func = new Bilm0100Func(wp);

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      // if (wp.respHtml.indexOf("_detl") > 0){
      // wp.initOption="--";
      // wp.optionKey = wp.col_ss("mcht_no");
      // }else{
      // wp.initOption="--";
      // wp.optionKey = wp.item_ss("ex_merchant");
      //
      // }
      //
      // this.dddw_list("dddw_merchant", "bil_merchant", "mcht_no", "", "where 1=1 order by
      // mcht_no");
    } catch (Exception ex) {
    }
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }
    return;
  }

}
