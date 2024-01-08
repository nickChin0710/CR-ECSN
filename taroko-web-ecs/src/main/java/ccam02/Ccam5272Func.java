/*
 * 2019-1213  V1.00.01  Alex  fix dataCheck
 * 
2020-0420  V1.00.01 yanghan 修改了變量名稱和方法名稱
 */
package ccam02;

import busi.FuncAction;


public class Ccam5272Func extends FuncAction {
  String cardNote = "", entryModeType = "", web3dFlag = "", riskType = "";

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      cardNote = wp.itemStr("kk_card_note");
//      entryModeType = wp.itemStr("kk_entry_mode_type");
      web3dFlag = wp.itemStr("kk_web3d_flag");
//      riskType = wp.itemStr("kk_risk_type");
    } else {
      cardNote = wp.itemStr("card_note");
//      entryModeType = wp.itemStr("entry_mode_type");
      web3dFlag = wp.itemStr("web3d_flag");
//      riskType = wp.itemStr("risk_type");
    }

    if (empty(cardNote)) {
      errmsg("卡片等級: 不可空白");
      return;
    }

//    if (empty(entryModeType)) {
//      errmsg("entry Mode類別: 不可空白");
//      return;
//    }

    if (empty(web3dFlag)) {
      errmsg("交易類別: 不可空白");
      return;
    }

//    if (empty(riskType)) {
//      errmsg("風險類別: 不可空白");
//      return;
//    }

    if (this.ibDelete)
      return;

    if (wp.itemEmpty("cond1_yn") && wp.itemEmpty("cond2_yn")) {
      errmsg("簡訊一、簡訊二，內容不可皆為空白");
      return;
    }

    if (wp.itemEq("cond1_yn", "Y")) {
      if (wp.itemNum("dd_tx_times") == 0 && wp.itemEmpty("cond1_area")) {
        errmsg("簡訊一：日累計筆數門檻 , 適用地區別 不可全部空白 !");
        return;
      }
    }

    if (wp.itemEq("cond2_yn", "Y")) {
      if (wp.itemEmpty("cond2_area")) {
        errmsg("簡訊二：適用地區別 不可空白 !");
        return;
      }
    }

    if (wp.itemNum("dd_tx_times") != 0) {
      if (wp.itemEmpty("cond1_area")) {
        errmsg("簡訊一:適用地區別 不可空白");
        return;
      }

      if (wp.itemEmpty("msg_id1")) {
        errmsg("簡訊一:簡訊代碼 不可空白");
        return;
      }

      if (wp.itemEmpty("msg_desc1")) {
        errmsg("簡訊一:簡訊內容 不可空白");
        return;
      }
    }


    if (ibAdd)
      return;

    if (wp.itemEq("cond2_yn", "Y")) {
      if (checkSms() == false) {
        errmsg("明細維護：簡訊二-發送回覆碼 不可全部空白");
        return;
      }
    }

    // --5/27 條件二回覆碼改為明細維護
    // if(wp.item_empty("cond2_resp1")==false || wp.item_empty("cond2_resp2")==false){
    // if(wp.item_empty("cond2_area")){
    // errmsg("簡訊二:適用地區別 不可空白");
    // return ;
    // }
    //
    // if(wp.item_empty("msg_id2")){
    // errmsg("簡訊二:簡訊代碼 不可空白");
    // return;
    // }
    //
    // if(wp.item_empty("msg_desc2")){
    // errmsg("簡訊二:簡訊內容 不可空白");
    // return ;
    // }
    // }


  }

  boolean checkSms() {

    String sql1 = " select count(*) as db_cnt " + " from cca_auth_sms2_detl "
    		+ " where card_note = ? " 
//    		+ " and entry_mode_type = ? " 
    		+ " and web3d_flag = ? "
//    		+ " and risk_type = ? "
    		+ " and data_type = 'SMS2' ";

    sqlSelect(sql1, new Object[] {cardNote, entryModeType, web3dFlag, riskType});

    if (sqlRowNum < 0 || colNum("db_cnt") <= 0) {
      return false;
    }

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into cca_auth_sms2_parm (" + " card_note , " // 1
        + " entry_mode_type , " + " web3d_flag , " + " risk_type , " + " cond1_yn , " // 5
        + " dd_tx_times , " + " cond1_mcc , " + " cond1_mcht , " + " cond1_risk , " + " msg_id1 , "
        + " cond2_resp1 , " + " cond2_resp2 , " + " cond2_mcc , " + " cond2_mcht , "
        + " cond2_risk , " + " cond2_yn , " + " msg_id2 ," + " cond1_area ," + " msg_desc1 ,"
        + " cond2_area ," + " msg_desc2 ," + " crt_date , " // 15
        + " crt_user , " + " apr_date , " + " apr_user , " + " mod_user , " + " mod_time , " // 20
        + " mod_pgm , " + " mod_seqno " // 22
        + " ) values (" + " :card_note , " // 1
        + " :entry_mode_type , " + " :web3d_flag , " + " :risk_type , " + " :cond1_yn , " // 5
        + " :dd_tx_times , " + " 'N' , " + " 'N' , " + " 'N' , " + " :msg_id1 , "
        + " :cond2_resp1 , " + " :cond2_resp2 , " + " 'N' , " + " 'N' , " + " 'N' , "
        + " :cond2_yn , " + " :msg_id2 ," + " :cond1_area ," + " :msg_desc1 ," + " :cond2_area ,"
        + " :msg_desc2 ," + " to_char(sysdate,'yyyymmdd') , " // 15
        + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , " + " :apr_user , " + " :mod_user , "
        + " sysdate , " // 20
        + " :mod_pgm , " + " '1' " // 22
        + " )";

    setString("card_note", cardNote);
    setString("entry_mode_type", entryModeType);
    setString("web3d_flag", web3dFlag);
    setString("risk_type", riskType);
    item2ParmNum("dd_tx_times");
    item2ParmStr("msg_id1");
    item2ParmStr("cond2_resp1");
    item2ParmStr("cond2_resp2");
    item2ParmStr("msg_id2");
    item2ParmStr("cond1_area");
    item2ParmStr("msg_desc1");
    item2ParmStr("cond2_area");
    item2ParmStr("msg_desc2");
    item2ParmNvl("cond1_yn", "N");
    item2ParmNvl("cond2_yn", "N");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5272");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = " update cca_auth_sms2_parm set " + " dd_tx_times=:dd_tx_times , "
        + " msg_id1=:msg_id1 , " + " cond2_resp1 =:cond2_resp1 , " + " cond2_resp2 =:cond2_resp2 , "
        + " msg_id2 =:msg_id2 ," + " cond1_area =:cond1_area , " + " msg_desc1 =:msg_desc1 , "
        + " cond2_area =:cond2_area , " + " msg_desc2 =:msg_desc2 , " + " cond1_yn =:cond1_yn , "
        + " cond2_yn =:cond2_yn , " + " apr_date=to_char(sysdate,'yyyymmdd') , "
        + " apr_user=:apr_user , " + " mod_user=:mod_user , " + " mod_time=sysdate , "
        + " mod_pgm=:mod_pgm , " + " mod_seqno=nvl(mod_seqno,0)+1 "
        + " where card_note=:card_note "
//        + " and entry_mode_type=:entry_mode_type "
        + " and web3d_flag=:web3d_flag ";
//        + " and risk_type=:risk_type";
    item2ParmNum("dd_tx_times");
    item2ParmStr("msg_id1");
    item2ParmStr("cond2_resp1");
    item2ParmStr("cond2_resp2");
    item2ParmStr("msg_id2");
    item2ParmStr("cond1_area");
    item2ParmStr("msg_desc1");
    item2ParmStr("cond2_area");
    item2ParmStr("msg_desc2");
    item2ParmNvl("cond1_yn", "N");
    item2ParmNvl("cond2_yn", "N");
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5272");
    setString("card_note", cardNote);
//    setString("entry_mode_type", entryModeType);
    setString("web3d_flag", web3dFlag);
//    setString("risk_type", riskType);
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    msgOK();
    strSql = "Delete cca_auth_sms2_parm" + " where card_note =:card_note "
//        + " and entry_mode_type =:entry_mode_type " 
    	+ " and web3d_flag =:web3d_flag "
//        + " and risk_type =:risk_type "
    	;
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Delete parmdtl err; " + getMsg());
      rc = -1;
    } else
      rc = 1;

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int dbDeleteDetl() {
    msgOK();
    strSql = "Delete cca_auth_sms2_detl " + " where card_note =:card_note "
//        + " and entry_mode_type =:entry_mode_type "
    	+ " and web3d_flag =:web3d_flag "
//        + " and risk_type =:risk_type "
    	+ " and data_type =:data_type";
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    setString("data_type", wp.itemStr("data_type1"));
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("Delete parmdtl err; " + getMsg());
      rc = -1;
    } else
      rc = 1;

    return rc;
  }

  boolean checkMcc() {

    String sql1 =
        " select " + " count(*) as db_cnt " + " from cca_mcc_risk " + " where mcc_code = ? ";

    sqlSelect(sql1, new Object[] {varsStr("data_code1")});

    if (colNum("db_cnt") <= 0)
      return false;

    return true;
  }

  boolean checkResp() {

    String sql1 = " select count(*) as db_cnt2 from cca_resp_code where resp_code = ? ";
    sqlSelect(sql1, new Object[] {varsStr("data_code1")});

    if (colNum("db_cnt2") <= 0)
      return false;

    return true;
  }

  public int dbInsertDetl() {
    msgOK();

    if (eqIgno(wp.respHtml, "ccam5272_mcc1") || eqIgno(wp.respHtml, "ccam5272_mcc2")) {
      if (checkMcc() == false) {
        errmsg("Mcc Code 不存在");
        return rc;
      }
    }

    if (eqIgno(wp.respHtml, "ccam5272_sms2")) {
      if (checkResp() == false) {
        errmsg("回覆碼 不存在");
        return rc;
      }
    }

    strSql = "insert into cca_auth_sms2_detl (" + " card_note , " + " entry_mode_type , "
        + " web3d_flag , " + " risk_type , " + " data_type , " + " apr_flag , " + " data_code1 , "
        + " data_code2 , " + " mod_time , " + " mod_pgm " + " ) values (" + " :card_note , "
        + " :entry_mode_type , " + " :web3d_flag , " + " :risk_type , " + " :data_type , "
        + " 'Y' , " + " :data_code1 , " + " :data_code2 , " + " sysdate , " + " :mod_pgm " + " )";
    item2ParmStr("card_note");
    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
    item2ParmStr("risk_type");
    var2ParmStr("data_type");
    var2ParmStr("data_code1");
    var2ParmStr("data_code2");
    setString("mod_pgm", "ccam5270");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  // --
  public int updateDetl() {
    msgOK();
    updateMCC1();
    if (sqlRowNum <= 0) {
      errmsg("update MCC1 error !");
      return rc;
    }
    updateMCHT1();
    if (sqlRowNum <= 0) {
      errmsg("update MCHT1 error !");
      return rc;
    }
    updateRISK1();
    if (sqlRowNum <= 0) {
      errmsg("update RISK1 error !");
      return rc;
    }
    updateMCC2();
    if (sqlRowNum <= 0) {
      errmsg("update MCC2 error !");
      return rc;
    }
    updateMCHT2();
    if (sqlRowNum <= 0) {
      errmsg("update MCHT2 error !");
      return rc;
    }
    updateRISK2();
    if (sqlRowNum <= 0) {
      errmsg("update RISK2 error !");
    }
    return rc;
  }

  // --MCC1
  boolean checkDtlMCC1() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_sms2_detl " + " where card_note =:card_note "
//            + " and entry_mode_type =:entry_mode_type " 
        	+ " and web3d_flag =:web3d_flag "
//            + " and risk_type =:risk_type "
        	+ " and data_type = 'MCC1' ";
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateMCC1() {
    if (checkDtlMCC1()) {
      strSql = " update cca_auth_sms2_parm set " + " cond1_mcc='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    } else {
      strSql = " update cca_auth_sms2_parm set " + " cond1_mcc='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    }
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5270");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  // --MCC2
  boolean checkDtlMCC2() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_sms2_detl " + " where card_note =:card_note "
//            + " and entry_mode_type =:entry_mode_type "
        	+ " and web3d_flag =:web3d_flag "
//            + " and risk_type =:risk_type "
        	+ " and data_type = 'MCC2' ";
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateMCC2() {
    if (checkDtlMCC2()) {
      strSql = " update cca_auth_sms2_parm set " + " cond2_mcc='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    } else {
      strSql = " update cca_auth_sms2_parm set " + " cond2_mcc='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    }
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5272");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }
  // -- MCHT1

  boolean checkDtlMCHT1() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_sms2_detl " + " where card_note =:card_note "
//            + " and entry_mode_type =:entry_mode_type "
        	+ " and web3d_flag =:web3d_flag "
//            + " and risk_type =:risk_type "
        	+ " and data_type = 'MCHT1' ";
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateMCHT1() {
    if (checkDtlMCHT1()) {
      strSql = " update cca_auth_sms2_parm set " + " cond1_mcht='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    } else {
      strSql = " update cca_auth_sms2_parm set " + " cond1_mcht='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    }
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5272");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  // --MCH2
  boolean checkDtlMCHT2() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_sms2_detl " + " where card_note =:card_note "
//            + " and entry_mode_type =:entry_mode_type "
        	+ " and web3d_flag =:web3d_flag "
//            + " and risk_type =:risk_type "
        	+ " and data_type = 'MCHT2' ";
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateMCHT2() {
    if (checkDtlMCHT2()) {
      strSql = " update cca_auth_sms2_parm set " + " cond2_mcht='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 "
          + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    } else {
      strSql = " update cca_auth_sms2_parm set " + " cond2_mcht='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 "
          + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    }
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5272");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  // -- RISK1
  boolean checkDtlRISK1() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_sms2_detl " + " where card_note =:card_note "
//            + " and entry_mode_type =:entry_mode_type "
        	+ " and web3d_flag =:web3d_flag "
//            + " and risk_type =:risk_type "
        	+ " and data_type = 'RISK1' ";
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateRISK1() {
    if (checkDtlRISK1()) {
      strSql = " update cca_auth_sms2_parm set " + " cond1_risk='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    } else {
      strSql = " update cca_auth_sms2_parm set " + " cond1_risk='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 "
          + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    }
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5272");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  // --RISK2
  boolean checkDtlRISK2() {
    String sql1 =
        "select count(*) as db_cnt " + " from cca_auth_sms2_detl " + " where card_note =:card_note "
//            + " and entry_mode_type =:entry_mode_type "
        	+ " and web3d_flag =:web3d_flag "
//            + " and risk_type =:risk_type "
        	+ " and data_type = 'RISK2' ";
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    sqlSelect(sql1);
    if (colNum("db_cnt") > 0)
      return true;
    return false;
  }

  public int updateRISK2() {
    if (checkDtlRISK2()) {
      strSql = " update cca_auth_sms2_parm set " + " cond2_risk='Y' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    } else {
      strSql = " update cca_auth_sms2_parm set " + " cond2_risk='N' , "
          + " apr_date=to_char(sysdate,'yyyymmdd') , " + " apr_user=:apr_user , "
          + " mod_user=:mod_user , " + " mod_time=sysdate , " + " mod_pgm=:mod_pgm , "
          + " mod_seqno=nvl(mod_seqno,0)+1 " + " where card_note=:card_note "
//          + " and entry_mode_type=:entry_mode_type "
          + " and web3d_flag=:web3d_flag "
//          + " and risk_type=:risk_type"
          ;
    }
    item2ParmStr("card_note");
//    item2ParmStr("entry_mode_type");
    item2ParmStr("web3d_flag");
//    item2ParmStr("risk_type");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam5272");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }
}
