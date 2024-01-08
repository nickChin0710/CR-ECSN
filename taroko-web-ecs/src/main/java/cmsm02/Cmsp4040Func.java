/*客服D檔調整匯入作業 V.2018-0726.JH
 * 2018-0726:	JH		modify
 * 109-04-27  shiyuqi       updated for project coding standard     *  
 * */
package cmsm02;

import busi.FuncAction;


public class Cmsp4040Func extends FuncAction {

  String isErrCode = "", isAdjustType = "", isJobCode = "", isVouchJobCode = "";
  double imChgRate = 1;
  busi.SqlPrepare spAcaj = new busi.SqlPrepare();

  void selectCmsAcaj(String aRowid) {
    imChgRate = 1;

    strSql = "select A.*" + ", uf_dc_amt2(A.d_avail_bal, A.dc_d_avail_bal) as db_d_avail_bal"
        + " from cms_acaj A " + " where rowid =?";
    setRowId2(1, aRowid);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("D檔調整資料, 已不存在");
    }

    if (!eqIgno(colNvl("curr_code", "901"), "901") && colNum("dc_beg_bal") > 0) {
      imChgRate = colNum("beg_bal") / colNum("dc_beg_bal");
    }

  }

  @Override
  public void dataCheck() {
    isErrCode = "";

    // -acaj尚未處理-
    String sql1 = "select count(*) as db_cnt1 " + " from act_acaj " + " where reference_no =? "
        + " and nvl(process_flag,'N')<>'Y' ";
    sqlSelect(sql1, new Object[] {colStr("reference_no")});
    if (colNum("db_cnt1") > 0) {
      isErrCode = "01";
      return;
    }

    // -問交未結案------------------
    String sql2 = "select count(*) as db_cnt2 " + " from rsk_problem " + " where reference_no =? "
        + " and nvl(prb_status,' ')<>'80' ";
    sqlSelect(sql2, new Object[] {colStr("reference_no")});
    if (colNum("db_cnt2") > 0) {
      isErrCode = "03";
      return;
    }
    // -查無來源帳款------------------------------------
    String sql3 =
        "select count(*) as db_cnt3 " + " from ( select 1 from act_debt where reference_no =? "
            + " union select 1 from act_debt_hst where reference_no =?" + " )";
    sqlSelect(sql3, new Object[] {colStr("reference_no"), colStr("reference_no")});
    if (colNum("db_cnt3") == 0) {
      isErrCode = "02";
      return;
    }
    // -D檔金額<0-
    if (colNum("adj_amt") < 0) {
      isErrCode = "04";
      return;
    }
    // -D檔金額>可D數--------------------------
    double lmDAvailAmt = colNum("db_d_avail_bal");
    if (colNum("adj_amt") > lmDAvailAmt) {
      isErrCode = "05";
      return;
    }
    // -調整類別比對不到----------------------
    String lsAcctCode = colStr("acct_code");
    String lsBillType = commString.mid(colStr("bill_type"), 1, 1);
    if (eqIgno(lsAcctCode, "ID")) {
      if (eqIgno(lsBillType, "1")) {
        isAdjustType = "DE01";
      } else if (eqIgno(lsBillType, "2")) {
        isAdjustType = "DE04";
      } else {
        isAdjustType = "DE07";
      }
    } else if (commString.strIn(lsAcctCode, ",BL,CB,CA,IT,AO,DB,OT")) {
      isAdjustType = "DE08";
    } else if (commString.strIn(lsAcctCode, ",AF,LF,CF,PF,SF,CC")) {
      isAdjustType = "DE09";
    } else if (commString.strIn(lsAcctCode, ",RI,AI,CI")) {
      isAdjustType = "DE13";
    } else if (commString.strIn(lsAcctCode, ",PN")) {
      isAdjustType = "DE14";
    } else {
      isErrCode = "06";
    }

    if (empty(isJobCode)) {
      selectPtrDeptCode();
    }
  }

  void selectPtrDeptCode() {
    // -部門代號,起帳部門代碼-----
    strSql = "select gl_code" + " from ptr_dept_code" + " where dept_code =?";
    setString2(1, wp.loginDeptNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      isErrCode = "07";
    }
    isJobCode = wp.loginDeptNo;
    isVouchJobCode = "0" + commString.mid(colStr("gl_code"), 0, 1);
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
    selectCmsAcaj(varsStr("rowid"));
    if (rc != 1)
      return -1;

    dataCheck();
    if (rc != 1) {
      return rc;
    }

    if (empty(isErrCode)) {
      insertActAcaj();
    }
    if (rc == 1) {
      updateCmsAcaj();
    }

    return rc;
  }

  void insertActAcaj() {
    msgOK();
    busi.CommCurr oocurr = new busi.CommCurr();

    double lmDrAmt = colNum("adj_amt");
    if (imChgRate != 1) {
      lmDrAmt = oocurr.dc2twAmt(colNum("beg_bal"), colNum("dc_beg_bal"), colNum("adj_amt"));
    }
    double lmAftAmt = colNum("end_bal") - lmDrAmt;
    double lmAftDAmt = colNum("d_avail_bal") - lmDrAmt;

    spAcaj.sql2Insert("act_acaj");
    spAcaj.addsqlParm("  p_seqno", "?", colStr("p_seqno"));
    spAcaj.addsqlParm(", acct_type", ", ?", colStr("acct_type"));
    spAcaj.addsqlParm(", adjust_type", ", ?", isAdjustType);
    spAcaj.addsqlParm(", reference_no", ", ?", colStr("reference_no"));
    spAcaj.addsqlParm(", post_date", ", ?", colStr("item_post_date"));
    spAcaj.addsqlParm(", acct_code", ", ?", colStr("acct_code"));
    spAcaj.addsqlParm(", function_code", ", 'U'");
    spAcaj.addsqlParm(", card_no", ", ?", colStr("card_no"));
    spAcaj.addsqlParm(", interest_date", ", ?", colStr("interest_date"));
    spAcaj.addsqlParm(", adj_comment", ", ?", colStr("adj_remark"));
    spAcaj.addsqlParm(", cr_amt", ", ?", 0);
    spAcaj.addsqlParm(", orginal_amt", ", ?", colNum("beg_bal"));
    spAcaj.addsqlParm(", dr_amt", ", ?", lmDrAmt);
    spAcaj.addsqlParm(", bef_amt", ", ?", colNum("end_bal"));
    spAcaj.addsqlParm(", bef_d_amt", ", ?", colNum("d_avail_bal"));
    spAcaj.addsqlParm(", aft_amt", ", ?", lmAftAmt);
    spAcaj.addsqlParm(", aft_d_amt", ", ?", lmAftDAmt);
    spAcaj.addsqlParm(", curr_code", ", ?", colNvl("curr_code", "901"));
    if (imChgRate == 1) {
      spAcaj.addsqlParm(", dc_orginal_amt", ", ?", colNum("beg_bal"));
      spAcaj.addsqlParm(", dc_dr_amt", ", ?", colNum("adj_amt"));
      spAcaj.addsqlParm(", dc_bef_amt", ", ?", colNum("end_bal"));
      spAcaj.addsqlParm(", dc_bef_d_amt", ", ?", colNum("d_avail_bal"));
      spAcaj.addsqlParm(", dc_aft_amt", ", ?", colNum("end_bal") - colNum("adj_amt"));
      spAcaj.addsqlParm(", dc_aft_d_amt", ", ?", colNum("d_avail_bal") - colNum("adj_amt"));
    } else {
      spAcaj.addsqlParm(", dc_orginal_amt", ", ?", colNum("dc_beg_bal"));
      spAcaj.addsqlParm(", dc_dr_amt", ", ?", colNum("adj_amt"));
      spAcaj.addsqlParm(", dc_bef_amt", ", ?", colNum("dc_end_bal"));
      spAcaj.addsqlParm(", dc_bef_d_amt", ", ?", colNum("dc_d_avail_bal"));
      spAcaj.addsqlParm(", dc_aft_amt", ", ?", colNum("dc_end_bal") - colNum("adj_amt"));
      spAcaj.addsqlParm(", dc_aft_d_amt", ", ?", colNum("dc_d_avail_bal") - colNum("adj_amt"));
    }
    spAcaj.addsqlParm(", apr_flag", ", 'N'");
    spAcaj.addsqlParm(", crt_user", ", ?", modUser);
    spAcaj.addsqlParm(", crt_date", ", " + commSqlStr.sysYYmd);
    spAcaj.addsqlParm(", crt_time", ", " + commSqlStr.sysTime);
    spAcaj.addsqlParm(", update_date", ", " + commSqlStr.sysYYmd);
    spAcaj.addsqlParm(", update_user", ", ?", modUser);
    spAcaj.addsqlParm(", mod_user", ", ?", modUser);
    spAcaj.addsqlParm(", mod_time", ", " + commSqlStr.sysdate);
    spAcaj.addsqlParm(", mod_pgm", ", ?", modPgm);
    spAcaj.addsqlParm(", mod_seqno", ", ?", 1);

    this.sqlExec(spAcaj.sqlStmt(), spAcaj.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("Insert act_acaj error, " + getMsg());
    }
    return;
  }

  void updateCmsAcaj() {
    msgOK();
    strSql =
        " update cms_acaj set " + " acct_post_flag ='Y' ," + " acct_post_user =:acct_post_user ,"
            + " acct_post_date = " + commSqlStr.sysYYmd + "," + " acct_errcode =:acct_errcode ,"
            + commSqlStr.setModxxx(modUser, modPgm) + " where rowid =:rowid ";
    setString2("acct_post_user", modUser);
    setString2("acct_errcode", isErrCode);
    // kk
    setRowId2("rowid", varsStr("rowid"));

    this.sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Update cms_acaj error, " + getMsg());
      rc = -1;
    }
    return;

  }


}
