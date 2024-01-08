package com;
/** Sql.Insert/Update 功能二
 * 110-01-07  V1.00.02    shiyuqi       修改无意义命名                                                                           *
*  109/07/22  V1.00.01    Zuwei     coding standard, rename field method                   *
*  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
 * 2020-0109   JH    aaa_date()
 * 2020-0106   JH    initial
 * */
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Parm2sql {
  private HashMap<Integer, Object> sortHash = new HashMap<Integer, Object>();

  // public String sql_from="";
  private String convSQL = "";
  private Object[] convParm;
  private int parmIndx = 0;
  private int audCode = 0;
  private String tableName = "";

  public int ti = -1;

  private StringBuffer sbSql1 = new StringBuffer("");
  private StringBuffer sbSql2 = new StringBuffer("");
  private StringBuffer sbSqlWhere = new StringBuffer("");

  public void insert(String aTab) {
    parmIndx = 0;
    if (ti > 0)
      return;
    audCode = 1;
    tableName = aTab;
    sbSql1 = new StringBuffer("");
    sbSql2 = new StringBuffer("");
  }

  public void update(String aTab) {
    parmIndx = 0;
    if (ti > 0)
      return;
    audCode = 2;
    tableName = aTab;
    sbSql1 = new StringBuffer("");
    sbSql2 = new StringBuffer("");
    sbSqlWhere = new StringBuffer("");
  }

  public String getConvSQL() {
    convSQL = "";
    if (audCode == 1) {
      convSQL = "insert into "
          + tableName
          + " ( "
          + sbSql1.substring(0, sbSql1.length() - 1)
          + " ) values ( "
          + sbSql2.substring(0, sbSql2.length() - 1)
          + " )";
    } else if (audCode == 2) {
      convSQL = "update "
          + tableName
          + " set "
          + sbSql1.substring(0, sbSql1.length() - 1)
          + " "
          + sbSqlWhere.toString();
    }
    // sql_from =_convSQL;
    return convSQL;
  }

  public String getSql() {
    if (convSQL.length() > 0) {
      return convSQL;
    }
    return getConvSQL();
  }

  public Object[] getParms() {
    parmIndx = 0;
    sort2Parm(true);
    return convParm;
  }

  public Object[] getConvParm(boolean aClear) {
    parmIndx = 0;
    sort2Parm(aClear);
    return convParm;
  }

  public Object[] getConvParm() {
    parmIndx = 0;
    sort2Parm(true);
    return convParm;
  }

  public void clear() {
    convSQL = "";
    convParm = null;
    sortHash.clear();
    parmIndx = 0;
  }

  private void sort2Parm(boolean aClear) {
    convParm = new Object[sortHash.size()];
    Object[] keys2 = sortHash.keySet().toArray();
    Arrays.sort(keys2);
    int ii = -1;
    for (Object sortData : keys2) {
      ii++;
      convParm[ii] = sortHash.get(sortData);
    }

    if (aClear)
      sortHash.clear();
  }

  private String nvl(String param) {
    if (param == null)
      return "";
    return param.trim();
  }

  private void addSql(String col, String sql1) {
    if (ti > 0)
      return;
    if (audCode == 1) {
      sbSql1.append(col + " ,");
      sbSql2.append(sql1 + " ,");
    } else if (audCode == 2) {
      sbSql1.append(col + " =" + sql1 + " ,");
    }
  }

  private void addSql(String col) {
    if (ti > 0)
      return;
    if (audCode == 1) {
      sbSql1.append(col + " ,");
      sbSql2.append("? ,");
    } else if (audCode == 2) {
      sbSql1.append(col + " =? ,");
    }
  }

  // -sql-function-
  public void aaaFunc(String col, String sql1, Object obj) {
    if (sql1.indexOf("?") >= 0) {
      parmAdd(obj);
    }
    addSql(col, sql1);
  }
  // public void aaFunc(String col, String sql1, double num1) {
  // if (sql1.indexOf("?")>=0) {
  // parmAdd(num1);
  // }
  // addSql(col,sql1);
  // }
  // public void aaFunc(String col, String sql1, int int1) {
  // if (sql1.indexOf("?")>=0) {
  // parmAdd(int1);
  // }
  // addSql(col,sql1);
  // }

  // -parm=?-
  public void aaa(String col, Object obj) {
    parmAdd(obj);
    addSql(col);
  }

  // public void aaa(String col, double num1) {
  // parmAdd(num1);
  // addSql(col);
  // }
  // public void aaa(String col, int int1) {
  // parmAdd(int1);
  // addSql(col);
  // }
  public void aaaYmd(String col) {
    if (ti > 0)
      return;
    addSql(col, " to_char(sysdate,'yyyymmdd') ");
  }

  public void aaaTime(String col) {
    if (ti > 0)
      return;
    addSql(col, " to_char(sysdate,'hh24miss') ");
  }

  public void aaaDtime(String col) {
    if (ti > 0)
      return;
    addSql(col, "sysdate");
  }

  // -where-
  public void aaaWhere(String sql1, Object obj) {
    if (sql1.indexOf("?") >= 0) {
      parmAdd(obj);
    }
    if (audCode != 2)
      return;
    sbSqlWhere.append(" " + sql1);
  }

  public void aaaModxxx(String modUser, String modPgm) {
    if (audCode == 1) {
      aaa("mod_user", modUser);
      aaaFunc("mod_time", "sysdate", "");
      aaa("mod_pgm", modPgm);
      aaaFunc("mod_seqno", "1", "");
    }
    if (audCode == 2) {
      aaa("mod_user", modUser);
      aaaFunc("mod_time", "sysdate", "");
      aaa("mod_pgm", modPgm);
      aaaFunc("mod_seqno", "nvl(mod_seqno,0)+1", "");
    }
  }

  private void parmAdd(Object param) {
    parmIndx++;
    sortHash.put(parmIndx, param);
  }

}
