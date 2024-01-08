/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/07/03  V1.00.01   Allen Ho      Initial                              *
* 111/12/08  V1.00.02   Yang Bo       update naming rule                   *
***************************************************************************/
package dbmm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmm0010Func extends FuncEdit
{
 private final String PROGNAME = "Debit紅利期限移除參數維護處理程式111/12/08  V1.00.02";
  String controlTabName = "dbm_sysparm";

 public Dbmm0010Func(TarokoCommon wr)
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
  if (!this.ibAdd)
     {
     }
  if (!wp.itemStr("corp_delete_flag").equals("Y")) wp.itemSet("corp_delete_flag","N");

      if (wp.itemStr("effect_months").length()==0) wp.itemSet("effect_months" , "0");
      if (wp.itemNum("effect_months")==0)
         {
          errmsg("[紅利有效月數] 必須大於 0!");
          return;
         }

      if (wp.itemStr("novalid_card_mm").length()==0) wp.itemSet("novalid_card_mm" , "0");
      if (wp.itemNum("novalid_card_mm")==0)
         {
          errmsg("[無有效金融卡月數] 必須大於 0!");
          return;
         }

  if (this.isAdd()) return;

 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  return 1 ;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " + controlTabName + " set "
         + "effect_months = ?, "
         + "novalid_card_mm = ?, "
         + "corp_delete_flag = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemNum("effect_months"),
     wp.itemNum("novalid_card_mm"),
     wp.itemStr("corp_delete_flag"),
     wp.loginUser,
     wp.itemStr("zz_apr_user"),
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
  return 1;
 }
// ************************************************************************

}  // End of class
