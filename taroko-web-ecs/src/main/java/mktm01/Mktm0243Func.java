/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/03/31  V1.00.07   Allen Ho      Initial                              *
* 111/11/29  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0243Func extends FuncEdit
{
 private final String PROGNAME = "處理程式111/11/29  V1.00.02";
  String kk1;
  String orgControlTabName = "mkt_gift_stock";
  String controlTabName = "mkt_gift_stock_t";

 public Mktm0243Func(TarokoCommon wr)
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
          + " adjust_flag, "
          + " adjust_count, "
          + " web_count, "
          + " stock_desc, "
          + " stock_comment, "
          + " m_supply_cnt, "
          + " m_use_cnt, "
          + " m_web_cnt, "
          + " a_supply_cnt, "
          + " a_use_cnt, "
          + " a_web_cnt, "
          + " p_supply_cnt, "
          + " p_use_cnt, "
          + " p_web_cnt, "
          + " m_supply_cnt, "
          + " m_use_cnt, "
          + " m_web_cnt, "
          + " create_date, "
          + " create_time, "
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
     }
  else
     {
      kk1 = wp.itemStr("gift_no");
     }

  Object[] param = null;
  if (this.ibAdd)
     {
      if (wp.itemStr("control_tab_name").equals(orgControlTabName))
         {
          errmsg("已覆核資料, 只可查詢不可異動 !");
          return;
         }
      if (wp.itemStr("gift_no").length()==0)
         {
          errmsg("商品代號必須輸入 !");
          return;
         }

      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where gift_no = ? "
             ;
      param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[商品代號] 未覆核不可再新增!");
          return;
         }

      if (wp.itemStr("adjust_count").length()==0) wp.itemSet("adjust_count","0");
      if (wp.itemStr("web_count").length()==0) wp.itemSet("web_count","0");
      if ((wp.itemNum("adjust_count")==0)&&
          (wp.itemNum("web_count")==0))
         {
          errmsg("[供應異動數量]與[WEB異動數量]不同時為0 !");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      strSql = " select "
             + " decode(gift_type,'3',max_limit_count,supply_count) as m_supply_cnt ,"
             + " decode(gift_type,'3',use_limit_count,use_count) as m_use_cnt ,"
             + " web_sumcnt as m_web_cnt "
             + " from  mkt_gift  "
             + " where gift_no =? "
             ;

      param = new Object[] {kk1};

      sqlSelect(strSql,param);

      if (wp.itemStr("adjust_flag").equals("1"))
         {
          if (colNum("m_supply_cnt")+wp.itemNum("adjust_count")
                                     -colNum("m_use_cnt")
                                     -colNum("m_web_cnt")
                                     -wp.itemNum("web_count") <0)
             {
              errmsg("供應數量["+(int)(colNum("m_supply_cnt")+wp.itemNum("adjust_count"))
                                +"]要大於(已兌數量["
                                + (int)colNum("m_use_cnt")
                                +"]+WEB 數量["
                                +(int)(colNum("m_web_cnt")+wp.itemNum("web_count"))+"]) !");
              return;
             }
         }
      if (wp.itemNum("adjust_count")>0)
         {
          if (wp.itemNum("adjust_count")< wp.itemNum("web_count"))
             {
              errmsg("供應數量"+(int)wp.itemNum("adjust_count")
                                   +"]要大於WEB數量["
                                   +(int)wp.itemNum("web_count")+"]");
              return;
             }
         }
      else if (wp.itemNum("adjust_count")<0) 
         {
          if (wp.itemNum("web_count")!=0)
             {
              errmsg("供應數量與WEB 數量:不可同時異動");
              return;
             }
          if (colNum("m_supply_cnt")+wp.itemNum("adjust_count")
                                     -colNum("m_use_cnt")
                                     -colNum("m_web_cnt")<0)
             {
              errmsg("待兌數量"+(int)(colNum("m_supply_cnt")
                                     -colNum("m_web_cnt")
                                     -colNum("m_use_cnt"))
                               +"]小於異動累計數量["
                               +(int)wp.itemNum("adjust_count")+"]");
              return;
             }
         }
      else
         {
          wp.log("STEP 001 ["+ colNum("m_supply_cnt") + "]");
          wp.log("STEP 002 ["+ wp.itemNum("adjust_count") + "]");
          wp.log("STEP 003 ["+ colNum("m_use_cnt") + "]");
          wp.log("STEP 004 ["+ colNum("m_web_cnt") + "]");
          wp.log("STEP 005 ["+ wp.itemNum("web_count") + "]");
          if (wp.itemNum("web_count")>0)
             {
              if (colNum("m_supply_cnt")-colNum("m_web_cnt")
                                         -wp.itemNum("web_count")<0)
                 {
                  errmsg("待兌數量"+(int)(colNum("m_supply_cnt")-colNum("m_web_cnt"))
                                   +"]小於WEB 異動數量["
                                   +(int)wp.itemNum("web_count")+"]");
                  return;
                 }
             }
          else
             {
              if (colNum("m_web_cnt")+wp.itemNum("web_count")<0)
                 {
                  errmsg("WEB 數量"+(int)colNum("m_web_cnt")
                                   +"]小於WEB 異動數量["
                                   +(int)wp.itemNum("web_count")+"]");
                  return;
                 }
             }
         }
      colSet("m_supply_cnt" , colStr("m_supply_cnt"));
      colSet("m_use_cnt"    , colStr("m_use_cnt"));
      colSet("m_web_cnt"    , colStr("m_web_cnt"));
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


  strSql= " insert into  " + controlTabName + " ("
          + " gift_no, "
          + " aud_type, "
          + " adjust_flag, "
          + " adjust_count, "
          + " web_count, "
          + " stock_desc, "
          + " stock_comment, "
          + " a_supply_cnt, "
          + " a_use_cnt, "
          + " a_web_cnt, "
          + " p_supply_cnt, "
          + " p_use_cnt, "
          + " p_web_cnt, "
          + " m_supply_cnt, "
          + " m_use_cnt, "
          + " m_web_cnt, "
          + " create_date, "
          + " create_time, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr("aud_type"),
        wp.itemStr("adjust_flag"),
        wp.itemNum("adjust_count"),
        wp.itemNum("web_count"),
        wp.itemStr("stock_desc"),
        wp.itemStr("stock_comment"),
        colNum("a_supply_cnt"),
        colNum("a_use_cnt"),
        colNum("a_web_cnt"),
        colNum("p_supply_cnt"),
        colNum("p_use_cnt"),
        colNum("p_web_cnt"),
        colStr("m_supply_cnt"),
        colStr("m_use_cnt"),
        colStr("m_web_cnt"),
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
 @Override
 public int dbUpdate()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " + controlTabName + " set "
         + "adjust_flag = ?, "
         + "adjust_count = ?, "
         + "web_count = ?, "
         + "stock_desc = ?, "
         + "stock_comment = ?, "
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
     wp.itemStr("adjust_flag"),
     wp.itemNum("adjust_count"),
     wp.itemNum("web_count"),
     wp.itemStr("stock_desc"),
     wp.itemStr("stock_comment"),
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

}  // End of class
