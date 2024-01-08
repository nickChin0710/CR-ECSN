/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/16  V1.00.01   Ray Ho        Initial                              *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
***************************************************************************/
package ptrm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ptrm0700Func extends FuncEdit {
  private String PROGNAME = "檔案瘦身參數維護處理程式108/05/16 V1.00.01";
  String acctType;
  String controlTabName = "ptr_simp_param";

  public Ptrm0700Func(TarokoCommon wr) {
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
      acctType = wp.itemStr("kk_acct_type");
    } else {
      acctType = wp.itemStr("acct_type");
    }
    if (this.ibAdd)
      if (acctType.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where acct_type = ? ";
        Object[] param = new Object[] {acctType};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[帳戶類別] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " acct_type, " + " out_of_months, "
        + " settle_months, " + " apr_date, " + " apr_flag, " + " apr_user, " + " crt_date, "
        + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {acctType, wp.itemNum("out_of_months"), wp.itemNum("settle_months"),
        "Y", wp.itemStr("approval_user"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "out_of_months = ?, " + "settle_months = ?, "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemNum("out_of_months"), wp.itemNum("settle_months"),
        wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"),
        wp.itemNum("mod_seqno")};

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

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

}  // End of class
