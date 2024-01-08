/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-20  V1.00.01  ryan       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
******************************************************************************/
package mktp02;

import busi.SqlPrepare;
import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;


public class Mktp4100 extends BaseProc {

  int rr = -1;
  String msg = "", msgok = "";
  int ilOk = 0;
  int ilErr = 0;
  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {}

  // for query use only
  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_mchtno1");
    String lsDate2 = wp.itemStr("ex_mchtno2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[特店代號-起迄]  輸入錯誤");
      return -1;
    }

    lsDate1 = wp.itemStr("ex_date1");
    lsDate2 = wp.itemStr("ex_date2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[建檔期間-起迄]  輸入錯誤");
      return -1;
    }

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_mchtno1")) == false) {
      wp.whereStr += " and mcht_no >= :ex_mchtno1 ";
      setString("ex_mchtno1", wp.itemStr("ex_mchtno1"));
    }
    if (empty(wp.itemStr("ex_mchtno2")) == false) {
      wp.whereStr += " and mcht_no <= :ex_mchtno2 ";
      setString("ex_mchtno2", wp.itemStr("ex_mchtno2"));
    }
    if (empty(wp.itemStr("ex_userid")) == false) {
      wp.whereStr += " and mod_user = :ex_userid ";
      setString("ex_userid", wp.itemStr("ex_userid"));
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " hex(rowid) as rowid," + "mcht_no, "
        + "UF_MCHT_NAME_BIL(mcht_no) as db_mcht_cname, " + "product_no, " + "product_name, "
        + "seq_no, " + "start_date, " + "end_date, " + "unit_price, " + "tot_amt, " + "tot_term, "
        + "remd_amt, " + "extra_fees, " + "fees_fix_amt, " + "fees_min_amt, " + "fees_max_amt, "
        + "interest_rate, " + "interest_min_rate, " + "interest_max_rate, " + "clt_fees_fix_amt, "
        + "clt_interest_rate, " + "trans_rate, " + "against_num, " + "dtl_flag, " + "confirm_flag, "
        + "limit_min, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno, "
        + "installment_flag, " + "mod_audcode, " + "year_fees_rate ";

    wp.daoTable = " bil_prod_nccc_t ";
    wp.whereOrder = " order by mcht_no , product_no ";
    if (getWhereStr() != 1)
      return;
    pageQuery();    
    if (sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
    list_wkdata();
  }


  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    String kkRowid = wp.itemStr("data_k1");

    wp.selectSQL = " hex(rowid) as rowid," + "mcht_no, "
        + "UF_MCHT_NAME_BIL(mcht_no) as db_mcht_cname, " + "product_no, " + "product_name, "
        + "seq_no, " + "start_date, " + "end_date, " + "unit_price, " + "tot_amt, " + "tot_term, "
        + "remd_amt, " + "extra_fees, " + "fees_fix_amt, " + "fees_min_amt, " + "fees_max_amt, "
        + "interest_rate, " + "interest_min_rate, " + "interest_max_rate, " + "clt_fees_fix_amt, "
        + "clt_interest_rate, " + "trans_rate, " + "against_num, " + "dtl_flag, " + "confirm_flag, "
        + "limit_min, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno, "
        + "installment_flag, " + "mod_audcode, " + "year_fees_rate ";

    wp.daoTable = " bil_prod_nccc_t ";
    wp.whereOrder = "  ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and rowid = ? ";
    setRowid(1,kkRowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("資料不存在 !");
      return;
    }
    listWkdata2();
  }

  @Override
  public void dataProcess() throws Exception {    
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");

    wp.listCount[0] = wp.itemRows("rowid");
    
    rr = optToIndex(aaOpt[0]);
    if (rr < 0) {
      alertErr2("請點選欲覆核資料");
      return;
    }
    
    // -update-
    for (int ii = 0; ii < aaOpt.length; ii++) {    	
    	rr = optToIndex(aaOpt[ii]);
        if (rr < 0) {        	
        	continue;
        }
        if (wf_upd_file() != 1) {        	
        	ilErr++;
        	wp.colSet(rr, "ok_flag", "X");
        	sqlCommit(0);
        	continue;
        }
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        sqlCommit(1);
    }
    
    alertMsg("資料處理, 成功筆數= " + ilOk + " ,失敗筆數= " + ilErr);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void list_wkdata() {
    String dbType01 = "", dbType05 = "", dbType06 = "", dbType02 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {

      dbType01 = wfGetActtype(wp.colStr(ii, "mcht_no"), wp.colStr(ii, "product_no"),
          (int) wp.colNum(ii, "seq_no"), "01");
      dbType05 = wfGetActtype(wp.colStr(ii, "mcht_no"), wp.colStr(ii, "product_no"),
          (int) wp.colNum(ii, "seq_no"), "05");
      dbType06 = wfGetActtype(wp.colStr(ii, "mcht_no"), wp.colStr(ii, "product_no"),
          (int) wp.colNum(ii, "seq_no"), "06");
      dbType02 = wfGetActtype(wp.colStr(ii, "mcht_no"), wp.colStr(ii, "product_no"),
          (int) wp.colNum(ii, "seq_no"), "02");
      wp.colSet(ii, "db_type01", dbType01);
      wp.colSet(ii, "db_type05", dbType05);
      wp.colSet(ii, "db_type06", dbType06);
      wp.colSet(ii, "db_type02", dbType02);
    }
  }

  void listWkdata2() throws Exception {
    String lsVal = "", lsDesc = "", sqlSelect = "";
    String mchtNo = wp.colStr("mcht_no");
    String productNo = wp.colStr("product_no");
    String seqNo = wp.colStr("seq_no");
    wp.pageControl();
    wp.selectSQL = " dtl_kind,dtl_value";
    wp.daoTable = " bil_prod_nccc_bin_t ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and mcht_no = :mcht_no ";
    setString("mcht_no", mchtNo);
    wp.whereStr += " and product_no = :product_no ";
    setString("product_no", productNo);
    wp.whereStr += " and seq_no = :seq_no ";
    setString("seq_no", seqNo);
    wp.whereOrder = "  ";
    pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      selectOK();
      return;
    }

    int x = 0;
    int y = 0;
    int z = 0;
    for (int i = 0; i < wp.selectCnt; i++) {
      lsVal = wp.colStr(i, "dtl_value");
      switch (wp.colStr(i, "dtl_kind")) {
        case "ACCT-TYPE":
          sqlSelect = "select chin_name FROM ptr_acct_type WHERE acct_type = :ls_val ";
          setString("ls_val", lsVal);
          sqlSelect(sqlSelect);
          lsDesc = sqlStr("chin_name");
          if (x == 0) {
            wp.colSet(i, "tt_dtl_kind", "帳戶類別");
          }
          x++;
          break;
        case "GROUP-CODE":
          sqlSelect = "select group_name FROM ptr_group_code WHERE group_code = :ls_val ";
          setString("ls_val", lsVal);
          sqlSelect(sqlSelect);
          lsDesc = sqlStr("group_name");
          if (y == 0) {
            wp.colSet(i, "tt_dtl_kind", "團體代號");
          }
          y++;
          break;
        case "CARD-TYPE":
          sqlSelect = "select name FROM ptr_card_type WHERE card_type = :ls_val ";
          setString("ls_val", lsVal);
          sqlSelect(sqlSelect);
          lsDesc = sqlStr("name");
          if (z == 0) {
            wp.colSet(i, "tt_dtl_kind", "卡種");

          }
          z++;
          break;
      }
      wp.colSet(i, "db_dtldesc", lsDesc);
    }
  }

  String wfGetActtype(String asMchtno, String asProdno, int asSeqno, String asType) {

    String sqlSelect = "select dtl_value01 from ( " + " select '1' as key,dtl_value as dtl_value01 "
        + " from bil_prod_nccc_bin_t " + " where  dtl_kind = 'ACCT-TYPE' "
        + " and MCHT_NO=:as_mchtno " + " and product_no=:as_prodno " + " and seq_no=:as_seqno "
        + " and dtl_value=:as_type ) as a ";
    setString("as_mchtno", asMchtno);
    setString("as_prodno", asProdno);
    setString("as_seqno", Integer.toString(asSeqno));
    setString("as_type", asType);
    sqlSelect(sqlSelect);
    String sScttype = sqlStr("dtl_value01");
    if (sqlRowNum <= 0) {
      return "";
    }
    if (empty(sScttype)) {
      return "";
    }
    return "Y";
  }

  int wf_upd_file() {
    busi.SqlPrepare sp = new SqlPrepare();
    String[] sMchtno = wp.itemBuff("mcht_no");
    String[] sProdno = wp.itemBuff("product_no");
    String[] lSeqno = wp.itemBuff("seq_no");
    String[] aaProductName = wp.itemBuff("product_name");
    String[] aaStartDate = wp.itemBuff("start_date");
    String[] aaEndDate = wp.itemBuff("end_date");
    String[] aaTotAmt = wp.itemBuff("tot_amt");
    String[] aaTotTerm = wp.itemBuff("tot_term");
    String[] aaLimitMin = wp.itemBuff("limit_min");
    String[] aaCltFeesFixAmt = wp.itemBuff("clt_fees_fix_amt");
    String[] aaCltInterestRate = wp.itemBuff("clt_interest_rate");
    String[] aaFeesFixAmt = wp.itemBuff("fees_fix_amt");
    String[] aaInterestRate = wp.itemBuff("interest_rate");
    String[] aaInstallmentFlag = wp.itemBuff("installment_flag");
    String[] aaTransRate = wp.itemBuff("trans_rate");
    String[] aaYearFeesRate = wp.itemBuff("year_fees_rate");
    String[] aaDtlFlag = wp.itemBuff("dtl_flag");

    String sqlSelect = "select count(*) as cnt from bil_prod_nccc " + " where mcht_no = :s_mchtno "
        + " and product_no = :s_prodno " + " and seq_no = :l_seqno ";
    setString("s_mchtno", sMchtno[rr]);
    setString("s_prodno", sProdno[rr]);
    setString("l_seqno", lSeqno[rr]);
    sqlSelect(sqlSelect);

    if (sqlNum("cnt") <= 0) {
      sp.sql2Insert("bil_prod_nccc");
      sp.ppstr("mcht_no", sMchtno[rr]);
      sp.ppstr("product_no", sProdno[rr]);
      sp.ppnum("seq_no", this.toInt(lSeqno[rr]));
      sp.ppstr("product_name", aaProductName[rr]);
      sp.ppstr("start_date", aaStartDate[rr]);
      sp.ppstr("end_date", aaEndDate[rr]);
      sp.ppnum("tot_amt", this.toNum(aaTotAmt[rr]));
      sp.ppnum("tot_term", this.toInt(aaTotTerm[rr]));
      sp.ppnum("limit_min", this.toNum(aaLimitMin[rr]));
      sp.ppnum("clt_fees_fix_amt", this.toNum(aaCltFeesFixAmt[rr]));
      sp.ppnum("clt_interest_rate", this.toNum(aaCltInterestRate[rr]));
      sp.ppnum("fees_fix_amt", this.toNum(aaFeesFixAmt[rr]));
      sp.ppnum("interest_rate", this.toNum(aaInterestRate[rr]));
      sp.ppstr("installment_flag", aaInstallmentFlag[rr]);
      sp.ppstr("trans_rate", aaTransRate[rr]);
      sp.ppstr("year_fees_rate", aaYearFeesRate[rr]);
      sp.ppstr("dtl_flag", aaDtlFlag[rr]);
      sp.ppstr("confirm_flag", "Y");
      sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_seqno ", ", 1 ");
      sp.addsql(", mod_time ", ", sysdate ");
    } else {
      sp.sql2Update("bil_prod_nccc");
      sp.ppstr("product_name", aaProductName[rr]);
      sp.ppstr("start_date", aaStartDate[rr]);
      sp.ppstr("end_date", aaEndDate[rr]);
      sp.ppnum("tot_amt", this.toNum(aaTotAmt[rr]));
      sp.ppnum("tot_term", this.toInt(aaTotTerm[rr]));
      sp.ppnum("limit_min", this.toNum(aaLimitMin[rr]));
      sp.ppnum("clt_fees_fix_amt", this.toNum(aaCltFeesFixAmt[rr]));
      sp.ppnum("clt_interest_rate", this.toNum(aaCltInterestRate[rr]));
      sp.ppnum("fees_fix_amt", this.toNum(aaFeesFixAmt[rr]));
      sp.ppnum("interest_rate", this.toNum(aaInterestRate[rr]));
      sp.ppstr("installment_flag", aaInstallmentFlag[rr]);
      sp.ppstr("trans_rate", aaTransRate[rr]);
      sp.ppstr("year_fees_rate", aaYearFeesRate[rr]);
      sp.ppstr("dtl_flag", aaDtlFlag[rr]);
      sp.ppstr("confirm_flag", "Y");
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
      sp.sql2Where(" where mcht_no=?", sMchtno[rr]);
      sp.sql2Where(" and product_no=?", sProdno[rr]);
      sp.sql2Where(" and seq_no=?", this.toInt(lSeqno[rr]));
    }
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      wp.colSet(rr, "errmsg", "update bil_prod_nccc erro");
      return -1;
    }
    // --Update bil_prod_nccc_bin----------------------------------------------
    String sqlDelete = "Delete From bil_prod_nccc_bin " + " Where mcht_no = :s_mchtno "
        + " and product_no  = :s_prodno " + " and seq_no = :l_seqno ";
    setString("s_mchtno", sMchtno[rr]);
    setString("s_prodno", sProdno[rr]);
    setString("l_seqno", lSeqno[rr]);
    sqlExec(sqlDelete);
    if (sqlRowNum < 0) {
      wp.colSet(rr, "errmsg", "Delete bil_prod_nccc_bin error");
      return -1;
    }
    String sqlInsert =
        "INSERT INTO bil_prod_nccc_bin  ( mcht_no, product_no, seq_no, bin_no, dtl_kind, dtl_value ) "
            + " SELECT mcht_no, product_no, seq_no, bin_no, dtl_kind, dtl_value "
            + " FROM bil_prod_nccc_bin_t " + " Where mcht_no = :s_mchtno "
            + " and product_no  = :s_prodno " + " and seq_no = :l_seqno ";
    setString("s_mchtno", sMchtno[rr]);
    setString("s_prodno", sProdno[rr]);
    setString("l_seqno", lSeqno[rr]);
    sqlExec(sqlInsert);
    if (sqlRowNum < 0) {
      wp.colSet(rr, "errmsg", "INSERT bil_prod_nccc_bin error");
      return -1;
    }

    // --Delete Temp-File-
    sqlDelete = "Delete From bil_prod_nccc_t " + " Where mcht_no = :s_mchtno "
        + " and product_no = :s_prodno" + " and seq_no = :l_seqno";
    setString("s_mchtno", sMchtno[rr]);
    setString("s_prodno", sProdno[rr]);
    setString("l_seqno", lSeqno[rr]);
    sqlExec(sqlDelete);
    if (sqlRowNum <= 0) {
      wp.colSet(rr, "errmsg", "Delete bil_prod_nccc_t error");
      return -1;
    }


    sqlDelete = "Delete From bil_prod_nccc_bin_t " + " Where mcht_no = :s_mchtno "
        + " and product_no = :s_prodno" + " and seq_no = :l_seqno";
    setString("s_mchtno", sMchtno[rr]);
    setString("s_prodno", sProdno[rr]);
    setString("l_seqno", lSeqno[rr]);
    sqlExec(sqlDelete);
    if (sqlRowNum < 0) {
      wp.colSet(rr, "errmsg", "Delete bil_prod_nccc_bin_t error");
      return -1;
    }

    return 1;
  }

}
