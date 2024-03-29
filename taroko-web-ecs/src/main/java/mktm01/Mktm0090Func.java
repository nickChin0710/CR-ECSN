/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/07  V1.00.00   Yang Bo      Initial                              *
* 112/03/10  V1.00.01   Yang Bo      Add Params Check - BL, IT, AO                              *
* 112/03/19  V1.00.01   Zuwei Su     放行狀態顯示錯誤                  *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import busi.ecs.CommFunction;
import busi.ecs.CommRoutine;
import taroko.com.TarokoCommon;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;

// ************************************************************************
public class Mktm0090Func extends FuncEdit
{
 private final String PROGNAME = "紅利利率轉換檔參數維護 112/03/07  V1.00.00";
  String kk1,kk2,kk3,kk4;
  String orgControlTabName = "cyc_bpid2";
  String controlTabName = "cyc_bpid2_t";

 public Mktm0090Func(TarokoCommon wr)
 {
  wp = wr;
  conn = wp.getConn();
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
  if (procTabName.length()==0) {
      return(1);
  }
  strSql= " select "
          + " apr_flag, "
          + " active_month_s, "
          + " active_month_e, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " bl_cond, "
          + " it_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " ot_cond, "
          + " ca_cond, "
          + " effect_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " group_card_sel, "
          + " group_merchant_sel, "
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
          + " feedback_lmt, "
          + " limit_amt, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("rowid")
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) {
      errmsg("查無資料，讀取 "+ controlTabName +" 失敗");
  }

  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck()
 {
  if (!ibDelete)
     {
      if (wp.colStr("storetype").equals("Y"))
        {
         errmsg("[查原資料]模式中, 請按[還原異動] 才可儲存 !");
         return;
        }
     }
  if (ibAdd)
     {
//      kk1 = wp.itemStr("years");
//      if (empty(kk1))
//         {
//          errmsg("活動年度 不可空白");
//          return;
//         }
      kk1 = wp.itemStr("active_code");
      if (empty(kk1))
         {
             errmsg("基本回饋代號 不可空白");
             return;
         }
      kk2 = wp.itemStr("bonus_type");
      kk3 = wp.itemStr("acct_type");
      kk4 = wp.itemStr("foreign_code");
      if (empty(kk3))
         {
          errmsg("帳戶類別 不可空白");
          return;
         }
//      kk4 = wp.itemStr("item_code");
//      if (empty(kk4))
//         {
//          errmsg("科目類別 不可空白");
//          return;
//         }
     }
  else
     {
//      kk1 = wp.itemStr("years");
      kk1 = wp.itemStr("active_code");
      kk2 = wp.itemStr("bonus_type");
      kk3 = wp.itemStr("acct_type");
      kk4 = wp.itemStr("foreign_code");
     }
  if (ibUpdate)
     {
      if ((wp.itemStr("merchant_sel").equals("1"))||
          (wp.itemStr("merchant_sel").equals("2")))
         {
          if (listParmDataCnt("cyc_bn_data_t"
                                ,"CYC_BPID2"
                                ,wp.colStr("active_code")
                                ,"1")==0)
             {
              errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr("mcht_group_sel").equals("1"))||
          (wp.itemStr("mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("cyc_bn_data_t"
                                ,"CYC_BPID2"
                                ,wp.colStr("active_code")
                                ,"4")==0)
             {
              errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr("platform_kind_sel").equals("1"))||
              (wp.itemStr("platform_kind_sel").equals("2")))
             {
              if (listParmDataCnt("cyc_bn_data_t"
                                    ,"CYC_BPID2"
                                    ,wp.colStr("active_code")
                                    ,"P")==0)
                 {
                  errmsg("[一般消費群組] 明細沒有設定, 筆數不可為 0  !");
                  return;
                 }
          }
      if ((wp.itemStr("group_card_sel").equals("1"))||
          (wp.itemStr("group_card_sel").equals("2")))
         {
          if (listParmDataCnt("cyc_bn_data_t"
                                ,"CYC_BPID2"
                                ,wp.colStr("active_code")
                                ,"2")==0)
             {
              errmsg("[團代卡種] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr("group_merchant_sel").equals("1"))||
          (wp.itemStr("group_merchant_sel").equals("2")))
         {
          if (listParmDataCnt("cyc_bn_data_t"
                                ,"CYC_BPID2"
                                ,wp.colStr("active_code")
                                ,"3")==0)
             {
              errmsg("[團代特店] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
     }
//  if (!wp.itemStr("other_item").equals("Y")) {
//      wp.itemSet("other_item","N");
//  }
     if (ibAdd || ibUpdate) {
         if (wp.itemStr("stop_flag").equals("Y")) {
             if (wp.itemStr("stop_date").length() == 0) {
                 errmsg("取消日期勾選時 不可空白");
                 return;
             }
         }
     }

     if (ibAdd || ibUpdate) {
         if (!wp.itemStr("bl_cond").equals("Y") && !wp.itemStr("it_cond").equals("Y")
                 && !wp.itemStr("id_cond").equals("Y") && !wp.itemStr("ao_cond").equals("Y")
                 && !wp.itemStr("ot_cond").equals("Y") && !wp.itemStr("ca_cond").equals("Y")) {
             errmsg("消費本金類: 至少選取一項");
             return;
         }
     }

     if (ibAdd || ibUpdate) {
         if (!wp.itemStr("bl_cond").equals("Y") || !wp.itemStr("ao_cond").equals("Y")
                 || !wp.itemStr("it_cond").equals("Y")) {
             errmsg("消費本金類: BL, AO, IT 三項本金類必選");
             return;
         }
     }

     if (ibAdd || ibUpdate) {
         if (!wp.itemEmpty("active_month_s")) {
             if (!dateStrIsValid(wp.itemStr("active_month_s"))) {
                 errmsg("基本回饋期間 -起 日期格式錯誤,格式要求YYYYMM");
                 return;
             }
         }
         if (!wp.itemEmpty("active_month_e")) {
             if (!dateStrIsValid(wp.itemStr("active_month_e"))) {
                 errmsg("基本回饋期間 -迄 日期格式錯誤,格式要求YYYYMM");
                 return;
             }
         }

         if (!wp.itemEmpty("active_month_s") && !wp.itemEmpty("active_month_e")) {
             if (Integer.parseInt(wp.itemStr("active_month_s")) > Integer.parseInt(wp.itemStr("active_month_e"))) {
                 errmsg("基本回饋期間 -起迄日期範圍錯誤");
                 return;
             }
         }
     }

     if (wp.empty("aud_type")) {
         wp.itemSet("aud_type", "A");
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


   if (wp.itemEmpty("apr_flag")) {
       wp.colSet("apr_flag", "N");
       wp.itemSet("apr_flag", "N");
   }
   if ((ibDelete)||
       (wp.itemStr("aud_type").equals("D"))) {
       return;
   }


  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_1_beg").length()==0) {
          wp.itemSet("limit_1_beg","0");
      }
      if (wp.itemStr("LIMIT_1_END").length()==0) {
          wp.itemSet("LIMIT_1_END","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_1_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_1_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_1_END"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_1_beg")+")>=limit_1_end("+wp.itemStr("LIMIT_1_END")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_1_end").length()==0) {
          wp.itemSet("limit_1_end","0");
      }
      if (wp.itemStr("LIMIT_2_BEG").length()==0) {
          wp.itemSet("LIMIT_2_BEG","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_1_end"))>=Double.parseDouble(wp.itemStr("LIMIT_2_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_2_BEG"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_1_end")+")>=limit_2_beg("+wp.itemStr("LIMIT_2_BEG")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_2_beg").length()==0) {
          wp.itemSet("limit_2_beg","0");
      }
      if (wp.itemStr("LIMIT_2_END").length()==0) {
          wp.itemSet("LIMIT_2_END","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_2_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_2_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_2_END"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_2_beg")+")>=limit_2_end("+wp.itemStr("LIMIT_2_END")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_2_end").length()==0) {
          wp.itemSet("limit_2_end","0");
      }
      if (wp.itemStr("LIMIT_3_BEG").length()==0) {
          wp.itemSet("LIMIT_3_BEG","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_2_end"))>=Double.parseDouble(wp.itemStr("LIMIT_3_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_3_BEG"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_2_end")+")>=limit_3_beg("+wp.itemStr("LIMIT_3_BEG")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_3_beg").length()==0) {
          wp.itemSet("limit_3_beg","0");
      }
      if (wp.itemStr("LIMIT_3_END").length()==0) {
          wp.itemSet("LIMIT_3_END","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_3_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_3_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_3_END"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_3_beg")+")>=limit_3_end("+wp.itemStr("LIMIT_3_END")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_3_end").length()==0) {
          wp.itemSet("limit_3_end","0");
      }
      if (wp.itemStr("LIMIT_4_BEG").length()==0) {
          wp.itemSet("LIMIT_4_BEG","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_3_end"))>=Double.parseDouble(wp.itemStr("LIMIT_4_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_4_BEG"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_3_end")+")>=limit_4_beg("+wp.itemStr("LIMIT_4_BEG")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_4_beg").length()==0) {
          wp.itemSet("limit_4_beg","0");
      }
      if (wp.itemStr("LIMIT_4_END").length()==0) {
          wp.itemSet("LIMIT_4_END","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_4_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_4_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_4_END"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_4_beg")+")>=limit_4_end("+wp.itemStr("LIMIT_4_END")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_4_end").length()==0) {
          wp.itemSet("limit_4_end","0");
      }
      if (wp.itemStr("LIMIT_5_BEG").length()==0) {
          wp.itemSet("LIMIT_5_BEG","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_4_end"))>=Double.parseDouble(wp.itemStr("LIMIT_5_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_5_BEG"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_4_end")+")>=limit_5_beg("+wp.itemStr("LIMIT_5_BEG")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_5_beg").length()==0) {
          wp.itemSet("limit_5_beg","0");
      }
      if (wp.itemStr("LIMIT_5_END").length()==0) {
          wp.itemSet("LIMIT_5_END","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_5_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_5_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_5_END"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_5_beg")+")>=limit_5_end("+wp.itemStr("LIMIT_5_END")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_5_end").length()==0) {
          wp.itemSet("limit_5_end","0");
      }
      if (wp.itemStr("LIMIT_6_BEG").length()==0) {
          wp.itemSet("LIMIT_6_BEG","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_5_end"))>=Double.parseDouble(wp.itemStr("LIMIT_6_BEG"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_6_BEG"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_5_end")+")>=limit_6_beg("+wp.itemStr("LIMIT_6_BEG")+") 起迄值錯誤!");
          return;
         }
     }

  if ((ibAdd)||(ibUpdate))
     {
      if (wp.itemStr("limit_6_beg").length()==0) {
          wp.itemSet("limit_6_beg","0");
      }
      if (wp.itemStr("LIMIT_6_END").length()==0) {
          wp.itemSet("LIMIT_6_END","0");
      }
      if (Double.parseDouble(wp.itemStr("limit_6_beg"))>=Double.parseDouble(wp.itemStr("LIMIT_6_END"))&&
          (Double.parseDouble(wp.itemStr("LIMIT_6_END"))!=0))
         {
          errmsg("　("+wp.itemStr("limit_6_beg")+")>=limit_6_end("+wp.itemStr("LIMIT_6_END")+") 起迄值錯誤!");
          return;
         }
     }

  int checkInt = checkDecnum(wp.itemStr("limit_1_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_1_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_2_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_2_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_3_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_3_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_4_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_4_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_5_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_5_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_6_beg"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  checkInt = checkDecnum(wp.itemStr("limit_6_end"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) {
          errmsg("　 格式超出範圍 : 整數[9]位");
      }
      if (checkInt==2) {
          errmsg("　 格式超出範圍 : 不可有小數位");
      }
      if (checkInt==3) {
          errmsg("　 非數值");
      }
      return;
     }

  if ((ibAdd)||(ibUpdate)) {
      if (wp.itemEmpty("apr_flag"))
         {
          errmsg("覆核狀態: 不可空白");
          return;
         }
  }


  if (isAdd()) {
      return;
  }

  if (ibDelete)
     {
      wp.colSet("storetype" , "N");
     }
 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  rc = dataSelect();
  if (rc!=1) {
      return rc;
  }
  actionInit("A");
  dataCheck();
  if (rc!=1) {
      return rc;
  }

  dbInsertD3T();
  dbInsertI3T();

  strSql= " insert into  " + controlTabName + " ("
          + " active_code, "
          + " active_name, "
          + " bonus_type, "
          + " acct_type, "
          + " foreign_code, "
          + " active_month_s, "
          + " active_month_e, "
          + " stop_flag, "
          + " stop_date, "
          + " stop_desc, "
          + " bl_cond, "
          + " ca_cond, "
          + " id_cond, "
          + " ao_cond, "
          + " it_cond, "
          + " ot_cond, "
          + " effect_months, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " group_card_sel, "
          + " group_merchant_sel, "
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
          + " feedback_lmt, "
          + " limit_amt, "
          + " crt_date, "
          + " crt_user, "
          + " apr_flag, "
          + " aud_type, "
          + " mod_user, "
          + " mod_time, "
          + " mod_seqno, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,"
          + "?,"
          + "? )";

  Object[] param =new Object[]
       {
        wp.itemStr("active_code"),
        wp.itemStr("active_name"),
        kk2,
        kk3,
        kk4,
        wp.itemStr("active_month_s"),
        wp.itemStr("active_month_e"),
        wp.itemStr("stop_flag"),
        wp.itemStr("stop_date"),
        wp.itemStr("stop_desc"),
        wp.itemStr("bl_cond"),
        wp.itemStr("ca_cond"),
        wp.itemStr("id_cond"),
        wp.itemStr("ao_cond"),
        wp.itemStr("it_cond"),
        wp.itemStr("ot_cond"),
        wp.itemNum("effect_months"),
        wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"),
        wp.itemStr("platform_kind_sel"),
        wp.itemStr("group_card_sel"),
        wp.itemStr("group_merchant_sel"),
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
        wp.itemNum("feedback_lmt"),
        wp.itemNum("limit_amt"),
        wp.loginUser,
        wp.itemStr("apr_flag"),
        wp.itemStr("aud_type"),
        wp.loginUser,
        wp.modSeqno(),
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
      errmsg("新增 "+ controlTabName +" 重複錯誤");
  }

  return rc;
 }
// ************************************************************************
 public int dbInsertI3T()
 {
   msgOK();

  strSql = "insert into CYC_BN_DATA_T "
         + "select * "
         + "from CYC_BN_DATA "
         + "where table_name  =  'CYC_BPID2' "
         + "and   data_key = ? "
         + "";

   Object[] param =new Object[]
     {
//      wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code"), 
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
  rc = dataSelect();
  if (rc!=1) {
      return rc;
  }
  actionInit("U");
  dataCheck();
  if (rc!=1) {
      return rc;
  }

  strSql= "update " + controlTabName + " set "
          + " active_code = ?, "
          + " active_name = ?, "
          + " bonus_type = ?, "
          + " acct_type = ?, "
          + " foreign_code = ?, "
          + " active_month_s = ?, "
          + " active_month_e = ?, "
          + " stop_flag = ?, "
          + " stop_date = ?, "
          + " stop_desc = ?, "
          + " bl_cond = ?, "
          + " ca_cond = ?, "
          + " id_cond = ?, "
          + " ao_cond = ?, "
          + " it_cond = ?, "
          + " ot_cond = ?, "
          + " effect_months = ?, "
          + " merchant_sel = ?, "
          + " mcht_group_sel = ?, "
          + " platform_kind_sel = ?, "
          + " group_card_sel = ?, "
          + " group_merchant_sel = ?, "
          + " limit_1_beg = ?, "
          + " limit_1_end = ?, "
          + " exchange_1 = ?, "
          + " limit_2_beg = ?, "
          + " limit_2_end = ?, "
          + " exchange_2 = ?, "
          + " limit_3_beg = ?, "
          + " limit_3_end = ?, "
          + " exchange_3 = ?, "
          + " limit_4_beg = ?, "
          + " limit_4_end = ?, "
          + " exchange_4 = ?, "
          + " limit_5_beg = ?, "
          + " limit_5_end = ?, "
          + " exchange_5 = ?, "
          + " limit_6_beg = ?, "
          + " limit_6_end = ?, "
          + " exchange_6 = ?, "
          + " feedback_lmt = ?, "
          + " limit_amt = ?, "
          + " crt_user  = ?, "
          + " crt_date  = to_char(sysdate,'yyyymmdd'), "
          + " mod_user  = ?, "
          + " mod_seqno = nvl(mod_seqno,0)+1, "
          + " mod_time  = sysdate, "
          + " mod_pgm   = ? "
          + " where rowid = ? "
          + " and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr("active_code"),
     wp.itemStr("active_name"),
     kk2,
     kk3,
     kk4,
     wp.itemStr("active_month_s"),
     wp.itemStr("active_month_e"),
     wp.itemStr("stop_flag"),
     wp.itemStr("stop_date"),
     wp.itemStr("stop_desc"),
     wp.itemStr("bl_cond"),
     wp.itemStr("ca_cond"),
     wp.itemStr("id_cond"),
     wp.itemStr("ao_cond"),
     wp.itemStr("it_cond"),
     wp.itemStr("ot_cond"),
     wp.itemNum("effect_months"),
     wp.itemStr("merchant_sel"),
     wp.itemStr("mcht_group_sel"),
     wp.itemStr("platform_kind_sel"),
     wp.itemStr("group_card_sel"),
     wp.itemStr("group_merchant_sel"),
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
     wp.itemNum("feedback_lmt"),
     wp.itemNum("limit_amt"),
     wp.loginUser,
     wp.loginUser,
     wp.itemStr("mod_pgm"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
      errmsg("更新 "+ controlTabName +" 錯誤");
  }

  if (sqlRowNum <= 0) { 
      rc=0;
  } else {
      rc=1;
  }
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  rc = dataSelect();
  if (rc!=1) {
      return rc;
  }
  actionInit("D");
  dataCheck();
  if (rc!=1) {
      return rc;
  }

  dbInsertD3T();

  strSql = "delete " + controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
      rc=0;
  } else {
      rc=1;
  }
  if (sqlRowNum <= 0) 
     {
      errmsg("刪除 "+ controlTabName +" 錯誤");
      return(-1);
     }

  return rc;
 }
// ************************************************************************
 public int dbInsertD3T()
 {
   msgOK();

   strSql = "delete CYC_BN_DATA_T "
         + " where table_name  =  'CYC_BPID2' "
         + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
//      wp.itemStr("years")+wp.itemStr("bonus_type")+wp.itemStr("acct_type")+wp.itemStr("item_code"), 
      wp.itemStr("active_code"), 
     };

   sqlExec(strSql,param,true);
   if (sqlRowNum <= 0) {
       rc=0;
   } else {
       rc=1;
   }

   if (rc!=1) {
       errmsg("刪除 CYC_BN_DATA_T 錯誤");
   }

   return rc;

 }
// ************************************************************************
 public int checkDecnum(String decStr, int colLength, int colScale)
 {
  if (decStr.length()==0) {
      return(0);
  }
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  if (!comm.isNumber(decStr.replace("-","").replace(".",""))) {
      return(3);
  }
  decStr = decStr.replace("-","");
  if ((colScale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) {
      return(2);
  }
  String[]  parts = decStr.split("[.^]");
  if ((parts.length==1&&parts[0].length()>colLength)||
      (parts.length==2&&
       (parts[0].length()>colLength||parts[1].length()>colScale))) {
      return(1);
  }
  return(0);
 }
// ************************************************************************
 public int dbInsertI3() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0090_mccd")) {
       dataType = "1" ;
   }
   if (wp.respHtml.equals("mktm0090_gpcd")) {
       dataType = "2" ;
   }
   if (wp.respHtml.equals("mktm0090_gpmc")) {
       dataType = "3" ;
   }
  strSql = "insert into CYC_BN_DATA_T ( "
          + "table_name, "
          + "data_key, "
          + "data_type, "
          + "data_code,"
          + "data_code2,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "'CYC_BPID2', "
          + "?, "
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
      wp.itemStr("active_code"), 
      dataType, 
      varsStr("data_code"),
      varsStr("data_code2"),
      wp.loginUser,
      wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) {
       rc=0;
   } else {
       rc=1;
   }

   if (rc!=1) {
       errmsg("新增8 CYC_BN_DATA_T 錯誤");
   }

   return rc;
 }
// ************************************************************************
 public int dbDeleteD3()
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0090_mccd")) {
       dataType = "1" ;
   }
   if (wp.respHtml.equals("mktm0090_gpcd")) {
       dataType = "2" ;
   }
   if (wp.respHtml.equals("mktm0090_gpmc")) {
       dataType = "3" ;
   }
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr("active_code"), 
      dataType  
     };
   if (sqlRowcount("CYC_BN_DATA_T" 
                     , "where data_key = ? "
                    + "and   data_type = ? "
                    + "and   table_name = 'CYC_BPID2' "
                    , param) <= 0) {
       return 1;
   }

   strSql = "delete CYC_BN_DATA_T "
          + "where data_key = ? "
          + "and   data_type = ? "
          + "and   table_name = 'CYC_BPID2'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI2()
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0090_mcgp")) {
       dataType = "4" ;
   }
   if (wp.respHtml.equals("mktm0090_mcgp2")) {
       dataType = "P" ;
   }
  strSql = "insert into CYC_BN_DATA_T ( "
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
          + "'CYC_BPID2', "
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
      wp.itemStr("active_code"), 
      dataType, 
      varsStr("data_code"),
      wp.loginUser,
      wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) {
       rc=0;
   } else {
       rc=1;
   }

   if (rc!=1) {
       errmsg("新增8 CYC_BN_DATA_T 錯誤");
   }

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2()
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm0090_mcgp")) {
       dataType = "4" ;
   }
   if (wp.respHtml.equals("mktm0090_mcgp2")) {
       dataType = "P" ;
   }
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr("active_code"), 
      dataType  
     };
   if (sqlRowcount("CYC_BN_DATA_T" 
                     , "where data_key = ? "
                    + "and   data_type = ? "
                    + "and   table_name = 'CYC_BPID2' "
                    , param) <= 0) {
       return 1;
   }

   strSql = "delete CYC_BN_DATA_T "
          + "where data_key = ? "
          + "and   data_type = ? "
          + "and   table_name = 'CYC_BPID2'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat)
 {
  String[] columnData = new String[50];
  String   stra="",strb="";
  int      skipLine= 0;
  long     listCnt   = 50;
  strSql= " insert into  " + tableName + " (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) {
         continue;
     }
     strSql = strSql + stra + ",";
    }

  strSql = strSql
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values (";
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) {
         continue;
     }
     strSql = strSql + "?," ;
    }
  strSql = strSql
         + "?,"
         + "timestamp_format(?,'yyyymmddhh24miss'),?)";

  Object[] param1 =new Object[50];
  for (int inti=0;inti<listCnt;inti++)
    {
     stra = columnCol[inti];
     if (stra.length()==0) {
         continue;
     }
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
  if (sqlRowNum <= 0) {
      rc=0;
  } else {
      rc=1;
  }

  return rc;
 }
// ************************************************************************
 public int dbDeleteD2Aaa1(String table_name)
 {
  strSql = "delete  "+table_name+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "CYC_BPID2",
      wp.itemStr("active_code"),
     "1"
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) {
      rc=0;
  } else {
      rc=1;
  }
  if (rc!=1) {
      errmsg("刪除 "+ table_name +" 錯誤");
  }

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg )
 {
  dateTime();
  CommRoutine comr = new CommRoutine();
  CommFunction comm = new CommFunction();
  comr.setConn(wp);

  if (!comm.isNumber(errMsg[10])) {
      errMsg[10]="0";
  }
  if (!comm.isNumber(errMsg[1])) {
      errMsg[1]="0";
  }
  if (!comm.isNumber(errMsg[2])) {
      errMsg[2]="0";
  }

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
  if (sqlRowNum <= 0) {
      errmsg("新增4 ecs_media_errlog 錯誤");
  }

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt )
 {
  CommRoutine comr = new CommRoutine();
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
  if (sqlRowNum <= 0) {
      errmsg("新增5 ecs_modify_log 錯誤");
  }
  return rc;
 }
// ************************************************************************
 int listParmDataCnt(String s1, String s2, String s3, String s4)
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

    public boolean dateStrIsValid(String dateString) {
        if (dateString == null || dateString.length() != 6) {
            return false;
        }
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMM");
        Date date;
        try {
            date = dateTimeFormatter.parse(dateString);
            return dateString.equals(dateTimeFormatter.format(date));
        } catch (Exception e) {
            return false;
        }
    }

// ************************************************************************

}  // End of class
