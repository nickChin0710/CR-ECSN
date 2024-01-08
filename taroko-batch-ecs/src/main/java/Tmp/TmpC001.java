/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 111/04/15  V1.00.00  Justin     initial                                    *
******************************************************************************/
package Tmp;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.*;
import com.google.gson.stream.JsonReader;

import Dxc.Util.SecurityUtil;

public class TmpC001 extends AccessDAO{


private static final String DBC_CARD = "DBC_CARD";
private static final String CRD_CARD = "CRD_CARD";
private static final String progname = "Open Card Fisc 程式 111/04/15 V1.00.00";
private static final String FILE_NAME = "CARDOPENFISC";
private static final String INPUT_FOLDER = "/media/crd/";
private static final String BACKUP_FOLDER = "/media/crd/backup/";
private static final String HEADER = "IS_UPDATE, IS_AUTH, IS_ETABS, IS_EAI, CARD_NO, NEW_END_DATE, BEFORE_ACTIVATE_FLAG, BEFORE_ACTIVATE_DATE, ACTIVATE_FLAG, ACTIVATE_DATE\r\n";
CommCrd commCrd = new CommCrd();
CommFunction com = new CommFunction();
CommFTP commFTP = null;
CommRoutine comr = null;
CommString zzstr = new CommString();
private int inputRecordCnt = 0;
private int updateSuccessCnt = 0;
private int recordCntOfAFile = 0;
private final int cntPerCommit = 1000;
private final int cntPerWrite = 1000;
private boolean shouldUpdate = false;
private HashMap<String, ETABSObj> etabsMap = new HashMap<>();
private HashMap<String, EAIObj> eaiMap = new HashMap<>();
private ArrayList<String> outputBuffer = new ArrayList<>();
// ************************************************************************
	public static void main(String[] args) throws Exception {
		TmpC001 proc = new TmpC001();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
		    setConsoleMode("Y");
		    javaProgram = this.getClass().getName();
		    showLogMessage("I", "", javaProgram + " " + progname);
		    // =====================================
		   
			// 固定要做的
			if (!connectDataBase()) {
				commCrd.errExit("connect DataBase error", "");
			}
			
			loadArgs(args);
			
			processFile();
			
			if (shouldUpdate) {
				showLogMessage("I", "", String.format("總共讀取[%d]筆資料，成功更新[%d]筆卡檔資料", inputRecordCnt, updateSuccessCnt));
			}else {
				showLogMessage("I", "", String.format("總共讀取[%d]筆資料，預計更新[%d]筆卡檔資料", inputRecordCnt, recordCntOfAFile));
			}

			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		}catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess
	
	
	private void loadArgs(String[] args) throws Exception {
		if (args.length == 1) {
			//  P 產表, U 更新
			if ("P".equalsIgnoreCase(args[0]) || "U".equalsIgnoreCase(args[0])) {
				shouldUpdate = ( "U".equalsIgnoreCase(args[0])) ? true : false; 
			}else {
				commCrd.errExit("參數1：必填，模式：P(產表)、U(更新)", "");
			}
		}else {
			commCrd.errExit("參數1：必填，模式：P(產表)、U(更新)", "");
		}
	}
	
	private boolean processFile() throws Exception {
		loadETABSObj();
		loadEAIObj();
		
		String inputFileName = FILE_NAME + ".txt";
		String inputFilePath = commCrd.getECSHOME() + INPUT_FOLDER + inputFileName;
		inputFilePath = SecurityUtil.verifyPath(inputFilePath);
		
		String outputFileName = FILE_NAME + "_update." + sysDate + sysTime + ".txt";
		String outputFilePath = commCrd.getECSHOME() + INPUT_FOLDER + outputFileName;
		outputFilePath = SecurityUtil.verifyPath(outputFilePath);
		
		showLogMessage("I", "", String.format("讀取檔案[%s]", inputFilePath));
		
		// open input file
		if (doesFileExist(inputFilePath) == false) return false;
		
		setConsoleMode("N");
		int fi = openInputText(inputFilePath, "MS950");
		setConsoleMode("Y");
		if (fi == -1) return false;
		
		showLogMessage("I", "", " Process file path =[" + inputFilePath + "]");
		
		// open output file
		int outputFileIndex = openOutputText(outputFilePath, "UTF8");
		if (outputFileIndex == -1) {
			throw new Exception("無法產出TXT");
		}
		writeTextFile(outputFileIndex, HEADER);
		
		boolean isUpdateSuccess = false;
		while (true) {
			String rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;
			
			OpenCardFisc openCardFisc = generateOpenCardFisc(rec);
			inputRecordCnt++;

			if (openCardFisc.cardNo.length() > 6 && openCardFisc.cardNo.substring(0, 6).equals("460199")) {
//				isUpdateSuccess = updateCard(openCardFisc, DBC_CARD);
			} else {
				isUpdateSuccess = updateCard(openCardFisc, CRD_CARD);
			}
			
			if (isUpdateSuccess) {
				updateSuccessCnt++;
			}
			
			if (inputRecordCnt % cntPerCommit == 0) {
				showLogMessage("I", "", String.format("已讀取%d到%d筆資料", inputRecordCnt-cntPerCommit+1, inputRecordCnt));
				if (shouldUpdate) {
					commitDataBase();
				}
			}
			
			if (outputBuffer.size() % cntPerWrite == 0) {
				for (int i = 0; i < outputBuffer.size(); i++) {
					writeTextFile(outputFileIndex, outputBuffer.get(i));
					recordCntOfAFile++;
				}
				outputBuffer = new ArrayList<>();
			}
		}
		
		if (inputRecordCnt % cntPerCommit > 0) {
			commitDataBase();
			showLogMessage("I", "", String.format("已讀取%d到%d筆資料", inputRecordCnt-(inputRecordCnt%cntPerCommit)+1, inputRecordCnt));
		}
		
		for (int i = 0; i < outputBuffer.size(); i++) {
			writeTextFile(outputFileIndex, outputBuffer.get(i));
			recordCntOfAFile++;
		}
		
		closeOutputText(outputFileIndex);
		closeInputText(fi);
		
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP(outputFileName);
		renameFile1(outputFileName);
		insertFileCtl(outputFileName, recordCntOfAFile);
		
		if (shouldUpdate) {
			renameFile1(inputFileName);
		}

		return true;
	}
	
	private boolean updateCard(OpenCardFisc openCardFisc, String tableName) {
		//比較卡號+效期
		try {
			TmpC001Obj tmpC001Obj = new TmpC001Obj();
			
			String activateFlag="";
			String activateDate = "";
			String activateType="";
			
			sqlCmd =  " SELECT activate_flag, activate_type, activate_date, NEW_END_DATE ";
			sqlCmd += " FROM " + tableName;
			sqlCmd += " WHERE  card_no = ?  ";
			setString(1, openCardFisc.cardNo);
			selectTable();
			
			if (notFound == "Y") {
				showLogMessage("E", "", String.format("card_no[%s] is not found in %s", openCardFisc.cardNo, tableName));
		        return false;
		    }
			
			activateType = getValue("activate_type");
			activateFlag = getValue("activate_flag");
			activateDate = getValue("activate_date");
			
			tmpC001Obj.cardNo = openCardFisc.cardNo;
			tmpC001Obj.beforeActivateFlag = activateFlag;
			tmpC001Obj.beforeActivateDate = activateDate;
			tmpC001Obj.activateFlag = activateFlag;
			tmpC001Obj.activateDate = activateDate;
			
			String dbNewEndDate = getValue("NEW_END_DATE");
			String newEndDate = com.lastdateOfmonth("20" + openCardFisc.validDate);
			tmpC001Obj.newEndDate = dbNewEndDate;
			
			if (zzstr.ss2Num(dbNewEndDate) == zzstr.ss2Num(newEndDate) && 
					openCardFisc.openDate.equals("00000000") && 
					activateFlag != "1" && activateDate.isEmpty() == false && 
					activateDate.compareTo("20220321") < 0) {
				
//				(1) 授權交易紀錄檔若有大於等於3/21以上的開卡交易且出現已開卡E5的訊息 or 交易成功的資料則排除
//				(2) select count(*) from web_service_log where system_name='eai' and FUNCTION_NAME='EcsWsEaiPortImpl.ecscda62' and RESPONSE like '%查無資料%'
//				(3) select count from (select distinct left(right(REQUEST,69),16) as card_no from web_service_log where system_name='etabs' and EXCEPTION_MESSAGE like '%CARD_HAS_BEEN_ACTIVATED%')
				
				if (isExcluded(tmpC001Obj) == false) {
					tmpC001Obj.isUpdate = "Y";
					tmpC001Obj.activateFlag = "1";
					tmpC001Obj.activateDate = "";
					
					if (shouldUpdate) {
						boolean result = update(openCardFisc.cardNo, tableName);
						if (result == false) return false;
					}
				}
				outputBuffer.add(tmpC001Obj.convertToStr());
			} else {
				return false;
			}
		}catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	private boolean isExcluded(TmpC001Obj tmpC001Obj) throws Exception {
		if (isExcludedByTxlog(tmpC001Obj.cardNo, tmpC001Obj.newEndDate)) {
			tmpC001Obj.isAuth = "Y";
			return true;
		}
		tmpC001Obj.isAuth = "N";
		if (isExcludedByETABS(tmpC001Obj.cardNo)) {
			tmpC001Obj.isEtabs = "Y";
			return true;
		}
		tmpC001Obj.isEtabs = "N";
		if (isExcludedByEAI(tmpC001Obj.cardNo, tmpC001Obj.newEndDate)) {
			tmpC001Obj.isEai = "Y";
			return true;
		}
		tmpC001Obj.isEai = "N";
		return false;
	}
	private boolean isExcludedByTxlog(String cardNo, String newEndDate) throws Exception {
		sqlCmd = "SELECT count(*) as txlogCnt FROM CCA_AUTH_TXLOG WHERE ( AUTH_STATUS_CODE='E5' OR ISO_RESP_CODE='00' ) AND tx_date >='20220321' AND card_no = ? AND USER_EXPIRE_DATE = ? ";
		setString(1, cardNo);
		setString(2, newEndDate.substring(2,6));
		selectTable();
		long cnt = getValueLong("txlogCnt");
		return cnt > 0;
	}
	
	private boolean isExcludedByETABS(String cardNo) throws Exception {
		//ETABS
		return etabsMap.containsKey(cardNo);
	}
	
	private boolean isExcludedByEAI(String cardNo, String newEndDate) throws Exception {
		//EAI
		if (eaiMap.containsKey(cardNo) == false) return false;
		EAIObj obj = eaiMap.get(cardNo);
		sqlCmd = "SELECT ID_NO, BIRTHDAY FROM CRD_IDNO WHERE ID_P_SEQNO IN ( SELECT ID_P_SEQNO FROM CRD_CARD WHERE CARD_NO = ? ) ";
		setString(1, cardNo);
		int cnt = selectTable();
		if (cnt > 0) {
			String idNoFromDB = getValue("ID_NO");
			String birthdayFromDB = getValue("BIRTHDAY");
			// crd_idno AND file的idNo、birthday、newEndDate與expireDate一致 => return true
			return obj.customerIDSet.contains(idNoFromDB) && obj.birthDate.equals(birthdayFromDB) && ( newEndDate.length() >= 4 && obj.expireDate.equals(newEndDate.substring(4)) );
		}
		return false;
	}
	
	private void loadETABSObj() throws Exception {
		sqlCmd = "select request from web_service_log where system_name='etabs' AND EXCEPTION_MESSAGE like '%CARD_HAS_BEEN_ACTIVATED%' ";
		int cursorIndex = openCursor();
	    while (fetchTable(cursorIndex)) {
	    	ETABSObj obj = getEtabsRequest(getValue("request"));
	    	etabsMap.put(obj.cardNo, obj);
	    }
	    closeCursor(cursorIndex);
	}
	
	private void loadEAIObj() throws Exception {
		sqlCmd = "select request from web_service_log where system_name='eai' AND FUNCTION_NAME='EcsWsEaiPortImpl.ecscda62' AND RESPONSE like '%查無資料%' ";
		int cursorIndex = openCursor();
	    while (fetchTable(cursorIndex)) {
	    	EAIObj obj = getEaiRequest(getValue("request"));
	    	eaiMap.put(obj.cardNo, obj);
	    }
	    closeCursor(cursorIndex);
	}
	
	ETABSObj getEtabsRequest(String jsonStr) throws IOException {
		ETABSObj obj = new ETABSObj();
		try (JsonReader reader = new JsonReader(new StringReader(jsonStr));) {
            reader.beginObject(); // throws IOException
            reader.skipValue(); // skip request
            reader.beginArray();
            reader.beginObject();
            while (reader.hasNext()) {
            	if ("cardNo".equals(reader.nextName())) {
            		obj.cardNo = reader.nextString();
            		break;
            	}else {
            		reader.skipValue();
            	}
            }
        }
		return obj;
	}
	
	EAIObj getEaiRequest(String jsonStr) throws IOException {
		EAIObj obj = new EAIObj();
		try (JsonReader reader = new JsonReader(new StringReader(jsonStr));) {
            reader.beginObject(); // throws IOException
            reader.skipValue(); // skip request
            reader.beginArray();
            reader.beginObject();
            int requiredCol = 4;
            while (reader.hasNext()) {
            	String name = reader.nextName();
            	switch(name) {
            	case "cardNo":
            		obj.cardNo = reader.nextString();
            		requiredCol--;
            		break;
            	case "birthDate":
            		obj.birthDate = reader.nextString();
            		requiredCol--;
            		break;
            	case "customerID":
            		obj.customerIDSet.add(reader.nextString());
            		requiredCol--;
            		break;
            	case "expireDate":
            		obj.expireDate = reader.nextString();
            		requiredCol--;
            		break;
            	default:
            		reader.skipValue();
            		break;
            	}
            	if (requiredCol == 0) break;
            }
        }
		return obj;
	}
	

	
	private boolean update(String cardNo, String tableName) throws Exception {
		daoTable = tableName;
		updateSQL = " activate_date = ? ";
		updateSQL += ", activate_flag = ? ";
		updateSQL += ", activate_type = ? ";
		updateSQL += ", mod_time = sysdate ";
		updateSQL += ", mod_pgm = ? ";
		whereStr = "WHERE card_no = ? ";
		setString(1, "");
		setString(2, "1");
		setString(3, "V");
		setString(4, javaProgram);
		setString(5, cardNo);

		updateTable();

		if (notFound == "Y") {
			showLogMessage("E", "", String.format("card_no[%s] update not found in %s", cardNo, tableName));
			return false;
		}
		
		return true;
	}
	
	private boolean doesFileExist(String inputFilePath) {
		File file = new File(inputFilePath);
		if (file.exists() == false) {
			showLogMessage("I", "", "無檔案可處理");
			return false;
		}
		return true;
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
		String tmpstr1 = commCrd.getECSHOME() + INPUT_FOLDER + removeFileName;
		String tmpstr2 = commCrd.getECSHOME() + BACKUP_FOLDER + removeFileName + "." + sysDate + sysTime;
		
		if (commCrd.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");

	}

	
	int procFTP(String outputFileName) throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/%s", commCrd.getECSHOME(), INPUT_FOLDER);
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
	
	class OpenCardFisc {
		String cardNo = "";     // 卡號
		String validDate = "";  // 有效期限
		String openDate = "";   // 開卡日期
		String cardStatus = ""; // 卡片狀態
//		String atmErrCnt;  // 金融卡鎖卡次數

	}
	
	OpenCardFisc generateOpenCardFisc(String row) throws UnsupportedEncodingException {
		OpenCardFisc openCardFisc = new OpenCardFisc();
//		byte[] bytes = row.getBytes("MS950");
//		openCardFisc.cardNo = commCrd.subMS950String(bytes, 1, 16);
//		openCardFisc.validDate = commCrd.subMS950String(bytes, 20, 4);
//		openCardFisc.openDate = commCrd.subMS950String(bytes, 27, 8);
//		openCardFisc.cardStatus = commCrd.subMS950String(bytes, 38, 1);
//		openCardFisc.atmErrCnt = comc.subMS950String(bytes, 41, 1);
		String[] strArr = row.split(",");
		if (strArr.length == 4) {
			openCardFisc.cardNo = strArr[0].trim();
			openCardFisc.validDate = strArr[1].trim();
			openCardFisc.openDate = strArr[2].trim();
			openCardFisc.cardStatus = strArr[3].trim();
		}

		return openCardFisc;
	}
	
	class TmpC001Obj {
		private final byte[] lineSeparatorBytes =  {0x0D , 0x0A};
		String isUpdate = "N";
		String isAuth = " ";
		String isEtabs = " ";
		String isEai = " ";
		String cardNo = "";
		String newEndDate = "";
		String beforeActivateFlag = "";
		String beforeActivateDate = "";
		String activateFlag = "";
		String activateDate = "";
		public String convertToStr() throws UnsupportedEncodingException {
			StringBuilder sb = new StringBuilder();
			sb.append(isUpdate).append(",")
			  .append(isAuth).append(",")
			  .append(isEtabs).append(",")
			  .append(isEai).append(",")
			  .append(cardNo).append(",")
			  .append(newEndDate).append(",")
			  .append(String.format("%1s", beforeActivateFlag)).append(",")
			  .append(String.format("%1s", beforeActivateDate)).append(",")
			  .append(String.format("%1s", activateFlag)).append(",")
			  .append(String.format("%1s", activateDate))
			  .append(new String(lineSeparatorBytes, "MS950"));
			return sb.toString();
		}	
	}
	
	class ETABSObj {
		String cardNo = "";
	}
	
	class EAIObj {
		String cardNo = "";
		String birthDate = "";
		String expireDate = "";
		HashSet<String> customerIDSet = new HashSet<>();
	}


}  // End of class FetchSample

