/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/09/29  V1.00.01   Allen Ho      Initial                              ** 111-12-07  V1.00.01 Yanghan sync from mega & updated for project coding standard *
* 111-12-07  V1.00.02   Yanghan sync from mega & updated for project coding standard *
* 111-12-13  V1.00.03   Zuwei         fix compile issue *
* 111/12/16  V1.00.04   Machao        命名规则调整后测试修改
* 111/12/22  V1.00.05   Zuwei         輸出sql log                                                                     *
* 112/03/30  V1.00.06   JiangYingdong        program update                *
* 112/04/28  V1.00.07   JiangYingdong        bug fix                       *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0180Func extends FuncEdit
{
 private final String PROGNAME = "紅利特惠(二)新發卡/介紹人加贈點數參數檔維護處理程式111-12-16 V1.00.04";
  String kk1,kk2;
  String orgControlTabName = "mkt_bpnw";
  String controlTabName = "mkt_bpnw_t";

 public Mktm0180Func(TarokoCommon wr)
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
          + " bonus_type, "
          + " tax_flag, "
          + " active_date_s, "
          + " active_date_e, "
          + " effect_months, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " apply_date_type, "
          + " apply_date_s, "
          + " apply_date_e, "
          + " acct_type_sel, "
          + " group_card_sel, "
          + " platform_kind_sel, "
          + " applicant_cond, "
          + " new_card_cond, "
          + " major_cond, "
          + " major_point, "
          + " sub_cond, "
          + " sub_point, "
          + " app_purch_cond, "
          + " app_months, "
          + " add_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " add_times, "
          + " add_point, "
          + " purch_reclow_cond, "
          + " purch_reclow_amt, "
          + " purch_tol_amt_cond, "
          + " purch_tol_amt, "
          + " purch_tol_time_cond, "
          + " purch_tol_time, "
          + " feedback_lmt, "
          + " limit_1_beg, "
          + " limit_1_end, "
          + " exchange_1, "
          + " limit_2_beg, "
          + " limit_2_end, "
          + " exchange_2, "
          + " limit_3_beg, "
          + " limit_3_end, "
          + " exchange_3, "
          + " limit_4_beg, "
          + " limit_4_end, "
          + " exchange_4, "
          + " limit_5_beg, "
          + " limit_5_end, "
          + " exchange_5, "
          + " limit_6_beg, "
          + " limit_6_end, "
          + " exchange_6, "
          + " introducer_cond, "
//          + " new_card_cond1, "
          + " intro_point, "
          + " intro_purch_cond, "
          + " intro_months, "
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
          errmsg("活動代號 不可空白");
          return;
         }
      kk2 = wp.itemStr("active_name");
      if (empty(kk2))
         {
          errmsg("活動說明 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr("active_code");
      kk2 = wp.itemStr("active_name");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where active_code = ? "
             +"and   active_name = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代號][活動說明] 不可重複("+ orgControlTabName +"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where active_code = ? "
             + " and   active_name = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代號][活動說明] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
         }
     }
    try {
      if (this.ibUpdate)
         {
          if ((wp.itemStr("acct_type_sel").equals("1"))||
              (wp.itemStr("acct_type_sel").equals("2")))
             {
                     if (listParmDataCnt("mkt_bn_data_t"
                                           ,"MKT_BPNW"
                                           ,wp.colStr("active_code")
                                           ,"1")==0)
                        {
                         errmsg("[B.帳戶類別] 明細沒有設定, 筆數不可為 0  !");
                         return;
                        }
             }
          if ((wp.itemStr("group_card_sel").equals("1"))||
              (wp.itemStr("group_card_sel").equals("2")))
             {
              if (listParmDataCnt("mkt_bn_data_t"
                                    ,"MKT_BPNW"
                                    ,wp.colStr("active_code")
                                    ,"7")==0)
                 {
                  errmsg("[C.團代卡種] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
             }
          if ((wp.itemStr("platform_kind_sel").equals("1"))||
              (wp.itemStr("platform_kind_sel").equals("2")))
             {
              if (listParmDataCnt("mkt_bn_data_t"
                                    ,"MKT_BPNW"
                                    ,wp.colStr("active_code")
                                    ,"P")==0)
                 {
                  errmsg("[D.一般消費群組] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
             }
          if ((wp.itemStr("merchant_sel").equals("1"))||
              (wp.itemStr("merchant_sel").equals("2")))
             {
              if (listParmDataCnt("mkt_bn_data_t"
                                    ,"MKT_BPNW"
                                    ,wp.colStr("active_code")
                                    ,"6")==0)
                 {
                  errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
             }
          if ((wp.itemStr("mcht_group_sel").equals("1"))||
              (wp.itemStr("mcht_group_sel").equals("2")))
             {
              if (listParmDataCnt("mkt_bn_data_t"
                                    ,"MKT_BPNW"
                                    ,wp.colStr("active_code")
                                    ,"4")==0)
                 {
                  errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
             }
         }
     } catch (Exception e) {
        e.printStackTrace();
    }
  if (!wp.itemStr("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
  if (!wp.itemStr("applicant_cond").equals("Y")) wp.itemSet("applicant_cond","N");
  if (!wp.itemStr("new_card_cond").equals("Y")) wp.itemSet("new_card_cond","N");
  if (!wp.itemStr("major_cond").equals("Y")) wp.itemSet("major_cond","N");
  if (!wp.itemStr("sub_cond").equals("Y")) wp.itemSet("sub_cond","N");
  if (!wp.itemStr("app_purch_cond").equals("Y")) wp.itemSet("app_purch_cond","N");
  if (!wp.itemStr("purch_reclow_cond").equals("Y")) wp.itemSet("purch_reclow_cond","N");
  if (!wp.itemStr("purch_tol_amt_cond").equals("Y")) wp.itemSet("purch_tol_amt_cond","N");
  if (!wp.itemStr("purch_tol_time_cond").equals("Y")) wp.itemSet("purch_tol_time_cond","N");
  if (!wp.itemStr("introducer_cond").equals("Y")) wp.itemSet("introducer_cond","N");
//  if (!wp.itemStr("new_card_cond1").equals("Y")) wp.itemSet("new_card_cond1","N");
  if (!wp.itemStr("intro_purch_cond").equals("Y")) wp.itemSet("intro_purch_cond","N");

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

   if (wp.itemEmpty("apr_flag")) {
       wp.colSet("apr_flag", "N");
       wp.itemSet("apr_flag", "N");
   }
   if ((this.ibDelete)||
       (wp.itemStr("aud_type").equals("D"))) return;

   if (wp.itemStr("effect_months").length()==0) wp.itemSet("effect_months","0");
   if ((wp.itemStr("tax_flag").equals("Y"))&&
       (wp.itemNum("effect_months")!=0))
      {
       errmsg("應稅紅利不可有效期["+(int)wp.itemNum("effect_months")+"]!");
       return;
      }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if ((!wp.itemStr("applicant_cond").equals("Y"))&&
          (!wp.itemStr("introducer_cond").equals("Y"))) 
         {
          errmsg("贈送申請人與贈送介紹人 至少要選一個 !");
          return;
         }
      if (wp.itemStr("introducer_cond").equals("Y"))
         {
          if (wp.itemNum("intro_point")==0)
             {
              errmsg("介紹人贈送點數 不可為 0  !");
              return;
             }
          if ((wp.itemStr("intro_purch_cond").equals("Y"))&&
              (wp.itemNum("intro_months")==0))
             {
              errmsg("介紹人有刷卡才回饋期數 不可為 0  !");
              return;
             }
         }
      if (wp.itemStr("applicant_cond").equals("Y")) 
         {
          if ((!wp.itemStr("major_cond").equals("Y"))&&
              (!wp.itemStr("sub_cond").equals("Y"))) 
             {
              errmsg("正卡申請人與附卡申請人贈送點數 至少要選一個 !");
              return;
             }
          if ((wp.itemStr("major_cond").equals("Y"))&&
              (wp.itemNum("major_point")==0))
             {
              errmsg("正卡申請人贈送點數 不可為 0  !");
              return;
             }
          if ((wp.itemStr("sub_cond").equals("Y"))&&
              (wp.itemNum("sub_point")==0))
             {
              errmsg("附卡申請人贈送點數 不可為 0  !");
              return;
             }

          if ((wp.itemStr("app_purch_cond").equals("Y"))&&
              (wp.itemNum("app_months")==0))
             {
              errmsg("申請人有刷卡才回饋期數 不可為 0  !");
              return;
             }
          if ((wp.itemNum("add_months")!=0)&&
              (wp.itemNum("add_times")+wp.itemNum("add_point")==0))
             {
              errmsg("申請人回饋設定 不可全為 0  !");
              return;
             }

         }
     }


  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("active_date_s")&&(!wp.itemEmpty("ACTIVE_DATE_E")))
      if (wp.itemStr("active_date_s").compareTo(wp.itemStr("ACTIVE_DATE_E"))>0)
         {
          errmsg("活動日期：["+wp.itemStr("active_date_s")+"]>["+wp.itemStr("ACTIVE_DATE_E")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("apply_date_s")&&(!wp.itemEmpty("APPLY_DATE_E")))
      if (wp.itemStr("apply_date_s").compareTo(wp.itemStr("APPLY_DATE_E"))>0)
         {
          errmsg("　　發卡/申請日期["+wp.itemStr("apply_date_s")+"]>["+wp.itemStr("APPLY_DATE_E")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_1_beg").length()==0)
          wp.itemSet("limit_1_beg","0");
      if (wp.itemStr("LIMIT_1_END").length()==0)
          wp.itemSet("LIMIT_1_END","0");
      if (Double.parseDouble(wp.itemStr("limit_1_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_1_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_1_END"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr("limit_1_beg")+ ")~(" + wp.itemStr("LIMIT_1_END")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_1_end").length()==0)
          wp.itemSet("limit_1_end","0");
      if (wp.itemStr("LIMIT_2_BEG").length()==0)
          wp.itemSet("LIMIT_2_BEG","0");
      if (Double.parseDouble(wp.itemStr("limit_1_end"))>=Double.parseDouble(wp.itemStr("LIMIT_2_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_2_BEG"))!=0))
         {
          errmsg("區間1-2:("+wp.itemStr("limit_1_end")+ ")~(" + wp.itemStr("LIMIT_2_BEG")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_2_beg").length()==0)
          wp.itemSet("limit_2_beg","0");
      if (wp.itemStr("LIMIT_2_END").length()==0)
          wp.itemSet("LIMIT_2_END","0");
      if (Double.parseDouble(wp.itemStr("limit_2_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_2_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_2_END"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr("limit_2_beg")+ ")~(" + wp.itemStr("LIMIT_2_END")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_2_end").length()==0)
          wp.itemSet("limit_2_end","0");
      if (wp.itemStr("LIMIT_3_BEG").length()==0)
          wp.itemSet("LIMIT_3_BEG","0");
      if (Double.parseDouble(wp.itemStr("limit_2_end"))>=Double.parseDouble(wp.itemStr("LIMIT_3_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_3_BEG"))!=0))
         {
          errmsg("區間1-2:("+wp.itemStr("limit_2_end")+ ")~(" + wp.itemStr("LIMIT_3_BEG")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_3_beg").length()==0)
          wp.itemSet("limit_3_beg","0");
      if (wp.itemStr("LIMIT_3_END").length()==0)
          wp.itemSet("LIMIT_3_END","0");
      if (Double.parseDouble(wp.itemStr("limit_3_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_3_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_3_END"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr("limit_3_beg")+ ")~(" + wp.itemStr("LIMIT_3_END")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_3_end").length()==0)
          wp.itemSet("limit_3_end","0");
      if (wp.itemStr("LIMIT_4_BEG").length()==0)
          wp.itemSet("LIMIT_4_BEG","0");
      if (Double.parseDouble(wp.itemStr("limit_3_end"))>=Double.parseDouble(wp.itemStr("LIMIT_4_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_4_BEG"))!=0))
         {
          errmsg("區間1-2:("+wp.itemStr("limit_3_end")+ ")~(" + wp.itemStr("LIMIT_4_BEG")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_4_beg").length()==0)
          wp.itemSet("limit_4_beg","0");
      if (wp.itemStr("LIMIT_4_END").length()==0)
          wp.itemSet("LIMIT_4_END","0");
      if (Double.parseDouble(wp.itemStr("limit_4_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_4_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_4_END"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr("limit_4_beg")+ ")~(" + wp.itemStr("LIMIT_4_END")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_4_end").length()==0)
          wp.itemSet("limit_4_end","0");
      if (wp.itemStr("LIMIT_5_BEG").length()==0)
          wp.itemSet("LIMIT_5_BEG","0");
      if (Double.parseDouble(wp.itemStr("limit_4_end"))>=Double.parseDouble(wp.itemStr("LIMIT_5_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_5_BEG"))!=0))
         {
          errmsg("區間1-2:("+wp.itemStr("limit_4_end")+ ")~(" + wp.itemStr("LIMIT_5_BEG")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_5_beg").length()==0)
          wp.itemSet("limit_5_beg","0");
      if (wp.itemStr("LIMIT_5_END").length()==0)
          wp.itemSet("LIMIT_5_END","0");
      if (Double.parseDouble(wp.itemStr("limit_5_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_5_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_5_END"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr("limit_5_beg")+ ")~(" + wp.itemStr("LIMIT_5_END")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_5_end").length()==0)
          wp.itemSet("limit_5_end","0");
      if (wp.itemStr("LIMIT_6_BEG").length()==0)
          wp.itemSet("LIMIT_6_BEG","0");
      if (Double.parseDouble(wp.itemStr("limit_5_end"))>=Double.parseDouble(wp.itemStr("LIMIT_6_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_6_BEG"))!=0))
         {
          errmsg("區間1-2:("+wp.itemStr("limit_5_end")+ ")~(" + wp.itemStr("LIMIT_6_BEG")+") 迄起值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("limit_6_beg").length()==0)
          wp.itemSet("limit_6_beg","0");
      if (wp.itemStr("LIMIT_6_END").length()==0)
          wp.itemSet("LIMIT_6_END","0");
      if (Double.parseDouble(wp.itemStr("limit_6_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_6_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_6_END"))!=0))
         {
          errmsg("區間2-3:("+wp.itemStr("limit_6_beg")+ ")~(" + wp.itemStr("LIMIT_6_END")+") 迄起值錯誤!");
          return;
         }
     }

  int checkInt = checkDecnum(wp.itemStr("add_times"),3,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　　　　　回饋設定:　紅利積點 格式超出範圍 : 整數[3]位 小數[2]位");
      if (checkInt==2) 
         errmsg("　　　　　回饋設定:　紅利積點 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　　　　　回饋設定:　紅利積點 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("purch_reclow_amt"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("單筆最低金額 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("單筆最低金額 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("單筆最低金額 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("purch_tol_amt"),11,3);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("累計金額 格式超出範圍 : 整數[11]位 小數[3]位");
      if (checkInt==2) 
         errmsg("累計金額 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("累計金額 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("purch_tol_time"),12,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("累計次數 格式超出範圍 : 整數[12]位");
      if (checkInt==2) 
         errmsg("累計次數 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("累計次數 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("feedback_lmt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　　　　　回饋上限： 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　　　　　回饋上限： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　　　　　回饋上限： 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_1_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_1_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_2_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_2_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_3_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_3_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_4_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_4_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_5_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_5_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_6_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_6_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("bonus_type"))
     {
      errmsg("紅利類別： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apply_date_s"))
     {
      errmsg("　　發卡/申請日期 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apply_date_e"))
     {
      errmsg(" 不可空白");
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
     try {
         rc = dataSelect();
         if (rc!=1) return rc;
         actionInit("A");
         dataCheck();
         if (rc!=1) return rc;

         dbInsertD2T();
         dbInsertI2T();
     } catch (Exception e) {
         e.printStackTrace();
     }
  strSql= " insert into  " + controlTabName + " ("
          + " active_code, "
          + " apr_flag, "
          + " aud_type, "
          + " active_name, "
          + " bonus_type, "
          + " tax_flag, "
          + " active_date_s, "
          + " active_date_e, "
          + " effect_months, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " apply_date_type, "
          + " apply_date_s, "
          + " apply_date_e, "
          + " acct_type_sel, "
          + " group_card_sel, "
          + " platform_kind_sel, "
          + " applicant_cond, "
          + " new_card_cond, "
          + " major_cond, "
          + " major_point, "
          + " sub_cond, "
          + " sub_point, "
          + " app_purch_cond, "
          + " app_months, "
          + " add_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " add_times, "
          + " add_point, "
          + " purch_reclow_cond, "
          + " purch_reclow_amt, "
          + " purch_tol_amt_cond, "
          + " purch_tol_amt, "
          + " purch_tol_time_cond, "
          + " purch_tol_time, "
          + " feedback_lmt, "
          + " limit_1_beg, "
          + " limit_1_end, "
          + " exchange_1, "
          + " limit_2_beg, "
          + " limit_2_end, "
          + " exchange_2, "
          + " limit_3_beg, "
          + " limit_3_end, "
          + " exchange_3, "
          + " limit_4_beg, "
          + " limit_4_end, "
          + " exchange_4, "
          + " limit_5_beg, "
          + " limit_5_end, "
          + " exchange_5, "
          + " limit_6_beg, "
          + " limit_6_end, "
          + " exchange_6, "
          + " introducer_cond, "
//          + " new_card_cond1, "
          + " intro_point, "
          + " intro_purch_cond, "
          + " intro_months, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
//          + "?,"
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
        wp.itemStr("bonus_type"),
        wp.itemStr("tax_flag"),
        wp.itemStr("active_date_s"),
        wp.itemStr("active_date_e"),
        wp.itemNum("effect_months"),
        wp.itemStr("stop_flag"),
        wp.itemStr("stop_date"),
        wp.itemStr("stop_desc"),
        wp.itemStr("apply_date_type"),
        wp.itemStr("apply_date_s"),
        wp.itemStr("apply_date_e"),
        wp.itemStr("acct_type_sel"),
        wp.itemStr("group_card_sel"),
        wp.itemStr("platform_kind_sel"),
        wp.itemStr("applicant_cond"),
        wp.itemStr("new_card_cond"),
        wp.itemStr("major_cond"),
        wp.itemNum("major_point"),
        wp.itemStr("sub_cond"),
        wp.itemNum("sub_point"),
        wp.itemStr("app_purch_cond"),
        wp.itemNum("app_months"),
        wp.itemNum("add_months"),
        wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"),
        wp.itemNum("add_times"),
        wp.itemNum("add_point"),
        wp.itemStr("purch_reclow_cond"),
        wp.itemNum("purch_reclow_amt"),
        wp.itemStr("purch_tol_amt_cond"),
        wp.itemNum("purch_tol_amt"),
        wp.itemStr("purch_tol_time_cond"),
        wp.itemNum("purch_tol_time"),
        wp.itemNum("feedback_lmt"),
        wp.itemNum("limit_1_beg"),
        wp.itemNum("limit_1_end"),
        wp.itemNum("exchange_1"),
        wp.itemNum("limit_2_beg"),
        wp.itemNum("limit_2_end"),
        wp.itemNum("exchange_2"),
        wp.itemNum("limit_3_beg"),
        wp.itemNum("limit_3_end"),
        wp.itemNum("exchange_3"),
        wp.itemNum("limit_4_beg"),
        wp.itemNum("limit_4_end"),
        wp.itemNum("exchange_4"),
        wp.itemNum("limit_5_beg"),
        wp.itemNum("limit_5_end"),
        wp.itemNum("exchange_5"),
        wp.itemNum("limit_6_beg"),
        wp.itemNum("limit_6_end"),
        wp.itemNum("exchange_6"),
        wp.itemStr("introducer_cond"),
//        wp.itemStr("new_card_cond1"),
        wp.itemNum("intro_point"),
        wp.itemStr("intro_purch_cond"),
        wp.itemNum("intro_months"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+ controlTabName +" 重複錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertI2T() throws Exception
 {
   msgOK();

  strSql = "insert into MKT_BN_DATA_T "
         + "select * "
         + "from MKT_BN_DATA "
         + "where table_name  =  'MKT_BPNW' "
         + "and   data_key = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr("active_code"),
     };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , true);


   return 1;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
     try {
         rc = dataSelect();
         if (rc!=1) return rc;
         actionInit("U");
         dataCheck();
         if (rc!=1) return rc;
     } catch (Exception e) {
         e.printStackTrace();
     }

  strSql= "update " + controlTabName + " set "
         + "apr_flag = ?, "
         + "bonus_type = ?, "
         + "tax_flag = ?, "
         + "active_date_s = ?, "
         + "active_date_e = ?, "
         + "effect_months = ?, "
         + "stop_flag = ?, "
         + "stop_date = ?, "
         + "stop_desc = ?, "
         + "apply_date_type = ?, "
         + "apply_date_s = ?, "
         + "apply_date_e = ?, "
         + "acct_type_sel = ?, "
         + "group_card_sel = ?, "
         + "platform_kind_sel = ?, "
         + "applicant_cond = ?, "
         + "new_card_cond = ?, "
         + "major_cond = ?, "
         + "major_point = ?, "
         + "sub_cond = ?, "
         + "sub_point = ?, "
         + "app_purch_cond = ?, "
         + "app_months = ?, "
         + "add_months = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "add_times = ?, "
         + "add_point = ?, "
         + "purch_reclow_cond = ?, "
         + "purch_reclow_amt = ?, "
         + "purch_tol_amt_cond = ?, "
         + "purch_tol_amt = ?, "
         + "purch_tol_time_cond = ?, "
         + "purch_tol_time = ?, "
         + "feedback_lmt = ?, "
         + "limit_1_beg = ?, "
         + "limit_1_end = ?, "
         + "exchange_1 = ?, "
         + "limit_2_beg = ?, "
         + "limit_2_end = ?, "
         + "exchange_2 = ?, "
         + "limit_3_beg = ?, "
         + "limit_3_end = ?, "
         + "exchange_3 = ?, "
         + "limit_4_beg = ?, "
         + "limit_4_end = ?, "
         + "exchange_4 = ?, "
         + "limit_5_beg = ?, "
         + "limit_5_end = ?, "
         + "exchange_5 = ?, "
         + "limit_6_beg = ?, "
         + "limit_6_end = ?, "
         + "exchange_6 = ?, "
         + "introducer_cond = ?, "
//         + "new_card_cond1 = ?, "
         + "intro_point = ?, "
         + "intro_purch_cond = ?, "
         + "intro_months = ?, "
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
     wp.itemStr("bonus_type"),
     wp.itemStr("tax_flag"),
     wp.itemStr("active_date_s"),
     wp.itemStr("active_date_e"),
     wp.itemNum("effect_months"),
     wp.itemStr("stop_flag"),
     wp.itemStr("stop_date"),
     wp.itemStr("stop_desc"),
     wp.itemStr("apply_date_type"),
     wp.itemStr("apply_date_s"),
     wp.itemStr("apply_date_e"),
     wp.itemStr("acct_type_sel"),
     wp.itemStr("group_card_sel"),
     wp.itemStr("platform_kind_sel"),
     wp.itemStr("applicant_cond"),
     wp.itemStr("new_card_cond"),
     wp.itemStr("major_cond"),
     wp.itemNum("major_point"),
     wp.itemStr("sub_cond"),
     wp.itemNum("sub_point"),
     wp.itemStr("app_purch_cond"),
     wp.itemNum("app_months"),
     wp.itemNum("add_months"),
     wp.itemStr("merchant_sel"),
     wp.itemStr("mcht_group_sel"),
     wp.itemNum("add_times"),
     wp.itemNum("add_point"),
     wp.itemStr("purch_reclow_cond"),
     wp.itemNum("purch_reclow_amt"),
     wp.itemStr("purch_tol_amt_cond"),
     wp.itemNum("purch_tol_amt"),
     wp.itemStr("purch_tol_time_cond"),
     wp.itemNum("purch_tol_time"),
     wp.itemNum("feedback_lmt"),
     wp.itemNum("limit_1_beg"),
     wp.itemNum("limit_1_end"),
     wp.itemNum("exchange_1"),
     wp.itemNum("limit_2_beg"),
     wp.itemNum("limit_2_end"),
     wp.itemNum("exchange_2"),
     wp.itemNum("limit_3_beg"),
     wp.itemNum("limit_3_end"),
     wp.itemNum("exchange_3"),
     wp.itemNum("limit_4_beg"),
     wp.itemNum("limit_4_end"),
     wp.itemNum("exchange_4"),
     wp.itemNum("limit_5_beg"),
     wp.itemNum("limit_5_end"),
     wp.itemNum("exchange_5"),
     wp.itemNum("limit_6_beg"),
     wp.itemNum("limit_6_end"),
     wp.itemNum("exchange_6"),
     wp.itemStr("introducer_cond"),
//     wp.itemStr("new_card_cond1"),
     wp.itemNum("intro_point"),
     wp.itemStr("intro_purch_cond"),
     wp.itemNum("intro_months"),
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
     try {
         rc = dataSelect();
         if (rc!=1) return rc;
         actionInit("D");
         dataCheck();
         if (rc!=1)return rc;
         dbInsertD2T();
     } catch (Exception e) {
         e.printStackTrace();
     }

  strSql = "delete " + controlTabName + " " 
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
 public int dbInsertD2T() throws Exception
 {
   msgOK();

   strSql = "delete MKT_BN_DATA_T "
         + " where table_name  =  'MKT_BPNW' "
          + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr("active_code"),
     };

   sqlExec(strSql,param,true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 MKT_BN_DATA_T 錯誤");

   return rc;

 }
// ************************************************************************
 public int checkDecnum(String decStr, int col_length, int colScale)
 {
  if (decStr.length()==0) return(0);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  if (!comm.isNumber(decStr.replace("-","").replace(".",""))) return(3);
  decStr = decStr.replace("-","");
  if ((colScale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) return(2);
  String[]  parts = decStr.split("[.^]");
  if ((parts.length==1&&parts[0].length()>col_length)||
      (parts.length==2&&
       (parts[0].length()>col_length||parts[1].length()>colScale)))
      return(1);
  return(0);
 }
// ************************************************************************
 public int dbInsertI2() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0180_acty"))
      dataType = "1" ;
   if (wp.respHtml.equals("mktm0180_aaa1"))
      dataType = "4" ;
   if (wp.respHtml.equals("mktm0180_platform"))
      dataType = "P" ;
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
          + "'MKT_BPNW', "
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
      wp.itemStr("active_code"),
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
   if (wp.respHtml.equals("mktm0180_acty"))
      dataType = "1" ;
   if (wp.respHtml.equals("mktm0180_aaa1"))
      dataType = "4" ;
   if (wp.respHtml.equals("mktm0180_platform"))
      dataType = "P" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType,
      wp.itemStr("active_code")
     };
   if (sqlRowcount("MKT_BN_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_BPNW' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BN_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_BPNW'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI3() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0180_gpcd"))
      dataType = "7" ;
   if (wp.respHtml.equals("mktm0180_mrch"))
      dataType = "6" ;
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
          + "'MKT_BPNW', "
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
      wp.itemStr("active_code"),
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
   if (wp.respHtml.equals("mktm0180_gpcd"))
      dataType = "7" ;
   if (wp.respHtml.equals("mktm0180_mrch"))
      dataType = "6" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType,
      wp.itemStr("active_code")
     };
   if (sqlRowcount("MKT_BN_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_BPNW' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_BN_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_BPNW'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) throws Exception
 {
  String[] columnData = new String[300];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 300;
  long     realCnt   = 0;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0)
        {
         realCnt = inti;
         break;
        }
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<realCnt;inti++)
    {
     stra = columnCol[inti];
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<realCnt;inti++)
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
      "MKT_BPNW",
      wp.itemStr("active_code"),
     "6"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg ) throws Exception
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
        wp.itemStr("zz_file_name"),
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
 public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt ) throws Exception
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
        "媒體檔名:"+wp.itemStr("zz_file_name"),
        "程式 "+wp.modPgm()+" 轉 "+wp.itemStr("zz_file_name")+" 有"+errorCnt+" 筆錯誤",
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
 int listParmDataCnt(String s1, String s2, String s3, String s4) throws Exception
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

// ************************************************************************

}  // End of class
