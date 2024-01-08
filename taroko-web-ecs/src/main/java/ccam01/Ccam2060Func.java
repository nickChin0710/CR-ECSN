package ccam01;
/*臨時調整額度維護-依產品類別 V.2018-0502-JH
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * */
import busi.FuncAction;

public class Ccam2060Func extends FuncAction {
  String cardNote = "", mccCode = "";
  double userLimit = 0;

  @Override
  public void dataCheck() {
    if (ibAdd) {
      cardNote = wp.itemStr("kk_card_note");
      mccCode = wp.itemStr("kk_mcc_code");
    } else {
      cardNote = wp.itemStr("card_note");
      mccCode = wp.itemStr("mcc_code");
    }

    if (this.ibDelete)
      return;

    if (selectMcode()) {
      errmsg("MCC CODE: 不存在");
      return;
    }
    /*
     * select_user_limit(); if (wp.item_num("tot_amt_month") > _user_limit) { errmsg("放大倍數超過使用者權限:"
     * + _user_limit + " 倍 "); return; }
     */
    if (empty(cardNote)) {
      errmsg("卡片等級:不可空白");
      return;
    }
    if (empty(mccCode)) {
      errmsg("放大MCC CODE:不可空白");
      return;
    }
    if (wp.itemEmpty("area_type")) {
      errmsg("適用地區別:不可空白");
      return;
    }
    if (wp.itemEmpty("adj_eff_date1")) {
      errmsg("有效日期(起):不可空白");
      return;
    }
    if (wp.itemEmpty("adj_eff_date2")) {
      errmsg("有效日期(迄):不可空白");
      return;
    }

    if (wp.itemStr("adj_eff_date1").compareTo(this.getSysDate()) < 0
        || wp.itemStr("adj_eff_date2").compareTo(this.getSysDate()) < 0) {
      errmsg("有效日期 起迄 須大於等於 系統日期");
      return;
    }


    if (this.chkStrend(wp.itemStr("adj_eff_date1"), wp.itemStr("adj_eff_date2")) == -1) {
      errmsg("有效日期起迄錯誤");
      return;
    }

    if (wp.itemEmpty("times_amt")) {
      errmsg("金額百分比 : 不可空白");
      return;
    }

    if (wp.itemEmpty("times_cnt")) {
      errmsg("次數百分比 : 不可空白");
      return;
    }
    /*
     * if(wp.item_empty("adj_remark")){ errmsg("備註 : 不可空白"); return ; }
     */
    if (wp.itemNum("tot_amt_month") <= 0) {
      errmsg("放大總月限額 不可為0");
      return;
    }

    if (ibAdd) {
      if (checkProd() == false) {
        errmsg("資料已存在不可新增");
      }
      return;
    }

  }

  boolean checkProd() {

    String sql1 = " select " + " count(*) as db_cnt_prod " + " from cca_adj_prod_parm "
        + " where card_note = ? " + " and mcc_code = ? ";

    sqlSelect(sql1, new Object[] {cardNote, mccCode});

    if (colNum("db_cnt_prod") > 0)
      return false;

    return true;
  }

  /*
   * void select_adj_prod_parm() { is_sql ="select hex(rowid) as rowid, mod_seqno"
   * +" from cca_adj_prod_parm" +" where card_note =? and mcc_code =?"
   * +" and area_type =? and adj_eff_date1 =? and adj_eff_date2 =?" ; ppp(1,cardNote); ppp(mccCode);
   * ppp(kk3); ppp(kk4); ppp(kk5); daoTid ="A."; sqlSelect(is_sql); rc
   * =aud_Check("mod_seqno","A.mod_seqno"); }
   */

  void selectUserLimit() {
    userLimit = 0;
    String sql1 = "select uf_nvl(mon_auth_amt, 0) as im_usr_limit " + " from cca_user_base"
        + " where user_id =?";
    sqlSelect(sql1, new Object[] {wp.loginUser});
    if (sqlRowNum > 0) {
      userLimit = colNum("im_usr_limit");
    }
  }

  boolean selectMcode() {
    String sql1 = "select mcc_code " + " from CCA_MCC_RISK" + " where mcc_code =?";
    sqlSelect(sql1, new Object[] {mccCode});
    if (sqlNotfind) {
      return true;
    }
    return false;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into CCA_ADJ_PROD_PARM (" + " card_note, " // 1
        + " area_type, " + " mcc_code, " + " adj_eff_date1, " + " adj_eff_date2, " + " times_amt, "
        + " times_cnt, " + " adj_remark," + " tot_amt_month, " + " crt_date," + " crt_user,"
        + " apr_date," + " apr_user, " + " mod_time," + " mod_user," + " mod_pgm," + " mod_seqno"
        + " ) values (" + " :cardNote," + " :area_type," + " :mccCode," + " :adj_eff_date1,"
        + " :adj_eff_date2," + " :times_amt, " + " :times_cnt, " + " :adj_remark,"
        + " :tot_amt_month, " + " to_char(sysdate,'yyyymmdd')," + " :crt_user,"
        + " to_char(sysdate,'yyyymmdd')," + " :apr_user," + " sysdate," + " :mod_user,"
        + " :mod_pgm," + " 1" + " )";

    setString("cardNote", cardNote);
    setString("mccCode", mccCode);
    item2ParmStr("area_type");
    item2ParmStr("adj_eff_date1");
    item2ParmStr("adj_eff_date2");
    item2ParmNum("times_amt");
    item2ParmNum("times_cnt");
    item2ParmNum("tot_amt_month");
    item2ParmStr("adj_remark");
    item2ParmNum("tot_amt_month");
    setString2("apr_user", wp.itemStr2("approval_user"));
    setString("crt_user", modUser);
    setString("mod_user", modUser);
    setString("mod_pgm", modPgm);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("cca_adj_prod_parm.Add; " + sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql =
        "update CCA_ADJ_PROD_PARM set " + "area_type=:area_type," + "adj_eff_date1=:adj_eff_date1,"
            + "adj_eff_date2=:adj_eff_date2," + "times_amt=:times_amt," + "times_cnt=:times_cnt,"
            + "adj_remark=:adj_remark," + "tot_amt_month=:tot_amt_month,"
            + "apr_date=to_char(sysdate,'yyyymmdd')," + "apr_user=:apr_user,"
            + commSqlStr.setModxxx(modUser, modPgm) + " where 1=1 and rowid = :rowid "
            ;

    item2ParmStr("area_type");
    item2ParmStr("adj_eff_date1");
    item2ParmStr("adj_eff_date2");
    item2ParmNum("times_amt");
    item2ParmNum("times_cnt");
    item2ParmStr("adj_remark");
    item2ParmNum("tot_amt_month");
    setString2("apr_user", wp.itemStr2("approval_user"));
    setRowId2("rowid",wp.itemStr("rowid"));
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("cca_adj_prod_parm.Update; " + this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    //strSql = " delete cca_adj_prod_parm where 1=1 " + commSqlStr.whereRowid(wp.itemStr("rowid"));
    strSql = " delete cca_adj_prod_parm where 1=1 and rowid =? " ;
    setRowId(wp.itemStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete cca_adj_prod_parm error");
    }
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
