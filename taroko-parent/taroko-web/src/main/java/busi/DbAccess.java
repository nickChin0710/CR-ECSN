/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-20  V1.00.01  Zuwei       code format                              *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*  109-09-28  V1.00.01  Zuwei       fix code scan issue      *
*  109-12-28  V1.00.02  Justin        zz -> comm
*  110-01-07  V1.00.03  tanwei      修改意義不明確變量 *
*  110-01-18  V1.00.04  Justin        change the parameter name of the debugMode
*  111-01-17  V1.00.05  Justin       logger -> getNormalLogger()
*  111-02-08  V1.00.06  Justin      output error messages of level D         *
*  111-02-21  V1.00.07  Justin      show exception messages                  *
*  111-06-28  V1.00.08  Justin      show exception messages                  *
*  111-11-28  V1.00.09  Zuwei       Sync from mega                           *
*  112-03-30  V1.00.10  Yang Bo     fix NullPointerException error           *
******************************************************************************/
package busi;
/** 資料庫公用程式
 * 2019-1224   JH    readonly
 * 2019-1209   JH    col_int()
 * 2019-0815   JH    ++ sqlSelect_wp
 * 2019-0812   JH    wp.ddd_sql()
 * 2019-0808   JH    sqlSelect(,ab_log)
   190313:     JH    sqlExec, sqlSelect
   190312:     JH    getMsg()
-->Oracle: 須用 metaData 之Name, DB2用 Label
Oracle: mdata.getColumnName(ii)
DB2: mdata.getColumnLabel(ii)
*/

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Dxc.Util.SecurityUtil;
import taroko.base.BaseData;
import taroko.base.CommString;

public class DbAccess {

  protected String[] colName = null;
  protected String[] colType = null;

  protected Connection conn = null;
  private HashMap<String, String> colHash = new HashMap<String, String>();
  // @SuppressWarnings("rawtypes")
  // public HashMap colHash = new HashMap();
  private HashMap<String, String> itemHash = new HashMap<String, String>();
private final String numberType=",DECIMAL,DOUBLE,LONG,INTEGER,";
  protected taroko.com.TarokoCommon wp;

  // -SQL-
private int fetchRows=100;
  private int selectLimit = 9999;
  public int sqlRowNum = 0;
  public int sqlCode = 0;
  protected boolean sqlDupl = false;
  public int sqlErrorCode = 0;
  protected String sqlErrtext = "";
  protected boolean sqlNotfind = false;

  // -code-
  protected final String sqlDate = " sysdate ";
  protected final String sqlYmd = " to_char(sysdate,'yyyymmdd') ";
  protected final String sqlHms = " to_char(sysdate,'hh24miss') ";
  protected final String sqlDual = " SYSIBM.SYSDUMMY1 ";

  protected String daoTid = "";
  // protected String dbUser="";
  protected int colrr = 0;

  // private HashMap<String,Object> parmHash = new HashMap<String,Object>();
  // private HashMap<Integer,Object> sortHash = new HashMap<Integer,Object>();
  // private int parmCnt=0;
  // private String parmFlag="",convertSQL="",internalCall="";
  taroko.base.SqlParm sqlParm = new taroko.base.SqlParm();
  protected taroko.base.CommSqlStr commSqlStr = new taroko.base.CommSqlStr();
  protected taroko.base.CommString commString = new taroko.base.CommString();

  // -Return MSG-
  protected String versionStr = "00"; // "0.00.0";
  protected int rc = 1;
  String retMsg = "";
  boolean isDebug = false;

  protected void debugMode(boolean isDebug) {
    this.isDebug = isDebug;
  }

  public void setConn(Connection con1) {
    conn = con1;
  }

  public void setSelectLimit(int aiMax) {
    selectLimit = aiMax;
  }

  protected void colToItem() {
    colHash.putAll(itemHash);
  }

  void readColNames(java.sql.ResultSetMetaData mdata) {
    try {
      colName = new String[mdata.getColumnCount() + 1];
      colType = new String[mdata.getColumnCount() + 1];
      colName[0] = "";
      colType[0] = "";
      for (int ii = 1; ii < colName.length; ii++) {
        colName[ii] = mdata.getColumnLabel(ii).trim();
        if (empty(colName[ii])) {
          colName[ii] = mdata.getColumnName(ii).trim();
        }
        colType[ii] = mdata.getColumnTypeName(ii).trim();
      }
    } catch (Exception ex) {
      expHandle("colName_get", ex);
    }
  }

  // -AA-setParm-value---------------------------
  protected void setInt(int i, int parmNum) {
    sqlParm.setInt(i, parmNum);
    // sortHash.put(i, parmNum);
    // parmFlag = "N";
    // parmCnt = i;
    // return;
  }

  protected void setInt(String parmField, int parmNum) {
    sqlParm.setInt(parmField, parmNum);
    // parmHash.put(""+(100 - parmField.trim().length())+":"+parmField.trim(), parmNum);
    // parmFlag = "S";
    return;
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
    // parmHash.put(""+(100 - parmField.trim().length())+":"+parmField.trim(), parmNum);
    // parmFlag = "S";
    return;
  }

  protected void setDouble(int i, double parmNum) {
    sqlParm.setDouble(i, parmNum);
    // sortHash.put(i, parmNum);
    // parmFlag = "N";
    // parmCnt = i;
    return;
  }

  protected void setDouble(String parmField, double parmNum) {
    sqlParm.setDouble(parmField, parmNum);
    // parmHash.put(""+(100 - parmField.trim().length())+":"+parmField.trim(), parmNum);
    // parmFlag = "S";
    return;
  }

  protected void setNumber(String col, double num1) {
    setDouble(col, num1);
  }

  protected void setNumber(String col) {
    setDouble(col, 0);
  }

  protected void setRowId(int ii, String parmRowId) // throws Exception
  {

    sqlParm.setRowId(ii, parmRowId);
    // sortHash.put(ii, hex2Byte(parmRowId));
    // parmFlag = "N";
    // parmCnt = ii;
    return;
  }

  protected void setRowId(String parmField, String parmRowId) // throws Exception
  {
    sqlParm.setRowId(parmField, parmRowId);
    // parmHash.put(""+(100 - parmField.trim().length())+":"+parmField.trim(), hex2Byte(parmRowId));
    // parmFlag = "S";
    return;
  }

  // byte[] hex2Byte(String s)
  // {
  // int len = s.length();
  // byte[] data = new byte[len / 2];
  // for (int i = 0; i < len; i += 2)
  // data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
  // + Character.digit(s.charAt(i+1), 16));
  // return data;
  // }

  protected void setParm(int i, int parmInt) {
	    sqlParm.setInt(i, parmInt);
  }
  protected void setParm(int i, double parmNum) {
	    sqlParm.setDouble(i, parmNum);
  }
  protected void setParm(int i, String parmString) {
	    sqlParm.setString(i, parmString);
  }
  protected void setString(int i, String parmString) {
    sqlParm.setString(i, parmString);
    // sortHash.put(i, parmString);
    // parmFlag = "N";
    // parmCnt = i;
    return;
  }

  protected void setString(String parmField, String parmString) {
    sqlParm.setString(parmField, parmString);
    // parmHash.put(""+(100 - parmField.trim().length())+":"+parmField.trim(), parmString);
    // parmFlag = "S";
    return;
  }

  protected void setEmptyString(String col) {
    setString(col, "");
  }

  // -VV------------------------------------------------------
  protected void sqlSelect(String sql1) {
    sqlSelect(sql1, null);
    return;
  }

  // public void col_Clear(String tid) {
  // for(String col : colName) {
  // col_set(tid+col,"");
  // }
  // }

  protected void sqlSelect(String sql1, Object param[]) {
    PreparedStatement ps = null;
    java.sql.ResultSet rs = null;
    sqlRowNum = 0;
    sqlCode = 0;
    sqlErrtext = "";
    sqlNotfind = true;

    // --
    if (param == null && sqlParm.nameSqlParm(sql1)) {
      sql1 = sqlParm.getConvSQL();
      param = sqlParm.getConvParm();
    }

    try {
	   //ResultSet.TYPE_SCROLL_INSENSITIVE--
      ps = conn.prepareStatement(sql1,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      ps.setFetchSize(fetchRows);
      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }

      if (wp != null)
        wp.logSql2(sql1, param);

      rs = ps.executeQuery();
      //rs.setFetchSize(1);

      java.sql.ResultSetMetaData mdata = rs.getMetaData();
      readColNames(mdata);
      // int cols = mdata.getColumnCount();
      int rr = -1;
      // -JH:2018-0608---
      while (rs.next()) {
        rr++;
        sqlRowNum = rr + 1;

        // -setdata-
        for (int ii = 1; ii < colName.length; ii++) {
            if (numberType.indexOf(colType[ii])>0) {
               colSet(rr,daoTid+colName[ii],commString.numFormat(rs.getDouble(ii),"#0.######"));
            }
            else {
               colSet(rr, daoTid + colName[ii], rs.getObject(ii));
            }
         }

        if (selectLimit > 0 && sqlRowNum >= selectLimit) {
          logErr("XXXXX select-筆數已超過 [%s] XXXXX", selectLimit);
          break;
        }
      }
      sqlNotfind = (sqlRowNum <= 0);

      setSelectLimit(9999);
      if (sqlNotfind) {
        logErr("N-find: " + sql1, param);
      }
    } catch (SQLException ex2) {
      logErr(sql1, param);
      rc = -1;
      logErr("err:" + sql1, ex2);
      int liExp = sqlErrmsg(ex2);
      if (liExp == 1) {
        expHandle("sqlSelect", ex2);
      }
    } catch (Exception e) {
      sqlCode = -1;
      sqlRowNum = -1;
      sqlErrorCode = -1;
      sqlErrtext = e.getMessage();
      logErr("ERROR-DDD->SQL=" + sql1);
      expHandle("sqlSelect", e);
      logErr("err:" + sql1, e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
		  rs =null;
        }
        if (ps != null) {
          ps.close();
		  ps =null;
        }
      } catch (Exception ex2) {
      }
    }

    daoTid = "";
    return;
  }

  protected void sqlSelectWp(String sql1) {
    sqlSelectWp(sql1, null);
  }

  protected void sqlSelectWp(String sql1, Object param[]) {
    if (wp == null) {
      sqlSelect(sql1, param);
      return;
    }

    PreparedStatement ps = null;
    java.sql.ResultSet rs = null;
    sqlRowNum = 0;
    sqlCode = 0;
    sqlErrtext = "";
    sqlNotfind = true;

    // --
    if (param == null && sqlParm.nameSqlParm(sql1)) {
      sql1 = sqlParm.getConvSQL();
      param = sqlParm.getConvParm();
    }

    try {
      ps = conn.prepareStatement(sql1,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
      ps.setFetchSize(fetchRows);

      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }

      if (wp != null)
        wp.logSql2(sql1, param);

      rs = ps.executeQuery();
      rs.setFetchSize(1);

      java.sql.ResultSetMetaData mdata = rs.getMetaData();
      readColNames(mdata);
      // int cols = mdata.getColumnCount();
      int num = -1;
      // -JH:2018-0608---
      while (rs.next()) {
        num++;
        sqlRowNum = num + 1;

        // -setdata-
        for (int ii = 1; ii < colName.length; ii++) {
          // wp.col_set(rr, daoTid + colName[ii], rs.getObject(ii).toString());
          String colStr = "";
          String colNameStr = daoTid + colName[ii];
            if (numberType.indexOf(colType[ii])>0) {
               if (rs.getObject(ii)!=null)
                  wp.setNumber(colNameStr,rs.getDouble(ii),num);
               else wp.colSet(num,colNameStr,"0");

          } else {
            if (rs.getObject(ii) != null) {
              wp.colSet(num, colNameStr, rs.getObject(ii).toString());
            } else
              wp.colSet(num, colNameStr, "");
          }
        }

        if (selectLimit > 0 && sqlRowNum >= selectLimit) {
          logErr("XXXXX select-筆數已超過 [%s] XXXXX", selectLimit);
          break;
        }
      }
      sqlNotfind = (sqlRowNum <= 0);
      if (sqlRowNum == 0) {
        sqlCode = 100;
        logErr(sql1, param);
      }
      setSelectLimit(9999);
    } catch (SQLException ex2) {
      rc = -1;
      logErr(sql1, param);
      int liExp = sqlErrmsg(ex2);
      if (liExp == 1) {
        expHandle("sqlSelect", ex2);
      }
    } catch (Exception e) {
      sqlCode = -1;
      sqlRowNum = -1;
      sqlErrorCode = -1;
      sqlErrtext = e.getMessage();
      logErr("-DDD->SQL=" + sql1);
      expHandle("sqlSelect", e);
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
    return;
  }

  public double getNumber(String sql1, Object... obj) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    double liNum = 0;

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
      sqlErrmsg(ex.getMessage());
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

  public List<Map<String, String>> sqlQuery(String sql1) {
    return sqlQuery(sql1, null);
  }

  public List<Map<String, String>> sqlQuery(String sql1, Object[] param) {
    // msgOK();
    sqlRowNum = 0;
    sqlCode = 0;
    sqlErrtext = "";

    java.sql.ResultSet rs = null;
    List<Map<String, String>> colList = new ArrayList<>();

    if (param == null && sqlParm.nameSqlParm(sql1)) {
      sql1 = sqlParm.getConvSQL();
      param = sqlParm.getConvParm();
    }

	//ResultSet.TYPE_SCROLL_INSENSITIVE--
	try (java.sql.PreparedStatement ps = conn.prepareStatement(sql1,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);) {
      ps.setFetchSize(fetchRows);

      if (param != null) {
        for (int ii = 0; ii < param.length; ii++) {
          ps.setObject(ii + 1, param[ii]);
        }
      }

      if (wp != null)
        wp.logSql2(sql1, param);

      rs = ps.executeQuery();
      java.sql.ResultSetMetaData mdata = rs.getMetaData();
      // int cols = mdata.getColumnCount();
      readColNames(mdata);
      while (rs.next()) {
        sqlRowNum++;

        Map<String, String> map = new HashMap<>();
        for (int ii = 1; ii < colName.length; ii++) {
			   String ss=rs.getObject(ii) == null ? "" : rs.getObject(ii).toString();
			   if (numberType.indexOf(colType[ii])>0) {
			      ss =commString.numFormat(rs.getDouble(ii),"#0.#######");
            }
            map.put(daoTid + colName[ii], nvl(ss));
        }
        colList.add(map);
        if (selectLimit > 0 && sqlRowNum >= selectLimit) {
          logErr("XXXXX select-筆數已超過 [%s] XXXXX", selectLimit);
          break;
        }
      }

      if (sqlRowNum == 0) {
        sqlNotfind = true;
        logErr(sql1, param);
      }

      daoTid = "";
      setSelectLimit(9999);

    } catch (SQLException ex2) {
      rc = -1;
      logErr("sqlerr: " + sql1, param);
      int liExp = sqlErrmsg(ex2);
      if (liExp == 1) {
        expHandle("sqlExec", ex2);
      }
    } catch (Exception e) {
      rc = -1;
      sqlErrtext = e.getMessage();
      expHandle("sqlQuery", e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (Exception ex2) {
      }
    }

    return colList;
  }

  // --

  // --sql:insert,update,delete--
  protected int sqlExec(String sql1) {
    return sqlExec(sql1, null, true);
  }
protected int sqlExec(String sql1, boolean bLog) {
   return sqlExec(sql1, null, bLog);
}
protected int sqlExec(String sql1, Object[] objs) {
   return sqlExec(sql1, objs, true);
}

  protected int sqlExec(String sql1, Object param[], boolean bLog) {
    sqlRowNum = 0;
    sqlCode = 0;
    sqlDupl = false;
    sqlErrtext = "";

    // msgOK();

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
      // -SQL-log-
      if (wp != null && bLog) {
        wp.logSql(sql1, param);
      }

      sqlRowNum = ps.executeUpdate();
      // if (sqlRowNum == 0) {
      // rc = 0;
      // }
    } catch (SQLException ex2) {
      rc = -1;
      logErr("sqlExec: " + sql1, param);
      logErr("Error", ex2);
      int liExp = sqlErrmsg(ex2);
      if (liExp == 1) {
        expHandle("sqlExec", ex2);
      }
    } catch (Exception ex) {
      sqlRowNum = -1;
      sqlCode = -1;
      sqlErrorCode = -1;
      rc = -1;
      logErr("sqlExec: " + sql1, param);
      expHandle("sqlExec", ex);
      logErr("Error", ex);
    }

    return rc;
  }

  boolean itemNameFind(String col) throws Exception {
    if (itemHash.containsKey(col.trim().toUpperCase()) == false) {
      throw new Exception("item name not find: " + col);
      // this.expHandle(new Exception("input Name not find:"+col));
    }
    return true;
  }

  // boolean colName_find(String col) throws Exception {
  // if (colHash.containsKey(col.trim().toUpperCase())==false) {
  // throw new Exception("<<<?????>>>col_ss.Key.noFind: "+col);
  // }
  // return true;
  // }

  // --HTML-value: item-func--
  public void varsClear() {
    itemHash.clear();
  }

  public void itemClear() {
    itemHash.clear();
  }

  public String varsNvl(String colStr, String strName) {
    String colName = "";
    try {
      // itemName_find(col1);
      colName = (String) itemHash.get(colStr.trim().toUpperCase());
      if (colName == null) {
        return strName;
      }
    } catch (Exception ex) {
      expHandle("item_ss", ex);
    }
    return nvl(colName);
  }

  public String varsStr(String colStr) {
    String colName = "";
    try {
      // itemName_find(col1);
      colName = (String) itemHash.get(colStr.trim().toUpperCase());
      if (colName == null) {
        logErr("<<<itemHash.key=" + colStr + "; not find>>>");
      }
    } catch (Exception ex) {
      expHandle("item_ss", ex);
    }
    return nvl(colName);
  }

  // public String item_ss(String col1) {
  // return vars_ss(col1);
  // }
  public int varsInt(String colStr) {
    String ss = "";
    ss = varsStr(colStr);
    try {
      return Integer.parseInt(ss);
    } catch (Exception ex) {
      return 0;
    }
  }

  public double varsNum(String colStr) {
    String ss = "";
    ss = varsStr(colStr);
    try {
      return Double.parseDouble(ss);
    } catch (Exception ex) {
      return 0;
    }
  }

  // public double item_num(String col) {
  // return vars_num(col);
  // }

  public void varsSet(String colStr, String strName) {
    if (nvl(colStr).length() == 0) {
      return;
    }
    try {
      colStr = colStr.trim().toUpperCase();
      if (colStr.equalsIgnoreCase("mod_seqno")) {
        if (strName == null || strName.length() == 0) {
          itemHash.put(colStr, "0");
        } else {
          itemHash.put(colStr, strName);
        }
      } else {
        itemHash.put(colStr, nvl(strName));
      }
    } catch (Exception ex) {
      expHandle("vars_set", ex);
    }
  }

  // public void item_set(String col, String s1) {
  // wp.item_set(col,s1);
  // }

  // --DB-set-value-
  protected void colSet(int rMount, String colStr, Object obj) {
    if (nvl(colStr).length() == 0)
      return;

    try {
      if (obj == null) {
        colHash.put(nvl(colStr).toUpperCase() + "-" + rMount, "");
      } else {
        colHash.put(nvl(colStr).toUpperCase() + "-" + rMount, nvl(obj.toString()));
      }
    } catch (Exception ex) {
      expHandle("col_set", ex);
    }
  }

  protected void colSet(String colStr, Object obj) {
    colSet(0, colStr, obj);
  }

  // --DB:-get-value-
  // public void list_fetch(int ll) {
  // colHash = (HashMap<String, String>) colList.get(ll);
  // }
  //
  // public void list_2Col(int ll) {
  // colHash = (HashMap<String, String>) colList.get(ll);
  // }

  public String colNvl(String colStr, String strName) {
    String colName = colStr(colStr);
    if (empty(colName))
      return strName;
    return colName;
  }

  public String colStr(int rMount, String colStr) {
    // colrr =rr;
    colStr = colStr.trim().toUpperCase() + "-" + rMount;
    String colName = "";
    try {
      // colName_find(col);
      colName = (String) colHash.get(colStr);
      if (colName == null) {
        logErr("<<<XXXXX>>>col_ss.Key[%s].not-find", colStr);
        return "";
      }
    } catch (Exception ex) {
      expHandle("col_ss", ex);
    }
    return colName.trim();
  }

  public String colStr(String colStr) {
    return colStr(0, colStr);
  }

  public double colNum(String colStr) {
    return colNum(0, colStr);
  }

  public double colNum(int rMount, String colStr) {
    String colName = "";
    try {
      // colName_find(col+"-"+rr);
      colName = colStr(rMount, colStr);
      return Double.parseDouble(colName);
    } catch (Exception ex) {
      // expHandle("col_num", ex);
    }
    return 0;
  }

  public int colInt(String colStr) {
    return colInt(0, colStr);
  }

  public int colInt(int rMount, String colStr) {
    int liNum = 0;
    try {
      // colName_find(col);
      liNum = (int) Double.parseDouble(colStr(rMount, colStr));
    } catch (Exception ex) {
      // expHandle("col_int", ex);
      // ddd("col_int.error="+ex.getMessage());
      liNum = 0;
    }
    return liNum;
  }

  protected boolean colEqIgno(String colStr, String strName) {
    if (colStr == null || strName == null) {
      return false;
    }
    return (colStr(colrr, colStr).equalsIgnoreCase(strName));
  }

  //
  // protected boolean col_eqAny(String col, String s1) {
  // if (col == null || s1 == null) {
  // return false;
  // }
  // return col_ss(colrr,col).equalsIgnoreCase(s1);
  // }

  protected boolean colNeq(String colStr, String strName) {
    return !colEq(colStr, strName);
  }

  protected boolean colEq(String colStr, String strName) {
    if (colStr == null || strName == null) {
      return false;
    }
    return (colStr(colrr, colStr).equals(strName));
  }

  protected boolean colEq(String colStr, double num) {
    if (colStr == null) {
      return false;
    }

    return (colNum(colrr, colStr) == num);
  }

  protected int colLen(String colStr) {
    return colStr(colrr, colStr).length();
  }

  protected boolean colEmpty(String colStr) {
    return (colStr(colrr, colStr).length() == 0);
  }

  protected String sqlRownum(int num) {
    return " fetch first " + num + " rows only ";
  }

  // -commit+rollback-
  protected int sqlCommit(int aiCommit) {
    try {
      if (aiCommit == 1) {
        conn.commit();
      } else {
        conn.rollback();
      }
    } catch (Exception ex) {
      errmsg("db-commit/rollback error; " + ex.getMessage());
    }

    return rc;
  }

  // --String-func----------------------
  protected String nvl(String strName) {
    if (strName == null) {
      return "";
    }
    return strName.trim();
  }

  protected String nvl(String strName, String nvlName) {
    if (empty(strName)) {
      if (nvlName == null) {
        return "";
      }
      return nvlName;
    }
    return strName.trim();
  }

  // -return errMSG-----------------
  protected void errmsg(String strName, Object... obj) {
    rc = -1;
    if (empty(strName) && obj.length == 0)
      return;

    retMsg = commString.formatSqlString(strName, obj);
  }

  public String mesg() {
    return getMsg();
  }

  public String getMsg() {
    if (empty(retMsg))
      return "";
    // if (retMsg.indexOf("(V."+versionStr+")")>0)
    // return retMsg;

    return retMsg; // + " (V." + versionStr + ")";
  }

  protected void msgOK() {
    rc = 1;
    retMsg = "";
    sqlErrtext = "";
  }

  // private String logParm(String s1,Object[] obj) {
  // if (obj==null || obj.length==0)
  // return s1;
  //
  // for(int ii=0; ii<obj.length; ii++) {
  // if (obj[ii]==null)
  // obj[ii] ="NULL";
  // }
  // String ss = s1.replaceAll("\\?","'%s'");
  // //return String.format(ss,obj);
  //
  // for(int ii=0; ii<obj.length; ii++) {
  // ss =ss.replaceFirst("%s",obj[ii].toString());
  // }
  // return ss;
  // }
  protected void log(String strName, Object... args) {
    if (empty(strName) && args.length == 0)
      return;

    String colName = strName;
    colName = this.getClass().getSimpleName() + ":" + commString.formatSqlString(strName, args);
    if (wp != null && BaseData.getNormalLogger() != null) {
      wp.log(colName);
    } else {
//      System.out.println(ss);
    }
  }
  
	protected void logErr(String strName, Object... args) {
		if (empty(strName) && args.length == 0)
			return;

		String errInfo = this.getClass().getSimpleName() + ":" + commString.formatSqlString(strName, args);
		if (BaseData.getNormalLogger() != null) {
			BaseData.getNormalLogger().info(CommString.validateLogData(errInfo));
		}
	}

	protected void logErr(String error, Exception ex) {
		if (empty(error) || ex == null)
			return;

		if (BaseData.getNormalLogger() != null) {
			BaseData.getNormalLogger().error(error, ex);
		}
	}


  // public void ddd(String s1, Object[] obj) {
  // // if (! ib_debug) return;
  // String ss = s1;
  //
  // for (Object o1 : obj) {
  // ss = ss.replaceFirst("\\?", o1.toString());
  // }
  // if (wp==null) {
  // System.out.println("-DDD->" + this.getClass().getSimpleName() + ":" + ss);
  // }
  // else {
  // wp.ddd(this.getClass().getSimpleName() + ":" + ss);
  // }
  // }
  protected boolean empty(String strName) {
    if (strName == null || strName.trim().length() == 0) {
      return true;
    }
    return false;
  }

  void expHandle(String strName, Exception ex) {
    sqlErrtext = ex.getMessage();
    if (sqlErrtext.toLowerCase().indexOf("sqlstate=23505") > 0) {
      sqlErrtext = "資料已存在, 不可新增";
    }
    errmsg(strName + ">> " + ex.getMessage());
    // if (wp != null) {
    // wp.expMethod = s1;
    // wp.expHandle(ex);
    // } else ddd(s1 + ">> " + ex.getMessage());
    // throw new Exception(ex.getMessage());
  }

  int sqlErrmsg(SQLException ex2) {
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
      sqlRowNum = 0;
      sqlErrtext = "value too large";
      return 0;
    }
    return 1;
  }

  void sqlErrmsg(String strName) {
    sqlRowNum = -1;
    sqlCode = -1;
    sqlErrorCode = -1;
    sqlErrtext = strName;
  }

  // boolean sql_errHandle(String a_msg) {
  // if (a_msg.toLowerCase().indexOf("sqlcode=-302") >= 0) {
  // sqlRowNum = 0;
  // this.sqlErrtext = "(value too large[302]); " + a_msg;
  // // printf(">>>sqlErr:" + sqlErrtext);
  // return true;
  // }
  //
  // return false;
  // }

}
