/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-07  V1.00.00  yash           program initial                        *
* 109-03-26  V1.00.01  Zhenwu Zhu     增加 保證責任種類字段及查詢功能                                                                           *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Crdm0910 extends BaseEdit {
  String mRelaType = "";
  String mIdPSeqno = "";
  String mCardNo = "";
  String mRelaDutyType = "";
  CommString commString = new CommString();

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
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      wfIdnoData();
    } else if (eqIgno(wp.buttonCode, "R3")) {
      // -資料讀取-
      strAction = "R3";
      wfIdnoData1();
    }


    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_cardid")) == false) {
      wp.whereStr +=
          " and id_p_seqno = (select id_p_seqno from crd_idno " + "where id_no=:ex_cardid ) ";
      setString("ex_cardid", wp.itemStr("ex_cardid"));
    }
    if (empty(wp.itemStr("ex_cardno")) == false) {
      wp.whereStr += " and  card_no = :ex_cardno ";
      setString("ex_cardno", wp.itemStr("ex_cardno"));
    }
    if (empty(wp.itemStr("ex_relid")) == false) {
      wp.whereStr += " and  rela_id = :ex_relid ";
      setString("ex_relid", wp.itemStr("ex_relid"));
    }
    if (!"0".equals(wp.itemStr("ex_rela_duty_type"))) {
      wp.whereStr += " and  rela_duty_type = :ex_rela_duty_type ";
      setString("ex_rela_duty_type", wp.itemStr("ex_rela_duty_type"));
    }
    // if(empty(wp.item_ss("ex_cardid"))&&empty(wp.item_ss("ex_cardno"))){
    // wp.whereStr += " and rela_type = '1' ";
    // }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " UF_IDNO_ID(id_p_seqno) as id" + ", id_p_seqno" + ", card_no" + ", acct_type"
        + ", rela_duty_type" + ", rela_id" + ", rela_name" + ", rela_type";

    wp.daoTable = "crd_rela";
    wp.whereOrder = " order by card_no";
    getWhereStr();
    pageQuery();

    String relaDutyType = wp.colStr("rela_duty_type");
    wp.colSet("rela_duty_type_desc", commString.decode(relaDutyType, ",1,2", ",一般保證,連帶保證"));
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mRelaType = itemKk("data_k1");
    mIdPSeqno = itemKk("data_k2");
    mCardNo = itemKk("data_k3");
    mRelaDutyType = itemKk("data_k4");

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + " ,rela_type        "
        + " ,rela_duty_type        " + " ,job_position        " + " ,annual_income        "
        + " ,acno_p_seqno          " + " ,UF_ACNO_KEY(acno_p_seqno) as  acct_key    "
        + " ,rela_id          " + " ,card_no          " + " ,sex              "
        + " ,birthday         " + " ,id_p_seqno       " + " ,UF_IDNO_NAME(id_p_seqno) as name  "
        + " ,UF_IDNO_ID(id_p_seqno) as id_no " + " ,acct_type        " + " ,rela_name        "
        + " ,company_name     " + " ,company_zip      " + " ,company_addr1    "
        + " ,company_addr2    " + " ,company_addr3    " + " ,company_addr4    "
        + " ,company_addr5    " + " ,office_area_code1" + " ,office_tel_no1   "
        + " ,office_tel_ext1  " + " ,office_area_code2" + " ,office_tel_no2   "
        + " ,office_tel_ext2  " + " ,home_area_code1  " + " ,home_tel_no1     "
        + " ,home_tel_ext1    " + " ,home_area_code2  " + " ,home_tel_no2     "
        + " ,home_tel_ext2    " + " ,resident_zip     " + " ,resident_addr1   "
        + " ,resident_addr2   " + " ,resident_addr3   " + " ,resident_addr4   "
        + " ,resident_addr5   " + " ,mail_zip         " + " ,mail_addr1       "
        + " ,mail_addr2       " + " ,mail_addr3       " + " ,mail_addr4       "
        + " ,mail_addr5       " + " ,cellar_phone     " + " ,apr_date         "
        + " ,apr_user         " + " ,crt_date         " + " ,crt_user         "
        + " ,start_date       " + " ,end_date         " + " ,rela_seqno       "
        + " ,mod_user         " + " ,mod_time         " + " ,mod_pgm          ";
    wp.daoTable = "crd_rela";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  rela_type = :rela_type ";
    wp.whereStr += " and  id_p_seqno = :id_p_seqno ";
    wp.whereStr += " and  card_no = :card_no ";
    wp.whereStr += " and rela_duty_type = :rela_duty_type";
    setString("rela_type", mRelaType);
    setString("id_p_seqno", mIdPSeqno);
    setString("card_no", mCardNo);
    setString("rela_duty_type", mRelaDutyType);

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, id_p_seqno=" + mIdPSeqno);
    }
  }

  @Override
  public void saveFunc() throws Exception {
    Crdm0910Func func = new Crdm0910Func(wp);
    if (strAction.equals("D")) {
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }
    }
    if (strAction.equals("A")) {
      if (empty(wp.itemStr("acct_key")) && empty(wp.itemStr("card_no"))) {
        alertErr("帳戶ID或卡號至少要輸入一項 !!");
        return;
      }
    }

    if (ofCalidation() != 1) {
      return;
    }
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
      this.sqlCommit(rc);
      return;
    }
    /*
     * 20190906 add 新增/刪除保證人及修改保證人姓名時，若被保證人ID項下仍有流通正卡， 須傳送JCIC保證人新增、刪除及異動檔(KK2)
     */
    String exCardNo = wp.itemStr("card_no");
    String lsIdPSeqno = wp.itemStr("id_p_seqno");
    String lsAcnoPSeqno = wp.itemStr("acno_p_seqno");
    String lsSql = "", lsCardNo = "", lsPaymentDate = "";
    String lsAcctKey = "";
    if (empty(lsIdPSeqno)) {
      if (empty(wp.itemStr("acct_key"))) {
        lsAcctKey = wp.itemStr("id_no") + "0";
      }
      lsSql = "select id_p_seqno from crd_idno where id_no =:id_no ";
      setString("id_no", strMid(lsAcctKey, 0, 10));
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        lsIdPSeqno = sqlStr("id_p_seqno");
      }
    }
    if (empty(lsAcnoPSeqno)) {
      lsSql = "select acno_p_seqno from act_acno " + "where acct_type =:acct_type "
          + "and acct_key =:acct_key ";
      setString("acct_type", wp.itemStr("acct_type"));
      setString("acct_key", lsAcctKey);
      sqlSelect(lsSql);
      if (sqlRowNum > 0) {
        lsAcnoPSeqno = sqlStr("acno_p_seqno");
      }
    }

    lsSql = "select card_no " + "from crd_card " + "where current_code = '0' "
        + "and id_p_seqno =:id_p_seqno ";
    setString("id_p_seqno", lsIdPSeqno);
    sqlSelect(lsSql);
    int selCt = sqlRowNum;
    if (selCt > 0) {
      lsCardNo = sqlStr(0, "card_no");
      lsSql = "select debt_close_date " + "from act_acno " + "where acno_p_seqno =:acno_p_seqno ";
      setString("acno_p_seqno", lsAcnoPSeqno);
      sqlSelect(lsSql);
      int selCt1 = sqlRowNum;
      if (selCt1 > 0) {
        lsPaymentDate = sqlStr("debt_close_date");
      }
      busi.SqlPrepare sp = new SqlPrepare();
      sp.sql2Insert("crd_jcic_kk2");
      sp.ppstr("card_no", lsCardNo);
      sp.ppstr("trans_type", strAction);
      sp.ppstr("current_code", "0");
      // sp.ppss("oppost_reason", "");
      // sp.ppss("oppost_date", "");
      sp.ppstr("payment_date", lsPaymentDate);
      // sp.ppnum("risk_amt", 0);
      // sp.ppss("kk4_note", "");
      // sp.ppss("to_jcic_date", "");
      sp.ppstr("crt_date", getSysDate());
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppnum("mod_seqno", 1);
      sp.addsql(", mod_time ", ", sysdate ");
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum == 0) {
        rc = -1;
      } else {
        rc = 1;
      }
    } else {
      lsSql = "select card_no " + "from crd_card "
          + "where issue_date = (select max(issue_date) from crd_card "
          + "					   where id_p_seqno =:id_p_seqno ) "
          + "and id_p_seqno =:id_p_seqno ";
      setString("id_p_seqno", lsIdPSeqno);
      sqlSelect(lsSql);
      lsCardNo = sqlStr("card_no");

      lsSql = "select debt_close_date " + "from act_acno " + "where acno_p_seqno =:acno_p_seqno ";
      setString("acno_p_seqno", lsAcnoPSeqno);
      sqlSelect(lsSql);
      int selCt1 = sqlRowNum;
      if (selCt1 > 0) {
        lsPaymentDate = sqlStr("debt_close_date");
      }
      busi.SqlPrepare sp = new SqlPrepare();
      sp.sql2Insert("crd_jcic_kk2");
      sp.ppstr("card_no", lsCardNo);
      sp.ppstr("trans_type", strAction);
      sp.ppstr("current_code", "0");
      // sp.ppss("oppost_reason", "");
      // sp.ppss("oppost_date", "");
      sp.ppstr("payment_date", lsPaymentDate);
      // sp.ppnum("risk_amt", 0);
      // sp.ppss("kk4_note", "");
      // sp.ppss("to_jcic_date", "");
      sp.ppstr("crt_date", getSysDate());
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.ppnum("mod_seqno", 1);
      sp.addsql(", mod_time ", ", sysdate ");
      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum == 0) {
        rc = -1;
      } else {
        rc = 1;
      }
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
        wp.optionKey = "--";
        wp.optionKey = wp.itemStr("acct_type");
        this.dddwList("dddw_acct", "ptr_acct_type", "acct_type", "chin_name",
            " where 1=1  order by acct_type");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("company_zip");
        this.dddwList("dddw_zipcode", "ptr_zipcode", "zip_code",
            "zip_code||' '||zip_city||zip_town", "where 1=1  order by zip_code");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("resident_zip");
        this.dddwList("dddw_zipcode_r", "ptr_zipcode", "zip_code",
            "zip_code||' '||zip_city||zip_town", "where 1=1  order by zip_code");

        wp.initOption = "--";
        wp.optionKey = wp.colStr("mail_zip");
        this.dddwList("dddw_zipcode_m", "ptr_zipcode", "zip_code",
            "zip_code||' '||zip_city||zip_town", "where 1=1  order by zip_code");
      }

    } catch (Exception ex) {
    }
  }

  public int ofCalidation() {
    String lsSql = "", lsPSeqno = "", lsIdPSeqno = "";
    // 卡號非空白==>檢核卡號->帳戶資料->卡人
    if (!empty(wp.itemStr("card_no"))) {
      lsSql = "select p_seqno,id_p_seqno " + "from crd_card " + "where card_no =:card_no "
          + "and current_code ='0' ";
      setString("card_no", wp.itemStr("card_no"));
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        alertErr("輸入卡號無效或非有效卡!!");
        return 0;
      }
      lsPSeqno = sqlStr("p_seqno");
      lsIdPSeqno = sqlStr("id_p_seqno");

      if (!empty(wp.itemStr("acct_key"))) {
        lsSql = "select acno_p_seqno " + "from act_acno " + "where acct_type =:acct_type "
            + "and acct_key =:acct_key ";
        setString("acct_type", wp.itemStr("acct_type"));
        setString("acct_key", wp.itemStr("acct_key"));
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          alertErr("查無帳戶資料!!");
          return 0;
        }
        if (!lsPSeqno.equals(sqlStr("acno_p_seqno"))) {
          alertErr("輸入帳戶資料與卡號非同一卡人!!");
          return 0;
        }
      }

      lsSql = "select id_no " + "from crd_idno " + "where id_p_seqno =:id_p_seqno ";
      setString("id_p_seqno", lsIdPSeqno);
      sqlSelect(lsSql);
      if (sqlRowNum <= 0) {
        alertErr("查無卡人資料!!");
        return 0;
      }

    }
    // 卡號空白==>檢核帳戶資料->卡人
    if (empty(wp.itemStr("card_no"))) {
      if (!empty(wp.itemStr("acct_key"))) {
        lsSql = "select acno_p_seqno " + "from act_acno " + "where acct_type =:acct_type "
            + "and acct_key =:acct_key ";
        setString("acct_type", wp.itemStr("acct_type"));
        setString("acct_key", wp.itemStr("acct_key"));
        sqlSelect(lsSql);
        if (sqlRowNum <= 0) {
          alertErr("查無帳戶資料!!");
          return 0;
        }

        lsSql = "select count(*) ct " + "from crd_idno " + "where id_no =:id_no ";
        setString("id_no", strMid(wp.itemStr("acct_key"), 0, 10));
        sqlSelect(lsSql);
        if (sqlNum("ct") <= 0) {
          alertErr("查無卡人資料!!");
          return 0;
        }
      }
    }
    return 1;
  }

  void wfIdnoData() {

    if (empty(wp.itemStr("acct_key"))) {
      return;
    }

    String sqlSelect = " select " + "a.acct_key, " + "b.chi_name, " + "b.id_no "
        + " from act_acno a , crd_idno b " + " where 1 = 1 " + " and a.id_p_seqno = b.id_p_seqno "
        + " and a.acct_key = :acct_key ";
    setString("acct_key", wp.itemStr("acct_key"));
    sqlSelect(sqlSelect);
    wp.colSet("acct_key", wp.itemStr("acct_key"));
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此卡人資料");
      return;
    }
    wp.colSet("name", sqlStr("chi_name"));
    wp.colSet("id_no", sqlStr("id_no"));

  }

  void wfIdnoData1() {

    if (empty(wp.itemStr("card_no"))) {
      return;
    }

    String sqlSelect =
        " select " + "a.card_no, " + "b.chi_name, " + "b.id_no " + " from crd_card a , crd_idno b "
            + " where 1 = 1 " + " and a.id_p_seqno = b.id_p_seqno " + " and a.card_no = :card_no ";
    setString("card_no", wp.itemStr("card_no"));
    sqlSelect(sqlSelect);
    wp.colSet("card_no", wp.itemStr("card_no"));
    if (sqlRowNum <= 0) {
      alertErr("抓取不到此卡人資料");
      return;
    }
    wp.colSet("name", sqlStr("chi_name"));
    wp.colSet("id_no", sqlStr("id_no"));

  }
}
