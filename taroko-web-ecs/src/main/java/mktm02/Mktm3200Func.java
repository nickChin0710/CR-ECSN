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
public class Mktm3200Func extends FuncEdit {
  private String PROGNAME = "高鐵車廂請款明細檔維護處理程式108/12/12 V1.00.01";
  String serialNO;
  String orgControlTabName = "mkt_thsr_disc";
  String controlTabName = "mkt_thsr_disc_t";

  public Mktm3200Func(TarokoCommon wr) {
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
    strSql = " select " + " trans_date, " + " trans_time, " + " card_no, " + " acct_type, "
        + " authentication_code, " + " pay_cardid, " + " trans_amount, " + " trans_type, "
        + " group_code, " + " card_mode, " + " proc_date, " + " proc_flag, " + " error_code, "
        + " error_desc, " + " major_id_p_seqno, " + " major_card_no, " + " id_p_seqno, "
        + " p_seqno, " + " group_code, " + " card_type, " + " file_date, " + " file_time, "
        + " business_date, " + " orig_trans_date, " + " pnr, " + " ticket_id, " + " train_no, "
        + " card_mode, " + " departure_station_id, " + " arrival_station_id, " + " seat_no, "
        + " depart_date, " + " car_no, " + " discount_value, " + " total_ticket_number, "
        + " total_amount, " + " ticketing_station_id, " + " orig_serial_no, " + " plan_code, "
        + " refund_amt, " + " match_flag, " + " match_date, " + " reference_no, " + " crt_time, "
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
      serialNO = wp.itemStr("serial_no");
    } else {
      serialNO = wp.itemStr("serial_no");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (serialNO.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where serial_no = ? ";
          Object[] param = new Object[] {serialNO};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[交易流水號] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (serialNO.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where serial_no = ? ";
        Object[] param = new Object[] {serialNO};
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
        errmsg("已覆核資料, 只可修改不可刪除 !");
        errmsg("原始資料, 不可刪除!");
        return;
      }
    }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("card_no").length() != 0) {
        strSql = " select a.acct_type,b.acct_key,a.p_seqno,a.id_p_seqno,c.chi_name,  "
            + "        a.group_code,a.card_type,major_Card_no,major_id_p_seqno, "
            + "        a.issue_date,a.reg_bank_no,a.promote_emp_no,a.corp_no,c.chi_name,b.acct_key "
            + " from crd_card a,act_acno b,crd_idno c " + " where c.id_p_seqno = a.id_p_seqno "
            + " and   b.p_seqno = a.p_seqno " + " and   a.card_no = ? ";

        Object[] param = new Object[] {wp.itemStr("card_no")};
        sqlSelect(strSql, param);

        if (sqlRowNum <= 0) {
          errmsg("卡號[" + wp.itemStr("card_no") + "]不存在 !");
          return;
        }

        strSql = " select 1 as card_mode  " + " from mkt_topafee_parm " + " where group_code = ? "
            + " and   card_type  = ? ";

        param = new Object[] {colStr("group_code"), colStr("card_type")};
        sqlSelect(strSql, param);

        if (sqlRowNum <= 0)
          colSet("card_mode", "2");
      }
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
        + " trans_date, " + " trans_time, " + " card_no, " + " acct_type, "
        + " authentication_code, " + " pay_cardid, " + " trans_amount, " + " trans_type, "
        + " proc_date, " + " proc_flag, " + " error_code, " + " error_desc, "
        + " major_id_p_seqno, " + " major_card_no, " + " id_p_seqno, " + " p_seqno, "
        + " group_code, " + " card_type, " + " file_date, " + " file_time, " + " business_date, "
        + " orig_trans_date, " + " pnr, " + " ticket_id, " + " train_no, " + " card_mode, "
        + " departure_station_id, " + " arrival_station_id, " + " seat_no, " + " depart_date, "
        + " car_no, " + " discount_value, " + " total_ticket_number, " + " total_amount, "
        + " ticketing_station_id, " + " orig_serial_no, " + " plan_code, " + " refund_amt, "
        + " match_flag, " + " match_date, " + " reference_no, " + " crt_time, " + " crt_date, "
        + " crt_user, " + " mod_seqno, " + " mod_time,mod_user,mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?)";

    Object[] param = new Object[] {serialNO, wp.itemStr("aud_type"), wp.itemStr("trans_date"),
        wp.itemStr("trans_time"), wp.itemStr("card_no"), wp.itemStr("acct_type"),
        wp.itemStr("authentication_code"), wp.itemStr("pay_cardid"), colNum("trans_amount"),
        wp.itemStr("trans_type"), wp.itemStr("proc_date"), wp.itemStr("proc_flag"),
        wp.itemStr("error_code"), wp.itemStr("error_desc"), colStr("major_id_p_seqno"),
        colStr("major_card_no"), colStr("id_p_seqno"), colStr("p_seqno"), colStr("group_code"),
        colStr("card_type"), colStr("file_date"), colStr("file_time"), colStr("business_date"),
        colStr("orig_trans_date"), colStr("pnr"), colStr("ticket_id"), colStr("train_no"),
        colStr("card_mode"), colStr("departure_station_id"), colStr("arrival_station_id"),
        colStr("seat_no"), colStr("depart_date"), colStr("car_no"), colStr("discount_value"),
        colStr("total_ticket_number"), colStr("total_amount"), colStr("ticketing_station_id"),
        colStr("orig_serial_no"), colStr("plan_code"), colStr("refund_amt"), colStr("match_flag"),
        colStr("match_date"), colStr("reference_no"), wp.sysTime, wp.loginUser, wp.modSeqno(),
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

    strSql = "update " + controlTabName + " set " + "trans_date = ?, " + "trans_time = ?, "
        + "card_no = ?, " + "authentication_code = ?, " + "apr_user = '', " + "apr_date = '', "
        + "major_id_p_seqno = ?, " + "major_card_no = ?, " + "id_p_seqno = ?, " + "p_seqno = ?, "
        + "group_code = ?, " + "card_type = ?, " + "card_mode = ?, " + "crt_user  = ?, "
        + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("trans_date"), wp.itemStr("trans_time"),
        wp.itemStr("card_no"), wp.itemStr("authentication_code"), colStr("major_id_p_seqno"),
        colStr("major_card_no"), colStr("id_p_seqno"), colStr("p_seqno"), colStr("group_code"),
        colStr("card_type"), colStr("card_mode"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
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
