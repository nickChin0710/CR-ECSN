/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-06  V1.00.00  ANDY       program initial                            *
* 106-12-14            Andy		  update : program name : Crdi0140==>Crdq0140*
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package crdq01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdq0140 extends BaseProc {
  String lsCorpNo = "", lsCorpNoCode = "", lsKey = "", lsBegDate = "", lsEndDate = "", lsMsg = "";
  String dataKK1 = "", exIdPSeqno = "", exAcctType = "", exAcctKey = "", exPSeqno = "";
  Calendar cal = Calendar.getInstance();
  SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
  String lsCharge = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;
    Date date = format.parse(wp.sysDate);
    cal.setTime(date);
    cal.add(Calendar.MARCH, -1);
    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
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
    } else if (eqIgno(wp.buttonCode, "S1")) {
      /* crdi0130_q */
      strAction = "S1";
    }

    // dddw_select();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_corp_no_code", "0");
    wp.colSet("ex_beg_date", wp.sysDate);
    wp.colSet("ex_end_date", wp.sysDate);
  }

  @Override
  public void queryFunc() throws Exception {
    lsCorpNo = wp.itemStr("ex_corp_no");
    lsCorpNoCode = wp.itemStr("ex_corp_no_code");
    lsBegDate = wp.itemStr("ex_beg_date");
    lsEndDate = wp.itemStr("ex_end_date");
    lsCharge = wp.itemStr("ex_charge");
    String lsSql = "";
    // if (empty(ls_beg_date) && empty(ls_end_date)) {
    // err_alert("起訖日期不可為空白");
    // return;
    // }
    if (this.chkStrend(lsBegDate, lsEndDate) == false) {
      alertErr2("[起訖日期-起迄]  輸入錯誤");
      return;
    }
    // if (empty(ls_corp_no)) {
    // err_alert("請輸入法人統編!!");
    // return;
    // }
    if (!empty(lsCorpNo)) {
      lsSql = "select corp_p_seqno from  crd_corp where  1=1 ";
      lsSql += sqlCol(lsCorpNo, "corp_no");
      // ls_sql += sql_col(ls_corp_no_code,"corp_no_code"); //table crd_corp無corp_no_code欄位
      // System.out.println(" corp_p_seqno ls_sql : "+ls_sql);
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        alertErr2("無法人資料!!");
        return;
      } else {
        lsKey = sqlStr("corp_p_seqno");
      }
    }
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    // wp.pageControl();
    queryA();
    queryB();
    queryC();
    queryD();
    queryE();
    queryF();
    alertErr2(lsMsg);
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    // wp.setPageValue();
  }

  void queryA() throws Exception {
    daoTid = "A-";

    wp.selectSQL = "" + "uf_2ymd(mod_time) wk_mod_time, " + "mod_user, " + "mod_audcode, "
        + "corp_p_seqno, " + "corp_no, " + "abbr_name, " + "eng_name, " + "reg_zip, "
        + "reg_addr1, " + "reg_addr2, " + "reg_addr3, " + "reg_addr4, " + "reg_addr5, "
        + "max_rcv_fee_cnt, " + "mod_pgm, " + "assure_value, " + "charge_id_prev, "
        + "charge_name_prev, " + "charge_id, " + "charge_name ";
    wp.daoTable = "crd_corp_hist ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += sqlCol(lsKey, "corp_p_seqno");
    wp.whereStr += sqlStrend(lsBegDate, lsEndDate, "mod_time");
    if (lsCharge.equals("Y")) {
      wp.whereStr += "and ((charge_name_prev !='' and charge_id_prev !='') and "
          + "(charge_name_prev != charge_name or charge_id_prev !=charge_id)) ";
    }
    if (lsCharge.equals("N")) {
      wp.whereStr += "and (((charge_name_prev !='' and charge_id_prev !='') and "
          + "(charge_name_prev = charge_name and charge_id_prev=charge_id)) or "
          + "(charge_name_prev ='' and charge_id_prev='')) ";
    }

    wp.whereOrder = " order by mod_time, corp_p_seqno ";
    // System.out.println(" tab2 : select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();
    wp.setListCount(1);

    if (sqlNotFind()) {
      lsMsg += "無法人資料! ";
    }
    daoTid = "a-";
    listWkdataA();

  }

  void listWkdataA() {
    String modAudcode = "";
    int selCtA = wp.selectCnt;
    for (int ii = 0; ii < selCtA; ii++) {
      modAudcode = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(modAudcode, ",A,U,D", ",新增,修改,刪除"));

    }
  }

  void queryB() throws Exception {
    daoTid = "B-";

    wp.selectSQL = "" + "card_no, " + "id_p_seqno, " + "corp_p_seqno, " + "corp_no, "
        + "card_type, " + "urgent_flag, " + "group_code, " + "source_code, " + "sup_flag, "
        + "son_card_flag, " + "major_relation, " + "major_id_p_seqno, "
        + "nvl (uf_idno_id (major_id_p_seqno), '') major_id_no, " + "major_card_no, "
        + "member_id, " + "current_code, " + "force_flag, " + "eng_name, " + "reg_bank_no, "
        + "unit_code, " + "new_beg_date, " + "new_end_date, " + "issue_date, " + "emergent_flag, "
        + "oppost_reason, " + "oppost_date, " + "acct_type, " + "acno_p_seqno, " + "p_seqno, "
        + "stmt_cycle, " + "fee_code, " + "curr_fee_code, " + "indiv_crd_lmt, " + "indiv_inst_lmt, "
        + "crt_date, " + "crt_user, " + "mod_time_x, " + "mod_time, " + "mod_pgm, " + "mod_user, "
        + "mod_audcode";

    wp.daoTable = " crd_card_hist ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 ";
    wp.whereStr += sqlCol(lsKey, "corp_p_seqno") + " ) ";
    wp.whereStr += sqlStrend(lsBegDate, lsEndDate, "to_char(mod_time,'YYYYMMDD')");

    wp.whereOrder = " order by mod_time , acct_type , card_no ";

    // System.out.println(" tab2 : select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();
    wp.setListCount(2);
    if (sqlNotFind()) {
      lsMsg += "無卡片資料! ";
    }
    daoTid = "b-";
    listWkdataB();
  }

  void listWkdataB() {
    String wkData = "";
    int selCtB = wp.selectCnt;
    for (int ii = 0; ii < selCtB; ii++) {
      // query_b//
      wkData = wp.colStr(ii, daoTid + "sup_flag");
      wp.colSet(ii, daoTid + "sup_flag", commString.decode(wkData, ",0,1", ",正卡,附卡"));
      wkData = wp.colStr(ii, daoTid + "current_code");
      wp.colSet(ii, daoTid + "current_code",
          commString.decode(wkData, ",0,1,2,3,4,5", ",正常,申停,掛失,強停,其他,偽卡"));
    }
  }

  void queryC() throws Exception {
    daoTid = "C-";

    wp.selectSQL = "" + "mod_time, " + "uf_2ymd(mod_time) wk_mod_time, " + "mod_user, "
        + "mod_audcode, " + "acno_p_seqno, " + "acct_type, " + "acct_key, " + "corp_p_seqno, "
        + "corp_no, " + "corp_no_code, " + "risk_bank_no, " + "class_code, " + "vip_code, "
        + "bill_sending_zip, " + "bill_sending_addr1, " + "bill_sending_addr2, "
        + "bill_sending_addr3, " + "bill_sending_addr4, " + "bill_sending_addr5, "
        + "(bill_sending_zip||' '||bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5) wk_mail_addr, "
        + "mod_pgm ";
    wp.daoTable = "act_acno_hist ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr +=
        "and (acct_type,acct_key) in (select acct_type,acct_key from act_acno where 1=1 ";
    wp.whereStr += sqlCol(lsKey, "corp_p_seqno") + " ) ";
    wp.whereStr += sqlStrend(lsBegDate, lsEndDate, "mod_time");

    wp.whereOrder = " order by mod_time, acct_type";
    // System.out.println(" tab2 : select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();
    wp.setListCount(3);
    if (sqlNotFind()) {
      lsMsg += "無帳務資料一! ";
    }
    daoTid = "c-";
    listWkdataC();

  }

  void listWkdataC() {
    String wkData = "";
    int selCtc = wp.selectCnt;
    for (int ii = 0; ii < selCtc; ii++) {
      wkData = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));
      // query_c//
      wkData = wp.colStr(ii, daoTid + "acct_type");
      wp.colSet(ii, daoTid + "acct_type",
          commString.decode(wkData, ",01,02,03", ",01.一般卡,02.商務卡,03.外幣卡(加幣)"));

    }
  }

  void queryD() throws Exception {
    daoTid = "D-";

    wp.selectSQL = "" + "mod_time, " + "uf_2ymd(mod_time) wk_mod_time, " + "mod_user, "
        + "mod_audcode, " + "acno_p_seqno, " + "acct_type, " + "acct_key, "
        // + "acct_p_seqno, "
        + "corp_p_seqno, " + "corp_no, " + "corp_no_code, "
        + "(corp_no||'-'||corp_no_code) wk_corp_no, " + "autopay_acct_bank, " + "autopay_acct_no, "
        + "(autopay_acct_bank||'-'||autopay_acct_no) wk_autopay_act_no, " + "autopay_id, "
        + "autopay_id_code, " + "(autopay_id||'-'||autopay_id_code) wk_autopay_id,"
        + "autopay_indicator, " + "autopay_rate, " + "autopay_fix_amt, " + "min_pay_rate, "
        + "rc_use_indicator, " + "acct_holder_id, " + "acct_holder_id_code, "
        + "(acct_holder_id||'-'||acct_holder_id_code) wk_acct_holder_id," + "stmt_cycle, "
        + "id_p_seqno, " + "mod_pgm ";

    wp.daoTable = " act_acno_hist ";

    wp.whereStr = " where 1=1 ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr +=
        "and (acct_type,acct_key) in (select acct_type,acct_key from act_acno where 1=1 ";
    wp.whereStr += sqlCol(lsKey, "corp_p_seqno") + " ) ";
    wp.whereStr += sqlStrend(lsBegDate, lsEndDate, "mod_time");

    wp.whereOrder = " order by mod_time, acct_type";
    // System.out.println(" tab2 : select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();
    wp.setListCount(4);
    if (sqlNotFind()) {
      lsMsg += "無帳務資料二! ";
    }
    daoTid = "d-";
    // list_wkdata_a();
  }

  void queryE() throws Exception {
    daoTid = "E-";

    wp.selectSQL = "" + "mod_time, " + "uf_2ymd(mod_time) wk_mod_time, " + "mod_pgm, "
        + "mod_user, " + "mod_audcode, " + "p_seqno, " + "acct_type, " + "corp_p_seqno, "
        + "corp_no, " + "corp_no_code, " + "(corp_no||'-'||corp_no_code) wk_corp_no, "
        + "id_p_seqno, " + "UF_IDNO_ID(id_p_seqno) as acct_holder_id, "
        // + "acct_holder_id_code, "
        + "min_pay_bal, " + "m_code, " + "revolve_int_sign, " + "revolve_int_rate";

    wp.daoTable = "act_acct_mrk ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += "and p_seqno in (select p_seqno from act_acno where 1=1 ";
    wp.whereStr += sqlCol(lsKey, "corp_p_seqno") + " ) ";
    wp.whereStr += sqlStrend(lsBegDate, lsEndDate, "mod_time");

    wp.whereOrder = " order by mod_time, acct_type";
    // System.out.println(" tab2 : select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();
    wp.setListCount(5);
    if (sqlNotFind()) {
      lsMsg += "無帳務資料三! ";
    }
    daoTid = "e-";
    // list_wkdata_a();

  }

  void queryF() throws Exception {
    daoTid = "F-";

    wp.selectSQL = "" + "mod_time, " + "mod_user, " + "mod_pgm, " + "acct_date, " + "acct_type, "
        + "acct_key, " + "corp_no, " + "corp_no_code, "
        + "(corp_no||'-'||corp_no_code) wk_corp_no, " + "payment_rate1, " + "payment_rate2, "
        + "payment_rate3, " + "payment_rate4, " + "payment_rate5, " + "payment_rate6, "
        + "payment_rate7, " + "payment_rate8, " + "payment_rate9, " + "payment_rate10, "
        + "payment_rate11, " + "payment_rate12, " + "payment_rate13, " + "payment_rate14, "
        + "payment_rate15, " + "payment_rate16, " + "payment_rate17, " + "payment_rate18, "
        + "payment_rate19, " + "payment_rate20, " + "payment_rate21, " + "payment_rate22, "
        + "payment_rate23, " + "payment_rate24, " + "payment_rate25 ";

    wp.daoTable = " act_pay_record ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += "and p_seqno in (select p_seqno from act_acno where 1=1 ";
    wp.whereStr += sqlCol(lsKey, "corp_p_seqno") + " ) ";
    wp.whereStr += sqlCol(lsBegDate, "acct_date");

    wp.whereOrder = " order by acct_date , acct_type";
    // System.out.println(" tab2 : select " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageQuery();
    wp.setListCount(6);
    if (sqlNotFind()) {
      lsMsg += "無帳務資料四! ";
    }
    daoTid = "f-";
    // list_wkdata();

  }

  void listWkdata() {

  }

  @Override
  public void querySelect() throws Exception {
    dataKK1 = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }

  }

}
