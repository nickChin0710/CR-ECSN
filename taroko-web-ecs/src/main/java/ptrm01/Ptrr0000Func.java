/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-06-19  V1.00.00             program initial                            *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrr0000Func extends FuncEdit {
  String mKkProgramCode = "";

  public Ptrr0000Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {

  }

  @Override
  public int dbInsert() {
    return 0;
  }

  @Override
  public int dbUpdate() {
    return 0;
  }

  @Override
  public int dbDelete() {
    return 0;
  }

}
