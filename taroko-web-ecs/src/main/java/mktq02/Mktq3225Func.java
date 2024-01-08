/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/08  V1.00.01   Allen Ho      Initial                              *
* 111/07/20  V1.00.02   machao        新增程式mktq3225                                                                         *
***************************************************************************/
package mktq02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq3225Func extends FuncEdit
{
 private  String PROGNAME = "高鐵標準車廂每月對帳查詢處理程式111/07/20 V1.00.02";
  String control_tab_name = "mkt_thsr_redem";

 public Mktq3225Func(TarokoCommon wr)
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
  if (!this.isAdd())
     {
     }


  if (this.isAdd()) return;

  //-other modify-
  sqlWhere = "where rowid = x'" + wp.itemStr2("rowid") +"'"
            + " and nvl(mod_seqno,0)=" + wp.modSeqno();

  if (this.isOtherModify(control_tab_name, sqlWhere))
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
