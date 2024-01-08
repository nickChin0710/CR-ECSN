
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5060Func extends FuncEdit {
  String cardNote = "", areaType = "F", prdAttrib = "P";

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      cardNote = wp.itemStr("kk_card_note");
    } else {
      cardNote = wp.itemStr("card_note");
    }

    if (empty(cardNote)) {
      errmsg("卡片等級:不可空白");
      return;
    }

    if (wp.itemEmpty("start_date")) {
      errmsg("日期不可空白");
      return;
    }
    if (wp.itemEmpty("end_date")) {
      errmsg("日期不可空白");
      return;
    }

    if (chkStrend(wp.itemStr("start_date"), wp.itemStr("end_date")) != 1) {
      errmsg("日期: 起迄輸入錯誤");
      return;
    }

    if (wp.itemNum("start_date") < commString.strToNum(this.getSysDate())) {
      errmsg("日期(起):不可小於系統日");
      return;
    }

    if (wp.itemNum("end_date") < commString.strToNum(this.getSysDate())) {
      errmsg("日期(迄):不可小於系統日");
      return;
    }

    if (wp.itemEmpty("prd_remark")) {
      errmsg("說明不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }
    sqlWhere = " where card_note= ? and area_type= ? and prd_attrib = ? and nvl(mod_seqno,0) = ? ";
    Object[] parms = new Object[] {cardNote, areaType,prdAttrib, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("CCA_PRD_TYPE_INTR", sqlWhere,parms)) {
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into CCA_PRD_TYPE_INTR (" + " card_note, " // 1
        + " start_date, " + " end_date, area_type, prd_attrib, " + " prd_remark, " // 5
        + " int_n_lmt_amt_month, " + " int_n_lmt_amt_time, " + " int_n_lmt_cnt_month, "
        + " int_n_lmt_cnt_day, " + " int_c_lmt_amt_month, " // 10
        + " int_c_lmt_amt_time, " + " int_c_lmt_cnt_month, " + " int_c_lmt_cnt_day, "
        + " int_lmt_amt_mon, " // 14
        + " crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?" + ",?,?,?,?,?" + ",?,?,?,? " + ",to_char(sysdate,'yyyymmdd'),? "
        + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {cardNote, // 1
        wp.itemStr("start_date"), wp.itemStr("end_date"), areaType, prdAttrib, wp.itemStr("prd_remark"),
        wp.itemNum("N_amt_mon") * 100, wp.itemNum("N_amt_time") * 100,
        wp.itemNum("N_cnt_mon") * 100, wp.itemNum("N_cnt_day") * 100, wp.itemNum("C_amt_mon") * 100,
        wp.itemNum("C_amt_time") * 100, wp.itemNum("C_cnt_mon") * 100,
        wp.itemNum("C_cnt_day") * 100, wp.itemNum("lmt_amt_mon") * 100, wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm")};

    // ddd(is_sql,param);
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
    strSql = "update CCA_PRD_TYPE_INTR set " + " start_date =?, " + " end_date =?, "
        + " prd_remark =?, " + " int_n_lmt_amt_month =?, " + " int_n_lmt_amt_time =?, "
        + " int_n_lmt_cnt_month =?, " + " int_n_lmt_cnt_day =?, " + " int_c_lmt_amt_month =?, "
        + " int_c_lmt_amt_time =?, " + " int_c_lmt_cnt_month =?, " + " int_c_lmt_cnt_day =?, "
        + " int_lmt_amt_mon =?, " + " mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 where card_note= ? and area_type= ? and prd_attrib = ? and nvl(mod_seqno,0) = ?" ;
    Object[] param = new Object[] {wp.itemStr("start_date"), wp.itemStr("end_date"),
        wp.itemStr("prd_remark"), wp.itemNum("n_amt_mon") * 100, wp.itemNum("n_amt_time") * 100,
        wp.itemNum("n_cnt_mon") * 100, wp.itemNum("n_cnt_day") * 100, wp.itemNum("c_amt_mon") * 100,
        wp.itemNum("c_amt_time") * 100, wp.itemNum("c_cnt_mon") * 100,
        wp.itemNum("c_cnt_day") * 100, wp.itemNum("lmt_amt_mon") * 100, wp.loginUser,
        wp.itemStr("mod_pgm"),cardNote, areaType,prdAttrib, wp.itemNum("mod_seqno")};

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
    strSql = "delete CCA_PRD_TYPE_INTR where card_note= ? and area_type= ? and prd_attrib = ? and nvl(mod_seqno,0) = ?" ;
    Object[] param = new Object[] {cardNote, areaType,prdAttrib, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql,param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
