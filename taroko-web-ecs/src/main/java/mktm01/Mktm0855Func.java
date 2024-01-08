/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/01/06  V1.01.01   Allen Ho      Initial                              *
* 112/02/08  V1.00.02   Zuwei Su      naming rule update                   *
*                                                                          *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0855Func extends FuncEdit
{
 private final String PROGNAME = "行銷通路活動登錄參數維護處理程式112/02/08 V1.00.02";
  String kk1,kk2;
  String orgControlTabName = "mkt_chanrec_parm";
  String controlTabName = "mkt_chanrec_parm_t";

 public Mktm0855Func(TarokoCommon wr)
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
  procTabName = wp.itemStr("control_tab_name");
  if (procTabName.length()==0) return(1);
  strSql= " select "
          + " apr_flag, "
          + " record_group_no, "
          + " record_date_sel, "
          + " pur_date_sel, "
          + " purchase_date_s, "
          + " purchase_date_e, "
          + " week_cond, "
          + " month_cond, "
          + " cap_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " purchase_type_sel, "
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
   if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

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
      kk1 = wp.itemStr("active_code");
      if (empty(kk1))
         {
          errmsg("活動代碼 不可空白");
          return;
         }
      kk2 = wp.itemStr("active_seq");
      if (empty(kk2))
         {
          errmsg("活動順序 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr("active_code");
      kk2 = wp.itemStr("active_seq");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where active_code = ? "
             +"and   active_seq = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代碼][活動順序] 不可重複("+orgControlTabName+"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where active_code = ? "
             + " and   active_seq = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代碼][活動順序] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }

  if (!wp.itemStr("week_cond").equals("Y")) wp.itemSet("week_cond","N");
  if (!wp.itemStr("month_cond").equals("Y")) wp.itemSet("month_cond","N");
  if (!wp.itemStr("bl_cond").equals("Y")) wp.itemSet("bl_cond","N");
  if (!wp.itemStr("ca_cond").equals("Y")) wp.itemSet("ca_cond","N");
  if (!wp.itemStr("it_cond").equals("Y")) wp.itemSet("it_cond","N");
  if (!wp.itemStr("id_cond").equals("Y")) wp.itemSet("id_cond","N");
  if (!wp.itemStr("ao_cond").equals("Y")) wp.itemSet("ao_cond","N");
  if (!wp.itemStr("ot_cond").equals("Y")) wp.itemSet("ot_cond","N");
  if (!wp.itemStr("per_amt_cond").equals("Y")) wp.itemSet("per_amt_cond","N");
  if (!wp.itemStr("perday_cnt_cond").equals("Y")) wp.itemSet("perday_cnt_cond","N");
  if (!wp.itemStr("sum_amt_cond").equals("Y")) wp.itemSet("sum_amt_cond","N");
  if (!wp.itemStr("sum_cnt_cond").equals("Y")) wp.itemSet("sum_cnt_cond","N");
  if (!wp.itemStr("above_cond").equals("Y")) wp.itemSet("above_cond","N");
  if (!wp.itemStr("max_cnt_cond").equals("Y")) wp.itemSet("max_cnt_cond","N");

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

   if (this.ibUpdate)
      {
       if (wp.itemStr("week_cond").equals("Y"))
          {
           wp.log("STEP 1 ["+wp.colStr("active_code")+wp.colStr("active_seq") +"]");
           if (listParmDataCnt("mkt_bn_data_t"
                                 ,"MKT_CHANREC_PARM"
                                 ,wp.colStr("active_code")+wp.colStr("active_seq")
                                 ,"5")==0)
              {
               errmsg("[每週指定日] 明細沒有設定, 筆數不可為 0  !");
               return;
              }
          }
       if (wp.itemStr("month_cond").equals("Y"))
          {
           if (listParmDataCnt("mkt_bn_data_t"
                                 ,"MKT_CHANREC_PARM"
                                 ,wp.colStr("active_code")+ wp.colStr("active_seq")
                                 ,"6")==0)
              {
               errmsg("[每月指定日] 明細沒有設定, 筆數不可為 0  !");
               return;
              }
          }
      }

  Object[] param = null;

  if (this.ibAdd)
     {
      if (wp.itemStr("active_seq").length()!=2)
         {
          errmsg("["+colStr("active_code")+"]登錄順序需為2碼資料 !");
          return;
         }
      if (wp.itemStr("active_seq").equals("00"))
         {
          errmsg("["+colStr("active_code")+"]登錄順序00為保留順序 !");
          return;
         }

      if (!wp.itemStr("control_tab_name").equals(orgControlTabName))
         {
          strSql = "select "
                 + " active_seq "
                 + " from  mkt_chanrec_parm "
                 + " where active_code = ? "
                 + " and   active_seq = ? "
                 +  "union "
                 +  "select "
                 + " active_seq "
                 + " from  mkt_chanrec_parm_t "
                 + " where active_code = ? "
                 + " and   active_seq = ? "
                 ;
          param = new Object[] {wp.itemStr("active_code"),
                                wp.itemStr("active_seq"),
                                wp.itemStr("active_code"),
                                wp.itemStr("active_seq")
                               };
          sqlSelect(strSql,param);

          if (sqlRowNum > 0)
             {
              errmsg("[活動順序]"+wp.itemStr("active_seq")+" 不可重複 !");
              return;
             }
         }
     }

  if (this.ibUpdate)
     {
      if (wp.itemStr("week_cond").equals("Y"))
         {
          strSql = "select "
                 + " data_code "
                 + " from  mkt_bn_data_t "
                 + " where table_name = 'MKT_CHANREC_PARM' "
                 + " and   data_key   = ? "
                 + " and   data_type   = '5' "
                 ;
          param = new Object[] {wp.itemStr("active_code")
                               +wp.itemStr("active_seq")};
          sqlSelect(strSql,param);
      
          if (sqlRowNum <= 0)
             {
              errmsg("[依特定期間:每週指定日] 必須輸入資料 !");
              return;
             }
         }

      if (wp.itemStr("month_cond").equals("Y"))
         {
          strSql = "select "
                 + " data_code "
                 + " from  mkt_bn_data_t "
                 + " where table_name = 'MKT_CHANREC_PARM' "
                 + " and   data_key   = ? "
                 + " and   data_type   = '6' "
                 ;
          param = new Object[] {wp.itemStr("active_code")
                               +wp.itemStr("active_seq")};
          sqlSelect(strSql,param);
         
          if (sqlRowNum <= 0)
             {
              errmsg("[依特定期間:每月指定日] 必須輸入資料 !");
              return;
             }
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
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

      strSql = "select "
                 + " active_seq "
                 + " from  mkt_chanrec_parm "
                 + " where active_code = ? "
                 + " and   record_group_no = ? "
                 + " and   active_seq != ? "
                 +  "union "
                 +  "select "
                 + " active_seq "
                 + " from  mkt_chanrec_parm_t "
                 + " where active_code = ? "
                 + " and   record_group_no = ? "
                 + " and   active_seq != ? "
                 ;
          param = new Object[] {wp.itemStr("active_code"),
                                wp.itemStr("record_group_no"),
                                wp.itemStr("active_seq"),
                                wp.itemStr("active_code"),
                                wp.itemStr("record_group_no"),
                                wp.itemStr("active_seq")
                               };
      sqlSelect(strSql,param);

      if (sqlRowNum > 0)
         {
          errmsg("[同一活動代號 登錄:群組]"+wp.itemStr("record_group_no")+" 不可重複 !");
          return;
         }

      strSql = "select "
             + " record_cond,"
             + " bonus_type_cond,"
             + " fund_code_cond,"
             + " other_type_cond,"
             + " bl_cond,"
             + " ca_cond,"
             + " it_cond,"
             + " ao_cond,"
             + " ot_cond,"
             + " id_cond,"
             + " it_flag,"
             + " purchase_date_s,"
             + " purchase_date_e  "
             + " from  mkt_channel_parm "
             + " where active_code = ? "
             ;

      param = new Object[] {wp.itemStr("active_code")};
      sqlSelect(strSql,param);

      if (sqlRowNum <= 0)
         {
          errmsg(" 此活動代號[ "+wp.itemStr("active_code").toUpperCase() +"] 不存在");
          return;
         }
      if (wp.itemStr("cap_sel").equals("2"))
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

          if ((wp.itemStr("bl_cond").equals("Y"))&&
              (!colStr("bl_cond").equals("Y")))
             {
              errmsg("原消費本金類未勾選 簽帳款(BL)");
              return;
             }
          if ((wp.itemStr("ca_cond").equals("Y"))&&
              (!colStr("ca_cond").equals("Y")))
             {
              errmsg("原消費本金類未勾選 預借現金(CA)");
              return;
             }
          if ((wp.itemStr("it_cond").equals("Y"))&&
              (!colStr("it_cond").equals("Y")))
             {
              errmsg("原消費本金類未勾選 分期付款(IT)");
              return;
             }
          if ((wp.itemStr("id_cond").equals("Y"))&&
              (!colStr("id_cond").equals("Y")))
             {
              errmsg("原消費本金類未勾選 代收款(ID)");
              return;
             }
          if ((wp.itemStr("ao_cond").equals("Y"))&&
              (!colStr("ao_cond").equals("Y")))
             {
              errmsg("原消費本金類未勾選 餘額代償(AO)");
              return;
             }
          if ((wp.itemStr("ot_cond").equals("Y"))&&
              (!colStr("ot_cond").equals("Y")))
             {
              errmsg("原消費本金類未勾選 其他應收款(OT)");
              return;
             }
         }

      if (!colStr("record_cond").equals("Y"))
         {
          errmsg(" 此活動代號[ "+wp.itemStr("active_code").toUpperCase() +"] 非設定登錄判斷");
          return;
         }

      if (wp.itemStr("per_amt_cond").equals("Y"))
         {
          if (wp.itemStr("per_amt").length()==0)  wp.itemSet("per_amt","0");
          if (wp.itemNum("per_amt")==0)  
             {
              errmsg("[單筆最低消費金額] 不可為 0 !");
              return;
             }
         }

      if (wp.itemStr("sum_amt_cond").equals("Y"))
         {
          if (wp.itemStr("sum_amt").length()==0)  wp.itemSet("sum_amt","0");
          if (wp.itemNum("sum_amt")==0)  
             {
              errmsg("[累積最低消費金額] 不可為 0 !");
              return;
             }
         }

      if (wp.itemStr("sum_cnt_cond").equals("Y"))
         {
          if (wp.itemStr("sum_cnt").length()==0)  wp.itemSet("sum_cnt","0");
          if (wp.itemNum("sum_cnt")==0)  
             {
              errmsg("[累積最低消費筆數] 不可為 0 !");
              return;
             }
         }

      if (wp.itemStr("pur_date_sel").equals("2"))
         {
          if ((wp.itemStr("purchase_date_s").length()==0)||
              (wp.itemStr("purchase_date_e").length()==0))
             {
              errmsg("[消費期間:一段期間] 消費期間起迄日必須輸入 !");
              return;
             }

          if ((wp.itemStr("purchase_date_s").compareTo(colStr("purchase_date_s"))<0)||
              (wp.itemStr("purchase_date_e").compareTo(colStr("purchase_date_s"))<0))
             {
              errmsg("[消費期間:一段期間] 消費期間起必須>=原消費期間起 !");
              return;
             }
          if ((wp.itemStr("purchase_date_e").compareTo(colStr("purchase_date_e"))>0)||
              (wp.itemStr("purchase_date_s").compareTo(colStr("purchase_date_e"))>0))
             {
              errmsg("[消費期間:一段期間] 消費期間迄必須<=原消費期間迄 !");
              return;
             }
         }
      if (wp.itemStr("purchase_amt_s1").length()==0)  wp.itemSet("purchase_amt_s1","0");
      if (wp.itemStr("purchase_amt_e1").length()==0)  wp.itemSet("purchase_amt_e1","0");
      if ((wp.itemNum("purchase_amt_s1")==0)||
          (wp.itemNum("purchase_amt_e1")==0))
         {
          errmsg("[門檻一]必須輸入 !");
          return;
         }
      if ((wp.itemNum("purchase_amt_e1")!=0)&&
          (wp.itemStr("active_type_1").length()==0))
         {
          errmsg("[門檻一]:回饋類型] 參數未選取 !");
          return;
         }

      if (wp.itemStr("feedback_amt_1").length()==0)  wp.itemSet("feedback_amt_1","0");
      if (wp.itemStr("feedback_rate_1").length()==0)  wp.itemSet("feedback_rate_1","0");

      wp.log("20220106 ==> active_type["+wp.itemStr("active_type_1")+"]["+ colStr("bonus_type_cond")+"]["
            + colStr("fund_code_cond") +"]["+ colStr("other_type_cond")+"]");
      if (((wp.itemStr("active_type_1").equals("1"))&&
           (!colStr("bonus_type_cond").equals("Y")))||
          ((wp.itemStr("active_type_1").equals("2"))&&
           (!colStr("fund_code_cond").equals("Y")))||
          ((wp.itemStr("active_type_1").equals("3"))&&
           (!colStr("other_type_cond").equals("Y")))||
          ((wp.itemStr("active_type_1").equals("4"))&&
           (!colStr("lottery_cond").equals("Y"))))
         {
          errmsg("[門檻一:回饋類型] 參數未正確選取 !");
          return;
         }
      if (((wp.itemNum("feedback_amt_1")!=0)&&
           (wp.itemNum("feedback_rate_1")!=0))||
          ((wp.itemNum("feedback_amt_1")==0)&&
           (wp.itemNum("feedback_rate_1")==0)))
         {
          errmsg("[門檻一] 給倍數(%)與給點數/刷卡金/贈品數 只能(必須))一項有值 !");
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

          if (((wp.itemStr("active_type_2").equals("1"))&&
               (!colStr("bonus_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_2").equals("2"))&&
               (!colStr("fund_code_cond").equals("Y")))||
              ((wp.itemStr("active_type_2").equals("3"))&&
               (!colStr("other_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_2").equals("4"))&&
               (!colStr("lottery_cond").equals("Y"))))
             {
              errmsg("[門檻二:回饋類型] 參數未正確選取 !");
              return;
             }
          if (((wp.itemNum("feedback_amt_2")!=0)&&
               (wp.itemNum("feedback_rate_2")!=0))||
              ((wp.itemNum("feedback_amt_2")==0)&&
               (wp.itemNum("feedback_rate_2")==0)))
             {
              errmsg("[門檻二] 給倍數(%)與給點數/刷卡金/贈品數 只能(必須))一項有值 !");
              return;
             }
         }
      if (wp.itemStr("purchase_amt_s3").length()==0)  wp.itemSet("purchase_amt_s3","0");
      if (wp.itemNum("purchase_amt_s3")!=0)
         {
          if (wp.itemStr("feedback_amt_3").length()==0)  wp.itemSet("feedback_amt_3","0");
          if (wp.itemStr("feedback_rate_3").length()==0)  wp.itemSet("feedback_rate_3","0");

          if (((wp.itemStr("active_type_3").equals("1"))&&
               (!colStr("bonus_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_3").equals("2"))&&
               (!colStr("fund_code_cond").equals("Y")))||
              ((wp.itemStr("active_type_3").equals("3"))&&
               (!colStr("other_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_3").equals("4"))&&
               (!colStr("lottery_cond").equals("Y"))))
             {
              errmsg("[門檻三:回饋類型] 參數未正確選取 !");
              return;
             }
          if (((wp.itemNum("feedback_amt_3")!=0)&&
               (wp.itemNum("feedback_rate_3")!=0))||
              ((wp.itemNum("feedback_amt_3")==0)&&
               (wp.itemNum("feedback_rate_3")==0)))
             {
              errmsg("[門檻三] 給倍數(%)與給點數/刷卡金/贈品數 只能(必須))一項有值 !");
              return;
             }
         }
      if (wp.itemStr("purchase_amt_s4").length()==0)  wp.itemSet("purchase_amt_s4","0");
      if (wp.itemNum("purchase_amt_s4")!=0)
         {
          if (wp.itemStr("feedback_amt_4").length()==0)  wp.itemSet("feedback_amt_4","0");
          if (wp.itemStr("feedback_rate_4").length()==0)  wp.itemSet("feedback_rate_4","0");

          if (((wp.itemStr("active_type_4").equals("1"))&&
               (!colStr("bonus_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_4").equals("2"))&&
               (!colStr("fund_code_cond").equals("Y")))||
              ((wp.itemStr("active_type_4").equals("3"))&&
               (!colStr("other_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_4").equals("4"))&&
               (!colStr("lottery_cond").equals("Y"))))
             {
              errmsg("[門檻四:回饋類型] 參數未正確選取 !");
              return;
             }
          if (((wp.itemNum("feedback_amt_4")!=0)&&
               (wp.itemNum("feedback_rate_4")!=0))||
              ((wp.itemNum("feedback_amt_4")==0)&&
               (wp.itemNum("feedback_rate_4")==0)))
             {
              errmsg("[門檻四] 給倍數(%)與給點數/刷卡金/贈品數 只能(必須))一項有值 !");
              return;
             }
         }
      if (wp.itemStr("purchase_amt_s5").length()==0)  wp.itemSet("purchase_amt_s5","0");
      if (wp.itemNum("purchase_amt_s5")!=0)
         {
          if (wp.itemStr("feedback_amt_5").length()==0)  wp.itemSet("feedback_amt_5","0");
          if (wp.itemStr("feedback_rate_5").length()==0)  wp.itemSet("feedback_rate_5","0");

          if (((wp.itemStr("active_type_5").equals("1"))&&
               (!colStr("bonus_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_5").equals("2"))&&
               (!colStr("fund_code_cond").equals("Y")))||
              ((wp.itemStr("active_type_5").equals("3"))&&
               (!colStr("other_type_cond").equals("Y")))||
              ((wp.itemStr("active_type_5").equals("4"))&&
               (!colStr("lottery_cond").equals("Y"))))
             {
              errmsg("[門檻五:回饋類型] 參數未正確選取 !");
              return;
             }
          if (((wp.itemNum("feedback_amt_5")!=0)&&
               (wp.itemNum("feedback_rate_5")!=0))||
              ((wp.itemNum("feedback_amt_5")==0)&&
               (wp.itemNum("feedback_rate_5")==0)))
             {
              errmsg("[門檻五] 給倍數(%)與給點數/刷卡金/贈品數 只能(必須))一項有值 !");
              return;
             }
         }

     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("purchase_date_s")&&(!wp.itemEmpty("purchase_date_e")))
      if (wp.itemStr("purchase_date_s").compareTo(wp.itemStr("purchase_date_e"))>0)
         {
          errmsg("["+wp.itemStr("purchase_date_s")+"]>["+wp.itemStr("purchase_date_e")+"] 起迄值錯誤!");
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
// ************************************************************************
 @Override
 public int dbInsert()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;

  dbInsertD2T();
  dbInsertI2T();

  strSql= " insert into  " + controlTabName+ " ("
          + " active_code, "
          + " apr_flag, "
          + " aud_type, "
          + " active_seq, "
          + " record_group_no, "
          + " record_date_sel, "
          + " pur_date_sel, "
          + " purchase_date_s, "
          + " purchase_date_e, "
          + " week_cond, "
          + " month_cond, "
          + " cap_sel, "
          + " bl_cond, "
          + " ca_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " purchase_type_sel, "
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
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr("apr_flag"),
        wp.itemStr("aud_type"),
        kk2,
        wp.itemStr("record_group_no"),
        wp.itemStr("record_date_sel"),
        wp.itemStr("pur_date_sel"),
        wp.itemStr("purchase_date_s"),
        wp.itemStr("purchase_date_e"),
        wp.itemStr("week_cond"),
        wp.itemStr("month_cond"),
        wp.itemStr("cap_sel"),
        wp.itemStr("bl_cond"),
        wp.itemStr("ca_cond"),
        wp.itemStr("it_cond"),
        wp.itemStr("id_cond"),
        wp.itemStr("ao_cond"),
        wp.itemStr("ot_cond"),
        wp.itemStr("purchase_type_sel"),
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
 public int dbInsertI2T()
 {
   msgOK();

  strSql = "insert into MKT_BN_DATA_T "
         + "select * "
         + "from MKT_BN_DATA "
         + "where table_name  =  'MKT_CHANREC_PARM' "
         + "and   data_key = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr("active_code")+wp.itemStr("active_seq"), 
     };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , false);


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
         + "record_group_no = ?, "
         + "record_date_sel = ?, "
         + "pur_date_sel = ?, "
         + "purchase_date_s = ?, "
         + "purchase_date_e = ?, "
         + "week_cond = ?, "
         + "month_cond = ?, "
         + "cap_sel = ?, "
         + "bl_cond = ?, "
         + "ca_cond = ?, "
         + "it_cond = ?, "
         + "id_cond = ?, "
         + "ao_cond = ?, "
         + "ot_cond = ?, "
         + "purchase_type_sel = ?, "
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
     wp.itemStr("apr_flag"),
     wp.itemStr("record_group_no"),
     wp.itemStr("record_date_sel"),
     wp.itemStr("pur_date_sel"),
     wp.itemStr("purchase_date_s"),
     wp.itemStr("purchase_date_e"),
     wp.itemStr("week_cond"),
     wp.itemStr("month_cond"),
     wp.itemStr("cap_sel"),
     wp.itemStr("bl_cond"),
     wp.itemStr("ca_cond"),
     wp.itemStr("it_cond"),
     wp.itemStr("id_cond"),
     wp.itemStr("ao_cond"),
     wp.itemStr("ot_cond"),
     wp.itemStr("purchase_type_sel"),
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
     wp.loginUser,
     wp.loginUser,
     wp.itemStr("mod_pgm"),
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
 public int dbInsertD2T()
 {
   msgOK();

   strSql = "delete MKT_BN_DATA_T "
         + " where table_name  =  'MKT_CHANREC_PARM' "
         + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr("active_code")+wp.itemStr("active_seq"), 
     };

   sqlExec(strSql,param,false);
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
 public int dbInsertI2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0855_week"))
      dataType = "5" ;
   if (wp.respHtml.equals("mktm0855_mont"))
      dataType = "6" ;
  strSql = "insert into MKT_BN_DATA_T ( "
          + "table_name, "
          + "data_key, "
          + "data_type, "
          + "data_code,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'MKT_CHANREC_PARM', "
          + "?, "
          + "?, "
          + "?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr("active_code")+wp.itemStr("active_seq"), 
      dataType, 
      varsStr("data_code"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");
   else dbUpdateMainU2();

   return rc;
 }
// ************************************************************************
 public int dbUpdateMainU2() throws Exception
  {
   // TODO Auto-update main 
   return rc;
  }
// ************************************************************************
 public int dbDeleteD2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0855_week"))
      dataType = "5" ;
   if (wp.respHtml.equals("mktm0855_mont"))
      dataType = "6" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr("active_code")+wp.itemStr("active_seq"), 
      dataType  
     };
   if (sqlRowcount("MKT_BN_DATA_T" 
                     , "where data_key = ? "
                    + "and   data_type = ? "
                    + "and   table_name = 'MKT_CHANREC_PARM' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BN_DATA_T "
          + "where data_key = ? "
          + "and   data_type = ? "
          + "and   table_name = 'MKT_CHANREC_PARM'  "
          ;
   sqlExec(strSql,param,false);


   return 1;

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

 wp.log("STEP 2 [" +isSql +"][" + (int)colNum("data_cnt")+"][" +s2+"]["+s3+"]["+s4+"]");

 return((int)colNum("data_cnt"));
 }

// ************************************************************************

}  // End of class
