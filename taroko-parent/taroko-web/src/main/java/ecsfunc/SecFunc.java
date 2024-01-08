/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-13  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*  109-12-24  V1.00.02  Justin      parameterize sql
******************************************************************************/
package ecsfunc;

public class SecFunc extends taroko.base.BaseSQL {

  taroko.com.TarokoCommon wp;

  public SecFunc(taroko.com.TarokoCommon wr) {
    wp = wr;
  }

  public void reportHeaderFooter(String pgmid) throws Exception {
    wp.colSet(0, "report_header", "");
    wp.colSet(1, "report_header", "");
    wp.colSet(0, "report_footer", "");
    wp.colSet(1, "report_footer", "");
    if (empty(pgmid))
      return;

    String sql1 =
        "select report_header1, report_header2" + ", report_footer1, report_footer2"
            + " from sec_window" + " where 1=1 and wf_winid = ? ";
    this.sqlSelect(wp.getConn(), sql1, new Object[] {pgmid});
    if (sqlRowNum > 0) {
      wp.colSet(0, "report_header", sqlStr("report_header1"));
      wp.colSet(1, "report_header", sqlStr("report_header2"));
      wp.colSet(0, "report_footer", sqlStr("report_footer1"));
      wp.colSet(1, "report_footer", sqlStr("report_footer2"));
    }
    return;
  }

}
