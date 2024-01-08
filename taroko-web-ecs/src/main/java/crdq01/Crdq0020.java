/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-07  V1.00.00             program initial                            *
* 106-12-14  V1.00.01  Andy		  update : program name : Crdi0020==>Crdq0020*
* 108-08-13  V1.00.02  Andy		  update : UI                                *
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
* 109-07-27  V1.00.03  Wilson     卡號用途註記修改                                                                                         *
******************************************************************************/

package crdq01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdq0020 extends BaseEdit {
  String mExGroupCode = "";
  String mExCardType = "";
  String mExDateS = "";
  String mExDateE = "";

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
    // if (getWhereStr() == false)
    // return;
    String exDateS = wp.itemStr("ex_dateS");
    String exDateE = wp.itemStr("ex_dateE");
    String exGroupCode = wp.itemStr("ex_group_code");
    String exCardType = wp.itemStr("ex_card_type");
    String exCardFlag = wp.itemStr("ex_card_flag");
    String exOrder = wp.itemStr("ex_order");
    String exSelect = wp.itemStr("ex_select");
    String exSelectCnt = wp.itemStr("ex_select_cnt");
    
    if(exSelect.equals("1"))
    if(wp.itemEmpty("ex_select_cnt")) {
  	  alertErr("需輸入未使用筆數");
     	  return;
    }

    wp.sqlCmd = "select " + "group_code " + ", card_type " + ", card_flag " + ", trans_date "
        + ", crt_date " + ", trans_no " + ", bin_no " + ", beg_seqno " + ", end_seqno " + ", unuse "
        + ", charge_id " + ", charge_date " + ", member_note " + ", remark_40 " + ", wk_temp "
        + ", decode(card_flag,'1','一般用','2','緊急替代用','5','HCE TPAN用','7','BANK PAY',card_flag) as card_flag_desc "
        + "from ( select " + " group_code " + ", card_type " + ", card_flag " + ", crt_date "
        + ", trans_date " + ", trans_no " + ", bin_no " + ", beg_seqno " + ", end_seqno "
        + ", (end_seqno - beg_seqno) - ( " + "    (select count(*) " + "     from crd_seqno_log a "
        + "     where a.reserve='Y' " + "     and a.card_type = crd_cardno_range.card_type "
        + "     and a.group_code = crd_cardno_range.group_code and a.bin_no = crd_cardno_range.bin_no "
        + " and substr(a.seqno,1,9) >= crd_cardno_range.beg_seqno"
        + " and substr(a.seqno,1,9) <= crd_cardno_range.end_seqno ) + " 
        + "    (select count(*) "
        + "     from crd_prohibit a "
        + "     where substr(a.card_no,1,6) = crd_cardno_range.bin_no "
        + "     and substr(a.card_no,7,9) >= crd_cardno_range.beg_seqno "
        + "     and substr(a.card_no,7,9) <= crd_cardno_range.end_seqno) " + ") as unuse "
        + ", charge_id " + ", charge_date " + ",'' member_note " + ",remark_40 " + ",' ' wk_temp "
        + "from crd_cardno_range " + "where 1=1";
    if (!empty(exDateS) || !empty(exDateE)) {
      wp.sqlCmd += sqlStrend(exDateS, exDateE, "trans_date");
    }
    wp.sqlCmd += sqlCol(exGroupCode, "group_code");
    wp.sqlCmd += sqlCol(exCardType, "card_type");
    wp.sqlCmd += sqlCol(exCardFlag, "card_flag");
    wp.sqlCmd += ")";
    if (exSelect.equals("1")) {
      wp.sqlCmd += "where 1=1 ";
      wp.sqlCmd += sqlCol(exSelectCnt, "unuse","<");
    }
    if (exOrder.equals("1")) {
      wp.sqlCmd += " order by beg_seqno,unuse ";
    } else {
      wp.sqlCmd += " order by group_code,card_type,trans_date,unuse";
    }
    // getWhereStr();

    // 重新計算頁筆數/跳頁
    wp.pageCountSql = " select count(*) from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql += ")";

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
    String wpCrdFlag = "";
    int selCt = wp.selectCnt;
    for (int ii = 0; ii < selCt; ii++) {
      wpCrdFlag = wp.colStr(ii, "card_flag");
      String[] cde = new String[] {"1", "2", "5", "7"};
      String[] txt = new String[] {"一般用", "緊急替代用", "HCE TPAN用", "BANK PAY"};
      wp.colSet(ii, "card_flag", commString.decode(wpCrdFlag, cde, txt));
    }
  }

  @Override
  public void querySelect() throws Exception {}

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

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
      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.itemStr("kk_card_type");
      else
        wp.optionKey = wp.itemStr("ex_card_type");
      this.dddwList("dddw_card_type", "ptr_card_type", "card_type", "name",
          "where 1=1 order by card_type");

      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.itemStr("kk_group_code");
      else
        wp.optionKey = wp.itemStr("ex_group_code");
      this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1 order by group_code");
    } catch (Exception ex) {
    }
  }

}
