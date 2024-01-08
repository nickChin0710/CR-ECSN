package com;
/**
 *  dataSet公用程式: V32018-0814.JH
*  109/07/06  V0.00.02    Zuwei     coding standard, rename field method & format                   *
 * 2018-0814:	JH		++sss,nnn,iii()
 * 2018-0615:	JH		++colss, colnum, colint()
 * 110-01-07   V1.00.02    shiyuqi       修改无意义命名                                                                           *
 * */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSetWeb {

  private HashMap<String, String> colHash = new HashMap<String, String>();
  public List<Map<String, String>> colList = null;

  int currRow = -1;

  // --DB-set-value-
  public void colSet(int rr, String col, Object obj1) {
    if (nvl(col).length() == 0)
      return;

    String col1 = nvl(col).toLowerCase();
    if (rr > 0)
      col1 += "-" + rr;
    try {
      if (obj1 == null) {
        colHash.put(col1, "");
      } else {
        colHash.put(col1, nvl(obj1.toString()));
      }
    } catch (Exception ex) {
      expHandle("col_set", ex);
    }
  }

  public void colSet(String col, Object obj1) {
    colSet(0, col, obj1);
  }

  public void dataClear() {
    listClear();
    colClear();
  }

  public int listRows() {
    return colList.size();
  }

  public void listFetch(int ll) {
    list2Col(ll);
  }

  public void list2Col(int ll) {
    // colClear();
    if (ll < colList.size()) {
      colHash = (HashMap<String, String>) colList.get(ll);
      currRow = ll;
    } else
      currRow = -1;
    return;
  }

  public boolean listNext() {
    int ll = currRow + 1;
    list2Col(ll);
    return (currRow >= 0);
  }

  public String colNvl(String col, String param1) {
    String str = colSs(col);
    if (empty(str))
      return param1;
    return str;
  }

  public String sss(String col) {
    String str = "";
    try {
      str = (String) colHash.get(col.trim().toUpperCase());
    } catch (Exception ex) {
      expHandle("col_ss=" + col, ex);
    }
    return str.trim();
  }

  public String colSs(String col) {
    String str = "";
    try {
      str = (String) colHash.get(col.trim().toUpperCase());
    } catch (Exception ex) {
      expHandle("col_ss=" + col, ex);
    }
    return str.trim();
  }

  public String colss(int ll, String col) {
    String str = "";
    try {
      str = (String) colList.get(ll).get(col.trim().toUpperCase());
    } catch (Exception ex) {
      expHandle("col_ss=" + col, ex);
    }
    return nvl(str);
  }
  // public String col_ss(int rr,String col) {
  // if (rr>0)
  // return col_ss(nvl(col).toUpperCase()+"-"+rr);
  //
  // return col_ss(col);
  // }

  public double num(String col) {
    String str = "";
    try {
      str = (String) colHash.get(col.trim().toUpperCase());
      return Double.parseDouble(nvl(str, "0"));
    } catch (Exception ex) {
      expHandle("col_num", ex);
    }
    return 0;
  }

  public double colNum(String col) {
    String str = "";
    try {
      str = (String) colHash.get(col.trim().toUpperCase());
      return Double.parseDouble(nvl(str, "0"));
    } catch (Exception ex) {
      expHandle("col_num", ex);
    }
    return 0;
  }

  public double colnum(int ll, String col) {
    String str = "";
    try {
      str = (String) colList.get(ll).get(col.trim().toUpperCase());
      return Double.parseDouble(nvl(str, "0"));
    } catch (Exception ex) {
      expHandle("colnum=" + col, ex);
    }
    return 0;
  }

  public int colInt(String col) {
    String str = "";
    try {
      str = (String) colHash.get(col.trim().toUpperCase());
      return Integer.parseInt(nvl(str, "0"));
    } catch (Exception ex) {
      expHandle("col_int", ex);
    }
    return 0;
  }

  public double colint(int ll, String col) {
    String str = "";
    try {
      str = (String) colList.get(ll).get(col.trim().toUpperCase());
      return Integer.parseInt(nvl(str, "0"));
    } catch (Exception ex) {
      expHandle("colint=" + col, ex);
    }
    return 0;
  }

  public boolean coleqIgno(String col, String param1) {
    if (col == null || param1 == null) {
      return false;
    }
    return (colSs(col).equalsIgnoreCase(param1));
  }

  public boolean colEq(String col, String param) {
    if (col == null || param == null) {
      return false;
    }
    return (colSs(col).equals(param));
  }

  public boolean colEq(String col, double num1) {
    if (col == null) {
      return false;
    }

    return (colNum(col) == num1);
  }

  public int colLen(String col) {
    return colSs(col).length();
  }

  public boolean colEmpty(String col) {
    return (colSs(col).length() == 0);
  }

  void expHandle(String param, Exception ex) {
    System.out.println("-DDD->" + this.getClass().getSimpleName() + ":" + param);
  }

  boolean empty(String param) {
    if (param == null || param.trim().length() == 0) {
      return true;
    }
    return false;
  }

  void colClear() {
    if (colHash != null)
      colHash.clear();
  }

  void listClear() {
    if (colList != null)
      colList.clear();
  }

  // --String-func----------------------
  String nvl(String param) {
    if (param == null) {
      return "";
    }
    return param.trim();
  }

  String nvl(String param1, String param2) {
    if (param1 == null) {
      if (param2 == null) {
        return "";
      }
      return param2;
    }
    return param1.trim();
  }
}
