/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-0621:           JH    p_xxx >>acno_p_xxx
* 109-04-28  V1.00.01  Tanwei       updated for project coding standard      *
* 109-12-24  V1.00.02  Justin         parameterize sql
******************************************************************************/
package rskr02;
import ofcapp.BaseAction;

public class Rskq0920 extends BaseAction {
  String isAcctKey = "";

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
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
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
    try {
      if (eqIgno(wp.respHtml, "rskq0920")) {
        ddlbList("dddw_emend", wp.colStr("ex_emend"), "ecsfunc.DeCodeRsk.emend_type");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskq0920")) {
        wp.optionKey = wp.itemStr("ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  boolean queryBefore() {
    isAcctKey = wp.itemStr("ex_acct_key");
    if (!empty(wp.itemStr("ex_card_no"))) {
      if (wp.itemStr("ex_card_no").length() != 15 && wp.itemStr("ex_card_no").length() != 16) {
        errmsg("卡號 : 輸入錯誤!");
        return false;
      }
      if (selectCrdCard() == false) {
        errmsg("卡號: 輸入錯誤");
        return false;
      }

      zzVipColor(wp.itemStr2("ex_card_no"));

    } else if (!empty(wp.itemStr("ex_acct_key"))) {
      if (isAcctKey.length() != 8 && isAcctKey.length() != 10 && isAcctKey.length() != 11) {
        errmsg("帳戶帳號: 輸入錯誤");
        return false;
      }
//      if (isAcctKey.length() == 8)
//        isAcctKey += "000";
      if (isAcctKey.length() == 10)
        isAcctKey += "0";
      if (selectActAcno() == false) {
        errmsg("帳戶帳號: 輸入錯誤");
        return false;
      }

      zzVipColor(wp.itemNvl("ex_acct_tpye", "01") + isAcctKey);

    }
    String sqlCorp = " select " + " chi_name " + " from crd_corp " + " where corp_p_seqno = ? ";
    if (!empty(sqlStr("corp_p_seqno"))) {
      sqlSelect(sqlCorp, new Object[] {sqlStr("corp_p_seqno")});
      if (sqlRowNum > 0) {
        wp.colSet("corp_name", sqlStr("chi_name"));
      }
    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.logSql = true;
    if (empty(wp.itemStr("ex_acct_key")) && empty(wp.itemStr("ex_card_no"))) {
      errmsg("卡號, 帳戶帳號: 不可同時空白");
      return;
    }

    if (wp.itemEq("ex_emend", "3") || wp.itemEq("ex_emend", "4")) {
      if (empty(wp.itemStr("ex_card_no"))) {
        errmsg("卡號不可空白");
        return;
      }
    }
    if (queryBefore() == false) {
      return;
    }

    String lsWhere = " where log_type ='1'";
    if (wp.itemEq("ex_emend", "4")) {
      lsWhere += " and kind_flag ='C'" + sqlCol(wp.itemStr("ex_card_no"), "card_no");
    } else {
      lsWhere += " and kind_flag ='A' and acno_p_seqno = ? ";
      setString(sqlStr("acno_p_seqno"));
    }

    if (!wp.itemEq("ex_emend", "5")) {
      lsWhere += " and nvl(emend_type,'')<>'5' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + " log_date ," + " emend_type ,"
        + " decode(emend_type,'5',bef_loc_cash,bef_loc_amt) as wk_amt_bef ,"
        + " decode(emend_type,'5',aft_loc_cash,aft_loc_amt) as wk_amt_aft ," + " adj_loc_flag ,"
        + " mod_user ," + " log_reason ," + " mail_comp_yn ," + " apr_user ," + " apr_date ,"
        + " (select wf_desc from ptr_sys_idtab where wf_type=decode(A.adj_loc_flag,'1','ADJ_REASON_UP','ADJ_REASON_DOWN') and wf_id =A.log_reason) as tt_log_reason ";
    wp.daoTable = "rsk_acnolog A";
    wp.whereOrder = " order by 1 Desc";

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

  void queryAfter() {
    if (!empty(wp.itemStr("ex_acct_key"))) {
      selectPseqnoByIdNo();
    } else if (!empty(wp.itemStr("ex_card_no"))) {
      selectPseqnoByCardNo();
    }
    selectCondData();

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (eqIgno(wp.colStr(ii, "adj_loc_flag"), "1")) {
        wp.colSet(ii, "tt_adj_loc_flag", "調高");
      } else if (eqIgno(wp.colStr(ii, "adj_loc_flag"), "2")) {
        wp.colSet(ii, "tt_adj_loc_flag", "調低");
      } else if (eqIgno(wp.colStr(ii, "adj_loc_flag"), "3")) {
        wp.colSet(ii, "tt_adj_loc_flag", "不調整");
      }
    }

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
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  boolean selectCrdCard() {

    String sql1 = " select acno_p_seqno from crd_card where card_no = ? ";
    String sql2 = " select acno_p_seqno ," + " uf_acno_name(acno_p_seqno) as db_acno_name ,"
        + " id_p_seqno ," + " corp_p_seqno " + " from act_acno " + " where acno_p_seqno =?";

    sqlSelect(sql1, new Object[] {wp.itemStr2("ex_card_no")});

    if (sqlRowNum > 0) {
      sqlSelect(sql2, new Object[] {sqlStr("acno_p_seqno")});
      if (sqlRowNum <= 0)
        return false;
    } else
      return false;

    return true;
  }

  boolean selectActAcno() {
    String sql2 = "select acno_p_seqno ," + " id_p_seqno ," + " corp_p_seqno ,"
        + " uf_acno_name(acno_p_seqno) as db_acno_name " + " from act_acno "
        + " where acct_key = ? " + " and acct_type =nvl(?,'01')";
    sqlSelect(sql2, new Object[] {isAcctKey, wp.itemStr("ex_acct_type")});
    if (sqlRowNum <= 0)
      return false;
    return true;
  }


  void sumCreditAmt() {
    String sql3 = "select sum(line_of_credit_amt) as db_tot_creit_amt " + " from act_acno "
        + " where acno_p_seqno in (select acno_p_seqno from crd_card where id_p_seqno =?"
        + " and current_code='0' and acno_flag<>'Y') ";
    sqlSelect(sql3, new Object[] {sqlStr("id_p_seqno")});
    if (sqlRowNum <= 0)
      return;
    wp.colSet("db_tot_credit_amt", sqlStr("db_tot_creit_amt"));
  }

  void selectPseqnoByCardNo() {
    String sql4 =
        " select major_id_p_seqno as cond_id_p_seqno ," + " acno_p_seqno as cond_p_seqno ,"
            + " card_no as cond_card_no " + " from crd_card " + " where card_no = ?";
    sqlSelect(sql4, new Object[] {wp.itemStr("ex_card_no")});
    if (sqlRowNum <= 0)
      return;
  }

  void selectPseqnoByIdNo() {
    String sql5 = "select id_p_seqno as cond_id_p_seqno ," + " acno_p_seqno as cond_p_seqno "
        + " from act_acno " + " where acct_key = ?";
    sqlSelect(sql5, new Object[] {isAcctKey});
    if (sqlRowNum <= 0)
      return;
  }

  void selectCondData() {
    // ** 姓名
    String sql6 = "select " + wp.sqlID + " uf_idno_name(?) as db_acno_name " + " from dual ";
    sqlSelect(sql6, new Object[] {sqlStr("cond_id_p_seqno")});
    if (sqlRowNum > 0)
      wp.colSet("db_acno_name", sqlStr("db_acno_name"));
    // **acno_flag
    String sql71 = " select " + " acno_flag " + " from act_acno " + " where acno_p_seqno = ? ";

    sqlSelect(sql71, new Object[] {sqlStr("cond_p_seqno")});

    // ** ID 總歸戶信用額度
    String sql7 = "select " + " sum(line_of_credit_amt) as db_tl_credit_amt " + " from act_acno "
        + " where id_p_seqno =?";
    sqlSelect(sql7, new Object[] {sqlStr("cond_id_p_seqno")});
    if (sqlRowNum > 0) {
      if (eqIgno(sqlStr("acno_flag"), "1") || eqIgno(sqlStr("acno_flag"), "3")) {
        wp.colSet("db_tot_credit_amt", sqlStr("db_tl_credit_amt"));
      } else {
        wp.colSet("db_tot_credit_amt", "--");
      }
    }

    // ** 子卡額度

    if (wp.itemEq("ex_emend", "4") && !empty(wp.itemStr("ex_card_no"))) {
      String sql8 =
          "select " + " indiv_crd_lmt as db_crd_amt " + " from crd_card " + " where card_no =?";
      sqlSelect(sql8, new Object[] {wp.itemStr("ex_card_no")});
      if (sqlRowNum > 0)
        wp.colSet("db_son_loc_amt", sqlStr("db_crd_amt"));
    }

    // ** 帳戶信用額度

    String sql9 = "select " + " line_of_credit_amt as db_credit_amt " + " from act_acno "
        + " where acno_p_seqno =?";
    sqlSelect(sql9, new Object[] {sqlStr("cond_p_seqno")});
    if (sqlRowNum > 0)
      wp.colSet("db_bef_loc_amt", sqlStr("db_credit_amt"));

    // ** 預借現金額度

    String sql10 = "select " + " line_of_credit_amt_cash as db_credit_amt_cash " + " from act_acno "
        + " where acno_p_seqno =?";
    sqlSelect(sql10, new Object[] {sqlStr("cond_p_seqno")});
    if (sqlRowNum > 0)
      wp.colSet("db_credit_cash", sqlStr("db_credit_amt_cash"));

    // ** 指撥額度

    String sql11 = "select " + " combo_cash_limit as db_combo_limit " + " from act_acno "
        + " where acno_p_seqno =?";
    sqlSelect(sql11, new Object[] {sqlStr("cond_p_seqno")});
    if (sqlRowNum > 0)
      wp.colSet("db_combo_cash_limit", sqlStr("db_combo_limit"));

    // --
    String sql12 = " select " + " birthday , " + " sex , "
        + " home_area_code1||'-'||home_tel_no1||'-'||home_tel_ext1 as wk_home_tel "
        + " from crd_idno " + " where id_p_seqno = ? ";
    sqlSelect(sql12, new Object[] {sqlStr("cond_id_p_seqno")});
    if (sqlRowNum > 0) {
      wp.colSet("birthday", sqlStr("birthday"));
      if (eqIgno(sqlStr("sex"), "1")) {
        wp.colSet("sex", "男");
      } else {
        wp.colSet("sex", "女");
      }
      wp.colSet("wk_home_tel", sqlStr("wk_home_tel"));
    }

    // --acct_type acct_key
    String sql13 = " select " + " acct_type , " + " acct_key " + " from act_acno "
        + " where acno_p_seqno = ? ";
    sqlSelect(sql13, new Object[] {sqlStr("cond_p_seqno")});
    if (sqlRowNum > 0) {
      wp.colSet("ex_acct_type", sqlStr("acct_type"));
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
    }


  }



}
