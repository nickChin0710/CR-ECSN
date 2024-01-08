/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-24  V1.00.02  Justin         parameterize sql
******************************************************************************/
package ptrm02;
/* 系統參數對照表維護 V.2018-0322
 * 2018-0322:	JH		++id_code,id_code2
 * 
 * */
import busi.FuncEdit;

public class Ptrm8020Func extends FuncEdit {
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
    strSql = "Delete ptr_sys_idtab" + " where wf_type = ? ";
    setString(wp.itemStr("A_wf_key"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("Delete ptr_sys_idtab err; " + getMsg());
      rc = -1;
    }

    return rc;
  }

}
