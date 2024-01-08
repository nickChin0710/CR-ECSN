/*******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  111/02/10  V1.00.00    Justin       program initial                        *
*  111/02/13  V1.00.01    Justin       add empty in the end of the attr files *
*  111/02/16  V1.00.02     Ryan      big5 to MS950                            *
*  111/03/31  V1.00.03    Justin       起始編號修改及增加input參數            *
*  111/03/31  V1.00.04    Justin       起始編號修改為50開始                   *
*  111/04/01  V1.00.05    Justin       將CrdF071的IcuD017併入CrdF074          *
*  111/04/07  V1.00.06    Justin       when id_p_seqno selected from XXX_card *
*                                      is empty, continue to do next record   *
*             V1.00.07    Justin       讀取開卡日                             *
*  111/04/08  V1.00.08    Justin       Update current_code成功，則新增        *
*                                      停卡INSERT OUNTGOING                   *
*  111/04/12  V1.00.09    Justin       新增A、B、F、L、S、O code              *
*  111/04/13  V1.00.10    Justin       若卡片已停卡則不更新current_code       *
*                                      避免重複UPDATE CRD_IDNO_SEQNO          *
*                                      更新異動信用卡ID及異動VD卡ID邏輯       *
*  111/04/20  V1.00.11    Justin       增加顯示個別ID變更筆數                 *
*             V1.00.12                 修改update條件                         *
*  111/04/21  V1.00.13    Justin       修改input參數                          *
*  111/04/29  V1.00.14    Justin       mark some log messages、調整log        *
*  111/05/03  V1.00.15    Justin       新增insert cca_spe_his                 *
*  111/05/11  V1.00.16    Justin       update dba_acno增加acct_holder_id      *
*  111/05/16  V1.00.17    Justin       凍結、特指增加INSEERT RSK_ACNOLOG      *
*  111/05/17  V1.00.18    Justin       fix SQL error                          *
*  111/05/20  V1.00.19    Justin       調整3D簡訊發送問題                     *
*  111/05/23  V1.00.20    Justin       crd_chg_id update old_id_p_seqno       *   
*  111/06/10  V1.00.21    Justin       print cellar_phone for debugging       *
*  111/08/24  V1.00.22    Ryan         增加一個判斷，來控制是否執行runIcuD017      *
*  111/09/13  V1.00.23    Wilson       update act_acno增加card_indicator = '1' *
*  111/10/05  V1.00.24    Wilson       updateCrdAcnoSetIdNo、updateDbaAcnoSetIdNo 找不到資料繼續往下執行
*  111/10/21  V1.00.25    Ryan         增加凍結、特指送黑名單功能                                                     *
*  112/03/24  V1.00.26    Ryan         197~209行讀取檔案的部分，改成全部都直接讀DB取得資料                                                  *
*  112/04/12  V1.00.27    Ryan         合併P2與P3程式，抓取參數判別要走P2或P3邏輯             *
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

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommSecr;
import com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException;

import Cca.CcaOutGoing;

import com.CommRoutine;

import Dxc.Util.SecurityUtil;


public class CrdF074 extends AccessDAO {

    public static final boolean DEBUG_MODE = false;
    
    private static final int RECORD_CNT_PER_FILE = 5000;

    private final String progname = "產生送財金信用卡開卡資料檔(使用ICUD17格式)程式 112/04/12 V1.00.27";
    private final String OUTPUT_DATA_FILE = "ICPSMQND";
    private final String OUTPUT_ATTR_FILE = "ICPSMQNA";

    private final String ISSUE_UNIT_CODE = "M00600000";
    private final String ISSUE_UNIT = "00600000";
    private final String FISC_UNIT = "95000000";
    
    // CrdF071
    private static final String DBC_IDNO = "DBC_IDNO";
	private static final String CRD_IDNO = "CRD_IDNO";
	
	private String prodFlag = "";
	private boolean shouldProdFile = true;
	private boolean shouldDoOpenCard = true;
	private boolean isRunIcuD017 = true;
	
	private List<CrdFileCtlObj> icud017InputFileNames = new ArrayList<CrdFileCtlObj>();
    private List<CrdFileCtlObj> icud017ErrorFileNames = new ArrayList<CrdFileCtlObj>();
	
	private String errFileName = "";
    private String errFilePath = "";
	
    private String queryDate = "";
    private final String INPUT_FILE = "ICPSMQND";
    
    private int totalInputData = 0;
    
    //
    
    Map<String, ICPSMQND> psmqdMap = new HashMap<String, ICPSMQND>();
    
    
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
    CcaOutGoing ccaOutGoing = null;
    

    int debug = 0;

    String prgmId = "CrdF074";
    String hModUser = "";
    int hMinute = 0;
    String hSystemDate = "";
    String hModPgm = "";
    int totalRecordCnt = 0;
    int totalSuccessChgDbcIdCnt = 0;
    int totalSuccessChgCrdIdCnt = 0;
    
    String modAndOpenCardDate = "";
    
    String beginModAndOpenCardDate = "";
    String endModAndOpenCardDate = "";
    
    
    BufferedWriter outputDataBufWri = null;
    BufferedWriter outputAttrBufWri = null;

    String outputDataFilePath = "";
    String outputAttrFilePath = "";

	String outputDataFileName = "";
	String outputAttrFileName = "";
	ArrayList<CrdFileCtlObj> outputDataFileCtlList = new ArrayList<CrdFileCtlObj>();
	ArrayList<CrdFileCtlObj> outputAttrFileCtlList = new ArrayList<CrdFileCtlObj>();
	
	String rptName1 = "";

    int rptSeq1 = 0;
    int icud017ErrorFileRecordCnt = 0;
    int errFileNo = 0;
    String wfValueP2 = "";

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
	ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());

	hModUser = comc.commGetUserID();

	hModPgm = javaProgram;
   
	selectPtrSysParmP2();

	int icud017InputFileCnt = 0;
	if("Y".equals(wfValueP2)) {
		/*** 取得系統日期 ***/
		commonRtnP2();
		/*** 讀取Args ***/
		loadArgs(args);
		if(isRunIcuD017) {
			// 2022/03/31 merge CrdF071 into CrdF074
			// (舊IcuD017)
			// 找出CARDLINK手機異動資料檔
			// 並記錄M00600000.ICPSMQND.YYMMDDNN檔案數
			showLogMessage("I", "", "找出CARDLINK手機異動資料檔(舊IcuD017)");
			icud017InputFileCnt = runIcuD017();
			showLogMessage("I", "",String.format("CARDLINK手機異動資料檔(舊IcuD017)：處理完成[%d]個檔案、[%d]筆資料", icud017InputFileCnt, totalInputData));
			showLogMessage("I", "", String.format("CARDLINK手機異動資料檔(舊IcuD017)：成功變更[%d]筆ID，其中變更VD卡[%d]筆ID、變更信用卡[%d]筆ID",totalSuccessChgDbcIdCnt + totalSuccessChgCrdIdCnt, totalSuccessChgDbcIdCnt, totalSuccessChgCrdIdCnt));
		}
	}else{
		if(args.length == 0) {
			commonRtn();
		} else
	    if(args.length == 1) {
	        if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
	            showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
	            return -1;
	        }
	        hSystemDate = args[0];
	    }else {
	        comc.errExit("參數1：非必填，預設為系統日前一日，也可輸入西元年(如：20200715)", "");
	    }     
		showLogMessage("E", "", String.format("系統日前一日[%s]", hSystemDate));
	}

	/*** 前一天有異動卡況或開卡的卡號 ***/
	if (shouldDoOpenCard) {
		// 判斷是否有M00600000.ICPSMQND.YYMMDDNN
		// 若有，則讀取「前一天有異動卡況或開卡的卡號」
		showLogMessage("I", "", "開始讀取「前一天有異動卡況或開卡的卡號」......");
		if("Y".equals(wfValueP2)) {
			selectCardsModOrOpenYesterdayP2();
		}else {
			selectCardsModOrOpenYesterday();
		}
	}else {
		showLogMessage("I", "", String.format("因產檔參數為[%s]，所以不讀取「前一天有異動卡況或開卡的卡號」", prodFlag));
	}
	
	/*** 產檔 ***/
	int ftpOkFileCnt = 0;
	if (shouldProdFile == false) {
		showLogMessage("I", "", String.format("因產檔參數為[%s]，所以不產檔", prodFlag));
	} else {
		
		/*** 開啟產生檔案 ***/
		checkOpen();

		createICPSMQND();

		/*** 關閉檔案 ***/
		if (outputDataBufWri != null)
			outputDataBufWri.close();

		if (outputAttrBufWri != null)
			outputAttrBufWri.close();

		// 無資料須傳輸，因此刪除檔案
		if (totalRecordCnt == 0) {
			String path = String.format("%s/media/icu/out/%s", comc.getECSHOME(), outputDataFileName);
			path = SecurityUtil.verifyPath(path);
			File file = new File(path);
			file.delete();

			String path1 = String.format("%s/media/icu/out/%s", comc.getECSHOME(), outputAttrFileName);
			path1 = SecurityUtil.verifyPath(path1);
			File file1 = new File(path1);
			file1.delete();

		} else {
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			ftpOkFileCnt = procFTP();

			renameOutputFile();

			insertOutputFileCtl();

		}
	}
	
	int totalErrorCnt = 0;
	// move icud017 error files
	for (int i = 0; i < icud017ErrorFileNames.size(); i++) {
		CrdFileCtlObj errFileObj = icud017ErrorFileNames.get(i);
		insertFileCtl(errFileObj);
		procFTPErrorFile(errFileObj.fileName);
		renameErrFile(errFileObj.fileName);
		totalErrorCnt += errFileObj.recordCnt;
	}
	
	// move icud017 input files 
	for (int i = 0; i < icud017InputFileNames.size(); i++) {
		CrdFileCtlObj fileObj = icud017InputFileNames.get(i);
		String orgFileName = fileObj.fileName;
		// 2022/02/13 Justin 避免與CrdF074產出的檔案衝突，因此加上CLK來做區別
		fileObj.fileName = renameToCardLinkFileName(orgFileName);
		insertFileCtl(fileObj);
		renameFile(orgFileName, fileObj.fileName);
	}
	if("Y".equals(wfValueP2)) {
		showLogMessage("I", "", String.format("CARDLINK手機異動資料檔(舊IcuD017)：處理完成[%d]個檔案、[%d]筆資料", icud017InputFileCnt, totalInputData));
		showLogMessage("I", "", String.format("CARDLINK手機異動資料檔(舊IcuD017)：成功變更[%d]筆ID，其中變更VD卡[%d]筆ID、變更信用卡[%d]筆ID", totalSuccessChgDbcIdCnt + totalSuccessChgCrdIdCnt, totalSuccessChgDbcIdCnt, totalSuccessChgCrdIdCnt));
		showLogMessage("I","",String.format("產出錯誤報表[%d]，錯誤資料總筆數[%d]", icud017ErrorFileNames.size(), totalErrorCnt));
	}
	showLogMessage("I","",String.format("送出檔案[%d]，資料總筆數[%d]", ftpOkFileCnt, totalRecordCnt));

   // ==============================================
   // 固定要做的
   // comcr.callbatch(1, 0, 0);
   showLogMessage("I", "", "執行結束");
   finalProcess();
   ccaOutGoing.finalCnt2();
   return 0;
  } catch (Exception ex) {
	expMethod = "mainProcess";
	expHandle(ex);
	return exceptExit;
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

private int runIcuD017() throws Exception {
	return openFileIcuD017();
}

private int openFileIcuD017() throws Exception {
	int fileCount = 0;

	String tmpstr = String.format("%s/media/icu", comc.getECSHOME());

	List<String> listOfFiles = comc.listFsSort(tmpstr);

	String fileNameTemplate  = ""; // String fileNameTemplate
//	String fileNameTemplate2 = ""; // previous business date

	if (queryDate.length() > 0) {
		fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
				ISSUE_UNIT_CODE, 
				INPUT_FILE,
				new CommDate().getLastTwoTWDate(queryDate), 
				queryDate.substring(4, 8)); // 檔案正規表達式
		
		showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate));
	}else {
		fileNameTemplate = String.format("%s\\.%s\\.[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9].*", 
				ISSUE_UNIT_CODE, 
				INPUT_FILE); // 檔案正規表達式
		
//		fileNameTemplate = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
//				ISSUE_UNIT_CODE, 
//				INPUT_FILE,
//				new CommDate().getLastTwoTWDate(hBusiBusinessDate), 
//				hBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
		
//		fileNameTemplate2 = String.format("%s\\.%s\\.%s%s[0-9][0-9].*", 
//				ISSUE_UNIT_CODE, 
//				INPUT_FILE,
//				new CommDate().getLastTwoTWDate(hPreviousBusiBusinessDate), 
//				hPreviousBusiBusinessDate.substring(4, 8)); // 檔案正規表達式
		
		showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate));
//		showLogMessage("I", "", String.format("尋找檔案[%s] 或", fileNameTemplate));
//		showLogMessage("I", "", String.format("尋找檔案[%s]", fileNameTemplate2));
	}

	ArrayList<String> matchedFileList = new ArrayList<String>();
	
	for (String file : listOfFiles) {
		if (file.matches(".*\\.bak"))
			continue;
//		if ( !( file.matches(fileNameTemplate) || ( fileNameTemplate2.length() > 0 && file.matches(fileNameTemplate2) ) ))
		if ( file.matches(fileNameTemplate) == false )
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
		if (idPSeqnoSelectedByTxtCardNo == null || idPSeqnoSelectedByTxtCardNo.trim().isEmpty()) {
			continue;
		}
		
		if (checkExtendCode(extendCode, inputObj)) {
			
			switch (inputObj.tmpCardType) {
			
			case "VD":
				System.out.println(String.format("檢查碼為:%s", (String) dataMap.get("mod_no")));
				
				updateOppstReasonSpecStatusAndAndCurrentCode(inputObj);
				
				// check 異動碼是否為C
				if ( "C".equalsIgnoreCase( (String)dataMap.get("mod_no") ) ) {	
					DbcDataD017 dbaData = getDbcData(inputObj.tmpCardNo);
					if (dbaData != null && isIdChg(dbaData.idNo, inputObj.tmpIdNo)) {
						log(String.format("Update id_no. [id_p_seqno = %s]", dbaData.idPSeqno));
						boolean isupdateOk = updateIdNo(inputObj.tmpIdNo, dbaData, inputObj);
						if (isupdateOk == false) continue;
					}
				}
				
				if (checkIdno(DBC_IDNO, inputObj, idPSeqnoSelectedByTxtCardNo) == false) {
					rollbackDataBase();
					continue;
				}
				updateDbcIdnoCellPhoneAndBirthday(inputObj);
				break;
				
			default:
				updateOppstReasonSpecStatusAndAndCurrentCode(inputObj);
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

//2022-10-21 V1.00.09 Ryan 增加凍結、特指送黑名單功能
private boolean updateOppstReasonSpecStatusAndAndCurrentCode(TempICPSMQND inputObj) throws Exception {
	EcsCardStatus ecsCardStatus = EcsCardStatus.getEcsCardStatus(inputObj.blockCode, inputObj.reasonCode);
	if (ecsCardStatus == null) {
		showLogMessage("I", "", String.format("控管碼[%s], 理由代碼[%s]此程式不處理", inputObj.blockCode, inputObj.reasonCode));
		return true;
	}
	boolean updateResult = false;
	switch(inputObj.blockCode) {
	case "T":
		switch(ecsCardStatus.cardClass) {
		case "1": //凍結(戶)
			updateBlockReason(inputObj, ecsCardStatus);
			insertRskAcnolog("A", inputObj, ecsCardStatus); // 2022/05/16 Justin 凍結、特指增加INSEERT RSK_ACNOLOG
			ccaOutGoing.InsertCcaOutGoingBlock(inputObj.tmpCardNo, ecsCardStatus.currentCode, sysDate, ecsCardStatus.blockReasonOrSpecStatus);
			break;
		case "2": //特指(卡)
			updateResult = updateSpecStatus(inputObj, ecsCardStatus);
			if (updateResult) {
				getSpecReason(inputObj, ecsCardStatus);
				insertCcaSpeHis(inputObj, ecsCardStatus);
				insertRskAcnolog("C", inputObj, ecsCardStatus); // 2022/05/16 Justin 凍結、特指增加INSEERT RSK_ACNOLOG
				ccaOutGoing.InsertCcaOutGoingBlock(inputObj.tmpCardNo, ecsCardStatus.currentCode, sysDate, ecsCardStatus.blockReasonOrSpecStatus);
			}
			break;
		}
		break;
	case "E":
	case "X":
	case "A": // 2022/04/12 Justin 新增A、B、F、L、S、O
	case "B":
	case "F":
	case "L":
	case "S":
	case "O":
		// 2022/03/31 If block code equals "E", mark the data in the map as block code E
		// 2022/04/12 If block code equals "E","A","B","F","L","S","O", mark the data in the map as "cannotBeSent"
		ICPSMQND obj = psmqdMap.get(inputObj.tmpCardNo.trim());
		if (obj != null) obj.cannotBeSent = true;
		// 2022/04/13 Justin if current_code != '0', update crd_card or dbc_card, and if update successfully, return true;
		updateResult = updateCurrentCodeAndOppostReason(inputObj, ecsCardStatus);
		// 2022/04/08 Justin Update current_code成功，則新增停卡INSERT OUNTGOING
		if (updateResult) ccaOutGoing.InsertCcaOutGoing(inputObj.tmpCardNo, ecsCardStatus.currentCode, sysDate, ecsCardStatus.oppostReason);
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
    
    // 2022/05/16 Justin 修改where條件
//    switch(inputObj.tmpCardType) {
//	case "VD":
//		whereStr   = " where ACNO_P_SEQNO = (select P_SEQNO from DBC_CARD where card_no = ? ) ";  
//		break;
//	default:
//		whereStr   = " where ACNO_P_SEQNO = (select ACNO_P_SEQNO from CRD_CARD where card_no = ? ) ";  
//		break;
//	}
    whereStr = " where CARD_ACCT_IDX = (select CARD_ACCT_IDX from CCA_CARD_BASE where card_no = ? ) ";
    
    setString(5, inputObj.tmpCardNo);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", String.format("update CCA_CARD_ACCT not found, card_type[%s], card_no[%s]", inputObj.tmpCardType, inputObj.tmpCardNo));
	}
	
}

/****************************************************************************/

private boolean updateSpecStatus(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {

	daoTable = "CCA_CARD_BASE";
	updateSQL =  " SPEC_FLAG = 'Y' ,";
	updateSQL += " SPEC_STATUS  = ? ,";
	updateSQL += " SPEC_DATE  = ? ,";
	updateSQL += " SPEC_TIME  = ? ,";
	updateSQL += " SPEC_USER  = ? ,";
	updateSQL += " SPEC_DEL_DATE  = ? ,";  // 2022/05/16 Justin update cca_card_base增加spec_del_date = inputObj.newEndDate
	updateSQL += " MOD_USER  = ? ,";
    updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
    updateSQL += " MOD_PGM  = ? ";
    whereStr   = " where CARD_NO = ? ";
    setString(1, ecsCardStatus.blockReasonOrSpecStatus);
    setString(2, sysDate);
    setString(3, sysTime);
    setString(4, prgmId);
    setString(5, inputObj.newEndDate);
    setString(6, prgmId);
    setString(7, sysDate + sysTime);
    setString(8, prgmId);
    setString(9, inputObj.tmpCardNo);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", String.format("update CCA_CARD_BASE not found, card_type[%s], card_no[%s]", inputObj.tmpCardType, inputObj.tmpCardNo));
		return false;
	}
	
	return true;
}

/****************************************************************************/

private boolean updateCurrentCodeAndOppostReason(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
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
    whereStr   = " where CARD_NO = ? AND CURRENT_CODE = '0' ";
    setString(1, ecsCardStatus.currentCode);
    setString(2, sysDate);
    setString(3, ecsCardStatus.oppostReason);
    setString(4, prgmId);
    setString(5, sysDate + sysTime);
    setString(6, prgmId);
    setString(7, inputObj.tmpCardNo);

	int cnt = updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", String.format("update crd_card or dbc_card CURRENT_CODE not found, card_type[%s], card_no[%s]", inputObj.tmpCardType, inputObj.tmpCardNo));
		return false;
	}
	
	return (cnt > 0) ? true : false;
}

/****************************************************************************/

private boolean insertRskAcnolog(String kindFlag, TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
	RskAcnologObj obj = getRskAcnologObj(kindFlag, inputObj, ecsCardStatus);
	if (obj == null) {
		showLogMessage("W", "", "insertRskAcnolog: unable to getRskAcnologObj, card_no[" + inputObj.tmpCardNo + "]");
		return false;
	}
	setValue("kind_flag", kindFlag);
	setValue("card_no", obj.cardNo);
	setValue("acno_p_seqno", obj.acnoPSeqno);
	setValue("acct_type", obj.acctType);
	setValue("id_p_seqno", obj.idPSeqno);
	setValue("corp_p_seqno", obj.corpPSeqno);
	setValue("log_date", sysDate);
	setValue("log_mode", "1");
	setValue("log_type", obj.logType);
	setValue("log_reason", obj.logReason);
	setValue("log_remark", obj.logRemark);
	setValue("fit_cond", obj.fitCond);
	setValue("block_reason", obj.blockReason);
	setValue("spec_status", obj.specStatus);
	setValue("spec_del_date", obj.specDelDate);
	setValue("sms_flag", obj.smsFlag);
	setValue("user_dept_no", obj.userDeptNo);
	setValue("send_ibm_flag", "");
	setValue("send_ibm_date", "");
	setValue("apr_flag", "Y");
	setValue("apr_date", sysDate);
	setValue("mod_user", prgmId);
	setValue("mod_time", sysDate + sysTime);
	setValue("mod_pgm", prgmId);
	daoTable = "RSK_ACNOLOG";
	int insertCnt = insertTable();
	if (insertCnt <= 0) {
		comcr.errRtn("Error! insert RSK_ACNOLOG error!", "", "");
	}

	return true;
}

/***********************************************************************/
private RskAcnologObj getRskAcnologObj(String kindFlag, TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
	RskAcnologObj obj = null;
	switch(kindFlag) {
	case "A":
		obj = getCcaCardAcct(inputObj, ecsCardStatus);
		break;
	case "C":
		obj = getCcaCardBase(inputObj, ecsCardStatus);
		break;
	default:
		return obj;
	}
	return obj;
}

/****************************************************************************/
private RskAcnologObj getCcaCardAcct(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
	RskAcnologObj obj = null;
	
	sqlCmd =  "SELECT acno_p_seqno, acct_type , id_p_seqno , corp_p_seqno , spec_remark, ";
	sqlCmd += " block_reason1, spec_status, spec_del_date, block_sms_flag ";
	sqlCmd += "from CCA_CARD_ACCT ";
    // 2022/05/16 Justin 修改where條件
//  switch(inputObj.tmpCardType) {
//	case "VD":
//		whereStr   = " where ACNO_P_SEQNO = (select P_SEQNO from DBC_CARD where card_no = ? ) ";  
//		break;
//	default:
//		whereStr   = " where ACNO_P_SEQNO = (select ACNO_P_SEQNO from CRD_CARD where card_no = ? ) ";  
//		break;
//	}
	sqlCmd += " where CARD_ACCT_IDX = (select CARD_ACCT_IDX from CCA_CARD_BASE where card_no = ? ) ";
	setString(1, inputObj.tmpCardNo);

	int cnt = selectTable();
	if (cnt > 0) {
		obj = new RskAcnologObj();
		obj.cardNo = "";
		obj.acnoPSeqno = getValue("acno_p_seqno");
		obj.acctType = getValue("acct_type");
		obj.idPSeqno = getValue("id_p_seqno");
		obj.corpPSeqno = getValue("corp_p_seqno");
		obj.logType = "3";
		obj.logReason = "";
		obj.fitCond = "";
		obj.logRemark = getValue("spec_remark");
		obj.blockReason = getValue("block_reason1");
		obj.specStatus = getValue("spec_status");
		obj.specDelDate = getValue("spec_del_date");
		obj.smsFlag = getValue("block_sms_flag");
		obj.userDeptNo = "";
	}
	return obj;
}

/****************************************************************************/
private RskAcnologObj getCcaCardBase(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
	RskAcnologObj obj = null;
	
	sqlCmd =  "SELECT acno_p_seqno, acct_type , id_p_seqno , corp_p_seqno , spec_remark, spec_dept_no ";
	sqlCmd += "from CCA_CARD_BASE ";
	sqlCmd += "where card_no = ? ";
	setString(1, inputObj.tmpCardNo);

	int cnt = selectTable();
	if (cnt > 0) {
		obj = new RskAcnologObj();
		obj.cardNo = inputObj.tmpCardNo;
		obj.acnoPSeqno = getValue("acno_p_seqno");
		obj.acctType = getValue("acct_type");
		obj.idPSeqno = getValue("id_p_seqno");
		obj.corpPSeqno = getValue("corp_p_seqno");
		obj.logType = "6";
		obj.logReason = "A";
		obj.fitCond = "Y";
		obj.logRemark = getValue("spec_remark");
		obj.blockReason = "";
		obj.specStatus = ecsCardStatus.blockReasonOrSpecStatus;
		obj.specDelDate = inputObj.newEndDate;
		obj.smsFlag = "";
		obj.userDeptNo = getValue("spec_dept_no");
	}
	return obj;
}

/****************************************************************************/

private boolean insertCcaSpeHis(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
	setValue("log_date", sysDate);
	setValue("log_time", sysTime);
	setValue("card_no", inputObj.tmpCardNo);
	setValue("bin_type", inputObj.binType);
	setValue("from_type", "2");
	setValue("spec_status", ecsCardStatus.blockReasonOrSpecStatus);
	setValue("spec_del_date", inputObj.newEndDate);
	setValue("spec_outgo_reason", ecsCardStatus.specOutgoReason);
	setValue("spec_neg_reason", ecsCardStatus.specNegReason);
	setValue("aud_code", "A");
	setValue("pgm_id", prgmId);
	setValue("log_user", "batch");
	daoTable = "cca_spec_his";
	int insertCnt = insertTable();
	if (insertCnt <= 0) {
		comcr.errRtn("Error! insert cca_spec_his error!", "", "");
	}

	return true;

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
private void getSpecReason(TempICPSMQND inputObj, EcsCardStatus ecsCardStatus) throws Exception {
	sqlCmd =  "SELECT VISA_REASON, MAST_REASON , JCB_REASON , SEND_IBM , NEG_REASON ";
	sqlCmd += "from cca_spec_code ";
	sqlCmd += "where spec_code = ? ";
	setString(1, ecsCardStatus.blockReasonOrSpecStatus);

	int cnt = selectTable();

	if (cnt > 0) {
		switch (inputObj.binType) {
		case "V":
			ecsCardStatus.specOutgoReason = getValue("VISA_REASON");
			break;
		case "M":
			ecsCardStatus.specOutgoReason = getValue("MAST_REASON");
			break;
		case "J":
			ecsCardStatus.specOutgoReason = getValue("JCB_REASON");
			break;
		}
		
		if ("VD".equals(inputObj.tmpCardType)) {
			ecsCardStatus.specNegReason = getValue("SEND_IBM");
		} else {
			ecsCardStatus.specNegReason = getValue("NEG_REASON");
		}
	}

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
	
	// 2022/04/13
//	1. 異動信用卡ID
//	(1)SELECT CRD_IDNO(用檔案的ID)，若存在則不往下做(要記錄一筆到錯誤報表)，若不存在則接續做原本的UPDATE
//	(2)UPDATE CRD_IDNO_SEQNO 要增加WHERE條件 -> DEBIT_IDNO_FLAG = 'N'
//	(3)UPDATE CRD_IDNO、ACT_ACNO、CRD_IDNO_SEQNO 若重複則寫錯誤報表但不ROLLBACK
//	2.異動VD卡ID
//	(1)SELECT DBC_IDNO(用檔案的ID)，若存在則不往下做(要記錄一筆到錯誤報表)，若不存在則接續做原本的UPDATE
//	(2)UPDATE CRD_IDNO_SEQNO 要增加WHERE條件 -> DEBIT_IDNO_FLAG = 'Y'
//	(3)UPDATE DBC_IDNO、DBA_ACNO、CRD_IDNO_SEQNO 若重複則寫錯誤報表但不ROLLBACK
	
	// 2022/04/13 Justin 更新VD卡的ID
	boolean dbcSqlResult = updateDbcId(idNoFromFile, dbaData, inputObj);
	if (dbcSqlResult) {
		totalSuccessChgDbcIdCnt++;
		commitDataBase();
	}

	// 2022/01/11 Justin 同步信用卡的ID
	try {
		showLogMessage("D", "", String.format("開始確認並同步信用卡的ID[%s]->[%s]", dbaData.idNo, idNoFromFile));
		boolean crdSqlResult = updateCrdId(idNoFromFile, dbaData, inputObj);
		if (crdSqlResult) {
			totalSuccessChgCrdIdCnt++;
			commitDataBase();
		}
	}catch (Exception e) {
		showLogMessage("I", "", "更新信用卡的ID發生錯誤:" + e.getLocalizedMessage());
	}
	
	return dbcSqlResult;
	
}

boolean updateCrdId(String idNoFromFile, DbcDataD017 dbaData, TempICPSMQND inputObj) throws Exception {
	boolean sqlResult = false;
	
	String chiName = getChiNameFromCrdIdno(dbaData.idPSeqno, inputObj);
	if (chiName == null) return false;
	
	sqlResult = updateCrdIdnoSetIdNo(idNoFromFile, dbaData.idPSeqno, inputObj);
	if (sqlResult == false) return false;
	
	sqlResult = updateCrdAcnoSetIdNo(idNoFromFile, dbaData.idPSeqno, inputObj);
	if (sqlResult == false) return false;
	
	updateCrdIdnoSeqnoSetIdno(false, idNoFromFile, dbaData.idPSeqno, inputObj);
	
	insertCrdChgId(idNoFromFile, dbaData, chiName);
	showLogMessage("D", "", "成功同步信用卡的ID");
	
	return true;
}

boolean updateDbcId(String idNoFromFile, DbcDataD017 dbaData, TempICPSMQND inputObj) throws Exception {
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
	
	insertDbcChgId(idNoFromFile, dbaData);
	
	updateCrdIdnoSeqnoSetIdno(true, idNoFromFile, dbaData.idPSeqno, inputObj);
	
	updateCrdEmployeeSetIdno(idNoFromFile, dbaData.idNo);
	
	return true;
}

/***********************************************************************/

boolean insertCrdChgId(String newIdno, DbcDataD017 dbaData, String chiName) throws Exception {
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
		updateSQL += " old_id_p_seqno = ?,";
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
        setString(3, dbaData.idPSeqno);
        setString(4, chiName);
        setString(5, prgmId);
        setString(6, sysDate + sysTime);
        setString(7, prgmId);
        setString(8, prgmId);
        setString(9, dbaData.idNo);
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
/**
* @ClassName: CrdF074
* @Description: updateCrdAcnoSetIdNo 異動帳戶資料檔的帳戶查詢碼時增加綁定限一般卡(商務卡不需異動)
* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
* @Company: DXC Team.
* @author Wilson
* @version V1.00.23, Sep 13, 2022
*/
/**
* @ClassName: CrdF074
* @Description: updateCrdAcnoSetIdNo 找不到資料繼續往下執行
* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
* @Company: DXC Team.
* @author Wilson
* @version V1.00.24, Oct 05, 2022
*/
 boolean updateCrdAcnoSetIdNo(String newIdno, String idPSeqno, TempICPSMQND inputObj) throws Exception {
	daoTable = "act_acno";
	updateSQL =  " acct_key  = ?,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_pgm  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'),  ";
    updateSQL += " apr_user  = ? ,";
	updateSQL += " apr_date  = to_char(sysdate,'YYYYMMDD') ";
    whereStr   = " where id_p_seqno = ? and card_indicator = '1' ";      
    setString(1, newIdno + "0");
    setString(2, prgmId);
    setString(3, prgmId);
    setString(4, sysDate + sysTime);
    setString(5, prgmId);
    setString(6, idPSeqno);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update ACT_ACNO[id_no] not found! id_p_seqno = [" + idPSeqno + "]");
//		putErrReportList(ErrorReason.ERR16, inputObj);
//		return false;
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
 boolean updateCrdIdnoSetIdNo(String newIdno, String idPSeqno, TempICPSMQND inputObj) throws Exception {
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
			putErrReportList(ErrorReason.ERR15, inputObj);
		}
		return false;
	}

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update CRD_IDNO[id_no]: Not found! id_p_seqno = [" + idPSeqno + "]");
		putErrReportList(ErrorReason.ERR14, inputObj);
		return false;
	}

	return true;
	
}

/**********************************************************************/

String getChiNameFromCrdIdno(String idPSeqno, TempICPSMQND inputObj) throws Exception {

	sqlCmd =  "select chi_name ";
	sqlCmd += "from crd_idno ";
	sqlCmd += "where id_p_seqno = ? ";
	setString(1, idPSeqno); // 客戶代號前10碼

	int cnt = selectTable();

	if (cnt <= 0) {
		showLogMessage("I", "", "getChiNameFromCrdIdno CRD_IDNO[id_no]: Not found! id_p_seqno = [" + idPSeqno + "]");
		putErrReportList(ErrorReason.ERR14, inputObj);
		return null;
	}

	return getValue("chi_name");
}

/***********************************************************************/

boolean updateCrdIdnoSeqnoSetIdno(boolean isDebit, String idNoFromFile, String idPSeqno, TempICPSMQND inputObj) throws Exception {
	daoTable = "crd_idno_seqno";
	updateSQL =   " id_no  = ? ";
    whereStr   = " where id_p_seqno = ? AND DEBIT_IDNO_FLAG = ?  ";      
    setString(1, idNoFromFile);
    setString(2, idPSeqno);
    setString(3, (isDebit) ? "Y" : "N");
//	updateTable();
    
    // 2022/04/12 Justin prevent from updating duplicate crd_idno_seqno 
	try {
		updateTable();
	} catch (Exception e) {
		if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
 		    // duplicate
			ErrorReason errorReason = (isDebit) ? ErrorReason.ERR13 : ErrorReason.ERR17 ;
			showLogMessage("I", "", "id_p_seqno = [" + idPSeqno + "]，Error : " + errorReason.getErrorCode());
			putErrReportList(errorReason, inputObj);
			return true;
		}
		comcr.errRtn("id_p_seqno = [" + idPSeqno + "]，Error : " + e.getMessage(), "", "");
	}

	if (notFound.equals("Y")) {
		ErrorReason errorReason = (isDebit) ? ErrorReason.ERR18 : ErrorReason.ERR19 ;
		showLogMessage("I", "", "update CRD_IDNO_SEQNO not found! id_p_seqno= [" + idPSeqno + "], Error: " + errorReason.getErrorCode());
//		putErrReportList(errorReason, inputObj);
//		return false; //沒更新也沒關係
	}

	return true;
	
}

/***********************************************************************/

boolean updateCrdEmployeeSetIdno(String idNoFromFile, String idNoFromDB) throws Exception {
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

boolean insertDbcChgId(String idNoFromFile, DbcDataD017 dbaData) throws Exception {
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
/**
* @ClassName: CrdF074
* @Description: updateDbaAcnoSetIdNo 找不到資料繼續往下執行
* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
* @Company: DXC Team.
* @author Wilson
* @version V1.00.24, Oct 05, 2022
*/
boolean updateDbaAcnoSetIdNo(String idNoFromFile, String idPSeqno, TempICPSMQND inputObj) throws Exception {
	daoTable = "dba_acno";
	updateSQL =   " acct_key  = ?,";
	updateSQL += " mod_user  = ? ,";
    updateSQL += " mod_pgm  = ? ,";
    updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') , ";
    updateSQL += " acct_holder_id  = ? ";
    whereStr   = " where id_p_seqno = ? ";      
    setString(1, idNoFromFile + "0");
    setString(2, prgmId);
    setString(3, prgmId);
    setString(4, sysDate + sysTime);
    setString(5, idNoFromFile);
    setString(6, idPSeqno);

	updateTable();

	if (notFound.equals("Y")) {
		showLogMessage("I", "", "update DBA_ACNO[id_no] not found! id_p_seqno = " + idPSeqno);
//		putErrReportList(ErrorReason.ERR8, inputObj);
//		return false;
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
boolean updateDbcIdnoSetIdNo(String idNoFromFile, String idPSeqno, TempICPSMQND inputObj) throws Exception {
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

/**
 * 
 * @param idNoFromFile
 * @param idNoFromDB
 * @return
 */
private boolean isIdChg(String idNoFromDB, String idNoFromFile) {
	return ! idNoFromFile.trim().equals(idNoFromDB);
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
	
	/** 2022/05/20 若該信用卡卡號為附卡(VD卡沒有附卡，故無需判斷)，就要比對檔案與新系統的手機號碼 **/
	if ("CC".equals(inputObj.tmpCardType) && "1".equals(inputObj.supFlag)) {
		String cellarPhoneFromDB = getCellarPhoneFromCrdIdno((String) map.get("id_no"));
		if (cellarPhoneFromDB.equals(((String) map.get("cellar_phone")).trim()) == false) map.put("cellar_phone", cellarPhoneFromDB);
	}
	
	// 若為信用卡，取開卡日期
	if ("CC".equals(inputObj.tmpCardType))
		inputObj.tmpActivateDate = getActivateDate(inputObj.tmpCardNo); // 開卡日期

	if (inputObj.tmpActivateDate == null || inputObj.tmpActivateDate.trim().isEmpty())
		inputObj.tmpActivateDate = (String) map.get("activate_date");
	showLogMessage("I", "", "開卡日期 = [" + inputObj.tmpActivateDate + "]");
	
	ICPSMQND inputData = new ICPSMQND();
    inputData.cardNo = (String) map.get("card_no");
//    inputData.activateDate = tmpStr;
//    inputData.activateDate = ""; // 2022/04/01 改取17檔案內的activate_date
//    inputData.activateDate =  (String) map.get("activate_date");
    inputData.activateDate = inputObj.tmpActivateDate;
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

String getIdPSeqnoFromCrdCard(TempICPSMQND inputObj) throws Exception {
	String idPSeqno = "";

	sqlCmd = "select id_p_seqno, bin_type, new_end_date, SUP_FLAG  ";
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
	
	inputObj.binType = getValue("bin_type");
	inputObj.newEndDate = getValue("new_end_date");
	inputObj.supFlag = getValue("SUP_FLAG");
	
	return idPSeqno;
}

/**********************************************************************/
String getIdPSeqnoFromDbcCard(TempICPSMQND inputObj) throws Exception {
	String idPSeqno = "";

	sqlCmd = "select id_p_seqno, bin_type, new_end_date ";
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
	
	inputObj.binType = getValue("bin_type");
	inputObj.newEndDate = getValue("new_end_date");

	return idPSeqno;
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

private void insertOutputFileCtl() throws Exception {
	
	for (int i = 0; i < outputDataFileCtlList.size(); i++) {
		insertFileCtl(outputDataFileCtlList.get(i));
		insertFileCtl(outputAttrFileCtlList.get(i));
	}
	
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

private void renameOutputFile() throws Exception {
	for (int i = 0; i < outputDataFileCtlList.size(); i++) {
		String outputDataFileName =  outputDataFileCtlList.get(i).fileName;
		renameFile1(outputDataFileName);
		String outputAttrFileName =  outputAttrFileCtlList.get(i).fileName;
		renameFile1(outputAttrFileName);
	}
	
}

private void selectCardsModOrOpenYesterday() throws Exception {
	 
//	SELECT CARD_NO 
//	FROM CCA_CARD_OPEN cco  
//	WHERE OPEN_DATE =  to_char(sysdate - 1 DAYS, 'YYYYMMDD')
	
	StringBuilder sb = new StringBuilder();
//    sb.append(" SELECT CARD_NO ")
//      .append(" FROM CCA_CARD_OPEN cco ");
//    
//    if (modAndOpenCardDate.isEmpty()) {
//    	if (beginModAndOpenCardDate.isEmpty() == false && endModAndOpenCardDate.isEmpty() == false) {
//    		sb.append("WHERE   ? <=  OPEN_DATE AND  OPEN_DATE <= ? ");
//    		setString(1, beginModAndOpenCardDate);
//    		setString(2, endModAndOpenCardDate);
//    	}else {
//    		sb.append("WHERE     OPEN_DATE =  to_char(sysdate - 1 DAYS, 'YYYYMMDD')  ");
//    	}
//	} else {
//		sb.append("WHERE     OPEN_DATE =  ? ");
//		setString(1, modAndOpenCardDate);
//	}
	
	sb.append("SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'A' AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM CRD_CARD A,CRD_IDNO B WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.CRT_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'A' AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM DBC_CARD A,DBC_IDNO B WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.CRT_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'C' AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM CRD_CARD A,CRD_IDNO B,CRD_CHG_ID C WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND B.ID_NO = C.ID_NO AND C.CHG_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'C' AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM DBC_CARD A,DBC_IDNO B,DBC_CHG_ID C WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND B.ID_NO = C.AFT_ID AND C.CRT_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'C' AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM CRD_CARD A,CRD_IDNO B, CMS_CHGCOLUMN_LOG C WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.ID_P_SEQNO = C.ID_P_SEQNO AND C.CHG_COLUMN ='birthday' AND C.DEBIT_FLAG ='N' AND C.CHG_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'C'  AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM DBC_CARD A,DBC_IDNO B, CMS_CHGCOLUMN_LOG C WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.ID_P_SEQNO = C.ID_P_SEQNO AND C.CHG_COLUMN ='birthday' AND C.DEBIT_FLAG ='Y' AND C.CHG_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'C'  AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM CRD_CARD A,CRD_IDNO B, CMS_CHGCOLUMN_LOG C WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.CARD_NO = C.CARD_NO AND C.CHG_COLUMN IN ('current_code','new_end_date') AND C.DEBIT_FLAG ='N' AND C.CHG_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'C'  AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM DBC_CARD A,DBC_IDNO B, CMS_CHGCOLUMN_LOG C WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.CARD_NO = C.CARD_NO AND C.CHG_COLUMN IN ('current_code','new_end_date') AND C.DEBIT_FLAG ='Y' AND C.CHG_DATE = ? ")
	.append(" UNION ")
	.append(" SELECT A.CARD_NO,A.ACTIVATE_DATE,B.ID_NO,B.BIRTHDAY,B.CELLAR_PHONE,A.CURRENT_CODE,'C'  AS MOD_NO,A.NEW_END_DATE ")
	.append(" FROM CRD_CARD A,CRD_IDNO B,CCA_CARD_OPEN C WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.CARD_NO = C.CARD_NO AND C.OPEN_DATE = ? ")
	;
	int i = 1;
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	setString(i++,hSystemDate);
	
    sqlCmd = sb.toString();
//    showLogMessage("D", "", sqlCmd);
    
    int modAndOpenCardCnt = 0;
    int modAndOpenCardDupCnt = 0;
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
    	
    	String cardNo = getValue("CARD_NO");
    	String activateDate = getValue("ACTIVATE_DATE");
    	String idNo = getValue("ID_NO");
    	String birthday = getValue("BIRTHDAY");
    	String cellarPhone = getValue("CELLAR_PHONE");
    	String currentCode = getValue("CURRENT_CODE");
    	String modNo = getValue("MOD_NO");
    	String newEndDate = getValue("NEW_END_DATE");
//    	CardObj cardObj = getCardObj(cardNo);
//    	if (cardObj == null) {
//    		showLogMessage("I", "selectCardsModOrOpenYesterday", String.format("卡號[%s]找不到相對應卡片資料", cardNo));
//		    continue;
//    	}
    	
    	modAndOpenCardCnt++;
    	showLogMessage("I", "", "==========");
    	// 2022/03/31: merge CrdF071 into CrdF074
    	// 若該卡號已存在檔案中就直接補上開卡日期
    	// 若不存在則直接寫新的一筆資料進檔案
    	ICPSMQND psmqdObj = psmqdMap.get(cardNo.trim());
    	if (psmqdObj == null) {
//    		IdNoObj idNoObj = getIdnoObj(cardObj.idPSeqno);
    		psmqdObj = new ICPSMQND();
    		psmqdObj.cardNo = cardNo;
    		psmqdObj.activateDate = activateDate;
    		psmqdObj.idNo = idNo;
    		psmqdObj.birthday = birthday;
    		psmqdObj.cellarPhone = cellarPhone;
    		psmqdObj.stopCode = convertCurrentCodeToStopCode(currentCode);
    		psmqdObj.modNo = modNo;
    		psmqdObj.msgSendStatus = "Y";
    		psmqdObj.availableDate = newEndDate;
    		psmqdObj.issueUnit = ISSUE_UNIT;
    	}else {
    		modAndOpenCardDupCnt++;
    		psmqdObj.activateDate = activateDate;
    		psmqdObj.cannotBeSent = false;
    		showLogMessage("I", "", String.format("卡號[%s]已存在檔案中直接補上開卡日期[%s]", cardNo, psmqdObj.activateDate));
    	}

    	showLogMessage("I", "", String.format("card_no = [%s]", cardNo));
    	showLogMessage("D", "", String.format("開卡日期 = [%s]", psmqdObj.activateDate));
    	showLogMessage("D", "", String.format("id_no = [%s]", psmqdObj.idNo));
//    	showLogMessage("D", "", String.format("birthday = [%s]", psmqdObj.birthday));
    	showLogMessage("D", "", String.format("cellar_phone = [%s]", psmqdObj.cellarPhone));
//    	showLogMessage("D", "", String.format("停用碼 = [%s]", psmqdObj.stopCode));
//    	showLogMessage("D", "", String.format("異動碼 = [%s]", psmqdObj.modNo));
//    	showLogMessage("D", "", String.format("簡訊發送狀態 = [%s]", psmqdObj.msgSendStatus));
//    	showLogMessage("D", "", String.format("卡片有效日 = [%s]", psmqdObj.availableDate));
//    	showLogMessage("D", "", String.format("發卡單位代號 = [%s]", psmqdObj.issueUnit));
    	
    	psmqdMap.put(psmqdObj.cardNo.trim(), psmqdObj); 
    }
    showLogMessage("I", "", String.format("總共讀取[%d]筆「前一天有異動卡況或開卡的卡號」，", modAndOpenCardCnt));
    showLogMessage("I", "", String.format("其中[%d]筆已存在ICUD17檔案中，[%d]筆為新資料", modAndOpenCardDupCnt, modAndOpenCardCnt-modAndOpenCardDupCnt));
    closeCursor(cursorIndex);

}

private void selectCardsModOrOpenYesterdayP2() throws Exception {
	 
//	SELECT CARD_NO 
//	FROM CCA_CARD_OPEN cco  
//	WHERE OPEN_DATE =  to_char(sysdate - 1 DAYS, 'YYYYMMDD')
	
	StringBuilder sb = new StringBuilder();
    sb.append(" SELECT CARD_NO ")
      .append(" FROM CCA_CARD_OPEN cco ");
    
    if (modAndOpenCardDate.isEmpty()) {
    	if (beginModAndOpenCardDate.isEmpty() == false && endModAndOpenCardDate.isEmpty() == false) {
    		sb.append("WHERE   ? <=  OPEN_DATE AND  OPEN_DATE <= ? ");
    		setString(1, beginModAndOpenCardDate);
    		setString(2, endModAndOpenCardDate);
    	}else {
    		sb.append("WHERE     OPEN_DATE =  to_char(sysdate - 1 DAYS, 'YYYYMMDD')  ");
    	}
	} else {
		sb.append("WHERE     OPEN_DATE =  ? ");
		setString(1, modAndOpenCardDate);
	}

    sqlCmd = sb.toString();
    showLogMessage("D", "", sqlCmd);
    
    int modAndOpenCardCnt = 0;
    int modAndOpenCardDupCnt = 0;
    int cursorIndex = openCursor();
    while (fetchTable(cursorIndex)) {
    	
    	String cardNo = getValue("CARD_NO");
			  
    	CardObj cardObj = getCardObjP2(cardNo);
    	if (cardObj == null) {
    		showLogMessage("I", "selectCardsModOrOpenYesterdayP2", String.format("卡號[%s]找不到相對應卡片資料", cardNo));
		    continue;
    	}
    	
    	modAndOpenCardCnt++;
    	showLogMessage("I", "", "==========");
    	// 2022/03/31: merge CrdF071 into CrdF074
    	// 若該卡號已存在檔案中就直接補上開卡日期
    	// 若不存在則直接寫新的一筆資料進檔案
    	ICPSMQND psmqdObj = psmqdMap.get(cardNo.trim());
    	if (psmqdObj == null) {
    		IdNoObj idNoObj = getIdnoObjP2(cardObj.idPSeqno);
    		psmqdObj = new ICPSMQND();
    		psmqdObj.cardNo = cardNo;
    		psmqdObj.activateDate = cardObj.activateDate;
    		psmqdObj.idNo = idNoObj.idNo;
    		psmqdObj.birthday = idNoObj.birthday;
    		psmqdObj.cellarPhone = idNoObj.cellarPhone;
//    		psmqdObj.stopCode = convertCurrentCodeToStopCode(cardObj.currentCode);
    		psmqdObj.stopCode = "";
    		psmqdObj.modNo = "C";
    		psmqdObj.msgSendStatus = "Y";
    		psmqdObj.availableDate = cardObj.newEndDate;
    		psmqdObj.issueUnit = ISSUE_UNIT;
    	}else {
    		modAndOpenCardDupCnt++;
    		psmqdObj.activateDate = cardObj.activateDate;
    		psmqdObj.cannotBeSent = false;
    		showLogMessage("I", "", String.format("卡號[%s]已存在檔案中直接補上開卡日期[%s]", cardNo, psmqdObj.activateDate));
    	}

    	showLogMessage("I", "", String.format("card_no = [%s]", cardNo));
    	showLogMessage("D", "", String.format("開卡日期 = [%s]", psmqdObj.activateDate));
    	showLogMessage("D", "", String.format("id_no = [%s]", psmqdObj.idNo));
//    	showLogMessage("D", "", String.format("birthday = [%s]", psmqdObj.birthday));
    	showLogMessage("D", "", String.format("cellar_phone = [%s]", psmqdObj.cellarPhone));
//    	showLogMessage("D", "", String.format("停用碼 = [%s]", psmqdObj.stopCode));
//    	showLogMessage("D", "", String.format("異動碼 = [%s]", psmqdObj.modNo));
//    	showLogMessage("D", "", String.format("簡訊發送狀態 = [%s]", psmqdObj.msgSendStatus));
//    	showLogMessage("D", "", String.format("卡片有效日 = [%s]", psmqdObj.availableDate));
//    	showLogMessage("D", "", String.format("發卡單位代號 = [%s]", psmqdObj.issueUnit));
    	
    	psmqdMap.put(psmqdObj.cardNo.trim(), psmqdObj); 
    }
    showLogMessage("I", "", String.format("總共讀取[%d]筆「前一天有異動卡況或開卡的卡號」，", modAndOpenCardCnt));
    showLogMessage("I", "", String.format("其中[%d]筆已存在ICUD17檔案中，[%d]筆為新資料", modAndOpenCardDupCnt, modAndOpenCardCnt-modAndOpenCardDupCnt));
    closeCursor(cursorIndex);
	
}

private String convertCurrentCodeToStopCode(String currentCode) {
	switch (currentCode) {
	case "0":
		return "";
	case "1":
		return "Q";
	case "2":
		return "L";
	case "3":
		return "C";
	case "4":
		return "U";
	case "5":
		return "F";
	default:
		return null;
	}
}

private IdNoObj getIdnoObjP2(String idPSeqno) throws Exception {
   return getCrdIdnoObjP2(idPSeqno);
}

private IdNoObj getCrdIdnoObjP2(String idPSeqno) throws Exception {
	IdNoObj idNoObj = new IdNoObj();
	sqlCmd =  " SELECT id_no, birthday, cellar_phone ";
	sqlCmd += " FROM crd_idno";
	sqlCmd += " WHERE id_p_seqno = ? ";
	setString(1, idPSeqno);
	selectTable();
	if (notFound.equals("Y")) {
		return null;
	}
	idNoObj.idPSeqno = getValue("id_p_seqno");
	idNoObj.idNo = getValue("id_no");
	idNoObj.birthday = getValue("birthday");
	idNoObj.cellarPhone = getValue("cellar_phone");
	return idNoObj;
}

private String getCellarPhoneFromCrdIdno(String idno) throws Exception {
	sqlCmd =  " SELECT cellar_phone ";
	sqlCmd += " FROM crd_idno";
	sqlCmd += " WHERE id_no = ? ";
	setString(1, idno);
	selectTable();
	if (notFound.equals("Y")) {
		return "";
	}
	return getValue("cellar_phone");
}

private CardObj getCardObjP2(String cardNo) throws Exception {
	return getCrdCardObjP2(cardNo);

}

private CardObj getCrdCardObjP2(String cardNo) throws Exception {
	CardObj cardObj = new CardObj();
	sqlCmd =  " SELECT id_p_seqno, current_code, new_end_date, activate_date";
	sqlCmd += " FROM crd_card";
	sqlCmd += " WHERE card_no = ? ";
	setString(1, cardNo);
	selectTable();
	if (notFound.equals("Y")) {
		showLogMessage("I", "getCrdCardObj", String.format("卡號[%s]不存在crd_card中", cardNo));
		return null;
	}
	cardObj.cardNo = cardNo;
	cardObj.idPSeqno = getValue("id_p_seqno");
	cardObj.currentCode = getValue("current_code");
	cardObj.newEndDate = convertToYYMM(getValue("new_end_date"), cardNo);
	cardObj.activateDate = getValue("activate_date");
	return cardObj;
}

private String convertToYYMM(String date, String cardNo) {
	if (date != null && date.length() == 8) {
		return date.substring(2, 6);
	}
	showLogMessage("E", "", String.format("card_no[%s] new_end_date[%s]格式有誤", cardNo, date));
	return "";
}


/**
 * 參數1：必填，是否要產檔，(1)"Y"'(要產檔)) (2)"N"(不產檔) (3)"X"無ICUD17檔->不執行，有ICUD17檔->只處理ICUD17並產檔(不讀取開卡資料)<br>
 * 參數2：非必填，指定檔案日期，預設為不指定日期，也可輸入西元年(如：20200715)<br>
 * 參數3：非必填, (1)參數3有值、參數4無值: 指定讀取開卡資料日期    (2)參數3有值、參數4有值:  指定讀取開卡日資料起日<br>
 * 參數4：非必填, 指定讀取開卡日資料迄日
 * @param args
 * @throws Exception
 */
private void loadArgs(String[] args) throws Exception {

	if (args.length == 1) {
		
		// 是否要產檔
		// 第一個放是否要產檔
		getFirstArg(args[0]);
		
		queryDate = "";
		
		modAndOpenCardDate = "";
		beginModAndOpenCardDate = "";
		endModAndOpenCardDate = "";
		
	} else if (args.length == 2) {
		
		// 是否要產檔，預設為Y(要產檔)
		// 第一個放是否要產檔
		getFirstArg(args[0]);
		
		// 第二個放指定檔案日期yyyyMMdd
		getSecondArg(args[1]);
		
		modAndOpenCardDate = "";
		beginModAndOpenCardDate = "";
		endModAndOpenCardDate = "";
		
	} else if (args.length == 3) {
		
		// 是否要產檔，預設為Y(要產檔)
		// 第一個放是否要產檔
		getFirstArg(args[0]);

		// 第二個放指定檔案日期yyyyMMdd
		getSecondArg(args[1]);
		
		// 參數3有值、參數4無值: 
		// 參數3 = 指定讀取開卡資料日期
		getThirdArg(args[2], args.length);		
		
		beginModAndOpenCardDate = "";
		endModAndOpenCardDate = "";
		
	} else if (args.length == 4) {
		
		// 是否要產檔，預設為Y(要產檔)
		// 第一個放是否要產檔
		getFirstArg(args[0]);

		// 第二個放指定檔案日期yyyyMMdd
		getSecondArg(args[1]);

		//  參數3有值、參數4有值:  
		//  參數3 = 指定讀取開卡日資料起日
		getThirdArg(args[2], args.length);
		
		// 參數4 = 指定讀取開卡日資料迄日
		getFourthArg(args[3]);
		
	} else {
//		StringBuilder sb = new StringBuilder();
//		sb.append("參數1：必填，是否要產檔，(1)Y(要產檔) (2)N(不產檔) (3)X(若無ICUD17檔->不執行；若有ICUD17檔->只處理ICUD17並產檔，但不讀取開卡資料")
//		  .append("參數2：非必填，指定檔案日期，預設為不指定日期，也可輸入西元年(如：20200715)")
//		  .append("參數3：非必填, (1)參數3有值、參數4無值: 指定讀取開卡資料日期    (2)參數3有值、參數4有值:  指定讀取開卡日資料起日")
//		  .append("參數4：非必填, 指定讀取開卡日資料迄日");
//		comc.errExit(sb.toString(), "");
		
		shouldProdFile = true;
		shouldDoOpenCard = true;
		
		queryDate = "";

		modAndOpenCardDate = "";
		beginModAndOpenCardDate = "";
		endModAndOpenCardDate = "";
	}
	
}

private void getFourthArg(String string) throws Exception {
	if (!new CommFunction().checkDateFormat(string, "yyyyMMdd")) {
		comc.errExit(String.format("參數4日期格式[%s]錯誤", string), "");
	}
	endModAndOpenCardDate = string;
	showLogMessage("I", "", String.format("「異動卡況或開卡的卡號結束日」讀取%s", endModAndOpenCardDate));
}


private void getThirdArg(String string, int length) throws Exception {
	if (length == 3) {
		// 參數3有值、參數4無值:
		// 參數3 = 指定讀取開卡資料日期
		if (string.trim().length() == 0) {
			modAndOpenCardDate = "";
			showLogMessage("I", "", "「異動卡況或開卡的卡號」讀取系統日前一天");
		} else {
			if (!new CommFunction().checkDateFormat(string, "yyyyMMdd")) {
				comc.errExit(String.format("參數3日期格式[%s]錯誤", string), "");
			}
			modAndOpenCardDate = string;
			showLogMessage("I", "", String.format("「異動卡況或開卡的卡號」讀取%s", modAndOpenCardDate));
		}
		
	}else if (length == 4) {
		// 參數3有值、參數4有值:
		// 參數3 = 指定讀取開卡日資料起日
		modAndOpenCardDate = "";
		
		if (!new CommFunction().checkDateFormat(string, "yyyyMMdd")) {
			comc.errExit(String.format("參數3日期格式[%s]錯誤", string), "");
		}
		beginModAndOpenCardDate = string;
		showLogMessage("I", "", String.format("「異動卡況或開卡的卡號起始日」讀取%s", beginModAndOpenCardDate));
	}
	
}


private void getSecondArg(String string) throws Exception {
	// 第二個放指定檔案日期yyyyMMdd
	if (string.trim().length() == 0) {
		queryDate = "";
		showLogMessage("I", "", "不指定日期，讀取所有符合的檔案");
	} else {
		if (!new CommFunction().checkDateFormat(string, "yyyyMMdd")) {
			comc.errExit(String.format("參數2日期格式[%s]錯誤",string), "");
		}
		queryDate = string;
	}
	
}


private void getFirstArg(String string) throws Exception {
	// 是否要產檔
	// 第一個放是否要產檔
	switch (string) {
	case "Y":
		shouldProdFile = true;
		shouldDoOpenCard = true;
		isRunIcuD017 = true;
		break;
	case "N":
		shouldProdFile = false;
		shouldDoOpenCard = false;
		isRunIcuD017 = true;
		break;
	case "X":
		shouldProdFile = true;
		shouldDoOpenCard = false;
		isRunIcuD017 = true;
		break;
	case "Z":
		shouldProdFile = true;
		shouldDoOpenCard = true;
		isRunIcuD017 = false;
		break;
	default:
		comc.errExit("參數1是否要產檔請填入Y, N, 或X", "");
		break;
	}
	prodFlag = string;
	showLogMessage("I", "", String.format("是否要產檔參數為[%s]", prodFlag));

}


	/***********************************************************************/
	private void commonRtn() throws Exception {
		sqlCmd = "select to_char(sysdate -1 ,'yyyymmdd') h_system_date from ptr_businday ";
		int tmpInt = selectTable();
		if (tmpInt > 0) {
			hSystemDate = getValue("h_system_date");
		}
	}
	
	private void commonRtnP2() throws Exception {
		sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date from ptr_businday ";
		int tmpInt = selectTable();
		if (tmpInt > 0) {
			hSystemDate = getValue("h_system_date");
		}
	}
    
    /***************************************************************************/
    
private void setOutputDataFile(int nnNo) throws Exception {
    	// OUTPUT_DATA_FILE
    	outputDataFileName = String.format("%s.%s.%s%s%02d", 
    			ISSUE_UNIT_CODE, 
    			OUTPUT_DATA_FILE, 
    			new CommDate().getLastTwoTWDate(hSystemDate), 
    			hSystemDate.substring(4, 8), 
    			nnNo
    			);
    	outputDataFilePath = String.format("%s/media/icu/out/%s", comc.getECSHOME(),outputDataFileName);
        showLogMessage("I", "", "Open file=[" + outputDataFilePath + "]"); 
        outputDataFilePath = SecurityUtil.verifyPath(outputDataFilePath);
        try {
            comc.mkdirsFromFilenameWithPath(outputDataFilePath);
            outputDataBufWri = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDataFilePath), "MS950"));
        } catch (Exception ex) {
            comcr.errRtn(String.format("開啟檔案[%s]失敗[%s]", outputDataFilePath, ex.getMessage()), "", "");
        }
    }
    
	/***************************************************************************/

private void setOutputAttrFile(int nnNo) throws Exception {
		// OUTPUT_ATTR_FILE
        outputAttrFileName = String.format("%s.%s.%s%s%02d", 
        		ISSUE_UNIT_CODE, 
        		OUTPUT_ATTR_FILE, 
        		new CommDate().getLastTwoTWDate(hSystemDate), 
    			hSystemDate.substring(4, 8), 
        		nnNo);
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
private void checkOpen() throws Exception {    	
    	// get next number of the file
    	int hNn = checkFileCtl();
    	
    	setOutputDataFile(hNn);
    	
    	setOutputAttrFile(hNn);

    }
    
    /***********************************************************************/
    private void createICPSMQND() throws Exception {
    	if (psmqdMap.isEmpty() == false) {
    		showLogMessage("I", "", "開始產生資料檔......");
    		int recordCntPerFile = 0;
    		List<ICPSMQND> icpsmqnDs = new ArrayList<>(psmqdMap.values());
    		for (int i = 0; i < icpsmqnDs.size(); i++) {
//    			showData(icpsmqnDs.get(i));
    			// 2022/03/31 Justin bypass the data with block code equal to E
    			// 2022/04/12 Justin bypass the data with block code E,X,A,B,F,L,S,O
    			if (icpsmqnDs.get(i).cannotBeSent) {
    				showLogMessage("I", "", "以上資料current_code不為0且不存在於開卡指定日期中，因此不產此資料");
    				continue;
    			}
    			totalRecordCnt++;
    			recordCntPerFile++;
    			outputDataBufWri.write(icpsmqnDs.get(i).convertToRowOfData() + "\r\n");
    			
    			//切檔
    			if (recordCntPerFile % RECORD_CNT_PER_FILE == 0) {
    				createICPSMQNA(recordCntPerFile);
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
    			createICPSMQNA(recordCntPerFile);
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
    void createICPSMQNA(int dataCount) throws Exception {
    	PSMQAObj psmqAObj = new PSMQAObj();
    	psmqAObj.dataCount = String.format("%07d",dataCount);
    	psmqAObj.errCode = "";
//		icuData.filler = "";
    	String tmp = psmqAObj.allText();
        outputAttrBufWri.write(tmp + "\r\n");
		
		return;
	}
    
	/**********************************************************************/
    private int checkFileCtl() throws Exception {
    	int hNn = 0;
    	String likeFilename = "";
    	String hFilename = "";
    	likeFilename = String.format("%s.%s.%s%s", 
    			ISSUE_UNIT_CODE, 
    			OUTPUT_DATA_FILE, 
    			new CommDate().getLastTwoTWDate(hSystemDate), 
    			hSystemDate.substring(4, 8)
    			) +"%";
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
        	hNn = Integer.valueOf(hFilename.substring(25, 27))+1;
        }
        
        return hNn;
    }

    private void insertFileCtl(CrdFileCtlObj crdFileCtlObj) throws Exception {
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
    	
    	boolean cannotBeSent = false;
    	
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
			rtn += fixLeft("", 4);
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
    
    class RskAcnologObj{
    	String kindFlag = "";
    	String cardNo = "";
    	String acnoPSeqno = "";
    	String acctType = "";
    	String idPSeqno = "";
    	String corpPSeqno = "";
    	String logDate = "";
    	String logMode = "";
    	String logType = "";
    	String logReason = "";
    	String fitCond = "";
    	String logRemark = "";
    	String blockReason = "";
    	String specStatus = "";
    	String specDelDate = "";
    	String smsFlag = "";
    	String userDeptNo = "";
    	String sendIbmFlag = "";
    	String sendIbmDate = "";
    	String aprFlag = "";
    	String aprDate = "";
    	String modUser = "";
    	String modTime = "";
    	String modPgm = "";
    }

	/****************************************************************************/
    public static void main(String[] args) throws Exception {
    	CrdF074 proc = new CrdF074();
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
			String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + outputDataFileName;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
		
		/**********************************************************************/
		/***
		 * WF_VALUE = Y  走P2的邏輯
		 * WF_VALUE = N  走P3的邏輯
		 * @throws Exception
		 */
		void selectPtrSysParmP2() throws Exception {
			sqlCmd = "SELECT WF_VALUE FROM PTR_SYS_PARM WHERE WF_PARM = 'SYSPARM' AND WF_KEY = 'ROLLBACK_P2'";
			int parmCnt = selectTable();
			if (parmCnt > 0) {
				wfValueP2 = getValue("WF_VALUE");
			}
			showLogMessage("I","","wfValueP2 = [ " + wfValueP2 + " ]");
		}
}

enum ErrorReason{
	ERR1(String.format("%-200s", "客戶代號第11碼延伸碼不為空白或“R”")), 
	ERR2(String.format("%-200s", "卡片號碼不存在卡片主檔")), 
	ERR3(String.format("%-200s", "客戶代號不存在卡人主檔")), 
	ERR4(String.format("%-200s", "主卡人ID不存在信用卡卡人主檔")), 
	ERR5(String.format("%-200s", "主卡人ID不存在VD卡卡人主檔")), 
	ERR6(String.format("%-200s", "異動卡人檔查無資料(DBC)")), 
	ERR7(String.format("%-200s", "此ID已存在卡人檔(DBC)")), 
	ERR8(String.format("%-200s", "此ID_P_SEQNO不存在帳戶檔(DBC)")), 
	ERR9(String.format("%-200s", "客戶代號與卡人檔不一致")),
	ERR10(String.format("%-200s", "客戶代號與VD卡人檔不一致")),
	ERR11(String.format("%-200s", "使用檔案中的卡號從crd_card取得的id_p_seqno不存在卡人主檔")),
	ERR12(String.format("%-200s", "使用檔案中的卡號從dbc_card取得的id_p_seqno不存在VD卡人主檔")),
	ERR13(String.format("%-200s", "卡人流水號檔已存在相同的ID(DBC)")),
	ERR14(String.format("%-200s", "異動卡人檔查無資料(CRD)")),
	ERR15(String.format("%-200s", "此ID已存在卡人檔(CRD)")),
	ERR16(String.format("%-200s", "此ID_P_SEQNO不存在帳戶檔(CRD)")),
	ERR17(String.format("%-200s", "卡人流水號檔已存在相同的ID(CRD)")),
	ERR18(String.format("%-200s", "異動身分證序號代碼檔查無資料(DBC)")),
	ERR19(String.format("%-200s", "異動身分證序號代碼檔查無資料(CRD)"))
	;
			
    private String info = "";
	ErrorReason(String info){
		this.info = info;
	}
	public String getErrorCode() {
		return info;
	}
	
}

