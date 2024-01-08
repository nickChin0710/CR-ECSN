/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-25   V1.00.02 Justin        zz -> comm
******************************************************************************/
package busi.func;
/** 帳戶信用額度調整公用[uc_credit_limit]
 * 2019-1230   JH    busi.func >>ecsfunc
 * 2019-0925   JH    永調遇臨調.adj_inst_pct
 * 2019-0821   JH    aa_date
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
   2018-1214:  JH    永調遝臨調
 * 2018-0918:	JH		臨調取消
 * 2018-0203:	JH		initial
 * 110-01-07  V1.00.08  tanwei        修改意義不明確變量                                                                          *
 * */

import busi.FuncBase;

public class AcnoCreditLimit extends FuncBase {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  public String acnoPseqno = "", adjReason = "";
  public double imAmtBefore = 0, imAmtAfter = 0;
  public double imCashBefore = 0, imCashAfter = 0;
  public int iiCombo = 0;

  busi.DataSet idsAcno = new busi.DataSet();
  public int ilCombo = 0;

  private String adjEndDate = "";
  private String logType = "";
  private double adjInstPct = 0;

  public int trialAction() {
    msgOK();
    // --期中覆審: 調降額度
    strSql =
        "select count(*) as ll_cnt" + " from rsk_acnolog"
            + " where kind_flag ='A' and log_type ='1'" + " and acno_p_seqno =? and apr_flag=''";
    setString2(1, acnoPseqno);
    sqlSelect(strSql);
    if (sqlRowNum > 0 && colInt("ll_cnt") > 0) {
      errmsg("帳戶額度 有調整未覆核");
      return -1;
    }

    selectActAcno();
    if (rc != 1)
      return -1;

    if (imAmtBefore != idsAcno.colNum("line_of_credit_amt")) {
      errmsg("帳戶額度 與ACTION時額度不同");
      return -1;
    }

    String lsSysdate = commDate.sysDate();
    String lsComboIndr = idsAcno.colStr("combo_indr");
    double ldcCashBef = idsAcno.colNum("line_of_credit_amt_cash");
    double ldcComboBef = idsAcno.colNum("combo_cash_limit");
    double ldcAfterAmt = imAmtAfter;
    double lmCashRate1 = idsAcno.colNum("cashadv_loc_rate");
    double lmCashRate2 = idsAcno.colNum("cashadv_loc_rate_old");
    double ldcCashAmt = ldcAfterAmt * lmCashRate2 / 100;
    if (colEq("acno.new_acct_flag", "Y")) {
      ldcCashAmt = ldcAfterAmt * lmCashRate1 / 100;
    }
    ldcCashAmt = commString.numScale(ldcCashAmt, 0);
    double ldcMaxAmt = idsAcno.colNum("cashadv_loc_maxamt");
    if (ldcCashAmt > ldcMaxAmt)
      ldcCashAmt = ldcMaxAmt;
    // -額度-
    idsAcno.colSet("adj_before_loc_amt", imAmtBefore);
    idsAcno.colSet("line_of_credit_amt", ldcAfterAmt);
    idsAcno.colSet("h_adj_loc_low_date", lsSysdate);
    idsAcno.colSet("adj_loc_low_t", adjReason);
    // -???-預借現金--
    // ls_no_adj_high_cash =ids_acno.of_getitem(1,"no_adj_loc_high_cash")
    // ls_cash_date[1] =ids_acno.of_getitem(1,"no_adj_loc_high_s_date_cash")
    // ls_cash_date[2] =ids_acno.of_getitem(1,"no_adj_loc_high_e_date_cash")
    // if Len(ls_cash_date[2])=0 then ls_cash_date[2] ='99991231'
    //
    boolean lbCashAdj = true;
    // if ldc_cash_bef<=0 then lb_cash_adj=false
    // if ldc_cash_bef=ldc_cash_amt then lb_cash_adj=false
    double lmCashLimitRate = lmCashRate1;
    if (ldcCashAmt > ldcCashBef) {
      lmCashLimitRate = commString.numScale(idsAcno.colNum("db_cash_limit_rate") * 100, 0);
    }
    if (lmCashLimitRate != lmCashRate1 && lmCashLimitRate != lmCashRate2)
      lbCashAdj = false;

    if (lbCashAdj) {
      if (ldcCashBef >= ldcCashAmt) { // -調低-
        idsAcno.colSet("line_of_credit_amt_cash", ldcCashAmt);
        imCashBefore = ldcCashBef;
        imCashAfter = ldcCashAmt;
      }
    }

    // -Combo-card-
    ldcCashAmt = idsAcno.colNum("line_of_credit_amt_cash");
    boolean lbCombo = false;
    if (eqAny(lsComboIndr, "Y") && ldcComboBef > ldcCashAmt) {
      strSql =
          "select card_no, combo_acct_no," + " uf_idno_id(id_p_seqno) as db_idno"
              + " from crd_card" + " where acno_p_seqno =? and current_code='0' and sup_flag='0'"
              + " and oppost_date =''" + commSqlStr.rownum(1);
      setString2(1, acnoPseqno);
      daoTid = "card.";
      sqlSelect(strSql);
      if (sqlRowNum > 0)
        lbCombo = true;
    }
    updateActAcno();
    if (rc != 1)
      return rc;

    // -insert voc_appc_temp-
    if (lbCombo) {
      ilCombo++;
      double ldcTaxAmt = ldcComboBef - ldcCashAmt;
      this.colSet("ldc_tx_amt", ldcTaxAmt);
      colSet("ldc_cash_amt", ldcCashAmt);
      insertVocAppcTemp();
    }
    if (rc == 1) {
      insertRskAcnolog();
    }
    if (rc == 1) {
      insertOnbat2Ccas();
    }
    // -臨調取消-
    if (rc == 1) {
      updateCcasTotAmtMonth();
    }


    return rc;
  }

  void insertVocAppcTemp() {
    strSql =
        "insert into voc_appc_temp (" + " assign_kind, tx_date, tx_seq, card_no"
            + ", acct_id, acct_no" + ", effc_month, tx_amt, combo_amt"
            + ", mod_time, mod_pgm, mod_user )" + " values (" + "'2', " + commSqlStr.sysYYmd
            + ", 0, :ls_card_no" + ", :ls_id, :ls_combo_acct_no" + "," + commSqlStr.sysYYmd
            + ",:ldc_tax_amt,:ldc_cash_amt" + ", sysdate, :mod_pgm, :mod_user)";
    setString2("ls_card_no", colStr("card.card_no"));
    setString2("ls_id", colStr("card.db_idno"));
    setString2("ls_combo_acct_no", colStr("card.combo_acct_no"));
    setDouble2("ldc_tax_amt", colNum("ldc_tax_amt"));
    setDouble2("ldc_cash_amt", colNum("ldc_cash_amt"));
    setString2("mod_user", modUser);
    setString2("mod_pgm", modPgm);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert voc_appc_temp error; kk[%s]", colStr("card.card_no"));
      return;
    }
  }

  void updateActAcno() {
    strSql =
        "UPDATE act_acno SET" + ", h_adj_loc_high_date =:h_adj_loc_high_date"
            + ", h_adj_loc_low_date =:h_adj_loc_low_date" + ", adj_loc_high_t =:adj_loc_high_t"
            + ", adj_loc_low_t =:adj_loc_low_t" + ", line_of_credit_amt =:line_of_credit_amt"
            + ", adj_before_loc_amt =:adj_before_loc_amt" + ", combo_cash_limit =:combo_cash_limit"
            + ", line_of_credit_amt_cash =:line_of_credit_amt_cash" + ", mod_user =:mod_user"
            + ", mod_time =" + commSqlStr.sysdate + ", mod_pgm =:mod_pgm"
            + ", mod_seqno =nvl(mod_seqno,0)+1" + " where acno_p_seqno =:kk_p_seqno";
    setString2("h_adj_loc_high_date", idsAcno.colStr("h_adj_loc_high_date"));
    setString2("h_adj_loc_low_date", idsAcno.colStr("h_adj_loc_low_date"));
    setString2("adj_loc_high_t", idsAcno.colStr("adj_loc_high_t"));
    setDouble2("line_of_credit_amt", idsAcno.colNum("line_of_credit_amt"));
    setDouble2("adj_before_loc_amt", idsAcno.colNum("adj_before_loc_amt"));
    setDouble2("combo_cash_limit", idsAcno.colNum("combo_cash_limit"));
    setDouble2("line_of_credit_amt_cash", idsAcno.colNum("line_of_credit_amt_cash"));
    setString2("mod_user", modUser);
    setString2("mod_pgm", modPgm);
    setString2("kk_p_seqno", acnoPseqno);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ACT_ACNO error; kk[%s]", acnoPseqno);
      return;
    }
  }

  void selectActAcno() {
    strSql =
        "SELECT A.acct_type,   "
            + "       A.acct_key,    "
            + "       A.corp_p_seqno,"
            + "       A.acct_status, "
            + "       A.id_p_seqno, A.corp_p_seqno, "
            + "       A.no_adj_loc_high, "
            + "       A.no_adj_loc_high_s_date, "
            + "       A.no_adj_loc_high_e_date, "
            + "       A.no_adj_loc_low,         "
            + "       A.no_adj_loc_low_s_date,  "
            + "       A.no_adj_loc_low_e_date,  "
            + "       A.h_adj_loc_high_date,    "
            + "       A.h_adj_loc_low_date,     "
            + "       A.adj_loc_high_t,         "
            + "       A.adj_loc_low_t,          "
            + "       A.line_of_credit_amt,     "
            + "       A.adj_before_loc_amt,     "
            + "       A.card_indicator as card_indr,"
            + "       A.acno_flag, "
            + "       A.combo_indicator as combo_indr, "
            + "       A.combo_cash_limit, "
            // +"       --A.no_adj_loc_high_cash, "
            // +"       --A.no_adj_loc_high_s_date_cash,"
            // +"       --A.no_adj_loc_high_e_date_cash,"
            + "       A.line_of_credit_amt_cash,      "
            + "       A.new_acct_flag,"
            + "       decode(line_of_credit_amt_cash,0,0, line_of_credit_amt_cash / A.line_of_credit_amt) as db_cash_limit_rate,"
            + "       A.vip_code," + "       A.month_purchase_lmt,"
            + "       B.cashadv_loc_rate,  " + "       B.cashadv_loc_maxamt,"
            + "       B.cashadv_loc_rate_old," + "       B.breach_num_month," + "       '' as xxx "
            + "  FROM act_acno A join ptr_acct_type B " + "          on A.acct_type =B.acct_type"
            + " WHERE A.acno_p_seqno = ?";
    setString2(1, acnoPseqno);
    idsAcno.colList = sqlQuery(strSql);
    if (sqlRowNum <= 0) {
      errmsg("select act_acno error, kk[%s]", acnoPseqno);
      return;
    }
    idsAcno.listNext();
  }

  void insertRskAcnolog() {
    // --調額
    String lsFhFlag = "";

    strSql = "select fh_flag from crd_correlate" + " where correlate_id =?" + commSqlStr.rownum(1);
    setString2(1, colStr("acno.id_no"));
    if (sqlRowNum > 0) {
      lsFhFlag = colStr("fh_flag");
    }

    strSql =
        "insert into rsk_acnolog (" + " kind_flag," + " acno_p_seqno," + " acct_type,"
            + " id_p_seqno," + " copr_p_seqno," + " log_date," + " log_mode, log_type,"
            + " log_reason," + " bef_loc_amt," + " aft_loc_amt," + " adj_loc_flag," + " fit_cond,"
            + " security_amt," + " apr_flag, apr_user, apr_date," + " emend_type," + " fh_flag,"
            + " bef_loc_cash," + " aft_loc_cash," + " mod_user, mod_time, mod_pgm, mod_seqno"
            + " ) values (" + " 'A'," + " :kk_p_seqno," + " :acct_type," + " :id_p_seqno,"
            + " :corp_p_seqno," + commSqlStr.sysYYmd + "," + " '1','1'," + " :log_reason,"
            + " :bef_loc_amt," + " :aft_loc_amt,"
            + " '2'," // adj_loc_flag,"
            + " 'ECS'," // fit_cond,"
            + " 0," // security_amt,"
            + " 'Y', :apr_user," + commSqlStr.sysYYmd + ","
            + " '1'," // emend_type,"
            + " :fh_flag," + " :bef_loc_cash," + " :aft_loc_cash," + " :mod_user," + commSqlStr.sysdate
            + ", :mod_pgm, 1" + " )";
    setString2("kk_p_seqno", acnoPseqno);
    setString2("acct_type", colStr("acno.acct_type"));
    setString2("id_p_seqno", colStr("acno.id_p_seqno"));
    setString2("corp_p_seqno", colStr("acno.corp_p_seqno"));
    setString2("log_reason", adjReason);
    setDouble2("bef_loc_amt", imAmtBefore);
    setDouble2("aft_loc_amt", imAmtAfter);
    setString2("apr_user", modUser);
    setString2("fh_flag", lsFhFlag);
    setDouble2("bef_loc_cash", imCashBefore);
    setDouble2("aft_loc_cash", imCashAfter);
    setString2("mod_user", modUser);
    setString2("mod_pgm", modPgm);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_acnolog.Limit error, kk[%s]", acnoPseqno);
      return;
    }
  }

  void insertOnbat2Ccas() {
    //
    double lmMmPurchLimit = idsAcno.colNum("month_purchase_lmt");
    String lsVipCode = idsAcno.colStr("vip_code");

    // decode(:ls_card_indr,'1',null,'1'),
    String lsPayType = idsAcno.colStr("card_indr");
    if (idsAcno.colEq("card_indr", "1"))
      lsPayType = "";

    strSql =
        "insert into onbat_2ccas (" + " trans_type," + " to_which," + " dog," + " proc_mode,"
            + " proc_status," + " card_catalog," + " payment_type," + " acct_type,"
            + " acno_p_seqno," + " card_no," + " block_code_1," + " trans_amt," + " credit_limit,"
            + " credit_limit_cash" + " ) values (" + " '12'," + " '2'," + " sysdate," + " 'B',"
            + " 0," + " :ls_card_indr,"
            + " :ls_pay_type," // decode(:ls_card_indr,'1',null,'1'),
            + " :ls_acct_type," + " :kk_p_seqno,"
            + " ''," // card_no
            + " :ls_vip_code," + " :lm_mm_purch_limit," + " :im_amt_after," + " :im_cash_after"
            + " )";
    setString2("ls_card_indr", idsAcno.colStr("card_indr"));
    setString2("ls_pay_type", lsPayType);
    setString2("ls_acct_type", idsAcno.colStr("acct_type"));
    setString2("kk_p_seqno", acnoPseqno);
    setString2("ls_vip_code", lsVipCode);
    setDouble2("lm_mm_purch_limit", lmMmPurchLimit);
    setDouble2("im_amt_after", imAmtAfter);
    setDouble2("im_cash_after", imCashAfter);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ONBAT error, p_seqno=", acnoPseqno);
      return;
    }
  }

  // -臨調額度-
  public int updateCcasTotAmtMonth() {
    if (imAmtAfter == imAmtBefore)
      return 1;

    int liRc = selectCcaCardAcct();
    if (liRc != 1)
      return 1;

    logType = "";
    double lmTotAmt = colNum("ccas.tot_amt_month");
    adjInstPct = colNum("ccas.adj_inst_pct"); // ADJ_INST_PCT

    // -調高:永調<=臨調-
    if (imAmtAfter > imAmtBefore) {
      if (imAmtAfter > lmTotAmt)
        logType = "1"; // 終止臨調
      else {
        logType = "2"; // 臨調續用
        if (imAmtAfter > adjInstPct) {
          adjInstPct = imAmtAfter;
          logType = "5"; // 調高分期額度
        }
      }
    }
    // -調降:永調>=臨調-
    if (imAmtAfter < imAmtBefore) {
      if (imAmtAfter < lmTotAmt)
        logType = "4"; // 終止臨調
      else
        logType = "3"; // 臨調續用
    }

    // -終止臨調-
    this.dateTime();
    if (commString.strIn2(logType, ",1,4")) {
      adjEndDate = commDate.dateAdd(this.sysDate, 0, 0, -1);
    } else
      adjEndDate = colStr("ccas.adj_eff_end_date");

    updateCcaCardAcct();
    if (rc == 1 && commString.strIn2(logType, ",1,4,5")) {
      insertCcaLimitAdjLog();
    }
    if (rc == 1) {
      insertCcaCreditLog();
    }

    return rc;
  }

  void updateCcaCardAcct() {
    strSql =
        "update cca_card_acct set" + " adj_eff_end_date =?" + ", adj_inst_pct =?" + ","
            + commSqlStr.setModxxx(modUser, modPgm) + " where card_acct_idx =?";

    setString2(1, adjEndDate);
    setDouble(adjInstPct);
    setDouble(colNum("ccas.card_acct_idx"));
    sqlExec(strSql);
    if (sqlRowNum != 1) {
      this.sqlErr("update cca_card_acct error, kk[" + colNum("card_acct_idx") + "]");
    }
  }

  void insertCcaLimitAdjLog() {
    busi.SqlPrepare tt = new busi.SqlPrepare();
    tt.sql2Insert("cca_limit_adj_log");
    tt.addsqlYmd(" log_date");
    tt.addsqlTime(", log_time");
    tt.addsqlParm(", aud_code", ",'U'");
    tt.addsqlParm(",?", ", card_acct_idx", colNum("ccas.card_acct_idx"));
    tt.addsqlParm(", debit_flag", ", 'N'");
    tt.addsqlParm(", mod_type", ", '0'");
    // tt.aaa(", rela_flag", ", ")
    tt.addsqlParm(",?", ", lmt_tot_consume", imAmtBefore); // 原額度
    tt.addsqlParm(",?", ", tot_amt_month_b", colNum("ccas.tot_amt_month"));
    tt.addsqlParm(",?", ", tot_amt_month", colNum("ccas.tot_amt_month")); // 一般調整後額度
    tt.addsqlParm(",?", ", adj_inst_pct_b", colNum("ccas.adj_inst_pct"));
    tt.addsqlParm(",?", ", adj_inst_pct", adjInstPct); // 分期調整後額度
    tt.addsqlParm(",?", ", adj_eff_date1", colStr("ccas.adj_eff_start_date"));
    tt.addsqlParm(",?", ", adj_eff_date2", adjEndDate);
    tt.addsqlParm(",?", ", adj_reason", adjReason);
    tt.addsqlParm(",?", ", adj_remark", "永久額度調整");
    tt.addsqlParm(",?", ", adj_area", colStr("ccas.adj_area"));
    tt.addsqlParm(", ecs_adj_rate", ", '0'");
    tt.addsqlParm(",?", ", adj_user", modUser);
    tt.addsqlParm(",?", ", adj_date", sysDate);
    tt.addsqlParm(",?", ", adj_time", sysTime);
    tt.addsqlParm(",?", ", apr_user", modUser);

    sqlExec(tt.sqlStmt(), tt.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("cca_limit_adj_log.ADD, err=" + sqlErrtext);
    }
    return;
  }

  void insertCcaCreditLog() {
    double lmTotAmtMonth = colNum("ccas.tot_amt_month");
    if (commString.strIn2(logType, ",1,4")) {
      lmTotAmtMonth = imAmtAfter;
    }
    busi.SqlPrepare prepare = new busi.SqlPrepare();
    prepare.sql2Insert("cca_credit_log");
    prepare.addsqlParm(" tx_date", commSqlStr.sysYYmd);
    prepare.addsqlParm(", tx_time", "," + commSqlStr.sysTime);
    prepare.addsqlParm(",?", ", card_acct_idx", colNum("ccas.card_acct_idx"));
    prepare.addsqlParm(",?", ", acct_type", colStr("ccas.acct_type"));
    prepare.addsqlParm(",?", ", org_credit_cash", imCashBefore);
    prepare.addsqlParm(",?", ", line_credit_cash", imCashAfter);
    prepare.addsqlParm(",?", ", org_credit_amt", imAmtBefore);
    prepare.addsqlParm(",?", ", line_credit_amt", imAmtAfter);
    prepare.addsqlParm(",?", ", adj_quota", colStr("ccas.adj_quota"));
    prepare.addsqlParm(",?", ", adj_eff_start_date", colStr("ccas.adj_eff_start_date"));
    prepare.addsqlParm(",?", ", adj_eff_end_date", adjEndDate);
    prepare.addsqlParm(",?", ", adj_reason", colStr("ccas.adj_reason"));
    prepare.addsqlParm(",?", ", org_amt_month", colNum("ccas.tot_amt_month"));
    prepare.addsqlParm(",?", ", tot_amt_month", lmTotAmtMonth);
    prepare.addsqlParm(",?", ", org_inst_pct", colNum("ccas.adj_inst_pct"));
    prepare.addsqlParm(",?", ", adj_inst_pct", adjInstPct);
    prepare.addsqlParm(",?", ", adj_user", modUser);
    prepare.addsqlParm(",?", ", log_type", logType);
    prepare.addsqlParm(",?", ", mod_pgm", modPgm);
    prepare.addsqlDate(", mod_time");

    sqlExec(prepare.sqlStmt(), prepare.sqlParm());
    if (sqlRowNum <= 0) {
      sqlErr("cca_limit_adj_log.ADD");
    }
    return;
  }

  int selectCcaCardAcct() {
    adjEndDate = "";

    strSql =
        "select card_acct_idx, acno_p_seqno, id_p_seqno, tot_amt_month"
            + ", adj_inst_pct, adj_eff_start_date, adj_eff_end_date, adj_area , acct_type , "
            + "adj_quota , adj_reason , adj_remark " + " from cca_card_acct"
            + " where acno_p_seqno =? and debit_flag<>'Y'" + " and adj_eff_start_date <="
            + commSqlStr.sysYYmd + " and adj_eff_end_date >=" + commSqlStr.sysYYmd;
    setString2(1, acnoPseqno);

    daoTid = "ccas.";
    sqlSelect(strSql);
    if (sqlRowNum < 0) {
      wp.log("select cca_card_acct error, kk[%s]", acnoPseqno);
      return 0;
    }

    // -無臨調-
    if (sqlRowNum == 0)
      return 0;

    // -有臨調-
    adjEndDate = colStr("ccas.adj_eff_end_date");
    return 1;
  }
}
