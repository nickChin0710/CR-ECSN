/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/09  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-07-17  V1.00.03   shiyuqi        rename tableName &FiledName         *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名       
* 111-11-08  V1.00.03  machao     變量名稱調整                                                                                *    
* 112-03-24  V1.00.04  Zuwei Su       增匯入名單3個欄位                                                                   *    
* 112/04/04  V1.00.05   Ryan         修改名單匯入時預設活動序號帶00                *
* 112/06/21  V1.00.06   Zuwei Su     欄位new_hldr_sel沒有寫入db                *
***************************************************************************/
package mktp02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp6240Func extends busi.FuncProc {
  private String PROGNAME = "首刷禮活動回饋參數覆核處理程式108/10/09 V1.00.01";
// / String kk1;
  String approveTabName = "mkt_fstp_parm";
  String controlTabName = "mkt_fstp_parm_t";

  public Mktp6240Func(TarokoCommon wr) {
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
    
    rc = ActiveCodeCheck();
    if (rc !=1)
    {
    	return rc;
    }
    
    strSql = " insert into  "
            + approveTabName
            + " ("
            + " active_code, "
            + " active_name, "
            + " stop_flag, "
            + " stop_date, "
            + " stop_desc, "
            + " issue_date_s, "
            + " issue_date_e, "
            + " effect_months, "
            + " purchase_days, "
            + " n1_days, "
            + " achieve_cond, "
            + " new_hldr_cond, "
            + " new_hldr_sel, "
            + " new_hldr_days, "
            + " new_group_cond, "
            + " new_hldr_card, "
            + " new_hldr_sup, "
            + " acct_type_sel, "
            + " group_code_sel, "
            + " source_code_sel, "
            + " card_type_sel, "
            + " promote_dept_sel, "
            + " list_cond, "
            + " list_flag, "
            + " list_use_sel, "
            + " mcc_code_sel, "
            + " merchant_sel, "
            + " mcht_group_sel, "
            + " mcht_in_amt, "
            + " in_merchant_sel, "
            + " in_mcht_group_sel, "
            + " pos_entry_sel, "
            + " ucaf_sel, "
            + " eci_sel, "
            + " bl_cond, "
            + " ca_cond, "
            + " it_cond, "
            + " it_flag, "
            + " id_cond, "
            + " ao_cond, "
            + " ot_cond, "
            + " linebc_cond, "
            + " banklite_cond, "
            + " anulfee_cond, "
            + " anulfee_days, "
            + " action_pay_cond, "
            + " action_pay_times, "
            + " selfdeduct_cond, "
            + " sms_nopurc_cond, "
            + " sms_nopurc_days, "
            + " nopurc_msg_id_g, "
            + " nopurc_msg_id_c, "
            + " sms_half_cond, "
            + " sms_half_days, "
            + " half_cnt_cond, "
            + " half_cnt, "
            + " half_andor_cond, "
            + " half_amt_cond, "
            + " half_amt, "
            + " half_msg_id_g, "
            + " half_msg_id_c, "
            + " sms_send_cond, "
            + " sms_send_days, "
            + " send_msg_id, "
            + " multi_fb_type, "
            + " record_cond, "
            + " record_group_no, "
            + " active_type, "
            + " bonus_type, "
            + " tax_flag, "
            + " fund_code, "
            + " group_type, "
            + " prog_code, "
            + " prog_s_date, "
            + " prog_e_date, "
            + " gift_no, "
            + " spec_gift_no, "
            + " per_amt_cond, "
            + " per_amt, "
            + " perday_cnt_cond, "
            + " perday_cnt, "
            + " sum_amt_cond, "
            + " sum_amt, "
            + " sum_cnt_cond, "
            + " sum_cnt, "
            + " purch_feed_type, "
            + " threshold_sel, "
            + " purchase_type_sel, "
            + " purchase_amt_s1, "
            + " purchase_amt_e1, "
            + " feedback_amt_1, "
            + " purchase_amt_s2, "
            + " purchase_amt_e2, "
            + " feedback_amt_2, "
            + " purchase_amt_s3, "
            + " purchase_amt_e3, "
            + " feedback_amt_3, "
            + " purchase_amt_s4, "
            + " purchase_amt_e4, "
            + " feedback_amt_4, "
            + " purchase_amt_s5, "
            + " purchase_amt_e5, "
            + " feedback_amt_5, "
            + " feedback_limit, "
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " mod_time, "
            + " mod_user, "
            + " mod_seqno, "
            + " mod_pgm, "
            + " new_hldr_flag, "
            + " mcht_seq_flag, "
            + " c_record_cond,"
            + " c_record_group_no, "
            + " mcht_in_cond,"
            + " nopurc_msg_pgm,"
            + " half_msg_pgm, "
            + " send_msg_pgm,"
            + " add_value_cond,"
            + " add_value,"
            + "mkt_fstp_gift_cond "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "?,"
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + "?,"
            + "?,"
            + " ?,?,?,?,?,?,?,?,?,?,?,?) ";

    Object[] param = new Object[] {
            colStr("active_code"),
            colStr("active_name"),
            colStr("stop_flag"),
            colStr("stop_date"),
            colStr("stop_desc"),
            colStr("issue_date_s"),
            colStr("issue_date_e"),
            colStr("effect_months"),
            colStr("purchase_days"),
            colStr("n1_days"),
            colStr("achieve_cond"),
            colStr("new_hldr_cond"),
            colStr("new_hldr_sel"),
            colStr("new_hldr_days"),
            colStr("new_group_cond"),
            colStr("new_hldr_card"),
            colStr("new_hldr_sup"),
            colStr("acct_type_sel"),
            colStr("group_code_sel"),
            colStr("source_code_sel"),
            colStr("card_type_sel"),
            colStr("promote_dept_sel"),
            colStr("list_cond"),
            colStr("list_flag"),
            colStr("list_use_sel"),
            colStr("mcc_code_sel"),
            colStr("merchant_sel"),
            colStr("mcht_group_sel"),
            colStr("mcht_in_amt"),
            colStr("in_merchant_sel"),
            colStr("in_mcht_group_sel"),
            colStr("pos_entry_sel"),
            colStr("ucaf_sel"),
            colStr("eci_sel"),
            colStr("bl_cond"),
            colStr("ca_cond"),
            colStr("it_cond"),
            colStr("it_flag"),
            colStr("id_cond"),
            colStr("ao_cond"),
            colStr("ot_cond"),
            colStr("linebc_cond"),
            colStr("banklite_cond"),
            colStr("anulfee_cond"),
            colStr("anulfee_days"),
            colStr("action_pay_cond"),
            colStr("action_pay_times"),
            colStr("selfdeduct_cond"),
            colStr("sms_nopurc_cond"),
            colStr("sms_nopurc_days"),
            colStr("nopurc_msg_id_g"),
            colStr("nopurc_msg_id_c"),
            colStr("sms_half_cond"),
            colStr("sms_half_days"),
            colStr("half_cnt_cond"),
            colStr("half_cnt"),
            colStr("half_andor_cond"),
            colStr("half_amt_cond"),
            colStr("half_amt"),
            colStr("half_msg_id_g"),
            colStr("half_msg_id_c"),
            colStr("sms_send_cond"),
            colStr("sms_send_days"),
            colStr("send_msg_id"),
            colStr("multi_fb_type"),
            colStr("record_cond"),
            colStr("record_group_no"),
            colStr("active_type"),
            colStr("bonus_type"),
            colStr("tax_flag"),
            colStr("fund_code"),
            colStr("group_type"),
            colStr("prog_code"),
            colStr("prog_s_date"),
            colStr("prog_e_date"),
            colStr("gift_no"),
            colStr("spec_gift_no"),
            colStr("per_amt_cond"),
            colStr("per_amt"),
            colStr("perday_cnt_cond"),
            colStr("perday_cnt"),
            colStr("sum_amt_cond"),
            colStr("sum_amt"),
            colStr("sum_cnt_cond"),
            colStr("sum_cnt"),
            colStr("purch_feed_type"),
            colStr("threshold_sel"),
            colStr("purchase_type_sel"),
            colStr("purchase_amt_s1"),
            colStr("purchase_amt_e1"),
            colStr("feedback_amt_1"),
            colStr("purchase_amt_s2"),
            colStr("purchase_amt_e2"),
            colStr("feedback_amt_2"),
            colStr("purchase_amt_s3"),
            colStr("purchase_amt_e3"),
            colStr("feedback_amt_3"),
            colStr("purchase_amt_s4"),
            colStr("purchase_amt_e4"),
            colStr("feedback_amt_4"),
            colStr("purchase_amt_s5"),
            colStr("purchase_amt_e5"),
            colStr("feedback_amt_5"),
            colStr("feedback_limit"),
            "Y",
            wp.loginUser,
            colStr("crt_date"),
            colStr("crt_user"),
            wp.sysDate + wp.sysTime,
            wp.loginUser,
            colStr("mod_seqno"),
            wp.modPgm(),
            colStr("new_hldr_flag"),
            colStr("mcht_seq_flag"),
            colStr("c_record_cond"),
            colStr("c_record_group_no"),
            colStr("mcht_in_cond"),
            colStr("nopurc_msg_pgm"),
            colStr("half_msg_pgm"),
            colStr("send_msg_pgm"),
            colStr("add_value_cond"),
            colStr("add_value"),
            colStr("mkt_fstp_gift_cond")
    };

    sqlExec(strSql, param);

    return rc;
  }

  // **************************************
  public int ActiveCodeCheck() {
	  strSql = "select active_code from mkt_fstp_parm where active_code = ?";
	  Object[] param = new Object[] {colStr("active_code")};
	  
	  sqlSelect(strSql, param);
	  if (sqlRowNum > 0)
	  { 
		  errmsg("活動代碼 [" + colStr("active_code") + "] 已存在，無法新增");
	  }
	    return rc;	 
	  
  }
  
  // ************************************************************************
  public int dbSelectS4() {
    String procTabName = "";
    procTabName = controlTabName;
    strSql = " select "
            + " active_code, "
            + " active_name, "
            + " stop_flag, "
            + " stop_date, "
            + " stop_desc, "
            + " issue_date_s, "
            + " issue_date_e, "
            + " effect_months, "
            + " purchase_days, "
            + " n1_days, "
            + " achieve_cond, "
            + " new_hldr_cond, "
            + " new_hldr_sel, "
            + " new_hldr_days, "
            + " new_group_cond, "
            + " new_hldr_card, "
            + " new_hldr_sup, "
            + " acct_type_sel, "
            + " group_code_sel, "
            + " source_code_sel, "
            + " card_type_sel, "
            + " promote_dept_sel, "
            + " list_cond, "
            + " list_flag, "
            + " list_use_sel, "
            + " mcc_code_sel, "
            + " merchant_sel, "
            + " mcht_group_sel, "
            + " mcht_in_amt, "
            + " in_merchant_sel, "
            + " in_mcht_group_sel, "
            + " pos_entry_sel, "
            + " ucaf_sel, "
            + " eci_sel, "
            + " bl_cond, "
            + " ca_cond, "
            + " it_cond, "
            + " it_flag, "
            + " id_cond, "
            + " ao_cond, "
            + " ot_cond, "
            + " linebc_cond, "
            + " banklite_cond, "
            + " anulfee_cond, "
            + " anulfee_days, "
            + " action_pay_cond, "
            + " action_pay_times, "
            + " selfdeduct_cond, "
            + " sms_nopurc_cond, "
            + " sms_nopurc_days, "
            + " nopurc_msg_id_g, "
            + " nopurc_msg_id_c, "
            + " sms_half_cond, "
            + " sms_half_days, "
            + " half_cnt_cond, "
            + " half_cnt, "
            + " half_andor_cond, "
            + " half_amt_cond, "
            + " half_amt, "
            + " half_msg_id_g, "
            + " half_msg_id_c, "
            + " sms_send_cond, "
            + " sms_send_days, "
            + " send_msg_id, "
            + " multi_fb_type, "
            + " record_cond, "
            + " record_group_no, "
            + " active_type, "
            + " bonus_type, "
            + " tax_flag, "
            + " fund_code, "
            + " group_type, "
            + " prog_code, "
            + " prog_s_date, "
            + " prog_e_date, "
            + " gift_no, "
            + " spec_gift_no, "
            + " per_amt_cond, "
            + " per_amt, "
            + " perday_cnt_cond, "
            + " perday_cnt, "
            + " sum_amt_cond, "
            + " sum_amt, "
            + " sum_cnt_cond, "
            + " sum_cnt, "
            + " purch_feed_type, "
            + " threshold_sel, "
            + " purchase_type_sel, "
            + " purchase_amt_s1, "
            + " purchase_amt_e1, "
            + " feedback_amt_1, "
            + " purchase_amt_s2, "
            + " purchase_amt_e2, "
            + " feedback_amt_2, "
            + " purchase_amt_s3, "
            + " purchase_amt_e3, "
            + " feedback_amt_3, "
            + " purchase_amt_s4, "
            + " purchase_amt_e4, "
            + " feedback_amt_4, "
            + " purchase_amt_s5, "
            + " purchase_amt_e5, "
            + " feedback_amt_5, "
            + " feedback_limit, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, "
            + " new_hldr_flag, "
            + " mcht_seq_flag, "
            + " c_record_cond, "
            + " c_record_group_no, "
            + " mcht_in_cond,"
            + " nopurc_msg_pgm, "
            + " half_msg_pgm, "
            + " send_msg_pgm,"
            + " add_value_cond,"
            + " add_value,"
            + " mkt_fstp_gift_cond "
            + " from "
            + procTabName
            + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("wprowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return rc;
  }

  // ************************************************************************
  public int dbUpdateU4() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    String aprFlag = "Y";
    strSql = "update "
            + approveTabName
            + " set "
            + "active_name = ?, "
            + "stop_flag = ?, "
            + "stop_date = ?, "
            + "stop_desc = ?, "
            + "issue_date_s = ?, "
            + "issue_date_e = ?, "
            + "effect_months = ?, "
            + "purchase_days = ?, "
            + "n1_days = ?, "
            + "achieve_cond = ?, "
            + "new_hldr_cond = ?, "
            + "new_hldr_sel = ?, "
            + "new_hldr_days = ?, "
            + "new_group_cond = ?, "
            + "new_hldr_card = ?, "
            + "new_hldr_sup = ?, "
            + "acct_type_sel = ?, "
            + "group_code_sel = ?, "
            + "source_code_sel = ?, "
            + "card_type_sel = ?, "
            + "promote_dept_sel = ?, "
            + "list_cond = ?, "
            + "list_flag = ?, "
            + "list_use_sel = ?, "
            + "mcc_code_sel = ?, "
            + "merchant_sel = ?, "
            + "mcht_group_sel = ?, "
            + "mcht_in_amt = ?, "
            + "in_merchant_sel = ?, "
            + "in_mcht_group_sel = ?, "
            + "pos_entry_sel = ?, "
            + "ucaf_sel = ?, "
            + "eci_sel = ?, "
            + "bl_cond = ?, "
            + "ca_cond = ?, "
            + "it_cond = ?, "
            + "it_flag = ?, "
            + "id_cond = ?, "
            + "ao_cond = ?, "
            + "ot_cond = ?, "
            + "linebc_cond = ?, "
            + "banklite_cond = ?, "
            + "anulfee_cond = ?, "
            + "anulfee_days = ?, "
            + "action_pay_cond = ?, "
            + "action_pay_times = ?, "
            + "selfdeduct_cond = ?, "
            + "sms_nopurc_cond = ?, "
            + "sms_nopurc_days = ?, "
            + "nopurc_msg_id_g = ?, "
            + "nopurc_msg_id_c = ?, "
            + "sms_half_cond = ?, "
            + "sms_half_days = ?, "
            + "half_cnt_cond = ?, "
            + "half_cnt = ?, "
            + "half_andor_cond = ?, "
            + "half_amt_cond = ?, "
            + "half_amt = ?, "
            + "half_msg_id_g = ?, "
            + "half_msg_id_c = ?, "
            + "sms_send_cond = ?, "
            + "sms_send_days = ?, "
            + "send_msg_id = ?, "
            + "multi_fb_type = ?, "
            + "record_cond = ?, "
            + "record_group_no = ?, "
            + "active_type = ?, "
            + "bonus_type = ?, "
            + "tax_flag = ?, "
            + "fund_code = ?, "
            + "group_type = ?, "
            + "prog_code = ?, "
            + "prog_s_date = ?, "
            + "prog_e_date = ?, "
            + "gift_no = ?, "
            + "spec_gift_no = ?, "
            + "per_amt_cond = ?, "
            + "per_amt = ?, "
            + "perday_cnt_cond = ?, "
            + "perday_cnt = ?, "
            + "sum_amt_cond = ?, "
            + "sum_amt = ?, "
            + "sum_cnt_cond = ?, "
            + "sum_cnt = ?, "
            + "purch_feed_type = ?, "
            + "threshold_sel = ?, "
            + "purchase_type_sel = ?, "
            + "purchase_amt_s1 = ?, "
            + "purchase_amt_e1 = ?, "
            + "feedback_amt_1 = ?, "
            + "purchase_amt_s2 = ?, "
            + "purchase_amt_e2 = ?, "
            + "feedback_amt_2 = ?, "
            + "purchase_amt_s3 = ?, "
            + "purchase_amt_e3 = ?, "
            + "feedback_amt_3 = ?, "
            + "purchase_amt_s4 = ?, "
            + "purchase_amt_e4 = ?, "
            + "feedback_amt_4 = ?, "
            + "purchase_amt_s5 = ?, "
            + "purchase_amt_e5 = ?, "
            + "feedback_amt_5 = ?, "
            + "feedback_limit = ?, "
            + "crt_user  = ?, "
            + "crt_date  = ?, "
            + "apr_user  = ?, "
            + "apr_date  = to_char(sysdate,'yyyymmdd'), "
            + "apr_flag  = ?, "
            + "mod_user  = ?, "
            + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "mod_pgm   = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1,"
            + "new_hldr_flag = ?, "
            + "mcht_seq_flag = ?, "
            + "c_record_cond = ?,"
            + "c_record_group_no = ?, "
            + "mcht_in_cond = ?, "
            + "nopurc_msg_pgm = ?, "
            + "half_msg_pgm = ?, "
            + "send_msg_pgm = ?, "
            + "add_value_cond = ?,"
            + "add_value = ?,"
            + "mkt_fstp_gift_cond = ? "
            + " where 1     = 1 "
            + "and   active_code  = ? ";

    Object[] param = new Object[] {
            colStr("active_name"),
            colStr("stop_flag"),
            colStr("stop_date"),
            colStr("stop_desc"),
            colStr("issue_date_s"),
            colStr("issue_date_e"),
            colStr("effect_months"),
            colStr("purchase_days"),
            colStr("n1_days"),
            colStr("achieve_cond"),
            colStr("new_hldr_cond"),
            colStr("new_hldr_sel"),
            colStr("new_hldr_days"),
            colStr("new_group_cond"),
            colStr("new_hldr_card"),
            colStr("new_hldr_sup"),
            colStr("acct_type_sel"),
            colStr("group_code_sel"),
            colStr("source_code_sel"),
            colStr("card_type_sel"),
            colStr("promote_dept_sel"),
            colStr("list_cond"),
            colStr("list_flag"),
            colStr("list_use_sel"),
            colStr("mcc_code_sel"),
            colStr("merchant_sel"),
            colStr("mcht_group_sel"),
            colStr("mcht_in_amt"),
            colStr("in_merchant_sel"),
            colStr("in_mcht_group_sel"),
            colStr("pos_entry_sel"),
            colStr("ucaf_sel"),
            colStr("eci_sel"),
            colStr("bl_cond"),
            colStr("ca_cond"),
            colStr("it_cond"),
            colStr("it_flag"),
            colStr("id_cond"),
            colStr("ao_cond"),
            colStr("ot_cond"),
            colStr("linebc_cond"),
            colStr("banklite_cond"),
            colStr("anulfee_cond"),
            colStr("anulfee_days"),
            colStr("action_pay_cond"),
            colStr("action_pay_times"),
            colStr("selfdeduct_cond"),
            colStr("sms_nopurc_cond"),
            colStr("sms_nopurc_days"),
            colStr("nopurc_msg_id_g"),
            colStr("nopurc_msg_id_c"),
            colStr("sms_half_cond"),
            colStr("sms_half_days"),
            colStr("half_cnt_cond"),
            colStr("half_cnt"),
            colStr("half_andor_cond"),
            colStr("half_amt_cond"),
            colStr("half_amt"),
            colStr("half_msg_id_g"),
            colStr("half_msg_id_c"),
            colStr("sms_send_cond"),
            colStr("sms_send_days"),
            colStr("send_msg_id"),
            colStr("multi_fb_type"),
            colStr("record_cond"),
            colStr("record_group_no"),
            colStr("active_type"),
            colStr("bonus_type"),
            colStr("tax_flag"),
            colStr("fund_code"),
            colStr("group_type"),
            colStr("prog_code"),
            colStr("prog_s_date"),
            colStr("prog_e_date"),
            colStr("gift_no"),
            colStr("spec_gift_no"),
            colStr("per_amt_cond"),
            colStr("per_amt"),
            colStr("perday_cnt_cond"),
            colStr("perday_cnt"),
            colStr("sum_amt_cond"),
            colStr("sum_amt"),
            colStr("sum_cnt_cond"),
            colStr("sum_cnt"),
            colStr("purch_feed_type"),
            colStr("threshold_sel"),
            colStr("purchase_type_sel"),
            colStr("purchase_amt_s1"),
            colStr("purchase_amt_e1"),
            colStr("feedback_amt_1"),
            colStr("purchase_amt_s2"),
            colStr("purchase_amt_e2"),
            colStr("feedback_amt_2"),
            colStr("purchase_amt_s3"),
            colStr("purchase_amt_e3"),
            colStr("feedback_amt_3"),
            colStr("purchase_amt_s4"),
            colStr("purchase_amt_e4"),
            colStr("feedback_amt_4"),
            colStr("purchase_amt_s5"),
            colStr("purchase_amt_e5"),
            colStr("feedback_amt_5"),
            colStr("feedback_limit"),
            colStr("crt_user"),
            colStr("crt_date"),
            wp.loginUser,
            aprFlag,
            colStr("mod_user"),
            colStr("mod_time"),
            colStr("mod_pgm"),
            colStr("new_hldr_flag"),
            colStr("mcht_seq_flag"),
            colStr("c_record_cond"),
            colStr("c_record_group_no"),
            colStr("mcht_in_cond"),
            colStr("nopurc_msg_pgm"),
            colStr("half_msg_pgm"),
            colStr("send_msg_pgm"),
            colStr("add_value_cond"),
            colStr("add_value"),
            colStr("mkt_fstp_gift_cond"),
            colStr("active_code")
    };

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
    strSql = "delete " + approveTabName + " " + "where 1 = 1 " + "and active_code = ? ";

    Object[] param = new Object[] {colStr("active_code")};

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
  public int dbDeleteD4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_bn_data " + "where 1 = 1 " + "and table_name  =  'MKT_FSTP_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("active_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete mkt_bn_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_FSTP_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("active_code"),};

    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_data_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Bndata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_bn_data " + "select * " + "from  mkt_bn_data_t " + "where 1 = 1 "
        + "and table_name  =  'MKT_FSTP_PARM' " + "and data_key  = ?  ";

    Object[] param = new Object[] {colStr("active_code"),};

    sqlExec(strSql, param);

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4Dmlist() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete from mkt_imfstp_list " + "where 1 = 1 " + "and active_code = ? and active_seq = '00' ";

    Object[] param = new Object[] 
            {
            colStr("active_code")
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
    strSql = "delete from mkt_imfstp_list_t " + "where 1 = 1 " + "and active_code = ? and active_seq = '00' ";

    Object[] param = new Object[] 
            {
            colStr("active_code")
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
        + "where 1 = 1 " + "and active_code = ? and active_seq = '00' ";

    Object[] param = new Object[] 
            {
            colStr("active_code")
            };

    sqlExec(strSql, param);

    return 1;
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
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }
  // ************************************************************************

}  // End of class
