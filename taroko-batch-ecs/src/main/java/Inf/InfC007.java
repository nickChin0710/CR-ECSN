/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  110/08/05  V1.00.00   Castor     program initial                          *
*  110/08/20  V1.00.01   SunnyTs    將mainProcess private改 public            *
*  112/03/02  V1.00.02   Sunny      修正檔名，-換成_底線                                                  *
*  112/04/20  V1.00.03   Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。                                                     *	
*****************************************************************************/
package Inf;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommFunction;

import Dxc.Util.SecurityUtil;


public class InfC007 extends AccessDAO {
  private final String progname = "產生送CRDB 07 更新CAASEG持卡人CRAIDN資料的程式 112/04/20  V1.00.03";
  private String prgmId = "InfC007";
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
  private final String fileName = "CRU23B1_TYPE_07_";

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
		
      // 若沒有給定查詢日期，則查詢日期為系統日
      if(args.length == 0) {
    	  searchDate = hBusiBusinessDate;
      }else
      if(args.length == 1) {
          if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
              showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
              return -1;
          }
          searchDate = args[0];
      }else {
          comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
      }                       
 
        //(1) 信用卡則SELECT CRD_CHG_ID、CRD_IDNO、CARD_CARD
        //(2) VD卡則SELECT DBC_CHG_ID、DBC_IDNO、DBC_CARD

		//日期減一天
		searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
		showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
      
        List<ChgObj007> chgObjList = findChgObjList(searchDate);
        
        //(3) 產生檔案CRU23B1_TYPE_07_yyyymmdd.txt
        //(5) 若無符合的資料，一樣會產生空檔
		// get the fileFolderPath such as C:\EcsWeb\media\crdb
		String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + searchDate + ".txt");						
		filePath1 = fileName + searchDate + ".txt";

		// open CRU23B1-TYPE-07_yyyymmdd.txt file
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
		
		//(4) 將檔案PUT到/crdatacrea/NCR2TCB路徑下
		
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
   * 找出前一日有異動卡況的資料
   * 
   * @param searchDate
   * @return ChgObjList: 有異動的資料
   * @throws Exception
   */
  private List<ChgObj007> findChgObjList(String searchDate) throws Exception {
    System.out.print(searchDate);

	sqlCmd = " SELECT CARD_NO,MAJOR_ID,OLD_ID, NEW_ID ";
	sqlCmd += " FROM ( ";
	sqlCmd += " SELECT b.CARD_NO, UF_IDNO_ID(b.MAJOR_ID_P_SEQNO) MAJOR_ID, c.OLD_ID_NO OLD_ID , c.ID_NO NEW_ID,c.MOD_TIME ";
	sqlCmd += " FROM crd_idno a,crd_card b, crd_chg_id c ";
	sqlCmd += " WHERE a.ID_P_SEQNO=b.ID_P_SEQNO ";  //篩選正附卡
	sqlCmd += " AND a.ID_NO=c.ID_NO ";
	sqlCmd += " AND c.crt_date= ? ";
	sqlCmd += " UNION ALL ";
	sqlCmd += " SELECT b.CARD_NO, UF_VD_IDNO_ID(b.MAJOR_ID_P_SEQNO) MAJOR_ID,c.ID OLD_ID, c.AFT_ID NEW_ID,c.MOD_TIME ";
	sqlCmd += " FROM dbc_idno a,dbc_card b, dbc_chg_id c ";
	sqlCmd += " WHERE a.ID_P_SEQNO=b.ID_P_SEQNO ";  //篩選正附卡，但VD僅有正卡
	sqlCmd += " AND a.ID_NO=c.AFT_ID ";
	sqlCmd += " AND c.crt_date= ? )";
	sqlCmd += " ORDER BY NEW_ID,MOD_TIME ";
	setString(1, searchDate);
	setString(2, searchDate);
    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<ChgObj007> chgObjList = new LinkedList<ChgObj007>();
    ChgObj007 chgObj = null;

    for (int i = 0; i < selectCount; i++) {
    	
      chgObj = new ChgObj007();
      chgObj.cardNo = getValue("CARD_NO", i);
      chgObj.idNo = getValue("MAJOR_ID", i);
      chgObj.oldIdNo = getValue("OLD_ID", i);
      chgObj.newIdNo = getValue("NEW_ID", i);

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
   * 
   * @param chgObjList
   * @return
   * @throws UnsupportedEncodingException
   */
  private String getOutputString(List<ChgObj007> chgObjList) throws UnsupportedEncodingException {
	byte[] lineSeparatorArr = {'\r','\n'}; //0D0A
	String nextLine = new String(lineSeparatorArr,"MS950");
	
    StringBuilder sb = new StringBuilder();

    for (ChgObj007 chgObj : chgObjList) {
    	
      sb.append("07");
      sb.append(comc.fixLeft(chgObj.cardNo, 16));
      sb.append(comc.fixLeft(chgObj.idNo, 11));
      sb.append(comc.fixLeft(chgObj.oldIdNo, 11));
      sb.append(comc.fixLeft(chgObj.newIdNo, 11));
      sb.append(comc.fixLeft("", 99));

      sb.append(nextLine);
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    InfC007 proc = new InfC007();
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


class ChgObj007 {
  String cardNo = "";
  String idNo = "";
  String oldIdNo = "";
  String newIdNo = "";
}

