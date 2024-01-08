/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*  110-01-08  V1.00.02  tanwei     修改意義不明確變量                                                                      *   
******************************************************************************/
package ofcapp;

import java.sql.*;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "deprecation"})
public class DataStore {
  String dbType = "ORACLE";

  public String strSelect = "";
  public String strWhere = "";
  public String strTable = "";
  public int selectCnt = 0;

  private String columnString = "";
  int maxColSize = 300;
  private String[] columnName = new String[maxColSize];
  private String[] dataType = new String[maxColSize];
  private Integer[] dataLength = new Integer[maxColSize];
  private String[] parmType = new String[maxColSize];
  private String[] parmData = new String[maxColSize];
  private String[] secureData = new String[maxColSize];

  @SuppressWarnings("rawtypes")
  private HashMap outputHash = new HashMap();

  int columnCnt = 0;
  int parmCnt = 0;

  public void sqlSelect(Connection conn) throws Exception {
    PreparedStatement ps = null;
    ResultSet rs = null;
    String lsSql = "";
    // String selectColumn="";
    selectCnt = 0;

    try {
      resetOutputData();

      if (strSelect.length() == 0) {
        procAllColumn(conn);
      } else {
        procColName();
        procSelectColumn();
      }

      lsSql = "SELECT " + columnString + " FROM " + strTable + " " + strWhere;

      ps =
          conn.prepareStatement(lsSql, ResultSet.TYPE_SCROLL_INSENSITIVE,
              ResultSet.CONCUR_READ_ONLY);
      setParmData(ps, 1);
      clearParmData();
      rs = ps.executeQuery();

      while (rs.next()) {
        selectCnt++;
        for (int k = 0; k < columnCnt; k++) {
          if (dataType[k].equals("BLOB")) {
            // wp.blobValue = rs.getBlob(k+1);
            continue;
          }

          setValue(columnName[k], rs.getString(k + 1), selectCnt - 1);
        }
      }

      rs.close();
      ps.close();
      rs = null;
      ps = null;
    } // End of try
    catch (Exception ex) {
      throw new Exception("DataSelect: " + lsSql + "; error=" + ex.getMessage());
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

    return;
  } // End of selectTable

  @SuppressWarnings("unchecked")
  void setValue(String fieldName, String setData, int num) {
    if (setData == null) {
      setData = "";
    }

    setData = setData.trim();
    fieldName = fieldName.toUpperCase() + "_" + num;
    outputHash.put(fieldName, setData);

    return;
  }

  public String getValue(int num, String fieldName) {
    String colName = "";

    fieldName = fieldName.toUpperCase() + "_" + num;
    colName = (String) outputHash.get(fieldName);
    if (colName == null) {
      return "";
    }

    return colName.trim();
  }

  public double getNumber(int num, String fieldName) {
    String colName = "";

    fieldName = fieldName.toUpperCase() + "_" + num;
    colName = (String) outputHash.get(fieldName);
    if (colName == null) {
      return 0;
    }

    return Double.parseDouble(colName);
  }

  void resetOutputData() throws Exception {
    outputHash.clear();
    return;
  }

  private boolean procColName() throws Exception {
    int i = 0, k = 0, pnt = 0;
    String fieldString = "", convertSQL = "";
    String[] splitString;
    byte[] cvtData;

    convertSQL = strSelect;
    // if ( !wp.dbType[dbPnt].equals("SYBASE_ASE") && !wp.dbType[dbPnt].equals("SYBASE_IQ") )
    // { convertSQL = columnData.toUpperCase(); }

    convertSQL = convertSQL.replaceAll(" as ", " AS ");
    cvtData = convertSQL.getBytes();
    int quoCnt = 0;
    for (i = 0; i < cvtData.length; i++) {
      if (cvtData[i] == '(') {
        quoCnt++;
      } else if (cvtData[i] == ')') {
        quoCnt--;
        cvtData[i] = '#';
      }

      if (cvtData[i] == ',' && quoCnt > 0) {
        cvtData[i] = '#';
      }
    }

    convertSQL = new String(cvtData);
    splitString = convertSQL.split(",");
    i = 0;
    k = 0;
    for (i = 0; i < splitString.length; i++) {
      fieldString = splitString[i];
      pnt = splitString[i].indexOf(" AS ");
      if (pnt != -1) {
        fieldString = splitString[i].substring(pnt + 4);
      } else {
        pnt = splitString[i].lastIndexOf("(");
        if (pnt != -1) {
          fieldString = splitString[i].substring(pnt + 1);
          pnt = fieldString.indexOf("#");
          fieldString = fieldString.substring(0, pnt);
        }
      }
      columnName[k] = fieldString.trim();
      k++;
    }

    columnCnt = k;
    for (i = 0; i < columnCnt; i++) {
      pnt = columnName[i].indexOf(".");
      columnName[i] = columnName[i].substring(pnt + 1);
      dataType[i] = "";
    }

    return true;
  }

  void procAllColumn(Connection conn) {
    Statement st = null;
    ResultSet rs = null;
    try {
      st = conn.createStatement();
      rs = st.executeQuery("SELECT * FROM " + strTable.toUpperCase());
      ResultSetMetaData md = rs.getMetaData();
      columnCnt = md.getColumnCount();
      for (int i = 0; i < md.getColumnCount(); i++) {
        columnName[i] = md.getColumnName(i + 1);
        dataType[i] = md.getColumnTypeName(i + 1);
        dataLength[i] = md.getPrecision(i + 1);
      }

      rs.close();
      st.close();
      st = null;
      rs = null;
      md = null;
    } catch (Exception ex) {
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (st != null) {
          st.close();
        }
      } catch (Exception ex2) {
      }
    }
    return;
  }

  private void procSelectColumn() throws Exception {
    // String selectColumn="";
    if (strSelect.length() > 0) {
      columnString = strSelect;
      return;
    }

    StringBuffer strbuf = new StringBuffer();
    columnString = "";
    for (int i = 0; i < columnCnt; i++) {
      if (dataType[i].equals("DATE") && dbType.equalsIgnoreCase("ORACLE")) {
        strbuf.append("NVL(TO_CHAR(" + columnName[i] + ",'YYYYMMDDHH24MISS'),' '),");
      } else {
        strbuf.append(columnName[i] + ",");
      }
    }

    columnString = strbuf.toString();
    columnString = columnString.substring(0, columnString.length() - 1);

    return;
  }

  void setParmData(PreparedStatement ps, int startPnt) throws Exception {
    for (int i = startPnt; i <= parmCnt; i++) {
      secureData[i] = parmData[i];
      if (parmType[i].equals("S")) {
        ps.setString(i, secureData[i]);
      } else {
        Double cvtDouble = new Double(secureData[i]);
        ps.setDouble(i, cvtDouble);
      }
    }
    return;
  }

  void clearParmData() throws Exception {
    for (int i = 1; i <= parmCnt; i++) {
      parmType[i] = "";
      parmData[i] = "";
    }

    parmCnt = 0;
    return;
  }
}
