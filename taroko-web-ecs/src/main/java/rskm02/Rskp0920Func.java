/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27   V1.00.00  Tanwei       updated for project coding standard     *
* 109-12-23   V1.00.01  Justin         parameterize sql                      *
* 110-01-06   V1.00.02  tanwei        修改zz開頭的變量                                                                           *
******************************************************************************/
package rskm02;
/** 線上持卡人信用額度調整主管覆核
 * 2019-1230   JH    credit_limit: busi.func >>ecsfunc
 * 2019-0925   JH    永調遇臨調
 * 2019-0619:  JH    p_xxx >>acno_pxxx
 *  2018-1212:  JH    modify
 * 2018-0129:	JH		主管覆核權限
 *
 * */

import busi.FuncAction;
import busi.func.AcnoCreditLimit;

public class Rskp0920Func extends FuncAction {

  taroko.base.CommDate commDate = new taroko.base.CommDate();
  AcnoCreditLimit ooLimit = null;

  boolean selectLimit = false;
  double imAprLimitIdno = 0;
  double imAprLimitCorp = 0;
  String pSeqno = "", idpSeqno = "", corpSeqno = "", cardNo = "";
  private int ilCombo = 0;

  @SuppressWarnings("unused")
  void selectSecAmtlimit() {

    strSql = "select A.al_amt, A.al_amt02" + " from sec_amtlimit A join sec_user B"
        + "   on A.al_level =B.usr_amtlevel" + " where B.usr_id =?";
    setString2(1, modUser);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      // errmsg("使用者: 無額度覆核權限");
      return;
    }

    imAprLimitIdno = colNum("al_amt");
    imAprLimitCorp = colNum("al_amt02");
    return;
  }

  public void setPSeqno(String pSeqno, String idPSeqno, String corpPSeqno) {
    pSeqno = pSeqno;
    idpSeqno = idPSeqno;
    corpSeqno = corpPSeqno;
  }

  public int checkAprAuth(String emendType, double befAmt, double aftAmt) {
    if (selectLimit == false) {
      selectSecAmtlimit();
      selectLimit = true;
      wp.colSet("db_apr_limit", "" + imAprLimitIdno);
    }

    if (rc != 1)
      return rc;
    // -1.個人額度-
    if (eqAny(emendType, "1")) {
      strSql = "select sum(line_of_credit_amt) as idno_limit" + " from act_acno" 
          + " where 1=1"
          + " and acno_flag in ('1','3')";
      if (!empty(idpSeqno)) {
		strSql += " and id_p_seqno = ? ";
		setString(idpSeqno);
	  }
      sqlSelect(strSql);
      if (sqlRowNum <= 0)
        return -1;

      double lmAmt = colNum("idno_limit") + aftAmt - befAmt;
      if (imAprLimitIdno >= lmAmt)
        return 1;
      return -1;
    }
    // -4.子卡額度-
    if (eqAny(emendType, "4")) {
      strSql = "select line_of_credit_amt as idno_limit" + " from act_acno where acno_p_seqno =?";
      setString2(1, pSeqno);
      sqlSelect(strSql);
      if (sqlRowNum <= 0)
        return -1;

      if (colNum("idno_limit") >= aftAmt)
        return 1;
      return -1;
    }
    // 5.預借現金額度
    if (eqAny(emendType, "5")) {
      strSql = "select sum(line_of_credit_amt_cash) as idno_cash" + " from act_acno"
          + " where id_p_seqno =?" // +commSqlStr.col(_idp_seqno,"id_p_seqno")
          + " and acno_flag in ('1','3')";
      setString2(1, idpSeqno);

      sqlSelect(strSql);
      if (sqlRowNum <= 0)
        return -1;
      double lmCash = aftAmt - befAmt + colNum("idno_cash");
      if (imAprLimitIdno >= lmCash)
        return 1;
      return -1;
    }
    // -商務卡總繳-
    if (pos("|2|3", emendType) > 0) {
      if (imAprLimitCorp >= aftAmt)
        return 1;
      return -1;
    }

    return 1;
  }

  @Override
  public void dataCheck() {
    selectSecAmtlimit();

    selectRskAcnolog();
    if (rc != 1)
      return;

    strSql = "select A.line_of_credit_amt," + " A.no_adj_h_cash," + " A.no_adj_h_s_date_cash,"
        + " A.no_adj_h_e_date_cash," + " A.line_of_credit_amt_cash," + " A.combo_cash_limit,"
        + " A.combo_indicator,"
        + " decode(A.new_acct_flag,'Y',B.cashadv_loc_rate,B.cashadv_loc_rate_old) as cash_loc_rate,"
        + " B.cashadv_loc_maxamt as cash_loc_max" + " from act_acno A join ptr_acct_type B"
        + "    on A.acct_type =B.acct_type" + " where A.acno_p_seqno =?"; // +commSqlStr.col(_p_seqno,"A.p_senqo");
    setString2(1, pSeqno);
    daoTid = "acno.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("select act_acno error; kk[%s]", pSeqno);
    }

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

  void selectRskAcnolog() {
    strSql = "select * from rsk_acnolog" + " where 1=1" + " and rowid =? and mod_seqno =?";
    setRowId2(1, varsStr("rowid"));
    setDouble(varsNum("mod_seqno"));

    daoTid = "aclg.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("資料不存在 OR 己被異動");
      return;
    }

    pSeqno = colStr("aclg.acno_p_seqno");
    idpSeqno = colStr("aclg.id_p_seqno");
    corpSeqno = colStr("aclg.corp_p_seqno");
    cardNo = colStr("aclg.card_no");
  }

  @Override
  public int dataProc() {
    int liRc = 0;

    dataCheck();
    if (rc != 1)
      return rc;

    if (colEq("aclg.emend_type", "4")) {
      updateCrdCard();
    }
    // else if (col_eq("aclg.emend_type","3")) { //-公司個人-
    // update_act_corp_gp();
    // }
    else {
      updateActAcno();
    }
    if (rc == 1) {
      updateRskAcnolog();
    }
    if (rc == 1 && colEq("aclg.sms_flag", "N") == false) {
      insertSmsMsgDtl(varsNum("wk_aft_amt"));
    }

    String lsEmend = colStr("aclg.emend_type");
    if (rc == 1 && commString.strIn2(lsEmend, ",1,2,3")) {
      // update_Cca_card_acct();
      if (ooLimit == null) {
        ooLimit = new AcnoCreditLimit();
        ooLimit.setConn(wp);
      }
      ooLimit.acnoPseqno = pSeqno;
      ooLimit.imAmtAfter = varsNum("wk_aft_amt");
      ooLimit.imAmtBefore = varsNum("wk_bef_amt");
      ooLimit.adjReason = colStr("aclg.log_reason");
      ooLimit.imCashAfter = colNum("aclg.aft_loc_cash");
      ooLimit.imCashBefore = colNum("aclg.bef_loc_cash");
      if (ooLimit.updateCcasTotAmtMonth() == -1) {
        errmsg(ooLimit.getMsg());
      }
    }

    // --2018/07/30
    return rc;
  }

  void insertSmsMsgDtl(double aAmt) {
    boolean lbUp = colEq("aclg.adj_loc_flag", "1");
    busi.func.SmsMsgDetl ooSms = new busi.func.SmsMsgDetl();
    ooSms.setConn(wp);

    ooSms.strMsgDesc = "";
    // --調升 RSKM0920-1 調降 RSKM0920-2 預借現金 RSKM0920-5 覆審 RSKM0920-6
    if (colEq("aclg.sms_flag", "1")) {
      if (lbUp) {
        if ((aAmt % 10000) == 0)
          ooSms.strMsgDesc = commString.numFormat((aAmt / 10000), "##0") + "萬";
        else
          ooSms.strMsgDesc = commString.numFormat(aAmt, "##0");
        rc = ooSms.rskP0920LimitUp(pSeqno, idpSeqno, "RSKM0920-1");
      } else {
        rc = ooSms.rskP0920LimitUp(pSeqno, idpSeqno, "RSKM0920-2");
      }
    } else if (colEq("aclg.sms_flag", "5")) {
      rc = ooSms.rskP0920LimitUp(pSeqno, idpSeqno, "RSKM0920-5");
    } else if (colEq("aclg.sms_flag", "6")) {
      if ((aAmt % 10000) == 0)
        ooSms.strMsgDesc = commString.numFormat((aAmt / 10000), "##0") + "萬";
      else
        ooSms.strMsgDesc = commString.numFormat(aAmt, "##0");
      rc = ooSms.rskP0920LimitUp(pSeqno, idpSeqno, "RSKM0920-6");
    }


  }

  void updateRskAcnolog() {
    // double
    // ldc_cash_bef =dw_data.of_getitem(L,"before_loc_amt_cash")
    // ldc_cash_aft =dw_data.of_getitem(L,"after_loc_amt_cash")
    strSql = "update rsk_acnolog set" + " apr_flag ='Y'," + " fit_cond ='N'," + " security_amt =?," // :ldc_sec_amt,
        + " APR_USER =?," // :ls_userid,
        + " APR_DATE =" + commSqlStr.sysYYmd + "," + " bef_loc_cash =?," + " aft_loc_cash =?,"
        + " mod_pgm =?," + commSqlStr.modSeqnoSet + " where rowid =?";
    setDouble2(1, imAprLimitIdno);
    setString(modUser); // apr_user
    setDouble(wp.itemNum("wk_bef_loc_cash"));
    setDouble(wp.itemNum("wk_aft_loc_cash"));
    setString(modPgm);
    setRowId(varsStr("rowid"));

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update rsk_acnolog error; kk[%s]", pSeqno);
    }
  }

  void updateActAcno() {
    busi.SqlPrepare idsAcno = new busi.SqlPrepare();
    idsAcno.sql2Update("act_acno");

    String lsComboInd = colStr("acno.combo_indicator");
    double lmCashBef = colNum("acno.line_of_credit_amt_cash");
    double lmComboBef = colNum("acno.combo_cash_limit");
    double lmCashAft = 0;
    double lmAftAmt = 0;

    // -預借現金調整-
    if (colEq("aclg.emend_type", "5")) {
      lmCashAft = colNum("aclg.aft_loc_cash");
      idsAcno.ppdouble("line_of_credit_amt_cash", colNum("aclg.aft_loc_cash"));
      wp.itemSet("wk_bef_loc_cash", "" + lmCashBef);
      wp.itemSet("wk_aft_loc_cash", "" + lmCashAft);
    }
    // -額度調整--------------------------------------------------------------------------------
    else {
      lmAftAmt = colNum("aclg.aft_loc_amt");
      lmCashAft = lmAftAmt * colNum("acno.cash_loc_rate") / 100;
      if (lmCashAft > colNum("acno.cash_loc_max")) {
        lmCashAft = colNum("acno.cash_loc_max");
      }
      if(lmCashAft!=0)	lmCashAft = commString.numScale(lmCashAft, 0);

      idsAcno.ppdouble("adj_before_loc_amt", colNum("aclg.bef_loc_amt"));
      idsAcno.ppdouble("line_of_credit_amt", lmAftAmt);
      if (colEq("aclg.adj_loc_flag", "1")) {
        // 調高
        idsAcno.ppymd("h_adj_loc_high_date");
        idsAcno.ppstr2("adj_loc_high_t", colStr("aclg.log_reason"));
      } else if (colEq("aclg.adj_loc_flag", "2")) {
        // --調低
        idsAcno.ppymd("h_adj_loc_low_date");
        idsAcno.ppstr2("adj_loc_low_t", colStr("aclg.log_reason"));
      }
      // --預借現金--
      String lsNoAdjHighCash = colStr("acno.no_adj_h_cash");
      String lsCashDate1 = colStr("acno.no_adj_h_s_date_cash");
      String lsCashDate2 = colStr("acno.no_adj_h_e_date_cash");
      if (empty(lsCashDate2))
        lsCashDate2 = "99991231";
      // -R100-009-
      boolean lbCashAdj = true;
      if (lmCashBef <= 0)
        lbCashAdj = false;
      if (lmCashBef == lmCashAft)
        lbCashAdj = false;
      if (lmCashAft > lmCashBef) {
    	  double lmCashRateO = 0.0 ;
    	  if(colNum("acno.line_of_credit_amt") == 0)
    		  lmCashRateO = 0.0;
    	  else
    		  lmCashRateO = colNum("acno.line_of_credit_amt_cash") / colNum("acno.line_of_credit_amt") * 100;
//        double lmCashRateO =
//            colNum("acno.line_of_credit_amt_cash") / colNum("acno.line_of_credit_amt") * 100;
        if(lmCashRateO!=0)	lmCashRateO = commString.numScale(lmCashRateO, 0);
        if (colNum("acno.cash_loc_rate") != lmCashRateO)
          lbCashAdj = false;
      }
      if (lbCashAdj) {
        if (lmCashBef >= lmCashAft) { // -調低-
          idsAcno.ppdouble("line_of_credit_amt_cash", lmCashAft);
          wp.itemSet("wk_bef_loc_cash", "" + lmCashBef);
          wp.itemSet("wk_aft_loc_cash", "" + lmCashAft);
        } else {
          if (!(eqIgno(lsNoAdjHighCash, "Y") && commDate.sysComp(lsCashDate1) >= 0
              && commDate.sysComp(lsCashDate2) <= 0)) {
            idsAcno.ppdouble("line_of_credit_amt_cash", lmCashAft);
            wp.itemSet("wk_bef_loc_cash", "" + lmCashBef);
            wp.itemSet("wk_aft_loc_cash", "" + lmCashAft);
          }
        }
      }
    }
    idsAcno.modxxx(modUser, modPgm);
    idsAcno.sql2Where("where acno_p_seqno =?", pSeqno);
    sqlExec(idsAcno.sqlStmt(), idsAcno.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("update act_acno error; kk[%s]", pSeqno);
      return;
    }

    // --Combo-card-----------------
    // -B95-015:Combo卡信用額度由批次調整- 
    //-- **2020/12/31 TCB COMBO無須特殊處理 (指撥/撥回)
//    if (eqIgno(lsComboInd, "Y") == false || (lmComboBef > lmCashAft) == false) {
//      return;
//    }
//    strSql = "select card_no, combo_acct_no," + " uf_idno_id(id_p_seqno) as id_no"
//        + " from crd_card" + " where acno_p_seqno =?" + " and current_code ='0' and sup_flag ='0'"
//        + " and oppost_date =''" + commSqlStr.rownum(1);
//    setString2(1, pSeqno);
//    daoTid = "card.";
//    sqlSelect(strSql);
//    if (sqlRowNum <= 0)
//      return;
//
//    ilCombo++;
//    strSql = "insert into voc_appc_temp (" + " assign_kind, tx_date, tx_seq, card_no"
//        + ", acct_id, acct_no, effc_month, tx_amt, combo_amt"
//        + ", mod_user, mod_time, mod_pgm, mod_seqno " + " ) values (" + "'2'," + commSqlStr.sysYYmd
//        + ",0," + " ?," // -card_no-
//        + " ?," // acct_id
//        + " ?," // acct_no
//        + " to_char(sysdate,'yyyymm')," // effc_month
//        + " ?," // ex_amt
//        + " ?," // combo-amt
//        + " ?,sysdate,?,1)" // mod-xxx
//    ;
//    setString2(1, colStr("card.card_no"));
//    setString(colStr("card.id_no"));
//    setString(colStr("card.combo_acct_no"));
//    setDouble(lmComboBef - lmCashAft);
//    setDouble(lmCashAft);
//    setString(modUser);
//    setString(modPgm);
//    sqlExec(strSql);
//    if (sqlRowNum <= 0) {
//      errmsg("insert voc_appc_temp error; kk[%s]", colStr("card.card_no"));
//      return;
//    }
  }
  // void update_act_corp_gp() {
  // errmsg("???未完成");
  // }

  void updateCrdCard() {
    strSql = "update crd_card set" + " indiv_crd_lmt =?" + " where card_no =?";
    setDouble2(1, colNum("aclg.aft_loc_amt"));
    setString2(2, cardNo);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update crd_card error, kk[%s]", cardNo);
    }
  }

  public int upateCcaCardAcct() {

    String lsHigh = "", lsSysdate = "";
    lsSysdate = getSysDate();
    if (varsNum("aft_loc_amt") > varsNum("bef_loc_amt")) {
      lsHigh = "Y";
    } else {
      lsHigh = "N";
    }

    String sql1 = " select " + " adj_eff_start_date , " + " adj_eff_end_date , "
        + " tot_amt_month , " + " card_acct_idx , " + " debit_flag , " + " id_p_seqno , "
        + " adj_inst_pct , " + " adj_area " + " from cca_card_acct " + " where acno_p_seqno = ? "
        + " and debit_flag <> 'Y' ";

    sqlSelect(sql1, new Object[] {varsStr("p_seqno")});

    if (sqlRowNum <= 0) {
      errmsg("select cca_card_acct error !");
      return rc;
    }

    if (!empty(colStr("adj_eff_start_date")) && !empty(colStr("adj_eff_end_date"))) {
      if (lsSysdate.compareTo(colStr("adj_eff_start_date")) >= 0
          && lsSysdate.compareTo(colStr("adj_eff_end_date")) <= 0) {
        if (eqIgno(lsHigh, "Y") && varsNum("bef_loc_amt") > colNum("tot_amt_month")) {
          strSql = " update cca_card_acct set " + " tot_amt_month =:tot_amt_month "
              + " where acno_p_seqno =:p_seqno " + " and debit_flag <> 'Y' ";

          setDouble("tot_amt_month", varsNum("bef_loc_amt"));
          var2ParmStr("p_seqno");

          sqlExec(strSql);
          if (sqlRowNum <= 0) {
            errmsg("update cca_card_acct error !");
            return rc;
          }

        } else if (eqIgno(lsHigh, "N") && varsNum("bef_loc_amt") < colNum("tot_amt_month")) {
          strSql = " update cca_card_acct set " + " tot_amt_month =:tot_amt_month "
              + " where acno_p_seqno =:p_seqno " + " and debit_flag <> 'Y' ";

          setDouble("tot_amt_month", varsNum("bef_loc_amt"));
          var2ParmStr("p_seqno");

          sqlExec(strSql);
          if (sqlRowNum <= 0) {
            errmsg("update cca_card_acct error !");
            return rc;
          }
        }
      }
    }

    strSql = " insert into cca_limit_adj_log (" + " log_date , " + " log_time , " + " aud_code , "
        + " card_acct_idx , " + " debit_flag , " + " mod_type , " + " lmt_tot_consume , "
        + " tot_amt_month_b , " + " tot_amt_month , " + " adj_inst_pct_b , " + " adj_inst_pct , "
        + " adj_eff_date1 , " + " adj_eff_date2 , " + " adj_reason , " + " adj_remark , "
        + " adj_area , " + " adj_user , " + " adj_date , " + " adj_time , " + " apr_user "
        + " ) values ( " + " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , "
        + " 'U' , " + " :card_acct_idx , " + " :debit_flag , " + " '1' , " + " :lmt_tot_consume , "
        + " :tot_amt_month_b , " + " :tot_amt_month , " + " :adj_inst_pct_b , "
        + " :adj_inst_pct , " + " :adj_eff_date1 , " + " :adj_eff_date2 , " + " :adj_reason , "
        + " :adj_remark , " + " :adj_area , " + " :adj_user , " + " to_char(sysdate,'yyyymmdd') , "
        + " to_char(sysdate,'hh24miss') , " + " :apr_user " + " ) ";

    col2ParmNum("card_acct_idx");
    col2ParmStr("debit_flag");
    setDouble("lmt_tot_consume", varsNum("bef_loc_amt"));
    col2ParmNum("tot_amt_month_b", "tot_amt_month");
    var2ParmNum("tot_amt_month", "aft_loc_amt");
    col2ParmNum("adj_inst_pct_b", "adj_inst_pct");
    col2ParmNum("adj_inst_pct", "adj_inst_pct");
    col2ParmStr("adj_eff_date1", "adj_eff_start_date");
    col2ParmStr("adj_eff_date2", "adj_eff_end_date");
    var2ParmStr("adj_reason", "log_reason");
    var2ParmStr("adj_reason", "log_remark");
    col2ParmStr("adj_area");
    var2ParmStr("adj_user", "mod_user");
    var2ParmStr("apr_user");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("insert cca_limit_adj_log error !");
    }

    return rc;
  }


}
