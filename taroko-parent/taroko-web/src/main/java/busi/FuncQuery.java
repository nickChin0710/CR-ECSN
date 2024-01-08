/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi;

import java.sql.Connection;

public abstract class FuncQuery extends busi.FuncBase {

  public FuncQuery() {}

  public FuncQuery(Connection con1) {
    conn = con1;
  }

  public abstract int querySelect();

  public abstract int dataSelect();

  public abstract void dataCheck();

}