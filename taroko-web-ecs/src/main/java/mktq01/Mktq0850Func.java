/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/27  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
***************************************************************************/
package mktq01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0850Func extends FuncEdit {
  private String PROGNAME = "通路活動卡人消費查詢作業處理程式108/08/27 V1.00.01";
  String controlTabName = "mkt_channel_bill";

  public Mktq0850Func(TarokoCommon wr) {
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
        "where rowid = x'" + wp.itemStr("rowid") + "'" + " and nvl(mod_seqno,0)=" + wp.modSeqno();

    if (this.isOtherModify(controlTabName, sqlWhere)) {
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
