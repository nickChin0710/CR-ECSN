/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-27  V1.00.00  yash       program initial                            *
* 109-04-19  V1.00.01  shiyuqi    updated for project coding standard        * 
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0076Func extends FuncEdit {
  String mKkEcsPvk1 = "";

  public Ptrm0076Func(TarokoCommon wr) {
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


    }
    if (this.isAdd()) {
      // 檢查新增資料是否超過一筆
      String lsSql = "select count(*) as tot_cnt from ptr_keys_table ";
      sqlSelect(lsSql);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
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
    strSql = "insert into ptr_keys_table (" + "  ecs_pvk1 " + ", ecs_pvk1_chk " + ", ecs_pvk3"
        + ", ecs_pvk3_chk" + ", ecs_pvk5" + ", ecs_pvk5_chk" + ", ecs_pvk7 " + ", ecs_pvk7_chk "
        + ", ecs_pvk9  " + ", ecs_pvk9_chk "// 10
        + ", ecs_pvk11" + ", ecs_pvk11_chk " + ", racal_ip_addr " + ", racal_port" + ", ecs_cvk1 "
        + ", ecs_cvk1_chk " + ", ecs_csck1" + ", mob_zek_kek1 " + ", mob_zek_kek1_chk"
        + ", mob_zek_dek1"// 20
        + ", mob_zek_dek1_chk" + ", mob_ip_addr " + ", mob_ip_port" + ", mob_version_id"
        + ", acs_zek_kek1 " + ", acs_zek_kek1_chk" + ", acs_ip_addr" + ", acs_ip_port"
        + ", acs_version_id "// 29
        + ", mod_time " + ", mod_user " + ", mod_pgm " + ", mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? " + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("ecs_pvk1"), wp.itemStr("ecs_pvk1_chk"),
        wp.itemStr("ecs_pvk3"), wp.itemStr("ecs_pvk3_chk"), wp.itemStr("ecs_pvk5"),
        wp.itemStr("ecs_pvk5_chk"), wp.itemStr("ecs_pvk7"), wp.itemStr("ecs_pvk7_chk"),
        wp.itemStr("ecs_pvk9"), wp.itemStr("ecs_pvk9_chk"), wp.itemStr("ecs_pvk11"),
        wp.itemStr("ecs_pvk11_chk"), wp.itemStr("racal_ip_addr"),
        wp.itemStr("racal_port").equals("") ? 0 : wp.itemStr("racal_port"), wp.itemStr("ecs_cvk1"),
        wp.itemStr("ecs_cvk1_chk"), wp.itemStr("ecs_csck1"), wp.itemStr("mob_zek_kek1"),
        wp.itemStr("mob_zek_kek1_chk"), wp.itemStr("mob_zek_dek1"), wp.itemStr("mob_zek_dek1_chk"),
        wp.itemStr("mob_ip_addr"), wp.itemStr("mob_ip_port"), wp.itemStr("mob_version_id"),
        wp.itemStr("acs_zek_kek1"), wp.itemStr("acs_zek_kek1_chk"), wp.itemStr("acs_ip_addr"),
        wp.itemStr("acs_ip_port"), wp.itemStr("acs_version_id"), wp.loginUser,
        wp.itemStr("mod_pgm")};
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

    strSql = "update ptr_keys_table set " + " ecs_pvk1 =? " + ", ecs_pvk1_chk =? "
        + ", ecs_pvk3 =? " + ", ecs_pvk3_chk =? " + ", ecs_pvk5 =? " + ", ecs_pvk5_chk =? "
        + ", ecs_pvk7 =? " + ", ecs_pvk7_chk =? " + ", ecs_pvk9 =? " + ", ecs_pvk9_chk =? "
        + ", ecs_pvk11 =? " + ", ecs_pvk11_chk =? " + ", racal_ip_addr =? " + ", racal_port =? "
        + ", ecs_cvk1 =? " + ", ecs_cvk1_chk =? " + ", ecs_csck1 =? " + ", mob_zek_kek1 =? "
        + ", mob_zek_kek1_chk =? " + ", mob_zek_dek1 =? " + ", mob_zek_dek1_chk =? "
        + ", mob_ip_addr =? " + ", mob_ip_port =? " + ", mob_version_id =? " + ", acs_zek_kek1 =? "
        + ", acs_zek_kek1_chk =? " + ", acs_ip_addr =? " + ", acs_ip_port =? "
        + ", acs_version_id =? " + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
        + " , mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("ecs_pvk1"), wp.itemStr("ecs_pvk1_chk"),
        wp.itemStr("ecs_pvk3"), wp.itemStr("ecs_pvk3_chk"), wp.itemStr("ecs_pvk5"),
        wp.itemStr("ecs_pvk5_chk"), wp.itemStr("ecs_pvk7"), wp.itemStr("ecs_pvk7_chk"),
        wp.itemStr("ecs_pvk9"), wp.itemStr("ecs_pvk9_chk"), wp.itemStr("ecs_pvk11"),
        wp.itemStr("ecs_pvk11_chk"), wp.itemStr("racal_ip_addr"),
        wp.itemStr("racal_port").equals("") ? 0 : wp.itemStr("racal_port"), wp.itemStr("ecs_cvk1"),
        wp.itemStr("ecs_cvk1_chk"), wp.itemStr("ecs_csck1"), wp.itemStr("mob_zek_kek1"),
        wp.itemStr("mob_zek_kek1_chk"), wp.itemStr("mob_zek_dek1"), wp.itemStr("mob_zek_dek1_chk"),
        wp.itemStr("mob_ip_addr"), wp.itemStr("mob_ip_port"), wp.itemStr("mob_version_id"),
        wp.itemStr("acs_zek_kek1"), wp.itemStr("acs_zek_kek1_chk"), wp.itemStr("acs_ip_addr"),
        wp.itemStr("acs_ip_port"), wp.itemStr("acs_version_id"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.modSeqno()};
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
