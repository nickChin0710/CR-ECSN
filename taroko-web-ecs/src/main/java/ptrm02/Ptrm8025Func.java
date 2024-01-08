/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-04-20  V1.00.00  Justin     add the program based on ptrm8020          *
* 111-04-22  V1.00.01  Justin     調整新增資料方式                           *
******************************************************************************/
package ptrm02;
/* 系統參數對照表維護 */
import busi.FuncEdit;

public class Ptrm8025Func extends FuncEdit {
 // String kk1 = "", kk2 = "";

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
  public void dataCheck() {}

  @Override
  public int dbInsert() {
    msgOK();

    strSql = "insert into ptr_sys_idtab (" + " wf_type, " // 1
        + " wf_id, " + " wf_desc, " + " wf_useredit, " + " id_code, id_code2,"
        + " mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?" + ",?,?"
        + ",sysdate,?,?,1" + " )";;
    Object[] param = new Object[] {wp.itemStr("A_wf_key"), // 1
        varsStr("wf_id"), varsStr("wf_desc"), "Y", varsStr("id_code"), varsStr("id_code2"),
        this.modUser, this.modPgm};

    this.sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("Insert ptr_sys_idtab error; " + getMsg());
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    msgOK();
    strSql = "Delete ptr_sys_idtab where wf_type = ? AND wf_id = ? ";
    setString(wp.itemStr("A_wf_key"));
    setString(varsStr("wf_id"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete ptr_sys_idtab err; " + getMsg());
      rc = -1;
    }

    return rc;
  }

}
