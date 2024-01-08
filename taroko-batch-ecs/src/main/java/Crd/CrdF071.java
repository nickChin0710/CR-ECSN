/*******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/08/17  V1.00.00    Pino       program initial                          *
*  109-09-04  V1.02.01  yanghan     处理Portability Flaw: File Separator 问题          *
*  109/09/04  V1.03.01   Wilson     換行符號 -> "\r\n"(0D0A)                     *    
*  109/09/04  V1.00.06    Zuwei     code scan issue                           *
*  109/09/12  V1.00.07   Wilson     新增產生ICPSMQNA屬性檔                                                                   *
*  109/09/14  V1.00.08   Wilson     新增procFTP                                * 
*  109/10/14  V1.00.09   Wilson     LOCAL_FTP_PUT -> NCR2TCB                  *
*  109-10-19  V1.00.10   shiyuqi       updated for project coding standard     *
*  109/11/09  V1.00.11   Wilson     NCR2TCB -> FISC_FTP_PUT                   *
*  110/02/01  V1.00.12   Wilson     變更檔名並新增TEMPFILE檔                                                               *
*  110/02/02  V1.00.13   Justin       use a parameter selected from PTR_SYS_PARM
*  110/12/01  V1.00.14   Wilson     無資料不產生檔案                                    
*  111/01/04  V1.00.15   Justin     combine CrdF071 and IcuD017               *
*  111/01/11  V1.00.16   Justin     update id_no of credit card               *
*                                   insert dbc_chg_id: acct_no = ""           *
*  111/01/22  V1.00.17   Justin     新增update block code 、停卡原因          *
*  111/01/24  V1.00.18   Justin     fix a bug of spec_time                    *
*  111/01/28  V1.00.19   Justin     split the file if the record exceeds 5000 *
*  111/02/10  V1.00.20   Justin     刪除開卡、卡況有異動的資料、              *
*                                   資料檔的開卡日期跟停用碼都改成放空白      *
*  111/02/13  V1.00.21   Justin     rename and insert the file with CLK into  *
*                                   crd_file_ctl                              *
*  111/02/14  V1.00.22   Justin     sort files by their modified dates        *
*  111/02/14  V1.00.23    Ryan      big5 to MS950                                           *
*  111/02/22  V1.00.24   Justin     新增加參數                                *
*  111/03/02  V1.00.25   Justin     修改T Code oppost_reason 04, 32, and 35歸類 *
*                                   修改E Code B1、B2 直接改為A2寫入          *
*  111/03/03  V1.00.26   Justin     修改T Code oppost_reason 32 為 特指       *
*                                   修改E Code oppost_reason 15, 44, 07, 05   *
*  111/03/08  V1.00.27   Justin     調整Code處理邏輯                          *
*  111/03/23  V1.00.28   Justin     未提供參數，讀取前一天所有異動電話資料    *
*  111/04/01  V1.00.29   Justin     將CrdF071的IcuD17併入CrdF074              *
*  111/04/06  V1.00.30   Justin     修改TEMPFILE檔名                          *
*  111/04/12  V1.00.31   Justin     新增A、B、F、L、S、O code                 *
*  111/04/13  V1.00.32   Justin     add ERR13                                 *
*                                   將ErrorReason併入CrdF074                  *
*  111/04/21  V1.00.33   Justin     修改input參數                             *
*  111/04/29  V1.00.34   Justin     mark some log messages                    *
*  111/05/03  V1.00.35   Justin     add class variables                       *
*  111/05/20  V1.00.36   Justin     調整3D簡訊發送問題                        *
*  111/06/10  V1.00.37   Justin     print cellar_phone for debugging          *
******************************************************************************/

package Crd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommSecr;
import com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException;


import com.CommRoutine;

import Dxc.Util.SecurityUtil;


public class CrdF071 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;
    
    private static final int RECORD_CNT_PER_FILE = 5000;

    private final String progname = "產生送財金PRISM資料檔 111/06/10  V1.00.37";
    private final String OUTPUT_DATA_FILE = "PSMQD";
    private final String OUTPUT_ATTR_FILE = "PSMQA";
    private final String OUTPUT_TEMP_FILE = "TEMPFILE";
    
    private String outputTempFileName = "";
    
    private final String INPUT_FILE = "ICPSMQND";
    
    private final String ISSUE_UNIT_CODE = "M00600000";
    private final String ISSUE_UNIT = "00600000";
    private final String FISC_UNIT = "95000000";

    private String hPreviousBusiBusinessDate = "";
    private String hBusiBusinessDate = "";
    private boolean doesProdFile = true;
    private String queryDate = "";
    int hMinute = 0;
    private boolean isMinProvided = false;
    
    Map<String, ICPSMQND> psmqdMap = new HashMap<String, ICPSMQND>();
    
    // =====IcuD017=====================
	private static final String DBC_IDNO = "DBC_IDNO";
	private static final String CRD_IDNO = "CRD_IDNO";
	
    private int totalInputData = 0;
    
    private List<CrdFileCtlObj> icud017InputFileNames = new ArrayList<CrdFileCtlObj>();
    private List<CrdFileCtlObj> icud017ErrorFileNames = new ArrayList<CrdFileCtlObj>();
    
    private String errFileName = "";
    private String errFilePath = "";
    
    
	protected final String dT1Str = "card_no, activate_date, col3, col4, col5, col6, col7, col8, col9, col10, col11, "
			+ "id_no, birthday, col14, col15, col16, col17, col18, col19, col20, col21, col22, col23, "
			+ "col24, cellar_phone, col26, col27, col28, col29, col30, col31, col32, col33, col34, col35, col36, "
			+ "col37, col38, col39, col40, col41, col42, col43, col44, col45, col46, col47, col48, col49, "
			+ "mod_no, col51, col52, col53, col54, col55, col56 ";

	protected final int[] dt1Length = { 19, 8, 14, 14, 14, 8, 14, 14, 12, 19, 40, 20, 8, 40, 40, 40, 40, 40, 30, 30, 3,
			16, 20, 20, 20, 40, 30, 2, 2, 8, 8, 8, 8, 8, 8, 8, 2, 3, 1, 1, 1, 1, 4, 1, 14, 14, 14, 8, 6, 1, 1, 30, 4, 8,
			7, 4 };
	
	ErrorReason errCode;
	
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
	
    
    // =================================
    
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommSecr comsecr = null;
    CommFTP commFTP = null;
    CommRoutine comr = null;
    

    int debug = 0;

    String prgmId = "CrdF071";
    String hModUser = "";
    String hSystemDate = "";
    String hModPgm = "";
    int totalRecordCnt = 0;
    
    BufferedWriter outputDataBufWri = null;
    BufferedWriter outputAttrBufWri = null;
    BufferedWriter outputTempBufWri = null;
    String outputDataFilePath = "";
    String outputAttrFilePath = "";
    String outputTempFilePath = "";
	String outputDataFileName = "";
	String outputAttrFileName = "";
	ArrayList<CrdFileCtlObj> outputDataFileCtlList = new ArrayList<CrdFileCtlObj>();
	ArrayList<CrdFileCtlObj> outputAttrFileCtlList = new ArrayList<CrdFileCtlObj>();
	
	String rptName1 = "";

    int rptSeq1 = 0;
    int icud017ErrorFileRecordCnt = 0;
    int errFileNo = 0;

	private boolean doCheckSupFlag = false;
    

public int mainProcess(String[] args) 
{
 try 
  {
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

	hModUser = comc.commGetUserID();

	hModPgm = javaProgram;
   
	/*** 取得系統日期 ***/
	commonRtn();

	/*** 讀取Args ***/
	loadArgs(args);
	
	// (舊IcuD017)
    // 找出CARDLINK手機異動資料檔
    // 並記錄M00600000.ICPSMQND.YYMMDDNN檔案數
	// 2022/04/01 Justin merge the code below into CrdF074
//  showLogMessage("I", "", "找出CARDLINK手機異動資料檔(舊IcuD017)");
//	int icud017InputFileCnt = runIcuD017();
//	showLogMessage("I", "", String.format("CARDLINK手機異動資料檔(舊IcuD017)：處理完成[%d]個檔案及[%d]筆資料", icud017InputFileCnt, totalInputData));
	
//	// 判斷是否有M00600000.ICPSMQND.YYMMDDNN
//	// 若有，則讀取「前一天有異動卡況或開卡的卡號」
//	if (icud017InputFileCnt > 0) {
//		showLogMessage("I", "", "處理M00600000.ICPSMQND.YYMMDDNN檔案完成，開始讀取「前一天有異動卡況或開卡的卡號」");
//		selectCardsModOrOpenYesterday();
//	}else {
//		showLogMessage("I", "", "無M00600000.ICPSMQND.YYMMDDNN資料可處理");
//	}

	// 讀取「手機號碼異動資料」
    String chgTime = getCrdF071ChgTime();
    showLogMessage("I", "", "讀取「手機號碼異動資料」");
    String maxChgTime = selectCmsChgcolumnLog(chgTime);
   
	if (maxChgTime != null) {
		updateCrdF071ChgTime(maxChgTime);
	}else {
		showLogMessage("I", "", "CmsChgcolumnLog查無「手機號碼異動資料」");
	}
	
	// 是否產檔
	if (doesProdFile) {
		/*** 開啟產生檔案 ***/
	    checkOpen();
		
		createPsmqD();
	   
		// 改到PsmqD裡面去做
	    // createPsmqA();
	   
	    createTempfile();

	   /*** 關閉檔案 ***/
	   if (outputDataBufWri != null)
	       outputDataBufWri.close();
	   
	   if (outputAttrBufWri != null)
	       outputAttrBufWri.close();
	   
	   if (outputTempBufWri != null)
	       outputTempBufWri.close();
	}
	
	// 2022/04/01 Justin merge the code below into CrdF074
//	int totalErrorCnt = 0;
//   // move icud017 error files
//   for (int i = 0; i < icud017ErrorFileNames.size(); i++) {
//		CrdFileCtlObj errFileObj = icud017ErrorFileNames.get(i);
//		insertFileCtl(errFileObj);
//		procFTPErrorFile(errFileObj.fileName);
//	    renameErrFile(errFileObj.fileName);
//	    totalErrorCnt += errFileObj.recordCnt;
//	}
//   
//	// move icud017 input files 
//	for (int i = 0; i < icud017InputFileNames.size(); i++) {
//		CrdFileCtlObj fileObj = icud017InputFileNames.get(i);
//		String orgFileName = fileObj.fileName;
//		// 2022/02/13 Justin 避免與CrdF074產出的檔案衝突，因此加上CLK來做區別
//		fileObj.fileName = renameToCardLinkFileName(orgFileName);
//		insertFileCtl(fileObj);
//		renameFile(orgFileName, fileObj.fileName);
//	}
	
	int okFileCnt = 0;
	
	if (doesProdFile) {
		// 無資料須傳輸，因此刪除檔案
		   if(totalRecordCnt==0) {
				String path = String.format("%s/media/icu/out/%s", comc.getECSHOME(), outputDataFileName);
				path = SecurityUtil.verifyPath(path);
				File file = new File(path);
				file.delete();

				String path1 = String.format("%s/media/icu/out/%s", comc.getECSHOME(), outputAttrFileName);
				path1 = SecurityUtil.verifyPath(path1);
				File file1 = new File(path1);
				file1.delete();

				String path2 = String.format("%s/media/icu/out/%s", comc.getECSHOME(), outputTempFileName);
				path2 = SecurityUtil.verifyPath(path2);
				File file2 = new File(path2);
				file2.delete();
		   }else {
				commFTP = new CommFTP(getDBconnect(), getDBalias());
				comr = new CommRoutine(getDBconnect(), getDBalias());

				okFileCnt = procFTP();

				renameOutputFile();
				
				insertOutputFileCtl();

		   }
	}
	
	// 2022/04/01 Justin merge the code below into CrdF074
//   showLogMessage("I","",String.format("產出錯誤報表[%d]，錯誤資料總筆數[%d]", icud017ErrorFileNames.size(), totalErrorCnt));
   showLogMessage("I","",String.format("送出檔案[%d]，資料總筆數[%d]", okFileCnt, totalRecordCnt));
   
   // ==============================================
   // 固定要做的
   // comcr.callbatch(1, 0, 0);
   showLogMessage("I", "", "執行結束");
   finalProcess();
   return 0;
  } catch (Exception ex) 
      { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
      }
}

private void insertOutputFileCtl() throws Exception {
	
	for (int i = 0; i < outputDataFileCtlList.size(); i++) {
		insertFileCtl(outputDataFileCtlList.get(i));
		insertFileCtl(outputAttrFileCtlList.get(i));
	}

	CrdFileCtlObj fileTempObj = new CrdFileCtlObj(outputTempFileName);
	fileTempObj.recordCnt = outputDataFileCtlList.size() * 2;
	insertFileCtl(fileTempObj);
	
}

private void renameOutputFile() throws Exception {
	for (int i = 0; i < outputDataFileCtlList.size(); i++) {
		String outputDataFileName =  outputDataFileCtlList.get(i).fileName;
		renameFile1(outputDataFileName);
		String outputAttrFileName =  outputAttrFileCtlList.get(i).fileName;
		renameFile1(outputAttrFileName);
	}
	renameFile2(outputTempFileName);
	
}
//
//private String convertCurrentCodeToStopCode(String currentCode) {
//	switch (currentCode) {
//	case "0":
//		return "";
//	case "1":
//		return "Q";
//	case "2":
//		return "L";
//	case "3":
//		return "C";
//	case "4":
//		return "U";
//	case "5":
//		return "F";
//	default:
//		return null;
//	}
//}

private String convertToYYMM(String date, String cardNo) {
	if (date != null && date.length() == 8) {
		return date.substring(2, 6);
	}
	showLogMessage("E", "", String.format("card_no[%s] new_end_date[%s]格式有誤", cardNo, date));
	return "";
}

private int runIcuD017() throws Exception {
	return openFileIcuD017();
}

private int openFileIcuD017() throws Exception {
	int fileCount = 0;

	String tmpstr = String.format("%s/media/icu", comc.getECSHOME());

	List<String> listOfFiles = comc.listFsSort(tmpstr);

	String fileNameTemplate  = ""; // String fileNameTemplate
	String fileNameTemplate2 = ""; // previous business date

	if (queryDate.length() > 0) {
		fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
				ISSUE_UNIT_CODE, 
				INPUT_FILE,
				new CommDate().getLastTwoTWDate(queryDate), 
				queryDate.substring(4, 8)); // 檔案正規表達式
		
		showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate));
	}else {
		fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
				ISSUE_UNIT_CODE, 
				INPUT_FILE,
				new CommDate().getLastTwoTWDate(hBusiBusinessDate), 
				hBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
		
		fileNameTemplate2 = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
				ISSUE_UNIT_CODE, 
				INPUT_FILE,
				new CommDate().getLastTwoTWDate(hPreviousBusiBusinessDate), 
				hPreviousBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
		
		showLogMessage("I", "", String.format("尋找檔案[%s] 或", fileNameTemplate));
		showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate2));
	}

	ArrayList<String> matchedFileList = new ArrayList<String>();
	
	for (String file : listOfFiles) {
		if (file.matches(".*\\.bak"))
			continue;
		if ( !( file.matches(fileNameTemplate) || ( fileNameTemplate2.length() > 0 && file.matches(fileNameTemplate2) ) ))
			continue;
		if (checkFileCtlIcuD017(file) != 0)
			continue;
		
		matchedFileList.add(file);
		
	}
	
	for (int i = 0; i < matchedFileList.size(); i++) {
		fileCount++;
		readFileIcuD017(matchedFileList.get(i));
	}
	
	if (fileCount < 1) {
		showLogMessage("I", "", "無檔案可處理");
	}
	
	return (fileCount);
}
	
private int readFileIcuD017(String fileName) throws Exception {
	String inputFilePath = comc.getECSHOME() + "/media/icu/" + fileName;

	// check whether the file exist
	int f = openInputText(inputFilePath);
	if (f == -1) {
		return 1;
	}
	closeInputText(f);
	
	// open the file
	setConsoleMode("N");
	int fi = openInputText(inputFilePath, "MS950");
	setConsoleMode("Y");
	if (fi == -1) {
		return 1;
	}

	showLogMessage("I", "", " Process file path =[" + comc.getECSHOME() + "/media/icu ]");
	showLogMessage("I", "", " Process file =[" + fileName + "]");

	int recordCntOfAFile = 0;
	while (true) {
		String rec = readTextFile(fi); // read file data
		if (endFile[fi].equals("Y"))
			break;
		recordCntOfAFile++;
		totalInputData++;
		Map<String, Object> dataMap = processDataRecord(getFieldValueIcuD017(rec, dt1Length), dT1Str.split(","));
		TempICPSMQND inputObj = moveData(dataMap);
		
		inputObj.tmpIdNo = (String) dataMap.get("id_no"); // 客戶代號
		String extendCode = inputObj.tmpIdNo.substring(10, 11); // 延伸碼
		inputObj.tmpIdNo = inputObj.tmpIdNo.substring(0, 10);
		String idPSeqnoSelectedByTxtCardNo =  (String)dataMap.get("idPSeqnoSelectedByTxtCardNo");
		
		if (checkExtendCode(extendCode, inputObj)) {
			
			switch (inputObj.tmpCardType) {
			
			case "VD":
				System.out.println(String.format("檢查碼為:%s", (String) dataMap.get("mod_no")));
				
				// check 異動碼是否為C
				if ( "C".equalsIgnoreCase( (String)dataMap.get("mod_no") ) ) {	
					DbcDataD017 dbaData = getDbcData(inputObj.tmpCardNo);
					if (dbaData != null) {
						if ( isIdNoChg(dbaData.idNo, inputObj.tmpIdNo) ) {
							log(String.format("Update id_no. [id_p_seqno = %s]", dbaData.idPSeqno));
							boolean isupdateOk = updateIdNo(inputObj.tmpIdNo, dbaData, inputObj);
							if (isupdateOk == false) continue;
							
						}
					}
				}
				
				if (isDbcIdnoExist(inputObj)) {
					if (checkIdno(DBC_IDNO, inputObj, idPSeqnoSelectedByTxtCardNo) == false) {
						rollbackDataBase();
						continue;
					}
					updateDbcIdnoCellPhoneAndBirthday(inputObj);
					updateOppstReasonSpecStatusAndAndCurrentCode(inputObj);
				}
				break;
				
			default:
				if (isCrdIdnoExist(inputObj)) {
					if (checkIdno(CRD_IDNO, inputObj, idPSeqnoSelectedByTxtCardNo) == false) {
						continue;
					}
					updateOppstReasonSpecStatusAndAndCurrentCode(inputObj);
				}
				break;
				
			}
		}	
		commitDataBase();
		processDisplay(1000);
		
	}

	if (icud017ErrorFileRecordCnt > 0) {
		outPutErrFile();
		comc.writeReport(errFilePath, lpar1, "MS950");
		lpar1.clear();
		
//		insertFileCtl(errFileName);
//		procFTPErrorFile(errFileName);
//	    renameErrFile(errFileName);
		
		CrdFileCtlObj crdErrorFileCtlObj = new CrdFileCtlObj(errFileName);
		crdErrorFileCtlObj.recordCnt = icud017ErrorFileRecordCnt;
	    icud017ErrorFileNames.add(crdErrorFileCtlObj);
	    
	    icud017ErrorFileRecordCnt = 0;
	}

	closeInputText(fi);

	CrdFileCtlObj fileObj = new CrdFileCtlObj(fileName);
	fileObj.recordCnt = recordCntOfAFile;
	icud017InputFileNames.add(fileObj);
	
//	insertFileCtl1(fileName);
//
//	renameFile(fileName);
	
	return 0;
}

private boolean updateOppstReasonSpecStatusAndAndCurrentCode(TempICPSMQND inputObj) throws Exception {
	EcsCardStatus ecsCardStatus = EcsCardStatus.getEcsCardStatus(inputObj.blockCode, inputObj.reasonCode);
	if (ecsCardStatus == null) {
		showLogMessage("I", "", String.format("控管碼[%s], 理由代碼[%s]此程式不處理", inputObj.blockCode, inputObj.reasonCode));
		return true;
	}
	switch(inputObj.blockCode) {
	case "T":
		switch(ecsCardStatus.cardClass) {
		case "1": //凍結(戶)
			updateBlockReason(inputObj, ecsCardStatus);
			break;
		case "2": //特指(卡)
			updateSpecStatus(inputObj, ecsCardStatus);
			break;
		}
		break;
	case "E":
	case "X":
		updateCurrentCodeAndOppostReason(inputObj, ecsCardStatus);
		break;
	default:
		break;
	}
	return true;
}

/****************************************************************************/

private void updateBlockReason(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {

	daoTable = "CCA_CARD_ACCT";
	updateSQL =  " BLOCK_DATE  = ?,";
	updateSQL += " BLOCK_REASON1  = ? ,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
    setString(1, sysDate);
    setString(2, ecsCardStatus.blockReasonOrSpecStatus);
    setString(3, prgmId);
    setString(4, sysDate + sysTime);
    
    switch(inputObj.tmpCardType) {
	case "VD":
		whereStr   = " where ACNO_P_SEQNO = (select P_SEQNO from DBC_CARD where card_no = ? ) ";  
		break;
	default:
		whereStr   = " where ACNO_P_SEQNO = (select ACNO_P_SEQNO from CRD_CARD where card_no = ? ) ";  
		break;
	}

    setString(5, inputObj.tmpCardNo);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", String.format("update CCA_CARD_ACCT not found, card_type[%s], card_no[%s]", inputObj.tmpCardType, inputObj.tmpCardNo));
	}
	
}

/****************************************************************************/

private void updateSpecStatus(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {

	daoTable = "CCA_CARD_BASE";
	updateSQL =  " SPEC_FLAG = 'Y' ,";
	updateSQL += " SPEC_STATUS  = ? ,";
	updateSQL += " SPEC_DATE  = ? ,";
	updateSQL += " SPEC_TIME  = ? ,";
	updateSQL += " SPEC_USER  = ? ,";
	updateSQL += " MOD_USER  = ? ,";
    updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
    updateSQL += " MOD_PGM  = ? ";
    whereStr   = " where CARD_NO = ? ";
    setString(1, ecsCardStatus.blockReasonOrSpecStatus);
    setString(2, sysDate);
    setString(3, sysTime);
    setString(4, prgmId);
    setString(5, prgmId);
    setString(6, sysDate + sysTime);
    setString(7, prgmId);
    setString(8, inputObj.tmpCardNo);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", String.format("update CCA_CARD_BASE not found, card_type[%s], card_no[%s]", inputObj.tmpCardType, inputObj.tmpCardNo));
	}
	
}

/****************************************************************************/

private void updateCurrentCodeAndOppostReason(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
    switch(inputObj.tmpCardType) {
	case "VD":
		daoTable = "DBC_CARD";
		break;
	default:
		daoTable = "CRD_CARD";  
		break;
	}
	
	updateSQL =  " CURRENT_CODE = ? ,";
	updateSQL += " OPPOST_DATE  = ? ,";
	updateSQL += " OPPOST_REASON  = ? ,";
	updateSQL += " MOD_USER  = ? ,";
	updateSQL += " MOD_TIME  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
	updateSQL += " MOD_PGM  = ? ";
    whereStr   = " where CARD_NO = ? ";
    setString(1, ecsCardStatus.currentCode);
    setString(2, sysDate);
    setString(3, ecsCardStatus.oppostReason);
    setString(4, prgmId);
    setString(5, sysDate + sysTime);
    setString(6, prgmId);
    setString(7, inputObj.tmpCardNo);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", String.format("update updateCurrentCodeAndOppostReason not found, card_type[%s], card_no[%s]", inputObj.tmpCardType, inputObj.tmpCardNo));
	}
	
}

/****************************************************************************/
void renameFile(String srcFileName, String desFileName) throws Exception {
	String tmpstr1 = comc.getECSHOME() + "/media/icu/" + srcFileName;
	String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + desFileName + "." + sysDate;

	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
		showLogMessage("I", "", "ERROR : 檔案[" + srcFileName + "]更名失敗!");
		return;
	}
	showLogMessage("I", "", "檔案 [" + srcFileName + "] 已移至 [" + tmpstr2 + "]");
}

/****************************************************************************/
void procFTPErrorFile(String errFileName) throws Exception {
	  commFTP = new CommFTP(getDBconnect(), getDBalias());
	  comr = new CommRoutine(getDBconnect(), getDBalias());
	  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEflgModPgm = javaProgram;
      

      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
      showLogMessage("I", "", "mput " + errFileName + " 開始傳送....");
      int err_code = commFTP.ftplogName("NCR2EMP", "mput " + errFileName);
      
      if (err_code != 0) {
          showLogMessage("I", "", "ERROR:無法傳送 " + errFileName + " 資料"+" errcode:"+err_code);
          insertEcsNotifyLog(errFileName);          
      }
  }

/***********************************************************************/
int outPutErrFile() throws Exception {
	
	// 第一個input檔從DB撈最新的錯誤報表編號
	// 若有兩個以上的input檔，則讀取完第一個input檔後，直接接續前一個錯誤報表編號
	if (errFileNo == 0) {
		sqlCmd =  "select max(substr(file_name, 22, 2)) file_no";
		sqlCmd += " from crd_file_ctl  ";
		sqlCmd += " where file_name like ?";
		sqlCmd += "  and crt_date  = ? ";
		setString(1, "ICPSMQND.ERR." + "%" + ".TXT");
		setString(2, sysDate);
		if (selectTable() > 0)
			errFileNo = getValueInt("file_no") + 1;
	}else {
		errFileNo++;
	}

	errFileName = String.format("ICPSMQND.ERR.%s%02d.TXT", queryDate, errFileNo);
	showLogMessage("I", "", "Output Filename = [" + errFileName + "]");

	errFilePath = String.format("%s/media/icu/error/%s", comc.getECSHOME(), errFileName);
	errFilePath = Normalizer.normalize(errFilePath, java.text.Normalizer.Form.NFKD);
	showLogMessage("I", "", "Output Filepath = [" + errFilePath + "]");

	return 0;
}

/***********************************************************************/

void updateCrdIdno(TempICPSMQND inputObj) throws Exception {

	daoTable = CRD_IDNO;
	updateSQL = " cellar_phone  = ?,";
	updateSQL += " mod_user  = 'IcdD017',";
	updateSQL += " mod_pgm  = 'IcdD017',";
	updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
	whereStr = " where id_no = ? ";
	setString(1, inputObj.tmpCellarPhone);
	setString(2, sysDate + sysTime);
	setString(3, inputObj.tmpIdNo);

	updateTable();

	if (notFound.equals("Y")) {
		comcr.errRtn("update_crd_idno not found! ", inputObj.tmpIdNo, "");
	}

	return;
}

/**********************************************************************/

boolean isCrdIdnoExist(TempICPSMQND inputObj) throws Exception {

	sqlCmd = "select count(*) tmpInt ";
	sqlCmd += "from crd_idno ";
	sqlCmd += "where id_no = ? ";
	setString(1, inputObj.tmpIdNo); // 客戶代號前10碼

	selectTable();

	int tmpInt = getValueInt("tmpInt");
	if (tmpInt <= 0) {
		showLogMessage("I", "", "Error : id_no = [" + inputObj.tmpIdNo + "]，客戶代號不存在信用卡人主檔");
		putErrReportList(ErrorReason.ERR3, inputObj);
		return false;
	}

	return true;
}

/**********************************************************************/

String getChiNameFromCrdIdno(String idPSeqno) throws Exception {

	sqlCmd =  "select chi_name ";
	sqlCmd += "from crd_idno ";
	sqlCmd += "where id_p_seqno = ? ";
	setString(1, idPSeqno); // 客戶代號前10碼

	int cnt = selectTable();

	if (cnt <= 0) {
		showLogMessage("I", "", "getChiNameFromCrdIdno CRD_IDNO[id_no]: Not found! id_p_seqno = [" + idPSeqno + "]");
		return null;
	}

	return getValue("chi_name");
}

/***********************************************************************/
void updateDbcIdnoCellPhoneAndBirthday(TempICPSMQND inputObj) throws Exception {

	daoTable = DBC_IDNO;
	updateSQL =  " cellar_phone  = ?,";
	updateSQL += " birthday  = ? ,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_pgm  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
    whereStr   = " where id_no = ? ";      
    setString(1, inputObj.tmpCellarPhone);
    setString(2, inputObj.tmpBirthDay);
    setString(3, prgmId);
    setString(4, prgmId);
    setString(5, sysDate + sysTime);
    setString(6, inputObj.tmpIdNo);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "updateDbcIdnoCellPhoneAndBirthday not found! ");
	}

	return;
}

/***********************************************************************/

private boolean checkIdno(String idNoTable, TempICPSMQND inputObj, String idPSeqnoSelectedByTxtCardNo) throws Exception {
	boolean isVDCard = false;
	if (DBC_IDNO.equalsIgnoreCase(idNoTable)) {
		idNoTable = DBC_IDNO;
		isVDCard = true;
	}else {
		idNoTable = CRD_IDNO;
	}
	
	sqlCmd =    "select id_no ";
	sqlCmd += String.format(" from %s ", idNoTable);
	sqlCmd += "where id_p_seqno = ? ";
	setString(1, idPSeqnoSelectedByTxtCardNo); 

	int cnt = selectTable();
	if (cnt == 0) {
		if (isVDCard) {
			showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號不存在VD卡人主檔");
			putErrReportList(ErrorReason.ERR12, inputObj);
		}else {
			showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號不存在卡人主檔");
			putErrReportList(ErrorReason.ERR11, inputObj);
		}		
		return false;
	}

	String idNo = getValue("id_no");
	if (inputObj.tmpIdNo.equalsIgnoreCase(idNo) == false) {
		if (isVDCard) {
			showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號與VD卡人檔不一致");
			putErrReportList(ErrorReason.ERR10, inputObj);
		}else {
			showLogMessage("I", "checkIdno", "Error : id_p_seqno = [" + idPSeqnoSelectedByTxtCardNo + "]，客戶代號與卡人檔不一致");
			putErrReportList(ErrorReason.ERR9, inputObj);
		}		
		
		return false;
	}

	return true;
}

/***********************************************************************/
boolean isDbcIdnoExist(TempICPSMQND inputObj) throws Exception {

	sqlCmd = "select count(*) tmpInt ";
	sqlCmd += "from dbc_idno ";
	sqlCmd += "where id_no = ? ";
	setString(1, inputObj.tmpIdNo); // 客戶代號前10碼

	selectTable();

	int tmpInt = getValueInt("tmpInt");
	if (tmpInt == 0) {
		showLogMessage("I", "", "Error : id_no = [" + inputObj.tmpIdNo + "]，客戶代號不存在VD卡人主檔");
		putErrReportList(ErrorReason.ERR3, inputObj);
		return false;
	}

	return true;
}

/**********************************************************************/

/**
 * update or insert all tables in which there are columns related to id_no
 * @param idNoFromFile
 * @param dbaData
 * @return
 * @throws Exception
 */
private boolean updateIdNo(String idNoFromFile, DbcDataD017 dbaData, TempICPSMQND inputObj) throws Exception {
	boolean sqlResult = false;
	
	sqlResult = updateDbcIdnoSetIdNo(idNoFromFile, dbaData.idPSeqno, inputObj);
	if ( ! sqlResult) {
		rollbackDataBase();
		return false;
	}
	
	sqlResult = updateDbaAcnoSetIdNo(idNoFromFile, dbaData.idPSeqno, inputObj);
	if ( ! sqlResult) {
		rollbackDataBase();
		return false;
	}
	
	sqlResult = insertDbcChgId(idNoFromFile, dbaData);
	if ( ! sqlResult) {
		rollbackDataBase();
		return false;
	}
	
	sqlResult = updateCrdEmployeeSetIdno(idNoFromFile, dbaData.idNo);
	if ( ! sqlResult) {
		rollbackDataBase();
		return false;
	}
	
	sqlResult = updateCrdIdnoSeqnoSetIdno(idNoFromFile, dbaData.idPSeqno);
	if ( ! sqlResult) {
		rollbackDataBase();
		return false;
	}

	// 2022/01/11 Justin 同步信用卡的ID
	try {
		showLogMessage("D", "", String.format("開始確認並同步信用卡的ID[%s]->[%s]", dbaData.idNo, idNoFromFile));
		String chiName = getChiNameFromCrdIdno(dbaData.idPSeqno);
		if (chiName != null) {
			sqlResult = updateCrdIdnoSetIdNo(idNoFromFile, dbaData.idPSeqno, inputObj);
			if (sqlResult) {
				updateCrdAcnoSetIdNo(idNoFromFile, dbaData.idPSeqno, inputObj);
				insertCrdChgId(idNoFromFile, dbaData, chiName);
				showLogMessage("D", "", "成功同步信用卡的ID");
			}
		}
	}catch (Exception e) {
		showLogMessage("I", "", "更新信用卡的ID發生錯誤:" + e.getLocalizedMessage());
	}
	
	return true;
	
}

/***********************************************************************/

private boolean updateCrdIdnoSeqnoSetIdno(String idNoFromFile, String idPSeqno) throws Exception {
	daoTable = "crd_idno_seqno";
	updateSQL =   " id_no  = ? ";
    whereStr   = " where id_p_seqno = ? ";      
    setString(1, idNoFromFile);
    setString(2, idPSeqno);
	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update CRD_IDNO_SEQNO not found! id_p_seqno= " + idPSeqno);;
//		return false; //沒更新也沒關係
	}

	return true;
	
}

/***********************************************************************/

private boolean updateCrdEmployeeSetIdno(String idNoFromFile, String idNoFromDB) throws Exception {
	daoTable = "crd_employee";
	updateSQL =   " id  = ? ";
    whereStr   = " where id = ? ";      
    setString(1, idNoFromFile);
    setString(2, idNoFromDB);
	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update crd_employee not found! id = " + idNoFromDB);;
//		return false; //沒更新也沒關係
	}

	return true;
	
}

/***********************************************************************/

private boolean insertDbcChgId(String idNoFromFile, DbcDataD017 dbaData) throws Exception {
	setValue("acct_no", "");
	setValue("id", dbaData.idNo);
	setValue("id_code", dbaData.idNoCode);
	setValue("corp_flag", "N");
	setValue("aft_id", idNoFromFile);
	setValue("aft_id_code", "0");
	setValue("crt_date", sysDate);
	setValue("process_flag", "Y");
	setValue("mod_user", prgmId);
	setValue("mod_time", sysDate + sysTime);
	setValue("mod_pgm", prgmId);
	daoTable = "dbc_chg_id";
	int insertCnt = insertTable();
	if (insertCnt <= 0 ) {
		comcr.errRtn("Error! insert DBC_CHG_ID error!", "", "");
	}
	
	return true;				
}

/***********************************************************************/

/**
 * 
 * @param idNoFromFile
 * @param idPSeqno
 * @param inputObj
 * @return
 * @throws Exception
 */
private boolean updateDbaAcnoSetIdNo(String idNoFromFile, String idPSeqno, TempICPSMQND inputObj) throws Exception {
	daoTable = "dba_acno";
	updateSQL =   " acct_key  = ?,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_pgm  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS')  ";
    whereStr   = " where id_p_seqno = ? ";      
    setString(1, idNoFromFile + "0");
    setString(2, prgmId);
    setString(3, prgmId);
    setString(4, sysDate + sysTime);
    setString(5, idPSeqno);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update DBA_ACNO[id_no] not found! id_p_seqno = " + idPSeqno);
		putErrReportList(ErrorReason.ERR8, inputObj);
		return false;
	}

	return true;
	
}

/***********************************************************************/

/**
 * 
 * @param idNoFromFile
 * @param idPSeqno
 * @param inputObj
 * @return
 * @throws Exception
 */
private boolean updateDbcIdnoSetIdNo(String idNoFromFile, String idPSeqno, TempICPSMQND inputObj) throws Exception {
	daoTable = DBC_IDNO;
	updateSQL = " id_no  = ?,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_pgm  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
    whereStr   = " where id_p_seqno = ? ";      
    setString(1, idNoFromFile);
    setString(2, prgmId);
    setString(3, prgmId);
    setString(4, sysDate + sysTime);
    setString(5, idPSeqno);

    try {
    	updateTable();
    }catch (Exception e) {
		if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
			showLogMessage("I", "","update DBC_IDNO[id_no]: Duplicate! id_p_seqno = " + idPSeqno);
			putErrReportList(ErrorReason.ERR7, inputObj);
			return false;
		}
		comcr.errRtn("Error! : update DBC_IDNO[id_no]: " + e.getMessage() + ". id_p_seqno = " + idPSeqno, "", "");
	}

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update DBC_IDNO[id_no]: Not found! id_p_seqno = " + idPSeqno);
		putErrReportList(ErrorReason.ERR6, inputObj);
		return false;
	}

	return true;
	
}

/***********************************************************************/

private boolean insertCrdChgId(String newIdno, DbcDataD017 dbaData, String chiName) throws Exception {
	sqlCmd = " select count(*) as tot_cnt ";
	sqlCmd += " from crd_chg_id ";
	sqlCmd += " where old_id_no = ? ";
	setString(1, dbaData.idNo);
	selectTable();
	int totCnt = getValueInt("tot_cnt");
	
	if(totCnt<1) {
		setValue("old_id_no", dbaData.idNo);
		setValue("old_id_no_code", "0");
		setValue("id_p_seqno", dbaData.idPSeqno);
        setValue("old_id_p_seqno", dbaData.idPSeqno);
		setValue("id_no", newIdno);
		setValue("id_no_code", "0");
		setValue("post_jcic_flag", "Y");
		setValue("chi_name", chiName);
		setValue("chg_date", sysDate);
		setValue("crt_date", sysDate);
        setValue("crt_user", prgmId);
        setValue("apr_date", sysDate);
        setValue("apr_user", prgmId);
        setValue("src_from", "2");
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_user", prgmId);
        setValue("mod_pgm", prgmId);
		daoTable = "crd_chg_id";
		insertTable();				
	}else {
		daoTable = "crd_chg_id";
		updateSQL = " id_no = ?,";
		updateSQL += " id_p_seqno = ?,";
		updateSQL += " old_id_p_seqno = '',";
		updateSQL += " chi_name = ?,";
		updateSQL += " chg_date = to_char(sysdate,'YYYYMMDD'),";
		updateSQL += " apr_user  = ? ,";
		updateSQL += " apr_date  = to_char(sysdate,'YYYYMMDD') ,";
		updateSQL += " src_from  = '2' ,";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ,";
		updateSQL += " mod_user  = ? ,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = " where old_id_no = ? ";      
        setString(1, newIdno);
        setString(2, dbaData.idPSeqno);
        setString(3, chiName);
        setString(4, prgmId);
        setString(5, sysDate + sysTime);
        setString(6, prgmId);
        setString(7, prgmId);
        setString(8, dbaData.idNo);
		updateTable();
	}
	
	return true;				
}

/***********************************************************************/

/**
 * 
 * @param idNoFromFile
 * @param idPSeqno
 * @param inputObj
 * @return
 * @throws Exception
 */
private boolean updateCrdAcnoSetIdNo(String newIdno, String idPSeqno, TempICPSMQND inputObj) throws Exception {
	daoTable = "act_acno";
	updateSQL =  " acct_key  = ?,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_pgm  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'),  ";
    updateSQL += " apr_user  = ? ,";
	updateSQL += " apr_date  = to_char(sysdate,'YYYYMMDD') ";
    whereStr   = " where id_p_seqno = ? ";      
    setString(1, newIdno + "0");
    setString(2, prgmId);
    setString(3, prgmId);
    setString(4, sysDate + sysTime);
    setString(5, prgmId);
    setString(6, idPSeqno);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update CRD_ACNO[id_no] not found! id_p_seqno = [" + idPSeqno + "]");
		return true;
	}

	return true;
	
}

/***********************************************************************/

/**
 * 
 * @param idNoFromFile
 * @param idPSeqno
 * @param inputObj
 * @return
 * @throws Exception
 */
private boolean updateCrdIdnoSetIdNo(String newIdno, String idPSeqno, TempICPSMQND inputObj) throws Exception {
	daoTable = CRD_IDNO;
	updateSQL = " id_no  = ?,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_pgm  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
    whereStr   = " where id_p_seqno = ? ";      
    setString(1, newIdno);
    setString(2, prgmId);
    setString(3, prgmId);
    setString(4, sysDate + sysTime);
    setString(5, idPSeqno);

    try {
    	updateTable();
    }catch (Exception e) {
		if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
			showLogMessage("I", "","update CRD_IDNO[id_no]: Duplicate! id_p_seqno = [" + idPSeqno + "]");
		}
		return false;
	}

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update CRD_IDNO[id_no]: Not found! id_p_seqno = [" + idPSeqno + "]");
		return false;
	}

	return true;
	
}

/***********************************************************************/

/**
 * 
 * @param idNoFromFile
 * @param idNoFromDB
 * @return
 */
private boolean isIdNoChg(String idNoFromDB, String idNoFromFile) {
	return ! idNoFromFile.trim().equals(idNoFromDB);
}

/***********************************************************************/

private boolean checkExtendCode(String extendCode, TempICPSMQND inputObj) throws Exception {
	showLogMessage("I", "", "id_no = [" + inputObj.tmpIdNo + extendCode + "]");

	if (!extendCode.equals(" ") && !extendCode.equals("R")) {
		showLogMessage("I", "", "Error : 客戶代號第11碼延伸碼不為空白 或'R'");
		putErrReportList(ErrorReason.ERR1, inputObj);
		return false;
	}
	return true;
	
}
/***********************************************************************/
private TempICPSMQND moveData(Map<String, Object> map) throws Exception {
	TempICPSMQND inputObj = new TempICPSMQND();
	
	String idPSeqnoSelectedByTxtCardNo = "";
//	String tmpStr;
	boolean isCard;
//	String newLine = "\r";

	showLogMessage("I", "", "==========");
	inputObj.tmpCardNo = ((String) map.get("card_no")).trim();
	showLogMessage("I", "", "card_no = [" + inputObj.tmpCardNo + "]");
	
	// 2020/12/18 Justin revised the misunderstanding function name and extract some functions
	idPSeqnoSelectedByTxtCardNo = getIdPSeqnoFromCard(inputObj);
	isCard = idPSeqnoSelectedByTxtCardNo != null && idPSeqnoSelectedByTxtCardNo.trim().length() != 0;
	map.put("idPSeqnoSelectedByTxtCardNo", idPSeqnoSelectedByTxtCardNo);

//	if (inputObj.tmpActivateDate.isEmpty())
//		inputObj.tmpActivateDate = String.format("%-8s", " ");
	
	inputObj.tmpActivateDate = getActivateDate(inputObj.tmpCardNo); // 開卡日期
	if (inputObj.tmpActivateDate.isEmpty())
		inputObj.tmpActivateDate = String.format("%-8s", " ");
	
	showLogMessage("I", "", "開卡日期 = [" + inputObj.tmpActivateDate + "]");

//	if (inputObj.tmpCardType.equals("CC")) // 若為信用卡，取開卡日期
//		tmpStr = inputObj.tmpActivateDate;
//	else
//		tmpStr = (String) map.get("activate_date");
	
	ICPSMQND inputData = new ICPSMQND();
    inputData.cardNo = (String) map.get("card_no");
//    inputData.activateDate = tmpStr;
    inputData.activateDate = "";
    inputData.field3 = (String) map.get("col3");
    inputData.field4 = (String) map.get("col4");
    inputData.field5 = (String) map.get("col5");
    inputData.field6 = (String) map.get("col6");
    inputData.field7 = (String) map.get("col7");
    inputData.field8 = (String) map.get("col8");
    inputData.field9 = (String) map.get("col9");
    inputData.field10 = (String) map.get("col10");
    inputData.field11 = (String) map.get("col11");
    inputData.idNo = (String) map.get("id_no");
    inputData.birthday = (String) map.get("birthday");
    inputData.field14 = (String) map.get("col14");
    inputData.field15 = (String) map.get("col15");
    inputData.field16 = (String) map.get("col16");
    inputData.field17 = (String) map.get("col17");
    inputData.field18 = (String) map.get("col18");
    inputData.field19 = (String) map.get("col19");
    inputData.field20 = (String) map.get("col20");
    inputData.field21 = (String) map.get("col21");
    inputData.field22 = (String) map.get("col22");
    inputData.field23 = (String) map.get("col23");
    inputData.field24 = (String) map.get("col24");
    inputData.cellarPhone = (String) map.get("cellar_phone");
    inputData.field26 = (String) map.get("col26");
    inputData.field27 = (String) map.get("col27");
    inputData.field28 = (String) map.get("col28");
    inputData.field29 = (String) map.get("col29");
    inputData.field30 = (String) map.get("col30");
    inputData.field31 = (String) map.get("col31");
    inputData.field32 = (String) map.get("col32");
    inputData.field33 = (String) map.get("col33");
    inputData.field34 = (String) map.get("col34");
    inputData.field35 = (String) map.get("col35");
    inputData.field36 = (String) map.get("col36");
    inputData.field37 = (String) map.get("col37");
    inputData.blockCodeAndReasonCode = (String) map.get("col38");
    inputData.field39 = (String) map.get("col39");
//    inputData.stopCode = (String) map.get("col40");
    inputData.stopCode = "";
    inputData.field41 = (String) map.get("col41");
    inputData.field42 = (String) map.get("col42");
    inputData.field43 = (String) map.get("col43");
    inputData.field44 = (String) map.get("col44");
    inputData.field45 = (String) map.get("col45");
    inputData.field46 = (String) map.get("col46");
    inputData.field47 = (String) map.get("col47");
    inputData.field48 = (String) map.get("col48");
    inputData.field49 = (String) map.get("col49");
    inputData.modNo = (String) map.get("mod_no");
    inputData.msgSendStatus = (String) map.get("col51");
    inputData.field52 = (String) map.get("col52");
    inputData.availableDate = (String) map.get("col53");
    inputData.issueUnit = (String) map.get("col54");
    inputData.field55 = (String) map.get("col55");
    inputData.field56 = (String) map.get("col56");
    
//	String aRowOfData = (String) map.get("card_no") + tmpStr + (String) map.get("col3") + (String) map.get("col4")
//			+ (String) map.get("col5") + (String) map.get("col6") + (String) map.get("col7")
//			+ (String) map.get("col8") + (String) map.get("col9") + (String) map.get("col10")
//			+ (String) map.get("col11") + (String) map.get("id_no") + (String) map.get("birthday")
//			+ (String) map.get("col14") + (String) map.get("col15") + (String) map.get("col16")
//			+ (String) map.get("col17") + (String) map.get("col18") + (String) map.get("col19")
//			+ (String) map.get("col20") + (String) map.get("col21") + (String) map.get("col22")
//			+ (String) map.get("col23") + (String) map.get("col24") + (String) map.get("cellar_phone")
//			+ (String) map.get("col26") + (String) map.get("col27") + (String) map.get("col28")
//			+ (String) map.get("col29") + (String) map.get("col30") + (String) map.get("col31")
//			+ (String) map.get("col32") + (String) map.get("col33") + (String) map.get("col34")
//			+ (String) map.get("col35") + (String) map.get("col36") + (String) map.get("col37")
//			+ (String) map.get("col38") + (String) map.get("col39") + (String) map.get("col40")
//			+ (String) map.get("col41") + (String) map.get("col42") + (String) map.get("col43")
//			+ (String) map.get("col44") + (String) map.get("col45") + (String) map.get("col46")
//			+ (String) map.get("col47") + (String) map.get("col48") + (String) map.get("col49")
//			+ (String) map.get("mod_no") + (String) map.get("col51") + (String) map.get("col52")
//			+ (String) map.get("col53") + (String) map.get("col54") + (String) map.get("col55")
//			+ (String) map.get("col56") + newLine;
    
	psmqdMap.put(inputData.cardNo.trim(), inputData);
//	lpar2.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", aRowOfData));  #add

	// if the card is not exist, return
	if (isCard == false) return inputObj;

	inputObj.tmpCellarPhone = ((String) map.get("cellar_phone")).trim();
	showLogMessage("I", "", "cellar_phone = [" + inputObj.tmpCellarPhone + "]");
	
	inputObj.tmpBirthDay = ((String) map.get("birthday")).trim();
	showLogMessage("I", "", "birthday = [" + inputObj.tmpBirthDay + "]");
	
	inputObj.reasonCode = inputData.blockCodeAndReasonCode.substring(0,2);
	inputObj.blockCode = inputData.blockCodeAndReasonCode.substring(2);
	showLogMessage("I", "", "reason code = [" + inputObj.reasonCode + "], block code = [" + inputObj.blockCode + "]" );

	return inputObj;
}
/****************************************************************************/
String getIdPSeqnoFromCard(TempICPSMQND inputObj) throws Exception {
	String idPSeqno = getIdPSeqnoFromCrdCard(inputObj);
	if (idPSeqno == null || idPSeqno.trim().length() == 0 ) {
		inputObj.tmpActivateDate = "";
		idPSeqno = getIdPSeqnoFromDbcCard(inputObj);
		if (idPSeqno == null || idPSeqno.trim().length() == 0) {
			inputObj.tmpCardType = "";
			return null;
		}
	}

	return idPSeqno;
}

/**********************************************************************/
String getIdPSeqnoFromDbcCard(TempICPSMQND inputObj) throws Exception {
	String idPSeqno = "";

	sqlCmd = "select id_p_seqno ";
	sqlCmd += "from dbc_card ";
	sqlCmd += "where card_no = ? ";
	setString(1, inputObj.tmpCardNo);

	int cnt = selectTable();
	
	if (cnt == 0) {
		showLogMessage("I", "", "Error : card_no = [" + inputObj.tmpCardNo + "]，卡片號碼不存在卡片主檔");
		putErrReportList(ErrorReason.ERR2, inputObj);
		return null;
	}
	
	inputObj.tmpCardType = "VD"; // 卡片為VD信用卡
	showLogMessage("I", "", "card_no = [" + inputObj.tmpCardNo + "]，為VD卡");
	idPSeqno = getValue("id_p_seqno");

	return idPSeqno;
}

/**********************************************************************/

/**
 * get data from dbc_crd and dbc_idno
 * @return
 * @throws Exception 
 */
private DbcDataD017 getDbcData(String cardNo) throws Exception {
	DbcDataD017 dbaData = new DbcDataD017();
	boolean selectResult = false;
	
	selectResult = selectDbcCardIcuD017(cardNo);
	if (selectResult == false) {
		return null;
	}
	dbaData.idPSeqno = getValue("id_p_seqno");
//	dbaData.acctNo = getValue("acct_no");
	
	
	selectResult = selectDbcIdnoIcuD017(dbaData.idPSeqno);
	if (selectResult == false) {
		return null;
	}
	dbaData.idNo = getValue("id_no");
	dbaData.idNoCode = getValue("id_no_code");
	
	return dbaData;
}
/**********************************************************************/

/**
 * 
 * @return
 * @throws Exception 
 */
private boolean selectDbcCardIcuD017(String cardNo) throws Exception {
	sqlCmd =  "select id_p_seqno, acct_no ";
	sqlCmd += "from dbc_card ";
	sqlCmd += "where card_no = ? ";
	setString(1, cardNo);

	int selectCnt = selectTable();

	if (selectCnt <= 0) {
		showLogMessage("I", "", "Error : card_no = [" + cardNo + "]，卡片號碼不存在卡片主檔[selectDbcCard]");
		return false;
	}

	return true;
	
}
/**********************************************************************/

/**
 * 
 * @param idPSeqno
 * @return
 * @throws Exception 
 */
private boolean selectDbcIdnoIcuD017(String idPSeqno) throws Exception {
	sqlCmd =   "select id_no, id_no_code ";
	sqlCmd += "from dbc_idno ";
	sqlCmd += "where id_p_seqno = ? ";
	setString(1, idPSeqno);

	int selectCnt = selectTable();

	if (selectCnt <= 0) {
		showLogMessage("I", "", "Error : id_p_seqno = [" + idPSeqno + "]，客戶代號不存在卡人主檔[selectDbcIdno]");
		return false;
	}

	return true;
}

/**********************************************************************/

String getIdPSeqnoFromCrdCard(TempICPSMQND inputObj) throws Exception {
	String idPSeqno = "";

	sqlCmd = "select id_p_seqno ";
	sqlCmd += "from crd_card ";
	sqlCmd += "where card_no = ? ";
	setString(1, inputObj.tmpCardNo);

	int cnt = selectTable();
	if (cnt == 0) {
		return null;
	}
		
	idPSeqno = getValue("id_p_seqno");
	
//	inputObj.tmpActivateDate = getActivateDate(inputObj.tmpCardNo); // 開卡日期
	inputObj.tmpCardType = "CC"; // 卡片為信用卡
	showLogMessage("I", "", "card_no = [" + inputObj.tmpCardNo + "]，為信用卡");
	
	return idPSeqno;
}
/**********************************************************************/

/**
 * 
 * @param tmpCardNo 
 * @return
 * @throws Exception
 */
private String getActivateDate(String tmpCardNo) throws Exception {
	sqlCmd = "select activate_date ";
	sqlCmd += "from crd_card ";
	sqlCmd += "where card_no = ? ";
	setString(1, tmpCardNo);
	selectTable();
	
	return getValue("activate_date");
}

/**********************************************************************/

String[] getFieldValueIcuD017(String rec, int[] parm) {
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
private Map<String, Object> processDataRecord(String[] row, String[] dt) throws Exception {
	Map<String, Object> map = new HashMap<>();
	int i = 0;
	for (String s : dt) {
		map.put(s.trim(), row[i]);
		i++;
	}
	return map;
}
/****************************************************************************/


///**
// * 第一個放是否要產檔，第二個放指定檔案日期yyyyMMdd, 第三個放:XX分鐘
// * 若沒有給定查詢日期，則查詢日期為系統日
// * @param args
// * @throws Exception
// */
//private void loadArgs(String[] args) throws Exception {
//
//	isMinProvided = false;
//	if (args.length == 1) {
//		// 第一個放是否要產檔
//		if ("Y".equals(args[0]) || "N".equals(args[0])) {
//			doesProdFile = "Y".equals(args[0]) ? true : false;
//		} else {
//			comc.errExit("參數1是否要產檔請填入Y或N", "");
//		}
//		queryDate = "";
//		hMinute = 0;
//	} else if (args.length == 2) {
//		
//		// 第一個放是否要產檔
//		if ("Y".equals(args[0]) || "N".equals(args[0])) {
//			doesProdFile = "Y".equals(args[0]) ? true : false;
//		} else {
//			comc.errExit("參數1是否要產檔請填入Y或N", "");
//		}
//		
//		// 第二個放指定檔案日期yyyyMMdd
//		if (args[1].trim().length() == 0) {
//			queryDate = "";
//			showLogMessage("I", "", "指定檔案日期讀取系統日及系統日前一天");
//		} else {
//			if (!new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
//				comc.errExit(String.format("參數2日期格式[%s]錯誤", args[1]), "");
//			}
//			queryDate = args[1];
//		}	
//		
//		hMinute = 0;
//				
//	} else if (args.length == 3) {
//		
//		// 第一個放是否要產檔
//		if ("Y".equals(args[0])|| "N".equals(args[0])) {
//			doesProdFile = "Y".equals(args[0]) ? true : false;
//		}else {
//			comc.errExit("參數1是否要產檔請填入Y或N", "");
//		}
//		
//		// 第二個放指定檔案日期yyyyMMdd
//		if (args[1].trim().length() == 0) {
//			queryDate = "";
//			showLogMessage("I", "", "指定檔案日期讀取系統日及系統日前一天");
//		} else {
//			if (!new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
//				comc.errExit(String.format("參數2日期格式[%s]錯誤", args[1]), "");
//			}
//			queryDate = args[1];
//		}
//		
//		// 第三個放XX分鐘
//		if (args[2].trim().length() == 0) {
//			hMinute = 0;
//		} else {
//			/*** 參數如果不是數字 ***/
//			if (!isNumeric(args[2])) {
//				comc.errExit("Usage : CrdF071 請輸入參數:XX分鐘[ex:30、60、120]", "");
//			} else {
//				isMinProvided = true;
//				hMinute = Integer.parseInt(args[2]);
//			}
//		}
//		
//	} else {
//		comc.errExit("參數1：必填，是否要產檔，預設為Y(要產檔)；"
//				   + "參數2：非必填，指定檔案日期，預設為系統日及系統日前一天，也可輸入西元年(如：20200715);"   
//				   + "參數3：非必填，可輸入XX分鐘[ex:30、60、120];"
//				   , "");
//	}
//	
//	if (doesProdFile) 
//		showLogMessage("I", "", "是否要產檔參數為Y，因此會產檔");
//	else
//		showLogMessage("I", "", "是否要產檔參數為N，因此不產檔");
//	
//}

/**
 * 第一個放:XX分鐘
 * @param args
 * @throws Exception
 */
private void loadArgs(String[] args) throws Exception {

	isMinProvided = false;
	if (args.length == 0) {
		hMinute = 0;
	} else if (args.length == 1) {
		
		// 第一個放XX分鐘
		if (args[0].trim().length() == 0) {
			hMinute = 0;
		} else {
			/*** 參數如果不是數字 ***/
			if (!isNumeric(args[0])) {
				if ("Y".equals(args[0])) {
					hMinute = 0;
				}else {
					comc.errExit("Usage : CrdF071 請輸入參數:XX分鐘[ex:30、60、120]", "");
				}
			} else {
				isMinProvided = true;
				hMinute = Integer.parseInt(args[0]);
			}
		}
				
	} else {
		comc.errExit("參數1：非必填，可輸入XX分鐘[ex:30、60、120];", "");
	}
}

/***********************************************************************/
/**
 * 
 * @param maxChgDate
 * @throws Exception 
 */
private void updateCrdF071ChgTime(String maxChgDate) throws Exception {
	daoTable = "PTR_SYS_PARM";
	updateSQL = " WF_VALUE  = ? ";
	whereStr = " WHERE WF_PARM = 'SYSPARM' AND WF_KEY = 'CRDF071' ";
	setString(1, maxChgDate);
	updateTable();
	if (notFound.equals("Y")) {
		comcr.errRtn("update PTR_SYS_PARM where WF_PARM = 'SYSPARM' and WF_KEY = 'CRDF071' not found!", "", "");
	}else {
		showLogMessage("I", "", String.format("update PTR_SYS_PARM WF_VALUE =[%s] ", maxChgDate));
	}
}

	/***********************************************************************/
    void commonRtn() throws Exception {
    	hBusiBusinessDate = "";
    	hPreviousBusiBusinessDate = "";
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date, ";
        sqlCmd += " business_date, ";
        sqlCmd += " to_char( to_date(business_date, 'yyyymmdd') - 1 DAYS , 'yyyymmdd') as prev_business_date ";
        sqlCmd += " from ptr_businday ";
        int tmpInt = selectTable();
        if (tmpInt > 0) {
            hSystemDate = getValue("h_system_date");
            hBusiBusinessDate = getValue("business_date");
            hPreviousBusiBusinessDate = getValue("prev_business_date");
        }
    }
    
    /***************************************************************************/
    
    void setOutputDataFile(int nnNo) throws Exception {
    	// OUTPUT_DATA_FILE
    	outputDataFileName = String.format("%s.%8s%02d", OUTPUT_DATA_FILE, hSystemDate, nnNo);
    	outputDataFilePath = String.format("%s/media/icu/out/%s", comc.getECSHOME(),outputDataFileName);
        showLogMessage("I", "", "Open file = [" + outputDataFilePath + "]"); 
        outputDataFilePath = SecurityUtil.verifyPath(outputDataFilePath);
        try {
            comc.mkdirsFromFilenameWithPath(outputDataFilePath);
            outputDataBufWri = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDataFilePath), "MS950"));
        } catch (Exception ex) {
            comcr.errRtn(String.format("開啟檔案[%s]失敗[%s]", outputDataFilePath, ex.getMessage()), "", "");
        }
    }
    
	/***************************************************************************/

	void setOutputAttrFile(int nnNo) throws Exception {
		// OUTPUT_ATTR_FILE
        outputAttrFileName = String.format("%s.%8s%02d", OUTPUT_ATTR_FILE, hSystemDate, nnNo);
    	outputAttrFilePath = String.format("%s/media/icu/out/%s", comc.getECSHOME(),outputAttrFileName);
		showLogMessage("I", "", "Open file = [" + outputAttrFilePath + "]");
		outputAttrFilePath = SecurityUtil.verifyPath(outputAttrFilePath);
        try {
            comc.mkdirsFromFilenameWithPath(outputAttrFilePath);
            outputAttrBufWri = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputAttrFilePath), "MS950"));
        } catch (Exception ex) {
            comcr.errRtn(String.format("開啟檔案[%s]失敗[%s]", outputAttrFilePath, ex.getMessage()), "", "");
        }
	}
	
	/***************************************************************************/

	void setOutputTempFile(int nnNo) throws Exception {
		// OUTPUT_TEMP_FILE
		outputTempFileName = String.format("%s.%8s%02d", OUTPUT_TEMP_FILE, hSystemDate, nnNo);
    	outputTempFilePath = String.format("%s/media/icu/out/%s", comc.getECSHOME(), outputTempFileName);
		showLogMessage("I", "", "Open file = [" + outputTempFilePath + "]");
		outputTempFilePath = SecurityUtil.verifyPath(outputTempFilePath);
        try {
            comc.mkdirsFromFilenameWithPath(outputTempFilePath);
            outputTempBufWri = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputTempFilePath), "MS950"));
        } catch (Exception ex) {
            comcr.errRtn(String.format("開啟檔案[%s]失敗[%s]", outputTempFilePath, ex.getMessage()), "", "");
        }
	}
    
    /***************************************************************************/
    void checkOpen() throws Exception {
//    	String yy = (String.valueOf(Integer.parseInt(hSystemDate.substring(0, 4))-1911)).substring(1, 3); //民國年後兩碼
    	
    	// get next number of the file
    	int hNn = checkFileCtl();
    	
    	setOutputDataFile(hNn);
    	
    	setOutputAttrFile(hNn);
    	
    	int tempNn = checkTempFileCtl();
    	setOutputTempFile(tempNn);

    }
    
    /***********************************************************************/
    private void createPsmqD() throws Exception {
    	if (psmqdMap.isEmpty() == false) {
    		showLogMessage("I", "", "開始產生資料檔......");
    		int recordCntPerFile = 0;
    		List<ICPSMQND> icpsmqnDs = new ArrayList<>(psmqdMap.values());
    		for (int i = 0; i < icpsmqnDs.size(); i++) {
    			totalRecordCnt++;
    			recordCntPerFile++;
//    			showData(icpsmqnDs.get(i));
    			outputDataBufWri.write(icpsmqnDs.get(i).convertToRowOfData() + "\r\n");
    			
    			//切檔
    			if (recordCntPerFile % RECORD_CNT_PER_FILE == 0) {
    				createPsmqA(recordCntPerFile);
    				CrdFileCtlObj outputDataCtlObj = new CrdFileCtlObj(outputDataFileName);
    				outputDataCtlObj.recordCnt = recordCntPerFile;
    				CrdFileCtlObj outputAttrCtlObj = new CrdFileCtlObj(outputAttrFileName);
    				outputAttrCtlObj.recordCnt = 1;
    				outputDataFileCtlList.add(outputDataCtlObj);
    				outputAttrFileCtlList.add(outputAttrCtlObj);
    				outputDataBufWri.close();
    				outputAttrBufWri.close();
    				int nnNo = Integer.parseInt(outputAttrFileName.substring(outputAttrFileName.length()-2, outputAttrFileName.length()));
					setOutputDataFile(nnNo + 1);
					setOutputAttrFile(nnNo + 1);
					recordCntPerFile = 0;
				}
    		}
    		
    		if (recordCntPerFile > 0) {
    			createPsmqA(recordCntPerFile);
				CrdFileCtlObj outputDataCtlObj = new CrdFileCtlObj(outputDataFileName);
				outputDataCtlObj.recordCnt = recordCntPerFile;
				CrdFileCtlObj outputAttrCtlObj = new CrdFileCtlObj(outputAttrFileName);
				outputDataCtlObj.recordCnt = 1;
				outputDataFileCtlList.add(outputDataCtlObj);
				outputAttrFileCtlList.add(outputAttrCtlObj);
				outputDataBufWri.close();
				outputAttrBufWri.close();
			}
    		
    	}
    }
    /***********************************************************************/
    
    private void showData(ICPSMQND psmqdObj) {
    	showLogMessage("D", "", "==========");
    	showLogMessage("D", "", String.format("card_no = [%s]", psmqdObj.cardNo));
    	showLogMessage("D", "", String.format("開卡日期 = [%s]", psmqdObj.activateDate));
    	showLogMessage("D", "", String.format("id_no = [%s]", psmqdObj.idNo));
    	showLogMessage("D", "", String.format("birthday = [%s]", psmqdObj.birthday));
    	showLogMessage("D", "", String.format("cellar_phone = [%s]", psmqdObj.cellarPhone));
    	showLogMessage("D", "", String.format("停用碼 = [%s]", psmqdObj.stopCode));
    	showLogMessage("D", "", String.format("異動碼 = [%s]", psmqdObj.modNo));
    	showLogMessage("D", "", String.format("簡訊發送狀態 = [%s]", psmqdObj.msgSendStatus));
    	showLogMessage("D", "", String.format("卡片有效日 = [%s]", psmqdObj.availableDate));
    	showLogMessage("D", "", String.format("發卡單位代號 = [%s]", psmqdObj.issueUnit));
	}

	/***********************************************************************/
    void createPsmqA(int dataCount) throws Exception {
    	PSMQAObj psmqAObj = new PSMQAObj();
    	psmqAObj.dataCount = String.format("%07d",dataCount);
    	psmqAObj.errCode = "";
//		icuData.filler = "";
    	String tmp = psmqAObj.allText();
        outputAttrBufWri.write(tmp + "\r\n");
		
		return;
	}
    /***********************************************************************/
    void createTempfile() throws Exception {
    	TempFileObj tempData = new TempFileObj();

    	for (int i = 0; i < outputDataFileCtlList.size(); i++) {
    		tempData.uploadFileName = outputAttrFileCtlList.get(i).fileName;
    		String tempStr = tempData.allText();
            outputTempBufWri.write(tempStr + "\r\n");
            
    		tempData.uploadFileName = outputDataFileCtlList.get(i).fileName;
        	tempStr = tempData.allText();
            outputTempBufWri.write(tempStr + "\r\n");
		}

	}
    
	/**********************************************************************/
    int checkFileCtl() throws Exception {
    	int hNn = 0;
    	String likeFilename = "";
    	String hFilename = "";
    	likeFilename = String.format("%s.%8s", OUTPUT_DATA_FILE, hSystemDate)+"%";
        sqlCmd = "select file_name ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
//        sqlCmd += " and crt_date  = to_char(sysdate,'yyyymmdd') ";
        sqlCmd += " order by file_name desc  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, likeFilename);
        selectTable();
        if (notFound.equals("Y")) {
        	hNn++;
        }else {
        	hFilename = getValue("file_name");
        	hNn = Integer.valueOf(hFilename.substring(14, 16))+1;
        }
        
        return hNn;
    }
    
    /**********************************************************************/
    int checkTempFileCtl() throws Exception {
    	int hNn = 0;
    	String likeFilename = "";
    	String hFilename = "";
    	likeFilename = String.format("%s.%8s", OUTPUT_TEMP_FILE, hSystemDate)+"%";
        sqlCmd = "select file_name ";
        sqlCmd += " from crd_file_ctl  ";
        sqlCmd += "where file_name like ?  ";
        sqlCmd += " order by file_name desc  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, likeFilename);
        selectTable();
        if (notFound.equals("Y")) {
        	hNn++;
        }else {
        	hFilename = getValue("file_name");
        	hNn = Integer.valueOf(hFilename.substring(17, 19))+1;
        }
        
        return hNn;
    }
    
    
	int checkFileCtlIcuD017(String fileName) throws Exception {
		int totalCount = 0;

		sqlCmd = "select count(*) totalCount ";
		sqlCmd += " from crd_file_ctl ";
		sqlCmd += " where file_name = ? ";
		setString(1, renameToCardLinkFileName(fileName));
		int recordCnt = selectTable();

		if (recordCnt > 0)
			totalCount = getValueInt("totalCount");

		if (totalCount > 0) {
			showLogMessage("I", "", String.format("此檔案 = [" + fileName + "]已處理過不可重複處理(crd_file_ctl)"));
			return (1);
		}
		return (0);
	}

	private String renameToCardLinkFileName(String fileName) {
		return fileName.substring(0,19) + "CLK" + fileName.substring(18);
	}
    /***************************************************************************/
    /**
     * return the maxChgTime
     * @param isThereArg
     * @param chgTime
     * @return
     * @throws Exception
     */
    String selectCmsChgcolumnLog(String chgTime) throws Exception {
    	String maxChgTime = null;
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(" SELECT id_p_seqno,debit_flag, (chg_date || chg_time) as chgTime ")
          .append(" FROM cms_chgcolumn_log ")
          .append(" WHERE chg_column = 'cellar_phone' ");
    	
        if (isMinProvided) {
        	sb.append(" AND (chg_date || chg_time) >= to_char(sysdate - (? MINUTES),'yyyymmddHH24miss') ");
            setInt(1, hMinute);
            showLogMessage("I", "", String.format("select CmsChgcolumnLog 使用程式提供的參數[%s]", hMinute));
		}else {
			if (chgTime != null && chgTime.trim().length() != 0) {
				sb.append(" AND (chg_date || chg_time) > ? ");
				setString(1, chgTime);
	            showLogMessage("I", "", String.format("select CmsChgcolumnLog 使用PTR_SYS_PARM的參數[%s]", chgTime));
			}else {
				showLogMessage("I", "", String.format("select CmsChgcolumnLog 使用PTR_SYS_PARM的參數[目前為空]"));
			}
		}
        sb.append(" limit 1 ");
        
        sqlCmd = sb.toString();
        
        // ======================
        
        selectTable();
		if (notFound.equals("Y")) {
			return maxChgTime;
		}
		
		// ======================
		
		sb = new StringBuilder();
		
		sb.append(" SELECT id_p_seqno,debit_flag, (chg_date || chg_time) as chgTime ")
		  .append(" FROM cms_chgcolumn_log ")
		  .append(" WHERE chg_column = 'cellar_phone' ");

		if (isMinProvided) {
			sb.append(" AND (chg_date || chg_time) >= to_char(sysdate - (? MINUTES),'yyyymmddHH24miss') ");
			setInt(1, hMinute);
			showLogMessage("I", "", String.format("select CmsChgcolumnLog 使用程式提供的參數[%s]", hMinute));
		} else {
			if (chgTime != null && chgTime.trim().length() != 0) {
				sb.append(" AND (chg_date || chg_time) > to_char( to_date(?, 'yyyymmddhh24miss') - 1 DAYS, 'yyyymmddhh24miss') ");
				setString(1, chgTime);
				showLogMessage("I", "", String.format("select CmsChgcolumnLog 使用PTR_SYS_PARM的參數[%s]", chgTime));
			} else {
				showLogMessage("I", "", String.format("select CmsChgcolumnLog 使用PTR_SYS_PARM的參數[目前為空]"));
			}
		}
		sb.append(" ORDER by (chg_date || chg_time) DESC ");

		sqlCmd = sb.toString();
		showLogMessage("D", "", sqlCmd);
        
        boolean isFirstRow = true;
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
        	
        	String idPSeqno = getValue("id_p_seqno");
        	String debitFlag = getValue("debit_flag");
        	
            if(debitFlag.equals("Y")) {
            	IdNoObj idNoObj = selectDbcIdno(idPSeqno);
            	if (idNoObj == null) continue;
            	selectDbcCard(idNoObj);
            }else if(debitFlag.equals("N")) {
            	/** 2022/05/20 Justin 若WF_VALUE2==1且為純附卡人才能產生檔案 **/
				if ( doCheckSupFlag == false || (doCheckSupFlag && isSupCardPerson(idPSeqno))) {
					IdNoObj idNoObj = selectCrdIdno(idPSeqno);
					if (idNoObj == null)
						continue;
					selectCrdCard(idNoObj);
				}
            }
            
            // 取得最大的時間
            if (isFirstRow) {
        		maxChgTime = getValue("chgTime");
        		isFirstRow = false;
        		showLogMessage("I", "", String.format("最大時間為%s", maxChgTime));
			}
            
        }
        closeCursor(cursorIndex);
        
        return maxChgTime;
    }
    
    /*************************************************************************/
    private boolean isSupCardPerson(String idPSeqno) throws Exception {

    	sqlCmd =  "SELECT ";
		sqlCmd += "COUNT(DECODE(SUP_FLAG,'1','Y')) AS SUP_FLAG_1, ";
		sqlCmd += "COUNT(DECODE(SUP_FLAG,'0','Y')) AS SUP_FLAG_0  ";
		sqlCmd += " FROM crd_card";
		sqlCmd += " WHERE id_p_seqno = ? ";
		setString(1, idPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	showLogMessage("I", "isSupCardPerson", String.format("isSupCardPerson: id_p_seqno[%s]不存在crd_idno中", idPSeqno));
            return false;
        }
        if (recordCnt > 0) {
        	int supFlag1 = getValueInt("SUP_FLAG_1");
			int supFlag0 = getValueInt("SUP_FLAG_0");
			return supFlag1 > 0 && supFlag0 == 0;
        }
		return false;
	}

	/** ***********************************************************************/
    IdNoObj selectDbcIdno(String idPSeqno) throws Exception {
    	IdNoObj idNoObj = null;

        sqlCmd = "select id_no,birthday,cellar_phone ";
        sqlCmd += " from dbc_idno";
        sqlCmd += " where id_p_seqno = ? ";
        setString(1, idPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	showLogMessage("I", "selectDbcIdno", String.format("id_p_seqno[%s]不存在dbc_idno中", idPSeqno));
            return null;
        }
        if (recordCnt > 0) {
        	idNoObj = new IdNoObj();
        	idNoObj.idPSeqno = idPSeqno;
        	idNoObj.idNo = getValue("id_no");
        	idNoObj.birthday = getValue("birthday");
        	idNoObj.cellarPhone = getValue("cellar_phone");
        }
        return idNoObj;
    }
    
    
    /*************************************************************************/
	int selectDbcCard(IdNoObj idNoObj) throws Exception {

//		sqlCmd =  "SELECT card_no, new_end_date, current_code ";
		sqlCmd =  "SELECT card_no, new_end_date ";
		sqlCmd += " FROM dbc_card";
		sqlCmd += " WHERE id_p_seqno = ? ";
		setString(1, idNoObj.idPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "selectDbcCard", String.format("id_p_seqno[%s]不存在dbc_card中", idNoObj.idPSeqno));
			return (0);
		}
		for (int i = 0; i < recordCnt; i++) {
			String cardNo = getValue("card_no", i);
			String newEndDate = getValue("new_end_date", i);
//			String currentCode = getValue("current_code", i);
            
        	ICPSMQND psmqdObj = null;
        	if (psmqdMap.containsKey(cardNo.trim())) {
        		showLogMessage("D", "", "已存在檔案中");
        		psmqdObj = psmqdMap.get(cardNo);
        		psmqdObj.activateDate = "";
//        		psmqdObj.stopCode = convertCurrentCodeToStopCode(currentCode);
        		psmqdObj.stopCode = "";
    		}else {
    			psmqdObj = new ICPSMQND();
    			psmqdObj.cardNo = cardNo;
    			psmqdObj.activateDate = "";
    			psmqdObj.idNo = idNoObj.idNo;
    			psmqdObj.birthday = idNoObj.birthday;
    			psmqdObj.cellarPhone = idNoObj.cellarPhone;
//    			psmqdObj.stopCode = convertCurrentCodeToStopCode(currentCode);
    			psmqdObj.stopCode = "";
    			psmqdObj.modNo = "C";
    			psmqdObj.msgSendStatus = "Y";
    			psmqdObj.availableDate = convertToYYMM(newEndDate, cardNo);
    			psmqdObj.issueUnit = ISSUE_UNIT;
    		}
        	psmqdMap.put(psmqdObj.cardNo.trim(), psmqdObj);
            
        	showLogMessage("D", "", "==========");
        	showLogMessage("I", "", String.format("card_no = [%s]", psmqdObj.cardNo));
        	showLogMessage("I", "", String.format("debit_flag = [%s]", "Y"));
        	showLogMessage("D", "", String.format("開卡日期 = [%s]", psmqdObj.activateDate));
        	showLogMessage("D", "", String.format("id_no = [%s]", psmqdObj.idNo));
//        	showLogMessage("D", "", String.format("birthday = [%s]", psmqdObj.birthday));
        	showLogMessage("D", "", String.format("cellar_phone = [%s]", psmqdObj.cellarPhone));
//        	showLogMessage("D", "", String.format("停用碼 = [%s]", psmqdObj.stopCode));
//        	showLogMessage("D", "", String.format("異動碼 = [%s]", psmqdObj.modNo));
//        	showLogMessage("D", "", String.format("簡訊發送狀態 = [%s]", psmqdObj.msgSendStatus));
//        	showLogMessage("D", "", String.format("卡片有效日 = [%s]", psmqdObj.availableDate));
//        	showLogMessage("D", "", String.format("發卡單位代號 = [%s]", psmqdObj.issueUnit));
        	
//            buf1data.cardNo = hCardNo; //卡片號碼
//            buf1data.activateDate = hActivateDate; //開卡日期
//            buf1data.idNo = hIdNo; //客戶代號(身分證字號)
//            buf1data.birthday = hBirthday; //主卡人出生日期
//            buf1data.cellarPhone = hCellarPhone; //主卡人行動電話
//            buf1data.modNo = "C"; //異動碼
//            buf1data.msgSendStatus = hMsgFlag; //簡訊發送狀態
//            buf1data.availableDate = hNewEndDate.substring(2, 6); //有效日期YYMM(西元年)
//            buf1data.issueUnit = ISSUE_UNIT; //發卡單位代號
//            outputAttrFilePath = buf1data.convertToRowOfData();
//            outputDataBufWri.write(outputAttrFilePath + "\r\n");
            
		}
		return (1);
	}
	/*************************************************************************/
    String getCrdF071ChgTime() throws Exception {
    	doCheckSupFlag = false;
    	String chgTime = "";
        sqlCmd =  " SELECT WF_VALUE, WF_VALUE2 "
        		+ " FROM PTR_SYS_PARM "
        		+ " WHERE WF_PARM = 'SYSPARM' AND WF_KEY = 'CRDF071' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	chgTime = getValue("WF_VALUE");
        	doCheckSupFlag  = "1".equals(getValue("WF_VALUE2"));
        }
        
        return chgTime;
    }
    /*************************************************************************/
    IdNoObj selectCrdIdno(String idPSeqno) throws Exception {
    	IdNoObj idNoObj = null;

        sqlCmd =  "SELECT id_no, birthday, cellar_phone ";
        sqlCmd += " FROM crd_idno";
        sqlCmd += " WHERE id_p_seqno = ? ";
        setString(1, idPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
        	showLogMessage("I", "selectCrdIdno", String.format("id_p_seqno[%s]不存在crd_idno中", idPSeqno));
            return null;
        }
        if (recordCnt > 0) {
        	idNoObj = new IdNoObj();
        	idNoObj.idPSeqno = idPSeqno;
        	idNoObj.idNo = getValue("id_no");
        	idNoObj.birthday = getValue("birthday");
        	idNoObj.cellarPhone = getValue("cellar_phone");
        }
        return idNoObj;
    }
    /*************************************************************************/
	int selectCrdCard(IdNoObj idNoObj) throws Exception {
//		sqlCmd =  "SELECT card_no, new_end_date, activate_date, current_code ";
		sqlCmd =  "SELECT card_no, new_end_date  ";
		sqlCmd += " FROM crd_card";
		sqlCmd += " WHERE id_p_seqno = ? ";
		setString(1, idNoObj.idPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "selectCrdCard", String.format("id_p_seqno[%s]不存在crd_card中", idNoObj.idPSeqno));
			return (0);
		}
		for (int i = 0; i < recordCnt; i++) {
			String cardNo = getValue("card_no", i);
			String newEndDate = getValue("new_end_date", i);
//			String activateDate = getValue("activate_date", i);
//			String currentCode = getValue("current_code", i);
			
        	ICPSMQND psmqdObj = null;
        	if (psmqdMap.containsKey(cardNo.trim())) {
        		showLogMessage("D", "", "已存在檔案中");
        		psmqdObj = psmqdMap.get(cardNo);
//        		psmqdObj.activateDate = activateDate;
//        		psmqdObj.stopCode = convertCurrentCodeToStopCode(currentCode);
        		psmqdObj.activateDate = "";
        		psmqdObj.stopCode = "";
    		}else {
    			psmqdObj = new ICPSMQND();
    			psmqdObj.cardNo = cardNo;
//    			psmqdObj.activateDate = activateDate;
    			psmqdObj.activateDate = "";
    			psmqdObj.idNo = idNoObj.idNo;
    			psmqdObj.birthday = idNoObj.birthday;
    			psmqdObj.cellarPhone = idNoObj.cellarPhone;
//    			psmqdObj.stopCode = convertCurrentCodeToStopCode(currentCode);
    			psmqdObj.stopCode = "";
    			psmqdObj.modNo = "C";
    			psmqdObj.msgSendStatus = "Y";
    			psmqdObj.availableDate = convertToYYMM(newEndDate, cardNo);
    			psmqdObj.issueUnit = ISSUE_UNIT;
    		}
        	psmqdMap.put(psmqdObj.cardNo.trim(), psmqdObj);
        	
        	showLogMessage("D", "", "==========");
        	showLogMessage("I", "", String.format("card_no = [%s]", psmqdObj.cardNo));
        	showLogMessage("I", "", String.format("debit_flag = [%s]", "N"));
        	showLogMessage("D", "", String.format("開卡日期 = [%s]", psmqdObj.activateDate));
        	showLogMessage("D", "", String.format("id_no = [%s]", psmqdObj.idNo));
//        	showLogMessage("D", "", String.format("birthday = [%s]", psmqdObj.birthday));
        	showLogMessage("D", "", String.format("cellar_phone = [%s]", psmqdObj.cellarPhone));
//        	showLogMessage("D", "", String.format("停用碼 = [%s]", psmqdObj.stopCode));
//        	showLogMessage("D", "", String.format("異動碼 = [%s]", psmqdObj.modNo));
//        	showLogMessage("D", "", String.format("簡訊發送狀態 = [%s]", psmqdObj.msgSendStatus));
//        	showLogMessage("D", "", String.format("卡片有效日 = [%s]", psmqdObj.availableDate));
//        	showLogMessage("D", "", String.format("發卡單位代號 = [%s]", psmqdObj.issueUnit));
			
//            buf1data.cardNo = hCardNo; //卡片號碼
//            buf1data.activateDate = hActivateDate; //開卡日期
//            buf1data.idNo = hIdNo; //客戶代號(身分證字號)
//            buf1data.birthday = hBirthday; //主卡人出生日期
//            buf1data.cellarPhone = hCellarPhone; //主卡人行動電話
//            buf1data.modNo = "C"; //異動碼
//            buf1data.msgSendStatus = hMsgFlag; //簡訊發送狀態
//            buf1data.availableDate = hNewEndDate.substring(2, 6); //有效日期YYMM(西元年)
//            buf1data.issueUnit = ISSUE_UNIT; //發卡單位代號
//            outputAttrFilePath = buf1data.convertToRowOfData();
//            outputDataBufWri.write(outputAttrFilePath + "\r\n");
		}
		return (1);
	}
    /***********************************************************************/
    void insertFileCtl(CrdFileCtlObj crdFileCtlObj) throws Exception {
        daoTable = "crd_file_ctl";
        setValue("file_name", crdFileCtlObj.fileName);
        setValue("crt_date", sysDate);
        setValueInt("head_cnt", crdFileCtlObj.headCnt);
        setValueInt("record_cnt", crdFileCtlObj.recordCnt);
        setValue("trans_in_date", sysDate);
        insertTable();
        if (dupRecord.equals("Y")) {
            daoTable = "crd_file_ctl";
            updateSQL = "head_cnt  = ?,";
            updateSQL += " record_cnt = ?,";
            updateSQL += " trans_in_date = to_char(sysdate,'yyyymmdd')";
            whereStr = "where file_name  = ? ";
            setInt(1, crdFileCtlObj.headCnt);
            setInt(2, crdFileCtlObj.recordCnt);
            setString(3, crdFileCtlObj.fileName);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_crd_file_ctl not found!", "", "");
            }
        }
    }
    /***********************************************************************/
    void insertFileCtl1(String getFileName1) throws Exception {

		setValue("file_name", getFileName1);
		setValue("crt_date", sysDate);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
	}

	/***********************************************************************/
    public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);           
        if( !isNum.matches() ){               
            return false;
        }  return true;
 }
    /***********************************************************************/
    class ICPSMQND {
    	String cardNo; //卡片號碼
    	String activateDate; //開卡日期
    	String field3; //客戶可用餘額
    	String field4; //客戶信用限額
    	String field5; //上期帳戶帳單金額
    	String field6; //最後付款日期
    	String field7; //最後付款金額
    	String field8; //累積刷卡未結金額
    	String field9; //累積刷卡次數
    	String field10; //前一卡號
    	String field11; //主卡人中文姓名
    	String idNo; //客戶代號(身分證字號)
    	String birthday; //主卡人出生日期
    	String field14; //主卡人英文姓名
    	String field15; //客戶來源
    	String field16; //客戶聯絡人姓名
    	String field17; //主卡人中文地址1
    	String field18; //主卡人中文地址2
    	String field19; //主卡人公司名稱
    	String field20; //主卡人職稱
    	String field21; //有無自動扣繳碼
    	String field22; //主卡人所在郵遞區號
    	String field23; //主卡人所在住家電話
    	String field24; //主卡人公司電話
    	String cellarPhone; //主卡人行動電話
    	String field26; //主卡人E-MAIL位址
    	String field27; //主卡人ID
    	String field28; //卡別代號
    	String field29; //客戶等級碼
    	String field30; //遺失止付卡片日期
    	String field31; //卡片密碼變更
    	String field32; //發卡日
    	String field33; //製卡日期
    	String field34; //最近製卡日
    	String field35; //最近地址變更日
    	String field36; //最近地區變更日
    	String field37; //發卡次數
    	String blockCodeAndReasonCode; //保留欄位   // 2022/01/24: 第1~2碼=理由代碼、第3碼=控管碼
    	String field39; //主卡人卡片狀態
    	String stopCode; //停用碼/VIP碼
    	String field41; //保留欄位
    	String field42; //保留欄位
    	String field43; //行為等級
    	String field44; //摘要別
    	String field45; //最高預借金額
    	String field46; //最高刷卡金額
    	String field47; //最高消費金額
    	String field48; //保留欄位
    	String field49; //保留欄位
    	String modNo; //異動碼
    	String msgSendStatus; //簡訊發送狀態
    	String field52; //保留欄位
    	String availableDate; //有效日期YYMM(西元年)
    	String issueUnit; //發卡單位代號
    	String field55; //卡片流水號
    	String field56; //錯誤回覆碼
    	
    	String convertToRowOfData() throws UnsupportedEncodingException {
            String rtn = "";        
            rtn += fixLeft(cardNo, 19);
            rtn += fixLeft(activateDate, 8);
            rtn += fixLeft(field3, 14);
            rtn += fixLeft(field4, 14);
            rtn += fixLeft(field5, 14);
            rtn += fixLeft(field6, 8);
            rtn += fixLeft(field7, 14);
            rtn += fixLeft(field8, 14);
            rtn += fixLeft(field9, 12);
            rtn += fixLeft(field10, 19);
            rtn += fixLeft(field11, 40);
            rtn += fixLeft(idNo, 20);
			rtn += fixLeft(birthday, 8);
            rtn += fixLeft(field14, 40);
            rtn += fixLeft(field15, 40);
            rtn += fixLeft(field16, 40);
            rtn += fixLeft(field17, 40);
            rtn += fixLeft(field18, 40);
            rtn += fixLeft(field19, 30);
            rtn += fixLeft(field20, 30);
            rtn += fixLeft(field21, 3);
            rtn += fixLeft(field22, 16);
            rtn += fixLeft(field23, 20);
            rtn += fixLeft(field24, 20);
			rtn += fixLeft(cellarPhone, 20);
            rtn += fixLeft(field26, 40);
            rtn += fixLeft(field27, 30);
            rtn += fixLeft(field28, 2);
            rtn += fixLeft(field29, 2);
            rtn += fixLeft(field30, 8);
            rtn += fixLeft(field31, 8);
            rtn += fixLeft(field32, 8);
            rtn += fixLeft(field33, 8);
            rtn += fixLeft(field34, 8);
            rtn += fixLeft(field35, 8);
            rtn += fixLeft(field36, 8);
			rtn += fixLeft(field37, 2);
            rtn += fixLeft(blockCodeAndReasonCode, 3);
            rtn += fixLeft(field39, 1);
            rtn += fixLeft(stopCode, 1);
            rtn += fixLeft(field41, 1);
            rtn += fixLeft(field42, 1);
            rtn += fixLeft(field43, 4);
            rtn += fixLeft(field44, 1);
            rtn += fixLeft(field45, 14);
            rtn += fixLeft(field46, 14);
            rtn += fixLeft(field47, 14);
            rtn += fixLeft(field48, 8);
			rtn += fixLeft(field49, 6);
			rtn += fixLeft(modNo, 1);
			rtn += fixLeft(msgSendStatus, 1);
            rtn += fixLeft(field52, 30);
			rtn += fixLeft(availableDate, 4);
			rtn += fixLeft(issueUnit, 8);
			rtn += fixLeft(field55, 7);
			rtn += fixLeft(field56, 4);
            return rtn;
    	}
    	
    	String fixLeft(String str, int len) throws UnsupportedEncodingException {
    	    int size = (Math.floorDiv(len, 100) + 1) * 100;
    	    String spc = "";
    	    for (int i = 0; i < size; i++)    spc += " ";
    	    if (str == null)                  str  = "";
    	    str = str + spc;
    	    byte[] bytes = str.getBytes("MS950");
    	    byte[] vResult = new byte[len];
    	    System.arraycopy(bytes, 0, vResult, 0, len);

    	    return new String(vResult, "MS950");
    	}	
    }
    /***********************************************************************/
    class PSMQAObj {
		final String dataProduceUnit = ISSUE_UNIT;
		final String dataReceiveUnit = FISC_UNIT;
		String dataCount;
		String errCode;
//		String filler;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(dataProduceUnit, 8);
			rtn += fixLeft(dataReceiveUnit, 8);
			rtn += fixLeft(dataCount, 7);
			rtn += fixLeft(errCode, 4);
//			rtn += fixLeft(filler, 4);
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
    /***********************************************************************/
    class TempFileObj {
		String uploadFileName;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += fixLeft(uploadFileName, 16);
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

	/****************************************************************************/
    public static void main(String[] args) throws Exception {
    	CrdF071 proc = new CrdF071();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /****************************************************************************/
	int procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "FISC_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/icu/out", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      
	      int sendOkCnt = 0;
	      
	      for (int i = 0; i < outputDataFileCtlList.size(); i++) {
	    	  // SEND DATA file
	    	  String outputDataFileName = outputDataFileCtlList.get(i).fileName;  	  
		      showLogMessage("I", "", "mput " + outputDataFileName + " 開始傳送....");
		      int errcode = commFTP.ftplogName("FISC_FTP_PUT", "mput " + outputDataFileName);
		      
		      if (errcode != 0) {
		          showLogMessage("I", "", "ERROR:無法傳送 " + outputDataFileName + " 資料"+" errcode:"+errcode);
		          insertEcsNotifyLog(outputDataFileName);          
		      }else {
		    	  sendOkCnt ++;
		      }
		      
		      // SEND ATTR file
		      String outputAttrFileName = outputAttrFileCtlList.get(i).fileName;
		      showLogMessage("I", "", "mput " + outputAttrFileName + " 開始傳送....");
		      int errCode1 = commFTP.ftplogName("FISC_FTP_PUT", "mput " + outputAttrFileName);
		      
		      if (errCode1 != 0) {
		          showLogMessage("I", "", "ERROR:無法傳送 " + outputAttrFileName + " 資料"+" errcode:"+errCode1);
		          insertEcsNotifyLog(outputAttrFileName);          
		      }else {
		    	  sendOkCnt ++;
		      }
		  }

	      // SEND TEMP file
	      showLogMessage("I", "", "mput " + outputTempFileName + " 開始傳送....");
	      int errCode2 = commFTP.ftplogName("FISC_FTP_PUT", "mput " + outputTempFileName);
	      
	      if (errCode2 != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + outputTempFileName + " 資料"+" errcode:"+errCode2);
	          insertEcsNotifyLog(outputTempFileName);          
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
	  /****************************************************************************/
		void renameFile1(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/icu/out/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
		/****************************************************************************/
		void renameFile2(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/icu/out/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
		
		/****************************************************************************/
		void renameErrFile(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/icu/error/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + outputDataFileName;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
		
		/**********************************************************************/
		
		private void putErrReportList(ErrorReason errorCode, TempICPSMQND inputObj) throws Exception {
			this.errCode = errorCode;
			createErrReport(inputObj);
		}
		
		/**********************************************************************/
		void createErrReport(TempICPSMQND inputObj) throws Exception {

			ErrorReportObj errorReportObj = new ErrorReportObj();

			icud017ErrorFileRecordCnt++;
			errorReportObj.idNo = inputObj.tmpIdNo;
			errorReportObj.cardNo = inputObj.tmpCardNo;

			errorReportObj.errReason = errCode.getErrorCode();

			errorReportObj.date = sysDate;

			String tempStr = errorReportObj.allText();
			lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1, "0", tempStr));
			return;
		}
		/**********************************************************************/
}

/***********************************************************************/
class ErrorReportObj {
	String cardNo;
	String idNo;
	String errReason;
	String date;

	String allText() throws UnsupportedEncodingException {
		String rtn = "";
		rtn += fixLeft(idNo, 20);
		rtn += fixLeft(cardNo, 19);
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

/**********************************************************************/

class TempICPSMQND{
	String blockCode;
	String reasonCode;
	String tmpCardNo;
	String tmpActivateDate;
	String tmpCellarPhone;
	String tmpIdNo;
	String tmpModNo;
	String tmpBirthDay;
	String tmpCardType;
	String binType;
	String newEndDate;
	
	
	String supFlag = "";
}

/***********************************************************************/
class DbcDataD017 {
	String idPSeqno;
	String acctNo;
	String idNo;
	String idNoCode;
}

/***********************************************************************/
class CardObj {
	String cardNo;
	String idPSeqno;
	String activateDate; // credit card
	String currentCode;
	String newEndDate;
}

/***********************************************************************/
class IdNoObj {
	String idPSeqno;
	String idNo;
	String birthday;
	String cellarPhone;
}
/***********************************************************************/
class CrdFileCtlObj {
	String fileName;
	int headCnt = 0;
	int recordCnt = 0;
	CrdFileCtlObj(String fileName){
		this.fileName = fileName;
	}
}
/***********************************************************************/
class EcsCardStatus{
	public  String oppostReason = "";
	public  String currentCode = "";
	public  String blockReasonOrSpecStatus = "";
	public  String cardClass = "";
	public  String specOutgoReason = "";
	public  String specNegReason = "";
	public static EcsCardStatus getEcsCardStatus(String blockCode, String reasonCode){
        EcsCardStatus cardStatus = new EcsCardStatus();
		switch(blockCode) {
		case "T":
			switch(reasonCode) {
			case "01":
				cardStatus.blockReasonOrSpecStatus = "01";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "02":
				cardStatus.blockReasonOrSpecStatus = "T2";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "03":
				cardStatus.blockReasonOrSpecStatus = "0A";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "04":
				cardStatus.blockReasonOrSpecStatus = "T4";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "05":
				cardStatus.blockReasonOrSpecStatus = "T5";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "06":
				cardStatus.blockReasonOrSpecStatus = "T6";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "07":
				cardStatus.blockReasonOrSpecStatus = "0E";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "08":
				cardStatus.blockReasonOrSpecStatus = "T8";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "09":
				cardStatus.blockReasonOrSpecStatus = "09";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "11":
				cardStatus.blockReasonOrSpecStatus = "06";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "12":
				cardStatus.blockReasonOrSpecStatus = "0C";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "13":
				cardStatus.blockReasonOrSpecStatus = "0F";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "14":
				cardStatus.blockReasonOrSpecStatus = "14";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "15":
				cardStatus.blockReasonOrSpecStatus = "15";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "21":
				cardStatus.blockReasonOrSpecStatus = "T1";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "22":
				cardStatus.blockReasonOrSpecStatus = "22";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "23":
				cardStatus.blockReasonOrSpecStatus = "23";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "24":
				cardStatus.blockReasonOrSpecStatus = "24";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "25":
				cardStatus.blockReasonOrSpecStatus = "T4";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "26":
				cardStatus.blockReasonOrSpecStatus = "26";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "0A":
				cardStatus.blockReasonOrSpecStatus = "0X";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "0B":
				cardStatus.blockReasonOrSpecStatus = "0N";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "31":
				cardStatus.blockReasonOrSpecStatus = "04";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "32":
				cardStatus.blockReasonOrSpecStatus = "32";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			case "34":
				cardStatus.blockReasonOrSpecStatus = "34";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "35":
				cardStatus.blockReasonOrSpecStatus = "35";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "36":
				cardStatus.blockReasonOrSpecStatus = "36";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "37":
				cardStatus.blockReasonOrSpecStatus = "37";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			case "38":
				cardStatus.blockReasonOrSpecStatus = "38";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "1";
				break;
			default:
				cardStatus.blockReasonOrSpecStatus = "09";
				cardStatus.currentCode = "0";
				cardStatus.cardClass = "2";
				break;
			}
			break;
		// =====================================
		case "E":
			switch(reasonCode) {
			case "01":
				cardStatus.oppostReason = "AP";
				cardStatus.currentCode = "1";
				break;
			case "02":
				cardStatus.oppostReason = "T1";
				cardStatus.currentCode = "3";
				break;
			case "03":
				cardStatus.oppostReason = "F1";
				cardStatus.currentCode = "4";
				break;
			case "04":
				cardStatus.oppostReason = "E4";
				cardStatus.currentCode = "4";
				break;
			case "05":
				cardStatus.oppostReason = "A1";
				cardStatus.currentCode = "1";
				break;
			case "06":
				cardStatus.oppostReason = "B3";
				cardStatus.currentCode = "1";
				break;
			case "07":
				cardStatus.oppostReason = "AA";
				cardStatus.currentCode = "1";
				break;
			case "08":
				cardStatus.oppostReason = "B4";
				cardStatus.currentCode = "1";
				break;
			case "09":
				cardStatus.oppostReason = "11";
				cardStatus.currentCode = "1";
				break;
			case "10":
				cardStatus.oppostReason = "AX";
				cardStatus.currentCode = "1";
				break;
			case "11":
				cardStatus.oppostReason = "EB";
				cardStatus.currentCode = "4";
				break;
			case "12":
				cardStatus.oppostReason = "C2";
				cardStatus.currentCode = "4";
				break;
			case "13":
				cardStatus.oppostReason = "ED";
				cardStatus.currentCode = "4";
				break;
			case "14":
				cardStatus.oppostReason = "EE";
				cardStatus.currentCode = "4";
				break;
			case "15":
				cardStatus.oppostReason = "R1";
				cardStatus.currentCode = "1";
				break;
			case "44":
				cardStatus.oppostReason = "AY";
				cardStatus.currentCode = "1";
				break;
			default:
				cardStatus.oppostReason = "10";
				cardStatus.currentCode = "1";
				break;
			}
			break;
		// =====================================
		case "X":
			cardStatus.oppostReason = "AX";
			cardStatus.currentCode = "1";
			break;
		// ===================================== 2022/04/12 Justin 新增 A、B、F、L、S、O
		case "A":
			cardStatus.oppostReason = "A2";
			cardStatus.currentCode = "1";
			break;
		case "B":
			switch(reasonCode.trim()) {
			case "0":
			case "00":
				cardStatus.oppostReason = "J2";
				cardStatus.currentCode = "3";
				break;
			case "2":
			case "02":
				cardStatus.oppostReason = "H1";
				cardStatus.currentCode = "3";
				break;
			case "3":
			case "03":
				cardStatus.oppostReason = "Z2";
				cardStatus.currentCode = "3";
				break;
			case "4":
			case "04":
				cardStatus.oppostReason = "U1";
				cardStatus.currentCode = "3";
				break;
			case "5":
			case "05":
				cardStatus.oppostReason = "B5";
				cardStatus.currentCode = "3";
				break;
			case "6":
			case "06":
				cardStatus.oppostReason = "B6";
				cardStatus.currentCode = "3";
				break;
			default:
				cardStatus.oppostReason = "30";
				cardStatus.currentCode = "3";
				break;
			}
			break;
		case "F":
			switch(reasonCode) {
			case "FM":
				cardStatus.oppostReason = "M1";
				cardStatus.currentCode = "5";
				break;
			case "FN":
				cardStatus.oppostReason = "N1";
				cardStatus.currentCode = "5";
				break;
			case "FO":
				cardStatus.oppostReason = "AK";
				cardStatus.currentCode = "5";
				break;
			default:
				cardStatus.oppostReason = "M2";
				cardStatus.currentCode = "5";
				break;
			}
			break;
		case "L":
			cardStatus.oppostReason = "C1";
			cardStatus.currentCode = "2";
			break;
		case "S":
			cardStatus.oppostReason = "S0";
			cardStatus.currentCode = "2";
			break;
		case "O":
			cardStatus.oppostReason = "O1";
			cardStatus.currentCode = "5";
			break;
		default:
			return null;
		}
		
		
		return cardStatus;
		
	}
}
