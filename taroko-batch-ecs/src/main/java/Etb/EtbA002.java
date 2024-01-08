/*************************************************************************************
*                                                                                    *
*                              MODIFICATION LOG                                      *
*                                                                                    *
*     DATE          Version    AUTHOR              DESCRIPTION                       *
*  ---------       --------- ----------- ------------------------------------        *
*  111/05/27        V1.00.00    JustinWu           program initial                   *    
*  111/06/01        V1.00.01    JustinWu           change_code '99'->'ID'            * 
*                   V1.00.02    JustinWu           fix bugs                          *
*  111/06/02        V1.00.03    JustinWu           fix bugs                          *
*                   V1.00.04    JustinWu           調整noChgReason1中文敘述                                       *
*  111/08/04        V1.00.05    Ryan               檢核ID不足10碼直接下一筆                                                *
*  111/09/13        V1.00.06    Wilson      update act_acno增加card_indicator = '1'   *
*  111/10/05        V1.00.07    Wilson      updateAcno 找不到資料繼續往下執行                                          * 
*  113/01/04        V1.00.08    Wilson      mainProcess宣告改為public                   *
*****************************************************************************/
package Etb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommTxBill;

public class EtbA002 extends AccessDAO {
  static private final String DBC_IDNO = "DBC_IDNO";
  static private final String CRD_IDNO = "CRD_IDNO";
  static private final String DBC_CARD = "DBC_CARD";
  static private final String CRD_CARD = "CRD_CARD";
  static private final String ACT_ACNO = "ACT_ACNO";
  static private final String DBA_ACNO = "DBA_ACNO";
  static private final String CRD_CHG_ID = "CRD_CHG_ID";
  static private final String DBC_CHG_ID = "DBC_CHG_ID";
  static private final String CRD_IDNO_SEQNO = "CRD_IDNO_SEQNO";
  private final String PROGNAME = "主機變更ID檔處理程式 113/01/04 V1.00.08";
  private final String prgmId = "EtbA002";
  
  private final byte[] lineSeparatorBytes = System.lineSeparator().getBytes();
  private final byte emptyByte = " ".getBytes()[0];
  
  CommCrdRoutine comcr = null;
  CommCrd comc = new CommCrd();
  CommDate commDate = new CommDate();
  String tempUser = "";

  private final String FILE_PATH = "media/crd";
  private final String FILE_NAME_HEAD = "CTMCHGW2";
  private final int COMMIT_NUM = 1000;
  
  private int noChgCnt = 0;

  public int mainProcess(String[] args) {
    String callBatchSeqno = "";

    try {
      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + PROGNAME);

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);

      // =====================================
      String fileName = "";
//      fileName = String.format("%s_%s", fileNameHead, queryDate);
      fileName = FILE_NAME_HEAD;
      
      // get the fileFolderPath such as C:\EcsWeb\media\crd
      String filePath = getFilePath(comc.getECSHOME(), FILE_PATH, fileName);

      /** 讀取變更ID檔資料並INSERT到CRD_0CJ0_POST **/
      int insertDataNum = 0;
      int inputFile = openInputText(filePath, "MS950");
      if (inputFile == -1) {
        showLogMessage("I", "", String.format("檔案不存在: %s", filePath));
      }else {
    	  ChgInfoData chgInfoData = null;
          String text = "";
          int uncommitCnt = 0;
          while (true) {
            text = readTextFile(inputFile);
            if (text.trim().length() != 0) {
            	// 從檔案取得物件
    			chgInfoData = getChangingInfoObjFromText(text);
    			
    			// 將物件insert至Crd_0cj0
    			insertCrd0cj0Post(chgInfoData);
    			
    			uncommitCnt++;
    			insertDataNum++;
            }

            if (endFile[inputFile].equals("Y")) {
            	closeInputText(inputFile);
            	if (uncommitCnt > 0) {
            		commitDataBase();
            	}
            	break;
            }
            
            if (uncommitCnt == COMMIT_NUM) {
            	commitDataBase();
            	uncommitCnt = 0;
            }
          }
          
          /** 搬檔到BACKUP並變更檔名 **/
          moveTxtToBackup(filePath, fileName);
      }

//      List<NoChangeReport> noChgReportList = new ArrayList<NoChangeReport>();
      
      // start processing data
      showLogMessage("I", "", "開始讀取CRD_0CJ0_POST並處理......");
      int processNum = modifyCustormerInfo();
      if (processNum == 0) showLogMessage("I", "", "CRD_0CJ0_POST無資料需處理");

      /** 2022/05/26 Justin
      // 輸出未更新報表
      if (noChgReportList.size() != 0) {
			String noChgReportFileName = String.format("%s_%s", "0CJ0_NOCHANGE", sysDate);
			
			// media/crd/error
			Path outputFileFolderPath = Paths.get(comc.getECSHOME(), FILE_PATH, "error");
			
			// create the parent directory if parent the directory is not exist
			Files.createDirectories(outputFileFolderPath);
			
			// get output file path
			String outputFilePath = Paths.get(outputFileFolderPath.toString(), noChgReportFileName).toString();
    	  
			outputNoChgReport(noChgReportList, outputFilePath);
			procFTP(noChgReportFileName, outputFileFolderPath.toString());
			
	  }
	  **/
      
      showLogMessage("I", "", String.format("處理日期: %s, 檔案資料新增筆數: %d筆", sysDate, insertDataNum));
      showLogMessage("I", "", String.format("總處理筆數: %d筆, 未更新筆數: %d筆", processNum, noChgCnt));
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

private void insertCrd0cj0Post(ChgInfoData chgInfoData) throws Exception {
	daoTable = "CRD_0CJ0_POST";

	extendField = "CRD_0CJ0_POST.";
    setValue("CRD_0CJ0_POST.TX_DATE", commDate.tw2adDate(chgInfoData.outDate));
    setValue("CRD_0CJ0_POST.TX_TIME", chgInfoData.outTime);
    setValue("CRD_0CJ0_POST.ID_NO", chgInfoData.outCikeyOld);
    setValue("CRD_0CJ0_POST.CHANGE_CODE", "ID");
    setValue("CRD_0CJ0_POST.CHANGE_CONTAIN", chgInfoData.outCikeyNew);
    setValue("CRD_0CJ0_POST.ACCT_NO", chgInfoData.outNoAccount);
    setValue("CRD_0CJ0_POST.MOD_USER", prgmId);
    setValue("CRD_0CJ0_POST.MOD_TIME", sysDate + sysTime);
    setValue("CRD_0CJ0_POST.MOD_PGM", prgmId);

    insertTable();
}

private int modifyCustormerInfo() throws Exception {
	int processNumber = 0;
	/** SELECT CRD_0CJ0_POST讀取未處理過的資料 **/
	StringBuilder sb = new StringBuilder();
	sb.append("SELECT TX_DATE, TX_TIME, ID_NO, CHANGE_CODE, CHANGE_CONTAIN, ")
	  .append("ADDR_CHG_FLAG, ENG_CHG_RIS_FLAG, DEAD_FLAG, rowid as rowid ")
	  .append("FROM CRD_0CJ0_POST ")
	  .append("WHERE POST_FLAG <> 'Y' ")
	  .append("AND CHANGE_CODE = 'ID' ");
	sqlCmd = sb.toString();
	int cursorIndex = openCursor();
	while (fetchTable(cursorIndex)) {
		ChgInfoObj chgInfoObj = getChInfoObjFromDB();
		processModification(chgInfoObj);
		updateCrd0cj0Post(chgInfoObj);
		commitDataBase();
		processNumber++;
	}
	closeCursor(cursorIndex);
	return processNumber;
}

private void updateCrd0cj0Post(ChgInfoObj chgInfoObj) throws Exception {
	if (chgInfoObj.isNoChg)
		showLogMessage("I", "", String.format("[未更新]old_id_no[%s] -> new_id_no[%s], %s", 
				chgInfoObj.id, chgInfoObj.chgContain, chgInfoObj.noChgReason.getReason()));
	
	StringBuffer sb = new StringBuffer();
	sb.append(" POST_FLAG = 'Y' , POST_DATE = to_char(sysdate,'yyyyMMdd'),  ")
	  .append(" POST_RESULT_CODE = ? , POST_RESULT_DESC = ?,  ")
	  .append(" MOD_USER = ? , MOD_TIME = sysdate , MOD_PGM = ? ");
	
	updateSQL = sb.toString();
	whereStr = " WHERE rowid = ? ";
	
	setString(1, chgInfoObj.isNoChg ? chgInfoObj.noChgReason.getCode() : "00");
	setString(2, chgInfoObj.isNoChg ? chgInfoObj.noChgReason.getReason() : "");
	setString(3, prgmId);
	setString(4, prgmId);
	setRowId(5, chgInfoObj.rowId);
	
	daoTable = "CRD_0CJ0_POST";
	
	int returnInt = updateTable();
    if (returnInt == 0) {
      String errorlog = String.format("%s %s, chgCode[%s], id[%s], date[%s], time[%s]", 
    		  "fail to update ", "CRD_0CJ0_POST", chgInfoObj.chgCode, chgInfoObj.id, chgInfoObj.date, chgInfoObj.time);
      log(errorlog) ;
      throw new Exception("Fail to update CRD_0CJ0_POST");
    }

}

private ChgInfoObj getChInfoObjFromDB() throws Exception {
	ChgInfoObj chgInfoObj = new ChgInfoObj();
	chgInfoObj.date = getValue("TX_DATE");
	chgInfoObj.time = getValue("TX_TIME");
	chgInfoObj.id = getValue("ID_NO");
	chgInfoObj.chgCode = getValue("CHANGE_CODE");
	chgInfoObj.chgContain = getValue("CHANGE_CONTAIN");
	chgInfoObj.crdCardAddrChgFlag = getValue("ADDR_CHG_FLAG");
	chgInfoObj.chgCrdCardFlag = getValue("ENG_CHG_RIS_FLAG");
	chgInfoObj.deadFlag = getValue("DEAD_FLAG");
	chgInfoObj.rowId = getValue("rowid");
	return chgInfoObj;
}

private boolean processModification(ChgInfoObj chgInfoObj) throws Exception {
	
	/** 檢核ID變前是否存在資料庫 **/
	IdnoObj crdIdnoObj = getIdnoObj(CRD_IDNO, chgInfoObj.id);
	IdnoObj dbcIdnoObj = getIdnoObj(DBC_IDNO, chgInfoObj.id);
    if (crdIdnoObj == null && dbcIdnoObj == null) {
//    	noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason1.getReason(), chgInfoObj));
		chgInfoObj.setNoChgReason(ErrorReason.noChgReason1);
		noChgCnt++;
		return true;
    }
    
    /** 判斷ID變後是否已存在資料庫 **/
    IdnoObj crdIdnoObjByNewIdno = null;
    IdnoObj dbcIdnoObjByNewIdno = null;
    
    if(chgInfoObj.chgContain.length()<10) {
    	chgInfoObj.setNoChgReason(ErrorReason.noChgReason3);
		noChgCnt++;
		return true;
    }
    crdIdnoObjByNewIdno = getIdnoObj(CRD_IDNO, chgInfoObj.chgContain);
	if (crdIdnoObjByNewIdno != null) {
		if (doesHasCard(CRD_CARD, crdIdnoObjByNewIdno.idPSeqno)) {
//			noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
			chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
			noChgCnt++;
			return true;
		}else {
			deleteIdno(CRD_IDNO, crdIdnoObjByNewIdno.idPSeqno);
			deleteCrdIdnoSeqno(false, crdIdnoObjByNewIdno.idPSeqno);
		}
	}
	dbcIdnoObjByNewIdno = getIdnoObj(DBC_IDNO, chgInfoObj.chgContain);
	if (dbcIdnoObjByNewIdno != null) {
		if (doesHasCard(DBC_CARD, dbcIdnoObjByNewIdno.idPSeqno)) {
//			noChgReportList.add(new NoChangeReport(ErrorReason.noChgReason2.getReason(), chgInfoObj));
			chgInfoObj.setNoChgReason(ErrorReason.noChgReason2);
			noChgCnt++;
			return true;
		}else {
			deleteIdno(DBC_IDNO, dbcIdnoObjByNewIdno.idPSeqno);
			deleteCrdIdnoSeqno(true, dbcIdnoObjByNewIdno.idPSeqno);
		}
	}
	
	
	/** UPDATE CRD_IDNO、ÁCT_ACNO、UPDATE OR INSERT CRD_CHG_ID **/
	if (crdIdnoObj != null) {
		boolean isUpdated = updateIdno(CRD_IDNO, chgInfoObj.chgContain, crdIdnoObj.idPSeqno);
		if (isUpdated) {
			updateAcno(ACT_ACNO, chgInfoObj.chgContain, crdIdnoObj.idPSeqno);
			if (doesHasChgId(CRD_CHG_ID, chgInfoObj.id)) 
				updateChgId(CRD_CHG_ID, chgInfoObj, crdIdnoObj);
			else 
				insertChgId(CRD_CHG_ID, chgInfoObj, crdIdnoObj);
		}
	}

	/** UPDATE DBC_IDNO、DBA_ACNO、INSERT DBC_CHG_ID **/
	if (dbcIdnoObj != null) {
		boolean isUpdated = updateIdno(DBC_IDNO, chgInfoObj.chgContain, dbcIdnoObj.idPSeqno);
		if (isUpdated) {
			updateAcno(DBA_ACNO, chgInfoObj.chgContain, dbcIdnoObj.idPSeqno);
			insertChgId(DBC_CHG_ID, chgInfoObj, dbcIdnoObj);
		}
	}
	
	/** UPDATE CRD_IDNO_SEQNO、CRD_EMPLOYEE **/
	updateCrdIdnoSeqno(chgInfoObj.chgContain, crdIdnoObj != null ? crdIdnoObj.idPSeqno : dbcIdnoObj.idPSeqno);
	updateCrdEmployee(chgInfoObj.chgContain, chgInfoObj.id);
	
    return true;
}

private boolean updateCrdEmployee(String newIdno, String oldIdno) throws Exception {
	updateSQL = " ID = ? ";
	whereStr  = " WHERE ID = ? ";
	setString(1, newIdno);
	setString(2, oldIdno);
	
	daoTable = "CRD_EMPLOYEE";
	
	int returnInt = updateTable();
    if (returnInt == 0) {
      log(String.format("%s[old_id_no = %s]", "Fail to update CRD_EMPLOYEE", oldIdno)) ;
      return false;
    }
	
	return true;
}

private boolean updateCrdIdnoSeqno(String newIdno, String idPSeqno) throws Exception {
	updateSQL = " ID_NO = ? ";
	whereStr  = " WHERE ID_P_SEQNO = ? ";
	setString(1, newIdno);
	setString(2, idPSeqno);
	
	daoTable = CRD_IDNO_SEQNO;
	
	int returnInt = updateTable();
    if (returnInt == 0) {
      log(String.format("%s[id_p_seqno = %s]", "Fail to update CRD_IDNO_SEQNO", idPSeqno)) ;
      return false;
    }
	
	return true;
}

private boolean insertChgId(String tableName, ChgInfoObj chgInfoObj, IdnoObj idnoObj) throws Exception {
	
	if (CRD_CHG_ID.equals(tableName)) {
		extendField = "CRD_CHG_ID.";
		setValue("CRD_CHG_ID.OLD_ID_NO", chgInfoObj.id);
	    setValue("CRD_CHG_ID.OLD_ID_NO_CODE", "0");
	    setValue("CRD_CHG_ID.ID_P_SEQNO", idnoObj.idPSeqno);
	    setValue("CRD_CHG_ID.OLD_ID_P_SEQNO", idnoObj.idPSeqno);
	    setValue("CRD_CHG_ID.ID_NO", chgInfoObj.chgContain);
	    setValue("CRD_CHG_ID.ID_NO_CODE", "0");
	    setValue("CRD_CHG_ID.POST_JCIC_FLAG", "Y");
	    setValue("CRD_CHG_ID.CHI_NAME", idnoObj.chiName);
	    setValue("CRD_CHG_ID.CHG_DATE", sysDate);
	    setValue("CRD_CHG_ID.CRT_DATE", sysDate);
	    setValue("CRD_CHG_ID.CRT_USER", prgmId);
	    setValue("CRD_CHG_ID.APR_DATE", sysDate);
	    setValue("CRD_CHG_ID.APR_USER", prgmId);
	    setValue("CRD_CHG_ID.SRC_FROM", "2");
	    setValue("CRD_CHG_ID.MOD_USER", prgmId);
	    setValue("CRD_CHG_ID.MOD_TIME", sysDate + sysTime);
	    setValue("CRD_CHG_ID.MOD_PGM", prgmId);
	}else {
		extendField = "DBC_CHG_ID.";
	    setValue("DBC_CHG_ID.ID", chgInfoObj.id);
	    setValue("DBC_CHG_ID.ID_CODE", "0");
	    setValue("DBC_CHG_ID.CORP_FLAG", "N");
	    setValue("DBC_CHG_ID.AFT_ID", chgInfoObj.chgContain);
	    setValue("DBC_CHG_ID.AFT_ID_CODE", "0");
	    setValue("DBC_CHG_ID.CRT_DATE", sysDate);
	    setValue("DBC_CHG_ID.PROCESS_FLAG", "Y");
	    setValue("DBC_CHG_ID.MOD_USER", prgmId);
	    setValue("DBC_CHG_ID.MOD_TIME", sysDate + sysTime);
	    setValue("DBC_CHG_ID.MOD_PGM", prgmId);
	}

    daoTable = tableName;
    insertTable();
    
    return true;
}

private boolean updateChgId(String tableName, ChgInfoObj chgInfoObj, IdnoObj idnoObj) throws Exception {
	updateSQL = " ID_NO = ? , ID_P_SEQNO = ? , OLD_ID_P_SEQNO = ? , CHG_DATE = ? , APR_USER = ? , "
			+   " APR_DATE = ? , SRC_FROM = ? , MOD_USER = ? , MOD_TIME = sysdate , MOD_PGM = ? ";
	whereStr  = " WHERE OLD_ID_NO = ? ";
	setString(1, chgInfoObj.chgContain);
	setString(2, idnoObj.idPSeqno);
	setString(3, idnoObj.idPSeqno);
	setString(4, sysDate);
	setString(5, prgmId);
	setString(6, sysDate);
	setString(7, "2");
	setString(8, prgmId);
	setString(9, prgmId);
	setString(10, chgInfoObj.id);
	
	daoTable = tableName;
	
	int returnInt = updateTable();
    if (returnInt == 0) {
      log(String.format("%s[old_id_no = %s]", "Fail to update " + tableName, chgInfoObj.id)) ;
      return false;
    }
	
	return true;
}

private boolean doesHasChgId(String tableName, String oldIdNo) throws Exception {
	selectSQL = " OLD_ID_NO ";
    daoTable = tableName;
    whereStr = " where OLD_ID_NO = ? ";

    setString(1, oldIdNo);

    int selectCnt = selectTable();
    
    if (selectCnt > 0 ) {
    	return true;
	}

    return false;
}
/**
* @ClassName: EtbA002
* @Description: updateAcno 異動帳戶資料檔的帳戶查詢碼時增加綁定限一般卡(商務卡不需異動)
* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
* @Company: DXC Team.
* @author Wilson
* @version V1.00.06, Sep 13, 2022
*/
/**
* @ClassName: EtbA002
* @Description: updateAcno 找不到資料繼續往下執行
* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
* @Company: DXC Team.
* @author Wilson
* @version V1.00.07, Oct 05, 2022
*/
private boolean updateAcno(String tableName, String newIdno, String idPSeqno) throws Exception {
	if (ACT_ACNO.equals(tableName)) {
		updateSQL = " ACCT_KEY = ? , APR_USER = ? , APR_DATE = ? , MOD_USER = ? , MOD_TIME = sysdate , MOD_PGM = ? ";
		whereStr  = " WHERE ID_P_SEQNO = ? and card_indicator = '1' ";
		setString(1, newIdno + "0");
		setString(2, prgmId);
		setString(3, sysDate);
		setString(4, prgmId);
		setString(5, prgmId);
		setString(6, idPSeqno);
	}else {
		updateSQL = " ACCT_KEY = ? , ACCT_HOLDER_ID = ? , MOD_USER = ? , MOD_TIME = sysdate , MOD_PGM = ? ";
		whereStr  = " WHERE ID_P_SEQNO = ? ";
		setString(1, newIdno + "0");
		setString(2, newIdno);
		setString(3, prgmId);
		setString(4, prgmId);
		setString(5, idPSeqno);
	}

	daoTable = tableName;
	
	int returnInt = updateTable();
    if (returnInt == 0) {
      log(String.format("%s[id_p_seqno = %s]", "Fail to update " + tableName, idPSeqno)) ;
//      return false;
    }
	
	return true;
}

private boolean updateIdno(String tableName, String newIdno, String idPSeqno) throws Exception {
	updateSQL = " ID_NO = ? , MOD_USER = ? , MOD_TIME = sysdate , MOD_PGM = ? ";
	whereStr  = " WHERE ID_P_SEQNO = ? ";
	setString(1, newIdno);
	setString(2, prgmId);
	setString(3, prgmId);
	setString(4, idPSeqno);
	
	daoTable = tableName;
	
	int returnInt = updateTable();
    if (returnInt == 0) {
      log(String.format("%s[id_p_seqno = %s]", "Fail to update " + tableName, idPSeqno)) ;
      return false;
    }
	
	return true;
}

private void deleteCrdIdnoSeqno(boolean isDebit, String idPSeqno) throws Exception {
	daoTable = CRD_IDNO_SEQNO;
	whereStr = "where id_p_seqno = ? AND DEBIT_IDNO_FLAG = ? ";
	setString(1, idPSeqno);
	setString(2, isDebit ? "Y" : "N");
	deleteTable();
}

private void deleteIdno(String tableName, String idPSeqno) throws Exception {
	daoTable = tableName;
	whereStr = "where id_p_seqno = ? ";
	setString(1, idPSeqno);
	deleteTable();
}

private boolean doesHasCard(String tableName, String idPSeqno) throws Exception {
	selectSQL = " id_p_seqno ";
    daoTable = tableName;
    whereStr = " where id_p_seqno = ? ";

    setString(1, idPSeqno);

    int selectCnt = selectTable();
    
    if (selectCnt > 0 ) {
    	return true;
	}

    return false;
}

private IdnoObj getIdnoObj(String tableName, String idNo) throws Exception {
	IdnoObj idnoObj = null;
	selectSQL = " ID_P_SEQNO, CHI_NAME ";
    daoTable = tableName;
    whereStr = " where id_no = ? ";

    setString(1, idNo);

    int selectCnt = selectTable();
    
    if (selectCnt > 0 ) {
    	idnoObj = new IdnoObj();
        idnoObj.idPSeqno = getValue("ID_P_SEQNO");
        idnoObj.chiName = getValue("CHI_NAME");
	}

    return idnoObj;
}

private void moveTxtToBackup(String filePath, String fileName) throws IOException, Exception {
	// media/crd/backup
	Path backupFileFolderPath = Paths.get(comc.getECSHOME(), FILE_PATH, "backup");
	// create the parent directory if parent the directory is not exist
	Files.createDirectories(backupFileFolderPath);
	// get output file path
	String backupFilePath = Paths.get(backupFileFolderPath.toString(), fileName + "." + sysDate + sysTime).toString();
	
	moveFile(filePath, backupFilePath);
}
  
  private void procFTP(String fileName, String fileFolder) throws Exception {
	  CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
	  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
	  
	  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
      commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
      commFTP.hEriaLocalDir = fileFolder;
      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEflgModPgm = javaProgram;     

      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
      showLogMessage("I", "", "mput " + fileName + " 開始傳送....");
      int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fileName);
      
      if (errCode != 0) {
          showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料"+" errcode:"+errCode);
          insertEcsNotifyLog(fileName, comr, commFTP);          
      }
  }
  
	private int insertEcsNotifyLog(String fileName, CommRoutine comr, CommFTP commFTP) throws Exception {
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
	
	private void moveFile(String srcFilePath, String targetFilePath) throws Exception {
		
		if (comc.fileMove(srcFilePath, targetFilePath) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + srcFilePath + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + srcFilePath + "] 已移至 [" + targetFilePath + "]");
	}

	/****************************************************************************/

  private void outputNoChgReport(List<NoChangeReport> noChgReportList, String outputFilePath) throws Exception {

	int outFileIndex = openBinaryOutput2(outputFilePath);

	writeFile(noChgReportList, outFileIndex);

	showLogMessage("I", "", String.format("產出未更新報表檔: %s", outputFilePath));

	closeBinaryOutput2(outFileIndex);
	
  }

private void writeFile(List<NoChangeReport> noChgReportList, int outFileIndex) throws Exception {
	int size = noChgReportList.size();
	for (int i = 0; i < size; i++) {
		NoChangeReport noChangeReport = noChgReportList.get(i);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgKey, 10);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgDate, 7);
		writeFileInCertainLength(outFileIndex, noChangeReport.crdCardTransTime, 6);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgCode, 2);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgContain, 110);
		writeFileInCertainLength(outFileIndex, noChangeReport.crdCardAddrChgFlag, 1);
		writeFileInCertainLength(outFileIndex, noChangeReport.chgCrdCardFlag, 1);
		writeFileInCertainLength(outFileIndex, noChangeReport.deadFlag, 1);
		writeFileInCertainLength(outFileIndex, noChangeReport.noChgReason, 200);
		writeFileInCertainLength(outFileIndex, System.lineSeparator(), lineSeparatorBytes.length);
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

    fileFolderPath = Paths.get(projectPath).toString();

    for (int i = 0; i < arrFilePathFromDb.length; i++) {
      fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();
    }

    return Paths.get(fileFolderPath, fileNameAndTxt).toString();
  }

  private ChgInfoData getChangingInfoObjFromText(String text) throws UnsupportedEncodingException {
    return splitBytesArr(text.getBytes("MS950"));
  }


  /**
   * separate columns from the byte array of the given txt file .
   * 
   * @param bytesArr : the byte array of the given txt file
   * @return
   */
  private ChgInfoData splitBytesArr(byte[] bytesArr) {
	ChgInfoData changingInfoFile = new ChgInfoData();

    changingInfoFile.outDate = CommTxBill.subByteToStr(bytesArr, 0, 7);
    changingInfoFile.outCikeyOld = CommTxBill.subByteToStr(bytesArr, 8, 18);
    changingInfoFile.outCikeyNew = CommTxBill.subByteToStr(bytesArr, 19, 29);
    changingInfoFile.outNoAccount = CommTxBill.subByteToStr(bytesArr, 30, 43);
    changingInfoFile.outTime = CommTxBill.subByteToStr(bytesArr, 44, 50);

    return changingInfoFile;
  }

  public static void main(String[] args) {
    EtbA002 proc = new EtbA002();
    int retCode = proc.mainProcess(args);
    System.exit(retCode);
  }

  class ChgInfoData {
	String outDate = ""; // 日期
	String outCikeyOld = ""; // ID變前
	String outCikeyNew = ""; // ID變後
	String outNoAccount = ""; // 帳號
	String outTime = ""; // 時間
  }
  
  class ChgInfoObj {
	    String id = ""; // 身份證號 or 變更key
	    String fil1 = ""; // 空白
	    String date = ""; // 變更日期 民國年YYYMMDD
	    String fil2 = ""; // 空白
	    String chgCode = ""; // 變更代號
	    String fil3 = ""; // 空白
	    String chgContain = ""; // 變更後內容
	    String crdCardAddrChgFlag = ""; // 信用卡地址變更註記 Y:需一併變更信用卡地址 N:不需變更信用卡地址
	    String fundSystemChgFlag = ""; // 基金系統變更註記
	    String opBrh = ""; // 支庫代號
	    String opId = ""; // 櫃員代碼
	    String chgCrdCardFlag = ""; // 更換信用卡註記
	    String deadFlag = ""; // 繼承事故停用信用卡註記 已無使用
	    String fil5 = ""; // 空白
//	    String countCnt = ""; // 筆數
	    String time = ""; // 信用卡交易時間 hhmmss
//	    String fil6 = ""; // 空白
	    
	    String rowId = "";
	    boolean isNoChg = false;
	    ErrorReason noChgReason = null;
	    
		public void setNoChgReason(ErrorReason nochgreason8) {
			this.isNoChg = true;
			this.noChgReason = nochgreason8;
		}
		
	}
  class IdnoObj{
	  String idPSeqno = "";
	  String chiName = "";
  }
  
  class NoChangeReport{
	  String chgKey; //	  變更KEY
	  String chgDate; //	  變更日期
	  String crdCardTransTime; //	  信用卡交易時間
	  String chgCode; //	  變更代號
	  String chgContain; //	  變更後內容
	  String crdCardAddrChgFlag; //	  信用卡地址變更註記
	  String chgCrdCardFlag; //	  更換信用卡註記
	  String deadFlag; //	  繼承事故告知信用卡停卡
	  String noChgReason; //	  未更新原因
	  
	  NoChangeReport(String noChgReason, ChgInfoObj chgInfoObj){
		  this.chgKey = chgInfoObj.id;
		  this.chgDate = chgInfoObj.date;
		  this.crdCardTransTime = chgInfoObj.time;
		  this.chgCode = chgInfoObj.chgCode;
		  this.chgContain = chgInfoObj.chgContain;
		  this.crdCardAddrChgFlag = chgInfoObj.crdCardAddrChgFlag;
		  this.chgCrdCardFlag = chgInfoObj.chgCrdCardFlag;
		  this.deadFlag = chgInfoObj.deadFlag;
		  this.noChgReason = noChgReason;
	  }
  }
  
  enum ErrorReason{
	  
	  noChgReason1("此ID不存在卡人資料檔", "B1"),
	  noChgReason2("此ID已存在且向下有卡片，不可修改", "B2"),
	  noChgReason3("ID變後不為10碼", "B3"),
	  ;
	  
	  String reason = "";
	  String code = "";
	  private ErrorReason(String reason, String code) {
		  this.reason = reason;
		  this.code = code;
	  }
	  
	  public String getReason() {
		  return reason;
	  }
	  
	  public String getCode() {
		  return code;
	  }
  }
  
}
