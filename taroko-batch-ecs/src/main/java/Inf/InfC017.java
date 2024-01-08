/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/05/04  V1.00.00    JustinWu     program initial                          
*  109/06/09  V1.00.01    JustinWu     if there is an given argument, use it as the value of open_date*
*  109-07-03  V1.00.02   shiyuqi       updated for project coding standard     *
*  109-07-22  V1.00.03   yanghan       修改了字段名称                                                                                   *
*  109-09-03  V1.00.05   JustinWu     lineSeparator -> 0D0A
*  109/09/05  V1.00.06    yanghan     code scan issue    
*  109/09/14  V1.00.06    Zuwei     code scan issue
*  109/09/14  V1.00.07   Wilson       新增procFTP                              *
*  109/09/29  V1.00.08   Wilson       InfCrb17 -> InfC017                    *    
*  109/09/30  V1.00.09   JustinWu    每次都產檔                                                                                           *
*  109/10/14  V1.00.10   Wilson      LOCAL_FTP_PUT -> NCR2TCB                *
*  109-10-19  V1.00.11    shiyuqi       updated for project coding standard  *
*  109-11-11  V1.00.12    tanwei        updated for project coding standard  *
*  110-02-01  V1.00.13    Alex          改用營業日								 *
*  110/10/01  V1.00.14    Wilson        將mainProcess private改 public         *
*  112/05/02  V1.00.15    Wilson        檔案搬到backup不更名                                                           *
*  112/12/06  V1.00.16    Wilson        檔名日期減一天                                                                            *
*****************************************************************************/
package Inf;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;


public class InfC017 extends AccessDAO {
  private final String progname = "產生送CRDB 17異動開卡狀態檔程式 112/12/06 V1.00.16";
  private String prgmId = "InfC017";
  CommCrdRoutine comcr = null;
  CommCrd comc = new CommCrd();
  CommFTP commFTP = null;
  CommRoutine comr = null;
  String tempUser = "";
  String filePath1 = "";
  String busiDate = "";
  String hOpenDate = "";
  private final String filePathFromDb = "media/crdb";
  private final String fileName = "CRU23B1_TYPE_17_";

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
      readSysdate();   
      String searchDate = (args.length == 0) ? "" : args[0].trim();
      if(searchDate.isEmpty())
    	  searchDate = hOpenDate;
      List<ChgObj17> chgObjList = findChgObjList(searchDate);


		// get the fileFolderPath such as C:\EcsWeb\media\crdb
		String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + searchDate + ".txt");

		filePath1 = fileName + searchDate + ".txt";

		// open CRU23B1_TYPE_17_yyyymmdd.txt file
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
  
  public void readSysdate() throws Exception {
	  sqlCmd = " select to_char(sysdate-1,'yyyymmdd') as sysdate1 ";
	  sqlCmd += "from dual";		
	  selectTable();
	  hOpenDate = getValue("sysdate1");
	}

  /**
   * 找出前一日有異動卡況的資料
   * 
   * @param searchDate
   * @return ChgObjList: 有異動的資料
   * @throws Exception
   */
  private List<ChgObj17> findChgObjList(String searchDate) throws Exception {
    selectSQL = " CARD_NO ";
    daoTable = " CCA_CARD_OPEN ";
    whereStr = " where 1=1 ";
    if (searchDate.length() == 0)
      whereStr += " and OPEN_DATE = to_char(sysdate - 1 day, 'yyyyMMdd') ";
    else {
      whereStr += " and OPEN_DATE = ? ";
      setString(1, searchDate);
    }

    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<ChgObj17> chgObjList = new LinkedList<ChgObj17>();
    ChgObj17 chgObj = null;

    for (int i = 0; i < selectCount; i++) {
      chgObj = new ChgObj17();

      chgObj.cardNo = getValue("CARD_NO", i);
      chgObj.idPSeqno = getIdPSeqno(chgObj.cardNo);
      chgObj.idNo = getIdno(chgObj.idPSeqno);

      chgObjList.add(chgObj);
    }

    return chgObjList;
  }

  /**
   * elect crd_card 得到ID_P_SEQNO
   * 
   * @param cardNo
   * @return
   * @throws Exception
   */
  private String getIdPSeqno(String cardNo) throws Exception {
    selectSQL = " ID_P_SEQNO ";
    daoTable = " CRD_CARD ";
    whereStr = " where 1=1 " + " and card_no = ? ";

    setString(1, cardNo);

    int selectCount = selectTable();

    if (selectCount == 0) {
      throw new Exception(String.format("找不到card_no = %s 的id_p_seqno", cardNo));
    }

    return getValue("ID_P_SEQNO");
  }

  /**
   * select crd_idno得到ID_NO
   * 
   * @param idPSeqno
   * @return
   * @throws Exception
   */
  private String getIdno(String idPSeqno) throws Exception {
    selectSQL = " ID_NO ";
    daoTable = " CRD_IDNO ";
    whereStr = " where 1=1 " + " and id_p_seqno = ? ";

    setString(1, idPSeqno);

    int selectCount = selectTable();

    if (selectCount == 0) {
      throw new Exception(String.format("找不到id_p_seqno = %s 的id_no", idPSeqno));
    }

    return getValue("ID_NO");
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
  private String getOutputString(List<ChgObj17> chgObjList) throws UnsupportedEncodingException {
	byte[] lineSeparatorArr = {'\r','\n'}; //0D0A
	String nextLine = new String(lineSeparatorArr,"MS950");
	
    StringBuilder sb = new StringBuilder();

    for (ChgObj17 chgObj : chgObjList) {
      sb.append("17");

      sb.append(comc.fixLeft(chgObj.cardNo, 16));
      sb.append(comc.fixLeft(chgObj.idNo, 11));
      sb.append("1");
      sb.append(comc.fixLeft("", 120));

      sb.append(nextLine);
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    InfC017 proc = new InfC017();
    int retCode = proc.mainProcess(args);
    System.exit(retCode);
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
			String tmpstr2 = comc.getECSHOME() + "/media/crdb/backup/" + removeFileName;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}

}


class ChgObj17 {
  String idPSeqno = "";
  String cardNo = "";
  String idNo = "";
}
