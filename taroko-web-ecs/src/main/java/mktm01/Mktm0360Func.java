/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/02  V1.00.02   Allen Ho      Initial                              *
* 111/12/07  V1.00.03  Machao    sync from mega & updated for project coding standard 
* 111/12/16  V1.00.04   Machao        命名规则调整后测试修改                                                                        *
* 111/12/22  V1.00.05   Zuwei        listBpmh3DataCnt issue
* 111/12/22  V1.00.06   Zuwei         輸出sql log                                                                     *
* 111/12/23  V1.00.07   Zuwei         區間檢查問題                                                                     *
* 112/04/06  V1.00.08   JiangYingdong        program update                *
* 112/04/06  V1.00.09   Zuwei         區間門檻二三檢查問題                                                                     *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0360Func extends FuncEdit
{
 private final String PROGNAME = "紅利特惠參數檔維護處理程式111/12/22 V1.00.05";
  String kk1;
  String orgControlTabName = "mkt_bpmh3";
  String controlTabName = "mkt_bpmh3_t";

 public Mktm0360Func(TarokoCommon wr)
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
  String procTabName="";
  procTabName = wp.itemStr2("control_tab_name");
  if (procTabName.length()==0) return(1);
  strSql= " select "
          + " apr_flag, "
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
          + " run_start_cond, "
          + " run_start_month, "
          + " run_time_mm, "
          + " run_time_type, "
          + " run_time_dd, "
          + " per_point_amt, "
          + " feedback_lmt, "
          + " list_cond, "
          + " vd_flag, "
          + " acct_type_sel, "
          + " vd_corp_flag, "
          + " issue_cond, "
          + " issue_date_s, "
          + " issue_date_e, "
          + " card_re_days, "
          + " purch_cond, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " group_card_sel, "
//          + " group_oppost_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " mcc_code_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " it_flag, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " bill_type_sel, "
          + " currency_sel, "
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
          + " platform2_kind_sel = ?, "
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
          + " d_add_item_flag, "
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
        wp.itemRowId("rowid")
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck() 
 {
  if (!this.ibDelete)
     {
      if (wp.colStr("storetype").equals("Y"))
        {
         errmsg("[查原資料]模式中, 請按[還原異動] 才可儲存 !");
         return;
        }
     }
  if (this.ibAdd)
     {
      kk1 = wp.itemStr2("active_code");
     }
  else
     {
      kk1 = wp.itemStr2("active_code");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where active_code = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代號] 不可重複("+orgControlTabName+"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where active_code = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代號] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }

  if (this.ibUpdate)
     {
      if ((wp.itemStr2("acct_type_sel").equals("1"))||
          (wp.itemStr2("acct_type_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"1")==0)
             {
              errmsg("[B.帳戶類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("group_card_sel").equals("1"))||
          (wp.itemStr2("group_card_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"2")==0)
             {
              errmsg("[團代卡種] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("merchant_sel").equals("1"))||
          (wp.itemStr2("merchant_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"3")==0)
             {
              errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("mcht_group_sel").equals("1"))||
          (wp.itemStr2("mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"7")==0)
             {
              errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("platform_kind_sel").equals("1"))||
          (wp.itemStr2("platform_kind_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"P")==0)
             {
              errmsg("[一般消費群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("mcc_code_sel").equals("1"))||
          (wp.itemStr2("mcc_code_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"4")==0)
             {
              errmsg("[F.特店類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("bill_type_sel").equals("1"))||
          (wp.itemStr2("bill_type_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"5")==0)
             {
              errmsg("[H.帳單來源] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("currency_sel").equals("1"))||
          (wp.itemStr2("currency_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"6")==0)
             {
              errmsg("[I.交易幣別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_group_card_sel").equals("1"))||
          (wp.itemStr2("d_group_card_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"A")==0)
             {
              errmsg("[團代卡種] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_merchant_sel").equals("1"))||
          (wp.itemStr2("d_merchant_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"B")==0)
             {
              errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_mcht_group_sel").equals("1"))||
          (wp.itemStr2("d_mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"G")==0)
             {
              errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("platform2_kind_sel").equals("1"))||
          (wp.itemStr2("platform2_kind_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"P2")==0)
             {
              errmsg("[一般消費群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_mcc_code_sel").equals("1"))||
          (wp.itemStr2("d_mcc_code_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"C")==0)
             {
              errmsg("[B.特店類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_card_type_sel").equals("1"))||
          (wp.itemStr2("d_card_type_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"F")==0)
             {
              errmsg("[C.卡種] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_bill_type_sel").equals("1"))||
          (wp.itemStr2("d_bill_type_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"D")==0)
             {
              errmsg("[E.帳單來源] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_currency_sel").equals("1"))||
          (wp.itemStr2("d_currency_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"E")==0)
             {
              errmsg("[F.交易幣別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_pos_entry_sel").equals("1"))||
          (wp.itemStr2("d_pos_entry_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"H")==0)
             {
              errmsg("[G.POS ENTRY] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_ucaf_sel").equals("1"))||
          (wp.itemStr2("d_ucaf_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"I")==0)
             {
              errmsg("[H.UCAF] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("d_eci_sel").equals("1"))||
          (wp.itemStr2("d_eci_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_bn_data_t"
                                ,"MKT_BPMH3"
                                ,wp.colStr("active_code")
                                ,"J")==0)
             {
              errmsg("[I.ECI] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
     }
  if (!wp.itemStr2("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
  if (!wp.itemStr2("run_start_cond").equals("Y")) wp.itemSet("run_start_cond","N");
  if (!wp.itemStr2("vd_flag").equals("Y")) wp.itemSet("vd_flag","N");
  if (!wp.itemStr2("vd_corp_flag").equals("Y")) wp.itemSet("vd_corp_flag","N");
  if (!wp.itemStr2("issue_cond").equals("Y")) wp.itemSet("issue_cond","N");
  if (!wp.itemStr2("purch_cond").equals("Y")) wp.itemSet("purch_cond","N");
//  if (!wp.itemStr2("group_oppost_cond").equals("Y")) wp.itemSet("group_oppost_cond","N");
  if (!wp.itemStr2("bl_cond").equals("Y")) wp.itemSet("bl_cond","N");
  if (!wp.itemStr2("ca_cond").equals("Y")) wp.itemSet("ca_cond","N");
  if (!wp.itemStr2("it_cond").equals("Y")) wp.itemSet("it_cond","N");
  if (!wp.itemStr2("id_cond").equals("Y")) wp.itemSet("id_cond","N");
  if (!wp.itemStr2("ao_cond").equals("Y")) wp.itemSet("ao_cond","N");
  if (!wp.itemStr2("ot_cond").equals("Y")) wp.itemSet("ot_cond","N");
  if (!wp.itemStr2("doorsill_flag").equals("Y")) wp.itemSet("doorsill_flag","N");
  if (!wp.itemStr2("d_bl_cond").equals("Y")) wp.itemSet("d_bl_cond","N");
  if (!wp.itemStr2("d_ca_cond").equals("Y")) wp.itemSet("d_ca_cond","N");
  if (!wp.itemStr2("d_it_cond").equals("Y")) wp.itemSet("d_it_cond","N");
  if (!wp.itemStr2("d_id_cond").equals("Y")) wp.itemSet("d_id_cond","N");
  if (!wp.itemStr2("d_ao_cond").equals("Y")) wp.itemSet("d_ao_cond","N");
  if (!wp.itemStr2("d_ot_cond").equals("Y")) wp.itemSet("d_ot_cond","N");

   if (this.ibUpdate)
      {
       if (wp.itemStr2("list_cond").length()!=0)
          {
          if (listBpmh3DataCnt("mkt_bpmh3_list_t"
                                ,""
                                ,wp.colStr("active_code")
                                ,"")==0)
              {
               errmsg("[A.指定名單] 明細沒有設定, 筆數不可為 0  !");
               return;
              }
          }
      }

   if (wp.itemStr2("aud_type").equals("A"))
      {
       if (wp.itemStr2("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }
   else
      {
       if (wp.itemStr2("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }

   if (wp.itemEmpty("apr_flag")) {
       wp.colSet("apr_flag", "N");
       wp.itemSet("apr_flag", "N");
   }
   if ((this.ibDelete)||
       (wp.itemStr2("aud_type").equals("D"))) return;

   if (wp.itemStr2("per_point_amt").length()==0) wp.itemSet("per_point_amt","0");
   if (wp.itemNum("per_point_amt")==0)
      {
       errmsg("加贈點數計算基準, 數值要大於 0!");
       return;
      }

   if (wp.itemStr2("stop_flag").equals("Y"))
      {
       if ((wp.itemStr2("stop_date").length()==0)||
           (wp.itemStr2("stop_desc").length()==0))
          {
           errmsg("[取消日期與取消說明], 必須輸入 !");
           return;
          }
      }

   if (wp.itemStr2("run_start_cond").equals("Y"))
      {
       if (wp.itemStr2("run_start_month").length()==0) wp.itemSet("run_start_month","0");
       if (wp.itemStr2("run_time_mm").length()==0) wp.itemSet("run_time_mm","0");
       if ((wp.itemNum("run_start_month")==0)||
           (wp.itemNum("run_time_mm")==0))
          {
           errmsg("[產生起始年月][每N月回饋一次] 必須輸入!");
           return;
          }
      }
   if (wp.itemStr2("run_time_type").equals("2"))
      {
       if (wp.itemStr2("run_time_dd").length()==0) wp.itemSet("run_time_dd","0");
       if (wp.itemNum("run_time_dd")==0) 
          {
           errmsg("[回饋執行方式][每月N日執行] 必須輸入!");
           return;
          }
       if ((wp.itemNum("run_time_dd")<1)||
           (wp.itemNum("run_time_dd")>28))
          {
           errmsg("[回饋執行方式][每月N日執行],N日需在1-28!");
           return;
          }
      }
   if (wp.itemStr2("issue_cond").equals("Y"))
      {
       if ((wp.itemStr2("issue_date_s").length()==0)||
           (wp.itemStr2("issue_date_e").length()==0))
         {
          errmsg("[發卡日期起迄]必須輸入!");
          return;
         }
       if (wp.itemStr2("card_re_days").length()==0) wp.itemSet("card_re_days","0");
       if (wp.itemNum("card_re_days")==0)
          {
           errmsg("[發卡後N天內回饋] N必須輸入!");
           return;
          }
      }
   if (wp.itemStr2("purch_cond").equals("Y"))
      {
       if ((wp.itemStr2("purch_s_date").length()==0)||
           (wp.itemStr2("purch_e_date").length()==0))
          {
           errmsg("[消費日期起迄]必須輸入!");
           return;
          }
      }
   if ((!wp.itemStr2("bl_cond").equals("Y"))&&
       (!wp.itemStr2("ot_cond").equals("Y"))&&
       (!wp.itemStr2("it_cond").equals("Y"))&&
       (!wp.itemStr2("ca_cond").equals("Y"))&&
       (!wp.itemStr2("id_cond").equals("Y"))&&
       (!wp.itemStr2("ao_cond").equals("Y")))
      {
       errmsg("[G.消費本金類] 至少要選一個!");
       return;
      }

   if (wp.itemStr2("add_amt_e1").length()==0) wp.itemSet("add_amt_e1","0");
   if (wp.itemNum("add_amt_e1")==0)
      {
       errmsg("[基本門檻]至少要輸入一筆!");
       return;
      }
   if (!wp.itemStr2("doorsill_flag").equals("Y"))
      {
       if (wp.itemStr2("add_times1").length()==0) wp.itemSet("add_times1","0");
       if (wp.itemStr2("add_point1").length()==0) wp.itemSet("add_point1","0");
       if ((wp.itemNum("add_times1")==0)&&
           (wp.itemNum("add_point1")==0))
          {
           errmsg("[倍數或點數]至少要輸入一筆!");
           return;
          }
      }
   if (wp.itemStr2("doorsill_flag").equals("Y"))
      {
       if ((!wp.itemStr2("d_bl_cond").equals("Y"))&&
           (!wp.itemStr2("d_ot_cond").equals("Y"))&&
           (!wp.itemStr2("d_it_cond").equals("Y"))&&
           (!wp.itemStr2("d_ca_cond").equals("Y"))&&
           (!wp.itemStr2("d_id_cond").equals("Y"))&&
           (!wp.itemStr2("d_ao_cond").equals("Y")))
          {
           errmsg("[D.消費本金類] 至少要選一個!");
           return;
          }
       if (wp.itemStr2("d_add_amt_e1").length()==0) wp.itemSet("d_add_amt_e1","0");
       if (wp.itemNum("d_add_amt_e1")==0)
          {
           errmsg("[加贈門檻]至少要輸入一筆!");
           return;
          }
       if (wp.itemStr2("d_add_point1").length()==0) wp.itemSet("d_add_point1","0");
/*
       if (wp.item_num("d_add_times1")==0)
          {
           errmsg("[加贈門檻點數]必須輸入!");
           return;
          }

*/
      }
  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("active_date_s")&&(!wp.itemEmpty("ACTIVE_DATE_e")))
      if (wp.itemStr2("active_date_s").compareTo(wp.itemStr2("ACTIVE_DATE_e"))>0)
         {
          errmsg("活動日期：["+wp.itemStr2("active_date_s")+"]>["+wp.itemStr2("ACTIVE_DATE_e")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("issue_date_s")&&(!wp.itemEmpty("ISSUE_E_DATE")))
      if (wp.itemStr2("issue_date_s").compareTo(wp.itemStr2("ISSUE_E_DATE"))>0)
         {
          errmsg("["+wp.itemStr2("issue_date_s")+"]>["+wp.itemStr2("ISSUE_E_DATE")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("purch_s_date")&&(!wp.itemEmpty("PURCH_E_DATE")))
      if (wp.itemStr2("purch_s_date").compareTo(wp.itemStr2("PURCH_E_DATE"))>0)
         {
          errmsg("["+wp.itemStr2("purch_s_date")+"]>["+wp.itemStr2("PURCH_E_DATE")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s1").length()==0)
          wp.itemSet("add_amt_s1","0");
      if (wp.itemStr2("add_amt_e1").length()==0)
          wp.itemSet("add_amt_e1","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s1"))>Double.parseDouble(wp.itemStr2("add_amt_e1"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e1"))!=0))
         {
          errmsg("區間一:("+wp.itemStr2("add_amt_s1")+ ")~(" + wp.itemStr2("add_amt_e1")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s2").length()==0)
          wp.itemSet("add_amt_s2","0");
      if (wp.itemStr2("add_amt_e2").length()==0)
          wp.itemSet("add_amt_e2","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s2"))>Double.parseDouble(wp.itemStr2("add_amt_e2"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e2"))!=0))
         {
          errmsg("區間二:("+wp.itemStr2("add_amt_s2")+ ")~(" + wp.itemStr2("add_amt_e2")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s3").length()==0)
          wp.itemSet("add_amt_s3","0");
      if (wp.itemStr2("add_amt_e3").length()==0)
          wp.itemSet("add_amt_e3","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s3"))>Double.parseDouble(wp.itemStr2("add_amt_e3"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e3"))!=0))
         {
          errmsg("區間三:("+wp.itemStr2("add_amt_s3")+ ")~(" + wp.itemStr2("add_amt_e3")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s4").length()==0)
          wp.itemSet("add_amt_s4","0");
      if (wp.itemStr2("add_amt_e4").length()==0)
          wp.itemSet("add_amt_e4","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s4"))>Double.parseDouble(wp.itemStr2("add_amt_e4"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e4"))!=0))
         {
          errmsg("區間四:("+wp.itemStr2("add_amt_s4")+ ")~(" + wp.itemStr2("add_amt_e4")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s5").length()==0)
          wp.itemSet("add_amt_s5","0");
      if (wp.itemStr2("add_amt_e5").length()==0)
          wp.itemSet("add_amt_e5","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s5"))>Double.parseDouble(wp.itemStr2("add_amt_e5"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e5"))!=0))
         {
          errmsg("區間五:("+wp.itemStr2("add_amt_s5")+ ")~(" + wp.itemStr2("add_amt_e5")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s6").length()==0)
          wp.itemSet("add_amt_s6","0");
      if (wp.itemStr2("add_amt_e6").length()==0)
          wp.itemSet("add_amt_e6","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s6"))>Double.parseDouble(wp.itemStr2("add_amt_e6"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e6"))!=0))
         {
          errmsg("區間六:("+wp.itemStr2("add_amt_s6")+ ")~(" + wp.itemStr2("add_amt_e6")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s7").length()==0)
          wp.itemSet("add_amt_s7","0");
      if (wp.itemStr2("add_amte_7").length()==0)
          wp.itemSet("add_amte_7","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s7"))>Double.parseDouble(wp.itemStr2("add_amte_7"))&&
          (Double.parseDouble(wp.itemStr2("add_amte_7"))!=0))
         {
          errmsg("區間七:("+wp.itemStr2("add_amt_s7")+ ")~(" + wp.itemStr2("add_amte_7")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s8").length()==0)
          wp.itemSet("add_amt_s8","0");
      if (wp.itemStr2("add_amt_e8").length()==0)
          wp.itemSet("add_amt_e8","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s8"))>Double.parseDouble(wp.itemStr2("add_amt_e8"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e8"))!=0))
         {
          errmsg("區間八:("+wp.itemStr2("add_amt_s8")+ ")~(" + wp.itemStr2("add_amt_e8")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s9").length()==0)
          wp.itemSet("add_amt_s9","0");
      if (wp.itemStr2("add_amt_e9").length()==0)
          wp.itemSet("add_amt_e9","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s9"))>Double.parseDouble(wp.itemStr2("add_amt_e9"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e9"))!=0))
         {
          errmsg("區間九:("+wp.itemStr2("add_amt_s9")+ ")~(" + wp.itemStr2("add_amt_e9")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("add_amt_s10").length()==0)
          wp.itemSet("add_amt_s10","0");
      if (wp.itemStr2("add_amt_e10").length()==0)
          wp.itemSet("add_amt_e10","0");
      if (Double.parseDouble(wp.itemStr2("add_amt_s10"))>Double.parseDouble(wp.itemStr2("add_amt_e10"))&&
          (Double.parseDouble(wp.itemStr2("add_amt_e10"))!=0))
         {
          errmsg("區間十:("+wp.itemStr2("add_amt_s10")+ ")~(" + wp.itemStr2("add_amt_e10")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s1").length()==0)
          wp.itemSet("d_add_amt_s1","0");
      if (wp.itemStr2("d_add_amt_e1").length()==0)
          wp.itemSet("d_add_amt_e1","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s1"))>Double.parseDouble(wp.itemStr2("d_add_amt_e1"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e1"))!=0))
         {
          errmsg("區間門檻一("+wp.itemStr2("d_add_amt_s1")+ ")~(" + wp.itemStr2("d_add_amt_e1")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s2").length()==0)
          wp.itemSet("d_add_amt_s2","0");
      if (wp.itemStr2("d_add_amt_e2").length()==0)
          wp.itemSet("d_add_amt_e2","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s2"))>Double.parseDouble(wp.itemStr2("d_add_amt_e2"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e2"))!=0))
         {
          errmsg("區間門檻二("+wp.itemStr2("d_add_amt_s2")+ ")~(" + wp.itemStr2("d_add_amt_e2")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s3").length()==0)
          wp.itemSet("d_add_amt_s3","0");
      if (wp.itemStr2("d_add_amt_e3").length()==0)
          wp.itemSet("d_add_amt_e3","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s3"))>Double.parseDouble(wp.itemStr2("d_add_amt_e3"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e3"))!=0))
         {
          errmsg("區間門檻三("+wp.itemStr2("d_add_amt_s3")+ ")~(" + wp.itemStr2("d_add_amt_e3")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s4").length()==0)
          wp.itemSet("d_add_amt_s4","0");
      if (wp.itemStr2("d_add_amt_e4").length()==0)
          wp.itemSet("d_add_amt_e4","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s4"))>Double.parseDouble(wp.itemStr2("d_add_amt_e4"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e4"))!=0))
         {
          errmsg("區間門檻四("+wp.itemStr2("d_add_amt_s4")+ ")~(" + wp.itemStr2("d_add_amt_e4")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s5").length()==0)
          wp.itemSet("d_add_amt_s5","0");
      if (wp.itemStr2("d_add_amt_e5").length()==0)
          wp.itemSet("d_add_amt_e5","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s5"))>Double.parseDouble(wp.itemStr2("d_add_amt_e5"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e5"))!=0))
         {
          errmsg("區間門檻五("+wp.itemStr2("d_add_amt_s5")+ ")~(" + wp.itemStr2("d_add_amt_e5")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s6").length()==0)
          wp.itemSet("d_add_amt_s6","0");
      if (wp.itemStr2("d_add_amt_e6").length()==0)
          wp.itemSet("d_add_amt_e6","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s6"))>Double.parseDouble(wp.itemStr2("d_add_amt_e6"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e6"))!=0))
         {
          errmsg("區間門檻六("+wp.itemStr2("d_add_amt_s6")+ ")~(" + wp.itemStr2("d_add_amt_e6")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s7").length()==0)
          wp.itemSet("d_add_amt_s7","0");
      if (wp.itemStr2("d_add_amt_e7").length()==0)
          wp.itemSet("d_add_amt_e7","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s7"))>Double.parseDouble(wp.itemStr2("d_add_amt_e7"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e7"))!=0))
         {
          errmsg("區間門檻七("+wp.itemStr2("d_add_amt_s7")+ ")~(" + wp.itemStr2("d_add_amt_e7")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s8").length()==0)
          wp.itemSet("d_add_amt_s8","0");
      if (wp.itemStr2("d_add_amt_e8").length()==0)
          wp.itemSet("d_add_amt_e8","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s8"))>Double.parseDouble(wp.itemStr2("d_add_amt_e8"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e8"))!=0))
         {
          errmsg("區間門檻八("+wp.itemStr2("d_add_amt_s8")+ ")~(" + wp.itemStr2("d_add_amt_e8")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s9").length()==0)
          wp.itemSet("d_add_amt_s9","0");
      if (wp.itemStr2("d_add_amt_e9").length()==0)
          wp.itemSet("d_add_amt_e9","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s9"))>Double.parseDouble(wp.itemStr2("d_add_amt_e9"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e9"))!=0))
         {
          errmsg("區間門檻九("+wp.itemStr2("d_add_amt_s9")+ ")~(" + wp.itemStr2("d_add_amt_e9")+") 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("d_add_amt_s10").length()==0)
          wp.itemSet("d_add_amt_s10","0");
      if (wp.itemStr2("d_add_amt_e10").length()==0)
          wp.itemSet("d_add_amt_e10","0");
      if (Double.parseDouble(wp.itemStr2("d_add_amt_s10"))>Double.parseDouble(wp.itemStr2("d_add_amt_e10"))&&
          (Double.parseDouble(wp.itemStr2("d_add_amt_e10"))!=0))
         {
          errmsg("區間門檻十("+wp.itemStr2("d_add_amt_s10")+ ")~(" + wp.itemStr2("d_add_amt_e10")+") 起迄值錯誤!");
          return;
         }
     }

  int checkInt = checkDecnum(wp.itemStr2("feedback_lmt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("紅利點數每次回饋上限： 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("紅利點數每次回饋上限： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("紅利點數每次回饋上限： 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_item_amt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("單筆金額： 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("單筆金額： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("單筆金額： 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s1"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("一. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("一. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("一. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e1"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s2"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("二. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("二. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("二. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e2"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s3"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("三. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("三. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("三. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e3"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s4"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("四. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("四. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("四. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e4"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s5"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("五. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("五. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("五. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e5"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s6"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("六. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("六. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("六. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e6"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s7"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("七. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("七. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("七. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e7"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s8"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("八. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("八. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("八. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e8"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s9"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("九. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("九. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("九. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e9"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_s10"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("十. 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("十. 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("十. 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("add_amt_e10"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_item_amt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("單筆金額： 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("單筆金額： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("單筆金額： 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s1"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻一 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻一 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻一 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e1"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s2"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻二 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻二 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻二 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e2"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s3"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻三 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻三 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻三 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e3"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s4"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻四 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻四 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻四 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e4"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s5"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻五 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻五 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻五 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e5"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s6"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻六 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻六 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻六 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e6"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s7"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻七 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻七 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻七 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e7"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s8"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻八 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻八 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻八 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e8"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s9"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻九 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻九 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻九 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e9"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_s10"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("門檻十 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("門檻十 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("門檻十 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("d_add_amt_e10"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
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

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("active_name"))
     {
      errmsg("活動名稱： 不可空白");
      return;
     }


  if (this.isAdd()) return;

  if (this.ibDelete)
     {
      wp.colSet("storetype" , "N");
     }
 }
// ************************************************************************
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

  strSql= " insert into  " + controlTabName+ " ("
          + " active_code, "
          + " apr_flag, "
          + " aud_type, "
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
          + " run_start_cond, "
          + " run_start_month, "
          + " run_time_mm, "
          + " run_time_type, "
          + " run_time_dd, "
          + " per_point_amt, "
          + " feedback_lmt, "
          + " list_cond, "
          + " vd_flag, "
          + " acct_type_sel, "
          + " vd_corp_flag, "
          + " issue_cond, "
          + " issue_date_s, "
          + " issue_date_e, "
          + " card_re_days, "
          + " purch_cond, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " group_card_sel, "
//          + " group_oppost_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " mcc_code_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " it_flag, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " bill_type_sel, "
          + " currency_sel, "
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
          + " d_add_item_flag, "
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
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr2("apr_flag"),
        wp.itemStr2("aud_type"),
        wp.itemStr2("active_name"),
        wp.itemStr2("bonus_type"),
        wp.itemStr2("tax_flag"),
        wp.itemStr2("active_date_s"),
        wp.itemStr2("active_date_e"),
        wp.itemStr2("proc_date"),
        wp.itemNum("effect_months"),
        wp.itemStr2("stop_flag"),
        wp.itemStr2("stop_date"),
        wp.itemStr2("stop_desc"),
        wp.itemStr2("run_start_cond"),
        wp.itemStr2("run_start_month"),
        wp.itemNum("run_time_mm"),
        wp.itemStr2("run_time_type"),
        wp.itemNum("run_time_dd"),
        wp.itemNum("per_point_amt"),
        wp.itemNum("feedback_lmt"),
        wp.itemStr2("list_cond"),
        wp.itemStr2("vd_flag"),
        wp.itemStr2("acct_type_sel"),
        wp.itemStr2("vd_corp_flag"),
        wp.itemStr2("issue_cond"),
        wp.itemStr2("issue_date_s"),
        wp.itemStr2("issue_date_e"),
        wp.itemNum("card_re_days"),
        wp.itemStr2("purch_cond"),
        wp.itemStr2("purch_s_date"),
        wp.itemStr2("purch_e_date"),
        wp.itemStr2("group_card_sel"),
//        wp.itemStr2("group_oppost_cond"),
        wp.itemStr2("merchant_sel"),
        wp.itemStr2("mcht_group_sel"),
        wp.itemStr2("platform_kind_sel"),
        wp.itemStr2("mcc_code_sel"),
        wp.itemStr2("bl_cond"),
        wp.itemStr2("ca_cond"),
        wp.itemStr2("it_cond"),
        wp.itemStr2("it_flag"),
        wp.itemStr2("id_cond"),
        wp.itemStr2("ao_cond"),
        wp.itemStr2("ot_cond"),
        wp.itemStr2("bill_type_sel"),
        wp.itemStr2("currency_sel"),
        wp.itemStr2("add_type"),
        wp.itemStr2("add_item_flag"),
        wp.itemNum("add_item_amt"),
        wp.itemNum("add_amt_s1"),
        wp.itemNum("add_amt_e1"),
        wp.itemNum("add_times1"),
        wp.itemNum("add_point1"),
        wp.itemNum("add_amt_s2"),
        wp.itemNum("add_amt_e2"),
        wp.itemNum("add_times2"),
        wp.itemNum("add_point2"),
        wp.itemNum("add_amt_s3"),
        wp.itemNum("add_amt_e3"),
        wp.itemNum("add_times3"),
        wp.itemNum("add_point3"),
        wp.itemNum("add_amt_s4"),
        wp.itemNum("add_amt_e4"),
        wp.itemNum("add_times4"),
        wp.itemNum("add_point4"),
        wp.itemNum("add_amt_s5"),
        wp.itemNum("add_amt_e5"),
        wp.itemNum("add_times5"),
        wp.itemNum("add_point5"),
        wp.itemNum("add_amt_s6"),
        wp.itemNum("add_amt_e6"),
        wp.itemNum("add_times6"),
        wp.itemNum("add_point6"),
        wp.itemNum("add_amt_s7"),
        wp.itemNum("add_amt_e7"),
        wp.itemNum("add_times7"),
        wp.itemNum("add_point7"),
        wp.itemNum("add_amt_s8"),
        wp.itemNum("add_amt_e8"),
        wp.itemNum("add_times8"),
        wp.itemNum("add_point8"),
        wp.itemNum("add_amt_s9"),
        wp.itemNum("add_amt_e9"),
        wp.itemNum("add_times9"),
        wp.itemNum("add_point9"),
        wp.itemNum("add_amt_s10"),
        wp.itemNum("add_amt_e10"),
        wp.itemNum("add_times10"),
        wp.itemNum("add_point10"),
        wp.itemStr2("doorsill_flag"),
        wp.itemStr2("d_group_card_sel"),
        wp.itemStr2("d_merchant_sel"),
        wp.itemStr2("d_mcht_group_sel"),
        wp.itemStr2("platform2_kind_sel"),
        wp.itemStr2("d_mcc_code_sel"),
        wp.itemStr2("d_card_type_sel"),
        wp.itemStr2("d_bl_cond"),
        wp.itemStr2("d_ca_cond"),
        wp.itemStr2("d_it_cond"),
        wp.itemStr2("d_it_flag"),
        wp.itemStr2("d_id_cond"),
        wp.itemStr2("d_ao_cond"),
        wp.itemStr2("d_ot_cond"),
        wp.itemStr2("d_bill_type_sel"),
        wp.itemStr2("d_currency_sel"),
        wp.itemStr2("d_pos_entry_sel"),
        wp.itemStr2("d_ucaf_sel"),
        wp.itemStr2("d_eci_sel"),
        wp.itemStr2("d_add_item_flag"),
        wp.itemNum("d_add_amt_s1"),
        wp.itemNum("d_add_amt_e1"),
        wp.itemNum("d_add_point1"),
        wp.itemNum("d_add_amt_s2"),
        wp.itemNum("d_add_amt_e2"),
        wp.itemNum("d_add_point2"),
        wp.itemNum("d_add_amt_s3"),
        wp.itemNum("d_add_amt_e3"),
        wp.itemNum("d_add_point3"),
        wp.itemNum("d_add_amt_s4"),
        wp.itemNum("d_add_amt_e4"),
        wp.itemNum("d_add_point4"),
        wp.itemNum("d_add_amt_s5"),
        wp.itemNum("d_add_amt_e5"),
        wp.itemNum("d_add_point5"),
        wp.itemNum("d_add_amt_s6"),
        wp.itemNum("d_add_amt_e6"),
        wp.itemNum("d_add_point6"),
        wp.itemNum("d_add_amt_s7"),
        wp.itemNum("d_add_amt_e7"),
        wp.itemNum("d_add_point7"),
        wp.itemNum("d_add_amt_s8"),
        wp.itemNum("d_add_amt_e8"),
        wp.itemNum("d_add_point8"),
        wp.itemNum("d_add_amt_s9"),
        wp.itemNum("d_add_amt_e9"),
        wp.itemNum("d_add_point9"),
        wp.itemNum("d_add_amt_s10"),
        wp.itemNum("d_add_amt_e10"),
        wp.itemNum("d_add_point10"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 重複錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertI4T() 
 {
   msgOK();

  strSql = "insert into MKT_BPMH3_LIST_T "
         + "select * "
         + "from MKT_BPMH3_LIST "
         + "where active_code = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr2("active_code"),
     };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , true);


   return 1;
 }
// ************************************************************************
 public int dbInsertI2T() 
 {
   msgOK();

  strSql = "insert into MKT_BN_DATA_T "
         + "select * "
         + "from MKT_BN_DATA "
         + "where table_name  =  'MKT_BPMH3' "
         + "and   data_key = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr2("active_code"),
     };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , true);


   return 1;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " +controlTabName + " set "
         + "apr_flag = ?, "
         + "active_name = ?, "
         + "bonus_type = ?, "
         + "tax_flag = ?, "
         + "active_date_s = ?, "
         + "active_date_e = ?, "
         + "effect_months = ?, "
         + "stop_flag = ?, "
         + "stop_date = ?, "
         + "stop_desc = ?, "
         + "run_start_cond = ?, "
         + "run_start_month = ?, "
         + "run_time_mm = ?, "
         + "run_time_type = ?, "
         + "run_time_dd = ?, "
         + "per_point_amt = ?, "
         + "feedback_lmt = ?, "
         + "list_cond = ?, "
         + "vd_flag = ?, "
         + "acct_type_sel = ?, "
         + "vd_corp_flag = ?, "
         + "issue_cond = ?, "
         + "issue_date_s = ?, "
         + "issue_date_e = ?, "
         + "card_re_days = ?, "
         + "purch_cond = ?, "
         + "purch_s_date = ?, "
         + "purch_e_date = ?, "
         + "group_card_sel = ?, "
//         + "group_oppost_cond = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "platform_kind_sel = ?, "
         + "mcc_code_sel = ?, "
         + "bl_cond = ?, "
         + "ca_cond = ?, "
         + "it_cond = ?, "
         + "it_flag = ?, "
         + "id_cond = ?, "
         + "ao_cond = ?, "
         + "ot_cond = ?, "
         + "bill_type_sel = ?, "
         + "currency_sel = ?, "
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
         + "d_add_item_flag = ?, "
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
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("apr_flag"),
     wp.itemStr2("active_name"),
     wp.itemStr2("bonus_type"),
     wp.itemStr2("tax_flag"),
     wp.itemStr2("active_date_s"),
     wp.itemStr2("active_date_e"),
     wp.itemNum("effect_months"),
     wp.itemStr2("stop_flag"),
     wp.itemStr2("stop_date"),
     wp.itemStr2("stop_desc"),
     wp.itemStr2("run_start_cond"),
     wp.itemStr2("run_start_month"),
     wp.itemNum("run_time_mm"),
     wp.itemStr2("run_time_type"),
     wp.itemNum("run_time_dd"),
     wp.itemNum("per_point_amt"),
     wp.itemNum("feedback_lmt"),
     wp.itemStr2("list_cond"),
     wp.itemStr2("vd_flag"),
     wp.itemStr2("acct_type_sel"),
     wp.itemStr2("vd_corp_flag"),
     wp.itemStr2("issue_cond"),
     wp.itemStr2("issue_date_s"),
     wp.itemStr2("issue_date_e"),
     wp.itemNum("card_re_days"),
     wp.itemStr2("purch_cond"),
     wp.itemStr2("purch_s_date"),
     wp.itemStr2("purch_e_date"),
     wp.itemStr2("group_card_sel"),
//     wp.itemStr2("group_oppost_cond"),
     wp.itemStr2("merchant_sel"),
     wp.itemStr2("mcht_group_sel"),
     wp.itemStr2("platform_kind_sel"),
     wp.itemStr2("mcc_code_sel"),
     wp.itemStr2("bl_cond"),
     wp.itemStr2("ca_cond"),
     wp.itemStr2("it_cond"),
     wp.itemStr2("it_flag"),
     wp.itemStr2("id_cond"),
     wp.itemStr2("ao_cond"),
     wp.itemStr2("ot_cond"),
     wp.itemStr2("bill_type_sel"),
     wp.itemStr2("currency_sel"),
     wp.itemStr2("add_type"),
     wp.itemStr2("add_item_flag"),
     wp.itemNum("add_item_amt"),
     wp.itemNum("add_amt_s1"),
     wp.itemNum("add_amt_e1"),
     wp.itemNum("add_times1"),
     wp.itemNum("add_point1"),
     wp.itemNum("add_amt_s2"),
     wp.itemNum("add_amt_e2"),
     wp.itemNum("add_times2"),
     wp.itemNum("add_point2"),
     wp.itemNum("add_amt_s3"),
     wp.itemNum("add_amt_e3"),
     wp.itemNum("add_times3"),
     wp.itemNum("add_point3"),
     wp.itemNum("add_amt_s4"),
     wp.itemNum("add_amt_e4"),
     wp.itemNum("add_times4"),
     wp.itemNum("add_point4"),
     wp.itemNum("add_amt_s5"),
     wp.itemNum("add_amt_e5"),
     wp.itemNum("add_times5"),
     wp.itemNum("add_point5"),
     wp.itemNum("add_amt_s6"),
     wp.itemNum("add_amt_e6"),
     wp.itemNum("add_times6"),
     wp.itemNum("add_point6"),
     wp.itemNum("add_amt_s7"),
     wp.itemNum("add_amt_e7"),
     wp.itemNum("add_times7"),
     wp.itemNum("add_point7"),
     wp.itemNum("add_amt_s8"),
     wp.itemNum("add_amt_e8"),
     wp.itemNum("add_times8"),
     wp.itemNum("add_point8"),
     wp.itemNum("add_amt_s9"),
     wp.itemNum("add_amt_e9"),
     wp.itemNum("add_times9"),
     wp.itemNum("add_point9"),
     wp.itemNum("add_amt_s10"),
     wp.itemNum("add_amt_e10"),
     wp.itemNum("add_times10"),
     wp.itemNum("add_point10"),
     wp.itemStr2("doorsill_flag"),
     wp.itemStr2("d_group_card_sel"),
     wp.itemStr2("d_merchant_sel"),
     wp.itemStr2("d_mcht_group_sel"),
     wp.itemStr2("platform2_kind_sel"),
     wp.itemStr2("d_mcc_code_sel"),
     wp.itemStr2("d_card_type_sel"),
     wp.itemStr2("d_bl_cond"),
     wp.itemStr2("d_ca_cond"),
     wp.itemStr2("d_it_cond"),
     wp.itemStr2("d_it_flag"),
     wp.itemStr2("d_id_cond"),
     wp.itemStr2("d_ao_cond"),
     wp.itemStr2("d_ot_cond"),
     wp.itemStr2("d_bill_type_sel"),
     wp.itemStr2("d_currency_sel"),
     wp.itemStr2("d_pos_entry_sel"),
     wp.itemStr2("d_ucaf_sel"),
     wp.itemStr2("d_eci_sel"),
     wp.itemStr2("d_add_item_flag"),
     wp.itemNum("d_add_amt_s1"),
     wp.itemNum("d_add_amt_e1"),
     wp.itemNum("d_add_point1"),
     wp.itemNum("d_add_amt_s2"),
     wp.itemNum("d_add_amt_e2"),
     wp.itemNum("d_add_point2"),
     wp.itemNum("d_add_amt_s3"),
     wp.itemNum("d_add_amt_e3"),
     wp.itemNum("d_add_point3"),
     wp.itemNum("d_add_amt_s4"),
     wp.itemNum("d_add_amt_e4"),
     wp.itemNum("d_add_point4"),
     wp.itemNum("d_add_amt_s5"),
     wp.itemNum("d_add_amt_e5"),
     wp.itemNum("d_add_point5"),
     wp.itemNum("d_add_amt_s6"),
     wp.itemNum("d_add_amt_e6"),
     wp.itemNum("d_add_point6"),
     wp.itemNum("d_add_amt_s7"),
     wp.itemNum("d_add_amt_e7"),
     wp.itemNum("d_add_point7"),
     wp.itemNum("d_add_amt_s8"),
     wp.itemNum("d_add_amt_e8"),
     wp.itemNum("d_add_point8"),
     wp.itemNum("d_add_amt_s9"),
     wp.itemNum("d_add_amt_e9"),
     wp.itemNum("d_add_point9"),
     wp.itemNum("d_add_amt_s10"),
     wp.itemNum("d_add_amt_e10"),
     wp.itemNum("d_add_point10"),
     wp.loginUser,
     wp.loginUser,
     wp.itemStr2("mod_pgm"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

  if (sqlRowNum <= 0) rc=0;else rc=1;
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  dbInsertD4T();
  dbInsertD2T();

  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
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
 public int dbInsertD4T()
 {
   msgOK();

   strSql = "delete MKT_BPMH3_LIST_T "
          + "WHERE active_code = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr2("active_code"),
     };

   sqlExec(strSql,param,true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 MKT_BPMH3_LIST_T 錯誤");

   return rc;

 }
// ************************************************************************
 public int dbInsertD2T() 
 {
   msgOK();

   strSql = "delete MKT_BN_DATA_T "
         + " where table_name  =  'MKT_BPMH3' "
          + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr2("active_code"),
     };

   sqlExec(strSql,param,true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 MKT_BN_DATA_T 錯誤");

   return rc;

 }
// ************************************************************************
 public int checkDecnum(String decStr,int colLength,int colScale)
 {
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
 public int dbInsertI4() throws Exception
 {
   msgOK();

  strSql = "insert into MKT_BPMH3_LIST_T ( "
          + "active_code,"
          + " mod_time, "
          + " mod_pgm "
          + ") values ("
          + "?," 
          + " sysdate, "
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr2("active_code"),
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_BPMH3_LIST_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD4() throws Exception
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr2("active_code")
     };
   if (sqlRowcount("MKT_BPMH3_LIST_T" 
                   , "where active_code = ? "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BPMH3_LIST_T "
          + "where active_code = ?  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0360_acty"))
      dataType = "1" ;
   if (wp.respHtml.equals("mktm0360_aaa1"))
      dataType = "7" ;
   if (wp.respHtml.equals("mktm0360_pmkd"))
      dataType = "P" ;
   if (wp.respHtml.equals("mktm0360_mccd"))
      dataType = "4" ;
   if (wp.respHtml.equals("mktm0360_acsr"))
      dataType = "5" ;
   if (wp.respHtml.equals("mktm0360_aaa2"))
      dataType = "G" ;
   if (wp.respHtml.equals("mktm0360_pmkd1"))
      dataType = "P2" ;
   if (wp.respHtml.equals("mktm0360_dccd"))
      dataType = "C" ;
   if (wp.respHtml.equals("mktm0360_dype"))
      dataType = "F" ;
   if (wp.respHtml.equals("mktm0360_desr"))
      dataType = "D" ;
   if (wp.respHtml.equals("mktm0360_pose"))
      dataType = "H" ;
   if (wp.respHtml.equals("mktm0360_ucaf"))
      dataType = "I" ;
   if (wp.respHtml.equals("mktm0360_deci"))
      dataType = "J" ;
  strSql = "insert into MKT_BN_DATA_T ( "
          + "table_name, "
          + "data_type, "
          + "data_key,"
          + "data_code,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'MKT_BPMH3', "
          + "?, "
          + "?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      dataType, 
      wp.itemStr2("active_code"),
      varsStr("data_code"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0360_acty"))
      dataType = "1" ;
   if (wp.respHtml.equals("mktm0360_aaa1"))
      dataType = "7" ;
   if (wp.respHtml.equals("mktm0360_pmkd"))
      dataType = "P" ;
   if (wp.respHtml.equals("mktm0360_mccd"))
      dataType = "4" ;
   if (wp.respHtml.equals("mktm0360_acsr"))
      dataType = "5" ;
   if (wp.respHtml.equals("mktm0360_aaa2"))
      dataType = "G" ;
   if (wp.respHtml.equals("mktm0360_pmkd1"))
      dataType = "P2" ;
   if (wp.respHtml.equals("mktm0360_dccd"))
      dataType = "C" ;
   if (wp.respHtml.equals("mktm0360_dype"))
      dataType = "F" ;
   if (wp.respHtml.equals("mktm0360_desr"))
      dataType = "D" ;
   if (wp.respHtml.equals("mktm0360_pose"))
      dataType = "H" ;
   if (wp.respHtml.equals("mktm0360_ucaf"))
      dataType = "I" ;
   if (wp.respHtml.equals("mktm0360_deci"))
      dataType = "J" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("active_code")
     };
   if (sqlRowcount("MKT_BN_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_BPMH3' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BN_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_BPMH3'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI3() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0360_gpcd"))
      dataType = "2" ;
   if (wp.respHtml.equals("mktm0360_mrch"))
      dataType = "3" ;
   if (wp.respHtml.equals("mktm0360_cocq"))
      dataType = "6" ;
   if (wp.respHtml.equals("mktm0360_dpcd"))
      dataType = "A" ;
   if (wp.respHtml.equals("mktm0360_drch"))
      dataType = "B" ;
   if (wp.respHtml.equals("mktm0360_docq"))
      dataType = "E" ;
  strSql = "insert into MKT_BN_DATA_T ( "
          + "table_name, "
          + "data_type, "
          + "data_key,"
          + "data_code,"
          + "data_code2,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'MKT_BPMH3', "
          + "?, "
          + "?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      dataType, 
      wp.itemStr2("active_code"),
      varsStr("data_code"),
      varsStr("data_code2"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD3() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0360_gpcd"))
      dataType = "2" ;
   if (wp.respHtml.equals("mktm0360_mrch"))
      dataType = "3" ;
   if (wp.respHtml.equals("mktm0360_cocq"))
      dataType = "6" ;
   if (wp.respHtml.equals("mktm0360_dpcd"))
      dataType = "A" ;
   if (wp.respHtml.equals("mktm0360_drch"))
      dataType = "B" ;
   if (wp.respHtml.equals("mktm0360_docq"))
      dataType = "E" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("active_code")
     };
   if (sqlRowcount("MKT_BN_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_BPMH3' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BN_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_BPMH3'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI2List(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbDeleteD2List(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where active_code = ? "
         ;

  Object[] param =new Object[]
    {
     wp.itemStr2("ACTIVE_CODE")
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertI2Aaa1(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.loginUser;
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
 public int dbInsertI2Pmkd(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.loginUser;
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbDeleteD2Aaa1(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "MKT_BPMH3",
      wp.itemStr2("active_code"),
     "3"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
 public int dbDeleteD2Pmkd(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" "
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "MKT_BPMH3",
      wp.itemStr2("active_code"),
     "3"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertI2Aaa2(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.loginUser;
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbInsertI2Pmkd1(String tableName,String[] columnCol,String[] columnDat) throws Exception
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) continue;
     stra = columnDat[inti];
     param1[skipLine]= stra ;
     skipLine++;
    }
  param1[skipLine++]= wp.loginUser;
  param1[skipLine++]= wp.sysDate + wp.sysTime;
  param1[skipLine++]= wp.modPgm();
  Object[] param = Arrays.copyOf(param1,skipLine);
  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbDeleteD2Aaa2(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "MKT_BPMH3",
      wp.itemStr2("active_code"),
     "B"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbDeleteD2Pmkd1(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" "
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "MKT_BPMH3",
      wp.itemStr2("active_code"),
     "B"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsMediaErrlog(String tranSeqStr,String[] errMsg ) throws Exception
 {
  dateTime();
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  comr.setConn(wp);

  if (!comm.isNumber(errMsg[10])) errMsg[10]="0";
  if (!comm.isNumber(errMsg[1])) errMsg[1]="0";
  if (!comm.isNumber(errMsg[2])) errMsg[2]="0";

  strSql= " insert into ecs_media_errlog ("
          + " crt_date, "
          + " crt_time, "
          + " file_name, "
          + " unit_code, "
          + " main_desc, "
          + " error_seq, "
          + " error_desc, "
          + " line_seq, "
          + " column_seq, "
          + " column_data, "
          + " trans_seqno, "
          + " column_desc, "
          + " program_code, "
          + " mod_time, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?," // 10 record
          + "?,?,?,"               // 4 trvotfd
          + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param =new Object[]
       {
        wp.sysDate,
        wp.sysTime,
        wp.itemStr2("zz_file_name"),
        comr.getObjectOwner("3",wp.modPgm()),
        errMsg[0],
        Integer.valueOf(errMsg[1]),
        errMsg[4],
        Integer.valueOf(errMsg[10]),
        Integer.valueOf(errMsg[2]),
        errMsg[3],
        tranSeqStr,
        errMsg[5],
        wp.modPgm(),
        wp.sysDate + wp.sysTime,
        wp.modPgm()
       };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) errmsg("新增4 ecs_media_errlog 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsNotifyLog(String tranSeqStr,int errorCnt ) throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  dateTime();
  strSql= " insert into ecs_notify_log ("
          + " crt_date, "
          + " crt_time, "
          + " unit_code, "
          + " obj_type, "
          + " notify_head, "
          + " notify_name, "
          + " notify_desc1, "
          + " notify_desc2, "
          + " trans_seqno, "
          + " mod_time, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?," // 9 record
          + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param =new Object[]
       {
        wp.sysDate,
        wp.sysTime,
        comr.getObjectOwner("3",wp.modPgm()),
        "3",
        "媒體檔轉入資料有誤(只記錄前100筆)",
        "媒體檔名:"+wp.itemStr2("zz_file_name"),
        "程式 "+wp.modPgm()+" 轉 "+wp.itemStr2("zz_file_name")+" 有"+errorCnt+" 筆錯誤",
        "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤",
        tranSeqStr,
        wp.sysDate + wp.sysTime,
        wp.modPgm()
       };

  wp.dupRecord = "Y";
  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) errmsg("新增5 ecs_modify_log 錯誤");
  return rc;
 }
// ************************************************************************
 int listParmDataCnt(String s1,String s2,String s3,String s4) 
 {
  String isSql = "select count(*) as data_cnt "
                + "from  " + s1 +" "
                + " where table_name = ? "
                + " and   data_key   = ? "
                + " and   data_type  = ? "
                ;
  Object[] param = new Object[] {s2,s3,s4};
  sqlSelect(isSql,param);

 return(Integer.parseInt(colStr("data_cnt")));
 }
// ************************************************************************
 int listBpmh3DataCnt(String s1,String s2,String s3,String s4) 
 {
  String isSql = "select count(*) as data_cnt "
                + "from  " + s1 +" "
                + " where  active_code = '" + s3 + "' "
                ;
  Object[] param = new Object[] {}; // new Object[] {s2,s3,s4};
  sqlSelect(isSql,param);

 return(Integer.parseInt(colStr("data_cnt")));
 }

// ************************************************************************

}  // End of class
