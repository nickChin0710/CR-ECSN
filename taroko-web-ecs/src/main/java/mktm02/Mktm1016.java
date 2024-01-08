/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-09  V1.00.00  yash       program initial                            *
* 109-03-09  V1.10.00  yanghan    Add two rows in bil_dodo_parm              * 
* 109-04-24  V1.00.01  shiyuqi    updated for project coding standard        *  
* 109-11-18  V1.00.01  Kirin      bilm1110移至MKT02,並更名mktm1016           *
* 110/1/4    V1.00.01  yanghan       修改了變量名稱和方法名稱            
* 111-07-22  V1.00.01  machao     update 修改卡種，存檔出現Error*
******************************************************************************/

package mktm02;

import java.util.Arrays;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;

public class Mktm1016 extends BaseEdit {
  String actionCd = "";
  String cardKk1 = "", cardKk2 = "";
  String groupKk1 = "", groupKk2 = "";
  String mchtKk1 = "", mchtKk2 = "";
  String mcht2Kk1 = "", mcht2Kk2 = "";

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
    } else if (eqIgno(wp.buttonCode, "SC")) {
      /* 查詢card */
      strAction = "SC";

      selectCard();

    } else if (eqIgno(wp.buttonCode, "SG")) {
      /* 查詢group */
      strAction = "SG";
      selectGroup();
    } else if (eqIgno(wp.buttonCode, "SM")) {
      /* 查詢group */
      strAction = "SM";
      selectMcht();
    } else if (eqIgno(wp.buttonCode, "SM2")) {
      /* 查詢group */
      strAction = "SM2";
      selectMcht2();
    } else if (eqIgno(wp.buttonCode, "SCU")) {
      /* card存檔 */
      strAction = "SCU";
      updateCard();
    } else if (eqIgno(wp.buttonCode, "SGU")) {
      /* group存檔 */
      strAction = "SGU";
      updateGroup();
    } else if (eqIgno(wp.buttonCode, "SMU")) {
      /* mcht存檔 */
      strAction = "SMU";
      updateMcht();
    } else if (eqIgno(wp.buttonCode, "SMU2")) {
      /* mcht2存檔 */
      strAction = "SMU2";
      updateMcht2();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      /* mcht匯入 */
      strAction = "UPLOAD";
      procUploadFile();
    }
    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_action_cd")) == false) {
      wp.whereStr += " and  action_cd = :action_cd ";
      setString("action_cd", wp.itemStr("ex_action_cd"));
    }

    // if(wp.item_ss("ex_apr_flag").equals("Y")){
    // wp.whereStr += " and apr_flag = 'Y' ";
    // }
    // else if(wp.item_ss("ex_apr_flag").equals("N")){
    // wp.whereStr += " and apr_flag = 'N' ";
    // }

    if (empty(wp.itemStr("ex_crt_date1")) == false) {
      wp.whereStr += " and crt_date >= :crt_date1 ";
      setString("crt_date1", wp.itemStr("ex_crt_date1"));
    }
    if (empty(wp.itemStr("ex_crt_date2")) == false) {
      wp.whereStr += " and crt_date <= :crt_date2 ";
      setString("crt_date2", wp.itemStr("ex_crt_date2"));
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    String lsDate1 = wp.itemStr("ex_crt_date1");
    String lsDate2 = wp.itemStr("ex_crt_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[登錄日期-起迄]  輸入錯誤");
      return;
    }

    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " action_cd" + ", document_desc" + ", car_hours" + ", crt_date" + ", crt_user"
        + ", apr_user" + ", apr_date";

    wp.daoTable = "bil_dodo_parm";
    wp.whereOrder = " order by action_cd";
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
    actionCd = wp.itemStr("kk_action_cd");
    if (empty(actionCd)) {
      actionCd = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ", action_cd " + ", document_desc"
        + ", car_hours" + ", charge_amt" + ", ivr_flag" + ", document" + ", group_code_flag"
        + ", total_bonus" + ", ext_batch_no" + ", consume_method_1" + ", consume_period_1"
        + ", date_fm_1" + ", date_to_1" + ", limit_days_1" + ", consume_amt_fm_1"
        + ", consume_amt_to_1" + ", consume_cnt_1" + ", consume_method_2" + ", consume_period_2"
        + ", date_fm_2" + ", date_to_2" + ", limit_days_2" + ", consume_amt_fm_2"
        + ", consume_amt_to_2" + ", consume_cnt_2" + ", card_type_flag" + ", item_ename_bl_1"
        + ", item_ename_it_1" + ", item_ename_ca_1" + ", item_ename_id_1" + ", item_ename_ao_1"
        + ", item_ename_ot_1" + ", item_ename_bl_2" + ", item_ename_it_2" + ", item_ename_ca_2"
        + ", item_ename_id_2" + ", item_ename_ao_2" + ", item_ename_ot_2" + ", it_1_type "// 新增兩列
                                                                                          // 首年分期付款(IT)類別
        + ", it_2_type"// 次年分期付款(IT)類別
        + ", mcht_no_1" + ", mcht_no_2" + ", apr_user" + ", apr_date" + ", crt_date" + ", crt_user";
    wp.daoTable = "bil_dodo_parm";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  action_cd = :action_cd ";
    setString("action_cd", actionCd);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, action_cd=" + actionCd);
    }
    setSelectLimit(0);
    String isSql = "select * from bil_dodo_bn_data where action_cd= ? ";
    Object[] param = new Object[] {actionCd};
    this.sqlSelect(isSql, param);
    if (this.sqlNotFind()) {
      wp.alertMesg = "<script language='javascript'> alert('卡種尚未新增!')</script>";
    }
  }

  @Override
  public void saveFunc() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    if (wp.itemStr("ivr_flag").equals("0") && empty(wp.itemStr("document"))) {
      alertErr("IVR不可空白!");
      return;
    }
    if (!empty(wp.itemStr("date_fm_1")) && !empty(wp.itemStr("date_to_1"))) {
      if (chkStrend(wp.itemStr("date_fm_1"), wp.itemStr("date_to_1")) == false) {
        alertErr("核卡首年區間起日不可大於迄日!!");
        return;
      }
    }
    if (!empty(wp.itemStr("date_fm_2")) && !empty(wp.itemStr("date_to_2"))) {
      if (chkStrend(wp.itemStr("date_fm_2"), wp.itemStr("date_to_2")) == false) {
        alertErr("核卡次年區間起日不可大於迄日!!");
        return;
      }
    }

    if (strAction.equals("U")) {
      setSelectLimit(0);
      String lsSql = "select *  " + "from bil_dodo_bn_data  " + " where action_cd  = :action_cd   "
          + "   and data_type  = '01'  ";

      setString("action_cd", wp.itemStr("action_cd"));
      sqlSelect(lsSql);

      if (sqlRowNum <= 0) {
        alertErr("卡種不可空白!");
        return;
      }
    }

    Mktm1016Func func = new Mktm1016Func(wp);

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
      if (wp.respHtml.indexOf("_detl") > 0)
        wp.optionKey = wp.itemStr("kk_action_cd");
      else
        wp.optionKey = wp.itemStr("ex_action_cd");
      this.dddwList("dddw_action_cd", "bil_dodo_parm", "action_cd", "",
          "where 1=1 group by action_cd order by action_cd");

      // for (int ii = 0; ii < wp.selectCnt; ii++) {
      // wp.initOption = "--";
      // // wp.optionKey = wp.col_ss(ii,"data_code");
      // this.dddw_list(ii, "dddw_data_code", "ptr_card_type",
      // "card_type", "name", "where 1=1 order by card_type");
      // wp.initOption = "--";
      // wp.optionKey = wp.col_ss(ii, "data_code2");
      // this.dddw_list(ii, "dddw_data_code2", "ptr_group_code",
      // "group_code", "group_name", "where 1=1 order by group_code");
      // }

      wp.initOption = "--";
      this.dddwList("dddw_data_code", "ptr_card_type", "card_type", "name",
          "where 1=1  order by card_type");
      wp.initOption = "--";
      this.dddwList("dddw_data_code2", "ptr_group_code", "group_code", "group_name",
          "where 1=1  order by group_code");

      wp.initOption = "--";
      this.dddwList("card_data_code", "ptr_card_type", "card_type", "name",
          "where 1=1  order by card_type");
      wp.initOption = "--";
      this.dddwList("group_data_code", "ptr_group_code", "group_code", "group_name",
          "where 1=1  order by group_code");
    } catch (Exception ex) {
    }
  }

  public void selectCard() throws Exception {
    // this.select_noLimit();
    setSelectLimit(0);
    cardKk2 = wp.itemStr("card_kk2");
    if (empty(cardKk2))
      cardKk2 = itemKk("data_k2");
    wp.colSet("card_kk2", cardKk2);

    cardKk1 = wp.itemStr("card_kk1");
    if (empty(cardKk1))
      cardKk1 = itemKk("data_k1");
    wp.colSet("card_kk1", cardKk1);

    wp.selectSQL = "hex(rowid) as rowid1 " + ", data_type" + ", data_code"
        + ", (select name from ptr_card_type where card_type = bil_dodo_bn_data.data_code) chi_name "
        + ", apr_flag" + ", type_desc";
    wp.daoTable = "bil_dodo_bn_data";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  action_cd = :action_cd and data_type = '01'";
    setString("action_cd", cardKk1);

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

  }

  public void selectGroup() throws Exception {
    this.selectNoLimit();
    setSelectLimit(0);
    groupKk1 = wp.itemStr("group_kk1");
    if (empty(groupKk1))
      groupKk1 = itemKk("data_k1");
    wp.colSet("group_kk1", groupKk1);

    groupKk2 = wp.itemStr("group_kk2");
    if (empty(groupKk2))
      groupKk2 = itemKk("data_k2");
    wp.colSet("group_kk2", groupKk2);

    wp.selectSQL = "hex(rowid) as rowid2 " + ", data_type" + ", data_code as data_code2"
        + ", (select group_name from ptr_group_code where group_code = bil_dodo_bn_data.data_code) chi_name "
        + ", apr_flag" + ", type_desc";
    wp.daoTable = "bil_dodo_bn_data";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  action_cd = :action_cd and data_type = '02'";
    setString("action_cd", groupKk1);

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
  }

  public void selectMcht() throws Exception {
    // selectMcht
    this.selectNoLimit();

    mchtKk1 = wp.itemStr("mcht_kk1");
    if (empty(mchtKk1))
      mchtKk1 = itemKk("data_k1");
    wp.colSet("mcht_kk1", mchtKk1);

    mchtKk2 = wp.itemStr("mcht_kk2");
    if (empty(mchtKk2))
      mchtKk2 = itemKk("data_k2");
    wp.colSet("mcht_kk2", mchtKk2);

    wp.selectSQL =
        "hex(rowid) as rowid3 " + ", data_type" + ", data_code" + ", apr_flag" + ", type_desc";
    wp.daoTable = "bil_dodo_bn_data";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  action_cd = :action_cd and data_type = '03'";
    setString("action_cd", mchtKk1);

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
    wp.colSet("row_ct", wp.selectCnt);
  }

  public void selectMcht2() throws Exception {
    // selectMcht
    this.selectNoLimit();
    setSelectLimit(0);
    mcht2Kk1 = wp.itemStr("mcht2_kk1");
    if (empty(mcht2Kk1))
      mcht2Kk1 = itemKk("data_k1");
    wp.colSet("mcht2_kk1", mcht2Kk1);

    mcht2Kk2 = wp.itemStr("mcht2_kk2");
    if (empty(mcht2Kk2))
      mcht2Kk2 = itemKk("data_k2");
    wp.colSet("mcht2_kk2", mcht2Kk2);

    wp.selectSQL =
        "hex(rowid) as rowid4 " + ", data_type" + ", data_code" + ", apr_flag" + ", type_desc";
    wp.daoTable = "bil_dodo_bn_data";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  action_cd = :action_cd and data_type = '04'";
    setString("action_cd", mcht2Kk1);

    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";
    wp.colSet("row_ct", wp.selectCnt);
  }

  public void updateCard() throws Exception {
    Mktm1016Func func = new Mktm1016Func(wp);
    int llOk = 0, llErr = 0;
    String[] aaDataCode = wp.itemBuff("data_code");
    String[] aaRowid1 = wp.itemBuff("rowid1");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaDataCode.length;
    String lsSql = "", dsSql = "";
    String mCardType = "", mActionCd = "";
    // wp.col_set("IND_NUM", "" + aa_data_code.length);
    // -insert-
    mActionCd = wp.itemStr("card_kk1");
    if (empty(mActionCd))
      return;
    dsSql = "delete bil_dodo_bn_data" + " where action_cd =:action_cd and data_type = '01' ";
    setString("action_cd", mActionCd);
    sqlExec(dsSql);
    if (sqlRowNum < 0) {
      alertErr("資料處理異常:Delete bil_dodo_bn_data error!!");
      sqlCommit(0);
      return;
    }

    for (int ll = 0; ll < aaDataCode.length; ll++) {
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }
      // check card_type
      mCardType = aaDataCode[ll];
      lsSql = "select card_type " + "from ptr_card_type " + "where 1=1 ";
      lsSql += sqlCol(mCardType, "card_type");
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "卡種不存在 !!");
        llErr++;
        continue;
      }
      // check empty
      if (empty(aaDataCode[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "卡種資料空白 !!");
        llErr++;
        continue;
      }
      // -check duplication-
      if (ll != Arrays.asList(aaDataCode).indexOf(aaDataCode[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "卡種資料值重複 !!");
        llErr++;
        continue;
      }
      func.varsSet("aa_data_code", aaDataCode[ll]);
//      func.varsSet("aa_rowid1", aaRowid1[ll]);

      // -delete no-approve-
      // if (func.dbDelete2() < 0) {
      // alert_err(func.getMsg());
      // return;
      // }

      if (func.dbInsert2() == 1) {
        llOk++;
      } else {
        llErr++;
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }

    alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);

    // SAVE後 SELECT
    // selectCard();

  }

  public void updateGroup() throws Exception {
    Mktm1016Func func = new Mktm1016Func(wp);
    int llOk = 0, llErr = 0;
    String[] aaDataCode2 = wp.itemBuff("data_code2");
    String[] aaRowid2 = wp.itemBuff("rowid2");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaDataCode2.length;
    // wp.col_set("IND_NUM", "" + aa_data_code.length);
    // -insert-
    String lsSql = "", dsSql = "";
    String mGroupCode = "", mActionCd = "";
    // wp.col_set("IND_NUM", "" + aa_data_code.length);
    // -insert-
    mActionCd = wp.itemStr("group_kk1");
    if (empty(mActionCd))
      return;
    dsSql = "delete bil_dodo_bn_data" + " where action_cd =:action_cd and data_type = '02' ";
    setString("action_cd", mActionCd);
    sqlExec(dsSql);
    if (sqlRowNum < 0) {
      alertErr("資料處理異常:Delete bil_dodo_bn_data error!!");
      sqlCommit(0);
      return;
    }

    for (int ll = 0; ll < aaDataCode2.length; ll++) {
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }
      // check empty
      if (empty(aaDataCode2[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "團代資料空白 !!");
        llErr++;
        continue;
      }
      // check card_type
      mGroupCode = aaDataCode2[ll];
      lsSql = "select group_code " + "from ptr_group_code " + "where 1=1 ";
      lsSql += sqlCol(mGroupCode, "group_code");
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "團代不存在 !!");
        llErr++;
        continue;
      }

      // -check duplication-
      if (ll != Arrays.asList(aaDataCode2).indexOf(aaDataCode2[ll])) {
        wp.colSet(ll, "ok_flag", "X");
        wp.colSet(ll, "err_mesg", "團代資料值重複 !!");
        llErr++;
        continue;
      }
      func.varsSet("aa_data_code2", aaDataCode2[ll]);
      func.varsSet("aa_rowid2", aaRowid2[ll]);

      // -delete no-approve-
      // if (func.dbDelete3() < 0) {
      // alert_err(func.getMsg());
      // return;
      // }

      if (func.dbInsert3() == 1) {
        llOk++;
      } else {
        llErr++;
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }

    alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
    // SAVE後 SELECT

    // selectGroup();

  }

  public void updateMcht() throws Exception {
    Mktm1016Func func = new Mktm1016Func(wp);
    int llOk = 0, llErr = 0;
    String[] aaDataCode3 = wp.itemBuff("data_code3");
    String[] aaRowid3 = wp.itemBuff("rowid3");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaDataCode3.length;
    // wp.col_set("IND_NUM", "" + aa_data_code.length);
    // -insert-

    for (int ll = 0; ll < aaDataCode3.length; ll++) {

      func.varsSet("aa_data_code3", aaDataCode3[ll]);
      func.varsSet("aa_rowid3", aaRowid3[ll]);

      // -delete no-approve-
      if (func.dbDelete4() < 0) {
        alertErr(func.getMsg());
        return;
      }

      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {

        continue;
      }

      if (func.dbInsert4() == 1) {
        llOk++;
      } else {
        llErr++;
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }

    alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
    // SAVE後 SELECT

    selectMcht();

  }

  public void updateMcht2() throws Exception {
    Mktm1016Func func = new Mktm1016Func(wp);
    int llOk = 0, llErr = 0;
    String[] aaDataCode4 = wp.itemBuff("data_code4");
    String[] aaRowid4 = wp.itemBuff("rowid4");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaDataCode4.length;
    // wp.col_set("IND_NUM", "" + aa_data_code.length);
    // -insert-

    for (int ll = 0; ll < aaDataCode4.length; ll++) {

      func.varsSet("aa_data_code4", aaDataCode4[ll]);
      func.varsSet("aa_rowid4", aaRowid4[ll]);

      // -delete no-approve-
      if (func.dbDelete5() < 0) {
        alertErr(func.getMsg());
        return;
      }

      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {

        continue;
      }

      if (func.dbInsert5() == 1) {
        llOk++;
      } else {
        llErr++;
      }

      // 有失敗rollback，無失敗commit
      sqlCommit(llOk > 0 ? 1 : 0);
    }

    alertMsg("資料存檔處理完成; OK = " + llOk + ", ERR = " + llErr);
    // SAVE後 SELECT

    selectMcht2();

  }

  public void procUploadFile() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }
    fileDataImp();
  }

  int fileUpLoad() {
    TarokoUpload func = new TarokoUpload();
    try {
      func.actionFunction(wp);
      wp.colSet("zz_file_name", func.fileName);
    } catch (Exception ex) {
      wp.log("file_upLoad: error=" + ex.getMessage());
      return -1;
    }

    return func.rc;
  }

  void fileDataImp() {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile = wp.itemStr("zz_file_name");

    // int fi = tf.openInputText(inputFile,"UTF-8"); //決定上傳檔內碼
    int fi = tf.openInputText(inputFile, "MS950");
    if (fi == -1) {
      return;
    }
    Mktm1016Func func = new Mktm1016Func(wp);
    func.setConn(wp);

    String lsSql = "", dataType = "", actionCd = "";
    actionCd = wp.itemStr("action_cd");
    dataType = itemKk("data_k1");
    int llOk = 0, llErr = 0, llCnt = 0;
    wp.logSql = false;
    if (func.dbDeleteUpload(actionCd, dataType) != 1) {
      alertErr("資料刪除失敗!!");
      return;
    }
    while (true) {
      String tmpStr = "";
      try {
        tmpStr = tf.readTextFile(fi);
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (tmpStr.length() < 2) {
        continue;
      }

      llCnt++;
      String[] splitLine = tmpStr.split(",");

      try {
        String ccDataCode = splitLine[0];// data_code特店代號
        // check

        func.varsSet("aa_data_code", ccDataCode);
        func.varsSet("aa_data_type", dataType);
        // server debug message ==>只會顯示最後一筆訊息
        // wp.alertMesg = "<script
        // language='javascript'>alert('"+cc_data_code+"')</script>";
        if (func.dbInsertCode(actionCd, dataType) != 1) {
          llErr++;
          continue;
        }

        if (rc < 0) {
          llErr++;
        } else {
          llOk++;
        }
        // 固定長度上傳檔
        // wp.item_set("id_no",commString.mid_big5(ss,0,10));
        // wp.item_set("data_flag1",commString.mid_big5(ss,10,1));
        // wp.item_set("data_flag2",commString.mid_big5(ss,11,1));

      } catch (Exception e) {
        alertMsg("匯入資料異常!!");
        return;
      }
    }
    // wp.listCount[0]=ll_cnt; //--->開啟上傳檔檢視
    tf.closeInputText(fi);
    try {
      tf.deleteFile(inputFile);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk + ", 失敗筆數=" + llErr);
    // queryRead();
  }

  void fileDataImp2() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String inputFile2 = wp.itemStr("zz_file_name");
    int fi2 = tf.openInputText(inputFile2, "UTF-8"); // 決定上傳檔內碼
    // int fi = tf.openInputText(inputFile,"MS950");
    if (fi2 == -1) {
      return;
    }

    Mktm1016Func func = new Mktm1016Func(wp);

    String tmpStr = "";
    int llOk = 0, llCnt = 0, llErr = 0;
    while (true) {
      tmpStr = tf.readTextFile(fi2);
      if (tf.endFile[fi2].equals("Y"))
        break;

      if (tmpStr.length() < 2)
        continue;

      // wp.alertMesg = "<script language='javascript'>
      // alert('"+ss+"')</script>";

      llCnt++;
      if (llCnt == 1) {
        if (func.dbDeleteUpload(wp.itemStr("mcht2_kk1"), "04") < 0) {
          alertErr(func.getMsg());
          sqlCommit(0);
          return;
        }
      }

      func.varsSet("u_data_code", tmpStr);

      if (func.dbInsertCode(wp.itemStr("mcht2_kk1"), "04") == 1) {
        llOk++;
      } else {
        llErr++;
      }

      sqlCommit(llOk > 0 ? 1 : 0);
    }

    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功 = " + llOk + ", 重複 = " + llErr);

    tf.closeInputText(fi2);
    tf.deleteFile(inputFile2);

    selectMcht2();

    return;
  }

}
