/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-28   V1.00.02 Justin         parameterize sql
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Ichr0030 extends BaseAction implements InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (chkStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2")) == false) {
      alertErr2("請款日期:起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 and online_mark in ('0','2') "
        + sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "substr(batch_no,1,8)");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " card_no , " + " ich_card_no , " + " txn_date ";

    wp.daoTable = " ich_a07b_add ";

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

    queryReadAfter();

  }

  void queryReadAfter() {
    String lsBlockCond = "", lsCardActive = "", lsBlockFlag = "", lsOverFlag = "";
    double lmEndBal = 0;
    int ilSqlCnt = 0;
    ilSqlCnt = wp.selectCnt;

    lsBlockCond = getBlockCond();

    String sql1 = " select " + " A.line_of_credit_amt , "
    // + " A.card_acct_idx , "
        + " uf_idno_id(B.id_p_seqno) as id_no , " + " B.p_seqno , " + " B.activate_date "
        + ", C.block_reason1, C.block_reason2, C.block_reason3"
        + ", C.block_reason4, C.block_reason5"
        + " from act_acno A join crd_card B on A.p_seqno = B.p_seqno "
        + " left join cca_card_acct C on C.p_seqno=B.p_seqno and C.acct_type=B.acct_type"
        + " where B.card_no = ? ";

    // String sql2 = " select "
    // + " block_reason1 , "
    // + " block_reason2 , "
    // + " block_reason3 , "
    // + " block_reason4 , "
    // + " block_reason5 "
    // + " from cca_card_acct "
    // + " where card_acct_idx = ? "
    ;
    String lsBlockReason, lsBlockReason1 = "", lsBlockReason2 = "", lsBlockReason3 = "",
        lsBlockReason4 = "", lsBlockReason5 = "";
    for (int ii = 0; ii < ilSqlCnt; ii++) {
      lsCardActive = "";
      lsBlockFlag = "";
      lsOverFlag = "";
      lsBlockReason = "";
      lsBlockReason1 = "";
      lsBlockReason2 = "";
      lsBlockReason3 = "";
      lsBlockReason4 = "";
      lsBlockReason5 = "";
      if (wp.colEmpty(ii, "card_no"))
        continue;
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no")});
      if (sqlRowNum <= 0)
        continue;
      // sqlSelect(sql2,new Object[]{wp.col_num(ii,"card_acct_idx")});
      // if(sql_nrow<=0) continue;

      lsBlockReason1 = sqlStr("block_reason1");
      lsBlockReason2 = sqlStr("block_reason2");
      lsBlockReason3 = sqlStr("block_reason3");
      lsBlockReason4 = sqlStr("block_reason4");
      lsBlockReason5 = sqlStr("block_reason5");

      if (commString.pos(",61,71,72,73,74,81,91", lsBlockReason1) > 0)
        lsBlockReason1 = "";
      if (commString.pos(",61,71,72,73,74,81,91", lsBlockReason2) > 0)
        lsBlockReason2 = "";
      if (commString.pos(",61,71,72,73,74,81,91", lsBlockReason3) > 0)
        lsBlockReason3 = "";
      if (commString.pos(",61,71,72,73,74,81,91", lsBlockReason4) > 0)
        lsBlockReason4 = "";
      if (commString.pos(",61,71,72,73,74,81,91", lsBlockReason5) > 0)
        lsBlockReason5 = "";

      lsBlockReason +=
          lsBlockReason1 + lsBlockReason2 + lsBlockReason3 + lsBlockReason4 + lsBlockReason5;
      lmEndBal = wfGetToBal(sqlStr("p_seqno"));
      if (empty(sqlStr("activate_date")))
        lsCardActive = "N";
      else
        lsCardActive = "Y";
      if (empty(lsBlockReason))
        lsBlockFlag = "N";
      else
        lsBlockFlag = "Y";
      if (lmEndBal > sqlNum("line_of_credit_amt"))
        lsOverFlag = "Y";
      else
        lsOverFlag = "N";

      wp.colSet(ii, "id_no", sqlStr("id_no"));
      wp.colSet(ii, "limit_amt", sqlStr("line_of_credit_amt"));
      wp.colSet(ii, "end_bal", lmEndBal);
      wp.colSet(ii, "active_date", sqlStr("activate_date"));
      wp.colSet(ii, "over_limit", lmEndBal - sqlNum("line_of_credit_amt"));
      wp.colSet(ii, "wk_block_reason", lsBlockReason);
      wp.colSet(ii, "card_active", lsCardActive);
      wp.colSet(ii, "over_limit", lsOverFlag);
      wp.colSet(ii, "block", lsBlockFlag);

    }

  }

  double wfGetToBal(String aPSeqno) {
    double lmDebt = 0, lmCashUse = 0, lmUnbillAmt = 0, lmUnbillFee = 0;

    String sql1 = " select sum(end_bal) as lm_debt from act_debt where p_seqno in "
        + " (select gp_no from act_acno where p_seqno = ? ) ";

    sqlSelect(sql1, new Object[] {aPSeqno});
    lmDebt = sqlNum("lm_debt");

    String sql2 = " select sum(A.cash_use_balance) as lm_cash_use "
        + " from act_combo_m_jrnl A join act_acno B on A.acct_type = B.acct_type "
        + " A.id_p_seqno = B.id_p_seqno where B.p_seqno = ? ";

    sqlSelect(sql2, new Object[] {aPSeqno});
    lmCashUse = sqlNum("lm_cash_use");

    String sql3 = " select "
        + " sum(A.unit_price * (A.install_tot_term - A.install_curr_term) + A.remd_amt + "
        + " decode(A.install_curr_term,0,A.first_remd_amt+A.extra_fees,0)) as lm_unbill_amt , "
        + " sum(A.lm_unbill_amt * (A.clt_install_tot_term - A.install_curr_term) + A.clt_remd_amt) as lm_unbill_fee "
        + " from bil_contract A join act_acno B on A.acct_type = B.acct_type and A.id_p_seqno = B.id_p_seqno "
        + " where B.p_seqno = ? ";

    sqlSelect(sql3, new Object[] {aPSeqno});
    lmUnbillAmt = sqlNum("lm_unbill_amt");
    lmUnbillFee = sqlNum("lm_unbill_fee");


    return lmDebt + lmCashUse + lmUnbillAmt + lmUnbillFee;
  }

  String getBlockCond() {
    String sql1 = " select block_reason from ich_00_parm where parm_type='ICHM0040' "
        + " and apr_date <> '' and block_cond ='Y' " + commSqlStr.rownum(1);
    sqlSelect(sql1, new Object[] {});
    if (sqlRowNum <= 0)
      return "";
    return sqlStr("block_reason");
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

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "ichr0030";
    wp.pageRows = 9999;
    String tmpStr = "";
    tmpStr = "請款日期:" + commString.strToYmd(wp.itemStr2("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr2("ex_date2"));
    wp.colSet("cond1", tmpStr);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDFLine pdf = new TarokoPDFLine();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ichr0030.xlsx";
    // pdf.pageVert =false;
    // pdf.pageCount = 30;
    // pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
