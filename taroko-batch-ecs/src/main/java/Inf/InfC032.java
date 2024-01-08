/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/05/18  V1.00.00   SunnyTs     program initial                          *
*  112/05/18  V1.00.00   SunnyTs    調整處理邏輯，此為附卡人變更ID，要刪除舊附卡人ID的卡片資料      *	
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


public class InfC032 extends AccessDAO {
  private final String progname = "產生送CRDB 32刪除CDESEG資料(附卡及商務卡舊ID)程式 112/05/18  V1.00.00";
  private String prgmId = "InfC032";
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
  private final String fileName = "CRU23B1_TYPE_3";
  //CRU23B1_TYPE_32

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

		//日期減一天
		searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
		
		showLogMessage("I", "", String.format("執行日期 = [%s]", searchDate));
      
       //(1) 信用卡則SELECT CRD_CHG_ID、CRD_IDNO、CARD_CARD
       //(2) 不包含VD卡，因為VD沒有附卡。

       List<ChgObj032> chgObjList = findChgObjList(searchDate);
       
       //(3) 產生檔案CRU23B1_TYPE_31_yyyymmdd.txt 及 CRU23B1_TYPE_32_yyyymmdd.txt
       //(4) 20221209 mod 產生CRU23B1_TYPE_31_yyyymmdd.txt此檔即可，因重新評估 CRU23B1_TYPE_32為刪除資料通知檔，新系統卡人及卡片不會有刪除的動作。
       
       //產生檔名處理，只產生32的檔案
        for (int i =2;i <= 2;i++) {
        	//for (int i =1;i <= 1;i++) {
        	String tmpfileNo = String.valueOf(i) +"_";
			// get the fileFolderPath such as C:\EcsWeb\media\crdb
			String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + tmpfileNo + searchDate + ".txt");						
			filePath1 = fileName +tmpfileNo + searchDate + ".txt";
	
			// open CRU23B1-TYPE-31_yyyymmdd.txt file
			// open CRU23B1-TYPE-32_yyyymmdd.txt file
			int outputFile = openOutputText(filePath);
			if (outputFile == -1) {
				showLogMessage("E", "", String.format("此路徑或檔案不存在: %s", fileName));
				return -1;
			}
	
			String outputString = "";
			if (chgObjList != null) {
				outputString = getOutputString(chgObjList , i);
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
        }

      
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
  private List<ChgObj032> findChgObjList(String searchDate) throws Exception {
    System.out.print(searchDate);
    
    /*於32此檔案提供舊ID (第3欄-持卡ID)及其對應之附卡及商務卡的卡號供刪除舊資料
     	CRU23X1-TYPE-32刪除CDESEG資料
		CRU23X1-32-CODE	代碼	9(02)	1-2
		CRU23X1-32-CARD-NMBR	卡號	9(16)	3-18
		CRU23X1-32-ID-NMBR	持卡ID(變更前-舊ID)	X(11)	19-29
		CRU23X1-32-PP-ID	正卡ID/公司統編	X(11)	30-40
		FILLER	保留	X(110)	41-150
     */

    //提供舊ID (第3欄-持卡ID)及其對應之附卡及商務卡的卡號供刪除舊資料
    
    sqlCmd += " SELECT CRT_DATE,CARD_NO CARD_NO,sup_id OLD_ID_NO,ID_NO major_id,ACCT_TYPE ";
    sqlCmd += " FROM ( ";
    sqlCmd += " SELECT ";
    sqlCmd += " A.CRT_DATE,A.ID_NO AS ID_NO,A.OLD_ID_NO AS CHG_OLD_ID,b.CARD_NO,b.SUP_FLAG,b.ACCT_TYPE, ";
    sqlCmd += " (SELECT ID_NO FROM crd_idno WHERE ID_P_SEQNO=b.MAJOR_ID_P_SEQNO) major_id, ";
    sqlCmd += " (SELECT ID_NO FROM crd_idno WHERE ID_P_SEQNO=b.ID_P_SEQNO) sup_id ";
    sqlCmd += " FROM CRD_CHG_ID a,crd_card b ";
    sqlCmd += " WHERE a.ID_P_SEQNO = b.MAJOR_ID_P_SEQNO ";
    sqlCmd += " AND B.ID_P_SEQNO<>B.MAJOR_ID_P_SEQNO ";
    sqlCmd += " AND NOT exists (SELECT 1 FROM crd_card c WHERE a.ID_P_SEQNO=c.ID_P_SEQNO AND SUP_FLAG='0' AND CURRENT_CODE='0' AND ACNO_FLAG='1') ";
    sqlCmd += " UNION ";
    sqlCmd += " SELECT A.CRT_DATE,b.CORP_NO AS ID_NO,A.OLD_ID_NO AS CHG_OLD_ID,b.CARD_NO,b.SUP_FLAG,ACCT_TYPE, ";
    sqlCmd += " (SELECT ID_NO FROM crd_idno WHERE ID_P_SEQNO=b.MAJOR_ID_P_SEQNO) major_id, ";
    sqlCmd += " (SELECT ID_NO FROM crd_idno WHERE ID_P_SEQNO=b.ID_P_SEQNO) sup_id ";
    sqlCmd += " FROM CRD_CHG_ID a,crd_card b ";
    sqlCmd += " WHERE a.ID_P_SEQNO = b.MAJOR_ID_P_SEQNO ";
    sqlCmd += " AND B.ACNO_FLAG!='1') ";
    sqlCmd += " WHERE CRT_DATE = ? ";
    sqlCmd += " ORDER BY CRT_DATE,acct_type ";
	
	setString(1, searchDate);
    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<ChgObj032> chgObjList = new LinkedList<ChgObj032>();
    ChgObj032 chgObj = null;

    for (int i = 0; i < selectCount; i++) {
    	
      chgObj = new ChgObj032();
      chgObj.cardNo = getValue("CARD_NO", i);
      chgObj.idNo = getValue("ID_NO", i);
      chgObj.majorId = getValue("MAJOR_ID", i);

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
  private String getOutputString(List<ChgObj032> chgObjList ,int fileno) throws UnsupportedEncodingException {
	byte[] lineSeparatorArr = {'\r','\n'}; //0D0A
	String nextLine = new String(lineSeparatorArr,"MS950");
	
    StringBuilder sb = new StringBuilder();

    for (ChgObj032 chgObj : chgObjList) {
    	
      String seqno = "3" + String.valueOf(fileno)	;
      sb.append(seqno);
      sb.append(comc.fixLeft(chgObj.cardNo, 16));
      sb.append(comc.fixLeft(chgObj.idNo, 11));
      sb.append(comc.fixLeft(chgObj.majorId, 11));
      sb.append(comc.fixLeft("", 110));

      sb.append(nextLine);
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    InfC032 proc = new InfC032();
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


class ChgObj032 {
  String cardNo = "";
  String idNo = "";
  String majorId = "";
}

