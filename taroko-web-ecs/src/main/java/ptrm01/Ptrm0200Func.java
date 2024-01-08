/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-05  V1.00.00  yash           program initial                        *
* 109-04-20  V1.00.00  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/

package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0200Func extends FuncEdit {
  String groupCode = "";
  String cardType = "";

  public Ptrm0200Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      groupCode = wp.itemStr("kk_group_code");
      cardType = wp.itemStr("kk_card_type");
    } else {
      groupCode = wp.itemStr("group_code");
      cardType = wp.itemStr("card_type");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from Ptr_group_card where group_code = ?  and  card_type= ?";
      Object[] param = new Object[] {groupCode, cardType};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;
    }

    // -other modify-
    sqlWhere = " where group_code= ? and card_type= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {groupCode, cardType, wp.modSeqno()};
    if (this.isOtherModify("Ptr_group_card", sqlWhere, param)) {
      errmsg("請重新查詢 !");
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
    strSql = "insert into Ptr_group_card (" + "  group_code " + ", card_type " + ", name "
        + ", card_mold_flag " + ", service_type " + ", org_cardno_flag " + ", remark "
        + ", cash_limit_rate  " + ", crt_date, crt_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,? "
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {groupCode // 1
        , cardType // 2
        , wp.itemStr("name"), wp.itemStr("card_mold_flag"), wp.itemStr("service_type"),
        wp.itemStr("org_cardno_flag"), wp.itemStr("remark"), wp.itemNum("db_cash_limit_rate") / 100,
        wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update Ptr_group_card set " + "  name =? " + " ,card_mold_flag =? "
        + " ,service_type =? " + " ,org_cardno_flag =? " + " ,remark =? " + " ,cash_limit_rate =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("name"), wp.itemStr("card_mold_flag"),
        wp.itemStr("service_type"), wp.itemStr("org_cardno_flag"), wp.itemStr("remark"),
        wp.itemNum("db_cash_limit_rate") / 100, wp.loginUser, wp.itemStr("mod_pgm"), groupCode,
        cardType, wp.modSeqno()};
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
    strSql = "delete Ptr_group_card " + sqlWhere;
    Object[] param = new Object[] {groupCode, cardType, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  public int dbDelete2() {
    msgOK();
    groupCode = wp.itemStr("group_code");
    cardType = wp.itemStr("card_type");

    // 如果沒有資料回傳成功
    Object[] param = new Object[] {groupCode, cardType};
    if (sqlRowcount("ptr_group_card_dtl", "where group_code = ? and card_type = ? ", param) <= 0)
      return 1;

    strSql = "delete ptr_group_card_dtl where group_code = ? and card_type = ? ";
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      rc = -1;
    }

    return rc;

  }

  public int dbInsert2() {
    msgOK();
    groupCode = wp.itemStr("group_code");
    cardType = wp.itemStr("card_type");

    strSql = "insert into ptr_group_card_dtl (" + " group_code, " // 1
        + " card_type, "
        // + " seqno, "
        + " unit_code, "
        // + " card_item, "
        + " mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?"
        + ", sysdate, ?, ?, 1" + " )";;
    Object[] param = new Object[] {groupCode, // 1
        cardType,
        // vars_num("seqno"),
        varsStr("unit_code"),
        // vars_ss("card_item"),
        wp.loginUser, wp.modPgm()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

}
