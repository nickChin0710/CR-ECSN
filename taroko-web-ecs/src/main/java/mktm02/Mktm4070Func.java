/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/06/11  V1.00.03   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                           *
* 111-12-16  V1.00.03  Zuwei Su       fix issue 覆核狀態: 不可空白         *
* 111/12/22  V1.00.04   Zuwei         輸出sql log                           *
* 112/03/03  V1.00.05  Grace          '基金'修訂為'現金回饋'                  *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm4070Func extends FuncEdit
{
  String kk1;
  String orgControlTabName = "mkt_loan_parm";
  String controlTabName = "mkt_loan_parm_t";

 public Mktm4070Func(TarokoCommon wr)
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
          + " fund_name, "
          + " effect_months, "
          + " stop_flag, "
          + " list_cond, "
          + " list_flag, "
          + " list_feedback_date, "
          + " add_vouch_no, "
          + " rem_vouch_no, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " group_oppost_cond, "
          + " feedback_lmt, "
          + " res_flag, "
          + " res_total_cnt, "
          + " exec_s_months, "
          + " move_cond, "
          + " bil_mcht_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " issue_a_months, "
          + " mcode, "
          + " cancel_rate, "
          + " cancel_scope, "
          + " cancel_event, "
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
      kk1 = wp.itemStr2("fund_code");
      if (empty(kk1))
         {
          errmsg("現金回饋代碼 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr2("fund_code");
     }
  if (this.ibUpdate)
     {
      if ((wp.itemStr2("acct_type_sel").equals("1"))||
          (wp.itemStr2("acct_type_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_parm_data_t"
                                ,"MKT_LOAN_PARM"
                                ,wp.colStr("fund_code")
                                ,"1")==0)
             {
              errmsg("[帳戶類別] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("group_code_sel").equals("1"))||
          (wp.itemStr2("group_code_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_parm_data_t"
                                ,"MKT_LOAN_PARM"
                                ,wp.colStr("fund_code")
                                ,"2")==0)
             {
              errmsg("[團體代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("merchant_sel").equals("1"))||
          (wp.itemStr2("merchant_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_parm_data_t"
                                ,"MKT_LOAN_PARM"
                                ,wp.colStr("fund_code")
                                ,"3")==0)
             {
              errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
      if ((wp.itemStr2("mcht_group_sel").equals("1"))||
          (wp.itemStr2("mcht_group_sel").equals("2")))
         {
          if (listParmDataCnt("mkt_parm_data_t"
                                ,"MKT_LOAN_PARM"
                                ,wp.colStr("fund_code")
                                ,"4")==0)
             {
              errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
              return;
             }
         }
     }
  if (!wp.itemStr2("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
  if (!wp.itemStr2("list_cond").equals("Y")) wp.itemSet("list_cond","N");
  if (!wp.itemStr2("group_oppost_cond").equals("Y")) wp.itemSet("group_oppost_cond","N");
  if (!wp.itemStr2("bil_mcht_cond").equals("Y")) wp.itemSet("bil_mcht_cond","N");

   if (this.ibUpdate)
      {
       if (wp.itemStr2("list_cond").equals("Y"))
          {
           if (listImloanDataCnt("mkt_imloan_list_t"
                                 ,""
                                 ,wp.colStr("fund_code")
                                 ,"")==0)

              {
               errmsg("[名單類別] 明細沒有設定, 筆數不可為 0  !");
               return;
              }
          }
      }

  if ((this.ibAdd)&&
      (wp.itemStr2("aud_type").equals("U")))
     {
      if ((wp.itemStr2("list_cond").equals("Y"))&&
           (wp.itemStr2("list_feedback_date").length()!=0))
         {
          errmsg("[名單匯入] 該現金回饋代碼已回饋, 不可再異動! ");
          return;
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

 colSet("list_feedback_date", "");
  if ((this.ibAdd)||(this.ibUpdate))
     {
      if ((wp.itemStr2("bil_mcht_cond").equals("Y"))&&
          (wp.itemStr2("merchant_sel").equals("1")))
         {
          errmsg("[bilm0810維護之排除]時，[特店代號]不能選擇指定");
          return;
         }
      if ((wp.itemStr2("bil_mcht_cond").equals("Y"))&&
          (wp.itemStr2("mcht_group_sel").equals("1")))
         {
          errmsg("[bilm0810維護之排除]時，[特店群組]不能選擇指定");
          return;
         }

/*
      if ((wp.item_ss("issue_a_months").length()==0)||(wp.item_ss("issue_a_months").equals("0")))
         {
          errmsg("[抵用限制：1.新發卡] 要大於0!");
          return;
         }
      if ((wp.item_ss("mcode").length()==0)||(wp.item_ss("mcode").equals("0")))
         {
          errmsg("[M_code大於] 要大於0!");
          return;
         }
*/
      if ((wp.itemStr2("group_oppost_cond").equals("Y"))&& 
           (!wp.itemStr2("group_code_sel").equals("1")))
         {
          errmsg("[團體代號] 團代停卡判斷必須選擇指定團體");
          return;

         } 
      if ((wp.itemStr2("list_cond").equals("Y"))&&
          (wp.itemStr2("list_flag").length()==0))
         {
          errmsg("[名單匯入] 名單類別未選擇 ");
          return;
         }

      if ((!wp.itemStr2("list_cond").equals("Y"))&&
          (wp.itemStr2("list_flag").length()!=0))
         {
          errmsg("[名單匯入] 名單類別旗標未選擇 ");
          return;
         }

      if (wp.itemStr2("res_flag").equals("1"))
         {
          if (wp.itemStr2("res_total_cnt").length()==0) wp.itemSet("res_total_cnt","0");
          if (wp.itemNum("res_total_cnt")==0)
             {
              errmsg("[分次贈送期] 要大於0!");
              return;
             }
         }
/*
      if (wp.item_ss("res_flag").equals("2"))
         {
          if (wp.item_ss("exec_s_months").length()==0) wp.item_set("exec_s_months","0");
          if (wp.item_num("exec_s_months")==0)
             {
              errmsg("[抵用方式] 回灌當月後第 N 個月要大於0!");
              return;
             }
         }
*/
      if (wp.itemStr2("add_vouch_no").length()>0)
         {
          strSql = "select std_vouch_no "
                 + " from  gen_sys_vouch "
                 + " where td_vouch_no =  ? "
                 ;
          Object[] param = new Object[] {wp.itemStr2("add_vouch_no")};
          sqlSelect(strSql,param);

          if (sqlRowNum <= 0)
             {
              errmsg("[新增標準分錄代碼] 並未建參數!");
              return;
             }
         }
      if (wp.itemStr2("rem_vouch_no").length()>0)
         {
          strSql = "select std_vouch_no "
                 + " from  gen_sys_vouch "
                 + " where td_vouch_no =  ? "
                 ;
          Object[] param = new Object[] {wp.itemStr2("rem_vouch_no")};
          sqlSelect(strSql,param);

          if (sqlRowNum <= 0)
             {
              errmsg("[移除標準分錄代碼] 並未建參數!");
              return;
             }
         }
     }


  if ((!wp.itemStr2("control_tab_name").equals(orgControlTabName))&&
      (wp.itemStr2("aud_type").equals("A")))
    {
     if (wp.itemStr2("fund_code").length()<4)
        {
         errmsg("現金回饋代碼長度至少要 4碼!");
         return;
        }
      strSql = "select type_name "
             + " from vmkt_fund_name "
             + " where fund_code =  ? "
             ;
      Object[] param = new Object[] {wp.itemStr2("fund_code")};
      sqlSelect(strSql,param);

      if (sqlRowNum > 0)
         {
          errmsg("["+colStr("type_name")+"] 已使用本現金回饋代碼!");
          return;
         }
      strSql = "select payment_type "
             + " from ptr_payment "
             + " where payment_type =  ? "
             ;
      param = new Object[] {wp.itemStr2("fund_code").substring(0,4)};
      sqlSelect(strSql,param);

      if (sqlRowNum <= 0)
         {
          errmsg("現金回饋前4碼在ptrm0030繳款類別參數查無資料！");
          return;
         }

     }

  int checkInt = checkDecnum(wp.itemStr2("feedback_lmt"),12,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("回饋上限： 格式超出範圍 : 整數[12]位 小數[2]位");
      if (checkInt==2) 
         errmsg("回饋上限： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("回饋上限： 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("cancel_rate"),4,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("抵用範圍：* 比率 格式超出範圍 : 整數[4]位 小數[2]位");
      if (checkInt==2) 
         errmsg("抵用範圍：* 比率 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("抵用範圍：* 比率 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("move_cond"))
     {
      errmsg("抵用餘額撥入溢付款: 不可空白");
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
          + " fund_code, "
          + " apr_flag, "
          + " aud_type, "
          + " fund_name, "
          + " effect_months, "
          + " stop_flag, "
          + " list_cond, "
          + " list_flag, "
          + " list_feedback_date, "
          + " add_vouch_no, "
          + " rem_vouch_no, "
          + " acct_type_sel, "
          + " group_code_sel, "
          + " group_oppost_cond, "
          + " feedback_lmt, "
          + " res_flag, "
          + " res_total_cnt, "
          + " exec_s_months, "
          + " move_cond, "
          + " bil_mcht_cond, "
          + " merchant_sel, "
          + " mcht_group_sel, "
          + " issue_a_months, "
          + " mcode, "
          + " cancel_rate, "
          + " cancel_scope, "
          + " cancel_event, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
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
        wp.itemStr2("fund_name"),
        wp.itemNum("effect_months"),
        wp.itemStr2("stop_flag"),
        wp.itemStr2("list_cond"),
        wp.itemStr2("list_flag"),
        colStr("list_feedback_date"),
        wp.itemStr2("add_vouch_no"),
        wp.itemStr2("rem_vouch_no"),
        wp.itemStr2("acct_type_sel"),
        wp.itemStr2("group_code_sel"),
        wp.itemStr2("group_oppost_cond"),
        wp.itemNum("feedback_lmt"),
        wp.itemStr2("res_flag"),
        wp.itemNum("res_total_cnt"),
        wp.itemNum("exec_s_months"),
        wp.itemStr2("move_cond"),
        wp.itemStr2("bil_mcht_cond"),
        wp.itemStr2("merchant_sel"),
        wp.itemStr2("mcht_group_sel"),
        wp.itemNum("issue_a_months"),
        wp.itemNum("mcode"),
        wp.itemNum("cancel_rate"),
        wp.itemStr2("cancel_scope"),
        wp.itemStr2("cancel_event"),
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

  strSql = "insert into MKT_IMLOAN_LIST_T "
         + "select * "
         + "from MKT_IMLOAN_LIST "
         + "where fund_code = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr2("fund_code"),
     };

  wp.dupRecord = "Y";
  sqlExec(strSql, param , true);


   return 1;
 }
// ************************************************************************
 public int dbInsertI2T()
 {
   msgOK();

  strSql = "insert into MKT_PARM_DATA_T "
         + "select * "
         + "from MKT_PARM_DATA "
         + "where table_name  =  'MKT_LOAN_PARM' "
         + "and   data_key = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr2("fund_code"),
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
         + "fund_name = ?, "
         + "effect_months = ?, "
         + "stop_flag = ?, "
         + "list_cond = ?, "
         + "list_flag = ?, "
         + "add_vouch_no = ?, "
         + "rem_vouch_no = ?, "
         + "acct_type_sel = ?, "
         + "group_code_sel = ?, "
         + "group_oppost_cond = ?, "
         + "feedback_lmt = ?, "
         + "res_flag = ?, "
         + "res_total_cnt = ?, "
         + "exec_s_months = ?, "
         + "move_cond = ?, "
         + "bil_mcht_cond = ?, "
         + "merchant_sel = ?, "
         + "mcht_group_sel = ?, "
         + "issue_a_months = ?, "
         + "mcode = ?, "
         + "cancel_rate = ?, "
         + "cancel_scope = ?, "
         + "cancel_event = ?, "
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
     wp.itemStr2("fund_name"),
     wp.itemNum("effect_months"),
     wp.itemStr2("stop_flag"),
     wp.itemStr2("list_cond"),
     wp.itemStr2("list_flag"),
     wp.itemStr2("add_vouch_no"),
     wp.itemStr2("rem_vouch_no"),
     wp.itemStr2("acct_type_sel"),
     wp.itemStr2("group_code_sel"),
     wp.itemStr2("group_oppost_cond"),
     wp.itemNum("feedback_lmt"),
     wp.itemStr2("res_flag"),
     wp.itemNum("res_total_cnt"),
     wp.itemNum("exec_s_months"),
     wp.itemStr2("move_cond"),
     wp.itemStr2("bil_mcht_cond"),
     wp.itemStr2("merchant_sel"),
     wp.itemStr2("mcht_group_sel"),
     wp.itemNum("issue_a_months"),
     wp.itemNum("mcode"),
     wp.itemNum("cancel_rate"),
     wp.itemStr2("cancel_scope"),
     wp.itemStr2("cancel_event"),
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

   strSql = "delete MKT_IMLOAN_LIST_T "
          + "WHERE fund_code = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr2("fund_code"),
     };

   sqlExec(strSql,param,true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 MKT_IMLOAN_LIST_T 錯誤");

   return rc;

 }
// ************************************************************************
 public int dbInsertD2T()
 {
   msgOK();

   strSql = "delete MKT_PARM_DATA_T "
         + " where table_name  =  'MKT_LOAN_PARM' "
          + "and   data_key = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr2("fund_code"),
     };

   sqlExec(strSql,param,true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 MKT_PARM_DATA_T 錯誤");

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
 public int dbInsertI4() 
 {
   msgOK();

  strSql = "insert into MKT_IMLOAN_LIST_T ( "
          + "fund_code,"
          + " mod_time, "
          + " mod_pgm "
          + ") values ("
          + "?," 
          + " sysdate, "
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr2("fund_code"),
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增 MKT_IMLOAN_LIST_T 錯誤 (dbInsertI4())");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD4()
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr2("fund_code")
     };
   if (sqlRowcount("MKT_IMLOAN_LIST_T" 
                   , "where fund_code = ? "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_IMLOAN_LIST_T "
          + "where fund_code = ?  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI2() 
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm4070_actp"))
      dataType = "1" ;
   if (wp.respHtml.equals("mktm4070_gpcd"))
      dataType = "2" ;
   if (wp.respHtml.equals("mktm4070_aaa1"))
      dataType = "4" ;
  strSql = "insert into MKT_PARM_DATA_T ( "
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
          + "'MKT_LOAN_PARM', "
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
      wp.itemStr2("fund_code"),
      varsStr("data_code"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增 MKT_PARM_DATA_T 錯誤 (dbInsertI2())");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2() 
 {
   msgOK();

   String data_type="";
   if (wp.respHtml.equals("mktm4070_actp"))
      data_type = "1" ;
   if (wp.respHtml.equals("mktm4070_gpcd"))
      data_type = "2" ;
   if (wp.respHtml.equals("mktm4070_aaa1"))
      data_type = "4" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      data_type, 
      wp.itemStr2("fund_code")
     };
   if (sqlRowcount("MKT_PARM_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_LOAN_PARM' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_PARM_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_LOAN_PARM'  "
          ;
   sqlExec(strSql,param,true);


   return 1;

 }
// ************************************************************************
 public int dbInsertI3() 
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm4070_mrch"))
      dataType = "3" ;
  strSql = "insert into MKT_PARM_DATA_T ( "
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
          + "'MKT_LOAN_PARM', "
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
      wp.itemStr2("fund_code"),
      varsStr("data_code"),
      varsStr("data_code2"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , true);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增 MKT_PARM_DATA_T 錯誤 (dbInsertI3())");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD3() throws Exception
 {
   msgOK();

   String dataType="";
   if (wp.respHtml.equals("mktm4070_mrch"))
      dataType = "3" ;
   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      dataType, 
      wp.itemStr2("fund_code")
     };
   if (sqlRowcount("MKT_PARM_DATA_T" 
                    , "where data_type = ? "
                   + "and   data_key = ? "
                    + "and   table_name = 'MKT_LOAN_PARM' "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_PARM_DATA_T "
          + "where data_type = ? "
          + "and   data_key = ?  "
          + "and   table_name = 'MKT_LOAN_PARM'  "
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
         + "where fund_code = ? "
         ;

  Object[] param =new Object[]
    {
     wp.itemStr2("fund_code")
    };

  sqlExec(strSql, param, true);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbInsertI2Gpcd(String tableName,String[] columnCol,String[] columnDat) throws Exception
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
 public int dbDeleteD2Gpcd(String tableName) throws Exception
 {
  strSql = "delete  "+tableName+" " 
         + "where table_name = ? "
         + "and   data_key = ? "
         + "and   data_type = ? "
         ;

  Object[] param =new Object[]
    {
      "MKT_LOAN_PARM",
      wp.itemStr2("fund_code"),
     "2"
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
      "MKT_LOAN_PARM",
      wp.itemStr2("fund_code"),
     "3"
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
  if (sqlRowNum <= 0) errmsg("新增 ecs_media_errlog 錯誤 (dbInsertEcsMediaErrlog())");

  return rc;
 }
// ************************************************************************
 public int dbInsertEcsNotifyLog(String tranSeqStr,int errorCnt ) 
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
  if (sqlRowNum <= 0) errmsg("新增 ecs_modify_log 錯誤 (dbInsertEcsNotifyLog())");
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
 int listImloanDataCnt(String s1,String s2,String s3,String s4) 
 {
  String isSql = "select count(*) as data_cnt "
                + "from  " + s1 +" "
              + " where  fund_code = ?"
              ;
                
  Object[] param = new Object[] {s3};
  sqlSelect(isSql,param);

  return(Integer.parseInt(colStr("data_cnt")));
 }

// ************************************************************************

}  // End of class
