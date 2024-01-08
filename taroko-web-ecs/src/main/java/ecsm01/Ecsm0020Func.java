/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0020Func extends FuncEdit {
  private String profname = "物件權責歸屬檔維護處理程式108/01/29 V1.00.01";
  String kkObjCode, kkObjType;
  String controlTabName = "ecs_object_owner";

  public Ecsm0020Func(TarokoCommon wr) {
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
    if (this.ibAdd) {
      kkObjCode = wp.itemStr("kk_obj_code");
      if (empty(kkObjCode)) {
        errmsg("物件代碼: 不可空白");
        return;
      }
      kkObjType = wp.itemStr("kk_obj_type");
      if (empty(kkObjType)) {
        errmsg("物件類別 不可空白");
        return;
      }
    } else {
      kkObjCode = wp.itemStr("obj_code");
      kkObjType = wp.itemStr("obj_type");
    }
    if (this.ibAdd)
      if (kkObjCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where obj_code = ? ";
        Object[] param = new Object[] {kkObjCode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[物件代碼:] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("unit_code")) {
        errmsg("歸屬單位 不可空白");
        return;
      }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;


    strSql = " insert into  " + controlTabName + " (" + " obj_code, " + " obj_type, "
        + " obj_name, " + " unit_code, " + " owner_code, " + " owner_name, " + " owner_tel1, "
        + " program_code, " + " obj_comment1, " + " obj_comment2, " + " obj_comment3, "
        + " crt_date, " + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {kkObjCode, kkObjType, wp.itemStr("obj_name"), wp.itemStr("unit_code"),
        wp.itemStr("owner_code"), wp.itemStr("owner_name"), wp.itemStr("owner_tel1"),
        wp.itemStr("program_code"), wp.itemStr("obj_comment1"), wp.itemStr("obj_comment2"),
        wp.itemStr("obj_comment3"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "obj_name = ?, " + "unit_code = ?, "
        + "owner_code = ?, " + "owner_name = ?, " + "owner_tel1 = ?, " + "program_code = ?, "
        + "obj_comment1 = ?, " + "obj_comment2 = ?, " + "obj_comment3 = ?, " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param =
        new Object[] {wp.itemStr("obj_name"), wp.itemStr("unit_code"), wp.itemStr("owner_code"),
            wp.itemStr("owner_name"), wp.itemStr("owner_tel1"), wp.itemStr("program_code"),
            wp.itemStr("obj_comment1"), wp.itemStr("obj_comment2"), wp.itemStr("obj_comment3"),
            wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
