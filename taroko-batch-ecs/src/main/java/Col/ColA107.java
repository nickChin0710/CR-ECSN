/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE          Version     AUTHOR              DESCRIPTION              *
 *  ---------       --------- ----------- ------------------------------------*
 *  112/02/08        V1.00.00    Yang Bo    program initial            		  *    
 *  112/09/18        V1.00.01    Sunny      檔案不存在，不跳ERROR                 *
 *  112/10/04 		 V1.00.02    Sunny      增加參數all，區分轉檔時處理的檔名                  *
 *****************************************************************************/
package Col;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ColA107 extends AccessDAO {

    private final String PROGNAME = "前置協商免列報(繳款達三期以上)處理程式 112/10/04  V1.00.02";
    private final String prgmId = "ColA107";
    private static final int COMMIT_CNT = 1000;
    private final boolean DO_DELETE = true;
    private final boolean DO_INSERT = false;
    private int insertCnt = 0;
    private int deleteCnt = 0;
    private int unknownCnt = 0;

    CommCrdRoutine comcr = null;
    CommCrd comc = new CommCrd();
    String tempUser = "";

    private final String FILE_PATH = "media/col";
    private final String FILE_NAME = "Predue3.txt";
    private final String FILE_NAME2 = "Predue3_ALL.txt";


    public int mainProcess(String[] args) {
        String callBatchSeqno = "";

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);

            // =====================================


            /* Get all the ID_NO from COL_COLLECT_FLAGX */
            Map<String, Boolean> id2DeleteMap = getIdSetFromColCollectFlagx();

            /* Get the fileFolderPath such as C:\EcsWeb\media\col */
            String fileName = FILE_NAME;
            
			//如果參數1為all，則令檔名為FILE_NAME2的值
			if ((args.length >= 1) && (args[0].equals("all"))) {				
				fileName = FILE_NAME2;
			}
			
            String filePath = getFilePath(comc.getECSHOME(), FILE_PATH, fileName);

            /** 讀取前置協商免列報通知檔之ID並對COL_COLLECT_FLAGX做相對應處理 **/
            int inputFile = openInputText(filePath, "MS950");
            if (inputFile == -1) {
            	exceptExit = 0;
                comc.errExit(String.format("檔案不存在: %s", filePath), "");
            } else {
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
                        } else {
                            /* CASE2:
                             * If the map does not contain the id, put this id into the map and set its
                             * value as DO_INSERT, meaning that it should be inserted into COL_COLLECT_FLAGX.
                             */
                            id2DeleteMap.put(id, DO_INSERT);
                        }
                        /* CASE3:
                         * Note that if an ID exists in COL_COLLECT_FLAGX but not in the file, its value
                         * in the map is DO_DELETE, meaning that it should be removed from COL_COLLECT_FLAGX
                         */
                    }

                    if (endFile[inputFile].equals("Y")) {
                        closeInputText(inputFile);
                        break;
                    }
                }

                /* 對COL_COLLECT_FLAGX做相對應處理  */
                updateColCollectFlagx(id2DeleteMap);

                /** 搬檔到BACKUP並變更檔名 **/
                moveTxtToBackup(filePath, fileName);
            }

            showLogMessage("I", "", String.format("ID異動筆數[%d]筆：其中新增[%d]筆、刪除[%d]筆", insertCnt + deleteCnt, insertCnt, deleteCnt));
            if (unknownCnt > 0) {
                showLogMessage("W", "", String.format("未知ID異動筆數[%d]筆", unknownCnt));
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

    private Map<String, Boolean> getIdSetFromColCollectFlagx() throws Exception {
        Map<String, Boolean> id2DeleteMap = new HashMap<>();
        sqlCmd = " SELECT ID_NO FROM COL_COLLECT_FLAGX WHERE FROM_MARK='LIAC' ";
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            /* 預設為要刪除 */
            id2DeleteMap.put(getValue("ID_NO"), DO_DELETE);
        }
        closeCursor(cursorIndex);
        return id2DeleteMap;
    }

    private void updateColCollectFlagx(Map<String, Boolean> id2DeleteMap) throws Exception {
        int updateCnt = 0;
        for (String id : id2DeleteMap.keySet()) {
            boolean action = id2DeleteMap.get(id);
            if (DO_DELETE == action) {
                deleteColCollectFlagx(id);
                deleteCnt++;
                updateCnt++;
                showLogMessage("I", "", String.format("刪除[%s]", id));
            } else if (DO_INSERT == action) {
                insertColCollectFlagx(id);
                insertCnt++;
                updateCnt++;
                showLogMessage("I", "", String.format("新增[%s]", id));
            } else {
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

    private boolean insertColCollectFlagx(String id) throws Exception {

        extendField = "COL_COLLECT_FLAGX.";
        setValue("COL_COLLECT_FLAGX.ID_NO", id);
        setValue("COL_COLLECT_FLAGX.FROM_MARK", "LIAC");
        setValue("COL_COLLECT_FLAGX.MOD_TIME", sysDate + sysTime);
        setValue("COL_COLLECT_FLAGX.MOD_PGM", prgmId);
        daoTable = "COL_COLLECT_FLAGX";
        insertTable();
        return true;
    }


    private void deleteColCollectFlagx(String id) throws Exception {
        daoTable = "COL_COLLECT_FLAGX";
        whereStr = "where ID_NO = ? " +
                " and FROM_MARK = 'LIAC' ";
        setString(1, id);
        deleteTable();
    }


    private void moveTxtToBackup(String filePath, String fileName) throws Exception {
        // media/mkt/backup
        Path backupFileFolderPath = Paths.get(comc.getECSHOME(), FILE_PATH, "backup");
        // create the parent directory if parent the directory is not exist
        Files.createDirectories(backupFileFolderPath);
        // get output file path
        String backupFilePath = Paths.get(backupFileFolderPath.toString(), fileName + "." + sysDate + sysTime)
                .toString();

        moveFile(filePath, backupFilePath);
    }


    private void moveFile(String srcFilePath, String targetFilePath) {

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
        String fileFolderPath;

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
        ColA107 proc = new ColA107();
        int retCode = proc.mainProcess(args);
        System.exit(retCode);
    }
}
