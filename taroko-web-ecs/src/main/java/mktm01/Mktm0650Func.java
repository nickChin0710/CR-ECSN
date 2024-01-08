/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/13  V1.00.01   Ray Ho        Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0650Func extends FuncEdit {
  private String PROGNAME = "WEB登錄代碼參數檔維護作業處理程式108/08/13 V1.00.01";
  String webRecordNo;
  String controlTabName = "web_activity_parm";

  public Mktm0650Func(TarokoCommon wr) {
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
      webRecordNo = wp.itemStr("kk_web_record_no");
    } else {
      webRecordNo = wp.itemStr("web_record_no");
    }
    if (this.ibAdd)
      if (webRecordNo.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where web_record_no = ? ";
        Object[] param = new Object[] {webRecordNo};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[專案代號] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("web_date_s").length() == 0) {
        errmsg("[活動期間:起日]必須輸入 !");
        return;
      }
      if (wp.itemStr("web_date_e").length() == 0) {
        errmsg("[活動期間:迄日]必須輸入 !");
        return;
      }
      if (wp.itemStr("record_desc").length() == 0) {
        errmsg("[專案活動說明]必須輸入 !");
        return;
      }
      if (wp.itemStr("record_name").length() == 0) {
        errmsg("[專案活動名稱]必須輸入 !");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemEmpty("web_date_s") && (!wp.itemEmpty("WEB_DATE_E")))
        if (wp.itemStr("web_date_s").compareTo(wp.itemStr("WEB_DATE_E")) > 0) {
          errmsg(
              "活動期間：[" + wp.itemStr("web_date_s") + "]>[" + wp.itemStr("WEB_DATE_E") + "] 起迄值錯誤!");
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


    strSql = " insert into  " + controlTabName + " (" + " web_record_no, " + " record_name, "
        + " web_date_s, " + " web_date_e, " + " record_desc, " + " apr_date, " + " apr_flag, "
        + " apr_user, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {webRecordNo, wp.itemStr("record_name"), wp.itemStr("web_date_s"),
        wp.itemStr("web_date_e"), wp.itemStr("record_desc"), "Y", wp.itemStr("approval_user"),
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

    strSql = "update " + controlTabName + " set " + "record_name = ?, " + "web_date_s = ?, "
        + "web_date_e = ?, " + "record_desc = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param =
        new Object[] {wp.itemStr("record_name"), wp.itemStr("web_date_s"), wp.itemStr("web_date_e"),
            wp.itemStr("record_desc"), wp.loginUser, wp.itemStr("approval_user"), wp.loginUser,
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

} // End of class
