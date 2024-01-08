/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-29  V1.00.02  Tanwei       updated for project coding standard
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
***************************************************************************/
package smsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsm0030Func extends FuncEdit {
  private String PROGNAME = "簡訊內容迷戲檔維護處理程式108/12/12 V1.00.01";
  String msgSeqno1;
  String orgControlTabName = "sms_msg_dtl";
  String controlTaName = "sms_msg_dtl_t";

  public Smsm0030Func(TarokoCommon wr) {
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
    strSql = " select " + " cellar_phone, " + " msg_dept, " + " chi_name, " + " id_p_seqno, "
        + " ex_id, " + " msg_userid, " + " msg_id, " + " msg_desc, " + " chi_name_flag, "
        + " create_txt_date, " + " add_mode, " + " cellphone_check_flag, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno " + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTaName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      msgSeqno1 = wp.itemStr("msg_seqno");
    } else {
      msgSeqno1 = wp.itemStr("msg_seqno");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (msgSeqno1.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where msg_seqno = ? ";
          Object[] param = new Object[] {msgSeqno1};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[簡訊流水號] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (msgSeqno1.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTaName + " where msg_seqno = ? "
            + " and   add_mode  !=  'B' ";
        Object[] param = new Object[] {msgSeqno1};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[簡訊流水號] 不可重複(" + controlTaName + ") ,請重新輸入!");
          return;
        }
      }


    if (this.ibAdd) {
      if ((wp.itemStr("apr_date").length() != 0)
          && ((wp.itemStr("aud_type").equals("U")) || (wp.itemStr("aud_type").equals("D")))
          && (wp.itemStr("control_tab_name").equals(orgControlTabName))) {
        errmsg("已覆核資料, 不可異動和刪除 !");
        return;
      }
    }

    strSql = "select id_no " + "from crd_idno " + "where id_no = ? ";
    Object[] param = new Object[] {wp.itemStr("id_no")};
    sqlSelect(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("[身分證號]非本行卡友ID, 請重新輸入!");
      return;
    }
    if (wp.itemStr("msg_seqno").length() == 0) {
      busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
      comr.setConn(wp);
      wp.itemSet("msg_seqno", comr.getSeqno("MKT_MODSEQ"));
      msgSeqno1 = wp.itemStr("msg_seqno");
    }

    if (wp.itemStr("control_tab_name").equals(controlTaName)) {
      wp.itemSet("resend_flag", "N");
      colSet("resend_flag", "N");
    }
    if (this.ibAdd) {
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

      if ((!comm.isNumber(wp.itemStr("cellar_phone")))
          || (wp.itemStr("cellar_phone").length() < 10)) {
        errmsg("行動電話: 不符規則 ");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("cellar_phone")) {
        errmsg("行動電話: 不可空白");
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


    strSql = " insert into  " + controlTaName + " (" + " msg_seqno, " + " aud_type, "
        + " cellar_phone, " + " msg_dept, " + " chi_name, " + " id_p_seqno, " + " ex_id, "
        + " msg_userid, " + " msg_id, " + " msg_desc, " + " chi_name_flag, " + " create_txt_date, "
        + " add_mode, " + " cellphone_check_flag, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?," + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {msgSeqno1, wp.itemStr("aud_type"), wp.itemStr("cellar_phone"),
        wp.itemStr("msg_dept"), wp.itemStr("chi_name"), wp.itemStr("id_p_seqno"),
        wp.itemStr("ex_id"), wp.itemStr("msg_userid"), wp.itemStr("msg_id"), wp.itemStr("msg_desc"),
        wp.itemStr("chi_name_flag"), colStr("create_txt_date"), "O", "Y", wp.loginUser,
        wp.modSeqno(), wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTaName + " 錯誤");

    return rc;
  }

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

    strSql = "update " + controlTaName + " set " + "cellar_phone = ?, " + "msg_dept = ?, "
        + "chi_name = ?, " + "id_p_seqno = ?, " + "ex_id = ?, " + "msg_desc = ?, "
        + "chi_name_flag = ?, " + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), "
        + "mod_user  = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, "
        + "mod_pgm   = ? " + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("cellar_phone"), wp.itemStr("msg_dept"),
        wp.itemStr("chi_name"), wp.colStr("id_p_seqno"), wp.itemStr("ex_id"),
        wp.itemStr("msg_desc"), wp.itemStr("chi_name_flag"), wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm"), wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTaName + " 錯誤");

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

    strSql = "delete " + controlTaName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTaName + " 錯誤");
      return (-1);
    }

    return rc;
  }
  // ************************************************************************

} // End of class
