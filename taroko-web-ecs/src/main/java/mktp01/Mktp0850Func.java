/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/26  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 110-08-31  V1.00.03  Wendy Lu   程式修改     
* 111-07-28  V1.00.04  machao          覆核Bug處理                                                                     *
* 112-01-11  V1.00.05  Zuwei Su        配合mktm0850 [特店中文名稱]、[特店英文名稱]、[交易平台種類]  欄位增加而調整mktp0850明細資料                                                         *
* 112-01-18  V1.00.06  Zuwei Su        dbInsertA4() insert sql values 參數個數不匹配                                                        *
* 112-02-02  V1.00.07  Zuwei Su        新增mkt_bn_cdata和mkt_bn_cdata_t的新增刪除method dbDeleteD4BnCdata, dbDeleteD4TBnCdata, dbInsertA4BnCdata                                                      *
* 112-02-16  V1.00.08  Zuwei Su        刪除[交易平台種類]選項，增加 [一般消費群組]選項
* 112-02-17  V1.00.09  Zuwei Su        insert mkt_channel_parm 參數錯誤
* 112-03-16  V1.00.10  Machao         增加 [通路類別]選項
* 112-05-16  V1.00.11  Ryan           增一般名單產檔格式、回饋周期 的參數設定
* 112-05-18  V1.00.12  Ryan           增加按月回饋取得分析日期的邏輯
* 112-06-09  V1.00.13  Ryan           調整按月回饋取得分析日期的邏輯
* 112-06-19  V1.00.14  Ryan           feedback_cycle = D ,修正分析日期
* 112-10-13  V1.00.15  Zuwei Su       增[消費累計基礎],增[當期帳單(年月)]  *
* 112-11-03  V1.00.16  Zuwei Su       當期帳單(年月)取營業日或分析日期上一個月  *
* 112-11-08  V1.00.17  Zuwei Su       調整當期帳單(年月)取值  *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;

import taroko.base.CommDate;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0850Func extends busi.FuncProc {
  private String PROGNAME = "行銷通路活動回饋參數檔覆核處理程式112/02/16 V1.00.08";
  String kk1;
  String approveTabName = "mkt_channel_parm";
  String controlTabName = "mkt_channel_parm_t";
  CommDate comDate = new CommDate();
  public Mktp0850Func(TarokoCommon wr) {
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
    setCalDefDate();
    strSql= " insert into  " + approveTabName+ " ("
            + " active_code, "
            + " active_name, "
            + " stop_flag, "
            + " stop_date, "
            + " bonus_type_cond, "
            + " bonus_type, "
            + " tax_flag, "
            + " b_effect_months, "
            + " fund_code_cond, "
            + " fund_code, "
            + " f_effect_months, "
            + " other_type_cond, "
            + " spec_gift_no, "
            + " send_msg_pgm, "
            + " lottery_cond, "
            + " lottery_type, "
            + " prog_code, "
            + " prog_msg_pgm, "
            + " accumulate_term_sel,"
            + " acct_month, "
            + " purchase_date_s, "
            + " purchase_date_e, "
            + " cal_def_date, "
            + " list_cond, "
            + " list_flag, "
            + " list_use_sel, "
            + " acct_type_sel, "
            + " group_code_sel, "
            + " mcc_code_sel, "
            + " merchant_sel, "
            + " mcht_group_sel, "
            + " mcht_cname_sel, "
            + " mcht_ename_sel, "
            + " it_term_sel, "
            + " terminal_id_sel, "
            + " pos_entry_sel, "
            + " platform_kind_sel, "
            + " platform_group_sel, "
            + " channel_type_sel, "
            + " bl_cond, "
            + " ca_cond, "
            + " it_cond, "
            + " it_flag, "
            + " id_cond, "
            + " ao_cond, "
            + " ot_cond, "
            + " minus_txn_cond, "
            + " block_cond, "
            + " oppost_cond, "
            + " payment_rate_cond, "
            + " record_cond, "
            + " feedback_key_sel, "
            + " purchase_type_sel, "
            + " per_amt_cond, "
            + " per_amt, "
            + " perday_cnt_cond, "
            + " perday_cnt, "
            + " max_cnt_cond, "
            + " max_cnt, "
            + " sum_amt_cond, "
            + " sum_amt, "
            + " sum_cnt_cond, "
            + " sum_cnt, "
            + " above_cond, "
            + " above_amt, "
            + " above_cnt, "
            + " b_feedback_limit, "
            + " f_feedback_limit, "
            + " s_feedback_limit, "
            + " l_feedback_limit, "
            + " b_feedback_cnt_limit, "
            + " f_feedback_cnt_limit, "
            + " s_feedback_cnt_limit, "
            + " l_feedback_cnt_limit, "
            + " threshold_sel, "
            + " purchase_amt_s1, "
            + " purchase_amt_e1, "
            + " active_type_1, "
            + " feedback_rate_1, "
            + " feedback_amt_1, "
            + " feedback_lmt_cnt_1, "
            + " feedback_lmt_amt_1, "
            + " purchase_amt_s2, "
            + " purchase_amt_e2, "
            + " active_type_2, "
            + " feedback_rate_2, "
            + " feedback_amt_2, "
            + " feedback_lmt_cnt_2, "
            + " feedback_lmt_amt_2, "
            + " purchase_amt_s3, "
            + " purchase_amt_e3, "
            + " active_type_3, "
            + " feedback_rate_3, "
            + " feedback_amt_3, "
            + " feedback_lmt_cnt_3, "
            + " feedback_lmt_amt_3, "
            + " purchase_amt_s4, "
            + " purchase_amt_e4, "
            + " active_type_4, "
            + " feedback_rate_4, "
            + " feedback_amt_4, "
            + " feedback_lmt_cnt_4, "
            + " feedback_lmt_amt_4, "
            + " purchase_amt_s5, "
            + " purchase_amt_e5, "
            + " active_type_5, "
            + " feedback_rate_5, "
            + " feedback_amt_5, "
            + " feedback_lmt_cnt_5, "
            + " feedback_lmt_amt_5, "
            + " feedback_date, "
            + " feedback_apr_date, "
            + " bonus_date, "
            + " fund_date, "
            + " gift_date, "
            + " lottery_date, "
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " mod_time, "
            + " mod_user, "
            + " mod_seqno, "
            + " mod_pgm,"
            + " feedback_cycle,"
            + " feedback_dd,"
            + " outfile_type "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "?,"
            + " timestamp_format(?,'yyyymmddhh24miss'), "
            + "?,"
            + "?,"
            + " ?,?,?,?) ";

    Object[] param =new Object[]
         {
          colStr("active_code"),
          colStr("active_name"),
          colStr("stop_flag"),
          colStr("stop_date"),
          colStr("bonus_type_cond"),
          colStr("bonus_type"),
          colStr("tax_flag"),
          colStr("b_effect_months"),
          colStr("fund_code_cond"),
          colStr("fund_code"),
          colStr("f_effect_months"),
          colStr("other_type_cond"),
          colStr("spec_gift_no"),
          colStr("send_msg_pgm"),
          colStr("lottery_cond"),
          colStr("lottery_type"),
          colStr("prog_code"),
          colStr("prog_msg_pgm"),
          colStr("accumulate_term_sel"),
          colStr("acct_month"),
          colStr("purchase_date_s"),
          colStr("purchase_date_e"),
          colStr("cal_def_date"),
          colStr("list_cond"),
          colStr("list_flag"),
          colStr("list_use_sel"),
          colStr("acct_type_sel"),
          colStr("group_code_sel"),
          colStr("mcc_code_sel"),
          colStr("merchant_sel"),
          colStr("mcht_group_sel"),
          colStr("mcht_cname_sel"),
          colStr("mcht_ename_sel"),
          colStr("it_term_sel"),
          colStr("terminal_id_sel"),
          colStr("pos_entry_sel"),
          colStr("platform_kind_sel"),
          colStr("platform_group_sel"),
          colStr("channel_type_sel"),
          colStr("bl_cond"),
          colStr("ca_cond"),
          colStr("it_cond"),
          colStr("it_flag"),
          colStr("id_cond"),
          colStr("ao_cond"),
          colStr("ot_cond"),
          colStr("minus_txn_cond"),
          colStr("block_cond"),
          colStr("oppost_cond"),
          colStr("payment_rate_cond"),
          colStr("record_cond"),
          colStr("feedback_key_sel"),
          colStr("purchase_type_sel"),
          colStr("per_amt_cond"),
          colStr("per_amt"),
          colStr("perday_cnt_cond"),
          colStr("perday_cnt"),
          colStr("max_cnt_cond"),
          colStr("max_cnt"),
          colStr("sum_amt_cond"),
          colStr("sum_amt"),
          colStr("sum_cnt_cond"),
          colStr("sum_cnt"),
          colStr("above_cond"),
          colStr("above_amt"),
          colStr("above_cnt"),
          colStr("b_feedback_limit"),
          colStr("f_feedback_limit"),
          colStr("s_feedback_limit"),
          colStr("l_feedback_limit"),
          colStr("b_feedback_cnt_limit"),
          colStr("f_feedback_cnt_limit"),
          colStr("s_feedback_cnt_limit"),
          colStr("l_feedback_cnt_limit"),
          colStr("threshold_sel"),
          colStr("purchase_amt_s1"),
          colStr("purchase_amt_e1"),
          colStr("active_type_1"),
          colStr("feedback_rate_1"),
          colStr("feedback_amt_1"),
          colStr("feedback_lmt_cnt_1"),
          colStr("feedback_lmt_amt_1"),
          colStr("purchase_amt_s2"),
          colStr("purchase_amt_e2"),
          colStr("active_type_2"),
          colStr("feedback_rate_2"),
          colStr("feedback_amt_2"),
          colStr("feedback_lmt_cnt_2"),
          colStr("feedback_lmt_amt_2"),
          colStr("purchase_amt_s3"),
          colStr("purchase_amt_e3"),
          colStr("active_type_3"),
          colStr("feedback_rate_3"),
          colStr("feedback_amt_3"),
          colStr("feedback_lmt_cnt_3"),
          colStr("feedback_lmt_amt_3"),
          colStr("purchase_amt_s4"),
          colStr("purchase_amt_e4"),
          colStr("active_type_4"),
          colStr("feedback_rate_4"),
          colStr("feedback_amt_4"),
          colStr("feedback_lmt_cnt_4"),
          colStr("feedback_lmt_amt_4"),
          colStr("purchase_amt_s5"),
          colStr("purchase_amt_e5"),
          colStr("active_type_5"),
          colStr("feedback_rate_5"),
          colStr("feedback_amt_5"),
          colStr("feedback_lmt_cnt_5"),
          colStr("feedback_lmt_amt_5"),
          colStr("feedback_date"),
          colStr("feedback_apr_date"),
          colStr("bonus_date"),
          colStr("fund_date"),
          colStr("gift_date"),
          colStr("lottery_date"),
          "Y",
          wp.loginUser,
          colStr("crt_date"),
          colStr("crt_user"),
          wp.sysDate + wp.sysTime,
          wp.loginUser,
          colStr("mod_seqno"),  
          wp.modPgm(),
          colStr("feedback_cycle"),
          colStr("feedback_dd"),
          colStr("outfile_type")
         };

    sqlExec(strSql, param);

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
            + " bonus_type_cond, "
            + " bonus_type, "
            + " tax_flag, "
            + " b_effect_months, "
            + " fund_code_cond, "
            + " fund_code, "
            + " f_effect_months, "
            + " other_type_cond, "
            + " spec_gift_no, "
            + " send_msg_pgm, "
            + " lottery_cond, "
            + " lottery_type, "
            + " prog_code, "
            + " prog_msg_pgm,"
            + " accumulate_term_sel,"
            + " acct_month, "
            + " purchase_date_s, "
            + " purchase_date_e, "
            + " cal_def_date, "
            + " list_cond, "
            + " list_flag, "
            + " list_use_sel, "
            + " acct_type_sel, "
            + " group_code_sel, "
            + " mcc_code_sel, "
            + " merchant_sel, "
            + " mcht_group_sel, "
            + " mcht_cname_sel, "
            + " mcht_ename_sel, "
            + " it_term_sel, "
            + " terminal_id_sel, "
            + " pos_entry_sel, "
            + " platform_kind_sel, "
            + " platform_group_sel, "
            + " channel_type_sel, "
            + " bl_cond, "
            + " ca_cond, "
            + " it_cond, "
            + " it_flag, "
            + " id_cond, "
            + " ao_cond, "
            + " ot_cond, "
            + " minus_txn_cond, "
            + " block_cond, "
            + " oppost_cond, "
            + " payment_rate_cond, "
            + " record_cond, "
            + " feedback_key_sel, "
            + " purchase_type_sel, "
            + " per_amt_cond, "
            + " per_amt, "
            + " perday_cnt_cond, "
            + " perday_cnt, "
            + " max_cnt_cond, "
            + " max_cnt, "
            + " sum_amt_cond, "
            + " sum_amt, "
            + " sum_cnt_cond, "
            + " sum_cnt, "
            + " above_cond, "
            + " above_amt, "
            + " above_cnt, "
            + " b_feedback_limit, "
            + " f_feedback_limit, "
            + " s_feedback_limit, "
            + " l_feedback_limit, "
            + " b_feedback_cnt_limit, "
            + " f_feedback_cnt_limit, "
            + " s_feedback_cnt_limit, "
            + " l_feedback_cnt_limit, "
            + " threshold_sel, "
            + " purchase_amt_s1, "
            + " purchase_amt_e1, "
            + " active_type_1, "
            + " feedback_rate_1, "
            + " feedback_amt_1, "
            + " feedback_lmt_cnt_1, "
            + " feedback_lmt_amt_1, "
            + " purchase_amt_s2, "
            + " purchase_amt_e2, "
            + " active_type_2, "
            + " feedback_rate_2, "
            + " feedback_amt_2, "
            + " feedback_lmt_cnt_2, "
            + " feedback_lmt_amt_2, "
            + " purchase_amt_s3, "
            + " purchase_amt_e3, "
            + " active_type_3, "
            + " feedback_rate_3, "
            + " feedback_amt_3, "
            + " feedback_lmt_cnt_3, "
            + " feedback_lmt_amt_3, "
            + " purchase_amt_s4, "
            + " purchase_amt_e4, "
            + " active_type_4, "
            + " feedback_rate_4, "
            + " feedback_amt_4, "
            + " feedback_lmt_cnt_4, "
            + " feedback_lmt_amt_4, "
            + " purchase_amt_s5, "
            + " purchase_amt_e5, "
            + " active_type_5, "
            + " feedback_rate_5, "
            + " feedback_amt_5, "
            + " feedback_lmt_cnt_5, "
            + " feedback_lmt_amt_5, "
            + " feedback_date, "
            + " feedback_apr_date, "
//            2022-07-28    新增bug調整
//            + " bonus_date, "
//            + " fund_date, "
            + " gift_date, "
//            + " lottery_date, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno,"
            + " feedback_cycle,"
            + " feedback_dd,"
            + " outfile_type "
            + " from " + procTabName 
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
    setCalDefDate();
    String apr_flag = "Y";
    strSql= "update " +approveTabName + " set "
            + "active_name = ?, "
            + "stop_flag = ?, "
            + "stop_date = ?, "
            + "bonus_type_cond = ?, "
            + "bonus_type = ?, "
            + "tax_flag = ?, "
            + "b_effect_months = ?, "
            + "fund_code_cond = ?, "
            + "fund_code = ?, "
            + "f_effect_months = ?, "
            + "other_type_cond = ?, "
            + "spec_gift_no = ?, "
            + "send_msg_pgm = ?, "
            + "lottery_cond = ?, "
            + "lottery_type = ?, "
            + "prog_code = ?, "
            + "prog_msg_pgm = ?, "
            + "accumulate_term_sel = ?,"
            + "acct_month= ?, "
            + "purchase_date_s = ?, "
            + "purchase_date_e = ?, "
            + "cal_def_date = ?, "
            + "list_cond = ?, "
            + "list_flag = ?, "
            + "list_use_sel = ?, "
            + "acct_type_sel = ?, "
            + "group_code_sel = ?, "
            + "mcc_code_sel = ?, "
            + "merchant_sel = ?, "
            + "mcht_group_sel = ?, "
            + "mcht_cname_sel = ?, "
            + "mcht_ename_sel = ?, "
            + "it_term_sel = ?, "
            + "terminal_id_sel = ?, "
            + "pos_entry_sel = ?, "
            + "platform_kind_sel = ?, "
            + "platform_group_sel = ?, "
            + "channel_type_sel = ?, "
            + "bl_cond = ?, "
            + "ca_cond = ?, "
            + "it_cond = ?, "
            + "it_flag = ?, "
            + "id_cond = ?, "
            + "ao_cond = ?, "
            + "ot_cond = ?, "
            + "minus_txn_cond = ?, "
            + "block_cond = ?, "
            + "oppost_cond = ?, "
            + "payment_rate_cond = ?, "
            + "record_cond = ?, "
            + "feedback_key_sel = ?, "
            + "purchase_type_sel = ?, "
            + "per_amt_cond = ?, "
            + "per_amt = ?, "
            + "perday_cnt_cond = ?, "
            + "perday_cnt = ?, "
            + "max_cnt_cond = ?, "
            + "max_cnt = ?, "
            + "sum_amt_cond = ?, "
            + "sum_amt = ?, "
            + "sum_cnt_cond = ?, "
            + "sum_cnt = ?, "
            + "above_cond = ?, "
            + "above_amt = ?, "
            + "above_cnt = ?, "
            + "b_feedback_limit = ?, "
            + "f_feedback_limit = ?, "
            + "s_feedback_limit = ?, "
            + "l_feedback_limit = ?, "
            + "b_feedback_cnt_limit = ?, "
            + "f_feedback_cnt_limit = ?, "
            + "s_feedback_cnt_limit = ?, "
            + "l_feedback_cnt_limit = ?, "
            + "threshold_sel = ?, "
            + "purchase_amt_s1 = ?, "
            + "purchase_amt_e1 = ?, "
            + "active_type_1 = ?, "
            + "feedback_rate_1 = ?, "
            + "feedback_amt_1 = ?, "
            + "feedback_lmt_cnt_1 = ?, "
            + "feedback_lmt_amt_1 = ?, "
            + "purchase_amt_s2 = ?, "
            + "purchase_amt_e2 = ?, "
            + "active_type_2 = ?, "
            + "feedback_rate_2 = ?, "
            + "feedback_amt_2 = ?, "
            + "feedback_lmt_cnt_2 = ?, "
            + "feedback_lmt_amt_2 = ?, "
            + "purchase_amt_s3 = ?, "
            + "purchase_amt_e3 = ?, "
            + "active_type_3 = ?, "
            + "feedback_rate_3 = ?, "
            + "feedback_amt_3 = ?, "
            + "feedback_lmt_cnt_3 = ?, "
            + "feedback_lmt_amt_3 = ?, "
            + "purchase_amt_s4 = ?, "
            + "purchase_amt_e4 = ?, "
            + "active_type_4 = ?, "
            + "feedback_rate_4 = ?, "
            + "feedback_amt_4 = ?, "
            + "feedback_lmt_cnt_4 = ?, "
            + "feedback_lmt_amt_4 = ?, "
            + "purchase_amt_s5 = ?, "
            + "purchase_amt_e5 = ?, "
            + "active_type_5 = ?, "
            + "feedback_rate_5 = ?, "
            + "feedback_amt_5 = ?, "
            + "feedback_lmt_cnt_5 = ?, "
            + "feedback_lmt_amt_5 = ?, "
            + "crt_user  = ?, "
            + "crt_date  = ?, "
            + "apr_user  = ?, "
            + "apr_date  = to_char(sysdate,'yyyymmdd'), "
            + "apr_flag  = ?, "
            + "mod_user  = ?, "
            + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
            + "mod_pgm   = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1,"
            + "feedback_cycle = ?,"
            + "feedback_dd = ?,"
            + "outfile_type= ? "
            + "where 1     = 1 " 
            + "and   active_code  = ? "
            ;

     Object[] param =new Object[]
       {
        colStr("active_name"),
        colStr("stop_flag"),
        colStr("stop_date"),
        colStr("bonus_type_cond"),
        colStr("bonus_type"),
        colStr("tax_flag"),
        colStr("b_effect_months"),
        colStr("fund_code_cond"),
        colStr("fund_code"),
        colStr("f_effect_months"),
        colStr("other_type_cond"),
        colStr("spec_gift_no"),
        colStr("send_msg_pgm"),
        colStr("lottery_cond"),
        colStr("lottery_type"),
        colStr("prog_code"),
        colStr("prog_msg_pgm"),
        colStr("accumulate_term_sel"),
        colStr("acct_month"),
        colStr("purchase_date_s"),
        colStr("purchase_date_e"),
        colStr("cal_def_date"),
        colStr("list_cond"),
        colStr("list_flag"),
        colStr("list_use_sel"),
        colStr("acct_type_sel"),
        colStr("group_code_sel"),
        colStr("mcc_code_sel"),
        colStr("merchant_sel"),
        colStr("mcht_group_sel"),
        colStr("mcht_cname_sel"),
        colStr("mcht_ename_sel"),
        colStr("it_term_sel"),
        colStr("terminal_id_sel"),
        colStr("pos_entry_sel"),
        colStr("platform_kind_sel"),
        colStr("platform_group_sel"),
        colStr("channel_type_sel"),
        colStr("bl_cond"),
        colStr("ca_cond"),
        colStr("it_cond"),
        colStr("it_flag"),
        colStr("id_cond"),
        colStr("ao_cond"),
        colStr("ot_cond"),
        colStr("minus_txn_cond"),
        colStr("block_cond"),
        colStr("oppost_cond"),
        colStr("payment_rate_cond"),
        colStr("record_cond"),
        colStr("feedback_key_sel"),
        colStr("purchase_type_sel"),
        colStr("per_amt_cond"),
        colStr("per_amt"),
        colStr("perday_cnt_cond"),
        colStr("perday_cnt"),
        colStr("max_cnt_cond"),
        colStr("max_cnt"),
        colStr("sum_amt_cond"),
        colStr("sum_amt"),
        colStr("sum_cnt_cond"),
        colStr("sum_cnt"),
        colStr("above_cond"),
        colStr("above_amt"),
        colStr("above_cnt"),
        colStr("b_feedback_limit"),
        colStr("f_feedback_limit"),
        colStr("s_feedback_limit"),
        colStr("l_feedback_limit"),
        colStr("b_feedback_cnt_limit"),
        colStr("f_feedback_cnt_limit"),
        colStr("s_feedback_cnt_limit"),
        colStr("l_feedback_cnt_limit"),
        colStr("threshold_sel"),
        colStr("purchase_amt_s1"),
        colStr("purchase_amt_e1"),
        colStr("active_type_1"),
        colStr("feedback_rate_1"),
        colStr("feedback_amt_1"),
        colStr("feedback_lmt_cnt_1"),
        colStr("feedback_lmt_amt_1"),
        colStr("purchase_amt_s2"),
        colStr("purchase_amt_e2"),
        colStr("active_type_2"),
        colStr("feedback_rate_2"),
        colStr("feedback_amt_2"),
        colStr("feedback_lmt_cnt_2"),
        colStr("feedback_lmt_amt_2"),
        colStr("purchase_amt_s3"),
        colStr("purchase_amt_e3"),
        colStr("active_type_3"),
        colStr("feedback_rate_3"),
        colStr("feedback_amt_3"),
        colStr("feedback_lmt_cnt_3"),
        colStr("feedback_lmt_amt_3"),
        colStr("purchase_amt_s4"),
        colStr("purchase_amt_e4"),
        colStr("active_type_4"),
        colStr("feedback_rate_4"),
        colStr("feedback_amt_4"),
        colStr("feedback_lmt_cnt_4"),
        colStr("feedback_lmt_amt_4"),
        colStr("purchase_amt_s5"),
        colStr("purchase_amt_e5"),
        colStr("active_type_5"),
        colStr("feedback_rate_5"),
        colStr("feedback_amt_5"),
        colStr("feedback_lmt_cnt_5"),
        colStr("feedback_lmt_amt_5"),
        colStr("crt_user"),
        colStr("crt_date"),
        wp.loginUser,
        apr_flag,
        colStr("mod_user"),
        wp.sysDate + wp.sysTime,
        wp.modPgm(),
        colStr("feedback_cycle"),
        colStr("feedback_dd"),
        colStr("outfile_type"),
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
    strSql = "delete from " + approveTabName + " " + "where 1 = 1 " + "and active_code = ? ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code")
    		};

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
    strSql = "delete from mkt_bn_data " + "where 1 = 1 " + "and table_name  =  'MKT_CHANNEL_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code"),
    		};
    
    wp.dupRecord = "Y";
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
    strSql = "delete from mkt_bn_data_t " + "where 1 = 1 " + "and table_name  =  'MKT_CHANNEL_PARM' "
        + "and data_key  = ?  ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code")
    		};
    
    wp.dupRecord = "Y";
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
        + "and table_name  =  'MKT_CHANNEL_PARM' " + "and data_key  = ?  ";

    Object[] param = new Object[] 
    		{
    		   colStr("active_code"),
    		};

    sqlExec(strSql, param);

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4BnCdata() {
    rc = dbSelectS4();
    if (rc != 1)
        return rc;
    strSql = "delete from mkt_bn_cdata "
            + "where 1 = 1 "
            + "and table_name  =  'MKT_CHANNEL_PARM' "
            + "and data_key  = ?  ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code"),
    		};
    
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_data 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TBnCdata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
  strSql = "delete from mkt_bn_cdata_t "
          + "where 1 = 1 "
          + "and table_name  =  'MKT_CHANNEL_PARM' "
          + "and data_key  = ?  ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code")
    		};
    
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_bn_cdata_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4BnCdata() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
  strSql = "insert into mkt_bn_cdata "
          + "select * "
          + "from  mkt_bn_cdata_t "
          + "where 1 = 1 "
          + "and table_name  =  'MKT_CHANNEL_PARM' "
          + "and data_key  = ?  ";

    Object[] param = new Object[] 
    		{
    		   colStr("active_code"),
    		};

    sqlExec(strSql, param);

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4Dmlist() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete from mkt_imchannel_list " + "where 1 = 1 " + "and active_code = ? ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code")
    		};
    
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_imchannel_list 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbDeleteD4TDmlist() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "delete from mkt_imchannel_list_t " + "where 1 = 1 " + "and active_code = ? ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code")
    		};
    
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (rc != 1)
      errmsg("刪除 mkt_imchannel_list_T 錯誤");

    return 1;
  }

  // ************************************************************************
  public int dbInsertA4Dmlist() {
    rc = dbSelectS4();
    if (rc != 1)
      return rc;
    strSql = "insert into mkt_imchannel_list " + "select * " + "from  mkt_imchannel_list_t "
        + "where 1 = 1 " + "and active_code = ? ";

    Object[] param = new Object[] 
    		{
    		colStr("active_code")
    		};

    sqlExec(strSql, param);

    return 1;
  }

  // ************************************************************************
  public int dbDelete() {
    strSql = "delete  from " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] 
    		{
    		wp.itemRowId("wprowid")
    		};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
    	
     errmsg("刪除 " + controlTabName + " 錯誤");
     return(-1);
    }
    return rc;
  }
  
  // ************************************************************************
  public void setCalDefDate() {
	
	    String feedbackCycle = colStr("feedback_cycle");
	    String feedbackDd = colStr("feedback_dd");
	    String purchaseDateE = colStr("purchase_date_e");
        String accumulateTermSel = colStr("accumulate_term_sel");
	    String newSalDefDate = wp.sysDate;
        if ("M".equals(feedbackCycle)) {
            // 按月回饋
            if ("2".equals(accumulateTermSel)) {
                // 如果 feedbackcycle='M' (按月回饋), 且採 '當期帳單', 那麼就比較 回饋日 與 營業日期;
                // 回饋日期(dd) >= 營業日期(dd), cal_def_date = 營業日期當月+ 回饋日期(dd)
                // else if 回饋日期(dd) < 營業日期(dd)
                // cal_def_date = 營業日期次月+ 回饋日期(dd)
                if (feedbackDd.compareTo(newSalDefDate.substring(6)) >= 0) {
                    newSalDefDate = newSalDefDate.substring(0, 6) + feedbackDd;
                } else {
                    newSalDefDate = comDate.monthAdd(newSalDefDate, 1) + feedbackDd;
                }
            } else {
                while (true) {
                    newSalDefDate = checkDate(newSalDefDate, feedbackDd);
                    if (strToInt(strMid(newSalDefDate, 0, 6)) > 0 
                            && strToInt(strMid(newSalDefDate, 0, 6)) > strToInt(comDate.monthAdd(purchaseDateE, 1))) {
                        newSalDefDate = strMid(purchaseDateE, 0, 6) + feedbackDd;
                        continue;
                    }
                    break;
                }
            }
            colSet("cal_def_date", newSalDefDate);
        } else {
            // 非"按月回饋"
            newSalDefDate = colStr("cal_def_date");
        }
        if ("2".equals(accumulateTermSel)) {
            // 當期帳單
            colSet("acct_month", comDate.monthAdd(newSalDefDate, -1));
        } else {
            // 非"當期帳單"
            colSet("acct_month", "");
        }
    }
  
  public String checkDate(String newSalDefDate ,String feedbackDd) {
	  newSalDefDate = strMid(newSalDefDate, 0, 6) + String.format("%02d", strToInt(feedbackDd));
	  if(strToInt(newSalDefDate) < strToInt(wp.sysDate)) {
		  newSalDefDate = comDate.dateAdd(newSalDefDate,0,1,0);
	  }
	  while(comDate.isDate(newSalDefDate) == false) {
		  newSalDefDate = String.format("%08d", strToInt(newSalDefDate) -1);
		  if (strToInt(newSalDefDate) < 0) {
		      break;
		  }
	  }
	  return newSalDefDate;
  }

} // End of class
