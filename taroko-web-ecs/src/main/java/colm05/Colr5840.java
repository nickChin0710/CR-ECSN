package colm05;
/** 整批解凍客戶明細表
 * 2019-1209   JH    UAT
 * 2019-0726   JH    modify
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 2018/03/16				Alex			PDF列印修正
 * 109-05-06  V1.00.04  Tanwei       updated for project coding standard
 * */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;

import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Colr5840 extends BaseQuery implements InfacePdf {
  // int ii=-1;
  int iiPdfRow = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();

  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr("ex_acct_type");
      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_exec_date1"))) {
      alertErr2("執行日期起 : 不可空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_exec_date1"), wp.itemStr("ex_exec_date2")) == false) {
      alertErr2("執行日期起迄: 輸入錯誤");
      return;
    }

    wp.whereStr = " where param_type in ('2','4') "        
        + sqlCol(wp.itemStr("ex_exec_date1"),"exec_date",">=")
        + sqlCol(wp.itemStr("ex_exec_date2"),"exec_date","<=")
        + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
        ;

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " param_type , " + " acct_type , " + " valid_date , " + " exec_date , "
        + " print_flag2 , " + " t_acct_cnt , "
        + " t_block_cnt + t_block_cnt2 + t_block_cnt3 as db_block_cnt , "
        + " t_blocknot1_cnt + t_blocknot2_cnt + t_blocknot3_cnt + t_blocknot4_cnt as db_blocknot_cnt ";
    wp.daoTable = "rsk_blockexec";
    wp.whereOrder = " order by exec_date Desc, param_type Asc, acct_type Asc, valid_date Desc";
    logSql();
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.totalRows = wp.dataCnt;
    wp.setPageValue();
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
  public void pdfPrint() throws Exception {
    wp.reportId = "colr5840";
    dataPrint();
    wp.pageRows = 9999;
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "colr5840.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.setListIndex(2);
    pdf.procesPDFreport(wp);
    pdf = null;
    return;
  }

  void dataPrint() {
    String wkAcctType = "", wkExecDate = "", wkValidDate = "";
    String[] lsOpt = wp.itemBuff("opt");
    int rr = optToIndex(lsOpt[0]);
    if (rr < 0) {
      alertPdfErr("未點選列印資料");
      return;
    }
    String lsAcctType = wp.itemStr(rr, "acct_type");
    String lsExecDate = wp.itemStr(rr, "exec_date");
    String lsValidDate = wp.itemStr(rr, "valid_date");

    selectLogAcno(lsAcctType, lsExecDate);
    if (iiPdfRow <= 0) {
      alertPdfErr("無資料可列印");
      return;
    }

    wkAcctType = lsAcctType;
    wkExecDate = lsExecDate;
    wkValidDate = lsValidDate;

    String cond1;
    cond1 = "帳戶類別 : " + wkAcctType + " " + "生效日期 : " + commString.strToYmd(wkValidDate) + " "
        + "執行日期 : " + commString.strToYmd(wkExecDate);
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);

    // wp.listCount[0] =ii+1;
  }

  void selectLogAcno(String lsAcctType, String lsExecDate) {
    iiPdfRow = 0;
    String sql1 = "select row_number()over() as ser_num" + ", A.log_reason as ex_log_reason"
        + ", B.acct_key as ex_acct_key" + ", B.line_of_credit_amt as ex_line_of_credit_amt"
        + ", uf_acno_name(B.acno_p_seqno) as ex_chi_name"
        + ", decode(B.pay_by_stage_flag,'00','N','','N','Y') as ex_db_stage_flag "
        + ", A.mod_pgm as ex_mod_pgm" + ", B.p_seqno as ex_p_seqno"
        + " from rsk_acnolog A join act_acno B on B.acno_p_seqno=A.acno_p_seqno "
        + " where A.kind_flag = 'A' and A.log_type = '4' and A.log_mode = '2' "
        + " and A.log_not_reason ='' " + " and A.log_date = ? " + " and A.acct_type =? "
        + " order by B.acct_key Asc , B.acct_type Asc ";
    sqlSelect(wp, sql1, new Object[] {lsExecDate, lsAcctType});
    if (sqlRowNum <= 0) {
      return;
    }

    int llSelectCnt = sqlRowNum;
    int rr = 0;
    for (int ll = 0; ll < llSelectCnt; ll++) {
      rr++;
      String lsPseqno = wp.colStr(ll, "ex_p_seqno");
      if (empty(lsPseqno))
        continue;

      double lmAmt = selectCPurchaseAmt(lsPseqno);
      wp.colSet(ll, "ex_c_purchase_amt", lmAmt);
      lmAmt = selectAcctJrnlBal(lsPseqno);
      wp.colSet(ll, "ex_acct_jrnl_bal", lmAmt);
    }

    iiPdfRow = llSelectCnt;
    wp.listCount[1] = iiPdfRow;
  }

  double selectCPurchaseAmt(String lsPSeqno) {
    String sql1 = " select " + " sum(nvl(unbill_end_bal,0)) as c_purch_amt " + " from act_acct_sum "
        + " where p_seqno = ? " + " and acct_code in ('BL','CA','IT','ID','AO','CB','OT') ";
    sqlSelect(sql1, new Object[] {lsPSeqno});
    if (sqlRowNum > 0)
      return sqlNum("c_purch_amt");
    return 0;
  }

  double selectAcctJrnlBal(String lsPSeqno) {
    String sql1 = " select " + " acct_jrnl_bal " + " from act_acct " + " where p_seqno = ? ";
    sqlSelect(sql1, new Object[] {lsPSeqno});
    if (sqlRowNum > 0)
      return sqlNum("acct_jrnl_bal");
    return 0;
  }
}
