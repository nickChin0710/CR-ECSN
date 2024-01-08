/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 108/01/29  V1.00.01   Ray Ho        Initial                              *
 * 109-04-20  v1.00.02   Andy          Update add throws Exception          *
 * 111/10/28  V1.00.03  jiangyigndong  updated for project coding standard  *
 ***************************************************************************/
package cycq01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycq2010Func extends FuncEdit
{
  private  String PROGNAME = "帳戶利息、利率折扣查詢作業處理程式108/01/29 V1.00.01";
  String control_tab_name = "act_int_hst";

  public Cycq2010Func(TarokoCommon wr)
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
