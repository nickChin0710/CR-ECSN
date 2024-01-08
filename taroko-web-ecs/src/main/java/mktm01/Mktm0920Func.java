/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/04/04  V1.00.01   machao      Initial                                *         
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0920Func extends FuncEdit {
  private final String PROGNAME = "金庫幣活動回饋參數設定112/04/04 V1.00.01";
  String mchtGroupId;
  String orgControlTabName = "MKT_GOLDBILL_PARM";
  String controlTabName = "MKT_GOLDBILL_PARM_T";

  public Mktm0920Func(TarokoCommon wr) {
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
    String procTabName = "";
    procTabName = wp.itemStr("control_tab_name");
    strSql = " select " + " active_code, " + "active_name," + "stop_flag," + "stop_date," + "active_type,"
    		+ "purchase_date_s," + "purchase_date_e," + "active_date_s," + "active_date_e,"
            + "feedback_cycle," + "feedback_dd," + "aud_type,"
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      mchtGroupId = wp.itemStr("active_code");
    } else {
      mchtGroupId = wp.itemStr("active_code");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (mchtGroupId.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where active_code = ? ";
          Object[] param = new Object[] {mchtGroupId};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[活動代碼] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (mchtGroupId.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where active_code = ? ";
        Object[] param = new Object[] {mchtGroupId};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[活動代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if (this.ibUpdate) {
      strSql = "select count(*) as qua from MKT_GOLDBILL_PARM_T where active_code = ?";
      Object[] param1 = new Object[] {wp.itemStr("active_code")};
      sqlSelect(strSql, param1);
      int qua1 = Integer.parseInt(colStr("qua"));
      if (qua1 == 0) {
        errmsg("活動代碼不存在，不可更改!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("active_name")) {
        errmsg("活動說明： 不可空白");
        return;
      }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into  "
            + controlTabName
            + " ("
            + " active_code, "
            + " active_name, "
            + " stop_flag, "
            + " stop_date, "
            + " active_type, "
            + " purchase_date_s, "
            + " purchase_date_e, "
            + " active_date_s, "
            + " active_date_e, "
            + " feedback_cycle, "
            + " feedback_dd, "
            + " aud_type, "
            + " crt_date, "
            + " crt_user, "
            + " mod_seqno, "
            + " mod_time,"
            + "mod_user,"
            + "mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "sysdate,?,?)";

    Object[] param = new Object[] {
            mchtGroupId,
            wp.itemStr("active_name"),
            wp.itemStr("stop_flag"),
            wp.itemStr("stop_date"),
            wp.itemStr("active_type"),
            wp.itemStr("purchase_date_s"),
            wp.itemStr("purchase_date_e"),
            wp.itemStr("active_date_s"),
            wp.itemStr("active_date_e"),
            wp.itemStr("feedback_cycle"),
            wp.itemStr("feedback_dd"),
            wp.itemStr("aud_type"),
            wp.loginUser,
            wp.modSeqno(),
            wp.loginUser,
            wp.modPgm()
    };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************

  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

  strSql = "update "
          + controlTabName
          + " set "
          + "active_name = ?, "
          + "stop_flag = ?, "
          + "stop_date = ?, "
          + "active_type = ?, "
          + "purchase_date_s = ?, "
          + "purchase_date_e = ?, "
          + "active_date_s = ?, "
          + "active_date_e = ?, "
          + "feedback_cycle = ?, "
          + "feedback_dd = ?, "
          + "aud_type = ?, "
          + "crt_user  = ?, "
          + "crt_date  = to_char(sysdate,'yyyymmdd'), "
          + "mod_user  = ?, "
          + "mod_seqno = nvl(mod_seqno,0)+1, "
          + "mod_time  = sysdate, "
          + "mod_pgm   = ? "
          + "where active_code = ? "
          + "and   mod_seqno = ? ";

  Object[] param = new Object[] {
		  wp.itemStr("active_name"),
          wp.itemStr("stop_flag"),
          wp.itemStr("stop_date"),
          wp.itemStr("active_type"),
          wp.itemStr("purchase_date_s"),
          wp.itemStr("purchase_date_e"),
          wp.itemStr("active_date_s"),
          wp.itemStr("active_date_e"),
          wp.itemStr("feedback_cycle"),
          wp.itemStr("feedback_dd"),
          wp.itemStr("aud_type"),
          wp.loginUser,
          wp.loginUser,
          wp.itemStr("mod_pgm"),
          wp.itemStr("active_code"),
          wp.itemNum("mod_seqno")
  };

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
    rc = dataSelect();
    if (rc != 1)
      return rc;
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
