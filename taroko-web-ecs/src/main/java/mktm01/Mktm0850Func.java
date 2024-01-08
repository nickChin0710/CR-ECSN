/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                          *
* 110-08-30  V1.00.04  Wendy Lu      修改程式                               *
* 111-07-21  V1.00.05  Machao       Bug處理                                *
* 112-01-06  V1.00.06  Zuwei Su       增[交易平台種類], 增[特店名稱]          *
* 112-01-07  V1.00.07  Zuwei Su       [交易平台種類]取值改為Y和空; 調整 listImchannelDataCnt()變數 *
* 112-01-17  V1.00.08  Zuwei Su       非新增刪除異常，[交易平台種類]取值改為[全部,指定,排除] *
* 112-02-16  V1.00.09  Zuwei Su        刪除[交易平台種類]選項，增加 [一般消費群組]選項
* 112-02-20  V1.00.10  Zuwei Su        insert mkt_channel_parm_t 參數個數不匹配
* 112-03-16  V1.00.11  Machao         增加 [通路類別]選項
* 112-05-16  V1.00.12  Ryan           增一般名單產檔格式、回饋周期 的參數設定
* 112-10-13  V1.00.13  Zuwei Su       增[消費累計基礎],增[當期帳單(年月)]  *
* 112-10-17  V1.00.14  Zuwei Su       [消費累計基礎]選消費期間需清空[當期帳單(年月)]  *
* 112-11-03  V1.00.15  Zuwei Su       當期帳單(年月)取營業日或分析日期上一個月  *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0850Func extends FuncEdit {
  private String PROGNAME = "行銷通路活動回饋參數檔維護處理程式112/05/16 V1.00.12";
  CommDate commDate = new CommDate();
  String kk1;
  String orgControlTabName = "mkt_channel_parm";
  String controlTabName = "mkt_channel_parm_t";

  public Mktm0850Func(TarokoCommon wr) {
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
    if (procTabName.length()==0) return(1);
    strSql =  " select "
            + " apr_flag, "
            + " active_name, "
            + " stop_flag, "
            + " stop_date, "
            + " feedback_apr_date, "
            + " feedback_date, "
            + " bonus_type_cond, "
            + " bonus_type, "
            + " tax_flag, "
            + " b_effect_months, "
//            + " bonus_date, "
            + " fund_code_cond, "
            + " fund_code, "
//            + " fund_date, "
            + " f_effect_months, "
            + " other_type_cond, "
            + " spec_gift_no, "
            + " gift_date, "
            + " send_msg_pgm, "
            + " lottery_cond, "
            + " lottery_type, "
//            + " lottery_date, "
            + " prog_msg_pgm, "
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
            + " per_amt_cond, "
            + " per_amt, "
            + " perday_cnt_cond, "
            + " perday_cnt, "
            + " sum_amt_cond, "
            + " sum_amt, "
            + " sum_cnt_cond, "
            + " sum_cnt, "
            + " above_cond, "
            + " above_amt, "
            + " above_cnt, "
            + " max_cnt_cond, "
            + " max_cnt, "
            + " purchase_type_sel, "
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
            + " b_feedback_limit, "
            + " f_feedback_limit, "
            + " s_feedback_limit, "
            + " l_feedback_limit, "
            + " b_feedback_cnt_limit, "
            + " f_feedback_cnt_limit, "
            + " s_feedback_cnt_limit, "
            + " l_feedback_cnt_limit, "
            + " prog_code, "
            + " prog_code, "
            + " apr_date, "
            + " apr_user, "
            + " crt_date, "
            + " crt_user, "
            + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno, "
            + " feedback_cycle, "
            + " feedback_dd, "
            + " outfile_type "
            + " from " + procTabName 
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
	  
	  if (!this.ibDelete)
	     {
	      if (wp.colStr("storetype").equals("Y"))
	        {
	         errmsg("[查原資料]模式中, 請按[還原異動] 才可儲存 !");
	         return;
	        }
	     }  
    if (this.ibAdd) {
      kk1 = wp.itemStr("active_code");
      if (empty(kk1)) {
        errmsg("活動代碼 不可空白");
        return;
      }
    } else {
      kk1 = wp.itemStr("active_code");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (kk1.length() > 0) {
          strSql =
              "select count(*) as qua " + "from " + orgControlTabName + " where active_code = ? ";
          Object[] param = new Object[] {kk1};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[活動代碼] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }
    if (this.ibUpdate)
    {
     if ((wp.itemStr("acct_type_sel").equals("1"))||
         (wp.itemStr("acct_type_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"1")==0)
            {
             errmsg("[帳戶類別] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
     if ((wp.itemStr("group_code_sel").equals("1"))||
         (wp.itemStr("group_code_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"2")==0)
            {
             errmsg("[團體代號] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
     if ((wp.itemStr("mcc_code_sel").equals("1"))||
         (wp.itemStr("mcc_code_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"5")==0)
            {
             errmsg("[特店類別] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
     if ((wp.itemStr("merchant_sel").equals("1"))||
         (wp.itemStr("merchant_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"3")==0)
            {
             errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
     if ((wp.itemStr("mcht_group_sel").equals("1"))||
         (wp.itemStr("mcht_group_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"6")==0)
            {
             errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
      if ((wp.itemStr("mcht_cname_sel").equals("1"))||
          (wp.itemStr("mcht_cname_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_cdata_t"
                                ,"MKT_CHANNEL_PARM"
                                ,wp.colStr("active_code")
                                ,"A")==0)
             {
              errmsg("[特店中文名稱] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr("mcht_ename_sel").equals("1"))||
          (wp.itemStr("mcht_ename_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_cdata_t"
                                ,"MKT_CHANNEL_PARM"
                                ,wp.colStr("active_code")
                                ,"B")==0)
             {
              errmsg("[特店英文名稱] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
     if ((wp.itemStr("it_term_sel").equals("1"))||
         (wp.itemStr("it_term_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"4")==0)
            {
             errmsg("[分期期數] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
     if ((wp.itemStr("terminal_id_sel").equals("1"))||
         (wp.itemStr("terminal_id_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"7")==0)
            {
             errmsg("[終端代碼] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
     if ((wp.itemStr("pos_entry_sel").equals("1"))||
         (wp.itemStr("pos_entry_sel").equals("2")))
        {
         if (listParmDataCnt("mkt_bn_data_t"
                               ,"MKT_CHANNEL_PARM"
                               ,wp.colStr("active_code")
                               ,"8")==0)
            {
             errmsg("[POS ENTRY] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
//     if ((wp.itemStr("platform_kind_sel").equals("1"))
//             || (wp.itemStr("platform_kind_sel").equals("2"))) {
//        if (listParmDataCnt("mkt_bn_data_t", "MKT_CHANNEL_PARM", wp.colStr("active_code"),
//                "9") == 0) {
//            errmsg("[交易平台種類] 明細沒有設定, 筆數不可為 0  !");
//            return;
//        }
//    }
     if ((wp.itemStr("platform_group_sel").equals("1"))
             || (wp.itemStr("platform_group_sel").equals("2"))) {
        if (listParmDataCnt("mkt_bn_data_t", "MKT_CHANNEL_PARM", wp.colStr("active_code"),
                "10") == 0) {
            errmsg("[一般消費群組] 明細沒有設定, 筆數不可為 0  !");
            return;
        }
    }
   }
    
    if (!wp.itemStr("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
    if (!wp.itemStr("bonus_type_cond").equals("Y")) wp.itemSet("bonus_type_cond","N");
    if (!wp.itemStr("fund_code_cond").equals("Y")) wp.itemSet("fund_code_cond","N");
    if (!wp.itemStr("other_type_cond").equals("Y")) wp.itemSet("other_type_cond","N");
    if (!wp.itemStr("lottery_cond").equals("Y")) wp.itemSet("lottery_cond","N");
    if (!wp.itemStr("list_cond").equals("Y")) wp.itemSet("list_cond","N");
    if (!wp.itemStr("bl_cond").equals("Y")) wp.itemSet("bl_cond","N");
    if (!wp.itemStr("ca_cond").equals("Y")) wp.itemSet("ca_cond","N");
    if (!wp.itemStr("it_cond").equals("Y")) wp.itemSet("it_cond","N");
    if (!wp.itemStr("id_cond").equals("Y")) wp.itemSet("id_cond","N");
    if (!wp.itemStr("ao_cond").equals("Y")) wp.itemSet("ao_cond","N");
    if (!wp.itemStr("ot_cond").equals("Y")) wp.itemSet("ot_cond","N");
    if (!wp.itemStr("minus_txn_cond").equals("Y")) wp.itemSet("minus_txn_cond","N");
    if (!wp.itemStr("block_cond").equals("Y")) wp.itemSet("block_cond","N");
    if (!wp.itemStr("oppost_cond").equals("Y")) wp.itemSet("oppost_cond","N");
    if (!wp.itemStr("payment_rate_cond").equals("Y")) wp.itemSet("payment_rate_cond","N");
    if (!wp.itemStr("record_cond").equals("Y")) wp.itemSet("record_cond","N");
    if (!wp.itemStr("per_amt_cond").equals("Y")) wp.itemSet("per_amt_cond","N");
    if (!wp.itemStr("perday_cnt_cond").equals("Y")) wp.itemSet("perday_cnt_cond","N");
    if (!wp.itemStr("sum_amt_cond").equals("Y")) wp.itemSet("sum_amt_cond","N");
    if (!wp.itemStr("sum_cnt_cond").equals("Y")) wp.itemSet("sum_cnt_cond","N");
    if (!wp.itemStr("above_cond").equals("Y")) wp.itemSet("above_cond","N");
    if (!wp.itemStr("max_cnt_cond").equals("Y")) wp.itemSet("max_cnt_cond","N");
    
    if (this.ibUpdate)
    {
     if (wp.itemStr("list_cond").equals("Y"))
        {
         if (wp.colNum("list_flag_cnt")==0)
         if (listImchannelDataCnt("mkt_imchannel_list_t"
                               ,""
                               ,wp.colStr("active_code")
                               ,"")==0)
            {
             errmsg("[名單類別] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
    }

 if (wp.itemStr("aud_type").equals("A"))
    {
     if (wp.itemStr("apr_flag").equals("Y"))
        {
         wp.colSet("apr_flag" , "N");
         wp.itemSet("apr_flag" , "N");
        }
    }
 else
    {
     if (wp.itemStr("apr_flag").equals("Y"))
        {
         wp.colSet("apr_flag" , "N");
         wp.itemSet("apr_flag" , "N");
        }
    }

 if ((this.ibDelete)||
     (wp.itemStr("aud_type").equals("D"))) return;

if ((((this.ibAdd)||(this.ibUpdate))&&
    (!wp.itemStr("aud_type").equals("D"))))
   {
     if (wp.itemStr("above_cond").equals("Y"))
        {
         if ((wp.itemStr("purchase_type_sel").equals("2"))||
             (wp.itemStr("purchase_type_sel").equals("4")))
            {
             errmsg("設定[每滿x元,回饋y次] 門檻項目只可選擇金額類(1,3,5) !");
             return;
            }
        }
    if (wp.itemStr("feedback_date").length()!=0)
       {
        errmsg("已回饋資料, 不可再異動 !");
        return;
       }
    if ((wp.itemStr("stop_flag").equals("Y"))&&
        (wp.itemStr("stop_date").length()==0))
       {
        errmsg("[活動取消:截止日期]必須輸入 !");
        return;
       }
    if ((wp.itemStr("bonus_type_cond").equals("Y"))&&
        (wp.itemStr("bonus_type").length()==0))
       {
        errmsg("[紅利類別:紅利類別]必須輸入 !");
        return;
       }
    if ((wp.itemStr("fund_code_cond").equals("Y"))&&
        (wp.itemStr("fund_code").length()==0))
       {
        errmsg("[現金回饋代碼:現金回饋代碼]必須輸入 !");
        return;
       }
    if ((wp.itemStr("other_type_cond").equals("Y"))&&
        (wp.itemStr("spec_gift_no").length()==0))
       {
        errmsg("[贈品:贈品代碼]必須輸入 !");
        return;
       }
    if (wp.itemStr("accumulate_term_sel").equals("1")) {
        wp.itemSet("acct_month", "");
        if (wp.itemStr("purchase_date_s").length()==0)
           {
            errmsg("[消費期間:消費期間起日]必須輸入 !");
            return;
           }
        if (wp.itemStr("purchase_date_e").length()==0)
           {
            errmsg("[消費期間:消費期間迄日]必須輸入 !");
            return;
           }
    }
    if (wp.itemStr("accumulate_term_sel").equals("2")) {
        wp.itemSet("purchase_date_s", "");
        wp.itemSet("purchase_date_e", "");
        String acctMonth = "";
//        if (wp.itemEq("feedback_cycle", "M")) {
//            acctMonth = wp.itemStr("cal_def_date_m").substring(0,6);
//        } else {
//            acctMonth = wp.itemStr("cal_def_date").substring(0,6);
//        }
//        if (acctMonth.trim().length() == 0) {
//            acctMonth = wp.itemStr("busi_date").substring(0,6);
//        }
//        acctMonth = commDate.monthAdd(acctMonth, -1);
        wp.itemSet("acct_month", acctMonth);
    }
    if ((wp.itemStr("list_cond").equals("Y"))&&
        (wp.itemStr("list_flag").length()==0))
       {
        errmsg("[名單類別:名單類別選項]必須輸入 !");
        return;
       }
    if (!wp.itemStr("record_cond").equals("Y"))
       {
        if ((!wp.itemStr("bl_cond").equals("Y"))&&
            (!wp.itemStr("ot_cond").equals("Y"))&&
            (!wp.itemStr("it_cond").equals("Y"))&&
            (!wp.itemStr("ca_cond").equals("Y"))&&
            (!wp.itemStr("id_cond").equals("Y"))&&
            (!wp.itemStr("ao_cond").equals("Y")))
           {
            errmsg("[消費本金類] 至少要選一個!");
            return;
           }   
        if (wp.itemStr("feedback_key_sel").length()==0)
           {
            errmsg("[回饋方式]必須輸入 !");
            return;
           }
        if (wp.itemStr("purchase_amt_s1").length()==0)  wp.itemSet("purchase_amt_s1","0");
        if (wp.itemStr("purchase_amt_e1").length()==0)  wp.itemSet("purchase_amt_e1","0");
        if ((wp.itemNum("purchase_amt_s1")==0)||
            (wp.itemNum("purchase_amt_e1")==0))
           {
            errmsg("[門檻一]必須輸入 !");
            return;
           }
        if (wp.itemStr("feedback_amt_1").length()==0)  wp.itemSet("feedback_amt_1","0");
        if (wp.itemStr("feedback_rate_1").length()==0)  wp.itemSet("feedback_rate_1","0");

        if ((wp.itemNum("purchase_amt_e1")!=0)&&
            (wp.itemStr("active_type_1").length()==0))
           {
            errmsg("[門檻一]:回饋類型] 參數未選取 !");
            return;
           }

        if (((wp.itemStr("active_type_1").equals("1"))&&
             (!wp.itemStr("bonus_type_cond").equals("Y")))||
            ((wp.itemStr("active_type_1").equals("2"))&&
             (!wp.itemStr("fund_code_cond").equals("Y")))||
            ((wp.itemStr("active_type_1").equals("3"))&&
             (!wp.itemStr("other_type_cond").equals("Y"))))
           {
            errmsg("[門檻一] 回饋類型 參數未正確選取 !");
            return;
           }
        if (((wp.itemNum("feedback_amt_1")!=0)&&
             (wp.itemNum("feedback_rate_1")!=0))||
            ((wp.itemNum("feedback_amt_1")==0)&&
             (wp.itemNum("feedback_rate_1")==0)))
           {
            errmsg("[門檻一] 給倍數(%)與給點數/現金回饋/贈品數 只能(必須))一項有值 !");
            return;
           }

        if (wp.itemStr("purchase_amt_s2").length()==0)  wp.itemSet("purchase_amt_s2","0");
        if (wp.itemNum("purchase_amt_s2")!=0)
           {
            if (wp.itemStr("above_cond").equals("Y"))
               {
                errmsg("設定[每滿x元,回饋y次] 不可設門檻二(含)以上之消費金額/筆數 !");
                return;
               }

            if (wp.itemStr("feedback_amt_2").length()==0)  wp.itemSet("feedback_amt_2","0");
            if (wp.itemStr("feedback_rate_2").length()==0)  wp.itemSet("feedback_rate_2","0");
 
            if (wp.itemStr("active_type_2").length()==0)
               {
                errmsg("[門檻二] 回饋類型 參數未選取 !");
                return;
               }

            if (((wp.itemStr("active_type_2").equals("1"))&&
                 (!wp.itemStr("bonus_type_cond").equals("Y")))||
                ((wp.itemStr("active_type_2").equals("2"))&&
                 (!wp.itemStr("fund_code_cond").equals("Y")))||
                ((wp.itemStr("active_type_2").equals("3"))&&
                 (!wp.itemStr("other_type_cond").equals("Y"))))
               {
                errmsg("[門檻二] 回饋類型 參數未正確選取 !");
                return;
               }
            if (((wp.itemNum("feedback_amt_2")!=0)&&
                 (wp.itemNum("feedback_rate_2")!=0))||
                ((wp.itemNum("feedback_amt_2")==0)&&
                 (wp.itemNum("feedback_rate_2")==0)))
               {
                errmsg("[門檻二] 給倍數(%)與給點數/現金回饋/贈品數 只能(必須))一項有值 !");
                return;
               }
           }
        if (wp.itemStr("purchase_amt_s3").length()==0)  wp.itemSet("purchase_amt_s3","0");
        if (wp.itemNum("purchase_amt_s3")!=0)
           {
            if (wp.itemStr("feedback_amt_3").length()==0)  wp.itemSet("feedback_amt_3","0");
            if (wp.itemStr("feedback_rate_3").length()==0)  wp.itemSet("feedback_rate_3","0");
        
            if (wp.itemStr("active_type_3").length()==0)
               {
                errmsg("[門檻三] 回饋類型 參數未選取 !");
                return;
               }
            if (((wp.itemStr("active_type_3").equals("1"))&&
                 (!wp.itemStr("bonus_type_cond").equals("Y")))||
                ((wp.itemStr("active_type_3").equals("2"))&&
                 (!wp.itemStr("fund_code_cond").equals("Y")))||
                ((wp.itemStr("active_type_3").equals("3"))&&
                 (!wp.itemStr("other_type_cond").equals("Y"))))
               {
                errmsg("[門檻三] 回饋類型 參數未正確選取 !");
                return;
               }
            if (((wp.itemNum("feedback_amt_3")!=0)&&
                 (wp.itemNum("feedback_rate_3")!=0))||
                ((wp.itemNum("feedback_amt_3")==0)&&
                 (wp.itemNum("feedback_rate_3")==0)))
               {
                errmsg("[門檻三] 給倍數(%)與給點數/現金回饋/贈品數 只能(必須))一項有值 !");
                return;
               }
           }
        if (wp.itemStr("purchase_amt_s4").length()==0)  wp.itemSet("purchase_amt_s4","0");
        if (wp.itemNum("purchase_amt_s4")!=0)
           {
            if (wp.itemStr("feedback_amt_4").length()==0)  wp.itemSet("feedback_amt_4","0");
            if (wp.itemStr("feedback_rate_4").length()==0)  wp.itemSet("feedback_rate_4","0");
        
            if (wp.itemStr("active_type_4").length()==0)
               {
                errmsg("[門檻四] 回饋類型 參數未選取 !");
                return;
               }
            if (((wp.itemStr("active_type_4").equals("1"))&&
                 (!wp.itemStr("bonus_type_cond").equals("Y")))||
                ((wp.itemStr("active_type_4").equals("2"))&&
                 (!wp.itemStr("fund_code_cond").equals("Y")))||
                ((wp.itemStr("active_type_4").equals("3"))&&
                 (!wp.itemStr("other_type_cond").equals("Y"))))
               {
                errmsg("[門檻四] 回饋類型 參數未正確選取 !");
                return;
               }
            if (((wp.itemNum("feedback_amt_4")!=0)&&
                 (wp.itemNum("feedback_rate_4")!=0))||
                ((wp.itemNum("feedback_amt_4")==0)&&
                 (wp.itemNum("feedback_rate_4")==0)))
               {
                errmsg("[門檻四] 給倍數(%)與給點數/現金回饋/贈品數 只能(必須))一項有值 !");
                return;
               }
           }
        if (wp.itemStr("purchase_amt_s5").length()==0)  wp.itemSet("purchase_amt_s5","0");
        if (wp.itemNum("purchase_amt_s5")!=0)
           {
            if (wp.itemStr("feedback_amt_5").length()==0)  wp.itemSet("feedback_amt_5","0");
            if (wp.itemStr("feedback_rate_5").length()==0)  wp.itemSet("feedback_rate_5","0");
        
            if (wp.itemStr("active_type_5").length()==0)
               {
                errmsg("[門檻五] 回饋類型 參數未選取 !");
                return;
               }
            if (((wp.itemStr("active_type_5").equals("1"))&&
                 (!wp.itemStr("bonus_type_cond").equals("Y")))||
                ((wp.itemStr("active_type_5").equals("2"))&&
                 (!wp.itemStr("fund_code_cond").equals("Y")))||
                ((wp.itemStr("active_type_5").equals("3"))&&
                 (!wp.itemStr("other_type_cond").equals("Y"))))
               {
                errmsg("[門檻五] 回饋類型 參數未正確選取 !");
                return;
               }
            if (((wp.itemNum("feedback_amt_5")!=0)&&
                 (wp.itemNum("feedback_rate_5")!=0))||
                ((wp.itemNum("feedback_amt_5")==0)&&
                 (wp.itemNum("feedback_rate_5")==0)))
               {
                errmsg("[門檻五] 給倍數(%)與給點數/現金回饋/贈品數 只能(必須))一項有值 !");
                return;
               }
           }
       }
    if (wp.itemStr("other_type_cond").equals("Y"))
       {
        strSql = "select gift_type "
               + " from mkt_spec_gift "
               + " where  gift_no =  ? "
               + " and    gift_group =  '2' "
               ;
        Object[] param = new Object[] {wp.itemStr("spec_gift_no")};
        sqlSelect(strSql,param);

        if (sqlRowNum <= 0 )
           {
            errmsg("["+wp.itemStr("spec_gift_no")+"]贈品資料不存在 !");
            return;
           }
        if (colStr("gift_type").equals("3"))
           {
            if (wp.itemStr("send_msg_pgm").length()==0)
               { 
                errmsg("電子商品簡訊程式, 必須設定 !");
                return;
               }
           }
       }
    if (wp.itemStr("lottery_cond").equals("Y"))
       {
        if (wp.itemStr("lottery_type").equals("2"))
           {
            if (wp.itemStr("prog_code1").length()==0)
               {
                errmsg("豐富點數活動代碼, 必須選擇 !");
                return;
               }
            int codePos = wp.itemStr("prog_code1").indexOf("-");
            if (codePos == -1)
               {
                errmsg("豐富點數活動代碼, 顯示錯誤 !");
                return;
               }
            colSet("prog_code" , wp.itemStr("prog_code1").substring(0,codePos));
            wp.itemSet("prog_code" , wp.itemStr("prog_code1").substring(0,codePos));
           }
        else if (wp.itemStr("record_cond").equals("Y"))
           {
            errmsg("產生名單(抽獎名單), 不可選擇登錄判斷 !");
            return;
           }

       }
   }
if ((this.ibAdd)||(this.ibUpdate))
   {
    if (!wp.itemEmpty("purchase_date_s")&&(!wp.itemEmpty("purchase_date_e")))
    if (wp.itemStr("purchase_date_s").compareTo(wp.itemStr("purchase_date_e"))>0)
       {
        errmsg("消費期間:["+wp.itemStr("purchase_date_s")+"]>["+wp.itemStr("purchase_date_e")+"] 起迄值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_s1").length()==0)
        wp.itemSet("purchase_amt_s1","0");
    if (wp.itemStr("PURCHASE_AMT_E1").length()==0)
        wp.itemSet("PURCHASE_AMT_E1","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_s1"))>Double.parseDouble(wp.itemStr("PURCHASE_AMT_E1"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E1"))!=0))
       {
        errmsg("區間一:("+wp.itemStr("purchase_amt_s1")+ ")~(" + wp.itemStr("PURCHASE_AMT_E1")+") 起迄值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_e1").length()==0)
        wp.itemSet("purchase_amt_e1","0");
    if (wp.itemStr("PURCHASE_AMT_S2").length()==0)
        wp.itemSet("PURCHASE_AMT_S2","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_e1"))>=Double.parseDouble(wp.itemStr("PURCHASE_AMT_S2"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S2"))!=0))
       {
        errmsg("區間2-3:("+wp.itemStr("purchase_amt_e1")+ ")~(" + wp.itemStr("PURCHASE_AMT_S2")+") 迄起值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_s2").length()==0)
        wp.itemSet("purchase_amt_s2","0");
    if (wp.itemStr("PURCHASE_AMT_E2").length()==0)
        wp.itemSet("PURCHASE_AMT_E2","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_s2"))>Double.parseDouble(wp.itemStr("PURCHASE_AMT_E2"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E2"))!=0))
       {
        errmsg("區間二:("+wp.itemStr("purchase_amt_s2")+ ")~(" + wp.itemStr("PURCHASE_AMT_E2")+") 起迄值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_e2").length()==0)
        wp.itemSet("purchase_amt_e2","0");
    if (wp.itemStr("PURCHASE_AMT_S3").length()==0)
        wp.itemSet("PURCHASE_AMT_S3","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_e2"))>=Double.parseDouble(wp.itemStr("PURCHASE_AMT_S3"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S3"))!=0))
       {
        errmsg("區間2-3:("+wp.itemStr("purchase_amt_e2")+ ")~(" + wp.itemStr("PURCHASE_AMT_S3")+") 迄起值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_s3").length()==0)
        wp.itemSet("purchase_amt_s3","0");
    if (wp.itemStr("PURCHASE_AMT_E3").length()==0)
        wp.itemSet("PURCHASE_AMT_E3","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_s3"))>Double.parseDouble(wp.itemStr("PURCHASE_AMT_E3"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E3"))!=0))
       {
        errmsg("區間三:("+wp.itemStr("purchase_amt_s3")+ ")~(" + wp.itemStr("PURCHASE_AMT_E3")+") 起迄值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_e3").length()==0)
        wp.itemSet("purchase_amt_e3","0");
    if (wp.itemStr("PURCHASE_AMT_S4").length()==0)
        wp.itemSet("PURCHASE_AMT_S4","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_e3"))>=Double.parseDouble(wp.itemStr("PURCHASE_AMT_S4"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S4"))!=0))
       {
        errmsg("區間2-3:("+wp.itemStr("purchase_amt_e3")+ ")~(" + wp.itemStr("PURCHASE_AMT_S4")+") 迄起值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_s4").length()==0)
        wp.itemSet("purchase_amt_s4","0");
    if (wp.itemStr("PURCHASE_AMT_E4").length()==0)
        wp.itemSet("PURCHASE_AMT_E4","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_s4"))>Double.parseDouble(wp.itemStr("PURCHASE_AMT_E4"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E4"))!=0))
       {
        errmsg("區間四:("+wp.itemStr("purchase_amt_s4")+ ")~(" + wp.itemStr("PURCHASE_AMT_E4")+") 起迄值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_e4").length()==0)
        wp.itemSet("purchase_amt_e4","0");
    if (wp.itemStr("PURCHASE_AMT_S5").length()==0)
        wp.itemSet("PURCHASE_AMT_S5","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_e4"))>=Double.parseDouble(wp.itemStr("PURCHASE_AMT_S5"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S5"))!=0))
       {
        errmsg("區間2-3:("+wp.itemStr("purchase_amt_e4")+ ")~(" + wp.itemStr("PURCHASE_AMT_S5")+") 迄起值錯誤!");
        return;
       }
   }

if ((this.ibAdd)||(this.ibUpdate))
   {
    if (wp.itemStr("purchase_amt_s5").length()==0)
        wp.itemSet("purchase_amt_s5","0");
    if (wp.itemStr("PURCHASE_AMT_E5").length()==0)
        wp.itemSet("PURCHASE_AMT_E5","0");
    if (Double.parseDouble(wp.itemStr("purchase_amt_s5"))>Double.parseDouble(wp.itemStr("PURCHASE_AMT_E5"))&&
        (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E5"))!=0))
       {
        errmsg("區間五:("+wp.itemStr("purchase_amt_s5")+ ")~(" + wp.itemStr("PURCHASE_AMT_E5")+") 起迄值錯誤!");
        return;
       }
   }

	if ((this.ibAdd) || (this.ibUpdate)) {
		if(wp.itemEq("feedback_cycle", "M")&&(wp.itemNum("feedback_dd")==0||wp.itemNum("feedback_dd")>31)) {
			errmsg("回饋日需介於1~31之間");
			return;
		}
		if(wp.itemEq("feedback_cycle", "D")&&wp.itemEmpty("cal_def_date")) {
			errmsg("分析日期不可為空值");
			return;
		}
	}

int checkInt = checkDecnum(wp.itemStr("per_amt"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("perday_cnt"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("sum_amt"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("sum_cnt"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("above_amt"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("max_cnt"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_s1"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg("一. 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg("一. 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg("一. 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_e1"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_rate_1"),3,2);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_amt_1"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_s2"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg("二. 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg("二. 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg("二. 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_e2"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_rate_2"),3,2);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_amt_2"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_s3"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg("三. 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg("三. 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg("三. 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_e3"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_rate_3"),3,2);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_amt_3"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_s4"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg("四. 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg("四. 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg("四. 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_e4"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_rate_4"),3,2);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_amt_4"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_s5"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg("五. 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg("五. 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg("五. 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("purchase_amt_e5"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_rate_5"),3,2);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

checkInt = checkDecnum(wp.itemStr("feedback_amt_5"),11,3);
if (checkInt!=0) 
   {
    if (checkInt==1) 
       errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
    if (checkInt==2) 
       errmsg(" 格式超出範圍 : 不可有小數位");
    if (checkInt==3) 
       errmsg(" 非數值");
    return;
   }

if ((this.ibAdd)||(this.ibUpdate))
if (wp.itemEmpty("apr_flag"))
   {
    errmsg("覆核狀態: 不可空白");
    return;
   }


if (this.isAdd()) return;

if (this.ibDelete)
   {
    wp.colSet("storetype" , "N");
   }
}
  
//************************************************************************
@Override
public int dbInsert()
{
 rc = dataSelect();
 if (rc!=1) return rc;
 actionInit("A");
 dataCheck();
 if (rc!=1) return rc;

 dbInsertD4T();
 dbInsertI4T();
 dbInsertD2T();
 dbInsertI2T();
  dbInsertD5T();
  dbInsertI5T();

 strSql = " insert into  " + controlTabName+ " ("
         + " active_code, "
         + " apr_flag, "
         + " aud_type, "
         + " active_name, "
         + " stop_flag, "
         + " stop_date, "
         + " feedback_apr_date, "
         + " feedback_date, "
         + " bonus_type_cond, "
         + " bonus_type, "
         + " tax_flag, "
         + " b_effect_months, "
//         + " bonus_date, "
         + " fund_code_cond, "
         + " fund_code, "
//         + " fund_date, "
         + " f_effect_months, "
         + " other_type_cond, "
         + " spec_gift_no, "
         + " gift_date, "
         + " send_msg_pgm, "
         + " lottery_cond, "
         + " lottery_type, "
//         + " lottery_date, "
         + " prog_msg_pgm, "
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
         + " per_amt_cond, "
         + " per_amt, "
         + " perday_cnt_cond, "
         + " perday_cnt, "
         + " sum_amt_cond, "
         + " sum_amt, "
         + " sum_cnt_cond, "
         + " sum_cnt, "
         + " above_cond, "
         + " above_amt, "
         + " above_cnt, "
         + " max_cnt_cond, "
         + " max_cnt, "
         + " purchase_type_sel, "
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
         + " b_feedback_limit, "
         + " f_feedback_limit, "
         + " s_feedback_limit, "
         + " l_feedback_limit, "
         + " b_feedback_cnt_limit, "
         + " f_feedback_cnt_limit, "
         + " s_feedback_cnt_limit, "
         + " l_feedback_cnt_limit, "
         + " prog_code, "
         + " accumulate_term_sel, "
         + " acct_month, "
         + " crt_date, "
         + " crt_user, "
         + " mod_seqno, "
         + " mod_user, "
         + " mod_time,"
         + " mod_pgm,"
         + " feedback_cycle, "
         + " feedback_dd, "
         + " outfile_type "
         + " ) values ("
         + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
         + "?,?,?,?,"
         + "?,?,?,"
         + "to_char(sysdate,'yyyymmdd'),"
         + "?,"
         + "?,"
         + "?,"
         + "sysdate,?,?,?,?)";

 Object[] param =new Object[]
      {
       kk1,
       wp.itemStr("apr_flag"),
       wp.itemStr("aud_type"),
       wp.itemStr("active_name"),
       wp.itemStr("stop_flag"),
       wp.itemStr("stop_date"),
       wp.itemStr("feedback_apr_date"),
       wp.itemStr("feedback_date"),
       wp.itemStr("bonus_type_cond"),
       wp.itemStr("bonus_type"),
       wp.itemStr("tax_flag"),
       wp.itemNum("b_effect_months"),
//       wp.itemStr("bonus_date"),
       wp.itemStr("fund_code_cond"),
       wp.itemStr("fund_code"),
//       wp.itemStr("fund_date"),
       wp.itemNum("f_effect_months"),
       wp.itemStr("other_type_cond"),
       wp.itemStr("spec_gift_no"),
       wp.itemStr("gift_date"),
       wp.itemStr("send_msg_pgm"),
       wp.itemStr("lottery_cond"),
       wp.itemStr("lottery_type"),
//       wp.itemStr("lottery_date"),
       wp.itemStr("prog_msg_pgm"),
       wp.itemStr("purchase_date_s"),
       wp.itemStr("purchase_date_e"),
       wp.itemStr("cal_def_date"),
       wp.itemStr("list_cond"),
       wp.itemStr("list_flag"),
       wp.itemStr("list_use_sel"),
       wp.itemStr("acct_type_sel"),
       wp.itemStr("group_code_sel"),
       wp.itemStr("mcc_code_sel"),
       wp.itemStr("merchant_sel"),
       wp.itemStr("mcht_group_sel"),
        wp.itemStr("mcht_cname_sel"),
        wp.itemStr("mcht_ename_sel"),
       wp.itemStr("it_term_sel"),
       wp.itemStr("terminal_id_sel"),
       wp.itemStr("pos_entry_sel"),
       wp.itemStr("platform_kind_sel"),
       wp.itemStr("platform_group_sel"),
       wp.itemStr("channel_type_sel"),
       wp.itemStr("bl_cond"),
       wp.itemStr("ca_cond"),
       wp.itemStr("it_cond"),
       wp.itemStr("it_flag"),
       wp.itemStr("id_cond"),
       wp.itemStr("ao_cond"),
       wp.itemStr("ot_cond"),
       wp.itemStr("minus_txn_cond"),
       wp.itemStr("block_cond"),
       wp.itemStr("oppost_cond"),
       wp.itemStr("payment_rate_cond"),
       wp.itemStr("record_cond"),
       wp.itemStr("feedback_key_sel"),
       wp.itemStr("per_amt_cond"),
       wp.itemNum("per_amt"),
       wp.itemStr("perday_cnt_cond"),
       wp.itemNum("perday_cnt"),
       wp.itemStr("sum_amt_cond"),
       wp.itemNum("sum_amt"),
       wp.itemStr("sum_cnt_cond"),
       wp.itemNum("sum_cnt"),
       wp.itemStr("above_cond"),
       wp.itemNum("above_amt"),
       wp.itemNum("above_cnt"),
       wp.itemStr("max_cnt_cond"),
       wp.itemNum("max_cnt"),
       wp.itemStr("purchase_type_sel"),
       wp.itemStr("threshold_sel"),
       wp.itemNum("purchase_amt_s1"),
       wp.itemNum("purchase_amt_e1"),
       wp.itemStr("active_type_1"),
       wp.itemNum("feedback_rate_1"),
       wp.itemNum("feedback_amt_1"),
       wp.itemNum("feedback_lmt_cnt_1"),
       wp.itemNum("feedback_lmt_amt_1"),
       wp.itemNum("purchase_amt_s2"),
       wp.itemNum("purchase_amt_e2"),
       wp.itemStr("active_type_2"),
       wp.itemNum("feedback_rate_2"),
       wp.itemNum("feedback_amt_2"),
       wp.itemNum("feedback_lmt_cnt_2"),
       wp.itemNum("feedback_lmt_amt_2"),
       wp.itemNum("purchase_amt_s3"),
       wp.itemNum("purchase_amt_e3"),
       wp.itemStr("active_type_3"),
       wp.itemNum("feedback_rate_3"),
       wp.itemNum("feedback_amt_3"),
       wp.itemNum("feedback_lmt_cnt_3"),
       wp.itemNum("feedback_lmt_amt_3"),
       wp.itemNum("purchase_amt_s4"),
       wp.itemNum("purchase_amt_e4"),
       wp.itemStr("active_type_4"),
       wp.itemNum("feedback_rate_4"),
       wp.itemNum("feedback_amt_4"),
       wp.itemNum("feedback_lmt_cnt_4"),
       wp.itemNum("feedback_lmt_amt_4"),
       wp.itemNum("purchase_amt_s5"),
       wp.itemNum("purchase_amt_e5"),
       wp.itemStr("active_type_5"),
       wp.itemNum("feedback_rate_5"),
       wp.itemNum("feedback_amt_5"),
       wp.itemNum("feedback_lmt_cnt_5"),
       wp.itemNum("feedback_lmt_amt_5"),
       wp.itemNum("b_feedback_limit"),
       wp.itemNum("f_feedback_limit"),
       wp.itemNum("s_feedback_limit"),
       wp.itemNum("l_feedback_limit"),
       wp.itemNum("b_feedback_cnt_limit"),
       wp.itemNum("f_feedback_cnt_limit"),
       wp.itemNum("s_feedback_cnt_limit"),
       wp.itemNum("l_feedback_cnt_limit"),
       wp.colStr("prog_code"),
       wp.itemStr("accumulate_term_sel"),
       wp.itemStr("acct_month"),
       wp.loginUser,
       wp.modSeqno(),
       wp.loginUser,
       wp.modPgm(),
       wp.itemStr("feedback_cycle"),
       wp.itemStr("feedback_dd"),
       wp.itemStr("outfile_type")
      };

 sqlExec(strSql, param);
 if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 重複錯誤");

 return rc;
}
 
  // ************************************************************************
  public int dbInsertI4T() {
    msgOK();

    strSql = "insert into MKT_IMCHANNEL_LIST_T " + "select * " + "from MKT_IMCHANNEL_LIST "
        + "where active_code = ? " + "";

    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);


    return 1;
  }

  // ************************************************************************
  public int dbInsertI2T() {
    msgOK();

    strSql = "insert into MKT_BN_DATA_T " + "select * " + "from MKT_BN_DATA "
        + "where table_name  =  'MKT_CHANNEL_PARM' " + "and   data_key = ? " + "";

    Object[] param = new Object[] {wp.itemStr("active_code"),};
    
    wp.dupRecord = "Y";
    sqlExec(strSql, param);

    
    return 1;
  }
// ************************************************************************
 public int dbInsertI5T()
 {
   msgOK();

  strSql = "insert into MKT_BN_CDATA_T "
         + "select * "
         + "from MKT_BN_CDATA "
         + "where table_name  =  'MKT_CHANNEL_PARM' "
         + "and   data_key = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr("active_code"),
     };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , false);


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

    strSql = "update " + controlTabName + " set " 
            + "apr_flag = ?, "
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
            + "prog_msg_pgm = ?, "
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
            + "per_amt_cond = ?, "
            + "per_amt = ?, "
            + "perday_cnt_cond = ?, "
            + "perday_cnt = ?, "
            + "sum_amt_cond = ?, "
            + "sum_amt = ?, "
            + "sum_cnt_cond = ?, "
            + "sum_cnt = ?, "
            + "above_cond = ?, "
            + "above_amt = ?, "
            + "above_cnt = ?, "
            + "max_cnt_cond = ?, "
            + "max_cnt = ?, "
            + "purchase_type_sel = ?, "
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
            + "b_feedback_limit = ?, "
            + "f_feedback_limit = ?, "
            + "s_feedback_limit = ?, "
            + "l_feedback_limit = ?, "
            + "b_feedback_cnt_limit = ?, "
            + "f_feedback_cnt_limit = ?, "
            + "s_feedback_cnt_limit = ?, "
            + "l_feedback_cnt_limit = ?, "
            + "prog_code = ?, "
            + "accumulate_term_sel = ?, "
            + "acct_month = ?, "
            + "crt_user  = ?, "
            + "crt_date  = to_char(sysdate,'yyyymmdd'), "
            + "mod_user  = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1, "
            + "mod_time  = sysdate, "
            + "mod_pgm   = ?,"
            + "feedback_cycle = ?,"
            + "feedback_dd = ?,"
            + "outfile_type = ? "
            + "where rowid = ? "
            + "and   mod_seqno = ? ";

     Object[] param =new Object[]
       {
        wp.itemStr("apr_flag"),
        wp.itemStr("active_name"),
        wp.itemStr("stop_flag"),
        wp.itemStr("stop_date"),
        wp.itemStr("bonus_type_cond"),
        wp.itemStr("bonus_type"),
        wp.itemStr("tax_flag"),
        wp.itemNum("b_effect_months"),
        wp.itemStr("fund_code_cond"),
        wp.itemStr("fund_code"),
        wp.itemNum("f_effect_months"),
        wp.itemStr("other_type_cond"),
        wp.itemStr("spec_gift_no"),
        wp.itemStr("send_msg_pgm"),
        wp.itemStr("lottery_cond"),
        wp.itemStr("lottery_type"),
        wp.itemStr("prog_msg_pgm"),
        wp.itemStr("purchase_date_s"),
        wp.itemStr("purchase_date_e"),
        wp.itemStr("cal_def_date"),
        wp.itemStr("list_cond"),
        wp.itemStr("list_flag"),
        wp.itemStr("list_use_sel"),
        wp.itemStr("acct_type_sel"),
        wp.itemStr("group_code_sel"),
        wp.itemStr("mcc_code_sel"),
        wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"),
        wp.itemStr("mcht_cname_sel"),
        wp.itemStr("mcht_ename_sel"),
        wp.itemStr("it_term_sel"),
        wp.itemStr("terminal_id_sel"),
        wp.itemStr("pos_entry_sel"),
        wp.itemStr("platform_kind_sel"),
        wp.itemStr("platform_group_sel"),
        wp.itemStr("channel_type_sel"),
        wp.itemStr("bl_cond"),
        wp.itemStr("ca_cond"),
        wp.itemStr("it_cond"),
        wp.itemStr("it_flag"),
        wp.itemStr("id_cond"),
        wp.itemStr("ao_cond"),
        wp.itemStr("ot_cond"),
        wp.itemStr("minus_txn_cond"),
        wp.itemStr("block_cond"),
        wp.itemStr("oppost_cond"),
        wp.itemStr("payment_rate_cond"),
        wp.itemStr("record_cond"),
        wp.itemStr("feedback_key_sel"),
        wp.itemStr("per_amt_cond"),
        wp.itemNum("per_amt"),
        wp.itemStr("perday_cnt_cond"),
        wp.itemNum("perday_cnt"),
        wp.itemStr("sum_amt_cond"),
        wp.itemNum("sum_amt"),
        wp.itemStr("sum_cnt_cond"),
        wp.itemNum("sum_cnt"),
        wp.itemStr("above_cond"),
        wp.itemNum("above_amt"),
        wp.itemNum("above_cnt"),
        wp.itemStr("max_cnt_cond"),
        wp.itemNum("max_cnt"),
        wp.itemStr("purchase_type_sel"),
        wp.itemStr("threshold_sel"),
        wp.itemNum("purchase_amt_s1"),
        wp.itemNum("purchase_amt_e1"),
        wp.itemStr("active_type_1"),
        wp.itemNum("feedback_rate_1"),
        wp.itemNum("feedback_amt_1"),
        wp.itemNum("feedback_lmt_cnt_1"),
        wp.itemNum("feedback_lmt_amt_1"),
        wp.itemNum("purchase_amt_s2"),
        wp.itemNum("purchase_amt_e2"),
        wp.itemStr("active_type_2"),
        wp.itemNum("feedback_rate_2"),
        wp.itemNum("feedback_amt_2"),
        wp.itemNum("feedback_lmt_cnt_2"),
        wp.itemNum("feedback_lmt_amt_2"),
        wp.itemNum("purchase_amt_s3"),
        wp.itemNum("purchase_amt_e3"),
        wp.itemStr("active_type_3"),
        wp.itemNum("feedback_rate_3"),
        wp.itemNum("feedback_amt_3"),
        wp.itemNum("feedback_lmt_cnt_3"),
        wp.itemNum("feedback_lmt_amt_3"),
        wp.itemNum("purchase_amt_s4"),
        wp.itemNum("purchase_amt_e4"),
        wp.itemStr("active_type_4"),
        wp.itemNum("feedback_rate_4"),
        wp.itemNum("feedback_amt_4"),
        wp.itemNum("feedback_lmt_cnt_4"),
        wp.itemNum("feedback_lmt_amt_4"),
        wp.itemNum("purchase_amt_s5"),
        wp.itemNum("purchase_amt_e5"),
        wp.itemStr("active_type_5"),
        wp.itemNum("feedback_rate_5"),
        wp.itemNum("feedback_amt_5"),
        wp.itemNum("feedback_lmt_cnt_5"),
        wp.itemNum("feedback_lmt_amt_5"),
        wp.itemNum("b_feedback_limit"),
        wp.itemNum("f_feedback_limit"),
        wp.itemNum("s_feedback_limit"),
        wp.itemNum("l_feedback_limit"),
        wp.itemNum("b_feedback_cnt_limit"),
        wp.itemNum("f_feedback_cnt_limit"),
        wp.itemNum("s_feedback_cnt_limit"),
        wp.itemNum("l_feedback_cnt_limit"),
        wp.colStr("prog_code"),
        wp.itemStr("accumulate_term_sel"),
        wp.itemStr("acct_month"),
        wp.loginUser,
        wp.loginUser,
        wp.itemStr("mod_pgm"),
        wp.itemStr("feedback_cycle"),
        wp.itemStr("feedback_dd"),
        wp.itemStr("outfile_type"),
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
    dbInsertD5T();

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

    strSql = "delete MKT_IMCHANNEL_LIST_T " + "WHERE active_code = ? " + "";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("active_code"),};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("刪除 MKT_IMCHANNEL_LIST_T 錯誤");

    return rc;

  }

  // ************************************************************************
  public int dbInsertD2T() {
    msgOK();

    strSql = "delete MKT_BN_DATA_T " + " where table_name  =  'MKT_CHANNEL_PARM' "
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
 public int dbInsertD5T()
 {
   msgOK();

   strSql = "delete MKT_BN_CDATA_T "
         + " where table_name  =  'MKT_CHANNEL_PARM' "
          + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr("active_code"),
     };

   sqlExec(strSql,param,false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 MKT_BN_CDATA_T 錯誤");

   return rc;

 }

  // ************************************************************************
  public int checkDecnum(String decStr, int colLength, int colScale) {
	  if (decStr.length()==0) return(0);
	  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	  if (!comm.isNumber(decStr.replace("-","").replace(".",""))) return(3);
	  decStr = decStr.replace("-","");
	  if ((colScale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) return(2);
	  String[]  parts = decStr.split("[.^]");
	  if ((parts.length==1&&parts[0].length()>colLength)||
	      (parts.length==2&&
	       (parts[0].length()>colLength||parts[1].length()>colScale)))
	      return(1);
	  return(0);
  }

  // ************************************************************************
  public int dbInsertI4() throws Exception {
    msgOK();

    strSql = "insert into MKT_IMCHANNEL_LIST_T ( " + "active_code," + " mod_time, " + " mod_pgm "
        + ") values (" + "?," + " sysdate, " + " ? " + ")";

    Object[] param = new Object[] {wp.itemStr("active_code"), wp.modPgm()};
     
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    if (rc != 1)
      errmsg("新增 MKT_IMCHANNEL_LIST_T 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD4() {
    msgOK();

    // 如果沒有資料回傳成功
    Object[] param = new Object[] {wp.itemStr("active_code")};
    if (sqlRowcount("MKT_IMCHANNEL_LIST_T", "where active_code = ? ", param) <= 0)
      return 1;

    strSql = "delete MKT_IMCHANNEL_LIST_T " + "where active_code = ?  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI2() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0850_actp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm0850_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm0850_mccd"))
      dataType = "5";
    if (wp.respHtml.equals("mktm0850_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm0850_ittr"))
      dataType = "4";
    if (wp.respHtml.equals("mktm0850_term"))
      dataType = "7";
    if (wp.respHtml.equals("mktm0850_posn"))
      dataType = "8";
    if (wp.respHtml.equals("mktm0850_platformn"))
        dataType = "9";
    if (wp.respHtml.equals("mktm0850_platformg"))
        dataType = "10";
    if (wp.respHtml.equals("mktm0850_channel"))
        dataType = "15";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_CHANNEL_PARM', " + "?, " + "?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        wp.loginUser, wp.loginUser, wp.modPgm()};
    
    wp.dupRecord = "Y";
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
    if (wp.respHtml.equals("mktm0850_actp"))
      dataType = "1";
    if (wp.respHtml.equals("mktm0850_gpcd"))
      dataType = "2";
    if (wp.respHtml.equals("mktm0850_mccd"))
      dataType = "5";
    if (wp.respHtml.equals("mktm0850_aaa1"))
      dataType = "6";
    if (wp.respHtml.equals("mktm0850_ittr"))
      dataType = "4";
    if (wp.respHtml.equals("mktm0850_term"))
      dataType = "7";
    if (wp.respHtml.equals("mktm0850_posn"))
      dataType = "8";
    if (wp.respHtml.equals("mktm0850_platformn"))
        dataType = "9";
    if (wp.respHtml.equals("mktm0850_platformg"))
        dataType = "10";
    if (wp.respHtml.equals("mktm0850_channel"))
        dataType = "15";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_CHANNEL_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_CHANNEL_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }

  // ************************************************************************
  public int dbInsertI3() throws Exception {
    msgOK();

    String dataType = "";
    if (wp.respHtml.equals("mktm0850_mrcd"))
      dataType = "3";
    strSql = "insert into MKT_BN_DATA_T ( " + "table_name, " + "data_type, " + "data_key,"
        + "data_code," + "data_code2," + "crt_date, " + "crt_user, " + " mod_time, " + " mod_user, "
        + " mod_seqno, " + " mod_pgm " + ") values (" + "'MKT_CHANNEL_PARM', " + "?, " + "?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + " sysdate, " + "?," + "1," + " ? " + ")";

    Object[] param = new Object[] {dataType, wp.itemStr("active_code"), varsStr("data_code"),
        varsStr("data_code2"), wp.loginUser, wp.loginUser, wp.modPgm()};
    
    wp.dupRecord = "Y";
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
    if (wp.respHtml.equals("mktm0850_mrcd"))
      dataType = "3";
    // 如果沒有資料回傳成功
    Object[] param = new Object[] {dataType, wp.itemStr("active_code")};
    if (sqlRowcount("MKT_BN_DATA_T",
        "where data_type = ? " + "and   data_key = ? " + "and   table_name = 'MKT_CHANNEL_PARM' ",
        param) <= 0)
      return 1;

    strSql = "delete MKT_BN_DATA_T " + "where data_type = ? " + "and   data_key = ?  "
        + "and   table_name = 'MKT_CHANNEL_PARM'  ";
    sqlExec(strSql, param);


    return 1;

  }
// ************************************************************************
 public int dbInsertI5() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0850_namc"))
      dataType = "A" ;
   if (wp.respHtml.equals("mktm0850_name"))
      dataType = "B" ;
  strSql = "insert into MKT_BN_CDATA_T ( "
          + "table_name, "
          + "data_type, "
          + "data_key,"
          + "data_code,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_pgm "
          + ") values ("
          + "'MKT_CHANNEL_PARM', "
          + "?, "
          + "?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      dataType, 
      wp.itemStr("active_code"),
      varsStr("data_code"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_BN_CDATA_T 錯誤");
   else dbUpdateMainU5();

   return rc;
 }
// ************************************************************************
 public int dbUpdateMainU5() throws Exception
  {
   // TODO Auto-update main 
   return rc;
  }
// ************************************************************************
 public int dbDeleteD5()
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0850_namc"))
      dataType = "A" ;
   if (wp.respHtml.equals("mktm0850_name"))
      dataType = "B" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr("active_code")
     };
   if (sqlRowcount("MKT_BN_CDATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_CHANNEL_PARM' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BN_CDATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_CHANNEL_PARM'  "
          ;
   sqlExec(strSql,param,false);


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
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaa1(String table_name) throws Exception {
    strSql = "delete  " + table_name + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_CHANNEL_PARM", wp.itemStr("active_code"), "3"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + table_name + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int dbInsertI2Aaat(String tableName, String[] columnCol, String[] columnDat) {
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
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }

  // ************************************************************************
  public int dbDeleteD2Aaat(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_CHANNEL_PARM", wp.itemStr("active_code"), "7"};

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

    wp.dupRecord = "Y";
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
    
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 ecs_modify_log 錯誤");
    return rc;
  }
//************************************************************************
int listParmDataCnt(String s1,String s2,String s3,String s4)
{
 String strSql = "select count(*) as data_cnt "
               + "from  " + s1 +" "
               + " where table_name = ? "
               + " and   data_key   = ? "
               + " and   data_type  = ? "
               ;
 Object[] param = new Object[] {s2,s3,s4};
 sqlSelect(strSql,param);

return(Integer.parseInt(colStr("data_cnt")));
}
//************************************************************************
int listImchannelDataCnt(String s1,String s2,String s3,String s4)
{
 String strSql = "select count(*) as data_cnt "
               + "from  " + s1 +" "
               + " where  active_code = ? "
               ;
 Object[] param = new Object[] {s3};
 sqlSelect(strSql,param);

return(Integer.parseInt(colStr("data_cnt")));
}


//************************************************************************

}  // End of class

