/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-07  V1.00.00  yash           program initial                        *
* 109-03-26  V1.00.01  Zhenwu Zhu     增加 保證責任種類，年收入， 任職職稱字段及新增驗證                                                                *
* 109-04-28  V1.00.02 YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;

import java.math.BigDecimal;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0910Func extends FuncEdit {
  String mIdPSeqno = "";
  String mCardNo = "";
  String mRelaType = "";
  String mPSeqno = "";
  String lsAcnoKey = "";

  public Crdm0910Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    String lsIdno = "";
    if (wp.itemStr("acno_key").length() == 10) {
      lsAcnoKey = wp.itemStr("acno_key") + "0";
    } else {
      lsAcnoKey = wp.itemStr("acno_key");
    }
    if (empty(lsAcnoKey)) {
      lsAcnoKey = wp.itemStr("id_no") + "0";
      lsIdno = wp.itemStr("id_no");
    } else {
      lsIdno = strMid(lsAcnoKey, 0, 10);
    }

    if ("0".equals(wp.itemStr("rela_duty_type"))) {
      errmsg("請輸入保證責任種類");
      return;
    }

    if (this.ibAdd) {

      String sqlPSeqno = "select acno_p_seqno from act_acno where acct_type = ? and acct_key = ?  ";
      Object[] param = new Object[] {wp.itemStr("acct_type"), lsAcnoKey};
      sqlSelect(sqlPSeqno, param);
      mPSeqno = colStr("acno_p_seqno");

      String sqlIdPSeqno = "select id_p_seqno from crd_idno where id_no = ? ";
      // Object[] param2 = new Object[] {wp.item_ss("rela_id")};
      Object[] param2 = new Object[] {lsIdno};
      sqlSelect(sqlIdPSeqno, param2);
      mIdPSeqno = colStr("id_p_seqno");

    }

    if (this.ibUpdate) {
      String sqlPSeqno = "select acno_p_seqno from act_acno where acct_type = ? and acct_key = ?  ";
      Object[] param = new Object[] {wp.itemStr("acct_type"), lsAcnoKey};
      sqlSelect(sqlPSeqno, param);
      mPSeqno = colStr("acno_p_seqno");

      String sqlIdPSeqno = "select id_p_seqno from crd_idno where id_no = ? ";
      Object[] param2 = new Object[] {lsIdno};
      sqlSelect(sqlIdPSeqno, param2);
      mIdPSeqno = colStr("id_p_seqno");

    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      // String lsSql = "select count(*) as tot_cnt from crd_rela where
      // id_p_seqno = ? and card_no = ? and rela_type = ? and p_seqno =
      // ?";
      // Object[] param = new Object[] {
      // m_id_p_seqno,m_card_no,m_rela_type,m_p_seqno};
      // sqlSelect(lsSql, param);
      // if (col_num("tot_cnt") > 0)
      // {
      // errmsg("資料已存在，無法新增");
      // }
      // return;
    } else {
      // -other modify-
      sqlWhere = " where rowid = ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {wp.itemRowId("rowid"), wp.modSeqno()};
      isOtherModify("crd_rela", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    BigDecimal annualIncome;
    if (empty(wp.itemStr("annual_income"))) {
      annualIncome = new BigDecimal(0);
    } else {
      annualIncome = new BigDecimal(wp.itemStr("annual_income"));
    }

    strSql = "insert into crd_rela (" + "  acct_type " + ", rela_duty_type " + ", annual_income "
        + ", job_position " + ", card_no " + ", rela_id " + ", start_date " + ", end_date "
        + ", rela_name " + ", sex " + ", birthday " + ", company_name " + ", company_zip "
        + ", company_addr1 " + ", company_addr2 " + ", company_addr3 " + ", company_addr4 "
        + ", company_addr5 " + ", office_area_code1 " + ", office_tel_no1 " + ", office_tel_ext1 "
        + ", office_area_code2 " + ", office_tel_no2 " + ", home_area_code1 " + ", home_tel_no1 "
        + ", home_tel_ext1 " + ", home_area_code2 " + ", home_tel_no2 " + ", home_tel_ext2 "
        + ", resident_zip " + ", resident_addr1 " + ", resident_addr2 " + ", resident_addr3 "
        + ", resident_addr4 " + ", resident_addr5 " + ", mail_zip " + ", mail_addr1 "
        + ", mail_addr2 " + ", mail_addr3 " + ", mail_addr4 " + ", mail_addr5 " + ", cellar_phone "
        + ", acno_p_seqno " + ", id_p_seqno " + ", rela_type " + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("acct_type"), wp.itemStr("rela_duty_type"),
        annualIncome, wp.itemStr("job_position"), wp.itemStr("card_no"), wp.itemStr("rela_id"),
        wp.itemStr("start_date"), wp.itemStr("end_date"), wp.itemStr("rela_name"),
        wp.itemStr("sex"), wp.itemStr("birthday"), wp.itemStr("company_name"),
        wp.itemStr("company_zip"), wp.itemStr("company_addr1"), wp.itemStr("company_addr2"),
        wp.itemStr("company_addr3"), wp.itemStr("company_addr4"), wp.itemStr("company_addr5"),
        wp.itemStr("office_area_code1"), wp.itemStr("office_tel_no1"),
        wp.itemStr("office_tel_ext1"), wp.itemStr("office_area_code2"),
        wp.itemStr("office_tel_no2"), wp.itemStr("home_area_code1"), wp.itemStr("home_tel_no1"),
        wp.itemStr("home_tel_ext1"), wp.itemStr("home_area_code2"), wp.itemStr("home_tel_no2"),
        wp.itemStr("home_tel_ext2"), wp.itemStr("resident_zip"), wp.itemStr("resident_addr1"),
        wp.itemStr("resident_addr2"), wp.itemStr("resident_addr3"), wp.itemStr("resident_addr4"),
        wp.itemStr("resident_addr5"), wp.itemStr("mail_zip"), wp.itemStr("mail_addr1"),
        wp.itemStr("mail_addr2"), wp.itemStr("mail_addr3"), wp.itemStr("mail_addr4"),
        wp.itemStr("mail_addr5"), wp.itemStr("cellar_phone"), mPSeqno, mIdPSeqno,
        wp.itemStr("rela_type"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    BigDecimal annualIncome;
    if (empty(wp.itemStr("annual_income"))) {
      annualIncome = new BigDecimal(0);
    } else {
      annualIncome = new BigDecimal(wp.itemStr("annual_income"));
    }
    strSql = "update crd_rela set " + " acct_type =? " + ", rela_duty_type =? "
        + ", job_position =? " + ", annual_income =? " + " , card_no =? " + " , rela_id =? "
        + " , start_date =? " + " , end_date =? " + " , rela_name =? " + " , sex =? "
        + " , birthday =? " + " , company_name =? " + " , company_zip =? " + " , company_addr1 =? "
        + " , company_addr2 =? " + " , company_addr3 =? " + " , company_addr4 =? "
        + " , company_addr5 =? " + " , office_area_code1 =? " + " , office_tel_no1 =? "
        + " , office_tel_ext1 =? " + " , office_area_code2 =? " + " , office_tel_no2 =? "
        + " , office_tel_ext2 =? " + " , home_area_code1 =? " + " , home_tel_no1 =? "
        + " , home_tel_ext1 =? " + " , home_area_code2 =? " + " , home_tel_no2 =? "
        + " , home_tel_ext2 =? " + " , resident_zip =? " + " , resident_addr1 =? "
        + " , resident_addr2 =? " + " , resident_addr3 =? " + " , resident_addr4 =? "
        + " , resident_addr5 =? " + " , mail_zip =? " + " , mail_addr1 =? " + " , mail_addr2 =? "
        + " , mail_addr3 =? " + " , mail_addr4 =? " + " , mail_addr5 =? " + " , cellar_phone =? "
        + " , acno_p_seqno =? " + " , id_p_seqno =? " + " , rela_type =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("acct_type"), wp.itemStr("rela_duty_type"),
        wp.itemStr("job_position"), annualIncome, wp.itemStr("card_no"), wp.itemStr("rela_id"),
        wp.itemStr("start_date"), wp.itemStr("end_date"), wp.itemStr("rela_name"),
        wp.itemStr("sex"), wp.itemStr("birthday"), wp.itemStr("company_name"),
        wp.itemStr("company_zip"), wp.itemStr("company_addr1"), wp.itemStr("company_addr2"),
        wp.itemStr("company_addr3"), wp.itemStr("company_addr4"), wp.itemStr("company_addr5"),
        wp.itemStr("office_area_code1"), wp.itemStr("office_tel_no1"),
        wp.itemStr("office_tel_ext1"), wp.itemStr("office_area_code2"),
        wp.itemStr("office_tel_no2"), wp.itemStr("office_tel_ext2"), wp.itemStr("home_area_code1"),
        wp.itemStr("home_tel_no1"), wp.itemStr("home_tel_ext1"), wp.itemStr("home_area_code2"),
        wp.itemStr("home_tel_no2"), wp.itemStr("home_tel_ext2"), wp.itemStr("resident_zip"),
        wp.itemStr("resident_addr1"), wp.itemStr("resident_addr2"), wp.itemStr("resident_addr3"),
        wp.itemStr("resident_addr4"), wp.itemStr("resident_addr5"), wp.itemStr("mail_zip"),
        wp.itemStr("mail_addr1"), wp.itemStr("mail_addr2"), wp.itemStr("mail_addr3"),
        wp.itemStr("mail_addr4"), wp.itemStr("mail_addr5"), wp.itemStr("cellar_phone"), mPSeqno,
        mIdPSeqno, wp.itemStr("rela_type"), wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete crd_rela " + sqlWhere;
    Object[] param = new Object[] {wp.itemRowId("rowid"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
