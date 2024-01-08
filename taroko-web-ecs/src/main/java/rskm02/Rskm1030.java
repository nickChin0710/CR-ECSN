/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;

import java.util.Arrays;

import ofcapp.BaseAction;

public class Rskm1030 extends BaseAction {
  String riskGroup = "";

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
      wp.colSet("IND_NUM1", "0");
      wp.colSet("IND_NUM2", "0");
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
      strAction = "U";
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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      // -讀取明細資料-
      strAction = "R2";
      detl2Read();
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("建檔日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_risk_group"), "risk_group")
        + sqlCol(wp.itemStr("ex_crt_user"), "crt_user", "like%");

    if (wp.itemEq("ex_apr_flag", "Y")) {
      lsWhere += " and apr_flag='Y'";
    } else if (wp.itemEq("ex_apr_flag", "N")) {
      lsWhere += " and apr_flag='N'";
    }


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "risk_group," + " uf_user_name(crt_user) as crt_user ," + " crt_date,"
        + " apr_flag," + " apr_date," + " uf_user_name(apr_user) as apr_user , "
        + " risk_group_desc , " + " rskgp_remark ";
    wp.daoTable = "rsk_trial_parm2 ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by risk_group, apr_flag  ";
    pageQuery();


    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    // queryAfter();
    wp.setListCount(1);
    wp.setPageValue();

  }

  /*
   * void queryAfter(){ String sql1 = " select " + " usr_cname " + " from sec_user " +
   * " where usr_id = ? " ; for(int ii=0;ii<wp.selectCnt;ii++){ sqlSelect(sql1,new
   * Object[]{wp.col_ss(ii,"crt_user")}); if(sql_nrow>0) wp.col_set(ii,"crt_user",
   * sql_ss("usr_cname")); sqlSelect(sql1,new Object[]{wp.col_ss(ii,"apr_user")}); if(sql_nrow>0)
   * wp.col_set(ii,"apr_user", sql_ss("usr_cname")); } }
   */
  @Override
  public void querySelect() throws Exception {
    riskGroup = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(riskGroup)) {
      riskGroup = itemkk("risk_group");
    }
    wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " risk_group," + " apr_flag,"
        + " risk_group_desc," + " rskgp_remark," + " no_assure_cond," + " no_assure_amt_s,"
        + " no_assure_amt_e," + " dbr_cond," + " dbr_s," + " dbr_e," + " k34_estimate_rcbal_cond,"
        + " k34_estimate_rcbal_s," + " k34_estimate_rcbal_e," + " k34_use_rc_rate_cond,"
        + " k34_use_rc_rate_s," + " k34_use_rc_rate_e," + " k34_overdue_cond,"
        + " k34_overdue_flag," + " k34_overdue_banks_cond," + " k34_overdue_banks_s,"
        + " k34_overdue_banks_e," + " k34_overdue_6mm_cond," + " k34_overdue_6mm_s,"
        + " k34_overdue_6mm_e," + " k34_overdue_12mm_cond," + " k34_overdue_12mm_s,"
        + " k34_overdue_12mm_e," + " k34_use_cash_cond," + " k34_use_cash_flag,"
        + " k34_use_cash_6mm_cond," + " k34_use_cash_6mm_s," + " k34_use_cash_6mm_e,"
        + " k34_use_cash_12mm_cond," + " k34_use_cash_12mm_s," + " k34_use_cash_12mm_e,"
        + " k34_debt_code_cond," + " k34_debt_code," + " b63_no_overdue_amt_cond,"
        + " b63_no_overdue_amt_s," + " b63_no_overdue_amt_e," + " b63_overdue_cond,"
        + " b63_overdue_flag," + " b63_overdue_nopay_cond," + " b63_overdue_nopay_s,"
        + " b63_overdue_nopay_e," + " b63_cash_due_amt_cond," + " b63_cash_due_amt_s,"
        + " b63_cash_due_amt_e," + " jcic028_cond," + " jcic028_s," + " jcic028_e,"
        + " jcic029_cond," + " jcic029_s," + " jcic029_e," + " no_assure_add_cond,"
        + " no_assure_add_amt," + " no_assure_add_amt2," + " jcic036_cond," + " jcic036,"
        + " jcic030_cond," + " jcic030," + " jcic031_cond," + " jcic031," + " jcic023_03_cond,"
        + " jcic023_03," + " jcic023_03_e," + " jcic025_01_cond," + " jcic025_01,"
        + " jcic025_01_e," + " jcic030_01_cond," + " jcic030_02_cond," + " jcic031_01_cond,"
        + " jcic031_02_cond," + " jcic034_cond," + " jcic034," + " jcic032_cond," + " jcic032,"
        + " jcic004_01_cond," + " jcic004_01," + " jcic004_01_e," + " jcic009_cond," + " jcic009,"
        + " jcic009_e," + " jcic010_02_cond," + " jcic010_02," + " jcic010_02_e," + " jcic013_cond,"
        + " jcic013," + " jcic023_01_cond," + " jcic023_01," + " jcic023_01_e,"
        + " jcic023_02_cond ," + " jcic023_02," + " jcic023_02_e," + " credit_limit_cond,"
        + " credit_limit_s_date," + " credit_limit_e_date," + " rc_avguse_cond," + " rc_avguse_mm,"
        + " rc_avguse_rate," + " cash_use_cond," + " cash_use_mm," + " cash_use_times,"
        + " limit_avguse_cond," + " limit_avguse_mm," + " limit_avguse_rate,"
        + " payment_rate_cond," + " payment_rate_mm," + " payment_rate_times," + " no_debt_cond,"
        + " no_debt_mm," + " payment_int_cond," + " acct_jrnl_bal_cond," + " acct_jrnl_bal_s,"
        + " acct_jrnl_bal_e," + " trial_score_cond," + " trial_score_s," + " trial_score_e,"
        + " block_reason_cond," + " crt_user," + " crt_date," + " mod_user," + " apr_user,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " apr_date," + " acno_block_reason,"
        // + " db_old,"
        + " jcic031_02," + " jcic030_01," + " jcic031_01," + " jcic030_02," + " credit_limit_code,"
        + " acno_block_reason as ex_acno_block_reason";
    wp.daoTable = "rsk_trial_parm2";
    wp.whereStr = " where 1=1 and apr_flag='N'" + sqlCol(riskGroup, "risk_group");
    wp.whereOrder = " order by risk_group , apr_flag ";
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " risk_group," + " apr_flag,"
          + " risk_group_desc," + " rskgp_remark," + " no_assure_cond," + " no_assure_amt_s,"
          + " no_assure_amt_e," + " dbr_cond," + " dbr_s," + " dbr_e," + " k34_estimate_rcbal_cond,"
          + " k34_estimate_rcbal_s," + " k34_estimate_rcbal_e," + " k34_use_rc_rate_cond,"
          + " k34_use_rc_rate_s," + " k34_use_rc_rate_e," + " k34_overdue_cond,"
          + " k34_overdue_flag," + " k34_overdue_banks_cond," + " k34_overdue_banks_s,"
          + " k34_overdue_banks_e," + " k34_overdue_6mm_cond," + " k34_overdue_6mm_s,"
          + " k34_overdue_6mm_e," + " k34_overdue_12mm_cond," + " k34_overdue_12mm_s,"
          + " k34_overdue_12mm_e," + " k34_use_cash_cond," + " k34_use_cash_flag,"
          + " k34_use_cash_6mm_cond," + " k34_use_cash_6mm_s," + " k34_use_cash_6mm_e,"
          + " k34_use_cash_12mm_cond," + " k34_use_cash_12mm_s," + " k34_use_cash_12mm_e,"
          + " k34_debt_code_cond," + " k34_debt_code," + " b63_no_overdue_amt_cond,"
          + " b63_no_overdue_amt_s," + " b63_no_overdue_amt_e," + " b63_overdue_cond,"
          + " b63_overdue_flag," + " b63_overdue_nopay_cond," + " b63_overdue_nopay_s,"
          + " b63_overdue_nopay_e," + " b63_cash_due_amt_cond," + " b63_cash_due_amt_s,"
          + " b63_cash_due_amt_e," + " jcic028_cond," + " jcic028_s," + " jcic028_e,"
          + " jcic029_cond," + " jcic029_s," + " jcic029_e," + " no_assure_add_cond,"
          + " no_assure_add_amt," + " no_assure_add_amt2," + " jcic036_cond," + " jcic036,"
          + " jcic030_cond," + " jcic030," + " jcic031_cond," + " jcic031," + " jcic023_03_cond,"
          + " jcic023_03," + " jcic023_03_e," + " jcic025_01_cond," + " jcic025_01,"
          + " jcic025_01_e," + " jcic030_01_cond," + " jcic030_02_cond," + " jcic031_01_cond,"
          + " jcic031_02_cond," + " jcic034_cond," + " jcic034," + " jcic032_cond," + " jcic032,"
          + " jcic004_01_cond," + " jcic004_01," + " jcic004_01_e," + " jcic009_cond," + " jcic009,"
          + " jcic009_e," + " jcic010_02_cond," + " jcic010_02," + " jcic010_02_e,"
          + " jcic013_cond," + " jcic013," + " jcic023_01_cond," + " jcic023_01," + " jcic023_01_e,"
          + " jcic023_02_cond ," + " jcic023_02," + " jcic023_02_e," + " credit_limit_cond,"
          + " credit_limit_s_date," + " credit_limit_e_date," + " rc_avguse_cond,"
          + " rc_avguse_mm," + " rc_avguse_rate," + " cash_use_cond," + " cash_use_mm,"
          + " cash_use_times," + " limit_avguse_cond," + " limit_avguse_mm," + " limit_avguse_rate,"
          + " payment_rate_cond," + " payment_rate_mm," + " payment_rate_times," + " no_debt_cond,"
          + " no_debt_mm," + " payment_int_cond," + " acct_jrnl_bal_cond," + " acct_jrnl_bal_s,"
          + " acct_jrnl_bal_e," + " trial_score_cond," + " trial_score_s," + " trial_score_e,"
          + " block_reason_cond," + " crt_user," + " crt_date," + " mod_user," + " apr_user,"
          + " to_char(mod_time,'yyyymmdd') as mod_date ," + " apr_date," + " acno_block_reason,"
          // + " db_old,"
          + " jcic031_02," + " jcic030_01," + " jcic031_01," + " jcic030_02,"
          + " credit_limit_code," + " acno_block_reason as ex_acno_block_reason";
      wp.daoTable = "rsk_trial_parm2";
      wp.whereStr = " where 1=1 and apr_flag='Y'" + sqlCol(riskGroup, "risk_group");
      this.logSql();
      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + riskGroup);
        return;
      }
    }

    setBlockReason();
    setAdj();
  }

  void setBlockReason() {
    if (wp.colEmpty("acno_block_reason")) {
      wp.colSet("ind_num1", 0);
      return;
    }

    String[] ss = new String[2];
    ss[0] = wp.colStr("acno_block_reason");
    int ii = 0, nn = 0;
    while (true) {
      if (empty(ss[0]) || ss[0].length() == 0)
        break;
      nn++;
      ss = commString.token(ss, ",");
      wp.colSet(ii, "block_code", ss[1]);
      if (nn <= 9) {
        wp.colSet(ii, "ser_num", "0" + nn);
      } else {
        wp.colSet(ii, "ser_num", nn);
      }
      ii++;

    }

    wp.listCount[0] = nn;
    wp.colSet("ind_num1", "" + nn);

  }

  void setAdj() {
    if (wp.colEmpty("credit_limit_code")) {
      wp.colSet("ind_num2", 0);
      return;
    }

    String[] ss = new String[2];
    ss[0] = wp.colStr("credit_limit_code");
    int ii = 0, nn = 0;
    while (true) {
      if (empty(ss[0]) || ss[0].length() == 0)
        break;
      nn++;
      ss = commString.token(ss, ",");
      wp.colSet(ii, "adj_code", ss[1]);
      if (nn <= 9) {
        wp.colSet(ii, "ser_num", "0" + nn);
      } else {
        wp.colSet(ii, "ser_num", nn);
      }
      ii++;

    }

    wp.listCount[1] = nn;
    wp.colSet("ind_num2", "" + nn);

    // int ss = wp.col_ss("credit_limit_code").length();
    // int tt=0;
    // for (int ii=0 ; ii<ss ; ii++){
    // String rr="";
    // rr=wp.col_ss("credit_limit_code").substring(tt, tt+1);
    // wp.col_set(ii,"adj_code", rr);
    // tt = tt+2;
    // }
    // wp.listCount[1] = ss;
    // wp.col_set("ind_num2", ""+ss);
  }

  @Override
  public void saveFunc() throws Exception {
    if (isDelete() && wp.itemEq("apr_flag", "Y")) {
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }
    }

    // move_block2Data();

    rskm02.Rskm1030Func func = new rskm02.Rskm1030Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    if (rc == 1) {
      this.saveAfter(true);
    }

  }



  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
    }

  }


  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskm1030")) {
        wp.optionKey = wp.colStr(0, "ex_crt_user");
        dddwList("dddw_crt_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  void detl2Read() throws Exception {
    riskGroup = wp.itemStr("data_k1");
    if (eqIgno(riskGroup, "02")) {
      int blockReasonLength = wp.colStr("acno_block_reason").length() / 2;
      wp.log("ss=" + blockReasonLength);
      int cnt = 0;
      for (int ii = 0; ii < blockReasonLength; ii++) {
        String rr = "";
        int num = 0;
        num = ii + 1;
        rr = wp.colStr("acno_block_reason").substring(cnt, cnt + 2);
        wp.log("rr=" + rr);
        wp.colSet(ii, "data_code", rr);
        cnt = cnt + 2;
        wp.colSet(ii, "ser_num", "" + num);
      }
      wp.listCount[0] = blockReasonLength;
      wp.colSet("ind_num", "" + blockReasonLength);
    } else if (eqIgno(riskGroup, "01")) {
      int limitCodeLength = wp.colStr("credit_limit_code").length();
      int cnt = 0;
      for (int ii = 0; ii < limitCodeLength; ii++) {
        String rr = "";
        rr = wp.colStr("credit_limit_code").substring(cnt, cnt + 1);
        wp.colSet(ii, "data_code", rr);
        cnt = cnt + 1;
      }
      wp.listCount[0] = limitCodeLength;
      wp.colSet("ind_num", "" + limitCodeLength);

    }
  }



}
