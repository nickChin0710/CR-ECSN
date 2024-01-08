/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0050Func extends FuncEdit {
  private String progname = "MIS報表參數說明維護處理程式108/12/05 V1.00.01";
  String parmPgm;
  String controlTabName = "ptr_rpt_parm";

  public Ecsm0050Func(TarokoCommon wr) {
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
      parmPgm = wp.itemStr("kk_parm_pgm");
      if (empty(parmPgm)) {
        errmsg("參數代碼 不可空白");
        return;
      }
    } else {
      parmPgm = wp.itemStr("parm_pgm");
    }
    if (this.ibAdd)
      if (parmPgm.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where parm_pgm = ? ";
        Object[] param = new Object[] {parmPgm};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[參數代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("pwd1").equals("Y"))
      wp.itemSet("pwd1", "N");
    if (!wp.itemStr("pwd2").equals("Y"))
      wp.itemSet("pwd2", "N");
    if (!wp.itemStr("pwd3").equals("Y"))
      wp.itemSet("pwd3", "N");
    if (!wp.itemStr("pwd4").equals("Y"))
      wp.itemSet("pwd4", "N");
    if (!wp.itemStr("pwd5").equals("Y"))
      wp.itemSet("pwd5", "N");
    if (!wp.itemStr("pwd6").equals("Y"))
      wp.itemSet("pwd6", "N");
    if (!wp.itemStr("pwd7").equals("Y"))
      wp.itemSet("pwd7", "N");
    if (!wp.itemStr("pwd8").equals("Y"))
      wp.itemSet("pwd8", "N");
    if (!wp.itemStr("pwd9").equals("Y"))
      wp.itemSet("pwd9", "N");
    if (!wp.itemStr("pwd10").equals("Y"))
      wp.itemSet("pwd10", "N");
    if (!wp.itemStr("pwd11").equals("Y"))
      wp.itemSet("pwd11", "N");
    if (!wp.itemStr("pwd12").equals("Y"))
      wp.itemSet("pwd12", "N");
    if (!wp.itemStr("pwd13").equals("Y"))
      wp.itemSet("pwd13", "N");
    if (!wp.itemStr("pwd14").equals("Y"))
      wp.itemSet("pwd14", "N");
    if (!wp.itemStr("pwd15").equals("Y"))
      wp.itemSet("pwd15", "N");


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


    strSql = " insert into  " + controlTabName + " (" + " parm_pgm, " + " main_dept, "
        + " parm_dept, " + " parm_name, " + " pwd1, " + " q1, " + " pwd2, " + " q2, " + " pwd3, "
        + " q3, " + " pwd4, " + " q4, " + " pwd5, " + " q5, " + " pwd6, " + " q6, " + " pwd7, "
        + " q7, " + " pwd8, " + " q8, " + " pwd9, " + " q9, " + " pwd10, " + " q10, " + " pwd11, "
        + " q11, " + " pwd12, " + " q12, " + " pwd13, " + " q13, " + " pwd14, " + " q14, "
        + " pwd15, " + " q15, " + " ps, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {parmPgm, wp.itemStr("main_dept"), wp.itemStr("parm_dept"),
        wp.itemStr("parm_name"), wp.itemStr("pwd1"), wp.itemStr("q1"), wp.itemStr("pwd2"),
        wp.itemStr("q2"), wp.itemStr("pwd3"), wp.itemStr("q3"), wp.itemStr("pwd4"),
        wp.itemStr("q4"), wp.itemStr("pwd5"), wp.itemStr("q5"), wp.itemStr("pwd6"),
        wp.itemStr("q6"), wp.itemStr("pwd7"), wp.itemStr("q7"), wp.itemStr("pwd8"),
        wp.itemStr("q8"), wp.itemStr("pwd9"), wp.itemStr("q9"), wp.itemStr("pwd10"),
        wp.itemStr("q10"), wp.itemStr("pwd11"), wp.itemStr("q11"), wp.itemStr("pwd12"),
        wp.itemStr("q12"), wp.itemStr("pwd13"), wp.itemStr("q13"), wp.itemStr("pwd14"),
        wp.itemStr("q14"), wp.itemStr("pwd15"), wp.itemStr("q15"), wp.itemStr("ps"),
        wp.itemStr("approval_user"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "main_dept = ?, " + "parm_dept = ?, "
        + "parm_name = ?, " + "pwd1 = ?, " + "q1 = ?, " + "pwd2 = ?, " + "q2 = ?, " + "pwd3 = ?, "
        + "q3 = ?, " + "pwd4 = ?, " + "q4 = ?, " + "pwd5 = ?, " + "q5 = ?, " + "pwd6 = ?, "
        + "q6 = ?, " + "pwd7 = ?, " + "q7 = ?, " + "pwd8 = ?, " + "q8 = ?, " + "pwd9 = ?, "
        + "q9 = ?, " + "pwd10 = ?, " + "q10 = ?, " + "pwd11 = ?, " + "q11 = ?, " + "pwd12 = ?, "
        + "q12 = ?, " + "pwd13 = ?, " + "q13 = ?, " + "pwd14 = ?, " + "q14 = ?, " + "pwd15 = ?, "
        + "q15 = ?, " + "ps = ?, " + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("main_dept"), wp.itemStr("parm_dept"),
        wp.itemStr("parm_name"), wp.itemStr("pwd1"), wp.itemStr("q1"), wp.itemStr("pwd2"),
        wp.itemStr("q2"), wp.itemStr("pwd3"), wp.itemStr("q3"), wp.itemStr("pwd4"),
        wp.itemStr("q4"), wp.itemStr("pwd5"), wp.itemStr("q5"), wp.itemStr("pwd6"),
        wp.itemStr("q6"), wp.itemStr("pwd7"), wp.itemStr("q7"), wp.itemStr("pwd8"),
        wp.itemStr("q8"), wp.itemStr("pwd9"), wp.itemStr("q9"), wp.itemStr("pwd10"),
        wp.itemStr("q10"), wp.itemStr("pwd11"), wp.itemStr("q11"), wp.itemStr("pwd12"),
        wp.itemStr("q12"), wp.itemStr("pwd13"), wp.itemStr("q13"), wp.itemStr("pwd14"),
        wp.itemStr("q14"), wp.itemStr("pwd15"), wp.itemStr("q15"), wp.itemStr("ps"), wp.loginUser,
        wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"),
        wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
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

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }
  // ************************************************************************

} // End of class
