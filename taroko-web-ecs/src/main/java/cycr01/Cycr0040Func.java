/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/04/15  V1.00.01   Allen Ho      Initial                              *
 * 111/10/28  V1.00.02  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package cycr01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycr0040Func extends FuncEdit
{
  private  String PROGNAME = "累積消費次數及金額查詢處理程式110/04/15 V1.00.01";
  String control_tab_name = "mkt_card_consume";

  public Cycr0040Func(TarokoCommon wr)
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

    if (this.isOtherModify(control_tab_name, sqlWhere))
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
