/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 111/07/04  V1.00.01  Jeff Kung initial draft                                       *
******************************************************************************/
package Tmp;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class TmpB500W extends AccessDAO {
	private String PROGNAME = "DB2 資料庫 Table 資料匯出(export)可下條件處理程式 111/07/06 V1.00.01";
	CommFunction comm = new CommFunction();
	CommString commstring = new CommString();
	String ECStabName = "", DB2tabName = "", DB2colName = "", outData = "";
	int fo, maxrecCnt;
	String checkHome = "", fileName = "", fileName2 = "", filePath = "";
	String whereCond = "";
	String[] colAName = new String[1000];
	String[] typeAName = new String[1000];
	int[] colScale = new int[1000];
	String newLine = "\n";

// ************************************************************************
	public static void main(String[] args) throws Exception {

		TmpB500W proc = new TmpB500W();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);

			setConsoleMode("N");
			if (!connectDataBase(""))
				return (1);

			if (args.length == 0) {
				showLogMessage("I", "", "For program run should enter [table name] !! ");
				return (1);
			}

			DB2tabName = args[0].toUpperCase(Locale.TAIWAN);
			fileName2 = args[0].toUpperCase(Locale.TAIWAN);
			
			filePath = args[1];
			int testCount = commstring.ss2int(args[2]);
			
			if (args.length==4) {
				whereCond = args[3];
			} else {
				whereCond = "";
			}

			proc_insert_script(testCount);

			finalProcess();
			return (0);
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess
// ************************************************************************

	void proc_insert_script(int testCount) throws Exception {
		
		select_syscat_columns();
		
		checkHome = System.getenv("PROJ_HOME");
		selectSQL = "";
		daoTable = DB2tabName;
		whereStr = whereCond;
		
		showLogMessage("I", "", "Processing table name [ " + DB2tabName + "] ");
		showLogMessage("I", "", "   where condition [ " + whereCond + "] ");

		openCursor();
		String fieldData = "";
		int procFile = 0;
		int procCnt = 0;
		while (fetchTable()) {
			
			if (procCnt == 0 || procCnt%100000==0) {
				
				//第一個檔案結束需要將檔案關閉
				if (procFile > 0) {
					closeOutputText(fo);
				}
				
				procFile ++;
				fileName = filePath + fileName2.toLowerCase(Locale.TAIWAN)+ procFile + ".csv";
				fo = openOutputText(fileName);
				if (fo == -1) {
					showLogMessage("I", "", "檔案" + fileName + "無法開啟寫入 error!");
					return;
				}
				
				String fieldName = "";
				for (int intf = 0; intf < maxrecCnt; intf++) {
					if (intf == (maxrecCnt - 1)) {
						fieldName += (colAName[intf]);
					} else {
						fieldName += (colAName[intf] + "||");
					}
				}

				writeTextFile(fo, fieldName + newLine);
			}
			
			fieldData = "";
			procCnt ++;
			for (int inti = 0; inti < maxrecCnt; inti++) {
				if (inti == maxrecCnt - 1)
					fieldData += (getValue(colAName[inti])+"||E");
				else
					fieldData += (getValue(colAName[inti]).trim() + "||");
			}

			writeTextFile(fo, fieldData + newLine);
			
			if (procCnt%100000==0) {
				showLogMessage("I", "", "Process cnt : [" + procCnt + "]");
			}
			
			if (procCnt == testCount) {
				showLogMessage("I", "", "Total Process cnt : [" + procCnt + "]");
				closeCursor();
				return;
			}

		}
		closeCursor();
		showLogMessage("I", "", "Total Process cnt : [" + procCnt + "]");
	}

// ************************************************************************
	void select_syscat_columns() throws Exception {
		selectSQL = "";
		daoTable = "SYSCAT.COLUMNS";
		whereStr = "WHERE TABNAME = ? and TABSCHEMA = 'ECSCRDB' " + "ORDER BY COLNO";
		setString(1, DB2tabName);

		showLogMessage("I", "", "DB2 TABLE_NAME : [" + DB2tabName + "]");

		int recCnt = selectTable();
		outData = DB2tabName + ",";
		String fieldName = "";
		for (int inti = 0; inti < recCnt; inti++) {
			colAName[inti] = getValue("COLNAME", inti);
			typeAName[inti] = getValue("TYPENAME", inti);
			colScale[inti] = getValueInt("SCALE", inti);
		}
	
		maxrecCnt = recCnt;

	}

} // End of class FetchSample
