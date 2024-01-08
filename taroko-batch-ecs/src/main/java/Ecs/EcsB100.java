/*****************************************************************************
*                                                                                                      *
*                              MODIFICATION LOG                                           *
*                                                                                                      *
*    DATE    Version    AUTHOR              DESCRIPTION                         *
*  --------  -------------------  ----------------------------------------------- *
* 111/06/17  V1.00.01  JeffKung   initial draft                                 *
*                                                                                                      *
******************************************************************************/
package Ecs;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class EcsB100 extends AccessDAO {
	private String PROGNAME = "批次排程設定檔轉入每日排程控制檔 111/06/17 V1.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	int totalCnt = 0;
	String checkHome = "";
	String hBusiBusinessDate = "";
	String hShellId = "";

	// ************************************************************************
	public static void main(String[] args) throws Exception {
		EcsB100 proc = new EcsB100();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
		return;
	}

	// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			checkHome = System.getenv("PROJ_HOME");

			// 固定要做的
			if (!connectDataBase()) {
				comcr.errRtn("connect DataBase Error", "", "");
			}

			selectPtrBusinday();
			
			//取得帶入的參數值
			if (args.length > 0) {
				for (int i =0; i<args.length ; i++) {
					
					if ("SHELL=".equals(comc.getSubString(args[i],0, 6))) {
						hShellId = comc.getSubString(args[i],6);
					}
				}
			}
			
			//指定特定shellId要先刪除重覆資料
			if ("".equals(hShellId) == false ) {
				commitDataBase();
				deleteEcsBatchCtl();
				commitDataBase();
			}
			
			selectEcsBatchSetting();

			finalProcess();
			return 0;
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess

	/*******************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = " select business_date ";
		sqlCmd += " from   ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		if (selectTable() > 0) {
			hBusiBusinessDate = getValue("business_date");
		} else {
			comcr.errRtn("selectPtrBusinday error", "", "");
		}
	}

	/*******************************************************************/
	void selectEcsBatchSetting() throws Exception {

		int idxParm = 0;
		sqlCmd = " select shell_id, priority_level, pgm_id, pgm_desc, key_parm1, key_parm2, ";
		sqlCmd += "        key_parm3, key_parm4, key_parm5, wait_flag, repeat_code, normal_code, ";
		sqlCmd += "        call_duty_ind ";
		sqlCmd += " from   ecs_batch_setting ";
		sqlCmd += " where priority_level >0  ";
		
		//指定Shell ID時, 只處理特定資料
		if ("".equals(hShellId) == false ) {
			sqlCmd += " and   shell_id = ?  ";
			setString(++idxParm, hShellId);
		}
		
		sqlCmd += " order by shell_id, priority_level ";

		daoTable = "ecs_batch_setting";
		totalCnt = 0;

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

			totalCnt++;

			insertEcsBatchCtl();
			if (totalCnt % 5000 == 0) {
				showLogMessage("I", "", "Process Count :  " + totalCnt);
				countCommit();
			}

		}
		closeCursor(cursorIndex);
		showLogMessage("I", "", "檔案轉入 [" + totalCnt + "] 筆");
	}
	
	/*******************************************************************/
	void deleteEcsBatchCtl() throws Exception {

		int idxParm = 0;
		
		daoTable = "ecs_batch_ctl";
		whereStr = " where shell_id = ? and   batch_date = ? ";

		setString(++idxParm, hShellId);
		setString(++idxParm, hBusiBusinessDate);
		
		deleteTable();
		
	}

	// ************************************************************************
	public void insertEcsBatchCtl() throws Exception {
		daoTable = "ecs_batch_ctl";

		setValue("batch_date", hBusiBusinessDate);
		setValue("batch_time", sysTime);
		setValue("shell_id", getValue("shell_id"));
		setValueInt("priority_level", getValueInt("priority_level"));
		setValue("pgm_id", getValue("pgm_id"));
		setValue("pgm_desc", getValue("pgm_desc"));
		setValue("key_parm1", getValue("key_parm1"));
		setValue("key_parm2", getValue("key_parm2"));
		setValue("key_parm3", getValue("key_parm3"));
		setValue("key_parm4", getValue("key_parm4"));
		setValue("key_parm5", getValue("key_parm5"));
		setValue("wait_flag", getValue("wait_flag"));
		setValue("repeat_code", getValue("repeat_code"));
		setValue("normal_code", getValue("normal_code"));
		setValue("call_duty_ind", getValue("call_duty_ind"));
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", "system");
		setValue("mod_pgm", javaProgram);

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", "insert_ecs_batch_ctl error[dupRecord]");
			return;
		}
		return;
	}
} // End of class
