/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-12  V1.00.00             program initial                            *
* 108-12-03  V1.00.01  Amber	  Update init_button Authority 			     *
* 109-02-05  V1.00.01  Amber	  Update init_button Authority 			     *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-07-27  V1.00.03  Wilson     卡號用途註記修改                                                                                         *
******************************************************************************/

package crdm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0050 extends BaseEdit {
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

  @Override
  public void saveFunc() throws Exception {
    Crdm0050Func func = new Crdm0050Func(wp);
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    if (rc > 0 && strAction.equals("D") == false) {
      strAction = "R";
      updateRetrieve = true;
    }
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String cardNo1 = wp.itemStr("ex_card_no1");

    String binNo1 = String.format("%-16s", cardNo1).replace(" ", "0").substring(0, 6);
    String seqno1 = String.format("%-16s", cardNo1).replace(" ", "0").substring(6, 16);

    wp.whereStr = " where 1=1 and RESERVE_DATE <> '' ";

    if (empty(wp.itemStr("ex_card_type")) == false) {
      wp.whereStr += " and  card_type = :card_type ";
      setString("card_type", wp.itemStr("ex_card_type"));
    }

    if (empty(wp.itemStr("ex_groupcode")) == false) {
      wp.whereStr += " and  group_code = :group_code ";
      setString("group_code", wp.itemStr("ex_groupcode"));
    }

    if (empty(wp.itemStr("ex_use")) == false) {
      wp.whereStr += " and  card_flag = :card_flag ";
      setString("card_flag", wp.itemStr("ex_use"));
    }

    if (empty(wp.itemStr("ex_reason_code")) == false) {
      wp.whereStr += " and  reason_code = :reason_code ";
      setString("reason_code", wp.itemStr("ex_reason_code"));
    }


    if (empty(wp.itemStr("ex_card_no1")) == false) {
      wp.whereStr += " and  ( bin_no >= :b_card1 and seqno >= :s_card1 ) ";
      setString("b_card1", binNo1);
      setString("s_card1", seqno1);
    }

    switch (wp.itemStr("ex_use_type")) {
      case "N":
        wp.whereStr += " and use_date = '' ";
        break;
      case "Y":
        wp.whereStr += " and use_date != '' ";
        break;
    }

    if (empty(wp.itemStr("ex_reserve")) == false) {
      wp.whereStr += " and  reserve = :reserve ";
      setString("reserve", wp.itemStr("ex_reserve"));
    }

    if (empty(wp.itemStr("cancel_all")) == false) {
      wp.whereStr += " and  = :cancel_all ";
      setString("cancel_all", wp.itemStr("cancel_all"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {

    if (!getWhereStr()) {
      return;
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " bin_no" + ", seqno  " + ", bin_no||seqno as card" + ", group_code"
        + ", card_type"
        + ", decode(card_flag,'1','一般','2','緊急替代卡','5','HCE TPAN用','7','BANK PAY',card_flag) as card_flag_desc "
        + ", reserve" + ", decode(reserve,'N','未保留','Y','已保留',reserve) as reserve_desc"
        + ", decode(use_date,'','N','Y') as use_type" + ", use_date" + ", use_id" + ", reason_code"
        + ", decode(reason_code,'1','首發指定','2','長官指定','3','持卡人指定',reason_code) as reason_code_desc"
        + ", reserve_date" + ", reserve_id";

    wp.daoTable = "crd_seqno_log";

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
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    String binNo = wp.itemStr("s_bin_no");
    String seqno = wp.itemStr("s_seqno");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "bin_no," + "seqno," + "group_code,"
        + "card_type," + "unit_code," + "card_item," + "card_type_sort," + "card_flag," + "reserve,"
        + "reserve_date," + "reserve_id," + "reason_code," + "use_date," + "use_id," + "crt_date,"
        + "trans_no," + "seqno_old," + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_time,"
        + "mod_pgm";
    wp.daoTable = "crd_seqno_log";
    wp.whereStr = "where  bin_no = :bin_no and seqno = :seqno ";
    setString("bin_no", binNo);
    setString("seqno", seqno);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, bin_no=" + binNo + ", seqno=" + seqno);
    }
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
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.itemStr("card_type");
      } else {
        wp.optionKey = wp.itemStr("ex_card_type");
      }
      this.dddwList("dddw_cardtype", "ptr_card_type", "card_type", "name",
          "where 1=1  order by card_type");

      wp.initOption = "--";
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.itemStr("group_code");
      } else {
        wp.optionKey = wp.itemStr("ex_groupcode");
      }
      this.dddwList("dddw_groupcode", "ptr_group_code", "group_code", "group_name",
          "where 1=1  order by group_code");
    } catch (Exception ex) {
    }
  }



}
