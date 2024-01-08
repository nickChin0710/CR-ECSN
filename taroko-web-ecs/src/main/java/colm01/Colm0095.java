/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/02/20  V1.00.00    Zuwei       program initial                         *     
*  109/03/30  V1.00.01    Zhenwu Zhu  繳款方式顯示樣式更改                    *
*  109-05-06  V1.00.02    Aoyulan     updated for project coding standard     *
*  109/06/09  V1.00.03    shiyuqi     明細顯示條件語法調整                    *
*  109-01-04  V1.00.04    shiyuqi     修改无意义命名                          *
*  111-06-23  V1.00.05    sunny       查無資料的訊息增加table name明確顯示。  *  
*  111-06-24  V1.00.06    Justin      fix bug within itemchangedA()           *
*                                     if selection is not found, return       *
*                                                                           新增欲轉催呆金額及欠款總額計算                                   *
*  111-08-02  V1.00.07    sunny       檢核該公司(corp_p_seqno)項下所有p_seqno的金額，*
*                                                                           若有任何一個帳戶含有溢繳款就一律不轉呆。                     *
*  112-11-28  V1.00.08    sunny     加強商務卡區分acct_type的查詢條件                        *                                                 
******************************************************************************/

package colm01;

import java.math.BigDecimal;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colm0095 extends BaseEdit {
  CommString commString = new CommString();
  Colm0095FuncA funcA;
  Colm0095FuncB funcB;

  String kkOptName = "";
  String kkSeqno = "";
  String isCurr = "";
  String progName = "";
  int totalCnt = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
    } else if (eqIgno(wp.buttonCode, "G")) {
      /* Item changed */
      itemfocuschangedB();
    } else if (eqIgno(wp.buttonCode, "T") || eqIgno(wp.buttonCode, "AJAX")) {
      /* Item changed for 新增轉呆 */
      strAction = "AJAX";
      itemchangedA();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    String lsWfDesc = "";

    if (wp.respHtml.indexOf("_detl") > 0) {
      String lsSql = "select wf_desc from ptr_sys_idtab where wf_type = 'COL_CERTIFICATE' "
          + "and id_code = 'Y' order by wf_id ";
      sqlSelect(lsSql);
      lsWfDesc = sqlStr("wf_desc");
      wp.colSet("paper_name", lsWfDesc);

      wp.colSet("kk_acct_type", "01");
    }
  }

  private boolean checkQueryParam() throws Exception {
    String exAcctkey = wp.itemStr("exAcctKey");
    if (exAcctkey.length() < 8 || exAcctkey.length() > 11) {
      alertErr2("[帳戶號碼]輸入介於8~11碼!");
      return false;
    }

    return true;
  }

  /**
   * 查詢列表數據
   */
  @Override
  public void queryFunc() throws Exception {
    queryRead();
  }

  /**
   * 查詢列表數據
   */
  @Override
  public void queryRead() throws Exception {
    if (checkQueryParam() == false)
      return;

    if (eqIgno(wp.colStr("optname"), "aopt")) {
      queryReadA();
    }
    if (eqIgno(wp.colStr("optname"), "bopt")) {
      queryReadB();
    }
  }

  void queryReadA() throws Exception {
    daoTid = "A-";

    wp.selectSQL = " act_acno.acno_p_seqno as p_seqno " + " ,act_acno.acct_type " + " ,acct_key "
        + " ,acct_status " + " ,status_change_date " + " ,act_acno.id_p_seqno "
        + " ,act_acno.corp_p_seqno " 
        + " ,uf_idno_id(act_acno.id_p_seqno) as id_no  "
        + " ,(case when nvl(chkcard.ll_cnt, 0) > 0 then 'Y' else 'N' end) as db_curr " // 是否有有效卡
        + " ,(case when nvl(chkcard.ll_cnt, 0) > 0 then '是' else '否' end) as db_curr_desc " // 是否有有效卡
        + " ,act_acct.acct_jrnl_bal " // 欠款總額（按公司加總）
        + " ,crd_corp.corp_no" // 公司統編
        + " ,crd_corp.corp_act_type" // 繳款方式
        + " ,decode(crd_corp.corp_act_type, '1', '1.總繳', '2','2.個繳', '') as corp_act_type_desc ";

    wp.daoTable = " act_acno "
        + "left join crd_corp on act_acno.corp_p_seqno=crd_corp.corp_p_seqno "
        + "left join crd_idno on act_acno.id_p_seqno = crd_idno.id_p_seqno "
        + "left join (select corp_p_seqno, sum(nvl(acct_jrnl_bal, 0)) as acct_jrnl_bal from act_acct group by corp_p_seqno) act_acct on act_acno.corp_p_seqno = act_acct.corp_p_seqno "
        + "left join (select corp_p_seqno, count(*) as ll_cnt from crd_card where current_code = '0' group by corp_p_seqno) chkcard on act_acno.corp_p_seqno = chkcard.corp_p_seqno ";
    wp.whereOrder = " ";
    wp.whereStr = " where acno_flag='2' ";

    if (empty(wp.itemStr("exAcctkey")) == false) {
      wp.whereStr +=
          "and act_acno.acct_type = :acct_type and act_acno.corp_p_seqno in (Select corp_p_seqno From crd_corp Where corp_no =:acct_key) ";
      setString("acct_type", wp.itemStr("exAcctType"));
      setString("acct_key", wp.itemStr("exAcctkey"));
    }

    if (eqIgno(wp.itemStr("exAcctStatus"), "0") == false) {
      wp.whereStr += "and act_acno.acct_status = :acct_status ";
      setString("acct_status", wp.itemStr("exAcctStatus"));
    }

    pageQuery();
    String corpActType = wp.colStr("a-corp_act_type");
    wp.colSet("a-corp_act_type_desc", commString.decode(corpActType, ",1,2", ",1.總繳,2.個繳"));

    wp.setListCount(1);
    totalCnt = wp.selectCnt;
    if (sqlNotFind()) {
      return;
    }

    String param = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String corpSeqNo = wp.colStr(ii, "a-corp_p_seqno");
      String acctStatus = wp.colStr(ii, "a-acct_status");
      String isCurr = wp.colStr(ii, "a-db_curr");
      param = wp.colStr(ii, "a-acct_status");
      wp.colSet(ii, "a-tt_acct_status",
          commString.decode(param, ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
      String lsSumBal = getCorpSumBal(corpSeqNo, acctStatus, isCurr);
      // 轉催呆金額
      wp.colSet(ii, "no_end_bal", lsSumBal);
    }
  }

  void queryReadB() throws Exception {
    daoTid = "B-";

    // 2019.8.29 user要求呆帳LIST同ID只顯示一筆,SQL大改
    wp.selectSQL = " p_seqno " + " ,trans_date " + " ,acct_type " + " ,credit_act_no "
        + " ,src_amt " + " ,alw_bad_date " + " ,paper_conf_date " + " ,paper_name "
        + " ,terminate_date " + " ,apr_user " + " ,apr_date "
        + " ,uf_acno_key(p_seqno) as acct_key " + " ,trans_type " + " ,corp_p_seqno ";

    wp.daoTable = " col_bad_debt, ";
    wp.daoTable += "(SELECT col_bad_debt.p_seqno p_seqnoBB, max(trans_date) trans_dateBB ";
    wp.daoTable +=
        " FROM act_acno, col_bad_debt WHERE act_acno.acno_p_seqno = col_bad_debt.p_seqno and acno_flag='2' ";
    if (empty(wp.itemStr("exAcctkey")) == false) {
      wp.daoTable +=
          "and act_acno.acct_type = :acct_type and act_acno.corp_p_seqno in (Select corp_p_seqno From crd_corp Where corp_no =:acct_key) ";
      setString("acct_type", wp.itemStr("exAcctType"));
      setString("acct_key", wp.itemStr("exAcctkey"));
    }
    wp.daoTable += "group by col_bad_debt.p_seqno ) BB ";
    wp.whereOrder = " ";
    wp.whereStr = " where p_seqno = p_seqnoBB and trans_date = trans_dateBB ";

    pageQuery();
    wp.setListCount(2);
    totalCnt += wp.selectCnt;
    if (totalCnt == 0) {
      // wp.notFound = "Y";
      // alert_err(AppMsg.err_condNodata);
      return;
    }

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String corpSeqNo = wp.colStr(ii, "b-corp_p_seqno");
      String acctType = wp.colStr(ii, "b-acct_type");
      String srcAmtSum = sumSrcAmt(acctType, corpSeqNo);
      wp.colSet(ii, "b-src_amt", srcAmtSum);
    }
  }

  /**
   * 同一corp_no下的合計src_amt src_amt
   * 
   * @return
   */
  private String sumSrcAmt(String acctType, String corpSeqno) {
    String lsSql = "";
    String lsSumSrcAmt = "0";

    lsSql = "select sum(nvl(src_amt, 0)) as src_amt from col_bad_debt,"
        + "(SELECT col_bad_debt.p_seqno p_seqnoBB, max(trans_date) trans_dateBB "
        + "FROM act_acno, col_bad_debt WHERE act_acno.acno_p_seqno = col_bad_debt.p_seqno "
        + "and act_acno.acct_type = :acctType and act_acno.corp_p_seqno = :corpSeqno "
        + "group by col_bad_debt.p_seqno ) BB "
        + "where p_seqno = p_seqnoBB and trans_date = trans_dateBB " + " ";
    setString("acctType", acctType);
    setString("corpSeqno", corpSeqno);
    sqlSelect(lsSql);
    lsSumSrcAmt = numToStr(sqlNum("src_amt"), "###0");

    return lsSumSrcAmt;
  }

  private int getPseqnobyAcctKey() throws Exception {
    String lsAcctType, lsAcctKey;

    wp.colSet("queryReadCnt", "0");
    // 以輸入欄位優先查詢
    lsAcctType = wp.itemStr("kk_acct_type");
    lsAcctKey = wp.itemStr("kk_acct_key");
    if (empty(lsAcctType) && empty(lsAcctKey)) {
      lsAcctType = wp.itemStr("acct_type");
      lsAcctKey = wp.itemStr("acct_key");
    }

    if (empty(lsAcctType)) {
      alertErr("帳戶號碼(type) 不可空白");
      return -1;
    }
    if (empty(lsAcctKey)) {
      alertErr("帳戶號碼(key) 不可空白");
      return -1;
    }
    if (lsAcctKey.length() < 6) {
      alertErr("[帳戶號碼]輸入至少6碼!");
      return -1;
    }

    daoTid = "D-";
    wp.sqlCmd =
        "select act_acno.acno_p_seqno as p_seqno, act_acno.acct_type, act_acno.acct_key, act_acno.acct_status, "
            + "decode(act_acno.acct_status,'1','1.正常','2','2.逾放','3','3.催收','4','4.呆帳','5','5.結清',act_acno.acct_status) tt_acct_status, "
            + "crd_idno.id_no, crd_idno.id_no_code, crd_idno.chi_name, crd_idno.birthday "
            + "from act_acno " + "left join crd_idno on act_acno.id_p_seqno = crd_idno.id_p_seqno "
            + "where acct_type = :acct_type and acct_key like :acct_key "
            + "order by act_acno.acct_type, act_acno.acct_key ";
    setString("acct_type", lsAcctType);
    setString("acct_key", lsAcctKey + "%");

    pageQuery();

    wp.setListCount(1); // 彈跳視窗
    if (sqlRowNum == 0) {
      alertErr("查無資料(act_acno): acct_type=" + lsAcctType + ", acct_key=" + lsAcctKey);
      return -1;
    } else if (sqlRowNum == 1) {
      kkSeqno = wp.colStr("d-p_seqno");
      return 1;
    } else {
      // 顯示多選一的彈出視窗畫面
      wp.setPageValue();
      wp.colSet("queryReadCnt", intToStr(sqlRowNum));
      return -1;
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    kkSeqno = wp.itemStr("data_k1");
    kkOptName = wp.itemStr("optname");

    if (empty(kkSeqno)) {
      if (getPseqnobyAcctKey() < 0)
        return;
    }

    switch (kkOptName) {
      case "aopt":
        dataReadA(); // ofc_retrieve_1
        break;
      case "bopt":
        dataReadB(); // ofc_retrieve_2
        break;
    }

  }

  // 說明:
  // a. 檢查執行【queryColWaitTrans】結果，有無資料。
  // b. 若有資料，顯示訊息並執行wf_chk_card()，確認是否有有效卡。
  // c. 若無資料，則執行wf_read_acno()。
  public void dataReadA() throws Exception {
    // 查詢權限檢查，參考【f_auth_query】
    String lsSql = "", lsAcctKey = "";
    lsSql = "select uf_acno_key(:p_seqno) as acct_key from dual ";
    setString("p_seqno", kkSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      lsAcctKey = sqlStr("acct_key");
    }
    busi.func.ColFunc func = new busi.func.ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(lsAcctKey) != 1) {
      alertErr2(func.getMsg());
      return;
    }

    // 檢查是否在col_wait_trans中存在
    wp.selectSQL = " hex(col_wait_trans.rowid) as rowid "
        // + " ,act_acno.p_seqno "
        + " ,act_acno.acno_p_seqno as p_seqno " + " ,act_acno.acct_type " + " ,act_acno.acct_key "
        + " ,act_acno.id_p_seqno " + " ,act_acno.corp_p_seqno "
        + " ,uf_corp_name(act_acno.corp_p_seqno) corp_name "
        + " ,uf_idno_name(act_acno.id_p_seqno) id_name "
        + " ,decode(act_acno.no_delinquent_flag, '', 'N', act_acno.no_delinquent_flag) no_delinquent_flag "
        + " ,act_acno.no_delinquent_s_date " + " ,act_acno.no_delinquent_e_date "
        + " ,decode(act_acno.no_collection_flag, '', 'N', act_acno.no_collection_flag) no_collection_flag "
        + " ,act_acno.no_collection_s_date " + " ,act_acno.no_collection_e_date "
        + " ,act_acno.acct_status " + " ,act_acno.org_delinquent_date " + " ,act_acno.stop_status "
        + " ,decode(act_acno.stop_status, 'Y', 'Y-強停', 'N','N-未強停', 'N-未強停') as tt_stop_status "
        + " ,col_wait_trans.p_seqno " + " ,col_wait_trans.src_acct_stat "
        + " ,col_wait_trans.trans_type " + " ,col_wait_trans.alw_bad_date "
        + " ,col_wait_trans.paper_conf_date " + " ,col_wait_trans.valid_cancel_date "
        + " ,col_wait_trans.paper_name " + " ,col_wait_trans.crt_date "
        + " ,col_wait_trans.crt_time " + " ,col_wait_trans.crt_user " + " ,col_wait_trans.apr_flag "
        + " ,col_wait_trans.apr_date " + " ,col_wait_trans.apr_time " + " ,col_wait_trans.apr_user "
        + " ,col_wait_trans.sys_trans_flag " + " ,col_wait_trans.mod_user "
        + " ,col_wait_trans.mod_time " + " ,col_wait_trans.mod_pgm " + " ,col_wait_trans.mod_seqno "
        + " ,col_wait_trans.chi_name " + " ,col_wait_trans.bad_debt_amt " + " ,0 no_acct_jrnl_bal "
        + " ,0 no_end_bal " + " ,crd_corp.corp_no" // 公司統編
        + " ,crd_corp.corp_act_type" // 繳款方式
        + " ,decode(crd_corp.corp_act_type, '1', '1.總繳', '2','2.個繳', '') as corp_act_type_desc ";

    wp.daoTable = " col_wait_trans, act_acno ";
    wp.daoTable += "left join crd_corp on act_acno.corp_p_seqno=crd_corp.corp_p_seqno ";
    wp.whereOrder = " ";
    wp.whereStr = " where col_wait_trans.p_seqno = act_acno.acno_p_seqno ";
    wp.whereStr += "  and col_wait_trans.p_seqno = :p_seqno ";
    setString("p_seqno", kkSeqno);
    pageSelect();
    if (sqlNotFind()) {
      int result = wfReadAcno();
      if (result != 1) return;
    } else {
      // 在col_wait_trans中存在
      if (eqIgno(wp.colStr("apr_flag"), "Y")) {
        wp.alertMesg = "<script language='javascript'> alert('此筆資料待批次處理轉入帳戶中.....')</script>";
      } else {
        wp.alertMesg = "<script language='javascript'> alert('此筆資料待覆核.....')</script>";
      }

      isCurr = wfCorpChkCard(wp.colStr("corp_p_seqno"));
      
      //
      String sumBal = getCorpSumBal(wp.colStr("corp_p_seqno"), wp.colStr("acct_status"), isCurr);
      wp.colSet("no_end_bal", sumBal);
    }
    wp.colSet("valid_cancel_date", wp.colStr("org_delinquent_date"));
    wp.colSet("db_curr", isCurr);
    wp.colSet("db_cname",
        empty(wp.colStr("id_name")) ? wp.colStr("corp_name") : wp.colStr("id_name"));
    wp.colSet("tt_acct_status",
        commString.decode(wp.colStr("acct_status"), ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));

    listSubA(wp.colStr("acct_type"), wp.colStr("corp_p_seqno"), wp.colStr("acct_status"));
  }

  /**
   * 查詢公司下的所有個卡列表
   * 
   * @throws Exception
   */
  private void listSubA(String acctType, String corpSeqNo, String acctStatus) throws Exception {
    daoTid = "A-";

    wp.selectSQL = " act_acno.acno_p_seqno as p_seqno " + " ,act_acno.acct_type " + " ,acct_key "
        + " ,acct_status " + " ,status_change_date " + " ,act_acno.id_p_seqno "
        + " ,act_acno.corp_p_seqno " 
        + " ,uf_idno_id(act_acno.id_p_seqno) as id_no  "
        + " ,(case when nvl(chkcard.ll_cnt, 0) > 0 then 'Y' else 'N' end) as db_curr " // 是否有有效卡
        + " ,(case when nvl(chkcard.ll_cnt, 0) > 0 then '是' else '否' end) as db_curr_desc " // 是否有有效卡
        + " ,act_acct.acct_jrnl_bal " // 欠款總額
        + " ,act_acno.acno_flag " + " ,uf_corp_name(act_acno.corp_p_seqno) corp_name "
        + " ,uf_idno_name(act_acno.id_p_seqno) id_name ";

    wp.daoTable = " act_acno " + "left join crd_idno on act_acno.id_p_seqno = crd_idno.id_p_seqno "
        + "left join act_acct on act_acno.acno_p_seqno = act_acct.p_seqno "
        + "left join (select acno_p_seqno, count(*) as ll_cnt from crd_card where current_code = '0' group by acno_p_seqno) chkcard on act_acno.acno_p_seqno = chkcard.acno_p_seqno ";
    wp.whereOrder = " ";
    wp.whereStr = " where act_acno.corp_p_seqno = :corpSeqNo "; // and acno_flag<>'2' ";
    setString("corpSeqNo", corpSeqNo);
    // wp.whereStr += "and act_acno.acct_type = :acct_type ";
    // setString("acct_type",acctType);
    //
    // wp.whereStr += "and act_acno.acct_status = :acct_status ";
    // setString("acct_status",acctStatus);

    pageQuery();
    wp.setListCount(1);
    totalCnt = wp.selectCnt;
    wp.colSet("total", wp.selectCnt);
    if (sqlNotFind()) {
      wp.notFound = "N";
      return;
    }

    StringBuilder seqnoStr = new StringBuilder(wp.selectCnt * 20);
    BigDecimal acctJrnlBalSum = BigDecimal.ZERO;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String seqNo = wp.colStr(ii, "a-p_seqno");
      String status = wp.colStr(ii, "a-acct_status");
      String isCurr = wp.colStr(ii, "a-db_curr");
      String param = wp.colStr(ii, "a-acct_status");
      double acctJrnlBal = wp.colNum(ii, "a-acct_jrnl_bal");
      acctJrnlBalSum = acctJrnlBalSum.add(BigDecimal.valueOf(acctJrnlBal));
      wp.colSet(ii, "a-tt_acct_status",
          commString.decode(param, ",1,2,3,4,5", ",1.正常,2.逾放,3.催收,4.呆帳,5.結清"));
      String lsSumBal = getSumBal(seqNo, status, isCurr);
      wp.colSet(ii, "a-no_end_bal", lsSumBal);
      wp.colSet(ii, "a-db_cname",
          empty(wp.colStr("id_name")) ? wp.colStr("corp_name") : wp.colStr("id_name"));

      seqnoStr.append(",").append(seqNo);
    }
    wp.colSet("sub_seqnos", seqnoStr.substring(1));
    wp.colSet("no_acct_jrnl_bal", acctJrnlBalSum.doubleValue());
  }

  /**
   * 欲轉催呆金額 no_end_bal
   * 
   * @return
   */
  private String getSumBal(String seqNo, String acctStatus, String isCurr) {
    String lsSql = "";
    String lsSumBal = "0";

    if (eqIgno(acctStatus, "3")) {
      lsSql = "select nvl(sum(end_bal),0) as sum_bal from act_debt " + "where p_seqno = :p_seqno "
          + "and (acct_code = 'CB' or acct_code = 'CC' or acct_code = 'CI') ";
      setString("p_seqno", seqNo);
      sqlSelect(lsSql);
      // ls_sum_bal = sql_ss("sum_bal"); //有小數
      lsSumBal = numToStr(sqlNum("sum_bal"), "###0"); // 無小數
    }

    if ((eqIgno(acctStatus, "1") || eqIgno(acctStatus, "2")) && eqIgno(isCurr, "N")) {
      lsSql = "select nvl(sum(end_bal),0) as sum_bal from act_debt " + "where p_seqno = :p_seqno "
          + "and acct_code != 'DP' " + "and acct_code != 'CI' " + "and acct_code != 'CC' "
          + "and acct_code != 'CB' " + "and acct_code != 'DB' " + " ";
      setString("p_seqno", seqNo);
      sqlSelect(lsSql);
      // ls_sum_bal = sql_ss("sum_bal"); //有小數
      lsSumBal = numToStr(sqlNum("sum_bal"), "###0"); // 無小數
    }

    return lsSumBal;
  }

  // 說明:
  // a. 取得act_acct資料。
  // b. 取得act_acno資料。
  // c. 取得轉催、轉呆相關金額資料。
  // d. 取得姓名或公司名稱資料以顯示。
  public void dataReadB() throws Exception {

    if (preretrieveB() < 0) {
      return;
    }

    wp.selectSQL = " hex(col_bad_debt.rowid) as rowid " + " ,col_bad_debt.p_seqno "
        + " ,col_bad_debt.acct_type " + " ,uf_acno_key(col_bad_debt.p_seqno) as acct_key "
        + " ,col_bad_debt.line_of_credit_amt " + " ,col_bad_debt.credit_act_no "
        + " ,col_bad_debt.src_amt " + " ,col_bad_debt.alw_bad_date "
        + " ,col_bad_debt.paper_conf_date " + " ,col_bad_debt.paper_name "
        + " ,col_bad_debt.paper_type " + " ,col_bad_debt.terminate_date "
        + " ,col_bad_debt.recourse_mark " + " ,col_bad_debt.recourse_mark_date "
        + " ,col_bad_debt.apr_user " + " ,col_bad_debt.apr_date " + " ,col_bad_debt.id_p_seqno "
        + " ,col_bad_debt.corp_p_seqno " + " ,uf_corp_name(col_bad_debt.corp_p_seqno) corp_name "
        + " ,uf_idno_name(col_bad_debt.id_p_seqno) id_name " + " ,col_bad_debt.trans_date "
        + " ,col_bad_debt.trans_type "
        // + " ,' ' org_trans_date "
        // + " ,lpad (' ', 10) db_acct_staus "
        // + " ,lpad (' ', 20) db_cname "
        + " ,col_bad_debt.description " + " ,col_bad_debt.mod_user " + " ,col_bad_debt.mod_time "
        + " ,col_bad_debt.mod_pgm " + " ,col_bad_debt.mod_seqno ";

    wp.daoTable = " col_bad_debt ";
    wp.whereOrder = " ";  

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料(col_bad_debt), p_seqno:" + kkSeqno);
      return;
    }

    // 獲取詳情其他數據
    String acctType = wp.colStr("acct_type");
    String corpSeqno = wp.colStr("corp_p_seqno");
    detlWkdataB(acctType, corpSeqno, kkSeqno);
  }

  /**
   * 呆賬資料維護詳情
   * 
   * @throws Exception
   */
  void detlWkdataB(String acctType, String corpSeqno, String seqno) throws Exception {
    String lsAutopayAcctBank = "", lsAutopayAcctNo = "", lsAcctStatus = "";
    double ldAdiInterest = 0, ldAcctJrnlBal = 0;
    String lsOrgTransDate = "", lsTransType = "";
    double ldCbAmt = 0, ldCiAmt = 0, ldCcAmt = 0;
    double ldCbAmtAcct = 0, ldCiAmtAcct = 0, ldCcAmtAcct = 0;

    // 加總src_amt
    String srcAmtSum = sumSrcAmt(acctType, corpSeqno);
    wp.colSet("src_amt", srcAmtSum);

    // --get act_acct--
    String lsSql = "select sum(nvl(temp_adi_interest,0)) as temp_adi_interest, "
        + "sum(nvl(acct_jrnl_bal,0)) as acct_jrnl_bal "
        + "from act_acct where corp_p_seqno = :corpSeqno ";
    setString("corpSeqno", corpSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      ldAdiInterest = sqlNum("temp_adi_interest");
      ldAcctJrnlBal = sqlNum("acct_jrnl_bal");
    }
    // 帳外息
    wp.colSet("txt_interest", numToStr(ldAdiInterest, ""));
    // 欠款總款
    wp.colSet("jrnl_bal", numToStr(ldAcctJrnlBal, ""));

    // --get act_acno--
    lsSql = "select autopay_acct_bank,autopay_acct_no,acct_status "
        // + "from act_acno where p_seqno = :p_seqno ";
        + "from act_acno where acno_p_seqno = :p_seqno ";
    setString("p_seqno", seqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      lsAutopayAcctBank = sqlStr("autopay_acct_bank");
      lsAutopayAcctNo = sqlStr("autopay_acct_no");
      lsAcctStatus = sqlStr("acct_status");
    }
    // 扣款銀行與帳號
    wp.colSet("autopay_acct_bank", lsAutopayAcctBank);
    wp.colSet("autopay_acct_no", lsAutopayAcctNo);
    // 狀態別
    wp.colSet("acct_status", lsAcctStatus);
    wp.colSet("tt_acct_status", commString.decode(lsAcctStatus, ",1,2,3,4", ",1.正常,2.逾放,3.催收,4.呆帳"));

    lsTransType = wp.colStr("trans_type");
    if (eqIgno(lsTransType, "3")) {
      // 原始轉呆金額
      wp.colSet("src_amt", "0");
      // 轉呆帳日期
      wp.colSet("trans_date", "");
    }

    // 取得轉催、轉呆相關金額資料
    // 按corp_p_seqno匯總
    // --get col_bad_detail-- trans_type = '3'
    lsSql = "select sum(decode(new_acct_code,'CB',nvl(end_bal,0),0)) as cb_amt, "
        + "sum(decode(new_acct_code,'CI',nvl(end_bal,0),0)) as ci_amt, "
        + "sum(decode(new_acct_code,'CC',nvl(end_bal,0),0)) as cc_amt, "
        + "max(col_bad_detail.trans_date) as org_trans_date " + "from col_bad_detail "
        + "inner join (Select p_seqno, min(trans_date) as trans_date from col_bad_detail where trans_type = '3' group by p_seqno) col_bad_detail_min "
        + "	on col_bad_detail_min.p_seqno = col_bad_detail.p_seqno and col_bad_detail.trans_date = col_bad_detail_min.trans_date "
        + "where trans_type = '3' "
        + "and exists (select 1 from col_bad_debt where col_bad_debt.corp_p_seqno = :corpSeqno and col_bad_debt.p_seqno = col_bad_detail.p_seqno) ";
    setString("corpSeqno", corpSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      ldCbAmt = sqlNum("cb_amt");
      ldCiAmt = sqlNum("ci_amt");
      ldCcAmt = sqlNum("cc_amt");
      lsOrgTransDate = sqlStr("org_trans_date");
    }
    wp.colSet("cb_amt", numToStr(ldCbAmt, ""));
    wp.colSet("ci_amt", numToStr(ldCiAmt, ""));
    wp.colSet("cc_amt", numToStr(ldCcAmt, ""));
    wp.colSet("total_amt", numToStr(ldCbAmt + ldCiAmt + ldCcAmt, ""));
    wp.colSet("org_trans_date", lsOrgTransDate);

    // --get col_bad_detail-- trans_type = '4'
    lsSql = "select sum(decode(acct_code,'CB',nvl(end_bal,0),0)) as cb_amt_acct, "
        + "sum(decode(acct_code,'CI',nvl(end_bal,0),0)) as ci_amt_acct, "
        + "sum(decode(acct_code,'CC',nvl(end_bal,0),0)) as cc_amt_acct " + "from col_bad_detail "
        + "inner join (Select p_seqno, min(trans_date) as trans_date from col_bad_detail where trans_type = '4' group by p_seqno) col_bad_detail_min "
        + "	on col_bad_detail_min.p_seqno = col_bad_detail.p_seqno and col_bad_detail.trans_date = col_bad_detail_min.trans_date "
        + "where trans_type = '4' "
        + "and exists (select 1 from col_bad_debt where col_bad_debt.corp_p_seqno = :corpSeqno and col_bad_debt.p_seqno = col_bad_detail.p_seqno) ";
    setString("corpSeqno", corpSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      ldCbAmtAcct = sqlNum("cb_amt_acct");
      ldCiAmtAcct = sqlNum("ci_amt_acct");
      ldCcAmtAcct = sqlNum("cc_amt_acct");
    }
    wp.colSet("cb_amt_acct", numToStr(ldCbAmtAcct, ""));
    wp.colSet("ci_amt_acct", numToStr(ldCiAmtAcct, ""));
    wp.colSet("cc_amt_acct", numToStr(ldCcAmtAcct, ""));

  }

  // 說明:
  // a. 檢查是否有查詢權限。
  // b. 以 p_seqno 為查詢條件，查詢 col_bad_debt，取得最大trans_type。
  // c. 以 p_seqno + trans_type 為查詢條件，取得 col_bad_debt 資料。
  // d. 若 trans_type = '3'，維護頁面之【修改】按鈕，disabled。
  int preretrieveB() throws Exception {
    String lsSql = "";
    String lsAcctKey = "", lsTransType = "";

    // 檢查是否有查詢權限
    lsSql = "select uf_acno_key(:p_seqno) as acct_key from dual ";
    setString("p_seqno", kkSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0)
      lsAcctKey = sqlStr("acct_key");
    // if (!f_auth_query(parent.classname(),ls_acct_key)) { return -1; }
    // use local Func:
    // if (f_auth_query(ls_acct_key)!=1) { return -1; }
    // use busi.func.ColFunc:
    busi.func.ColFunc func = new busi.func.ColFunc();
    func.setConn(wp);
    if (func.fAuthQuery(lsAcctKey) != 1) {
      alertErr2(func.getMsg());
      return -1;
    }

    // 以 p_seqno 為查詢條件，查詢 col_bad_debt，取得最大trans_type。
    // 以 p_seqno + trans_type 為查詢條件，取得 col_bad_debt 資料。
    // 若 trans_type = '3'，維護頁面之【修改】按鈕，disabled。
    lsSql =
        "select max(trans_type) as now_trans_type from col_bad_debt " + "where p_seqno = :p_seqno1 "
            + "  and trans_date = (select max(trans_date) from col_bad_debt "
            + " 			        where p_seqno = :p_seqno2 ) ";
    setString("p_seqno1", kkSeqno);
    setString("p_seqno2", kkSeqno);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      lsTransType = sqlStr("now_trans_type");
    }

    wp.whereStr = " where p_seqno = :p_seqno ";
    setString("p_seqno", kkSeqno);
    wp.whereStr += "and trans_type = :trans_type ";
    setString("trans_type", lsTransType);

    return 1;
  }

  // 說明:檢查是否有有效卡
  String wfChkCard(String seqNo) throws Exception {
    String rtn = "";
    long llCnt = 0;
    if (empty(seqNo)) {
      return rtn;
    }
    String lsSql = "select count(*) as ll_cnt from crd_card "
        + "where acno_p_seqno = :p_seqno and current_code = '0' ";
    setString("p_seqno", seqNo);
    sqlSelect(lsSql);
    llCnt = (long) sqlNum("ll_cnt");
    if (llCnt > 0) {
      rtn = "Y";
    } else {
      rtn = "N";
    }
    return rtn;
  }

  // 說明:檢查是否有有效公司卡
  String wfCorpChkCard(String corpSeqNo) throws Exception {
    String rtn = "";
    long llCnt = 0;
    if (empty(corpSeqNo)) {
      return rtn;
    }
    String lsSql = "select count(*) as ll_cnt from crd_card "
        + "where corp_p_seqno = :corp_p_seqno and current_code = '0' ";
    setString("corp_p_seqno", corpSeqNo);
    sqlSelect(lsSql);
    llCnt = (long) sqlNum("ll_cnt");
    if (llCnt > 0) {
      rtn = "Y";
    } else {
      rtn = "N";
    }
    return rtn;
  }

  // 說明:查詢取得act 相關資料。
  // a. 檢查是否有有效卡,如全部為無效卡才能轉催。
  // b. 取得 act_acno,act_acct資料。
  // c. 取得中文姓名或公司名稱。
  int wfReadAcno() throws Exception {
    if (empty(kkSeqno)) {
      return 0;
    }

    // wp.selectSQL = " hex(act_acno.rowid) as rowid "
    wp.selectSQL = " '' as rowid "
        // + " ,act_acno.p_seqno "
        + " ,act_acno.acno_p_seqno as p_seqno " + " ,act_acno.acct_type " + " ,act_acno.acct_key "
        + " ,act_acno.acct_status " + " ,act_acno.corp_p_seqno " + " ,act_acno.id_p_seqno "
        + " ,uf_corp_name(act_acno.corp_p_seqno) corp_name "
        + " ,uf_idno_name(act_acno.id_p_seqno) id_name "
        + " ,decode(act_acno.no_delinquent_flag, '', 'N', act_acno.no_delinquent_flag) as no_delinquent_flag "
        + " ,act_acno.no_delinquent_s_date " + " ,act_acno.no_delinquent_e_date "
        + " ,decode(act_acno.no_collection_flag, '', 'N', act_acno.no_collection_flag) as no_collection_flag "
        + " ,act_acno.no_collection_s_date " + " ,act_acno.no_collection_e_date "
        + " ,decode(act_acno.stop_status, '', 'N', act_acno.stop_status) as stop_status "
        + " ,decode(act_acno.stop_status, 'Y', 'Y-強停', 'N','N-未強停', 'N-未強停') as tt_stop_status "
        + " ,act_acno.org_delinquent_date " + " ,act_acct.acct_jrnl_bal " + " ,act_acno.acno_flag "
        + " ,'' as alw_bad_date " // init data
        + " ,'' as paper_conf_date " // init data
        + " ,'' as paper_name " // init data
        + " ,crd_corp.corp_no" // 公司統編
        + " ,crd_corp.corp_act_type" // 繳款方式
        + " ,decode(crd_corp.corp_act_type, '1', '1.總繳', '2','2.個繳', '') as corp_act_type_desc ";

    wp.daoTable = " act_acno,act_acct ";
    wp.daoTable += "left join crd_corp on act_acno.corp_p_seqno=crd_corp.corp_p_seqno ";
    wp.whereOrder = " ";
    // wp.whereStr = " where act_acno.p_seqno = act_acct.p_seqno ";
    // wp.whereStr+= " and act_acno.p_seqno = :p_seqno ";
    wp.whereStr = " where act_acno.acno_p_seqno = act_acct.p_seqno ";
    wp.whereStr += "   and act_acno.acno_p_seqno = :p_seqno ";
    setString("p_seqno", kkSeqno);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料(act_acct), p_seqno:" + kkSeqno);
      return -1;
    }

    String corpSeqno = wp.colStr("corp_p_seqno");
    // 檢查是否有有效卡
    isCurr = wfCorpChkCard(corpSeqno);
    String lsSumBal = "0";

    lsSumBal = getCorpSumBal(corpSeqno, wp.colStr("acct_status"), isCurr);

    if (eqIgno(wp.colStr("acct_status"), "3")) {
      wp.colSet("trans_type", "4");
      itemchangedA(); // 問題單:0001234
    }

    if ((eqIgno(wp.colStr("acct_status"), "1") || eqIgno(wp.colStr("acct_status"), "2"))
        && eqIgno(isCurr, "N")) {
      wp.colSet("trans_type", "3");
    }

    wp.colSet("bad_debt_amt", lsSumBal);
    wp.colSet("no_acct_jrnl_bal", wp.colStr("acct_jrnl_bal"));
    wp.colSet("no_end_bal", lsSumBal);

    return 1;
  }

  // 執行【queryColAcnoR】，無資料之邏輯處理。
  int wfActAcno() throws Exception {

    if (empty(kkSeqno))
      return 0;

    wp.selectSQL = " hex(rowid) as rowid ,'0' as mod_seqno "
        // + " ,p_seqno "
        + " ,acno_p_seqno as p_seqno " + " ,acct_type " + " ,acct_key " + " ,acct_status "
        + " ,corp_p_seqno " + " ,id_p_seqno " + " ,uf_corp_name(corp_p_seqno) corp_name "
        + " ,uf_idno_name(id_p_seqno) id_name " + " ,recourse_mark " + " ,recourse_mark_date "
        + " ,recourse_mark as org_recourse_mark "
        + " ,recourse_mark_date as org_recourse_mark_date " + " ,'A' as aud_code ";

    wp.daoTable = " act_acno ";
    wp.whereOrder = " ";
    // wp.whereStr = " where p_seqno = :p_seqno ";
    wp.whereStr = " where acno_p_seqno = :p_seqno ";
    setString("p_seqno", kkSeqno);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無呆帳資料, p_seqno:" + kkSeqno);
      return -1;
    }

    wp.colSet("btnDelete_disable", "disabled"); // 底層會干擾
    wp.colSet("chi_name",
        empty(wp.colStr("id_name")) ? wp.colStr("corp_name") : wp.colStr("id_name"));
    return 1;
  }

  /**
   * 公司欲轉催呆金額，將該公司(corp_p_seqno)項下所有p_seqno的金額加總 no_end_bal
   * 
   * @return
   */
  private String getCorpSumBal(String corpSeqNo, String acctStatus, String isCurr) {
    String lsSql = "";
    String lsSumBal = "0";

    if (eqIgno(acctStatus, "3")) {
      lsSql = "select nvl(sum(end_bal),0) as sum_bal from act_debt "
          + "where exists (select 1 from act_acno where corp_p_seqno = :corp_p_seqno and act_acno.p_seqno = act_debt.p_seqno) "
          + "and (acct_code = 'CB' or acct_code = 'CC' or acct_code = 'CI') ";
      setString("corp_p_seqno", corpSeqNo);
      sqlSelect(lsSql);
      // ls_sum_bal = sql_ss("sum_bal"); //有小數
      lsSumBal = numToStr(sqlNum("sum_bal"), "###0"); // 無小數
    }

    if ((eqIgno(acctStatus, "1") || eqIgno(acctStatus, "2")) && eqIgno(isCurr, "N")) {
      lsSql = "select nvl(sum(end_bal),0) as sum_bal from act_debt "
          + "where exists (select 1 from act_acno where corp_p_seqno = :corp_p_seqno and act_acno.p_seqno = act_debt.p_seqno) "
          + "and acct_code != 'DP' " + "and acct_code != 'CI' " + "and acct_code != 'CC' "
          + "and acct_code != 'CB' " + "and acct_code != 'DB' " + " ";
      setString("corp_p_seqno", corpSeqNo);
      sqlSelect(lsSql);
      // ls_sum_bal = sql_ss("sum_bal"); //有小數
      lsSumBal = numToStr(sqlNum("sum_bal"), "###0"); // 無小數
    }

    return lsSumBal;
  }

  void itemfocuschangedB() throws Exception {
    String lsSql = "";
    String lsPaperConfDate = "";
    String lsPaperName = "";
    String lsTerminateDate = "";
    double llSrcAmt = 0;

    lsPaperConfDate = wp.itemStr("paper_conf_date");
    lsPaperName = wp.itemStr("paper_name");
    llSrcAmt = wp.itemNum("src_amt");
    if (empty(lsPaperConfDate) == false && empty(lsPaperName) == false) {
      if (eqIgno(lsPaperName, "本票裁定")) {
        lsSql =
            "SELECT to_char(add_months(to_date(to_char(to_number(:paper_conf_date)),'yyyymmdd'),31),'yyyymmdd') as terminate_date "
                + "FROM dual ";
        setString("paper_conf_date", lsPaperConfDate);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          lsTerminateDate = sqlStr("terminate_date");
        }
      } else {
        lsSql = "SELECT case when to_number(TERMINATE_AMT1)*10000 <= :src_amt "
            + " then to_char(add_months(to_date(:paper_conf_date1,'yyyymmdd'),to_number(TERMINATE_YEAR1)*12+to_number(TERMINATE_MONTH1)),'yyyymmdd') "
            + " else to_char(add_months(to_date(:paper_conf_date2,'yyyymmdd'),to_number(TERMINATE_YEAR2)*12+to_number(TERMINATE_MONTH2)),'yyyymmdd') "
            + " end as terminate_date " + "from COL_PARAM ";
        setDouble("src_amt", llSrcAmt);
        setString("paper_conf_date1", lsPaperConfDate);
        setString("paper_conf_date2", lsPaperConfDate);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          lsTerminateDate = sqlStr("terminate_date");
        }
      }
      wp.colSet("terminate_date", lsTerminateDate);
    }
  }

  // 新增的功能,轉呆帳時
  // 2.憑證名稱預設為:存證信函,直接帶入欄位中
  // 3.呆帳核准日及法律文件確認日期:預設系統日
  // 4.20231118 已取消預設憑證名稱預設為:存證信函及法律文件確認日期
  void itemchangedA() throws Exception {
    //// 預設為:存證信函
    String lsWfDesc = "";
    String lsSql = "select wf_desc from ptr_sys_idtab where wf_type = 'COL_CERTIFICATE' "
        + "and id_code = 'Y' order by wf_id ";
    sqlSelect(lsSql);
    lsWfDesc = sqlStr("wf_desc");
//    wp.colSet("paper_name", lsWfDesc);

    wp.colSet("alw_bad_date", wp.sysDate);
//    wp.colSet("paper_conf_date", wp.sysDate);

    // 使用Ajax返回數據
    if (eqIgno(wp.buttonCode, "T") || eqIgno(wp.buttonCode, "AJAX")) {
//    	wp.addJSON("paper_name", lsWfDesc);
        wp.addJSON("alw_bad_date", wp.sysDate);
//        wp.addJSON("paper_conf_date", wp.sysDate);
    }
//    wp.colSet("paper_name", lsWfDesc);
    wp.colSet("alw_bad_date", wp.sysDate);
//    wp.colSet("paper_conf_date", wp.sysDate);
  }

  // (頁籤:線上轉正常、催收、呆帳戶作業)
  // 說明:
  // a. 取得營業日。
  // b. 檢查資料是否已經覆核。
  // c. 轉呆帳戶必須輸入呆帳核准日。
  // d. 檢查【原始帳戶狀態】，及其限制。
  int validationA() throws Exception {
    String lsTransType, lsAlwBadDate, lsDateS, lsDateE;
    String lsBusinessDate, lsAcctStatus, lsCurr, lsAprFlag;

    // 取得營業日
    String lsSql = "select business_date from ptr_businday ";
    sqlSelect(lsSql);
    lsBusinessDate = sqlStr("business_date");

    // 檢查資料是否已經覆核
    lsAprFlag = wp.itemStr("apr_flag");
    if (eqIgno(lsAprFlag, "Y")) {
      alertErr("此筆資料已覆核不可修改或刪除");
      return -1;
    }

    if (eqIgno(strAction, "D")) {
      return 1;
    }

    // 轉呆帳戶必須輸入呆帳核准日
    lsTransType = wp.itemStr("trans_type");
    lsAlwBadDate = wp.itemStr("alw_bad_date");
    if (eqIgno(lsTransType, "4") && empty(lsAlwBadDate)) {
      alertErr("轉呆帳戶必須輸入呆帳核准日");
      return -1;
    }

    // 檢查【原始帳戶狀態】，及其限制
    lsAcctStatus = wp.itemStr("acct_status");
    switch (lsAcctStatus) {
      case "1": // --正常
      case "2": // --逾放
        if (eqIgno(lsTransType, "3") == false) {
          if (eqIgno(lsAcctStatus, "1")) {
            alertErr("正常戶只可轉催收戶");
          } else {
            alertErr("逾放戶只可轉催收戶");
          }
          return -1;
        }

        lsDateS = wp.itemStr("no_collection_s_date");
        lsDateE = wp.itemStr("no_collection_e_date");
        if (empty(lsDateS) == false) {
          if ((toNum(lsDateS) <= toNum(lsBusinessDate))
              && (toNum(lsBusinessDate) <= toNum(lsDateE) || empty(lsDateE))) {
            alertErr("已做暫不轉催,不可轉催收");
            return -1;
          }
        }
        lsCurr = wp.itemStr("db_curr");
        if (eqIgno(lsCurr, "N") == false) {
          alertErr("該帳戶有有效卡,不可轉催收");
          return -1;
        }
        break;
      case "3": // --催收
        if (eqIgno(lsTransType, "1") == false && eqIgno(lsTransType, "4") == false) {
          alertErr("催收戶只可轉正常戶或呆帳戶");
          return -1;
        }
        break;
      case "4": // --呆帳
        if (eqIgno(lsTransType, "1") == false) {
          alertErr("呆帳戶只可轉正常戶");
          return -1;
        }
        break;
      default:
        alertErr("此帳戶目前狀況不可轉");
        return -1;
    }

    // --Set Value--
    wp.colSet("src_acct_stat", lsAcctStatus);
    funcA.varsSet("src_acct_stat", lsAcctStatus);

    return 1;
  }

  @Override
  public void saveFunc() throws Exception {
	addRetrieve = true;
    kkOptName = wp.itemStr("optname");
    switch (kkOptName) {
      case "aopt":
        saveFuncA();
        if (rc != 1) {
          listSubA(wp.colStr("acct_type"), wp.colStr("corp_p_seqno"), wp.colStr("acct_status"));
        }
        break;
      case "bopt":
        saveFuncB();
        break;
    }
  }


  // 新增 1. 執行【of_validation_1】。
  // 2. 執行【ofc_updatebefore_1】。
  // 3. Delete col_wait_trans where p_seqno = :p_seqno
  // 4. Insert col_wait_trans
  // 修改 1. 執行【of_validation_1】。
  // 2. 執行【ofc_updatebefore_1】。
  // 3. Delete col_wait_trans where p_seqno = :p_seqno
  // 4. Insert col_wait_trans
  // 刪除 1. 執行【of_validation_1】。
  // 2. Delete col_wait_trans where p_seqno = :p_seqno
  public void saveFuncA() throws Exception {
    funcA = new Colm0095FuncA(wp);

    if (validationA() < 0) {
      return;
    }
    if (strAction.equals("D") == false) {
      if (beforeUpdateA() < 0) {
        return;
      }
    }

    rc = funcA.dbSave(strAction);
    if (rc != 1) {
      alertErr2(funcA.getMsg());
    }
    this.sqlCommit(rc);

    if (strAction.equals("D") == false) {
      strAction = "R";
      addRetrieve = true;
      updateRetrieve = true;
    }
  }

  /**
   * A修改之前的校驗
   * 
   * @return
   * @throws Exception
   */
  private int beforeUpdateA() throws Exception {
    String lsTransType, lsAcctStatus;
    String lsAcctType, lsAcctKey, lsPseqno;

    // 轉呆帳戶必須輸入呆帳核准日
    lsTransType = wp.itemStr("trans_type");
    lsAcctStatus = wp.itemStr("acct_status");
    switch (lsTransType) {
      case "1":
        if (eqIgno(lsAcctStatus, "4")) {
          lsAcctType = wp.itemStr("acct_type");
          lsAcctKey = wp.itemStr("acct_key");
          lsPseqno = wp.itemStr("p_seqno");
          if (empty(lsPseqno)) {
            return 0;
          }

          String lsSql = "select acct_jrnl_bal from act_acct where p_seqno = :p_seqno ";
          setString("p_seqno", lsPseqno);
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            if (sqlNum("acct_jrnl_bal") > 0) {
              alertErr("該戶尚有呆帳金額未繳納, 不可恢復正常戶");
              return -1;
            }
          }
        }
        break;
      case "3":
        lsAcctType = wp.itemStr("acct_type");
        lsAcctKey = wp.itemStr("acct_key");
        lsPseqno = wp.itemStr("p_seqno");
        String corpSeqNo = wp.itemStr("corp_p_seqno");
        if (empty(lsPseqno)) {
          return 0;
        }

        String lsSql = "select no_collection_flag from col_acno_t where p_seqno = :p_seqno ";
        setString("p_seqno", lsPseqno);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          if (eqIgno(sqlStr("no_collection_flag"), "Y")) {
            alertErr("該戶轉暫不催收覆核中, 不可轉催收");
            return -1;
          }
        }

        // 2020/01/31 Zuwei 執行轉催動作時，若act_debt帳務含有DP爭議款科目且end_bal>0的資料，需提醒「此帳戶尚有爭議款項，不可轉催」
        // 2020/02/21 Zuwei 需要修改為僅檢核該公司(corp_p_seqno)項下所有p_seqno的爭議款金額，只要加總值>0就一律不轉催。
        lsSql = "select nvl(sum(end_bal),0) as sum_bal from act_debt "
            + "where exists (select p_seqno from act_acno where corp_p_seqno=:corp_p_seqno and act_debt.p_seqno = act_acno.p_seqno)"
            + " and (acct_code = 'DP') ";
        setString("corp_p_seqno", corpSeqNo);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          double sumBal = sqlNum("sum_bal");
          if (sumBal > 0) {
            alertErr("此統編項下帳款尚有爭議款項，不可轉催收");
            return -1;
          }
        }
        // 2023/07/21 sunny 檢核該公司(corp_p_seqno)項下所有p_seqno的金額若有任何一個帳戶含有溢繳款就一律不轉催呆。
  	  	lsSql = "select nvl(sum(END_BAL_OP),0) as sumOpBal from act_acct "
              + "where exists (select p_seqno from act_acno where corp_p_seqno=:corp_p_seqno and act_acct.p_seqno = act_acno.p_seqno and acct_type = :acct_type)"
			  + "and acct_type = :acct_type ";
        setString("corp_p_seqno", corpSeqNo);
        setString("acct_type", lsAcctType);
        setString("acct_type", lsAcctType);
          sqlSelect(lsSql);
          if (sqlRowNum > 0) {
            double sumOpBal = sqlNum("sumOpBal");
            if (sumOpBal > 0) {
              alertErr("此統編項下帳戶尚有溢繳款，不可轉催");
              return -1;
            } 
          }
        // 2023/07/21 sunny 檢核該公司(corp_p_seqno)項下所有p_seqno的金額，若有任何一個帳戶含有負餘額就一律不轉催呆。              
          lsSql = "select count(*) as jrnlbalcnt from act_acct "
                  + "where exists (select p_seqno from act_acno where corp_p_seqno=:corp_p_seqno and act_acct.p_seqno = act_acno.p_seqno)"
                  + "and acct_jrnl_bal < 0";
           setString("corp_p_seqno", corpSeqNo);
           setString("acct_type", lsAcctType);
           setString("acct_type", lsAcctType);
           sqlSelect(lsSql);
           if (sqlRowNum > 0) {
              	double jrnlbalcnt = sqlNum("jrnlbalcnt");
             if (jrnlbalcnt > 0) {
           	alertErr("此統編項下帳戶尚有負餘額帳戶，不可轉催");
               return -1;
             }
        }
        break;
      case "4":
    	  lsAcctType = wp.itemStr("acct_type");
    	  corpSeqNo = wp.itemStr("corp_p_seqno");
    	// 2023/07/21 sunny 檢核該公司(corp_p_seqno)項下所有p_seqno的金額，若有任何一個帳戶含有溢繳款就一律不轉呆。
    	  lsSql = "select count(*) as jrnlbalcnt from act_acct "
                  + "where exists (select p_seqno from act_acno where corp_p_seqno=:corp_p_seqno and act_acct.p_seqno = act_acno.p_seqno and acct_type = :acct_type)"
                  + "and acct_jrnl_bal < 0 and acct_type = :acct_type ";
            setString("corp_p_seqno", corpSeqNo);
            setString("acct_type", lsAcctType);
            setString("acct_type", lsAcctType);
            sqlSelect(lsSql);
            if (sqlRowNum > 0) {
              double sumOpBal = sqlNum("sumOpBal");
              if (sumOpBal > 0) {
                alertErr("此統編項下帳戶尚有溢繳款，不可轉呆");
                return -1;
              } 
            }
            
            // 2023/07/21 sunny 檢核該公司(corp_p_seqno)項下所有p_seqno的金額，若有任何一個帳戶含有負餘額就一律不轉呆。              
            lsSql = "select count(*) as jrnlbalcnt from act_acct "
                   + "where exists (select p_seqno from act_acno where corp_p_seqno=:corp_p_seqno and act_acct.p_seqno = act_acno.p_seqno and acct_type = :acct_type)"
                   + "and acct_jrnl_bal < 0 and acct_type = :acct_type ";
            setString("corp_p_seqno", corpSeqNo);
            setString("acct_type", lsAcctType);
            setString("acct_type", lsAcctType);
            sqlSelect(lsSql);
            if (sqlRowNum > 0) {
               	double jrnlbalcnt = sqlNum("jrnlbalcnt");
              if (jrnlbalcnt > 0) {
            	alertErr("此統編項下帳戶尚有負餘額帳戶，不可轉呆");
                return -1;
              }
            }
//        break;
       return 1;
    }

    // --trans_type:4-呆帳, 3-催收, 1-正常; 只有 4-呆帳才能輸入下面資料--
    wp.colSet("alw_bad_date", "");
    wp.colSet("paper_conf_date", "");
    wp.colSet("paper_name", "");

    return 1;
  }

  public void saveFuncB() throws Exception {
    funcB = new Colm0095FuncB(wp);

    rc = funcB.dbSave(strAction);
    if (rc != 1) {
      alertErr2(funcB.getMsg());
    }
    this.sqlCommit(rc);

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
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("kk_acct_type");
        dddwList("PtrCertificateList", "ptr_sys_idtab", "wf_desc", "wf_desc",
            "where wf_type = 'COL_CERTIFICATE' order by wf_id ");
      } else {
        wp.optionKey = wp.itemStr("exAcctType");
        dddwList("PtrAcctTypeList", "ptr_acct_type", "acct_type", "acct_type||' ['||chin_name||']'",
            "where card_indicator = '2' and no_collection_flag <> 'Y' order by acct_type");
      }
    } catch (Exception ex) {
    }
  }
}
