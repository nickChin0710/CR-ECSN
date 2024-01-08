package colm05;
/**
 * 整批解凍戶但有其他帳戶類別仍凍結者明細表
 * 2019-0726   JH    sqlSelect().
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 2018-0808:	JH		bugfix
 * 2018-0316:	Alex	PDF列印修正
 * 109-05-06  V1.00.04  Tanwei       updated for project coding standard
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;
import taroko.com.TarokoPDFLine;

public class Colr5845 extends BaseQuery implements InfacePdf {
  int ilPdfRow = 0;

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
      if (wp.respHtml.indexOf("5845") > 0) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_exec_date1"))) {
      alertErr2("執行日期起  : 不可空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_exec_date1"), wp.itemStr("ex_exec_date2")) == false) {
      alertErr2("執行日期起迄: 輸入錯誤");
      return;
    }

    wp.whereStr =
        " where param_type in ('2','4') " + sqlCol(wp.itemStr("ex_exec_date1"), "exec_date", ">=")
            + sqlCol(wp.itemStr("ex_exec_date2"), "exec_date", "<=")
            + sqlCol(wp.itemStr("ex_acct_type"), "acct_type");


    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " param_type , " + " acct_type , " + " valid_date , " + " exec_date , "
        + " exec_mode , " + " t_acct_cnt ,"
        + " t_acct_cnt - t_blocknot1_cnt - t_blocknot2_cnt - t_blocknot3_cnt - t_blocknot4_cnt as tt_block_cnt ,  "
        + " t_blocknot1_cnt + t_blocknot2_cnt + t_blocknot3_cnt + t_blocknot4_cnt as tt_blocknot_cnt ";
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
    ilPdfRow = 0;
    wp.reportId = "colr5845";
    dataPrint();
    if (rc != 1 || ilPdfRow == 0)
      return;

    wp.pageRows = 9999;
    TarokoPDFLine pdf = new TarokoPDFLine();
    wp.fileMode = "Y";
    pdf.excelTemplate = "colr5845.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.wpIndx = 1;
    // pdf.setListIndex(2);
    pdf.procesPDFreport(wp);
    pdf = null;

    return;
  }

  void dataPrint() {
    String exValidDate = "", exExecDate = "", exAcctType = "";
    String lsOpt = wp.itemStr("opt");
    int rr = optToIndex(lsOpt);
    if (rr < 0) {
      alertPdfErr("未點選列印資料");
      return;
    }
    String lsAcctType = wp.itemStr(rr, "acct_type");
    String lsExecDate = wp.itemStr(rr, "exec_date");
    String lsValidDate = wp.itemStr(rr, "valid_date");

    selectLogAcno(lsAcctType, lsExecDate);
    if (ilPdfRow == 0)
      return;

    exValidDate = lsValidDate;
    exExecDate = lsExecDate;
    exAcctType = lsAcctType;

    String cond1 = "";
    cond1 = "帳戶類別 : " + exAcctType + " " + "生效日期 : " + commString.strToYmd(exValidDate) + " "
        + "執行日期 : " + commString.strToYmd(exExecDate);
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
  }

  void selectLogAcno(String lsAcctType, String lsExecDate) {
    String sql1 = " select row_number()over() as ser_num,"
        // + " uf_acno_key(A.acno_p_seqno) as acct_key , "
        + " A.acct_type||'-'||uf_acno_key(A.acno_p_seqno) as ex_acct_key,"
        + " uf_acno_name(A.acno_p_seqno) as ex_chi_name ," + " B.acct_type as wk_acct_type,"
        + " A.log_reason as ex_log_reason," + " B.acct_type as acct_type_2, "
        + " B.block_reason1||','||B.block_reason2" + "||','||B.block_reason3||','||B.block_reason4"
        + "||','||B.block_reason5 as ex_block_reason," + " A.mod_pgm as ex_mod_pgm "
        + " from rsk_acnolog A join cca_card_acct B"
        + " on B.id_p_seqno=A.id_p_seqno and B.debit_flag<>'Y'"
        + " where B.acno_p_seqno <>A.acno_p_seqno" + " and A.log_date = ? "
        + " and A.kind_flag ='A' " + " and A.log_mode ='2' " + " and A.log_type ='4' " // -解凍-
        + " and A.log_not_reason ='' " + " and A.acct_type = ? and A.id_p_seqno<>''"
        + " and B.block_reason1 <>'' " // -仍凍結-
        + " order by 1 Asc ";
    sqlSelect(wp, sql1, new Object[] {lsExecDate, lsAcctType});
    if (sqlRowNum <= 0) {
      alertPdfErr("無資料可列印");
      return;
    }

    int llSelectCnt = sqlRowNum;
    // int rr = -1;
    // for (int ii = 0; ii < ll_select_cnt; ii++) {
    // rr++;
    // this.set_rowNum(ii, rr);
    //
    // wp.col_set(rr, "ex_acct_key", sql_ss(ii, "acct_type") + "-" + sql_ss(ii, "acct_key"));
    // wp.col_set(rr, "ex_chi_name", sql_ss(ii, "db_chi_name"));
    // wp.col_set(rr, "ex_acct_type", sql_ss(ii, "acct_type_2"));
    // wp.col_set(rr, "ex_block_reason", sql_ss(ii, "block_reason")
    // + "," + sql_ss(ii, "block_reason2") + "," + sql_ss(ii, "block_reason3")
    // + "," + sql_ss(ii, "block_reason4") + "," + sql_ss(ii, "block_reason5"));
    // wp.col_set(rr, "ex_mod_pgm", sql_ss(ii, "mod_pgm"));
    // wp.col_set(rr, "ex_log_reason", sql_ss(ii, "log_reason"));
    // }

    ilPdfRow += llSelectCnt;
    wp.listCount[1] = ilPdfRow;
  }

}
