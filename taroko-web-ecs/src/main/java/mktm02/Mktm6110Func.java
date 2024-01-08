/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/20  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6110Func extends FuncEdit {
  private String PROGNAME = "標準年費參數設定作業處理程式108/08/20 V1.00.01";
  String groupCode, cardType;
  String controlTabName = "ptr_group_card";

  public Mktm6110Func(TarokoCommon wr) {
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
      groupCode = wp.itemStr("kk_group_code");
      if (empty(groupCode)) {
        errmsg("團體代號 不可空白");
        return;
      }
      cardType = wp.itemStr("kk_card_type");
    } else {
      groupCode = wp.itemStr("group_code");
      cardType = wp.itemStr("card_type");
    }
    if (this.ibAdd)
      if (groupCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where group_code = ? ";
        Object[] param = new Object[] {groupCode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[團體代號] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if (checkDecnum(wp.itemStr("sup_rate"), 4, 2) != 0) {
      errmsg("附卡年費按正卡 格式超出範圍 : [4][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("sup_end_rate"), 4, 2) != 0) {
      errmsg("年費按正卡 格式超出範圍 : [4][2]");
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


    strSql = " insert into  " + controlTabName + " (" + " group_code, " + " card_type, "
        + " first_fee, " + " other_fee, " + " sup_rate, " + " sup_end_month, " + " sup_end_rate, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {groupCode, cardType, wp.itemNum("first_fee"), wp.itemNum("other_fee"),
        wp.itemNum("sup_rate"), wp.itemNum("sup_end_month"), wp.itemNum("sup_end_rate"),
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

    strSql = "update " + controlTabName + " set " + "first_fee = ?, " + "other_fee = ?, "
        + "sup_rate = ?, " + "sup_end_month = ?, " + "sup_end_rate = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemNum("first_fee"), wp.itemNum("other_fee"),
        wp.itemNum("sup_rate"), wp.itemNum("sup_end_month"), wp.itemNum("sup_end_rate"),
        wp.loginUser, wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"),
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
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }
  // ************************************************************************

} // End of class
