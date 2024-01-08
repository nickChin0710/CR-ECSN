/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-09-12  V1.00.00  ryan       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
* 110-08-18  V1.00.04  Wilson     新增原住名姓名羅馬拼音欄位                                                                      *
******************************************************************************/
package crdp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Crdp0420 extends BaseProc {
  Crdp0420Func func;
  int rr = -1;
  String msg = "";
  String idPSeqno = "", crtDate = "";
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("ex_crt_dat1", wp.sysDate);
    wp.colSet("ex_crt_dat2", wp.sysDate);
  }

  @Override
  public void dddwSelect() {
    try {

      wp.initOption = "--";
      wp.optionKey = wp.colStr("account_style");
      dddwList("dddw_jcic_type", "crd_message", "msg_value", "msg",
          "where 1=1 and msg_type = 'JCIC_TYPE'");

      wp.initOption = "--";
      wp.optionKey = wp.colStr("education");
      dddwList("dddw_education", "crd_message", "msg_value", "msg",
          "where 1=1 and msg_type = 'EDUCATION'");

      wp.initOption = "--";
      wp.optionKey = wp.colStr("business_code");
      dddwList("dddw_bus_code", "crd_message", "msg_value", "msg",
          "where 1=1 and msg_type = 'BUS_CODE'");

    } catch (Exception ex) {
    }
  }

  // for query use only
  private int getWhereStr() throws Exception {
    String lsDate1 = wp.itemStr("ex_crt_dat1");
    String lsDate2 = wp.itemStr("ex_crt_dat2");

    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[起迄日期-起迄]  輸入錯誤");
      return -1;
    }
    wp.whereStr = " where 1=1 and a.to_jcic_date ='' and a.apr_user ='' and a.apr_date = ''";

    if (empty(wp.itemStr("ex_crt_dat1")) == false) {
      wp.whereStr += " and a.crt_date >= :ex_crt_dat1 ";
      setString("ex_crt_dat1", wp.itemStr("ex_crt_dat1"));
    }
    if (empty(wp.itemStr("ex_crt_dat2")) == false) {
      wp.whereStr += " and a.crt_date <= :ex_crt_dat2 ";
      setString("ex_crt_dat2", wp.itemStr("ex_crt_dat2"));
    }
    if (empty(wp.itemStr("ex_crt_user")) == false) {
      wp.whereStr += " and a.crt_user = :ex_crt_user ";
      setString("ex_crt_user", wp.itemStr("ex_crt_user"));
    }
    return 1;
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

    wp.selectSQL = "hex(a.rowid) as rowid, " + " a.crt_date, " + " a.crt_user, " + " a.trans_type, "
        + " a.birthday, " + " a.chi_name, " + " a.mod_seqno, " + " a.update_date, "
        + " a.id_p_seqno, " + " b.id_no ";
    wp.daoTable = " crd_jcic_idno as a left join crd_idno as b on a.id_p_seqno = b.id_p_seqno ";

    wp.whereOrder = "";
    if (getWhereStr() != 1)
      return;
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    // wp.col_set("exCnt", int_2Str(wp.selectCnt));
    listWkdata();

  }

  void listWkdata() throws Exception {
    String transType = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {

      transType = wp.colStr(ii, "trans_type");
      wp.colSet(ii, "tt_trans_type", commString.decode(transType, ",A,C,D", ",A:新增,C:修改,D:刪除"));

    }
  }

  void listWkdata2() {

    if (empty(wp.colStr("id_no"))) {
      wp.colSet("id_no", "");
    }
    if (!wp.colStr("db_resident_addr").equals(wp.colStr("resident_addr"))) {
      wp.colSet("resident_addr_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_mail_addr").equals(wp.colStr("mail_addr"))) {
      wp.colSet("mail_addr_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_tel_no").equals(wp.colStr("tel_no"))) {
      wp.colSet("tel_no_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_office_tel_no").equals(wp.colStr("office_tel_no"))) {
      wp.colSet("office_tel_no_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_cellar_phone").equals(wp.colStr("cellar_phone"))) {
      wp.colSet("cellar_phone_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_other_cntry_code").equals(wp.colStr("cntry_code"))) {
      wp.colSet("cntry_code_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_passport_date").equals(wp.colStr("passport_date"))) {
      wp.colSet("passport_date_color", "style=\"background-color:#00DDDD\"");
    }
    if (!wp.colStr("db_passport_no").equals(wp.colStr("passport_no"))) {
      wp.colSet("passport_no_color", "style=\"background-color:#00DDDD\"");
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(itemKk("data_k1")) == false) {
      idPSeqno = itemKk("data_k1");
    }

    if (empty(itemKk("data_k2")) == false) {
      crtDate = itemKk("data_k2");
    }

    wp.selectSQL = "hex(a.rowid) as rowid2, a.mod_seqno, " + " a.crt_date, " + " a.trans_type, "
        + " a.account_style, " + " UF_IDNO_ID(a.id_p_seqno) as id_no, " + " a.chi_name, "
        + " a.eng_name, " + " a.birthday, " + " a.education, " + " a.sex, " + " a.cntry_code, "
        + " a.passport_no, " + " a.passport_date, " + " a.mail_zip, " + " a.mail_addr, "
        + " a.resident_addr, " + " a.resident_flag, " + " a.cellar_phone, " + " a.tel_no, "
        + " a.company_name, " + " a.business_id, " + " a.job_position, " + " a.office_tel_no, "
        + " a.salary, " + " a.service_year, " + " a.update_date, " + " a.business_code, "
        + " a.crt_user, " + " a.to_jcic_date, " + " a.indigenous_name, "
        + " rtrim(b.resident_addr1)||rtrim(b.resident_addr2)||rtrim(b.resident_addr3)||rtrim(b.resident_addr4)||rtrim(b.resident_addr5) as db_resident_addr, "
        + " rtrim(d.bill_sending_addr1)||rtrim(d.bill_sending_addr2)||rtrim(d.bill_sending_addr3)||rtrim(d.bill_sending_addr4)||rtrim(d.bill_sending_addr5) as db_mail_addr, "
        + " rtrim(b.home_area_code1)||rtrim(b.home_tel_no1)||rtrim(b.home_tel_ext1) as db_tel_no, "
        + " rtrim(b.office_area_code1)||rtrim(b.office_tel_no1)||rtrim(b.office_tel_ext1) as db_office_tel_no, "
        + " rtrim(b.cellar_phone) as db_cellar_phone, " + " rtrim(b.sex) as db_sex, "
        + " rtrim(b.other_cntry_code) as db_other_cntry_code, "
        + " rtrim(b.passport_date) as db_passport_date, "
        + " rtrim(b.passport_no) as db_passport_no ";
    wp.daoTable = " crd_jcic_idno as a join crd_idno as b on a.id_p_seqno = b.id_p_seqno "
        + " join crd_card as c on b.id_p_seqno = c.id_p_seqno "
        + " join act_acno as d on c.acno_p_seqno = d.acno_p_seqno ";

    wp.whereStr = " where 1=1 ";


    if (empty(idPSeqno) == false) {
      wp.whereStr += " and  a.id_p_seqno = :id_p_seqno ";
      setString("id_p_seqno", idPSeqno);
    }
    if (empty(crtDate) == false) {
      wp.whereStr += " and  a.crt_date = :crt_date ";
      setString("crt_date", crtDate);
    }

    pageSelect();
    dddwSelect();
    listWkdata2();
    if (sqlNotFind()) {
      alertErr("此卡號之明細資料不存在");
      return;
    }

  }

  @Override
  public void dataProcess() throws Exception {
    // -check approve-
    /*
     * if (!check_approve(wp.item_ss("approval_user"), wp.item_ss("approval_passwd"))) { return; }
     */
    func = new Crdp0420Func(wp);

    String[] aaRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    String[] aaModSeqno = wp.itemBuff("mod_seqno");
    String[] aaIdPSeqno = wp.itemBuff("id_p_seqno");
    String[] aaCrtDate = wp.itemBuff("crt_date");

    wp.listCount[0] = aaIdPSeqno.length;

    // -update-
    for (rr = 0; rr < aaIdPSeqno.length; rr++) {
      if (!checkBoxOptOn(rr, opt)) {
        continue;
      }
      func.varsSet("aa_rowid", aaRowid[rr]);
      func.varsSet("aa_mod_seqno", aaModSeqno[rr]);
      func.varsSet("aa_id_p_seqno", aaIdPSeqno[rr]);
      func.varsSet("aa_crt_date", aaCrtDate[rr]);
      rc = func.dataProc();
      sqlCommit(rc);
      if (rc != 1) {
        wp.colSet(rr, "ok_flag", "!");
        ilErr++;
        continue;
      }
      wp.colSet(rr, "ok_flag", "V");
      rc = func.dbDelete();
      sqlCommit(rc);
      ilOk++;
    }
    // queryFunc();
    alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
  }


  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
