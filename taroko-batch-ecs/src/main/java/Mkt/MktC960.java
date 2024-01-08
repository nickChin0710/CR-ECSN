/*****************************************************************************************************
 *                                                                                                   *
 *                              MODIFICATION LOG                                                     *
 *                                                                                                   *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                      *
 *  ---------  --------- ----------- --------------------------------------------------------------  *
 *  112/07/24  V1.00.00    Zuwei Su                 program initial                                   *
 *  112/10/16  V1.00.01    Rayn                     系統日改為營業日,修改檔案路徑                                                                                 *
 *  112/12/19  V1.00.02  Zuwei Su    errRtn改為 show message & return 1  *  
 *  113/01/04  V1.00.03  Zuwei Su    method mainProcess增加參數  *  
 *****************************************************************************************************/
package Mkt;

import com.*;

import java.text.Normalizer;

public class MktC960 extends AccessDAO {
    private final String PROGNAME = "icash 愛金卡點數檔(A16B) 接收月彙整處理 112/10/16";
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommDate commDate = new CommDate();
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;

    private static final String OUTFILE = "Icash_A16B_YYYYMM.TXT";
    //private static final String PATH_FOLDER = "/reports/";
    private static final String PATH_FOLDER = "/media/mkt/";
    private final static String LINE_SEPARATOR = System.lineSeparator();

    private int totCnt = 0;
    private int fptr1 = -1;
    private String busiDate = "";

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            // 固定要做的
            if (!connectDataBase()) {
//                comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error" );
                return 0;
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            busiDate = getBusiDate();
            showLogMessage("I", "", String.format("今日營業日[%s]", busiDate));
            
            selectMktOpenpointData();
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    private void selectMktOpenpointData() throws Exception {
        //sqlCmd += " Select b.card_no ";
        sqlCmd += " Select distinct b.card_no ";
        sqlCmd += " From mkt_openpoint_data a, ich_card b, crd_card c ";
        sqlCmd += " Where substr(a.Tx_date,1,6) = ? ";
        sqlCmd += " And a.Icash_cardno=b.ich_card_no ";
        sqlCmd += " And b.card_no = c.card_no ";
        sqlCmd += " And a.tx_type = '21' ";		//--交易類別=21購貨

        String lastMonth = commDate.monthAdd(busiDate, -1);
        setString(1, lastMonth);
        StringBuilder sb = new StringBuilder();
        openCursor();
        while (fetchTable()) {
            sb.append(getValue("card_no")).append(LINE_SEPARATOR);
            totCnt++;
        }
        closeCursor();
        showLogMessage("I", "", String.format("Process records = [%d]\n", totCnt));

        // 寫入檔案
        writeReport(sb.toString());
    }

    private void writeReport(String conent) throws Exception {
        // 依據不同的產檔格式, 分別產出檔案及傳送至遠端
        //String filename = OUTFILE.replace("YYYYMM", sysDate.substring(0, 6));		//系統年月
    	String lastMonth = commDate.monthAdd(busiDate, -1);			//上個月
    	String filename = OUTFILE.replace("YYYYMM", lastMonth);
        fileOpen(filename);
        showLogMessage("I", "", "開始寫入檔案: " + filename);
        writeTextFile(fptr1, conent);
        closeOutputText(fptr1);
        ftpProc(filename);
    }

    /*******************************************************************/
    private void fileOpen(String filename) throws Exception {
        String tempStr1 = String.format("%s%s%s", comc.getECSHOME(), PATH_FOLDER, filename);
        String fileName = Normalizer.normalize(tempStr1, java.text.Normalizer.Form.NFKD);
        fptr1 = openOutputText(fileName, "MS950");

        if (fptr1 == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
        }
    }

    // ************************************************************************
    private void ftpProc(String filename) throws Exception {
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "TOHOST"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME() + PATH_FOLDER;    //相關目錄皆同步
        commFTP.hEflgModPgm = javaProgram;
        commFTP.hEriaRemoteDir = "/media/mkt";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        showLogMessage("I", "", "put %s " + filename + " 開始上傳....");

        String tmpChar = "put " + filename;

        int errCode = commFTP.ftplogName("NCR2EMP", tmpChar);

        if (errCode != 0) {
            showLogMessage("I", "", "檔案傳送 NCR2EMP 有誤(error), 請通知相關人員處理");
            showLogMessage("I", "", "MktC960 執行完成 傳送EMP失敗[" + filename + "]");
            commFTP.insertEcsNotifyLog(filename, "3", javaProgram, sysDate, sysTime);
            return;
        }

        showLogMessage("I", "", "FTP完成.....");

        // 刪除檔案 put 不用刪除
        renameFile(filename);
    }

    // ************************************************************************
    public void renameFile(String removeFileName) throws Exception {
        String tmpStr1 = comc.getECSHOME() + PATH_FOLDER + removeFileName;
        //String tmpStr2 = comc.getECSHOME() + PATH_FOLDER + removeFileName + "." + sysDate;
        String tmpStr2 = comc.getECSHOME() + PATH_FOLDER + removeFileName + "." + sysDate+sysTime;

        if (!comc.fileRename2(tmpStr1, tmpStr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpStr2 + "]");
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC960 proc = new MktC960();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
