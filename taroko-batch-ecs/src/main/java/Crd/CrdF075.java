/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/02/10  V1.00.00    JustinWu   initial                                  *
 *  111/02/16  V1.00.01    Ryan      big5 to MS950                                           *
 ******************************************************************************/
package Crd;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;


public class CrdF075 extends AccessDAO {
	private final String progname = "接收送財金信用卡開卡資料回覆檔(使用ICUD17格式) 111/02/16 V1.00.01";
	private String prgmId = "CrdF075";
    String queryDate = "";
    String outputFileName = "";
   
	private final byte emptyByte = " ".getBytes()[0];
	private final byte[] lineSeparatorBytes =  {0x0D , 0x0A};

	CommCrdRoutine comcr = null;
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommTxBill commTxBill;
	CommFTP commFTP = null;
	CommRoutine comr = null;

	public int mainProcess(String[] args) {
		try {
			int rtnCode = 0;
			
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

//            // 若沒有給定查詢日期，則查詢日期為系統日
//            if(args.length == 0) {
//                queryDate = sysDate;
//            }else
//            if(args.length == 1) {
//                // 檢查參數(查詢日期)
//                if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
//                    showLogMessage("E", "", String.format("日期格式[%s]錯誤，日期格式應為西元年yyyyMMdd", args[0]));
//                    return -1;
//                }
//                queryDate = args[0];
//            }else {
//                comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
//            }   

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commTxBill = new CommTxBill(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
		    comr = new CommRoutine(getDBconnect(), getDBalias());
//			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			// ====================================

			String text;
			final String filePathFromDb = "media/icu";
			final String bankNo = "F00600000";
			
			final String fileTypeAttrName = "ICPSMPNA";  //屬性檔
			final String fileTypeDataName = "ICPSMPND";     //資料檔
			
//			final String twDate = new CommDate().getLastTwoTWDate(queryDate);

			// get the fileFolderPath such as C:\EcsWeb\media\icu
			final String fileFolderPath = getFileFolderPath(comc.getECSHOME(), filePathFromDb);
			
			// 若查詢日(queryDate)為西元年2020年07月03日，則fileNameTemplate = F00600000.XXXXXXXX.090703nn.txt，
			// 其中fileName的兩碼年份為民國年後兩碼，因此西元2020年->民國109年->09；nn為編號
//			final String attrFileNameTemplate = String.format("%s\\.%s\\.%s[0-9][0-9].*",
//					bankNo, fileTypeAttrName, twDate + queryDate.substring(4, 8) ); // 檔案正規表達式
//			final String attrFileNameTemplate = String.format("%s\\.%s\\.[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9].*",
//					bankNo, fileTypeAttrName ); // 檔案正規表達式
			
			final String attrFileNameTemplate = String.format("%s\\.%s\\.[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9].*",
					bankNo,
					fileTypeAttrName ); // 檔案正規表達式

			File[] attrFileArr = getAllTodayFiles(fileFolderPath, attrFileNameTemplate);

			// ===== for loop start====================
			
			int totalAttrRecord = 0;
			int totalDataFileNotFound = 0;  // 未找到對應資料檔數
			ErrorFile075 errorFile = new ErrorFile075(sysDate);
			
			if (attrFileArr != null)
			// start 屬性檔==================
			for (File attrFile : attrFileArr) {
				
				String attrFileName = attrFile.getName(); // F00600000.ICPSMPNA.YYMMDDNN

				// open file
				int inputAttrFileIndex = openInputText(attrFile.getAbsolutePath(), "MS950");
				if (inputAttrFileIndex == -1) {
					showLogMessage("E", "", String.format("檔案不存在: %s", attrFileName));
					return -1;
				}

				text = readTextFile(inputAttrFileIndex);

				if (text.trim().length() != 0) {

					totalAttrRecord++;
					String errorCode = getErrCodeFromAttrFile(text);
					showLogMessage("I", "", String.format("[屬性檔]檔案名稱[%s]，錯誤回覆碼[%s]", attrFileName, errorCode));

					int returnCode = insertCrdFileCtl(attrFileName);
					if (returnCode == 0) {
						showLogMessage("I", "", String.format("新增%s至crd_file_ctl成功", attrFileName));
					}
					
					closeInputText(inputAttrFileIndex);
					moveFileToBackup(fileFolderPath, attrFileName);

					// 處理資料檔
//					String fileNameTemplate = String.format("%s\\.%s\\.%s.*", bankNo, fileTypeName, twDate + queryDate.substring(4, 8)); // 檔案正規表達式
//					String fileNameTemplate = String.format("%s\\.%s\\.%s.*", bankNo, fileTypeName, attrFileName.substring(19)); // 檔案正規表達式
					String fileNameTemplate = String.format("%s\\.%s\\.%s.*", bankNo, fileTypeDataName, attrFileName.substring(19)); // 檔案正規表達式
					
					showLogMessage("I", "", String.format("資料檔template[%s]", fileNameTemplate));
					
					File[] fileArr = getAllTodayFiles(fileFolderPath, fileNameTemplate);

					if (fileArr != null && fileArr.length != 0) {
						// 正常來說只有一筆資料檔
						for (File file : fileArr) {
							String fileName = file.getName(); //  F00600000.ICPSMPND.YYMMDDNN

							// open file
							int inputFileIndex = openInputText(file.getAbsolutePath(), "MS950");
							if (inputFileIndex == -1) {
								showLogMessage("E", "", String.format("檔案不存在: %s", fileName));
								return -1;
							}

							while (true) {

								text = readTextFile(inputFileIndex);

								if (text.trim().length() != 0) {
									CrdF075Data crdF075Data = getDataFromFile(text);
									showLogMessage("E", "", String.format("[錯誤資料]資料檔檔名[%s]，卡號[%s]，錯誤回覆碼[%s]",
											fileName, crdF075Data.cardno, crdF075Data.errorCode));
									errorFile.setErrorInfo( crdF075Data);				
								}
								
								if (endFile[inputFileIndex].equals("Y")) {
									break; // break while loop
								}

							}
							
							returnCode = insertCrdFileCtl(fileName);
							if (returnCode == 0) {
								showLogMessage("E", "", String.format("新增%s至crd_file_ctl成功", fileName));
							}
							closeInputText(inputFileIndex);
							moveFileToBackup(fileFolderPath, fileName);				
						}
						
					}else {
						showLogMessage("E", "", String.format("%s找不到相對應資料檔", attrFileName));
						rtnCode = -1;
						totalDataFileNotFound ++;
					}
				}

			}
			// end 屬性檔==================
			
			commitDataBase();
			
			if (errorFile.isError) {
//				rtnCode = -1; //2021-11-01 Justin 
				produceErrorFile(errorFile, fileFolderPath, fileTypeDataName);
				
				procFTP();
			    renameFile1(outputFileName);
			}

			if (attrFileArr == null || attrFileArr.length == 0)
				showLogMessage("I", "", "無資料可處理");
			else
				showLogMessage("I", "",String.format("處理日期：%s，　處理屬性檔數：%s，  找不到相對應資料檔數：%s， 總錯誤筆數：%s", 
				    sysDate, totalAttrRecord, totalDataFileNotFound, errorFile.isError ? errorFile.cardnoArr.size() : 0));
			
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

	private String getErrCodeFromAttrFile(String text) throws UnsupportedEncodingException {
		byte[] bytesArr = text.getBytes("MS950");

		return CommTxBill.subByteToStr(bytesArr, 23, 27);  //取24~27byte
		
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
			throw new Exception("file path selected from database is error");
		}

		String[] arrFilePathFromDb = filePathFromDb.split("/");

		fileFolderPath = Paths.get(projectPath).toString();

		for (int i = 0; i < arrFilePathFromDb.length; i++)
			fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();

		return fileFolderPath;
	}

	/**
	 * 找出所有符合的字串
	 * 
	 * @param fileFolderPath
	 * @param fileNameTemplate
	 * @return
	 */
	private File[] getAllTodayFiles(String fileFolderPath, String fileNameTemplate) {
		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);
		File file = new File(fileFolderPath);

		File[] files = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(fileNameTemplate);
			}
		});

		return files;
	}

	private CrdF075Data getDataFromFile(String text) throws UnsupportedEncodingException {
		byte[] bytesArr = text.getBytes("MS950");

		CrdF075Data f075Data = new CrdF075Data();

		f075Data.cardno = CommTxBill.subByteToStr(bytesArr, 0, 19);// 卡片號碼
		f075Data.cardActiveDate = CommTxBill.subByteToStr(bytesArr, 19, 27);// 開卡日期
		f075Data.customerCode = CommTxBill.subByteToStr(bytesArr, 176, 196);// 客戶代號
		f075Data.cardOwnerBirthDate = CommTxBill.subByteToStr(bytesArr, 196, 204);// 主卡人出生日期
		f075Data.cardOwnerCellphone = CommTxBill.subByteToStr(bytesArr, 523, 543);// 主卡人行動電話
		f075Data.effectiveDate = CommTxBill.subByteToStr(bytesArr, 775, 779);// 有效日期(西元YYMM)
		f075Data.errorCode = CommTxBill.subByteToStr(bytesArr, 794, 798);// 錯誤回覆碼
		
		return f075Data;
	}

	private void produceErrorFile(ErrorFile075 errorFile, String inputFileFolderPath, String fileTypeName) throws Exception {

		inputFileFolderPath = SecurityUtil.verifyPath(inputFileFolderPath);
		// media/icu/error
		Path outputFileFolderPath = Paths.get(inputFileFolderPath, "error");

		// create the parent directory if parent the directory is not exist
		Files.createDirectories(outputFileFolderPath);

		// get output file name :F00600000.ICPSMPND.YYMMDDhhmmss.TXT =>
		// ICPSMPND.ERR.YYYYMMDDhhmmss
		outputFileName = String.format("%s.ERR.%s.TXT", fileTypeName, sysDate + sysTime);
		outputFileName = SecurityUtil.verifyPath(outputFileName);

		// get output file path
		String outputFilePath = Paths.get(outputFileFolderPath.toString(), outputFileName).toString();

		int outFileIndex = openBinaryOutput2(outputFilePath);

		writeFile(errorFile, outFileIndex);

		showLogMessage("I", "", String.format("產出錯誤報表檔: %s", outputFilePath));

		closeBinaryOutput2(outFileIndex);

	}

	private void writeFile(ErrorFile075 errorFile, int outFileIndex) throws Exception, UnsupportedEncodingException {
		int size = errorFile.cardnoArr.size();
		final String lineSeparator = new String(lineSeparatorBytes, "MS950");
		for (int i = 0; i < size; i++) {
			writeFileInCertainLength(outFileIndex, errorFile.cardnoArr.get(i), 19);
			writeFileInCertainLength(outFileIndex, errorFile.cardActiveDateArr.get(i), 8);
			writeFileInCertainLength(outFileIndex, errorFile.customerCodeArr.get(i), 20);
			writeFileInCertainLength(outFileIndex, errorFile.cardOwnerBirthDateArr.get(i), 8);
			writeFileInCertainLength(outFileIndex, errorFile.cardOwnerCellphoneArr.get(i), 20);
			writeFileInCertainLength(outFileIndex, errorFile.effectiveDateArr.get(i), 4);
			writeFileInCertainLength(outFileIndex, errorFile.errorCodeArr.get(i), 4);
			writeFileInCertainLength(outFileIndex, errorFile.processDate, 8);
			writeFileInCertainLength(outFileIndex, lineSeparator, lineSeparatorBytes.length);

		}

	}

	private void writeFileInCertainLength(int outFileIndex, String str, int targetLength) throws Exception {

		byte[] byteArr = str.getBytes("MS950");

		writeBinFile2(outFileIndex, byteArr, byteArr.length);

		int emptyLength = targetLength - byteArr.length;

		if (emptyLength == 0)
			return;

		byte[] emptyByteArr = new byte[emptyLength];
		for (int i = 0; i < emptyLength; i++) {
			emptyByteArr[i] = emptyByte;
		}

		writeBinFile2(outFileIndex, emptyByteArr, emptyLength);

	}

	private void moveFileToBackup(String fileFolderPath, String fileName) throws IOException {
		fileFolderPath = SecurityUtil.verifyPath(fileFolderPath);
		fileName = SecurityUtil.verifyPath(fileName);
		Path backupPath = Paths.get(fileFolderPath, "backup");

		// create the parent directory if parent the directory is not exist
		Files.createDirectories(backupPath);

		Path backupFilePath = Paths.get(backupPath.toString(), String.format("%s.%s", fileName,  sysDate));

		Files.move(Paths.get(fileFolderPath, fileName), backupFilePath, StandardCopyOption.REPLACE_EXISTING);

		showLogMessage("I", "", String.format("移動%s至 %s", fileName, backupFilePath.toString()));

	}
	
	 void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + outputFileName + " 開始傳送....");
	      int errCode = commFTP.ftplogName("NCR2EMP", "mput " + outputFileName);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + outputFileName + " 資料"+" errcode:"+errCode);
	          insertEcsNotifyLog(outputFileName);          
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
	
	/**
	 * insert into crd_file_ctl
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private int insertCrdFileCtl(String fileName) throws Exception {
		
		daoTable = "crd_file_ctl";

		setValue("file_name", fileName);
		setValue("trans_in_date", sysDate);
		setValue("crt_date", sysDate);

		return insertTable();
	}

	public static void main(String[] args) {
		CrdF075 proc = new CrdF075();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);

	}

}

class CrdF075Data {
	String cardno ;// 卡片號碼
	String cardActiveDate ;// 開卡日期
	String customerCode ;// 客戶代號
	String cardOwnerBirthDate ;// 主卡人出生日期
	String cardOwnerCellphone ;// 主卡人行動電話
	String effectiveDate ;// 有效日期
	String errorCode ;// 錯誤回覆碼
}

class ErrorFile075 {
	boolean isError;
	ArrayList<String> cardnoArr ;// 卡片號碼
	ArrayList<String> cardActiveDateArr ;// 開卡日期
	ArrayList<String> customerCodeArr ;// 客戶代號
	ArrayList<String> cardOwnerBirthDateArr ;// 主卡人出生日期
	ArrayList<String> cardOwnerCellphoneArr ;// 主卡人行動電話
	ArrayList<String> effectiveDateArr ;// 有效日期
	ArrayList<String> errorCodeArr ;// 錯誤回覆碼
	String processDate;
	
	public ErrorFile075(String sysDate) {
		processDate  = sysDate;
		cardnoArr= new ArrayList<String>();// 卡片號碼
		cardActiveDateArr= new ArrayList<String>();// 開卡日期
		customerCodeArr= new ArrayList<String>();// 客戶代號
		cardOwnerBirthDateArr= new ArrayList<String>();// 主卡人出生日期
		cardOwnerCellphoneArr= new ArrayList<String>();// 主卡人行動電話
		effectiveDateArr= new ArrayList<String>();// 有效日期
		errorCodeArr= new ArrayList<String>();// 錯誤回覆碼
	}
	
	/**
	 * 設定錯誤檔資料
	 * @param cardnoArr
	 * @param cardActiveDateArr
	 * @param customerCodeArr
	 * @param cardOwnerBirthDateArr
	 * @param cardOwnerCellphoneArr
	 * @param effectiveDateArr
	 * @param errorCodeArr
	 */
	 void setErrorInfo(CrdF075Data crdF075Data) {
		this.isError = true;
		this.cardnoArr.add(crdF075Data.cardno); 
		this.cardActiveDateArr.add(crdF075Data.cardActiveDate); 
		this.customerCodeArr.add(crdF075Data.customerCode); 
		this.cardOwnerBirthDateArr.add(crdF075Data.cardOwnerBirthDate); 
		this.cardOwnerCellphoneArr.add(crdF075Data.cardOwnerCellphone); 
		this.effectiveDateArr.add(crdF075Data.effectiveDate); 
		this.errorCodeArr.add(crdF075Data.errorCode); 
	}
}
