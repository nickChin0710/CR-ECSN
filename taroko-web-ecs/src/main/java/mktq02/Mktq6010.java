/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-09-21  V1.00.00  Andy       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *                                                                      *	
******************************************************************************/

package mktq02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Mktq6010 extends BaseEdit {
  String mBatchno1 = "", mBatchno2 = "", mDataCode = "";
  String mProgName = "mktq6010";
  String reportTitle = "", reportSubtitle = "", reportSubtitle2 = "", reportSubtitle3 = "",
      reportSubtitle4 = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
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
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    String exDate1 = wp.itemStr("ex_date1");
    String exDate2 = wp.itemStr("ex_date2");
    String exGiftno = wp.itemStr("ex_giftno");
    wp.sqlCmd = "SELECT " + "a.acct_month, " + "a.program_code, " + "b.fund_name, "
        + "count (c.dest_amt) feeback_cnt, " + "sum (c.dest_amt) destination_amt, "
        + "sum (a.fund_amt) feedback_amt "
        + "FROM cyc_cobrand_fund a LEFT JOIN ptr_fundp b ON a.program_code = b.fund_code "
        + "LEFT JOIN cyc_cal_fund c ON a.p_seqno = c.p_seqno " + "AND a.program_code = c.fund_code "
        + "AND a.acct_month = c.acct_month " + "WHERE 1 = 1 " + "AND c.proc_mark = 'Y' ";
    wp.sqlCmd += sqlStrend(exDate1, exDate2, "a.acct_month");
    wp.sqlCmd += sqlCol(exGiftno, "a.program_code", "like%");
    wp.sqlCmd += " GROUP BY a.acct_month, a.program_code, b.fund_name " + "UNION ALL " + "SELECT "
        + "acct_month, " + "program_code, " + "bill_desc AS fund_name, " + "feeback_cnt, "
        + "destination_amt, " + "feedback_amt " + "FROM ( " + "SELECT " + "a.acct_month, "
        + "a.program_code, " + "b.bill_desc, " + "count (a.destination_cnt) feeback_cnt, "
        + "sum (a.destination_cnt) destination_amt, " + "sum (c.beg_tran_amt) feedback_amt "
        + "FROM cyc_card_data a, ptr_payment b, mkt_cashback_dtl c "
        + "WHERE a.program_code = b.payment_type " + "AND a.id_p_seqno = c.id_p_seqno "
        + "AND a.program_code = c.fund_code " + "AND a.proc_date = c.tran_date "
        + "AND a.proc_flag = 'Y' " + "AND c.tran_code = '1' "
        + "AND a.program_code in ('0815','0817','0819')";
    wp.sqlCmd += sqlStrend(exDate1, exDate2, "a.acct_month");
    wp.sqlCmd += sqlCol(exGiftno, "a.program_code", "like%");
    wp.sqlCmd += " GROUP BY a.acct_month, a.program_code, b.bill_desc " + ") "
        + " order by acct_month, program_code, fund_name ";
    // System.out.println(wp.sqlCmd);
    wp.colSet("sql_select", wp.sqlCmd);
    // 重新計算頁面筆數
    wp.pageCountSql = "select count(*) from (";
    wp.pageCountSql += wp.sqlCmd;
    wp.pageCountSql += ")";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.setPageValue();
    listWkdata();

  }

  void listWkdata() throws Exception {
    int rowCt = 0;
    int scnt = wp.selectCnt;
    double wkDestAmt = 0, wkFeedbackAmt = 0;
    double sumDestAmt = 0, sumFeedbackAmt = 0, sumFeedbackAmt1 = 0;

    for (int ii = 0; ii < scnt; ii++) {
      // 計算欄位
      rowCt += 1;
      wkDestAmt = wp.colNum(ii, "destination_amt");
      sumDestAmt += wkDestAmt;
      wkFeedbackAmt = wp.colNum(ii, "feedback_amt");
      sumFeedbackAmt += wkFeedbackAmt;
    }
    wp.colSet("sum_dest_amt", numToStr(sumDestAmt, "###"));
    wp.colSet("sum_feedback_amt", numToStr(sumFeedbackAmt, "###"));
    sumFeedbackAmt1 = sumFeedbackAmt * 0.95;
    wp.colSet("sum_feedback_amt1", numToStr(sumFeedbackAmt1, "###"));

  }

  @Override
  public void querySelect() throws Exception {
    // m_batchno =wp.item_ss("batchno");
    // m_to_nccc_date =wp.item_ss("to_nccc_date");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void saveFunc() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      // this.dddw_list("dddw_ac_no", "gen_digest", "ac_no", "", "where
      // 1=1 group by ac_no order by ac_no");
      // this.dddw_list("dddw_group_abbr_code", "ptr_group_code",
      // "group_abbr_code", "", "where 1=1 group by group_abbr_code order
      // by group_abbr_code");
      // 提供Detel頁下拉指標到...
      // wp.optionKey = wp.col_ss("memo3_kind");
      // this.dddw_list("dddw_bill_form", "cyc_bill_form", "bill_form",
      // "bill_form_name", "where 1=1 order by bill_form");
    } catch (Exception ex) {
    }
  }

}
