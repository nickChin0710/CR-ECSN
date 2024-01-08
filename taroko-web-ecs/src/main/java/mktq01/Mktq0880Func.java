/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/01/15  V1.00.05   Allen Ho      Initial                              *
* 111/03/01  V1.00.06   jiangyingdong  sync code from mega                 *
*                                                                          *
***************************************************************************/
package mktq01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0880Func extends FuncEdit
{
 private  String PROGNAME = "通路活動匯入名單查詢作業處理程式110/01/15 V1.00.01";
  String controlTabName = "mkt_imchannel_list";

 public Mktq0880Func(TarokoCommon wr)
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
 public int dataSelect() {
  // TODO Auto-generated method stub
  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck() {
  if (!this.ibAdd)
     {
     }


  if (this.isAdd()) return;

  //-other modify-
  sqlWhere = "where rowid = x'" + wp.itemStr("rowid") +"'"
            + " and nvl(mod_seqno,0)=" + wp.modSeqno();

  if (this.isOtherModify(controlTabName, sqlWhere))
     {
      errmsg("請重新查詢 !");
      return;
     }
 }
// ************************************************************************
 @Override
 public int dbInsert() {
  return 1 ;
 }
// ************************************************************************
 @Override
 public int dbUpdate() {
  return rc;
}
// ************************************************************************
 @Override
 public int dbDelete() {
  return 1;
 }
// ************************************************************************

}  // End of class
