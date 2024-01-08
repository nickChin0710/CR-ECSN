/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-21  V1.00.01  ryan       program initial                            *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0430Func extends FuncEdit {
  String kk1CardNo = "";
  String kk2CrtDate = "";

  public Crdm0430Func(TarokoCommon wr) {
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
      // 檢查新增資料是否重複
      kk1CardNo = wp.itemStr("kk_card_no");
      kk2CrtDate = wp.itemStr("crt_date");
      String lsSql =
          "select count(*) as tot_cnt from crd_jcic_card where card_no = ? and crt_date = ?";
      Object[] param = new Object[] {kk1CardNo, kk2CrtDate};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增,請從新查詢");
        return;
      }
    } else {
      kk1CardNo = wp.itemStr("card_no");
      kk2CrtDate = wp.itemStr("crt_date");
      // -other modify-
      sqlWhere = " where 1=1 and card_no = ?  and crt_date=?" + " and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {kk1CardNo, kk2CrtDate, wp.modSeqno()};
      if (this.isOtherModify("crd_jcic_card", sqlWhere, param)) {
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

    String lsSql = "select id_p_seqno from crd_idno where 1=1 and id_no = :ex_id_no";
    setString("ex_id_no", wp.itemStr("id_no"));
    sqlSelect(lsSql);
    String idPSeqno = colStr("id_p_seqno");

    lsSql = "select id_p_seqno from crd_idno where 1=1 and id_no = :major_id";
    setString("major_id", wp.itemStr("major_id"));
    sqlSelect(lsSql);
    String majorIdPSeqno = colStr("id_p_seqno");

    strSql = "insert into crd_jcic_card (" + "card_no, " + "id_p_seqno, " + "chi_name,"
        + "eng_name, " + " jcic_card_type, " + " card_type, " + " sup_flag, " + " card_since, "
        + " m_card_no, " + " m_id_p_seqno, "// 10
        + " m_relation, " + " current_code, " + " credit_lmt, " + " credit_flag, "
        + " oppost_date, " + " oppost_reason, " + " payment_date, " + " risk_amt, "
        + " debit_trans_code, " + " update_date, " // 20
        + " bill_type_flag, " + " rela_id, " + " old_chi_name, " + " trans_type, "// 24
        + " crt_date,crt_user,mod_time,mod_user,mod_pgm,mod_seqno " + " ) values ( "
        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, " + " ?,?,sysdate,?,?,?) ";

    // -set ?value-

    Object[] param = new Object[] {kk1CardNo, idPSeqno, wp.itemStr("chi_name"),
        wp.itemStr("eng_name"), wp.itemStr("jcic_card_type"), wp.itemStr("card_type"),
        wp.itemStr("sup_flag"), wp.itemStr("card_since"), wp.itemStr("m_card_no"), majorIdPSeqno,
        wp.itemStr("m_relation"), wp.itemStr("current_code"), wp.itemNum("credit_lmt"),
        wp.itemStr("credit_flag"), wp.itemStr("oppost_date"), wp.itemStr("oppost_reason"),
        wp.itemStr("payment_date"), wp.itemNum("risk_amt"), wp.itemStr("debit_trans_code"),
        wp.itemStr("update_date"), wp.itemStr("bill_type_flag"), wp.itemStr("rela_id"),
        wp.itemStr("old_chi_name"), wp.itemStr("trans_type"), kk2CrtDate, wp.loginUser,
        wp.loginUser, wp.modPgm(), wp.modSeqno()};

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
    String lsSql = "select id_p_seqno from crd_idno where 1=1 and id_no = :major_id";
    setString("major_id", wp.itemStr("major_id"));
    sqlSelect(lsSql);
    String majorIdPSeqno = colStr("id_p_seqno");

    strSql = " update crd_jcic_card set " + " chi_name=?, " + " eng_name=?, "
        + " jcic_card_type=?, " + " card_type=?, " + " sup_flag=?, " + " card_since=?, "
        + " m_card_no=?, " + " m_id_p_seqno=?, " + " m_relation=?, " + " current_code=?, "
        + " credit_lmt=?, " + " credit_flag=?, " + " oppost_date=?, " + " oppost_reason=?, "
        + " payment_date=?, " + " risk_amt=?, " + " debit_trans_code=?, " + " update_date=?, "
        + " bill_type_flag=?, " + " rela_id=?, " + " trans_type=?, "
        // + " crt_date=?, "
        + " mod_user =?, " + " mod_time=sysdate, " + " mod_pgm =? "
        + " ,mod_seqno =nvl(mod_seqno,0)+1 " + " where 1=1 and card_no = ? and crt_date = ? ";

    Object[] param =
        new Object[] {wp.itemStr("chi_name"), wp.itemStr("eng_name"), wp.itemStr("jcic_card_type"),
            wp.itemStr("card_type"), wp.itemStr("sup_flag"), wp.itemStr("card_since"),
            wp.itemStr("m_card_no"), majorIdPSeqno, wp.itemStr("m_relation"),
            wp.itemStr("current_code"), wp.itemNum("credit_lmt"), wp.itemStr("credit_flag"),
            wp.itemStr("oppost_date"), wp.itemStr("oppost_reason"), wp.itemStr("payment_date"),
            wp.itemNum("risk_amt"), wp.itemStr("debit_trans_code"), wp.itemStr("update_date"),
            wp.itemStr("bill_type_flag"), wp.itemStr("rela_id"), wp.itemStr("trans_type"),
            // wp.item_ss("crt_date"),
            wp.loginUser, wp.modPgm(), kk1CardNo, kk2CrtDate};
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
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

    strSql = "delete crd_jcic_card " + sqlWhere;
    Object[] param = new Object[] {kk1CardNo, kk2CrtDate, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }

}
