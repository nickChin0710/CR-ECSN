/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;
/** 線上持卡人信用額度調整主管覆核
 * 2019-0814   JH    modify
 * 2019-0628:  JH    modify
 * 2019-0619:  JH    p_xxx >>acno_pxxx
   2019-0315:  JH    totoal
 * 2018-0919:	JH		遇臨調終止
 * */

import ofcapp.BaseAction;

public class Rskp0920 extends BaseAction {

  taroko.base.CommDate commDate = new taroko.base.CommDate();

  int ilAmtAcno = 0, ilAmCorp = 0;

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
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
    // TODO Auto-generated method stub

  }

  boolean querybefore() {
    String sql1 = " select al_amt as amt_acno , " + " al_amt02 as amt_corp " + " from sec_amtlimit "
        + " where al_level in (select usr_amtlevel from sec_user where usr_id = ? )";

    sqlSelect(sql1, new Object[] {wp.loginUser});
    if (sqlRowNum <= 0) {
      return false;
    }
    wp.colSet("amt_acno", sqlStr("amt_acno"));
    wp.colSet("amt_corp", sqlStr("amt_corp"));

    ilAmtAcno = sqlInt("amt_acno");
    ilAmCorp = sqlInt("amt_corp");

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (querybefore() == false) {
      alertErr2("無法取得 [額度層級]");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_log_date1"), wp.itemStr("ex_log_date2")) == false) {
      alertErr2("異動日期起迄：輸入錯誤");
      return;
    }

    String lsWhere =
        " where 1=1 and log_type ='1' and log_mode='1' and emend_type in ('1','2','3','4','5') "
            + " and   kind_flag ='A' and apr_flag<>'Y' " // in ('A','C')
            + sqlCol(wp.itemStr("ex_mod_user"), "mod_user")
            + sqlCol(wp.itemStr("ex_log_date1"), "log_date", ">=")
            + sqlCol(wp.itemStr("ex_log_date2"), "log_date", "<=")
            + sqlCol(wp.itemStr("ex_card_no"), "card_no") + sqlCol(wp.loginUser, "mod_user", "<>") // -不可覆核本人異動-
    ;

    if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
          + sqlCol(wp.itemStr2("ex_idno"), "id_no") + " ) ";
    }

    // query_sum(ls_where);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // void query_sum(String ls_where){
  // String sql1 = " select "
  // + " count(*) as db_cnt "
  // +", sum(decode(adj_loc_flag,'1',decode(emend_type,'5',aft_loc_cash,aft_loc_amt),0)) as li_up"
  // +", sum(decode(adj_loc_flag,'2',decode(emend_type,'5',aft_loc_cash,aft_loc_amt),0)) as li_down"
  // + " from rsk_acnolog "
  // +ls_where
  // ;
  //
  // sqlSelect(sql1);
  //
  // wp.col_set("wk_down_amt", ""+sql_num("li_down"));
  // wp.col_set("wk_up_amt", ""+sql_num("li_up"));
  // wp.col_set("tl_cnt", ""+sql_num("db_cnt"));
  // }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " acct_type ," + " uf_acno_key(acno_p_seqno) as acct_key ,"
        + " id_p_seqno ," + " corp_p_seqno ," + " acno_p_seqno ," + " kind_flag ," + " card_no ,"
        + " bef_loc_amt ," + " aft_loc_amt ," + " bef_loc_cash ," + " aft_loc_cash ,"
        + " log_reason ," + " adj_loc_flag ," + " log_date ," + " log_remark ,"
        + " uf_acno_name(acno_p_seqno) as db_idno_name ," + " emend_type ," + " apr_flag ,"
        + " fh_flag ," + " decode(emend_type,'5',bef_loc_cash,bef_loc_amt) as wk_bef_amt ," // 預借現金
        + " decode(emend_type,'5',aft_loc_cash,aft_loc_amt) as wk_aft_amt ," // 預借現金
        + " mod_user ," + " mod_time ," + " mod_seqno ," + " hex(rowid) as rowid , "
        + " apr_user , " + " sms_flag , "
        + " decode(adj_loc_flag,'1','調高','2','調低') as tt_adj_loc_flag ,"
        // + " decode(sign(decode(emend_type,'5',bef_loc_cash,bef_loc_amt) -
        // decode(emend_type,'5',aft_loc_cash,aft_loc_amt)) , 1 ,
        // decode(emend_type,'5',bef_loc_cash,bef_loc_amt) -
        // decode(emend_type,'5',aft_loc_cash,aft_loc_amt),0) as db_lower , "
        // + " decode(sign(decode(emend_type,'5',bef_loc_cash,bef_loc_amt) -
        // decode(emend_type,'5',aft_loc_cash,aft_loc_amt)) , -1 ,
        // decode(emend_type,'5',aft_loc_cash,aft_loc_amt) -
        // decode(emend_type,'5',bef_loc_cash,bef_loc_amt),0) as db_upper "
        + " decode(adj_loc_flag,'1',decode(emend_type,'5',aft_loc_cash - bef_loc_cash,aft_loc_amt - bef_loc_amt),0) as db_upper"
        + ", decode(adj_loc_flag,'2',decode(emend_type,'5',bef_loc_cash - aft_loc_cash,bef_loc_amt - aft_loc_amt),0) as db_lower";
    wp.daoTable = " rsk_acnolog ";
    wp.whereOrder = " order by log_date, mod_time ";
    pageQuery2(null);
    wp.listCount[0] = sqlRowNum;
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
    queryAfter(sqlRowNum);
  }

  @Override
  public boolean rowIsShow(int ll) throws Exception {
    double liAmt = 0, liAmt2 = 0, liTlAmt = 0;
    String lsKkPseqno = wp.colStr(ll, "acno_p_seqno");
    String lsIdPseqno = wp.colStr(ll, "id_p_seqno");

    // --
    String sql0 = " select acno_flag from act_acno where acno_p_seqno = ? ";

    // -未調額度-
    String sql1 = " select sum(line_of_credit_amt) as db_amt from act_acno where id_p_seqno = ? "
        + " and acct_type not in (select acct_type from rsk_acnolog where log_type ='1' and log_mode='1' and emend_type in ('1','2','3','4','5') "
        + " and kind_flag ='A' and apr_flag<>'Y' and id_p_seqno = ? ) "
        + " and acno_flag in ('1','3') ";

    // -未調額度-
    String sql2 =
        " select decode(emend_type,'5',aft_loc_cash,aft_loc_amt) as li_amt2 from rsk_acnolog where log_type ='1' and log_mode='1' and emend_type in ('1','2','3','4','5') "
            + " and kind_flag ='A' and apr_flag<>'Y' and acct_type <> ? and id_p_seqno = ? ";

    if (wp.colEq(ll, "emend_type", "1") || wp.colEq(ll, "emend_type", "3")) {

      sqlSelect(sql0, new Object[] {lsKkPseqno});
      if (sqlRowNum > 0) {
        if (eqIgno(sqlStr("acno_flag"), "Y")) {
          liAmt = 0;
          liAmt2 = 0;
        } else {
          sqlSelect(sql1, new Object[] {lsIdPseqno, lsIdPseqno});
          if (sqlRowNum > 0)
            liAmt = sqlNum("db_amt");
          else
            liAmt = 0;

          sqlSelect(sql2, new Object[] {wp.colStr(ll, "acct_type"), lsIdPseqno});
          if (sqlRowNum > 0)
            liAmt2 = sqlNum("li_amt2");
          else
            liAmt2 = 0;
        }
        liTlAmt = (int) (liAmt + liAmt2 + wp.colNum(ll, "wk_aft_amt"));
        if (ilAmtAcno < liTlAmt)
          return false;
      } else
        return false;
    } else if (wp.colEq(ll, "emend_type", "2")) {
      liTlAmt = wp.colNum(ll, "wk_aft_amt");
      if (ilAmCorp < liTlAmt)
        return false;
    } else if (wp.colEq(ll, "emend_type", "5")) {
      liTlAmt = wp.colNum(ll, "wk_aft_amt");
      if (ilAmtAcno < liTlAmt)
        return false;
    }

    return true;
  }

  void queryAfter(int llNrow) {

    String[] aaCde = new String[] {"1", "5", "6", "N"};
    String[] aaDesc = new String[] {".調整額度", ".預借現金額度調整", ".覆審降額", " "};

    double liUpAmt = 0, liDownAmt = 0;
    String sql1 = " select id_no from crd_idno where id_p_seqno = ? ";
    String sql2 =
        " select count(*) as db_line from mkt_line_cust where id_no = ? and status_code = '0' ";
    // -check opt ON/OFF-
    for (int ii = 0; ii < llNrow; ii++) {
      String lsEmend = wp.colStr(ii, "emend_type");
      double lmAftAmt = wp.colNum(ii, "wk_aft_amt");
      double lmBefAmt = wp.colNum(ii, "wk_bef_amt");

      // if (func.check_Apr_auth(ls_emend,lm_bef_amt,lm_aft_amt)!=1) {
      // wp.col_set(ii, "opt_show", "disabled");
      // }

      liUpAmt += wp.colNum(ii, "db_upper");
      liDownAmt += wp.colNum(ii, "db_lower");
      // if(wp.col_num(ii,"db_lower")<0) wp.col_set(ii,"db_lower", "0");
      // if(wp.col_num(ii,"db_upper")<0) wp.col_set(ii,"db_upper", "0");
      // --
      String smsFlag = wp.colStr(ii, "sms_flag");
      wp.colSet(ii, "tt_sms_flag", commString.decode(smsFlag, aaCde, aaDesc));

      // --check line 推播
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "id_p_seqno")});
      if (sqlRowNum > 0) {
        sqlSelect(sql2, new Object[] {sqlStr("id_no")});
        if (sqlNum("db_line") > 0)
          wp.colSet(ii, "wk_line", "Y");
        else
          wp.colSet(ii, "wk_line", "N");
      } else {
        wp.colSet(ii, "wk_line", "N");
      }
    }
    wp.colSet("wk_down_amt", liDownAmt);
    wp.colSet("wk_up_amt", liUpAmt);
    wp.colSet("tl_cnt", llNrow);
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;
    Rskp0920Func func = new Rskp0920Func();
    func.setConn(wp);

    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("rowid");
    optNumKeep(wp.listCount[0]);
    if (optToIndex(aaOpt[0]) < 0) {
      alertErr2("請選取欲處理之批號");
      return;
    }

    int rr = -1;
    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = this.optToIndex(aaOpt[ii]);
      if (rr < 0) {
        continue;
      }
      func.varsSet("rowid", wp.itemStr(rr, "rowid"));
      func.varsSet("mod_seqno", wp.itemStr(rr, "mod_seqno"));
      func.varsSet("wk_bef_amt", wp.itemStr(rr, "wk_bef_amt"));
      func.varsSet("wk_aft_amt", wp.itemStr(rr, "wk_aft_amt"));

      optOkflag(rr);
      int liRc = func.dataProc();
      sqlCommit(liRc);
      optOkflag(rr, liRc);
      if (liRc == 1) {
        llOk++;
      } else {
        llErr++;
      }
    }
    alertMsg("覆核完成; OK=" + llOk + ", ERR=" + llErr);
    // ---------> call batch CMS_A002 !!
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    String lsDate1 = "";
    if (empty(strAction)) {
      lsDate1 = commDate.dateAdd(getSysDate(), 0, 0, -5);
      wp.colSet("ex_log_date1", lsDate1);
      wp.colSet("ex_log_date2", getSysDate());
    }

    wp.sqlCmd = "select al_amt as apr_amt, al_amt02 as apr_amt02" + " from sec_amtlimit"
        + " where al_level in (select usr_amtlevel from sec_user where usr_id=?)";
    setString2(1, wp.loginUser);
    sqlSelect();
    if (sqlRowNum > 0) {
      wp.colSet("apr_amt", sqlStr("apr_amt"));
      wp.colSet("apr_amt02", sqlStr("apr_amt02"));
    }

  }

}
