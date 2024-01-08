/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*                                                                            *  
******************************************************************************/
package ecsfunc;

import java.sql.Connection;

public class IdDescRsk extends taroko.base.BaseSQL {
  taroko.base.CommString commString = new taroko.base.CommString();

  public String limitAdjReason(Connection con, String adjReason) {
    if (commString.empty(adjReason))
      return "";

    String sql1 = "select wf_desc from ptr_sys_idtab" + " where wf_type like ?" + " and wf_id =?";

    try {
      sqlSelect(con, sql1, new Object[] {"ADJ_REASON%", adjReason});
    } catch (Exception ex) {
    }
    if (sqlRowNum > 0) {
      return sqlStr("wf_desc");
    }

    return "";
  }

}
