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
/**
 * 2019-1002   JH    modify
 * */
import ofcapp.BaseAction;

public class Rskp2050 extends BaseAction {
  String riskGroup = "";

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

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("建檔日期起迄：輸入錯誤");
      return;
    }


    String lsWhere =
        " where 1=1 and apr_flag<>'Y' " + sqlCol(wp.itemStr("ex_risk_group"), "risk_group")
            + sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
            + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
            + sqlCol(wp.itemStr("ex_crt_user"), "crt_user", "like%");
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " risk_group ," + " risk_group_desc ," + " crt_user ," + " crt_date";
    wp.daoTable = "rsk_trial_parm2";
    wp.whereOrder = " order by risk_group";

    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    riskGroup = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
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
        + " block_reason_cond," + " crt_user," + " crt_date," + " apr_user," + " apr_user,"
        + " apr_date," + " apr_date," + " acno_block_reason,"
        // + " db_old,"
        + " jcic031_02," + " jcic030_01," + " jcic031_01," + " jcic030_02," + " credit_limit_code,"
        + " acno_block_reason as ex_acno_block_reason";
    wp.daoTable = "rsk_trial_parm2";
    wp.whereStr = " where 1=1 and apr_flag='N'" + sqlCol(riskGroup, "risk_group");
    wp.whereOrder = " order by risk_group , apr_flag ";
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key1=" + riskGroup);
      return;

    }

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilCnt = 0;
    rskm02.Rskp2050Func func = new rskm02.Rskp2050Func();
    func.setConn(wp);

    String[] lsRiskGroup = wp.itemBuff("risk_group");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsRiskGroup.length;
    if (optToIndex(opt[0]) < 0) {
      alertErr2("請勾選覆核資料");
      return;
    }

    for (int ii = 0; ii < opt.length; ii++) {
      int rr = optToIndex(opt[ii]);
      if (rr < 0) {
        continue;
      }
      ilCnt++;
      optOkflag(rr);

      func.varsSet("risk_group", lsRiskGroup[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      optOkflag(rr, rc);
      if (rc == 1)
        ilOk++;
      else
        ilErr++;
    }


    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
