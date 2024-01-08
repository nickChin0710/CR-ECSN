/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-24  V1.00.01  Zuwei       coding standard      *
*  110-01-08  V1.00.02   shiyuqi       修改无意义命名  
*  110-01-13  V1.00.02  Justin        modified for parameterize sql
******************************************************************************/
package taroko.com;

import java.sql.*;
import java.util.*;

/*
 * JH: 105-06-05 exec_select()
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoDAO {
  private String columnString = "", numberString = "", valueString = "", columnValue = "",
      cvtWhere = "";
  private String confirmTable = "", confirmParmKey = "", logKeySeq = "", comName = "",
      dateString = "";
  private int retCode = 0, startParm = 0, updateColCount = 0;
  private boolean confirmMode = false, specControl = false, saveMode = false;

  private int maxColSize = 300;
  public String[] updateType = new String[maxColSize];
  public String[] updateValue = new String[maxColSize];
  public String[] updateColumn = new String[maxColSize];
  public String[] columnName = new String[maxColSize];
  public String[] dataType = new String[maxColSize];
  private String[] secureData = new String[maxColSize];
  public Integer[] dataLength = new Integer[maxColSize];

  private String[] splitCmd = new String[30];

  public TarokoCommon wp = null;
  public boolean logMode = false;
  public int columnCnt = 0;
  // public int ii_conn_indx=0;
  private int dbPnt = 0;
  // -JH:add------------------
  public String daoTid = "";
  public int sqlCount = 0;
  public boolean isDspSql = false;
  public int sqlRowNum = 0;

  private HashMap<String, String> colHash = new HashMap<String, String>();

  private HashMap<String, Object> parmHash = new HashMap<String, Object>();
  private HashMap<Integer, Object> sortHash = new HashMap<Integer, Object>();
  private int parmCnt = 0;
  private String parmFlag = "", convertSQL = "", internalCall = "";
  private String convertWhereStr;

  public void setEcsDB() {
    dbPnt = 0;
  }

  public Connection getConn() {
    return wp.conn[dbPnt];
  }

  public boolean confirmInsert() throws Exception {
    if (wp.errorInput) {
      return false;
    }

    confirmTable = wp.daoTable;
    logKeySeq = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    wp.setParameter("LOG_KEY_SEQ", logKeySeq);
    realTableInsert();

    confirmMode = true;
    insertLogMaster("A");
    confirmMode = false;

    return true;
  }

  public boolean insertTable() throws Exception {
    if (wp.errorInput) {
      return false;
    }

    logKeySeq = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    wp.setParameter("LOG_KEY_SEQ", logKeySeq);
    realTableInsert();

    if (logMode) {
      insertLogMaster("A");
    }
    return true;
  }

  public boolean realTableInsert() {
    PreparedStatement ps = null;

    try {
      String insertData = "", insertValue = "";
      int int1 = 0;

      wp.dateTime();
      String startTime = wp.sysTime + wp.millSecond;
      wp.showLogMessage("D", "insert " + wp.daoTable.toUpperCase(), "started");
      wp.dupRecord = "";

      processColumnName("");

      columnString = "";
      valueString = "";
      
      String dbType = TarokoParm.getInstance().getDbType()[dbPnt];

      for (int i = 0; i < columnCnt; i++) {
        columnString = columnString + columnName[i] + ",";
        if (dataType[i].equals("DATE") && dbType.equals("ORACLE")) {
          valueString = valueString + "TO_DATE(?,'YYYYMMDDHH24MISS'),";
        } else {
          valueString = valueString + "?,";
        }
      }

      columnString = columnString.substring(0, columnString.length() - 1);
      valueString = valueString.substring(0, valueString.length() - 1);

      wp.sqlCmd =
          "INSERT INTO " + wp.daoTable + " ( " + columnString + " ) VALUES " + " ( " + valueString
              + " ) ";
      ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd);
      dateString = wp.sysDate + wp.sysTime;
      if (dbType.equals("SYBASE_ASE") || dbType.equals("SYBASE_IQ")
          || dbType.equals("MYSQL")) {
        dateString = wp.dispDate + "T" + wp.dispTime + ":" + wp.millSecond;
      } else if (dbType.equals("SQLserver")) {
        dateString = wp.sqlTime;
      }

      for (int i = 0; i < columnCnt; i++) {
        int1 = wp.insertPnt;
        String[] tmpString = wp.getInBuffer(columnName[i]);
        if (dataType[i].equals("NUMBER") || dataType[i].equals("NUMERIC")
            || dataType[i].equals("DECIMAL")) {
          if (insertData.length() == 0) {
            insertData = "0";
          }
        }
        if (int1 >= tmpString.length) {
          insertData = tmpString[0];
        } else {
          insertData = tmpString[int1];
        }

        if (dataType[i].equals("BLOB")) {
          ps.setBytes(i + 1, insertData.getBytes());
        } else if (dataType[i].equals("NUMERIC") || dataType[i].equals("NUMERIC")
            || dataType[i].equals("DECIMAL")) {
          Double cvtDouble = new Double(insertData);
          ps.setDouble(i + 1, cvtDouble);
        } else {
          ps.setString(i + 1, insertData);
        }
      }

      try {
        retCode = ps.executeUpdate();
      } catch (SQLException ex) {
        if (ex.getErrorCode() == 1) {
          wp.dupRecord = "Y";
          wp.showLogMessage("W", "insert " + wp.daoTable.toUpperCase(), " DMPDuplicate");
          ps.close();
          ps = null;
          return false;
        } else {
          wp.errSql = "Y";
          wp.expMethod = "realTableInsert";
          wp.expHandle(ex);
          return false;
        }
      }

      ps.close();
      ps = null;
      wp.sqlCmd = "";
      dbPnt = 0;

      if (retCode == 0) {
        wp.notFound = "Y";
        wp.dispMesg = "新增錯誤";
        wp.showLogMessage("E", wp.dispMesg + " " + wp.daoTable, "");
      }

      wp.insertCnt++;
      wp.durationTime(startTime, "insert " + wp.daoTable.toUpperCase());
      wp.showLogMessage("D", "insert " + wp.daoTable.toUpperCase(), "ended");
    }

    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "realTableInsert";
      wp.expHandle(ex);
      return false;
    }

    finally {
      try {
        if (ps != null) {
          ps.close();
        }
      } catch (Exception ex2) {
      }
    }

    return true;
  } // End of realTableInsert

  public boolean selectTable() {
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      // String selectColumn="";
      if (wp.comSelect) {
        if (wp.daoTable.trim().equals(wp.comTable)) {
          wp.comSelect = false;
          daoTid = "";
          wp.whereStr = "";
          wp.actionCode = "";
          return false;
        }
      }

      wp.dateTime();
      String startTime = wp.sysTime + wp.millSecond;

      // wp.showLogMessage("D","select "+wp.daoTable.toUpperCase(),"started");

      wp.notFound = "";

      if (wp.sqlCmd.length() > 6) {
        if (!specialSelect()) {
          return false;
        }
      }

      cvtWhere = wp.whereStr;
      if (wp.actionCode.equals("B") || wp.actionCode.equals("Q")) {
        internalCall = "Y";
        selectTotal();
      }

      if (wp.svFlag.equals("Y") && wp.queryWhere.length() == 0) {
        wp.queryWhere = wp.whereStr + " " + wp.whereOrder;
        wp.svFlag = "";
      }

//      wp.sqlCmd =
//          "SELECT " + wp.selectSQL + " FROM " + wp.daoTable + " " + wp.whereStr + " "
//              + wp.whereOrder;
//      wp.selectSQL = "";
      
      Object[] param = null;
      
      if (Arrays.asList("N", "S").contains(parmFlag)) {
			if (wp.sqlCmd.length() == 0) {
				param = setSqlParm(wp.selectSQL, wp.daoTable, wp.whereStr);
				cvtWhere = getConvertWhereStr();
			} else {
				param = setSqlParm(wp.sqlCmd);
			}	
			wp.sqlCmd = convertSQL;
      }
      wp.selectSQL = "";

      if (wp.firstBrowse.equals("Y") || wp.secondBrowse.equals("Y")) {
        if (wp.secondBrowse.equals("Y")) {
          wp.pageSQL2 = cvtWhere;
        } else {
          wp.pageSQL1 = cvtWhere;
        }

        wp.rowSQL = wp.queryWhere;
        if (wp.rowSQL.length() == 0) {
          wp.rowSQL = cvtWhere;
        }
        wp.firstBrowse = "";
        wp.secondBrowse = "";
        wp.queryWhere = "";
      }
      if (isDspSql) {
        wp.log("SQL:" + wp.sqlCmd);
      }

      
      ps =
          wp.conn[dbPnt].prepareStatement(wp.sqlCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,
              ResultSet.CONCUR_READ_ONLY);
      setParmData(ps, param, 0);
      clearParmData();
      rs = ps.executeQuery();
      // JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ
      java.sql.ResultSetMetaData mdata = rs.getMetaData();
      columnCnt = mdata.getColumnCount();

      if (wp.firstRow <= 0) {
        rs.beforeFirst();
      } else {
        rs.absolute(wp.firstRow);
      }

      if (wp.pageRows == 0) {
        wp.pageRows = 20;
      }

      // rs.setFetchSize(wp.pageRows);
      int i = wp.sumLine;
      int j = wp.firstRow;

      saveMode = false;
      int limitCnt = 0;
      if (wp.varRows > 0) {
        limitCnt = wp.varRows;
      } else {
        limitCnt = wp.pageRows + wp.varRows + wp.sumLine;
      }

      String str = "";
      String[] col = new String[columnCnt + 1];
      String[] colType = new String[columnCnt + 1];
      col[0] = "";
      colType[0] = "";
      for (int ii = 1; ii <= columnCnt; ii++) {
        col[ii] = mdata.getColumnLabel(ii).trim();
        if (col[ii] == null || col[ii].length() == 0) {
          col[ii] = mdata.getColumnName(ii).trim();
        }
        colType[ii] = mdata.getColumnTypeName(ii);
        // wp.ddd("col="+col[ii]+", type="+mdata.getColumnTypeName(ii)+","+mdata.getColumnType(ii));
      }
      while (rs.next()) {

        if (i >= limitCnt) {
          break;
        }

        for (int k = 1; k <= columnCnt; k++) {
          // if ( mdata.getColumnTypeName(k).equalsIgnoreCase("BLOB") ) {
          if (colType[k].equalsIgnoreCase("BLOB")) {
            wp.blobValue = rs.getBlob(k);
          } else {
            str = "";
            if (colType[k].equalsIgnoreCase("DECIMAL")) {
              if (rs.getObject(k) != null)
                str = rs.getDouble(k) + "";
              else
                str = "0";
            } else {
              if (rs.getObject(k) != null)
                str = rs.getObject(k).toString().trim();
              else
                str = "";
            }
            wp.setValue(daoTid + col[k], str, i);

            if (wp.jsonCode.equals("Y") && wp.autoJSON) {
              wp.addJSON(daoTid + col[k], str);
            }
          }
        }

        j++;
        wp.serNum = ("" + j);
        if (j < 10) {
          wp.serNum = "0" + j;
        }
        wp.setValue("SER_NUM", wp.serNum, i);
        i++;
      } // End of while

      daoTid = "";
      wp.autoJSON = false;
      wp.selectCnt = i - wp.sumLine;
      wp.firstRow = 0;
      wp.varRows = 0;
      rs.close();
      ps.close();
      rs = null;
      ps = null;
      // wp.actionCode="";
      dbPnt = 0;
      if (i == 0) {
        wp.notFound = "Y";
        wp.svWhere = wp.whereStr;
        wp.showLogMessage("W", "NOT FOUND : select " + wp.daoTable + " " + wp.whereStr, "");
      }

      wp.whereStr = "";
      wp.whereOrder = "";
      wp.durationTime(startTime, "select " + wp.daoTable.toUpperCase());

      // wp.showLogMessage("D","select "+wp.daoTable.toUpperCase(),"ended");
    } // End of try
    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "selectTable";
      wp.log("SQL=" + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " "
          + wp.whereOrder);
      wp.expHandle(ex);
      return false;
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

    return true;
  } // End of selectTable

  private Object[] setSqlParm(String selectSQL, String daoTable, String whereStr) {
	  int numOfQMSelect = 0, numOfQMTable = 0 , numOfQMWhere = 0 ;
	  String questionMarkWhere = whereStr;
	  
	  convertSQL = new StringBuffer()
			  .append(" SELECT ").append(selectSQL)
			  .append(" FROM ").append(daoTable)
			  .append(whereStr)
			  .toString();

	    if (parmFlag.equals("S")) {
	      Object[] keys1 = parmHash.keySet().toArray();
	      for (Object parmKey : keys1) {
	        int i = selectSQL.indexOf(":" + parmKey);
	        
	        // select
	        if (i > 0) {
	          convertSQL = convertSQL.replaceAll(":" + parmKey, "?");
	          sortHash.put(i, parmHash.get(parmKey));
	          parmCnt++;
	          numOfQMSelect++;
	        }
	        
	        // table
	        i = daoTable.indexOf(":" + parmKey);
			if (i > 0) {
				convertSQL = convertSQL.replaceAll(":" + parmKey, "?");
				sortHash.put(i, parmHash.get(parmKey));
				parmCnt++;
				numOfQMTable++;
			}
	        
	        // where
	        i = whereStr.indexOf(":" + parmKey);
			if (i > 0) {
				questionMarkWhere = questionMarkWhere.replaceAll(":" + parmKey, "?");
				convertSQL = convertSQL.replaceAll(":" + parmKey, "?");
				sortHash.put(i, parmHash.get(parmKey));
				parmCnt++;
				numOfQMWhere++;
			}
	      }
	    }else {

	    	numOfQMSelect = countQuestMarkNum(selectSQL);
			numOfQMTable = countQuestMarkNum(daoTable);
			numOfQMWhere = countQuestMarkNum(whereStr);

	    }

	    if (parmCnt == 0) {
	      return null;
	    }

	    Object param[] = new Object[parmCnt];
	    Object[] keys2 = sortHash.keySet().toArray();
	    Arrays.sort(keys2);
	    int i = 0;
	    for (Object sortData : keys2) {
	      param[i++] = sortHash.get(sortData);
	    }
	    
	    String convertWhereStr = questionMarkWhere;
	    
	    if (numOfQMWhere != 0) {
	    	int numOfWhereParm = numOfQMSelect + numOfQMTable;
	    	for (int j = 0; j < numOfQMWhere; j++) {
	    		convertWhereStr = 
	    				convertWhereStr.replaceFirst("\\?", "'" + param[numOfWhereParm + j].toString() + "'");
			}
		}
	    
	    setConvertWhereStr(convertWhereStr);

	    if (internalCall.equals("Y")) {
	      if (parmFlag.equals("S")) {
	        sortHash.clear();
	        parmCnt = 0;
	      }
	      internalCall = "";
	      return param;
	    }
	    sortHash.clear();
	    parmHash.clear();
	    parmCnt = 0;
	    parmFlag = "";
	    return param;
}

private void setConvertWhereStr(String convertWhereStr) {
	this.convertWhereStr = convertWhereStr;
}

public String getConvertWhereStr() {
	return this.convertWhereStr ;
}

public boolean selectTotal() {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      wp.dateTime();
      wp.showLogMessage("D", "selectTotal " + wp.daoTable.toUpperCase(), "started");

      String startTime = wp.sysTime + wp.millSecond;
      String sumSQL = "";

      columnCnt = 0;
      if (wp.sumField.length() > 0) {
        processColumnName(wp.sumField);
        wp.sumField = "," + wp.sumField;
      }

      if (wp.selectSQL.length() > 0) {
        sumSQL =
            "SELECT COUNT(*) " + wp.sumField + " FROM ( SELECT " + wp.selectSQL + " FROM "
                + wp.daoTable + " " + wp.whereStr + ")";
      } else {
        sumSQL = "SELECT COUNT(*) " + wp.sumField + " FROM " + wp.daoTable + " " + wp.whereStr;
      }

      wp.sumField = "";
      Object[] param = null;
      if (Arrays.asList("N", "S").contains(parmFlag)) {
        param = setSqlParm(sumSQL);
        sumSQL = convertSQL;
      }
      ps = wp.conn[dbPnt].prepareStatement(sumSQL);
      byte[] checkChar = wp.selectSQL.getBytes();

      int qMarkCnt = 0;
      for (int i = 0; i < checkChar.length; i++) {
        if (checkChar[i] == 0x3A || checkChar[i] == 0x3F) {
          qMarkCnt++;
        }
      }

      setParmData(ps, param, qMarkCnt);
      rs = ps.executeQuery();
      if (rs.next()) {
        wp.dataCnt = rs.getInt(1);
        for (int i = 0; i < columnCnt; i++) {
          String cvtData = String.format("%,14.0f", rs.getDouble(i + 2));
          wp.setValue(columnName[i], cvtData, 0);
          if (wp.dataCnt > 1) {
            wp.sumLine = 1;
            wp.setValue("SER_NUM", "-總計-", 0);
          }
        }
      }

      if (parmFlag.equals("S")) {
        Object[] keys1 = parmHash.keySet().toArray();
        for (Object parmKey : keys1) {
          cvtWhere = cvtWhere.replaceAll(":" + parmKey, "?");
          wp.queryWhere = wp.queryWhere.replaceAll(":" + parmKey, "?");
        }
      }

      if (param != null) {
        for (int i = qMarkCnt; i < parmCnt; i++) {
          cvtWhere = cvtWhere.replaceFirst("\\?", "'" + param[i] + "'");
          wp.queryWhere = wp.queryWhere.replaceFirst("\\?", "'" + param[i] + "'");
        }
      }

      rs.close();
      ps.close();
      rs = null;
      ps = null;
      wp.showLogMessage("D", "selectTotal " + wp.daoTable.toUpperCase(), "ended");
    } // End of try

    catch (Exception ex) {
      wp.log("SQL:" + wp.sqlCmd);
      wp.errSql = "Y";
      wp.expMethod = "selectTotal";
      wp.expHandle(ex);
      return false;
    }

    finally {
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
    return true;
  } // End of selectTotal

  public double sqlNumber() {
    PreparedStatement ps = null;
    ResultSet rs = null;
    double num = 0;
    try {

      Object[] param = null;
      if (Arrays.asList("N", "S").contains(parmFlag)) {
        param = setSqlParm(wp.sqlCmd);
        wp.sqlCmd = convertSQL;
      }
      ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd);
      setParmData(ps, param, 0);

      rs = ps.executeQuery();
      if (rs.next()) {
        num = rs.getDouble(1);
      }
      rs.close();
      ps.close();
      rs = null;
      ps = null;

      wp.sqlCmd = "";
    } // End of try

    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "sql_number";
      wp.expHandle(ex);
      return -1;
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
    return num;
  }

  public int selectCount() {
    PreparedStatement ps = null;
    ResultSet rs = null;
    int count = 0;
    try {
      Object[] param = null;
      if (Arrays.asList("N", "S").contains(parmFlag)) {
        param = setSqlParm(wp.sqlCmd);
        wp.sqlCmd = convertSQL;
      }
      ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd);
      setParmData(ps, param, 0);
      clearParmData();

      rs = ps.executeQuery();
      if (rs.next()) {
        count = rs.getInt(1);
      }
      rs.close();
      ps.close();
      rs = null;
      ps = null;
    } // End of try

    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "selectCount";
      wp.expHandle(ex);
      return -1;
    }

    finally {
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
    return count;
  } // End of selectCount

  public boolean checkTable() {
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      wp.dateTime();
      String startTime = wp.sysTime + wp.millSecond;

      processColumnName(wp.selectSQL);
      if (columnCnt == 0) {
        wp.errInput = "Y";
        return false;
      }

      wp.durationTime(startTime, "checkTable " + wp.daoTable.toUpperCase());
      // wp.showLogMessage("D","checkTable "+wp.daoTable.toUpperCase(),"ended");
    }

    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "checkTable";
      wp.expHandle(ex);
      return false;
    }

    finally {
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
    return true;
  } // End of checkTable

  public boolean specialSelect() throws Exception {
    wp.dateTime();
    wp.showLogMessage("D", "specialSelect", "started");

    String cvtCmd = wp.sqlCmd.trim().toUpperCase();

    if (!cvtCmd.substring(0, 6).equals("SELECT")) {
      return false;
    }

    cvtCmd = cvtCmd.substring(6);
    String[] cvtData1 = cvtCmd.split("FROM");
    byte[] checkData = cvtCmd.substring(cvtData1[0].length() + 4).getBytes();

    int i = 0, k = 0, leftcnt = 0, rightcnt = 0;
    for (i = 0; i < checkData.length; i++) {
      if ((checkData[i] >= '0' && checkData[i] <= '9')
          || (checkData[i] >= 'A' && checkData[i] <= 'Z') && leftcnt == 0) {
        break;
      }
      if (checkData[i] == '(') {
        leftcnt++;
      } else if (checkData[i] == ')') {
        rightcnt++;
      }

      k++;
      if (leftcnt > 0 && leftcnt == rightcnt) {
        break;
      }
    }

    if (leftcnt == 0) {
      String[] tmpData = cvtData1[1].split("WHERE");
      k = tmpData[0].getBytes().length;
    }

    wp.selectSQL = cvtData1[0].trim();
    wp.daoTable = new String(checkData, 0, k).trim();
    wp.whereStr = new String(checkData, k, checkData.length - k).trim();

    dbPnt = 0;
    wp.showLogMessage("D", "specialSelect", "ended");

    return true;
  } // End of specialSelect

  /* 動態選單處理-1 */
  public boolean dropdownList(String dynamicName, String dynamicTable, String optionValue,
      String optionDisplay) throws Exception {
    String checkFlag = "";
    wp.varRows = 10000;
    if (optionValue.equals(optionDisplay)) {
      wp.selectSQL = optionValue;
    } else {
      wp.selectSQL = optionValue + "," + optionDisplay;
      checkFlag = "Y";
    }
    wp.daoTable = dynamicTable;
    wp.whereStr = "ORDER BY " + optionValue;
    selectTable();
    wp.dropDownKey[0] = wp.optionKey;
    if (checkFlag.equals("Y")) {
      optionDisplay = columnName[1];
    }

    wp.multiOptionList(0, dynamicName, optionValue, optionDisplay);

    return true;
  }

  public boolean processRealConfirm(String confirmKey, String funcCode, String confirmTable,
      String userLevel, String parmLevel) throws Exception {
    specControl = true;
    confirmMode = false;

    wp.daoTable = "COMM_LOG_MASTER";
    wp.whereStr = "WHERE LOG_KEY_SEQ = '" + confirmKey + "'";
    wp.removeValue("LOG_KEY_SEQ", 0);
    wp.setValue("LOG_CONFIRM_LEVEL", userLevel, 0);
    wp.setValue("LOG_CHECK_FLAG", "", 0);
    if (parmLevel.equals(userLevel)) {
      wp.setValue("LOG_CHECK_FLAG", "Y", 0);
    }
    updateTable();

    if (!parmLevel.equals(userLevel)) {
      return true;
    }

    if (funcCode.equals("U")) {
      wp.daoTable = "COMM_LOG_DETAIL_DATA";
      wp.whereStr = "WHERE LOG_KEY_SEQ = '" + confirmKey + "'";
      selectTable();
      if (wp.selectCnt == 0) {
        return false;
      }
      for (int i = 0; i < wp.selectCnt; i++) {
        wp.setValue(wp.getValue("UPDATE_COLUMN", i), wp.getValue("UPDATE_VALUE", i), 0);
      }
    } else if (funcCode.equals("A")) {
      wp.setValue("CONFIRM_FLAG", "Y", 0);
    }

    wp.daoTable = confirmTable;
    wp.whereStr = "WHERE LOG_KEY_SEQ = '" + confirmKey + "'";
    updateTable();

    specControl = false;

    return true;
  } // End of processRealConfirm

  public void setConfirmParm(String confirmParmKey) throws Exception {
    this.confirmParmKey = confirmParmKey;
    return;
  }

  public boolean confirmUpdate() throws Exception {
    if (wp.errorInput) {
      return false;
    }

    String saveWhere = "";
    boolean saveLogMode = logMode;

    confirmMode = true;
    confirmTable = wp.daoTable;
    updateTable();

    saveWhere = wp.whereStr;
    wp.comSelect = true;
    wp.comTable = wp.daoTable.trim();
    confirmMode = false;
    logMode = false;

    wp.updateSQL = "LOG_KEY_SEQ = '" + logKeySeq + "'";
    wp.whereStr = saveWhere;
    updateTable();
    logMode = saveLogMode;

    return true;
  } // End of confirmUpdate

  public boolean updateTable() {
    PreparedStatement ps = null;

    try {
      if (wp.errorInput) {
        return false;
      }

      wp.dateTime();
      String startTime = wp.sysTime + wp.millSecond;
      String svData[] = {"", "", "", "", "", "", "", "", "", ""};
      wp.showLogMessage("D", "update " + wp.daoTable.toUpperCase(), "started");

      wp.notFound = "";

      if (!specControl && (confirmMode || logMode) && updateColCount > 0) {
        processUpdateLog();
      }

      if (confirmMode) {
        wp.updateSQL = "";
        wp.whereStr = "";
        return true;
      }

      Object[] param = null;
      if (Arrays.asList("N", "S").contains(parmFlag)) {
        param = setSqlParm(wp.sqlCmd);
        wp.sqlCmd = convertSQL;
      }
      wp.sqlCmd = "UPDATE " + wp.daoTable + " SET " + wp.updateSQL + " " + wp.whereStr;
      ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd);
      setParmData(ps, param, 0);
      clearParmData();
      retCode = ps.executeUpdate();
      ps.close();
      ps = null;

      dbPnt = 0;
      updateColCount = 0;
      wp.updateSQL = "";
      // wp.sqlCmd = "";
      wp.whereStr = "";
      if (retCode == 0) {
        wp.notFound = "Y";
        wp.dispMesg = "更新錯誤";
        wp.showLogMessage("E", wp.dispMesg + " " + wp.daoTable + " " + wp.whereStr, "");
      }

      wp.updateCnt = retCode;
      wp.durationTime(startTime, "update " + wp.daoTable.toUpperCase());
      wp.showLogMessage("D", "update " + wp.daoTable.toUpperCase() + " " + wp.updateCnt, "ended");
    }

    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "updateTable";
      wp.expHandle(ex);
      return false;
    }

    finally {
      try {
        if (ps != null) {
          ps.close();
        }
      } catch (Exception ex2) {
      }
    }
    return true;
  } // End of updateTable

  public boolean processUpdateLog() throws Exception {
    int checkCnt = updateColCount;
    updateColCount = 0;

    String saveTable = wp.daoTable;
    String saveWhere = wp.whereStr;
    String saveColumn = columnString;
    String saveSelect = wp.selectSQL;
    String[] ckData = wp.daoTable.trim().split(" ");
    if (ckData.length > 1) {
      return false;
    }

    wp.selectSQL = "";
    for (int i = 0; i < checkCnt; i++) {
      if (updateType[i].equals("BLOB") || updateType[i].equals("CLOB")) {
        continue;
      }
      wp.selectSQL = wp.selectSQL + updateColumn[i] + ",";
    }

    if (wp.selectSQL.length() > 0) {
      wp.selectSQL = wp.selectSQL.substring(0, wp.selectSQL.length() - 1);
      saveMode = true;
      selectTable();
    } else {
      return false;
    }

    logKeySeq = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    String beforeValue = "", checkCode = "";
    int cnt = 0;
    for (int i = 0; i < checkCnt; i++) {
      if (updateType[i].equals("BLOB") || updateType[i].equals("CLOB")) {
        continue;
      }
      beforeValue = wp.getValue(updateColumn[i] + "_S", 0);
      if (!updateValue[i].equals(beforeValue)) {
        cnt++;
        insertLogDetailData(i, cnt, beforeValue);
        checkCode = "Y";
      }
    }

    if (checkCode.equals("Y")) {
      insertLogMaster("U");
    }

    wp.daoTable = saveTable;
    wp.whereStr = saveWhere;
    columnString = saveColumn;
    wp.selectSQL = saveSelect;
    return true;
  } // End of processUpdateLog

  public boolean insertLogMaster(String funcCode) throws Exception {
    wp.setParameter("LOG_KEY_SEQ", logKeySeq);
    wp.setParameter("LOG_DATE", wp.sysDate);
    wp.setParameter("LOG_TIME", wp.sysTime);
    wp.setParameter("CONFIRM_PARM_KEY", confirmParmKey);
    wp.setParameter("USER_ID", wp.loginUser);
    wp.setParameter("PROGRAM_NAME", wp.javaName);
    wp.setParameter("LOG_FUNCTION", funcCode);
    wp.setParameter("TABLE_NAME", confirmTable);
    if (confirmMode) {
      wp.setParameter("LOG_CONFIRM_LEVEL", "0");
      wp.setParameter("LOG_CHECK_FLAG", "");
    } else {
      wp.setParameter("LOG_CONFIRM_LEVEL", "");
      wp.setParameter("LOG_CHECK_FLAG", "Y");
    }

    wp.daoTable = "COMM_LOG_MASTER";
    realTableInsert();
    return true;
  } // End of insertLogMaster

  public boolean insertLogDetailData(int i, int cnt, String beforeValue) throws Exception {
    wp.setParameter("LOG_KEY_SEQ", logKeySeq);
    wp.setParameter("SEQ_NO", "" + cnt);
    wp.setParameter("UPDATE_COLUMN", updateColumn[i]);
    wp.setParameter("UPDATE_VALUE", updateValue[i]);
    wp.setParameter("UPDATE_VALUE_BEFORE", beforeValue);

    wp.daoTable = "COMM_LOG_DETAIL_DATA";
    realTableInsert();
    return true;
  } // End of insertLogDetailData

  public boolean deleteTable() {
    PreparedStatement ps = null;
    try {
      wp.dateTime();
      String startTime = wp.sysTime + wp.millSecond;
      wp.showLogMessage("D", "delete " + wp.daoTable.toUpperCase(), "started");
      wp.notFound = "";

      wp.sqlCmd = "DELETE " + wp.daoTable;
      wp.sqlCmd = wp.sqlCmd + " " + wp.whereStr;
      Object[] param = null;
      if (Arrays.asList("N", "S").contains(parmFlag)) {
        param = setSqlParm(wp.sqlCmd);
        wp.sqlCmd = convertSQL;
      }
      ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd);
      setParmData(ps, param, 0);
      clearParmData();
      retCode = ps.executeUpdate();
      ps.close();
      ps = null;
      // wp.sqlCmd="";
      dbPnt = 0;

      if (retCode == 0) {
        wp.deleteNotFound = "Y";
        wp.showLogMessage("E", "delete no row " + wp.daoTable + " " + wp.whereStr, "");
      }

      wp.deleteCnt = retCode;
      wp.durationTime(startTime, "delete " + wp.daoTable.toUpperCase());
      wp.showLogMessage("D", "delete " + wp.daoTable.toUpperCase(), "ended");
    }

    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "deleteTable";
      wp.expHandle(ex);
      return false;
    }

    finally {
      try {
        if (ps != null) {
          ps.close();
        }
      } catch (Exception ex2) {
      }
    }
    return true;
  } // End of deleteTable

  public boolean executeSqlCommand() {
    sqlRowNum = 0;
    PreparedStatement ps = null;
    try {
      wp.dateTime();
      wp.showLogMessage("D", "executeSqlCommand", "started");


      if (isDspSql) {
        wp.log("SQL:" + wp.sqlCmd);
      }

      Object[] param = null;
      if (Arrays.asList("N", "S").contains(parmFlag)) {
        param = setSqlParm(wp.sqlCmd);
        wp.sqlCmd = convertSQL;
      }
      ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd);
      setParmData(ps, param, 0);
      clearParmData();
      retCode = ps.executeUpdate();
      ps.close();


      ps = null;
      dbPnt = 0;
      wp.specialSQL = "";
      // wp.sqlCmd ="";
      sqlRowNum = 0;

      wp.showLogMessage("D", "executeSqlCommand", "ended");
    } catch (Exception ex) {
      wp.log(wp.sqlCmd);
      wp.errSql = "Y";
      wp.expMethod = "executeSqlCommand";
      wp.expHandle(ex);
      return false;
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
      } catch (Exception ex2) {
      }
    }
    return true;
  } // End of executeSqlCommand

  public boolean processColumnName(String columnData) throws Exception {
    int i = 0, cnt = 0, pnt = 0;
    String fieldString = "", convertSQL = "";
    String[] splitString;
    byte[] cvtData;

    if (columnData.length() == 0) {
      processFullColumn();
      return true;
    }

    convertSQL = columnData;
    String dbType = TarokoParm.getInstance().getDbType()[dbPnt];
    if (!dbType.equals("SYBASE_ASE") && !dbType.equals("SYBASE_IQ")) {
      convertSQL = columnData.toUpperCase();
    }

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
    cnt = 0;
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
      columnName[cnt] = fieldString.trim();
      cnt++;
    }

    columnCnt = cnt;
    for (i = 0; i < columnCnt; i++) {
      pnt = columnName[i].indexOf(".");
      columnName[i] = columnName[i].substring(pnt + 1);
      dataType[i] = "";
      // wp.showLogMessage("D",i+" "+columnName[i],"");
    }

    return true;
  } // End of processColumnName

  public boolean processFullColumn() {
    Statement st = null;
    ResultSet rs = null;
    try {
      st = wp.conn[dbPnt].createStatement();
      rs = st.executeQuery("SELECT * FROM " + wp.daoTable.toUpperCase());
      ResultSetMetaData md = rs.getMetaData();
      columnCnt = md.getColumnCount();
      for (int i = 0; i < md.getColumnCount(); i++) {
        columnName[i] = md.getColumnName(i + 1);
        dataType[i] = md.getColumnTypeName(i + 1);
        dataLength[i] = md.getPrecision(i + 1);
      }

      if (columnCnt == 0) {
        wp.showLogMessage("E", "TABLE NAME ERROR : " + wp.daoTable.toUpperCase(), "");
        return false;
      }

      rs.close();
      st.close();
      st = null;
      rs = null;
      md = null;
    }

    catch (Exception ex) {
      wp.expMethod = "processFullColumn";
      wp.expHandle(ex);
      return false;
    }

    finally {
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
    return true;
  } // End of processFullColumn

  private boolean processSelectColumn() throws Exception {
    // String selectColumn="";
    if (wp.selectSQL.length() > 0) {
      columnString = wp.selectSQL;
      return true;
    }
    StringBuffer strbuf = new StringBuffer();
    columnString = "";
    for (int i = 0; i < columnCnt; i++) {
      if (dataType[i].equals("DATE") && TarokoParm.getInstance().getDbType()[dbPnt].equals("ORACLE")) {
        strbuf.append("NVL(TO_CHAR(" + columnName[i] + ",'YYYYMMDDHH24MISS'),' '),");
      } else {
        strbuf.append(columnName[i] + ",");
      }
    }

    columnString = strbuf.toString();
    columnString = columnString.substring(0, columnString.length() - 1);

    return true;
  } // End of processSelectColumn

  public String[] getTableList(String schema) throws Exception {
    String[] tableList = new String[1500];
    String catalog = null, tableNamePattern = null;
    String[] types = null;
    String tableName = "", tableTypes = "", tableRemarks = "";

    DatabaseMetaData meta = wp.conn[dbPnt].getMetaData();
    ResultSet rs = meta.getTables(catalog, schema, tableNamePattern, types);
    int i = 0;
    while (rs.next()) {
      tableName = rs.getString("TABLE_NAME");
      tableTypes = rs.getString("TABLE_TYPE");
      tableRemarks = rs.getString("REMARKS");
      if (tableRemarks == null) {
        tableRemarks = "";
      }
      tableList[i] = (tableName + "@" + tableRemarks + "@#");
      i++;
    }
    rs.close();
    rs = null;
    return tableList;
  }

  public void setInt(int i, int parmNum) throws Exception {
    sortHash.put(i, parmNum);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void setInt(String parmField, int parmNum) throws Exception {
    parmHash.put(parmField.toUpperCase(), parmNum);
    parmFlag = "S";
    return;
  }

  public void setLong(int i, long parmNum) throws Exception {
    sortHash.put(i, parmNum);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void setLong(String parmField, long parmNum) throws Exception {
    parmHash.put(parmField.toUpperCase(), parmNum);
    parmFlag = "S";
    return;
  }

  public void setDouble(int i, double parmNum) throws Exception {
    sortHash.put(i, parmNum);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void setDouble(String parmField, double parmNum) throws Exception {
    parmHash.put(parmField.toUpperCase(), parmNum);
    parmFlag = "S";
    return;
  }

  public void setString(int i, String parmString) throws Exception {
    sortHash.put(i, parmString);
    parmFlag = "N";
    parmCnt = i;
    return;
  }

  public void setString(String parmField, String parmString) throws Exception {
    parmHash.put(parmField.toUpperCase(), parmString);
    parmFlag = "S";
    return;
  }

  public Object[] setSqlParm(String cvtSql) throws Exception {

    convertSQL = cvtSql.toUpperCase();

    if (parmFlag.equals("S")) {
      Object[] keys1 = parmHash.keySet().toArray();
      for (Object parmKey : keys1) {
        int i = convertSQL.indexOf(":" + parmKey);
        if (i > 0) {
          convertSQL = convertSQL.replaceAll(":" + parmKey, "?");
          sortHash.put(i, parmHash.get(parmKey));
          parmCnt++;
        }
      }
    }

    if (parmCnt == 0) {
      return null;
    }

    Object param[] = new Object[parmCnt];
    Object[] keys2 = sortHash.keySet().toArray();
    Arrays.sort(keys2);
    int i = 0;
    for (Object sortData : keys2) {
      param[i++] = sortHash.get(sortData);
    }

    if (internalCall.equals("Y")) {
      if (parmFlag.equals("S")) {
        sortHash.clear();
        parmCnt = 0;
      }
      internalCall = "";
      return param;
    }
    sortHash.clear();
    parmHash.clear();
    parmCnt = 0;
    parmFlag = "";
    return param;
  }

  public boolean setParmData(PreparedStatement ps, Object[] param, int startPnt) throws Exception {
    if (param == null) {
      return false;
    }

    wp.saveKey = (String) param[0];
    if (param != null) {
      for (int ii = startPnt; ii < param.length; ii++) {
        ps.setObject(ii + 1, param[ii]);
      }
    }

    return true;
  }

  public boolean clearParmData() throws Exception {

    startParm = 0;
    wp.sqlCmd = "";
    return true;
  }

  public void setSaveMode() {
    saveMode = true;
    return;
  }

  // -JH: add-==========================================
  public int sqlSelect() {
    int liRc = 0, ii = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;

    sqlCount = 0;
    try {
      colHash.clear();

      processColumnName(wp.selectSQL);
      processSelectColumn();

      wp.sqlCmd = "SELECT " + columnString + " FROM " + wp.daoTable + " " + wp.whereStr;

      // wp.showLogMessage("U","","-JJJ->sql_select:"+wp.sqlCmd);
      Object[] param = null;
      if (Arrays.asList("N", "S").contains(parmFlag)) {
        param = setSqlParm(wp.sqlCmd);
        wp.sqlCmd = convertSQL;
      }
      ps =
          wp.conn[dbPnt].prepareStatement(wp.sqlCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,
              ResultSet.CONCUR_READ_ONLY);
      setParmData(ps, param, 0);
      clearParmData();
      rs = ps.executeQuery();

      while (rs.next()) {
        for (int k = 0; k < columnCnt; k++) {
          sqlSetcol(columnName[k], rs.getString(k + 1), ii);
        }
        ii++;
      } // End of while


      rs.close();
      ps.close();
      rs = null;
      ps = null;
      // wp.selectSQL = "";
      // dbPnt = 0;
      sqlCount = ii;
      if (ii == 0) {
        liRc = 100;
      }
    } // End of try
    catch (Exception ex) {
      wp.errSql = "Y";
      wp.expMethod = "sql_select";
      wp.expHandle(ex);
      liRc = -1;
      wp.showLogMessage("U", "", "-JJJ->sql_error: select from " + wp.daoTable + " " + wp.whereStr);
    }

    finally {
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
    return liRc;
  } // End of sql_select

  public void sqlSetcol(String fieldName, String setData, int cnt) {
    try {
      if (setData == null) {
        setData = "";
      }

      setData = setData.trim();
      fieldName = fieldName.toUpperCase() + "-" + cnt;
      colHash.put(fieldName, setData);
    } catch (Exception ex) {
      wp.expMethod = "setcol";
      wp.expHandle(ex);
    }

    return;
  }

  public double sqlColnum(int int1, String fieldName) {
    String str = "";
    try {
      fieldName = fieldName.toUpperCase() + "-" + int1;
      str = (String) colHash.get(fieldName);
      if (str == null) {
        return 0;
      }
      if (str.trim().length() == 0) {
        return 0;
      }

      return Double.parseDouble(str.trim());
    }

    catch (Exception ex) {
      wp.expMethod = "sql_getnum";
      wp.expHandle(ex);
    }

    return 0;
  }

  public String sqlColget(String fieldName, int cnt) {
    String retnStr = "";
    try {
      fieldName = fieldName.toUpperCase() + "-" + cnt;
      retnStr = (String) colHash.get(fieldName);
      if (retnStr == null) {
        return "";
      }

      retnStr = retnStr.trim();
    }

    catch (Exception ex) {
      wp.expMethod = "getcol";
      wp.expHandle(ex);
    }

    return retnStr;
  }

  public String sqlColget(int cnt, String fieldName) {
    String retnStr = "";
    try {
      fieldName = fieldName.toUpperCase() + "-" + cnt;
      retnStr = (String) colHash.get(fieldName);
      if (retnStr == null) {
        return "";
      }

      retnStr = retnStr.trim();
    }

    catch (Exception ex) {
      wp.expMethod = "getcol";
      wp.expHandle(ex);
    }

    return retnStr;
  }
 
	private int countQuestMarkNum(String selectSQL) {
		int cnt = 0;
		for (int i = 0; i < selectSQL.length(); i++) {
			if (selectSQL.charAt(i) == '?') {
				cnt++;
			}
		}
		return cnt;
	}


} // End of class TarokoDAO
