package colm05;
/** 符合解凍條件而不解凍明細表
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 * 2018/03/16				Alex			PDF列印修正
 * 109-05-06  V1.00.02  Tanwei       updated for project coding standard
 * */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Colr5860 extends BaseQuery implements InfacePdf {
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
    } else if (eqIgno(wp.buttonCode, "OK")) { // -PDF-
      updatePF3();
    }
    dddwSelect();
    initButton();

  }

  @Override
  public void initPage() {
    try {
      wp.colSet("ex_exec_mm", "3");
    } catch (Exception ex) {
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("5860") > 0) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

    if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("執行日期: 起迄錯誤");
      return;
    }

    wp.whereStr =
        " where param_type in ('2','4') " + sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
//            + sqlBetween("ex_date1", "ex_date2", "exec_date")
            + sqlCol(wp.itemStr("ex_date1"),"exec_date",">=")
            + sqlCol(wp.itemStr("ex_date2"),"exec_date","<=")
            ;

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " param_type , " + " acct_type , " + " valid_date , " + " exec_date , "
        + " print_flag4 , " + " hex(rowid) as rowid," + " t_acct_cnt , "
        + " t_blocknot1_cnt + t_blocknot2_cnt + t_blocknot4_cnt as blocknot_cnt  ";
    wp.daoTable = "rsk_blockexec";
    wp.whereOrder = " order by exec_date Desc , param_type Asc, acct_type Asc, valid_date Desc";

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
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {

    wp.reportId = "colr5860";
    dataPrint();
    wp.pageRows = 9999;
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "colr5860.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;
  }

  void dataPrint() {
    String wkAcctType = "", wkExecDate = "", wkValidDate = "";
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
    if (iiPdfRow <= 0) {
      alertPdfErr("查無資料可列印");
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

    wp.listCount[0] = iiPdfRow;
  }

  void selectLogAcno(String lsAcctType, String lsExecDate) {
    String lsAcctKey = "";
    lsAcctKey = wp.itemStr("ex_acct_key");
    lsAcctKey = commString.acctKey(lsAcctKey);

    daoTid = "wk-";
    String sql1 = " select " + " A.log_reason ," + " B.acct_key ," + " A.log_date ,"
        + " A.log_mode ," + " A.log_not_reason ," + " A.fit_cond ," + " B.line_of_credit_amt ,"
        + " C.block_status ," + " B.payment_rate1 ," + " B.payment_rate2 ," + " B.payment_rate3 ,"
        + " B.payment_rate4 ," + " B.payment_rate5 ," + " B.payment_rate6 ," + " B.vip_code ,"
        + " C.block_reason1 ," + " C.block_reason2 ," + " C.block_reason3 ," + " C.block_reason4 ,"
        + " C.block_reason5 ," + " 0 as db_prb_amt ," + " 0.00 as db_acct_jrnl_bal ,"
        + " '' as db_chg_date ," + " '' as db_msg ,"
        + " uf_acno_name(A.acno_p_seqno) as db_chi_name ," + " B.p_seqno , B.acno_p_seqno, "
        + " B.corp_p_seqno ," + " B.id_p_seqno "
        + " from rsk_acnolog A, act_acno B , cca_card_acct C "
        + " where A.acno_p_seqno = B.acno_p_seqno "
        + " and A.acno_p_seqno = C.acno_p_seqno and C.debit_flag<>'Y' " + " and A.log_date = ? "
        + " and A.kind_flag = 'A' and A.log_type = '4' and A.log_mode = '2' "
        + " and A.acct_type = ? " + " and A.log_not_reason in ('T1','T2','T4') "
        + sqlCol(lsAcctKey, "B.acct_key") + " order by 1 Asc , 2 Asc ";

    sqlSelect(sql1, new Object[] {lsExecDate, lsAcctType});
    if (sqlRowNum <= 0)
      return;

    int llSelectCnt = sqlRowNum;
    int rr = 0;
    for (int ll = 0; ll < llSelectCnt; ll++) {
      int ii = ll;

      setRowNum(ll, (ll+1));

      wp.colSet(ii, "ex_acct_key", sqlStr(ll, "wk-acct_key"));
      wp.colSet(ii, "ex_chi_name", sqlStr(ll, "wk-db_chi_name"));
      if (selectAcctJrnlBal(sqlStr(ll, "wk-p_seqno")) == false) {
        wp.colSet(ii, "ex_acct_jrnl_bal", "0");
      } else {
        wp.colSet(ii, "ex_acct_jrnl_bal", sqlNum("acct_jrnl_bal"));
      }
      wp.colSet(ii, "ex_line_of_credit_amt", sqlNum(ll, "wk-line_of_credit_amt"));
      wp.colSet(ii, "ex_payment_record",
          sqlStr(ll, "wk-payment_rate1") + "_" + sqlStr(ll, "wk-payment_rate2") + "_"
              + sqlStr(ll, "wk-payment_rate3") + "_" + sqlStr(ll, "wk-payment_rate4") + "_"
              + sqlStr(ll, "wk-payment_rate5") + "_" + sqlStr(ll, "wk-payment_rate6"));
      if (selectCPrbAmt(sqlStr(ll, "wk-p_seqno")) == false) {
        wp.colSet(ii, "ex_c_prb_amt", "0");
      } else {
        wp.colSet(ii, "ex_c_prb_amt", sqlNum("c_prb_amt"));
      }
      wp.colSet(ii, "ex_block_reason",
          sqlStr(ll, "wk-block_reason1") + " " + sqlStr(ll, "wk-block_reason2") + " "
              + sqlStr(ll, "wk-block_reason3") + " " + sqlStr(ll, "wk-block_reason4") + " "
              + sqlStr(ll, "wk-block_reason5"));
      wp.colSet(ii, "ex_log_reason", sqlStr(ll, "wk-log_reason"));

      String lsNotReason = sqlStr(ll, "wk-log_not_reason");
      if (eqIgno(lsNotReason, "T1")) {
        wp.colSet(ii, "ex_log_not_reason", lsNotReason + "_永不解凍帳戶數(不解凍)");
      } else if (eqIgno(lsNotReason, "T2")) {
        wp.colSet(ii, "ex_log_not_reason", lsNotReason + "_卡片未解凍");
      } else if (eqIgno(lsNotReason, "T4")) {
        wp.colSet(ii, "ex_log_not_reason", lsNotReason + "_有其他凍結原因帳戶數(不解凍)");
      }

      wp.colSet(ii, "ex_chg_date", sqlStr(ll, "wk-db_chg_date"));
      wp.colSet(ii, "ex_msg", sqlStr(ll, "wk-db_msg"));
    }

    iiPdfRow = llSelectCnt;
  }

  boolean selectCPrbAmt(String lsPSeqno) {
    String sql1 = " select " + " sum(prb_amount) as c_prb_amt " + " from rsk_problem "
        + " where card_no in (select card_no from crd_card where p_seqno=?) "
        + " and prb_status >='30' " + " and prb_status <'80' ";
    sqlSelect(sql1, new Object[] {lsPSeqno});
    if (sqlNum("c_prb_amt") == 0)
      return false;

    return true;
  }

  boolean selectAcctJrnlBal(String lsPSeqno) {
    String sql1 = " select acct_jrnl_bal " + " from act_acct " + " where p_seqno = ? ";
    sqlSelect(sql1, new Object[] {lsPSeqno});
    if (sqlNum("acct_jrnl_bal") == 0)
      return false;
    return true;

  }

  void updatePF3() throws Exception {
    String lsOpt = wp.itemStr("opt");
    wp.listCount[0] = wp.itemRows("rowid");
    int rr = optToIndex(lsOpt);
    if (rr < 0) {
      alertErr2("未點選列印資料");
      return;
    }
    String lsRowid = wp.itemStr(rr, "rowid");

    String sql1 = "update RSK_BLOCKEXEC set " + " print_flag4 = 'Y', "
        + commSqlStr.setModxxx(wp.loginUser, wp.modPgm()) + " where rowid =? ";
    setRowid(1, lsRowid);

    sqlExec(sql1);
    if (sqlRowNum > 0) {
      dbCommit();
    } else {
      dbRollback();
      alertErr2("update rsk_blockexec error");
      return;
    }

    queryFunc();
  }
}
