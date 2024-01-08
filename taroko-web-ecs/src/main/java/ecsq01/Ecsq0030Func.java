/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/05/09  V1.00.01   Ray Ho        Initial                              *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*
* 109-12-28  V1.00.03  Justin       parameterize sql
***************************************************************************/
package ecsq01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsq0030Func extends FuncEdit {
  private String progname = "提醒事項紀錄查詢處理程式109/12/28 V1.00.03";
  String controlTabName = "ecs_notify_log";

  public Ecsq0030Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
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
    if (!this.ibAdd) {
    }


    if (this.isAdd())
      return;

    // -other modify-
    sqlWhere =
        "where hex(rowid) = ? and nvl(mod_seqno,0)= ? " ;

    if (this.isOtherModify(controlTabName, sqlWhere, new Object[] {wp.itemStr("rowid"), wp.modSeqno()})) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    return 1;
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

} // End of class
