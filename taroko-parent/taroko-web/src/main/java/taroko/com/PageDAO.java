/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-13  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-22  V1.00.01  Zuwei       coding standard      *
*  109-09-30  V1.00.02  JustinWu   modify getSiteName                                                                          *  
*  109-10-15   V1.00.03  Zuwei       刪除調用wp.expHandle時傳入的sql内容      *
*  109-10-16   V1.00.04  JustinWu   remove getSiteName()
*  109-12-24  V1.00.05  JustinWu    add tempIsNoClear statement
*  110-01-08  V1.00.02   shiyuqi       修改无意义命名
*  110-01-13  V1.00.06  JustinWu    modified for parameterized sql
*  110-01-19  V1.00.07  JustinWu    change variable names
*  110-01-27  V1.00.08  JustinWu    write private logs
*  110-12-24  V1.00.09  JustinWu    use wp.showPDPALog(">>>idno>>>" + str)
*  111-01-07  V1.00.10  JustinWu    optimize the SQL script by replacing + with StringBuilder *
*  111-02-08  V1.00.11  JustinWu    output error messages of level D         *
******************************************************************************/
package taroko.com;
/** UI-sql公用程式
 * 2019-1230   JH    pageQuery2()
 * 2019-1225   JH    orderby, selectCount()
 * 2019-1220   JH    modify
 * 2019-1203   JH    pageQuery([]).public
 * 2019-1120   JH    pageQuery([]): use JH.page.count()
 * 2019-1015   JH    idno_QueryLog
 * 2019-0912   JH    pageQuery([]): JACK.原始語法(恢復使用)
 * 2019-0812   JH    個資查詢LOG
 * 2019-0726   JH    sqlSelect(wp,..)
 * 2019-0321:  JH    pageQuery2()
 * 2018-1025.jh:		pageQuery().limit
 */

import taroko.base.BaseSQL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings({ "unchecked", "deprecation" })
public class PageDAO extends BaseSQL {

	private static final String CARD_NO_KEY = "card_no";
	private static final String ACCT_KEY = "acct_key";
	private static final String ID_NO_KEY = "id_no";
	public taroko.com.TarokoCommon wp = null;
	public boolean logMode = false;
	boolean selectLimit = true;
	boolean isQueryOverLimit = false;

	public String[] colName = null;
	public String[] colType = null;
	public int columnCnt = 0;

	int dbPnt = 0;
	String internalCall = "";

	// -Idno_QueryLog-
	public String qqDebit = "debit_flag";
	public String qqIdno = "";
	public String qqCard = "";

	// 2020-10-16 Justin remove getSiteName()
//	public String getSiteName() {
//		String appName = wp.request.getContextPath();
//		if (appName == null) {
//			appName = "";
//		}
//		if (appName.length()>0 && appName.indexOf("/") == 0) {
//			appName = appName.substring(1);
//		}
//		
//		String lssit = "";
//		if (commString.eqAny(wp.request.getServerName(), "10.5.109.1")) {
//			lssit = "UAT";
//		} else if (commString.eqAny(wp.request.getServerName(), "10.1.109.1")) {
//			lssit = "SIT-" + appName;
//		} else if (commString.strIn2("192.162.", wp.request.getServerName())) {
//			lssit = "SIT-" + appName;
//			// |192.168.30.20|127.0.0.1"))
//		} else if (commString.strIn2("127.0.0", wp.request.getServerName())) {
//			lssit = "LOC-" + appName;
//		}
//
//		return lssit;
//	}

	public void setEcsDB() {
		dbPnt = 0;
	}

	public void selectNoLimit() {
		wp.varRows = 99999;
		selectLimit = false;
	}

	public boolean rowIsShow(int ll) throws Exception {
		// -check資料是否顯示-
		return true;
	}

	private void xxPageQueryJack(Object param[]) {
		// -JACK.原始語法(恢復使用)---
		PreparedStatement ps = null;
		ResultSet rs = null;
		sqlErrtext = "";
		sqlCode = 100;
		sqlRowNum = 0;
		wp.notFound = "Y";
		isQueryOverLimit = false;

		try {
			wp.dateTime();
			String startTime = wp.sysTime + wp.millSecond;
			wp.showLogMessage("D", "select " + wp.daoTable.toUpperCase(), "started");

			cvtWhere = wp.whereStr;

			if (wp.svFlag.equals("Y") && wp.queryWhere.length() == 0) {
				wp.queryWhere = wp.whereStr;
				wp.svFlag = "";
			}

			// 2021/01/14 Justin
			if (wp.sqlCmd.length() == 0) {
				if (param == null) {		
					sqlParm.nameSqlParm(wp.selectSQL, wp.daoTable, wp.whereStr, wp.whereOrder, param);
					wp.sqlCmd = sqlParm.getConvSQL();
					param = sqlParm.getConvParm();
					cvtWhere = sqlParm.getConvertWhere();
				}else {
					wp.sqlCmd = sqlParm.getSQLString(wp.selectSQL, wp.daoTable, wp.whereStr, wp.whereOrder);
//					param = param;
					cvtWhere = sqlParm.getConvertWhere(wp.selectSQL, wp.daoTable, wp.whereStr, param);			
				}
			}else {
				if (wp.whereOrder.length() > 0) { 
					wp.sqlCmd += " " + wp.whereOrder;
				}
				
				if (param == null ) {
					sqlParm.nameSqlParm(wp.sqlCmd, wp.whereStr, param);
					wp.sqlCmd = sqlParm.getConvSQL();
					param = sqlParm.getConvParm();		
					cvtWhere = sqlParm.getConvertWhere();
				}else {
//					wp.sqlCmd =wp.sqlCmd;
//					param = param;
					cvtWhere = sqlParm.getConvertWhere(wp.sqlCmd, wp.whereStr, param);
				}
			}
			// 2021/01/14 Justin

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



			// wp.buttonCode=M
			boolean lbSqlAddlimit = false;
			if (selectLimit && wp.pageRows > 0 && wp.pageRows < 999 && wp.totalRows > 0) {
				String lowercaseSql = wp.sqlCmd.toLowerCase();
				int liWhere = lowercaseSql.lastIndexOf(" where ");
				int liFetch = lowercaseSql.indexOf(" fetch ");
				int liFirst = lowercaseSql.indexOf(" first ");
				int liLimit = lowercaseSql.indexOf(" limit ");
				int liOffset = lowercaseSql.indexOf(" offset ");
				if (liFetch <= 0 && liFirst <= 0 && liLimit <= 0 && liOffset <= 0) {
					wp.sqlCmd += " limit " + wp.firstRow + " , " + wp.pageRows;
					lbSqlAddlimit = true;
				} else {
					if ((liFetch < liWhere && liFirst < liWhere) && (liLimit < liWhere && liOffset < liWhere)) {
						wp.sqlCmd += " limit " + wp.firstRow + " , " + wp.pageRows;
						lbSqlAddlimit = true;
					}
				}
			}
			// -SQL-Log-------------
			dddSqlLog(wp.sqlCmd, param);

			ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			if (param != null) {
				for (int ii = 0; ii < param.length; ii++) {
					ps.setObject(ii + 1, param[ii]);
				}
			}
			if (wp.pageRows > 0 && wp.pageRows < 999) {
				ps.setFetchSize(wp.pageRows);
			}
			ps.setFetchDirection(ResultSet.FETCH_REVERSE);

			rs = ps.executeQuery();
			readColNames(rs.getMetaData());
			colName = getColNames();
			colType = getColTypes();
			columnCnt = getColumnCnt();

			if (wp.totalRows == 0 && wp.pageRows < 999) {
				rs.last();
				wp.dataCnt = rs.getRow();
				wp.totalRows = wp.dataCnt;
			}

			if (wp.pageRows >= 0 && wp.pageRows < 999) {
				if (wp.pageRows == 0)
					wp.pageRows = 20;
				if (wp.firstRow <= 0)
					rs.beforeFirst();
				else if (!lbSqlAddlimit) {
					rs.absolute(wp.firstRow);
				}
			}

			int liKk = wp.sumLine;
			int row = wp.firstRow;

			String str = "";
			String col = "";
			while (rs.next()) {
				sqlRowNum++;

				for (int k = 1; k < colName.length; k++) {
					col = daoTid + colName[k];
					if (colType[k].equals("BLOB")) {
						wp.blobValue = rs.getBlob(k);
					} else {
						str = "";
						if (colType[k].equals("DECIMAL") || colType[k].equals("DOUBLE")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getDouble(k);
							} else
								str = "0";
							wp.setNumber(col, rs.getDouble(k), liKk);
						} else if (colType[k].equals("INTEGER") || colType[k].equals("LONG")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getLong(k);
							} else
								str = "0";
							wp.setNumber(col, rs.getLong(k), liKk);
						} else {
							if (rs.getObject(k) != null) {
								str = rs.getObject(k).toString(); // .trim();
							} else
								str = "";
							wp.setValue(col, str, liKk);
						}
						if (wp.jsonCode.equals("Y") && wp.autoJSON) {
							wp.addJSON(col, str);
						}
					}
				}

				row++;
				wp.serNum = "" + row;
				if (row < 10)
					wp.serNum = "0" + row;
				else
					wp.serNum = "" + row;

				wp.setValue("SER_NUM", wp.serNum, liKk);
				wp.setValue(daoTid + "row_num", "" + liKk, liKk);

				liKk++;
				if (selectLimit && liKk > wp.pageRows) {
					wp.logErr("?????資料筆數超過 999????");
					isQueryOverLimit = true;
					break;
				}
				if (selectLimit && sqlRowNum >= wp.pageRows) {
					break;
				}
			} // End of while
			if (sqlRowNum == 0) {
				wp.svWhere = wp.whereStr;
				wp.notFound = "Y";
				wp.showLogMessage("W", "NOT FOUND : select " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder,
						"");
			} else {
				sqlCode = 0;
				wp.notFound = "N";
			}

			wp.autoJSON = false;
			wp.selectCnt = sqlRowNum; // li_kk - wp.sumLine;

			wp.varRows = 0;
			selectLimit = true;
			rs.close();
			ps.close();
			rs = null;
			ps = null;
			wp.actionCode = "";
			dbPnt = 0;

			wp.durationTime(startTime, "select " + wp.daoTable.toUpperCase());

			daoTid = "";
			wp.sqlCmd = "";
			wp.daoTable = "";
			wp.whereStr = "";
			wp.selectSQL = "";
			wp.whereOrder = "";
		} // End of try
		catch (SQLException ex2) {
			int liExp = getSqlErrmsg(ex2);
			wp.errSql = "Y";
			wp.expMethod = "pageQuery";
			wp.logErr("SQL-err=" + ex2.getMessage());
			wp.logErr("SQL-err=" + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder);
			if (liExp == 1) {
				wp.expHandle(ex2);
			}
		} catch (Exception ex) {
			setSqlErrmsg(ex.getMessage());
			wp.errSql = "Y";
			wp.expMethod = "pageQuery";
			wp.logErr("SQL-err=" + ex.getMessage());
			wp.logErr("SQL-err=" + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder);
			wp.expHandle(ex);
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

	} // End of selectTable

	public void pageQuery() {
		pageQuery(null);
	}

	public void pageQuery(Object param[]) {
		// -JH.語法:---
		PreparedStatement ps = null;
		ResultSet rs = null;
		sqlErrtext = "";
		sqlCode = 100;
		sqlRowNum = 0;
		wp.notFound = "Y";
		isQueryOverLimit = false;

		try {
			wp.dateTime();
			String startTime = wp.sysTime + wp.millSecond;
			wp.showLogMessage("D", "select " + wp.daoTable.toUpperCase(), "started");

			cvtWhere = wp.whereStr;
			if (wp.pageRows < 999 && wp.totalRows == 0) {
				if (wp.actionCode.equals("B") || wp.actionCode.equals("Q")) {
					internalCall = "Y";
					pageQueryRowcount(param);
				}
			} else if (wp.totalRows > 0) {
				wp.dataCnt = wp.totalRows;
			}
			
			if (wp.dataCnt > wp.pageRows) {
				this.isQueryOverLimit = true;
			}

			if (wp.svFlag.equals("Y") && wp.queryWhere.length() == 0) {
				wp.queryWhere = wp.whereStr;
				wp.svFlag = "";
			}
			
//			2021/01/12 Justin
//			if (wp.sqlCmd.length() == 0) {
//				wp.sqlCmd = "SELECT " + wp.selectSQL + " FROM " + wp.daoTable + " " + wp.whereStr;
//				if (wp.whereOrder.length() > 0) { // && wp.sqlCmd.toLowerCase().indexOf(" order by ") < 0) {
//					wp.sqlCmd += " " + wp.whereOrder;
//				}
//			}
			
//			2021/01/12 Justin
			if (wp.sqlCmd.length() == 0) {
				if (param == null) {		
					sqlParm.nameSqlParm(wp.selectSQL, wp.daoTable, wp.whereStr, wp.whereOrder, param);
					wp.sqlCmd = sqlParm.getConvSQL();
					param = sqlParm.getConvParm();
					cvtWhere = sqlParm.getConvertWhere();
				}else {
					wp.sqlCmd = sqlParm.getSQLString(wp.selectSQL, wp.daoTable, wp.whereStr, wp.whereOrder);
//					param = param;
					cvtWhere = sqlParm.getConvertWhere(wp.selectSQL, wp.daoTable, wp.whereStr, param);			
				}
			}else {
				if (wp.whereOrder.length() > 0) { 
					wp.sqlCmd += " " + wp.whereOrder;
				}
				
				if (param == null ) {
					sqlParm.nameSqlParm(wp.sqlCmd, wp.whereStr, param);
					wp.sqlCmd = sqlParm.getConvSQL();
					param = sqlParm.getConvParm();
					cvtWhere = sqlParm.getConvertWhere();
				}else {
//					wp.sqlCmd =wp.sqlCmd;
//					param = param;
					cvtWhere = sqlParm.getConvertWhere(wp.sqlCmd, wp.whereStr, param);
				}
			}
				
			
//	         2021/01/12  Justin
			
			// -2018-0322;2018-0916.cancel-
			// int first_row =0;
			// if (wp.totalRows >999 && wp.sqlCmd.toLowerCase().indexOf(" fetch first ")<=0)
			// {
			// if (wp.firstRow<=0)
			// first_row =100 + wp.pageRows;
			// else first_row =wp.firstRow + (wp.pageRows * 2);
			//
			// wp.sqlCmd +=commSqlStr .rownum(first_row);
			// }

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
			if (ibDspSql) {
				wp.log("SQL:" + wp.sqlCmd);
			}

			// if ( Arrays.asList("N","S").contains(parmFlag) ) {
			// param = nameSqlParm(wp.sqlCmd);
			// wp.sqlCmd=convertSQL;
			// }
			
//			2021/01/12 Justin
//			if (param == null && sqlParm.nameSqlParm(wp.sqlCmd)) {
//				wp.sqlCmd = sqlParm.getConvSQL();
//				param = sqlParm.getConvParm();
//			}

			// -JH:2018-0916---
			if (selectLimit && wp.pageRows > 0 && wp.pageRows < 999) {
				if (!sqlHasLimit(wp.sqlCmd)) {
					wp.sqlCmd += " limit " + wp.firstRow + " , " + wp.pageRows;
		        }
								
			}
			// -SQL-Log-------------
			dddSqlLog(wp.sqlCmd, param);

			ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			if (param != null) {
				for (int ii = 0; ii < param.length; ii++) {
					ps.setObject(ii + 1, param[ii]);
				}
			}

			rs = ps.executeQuery();
			readColNames(rs.getMetaData());
			colName = getColNames();
			colType = getColTypes();
			columnCnt = getColumnCnt();

			// -2018-0916-----
			// if (wp.pageRows >= 0 && wp.pageRows < 999) {
			// if (wp.firstRow <= 0) {
			// rs.beforeFirst();
			// } else {
			// rs.absolute(wp.firstRow);
			// }
			// if (wp.pageRows == 0) {
			// wp.pageRows = 20;
			// }
			// //wp.ddd("pageRows="+wp.pageRows);
			// }
			// else {
			// wp.currPage =1;
			// }

			// rs.setFetchSize(wp.pageRows);
			int likk = wp.sumLine;
			int row = wp.firstRow;

			// int limitCnt = 0;
			// if (wp.varRows > 0) {
			// _selectLimit =(wp.varRows<99999);
			// limitCnt = wp.varRows;
			// } else {
			// limitCnt = wp.pageRows + wp.varRows + wp.sumLine;
			// }

			String str = "";
			String col = "";
			while (rs.next()) {
				// 2018-0916
				// if (li_kk >= limitCnt) {
				// break;
				// }
				sqlRowNum++;

				for (int k = 1; k < colName.length; k++) {
					col = daoTid + colName[k];
					// if (mdata.getColumnTypeName(k).equalsIgnoreCase("BLOB")) {
					if (colType[k].equals("BLOB")) {
						wp.blobValue = rs.getBlob(k);
					} else {
						str = "";
						if (colType[k].equals("DECIMAL") || colType[k].equals("DOUBLE")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getDouble(k);
							} else
								str = "0";
							wp.setNumber(col, rs.getDouble(k), likk);
						} else if (colType[k].equals("INTEGER") || colType[k].equals("LONG")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getLong(k);
							} else
								str = "0";
							wp.setNumber(col, rs.getLong(k), likk);
						} else {
							if (rs.getObject(k) != null) {
								str = rs.getObject(k).toString(); // .trim();
							} else
								str = "";
							wp.setValue(col, str, likk);
						}
						if (wp.jsonCode.equals("Y") && wp.autoJSON) {
							wp.addJSON(col, str);
						}
					}
				}

				row++;
				// *-20170809,0811-
				wp.serNum = "" + row;
				if (row < 10)
					wp.serNum = "0" + row;
				else
					wp.serNum = "" + row;
				wp.setValue("SER_NUM", wp.serNum, likk);
				// */
				wp.setValue(daoTid + "row_num", "" + likk, likk);

				likk++;
				if (selectLimit && likk > wp.pageRows) {
					wp.logErr("?????資料筆數超過 999????");
					isQueryOverLimit = true;
					break;
				}
			} // End of while
			if (sqlRowNum == 0) {
				wp.svWhere = wp.whereStr;
				wp.notFound = "Y";
				wp.showLogMessage("W", "NOT FOUND : select " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder,
						"");
			} else {
				sqlCode = 0;
				wp.notFound = "N";
				if ("Ecsq0050".equals(wp.javaName) == false) {
					zzIdnoQuerylog();
				}
			}

			wp.autoJSON = false;
			wp.selectCnt = sqlRowNum; // li_kk - wp.sumLine;
			// wp.selectCnt = sql_nrow;
			// wp.firstRow = 0; //-20170809-
			wp.varRows = 0;
			selectLimit = true;
			rs.close();
			ps.close();
			rs = null;
			ps = null;
			wp.actionCode = "";
			dbPnt = 0;

			wp.durationTime(startTime, "select " + wp.daoTable.toUpperCase());

			daoTid = "";
			wp.sqlCmd = "";
			wp.daoTable = "";
			wp.whereStr = "";
			wp.selectSQL = "";
			wp.whereOrder = "";
		} // End of try
		catch (SQLException ex2) {
			int liExp = getSqlErrmsg(ex2);
			wp.errSql = "Y";
			wp.expMethod = "pageQuery";
			wp.logErr("SQL-err=" + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder);
			if (liExp == 1) {
				wp.expHandle(ex2);
			}
		} catch (Exception ex) {
			setSqlErrmsg(ex.getMessage());
			wp.errSql = "Y";
			wp.expMethod = "pageQuery";
			wp.logErr("SQL-err=" + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder);
			wp.expHandle(ex);
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

	} // End of selectTable

	public void pageQuery2(Object param[]) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		sqlErrtext = "";
		sqlCode = 100;
		sqlRowNum = 0;
		wp.notFound = "Y";

		try {
			wp.dateTime();
			String startTime = wp.sysTime + wp.millSecond;
			wp.showLogMessage("D", "select " + wp.daoTable.toUpperCase(), "started");

			cvtWhere = wp.whereStr;
			if (wp.pageRows < 999 && wp.totalRows == 0) {
				if (wp.actionCode.equals("B") || wp.actionCode.equals("Q")) {
					internalCall = "Y";
					pageQueryRowcount(param);
				}
			} else if (wp.totalRows > 0) {
				wp.dataCnt = wp.totalRows;
			}

			if (wp.svFlag.equals("Y") && wp.queryWhere.length() == 0) {
				wp.queryWhere = wp.whereStr;
				wp.svFlag = "";
			}
			
//         2021/01/12  Justin
//			if (wp.sqlCmd.length() == 0) {
//				wp.sqlCmd = "SELECT " + wp.selectSQL + " FROM " + wp.daoTable + " " + wp.whereStr;
//				if (wp.whereOrder.length() > 0) { // && wp.sqlCmd.toLowerCase().indexOf(" order by ") < 0) {
//					wp.sqlCmd += " " + wp.whereOrder;
//				}
//			}
			
//	         2021/01/12  Justin
			if (wp.sqlCmd.length() == 0) {
				if (param == null) {		
					sqlParm.nameSqlParm(wp.selectSQL, wp.daoTable, wp.whereStr, wp.whereOrder, param);
					wp.sqlCmd = sqlParm.getConvSQL();
					param = sqlParm.getConvParm();
					cvtWhere = sqlParm.getConvertWhere();
				}else {
					wp.sqlCmd = sqlParm.getSQLString(wp.selectSQL, wp.daoTable, wp.whereStr, wp.whereOrder);
//					param = param;
					cvtWhere = sqlParm.getConvertWhere(wp.selectSQL, wp.daoTable, wp.whereStr, param);			
				}
			}else {
				if (wp.whereOrder.length() > 0) { 
					wp.sqlCmd += " " + wp.whereOrder;
				}
				
				if (param == null ) {
					sqlParm.nameSqlParm(wp.sqlCmd, wp.whereStr, param);
					wp.sqlCmd = sqlParm.getConvSQL();
					param = sqlParm.getConvParm();		
					cvtWhere = sqlParm.getConvertWhere();
				}else {
//					wp.sqlCmd =wp.sqlCmd;
//					param = param;
					cvtWhere = sqlParm.getConvertWhere(wp.sqlCmd, wp.whereStr, param);
				}
			}
//	         2021/01/12  Justin

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
			if (ibDspSql) {
				wp.log("SQL:" + wp.sqlCmd);
			}

//	         2021/01/12  Justin
//			if (param == null && sqlParm.nameSqlParm(wp.sqlCmd)) {
//				wp.sqlCmd = sqlParm.getConvSQL();
//				param = sqlParm.getConvParm();
//			}

			// -JH:2018-0916---
			if (selectLimit && wp.pageRows > 0 && wp.pageRows < 999) {
				wp.sqlCmd += " limit 99999 offset " + wp.currRows;
				// int li_where = wp.sqlCmd.toLowerCase().lastIndexOf(" where ");
				// int li_fetch = wp.sqlCmd.toLowerCase().indexOf(" fetch ");
				// int li_first = wp.sqlCmd.toLowerCase().indexOf(" first ");
				// int li_limit = wp.sqlCmd.toLowerCase().indexOf(" limit ");
				// int li_offset = wp.sqlCmd.toLowerCase().indexOf(" offset ");
				// if (li_fetch <= 0 && li_first <= 0 && li_limit <= 0 && li_offset <= 0) {
				// wp.sqlCmd += " limit 99999 offset " + wp.currRows;
				// } else {
				// if ((li_fetch < li_where && li_first < li_where) && (li_limit < li_where &&
				// li_offset <
				// li_where)) {
				// wp.sqlCmd += " limit 99999 offset " + wp.currRows;
				// }
				// }
			}
			// -SQL-Log-------------
			dddSqlLog(wp.sqlCmd, param);

			ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			if (param != null) {
				for (int ii = 0; ii < param.length; ii++) {
					ps.setObject(ii + 1, param[ii]);
				}
			}

			rs = ps.executeQuery();
			readColNames(rs.getMetaData());
			String[] aaColName = getColNames();
			String[] aaColType = getColTypes();

			int liKk = wp.sumLine;
			int row = wp.firstRow;
			// wp.currRows =wp.firstRow;

			String str = "";
			String col = "";
			String lsDaoTid = daoTid;
			int llSqlNrow = 0, llCurrRows = 0;
			int llOffsetRow = wp.currRows;
			while (rs.next()) {
				llCurrRows++;
				for (int k = 1; k < aaColName.length; k++) {
					String lsName = aaColName[k];
					String lsType = aaColType[k];
					col = lsDaoTid + lsName;
					// if (mdata.getColumnTypeName(k).equalsIgnoreCase("BLOB")) {
					if (lsType.equals("BLOB")) {
						wp.blobValue = rs.getBlob(k);
					} else {
						str = "";
						if (lsType.equals("DECIMAL") || lsType.equals("DOUBLE")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getDouble(k);
							} else
								str = "0";
							wp.setNumber(col, rs.getDouble(k), liKk);
						} else if (lsType.equals("INTEGER") || lsType.equals("LONG")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getLong(k);
							} else
								str = "0";
							wp.setNumber(col, rs.getLong(k), liKk);
						} else {
							if (rs.getObject(k) != null) {
								str = rs.getObject(k).toString(); // .trim();
							} else
								str = "";
							wp.setValue(col, str, liKk);
						}
						if (wp.jsonCode.equals("Y") && wp.autoJSON) {
							wp.addJSON(col, str);
						}
					}
				}

				// -remove不顯示資料-
				if (!wp.jsonCode.equals("Y") || !wp.autoJSON) {
					if (rowIsShow(liKk) == false) {
						for (int kk = 1; kk < aaColName.length; kk++) {
							wp.colClear(liKk, lsDaoTid + aaColName[kk]);
						}
						// wp.dataCnt =selectCount(wp.pageCount_sql,param);
						// wp.totalRows --;
						continue;
					}
				}

				llSqlNrow++;

				row++;
				// *-20170809,0811-
				wp.serNum = "" + row;
				if (row < 10)
					wp.serNum = "0" + row;
				else
					wp.serNum = "" + row;
				wp.setValue("SER_NUM", wp.serNum, liKk);
				wp.setValue(lsDaoTid + "row_num", "" + liKk, liKk);

				liKk++;
				if (llSqlNrow >= wp.pageRows) {
					break;
				}
			} // End of while
			wp.currRows = llCurrRows + llOffsetRow;
			sqlRowNum = llSqlNrow;

			if (sqlRowNum == 0) {
				wp.svWhere = wp.whereStr;
				wp.notFound = "Y";
				wp.showLogMessage("W", "NOT FOUND : select " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder,
						"");
			} else {
				sqlCode = 0;
				wp.notFound = "N";
			}

			wp.autoJSON = false;
			wp.selectCnt = sqlRowNum; // li_kk - wp.sumLine;
			// wp.selectCnt = sql_nrow;
			// wp.firstRow = 0; //-20170809-
			wp.varRows = 0;
			selectLimit = true;
			rs.close();
			ps.close();
			rs = null;
			ps = null;
			wp.actionCode = "";
			dbPnt = 0;

			wp.durationTime(startTime, "select " + wp.daoTable.toUpperCase());

			daoTid = "";
			wp.sqlCmd = "";
			wp.daoTable = "";
			wp.whereStr = "";
			wp.selectSQL = "";
			wp.whereOrder = "";
		} // End of try
		catch (SQLException ex2) {
			int liExp = getSqlErrmsg(ex2);
			wp.logErr("SQL-err=" + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder);
			if (liExp == 1) {
				wp.errSql = "Y";
				wp.expMethod = "pageQuery";
				wp.expHandle(ex2);
			}
		} catch (Exception ex) {
			setSqlErrmsg(ex.getMessage());
			wp.errSql = "Y";
			wp.expMethod = "pageQuery";
			wp.logErr("SQL-err=" + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " " + wp.whereOrder);
			wp.expHandle(ex);
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

	} // End of selectTable

	public void pageSelect() {
		pageSelect(null);
	}

	public void pageSelect(Object param[]) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		sqlErrtext = "";
		sqlCode = 100;
		sqlRowNum = 0;
		wp.notFound = "Y";

		try {
			if (empty(wp.sqlCmd)) {
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT ")
				  .append(wp.selectSQL)
				  .append(" FROM ")
				  .append(wp.daoTable)
				  .append(" ")
				  .append(wp.whereStr);
				if (wp.whereOrder.length() > 0) { // && wp.sqlCmd.toLowerCase().indexOf(" order by ") < 0) {
					sb.append(" ").append(wp.whereOrder);
				}
				wp.sqlCmd = sb.toString();
			}

			if (param == null) {
				if (sqlParm.nameSqlParm(wp.sqlCmd)) {
					wp.sqlCmd = sqlParm.getConvSQL();
					param = sqlParm.getConvParm();
				}
			}
			// -SQL-Log----------------
			dddSqlLog(wp.sqlCmd, param);

			ps = wp.conn[dbPnt].prepareStatement(wp.sqlCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			if (param != null) {
				for (int ii = 0; ii < param.length; ii++) {
					ps.setObject(ii + 1, param[ii]);
				}
			}

			rs = ps.executeQuery();
			java.sql.ResultSetMetaData mdata = rs.getMetaData();
			readColNames(mdata);
			colName = getColNames();
			colType = getColTypes();
			columnCnt = getColumnCnt();

			int likk = wp.sumLine;
			String strColName = "";
			String str = "";

			while (rs.next()) {
				sqlRowNum++;
				for (int k = 1; k < colName.length; k++) {
					strColName = daoTid + colName[k];
					if (colType[k].equalsIgnoreCase("BLOB")) {
						wp.blobValue = rs.getBlob(k);
					} else {
						str = "";
						if (colType[k].equals("DECIMAL") || colType[k].equals("DOUBLE")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getDouble(k);
							} else
								str = "0";
							wp.setNumber(strColName, rs.getDouble(k), likk);
						} else if (colType[k].equals("INTEGER") || colType[k].equals("LONG")) {
							if (rs.getObject(k) != null) {
								str = "" + rs.getLong(k);
							} else
								str = "0";
							wp.setNumber(strColName, rs.getLong(k), likk);
						} else {
							if (rs.getObject(k) != null) {
								str = rs.getObject(k).toString(); // .trim();
							} else
								str = "";
							wp.setValue(strColName, str, likk);
						}

						if (wp.jsonCode.equals("Y") && wp.autoJSON) {
							wp.addJSON(strColName, nvl(str));
						}
					}
				}
				likk++;
				if (likk > 999)
					break;
			} // End of while

			if (sqlRowNum == 0) {
				wp.svWhere = wp.whereStr;
				wp.notFound = "Y";
				wp.showLogMessage("W", "NOT FOUND : " + wp.sqlCmd, "");
			} else {
				sqlCode = 0;
				wp.notFound = "N";
				zzIdnoQuerylog();
			}

			wp.autoJSON = false;
			wp.selectCnt = sqlRowNum;
			wp.firstRow = 0;
			wp.varRows = 0;
			rs.close();
			ps.close();
			rs = null;
			ps = null;
			wp.actionCode = "";
			dbPnt = 0;

			daoTid = "";
			wp.sqlCmd = "";
			wp.daoTable = "";
			wp.whereStr = "";
			wp.selectSQL = "";
		} // End of try
		catch (SQLException ex2) {
			wp.logErr("SQL-err=" + wp.sqlCmd);
			int liExp = getSqlErrmsg(ex2);
			if (liExp == 1) {
				wp.errSql = "Y";
				wp.expMethod = "pageSelect";
				wp.expHandle(ex2);
			}
		} catch (Exception ex) {
			wp.logErr("SQL=" + wp.sqlCmd);
			setSqlErrmsg(ex.getMessage());
			wp.errSql = "Y";
			wp.expMethod = "pageSelect";
			wp.expHandle(ex);
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
	} // End of selectTable

	private void dddSqlLog(String aSql, Object[] aParam) {
		wp.logSql(aSql, aParam);
	}

	private void zzIdnoQuerylog() {
		String privateInfo = "", lsDebit = "";
		
		if (empty(qqDebit) && empty(qqIdno) && empty(qqCard)
				&& wp.colEmpty(daoTid +ID_NO_KEY) && wp.colEmpty(daoTid +ACCT_KEY) 
				&& wp.colEmpty(daoTid +CARD_NO_KEY) 
				) {
			return;
		}
		
		// --
		for (int i = 0; i < sqlRowNum; i++) {
			
			privateInfo = getPrivateInfo(i);
			if (empty(privateInfo)) {
				break;
			}
			
			// --
			lsDebit = getDebitFlag();
			
			zzAppLogIdno(lsDebit, privateInfo);
		}

		
	}

	private String getDebitFlag() {
		String lsDebit = "N";
		if (!empty(qqDebit)) {
			if (qqDebit.length() == 1) {
				lsDebit = qqDebit;
			} else
				lsDebit = wp.colStr(daoTid + qqDebit);
		}
		return lsDebit;
	}

	private String getPrivateInfo(int index) {
		String privateInfo = "";
		
		// --
		if (empty(privateInfo) && !empty(qqCard)) {
			privateInfo = wp.colStr(index, daoTid + qqCard);
			if (!empty(privateInfo))
				privateInfo = "CRD=" + privateInfo;
		}
		if (!empty(qqIdno)) {
			privateInfo = wp.colStr(index, daoTid + qqIdno);
			if (!empty(privateInfo))
				privateInfo = "ID=" + privateInfo;
		}	
		
		// -default-
		if (empty(privateInfo)) {
			privateInfo = wp.colStr(index, daoTid + CARD_NO_KEY);
			if (!empty(privateInfo))
				privateInfo = "CRD=" + privateInfo;
		}
		if (empty(privateInfo)) {
			privateInfo = wp.colStr(index, daoTid + ID_NO_KEY);
			if (empty(privateInfo))
				privateInfo = wp.colStr(index, daoTid + ACCT_KEY);
			if (!empty(privateInfo))
				privateInfo = "ID=" + privateInfo;
		}
		
		return privateInfo;
	}

	public void appLogIdno(String vdFlag, String aIdno) {
		String lsDebit = "";
		if (empty(vdFlag)) {
			lsDebit = wp.colStr("debit_flag");
		} else {
			if (commString.strIn2(vdFlag, "N,Y"))
				lsDebit = vdFlag;
			else {
				lsDebit = wp.colStr(vdFlag);
			}
		}
		vdFlag = commString.nvl(lsDebit, "N");

		// -IDNO-
		String lsIdno = "";
		if (!empty(aIdno)) {
			lsIdno = wp.colStr(aIdno);
			zzAppLogIdno(vdFlag, lsIdno);
			return;
		}

		lsIdno = wp.colStr(ID_NO_KEY);
		if (!empty(lsIdno)) {
			zzAppLogIdno(vdFlag, "ID=" + lsIdno);
			return;
		}
		lsIdno = wp.colStr(CARD_NO_KEY);
		if (!empty(lsIdno)) {
			zzAppLogIdno(vdFlag, "CRD=" + lsIdno);
			return;
		}
		lsIdno = wp.colStr(ACCT_KEY);
		if (!empty(lsIdno)) {
			zzAppLogIdno(vdFlag, "ID=" + lsIdno);
			return;
		}
	}

	private void zzAppLogIdno(String vdFlag, String privateInfo) {
		if (empty(privateInfo))
			return;

		/*
		 * 系統名稱 交易時間 使用者 端末機號 作業代號(java.html) 查詢理由 VD 查詢條件
		 * -ECS-系統--MMdd-HHmm--xxxxxxxx10-999.999.999.999
		 * --xxxxxxxx10.xxxxxxxxxxxxx15--XX----X--ID=12345678901234567890
		 */
		String str = "" + // commString.right(wp.sysDate,4)+"-"+commString.left(wp.sysTime,4)+commString.space(2)+
				commString.rpad(wp.loginUser, 10) + " " + commString.rpad(wp.request.getRemoteAddr(), 15) + "  "
				+ commString.rpad(wp.modPgm() + "." + wp.respHtml, 26) + "  " + commString.rpad(wp.queryReason, 2) + "    "
				+ commString.nvl(vdFlag, "N") + "  " + commString.rpad(privateInfo, 23);
//		wp.showLog("I", ">>>idno>>>" + str);
//		wp.showLog("P",">>>idno>>>" + str);
		wp.showPDPALog(">>>idno>>>" + str);
	}

	public int selectCount() {
		String sql1 = "select count(*) from " + wp.daoTable + " " + wp.whereStr;
		Object[] param = null;
		return selectCount(sql1, param);
	}

	public int selectCount(String sTable, String sWhere, Object[] param) {
		String lsTable = sTable;
		if (empty(lsTable)) {
			lsTable = wp.daoTable;
		}
		String lsWhere = sWhere;
		if (empty(lsWhere)) {
			lsWhere = wp.whereStr;
		}
		String lsSql = "SELECT COUNT(*) " + " FROM " + lsTable + " " + lsWhere;

		return selectCount(lsSql, param);
	}

	public int selectCount(String sTable, String sWhere) {
		String lsTable = sTable;
		if (empty(lsTable)) {
			lsTable = wp.daoTable;
		}
		String lsWhere = sWhere;
		if (empty(lsWhere)) {
			lsWhere = wp.whereStr;
		}
		String lsSql = "SELECT COUNT(*) as xx_cnt " + " FROM " + lsTable + " " + lsWhere;

		sqlSelect(lsSql, null);
		if (sqlRowNum > 0)
			return sqlInt("xx_cnt");

		// Object[] param=null;
		// if (oo_parm.nameSqlParm(ls_sql)){
		// ls_sql =oo_parm.get_convSQL();
		// param =oo_parm.get_convParm();
		// }

		// if ( Arrays.asList("N","S").contains(parmFlag) ) {
		// param = nameSqlParm(ls_sql);
		// ls_sql=convertSQL;
		// }

		return 0;
	}

	public int selectCount(String lsSql, Object param[]) {
		ResultSet rs = null;
		int llCnt = 0;

		// -2019-1224-
		try (PreparedStatement ps = wp.conn[dbPnt].prepareStatement(lsSql, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);) {
			if (param != null) {
				for (int ii = 0; ii < param.length; ii++) {
					ps.setObject(ii + 1, param[ii]);
				}
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				llCnt = rs.getInt(1);
			}
			// wp.ddd("SQL-Count="+ll_cnt+"; sql="+ls_sql);
			rs.close();
			ps.close();
			rs = null;
		} // End of try
		catch (SQLException ex2) {
			wp.logErr("SQL-err=" + lsSql);
			int liExp = getSqlErrmsg(ex2);
			if (liExp == 1) {
				wp.errSql = "Y";
				wp.expMethod = "pageSelect";
				wp.expHandle(ex2);
			}
		} catch (Exception ex) {
			wp.logErr("SQL:" + lsSql);
			wp.expHandle("selectCount", ex);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception ex2) {
			}
		}

		return llCnt;
	}

	private void pageQueryRowcount(Object param[]) {
		if (empty(wp.pageCountSql) && empty(wp.daoTable) == false) {
			wp.pageCountSql = "SELECT COUNT(*) " + wp.sumField + " FROM " + wp.daoTable + " " + wp.whereStr;
		}
		if (empty(wp.pageCountSql)) {
			return;
		}
		// wp.ddd("page-tot-rows="+wp.totalRows);
		boolean tempIsNoClear = sqlParm.getNoClear();
		if (param == null && sqlParm.nameSqlParm(wp.pageCountSql, true)) {
			param = sqlParm.getConvParm();
			wp.pageCountSql = sqlParm.getConvSQL();
		}
		if (tempIsNoClear) {
			sqlParm.setSqlParmNoClear(tempIsNoClear);
		}
		// if ( Arrays.asList("N","S").contains(parmFlag) ) {
		// param = nameSqlParm(wp.pageCount_sql);
		// wp.pageCount_sql=convertSQL;
		// }
		wp.dataCnt = selectCount(wp.pageCountSql, param);
		wp.totalRows = wp.dataCnt;
		// wp.ddd_sql(wp.pageCount_sql,param);
	}

	/* 下拉式選單 */
	public boolean dropdownList(String dynamicName, String dynamicTable, String optionValue, String optionDisplay)
			throws Exception {
		String checkFlag = "";
		wp.varRows = 10000;
		if (optionValue.equals(optionDisplay)) {
			wp.selectSQL = optionValue;
		} else {
			wp.selectSQL = optionValue + "," + optionDisplay;
			checkFlag = "Y";
		}
		wp.daoTable = dynamicTable;
		wp.whereStr = " ORDER BY " + optionValue;
		pageQuery();
		wp.dropDownKey[0] = wp.optionKey;
		if (checkFlag.equals("Y")) {
			optionDisplay = colName[1];
		}

		wp.multiOptionList(0, dynamicName, optionValue, optionDisplay);

		return true;
	}

	// -------------------------------------------------------
	public void sqlSelect() {
		sqlSelect(wp.sqlCmd, null);
	}

	public void sqlSelect(String sql1) {
		sqlSelect(sql1, null);
	}

	public void sqlSelect(Object[] obj) {
		sqlSelect(wp.sqlCmd, obj);
	}

	public void sqlSelect(String sql1, Object[] obj) {
		try {
			sqlSelect(wp.getConn(), sql1, obj);
			wp.logSql2(sqlLog());
		} catch (Exception ex) {
			wp.logErr(this.getClass().getSimpleName() + "-sqlSelect: " + sql1 + "; error: " + ex.getMessage());
			// wp.expHandle(this.getClass().getSimpleName() + "-sqlSelect: " + sql1, ex);
			wp.expHandle(this.getClass().getSimpleName() + "-sqlSelect: SQL運行異常！", ex);
		}
	}

	public void sqlSelect(TarokoCommon wp, String sql1, Object[] obj) {
		// -jh:2019-0726-
		PreparedStatement ps = null;
		ResultSet rs = null;
		sqlCode = 0;
		sqlRowNum = 0;
		sqlErrtext = "";

		Object[] param = null;
		try {

			if (obj == null && sqlParm.nameSqlParm(sql1)) {
				sql1 = sqlParm.getConvSQL();
				param = sqlParm.getConvParm();
			} else
				param = obj;

			ps = wp.getConn().prepareStatement(sql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			if (param != null) {
				for (int ii = 0; ii < param.length; ii++) {
					ps.setObject(ii + 1, param[ii]);
				}
			}

			rs = ps.executeQuery();
			java.sql.ResultSetMetaData mdata = rs.getMetaData();
			readColNames(mdata);
			String[] aaCol = getColNames();
			String[] aaType = getColTypes();

			String colName = "";

			// --
			int rr = 0;
			while (rs.next()) {
				sqlRowNum++;
				for (int k = 1; k < aaCol.length; k++) {
					colName = daoTid + aaCol[k];
					if (aaType[k].equalsIgnoreCase("DECIMAL") || aaType[k].equalsIgnoreCase("DOUBLE")) {
						if (rs.getObject(k) != null)
							wp.colSet(rr, colName, rs.getDouble(k));
						else
							wp.colSet(rr, colName, 0);
					} else if (aaType[k].equalsIgnoreCase("INTEGER") || aaType[k].equalsIgnoreCase("LONG")) {
						if (rs.getObject(k) != null)
							wp.colSet(rr, colName, rs.getLong(k));
						else
							wp.colSet(rr, colName, 0);
					} else {
						if (rs.getObject(k) != null) {
							wp.colSet(rr, colName, rs.getObject(k).toString());
						} else
							wp.colSet(rr, colName, "");
					}
				}
				rr++;

				// -JH:2018-0608-
				if (wp.varRows > 0 && sqlRowNum >= wp.varRows) {
					errmsg("????? select-筆數已超過 [%s] ?????", wp.varRows);
					break;
				}
			} // End of while

			daoTid = "";
			rs.close();
			ps.close();
			rs = null;
			ps = null;

			if (sqlRowNum == 0) {
				sqlCode = 100;
			}
		} // End of try
		catch (SQLException ex2) {
			rc = -1;
			int liExp = getSqlErrmsg(ex2);
			if (liExp == 1) {
				log(commString.formatSqlString("sqlerr: " + sql1, param));
				wp.expHandle("pageDAO", ex2);
			}
		} catch (Exception ex) {
			rc = -1;
			setSqlErrmsg(ex.getMessage());
			wp.expHandle("PageDAO", ex);
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

	// --sql:insert,update,delete--
	public void sqlExec(String sql1) {
		if (empty(sql1)) {
			sqlExec(wp.getConn(), wp.sqlCmd, null);
		} else {
			sqlExec(wp.getConn(), sql1, null);
		}
		dddSqlLog(sql1, null);
	}

	public void sqlExec(String sql1, Object param[]) {
		sqlExec(wp.getConn(), sql1, param);
		dddSqlLog(sql1, param);
	}

	// public void sql_exec() {
	// sql_exec(wp.getConn(), wp.sqlCmd, null);
	// }
	//
	// public void sql_exec(String sql1) {
	// sql_exec(sql1, null);
	// }

	private boolean sqlHasLimit(String aSql) {
		String lsSql = aSql.toLowerCase();
		int liWhere = lsSql.lastIndexOf(" where ");
		int liFetch = lsSql.indexOf(" fetch ");
		int liFirst = lsSql.indexOf(" first ");
		int liLimit = lsSql.indexOf(" limit ");
		int liOffset = lsSql.indexOf(" offset ");
		if (liFetch <= 0 && liFirst <= 0 && liLimit <= 0 && liOffset <= 0) {
			return false;
		} else {
			if ((liFetch < liWhere && liFirst < liWhere) && (liLimit < liWhere && liOffset < liWhere)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean getIsQueryOverLimit() {
		return isQueryOverLimit;
	}

}
