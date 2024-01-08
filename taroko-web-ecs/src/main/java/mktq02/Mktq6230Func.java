/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/04/20  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktq02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq6230Func extends FuncEdit
{
 private final String PROGNAME = "專案回饋金紀錄處理程式111-11-30  V1.00.01";
  String controlTabName = "mkt_loan";

 public Mktq6230Func(TarokoCommon wr)
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


  if (this.isAdd()) return;

  //-other modify-
  sqlWhere = "where rowid = x'" + wp.itemStr2("rowid") +"'"
            + " and nvl(mod_seqno,0)=" + wp.modSeqno();

  if (this.isOtherModify(controlTabName, sqlWhere))
     {
      errmsg("請重新查詢 !");
      return;
     }
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
