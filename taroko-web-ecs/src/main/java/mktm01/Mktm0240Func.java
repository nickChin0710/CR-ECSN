/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/09  V1.00.01   Allen Ho      Initial                              *
* 111/12/05  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0240Func extends FuncEdit
{
 private final String PROGNAME = "紅利贈品資料檔維護作業處理程式111/12/05  V1.00.02";
  String kk1;
  String orgControlTabName = "mkt_gift";
  String controlTabName = "mkt_gift_t";

 public Mktm0240Func(TarokoCommon wr)
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
          + " gift_typeno, "
          + " gift_name, "
          + " disable_flag, "
          + " cash_value, "
          + " gift_type, "
          + " list_price, "
          + " effect_months, "
          + " redem_days, "
          + " max_limit_count, "
          + " use_limit_count, "
          + " net_limit_count, "
          + " fund_code, "
          + " air_type, "
          + " cal_mile, "
          + " supply_count, "
          + " use_count, "
          + " web_sumcnt, "
          + " limit_last_date, "
          + " vendor_no, "
          + " exchg_type, "
          + " disable_flag, "
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
      kk1 = wp.itemStr("gift_no");
      if (empty(kk1))
         {
          errmsg("贈品代號 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr("gift_no");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where gift_no = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[贈品代號] 不可重複("+ orgControlTabName +"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where gift_no = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[贈品代號] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
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

   if (wp.itemStr("aud_type").equals("U"))
      {
        if (wp.itemStr("web_sumcnt").length()==0) wp.itemSet("web_sumcnt","0");
        if ((wp.itemNum("web_sumcnt")!=0)&&
            (wp.itemNum("web_sumcnt")!=colNum("web_sumcnt")))
           {
            errmsg("舊商品轉商城累積數 , 請至mktm0243維護 !");
            return;
           }
      }

   if (wp.itemStr("vendor_no").length()==0)
      {
       errmsg("供應廠商代碼必須選取 !");
       return;
      }

   if ((!wp.itemStr("control_tab_name").equals(orgControlTabName))&&
      (wp.itemStr("aud_type").equals("A")))
      {
      }
   else
      {
       if ((colNum("max_limit_count")!=wp.itemNum("max_limit_count"))||
           (colNum("web_sumcnt")!=wp.itemNum("web_sumcnt"))||
           (colNum("supply_count")!=wp.itemNum("supply_count")))
         {
          errmsg("已覆核之商品數量異動, 請在mktm0243處理 !");
          return;
         }
      }

  if ((this.ibUpdate))
     {
      if (wp.itemStr("control_tab_name").equals(orgControlTabName))
      strSql = "select gift_no "
             + "from mkt_gift_exchgdata " 
             + "where  gift_no  = ? "
             ;
      else
      strSql = "select gift_no "
             + "from mkt_gift_exchgdata_t " 
             + "where  gift_no  = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      
      if (sqlRowNum <= 0)
         {
          errmsg("兌換點數方式"+kk1+"]未輸入資料 !");
          return;
         }
     }
  if ((this.ibAdd)||(this.ibUpdate))
     {
      if ((colStr("disable_flag").equals("Y"))&&
          (wp.itemStr("disable_flag").equals("Y")))
         {
          errmsg("已停用資料不可更新 !["
                +wp.colStr("disable_flag")
                +"]["
                +wp.itemStr("disable_flag")+"]");
          return;
         }
      if (wp.itemStr("air_type").length()!=0)
         {
          if (wp.itemStr("cal_mile").length()==0) wp.itemSet("cal_mile","0");
          if (wp.itemNum("cal_mile")==0)
             {
              errmsg("換算航空哩程數, 不可為0 !");
              return;
             }
         }
      if ((wp.itemStr("gift_type").equals("1"))&&
          (wp.itemStr("fund_code").length()!=0))
         {
          errmsg("贈品類別為商品, 不可輸入基金代碼 !");
          return;
         }
      if ((!wp.itemStr("gift_type").equals("1"))&&
          (wp.itemStr("air_type").length()!=0))
         {
          errmsg("贈品類別不為商品, 不可選航空哩程 !");
          return;
         }
      if ((wp.itemStr("gift_type").equals("2"))&&
          (wp.itemStr("fund_code").length()==0))
         {
          errmsg("贈品類別為基金, 必須輸入基金代碼 !");
          return;
         }
      if (wp.itemStr("cash_value").length()==0) wp.itemSet("cash_value","0");
      if (wp.itemNum("cash_value")==0)
         {
          errmsg("贈品價格, 不可為0 !");
          return;
         }

      if (wp.itemStr("web_sumcnt").length()==0) wp.itemSet("web_sumcnt","0");
      if (wp.itemStr("gift_type").equals("3"))
         {
          wp.itemSet("supply_count","0");
          if (wp.itemStr("effect_months").length()==0) wp.itemSet("effect_months","0");
          if (wp.itemStr("redem_days").length()==0) wp.itemSet("redem_days","0");
          if (((wp.itemNum("effect_months")!=0)&& (wp.itemNum("redem_days")==0))|| 
              ((wp.itemNum("effect_months")==0)&& (wp.itemNum("redem_days")!=0))) 
             {
              errmsg("贈品類別為電子商品, 效期月數與到期清算不可單一有值 !");
              return;
             }
          if (wp.itemStr("max_limit_count").length()==0) wp.itemSet("max_limit_count","0");
          if (wp.itemNum("max_limit_count")==0)
             {
              errmsg("電子商品供應數量不可為0 !");
              return;
             }
          if (wp.itemNum("web_sumcnt")>wp.itemNum("max_limit_count"))
             {
              errmsg("轉商城累積數不可大於電子商品供應數量 !");
              return;
             }
         }
      else if (wp.itemStr("gift_type").equals("1"))
         {
          wp.itemSet("effect_months","0");
          wp.itemSet("redem_days","0");
          wp.itemSet("max_limit_count","0");
          if (wp.itemStr("supply_count").length()==0) wp.itemSet("supply_count","0");
          if (wp.itemNum("supply_count")==0)
             {
              errmsg("一般供應數量不可為0 !");
              return;
             }
          if (wp.itemNum("web_sumcnt")>wp.itemNum("supply_count"))
             {
              errmsg("轉商城累積數不可大於一般商品供應數量 !");
              return;
             }
         }
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
  if (wp.itemEmpty("gift_name"))
     {
      errmsg("贈品名稱： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("cash_value"))
     {
      errmsg("贈品價格： 不可空白");
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

  dbInsertD3T();
  dbInsertI3T();

  strSql= " insert into  " + controlTabName + " ("
          + " gift_no, "
          + " apr_flag, "
          + " aud_type, "
          + " bonus_type, "
          + " gift_typeno, "
          + " gift_name, "
          + " cash_value, "
          + " gift_type, "
          + " list_price, "
          + " effect_months, "
          + " redem_days, "
          + " max_limit_count, "
          + " use_limit_count, "
          + " fund_code, "
          + " air_type, "
          + " cal_mile, "
          + " supply_count, "
          + " use_count, "
          + " web_sumcnt, "
          + " limit_last_date, "
          + " vendor_no, "
          + " exchg_type, "
          + " disable_flag, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "'1','N',"
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
        wp.itemStr("bonus_type"),
        wp.itemStr("gift_typeno"),
        wp.itemStr("gift_name"),
        wp.itemNum("cash_value"),
        wp.itemStr("gift_type"),
        wp.itemNum("list_price"),
        wp.itemNum("effect_months"),
        wp.itemNum("redem_days"),
        wp.itemNum("max_limit_count"),
        wp.itemNum("use_limit_count"),
        wp.itemStr("fund_code"),
        wp.itemStr("air_type"),
        wp.itemNum("cal_mile"),
        wp.itemNum("supply_count"),
        wp.itemNum("use_count"),
        wp.itemNum("web_sumcnt"),
        wp.itemStr("limit_last_date"),
        wp.itemStr("vendor_no"),
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
 public int dbInsertI3T()
 {
   msgOK();

  strSql = "insert into MKT_GIFT_EXCHGDATA_T "
         + "select * "
         + "from MKT_GIFT_EXCHGDATA "
         + "where gift_no = ? "
         + "";

   Object[] param =new Object[]
     {
      wp.itemStr("gift_no"),
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

  strSql= "update " + controlTabName + " set "
         + "apr_flag = ?, "
         + "bonus_type = ?, "
         + "gift_typeno = ?, "
         + "gift_name = ?, "
         + "disable_flag = ?, "
         + "cash_value = ?, "
         + "gift_type = ?, "
         + "list_price = ?, "
         + "effect_months = ?, "
         + "redem_days = ?, "
         + "max_limit_count = ?, "
         + "fund_code = ?, "
         + "air_type = ?, "
         + "cal_mile = ?, "
         + "supply_count = ?, "
         + "web_sumcnt = ?, "
         + "vendor_no = ?, "
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
     wp.itemStr("gift_typeno"),
     wp.itemStr("gift_name"),
     wp.itemStr("disable_flag"),
     wp.itemNum("cash_value"),
     wp.itemStr("gift_type"),
     wp.itemNum("list_price"),
     wp.itemNum("effect_months"),
     wp.itemNum("redem_days"),
     wp.itemNum("max_limit_count"),
     wp.itemStr("fund_code"),
     wp.itemStr("air_type"),
     wp.itemNum("cal_mile"),
     wp.itemNum("supply_count"),
     wp.itemNum("web_sumcnt"),
     wp.itemStr("vendor_no"),
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

  dbInsertD3T();

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
 public int dbInsertD3T()
 {
   msgOK();

   strSql = "delete MKT_GIFT_EXCHGDATA_T "
          + "WHERE gift_no = ? "
          + "";
   //如果沒有資料回傳成功1
   Object[] param = new Object[]
     {
      wp.itemStr("gift_no"),
     };

   sqlExec(strSql,param,false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("刪除 MKT_GIFT_EXCHGDATA_T 錯誤");

   return rc;

 }
// ************************************************************************
 public int dbInsertI3() throws Exception
 {
   msgOK();

  strSql = "insert into MKT_GIFT_EXCHGDATA_T ( "
          + "gift_no,"
          + "card_note,"
          + "group_code,"
          + "exchange_bp,"
          + "exchange_amt,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "?,?,?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr("gift_no"),
      varsStr("card_note"),
      varsStr("group_code"),
      varsStr("exchange_bp"),
      varsStr("exchange_amt"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_GIFT_EXCHGDATA_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD3() throws Exception
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr("gift_no")
     };
   if (sqlRowcount("MKT_GIFT_EXCHGDATA_T" 
                   , "where gift_no = ? "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_GIFT_EXCHGDATA_T "
          + "where gift_no = ?  "
          ;
   sqlExec(strSql,param,false);


   return 1;

 }
// ************************************************************************

}  // End of class
