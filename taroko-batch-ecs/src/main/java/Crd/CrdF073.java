/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/12/15  V1.00.00    Wendy Lu                     program initial        *
 *  112/03/21  V1.00.01    Wilson      拒絕行銷欄位定義調整                                                                         *
 *  112/09/12  V1.00.02    Wilson      檔名增加日期                                                                                        *
 *  112/12/13  V1.00.03    Wilson      檔名無日期                                                                                            
 *  112/12/27  V1.00.04    Ryan        SQL陳述式太長調整                                                                             *
 *  112/12/28  V1.00.05    Ryan        SQL陳述式太長調整                                                                             *
 ******************************************************************************/

package Crd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;

/* 讀取最新勿擾名單檔案作業 */
public class CrdF073 extends AccessDAO {

	private final String progname = "接收前一日最新勿擾名單檔案作業  112/12/28 V1.00.05";
	private String prgmId = "CrdF073";
	private final byte emptyByte = " ".getBytes()[0];
	private final byte[] nextLineByte = System.lineSeparator().getBytes();

	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString coms = new CommString();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommTxBill commTxBill;
	CommFTP commFTP = null;
	CommRoutine comr = null;

	String queryDate = "";
	String hBusiBusinessDate = "";
	String outputFileName = "";

	String temstr2 = "";
	int cmsChgcolumnLogCnt = 0;
	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commTxBill = new CommTxBill(getDBconnect(), getDBalias());
			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

			selectPtrBusinday();

			// 若沒有給定查詢日期，則查詢日期為系統日
			if (args.length == 0) {
				queryDate = hBusiBusinessDate;
			} else if (args.length == 1) {
				if (!new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
					showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
					return -1;
				}
				queryDate = args[0];
			} else {
				comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
			}

			int holidayCount = 0;

			sqlCmd = "select count(*) holidayCount ";
			sqlCmd += " from ptr_holiday  ";
			sqlCmd += "where holiday = ? ";
			setString(1, queryDate);
			int recordCnt = selectTable();
			if (notFound.equals("Y")) {
				comc.errExit("select_ptr_holiday not found!", "");
			}

			if (recordCnt > 0) {
				holidayCount = getValueInt("holidayCount");
			}

			if (holidayCount > 0) {
				showLogMessage("E", "", "今日為假日,不執行此程式");
				return 0;
			}
			// ====================================
			
			openFile();
			deleteTmpKeyDate();
			
			// ==============================================
			// 固定要做的
			comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatch(1, 0, 0);
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/************************************************************************/
	int openFile() throws Exception {
		final String filePathFromDb = "media/crd";
//		final String fullFileName = SecurityUtil.verifyPath(String.format("70799128_1AA_K_%s", queryDate));
		final String fileExtension = ".TXT";
		final String fileName = "70799128_1AA_K";
		final String fullFileName = fileName + fileExtension;

		// get the fileFolderPath such as C:\EcsWeb\media\icu
		String fileFolderPath = getFileFolderPath(comc.getECSHOME(), filePathFromDb);
		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);
		String filePath = Paths.get(fileFolderPath, fullFileName).toString();
		// open file

		int f = openInputText(filePath, "MS950");

		String tmp = selectCmsChgcolumnLog();
		
		if (f == -1) {
			showLogMessage("I", "", "無檔案需處理");
			return (0);
		} else {
			showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
			showLogMessage("I", "", " Process file =[" + fullFileName + "]");
			int readCnt = 0;
			int upCnt = 0;
			try {
				String cikey = "";
				while (true) {
					cikey = readTextFile(f);
		            if (endFile[f].equals("Y"))
		                break;
		            readCnt ++;
		            if(cikey.length() < 10)
		            	continue; 
					String idNo = coms.left(cikey, 10);
					upCnt += updateCrdIdno1(idNo, tmp);
					upCnt += updateDbcIdno1(idNo, tmp);
					insertTmpKeyData(idNo);
					if(upCnt > 0 && upCnt % 5000 == 0) {
			        	commitDataBase();
			        	showLogMessage("I", "", "update crd_idno_1/dbc_idno_1 end ,cnt = " + upCnt);
					}
				}
				commitDataBase();
				showLogMessage("I", "", "read file ,cnt = " + readCnt);
				showLogMessage("I", "", "update crd_idno_1/dbc_idno_1 end ,cnt = " + upCnt);
				closeInputText(f);
			} catch (IOException e) {
				e.printStackTrace();
			}

			updateCrdIdno2(tmp);
			updateDbcIdno2(tmp);
		
		}

		insertCrdFileCtl(fileName,fileExtension);
		removeFile(fileName,fileExtension);
		
		return (0);
	}
	
	/************************************************************************/
	private String getFileFolderPath(String projectPath, String filePathFromDb) throws Exception {
		String fileFolderPath = null;

		if (filePathFromDb.isEmpty() || filePathFromDb == null) {
			throw new Exception("file path selected from database is error");
		}

		String[] arrFilePathFromDb = filePathFromDb.split("/");

		projectPath = SecurityUtil.verifyPath(projectPath);
		fileFolderPath = Paths.get(projectPath).toString();
		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);

		for (int i = 0; i < arrFilePathFromDb.length; i++)
			fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();
		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);
		return fileFolderPath;
	}

	/***********************************************************************/
	String selectCmsChgcolumnLog() throws Exception {
		sqlCmd = " select ";
		sqlCmd += " distinct id_p_seqno ";
		sqlCmd += " from cms_chgcolumn_log ";
		sqlCmd += " where chg_column = 'market_agree_base' and chg_date = to_char(sysdate, 'yyyymmdd') ";
		String strSql = sqlCmd;
		cmsChgcolumnLogCnt = selectTable();

		return strSql;
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		hBusiBusinessDate = getValue("business_date");
	}

	/**
	 * @param hCikey
	 *********************************************************************/
	int updateCrdIdno1(String containStr, String concatStr) throws Exception {

		daoTable = "crd_idno";
		updateSQL = "market_agree_base = '0',";
		updateSQL += "accept_call_sell = 'N',";
		updateSQL += "call_sell_from_mark = 'B',";
		updateSQL += "call_sell_chg_date = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += "mod_user = 'CrdF073',";
		updateSQL += "mod_time = sysdate, ";
		updateSQL += "mod_pgm = 'CrdF073'";
		whereStr = "where market_agree_base <> '0' and id_no = ? ";
		if (cmsChgcolumnLogCnt > 0) {
			whereStr += " and id_p_seqno not in ( ";
			whereStr += concatStr;
			whereStr += " ) ";
		}
		setString(1, containStr);
		int n = updateTable();

		return n;
	}

	/**
	 * @param hCikey
	 *********************************************************************/
	void updateCrdIdno2(String concatStr) throws Exception {
		int upCnt = 0;
		while(true) {
			daoTable = "crd_idno";
			updateSQL = "market_agree_base = '2',";
			updateSQL += "accept_call_sell = 'Y',";
			updateSQL += "call_sell_from_mark = 'B',";
			updateSQL += "call_sell_chg_date = to_char(sysdate, 'yyyymmdd'),";
			updateSQL += "mod_user = 'CrdF073',";
			updateSQL += "mod_time = sysdate, ";
			updateSQL += "mod_pgm = 'CrdF073'";
			whereStr = " where market_agree_base not in('1','2') ";
			whereStr += " and id_no not in ( select id_no from tmp_key_data where mod_pgm = 'CrdF073' ) ";
			if(cmsChgcolumnLogCnt > 0) {
				whereStr += " and id_p_seqno not in ( ";
    			whereStr += concatStr;
    			whereStr += " ) ";
			}
		
			whereStr += " fetch first 5000 rows only ";
            int reccnt = updateTable();
            upCnt += reccnt;
        	if(reccnt == 0) {
        		break;
        	}
        	showLogMessage("I", "", "update crd_idno_2 ,cnt = " + upCnt);
        	commitDataBase();
		}		
	}

	/**
	 * @param hCikey
	 *********************************************************************/
	int updateDbcIdno1(String containStr, String concatStr) throws Exception {
		daoTable = "dbc_idno";
		updateSQL = "market_agree_base = '0',";
		updateSQL += "accept_call_sell = 'N',";
		updateSQL += "call_sell_from_mark = 'B',";
		updateSQL += "call_sell_chg_date = to_char(sysdate, 'yyyymmdd'),";
		updateSQL += "mod_user = 'CrdF073',";
		updateSQL += "mod_time = sysdate, ";
		updateSQL += "mod_pgm = 'CrdF073'";
		whereStr = "where market_agree_base <> '0' and id_no = ? ";
		if (cmsChgcolumnLogCnt > 0) {
			whereStr += " and id_p_seqno not in ( ";
			whereStr += concatStr;
			whereStr += " ) ";
		}
		setString(1, containStr);
		int n = updateTable();

		return n;
	}

	/**
	 * @param hCikey
	 *********************************************************************/
	void updateDbcIdno2(String concatStr) throws Exception {
		int upCnt = 0;
		
		while(true) {
			daoTable = "dbc_idno";
			updateSQL = "market_agree_base = '2',";
			updateSQL += "accept_call_sell = 'Y',";
			updateSQL += "call_sell_from_mark = 'B',";
			updateSQL += "call_sell_chg_date = to_char(sysdate, 'yyyymmdd'),";
			updateSQL += "mod_user = 'CrdF073',";
			updateSQL += "mod_time = sysdate, ";
			updateSQL += "mod_pgm = 'CrdF073'";
			whereStr = " where market_agree_base not in('1','2') ";
			whereStr += " and id_no not in ( select id_no from tmp_key_data where mod_pgm = 'CrdF073' ) ";
			if(cmsChgcolumnLogCnt > 0) {
				whereStr += " and id_p_seqno not in ( ";
    			whereStr += concatStr;
    			whereStr += " ) ";
			}
			whereStr += " fetch first 5000 rows only ";
            int reccnt = updateTable();
            upCnt += reccnt;
        	if(reccnt == 0) {
        		break;
        	}
          	showLogMessage("I", "", "update dbc_idno_2 ,cnt = " + upCnt);
        	commitDataBase();
		}
	}
	
	private void insertTmpKeyData(String idNo) throws Exception {
		daoTable = "TMP_KEY_DATA";
		setValue("ID_NO",idNo);
		setValue("CARD_NO","");
		setValue("MOD_PGM","CrdF073");
		setValue("DATE_01","");
		insertTable();
	}
	
	private void deleteTmpKeyDate() throws Exception {
		daoTable = "TMP_KEY_DATA";
		whereStr = "WHERE MOD_PGM = 'CrdF073' ";
		int delCnt = deleteTable();
     	showLogMessage("I", "", "delete TMP_KEY_DATA ,cnt = " + delCnt);
	}

	/***********************************************************************/

	public void removeFile(String removeFileName,String fileExtension) throws Exception {
		String fileStr1 = comc.getECSHOME() + "/media/crd/" + removeFileName + fileExtension;
		String fileStr2 = comc.getECSHOME() + "/media/crd/backup/" + removeFileName + "_" + sysDate + fileExtension;

		if (comc.fileRename2(fileStr1, fileStr2) == false) {
			showLogMessage("I", "", "Error : File = [" + removeFileName + fileExtension + "] rename fail!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + fileExtension +"] 已移至 [" + fileStr2 + "]");
	}

	/***********************************************************************/
	public int insertCrdFileCtl(String fileName, String fileExtension) throws Exception {
		setValue("file_name", fileName + '_' + sysDate + fileExtension);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);

		daoTable = "crd_file_ctl";

		insertTable();
		return (0);
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CrdF073 proc = new CrdF073();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
