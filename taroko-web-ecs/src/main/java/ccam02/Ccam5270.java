/*
 * 2019-1213  V1.00.01  Alex  fix queryRead
 * 
2020-0420  V1.00.02 yanghan 修改了變量名稱和方法名稱
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
 */
package ccam02;

import java.util.Arrays;

import ofcapp.BaseAction;

public class Ccam5270 extends BaseAction {
  String cardNote = "", entryModeType = "", web3dFlag = "", riskType = "", dataType1 = "";

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
    } else if (eqIgno(wp.buttonCode, "U2")) {
      // -存檔-明細資料-
      strAction = "U2";
      detl2Save();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    }

    else if (eqIgno(wp.buttonCode, "S2") || eqIgno(wp.buttonCode, "R2")) {
      strAction = "R2";
      detl2Read();

    }

    else if (eqIgno(wp.buttonCode, "L")) {
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
    try {
      if (eqIgno(wp.respHtml, "ccam5270")) {
        wp.optionKey = wp.colStr(0, "ex_risk_type");
        dddwAddOption("*", "全部");
        dddwList("dddw_risk_type", "vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
        wp.optionKey = wp.colStr(0, "ex_entry_mode_type");
        dddwList("dddw_entry_type",
            "select distinct entry_type as db_code , entry_type as db_desc from cca_entry_mode where 1=1 ");
        
        wp.optionKey = wp.colStr("ex_card_note");
      	dddwAddOption("*", "*_通用");      	
        dddwList("ddlb_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
        
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ccam5270_detl")) {
        wp.optionKey = wp.colStr(0, "kk_risk_type");
        dddwList("dddw_kk_risk_type", "vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
        
        wp.optionKey = wp.colStr("kk_card_note");
        dddwAddOption("*", "*_通用");  
        dddwList("ddlb_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ccam5270_detl")) {
        wp.optionKey = wp.colStr(0, "kk_entry_mode_type");
        dddwList("dddw_kk_entry_mode_type",
            "select distinct entry_type as db_code , entry_type as db_desc from cca_entry_mode where 1=1 ");
      }
    } catch (Exception ex) {
    }

    // try {
    // if (eq_igno(wp.respHtml,"ccam5270_detl")) {
    // wp.optionKey = wp.col_ss(0, "msg_id1");
    // dddw_list("dddw_msg_id1","sms_msg_id", "msg_id", "msg_desc", "where 1=1");
    // }
    // }
    // catch(Exception ex){}


    try {
      if (eqIgno(wp.respHtml, "ccam5270_risk1")) {
        // wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "ex_risk_type");
        dddwList("dw_spec_risk_type", "Vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
      }
    } catch (Exception ex) {
    }


  }


  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1" + sqlCol(wp.itemStr("ex_card_note"), "card_note")
        + sqlCol(wp.itemStr("ex_entry_mode_type"), "entry_mode_type")
        + sqlCol(wp.itemStr("ex_web3d_flag"), "web3d_flag")
        + sqlCol(wp.itemStr("ex_risk_type"), "risk_type");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " card_note , " + " decode(card_note,'*','通用',card_note) as tt_card_note , "
        + " entry_mode_type , " + " web3d_flag , "
        + " decode(web3d_flag,'1','一般交易','2','網路3D交易','3','網路非3D交易') as tt_web3d_flag , "
        + " risk_type , " + " decode(risk_type,'*','全部',risk_type) as tt_risk_type , "
        + " tx_amt , " + " msg_id1 , " + " mod_user , " + " cond1_mcc , " + " cond1_mcht , "
        + " cond1_risk , " + " decode(cond1_risk,'Y','col_key','') as cond1_risk_color , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " use_flag ";
    wp.daoTable = "cca_auth_sms";
    wp.whereOrder = " order by card_note, entry_mode_type, web3d_flag, risk_type ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  public void queryAfter() {
    String wkMode = "";
    String sql1 = " select " + " entry_mode " + " from cca_entry_mode " + " where entry_type = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkMode = "";
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "entry_mode_type")});

      if (sqlRowNum <= 0)
        continue;

      for (int rr = 0; rr < sqlRowNum; rr++) {
        if (rr == 0)
          wkMode += sqlStr(rr, "entry_mode");
        else
          wkMode += "," + sqlStr(rr, "entry_mode");
      }
      wp.colSet(ii, "wk_entry_mode", wkMode);
    }

  }

  @Override
  public void querySelect() throws Exception {
    cardNote = wp.itemStr("data_k1");
    entryModeType = wp.itemStr("data_k2");
    web3dFlag = wp.itemStr("data_k3");
    riskType = wp.itemStr("data_k4");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNote)) {
      cardNote = itemkk("card_note");
    }
    if (empty(entryModeType)) {
      entryModeType = wp.itemStr("entry_mode_type");
    }

    if (empty(web3dFlag)) {
      web3dFlag = wp.itemStr("web3d_flag");
    }
    if (empty(riskType)) {
      riskType = wp.itemStr("risk_type");
    }
    wp.selectSQL = "" + " card_note , " + " decode(card_note,'*','通用',card_note) as tt_card_note , "
        + " entry_mode_type , " + " web3d_flag , "
        + " decode(web3d_flag,'1','一般交易','2','網路3D交易','3','網路非3D交易') as tt_web3d_flag , "
        + " risk_type , " + " decode(risk_type,'*','全部',risk_type) as tt_risk_type , "
        + " msg_id1 , " + " tx_amt , " + " cond1_mcc , " + " cond1_mcht , " + " cond1_risk , "
        + " crt_user ," + " crt_date ," + " mod_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date," + " hex(rowid) as rowid , mod_seqno , "
        + " use_flag ";
    wp.daoTable = "cca_auth_sms";
    wp.whereStr = "where 1=1" + sqlCol(cardNote, "card_note") + sqlCol(entryModeType, "entry_mode_type")
        + sqlCol(web3dFlag, "web3d_flag") + sqlCol(riskType, "risk_type");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNote);
    }


  }

  void detl2Read() throws Exception {
    cardNote = wp.itemStr("data_k1");
    entryModeType = wp.itemStr("data_k2");
    web3dFlag = wp.itemStr("data_k3");
    riskType = wp.itemStr("data_k4");
    dataType1 = wp.itemStr("data_k5");

    if (empty(cardNote)) {
      cardNote = wp.itemStr("card_note");
    }
    if (empty(entryModeType)) {
      entryModeType = wp.itemStr("entry_mode_type");
    }
    if (empty(web3dFlag)) {
      web3dFlag = wp.itemStr("web3d_flag");
    }
    if (empty(riskType)) {
      riskType = wp.itemStr("risk_type");
    }
    if (empty(dataType1)) {
      dataType1 = wp.itemStr("data_type1");
    }

    wp.selectSQL = " data_type, " + " data_code1, " + " data_code2, " + " data_code3 ";
    wp.daoTable = " cca_auth_smsdetl ";
    wp.whereStr = " where 1=1 " + sqlCol(cardNote, "card_note") + sqlCol(entryModeType, "entry_mode_type")
        + sqlCol(web3dFlag, "web3d_flag") + sqlCol(riskType, "risk_type") + sqlCol(dataType1, "data_type");

    pageQuery();
    if (sqlNotFind()) {
      wp.notFound = "N";
    }

    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);

    // --
    if (eqIgno(web3dFlag, "1")) {
      wp.colSet("tt_web3d_flag", "一般交易");
    } else if (eqIgno(web3dFlag, "2")) {
      wp.colSet("tt_web3d_flag", "網路3D交易");
    } else if (eqIgno(web3dFlag, "3")) {
      wp.colSet("tt_web3d_flag", "網路非3D交易");
    }

  }

  @Override
  public void saveFunc() throws Exception {
    ccam02.Ccam5270Func func = new ccam02.Ccam5270Func();
    func.setConn(wp);
    if (checkApproveZz() == false) {
      return;
    }
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    }
    this.saveAfter(false);
  }

  void detl2Save() throws Exception {
    int isOk = 0, isError = 0, ii = 0;

    ccam02.Ccam5270Func func = new ccam02.Ccam5270Func();
    func.setConn(wp);
    String[] code1 = wp.itemBuff("data_code1");
    String[] code2 = wp.itemBuff("data_code2");
    String[] code3 = wp.itemBuff("data_code3");
    String[] type = wp.itemBuff("data_type");
    String[] opt = wp.itemBuff("opt");
    // --
    wp.listCount[0] = wp.itemRows("data_code1");
    wp.colSet("IND_NUM", "" + code1.length);
    if (checkApproveZz() == false) {
      return;
    }
    // -check duplication-
    ii = -1;
    for (String ss : code1) {
      ii++;
      wp.colSet(ii, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ii, opt)) {
        continue;
      }

      if (ii != Arrays.asList(code1).indexOf(ss)) {
        wp.colSet(ii, "ok_flag", "!");
        isError++;
      }
    }
    if (isError > 0) {
      alertErr("資料值重複: " + isError);
      return;
    }

    // -delete no-approve-
    if (func.dbDeleteDetl() < 0) {
      alertErr(func.getMsg());
      return;
    }
    
    //String ss2 = "";
    for (int rr=0;rr<wp.itemRows("data_code1");rr++) {      
      if (checkBoxOptOn(rr, opt)) {
        continue;
      }
      
      func.varsSet("data_code1", code1[rr]);
      func.varsSet("data_code2", code2[rr]);
      func.varsSet("data_code3", code3[rr]);
      func.varsSet("data_type", type[rr]);

      if (func.dbInsertDetl() == 1) {
        isOk++;
      } else {
        isError++;
        dbRollback();
        wp.colSet(ii, "ok_flag", "X");
        errmsg(func.getMsg());
        return;
      }
    }
    if (isOk > 0) {
      sqlCommit(1);
    }
    rc = func.updateMcc1();
    sqlCommit(rc);
    rc = func.updateMcht1();
    sqlCommit(rc);
    rc = func.updateRisk1();
    sqlCommit(rc);

    alertMsg("資料存檔處理完成; OK=" + isOk + ", ERR=" + isError);
    // detl2_Read();
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    if (eqIgno(strAction, "new")) {
      wp.colSet("use_flag", "Y");
    }

  }

}
