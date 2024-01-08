/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
******************************************************************************/
package ofcapp;
/** DataBean 公用程式:
 * 2019-1216   JH    initial
 * */

import java.util.HashMap;

public class DataBean {
  private HashMap<String, String> colHash = new HashMap<String, String>();

  public void dataClear() {
    if (colHash != null) {
      colHash.clear();
    } else {
      colHash = new HashMap<String, String>();
    }
  }

  public void colSet(String col, Object obj1) {
    if (nvl(col).length() == 0)
      return;

    String col1 = nvl(col).toUpperCase();
    try {
      colHash.remove(col1);
      if (obj1 == null) {
        colHash.put(col1, "");
      } else {
        colHash.put(col1, nvl(obj1.toString()));
      }
    } catch (Exception ex) {
      expHandle("col_set", ex);
    }
  }

  public String colStr(String col) {
    String ss = "";
    try {
      ss = (String) colHash.get(col.trim().toUpperCase());
    } catch (Exception ex) {
      expHandle("sss=" + col, ex);
    }
    return nvl(ss);
  }

  public String colNvl(String col, String strName) {
    String colName = colStr(col);
    if (empty(colName))
      return strName;
    return colName;
  }

  public double num(String col) {
    String ss = "";
    try {
      ss = (String) colHash.get(col.trim().toUpperCase());
      return Double.parseDouble(nvl(ss, "0"));
    } catch (Exception ex) {
      expHandle("col_num", ex);
    }
    return 0;
  }

  public int colInt(String col) {
    String ss = "";
    try {
      ss = (String) colHash.get(col.trim().toUpperCase());
      return Integer.parseInt(nvl(ss, "0"));
    } catch (Exception ex) {
      expHandle("col_int", ex);
    }
    return 0;
  }

  public boolean colEq(String col, String strName) {
    if (col == null || strName == null) {
      return false;
    }
    return (colStr(col).equals(strName));
  }

  public int colLen(String col) {
    return colStr(col).length();
  }

  public boolean colEmpty(String col) {
    return (colStr(col).length() == 0);
  }

  // --mod-xxx--------------------------
  public void modUser(String strName) {
    colSet("mod_user", strName);
  }

  public void modPgm(String strName) {
    colSet("mod_pgm", strName);
  }

  public void modSeqno(String strName) {
    colSet("mod_seqno", strName);
  }

  public void rowid(String strName) {
    colSet("rowid", strName);
  }

  // --String-func----------------------
  private void expHandle(String strName, Exception ex) {
    System.out.println("-DDD->" + this.getClass().getSimpleName() + ":" + strName);
  }

  private boolean empty(String strName) {
    if (strName == null || strName.trim().length() == 0) {
      return true;
    }
    return false;
  }

  private String nvl(String strName) {
    if (strName == null) {
      return "";
    }
    return strName.trim();
  }

  private String nvl(String strName, String colName) {
    if (strName == null) {
      if (colName == null) {
        return "";
      }
      return colName;
    }
    return strName.trim();
  }

}
