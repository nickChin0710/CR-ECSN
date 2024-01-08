/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/22  V1.00.01   Allen Ho      Initial                              *
* 109-04-29  V1.00.02  Tanwei        updated for project coding standard
* 109-12-24  V1.00.03  Justin          parameterize sql
***************************************************************************/
package smsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsq0010Func extends FuncEdit {
  private String PROGNAME = "簡訊查詢處理程式109/12/24 V1.00.03";
  String controlTabName = "sms_msg_dtl";

  public Smsq0010Func(TarokoCommon wr) {
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
        "where hex(rowid) = ? and nvl(mod_seqno,0)= ? ";
    

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
