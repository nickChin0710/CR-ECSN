/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei       coding standard      *
*  109-12-25  V1.00.02  Justin       zz -> comm
*  110-01-08  V1.00.03  tanwei       修改意義不明確變量                                                                        * 
******************************************************************************/
package ecsfunc;

import taroko.com.TarokoCommon;

public class HiData {
  private taroko.base.CommString commString = new taroko.base.CommString();

  private String cardnoDb = "", cardnoHh = "";
  private boolean bcardno = false;
  private String idnoDb = "", idnohh = "";
  private boolean bidno = false;
  private String idnameDb = "", idnamehh = "";
  private boolean bIdname = false;

  public void hhCardno(taroko.com.TarokoCommon wp, int num, String coldb, String colhh) {
    wp.colSet(num, colhh, commString.hideCardNo(wp.colStr(num, coldb)));
  }

  public void hhIdno(taroko.com.TarokoCommon wp, int num, String coldb, String colhh) {
    wp.colSet(num, colhh, commString.hideIdno(wp.colStr(num, coldb)));
  }

  public void hhIdname(taroko.com.TarokoCommon wp, int num, String coldb, String colhh) {
    wp.colSet(num, colhh, commString.hideIdnoName(wp.colStr(num, coldb)));
  }

  public void hhCardno(String col) {
    setHhcol("cardno", col, col);
  }

  public void hhCardno(String coldb, String colhh) {
    setHhcol("cardno", coldb, colhh);
  }

  public void hhIdno(String col) {
    setHhcol("idno", col, col);
  }

  public void hhIdno(String coldb, String colhh) {
    setHhcol("idno", coldb, colhh);
  }

  public void hhIdname(String col) {
    setHhcol("idname", col, col);
  }

  public void hhIdname(String coldb, String colhh) {
    setHhcol("idname", coldb, colhh);
  }

  private void setHhcol(String atype, String coldb, String colhh) {
    if (commString.empty(coldb) && commString.empty(colhh))
      return;

    if (commString.eqAny(atype, "cardno")) {
      cardnoDb = commString.nvl(coldb, colhh);
      cardnoHh = commString.nvl(colhh, coldb);
      bcardno = true;
    } else if (commString.eqIgno(atype, "idno")) {
      idnoDb = commString.nvl(coldb, colhh);
      idnohh = commString.nvl(colhh, coldb);
      bidno = true;
    } else if (commString.eqIgno(atype, "idname")) {
      idnameDb = commString.nvl(coldb, colhh);
      idnamehh = commString.nvl(colhh, coldb);
      bIdname = true;
    }

  }

  public void hidataWp(taroko.com.TarokoCommon wp) {
    hidataWp(wp, 0);
  }

  public void hidataWp(taroko.com.TarokoCommon wp, int aiList) {
    int llNrow = wp.listCount[aiList];
    if (llNrow <= 0)
      return;

    if (bcardno || bcardno || bIdname) {
    } else
      return;

    for (int ii = 0; ii < llNrow; ii++) {
      if (bcardno) {
        wp.colSet(ii, cardnoHh, commString.hideCardNo(wp.colStr(ii, cardnoDb)));
      }
      if (bidno) {
        wp.colSet(ii, idnohh, commString.hideIdno(wp.colStr(ii, idnoDb)));
      }
      if (bIdname) {
        wp.colSet(ii, idnamehh, commString.hideIdnoName(wp.colStr(ii, idnameDb)));
      }
    }
  }

}
