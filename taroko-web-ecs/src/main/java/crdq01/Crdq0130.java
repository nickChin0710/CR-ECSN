/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-31  V1.00.00  ryan       program initial                            *
* 106-12-14            Andy		  update : program name : Crdi0130==>Crdq0130*
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 109-05-06  V1.00.02  shiyuqi      updated for project coding standard      * 
  109-07-17  V1.00.03   shiyuqi        rename tableName &FiledName   
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package crdq01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Crdq0130 extends BaseProc {
  String lsBegDate = "", lsEndDate = "", msg = "";
  String dataKK1 = "", exIdPseqno = "", exAcctType = "", exAcctKey = "", exAcnoPSeqno = "",
      exPSeqno = "";
  Calendar cal = Calendar.getInstance();
  SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
  int totalCnt = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;
    Date date = format.parse(wp.sysDate);
    cal.setTime(date);
    cal.add(Calendar.MARCH, -1);
    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
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
      actAcnoQ();
    }

    // dddw_select();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_beg_date", format.format(cal.getTime()));
    wp.colSet("ex_end_date", wp.sysDate);
    wp.colSet("ex_id_code", "0");
  }

  @Override
  public void queryFunc() throws Exception {
    lsBegDate = wp.itemStr("ex_beg_date");
    lsEndDate = wp.itemStr("ex_end_date");
    String lsId = "";// For ID 總歸戶信用額度
    String lsKey1 = "", lsKey2 = "";
    if (empty(lsBegDate) || empty(lsEndDate)) {
      alertErr2("起訖日期不可為空白");
      return;
    }
    if (this.chkStrend(lsBegDate, lsEndDate) == false) {
      alertErr2("[起訖日期-起迄]  輸入錯誤");
      return;
    }
    if (empty(wp.itemStr("ex_id")) && empty(wp.itemStr("ex_card_no"))) {
      alertErr2("請輸入查詢值");
      return;
    }
    if (!empty(wp.itemStr("ex_id"))) {
      lsKey1 = wp.itemStr("ex_id");
      lsKey2 = wp.itemStr("ex_id_code");

      String sql1 =
          "select id_p_seqno from crd_idno where id_no = :ls_key1 and id_no_code = :ls_key2";
      setString("ls_key1", lsKey1);
      setString("ls_key2", lsKey2);
      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2("無此身分證號資料");
        return;
      }
      exIdPseqno = sqlStr("id_p_seqno");
      // wp.col_set("ex_id_p_seqno", ls_value1);

      sql1 = "select acno_p_seqno,acct_key from act_acno where id_p_seqno = :ex_id_p_seqno ";
      setString("ex_id_p_seqno", exIdPseqno);
      sqlSelect(sql1);
      exAcnoPSeqno = sqlStr("acno_p_seqno");
      exPSeqno = sqlStr("acno_p_seqno");
      exAcctKey = sqlStr("acct_key");
      lsId = exIdPseqno;
      // -AA- For ID 總歸戶信用額度 -AA
      // ls_id = wp.item_ss("ex_id");

      // -VV- For ID 總歸戶信用額度 -VV
    }
    if (!empty(wp.itemStr("ex_card_no")) && empty(exIdPseqno)) {
      lsKey1 = wp.itemStr("ex_card_no");
      String sql2 = "select id_p_seqno" + ",UF_IDNO_ID(id_p_seqno) as id_no" + ",acct_type"
          + ",UF_ACNO_KEY(acno_p_seqno) as acct_key" + ",acno_p_seqno" + ",p_seqno"
          + ",major_id_p_seqno" + ",UF_IDNO_ID(major_id_p_seqno) as major_id " + "from crd_card "
          + "where card_no = :ls_key1 ";
      setString("ls_key1", lsKey1);
      sqlSelect(sql2);
      if (sqlRowNum <= 0) {
        alertErr2("無此卡號資料");
        return;
      }
      // wp.col_set("ex_id_p_seqno", sql_ss("id_p_seqno"));
      exIdPseqno = sqlStr("id_p_seqno");
      // wp.col_set("ex_acct_type", sql_ss("acct_type"));
      exAcctType = sqlStr("acct_type");
      wp.colSet("ex_acct_key", sqlStr("acct_key"));
      exAcctKey = sqlStr("acct_key");
      // wp.col_set("ex_p_seqno", sql_ss("p_seqno"));
      exAcnoPSeqno = sqlStr("acno_p_seqno");
      exPSeqno = sqlStr("p_seqno");
      // wp.col_set("ex_major_id_p_seqno", sql_ss("major_id_p_seqno"));
      // wp.col_set("ex_major_id", sql_ss("major_id"));
      lsId = sqlStr("major_id_p_seqno");
    }
    // -AA- For ID 總歸戶信用額度 -AA
    String sql3 =
        " select sum( distinct a.line_of_credit_amt) as line_of_credit_amt from  act_acno as a , crd_card as c  "
            + " where  ( a.acno_p_seqno = c.acno_p_seqno ) and  c.oppost_date ='' "
            + " and ( c.id_p_seqno = :ls_id1  and a.card_indicator='1') "
            + " or (  c.id_p_seqno = :ls_id2 and a.card_indicator='2' and a.corp_act_flag <>'Y') ";
    setString("ls_id1", lsId);
    setString("ls_id2", lsId);
    sqlSelect(sql3);
    wp.colSet("ex_tot_line_of_credit_amt", sqlStr("line_of_credit_amt"));
    wp.colSet("ex_acct_key", sqlStr("acct_key"));

    // //-page control-
    // wp.queryWhere = wp.whereStr;
    // wp.setQueryMode();

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
    queryG();
    queryH();
    queryI();
    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    // wp.setPageValue();
  }

  void queryA() throws Exception {
    daoTid = "A-";

    wp.selectSQL = " mod_time" + " ,mod_user" + " ,id_p_seqno" + " ,id_no" + " ,id_no_code"
        + " ,chi_name" + " ,sex" + " ,birthday" + " ,asset_value" + " ,e_mail_addr"
        + " ,mod_audcode" + " ,office_area_code1" + " ,office_tel_no1" + " ,office_tel_ext1"
        + " ,office_area_code2" + " ,office_tel_no2" + " ,office_tel_ext2" + " ,home_area_code1"
        + " ,home_tel_no1" + " ,home_tel_ext1" + " ,home_area_code2" + " ,home_tel_no2"
        + " ,home_tel_ext2" + " ,cellar_phone" + " ,special_code" + " ,id_p_seqno";

    wp.daoTable = "crd_idno_hist ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and id_p_seqno = :ex_id_p_seqno ";
    setString("ex_id_p_seqno", exIdPseqno);
    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and to_char(mod_time,'YYYYMMDD') >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and to_char(mod_time,'YYYYMMDD') <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = " order by mod_time, id_p_seqno";

    pageQuery();
    wp.setListCount(1);
    totalCnt = wp.selectCnt;
    if (sqlNotFind()) {

    }
    daoTid = "a-";
    listWkdataA();

  }

  void queryB() throws Exception {
    daoTid = "B-";

    wp.selectSQL = " a.mod_user" + " ,a.card_no" + " ,a.corp_no" + " ,a.card_type"
        + " ,a.group_code" + " ,a.source_code" + " ,a.sup_flag" + " ,a.son_card_flag"
        + " ,b.id_no as major_id" + " ,b.id_no_code as major_id_code" + " ,a.major_card_no"
        + " ,a.eng_name" + " ,a.indiv_crd_lmt" + " ,a.indiv_inst_lmt" + " ,a.current_code"
        + " ,a.acct_type" + " ,a.mod_audcode" + " ,a.acno_p_seqno" + " ,a.mod_time" + " ,a.fee_code"
        + " ,a.id_p_seqno";

    wp.daoTable =
        " crd_card_hist as a left join crd_idno as b on a.major_id_p_seqno = b.id_p_seqno ";

    wp.whereStr = " where 1=1 ";
    // wp.whereStr += " and a.id_p_seqno = :ex_id_p_seqno ";
    // setString("ex_id_p_seqno",ex_id_p_seqno);
    if (!empty(wp.itemStr("ex_card_no")) || !empty(wp.itemStr("ex_id"))) {
      wp.whereStr += " and acno_p_seqno in (select acno_p_seqno from crd_card as c where 1=1 ";
      if (!empty(wp.itemStr("ex_card_no"))) {
        wp.whereStr += " and c.card_no = :ex_card_no ";
        wp.whereStr += " and a.card_type = c.card_type ";
        wp.whereStr += " and a.group_code = c.group_code ";
        setString("ex_card_no", wp.itemStr("ex_card_no"));
      }
      if (!empty(wp.itemStr("ex_id"))) {
        wp.whereStr += " and c.id_p_seqno = :ex_id_p_seqno ";
        setString("ex_id_p_seqno", exIdPseqno);
      }
      wp.whereStr += ")";
    }
    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and to_char(a.mod_time,'YYYYMMDD') >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and to_char(a.mod_time,'YYYYMMDD') <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = " order by a.mod_time, a.acct_type, a.card_no";

    pageQuery();
    wp.setListCount(2);
    totalCnt += wp.selectCnt;
    if (sqlNotFind()) {

    }
    daoTid = "b-";
    listWkdataB();
  }

  void queryC() throws Exception {
    daoTid = "C-";

    wp.selectSQL = " mod_time" + " ,mod_user" + " ,mod_audcode" + " ,acno_p_seqno" + " ,acct_type"
        + " ,acct_key" + " ,corp_p_seqno" + " ,corp_no" + " ,corp_no_code" + " ,risk_bank_no"
        + " ,class_code" + " ,vip_code" + " ,bill_sending_zip" + " ,bill_sending_addr1"
        + " ,bill_sending_addr2" + " ,bill_sending_addr3" + " ,bill_sending_addr4"
        + " ,bill_sending_addr5";

    wp.daoTable = "act_acno_hist ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and id_p_seqno = :ex_id_p_seqno ";
    setString("ex_id_p_seqno", exIdPseqno);
    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and mod_time >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and mod_time <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = " order by mod_time, acct_type";

    pageQuery();
    wp.setListCount(3);
    totalCnt += wp.selectCnt;
    if (sqlNotFind()) {

    }
    daoTid = "c-";
    listWkdataC();

  }

  void queryD() throws Exception {
    daoTid = "D-";

    wp.selectSQL = " a.mod_time" + " ,a.mod_user" + " ,a.mod_audcode" + " ,a.rela_type"
        + " ,a.acno_p_seqno" + " ,a.rela_id" + " ,a.card_no" + " ,a.sex" + " ,b.id_no"
        + " ,b.id_no_code" + " ,a.birthday" + " ,a.id_p_seqno" + " ,a.acct_type" + " ,a.rela_name"
        + " ,b.chi_name" + " ,a.home_area_code1" + " ,a.home_tel_no1" + " ,a.home_tel_ext1"
        + " ,a.office_area_code1" + " ,a.office_tel_no1" + " ,a.office_tel_ext1"
        + " ,a.office_area_code2" + " ,a.office_tel_no2" + " ,a.office_tel_ext2"
        + " ,a.home_area_code2" + " ,a.home_tel_no2" + " ,a.home_tel_ext2" + " ,a.cellar_phone"
        + " ,a.start_date" + " ,a.end_date";

    wp.daoTable = " crd_rela_hist as a left join crd_idno as b on a.id_p_seqno = b.id_p_seqno ";

    wp.whereStr = " where 1=1 ";

    if (!empty(exAcnoPSeqno)) {
      wp.whereStr += " and a.acno_p_seqno = :ex_acno_p_seqno ";
      setString("ex_acno_p_seqno", exAcnoPSeqno);
    } else {
      wp.whereStr += " and a.id_p_seqno = :ex_id_p_seqno ";
      setString("ex_id_p_seqno", exIdPseqno);

    }
    wp.whereStr += " and a.rela_type='1' ";

    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and to_char(a.mod_time,'YYYYMMDD') >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and to_char(a.mod_time,'YYYYMMDD') <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = " order by a.mod_time, a.acct_type ";
    pageQuery();
    wp.setListCount(4);
    totalCnt += wp.selectCnt;
    if (sqlNotFind()) {

    }
    daoTid = "d-";
    listWkdataD();
  }

  void queryE() throws Exception {
    daoTid = "E-";

    wp.selectSQL = " mod_time" + " ,mod_user" + " ,mod_audcode" + " ,acno_p_seqno" + " ,acct_type"
        + " ,acct_key" + " ,corp_p_seqno" + " ,corp_no" + " ,corp_no_code" + " ,autopay_acct_bank"
        + " ,autopay_acct_no" + " ,autopay_id" + " ,autopay_id_code" + " ,autopay_indicator"
        + " ,autopay_rate" + " ,autopay_fix_amt" + " ,min_pay_rate" + " ,rc_use_indicator"
        + " ,acct_holder_id" + " ,acct_holder_id_code" + " ,stmt_cycle" + " ,id_p_seqno"
        + " ,stat_send_paper" + " ,stat_send_internet" + " ,stat_send_fax" + " ,stat_send_s_month"
        + " ,stat_send_e_month" + " ,stat_send_s_month2" + " ,stat_send_e_month2"
        + " ,internet_upd_user" + " ,internet_upd_date" + " ,paper_upd_user" + " ,paper_upd_date";

    wp.daoTable = "act_acno_hist ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and id_p_seqno = :ex_id_p_seqno ";
    setString("ex_id_p_seqno", exIdPseqno);
    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and mod_time >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and mod_time <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = " order by mod_time, acct_type";

    pageQuery();
    wp.setListCount(5);
    totalCnt += wp.selectCnt;
    if (sqlNotFind()) {

    }
    daoTid = "e-";
    listWkdataE();

  }

  void queryF() throws Exception {
    daoTid = "F-";

    wp.selectSQL = " mod_time" + " ,mod_pgm" + " ,mod_user" + " ,mod_audcode" + " ,p_seqno"
    // + " ,acct_type"
    // + " ,acct_key"
        + " ,corp_p_seqno" + " ,corp_no" + " ,corp_no_code" + " ,id_p_seqno"
        + " ,UF_IDNO_ID(id_p_seqno) as acct_holder_id" + " ,min_pay_bal" + " ,m_code"
        + " ,revolve_int_sign" + " ,revolve_int_rate";

    wp.daoTable = " act_acct_mrk ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and id_p_seqno = :ex_id_p_seqno ";
    setString("ex_id_p_seqno", exIdPseqno);
    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and mod_time >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and mod_time <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = " order by mod_time, acct_type";

    pageQuery();

    wp.setListCount(6);
    totalCnt += wp.selectCnt;
    if (sqlNotFind()) {

    }
    daoTid = "f-";
    listWkdataF();

  }

  void queryG() throws Exception {
    daoTid = "G-";

    wp.selectSQL =
        " acct_date" + " ,acct_type" + " ,acct_key" + " ,corp_no" + " ,corp_no_code" + " ,mod_user"
            + " ,mod_time" + " ,mod_pgm" + " ,payment_rate1" + " ,payment_rate2" + " ,payment_rate3"
            + " ,payment_rate4" + " ,payment_rate5" + " ,payment_rate6" + " ,payment_rate7"
            + " ,payment_rate8" + " ,payment_rate9" + " ,payment_rate10" + " ,payment_rate11"
            + " ,payment_rate12" + " ,payment_rate13" + " ,payment_rate14" + " ,payment_rate15"
            + " ,payment_rate16" + " ,payment_rate17" + " ,payment_rate18" + " ,payment_rate19"
            + " ,payment_rate20" + " ,payment_rate21" + " ,payment_rate22" + " ,payment_rate23"
            + " ,payment_rate24" + " ,payment_rate25" + " ,p_seqno" + " ,id_p_seqno";

    wp.daoTable = " act_pay_record ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and p_seqno = :ex_p_seqno ";
    setString("ex_p_seqno", exPSeqno);

    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and to_char(mod_time,'YYYYMMDD') >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and to_char(mod_time,'YYYYMMDD') <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = " order by acct_date, acct_type";

    pageQuery();

    wp.setListCount(7);
    totalCnt += wp.selectCnt;
    if (sqlNotFind()) {

    }
    daoTid = "g-";
    listWkdataG();

  }

  void queryH() throws Exception {
    daoTid = "H-";

    wp.selectSQL = " UF_IDNO_ID(a.id_p_seqno) as id" + " ,a.market_agree_base"
        + " ,a.market_agree_act" + " ,a.bank_securit_flag" + " ,a.bank_prod_insur_flag"
        + " ,a.bank_bills_flag" + " ,a.bank_life_insur_flag" + " ,a.bank_invest_flag"
        + " ,a.bank_asset_flag" + " ,a.bank_venture_flag" + " ,a.mod_time" + " ,a.mod_user"
        + " ,b.chi_name" + " ,b.id_p_seqno";

    wp.daoTable =
        " crd_ibm_market_log as a left join crd_idno as b on a.id_p_seqno = b.id_p_seqno ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and a.id_p_seqno = :ex_id_p_seqno ";
    setString("ex_id_p_seqno", exIdPseqno);

    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and to_char(a.mod_time,'YYYYMMDD') >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and to_char(a.mod_time,'YYYYMMDD') <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = "";

    pageQuery();
    wp.setListCount(8);
    wp.notFound = "N";
    totalCnt += wp.selectCnt;

    daoTid = "h-";
    // list_wkdata();

  }

  void queryI() throws Exception {
    daoTid = "I-";

    wp.selectSQL = " mod_his_audcode," + " mod_user," + " mod_time," + " mod_pgm,"
        + " autopay_acct_bank," + " autopay_acct_no," + " autopay_id," + " autopay_id_code,"
        + " autopay_indicator," + " autopay_dc_flag," + " autopay_dc_indicator";

    wp.daoTable = " act_acct_curr_his ";

    wp.whereStr = " where 1=1 ";
    wp.whereStr += " and curr_code <> '901' ";
    wp.whereStr += " and p_seqno = :ex_p_seqno ";
    setString("ex_p_seqno", exPSeqno);

    if (!empty(lsBegDate) && !empty(lsEndDate)) {
      wp.whereStr += " and to_char(mod_time,'YYYYMMDD') >= :ls_beg_date ";
      setString("ls_beg_date", lsBegDate);
      wp.whereStr += " and to_char(mod_time,'YYYYMMDD') <= :ls_end_date ";
      setString("ls_end_date", lsEndDate);
    }

    wp.whereOrder = "";

    pageQuery();
    wp.setListCount(9);
    wp.notFound = "N";
    totalCnt += wp.selectCnt;
    if (totalCnt == 0) {
      wp.notFound = "Y";
      alertErr(appMsg.errCondNodata);
      return;
    }
    daoTid = "i-";
    listWkdataI();
  }

  void actAcnoQ() throws Exception {
    this.selectNoLimit();
    String asIdPSeqnoKk = "";

    asIdPSeqnoKk = itemKk("data_k1");
    wp.selectSQL =
        "hex(rowid) as rowid " + ", acct_type" + ", acct_key" + ", id_p_seqno" + ", acno_p_seqno";
    wp.daoTable = "act_acno";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  id_p_seqno = :as_id_p_seqno and card_indicator <> '2' ";
    setString("as_id_p_seqno", asIdPSeqnoKk);
    // pageSelect();
    pageQuery();
    listWkdata();
    wp.setListCount(1);
    wp.notFound = "";
  }

  void listWkdata() {
    String modAudcode = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      modAudcode = wp.colStr(ii, "mod_audcode");
      wp.colSet(ii, "mod_audcode", commString.decode(modAudcode, ",A,U,D", ",新增,修改,刪除"));

    }
  }

  void listWkdataA() {
    String wkData = "", wkOfficeTel1 = "", wkOfficeTel2 = "", wkHomeTel1 = "", wkHomeTel2 = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkOfficeTel1 = "";
      wkOfficeTel2 = "";
      wkHomeTel1 = "";
      wkHomeTel2 = "";
      wkData = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));
      wkData = wp.colStr(ii, daoTid + "office_area_code1");
      if (!empty(wkData)) {
        wkOfficeTel1 += "(" + wkData + ")";
      }
      wkOfficeTel1 += wp.colStr(ii, daoTid + "office_tel_no1");
      wkData = wp.colStr(ii, daoTid + "office_tel_ext1");
      if (!empty(wkData)) {
        wkOfficeTel1 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_office_tel1", wkOfficeTel1);

      wkData = wp.colStr(ii, daoTid + "office_area_code2");
      if (!empty(wkData)) {
        wkOfficeTel2 += "(" + wkData + ")";
      }
      wkOfficeTel2 += wp.colStr(ii, daoTid + "office_tel_no2");
      wkData = wp.colStr(ii, daoTid + "office_tel_ext2");
      if (!empty(wkData)) {
        wkOfficeTel2 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_office_tel2", wkOfficeTel2);

      wkData = wp.colStr(ii, daoTid + "home_area_code1");
      if (!empty(wkData)) {
        wkHomeTel1 += "(" + wkData + ")";
      }
      wkHomeTel1 += wp.colStr(ii, daoTid + "home_tel_no1");
      wkData = wp.colStr(ii, daoTid + "home_tel_ext1");
      if (!empty(wkData)) {
        wkHomeTel1 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_home_tel1", wkHomeTel1);

      wkData = wp.colStr(ii, daoTid + "home_area_code2");
      if (!empty(wkData)) {
        wkHomeTel2 += "(" + wkData + ")";
      }
      wkHomeTel2 += wp.colStr(ii, daoTid + "home_tel_no2");
      wkData = wp.colStr(ii, daoTid + "home_tel_ext2");
      if (!empty(wkData)) {
        wkHomeTel2 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_home_tel2", wkHomeTel2);

      wkData = wp.colStr(ii, daoTid + "id_no");
      if (!empty(wkData)) {
        wp.colSet(ii, daoTid + "wk_id_no", wkData + "-" + wp.colStr(ii, daoTid + "id_no_code"));
      }
    }
  }

  void listWkdataB() {
    String wkData = "";
    int wkIndiInstLmt = 0;

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));
      // query_b//
      wkData = wp.colStr(ii, daoTid + "sup_flag");
      wp.colSet(ii, daoTid + "sup_flag", commString.decode(wkData, ",0,1", ",正卡,附卡"));
      wkData = wp.colStr(ii, daoTid + "sup_flag");
      wp.colSet(ii, daoTid + "sup_flag", commString.decode(wkData, ",0,1,2,3,4,5", ",正常,申停,掛失,強停,其他,偽卡"));
      wkIndiInstLmt =
          (int) (wp.colNum(ii, "indiv_crd_lmt") * wp.colNum(ii, "indiv_inst_lmt") / 100);
      wp.colSet(ii, daoTid + "wk_indi_inst_lmt", wkIndiInstLmt + "");

      wkData = wp.colStr(ii, daoTid + "major_id");
      if (!empty(wkData)) {
        wp.colSet(ii, daoTid + "wk_major_id", wkData + "-" + wp.colStr(ii, daoTid + "major_id_code"));
      }
    }
  }

  void listWkdataC() {
    String wkData = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));
      // query_c//
      wkData = wp.colStr(ii, daoTid + "acct_type");
      wp.colSet(ii, daoTid + "acct_type",
          commString.decode(wkData, ",01,02,03", ",01.一般卡,02.商務卡,03.外幣卡(加幣)"));


    }
  }

  void listWkdataD() {
    String wkData = "", wkOfficeTel1 = "", wkOfficeTel2 = "", wkHomeTel1 = "", wkHomeTel2 = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkOfficeTel1 = "";
      wkOfficeTel2 = "";
      wkHomeTel1 = "";
      wkHomeTel2 = "";
      wkData = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));
      wkData = wp.colStr(ii, daoTid + "office_area_code1");
      if (!empty(wkData)) {
        wkOfficeTel1 += "(" + wkData + ")";
      }
      wkOfficeTel1 += wp.colStr(ii, daoTid + "office_tel_no1");
      wkData = wp.colStr(ii, daoTid + "office_tel_ext1");
      if (!empty(wkData)) {
        wkOfficeTel1 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_office_tel1", wkOfficeTel1);

      wkData = wp.colStr(ii, daoTid + "office_area_code2");
      if (!empty(wkData)) {
        wkOfficeTel2 += "(" + wkData + ")";
      }
      wkOfficeTel2 += wp.colStr(ii, daoTid + "office_tel_no2");
      wkData = wp.colStr(ii, daoTid + "office_tel_ext2");
      if (!empty(wkData)) {
        wkOfficeTel2 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_office_tel2", wkOfficeTel2);

      wkData = wp.colStr(ii, daoTid + "home_area_code1");
      if (!empty(wkData)) {
        wkHomeTel1 += "(" + wkData + ")";
      }
      wkHomeTel1 += wp.colStr(ii, daoTid + "home_tel_no1");
      wkData = wp.colStr(ii, daoTid + "home_tel_ext1");
      if (!empty(wkData)) {
        wkHomeTel1 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_home_tel1", wkHomeTel1);

      wkData = wp.colStr(ii, daoTid + "home_area_code2");
      if (!empty(wkData)) {
        wkHomeTel2 += "(" + wkData + ")";
      }
      wkHomeTel2 += wp.colStr(ii, daoTid + "home_tel_no2");
      wkData = wp.colStr(ii, daoTid + "home_tel_ext2");
      if (!empty(wkData)) {
        wkHomeTel2 += " ext:" + wkData;
      }
      wp.colSet(ii, daoTid + "wk_home_tel2", wkHomeTel2);

      wkData = wp.colStr(ii, daoTid + "id_no");
      if (!empty(wkData)) {
        wp.colSet(ii, daoTid + "wk_id_no", wkData + "-" + wp.colStr(ii, daoTid + "id_no_code"));
      }
    }
  }

  void listWkdataE() {
    String wkData = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));
      // query_c,query_e//
      wkData = wp.colStr(ii, daoTid + "acct_type");
      wp.colSet(ii, daoTid + "acct_type",
          commString.decode(wkData, ",01,02,03", ",01.一般卡,02.商務卡,03.外幣卡(加幣)"));
      // query_e//
      wkData = wp.colStr(ii, daoTid + "acct_type");
      wp.colSet(ii, daoTid + "acct_type", commString.decode(wkData, ",1,2,3", ",1.正常允用,2.例外允用,3.不得使用RC"));

      wkData = wp.colStr(ii, daoTid + "corp_no");
      if (!empty(wkData)) {
        wp.colSet(ii, daoTid + "wk_corp_no", wkData + "-" + wp.colStr(ii, daoTid + "orp_no_code"));
      }

      wkData = wp.colStr(ii, daoTid + "autopay_id");
      if (!empty(wkData)) {
        wp.colSet(ii, daoTid + "wk_autopay_id",
            wkData + "-" + wp.colStr(ii, daoTid + "autopay_id_code"));
      }

      wkData = wp.colStr(ii, daoTid + "acct_holder_id");
      if (!empty(wkData)) {
        wp.colSet(ii, daoTid + "wk_acct_holder_id",
            wkData + "-" + wp.colStr(ii, daoTid + "acct_holder_id_code"));
      }
    }
  }

  void listWkdataF() {
    String wkData = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, daoTid + "mod_audcode");
      wp.colSet(ii, daoTid + "mod_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));
      wkData = wp.colStr(ii, daoTid + "corp_no");
      if (!empty(wkData)) {
        wp.colSet(ii, daoTid + "wk_corp_no", wkData + "-" + wp.colStr(ii, daoTid + "orp_no_code"));
      }
    }
  }

  void listWkdataG() {
    String corpNo = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      corpNo = wp.colStr(ii, daoTid + "corp_no");
      if (!empty(corpNo)) {
        wp.colSet(ii, daoTid + "wk_corp_no", corpNo + "-" + wp.colStr(ii, daoTid + "orp_no_code"));
      }
    }
  }

  void listWkdataI() {
    String wkData = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, daoTid + "mod_his_audcode");
      wp.colSet(ii, daoTid + "mod_his_audcode", commString.decode(wkData, ",A,U,D", ",新增,修改,刪除"));

      wkData = wp.colStr(ii, daoTid + "autopay_indicator");
      wp.colSet(ii, daoTid + "autopay_indicator", commString.decode(wkData, ",1,2", ",扣TTL,扣MP"));

      wkData = wp.colStr(ii, daoTid + "autopay_dc_indicator");
      wp.colSet(ii, daoTid + "autopay_dc_indicator", commString.decode(wkData, ",1,2", ",扣TTL,扣MP"));

    }
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
