/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 109-04-21  V1.00.01  shiyuqi    updated for project coding standard        *
* 111-10-27  V1.00.02  Simon      sync codes with mega                       *
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actm0410 extends BaseEdit {
  String mAccttype = "";
  String mAcctkey = "";
  String mCurrcode = "";
  String idpseqno = "";

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
    } else if (eqIgno(wp.buttonCode, "I")) {
      /* 動態查詢 */
      wfReadIdno(wp.itemStr("kk_id_no"));
      wfReadAcno(idpseqno);
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  private int getWhereStr() throws Exception {

    wp.whereStr = " where 1=1 and crd_idno.id_p_seqno = act_pay_sms.id_p_seqno ";

    if (empty(wp.itemStr("ex_idno")) == false) {
      // wp.whereStr += " and act_pay_sms.id_no = :ex_idno ";
      wp.whereStr += " and crd_idno.id_no = :ex_idno ";
      setString("ex_idno", wp.itemStr("ex_idno"));
    }

    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // 設定queryRead() SQL條件

    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "hex(act_pay_sms.rowid) as rowid," + "crd_idno.chi_name," + "crd_idno.id_no,"
        + "act_pay_sms.id_p_seqno, " + "act_pay_sms.sms_s_acct_month,"
        + "act_pay_sms.sms_e_acct_month," + "act_pay_sms.lastpay_date_m3,"
        + "act_pay_sms.lastpay_date_m2," + "act_pay_sms.lastpay_date_m1,"
        + "act_pay_sms.lastpay_date_m0," + "act_pay_sms.lastpay_date_p1,"
        + "act_pay_sms.lastpay_date_p2," + "act_pay_sms.lastpay_date_p3,"
        + "act_pay_sms.m0_acct_month," + "act_pay_sms.sms_acct_month," + "act_pay_sms.stop_s_date,"
        + "act_pay_sms.stop_e_date," + "act_pay_sms.proc_flag," + "act_pay_sms.proc_date,"
        + "act_pay_sms.apr_date," + "act_pay_sms.apr_time," + "act_pay_sms.apr_user,"
        + "act_pay_sms.crt_user," + "act_pay_sms.crt_date," + "act_pay_sms.crt_time";

    wp.daoTable = " act_pay_sms , crd_idno ";
    wp.whereOrder = "  ";

    getWhereStr();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    idpseqno = wp.itemStr("data_k2");
    if (empty(idpseqno)) {
      if (wfReadIdno(wp.itemStr("kk_id_no")) != 1) {
        return;
      }
      if (wfReadAcno(idpseqno) != 1) {
        return;
      }
    }

    wp.sqlCmd = "  SELECT hex(act_pay_sms.rowid) as rowid,               "
        + "         crd_idno.chi_name,                               "
        + "         crd_idno.id_no,                                  "
        + "         crd_idno.cellar_phone,                           "
        + "         act_pay_sms.id_p_seqno,                          "
        + "         act_pay_sms.sms_s_acct_month,                    "
        + "         act_pay_sms.sms_e_acct_month,                    "
        + "         act_pay_sms.lastpay_date_m3,                     "
        + "         act_pay_sms.lastpay_date_m2,                     "
        + "         act_pay_sms.lastpay_date_m1,                     "
        + "         act_pay_sms.lastpay_date_m0,                     "
        + "         act_pay_sms.lastpay_date_p1,                     "
        + "         act_pay_sms.lastpay_date_p2,                     "
        + "         act_pay_sms.lastpay_date_p3,                     "
        + "         act_pay_sms.m0_acct_month,                       "
        + "         act_pay_sms.sms_acct_month,                      "
        + "         act_pay_sms.stop_s_date,                         "
        + "         act_pay_sms.stop_e_date,                         "
        + "         act_pay_sms.proc_flag,                           "
        + "         act_pay_sms.proc_date,                           "
        + "         act_pay_sms.apr_date,                            "
        + "         act_pay_sms.apr_time,                            "
        + "         act_pay_sms.apr_user,                            "
        + "         act_pay_sms.crt_user,                            "
        + "         act_pay_sms.crt_date,                            "
        + "         act_pay_sms.crt_time                             "
        + "    FROM crd_idno,   act_pay_sms                          "
        + "   WHERE ( crd_idno.id_p_seqno = act_pay_sms.id_p_seqno ) "
        + "     ANd act_pay_sms.id_p_seqno = :id_p_seqno                  ";

    setString("id_p_seqno", idpseqno);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, id_p_seqno= " + idpseqno);
      return;
    }
    listWkdata();
    dddwSelect();
  }

  @Override
  public void saveFunc() throws Exception {

    Actm0410Func func = new Actm0410Func(wp);
    /*
     * if (!check_approve(wp.item_ss("approval_user"),wp.item_ss("approval_passwd"))) { return; }
     */
    if (!strAction.equals("D")) {
      if (ofValidation() != 1) {
        return;
      }
    }

    func.varsSet("id_p_seqno", idpseqno);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
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

  }

  int wfReadAcno(String asIdPSeqno) throws Exception {
    // String ls_sql = " select next_lastpay_date from "
    // + " (select row_number() over (order by b.next_lastpay_date) as rownum, "
    // + " b.next_lastpay_date "
    // + " from act_acno a, ptr_workday b "
    // + " where 1=1 "
    // + " and a.stmt_cycle = b.stmt_cycle "
    // + " and a.id_p_seqno = :id_p_seqno ) x "
    // + " where x.rownum < 2 ";
    String lsSql = " select this_lastpay_date from                                       "
        + " (select row_number() over (order by b.this_lastpay_date) as rownum, "
        + "         b.this_lastpay_date                                         "
        + "   from act_acno a, ptr_workday b                                    "
        + "  where 1=1                                                          "
        + "    and a.stmt_cycle = b.stmt_cycle                                  "
        + "    and a.id_p_seqno = :id_p_seqno ) x                               "
        + " where x.rownum < 2                                                  ";
    setString("id_p_seqno", asIdPSeqno);

    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      // wp.col_set("db_lastpay_date", sql_ss("next_lastpay_date"));
      wp.colSet("db_lastpay_date", sqlStr("this_lastpay_date"));
    } else {
    //alertErr("select act_acno a, ptr_workday b");
			alertErr("無帳戶基本資料");
      return -1;
    }

    return 1;
  }


  int wfReadIdno(String asId) throws Exception {

    if (empty(asId)) {
      return -1;
    }
    String lsSql = "";
    lsSql = "SELECT id_p_seqno, chi_name, cellar_phone FROM crd_idno WHERE id_no = :id_no";
    setString("id_no", asId);
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("cname", sqlStr("chi_name"));
      wp.colSet("db_cell_phone", sqlStr("cellar_phone"));
      // wp.col_set("crt_user", wp.loginUser);
      // wp.col_set("crt_date",get_sysDate());
      // wp.col_set("id_on", as_id);
      idpseqno = sqlStr("id_p_seqno");
    } else {
      alertErr("身分證號 Error");
      return -1;
    }
    return 1;
  }

  int ofValidation() throws Exception {
    if (empty(wp.itemStr("lastpay_date_M3")) && empty(wp.itemStr("lastpay_date_M2"))
        && empty(wp.itemStr("lastpay_date_M1")) && empty(wp.itemStr("lastpay_date_M0"))
        && empty(wp.itemStr("lastpay_date_p1")) && empty(wp.itemStr("lastpay_date_p2"))
        && empty(wp.itemStr("lastpay_date_p3"))) {
      alertErr("發送條件, 請輸入 !");
      return -1;
    }

    String lsDate1 = wp.itemStr("sms_s_acct_month");
    String lsDate2 = wp.itemStr("sms_e_acct_month");
    String lsDateYymm = strMid(getSysDate(), 0, 6);
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[發送起迄年月-起迄]  輸入錯誤");
      return -1;
    }

    if (this.chkStrend(lsDateYymm, lsDate1) == false) {
      alertErr2("[發送起迄年月-起]  不能小於這個月");
      return -1;
    }
    if (this.chkStrend(lsDateYymm, lsDate2) == false) {
      alertErr2("[發送起迄年月-迄]  不能小於這個月");
      return -1;
    }

    lsDate1 = wp.itemStr("stop_s_date");
    lsDate2 = wp.itemStr("stop_e_date");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[暫停發送起迄日期-起迄]  不能小於這個月");
      return -1;
    }

    if (this.chkStrend(lsDateYymm, strMid(lsDate1, 0, 6)) == false) {
      alertErr2("[暫停發送起迄日期-起]  不能小於這個月");
      return -1;
    }
    if (this.chkStrend(lsDateYymm, strMid(lsDate2, 0, 6)) == false) {
      alertErr2("[暫停發送起迄日期-起迄]  輸入錯誤");
      return -1;
    }
    String lsIdNo = "";
    if (strAction.equals("A")) {
      lsIdNo = wp.itemStr("kk_id_no");
    } else {
      lsIdNo = wp.itemStr("id_no");
    }
    if (wfReadIdno(lsIdNo) != 1) {
      return -1;
    }
    if (wfReadAcno(idpseqno) != 1) {
      return -1;
    }
    return 1;
  }

  void listWkdata() throws Exception {
    wfReadIdno(wp.colStr("id_no"));
    wfReadAcno(wp.colStr("id_p_seqno"));
  }
}
