/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/03/18  V1.00.00    Rou        program initial                          *
 *  109/06/24  V1.00.01    Pino       讀不到檔案不須停住                                                                                *
 *  109/07/04  V1.00.02    Zuwei      coding standard, rename field method & format                   *
 *  109/07/23  V1.00.03    shiyuqi    coding standard, rename field method & format    
 *  109/09/05  V1.00.06    yanghan    code scan issue    
 *  109/09/25  V1.00.07    Alex       收主機碼並轉碼處理                                                                           *
 *  109-10-08  V1.00.01  Zuwei       fix code scan issue                        *
 *  109-10-19  V1.00.09    shiyuqi       updated for project coding standard    *
 *  111-08-09  V1.00.10    Alex       取消主機碼									*
 *  111-09-27  V1.00.11    Alex       改為固定檔名 FDHLD.DAT               	*
 ******************************************************************************/

package Ptr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

import Dxc.Util.SecurityUtil;

/*每天接收合庫主機系統的例假日檔案*/
public class PtrA010 extends AccessDAO {

	private final String progname = "每天接收合庫主機系統的例假日檔案  111/09/27 V1.00.11";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommString commstr = new CommString(); 
	bank.Auth.HpeUtil hpeUtil = new bank.Auth.HpeUtil();
	String prgmId = "PtrA010";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String hModUser = "";
	String hCallBatchSeqno = "";
	String iFileName = "";
	String hTempUser = "";
	String hCallErrorDesc = "";
	int ret = 0;
	int totCnt = 0;
	int totalReadC = 0;
	String getFileName;
	String txtFile;
	String tmpDate;
	String tDate;
	String getYear;
	int temp;
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	String[] pDate = new String[] {};
	int procCnt = 0; //--資料處理筆數
	String procYear = "";
	protected final String dt1Str = "col1, year, number, business_date";

	protected final int[] dt1Length = { 2, 3, 1, 186 };

	protected String[] dt1 = new String[] {};

	public int mainProcess(String[] args) {

		try {
			dt1 = dt1Str.split(",");
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 1) {
				comc.errExit("Usage : PtrA010 batch_seq", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			hTempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);
			if (hTempUser.length() == 0) {
				hModUser = comc.commGetUserID();
				hTempUser = hModUser;
			}
			if (selectFile() == 1 || totCnt > 2)
				comcr.hCallErrorDesc = "程式執行結束";
			else
				comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatchEnd();
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	// =============================================================================
	int selectFile() throws Exception {
		String filepath = comc.getECSHOME() + "/media/ptr/";
		File checkFilePath = new File(SecurityUtil.verifyPath(filepath));

		if (!checkFilePath.isDirectory())
			comcr.errRtn(String.format("[%s]目錄不存在", filepath), "", hCallBatchSeqno);

		filepath = Normalizer.normalize(filepath, java.text.Normalizer.Form.NFKD);
		List<String> listOfFiles = comc.listFS(filepath, "", "");

		if (listOfFiles.size() == 0) {
			showLogMessage("I", "", String.format("[%s]無檔案可處理!!", filepath));
			return 0;
		}
		
		//-改為固定檔名
		
		getFileName = "FDHLD.DAT";
		txtFile = filepath + getFileName;
		txtFile = Normalizer.normalize(txtFile, java.text.Normalizer.Form.NFKD);
		
		if (readFile(getFileName) == 1)
			return 1;
		
		return 0;
	}

	/***********************************************************************/
	int checkBusinessYear() throws Exception {
		sqlCmd = " select business_date, ";
		sqlCmd += " substr(business_date, 1, 4) as business_year";
		sqlCmd += " from ptr_businday ";
		int recordCnt = selectTable();
		int yearInt = 0 ;
		String yearStr = "";
		yearInt = 2011 +Integer.parseInt(getFileName.substring(6, 8));
		yearStr = commstr.int2Str(yearInt).substring(2,4);		
		for (int i = 0; i <= recordCnt; i++) {
			String hBusinessYear = getValue("business_year", i);
			getYear = "20"+yearStr;			
			if (getYear.equals(hBusinessYear) || String.valueOf(Integer.parseInt(getYear) - 1).equals(hBusinessYear))
				return 0;
		}
		return 1;
	}

	/***********************************************************************/
	void deletePtrHoliday() throws Exception {
		//--移除資料庫內假日資料 , 僅執行第一筆時執行
		daoTable = "ptr_holiday ";
		whereStr = "WHERE holiday >= ? and holiday <= ? ";

		setString(1, procYear + "0000");
		setString(2, procYear + "9999");
		deleteTable();

		if (dupRecord.equals("Y")) {
			comcr.errRtn("delete_ptr_holiday duplicate!", "", hCallBatchSeqno);
		}

		return;
	}

	/***********************************************************************/
//	int readFile(String fileName) throws Exception {		
//		temp = 1;
//		// fix issue "Unreleased Resource: Streams" 2020/10/08 Zuwei
//		try (InputStream inputstream = new FileInputStream(txtFile);) {
//			byte[] data = new byte[1024];
//			inputstream.read(data);
//			String totalFileData = "", fileDataPart1 = "", fileDataPart2 = "" ;
////			totalFileData = hpeUtil.ebcdic2Str(data);
//			totalFileData = data.toString();
//			fileDataPart1 = commstr.bbMid(totalFileData, 0,192);
//			fileDataPart2 = commstr.bbMid(totalFileData, 192,192);
//			int tmp = moveData(processDataRecord(getFieldValue(fileDataPart1, dt1Length, fileDataPart1.length()), dt1));
//			if (tmp == 1)	return 1;
//			temp ++;
//			//--
//			tmp = moveData(processDataRecord(getFieldValue(fileDataPart2, dt1Length, fileDataPart1.length()), dt1));
//			temp ++;
//			inputstream.close();
//			renameFile(fileName);
//		}
//		return 0;
//	}

	/***********************************************************************/
  int readFile(String fileName) throws Exception {

    int fi = openInputText(txtFile);
    temp = 1;

    while (true) {
      String rec = readTextFile(fi); // read file data
      if (endFile[fi].equals("Y"))
        break;

      if (rec.length() == 192) {
        totCnt++;
        if (totCnt > 2) {
          showLogMessage("D", "", "ERROR : 此檔案 [ " + getFileName + " ] 筆數不正確，讀取失敗 ");
          return 1;
        }
        byte[] bt = rec.getBytes("big5");
        int tmp = moveData(processDataRecord(getFieldValue(rec, dt1Length, rec.length()), dt1));
        if (tmp == 1)
          return 1;
        temp++;
      }

      else {
        showLogMessage("D", "", "ERROR : 此檔案 [ " + getFileName + " ] 資料長度不正確，讀取失敗 ");
        return 1;
      }
      totalReadC++;
      processDisplay(1000);
    }
    closeInputText(fi);
    renameFile(fileName);
    return 0;
  }

	/***********************************************************************/
	int moveData(Map<String, Object> map) throws Exception {

		String tmpChar;
		String tmpYear;
		String fileYear;
		int tmpInt;
		
//		int yearInt = 0 ;
//		String yearStr = "";
//		yearInt = 2011 +Integer.parseInt(getFileName.substring(6, 8));
//		yearStr = commstr.int2Str(yearInt).substring(2,4);		
		
//		fileYear = "20"+yearStr;
		tmpChar = (String) map.get("year");
		tmpInt = Integer.parseInt(tmpChar) + 1911;
		tmpYear = String.valueOf(tmpInt);		
//		if (!tmpYear.equals(fileYear)) {
//			showLogMessage("D", "", "ERROR : 檔案  [ " + getFileName + "] 檔名年份與資料內容年份不同，讀取失敗 ");
//			return 1;
//		}
		procYear = tmpYear ;
		tmpChar = (String) map.get("number");
		switch (temp) {
		case 1:
			if (!tmpChar.equals("1")) {
				showLogMessage("D", "", "ERROR : 檔案  [ " + getFileName + "] 第一筆資料之序號不為1，讀取失敗 ");
				return 1;
			}
			break;
		case 2:
			if (!tmpChar.equals("2")) {
				showLogMessage("D", "", "ERROR : 檔案  [ " + getFileName + "] 第二筆資料之序號不為2，讀取失敗 ");
				return 1;
			}
			break;
		}

		tmpDate = (String) map.get("business_date");
		tmpDate = tmpDate.trim();

		if ((tmpInt % 4 == 0 && tmpInt % 100 != 0) || tmpInt % 400 == 0) { // leap year
			if (tmpChar.equals("1")) {
				showLogMessage("I", "", "-- 此檔案  [ " + getFileName + "] 為閏年 --");
				Date dBegin = dateFormat.parse((String.format("%s0101", String.valueOf(tmpInt))));
				Date dEnd = dateFormat.parse((String.format("%s0628", String.valueOf(tmpInt))));
				checkHoliday(dBegin, dEnd);
			} else {
				Date dBegin = dateFormat.parse((String.format("%s0629", String.valueOf(tmpInt))));
				Date dEnd = dateFormat.parse((String.format("%s1231", String.valueOf(tmpInt))));
				checkHoliday(dBegin, dEnd);
			}
		} else {
			if (tmpChar.equals("1")) {
				showLogMessage("I", "", "-- 此檔案  [ " + getFileName + "] 非閏年 --");
				Date dBegin = dateFormat.parse((String.format("%s0101", String.valueOf(tmpInt))));
				Date dEnd = dateFormat.parse((String.format("%s0629", String.valueOf(tmpInt))));
				checkHoliday(dBegin, dEnd);
			} else {
				Date dBegin = dateFormat.parse((String.format("%s0630", String.valueOf(tmpInt))));
				Date dEnd = dateFormat.parse((String.format("%s1231", String.valueOf(tmpInt))));
				checkHoliday(dBegin, dEnd);
			}
		}

		return 0;
	}

	/**
	 * @throws Exception
	 *********************************************************************/
	void checkHoliday(Date dBegin, Date dEnd) throws Exception {

		String[] setDate = new String[186];
		String dateAll = "";
		setDate[0] = (dateFormat.format(dBegin));
		Calendar calBegin = Calendar.getInstance();
		calBegin.setTime(dBegin);
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTime(dEnd);
		dateAll += setDate[0];
		int i = 1, j = 1;
		while (dEnd.after(calBegin.getTime())) {
			calBegin.add(Calendar.DAY_OF_MONTH, 1);
			setDate[i] = dateFormat.format(calBegin.getTime());
			dateAll += "," + setDate[i];
			i++;
		}
		pDate = dateAll.split(",");

		for (i = 0, j = 0; i < tmpDate.length(); i++, j++) {
			String temp = tmpDate.substring(i, j + 1);
			tDate = "";
			if (temp.equals("N")) {
				tDate = pDate[i];
				insertPtrHoliday();
			}
		}
		return;
	}

	/***********************************************************************/
	void insertPtrHoliday() throws Exception {
		//--因假日檔為整年全檔 , 執行第一筆時將資料庫內假日資料移除 
		if(procCnt == 0)
			deletePtrHoliday();
		procCnt ++;
		setValue("holiday", tDate);
		setValue("crt_date", sysDate);
		setValue("crt_user", "ecs");
		setValue("mod_user", "ecs");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ptr_holiday";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_ptr_holiday duplicate!", "", hCallBatchSeqno);
		}

	}

	// ************************************************************************
	public void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/ptr/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/ptr/backup/" + removeFileName + "." + sysDate;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	// ************************************************************************
	private Map processDataRecord(String[] row, String[] DT) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		int j = 0;
		for (String s : DT) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;

	}

	/************************************************************************/
	public String[] getFieldValue(String rec, int[] parm, int length) {
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

	/***********************************************************************/
	public static void main(String[] args) throws Exception {

		PtrA010 proc = new PtrA010();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
