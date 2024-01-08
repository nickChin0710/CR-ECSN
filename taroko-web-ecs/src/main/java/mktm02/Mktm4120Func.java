/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/14  V1.00.01   Ray Ho        Initial                              *
* 109-04-20  v1.00.04   Andy          Update add throws Exception   
* 112-02-16  V1.00.05  Machao      sync from mega & updated for project coding standard       *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm4120Func extends FuncEdit
{
 private final String PROGNAME = "紅利兌換參數檔維護處理程式112/02/16  V1.00.05";
  String controlTabName = "bil_redeem";

 public Mktm4120Func(TarokoCommon wr)
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
  if (!wp.itemStr2("sms_flag").equals("Y")) wp.itemSet("sms_flag","N");
  if (!wp.itemStr2("edm_flag").equals("Y")) wp.itemSet("edm_flag","N");
  if (!wp.itemStr2("line_flag").equals("Y")) wp.itemSet("line_flag","N");

  if (checkDecnum(wp.itemStr2("disc_rate"),3,2)!=0)
     {
      errmsg("最高扣扺比率: 格式超出範圍 : [3][2]");
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

  strSql= "update " +controlTabName + " set "
         + "disc_rate = ?, "
         + "dest_amt = ?, "
         + "unit_point = ?, "
         + "unit_amt = ?, "
         + "disc_amt = ?, "
         + "sms_flag = ?, "
         + "sms_date = ?, "
         + "sms_bonus = ?, "
         + "edm_flag = ?, "
         + "edm_date = ?, "
         + "edm_bonus = ?, "
         + "line_flag = ?, "
         + "line_date = ?, "
         + "line_bonus = ?, "
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
     wp.itemNum("disc_rate"),
     wp.itemNum("dest_amt"),
     wp.itemNum("unit_point"),
     wp.itemNum("unit_amt"),
     wp.itemNum("disc_amt"),
     wp.itemStr2("sms_flag"),
     wp.itemStr2("sms_date"),
     wp.itemNum("sms_bonus"),
     wp.itemStr2("edm_flag"),
     wp.itemStr2("edm_date"),
     wp.itemNum("edm_bonus"),
     wp.itemStr2("line_flag"),
     wp.itemStr2("line_date"),
     wp.itemNum("line_bonus"),
     wp.itemStr2("zz_apr_user"),
     wp.loginUser,
     wp.itemStr2("mod_pgm"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  return 1;
 }
// ************************************************************************
 public int checkDecnum(String decStr,int colLength,int colScale)
 {
  String[]  parts = decStr.split("[.^]");
  if ((parts.length==1&&parts[0].length()>colLength)||
      (parts.length==2&&
       (parts[0].length()>colLength||parts[1].length()>colScale)))
      return(1);
  return(0);
 }
// ************************************************************************

}  // End of class
