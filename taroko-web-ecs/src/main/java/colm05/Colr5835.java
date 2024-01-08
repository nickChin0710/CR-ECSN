package colm05;
/** 整批凍結戶但有其他帳戶類別未凍結者明細表
 * 2019-1210   JH    UAT++
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * V.2018-0807.jh
 * 109-05-06  V1.00.03  Tanwei       updated for project coding standard
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Colr5835 extends BaseQuery implements InfacePdf {
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
      if (wp.respHtml.indexOf("5835") > 0) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }


  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_exec_date1"))) {
      alertErr2("執行日期 (起) : 不可空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_exec_date1"), wp.itemStr("ex_exec_date2")) == false) {
      alertErr2("執行日期起迄：輸入錯誤");
      return;
    }


    wp.whereStr =
        " where param_type in ('1','3') " + sqlCol(wp.itemStr("ex_exec_date1"), "exec_date", ">=")
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
        + " print_flag , " + " t_acct_cnt , "
        + " t_acct_cnt - t_blocknot1_cnt - t_blocknot2_cnt - t_blocknot3_cnt - t_blocknot4_cnt as tt_block_cnt ,  "
        + " t_blocknot1_cnt + t_blocknot2_cnt + t_blocknot3_cnt + t_blocknot4_cnt as tt_blocknot_cnt ";
    wp.daoTable = "rsk_blockexec";
    wp.whereOrder = " order by exec_date Desc, param_type Asc, acct_type Asc, valid_date Desc";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.totalRows = wp.dataCnt;
    wp.setPageValue();
    queryAfter();
  }

  void queryAfter() throws Exception {
    String sql1 = " select count(*) as unblock_cnt" + " from rsk_acnolog A join cca_card_acct B"
        + " on A.id_p_seqno=B.id_p_seqno and B.debit_flag<>'Y'"
        + " where A.acno_p_seqno <> B.acno_p_seqno " + " and A.log_date = ? "
        + " and A.kind_flag ='A' and A.log_type ='3' and A.log_mode ='2' "
        + " and A.acct_type = ? and A.id_p_seqno<>'' " + " and A.log_not_reason ='' "
        + " and B.block_reason1 ='' ";

    for (int ll = 0; ll < wp.listCount[0]; ll++) {
      String lsExecDate = wp.colStr(ll, "exec_date");
      String lsAcctType = wp.colStr(ll, "acct_type");
      double llCnt = getNumber(wp.conn(), sql1, lsExecDate, lsAcctType); // getNumber(sql1,ls_exec_date,ls_acct_type);

      wp.colSet(ll, "wk_blocknot_cnt", llCnt);
    }
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
    wp.reportId = "colr5835";
    dataPrint();
    if (rc != 1 || ilPdfRow == 0)
      return;

    wp.pageRows = 9999;
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "colr5835.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  void dataPrint() {
    ilPdfRow = 0;

    String exValidDate = "", exExecDate = "", exAcctType = "";
    int rr = optToIndex(wp.itemStr("opt"));
    if (rr < 0) {
      alertPdfErr("未點選列印資料");
      return;
    }
    String lsAcctType = wp.itemStr(rr, "acct_type");
    String lsExecDate = wp.itemStr(rr, "exec_date");
    String lsValidDate = wp.itemStr(rr, "valid_date");

    selectLogAcno(lsAcctType, lsExecDate);
    if (ilPdfRow <= 0) {
      alertPdfErr("無資料可列印");
      return;
    }

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
    String sql1 = " select " + " uf_acno_key(A.acno_p_seqno) as acct_key , " + " A.acct_type , "
        + " B.block_status , " + " A.log_date , " + " A.log_mode , " + " A.log_reason , "
        + " A.log_not_reason , " + " uf_acno_name(B.acno_p_seqno) as db_chi_name , "
        + " B.acct_type as acct_type_2," + " B.block_reason1 , " + " B.block_reason2 , "
        + " B.block_reason3 , " + " B.block_reason4 , " + " B.block_reason5 , " + " A.mod_pgm  "
        + " from rsk_acnolog A join cca_card_acct B"
        + " on A.id_p_seqno=B.id_p_seqno and B.debit_flag<>'Y'"
        + " where A.acno_p_seqno <> B.acno_p_seqno " + " and A.log_date = ? "
        + " and A.kind_flag ='A' and A.log_type ='3' and A.log_mode ='2' "
        + " and A.acct_type = ? and A.id_p_seqno<>'' " + " and A.log_not_reason ='' "
        + " and B.block_reason1 ='' " + " order by 1 Asc, 2 Asc ";
    sqlSelect(sql1, new Object[] {lsExecDate, lsAcctType});

    if (sqlRowNum <= 0) {
      return;
    }

    int llSelectCnt = sqlRowNum;
    int rr = -1;
    for (int ll = 0; ll < llSelectCnt; ll++) {
      rr++;
      this.setSerNum(rr, (ll + 1));

      wp.colSet(rr, "ex_acct_key", sqlStr(ll, "acct_type") + "-" + sqlStr(ll, "acct_key"));
      wp.colSet(rr, "ex_chi_name", sqlStr(ll, "db_chi_name"));
      wp.colSet(rr, "ex_acct_type", sqlStr(ll, "acct_type_2"));
      // wp.col_set(rr,"ex_card_no", sql_ss(ll,"card_no"));
      wp.colSet(rr, "ex_block_reason",
          sqlStr(ll, "block_reason") + "," + sqlStr(ll, "block_reason2") + ","
              + sqlStr(ll, "block_reason3") + "," + sqlStr(ll, "block_reason4") + ","
              + sqlStr(ll, "block_reason5"));
      wp.colSet(rr, "ex_mod_pgm", sqlStr(ll, "mod_pgm"));
      wp.colSet(rr, "ex_log_reason", sqlStr(ll, "log_reason"));
    }

    ilPdfRow += llSelectCnt;
    wp.listCount[0] = ilPdfRow;
  }

}
