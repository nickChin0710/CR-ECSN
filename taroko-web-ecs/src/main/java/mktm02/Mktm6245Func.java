/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/07  V1.00.01   Allen Ho      Initial                              *
* 111/12/01  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6245Func extends FuncEdit
{
 private final String PROGNAME = "紅利特殊商品資料檔處理程式109/12/07 V1.00.01";
  String kk1,kk2;
  String orgControlTabName = "mkt_spec_gift";
  String controlTabName = "mkt_spec_gift_t";

 public Mktm6245Func(TarokoCommon wr)
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
          + " gift_name, "
          + " gift_type, "
          + " disable_flag, "
          + " cash_value, "
          + " effect_months, "
          + " vendor_no, "
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
      kk1 = wp.itemStr("gift_no");
      if (empty(kk1))
         {
          errmsg("贈品代號 不可空白");
          return;
         }
      kk2 = wp.itemStr("gift_group");
      if (empty(kk2))
         {
          errmsg("歸屬群組 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr("gift_no");
      kk2 = wp.itemStr("gift_group");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where gift_no = ? "
             +"and   gift_group = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[贈品代號][歸屬群組] 不可重複("+ orgControlTabName +"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where gift_no = ? "
             + " and   gift_group = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[贈品代號][歸屬群組] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
         }
     }

  if (!wp.itemStr("disable_flag").equals("Y")) wp.itemSet("disable_flag","N");

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

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemStr("cash_value").length()==0)
         wp.itemSet("cash_value","0");
      if (wp.itemNum("cash_value")==0)
         {
          errmsg("贈品價值不可為 0");
          return;
         }
      if (wp.itemStr("gift_type").equals("3"))
         {
          if (wp.itemStr("effect_months").length()==0)
             wp.itemSet("effect_months","0");
          if (wp.itemNum("effect_months")==0)
             {
              errmsg("電子商品有效月數 不可為 0");
              return;
             }
         }
      if (wp.itemStr("vendor_no").length()==0)
         {
          errmsg("供應廠商代碼必須輸入");
          return;
         }
      if ((wp.colStr("disable_flag").equals("Y"))&&
          (wp.itemStr("disable_flag").equals("Y")))
         {
          errmsg("已停用資料不可更新 !");
          return;
         }

     }

  int checkInt = checkDecnum(wp.itemStr("cash_value"),6,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("贈品價值： 格式超出範圍 : 整數[6]位 小數[2]位");
      if (checkInt==2) 
         errmsg("贈品價值： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("贈品價值： 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("gift_name"))
     {
      errmsg("贈品名稱： 不可空白");
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


  strSql= " insert into  " + controlTabName + " ("
          + " gift_no, "
          + " apr_flag, "
          + " aud_type, "
          + " gift_group, "
          + " gift_name, "
          + " gift_type, "
          + " disable_flag, "
          + " cash_value, "
          + " effect_months, "
          + " vendor_no, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,"
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
        wp.itemStr("gift_name"),
        wp.itemStr("gift_type"),
        wp.itemStr("disable_flag"),
        wp.itemNum("cash_value"),
        wp.itemNum("effect_months"),
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
         + "gift_name = ?, "
         + "gift_type = ?, "
         + "disable_flag = ?, "
         + "cash_value = ?, "
         + "effect_months = ?, "
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
     wp.itemStr("gift_name"),
     wp.itemStr("gift_type"),
     wp.itemStr("disable_flag"),
     wp.itemNum("cash_value"),
     wp.itemNum("effect_months"),
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
 public int checkDecnum(String decStr, int colLength, int colScale)
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
