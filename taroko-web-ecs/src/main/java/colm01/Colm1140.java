/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/21  V1.00.00   Ryan       program initial                           *
*  108/12/19  V1.00.01   phopho     change table: prt_branch -> gen_brn       *
*  109-05-06  V1.00.02  Aoyulan       updated for project coding standard     *
* 109-06-29  V1.00.02  Zuwei        fix code scan issue
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package colm01;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoPDF;

public class Colm1140 extends BaseEdit {
  CommString commString = new CommString();
  Colm1140Func func;
  int i = 0;
  int rr = -1;
  String strIdPSeqno = "", kkLiadDocNo = "", kkIdNo = "", kkCaseLetter = "", kkIdPSeqno = "",
      lsIdCase = "", kkRenewStatus = "", kkLiadDocSeqno;
  String mProgName = "";
  String idPSeqno = "", chiName = "", liadDocNo = "", cardNum = "", acctStatus = "", mCode = "",
      badDebtAmt = "", demandAmt = "", debtAmt = "", renewFlag = "", liquiFlag = "",
      manulProcFlag = "";
  double renewLoseAmt = 0;
  double liquLoseAmt = 0;
  int idcAmtOrgDebt = 0;
  int cnt = 0;
  ArrayList<String> errlist = new ArrayList<String>();

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
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "X1")) {
      /* clm1150_detl轉換顯示畫面 */
      strAction = "X1";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "");
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "X2")) {
      /* 轉換顯示畫面 */
      strAction = "X2";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "X3")) {
      /* 轉換顯示畫面 */
      strAction = "X3";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "X4")) {
      /* 轉換顯示畫面 */
      strAction = "X4";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "X5")) {
      /* 轉換顯示畫面 */
      strAction = "X5";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "X6")) {
      /* 轉換顯示畫面 */
      strAction = "X6";
      String liadDocNo = wp.itemStr("liad_doc_no");
      String dbIdNo = wp.itemStr("id_no");
      String chiName = wp.itemStr("chi_name");
      String caseLetterDesc = wp.itemStr("case_letter_desc");
      clearFunc();
      wp.colSet("db_id_no", dbIdNo);
      wp.colSet("chi_name", chiName);
      wp.colSet("liad_doc_no", liadDocNo);
      wp.colSet("liad_doc_seqno", 0);
      wp.colSet("case_letter_desc", caseLetterDesc);
    } else if (eqIgno(wp.buttonCode, "X7")) {
      /* 轉換顯示畫面 */
      strAction = "X7";
      String liadDocNo = wp.itemStr("liad_doc_no");
      String dbIdNo = wp.itemStr("id_no");
      String chiName = wp.itemStr("chi_name");
      String caseLetterDesc = wp.itemStr("case_letter_desc");
      clearFunc();
      wp.colSet("db_id_no", dbIdNo);
      wp.colSet("chi_name", chiName);
      wp.colSet("liad_doc_no", liadDocNo);
      wp.colSet("liad_doc_seqno", 0);
      wp.colSet("case_letter_desc", caseLetterDesc);
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      /* 匯入Z60 */
      strAction = "UPLOAD";
      fileZ60();
    } else if (eqIgno(wp.buttonCode, "S1")) {
      /* colm1150_detl查詢 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "");
      dataReadA();
    } else if (eqIgno(wp.buttonCode, "R1")) {
      /* colm1150_detl讀取 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      dataReadA();
    } else if (eqIgno(wp.buttonCode, "A1")) {
      /* clm1150_detl新增 */
      strAction = "A";
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      saveFuncZ60();
    } else if (eqIgno(wp.buttonCode, "U1")) {
      /* colm1150_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncZ60();
    } else if (eqIgno(wp.buttonCode, "D1")) {
      /* colm1150_detl刪除 */
      strAction = "D";
      saveFuncZ60();
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
    } else if (eqIgno(wp.buttonCode, "CU1")) {
      /* colm1150_detl取消修改 */
      strAction = "CU1";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* colm1160_detl查詢 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "");
      dataReadB();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      /* colm1160_detl讀取 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      dataReadB();
    } else if (eqIgno(wp.buttonCode, "A2")) {
      /* clm1160_detl新增 */
      strAction = "A";
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      saveFuncB();
    } else if (eqIgno(wp.buttonCode, "U2")) {
      /* colm1160_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncB();
    } else if (eqIgno(wp.buttonCode, "D2")) {
      /* colm1160_detl刪除 */
      strAction = "D";
      saveFuncB();
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
    } else if (eqIgno(wp.buttonCode, "CU2")) {
      /* colm1160_detl取消修改 */
      strAction = "CU2";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    } else if (eqIgno(wp.buttonCode, "S3")) {
      /* colm1170_detl查詢 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "");
      dataReadC();
    } else if (eqIgno(wp.buttonCode, "R3")) {
      /* colm1170_detl讀取 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      dataReadC();
    } else if (eqIgno(wp.buttonCode, "A3")) {
      /* clm1170_detl新增 */
      strAction = "A";
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      saveFuncC();
    } else if (eqIgno(wp.buttonCode, "U3")) {
      /* colm1170_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncC();
    } else if (eqIgno(wp.buttonCode, "D3")) {
      /* colm1170_detl刪除 */
      strAction = "D";
      saveFuncC();
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
    } else if (eqIgno(wp.buttonCode, "CU3")) {
      /* colm1170_detl取消修改 */
      strAction = "CU3";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    } else if (eqIgno(wp.buttonCode, "S4")) {
      /* colm1180_detl查詢 */

      dataReadD();
    } else if (eqIgno(wp.buttonCode, "R4")) {
      /* colm1180_detl讀取 */
      // wp.col_set("btnAdd", "style='background: lightgray;' disabled");

      dataReadD();
    } else if (eqIgno(wp.buttonCode, "A4")) {
      /* clm1180_detl新增 */
      strAction = "A";
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      saveFuncD();
    } else if (eqIgno(wp.buttonCode, "U4")) {
      /* colm1180_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncD();
    } else if (eqIgno(wp.buttonCode, "D4")) {
      /* colm1180_detl刪除 */
      strAction = "D";
      saveFuncD();
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
    } else if (eqIgno(wp.buttonCode, "CU4")) {
      /* colm1180_detl取消修改 */
      strAction = "CU4";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    } else if (eqIgno(wp.buttonCode, "S5")) {
      /* colm1190_detl查詢 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "");
      dataReadE();
    } else if (eqIgno(wp.buttonCode, "R5")) {
      /* colm1190_detl讀取 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      dataReadE();
    } else if (eqIgno(wp.buttonCode, "U5")) {
      /* colm1190_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncE();
    } else if (eqIgno(wp.buttonCode, "CU5")) {
      /* colm1190_detl取消修改 */
      strAction = "CU5";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    } else if (eqIgno(wp.buttonCode, "S5_1")) {
      /* colm1192_detl查詢 */
      dataReadE();
    } else if (eqIgno(wp.buttonCode, "R5_1")) {
      /* colm1192_detl讀取 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      dataReadE();
    } else if (eqIgno(wp.buttonCode, "U5_1")) {
      /* colm1192_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncE1();
    } else if (eqIgno(wp.buttonCode, "CU5_1")) {
      /* colm1192_detl取消修改 */
      strAction = "CU5_1";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    } else if (eqIgno(wp.buttonCode, "S6")) {
      /* colm1196_detl查詢 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "");
      dataReadF();
    } else if (eqIgno(wp.buttonCode, "R6")) {
      /* colm1196_detl讀取 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      dataReadF();
    } else if (eqIgno(wp.buttonCode, "A6")) {
      /* clm1196_detl新增 */
      strAction = "A";
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      saveFuncF();
    } else if (eqIgno(wp.buttonCode, "U6")) {
      /* colm1196_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncF();
    } else if (eqIgno(wp.buttonCode, "D6")) {
      /* colm1196_detl刪除 */
      strAction = "D";
      saveFuncF();
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
    } else if (eqIgno(wp.buttonCode, "CU6")) {
      /* colm1196_detl取消修改 */
      strAction = "CU6";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    } else if (eqIgno(wp.buttonCode, "S7")) {
      /* colm1197_detl查詢 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "");
      dataReadG();
    } else if (eqIgno(wp.buttonCode, "R7")) {
      /* colm1197_detl讀取 */
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      dataReadG();
    } else if (eqIgno(wp.buttonCode, "A7")) {
      /* clm1197_detl新增 */
      strAction = "A";
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      saveFuncG();
    } else if (eqIgno(wp.buttonCode, "U7")) {
      /* colm1197_detl修改 */
      strAction = "U";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      saveFuncG();
    } else if (eqIgno(wp.buttonCode, "D7")) {
      /* colm1197_detl刪除 */
      strAction = "D";
      saveFuncG();
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
    } else if (eqIgno(wp.buttonCode, "CU7")) {
      /* colm1197_detl取消修改 */
      strAction = "CU7";
      wp.colSet("btnAdd", "style='background: lightgray;' disabled");
      wfDeleteModTmp();
    }


    dddwSelect();
    initButton();
  }

  void getWhereStr() {

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_id")) == false) {
      wp.whereStr += " and  id_p_seqno in(select id_p_seqno from crd_idno where id_no = :ex_id) ";
      setString("ex_id", wp.itemStr("ex_id"));
    }

    if (empty(wp.itemStr("ex_recv_date1")) == false) {
      wp.whereStr += " and  recv_date >= :ex_recv_date1 ";
      setString("ex_recv_date1", wp.itemStr("ex_recv_date1"));
    }

    if (empty(wp.itemStr("ex_recv_date2")) == false) {
      wp.whereStr += " and  recv_date <= :ex_recv_date2 ";
      setString("ex_recv_date2", wp.itemStr("ex_recv_date2"));
    }

    if (empty(wp.itemStr("ex_recv_yymm1")) == false) {
      wp.whereStr += " and  substr(recv_date, 1, 6) >= :ex_recv_yymm1 ";
      setString("ex_recv_yymm1", wp.itemStr("ex_recv_yymm1"));
    }

    if (empty(wp.itemStr("ex_recv_yymm2")) == false) {
      wp.whereStr += " and  substr(recv_date, 1, 6) <= :ex_recv_yymm2 ";
      setString("ex_recv_yymm2", wp.itemStr("ex_recv_yymm2"));
    }

  }


  @Override
  public void queryFunc() throws Exception {
    String lsDate1 = wp.itemStr("ex_recv_date1");
    String lsDate2 = wp.itemStr("ex_recv_date2");
    String lsDate3 = wp.itemStr("ex_recv_yymm1");
    String lsDate4 = wp.itemStr("ex_recv_yymm2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[收件日期-起迄]  輸入錯誤");
      return;
    }
    if (this.chkStrend(lsDate3, lsDate4) == false) {
      alertErr2("[收件日期(年/月)-起迄]  輸入錯誤");
      return;
    }
    wp.colSet("ex_chi_name", "");
    if (!empty(wp.itemStr("ex_id"))) {
      String sqlSelect = "select chi_name from crd_idno where id_no = :ex_id ";
      setString("ex_id", wp.itemStr("ex_id"));
      sqlSelect(sqlSelect);
      wp.colSet("ex_chi_name", sqlStr("chi_name"));

      if (wp.itemStr("ex_id").length() != 10) {
        alertErr2("身分證 ID需輸入10碼");
        return;
      }
    }

    if (empty(wp.itemStr("ex_id")) && empty(wp.itemStr("ex_recv_date1"))
        && empty(wp.itemStr("ex_recv_date2")) && empty(wp.itemStr("ex_liad_doc_no"))
        && empty(wp.itemStr("ex_recv_yymm1")) && empty(wp.itemStr("ex_recv_yymm2"))
        && empty(wp.itemStr("ex_renew_status"))) {
      alertErr2("請輸入任一查詢條件");
      return;
    }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    try {

      queryReadA();
      queryReadB();
      queryReadC();
      queryReadD();
      queryReadE();

    } catch (Exception ex) {

    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {}

  @Override
  public void initPage() {
    wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
    wp.colSet("btnDelete", "style='background: lightgray;' disabled");
    wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
    wp.colSet("btnAdd", "");
    wp.colSet("case_year", "0");
    wp.colSet("DEFAULT_CHK", "checked");
    wp.colSet("db_int_num", "0");
    wp.colSet("db_int_amt", "0");
  }

  void queryReadA() throws Exception {
    daoTid = "A-";
    wp.pageControl();
    wp.selectSQL = " liad_doc_no " + " ,id_no " + " ,id_p_seqno " + " ,chi_name " + " ,recv_date "
        + " ,renew_flag " + " ,liqui_flag " + " ,manul_proc_flag ";

    wp.daoTable = " col_liad_z60 ";
    wp.whereOrder = "";
    getWhereStr();
    if (empty(wp.itemStr("ex_liad_doc_no")) == false) {
      wp.whereStr += " and  liad_doc_no = :ex_liad_doc_no ";
      setString("ex_liad_doc_no", wp.itemStr("ex_liad_doc_no"));
    }

    pageQuery();
    wp.setListCount(1);
    cnt += wp.selectCnt;
    if (sqlNotFind()) {

    }

    daoTid = "a-";
  }

  void queryReadB() throws Exception {
    daoTid = "B-";
    wp.selectSQL = " liad_doc_no " + " ,id_no " + " ,id_p_seqno " + " ,chi_name " + " ,recv_date "
        + " ,case_letter " + " ,renew_status " + " ,renew_accetp_no ";
    wp.daoTable = " col_liad_renew ";
    wp.whereOrder = " order by liad_doc_no ";
    getWhereStr();
    if (empty(wp.itemStr("ex_renew_status")) == false) {
      wp.whereStr += " and  renew_status = :ex_renew_status ";
      setString("ex_renew_status", wp.itemStr("ex_renew_status"));
    }
    if (empty(wp.itemStr("ex_liad_doc_no")) == false) {
      wp.whereStr += " and  liad_doc_no = :ex_liad_doc_no ";
      setString("ex_liad_doc_no", wp.itemStr("ex_liad_doc_no"));
    }

    pageQuery();
    wp.setListCount(2);
    cnt += wp.selectCnt;
    if (sqlNotFind()) {

    }

    daoTid = "b-";
    listWkdataB();
  }

  void queryReadC() throws Exception {
    daoTid = "C-";
    wp.selectSQL = " liad_doc_no " + " ,id_no " + " ,id_p_seqno " + " ,chi_name " + " ,recv_date "
        + " ,case_letter " + " ,liqu_status " + " ,judic_avoid_no ";

    wp.daoTable = " col_liad_liquidate ";
    wp.whereOrder = " order by liad_doc_no ";
    getWhereStr();
    if (!wp.itemStr("ex_judic_avoid_flag").equals("0")) {
      wp.whereStr += " and  judic_avoid_flag = :ex_judic_avoid_flag ";
      setString("ex_judic_avoid_flag", wp.itemStr("ex_judic_avoid_flag"));
    }
    if (empty(wp.itemStr("ex_liad_doc_no")) == false) {
      wp.whereStr += " and  liad_doc_no = :ex_liad_doc_no ";
      setString("ex_liad_doc_no", wp.itemStr("ex_liad_doc_no"));
    }

    pageQuery();
    wp.setListCount(3);
    cnt += wp.selectCnt;
    if (sqlNotFind()) {

    }

    daoTid = "c-";
    listWkdataC();
  }

  void queryReadD() throws Exception {
    daoTid = "D-";

    getWhereStr();
    if (empty(wp.itemStr("ex_liad_doc_no")) == false) {
      wp.whereStr += " and  liad_doc_no = :ex_liad_doc_no ";
      setString("ex_liad_doc_no", wp.itemStr("ex_liad_doc_no"));
    }

    if (empty(wp.itemStr("ex_renew_status2")) == false) {
      wp.whereStr += " and  renew_status = :ex_renew_status2 ";
      setString("ex_renew_status2", wp.itemStr("ex_renew_status2"));
    }

    wp.sqlCmd = " select a.id_p_seqno, " + " a.case_letter, " + " b.id_no as db_id_no, "
        + " b.chi_name as db_chi_name, " + " b.renew_status as db_renew_status, "
        + " b.court_status as db_court_status, " + " b.liad_doc_no as db_liad_doc_no, "
        + " b.judic_date as db_judic_date, " + " b.org_debt_amt as db_org_debt_amt, "
        + " b.payoff_amt as db_payoff_amt, " + " b.renew_accetp_no as db_renew_accetp_no, "
        + " nvl((select sum (act_per_amt) from col_liad_paydetl where holder_id_p_seqno = a.id_p_seqno and case_letter = a.case_letter and pay_date <= to_char(sysdate,'yyyymmdd') and liad_type = '1'),0) as db_act_tot_amt, "
        + " nvl((select max (pay_date) from col_liad_paydetl where holder_id_p_seqno = a.id_p_seqno and case_letter = a.case_letter and pay_date <= to_char(sysdate,'yyyymmdd') and liad_type = '1'),'') as ls_max_pay_date "
        + " from ( " + " select id_p_seqno " + " ,max (lpad (' ', 10, ' ')) db_id_no "
        + " ,max (lpad (' ', 20, ' ')) db_chi_name " + " ,case_letter "
        + " ,max (col_liad_renew.renew_status) db_renew_status "
        + " ,max (col_liad_renew.court_status) db_court_status "
        + " ,max (col_liad_renew.liad_doc_no) db_liad_doc_no "
        + " ,max (col_liad_renew.recv_date) db_recv_date "
        + " ,max (col_liad_renew.judic_date) db_judic_date " + " ,sum (0) db_org_debt_amt "
        + " ,sum (0) db_payoff_amt " + " ,sum (0) db_act_tot_amt " + " ,sum (0) db_renew_accetp_no "
        + " from col_liad_renew " + wp.whereStr + " AND apr_flag = 'Y' "
        + " GROUP BY id_p_seqno, case_letter " + " ) as a " + " left join col_liad_renew as b "
        + " on  a.id_p_seqno = b.id_p_seqno " + " AND a.case_letter = b.case_letter "
        + " AND a.db_recv_date = b.recv_date  ";

    pageQuery();
    wp.setListCount(4);
    cnt += wp.selectCnt;
    if (sqlNotFind()) {

    }

    daoTid = "d-";
  }

  void queryReadE() throws Exception {
    daoTid = "E-";
    wp.selectSQL = " id_p_seqno " + " ,id_no " + " ,case_letter " + " ,recv_date "
        + " ,apply_bank_no " + " ,coll_apply_date " + " ,judic_date " + " ,renew_status "
        + " ,last_pay_date " + " ,last_pay_amt " + " ,pay_normal_flag_1 " + " ,pay_normal_flag_2 "
        + " ,debt_amt1 " + " ,debt_amt2 " + " ,alloc_debt_amt " + " ,unalloc_debt_amt "
        + " ,coll_remark " + " ,send_flag_571 " + " ,proc_date_571 " + " ,crt_user " + " ,crt_date "
        + " ,apr_date " + " ,apr_user " + " ,close_reason " + " ,close_remark " + " ,close_date "
        + " ,close_user " + " ,close_apr_date " + " ,close_apr_user " + " ,close_proc_date "
        + " ,idno_name " + " ,bank_name ";

    wp.daoTable = " col_liad_570 ";
    wp.whereOrder = " ";
    getWhereStr();
    pageQuery();
    wp.setListCount(5);
    cnt += wp.selectCnt;
    if (cnt == 0) {
      wp.notFound = "Y";
    } else {
      wp.notFound = "N";
    }
    if (sqlNotFind()) {

    }

    daoTid = "e-";
  }

  void queryReadF() throws Exception {

    kkLiadDocNo = itemKk("data_k1");
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }
    wp.selectSQL =
        " id_p_seqno " + " ,liad_doc_no as liad_doc_no_f " + " ,UF_IDNO_ID(id_p_seqno) db_id_no "
            + " ,chi_name " + " ,unit_doc_no " + " ,recv_date as recv_date_f " + " ,key_note "
            + " ,case_letter_desc as case_letter_desc_f " + " ,case_date " + " ,liad_doc_seqno ";

    wp.daoTable = " col_liad_renew_court ";
    wp.whereOrder = " ";
    wp.whereStr = " where 1=1 and liad_doc_no = :liad_doc_no ";
    setString("liad_doc_no", kkLiadDocNo);
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      wp.notFound = "N";
    }
    listWkdataFg(wp.selectCnt, "RENEWCOURT");
  }

  void queryReadG() throws Exception {

    kkLiadDocNo = itemKk("data_k1");
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }
    wp.selectSQL =
        " id_p_seqno " + " ,liad_doc_no as liad_doc_no_f " + " ,UF_IDNO_ID(id_p_seqno) db_id_no "
            + " ,chi_name " + " ,unit_doc_no " + " ,recv_date as recv_date_f " + " ,key_note "
            + " ,case_letter_desc as case_letter_desc_f " + " ,case_date " + " ,liad_doc_seqno ";

    wp.daoTable = " col_liad_liquidate_court ";
    wp.whereOrder = " ";
    wp.whereStr = " where 1=1 and liad_doc_no = :liad_doc_no ";
    setString("liad_doc_no", kkLiadDocNo);
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      wp.notFound = "N";
    }
    listWkdataFg(wp.selectCnt, "LIQUICOURT");
  }

  void dataReadA() throws Exception {
    kkLiadDocNo = itemKk("data_k1");
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }
    if (!empty(kkLiadDocNo)) {
      wp.selectSQL = " hex(rowid) as rowid " + " ,liad_doc_no " + " ,id_no " + " ,id_p_seqno "
          + " ,chi_name " + " ,recv_date " + " ,acct_status " + " ,m_code " + " ,bad_debt_amt "
          + " ,demand_amt " + " ,debt_amt " + " ,card_num " + " ,credit_branch "
          + " ,branch_comb_flag " + " ,court_id " + " ,court_name " + " ,case_year "
          + " ,case_letter " + " ,case_no " + " ,bullet_date " + " ,bullet_desc " + " ,data_date "
          + " ,crt_user " + " ,crt_date " + " ,mod_user " + " ,mod_time " + " ,mod_pgm "
          + " ,mod_seqno " + " ,renew_flag " + " ,liqui_flag " + " ,apr_flag ";

      wp.daoTable = " col_liad_z60 ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr += " and liad_doc_no = :liad_doc_no ";
      setString("liad_doc_no", kkLiadDocNo);
      pageSelect();
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }

      // dddw_select();
      // wp.setListCount(1);
      gotoZ60Update();
    } else {
      alertErr("無法取得 [文件編號] 請回前畫面選取異動資料 或 執行 [新增]");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnAdd", "");
      if (!empty(wp.itemStr("id_no"))) {
        wp.selectSQL = " id_p_seqno " + " ,chi_name ";
        wp.daoTable = " crd_idno ";
        wp.whereOrder = " ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr += " and id_no = :id_no";
        setString("id_no", wp.itemStr("id_no"));
        pageSelect();
        if (sqlNotFind()) {
          alertErr(appMsg.errCondNodata);
          return;
        }
      }
    }
  }

  void dataReadB() throws Exception {
    kkLiadDocNo = itemKk("data_k1");
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }
    if (!empty(kkLiadDocNo)) {
      wp.selectSQL = " hex(rowid) as rowid " + " ,liad_doc_no " + " ,id_no " + " ,id_p_seqno "
          + " ,chi_name " + " ,recv_date " + " ,acct_status " + " ,m_code " + " ,bad_debt_amt "
          + " ,demand_amt " + " ,debt_amt " + " ,acct_num " + " ,card_num " + " ,renew_status "
          + " ,renew_status as o_renew_status " + " ,branch_comb_flag " + " ,credit_branch "
          + " ,decode(max_bank_flag,'','N',max_bank_flag) as max_bank_flag " + " ,org_debt_amt "
          + " ,org_debt_amt_bef " + ",org_debt_amt_bef_base " + " ,renew_lose_amt " + " ,court_id "
          + " ,court_name " + " ,doc_chi_name " + " ,court_dept " + " ,payoff_amt "
          + " ,payment_day " + " ,court_status " + " ,court_status as o_court_status "
          + " ,case_year " + " ,case_letter " + " ,case_letter_desc " + " ,judic_date "
          + " ,judic_action_flag " + " ,action_date_s " + " ,judic_cancel_flag " + " ,cancel_date "
          + " ,renew_cancel_date " + " ,deliver_date " + " ,renew_first_date "
          + " ,renew_last_date " + " ,renew_int " + " ,renew_rate " + " ,confirm_date "
          + " ,run_renew_flag " + " ,renew_damage_date " + " ,renew_accetp_no " + " ,crt_user "
          + " ,crt_date " + " ,apr_flag " + " ,apr_date " + " ,apr_user " + " ,mod_user "
          + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno " + " ,case_date as case_date_m "
          + " ,super_name "// V0.6
      ;

      wp.daoTable = " col_liad_renew ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr += " and liad_doc_no = :liad_doc_no ";
      setString("liad_doc_no", kkLiadDocNo);
      pageSelect();
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }

      // dddw_select();
      // wp.setListCount(1);
      gotoRenewUpdate();
    } else {
      alertErr("無法取得 [文件編號] 請回前畫面選取異動資料 或 執行 [新增]");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnAdd", "");
      if (!empty(wp.itemStr("id_no"))) {
        wp.selectSQL = " id_p_seqno " + " ,chi_name ";
        wp.daoTable = " crd_idno ";
        wp.whereOrder = " ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr += " and id_no = :id_no";
        setString("id_no", wp.itemStr("id_no"));
        pageSelect();
        if (sqlNotFind()) {
          alertErr(appMsg.errCondNodata);
          return;
        }
      }
    }
    queryReadF();
  }

  void dataReadC() throws Exception {
    kkLiadDocNo = itemKk("data_k1");
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }
    if (!empty(kkLiadDocNo)) {
      wp.selectSQL = " hex(rowid) as rowid " + " ,liad_doc_no " + " ,id_no " + " ,id_p_seqno "
          + " ,chi_name " + " ,recv_date " + " ,acct_status " + " ,m_code " + " ,bad_debt_amt "
          + " ,demand_amt " + " ,debt_amt " + " ,acct_num " + " ,card_num " + " ,liqu_status "
          + " ,liqu_status as o_liqu_status "
          + " ,decode(branch_comb_flag,'','N',branch_comb_flag) as branch_comb_flag "
          + " ,credit_branch " + " ,decode(max_bank_flag,'','N',max_bank_flag) as max_bank_flag "
          + " ,org_debt_amt " + " ,org_debt_amt_bef " + " ,org_debt_amt_bef_base "
          + " ,liqu_lose_amt " + " ,court_id " + " ,court_name " + " ,doc_chi_name "
          + " ,court_dept " + " ,court_status " + " ,court_status as o_court_status "
          + " ,case_year " + " ,case_letter " + " ,case_letter_desc "
          + " ,decode(judic_avoid_flag,'','N',judic_avoid_flag) as judic_avoid_flag "
          + " ,decode(judic_avoid_sure_flag,'','N',judic_avoid_sure_flag) as judic_avoid_sure_flag "
          + " ,judic_avoid_no " + " ,judic_date "
          + " ,decode(judic_action_flag,'','N',judic_action_flag) as judic_action_flag "
          + " ,action_date_s "
          + " ,decode(judic_cancel_flag,'','N',judic_cancel_flag) as judic_cancel_flag "
          + " ,cancel_date " + " ,credit_print_flag " + " ,credit_print_date "
          + " ,credit_print_user " + " ,jcic_send_flag " + " ,jcic_send_date " + " ,jcic_send_user "
          + " ,crt_user " + " ,crt_date " + " ,apr_flag " + " ,apr_date " + " ,apr_user "
          + " ,mod_user " + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno "
          + " ,decode(law_133_flag,'','N',law_133_flag) as law_133_flag "
          + ",case_date as case_date_m";

      wp.daoTable = " col_liad_liquidate ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr += " and liad_doc_no = :liad_doc_no ";
      setString("liad_doc_no", kkLiadDocNo);
      pageSelect();
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }

      // dddw_select();
      // wp.setListCount(1);
      gotoLiquUpdate();
    } else {
      alertErr("無法取得 [文件編號] 請回前畫面選取異動資料 或 執行 [新增]");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnAdd", "");
      if (!empty(wp.itemStr("id_no"))) {
        wp.selectSQL = " id_p_seqno " + " ,chi_name ";
        wp.daoTable = " crd_idno ";
        wp.whereOrder = " ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr += " and id_no = :id_no";
        setString("id_no", wp.itemStr("id_no"));
        pageSelect();
        if (sqlNotFind()) {
          alertErr(appMsg.errCondNodata);
          return;
        }
      }
    }
    queryReadG();
  }

  void dataReadD() throws Exception {
    // wp.pageControl();
    wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
    wp.colSet("btnUpdate", "");
    kkIdPSeqno = itemKk("data_k1");
    kkCaseLetter = itemKk("data_k2");
    kkIdNo = itemKk("data_k_id_no");
    String kkRecvDate = itemKk("data_k_db_recv_date");
    kkRenewStatus = itemKk("data_k_db_renew_status");
    String kkOrgDebtAmt = itemKk("data_k_db_org_debt_amt");
    String kkChiName = itemKk("data_k_db_chi_name");
    String kkCourtStatus = itemKk("data_k_db_court_status");
    String kkLiadDocNo = itemKk("data_k_db_liad_doc_no");

    if (empty(kkIdPSeqno)) {
      kkIdPSeqno = wp.itemStr("id_p_seqno");
    }
    if (empty(kkCaseLetter)) {
      kkCaseLetter = wp.itemStr("case_letter");
    }
    if (empty(kkIdNo)) {
      kkIdNo = wp.itemStr("id_no");
    }
    if (empty(kkOrgDebtAmt)) {
      kkOrgDebtAmt = wp.itemStr("org_debt_amt");
    }
    if (empty(kkRecvDate)) {
      kkRecvDate = wp.itemStr("recv_date");
    }
    if (empty(kkChiName)) {
      kkChiName = wp.itemStr("chi_name");
    }
    if (empty(kkRenewStatus)) {
      kkRenewStatus = wp.itemStr("renew_status");
    }
    if (empty(kkCourtStatus)) {
      kkCourtStatus = wp.itemStr("court_status");
    }
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }

    wp.colSet("btnDelete", "");
    wp.colSet("id_p_seqno", kkIdPSeqno);
    wp.colSet("id_no", kkIdNo);
    wp.colSet("case_letter", kkCaseLetter);
    wp.colSet("org_debt_amt", kkOrgDebtAmt);
    wp.colSet("recv_date", kkRecvDate);
    wp.colSet("chi_name", kkChiName);
    wp.colSet("renew_status", kkRenewStatus);
    wp.colSet("court_status", kkCourtStatus);
    wp.colSet("liad_doc_no", kkLiadDocNo);

    if (!empty(kkIdNo) && !empty(kkCaseLetter)) {
      wp.selectSQL = " hex(rowid) as rowid " + " ,inst_seq " + " ,inst_date_s " + " ,inst_date_e "
          + " ,ar_per_amt " + " ,act_per_amt " + " ,pay_date " + " ,ar_tot_amt " + " ,act_tot_amt "
          + " ,unpay_amt " + " ,payment_day " + " ,mod_user " + " ,mod_time " + ",from_type "
          + ",(Case When ( from_type = 'Y' or act_per_amt = 0 ) and from_type != 'F' Then ''  Else 'disabled' End) as d_disabled "
          + ",(Case When from_type = 'Y'  Then 'Y.人工補分期資料'  When from_type = 'F'  Then 'F.補分期資料批次處理完成' End) as tt_from_type ";

      wp.daoTable = " col_liad_install ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr +=
          " and holder_id_p_seqno = :holder_id_p_seqno " + " and case_letter = :case_letter ";
      setString("holder_id_p_seqno", kkIdPSeqno);
      setString("case_letter", kkCaseLetter);
      // pageSelect();
      pageQuery();
      wp.colSet("num", wp.selectCnt + "");
      wp.setListCount(1);
      wp.notFound = "N";
      if (sqlNotFind()) {
        // alert_err(AppMsg.err_condNodata);
        // return;
      }
      gotoRenewAptUpdate();
      // dddw_select();
    } else {
      alertErr("無法取得 [文件編號] 請回前畫面選取異動資料 ");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnAdd", "");
      return;
    }
  }

  void dataReadE() throws Exception {
    kkIdNo = itemKk("data_k1");
    kkCaseLetter = itemKk("data_k2");
    if (empty(kkIdNo)) {
      kkIdNo = wp.itemStr("id_no");
    }
    if (empty(kkCaseLetter)) {
      kkCaseLetter = wp.itemStr("case_letter");
    }
    if (!empty(kkIdNo) && !empty(kkCaseLetter)) {
      wp.selectSQL = " hex(rowid) as rowid " + " ,id_no " + " ,id_p_seqno " + " ,idno_name "
          + " ,case_letter " + " ,recv_date " + " ,apply_bank_no " + " ,bank_name "
          + " ,coll_apply_date " + " ,judic_date " + " ,renew_status " + " ,last_pay_date "
          + " ,last_pay_amt " + " ,pay_normal_flag_1 " + " ,pay_normal_flag_2 " + " ,debt_amt1 "
          + " ,debt_amt2 " + " ,alloc_debt_amt " + " ,unalloc_debt_amt " + " ,coll_remark "
          + " ,send_flag_571 " + " ,proc_date_571 " + " ,crt_user " + " ,crt_date " + " ,apr_date "
          + " ,apr_user " + " ,close_reason " + " ,close_remark " + " ,close_date "
          + " ,close_user " + " ,close_apr_date " + " ,close_apr_user " + " ,close_proc_date "
          + " ,mod_user " + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno ";

      wp.daoTable = " col_liad_570 ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr += " and id_no = '" + kkIdNo + "' ";
      wp.whereStr += " and case_letter = :case_letter ";
      setString("case_letter", kkCaseLetter);
      pageSelect();
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }
      // dddw_select();
      if (strAction.equals("S5_1") || strAction.equals("R5_1") || strAction.equals("CU5_1")) {
        gotoCollCloseUpdate();
      } else {
        gotoColldataUpdate();
      }
      // wp.setListCount(1);
    } else {
      alertErr("【無法取得 [身分證ID 及 發文字號] 請回前畫面選取異動資料】");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnAdd", "");
    }
  }

  void dataReadF() throws Exception {
    kkLiadDocNo = itemKk("data_k1");
    kkLiadDocSeqno = itemKk("data_k2");
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }
    if (empty(kkLiadDocSeqno)) {
      kkLiadDocSeqno = wp.itemStr("liad_doc_seqno");
    }
    if (!empty(kkLiadDocNo)) {
      wp.selectSQL = " hex(rowid) as rowid " + " ,UF_IDNO_ID(id_p_seqno) db_id_no "
          + " ,id_p_seqno " + " ,chi_name " + " ,liad_doc_no " + " ,unit_doc_no " + " ,recv_date"
          + " ,key_note" + " ,case_letter_desc" + " ,case_date" + " ,apr_flag" + " ,liad_doc_no"
          + " ,mod_seqno " + " ,liad_doc_seqno ";

      wp.daoTable = " col_liad_renew_court ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr += " and liad_doc_no = :liad_doc_no ";
      setString("liad_doc_no", kkLiadDocNo);
      wp.whereStr += " and liad_doc_seqno = :liad_doc_seqno ";
      setString("liad_doc_seqno", kkLiadDocSeqno);
      pageSelect();
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }

      gotoCourtUpdate("RENEWCOURT");

    } else {
      alertErr("無法取得 [文件編號] 請回前畫面選取異動資料 或 執行 [新增]");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnAdd", "");
      if (!empty(wp.itemStr("db_id_no"))) {
        wp.selectSQL = " id_p_seqno " + " ,chi_name ";
        wp.daoTable = " crd_idno ";
        wp.whereOrder = " ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr += " and id_no = :id_no ";
        setString("id_no", wp.itemStr("db_id_no"));
        pageSelect();
        if (sqlNotFind()) {
          alertErr(appMsg.errCondNodata);
          return;
        }
      }
    }
  }

  void dataReadG() throws Exception {
    kkLiadDocNo = itemKk("data_k1");
    kkLiadDocSeqno = itemKk("data_k2");
    if (empty(kkLiadDocNo)) {
      kkLiadDocNo = wp.itemStr("liad_doc_no");
    }
    if (empty(kkLiadDocSeqno)) {
      kkLiadDocSeqno = wp.itemStr("liad_doc_seqno");
    }
    if (!empty(kkLiadDocNo)) {
      wp.selectSQL = " hex(rowid) as rowid " + " ,UF_IDNO_ID(id_p_seqno) db_id_no "
          + " ,id_p_seqno " + " ,chi_name " + " ,liad_doc_no " + " ,unit_doc_no " + " ,recv_date"
          + " ,key_note" + " ,case_letter_desc" + " ,case_date" + " ,apr_flag" + " ,liad_doc_no"
          + " ,mod_seqno " + " ,liad_doc_seqno ";

      wp.daoTable = " col_liad_liquidate_court ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr += " and liad_doc_no = :liad_doc_no ";
      setString("liad_doc_no", kkLiadDocNo);
      wp.whereStr += " and liad_doc_seqno = :liad_doc_seqno ";
      setString("liad_doc_seqno", kkLiadDocSeqno);
      pageSelect();
      if (sqlNotFind()) {
        alertErr(appMsg.errCondNodata);
        return;
      }

      gotoCourtUpdate("LIQUICOURT");

    } else {
      alertErr("無法取得 [文件編號] 請回前畫面選取異動資料 或 執行 [新增]");
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("btnUpdate", "style='background: lightgray;' disabled");
      wp.colSet("btnAdd", "");
      if (!empty(wp.itemStr("db_id_no"))) {
        wp.selectSQL = " id_p_seqno " + " ,chi_name ";
        wp.daoTable = " crd_idno ";
        wp.whereOrder = " ";
        wp.whereStr = " where 1=1 ";
        wp.whereStr += " and id_no = :id_no ";
        setString("id_no", wp.itemStr("db_id_no"));
        pageSelect();
        if (sqlNotFind()) {
          alertErr(appMsg.errCondNodata);
          return;
        }
      }
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Colm1140Func(wp);

    // rc = func.dbSave(is_action);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      // this.btnMode_aud();
    }
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {
    daoTid = "";
    try {

      wp.optionKey = wp.itemStr("ex_renew_status");
      this.dddwList("ColLiabIdtabList", "col_liab_idtab", "id_code", "id_desc",
          "where 1=1 and id_key = '3' order by id_key, id_code");

      wp.optionKey = wp.itemStr("ex_renew_status2");
      this.dddwList("ColLiabIdtabList2", "col_liab_idtab", "id_code", "id_desc",
          "where 1=1 and id_key = '3' and (id_code='3' or  id_code='8') order by id_key, id_code");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("credit_branch");
      } else {
        wp.optionKey = wp.itemStr("credit_branch");
      }
      // this.dddw_list("PtrBranchNameList", "ptr_branch", "branch", "branch_name", "where 1=1 order
      // by branch");
      this.dddwList("PtrBranchNameList", "gen_brn", "branch", "full_chi_name",
          "where 1=1  order by branch");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("law_user_id");
      } else {
        wp.optionKey = wp.itemStr("law_user_id");
      }
      this.dddwList("dddw_apuser_law_user_id", "sec_user", "usr_id", "usr_cname",
          "where 1=1  order by usr_id");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("demand_user_id");
      } else {
        wp.optionKey = wp.itemStr("demand_user_id");
      }
      this.dddwList("dddw_apuser_demand_user_id", "sec_user", "usr_id", "usr_cname",
          "where 1=1  order by usr_id");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("close_reason");
      } else {
        wp.optionKey = wp.itemStr("close_reason");
      }

      this.dddwList("PtrSysIdtabList", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'LIAD_CLOSE_REASON'  order by wf_type,wf_id");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("court_id");
      } else {
        wp.optionKey = wp.itemStr("court_id");
      }
      this.dddwList("PtrSysIdtabList_court_id", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'COURT_NAME'  order by wf_type,wf_id");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("renew_status");
      } else {
        wp.optionKey = wp.itemStr("renew_status");
      }
      this.dddwList("ColLiabIdtabList_renew_status", "col_liab_idtab", "id_code", "id_desc",
          "where 1=1 and id_key = '3' order by id_key, id_code");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("court_status");
      } else {
        wp.optionKey = wp.itemStr("court_status");
      }
      this.dddwList("PtrSysIdtabList_court_status", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'LIAD_RENEW_STATUS'  order by wf_type,wf_id");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("credit_branch");
      } else {
        wp.optionKey = wp.itemStr("credit_branch");
      }
      // this.dddw_list("PtrBranchNameList_credit_branch", "ptr_branch", "branch", "branch_name",
      // "where 1=1 order by branch");
      this.dddwList("PtrBranchNameList_credit_branch", "gen_brn", "branch", "full_chi_name",
          "where 1=1  order by branch");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("liqu_status");
      } else {
        wp.optionKey = wp.itemStr("liqu_status");
      }
      this.dddwList("ColLiabIdtabList_liqu_status", "col_liab_idtab", "id_code", "id_desc",
          "where 1=1 and id_key = '4' order by id_key, id_code");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("court_status");
      } else {
        wp.optionKey = wp.itemStr("court_status");
      }
      this.dddwList("PtrSysIdtabList_court_status2", "ptr_sys_idtab", "wf_id", "wf_desc",
          "where 1=1 and wf_type = 'LIAD_LIQU_STATUS'  order by wf_type,wf_id");

    } catch (Exception ex) {
    }
  }

  //////////////////// colm1150 function start/////////////////////////////
  void saveFuncZ60() throws Exception {
    func = new Colm1140Func(wp);
    String lsDocNo = "", lsDocNo1 = "", liadDocNo = "", lsAudCode = "";
    lsAudCode = strAction;
    Pattern pattern = Pattern.compile("[0-9]*");
    if (strAction.equals("A") || strAction.equals("U")) {
      if (ofValidationA() != 1) {
        if (strAction.equals("U")) {
          dataReadA();
        }
        return;
      }
    }
    if (!empty(wp.itemStr("liad_doc_no"))) {
      String sql1 = "select * from col_liad_z60 where liad_doc_no = :liad_doc_no ";
      setString("liad_doc_no", wp.itemStr("liad_doc_no"));
      sqlSelect(sql1);
      if (sqlRowNum > 0) {
        if (!wp.itemStr("apr_flag").equals("Y")) {
          lsAudCode = "A";
        }
      }
    }
    func.varsSet("aud_code", lsAudCode);
    if (strAction.equals("A")) {
      // 取得 liad_doc_no
      String sql2 =
          "select max(liad_doc_no) as ls_doc_no,to_char(sysdate,'yyyymm')||'0001' as ls_doc_no1"
              + " from col_liad_z60 " + " where liad_doc_no like :liad_doc_no ";
      setString("liad_doc_no", strMid(wp.sysDate, 0, 6) + "%");
      sqlSelect(sql2);
      lsDocNo = sqlStr("ls_doc_no");
      lsDocNo1 = sqlStr("ls_doc_no1");
      if (sqlCode == -1) {
        alertErr("select col_liad_z60 err");
        return;
      }
      if (!empty(lsDocNo) && pattern.matcher(lsDocNo).matches() == true) {
        liadDocNo = Integer.toString(this.toInt(lsDocNo) + 1);
      } else {
        liadDocNo = lsDocNo1;
      }
      func.varsSet("liad_doc_no", liadDocNo);
      func.insertColLiadZ60();
    } else {
      func.varsSet("liad_doc_no", wp.itemStr("liad_doc_no"));
    }
    if (func.deleteColLiadModTmpZ60() != 1) {
      alertErr("delete COL_LIAD_MOD_TMP error");
      dataReadA();
      return;
    }
    // get mod_data for insert col_liad_mod_tmp
    String modData = wp.itemStr("credit_branch") + "\";\"" + "" + "\";\"" + "" + "\";\""
        + wp.itemStr("branch_comb_flag") + "\";\"" + wp.itemStr("court_id") + "\";\""
        + wp.itemStr("court_name") + "\";\"" + wp.itemStr("case_year") + "\";\""
        + wp.itemStr("case_letter") + "\";\"" + wp.itemStr("case_no") + "\";\""
        + wp.itemStr("bullet_date") + "\";\"" + wp.itemStr("bullet_desc") + "\";\"";
    func.varsSet("data_type", "Z60");
    func.varsSet("mod_data", modData);
    if (func.insertColLiadModTmp() != 1) {
      alertErr("insert COL_LIAD_MOD_TMP error");
      dataReadA();
    } else {
      if (strAction.equals("A")) {
        clearFunc();
        alertMsg("新增完成");
      } else {

        func.updateColLiadZ60();
        dataReadA();
      }
    }
  }

  void gotoZ60Update() {
    String strModData = "", dbAudCode = "";
    String sql =
        "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'Z60' and data_key = :kk_liad_doc_no ";
    setString("kk_liad_doc_no", kkLiadDocNo);
    sqlSelect(sql);
    strModData = sqlStr("mod_data");
    dbAudCode = sqlStr("aud_code");
    if (dbAudCode.equals("A")) {
      wp.colSet("btnDelete", "style='background: lightgray;' disabled");
    }
    wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
    String[] modData = strModData.split("\";\"", -1);
    if (sqlRowNum > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
      wp.colSet("btnUpdate2", "");

      if (!modData[0].equals(wp.colStr("credit_branch"))) {
        wp.colSet("credit_branch_pink", "pink");
        wp.colSet("credit_branch", modData[0]);
      }
      if (!modData[3].equals(wp.colStr("branch_comb_flag"))) {
        wp.colSet("branch_comb_flag_pink", "pink");
        wp.colSet("branch_comb_flag", modData[3]);
      }
      if (!modData[4].equals(wp.colStr("court_id"))) {
        wp.colSet("court_id_pink", "pink");
        wp.colSet("court_id", modData[4]);
      }
      if (!modData[5].equals(wp.colStr("court_name"))) {
        wp.colSet("court_name_pink", "pink");
        wp.colSet("court_name", modData[5]);
      }
      if (!modData[6].equals(wp.colStr("case_year"))) {
        wp.colSet("case_year_pink", "pink");
        wp.colSet("case_year", modData[6]);
      }
      if (!modData[7].equals(wp.colStr("case_letter"))) {
        wp.colSet("case_letter_pink", "pink");
        wp.colSet("case_letter", modData[7]);
      }
      if (!modData[8].equals(wp.colStr("case_no"))) {
        wp.colSet("case_no_pink", "pink");
        wp.colSet("case_no", modData[8]);
      }
      if (!modData[9].equals(wp.colStr("bullet_date"))) {
        wp.colSet("bullet_date_pink", "pink");
        wp.colSet("bullet_date", modData[9]);
      }
      if (!modData[10].equals(wp.colStr("bullet_desc"))) {
        wp.colSet("bullet_desc_pink", "pink");
        wp.colSet("bullet_desc", modData[10]);
      }
    } else {
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
    }
  }

  int ofValidationA() {
    if (this.toNum(wp.itemStr("case_year")) < 0) {
      alertErr("案號年度 不可小於 0");
      return -1;
    }
    if (wp.itemStr("tt_id_no").equals(wp.itemStr("id_no"))
        && wp.itemStr("tt_branch_comb_flag").equals(wp.itemStr("branch_comb_flag"))) {
      if (wp.itemStr("tt_credit_branch").equals(wp.itemStr("credit_branch"))
          && wp.itemStr("tt_court_id").equals(wp.itemStr("court_id"))) {
        if (wp.itemStr("tt_court_name").equals(wp.itemStr("court_name"))
            && wp.itemStr("tt_case_year").equals(wp.itemStr("case_year"))) {
          if (wp.itemStr("tt_case_letter").equals(wp.itemStr("case_letter"))
              && wp.itemStr("tt_case_no").equals(wp.itemStr("case_no"))) {
            if (wp.itemStr("tt_bullet_date").equals(wp.itemStr("bullet_date"))
                && wp.itemStr("tt_bullet_desc").equals(wp.itemStr("bullet_desc"))) {
              alertErr("資料未異動，不可執行新增或修改作業!");
              return -1;
            }
          }
        }
      }
    }

    // 以【身分證號】為條件，檢查是否存在於 crd_idno table 中。
    String lsIdNo = wp.itemStr("id_no");
    String sql1 =
        "select id_p_seqno,chi_name from crd_idno where id_no = :id_no and id_no_code='0'";
    setString("id_no", lsIdNo);
    sqlSelect(sql1);
    String isIdPSeqno = sqlStr("id_p_seqno");
    String isChiName = sqlStr("chi_name");
    if (sqlRowNum <= 0) {
      alertErr("身分證ID不存在");
      return -1;
    }
    if (strAction.equals("A")) {
      func.varsSet("id_p_seqno", isIdPSeqno);
      func.varsSet("chi_name", isChiName);
    } else {
      func.varsSet("id_p_seqno", wp.itemStr("id_p_seqno"));
      func.varsSet("chi_name", wp.itemStr("chi_name"));
    }
    // 取得卡數
    String sql2 = "select count(*) as ii_card_num from crd_card where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", isIdPSeqno);
    sqlSelect(sql2);
    String iiCardNum = sqlStr("ii_card_num");
    if (sqlRowNum <= 0) {
      return -1;
    }
    func.varsSet("card_num", iiCardNum);
    if (strAction.equals("A")) {
      // 取得帳戶資料
      String sql3 = " select count(*) as ii_acct_num" + " ,max(acct_status) as is_acct_status"
          + " ,max(payment_rate1) as li_mcode"
          + " from act_acno where id_p_seqno = :id_p_seqno and payment_rate1 <>'' ";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql3);
      String isAcctStatus = sqlStr("is_acct_status");
      String isMCode = sqlStr("li_mcode");
      switch (isMCode) {
        case "0A":
          isMCode = "0A";
          break;
        case "0B":
          isMCode = "0B";
          break;
        case "0C":
          isMCode = "0C";
          break;
        case "0D":
          isMCode = "0D";
          break;
        case "0E":
          isMCode = "0E";
          break;
        default:
          isMCode = "00";
      }
      if (sqlRowNum <= 0) {
        return -1;
      }
      func.varsSet("acct_status", isAcctStatus);
      func.varsSet("m_code", isMCode);
      // -read act_debt-
      String sql7 = " select sum(decode(a.acct_code,'DB',a.end_bal)) idc_amt_db, "
          + " sum(decode(a.acct_code,'CB',a.end_bal)) idc_amt_cb, "
          + " sum(decode(a.acct_code,'RI',a.end_bal)) idc_amt_ri, "
          + " sum(decode(a.acct_code,'BL',a.end_bal, "
          + " 'IT',a.end_bal,'CA',a.end_bal,'ID',a.end_bal,'AO',a.end_bal,'OT',a.end_bal,0)) idc_amt_debt "
          + " from act_debt a join act_acno b on a.acno_p_seqno = b.acno_p_seqno "
          + " where b.id_p_seqno like :id_p_seqno ";
      setString("id_p_seqno", isIdPSeqno + "%");
      sqlSelect(sql7);
      String badDebtAmt = sqlStr("idc_amt_db");
      String demandAmt = sqlStr("idc_amt_cb");
      String idcAmtRi = sqlStr("idc_amt_ri");
      String debtAmt = sqlStr("idc_amt_debt");
      if (sqlCode == -1) {
        badDebtAmt = "0";
        demandAmt = "0";
        idcAmtRi = "0";
        debtAmt = "0";
        alertErr("select ACT_DEBT error");
        return -1;
      }
      if (empty(badDebtAmt)) {
        badDebtAmt = "0";
      }
      if (empty(demandAmt)) {
        demandAmt = "0";
      }
      if (empty(idcAmtRi)) {
        idcAmtRi = "0";
      }
      if (empty(debtAmt)) {
        debtAmt = "0";
      }
      func.varsSet("bad_debt_amt", badDebtAmt);
      func.varsSet("demand_amt", demandAmt);
      func.varsSet("debt_amt", debtAmt);
      // -OK-
      func.varsSet("recv_date", wp.sysDate);
      func.varsSet("data_date", wp.sysDate);
      func.varsSet("crt_user", wp.loginUser);
      func.varsSet("crt_date", wp.sysDate);
      func.varsSet("apr_flag", "N");
      func.varsSet("apr_user", wp.loginUser);
      func.varsSet("apr_date", wp.sysDate);
      func.varsSet("liqui_flag", "N");
      func.varsSet("renew_flag", "N");
      int i = wp.itemStr("case_letter").length();
      String caseLetter = "";
      if (i != 0) {
        caseLetter = strMid(wp.itemStr("case_letter"), i - 1, i);
      }
      switch (caseLetter) {
        case "更":
          func.varsSet("renew_flag", "Y");
          break;
        case "清":
          func.varsSet("liqui_flag", "Y");
      }
    }

    return 1;
  }
  //////////////////// colm1150 function end/////////////////////////////

  //////////////////// colm1160 function start/////////////////////////////
  void saveFuncB() throws Exception {
    func = new Colm1140Func(wp);
    Pattern pattern = Pattern.compile("[0-9]*");
    String lsAudCode = "", lsDocNo = "", lsDocNo1 = "";
    if (ofValidationB() != 1) {
      if (strAction.equals("U"))
        dataReadB();
      return;
    }
    // 設定ls_aud_code
    lsAudCode = strAction;
    String sql1 = "select * from col_liad_renew where liad_doc_no = :liad_doc_no";
    setString("liad_doc_no", wp.itemStr("liad_doc_no"));
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      if (!wp.itemStr("apr_flag").equals("Y")) {
        lsAudCode = "A";
      }
    }
    func.varsSet("aud_code", lsAudCode);
    if (strAction.equals("A")) {
      // 取得 liad_doc_no
      String sql2 =
          "select max(liad_doc_no) as ls_doc_no,to_char(sysdate,'yyyymm')||'0001' as ls_doc_no1"
              + " from col_liad_renew " + " where liad_doc_no like :liad_doc_no";
      setString("liad_doc_no", strMid(wp.sysDate, 0, 6) + "%");
      sqlSelect(sql2);
      lsDocNo = sqlStr("ls_doc_no");
      lsDocNo1 = sqlStr("ls_doc_no1");
      if (sqlCode == -1) {
        alertErr("select col_liad_renew err");
        return;
      }
      if (!empty(lsDocNo) && pattern.matcher(lsDocNo).matches() == true) {
        liadDocNo = ((int) this.toNum(lsDocNo) + 1) + "";
      } else {
        liadDocNo = lsDocNo1;
      }
      func.varsSet("liad_doc_no", liadDocNo);
    } else {
      func.varsSet("liad_doc_no", wp.itemStr("liad_doc_no"));
    }
    // 若更生進度為3(更生認可)，取得renew_accetp_no (更生認可歸檔編號)
    String lsRenewAccetpNo = wp.itemStr("renew_accetp_no");
    String renewStatus = wp.itemStr("renew_status");
    if (renewStatus.equals("3") && empty(lsRenewAccetpNo)) {
      String sql2 =
          "select max(renew_accetp_no) as ls_renew_accetp_no ,to_char(sysdate,'yyyymm')||'0001' as ls_doc_no1"
              + " from col_liad_renew " + " where liad_doc_no like :liad_doc_no";
      setString("liad_doc_no", strMid(wp.sysDate, 0, 6) + "%");
      sqlSelect(sql2);
      lsRenewAccetpNo = sqlStr("ls_renew_accetp_no");
      lsDocNo1 = sqlStr("ls_doc_no1");

      if (sqlCode == -1) {
        alertErr("select col_liad_renew err");
        return;
      }
      if (!empty(lsRenewAccetpNo) && pattern.matcher(lsRenewAccetpNo).matches() == true) {
        lsRenewAccetpNo = ((int) this.toNum(lsRenewAccetpNo) + 1) + "";
      } else {
        lsRenewAccetpNo = lsDocNo1;
      }

      func.varsSet("renew_accetp_no", lsRenewAccetpNo);
    }
    // else{
    // //若若更生進度不為3(不為更生認可)，清除renew_accetp_no (更生認可歸檔編號)內容。
    // func.vars_set("renew_accetp_no", "");
    // }
    if (strAction.equals("A")) {
      // insert col_liad_renew
      if (func.insertColLiadRenew() != 1) {
        sqlCommit(0);
        alertErr("insert col_liad_renew err");
        return;
      }
    }
    String oRenewStatus = wp.itemStr("o_renew_status");
    String oCourtStatus = wp.itemStr("o_court_status");
    String courtStatus = wp.itemStr("court_status");

    if (!renewStatus.equals(oRenewStatus) || !courtStatus.equals(oCourtStatus)
        || strAction.equals("A")) {

      // 取得liad_doc_seqno(序號)
      int liadDocSeqno = fDocSeqno("col_liad_renew_court");
      func.varsSet("liad_doc_seqno", Integer.toString(liadDocSeqno));
      func.varsSet("unit_doc_no", "");
      func.varsSet("recv_date", wp.itemStr("recv_date"));
      func.varsSet("key_note", "");
      func.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
      func.varsSet("case_date", wp.itemStr("case_date_m"));
      func.insertColLiadRenewCourt();
    }

    // get mod_data for insert col_liad_mod_tmp
    String modData = wp.itemStr("recv_date") + "\";\"" + wp.itemStr("branch_comb_flag") + "\";\""
        + wp.itemStr("max_bank_flag") + "\";\"" + wp.itemStr("court_id") + "\";\""
        + wp.itemStr("renew_status") + "\";\"" + wp.itemStr("court_status") + "\";\""
        + this.toInt(wp.itemStr("case_year")) + "\";\"" + wp.itemStr("case_letter_desc") + "\";\""
        + wp.itemStr("judic_date") + "\";\"" + wp.itemStr("confirm_date") + "\";\""
        + wp.itemNum("payoff_amt") + "\";\"" + wp.itemStr("run_renew_flag") + "\";\""
        + wp.itemStr("doc_chi_name") + "\";\"" + wp.itemStr("credit_branch") + "\";\""
        + idcAmtOrgDebt + "\";\"" + wp.itemStr("case_letter") + "\";\""
        + wp.itemStr("renew_cancel_date") + "\";\"" + wp.itemStr("renew_first_date") + "\";\""
        + this.toInt(wp.itemStr("renew_int")) + "\";\"" + wp.itemStr("deliver_date") + "\";\""
        + wp.itemStr("renew_damage_date") + "\";\"" + wp.itemStr("court_dept") + "\";\""
        + wp.itemStr("judic_action_flag") + "\";\"" + wp.itemStr("action_date_s") + "\";\""
        + wp.itemStr("judic_cancel_flag") + "\";\"" + wp.itemStr("cancel_date") + "\";\""
        + wp.itemStr("renew_last_date") + "\";\"" + wp.itemStr("payment_day") + "\";\""
        + wp.itemStr("renew_rate") + "\";\"" + (long) renewLoseAmt + "\";\""
        + wp.itemNum("org_debt_amt_bef_base") + "\";\"" + wp.itemStr("super_name") + "\";\""
        + lsRenewAccetpNo + "\";\"";
    func.varsSet("data_type", "RENEW");
    func.varsSet("mod_data", modData);

    // 刪除前一次異動的暫存資料
    if (func.deleteColLiadModTmp() != 1) {
      sqlCommit(0);
      alertErr("delete COL_LIAD_MOD_TMP error");
      dataReadB();
      return;
    }

    // 新增此次異動產生的暫存資料
    if (func.insertColLiadModTmp() != 1) {
      sqlCommit(0);
      alertErr("insert COL_LIAD_MOD_TMP error");
      if (!strAction.equals("A"))
        dataReadB();
      return;
    } else {
      if (strAction.equals("A")) {
        sqlCommit(1);
        clearFunc();
        errmsg("新增完成");
      } else {
        if (func.updateColLiadRenew() != 1) {
          sqlCommit(0);
          alertErr("update col_liad_renew err");
          dataReadB();
          return;
        }
        sqlCommit(1);
        dataReadB();
      }
    }

  }


  int ofValidationB() throws Exception {
    // 若is_action='D'(刪除模式)，檢查【credit_print_flag】、【jcic_send_flag】是否為Y，若為【Y】，需顯示警告訊息並 return -1。
    if (strAction.equals("D")) {
      if (wp.itemStr("credit_print_flag").equals("Y") || wp.itemStr("jcic_send_flag").equals("Y")) {
        alertErr("【資料已列印[債權陳報狀] or 報送聯徵 不可刪除】");
        return -1;
      }

      // 如果存在尚未完成報送的 col_liad_log資料，不可刪除
      String sqlSelect =
          " select count(*) as unproccnt from col_liad_log " + " where doc_no = :liad_doc_no "
              + " and liad_type = '1' " + " and event_type = 'S' " + " and proc_flag <> 'Y' ";
      setString("liad_doc_no", wp.itemStr("liad_doc_no"));
      sqlSelect(sqlSelect);

      if (sqlNum("unproccnt") > 0) {
        alertErr("【尚有待報送資料，不可刪除】");
        return -1;
      }
    }
    // 若有更生認可暫存資料，不可刪除
    String lsCaseLetter = wp.itemStr("case_letter");
    String isLiadDocNo = wp.itemStr("liad_doc_no");
    String lsId = wp.itemStr("id_no");
    if (!empty(isLiadDocNo) && strAction.equals("D")) {
      String sql1 =
          "select count (*) as ll_cnt from col_liad_mod_tmp where data_type = 'INST-MAST' and data_key like :is_liad_doc_no";
      setString("is_liad_doc_no", isLiadDocNo + "%");
      sqlSelect(sql1);
      if (this.toInt(sqlStr("ll_cnt")) > 0) {
        alertErr("【已有更生認可資料, 不可刪除】");
        return -1;
      }
    }

    // 檢查有無更生認可資料。
    if (!empty(isLiadDocNo) && strAction.equals("D")) {
      String sql2 = "SELECT count (*) AS ll_cnt_renew FROM col_liad_renew , crd_idno "
          + "WHERE col_liad_renew.id_p_seqno = crd_idno.id_p_seqno "
          + "AND col_liad_renew.liad_doc_no <> :is_liad_doc_no " + "AND crd_idno.id_no = :ls_id "
          + "AND col_liad_renew.case_letter = :ls_case_letter ";
      setString("is_liad_doc_no", isLiadDocNo);
      setString("ls_id", lsId);
      setString("ls_case_letter", lsCaseLetter);
      sqlSelect(sql2);
      int llCntRenew = this.toInt(sqlStr("ll_cnt_renew"));

      String sql3 = "SELECT count (*) AS ll_cnt FROM col_liad_install, crd_idno "
          + "WHERE col_liad_install.holder_id_p_seqno = crd_idno.id_p_seqno "
          + "AND col_liad_install.holder_id = :ls_id "
          + "AND col_liad_install.case_letter = :ls_case_letter ";
      setString("ls_id", lsId);
      setString("ls_case_letter", lsCaseLetter);
      sqlSelect(sql3);
      int llCnt = this.toInt(sqlStr("ll_cnt"));
      if (llCntRenew == 0 && llCnt > 0) {
        alertErr("【已有更生認可資料, 不可刪除】");
        return -1;
      }
    }

    // 計算dw_data[].renew_lose_amt 的金額
    renewLoseAmt = wp.itemNum("org_debt_amt_bef") - wp.itemNum("payoff_amt");
    func.varsSet("renew_lose_amt", renewLoseAmt + "");
    idcAmtOrgDebt = (int) wp.itemNum("org_debt_amt_bef");
    // 若is_action='D'，結束此段程式
    if (strAction.equals("D")) {
      dataReadB();
      return 1;
    }

    // 必填欄位檢核
    if (empty(wp.itemStr("recv_date"))) {
      alertErr("【收件日期不能為空】");
      return -1;
    }
    if (empty(wp.itemStr("case_letter_desc"))) {
      alertErr("【法院函號不能為空】");
      return -1;
    }
    if (empty(wp.itemStr("case_letter"))) {
      alertErr("【發文字號(案號字別&案號號碼)不能為空】");
      return -1;
    }

    // 檢查【dw_data[].case_year】
    if (this.toInt(wp.itemStr("case_year")) < 0) {
      alertErr("【案號年~度 不可小於 0】");
      return -1;
    }
    // 檢查【dw_data[].payment_day】，須為01-31之數字
    String paymentDay = wp.itemStr("payment_day");
    if (!empty(paymentDay)) {
      if (wp.isNumber(paymentDay) == false) {
        alertErr("【每月繳款日 須為 01 ~ 31】");
        return -1;
      }
      if (this.toInt(paymentDay) <= 0 || this.toInt(paymentDay) > 31) {
        alertErr("【每月繳款日 須為 01 ~ 31】");
        return -1;
      }
    }
    // 檢查身分證號，取得id_p_seqno、chi_name
    String lsIdNo = wp.itemStr("id_no");
    String sql1 =
        "select id_p_seqno,chi_name from crd_idno where id_no = :ls_id_no and id_no_code='0'";
    setString("ls_id_no", lsIdNo);
    sqlSelect(sql1);
    String isIdPSeqno = sqlStr("id_p_seqno");
    String isChiName = sqlStr("chi_name");
    if (sqlRowNum <= 0) {
      alertErr("身分證ID不存在");
      return -1;
    }
    func.varsSet("id_p_seqno", isIdPSeqno);
    func.varsSet("chi_name", isChiName);

    // 若為is_action='A'(新增模式)，取得卡數
    if (strAction.equals("A")) {
      String sql4 =
          "select count(*) as ii_card_num from crd_card where id_p_seqno = :is_id_p_seqno ";
      setString("is_id_p_seqno", isIdPSeqno);
      sqlSelect(sql4);
      String iiCardNum = sqlStr("ii_card_num");
      if (sqlRowNum <= 0) {
        alertErr("select ii_card_num error");
        return -1;
      }
      func.varsSet("card_num", iiCardNum);
    }
    // 若為is_action='A'(新增模式)，取得帳戶資料，及相關金額值
    if (strAction.equals("A")) {
      // 取得帳戶資料
      String sql5 = " select count(*) as ii_acct_num" + " ,max(acct_status) as is_acct_status"
          + " ,max(payment_rate1) as li_mcode"
          + " from act_acno where id_p_seqno = :is_id_p_seqno and payment_rate1 <>'' ";
      setString("is_id_p_seqno", isIdPSeqno);
      sqlSelect(sql5);
      String iiAcctNum = sqlStr("ii_acct_num");
      String isAcctStatus = sqlStr("is_acct_status");
      String isMCode = sqlStr("li_mcode");
      switch (isMCode) {
        case "0A":
          isMCode = "0A";
          break;
        case "0B":
          isMCode = "0B";
          break;
        case "0C":
          isMCode = "0C";
          break;
        case "0D":
          isMCode = "0D";
          break;
        case "0E":
          isMCode = "0E";
          break;
        default:
          isMCode = "00";
      }
      if (sqlCode < 0) {
        alertErr("select act_acno error");
        return -1;
      }
      func.varsSet("acct_num", iiAcctNum);
      func.varsSet("acct_status", isAcctStatus);
      func.varsSet("m_code", isMCode);
      // -read act_debt-
      String sql6 = " select sum(decode(a.acct_code,'DB',a.end_bal)) idc_amt_db, "
          + " sum(decode(a.acct_code,'CB',a.end_bal)) idc_amt_cb, "
          + " sum(decode(a.acct_code,'RI',a.end_bal)) idc_amt_ri, "
          + " sum(decode(a.acct_code,'BL',a.end_bal, "
          + " 'IT',a.end_bal,'CA',a.end_bal,'ID',a.end_bal,'AO',a.end_bal,'OT',a.end_bal,0)) idc_amt_debt "
          + " from act_debt a join act_acno b on a.acno_p_seqno = b.acno_p_seqno "
          + " where b.id_p_seqno like :is_id_p_seqno ";
      setString("is_id_p_seqno", isIdPSeqno + "%");
      sqlSelect(sql6);
      String badDebtAmt = sqlStr("idc_amt_db");
      String demandAmt = sqlStr("idc_amt_cb");
      String idcAmtRi = sqlStr("idc_amt_ri");
      String debtAmt = sqlStr("idc_amt_debt");
      if (sqlCode < 0) {
        badDebtAmt = "0";
        demandAmt = "0";
        idcAmtRi = "0";
        debtAmt = "0";
        alertErr("select ACT_DEBT error");
        return -1;
      }
      if (empty(badDebtAmt)) {
        badDebtAmt = "0";
      }
      if (empty(demandAmt)) {
        demandAmt = "0";
      }
      if (empty(idcAmtRi)) {
        idcAmtRi = "0";
      }
      if (empty(debtAmt)) {
        debtAmt = "0";
      }
      func.varsSet("bad_debt_amt", badDebtAmt);
      func.varsSet("demand_amt", demandAmt);
      func.varsSet("debt_amt", debtAmt);
    }

    if (strAction.equals("A")) {
      // sql long time
      String sql7 = " select sum(end_bal) as lm_debt from act_debt "
          + "where p_seqno in (select acno_p_seqno from act_acno where id_p_seqno =:id_p_seqno)";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql7);
      String lmDebt = sqlStr("lm_debt");
      if (sqlCode < 0) {
        alertErr("select ACT_DEBT error");
        return -1;
      }
      if (empty(lmDebt)) {
        lmDebt = "0";
      }
      // -歡喜卡之預借現金金額-
      String sql8 = "SELECT sum(a.cash_use_balance) as lm_cash_use "
          + "FROM act_combo_m_jrnl a, act_acno b " + "WHERE a.acct_type = b.acct_type "
          + "AND a.id_p_seqno  = b.id_p_seqno " + "AND b.id_p_seqno =:id_p_seqno";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql8);
      String lmCashUse = sqlStr("lm_cash_use");
      if (sqlCode < 0) {
        alertErr("select act_combo_m_jrnl error");
        return -1;
      }
      if (empty(lmCashUse)) {
        lmCashUse = "0";
      }
      // -分期未billing-
      String sql9 = "SELECT sum((a.unit_price)*(a.install_tot_term - "
          + "a.install_curr_term) + a.remd_amt + "
          + "decode(a.install_curr_term,0,a.first_remd_amt,0)) as lm_unbill_amt, "
          + "sum(a.clt_unit_price * ( a.clt_install_tot_term - "
          + "a.install_curr_term ) + a.clt_remd_amt) as lm_unbill_fee "
          + "FROM bil_contract a, act_acno b " + "WHERE a.acct_type = b.acct_type "
          + "AND a.id_p_seqno = b.id_p_seqno " + "AND b.id_p_seqno =:id_p_seqno "
          + "AND a.install_tot_term != a.install_curr_term " + "AND a.refund_apr_flag != 'Y' "
          + "AND ( ( (decode(a.auth_code,'','N',a.auth_code) not in ('N','REJECT','P','reject') "
          + "and a.contract_kind = '2' ) " + "or a.contract_kind = '1') "
          + "and (a.apr_date <> '' or a.delv_confirm_date <> '')) ";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql9);
      String lmUnbillAmt = sqlStr("lm_unbill_amt");
      String lmUnbillFee = sqlStr("lm_unbill_fee");
      if (sqlCode < 0) {
        alertErr("select BIL_CONTRACT error");
        return -1;
      }
      if (empty(lmUnbillAmt)) {
        lmUnbillAmt = "0";
      }
      if (empty(lmUnbillFee)) {
        lmUnbillFee = "0";
      }
      idcAmtOrgDebt = this.toInt(lmDebt) + this.toInt(lmCashUse) + this.toInt(lmUnbillAmt)
          + this.toInt(lmUnbillFee);
      func.varsSet("org_debt_amt", idcAmtOrgDebt + "");
      func.varsSet("org_debt_amt_bef", idcAmtOrgDebt + "");
    }
    // 資料檢查
    if (wp.itemStr("renew_status").equals("2")) {
      if (empty(wp.itemStr("renew_cancel_date"))) {
        alertErr("【更生進度為撤回，更生撤回日期 不可空白】");
        return -1;
      }
    }
    if (!empty(wp.itemStr("renew_cancel_date"))) {
      if (!wp.itemStr("renew_status").equals("2")) {
        alertErr("【有更生撤回日期，更生進度須為撤回】");
        return -1;
      }
    }
    if (wp.itemStr("deliver_date").equals("4")) {
      if (empty(wp.itemStr("renew_cancel_date"))) {
        alertErr("【更生進度為履行完畢，履行完畢日期 不可空白】");
        return -1;
      }
    }
    if (!empty(wp.itemStr("deliver_date"))) {
      if (!wp.itemStr("renew_status").equals("4")) {
        alertErr("【有履行完畢日期，更生進度須為履行完畢】");
        return -1;
      }
    }
    if (wp.itemStr("run_renew_flag").equals("N")) {
      if (empty(wp.itemStr("renew_damage_date"))) {
        alertErr("【未依更生條件履行，毀諾日期 不可空白】");
        return -1;
      }
    }
    if (!empty(wp.itemStr("renew_damage_date"))) {
      if (!wp.itemStr("run_renew_flag").equals("N")) {
        alertErr("【有毀諾日期，須為未依更生條件履行】");
        return -1;
      }
    }

    // 取得法院中文名稱
    String lsCourtId = wp.itemStr("court_id");
    String sql10 =
        " select wf_desc as ss from ptr_sys_idtab where wf_type = 'COURT_NAME' and wf_id = :ls_court_id ";
    setString("ls_court_id", lsCourtId);
    sqlSelect(sql10);
    paymentDay = sqlStr("ss");
    if (sqlRowNum > 0) {
      func.varsSet("court_name", paymentDay);
    } else {
      func.varsSet("court_name", lsCourtId);
    }
    // 設定資料內容
    if (strAction.equals("A")) {
      func.varsSet("crt_user", wp.loginUser);
      func.varsSet("crt_date", wp.sysDate);
      func.varsSet("apr_flag", "N");
      func.varsSet("apr_user", wp.loginUser);
      func.varsSet("apr_date", wp.sysDate);
    }

    return 1;
  }

  void gotoRenewUpdate() {
    String strModData = "", dbAudCode = "";
    String sql =
        "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'RENEW' and data_key = :data_key ";
    setString("data_key", kkLiadDocNo);
    sqlSelect(sql);
    strModData = sqlStr("mod_data");
    dbAudCode = sqlStr("aud_code");
    if (dbAudCode.equals("A")) {
      wp.colSet("btnDelete", "style='background: lightgray;' disabled");
    }

    wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
    String[] modData = strModData.split("\";\"", -1);
    if (sqlRowNum > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
      wp.colSet("btnUpdate2", "");
      if (!modData[0].equals(wp.colStr("recv_date"))) {
        wp.colSet("recv_date_pink", "pink");
        wp.colSet("recv_date", modData[0]);
      }
      if (!modData[1].equals(wp.colStr("branch_comb_flag"))) {
        wp.colSet("branch_comb_flag_pink", "pink");
        wp.colSet("branch_comb_flag", modData[1]);
      }
      if (!modData[2].equals(wp.colStr("max_bank_flag"))) {
        wp.colSet("max_bank_flag_pink", "pink");
        wp.colSet("max_bank_flag", modData[2]);
      }
      if (!modData[3].equals(wp.colStr("court_id"))) {
        wp.colSet("court_id_pink", "pink");
        wp.colSet("court_id", modData[3]);
      }
      if (!modData[4].equals(wp.colStr("renew_status"))) {
        wp.colSet("renew_status_pink", "pink");
        wp.colSet("renew_status", modData[4]);
        wp.colSet("o_renew_status", modData[4]);
      }
      if (!modData[5].equals(wp.colStr("court_status"))) {
        wp.colSet("court_status_pink", "pink");
        wp.colSet("court_status", modData[5]);
        wp.colSet("o_court_status", modData[5]);
      }
      if (!modData[6].equals(wp.colStr("case_year"))) {
        wp.colSet("case_year_pink", "pink");
        wp.colSet("case_year", modData[6]);
      }
      if (!modData[7].equals(wp.colStr("case_letter_desc"))) {
        wp.colSet("case_letter_desc_pink", "pink");
        wp.colSet("case_letter_desc", modData[7]);
      }
      if (!modData[8].equals(wp.colStr("judic_date"))) {
        wp.colSet("judic_date_pink", "pink");
        wp.colSet("judic_date", modData[8]);
      }
      if (!modData[9].equals(wp.colStr("confirm_date"))) {
        wp.colSet("confirm_date_pink", "pink");
        wp.colSet("confirm_date", modData[9]);
      }
      if (this.toNum(modData[10]) != wp.colNum("payoff_amt")) {
        wp.colSet("payoff_amt_pink", "pink");
        wp.colSet("payoff_amt", modData[10]);
      }
      if (!modData[11].equals(wp.colStr("run_renew_flag"))) {
        wp.colSet("run_renew_flag_pink", "pink");
        wp.colSet("run_renew_flag", modData[11]);
      }
      if (!modData[12].equals(wp.colStr("doc_chi_name"))) {
        wp.colSet("doc_chi_name_pink", "pink");
        wp.colSet("doc_chi_name", modData[12]);
      }
      if (!modData[13].equals(wp.colStr("credit_branch"))) {
        wp.colSet("credit_branch_pink", "pink");
        wp.colSet("credit_branch", modData[13]);
      }
      if (this.toNum(modData[14]) != wp.colNum("org_debt_amt_bef")) {
        wp.colSet("org_debt_amt_bef_pink", "pink");
        wp.colSet("org_debt_amt_bef", modData[14]);
      }
      if (!modData[15].equals(wp.colStr("case_letter"))) {
        wp.colSet("case_letter_pink", "pink");
        wp.colSet("case_letter", modData[15]);
      }
      if (!modData[16].equals(wp.colStr("renew_cancel_date"))) {
        wp.colSet("renew_cancel_date_pink", "pink");
        wp.colSet("renew_cancel_date", modData[16]);
      }
      if (!modData[17].equals(wp.colStr("renew_first_date"))) {
        wp.colSet("renew_first_date_pink", "pink");
        wp.colSet("renew_first_date", modData[17]);
      }
      if (toNum(modData[18]) != toNum(wp.colStr("renew_int"))) {
        wp.colSet("renew_int_pink", "pink");
        wp.colSet("renew_int", modData[18]);
      }
      if (!modData[19].equals(wp.colStr("deliver_date"))) {
        wp.colSet("deliver_date_pink", "pink");
        wp.colSet("deliver_date", modData[19]);
      }
      if (!modData[20].equals(wp.colStr("renew_damage_date"))) {
        wp.colSet("renew_damage_date_pink", "pink");
        wp.colSet("renew_damage_date", modData[20]);
      }
      if (!modData[21].equals(wp.colStr("court_dept"))) {
        wp.colSet("court_dept_pink", "pink");
        wp.colSet("court_dept", modData[21]);
      }
      if (!modData[22].equals(wp.colStr("judic_action_flag"))) {
        wp.colSet("judic_action_flag_pink", "pink");
        wp.colSet("judic_action_flag", modData[22]);
      }
      if (!modData[23].equals(wp.colStr("action_date_s"))) {
        wp.colSet("action_date_s_pink", "pink");
        wp.colSet("action_date_s", modData[23]);
      }
      if (!modData[24].equals(wp.colStr("judic_cancel_flag"))) {
        wp.colSet("judic_cancel_flag_pink", "pink");
        wp.colSet("judic_cancel_flag", modData[24]);
      }
      if (!modData[25].equals(wp.colStr("cancel_date"))) {
        wp.colSet("cancel_date_pink", "pink");
        wp.colSet("cancel_date", modData[25]);
      }
      if (!modData[26].equals(wp.colStr("renew_last_date"))) {
        wp.colSet("renew_last_date_pink", "pink");
        wp.colSet("renew_last_date", modData[26]);
      }
      if (!modData[27].equals(wp.colStr("payment_day"))) {
        wp.colSet("payment_day_pink", "pink");
        wp.colSet("payment_day", modData[27]);
      }
      if (toNum(modData[28]) != toNum(wp.colStr("renew_rate"))) {
        wp.colSet("renew_rate_pink", "pink");
        wp.colSet("renew_rate", modData[28]);
      }
      if (toNum(modData[29]) != wp.colNum("renew_lose_amt")) {
        wp.colSet("renew_lose_amt_pink", "pink");
        wp.colSet("renew_lose_amt", modData[29]);
      }
      if (toNum(modData[30]) != wp.colNum("org_debt_amt_bef_base")) {
        wp.colSet("org_debt_amt_bef_base_pink", "pink");
        wp.colSet("org_debt_amt_bef_base", modData[30]);
      }
      if (!modData[31].equals(wp.colStr("super_name"))) {
        wp.colSet("super_name_pink", "pink");
        wp.colSet("super_name", modData[31]);
      }
      if (!modData[32].equals(wp.colStr("renew_accetp_no"))) {
        wp.colSet("renew_accetp_no_pink", "pink");
        wp.colSet("renew_accetp_no", modData[32]);
      }
    } else {
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
    }
  }
  //////////////////// colm1160 function end/////////////////////////////

  //////////////////// colm1170 function start/////////////////////////////
  void saveFuncC() throws Exception {
    func = new Colm1140Func(wp);
    Pattern pattern = Pattern.compile("[0-9]*");
    String lsAudCode = "", lsDocNo = "", lsDocNo1 = "";
    if (ofValidationC() != 1) {
      if (strAction.equals("U"))
        dataReadC();
      return;
    }

    // 設定ls_aud_code
    lsAudCode = strAction;
    String sql1 = "select * from col_liad_liquidate where liad_doc_no = :liad_doc_no";
    setString("liad_doc_no", wp.itemStr("liad_doc_no"));
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      if (!wp.itemStr("apr_flag").equals("Y")) {
        lsAudCode = "A";
      }
    }
    func.varsSet("aud_code", lsAudCode);
    if (strAction.equals("A")) {
      // 取得 liad_doc_no
      String sql2 =
          "select max(liad_doc_no) as ls_doc_no,to_char(sysdate,'yyyymm')||'0001' as ls_doc_no1"
              + " from col_liad_liquidate " + " where liad_doc_no like :liad_doc_no";
      setString("liad_doc_no", strMid(wp.sysDate, 0, 6) + "%");
      sqlSelect(sql2);
      lsDocNo = sqlStr("ls_doc_no");
      lsDocNo1 = sqlStr("ls_doc_no1");
      if (sqlRowNum <= 0) {
        alertErr("select col_liad_liquidate err");
        return;
      }
      if (!empty(lsDocNo) && pattern.matcher(lsDocNo).matches() == true) {
        liadDocNo = ((int) this.toNum(lsDocNo) + 1) + "";
      } else {
        liadDocNo = lsDocNo1;
      }

      func.varsSet("liad_doc_no", liadDocNo);
    } else {
      func.varsSet("liad_doc_no", wp.itemStr("liad_doc_no"));
    }

    // 若judic_avoid_flag =‘Y’ (法院裁定免責)，取得judic_avoid_no (清算免責歸檔編號)
    String lsJudicAvoidNo = wp.itemStr("judic_avoid_no");
    String judicAvoidFlag = wp.itemStr("judic_avoid_flag");
    if (judicAvoidFlag.equals("Y") && empty(lsJudicAvoidNo)) {
      String sql2 =
          "select max(judic_avoid_no) as ls_judic_avoid_no ,to_char(sysdate,'yyyymm')||'0001' as ls_doc_no1"
              + " from col_liad_liquidate " + " where liad_doc_no like :liad_doc_no";
      setString("liad_doc_no", strMid(wp.sysDate, 0, 6) + "%");
      sqlSelect(sql2);
      lsJudicAvoidNo = sqlStr("ls_judic_avoid_no");
      lsDocNo1 = sqlStr("ls_doc_no1");
      if (sqlRowNum <= 0) {
        alertErr("select col_liad_liquidate err");
        return;
      }
      if (!empty(lsJudicAvoidNo) && pattern.matcher(lsJudicAvoidNo).matches() == true) {
        lsJudicAvoidNo = ((int) this.toNum(lsJudicAvoidNo) + 1) + "";
      } else {
        lsJudicAvoidNo = lsDocNo1;
      }
      func.varsSet("judic_avoid_no", lsJudicAvoidNo);
    }
    // else{
    // //若judic_avoid_flag <>‘Y’ (法院裁定免責<> Y)，清除judic_avoid_no
    // func.vars_set("judic_avoid_no", "");
    // }

    if (strAction.equals("A")) {
      // insert col_liad_liquidate
      if (func.insertColLiadLiquidate() == 1) {
        wp.alertMesg =
            "<script language='javascript'> alert('新增成功; 文件編號" + liadDocNo + "')</script>";
      } else {
        alertErr("insert col_liad_liquidate err");
        sqlCommit(0);
        return;
      }
    }

    String oLiquStatus = wp.itemStr("o_liqu_status");
    String liquStatus = wp.itemStr("liqu_status");
    String oCourtStatus = wp.itemStr("o_court_status");
    String courtStatus = wp.itemStr("court_status");

    if (!liquStatus.equals(oLiquStatus) || !courtStatus.equals(oCourtStatus)
        || strAction.equals("A")) {

      // 取得liad_doc_seqno(序號)
      int liadDocSeqno = fDocSeqno("col_liad_liquidate_court");
      func.varsSet("liad_doc_seqno", Integer.toString(liadDocSeqno));
      func.varsSet("unit_doc_no", "");
      func.varsSet("recv_date", wp.itemStr("recv_date"));
      func.varsSet("key_note", "");
      func.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
      func.varsSet("case_date", wp.itemStr("case_date_m"));
      func.insertColLiadLiquidateCourt();
    }


    // get mod_data for insert col_liad_mod_tmp
    String modData = wp.itemStr("recv_date") + "\";\"" + wp.itemStr("liqu_status") + "\";\""
        + wp.itemStr("branch_comb_flag") + "\";\"" + wp.itemStr("credit_branch") + "\";\""
        + wp.itemStr("max_bank_flag") + "\";\"" + idcAmtOrgDebt + "\";\""
        + wp.itemNum("org_debt_amt_bef_base") + "\";\"" + (long) liquLoseAmt + "\";\""
        + wp.itemStr("court_id") + "\";\"" + wp.itemStr("court_name") + "\";\""
        + wp.itemStr("doc_chi_name") + "\";\"" + wp.itemStr("court_dept") + "\";\""
        + wp.itemStr("court_status") + "\";\"" + (int) wp.itemNum("case_year") + "\";\""
        + wp.itemStr("case_letter") + "\";\"" + wp.itemStr("case_letter_desc") + "\";\""
        + wp.itemStr("judic_avoid_flag") + "\";\"" + wp.itemStr("judic_avoid_sure_flag") + "\";\""
        + lsJudicAvoidNo + "\";\"" + wp.itemStr("judic_date") + "\";\""
        + wp.itemStr("judic_action_flag") + "\";\"" + wp.itemStr("action_date_s") + "\";\""
        + wp.itemStr("judic_cancel_flag") + "\";\"" + wp.itemStr("cancel_date") + "\";\""
        + wp.itemStr("law_133_flag") + "\";\"";
    func.varsSet("data_type", "LIQUIDATE");
    func.varsSet("mod_data", modData);

    // 刪除前一次異動的暫存資料
    if (func.deleteColLiadModTmp2() != 1) {
      alertErr("delete COL_LIAD_MOD_TMP error");
      sqlCommit(0);
      dataReadC();
      return;
    }

    // 新增此次異動產生的暫存資料
    if (func.insertColLiadModTmp() != 1) {
      alertErr("insert COL_LIAD_MOD_TMP error");
      sqlCommit(0);
      if (!strAction.equals("A"))
        dataReadC();
      return;
    } else {
      if (strAction.equals("A")) {
        sqlCommit(1);
        clearFunc();
        errmsg("新增完成");
      } else {
        // 更新col_liad_liquidate的修改相關欄位,包括:mod_user、mod_time、mod_pgm、mod_seqno 等
        if (func.updateColLiadLiquidate() != 1) {
          sqlCommit(0);
          alertErr("update col_liad_liquidate err");
          dataReadC();
          return;
        }
        sqlCommit(1);
        dataReadC();
      }
    }
  }

  int ofValidationC() throws Exception {
    // 計算dw_data[].liqu_lose_amt的金額(清算損失金額 = 陳報法院債權金額)
    liquLoseAmt = wp.itemNum("org_debt_amt_bef");
    func.varsSet("liqu_lose_amt", liquLoseAmt + "");
    idcAmtOrgDebt = (int) wp.itemNum("org_debt_amt_bef");
    // 若is_action='D'(刪除模式)，檢查【credit_print_flag】、【jcic_send_flag】是否為Y，若為【Y】，需顯示警告訊息並 return -1。
    if (strAction.equals("D")) {
      if (wp.itemStr("credit_print_flag").equals("Y") || wp.itemStr("jcic_send_flag").equals("Y")) {
        alertErr("【資料已列印[債權陳報狀] or 報送聯徵 不可刪除】");
        return -1;
      }

      // 如果存在尚未完成報送的 col_liad_log資料，不可刪除
      String sqlSelect =
          " select count(*) as unproccnt from col_liad_log " + " where doc_no = :liad_doc_no "
              + " and liad_type = '2' " + " and event_type = 'S' " + " and proc_flag <> 'Y' ";
      setString("liad_doc_no", wp.itemStr("liad_doc_no"));
      sqlSelect(sqlSelect);

      if (sqlNum("unproccnt") > 0) {
        alertErr("【尚有待報送資料，不可刪除】");
        return -1;
      }
      return 1;
    }

    // 必填欄位檢核
    if (empty(wp.itemStr("recv_date"))) {
      alertErr("【收件日期不能為空】");
      return -1;
    }
    if (empty(wp.itemStr("liqu_status"))) {
      alertErr("【清算進度不能為空】");
      return -1;
    }
    if (empty(wp.itemStr("case_letter"))) {
      alertErr("【發文字號(案號字別&案號號碼)不能為空】");
      return -1;
    }
    if (empty(wp.itemStr("case_letter_desc"))) {
      alertErr("【法院函號不能為空】");
      return -1;
    }

    // 檢查【dw_data[].case_year】
    if (this.toInt(wp.itemStr("case_year")) < 0) {
      alertErr("【案號年度 不可小於 0】");
      return -1;
    }

    // 檢查身分證號，取得id_p_seqno、chi_name
    String lsIdNo = wp.itemStr("id_no");
    String sql1 =
        "select id_p_seqno,chi_name from crd_idno where id_no = :ls_id_no and id_no_code='0'";
    setString("ls_id_no", lsIdNo);
    sqlSelect(sql1);
    String isIdPSeqno = sqlStr("id_p_seqno");
    String isChiName = sqlStr("chi_name");
    if (sqlRowNum <= 0) {
      alertErr("身分證ID不存在");
      return -1;
    }
    func.varsSet("id_p_seqno", isIdPSeqno);
    func.varsSet("chi_name", isChiName);

    // 若為is_action='A'(新增模式)，取得卡數
    if (strAction.equals("A")) {
      String sql4 =
          "select count(*) as ii_card_num from crd_card where id_p_seqno = :is_id_p_seqno ";
      setString("is_id_p_seqno", isIdPSeqno);
      sqlSelect(sql4);
      String iiCardNum = sqlStr("ii_card_num");
      if (sqlCode < 0) {
        alertErr("select ii_card_num error");
        return -1;
      }
      func.varsSet("card_num", iiCardNum);
    }
    // 若為is_action='A'(新增模式)，取得帳戶資料，及相關金額值
    if (strAction.equals("A")) {
      // 取得帳戶資料
      String sql5 = " select count(*) as ii_acct_num" + " ,max(acct_status) as is_acct_status"
          + " ,max(payment_rate1) as li_mcode"
          + " from act_acno where id_p_seqno = :id_p_seqno and payment_rate1 <>'' ";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql5);
      String iiAcctNum = sqlStr("ii_acct_num");
      String isAcctStatus = sqlStr("is_acct_status");
      String isMCode = sqlStr("li_mcode");
      switch (isMCode) {
        case "0A":
          isMCode = "0A";
          break;
        case "0B":
          isMCode = "0B";
          break;
        case "0C":
          isMCode = "0C";
          break;
        case "0D":
          isMCode = "0D";
          break;
        case "0E":
          isMCode = "0E";
          break;
        default:
          isMCode = "00";
      }
      if (sqlCode < 0) {
        alertErr("select act_acno error");
        return -1;
      }
      func.varsSet("acct_num", iiAcctNum);
      func.varsSet("acct_status", isAcctStatus);
      func.varsSet("m_code", isMCode);
      // -read act_debt-
      // sql long time
      String sql6 = " select sum(decode(a.acct_code,'DB',a.end_bal)) idc_amt_db,"
          + " sum(decode(a.acct_code,'CB',a.end_bal)) idc_amt_cb, "
          + " sum(decode(a.acct_code,'RI',a.end_bal)) idc_amt_ri, "
          + " sum(decode(a.acct_code,'BL',a.end_bal, "
          + " 'IT',a.end_bal,'CA',a.end_bal,'ID',a.end_bal,'AO',a.end_bal,'OT',a.end_bal,0)) idc_amt_debt "
          + " from act_debt a join act_acno b on a.p_seqno = b.acno_p_seqno "
          + " where b.id_p_seqno like :is_id_p_seqno";
      setString("is_id_p_seqno", isIdPSeqno);
      sqlSelect(sql6);
      String badDebtAmt = sqlStr("idc_amt_db");
      String demandAmt = sqlStr("idc_amt_cb");
      String idcAmtRi = sqlStr("idc_amt_ri");
      String debtAmt = sqlStr("idc_amt_debt");
      if (sqlCode < 0) {
        badDebtAmt = "0";
        demandAmt = "0";
        idcAmtRi = "0";
        debtAmt = "0";
        alertErr("select ACT_DEBT error");
        return -1;
      }
      if (empty(badDebtAmt)) {
        badDebtAmt = "0";
      }
      if (empty(demandAmt)) {
        demandAmt = "0";
      }
      if (empty(idcAmtRi)) {
        idcAmtRi = "0";
      }
      if (empty(debtAmt)) {
        debtAmt = "0";
      }
      func.varsSet("bad_debt_amt", badDebtAmt);
      func.varsSet("demand_amt", demandAmt);
      func.varsSet("debt_amt", debtAmt);
    }

    if (strAction.equals("A")) {
      // -billed_amt+unbill_amt-
      String sql7 = " select sum(end_bal) as lm_debt from act_debt "
          + "where p_seqno in (select acno_p_seqno from act_acno where id_p_seqno =:id_p_seqno)";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql7);
      String lmDebt = sqlStr("lm_debt");
      if (sqlCode < 0) {
        alertErr("select ACT_DEBT error");
        return -1;
      }
      if (empty(lmDebt)) {
        lmDebt = "0";
      }
      // -歡喜卡之預借現金金額-
      String sql8 = "SELECT sum(a.cash_use_balance) as lm_cash_use "
          + "FROM act_combo_m_jrnl a, act_acno b " + "WHERE a.acct_type = b.acct_type "
          + "AND a.id_p_seqno  = b.id_p_seqno " + "AND b.id_p_seqno =:id_p_seqno";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql8);
      String lmCashUse = sqlStr("lm_cash_use");
      if (sqlCode < 0) {
        alertErr("select act_combo_m_jrnl error");
        return -1;
      }
      if (empty(lmCashUse)) {
        lmCashUse = "0";
      }
      // -分期未billing-
      String sql9 = "SELECT sum(a.unit_price * (a.install_tot_term - "
          + "a.install_curr_term ) + a.remd_amt+ "
          + "decode(a.install_curr_term,0,a.first_remd_amt,0)) as lm_unbill_amt, "
          + "sum(a.clt_unit_price * (a.clt_install_tot_term - "
          + "a.install_curr_term ) + a.clt_remd_amt ) as lm_unbill_fee "
          + "FROM bil_contract a, act_acno b " + "WHERE a.acct_type = b.acct_type "
          + "AND a.id_p_seqno = b.id_p_seqno " + "AND b.id_p_seqno =:id_p_seqno "
          + "AND a.install_tot_term != a.install_curr_term " + "AND a.refund_apr_flag != 'Y' "
          + "AND ( ( (decode(a.auth_code,'','N',a.auth_code) not in ('N','REJECT','P','reject') "
          + "and a.contract_kind = '2' ) " + "or a.contract_kind = '1') "
          + "and (a.apr_date <> '' or a.delv_confirm_date <> '')) ";
      setString("id_p_seqno", isIdPSeqno);
      sqlSelect(sql9);
      String lmUnbillAmt = sqlStr("lm_unbill_amt");
      String lmUnbillFee = sqlStr("lm_unbill_fee");
      if (sqlCode < 0) {
        alertErr("select BIL_CONTRACT error");
        return -1;
      }
      if (empty(lmUnbillAmt)) {
        lmUnbillAmt = "0";
      }
      if (empty(lmUnbillFee)) {
        lmUnbillFee = "0";
      }
      idcAmtOrgDebt = this.toInt(lmDebt) + this.toInt(lmCashUse) + this.toInt(lmUnbillAmt)
          + this.toInt(lmUnbillFee);
      func.varsSet("org_debt_amt", idcAmtOrgDebt + "");
      func.varsSet("org_debt_amt_bef", idcAmtOrgDebt + "");
    }

    // 資料檢查
    // 若【法院裁定免責】為Y，清算進度須為8，法院進度須為3。
    if (wp.itemStr("judic_avoid_flag").equals("Y")) {
      if (!wp.itemStr("liqu_status").equals("8") && !wp.itemStr("court_status").equals("3")) {
        alertErr("【[法院裁定免責]為Y，清算進度須為8，法院進度須為3】");
        return -1;
      }
    }
    // 若【法院裁定免責確定】為Y，清算進度須為8，法院進度須為3
    if (wp.itemStr("judic_avoid_sure_flag").equals("Y")) {
      if (!wp.itemStr("liqu_status").equals("8") && !wp.itemStr("court_status").equals("3")) {
        alertErr("【[法院裁定免責確定]為Y，清算進度須為8，法院進度須為3】");
        return -1;
      }
    }
    // 若清算進度為8(免責)，法院進度只能選3
    if (wp.itemStr("liqu_status").equals("8")) {
      if (!wp.itemStr("court_status").equals("3")) {
        alertErr("【清算進度為8，法院進度須為3】");
        return -1;
      }
    }
    // 若清算進度為9(不免責)，法院進度只能選6 或7
    if (wp.itemStr("liqu_status").equals("9")) {
      if (!wp.itemStr("court_status").equals("6") && !wp.itemStr("court_status").equals("7")) {
        alertErr("【清算進度為9，法院進度須為6或7】");
        return -1;
      }
    }

    // 若【法院裁定免責(judic_avoid_flag)】為Y，【是否133條不免責(law_133_flag)】不能為Y。
    if (wp.itemStr("judic_avoid_flag").equals("Y") && wp.itemStr("law_133_flag").equals("Y")) {
      alertErr("【法院裁定免責、是否133條不免責，不能同時為Y】");
      return -1;
    }

    // 取得法院中文名稱
    String courtName = "";
    String lsCourtId = wp.itemStr("court_id");
    String sql10 =
        " select wf_desc as ss from ptr_sys_idtab where wf_type = 'COURT_NAME' and wf_id = :ls_court_id ";
    setString("ls_court_id", lsCourtId);
    sqlSelect(sql10);
    courtName = sqlStr("ss");
    if (sqlRowNum > 0) {
      func.varsSet("court_name", courtName);
    } else {
      func.varsSet("court_name", lsCourtId);
    }

    // 設定資料內容
    if (strAction.equals("A")) {
      func.varsSet("crt_user", wp.loginUser);
      func.varsSet("crt_date", wp.sysDate);
      func.varsSet("apr_flag", "N");
      func.varsSet("apr_user", wp.loginUser);
      func.varsSet("apr_date", wp.sysDate);
    }

    return 1;
  }

  void gotoLiquUpdate() {
    String strModData = "", dbAudCode = "";
    String sql =
        "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'LIQUIDATE' and data_key = :data_key ";
    setString("data_key", kkLiadDocNo);
    sqlSelect(sql);
    strModData = sqlStr("mod_data");
    dbAudCode = sqlStr("aud_code");
    if (dbAudCode.equals("A")) {
      wp.colSet("btnDelete", "style='background: lightgray;' disabled");
    }
    wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
    String[] modData = strModData.split("\";\"", -1);
    if (sqlRowNum > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
      wp.colSet("btnUpdate2", "");
      if (!modData[0].equals(wp.colStr("recv_date"))) {
        wp.colSet("recv_date_pink", "pink");
        wp.colSet("recv_date", modData[0]);
      }
      if (!modData[1].equals(wp.colStr("liqu_status"))) {
        wp.colSet("liqu_status_pink", "pink");
        wp.colSet("liqu_status", modData[1]);
        wp.colSet("o_liqu_status", modData[1]);
      }
      if (!modData[2].equals(wp.colStr("branch_comb_flag"))) {
        wp.colSet("branch_comb_flag_pink", "pink");
        wp.colSet("branch_comb_flag", modData[2]);
      }
      if (!modData[3].equals(wp.colStr("credit_branch"))) {
        wp.colSet("credit_branch_pink", "pink");
        wp.colSet("credit_branch", modData[3]);
      }
      if (!modData[4].equals(wp.colStr("max_bank_flag"))) {
        wp.colSet("max_bank_flag_pink", "pink");
        wp.colSet("max_bank_flag", modData[4]);
      }
      if (this.toNum(modData[5]) != wp.colNum("org_debt_amt_bef")) {
        wp.colSet("org_debt_amt_bef_pink", "pink");
        wp.colSet("org_debt_amt_bef", modData[5]);
      }
      if (toNum(modData[6]) != wp.colNum("org_debt_amt_bef_base")) {
        wp.colSet("org_debt_amt_bef_base_pink", "pink");
        wp.colSet("org_debt_amt_bef_base", modData[6]);
      }
      if (this.toNum(modData[7]) != wp.colNum("liqu_lose_amt")) {
        wp.colSet("liqu_lose_amt_pink", "pink");
        wp.colSet("liqu_lose_amt", modData[7]);
      }
      if (!modData[8].equals(wp.colStr("court_id"))) {
        wp.colSet("court_id_pink", "pink");
        wp.colSet("court_id", modData[8]);
      }
      if (!modData[9].equals(wp.colStr("court_name"))) {
        // wp.col_set("court_name_pink", "pink");
        wp.colSet("court_name", modData[9]);
      }
      if (!modData[10].equals(wp.colStr("doc_chi_name"))) {
        wp.colSet("doc_chi_name_pink", "pink");
        wp.colSet("doc_chi_name", modData[10]);
      }
      if (!modData[11].equals(wp.colStr("court_dept"))) {
        wp.colSet("court_dept_pink", "pink");
        wp.colSet("court_dept", modData[11]);
      }
      if (!modData[12].equals(wp.colStr("court_status"))) {
        wp.colSet("court_status_pink", "pink");
        wp.colSet("court_status", modData[12]);
        wp.colSet("o_court_status", modData[12]);
      }
      if (!modData[13].equals(wp.colStr("case_year"))) {
        wp.colSet("case_year_pink", "pink");
        wp.colSet("case_year", modData[13]);
      }
      if (!modData[14].equals(wp.colStr("case_letter"))) {
        wp.colSet("case_letter_pink", "pink");
        wp.colSet("case_letter", modData[14]);
      }
      if (!modData[15].equals(wp.colStr("case_letter_desc"))) {
        wp.colSet("case_letter_desc_pink", "pink");
        wp.colSet("case_letter_desc", modData[15]);
      }
      if (!modData[16].equals(wp.colStr("judic_avoid_flag"))) {
        wp.colSet("judic_avoid_flag_pink", "pink");
        wp.colSet("judic_avoid_flag", modData[16]);
        wp.colSet("o_judic_avoid_flag", modData[16]);
      }
      if (!modData[17].equals(wp.colStr("judic_avoid_sure_flag"))) {
        wp.colSet("judic_avoid_sure_flag_pink", "pink");
        wp.colSet("judic_avoid_sure_flag", modData[17]);
      }
      if (!modData[18].equals(wp.colStr("judic_avoid_no"))) {
        wp.colSet("judic_avoid_no_pink", "pink");
        wp.colSet("judic_avoid_no", modData[18]);
      }
      if (!modData[19].equals(wp.colStr("judic_date"))) {
        wp.colSet("judic_date_pink", "pink");
        wp.colSet("judic_date", modData[19]);
      }
      if (!modData[20].equals(wp.colStr("judic_action_flag"))) {
        wp.colSet("judic_action_flag_pink", "pink");
        wp.colSet("judic_action_flag", modData[20]);
      }
      if (!modData[21].equals(wp.colStr("action_date_s"))) {
        wp.colSet("action_date_s_pink", "pink");
        wp.colSet("action_date_s", modData[21]);
      }
      if (!modData[22].equals(wp.colStr("judic_cancel_flag"))) {
        wp.colSet("judic_cancel_flag_pink", "pink");
        wp.colSet("judic_cancel_flag", modData[22]);
      }
      if (!modData[23].equals(wp.colStr("cancel_date"))) {
        wp.colSet("cancel_date_pink", "pink");
        wp.colSet("cancel_date", modData[23]);
      }
      if (!modData[24].equals(wp.colStr("law_133_flag"))) {
        wp.colSet("law_133_flag_pink", "pink");
        wp.colSet("law_133_flag", modData[24]);
      }
    } else {
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
    }
  }

  //////////////////// colm1170 function end/////////////////////////////

  //////////////////// colm1180 function start/////////////////////////////

  void saveFuncD() throws Exception {
    long llSeqno = 0;
    String llSeqnoStr = "";
    func = new Colm1140Func(wp);
    if (ofValidationD() != 1) {
      dataReadD();
      return;
    }
    // 新增一筆data_type 為'INST-MAST'的資料
    // get-mod_data-
    String modData = wp.itemStr("chi_name") + "\";\"" + wp.itemStr("recv_date") + "\";\""
        + wp.itemStr("renew_status") + "\";\"" + wp.itemStr("org_debt_amt") + "\";\""
        + wp.itemStr("court_status") + "\";\"" + wp.itemStr("liad_doc_no") + "\";\"";
    func.varsSet("aud_code", strAction);
    func.varsSet("mod_data", modData);
    func.varsSet("data_type", "INST-MAST");
    func.varsSet("ls_idcase", lsIdCase);
    if (func.deleteColLiadModTmpInstmast() != 1) {
      alertErr("delete COL_LIAD_MOD_TMP error");
      sqlCommit(0);
      // dataRead_d();
      return;
    }
    // -insert tmp-
    if (func.insertColLiadModTmp() != 1) {
      alertErr("insert COL_LIAD_MOD_TMP error");
      sqlCommit(0);
      return;
    }
    // 新增data_type 為'INST-DETL'的資料(所有頁面顯示之資料，包含已經存在於col_liad_install的資料)
    // 設定資料列排序，並依據產生各筆資料的【期數】內容。【期數】依據資料筆數值遞增。
    if (func.deleteColLiadModTmpInstdetl() != 1) {
      alertErr("delete COL_LIAD_MOD_TMP error");
      sqlCommit(0);
      // dataRead_d();
      return;
    }
    // get-mod_data-
    int rowidLength = 0;
    String[] opt = wp.itemBuff("opt");
    String[] rowid = wp.itemBuff("rowid");
    String[] aaSerNum = wp.itemBuff("ser_num");
    String[] aaInstDateS = wp.itemBuff("inst_date_s");
    String[] aaInstDateE = wp.itemBuff("inst_date_e");
    String[] aaArPerAmt = wp.itemBuff("ar_per_amt");
    String[] aaActPerAmt = wp.itemBuff("act_per_amt");
    String[] aaPayDate = wp.itemBuff("pay_date");
    String[] aaArTotAmt = wp.itemBuff("ar_tot_amt");
    String[] aaActTotAmt = wp.itemBuff("act_tot_amt");
    String[] aaUnpayAmt = wp.itemBuff("unpay_amt");
    String[] aaPaymentDay = wp.itemBuff("payment_day");
    String[] aaFromType = wp.itemBuff("from_type");
    rowidLength = rowid.length;
    if (empty(rowid[0])) {
      rowidLength = 0;
    }
    wp.listCount[0] = rowidLength;
    for (rr = 0; rr < rowidLength; rr++) {
      if (checkBoxOptOn(rr, opt)) {
        continue;
      }
      llSeqno++;
      String instSeq = aaSerNum[rr];
      String arPerAmt = aaArPerAmt[rr];
      String actPerAmt = aaActPerAmt[rr];
      String payDate = aaPayDate[rr];
      String arTotAmt = aaArTotAmt[rr];
      String actTotAmt = aaActTotAmt[rr];
      String unpayAmt = aaUnpayAmt[rr];
      String paymentDay = aaPaymentDay[rr];

      for (int i = instSeq.length(); i < 5; i++) {
        instSeq = "0" + instSeq;
      }
      for (int i = arPerAmt.length(); i < 9; i++) {
        arPerAmt = "0" + arPerAmt;
      }
      for (int i = actPerAmt.length(); i < 9; i++) {
        actPerAmt = "0" + actPerAmt;
      }
      if (empty(payDate)) {
        payDate = "        ";
      }
      for (int i = arTotAmt.length(); i < 9; i++) {
        arTotAmt = "0" + arTotAmt;
      }
      for (int i = actTotAmt.length(); i < 9; i++) {
        actTotAmt = "0" + actTotAmt;
      }
      for (int i = unpayAmt.length(); i < 9; i++) {
        unpayAmt = "0" + unpayAmt;
      }
      for (int i = paymentDay.length(); i < 2; i++) {
        paymentDay = "0" + paymentDay;
      }
      llSeqnoStr = String.format("%05d", llSeqno);
      modData = instSeq + "\";\"" + aaInstDateS[rr] + "\";\"" + aaInstDateE[rr] + "\";\"" + arPerAmt
          + "\";\"" + actPerAmt + "\";\"" + payDate + "\";\"" + arTotAmt + "\";\"" + actTotAmt
          + "\";\"" + unpayAmt + "\";\"" + paymentDay + "\";\"" + aaFromType[rr] + "\";\"";
      func.varsSet("mod_data", modData);
      func.varsSet("data_type", "INST-DETL");
      func.varsSet("ls_idcase", lsIdCase);
      func.varsSet("aud_code", strAction);
      arTotAmt = (this.toInt("ar_tot_amt") + this.toInt("ar_tot_amt")) + "";
      // -insert tmp-
      String lsDataKey = lsIdCase + "-" + llSeqnoStr;
      func.varsSet("data_key", lsDataKey);
      if (func.insertColLiadModTmp() != 1) {
        alertErr("insert COL_LIAD_MOD_TMP error");
        sqlCommit(0);
        return;
      }
    }
    sqlCommit(1);
    dataReadD();
  }

  void gotoRenewAptUpdate() throws Exception {
    String lsIdCase = "";
    String idDesc = "";
    String idCode = "";
    for (int i = kkIdNo.length(); i < 10; i++) {
      kkIdNo += " ";
    }
    for (int i = kkCaseLetter.length(); i < 10; i++) {
      kkCaseLetter += " ";
    }
    lsIdCase = kkIdNo + kkCaseLetter;
    String strModData = "", dbAudCode = "";

    String sql =
        "select mod_data,aud_code from col_liad_mod_tmp " + "where data_type = 'INST-MAST' "
            + "and data_key like :data_key " + " FETCH FIRST ROW ONLY ";
    setString("data_key", lsIdCase + "%");
    sqlSelect(sql);
    dbAudCode = sqlStr("aud_code");
    wp.colSet("db_aud_code", "");
    if (sqlRowNum > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
      wp.colSet("btnUpdate2", "");
      wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));

      // INST-MAST 有資料 才查 INST-DETL
      wp.selectSQL = " hex(rowid) as rowid " + " ,mod_data " + " ,aud_code ";
      wp.daoTable = " col_liad_mod_tmp ";
      wp.whereOrder = " ";
      wp.whereStr = " where 1=1 ";
      wp.whereStr += " and data_type = 'INST-DETL' " + " and data_key like :data_key ";
      setString("data_key", lsIdCase + "%");
      pageQuery();
      wp.notFound = "N";
      if (sqlNotFind()) {
      }
      wp.setListCount(1);
      if (sqlRowNum > 0) {
        wp.colSet("num", wp.selectCnt + "");
        // wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
        for (int i = 0; i < wp.selectCnt; i++) {
          strModData = wp.colStr(i, "mod_data");
          dbAudCode = wp.colStr(i, "aud_code");
          String[] modData2 = strModData.split("\";\"", -1);
          wp.colSet(i, "inst_date_s", modData2[1].trim());
          wp.colSet(i, "inst_date_e", modData2[2].trim());
          wp.colSet(i, "ar_per_amt", this.toInt(modData2[3].trim()) + "");
          wp.colSet(i, "act_per_amt", this.toInt(modData2[4].replaceFirst("^0*", "").trim()) + "");
          wp.colSet(i, "pay_date", modData2[5].trim());
          wp.colSet(i, "ar_tot_amt", this.toInt(modData2[6].trim()) + "");
          wp.colSet(i, "act_tot_amt", this.toInt(modData2[7].trim()) + "");
          wp.colSet(i, "unpay_amt", this.toInt(modData2[8].trim()) + "");
          String lsPaymentDay = this.toInt(modData2[9].trim()) + "";
          if (lsPaymentDay.length() == 1) {
            lsPaymentDay = "0" + lsPaymentDay;
          }
          wp.colSet(i, "payment_day", lsPaymentDay);
          wp.colSet(i, "from_type", modData2[10].trim());
          wp.colSet(i, "tt_from_type",
              commString.decode(modData2[10].trim(), ",Y,F", ",Y.人工補分期資料,F.補分期資料批次處理完成"));

          if (toInt(modData2[4].replaceFirst("^0*", "").trim()) != 0
              || modData2[10].trim().equals("F")) {
            wp.colSet(i, "d_disabled", "style='background: lightgray;' disabled");
          }
          if (modData2[10].trim().equals("Y")) {
            wp.colSet(i, "d_disabled", "");
          }
        }
      } else {
        wp.colSet("num", "0");
      }

    }
    idCode = kkRenewStatus;
    String sql1 = "select id_desc from col_liab_idtab where id_code = :id_code ";
    setString("id_code", idCode);
    sqlSelect(sql1);
    idDesc = sqlStr("id_desc");
    if (sqlRowNum <= 0) {
      idDesc = "";
    }
    wp.colSet("tt_renew_status", idDesc);
  }

  int ofValidationD() {
    kkIdNo = wp.itemStr("id_no");
    kkCaseLetter = wp.itemStr("case_letter");
    for (int i = kkIdNo.length(); i < 10; i++) {
      kkIdNo += " ";
    }
    for (int i = kkCaseLetter.length(); i < 10; i++) {
      kkCaseLetter += " ";
    }
    lsIdCase = kkIdNo + kkCaseLetter;
    // 查詢是否有暫存異動資料(data_type = 'INST-MAST')，若有，且為刪除模式(is_action='D')，則顯示錯誤訊息。
    String sqlSelect1 = "select count(*) as ll_cnt from col_liad_mod_tmp "
        + "where data_type = 'INST-MAST' " + "and data_key = :ls_id_case ";
    setString("ls_id_case", lsIdCase);
    sqlSelect(sqlSelect1);
    if (this.toInt(sqlStr("ll_cnt")) > 0 && strAction.equals("D")) {
      alertErr("【資料有修改, 請取消修改再刪除】");
      return -1;
    }
    // 已繳款，則不可刪除
    String sqlSelect2 = "select count(*) as ll_cnt from col_liad_install "
        + "where holder_id_p_seqno = :id_p_seqno " + "and case_letter = :is_case_letter "
        + "and act_per_amt > 0 ";
    setString("id_p_seqno", wp.itemStr("id_p_seqno"));
    setString("is_case_letter", wp.itemStr("case_letter"));
    sqlSelect(sqlSelect2);
    if (this.toInt(sqlStr("ll_cnt")) > 0 && strAction.equals("D")) {
      alertErr("【更生認可資料已繳款, 不可刪除】");
      return -1;
    }
    // 檢查輸入資料
    if (empty(wp.itemStr("db_payment_day"))) {
      if (this.toInt(wp.itemStr("db_payment_day")) < 0
          || this.toInt(wp.itemStr("db_payment_day")) > 31) {
        alertErr("【每期繳款日 須為 0 ～ 31】");
        return -1;
      }
    }
    // 檢查設定分期結果之資料
    // if(wf_chk_detail()!=1){
    // return -1;
    // }
    return 1;
  }

  int wfChkDetail() {
    // 檢查分期設定結果資料，通過檢查者，可進行DB處理頁面分期設定結果資料，設定為dw_detail;
    // 被勾選的資料，設定於dw_detail.selectrow;
    int rowidLength = 0;
    String[] aaActPerAmt = wp.itemBuff("act_per_amt");
    String[] opt = wp.itemBuff("opt");
    String[] rowid = wp.itemBuff("rowid");
    String[] aaPaymentDay = wp.itemBuff("payment_day");
    String[] aaInstDateS = wp.itemBuff("inst_date_s");
    String[] aaInstDateE = wp.itemBuff("inst_date_e");
    String[] aaArPerAmt = wp.itemBuff("ar_per_amt");
    rowidLength = rowid.length;
    if (empty(rowid[0])) {
      rowidLength = 0;
    }
    wp.listCount[0] = rowidLength;
    for (rr = 0; rr < rowidLength; rr++) {
      if (checkBoxOptOn(rr, opt)) {
        if (this.toInt(aaActPerAmt[rr]) > 0) {
          alertErr("【分期已繳款 不可刪除】");
          wp.colSet(rr, "ok_flag", "!");
          return -1;
        }
        continue;
      }
      // 若每期繳款日未輸入，顯示錯誤訊息，並return -1。
      if (empty(aaPaymentDay[rr])) {
        alertErr("【每期繳款日 不可空白】");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
      // 若繳款金額<0，顯示錯誤訊息，並return -1
      if (this.toNum(aaArPerAmt[rr]) <= 0) {
        alertErr("【繳款金額 須大於 0】");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
      // 檢查期數月份
      if (empty(aaInstDateS[rr]) && empty(aaInstDateE[rr])) {
        continue;
      }
      if (empty(aaInstDateS[rr]) || empty(aaInstDateE[rr])) {
        alertErr("【期數月份 須同時空白 or 同時輸入】");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
      // 檢查期數月份合理性
      if (this.toInt(aaInstDateS[rr]) > this.toInt(aaInstDateE[rr])) {
        alertErr("【期數月份 起月不可大於迄月】");
        wp.colSet(rr, "ok_flag", "!");
        return -1;
      }
    }
    return 1;
  }

  //////////////////// colm1180 function end/////////////////////////////

  //////////////////// colm1190 function Start/////////////////////////////
  void saveFuncE() throws Exception {
    func = new Colm1140Func(wp);
    String lsAudCode = "";
    if (ofValidationE() != 1) {
      dataReadE();
      return;
    }
    // 設定ls_aud_code
    lsAudCode = strAction;


    // get mod_data for insert col_liad_mod_tmp
    String modData = wp.itemStr("pay_normal_flag_2") + "\";\"" + wp.itemStr("debt_amt2") + "\";\""
        + wp.itemStr("alloc_debt_amt") + "\";\"" + wp.itemStr("unalloc_debt_amt") + "\";\""
        + wp.itemStr("coll_remark") + "\";\"" + wp.itemStr("send_flag_571") + "\";\""
        + wp.itemStr("crt_date") + "\";\"" + wp.itemStr("crt_user") + "\";\"";
    func.varsSet("data_type", "COLL-DATA");
    func.varsSet("mod_data", modData);
    func.varsSet("aud_code", lsAudCode);
    // 刪除前一次異動的暫存資料
    if (func.deleteColLiadModTmpColldata() != 1) {
      alertErr("delete COL_LIAD_MOD_TMP error");
      sqlCommit(0);
      dataReadE();
      return;
    }

    // 新增此次異動產生的暫存資料
    if (func.insertColLiadModTmp() != 1) {
      alertErr("insert COL_LIAD_MOD_TMP error");
      sqlCommit(0);
      dataReadE();
      return;
    } else {
      if (func.updateColLiad570() != 1) {
        alertErr("update col_liad_570 error");
        sqlCommit(0);
        dataReadE();
        return;
      }
      sqlCommit(1);
      dataReadE();
    }

  }

  // 檢查此筆資料是否已有暫存資料，等待覆核。
  void gotoColldataUpdate() {
    String strModData = "", dbAudCode = "";
    String sql =
        "select mod_data,aud_code from col_liad_mod_tmp " + "where data_type = 'COLL-DATA' "
            + "and data_key = rpad ( :kk_id_no , 10, ' ') || rpad ( :kk_case_letter , 10, ' ') ";
    setString("kk_id_no", kkIdNo);
    setString("kk_case_letter", kkCaseLetter);
    sqlSelect(sql);
    strModData = sqlStr("mod_data");
    dbAudCode = sqlStr("aud_code");
    wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
    String[] modData = strModData.split("\";\"", -1);
    if (sqlRowNum > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
      wp.colSet("btnUpdate2", "");
      if (!modData[0].equals(wp.colStr("pay_normal_flag_2"))) {
        wp.colSet("pay_normal_flag_2_pink", "pink");
        wp.colSet("pay_normal_flag_2", modData[0]);
      }
      if (!modData[1].equals(wp.colStr("debt_amt2"))) {
        wp.colSet("debt_amt2_pink", "pink");
        wp.colSet("debt_amt2", modData[1]);
      }
      if (!modData[2].equals(wp.colStr("alloc_debt_amt"))) {
        wp.colSet("alloc_debt_amt_pink", "pink");
        wp.colSet("alloc_debt_amt", modData[2]);
      }
      if (!modData[3].equals(wp.colStr("unalloc_debt_amt"))) {
        wp.colSet("unalloc_debt_amt_pink", "pink");
        wp.colSet("unalloc_debt_amt", modData[3]);
      }
      if (!modData[4].equals(wp.colStr("coll_remark"))) {
        wp.colSet("coll_remark_pink", "pink");
        wp.colSet("coll_remark", modData[4]);
      }
      if (!modData[5].equals(wp.colStr("send_flag_571"))) {
        wp.colSet("send_flag_571_pink", "pink");
        wp.colSet("send_flag_571", modData[5]);
      }
      if (!modData[6].equals(wp.colStr("crt_date"))) {
        wp.colSet("crt_date_pink", "pink");
        wp.colSet("crt_date", modData[6]);
      }
      if (!modData[7].equals(wp.colStr("crt_user"))) {
        wp.colSet("crt_user_pink", "pink");
        wp.colSet("crt_user", modData[7]);
      }
    } else {
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
      wp.colSet("apr_date", "");
      wp.colSet("apr_user", "");
    }
    wp.colSet("close_reason_disabled", "style='background: lightgray;' disabled");
    wp.colSet("close_remark_disabled", "style='background: lightgray;' disabled");
  }

  int ofValidationE() {
    // 檢查是否已經結案，已經結案的資料，不可異動
    if (!empty(wp.itemStr("close_date"))) {
      alertErr("【此案件已結案, 不可異動】");
      return -1;
    }
    // 已覆核未報送資料，不可異動。
    String sql1 = " select count (*) as ll_cnt " + " from col_liad_57x_log "
        + " where id_no = :id_no " + " and case_letter = :case_letter " + " and data_type = '571' "
        + " and send_proc_date = '' ";
    setString("id_no", wp.itemStr("id_no"));
    setString("case_letter", wp.itemStr("case_letter"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      alertErr("【統一收付回報債權, 已覆核未報送; 不可異動】");
      return -1;
    }

    if (wp.itemStr("tt_pay_normal_flag_2").equals(wp.itemStr("pay_normal_flag_2"))) {
      if (wp.itemStr("tt_debt_amt2").equals(wp.itemStr("debt_amt2"))) {
        if (wp.itemStr("tt_alloc_debt_amt").equals(wp.itemStr("alloc_debt_amt"))) {
          if (wp.itemStr("tt_unalloc_debt_amt").equals(wp.itemStr("unalloc_debt_amt"))) {
            if (wp.itemStr("tt_coll_remark").equals(wp.itemStr("coll_remark"))) {
              if (wp.itemStr("tt_send_flag_571").equals(wp.itemStr("send_flag_571"))) {
                alertErr("資料未異動，不可執行新增或修改作業!");
                return -1;
              }
            }
          }
        }
      }
    }
    return 1;
  }
  //////////////////// colm1190 function end/////////////////////////////

  //////////////////// colm1192 function Start/////////////////////////////

  void saveFuncE1() throws Exception {
    func = new Colm1140Func(wp);
    String lsAudCode = "";
    if (ofValidationE1() != 1) {
      strAction = "S5_1";
      dataReadE();
      return;
    }

    // 設定ls_aud_code
    lsAudCode = strAction;

    // get mod_data for insert col_liad_mod_tmp
    String modData = wp.itemStr("close_reason") + "\";\"" + wp.itemStr("close_remark") + "\";\""
        + wp.sysDate + "\";\"" + wp.loginUser + "\";\"";
    func.varsSet("data_type", "COLL-CLOSE");
    func.varsSet("mod_data", modData);
    func.varsSet("aud_code", lsAudCode);
    // 刪除前一次異動的暫存資料
    if (func.deleteColLiadModTmpCollclose() != 1) {
      strAction = "S5_1";
      dataReadE();
      alertErr("delete COL_LIAD_MOD_TMP error");
      return;
    }

    // 新增此次異動產生的暫存資料
    if (func.insertColLiadModTmp() != 1) {
      strAction = "S5_1";
      dataReadE();
      alertErr("insert COL_LIAD_MOD_TMP error");
      return;
    } else {
      strAction = "S5_1";
      func.updateColLiad570Close();
      dataReadE();
    }
  }

  // 檢查此筆資料是否已有暫存資料，等待覆核。
  void gotoCollCloseUpdate() {
    String strModData = "", dbAudCode = "", ss = "";
    String sql =
        "select mod_data,aud_code from col_liad_mod_tmp " + "where data_type = 'COLL-CLOSE' "
            + "and data_key = rpad (:kk_id_no, 10, ' ') || rpad (:kk_case_letter, 10, ' ') ";
    setString("kk_id_no", kkIdNo);
    setString("kk_case_letter", kkCaseLetter);
    sqlSelect(sql);
    strModData = sqlStr("mod_data");
    dbAudCode = sqlStr("aud_code");
    wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
    String[] modData = strModData.split("\";\"", -1);
    if (sqlRowNum > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
      wp.colSet("btnUpdate2", "");
      if (!modData[0].equals(wp.colStr("close_reason"))) {
        wp.colSet("close_reason_pink", "pink");
        wp.colSet("close_reason", modData[0]);
      }
      if (!modData[1].equals(wp.colStr("close_remark"))) {
        wp.colSet("close_remark_pink", "pink");
        wp.colSet("close_remark", modData[1]);
      }
      if (!modData[2].equals(wp.colStr("close_date"))) {
        // wp.col_set("close_date_pink", "pink");
        wp.colSet("close_date", modData[2]);
      }
      if (!modData[3].equals(wp.colStr("close_user"))) {
        wp.colSet("close_user_pink", "pink");
        wp.colSet("close_user", modData[3]);
      }
    } else {
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
    }
    if (empty(wp.colStr("proc_date_571"))) {
      wp.colSet("close_reason_disabled", "style='background: lightgray;' disabled");
      wp.colSet("close_remark_disabled", "style='background: lightgray;' disabled");
    }

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      ss = wp.colStr(ii, "pay_normal_flag_2");
      wp.colSet(ii, "pay_normal_flag_2", commString.decode(ss, ",Y,N", ",Y.正常,N.不正常"));
    }

  }

  int ofValidationE1() {
    if (!empty(wp.itemStr("close_apr_date"))) {
      alertErr("【主管已覆核 不可異動】");
      return -1;
    }
    if (empty(wp.itemStr("proc_date_571"))) {
      alertErr("【未回報債權 不可結案】");
      return -1;
    }
    if (empty(wp.itemStr("close_reason"))) {
      alertErr("【結案原因 不可空白】");
      return -1;
    }
    if (wp.itemStr("tt_close_reason").equals(wp.itemStr("close_reason"))
        && wp.itemStr("tt_close_remark").equals(wp.itemStr("close_remark"))) {
      alertErr("資料未異動，不可執行新增或修改作業!");
      return -1;
    }

    return 1;
  }

  //////////////////// colm1192 function end/////////////////////////////

  //////////////////// colm1196 colm1197 function start///////////////////////////
  void saveFuncF() throws Exception {
    func = new Colm1140Func(wp);
    String lsAudCode = "";
    if (strAction.equals("A") || strAction.equals("U")) {
      if (ofValidationF() != 1) {
        if (strAction.equals("U")) {
          dataReadF();
        }
        return;
      }
    }
    lsAudCode = strAction;
    if (!empty(wp.itemStr("liad_doc_no"))) {
      String sql1 =
          "select count(*) as cnt from col_liad_renew_court where liad_doc_no = :liad_doc_no and liad_doc_seqno = :liad_doc_seqno ";
      setString("liad_doc_no", wp.itemStr("liad_doc_no"));
      setString("liad_doc_seqno", wp.itemStr("liad_doc_seqno"));
      sqlSelect(sql1);
      if (sqlNum("cnt") > 0) {
        if (!wp.itemStr("apr_flag").equals("Y")) {
          lsAudCode = "A";
        }
      }
    }

    String liadDocNo = wp.itemStr("liad_doc_no");
    func.varsSet("aud_code", lsAudCode);
    func.varsSet("liad_doc_no", liadDocNo);

    if (strAction.equals("A")) {
      func.varsSet("unit_doc_no", wp.itemStr("unit_doc_no"));
      func.varsSet("recv_date", wp.itemStr("recv_date"));
      func.varsSet("key_note", wp.itemStr("key_note"));
      func.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
      func.varsSet("case_date", wp.itemStr("case_date"));

      // 取得liad_doc_seqno(序號)
      int liadDocSeqno = fDocSeqno("col_liad_renew_court");
      func.varsSet("liad_doc_seqno", Integer.toString(liadDocSeqno));

      // insert col_liad_renew_court
      if (func.insertColLiadRenewCourt() != 1) {
        sqlCommit(0);
        alertErr("insert col_liad_renew_court error");
        return;
      }
    }

    // 刪除前一次異動的暫存資料
    if (!strAction.equals("A")) {
      func.varsSet("liad_doc_seqno", wp.itemStr("liad_doc_seqno"));

      if (func.deleteColLiadModTmpRenew() != 1) {
        sqlCommit(0);
        dataReadF();
        alertErr("delete COL_LIAD_MOD_TMP error");
        return;
      }
    }
    // get mod_data for insert col_liad_mod_tmp
    String modData = wp.itemStr("unit_doc_no") + "\";\"" + wp.itemStr("recv_date") + "\";\""
        + wp.itemStr("key_note") + "\";\"" + wp.itemStr("case_letter_desc") + "\";\""
        + wp.itemStr("case_date") + "\";\"";
    func.varsSet("data_type", "RENEWCOURT");
    func.varsSet("mod_data", modData);
    if (func.insertColLiadModTmp() != 1) {
      sqlCommit(0);
      dataReadF();
      alertErr("insert COL_LIAD_MOD_TMP error");
      return;
    } else {
      if (strAction.equals("A")) {
        sqlCommit(1);
        clearFunc();
        errmsg("新增成功");
      } else {
        if (func.updateColLiadRenewCourt() != 1) {
          sqlCommit(0);
          dataReadF();
          alertErr("update col_liad_renew_court error");
          return;
        }
        sqlCommit(1);
        dataReadF();
      }
    }
  }

  void saveFuncG() throws Exception {
    func = new Colm1140Func(wp);
    String lsAudCode = "";
    if (strAction.equals("A") || strAction.equals("U")) {
      if (ofValidationF() != 1) {
        if (strAction.equals("U")) {
          dataReadG();
        }
        return;
      }
    }
    lsAudCode = strAction;
    if (!empty(wp.itemStr("liad_doc_no"))) {
      String sql1 =
          "select count(*) as cnt from col_liad_liquidate_court where liad_doc_no = :liad_doc_no and liad_doc_seqno = :liad_doc_seqno ";
      setString("liad_doc_no", wp.itemStr("liad_doc_no"));
      setString("liad_doc_seqno", wp.itemStr("liad_doc_seqno"));
      sqlSelect(sql1);
      if (sqlNum("cnt") > 0) {
        if (!wp.itemStr("apr_flag").equals("Y")) {
          lsAudCode = "A";
        }
      }
    }
    String liadDocNo = wp.itemStr("liad_doc_no");
    func.varsSet("aud_code", lsAudCode);
    func.varsSet("liad_doc_no", liadDocNo);

    if (strAction.equals("A")) {

      // 取得liad_doc_seqno(序號)
      int liadDocSeqno = fDocSeqno("col_liad_liquidate_court");
      func.varsSet("liad_doc_seqno", Integer.toString(liadDocSeqno));
      func.varsSet("unit_doc_no", wp.itemStr("unit_doc_no"));
      func.varsSet("recv_date", wp.itemStr("recv_date"));
      func.varsSet("key_note", wp.itemStr("key_note"));
      func.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
      func.varsSet("case_date", wp.itemStr("case_date"));

      // insert col_liad_liquidate_court
      if (func.insertColLiadLiquidateCourt() != 1) {
        sqlCommit(0);
        alertErr("insert col_liad_liquidate_court error");
        return;
      }
    }

    // 刪除前一次異動的暫存資料
    if (!strAction.equals("A")) {
      func.varsSet("liad_doc_seqno", wp.itemStr("liad_doc_seqno"));

      if (func.deleteColLiadModTmpLiquidate() != 1) {
        sqlCommit(0);
        dataReadG();
        alertErr("delete COL_LIAD_MOD_TMP error");
        return;
      }
    }

    // get mod_data for insert col_liad_mod_tmp
    String modData = wp.itemStr("unit_doc_no") + "\";\"" + wp.itemStr("recv_date") + "\";\""
        + wp.itemStr("key_note") + "\";\"" + wp.itemStr("case_letter_desc") + "\";\""
        + wp.itemStr("case_date") + "\";\"";
    func.varsSet("data_type", "LIQUICOURT");
    func.varsSet("mod_data", modData);
    if (func.insertColLiadModTmp() != 1) {
      sqlCommit(0);
      dataReadG();
      alertErr("insert COL_LIAD_MOD_TMP error");
      return;
    } else {
      if (strAction.equals("A")) {
        sqlCommit(1);
        clearFunc();
        errmsg("新增成功");
      } else {
        if (func.updateColLiadLiquidateCourt() != 1) {
          sqlCommit(0);
          dataReadG();
          alertErr("update col_liad_liquidate_court error");
          return;
        }
        sqlCommit(1);
        dataReadG();
      }
    }
  }

  // 檢查此筆資料是否已有暫存資料，等待覆核。
  void gotoCourtUpdate(String dataType) {
    String strModData = "", dbAudCode = "";
    String sql =
        "select mod_data,aud_code from col_liad_mod_tmp where data_type = :data_type and data_key = :kk_liad_doc_no ";
    setString("data_type", dataType);
    setString("kk_liad_doc_no", kkLiadDocNo + kkLiadDocSeqno);
    sqlSelect(sql);
    strModData = sqlStr("mod_data");
    dbAudCode = sqlStr("aud_code");
    wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
    String[] modData = strModData.split("\";\"", -1);
    if (sqlRowNum > 0) {
      wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
      wp.colSet("btnUpdate2", "");
      if (!modData[0].equals(wp.colStr("unit_doc_no"))) {
        wp.colSet("unit_doc_no_pink", "pink");
        wp.colSet("unit_doc_no", modData[0]);
      }
      if (!modData[1].equals(wp.colStr("recv_date"))) {
        wp.colSet("recv_date_pink", "pink");
        wp.colSet("recv_date", modData[1]);
      }
      if (!modData[2].equals(wp.colStr("key_note"))) {
        wp.colSet("key_note_pink", "pink");
        wp.colSet("key_note", modData[2]);
      }
      if (!modData[3].equals(wp.colStr("case_letter_desc"))) {
        wp.colSet("case_letter_desc_pink", "pink");
        wp.colSet("case_letter_desc", modData[3]);
      }
      if (!modData[4].equals(wp.colStr("case_date"))) {
        wp.colSet("case_date_pink", "pink");
        wp.colSet("case_date", modData[4]);
      }
    } else {
      wp.colSet("btnUpdate2", "style='background: lightgray;' disabled");
    }
  }

  int ofValidationF() {
    String isIdPSeqno = "", isChiName = "";
    // 若為刪除，不進行以下檢核
    if (strAction.equals("D")) {
      return -1;
    }
    // 若為新增模式，且存在於crd_idno table，則取回id_p_seqno、chi_name 欄位值。

    String isHolderId = wp.itemStr("db_id_no");
    if (empty(isHolderId)) {
      alertErr("身分證ID空白");
      return -1;
    }
    // 取得 id_p_seqn、chi_name
    String sql1 =
        "select id_p_seqno,chi_name from crd_idno where id_no = :is_holder_id and id_no_code='0'";
    setString("is_holder_id", isHolderId);
    sqlSelect(sql1);
    isIdPSeqno = sqlStr("id_p_seqno");
    isChiName = sqlStr("chi_name");
    if (sqlRowNum <= 0) {
      alertErr("身分證ID不存在");
      return -1;
    }
    if (strAction.equals("A")) {
      func.varsSet("id_p_seqno", isIdPSeqno);
      func.varsSet("chi_name", isChiName);
    }
    if (wp.itemStr("tt_db_id_no").equals(wp.itemStr("db_id_no"))) {
      if (wp.itemStr("tt_unit_doc_no").equals(wp.itemStr("unit_doc_no"))) {
        if (wp.itemStr("tt_recv_date").equals(wp.itemStr("recv_date"))) {
          if (wp.itemStr("tt_key_note").equals(wp.itemStr("key_note"))) {
            if (wp.itemStr("tt_case_letter_desc").equals(wp.itemStr("case_letter_desc"))) {
              if (wp.itemStr("tt_case_date").equals(wp.itemStr("case_date"))) {
                alertErr("資料未異動，不可執行新增或修改作業!");
                return -1;
              }
            }
          }
        }
      }
    }
    if (empty(wp.itemStr("key_note").trim())) {
      alertErr("主旨概要 不可空白!");
      return -1;
    }
    return 1;
  }

  int fDocSeqno(String tablename) {
    String sqlSelect = "";
    if (tablename.equals("col_liad_renew_court")) {
      sqlSelect =
          "select max(liad_doc_seqno) as max_liad_doc_seqno from col_liad_renew_court where liad_doc_no = :liad_doc_no ";
    }
    if (tablename.equals("col_liad_liquidate_court")) {
      sqlSelect =
          "select max(liad_doc_seqno) as max_liad_doc_seqno from col_liad_liquidate_court where liad_doc_no = :liad_doc_no ";
    }
    setString("liad_doc_no", wp.itemStr("liad_doc_no"));
    sqlSelect(sqlSelect);
    int maxLiadDocSeqno = (int) sqlNum("max_liad_doc_seqno");
    if (sqlRowNum > 0) {
      maxLiadDocSeqno++;
      return maxLiadDocSeqno;
    } else {
      return 1;
    }
  }


  //////////////////// colm1196 function end/////////////////////////////



  void listWkdataB() {
    String renewStatus = "";
    String idCode = "";
    String idDesc = "";
    String sql = "select id_code,id_desc from col_liab_idtab where id_key = '3' ";
    sqlSelect(sql);
    daoTid = "b-";
    for (int i = 0; i < sqlRowNum; i++) {
      idCode += "," + sqlStr(i, daoTid + "id_code");
      idDesc += "," + sqlStr(i, daoTid + "id_code") + "[" + sqlStr(i, daoTid + "id_desc") + "]";
    }
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      renewStatus = wp.colStr(ii, daoTid + "renew_status");
      wp.colSet(ii, daoTid + "tt_renew_status", commString.decode(renewStatus, idCode, idDesc));
    }
  }

  void listWkdataC() {
    String liquStatus = "";
    String idCode = "";
    String idDesc = "";
    String sql = "select id_code,id_desc from col_liab_idtab where id_key = '4' ";
    sqlSelect(sql);
    daoTid = "c-";
    for (int i = 0; i < sqlRowNum; i++) {
      idCode += "," + sqlStr(i, daoTid + "id_code");
      idDesc += "," + sqlStr(i, daoTid + "id_code") + "[" + sqlStr(i, daoTid + "id_desc") + "]";
    }
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      liquStatus = wp.colStr(ii, daoTid + "liqu_status");
      wp.colSet(ii, daoTid + "tt_liqu_status", commString.decode(liquStatus, idCode, idDesc));
    }

  }

  void listWkdataFg(int selectCnt, String dataType) {
    String liadDocNo = "", liadDocSeqno = "", sqlSelect = "";
    String strModData = "";
    for (int i = 0; i < selectCnt; i++) {
      liadDocNo = wp.colStr(i, "liad_doc_no_f");
      liadDocSeqno = wp.colStr(i, "liad_doc_seqno");

      sqlSelect =
          "select mod_data,aud_code from col_liad_mod_tmp where data_type = :data_type and data_key = :kk_liad_doc_no ";
      setString("kk_liad_doc_no", liadDocNo + liadDocSeqno);
      setString("data_type", dataType);
      sqlSelect(sqlSelect);
      strModData = sqlStr("mod_data");
      String[] modData = strModData.split("\";\"", -1);
      if (sqlRowNum > 0) {
        if (!modData[0].equals(wp.colStr(i, "unit_doc_no"))
            || !modData[1].equals(wp.colStr(i, "recv_date_f"))
            || !modData[2].equals(wp.colStr(i, "key_note"))
            || !modData[3].equals(wp.colStr(i, "case_letter_desc_f"))
            || !modData[4].equals(wp.colStr(i, "case_date"))) {
          wp.colSet(i, "background_color", "background-color:pink");
        }
      }
    }

  }

  void getReportDataA() throws Exception {
    String lsDocNo = "";
    lsDocNo = wp.itemStr("data_k1");
    wp.pageControl();
    if (empty(lsDocNo)) {
      lsDocNo = wp.itemStr(0, "b_liad_doc_no");
    }
    wp.selectSQL = " id_no " + " ,chi_name " + " ,recv_date " + " ,case_letter " + " ,m_code "
        + " ,law_user_id " + " ,demand_user_id " + " ,org_debt_amt " + " ,recv_doc_no "
        + " ,unit_doc_no " + " ,case_letter_desc " + " ,key_note " + " ,case_date "
        + " ,notify_date " + " ,put_file_pos "
        + " ,decode(col_liad_renew.max_bank_flag,'','N', max_bank_flag) max_bank_flag "
        + " ,court_curr_action " + " ,'' wp_temp ";

    wp.daoTable = " col_liad_renew ";
    wp.whereOrder = "  ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and liad_doc_no = :liad_doc_no ";
    setString("liad_doc_no", lsDocNo);
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    // wp.setPageValue();
    // dddw_select();
    listWkdataRepor();
  }

  void getReportDataB() throws Exception {
    String lsDocNo = "";
    lsDocNo = wp.itemStr("data_k1");
    if (empty(lsDocNo)) {
      lsDocNo = wp.itemStr(0, "c_liad_doc_no");
    }
    wp.pageControl();
    wp.selectSQL = " id_no " + " ,chi_name " + " ,recv_date " + " ,m_code " + " ,law_user_id "
        + " ,demand_user_id " + " ,org_debt_amt " + " ,recv_doc_no " + " ,unit_doc_no "
        + " ,case_letter_desc " + " ,key_note " + " ,case_date " + " ,notify_date "
        + " ,put_file_pos "
        + " ,decode(col_liad_liquidate.max_bank_flag,'', 'N', max_bank_flag) max_bank_flag "
        + " ,court_curr_action ";

    wp.daoTable = " col_liad_liquidate ";
    wp.whereOrder = "  ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and liad_doc_no = :liad_doc_no ";
    setString("liad_doc_no", lsDocNo);
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    // wp.setPageValue();
    // dddw_select();
    listWkdataRepor();
  }



  void listWkdataRepor() {
    String lawUserId = "";
    String usrCname = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      lawUserId = wp.colStr(ii, "demand_user_id");
      String sql1 = "select usr_cname from sec_user where usr_id = :ss ";
      setString("ss", lawUserId);
      sqlSelect(sql1);
      usrCname = sqlStr("usr_cname");
      if (sqlRowNum <= 0) {
        usrCname = "";
      }
      wp.colSet(ii, "tt_demand_user_id", usrCname);

      lawUserId = wp.colStr(ii, "law_user_id");
      String sql2 = "select usr_cname from sec_user where usr_id = :ss";
      setString("ss", lawUserId);
      sqlSelect(sql2);
      usrCname = sqlStr("usr_cname");
      if (sqlRowNum <= 0) {
        usrCname = "";
      }
      wp.colSet(ii, "tt_law_user_id", usrCname);
    }
  }

  void pdfPrint() throws Exception {
    if (wp.itemStr("data_kkpdf").equals("b")) {
      pdfPrintB();
    }
    if (wp.itemStr("data_kkpdf").equals("c")) {
      pdfPrintC();
    }
  }

  void pdfPrintB() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    mProgName = "colm1140";
    wp.reportId = mProgName;
    // -cond-
    /*
     * String ss = "異動日期 " + commString.ss_2ymd(wp.item_ss("ex_mod_date1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_mod_date2")); wp.col_set("cond_1", ss);
     */
    wp.pageRows = 9999;
    getReportDataA();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

  void pdfPrintC() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    mProgName = "colm1140";
    wp.reportId = mProgName;
    // -cond-
    /*
     * String ss = "異動日期 " + commString.ss_2ymd(wp.item_ss("ex_mod_date1")) + " -- " +
     * commString.ss_2ymd(wp.item_ss("ex_mod_date2")); wp.col_set("cond_1", ss);
     */
    wp.pageRows = 9999;
    getReportDataB();
    // wp.setListCount(1);

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "N";
    pdf.excelTemplate = mProgName + ".xlsx";
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
  }

  void fileZ60() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }
    TarokoFileAccess tf = new TarokoFileAccess(wp);
    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "UTF-8");
    if (fi == -1) {
      return;
    }
    int llCnt = 0, llErr = 0, llOk = 0;
    String exImpProc = "";
    String idNo = "", courtId = "", caseYear = "", caseLetter = "", caseNo = "", bulletDate = "",
        bulletDesc = "", courtName = "", dataDate = "";
    wp.colSet("ex_file", inputFile);

    while (true) {
      llCnt++;
      String file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
        break;
      }
      if (empty(file)) {
        continue;
      }
      String[] sa = file.split(",", -1);
      if (sa.length != 10) {
        llErr++;
        errlist.add("第" + llCnt + "筆資料:資料格式不符");
        continue;
      }
      idNo = sa[1];
      courtId = sa[2];
      caseYear = sa[3];
      caseLetter = sa[4];
      caseNo = sa[5];
      bulletDate = sa[6];
      bulletDate = westYear(bulletDate);
      if (empty(bulletDate)) {
        errlist.add("第" + llCnt + "筆資料:bullet_date日期格式不符");
        continue;
      }
      if (!isValidDate(bulletDate)) {
        errlist.add("第" + llCnt + "筆資料:bullet_date日期格式不符");
        continue;
      }

      bulletDesc = sa[7];
      courtName = sa[8];
      dataDate = sa[9];
      dataDate = westYear(dataDate);
      if (empty(dataDate)) {
        errlist.add("第" + llCnt + "筆資料:data_date日期格式不符");
        continue;
      }

      if (!isValidDate(dataDate)) {
        errlist.add("第" + llCnt + "筆資料:data_date日期格式不符");
        continue;
      }

      if (wfColLiadZ60Insert(idNo, llCnt) != 1) {
        llErr++;
        continue;
      }

      String sqlInsert = " insert into col_liad_z60(" + "liad_doc_no " + ",id_no " + ",court_id "
          + ",case_year " + ",case_letter " + ",case_no " + ",bullet_date " + ",bullet_desc "
          + ",court_name " + ",data_date " + ",id_p_seqno " + ",chi_name " + ",recv_date "
          + ",branch_comb_flag " + ",card_num " + ",acct_status " + ",m_code " + ",renew_flag "
          + ",liqui_flag " + ",manul_proc_flag " + ",bad_debt_amt " + ",demand_amt " + ",debt_amt "
          + ",crt_date " + ",crt_user " + ",apr_flag " + ",apr_user " + ",apr_date " + ",mod_time "
          + ",mod_user " + ",mod_pgm " + ",mod_seqno " + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?"
          + ",'N',?,?,?,?,?,?,?,?,?,?,?" + ",'Y',?,?,sysdate,?,?,1)";
      Object[] param = new Object[] {liadDocNo, idNo, courtId, caseYear, caseLetter, caseNo,
          bulletDate, bulletDesc, courtName, dataDate, idPSeqno, chiName, wp.sysDate, cardNum,
          acctStatus, mCode, renewFlag, liquiFlag, manulProcFlag, badDebtAmt, demandAmt, debtAmt,
          wp.sysDate, wp.loginUser, wp.loginUser, wp.sysDate, wp.loginUser, wp.modPgm(),};
      sqlExec(sqlInsert, param);
      if (sqlRowNum <= 0) {
        llErr++;
        errlist.add("第" + llCnt + "筆資料:匯入失敗");
        sqlCommit(0);
      } else {
        llOk++;
        sqlCommit(1);
      }

    }
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);
    exImpProc = "匯入說明: 成功" + llOk + "筆, 失敗" + llErr + "筆; 總比數:" + (llCnt - 1) + "筆";
    wp.colSet("ex_imp_proc", exImpProc);
    // wp.alertMesg += "<script language='javascript'> alert('"+errlist.size()+"')</script>";
    if (errlist.size() != 0) {
      for (int i = 0; i < errlist.size(); i++) {
        wp.alertMesg += "<script language='javascript'> alert('" + errlist.get(i) + "')</script>";
      }
      // errWriter(inputFile);
    }
  }

  void errWriter(String lsDoc) {
    String[] lsocErr = lsDoc.split(".txt");
    // wp.alertMesg += "<script language='javascript'> alert('"+ls_doc_err[0]+"')</script>";
    File mydir = new File("/err");
    mydir.mkdirs();
    File childFile = new File(mydir, lsocErr[0] + ".err");
    try (FileWriter filewrite = new FileWriter(childFile);
    	      BufferedWriter fileout = new BufferedWriter(filewrite);) {
      for (int i = 0; i < errlist.size(); i++) {

        fileout.write(errlist.get(i));
        fileout.newLine();
      }
      fileout.flush();
      filewrite.close();
    } catch (IOException e) {
      // wp.alertMesg += "<script language='javascript'> alert('errWriter')</script>";
    }
  }

  int wfColLiadZ60Insert(String isHolderId, int llCnt) {
    String lsDocNo = "", lsDocNo1 = "";
    Pattern pattern = Pattern.compile("[0-9]*");
    if (empty(isHolderId)) {
      errlist.add("第" + llCnt + "筆資料:" + "身分證ID空白");
      // wp.alertMesg += "<script language='javascript'> alert('身分證ID空白')</script>";
      return -1;
    }
    // 取得 id_p_seqn、chi_name
    String sql1 =
        "select id_p_seqno,chi_name from crd_idno where id_no = :is_holder_id and id_no_code='0'";
    setString("is_holder_id", isHolderId);
    sqlSelect(sql1);
    idPSeqno = sqlStr("id_p_seqno");
    chiName = sqlStr("chi_name");
    if (sqlRowNum <= 0) {
      errlist.add("第" + llCnt + "筆資料:" + "身分證ID不存在");
      // wp.alertMesg += "<script language='javascript'> alert('身分證ID不存在')</script>";
      return -1;
    }
    // 取得 liad_doc_no
    String sql2 =
        "select max(liad_doc_no) as ls_doc_no,to_char(sysdate,'yyyymm')||'0001' as ls_doc_no1"
            + " from col_liad_z60 " + " where liad_doc_no like :liad_doc_no ";
    setString("liad_doc_no", strMid(wp.sysDate, 0, 6) + "%");
    sqlSelect(sql2);
    lsDocNo = sqlStr("ls_doc_no");
    lsDocNo1 = sqlStr("ls_doc_no1");
    if (sqlRowNum <= 0) {
      errlist.add("第" + llCnt + "筆資料:" + "liad_doc_no不存在");
      return -1;
    }

    if (!empty(lsDocNo) && pattern.matcher(lsDocNo).matches() == true) {
      liadDocNo = ((int) this.toNum(lsDocNo) + 1) + "";
    } else {
      liadDocNo = lsDocNo1;
    }

    // 查詢卡數
    String sql3 = "select count(*) as ii_card_num from crd_card where id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", idPSeqno);
    sqlSelect(sql3);
    cardNum = sqlStr("ii_card_num");
    if (sqlRowNum <= 0) {
      errlist.add("第" + llCnt + "筆資料:" + "查詢卡數失敗");
      return -1;
    }
    // 取得帳戶資料
    String sql4 = " select count(*) as ii_acct_num" + " ,max(acct_status) as is_acct_status"
        + " ,max(payment_rate1) as li_mcode"
        + " from act_acno where id_p_seqno = :id_p_seqno and payment_rate1 <>'' ";
    setString("id_p_seqno", idPSeqno);
    sqlSelect(sql4);
    acctStatus = sqlStr("is_acct_status");
    mCode = sqlStr("li_mcode");
    if (sqlRowNum <= 0) {
      errlist.add("第" + llCnt + "筆資料:" + "查詢帳戶資料失敗");
      return -1;
    }
    // -read act_debt-
    String sql7 = " select sum(decode(a.acct_code,'DB',a.end_bal)) idc_amt_db, "
        + " sum(decode(a.acct_code,'CB',a.end_bal)) idc_amt_cb, "
        + " sum(decode(a.acct_code,'RI',a.end_bal)) idc_amt_ri, "
        + " sum(decode(a.acct_code,'BL',a.end_bal, "
        + " 'IT',a.end_bal,'CA',a.end_bal,'ID',a.end_bal,'AO',a.end_bal,'OT',a.end_bal,0)) idc_amt_debt "
        + " from act_debt a join act_acno b on a.p_seqno = b.acno_p_seqno "
        + " where b.id_p_seqno like :id_p_seqno";
    setString("id_p_seqno", idPSeqno + "%");
    sqlSelect(sql7);
    badDebtAmt = sqlStr("idc_amt_db");
    demandAmt = sqlStr("idc_amt_cb");
    String idcAmtRi = sqlStr("idc_amt_ri");
    debtAmt = sqlStr("idc_amt_debt");
    if (sqlCode == -1) {
      badDebtAmt = "0";
      demandAmt = "0";
      idcAmtRi = "0";
      debtAmt = "0";
      errlist.add("第" + llCnt + "筆資料:" + "select ACT_DEBT error");
      return -1;
    }
    if (empty(badDebtAmt)) {
      badDebtAmt = "0";
    }
    if (empty(demandAmt)) {
      demandAmt = "0";
    }
    if (empty(idcAmtRi)) {
      idcAmtRi = "0";
    }
    if (empty(debtAmt)) {
      debtAmt = "0";
    }

    // -check 公文-
    renewFlag = "N";
    liquiFlag = "N";
    manulProcFlag = "N";
    String sql5 = "select count(*) as ll_cnt from col_liad_renew where id_no = :is_holder_id ";
    setString("is_holder_id", isHolderId);
    sqlSelect(sql5);
    if (sqlRowNum > 0) {
      renewFlag = "Y";
    }
    String sql6 = "select count(*) as ll_cnt from col_liad_liquidate where id_no = :is_holder_id ";
    setString("is_holder_id", isHolderId);
    sqlSelect(sql6);
    if (sqlRowNum > 0) {
      liquiFlag = "Y";
    }

    return 1;
  }

  void wfDeleteModTmp() throws Exception {
    String dataType = "", dataType2 = "", mainTable = "";
    String sqlSelect = "";
    // 確認是否有liad_doc_no值
    String lsAprFlag = wp.itemStr("apr_flag");
    String lsDocNo = wp.itemStr("liad_doc_no");
    // String ls_id_no = wp.item_ss("id_no");
    // String ls_case_letter = wp.item_ss("case_letter");
    switch (strAction) {
      case "CU1":
        dataType = "Z60";
        mainTable = "col_liad_z60";
        break;
      case "CU2":
        dataType = "RENEW";
        mainTable = "col_liad_renew";
        break;
      case "CU3":
        dataType = "LIQUIDATE";
        mainTable = "col_liad_liquidate";
        break;
      case "CU4":
        dataType = "INST-MAST";
        dataType2 = "INST-DETL";
        kkIdNo = wp.itemStr("id_no");
        kkCaseLetter = wp.itemStr("case_letter");
        for (int i = kkIdNo.length(); i < 10; i++) {
          kkIdNo += " ";
        }
        for (int i = kkCaseLetter.length(); i < 10; i++) {
          kkCaseLetter += " ";
        }
        lsIdCase = kkIdNo + kkCaseLetter;
        break;
      case "CU5":
        dataType = "COLL-DATA";
        kkIdNo = wp.itemStr("id_no");
        kkCaseLetter = wp.itemStr("case_letter");
        break;
      case "CU5_1":
        dataType = "COLL-CLOSE";
        kkIdNo = wp.itemStr("id_no");
        kkCaseLetter = wp.itemStr("case_letter");
        break;
      case "CU6":
        dataType = "RENEWCOURT";
        mainTable = "col_liad_renew_court";
        break;
      case "CU7":
        dataType = "LIQUICOURT";
        mainTable = "col_liad_liquidate_court";
        break;
    }
    if (strAction.equals("CU4")) {
      if (empty(kkIdNo) || empty(kkCaseLetter)) {
        alertErr("未指定身分證 OR 發文文號, 無法取消修改");
        dataReadD();
        return;
      }
    } else if (strAction.equals("CU5") || strAction.equals("CU5_1")) {
      if (empty(kkIdNo) || empty(kkCaseLetter)) {
        alertErr("未指定身分證 OR 發文文號, 無法取消修改");
        dataReadE();
        return;
      }
    } else {
      if (empty(lsDocNo)) {
        alertErr("未指定文件編號, 無法取消修改");
        if (strAction.equals("CU1"))
          dataReadA();
        if (strAction.equals("CU2"))
          dataReadB();
        if (strAction.equals("CU6"))
          dataReadF();
        if (strAction.equals("CU7"))
          dataReadG();
        if (strAction.equals("CU5") || strAction.equals("CU5_1")) {
          dataReadE();
        }
        return;
      }
    }
    // 查詢col_liad_mod_tmp 資料，若存在資料，則予以刪除
    if (strAction.equals("CU5") || strAction.equals("CU5_1")) {
      String sqlSelectColLiadModTmp =
          "select data_type,data_key,aud_code,mod_data FROM col_liad_mod_tmp where "
              + " data_key = rpad ( :id_no, 10, ' ') || rpad ( :case_letter, 10, ' ') "
              + " and data_type= :data_type";
      setString("id_no", wp.itemStr("id_no"));
      setString("case_letter", wp.itemStr("case_letter"));
      setString("data_type", dataType);
      sqlSelect(sqlSelectColLiadModTmp);
      if (sqlRowNum <= 0) {
        alertErr("【無 [修改/結案] 資料可刪除】");
        dataReadE();
        return;
      }
    } else if (strAction.equals("CU4")) {
      String sqlSelectColLiadModTmp =
          "select data_type,data_key,aud_code,mod_data FROM col_liad_mod_tmp where "
              + " data_key = rpad ( :id_no, 10, ' ') || rpad ( :case_letter, 10, ' ') "
              + " and data_type= :data_type";
      setString("id_no", wp.itemStr("id_no"));
      setString("case_letter", wp.itemStr("case_letter"));
      setString("data_type", dataType);
      sqlSelect(sqlSelectColLiadModTmp);
      if (sqlRowNum <= 0) {
        alertErr("【無 [修改/結案] 資料可刪除】");
        dataReadD();
        return;
      }
    } else {
      String sqlSelectColLiadModTmp =
          "select data_type,data_key,aud_code,mod_data FROM col_liad_mod_tmp where "
              + " data_key = :data_key " + " and data_type= :data_type";
      if (strAction.equals("CU6") || strAction.equals("CU7")) {
        setString("data_key", wp.itemStr("liad_doc_no") + wp.itemStr("liad_doc_seqno"));

      } else {
        setString("data_key", wp.itemStr("liad_doc_no"));

      }
      setString("data_type", dataType);
      sqlSelect(sqlSelectColLiadModTmp);
      if (sqlRowNum <= 0) {
        alertErr("【無 [修改] 資料可刪除】");
        if (strAction.equals("CU1"))
          dataReadA();
        if (strAction.equals("CU2"))
          dataReadB();
        if (strAction.equals("CU3"))
          dataReadC();
        if (strAction.equals("CU6"))
          dataReadF();
        if (strAction.equals("CU7"))
          dataReadG();
        return;
      }
    }

    // 刪除col_liad_mod_tmp 資料
    if (strAction.equals("CU5") || strAction.equals("CU5_1")) {
      String sqlDelete1 = " delete col_liad_mod_tmp where "
          + " data_key = rpad ( :id_no, 10, ' ') || rpad ( :case_letter, 10, ' ') "
          + " and data_type = :data_type ";
      setString("id_no", wp.itemStr("id_no"));
      setString("data_type", dataType);
      setString("case_letter", wp.itemStr("case_letter"));
      sqlExec(sqlDelete1);
      if (sqlRowNum <= 0) {
        alertErr("【取消修改/結案 失敗】");
        dataReadE();
        sqlCommit(0);
        return;
      } else {
        alertMsg("【取消修改/結案  成功】");
        sqlCommit(1);
        dataReadE();
        return;
      }
    } else if (strAction.equals("CU4")) {
      String sqlDeleteInstMast = "delete col_liad_mod_tmp where " + " data_key = :data_key "
          + " and data_type = :data_type";
      setString("data_key", lsIdCase);
      setString("data_type", dataType);
      sqlExec(sqlDeleteInstMast);

      if (sqlRowNum <= 0) {
        alertErr("delete COL_LIAD_MOD_TMP error");
        sqlCommit(0);
        dataReadD();
        return;
      }
      String sqlDeleteInstDetl = "delete col_liad_mod_tmp where " + " data_key like :data_key "
          + " and data_type= :data_type2";
      setString("data_key", lsIdCase + "%");
      setString("data_type2", dataType2);
      sqlExec(sqlDeleteInstDetl);

      if (sqlCode < 0) {
        alertErr("delete COL_LIAD_MOD_TMP error");
        sqlCommit(0);
        dataReadD();
        return;
      }
      sqlCommit(1);
      dataReadD();
    } else {
      String sqlDelete1 = " delete col_liad_mod_tmp where " + " data_key = :data_key "
          + " and data_type = :data_type";
      if (strAction.equals("CU6") || strAction.equals("CU7")) {
        setString("data_key", wp.itemStr("liad_doc_no") + wp.itemStr("liad_doc_seqno"));
      } else {
        setString("data_key", wp.itemStr("liad_doc_no"));
      }
      setString("data_type", dataType);
      sqlExec(sqlDelete1);
      if (sqlRowNum <= 0) {
        alertErr("【取消修改失敗】");
        if (strAction.equals("CU1"))
          dataReadA();
        if (strAction.equals("CU2"))
          dataReadB();
        if (strAction.equals("CU3"))
          dataReadC();
        if (strAction.equals("CU6"))
          dataReadF();
        if (strAction.equals("CU7"))
          dataReadG();
        sqlCommit(0);
        return;
      }
    }

    if (strAction.equals("CU1")) {
      sqlSelect = "select apr_flag from col_liad_z60 where liad_doc_no= :liad_doc_no";
    }
    if (strAction.equals("CU2")) {
      sqlSelect = "select apr_flag from col_liad_renew where liad_doc_no= :liad_doc_no";
    }
    if (strAction.equals("CU3")) {
      sqlSelect = "select apr_flag from col_liad_liquidate where liad_doc_no= :liad_doc_no";
    }
    if (strAction.equals("CU4")) {
      return;
    }
    if (strAction.equals("CU6")) {
      sqlSelect =
          "select apr_flag from col_liad_renew_court where liad_doc_no= :liad_doc_no and liad_doc_seqno = :liad_doc_seqno";
    }
    if (strAction.equals("CU7")) {
      sqlSelect =
          "select apr_flag from col_liad_liquidate_court where liad_doc_no= :liad_doc_no and liad_doc_seqno = :liad_doc_seqno";
    }
    setString("liad_doc_no", lsDocNo);
    setString("liad_doc_seqno", wp.itemStr("liad_doc_seqno"));
    sqlSelect(sqlSelect);
    lsAprFlag = sqlStr("apr_flag");
    // 若ls_apr_flag<>'Y'，表示新增尚未覆核，可以逕行取消新增。故直接刪除main_table資料。
    StringBuffer str = new StringBuffer();
    if (!lsAprFlag.equals("Y")) {
      str.append(" delete ");
      str.append(mainTable);
      str.append(" where liad_doc_no = '");
      str.append(lsDocNo);
      str.append("'");
      if (strAction.equals("CU6") || strAction.equals("CU7")) {
        str.append(" and liad_doc_seqno = '");
        str.append(wp.itemStr("liad_doc_seqno"));
        str.append("'");
      }
      String sqlDelete2 = str.toString();
      sqlExec(sqlDelete2);
      if (sqlRowNum <= 0) {
        alertErr("【取消修改 失敗】");
        sqlCommit(0);
      } else {
        sqlCommit(1);
        alertMsg("【取消修改 成功】");
      }
    }
    if (lsAprFlag.equals("Y")) {
      if (strAction.equals("CU1"))
        dataReadA();
      if (strAction.equals("CU2"))
        dataReadB();
      if (strAction.equals("CU3"))
        dataReadC();
      if (strAction.equals("CU6"))
        dataReadF();
      if (strAction.equals("CU7"))
        dataReadG();
    } else {
      clearFunc();
    }
  }

  // public void errmsg(String msg){
  // alertErr(msg);
  // }

  // 轉西元
  String westYear(String str) {
    String date = str, y = "", md = "";
    if (date.length() == 8) {
      return date;
    }

    if (date.length() == 6) {
      date = "0" + date;
    }

    if (date.length() == 7) {
      y = strMid(date, 0, 3);
      y = Integer.toString(this.toInt(y) + 1911);
      md = strMid(date, 3, 7);
      date = y + md;
      return date;
    }

    return "";
  }

  // 判斷日期格式
  boolean isValidDate(String str) {
    boolean convertSuccess = true;
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    try {
      format.setLenient(false);
      format.parse(str);
    } catch (ParseException e) {
      convertSuccess = false;
    }
    return convertSuccess;
  }
}
