/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 110-03-23  V1.00.01  Tanwei       sql中cms_acaj表中crd_date字段改為 crt_date    *
******************************************************************************/
package cmsr03;
/** 19-0617:   JH    p_xxx >>acno_p_xxx
 *
 * */
import busi.FuncQuery;

public class Cmsq4220Func extends FuncQuery {

  private String isDate1 = "", isDate2 = "";
  public int listCnt = 0, listRrn = -1;

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    msgOK();
    dataCheck();
    if (rc != 1)
      return rc;

    wfGetAcaj();
    wfGetAcajHst();
    wfGetCmsAcaj();

    return rc;
  }

  void wfGetAcaj() {
    if (eqIgno(wp.itemStr("ex_acct_code"), "0"))
      return;
    strSql = "select crt_date, acct_code, card_no, dr_amt, apr_flag" + " from act_acaj"
        + " where card_no in (" + "   select card_no from crd_card "
        + "   where major_id_p_seqno =:ls_id_p_seqno or card_no =:ls_card_no )"
        + " and crt_date between :ls_date1 and :ls_date2"
        + " and acct_code in ('AF','LF','RI','PN','PF')" + " and acct_code like :ls_acct_code"
        + " order by 1,2";

    setString2("ls_id_p_seqno", wp.itemStr("id_p_seqno"));
    setString2("ls_card_no", wp.itemStr("ex_card_no"));
    setString2("ls_date1", isDate1);
    setString2("ls_date2", isDate2);
    setString2("ls_acct_code", wp.itemNvl("ex_acct_code", "%"));
    busi.DataSet oods = new busi.DataSet();
    oods.colList = sqlQuery(strSql);
    log("E:" + oods.listRows());
    for (int ll = 0; ll < oods.listRows(); ll++) {
      oods.listFetch(ll);
      listRrn++;
      if (listRrn < 10)
        wp.colSet(listRrn, "ser_num", "0" + (listRrn + 1));
      else
        wp.colSet(listRrn, "ser_num", "" + (listRrn + 1));
      wp.colSet(listRrn, "ex_item_ename", oods.colStr("acct_code"));
      wp.colSet(listRrn, "ex_d_date", oods.colStr("crt_date"));
      wp.colSet(listRrn, "ex_adj_amt", oods.colStr("dr_amt"));
      wp.colSet(listRrn, "ex_card_no", oods.colStr("card_no"));
      wp.colSet(listRrn, "ex_apr_flag", oods.colStr("apr_flag"));
      wp.colSet(listRrn, "ex_table", "acaj");
    }

    listCnt += oods.listRows();
  }

  void wfGetAcajHst() {
    if (eqIgno(wp.itemStr("ex_acct_code"), "0"))
      return;
    strSql = " select crt_date, acct_code, card_no, dr_amt, apr_flag " + " from act_acaj_hst "
        + " where card_no in (select card_no from crd_card "
        + " where major_id_p_seqno =:ls_id_p_seqno or card_no =:ls_card_no ) "
        + " and crt_date between :ls_date1 and :ls_date2 "
        + " and acct_code in ('AF','LF','RI','PN','PF') " + " and acct_code like :ls_acct_code ";

    setString2("ls_id_p_seqno", wp.itemStr("id_p_seqno"));
    setString2("ls_card_no", wp.itemStr("ex_card_no"));
    setString2("ls_date1", isDate1);
    setString2("ls_date2", isDate2);
    setString2("ls_acct_code", wp.itemNvl("ex_acct_code", "%"));
    busi.DataSet oods = new busi.DataSet();
    oods.colList = sqlQuery(strSql);
    for (int ll = 0; ll < oods.listRows(); ll++) {
      oods.listFetch(ll);
      listRrn++;
      if (listRrn < 10)
        wp.colSet(listRrn, "ser_num", "0" + (listRrn + 1));
      else
        wp.colSet(listRrn, "ser_num", "" + (listRrn + 1));
      wp.colSet(listRrn, "ex_item_ename", oods.colStr("acct_code"));
      wp.colSet(listRrn, "ex_d_date", oods.colStr("crt_date"));
      wp.colSet(listRrn, "ex_adj_amt", oods.colStr("dr_amt"));
      wp.colSet(listRrn, "ex_card_no", oods.colStr("card_no"));
      wp.colSet(listRrn, "ex_apr_flag", oods.colStr("apr_flag"));
      wp.colSet(listRrn, "ex_table", oods.colStr("acaj_hst"));
    }

    listCnt += oods.listRows();
  }

  void wfGetCmsAcaj() {
    if (eqIgno(wp.itemStr("ex_acct_code"), "0"))
      return;
    strSql = " crt_date, acct_code, card_no, adj_amt, decode(apr_date,'','N','Y') as apr_flag "
        + " from cms_acaj " + " where card_no in (select card_no from crd_card "
        + " where major_id_p_seqno =:ls_id_p_seqno or card_no =:ls_card_no ) "
        + " and crt_date between :ls_date1 and :ls_date2 "
        + " and acct_code in ('AF','LF','RI','PN','PF') " + " and acct_code like :ls_acct_code "
        + " and acct_post_flag ='N' " + " and acct_errcode ='' " + " order by 1,2 ";

    setString2("ls_id_p_seqno", wp.itemStr("id_p_seqno"));
    setString2("ls_card_no", wp.itemStr("ex_card_no"));
    setString2("ls_date1", isDate1);
    setString2("ls_date2", isDate2);
    setString2("ls_acct_code", wp.itemNvl("ex_acct_code", "%"));
    busi.DataSet oods = new busi.DataSet();
    oods.colList = sqlQuery(strSql);
    for (int ll = 0; ll < oods.listRows(); ll++) {
      oods.listFetch(ll);
      listRrn++;
      if (listRrn < 10)
        wp.colSet(listRrn, "ser_num", "0" + (listRrn + 1));
      else
        wp.colSet(listRrn, "ser_num", "" + (listRrn + 1));
      wp.colSet(listRrn, "ex_item_ename", oods.colStr("acct_code"));
      wp.colSet(listRrn, "ex_d_date", oods.colStr("crt_date"));
      wp.colSet(listRrn, "ex_adj_amt", oods.colStr("dr_amt"));
      wp.colSet(listRrn, "ex_card_no", oods.colStr("card_no"));
      wp.colSet(listRrn, "ex_apr_flag", oods.colStr("apr_flag"));
      wp.colSet(listRrn, "ex_table", oods.colStr("cms_acaj"));
    }

    listCnt += oods.listRows();


  }

  @Override
  public void dataCheck() {

    isDate1 = wp.itemNvl("ex_date1", "19000101");
    isDate2 = wp.itemNvl("ex_date2", "29991231");

  }

}
