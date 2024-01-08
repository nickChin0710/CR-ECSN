package cmsm01;
/** 
 * 2020-0828: JustinWu hide autopay_id_code and give 0 as default value of autopay_id_code 
 * 2020-0213: JustinWu add new column
 * 2019-0613:    JH    p_xxx >>acno_pxxx
 * 109-04-19    shiyuqi       updated for project coding standard
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名
 * 110-01-06    Justin          updated for XSS
 * 112-10-24    Ryan          修正地址顯示問題
 * */
import busi.FuncAction;


public class Cmsm2020Acno extends FuncAction {
  String isRespHtml = "";
  String acnoPSeqno = "";// dataKK2 = "", dataKK3 = "";
  String[] aprCols = new String[] {
	  "bill_sending_zip",
	  "bill_sending_zip2", "bill_sending_addr1", "bill_sending_addr2",
      "bill_sending_addr3", "bill_sending_addr4", "bill_sending_addr5", "special_comment",
      "risk_bank_no", "reg_bank_no", "accept_dm", "chg_addr_date", "new_acct_flag",
      "bill_apply_flag"};

  void setKkValue() {
    acnoPSeqno = wp.colStr("acno_p_seqno");
    isRespHtml = varsStr("is_respHtml");
  }

  public int selectModdataTmp() {
    setKkValue();

    if (empty(acnoPSeqno)) {
      errmsg("帳戶流水號: 不可空白");
      return -1;
    }
    strSql = " select tmp_moddata from ecs_moddata_tmp where tmp_pgm='cmsm2020'"
           + " and tmp_table = 'ACT_ACNO:CORP' and tmp_key =?";
    sqlSelect(strSql, new Object[] {acnoPSeqno});
    if (sqlRowNum <= 0)
      return 0;

    // -set-mod-data-
    int liMod = 0;
    String[] ss = new String[2];
    ss[0] = colStr("tmp_moddata");

    for (int ii = 0; ii < aprCols.length; ii++) {
      ss = commString.token(ss, "\t");
      String col = aprCols[ii];
      if (eqIgno(ss[1], wp.colStr(col))) {
        continue;
      }

      if (eqIgno(col, "accept_dm")) {
        wp.colSet("hh_accept_dm", ss[1]);
      }

      wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
      wp.colSet(col, ss[1]);
      liMod = 1;
    }

    return liMod;
  }



  void selectActAcno() {
    strSql = "select " + "  bill_sending_zip " + ", bill_sending_addr1 " + ", bill_sending_addr2 "
        + ", bill_sending_addr3 " + ", bill_sending_addr4 " + ", bill_sending_addr5 "
        + ", special_comment " + ", risk_bank_no " + ", reg_bank_no " + ", accept_dm "
        + ", chg_addr_date " + ", new_acct_flag " + " from act_acno " + " where acno_p_seqno =?";
    sqlSelect(strSql, new Object[] {acnoPSeqno});
    if (sqlRowNum <= 0) {
      errmsg("查無帳戶資料; kk=" + acnoPSeqno);
    }
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] billSendingZipArr = commString.splitZipCode(colStr("bill_sending_zip"));
    colSet("bill_sending_zip", billSendingZipArr[0]);
    colSet("bill_sending_zip2", billSendingZipArr[1]);
  }

  void deleteEcsModdata() {
    strSql = "delete ecs_moddata_tmp" + " where tmp_pgm ='cmsm2020'"
        + " and tmp_table ='ACT_ACNO:CORP'" + " and tmp_key =?";
    this.sqlExec(strSql, new Object[] {acnoPSeqno});
  }

  void insertEcsModdata() {
    String lsData = "";
    for (int ii = 0; ii < aprCols.length; ii++) {
      lsData += wp.itemStr(aprCols[ii]) + "\t";
    }

    // insert ecs_moddata_tmp
    strSql = "insert into ecs_moddata_tmp (" + " tmp_pgm, " + "tmp_table, " + "tmp_key, "
        + "tmp_dspdata, " + "tmp_moddata, " + "tmp_audcode, " + "mod_user, " + "mod_date, "
        + "mod_time2, " + "mod_seqno " + " ) values ( " + "'cmsm2020', " + "'ACT_ACNO:CORP', "
        + ":kk1 ," + ":dsp_data, " + ":mod_data, " + "'U', " + ":mod_user, "
        + "to_char(sysdate,'yyyymmdd'), " + "to_char(sysdate,'hh24miss'), " + "1" + " )";
    setString("kk1", acnoPSeqno);
    setString("dsp_data", "");
    setString("mod_data", lsData);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ecs_moddata error; kk=" + acnoPSeqno);
    }
  }



  boolean checkModifyApr() {
    for (int ii = 0; ii < aprCols.length; ii++) {
      String col = aprCols[ii];
      if (colEq(col, wp.itemStr(col)) == false) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  void dataCheckCurr() {
    if (wp.itemEq("kk_curr_code", "901")) {
      errmsg("不可新增 [台幣幣別]");
      return;
    }

    if (empty(wp.itemStr("autopay_acct_bank")) || empty(wp.itemStr("autopay_acct_no"))
        || empty(wp.itemStr("autopay_id")) ) {
      errmsg("自動扣繳帳戶資料[行庫, 帳號] 不可空白");
      return;
    }

  }

  @Override
  public int dbInsert() {

    dataCheckCurr();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into act_acct_curr (" + " p_seqno , " + " curr_code , "
        + " autopay_indicator , " + " autopay_acct_bank , " + " autopay_acct_no , "
        + " autopay_id , " + " autopay_id_code , " + " autopay_dc_flag ," + " acct_type,"
        + " apr_user , " + " apr_date , " + " mod_time , " + " mod_user , " + " mod_pgm , "
        + " mod_seqno " + " ) values (" + " :p_seqno , " + " :curr_code , "
        + " :autopay_indicator , " + " :autopay_acct_bank , " + " :autopay_acct_no , "
        + " :autopay_id , " + " :autopay_id_code , " + " :autopay_dc_flag ," + " :acct_type, "
        + " :apr_user , " + " to_char(sysdate,'yyyymmdd') ," + " sysdate , " + " :mod_user , "
        + " :mod_pgm , " + " '1'" + " )";

    setString2("p_seqno", wp.colStr("acno_p_seqno"));
    item2ParmStr("curr_code", "kk_curr_code");
    item2ParmStr("autopay_indicator");
    item2ParmStr("autopay_acct_bank");
    item2ParmStr("autopay_acct_no");
    item2ParmStr("autopay_id");
    item2ParmNvl("autopay_id_code","0");
    item2ParmNvl("autopay_dc_flag", "N");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm2020");
    setString("acct_type", wp.colStr("acct_type"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return rc;
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    log("is_respHtml:" + isRespHtml);
    setKkValue();
    if (eqIgno(isRespHtml, "cmsm2020_acno")) {
      selectActAcno();
      if (rc != 1) {
        return rc;
      }
      deleteEcsModdata();
      if (checkModifyApr()) {
        insertEcsModdata();
      }
    } else if (eqIgno(isRespHtml, "cmsm2020_curr")) {
      dataCheckCurr();
    	
      log("AAAAA");
      strSql = "update act_acct_curr set" + " autopay_indicator =:autopay_indicator  , "
          + " autopay_acct_bank =:autopay_acct_bank  , " + " autopay_acct_no =:autopay_acct_no  , "
          + " autopay_id =:autopay_id , " + " autopay_id_code =:autopay_id_code  , "
          + " autopay_dc_flag =:autopay_dc_flag ," + " apr_user =:apr_user ,"
          + " apr_date = to_char(sysdate,'yyyymmdd') ," + " mod_user =:mod_user ,"
          + " mod_time = sysdate ," + " mod_pgm  =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
          + " where p_seqno =:p_seqno " + " and curr_code =:curr_code";

      item2ParmStr("autopay_indicator");
      item2ParmStr("autopay_acct_bank");
      item2ParmStr("autopay_acct_no");
      item2ParmStr("autopay_id");
      item2ParmNvl("autopay_id_code","0");
      item2ParmNvl("autopay_dc_flag", "N");
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", "cmsm2020");
      setString("apr_user", wp.itemStr("approval_user"));
      // kk
      setString2("p_seqno", wp.itemStr2("acno_p_seqno"));
      item2ParmStr("curr_code");
      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
      }
    }


    return rc;
  }

  @Override
  public int dbDelete() {
    setKkValue();
    if (eqIgno(isRespHtml, "cmsm2020_acno")) {
      deleteEcsModdata();
    } else if (eqIgno(isRespHtml, "cmsm2020_curr")) {
      strSql = "delete act_acct_curr" + " where p_seqno = ? " + " and curr_code = ?";
      this.sqlExec(strSql, new Object[] {acnoPSeqno, wp.itemStr("curr_code")});
    }

    return rc;
  }

  @Override
  public int dataProc() {
    msgOK();

    strSql = " update act_acno set " + " bill_apply_flag =:bill_apply_flag , " // 2020-02-14 JustinWu
        + " bill_sending_zip =:bill_sending_zip , " + " bill_sending_addr1 =:bill_sending_addr1 , "
        + " bill_sending_addr2 =:bill_sending_addr2 , "
        + " bill_sending_addr3 =:bill_sending_addr3 , "
        + " bill_sending_addr4 =:bill_sending_addr4 , "
        + " bill_sending_addr5 =:bill_sending_addr5 , " + " reg_bank_no =:reg_bank_no , "
        + " risk_bank_no =:risk_bank_no , " + " chg_addr_date =:chg_addr_date , "
        + " special_comment =:special_comment , "
        // + " inst_auth_loc_amt =:inst_auth_loc_amt , "
        + " accept_dm =:accept_dm , " + " class_code =:class_code , "
        + " new_acct_flag =:new_acct_flag , " + " online_update_date =to_char(sysdate,'yyyymmdd') ,"
        + " mod_user =:mod_user , " + " mod_time =sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where acno_p_seqno =:acno_p_seqno ";

    item2ParmStr("bill_apply_flag"); // 2020-02-14 JustinWu
//    item2ParmStr("bill_sending_zip");
    setString("bill_sending_zip", wp.itemStr("bill_sending_zip") + wp.itemStr("bill_sending_zip2"));
    item2ParmStr("bill_sending_addr1");
    item2ParmStr("bill_sending_addr2");
    item2ParmStr("bill_sending_addr3");
    item2ParmStr("bill_sending_addr4");
    item2ParmStr("bill_sending_addr5");
    item2ParmStr("reg_bank_no");
    item2ParmStr("risk_bank_no");
    item2ParmStr("chg_addr_date");
    item2ParmStr("special_comment");
    item2ParmStr("accept_dm", "hh_accept_dm");
    item2ParmStr("class_code");
    item2ParmStr("new_acct_flag");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    // kk
    setString2("acno_p_seqno", wp.itemStr2("acno_p_seqno"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update act_acno error !");
      rc = -1;
      return rc;
    }

    strSql = " delete ecs_moddata_tmp where tmp_table ='ACT_ACNO:CORP'" + " and tmp_key =? "
        + " and tmp_pgm ='cmsm2020' ";

    setString2(1, wp.itemStr2("acno_p_seqno"));
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg(" delete tmp error ! ");
      rc = -1;
      return rc;
    }

    return rc;
  }

}
