/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi;
/** DataSet公用程式:
 * 2019-1205   JH    col_ss()
 * 2019-1008   JH    col_set()
 * 2018-0814:	JH		++sss,nnn,iii()
 * 2018-0615:	JH		++colss, colnum, colint()
 * 110-01-07  V1.00.03  tanwei        修改意義不明確變量                                                                          *
 * */
import java.util.*;

public class DataSet {

  private HashMap<String, String> colHash = new HashMap<String, String>();
  public List<Map<String, String>> colList = null;
  public int[] sortRow = null;
  
  int currRow = -1;
  
  public int sort(String key1) {
	  sortRow = null;
	  if(colList == null)
		  return -1;
	  
	  Map keyMap = new HashMap();
	  for(int ii=0; ii < listRows(); ii++) {
		  listToCol(ii);
		  String keyName = String.format("%s-%03d", colStr(key1),ii);
		  keyMap.put(keyName,ii);
	  }
	  
	  Set keySet = keyMap.keySet();
	  Object[] arr = keySet.toArray();
	  Arrays.sort(arr);
	  
	  sortRow = new int[arr.length];
	  for(int ii=0 ; ii<arr.length ; ii++) {
		  sortRow[ii] = (int) keyMap.get(arr[ii]);
	  }
	  
	  return sortRow.length;
  }
  
  // --DB-set-value-
  public void colSet(int num, String col, Object obj1) {
    if (nvl(col).length() == 0)
      return;

    String col1 = nvl(col).toUpperCase();
    try {
      colList.get(num).remove(col1);
      if (obj1 == null) {
        colList.get(num).put(col1, "");
      } else {
        colList.get(num).put(col1, nvl(obj1.toString()));
      }
    } catch (Exception ex) {
      expHandle("col_set", ex);
    }
  }

  public void colSet(String col, Object obj1) {
    colSet(currRow, col, obj1);
    // colHash.remove(col.toUpperCase().trim());
    // colHash.put(col.toUpperCase().trim(),nvl(obj1.toString()));
    // colList.remove(curr_row);
    // colList.add(curr_row,colHash);
  }

  public void dataClear() {
    listClear();
    colClear();
    currRow = -1;
    if (colList != null) {
      currRow = colList.size() - 1;
    }
  }

  public int listRows() {
    return colList.size();
  }

  public void listFetch(int num) {
    listToCol(num);
  }

  public void listToCol(int num) {
    // colClear();
    if (num < colList.size()) {
      colHash = (HashMap<String, String>) colList.get(num);
      currRow = num;
    } else
      currRow = -1;
    return;
  }

  public boolean listNext() {
    int ll = currRow + 1;
    listToCol(ll);
    return (currRow >= 0);
  }

  public String colNvl(String col, String strName) {
    String colStr = colStr(col);
    if (empty(colStr))
      return strName;
    return colStr;
  }

  public String colStr2(String col) {
    String ss = "";
    try {
      ss = (String) colHash.get(col.trim().toUpperCase());
    } catch (Exception ex) {
      expHandle("sss=" + col, ex);
    }
    return nvl(ss);
  }

  public String colStr(String col) {
    String ss = "";
    try {
      ss = (String) colHash.get(col.trim().toUpperCase());
    } catch (Exception ex) {
      expHandle("col_ss=" + col, ex);
    }
    return nvl(ss);
  }

  public String listStr(int num, String col) {
    String ss = "";
    try {
      ss = (String) colList.get(num).get(col.trim().toUpperCase());
    } catch (Exception ex) {
      expHandle("col_ss=" + col, ex);
    }
    return nvl(ss);
  }

  // public String col_ss(int rr,String col) {
  // if (rr>0)
  // return col_ss(nvl(col).toUpperCase()+"-"+rr);
  //
  // return col_ss(col);
  // }

  public double colNum2(String col) {
    String ss = "";
    try {
      ss = (String) colHash.get(col.trim().toUpperCase());
      return Double.parseDouble(nvl(ss, "0"));
    } catch (Exception ex) {
      expHandle("col_num", ex);
    }
    return 0;
  }

  public double colNum(String col) {
    String colName = "";
    try {
      colName = (String) colHash.get(col.trim().toUpperCase());
      return Double.parseDouble(nvl(colName, "0"));
    } catch (Exception ex) {
      expHandle("col_num", ex);
    }
    return 0;
  }

  public double listNum(int num, String col) {
    String ss = "";
    try {
      ss = (String) colList.get(num).get(col.trim().toUpperCase());
      return Double.parseDouble(nvl(ss, "0"));
    } catch (Exception ex) {
      expHandle("colnum=" + col, ex);
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

  public double listInt(int num, String col) {
    String colStr = "";
    try {
      colStr = (String) colList.get(num).get(col.trim().toUpperCase());
      return Integer.parseInt(nvl(colStr, "0"));
    } catch (Exception ex) {
      expHandle("colint=" + col, ex);
    }
    return 0;
  }

  public boolean colEqIgno(String col, String strName) {
    if (col == null || strName == null) {
      return false;
    }
    return (colStr(col).equalsIgnoreCase(strName));
  }

  public boolean colEq(String col, String strName) {
    if (col == null || strName == null) {
      return false;
    }
    return (colStr(col).equals(strName));
  }

  public boolean colEq(String col, double num1) {
    if (col == null) {
      return false;
    }

    return (colNum(col) == num1);
  }

  public int colLen(String col) {
    return colStr(col).length();
  }

  public boolean colEmpty(String col) {
    return (colStr(col).length() == 0);
  }

  public void addrow() {
    currRow++;
    colHash = new HashMap<String, String>();
    if (colList == null) {
      colList = new ArrayList<>();
    }
    colList.add(colHash);
    currRow = colList.size() - 1;
  }

  void expHandle(String strName, Exception ex) {
    System.out.println("-DDD->" + this.getClass().getSimpleName() + ":" + strName);
  }

  boolean empty(String strName) {
    if (strName == null || strName.trim().length() == 0) {
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
  String nvl(String strName) {
    if (strName == null) {
      return "";
    }
    return strName.trim();
  }

  String nvl(String strName, String colName) {
    if (strName == null) {
      if (colName == null) {
        return "";
      }
      return colName;
    }
    return strName.trim();
  }
}
