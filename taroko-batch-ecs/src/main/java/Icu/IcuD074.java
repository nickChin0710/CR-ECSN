/*******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/06/30  V1.00.00    Wendy Lu                          initial                           *
 *  109-07-03  V1.01.02  yanghan       修改了變量名稱和方法名稱            *
 *  109/07/03  V1.01.01    JustinWu                  change the program name*
 *  109-07-22    yanghan       修改了字段名称            *
 *  109/08/13  V1.01.03   Wilson       測試修改                                                                                              *
 *  109/08/14  V1.01.04   Wilson       資料夾名稱修改為小寫                                                                       *  
 *  109-09-04  V1.00.01  yanghan     解决Portability Flaw: Locale Dependent Comparison问题    * 
 *  109/09/14  V1.00.06    Zuwei     code scan issue    
 *  109/09/29  V1.00.07   Wilson      營業日才執行                                                                                         *
 *  109/10/12  V1.00.08   Wilson      檔名日期改營業日                                                                                  *
 *  109/10/19  V1.00.09   Wilson      錯誤報表FTP                                *
 *  109-10-19  V1.00.10   shiyuqi       updated for project coding standard     *
 *  110/08/20  V1.00.11   SunnyTs    將mainProcess private改 public               *
 *  111/02/14  V1.01.12    Ryan      big5 to MS950                                           *
 *  111/02/22  V1.01.13   Justin     增加錯誤訊息                             *
 *  111/11/30  V1.00.14   Wilson    新增updateIchCard，將舊愛金卡卡號停用                                     *
 *  111/12/14  V1.00.15   Wilson    製卡類別為R(其他補發卡)才做updateIchCard          *
******************************************************************************/
package Icu;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommSecr;
import com.CommTxBill;

import Dxc.Util.SecurityUtil;

public class IcuD074 extends AccessDAO {
	private final String progname = "CARDLINK愛金卡製卡回饋檔處理程式 111/12/14  V1.00.15";
	private String prgmId = "IcuD074";

	private ErrFileIch errFileIch = new ErrFileIch();
	private final byte emptyByte = " ".getBytes()[0];
	private final byte[] nextLineByte = System.lineSeparator().getBytes();
	// private final String nextLineByte = System.lineSeparator();

	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;
	CommTxBill commTxBill;
	CommFTP commFTP = null;
	CommRoutine comr = null;

	String queryDate = "";	
	String hBusiBusinessDate = "";
	String outputFileName = "";
	
	int lineCnt = 0;
	
	public int mainProcess(String[] args) {
		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// ====================================
			
		   // 固定要做的
		      
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commTxBill = new CommTxBill(getDBconnect(), getDBalias());
			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			
			selectPtrBusinday();

			// 若沒有給定查詢日期，則查詢日期為系統日
		      if(args.length == 0) {
		          queryDate = hBusiBusinessDate;
		      }else
		      if(args.length == 1) {
		          if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
		              showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
		              return -1;
		          }
		          queryDate = args[0];
		      }else {
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

			String text;
			final String filePathFromDb = "media/icu";
			final String fileName = SecurityUtil.verifyPath(String.format("ICH_MAKECARD_RTN_%s" , queryDate));

			// get the fileFolderPath such as C:\EcsWeb\media\icu
			String fileFolderPath = getFileFolderPath(comc.getECSHOME(), filePathFromDb);
			fileFolderPath=SecurityUtil.verifyPath(fileFolderPath);
			String filePath = Paths.get(fileFolderPath, fileName + ".TXT").toString();
			
			showLogMessage("I", "", String.format("讀取檔案[%s]", filePath));
			
			// open file
			int inputFileIndex = openInputText(filePath, "MS950");
			if (inputFileIndex == -1) {
				showLogMessage("E", "", String.format("檔案不存在: %s", "處理日期 = " + queryDate ));
				return -1;
			}

			while (true) {

				text = readTextFile(inputFileIndex);

				if (text.trim().length() != 0) {
					lineCnt++;
					
					byte[] bytesArr = text.getBytes("MS950");

					String label = CommTxBill.subByteToStr(bytesArr, 0, 1);

					if (label.equals("H"))
						continue;

					String status = CommTxBill.subByteToStr(bytesArr, 8, 14);

					if (status.equals("Failed"))
						continue;

					IcuD074Data icuD074Data = getTxt(text);

					int countIch;

					countIch = selectIchCard(icuD074Data.icashDIssueIcashPid);

					if (countIch > 0) {
						showLogMessage("E", "", String.format("該愛金卡卡號[%s]已存在愛金卡卡片資料檔中", icuD074Data.icashDIssueIcashPid));
						errFileIch.putError(icuD074Data.icashDIssueIcashPid, icuD074Data.icashDIssueCardno,
								"該愛金卡卡號已存在愛金卡卡片資料檔中");
						continue;
					}

					int countCrd;

					countCrd = selectCardNo(icuD074Data.icashDIssueCardno);

					if (countCrd <= 0) {
						showLogMessage("E", "", String.format("該信用卡卡號[%s]不存在卡片資料檔中", icuD074Data.icashDIssueCardno));
						errFileIch.putError(icuD074Data.icashDIssueIcashPid, icuD074Data.icashDIssueCardno,
								"該信用卡卡號不存在卡片資料檔中");
						continue;
					}

					String tmpNewEndDate = tmpNewEndDate(icuD074Data.icashDIssueCardno);

					String tmpOldIchCardNo = tmpOldIchCardNo(icuD074Data.icashDIssueCardno);
					
					if(icuD074Data.icashDIssueType.equals("R")) {
						updateIchCard(icuD074Data);
					}
					
					insertIchCard(icuD074Data, tmpOldIchCardNo, tmpNewEndDate);
					commitDataBase();

				}

				if (endFile[inputFileIndex].equals("Y"))
					break;

			}

			closeInputText(inputFileIndex);

			if (errFileIch.isError) {
				produceErrorFile(fileFolderPath, fileName);
				rollbackDataBase();
				
				commFTP = new CommFTP(getDBconnect(), getDBalias());
			    comr = new CommRoutine(getDBconnect(), getDBalias());
			    procFTP();
			    renameFile1(outputFileName);
			}

			moveFileToBackup(fileFolderPath, fileName);

			showLogMessage("I", "", String.format("讀取[%d]筆資料,寫入錯誤資料[%d]筆", lineCnt, errFileIch.cardNoList.size()));
			
			showLogMessage("I", "", "執行結束");
			comcr.hCallErrorDesc = "程式執行結束";
			comcr.callbatchEnd();
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}
	
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
	 * 
	 * @param ichCardNo
	 * @return the object of ActAcnoData if there are selected records; null if
	 *         there is no selected record.
	 * @throws Exception
	 */
	private int selectIchCard(String ichCardNo) throws Exception {
		selectSQL = " count(*) as ichCardNoCount ";
		daoTable = "ich_card";
		whereStr = " where ich_card_no = ? ";

		setString(1, ichCardNo);

		selectTable();

		return getValueInt("ichCardNoCount");

	}

	/**
	 * 
	 * @param card_no
	 * @return the object of ActAcnoData if there are selected records; null if
	 *         there is no selected record.
	 * @throws Exception
	 */
	private int selectCardNo(String cardNo) throws Exception {
		selectSQL = " count(*) as cardNoCount ";
		daoTable = "crd_card";
		whereStr = " where card_no = ? ";

		setString(1, cardNo);

		selectTable();

		return getValueInt("cardNoCount");

	}

	/**
	 * select tmp_old_ips_card_no
	 * 
	 * @param tmp_old_ips_card_no
	 * @return the object of ActAcnoData if there are selected records; null if
	 *         there is no selected record.
	 * @throws Exception
	 */

	private String tmpOldIchCardNo(String cardNo) throws Exception {
		sqlCmd = " with ";
		sqlCmd += " temp1 as( ";
		sqlCmd += "select old_card_no, ";
		sqlCmd += "case when old_card_no not like '' then old_card_no end as tmp_old_card_no ";
		sqlCmd += " from crd_card ";
		sqlCmd += "where card_no  = ? ";
		sqlCmd += "), ";
		sqlCmd += " temp2 as( ";
		sqlCmd += "select ich_card_no, ";
		sqlCmd += "case when ich_card_no not like '' then ich_card_no end as tmpOldIchCardNo ";
		sqlCmd += "from ich_card ";
		sqlCmd += "where card_no  = ? ";
		sqlCmd += "and crt_date in ( select max(crt_date) from ich_card where card_no  = ?) ";
		sqlCmd += "union all ";
		sqlCmd += "select ich_card_no, ";
		sqlCmd += "ich_card_no as tmpOldIchCardNo ";
		sqlCmd += "from ich_card ";
		sqlCmd += "where card_no = (select tmp_old_card_no from temp1) ";
		sqlCmd += " and crt_date in ( select max(crt_date) from ich_card where card_no = (select tmp_old_card_no from temp1)) ";
		sqlCmd += ") ";
		sqlCmd += "select * from temp2 where tmpOldIchCardNo is not null ";

		setString(1, cardNo);
		setString(2, cardNo);
		setString(3, cardNo);

		if (selectTable() > 0) {
			return getValue("tmpOldIchCardNo");
		}

		return "";

	}

	/**
	 * 
	 * @param card_no
	 * @return the object of ActAcnoData if there are selected records; null if
	 *         there is no selected record.
	 * @throws Exception
	 */
	private String tmpNewEndDate(String cardNo) throws Exception {
		selectSQL = " new_end_date as tmpNewEndDate ";
		daoTable = "crd_card";
		whereStr = " where card_no = ? ";

		setString(1, cardNo);

		selectTable();

		return getValue("tmpNewEndDate");

	}
	
	  /**
		* @ClassName: IcuD074
		* @Description: updateIchCard 將舊愛金卡卡號停用
		* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
		* @Company: DXC Team.
		* @author Wilson
		* @version V1.00.14, Nov 30, 2022
		*/
	private int  updateIchCard(IcuD074Data icuD074Data) throws Exception {
	      daoTable   = "ich_card ";
	      updateSQL  = "current_code = '4', ";
	      updateSQL += "oppost_date = ?, ";
	      updateSQL += "ich_oppost_date = ?, ";
	      updateSQL += "mod_time = sysdate, ";
	      updateSQL += "mod_pgm = ? ";
	      whereStr   = "where card_no = ? ";
	      whereStr  += "and current_code = '0' ";
	      whereStr  += "and ich_card_no <> ? ";
	      setString(1, sysDate);
	      setString(2, sysDate);
	      setString(3, javaProgram);
	      setString(4, icuD074Data.icashDIssueCardno);
	      setString(5, icuD074Data.icashDIssueIcashPid);
	      
	      int returnCode = updateTable();
	      
	      return returnCode;
	  }

	/**
	 * insert into ich_card
	 * 
	 * @param icud074Data
	 * @return the value of 0 if fail to insert;
	 * @throws Exception
	 */
	private int insertIchCard(IcuD074Data icuD074Data, String tmpOldIchCardNo, String tmpNewEndDate) throws Exception {

		switch (icuD074Data.icashDIssueType.toUpperCase(Locale.TAIWAN)) {
		case "N":
			icuD074Data.icashDIssueType = "1";
			break;
		case "C":
			icuD074Data.icashDIssueType = "3";
			break;
		case "R":
			icuD074Data.icashDIssueType = "5";
			break;

		}

		daoTable = "ich_card";

		setValue("ich_card_no", icuD074Data.icashDIssueIcashPid);
		setValue("card_no", icuD074Data.icashDIssueCardno);
		setValue("ich_emboss_rsn", icuD074Data.icashDIssueType);
		setValue("current_code", "0");
		setValue("crt_date", sysDate);
		setValue("new_beg_date", sysDate.substring(0, 6) + "01");
		setValue("new_end_date", tmpNewEndDate);
		setValue("autoload_flag", "Y");
		setValue("old_ich_card_no", tmpOldIchCardNo);
		setValueInt("autoload_amt", 500);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);

		int returnCode = insertTable();

		return returnCode;

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

		if (filePathFromDb.isEmpty() || filePathFromDb == null) {
			throw new Exception("file path selected from database is error");
		}

		String[] arrFilePathFromDb = filePathFromDb.split("/");

		projectPath=SecurityUtil.verifyPath(projectPath);
		fileFolderPath = Paths.get(projectPath).toString();
		fileFolderPath=SecurityUtil.verifyPath(fileFolderPath);
		
		for (int i = 0; i < arrFilePathFromDb.length; i++)
			fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();
		fileFolderPath=SecurityUtil.verifyPath(fileFolderPath);
		return fileFolderPath;
	}

	private IcuD074Data getTxt(String text) throws UnsupportedEncodingException {
		byte[] bytesArr = text.getBytes("MS950");

		IcuD074Data d074Data = new IcuD074Data();
		d074Data.icashDLable = CommTxBill.subByteToStr(bytesArr, 0, 1);
		d074Data.icashDSeq = CommTxBill.subByteToStr(bytesArr, 1, 7);
		d074Data.filler1 = CommTxBill.subByteToStr(bytesArr, 7, 8);
		d074Data.icashDIssueStatus = CommTxBill.subByteToStr(bytesArr, 8, 14);
		d074Data.filler2 = CommTxBill.subByteToStr(bytesArr, 14, 15);
		d074Data.icashDIssueRspCode = CommTxBill.subByteToStr(bytesArr, 15, 20);
		d074Data.filler3 = CommTxBill.subByteToStr(bytesArr, 20, 21);
		d074Data.icashDIssueDateTime = CommTxBill.subByteToStr(bytesArr, 21, 40);
		d074Data.filler4 = CommTxBill.subByteToStr(bytesArr, 40, 41);
		d074Data.icashDIssueUniqueId = CommTxBill.subByteToStr(bytesArr, 41, 55);
		d074Data.icashDIssueVersion = CommTxBill.subByteToStr(bytesArr, 55, 63);
		d074Data.icashDIssueIcashPid = CommTxBill.subByteToStr(bytesArr, 63, 79);
		d074Data.icashDIssueBasecode = CommTxBill.subByteToStr(bytesArr, 79, 81);
		d074Data.icashDIssueCardno = CommTxBill.subByteToStr(bytesArr, 81, 97);
		d074Data.icashDIssueType = CommTxBill.subByteToStr(bytesArr, 97, 98);
		d074Data.icashDIssueIsam = CommTxBill.subByteToStr(bytesArr, 98, 112);
		d074Data.filler5 = CommTxBill.subByteToStr(bytesArr, 112, 120);

		return d074Data;
	}

	private void produceErrorFile(String inputFileFolderPath, String inputFileName) throws Exception {

		// media/icu/error
		Path outputFileFolderPath = Paths.get(SecurityUtil.verifyPath(inputFileFolderPath), "error");
		
		// create the parent directory if parent the directory is not exist
		Files.createDirectories(outputFileFolderPath);

		// get output file name :MBBBBBBBB.ICBUSQND.YYMMDDNN => ICBUSQND.ERR.YYYYMMDDNN
		outputFileName = String.format("ICH_MAKECARD_RTN_ERR_%s.TXT", queryDate);
		outputFileName = SecurityUtil.verifyPath(outputFileName);

		// get output file path
		
		String outputFilePath = Paths.get(outputFileFolderPath.toString(), outputFileName).toString();
		
		//String outputFilePath = Paths.get(outputFileFolderPath.toString(), outputFileName + ".TXT").toString();

		int outFileIndex = openBinaryOutput2(outputFilePath);

		writeFile(outFileIndex);

		showLogMessage("I", "", String.format("產出錯誤報表檔: %s", outputFilePath));

		closeBinaryOutput2(outFileIndex);

	}

	private void writeFile(int outFileIndex) throws Exception, UnsupportedEncodingException {
		int size = errFileIch.ichCardNoList.size();

		for (int i = 0; i < size; i++) {
			writeFileInCertainLength(outFileIndex, errFileIch.ichCardNoList.get(i), 16);
			writeFileInCertainLength(outFileIndex, errFileIch.cardNoList.get(i), 16);
			writeFileInCertainLength(outFileIndex, errFileIch.errorReasonList.get(i), 200);
			writeFileInCertainLength(outFileIndex, sysDate, 8);
			writeBinFile2(outFileIndex, nextLineByte, nextLineByte.length);
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
		Path backupPath = Paths.get(SecurityUtil.verifyPath(fileFolderPath), "backup");
		// create the parent directory if parent the directory is not exist
		Files.createDirectories(backupPath);

		Path backupFilePath = Paths.get(backupPath.toString(), fileName + ".TXT");

		Files.move(Paths.get(fileFolderPath, fileName + ".TXT"), backupFilePath, StandardCopyOption.REPLACE_EXISTING);

		showLogMessage("I", "", String.format("移動CARDLINK愛金卡製卡回饋檔至 %s", backupFilePath.toString()));

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
	      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + outputFileName);
	      
	      if (err_code != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + outputFileName + " 資料"+" errcode:"+err_code);
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

	public static void main(String[] args) {
		IcuD074 proc = new IcuD074();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);

	}

}

class IcuD074Data {
	String icashDLable;
	String icashDSeq;
	String filler1;
	// 發卡狀態
	String icashDIssueStatus;
	String filler2;
	String icashDIssueRspCode;
	String filler3;
	// 發卡日期時間
	String icashDIssueDateTime;
	String filler4;
	// 晶片序號
	String icashDIssueUniqueId;
	String icashDIssueVersion;
	// 愛金卡卡號
	String icashDIssueIcashPid;
	String icashDIssueBasecode;
	// 信用卡卡號
	String icashDIssueCardno;
	// 製卡註記
	String icashDIssueType;
	String icashDIssueIsam;
	String filler5;

}

class ErrFileIch {
	boolean isError;
	List<String> ichCardNoList = new ArrayList<String>();
	List<String> cardNoList = new ArrayList<String>();
	List<String> errorReasonList = new ArrayList<String>();

	void putError(String ichCardNo, String cardNo, String errorReason) {
		isError = true;
		ichCardNoList.add(ichCardNo);
		cardNoList.add(cardNo);
		errorReasonList.add(errorReason);

	}
}
