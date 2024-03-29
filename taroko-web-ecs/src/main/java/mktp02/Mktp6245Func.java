/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/03/30  V1.00.01   Allen Ho      Initial                              *
 * 111/11/28  V1.00.02  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package mktp02;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6245Func extends busi.FuncProc
{
  private final String PROGNAME = "紅利特殊贈品資料檔處理程式110/03/30 V1.00.01";
  String kk1,kk2;
  String approveTabName = "mkt_spec_gift";
  String controlTabName = "mkt_spec_gift_t";

  public Mktp6245Func(TarokoCommon wr)
  {
    wp = wr;
    this.conn = wp.getConn();
  }
  // ************************************************************************
  @Override
  public int querySelect()
  {
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
  public int dataProc() {
    return rc;
  }
  // ************************************************************************
  public int dbInsertA4() throws Exception
  {
    rc = dbSelectS4();
    if (rc!=1) return rc;
    strSql= " insert into  " + approveTabName + " ("
            + " gift_no, "
            + " gift_group, "
            + " gift_name, "
            + " gift_type, "
            + " disable_flag, "
            + " cash_value, "
            + " effect_months, "
            + " vendor_no, "
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " mod_time, "
            + " mod_user, "
            + " mod_seqno, "
            + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,"
            + "?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "?,"
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + "?,"
            + "?,"
            + " ?) ";

    Object[] param =new Object[]
            {
                    colStr("gift_no"),
                    colStr("gift_group"),
                    colStr("gift_name"),
                    colStr("gift_type"),
                    colStr("disable_flag"),
                    colStr("cash_value"),
                    colStr("effect_months"),
                    colStr("vendor_no"),
                    "Y",
                    wp.loginUser,
                    colStr("crt_date"),
                    colStr("crt_user"),
                    wp.sysDate + wp.sysTime,
                    wp.loginUser,
                    colStr("mod_seqno"),
                    wp.modPgm()
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("新增資料 "+ controlTabName +" 失敗");

    return rc;
  }
  // ************************************************************************
  public int dbSelectS4() throws Exception
  {
    String procTabName="";
    procTabName = controlTabName;
    strSql= " select "
            + " gift_no, "
            + " gift_group, "
            + " gift_name, "
            + " gift_type, "
            + " disable_flag, "
            + " cash_value, "
            + " effect_months, "
            + " vendor_no, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
            + " from " + procTabName
            + " where rowid = ? ";

    Object[] param =new Object[]
            {
                    wp.itemRowId("wprowid")
            };

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

    return rc;
  }
  // ************************************************************************
  public int dbUpdateU4() throws Exception
  {
    rc = dbSelectS4();
    if (rc!=1) return rc;
    String aprFlag = "Y";
    strSql= "update " + approveTabName + " set "
            + "gift_name = ?, "
            + "gift_type = ?, "
            + "disable_flag = ?, "
            + "cash_value = ?, "
            + "effect_months = ?, "
            + "vendor_no = ?, "
            + "crt_user  = ?, "
            + "crt_date  = ?, "
            + "apr_user  = ?, "
            + "apr_date  = to_char(sysdate,'yyyymmdd'), "
            + "apr_flag  = ?, "
            + "mod_user  = ?, "
            + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "mod_pgm   = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1 "
            + "where 1     = 1 "
            + "and   gift_no  = ? "
            + "and   gift_group  = ? "
    ;

    Object[] param =new Object[]
            {
                    colStr("gift_name"),
                    colStr("gift_type"),
                    colStr("disable_flag"),
                    colStr("cash_value"),
                    colStr("effect_months"),
                    colStr("vendor_no"),
                    colStr("crt_user"),
                    colStr("crt_date"),
                    wp.loginUser,
                    aprFlag,
                    colStr("mod_user"),
                    colStr("mod_time"),
                    colStr("mod_pgm"),
                    colStr("gift_no"),
                    colStr("gift_group")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    return rc;
  }
  // ************************************************************************
  public int dbDeleteD4() throws Exception
  {
    rc = dbSelectS4();
    if (rc!=1) return rc;
    strSql = "delete " + approveTabName + " "
            + "where 1 = 1 "
            + "and gift_no = ? "
            + "and gift_group = ? "
    ;

    Object[] param =new Object[]
            {
                    colStr("gift_no"),
                    colStr("gift_group")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbDelete() throws Exception
  {
    strSql = "delete " + controlTabName + " "
            + "where rowid = ?";

    Object[] param =new Object[]
            {
                    wp.itemRowId("wprowid")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (sqlRowNum <= 0)
    {
      errmsg("刪除 "+ controlTabName +" 錯誤");
      return(-1);
    }

    return rc;
  }
// ************************************************************************

}  // End of class
