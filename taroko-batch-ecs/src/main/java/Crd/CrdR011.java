/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/03/31  V1.00.00    JustinWu   initial                                  *
 *  111/03/31  V1.00.01    JustinWu   select crd_card or dbc_card              *
 *                                    based on acct_type                       *
 *  111/04/06  V1.00.02    JustinWu   增加3個欄位new_end_date, expire_chg_flag,*
 *                                    and result                               *
 *  111/04/07  V1.00.03    JustinWu   加入header以及壓縮檔案                   *
 *  111/04/07  V1.00.04    Sunny      加入比對結果09的處理，E4有重複，其中一個改E5         *
 *  111/04/08  V1.00.05    JustinWu   split columns through "," instead of byte*
 *                                    add an input arguments fileDate          *
 *  111/04/11  V1.00.06    JustinWu   修改分類邏輯                             *
 *  111/04/28  V1.00.07    JustinWu   增加特指分類邏輯、新增產出報表           *
 *  111/05/04  V1.00.08    JustinWu   增加資料換行                             *
 ******************************************************************************/
package Crd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;

public class CrdR011 extends AccessDAO {
	private final String progname = "產生CARDLINK與ECS卡況比對報表 111/05/04 V1.00.08";
	private String prgmId = "CrdR011";
	
	long recordCntOfAFile = 0;
	long recordCntOfAEFile = 0;
	long recordCntOfResetBlockFile = 0;
	
	static final private String FILE_FOLDER_PATH = "media/crd/";
	static final private String OUTPUT_FILE_FOLDER_PATH = "media/crd/";
	static final private String BACKUP_FILE_FOLDER_PATH = "media/crd/backup/";
	static final private String FILE_NAME_CARD_HEAD = "CARD"; // full name = CARD_1110330.TXT
	static final private String FILE_NAME_BLOCK_HEAD = "BLOCK"; // full name = BLOCK_YYYMMDD.TXT AND RESET_BLOCK_YYYMMDD.TXT
	static final private String CARD_HEADER  = "ORG,MASTER_ID_NO,ID_NO,CARD_NO,CODE,CODE_REASON,CODE_DATE,ACCT_TYPE,CURRENT_CODE,OPPOST_REASON,OPPOST_DATE,SPEC_FLAG,SPEC_STATUS,SPEC_DATE,BLOCK_REASON1,BLOCK_DATE,NEW_END_DATE,EXPIRE_CHG_FLAG,MEMO\r\n";
	static final private String BLOCK_HEADER = "ORG,MASTER_ID_NO,ID_NO,CARD_NO,CODE,CODE_REASON,CODE_DATE\r\n";

	
	CommCrd commCrd = new CommCrd();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	private String fileDate = "";
	long batchNum = 1000;
	
	public int mainProcess(String[] args) {
		try {
			int rtnCode = 0;
			
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (!connectDataBase()) {
				commCrd.errExit("connect DataBase error", "");
			}
			
			CommCrdRoutine comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			
			loadArgs(args);
			
			final String fileFolderPath = getFileFolderPath(commCrd.getECSHOME(), FILE_FOLDER_PATH);
			
			String cardFileName = String.format("%s_%s.TXT", FILE_NAME_CARD_HEAD, fileDate);
			String cardFilePath = getFilePath(fileFolderPath, cardFileName);
			
			final String outputFileFolderPath = getFileFolderPath(commCrd.getECSHOME(), OUTPUT_FILE_FOLDER_PATH);
			
			// ECS_CARD_YYYMMDD.TXT
			String cardOutputFileName = String.format("%s_%s", "ECS", cardFileName);
			String cardOutputFilePath = getFilePath(outputFileFolderPath, cardOutputFileName);
			
			// ECS_E_CARD_YYYMMDD.TXT
			String cardErrOutputFileName = String.format("%s_E_%s", "ECS", cardFileName);
			String cardErrOutputFilePath = getFilePath(outputFileFolderPath, cardErrOutputFileName);
			
			// BLOCK_YYYMMDD.TXT
			String blockOutputFileName = String.format("%s_%s.TXT", FILE_NAME_BLOCK_HEAD, fileDate);
			String blockOutputFilePath = getFilePath(outputFileFolderPath, blockOutputFileName);
			
			// RESET_BLCOK_YYYMMDD.TXT
			String resetBlockOutputFileName = String.format("RESET_%s", blockOutputFileName);
			String resetBlockOutputFilePath = getFilePath(outputFileFolderPath, resetBlockOutputFileName);
			
			processFile(cardFilePath, cardOutputFilePath, cardErrOutputFilePath, blockOutputFilePath, resetBlockOutputFilePath);

			if (recordCntOfAFile > 0) {
				commFTP = new CommFTP(getDBconnect(), getDBalias());
				comr = new CommRoutine(getDBconnect(), getDBalias());

				int result = compressLog(cardOutputFilePath);
				if (result == 0) {
					File file = new File(cardOutputFilePath);
					file.deleteOnExit();
					cardOutputFileName = cardOutputFileName + ".gz";
				}
				procFTP(cardOutputFileName);
				renameFile1(cardOutputFileName);
				insertFileCtl(cardOutputFileName, recordCntOfAFile);
				
				if (recordCntOfAEFile > 0) {
					// card error file
					result = compressLog(cardErrOutputFilePath);
					if (result == 0) {
						File file = new File(cardErrOutputFilePath);
						file.deleteOnExit();
						cardErrOutputFileName = cardErrOutputFileName + ".gz";
					}
					procFTP(cardErrOutputFileName);
					renameFile1(cardErrOutputFileName);
					insertFileCtl(cardErrOutputFileName, recordCntOfAEFile);
					
					// block file
					result = compressLog(blockOutputFilePath);
					if (result == 0) {
						//File file = new File(blockOutputFilePath);
						//file.deleteOnExit();
						blockOutputFileName = blockOutputFileName + ".gz";
					}
					procFTP(blockOutputFileName);
					renameFile1(blockOutputFileName);
					insertFileCtl(blockOutputFileName, recordCntOfAEFile);
				}
				
				if (recordCntOfResetBlockFile > 0) {	
					// reset block file
					result = compressLog(resetBlockOutputFilePath);
					if (result == 0) {
						//File file = new File(resetBlockOutputFilePath);
						//file.deleteOnExit();
						resetBlockOutputFileName = resetBlockOutputFileName + ".gz";
					}
					procFTP(resetBlockOutputFileName);
					renameFile1(resetBlockOutputFileName);
					insertFileCtl(resetBlockOutputFileName, recordCntOfResetBlockFile);
				}
				
				// 原始檔不搬
				// renameFile1(cardFileName);
			}
			
			showLogMessage("I", "", String.format("共產出[%d]筆資料，其中[%d]筆不一致", recordCntOfAFile, recordCntOfAEFile));
			showLogMessage("I", "", String.format("原始檔F Code資料共[%d]筆", recordCntOfResetBlockFile));

			showLogMessage("I", "", "執行結束");
			comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatchEnd();
			
			return rtnCode;
			
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}
	
	private void loadArgs(String[] args) {
		if (args.length == 0) {
			fileDate = new CommDate().twDate();
		}else if (args.length == 1) {
			fileDate = new CommDate().toTwDate(args[0]);
		}else {
			fileDate = new CommDate().twDate();
		}
	}

	private int compressLog(String outputFilePath) {
		File file = new File(outputFilePath);
		String outFilePath = SecurityUtil.verifyPath(outputFilePath + ".gz");
		if (new File(outFilePath).exists() == false) {
			try(FileOutputStream fos = new FileOutputStream(outFilePath);
				    GZIPOutputStream gzipOut = new GZIPOutputStream(fos);
				    FileInputStream fis = new FileInputStream(file);){
				byte[] bytes = new byte[1024];
			    int length;
			    while((length = fis.read(bytes)) >= 0) {
			    	gzipOut.write(bytes, 0, length);
			    } 
			} catch (Exception e) {
				showLogMessage("E", "", e.getLocalizedMessage());
				e.printStackTrace();
				return 1; //檔案出現錯誤
			}
			
		
		}else {
			return 2; // 檔案已被壓縮
		}
		
	    return 0;
		
	}
	
	void insertFileCtl(String fileName, long recordCnt) throws Exception {
        daoTable = "crd_file_ctl";
        setValue("file_name", fileName);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", 0);
        setValueLong("record_cnt", recordCnt);
        setValue("trans_in_date", sysDate);
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable = "crd_file_ctl";
            updateSQL = "head_cnt  = ?,";
            updateSQL += " record_cnt = ?,";
            updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name  = ? ";
            setInt(1, 0);
            setLong(2, recordCnt);
            setString(3, fileName);
            updateTable();
            if (notFound.equals("Y")) {
            	new CommCrdRoutine(getDBconnect(), getDBalias()).errRtn("update_crd_file_ctl not found!", "", "");
            }
        }
    }
	
	void renameFile1(String removeFileName) throws Exception {
		String tmpstr1 = commCrd.getECSHOME() + "/" + OUTPUT_FILE_FOLDER_PATH + removeFileName;
		String tmpstr2 = commCrd.getECSHOME() + "/" + BACKUP_FILE_FOLDER_PATH + removeFileName + "." + sysDate + sysTime;
		
		if (commCrd.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");

	}
	
	void copyFile1(String removeFileName) throws Exception {
		String tmpstr1 = commCrd.getECSHOME() + "/" + OUTPUT_FILE_FOLDER_PATH + removeFileName;
		String tmpstr2 = commCrd.getECSHOME() + "/" + BACKUP_FILE_FOLDER_PATH + removeFileName + "." + sysDate + sysTime;
		
		if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]copy失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已copy至 [" + tmpstr2 + "]");

	}

	/****************************************************************************/
	int procFTP(String outputFileName) throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/%s", commCrd.getECSHOME(), OUTPUT_FILE_FOLDER_PATH);
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      
	      int sendOkCnt = 0;
	      
	      // SEND DATA file
    	  String outputDataFileName = outputFileName;  	  
	      showLogMessage("I", "", "mput " + outputDataFileName + " 開始傳送....");
	      int errcode = commFTP.ftplogName("NCR2EMP", "mput " + outputDataFileName);
	      
	      if (errcode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + outputDataFileName + " 資料"+" errcode:"+errcode);
	          insertEcsNotifyLog(outputDataFileName);          
	      }else {
	    	  sendOkCnt ++;
	      }
	      
	      return sendOkCnt;
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
	
	private boolean processFile(String cardFilePath, String cardOutputFilePath, String cardErrOutputFilePath, 
			String blockOutputFilePath, String resetBlockOutputFilePath) throws Exception {
		showLogMessage("I", "", String.format("讀取檔案[%s]", cardFilePath));
		// open input file
		File file = new File(cardFilePath);
		if (file.exists() == false) {
			showLogMessage("I", "", "無檔案可處理");
			return false;
		}
		
		setConsoleMode("N");
		int fi = openInputText(cardFilePath, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return false;
		}
		showLogMessage("I", "", " Process file path =[" + cardFilePath + "]");
		
		// open card output file
		int cardOutputFileIndex = openOutputText(cardOutputFilePath, "UTF8");
		if (cardOutputFileIndex == -1) {
			throw new Exception("無法產出TXT");
		}
		writeTextFile(cardOutputFileIndex, CARD_HEADER);
		
		// open card error output file
		int cardErrOutputFileIndex = openOutputText(cardErrOutputFilePath, "UTF8");
		if (cardErrOutputFileIndex == -1) {
			throw new Exception("無法產出TXT");
		}
		writeTextFile(cardErrOutputFileIndex, CARD_HEADER);
		
		// open block output file
		int blockOutputFileIndex = openOutputText(blockOutputFilePath, "UTF8");
		if (blockOutputFileIndex == -1) {
			throw new Exception("無法產出TXT");
		}
		writeTextFile(blockOutputFileIndex, BLOCK_HEADER);
		
		// open reset block output file
		int resetBlockOutputFileIndex = openOutputText(resetBlockOutputFilePath, "UTF8");
		if (resetBlockOutputFileIndex == -1) {
			throw new Exception("無法產出TXT");
		}
		writeTextFile(resetBlockOutputFileIndex, BLOCK_HEADER);
		
		StringBuilder buffer = new StringBuilder();
		recordCntOfAFile = 0;
		recordCntOfAEFile = 0;
		while (true) {
			String rowOfData = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
			recordCntOfAFile++;
			
			// get all needed data
			CrdR011File fileObj = getFileObj(rowOfData);
			fileObj.selectCol = getSelectColObj(fileObj.cardNo);
			fileObj.getDataResult();
			String newString = fileObj.turnIntoStr(rowOfData);
			buffer.append(newString);
			
			if (fileObj.isTypeE) {
				recordCntOfAEFile++;
				writeTextFile(cardErrOutputFileIndex, newString);
				writeTextFile(blockOutputFileIndex, rowOfData + "\r\n");
			}
			
			if (fileObj.isCodeF) {
				recordCntOfResetBlockFile++;
				writeTextFile(resetBlockOutputFileIndex, rowOfData + "\r\n");
			}
			
			if (recordCntOfAFile % batchNum == 0) {
				writeTextFile(cardOutputFileIndex, buffer.toString());
				buffer = new StringBuilder();
				showLogMessage("I", "", String.format("寫入%d到%d筆資料完成", recordCntOfAFile-batchNum+1, recordCntOfAFile));
			}
		}
		
		if (recordCntOfAFile > 0 && recordCntOfAFile % batchNum != 0) {
			writeTextFile(cardOutputFileIndex, buffer.toString());
			buffer = null;
			showLogMessage("I", "", String.format("寫入%d到%d筆資料完成", recordCntOfAFile-(recordCntOfAFile%batchNum)+1, recordCntOfAFile));
		}
		
		if (fi != -1) {
			closeInputText(fi);
		}
		
		if (cardOutputFileIndex != -1) {
			closeOutputText(cardOutputFileIndex);
		}
		
		if (cardErrOutputFileIndex != -1) {
			closeOutputText(cardErrOutputFileIndex);
		}
		
		if (blockOutputFileIndex != -1) {
			closeOutputText(blockOutputFileIndex);
		}
		
		if (resetBlockOutputFileIndex != -1) {
			closeOutputText(resetBlockOutputFileIndex);
		}

		return true;
	}

	private CrdR011SelectCol getSelectColObj(String cardNo) throws Exception {
		CrdR011SelectCol selectCol = new CrdR011SelectCol();
		selectCcaCardBase(selectCol, cardNo);
		if ("90".equals(selectCol.acctType)) {
			selectDbcCard(selectCol, cardNo);
		}else {
			selectCrdCard(selectCol, cardNo);
		}
		selectCcaCardAcct(selectCol, cardNo);
		return selectCol;
	}
	
	private void selectCcaCardAcct(CrdR011SelectCol selectCol, String cardNo) throws Exception {
		sqlCmd =  "SELECT BLOCK_REASON1, BLOCK_DATE FROM CCA_CARD_ACCT WHERE P_SEQNO IN (SELECT P_SEQNO FROM crd_card WHERE CARD_NO = ?)";
		setString(1, cardNo); 
		int cnt = selectTable();
		if (cnt > 0) {
			selectCol.blockReason1 = getValue("BLOCK_REASON1");
			selectCol.blockDate = getValue("BLOCK_DATE");
		}
	}
	
	private void selectCcaCardBase(CrdR011SelectCol selectCol, String cardNo) throws Exception {
		sqlCmd =  "SELECT ACCT_TYPE, SPEC_FLAG, SPEC_STATUS, SPEC_DATE FROM CCA_CARD_BASE WHERE CARD_NO = ?";
		setString(1, cardNo); 
		int cnt = selectTable();
		if (cnt > 0) {
			selectCol.acctType = getValue("ACCT_TYPE");
			selectCol.specFlag = getValue("SPEC_FLAG");
			selectCol.specStatus = getValue("SPEC_STATUS");
			selectCol.specDate = getValue("SPEC_DATE");
		}
	}

	private void selectCrdCard(CrdR011SelectCol selectCol, String cardNo) throws Exception {
		sqlCmd =  "Select current_code, oppost_reason, oppost_date, new_end_date, expire_chg_flag from crd_card where CARD_NO = ? ";
		setString(1, cardNo); 
		int cnt = selectTable();
		if (cnt > 0) {
			selectCol.currentCode = getValue("current_code");
			selectCol.oppostReason = getValue("oppost_reason");
			selectCol.oppostDate = getValue("oppost_date");
			selectCol.newEndDate = getValue("new_end_date");
			selectCol.expireChgFlag = getValue("expire_chg_flag");
		}
	}
	
	private void selectDbcCard(CrdR011SelectCol selectCol, String cardNo) throws Exception {
		sqlCmd =  "Select current_code, oppost_reason, oppost_date, new_end_date, expire_chg_flag from dbc_card where CARD_NO = ? ";
		setString(1, cardNo); 
		int cnt = selectTable();
		if (cnt > 0) {
			selectCol.currentCode = getValue("current_code");
			selectCol.oppostReason = getValue("oppost_reason");
			selectCol.oppostDate = getValue("oppost_date");
			selectCol.newEndDate = getValue("new_end_date");
			selectCol.expireChgFlag = getValue("expire_chg_flag");
		}
	}

	private CrdR011File getFileObj(String rowOfData) {
		CrdR011File fileObj = new CrdR011File();
		try {
//			byte[] tmpByte = rowOfData.getBytes("UTF8");
//			fileObj.masterIdNo = commCrd.subString(tmpByte, 4, 11, "UTF8");
//			fileObj.cardNo = commCrd.subString(tmpByte, 28, 16, "UTF8");
//			fileObj.code = commCrd.subString(tmpByte, 45, 1, "UTF8");
			
			// 2022/04/08 Justin split columns through "," instead of byte
			String[] strArr = rowOfData.split(",");
			fileObj.masterIdNo = (1 < strArr.length) ? strArr[1].trim() : "";
			fileObj.cardNo = (3 < strArr.length) ? strArr[3].trim() : "";
			fileObj.code = (4 < strArr.length) ? strArr[4].trim() : "";
			
		} catch (Exception e) {
			showLogMessage("E", "", "以下資料格式錯誤");
			showLogMessage("E", "", rowOfData);
			e.printStackTrace();
		}
		return fileObj;
	}

	private String getFilePath(String fileFolderPath, String fileName) {
		String path = Paths.get(fileFolderPath, fileName).toString();
		return SecurityUtil.verifyPath(path);
	}

	/**
	 * get file folder path by the project path and the file path selected from
	 * database
	 * 
	 * @param projectPath
	 * @param filePathFromDb
	 * @param fileNameAndTxt
	 * @return
	 * @throws Exception
	 */
	private String getFileFolderPath(String projectPath, String filePathFromDb) throws Exception {
		String fileFolderPath = null;

		projectPath = SecurityUtil.verifyPath(projectPath);
		if (filePathFromDb.isEmpty() || filePathFromDb == null) {
			throw new Exception("file path is error");
		}

		String[] arrFilePathFromDb = filePathFromDb.split("/");

		fileFolderPath = Paths.get(projectPath).toString();

		for (int i = 0; i < arrFilePathFromDb.length; i++)
			fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();

		return fileFolderPath;
	}
	
	public static void main(String[] args) {
		CrdR011 proc = new CrdR011();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);

	}

}


class CrdR011File {
	
	/**
	 * "A","F","L","S","O","B","E","X"
	 */
	static private final List<String> CODE_CHECK_LIST = Arrays.asList(new String[] {"A","F","L","S","O","B","E","X"});
	static private final byte[] lineSeparatorBytes =  {0x0D , 0x0A};
	String masterIdNo = "";
	String code = "";
	String cardNo = "";
	String result = "";
	boolean isTypeE = false;
	boolean isCodeF = false;
	CrdR011SelectCol selectCol = null;
	
	public String turnIntoStr(String rowOfData) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(rowOfData)
			  .append(",").append(fixLeft(selectCol.acctType, 2))
			  .append(",").append(fixLeft(selectCol.currentCode, 1))
			  .append(",").append(fixLeft(selectCol.oppostReason, 4))
			  .append(",").append(fixLeft(selectCol.oppostDate, 8))
			  .append(",").append(fixLeft(selectCol.specFlag, 1))
			  .append(",").append(fixLeft(selectCol.specStatus, 2))
			  .append(",").append(fixLeft(selectCol.specDate, 8))
			  .append(",").append(fixLeft(selectCol.blockReason1, 2))
			  .append(",").append(fixLeft(selectCol.blockDate, 8))
			  .append(",").append(fixLeft(selectCol.newEndDate, 8))
			  .append(",").append(fixLeft(selectCol.expireChgFlag, 1))
			  .append(",").append(fixLeft(result, 100))
			  .append(new String(lineSeparatorBytes, "UTF8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	private String fixLeft(String str, int len) throws UnsupportedEncodingException {
	    int size = (Math.floorDiv(len, 100) + 1) * 100;
	    String spc = "";
	    for (int i = 0; i < size; i++)
	      spc += " ";
	    if (str == null)
	      str = "";
	    str = str + spc;
	    byte[] bytes = str.getBytes("UTF8");
	    byte[] vResult = new byte[len];
	    System.arraycopy(bytes, 0, vResult, 0, len);

	    return new String(vResult, "UTF8");
   }
	
	//	CASE
	//	WHEN MASTER_ID_NO LIKE '%*%' THEN '08-【一致】-新系統卡號不存在(緊急替代卡)'
	//	WHEN current_code='' THEN 'E1-【不一致】-新系統卡號不存在'
	//	WHEN code='' AND CURRENT_CODE='0' AND SPEC_STATUS='' AND BLOCK_REASON1='' THEN '00-【一致】-正常狀態的卡片'
	//	WHEN code='' AND CURRENT_CODE='0' AND SPEC_STATUS<>'' THEN 'E2-【不一致】-cardlink無code，新系統為活卡且有上特指'
	//	WHEN code='' AND CURRENT_CODE='0' AND BLOCK_REASON1<>'' THEN 'E3-【不一致】-cardlink無code，新系統為活卡且有上凍結'
	//	WHEN code='' AND CURRENT_CODE<>'0' THEN 'E4-【不一致】-新系統已停卡，cardlink無code'
	//	WHEN code='J' AND CURRENT_CODE='0' THEN '01-【一致】-cardlink J code，新系統為活卡(未到期)'
	//	WHEN code='J' AND CURRENT_CODE<>'0' THEN '09-【一致】-cardlink J code，新系統已到期停卡'
	//	WHEN code='C' AND CURRENT_CODE='0' THEN '02-【一致】-cardlink C code，新系統不處理'
	//	WHEN code='H' AND CURRENT_CODE='0' THEN '03-【一致】-cardlink H code，新系統不處理'
	//	WHEN code='P' AND CURRENT_CODE='0' THEN '04-【一致】-cardlink P code，新系統不處理'
	//	WHEN code<>'' AND current_code<>'0' THEN '05-【一致】-新系統已停卡,cardlink也有上code'
	//	WHEN code<>'' AND current_code='0' AND SPEC_STATUS<>'' THEN '06-【一致】-新系統活卡且上特指，cardlink有上code'
	//	WHEN code<>'' AND current_code='0' AND BLOCK_REASON1<>'' THEN '07-【一致】-新系統活卡且上凍結，cardlink有上code'
	//	WHEN code<>'' AND current_code='0' AND SPEC_STATUS='' AND BLOCK_REASON1='' THEN 'E5-【不一致】新系統正常，cardlink有上code'
	//	ELSE '99-待分析'
		public void getDataResult() {
			String tmpCode = this.code.trim();
			// 導出另一個報表，將原始檔 tmpCode = F的名單，另外導成RESET_BLOCK_1110425.TXT
			if ("F".equals(tmpCode)) this.isCodeF = true;
			if (this.masterIdNo.contains("*")) {
				this.result = "08-【一致】-新系統卡號不存在(緊急替代卡)";
			}else if(isEmpty(this.selectCol.currentCode)) {
				this.result = "E1-【不一致】-新系統卡號不存在";
				this.isTypeE = true;
			}else {
				if ("".equals(tmpCode) && "0".equals(this.selectCol.currentCode) && 
						isEmpty(this.selectCol.specStatus) && isEmpty(this.selectCol.blockReason1) ) {
					this.result = "00-【一致】-正常狀態的卡片";
				}else 
				if ("".equals(tmpCode) && "0".equals(this.selectCol.currentCode) && 
						isEmpty(this.selectCol.specStatus) == false) {
					this.result = "E2-【不一致】-cardlink無code，新系統為活卡且有上特指";
					this.isTypeE = true;
				}else 
				if ("".equals(tmpCode) && "0".equals(this.selectCol.currentCode) && 
						isEmpty(this.selectCol.blockReason1) == false) {
					this.result = "E3-【不一致】-cardlink無code，新系統為活卡且有上凍結";
					this.isTypeE = true;
				}else 
				if ("".equals(tmpCode) && "0".equals(this.selectCol.currentCode) == false) {
					this.result = "E4-【不一致】-新系統已停卡，cardlink無code";
					this.isTypeE = true;
				}else 
				if ("J".equals(tmpCode) && "0".equals(this.selectCol.currentCode.trim())) {
					this.result = "01-【一致】-cardlink J code，新系統為活卡(未到期)";
				}else 
				if ("J".equals(tmpCode) && "0".equals(this.selectCol.currentCode.trim()) == false) {
					this.result = "09-【一致】-cardlink J code，新系統已到期停卡";
				}else 
				if ("C".equals(tmpCode) && "0".equals(this.selectCol.currentCode.trim())) {
					this.result = "02-【一致】-cardlink C code，新系統不處理";
				}else 
				if ("H".equals(tmpCode) && "0".equals(this.selectCol.currentCode.trim())) {
					this.result = "03-【一致】-cardlink H code，新系統不處理";
				}else 
				if ("P".equals(tmpCode) && "0".equals(this.selectCol.currentCode.trim())) {
					this.result = "04-【一致】-cardlink P code，新系統不處理";
				}else
				if ("".equals(tmpCode) == false && "0".equals(this.selectCol.currentCode.trim()) == false) {
					this.result = "05-【一致】-新系統已停卡,cardlink也有上code";
				}else
				if ("".equals(tmpCode) == false && "0".equals(this.selectCol.currentCode.trim()) && 
						isEmpty(this.selectCol.specStatus) == false) {
					if (CODE_CHECK_LIST.contains(tmpCode)) {
						this.result = String.format("E6 – CARDLINK已上CODE(停用碼為%s)，新系統為活卡且有上特指", tmpCode);
						this.isTypeE = true;
					}else {
						this.result = "06-【一致】-新系統活卡且上特指，cardlink有上code";
					}
				}else
				if ("".equals(tmpCode) == false && "0".equals(this.selectCol.currentCode.trim())
						&& isEmpty(this.selectCol.blockReason1) == false) {
					if (CODE_CHECK_LIST.contains(tmpCode)){
						this.result = String.format("E7 – CARDLINK已上CODE(停用碼為%s)，新系統為活卡且有上特指", tmpCode);
						this.isTypeE = true;
					} else {
						this.result = "07-【一致】-新系統活卡且上凍結，cardlink有上code";
					}
				}else 
				if ("".equals(tmpCode) == false && "0".equals(this.selectCol.currentCode.trim()) &&
						isEmpty(this.selectCol.specStatus) && isEmpty(this.selectCol.blockReason1) ) {
					this.result = "E5-【不一致】新系統正常，cardlink有上code";
					this.isTypeE = true;
				}
			}
			
			if (result.isEmpty()) {
				this.result = "99-待分析";
				this.isTypeE = true;
			}
			
		}
		
		private boolean isEmpty(String str) {
			if (str == null) return true;
			return str.trim().isEmpty();
		}
}

class CrdR011SelectCol {
	
	// the value is from DB
	String acctType = ""; //2
	String currentCode = ""; //1
	String oppostReason = ""; //4
	String oppostDate = ""; //8
	String specFlag = ""; //1
	String specStatus = ""; //2
	String specDate = ""; //8
	String blockReason1 = ""; //2
	String blockDate = ""; //8
	String newEndDate = ""; //8
	String expireChgFlag = ""; //1
	
	
}

