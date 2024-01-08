/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  110/08/04  V1.00.00   Castor     program initial                          *
*  110/08/20  V1.00.01   SunnyTs    將mainProcess private改 public            *
*  112/04/20  V1.00.02   Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。                      *	
*  112/05/18  V1.00.03   SunnyTs    調整處理邏輯，此為正卡人變更ID，更新附卡人卡片之正卡ID的資料    *
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


public class InfC031 extends AccessDAO {
  private final String progname = "產生送CRDB 31更新CDESEG資料(附卡及商務卡)程式 112/05/18  V1.00.03";
  private String prgmId = "InfC031";
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
  //CRU23B1_TYPE_31
  

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

       List<ChgObj031> chgObjList = findChgObjList(searchDate);
       
       //(3) 產生檔案CRU23B1_TYPE_31_yyyymmdd.txt 及 CRU23B1_TYPE_32_yyyymmdd.txt
       //(4) 20221209 mod 產生CRU23B1_TYPE_31_yyyymmdd.txt此檔即可，因重新評估 CRU23B1_TYPE_32為刪除資料通知檔，新系統卡人及卡片不會有刪除的動作。
              
       //產生檔名處理，只產生31的檔案
       // for (int i =1;i <= 2;i++) {
       	for (int i =1;i <= 1;i++) {
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
  private List<ChgObj031> findChgObjList(String searchDate) throws Exception {
    System.out.print(searchDate);
    
    /*
	sqlCmd = " SELECT b.CARD_NO CARD_NO,c.ID_NO ID_NO,UF_IDNO_ID(MAJOR_ID_P_SEQNO) as MAJOR_ID ";
	sqlCmd += " FROM crd_idno a,crd_card b, crd_chg_id c ";
	sqlCmd += " WHERE a.ID_P_SEQNO=b.ID_P_SEQNO ";  //正附卡全部
	sqlCmd += " And b.ID_P_SEQNO<>b.Major_ID_P_SEQNO ";  //僅挑選附卡
	sqlCmd += " AND a.ID_NO=c.ID_NO ";
	sqlCmd += " AND exists (SELECT 1 FROM crd_card d WHERE a.ID_P_SEQNO=d.ID_P_SEQNO AND SUP_FLAG='0' AND CURRENT_CODE='0' AND acct_type='01') ";
	sqlCmd += " AND c.crt_date= ? ";
	sqlCmd += " ORDER BY c.id_no,c.MOD_TIME";
	*/
    
    /*變更正卡人ID或統編時(如下)，以下規則符合則一律從31此檔案提供資訊。
     * (1)	當【正卡人ID】有變更時且有附卡資料，提供新ID (第4欄-正卡ID-majorId)。
     * (2)	當【公司統編 】有變更時且有商務卡資料，提供新ID (第4欄-公司統編-majorId)
     *      (新系統現無變更公司統編之功能)
     */
    
	//附卡之正卡人有變更ID    
	sqlCmd = "SELECT CRT_DATE,CARD_NO CARD_NO,sup_id ID_NO,CHG_NEW_ID major_id ";
	sqlCmd += " FROM ( ";
	sqlCmd += " SELECT ";
	sqlCmd += " A.CRT_DATE,A.ID_NO AS CHG_NEW_ID,A.OLD_ID_NO AS CHG_OLD_ID,b.CARD_NO,b.SUP_FLAG,";
	sqlCmd += " (SELECT ID_NO FROM crd_idno WHERE ID_P_SEQNO=b.MAJOR_ID_P_SEQNO) major_id, ";
	sqlCmd += " (SELECT ID_NO FROM crd_idno WHERE ID_P_SEQNO=b.ID_P_SEQNO) sup_id ";
	sqlCmd += " FROM CRD_CHG_ID a,crd_card b ";
	sqlCmd += " WHERE a.ID_P_SEQNO = b.MAJOR_ID_P_SEQNO ";
	sqlCmd += " AND B.ID_P_SEQNO<>B.MAJOR_ID_P_SEQNO ";
	sqlCmd += " AND exists (SELECT 1 FROM crd_card c WHERE a.ID_P_SEQNO=c.ID_P_SEQNO ";
	sqlCmd += " AND SUP_FLAG='0' AND CURRENT_CODE='0' AND ACNO_FLAG='1') ";
	sqlCmd += " ORDER BY A.CRT_DATE ) ";
	sqlCmd += " WHERE CRT_DATE = ? ";
			
	setString(1, searchDate);
    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<ChgObj031> chgObjList = new LinkedList<ChgObj031>();
    ChgObj031 chgObj = null;

    for (int i = 0; i < selectCount; i++) {
    	
      chgObj = new ChgObj031();
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
  private String getOutputString(List<ChgObj031> chgObjList ,int fileno) throws UnsupportedEncodingException {
	byte[] lineSeparatorArr = {'\r','\n'}; //0D0A
	String nextLine = new String(lineSeparatorArr,"MS950");
	
    StringBuilder sb = new StringBuilder();

    for (ChgObj031 chgObj : chgObjList) {
    	
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
    InfC031 proc = new InfC031();
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


class ChgObj031 {
  String cardNo = "";
  String idNo = "";
  String majorId = "";
}

