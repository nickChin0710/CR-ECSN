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
public class Mktm6290Func extends FuncEdit {
  private String PROGNAME = "帳戶紅利點數線上調整作業處理程式108/12/12 V1.00.01";
  String tranSeqno;
  String orgControlTabName = "mkt_bonus_dtl";
  String controlTabName = "mkt_bonus_dtl_t";

  public Mktm6290Func(TarokoCommon wr) {
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
    strSql = " select " + " acct_type, " + " p_seqno, " + " id_p_seqno, " + " bonus_type, "
        + " active_code, " + " tran_code, " + " beg_tran_bp, " + " tax_flag, " + " effect_e_date, "
        + " mod_reason, " + " mod_desc, " + " mod_memo, " + " end_tran_bp, " + " active_name, "
        + " tran_pgm, "
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
              "select count(*) as qua " + "from " + orgControlTabName + " where tran_seqno = ? ";
          Object[] param = new Object[] {tranSeqno};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[交易序號] 不可重複(" + orgControlTabName + "), 請重新輸入!");
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
      if (wp.itemStr("control_tab_name").equals(orgControlTabName)) {
        errmsg("已覆核資料, 只可查詢不可異動 !");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("active_code").length() != 0) {
        strSql = "select " + " active_name as active_name " + " from  vmkt_bonus_active_name"
            + " where active_code = ? ";
        Object[] param = new Object[] {wp.itemStr("active_code")};
        sqlSelect(strSql, param);

        wp.itemSet("active_name", colStr("active_name"));
      } else {
        wp.itemSet("active_name", "帳戶紅利點數線上調整");
      }

      if (wp.itemNum("beg_tran_bp") == 0) {
        errmsg("調整點數不可為 0 !");
        return;
      }
    }

    if (wp.itemStr("tran_seqno").length() == 0) {
      busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
      comr.setConn(wp);
      wp.itemSet("tran_seqno", comr.getSeqno("MKT_MODSEQ"));
      tranSeqno = wp.itemStr("tran_seqno");
    }
    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("acct_type")) {
        errmsg("帳戶類別: 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("tran_code")) {
        errmsg("交易類別: 不可空白");
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


    strSql = " insert into  " + controlTabName + " (" + " tran_seqno, " + " acct_type, "
        + " aud_type, " + " p_seqno, " + " id_p_seqno, " + " bonus_type, " + " active_code, "
        + " tran_code, " + " beg_tran_bp, " + " tax_flag, " + " effect_e_date, " + " mod_reason, "
        + " mod_desc, " + " mod_memo, " + " end_tran_bp, " + " active_name, " + " tran_pgm, "
        + " crt_date, " + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,?,?," + "to_char(sysdate,'yyyymmdd'),"
        + "?," + "?," + "sysdate,?,?)";

    Object[] param =
        new Object[] {tranSeqno, wp.itemStr("acct_type"), wp.itemStr("aud_type"), wp.itemStr("p_seqno"),
            wp.itemStr("id_p_seqno"), wp.itemStr("bonus_type"), wp.itemStr("active_code"),
            wp.itemStr("tran_code"), wp.itemNum("beg_tran_bp"), wp.itemStr("tax_flag"),
            wp.itemStr("effect_e_date"), wp.itemStr("mod_reason"), wp.itemStr("mod_desc"),
            wp.itemStr("mod_memo"), wp.itemNum("beg_tran_bp"), wp.itemStr("active_name"),
            wp.modPgm(), wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "acct_type = ?, " + "bonus_type = ?, "
        + "active_code = ?, " + "tran_code = ?, " + "beg_tran_bp = ?, " + "tax_flag = ?, "
        + "effect_e_date = ?, " + "mod_reason = ?, " + "mod_desc = ?, " + "mod_memo = ?, "
        + "end_tran_bp = ?, " + "active_name = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("acct_type"), wp.itemStr("bonus_type"),
        wp.itemStr("active_code"), wp.itemStr("tran_code"), wp.itemNum("beg_tran_bp"),
        wp.itemStr("tax_flag"), wp.itemStr("effect_e_date"), wp.itemStr("mod_reason"),
        wp.itemStr("mod_desc"), wp.itemStr("mod_memo"), wp.itemNum("beg_tran_bp"),
        wp.itemStr("active_name"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
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
