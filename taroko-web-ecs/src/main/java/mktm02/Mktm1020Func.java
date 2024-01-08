/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1020Func extends FuncEdit {
  private String PROGNAME = "市區停車手KEY資料審核作業處理程式108/12/12 V1.00.01";
  String tranSeqno;
  String orControlTabName = "mkt_dodo_resp";
  String controlTabName = "mkt_dodo_resp_t";

  public Mktm1020Func(TarokoCommon wr) {
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
    strSql = " select " + " verify_flag, " + " verify_remark, " + " acct_type, " + " card_no, "
        + " park_vendor, " + " station_id, " + " err_code, " + " park_date_s, " + " park_time_s, "
        + " park_date_e, " + " park_time_e, " + " park_hr, " + " free_hr, " + " use_bonus_hr, "
        + " use_point, " + " act_use_point, " + " act_charge_amt, " + " imp_date, " + " file_name, "
        + " manual_reason, " + " id_p_seqno, " + " p_seqno, " + " tran_date, " + " tran_time, "
        + " pass_type, " + " data_Date, "
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
      tranSeqno = wp.itemStr("tran_seqno");
    } else {
      tranSeqno = wp.itemStr("tran_seqno");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (tranSeqno.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orControlTabName + " where tran_seqno = ? ";
          Object[] param = new Object[] {tranSeqno};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[交易序號] 不可重複(" + orControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (tranSeqno.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where tran_seqno = ? ";
        Object[] param = new Object[] {tranSeqno};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[交易序號] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if (this.ibAdd) {
      if ((wp.itemStr("aud_type").equals("D"))
          && (wp.itemStr("control_tab_name").equals(orControlTabName))) {
        errmsg("轉入資料, 只可異動不可刪除 !");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if ((wp.itemStr("apr_date").length() != 0) && (wp.itemStr("apr_user").length() != 0)) {
        errmsg("該筆資料已覆核, 不可異動!");
        return;
      }
    }
    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("verify_flag")) {
        errmsg("審核結果 不可空白");
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


    strSql = " insert into  " + controlTabName + " (" + " tran_seqno, " + " aud_type, "
        + " verify_flag, " + " verify_remark, " + " acct_type, " + " card_no, " + " park_vendor, "
        + " station_id, " + " err_code, " + " park_date_s, " + " park_time_s, " + " park_date_e, "
        + " park_time_e, " + " park_hr, " + " free_hr, " + " use_bonus_hr, " + " use_point, "
        + " act_use_point, " + " act_charge_amt, " + " imp_date, " + " file_name, "
        + " manual_reason, " + " id_p_seqno, " + " p_seqno, " + " tran_date, " + " tran_time, "
        + " pass_type, " + " data_Date, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {tranSeqno, wp.itemStr("aud_type"), wp.itemStr("verify_flag"),
        wp.itemStr("verify_remark"), wp.itemStr("acct_type"), wp.itemStr("card_no"),
        wp.itemStr("park_vendor"), wp.itemStr("station_id"), wp.itemStr("err_code"),
        colStr("park_date_s"), wp.itemStr("park_time_s"), colStr("park_date_e"),
        wp.itemStr("park_time_e"), wp.itemNum("park_hr"), wp.itemNum("free_hr"),
        wp.itemNum("use_bonus_hr"), wp.itemNum("use_point"), wp.itemNum("act_use_point"),
        wp.itemNum("act_charge_amt"), wp.itemStr("imp_date"), wp.itemStr("file_name"),
        wp.itemStr("manual_reason"), colStr("id_p_seqno"), colStr("p_seqno"), colStr("tran_date"),
        colStr("tran_time"), colStr("pass_type"), colStr("data_DAte"), wp.loginUser, wp.modSeqno(),
        wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

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

    strSql = "update " + controlTabName + " set " + "verify_flag = ?, " + "verify_remark = ?, "
        + "crt_date = ?, " + "crt_user = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("verify_flag"), wp.itemStr("verify_remark"),
        wp.sysDate, wp.loginUser, wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

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
