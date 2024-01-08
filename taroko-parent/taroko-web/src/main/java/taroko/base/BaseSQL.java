/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-13  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei       coding standard      *
*  110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
******************************************************************************/
package taroko.base;
/** Taroko SQL 底層程式
 * 2020-1222   Justin chg names commString and zzcom
 * 2019-1220   JH    getNumber(.)
 * 2019-1025   JH    sqlExec()
 * 2019-0905   JH    colName_get()
 * 2019-0830   JH    throw exception
 * 2019-0705   JH    sql_isLimit
*  2019-0115:  JH    columnCnt
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
 *
 * */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import Dxc.Util.SecurityUtil;

@SuppressWarnings({"unchecked", "deprecation"})
public class BaseSQL {

  public int maxColSize = 300;
  // public String[] columnName = new String[maxColSize];
  private String[] colName = null;
  private String[] colType = null;
  private int columnCnt = 0;
  // public boolean keep_colName=false;
  // public String[] org_colName = null;
  // public int org_columnCnt = 0;

  protected String cvtWhere = "";
  // -JH:add------------------
  private int selectLimit = 999;
  public int sqlCode = 0; // 0.Success,100.not found, -1.Error Use SQL_ErrText or SQL_DBCode
  public int sqlRowNum = 0;
  public int sqlErrorCode = 0;
  public String sqlErrtext = "";
  public boolean sqlDupl = false;
  public String daoTid = "";
  public boolean ibDspSql = false;
  protected taroko.base.CommSqlStr commSqlStr = new taroko.base.CommSqlStr();
  protected taroko.base.CommString commString = new taroko.base.CommString();

  private HashMap<String, String> colHash = new HashMap<String, String>();

  protected SqlParm sqlParm = new SqlParm();
  // int ii_parm=0;
  // private HashMap<String, Object> parmHash = new HashMap<String, Object>();
  // private HashMap<Integer, Object> sortHash = new HashMap<Integer, Object>();
  // protected int parmCnt = 0;
  // protected String parmFlag = "", convertSQL = "", internalCall = "";
  private String mesg = "", sqlLog = "";
  protected int rc = 1;

  public String sqlLog() {
    return sqlLog;
  }

  public String getMesg() {
    return mesg;
  }

  protected void errmsg(String strName) {
    mesg = strName;
    rc = -1;
  }

  protected void errmsg(String strName, Object... objs) {
    mesg = String.format(strName, objs);
    rc = -1;
  }

  protected void setMesg(String strName) {
    mesg = strName;
  }

  protected void setMesg(String strName, Object... objs) {
    mesg = String.format(strName, objs);
  }

  protected void msgOK() {
    mesg = "";
    rc = 1;
  }

  protected boolean sqlIsLimit(String aSql) {
    String lsSql = aSql.toLowerCase();
    if (lsSql.indexOf(" fetch ") > 0)
      return true;
    if (lsSql.indexOf(" first ") > 0)
      return true;
    if (lsSql.indexOf(" limit ") > 0)
      return true;
    if (lsSql.indexOf(" offset ") > 0)
      return true;
    return false;
  }

  // -------------------------------------------------------------
  public void dataClear() {
    colHash.clear();
  }

  public void sqlInit() {
    sqlCode = 100; // not-find
    sqlErrtext = "";
  }

  public void setSelectLimit(int int1) {
    selectLimit = int1;
  }

  public String sqlRownum(int num) {
    if (num == 0)
      return "";
    return " fetch first " + num + " rows only ";
  }

  private void checkSelectLimit(ResultSet rs) throws Exception {
    if (selectLimit <= 0)
      return;
    // try {
    rs.last();
    int llRow = rs.getRow();
    rs.beforeFirst();
    if (llRow > 0 && llRow > selectLimit) {
      throw new IllegalArgumentException(">>>???>>>SQL select 已超過限制筆數(" + llRow
          + "); set_selectLimit(0)");
    }
    // }
    // catch (Exception ex) {}
  }

  public void sqlSelect(Connection conn, String sql1, Object[] obj) throws Exception {
    PreparedStatement ps = null;
    ResultSet rs = null;
    sqlCode = 0;
    sqlRowNum = 0;
    sqlErrtext = "";
    boolean lbRowsOver = false;
    Object[] param = null;

    try {

      if (obj == null && sqlParm.nameSqlParm(sql1)) {
        sql1 = sqlParm.getConvSQL();
        param = sqlParm.getConvParm();
      } else
        param = obj;

      sqlLog = commString.formatSqlString(sql1, param);

      ps =
          conn.prepareStatement(sql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }

      rs = ps.executeQuery();
      java.sql.ResultSetMetaData mdata = rs.getMetaData();
      readColNames(mdata);
      String strColName = "";

      checkSelectLimit(rs);

      // --
      int rr = 0;
      while (rs.next()) {
        sqlRowNum++;
        for (int k = 1; k < colName.length; k++) {
          strColName = daoTid + colName[k];
          // if ( colType[k].equalsIgnoreCase("BLOB")) {
          // wp.blobValue = rs.getBlob(k);
          // }
          // else
          if (colType[k].equalsIgnoreCase("DECIMAL") || colType[k].equalsIgnoreCase("DOUBLE")) {
            if (rs.getObject(k) != null)
              this.sqlSetNum(rr, strColName, rs.getDouble(k));
            else
              this.sqlSetNum(rr, strColName, 0);
          } else if (colType[k].equalsIgnoreCase("INTEGER") || colType[k].equalsIgnoreCase("LONG")) {
            if (rs.getObject(k) != null)
              sqlSetNum(rr, strColName, rs.getLong(k));
            else
              sqlSetNum(rr, strColName, 0);
          } else {
            if (rs.getObject(k) != null) {
              this.sqlSet(rr, strColName, rs.getObject(k).toString());
            } else
              sqlSet(rr, strColName, "");
          }
        }
        rr++;

        // -JH:2018-0608-
        if (selectLimit > 0 && sqlRowNum >= selectLimit) {
          errmsg("????? select-筆數已超過 [%s] ?????", selectLimit);
          lbRowsOver = true;
          break;
        }
      } // End of while

      daoTid = "";
      rs.close();
      ps.close();
      rs = null;
      ps = null;

      this.setSelectLimit(999);
      if (sqlRowNum == 0) {
        sqlCode = 100;
      }
    } // End of try
    catch (SQLException ex2) {
      rc = -1;
      int liExp = getSqlErrmsg(ex2);
      if (liExp == 1) {
        log(commString.formatSqlString("sqlerr: " + sql1, param));
        throw new Exception("sqlSelect: " + ex2.getMessage());
      }
    } catch (Exception ex) {
      rc = -1;
      setSqlErrmsg(ex.getMessage());
      throw new Exception("sqlSelect: " + ex.getMessage());
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
      } catch (Exception ex2) {
      }
    }
    daoTid = "";
  } // End of sql_select

  public void sqlExec(Connection conn, String sql1, Object param[]) {
    sqlRowNum = 0;
    sqlCode = 0;
    sqlErrtext = "";
    sqlDupl = false;
    if (empty(sql1)) {
      setSqlErrmsg("SQL is empty( String");
    }

    // if ( param == null && Arrays.asList("N","S").contains(parmFlag) ) {
    // param = nameSqlParm(sql1);
    // sql1=convertSQL;
    // }
    if (param == null && sqlParm.nameSqlParm(sql1)) {
      sql1 = sqlParm.getConvSQL();
      param = sqlParm.getConvParm();
    }

    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql1);) {
      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }
      sqlRowNum = ps.executeUpdate();
      ps.close();
    } catch (SQLException ex2) {
      rc = -1;
      log(commString.formatSqlString("sqlerr: " + sql1, param));
      if (getSqlErrmsg(ex2) != 0) {
        throw new IllegalArgumentException(ex2.getMessage());
      }
    } catch (Exception e) {
      rc = -1;
      log("sqlExec: " + sql1);
      setSqlErrmsg(e.getMessage());
      // if (sql_code ==-1) {
      throw new IllegalArgumentException(e.getMessage());
      // }
    }

  }


  public void sqlSet(int num, String fieldName, String setData) {
    // try {
    colHash.put(fieldName.trim().toUpperCase() + "-" + num, setData); // nvl(setData));
    // } catch (Exception ex) {
    // ddd("sql_set: "+ex.getMessage());
    // wp.expMethod = "sql_set";
    // wp.expHandle(ex);
    // }
  }

  public void sqlSetNum(int num, String fieldName, double numDou) {
    // try {
    String colName = "0";
    if ((numDou % 1) == 0) {
      colName = "" + (long) numDou;
    } else
      colName = "" + numDou;
    colHash.put(fieldName.trim().toUpperCase() + "-" + num, colName);
    // } catch (Exception ex) {
    // wp.expMethod = "sql_setNum";
    // wp.expHandle(ex);
    // }
  }

  public double sqlNum(String col1) {
    return sqlNum(0, col1);
  }

  public double sqlNum(int num, String fieldName) {
    String colName = "";
    // try {
    colName = (String) colHash.get(fieldName.trim().toUpperCase() + "-" + num);
    if (colName == null) {
      return 0;
    }
    if (colName.trim().length() == 0) {
      return 0;
    }

    return Double.parseDouble(colName.trim());
    // } catch (Exception ex) {
    // wp.expMethod = "sql_getnum";
    // wp.expHandle(ex);
    // }

    // return 0;
  }

  public int sqlInt(String col1) {
    return sqlInt(0, col1);
  }

  public int sqlInt(int num, String fieldName) {
    String colName = "";
    colName = (String) colHash.get(fieldName.trim().toUpperCase() + "-" + num);
    if (colName == null) {
      return 0;
    }
    if (colName.trim().length() == 0) {
      return 0;
    }
    // if (ss.indexOf(".")<0) {
    // return Integer.parseInt(ss.trim());
    // }

    return new BigDecimal(Double.parseDouble(colName.trim())).intValue();
  }

  public String sqlNvl(String col, String colName) {
    String rowName = sqlStr(0, col);
    if (empty(rowName))
      return colName;

    return rowName;
  }

  public String sqlStr(String col1) {
    return sqlStr(0, col1);
  }


  public String sqlStr(int num, String fieldName) {
    String retnStr = "";
    // try {
    // if (colName_find(fieldName.trim().toUpperCase() + "-" + kk))
    // return "";

    retnStr = (String) colHash.get(fieldName.trim().toUpperCase() + "-" + num);
    if (retnStr == null) {
      return "";
    }
    retnStr = retnStr.trim();
    // } catch (Exception ex) {
    // wp.expMethod = "sql_ss";
    // wp.expHandle(ex);
    // }
    //
    return retnStr;
  }

  public String[] getColNames() {
    return colName;
  }

  public String[] getColTypes() {
    return colType;
  }

  public int getColumnCnt() {
    return columnCnt;
  }

  protected void readColNames(java.sql.ResultSetMetaData mdata) throws SQLException {
    int liCnt = mdata.getColumnCount() + 1;
    columnCnt = liCnt;
    colName = new String[liCnt];
    colType = new String[liCnt];
    colName[0] = "";
    colType[0] = "";
    for (int ii = 1; ii < liCnt; ii++) {
      colName[ii] = mdata.getColumnLabel(ii);
      if (empty(colName[ii])) {
        colName[ii] = mdata.getColumnName(ii);
      }
      colType[ii] = mdata.getColumnTypeName(ii).toUpperCase();
    }
  }

  // protected void ppp(int ii,int num1) {
  // sqlParm.setInt(ii,num1);
  // }
  protected void setInt(int num1) {
    int ii = sqlParm.getParmcnt() + 1;
    sqlParm.setInt(ii, num1);
  }

  protected void setInt(int i, int parmNum) {
    sqlParm.setInt(i, parmNum);
    // sortHash.put(i, parmNum);
    // parmFlag = "N";
    // parmCnt = i;
    return;
  }

  protected void setInt(String parmField, int parmNum) {
    sqlParm.setInt(parmField, parmNum);
    // parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
    // parmFlag = "S";
    return;
  }

  // protected void ppp(int ii,long num1) {
  // sqlParm.setDouble(ii,num1);
  // }
  protected void setLong(long num1) {
    int ii = sqlParm.getParmcnt() + 1;
    sqlParm.setDouble(ii, num1);
  }

  protected void setLong(int i, long parmNum) {
    sqlParm.setDouble(i, parmNum);
    // sortHash.put(i, parmNum);
    // parmFlag = "N";
    // parmCnt = i;
    return;
  }

  protected void setLong(String parmField, long parmNum) {
    sqlParm.setDouble(parmField, parmNum);
    // parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
    // parmFlag = "S";
    return;
  }

  protected void setDouble(int ii, double num1) {
    sqlParm.setDouble(ii, num1);
  }

  protected void setDouble(double num1) {
    int ii = sqlParm.getParmcnt() + 1;
    sqlParm.setDouble(ii, num1);
  }

  // protected void setDouble(int i, double parmNum) {
  // sqlParm.setDouble(i, parmNum);
  // // sortHash.put(i, parmNum);
  // // parmFlag = "N";
  // // parmCnt = i;
  // return;
  // }

  protected void setDouble(String parmField, double parmNum) {
    sqlParm.setDouble(parmField, parmNum);
    // parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
    // parmFlag = "S";
    return;
  }

  protected void setString2(int ii, String strName) {
    sqlParm.setString(ii, strName);
  }

  protected void setString(String strName) {
    int ii = sqlParm.getParmcnt() + 1;
    sqlParm.setString(ii, strName);
  }

  protected void setString(int i, String parmString) {
    sqlParm.setString(i, parmString);
    // sortHash.put(i, parmString);
    // parmFlag = "N";
    // parmCnt = i;
    return;
  }

  protected Object[] sqlParm() {
    return sqlParm.getConvParm();
  }

  protected void setString2(String col, String val) {
    sqlParm.setString(col, val);
  }

  protected void setString(String parmField, String parmString) {
    sqlParm.setString(parmField, parmString);
    // parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmString);
    // parmFlag = "S";
    return;
  }

  protected void setRowId(String strName) {
    int ii = sqlParm.getParmcnt() + 1;
    sqlParm.setRowId(ii, strName);
  }

  protected void setRowid(int ppField, String ppString) {
    sqlParm.setRowId(ppField, ppString);
  }

  protected void setRowid(String ppField, String ppString) {
    sqlParm.setRowId(ppField, ppString);
  }

  protected boolean empty(String strName) {
    return commString.empty(strName);
    // if (s1 == null || s1.trim().length() == 0) {
    // return true;
    // }
    // return false;
  }

  protected String nvl(Object strName) {
    // /return commString.nvl(s1.toString());
    if (strName == null) {
      return "";
    }
    return strName.toString().trim();
  }

  protected void log(String strName) {
    System.out.println("BaseSQL:>>" + strName);
  }

  protected int getSqlErrmsg(SQLException ex2) {
    sqlRowNum = -1;
    sqlCode = -1;
    sqlErrorCode = ex2.getErrorCode();
    sqlErrtext = ex2.getMessage();
    if (sqlErrorCode == 1 || sqlErrorCode == -803) {
      sqlRowNum = 0;
      sqlDupl = true;
      sqlErrtext = "資料已存在, 不可新增";
      return 0;
    } else if (sqlErrorCode == -302) {
      sqlErrtext = "value too large";
      return 0;
    }
    return 1;
  }

  protected void setSqlErrmsg(String strName) {
    sqlRowNum = -1;
    sqlCode = -1;
    sqlErrorCode = -1;
    sqlErrtext = strName;
  }

  // boolean sql_errHandle(String a_msg) {
  // if (a_msg.toLowerCase().indexOf("sqlcode=-302") >= 0) {
  // sql_nrow = 0;
  // this.sql_errtext = "(value too large[302]); " + a_msg;
  // // printf(">>>sqlErr:" + sql_errtext);
  // return true;
  // }
  //
  // return false;
  // }

  // boolean colName_find(String col) {
  // if (empty(col))
  // return false;
  // if (colHash.containsKey(col.trim().toUpperCase())==false) {
  // ddd("warning: <<<?????>>>wp.col_name not file: "+col);
  // return false;
  // }
  // return true;
  // }
  public double getNumber(Connection conn, String sql1, Object... obj) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    double liNum = 0;
    sqlInit();

    if (empty(sql1)) {
      return 0;
    }
    String lsSql = sql1;
    Object[] param = null;
    if (obj == null && sqlParm.nameSqlParm(sql1)) {
      lsSql = sqlParm.getConvSQL();
      param = sqlParm.getConvParm();
    } else
      param = obj;

    try {
      ps =
          conn.prepareStatement(lsSql, ResultSet.TYPE_SCROLL_INSENSITIVE,
              ResultSet.CONCUR_READ_ONLY);
      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }

      rs = ps.executeQuery();
      if (rs.next()) {
        liNum = rs.getDouble(1);
      }
      rs.close();
      ps.close();
      rs = null;
      ps = null;
    } // End of try
    catch (Exception ex) {
      setSqlErrmsg(ex.getMessage());
      return 0;
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
      } catch (Exception ex2) {
      }
    }
    return liNum;
  }
  
  protected void setParm(int ii, String s1) {	  
	  sqlParm.setString(ii, s1);
  }
  protected void setParm(int ii, double num1) {
	  sqlParm.setDouble(ii, num1);
  }
  protected void setParm(int ii, int int1) {
	  sqlParm.setInt(ii, int1);
  }
  protected void setParm(String s1) {
	  sqlParm.setString(s1);
  }
  protected void setParm(double num1) {
	  sqlParm.setDouble(num1);
  }
  protected void setParm(int int1) {
	  sqlParm.setInt(int1);
  }

  
}
