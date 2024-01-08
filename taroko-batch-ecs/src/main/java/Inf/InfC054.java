/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  111/12/09  V1.00.00   Castor     program initial                          *
*  112/02/27  V1.00.01   sunny      fix STAT_SEND_INTERNET                   *
*  112/04/20  V1.00.02    Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。         *
*  112/08/22  V1.00.03   sunny      調整SQL語法，取消UF_IDNO_id(ID_P_SEQNO)寫法    *
*  112/09/15  V1.00.04   sunny	        修正SQL語法 and及or判斷處理                                     *
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


public class InfC054 extends AccessDAO {
  private final String progname = "產生送CRDB 54 異動 CDESEG FOR 申請電子帳單註記&電子郵件地址程式 112/09/15  V1.00.04";
  private String prgmId = "InfC054";
  CommCrdRoutine comcr = null;
  CommCrd comc = new CommCrd();
  CommFunction comm = new CommFunction();
  CommFTP commFTP = null;
  CommRoutine comr = null;
  CommDate comDate = new CommDate();
  String tempUser = "";
  String filePath1 = "";


  String hBusiBusinessDate = "";

  String searchDate = "";
  
  
  private final String filePathFromDb = "media/crdb";
  private final String fileName = "CRU23B1_TYPE_54_";

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

      // 檢核參數的日期格式
       if (args.length >= 1) {
     	   showLogMessage("I", "", "PARM 1 : [無參數] 表示抓取businday ");
    	   showLogMessage("I", "", "PARM 1 : [SYSDAY] 表示抓取系統日");
    	   showLogMessage("I", "", "PARM 1 : [YYYYMMDD] 表示人工指定執行日");
		}
		if(args.length == 1) {
			if (!args[0].toUpperCase(Locale.TAIWAN).equals("SYSDAY")) {
					if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
		        showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
		        return -1;
		    }
			}
		    searchDate = args[0];
		}
//		else {
//		    comc.errExit("參數1：非必填，預設為系統日，也可輸入西元年(如：20200715)", "");
//		}             		
      
		// get searchDate
		String searchDate = (args.length == 0) ? "" : args[0].trim();
		showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
		searchDate = getProgDate(searchDate, "D");
		
		//日期減一天
		searchDate = comDate.dateAdd(searchDate, 0, 0, -1);

		showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
      
//      selectPtrBusinday();
//		
//      // 若沒有給定查詢日期，則查詢日期為系統日
//      if(args.length == 0) {
//    	  searchDate = hBusiBusinessDate;
//      }else

      
        //(1) 以持有一般信用卡者自然人為主(排除法人，且不包含VD卡)。
        //(2) 主要篩選條件：
		//    2-1、僅帳務類別01,即一般信用卡卡友。
		//    2-2、以下符合任一條件即為符合。
		//    2-2-1、判斷有異動訂閱電子帳單註記，且執行當日(系統日)須在設定的起迄期間內
		//      (註:起日無值時視同’000000’,迄日無值時視同’999999’)。
		//    2-2-2、是訂閱電子帳單註記為Y且異動電子帳單信箱的人。
		//    2-3、判斷為程式執行日(如:營業日, 即系統日的前一天)異動。

        List<ChgObj054> chgObjList = findChgObjList(searchDate);
       
        //(3) 產生big5格式的檔案，檔名為：CRU23B1_TYPE_54_yyyymmdd.txt
       
		// get the fileFolderPath such as /cr/ecs/media/crdb
		String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + searchDate + ".txt");						
		filePath1 = fileName + searchDate + ".txt";

		// open CRU23B1_TYPE_54_yyyymmdd.txt file
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
		
		//(4) 將檔案PUT到/crdatacrea/NCR2TCB路徑下。
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
  * 篩選資料
  * 
  * @param searchDate
  * @return ChgObjList: 符合條件的資料
  * @throws Exception
  */  
  private List<ChgObj054> findChgObjList(String searchDate) throws Exception {
    System.out.print(searchDate);
	
	sqlCmd = " SELECT B.ID_NO,A.ID_P_SEQNO, ";
	sqlCmd += " CASE WHEN A.STAT_SEND_INTERNET='Y' AND to_char(sysdate,'yyyymm') ";
	sqlCmd += " AND DECODE(A.STAT_SEND_E_MONTH2,'','999999',A.STAT_SEND_E_MONTH2) ";		
    sqlCmd += " THEN A.STAT_SEND_INTERNET ELSE 'N' END AS STAT_SEND_INTERNET, ";
    sqlCmd += " A.E_MAIL_EBILL ";
    sqlCmd += " FROM ACT_ACNO A ,CRD_IDNO B";
    sqlCmd += " WHERE A.ID_P_SEQNO=B.ID_P_SEQNO ";
    sqlCmd += " AND A.ACCT_TYPE='01' ";
//    sqlCmd += " AND EXISTS (SELECT 1 FROM CRD_IDNO B WHERE A.ID_P_SEQNO=B.ID_P_SEQNO) ";
    sqlCmd += " AND (A.internet_upd_date=? OR (A.E_MAIL_EBILL_DATE=? AND A.STAT_SEND_INTERNET='Y')) ";
	
	setString(1, searchDate);
	setString(2, searchDate);
    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<ChgObj054> chgObjList = new LinkedList<ChgObj054>();
    ChgObj054 chgObj = null;

    for (int i = 0; i < selectCount; i++) {
    	
      chgObj = new ChgObj054();
      chgObj.idNo= getValue("ID_NO", i);
      chgObj.idPSeqno= getValue("ID_P_SEQNO", i);
      chgObj.statSendInternet = getValue("STAT_SEND_INTERNET", i);
      chgObj.eMailEbill = getValue("E_MAIL_EBILL", i);
           
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
  private String getOutputString(List<ChgObj054> chgObjList) throws UnsupportedEncodingException {
	byte[] lineSeparatorArr = {'\r','\n'}; //0D0A
	String nextLine = new String(lineSeparatorArr,"MS950");
	
    StringBuilder sb = new StringBuilder();

    for (ChgObj054 chgObj : chgObjList) {
    	
      sb.append("54");
      sb.append(comc.fixLeft(chgObj.idNo, 10));
      sb.append(comc.fixLeft(chgObj.statSendInternet, 1));
      sb.append(comc.fixLeft(chgObj.eMailEbill, 30));
      sb.append(comc.fixLeft("", 108));


      sb.append(nextLine);
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    InfC054 proc = new InfC054();
    int retCode = proc.mainProcess(args);
    System.exit(retCode);
  }

//  /**
//   * 找營業日
//   * 
//   * @param selectPtrBusinday
//   * @return
//   * @throws Exception
//   */
///***********************************************************************/
//	void selectPtrBusinday() throws Exception {
//		hBusiBusinessDate = "";
//
//		sqlCmd = " select business_date ";
//		sqlCmd += " from ptr_businday ";
//		sqlCmd += " fetch first 1 rows only ";
//		selectTable();
//		if (notFound.equals("Y")) {
//			comcr.errRtn("select_ptr_businday not found!", "", "");
//		}
//	
//		hBusiBusinessDate = getValue("business_date");
//			
//	}


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


class ChgObj054 {
  String idNo = "";
  String idPSeqno = "";
  String statSendInternet = "";
  String eMailEbill = "";

}

