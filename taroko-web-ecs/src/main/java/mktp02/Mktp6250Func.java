/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/21  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      
* 112/04/21  V1.00.03  Ryan          增加名單匯入功能 ,增加LIST_COND,LIST_FLAG,LIST_USE_SEL欄位維護*
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6250Func extends busi.FuncProc {
  private String PROGNAME = "首刷禮活動回饋參數處理程式108/08/21 V1.00.01";
  //String kk1, kk2;
  String approveTabName = "mkt_fstp_parmseq";
  String controlTabName = "mkt_fstp_parmseq_t";

  public Mktp6250Func(TarokoCommon wr) {
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
  public void dataCheck() {}

  // ************************************************************************
  @Override
  public int dataProc() {
    return rc;
  }

  // ************************************************************************
  public int dbInsertA4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = " insert into  " + approveTabName + " (" + " active_code, " + " active_seq, "
        + " level_seq, " + " record_cond, " + " record_group_no, " + " active_type, "
        + " bonus_type, " + " tax_flag, " + " fund_code, " + " group_type, " + " prog_code, "
        + " prog_s_date, " + " prog_e_date, " + " gift_no, " + " spec_gift_no, " + " per_amt_cond, "
        + " per_amt, " + " perday_cnt_cond, " + " perday_cnt, " + " sum_amt_cond, " + " sum_amt, "
        + " sum_cnt_cond, " + " sum_cnt, " + " threshold_sel, " + " purchase_type_sel, "
        + " purchase_amt_s1, " + " purchase_amt_e1, " + " feedback_amt_1, " + " purchase_amt_s2, "
        + " purchase_amt_e2, " + " feedback_amt_2, " + " purchase_amt_s3, " + " purchase_amt_e3, "
        + " feedback_amt_3, " + " purchase_amt_s4, " + " purchase_amt_e4, " + " feedback_amt_4, "
        + " purchase_amt_s5, " + " purchase_amt_e5, " + " feedback_amt_5, " + " feedback_limit, "
        + " apr_flag, " + " apr_date, " + " apr_user, " + " crt_date, " + " crt_user, "
        + " mod_time, " + " mod_user, " + " mod_seqno, " + " mod_pgm, " 
        + " stop_flag, "+ " stop_date," + " stop_desc,"+ " pur_date_sel,"
        + " purchase_days,"+ " mcht_group_sel,"+ " merchant_sel, "
        + "list_cond, "
        + "list_flag , "
        + "list_use_sel "
        + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "?," + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?,"
        + " timestamp_format(?,'yyyymmddhh24miss'), " + "?," + "?," + " ?,?,?,?,?,?,?,?,?,?,?) ";

    Object[] param = new Object[] {colStr("active_code"), colStr("active_seq"), colStr("level_seq"),
        colStr("record_cond"), colStr("record_group_no"), colStr("active_type"),
        colStr("bonus_type"), colStr("tax_flag"), colStr("fund_code"), colStr("group_type"),
        colStr("prog_code"), colStr("prog_s_date"), colStr("prog_e_date"), colStr("gift_no"),
        colStr("spec_gift_no"), colStr("per_amt_cond"), colStr("per_amt"),
        colStr("perday_cnt_cond"), colStr("perday_cnt"), colStr("sum_amt_cond"), colStr("sum_amt"),
        colStr("sum_cnt_cond"), colStr("sum_cnt"), colStr("threshold_sel"),
        colStr("purchase_type_sel"), colStr("purchase_amt_s1"), colStr("purchase_amt_e1"),
        colStr("feedback_amt_1"), colStr("purchase_amt_s2"), colStr("purchase_amt_e2"),
        colStr("feedback_amt_2"), colStr("purchase_amt_s3"), colStr("purchase_amt_e3"),
        colStr("feedback_amt_3"), colStr("purchase_amt_s4"), colStr("purchase_amt_e4"),
        colStr("feedback_amt_4"), colStr("purchase_amt_s5"), colStr("purchase_amt_e5"),
        colStr("feedback_amt_5"), colStr("feedback_limit"), "Y", wp.loginUser, colStr("crt_date"),
        colStr("crt_user"), wp.sysDate + wp.sysTime, wp.loginUser, colStr("mod_seqno"),
        wp.modPgm(),colStr("stop_flag"),colStr("stop_date"),colStr("stop_desc")
        ,colStr("pur_date_sel"),colStr("purchase_days"),colStr("mcht_group_sel"),
        colStr("merchant_sel"),colStr("list_cond"),colStr("list_flag"),colStr("list_use_sel")};

    sqlExec(strSql, param);

    return rc;
  }

  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select " + " active_code, " + " active_seq, " + " level_seq, " + " record_cond, "
        + " record_group_no, " + " active_type, " + " bonus_type, " + " tax_flag, " + " fund_code, "
        + " group_type, " + " prog_code, " + " prog_s_date, " + " prog_e_date, " + " gift_no, "
        + " spec_gift_no, " + " per_amt_cond, " + " per_amt, " + " perday_cnt_cond, "
        + " perday_cnt, " + " sum_amt_cond, " + " sum_amt, " + " sum_cnt_cond, " + " sum_cnt, "
        + " threshold_sel, " + " purchase_type_sel, " + " purchase_amt_s1, " + " purchase_amt_e1, "
        + " feedback_amt_1, " + " purchase_amt_s2, " + " purchase_amt_e2, " + " feedback_amt_2, "
        + " purchase_amt_s3, " + " purchase_amt_e3, " + " feedback_amt_3, " + " purchase_amt_s4, "
        + " purchase_amt_e4, " + " feedback_amt_4, " + " purchase_amt_s5, " + " purchase_amt_e5, "
        + " feedback_amt_5, " + " feedback_limit, " + " apr_date, " + " apr_user, " + " crt_date, "
        + " crt_user, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, " 
        + " stop_flag,"+ " stop_date," + " stop_desc,"
        + " pur_date_sel,"
        + " purchase_days,"+ " mcht_group_sel, "
        + " merchant_sel " + " ,list_cond " + " ,list_flag " + " ,list_use_sel "
        + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg(" 讀取 " + procTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String apr_flag = "Y";
    strSql = "update " + approveTabName + " set " + "level_seq = ?, " + "record_cond = ?, "
        + "record_group_no = ?, " + "active_type = ?, " + "bonus_type = ?, " + "tax_flag = ?, "
        + "fund_code = ?, " + "group_type = ?, " + "prog_code = ?, " + "prog_s_date = ?, "
        + "prog_e_date = ?, " + "gift_no = ?, " + "spec_gift_no = ?, " + "per_amt_cond = ?, "
        + "per_amt = ?, " + "perday_cnt_cond = ?, " + "perday_cnt = ?, " + "sum_amt_cond = ?, "
        + "sum_amt = ?, " + "sum_cnt_cond = ?, " + "sum_cnt = ?, " + "threshold_sel = ?, "
        + "purchase_type_sel = ?, " + "purchase_amt_s1 = ?, " + "purchase_amt_e1 = ?, "
        + "feedback_amt_1 = ?, " + "purchase_amt_s2 = ?, " + "purchase_amt_e2 = ?, "
        + "feedback_amt_2 = ?, " + "purchase_amt_s3 = ?, " + "purchase_amt_e3 = ?, "
        + "feedback_amt_3 = ?, " + "purchase_amt_s4 = ?, " + "purchase_amt_e4 = ?, "
        + "feedback_amt_4 = ?, " + "purchase_amt_s5 = ?, " + "purchase_amt_e5 = ?, "
        + "feedback_amt_5 = ?, " + "feedback_limit = ?, " + "crt_user  = ?, " + "crt_date  = ?, "
        + "apr_user  = ?, " + "apr_date  = to_char(sysdate,'yyyymmdd'), " + "apr_flag  = ?, "
        + "mod_user  = ?, " + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
        + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1, "
        + "stop_flag = ?, " + "stop_date = ?,"+ "stop_desc = ?, "
        + "pur_date_sel = ?, "+ "purchase_days = ?, "
        + "mcht_group_sel = ?, "
        + "merchant_sel = ?, "
        + "list_cond = ?, "
        + "list_flag = ?, "
        + "list_use_sel = ? "
        + "where 1     = 1 "
        + "and   active_code  = ? " + "and   active_seq  = ? ";

    Object[] param = new Object[] {colStr("level_seq"), colStr("record_cond"),
        colStr("record_group_no"), colStr("active_type"), colStr("bonus_type"), colStr("tax_flag"),
        colStr("fund_code"), colStr("group_type"), colStr("prog_code"), colStr("prog_s_date"),
        colStr("prog_e_date"), colStr("gift_no"), colStr("spec_gift_no"), colStr("per_amt_cond"),
        colStr("per_amt"), colStr("perday_cnt_cond"), colStr("perday_cnt"), colStr("sum_amt_cond"),
        colStr("sum_amt"), colStr("sum_cnt_cond"), colStr("sum_cnt"), colStr("threshold_sel"),
        colStr("purchase_type_sel"), colStr("purchase_amt_s1"), colStr("purchase_amt_e1"),
        colStr("feedback_amt_1"), colStr("purchase_amt_s2"), colStr("purchase_amt_e2"),
        colStr("feedback_amt_2"), colStr("purchase_amt_s3"), colStr("purchase_amt_e3"),
        colStr("feedback_amt_3"), colStr("purchase_amt_s4"), colStr("purchase_amt_e4"),
        colStr("feedback_amt_4"), colStr("purchase_amt_s5"), colStr("purchase_amt_e5"),
        colStr("feedback_amt_5"), colStr("feedback_limit"), colStr("crt_user"), colStr("crt_date"),
        wp.loginUser, apr_flag, colStr("mod_user"), colStr("mod_time"), colStr("mod_pgm"),
        colStr("stop_flag"),colStr("stop_date"),colStr("stop_desc"),
        colStr("pur_date_sel"),colStr("purchase_days"),colStr("mcht_group_sel"),
        colStr("merchant_sel"),colStr("list_cond"),colStr("list_flag"),colStr("list_use_sel"),
        colStr("active_code"), colStr("active_seq")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and active_code = ? "
        + "and active_seq = ? ";

    Object[] param = new Object[] {colStr("active_code"), colStr("active_seq")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + approveTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDelete() {
    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

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
//************************************************************************
public int dbDeleteD4bndata() throws Exception
{
 rc = dbSelectS4();
 if (rc!=1) return rc;
 strSql = "delete mkt_bn_data " 
        + "where 1 = 1 "
        + "and table_name  =  'MKT_FSTP_PARMSEQ' "
        + "and data_key  = ?  "
        ;

 Object[] param =new Object[]
   {
    colStr("active_code")+colStr("active_seq"), 
   };

 wp.dupRecord = "Y";
 sqlExec(strSql, param);
 if (rc!=1) errmsg("刪除 mkt_bn_data 錯誤");

 return 1;
}

//************************************************************************
public int dbInsertA4bndata() throws Exception
{
rc = dbSelectS4();
if (rc!=1) return rc;
strSql = "insert into mkt_bn_data "
      + "select * "
      + "from  mkt_bn_data_t " 
      + "where 1 = 1 "
      + "and table_name  =  'MKT_FSTP_PARMSEQ' "
      + "and data_key  = ?  "
      ;

Object[] param =new Object[]
 {
  colStr("active_code")+colStr("active_seq"), 
 };

sqlExec(strSql, param);

return 1;
}

//************************************************************************
public int dbDeleteD4Tbndata() throws Exception
{
rc = dbSelectS4();
if (rc!=1) return rc;
strSql = "delete mkt_bn_data_t " 
      + "where 1 = 1 "
      + "and table_name  =  'MKT_FSTP_PARMSEQ' "
      + "and data_key  = ?  "
      ;

Object[] param =new Object[]
 {
  colStr("active_code")+colStr("active_seq"), 
 };

wp.dupRecord = "Y";
sqlExec(strSql, param);
if (rc!=1) errmsg("刪除 mkt_bn_data_T 錯誤");

return 1;
}


// ************************************************************************
public int dbDeleteD4Dmlist() {
  rc = dbSelectS4();
  if (rc != 1)
    return rc;
  strSql = "delete from mkt_imfstp_list " + "where 1 = 1 " + "and active_code = ? and active_seq = ? ";

  Object[] param = new Object[] 
          {
          colStr("active_code"),
          colStr("active_seq")
          };
  
  wp.dupRecord = "Y";
  sqlExec(strSql, param);
  if (rc != 1)
    errmsg("刪除 mkt_imfstp_list 錯誤");

  return 1;
}

// ************************************************************************
public int dbDeleteD4TDmlist() {
  rc = dbSelectS4();
  if (rc != 1)
    return rc;
  strSql = "delete from mkt_imfstp_list_t " + "where 1 = 1 " + "and active_code = ? and active_seq = ?";

  Object[] param = new Object[] 
          {
          colStr("active_code"),
          colStr("active_seq")
          };
  
  wp.dupRecord = "Y";
  sqlExec(strSql, param);
  if (rc != 1)
    errmsg("刪除 mkt_imfstp_list_T 錯誤");

  return 1;
}

// ************************************************************************
public int dbInsertA4Dmlist() {
  rc = dbSelectS4();
  if (rc != 1)
    return rc;
  strSql = "insert into mkt_imfstp_list " + "select * " + "from  mkt_imfstp_list_t "
      + "where 1 = 1 " + "and active_code = ? and active_seq = ?";

  Object[] param = new Object[] 
          {
          colStr("active_code"),
          colStr("active_seq")
          };

  sqlExec(strSql, param);

  return 1;
}
}  // End of class
