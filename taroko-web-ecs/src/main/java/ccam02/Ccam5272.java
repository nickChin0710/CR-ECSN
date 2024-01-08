/*
 * 2019-1213  V1.00.01  Alex  fix queryRead
 * 
2020-0420  V1.00.01 yanghan 修改了變量名稱和方法名稱
 */
package ccam02;

import java.util.Arrays;

import ofcapp.BaseAction;

public class Ccam5272 extends BaseAction {
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
      if (eqIgno(wp.respHtml, "ccam5272")) {        
        wp.optionKey = wp.colStr("ex_card_note");
      	dddwAddOption("*", "*_通用");      	
        dddwList("ddlb_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
        wp.optionKey = wp.colStr("ex_web3d_flag");
        dddwAddOption("OA", "OA_網路授權交易");
        dddwList("ddlb_web3d", "cca_sys_parm3", "sys_key", "sys_data1", "where 1=1 and sys_id = 'TRANCODE'");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ccam5272_detl")) {                
        wp.optionKey = wp.colStr("kk_card_note");
        dddwAddOption("*", "*_通用");  
        dddwList("ddlb_card_note", "ptr_sys_idtab", "wf_id", "wf_desc","where wf_type='CARD_NOTE'");
        wp.optionKey = wp.colStr("kk_web3d_flag");
        dddwAddOption("OA", "OA_網路授權交易");
        dddwList("ddlb_web3d", "cca_sys_parm3", "sys_key", "sys_data1", "where 1=1 and sys_id = 'TRANCODE'");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "ccam5272_detl")) {
        wp.optionKey = wp.colStr(0, "msg_id1");
        dddwList("dddw_msg_id1", "sms_msg_id", "msg_id", "msg_desc", "where 1=1");
        wp.optionKey = wp.colStr(0, "msg_id2");
        dddwList("dddw_msg_id2", "sms_msg_id", "msg_id", "msg_desc", "where 1=1");
      }
    } catch (Exception ex) {
    }


    try {
      if (eqIgno(wp.respHtml, "ccam5272_risk1")) {
        // wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "ex_risk_type");
        dddwList("dw_spec_risk_type", "Vcca_risk_type", "risk_type", "risk_desc", "where 1=1");
      }
      if (eqIgno(wp.respHtml, "ccam5272_risk2")) {
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
//        + sqlCol(wp.itemStr("ex_entry_mode_type"), "entry_mode_type")
        + sqlCol(wp.itemStr("ex_web3d_flag"), "web3d_flag")
//        + sqlCol(wp.itemStr("ex_risk_type"), "risk_type")
        ;

    if (wp.itemEq("ex_sms_flag", "1")) {
      lsWhere += " and cond1_yn = 'Y' ";
    } else if (wp.itemEq("ex_sms_flag", "2")) {
      lsWhere += " and cond2_yn = 'Y' ";
    } else if (wp.itemEq("ex_sms_flag", "3")) {
      lsWhere += " and cond2_yn = 'Y' and cond1_yn = 'Y' ";
    } else if (wp.itemEq("ex_sms_flag", "4")) {
      lsWhere += " and cond2_yn <> 'Y' and cond1_yn <> 'Y' ";
    }

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
        + " risk_type , " + " decode(risk_type,'*','全部',risk_type) as tt_risk_type , "
        + " dd_tx_times , " + " msg_id1 , " + " cond2_resp1 , " + " cond2_resp2 ,  "
        + " cond1_risk , " + " decode(cond1_risk,'Y','col_key','') as cond1_risk_color , "
        + " cond2_risk , " + " decode(cond2_risk,'Y','col_key','') as cond2_risk_color , "
        + " msg_id2 ,  " + " mod_user , " + " to_char(mod_time,'yyyymmdd') as mod_date , "
        + " cond1_yn , " + " cond2_yn , "
        + " decode(cond1_yn,'Y','col_key','') as cond1_yn_color , "
        + " decode(cond2_yn,'Y','col_key','') as cond2_yn_color ";
    wp.daoTable = "cca_auth_sms2_parm";
    wp.whereOrder = " order by card_note, web3d_flag ";
//    wp.whereOrder = " order by card_note, entry_mode_type, web3d_flag, risk_type ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
    queryAfter();
  }

  public void queryAfter() {    
    String sql1 = "select sys_data1 from cca_sys_parm3 where sys_key = ? and sys_id = 'TRANCODE' ";
    String sql2 = " select count(*) as db_cnt from cca_auth_sms2_detl "
        + " where card_note = ? "
        + " and web3d_flag = ? "
        + " and data_type = 'SMS2' ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {      
	  sqlSelect(sql2, new Object[] {wp.colStr(ii, "card_note"),wp.colStr(ii, "web3d_flag")});
      if (sqlNum("db_cnt") > 0)
        wp.colSet(ii, "tt_resp_code2", "Y");
      else
        wp.colSet(ii, "tt_resp_code2", "N");
      
      if(wp.colEq(ii, "web3d_flag","OA")) {
    	  wp.colSet(ii,"tt_web3d_flag", "網路授權交易");    	  
      }	else	{
    	  sqlSelect(sql1,new Object[] {wp.colStr(ii,"web3d_flag")});
          if(sqlRowNum >0) {
        	  wp.colSet(ii,"tt_web3d_flag",sqlStr("sys_data1"));
          }	else	{
        	  wp.colSet(ii,"tt_web3d_flag","");
          }
      }            
    }
  }

  @Override
  public void querySelect() throws Exception {
    cardNote = wp.itemStr("data_k1");
//    entryModeType = wp.itemStr("data_k2");
    web3dFlag = wp.itemStr("data_k2");
//    riskType = wp.itemStr("data_k4");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNote)) {
      cardNote = itemkk("card_note");
    }

    if (empty(web3dFlag)) {
      web3dFlag = itemkk("web3d_flag");
    }

    wp.selectSQL = "" + " A.* , " + " to_char(A.mod_time,'yyyymmdd') as mod_date,"
        + " hex(A.rowid) as rowid , A.mod_seqno , "
        + " decode(A.card_note,'*','通用',A.card_note) as tt_card_note , "
        + " decode(A.web3d_flag,'1','一般交易','2','網路3D交易','3','網路非3D交易') as tt_web3d_flag , "
        + " decode(A.risk_type,'*','全部',A.risk_type) as tt_risk_type ";
    wp.daoTable = "cca_auth_sms2_parm A";
    wp.whereStr =
        "where 1=1" + sqlCol(cardNote, "A.card_note")
        + sqlCol(web3dFlag, "A.web3d_flag")
        ;
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNote);
      return ;
    }
    
    //--
    if(wp.colEq("web3d_flag", "OA")) {
    	wp.colSet("tt_web3d_flag","網路授權交易");    	
    }	else	{
    	String sql1 = "select sys_data1 from cca_sys_parm3 where sys_key = ? and sys_id = 'TRANCODE' ";
        sqlSelect(sql1,new Object[] {wp.colStr("web3d_flag")});
        if(sqlRowNum >0) {
      	  wp.colSet("tt_web3d_flag",sqlStr("sys_data1"));
        }	else	{
      	  wp.colSet("tt_web3d_flag","");
        }
    }            
  }

  void detl2Read() throws Exception {
    cardNote = wp.itemStr("data_k1");
//    entryModeType = wp.itemStr("data_k2");
    web3dFlag = wp.itemStr("data_k2");
//    riskType = wp.itemStr("data_k4");
    dataType1 = wp.itemStr("data_k5");

    if (empty(cardNote)) {
      cardNote = wp.itemStr("card_note");
    }
//    if (empty(entryModeType)) {
//      entryModeType = wp.itemStr("entry_mode_type");
//    }
    if (empty(web3dFlag)) {
      web3dFlag = wp.itemStr("web3d_flag");
    }
//    if (empty(riskType)) {
//      riskType = wp.itemStr("risk_type");
//    }
    if (empty(dataType1)) {
      dataType1 = wp.itemStr("data_type1");
    }

    wp.selectSQL = " data_type, " + " data_code1 , " + " data_code2  ";
    wp.daoTable = " cca_auth_sms2_detl ";
    wp.whereStr = " where 1=1 " + sqlCol(cardNote, "card_note")
//        + sqlCol(entryModeType, "entry_mode_type")
        + sqlCol(web3dFlag, "web3d_flag")
//        + sqlCol(riskType, "risk_type")
        + sqlCol(dataType1, "data_type");

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
    ccam02.Ccam5272Func func = new ccam02.Ccam5272Func();
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

    ccam02.Ccam5272Func func = new ccam02.Ccam5272Func();
    func.setConn(wp);
    String[] code1 = wp.itemBuff("data_code1");
    String[] code2 = wp.itemBuff("data_code2");
    String[] type = wp.itemBuff("data_type");
    String[] opt = wp.itemBuff("opt");
    // --
    wp.listCount[0] = code1.length;
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

    ii = -1;
    String dataType = "";
    for (String dataCode1 : code1) {
      ii++;
      if (checkBoxOptOn(ii, opt)) {
        continue;
      }

      dataType = "";
      if (ii < type.length) {
        dataType = type[ii];
      }
      if (empty(dataCode1) && empty(dataType)) {
        ii++;
        continue;
      }
      func.varsSet("data_code1", dataCode1);
      func.varsSet("data_type", dataType);
      func.varsSet("data_code2", code2[ii]);
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

    rc = func.updateDetl();
    sqlCommit(rc);


    alertMsg("資料存檔處理完成; OK=" + isOk + ", ERR=" + isError);
    detl2Read();
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
    // if(eq_igno(is_action,"new")){
    // wp.col_set("cond1_yn", "Y");
    // wp.col_set("cond2_yn", "Y");
    // }

  }

}
