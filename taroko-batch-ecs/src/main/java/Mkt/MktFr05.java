/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                DESCRIPTION                  *
 *  ---------  --------- ----------- ---------------------------------------- *
 * 112-06-27    V1.00.00   Bo Yang        program  initial                    *
 * 112-07-25    V1.00.01   Bo Yang        log update                          *
 *****************************************************************************/
package Mkt;


import Dxc.Util.SecurityUtil;
import com.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.Normalizer;

public class MktFr05 extends AccessDAO {
    private final String PROGNAME = "接收AP4傳送信託部提供之安養信託名單處理程式 112/07/25 V1.00.00";
    CommString commString = new CommString();
    CommCrd comc = new CommCrd();
    CommRoutine comr = null;
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;
    CommDate commDate = new CommDate();
    private static final String FOLDER = "/media/mkt";
    private static final String FILE_NAME = "CR_TRUST.TXT";
    private String procDate = "";
    private String dataMonth = "";
    private String idNo = "";

    private Integer totCnt = 0;

    //****************************************************************************
    public static void main(String[] args) {
        MktFr05 proc = new MktFr05();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    //****************************************************************************
    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            // 固定要做的
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            commFTP = new CommFTP(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            procDate = sysDate;
            String parm = "";
            if (args.length == 1) {
                parm = args[0];
                if (parm.length() != 8 || !commDate.isDate(parm)) {
                    showLogMessage("I", "", "請傳入參數合格值YYYYMMDD: [" + parm + "]");
                    return 1;
                }
                procDate = parm;
            }

            if (!"01".equals(commString.right(procDate, 2))) {
                showLogMessage("I", "", "非每月01日，程式不執行[" + procDate + "]");
                return 0;
            }

            showLogMessage("I", "", "傳入參數日期 = [" + parm + "]");
            showLogMessage("I", "", "取得處理日 =  [" + procDate + "]");

            String fileName = FILE_NAME;
            
            int rtnCode = 0;
            rtnCode = openFile(fileName);
            if (rtnCode  == 0) {
            	rtnCode = readFile(fileName);
                renameFile(fileName);
            }
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            comcr.callbatchEnd();
            finalProcess();
            return rtnCode ;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    //=============================================================================
    int openFile(String fileName) {
        String path = String.format("%s%s/%s", comc.getECSHOME(), FOLDER, fileName);
        path = Normalizer.normalize(path, Normalizer.Form.NFKD);

        int rec = openInputText(path);
        if (rec == -1) {
            showLogMessage("I", "", " CR_TRUST.TXT 資料內容空檔,連繫ap4業務窗口傳檔");
            return 7;
        }

        closeInputText(rec);
        return (0);
    }

    //=============================================================================
    int readFile(String fileName) throws Exception {
        showLogMessage("I", "", "==== Start Read File ====");
        BufferedReader bufferedReader;
        try {
            String tmpStr = String.format("%s%s/%s", comc.getECSHOME(), FOLDER, fileName);
            String tempPath = SecurityUtil.verifyPath(tmpStr);
            FileInputStream fileInputStream = new FileInputStream(tempPath);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "MS950"));

            showLogMessage("I", "", "   tempPath = [" + tempPath + "]");
        } catch (FileNotFoundException exception) {
            showLogMessage("I", "", "bufferedReader exception: " + exception.getMessage());
            return 7;
        }

        dataMonth = commDate.monthAdd(procDate, -1);
        String lineLength;
        while ((lineLength = bufferedReader.readLine()) != null) {
            if (lineLength.length() < 10) {
                continue;
            }
            totCnt++;
            byte[] bytes = lineLength.getBytes("MS950");
            idNo = comc.subMS950String(bytes, 0, 10).trim();
            if(idNo.length() == 0) {
            	continue;
            }
            if (totCnt == 1) {
                deleteMktAddonIdlist();
            }
            insertMktAddonIdlist();
        }

        if (totCnt == 0) {
            showLogMessage("I", "", " CR_TRUST.TXT 資料內容空檔,連繫ap4業務窗口傳檔");
            return 7;
        }
        
        bufferedReader.close();
        return 0 ;
    }

    //=============================================================================
    void deleteMktAddonIdlist() throws Exception {
        daoTable = "MKT_ADDON_IDLIST";
        whereStr = " WHERE DATA_MONTH = ? ";
        whereStr += "  AND LIST_TYPE = '0101' ";
        setString(1, dataMonth);

        if (deleteTable() != 0) {
            showLogMessage("I", "", "delete mkt_addon_idlist DATA_MONTH = [" + dataMonth + "] ");
        }
    }

    //=============================================================================
    void insertMktAddonIdlist() throws Exception {
        setValue("DATA_MONTH", dataMonth);
        setValue("LIST_TYPE", "0101");
        setValue("ID_NO", idNo);
        setValue("ACCT_NO", "");
        setValue("TRUST_NO", "");
        setValue("MEMO", "");
        setValue("RCV_DATE", sysDate);
        setValue("MOD_USER", javaProgram);
        setValue("MOD_TIME", sysDate + sysTime);
        setValue("MOD_PGM", javaProgram);
        daoTable = "MKT_ADDON_IDLIST";

        insertTable();
        if ("Y".equals(dupRecord)) {
            showLogMessage("I", "", "insert mkt_addon_idlist dupRecord DATA_MONTH = [" + dataMonth + "] ,ID_NO = [" + idNo + "]");
        }
    }

    //=============================================================================
    void renameFile(String fileName) throws Exception {
        String tmpStr1 = String.format("%s%s/%s", comc.getECSHOME(), FOLDER, fileName);
        String tempPath1 = SecurityUtil.verifyPath(tmpStr1);
        String tmpStr2 = String.format("%s%s/backup/%s", comc.getECSHOME(), FOLDER, fileName + "_" + sysDate + sysTime);
        String tempPath2 = SecurityUtil.verifyPath(tmpStr2);

        if (!comc.fileCopy(tempPath1, tempPath2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]備份失敗!");
            return;
        }
        comc.fileDelete(tempPath1);
        showLogMessage("I", "", "檔案 [" + fileName + "] 備份至 [" + tempPath2 + "]");
    }
}
