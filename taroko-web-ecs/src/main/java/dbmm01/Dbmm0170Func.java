/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/03/03  V1.00.04   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
* 111-12-16  V1.00.03  Zuwei Su       fix issue 覆核狀態: 不可空白         *
* 111/12/22  V1.00.04   Zuwei         輸出sql log                           *
* 113-03-26  V1.00.05  YangHan       增加 [一般消費群組]選項                  *
* 112-05-08  V1.00.06  Zuwei Su       一般消費群組data_type改為'P'                                                                          *
***************************************************************************/
package dbmm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmm0170Func extends FuncEdit
{
 private final String PROGNAME = "VD紅利特惠(一)-特店刷卡參數處理程式113-03-26  V1.00.05";
  String kk1;
  String orgControlTabName = "dbm_bpmh";
  String controlTabName = "dbm_bpmh_t";

 public Dbmm0170Func(TarokoCommon wr)
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
          + " give_code, "
          + " tax_flag, "
          + " feedback_sel, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " activate_s_date, "
          + " activate_e_date, "
          + " re_months, "
          + " in_bl_cond, "
          + " out_bl_cond, "
          + " out_ca_cond, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "  
          + " mcc_code_sel, "
          + " pos_entry_sel, "
          + " bp_amt, "
          + " bp_pnt, "
          + " add_times, "
          + " add_point, "
          + " give_name, "
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
      if (empty(kk1))
         {
          errmsg("活動代號 不可空白");
          return;
         }
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
          if (listParmDataCnt("dbm_bn_data_t"
                                ,"DBM_BPMH"
                                ,wp.colStr("active_code")
                                ,"3")==0)
             {
              errmsg("[帳戶類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("group_code_sel").equals("1"))||
          (wp.itemStr2("group_code_sel").equals("2")))
         {
          if (listParmDataCnt("dbm_bn_data_t"
                                ,"DBM_BPMH"
                                ,wp.colStr("active_code")
                                ,"2")==0)
             {
              errmsg("[團體代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("merchant_sel").equals("1"))||
          (wp.itemStr2("merchant_sel").equals("2")))
         {
          if (listParmDataCnt("dbm_bn_data_t"
                                ,"DBM_BPMH"
                                ,wp.colStr("active_code")
                                ,"1")==0)
             {
              errmsg("[特店代碼] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("mcht_group_sel").equals("1"))||
          (wp.itemStr2("mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("dbm_bn_data_t"
                                ,"DBM_BPMH"
                                ,wp.colStr("active_code")
                                ,"6")==0)
             {
              errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("platform_kind_sel").equals("1"))||
              (wp.itemStr2("platform_kind_sel").equals("2")))
             {
              if (listParmDataCnt("dbm_bn_data_t"
                                    ,"DBM_BPMH"
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
          if (listParmDataCnt("dbm_bn_data_t"
                                ,"DBM_BPMH"
                                ,wp.colStr("active_code")
                                ,"5")==0)
             {
              errmsg("[特店類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("pos_entry_sel").equals("1"))||
          (wp.itemStr2("pos_entry_sel").equals("2")))
         {
          if (listParmDataCnt("dbm_bn_data_t"
                                ,"DBM_BPMH"
                                ,wp.colStr("active_code")
                                ,"4")==0)
             {
              errmsg("[POS ENTRY] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
     }
  if (!wp.itemStr2("in_bl_cond").equals("Y")) wp.itemSet("in_bl_cond","N");
  if (!wp.itemStr2("out_bl_cond").equals("Y")) wp.itemSet("out_bl_cond","N");
  if (!wp.itemStr2("out_ca_cond").equals("Y")) wp.itemSet("out_ca_cond","N");

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

 if ((this.ibAdd)||(this.ibUpdate))
    {
     if ((!wp.itemStr2("in_bl_cond").equals("Y"))&&
         (!wp.itemStr2("out_bl_cond").equals("Y"))&&
         (!wp.itemStr2("out_ca_cond").equals("Y")))
        {
         errmsg("[回饋類別] 至少要勾選一項!");
         return;
        }


     if (wp.itemStr2("feedback_sel").equals("0"))
        {
         if ((wp.itemStr2("purch_s_date").length()==0)||
             (wp.itemStr2("purch_e_date").length()==0))
            {
             errmsg("[活動回饋類別] 點選消費日時，需指定消費期間!");
             return;
            }
        }
     else 
        {
         if ((wp.itemStr2("activate_s_date").length()==0)||
             (wp.itemStr2("activate_e_date").length()==0))
            {
             errmsg("[活動回饋類別] 點選發/開卡日時，需指定發/開卡日期!");
             return;
            }

         if (wp.itemStr2("re_months").length()==0) wp.itemSet("re_months" , "0");
         if (wp.itemNum("re_months")==0)
            {
             errmsg("[活動回饋類別] 點選發/開卡日時，需指定回饋計算月數!");
             return;
            }
        }
        
     if (wp.itemStr2("bp_amt").length()==0) wp.itemSet("bp_amt" , "0");
     if (wp.itemStr2("bp_pnt").length()==0) wp.itemSet("bp_pnt" , "0");
     if ((wp.itemNum("bp_amt")==0)||
         (wp.itemNum("bp_pnt")==0))
        {
         errmsg("每筆交易金額與兌換點數, 不可為 0!");
         return;
        }
     if (wp.itemStr2("add_times").length()==0) wp.itemSet("add_times" , "0");
     if (wp.itemStr2("add_point").length()==0) wp.itemSet("add_point" , "0");
     if ((wp.itemNum("add_times")==0)&&
         (wp.itemNum("add_point")==0))
        {
         errmsg("加贈倍數與點數, 不可同時為 0!");
         return;
        }

     strSql = "select "
            + " wf_desc as give_name "
            + " from ptr_sys_idtab "
            + " where wf_type='GIVE_CODE' "
            + " and wf_id = ? "
            ; 
     Object[] param = new Object[] {wp.itemStr2("give_code")};
     sqlSelect(strSql,param);
          
     wp.itemSet("give_name",colStr("give_name"));
    }
  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("purch_s_date")&&(!wp.itemEmpty("purch_e_date")))
      if (wp.itemStr2("purch_s_date").compareTo(wp.itemStr2("purch_e_date"))>0)
         {
          errmsg("["+wp.itemStr2("purch_s_date")+"]>["+wp.itemStr2("purch_e_date")+"] 起迄值錯誤!");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("activate_s_date")&&(!wp.itemEmpty("activate_e_date")))
      if (wp.itemStr2("activate_s_date").compareTo(wp.itemStr2("activate_e_date"))>0)
         {
          errmsg("　　　　　　　　發卡/開卡日期區間:["+wp.itemStr2("activate_s_date")+"]>["+wp.itemStr2("activate_e_date")+"] 起迄值錯誤!");
          return;
         }
     }

  int checkInt = checkDecnum(wp.itemStr2("bp_amt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("每筆交易金額 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("每筆交易金額 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("每筆交易金額 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("bp_pnt"),9,0);
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

  checkInt = checkDecnum(wp.itemStr2("add_times"),3,4);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("加贈點數 格式超出範圍 : 整數[3]位 小數[4]位");
      if (checkInt==2) 
         errmsg("加贈點數 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("加贈點數 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("give_code"))
     {
      errmsg("贈送代碼: 不可空白");
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
          + " active_name, "
          + " give_code, "
          + " tax_flag, "
          + " feedback_sel, "
          + " purch_s_date, "
          + " purch_e_date, "
          + " activate_s_date, "
          + " activate_e_date, "
          + " re_months, "
          + " in_bl_cond, "
          + " out_bl_cond, "
          + " out_ca_cond, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " platform_kind_sel, "
          + " mcc_code_sel, "
          + " pos_entry_sel, "
          + " bp_amt, "
          + " bp_pnt, "
          + " add_times, "
          + " add_point, "
          + " give_name, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,"
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
        wp.itemStr2("give_code"),
        wp.itemStr2("tax_flag"),
        wp.itemStr2("feedback_sel"),
        wp.itemStr2("purch_s_date"),
        wp.itemStr2("purch_e_date"),
        wp.itemStr2("activate_s_date"),
        wp.itemStr2("activate_e_date"),
        wp.itemNum("re_months"),
        wp.itemStr2("in_bl_cond"),
        wp.itemStr2("out_bl_cond"),
        wp.itemStr2("out_ca_cond"),
        wp.itemStr2("acct_type_sel"),
        wp.itemStr2("group_code_sel"),
        wp.itemStr2("merchant_sel"),
        wp.itemStr2("mcht_group_sel"),
        wp.itemStr2("platform_kind_sel"),
        wp.itemStr2("mcc_code_sel"),
        wp.itemStr2("pos_entry_sel"),
        wp.itemNum("bp_amt"),
        wp.itemNum("bp_pnt"),
        wp.itemNum("add_times"),
        wp.itemNum("add_point"),
        wp.itemStr2("give_name"),
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

  strSql = "insert into DBM_BN_DATA_T "
         + "select * "
         + "from DBM_BN_DATA "
         + "where table_name  =  'DBM_BPMH' "
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
         + "give_code = ?, "
         + "tax_flag = ?, "
         + "feedback_sel = ?, "
         + "purch_s_date = ?, "
         + "purch_e_date = ?, "
         + "activate_s_date = ?, "
         + "activate_e_date = ?, "
         + "re_months = ?, "
         + "in_bl_cond = ?, "
         + "out_bl_cond = ?, "
         + "out_ca_cond = ?, "
         + "acct_type_sel = ?, "
         + "group_code_sel = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "platform_kind_sel = ?, "       
         + "mcc_code_sel = ?, "
         + "pos_entry_sel = ?, "
         + "bp_amt = ?, "
         + "bp_pnt = ?, "
         + "add_times = ?, "
         + "add_point = ?, "
         + "give_name = ?, "
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
     wp.itemStr2("give_code"),
     wp.itemStr2("tax_flag"),
     wp.itemStr2("feedback_sel"),
     wp.itemStr2("purch_s_date"),
     wp.itemStr2("purch_e_date"),
     wp.itemStr2("activate_s_date"),
     wp.itemStr2("activate_e_date"),
     wp.itemNum("re_months"),
     wp.itemStr2("in_bl_cond"),
     wp.itemStr2("out_bl_cond"),
     wp.itemStr2("out_ca_cond"),
     wp.itemStr2("acct_type_sel"),
     wp.itemStr2("group_code_sel"),
     wp.itemStr2("merchant_sel"),
     wp.itemStr2("mcht_group_sel"),
     wp.itemStr2("platform_kind_sel"),
     wp.itemStr2("mcc_code_sel"),
     wp.itemStr2("pos_entry_sel"),
     wp.itemNum("bp_amt"),
     wp.itemNum("bp_pnt"),
     wp.itemNum("add_times"),
     wp.itemNum("add_point"),
     wp.itemStr2("give_name"),
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

   strSql = "delete DBM_BN_DATA_T "
         + " where table_name  =  'DBM_BPMH' "
          + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr2("active_code"),
     };

   sqlExec(strSql,param, true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 DBM_BN_DATA_T 錯誤");

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
 public int dbInsertI2() 
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("dbmm0170_acty"))
      dataType = "3" ;
   if (wp.respHtml.equals("dbmm0170_grop"))
      dataType = "2" ;
   if (wp.respHtml.equals("dbmm0170_aaa1"))
      dataType = "6" ;
   if (wp.respHtml.equals("dbmm0170_platform"))
	      dataType = "P" ;
   if (wp.respHtml.equals("dbmm0170_mccd"))
      dataType = "5" ;
   if (wp.respHtml.equals("dbmm0170_enty"))
      dataType = "4" ;
  strSql = "insert into DBM_BN_DATA_T ( "
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
          + "'DBM_BPMH', "
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

   if (rc!=1) errmsg("新增8 DBM_BN_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2() 
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("dbmm0170_acty"))
      dataType = "3" ;
   if (wp.respHtml.equals("dbmm0170_grop"))
      dataType = "2" ;
   if (wp.respHtml.equals("dbmm0170_aaa1"))
      dataType = "6" ;
   if (wp.respHtml.equals("dbmm0170_platform"))
	      dataType = "P" ;
   if (wp.respHtml.equals("dbmm0170_mccd"))
      dataType = "5" ;
   if (wp.respHtml.equals("dbmm0170_enty"))
      dataType = "4" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("active_code")
     };
   if (sqlRowcount("DBM_BN_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'DBM_BPMH' "
                    , param) <= 0)
       return 1;

   strSql = "delete DBM_BN_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'DBM_BPMH'  "
          ;
   sqlExec(strSql,param, true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI3() 
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("dbmm0170_mcht"))
      dataType = "1" ;
  strSql = "insert into DBM_BN_DATA_T ( "
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
          + "'DBM_BPMH', "
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

   if (rc!=1) errmsg("新增8 DBM_BN_DATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD3() 
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("dbmm0170_mcht"))
      dataType = "1" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("active_code")
     };
   if (sqlRowcount("DBM_BN_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'DBM_BPMH' "
                    , param) <= 0)
       return 1;

   strSql = "delete DBM_BN_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'DBM_BPMH'  "
          ;
   sqlExec(strSql,param, true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI2Mcht(String tableName,String[] columnCol,String[] columnDat) throws Exception
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
 public int dbDeleteD2Mcht(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "DBM_BPMH",
      wp.itemStr2("active_code"),
     "1"
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
  String Strsql = "select count(*) as data_cnt "
                + "from  " + s1 +" "
                + " where table_name = ? "
                + " and   data_key   = ? "
                + " and   data_type  = ? "
                ;
  Object[] param = new Object[] {s2,s3,s4};
  sqlSelect(Strsql,param);

 return(Integer.parseInt(colStr("data_cnt")));
 }

// ************************************************************************

}  // End of class
