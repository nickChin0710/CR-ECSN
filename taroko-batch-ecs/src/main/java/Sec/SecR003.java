package Sec;
/**
 * 2019-1101   JH    pageCont
 * 2019-0813   JH    initial
 * 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
 * 109-07-17   V1.00.01  Zuwei       “兆豐國際商銀”改為”合作金庫銀行”     *
 * 110-01-07   V1.00.02  shiyuqi       修改无意义命名                                                                           *
 * 110-01-19   V1.00.03  Justin          change to generate a log file containing the text [PDPA]  
 * 110-02-01   V1.00.04  Justin          update new logic
 * */

import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SecR003 extends com.BaseBatch {
//  private static final int MAX_LINE_PER_PAGE = 34;
  private static final String KEY_WORD = "[PDPA]";
  private static final String LOG_FILE_NAME = "Taroko_Log4j.log";
  private static final String OUTPUT_FILE_NAME = "PDPA"; //PERSONAL DATA PROTECTION ACT
  private static final String FILE_FOLDER_PATH = "/media/sec";
  private final String PROGNAME = "個資查詢交易記錄報表  V.2021-0201";
  private final String JAVA_NAME = "SecR003";
  // -----------------------------------------------------------------------------
//  private final String REPORT_ID = "SecR003";
//  private final String REPORT_NAME = "個資查詢交易記錄報表";
  // -----------------------------------------------------------------------------
  CommFTP commFTP = null;
  CommRoutine comr = null;
  //-----------------------------------------------------------------------------

  public static void main(String[] args) {
    SecR003 proc = new SecR003();
    int result = proc.mainProcess(args);
    proc.systemExit(result);
  }

  @Override
public int mainProcess(String[] args) {
		try {
			String queryDate = "";
		    dspProgram(PROGNAME);

		    int argsLength = args.length;
		    
		    if (argsLength > 1) {
		    	printf("請勿輸入超過一個參數。");
			    errExit(1);
			}
		    
		    if (argsLength == 0) {
		    	printf("未輸入任何參數，因此以系統前一天作為查詢日期");
		    	CommDate commDate = new CommDate();
		    	// 取得系統日前一天日期
		    	queryDate = commDate.dateAdd(commDate.sysDate(), 0, 0, -1); 
			}else {
				queryDate = args[0];
			    if (new CommDate().isDate(queryDate) == false) {
			    	printf("輸入日期不符合西元年日期格式");
			        errExit(1);
				}
			}
		    
		    if (!connectDataBase()) {
				showLogMessage("E", "", "connect DataBase error");
				errExit(1);
			}
		    
		    // 20210131 -> 2021-01-31
		    queryDate = convertDateFormat(queryDate);
	    
		    printf(String.format("搜尋log檔日期為%s的檔案", queryDate));
		    
		    ArrayList<String> allLogFileNames = getAllLogFileName();
		    
		    ArrayList<String> logFileNames = filterLogFileName(allLogFileNames, queryDate);

		    ArrayList<String> outputFileArr = extractPDPA(logFileNames, queryDate);		    
		    
		    procFTP(outputFileArr);
		    
		    deleteFiles(allLogFileNames, outputFileArr);

		    sqlCommit(1);
		    endProgram();
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
		return 0;
  }

	private void deleteFiles(ArrayList<String> logFileNames, ArrayList<String> outputFileArr) {
		CommCrd comc = new CommCrd();
		
		int size = logFileNames.size();
		for (int i = 0; i < size; i++) {
			delteFile(logFileNames.get(i), comc);
		}
		
		size = outputFileArr.size();
		for (int i = 0; i < size; i++) {
			delteFile(outputFileArr.get(i), comc);
		}

	}

	private void delteFile(String logFileName, CommCrd comc) {	
		StringBuffer sb = new StringBuffer().append(getLogFileFolderPath()).append("/").append(logFileName);
		String fileLogFilePath = SecurityUtil.verifyPath(sb.toString());
		boolean isDelete = comc.fileDelete(fileLogFilePath);
		if (isDelete) {
			showLogMessage("I", "", String.format("檔案刪除：%s", fileLogFilePath));
		}else {
			showLogMessage("E", "", String.format("檔案無法刪除：%s", fileLogFilePath));
		}
	}

	private ArrayList<String> filterLogFileName(ArrayList<String> allFileNameArrList, String queryDate) {
		ArrayList<String> fileNames = new ArrayList<String>();
		// server1 log
		String server1FileName = getServerLogName(1, allFileNameArrList, queryDate);
		fileNames.add(server1FileName);
		// server2 log
		String server2FileName = getServerLogName(2, allFileNameArrList, queryDate);
		fileNames.add(server2FileName);
		// server3 log
		String server3FileName = getServerLogName(3, allFileNameArrList, queryDate);
		fileNames.add(server3FileName);
		
		return fileNames;

	}
	
	private String getServerLogName(int serverNo, ArrayList<String> fileNameArrList, String queryDate) {
		String serverLogName = "";
		int size = fileNameArrList.size();
		
		for (int i = 0; i < size; i++) {
			String tmpFileName = fileNameArrList.get(i);
			if (tmpFileName.matches(String.format("server%s.*", serverNo))) {
				
				if (tmpFileName.equals(String.format("server%s_%s.%s", serverNo, LOG_FILE_NAME, queryDate))) {
					serverLogName = tmpFileName;
					break;
				}
				
				if (tmpFileName.equals(String.format("server%s_%s", serverNo, LOG_FILE_NAME))) {
					File file = new File(String.format("server%s_%s", serverNo, LOG_FILE_NAME));
					String modifiedDateStr = convertDateFormat(new Date(file.lastModified()));
					if (queryDate.equals(modifiedDateStr)) {
						serverLogName = tmpFileName;
						break;
					}
				}	
				
			}
		}
		
		return serverLogName;
	}

	private ArrayList<String> extractPDPA(ArrayList<String> logFileNames, String queryDate) throws Exception {
		ArrayList<String> outputFileArr = new ArrayList<String>();
		int size = logFileNames.size();
		for (int i = 0; i < size; i++) {
			String logFileName = logFileNames.get(i);

			String serverNo = getServerNo(logFileName);
			if (empty(serverNo))
				continue;

			int fileIndex = openLogFile(logFileName);
			if (fileIndex == -1) {
				continue;
			}

			List<String> reportList = processData(fileIndex);
			closeInputText(fileIndex);
			if (reportList.size() > 0) {
				writeOutputFile(reportList, serverNo, queryDate);
				outputFileArr.add(getOutFileName(serverNo, queryDate));
			}

		}
		return outputFileArr;
	}

private String getServerNo(String logFileName) {
	if (logFileName.matches("server._.*")) {
		return Character.toString(logFileName.charAt(6));
	}else {
		showLogMessage("E", "", "無法判斷檔案來源的Server編號");
		return null;
	}
}

private ArrayList<String> getAllLogFileName() {
	ArrayList<String> fileNameArrList = new ArrayList<String>();
	
	// server1_Taroko_Log4j.log, server2_Taroko_Log4j.log, server2_Taroko_Log4j.log.2021-02-01, ......
	String fileNameReg = String.format("server._%s.*", LOG_FILE_NAME);
	
	String logFileFolderPath = getLogFileFolderPath();
    log(String.format("檢查資料夾路徑: %s", logFileFolderPath));
    
	File file = new File(logFileFolderPath);
	if (file.isDirectory()) {
		File[] fileArr = file.listFiles();
		log("開始篩選相關檔案");
		for (int i = 0; i < fileArr.length; i++) {
			if (fileArr[i].getName().matches(fileNameReg)) {
				fileNameArrList.add(fileArr[i].getName());
				log(String.format("檔名：%s", fileArr[i].getName()));
			}
		}
		log("結束篩選相關檔案");
	}
    
	return fileNameArrList;
}

private String getLogFileFolderPath() {
	StringBuffer sb = new StringBuffer();
	sb.append(getEcsHome()).append(FILE_FOLDER_PATH);
	String logFileFolderPath = SecurityUtil.verifyPath(sb.toString());
	return logFileFolderPath;
}

private String convertDateFormat(String queryDate) throws ParseException {
	SimpleDateFormat srcSDF = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat targetSDF = new SimpleDateFormat("yyyy-MM-dd");
	queryDate = targetSDF.format(srcSDF.parse(queryDate));
	return queryDate;
}

private String convertDateFormat(Date queryDate){
	SimpleDateFormat targetSDF = new SimpleDateFormat("yyyy-MM-dd");
	String queryDateStr = targetSDF.format(queryDate);
	return queryDateStr;
}

	private void writeOutputFile(List<String> reportList, String serverNo, String queryDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		
		// /cr/EcsWeb/media/sec/server1_Taroko_Log4j.log.2021-01-19
		sb.append(getLogFileFolderPath())
			.append("/")
			.append(getOutFileName(serverNo, queryDate));
		
		String outputFilePath = SecurityUtil.verifyPath(sb.toString());
		
		int outFileIndex = openOutputText(outputFilePath);

		writeFile(reportList, outFileIndex);

		showLogMessage("I", "", String.format("產出個資查詢交易記錄報表: %s", outputFilePath));

		closeOutputText(outFileIndex);
	}
	
	/****************************************************************************/

	private void writeFile(List<String> reportList, int outFileIndex) throws Exception {
		int size = reportList.size();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; i++) {
			sb.append(reportList.get(i));
			sb.append(System.lineSeparator());
		}
		writeTextFile(outFileIndex, sb.toString());
		
	}

	int openLogFile(String logFileName) throws Exception {
		StringBuffer sb = new StringBuffer().append(getLogFileFolderPath()).append("/").append(logFileName);
		String fileLogFilePath = SecurityUtil.verifyPath(sb.toString());
		int fileIndex = openInputText(fileLogFilePath); 
		if (fileIndex < 0) {
			showLogMessage("E", "", String.format("沒有權限讀寫資料(%s)", fileLogFilePath));
			closeOutputText(fileIndex);
			return -1;
		}
		return fileIndex;

	}

private String getLogFilePath(String queryDate) {
	// -Log file-
    // /cr/EcsWeb/media/sec/Taroko_Log4j.log.2019-08-13
	StringBuffer sb = new StringBuffer();
	sb.append(getEcsHome()).append(FILE_FOLDER_PATH)
	    .append("/")
	    .append(LOG_FILE_NAME)
	    .append(".")
		.append(commString.left(queryDate, 4))
		.append("-")
		.append(commString.mid(queryDate, 4, 2))
		.append("-")
		.append(commString.mid(queryDate, 6, 2));
    String logFilePath = SecurityUtil.verifyPath(sb.toString());
	return logFilePath;
}

  List<String> processData(int fileIndex) throws Exception {
		List<String> reportList = new ArrayList<String>();
		while (true) {

			String rowStr = readTextFile(fileIndex);

			if (eq(endFile[fileIndex], "Y"))
				break;

			if (rowStr.indexOf(KEY_WORD) == -1)
				continue;

			totalCnt++;

			reportList.add(rowStr);
		}
		return reportList;
  }

  // --------------------------------------------------------------------------


@Override
protected void dataProcess(String[] args) throws Exception {
	// TODO Auto-generated method stub
	
}

private String getOutFileName(String serverNo, String queryDate) {
	StringBuffer sb = new StringBuffer();
	sb.append("server").append(serverNo).append("_").append(OUTPUT_FILE_NAME).append("_").append(queryDate)
	.append(".log");
	return sb.toString();
}

void procFTP(ArrayList<String> outputFileArr) throws Exception {
	commFTP = new CommFTP(getDBconnect(), getDBalias());
	comr = new CommRoutine(getDBconnect(), getDBalias());
	
	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
    commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    commFTP.hEriaLocalDir = String.format("%s%s", getEcsHome(), FILE_FOLDER_PATH);
    commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    commFTP.hEflgModPgm = JAVA_NAME;
    
    int size = outputFileArr.size();
    String outputFileName = "";
    for (int i = 0; i < size; i++) {
    	outputFileName = outputFileArr.get(i);
        showLogMessage("I", "", "mput " + outputFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName(commFTP.hEflgSystemId, "mput " + outputFileName);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + outputFileName + " 資料"+" errcode:"+errCode);
            insertEcsNotifyLog(outputFileName);          
        }
	}

   
}

private int insertEcsNotifyLog(String fileName) throws Exception {
    setValue("crt_date", sysDate);
    setValue("crt_time", sysTime);
    setValue("unit_code", comr.getObjectOwner("3", JAVA_NAME));
    setValue("obj_type", "3");
    setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
    setValue("notify_name", "媒體檔名:" + fileName);
    setValue("notify_desc1", "程式 " + JAVA_NAME + " 無法 FTP 傳送 " + fileName + " 資料");
    setValue("notify_desc2", "");
    setValue("trans_seqno", commFTP.hEflgTransSeqno);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", JAVA_NAME);
    daoTable = "ecs_notify_log";

    insertTable();

    return (0);
}


}
