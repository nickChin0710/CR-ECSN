package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  109-01-04  V1.00.01   shiyuqi       修改无意义命名
*  110-01-15  V1.00.02   Justin          fix  a query bug                                                                                         *  
*/

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar0190 extends BaseAction implements InfacePdf {
  String lsPSeqno = "", lsDebitFlag = "";

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

    if (wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_card_no")) {
      alertErr2("比對日期 , 卡號 不可皆為空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("比對日期起迄：輸入錯誤");
      return;
    }
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "u_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "u_date", "<=")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no")
        + sqlCol(wp.itemStr("ex_amt"), "amt_nt", ">=");

    if (wp.itemEq("ex_trans_type", "1")) {
      lsWhere += " and trans_type <'50' ";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
        " " + " card_no , " + " mcht_no , " + " amt_nt , " + " auth_no , " + " tx_date , "
            + " mcc_code , " + " trans_type , " + " proc_code , " + " bit127_rec_data , "
            + " message_head5 , " + " message_head6 , " + " u_date , " + " u_time , " + " ref_no , "
            + " auth_amt , " + " '' as d_latest_1_mnth , " + " '' as d_latest_2_mnth , "
            + " '' as d_latest_3_mnth , " + " auth_date , " + " 0 as d_limit_amt  "

    ;
    wp.daoTable = "cca_unmatch";
    wp.whereOrder = "  order by amt_nt Desc, card_no, tx_date ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }

    logSql();
    pageQuery();
    queryAfter();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {

      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();


  }

  int isDebitCard(String aCardNo) {

    if (aCardNo.length() < 6)
      return -1;

    String sql1 = "select debit_flag from ptr_bintable" + " where 1=1"
        + sqlCol(aCardNo.substring(0, 6), "bin_no");
    sqlSelect(sql1);

    if (eqAny(sqlStr("debit_flag"), "Y"))
      return 1;
    if (eqAny(sqlStr("debit_flag"), "N"))
      return 2;

    return 0;
  }

  void queryAfter() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (isDebitCard(wp.colStr(ii, "card_no")) == 1) {
        selectDbcCard(wp.colStr(ii, "card_no"));
        if (empty(lsPSeqno))
          continue;
        selectDbaAcno(lsPSeqno, ii);
      } else if (isDebitCard(wp.colStr(ii, "card_no")) == 2) {
        selectCrdCard(wp.colStr(ii, "card_no"));
        if (empty(lsPSeqno))
          continue;
        selectActAcno(lsPSeqno, ii);
      } else if (isDebitCard(wp.colStr(ii, "card_no")) == -1) {
        continue;
      }
    }
  }

  void selectCrdCard(String aCardNo) {
    String sql1 = " select" + " acno_p_seqno " + " from crd_card " + " where card_no = ? ";
    sqlSelect(sql1, new Object[] {aCardNo});

    if (sqlRowNum <= 0) {
      return;
    }

    lsPSeqno = sqlStr("acno_p_seqno");

  }

  void selectDbcCard(String aCardNo) {
    String sql1 = " select" + " p_seqno " + " from dbc_card " + " where card_no = ? ";
    sqlSelect(sql1, new Object[] {aCardNo});

    if (sqlRowNum <= 0) {
      return;
    }

    lsPSeqno = sqlStr("p_seqno");

  }

  void selectDbaAcno(String pSeqno, int ii) {
    String sql1 = "select payment_rate1 as d_latest_1_mnth , "
        + " payment_rate2 as d_latest_2_mnth , " + " payment_rate3 as d_latest_3_mnth , "
        + " line_of_credit_amt as d_limit_amt " + " from dba_acno " + " where p_seqno =?";
    sqlSelect(sql1, new Object[] {pSeqno});
    wp.colSet(ii, "d_latest_1_mnth", sqlStr("d_latest_1_mnth"));
    wp.colSet(ii, "d_latest_2_mnth", sqlStr("d_latest_2_mnth"));
    wp.colSet(ii, "d_latest_3_mnth", sqlStr("d_latest_3_mnth"));
    wp.colSet(ii, "d_limit_amt", sqlStr("d_limit_amt"));
    wp.colSet(ii, "ls_dl", sqlStr("d_latest_1_mnth") + "、" + sqlStr("d_latest_2_mnth") + "、"
        + sqlStr("d_latest_3_mnth"));
  }

  void selectActAcno(String pSeqno, int ii) {
    String sql1 = "select payment_rate1 as d_latest_1_mnth , "
        + " payment_rate2 as d_latest_2_mnth , " + " payment_rate3 as d_latest_3_mnth , "
        + " line_of_credit_amt as d_limit_amt " + " from act_acno " + " where acno_p_seqno =?";
    sqlSelect(sql1, new Object[] {pSeqno});
    wp.colSet(ii, "d_latest_1_mnth", sqlStr("d_latest_1_mnth"));
    wp.colSet(ii, "d_latest_2_mnth", sqlStr("d_latest_2_mnth"));
    wp.colSet(ii, "d_latest_3_mnth", sqlStr("d_latest_3_mnth"));
    wp.colSet(ii, "d_limit_amt", sqlStr("d_limit_amt"));
    wp.colSet(ii, "ls_dl", sqlStr("d_latest_1_mnth") + "、" + sqlStr("d_latest_2_mnth") + "、"
        + sqlStr("d_latest_3_mnth"));
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
    wp.reportId = "Ccar0190";

    String cond1 = "比對日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2")) + " 金額 :" + wp.itemStr("ex_amt") + " 以上";
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 9999;
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ccar0190.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

}
