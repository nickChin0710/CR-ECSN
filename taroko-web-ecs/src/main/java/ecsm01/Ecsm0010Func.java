/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/26  V1.00.01   Allen Ho      Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                         *
***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0010Func extends FuncEdit {
  private final String progname = "系統參考IP位址維護處理程式108/12/26 V1.00.01";
  String refIpCode;
  String controlTabName = "ecs_ref_ip_addr";

  public Ecsm0010Func(TarokoCommon wr) {
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
      refIpCode = wp.itemStr("kk_ref_ip_code");
    } else {
      refIpCode = wp.itemStr("ref_ip_code");
    }
    if (this.ibAdd)
      if (refIpCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where ref_ip_code = ? ";
        Object[] param = new Object[] {refIpCode};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[參考IP代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (!wp.itemStr("t_user_hidewd").equals(wp.itemStr("apr_pwd"))) {
        errmsg("[使用者密碼] 與確認值不一致!");
        return;
      }
      if (!wp.itemStr("t_file_zip_hidewd").equals(wp.itemStr("apr_1_pwd"))) {
        errmsg("[檔案加壓密碼] 與確認值不一致!");
        return;
      }
      if (!wp.itemStr("t_file_unzip_hidewd").equals(wp.itemStr("apr_2_pwd"))) {
        errmsg("[檔案解壓密碼] 與確認值不一致!");
        return;
      }
      if (wp.colStr("hide_ref_code").length() == 0) {
        busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
        comr.setConn(wp);
        wp.colSet("hide_ref_code", comr.getSeqno("ECS_MODSEQ"));
      }
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

      if (wp.itemStr("t_user_hidewd").length() != 0)
        wp.colSet("user_hidewd",
            comm.hideZipData(wp.itemStr("t_user_hidewd"), wp.colStr("hide_ref_code")));

      if (wp.itemStr("t_file_zip_hidewd").length() != 0)
        wp.colSet("file_zip_hidewd",
            comm.hideZipData(wp.itemStr("t_file_zip_hidewd"), wp.colStr("hide_ref_code")));

      if (wp.itemStr("t_file_unzip_hidewd").length() != 0)
        wp.colSet("file_unzip_hidewd",
            comm.hideZipData(wp.itemStr("t_file_unzip_hidewd"), wp.colStr("hide_ref_code")));

    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("ftp_type")) {
        errmsg("FTP類別: 不可空白");
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


    strSql = " insert into  " + controlTabName + " (" + " ref_ip_code, " + " ref_ip, "
        + " ref_name, " + " ftp_type, " + " user_id, " + " trans_type, " + " remote_dir, "
        + " local_dir, " + " port_no, " + " hide_ref_code, " + " user_hidewd, "
        + " file_zip_hidewd, " + " file_unzip_hidewd, " + " apr_date, " + " apr_user, "
        + " crt_date, " + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {refIpCode, wp.itemStr("ref_ip"), wp.itemStr("ref_name"),
        wp.itemStr("ftp_type"), wp.itemStr("user_id"), wp.itemStr("trans_type"),
        wp.itemStr("remote_dir"), wp.itemStr("local_dir"), wp.itemStr("port_no"),
        wp.colStr("hide_ref_code"), wp.colStr("user_hidewd"), wp.colStr("file_zip_hidewd"),
        wp.colStr("file_unzip_hidewd"), wp.itemStr("approval_user"), wp.loginUser, wp.modSeqno(),
        wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "ref_ip = ?, " + "ref_name = ?, "
        + "ftp_type = ?, " + "user_id = ?, " + "trans_type = ?, " + "remote_dir = ?, "
        + "local_dir = ?, " + "port_no = ?, " + "hide_ref_code = ?, " + "user_hidewd = ?, "
        + "file_zip_hidewd = ?, " + "file_unzip_hidewd = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "apr_user  = ?, "
        + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("ref_ip"), wp.itemStr("ref_name"),
        wp.itemStr("ftp_type"), wp.itemStr("user_id"), wp.itemStr("trans_type"),
        wp.itemStr("remote_dir"), wp.itemStr("local_dir"), wp.itemStr("port_no"),
        wp.colStr("hide_ref_code"), wp.colStr("user_hidewd"), wp.colStr("file_zip_hidewd"),
        wp.colStr("file_unzip_hidewd"), wp.loginUser, wp.itemStr("approval_user"), wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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
