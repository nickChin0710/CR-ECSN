/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/06/30  V1.00.00    Pino      program initial                           *
 *  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *                                                                             *
 *  109/07/03  V1.01.01    JustinWu                  change the program name*
 *  109-07-22    yanghan       修改了字段名称            *
 *  109/08/14  V1.01.05   Wilson       資料夾名稱修改為小寫                                                                         *
 *  109/08/26  V1.01.06   Wilson       檔名修改                                                                                               *
 *  109/10/12  V1.01.07   Wilson       檔名日期改營業日                                                                                * 
 *  109-10-19  V1.00.08    shiyuqi       updated for project coding standard     * 
 *  109/10/20  V1.00.09   Wilson       錯誤報表FTP                                *
 *  110/02/05  V1.00.10   Wilson       LAYOUT改成長度80(不讀HASH值)                *                    
 *  111/02/14  V1.00.11    Ryan      big5 to MS950                                           *                   
 ******************************************************************************/

package Icu;

import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;

public class IcuD075 extends AccessDAO {
	private final String progname = "CARDLINK悠遊卡發卡機構鎖卡名單檔(BKEC)處理程式  111/02/14 V1.00.11";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommFTP commFTP = null;
	CommRoutine comr = null;

	int debug = 0;

	String prgmId = "IcuD075";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	// col1檔頭標籤 col2檔案名稱 col3會員銀行代號 col4處理日期 col5處理時間 col6空白保留欄位 col7HASH值
	protected final String headStr = " col1 ,col2 ,col3 ,col4 ,col5 ,col6 ";
	// col1檔頭標籤 col2資料屬性 col3悠遊卡外顯卡號 col4掛卡日期 col5掛卡時間 col6空白保留欄位 col7HASH值
	protected final String dataStr = " col1 ,col2 ,col3 ,col4 ,col5 ,col6 ";
	// col1檔尾標籤 col2總資料筆數 col3空白保留欄位 col4HASH值
	protected final String trailerStr = " col1 ,col2 ,col3 ";
	protected final int[] headLENGTH = { 1, 4, 8, 8, 6, 53 };
	protected final int[] dataLENGTH = { 1, 2, 20, 8, 6, 43 };
	protected final int[] trailerLENGTH = { 1, 8, 71 };

	String fileDate = "";
	int rptSeq1 = 0;
	String buf = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	String tmpBatchno = "";
	int recno = 0;
	int hRecCnt1 = 0;

	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;
	String errCode;
	String hBusiBusinessDate = "";

	protected String[] head = new String[] {};
	protected String[] data = new String[] {};
	protected String[] trailer = new String[] {};
	Buf1 ncccData1 = new Buf1();

	public int mainProcess(String[] args) {

		try {
			head = headStr.split(",");
			data = dataStr.split(",");
			trailer = trailerStr.split(",");
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 1) {
				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());
			
			selectPtrBusinday();
			
			if (args.length == 0) {
				fileDate = hBusiBusinessDate;
			} else if (args.length == 1) {
				fileDate = args[0];
			}
			if (fileDate.length() != 8) {
				comc.errExit("Usage : " + prgmId, "file_date[yyyymmdd]");
			}
			hModUser = comc.commGetUserID();
			updateBlackltFlag();
			openFile();

			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束,[" + totalInputFile + "],[" + totalOutputFile + "]");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
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

	/************************************************************************/
	int openFile() throws Exception {
		int fileCount = 0;

		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());

		List<String> listOfFiles = comc.listFS(tmpstr, "", "");

		for (String file : listOfFiles) {
			getFileName = file;
			if (getFileName.length() != 24)
				continue;
			if (!getFileName.substring(0, 14).equals("BKEC.00600000."))
				continue;
			if (!getFileName.substring(14, 22).equals(fileDate))
				continue;
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
			comcr.hCallErrorDesc = "Error : 無檔案可處理";
			comcr.errRtn("Error : 無檔案可處理", "處理日期 = " + fileDate , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = comc.getECSHOME() + "/media/icu/" + fileName;

		int f = openInputText(fileName2);
		if (f == -1) {
			return 1;
		}
		closeInputText(f);

		setConsoleMode("N");
		fi = openInputText(fileName2, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return 1;
		}

		showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/icu ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
			if (rec.substring(0, 1).equals("H")) {
				if (debug == 1)
					showLogMessage("I", "", "檔頭:" + rec);
			}
			if (rec.substring(0, 1).equals("D")) {
				errCode = "";
				totalInputFile++;
				moveData(processDataRecord(getFieldValue(rec, dataLENGTH), data));
				processDisplay(1000);
			}
			if (rec.substring(0, 1).equals("T")) {
				if (debug == 1)
					showLogMessage("I", "", "檔尾總筆數:" + Integer.parseInt(rec.substring(1, 9)));
				break;
			}
		}

		if (totalOutputFile > 0) {
			outPutTextFile();
			comc.writeReport(outFileName, lpar1, "MS950");
			hRecCnt1 = totalOutputFile;
			insertFileCtl(hNcccFilename);
			lpar1.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}

		closeInputText(fi);
		hRecCnt1 = totalInputFile;

		insertFileCtl(fileName);

		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
	private void moveData(Map<String, Object> map) throws Exception {
		String tmpChar = "";
		String col1 = "";
		String col2 = "";
		String col3 = "";
		String col4 = "";
		String col5 = "";
		String col6 = "";
//		String col7 = "";
		// col1檔頭標籤 col2資料屬性 col3悠遊卡外顯卡號 col4掛卡日期 col5掛卡時間 col6空白保留欄位 col7HASH值
		col1 = (String) map.get("col1");
		if (debug == 1)
			System.out.println("檔頭標籤=" + col1);

		col2 = (String) map.get("col2");
		if (debug == 1)
			System.out.println("資料屬性=" + col2);

		col3 = (String) map.get("col3");
		if (debug == 1)
			System.out.println("悠遊卡外顯卡號=" + col3);

		col4 = (String) map.get("col4");
		if (debug == 1)
			System.out.println("掛卡日期=" + col4);

		col5 = (String) map.get("col5");
		if (debug == 1)
			System.out.println("掛卡時間=" + col5);

		col6 = (String) map.get("col6");
		if (debug == 1)
			System.out.println("空白保留欄位=" + col6);

//		col7 = (String) map.get("col7");
//		if (debug == 1)
//			System.out.println("HASH值=" + col7);

		selectTscCardNo(col3);

		commitDataBase();
		return;
	}

	/***********************************************************************/
	int outPutTextFile() throws Exception {

//        sqlCmd  = "select file_name";
//        sqlCmd += " from crd_file_ctl  ";
//        sqlCmd += " where file_name = ?";
//        sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
//        setString(1, String.format("BKEC.00600000.ERR.%s%02d", file_date,Integer.parseInt(getFileName.substring(22))));
//        
//        if (selectTable() > 0) {
//            showLogMessage("I", "", "Output Filename = [" + h_nccc_filename + "]");
//        } 

		hNcccFilename = String.format("BKEC.00600000.ERR.%s%02d", fileDate,
				Integer.parseInt(getFileName.substring(22)));
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%s/media/icu/error/%s", comc.getECSHOME(), hNcccFilename);
		outFileName = Normalizer.normalize(outFileName, java.text.Normalizer.Form.NFKD);
		showLogMessage("I", "", "Output Filepath = [" + outFileName + "]");

		return 0;
	}

	/***********************************************************************/
	int checkFileCtl() throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
		sqlCmd += " and crt_date = to_char(sysdate,'yyyymmdd') ";
		setString(1, getFileName);
		int recordCnt = selectTable();

		if (recordCnt > 0)
			totalCount = getValueInt("totalCount");

		if (totalCount > 0) {
			showLogMessage("I", "", String.format("此檔案 = [" + getFileName + "]已處理不可重複處理(crd_file_ctl)"));
			return (1);
		}
		return (0);
	}

	/***********************************************************************/
	void createErrReport(String tscCardNo) throws Exception {
		ncccData1 = new Buf1();

		ncccData1.tscCardNo = tscCardNo;

		switch (errCode) {
		case "1":
			ncccData1.errReason = String.format("%-200s", "悠遊卡外顯卡號不存在");
			break;
		}

		ncccData1.date = sysDate;

		buf = ncccData1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}

	/***********************************************************************/
	void selectTscCardNo(String tscCardNo) throws Exception {
		int cnt = 0;
		sqlCmd = "select count(*) as cnt";
		sqlCmd += " from tsc_card ";
		sqlCmd += " where tsc_card_no = ? ";
		setString(1, tscCardNo);
		int recordCnt = selectTable();
		cnt = getValueInt("cnt");
		if (cnt > 0) {
			updateTscCard(tscCardNo);
		} else {
			sqlCmd = "select count(*) as cnt";
			sqlCmd += " from tsc_vd_card ";
			sqlCmd += " where tsc_card_no = ? ";
			setString(1, tscCardNo);
			recordCnt = selectTable();
			cnt = getValueInt("cnt");
			if (cnt > 0) {
				updateTscVdCard(tscCardNo);
			} else {
				if (debug == 1)
					showLogMessage("I", "", "Error:悠遊卡外顯卡號不存在");
				errCode = "1";
				createErrReport(tscCardNo);
				totalOutputFile++;
			}
		}

	}

	/***********************************************************************/
	void updateBlackltFlag() throws Exception {
		daoTable = "tsc_card";
		updateSQL = " blacklt_flag = 'N',";
        updateSQL += " mod_pgm = ? ,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where blacklt_flag = 'Y' ";
        setString(1, prgmId);
        updateTable();
        commitDataBase();
        
        daoTable = "tsc_vd_card";
        updateSQL = " blacklt_flag = 'N',";
        updateSQL += " blacklt_s_date = to_char(sysdate,'yyyymmdd') ,";
        updateSQL += " mod_pgm = ? ,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where blacklt_flag = 'Y' ";
        setString(1, prgmId);
		updateTable();
		commitDataBase();
	}

	/***********************************************************************/
	void updateTscCard(String tscCardNo) throws Exception {
		daoTable = "tsc_card";
		updateSQL = " blacklt_flag = 'Y',";
		updateSQL += " blacklt_s_date = to_char(sysdate,'yyyymmdd') ,";
        updateSQL += " mod_pgm = ?  ,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where tsc_card_no = ? ";
        setString(1,prgmId);
        setString(2, tscCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_card not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void updateTscVdCard(String tscCardNo) throws Exception {
		daoTable = "tsc_vd_card";
		updateSQL = " blacklt_flag = 'Y',";
		updateSQL += " mod_pgm = 'IcuD075',";
		updateSQL += " mod_time = sysdate";
		whereStr = "where tsc_card_no = ? ";
		setString(1, tscCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_tsc_vd_card not found!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertFileCtl(String filename) throws Exception {
		setValue("file_name", filename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", hRecCnt1);
		setValueInt("record_cnt", hRecCnt1);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			daoTable = "crd_file_ctl";
			updateSQL = "head_cnt = ?,";
			updateSQL += " record_cnt = ?,";
			updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
			whereStr = "where file_name = ? ";
			setInt(1, hRecCnt1);
			setInt(2, hRecCnt1);
			setString(3, filename);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_crd_file_ctl not found!", "", hCallBatchSeqno);
			}
		}
	}

	/***********************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + hNcccFilename + " 開始傳送....");
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + hNcccFilename);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + hNcccFilename + " 資料"+" errcode:"+err_code);
	          insertEcsNotifyLog(hNcccFilename);          
	      }
	  }
	
	/****************************************************************************/
	public int insertEcsNotifyLog(String fileName) throws Exception {
	      setValue("crt_date", sysDate);
	      setValue("crt_time", sysTime);
	      setValue("unit_code", comr.getObjectOwner("3", javaProgram));
	      setValue("obj_type", "3");
	      setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_name", "媒體檔名:" + fileName);
	      setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_desc2", "");
	      setValue("trans_seqno", commFTP.hEflgTransSeqno);
	      setValue("mod_time", sysDate + sysTime);
	      setValue("mod_pgm", javaProgram);
	      daoTable = "ecs_notify_log";

	      insertTable();

	      return (0);
	  }

	/****************************************************************************/
	void renameFile1(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/error/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		IcuD075 proc = new IcuD075();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String tscCardNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(tscCardNo, 20);
			rtn += fixLeft(errReason, 200);
			rtn += fixLeft(date, 8);
			return rtn;
		}

		String fixLeft(String str, int len) throws UnsupportedEncodingException {
			String spc = "";
			for (int i = 0; i < 100; i++)
				spc += " ";
			if (str == null)
				str = "";
			str = str + spc;
			byte[] bytes = str.getBytes("MS950");
			byte[] vResult = new byte[len];
			System.arraycopy(bytes, 0, vResult, 0, len);

			return new String(vResult, "MS950");
		}
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		ncccData1.tscCardNo = comc.subMS950String(bytes, 0, 20);
		ncccData1.errReason = comc.subMS950String(bytes, 20, 220);
		ncccData1.date = comc.subMS950String(bytes, 220, 228);
	}

	/****************************************************************************/
	String fixAllLeft(String str, int len) throws UnsupportedEncodingException {
		String spc = "";
		for (int i = 0; i < 100; i++)
			spc += "　";
		if (str == null)
			str = "";
		str = str + spc;
		byte[] bytes = str.getBytes("MS950");
		byte[] vResult = new byte[len];
		System.arraycopy(bytes, 0, vResult, 0, len);
		return new String(vResult, "MS950");
	}

	/****************************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/****************************************************************************/
	String[] getFieldValue(String rec, int[] parm) {
		int x = 0;
		int y = 0;
		byte[] bt = null;
		String[] ss = new String[parm.length];
		try {
			bt = rec.getBytes("MS950");
		} catch (Exception e) {
			showLogMessage("I", "", comc.getStackTraceString(e));
		}
		for (int i : parm) {
			try {
				ss[y] = new String(bt, x, i, "MS950");
			} catch (Exception e) {
				showLogMessage("I", "", comc.getStackTraceString(e));
			}
			y++;
			x = x + i;
		}
		return ss;
	}

	/****************************************************************************/
	private Map processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}

}
