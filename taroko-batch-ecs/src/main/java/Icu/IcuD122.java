/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------- *
*  2021/01/25  V1.01.01  Eric         Initial                                 *
*  110/02/04   V1.01.02  Wilson       讀檔路徑修改、檔名不用判斷日期                                                 *
*  111/02/14   V1.01.03    Ryan      big5 to MS950                                           *
****************************************************************************/

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
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommString;

/*CARDLINK信用卡客戶資料處理程式*/
public class IcuD122 extends AccessDAO {
private String progname = "接收電子票證聯名卡自動加值功能開啟通知資料檔 111/02/14 V1.01.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommString zzStr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	int debug = 1;
	int datacnt = 0;

	String prgmId = "IcuD122";
	
//	String fileFolderPath = comc.getECSHOME() + "/media/icu/";
	String fileFolderPath = "/crdataupload/";
	
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	BufferedWriter nccc = null;
	Buf1 ncccData = new Buf1();
	protected final String dT1Str = "card_no,out_card_no,open_date,open_time,open_src,open_man,rcode";

	protected final int[] dt1Length = { 19,19,8,6,1,20,2};

	int rptSeq1 = 0;
	String buf = "";
    String queryDate = "";
	String hModUser = "";
	String hCallBatchSeqno = "";
	String hNcccFilename = "";
	int hRecCnt1 = 0;
	int seq = 0;

	String getFileName;
	String outFileName;
	int totalInputFile;
	int totalOutputFile;

	String tmpCardNo;
	String tmpOutCardNo;
	String tmpOpenDate;
	String tmpOpenTime;
	String tmpOpenSrc;
	String tmpOpenMan;
    String tmpRcode;
    
    String firstnum;
    String hBusiBusinessDate = "";
    String errCode;

	protected String[] dT1 = new String[] {};
	Buf1 ncccData1 = new Buf1();

	public int mainProcess(String[] args) {

		try {
			dT1 = dT1Str.split(",");
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
			comsecr = new CommSecr(getDBconnect(), getDBalias());

			hModUser = comc.commGetUserID();
			
            selectPtrBusinday();
			
            // 若沒有給定查詢日期，則查詢日期為系統日
//            if(args.length == 0) {
//                queryDate = hBusiBusinessDate;
//            }else
//            if(args.length == 1) {
//               if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
//                    showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
//                    return -1;
//                }
//                queryDate = args[0];
//            }else {
//                comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
//            }                       
			
			openFile();

			// ==============================================
			// 固定要做的
            showLogMessage("I", "", "執行結束,[ 總筆數 : "+ totalInputFile +"],[ 錯誤筆數 : "+ totalOutputFile +"]");
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


		List<String> listOfFiles = comc.listFS(fileFolderPath, "", "");

//				final String fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", "F00600000", "ICECOQND",
//				new CommDate().getLastTwoTWDate(queryDate), queryDate.substring(4, 8)); // 檔案正規表達式		
		
//		final String fileNameTemplate = String.format("%s\\.%s\\..*", "F00600000", "ICECOQND"); // 檔案正規表達式		
		
		if (listOfFiles.size() > 0)
		for (String file : listOfFiles) {
			getFileName = file;
			
			if (getFileName.length() != 27)
				continue;
			
			if(!getFileName.substring(0, 19).equals("F00600000.ICECOQND."))  
			{
//				System.out.println(file+" NOT MAP "+fileNameTemplate);
				continue;
			}
			if (checkFileCtl() != 0)
				continue;
			fileCount++;
			readFile(getFileName);
		}
		if (fileCount < 1) {
			showLogMessage("I", "", "無檔案可處理");
			
//			comcr.hCallErrorDesc = "無檔案可處理";
//            comcr.errRtn("無檔案可處理","處理日期 = " + queryDate  , comcr.hCallBatchSeqno);
		}
		return (0);
	}

	/**********************************************************************/
	int readFile(String fileName) throws Exception {
		String rec = "";
		String fileName2;
		int fi;
		fileName2 = fileFolderPath + fileName;

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

		showLogMessage("I", "", " Process file path =[" + fileFolderPath + " ]");
		showLogMessage("I", "", " Process file =[" + fileName + "]");
        
		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;

			totalInputFile++;
			
			moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			processDisplay(1000);
			datacnt++;
		}

		if (totalOutputFile > 0) {
			outPutTextFile();
			comc.writeReport(outFileName, lpar1, "MS950");
			insertFileCtl();
			lpar1.clear();
			
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
		    procFTP();
		    renameFile1(hNcccFilename);
		}

		closeInputText(fi);

		insertFileCtl1(fileName);

		renameFile(fileName);

		return 0;
	}

	/***********************************************************************/
    private void moveData(Map<String, Object> map) throws Exception {       
        String tmpChar1 = "";       
       
        tmpCardNo = (String) map.get("card_no"); //信用卡卡片號碼
        tmpCardNo = tmpCardNo.trim();
        tmpOutCardNo = (String) map.get("out_card_no"); //電子票證聯名卡外顯卡號
        tmpOutCardNo = tmpOutCardNo.trim();   
        tmpOpenDate = (String) map.get("open_date"); //開啟日期
        tmpOpenDate = tmpOpenDate.trim();
        
        tmpOpenTime = (String) map.get("open_time"); //開啟時間
        tmpOpenTime = tmpOpenTime.trim();
        tmpOpenSrc =  (String) map.get("open_src"); //開啟來源
        tmpOpenMan = (String) map.get("open_man"); //開啟人員
        tmpRcode =  (String) map.get("rcode"); //回覆碼
        
        
        
        firstnum = tmpOutCardNo.substring(0, 1); //外顯卡號第一碼
        
        int checknum;
        String friststr = firstnum;
        String otcardno   =tmpOutCardNo;
        
        if (firstnum.equals("8")) {
        	int chk = getDataCnt(otcardno,friststr);
        	if(chk == 1)
        	{
        		updateTscCard();
        	}else {
                showLogMessage("I", "", "ICH_CARD_NO = [" + tmpOutCardNo + "]，Error : 外顯卡號不存在悠遊卡主檔");
                errCode = "1";
                createErrReport();     
                totalOutputFile ++;         		
        	}

        }else if (firstnum.equals("7")){
        	int chk = getDataCnt(otcardno,friststr);
        	if(chk == 1)
        	{
        		updateTscVDCard();
        	}else {
                showLogMessage("I", "", "ICH_CARD_NO = [" + tmpOutCardNo + "]，Error : 外顯卡號不存在悠遊VD卡主檔");
                errCode = "2";
                createErrReport();     
                totalOutputFile ++;         		
        	}       	
        }else if ( firstnum.equals("0")) {
        	int chk = getDataCnt(otcardno,friststr);
        	if(chk == 1)
        	{
        		updateIpsCard();
        	}else {
                showLogMessage("I", "", "ICH_CARD_NO = [" + tmpOutCardNo + "]，Error : 外顯卡號不存在一卡通主檔");
                errCode = "3";
                createErrReport();     
                totalOutputFile ++;         		
        	}       	
        }else if ( firstnum.equals("6")) {
        	int chk = getDataCnt(otcardno,friststr);
        	if(chk == 1)
        	{
        		updateIchCard();
        	}else {
                showLogMessage("I", "", "ICH_CARD_NO = [" + tmpOutCardNo + "]，Error : 外顯卡號不存在愛金卡主檔");
                errCode = "4";
                createErrReport();     
                totalOutputFile ++;           		
        	}       	
        	
        }else {
        	
            showLogMessage("I", "", "ICH_CARD_NO = [" + tmpOutCardNo + "]，Error : 外顯卡號不為票證卡號");
            errCode = "5";
            createErrReport();     
            totalOutputFile ++;       	
        	
        }
       
     
     
        return;
    }

	/***********************************************************************/
	int outPutTextFile() throws Exception {
		int fileNo = 0;

        sqlCmd  = "select max(substr(file_name, 22, 2)) file_no";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += " where file_name like ?";
        sqlCmd += "  and crt_date  = to_char(sysdate,'yyyymmdd') ";
        setString(1, "ICECOQND.ERR." + "%" + ".TXT");

		if (selectTable() > 0)
			fileNo = getValueInt("file_no");

		hNcccFilename = String.format("ICECOQND.ERR.%s%02d.TXT", sysDate, fileNo + 1);
		showLogMessage("I", "", "Output Filename = [" + hNcccFilename + "]");

		outFileName = String.format("%s%s", fileFolderPath, hNcccFilename);
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
//      sqlCmd += " and crt_date = to_char(?,'yyyymmdd') ";
      setString(1, getFileName);
//      setString(2, queryDate1);
		int recordCnt = selectTable();

		if (recordCnt > 0)
			totalCount = getValueInt("totalCount");

		if (totalCount > 0) {
            showLogMessage("I", "", String.format("此檔案 = [" + getFileName + "]已處理過不可重複處理(crd_file_ctl)"));
			return (1);
		}
		return (0);
	}

	/***********************************************************************/
	void createErrReport() throws Exception {

		ncccData1 = new Buf1();
		
		seq++;
		ncccData1.cardNo = tmpCardNo;
		ncccData1.outCardNo = tmpOutCardNo;

		switch (errCode) {
		case "1":
			ncccData1.errReason = String.format("%-200s", "外顯卡號不存在悠遊卡主檔"); //8 but can not find update data
			break;
		case "2":
			ncccData1.errReason = String.format("%-200s", "外顯卡號不存在悠遊VD卡主檔"); //7 but can not find update data
			break;
		case "3":
			ncccData1.errReason = String.format("%-200s", "外顯卡號不存在一卡通主檔"); //0 but can not find update data
			break;
		case "4":
			ncccData1.errReason = String.format("%-200s", "外顯卡號不存在愛金卡主檔"); //6 but can not find update data
			break;
		case "5":  
		    ncccData1.errReason = String.format("%-200s", "外顯卡號不為票證卡號"); // firstnumber not in (8,7,0,6)
			break;
			
		}

		ncccData1.date = sysDate;

		buf = ncccData1.allText();
//        lpar1.clear();
		lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", buf));
		return;
	}



	//***********************************************************************/
	public int getDataCnt(String outcardno,String firststr) throws Exception {
		
		
		if(firststr.equals("8"))
		{
			sqlCmd = "select count(*) cnt ";
			sqlCmd += " from tsc_card ";
			sqlCmd += " where tsc_card_no = ? ";
			setString(1, outcardno);
		}
		
		
		if(firststr.equals("7"))
		{
			sqlCmd = "select count(*) cnt ";
			sqlCmd += " from TSC_VD_CARD ";
			sqlCmd += " where TSC_CARD_NO = ? ";
			setString(1, outcardno);
		}
		
		if(firststr.equals("0"))
		{
			sqlCmd = "select count(*) cnt ";
			sqlCmd += " from IPS_CARD ";
			sqlCmd += " where IPS_CARD_NO = ? ";
			setString(1, outcardno);
		}	
		
		if(firststr.equals("6"))
		{
			sqlCmd = "select count(*) cnt ";
			sqlCmd += " from ICH_CARD ";
			sqlCmd += " where ICH_CARD_NO = ? ";
			setString(1, outcardno);
		}	
		
		int totalCount = 0;


		int recordCnt = selectTable();

		totalCount = getValueInt("cnt");

		if (totalCount == 0) {
            showLogMessage("I", "", String.format("["+outcardno+"]外顯卡號不存在於主檔"));
			return 0;
		}
		return 1;

	}


	
	
	//========8 code======
	public void updateTscCard() throws Exception {
		daoTable = "TSC_CARD";
		updateSQL = " AUTOLOAD_DATE = ? ,";
		updateSQL += " AUTOLOAD_FLAG = 'Y',";
		updateSQL += " MOD_USER = 'IcuD122',";
		updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " MOD_PGM = 'IcuD122' ";
		whereStr = " where TSC_CARD_NO = ? "; 
		
        setString(1, tmpOpenDate);
        setString(2, sysDate + sysTime);
        setString(3, tmpOutCardNo);
        updateTable();
        
        return;
	}
	
	// 7 code======
	public void updateTscVDCard() throws Exception {
		daoTable = "TSC_VD_CARD";
		updateSQL = " AUTOLOAD_DATE = ? ,";
		updateSQL += " AUTOLOAD_FLAG = 'Y',";
		updateSQL += " MOD_USER = 'IcuD122',";
		updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " MOD_PGM = 'IcuD122' ";
		whereStr = " where TSC_CARD_NO = ? "; 
		
        setString(1, tmpOpenDate);
        setString(2, sysDate + sysTime);
        setString(3, tmpOutCardNo);
        updateTable();
        
        return;		
		
	}

	
	// 0 code======
	public void updateIpsCard() throws Exception {
		daoTable = "IPS_CARD";
		updateSQL = " AUTOLOAD_DATE = ? ,";
		updateSQL += " AUTOLOAD_FLAG = 'Y',";
		updateSQL += " MOD_USER = 'IcuD122',";
		updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " MOD_PGM = 'IcuD122' ";
		whereStr = " where IPS_CARD_NO = ? "; 
		
        setString(1, tmpOpenDate);
        setString(2, sysDate + sysTime);
        setString(3, tmpOutCardNo);
        updateTable();
        
        return;		
		
	}
	
	// 6 code======
	public void updateIchCard() throws Exception {
		daoTable = "ICH_CARD";
		updateSQL = " AUTOLOAD_DATE = ? ,";
		updateSQL += " AUTOLOAD_FLAG = 'Y',";
		updateSQL += " MOD_USER = 'IcuD122',";
		updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " MOD_PGM = 'IcuD122' ";
		whereStr = " where ICH_CARD_NO = ? "; 
		
        setString(1, tmpOpenDate);
        setString(2, sysDate + sysTime);
        setString(3, tmpOutCardNo);
        updateTable();
        
        return;		
		
	}		

	/***********************************************************************/
	void insertFileCtl1(String fileName) throws Exception {

		setValue("file_name", fileName);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);
		setValue("record_cnt",String.valueOf(datacnt));
		daoTable = "crd_file_ctl";
		insertTable();
	}

	/***********************************************************************/
	void insertFileCtl() throws Exception {
		setValue("file_name", hNcccFilename);
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
			setString(3, hNcccFilename);
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
	      commFTP.hEriaLocalDir = String.format("/crdataupload");
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (必要) */
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
		String tmpstr1 = "/crdataupload/" + removeFileName;
		String tmpstr2 = "/crdataupload/backup/" + removeFileName + "." + sysDate;
		
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	/****************************************************************************/	
	public static void main(String[] args) throws Exception {
		
		IcuD122 proc = new IcuD122();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class Buf1 {
		String cardNo;
		String outCardNo;
		String errReason;
		String date;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(cardNo, 19);
			rtn += fixLeft(outCardNo, 19);
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
		ncccData1.cardNo = comc.subMS950String(bytes, 0, 19);
		ncccData1.outCardNo = comc.subMS950String(bytes, 19, 38);
		ncccData1.errReason = comc.subMS950String(bytes, 38, 238);
		ncccData1.date = comc.subMS950String(bytes, 238, 246);
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
		String tmpstr1 = fileFolderPath + removeFileName;
		String tmpstr2 = fileFolderPath +"backup/" + removeFileName + "." + sysDate;

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
	private Map<String,Object> processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}
}
