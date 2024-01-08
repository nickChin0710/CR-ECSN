/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/23  V1.00.00   phopho     program initial                           *
*  109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/

package dbar01;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Dbar0060 extends BaseReport {
  CommString commString = new CommString();
  String progName = "dbar0060";
  String messageBox = "";
  String acctMonth = "", acctTypeKey = "", cardTitle = "", corpChiName = "";
  String pSeqno = "", acctType = "", acctKey = "", cardNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      // dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      wp.colSet("queryReadCnt", "0");
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      // is_action = "R";
      // dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
      xlsPrint();
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
      wp.optionKey = wp.itemStr("exAcctType");
      dddwList("DbpAcctTypeList", "dbp_acct_type", "acct_type", "chin_name",
          "where 1=1 order by acct_type");
    } catch (Exception ex) {
    }
  }

  // private boolean getWhereStr() throws Exception {
  // if (empty(wp.item_ss("exAcctKey")) && empty(wp.item_ss("exCardNo"))) {
  // err_alert("請輸入[帳戶帳號]或[卡號]!");
  // return false;
  // }
  //
  // wp.whereStr = "where 1=1 "
  // + "and dba_acno.p_seqno = dbc_card.p_seqno "
  // + "and dbc_card.id_p_seqno = dbc_idno.id_p_seqno ";
  //
  // if(empty(wp.item_ss("exAcctKey")) == false){
  // wp.whereStr += " and dba_acno.acct_type = :acct_type ";
  // wp.whereStr += " and dba_acno.acct_key like :acct_key ";
  // setString("acct_type", wp.item_ss("exAcctType"));
  // setString("acct_key", wp.item_ss("exAcctKey")+"%");
  // } else {
  // wp.whereStr += " and dbc_card.card_no = :card_no ";
  // setString("card_no", wp.item_ss("exCardNo"));
  // }
  //
  // //-page control-
  // wp.queryWhere = wp.whereStr;
  // return true;
  // }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    pSeqno = wp.itemStr("data_k1");
    acctType = wp.itemStr("data_k2");
    acctKey = wp.itemStr("data_k3");
    if (empty(pSeqno)) {
      if (getPseqnobyAcctKey() < 0)
        return;
    }

    // if (getWhereStr() == false)
    // return;

    // TAG9000 程序：
    // String ls_key = "";
    // ls_key = !empty(wp.item_ss("exAcctKey"))? kk3 : kk4;
    // if (f_auth_query_vd(ls_key,m_progName)==false) {
    // err_alert(messageBox);
    // return;
    // }

    wp.selectSQL = "d.p_seqno, " + "d.acct_type, " + "d.acct_no, " + "d.id_p_seqno, "
        + "d.debt_status, " + "d.trans_col_date, " + "d.trans_bad_date, " + "b.card_no, "
        + "b.txn_code, " + "b.dest_amt, " + "b.dest_curr, " + "b.source_amt, " + "b.source_curr, "
        + "b.mcht_eng_name, " + "b.mcht_city, " + "b.mcht_country, " + "b.mcht_chi_name, "
        + "b.bill_type, " + "b.purchase_date, " + "b.interest_date, " + "b.exchange_date, "
        + "b.billed_date, " + "b.rsk_post, " + "b.rsk_problem1_mark, " + "b.rsk_receipt_mark, "
        + "b.rsk_chgback_mark, " + "b.acct_code, " + "b.reference_no, " + "b.curr_adjust_amt, "
        + "b.process_date, " + "b.acct_month ";

    wp.daoTable = "dbb_bill b, dba_debt d";

    wp.whereStr = "where (d.reference_no = b.reference_no) " + "and d.debt_status > '2' "
        + "and d.p_seqno = :p_seqno ";
    setString("p_seqno", pSeqno);

    wp.whereOrder = "order by b.acct_month, b.reference_no ";

    if (strAction.equals("XLS")) {
      selectNoLimit();
    }

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      wp.colSet("data_k1", "");
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String all = "";
    // String cardChiName = "", corpChiName="";

    int recordCnt = wp.selectCnt;
    for (int ii = 0; ii < recordCnt; ii++) {

      if (ii == 0) {
        acctMonth = wp.colStr(ii, "acct_month");
        cardNo = wp.colStr(ii, "card_no");
      }

      all = wp.colStr(ii, "acct_code");
      if (!all.equals("BL") && !all.equals("IT") && !all.equals("ID") && !all.equals("CA")
          && !all.equals("AO") && !all.equals("AF"))
        wp.colSet(ii, "card_no", "");

      all = wp.colStr(ii, "mcht_chi_name");
      wp.colSet(ii, "wk_mchtname", all.equals("") ? wp.colStr(ii, "mcht_eng_name") : all);

      all = wp.colStr(ii, "source_curr");
      if (all.equals("901") || all.equals(""))
        wp.colSet(ii, "process_date", "");

      wp.colSet(ii, "tt_source_curr", wfPtrCurrCodeEngName(all));

      all = wp.colStr(ii, "debt_status");
      wp.colSet(ii, "tt_debt_status", commString.decode(all, ",1,2,3,4", ",1.正常,2.逾放,3.催收,4.呆帳"));

      if (all.equals("3")) {
        wp.colSet(ii, "wk_date1", wp.colStr(ii, "trans_col_date"));
      } else if (all.equals("4")) {
        wp.colSet(ii, "wk_date1", wp.colStr(ii, "trans_bad_date"));
      } else {
        wp.colSet(ii, "wk_date1", "");
      }
    }
    wp.colSet("tt_acct_month", acctMonth);
    acctTypeKey = acctType + "-" + acctKey;
    wp.colSet("acct_type_key", acctTypeKey);

    // cardChiName = wf_getMajorName(kk5,kk6,kk7);
    // corpChiName = wf_getCorpName(kk8);
    // if(empty(corpChiName) == false){
    // cc3 = "商務卡公司名稱:";
    // cc4 = corpChiName;
    // } else {
    // cc3 = "正卡人姓名:";
    // cc4 = cardChiName;
    // }

    wfGetChiName(cardNo);
    wp.colSet("card_title", cardTitle);
    wp.colSet("card_chi_name", corpChiName);
  }

  void wfGetChiName(String cardNo) throws Exception {
    String supFlag = "", chiName = "", majorName = "", corpName = "";
    cardTitle = "";
    corpChiName = "";

    String lsSql = "select dbc_card.sup_flag, dbc_idno.chi_name, "
        + "major.chi_name as major_name, crd_corp.chi_name as corp_name "
        + "from dbc_card, dbc_idno, dbc_idno major "
        + "  left join crd_corp on dbc_card.corp_p_seqno = crd_corp.corp_p_seqno "
        + "where dbc_card.id_p_seqno = dbc_idno.id_p_seqno "
        + "and dbc_card.major_id_p_seqno = major.id_p_seqno " + "and dbc_card.card_no = :card_no ";
    setString("card_no", cardNo);

    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      supFlag = sqlStr("sup_flag");
      chiName = sqlStr("chi_name");
      majorName = sqlStr("major_name");
      corpName = sqlStr("corp_name");
    }

    if (empty(corpName) == false) {
      cardTitle = "商務卡公司名稱:";
      corpChiName = corpName;
    } else {
      cardTitle = "正卡人姓名:";
      if (supFlag.equals("0")) {
        corpChiName = chiName;
      } else if (supFlag.equals("1")) {
        corpChiName = majorName;
      }
    }
  }

  // String wf_getMajorName(String supFlag, String chiName, String idPSeqno) throws Exception {
  // String rtn="";
  // if (supFlag.equals("0")) {
  // rtn = chiName;
  // } else if (supFlag.equals("1")) {
  // String ls_sql = "select chi_name from dbc_idno "
  // + "where id_p_seqno = :id_p_seqno ";
  // setString("id_p_seqno", idPSeqno);
  //
  // sqlSelect(ls_sql);
  // if (sql_nrow != 0) {
  // rtn=sql_ss("chi_name");
  // }
  // }
  // return rtn;
  // }
  //
  // String wf_getCorpName(String idPSeqno) throws Exception {
  // String rtn="";
  // String ls_sql = "select chi_name from crd_corp "
  // + "where corp_p_seqno = :corp_p_seqno ";
  // setString("corp_p_seqno", idPSeqno);
  //
  // sqlSelect(ls_sql);
  // if (sql_nrow != 0) {
  // rtn=sql_ss("chi_name");
  // }
  // return rtn;
  // }

  String wfPtrCardTypeName(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select card_type||' ['||name||']' card_name from ptr_card_type "
        + "where card_type= :card_type ";
    setString("card_type", idcode);

    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      rtn = idcode;
    } else {
      rtn = sqlStr("card_name");
    }
    return rtn;
  }

  String wfPtrCurrCodeEngName(String idcode) throws Exception {
    String rtn = "";
    String lsSql = "select curr_code||' ['||curr_eng_name||']' curr_eng_name from ptr_currcode "
        + "where curr_code= :curr_code ";
    setString("curr_code", idcode);

    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      rtn = idcode;
    } else {
      rtn = sqlStr("curr_eng_name");
    }
    return rtn;
  }

  int getPseqnobyAcctKey() throws Exception {
    wp.colSet("queryReadCnt", "0");

    // 以[帳戶帳號]優先查詢
    if (empty(wp.itemStr("exAcctKey")) && empty(wp.itemStr("exCardNo"))) {
      alertErr2("請輸入[帳戶帳號]或[卡號]!");
      return -1;
    }
    if (empty(wp.itemStr("exAcctKey")) == false) {
      if (wp.itemStr("exAcctKey").length() < 6) {
        alertErr("[帳戶號碼]輸入至少6碼!");
        return -1;
      }
    }

    daoTid = "P-";
    wp.sqlCmd = "select " + "dba_acno.p_seqno, " + "dba_acno.acct_type, " + "dba_acno.acct_key, "
        + "dba_acno.acct_status, " + "dba_acno.corp_act_flag, " + "dba_acno.id_p_seqno, "
        + "dba_acno.corp_p_seqno, " + "dba_acno.corp_no, " + "dba_acno.acct_no, "
        + "dbc_idno.id_no, " + "dbc_idno.id_no_code, " + "dbc_idno.chi_name, "
        + "dbc_idno.birthday ";

    if (empty(wp.itemStr("exAcctKey")) == false) {
      wp.sqlCmd += "from dba_acno, dbc_idno ";
      wp.sqlCmd += "where dba_acno.id_p_seqno = dbc_idno.id_p_seqno ";
      wp.sqlCmd += " and dba_acno.acct_type = :acct_type ";
      wp.sqlCmd += " and dba_acno.acct_key like :acct_key ";
      setString("acct_type", wp.itemStr("exAcctType"));
      setString("acct_key", wp.itemStr("exAcctKey") + "%");
    } else {
      wp.sqlCmd += "from dba_acno, dbc_card, dbc_idno ";
      wp.sqlCmd += "where dba_acno.id_p_seqno = dbc_idno.id_p_seqno ";
      wp.sqlCmd += " and dba_acno.p_seqno = dbc_card.p_seqno ";
      wp.sqlCmd += " and dbc_card.card_no = :card_no ";
      setString("card_no", wp.itemStr("exCardNo"));
    }
    wp.sqlCmd += "order by dba_acno.acct_key ";

    selectNoLimit();
    pageQuery();

    wp.setListCount(2); // 彈跳視窗
    int popNrow = sqlRowNum;
    if (popNrow == 0) {
      alertErr("無法取得卡人之帳戶資料!");
      return -1;
    } else if (popNrow == 1) {
      pSeqno = wp.colStr("p-p_seqno");
      acctType = wp.colStr("p-acct_type");
      acctKey = wp.colStr("p-acct_key");
      wp.colSet("data_k1", pSeqno);
      wp.colSet("data_k2", acctType);
      wp.colSet("data_k3", acctKey);
      wp.setPageValue();
      return 1;
    } else {
      // 顯示多選一的彈出視窗畫面
      pop_wkdata();
      wp.colSet("queryReadCnt", intToStr(popNrow));
      return -1;
    }
  }

  void pop_wkdata() throws Exception {
    String all = "";
    String[] cde = new String[] {"1", "2", "3", "4", "5"};
    String[] txt = new String[] {"1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清(Write Off)"};

    int recordCnt = wp.selectCnt;
    for (int ii = 0; ii < recordCnt; ii++) {
      all = wp.colStr(ii, "p-acct_status");
      wp.colSet(ii, "p-tt_acct_status", commString.decode(all, cde, txt));

      all = wp.colStr(ii, "p-corp_act_flag");
      wp.colSet(ii, "p-tt_corp_act_flag", commString.decode(all, ",Y,N", ",總繳,個繳"));

      all = wp.colStr(ii, "p-card_type");
      wp.colSet(ii, "p-tt_card_type", wfPtrCardTypeName(all));
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  public void dataRead() throws Exception {

  }

  void xlsPrint() throws Exception {

  }

  void pdfPrint() throws Exception {
    // if (getWhereStr() == false) {
    // wp.respHtml = "TarokoErrorPDF";
    // return;
    // }
    if (empty(wp.itemStr("data_k1"))) {
      alertErr2("請先查詢資料後再列印!");
      wp.respHtml = "TarokoErrorPDF";
      return;
    }

    wp.reportId = progName;
    wp.pageRows = 9999;

    wp.colSet("reportName", progName.toUpperCase());
    wp.colSet("loginUser", wp.loginUser);
    queryFunc();

    String all = "帳務年月: " + commString.strToYmd(acctMonth);
    wp.colSet("cond_1", all);
    all = "帳戶帳號: " + acctTypeKey;
    wp.colSet("cond_2", all);
    all = cardTitle + " " + corpChiName;
    wp.colSet("cond_3", all);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = progName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.pageCount = 28;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  boolean authQuery(String asKey, String asWinId) throws Exception {
    String winId = "", lsIdno = "", lsUserId = "", lsIdPSeqno = "";
    long count;
    double lmAmtParm, lmAmt;

    winId = asWinId.trim();
    if (empty(winId)) {
      messageBox = "資料查詢權限: 程式代碼 不可空白";
      return false;
    }

    count = 0;
    lsUserId = wp.loginUser;

    // -win_id-
    String lsSql = "select count(*) cnt from col_qry_data_auth "
        + "where user_id = :ls_user_id and win_id =:ls_win_id and decode(run_flag,'','N',run_flag)='Y' ";
    setString("ls_user_id", lsUserId);
    setString("ls_win_id", winId);

    sqlSelect(lsSql);
    if (wp.errSql.equals("Y")) {
      messageBox = "資料查詢權限: select COL_QRY_DATA_AUTH error; win_id=" + winId;
      return false;
    }
    count = (long) sqlNum("cnt");
    // if (ll_cnt==0) return true;

    // -欠款條件-
    lmAmtParm = 0;
    lsSql = "select vd_end_bal from ptr_comm_data " + "where parm_code='COLM0910' and seq_no =1 ";

    sqlSelect(lsSql);
    if (wp.errSql.equals("Y")) {
      messageBox = "資料查詢權限: select PTR_COMM_DATA error";
      return false;
    }
    lmAmtParm = (long) sqlNum("vd_end_bal");
    if (lmAmtParm <= 0)
      return true;

    // -ls_idno-
    if (empty(asKey.trim())) {
      messageBox = "資料查詢權限: 卡號 or 身分證ID 不可空白";
      return false;
    }
    // ls_key = as_key.trim(); //傳入參數為id_no, 非card_no
    // if (ls_key.length()>=14){
    // ls_sql = "select id_no from dbc_card "
    // + "where card_no like :card_no fetch first 1 row only ";
    // setString("card_no", ls_key);
    //
    // sqlSelect(ls_sql);
    // if (sql_nrow != 0) {
    // ls_idno=sql_ss("id_no");
    // }
    // if (empty(ls_idno)) {
    // messageBox = "資料查詢權限: 查核資料不是 VD 卡號 or 身分證ID";
    // return false;
    // }
    // }
    // ls_idno = as_key.trim();
    lsIdPSeqno = asKey.trim();

    lmAmt = 0;
    // ls_sql = "select nvl(sum(end_bal),0) end_bal from dba_debt "
    // + "where p_seqno = UF_IDNO_PSEQNO(:id_no) ";
    // setString("id_no", ls_idno);
    lsSql = "select nvl(sum(end_bal),0) end_bal from dba_debt " + "where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", lsIdPSeqno);

    sqlSelect(lsSql);
    if (wp.errSql.equals("Y")) {
      messageBox = "資料查詢權限: select DBC_DEBT error; KEY=" + asKey;
      return false;
    }
    lmAmt = (long) sqlNum("end_bal");
    if (lmAmt >= lmAmtParm)
      return true;

    messageBox = "資料查詢權限: 卡友欠款未達 [參數金額] 不可查詢; KEY=" + asKey;
    return false;
  }

}
