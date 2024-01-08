/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                            
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *                                              *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0070Func extends FuncEdit {
  private String progname = "線上啟動批次程式設定處理程式108/01/29 V1.00.01";
  String pgmCode, pgmCodeSeq;
  String controlTabName = "ecs_setbatch";

  public Ecsm0070Func(TarokoCommon wr) {
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
      pgmCode = wp.itemStr("kk_pgm_code");
      if (empty(pgmCode)) {
        errmsg("程式代碼: 不可空白");
        return;
      }
      pgmCodeSeq = wp.itemStr("kk_pgm_code_seq");
    } else {
      pgmCode = wp.itemStr("pgm_code");
      pgmCodeSeq = wp.itemStr("pgm_code_seq");
    }
    if (this.ibAdd)
      if (pgmCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where pgm_code = ? "
            + " and   pgm_code_seq = ? ";
        Object[] param = new Object[] {pgmCode, pgmCodeSeq};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[程式代碼:][程式序號:] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
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


    strSql = " insert into  " + controlTabName + " (" + " pgm_code, " + " parm1, " + " parm2, "
        + " parm3, " + " parm4, " + " from_mark, " + " pgm_code_seq, " + " apr_date, "
        + " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?," + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {pgmCode, wp.itemStr("parm1"), wp.itemStr("parm2"),
        wp.itemStr("parm3"), wp.itemStr("parm4"), "1", wp.sysDate + wp.sysTime,
        wp.itemStr("apr_user"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "parm1 = ?, " + "parm2 = ?, " + "parm3 = ?, "
        + "parm4 = ?, " + "from_mark = '1', " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("parm1"), wp.itemStr("parm2"), wp.itemStr("parm3"),
        wp.itemStr("parm4"), wp.itemStr("apr_user"), wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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
