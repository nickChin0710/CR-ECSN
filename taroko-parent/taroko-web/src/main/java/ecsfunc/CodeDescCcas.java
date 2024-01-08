/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package ecsfunc;

import java.sql.Connection;
import taroko.base.BaseSQL;

public class CodeDescCcas extends BaseSQL {

  public String oppoReason(Connection con, String oppType, String oppReason) throws Exception {
    if (empty(oppType) || empty(oppReason))
      return "";

    String sql1 =
        "select opp_remark" + " from cca_opp_type_reason" + " where ncc_opp_type =?"
            + " and opp_status =?";
    sqlSelect(con, sql1, new Object[] {oppType, oppReason});

    if (sqlRowNum > 0) {
      return sqlStr("opp_remark");
    }

    return "";
  }

  public String riskType(Connection con, String aRiskType) {
    if (aRiskType.length() == 0)
      return "";
    String sql1 = "select uf_tt_risk_type(?) as tt_risk_type from dual";

    try {
      sqlSelect(con, sql1, new Object[] {aRiskType});
    } catch (Exception ex) {
    }
    if (sqlRowNum > 0) {
      return sqlStr("tt_risk_type");
    }

    return "";
  }

}
