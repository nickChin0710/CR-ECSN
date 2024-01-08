/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-26  V1.00.00  yash           program initial                        *
* 106-12-14            Andy		  update : program name : Bili0020==>Bilq0020*
* 109-04-23  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/

package bilq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilq0020 extends BaseEdit {
  String mExReferenceNo = "";

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
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_cardno")) == false) {
      wp.whereStr += " and  card_no like :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno") + "%");
    }

    if (empty(wp.itemStr("exDateS")) == false) {
      // wp.whereStr += " and acquire_date >= :exDateS ";
      wp.whereStr += " and  this_close_date >= :exDateS ";
      setString("exDateS", wp.itemStr("exDateS"));
    }

    if (empty(wp.itemStr("exDateE")) == false) {
      wp.whereStr += " and  this_close_date <= :exDateE ";
      setString("exDateE", wp.itemStr("exDateE"));
    }

    if (empty(wp.itemStr("ex_bill_unit")) == false) {
      wp.whereStr += " and  substr(bill_type,1,2) = :ex_bill_unit ";
      setString("ex_bill_unit", wp.itemStr("ex_bill_unit"));
    }

    if (empty(wp.itemStr("ex_batch_no")) == false) {
      wp.whereStr += " and   batch_no like :ex_batch_no ";
      setString("ex_batch_no", wp.itemStr("ex_batch_no") + "%");
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      wp.whereStr += " and  curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
    }

    if (empty(wp.itemStr("ex_kind")) == false) {

      switch (wp.itemStr("ex_kind")) {
        case "1":
          wp.whereStr +=
              " and decode(doubt_type,'','k',doubt_type) !='k' and decode(rsk_type,'','k',rsk_type) ='k' ";
          break;
        case "2":
          wp.whereStr += " and decode(rsk_type,'','k',rsk_type) ='1'";
          break;
        case "3":
          wp.whereStr += " and decode(rsk_type,'','k',rsk_type) in ('2','3') ";
          break;
        case "4":
          wp.whereStr += " and decode(rsk_type,'','k',rsk_type) ='3' ";
          break;
        case "5":
          wp.whereStr += " and decode(rsk_type,'','k',rsk_type) in ('4','5')  ";
          break;
        case "6":
          wp.whereStr +=
              " and (decode(doubt_type,'','k',doubt_type) !='k' or  decode(rsk_type,'','k',rsk_type) !='k') ";
          break;
        case "7":
          wp.whereStr +=
              " and decode(doubt_type,'','k',doubt_type) = 'k' and decode(rsk_type,'','k',rsk_type) ='k' ";
          break;
        case "8":
          wp.whereStr += " and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) ='Y' ";
          wp.whereStr += " and decode(double_chk_ok_flag,'','N',double_chk_ok_flag) ='Y' ";
          wp.whereStr += " and decode(err_chk_ok_flag,'','N',err_chk_ok_flag) ='Y' ";
          break;

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

    wp.selectSQL = " card_no" + ", reference_no" + ", batch_no"
        + ", decode(dc_amount,0,dest_amt,dc_amount) as amt" + ", source_amt" + ", curr_code"
        + ", decode(sign_flag,'-',-1,1)*decode(dc_amount,0,dest_amt,dc_amount) as amt_sign"
        + ", purchase_date" + ", acquire_date" + ", bill_type" + ", mcht_chi_name"
        + ", mcht_eng_name" + ", mcht_city" + ", mcht_country" + ", rsk_type" + ", this_close_date";

    wp.daoTable = "bil_curpost";
    wp.whereOrder = " order by card_no,reference_no";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

    String lsSql =
        "select  sum(decode(sign_flag,'-',-1,1)*decode(dc_amount,0,dest_amt,dc_amount)) as sum_amt from bil_curpost ";
    lsSql += " where 1=1 ";

    if (empty(wp.itemStr("ex_cardno")) == false) {
      lsSql += " and  card_no = :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno"));
    }

    if (empty(wp.itemStr("exDateS")) == false) {
      // wp.whereStr += " and acquire_date >= :exDateS ";
      lsSql += " and  this_close_date >= :exDateS ";
      setString("exDateS", wp.itemStr("exDateS"));
    }

    if (empty(wp.itemStr("exDateE")) == false) {
      lsSql += " and  this_close_date <= :exDateE ";
      setString("exDateE", wp.itemStr("exDateE"));
    }

    if (empty(wp.itemStr("ex_bill_unit")) == false) {
      lsSql += " and  substr(bill_type,1,2) = :ex_bill_unit ";
      setString("ex_bill_unit", wp.itemStr("ex_bill_unit"));
    }

    if (empty(wp.itemStr("ex_batch_no")) == false) {
      lsSql += " and   batch_no = :ex_batch_no ";
      setString("ex_batch_no", wp.itemStr("ex_batch_no"));
    }

    if (empty(wp.itemStr("ex_curr_code")) == false) {
      lsSql += " and  curr_code = :ex_curr_code ";
      setString("ex_curr_code", wp.itemStr("ex_curr_code"));
    }

    if (empty(wp.itemStr("ex_kind")) == false) {

      switch (wp.itemStr("ex_kind")) {
        case "1":
          lsSql +=
              " and decode(doubt_type,'','k',doubt_type) !='k' and decode(rsk_type,'','k',rsk_type) ='k' ";
          break;
        case "2":
          lsSql += " and decode(rsk_type,'','k',rsk_type) ='1'";
          break;
        case "3":
          lsSql += " and decode(rsk_type,'','k',rsk_type) in ('2','3') ";
          break;
        case "4":
          lsSql += " and decode(rsk_type,'','k',rsk_type) ='3' ";
          break;
        case "5":
          lsSql += " and decode(rsk_type,'','k',rsk_type) in ('4','5')  ";
          break;
        case "6":
          lsSql +=
              " and (decode(doubt_type,'','k',doubt_type) !='k' or  decode(rsk_type,'','k',rsk_type) !='k') ";
          break;
        case "7":
          lsSql +=
              " and decode(doubt_type,'','k',doubt_type) = 'k' and decode(rsk_type,'','k',rsk_type) ='k' ";
          break;
        case "8":
          lsSql += " and decode(format_chk_ok_flag,'','N',format_chk_ok_flag) ='Y' ";
          lsSql += " and decode(double_chk_ok_flag,'','N',double_chk_ok_flag) ='Y' ";
          lsSql += " and decode(err_chk_ok_flag,'','N',err_chk_ok_flag) ='Y' ";
          break;

      }
    }
    sqlSelect(lsSql);
    wp.colSet("exAmt", sqlStr("sum_amt"));
    wp.colSet("exCnt", intToStr(wp.dataCnt));

  }

  @Override
  public void querySelect() throws Exception {
    mExReferenceNo = wp.itemStr("reference_no");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExReferenceNo = wp.itemStr("kk_reference_no");
    if (empty(mExReferenceNo)) {
      mExReferenceNo = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ",reference_no " + " ,batch_no" + " ,card_no"
        + " ,txn_code" + " ,bill_type" + " ,dest_amt" + " ,dest_curr" + " ,source_amt"
        + " ,source_curr"
        + " ,decode(sign_flag,'-',-1,1)*decode(dc_amount,0,dest_amt,dc_amount) as amt_sign"
        + " ,curr_code" + " ,purchase_date" + " ,dc_exchange_rate" + " ,auth_code"
        + " ,pos_entry_mode" + " ,doubt_type" + " ,film_no" + " ,rsk_type" + " ,duplicated_flag"
        + " ,process_date" + " ,mcht_no" + " ,mcht_city" + " ,mcht_chi_name" + " ,mcht_eng_name"
        + " ,mcht_country" + " ,mcht_state" + " ,mcht_zip" + " ,mcht_category";
    wp.daoTable = "bil_curpost";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  reference_no = :reference_no ";
    setString("reference_no", mExReferenceNo);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, reference_no=" + mExReferenceNo);
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // Bili0020_func func = new Bili0020_func(wp);
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
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_bill_unit");
      this.dddwList("dddw_bill_unit", "ptr_billunit", "bill_unit", "short_title",
          "where 1=1 order by bill_unit");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_curr_code");
      this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", 
    		  "where wf_type = 'DC_CURRENCY' order by wf_id");

    } catch (Exception ex) {
    }
  }

}
