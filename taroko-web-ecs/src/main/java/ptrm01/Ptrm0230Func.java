/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/07/22  V1.00.01   Ray Ho        Initial                              *
* 109-04-20  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
***************************************************************************/
package ptrm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ptrm0230Func extends FuncEdit {
  private String PROGNAME = "預借現金手續費參數處理程式108/07/22 V1.00.01";
  String cardType, currCode;
  String controlTabName = "ptr_prepaidfee";

  public Ptrm0230Func(TarokoCommon wr) {
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
      cardType = wp.itemStr("kk_card_type");
      currCode = wp.itemStr("kk_curr_code");
    } else {
      cardType = wp.itemStr("card_type");
      currCode = wp.itemStr("curr_code");
    }
    if (this.ibAdd)
      if (cardType.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where card_type = ? "
            + " and   curr_code = ? ";
        Object[] param = new Object[] {cardType, currCode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[卡片種類][幣別] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if (checkDecnum(wp.itemStr("dom_percent"), 3, 2) != 0) {
      errmsg("國內交易收取佔本金百分比: 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("int_percent"), 3, 2) != 0) {
      errmsg("國外交易收取佔本金百分比: 格式超出範圍 : [3][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("swap_percent"), 3, 2) != 0) {
      errmsg("線上結匯收取佔本金百分比: 格式超出範圍 : [3][2]");
      return;
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("fees_txn_code")) {
        errmsg("手續費交易代碼: 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("fees_bill_type")) {
        errmsg("手續費帳單來源: 不可空白");
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


    strSql = " insert into  " + controlTabName + " (" + " card_type, " + " curr_code, "
        + " fees_txn_code, " + " fees_bill_type, " + " dom_fix_amt, " + " dom_percent, "
        + " dom_min_amt, " + " dom_max_amt, " + " int_fix_amt, " + " int_percent, "
        + " int_min_amt, " + " int_max_amt, " + " swap_fix_amt, " + " swap_percent, "
        + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?,"
        + "sysdate,?,?)";

    Object[] param = new Object[] {cardType, currCode, wp.itemStr("fees_txn_code"),
        wp.itemStr("fees_bill_type"), wp.itemNum("dom_fix_amt"), wp.itemNum("dom_percent"),
        wp.itemNum("dom_min_amt"), wp.itemNum("dom_max_amt"), wp.itemNum("int_fix_amt"),
        wp.itemNum("int_percent"), wp.itemNum("int_min_amt"), wp.itemNum("int_max_amt"),
        wp.itemNum("swap_fix_amt"), wp.itemNum("swap_percent"), wp.itemStr("approval_user"),
        wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "fees_txn_code = ?, " + "fees_bill_type = ?, "
        + "dom_fix_amt = ?, " + "dom_percent = ?, " + "dom_min_amt = ?, " + "dom_max_amt = ?, "
        + "int_fix_amt = ?, " + "int_percent = ?, " + "int_min_amt = ?, " + "int_max_amt = ?, "
        + "swap_fix_amt = ?, " + "swap_percent = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("fees_txn_code"), wp.itemStr("fees_bill_type"),
        wp.itemNum("dom_fix_amt"), wp.itemNum("dom_percent"), wp.itemNum("dom_min_amt"),
        wp.itemNum("dom_max_amt"), wp.itemNum("int_fix_amt"), wp.itemNum("int_percent"),
        wp.itemNum("int_min_amt"), wp.itemNum("int_max_amt"), wp.itemNum("swap_fix_amt"),
        wp.itemNum("swap_percent"), wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"),
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
