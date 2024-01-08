/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE          Version    AUTHOR              DESCRIPTION               *
*  ---------       --------- ----------- ------------------------------------*
*  111/06/22        V1.00.00    JustinWu           program initial           *    
*****************************************************************************/
package Mkt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommTxBill;

public class MktW001 extends AccessDAO {
  
private final String PROGNAME = "接收主機下傳網銀使用名單檔之處理 111/06/22 V1.00.00";
  private final String prgmId = "MktW001";
  private static final int COMMIT_CNT = 1000;
  private final boolean DO_DELETE = true;
  private final boolean DO_INSERT = false;
  private int insertCnt  = 0;
  private int deleteCnt  = 0;
  private int unknownCnt = 0;
  
  CommCrdRoutine comcr = null;
  CommCrd comc = new CommCrd();
  CommDate commDate = new CommDate();
  String tempUser = "";

  private final String FILE_PATH = "media/mkt";
  private final String FILE_NAME = "NEQUSER.TXT";
 

  private int mainProcess(String[] args) {
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
      
      
      /* Get all the WEB_CUST_IDs from MKT_WEB_CUSTOMER */
      Map<String, Boolean> id2DeleteMap = getIdSetFromMktWebCustomer();
      
      /* Get the fileFolderPath such as C:\EcsWeb\media\mkt */
      String fileName = FILE_NAME;
      String filePath = getFilePath(comc.getECSHOME(), FILE_PATH, fileName);

      /** 讀取傳網銀使用名單檔之ID並對MKT_WEB_CUSTOMER做相對應處理 **/
      int inputFile = openInputText(filePath, "MS950");
      if (inputFile == -1) {
        showLogMessage("I", "", String.format("檔案不存在: %s", filePath));
      }else {
          while (true) {
	  
            String text = readTextFile(inputFile);
            if (text.trim().length() != 0) {
            	// 從檔案取得ID
    			String id = getId(text);
    			id = id.trim();
    			if (id2DeleteMap.containsKey(id)) {
					/* CASE1: 
					 * If the map contains the id, remove this id from the map, meaning that
					 * we do not need to do anything.
					 */
    				id2DeleteMap.remove(id);
    			}else {
					/* CASE2: 
					 * If the map does not contain the id, put this id into the map and set its
					 * value as DO_INSERT, meaning that it should be inserted into MKT_WEB_CUSTOMER.
					 */
    				id2DeleteMap.put(id, DO_INSERT);
    			}
				/* CASE3: 
				 * Note that if an ID exists in MKT_WEB_CUSTOMER but not in the file, its value
				 * in the map is DO_DELETE, meaning that it should be removed from MKT_WEB_CUSTOMER
				 */
            }

            if (endFile[inputFile].equals("Y")) {
            	closeInputText(inputFile);
            	break;
            }
            
          }
          
          /* 對MKT_WEB_CUSTOMER做相對應處理  */
          updateMktWebCustomer(id2DeleteMap);
          
          /** 搬檔到BACKUP並變更檔名 **/
          moveTxtToBackup(filePath, fileName);
      }

      showLogMessage("I", "", String.format("ID異動筆數[%d]筆：其中新增[%d]筆、刪除[%d]筆", insertCnt + deleteCnt, insertCnt, deleteCnt));
      if (unknownCnt > 0) showLogMessage("W", "", String.format("未知ID異動筆數[%d]筆", unknownCnt));
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

	private Map<String, Boolean> getIdSetFromMktWebCustomer() throws Exception {
		Map<String, Boolean> id2DeleteMap = new HashMap<>();
		sqlCmd = " SELECT WEB_CUST_ID FROM MKT_WEB_CUSTOMER ";
		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {
			/* 預設為要刪除 */
			id2DeleteMap.put(getValue("WEB_CUST_ID"), DO_DELETE);
		}
		closeCursor(cursorIndex);
		return id2DeleteMap;
	}
	
	private void updateMktWebCustomer(Map<String, Boolean> id2DeleteMap) throws Exception {
		int updateCnt = 0;
		for ( String id : id2DeleteMap.keySet()) {
			boolean action = id2DeleteMap.get(id);
			if (DO_DELETE == action) {
				deleteMktWebCustomer(id);
				deleteCnt++;
				updateCnt++;
				showLogMessage("I", "", String.format("刪除[%s]", id));
			}else if (DO_INSERT == action) {
				insertMktWebCustomer(id);
				insertCnt++;
				updateCnt++;
			}else {
				unknownCnt++;
				showLogMessage("I", "", String.format("未知處理動作[%s]", id));
			}
			
			if (updateCnt % COMMIT_CNT == 0) {
				commitDataBase();
				showLogMessage("I", "", String.format("Commit %d ~ %d 筆資料", updateCnt - COMMIT_CNT + 1, updateCnt));
			}
		}
		
		if (updateCnt > 0 && updateCnt % COMMIT_CNT > 0) {
			commitDataBase();
			showLogMessage("I", "", String.format("Commit %d ~ %d 筆資料", updateCnt - (updateCnt % COMMIT_CNT) + 1, updateCnt));
		}

	}

	private boolean insertMktWebCustomer(String id) throws Exception {

		extendField = "MKT_WEB_CUSTOMER.";
		setValue("MKT_WEB_CUSTOMER.WEB_CUST_ID", id);
		setValue("MKT_WEB_CUSTOMER.MOD_TIME", sysDate + sysTime);
		setValue("MKT_WEB_CUSTOMER.MOD_PGM", prgmId);
		daoTable = "MKT_WEB_CUSTOMER";
		insertTable();
		return true;
	}


	private void deleteMktWebCustomer(String id) throws Exception {
		daoTable = "MKT_WEB_CUSTOMER";
		whereStr = "where WEB_CUST_ID = ? ";
		setString(1, id);
		deleteTable();
	}


	private void moveTxtToBackup(String filePath, String fileName) throws IOException, Exception {
		// media/mkt/backup
		Path backupFileFolderPath = Paths.get(comc.getECSHOME(), FILE_PATH, "backup");
		// create the parent directory if parent the directory is not exist
		Files.createDirectories(backupFileFolderPath);
		// get output file path
		String backupFilePath = Paths.get(backupFileFolderPath.toString(), fileName + "." + sysDate + sysTime)
				.toString();

		moveFile(filePath, backupFilePath);
	}

	
	private void moveFile(String srcFilePath, String targetFilePath) throws Exception {
		
		if (comc.fileMove(srcFilePath, targetFilePath) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + srcFilePath + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + srcFilePath + "] 已移至 [" + targetFilePath + "]");
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
	private String getFilePath(String projectPath, String filePathFromDb, String fileNameAndTxt) throws Exception {
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

	private String getId(String text) throws UnsupportedEncodingException {
		return splitBytesArr(text.getBytes("MS950"));
	}


  /**
   * separate columns from the byte array of the given txt file .
   * 
   * @param bytesArr : the byte array of the given txt file
   * @return
   */
	private String splitBytesArr(byte[] bytesArr) {
		return CommTxBill.subByteToStr(bytesArr, 0, 10);
	}

	public static void main(String[] args) {
		MktW001 proc = new MktW001();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

  
}
