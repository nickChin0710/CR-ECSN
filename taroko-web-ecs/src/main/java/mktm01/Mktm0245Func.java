/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/04/20  V1.00.01   Allen Ho      Initial                              *
* 111/11/29  V1.00.02   Yang Bo    sync code from mega                     *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0245Func extends FuncEdit
{
 private final String PROGNAME = "高鐵生檔不送簡訊清單處理程式111/11/29  V1.00.02";
  String kk1;
  String controlTabName = "ptr_sys_idtab";

 public Mktm0245Func(TarokoCommon wr)
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
  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck()
 {
  if (this.ibAdd)
     {
      kk1 = wp.itemStr("kk_wf_id");
     }
  else
     {
      kk1 = wp.itemStr("wf_id");
     }
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where wf_id = ? "
             + " and   wf_type  =  'GIFT_TYPENO' "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[紅利商品類別] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
         }
     }


  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("wf_useredit"))
     {
      errmsg("停用註記: 不可空白");
      return;
     }


  if (this.isAdd()) return;

 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;


  strSql= " insert into  " + controlTabName + " ("
          + " wf_id, "
          + " wf_desc, "
          + " wf_useredit, "
          + " wf_type, "
          + " mod_seqno, "
          + " mod_time,mod_user,mod_pgm "
          + " ) values ("
          + "?,?,?,"
          + "?,"
          + "?,"
          + "sysdate,?,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr("wf_desc"),
        wp.itemStr("wf_useredit"),
        "GIFT_TYPENO",
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
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " + controlTabName + " set "
         + "wf_desc = ?, "
         + "wf_useredit = ?, "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr("wf_desc"),
     wp.itemStr("wf_useredit"),
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
