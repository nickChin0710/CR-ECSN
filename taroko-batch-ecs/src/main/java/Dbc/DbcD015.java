/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           * 
*  109/11/13  V1.01.02  yanghan       修改了變量名稱和方法名稱
*   110/06/18  V1.01.03   Wilson     where條件新增 -> end_ibm_date <> ''          * 
*   112/01/06  V1.00.04   Wilson     移除end_ibm_date                           *  
*   ******************************************************************************/

package Dbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*製卡成功\庫存計算處理*/
public class DbcD015 extends AccessDAO {
	private String progname = "製卡成功 庫存計算處理 110/06/18  V1.01.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	String prgmId = "DbcD015";
	String hCallBatchSeqno = "";

	String hQueryDate = "";
	int hBrnwInCnt = 0;
	int hBrnwOutCnt = 0;
	int hBrnwAccumInCnt = 0;
	int hBrnwAccumOutCnt = 0;
	String hBrnwBranch = "";
	String hBrnwCardType = "";
	String hBrnwGroupCode = "";
	String hBrnwWarehouseDate = "";
	String hBusinessDate = "";
	String hSystemDate = "";
	String hWareYear = "";
	int hWareMonth = 0;
	String hDd = "";
	int liErr = 0;

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0 && args.length != 1) {
				comc.errExit("Usage : DbcD015 query_date(yyyymmdd)", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			hQueryDate = "";
			if (args.length > 0 && args[0].length() == 8) {
				hQueryDate = args[0];
			} else {
				sqlCmd = "select to_char(sysdate-1 days,'yyyymmdd') h_query_date ";
				sqlCmd += " from dual ";
				int recordCnt = selectTable();
				if (recordCnt > 0) {
					hQueryDate = getValue("h_query_date");
				}
			}

			showLogMessage("I", "", String.format("query_date[%s]", hQueryDate));

			commonRtn();

			process();

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void commonRtn() throws Exception {

		sqlCmd = "select online_date ";
		sqlCmd += " from ptr_businday ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusinessDate = getValue("online_date");
		}

		sqlCmd = "select to_char(sysdate,'yyyy') h_ware_year,";
		sqlCmd += "to_number(to_char(sysdate,'mm')) h_ware_month, ";
		sqlCmd += " to_char(sysdate,'yyyymmdd') h_system_date ";
		sqlCmd += " from dual ";
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hWareYear = getValue("h_ware_year");
			hWareMonth = getValueInt("h_ware_month");
			hSystemDate = getValue("h_system_date");
		}

	}

	/***********************************************************************/
	void process() throws Exception {
		sqlCmd = "select ";
		sqlCmd += "substr(act_no,1,3) h_brnw_branch,";
		sqlCmd += "cast(? as varchar(10)) warehouse_date,";
		sqlCmd += "sum(decode(rtn_nccc_date,?,1,0)) h_brnw_in_cnt,";
		sqlCmd += "sum(decode(in_main_date ,?,1,0)) h_brnw_out_cnt,";
		sqlCmd += "card_type,";
		sqlCmd += "decode(group_code,'','0000',group_code) h_brnw_group_code ";
		sqlCmd += "from dbc_emboss ";
		sqlCmd += "where 1 = 1 ";
		sqlCmd += "and rtn_nccc_date = ? ";
		sqlCmd += "and decode(error_code,'',' ',error_code) = ' ' ";
		sqlCmd += "group by substr(act_no,1,3),warehouse_date,card_type, group_code ";
		setString(1, hQueryDate);
		setString(2, hQueryDate);
		setString(3, hQueryDate);
		setString(4, hQueryDate);
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hBrnwBranch = getValue("h_brnw_branch", i);
			hBrnwWarehouseDate = getValue("warehouse_date", i);
			hBrnwInCnt = getValueInt("h_brnw_in_cnt", i);
			hBrnwOutCnt = getValueInt("h_brnw_out_cnt", i);
			hBrnwCardType = getValue("card_type", i);
			hBrnwGroupCode = getValue("h_brnw_group_code", i);

			hBrnwInCnt = hBrnwOutCnt = 0;
			hDd = "";
			hBrnwAccumInCnt = hBrnwAccumOutCnt = 0;
			if (comcr.str2int(hDd) != 1) {
				sqlCmd = "select decode(IN_CNT,'','0',IN_CNT) h_brnw_in_cnt,";
				sqlCmd += "decode(OUT_CNT,'','0',OUT_CNT) h_brnw_out_cnt,";
				sqlCmd += "nvl(accum_in_cnt,0) h_brnw_accum_in_cnt,";
				sqlCmd += "nvl(accum_out_cnt,0) h_brnw_accum_out_cnt ";
				sqlCmd += " from dbc_branch_warehouse  ";
				sqlCmd += "where branch   = ?  ";
				sqlCmd += "and warehouse_date = to_char(to_date(? ,'yyyymmdd')-1 days,'yyyymmdd')  ";
				sqlCmd += "and card_type  = ?  ";
				sqlCmd += "and group_code  = ? ";
				setString(1, hBrnwBranch);
				setString(2, hQueryDate);
				setString(3, hBrnwCardType);
				setString(4, hBrnwGroupCode);
				int recordCnt1 = selectTable();
				if (recordCnt1 > 0) {
					hBrnwInCnt = getValueInt("h_brnw_in_cnt");
					hBrnwOutCnt = getValueInt("h_brnw_out_cnt");
					hBrnwAccumInCnt = getValueInt("h_brnw_accum_in_cnt");
					hBrnwAccumOutCnt = getValueInt("h_brnw_accum_out_cnt");
				}
			}

			liErr = checkExistsBranch();
			if (liErr == 0) {
				liErr = updateBranchWarehouse();
			} else {
				liErr = insertBranchWarehouse();

				if (liErr == 0) {
					commitDataBase();
				} else {
					rollbackDataBase();
					showLogMessage("I", "", "***  ROLLBACK ****");
				}
			}

		}
	}

	/***********************************************************************/
	int updateBranchWarehouse() throws Exception {
		daoTable = "dbc_branch_warehouse";
		updateSQL = "in_cnt   = in_cnt  + ?,";
		updateSQL += " out_cnt  = out_cnt  + ?,";
		updateSQL += " accum_in_cnt = accum_in_cnt + ?,";
		updateSQL += " accum_out_cnt = accum_out_cnt + ?";
		whereStr = "where branch   = ?  ";
		whereStr += "and warehouse_date =to_char(to_date(?,'yyyymmdd'),'yyyymmdd')  ";
		whereStr += "and card_type  = ?  ";
		whereStr += "and group_code  = ? ";
		setInt(1, hBrnwInCnt);
		setInt(2, hBrnwOutCnt);
		setInt(3, hBrnwInCnt);
		setInt(4, hBrnwOutCnt);
		setString(5, hBrnwBranch);
		setString(6, hQueryDate);
		setString(7, hBrnwCardType);
		setString(8, hBrnwGroupCode);
		updateTable();
		if (notFound.equals("Y")) {
			return 1;
		}
		return (0);

	}

	/***********************************************************************/
	int checkExistsBranch() throws Exception {
		hBrnwInCnt = hBrnwOutCnt = 0;
		sqlCmd = "select IN_CNT,";
		sqlCmd += "OUT_CNT ";
		sqlCmd += " from dbc_branch_warehouse  ";
		sqlCmd += "where branch   = ?  ";
		sqlCmd += "and warehouse_date =to_char(to_date(?,'yyyymmdd'),'yyyymmdd')  ";
		sqlCmd += "and card_type  = ?  ";
		sqlCmd += "and group_code  = ? ";
		setString(1, hBrnwBranch);
		setString(2, hQueryDate);
		setString(3, hBrnwCardType);
		setString(4, hBrnwGroupCode);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hBrnwInCnt = getValueInt("IN_CNT");
			hBrnwOutCnt = getValueInt("OUT_CNT");
		} else {
			return (1);
		}

		return (0);
	}

	/***********************************************************************/
	int insertBranchWarehouse() throws Exception {
		setValue("BRANCH", hBrnwBranch);
		setValue("WAREHOUSE_DATE", hBrnwWarehouseDate);
		setValue("CARD_TYPE", hBrnwCardType);
		setValue("GROUP_CODE", hBrnwGroupCode);
		setValueInt("IN_CNT", hBrnwInCnt);
		setValueInt("ACCUM_IN_CNT", hBrnwInCnt + hBrnwInCnt);
		setValueInt("OUT_CNT", hBrnwOutCnt);
		setValueInt("ACCUM_OUT_CNT", hBrnwOutCnt + hBrnwOutCnt);
		setValue("MOD_TIME", sysDate);
		setValue("MOD_PGM", prgmId);
		daoTable = "DBC_BRANCH_WAREHOUSE";
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", "insert DBC_BRANCH_WAREHOUSE duplicate!");
			return 1;
		}

		return (0);
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcD015 proc = new DbcD015();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
