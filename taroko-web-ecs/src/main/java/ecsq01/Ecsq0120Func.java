/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/09  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsq01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Ecsq0120Func extends FuncEdit {
  private String progname = "物件權責歸屬檔維護處理程式108/05/09 V1.00.01";
  String controlTabName = "ecs_object_owner";

  public Ecsq0120Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (!this.ibAdd) {
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("unit_code")) {
        errmsg("歸屬單位 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("obj_type")) {
        errmsg("物件類別 不可空白");
        return;
      }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    return 1;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "obj_code = ?, " + "obj_name = ?, "
        + "unit_code = ?, " + "obj_type = ?, " + "owner_code = ?, " + "owner_name = ?, "
        + "owner_tel1 = ?, " + "program_code = ?, " + "obj_comment1 = ?, " + "obj_comment2 = ?, "
        + "obj_comment3 = ?, " + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), "
        + "mod_user  = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, "
        + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param =
        new Object[] {wp.itemStr("obj_code"), wp.itemStr("obj_name"), wp.itemStr("unit_code"),
            wp.itemStr("obj_type"), wp.itemStr("owner_code"), wp.itemStr("owner_name"),
            wp.itemStr("owner_tel1"), wp.itemStr("program_code"), wp.itemStr("obj_comment1"),
            wp.itemStr("obj_comment2"), wp.itemStr("obj_comment3"), wp.itemStr("apr_user"),
            wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    return 1;
  }
  // ************************************************************************

} // End of class
