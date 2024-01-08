package actq01;
/**
 * 2017/05/15   Alex    Bug fixed
 * 2019/02/22   Alex    Tab
 * 111/10/23    jiangyigndong  updated for project coding standard
 * 111/11/06    Simon          1.avoid error from BasePage.sqlCol(x,x,x) 
 *                             2.tabClick() alternative solution 
 */

import busi.func.ColFunc;
import ofcapp.BaseAction;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actq0040 extends BaseAction {
  String lsAcctKey = "";
  String mProgName = "actq0040";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
        strAction = "new";
        clearFunc(); break;
      case "Q": //查詢功能
        queryFunc();
        tabClick();
        break;
      case "R": // -資料讀取-
        dataRead(); break;
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
        saveFunc(); break;
      case "M": //瀏覽功能 :skip-page-
        queryRead(); break;
      case "S": //動態查詢--
        querySelect(); break;
      case "L": //清畫面--
        strAction = "";
        clearFunc(); break;
      case "C": // -資料處理-
        procFunc(); break;
      default:
        alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "actq0040")) {
        wp.optionKey = wp.colStr(0, "ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
    }
    catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "actq0040")) {
        wp.optionKey = wp.colStr(0, "ex_curr_code");
        dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type='DC_CURRENCY'");
      }
    }
    catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    wp.colSet("ex_scope_cnt", "0");
    wp.colSet("ex_acno_name", "");
    wp.colSet("ex_stmt", "");
    if (empty(wp.itemStr("ex_acct_key"))==false || empty(wp.itemStr("ex_card_no"))==false ) {
      wp.colSet("ex_pay_no_1", "");
    }

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("入帳起迄日期 : 起迄錯誤 !");
      return;
    }
    //if (wp.iempty("ex_date1")) {
    //	alertErr("入帳日期(起) : 不可空白 !");
    //	return;
    //}

    if (empty(wp.itemStr("ex_acct_key")) &&
            empty(wp.itemStr("ex_card_no")) &&
            empty(wp.itemStr("ex_pay_no_1"))) {
      alertErr("帳戶帳號 , 卡號 , 繳款編號 不可同時空白 !");
      return;
    }

    //start-查詢權限檢查，參考【f_auth_query】
    String lm_qry_key = "";
    ColFunc colfunc = new ColFunc();
    colfunc.setConn(wp);
    if (!empty(wp.itemStr("ex_acct_key"))) {
      lm_qry_key = wp.itemStr("ex_acct_key");
    }
    else if (!empty(wp.itemStr("ex_card_no"))) {
      lm_qry_key = wp.itemStr("ex_card_no");
    }
    else if (!empty(wp.itemStr("ex_pay_no"))) {
      selectData3(wp.itemStr("ex_pay_no"));
      lm_qry_key = sqlStr("acct_key");
    }
    else if (!empty(wp.itemStr("ex_pay_no_1"))) {
      selectData3(wp.itemStr("ex_pay_no_1"));
      lm_qry_key = sqlStr("acct_key");
    }
    else {
      lm_qry_key = "";
    }

    if (colfunc.fAuthQuery(mProgName, lm_qry_key) != 1) {
      alertErr(colfunc.getMsg());
      return;
    }
    //end-查詢權限檢查，參考【f_auth_query】

    lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));

    if (!empty(lsAcctKey)) {
      if (lsAcctKey.length() != 11) {
        alertErr("帳戶帳號輸入錯誤 !");
        return;
      }
      zzVipColor(wp.itemNvl("ex_acct_type", "01") + lsAcctKey);
    }

    if (wp.itemEmpty("ex_card_no") == false) {
      zzVipColor(wp.itemStr("ex_card_no"));
    }


    if (getPseqno() == false) {
      return;
    }

    if (checkPseqno() == false) {
      alertErr("帳戶號碼不存在 !");
      return;
    }

    String ls_where = " where 1=1 "
          //+ sqlCol(wp.itemStr("ex_p_seqno"), "A.p_seqno")
          //+ sqlCol(wp.itemStr("ex_date1"), "A.acct_date", ">=")
          //+ sqlCol(wp.itemStr("ex_date2"), "A.acct_date", "<=");
            + " and A.p_seqno = '" + wp.itemStr("ex_p_seqno") + "'";
    if (!empty(wp.itemStr("ex_date1"))) {
      ls_where += " and A.acct_date >= '" + wp.itemStr("ex_date1") + "'";
    }
    if (!empty(wp.itemStr("ex_date2"))) {
      ls_where += " and A.acct_date <= '" + wp.itemStr("ex_date2") + "'";
    }
    if (!empty(wp.itemStr("ex_curr_code"))) {
      ls_where += " and " + wp.sqlID + "uf_dc_curr(A.curr_code) ='" + wp.itemStr("ex_curr_code") + "'";
    }

    wp.whereStr = ls_where;

    String ls_sql = "";
    int li_sel_totcnt = 0;

    ls_sql  = " select ";
    ls_sql += " count(*) colh_sel_totcnt ";
    ls_sql += " from act_jrnl a ";
    ls_sql += wp.whereStr;
    sqlSelect(ls_sql);
    if (sqlRowNum > 0) {
      li_sel_totcnt = sqlInt("colh_sel_totcnt");
    } else {
      li_sel_totcnt = 0;
    }
    wp.colSet("ex_scope_cnt", li_sel_totcnt);

    if (li_sel_totcnt == 0) {
      alertErr("此條件查無資料 ");
      return;
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  boolean getPseqno() throws Exception {
    String ss = "", ss2 = "", sql1 = "";
    if (wp.itemEmpty("ex_acct_key") == false) {
      ss = commString.acctKey(wp.itemStr("ex_acct_key"));
      ss2 = wp.itemNvl("ex_acct_type", "01");
      if (ss.length() != 11) {
        alertErr("帳戶帳號輸入錯誤 帳號:" + ss);
        return false;
      }

      sql1 = " select "
              + " uf_acno_name(p_seqno) as ex_acno_name , "
              + " stmt_cycle , "
              + " payment_no , "
              + " p_seqno "
              + " from act_acno "
              + " where acct_key = ? "
              + " and acct_type = ? "
              + " and acno_p_seqno = p_seqno "
      ;

      sqlSelect(sql1, new Object[]{ss, ss2});

      if (sqlRowNum <= 0) {
        errmsg("查無資料");
        return false;
      }

      wp.colSet("ex_acno_name", sqlStr("ex_acno_name"));
      wp.colSet("ex_stmt", sqlStr("stmt_cycle"));
      wp.colSet("ex_pay_no_1", sqlStr("payment_no"));
      wp.colSet("ex_pay_no", "");
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      wp.itemSet("ex_p_seqno", sqlStr("p_seqno"));
      return true;

    }
    else if (wp.itemEmpty("ex_card_no") == false) {
      ss = wp.itemStr("ex_card_no");
      sql1 = " select "
              + " uf_acno_name(p_seqno) as ex_acno_name , "
              + " stmt_cycle , "
              + " payment_no , "
              + " p_seqno , "
              + " acct_type , "
              + " acct_key "
              + " from act_acno "
              + " where acno_p_seqno in (select acno_p_seqno from crd_card where card_no =?)"
      ;

      sqlSelect(sql1, new Object[]{ss});

      if (sqlRowNum <= 0) {
        errmsg("查無資料");
        return false;
      }

      wp.colSet("ex_acno_name", sqlStr("ex_acno_name"));
      wp.colSet("ex_stmt", sqlStr("stmt_cycle"));
      wp.colSet("ex_pay_no_1", sqlStr("payment_no"));
      wp.colSet("ex_pay_no", "");
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      wp.colSet("ex_acct_type", sqlStr("acct_type"));
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
      wp.itemSet("ex_p_seqno", sqlStr("p_seqno"));
      return true;

    }
    else if (wp.itemEmpty("ex_pay_no") == false) {
      ss = wp.itemStr("ex_pay_no");
      sql1 = " select "
              + " acct_type , "
              + " acct_key , "
              + " uf_acno_name(p_seqno) as ex_acno_name , "
              + " stmt_cycle , "
              + " p_seqno "
              + " from act_acno "
              + " where payment_no =?"
              + " and acno_p_seqno = p_seqno "
      ;

      sqlSelect(sql1, new Object[]{ss});

      if (sqlRowNum <= 0) {
        errmsg("查無資料");
        return false;
      }

      wp.colSet("ex_acno_name", sqlStr("ex_acno_name"));
      wp.colSet("ex_stmt", sqlStr("stmt_cycle"));
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      wp.colSet("ex_acct_type", sqlStr("acct_type"));
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
      wp.itemSet("ex_p_seqno", sqlStr("p_seqno"));
      return true;
    }
    else if (wp.itemEmpty("ex_pay_no_1") == false) {
      ss = wp.itemStr("ex_pay_no_1");
      sql1 = " select "
              + " acct_type , "
              + " acct_key , "
              + " uf_acno_name(p_seqno) as ex_acno_name , "
              + " stmt_cycle , "
              + " p_seqno "
              + " from act_acno "
              + " where payment_no =?"
              + " and acno_p_seqno = p_seqno "
      ;

      sqlSelect(sql1, new Object[]{ss});

      if (sqlRowNum <= 0) {
        errmsg("查無資料");
        return false;
      }

      wp.colSet("ex_acno_name", sqlStr("ex_acno_name"));
      wp.colSet("ex_stmt", sqlStr("stmt_cycle"));
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      wp.colSet("ex_acct_type", sqlStr("acct_type"));
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
      wp.itemSet("ex_p_seqno", sqlStr("p_seqno"));
      return true;
    }

    return true;
  }

  boolean checkPseqno() throws Exception {
    String sql1 = " select count(*) as db_cnt from act_acno where p_seqno = ? ";
    sqlSelect(sql1, new Object[]{wp.itemStr("ex_p_seqno")});
    if (sqlNum("db_cnt") <= 0)
      return false;
    return true;
  }

  @Override
  public void queryRead() throws Exception {
    //select_noLimit();
    wp.pageControl();
    wp.selectSQL = " A.* , "
            + " decode(uf_tt_acct_code(A.acct_code),'',A.acct_code,uf_tt_acct_code(A.acct_code)) as tt_acct_code , "
            + " decode(A.tran_class,'B','帳單','P','繳款','D','銷帳','A','調整') as tt_tran_class , "
            + " decode(A.dr_cr,'D','-','C','+') as tt_dr_cr , "
            + " decode(A.cash_type,'1','溢付提領','2','開立即支票','3','匯入本行帳戶','4','匯入它行帳戶','5','CRS溢繳款超過4萬美金') as tt_cash_type "
    ;
    wp.daoTable = " act_jrnl A ";
    wp.whereOrder = " order by A.crt_date Asc, A.crt_time Asc, A.enq_seqno Asc ";
    pageQuery();
    wp.setListCount(9);

    if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
    }
    wp.setPageValue();
    query_After(wp.selectCnt);
  }

  void query_After(int ll_nrow) throws Exception {
    int rr = 0;
    String ss = "", ss2 = "";
    //A1
    for (int ii = 0; ii < ll_nrow; ii++) {
      wp.colSet(rr, "A1_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A1_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A1_acct_code", wp.colStr(ii, "acct_code"));
      wp.colSet(rr, "A1_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
      wp.colSet(rr, "A1_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A1_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A1_dc_item_bal", wp.colStr(ii, "dc_item_bal"));

      wp.colSet(rr, "dc_transaction_amt", wp.colStr(ii, "curr_code").equals("901") ? wp.colStr(ii, "transaction_amt") : wp.colStr(ii, "dc_transaction_amt"));
      wp.colSet(rr, "dc_jrnl_bal", wp.colStr(ii, "curr_code").equals("901") ? wp.colStr(ii, "jrnl_bal") : wp.colStr(ii, "dc_jrnl_bal"));
      wp.colSet(rr, "dc_item_bal", wp.colStr(ii, "curr_code").equals("901") ? wp.colStr(ii, "item_bal") : wp.colStr(ii, "dc_item_bal"));
      wp.colSet(rr, "dc_item_d_bal", wp.colStr(ii, "curr_code").equals("901") ? wp.colStr(ii, "item_d_bal") : wp.colStr(ii, "dc_item_d_bal"));
      if (empty(wp.colStr(ii, "trans_acct_type")))
        wp.colSet(rr, "wk_trans_key", "" + wp.colStr(ii, "trans_acct_key"));
      else
        wp.colSet(rr, "wk_trans_key", wp.colStr(ii, "trans_acct_type") + "-" + wp.colStr(ii, "trans_acct_key"));


      wp.colSet(rr, "A1_dc_jrnl_bal", wp.colStr(ii, "dc_jrnl_bal"));
      if (wp.colNum(ii, "dc_jrnl_bal") < 0) {
        wp.colSet(rr, "A1_bal_style", "col_key");
        wp.colSet(rr, "bal_style", "col_key");
      }
      else {
        wp.colSet(rr, "A1_bal_style", "");
      }
      wp.colSet(rr, "A1_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A1_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A1_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A1_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A1_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A1_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
    }
    wp.listCount[0] = rr;
    rr = 0;
    //A2----------------------------------------------
    for (int ii = 0; ii < ll_nrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "B"))
        continue;

      wp.colSet(rr, "A2_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A2_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A2_acct_code", wp.colStr(ii, "acct_code"));
      wp.colSet(rr, "A2_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
      wp.colSet(rr, "A2_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A2_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A2_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A2_dc_item_bal", wp.colStr(ii, "dc_item_bal"));
      wp.colSet(rr, "A2_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A2_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A2_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A2_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A2_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A2_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
    }
    wp.listCount[1] = rr;
    rr = 0;

    //A3
    for (int ii = 0; ii < ll_nrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type").substring(0, 2);
      if (!eqIgno(ss, "DE") && !eqIgno(ss, "DR"))
        continue;

      wp.colSet(rr, "A3_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A3_acct_code", wp.colStr(ii, "acct_code"));
      wp.colSet(rr, "A3_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
      //wp.colSet(rr, "A3_item_d_bal", wp.colStr(ii, "item_d_bal"));
      wp.colSet(rr, "A3_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A3_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A3_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A3_dc_item_bal", wp.colStr(ii, "dc_item_bal"));
      wp.colSet(rr, "A3_dc_item_d_bal", wp.colStr(ii, "dc_item_d_bal"));
      wp.colSet(rr, "A3_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A3_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A3_adj_reason_code", wp.colStr(ii, "adj_reason_code"));
      wp.colSet(rr, "A3_adj_comment", wp.colStr(ii, "adj_comment"));
      wp.colSet(rr, "A3_c_debt_key", wp.colStr(ii, "c_debt_key"));
      wp.colSet(rr, "A3_debit_item", wp.colStr(ii, "debit_item"));
      wp.colSet(rr, "A3_value_type", wp.colStr(ii, "value_type"));
      wp.colSet(rr, "A3_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A3_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A3_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A3_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A3_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
      ss = "";
    }
    wp.listCount[2] = rr;
    rr = 0;

    //A4
    for (int ii = 0; ii < ll_nrow; ii++) {
      ss = wp.colStr(ii, "tran_class");
      ss2 = wp.colStr(ii, "tran_type");
      if (pos("|P|D", ss) > 0 || (eqIgno(ss, "A") && eqIgno(ss2, "CN01"))) {
        wp.colSet(rr, "A4_tran_type", ss2);
        wp.colSet(rr, "A4_acct_date", wp.colStr(ii, "acct_date"));
        wp.colSet(rr, "A4_acct_code", wp.colStr(ii, "acct_code"));
        wp.colSet(rr, "A4_tt_acct_code", wp.colStr(ii, "tt_acct_code"));
        wp.colSet(rr, "A4_dr_cr", wp.colStr(ii, "dr_cr"));
        wp.colSet(rr, "A4_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
        wp.colSet(rr, "A4_transaction_amt", wp.colStr(ii, "transaction_amt"));
        wp.colSet(rr, "A4_dc_item_bal", wp.colStr(ii, "dc_item_bal"));
        wp.colSet(rr, "A4_item_date", wp.colStr(ii, "item_date"));
        wp.colSet(rr, "A4_reference_no", wp.colStr(ii, "reference_no"));
        wp.colSet(rr, "A4_crt_date", wp.colStr(ii, "crt_date"));
        wp.colSet(rr, "A4_crt_time", wp.colStr(ii, "crt_time"));
        wp.colSet(rr, "A4_curr_code", wp.colStr(ii, "curr_code"));
        wp.colSet(rr, "A4_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
        rr++;
        ss = "";
        ss2 = "";
      }
    }
    wp.listCount[3] = rr;
    rr = 0;

    //A5
    String sql1 = " select lgd_coll_flag from col_lgd_jrnl where 1=1 "
            + " and p_seqno = ? "
            + " and acct_date = ? "
            + " and jrnl_seqno = ? "
            + " and enq_seqno = ? "
            + " and trans_amt = ? "
            + " and apr_date <> '' "
            + this.sqlRownum(1);
    for (int ii = 0; ii < ll_nrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "P")
              && !eqIgno(wp.colStr(ii, "tran_type"), "DR11"))
        continue;
      wp.colSet(rr, "A5_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A5_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A5_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A5_reversal_flag", wp.colStr(ii, "reversal_flag"));
      wp.colSet(rr, "A5_tran_type", wp.colStr(ii, "tran_type"));
      wp.colSet(rr, "A5_tt_tran_type", wp.colStr(ii, "tran_type") + "." + selectTranTypeDesc(wp.colStr(ii, "tran_type")));
      wp.colSet(rr, "A5_payment_rev_amt", wp.colStr(ii, "payment_rev_amt"));
      wp.colSet(rr, "A5_reference_no", wp.colStr(ii, "reference_no"));
      wp.colSet(rr, "A5_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A5_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A5_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A5_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A5_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      wp.colSet(rr, "A5_wk_amt", "" + (wp.colNum(ii, "dc_transaction_amt") - wp.colNum(ii, "payment_rev_amt")));

      sqlSelect(sql1, new Object[]{
              wp.colStr(ii, "p_seqno"), wp.colStr(ii, "acct_date"), wp.colStr(ii, "jrnl_seqno"),
              wp.colStr(ii, "enq_seqno"), wp.colStr(ii, "transaction_amt")
      });
      if (sqlRowNum == 0) {
        wp.colSet(rr, "db_lgd_coll_flag", "");
      }
      else if (sqlRowNum > 0) {
        wp.colSet(rr, "db_lgd_coll_flag", sqlStr("lgd_coll_flag"));
      }

      rr++;
    }
    wp.listCount[4] = rr;
    rr = 0;

    //A6
    for (int ii = 0; ii < ll_nrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type");
      if (!eqIgno(ss, "OP02") && !eqIgno(ss, "OP03"))
        continue;
      wp.colSet(rr, "A6_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A6_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A6_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A6_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A6_dc_item_bal", wp.colStr(ii, "dc_item_bal"));
      wp.colSet(rr, "A6_dc_item_d_bal", wp.colStr(ii, "dc_item_d_bal"));
      wp.colSet(rr, "A6_tt_cash_type", wp.colStr(ii, "tt_cash_type"));
      wp.colSet(rr, "A6_cash_type", wp.colStr(ii, "cash_type"));
      wp.colSet(rr, "A6_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A6_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A6_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A6_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      if (empty(wp.colStr(ii, "trans_acct_type")))
        //wp.colSet(rr, "wk_trans_key", "" + wp.colStr(ii, "trans_acct_key"));
        wp.colSet(rr, "A6_wk_trans_key", "" + wp.colStr(ii, "trans_acct_key"));
      else
        //wp.colSet(rr, "wk_trans_key", wp.colStr(ii, "trans_acct_type") + "-" + wp.colStr(ii, "trans_acct_key"));
        wp.colSet(rr, "A6_wk_trans_key", wp.colStr(ii, "trans_acct_type") + "-" + wp.colStr(ii, "trans_acct_key"));

      rr++;
      ss = "";
    }
    wp.listCount[5] = rr;
    rr = 0;

    //A7
    for (int ii = 0; ii < ll_nrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type");
      if (!eqIgno(ss, "OP01") && !eqIgno(ss, "OP04"))
        continue;

      wp.colSet(rr, "A7_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A7_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A7_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A7_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A7_dc_item_bal", wp.colStr(ii, "dc_item_bal"));
      wp.colSet(rr, "A7_dc_item_d_bal", wp.colStr(ii, "dc_item_d_bal"));
      wp.colSet(rr, "A7_item_date", wp.colStr(ii, "item_date"));
      wp.colSet(rr, "A7_interest_date", wp.colStr(ii, "interest_date"));
      wp.colSet(rr, "A7_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A7_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A7_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A7_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
      ss = "";
    }
    wp.listCount[6] = rr;
    rr = 0;

    //A8
    for (int ii = 0; ii < ll_nrow; ii++) {
      if (!eqIgno(wp.colStr(ii, "tran_class"), "A"))
        continue;
      ss = wp.colStr(ii, "tran_type").substring(0, 2);
      if (!eqIgno(ss, "AI"))
        continue;

      wp.colSet(rr, "A8_acct_date", wp.colStr(ii, "acct_date"));
      wp.colSet(rr, "A8_dr_cr", wp.colStr(ii, "dr_cr"));
      wp.colSet(rr, "A8_tt_dr_cr", wp.colStr(ii, "tt_dr_cr"));
      wp.colSet(rr, "A8_transaction_amt", wp.colStr(ii, "transaction_amt"));
      wp.colSet(rr, "A8_item_bal", wp.colStr(ii, "item_bal"));
      wp.colSet(rr, "A8_item_d_bal", wp.colStr(ii, "item_d_bal"));
      wp.colSet(rr, "A8_crt_date", wp.colStr(ii, "crt_date"));
      wp.colSet(rr, "A8_crt_time", wp.colStr(ii, "crt_time"));
      wp.colSet(rr, "A8_curr_code", wp.colStr(ii, "curr_code"));
      wp.colSet(rr, "A8_dc_transaction_amt", wp.colStr(ii, "dc_transaction_amt"));
      rr++;
    }
    wp.listCount[7] = rr;
    rr = 0;
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
    tabClick();

  }

  public void wfAjaxKey(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =

    selectData1(wp.itemStr("ax_key"), wp.itemStr("ax_type"));
    if (rc != 1) {
      wp.addJSON("acno_name", "　");
      wp.addJSON("stmt_cycle", "　");
      wp.addJSON("payment_no", "　");
      wp.addJSON("p_seqno", "　");
      return;
    }
    wp.addJSON("acno_name", sqlStr("ex_acno_name"));
    wp.addJSON("stmt_cycle", sqlStr("stmt_cycle"));
    wp.addJSON("payment_no", sqlStr("payment_no"));
    wp.addJSON("p_seqno", sqlStr("p_seqno"));
  }

  public void wfAjaxCard(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    selectData2(wp.itemStr("ax_card"));
    if (rc != 1) {
      wp.addJSON("acno_name", "");
      wp.addJSON("stmt_cycle", "");
      wp.addJSON("payment_no", "");
      wp.addJSON("p_seqno", "");
      wp.addJSON("acct_type", "");
      wp.addJSON("acct_key", "");
      return;
    }
    wp.addJSON("acno_name", sqlStr("ex_acno_name"));
    wp.addJSON("stmt_cycle", sqlStr("stmt_cycle"));
    wp.addJSON("payment_no", sqlStr("payment_no"));
    wp.addJSON("p_seqno", sqlStr("p_seqno"));
    wp.addJSON("acct_type", sqlStr("acct_type"));
    wp.addJSON("acct_key", sqlStr("acct_key"));


  }

  public void wfAjaxPay(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    selectData3(wp.itemStr("ax_pay"));
    if (rc != 1) {
      wp.addJSON("acct_type", "");
      wp.addJSON("acno_name", "");
      wp.addJSON("stmt_cycle", "");
      wp.addJSON("acct_key", "");
      wp.addJSON("p_seqno", "");
      return;
    }
    wp.addJSON("acct_type", sqlStr("acct_type"));
    wp.addJSON("acno_name", sqlStr("ex_acno_name"));
    wp.addJSON("stmt_cycle", sqlStr("stmt_cycle"));
    wp.addJSON("acct_key", sqlStr("acct_key"));
    wp.addJSON("p_seqno", sqlStr("p_seqno"));

  }

  public void ajaxChkPayno(TarokoCommon wr) throws Exception {
    super.wp = wr;

    String js_payment_no_1 = wp.itemStr("aj_payment_no_1");
    String js_payno_flag   = "N";
    String js_payment_no   = "";

    String ls_sql = "select "
            + " acct_type , "
            + " acct_key , "
            + " p_seqno "
            + " from act_acno "
            + " where payment_no = ? "
            + " and acno_p_seqno = p_seqno ";
    this.sqlSelect(ls_sql, new Object[]{js_payment_no_1});

    if (sqlRowNum <= 0) {
      String ls_sql2 = "select "
              + " acct_type , "
              + " acct_key , "
              + " payment_no , "
              + " p_seqno "
              + " from act_acno "
              + " where payment_no like substr(:payment_no,1,12)||'%' "
              + " and acno_p_seqno = p_seqno "
              + " fetch first 1 row only ";
      this.setString("payment_no", js_payment_no_1);
      sqlSelect(ls_sql2);
      if (sqlRowNum > 0) {
        js_payment_no = sqlStr("payment_no");
      } else {
        js_payment_no = "";
      }
    } else {
      js_payno_flag   = "Y";
      js_payment_no   = "";
    }

    wp.addJSON("ax_payno_flag", js_payno_flag);
    wp.addJSON("ax_payment_no", js_payment_no);

  }

  void selectData1(String s1, String s2) throws Exception {

    s1 = commString.acctKey(s1);
    if (empty(s1)) {
      if (s1.length() != 11) {
        alertErr("帳戶帳號輸入錯誤 帳號:" + s1);
        return;
      }
    }

    if (empty(s2)) s2 = "01";

    String ls_sql = "select "
            + wp.sqlID + "uf_acno_name(p_seqno) as ex_acno_name , "
            + " stmt_cycle , "
            + " payment_no , "
            + " p_seqno "
            + " from act_acno "
            + " where acct_key = ? "
            + " and acct_type = ? "
            + " and acno_p_seqno = p_seqno ";
    sqlSelect(ls_sql, new Object[]{s1, s2});

    if (sqlRowNum <= 0) {
      alertErr("查無資料: ex_acct_key=" + s1);
    }
    return;
  }

  void selectData2(String s1) throws Exception {
    String ls_sql = "select "
            + wp.sqlID + "uf_acno_name(p_seqno) as ex_acno_name , "
            + " stmt_cycle , "
            + " payment_no , "
            + " p_seqno , "
            + " acct_type , "
            + " acct_key "
            + " from act_acno "
            + " where acno_p_seqno in (select acno_p_seqno from crd_card where card_no =?)";


    sqlSelect(ls_sql, new Object[]{s1});

    if (sqlRowNum <= 0) {
      alertErr("查無資料: ex_card_no=" + s1);
    }
    return;
  }

  void selectData3(String s1) throws Exception {
    String ls_sql = "select "
            + " acct_type , "
            + " acct_key , "
            + wp.sqlID + "uf_acno_name(p_seqno) as ex_acno_name , "
            + " stmt_cycle , "
            + " p_seqno "
            + " from act_acno "
            + " where payment_no =?"
            + " and acno_p_seqno = p_seqno ";
    this.sqlSelect(ls_sql, new Object[]{s1});

    if (sqlRowNum <= 0) {
      alertErr("查無資料: ex_pay_no=" + s1);
    }
    return;
  }

  String selectTranTypeDesc(String ls_tran_type) throws Exception {
    String sql1 = " select "
            + " bill_desc "
            + " from ptr_payment "
            + " where payment_type = ? ";

    sqlSelect(sql1, new Object[]{ls_tran_type});

    if (sqlRowNum > 0) return sqlStr("bill_desc");

    return ls_tran_type;
  }

  void tabClick() {
    wp.colSet("a_click_1", "t_click_1");
    wp.colSet("a_click_2", "t_click_2");
    wp.colSet("a_click_3", "t_click_3");
    wp.colSet("a_click_4", "t_click_4");
    wp.colSet("a_click_5", "t_click_5");
    wp.colSet("a_click_6", "t_click_6");
    wp.colSet("a_click_7", "t_click_7");
    wp.colSet("a_click_8", "t_click_8");
    wp.colSet("a_click_9", "t_click_9");

    String isClick = "";
    isClick = wp.itemStr("tab_click");
    if (eqIgno(isClick, "1")) {
      wp.colSet("a_click_1", "tab_active");
    }
    else if (eqIgno(isClick, "2")) {
      wp.colSet("a_click_2", "tab_active");
    }
    else if (eqIgno(isClick, "3")) {
      wp.colSet("a_click_3", "tab_active");
    }
    else if (eqIgno(isClick, "4")) {
      wp.colSet("a_click_4", "tab_active");
    }
    else if (eqIgno(isClick, "5")) {
      wp.colSet("a_click_5", "tab_active");
    }
    else if (eqIgno(isClick, "6")) {
      wp.colSet("a_click_6", "tab_active");
    }
    else if (eqIgno(isClick, "7")) {
      wp.colSet("a_click_7", "tab_active");
    }
    else if (eqIgno(isClick, "8")) {
      wp.colSet("a_click_8", "tab_active");
    }
    else if (eqIgno(isClick, "9")) {
      wp.colSet("a_click_9", "tab_active");
    }
    else {
      wp.colSet("a_click_1", "tab_active");
    }
  }

}
