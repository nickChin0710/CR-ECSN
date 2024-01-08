/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm4110Func extends FuncEdit {
  private String PROGNAME = "帳單別對映檔處理程式108/01/29 V1.00.01";
  String txnKind, respFlag;
  String controlTabName = "bil_txn_code";

  public Mktm4110Func(TarokoCommon wr) {
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
      txnKind = wp.itemStr("kk_txn_kind");
      respFlag = wp.itemStr("kk_resp_flag");
    } else {
      txnKind = wp.itemStr("txn_kind");
      respFlag = wp.itemStr("resp_flag");
    }
    if (this.ibAdd)
      if (txnKind.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where txn_kind = ? "
            + " and   resp_flag = ? ";
        Object[] param = new Object[] {txnKind, respFlag};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[類別:][回應碼:] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("resp_desc")) {
        errmsg("回應說明: 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("iso_code")) {
        errmsg("ISO代碼: 不可空白");
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


    strSql = " insert into  " + controlTabName + " (" + " txn_kind, " + " resp_flag, "
        + " resp_desc, " + " iso_code, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm "
        + " ) values (" + "?,?,?,?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {txnKind, respFlag, wp.itemStr("resp_desc"), wp.itemStr("iso_code"),
        wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "resp_desc = ?, " + "iso_code = ?, "
        + "mod_user  = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, "
        + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("resp_desc"), wp.itemStr("iso_code"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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
