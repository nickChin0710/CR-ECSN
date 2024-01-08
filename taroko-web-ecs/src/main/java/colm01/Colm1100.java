/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/02/12  V1.00.00   phopho     program initial                           *
*  108/12/19  V1.00.01   phopho     change table: prt_branch -> gen_brn       *
*  109-05-06  V1.00.02  Aoyulan       updated for project coding standard     *
*  109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
*  109-01-04  V1.00.01   shiyuqi       修改无意义命名                            
*  110-03-31  V1.00.04   Justin     fix XSS                                   *      
*  112-10-04  V1.00.05   Ryan       修改協商狀態查詢條件                                                                        *  
******************************************************************************/

package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm1100 extends BaseEdit {
  CommString commString = new CommString();
  Colm1100Func func;
  int llErr = 0;
  String liacSeqno = "";
  String recvDate = "";
  double imInBal = 0;
  double imOutBal = 0;
  String kkLiacSeqno, kkApplyDate, kkIdNo;
  int rowcntaa = 0, rowcntbb = 0, rowcntcc = 0, rowcntdd = 0, rowcntee = 0, rowcntff = 0,
      rowcntgg = 0, rowcntii = 0;

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
    } else if (eqIgno(wp.buttonCode, "CC")) {
      /* 取消 結案/復催 */
      wfCloseCancel();
    } else if (eqIgno(wp.buttonCode, "UC")) {
      /* 簽約方案 存檔 */
      doLiacContractUpdate();
    } else if (eqIgno(wp.buttonCode, "UG")) {
      /* 單獨受償 存檔 */
      wfReceiptUpd();
    } else if (eqIgno(wp.buttonCode, "E")) {
      /* 結案 */
      wfEnd();
    } else if (eqIgno(wp.buttonCode, "CE")) {
      /* 取消結案 */
      wfCancelEnd();
    } else if (eqIgno(wp.buttonCode, "CA")) {
      /* 法院公文新增頁面初始值 */
      addCourtKey();
      strAction = "new";
      clearFunc();
      setCourtKey();
    } else if (eqIgno(wp.buttonCode, "CT")) {
      /* 導入法院公文維護頁面 */
      dataReadCourt();
    } else if (eqIgno(wp.buttonCode, "CU")) {
      /* 法院公文存檔 */
      courtUpdate();
    } else if (eqIgno(wp.buttonCode, "CS")) {
      /* 法院公文修改取消 */
      courtSave();
    } else if (eqIgno(wp.buttonCode, "CD")) {
      /* 法院公文刪除公文 */
      courtDelete();
    } else if (eqIgno(wp.buttonCode, "CL")) {
      /* 法院公文清畫面 */
      addCourtKey();
      strAction = "";
      clearFunc();
      setCourtKey();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "--";
      wp.optionKey = wp.colStr("exBankCode");
      dddwList("ColLiabBankList", "col_liab_bank", "bank_code", "bank_code||' '||bank_name",
          "where 1=1 order by bank_code ");

      wp.optionKey = wp.colStr("reg_bank_no");
      // dddw_list("PtrBranchNameList", "ptr_branch", "branch", "branch||'['||branch_name||']'",
      // "where 1=1 order by branch ");
      dddwList("PtrBranchNameList", "gen_brn", "branch", "branch||' ['||full_chi_name||']'",
          "where 1=1 order by branch ");

      wp.optionKey = wp.colStr("h-end_reason");
      dddwList("ColLiabIdtabList", "col_liab_idtab", "id_code", "id_code||'['||id_desc||']'",
          "where id_key='5' order by id_key, id_code ");

      wp.optionKey = wp.colStr("court_name");
      dddwList("PtrSysIdtabList", "ptr_sys_idtab", "wf_desc", "wf_desc",
          "where wf_type='COURT_NAME' order by wf_type, wf_id ");
    } catch (Exception ex) {
    }
  }

  private boolean getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("exApplyDateS");
    String lsDate2 = wp.itemStr("exApplyDateE");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[申請日期-起迄]  輸入錯誤");
      return false;
    }
    if (empty(wp.itemStr("exId")) == false) {
      if (wp.itemStr("exId").trim().length() < 5) {
        alertErr2("身份證號至少要5碼");
        return false;
      }
    }

    wp.whereStr = "where 1=1 ";
    if (empty(wp.itemStr("exApplyDateS")) == false) {
      wp.whereStr += " and apply_date >= :apply_dates ";
      setString("apply_dates", wp.itemStr("exApplyDateS"));
    }
    if (empty(wp.itemStr("exApplyDateE")) == false) {
      wp.whereStr += " and apply_date <= :apply_datee ";
      setString("apply_datee", wp.itemStr("exApplyDateE"));
    }
    if (empty(wp.itemStr("exEndDate")) == false) {
      wp.whereStr += " and end_date = :end_date ";
      setString("end_date", wp.itemStr("exEndDate"));
    }
    if (empty(wp.itemStr("exId")) == false) {
      wp.whereStr += " and id_no like :id_no ";
      setString("id_no", wp.itemStr("exId") + "%");
    }
    if (empty(wp.itemStr("exRecolReason")) == false) {
      wp.whereStr += " and recol_reason = :recol_reason ";
      setString("recol_reason", wp.itemStr("exRecolReason"));
    }
    if (empty(wp.itemStr("exBankCode")) == false) {
      wp.whereStr += " and bank_code = :bank_code ";
      setString("bank_code", wp.itemStr("exBankCode"));
    }
//    if ((eqIgno(wp.itemStr("exLiacStatus"), "0") == false)
//        && (eqIgno(wp.itemStr("exLiacStatus"), "5") == false)) {
//      wp.whereStr += " and liac_status = :liac_status ";
//      setString("liac_status", wp.itemStr("exLiacStatus"));
//    } else if (eqIgno(wp.itemStr("exLiacStatus"), "5")) {
//      wp.whereStr += " and end_date <> '' ";
//    }
    if (eqIgno(wp.itemStr("exLiacStatus"), "0") == false){
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

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "liac_seqno, " + "file_date, "
        + "liac_status, " + "notify_date, " + "id_no, " + "id_p_seqno, " + "chi_name, "
        + "bank_code, " + "bank_name, " + "apply_date, " + "nego_s_date, " + "stop_notify_date, "
        + "recol_reason, " + "credit_flag, " + "no_credit_flag, " + "cash_card_flag, "
        + "credit_card_flag, " + "contract_date, " + "liac_remark, " + "end_date, " + "end_reason, "
        + "court_agree_date, " + "case_status ";

    wp.daoTable = "col_liac_nego";

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
    String param = "", param2 = "";
    String[] cde = new String[] {"1", "2", "3", "4", "5","6","A", "B", "C", "D", "E", "F"};
    String[] txt = new String[] {"1.受理申請", "2.停催通知", "3.簽約完成", "4.結案/復催", "5.結案/毀諾","6.結案/結清", "A.基本資料異動",
        "B.法院認可", "C.延期繳款", "D.請求同意(Z96)", "E.簽約方案(Z98)", "F.單獨受償"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "liac_status");
      wp.colSet(ii, "tt_liac_status", commString.decode(param, cde, txt));

      param = wp.colStr(ii, "bank_code");
      param2 = wp.colStr(ii, "bank_name");
      wp.colSet(ii, "wk_bank_name", param + " " + param2);

      param = wp.colStr(ii, "liac_remark");
      wp.colSet(ii, "tt_liac_remark", commString.decode(param, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    liacSeqno = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(liacSeqno)) {
      liacSeqno = itemKk("liac_seqno");
    }
    if (isEmpty(liacSeqno)) {
      alertErr("前置協商序號: 不可空白");
      return;
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "liac_seqno, " + "file_date, "
        + "liac_status, " + "query_date, " + "notify_date, " + "id_no, "
        // + "id_p_seqno, " //可能為空值, 會影響後續查詢資料, 直接改由id_no取得id_p_seqno
        + "uf_idno_pseqno(id_no) as id_p_seqno, " + "chi_name, " + "bank_code, " + "bank_name, "
        + "apply_date, " + "nego_s_date, " + "stop_notify_date, " + "interest_base_date, "
        + "recol_reason, " + "credit_flag, " + "no_credit_flag, " + "cash_card_flag, "
        + "credit_card_flag, " + "contract_date, " + "liac_remark, " + "end_date, " + "end_reason, "
        + "end_remark, " + "liac_txn_code, " + "reg_bank_no, " + "id_data_date, "
        + "court_agree_date, " + "case_status, " + "crt_date, " + "crt_time, " + "proc_flag, "
        + "proc_date, " + "apr_flag, " + "apr_date, " + "apr_user, " + "end_user, " + "mod_time, "
        + "mod_pgm, " + "mod_user, " + "acct_status_apply, " // 申請時戶況
        + "m_code_apply " // 申請時Mcode
    ;

    wp.daoTable = "col_liac_nego";

    wp.whereStr = "where 1=1" + " and liac_seqno  = :liac_seqno ";
    setString("liac_seqno", liacSeqno);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + liacSeqno);
      return;
    }

    wfGetDebtBal(liacSeqno);
    listWkdataDetl();
    queryLiacDebtDtl();
    queryLiacPlan();
    queryLiacContract();
    queryLiacContractHst(); // 20190307 增加[簽約暨變更方案歷史查詢]頁籤
    queryLiacCourt();
    queryLiacPay();
    queryLiacDelay();
    queryLiacReceipt();
    // queryLiacNego();
    listWkdataDetlH();
  }

  void queryLiacDebtDtl() throws Exception {
    this.selectNoLimit();

    daoTid = "A-";
    wp.selectSQL = "hex(col_liac_debt_dtl.rowid) as rowid, " + "col_liac_debt_dtl.liac_seqno, "
        + "col_liac_debt_dtl.id_no, " + "col_liac_debt_dtl.id_p_seqno, "
        + "col_liac_debt_dtl.notify_date, " + "col_liac_debt_dtl.apply_date, "
        + "col_liac_debt_dtl.p_seqno, " + "col_liac_debt_dtl.acct_type, " + "act_acno.acct_key, "
        + "col_liac_debt_dtl.acct_status, " + "col_liac_debt_dtl.acct_month, "
        + "col_liac_debt_dtl.mcode, " + "col_liac_debt_dtl.payment_rate1, "
        + "col_liac_debt_dtl.payment_rate2, " + "col_liac_debt_dtl.payment_rate3, "
        + "col_liac_debt_dtl.payment_rate4, " + "col_liac_debt_dtl.payment_rate5, "
        + "col_liac_debt_dtl.payment_rate6, " + "col_liac_debt_dtl.tot_amt, "
        + "col_liac_debt_dtl.ethic_risk_mark ";

    wp.daoTable = "col_liac_debt_dtl, act_acno";
    // wp.whereStr = "where col_liac_debt_dtl.p_seqno = act_acno.p_seqno "
    wp.whereStr = "where col_liac_debt_dtl.p_seqno = act_acno.acno_p_seqno "
        + "and liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    wp.setListCount(1);
    wp.notFound = "";
    listWkdataDetlA();
  }

  void queryLiacPlan() throws Exception {
    this.selectNoLimit();

    daoTid = "B-";
    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "id_no, " + "id_p_seqno, "
        + "apply_date, " + "reg_bank_no, " + "resp_agree_e_date, " + "debt_reason, "
        + "liac_credit_amt, " + "liac_install_cnt, " + "liac_int_rate, " + "month_pay_amt, "
        + "phase2_pay_flag, " + "lastest_pay_amt, " + "in_end_bal, " + "out_end_bal, "
        + "report_date, " + "agree_flag, " + "debt_remark, " + "est_allocate_amt, "
        + "liac_remark, " + "ethic_risk_mark, " + "proc_flag, " + "file_date, " + "notify_date, "
        + "bank_code, " + "bank_name ";

    wp.daoTable = "col_liac_plan";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    wp.setListCount(2);
    wp.notFound = "";
    listWkdataDetlB();
  }

  void queryLiacContract() throws Exception {
    this.selectNoLimit();

    daoTid = "C-";
    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "file_date, " + "id_no, "
        + "bank_code, " + "bank_name, " + "apply_date, " + "nego_e_date, " + "contract_e_date, "
        + "liac_install_cnt, " + "liac_int_rate, " + "install_s_date, " + "month_pay_amt, "
        + "credit_cont_amt, " + "cash_card_cont_amt, " + "credit_card_cont_amt, "
        + "total_cont_amt, " + "m_credit_cont_amt, " + "m_cash_card_cont_amt, "
        + "m_credit_card_cont_amt, " + "per_allocate_amt, " + "m_no_credit_rate, " + "pay_acct_no, "
        + "proc_flag ";

    wp.daoTable = "col_liac_contract";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by liac_seqno, nego_e_date ";

    pageQuery();

    wp.setListCount(3);
    wp.notFound = "";
    listWkdataDetlC();
  }

  void queryLiacContractHst() throws Exception {
    this.selectNoLimit();

    daoTid = "I-";
    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "file_date, " + "id_no, "
        + "bank_code, " + "bank_name, " + "apply_date, " + "nego_e_date, " + "contract_e_date, "
        + "liac_install_cnt, " + "liac_int_rate, " + "install_s_date, " + "month_pay_amt, "
        + "credit_cont_amt, " + "cash_card_cont_amt, " + "credit_card_cont_amt, "
        + "total_cont_amt, " + "m_credit_cont_amt, " + "m_cash_card_cont_amt, "
        + "m_credit_card_cont_amt, " + "per_allocate_amt, " + "m_no_credit_rate, " + "pay_acct_no, "
        + "proc_flag ";

    wp.daoTable = "col_liac_contract_hst";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by file_date asc ";

    pageQuery();

    wp.setListCount(4);
    wp.notFound = "";
  }

  void queryLiacCourt() throws Exception {
    this.selectNoLimit();

    daoTid = "D-";
    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "apply_date, " + "id_no, "
        + "recv_date, " + "liac_doc_no, " + "court_name, " + "case_no, " + "is_allow, "
        + "user_remark, " + "crt_user, " + "crt_date, " + "apr_flag, " + "apr_date, " + "apr_user, "
        + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno ";

    wp.daoTable = "col_liac_court";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by liac_seqno, recv_date ";

    pageQuery();

    // wp.setListCount(4);
    wp.setListCount(5);
    wp.notFound = "";
    listWkdataDetlD();
  }

  void queryLiacPay() throws Exception {
    this.selectNoLimit();

    daoTid = "E-";
    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "pay_seqno, " + "file_date, "
        + "file_type, " + "bank_code, " + "apply_date, " + "reg_bank_no, " + "allocate_date, "
        + "allocate_amt, " + "liac_remark ";

    wp.daoTable = "col_liac_pay";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by liac_seqno, apply_date ";

    pageQuery();

    // wp.setListCount(5);
    wp.setListCount(6);
    wp.notFound = "";
    listWkdataDetlE();
  }

  void queryLiacDelay() throws Exception {
    this.selectNoLimit();

    daoTid = "F-";
    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "file_date, " + "query_date, "
        + "notify_date, " + "bank_code, " + "bank_name, " + "apply_date, " + "delay_month, "
        + "delay_reason, " + "delay_desc, " + "reg_bank_no, " + "liac_remark ";

    wp.daoTable = "col_liac_delay";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    // wp.setListCount(7);
    wp.setListCount(8);
    wp.notFound = "";
    listWkdataDetlF();
  }

  void queryLiacReceipt() throws Exception {
    this.selectNoLimit();

    daoTid = "G-";
    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "file_date, " + "notify_date, "
        + "receipt_type, " + "receipt_code, " + "per_allocate_amt, " + "m_no_credit_rate, "
        + "install_s_date, " + "liac_install_cnt, " + "liac_txn_code, " + "receipt_date, "
        + "substrb(install_s_date, 1, 6) db_inst_s_date, "
        + "substrb(install_s_date, 1, 6) chk_inst_s_date, " + "crt_user, " + "crt_date ";

    wp.daoTable = "col_liac_receipt";
    wp.whereStr = "where liac_seqno = :liac_seqno ";
    setString("liac_seqno", liacSeqno);
    wp.whereOrder = "order by liac_seqno, notify_date ";

    pageQuery();

    // wp.setListCount(8);
    wp.setListCount(9);
    wp.notFound = "";
    listWkdataDetlG();
  }

  // void queryLiacNego() throws Exception{
  // this.select_noLimit();
  //
  // daoTid ="H-";
  // wp.selectSQL = "hex(rowid) as rowid, "
  // + "liac_seqno, "
  // + "file_date, "
  // + "liac_status, "
  // + "query_date, "
  // + "notify_date, "
  // + "id_no, "
  // + "id_p_seqno, "
  // + "chi_name, "
  // + "bank_code, "
  // + "bank_name, "
  // + "apply_date, "
  // + "nego_s_date, "
  // + "stop_notify_date, "
  // + "interest_base_date, "
  // + "recol_reason, "
  // + "credit_flag, "
  // + "no_credit_flag, "
  // + "cash_card_flag, "
  // + "credit_card_flag, "
  // + "contract_date, "
  // + "liac_remark, "
  // + "end_date, "
  // + "end_reason, "
  // + "end_remark, "
  // + "liac_txn_code, "
  // + "reg_bank_no, "
  // + "id_data_date, "
  // + "court_agree_date, "
  // + "case_status, "
  // + "crt_date, "
  // + "crt_time, "
  // + "proc_flag, "
  // + "proc_date, "
  // + "apr_flag, "
  // + "apr_date, "
  // + "apr_user, "
  // + "end_user, "
  // + "mod_time, "
  // + "mod_pgm, "
  // + "mod_user, "
  // + "mod_seqno "
  // ;
  //
  // wp.daoTable = "col_liac_nego";
  // wp.whereStr = "where liac_seqno = :liac_seqno ";
  // setString("liac_seqno", kk1);
  // wp.whereOrder = "";
  //
  // pageQuery();
  //
  // wp.setListCount(8);
  // wp.notFound = "";
  // list_wkdata_detlH();
  // }

  void listWkdataDetl() throws Exception {
    String param = "", param2 = "";
    String[] cde = new String[] {"1", "2", "3", "4", "5","6","A", "B", "C", "D", "E", "F"};
    String[] txt = new String[] {"1.受理申請", "2.停催通知", "3.簽約完成", "4.結案/復催", "5.結案/毀諾","6.結案/結清", "A.基本資料異動",
        "B.法院認可", "C.延期繳款", "D.請求同意(Z96)", "E.簽約方案(Z98)", "F.單獨受償"};

    param = wp.colStr("liac_status");
    wp.colSet("tt_liac_status", commString.decode(param, cde, txt));

    param = wp.colStr("bank_code");
    param2 = wp.colStr("bank_name");
    wp.colSet("wk_bank_code_name", param + "_" + param2);

    param = wp.colStr("recol_reason");
    wp.colSet("tt_recol_reason", wfColLiabIdtabDesc(param, "5"));

    param = wp.colStr("case_status");
    wp.colSet("tt_case_status", commString.decode(param, ",1,2", ",1.遞狀聲請,2.法院裁定"));

    param = wp.colStr("liac_txn_code");
    wp.colSet("tt_liac_txn_code", commString.decode(param, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));
  }

  void listWkdataDetlA() throws Exception {
    String param = "", param2 = "";
    
    if (wp.selectCnt <= 0) {
    	 wp.colSet("a-static_text_style", "display:none");
	}
    
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "a-acct_type");
      param2 = wp.colStr(ii, "a-acct_key");
      wp.colSet(ii, "a-wk_acct_key", param + "-" + param2);

//      if (ii == 0) {
//        param = "<tr><td nowrap class='list_ll' colspan='14'><span style='color:#A23400'> ";
//        param += "***備註：道德風險評估說明：  <br>" + "&nbsp;&nbsp;&nbsp;　　 1:近半年繳評不足6個月 <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 2:近半年繳評1~2月為0A.0B.0C.0D.0E任2個，3~6月為0A.0B. 0E任4個，且current總欠 > 0 <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 3: ( current總欠 – 最近關帳欠款金額 ) > 信用額度 * 30% <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 Y:人工評估有道德風險之歷史案件 <br>"
//            + "&nbsp;&nbsp;&nbsp;　　 4:近半年單筆消費金額超過10,000(含)以上";
//        param += "&nbsp;</span></td></tr>";
//        wp.colSet("a-static_text", param);
//      }
    }
  }

  void listWkdataDetlB() throws Exception {
    String param = "";
    double lic, mpa, lpa;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "b-proc_flag");
      wp.colSet(ii, "b-tt_proc_flag", commString.decode(param, ",0,1,2,A", ",0.未報送,1.未報送,2.已報送,A.不須報送"));

      param = wp.colStr(ii, "b-reg_bank_no");
      wp.colSet(ii, "b-tt_reg_bank_no", wfPtrBranchName(param));

      param = wp.colStr(ii, "b-debt_reason");
      wp.colSet(ii, "b-tt_debt_reason", wfColLiabIdtabDesc(param, "6"));

      lic = wp.colNum(ii, "b-liac_install_cnt");
      mpa = wp.colNum(ii, "b-month_pay_amt");
      lpa = wp.colNum(ii, "b-lastest_pay_amt");
      wp.colSet(ii, "b-wk_est_allocate_amt", numToStr((lic * mpa) + lpa, ""));

      param = wp.colStr(ii, "b-liac_remark");
      wp.colSet(ii, "b-tt_liac_remark",
          commString.decode(param, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));

      wp.colSet(ii, "b-in_end_bal", numToStr(imInBal, ""));
      wp.colSet(ii, "b-out_end_bal", numToStr(imOutBal, ""));
    }
  }

  void listWkdataDetlC() throws Exception {
    String param = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "c-liac_install_cnt");
      wp.colSet(ii, "c-chk_liac_install_cnt", param);

      param = wp.colStr(ii, "c-liac_int_rate");
      wp.colSet(ii, "c-chk_liac_int_rate", param);

      param = wp.colStr(ii, "c-install_s_date");
      wp.colSet(ii, "c-chk_install_s_date", param);

      param = wp.colStr(ii, "c-month_pay_amt");
      wp.colSet(ii, "c-chk_month_pay_amt", param);

      param = wp.colStr(ii, "c-credit_cont_amt");
      wp.colSet(ii, "c-chk_credit_cont_amt", param);

      param = wp.colStr(ii, "c-cash_card_cont_amt");
      wp.colSet(ii, "c-chk_cash_card_cont_amt", param);

      param = wp.colStr(ii, "c-credit_card_cont_amt");
      wp.colSet(ii, "c-chk_credit_card_cont_amt", param);

      param = wp.colStr(ii, "c-total_cont_amt");
      wp.colSet(ii, "c-chk_total_cont_amt", param);

      param = wp.colStr(ii, "c-per_allocate_amt");
      wp.colSet(ii, "c-chk_per_allocate_amt", param);

      param = wp.colStr(ii, "c-m_no_credit_rate");
      wp.colSet(ii, "c-chk_m_no_credit_rate", param);

      param = wp.colStr(ii, "c-pay_acct_no");
      wp.colSet(ii, "c-chk_pay_acct_no", param);
    }

    if (wp.selectCnt > 0) {
      wp.colSet("btnUpdateC_disable", "");
    } else {
      wp.colSet("btnUpdateC_disable", "disabled");
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
    String param = "";
    double ftTolamt = 0;

    int selectCntE = wp.selectCnt;
    for (int ii = 0; ii < selectCntE; ii++) {
      param = wp.colStr(ii, "e-file_type");
      wp.colSet(ii, "e-tt_file_type", commString.decode(param, ",F,E", ",F.他行,E.本行"));

      ftTolamt += wp.colNum(ii, "e-allocate_amt");
    }

    if (selectCntE > 0) {
//      param = "<tr><td colspan='2'></td><td nowrap class='list_rr'>筆  數：</td>";
//      param += "<td nowrap class='list_rr'> " + selectCntE + "&nbsp;</td>";
//      param += "<td></td><td nowrap class='list_rr'>金額合計：</td>";
//      param += "<td nowrap class='list_rr' colspan='2'> " + numToStr(ftTolamt, "") + "&nbsp;</td>";
//      param += "<td></td></tr>";
//      wp.colSet("e-ft_tolcnt", param);
      wp.colSet("selectCntE", selectCntE);
      wp.colSet("ftTolamt", numToStr(ftTolamt, ""));
      wp.selectCnt = 1;
      wp.setListCount(7);
    }
  }

  void listWkdataDetlF() throws Exception {
    String param = "", param2 = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "f-delay_reason");
      param2 = wp.colStr(ii, "f-delay_desc");
      wp.colSet(ii, "f-wk_delay_desc", param + " " + param2);

      param = wp.colStr(ii, "f-liac_remark");
      wp.colSet(ii, "f-tt_liac_remark",
          commString.decode(param, ",A,C,D,X,Z", ",A.新增,C.異動,D.刪除,X.補件,Z.結案"));
    }

    // if (wp.selectCnt > 0) {
    // ss = "<tr><td colspan='2'></td><td nowrap class='list_rr'>筆 數：</td>";
    // ss += "<td nowrap class='list_rr'> "+wp.selectCnt+"&nbsp;</td>";
    // ss += "<td colspan='4'></td></tr>";
    // wp.col_set("f-wk_tolcnt", ss);
    // }
  }

  void listWkdataDetlG() throws Exception {
    String param = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      param = wp.colStr(ii, "g-crt_user");
      wp.colSet(ii, "g-tt_crt_user", wfSecUserIDName(param));

      param = wp.colStr(ii, "g-receipt_code");
      wp.colSet(ii, "g-tt_receipt_code", wfColLiabIdtabDesc(param, "7"));

      if (ii == (wp.selectCnt - 1)) {
        // wp.col_set(ii,"g-col_inst_readonly", "");
        wp.colSet(ii, "g-dsp_inst", "none");
        wp.colSet(ii, "g-col_inst", "");
      } else {
        // wp.col_set(ii,"g-col_inst_readonly", "readOnly");
        wp.colSet(ii, "g-dsp_inst", "");
        wp.colSet(ii, "g-col_inst", "none");
      }
    }

    if (wp.selectCnt > 0) {
      wp.colSet("btnUpdateG_disable", "");
    } else {
      wp.colSet("btnUpdateG_disable", "disabled");
    }
  }

  void listWkdataDetlH() throws Exception {
    wp.colSet("h-rowid", wp.colStr("rowid"));
    wp.colSet("h-mod_seqno", wp.colStr("mod_seqno"));
    wp.colSet("h-end_date", wp.colStr("end_date"));
    wp.colSet("h-end_user", wp.colStr("end_user"));
    wp.colSet("h-end_reason", wp.colStr("end_reason"));
    wp.colSet("h-end_remark", wp.colStr("end_remark"));
    wp.colSet("h-apr_flag", wp.colStr("apr_flag"));
    wp.colSet("h-apr_date", wp.colStr("apr_date"));
    wp.colSet("h-apr_user", wp.colStr("apr_user"));

    String lsIdPSeqno = wp.colStr("id_p_seqno");
    double tolAmt = wfGetTolAmt(lsIdPSeqno);
    wp.colSet("h-db_tol_amt", numToStr(tolAmt, ""));
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

  // 說明: 取得回報債權等相關金額。
  int wfGetDebtBal(String asLiacSeqno) throws Exception {
    String lsLiacSeqno = "";

    // double im_in_bal =0;
    // double im_out_bal =0;
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
    func = new Colm1100Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    if (strAction.equals("D") == false) {
      strAction = "R";
      dataRead();
    }
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

  /*
   * void ue_callbatch(String aa_run) throws IOException{ String sendData ="",msg="",ls_run="";
   * InetAddress sAddress= InetAddress.getByName(wp.request.getRemoteAddr());
   * 
   * ls_run = aa_run; String lsSql =
   * " select wf_value,wf_value2 from PTR_sys_parm where wf_parm='SYSPARM' and wf_key = 'CALLBATCH' "
   * ; sqlSelect(lsSql); //ip and port String host = sql_ss("wf_value"); int port = (int)
   * sql_num("wf_value2");
   * 
   * //seqno String ls_mod_seqno = " select ecs_modseq.nextval AS MOD_SEQNO from dual ";
   * sqlSelect(ls_mod_seqno);
   * 
   * String MOD_SEQNO = sql_ss("MOD_SEQNO");
   * 
   * //傳送的參數 sendData=ls_run.substring(0,3)+"."+ls_run+" "+String.format("%020d",
   * Long.valueOf(MOD_SEQNO));
   * 
   * 
   * //insert ptr_callbatch String ls_ins = "insert into ptr_callbatch ( " + " batch_seqno,  " +
   * "program_code, " + "start_date," + "user_id," + "workstation_name," + "client_program," +
   * "parameter_data" + ")values(  " +" :batch_seqno," +" :program_code," +" :start_date,"
   * +" :user_id," +" :workstation_name," +" :client_program, " +" :parameter_data ) ";
   * setString("batch_seqno",String.format("%020d", Long.valueOf(MOD_SEQNO)));
   * setString("program_code", ls_run); setString("start_date", get_sysDate()); setString("user_id",
   * wp.loginUser); setString("workstation_name", sAddress.getHostName());
   * setString("client_program", wp.item_ss("MOD_PGM")); setString("parameter_data", sendData);
   * sqlExec(ls_ins); if (sql_nrow <= 0) { msg =" ERROR:insert ptr_callbatch"; sql_commit(0);
   * ll_err++; }else{ sql_commit(1); } Socket socket = null; //String sendData =
   * "BilA001 1061101  NUATMTP10"; //String sendData =
   * "EDS 'CHACHING2' w_crdp0040 /usr/bin/ksh /BANK/CR/ecs/shell/test1.sh 0000000000111";
   * //System.getenv("PROJ_HOME"); try { socket = new Socket(host, port); DataInputStream input =
   * null; DataOutputStream output = null; msg +="Starting...  \n";
   * 
   * try { while (true) { output = new DataOutputStream( socket.getOutputStream() );
   * msg+="Send data : [" + sendData + "] \n"; output.write(sendData.getBytes());
   * //output.writeUTF(sendData); output.flush();
   * 
   * input = new DataInputStream( socket.getInputStream() ); int inputLen = 0; byte[] inData = new
   * byte[2048];
   * 
   * inputLen = input.read(inData, 0, inData.length); if(inputLen > 0){ msg+="response data : [" +
   * new String(inData, 0, inputLen) + "] \n"; }else{ msg+="無回傳資料   \n"; ll_err++; } break; } }
   * catch (Exception e) { msg+="Exception : " + e.getMessage()+"  \n"; ll_err++; } finally { if
   * (input != null) input.close(); if (output != null) output.close(); msg+="Terminated..\n"; } }
   * catch (IOException e) { msg+="Exception2 : " + e.getMessage()+"  \n"; ll_err++;
   * e.printStackTrace(); } finally { if (socket != null) socket.close(); // if(consoleInput != null
   * ) consoleInput.close(); msg+="Socked Closed...  \n "; } if(ll_err>0){ //
   * alert_msg("啟動批次程式["+ls_run+"]: callbatch 失敗"); wp.dispMesg =
   * "啟動批次程式["+ls_run+"]: callbatch 失敗"; }else{ wp.dispMesg =
   * "啟動批次程式["+ls_run+"]: callbatch 成功, 序號:"+String.format("%020d", Long.valueOf(MOD_SEQNO)); } //
   * wp.col_set("msg", msg); }
   */

  // 說明:執行線上主管覆核。
  int wfApr() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return -1;
    }
    // 問題單 0000426: 2. 檢查線上主管審核，若通過，是否寫入apr_date、apr_user、apr_flag(若有此欄位)等欄位值。
    func.varsSet("apr_user", wp.itemStr("approval_user"));
    func.varsSet("apr_date", wp.sysDate);
    func.varsSet("apr_flag", "Y");
    return 1;
  }

  // 說明:
  // 1. 檢查資料是否可以進行【取消結案/復催】功能。
  // 2. 若可以進行，則經線上主管覆核後，新增一筆col_liac_rmrecol。
  // 3. 呼叫執行線上批次功能。批次:col_a470
  int wfCloseCancel() throws Exception {
    String lsStatus, lsLiacSeqno, lsId, lsIdPSeqno, lsSql;
    long llCnt;

    func = new Colm1100Func(wp);

    // Detail Control
    detailControl();

    lsStatus = wp.itemStr("liac_status");
    if (!eqIgno(lsStatus, "4")) {
      alertErr("前置協商狀態 非[結案/復催], 不可取消");
      return -1;
    }

    lsLiacSeqno = wp.itemStr("liac_seqno");
    // -check batch process-
    lsSql = "select count(*) as ll_cnt " + "from col_liac_rmrecol "
        + "where liac_seqno = :liac_seqno " + "and   proc_flag='N' ";
    setString("liac_seqno", lsLiacSeqno);
    sqlSelect(lsSql);
    llCnt = (long) sqlNum("ll_cnt");
    if (llCnt > 0) {
      alertErr("已登錄 取消[結案/復催]處理, 不可重複登錄");
      return -1;
    }

    // -approve-
    if (wfApr() != 1)
      return -1;

    // -insert COL_LIAC_RMRECOL-
    lsId = wp.itemStr("id_no");
    lsIdPSeqno = wp.itemStr("id_p_seqno");

    func.varsSet("id_no", lsId);
    func.varsSet("id_p_seqno", lsIdPSeqno);
    func.varsSet("rmrecol_date", wp.sysDate);
    func.varsSet("liac_seqno", lsLiacSeqno);
    func.varsSet("proc_date", "");
    func.varsSet("proc_flag", "N");
    if (func.insertColLiacRmrecol() < 0) {
      alertErr("insert COL_LIAC_RMRECOL error，記錄失敗 ?!");
      sqlCommit(0);
      return -1;
    }
    sqlCommit(1);
    // wp.alertMesg = "<script language='javascript'> alert('取消[結案/復催]處理成功 。')</script>";

    // ue_callbatch("ColA470"); //不再使用 by phopho 2018.10.19
    fCallBatch("ColA470");

    return 1;
  }

  // 說明:執行【變更還款方案】的維護功能。
  // 1. 可異動資料內容合理性檢核。
  // 2. Update col_liac_contract (前置協商合約檔)。
  // Update 內容，為【可異動之欄位】加上mod_user、mod_time、 mod_pgm、mod_seqno。
  // 3. Insert 一筆 col_liac_contract_hst (前置協商合約歷史檔)，內容為複製修改後的此筆col_liac_contract資料。
  // 4. 若liac_int_rate(利率) 有變更，執行【doActAcnoUpdate】。
  void doLiacContractUpdate() throws Exception {
    String lsRowid;
    String lsLiacInstallCnt, lsLiacIntRate, lsInstallSDate, lsMonthPayAmt;
    String lsCreditContAmt, lsCashCardContAmt, lsCreditCardContAmt, lsTotalContAmt;
    String lsPerAllocateAmt, lsMNoCreditRate, lsPayAcctNo;
    String lsChkLiacInstallCnt, lsChkLiacIntRate, lsChkInstallSDate, lsChkMonthPayAmt;
    String lsChkCreditContAmt, lsChkCashCardContCmt, lsChkCreditCardContAmt, lsChkTotalContAmt;
    String lsChkPerAllocateAmt, lsChkMNoCreditRate, lsChkPayAcctNo;

    func = new Colm1100Func(wp);

    // Detail Control
    detailControl();

    // 原則上合約主檔只會有一筆, 萬一需要多筆存檔時, 以後再修改. todo
    lsLiacInstallCnt = wp.itemStr("c-liac_install_cnt");
    lsLiacIntRate = wp.itemStr("c-liac_int_rate");
    lsInstallSDate = wp.itemStr("c-install_s_date");
    lsMonthPayAmt = wp.itemStr("c-month_pay_amt");
    lsCreditContAmt = wp.itemStr("c-credit_cont_amt");
    lsCashCardContAmt = wp.itemStr("c-cash_card_cont_amt");
    lsCreditCardContAmt = wp.itemStr("c-credit_card_cont_amt");
    lsTotalContAmt = wp.itemStr("c-total_cont_amt");
    lsPerAllocateAmt = wp.itemStr("c-per_allocate_amt");
    lsMNoCreditRate = wp.itemStr("c-m_no_credit_rate");
    lsPayAcctNo = wp.itemStr("c-pay_acct_no");

    lsChkLiacInstallCnt = wp.itemStr("c-chk_liac_install_cnt");
    lsChkLiacIntRate = wp.itemStr("c-chk_liac_int_rate");
    lsChkInstallSDate = wp.itemStr("c-chk_install_s_date");
    lsChkMonthPayAmt = wp.itemStr("c-chk_month_pay_amt");
    lsChkCreditContAmt = wp.itemStr("c-chk_credit_cont_amt");
    lsChkCashCardContCmt = wp.itemStr("c-chk_cash_card_cont_amt");
    lsChkCreditCardContAmt = wp.itemStr("c-chk_credit_card_cont_amt");
    lsChkTotalContAmt = wp.itemStr("c-chk_total_cont_amt");
    lsChkPerAllocateAmt = wp.itemStr("c-chk_per_allocate_amt");
    lsChkMNoCreditRate = wp.itemStr("c-chk_m_no_credit_rate");
    lsChkPayAcctNo = wp.itemStr("c-chk_pay_acct_no");

    // System.out.println("7777==> ls_liac_int_rate="+ls_liac_int_rate+",
    // ls_chk_liac_int_rate="+ls_chk_liac_int_rate);
    if ((commString.strToNum(lsChkLiacInstallCnt) == commString.strToNum(lsLiacInstallCnt))
        && (commString.strToNum(lsChkLiacIntRate) == commString.strToNum(lsLiacIntRate))
        && eqIgno(lsChkInstallSDate, lsInstallSDate)
        && (commString.strToNum(lsChkMonthPayAmt) == commString.strToNum(lsMonthPayAmt))
        && (commString.strToNum(lsChkCreditContAmt) == commString.strToNum(lsCreditContAmt))
        && (commString.strToNum(lsChkCashCardContCmt) == commString.strToNum(lsCashCardContAmt))
        && (commString.strToNum(lsChkCreditCardContAmt) == commString.strToNum(lsCreditCardContAmt))
        && (commString.strToNum(lsChkTotalContAmt) == commString.strToNum(lsTotalContAmt))
        && (commString.strToNum(lsChkPerAllocateAmt) == commString.strToNum(lsPerAllocateAmt))
        && (commString.strToNum(lsChkMNoCreditRate) == commString.strToNum(lsMNoCreditRate))
        && eqIgno(lsChkPayAcctNo, lsPayAcctNo)) {
      alertErr("還款方案 未異動, 不須存檔");
      return;
    }

    // 利率檢核不可超過15% phopho 2019.9.26
    if (commString.strToNum(lsLiacIntRate) > 15) {
      alertErr("利率不可超過15%");
      return;
    }

    // -approve- //依user要求加入 2019.9.26
    if (wfApr() != 1)
      return;

    // -update col_liac_contract-
    lsRowid = wp.itemStr("c-rowid");
    func.varsSet("liac_install_cnt", String.valueOf(commString.strToNum(lsLiacInstallCnt)));
    func.varsSet("liac_int_rate", String.valueOf(commString.strToNum(lsLiacIntRate)));
    func.varsSet("install_s_date", lsInstallSDate);
    func.varsSet("month_pay_amt", String.valueOf(commString.strToNum(lsMonthPayAmt)));
    func.varsSet("credit_cont_amt", String.valueOf(commString.strToNum(lsCreditContAmt)));
    func.varsSet("cash_card_cont_amt", String.valueOf(commString.strToNum(lsCashCardContAmt)));
    func.varsSet("credit_card_cont_amt", String.valueOf(commString.strToNum(lsCreditCardContAmt)));
    func.varsSet("total_cont_amt", String.valueOf(commString.strToNum(lsTotalContAmt)));
    func.varsSet("per_allocate_amt", String.valueOf(commString.strToNum(lsPerAllocateAmt)));
    func.varsSet("m_no_credit_rate", String.valueOf(commString.strToNum(lsMNoCreditRate)));
    func.varsSet("pay_acct_no", lsPayAcctNo);
    func.varsSet("rowid", lsRowid);
    if (func.updateColLiacContract() < 0) {
      alertErr("修改 還款方案 失敗");
      sqlCommit(0);
      return;
    }
    func.insertColLiacContractHst();
    wp.colSet("c-chk_liac_install_cnt", lsLiacInstallCnt);
    wp.colSet("c-chk_liac_int_rate", lsLiacIntRate);
    wp.colSet("c-chk_install_s_date", lsInstallSDate);
    wp.colSet("c-chk_month_pay_amt", lsMonthPayAmt);
    wp.colSet("c-chk_credit_cont_amt", lsCreditContAmt);
    wp.colSet("c-chk_cash_card_cont_amt", lsCashCardContAmt);
    wp.colSet("c-chk_credit_card_cont_amt", lsCreditCardContAmt);
    wp.colSet("c-chk_total_cont_amt", lsTotalContAmt);
    wp.colSet("c-chk_per_allocate_amt", lsPerAllocateAmt);
    wp.colSet("c-chk_m_no_credit_rate", lsMNoCreditRate);
    wp.colSet("c-chk_pay_acct_no", lsPayAcctNo);
    sqlCommit(1);
    wp.alertMesg = "<script language='javascript'> alert('修改 還款方案 成功 。')</script>";

    // 若liac_int_rate(利率) 有變更，執行【doActAcnoUpdate】。
    if (commString.strToNum(lsChkLiacIntRate) != commString.strToNum(lsLiacIntRate))
      doActAcnoUpdate(lsLiacIntRate);

    return;
  }

  // 說明:如果利率變更，檢查【COL_LIAB_PARAM 債務協商狀態參數檔】，
  // 若revolve_rate_flag(帳戶循環信用利率旗標)為’Y’，需更新act_acno.revolve_int_rate 欄位內容。
  void doActAcnoUpdate(String asLiacIntRate) throws Exception {
    String lsIdPSeqno, lsLiacStatus, lsRevolveRateFlag;

    lsIdPSeqno = wp.itemStr("id_p_seqno");
    lsLiacStatus = wp.itemStr("liac_status");
    String lsSql = "select revolve_rate_flag from col_liab_param "
        + "where apr_date <> '' and liab_status = :liab_status and liab_type = '2' ";
    setString("liab_status", lsLiacStatus);
    sqlSelect(lsSql);
    if (sqlRowNum == 0)
      return;

    // remark: 計算新利率時會讀取 crd_idno 及 act_acno. 處理時間相當久
    lsRevolveRateFlag = sqlStr("revolve_rate_flag");
    if (eqIgno(lsRevolveRateFlag, "Y")) {
      func.varsSet("id_p_seqno", lsIdPSeqno);
      func.varsSet("liac_int_rate", asLiacIntRate);
      if (func.updateActAcno() < 0) {
        alertErr("更新 帳戶循環信用利率 失敗");
        // sql_commit(0);
        return;
      }
    }
  }

  // 說明:
  // 1. 檢查是否可以進行修改。
  // 2. 若可以修改，進行線上主管覆核。
  // 3. 前項若通過，則更新col_liac_receipt資料。
  void wfReceiptUpd() throws Exception {
    String lsRowid, lsChkInstDate, lsDbInstDate;

    func = new Colm1100Func(wp);

    // Detail Control
    detailControl();

    if (rowcntgg == 0) {
      alertErr("無單獨受償資料");
      return;
    }

    lsChkInstDate = wp.itemStr(rowcntgg - 1, "g-chk_inst_s_date");
    lsDbInstDate = wp.itemStr(rowcntgg - 1, "g-db_inst_s_date");
    if (empty(lsDbInstDate)) {
      alertErr("單獨受償之適用起始月份 不可空白");
      return;
    }
    if (eqIgno(lsChkInstDate, lsDbInstDate)) {
      alertErr("單獨受償之適用起始月份 未異動, 不須存檔");
      return;
    }

    // -approve-
    if (wfApr() != 1)
      return;

    // -update col_liac_receipt-
    lsRowid = wp.itemStr(rowcntgg - 1, "g-rowid");

    func.varsSet("install_s_date", lsDbInstDate + "01");
    func.varsSet("proc_flag", "2");
    func.varsSet("rowid", lsRowid);
    if (func.updateColLiacReceipt() < 0) {
      alertErr("修改 單獨受償之適用起始月份 失敗");
      sqlCommit(0);
      return;
    }
    // wp.col_set(rowcntgg-1,"g-chk_inst_s_date", ls_db_inst_date+"01");//??
    wp.colSet(rowcntgg - 1, "g-chk_inst_s_date", lsDbInstDate);
    sqlCommit(1);
    wp.alertMesg = "<script language='javascript'> alert('修改 單獨受償之適用起始月份 成功 。')</script>";

    // ue_callbatch("ColA429"); //不再使用 by phopho 2018.10.19
    fCallBatch("ColA429");

    return;
  }

  // 說明: 執行結案功能。
  // 1. 檢查資料。
  // 2. 更新col_liac_nego。
  void wfEnd() throws Exception {
    double ldcAmt, lmAmt2;
    String lsEndReason, lsEndRemark;
    String lsRowid, lsModSeqno;

    func = new Colm1100Func(wp);

    // Detail Control
    detailControl();

    // 【問題單1283】【5-結案；結清】時不檢核 2019.10.04 phopho
    // ldc_amt = to_Num(wp.item_ss("h-db_tol_amt").replaceAll(",",""));
    // lm_amt2 = to_Num(wp.item_ss("db_in_bal").replaceAll(",",""));
    //
    // if (ldc_amt > 0 && lm_amt2 > 0){
    // alert_err("有欠款且回報債權對內金額 >0, 不可結案");
    // return;
    // }

    if (eqIgno(wp.itemStr("h-apr_flag"), "Y")) {
      alertErr("主管已覆核 不可修改");
      return;
    }

    lsEndReason = wp.itemStr("h-end_reason");
    lsEndRemark = wp.itemStr("h-end_remark");
    if (empty(lsEndReason) || empty(lsEndRemark)) {
      alertErr("結案原因, 備註 不可空白");
      return;
    }

    // -update col_liac_receipt-
    lsRowid = wp.itemStr("h-rowid");
    lsModSeqno = wp.itemStr("h-mod_seqno");

    func.varsSet("end_date", wp.sysDate);
    func.varsSet("end_reason", lsEndReason);
    func.varsSet("end_remark", lsEndRemark);
    func.varsSet("end_user", wp.loginUser);
    func.varsSet("apr_flag", "N");
    func.varsSet("apr_date", "");
    func.varsSet("apr_user", "");
    func.varsSet("rowid", lsRowid);
    func.varsSet("mod_seqno", lsModSeqno);

    if (func.doEndColLiacNego() < 0) {
      alertErr("執行[結案]失敗 ");
      sqlCommit(0);
      return;
    }
    sqlCommit(1);
    wp.alertMesg = "<script language='javascript'> alert('[結案]執行成功 。')</script>";
    strAction = "R";
    dataRead();

    return;
  }

  // 說明: 執行取消結案功能。
  // 1. 檢查資料。
  // 2. 更新col_liac_nego。
  void wfCancelEnd() throws Exception {
    String lsEndRemark, lsEndDate;
    String lsRowid, lsModSeqno;

    func = new Colm1100Func(wp);

    // Detail Control
    detailControl();

    lsEndDate = wp.itemStr("h-end_date");
    if (empty(lsEndDate)) {
      alertErr("資料未結案, 不須 [取消結案]");
      return;
    }
    if (eqIgno(wp.itemStr("h-apr_flag"), "Y")) {
      alertErr("主管已覆核 不可再異動");
      return;
    }

    // -update col_liac_receipt-
    lsEndRemark = wp.itemStr("h-end_remark");
    lsRowid = wp.itemStr("h-rowid");
    lsModSeqno = wp.itemStr("h-mod_seqno");

    func.varsSet("end_date", "");
    func.varsSet("end_reason", "");
    func.varsSet("end_remark", lsEndRemark);
    func.varsSet("end_user", "");
    func.varsSet("apr_flag", "N");
    func.varsSet("apr_date", "");
    func.varsSet("apr_user", "");
    func.varsSet("rowid", lsRowid);
    func.varsSet("mod_seqno", lsModSeqno);
    if (func.doEndColLiacNego() < 0) {
      alertErr("執行[取消結案]失敗 ");
      sqlCommit(0);
      return;
    }
    sqlCommit(1);
    wp.alertMesg = "<script language='javascript'> alert('[取消結案]執行成功 。')</script>";
    strAction = "R";
    dataRead();

    return;
  }

  public void addCourtKey() throws Exception {
    kkLiacSeqno = wp.itemStr("liac_seqno");
    kkApplyDate = wp.itemStr("apply_date");
    kkIdNo = wp.itemStr("id_no");
  }

  public void setCourtKey() throws Exception {
    wp.colSet("liac_seqno", kkLiacSeqno);
    wp.colSet("apply_date", kkApplyDate);
    wp.colSet("id_no", kkIdNo);
  }

  // 1. 由【法院公文】頁籤之資料列表導入，
  // 若存在尚未執行覆核之資料，則取得尚未覆核之公文資料。
  // 若，不存在尚未覆核之資料，則取得已經覆核之資料。
  // 2. 查詢條件:
  // a. 以【liac_seqno】、【recv_date】為查詢條件，檢查是否存在尚未執行覆核之資料。再判斷帶入之查詢條件。
  // 3. 顯示結果頁面，
  // a. 若資料的【主管覆核(apr_flag)】為【Y】，則顯示:【刪除公文】 按鈕，隱藏【修改取消】按鈕。
  // b. 若資料的【主管覆核(apr_flag)】為【N】，則顯示: 修改取消】 按鈕，隱藏【刪除公文】按鈕。
  public void dataReadCourt() throws Exception {
    String lsSql, isWhere, lsFlag;
    liacSeqno = wp.itemStr("data_k1");
    recvDate = wp.itemStr("data_k2");

    isWhere = " and liac_seqno = :liac_seqno " + " and recv_date = :recv_date ";
    setString("liac_seqno", liacSeqno);
    setString("recv_date", recvDate);
    lsFlag = " and apr_flag <> 'Y' ";

    lsSql = "select count(*) as ll_cnt " + "from col_liac_court " + "where 1=1 " + isWhere + lsFlag;
    sqlSelect(lsSql);
    long llCnt = (long) sqlNum("ll_cnt");
    if (llCnt == 0) {
      lsFlag = " and apr_flag = 'Y' ";
    }

    wp.selectSQL = "hex(rowid) as rowid, " + "liac_seqno, " + "apply_date, " + "id_no, "
        + "recv_date, " + "liac_doc_no, " + "court_name, " + "case_no, " + "is_allow, "
        + "user_remark, " + "crt_user, " + "crt_date, " + "apr_flag, " + "apr_date, " + "apr_user, "
        + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno ";
    wp.daoTable = "col_liac_court";
    wp.whereStr = "where 1=1 " + isWhere + lsFlag;
    setString("liac_seqno", liacSeqno);
    setString("recv_date", recvDate);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + liacSeqno + "," + recvDate);
      return;
    }
    // list_wkdata_court();
  }

  void listWkdataCourt() throws Exception {

  }

  // 說明:刪除特定條件下尚未覆核的法院公文，再新增一筆法院公文資料。
  int courtUpdate() throws Exception {
    func = new Colm1100Func(wp);

    String lsSeqno, lsDate, lsApplyDate, lsId, lsIdPSeqno;
    String lsCourtName, lsDocNo, lsCaseNo, lsAllow, lsRemark;

    lsSeqno = wp.itemStr("liac_seqno");
    lsApplyDate = wp.itemStr("apply_date");
    lsId = wp.itemStr("id_no");
    lsIdPSeqno = wp.itemStr("id_p_seqno");
    lsDate = wp.itemStr("recv_date");

    if (func.doDeleteColLiacCourt("U") < 0) {
      alertErr("刪除[法院公文]失敗 ");
      sqlCommit(0);
      return -1;
    }

    lsCourtName = wp.itemStr("court_name");
    lsDocNo = wp.itemStr("liac_doc_no");
    lsCaseNo = wp.itemStr("case_no");
    lsAllow = wp.itemStr("is_allow");
    lsRemark = wp.itemStr("user_remark");

    func.varsSet("liac_seqno", lsSeqno);
    func.varsSet("apply_date", lsApplyDate);
    func.varsSet("id_no", lsId);
    func.varsSet("id_p_seqno", lsIdPSeqno);
    func.varsSet("recv_date", lsDate);
    func.varsSet("court_name", lsCourtName);
    func.varsSet("liac_doc_no", lsDocNo);
    func.varsSet("case_no", lsCaseNo);
    func.varsSet("is_allow", lsAllow);
    func.varsSet("user_remark", lsRemark);
    func.varsSet("apr_flag", "N");
    func.varsSet("apr_date", "");
    func.varsSet("apr_user", "");

    if (func.doInsertColLiacCourt() < 0) {
      alertErr("執行[存檔]失敗 ");
      sqlCommit(0);
      return -1;
    }
    wp.respMesg = "資料存檔成功";
    sqlCommit(1);
    return 1;
  }

  // 說明:【修改取消】尚未覆核之法院公文。
  int courtSave() throws Exception {
    func = new Colm1100Func(wp);

    if (func.doDeleteColLiacCourt("U") < 0) {
      alertErr("執行[修改取消]失敗 ");
      sqlCommit(0);
      return -1;
    }
    wp.respMesg = "[修改取消]成功";
    sqlCommit(1);
    return 1;
  }

  // 說明:經線上主管覆核後，刪除法院公文。
  int courtDelete() throws Exception {
    func = new Colm1100Func(wp);

    String lsSeqno, lsDate, lsSql;
    long llCnt;

    lsSeqno = wp.itemStr("liac_seqno");
    lsDate = wp.itemStr("recv_date");
    lsSql =
        "select count(*) as ll_cnt " + "from col_liac_court " + "where liac_seqno = :liac_seqno "
            + "and   recv_date = :recv_date " + "and   apr_flag<>'N' ";
    setString("liac_seqno", lsSeqno);
    setString("recv_date", lsDate);
    sqlSelect(lsSql);
    llCnt = (long) sqlNum("ll_cnt");
    if (llCnt > 0) {
      alertErr("資料已有異動, 請先取消異動再刪除");
      return -1;
    }

    if (wfApr() != 1)
      return -1;
    // lstr_appr.winid = "w_colp1105" <== 要導到這支嗎? //todo
    // lstr_appr.moduser = gnv_app.of_getuserid()
    // of_approve(lstr_appr)

    if (func.doDeleteColLiacCourt("D") < 0) {
      alertErr("刪除[法院公文]失敗 ");
      sqlCommit(0);
      return -1;
    }
    wp.respMesg = "刪除[法院公文]成功";
    sqlCommit(1);
    return 1;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void detailControl() {
    rowcntaa = 0;
    rowcntbb = 0;
    rowcntcc = 0;
    rowcntii = 0;
    rowcntdd = 0;
    rowcntee = 0;
    rowcntff = 0;
    rowcntgg = 0;
    String[] aaLiacSeqno = wp.itemBuff("a-liac_seqno");
    String[] bbLiacSeqno = wp.itemBuff("b-liac_seqno");
    String[] ccLiacSeqno = wp.itemBuff("c-liac_seqno");
    String[] iiLiacSeqno = wp.itemBuff("i-liac_seqno");
    String[] ddLiacSeqno = wp.itemBuff("d-liac_seqno");
    String[] eeLiacSeqno = wp.itemBuff("e-liac_seqno");
    String[] ffLiacSeqno = wp.itemBuff("f-liac_seqno");
    String[] ggLiacSeqno = wp.itemBuff("g-liac_seqno");
    if (!(aaLiacSeqno == null) && !empty(aaLiacSeqno[0]))
      rowcntaa = aaLiacSeqno.length;
    if (!(bbLiacSeqno == null) && !empty(bbLiacSeqno[0]))
      rowcntbb = bbLiacSeqno.length;
    if (!(ccLiacSeqno == null) && !empty(ccLiacSeqno[0]))
      rowcntcc = ccLiacSeqno.length;
    if (!(iiLiacSeqno == null) && !empty(iiLiacSeqno[0]))
      rowcntii = iiLiacSeqno.length;
    if (!(ddLiacSeqno == null) && !empty(ddLiacSeqno[0]))
      rowcntdd = ddLiacSeqno.length;
    if (!(eeLiacSeqno == null) && !empty(eeLiacSeqno[0]))
      rowcntee = eeLiacSeqno.length;
    if (!(ffLiacSeqno == null) && !empty(ffLiacSeqno[0]))
      rowcntff = ffLiacSeqno.length;
    if (!(ggLiacSeqno == null) && !empty(ggLiacSeqno[0]))
      rowcntgg = ggLiacSeqno.length;
    wp.listCount[0] = rowcntaa;
    wp.listCount[1] = rowcntbb;
    wp.listCount[2] = rowcntcc;
    wp.listCount[3] = rowcntii;
    wp.listCount[4] = rowcntdd;
    wp.listCount[5] = rowcntee;
    wp.listCount[6] = rowcntff;
    wp.listCount[7] = rowcntgg;
  }

}
