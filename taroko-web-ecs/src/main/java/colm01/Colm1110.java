/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109-05-06  V1.00.00  Aoyulan       updated for project coding standard     *
** 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*  112-10-23  V1.00.02  sunny         調整錯誤訊息文字
******************************************************************************/
package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Colm1110 extends BaseEdit {
  CommString commString = new CommString();
  Colm1110Func func;

  String kkOptname = "";
  String kkRowid = "";
  String kkLiacSeqno = "";
  String mProgName = "colm1110";
  int totalCnt = 0;
  int breakpoint = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "X";
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
      wp.colSet("queryReadCnt", "0");
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Batch")) {
      // -執行批次程式-
      fCallBatch("ColA415");
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    // wp.col_set("btnUpdate2", "disabled");
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        // wp.initOption = "--";
        // wp.optionKey = wp.col_ss("kk_acct_type");
      } else {
        wp.initOption = "--";
        wp.optionKey = wp.itemStr("exCrtUser");
        dddwList("SecUserIDNameList", "sec_user", "usr_id", "usr_id||' ['||usr_cname||']'",
            "where usr_type = '4' order by usr_id");
      }
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {
    if (empty(wp.itemStr("exId")) && empty(wp.itemStr("exCrtUser"))
        && empty(wp.itemStr("exApplyDateS")) && empty(wp.itemStr("exApplyDateE"))
        && eqIgno(wp.itemStr("exRiskMark"), "0") && eqIgno(wp.itemStr("exNosendFlag"), "0")
        && empty(wp.itemStr("exProcDateS")) && empty(wp.itemStr("exProcDateE"))
        && eqIgno(wp.itemStr("exAprFlag"), "0") && empty(wp.itemStr("exSendFlag"))
        && empty(wp.itemStr("exProcChFlag"))) {
      alertErr2("至少輸入一個查詢條件!");
      return false;
    }

    String lsDate1 = wp.itemStr("exApplyDateS");
    String lsDate2 = wp.itemStr("exApplyDateE");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[協商申請日期-起迄]  輸入錯誤");
      return false;
    }

    String lsDate3 = wp.itemStr("exProcDateS");
    String lsDate4 = wp.itemStr("exProcDateE");
    if (this.chkStrend(lsDate3, lsDate4) == false) {
      alertErr2("[回報日期-起迄]  輸入錯誤");
      return false;
    }

    return true;
  }

  String getWhereStrA() throws Exception {
    String whereStr = "";

    // 身分證字號
    if (empty(wp.itemStr("exId")) == false) {
      whereStr +=
          "and col_liac_debt.id_p_seqno in (Select id_p_seqno From crd_idno Where id_no Like :id_no) ";
      setString("id_no", wp.itemStr("exId") + "%");
    }
    // 協商申請日
    if (empty(wp.itemStr("exApplyDateS")) == false) {
      whereStr += " and col_liac_debt.apply_date >= :apply_dates ";
      setString("apply_dates", wp.itemStr("exApplyDateS"));
    }
    if (empty(wp.itemStr("exApplyDateE")) == false) {
      whereStr += " and col_liac_debt.apply_date <= :apply_datee ";
      setString("apply_datee", wp.itemStr("exApplyDateE"));
    }
    // 處理狀態
    if (!wp.itemStr("exSendFlag").equals("")) {
      whereStr += "and col_liac_debt.proc_flag = :proc_flag ";
      setString("proc_flag", wp.itemStr("exSendFlag"));
    }
    // 回報日期
    if (empty(wp.itemStr("exProcDateS")) == false) {
      whereStr += " and col_liac_debt.report_date >= :report_dates ";
      setString("report_dates", wp.itemStr("exProcDateS"));
    }
    if (empty(wp.itemStr("exProcDateE")) == false) {
      whereStr += " and col_liac_debt.report_date <= :report_datee ";
      setString("report_datee", wp.itemStr("exProcDateE"));
    }
    // 有保人
    if (wp.itemStr("exHasRela").equals("Y")) {
      whereStr += " and col_liac_debt.has_rela_flag ='Y' ";
    } else if (wp.itemStr("exHasRela").equals("N")) {
      whereStr += " and col_liac_debt.has_rela_flag <>'Y' ";
    }
    // 經辦代號
    if (empty(wp.itemStr("exCrtUser")) == false) {
      whereStr += " and col_liac_debt.crt_user = :crt_user ";
      setString("crt_user", wp.itemStr("exCrtUser"));
    }
    // 有道德風險
    if (wp.itemStr("exRiskMark").equals("Y")) {
      whereStr += " and col_liac_debt.ethic_risk_mark <>'' ";
    } else if (wp.itemStr("exRiskMark").equals("N")) {
      whereStr += " and col_liac_debt.ethic_risk_mark = '' ";
    }
    // 暫不報送
    if (wp.itemStr("exNosendFlag").equals("Y")) {
      whereStr += " and col_liac_debt.not_send_flag ='Y' ";
    } else if (wp.itemStr("exNosendFlag").equals("N")) {
      whereStr += " and col_liac_debt.not_send_flag <>'Y' ";
    }
    // 主管覆核
    if (eqIgno(wp.itemStr("exAprFlag"), "0") == false) {
      whereStr += "and col_liac_debt.apr_flag = :apr_flag ";
      setString("apr_flag", wp.itemStr("exAprFlag"));
    }
    // 有附卡
    if (wp.itemStr("exHasSup").equals("Y")) {
      whereStr += " and col_liac_debt.has_sup_flag ='Y' ";
    } else if (wp.itemStr("exHasSup").equals("N")) {
      whereStr += " and col_liac_debt.has_sup_flag <>'Y' ";
    }

    return whereStr;
  }

  String getWhereStrB() throws Exception {
    String whereStr = "";

    // 身分證字號
    if (empty(wp.itemStr("exId")) == false) {
      whereStr +=
          "and col_liac_debt_ch.id_p_seqno in (Select id_p_seqno From crd_idno Where id_no Like :id_no) ";
      setString("id_no", wp.itemStr("exId") + "%");
    }
    // 協商申請日
    if (empty(wp.itemStr("exApplyDateS")) == false) {
      whereStr += " and col_liac_debt_ch.apply_date >= :apply_dates ";
      setString("apply_dates", wp.itemStr("exApplyDateS"));
    }
    if (empty(wp.itemStr("exApplyDateE")) == false) {
      whereStr += " and col_liac_debt_ch.apply_date <= :apply_datee ";
      setString("apply_datee", wp.itemStr("exApplyDateE"));
    }
    // 有保人
    if (wp.itemStr("exHasRela").equals("Y")) {
      whereStr += " and col_liac_debt_ch.has_rela_flag ='Y' ";
    } else if (wp.itemStr("exHasRela").equals("N")) {
      whereStr += " and col_liac_debt_ch.has_rela_flag <>'Y' ";
    }
    // 經辦代號
    if (empty(wp.itemStr("exCrtUser")) == false) {
      whereStr += " and col_liac_debt_ch.crt_user = :crt_user ";
      setString("crt_user", wp.itemStr("exCrtUser"));
    }
    // 主管覆核
    if (wp.itemStr("exAprFlag").equals("Y")) {
      whereStr += " and col_liac_debt_ch.apr_flag ='Y' ";
    } else if (wp.itemStr("exAprFlag").equals("N")) {
      whereStr += " and col_liac_debt_ch.apr_flag <>'Y' ";
    }
    // 變更方案回報債權處理狀態
    if (!wp.itemStr("exProcChFlag").equals("")) {
      whereStr += "and col_liac_debt_ch.proc_flag = :proc_flag ";
      setString("proc_flag", wp.itemStr("exProcChFlag"));
    }

    return whereStr;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();// x
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();// x
    if (getWhereStr() == false)
      return;

    queryReadA();
    queryReadB();
  }

  void queryReadA() throws Exception {
    daoTid = "A-";

    wp.selectSQL = " col_liac_debt.liac_seqno " + " ,col_liac_debt.id_p_seqno "
        + " ,col_liac_debt.id_no " + " ,col_liac_debt.apply_date "
        + " ,col_liac_debt.interest_base_date " + " ,col_liac_debt.bank_code "
        + " ,col_liac_debt.bank_name " + " ,col_liac_debt.auth_uncap_amt "
        + " ,col_liac_debt.in_end_bal " + " ,col_liac_debt.out_end_bal "
        + " ,col_liac_debt.lastest_pay_amt " + " ,col_liac_debt.in_end_bal_new "
        + " ,col_liac_debt.out_end_bal_new " + " ,col_liac_debt.lastest_pay_amt_new "
        + " ,col_liac_debt.crt_user " + " ,col_liac_debt.crt_date " + " ,col_liac_debt.apr_flag "
        + " ,col_liac_debt.apr_date " + " ,col_liac_debt.apr_user " + " ,col_liac_debt.mod_seqno "
        + " ,col_liac_debt.ethic_risk_mark " + " ,col_liac_debt.chi_name "
        + " ,col_liac_debt.proc_flag " + " ,col_liac_debt.proc_date " + " ,col_liac_debt.from_type "
        + " ,col_liac_debt.acct_status_flag " + " ,col_liac_debt.not_send_flag "
        + " ,col_liac_debt.has_rela_flag " + " ,col_liac_debt.has_sup_flag "
        + " ,col_liac_debt.debt_remark " + " ,hex(col_liac_debt.rowid) as rowid "
        // + " ,nvl (col_liac_debt.proc_flag, '0') db_proc "
        // + " ,nvl (col_liac_debt.from_type, '2') db_from "
        + " ,col_liac_nego.liac_status "
        // + " ,nvl (col_liac_debt.not_send_flag, 'x') db_send_flag "
        + " ,col_liac_debt.report_date " + " ,col_liac_debt.no_calc_flag "
        + " ,col_liac_debt.no_include_flag " + " ,col_liac_debt.paper_report_flag "
        + " ,col_liac_debt.out_capital " + " ,col_liac_debt.out_interest "
        + " ,col_liac_debt.out_fee " + " ,col_liac_debt.out_pn "
        + " ,col_liac_debt.out_capital_new " + " ,col_liac_debt.out_interest_new "
        + " ,col_liac_debt.out_fee_new " + " ,col_liac_debt.out_pn_new ";

    wp.daoTable = " col_liac_debt, col_liac_nego ";
    wp.whereOrder = " ";
    wp.whereStr = " where (col_liac_debt.liac_seqno = col_liac_nego.liac_seqno) ";
    wp.whereStr += getWhereStrA();

    pageQuery();
    wp.setListCount(1);
    totalCnt = wp.selectCnt;

    listWkdataA();
  }

  void queryReadB() throws Exception {
    daoTid = "B-";

    wp.selectSQL = " col_liac_debt_ch.liac_seqno " + " ,col_liac_debt_ch.id_p_seqno "
        + " ,col_liac_debt_ch.id_no " + " ,col_liac_debt_ch.chi_name "
        + " ,col_liac_debt_ch.jcic_notify_date " + " ,col_liac_debt_ch.apply_date "
        + " ,col_liac_debt_ch.bank_code " + " ,col_liac_debt_ch.bank_name "
        + " ,col_liac_debt_ch.clearing_ym " + " ,col_liac_debt_ch.apply_change_date "
        + " ,col_liac_debt_ch.remark " + " ,col_liac_debt_ch.change_cnt "
        + " ,col_liac_debt_ch.agree_flag " + " ,col_liac_debt_ch.receipt_amt "
        + " ,col_liac_debt_ch.card_debt " + " ,col_liac_debt_ch.cash_card_debt "
        + " ,col_liac_debt_ch.payment_cnt " + " ,col_liac_debt_ch.has_rela_flag "
        + " ,col_liac_debt_ch.rela_consent_flag " + " ,col_liac_debt_ch.proc_flag "
        + " ,col_liac_debt_ch.proc_date " + " ,col_liac_debt_ch.crt_user "
        + " ,col_liac_debt_ch.crt_date " + " ,col_liac_debt_ch.apr_flag "
        + " ,col_liac_debt_ch.apr_date " + " ,col_liac_debt_ch.apr_user "
        + " ,col_liac_nego.liac_status " + " ,hex(col_liac_debt_ch.rowid) as rowid ";

    wp.daoTable = " col_liac_debt_ch, col_liac_nego ";
    wp.whereOrder = " ";
    wp.whereStr = " where (col_liac_debt_ch.liac_seqno = col_liac_nego.liac_seqno) ";
    wp.whereStr += getWhereStrB();

    pageQuery();
    wp.setListCount(2);
    totalCnt += wp.selectCnt;
    wp.notFound = "N";
    if (totalCnt == 0) {
      wp.notFound = "Y";
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdataB();
  }

  void listWkdataA() throws Exception {
    String wkdata = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkdata = wp.colStr(ii, "a-liac_status");
      wp.colSet(ii, "a-tt_liac_status",
          commString.decode(wkdata, ",1,2,3,4,5", ",1.受理申請,2.停催通知,3.簽約完成,4.結案/復催,5.結案/結清"));

      wkdata = wp.colStr(ii, "a-proc_flag");
      wp.colSet(ii, "a-tt_proc_flag",
          commString.decode(wkdata, ",0,1,2,A,R", ",0.資料轉入,1.待報送,2.已報送,A.不須報送,R.待處理"));

      wkdata = wp.colStr(ii, "a-acct_status_flag");
      wp.colSet(ii, "a-tt_acct_status_flag", commString.decode(wkdata, ",Y,N", ",Y:呆帳戶,N:非呆帳戶"));

      wkdata = wp.colStr(ii, "a-apr_user");
      wp.colSet(ii, "a-tt_apr_user", wfSecUserName(wkdata));

      wp.colSet(ii, "a-wk_id_cname", wp.colStr(ii, "a-id_no") + "_" + wp.colStr(ii, "a-chi_name"));
      wp.colSet(ii, "a-wk_bank_code_name",
          wp.colStr(ii, "a-bank_code") + " " + wp.colStr(ii, "a-bank_name"));
    }
  }

  void listWkdataB() throws Exception {
    String wkdata = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkdata = wp.colStr(ii, "b-liac_status");
      wp.colSet(ii, "b-tt_liac_status",
          commString.decode(wkdata, ",1,2,3,4,5", ",1.受理申請,2.停催通知,3.簽約完成,4.結案/復催,5.結案/結清"));

      wkdata = wp.colStr(ii, "b-rela_consent_flag");
      wp.colSet(ii, "b-tt_rela_consent_flag",
          commString.decode(wkdata, ",1,2", ",1.是，且已徵提保證人同意書,2.是，未徵提保證人同意書"));

      wkdata = wp.colStr(ii, "b-proc_flag");
      wp.colSet(ii, "b-tt_proc_flag", commString.decode(wkdata, ",0,1", ",0.待通知,1.已通知"));

      wkdata = wp.colStr(ii, "b-apr_user");
      wp.colSet(ii, "b-tt_apr_user", wfSecUserName(wkdata));
    }
  }

  String wfSecUserName(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select usr_cname from sec_user " + "where usr_id = :id_code ";
    setString("id_code", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      rtn = sqlStr("usr_cname");
    }
    return rtn;
  }

  @Override
  public void querySelect() throws Exception {
    kkRowid = wp.itemStr("data_k1");
    kkLiacSeqno = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    kkOptname = wp.itemStr("optname");

    if (empty(kkRowid))
      kkRowid = wp.itemStr("rowid");
    if (empty(kkLiacSeqno))
      kkLiacSeqno = wp.itemStr("liac_seqno");

    switch (kkOptname) {
      case "aopt":
        dataReadA();
        break;
      case "bopt":
        dataReadB();
        break;
    }

  }

  public void dataReadA() throws Exception {

    wp.selectSQL = " col_liac_debt.liac_seqno " + " ,col_liac_debt.id_p_seqno "
        + " ,col_liac_debt.id_no " + " ,col_liac_debt.apply_date "
        + " ,col_liac_debt.interest_base_date " + " ,col_liac_debt.bank_code "
        + " ,col_liac_debt.bank_name " + " ,col_liac_debt.auth_uncap_amt "
        + " ,col_liac_debt.in_end_bal " + " ,col_liac_debt.out_end_bal "
        + " ,col_liac_debt.lastest_pay_amt " + " ,col_liac_debt.in_end_bal_new "
        + " ,col_liac_debt.out_end_bal_new " + " ,col_liac_debt.lastest_pay_amt_new "
        + " ,col_liac_debt.crt_user " + " ,col_liac_debt.crt_date " + " ,col_liac_debt.apr_flag "
        + " ,col_liac_debt.apr_date " + " ,col_liac_debt.apr_user " + " ,col_liac_debt.mod_seqno "
        + " ,col_liac_debt.ethic_risk_mark " + " ,col_liac_debt.chi_name "
        + " ,col_liac_debt.proc_flag " + " ,col_liac_debt.proc_date " + " ,col_liac_debt.from_type "
        + " ,col_liac_debt.acct_status_flag " + " ,col_liac_debt.not_send_flag "
        + " ,col_liac_debt.has_rela_flag " + " ,col_liac_debt.has_sup_flag "
        + " ,col_liac_debt.debt_remark " + " ,hex(col_liac_debt.rowid) as rowid "
        + " ,col_liac_nego.liac_status " + " ,col_liac_debt.report_date "
        + " ,col_liac_debt.no_calc_flag " + " ,col_liac_debt.no_include_flag "
        + " ,col_liac_debt.paper_report_flag " + " ,col_liac_debt.out_capital "
        + " ,col_liac_debt.out_interest " + " ,col_liac_debt.out_fee " + " ,col_liac_debt.out_pn "
        + " ,col_liac_debt.out_capital_new " + " ,col_liac_debt.out_interest_new "
        + " ,col_liac_debt.out_fee_new " + " ,col_liac_debt.out_pn_new ";

    wp.daoTable = " col_liac_debt, col_liac_nego ";
    wp.whereStr = " where (col_liac_debt.liac_seqno = col_liac_nego.liac_seqno) ";
    wp.whereStr += "   and col_liac_debt.liac_seqno = :liac_seqno ";
    wp.whereStr +=
        "   and col_liac_debt.mod_time = (select max(mod_time) from col_liac_debt where liac_seqno = :liac_seqno2) ";
    setString("liac_seqno", kkLiacSeqno);
    setString("liac_seqno2", kkLiacSeqno);
    // wp.whereStr+= " and col_liac_debt.rowid = :rowid ";
    // setRowid("rowid", kk_rowid);kk_liac_seqno

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, liac_seqno:" + kkLiacSeqno);
      return;
    }

    detlWkdataA();
  }


  public void dataReadB() throws Exception {

    wp.selectSQL = " col_liac_debt_ch.liac_seqno " + " ,col_liac_debt_ch.id_p_seqno "
        + " ,col_liac_debt_ch.id_no " + " ,col_liac_debt_ch.chi_name "
        + " ,col_liac_debt_ch.jcic_notify_date " + " ,col_liac_debt_ch.apply_date "
        + " ,col_liac_debt_ch.bank_code " + " ,col_liac_debt_ch.bank_name "
        + " ,col_liac_debt_ch.clearing_ym " + " ,col_liac_debt_ch.apply_change_date "
        + " ,col_liac_debt_ch.remark " + " ,col_liac_debt_ch.change_cnt "
        + " ,col_liac_debt_ch.agree_flag " + " ,col_liac_debt_ch.receipt_amt "
        + " ,col_liac_debt_ch.card_debt " + " ,col_liac_debt_ch.cash_card_debt "
        + " ,col_liac_debt_ch.payment_cnt " + " ,col_liac_debt_ch.has_rela_flag "
        + " ,col_liac_debt_ch.rela_consent_flag " + " ,col_liac_debt_ch.proc_flag "
        + " ,col_liac_debt_ch.proc_date " + " ,col_liac_debt_ch.crt_user "
        + " ,col_liac_debt_ch.crt_date " + " ,col_liac_debt_ch.apr_flag "
        + " ,col_liac_debt_ch.apr_date " + " ,col_liac_debt_ch.apr_user "
        + " ,col_liac_nego.liac_status " + " ,hex(col_liac_debt_ch.rowid) as rowid "
        + " ,col_liac_debt_ch.mod_seqno ";

    wp.daoTable = " col_liac_debt_ch, col_liac_nego ";
    wp.whereStr = " where (col_liac_debt_ch.liac_seqno = col_liac_nego.liac_seqno) ";
    wp.whereStr += "   and col_liac_debt_ch.rowid = :rowid ";
    setRowid("rowid", kkRowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, rowid:" + kkRowid);
      return;
    }

    detlWkdataB();
  }

  void detlWkdataA() throws Exception {
    String wkdata = "";

    wkdata = wp.colStr("liac_status");
    wp.colSet("tt_liac_status",
        commString.decode(wkdata, ",1,2,3,4,5", ",1.受理申請,2.停催通知,3.簽約完成,4.結案/復催,5.結案/結清"));

    wkdata = wp.colStr("proc_flag");
    wp.colSet("tt_proc_flag", commString.decode(wkdata, ",0,1,2,A,R", ",0.資料轉入,1.待報送,2.已報送,A.不須報送,R.待處理"));

    wkdata = wp.colStr("acct_status_flag");
    wp.colSet("tt_acct_status_flag", commString.decode(wkdata, ",Y,N", ",Y:呆帳戶,N:非呆帳戶"));

    wkdata = wp.colStr("apr_user");
    wp.colSet("tt_apr_user", wfSecUserName(wkdata));

    wkdata = wp.colStr("proc_flag");
    if (commString.pos("|2|A", wkdata) > 0)
      wp.colSet("pho_disable", "disabled style='background-color: lightgray;'");
  }

  void detlWkdataB() throws Exception {
    String wkdata = "";

    wkdata = wp.colStr("liac_status");
    wp.colSet("tt_liac_status",
        commString.decode(wkdata, ",1,2,3,4,5", ",1.受理申請,2.停催通知,3.簽約完成,4.結案/復催,5.結案/結清"));

    wkdata = wp.colStr("proc_flag");
    wp.colSet("tt_proc_flag", commString.decode(wkdata, ",0,1", ",0.待通知,1.已通知"));

    wkdata = wp.colStr("apr_user");
    wp.colSet("tt_apr_user", wfSecUserName(wkdata));
  }

  // 說明:資料檢核。
  // 1. 若【acct_status_flag(戶況)】為’Y’(呆帳戶)
  // 呆帳戶-對內債權餘額=本金+費用，
  // 呆帳戶-對外債權餘額=本金+利息+違約金+費用。
  // 2. 若【acct_status_flag(戶況)】<>’Y’(非呆帳戶)
  // 非呆帳戶-對內債權餘額=本金+利息+違約金+費用，
  // 非呆帳戶-對外債權餘額=本金+利息+違約金+費用。
  // 3. 若檢核金額不同，顯示確認訊息，仍可以強制存儲。
  // 確認訊息內容: 【對外(內)債權餘額 與 科目對外(內)債權總額 不相等, 是否存檔?】，選擇【確認】，及執行更新。
  int ofValidation() throws Exception {
    String lsAcctStatusFlag;
    double ldInEndBalNew, ldOutEndBalNew;
    double ldOutCapitalNew, ldOutInterestNew, ldOutPnNew, ldOutFeeNew;
    double diffInEndBalNew, diffOutEndBalNew;

    lsAcctStatusFlag = wp.itemStr("acct_status_flag");
    ldInEndBalNew = wp.itemNum("in_end_bal_new"); // 對內債權餘額
    ldOutEndBalNew = wp.itemNum("out_end_bal_new"); // 對外債權餘額
    ldOutCapitalNew = wp.itemNum("out_capital_new"); // 本金
    ldOutInterestNew = wp.itemNum("out_interest_new"); // 利息
    ldOutPnNew = wp.itemNum("out_pn_new"); // 違約金
    ldOutFeeNew = wp.itemNum("out_fee_new"); // 費用
    if (eqIgno(lsAcctStatusFlag, "Y")) {
      diffInEndBalNew = ldInEndBalNew - (ldOutCapitalNew + ldOutFeeNew);
      diffOutEndBalNew =
          ldOutEndBalNew - (ldOutCapitalNew + ldOutInterestNew + ldOutPnNew + ldOutFeeNew);
    } else {
      diffInEndBalNew =
          ldInEndBalNew - (ldOutCapitalNew + ldOutInterestNew + ldOutPnNew + ldOutFeeNew);
      diffOutEndBalNew =
          ldOutEndBalNew - (ldOutCapitalNew + ldOutInterestNew + ldOutPnNew + ldOutFeeNew);
    }

    if (diffInEndBalNew != 0 || diffOutEndBalNew != 0) {
      // if of_excmsg("對外(內)債權餘額 與 科目對外(內)債權總額 不相等, 是否存檔?")<>1 then return -1
      if (eqIgno(wp.itemStr("breakpoint_1"), "Y") == false) {
        breakpoint = 1;
        wp.colSet("breakpoint", 1);
        wp.dispMesg = "檢核中...";
        rc = -1;
        return -1;
      }

      // alert_err("對外(內)債權餘額 與 科目對外(內)債權總額 不相等, 是否存檔?");
      // return -1;
    }

    return 1;
  }

  void ofcPostupdate() throws Exception {
    //wp.alertMesg = "<script language='javascript'> alert('處理完畢請 [啟動 債權計算]')</script>";
	  wp.alertMesg = "<script language='javascript'> alert('處理完畢請等候批次重新計算債權')</script>";
    dataRead();
  }

  void ofcUpdate() throws Exception {
    String lsProcFlag;

    lsProcFlag = wp.itemStr("proc_flag");
    if (commString.pos("|2|A", lsProcFlag) > 0)
      lsProcFlag = "2A";
    func.varsSet("proc_flag", lsProcFlag);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }


  @Override
  public void saveFunc() throws Exception {
    kkOptname = wp.itemStr("optname");
    switch (kkOptname) {
      case "aopt":
        saveFuncA();
        break;
      case "bopt":
        saveFuncB();
        break;
    }
  }


  // 1. 執行【of_validation】。
  // 2. 執行【ofc_update】。
  // 3. 執行【ofc_postupdate】。
  public void saveFuncA() throws Exception {
    func = new Colm1110Func(wp);

    // if (of_validation()<0) return;
    if (ofValidation() < 0) {
      if (breakpoint == 0) { // 中斷做confirm時,不清breakpoint_x ; 其他狀況(跳出or完成)清除breakpoint_x
        wp.colSet("breakpoint_1", "");
      }
      return;
    }
    wp.colSet("breakpoint_1", "");
    ////
    ofcUpdate();
    if (rc == 1)
      ofcPostupdate();
  }

  public void saveFuncB() throws Exception {
    func = new Colm1110Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    dataRead();
  }

  void pdfPrint() throws Exception {
    if (getWhereStr() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.reportId = mProgName;
    wp.pageRows = 9999;

    String cond1 = "協商申請日: " + commString.strToYmd(wp.itemStr("exApplyDateS")) + " -- "
        + commString.strToYmd(wp.itemStr("exApplyDateE"));
    if (empty(wp.itemStr("exId")) == false)
      cond1 += "  身分證字號: " + wp.itemStr("exId");
    if (empty(wp.itemStr("exProcDateS")) == false || empty(wp.itemStr("exProcDateE")) == false) {
      cond1 += "  回報日期: " + commString.strToYmd(wp.itemStr("exProcDateS")) + " -- "
          + commString.strToYmd(wp.itemStr("exProcDateE"));
    }
    wp.colSet("cond_1", cond1);
    wp.colSet("reportName", mProgName.toUpperCase());
    wp.colSet("loginUser", wp.loginUser);
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";

    String subRpt;
    kkOptname = wp.itemStr("optname");
    subRpt = eqIgno(kkOptname, "aopt") ? "A" : "B";

    pdf.excelTemplate = mProgName + subRpt + ".xlsx";
    // pdf.pageCount =30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  int fCallBatch(String asPgname) throws Exception {
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    rc = batch.callBatch(asPgname);
    if (rc != 1) {
      alertErr(asPgname + " 處理: callbatch 失敗");
      return -1;
    }
    alertMsg(asPgname + " 處理: callbatch 成功, 處理序號: " + batch.batchSeqno());

    return 1;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
