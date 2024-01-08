/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  111/03/10  V1.00.01    JeffKung  program initial                           *
*                                                                             *  
*******************************************************************************/

package Dbb;

import java.sql.Connection;

import org.codehaus.plexus.util.ExceptionUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/* DBB歷史資料清理處理程式 */
public class DbbX010 extends AccessDAO {
  private final String progname = "DBB歷史資料清理處理程式  111/03/10 V1.00.01";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  String hTempUser = "";
  final int DEBUG = 0;
  String hBusinessDate = "";
  String parmBatchDate = "";
  String parmProcess = "";

  // ***********************************************************

	public int mainProcess(String[] args) {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			selectPtrBusinday();

			if ("".equals(businessDate)) {
				return 0;
			}
			
			if (args.length > 0) {
				parmProcess = args[0];
				showLogMessage("I", "", "程式執行傳入要處理的processName:" + parmProcess);
			} else {
				showLogMessage("E", "", "程式執行必須傳入要處理的processName(housekeeping/update/delete) !!");
				return 0;
			}

			int procRslt = 0;
			if ("housekeeping".equals(parmProcess)) {
				procRslt = processHouseKeeping(args);
			} else if ("update".equals(parmProcess)) {
				procRslt = processUpdateTable(args);
			} else if ("delete".equals(parmProcess)) {
				procRslt = processDeleteTable(args);
			}else {
				showLogMessage("I", "", "程式執行必須傳入要處理的processName(housekeeping/update/delete) !!");
			}
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
    void selectPtrBusinday() throws Exception{
    	
    	sqlCmd = "select business_date from ptr_businday";
    	 if (selectTable() > 0) {
    		 businessDate = getValue("business_date");
    		 showLogMessage("I", "", "本日營業日:" + businessDate);
         } else {
        	 showLogMessage("E", "", "營業日系統檔未建檔, ");
         }
    }


	int processHouseKeeping(String[] args) throws Exception {

		if (args.length == 2) {
			parmBatchDate = args[1];
		} else {
			parmBatchDate = comm.nextNDate(businessDate, -90);
		}

		showLogMessage("I", "", "處理日期參數 : " + parmBatchDate);

		selectFromBilPostcntl();
		selectFromBilFiscctl();
		
		//updateBilPostcntl();
		//updatePtrBillunit();

		return 0;
	}

	int processUpdateTable(String[] args) throws Exception {

		String parmTableName = "";
		String parmFieldName = "";
		String parmCondition = "";
		
		if (args.length >= 2) {
			parmTableName = args[1];
		} 
		
		if ("ECS_FTP_LOG".equalsIgnoreCase(parmTableName)) {
			if (args.length >= 3) {
				parmFieldName = args[2];
			} else {
				showLogMessage("E", "", "未傳入FieldName參數 !! ");
				return 1;
			}
			
			if ("FILE_NAME".equalsIgnoreCase(parmFieldName)) {
				if (args.length == 4) {
					parmCondition = args[3];
				} else {
					showLogMessage("E", "", "未傳入Condition參數 !! ");
					return 1;
				}
				updateEcsFtpLogFileName(parmCondition);
			} else if ("FILE_DATE".equalsIgnoreCase(parmFieldName)) {
				if (args.length == 4) {
					parmCondition = args[3];
				} else {
					parmCondition = businessDate;
				}
				updateEcsFtpLogFileDate(parmCondition);
			} else {
				showLogMessage("E", "", "未傳入定義的FieldName參數 !! ");
				return 1;
			}
			
		} else {
			showLogMessage("E", "", "未傳入table參數 !! ");
			return 1;
		}

		return 0;
	}
	
	int processDeleteTable(String[] args) throws Exception {

		String parmTableName = "";
		String parmCondition = "";
		
		if (args.length >= 2) {
			parmTableName = args[1];
		} 
		
		if ("ECS_FTP_LOG".equalsIgnoreCase(parmTableName)) {
			if (args.length == 3) {
				parmCondition = args[2];
			} else {
				showLogMessage("E", "", "未傳入Condition參數 !! ");
				return 1;
			}
			
			deleteEcsFtpLog(parmCondition);
			
		} else {
			showLogMessage("E", "", "未傳入table參數 !! ");
			return 1;
		}

		return 0;
	}

	void selectFromBilPostcntl() throws Exception {
		showLogMessage("I", "", "=====Start selectFromBilPostcntl()======");
		sqlCmd  = "select batch_no from bil_postcntl ";
	    sqlCmd+= "where batch_date <= ? ";
	    
	    setString(1, parmBatchDate);
		
		openCursor();

		String batchNo = "";
		while(fetchTable()) {
			batchNo = getValue("batch_no");
			showLogMessage("I", "", "process BATCH_NO ==>"+batchNo);
			
			deleteBilCurpost(batchNo);
			deleteBilNccc300Dtl(batchNo);
			deleteDbbCurpost(batchNo);
			deleteBilPostcntl(batchNo);

			commitDataBase();
		}
		
		commitDataBase();
		closeCursor();
		showLogMessage("I", "", "=====End selectFromBilPostcntl()======");
		
	}
	
	void selectFromBilFiscctl() throws Exception {
		showLogMessage("I", "", "=====Start selectFromBilFiscctl()======");
		sqlCmd  = "select fctl_no from bil_fiscctl ";
	    sqlCmd+= "where fctl_date <= ? ";
	    
	    setString(1, parmBatchDate);
		
		openCursor();

		String batchNo = "";
		while(fetchTable()) {
			batchNo = getValue("fctl_no");
			showLogMessage("I", "", "process BATCH_NO ==>"+batchNo);

			deleteBilFiscdtl(batchNo);
			deleteBilFiscctl(batchNo);

			commitDataBase();
		}
		
		commitDataBase();
		closeCursor();
		showLogMessage("I", "", "=====End selectFromBilFiscctl()======");

	}
	
	void updateBilPostcntl() throws Exception {
		showLogMessage("I", "", "=====Start updateBilPostcntl()======");
		sqlCmd  = "update bil_postcntl set confirm_flag_p = 'Y' ";
	    sqlCmd+= "where batch_date > ? ";
	    
	    setString(1, parmBatchDate);
		
	    try {
			int updateCnt = updateTable();
			showLogMessage("I", "", "update bil_postcntl batch_cnt["+updateCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] updateBilPostcntl  => " + msg);
		}

		commitDataBase();
		showLogMessage("I", "", "=====End updateBilPostcntl()======");
		
	}
	
	void updatePtrBillunit() throws Exception {
		showLogMessage("I", "", "=====Start updatePtrBillunit()======");
		sqlCmd  = "update ptr_billunit set conf_flag = 'N' ";
	    sqlCmd+= "where bill_unit = 'FI' ";
		
	    try {
			int updateCnt = updateTable();
			showLogMessage("I", "", "update ptr_bill_unit ["+updateCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] updatePtrBillunit  => " + msg);
		}

		commitDataBase();
		showLogMessage("I", "", "=====End updatePtrBillunit()======");
		
	}
	
	void deleteBilCurpost(String batchNo) throws Exception {
		daoTable = "BIL_CURPOST";
		whereStr = "WHERE BATCH_NO = ? ";
		setString(1,batchNo);
		
		try {
			int deleteCnt = deleteTable();
			showLogMessage("I", "", "delete from bil_curpost batch_cnt["+deleteCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] deleteBilCurpost  => " + msg);
		}
	}
	
	void deleteDbbCurpost(String batchNo) throws Exception {
		daoTable = "DBB_CURPOST";
		whereStr = "WHERE BATCH_NO = ? ";
		setString(1,batchNo);
		
		try {
			int deleteCnt = deleteTable();
			showLogMessage("I", "", "delete from dbb_curpost  batch_cnt["+deleteCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] deleteDbbCurpost  => " + msg);
		}
	}
	
	void deleteBilNccc300Dtl(String batchNo) throws Exception {
		daoTable = "BIL_NCCC300_DTL";
		whereStr = "WHERE BATCH_NO = ? ";
		setString(1,batchNo);
		
		try {
			int deleteCnt = deleteTable();
			showLogMessage("I", "", "delete from bil_nccc300_dtl  batch_cnt["+deleteCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] deleteBilNccc300Dtl  => " + msg);
		}
	}
	
	void deleteBilPostcntl(String batchNo) throws Exception {
		daoTable = "BIL_POSTCNTL";
		whereStr = "WHERE BATCH_NO = ? ";
		setString(1,batchNo);
		
		try {
			int deleteCnt = deleteTable();
			showLogMessage("I", "", "delete from bil_postcntl  batch_cnt["+deleteCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] deleteBilPostcntl  => " + msg);
		}
	}
	
	void deleteBilFiscdtl(String batchNo) throws Exception {
		daoTable = "BIL_FISCDTL";
		whereStr = "WHERE ECS_FCTL_NO = ? ";
		setString(1,batchNo);
		
		try {
			int deleteCnt = deleteTable();
			showLogMessage("I", "", "delete from bil_fiscdtl  batch_cnt["+deleteCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] deleteBilFiscdtl  => " + msg);
		}
	}
	
	void deleteBilFiscctl(String batchNo) throws Exception {
		daoTable = "BIL_FISCCTL";
		whereStr = "WHERE FCTL_NO = ? ";
		setString(1,batchNo);
		
		try {
			int deleteCnt = deleteTable();
			showLogMessage("I", "", "delete from bil_fiscctl  batch_cnt["+deleteCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] deleteBilFiscctl  => " + msg);
		}
	}

	void updateEcsFtpLogFileName(String parmCondition) throws Exception {
		showLogMessage("I", "", "=====Start updateEcsFtpLogFileName()======");
		
		sqlCmd  = "update ecs_ftp_log set proc_code = 'E' ";
	    sqlCmd+= "where file_name = ? ";
	    sqlCmd+= " and trans_resp_code = 'Y' ";
	    sqlCmd+= " and proc_code in ('', '0') ";
	    
	    setString(1,parmCondition);
		
	    try {
			int updateCnt = updateTable();
			showLogMessage("I", "", "update ECS_FTP_LOG count: ["+updateCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] updateEcsFtpLogFileName  => " + msg);
		}

		commitDataBase();
		showLogMessage("I", "", "=====End updateEcsFtpLogFileName()======");
		
	}
	
	void updateEcsFtpLogFileDate(String parmCondition) throws Exception {
		showLogMessage("I", "", "=====Start updateEcsFtpLogFileDate()======");
		
		sqlCmd  = "update ecs_ftp_log set file_date = ? ";
	    sqlCmd+= "where source_from='FISC_FCARD' and trans_mode='RECV' ";
	    sqlCmd+= " and trans_resp_code = 'Y' and proc_code in ('', '0') ";
	    
	    setString(1,parmCondition);
		
	    try {
			int updateCnt = updateTable();
			showLogMessage("I", "", "update ECS_FTP_LOG count: ["+updateCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] updateEcsFtpLogFileDate  => " + msg);
		}

		commitDataBase();
		showLogMessage("I", "", "=====End updateEcsFtpLogFileDate()======");
		
	}
	
	void deleteEcsFtpLog(String parmCondition) throws Exception {
		showLogMessage("I", "", "=====Start deleteEcsFtpLog()======");
		
		daoTable = "ECS_FTP_LOG";
		whereStr = "WHERE 1=1 ";
	    whereStr+= " and file_name = ?  ";
	    whereStr+= " and trans_resp_code = 'Y' ";
	    whereStr+= " and proc_code in ('', '0','E') ";
	    
	    setString(1,parmCondition);
		
	    try {
			int deleteCnt = deleteTable();
			showLogMessage("I", "", "delete ECS_FTP_LOG count: ["+deleteCnt+"] ");
			
		} catch (Exception ex) {
			String msg = ExceptionUtils.getFullStackTrace(ex);
			showLogMessage("E", "", "  [ERROR] deleteEcsFtpLog  => " + msg);
		}

		commitDataBase();
		showLogMessage("I", "", "=====End deleteEcsFtpLog()======");
		
	}

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbbX010 proc = new DbbX010();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

}
