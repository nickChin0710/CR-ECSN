/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-28  V1.00.00  yash       program initial                            *
* 109-04-20  V1.00.01  Tanwei       updated for project coding standard      *
*                                                                            *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0077Func extends FuncEdit {
  String mKkEcsPvk2 = "";

  public Ptrm0077Func(TarokoCommon wr) {
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
      // 檢查新增資料是否超過一筆
      String lsSql = "select count(*) as tot_cnt from ptr_keys_table ";
      sqlSelect(lsSql);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");

      }
      return;

    }
    if (this.isAdd()) {

    }

    // -other modify-
    sqlWhere = " where " + " nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {wp.modSeqno()};
    if (this.isOtherModify("ptr_keys_table", sqlWhere, param)) {
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
    strSql = "insert into ptr_keys_table (" + ", ecs_pvk2 " + ", ecs_pvk2_chk " + ", ecs_pvka_chk "
        + ", ecs_pvk4" + ", ecs_pvk4_chk" + ", ecs_pvkb_chk " + ", ecs_pvk6" + ", ecs_pvk6_chk"
        + ", ecs_pvkc_chk " + ", ecs_pvk8 "// 10
        + ", ecs_pvk8_chk " + ", ecs_pvkd_chk " + ", ecs_pvk10  " + ", ecs_pvk10_chk "
        + ", ecs_pvke_chk " + ", ecs_pvk12" + ", ecs_pvk12_chk " + ", ecs_pvkf_chk " + ", ecs_cvk2 "
        + ", ecs_cvk2_chk "// 20
        + ", ebk_zek " + ", ebk_zek_chk " + ", ebk_dek " + ", ebk_dek_chk" + ", ebk_hmack "
        + ", mod_time " + ", mod_user " + ", mod_pgm " + ", mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? " + ",?,?,?,?,? " + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("ecs_pvk2"), wp.itemStr("ecs_pvk2_chk"),
        wp.itemStr("ecs_pvka_chk"), wp.itemStr("ecs_pvk4"), wp.itemStr("ecs_pvk4_chk"),
        wp.itemStr("ecs_pvkb_chk"), wp.itemStr("ecs_pvk6"), wp.itemStr("ecs_pvk6_chk"),
        wp.itemStr("ecs_pvkc_chk"), wp.itemStr("ecs_pvk8"), wp.itemStr("ecs_pvk8_chk"),
        wp.itemStr("ecs_pvkd_chk"), wp.itemStr("ecs_pvk10"), wp.itemStr("ecs_pvk10_chk"),
        wp.itemStr("ecs_pvke_chk"), wp.itemStr("ecs_pvk12"), wp.itemStr("ecs_pvk12_chk"),
        wp.itemStr("ecs_pvkf_chk"), wp.itemStr("ecs_cvk2"), wp.itemStr("ecs_cvk2_chk"),
        wp.itemStr("ebk_zek"), wp.itemStr("ebk_zek_chk"), wp.itemStr("ebk_dek"),
        wp.itemStr("ebk_dek_chk"), wp.itemStr("ebk_hmack"), wp.loginUser, wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_keys_table set " + "  ecs_pvk2 =? " + ", ecs_pvk2_chk =? "
        + ", ecs_pvka_chk =? " + ", ecs_pvk4 =? " + ", ecs_pvk4_chk =? " + ", ecs_pvkb_chk =? "
        + ", ecs_pvk6 =? " + ", ecs_pvk6_chk =? " + ", ecs_pvkc_chk =? " + ", ecs_pvk8 =? "
        + ", ecs_pvk8_chk =? " + ", ecs_pvkd_chk =? " + ", ecs_pvk10 =? " + ", ecs_pvk10_chk =? "
        + ", ecs_pvke_chk =? " + ", ecs_pvk12 =? " + ", ecs_pvk12_chk =? " + ", ecs_pvkf_chk =? "
        + ", ecs_cvk2 =? " + ", ecs_cvk2_chk =? " + ", ebk_zek =? " + ", ebk_zek_chk =? "
        + ", ebk_dek =? " + ", ebk_dek_chk =? " + ", ebk_hmack =? "
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("ecs_pvk2"), wp.itemStr("ecs_pvk2_chk"),
        wp.itemStr("ecs_pvka_chk"), wp.itemStr("ecs_pvk4"), wp.itemStr("ecs_pvk4_chk"),
        wp.itemStr("ecs_pvkb_chk"), wp.itemStr("ecs_pvk6"), wp.itemStr("ecs_pvk6_chk"),
        wp.itemStr("ecs_pvkc_chk"), wp.itemStr("ecs_pvk8"), wp.itemStr("ecs_pvk8_chk"),
        wp.itemStr("ecs_pvkd_chk"), wp.itemStr("ecs_pvk10"), wp.itemStr("ecs_pvk10_chk"),
        wp.itemStr("ecs_pvke_chk"), wp.itemStr("ecs_pvk12"), wp.itemStr("ecs_pvk12_chk"),
        wp.itemStr("ecs_pvkf_chk"), wp.itemStr("ecs_cvk2"), wp.itemStr("ecs_cvk2_chk"),
        wp.itemStr("ebk_zek"), wp.itemStr("ebk_zek_chk"), wp.itemStr("ebk_dek"),
        wp.itemStr("ebk_dek_chk"), wp.itemStr("ebk_hmack"), wp.loginUser, wp.itemStr("mod_pgm"),
        wp.modSeqno()};
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
    strSql = "delete ptr_keys_table " + sqlWhere;
    Object[] param = new Object[] {wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
