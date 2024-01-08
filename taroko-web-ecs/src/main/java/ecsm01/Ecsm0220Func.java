/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/01/29  V1.00.01   Ray Ho        Initial                              *
* 109-04-27 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0220Func extends FuncEdit
{
  private String progname = "特店規屬類別參數檔維護處理程式108/01/29 V1.00.01";
  String bndataNo;
  String controlTabName = "mkt_bndata_parm";

  public Ecsm0220Func(TarokoCommon wr) {
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
      bndataNo = wp.itemStr("kk_bndata_no");
    } else {
      bndataNo = wp.itemStr("bndata_no");
    }
    if (this.ibAdd)
      if (bndataNo.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where bndata_no = ? ";
        Object[] param = new Object[] {bndataNo};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[物件代碼:] 不可重複(" + controlTabName + ") ,請重新輸入!");
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


    strSql = " insert into  " + controlTabName + " (" + " bndata_no, " + " bndata_name, "
        + " data_type, " + " table_name, " + " p_table_name, " + " d_table_name, "
        + " p_key_column, " + " key_operation, " + " d_key_column, " + " code_column, "
        + " desc_column, " + " key_column_name, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {bndataNo, wp.itemStr("bndata_name"), wp.itemStr("data_type"),
        wp.itemStr("table_name"), wp.itemStr("p_table_name"), wp.itemStr("d_table_name"),
        wp.itemStr("p_key_column"), wp.itemStr("key_operation"), wp.itemStr("d_key_column"),
        wp.itemStr("code_column"), wp.itemStr("desc_column"), wp.itemStr("key_column_name"),
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

    strSql = "update " + controlTabName + " set " + "bndata_name = ?, " + "data_type = ?, "
        + "table_name = ?, " + "p_table_name = ?, " + "d_table_name = ?, " + "p_key_column = ?, "
        + "key_operation = ?, " + "d_key_column = ?, " + "code_column = ?, " + "desc_column = ?, "
        + "key_column_name = ?, " + "mod_user  = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, "
        + "mod_time  = sysdate, " + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("bndata_name"), wp.itemStr("data_type"),
        wp.itemStr("table_name"), wp.itemStr("p_table_name"), wp.itemStr("d_table_name"),
        wp.itemStr("p_key_column"), wp.itemStr("key_operation"), wp.itemStr("d_key_column"),
        wp.itemStr("code_column"), wp.itemStr("desc_column"), wp.itemStr("key_column_name"),
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
