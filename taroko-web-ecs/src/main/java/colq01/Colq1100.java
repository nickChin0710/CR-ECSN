/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/02/13  V1.00.00   phopho     program initial                           *
*  108/12/19  V1.00.01   phopho     change table: prt_branch -> gen_brn       *
*  109-05-06  V1.00.02    Aoyulan       updated for project coding standard   *
*  109-12-31  V1.00.03   shiyuqi       修改无意义命名                 
*  110-03-31  V1.00.04   Justin     fix XSS                                   *   
*  110-09-24  V1.00.05   Sunny      配合TCB修改listWkdata()定義                                   *
******************************************************************************/

package colq01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colq1100 extends BaseEdit {
  CommString commString = new CommString();
  int llErr = 0;
  String rowid = "";
  double imInBal = 0;
  double imOutBal = 0;
  String kkLiacSeqno;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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

    // dddw_select();
    initButton();
  }

  private boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exFileDateS");
    String lsDate2 = wp.itemStr("exFileDateE");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[資料轉入日期-起迄]  輸入錯誤");
      return false;
    }
    String lsDate3 = wp.itemStr("exNotifyDateS");
    String lsDate4 = wp.itemStr("exNotifyDateE");
    if (this.chkStrend(lsDate3, lsDate4) == false) {
      alertErr2("[通知日期-起迄]  輸入錯誤");
      return false;
    }
    String lsDate5 = wp.itemStr("exApplyDateS");
    String lsDate6 = wp.itemStr("exApplyDateE");
    if (this.chkStrend(lsDate5, lsDate6) == false) {
      alertErr2("[申請日期-起迄]  輸入錯誤");
      return false;
    }
    // if(empty(wp.item_ss("exId")) == false){
    // if (wp.item_ss("exId").trim().length() < 5){
    // err_alert("身份證號至少要5碼");
    // return false;
    // }
    // }

    wp.whereStr = "where 1=1 ";
    if (empty(wp.itemStr("exFileDateS")) == false) {
      wp.whereStr += " and file_date >= :file_dates ";
      setString("file_dates", wp.itemStr("exFileDateS"));
    }
    if (empty(wp.itemStr("exFileDateE")) == false) {
      wp.whereStr += " and file_date <= :file_datee ";
      setString("file_datee", wp.itemStr("exFileDateE"));
    }
    if (empty(wp.itemStr("exNotifyDateS")) == false) {
      wp.whereStr += " and notify_date >= :notify_dates ";
      setString("notify_dates", wp.itemStr("exNotifyDateS"));
    }
    if (empty(wp.itemStr("exNotifyDateE")) == false) {
      wp.whereStr += " and notify_date <= :notify_datee ";
      setString("notify_datee", wp.itemStr("exNotifyDateE"));
    }
    if (empty(wp.itemStr("exApplyDateS")) == false) {
      wp.whereStr += " and apply_date >= :apply_dates ";
      setString("apply_dates", wp.itemStr("exApplyDateS"));
    }
    if (empty(wp.itemStr("exApplyDateE")) == false) {
      wp.whereStr += " and apply_date <= :apply_datee ";
      setString("apply_datee", wp.itemStr("exApplyDateE"));
    }
    if (empty(wp.itemStr("exId")) == false) {
      wp.whereStr += " and id_no = :id_no ";
      setString("id_no", wp.itemStr("exId"));
    }
    if (eqIgno(wp.itemStr("exLiacStatus"), "0") == false) {
      wp.whereStr += " and liac_status = :liac_status ";
      setString("liac_status", wp.itemStr("exLiacStatus"));
    }

    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, file_date, liac_status, "
            + "notify_date, id_no, id_p_seqno, chi_name, bank_code, "
            + "bank_name, apply_date, nego_s_date, stop_notify_date, recol_reason, "
            + "credit_flag, no_credit_flag, cash_card_flag, credit_card_flag, "
            + "contract_date, liac_remark, end_date, end_reason, court_agree_date, "
            + "case_status, liac_txn_code, 'nego_hst' as db_table ";

    wp.daoTable = "col_liac_nego_hst";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String wkData = "", wkData2 = "";
    //String[] cde = new String[] {"1", "2", "3", "4", "5", "A", "B", "C", "D", "E", "F"};
    //String[] txt = new String[] {"1.受理申請", "2.停催通知", "3.簽約完成", "4.結案/復催", "5.結案/結清",     
    String[] cde = new String[] {"1", "2", "3", "4", "5","6", "A", "B", "C", "D", "E", "F"};
    String[] txt = new String[] {"1.受理申請", "2.停催通知", "3.簽約完成", "4.復催", "5.毀諾","6.結清",
    		"A.基本資料異動","B.法院認可", "C.延期繳款", "D.請求同意(Z96)", "E.簽約方案(Z98)", "F.單獨受償"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "liac_status");
      wp.colSet(ii, "tt_liac_status", commString.decode(wkData, cde, txt));

      wkData = wp.colStr(ii, "bank_code");
      wkData2 = wp.colStr(ii, "bank_name");
      wp.colSet(ii, "wk_bank_name", wkData + " " + wkData2);

      wkData = wp.colStr(ii, "liac_remark");
      wp.colSet(ii, "tt_liac_remark", commString.decode(wkData, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    rowid = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "file_date, " + "liac_status, "
        + "query_date, " + "notify_date, " + "payment_rate, " + "id_no, "
        // + "id_p_seqno, " //可能為空值, 會影響後續查詢資料, 直接改由id_no取得id_p_seqno
        + "uf_idno_pseqno(id_no) as id_p_seqno, " + "chi_name, " + "bank_code, " + "bank_name, "
        + "apply_date, " + "nego_s_date, " + "stop_notify_date, " + "interest_base_date, "
        + "recol_reason, " + "credit_flag, " + "no_credit_flag, " + "cash_card_flag, "
        + "credit_card_flag, " + "contract_date, " + "liac_remark, " + "end_date, " + "end_reason, "
        + "end_remark, " + "liac_txn_code, " + "reg_bank_no, " + "id_data_date, "
        + "court_agree_date, " + "case_status, " + "crt_date, " + "crt_time, " + "apr_flag, "
        + "apr_date, " + "apr_user, " + "end_user, " + "mod_time, " + "mod_pgm, "
        + "acct_status_apply, " // 申請時戶況
        + "m_code_apply " // 申請時Mcode
    ;

    wp.daoTable = "col_liac_nego_hst";

    wp.whereStr = "where 1=1 and hex(rowid) = :rowid ";
    setString("rowid", rowid);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + rowid);
      return;
    }

    kkLiacSeqno = wp.colStr("liac_seqno");
    wfGetDebtBal(kkLiacSeqno);
    listWkdataDetl();
    queryLiacDebtDtl();
    queryLiacPlan();
    queryLiacContract(); // 比照colm1100新增頁籤
    queryLiacContractHst();
    queryLiacCourt();
    queryLiacPay();
    queryLiacDelay();
    queryLiacReceipt();
    queryLiacDebt();
  }

  void queryLiacDebtDtl() throws Exception {
    this.selectNoLimit();

    daoTid = "A-";
    wp.selectSQL = "hex(col_liac_debt_dtl.rowid) as rowid, col_liac_debt_dtl.liac_seqno, "
            + "col_liac_debt_dtl.id_no, col_liac_debt_dtl.id_p_seqno, "
            + "col_liac_debt_dtl.notify_date, col_liac_debt_dtl.apply_date, "
            + "col_liac_debt_dtl.p_seqno, col_liac_debt_dtl.acct_type, act_acno.acct_key, "
            + "col_liac_debt_dtl.acct_status, col_liac_debt_dtl.acct_month, "
            + "col_liac_debt_dtl.mcode, col_liac_debt_dtl.payment_rate1, "
            + "col_liac_debt_dtl.payment_rate2, col_liac_debt_dtl.payment_rate3, "
            + "col_liac_debt_dtl.payment_rate4, col_liac_debt_dtl.payment_rate5, "
            + "col_liac_debt_dtl.payment_rate6, col_liac_debt_dtl.tot_amt, "
            + "col_liac_debt_dtl.ethic_risk_mark ";

    wp.daoTable = "col_liac_debt_dtl, act_acno";
    // wp.whereStr = "where col_liac_debt_dtl.p_seqno = act_acno.p_seqno "
    wp.whereStr = "where col_liac_debt_dtl.p_seqno = act_acno.acno_p_seqno "
        + "and liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    wp.setListCount(1);
    wp.notFound = "";
    listWkdataDetlA();
  }

  void queryLiacPlan() throws Exception {
    this.selectNoLimit();

    daoTid = "B-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, id_no, id_p_seqno, "
            + "apply_date, reg_bank_no, resp_agree_e_date, debt_reason, "
            + "liac_credit_amt, liac_install_cnt, liac_int_rate, month_pay_amt, "
            + "phase2_pay_flag, lastest_pay_amt, in_end_bal, out_end_bal, "
            + "report_date, agree_flag, debt_remark, est_allocate_amt, "
            + "liac_remark, ethic_risk_mark, proc_flag, file_date, notify_date, "
            + "bank_code, bank_name ";

    wp.daoTable = "col_liac_plan";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    wp.setListCount(2);
    wp.notFound = "";
    listWkdataDetlB();
  }

  void queryLiacContract() throws Exception {
    this.selectNoLimit();

    daoTid = "C-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, file_date, id_no, "
            + "bank_code, bank_name, apply_date, nego_e_date, contract_e_date, "
            + "liac_install_cnt, liac_int_rate, install_s_date, month_pay_amt, "
            + "credit_cont_amt, cash_card_cont_amt, credit_card_cont_amt, "
            + "total_cont_amt, m_credit_cont_amt, m_cash_card_cont_amt, "
            + "m_credit_card_cont_amt, per_allocate_amt, m_no_credit_rate, pay_acct_no, "
            + "proc_flag ";

    wp.daoTable = "col_liac_contract";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, nego_e_date ";

    pageQuery();

    wp.setListCount(3);
    wp.notFound = "";
  }

  void queryLiacContractHst() throws Exception {
    this.selectNoLimit();

    daoTid = "J-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, file_date, id_no, "
            + "bank_code, bank_name, apply_date, nego_e_date, contract_e_date, "
            + "liac_install_cnt, liac_int_rate, install_s_date, month_pay_amt, "
            + "credit_cont_amt, cash_card_cont_amt, credit_card_cont_amt, "
            + "total_cont_amt, m_credit_cont_amt, m_cash_card_cont_amt, "
            + "m_credit_card_cont_amt, per_allocate_amt, m_no_credit_rate, pay_acct_no, "
            + "proc_flag ";

    wp.daoTable = "col_liac_contract_hst";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, nego_e_date ";

    pageQuery();

    wp.setListCount(4);
    wp.notFound = "";
  }

  void queryLiacCourt() throws Exception {
    this.selectNoLimit();

    daoTid = "D-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, apply_date, id_no, "
            + "recv_date, liac_doc_no, court_name, case_no, is_allow, "
            + "user_remark, crt_user, crt_date, apr_flag, apr_date, apr_user, "
            + "mod_user, mod_time, mod_pgm, mod_seqno ";

    wp.daoTable = "col_liac_court";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, recv_date ";

    pageQuery();

    wp.setListCount(5);
    wp.notFound = "";
    listWkdataDetlD();
  }

  void queryLiacPay() throws Exception {
    this.selectNoLimit();

    daoTid = "E-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, pay_seqno, file_date, "
            + "file_type, bank_code, apply_date, reg_bank_no, allocate_date, "
            + "allocate_amt, liac_remark ";

    wp.daoTable = "col_liac_pay";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, apply_date ";

    pageQuery();

    wp.setListCount(6);
    wp.notFound = "";
    listWkdataDetlE();
  }

  void queryLiacDelay() throws Exception {
    this.selectNoLimit();

    daoTid = "F-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, file_date, query_date, "
            + "notify_date, bank_code, bank_name, apply_date, delay_month, "
            + "delay_reason, delay_desc, reg_bank_no, liac_remark ";

    wp.daoTable = "col_liac_delay";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    wp.setListCount(7);
    wp.notFound = "";
    listWkdataDetlF();
  }

  void queryLiacReceipt() throws Exception {
    this.selectNoLimit();

    daoTid = "G-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, file_date, notify_date, "
            + "receipt_type, receipt_code, per_allocate_amt, m_no_credit_rate, "
            + "install_s_date, liac_install_cnt, liac_txn_code, receipt_date, "
            + "substrb(install_s_date, 1, 6) db_inst_s_date, "
            + "substrb(install_s_date, 1, 6) chk_inst_s_date, crt_user, crt_date ";

    wp.daoTable = "col_liac_receipt";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    wp.setListCount(8);
    wp.notFound = "";
    listWkdataDetlG();
  }

  void queryLiacDebt() throws Exception {
    this.selectNoLimit();

    daoTid = "I-";
    wp.selectSQL = "hex(rowid) as rowid, liac_seqno, id_no, apply_date, "
            + "interest_base_date, bank_code, bank_name, in_end_bal, out_end_bal, "
            + "lastest_pay_amt, in_end_bal_new, out_end_bal_new, lastest_pay_amt_new, "
            + "crt_user, crt_date, apr_flag, apr_date, apr_user, mod_seqno, "
            + "ethic_risk_mark, chi_name, proc_flag, proc_date, from_type, "
            + "not_send_flag, has_rela_flag, has_sup_flag, debt_remark, "
            + "report_date, no_calc_flag ";

    wp.daoTable = "col_liac_debt";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", kkLiacSeqno);
    wp.whereOrder = "order by liac_seqno, apply_date ";

    pageQuery();

    wp.setListCount(9);
    wp.notFound = "";
    listWkdataDetlI();
  }

  void listWkdataDetl() throws Exception {
    String wkData = "", wkData2 = "";
    String[] cde = new String[] {"1", "2", "3", "4", "5","6", "A", "B", "C", "D", "E", "F"};
    String[] txt = new String[] {"1.受理申請", "2.停催通知", "3.簽約完成", "4.結案/復催", "5.結案/毀諾","6.結案/結清", "A.基本資料異動",
        "B.法院認可", "C.延期繳款", "D.請求同意(Z96)", "E.簽約方案(Z98)", "F.單獨受償"};

    wkData = wp.colStr("liac_status");
    wp.colSet("tt_liac_status", commString.decode(wkData, cde, txt));

    wkData = wp.colStr("bank_code");
    wkData2 = wp.colStr("bank_name");
    wp.colSet("wk_bank_code_name", wkData + "_" + wkData2);

    wkData = wp.colStr("recol_reason");
    wp.colSet("tt_recol_reason", wfColLiabIdtabDesc(wkData, "5"));

    wkData = wp.colStr("case_status");
    wp.colSet("tt_case_status", commString.decode(wkData, ",1,2", ",1.遞狀聲請,2.法院裁定"));

    wkData = wp.colStr("liac_txn_code");
    wp.colSet("tt_liac_txn_code", commString.decode(wkData, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));

    wkData = wp.colStr("reg_bank_no");
    wp.colSet("tt_reg_bank_no", wfPtrBranchName(wkData));

    wkData = wp.colStr("end_reason");
    wp.colSet("tt_end_reason", wfColLiabIdtabDesc(wkData, "5"));

    System.out.println(">>>START list_wkdata_detl(): wf_get_tol_amt <<<");
    String lsIdPSeqno = wp.colStr("id_p_seqno");
    double tolAmt = wfGetTolAmt(lsIdPSeqno);
    wp.colSet("db_tol_amt", numToStr(tolAmt, ""));
    System.out.println(">>>  END list_wkdata_detl(): wf_get_tol_amt <<<");
  }

  void listWkdataDetlA() throws Exception {
    String wkData = "", wkData2 = "";

    if (wp.selectCnt <= 0) {
    	wp.colSet("a-static_text_style", "display:none");
	}
    
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "a-acct_type");
      wkData2 = wp.colStr(ii, "a-acct_key");
      wp.colSet(ii, "a-wk_acct_key", wkData + "-" + wkData2);

//      if (ii == 0) {
//        wkData = "<tr><td nowrap class='list_ll' colspan='14'><span style='color:#A23400'> ";
//        wkData += "***備註：道德風險評估說明：  <br>" + "&nbsp;&nbsp;&nbsp;　　 1:近半年繳評不足6個月 <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 2:近半年繳評1~2月為0A.0B.0C.0D.0E任2個，3~6月為0A.0B. 0E任4個，且current總欠 > 0 <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 3: ( current總欠 – 最近關帳欠款金額 ) > 信用額度 * 30% <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 Y:人工評估有道德風險之歷史案件 <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 4:近半年單筆消費金額超過10,000(含)以上";
//        wkData += "&nbsp;</span></td></tr>";
//        wp.colSet("a-static_text", wkData);
//      }

    }
  }

  void listWkdataDetlB() throws Exception {
    String wkData = "";
    double lic, mpa, lpa;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "b-proc_flag");
      wp.colSet(ii, "b-tt_proc_flag", commString.decode(wkData, ",0,1,2,A", ",0.未報送,1.未報送,2.已報送,A.不須報送"));

      wkData = wp.colStr(ii, "b-reg_bank_no");
      wp.colSet(ii, "b-tt_reg_bank_no", wfPtrBranchName(wkData));

      wkData = wp.colStr(ii, "b-debt_reason");
      wp.colSet(ii, "b-tt_debt_reason", wfColLiabIdtabDesc(wkData, "6"));

      lic = wp.colNum(ii, "b-liac_install_cnt");
      mpa = wp.colNum(ii, "b-month_pay_amt");
      lpa = wp.colNum(ii, "b-lastest_pay_amt");
      wp.colSet(ii, "b-wk_est_allocate_amt", numToStr((lic * mpa) + lpa, ""));

      wkData = wp.colStr(ii, "b-liac_remark");
      wp.colSet(ii, "b-tt_liac_remark",
          commString.decode(wkData, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));

      wp.colSet(ii, "b-in_end_bal", numToStr(imInBal, ""));
      wp.colSet(ii, "b-out_end_bal", numToStr(imOutBal, ""));
    }
  }

  void listWkdataDetlD() throws Exception {
    String wkData = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "d-apr_flag");
      wp.colSet(ii, "d-tt_apr_flag", commString.decode(wkData, ",Y,N", ",已覆核,未覆核"));

      wkData = wp.colStr(ii, "d-is_allow");
      wp.colSet(ii, "d-tt_is_allow", commString.decode(wkData, ",Y,N", ",認可: Y,認可: N"));
    }
  }

  void listWkdataDetlE() throws Exception {
    String wkData = "";
    double ftTolamt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "e-file_type");
      wp.colSet(ii, "e-tt_file_type", commString.decode(wkData, ",F,E", ",F.他行,E.本行"));

      ftTolamt += wp.colNum(ii, "e-allocate_amt");
    }

    if (wp.selectCnt > 0) {
      wkData = "<tr><td colspan='2'></td><td nowrap class='list_rr'>筆  數：</td>";
      wkData += "<td nowrap class='list_rr'> " + wp.selectCnt + "&nbsp;</td>";
      wkData += "<td></td><td nowrap class='list_rr'>金額合計：</td>";
      wkData += "<td nowrap class='list_rr' colspan='2'> " + numToStr(ftTolamt, "") + "&nbsp;</td>";
      wkData += "<td></td></tr>";
      wp.colSet("e-ft_tolcnt", wkData);
    }
  }

  void listWkdataDetlF() throws Exception {
    String wkData = "", wkData2 = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {	
      wkData = wp.colStr(ii, "f-delay_reason");
      wkData2 = wp.colStr(ii, "f-delay_desc");
      wp.colSet(ii, "f-wk_delay_desc", wkData + " " + wkData2);

      wkData = wp.colStr(ii, "f-liac_remark");
      wp.colSet(ii, "f-tt_liac_remark",
          commString.decode(wkData, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));
    }

    // if (wp.selectCnt > 0) {
    // ss = "<tr><td colspan='2'></td><td nowrap class='list_rr'>筆 數：</td>";
    // ss += "<td nowrap class='list_rr'> "+wp.selectCnt+"&nbsp;</td>";
    // ss += "<td colspan='4'></td></tr>";
    // wp.col_set("f-wk_tolcnt", ss);
    // }
  }

  void listWkdataDetlG() throws Exception {
    String wkData = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "g-crt_user");
      wp.colSet(ii, "g-tt_crt_user", wfSecUserIDName(wkData));

      wkData = wp.colStr(ii, "g-receipt_code");
      wp.colSet(ii, "g-tt_receipt_code", wfColLiabIdtabDesc(wkData, "7"));

      if (ii == (wp.selectCnt - 1)) {
        wp.colSet(ii, "g-col_inst_readonly", "");
      } else {
        wp.colSet(ii, "g-col_inst_readonly", "readOnly");
      }
    }

    if (wp.selectCnt > 0) {
      wp.colSet("btnUpdateG_disable", "");
    } else {
      wp.colSet("btnUpdateG_disable", "disabled");
    }
  }

  void listWkdataDetlI() throws Exception {
    String aprUser = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      aprUser = wp.colStr(ii, "i-apr_user");
      wp.colSet(ii, "i-tt_apr_user", wfSecUserIDName(aprUser));
    }
  }

  // 說明: 取得回報債權等相關金額。
  int wfGetDebtBal(String asLiacSeqno) throws Exception {
    String lsLiacSeqno = "";

    double imOutCapital = 0;
    double imOutInt = 0;
    double imOutFee = 0;
    double imOutPn = 0;

    lsLiacSeqno = asLiacSeqno;
    if (empty(lsLiacSeqno))
      return 0;

    String lsSql = "select in_end_bal_new, out_end_bal_new, out_capital_new, "
        + " out_interest_new, out_pn_new, out_fee_new from col_liac_debt "
        + "where liac_seqno = :liac_seqno " + "  and proc_flag = '2' " + "order by proc_date desc ";
    setString("liac_seqno", lsLiacSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      imInBal = sqlNum("in_end_bal_new");
      imOutBal = sqlNum("out_end_bal_new");
      imOutCapital = sqlNum("out_capital_new");
      imOutInt = sqlNum("out_interest_new");
      imOutFee = sqlNum("out_pn_new");
      imOutPn = sqlNum("out_fee_new");
    }

    wp.colSet("db_in_bal", numToStr(imInBal, ""));
    wp.colSet("db_out_bal", numToStr(imOutBal, ""));
    wp.colSet("db_out_capital", numToStr(imOutCapital, ""));
    wp.colSet("db_out_int", numToStr(imOutInt, ""));
    wp.colSet("db_out_pn", numToStr(imOutFee, ""));
    wp.colSet("db_out_fee", numToStr(imOutPn, ""));

    return 1;
  }

  // 說明: 以身分證號，取得【欠款金額(db_tol_amt)】。
  double wfGetTolAmt(String asIdPSeqno) throws Exception {
    String lsIdPSeqno = "";
    double lmAmt, lmUnpostBal = 0;

    lsIdPSeqno = asIdPSeqno;
    if (empty(lsIdPSeqno))
      return 0;

    //// -分期未posting-
    String lsSql = "select nvl(sum( "
        + "(nvl(install_tot_term,0) - nvl(install_curr_term,0)) * nvl(unit_price,0) "
        + " + nvl(remd_amt,0) "
        + " + decode(install_curr_term,0,nvl(first_remd_amt,0)+nvl(extra_fees,0),0) "
        + " ),0) unpost_bal from bil_contract " + "where id_p_seqno = :id_p_seqno "
        + "and nvl(install_tot_term,0) != nvl(install_curr_term,0) " + "and contract_kind = '1' "
        // + "and nvl(authorization,'N') not in ('N' , 'REJECT','P','reject') " //no column
        + "and nvl(auth_code,'N') not in ('N' , 'REJECT','P','reject') " + "and post_cycle_dd > 0 ";
    setString("id_p_seqno", lsIdPSeqno);
    sqlSelect(lsSql);
    lmUnpostBal = (long) sqlNum("unpost_bal");

    // -欠款金額-
    lsSql =
        "select nvl(sum(acct_jrnl_bal),0) amt from act_acct " + "where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", lsIdPSeqno);
    sqlSelect(lsSql);
    lmAmt = (long) sqlNum("amt");

    lmAmt = lmAmt + lmUnpostBal;

    return lmAmt;
  }

  String wfColLiabIdtabDesc(String idcode, String idkey) throws Exception {
    String rtn = "";
    String lsSql = "select id_code||' ['||id_desc||']' id_desc from col_liab_idtab "
        + "where id_code = :id_code and id_key = :id_key order by id_key, id_code ";
    setString("id_code", idcode);
    setString("id_key", idkey);
    sqlSelect(lsSql);
    if (sqlRowNum > 0)
      rtn = sqlStr("id_desc");

    return rtn;
  }

  String wfPtrBranchName(String idcode) throws Exception {
    String rtn = "";
    // String ls_sql = "select branch||'['||branch_name||']' id_desc from ptr_branch "
    String lsSql =
        "select branch||' ['||full_chi_name||']' id_desc from gen_brn " + "where branch= :id_code ";
    setString("id_code", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      rtn = idcode;
    } else {
      rtn = sqlStr("id_desc");
    }
    return rtn;
  }

  String wfSecUserIDName(String idcode) throws Exception {
    String rtn = "";
    String lsSql =
        "select usr_id||' ['||usr_cname||']' id_desc from sec_user " + "where usr_id= :id_code ";
    setString("id_code", idcode);
    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      rtn = idcode;
    } else {
      rtn = sqlStr("id_desc");
    }
    return rtn;
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

}
