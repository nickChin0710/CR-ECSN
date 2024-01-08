/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-25  V1.00.01  ryan      program initial                             *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
* 110-08-17  V1.00.04  Wilson     新增原住名姓名羅馬拼音欄位                                                                      * 
* 110-08-24  V1.00.05  Wilson     mark 本國人不可有國籍                                                                           *
******************************************************************************/
package crdm01;

import java.text.SimpleDateFormat;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0440 extends BaseEdit {
  Crdm0440Func func;
  int i = 0;
  String kk1IdPSeqno = "";
  String kk2CrtDate = "", kk3ExIdNo = "";
  SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyyMMdd");
  String sdate = nowdate.format(new java.util.Date());

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

    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      wfIdnoData();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    wp.colSet("crt_date", getSysDate());
    wp.colSet("update_date", getSysDate());
    wp.colSet("salary", "0");
    wp.colSet("kk_id_no_code", "0");
  }

  private int getWhereStr() throws Exception {
	int i = 0;
    wp.whereStr = " where 1=1 ";
    String lsDate1 = wp.itemStr("ex_crt_date1");
    String lsDate2 = wp.itemStr("ex_crt_date2");
    if (this.chkStrend(lsDate1, lsDate2) == false) {
      alertErr2("[鍵檔日期-起迄]  輸入錯誤");
      return -1;
    }
    if (empty(wp.itemStr("ex_id_no")) == false) {
      String lsSql = "select id_p_seqno from crd_idno where 1=1 and id_no = :ex_id_no";
      setString("ex_id_no", wp.itemStr("ex_id_no"));
      sqlSelect(lsSql);
      wp.whereStr += " and  a.id_p_seqno = :ex_id_p_seqno ";
      setString("ex_id_p_seqno", sqlStr("id_p_seqno"));
      i++;
    }
    if (empty(lsDate1) == false) {
      wp.whereStr += " and  a.crt_date >= :ex_crt_date1 ";
      setString("ex_crt_date1", lsDate1);
      i++;
    }
    if (empty(lsDate2) == false) {
      wp.whereStr += " and  a.crt_date <= :ex_crt_date2 ";
      setString("ex_crt_date2", lsDate2);
      i++;
    }
    if(i == 0) {
    	alertErr2("需輸入鍵檔日期 或 身分證號");
        return -1;
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

    wp.selectSQL = " a.id_p_seqno, " + " a.trans_type, " + " a.crt_user, " + " a.crt_date, "
        + " a.chi_name, " + " a.birthday, " + " a.to_jcic_date, " + " c.id_no ";

    wp.daoTable =
        "crd_jcic_idno as a left join crd_idno as b on a.id_p_seqno= b.id_p_seqno left join crd_idno as c on a.id_p_seqno = c.id_p_seqno";
    wp.whereOrder = " order by a.id_p_seqno,b.crt_date ";
    if (getWhereStr() != 1)
      return;
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
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

    kk1IdPSeqno = itemKk("data_k1");
    if (empty(kk1IdPSeqno)) {
      String lsSql =
          "select id_p_seqno from crd_idno where 1=1 and id_no = :kk_id_no and id_no_code = :kk_id_no_code ";
      setString("kk_id_no", wp.itemStr("kk_id_no"));
      setString("kk_id_no_code", wp.itemStr("kk_id_no_code"));
      sqlSelect(lsSql);
      kk1IdPSeqno = sqlStr("id_p_seqno");
    }
    if (empty(kk1IdPSeqno)) {
      String lsSql =
          "select id_p_seqno from crd_idno where 1=1 and id_no = :id_no and id_no_code = :id_no_code ";
      setString("id_no", wp.itemStr("id_no"));
      setString("id_no_code", wp.itemStr("id_no_code"));
      sqlSelect(lsSql);
      kk1IdPSeqno = sqlStr("id_p_seqno");
    }
    kk2CrtDate = itemKk("data_k2");
    if (empty(kk2CrtDate)) {
      kk2CrtDate = wp.itemStr("kk_crt_date");
    }
    if (empty(kk2CrtDate)) {
      kk2CrtDate = wp.itemStr("crt_date");
    }
    wp.selectSQL = "hex(a.rowid) as rowid, a.mod_seqno, " + "a.trans_type, " + "a.id_p_seqno, "
        + "a.account_style, " + "a.chi_name, " + "a.eng_name, " + "a.birthday, " + "a.education, "
        + "a.sex, " + "a.cntry_code, " + "a.passport_no, " + "a.passport_date, " + "a.mail_addr, "
        + "a.resident_addr, " + "a.resident_flag, " + "a.cellar_phone, " + "a.tel_no, "
        + "a.company_name, " + "a.business_id,  " + "a.job_position, " + "a.office_tel_no, "
        + "a.salary, " + "a.service_year, " + "a.update_date, " + "a.business_code, " + "a.indigenous_name, "
        + "a.crt_user, " + "a.crt_date, "
        // + "a.crt_date as old_crt_date, "
        + "uf_2ymd(a.mod_time) as mod_date," + "a.mod_user, " + "b.id_p_seqno, " + "c.id_no, "
        + "c.id_no_code, " + "c.id_no_code as kk_id_no_code ";

    wp.daoTable =
        "crd_jcic_idno as a left join crd_idno as b on a.id_p_seqno = b.id_p_seqno left join crd_idno as c on b.id_p_seqno = c.id_p_seqno";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  a.id_p_seqno = :kk1_id_p_seqno and a.crt_date =:kk2_crt_date ";
    setString("kk1_id_p_seqno", kk1IdPSeqno);
    setString("kk2_crt_date", kk2CrtDate);
    pageSelect();

    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      wfIdnoData();
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {
    func = new Crdm0440Func(wp);
    if (ofValidation() != 1) {
      return;
    }
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

    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("business_code");
      } else {
        wp.optionKey = wp.itemStr("business_code");
      }
      this.dddwList("dddw_business_code", "crd_message", "msg_value", "msg",
          "where 1=1 and msg_type='BUS_CODE' order by msg_value");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("education");
      } else {
        wp.optionKey = wp.itemStr("education");
      }
      this.dddwList("dddw_education", "crd_message", "msg_value", "msg",
          "where 1=1 and msg_type='EDUCATION' order by msg_value");

      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("account_style");
      } else {
        wp.optionKey = wp.itemStr("account_style");
      }
      this.dddwList("dddw_account_style", "crd_message", "msg_value", "msg",
          "where 1=1 and msg_type='JCIC_TYPE' order by msg_value");
    } catch (Exception ex) {
    }
  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String transType = wp.colStr(ii, "trans_type");;
      String[] cde = new String[] {"A", "C", "D",};
      String[] txt = new String[] {"新增", "異動", "刪除"};
      wp.colSet(ii, "tt_trans_type", commString.decode(transType, cde, txt));
    }
  }

  int ofValidation() {
    String lsId = "";
    String lsIdCode = "";
    String lsSex = wp.itemStr("sex");
    String lsCntryCode = wp.itemStr("cntry_code");
    String lsPassportDate = wp.itemStr("passport_date");
    String lsPassportNo = wp.itemStr("passport_no");
    String lsCrtDate = "";

    if (strAction.equals("A")) {
      lsId = wp.itemStr("kk_id_no");
      lsIdCode = wp.itemStr("kk_id_no_code");
      lsCrtDate = wp.itemStr("crt_date");
    }
    if (strAction.equals("U")) {
      lsId = wp.itemStr("id_no");
      lsIdCode = wp.itemStr("id_no_code");
      lsCrtDate = wp.itemStr("crt_date");
    }
    if (strAction.equals("D")) {
      return 1;
    }
    if (empty(lsId) || empty(lsCrtDate)) {
      alertErr("身分證號、建檔日期不可空白!");
      return -1;
    }
    String lsSql =
        "select id_p_seqno from crd_idno where 1=1 and id_no = :kk_id_no and id_no_code = :kk_id_no_code ";
    setString("kk_id_no", lsId);
    setString("kk_id_no_code", lsIdCode);
    sqlSelect(lsSql);
    if (sqlRowNum <= 0) {
      alertErr("身分證號有誤");
      return -1;
    }

//    String lsSql2 =
//        "select count(*) as li_int from crd_apscdsuc where 1=1 and apply_id = :ex_apply_id and  apply_id_code = :ex_pply_id_code and to_jcic_date =''";
//    setString("ex_apply_id", lsId);
//    setString("ex_pply_id_code", lsIdCode);
//    sqlSelect(lsSql2);
//
//    if (sqlNum("li_int") > 0) {
//      alertErr(" KK1 尚未新增送 JCIC , 不可異動補送 !! !!");
//      return -1;
//    }

    // -- 本國人
    if (strMid(lsId, 0, 1).matches("[A-Z]")
        && (strMid(lsId, 1, 1).equals("1") || strMid(lsId, 1, 1).equals("2"))) {
      if (strMid(lsId, 1, 1).equals("1") && !lsSex.equals("1")) {
        alertErr("錯誤~ , 此人為男性 !! ");
        return -1;
      }
      if (strMid(lsId, 1, 1).equals("2") && !lsSex.equals("2")) {
        alertErr("錯誤~ , 此人為女性 !! ");
        return -1;
      }

      if (empty(lsCntryCode) == false) {
//        alertErr("錯誤~ , 本國人不可有國籍 !!");
//        return -1;
      }
    } else {// 外國人
      if (empty(lsCntryCode)) {
        alertErr("Error , 外~國人必需有國籍 !!");
        return -1;
      }
      if (empty(lsPassportNo)) {
        errmsg("Error , 外~國人必需有護照號碼 !!");
        return -1;
      }
      if (empty(lsPassportDate) == true) {
        alertErr("Error , 外~國人必需有護照日期 !!");
        return -1;
      }
    }

    if (!empty(wp.itemStr("to_jcic_date"))) {
      alertErr("此卡號已存在檔內,並且已送JCIC,不可再做新增,修改或刪除");
      return -1;
    }
    if (strAction.equals("A")) {
      if (wfCrdChgId(lsId, lsIdCode) != 1) {
        alertErr("JCIC 變更 ID 已存在,不可再送 !!");
        return -1;
      }
    }
    if (wp.itemNum("salary") < 1000 && wp.itemNum("salary") > 0) {
      alertErr("年~薪~ 不可為負值或介於1 - 999!!");
      return -1;
    }
    if (wp.itemNum("salary") < 0) {
      alertErr("年~薪~ 不可為負值或介於1 - 999!!");
      return -1;
    }

    return 1;
  }

  int wfCrdChgId(String asId, String asIdCode) {
    String sqlSelect =
        "select post_jcic_flag from crd_chg_id where old_id_no = :as_id and old_id_no_code = :as_id_code";
    setString("as_id", asId);
    setString("as_id_code", asIdCode);
    sqlSelect(sqlSelect);
    String lsPostJcicFlag = sqlStr("post_jcic_flag");
    if (lsPostJcicFlag.equals("N")) {
      return -1;
    }
    return 1;
  }

  void wfIdnoData() {
    if (empty(wp.itemStr("kk_id_no"))) {
      return;
    }

    String sqlSelect = " select " + "id_no, " + "id_no_code, " + "id_p_seqno, " + "chi_name, "
        + "card_since, " + "birthday, " + "education, " + "marriage, " + "job_position, "
        + "service_year, " + "annual_income as salary, " + "company_name, " + "business_id , "
        + "rtrim(home_area_code1)||rtrim(home_tel_no1)||rtrim(home_tel_ext1) as ls_tel_no, "
        + "rtrim(office_area_code1)||rtrim(office_tel_no1)||rtrim(office_tel_ext1) as ls_office_tel_no, "
        + "rtrim(resident_zip) as ls_resident_zip, "
        + "rtrim(resident_addr1)||rtrim(resident_addr2)||rtrim(resident_addr3)||rtrim(resident_addr4)||rtrim(resident_addr5) as ls_resident_addr, "
        + "rtrim(cellar_phone) as cellar_phone, " + "passport_no, " + "passport_date, "
        + "other_cntry_code, " + "business_code, " + "sex, " + "indigenous_name " + " from crd_idno "
        + " where id_no = :kk_id_no " + " and id_no_code = :kk_id_no_code ";
    setString("kk_id_no", wp.itemStr("kk_id_no"));
    setString("kk_id_no_code", wp.itemStr("kk_id_no_code"));
    sqlSelect(sqlSelect);
    wp.colSet("id_no", wp.itemStr("kk_id_no"));
    wp.colSet("id_no_code", wp.itemStr("kk_id_no_code"));
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此卡人資料");
      return;
    }
    String lsIdPSeqno = sqlStr("id_p_seqno");
    String lsIdNo = sqlStr("id_no");
    String lsIdNoCode = sqlStr("id_no_code");
    String lsChiName = sqlStr("chi_name");
    String lsCardSince = sqlStr("card_since");
    String lsBirthday = sqlStr("birthday");
    String lsEducation = sqlStr("education");
    String lsMarriage = sqlStr("marriage");
    String lsJobPosition = sqlStr("job_position");
    String lsServiceYear = sqlStr("service_year");
    String lsSalary = sqlStr("salary");
    String lsCompanyName = sqlStr("company_name");
    String lsBusinessId = sqlStr("business_id");
    String lsTelNo = sqlStr("ls_tel_no");
    String lsOfficeTelNo = sqlStr("ls_office_tel_no");
    String lsResidentAddr = sqlStr("ls_resident_addr");
    String lsCellarPhone = sqlStr("cellar_phone");
    String lsPassportNo = sqlStr("passport_no");
    String lsPassportDate = sqlStr("passport_date");
    String lsOtherCntryCode = sqlStr("other_cntry_code");
    String lsSex = sqlStr("sex");
    String lsBusinessCode = sqlStr("business_code");
    String lsIndigenousName = sqlStr("indigenous_name");
    sqlSelect =
        " select card_no,eng_name,acno_p_seqno from crd_card where id_p_seqno = :ls_id_p_seqno";
    setString("ls_id_p_seqno", lsIdPSeqno);
    sqlSelect(sqlSelect);
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此卡號");
      return;
    }
    String lsCardNo = sqlStr("card_no");
    String lsEngName = sqlStr("eng_name");
    String lsAcnoPSeqno = sqlStr("acno_p_seqno");

    sqlSelect = " select bill_sending_zip "
        + " ,rtrim(bill_sending_addr1)||rtrim(bill_sending_addr2)||rtrim(bill_sending_addr3)||rtrim(bill_sending_addr4)||rtrim(bill_sending_addr5) as ls_mail_addr "
        + " from act_acno " + " where acno_p_seqno = :ls_acno_p_seqno ";
    setString("ls_acno_p_seqno", lsAcnoPSeqno);
    sqlSelect(sqlSelect);
    String lsMailAip = sqlStr("bill_sending_zip");
    String lsMailAddr = sqlStr("ls_mail_addr");
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此帳戶資料");
      return;
    }
    wp.colSet("mail_zip", lsMailAip);
    wp.colSet("old_mail_zip", lsMailAip);
    wp.colSet("mail_addr", lsMailAddr);
    wp.colSet("old_mail_addr", lsMailAddr);
    wp.colSet("card_no", lsCardNo);
    wp.colSet("eng_name", lsEngName);
    wp.colSet("id_no", lsIdNo);
    wp.colSet("id_no_code", lsIdNoCode);
    wp.colSet("id_p_seqno", lsIdPSeqno);
    wp.colSet("chi_name", lsChiName);
    wp.colSet("card_since", lsCardSince);
    wp.colSet("birthday", lsBirthday);
    wp.colSet("education", lsEducation);
    wp.colSet("marriage", lsMarriage);
    wp.colSet("job_position", lsJobPosition);
    wp.colSet("service_year", lsServiceYear);
    wp.colSet("salary", lsSalary);
    wp.colSet("company_name", lsCompanyName);
    wp.colSet("business_id", lsBusinessId);
    wp.colSet("tel_no", lsTelNo);
    wp.colSet("office_tel_no", lsOfficeTelNo);
    wp.colSet("resident_addr", lsResidentAddr);
    wp.colSet("cellar_phone", lsCellarPhone);
    wp.colSet("passport_no", lsPassportNo);
    wp.colSet("passport_date", lsPassportDate);
    wp.colSet("cntry_code", lsOtherCntryCode);
    wp.colSet("sex", lsSex);
    wp.colSet("business_code", lsBusinessCode);
    wp.colSet("indigenous_name", lsIndigenousName);
  }
}
