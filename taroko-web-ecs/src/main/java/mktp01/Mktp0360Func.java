/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/14  V1.00.02   Allen Ho      Initial                              *
* 111/12/07  V1.00.03  Machao    sync from mega & updated for project coding standard                                                                         *
* 111/12/22  V1.00.04   Zuwei         輸出sql log                                                                     *
* 112/04/06  V1.00.04   JiangYingdong        program update                *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0360Func extends busi.FuncProc
{
 private final String PROGNAME = "紅利特惠參數檔維護處理程式111/12/07  V1.00.03";
  String kk1;
  String approveTabName = "mkt_bpmh3";
  String controlTabName = "mkt_bpmh3_t";

 public Mktp0360Func(TarokoCommon wr)
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
  strSql= " insert into  " + approveTabName+ " ("
          + " active_code, "
          + " active_name, "
          + " bonus_type, "
          + " tax_flag, "
          + " active_date_s, "
          + " active_date_e, "
          + " proc_date, "
          + " effect_months, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " list_cond, "
          + " bpmh3_file, "
          + " cond_imp_desc, "
          + " purch_cond, "
          + " run_start_cond, "
          + " vd_flag, "
          + " issue_cond, "
          + " issue_date_s, "
          + " issue_date_e, "
          + " card_re_days, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " run_start_month, "
          + " run_time_mm, "
          + " run_time_type, "
          + " run_time_dd, "
          + " acct_type_sel, "
          + " vd_corp_flag, "
          + " group_card_sel, "
//          + " group_oppost_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " mcc_code_sel, "
          + " per_point_amt, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " it_flag, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " bill_type_sel, "
          + " currency_sel, "
          + " feedback_lmt, "
          + " add_type, "
          + " add_item_flag, "
          + " add_item_amt, "
          + " add_amt_s1, "
          + " add_amt_e1, "
          + " add_times1, "
          + " add_point1, "
          + " add_amt_s2, "
          + " add_amt_e2, "
          + " add_times2, "
          + " add_point2, "
          + " add_amt_s3, "
          + " add_amt_e3, "
          + " add_times3, "
          + " add_point3, "
          + " add_amt_s4, "
          + " add_amt_e4, "
          + " add_times4, "
          + " add_point4, "
          + " add_amt_s5, "
          + " add_amt_e5, "
          + " add_times5, "
          + " add_point5, "
          + " add_amt_s6, "
          + " add_amt_e6, "
          + " add_times6, "
          + " add_point6, "
          + " add_amt_s7, "
          + " add_amt_e7, "
          + " add_times7, "
          + " add_point7, "
          + " add_amt_s8, "
          + " add_amt_e8, "
          + " add_times8, "
          + " add_point8, "
          + " add_amt_s9, "
          + " add_amt_e9, "
          + " add_times9, "
          + " add_point9, "
          + " add_amt_s10, "
          + " add_amt_e10, "
          + " add_times10, "
          + " add_point10, "
          + " doorsill_flag, "
          + " d_group_card_sel, "
          + " d_merchant_sel, "
          + " d_mcht_group_sel, "
          + " platform2_kind_sel, "
          + " d_mcc_code_sel, "
          + " d_card_type_sel, "
          + " d_bl_cond, "
          + " d_ca_cond, "
          + " d_it_cond, "
          + " d_it_flag, "
          + " d_id_cond, "
          + " d_ao_cond, "
          + " d_ot_cond, "
          + " d_bill_type_sel, "
          + " d_currency_sel, "
          + " d_pos_entry_sel, "
          + " d_ucaf_sel, "
          + " d_eci_sel, "
          + " d_tax_flag, "
          + " d_add_item_flag, "
          + " d_add_item_amt, "
          + " d_add_amt_s1, "
          + " d_add_amt_e1, "
          + " d_add_point1, "
          + " d_add_amt_s2, "
          + " d_add_amt_e2, "
          + " d_add_point2, "
          + " d_add_amt_s3, "
          + " d_add_amt_e3, "
          + " d_add_point3, "
          + " d_add_amt_s4, "
          + " d_add_amt_e4, "
          + " d_add_point4, "
          + " d_add_amt_s5, "
          + " d_add_amt_e5, "
          + " d_add_point5, "
          + " d_add_amt_s6, "
          + " d_add_amt_e6, "
          + " d_add_point6, "
          + " d_add_amt_s7, "
          + " d_add_amt_e7, "
          + " d_add_point7, "
          + " d_add_amt_s8, "
          + " d_add_amt_e8, "
          + " d_add_point8, "
          + " d_add_amt_s9, "
          + " d_add_amt_e9, "
          + " d_add_point9, "
          + " d_add_amt_s10, "
          + " d_add_amt_e10, "
          + " d_add_point10, "
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
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//          + "?,"
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
        colStr("tax_flag"),
        colStr("active_date_s"),
        colStr("active_date_e"),
        colStr("proc_date"),
        colStr("effect_months"),
        colStr("stop_flag"),
        colStr("stop_date"),
        colStr("stop_desc"),
        colStr("list_cond"),
        colStr("bpmh3_file"),
        colStr("cond_imp_desc"),
        colStr("purch_cond"),
        colStr("run_start_cond"),
        colStr("vd_flag"),
        colStr("issue_cond"),
        colStr("issue_date_s"),
        colStr("issue_date_e"),
        colStr("card_re_days"),
        colStr("purch_s_date"),
        colStr("purch_e_date"),
        colStr("run_start_month"),
        colStr("run_time_mm"),
        colStr("run_time_type"),
        colStr("run_time_dd"),
        colStr("acct_type_sel"),
        colStr("vd_corp_flag"),
        colStr("group_card_sel"),
//        colStr("group_oppost_cond"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("platform_kind_sel"),
        colStr("mcc_code_sel"),
        colStr("per_point_amt"),
        colStr("bl_cond"),
        colStr("ca_cond"),
        colStr("it_cond"),
        colStr("it_flag"),
        colStr("id_cond"),
        colStr("ao_cond"),
        colStr("ot_cond"),
        colStr("bill_type_sel"),
        colStr("currency_sel"),
        colStr("feedback_lmt"),
        colStr("add_type"),
        colStr("add_item_flag"),
        colStr("add_item_amt"),
        colStr("add_amt_s1"),
        colStr("add_amt_e1"),
        colStr("add_times1"),
        colStr("add_point1"),
        colStr("add_amt_s2"),
        colStr("add_amt_e2"),
        colStr("add_times2"),
        colStr("add_point2"),
        colStr("add_amt_s3"),
        colStr("add_amt_e3"),
        colStr("add_times3"),
        colStr("add_point3"),
        colStr("add_amt_s4"),
        colStr("add_amt_e4"),
        colStr("add_times4"),
        colStr("add_point4"),
        colStr("add_amt_s5"),
        colStr("add_amt_e5"),
        colStr("add_times5"),
        colStr("add_point5"),
        colStr("add_amt_s6"),
        colStr("add_amt_e6"),
        colStr("add_times6"),
        colStr("add_point6"),
        colStr("add_amt_s7"),
        colStr("add_amt_e7"),
        colStr("add_times7"),
        colStr("add_point7"),
        colStr("add_amt_s8"),
        colStr("add_amt_e8"),
        colStr("add_times8"),
        colStr("add_point8"),
        colStr("add_amt_s9"),
        colStr("add_amt_e9"),
        colStr("add_times9"),
        colStr("add_point9"),
        colStr("add_amt_s10"),
        colStr("add_amt_e10"),
        colStr("add_times10"),
        colStr("add_point10"),
        colStr("doorsill_flag"),
        colStr("d_group_card_sel"),
        colStr("d_merchant_sel"),
        colStr("d_mcht_group_sel"),
        colStr("platform2_kind_sel"),
        colStr("d_mcc_code_sel"),
        colStr("d_card_type_sel"),
        colStr("d_bl_cond"),
        colStr("d_ca_cond"),
        colStr("d_it_cond"),
        colStr("d_it_flag"),
        colStr("d_id_cond"),
        colStr("d_ao_cond"),
        colStr("d_ot_cond"),
        colStr("d_bill_type_sel"),
        colStr("d_currency_sel"),
        colStr("d_pos_entry_sel"),
        colStr("d_ucaf_sel"),
        colStr("d_eci_sel"),
        colStr("d_tax_flag"),
        colStr("d_add_item_flag"),
        colStr("d_add_item_amt"),
        colStr("d_add_amt_s1"),
        colStr("d_add_amt_e1"),
        colStr("d_add_point1"),
        colStr("d_add_amt_s2"),
        colStr("d_add_amt_e2"),
        colStr("d_add_point2"),
        colStr("d_add_amt_s3"),
        colStr("d_add_amt_e3"),
        colStr("d_add_point3"),
        colStr("d_add_amt_s4"),
        colStr("d_add_amt_e4"),
        colStr("d_add_point4"),
        colStr("d_add_amt_s5"),
        colStr("d_add_amt_e5"),
        colStr("d_add_point5"),
        colStr("d_add_amt_s6"),
        colStr("d_add_amt_e6"),
        colStr("d_add_point6"),
        colStr("d_add_amt_s7"),
        colStr("d_add_amt_e7"),
        colStr("d_add_point7"),
        colStr("d_add_amt_s8"),
        colStr("d_add_amt_e8"),
        colStr("d_add_point8"),
        colStr("d_add_amt_s9"),
        colStr("d_add_amt_e9"),
        colStr("d_add_point9"),
        colStr("d_add_amt_s10"),
        colStr("d_add_amt_e10"),
        colStr("d_add_point10"),
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
          + " tax_flag, "
          + " active_date_s, "
          + " active_date_e, "
          + " proc_date, "
          + " effect_months, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " list_cond, "
          + " bpmh3_file, "
          + " cond_imp_desc, "
          + " purch_cond, "
          + " run_start_cond, "
          + " vd_flag, "
          + " issue_cond, "
          + " issue_date_s, "
          + " issue_date_e, "
          + " card_re_days, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " run_start_month, "
          + " run_time_mm, "
          + " run_time_type, "
          + " run_time_dd, "
          + " acct_type_sel, "
          + " vd_corp_flag, "
          + " group_card_sel, "
//          + " group_oppost_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " mcc_code_sel, "
          + " per_point_amt, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " it_flag, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " bill_type_sel, "
          + " currency_sel, "
          + " feedback_lmt, "
          + " add_type, "
          + " add_item_flag, "
          + " add_item_amt, "
          + " add_amt_s1, "
          + " add_amt_e1, "
          + " add_times1, "
          + " add_point1, "
          + " add_amt_s2, "
          + " add_amt_e2, "
          + " add_times2, "
          + " add_point2, "
          + " add_amt_s3, "
          + " add_amt_e3, "
          + " add_times3, "
          + " add_point3, "
          + " add_amt_s4, "
          + " add_amt_e4, "
          + " add_times4, "
          + " add_point4, "
          + " add_amt_s5, "
          + " add_amt_e5, "
          + " add_times5, "
          + " add_point5, "
          + " add_amt_s6, "
          + " add_amt_e6, "
          + " add_times6, "
          + " add_point6, "
          + " add_amt_s7, "
          + " add_amt_e7, "
          + " add_times7, "
          + " add_point7, "
          + " add_amt_s8, "
          + " add_amt_e8, "
          + " add_times8, "
          + " add_point8, "
          + " add_amt_s9, "
          + " add_amt_e9, "
          + " add_times9, "
          + " add_point9, "
          + " add_amt_s10, "
          + " add_amt_e10, "
          + " add_times10, "
          + " add_point10, "
          + " doorsill_flag, "
          + " d_group_card_sel, "
          + " d_merchant_sel, "
          + " d_mcht_group_sel, "
          + " platform2_kind_sel, "
          + " d_mcc_code_sel, "
          + " d_card_type_sel, "
          + " d_bl_cond, "
          + " d_ca_cond, "
          + " d_it_cond, "
          + " d_it_flag, "
          + " d_id_cond, "
          + " d_ao_cond, "
          + " d_ot_cond, "
          + " d_bill_type_sel, "
          + " d_currency_sel, "
          + " d_pos_entry_sel, "
          + " d_ucaf_sel, "
          + " d_eci_sel, "
          + " d_tax_flag, "
          + " d_add_item_flag, "
          + " d_add_item_amt, "
          + " d_add_amt_s1, "
          + " d_add_amt_e1, "
          + " d_add_point1, "
          + " d_add_amt_s2, "
          + " d_add_amt_e2, "
          + " d_add_point2, "
          + " d_add_amt_s3, "
          + " d_add_amt_e3, "
          + " d_add_point3, "
          + " d_add_amt_s4, "
          + " d_add_amt_e4, "
          + " d_add_point4, "
          + " d_add_amt_s5, "
          + " d_add_amt_e5, "
          + " d_add_point5, "
          + " d_add_amt_s6, "
          + " d_add_amt_e6, "
          + " d_add_point6, "
          + " d_add_amt_s7, "
          + " d_add_amt_e7, "
          + " d_add_point7, "
          + " d_add_amt_s8, "
          + " d_add_amt_e8, "
          + " d_add_point8, "
          + " d_add_amt_s9, "
          + " d_add_amt_e9, "
          + " d_add_point9, "
          + " d_add_amt_s10, "
          + " d_add_amt_e10, "
          + " d_add_point10, "
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
  strSql= "update " +approveTabName + " set "
         + "active_name = ?, "
         + "bonus_type = ?, "
         + "tax_flag = ?, "
         + "active_date_s = ?, "
         + "active_date_e = ?, "
         + "proc_date = ?, "
         + "effect_months = ?, "
         + "stop_flag = ?, "
         + "stop_date = ?, "
         + "stop_desc = ?, "
         + "list_cond = ?, "
         + "bpmh3_file = ?, "
         + "cond_imp_desc = ?, "
         + "purch_cond = ?, "
         + "run_start_cond = ?, "
         + "vd_flag = ?, "
         + "issue_cond = ?, "
         + "issue_date_s = ?, "
         + "issue_date_e = ?, "
         + "card_re_days = ?, "
         + "purch_s_date = ?, "
         + "purch_e_date = ?, "
         + "run_start_month = ?, "
         + "run_time_mm = ?, "
         + "run_time_type = ?, "
         + "run_time_dd = ?, "
         + "acct_type_sel = ?, "
         + "vd_corp_flag = ?, "
         + "group_card_sel = ?, "
//         + "group_oppost_cond = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "platform_kind_sel = ?, "
         + "mcc_code_sel = ?, "
         + "per_point_amt = ?, "
         + "bl_cond = ?, "
         + "ca_cond = ?, "
         + "it_cond = ?, "
         + "it_flag = ?, "
         + "id_cond = ?, "
         + "ao_cond = ?, "
         + "ot_cond = ?, "
         + "bill_type_sel = ?, "
         + "currency_sel = ?, "
         + "feedback_lmt = ?, "
         + "add_type = ?, "
         + "add_item_flag = ?, "
         + "add_item_amt = ?, "
         + "add_amt_s1 = ?, "
         + "add_amt_e1 = ?, "
         + "add_times1 = ?, "
         + "add_point1 = ?, "
         + "add_amt_s2 = ?, "
         + "add_amt_e2 = ?, "
         + "add_times2 = ?, "
         + "add_point2 = ?, "
         + "add_amt_s3 = ?, "
         + "add_amt_e3 = ?, "
         + "add_times3 = ?, "
         + "add_point3 = ?, "
         + "add_amt_s4 = ?, "
         + "add_amt_e4 = ?, "
         + "add_times4 = ?, "
         + "add_point4 = ?, "
         + "add_amt_s5 = ?, "
         + "add_amt_e5 = ?, "
         + "add_times5 = ?, "
         + "add_point5 = ?, "
         + "add_amt_s6 = ?, "
         + "add_amt_e6 = ?, "
         + "add_times6 = ?, "
         + "add_point6 = ?, "
         + "add_amt_s7 = ?, "
         + "add_amt_e7 = ?, "
         + "add_times7 = ?, "
         + "add_point7 = ?, "
         + "add_amt_s8 = ?, "
         + "add_amt_e8 = ?, "
         + "add_times8 = ?, "
         + "add_point8 = ?, "
         + "add_amt_s9 = ?, "
         + "add_amt_e9 = ?, "
         + "add_times9 = ?, "
         + "add_point9 = ?, "
         + "add_amt_s10 = ?, "
         + "add_amt_e10 = ?, "
         + "add_times10 = ?, "
         + "add_point10 = ?, "
         + "doorsill_flag = ?, "
         + "d_group_card_sel = ?, "
         + "d_merchant_sel = ?, "
         + "d_mcht_group_sel = ?, "
         + "platform2_kind_sel = ?, "
         + "d_mcc_code_sel = ?, "
         + "d_card_type_sel = ?, "
         + "d_bl_cond = ?, "
         + "d_ca_cond = ?, "
         + "d_it_cond = ?, "
         + "d_it_flag = ?, "
         + "d_id_cond = ?, "
         + "d_ao_cond = ?, "
         + "d_ot_cond = ?, "
         + "d_bill_type_sel = ?, "
         + "d_currency_sel = ?, "
         + "d_pos_entry_sel = ?, "
         + "d_ucaf_sel = ?, "
         + "d_eci_sel = ?, "
         + "d_tax_flag = ?, "
         + "d_add_item_flag = ?, "
         + "d_add_item_amt = ?, "
         + "d_add_amt_s1 = ?, "
         + "d_add_amt_e1 = ?, "
         + "d_add_point1 = ?, "
         + "d_add_amt_s2 = ?, "
         + "d_add_amt_e2 = ?, "
         + "d_add_point2 = ?, "
         + "d_add_amt_s3 = ?, "
         + "d_add_amt_e3 = ?, "
         + "d_add_point3 = ?, "
         + "d_add_amt_s4 = ?, "
         + "d_add_amt_e4 = ?, "
         + "d_add_point4 = ?, "
         + "d_add_amt_s5 = ?, "
         + "d_add_amt_e5 = ?, "
         + "d_add_point5 = ?, "
         + "d_add_amt_s6 = ?, "
         + "d_add_amt_e6 = ?, "
         + "d_add_point6 = ?, "
         + "d_add_amt_s7 = ?, "
         + "d_add_amt_e7 = ?, "
         + "d_add_point7 = ?, "
         + "d_add_amt_s8 = ?, "
         + "d_add_amt_e8 = ?, "
         + "d_add_point8 = ?, "
         + "d_add_amt_s9 = ?, "
         + "d_add_amt_e9 = ?, "
         + "d_add_point9 = ?, "
         + "d_add_amt_s10 = ?, "
         + "d_add_amt_e10 = ?, "
         + "d_add_point10 = ?, "
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
     colStr("tax_flag"),
     colStr("active_date_s"),
     colStr("active_date_e"),
     colStr("proc_date"),
     colStr("effect_months"),
     colStr("stop_flag"),
     colStr("stop_date"),
     colStr("stop_desc"),
     colStr("list_cond"),
     colStr("bpmh3_file"),
     colStr("cond_imp_desc"),
     colStr("purch_cond"),
     colStr("run_start_cond"),
     colStr("vd_flag"),
     colStr("issue_cond"),
     colStr("issue_date_s"),
     colStr("issue_date_e"),
     colStr("card_re_days"),
     colStr("purch_s_date"),
     colStr("purch_e_date"),
     colStr("run_start_month"),
     colStr("run_time_mm"),
     colStr("run_time_type"),
     colStr("run_time_dd"),
     colStr("acct_type_sel"),
     colStr("vd_corp_flag"),
     colStr("group_card_sel"),
//     colStr("group_oppost_cond"),
     colStr("merchant_sel"),
     colStr("mcht_group_sel"),
     colStr("platform_kind_sel"),
     colStr("mcc_code_sel"),
     colStr("per_point_amt"),
     colStr("bl_cond"),
     colStr("ca_cond"),
     colStr("it_cond"),
     colStr("it_flag"),
     colStr("id_cond"),
     colStr("ao_cond"),
     colStr("ot_cond"),
     colStr("bill_type_sel"),
     colStr("currency_sel"),
     colStr("feedback_lmt"),
     colStr("add_type"),
     colStr("add_item_flag"),
     colStr("add_item_amt"),
     colStr("add_amt_s1"),
     colStr("add_amt_e1"),
     colStr("add_times1"),
     colStr("add_point1"),
     colStr("add_amt_s2"),
     colStr("add_amt_e2"),
     colStr("add_times2"),
     colStr("add_point2"),
     colStr("add_amt_s3"),
     colStr("add_amt_e3"),
     colStr("add_times3"),
     colStr("add_point3"),
     colStr("add_amt_s4"),
     colStr("add_amt_e4"),
     colStr("add_times4"),
     colStr("add_point4"),
     colStr("add_amt_s5"),
     colStr("add_amt_e5"),
     colStr("add_times5"),
     colStr("add_point5"),
     colStr("add_amt_s6"),
     colStr("add_amt_e6"),
     colStr("add_times6"),
     colStr("add_point6"),
     colStr("add_amt_s7"),
     colStr("add_amt_e7"),
     colStr("add_times7"),
     colStr("add_point7"),
     colStr("add_amt_s8"),
     colStr("add_amt_e8"),
     colStr("add_times8"),
     colStr("add_point8"),
     colStr("add_amt_s9"),
     colStr("add_amt_e9"),
     colStr("add_times9"),
     colStr("add_point9"),
     colStr("add_amt_s10"),
     colStr("add_amt_e10"),
     colStr("add_times10"),
     colStr("add_point10"),
     colStr("doorsill_flag"),
     colStr("d_group_card_sel"),
     colStr("d_merchant_sel"),
     colStr("d_mcht_group_sel"),
     colStr("platform2_kind_sel"),
     colStr("d_mcc_code_sel"),
     colStr("d_card_type_sel"),
     colStr("d_bl_cond"),
     colStr("d_ca_cond"),
     colStr("d_it_cond"),
     colStr("d_it_flag"),
     colStr("d_id_cond"),
     colStr("d_ao_cond"),
     colStr("d_ot_cond"),
     colStr("d_bill_type_sel"),
     colStr("d_currency_sel"),
     colStr("d_pos_entry_sel"),
     colStr("d_ucaf_sel"),
     colStr("d_eci_sel"),
     colStr("d_tax_flag"),
     colStr("d_add_item_flag"),
     colStr("d_add_item_amt"),
     colStr("d_add_amt_s1"),
     colStr("d_add_amt_e1"),
     colStr("d_add_point1"),
     colStr("d_add_amt_s2"),
     colStr("d_add_amt_e2"),
     colStr("d_add_point2"),
     colStr("d_add_amt_s3"),
     colStr("d_add_amt_e3"),
     colStr("d_add_point3"),
     colStr("d_add_amt_s4"),
     colStr("d_add_amt_e4"),
     colStr("d_add_point4"),
     colStr("d_add_amt_s5"),
     colStr("d_add_amt_e5"),
     colStr("d_add_point5"),
     colStr("d_add_amt_s6"),
     colStr("d_add_amt_e6"),
     colStr("d_add_point6"),
     colStr("d_add_amt_s7"),
     colStr("d_add_amt_e7"),
     colStr("d_add_point7"),
     colStr("d_add_amt_s8"),
     colStr("d_add_amt_e8"),
     colStr("d_add_point8"),
     colStr("d_add_amt_s9"),
     colStr("d_add_amt_e9"),
     colStr("d_add_point9"),
     colStr("d_add_amt_s10"),
     colStr("d_add_amt_e10"),
     colStr("d_add_point10"),
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
  strSql = "delete " +approveTabName + " " 
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
         + "and table_name  =  'MKT_BPMH3' "
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
         + "and table_name  =  'MKT_BPMH3' "
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
         + "and table_name  =  'MKT_BPMH3' "
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
 public int dbDeleteD4Bpmh3list() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_bpmh3_list " 
         + "where 1 = 1 "
         + "and active_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_bpmh3_list 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4TBpmh3list() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete mkt_bpmh3_list_t " 
         + "where 1 = 1 "
         + "and active_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")
    };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (rc!=1) errmsg("刪除 mkt_bpmh3_list_T 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbInsertA4Bpmh3list() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "insert into mkt_bpmh3_list "
         + "select * "
         + "from  mkt_bpmh3_list_t " 
         + "where 1 = 1 "
         + "and active_code = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("active_code")
    };

  sqlExec(strSql, param,true);

  return 1;
 }
// ************************************************************************
 public int dbDelete() throws Exception
 {
  strSql = "delete " +controlTabName + " " 
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
