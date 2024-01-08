/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     *
* 109-09-01  V1.00.02  tanwei        查詢/更新中添加貴賓卡字段                                                              *
* 109-09-02  V1.00.03  tanwei        添加字段                                                                                              *
******************************************************************************/
package cmsm03;

import busi.FuncAction;

public class Cmsm3140Func extends FuncAction {
  String lsPpCardNo = "";
  int llDataSeqno = 0;

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      lsPpCardNo = wp.itemStr("kk_pp_card_no");
      String idNo = wp.itemStr("id_no");
      if (empty(idNo)) {
        errmsg("卡號不存在");
        return;
      }
    } else {
      lsPpCardNo = wp.itemStr("pp_card_no");
    }

    if (this.ibDelete)
      return;

    if (empty(lsPpCardNo)) {
      errmsg("貴賓卡卡號 不可空白");
      return;
    }

    if (wp.itemEmpty("visit_date")) {
      errmsg("使用日期 不可空白");
      return;
    }

    if (wp.itemNum("ch_visits") <= 0) {
      errmsg("使用次數 須大於 0");
      return;
    }

    if (wp.itemNum("ch_visits") < wp.itemNum("free_use_cnt")) {
      errmsg("卡友免費使用次數 不可大於 使用次數");
      return;
    }

    if (this.ibAdd) {
      selectDataSeqno();
      if (rc != 1)
        return;
      return;
    }

    String tableName = "";
    String vipKind = wp.itemStr("kk_vip_kind");
    
    tableName = " cms_ppcard_visit ";
    sqlWhere = " where nvl(mod_seqno,0) =? " + commSqlStr.whereRowid(wp.itemStr("rowid"));
    Object[] parms = new Object[] {wp.itemNum("mod_seqno")};
    if (this.isOtherModify(tableName, sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }

  }

  void selectDataSeqno() {

    String sql1 = " select " + " count(*) as allCount ,max(data_seqno) as li_data_seqno " + " from cms_ppcard_visit "
        + " where crt_date = to_char(sysdate,'yyyymmdd') " + " and from_type = '1' "
        + " and bin_type = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr("bin_type")});

    if (sqlRowNum < 0) {
      errmsg("select data_seqno error ");
      return;
    }
    
    if (colInt("allCount") == 0) {
      llDataSeqno = 0;
      return;
    }

    /*
     * if (sqlRowNum == 0 || colInt("li_data_seqno") == 0) { llDataSeqno = 0; return; }
     */

    llDataSeqno = colInt("li_data_seqno") + 1;

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    String tableName = "";
    String vipKind = wp.itemStr("kk_vip_kind");
    
    tableName = " cms_ppcard_visit ";
    strSql = " insert into " + tableName + " ( " + " crt_date ," + " vip_kind ," + " bin_type ," + " data_seqno ,"
        + " from_type ," + " pp_card_no ," + " ch_ename ," + " visit_date ," + " id_p_seqno ," + " iso_conty ,"
        + " ch_visits ," + " guests_count ," + " use_city ," + " id_no ," + " item_no ," + " id_no_code ,"
        + " free_use_cnt ," + " ch_cost_amt ," + " guest_cost_amt ," + " card_no ," + " mcht_no ,"
        + " user_remark ," + " crt_user ," + " mod_user ," + " mod_time ," + " mod_pgm ,"
        + " mod_seqno " + " ) values ( " + " to_char(sysdate,'yyyymmdd') ," + " :vip_kind ," + " :bin_type ,"
        + " :data_seqno ," + " '1' ," + " :pp_card_no ," + " :ch_ename ," + " :visit_date ," + " :id_p_seqno ,"
        + " :iso_conty ," + " :ch_visits ," + " :guests_count ," + " :use_city ," + " :id_no ," + " :item_no ,"
        + " '0' ," + " :free_use_cnt ," + " :ch_cost_amt ," + " :guest_cost_amt ," + " :card_no ,"
        + " :mcht_no ," + " :user_remark ," + " :crt_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";
    item2ParmStr("bin_type");
    setInt("data_seqno", llDataSeqno);
    setString("pp_card_no", lsPpCardNo);
    setString("vip_kind", vipKind);
    if("1".equals(vipKind)) {
      setString("item_no", "11");
    }
    if("2".equals(vipKind)) {
      setString("item_no", "10");
    }
    item2ParmStr("ch_ename");
    item2ParmStr("visit_date");
    item2ParmStr("iso_conty");
    item2ParmNum("ch_visits");
    item2ParmNum("guests_count");
    item2ParmStr("use_city");
    item2ParmStr("id_no");
    item2ParmNum("free_use_cnt");
    item2ParmNum("ch_cost_amt");
    item2ParmNum("guest_cost_amt");
    item2ParmStr("card_no");
    item2ParmStr("mcht_no");
    item2ParmStr("user_remark");
    item2ParmStr("id_p_seqno");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    String tableName = "";
    String vipKind = wp.itemStr("kk_vip_kind");

    tableName = " cms_ppcard_visit ";
    strSql = " update " + tableName + " set " + " visit_date =:visit_date ," + " vip_kind =:vip_kind ,"
        + " iso_conty =:iso_conty ," + " ch_visits =:ch_visits ," + " guests_count =:guests_count ,"
        + " use_city =:use_city ," + " free_use_cnt =:free_use_cnt ," + " item_no =:item_no ,"
        + " ch_cost_amt =:ch_cost_amt ," + " guest_cost_amt =:guest_cost_amt ,"
        + " user_remark =:user_remark ," + " crt_user =:crt_user ," + " mod_user =:mod_user ,"
        + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ," + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where 1=1 " + commSqlStr.whereRowid(wp.itemStr("rowid")) + " and mod_seqno =:mod_seqno ";

    item2ParmStr("visit_date");
    item2ParmStr("iso_conty");
    item2ParmNum("ch_visits");
    item2ParmNum("guests_count");
    item2ParmStr("use_city");
    item2ParmNum("free_use_cnt");
    item2ParmNum("ch_cost_amt");
    item2ParmNum("guest_cost_amt");
    item2ParmStr("user_remark");
    if("1".equals(vipKind)) {
      setString("item_no", "11");
    }
    if("2".equals(vipKind)) {
      setString("item_no", "10");
    }
    setString("vip_kind", vipKind);
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmNum("mod_seqno");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update cms_ppcard_visit error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");

    String tableName = "";
    String vipKind = wp.itemStr("kk_vip_kind");
    
    tableName = " cms_ppcard_visit ";
    strSql = " delete " + tableName + " where 1=1 " + commSqlStr.whereRowid(wp.itemStr("rowid"))
        + " and mod_seqno =:mod_seqno ";
    item2ParmNum("mod_seqno");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete cms_ppcard_visit error ");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
