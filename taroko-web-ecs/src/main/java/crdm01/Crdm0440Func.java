/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-25  V1.00.01  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 110-08-17  V1.00.02  Wilson     新增原住名姓名羅馬拼音欄位                                                                      *
******************************************************************************/
package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0440Func extends FuncEdit {
  String kk1IdPSeqno = "", kk1IdNo = "";
  String kk2DrtDate = "";


  public Crdm0440Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TOD11111
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {



    if (this.isAdd()) {
      kk1IdNo = wp.itemStr("kk_id_no");
      kk2DrtDate = wp.itemStr("crt_date");

      String lsSsql =
          "select id_p_seqno from crd_idno where 1=1 and id_no = :ex_id_no and id_no_code = :ex_id_no_code ";
      setString("ex_id_no", kk1IdNo);
      setString("ex_id_no_code", wp.itemStr("kk_id_no_code"));
      sqlSelect(lsSsql);
      kk1IdPSeqno = colStr("id_p_seqno");

      sqlWhere = " where 1=1 and id_p_seqno = ? ";
      Object[] param1 = new Object[] {kk1IdPSeqno};
      if (this.isOtherModify("crd_idno", sqlWhere, param1)) {
        errmsg("無此卡號，請重新查詢 !");
        return;
      }

      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from crd_jcic_idno where id_p_seqno = ? and crt_date = ? ";
      Object[] param = new Object[] {kk1IdPSeqno, kk2DrtDate};

      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }

    } else {
      kk1IdNo = wp.itemStr("id_no");
      kk2DrtDate = wp.itemStr("crt_date");
      String lsSql1 =
          "select id_p_seqno from crd_idno where 1=1 and id_no = :id_no and id_no_code = :id_no_code ";
      setString("id_no", kk1IdNo);
      setString("id_no_code", wp.itemStr("id_no_code"));
      sqlSelect(lsSql1);
      kk1IdPSeqno = colStr("id_p_seqno");
      // -other modify-
      sqlWhere = " where 1=1 and id_p_seqno = ? and crt_date=? and nvl(mod_seqno,0) = ?";

      Object[] param = new Object[] {kk1IdPSeqno, kk2DrtDate, wp.modSeqno()};
      if (this.isOtherModify("crd_jcic_idno", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = " insert into crd_jcic_idno ( " + " id_p_seqno, " + " trans_type," + " account_style, "
        + " chi_name, " + " eng_name, " + " birthday, " + " education, " + " sex, "
        + " cntry_code, " + " passport_no, "// 10
        + " passport_date, " + " mail_addr, " + " resident_addr, " + " resident_flag, "
        + " cellar_phone, " + " tel_no, " + " company_name, " + " business_id, " + " job_position, "
        + " office_tel_no, "// 20
        + " salary, " + " service_year, " + " update_date, " + " business_code, " + " mail_zip, "
        + " old_mail_zip, " + " old_mail_addr, " + " indigenous_name, "// 28
        + " crt_date,crt_user,mod_time,mod_user,mod_pgm,mod_seqno " + " ) values ("
        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, " + " ?,?,sysdate,?,?,?) ";

    // -set ?value-
    Object[] param =
        new Object[] {kk1IdPSeqno, wp.itemStr("trans_type"), wp.itemStr("account_style"),
            wp.itemStr("chi_name"), wp.itemStr("eng_name"), wp.itemStr("birthday"),
            wp.itemStr("education"), wp.itemStr("sex"), wp.itemStr("cntry_code"),
            wp.itemStr("passport_no"), wp.itemStr("passport_date"), wp.itemStr("mail_addr"),
            wp.itemStr("resident_addr"), wp.itemStr("resident_flag"), wp.itemStr("cellar_phone"),
            wp.itemStr("tel_no"), wp.itemStr("company_name"), wp.itemStr("business_id"),
            wp.itemStr("job_position"), wp.itemStr("office_tel_no"), wp.itemNum("salary"),
            wp.itemStr("service_year"), wp.itemStr("update_date"), wp.itemStr("business_code"),
            wp.itemStr("mail_zip"), wp.itemStr("old_mail_zip"), wp.itemStr("old_mail_addr"),
            wp.itemStr("indigenous_name"),
            kk2DrtDate, wp.loginUser, wp.loginUser, wp.modPgm(), wp.modSeqno()};
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
    strSql = "update crd_jcic_idno set " + "trans_type=?," + "account_style=?, " + " chi_name=?, "
        + " eng_name=?, " + " birthday=?, " + " education=?, " + " sex=?, " + " cntry_code=?, "
        + " passport_no=?, " + " passport_date=?, " + " mail_addr=?, " + " resident_addr=?, "
        + " resident_flag=?, " + " cellar_phone=?, " + " tel_no=?, " + " company_name=?, "
        + " business_id=?, " + " job_position=?, " + " office_tel_no=?, " + " salary=?, "
        + " service_year =?, " + " update_date =?, " + " business_code =?, " + "indigenous_name =?, " 
        + " crt_user=?, "
        // + " crt_date=?, "
        + " mod_time=sysdate, " + " mod_pgm =?, " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where 1=1 and id_p_seqno = ? and crt_date = ? ";

    Object[] param =
        new Object[] {wp.itemStr("trans_type"), wp.itemStr("account_style"), wp.itemStr("chi_name"),
            wp.itemStr("eng_name"), wp.itemStr("birthday"), wp.itemStr("education"),
            wp.itemStr("sex"), wp.itemStr("cntry_code"), wp.itemStr("passport_no"),
            wp.itemStr("passport_date"), wp.itemStr("mail_addr"), wp.itemStr("resident_addr"),
            wp.itemStr("resident_flag"), wp.itemStr("cellar_phone"), wp.itemStr("tel_no"),
            wp.itemStr("company_name"), wp.itemStr("business_id"), wp.itemStr("job_position"),
            wp.itemStr("office_tel_no"), wp.itemNum("salary"), wp.itemStr("service_year"),
            wp.itemStr("update_date"), wp.itemStr("business_code"), wp.itemStr("indigenous_name"),
            wp.loginUser,
            // wp.item_ss("crt_date"),
            wp.modPgm(), kk1IdPSeqno, kk2DrtDate};
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

    strSql = "delete crd_jcic_idno " + sqlWhere;
    // ddd("del-sql="+is_sql);
    Object[] param = new Object[] {kk1IdPSeqno, kk2DrtDate, wp.modSeqno()};
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
