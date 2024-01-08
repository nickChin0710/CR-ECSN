/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-11-02  V1.00.01  Tanwei       字符摩爾替換為龍騰卡，item_no=13替換為item_no=11  *
* 109-12-31  V1.00.02   shiyuqi     修改无意义命名                                                                                      *
* 110-03-08  V1.00.03  tanwei       將對應參數13改為11                           *                                                          *       
******************************************************************************/
package cmsr03;
/** 19-0617:   JH    p_xxx >>acno_p_xxx
 * */
import java.text.ParseException;

import busi.FuncAction;

public class Cmsq4210Func extends FuncAction {
  public int iiListCnt = 0, iiRrn = -1;
  private String idPseqno = "";
  private String isDate1;
  private String isDate2;
  private boolean ibNew = false;

  public void dataSelect2() throws Exception {

    dataCheck();
    if (rc != 1)
      return;

    String lsItemNo = wp.itemStr("ex_acct_code");

    if (eqIgno(lsItemNo, "0") || eqIgno(lsItemNo, "08"))
      wfGetItem08();
    if (eqIgno(lsItemNo, "0") || eqIgno(lsItemNo, "09"))
      wfGetItem09();
    if (eqIgno(lsItemNo, "0") || eqIgno(lsItemNo, "10"))
      wfGetItem10();
    //if (eqIgno(lsItemNo, "0") || eqIgno(lsItemNo, "13"))
    //wfGetItem13();
    if (eqIgno(lsItemNo, "0") || eqIgno(lsItemNo, "11"))
      wfGetItem11();


  }

  public int wfGetItem08() throws Exception {
    /*
     * 機場接送次數, bil_contract.count(*) by card_no ->merchant_no=substrb(key_data,1,15) and
     * prod_no=substrb(key_data,16,8) -> and first_post_date(1,6) = [成本年月]
     */
    String sql1 = "";
    if (wp.itemEmpty("ex_idno") == false) {
      sql1 = "select card_no, purchase_date" + " from bil_contract"
          + " where card_no in (select card_no from vcard_idno_M where id_no=:kk )"
          + " and (mcht_no,product_no) in ( select trim(substr(key_data,1,15)) as mcht_no"
          + ", trim(substrb(key_data,16,8)) as prod_no"
          + " from mkt_contri_parm where item_no ='08'"
          + " and cost_month between :ls_ym1 and :ls_ym2 )"
          + " and purchase_date between :ls_date1 and :ls_date2" + " order by 1,2";

      setString2("kk", wp.itemStr2("ex_idno"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    } else {
      sql1 = "select card_no, purchase_date" + " from bil_contract" + " where card_no =:kk "
          + " and (mcht_no,product_no) in ( select trim(substr(key_data,1,15)) as mcht_no"
          + ", trim(substrb(key_data,16,8)) as prod_no"
          + " from mkt_contri_parm where item_no ='08'"
          + " and cost_month between :ls_ym1 and :ls_ym2 )"
          + " and purchase_date between :ls_date1 and :ls_date2" + " order by 1,2";
      setString2("kk", wp.itemStr("ex_card_no"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    }

    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return rc;

    String cardNo = "";
    String lsCardNo = "";
    int llCnt = 0;
    for (int ll = 0; ll < sqlRowNum; ll++) {
      lsCardNo = colStr(ll, "card_no");
      if (cardNo.length() > 0 && eqIgno(cardNo, lsCardNo) == false) {
        iiRrn++;
        wp.colSet(iiRrn, "ex_item_no", "08");
        wp.colSet(iiRrn, "tt_item_no", ".機場接送");
        wp.colSet(iiRrn, "ex_type", "1");
        wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
        wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("08", llCnt));
        wp.colSet(iiRrn, "ex_card_no", cardNo);
        llCnt = 0;
      }

      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      wp.colSet(iiRrn, "ex_item_no", "08");
      wp.colSet(iiRrn, "tt_item_no", ".機場接送");
      wp.colSet(iiRrn, "ex_date", colStr(ll, "purchase_date"));
      wp.colSet(iiRrn, "ex_cnt", "1");
      wp.colSet(iiRrn, "ex_card_no", lsCardNo);
      wp.colSet(iiRrn, "opt_show", "disabled");
      llCnt++;
      cardNo = lsCardNo;
    }

    if (llCnt > 0) {
      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      wp.colSet(iiRrn, "ex_item_no", "08");
      wp.colSet(iiRrn, "tt_item_no", ".機場接送");
      wp.colSet(iiRrn, "ex_type", "1");
      wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
      wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("08", llCnt));
      wp.colSet(iiRrn, "ex_card_no", cardNo);
    }
    iiListCnt = (iiRrn + 1);
    return rc;
  }

  public int wfGetItem09() throws Exception {

    // 機場停車次數, bil_contract.count(*) by card_no
    // ->merchant_no= key_data
    // -> and first_post_date(1,6) = [成本年月]
    // =======================================================

    String lsCardNo = "", lsPurchDate = "";
    String lsYm1 = "", lsYm2 = "", lsDate1 = "", lsDate2 = "";
    int liType = 1;
    int ll = 0, llCnt = 0;

    if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no")))
      return 0;

    String sql1 = "";
    if (!empty(wp.itemStr("ex_idno"))) {
      liType = 1;
      sql1 = " select card_no , purchase_date from bil_contract "
          + " where card_no in (select card_no from vcard_idno_M where id_no=:kk )"
          + " and mcht_no in (select trim(key_data) mcht_no from mkt_contri_parm "
          + " where item_no ='09' and cost_month between :ls_ym1 and :ls_ym2)"
          + " and purchase_date between :ls_date1 and :ls_date2 " + " order by 1,2";

      setString2("kk", wp.itemStr2("ex_idno"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    } else {
      liType = 2;
      sql1 = " select card_no , purchase_date from bil_contract " + " where card_no =:card_no "
          + " and mcht_no in (select trim(key_data) mcht_no from mkt_contri_parm "
          + " where item_no ='09' and cost_month between :ls_ym1 and :ls_ym2) "
          + " and purchase_date between :ls_date1 and :ls_date2 " + " order by 1,2 ";
      setString2("card_no", wp.itemStr("ex_card_no"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    }
    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return rc;

    String lsKk1 = "";
    llCnt = 0;
    for (ll = 0; ll < sqlRowNum; ll++) {
      lsCardNo = colStr(ll, "card_no");
      if (lsKk1.length() > 0 && eqIgno(lsKk1, lsCardNo) == false) {
        iiRrn++;
        if (iiRrn < 10)
          wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
        else
          wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
        wp.colSet(iiRrn, "ex_item_no", "09");
        wp.colSet(iiRrn, "tt_item_no", ".機場停車");
        wp.colSet(iiRrn, "ex_type", "1");
        wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
        wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("09", llCnt));
        wp.colSet(iiRrn, "ex_card_no", lsCardNo);
        llCnt = 0;
      }

      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      wp.colSet(iiRrn, "ex_item_no", "09");
      wp.colSet(iiRrn, "tt_item_no", ".機場停車");
      wp.colSet(iiRrn, "ex_date", colStr(ll, "purchase_date"));
      wp.colSet(iiRrn, "ex_cnt", "1");
      wp.colSet(iiRrn, "ex_card_no", lsCardNo);
      wp.colSet(iiRrn, "opt_show", "disabled");
      lsKk1 = lsCardNo;
      llCnt++;
    }

    if (llCnt > 0) {
      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      wp.colSet(iiRrn, "ex_item_no", "09");
      wp.colSet(iiRrn, "tt_item_no", ".機場停車");
      wp.colSet(iiRrn, "ex_type", "1");
      wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
      wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("09", llCnt));
      wp.colSet(iiRrn, "ex_card_no", lsKk1);
    }

    iiListCnt = (iiRrn + 1);

    return rc;
  }

  public int wfGetItem10() throws Exception {

    // 龍騰卡貴賓室, bil_contract.count(*) by card_no
    // ->merchant_no= key_data
    // -> and first_post_date(1,6) = [成本年月]
    // =======================================================



    String lsCardNo = "", lsPurchDate = "", lsK1 = "";
    String lsYm1 = "", lsYm2 = "", lsDate1 = "", lsDate2 = "";
    int liType = 1;
    int ll = 0, llCnt = 0;

    if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no")))
      return 0;

    String sql1 = "";
    if (!empty(wp.itemStr("ex_idno"))) {
      liType = 1;
      sql1 = " select card_no , purchase_date " + " from bil_contract "
          + " where card_no in (select card_no from vcard_idno_M where id_no =:kk ) "
          + " and mcht_no in (select trim(key_data) mcht_no from mkt_contri_parm "
          + " where item_no ='10' and cost_month between :ls_ym1 and :ls_ym2 ) "
          + " and purchase_date between :ls_date1 and :ls_date2 " + " order by 1 , 2";
      setString2("kk", wp.itemStr2("ex_idno"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    } else {
      liType = 2;
      sql1 = " select card_no , purchase_date from bil_contract " + " where card_no =:card_no "
          + " and mcht_no in (select trim(key_data) mcht_no from mkt_contri_parm "
          + " where item_no ='10' and cost_month between :ls_ym1 and :ls_ym2 )"
          + " and purchase_date between :ls_date1 and :ls_date2 " + " order by 1,2 ";
      setString2("card", wp.itemStr("ex_card_no"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    }

    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return rc;

    for (ll = 0; ll < sqlRowNum; ll++) {
      lsCardNo = colStr(ll, "card_no");
      if (lsK1.length() > 0 && eqIgno(lsK1, lsCardNo) == false) {
        iiRrn++;
        if (iiRrn < 10)
          wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
        else
          wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
        wp.colSet(iiRrn, "ex_item_no", "10");
        //wp.colSet(iiRrn, "tt_item_no", ".摩爾貴賓室");
        wp.colSet(iiRrn, "tt_item_no", ".龍騰卡貴賓室");
        wp.colSet(iiRrn, "ex_type", "1");
        wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
        wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("10", llCnt));
        wp.colSet(iiRrn, "ex_card_no", lsK1);
        llCnt = 0;
      }

      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      wp.colSet(iiRrn, "ex_item_no", "10");
      //wp.colSet(iiRrn, "tt_item_no", ".摩爾貴賓室");
      wp.colSet(iiRrn, "tt_item_no", ".龍騰卡貴賓室");
      wp.colSet(iiRrn, "ex_date", colStr(ll, "purchase_date"));
      wp.colSet(iiRrn, "ex_cnt", "1");
      wp.colSet(iiRrn, "ex_card_no", lsCardNo);
      wp.colSet(iiRrn, "opt_show", "disabled");
      lsK1 = lsCardNo;
      llCnt++;
    }

    if (llCnt > 0) {
      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      wp.colSet(iiRrn, "ex_item_no", "10");
      //wp.colSet(iiRrn, "tt_item_no", ".摩爾貴賓室");
      wp.colSet(iiRrn, "tt_item_no", ".龍騰卡貴賓室");
      wp.colSet(iiRrn, "ex_type", "1");
      wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
      wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("10", llCnt));
      wp.colSet(iiRrn, "ex_card_no", lsK1);
    }

    iiListCnt = (iiRrn + 1);

    return rc;
  }
  
  //public int wfGetItem13() throws Exception {
  public int wfGetItem11() throws Exception {

    // 新貴通貴賓室, cms_ppcard_visit.free_use_cnt by card_no
    // ->merchant_no= key_data
    // -> and create_date(1,6) = [成本年月]
    // =======================================================

    String lsCardNo = "", lsPurchDate = "", lsK1 = "";
    String lsYm1 = "", lsYm2 = "", lsDate1 = "", lsDate2 = "";
    int liType = 1;
    int ll = 0, llCnt = 0, llFree = 0;

    if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no")))
      return 0;

    String sql1 = "";
    if (!empty(wp.itemStr("ex_idno"))) {
      liType = 1;
      sql1 = "select card_no , visit_date , free_use_cnt " + " from cms_ppcard_visit "
          + " where card_no in (select card_no from vcard_idno_M wher id_no =:kk ) "
          + " and mcht_no in (select trim(key_data) mcht_no "
          // + " from mkt_contri_parm where item_no ='13' "
          + " from mkt_contri_parm where item_no ='11' "
          + " and cost_month between :ls_ym1 and :ls_ym2) "
          + " and visit_date between :ls_date1 and :ls_date2 " + " and nvl(free_use_cnt,0) >0"
          + " order by 1,2";
      setString2("kk", wp.itemStr("ex_idno"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    } else {
      liType = 2;
      sql1 = " select card_no , visit_date , free_use_cnt " + " from cms_ppcard_visit "
          + " where card_no =:card_no " + " and mcht_no in (select trim(key_data) mcht_no "
          // + " from mkt_contri_parm " + " where item_no ='13' "
          + " from mkt_contri_parm " + " where item_no ='11' "
          + " and cost_month between :ls_ym1 and :ls_ym2 )"
          + " and visit_date between :ls_date1 and :ls_date2 " + " and nvl(free_use_cnt,0) >0 "
          + " order by 1,2 ";
      setString2("card_no", wp.itemStr("ex_card_no"));
      setString2("ls_ym1", commString.mid(isDate1, 0, 6));
      setString2("ls_ym2", commString.mid(isDate2, 0, 6));
      setString2("ls_date1", isDate1);
      setString2("ls_date2", isDate2);
    }
    sqlSelect(sql1);

    if (sqlRowNum <= 0)
      return rc;

    for (ll = 0; ll < sqlRowNum; ll++) {
      lsCardNo = colStr(ll, "card_no");
      if (lsK1.length() > 0 && eqIgno(lsK1, lsCardNo) == false) {
        iiRrn++;
        if (iiRrn < 10)
          wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
        else
          wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
        //wp.colSet(iiRrn, "ex_item_no", "13");
        wp.colSet(iiRrn, "ex_item_no", "11");
        wp.colSet(iiRrn, "tt_item_no", ".新貴通貴賓室");
        wp.colSet(iiRrn, "ex_type", "1");
        wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
        wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("11", llCnt));
        wp.colSet(iiRrn, "ex_card_no", lsK1);

        llCnt = 0;
      }

      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      //wp.colSet(iiRrn, "ex_item_no", "13"); 
      wp.colSet(iiRrn, "ex_item_no", "11");
      wp.colSet(iiRrn, "tt_item_no", ".新貴通貴賓室");
      wp.colSet(iiRrn, "ex_date", colStr(ll, "purchase_date"));
      wp.colSet(iiRrn, "ex_cnt", "" + llFree);
      wp.colSet(iiRrn, "ex_card_no", lsCardNo);
      wp.colSet(iiRrn, "opt_show", "disabled");
      lsK1 = lsCardNo;
      llCnt += llFree;
    }

    if (llCnt > 0) {
      iiRrn++;
      if (iiRrn < 10)
        wp.colSet(iiRrn, "ser_num", "0" + (iiRrn + 1));
      else
        wp.colSet(iiRrn, "ser_num", "" + (iiRrn + 1));
      //wp.colSet(iiRrn, "ex_item_no", "13");
      wp.colSet(iiRrn, "ex_item_no", "11");
      wp.colSet(iiRrn, "tt_item_no", ".新貴通貴賓室");
      wp.colSet(iiRrn, "ex_type", "1");
      wp.colSet(iiRrn, "ex_cnt", "" + llCnt);
      wp.colSet(iiRrn, "ex_tot_amt", "" + wfCnt2amt("13", llCnt));
      wp.colSet(iiRrn, "ex_card_no", lsK1);
    }

    iiListCnt = (iiRrn + 1);

    return rc;
  }

  double wfCnt2amt(String lsItem, int liUseCnt) {
    double lmAmt = 0;
    if (empty(lsItem) || liUseCnt <= 0)
      return lmAmt;

    strSql = "select * from mkt_contri_cnt2amt_parm" + " where item_no =?";
    setString2(1, lsItem);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("未定義 卡片權益[" + lsItem + "] 消費門檻參數");
      return 0;
    }

    if (ibNew) {
      for (int ii = 1; ii <= 6; ii++) {
        String col1 = "use_cnt_n" + ii;
        if (colInt(col1) == 0)
          continue;
        if (colInt(col1) > liUseCnt)
          break;
        lmAmt = colNum("tot_amt_n" + ii);
      }
    } else {
      for (int ii = 1; ii <= 6; ii++) {
        String col1 = "use_cnt_o" + ii;
        if (colInt(col1) == 0)
          continue;
        if (colInt(col1) > liUseCnt)
          break;
        lmAmt = colNum("tot_amt_o" + ii);
      }
    }

    return lmAmt;
  }

  @Override
  public void dataCheck() {
    if (wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_card_no")) {
      errmsg("身分證ID, 卡號: 不可空白");
      return;
    }


    // -OK-
    if (wp.itemEmpty("ex_idno") == false) {
      strSql = "select id_p_seqno from crd_idno" + " where id_no =?" + commSqlStr.rownum(1);
      setString2(1, wp.itemStr("ex_idno"));
    } else if (wp.itemEmpty("ex_card_no") == false) {
      strSql = "select id_p_seqno from crd_card" + " where card_no =?" + commSqlStr.rownum(1);
      setString2(1, wp.itemStr("ex_card_no"));
    }
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("不是本行卡友 OR 卡號");
      return;
    }

    idPseqno = colStr("id_p_seqno");
    busi.func.CrdFunc ooo = new busi.func.CrdFunc();
    ooo.setConn(wp.getConn());
    try {
      ibNew = ooo.idnoIsNew(idPseqno);
    } catch (ParseException e) {
      ibNew = false;
    }

    //
    isDate1 = commString.mid(wp.itemStr("ex_date1"), 0, 6);
    if (empty(isDate1))
      isDate1 = "190001";
    isDate2 = commString.mid(wp.itemStr("ex_date2"), 0, 6);
    if (empty(isDate2))
      isDate2 = "299912";
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    dataCheck();
    if (rc != 1)
      return rc;

    try {
      wfGetItem08();
      wfGetItem09();
      wfGetItem10();
      //wfGetItem13();
      wfGetItem11();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    return rc;
  }

}
