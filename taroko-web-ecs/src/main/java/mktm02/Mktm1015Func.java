/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/25  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1015Func extends FuncEdit {
  private String PROGNAME = "市區停車廠商參數檔處理程式108/06/25 V1.00.01";
  String parkVendor;
  String controlTabame = "mkt_park_parm";

  public Mktm1015Func(TarokoCommon wr) {
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
      parkVendor = wp.itemStr("kk_park_vendor");
      if (empty(parkVendor)) {
        errmsg("廠商代碼 不可空白");
        return;
      }
    } else {
      parkVendor = wp.itemStr("park_vendor");
    }
    if (this.ibAdd)
      if (parkVendor.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabame + " where park_vendor = ? ";
        Object[] param = new Object[] {parkVendor};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[廠商代碼] 不可重複(" + controlTabame + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemStr("park_pwd").equals(wp.itemStr("apr_pwd"))) {
        errmsg("[檔案密碼] 與確認值不一致!");
        return;
      }
    }

    if (checkDecnum(wp.itemStr("charge_hr_amt"), 7, 2) != 0) {
      errmsg("　-生效日起　每小時費用: 格式超出範圍 : [7][2]");
      return;
    }

    if (checkDecnum(wp.itemStr("charge_bef_amt"), 7, 2) != 0) {
      errmsg("　-生效日前　每小時費用: 格式超出範圍 : [7][2]");
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


    strSql = " insert into  " + controlTabame + " (" + " park_vendor, " + " vendor_name, "
        + " ref_ip_code, " + " auto_file_name, " + " key_file_name, " + " park_pwd, "
        + " charge_hr_date, " + " charge_hr_amt, " + " charge_bef_amt, " + " apr_date, "
        + " apr_flag, " + " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {parkVendor, wp.itemStr("vendor_name"), wp.itemStr("ref_ip_code"),
        wp.itemStr("auto_file_name"), wp.itemStr("key_file_name"), wp.itemStr("park_pwd"),
        wp.itemStr("charge_hr_date"), wp.itemNum("charge_hr_amt"), wp.itemNum("charge_bef_amt"),
        "Y", wp.itemStr("approval_user"), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabame + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabame + " set " + "vendor_name = ?, " + "ref_ip_code = ?, "
        + "auto_file_name = ?, " + "key_file_name = ?, " + "park_pwd = ?, " + "charge_hr_date = ?, "
        + "charge_hr_amt = ?, " + "charge_bef_amt = ?, " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("vendor_name"), wp.itemStr("ref_ip_code"),
        wp.itemStr("auto_file_name"), wp.itemStr("key_file_name"), wp.itemStr("park_pwd"),
        wp.itemStr("charge_hr_date"), wp.itemNum("charge_hr_amt"), wp.itemNum("charge_bef_amt"),
        wp.itemStr("approval_user"), wp.loginUser, wp.itemStr("mod_pgm"), wp.itemRowId("rowid"),
        wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabame + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete " + controlTabame + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabame + " 錯誤");

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
