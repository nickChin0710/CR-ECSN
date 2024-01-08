/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/04  V1.00.01   Allen Ho      Initial                              *
* 111/11/30  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0260Func extends FuncEdit
{
 private final String PROGNAME = "紅利贈品廠商維護作業處理程式111/11/30  V1.00.02";
  String kk1;
  String orgControlTabName = "mkt_vendor";
  String controlTabName = "mkt_vendor_t";

 public Mktm0260Func(TarokoCommon wr)
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
          + " vendor_name, "
          + " disable_flag, "
          + " id_no, "
          + " name, "
          + " sub_cname, "
          + " out_days, "
          + " tel_no, "
          + " contact_id, "
          + " contact_tel, "
          + " area_code, "
          + " address1, "
          + " address2, "
          + " address3, "
          + " address4, "
          + " address5, "
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
      kk1 = wp.itemStr("vendor_no");
      if (empty(kk1))
         {
          errmsg("廠商代號 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr("vendor_no");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where vendor_no = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[廠商代號] 不可重複("+ orgControlTabName +"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where vendor_no = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[廠商代號] 不可重複("+ controlTabName +") ,請重新輸入!");
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


  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("vendor_name"))
     {
      errmsg("廠商名稱: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("tel_no"))
     {
      errmsg("廠商電話: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("contact_id"))
     {
      errmsg("聯絡人姓名: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("contact_tel"))
     {
      errmsg("聯絡人電話: 不可空白");
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
          + " vendor_no, "
          + " apr_flag, "
          + " aud_type, "
          + " vendor_name, "
          + " disable_flag, "
          + " id_no, "
          + " name, "
          + " sub_cname, "
          + " out_days, "
          + " tel_no, "
          + " contact_id, "
          + " contact_tel, "
          + " area_code, "
          + " address1, "
          + " address2, "
          + " address3, "
          + " address4, "
          + " address5, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
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
        wp.itemStr("vendor_name"),
        wp.itemStr("disable_flag"),
        wp.itemStr("id_no"),
        wp.itemStr("name"),
        wp.itemStr("sub_cname"),
        wp.itemNum("out_days"),
        wp.itemStr("tel_no"),
        wp.itemStr("contact_id"),
        wp.itemStr("contact_tel"),
        wp.itemStr("area_code"),
        wp.itemStr("address1"),
        wp.itemStr("address2"),
        wp.itemStr("address3"),
        wp.itemStr("address4"),
        wp.itemStr("address5"),
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
         + "vendor_name = ?, "
         + "disable_flag = ?, "
         + "id_no = ?, "
         + "name = ?, "
         + "sub_cname = ?, "
         + "out_days = ?, "
         + "tel_no = ?, "
         + "contact_id = ?, "
         + "contact_tel = ?, "
         + "area_code = ?, "
         + "address1 = ?, "
         + "address2 = ?, "
         + "address3 = ?, "
         + "address4 = ?, "
         + "address5 = ?, "
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
     wp.itemStr("vendor_name"),
     wp.itemStr("disable_flag"),
     wp.itemStr("id_no"),
     wp.itemStr("name"),
     wp.itemStr("sub_cname"),
     wp.itemNum("out_days"),
     wp.itemStr("tel_no"),
     wp.itemStr("contact_id"),
     wp.itemStr("contact_tel"),
     wp.itemStr("area_code"),
     wp.itemStr("address1"),
     wp.itemStr("address2"),
     wp.itemStr("address3"),
     wp.itemStr("address4"),
     wp.itemStr("address5"),
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
