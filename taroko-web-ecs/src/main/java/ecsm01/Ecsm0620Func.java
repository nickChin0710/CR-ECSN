/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-06-21  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0620Func extends FuncEdit {

  public Ecsm0620Func(TarokoCommon wr) {
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

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;


    strSql = " insert into  ecs_batch_setting ( shell_id, " + " schedule_job_id, "
        + " priority_level, " + " pgm_id, " + " pgm_desc, " + " key_parm1, " + " key_parm2, "
        + " key_parm3, " + " key_parm4, " + " key_parm5, " + " wait_flag, "
        + " repeat_code, " + " normal_code, " + " call_duty_ind, " + " mod_time,mod_user,mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate,?,?)";

    Object[] param = new Object[] {wp.itemStr("shell_id"), wp.itemStr("schedule_job_id"),
        wp.itemStr("priority_level"), wp.itemStr("pgm_id"), wp.itemStr("pgm_desc"),
        wp.itemStr("key_parm1"), wp.itemStr("key_parm2"), wp.itemStr("key_parm3"),
        wp.colStr("key_parm4"), wp.colStr("key_parm5"), wp.colStr("wait_flag"),
        wp.colStr("repeat_code"), wp.itemNvl("normal_code","0"), wp.itemStr("call_duty_ind"),
        wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update ecs_batch_setting set " + "shell_id = ?, " + "schedule_job_id = ?, "
        + "priority_level = ?, " + "pgm_id = ?, " + "pgm_desc = ?, " + "key_parm1 = ?, "
        + "key_parm2 = ?, " + "key_parm3 = ?, " + "key_parm4 = ?, " + "key_parm5 = ?, "
        + "wait_flag = ?, " + "repeat_code = ?, " + "normal_code  = ?, "
        + "call_duty_ind = ?, " + "mod_user = ?, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " ;

    Object[] param = new Object[] {wp.itemStr("shell_id"), wp.itemStr("schedule_job_id"),
        wp.itemStr("priority_level"), wp.itemStr("pgm_id"), wp.itemStr("pgm_desc"),
        wp.itemStr("key_parm1"), wp.itemStr("key_parm2"), wp.itemStr("key_parm3"),
        wp.colStr("key_parm4"), wp.colStr("key_parm5"),
        wp.colStr("wait_flag"), wp.itemStr("repeat_code"), wp.itemNvl("normal_code","0"),
        wp.itemStr("call_duty_ind"),wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid")};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete ecs_batch_setting where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    
    return rc;
  }
  // ************************************************************************

} // End of class
