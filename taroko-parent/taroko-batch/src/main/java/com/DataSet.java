/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *    DATE    Version    AUTHOR                       DESCRIPTION              *
 *  --------  -------------------  ------------------------------------------  *
 *  109/07/06  V0.00.02    Zuwei     coding standard, rename field method & format *
 *  110-01-07  V1.00.03    shiyuqi   coding standard, rename                   *
 *  2023-0824 V1.00.04    JH        bugFix: colInt, colNum
 ******************************************************************************/
package com;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public class DataSet {
private HashMap<String, String> colHash = new HashMap<String, String>();
private final HashMap<String, String> keyHash = new HashMap<String, String>();
private int currRow = -1;
public int listCnt = 0;
public int[] aa_sortRow = null;

//---------------------------------
public int getCurrRow() {
   return currRow;
}

public void setCurrRow(int ll) {
   currRow = ll;
}

public void listCurr(int ll) {
   currRow = ll;
}

// --DB-set-value-
public void colSet(String col, Object obj1) {
   colSet(currRow, col, obj1);
}

public void colSet(int rr, String col, Object obj1) {
   if (nvl(col).length() == 0)
      return;

   String col1 = convCol(rr, col);
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

public String colNvl(String col, String param) {
   String str = colSs(col);
   if (empty(str))
      return param;
   return str;
}

public String colss(int ll, String col) {
   return colss(ll, col);
}

public double colnum(int ll, String col) {
   return colNum(ll, col);
}

public int colint(int ll, String col) {
   return colInt(ll, col);
}

public String colSs(String col) {
   return colSs(currRow, col);
}

public String colSs(int ll, String col) {
   String str = "";
   try {
      str = colHash.get(convCol(ll, col));
      if (str == null) return "";
   } catch (Exception ex) {
      expHandle("col_ss(" + col + ")", ex);
   }
   return nvl(str);
}

public double colNum(String col) {
   return colNum(currRow, col);
}

public double colNum(int ll, String col) {
   try {
      String str = colHash.get(convCol(ll, col));
      if (str == null || str.trim().length() == 0) return 0;
      return Double.parseDouble(str);
   } catch (Exception ex) {
      expHandle("col_num(" + col + ")", ex);
   }
   return 0;
}

public int colInt(String col) {
   return colInt(currRow, col);
}

public int colInt(int ll, String col) {
   try {
      String str = colHash.get(convCol(ll, col));
      if (str == null || str.trim().length() == 0) return 0;
      return (int) (Double.parseDouble(str.trim()));
   } catch (Exception ex) {
      expHandle("col_int(" + col + ")", ex);
   }
   return 0;
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

public boolean colEmpty(String col) {
   return (colSs(col).length() == 0);
}

// ----------------------------------------
public void dataClear() {
   colClear();
}

public boolean listNext() {
   int ll = currRow + 1;
   if (ll >= listCnt)
      return false;

   currRow++;
   return (currRow >= 0);
}

private boolean empty(String param) {
   if (param == null || param.trim().length() == 0) {
      return true;
   }
   return false;
}

void colClear() {
   listCnt = 0;
   currRow = -1;
   if (colHash != null)
      colHash.clear();
}

// --String-func----------------------
private String convCol(int rr, String col) {
   return col.trim().toUpperCase() + "-#" + rr;
}

String nvl(String param) {
   if (param == null) {
      return "";
   }
   return param.trim();
}

String nvl(String param, String param2) {
   if (param == null) {
      if (param2 == null) {
         return "";
      }
      return param2;
   }
   return param.trim();
}

void expHandle(String param, Exception ex) {
   System.out.println(
    "-DDD->" + this.getClass().getSimpleName() + ":" + param + ", err=" + ex.getMessage());
}

public int rowCount() {
   return listCnt;
}

public int addrow() {
   listCnt++;
   currRow = listCnt - 1;
   return currRow;
}

//---------------
//-key-index---
public int getKeyData(String kk) {
   if (empty(kk))
      return 0;
   String ls_row = keyHash.get(kk);
   if (ls_row == null) return 0;

   String[] tt = ls_row.split(",");
   int li_cnt = 0;
   if (tt.length > 0) {
      li_cnt = ss2Int(tt[0]);
   }
   if (li_cnt <= 0) {
      return 0;
   }
   //--
   int li_str = -1;
   if (tt.length > 1) {
      li_str = ss2Int(tt[1]);
   }
   //--
   if (li_str < 0) return 0;
   currRow = li_str;
   return li_cnt;
}

public int keyCount() {
   return keyHash.size();
}

public void loadKeyData(String col) {
   if (empty(col)) return;
   if (listCnt == 0) return;
   keyHash.clear();
   for (int ll = 0; ll < listCnt; ll++) {
      keyHash.put(colSs(ll, col), "1," + ll);
   }
}

public void loadKeyData_dupl(String col) {
   if (empty(col)) return;
   if (listCnt == 0) return;
   keyHash.clear();
   //-dupli-
   String kk1 = "", ss = "";
   int ll_str = 0, ll_cnt = 1;
   kk1 = colSs(0, col);
   for (int ll = 1; ll < listCnt; ll++) {
      ss = colSs(ll, col);
      if (empty(ss)) continue;

      if (!ss.equals(kk1) && !empty(kk1)) {
         keyHash.put(kk1, "" + ll_cnt + "," + ll_str);
         ll_cnt = 0;
         ll_str = ll;
      }
      ll_cnt++;
      kk1 = ss;
   }
   if (!empty(kk1)) {
      keyHash.put(kk1, "" + ll_cnt + "," + ll_str);
   }
}

//-sort-
public int sort(String... kk) {
   aa_sortRow = null;
   if (listCnt == 0)
      return -1;

   Map KK = new HashMap();
   for (int ii = 0; ii < listCnt; ii++) {
      String ss = "";
      for (String c1 : kk)
         ss += colSs(ii, c1) + "||";
      String kk_name = String.format("%s-%03d", ss, ii);
      KK.put(kk_name, ii);
   }

   Set kk_set = KK.keySet();
   Object[] arr = kk_set.toArray();
   Arrays.sort(arr);
   aa_sortRow = new int[arr.length];
   for (int ii = 0; ii < arr.length; ii++) {
      aa_sortRow[ii] = (int) KK.get(arr[ii]);
   }

   return aa_sortRow.length;
}

public String minSs(String col) {
   String ls_max = "";
   for (int ii = 0; ii < listCnt; ii++) {
      if (colSs(ii, col).compareTo(ls_max) < 0)
         ls_max = colSs(ii, col);
   }
   return ls_max;
}

public double maxNum(String col) {
   double lm_max = colNum(0, col);
   for (int ii = 1; ii < listCnt; ii++) {
      if (colNum(ii, col) > lm_max)
         lm_max = colNum(ii, col);
   }
   return lm_max;
}

public double min_num(String col) {
   double num = colNum(0, col);
   for (int ii = 1; ii < listCnt; ii++) {
      if (colNum(ii, col) < num)
         num = colNum(ii, col);
   }
   return num;
}


// --String-func----------------------
private int ss2Int(String s1) {
   try {
      if (empty(s1)) {
         return -1;
      }
      return Integer.parseInt(s1);
   } catch (Exception ex) {
      return -1;
   }
}

private double ss2Num(String s1) {
   try {
      if (empty(s1)) {
         return 0;
      }
      return Double.parseDouble(s1);
   } catch (Exception ex) {
      return 0;
   }
}

}
