/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/07  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm3120Func extends FuncEdit
{
 private final String PROGNAME = "IBON商品資料維護作業處理程式111-11-30  V1.00.01";
  String kk1,kk2,kk3,kk4;
  String orgControlTabName = "ibn_prog_gift";
  String controlTabName = "ibn_prog_gift_t";

 public Mktm3120Func(TarokoCommon wr)
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
          + " prog_code, "
          + " apr_flag, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " gift_typeno, "
          + " gift_name, "
          + " prd_price, "
          + " exchange_pnt, "
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
      kk1 = wp.itemStr2("prog_s_date").replace("/","");
      kk2 = wp.itemStr2("prog_e_date").replace("/","");
      kk3 = wp.itemStr2("gift_no");
     }
  else
     {
      kk1 = wp.itemStr2("prog_s_date").replace("/","");
      kk2 = wp.itemStr2("prog_e_date").replace("/","");
      kk3 = wp.itemStr2("gift_no");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where prog_s_date = ? "
             +"and   prog_e_date = ? "
             +"and   gift_no = ? "
             ;
      Object[] param = new Object[] {kk1,kk2,kk3};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代碼選擇][兌換期間][~][商品代號] 不可重複("+orgControlTabName+"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where prog_s_date = ? "
             + " and   prog_e_date = ? "
             + " and   gift_no = ? "
             ;
      Object[] param = new Object[] {kk1,kk2,kk3};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[活動代碼選擇][兌換期間][~][商品代號] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if (this.ibAdd)
     {
      if ( (wp.itemStr2("aud_type").equals("D"))&&
          (wp.itemStr2("control_tab_name").equals(orgControlTabName)))
         {
          errmsg("已覆核資料, 只可修改不可刪除 !");
          return;
         }
     }
   kk1 = kk1.replace("/","");
   kk2 = kk2.replace("/","");

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

   if ((this.ibDelete)||
       (wp.itemStr2("aud_type").equals("D"))) return;

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr2("control_tab_name").equals(orgControlTabName))
      if ((wp.sysDate.compareTo(wp.itemStr2("prog_e_date"))>0)||
          (wp.sysDate.compareTo(wp.itemStr2("gift_e_date"))>0))
        {
          errmsg("效期已過, 資料不得異動");
          return;
         }

      if (!wp.itemStr2("control_tab_name").equals(orgControlTabName))
      if ((wp.sysDate.compareTo(wp.itemStr2("prog_e_date"))>0)||
          (wp.sysDate.compareTo(wp.itemStr2("gift_e_date"))>0))
        { 
          errmsg("兌換期間或上架有效期間, 不得小於系統日");
          return;
         }

      if (wp.itemStr2("prd_price").length()==0)
         wp.itemSet("prd_price","0");
      if (wp.itemStr2("exchange_pnt").length()==0)
         wp.itemSet("exchange_pnt","0");

      if (wp.itemNum("prd_price")==0)
        {
          errmsg("商品零售價不得為 0");
          return;
         }

      if (wp.itemNum("exchange_pnt")==0)
        {
          errmsg("兌換點數不得為 0");
          return;
         }

     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (!wp.itemEmpty("gift_s_date")&&(!wp.itemEmpty("gift_e_date")))
      if (wp.itemStr2("gift_s_date").compareTo(wp.itemStr2("gift_e_date"))>0)
         {
          errmsg("上架有效期間:["+wp.itemStr2("gift_s_date")+"]>["+wp.itemStr2("gift_e_date")+"] 起迄值錯誤!");
          return;
         }
     }

  int checkInt = checkDecnum(wp.itemStr2("prd_price"),6,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("商品零售價: 格式超出範圍 : 整數[6]位 小數[2]位");
      if (checkInt==2) 
         errmsg("商品零售價: 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("商品零售價: 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("gift_s_date"))
     {
      errmsg("上架有效期間: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("gift_e_date"))
     {
      errmsg("~ 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("gift_name"))
     {
      errmsg("商品名稱: 不可空白");
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


  strSql= " insert into  " + controlTabName+ " ("
          + " prog_code, "
          + " apr_flag, "
          + " aud_type, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " gift_no, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " gift_typeno, "
          + " gift_name, "
          + " prd_price, "
          + " exchange_pnt, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        wp.itemStr2("prog_code"),
        wp.itemStr2("apr_flag"),
        wp.itemStr2("aud_type"),
        kk1,
        kk2,
        kk3,
        wp.itemStr2("gift_s_date"),
        wp.itemStr2("gift_e_date"),
        wp.itemStr2("gift_typeno"),
        wp.itemStr2("gift_name"),
        wp.itemNum("prd_price"),
        wp.itemNum("exchange_pnt"),
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
         + "gift_s_date = ?, "
         + "gift_e_date = ?, "
         + "gift_typeno = ?, "
         + "gift_name = ?, "
         + "prd_price = ?, "
         + "exchange_pnt = ?, "
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
     wp.itemStr2("gift_s_date"),
     wp.itemStr2("gift_e_date"),
     wp.itemStr2("gift_typeno"),
     wp.itemStr2("gift_name"),
     wp.itemNum("prd_price"),
     wp.itemNum("exchange_pnt"),
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

}  // End of class
