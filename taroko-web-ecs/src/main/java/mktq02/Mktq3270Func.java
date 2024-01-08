/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-12-09  V1.00.01   Machao      Initial                                                                                     *    
***************************************************************************/
package mktq02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq3270Func extends FuncEdit {
  private final String PROGNAME = "高鐵車廂請款明細檔維護處理程式111-12-09  V1.00.01";
  String serialNo;
  String orgControlTabName = "mkt_thsr_uptxn";
  String controlTabName = "mkt_thsr_uptxn_t";

  public Mktq3270Func(TarokoCommon wr) {
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
    strSql = " select " + " auth_flag, " + " trans_date, " + " trans_time, " + " card_no, "
        + " authentication_code, " + " error_desc, " + " error_code, " + " pay_cardid, "
        + " acct_type, " + " group_code, " + " card_type, " + " card_mode, " + " proc_date, "
        + " trans_type, " + " trans_amount, " + " org_serial_no, " + " major_id_p_seqno, "
        + " major_card_no, " + " id_p_seqno, " + " p_seqno, " + " acct_type, " + " group_code, "
        + " card_type, " + " file_date, " + " file_time, " + " file_name, " + " pnr, "
        + " ticket_id, " + " card_mode, " + " issue_date, " + " promote_emp_no, " + " corp_no, "
        + " reg_bank_no, " + " train_no, " + " departure_station_id, " + " arrival_station_id, "
        + " seat_no, " + " depart_date, " + " org_trans_date, " + " station_id, " + " proc_flag, "
        + " trans_amount, " + " issue_station_id, " + " auth_flag, " + " auth_date, "
        + " crt_time, "
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
      serialNo = wp.itemStr("serial_no");
    } else {
      serialNo = wp.itemStr("serial_no");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (serialNo.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where serial_no = ? ";
          Object[] param = new Object[] {serialNo};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[交易流水號] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (serialNo.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where serial_no = ? ";
        Object[] param = new Object[] {serialNo};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[交易流水號] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }


    if (this.ibAdd) {
      if ((wp.itemStr("aud_type").equals("D"))
          && (wp.itemStr("control_tab_name").equals(orgControlTabName))) {
        errmsg("原始資料, 不可刪除!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("auth_flag").equals("N")) {
        errmsg("審核註記不可為[錯誤待處理]!");
        return;
      } else if (wp.itemStr("auth_flag").equals("Y")) {
        if (wp.itemStr("card_no").length() != 0) {
          strSql = " select a.acct_type,b.acct_key,a.p_seqno,a.id_p_seqno,c.chi_name,  "
              + "        a.group_code,a.card_type,a.major_card_no,a.major_id_p_seqno, "
              + "        a.issue_date,a.corp_no,a.reg_bank_no,promote_emp_no "
              + " from crd_card a,act_acno b,crd_idno c " + " where c.id_p_seqno = a.id_p_seqno "
              + " and   b.p_seqno = a.p_seqno " + " and   a.card_no = ? ";

          Object[] param = new Object[] {wp.itemStr("card_no")};
          sqlSelect(strSql, param);

          if (sqlRowNum <= 0) {
            errmsg("卡號[" + wp.itemStr("card_no") + "]不存在 !");
            return;
          }
          if (wp.itemStr("trans_type").equals("R")) {
            strSql = " select mod_seqno  " + " from  mkt_thsr_uptxn " + " where serial_no = ? "
                + " and   trans_type = P' ";

            param = new Object[] {wp.itemStr("org_serial_no")};
            sqlSelect(strSql, param);

            if (sqlRowNum <= 0) {
              errmsg("原始交易流水號[" + wp.itemStr("org_Serial_no") + "] 無法比對不到購票資料 !");
              return;
            }
          }
        } else {
          errmsg("卡號不可為空白 !");
          return;
        }
      }
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("auth_flag")) {
        errmsg("審核註記: 不可空白");
        return;
      }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("trans_date")) {
        errmsg("交易日期: 不可空白");
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


    strSql = " insert into  " + controlTabName + " (" + " serial_no, " + " aud_type, "
        + " trans_date, " + " trans_time, " + " card_no, " + " authentication_code, "
        + " error_desc, " + " error_code, " + " pay_cardid, " + " proc_date, " + " trans_type, "
        + " org_serial_no, " + " major_id_p_seqno, " + " major_card_no, " + " id_p_seqno, "
        + " p_seqno, " + " acct_type, " + " group_code, " + " card_type, " + " file_date, "
        + " file_time, " + " file_name, " + " pnr, " + " ticket_id, " + " card_mode, "
        + " issue_date, " + " promote_emp_no, " + " corp_no, " + " reg_bank_no, " + " train_no, "
        + " departure_station_id, " + " arrival_station_id, " + " seat_no, " + " depart_date, "
        + " org_trans_date, " + " station_id, " + " proc_flag, " + " trans_amount, "
        + " issue_station_id, " + " auth_flag, " + " auth_date, " + " crt_time, " + " crt_date, "
        + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {serialNo, wp.itemStr("aud_type"), wp.itemStr("trans_date"),
        wp.itemStr("trans_time"), wp.itemStr("card_no"), wp.itemStr("authentication_code"),
        wp.itemStr("error_desc"), wp.itemStr("error_code"), wp.itemStr("pay_cardid"),
        colStr("proc_date"), wp.itemStr("trans_type"), wp.itemStr("org_serial_no"),
        colStr("major_id_p_seqno"), colStr("major_card_no"), colStr("id_p_seqno"),
        colStr("p_seqno"), colStr("acct_type"), colStr("group_code"), colStr("card_type"),
        colStr("file_date"), colStr("file_time"), colStr("file_name"), colStr("pnr"),
        colStr("ticket_id"), wp.itemStr("card_mode"), colStr("issue_date"),
        colStr("promote_emp_no"), colStr("corp_no"), colStr("reg_bank_no"), colStr("train_no"),
        colStr("departure_station_id"), colStr("arrival_station_id"), colStr("seat_no"),
        colStr("depart_date"), colStr("orig_trans_date"), colStr("station_id"), colStr("proc_flag"),
        colStr("trans_amount"), colStr("issue_station_id"), wp.itemStr("auth_flag"),
        colStr("auth_date"), wp.sysTime, wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm()};

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

    strSql = "update " + controlTabName + " set " + "auth_flag = ?, " + "trans_date = ?, "
        + "trans_time = ?, " + "card_no = ?, " + "authentication_code = ?, " + "error_desc = ?, "
        + "major_id_p_seqno = ?, " + "major_card_no = ?, " + "id_p_seqno = ?, " + "p_seqno = ?, "
        + "acct_type = ?, " + "group_code = ?, " + "card_type = ?, " + "card_mode = ?, "
        + "issue_date = ?, " + "promote_emp_no = ?, " + "corp_no = ?, " + "reg_bank_no = ?, "
        + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("auth_flag"), wp.itemStr("trans_date"),
        wp.itemStr("trans_time"), wp.itemStr("card_no"), wp.itemStr("authentication_code"),
        wp.itemStr("error_desc"), colStr("major_id_p_seqno"), colStr("major_card_no"),
        colStr("id_p_seqno"), colStr("p_seqno"), colStr("acct_type"), colStr("group_code"),
        colStr("card_type"), colStr("card_mode"), colStr("issue_date"), colStr("promote_emp_no"),
        colStr("corp_no"), colStr("reg_bank_no"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
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
