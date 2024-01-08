package cmsm02;
/** 19-0614:   JH    p_xxx >>acno_p_xxx
 *  19-1126:   Alex  code ->chinese
 *  19-1230:   Alex  add debit_flag
 ** 109-04-27 shiyuqi       updated for project coding standard     *  
 ** 109-12-31  V1.00.03   shiyuqi       修改无意义命名     
 *  111-11-04  V1.00.03   Machao        頁面bug調整                                                                                   *  
 * */
import busi.FuncAction;


public class Cmsm3210Func extends FuncAction {
  String cardNo = "";

  public int getDataCard(String lsCardNo) {
    if (empty(lsCardNo))
      return 0;

    wp.colSet("vd_flag", "C");
    wp.colSet("tt_vd_flag", "信用卡");
    strSql = "select A.acct_type, A.acno_p_seqno, A.id_p_seqno"
        + ", B.chi_name, B.birthday, B.cellar_phone, B.e_mail_addr, A.current_code"
        + ", uf_acno_key2(A.p_seqno,'N') as acct_key" + " from crd_card A, crd_idno B"
        + " where A.id_p_seqno = B.id_p_seqno" + commSqlStr.col(lsCardNo, "A.card_no");
    busi.func.CrdFunc ffCard = new busi.func.CrdFunc();
    ffCard.setConn(wp);
    if (ffCard.isDebitcard(lsCardNo)) {
      wp.colSet("vd_flag", "D");
      wp.colSet("tt_vd_flag", "VD金融卡");
      strSql = "select A.acct_type, A.p_seqno as acno_p_seqno, A.id_p_seqno"
          + ", B.chi_name, B.birthday, B.cellar_phone, B.e_mail_addr, A.current_code"
          + ", uf_acno_key2(A.p_seqno,'Y') as acct_key" + " from dbc_card A, dbc_idno B"
          + " where A.id_p_seqno = B.id_p_seqno" + commSqlStr.col(lsCardNo, "A.card_no");
    }
    this.sqlSelect(strSql,lsCardNo);
    if (sqlRowNum <= 0) {
      wp.colSet("debit_flag", "");
      errmsg("卡號: 輸入錯誤");
      return rc;
    }

    wp.colSet("card_no", lsCardNo);
    wp.colSet("db_current_code", colStr("current_code"));
    if (colEq("current_code", "0")) {
      wp.colSet("tt_current_code", "正常");
    } else if (colEq("current_code", "1")) {
      wp.colSet("tt_current_code", "申停");
    } else if (colEq("current_code", "2")) {
      wp.colSet("tt_current_code", "掛失");
    } else if (colEq("current_code", "3")) {
      wp.colSet("tt_current_code", "強停");
    } else if (colEq("current_code", "4")) {
      wp.colSet("tt_current_code", "其他停用");
    } else if (colEq("current_code", "5")) {
      wp.colSet("tt_current_code", "偽卡");
    }


    wp.colSet("db_chi_name", colStr("chi_name"));
    wp.colSet("db_bir_date", colStr("birthday"));
    if (empty(wp.colStr("rowid"))) {
      wp.colSet("id_p_seqno", colStr("id_p_seqno"));
      wp.colSet("cellar_phone", colStr("cellar_phone"));
      wp.colSet("e_mail_addr", colStr("e_mail_addr"));
      wp.colSet("acno_p_seqno", colStr("acno_p_seqno"));
      wp.colSet("acct_type", colStr("acct_type"));
      wp.colSet("acct_key", colStr("acct_key"));
    }

    return rc;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      cardNo = wp.itemStr("kk_card_no");
    } else {
      cardNo = wp.itemStr("card_no");
    }

    if (this.ibDelete) {
      if (!empty(wp.itemStr("send_date")) && this.eqIgno(wp.itemStr("proc_flag"), "N")) {
        errmsg("資料已發送, 不可刪除");
        return;
      }
      return;
    }

    if (this.ibUpdate) {
      if (!empty(wp.itemStr("send_date"))) {
        errmsg("資料已發送, 不可修改");
        return;
      }
    }

    if (selectVallCard() == false) {
      return;
    }

    if (empty(wp.itemStr("cellar_phone")) && empty(wp.itemStr("e_mail_addr"))) {
      errmsg("手機號碼, E_MAIL: 不可同時空白");
      return;
    }

  }

  boolean selectVallCard() {
    String sql1 =
        "select 'C' as vd_flag ," + " current_code , " + " id_p_seqno , " + " acct_type , "
            + " acno_p_seqno, p_seqno " + " from crd_card  " + " where card_no =?" + " union all "
            + " select 'D' as vd_flag , " + " current_code , " + " id_p_seqno , " + " acct_type , "
            + " p_seqno as acno_p_seqno, p_seqno " + " from dbc_card  " + " where card_no =?";
    sqlSelect(sql1, new Object[] {cardNo, cardNo});
    if (sqlRowNum <= 0) {
      errmsg("卡號: 輸入錯誤");
      return false;
    }

    if (this.eqIgno(colStr("current_code"), "0")) {
      errmsg("卡片未停用, 不可註銷");
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

    strSql = "insert into sms_einvo_cancel (" + " card_no ," + " vd_flag ," + " id_p_seqno ,"
        + " acct_type ," + " p_seqno ," + " e_mail_addr ," + " cellar_phone ," + " crt_date ,"
        + " crt_time ," + " crt_user ," + " apr_date ," + " mod_user ," + " mod_time ,"
        + " mod_pgm ," + " mod_seqno " + " ) values (" + " :kk1 ," + " :vd_flag ,"
        + " :id_p_seqno ," + " :acct_type ," + " :kk_p_seqno ," + " :e_mail_addr ,"
        + " :cellar_phone ," + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ,"
        + " :crt_user ," + " to_char(sysdate,'yyyymmdd') ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " '1' " + " )";
    setString("kk1", cardNo);
    setString("vd_flag", colStr("vd_flag"));
    setString("id_p_seqno", colStr("id_p_seqno"));
    setString("acct_type", colStr("acct_type"));
    setString2("kk_p_seqno", colStr("p_seqno"));
    item2ParmStr("e_mail_addr");
    item2ParmStr("cellar_phone");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm3210");
    this.sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Insert sms_einvo_cancel error, " + getMsg());
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
    strSql = "update sms_einvo_cancel set " + " e_mail_addr =:e_mail_addr ,"
        + " cellar_phone =:cellar_phone ," + " mod_user =:mod_user ," + " mod_time =sysdate ,"
        + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 " + " where card_no =:kk1 ";
    item2ParmStr("e_mail_addr");
    item2ParmStr("cellar_phone");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm3210");
    setString("kk1", cardNo);

    this.sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Update sms_einvo_cancel error, " + getMsg());
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
    strSql = "delete sms_einvo_cancel " + " where card_no =:kk1 "
        + " and nvl(mod_seqno,0) =:mod_seqno ";;
    setString("kk1", cardNo);
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }



  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
