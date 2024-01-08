/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/02  V1.00.00   Ryan     program initial                          *
*  112/04/20  V1.00.01   Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。                                                     *	
*  112/08/22  V1.00.02   Ryan      抓取執行日-1及執行日的異動資料 。                                                     *	
*****************************************************************************/
package Inf;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommFunction;

import Dxc.Util.SecurityUtil;


public class InfC018 extends AccessDAO {
  private final String progname = "產生送CRDB 18 更新CRRSEG債協註記CRRDUE 112/08/22 V1.00.02";
  CommCrdRoutine comcr = null;
  CommCrd comc = new CommCrd();
  CommFunction comm = new CommFunction();
  CommFTP commFTP = null;
  CommRoutine comr = null;
  CommDate commDate = new CommDate();
  String tempUser = "";
  String filePath1 = "";
  

  String hBusiBusinessDate = "";

  String searchDate = "";
  
  
  private final String filePathFromDb = "media/crdb";
  private final String fileName = "CRU23B1_TYPE_18_";

  public int mainProcess(String[] args) {
    String callBatchSeqno = "";

    try {
      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);
      // =====================================    
      
      
      selectPtrBusinday();                
      
		// 檢核參數的日期格式
		if (args.length >= 1) {
			showLogMessage("I", "", "PARM 1 : [無參數] 表示抓取businday ");
			showLogMessage("I", "", "PARM 1 : [SYSDAY] 表示抓取系統日");
			showLogMessage("I", "", "PARM 1 : [YYYYMMDD] 表示人工指定執行日");
		}

		if (args.length == 1) {
			if (!args[0].toUpperCase(Locale.TAIWAN).equals("SYSDAY")) {
				if (!new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
					showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
					return -1;
				}
			}
			  searchDate = args[0];
		}
		
		//取得參數傳入並呼叫公用程式getProgDate()，本程式為D日檔
		searchDate = (args.length == 0) ? "" : args[0].trim();
		showLogMessage("I", "", String.format("程式參數1 = [%s]", searchDate));
		searchDate = getProgDate(searchDate, "D");

		//日期減一天
		searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
		
		showLogMessage("I", "", String.format("執行日期 = [%s]", searchDate));
		
     
        //(1) 信用卡則SELECT CRD_CHG_ID、CRD_IDNO、CARD_CARD
      
        List<ChgObj018> chgObjList = findChgObjList(searchDate);
     
        //(2) 產生檔案CRU23B1_TYPE_41_yyyymmdd.txt
        
		// get the fileFolderPath such as C:\EcsWeb\media\crdb
		String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + searchDate + ".txt");						
		filePath1 = fileName + searchDate + ".txt";

		// open CRU23B1_TYPE_41_yyyymmdd.txt file
		int outputFile = openOutputText(filePath);
		if (outputFile == -1) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在: %s", fileName));
			return -1;
		}

		String outputString = "";
		if (chgObjList != null) {
			outputString = getOutputString(chgObjList);
		}

		boolean isWriteOk = writeTextFile(outputFile, outputString);
		if (!isWriteOk) {
			throw new Exception("writeTextFile Exception");
		}

		closeOutputText(outputFile);
		
		//(3) 將檔案PUT到/crdatacrea/NCR2TCB路徑下
		
		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile1(filePath1);

      
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

  /**
        *主要篩選條件：判斷col_cpbdue資料
 *抓取CPBDUE_UPD_DTE指定異動日期的資料
 *抓取債商註記狀態為1,2,3,4,5,6的人。
 *待確認何謂為結案狀態(CARDLINK如何判斷？)
   * @param searchDate
   * @return ChgObjList: 有異動的資料
   * @throws Exception
   */
  private List<ChgObj018> findChgObjList(String searchDate) throws Exception {
	sqlCmd = " SELECT decode(c.CORP_NO,null,b.ID_NO,c.CORP_NO) AS ID_CORP_NO   ";  //主卡身份證
	sqlCmd += " ,CASE WHEN CPBDUE_TYPE='1' AND CPBDUE_BANK_TYPE IN ('1','2','3','4','5','6') THEN 'Y' ";  
	sqlCmd += " WHEN CPBDUE_TYPE='2' AND CPBDUE_TCB_TYPE IN ('1','2','3','4','5','6') THEN 'Y' "; 
	sqlCmd += " WHEN CPBDUE_TYPE='3' AND CPBDUE_MEDI_TYPE IN ('1','2','3','4','5','6') THEN 'Y'  ELSE 'N' END AS NEGO_FLAG ";//債協註記
	sqlCmd += " FROM COL_CPBDUE a ";
	sqlCmd += " LEFT JOIN crd_idno b ON a.CPBDUE_ID_P_SEQNO = b.ID_P_SEQNO ";
	sqlCmd += " LEFT JOIN crd_corp c ON a.CPBDUE_ID_P_SEQNO = c.CORP_P_SEQNO ";
	sqlCmd += " WHERE to_char(a.MOD_TIME,'yyyymmdd') >= ? OR CPBDUE_UPD_DTE >= ? ORDER BY CPBDUE_UPD_DTE ";
	setString(1, searchDate);
	setString(2, searchDate);
    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<ChgObj018> chgObjList = new LinkedList<ChgObj018>();
    ChgObj018 chgObj = null;

    for (int i = 0; i < selectCount; i++) {
    	
      chgObj = new ChgObj018();
      chgObj.idCorpNo = getValue("ID_CORP_NO", i);
      chgObj.negoFlag = getValue("NEGO_FLAG", i);

      chgObjList.add(chgObj);
    }

    return chgObjList;
  }

   

  /**
   * get file folder path by the project path and the file path selected from database
   * 
   * @param projectPath
   * @param filePathFromDb
   * @param fileNameAndTxt
   * @return
   * @throws Exception
   */
  private String getFilePath(String projectPath, String filePathFromDb, String fileNameAndTxt)
      throws Exception {
    String fileFolderPath = null;

    if (filePathFromDb.isEmpty() || filePathFromDb == null) {
      throw new Exception("file path selected from database is error");
    }
    String[] arrFilePathFromDb = filePathFromDb.split("/");

    projectPath=SecurityUtil.verifyPath(projectPath);
    fileFolderPath = Paths.get(projectPath).toString();
    for (int i = 0; i < arrFilePathFromDb.length; i++) {
      fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();
    }

    fileFolderPath=SecurityUtil.verifyPath(fileFolderPath);
    fileNameAndTxt=SecurityUtil.verifyPath(fileNameAndTxt);
    return Paths.get(fileFolderPath, fileNameAndTxt).toString();
  }

  /**
   * 產生output字串
   * CRU23B1-TYPE-18更新CRRSEG債協註記CRRDUE
   * CRU23B1-18-CODE	代碼(值為18)	9(02)	1-2
   * FILLER	未用	X(16)	3-18
   * CRU23B1-18-ID-NMBR	主卡身份證	X(11)	19-29
   * CRU23B1-18-CRRDUE	債協註記(Y表示有Ｎ表示無）只要有協商(前置協商、個別協商…), 結案就不放	X(01)	30-30
   * FILLER	保留	X(120)	31-150
   * @param chgObjList
   * @return
   * @throws UnsupportedEncodingException
   */
  private String getOutputString(List<ChgObj018> chgObjList) throws UnsupportedEncodingException {
	byte[] lineSeparatorArr = {'\r','\n'}; //0D0A
	String nextLine = new String(lineSeparatorArr,"MS950");
	
    StringBuilder sb = new StringBuilder();

    for (ChgObj018 chgObj : chgObjList) {
    	
      sb.append("18");
      sb.append(comc.fixLeft("", 16));
      sb.append(comc.fixLeft(chgObj.idCorpNo, 11));
      sb.append(comc.fixLeft(chgObj.negoFlag, 1));
      sb.append(comc.fixLeft("", 120));

      sb.append(nextLine);
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    InfC018 proc = new InfC018();
    int retCode = proc.mainProcess(args);
    System.exit(retCode);
  }

  /**
   * 找營業日
   * 
   * @param selectPtrBusinday
   * @return
   * @throws Exception
   */
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


  /***********************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + filePath1 + " 開始傳送....");
	      int errCode = commFTP.ftplogName("NCR2TCB", "mput " + filePath1);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + filePath1 + " 資料"+" errcode:"+errCode);
	          insertEcsNotifyLog(filePath1);          
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
			String tmpstr1 = comc.getECSHOME() + "/media/crdb/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/crdb/backup/" + removeFileName + "." + sysDate;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}

		

  
}


class ChgObj018 {
  String idCorpNo = "";
  String negoFlag = "";
}

