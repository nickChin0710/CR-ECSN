/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/19  V1.00.01   Allen Ho      Initial                              *
 * 111-12-07  V1.00.02 Yanghan sync from mega & updated for project coding standard *
 *111/12/16  V1.00.03   Machao        命名规则调整后测试修改
* 111/12/22  V1.00.04   Zuwei         輸出sql log                                                                     *
* 111/12/26  V1.00.05   Zuwei         覆核失敗，sql問題                                                                     *
***************************************************************************/
package mktp01;

import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0170Func extends busi.FuncProc
{
 private final String PROGNAME = "紅利特惠參數檔維護處理程式111-12-16  V1.00.03" ;
  String kk1;
  String approveTabName = "mkt_bpmh2";
  String controlTabName = "mkt_bpmh2_t";

 public Mktp0170Func(TarokoCommon wr)
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
 public int dataSelect()
 {
  // TODO Auto-generated method stub
  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck()
 {
 }
// ************************************************************************
 @Override
 public int dataProc()
 {
  return rc;
 }
// ************************************************************************
 public int dbInsertA4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql= " insert into  " + approveTabName + " ("
          + " active_code, "
          + " active_name, "
          + " bonus_type, "
          + " active_month_s, "
          + " active_month_e, "
          + " stop_flag, "
          + " stop_date, "
          + " give_flag, "
          + " stop_desc, "
          + " effect_months, "
          + " issue_cond, "
          + " issue_date_s, "
          + " issue_date_e, "
          + " re_months, "
//          + " new_hldr_cond, "
//          + " new_hldr_days, "
//          + " new_group_cond, "
//          + " new_hldr_card, "
//          + " new_hldr_sup, "
          + " purch_cond, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " pre_filter_flag, "
          + " run_time_amt, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " card_type_sel, "
          + " limit_amt, "
          + " currency_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " mcc_code_sel, "
          + " pos_entry_sel, "
          + " currencyb_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " bill_type_sel, "
          + " add_times, "
          + " add_point, "
          + " per_point_amt, "
          + " feedback_lmt, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
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
        colStr("active_code"),
        colStr("active_name"),
        colStr("bonus_type"),
        colStr("active_month_s"),
        colStr("active_month_e"),
        colStr("stop_flag"),
        colStr("stop_date"),
        colStr("give_flag"),
        colStr("stop_desc"),
        colStr("effect_months"),
        colStr("issue_cond"),
        colStr("issue_date_s"),
        colStr("issue_date_e"),
        colStr("re_months"),
//        colStr("new_hldr_cond"),
//        colStr("new_hldr_days"),
//        colStr("new_group_cond"),
//        colStr("new_hldr_card"),
//        colStr("new_hldr_sup"),
        colStr("purch_cond"),
        colStr("purch_s_date"),
        colStr("purch_e_date"),
        colStr("pre_filter_flag"),
        colStr("run_time_amt"),
        colStr("acct_type_sel"),
        colStr("group_code_sel"),
        colStr("card_type_sel"),
        colStr("limit_amt"),
        colStr("currency_sel"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("mcc_code_sel"),
        colStr("pos_entry_sel"),
        colStr("currencyb_sel"),
        colStr("bl_cond"),
        colStr("ca_cond"),
        colStr("it_cond"),
        colStr("id_cond"),
        colStr("ao_cond"),
        colStr("ot_cond"),
        colStr("bill_type_sel"),
        colStr("add_times"),
        colStr("add_point"),
        colStr("per_point_amt"),
        colStr("feedback_lmt"),
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
          + " active_code, "
          + " active_name, "
          + " bonus_type, "
          + " active_month_s, "
          + " active_month_e, "
          + " stop_flag, "
          + " stop_date, "
          + " give_flag, "
          + " stop_desc, "
          + " effect_months, "
          + " issue_cond, "
          + " issue_date_s, "
          + " issue_date_e, "
          + " re_months, "
//          + " new_hldr_cond, "
//          + " new_hldr_days, "
//          + " new_group_cond, "
//          + " new_hldr_card, "
//          + " new_hldr_sup, "
          + " purch_cond, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " pre_filter_flag, "
          + " run_time_amt, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " card_type_sel, "
          + " limit_amt, "
          + " currency_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " mcc_code_sel, "
          + " pos_entry_sel, "
          + " currencyb_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " bill_type_sel, "
          + " add_times, "
          + " add_point, "
          + " per_point_amt, "
          + " feedback_lmt, "
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
         + "active_name = ?, "
         + "bonus_type = ?, "
         + "active_month_s = ?, "
         + "active_month_e = ?, "
         + "stop_flag = ?, "
         + "stop_date = ?, "
         + "give_flag = ?, "
         + "stop_desc = ?, "
         + "effect_months = ?, "
         + "issue_cond = ?, "
         + "issue_date_s = ?, "
         + "issue_date_e = ?, "
         + "re_months = ?, "
//         + "new_hldr_cond = ?, "
//         + "new_hldr_days = ?, "
//         + "new_group_cond = ?, "
//         + "new_hldr_card = ?, "
//         + "new_hldr_sup = ?, "
         + "purch_cond = ?, "
         + "purch_s_date = ?, "
         + "purch_e_date = ?, "
         + "pre_filter_flag = ?, "
         + "run_time_amt = ?, "
         + "acct_type_sel = ?, "
         + "group_code_sel = ?, "
         + "card_type_sel = ?, "
         + "limit_amt = ?, "
         + "currency_sel = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "mcc_code_sel = ?, "
         + "pos_entry_sel = ?, "
         + "currencyb_sel = ?, "
         + "bl_cond = ?, "
         + "ca_cond = ?, "
         + "it_cond = ?, "
         + "id_cond = ?, "
         + "ao_cond = ?, "
         + "ot_cond = ?, "
         + "bill_type_sel = ?, "
         + "add_times = ?, "
         + "add_point = ?, "
         + "per_point_amt = ?, "
         + "feedback_lmt = ?, "
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
         + "and   active_code  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_name"),
     colStr("bonus_type"),
     colStr("active_month_s"),
     colStr("active_month_e"),
     colStr("stop_flag"),
     colStr("stop_date"),
     colStr("give_flag"),
     colStr("stop_desc"),
     colStr("effect_months"),
     colStr("issue_cond"),
     colStr("issue_date_s"),
     colStr("issue_date_e"),
     colStr("re_months"),
//     colStr("new_hldr_cond"),
//     colStr("new_hldr_days"),
//     colStr("new_group_cond"),
//     colStr("new_hldr_card"),
//     colStr("new_hldr_sup"),
     colStr("purch_cond"),
     colStr("purch_s_date"),
     colStr("purch_e_date"),
     colStr("pre_filter_flag"),
     colStr("run_time_amt"),
     colStr("acct_type_sel"),
     colStr("group_code_sel"),
     colStr("card_type_sel"),
     colStr("limit_amt"),
     colStr("currency_sel"),
     colStr("merchant_sel"),
     colStr("mcht_group_sel"),
     colStr("mcc_code_sel"),
     colStr("pos_entry_sel"),
     colStr("currencyb_sel"),
     colStr("bl_cond"),
     colStr("ca_cond"),
     colStr("it_cond"),
     colStr("id_cond"),
     colStr("ao_cond"),
     colStr("ot_cond"),
     colStr("bill_type_sel"),
     colStr("add_times"),
     colStr("add_point"),
     colStr("per_point_amt"),
     colStr("feedback_lmt"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("active_code")
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
         + "and active_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbDeleteD4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_bn_data " 
         + "where 1 = 1 "
         + "and table_name  =  'MKT_BPMH2' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_bn_data 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4TBndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_bn_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'MKT_BPMH2' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code"), 
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_bn_data_T 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbInsertA4Bndata() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "insert into mkt_bn_data "
         + "select * "
         + "from  mkt_bn_data_t " 
         + "where 1 = 1 "
         + "and table_name  =  'MKT_BPMH2' "
         + "and data_key  = ?  "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code"), 
    };

  sqlExec(strSql, param,true);

  return 1;
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
