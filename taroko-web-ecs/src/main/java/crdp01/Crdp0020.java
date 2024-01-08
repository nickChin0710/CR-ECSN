/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-03  V1.00.00  ryan          program initial                            *
* 106-12-14  V1.00.01  Andy		    update : ucStr==>commString                     *
* 109-02-05  V1.00.02  JustinWu remove the call_Batch function *
* 109-02-10  V1.00.03  JustinWu add initFlag = "Y"
* 109-04-28  V1.00.04  YangFang   updated for project coding standard        *
* 109-07-24  V1.00.05  Wilson     卡號用途註記修改                                                                                          *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Crdp0020 extends BaseProc {
  Crdp0020Func func;
  int rr = -1;
  String msg = "";
  String dataKK1 = "";
  int ilOk = 0;
  int ilErr = 0;
  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        dataProcess();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      // case "A":
      // /* 新增功能 */
      // insertFunc();
      // break;
      // case "U":
      // /* 更新功能 */
      // updateFunc();
      // break;
      // case "D":
      // /* 刪除功能 */
      // deleteFunc();
      // break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_aprid", wp.loginUser);
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_grcode");
      dddwList("ex_grcode2", "ptr_group_code", "group_code", "group_name",
          "where 1=1  order by group_code");

      /*
       * wp.initOption = "--"; wp.optionKey = wp.item_ss("ex_user_id"); dddw_list("dddw_user_id",
       * "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id");
       */
    } catch (Exception ex) {
    }
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_crdtype");
      dddwList("ex_crdtype", "ptr_card_type", "card_type", "name", "where 1=1  order by card_type");
    } catch (Exception ex) {
    }
  }

  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_date1");
    String lsDate2 = wp.itemStr("ex_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[異動日期-起迄]  輸入錯誤");
      return -1;
    }
    if (empty(wp.itemStr("ex_charge")) == true) {
      alertErr2("需勾選主管覆核條件");
      return -1;
    } else if (wp.itemStr("ex_charge").equals("0")) {
      wp.whereStr = "where 1=1 ";
    } else if (wp.itemStr("ex_charge").equals("1")) {
      wp.whereStr = "where 1=1 and charge_date = ''";
    } else if (wp.itemStr("ex_charge").equals("2")) {
      wp.whereStr = "where 1=1 and charge_date <> ''";
    }

    if (empty(wp.itemStr("ex_date1")) == false) {
      wp.whereStr += " and trans_date >= :ex_date1 ";
      setString("ex_date1", wp.itemStr("ex_date1"));
    }
    if (empty(wp.itemStr("ex_date2")) == false) {
      wp.whereStr += " and trans_date <= :ex_date2 ";
      setString("ex_date2", wp.itemStr("ex_date2"));
    }
    if (empty(wp.itemStr("ex_grcode")) == false) {
      wp.whereStr += " and group_code = :ex_grcode ";
      setString("ex_grcode", wp.itemStr("ex_grcode"));
    }
    if (empty(wp.itemStr("ex_crdtype")) == false) {
      wp.whereStr += " and card_type = :ex_crdtype ";
      setString("ex_crdtype", wp.itemStr("ex_crdtype"));
    }

    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }
    if (empty(wp.itemStr("ex_card_flag")) == false) {
      wp.whereStr += " and card_flag = :ex_card_flag ";
      setString("ex_card_flag", wp.itemStr("ex_card_flag"));
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

    wp.selectSQL = "hex(rowid) as rowid, " + " trans_date, " + " card_type, " + " group_code, "
        + " bin_no, " + " beg_seqno, " + " end_seqno, " + " error_msg, " + " charge_date, "
        + " charge_id, " + " crt_date, " + " mod_seqno, " + " crt_user, " + " remark_40,"
        + " trans_no," + " card_flag ";

    wp.daoTable = "crd_cardno_range";

    wp.whereOrder = "";
    if (getWhereStr() != 1)
      return;
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata(wp.selectCnt);
  }

  @Override
  public void querySelect() throws Exception {
    dataKK1 = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {
    // -check approve-
    /*
     * if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) { return; }
     */
    String sendData = "";
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    func = new Crdp0020Func(wp);
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaTransDate = wp.itemBuff("trans_date");
    String[] aaGroupCode = wp.itemBuff("group_code");
    String[] aaCardType = wp.itemBuff("card_type");
    String[] opt = wp.itemBuff("opt");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaCrtDate = wp.itemBuff("crt_date");
    String[] aaTransNo = wp.itemBuff("trans_no");
    String[] aaChargeDate = wp.itemBuff("charge_date");
    wp.listCount[0] = aaTransDate.length;

    // -update-
    for (int ii = 0; ii < aaRowid.length; ii++) {
      func.varsSet("aa_rowid", aaRowid[ii]);
      func.varsSet("aa_trans_no", aaTransNo[ii]);
      func.varsSet("aa_crt_date", aaCrtDate[ii]);
      func.varsSet("aa_trans_date", aaTransDate[ii]);
      func.varsSet("aa_card_type", aaCardType[ii]);
      func.varsSet("aa_group_code", aaGroupCode[ii]);
      func.varsSet("aa_mod_seqno", aaModSeqno[ii]);
      if (opt.length > 1) {
        alertErr("一次只能覆核一筆");
        return;
      }
      if (checkBoxOptOn(ii, opt)) {
        if (!empty(aaChargeDate[ii])) {
          wp.colSet(ii, "ok_flag", "!");
          alertErr("主管已覆核完成,無法再覆核");
          return;
        }
        // // 2020-02-05 JustinWu
        // send_data = "CrdA001"+" "+aa_trans_no[ii]+" "+wp.loginUser;
        // rc = batch.call_Batch(send_data);
        // if (rc != 1) {
        // wp.col_set(ii, "ok_flag", "!");
        // batch.getMesg();
        // return;
        // }
        func.updateFunc();



        wp.colSet(ii, "ok_flag", "V");
        batch.getMesg();
      }
    }

  }


  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void listWkdata(int selectCnt) {
    String cardFlag = "";
    for (int ii = 0; ii < selectCnt; ii++) {

      cardFlag = wp.colStr(ii, "card_flag");
      wp.colSet(ii, "tt_card_flag", commString.decode(cardFlag, ",1,2,5,7", ",一般用,緊急替代用,HCE TPAN用,BANK PAY"));
    }
  }

  @Override
  public void showScreen(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.respHtml = wp.requHtml;
    wp.initFlag = "Y";
    initPage();
    dddwSelect();
    initButton();
  }

}
