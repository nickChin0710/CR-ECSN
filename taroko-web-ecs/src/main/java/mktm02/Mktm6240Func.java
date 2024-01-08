/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/10/09  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-07-17  V1.00.04   shiyuqi        rename tableName &FiledName   
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名  
* 111-08-03  V1.00.03   machao        頁面bug調整     
* 111-08-08  V1.00.03   machao        頁面門檻蘭bug調整      
* 111/11/1   V1.00.05   machao        程式頁面，欄位調整                      *
* 112/02/03  V1.00.06   Grace Huag   基金更名為現金回饋                       *    
* 112/03/24  V1.00.07   Zuwei Su     增匯入名單功能                *
* 112/04/04  V1.00.08   Ryan         修改名單匯入時預設活動序號帶00                *
* 112/06/01  V1.00.09   Zuwei Su     依MEGA 20230216 版本調整dataCheck(), insert/update增加欄位mcht_in_flag，暫時註解                *
* 112/06/05  V1.00.10   Grace Huang  insert/update欄位mcht_in_flag更名為mcht_in_cond; megalite_cond更名為banklite_cond  *
* 112/06/21  V1.00.11   Zuwei Su     欄位new_hldr_sel沒有寫入db                *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6240Func extends FuncEdit {
  private String PROGNAME = "首刷禮活動回饋參數處理程式111/11/1 V1.00.01";
  String activeCode;
  String orgControlTabName = "mkt_fstp_parm";
  String controlTabName = "mkt_fstp_parm_t";

  public Mktm6240Func(TarokoCommon wr) {
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
    strSql = " select "
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
//            + " mcht_in_flag, "
			+ " mcht_in_cond, "
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
            + " selfdeduct_cond, "
            + " anulfee_cond, "
            + " anulfee_days, "
            + " action_pay_cond, "
            + " action_pay_times, "
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
            + " sms_addvalue_cond, "
            + " sms_addvalue_days, "
            + " addvalue_msg_id_g, "
            + " addvalue_msg_id_c, "
            + " sms_send_cond, "
            + " send_msg_id, "
            + " sms_send_days, "
            + " multi_fb_type, "
            + " record_cond, "
            + " record_group_no, "
            + " active_type, "
            + " bonus_type, "
            + " tax_flag, "
            + " fund_code, "
            + " group_type, "
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
            + " add_value_cond, "
            + " add_value, "
            + " mkt_fstp_gift_cond, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, "
            + " nopurc_msg_pgm, "
            + "half_msg_pgm, "
            + " c_record_cond, "
            + " c_record_group_no,"
            + " new_hldr_flag,"
            + " mcht_seq_flag,"
            + " nopurc_g_cond,"
            + " half_g_cond,"
            + " send_msg_pgm "
            + " from "
            + procTabName
            + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
      if (!this.ibDelete) {
          if (wp.colStr("storetype").equals("Y")) {
              errmsg("[查原資料]模式中, 請按[還原異動] 才可儲存 !");
              return;
          }
      }
      if (this.ibAdd) {
          activeCode = wp.itemStr("active_code");
          if (empty(activeCode)) {
              errmsg("活動代碼 不可空白");
              return;
          }
      } else {
          activeCode = wp.itemStr("active_code");
      }
      if (wp.respHtml.indexOf("_nadd") > 0)
          if (this.ibAdd)
              if (activeCode.length() > 0) {
                  strSql = "select count(*) as qua "
                          + "from "
                          + orgControlTabName
                          + " where active_code = ? ";
                  Object[] param = new Object[] {
                          activeCode
                  };
                  sqlSelect(strSql, param);
                  int qua = Integer.parseInt(colStr("qua"));
                  if (qua > 0) {
                      errmsg("[活動代碼] 不可重複(" + orgControlTabName + "), 請重新輸入!");
                      return;
                  }
              }

      if (this.ibAdd)
          if (activeCode.length() > 0) {
              strSql = "select count(*) as qua "
                      + "from "
                      + controlTabName
                      + " where active_code = ? ";
              Object[] param = new Object[] {
                      activeCode
              };
              sqlSelect(strSql, param);
              int qua = Integer.parseInt(colStr("qua"));
              if (qua > 0) {
                  errmsg("[活動代碼] 不可重複(" + controlTabName + ") ,請重新輸入!");
                  return;
              }
          }

      if (this.ibUpdate) {
          if ((wp.itemStr("acct_type_sel").equals("1"))
                  || (wp.itemStr("acct_type_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "1") == 0) {
                  errmsg("[帳戶類別] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("group_code_sel").equals("1"))
                  || (wp.itemStr("group_code_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "2") == 0) {
                  errmsg("[團體代號] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("source_code_sel").equals("1"))
                  || (wp.itemStr("source_code_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "3") == 0) {
                  errmsg("[來源代號] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("card_type_sel").equals("1"))
                  || (wp.itemStr("card_type_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "4") == 0) {
                  errmsg("[卡種] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("promote_dept_sel").equals("1"))
                  || (wp.itemStr("promote_dept_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "5") == 0) {
                  errmsg("[通路代號] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("mcc_code_sel").equals("1"))
                  || (wp.itemStr("mcc_code_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "6") == 0) {
                  errmsg("[特店類別] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("merchant_sel").equals("1"))
                  || (wp.itemStr("merchant_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "7") == 0) {
                  errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("mcht_group_sel").equals("1"))
                  || (wp.itemStr("mcht_group_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "8") == 0) {
                  errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("in_merchant_sel").equals("1"))
                  || (wp.itemStr("in_merchant_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "9") == 0) {
                  errmsg("[店內特店] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("in_mcht_group_sel").equals("1"))
                  || (wp.itemStr("in_mcht_group_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "10") == 0) {
                  errmsg("[店內群組] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("pos_entry_sel").equals("1"))
                  || (wp.itemStr("pos_entry_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "11") == 0) {
                  errmsg("[POS ENTRY] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("ucaf_sel").equals("1")) || (wp.itemStr("ucaf_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "12") == 0) {
                  errmsg("[UCAF] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if ((wp.itemStr("eci_sel").equals("1")) || (wp.itemStr("eci_sel").equals("2"))) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "13") == 0) {
                  errmsg("[ECI] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
      }
      if (!wp.itemStr("stop_flag").equals("Y"))
          wp.itemSet("stop_flag", "N");
      if (!wp.itemStr("new_hldr_cond").equals("Y"))
          wp.itemSet("new_hldr_cond", "N");
      if (!wp.itemStr("new_group_cond").equals("Y"))
          wp.itemSet("new_group_cond", "N");
      if (!wp.itemStr("mcht_in_cond").equals("Y"))
          wp.itemSet("mcht_in_cond", "N");
      if (!wp.itemStr("bl_cond").equals("Y"))
          wp.itemSet("bl_cond", "N");
      if (!wp.itemStr("ca_cond").equals("Y"))
          wp.itemSet("ca_cond", "N");
      if (!wp.itemStr("it_cond").equals("Y"))
          wp.itemSet("it_cond", "N");
      if (!wp.itemStr("id_cond").equals("Y"))
          wp.itemSet("id_cond", "N");
      if (!wp.itemStr("ao_cond").equals("Y"))
          wp.itemSet("ao_cond", "N");
      if (!wp.itemStr("ot_cond").equals("Y"))
          wp.itemSet("ot_cond", "N");
      if (!wp.itemStr("linebc_cond").equals("Y"))
          wp.itemSet("linebc_cond", "N");
//      if (!wp.itemStr("megalite_cond").equals("Y"))
//          wp.itemSet("megalite_cond", "N");
      if (!wp.itemStr("banklite_cond").equals("Y"))
          wp.itemSet("banklite_cond", "N");
      if (!wp.itemStr("selfdeduct_cond").equals("Y"))
          wp.itemSet("selfdeduct_cond", "N");
      if (!wp.itemStr("anulfee_cond").equals("Y"))
          wp.itemSet("anulfee_cond", "N");
      if (!wp.itemStr("action_pay_cond").equals("Y"))
          wp.itemSet("action_pay_cond", "N");
      if (!wp.itemStr("sms_nopurc_cond").equals("Y"))
          wp.itemSet("sms_nopurc_cond", "N");
      if (!wp.itemStr("nopurc_g_cond").equals("Y"))
          wp.itemSet("nopurc_g_cond", "N");
      if (!wp.itemStr("sms_half_cond").equals("Y"))
          wp.itemSet("sms_half_cond", "N");
      if (!wp.itemStr("half_cnt_cond").equals("Y"))
          wp.itemSet("half_cnt_cond", "N");
      if (!wp.itemStr("half_amt_cond").equals("Y"))
          wp.itemSet("half_amt_cond", "N");
      if (!wp.itemStr("half_g_cond").equals("Y"))
          wp.itemSet("half_g_cond", "N");
      if (!wp.itemStr("sms_send_cond").equals("Y"))
          wp.itemSet("sms_send_cond", "N");
      if (!wp.itemStr("c_record_cond").equals("Y"))
          wp.itemSet("c_record_cond", "N");
      if (!wp.itemStr("record_cond").equals("Y"))
          wp.itemSet("record_cond", "N");
      if (!wp.itemStr("per_amt_cond").equals("Y"))
          wp.itemSet("per_amt_cond", "N");
      if (!wp.itemStr("perday_cnt_cond").equals("Y"))
          wp.itemSet("perday_cnt_cond", "N");
      if (!wp.itemStr("sum_amt_cond").equals("Y"))
          wp.itemSet("sum_amt_cond", "N");
      if (!wp.itemStr("sum_cnt_cond").equals("Y"))
          wp.itemSet("sum_cnt_cond", "N");

      if (this.ibUpdate) {
          if ((wp.itemStr("new_hldr_cond").equals("Y"))
                  && (wp.itemStr("new_hldr_flag").equals("1"))) {
              if (wp.itemStr("new_hldr_sel").equals("3")) {
                  errmsg("[首辦/全新卡友] 不可選(3.二者皆含) !");
                  return;
              }
          }

          if ((wp.itemStr("new_hldr_cond").equals("Y"))
                  && (wp.itemStr("new_hldr_flag").equals("2"))) {
              if ((wp.itemStr("new_hldr_sel").equals("3"))
                      && (!wp.itemStr("new_group_cond").equals("Y"))) {
                  errmsg("選取3.二者皆含時 [2.於核卡日前-未持有團代]必須勾選  !");
                  return;
              }

              if (wp.itemStr("new_group_cond").equals("Y")) {
                  if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                          "G") == 0) {
                      errmsg("[2.於核卡日前-未持有團代] 明細沒有設定, 筆數不可為 0  !");
                      return;
                  }
              }
              if (wp.itemStr("new_hldr_days").length() == 0)
                  wp.itemSet("new_hldr_days", "0");
              if (wp.itemNum("new_hldr_days") == 0) {
                  errmsg("[於核卡日前 N 日] 必須輸入 !");
                  return;
              }
          }
          if (wp.itemStr("action_pay_cond").equals("Y")) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "H") == 0) {
                  errmsg("[行動支付團代] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if (wp.itemStr("nopurc_g_cond").equals("Y")) {
              if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                      "A") == 0) {
                  errmsg("[核卡後未消費簡訊團代] 明細沒有設定, 筆數不可為 0  !");
                  return;
              }
          }
          if (wp.itemStr("half_g_cond").equals("Y")) {
              if (wp.colNum("half_g_cond_cnt") == 0)
                  if (listParmDataCnt("mkt_bn_data_t", "MKT_FSTP_PARM", wp.colStr("active_code"),
                          "C") == 0) {
                      errmsg("[消費過半簡訊團代] 明細沒有設定, 筆數不可為 0  !");
                      return;
                  }
          }
      }

      if (wp.itemStr("aud_type").equals("A")) {
          if (wp.itemStr("apr_flag").equals("Y")) {
              wp.colSet("apr_flag", "N");
              wp.itemSet("apr_flag", "N");
          }
      } else {
          if (wp.itemStr("apr_flag").equals("Y")) {
              wp.colSet("apr_flag", "N");
              wp.itemSet("apr_flag", "N");
          }
      }

      if ((this.ibDelete) || (wp.itemStr("aud_type").equals("D")))
          return;

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("stop_flag").equals("Y")) {
              if (wp.itemStr("stop_date").length() == 0) {
                  errmsg("[停用日期]必須輸入 !");
                  return;
              }
          }
          if (wp.itemStr("issue_date_s").length() == 0) {
              errmsg("[發卡期間:發卡期間間起日]必須輸入 !");
              return;
          }
          if (wp.itemStr("issue_date_e").length() == 0) {
              errmsg("[發卡期間:發卡期間間迄日]必須輸入 !");
              return;
          }
          if (wp.itemStr("purchase_days").length() == 0)
              wp.itemSet("purchase_days", "0");
          if (wp.itemNum("purchase_days") == 0) {
              errmsg("[消費日期期間 :發卡後N日] 必須輸入 !");
              return;
          }
          if (wp.itemStr("n1_days").length() == 0)
              wp.itemSet("n1_days", "0");
          if (wp.itemNum("n1_days") <= 0) {
              errmsg("[最後回饋日期 :消費日後N日] 必須大於 0 !");
              return;
          }
          if (wp.itemStr("anulfee_cond").equals("Y")) {
              if (wp.itemStr("anulfee_days").length() == 0)
                  wp.itemSet("anulfee_days", "0");
              if (wp.itemNum("anulfee_days") == 0) {
                  errmsg("[有繳年費 核卡日第N日] 必須輸入 !");
                  return;
              }
          }

          if (wp.itemStr("sms_nopurc_cond").equals("Y")) {
              if (wp.itemStr("sms_nopurc_days").length() == 0)
                  wp.itemSet("sms_nopurc_days", "0");
              if (wp.itemNum("sms_nopurc_days") == 0) {
                  errmsg("[簡訊設定 :N日未消費] 必須輸入 !");
                  return;
              }
              if ((wp.itemStr("nopurc_c_cond").length() == 0)
                      && (wp.itemStr("nopurc_g_cond").length() == 0)) {
                  errmsg("[簡訊設定 :N日未消費] 簡訊選項至少要勾選一項 !");
                  return;
              }
          }

          if (wp.itemStr("sms_half_cond").equals("Y")) {
              if (wp.itemStr("sms_half_days").length() == 0)
                  wp.itemSet("sms_half_days", "0");
              if (wp.itemNum("sms_half_days") == 0) {
                  errmsg("[簡訊設定 :N日消費過半] 必須輸入 !");
                  return;
              }
              if ((!wp.itemStr("half_cnt_cond").equals("Y"))
                      && (!wp.itemStr("half_amt_cond").equals("Y"))) {
                  errmsg("[簡訊設定 :N日消費過半] 過半筆數,過半金額必須選擇一項 !");
                  return;
              }
              if (wp.itemStr("half_cnt").length() == 0)
                  wp.itemSet("half_cnt", "0");
              if ((wp.itemNum("half_cnt") == 0) && (wp.itemStr("half_cnt_cond").equals("Y"))) {
                  errmsg("[簡訊代碼:N日消費過半]過半筆數,筆數必須大於0 !");
                  return;
              }
              if (wp.itemStr("half_amt").length() == 0)
                  wp.itemSet("half_amt", "0");
              if ((wp.itemNum("half_amt") == 0) && (wp.itemStr("half_amt_cond").equals("Y"))) {
                  errmsg("[簡訊代碼:N日消費過半]過半金額,金額必須大於0 !");
                  return;
              }
              if ((wp.itemStr("half_c_cond").length() == 0)
                      && (wp.itemStr("half_g_cond").length() == 0)) {
                  errmsg("[簡訊設定 :N日消費過半]簡訊選項至少要勾選一項 !");
                  return;
              }


          }

          if ((!wp.itemStr("bl_cond").equals("Y")) && (!wp.itemStr("ot_cond").equals("Y"))
                  && (!wp.itemStr("it_cond").equals("Y")) && (!wp.itemStr("ca_cond").equals("Y"))
                  && (!wp.itemStr("id_cond").equals("Y")) && (!wp.itemStr("ao_cond").equals("Y"))) {
              errmsg("[消費本金類] 至少要選一個!");
              return;
          }

          if ((wp.itemStr("record_cond").equals("Y"))
                  && (wp.itemStr("record_group_no").length() == 0)) {
              errmsg("[參數回饋類型 :登錄群組] 必須輸入 !");
              return;
          }

          if (wp.itemStr("active_type").equals("3")) {
              if ((wp.itemStr("group_type").length() == 0)
                      || (wp.itemStr("prog_code1").length() == 0)
                      || (wp.itemStr("gift_no").length() == 0)) {
                  errmsg("[回饋型態: 群組代號,活動代碼,贈品代碼] 必須輸入 !");
                  return;
              }
          }

          if (wp.itemStr("active_type").equals("4")) {
              if (wp.itemStr("spec_gift_no").length() == 0) {
                  errmsg("[回饋型態: 商品代號] 必須輸入 !");
                  return;
              }
          }
          if (wp.itemStr("per_amt_cond").equals("Y")) {
              if (wp.itemStr("per_amt").length() == 0)
                  wp.itemSet("per_amt", "0");
              if (wp.itemNum("per_amt") == 0) {
                  errmsg("[單筆最低消費金額] 不可為 0 !");
                  return;
              }
          }

          if (wp.itemStr("sms_nopurc_cond").equals("Y")) {
              if (wp.itemStr("sms_nopurc_days").length() == 0)
                  wp.itemSet("sms_nopurc_days", "0");
              if (wp.itemNum("sms_nopurc_days") == 0) {
                  errmsg("[簡訊代碼] N日未消費]不可為 0 !");
                  return;
              }
          }

          if (wp.itemStr("sms_half_cond").equals("Y")) {
              if (wp.itemStr("sms_half_days").length() == 0)
                  wp.itemSet("sms_half_days", "0");
              if (wp.itemNum("sms_half_days") == 0) {
                  errmsg("[簡訊代碼] 日消費過半]不可為 0 !");
                  return;
              }
          }

          if (wp.itemStr("half_cnt_cond").equals("Y")) {
              if (wp.itemStr("half_cnt").length() == 0)
                  wp.itemSet("half_cnt", "0");
              if (wp.itemNum("half_cnt") == 0) {
                  errmsg("[簡訊代碼] 過半筆數]不可為 0 !");
                  return;
              }
          }

          if (wp.itemStr("half_amt_cond").equals("Y")) {
              if (wp.itemStr("half_amt").length() == 0)
                  wp.itemSet("half_amt", "0");
              if (wp.itemNum("half_amt") == 0) {
                  errmsg("[簡訊代碼] 過半金額]不可為 0 !");
                  return;
              }
          }

          if ((wp.itemStr("half_cnt_cond").equals("Y"))
                  && (wp.itemStr("half_amt_cond").equals("Y"))) {
              if (wp.itemStr("half_andor_cond").length() == 0) {
                  errmsg("[簡訊代碼] 過半金額筆數條]件不可為 0 !");
                  return;
              }
          }


          if (wp.itemStr("sum_amt_cond").equals("Y")) {
              if (wp.itemStr("sum_amt").length() == 0)
                  wp.itemSet("sum_amt", "0");
              if (wp.itemNum("sum_amt") == 0) {
                  errmsg("[累積最低消費金額] 不可為 0 !");
                  return;
              }
          }

          if (wp.itemStr("purch_rec_amt_cond").equals("Y")) {
              if (wp.itemStr("purch_rec_amt").length() == 0)
                  wp.itemSet("purch_rec_amt", "0");
              if (wp.itemNum("purch_rec_amt") == 0) {
                  errmsg("[累積最低消費筆數] 不可為 0 !");
                  return;
              }
          }

          if (wp.itemStr("sum_cnt_cond").equals("Y")) {
              if (wp.itemStr("sum_cnt").length() == 0)
                  wp.itemSet("sum_cnt", "0");
              if (wp.itemNum("sum_cnt") == 0) {
                  errmsg("[累積最低消費筆數] 不可為 0 !");
                  return;
              }
          }

          if (wp.itemStr("multi_fb_type").equals("1")) {
              if (wp.itemStr("purchase_amt_e1").length() == 0)
                  wp.itemSet("purchase_amt_e1", "0");
              if (wp.itemNum("purchase_amt_e1") == 0) {
                  errmsg("[門檻一:迄累積金額] 不可為 0 !");
                  return;
              }

              if (wp.itemStr("feedback_amt_1").length() == 0)
                  wp.itemSet("feedback_amt_1", "0");
              if (wp.itemNum("feedback_amt_1") == 0) {
                  errmsg("[門檻一:給點數/基金/贈品] 不可為 0 !");
                  return;
              }
          }

          busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

          if (wp.itemStr("active_type").equals("1")) {
              wp.itemSet("fund_code", "");
              wp.itemSet("group_type", "");
              wp.itemSet("prog_code", "");
              wp.itemSet("prog_s_date", "");
              wp.itemSet("prog_e_date", "");
              wp.itemSet("gift_no", "");
              wp.itemSet("spec_gift_no", "");
              if (wp.itemStr("bonus_type").length() == 0) {
                  errmsg("欲更換兌換紅利類別代碼, 不可空白!");
                  return;
              }
          } else if (wp.itemStr("active_type").equals("2")) {
              wp.itemSet("bonus_type", "");
              wp.itemSet("group_type", "");
              wp.itemSet("prog_code", "");
              wp.itemSet("prog_s_date", "");
              wp.itemSet("prog_e_date", "");
              wp.itemSet("gift_no", "");
              wp.itemSet("spec_gift_no", "");
              if (wp.itemStr("fund_code").length() == 0) {
                  errmsg("欲更換兌換基金代碼, 不可空白!");
                  return;
              }
          } else if (wp.itemStr("active_type").equals("3")) {
              wp.itemSet("fund_code", "");
              wp.itemSet("bonus_type", "");
              wp.itemSet("spec_gift_no", "");
              if (wp.itemStr("gift_no").length() == 0) {
                  errmsg("欲更換兌換豐富點商品代號, 不可空白!");
                  return;
              }
          } else if (wp.itemStr("active_type").equals("4")) {
              wp.itemSet("fund_code", "");
              wp.itemSet("bonus_type", "");
              wp.itemSet("group_type", "");
              wp.itemSet("prog_code", "");
              wp.itemSet("prog_s_date", "");
              wp.itemSet("prog_e_date", "");
              wp.itemSet("gift_no", "");
              if (wp.itemStr("spec_gift_no").length() == 0) {
                  errmsg("欲更換兌換特殊商品代號, 不可空白!");
                  return;
              }
          }
          if (!wp.itemStr("record_cond").equals("Y")) {
              wp.itemSet("record_group_no", "");
          }
      }

      if ((this.ibAdd) && (wp.itemStr("aud_type").equals("D"))) {
          strSql = "select active_seq " + " from mkt_fstp_parmseq " + " where active_code =  ? ";
          Object[] param = new Object[] {
                  wp.itemStr("active_code")
          };
          sqlSelect(strSql, param);

          if (sqlRowNum > 0) {
              errmsg("[" + colStr("active_code") + "]首刷禮活動序號還存在,主檔不可刪除 !");
              return;
          }
          strSql = "select active_seq " + " from mkt_fstp_parmseq_t " + " where active_code =  ? ";
          param = new Object[] {
                  wp.itemStr("active_code")
          };
          sqlSelect(strSql, param);

          if (sqlRowNum > 0) {
              errmsg("[" + colStr("active_code") + "]首刷禮活動序號待覆核還存在,主檔不可刪除 !");
              return;
          }


      }
      if ((this.ibAdd) || (this.ibUpdate)) {
          if (!wp.itemEmpty("issue_date_s") && (!wp.itemEmpty("issue_date_e")))
              if (wp.itemStr("issue_date_s").compareTo(wp.itemStr("issue_date_e")) > 0) {
                  errmsg("發卡期間：["
                          + wp.itemStr("issue_date_s")
                          + "]>["
                          + wp.itemStr("issue_date_e")
                          + "] 起迄值錯誤!");
                  return;
              }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_s1").length() == 0)
              wp.itemSet("purchase_amt_s1", "0");
          if (wp.itemStr("PURCHASE_AMT_E1").length() == 0)
              wp.itemSet("PURCHASE_AMT_E1", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_s1")) > Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_E1"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E1")) != 0)) {
              errmsg("區間門檻一：("
                      + wp.itemStr("purchase_amt_s1")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_E1")
                      + ") 起迄值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_e1").length() == 0)
              wp.itemSet("purchase_amt_e1", "0");
          if (wp.itemStr("PURCHASE_AMT_S2").length() == 0)
              wp.itemSet("PURCHASE_AMT_S2", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_e1")) >= Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_S2"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S2")) != 0)) {
              errmsg("區間2-3:("
                      + wp.itemStr("purchase_amt_e1")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_S2")
                      + ") 迄起值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_s2").length() == 0)
              wp.itemSet("purchase_amt_s2", "0");
          if (wp.itemStr("PURCHASE_AMT_E2").length() == 0)
              wp.itemSet("PURCHASE_AMT_E2", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_s2")) > Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_E2"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E2")) != 0)) {
              errmsg("區間門檻二：("
                      + wp.itemStr("purchase_amt_s2")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_E2")
                      + ") 起迄值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_e2").length() == 0)
              wp.itemSet("purchase_amt_e2", "0");
          if (wp.itemStr("PURCHASE_AMT_S3").length() == 0)
              wp.itemSet("PURCHASE_AMT_S3", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_e2")) >= Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_S3"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S3")) != 0)) {
              errmsg("區間2-3:("
                      + wp.itemStr("purchase_amt_e2")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_S3")
                      + ") 迄起值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_s3").length() == 0)
              wp.itemSet("purchase_amt_s3", "0");
          if (wp.itemStr("PURCHASE_AMT_E3").length() == 0)
              wp.itemSet("PURCHASE_AMT_E3", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_s3")) > Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_E3"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E3")) != 0)) {
              errmsg("區間門檻三：("
                      + wp.itemStr("purchase_amt_s3")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_E3")
                      + ") 起迄值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_e3").length() == 0)
              wp.itemSet("purchase_amt_e3", "0");
          if (wp.itemStr("PURCHASE_AMT_S4").length() == 0)
              wp.itemSet("PURCHASE_AMT_S4", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_e3")) >= Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_S4"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S4")) != 0)) {
              errmsg("區間2-3:("
                      + wp.itemStr("purchase_amt_e3")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_S4")
                      + ") 迄起值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_s4").length() == 0)
              wp.itemSet("purchase_amt_s4", "0");
          if (wp.itemStr("PURCHASE_AMT_E4").length() == 0)
              wp.itemSet("PURCHASE_AMT_E4", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_s4")) > Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_E4"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E4")) != 0)) {
              errmsg("區間門檻四：("
                      + wp.itemStr("purchase_amt_s4")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_E4")
                      + ") 起迄值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_e4").length() == 0)
              wp.itemSet("purchase_amt_e4", "0");
          if (wp.itemStr("PURCHASE_AMT_S5").length() == 0)
              wp.itemSet("PURCHASE_AMT_S5", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_e4")) >= Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_S5"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S5")) != 0)) {
              errmsg("區間2-3:("
                      + wp.itemStr("purchase_amt_e4")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_S5")
                      + ") 迄起值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_s5").length() == 0)
              wp.itemSet("purchase_amt_s5", "0");
          if (wp.itemStr("PURCHASE_AMT_E5").length() == 0)
              wp.itemSet("PURCHASE_AMT_E5", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_s5")) > Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_E5"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E5")) != 0)) {
              errmsg("區間門檻五：("
                      + wp.itemStr("purchase_amt_s5")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_E5")
                      + ") 起迄值錯誤!");
              return;
          }
      }

      if ((this.ibAdd) || (this.ibUpdate)) {
          if (wp.itemStr("purchase_amt_e5").length() == 0)
              wp.itemSet("purchase_amt_e5", "0");
          if (wp.itemStr("PURCHASE_AMT_S5").length() == 0)
              wp.itemSet("PURCHASE_AMT_S5", "0");
          if (Double.parseDouble(wp.itemStr("purchase_amt_e5")) >= Double
                  .parseDouble(wp.itemStr("PURCHASE_AMT_S5"))
                  && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S5")) != 0)) {
              errmsg("區間2-3:("
                      + wp.itemStr("purchase_amt_e5")
                      + ")~("
                      + wp.itemStr("PURCHASE_AMT_S5")
                      + ") 迄起值錯誤!");
              return;
          }
      }

      int checkInt = checkDecnum(wp.itemStr("mcht_in_amt"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg("累計最低消費金額: 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg("累計最低消費金額: 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg("累計最低消費金額: 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("per_amt"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("perday_cnt"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("sum_amt"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("sum_cnt"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_s1"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg("門檻一： 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg("門檻一： 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg("門檻一： 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_e1"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("feedback_amt_1"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_s2"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg("門檻二： 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg("門檻二： 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg("門檻二： 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_e2"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("feedback_amt_2"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_s3"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg("門檻三： 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg("門檻三： 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg("門檻三： 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_e3"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("feedback_amt_3"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_s4"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg("門檻四： 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg("門檻四： 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg("門檻四： 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_e4"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("feedback_amt_4"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_s5"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg("門檻五： 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg("門檻五： 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg("門檻五： 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("purchase_amt_e5"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      checkInt = checkDecnum(wp.itemStr("feedback_amt_5"), 11, 3);
      if (checkInt != 0) {
          if (checkInt == 1)
              errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
          if (checkInt == 2)
              errmsg(" 格式超出範圍 : 不可有小數位");
          if (checkInt == 3)
              errmsg(" 非數值");
          return;
      }

      if ((this.ibAdd) || (this.ibUpdate))
          if (wp.itemEmpty("apr_flag")) {
              errmsg("覆核狀態: 不可空白");
              return;
          }

      if (this.isAdd())
          return;

      if (this.ibDelete) {
          wp.colSet("storetype", "N");
      }
  }

  // ************************************************************************
  int listParmDataCnt(String s1, String s2, String s3, String s4) {
      String is_sql = "select count(*) as data_cnt "
              + "from  "
              + s1
              + " "
              + " where table_name = ? "
              + " and   data_key   = ? "
              + " and   data_type  = ? ";
      Object[] param = new Object[] {
              s2, s3, s4
      };
      sqlSelect(is_sql, param);

      return (Integer.parseInt(colStr("data_cnt")));
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

    dbInsertD4T();
    dbInsertI4T();
    dbInsertD2T();
    dbInsertI2T();

    strSql = " insert into  "
            + controlTabName
            + " ("
            + " active_code, "
            + " aud_type, "
            + " active_name, "
            + " stop_flag, "
            + " stop_date, "
            + " stop_desc, "
            + " issue_date_s, "
            + " issue_date_e, "
            + " effect_months, "
            + " purchase_days, "
            + " n1_days, "
            // + " achieve_cond, "
            + " new_hldr_cond, "
            + " new_hldr_sel, "
            + " new_hldr_days, "
            + " new_group_cond, "
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
//            + " mcht_in_flag, "
            + " mcht_in_cond, "
            + " mcht_in_amt, "
            + " in_merchant_sel, "
            + " in_mcht_group_sel, "
            + " pos_entry_sel, "
            + " ucaf_sel, "
            + " eci_sel, "
            + " bl_cond, "
            + " ca_cond, "
            + " it_cond, "
            // + " it_flag, "
            + " id_cond, "
            + " ao_cond, "
            + " ot_cond, "
            + " linebc_cond, "
            + " banklite_cond, "
            + " selfdeduct_cond, "
            + " anulfee_cond, "
            + " anulfee_days, "
            // + " action_pay_cond, "
            // + " action_pay_times, "
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
            + " sms_addvalue_cond, "
            + " sms_addvalue_days, "
            + " addvalue_msg_id_g, "
            + " addvalue_msg_id_c, "
            + " sms_send_cond, "
            + " send_msg_id, "
            + " sms_send_days, "
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
            // + " perday_cnt_cond, "
            // + " perday_cnt, "
            + " sum_amt_cond, "
            + " sum_amt, "
            + " sum_cnt_cond, "
            + " sum_cnt, "
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
            + " add_value_cond, "
            + " add_value, "
            + " mkt_fstp_gift_cond, "
            + " crt_date, "
            + " crt_user, "
            + " mod_seqno, "
            + " mod_time,mod_user,mod_pgm,"
            + " nopurc_msg_pgm,"
            + " half_msg_pgm, "
            + " c_record_cond, "
            + " c_record_group_no,"
            + " new_hldr_flag,"
            + " mcht_seq_flag,"
            + " nopurc_g_cond,"
            + " half_g_cond,"
            + " send_msg_pgm,"
            + " achieve_cond,"
            + "it_flag,"
            + "action_pay_cond,"
            + "action_pay_times,"
            + "perday_cnt_cond,"
            + "perday_cnt"
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "sysdate,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,?,?,?)";

    Object[] param = new Object[] {
            activeCode,
            wp.itemStr("aud_type"),
            wp.itemStr("active_name"),
            wp.itemStr("stop_flag"),
            wp.itemStr("stop_date"),
            wp.itemStr("stop_desc"),
            wp.itemStr("issue_date_s"),
            wp.itemStr("issue_date_e"),
            wp.itemNum("effect_months"),
            wp.itemNum("purchase_days"),
            wp.itemNum("n1_days"),
            // wp.itemStr("achieve_cond"),
            wp.itemStr("new_hldr_cond"),
            wp.itemStr("new_hldr_sel"),
            wp.itemNum("new_hldr_days"),
            wp.itemStr("new_group_cond"),
            wp.itemStr("acct_type_sel"),
            wp.itemStr("group_code_sel"),
            wp.itemStr("source_code_sel"),
            wp.itemStr("card_type_sel"),
            wp.itemStr("promote_dept_sel"),
            wp.itemStr("list_cond"),
            wp.itemStr("list_flag"),
            wp.itemStr("list_use_sel"),
            wp.itemStr("mcc_code_sel"),
            wp.itemStr("merchant_sel"),
            wp.itemStr("mcht_group_sel"),
//            wp.itemStr("mcht_in_flag"),
            wp.itemStr("mcht_in_cond"),
            wp.itemNum("mcht_in_amt"),
            wp.itemStr("in_merchant_sel"),
            wp.itemStr("in_mcht_group_sel"),
            wp.itemStr("pos_entry_sel"),
            wp.itemStr("ucaf_sel"),
            wp.itemStr("eci_sel"),
            wp.itemStr("bl_cond"),
            wp.itemStr("ca_cond"),
            wp.itemStr("it_cond"),
            // wp.itemStr("it_flag"),
            wp.itemStr("id_cond"),
            wp.itemStr("ao_cond"),
            wp.itemStr("ot_cond"),
            wp.itemStr("linebc_cond"),
            wp.itemStr("banklite_cond"),
            wp.itemStr("selfdeduct_cond"),
            wp.itemStr("anulfee_cond"),
            wp.itemNum("anulfee_days"),
            // wp.itemStr("action_pay_cond"),
            // wp.itemNum("action_pay_times"),
            wp.itemStr("sms_nopurc_cond"),
            wp.itemNum("sms_nopurc_days"),
            wp.itemStr("nopurc_msg_id_g"),
            wp.itemStr("nopurc_msg_id_c"),
            wp.itemStr("sms_half_cond"),
            wp.itemNum("sms_half_days"),
            wp.itemStr("half_cnt_cond"),
            wp.itemNum("half_cnt"),
            wp.itemStr("half_andor_cond"),
            wp.itemStr("half_amt_cond"),
            wp.itemNum("half_amt"),
            wp.itemStr("half_msg_id_g"),
            wp.itemStr("half_msg_id_c"),
            wp.itemStr("sms_addvalue_cond"),
            wp.itemNum("sms_addvalue_days"),
            wp.itemStr("addvalue_msg_id_g"),
            wp.itemStr("addvalue_msg_id_c"),
            wp.itemStr("sms_send_cond"),
            wp.itemStr("send_msg_id"),
            wp.itemNum("sms_send_days"),
            wp.itemStr("multi_fb_type"),
            wp.itemStr("record_cond"),
            wp.itemStr("record_group_no"),
            wp.itemStr("active_type"),
            wp.itemStr("bonus_type"),
            wp.itemStr("tax_flag"),
            wp.itemStr("fund_code"),
            wp.itemStr("group_type"),
            wp.itemStr("prog_code"),
            wp.itemStr("prog_s_date"),
            wp.itemStr("prog_e_date"),
            wp.itemStr("gift_no"),
            wp.itemStr("spec_gift_no"),
            wp.itemStr("per_amt_cond"),
            wp.itemNum("per_amt"),
            // wp.itemStr("perday_cnt_cond"),
            // wp.itemNum("perday_cnt"),
            wp.itemStr("sum_amt_cond"),
            wp.itemNum("sum_amt"),
            wp.itemStr("sum_cnt_cond"),
            wp.itemNum("sum_cnt"),
            wp.itemStr("threshold_sel"),
            wp.itemStr("purchase_type_sel"),
            wp.itemNum("purchase_amt_s1"),
            wp.itemNum("purchase_amt_e1"),
            wp.itemNum("feedback_amt_1"),
            wp.itemNum("purchase_amt_s2"),
            wp.itemNum("purchase_amt_e2"),
            wp.itemNum("feedback_amt_2"),
            wp.itemNum("purchase_amt_s3"),
            wp.itemNum("purchase_amt_e3"),
            wp.itemNum("feedback_amt_3"),
            wp.itemNum("purchase_amt_s4"),
            wp.itemNum("purchase_amt_e4"),
            wp.itemNum("feedback_amt_4"),
            wp.itemNum("purchase_amt_s5"),
            wp.itemNum("purchase_amt_e5"),
            wp.itemNum("feedback_amt_5"),
            wp.itemNum("feedback_limit"),
            wp.itemStr("add_value_cond"),
            wp.itemStr("add_value"),
            wp.itemStr("mkt_fstp_gift_cond"),
            wp.loginUser,
            wp.modSeqno(),
            wp.loginUser,
            wp.modPgm(),
            wp.itemStr("nopurc_msg_pgm"),
            wp.itemStr("half_msg_pgm"),
            wp.itemStr("c_record_cond"),
            wp.itemStr("c_record_group_no"),
            wp.itemStr("new_hldr_flag"),
            wp.itemStr("mcht_seq_flag"),
            wp.itemStr("nopurc_g_cond"),
            wp.itemStr("half_g_cond"),
            wp.itemStr("send_msg_pgm"),
            wp.itemStr("achieve_cond"),
            wp.itemStr("it_flag"),
            wp.itemStr("action_pay_cond"),
            wp.itemNum("action_pay_times"),
            wp.itemStr("perday_cnt_cond"),
            wp.itemNum("perday_cnt")
    };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }
 
  // ************************************************************************
  public int dbInsertI4T() {
    msgOK();

    strSql = "insert into mkt_imfstp_list_T " + "select * " + "from mkt_imfstp_list "
        + "where active_code = ? " + " and active_seq = '00' ";

    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);


    return 1;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();



    strSql = "insert into MKT_BN_DATA_T " + "select * " + "from MKT_BN_DATA "
        + "where table_name  =  'MKT_FSTP_PARM' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);


    return 1;
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

  strSql = "update "
          + controlTabName
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
          + "new_hldr_sel = ?,"
          + "new_hldr_days = ?, "
          + "new_group_cond = ?, "
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
//          + "mcht_in_flag = ?, "
          + "mcht_in_cond = ?, "
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
          + "selfdeduct_cond = ?, "
          + "anulfee_cond = ?, "
          + "anulfee_days = ?, "
          + "action_pay_cond = ?, "
          + "action_pay_times = ?, "
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
          + "sms_addvalue_cond = ?, "
          + "sms_addvalue_days = ?, "
          + "addvalue_msg_id_g = ?, "
          + "addvalue_msg_id_c = ?, "
          + "sms_send_cond = ?, "
          + "send_msg_id = ?, "
          + "sms_send_days = ?, "
          + "multi_fb_type = ?, "
          + "record_cond = ?, "
          + "record_group_no = ?, "
          + "active_type = ?, "
          + "bonus_type = ?, "
          + "tax_flag = ?, "
          + "fund_code = ?, "
          + "group_type = ?, "
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
          + "add_value_cond = ?, "
          + "add_value = ?, "
          + "mkt_fstp_gift_cond = ?, "
          + "crt_user  = ?, "
          + "crt_date  = to_char(sysdate,'yyyymmdd'), "
          + "mod_user  = ?, "
          + "mod_seqno = nvl(mod_seqno,0)+1, "
          + "mod_time  = sysdate, "
          + "mod_pgm   = ?, "
          + "nopurc_msg_pgm = ?, "
          + "half_msg_pgm = ?, "
          + "c_record_cond = ?, "
          + "c_record_group_no = ?, "
          + "new_hldr_flag = ?,"
          + "mcht_seq_flag = ?, "
          + "nopurc_g_cond = ?,"
          + "half_g_cond = ?,"
          + "send_msg_pgm = ? "
          + "where rowid = ? "
          + "and   mod_seqno = ? ";

  Object[] param = new Object[] {
          wp.itemStr("active_name"),
          wp.itemStr("stop_flag"),
          wp.itemStr("stop_date"),
          wp.itemStr("stop_desc"),
          wp.itemStr("issue_date_s"),
          wp.itemStr("issue_date_e"),
          wp.itemNum("effect_months"),
          wp.itemNum("purchase_days"),
          wp.itemNum("n1_days"),
          wp.itemStr("achieve_cond"),
          wp.itemStr("new_hldr_cond"),
          wp.itemStr("new_hldr_sel"),
          wp.itemNum("new_hldr_days"),
          wp.itemStr("new_group_cond"),
          wp.itemStr("acct_type_sel"),
          wp.itemStr("group_code_sel"),
          wp.itemStr("source_code_sel"),
          wp.itemStr("card_type_sel"),
          wp.itemStr("promote_dept_sel"),
          wp.itemStr("list_cond"),
          wp.itemStr("list_flag"),
          wp.itemStr("list_use_sel"),
          wp.itemStr("mcc_code_sel"),
          wp.itemStr("merchant_sel"),
          wp.itemStr("mcht_group_sel"),
//          wp.itemStr("mcht_in_flag"),
          wp.itemStr("mcht_in_cond"), 
          wp.itemNum("mcht_in_amt"),
          wp.itemStr("in_merchant_sel"),
          wp.itemStr("in_mcht_group_sel"),
          wp.itemStr("pos_entry_sel"),
          wp.itemStr("ucaf_sel"),
          wp.itemStr("eci_sel"),
          wp.itemStr("bl_cond"),
          wp.itemStr("ca_cond"),
          wp.itemStr("it_cond"),
          wp.itemStr("it_flag"),
          wp.itemStr("id_cond"),
          wp.itemStr("ao_cond"),
          wp.itemStr("ot_cond"),
          wp.itemStr("linebc_cond"),
          wp.itemStr("banklite_cond"),
          wp.itemStr("selfdeduct_cond"),
          wp.itemStr("anulfee_cond"),
          wp.itemNum("anulfee_days"),
          wp.itemStr("action_pay_cond"),
          wp.itemNum("action_pay_times"),
          wp.itemStr("sms_nopurc_cond"),
          wp.itemNum("sms_nopurc_days"),
          wp.itemStr("nopurc_msg_id_g"),
          wp.itemStr("nopurc_msg_id_c"),
          wp.itemStr("sms_half_cond"),
          wp.itemNum("sms_half_days"),
          wp.itemStr("half_cnt_cond"),
          wp.itemNum("half_cnt"),
          wp.itemStr("half_andor_cond"),
          wp.itemStr("half_amt_cond"),
          wp.itemNum("half_amt"),
          wp.itemStr("half_msg_id_g"),
          wp.itemStr("half_msg_id_c"),
          wp.itemStr("sms_addvalue_cond"),
          wp.itemNum("sms_addvalue_days"),
          wp.itemStr("addvalue_msg_id_g"),
          wp.itemStr("addvalue_msg_id_c"),
          wp.itemStr("sms_send_cond"),
          wp.itemStr("send_msg_id"),
          wp.itemNum("sms_send_days"),
          wp.itemStr("multi_fb_type"),
          wp.itemStr("record_cond"),
          wp.itemStr("record_group_no"),
          wp.itemStr("active_type"),
          wp.itemStr("bonus_type"),
          wp.itemStr("tax_flag"),
          wp.itemStr("fund_code"),
          wp.itemStr("group_type"),
          wp.itemStr("gift_no"),
          wp.itemStr("spec_gift_no"),
          wp.itemStr("per_amt_cond"),
          wp.itemNum("per_amt"),
          wp.itemStr("perday_cnt_cond"),
          wp.itemNum("perday_cnt"),
          wp.itemStr("sum_amt_cond"),
          wp.itemNum("sum_amt"),
          wp.itemStr("sum_cnt_cond"),
          wp.itemNum("sum_cnt"),
          wp.itemStr("threshold_sel"),
          wp.itemStr("purchase_type_sel"),
          wp.itemNum("purchase_amt_s1"),
          wp.itemNum("purchase_amt_e1"),
          wp.itemNum("feedback_amt_1"),
          wp.itemNum("purchase_amt_s2"),
          wp.itemNum("purchase_amt_e2"),
          wp.itemNum("feedback_amt_2"),
          wp.itemNum("purchase_amt_s3"),
          wp.itemNum("purchase_amt_e3"),
          wp.itemNum("feedback_amt_3"),
          wp.itemNum("purchase_amt_s4"),
          wp.itemNum("purchase_amt_e4"),
          wp.itemNum("feedback_amt_4"),
          wp.itemNum("purchase_amt_s5"),
          wp.itemNum("purchase_amt_e5"),
          wp.itemNum("feedback_amt_5"),
          wp.itemNum("feedback_limit"),
          wp.itemStr("add_value_cond"),
          wp.itemStr("add_value"),
          wp.itemStr("mkt_fstp_gift_cond"),
          wp.loginUser,
          wp.loginUser,
          wp.itemStr("mod_pgm"),
          wp.itemStr("nopurc_msg_pgm"),
          wp.itemStr("half_msg_pgm"),
          wp.itemStr("c_record_cond"),
          wp.itemStr("c_record_group_no"),
          wp.itemStr("new_hldr_flag"),
          wp.itemStr("mcht_seq_flag"),
          wp.itemStr("nopurc_g_cond"),
          wp.itemStr("half_g_cond"),
          wp.itemStr("send_msg_pgm"),
          wp.itemRowId("rowid"),
          wp.itemNum("mod_seqno")
  };

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

    dbInsertD4T();
    dbInsertD2T();

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
  public int dbInsertD4T() {
    msgOK();

    strSql = "delete mkt_imfstp_list_T " + "WHERE active_code = ? " + " and active_seq = '00' ";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 mkt_imfstp_list_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertD2T() {
    msgOK();

    strSql = "delete MKT_BN_DATA_T " + " where table_name  =  'MKT_FSTP_PARM' "
        + "and   data_key = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_BN_DATA_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    if (wp.respHtml.equals("mktm6240_fgcd")) {
      String dataCode = varsStr("data_code");
      int rows = checkGift(dataCode);
      if (rows > 0)
        return 99;
    }

    String dataType = "";
    if (wp.respHtml.equals("mktm6240_gncd"))
      dataType = "G";
    if (wp.respHtml.equals("mktm6240_fgcd"))
      dataType = "F";
    if (wp.respHtml.equals("mktm6240_actp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6240_srcd"))
      dataType = "3";
    if (wp.respHtml.equals("mktm6240_cdtp"))
      dataType = "4";
    if (wp.respHtml.equals("mktm6240_pmdp"))
      dataType = "5";
    if (wp.respHtml.equals("mktm6240_mccd"))
      dataType = "6";
    if (wp.respHtml.equals("mktm6240_aaa1"))
      dataType = "8";
    if (wp.respHtml.equals("mktm6240_aaat"))
      dataType = "10";
    if (wp.respHtml.equals("mktm6240_ucaf"))
      dataType = "12";
    if (wp.respHtml.equals("mktm6240_deci"))
      dataType = "13";
    if (wp.respHtml.equals("mktm6240_apay"))
      dataType = "H";
    if (wp.respHtml.equals("mktm6240_smsa"))
      dataType = "A";
    if (wp.respHtml.equals("mktm6240_smsc"))
      dataType = "C";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_FSTP_PARM', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6240_gncd"))
      dataType = "G";
    if (wp.respHtml.equals("mktm6240_fgcd"))
      dataType = "F";
    if (wp.respHtml.equals("mktm6240_actp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm6240_srcd"))
      dataType = "3";
    if (wp.respHtml.equals("mktm6240_cdtp"))
      dataType = "4";
    if (wp.respHtml.equals("mktm6240_pmdp"))
      dataType = "5";
    if (wp.respHtml.equals("mktm6240_mccd"))
      dataType = "6";
    if (wp.respHtml.equals("mktm6240_aaa1"))
      dataType = "8";
    if (wp.respHtml.equals("mktm6240_aaat"))
      dataType = "10";
    if (wp.respHtml.equals("mktm6240_ucaf"))
      dataType = "12";
    if (wp.respHtml.equals("mktm6240_deci"))
      dataType = "13";
    if (wp.respHtml.equals("mktm6240_apay"))
      dataType = "H";
    if (wp.respHtml.equals("mktm6240_smsa"))
      dataType = "A";
    if (wp.respHtml.equals("mktm6240_smsc"))
      dataType = "C";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_FSTP_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_FSTP_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI4() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6240_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6240_posn"))
      dataType = "11";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "data_code3," + "crt_date, " + "crt_user, " + " mod_time, "
        + " mod_user, " + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_FSTP_PARM', " + "?, "
        + "?,?,?,?," + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? "
        + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        varsStr("data_code2"), varsStr("data_code3"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6240_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm6240_posn"))
      dataType = "11";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_FSTP_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_FSTP_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6240_mrcd"))
      dataType = "7";
    if (wp.respHtml.equals("mktm6240_inmc"))
      dataType = "9";
    if (wp.respHtml.equals("mktm6240_smsb"))
      dataType = "B";
    if (wp.respHtml.equals("mktm6240_smsd"))
      dataType = "D";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_FSTP_PARM', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_BN_DATA_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD3() {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm6240_mrcd"))
      dataType = "7";
    if (wp.respHtml.equals("mktm6240_inmc"))
      dataType = "9";
    if (wp.respHtml.equals("mktm6240_smsb"))
      dataType = "B";
    if (wp.respHtml.equals("mktm6240_smsd"))
      dataType = "D";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_FSTP_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_FSTP_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2List(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2List(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where active_code = ? ";

    Object[] param = new Object[] {wp.itemStr("active_code")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa1(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_FSTP_PARM", wp.itemStr("active_code"), "7"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2Aaa3(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.loginUser;
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa3(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_FSTP_PARM", wp.itemStr("active_code"), "9"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg) {
    dateTime();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    comr.setConn(wp);

    if (!comm.isNumber(errMsg[10]))
      errMsg[10] = "0";
    if (!comm.isNumber(errMsg[1]))
      errMsg[1] = "0";
    if (!comm.isNumber(errMsg[2]))
      errMsg[2] = "0";

    strSql = " insert into ecs_media_errlog (" + " crt_date, " + " crt_time, " + " file_name, "
        + " unit_code, " + " main_desc, " + " error_seq, " + " error_desc, " + " line_seq, "
        + " column_seq, " + " column_data, " + " trans_seqno, " + " column_desc, "
        + " program_code, " + " mod_time, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?," // 10
                                                                                                   // record
        + "?,?,?," // 4 trvotfd
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, wp.itemStr("zz_file_name"),
        comr.getObjectOwner("3", wp.modPgm()), errMsg[0], Integer.valueOf(errMsg[1]), errMsg[4],
        Integer.valueOf(errMsg[10]), Integer.valueOf(errMsg[2]), errMsg[3], tranSeqStr, errMsg[5],
        wp.modPgm(), wp.sysDate + wp.sysTime, wp.modPgm()};

    wp.logSql = false;
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_media_errlog 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt) {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    dateTime();
    strSql = " insert into ecs_notify_log (" + " crt_date, " + " crt_time, " + " unit_code, "
        + " obj_type, " + " notify_head, " + " notify_name, " + " notify_desc1, "
        + " notify_desc2, " + " trans_seqno, " + " mod_time, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?," // 9 record
        + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param = new Object[] {wp.sysDate, wp.sysTime, comr.getObjectOwner("3", wp.modPgm()),
        "3", "媒體檔轉入資料有誤(只記錄前100筆)", "媒體檔名:" + wp.itemStr("zz_file_name"),
        "程式 " + wp.modPgm() + " 轉 " + wp.itemStr("zz_file_name") + " 有" + errorCnt + " 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤", tranSeqStr, wp.sysDate + wp.sysTime, wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_modify_log 錯誤");
    return rc;
  }

  // *************************************************************************
  public int checkGift(String dataCode) {
    strSql = " select count(*) as qua " + " from mkt_fstp_gift " + " where id = ? ";

    Object[] param = new Object[] {dataCode};

    sqlSelect(strSql, param);

    int rows = Integer.parseInt(colStr("qua"));
    return rows;
  }
  // ************************************************************************

} // End of class
