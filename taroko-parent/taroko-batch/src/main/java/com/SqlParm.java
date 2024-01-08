package com;
/** SQL參數元件
 * 110-01-07  V1.00.02    shiyuqi       修改无意义命名                                                                           *
 *  109/07/22  V1.00.01   Zuwei     coding standard, rename field method                   *
 *  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
* 2019-0827   JH    modify
 * 2019-0723:  JH    >>StringBuffer
 * 2018-0706:	jh		add()
 * 2018-0702:	JH		kkk(ss,ss)
 * 2018-0517:	JH		ddd_sql()
 * 2018-0503:	JH		sort_2Parm()
 * 2018-0228:	JH		++ppp()指定小數點
 * V00.00	2017-10xx	JH		initial
 *
 * */

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class SqlParm {

  private HashMap<String, Object> parmHash = new HashMap<String, Object>();
  private HashMap<Integer, Object> sortHash = new HashMap<Integer, Object>();

  public String sqlFrom = "";
  private int parmCnt = 0;
  private String parmFlag = "";
  private String convSQL = "";
  private Object[] convParm;
  private String[] parmKey;
  private int parmKeyIndx = 0;
  private int audCode = 0;

  // private Object[] _parmVal;
  // private String internalCall="";
  public int pfidx = -1;
  public String useTable = "";

  private StringBuffer sbSql1 = new StringBuffer("");
  private StringBuffer sbSql2 = new StringBuffer("");

  // -AAA--add_sql for Insert------------------------------------------
  public void insert(String aTab) {
    audCode = 1;
    sbSql1 = new StringBuffer("insert into " + aTab + " (");
    sbSql2 = new StringBuffer(" ) values (");
  }

  public void update(String aTab) {
    audCode = 2;
    sbSql1 = new StringBuffer("update " + aTab + " set ");
    sbSql2 = new StringBuffer("");
  }

  public void aaaModxxx(String modUser, String modPgm) {
    if (audCode == 1) {
      sbSql1.append(" , mod_user, mod_time, mod_pgm, mod_seqno");
      sbSql2.append(", '" + modUser + "', sysdate, '" + modPgm + "', 1");
    }
    if (audCode == 2) {
      sbSql1.append(", mod_user ='" + modUser + "'");
      sbSql1.append(", mod_time =sysdate");
      sbSql1.append(", mod_pgm ='" + modPgm + "'");
      sbSql1.append(", mod_seqno =nvl(mod_seqno,0)+1");
    }
    sqlFrom = sbSql1.toString() + " " + sbSql2.toString();
    return;
  }

  public void aaa(String sql1, String sql2, String col) {
    if (empty(sql1) == false) {
      sbSql1.append(sql1);
      sbSql2.append(sql2);
      sqlFrom = sbSql1.toString() + " " + sbSql2.toString();
      pmkk(col);
      return;
    }
    // sql1=''----
    if (sql2.indexOf("?") >= 0) {
      sbSql2.append(sql2);
      sbSql1.append(sql2.replaceFirst("\\?", col.trim()));
      pmkk(col);
    } else {
      sbSql1.append(sql2);
      sbSql2.append(col);
    }

    sqlFrom = sbSql1.toString() + " " + sbSql2.toString();
    return;
  }

  public void aaa(String col, String param) {
    if (col.indexOf("?") >= 0) {
      sbSql1.append(col);
      pmkk(param);
      sqlFrom = sbSql1.toString() + " " + sbSql2.toString();
      return;
    }

    // -----------------------------------
    sbSql1.append(col);
    sbSql2.append(param);
    if (param.indexOf("?") >= 0) {
      pmkk(col);
    }
    sqlFrom = sbSql1.toString() + " " + sbSql2.toString();
  }

  public void aaa(String sql1) {
    sqlFrom += sql1;
  }
  // -VVV-----------------------------------------------------


  public String dddSql() {
    if (sqlFrom.trim().length() == 0)
      return "no sql statement ?????";
    Object[] obj = this.getConvParm(false);
    if (obj.length == 0)
      return sqlFrom;

    String str = sqlFrom;
    for (Object o1 : obj) {
      if (o1 == null)
        o1 = "NULL";
      if (str.indexOf("?") < 0)
        str += "; [" + o1.toString() + "]";
      else
        str = str.replaceFirst("\\?", "'" + o1 + "'");
    }
    return str;
  }

  public void ppp(String col, int num1) {
    parmInt(col, num1);
  }

  public void ppp(int col, int num1) {
    parmInt(col, num1);
  }

  public void ppp(int num1) {
    parmInt((parmCnt + 1), num1);
  }

  public void parmInt(int i, int parmNum) {
    sortHash.put(i, parmNum);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void parmInt(String parmField, int parmNum) {
    int ii = parmKeyFind(parmField);

    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
      parmFlag = "S";
      return;
    }

    parmInt(ii, parmNum);
    return;
  }

  double numRound(double num1, int po1) {
    return new BigDecimal(num1).setScale(po1, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  public void pppRound(String col, double num1, int po1) {
    parmNum(col, numRound(num1, po1));
  }

  public void ppp(String col, double num1) {
    parmNum(col, num1);
  }

  public void ppp(int col, double num1) {
    parmNum(col, num1);
  }

  public void ppp(double num1) {
    parmNum((parmCnt + 1), num1);
  }

  public void pppRound(int col, double num1, int po1) {
    parmNum(col, numRound(num1, po1));
  }

  public void parmNum(int i, double parmNum) {
    sortHash.put(i, parmNum);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void parmNum(String parmField, double parmNum) {
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
      parmFlag = "S";
      return;
    }

    parmNum(ii, parmNum);
    return;
  }
  // public void setNumber(String col,double num1) {
  // setDouble(col,num1);
  // }
  // public void setNumber(String col) {
  // setDouble(col,0);
  // }

  public void parmSs(int int1, String parmString) {
    sortHash.put(int1, parmString);
    parmFlag = "N";
    parmCnt = int1;
    return;
  }

  public void ppp(String col, String param) {
    parmSs(col, param);
  }

  public void ppp(int col, String param) {
    parmSs(col, param);
  }

  public void ppp(String param) {
    parmSs((parmCnt + 1), param);
  }

  public void parmSs(String parmField, String parmString) {
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmString);
      parmFlag = "S";
      return;
    }

    parmSs(ii, parmString);
    return;
  }

  public void parmInit(String col) {
    parmSs(col, "");
  }

  public void setRowId(int ii, String parmRowId) {
    sortHash.put(ii, hex2Byte(parmRowId));
    parmFlag = "N";
    parmCnt = ii;
    return;
  }

  public void setRowId(String parmField, String parmRowId) {
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(),
          hex2Byte(parmRowId));
      parmFlag = "S";
      return;
    }

    setRowId(ii, parmRowId);
    return;
  }

  public void setDate(String parmField) {
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmss");
    String sDate = form1.format(currDate).substring(0, 8);
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), sDate);
      parmFlag = "S";
      return;
    }

    parmSs(ii, sDate);
    return;
  }

  public void setTime(String parmField) {
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmss");
    String str = form1.format(currDate).substring(8);
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), str);
      parmFlag = "S";
      return;
    }

    parmSs(ii, str);
    return;
  }

  public void setDateTime(String parmField) {
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmss");
    int ii = parmKeyFind(parmField);
    if (ii < 0) {
      parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(),
          form1.format(currDate));
      parmFlag = "S";
      return;
    }

    parmSs(ii, form1.format(currDate));
    return;
  }

  byte[] hex2Byte(String str) {
    int len = str.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
    return data;
  }
  // -VV------------------------------------------------------

  // public boolean has_convSql() {
  // return _convSQL.length()>0;
  // }
  public String getConvSQL() {
    if (convSQL.length() == 0) {
      nameSqlParm(sqlFrom);
    }
    return convSQL;
  }

  public Object[] getConvParm(boolean aClear) {
    sort2Parm(aClear);
    return convParm;
  }

  public Object[] getConvParm() {
    sort2Parm(true);
    return convParm;
  }

  public void clear() {
    convSQL = "";
    convParm = null;
    parmKey = null;

  }

  public boolean nameSqlParm(String cvtSql) {
    if (cvtSql.trim().length() == 0) {
      cvtSql = this.sqlFrom;
    }
    convSQL = cvtSql;
    convParm = null;
    parmKey = null;
    // _parmVal=null;

    if (Arrays.asList("N", "S").contains(parmFlag) == false) {
      return false;
    }

    HashMap<Integer, String> kkHash = new HashMap<Integer, String>();

    if (parmFlag.equals("S")) {
      Object[] keys1 = parmHash.keySet().toArray();
      Arrays.sort(keys1);
      for (Object parmKey : keys1) {
        String kk = parmKey.toString();
        String replaceField = kk.substring(kk.indexOf(":"));
        // -相同變數取代-
        int ii = cvtSql.indexOf(replaceField);
        while (ii > 0) {
          convSQL = convSQL.replaceFirst(replaceField, "?");
          sortHash.put(ii, parmHash.get(parmKey));
          kkHash.put(ii, replaceField.toUpperCase().trim());
          parmCnt++;
          cvtSql = cvtSql.replaceFirst(replaceField,
              new String(new char[replaceField.length()]).replace("\0", "-"));
          // -next-value-
          ii = cvtSql.indexOf(replaceField);
        }
      }
    }

    if (parmCnt == 0) {
      return false;
    }

    // sort_2Parm();
    // _convParm = new Object[parmCnt];
    // Object[] keys2 = sortHash.keySet().toArray();
    // Arrays.sort(keys2);
    // int i=0;
    // for( Object sortData : keys2) {
    // _convParm[i++] = sortHash.get(sortData);
    // }

    if (kkHash.size() > 0) {
      parmKey = new String[kkHash.size()];
      Object[] param = kkHash.keySet().toArray();
      Arrays.sort(param);
      int ii = 0;
      for (Object obj : param) {
        parmKey[ii] = kkHash.get(obj).trim().toUpperCase().replace(":", "");
        // System.out.println(_parmKey[ii]);
        ii++;
      }
    }

    // sortHash.clear();
    parmHash.clear();
    parmCnt = 0;
    parmFlag = "";
    // return param;
    return true;
  }

  public String kkk(String sql1, String col) {
    if (sql1.indexOf("?") < 0 || col.trim().length() == 0)
      return sql1;

    parmKeyIndx++;
    sortHash.put(parmKeyIndx, col.toLowerCase().trim());

    return sql1;
  }

  public String kkk(int ii, String param) {
    return pmkk(ii, param);
  }

  public String kkk(String param) {
    return pmkk(param);
  }

  public String pmkk(int ii, String param) {
    if (param.trim().length() == 0)
      return "";
    parmKeyIndx = ii;
    return pmkk(param);
  }

  public String pmkk(String param) {
    if (param.trim().length() == 0)
      return "";
    parmKeyIndx++;
    // String col =s1.replaceAll(",", "").replaceAll(":","").trim();
    String col = param.replaceAll(",", "").trim();
    if (col.indexOf(":") == 0) {
      col = col.replace(":", "");
    } else if (col.indexOf(":") > 0) {
      col = col.substring(col.indexOf(":") + 1);
    }
    sortHash.put(parmKeyIndx, col.toLowerCase().trim());

    return param.replace(col, "?").replaceAll(":", "");
  }

  public void parmKeySort() {
    Object[] keys2 = sortHash.keySet().toArray();
    this.parmKey = new String[keys2.length];
    Arrays.sort(keys2);
    int ii = -1;
    for (Object sortData : keys2) {
      ii++;
      parmKey[ii] = sortHash.get(sortData).toString().toUpperCase().trim();
    }
    sortHash.clear();
    return;
  }

  public String parmKeyPrint() {
    Object[] keys2 = sortHash.keySet().toArray();
    Arrays.sort(keys2);
    String str = "";
    for (Object sortData : keys2) {
      str += "," + sortData.toString() + ":" + sortHash.get(sortData);
    }
    return str;
  }

  public void parmValueClear() {
    sortHash.clear();
  }

  void sort2Parm(boolean aClear) {
    if (parmKey != null && parmKey.length > 0) {
      convParm = new Object[parmKey.length];
      for (int ii = 0; ii < parmKey.length; ii++) {
        convParm[ii] = sortHash.get(ii);
      }
    } else {
      convParm = new Object[sortHash.size()];
      Object[] keys2 = sortHash.keySet().toArray();
      Arrays.sort(keys2);
      int ii = -1;
      for (Object sortData : keys2) {
        ii++;
        convParm[ii] = sortHash.get(sortData);
      }
    }

    if (aClear)
      sortHash.clear();
  }

  int parmKeyFind(String col) {
    if (parmKey == null) {
      this.parmKeySort();
    }
    return Arrays.asList(parmKey).indexOf(col.trim().toUpperCase());
  }

  private boolean empty(String param) {
    if (param == null)
      return true;
    if (param.trim().length() == 0)
      return true;
    return false;
  }

}
