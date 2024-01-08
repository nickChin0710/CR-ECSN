/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/06/21  V1.00.00    Brian       program initial                         *
*  109-11-18  V1.00.01    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import java.io.UnsupportedEncodingException;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

//FTP import
import com.CommFTP;

/*從資訊處FTP Server收檔傳給SVCS處理程式*/
public class TscT005 extends AccessDAO {

    private String progname = "從資訊處FTP Server收檔傳給SVCS處理程式  109/11/18 V1.00.01";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    EcsFtpBuf ecsftp = new EcsFtpBuf();

    int    forceFlag           = 0;
    int    totalCnt            = 0;
    String hTempSysdate       = "";
    String hEflgTransSeqno   = "";
    String transSeqno          = "";
    String hBusiBusinessDate = "";
    String hCallBatchSeqno   = "";
    int    okFlag              = 0;
    String hEflgFileName     = "";
    String hEflgRefIpCode   = "";
    String hEflgSourceFrom   = "";
    String hEflgRowid         = "";
    String hEflgProcCode     = "";
    String hEflgProcDesc     = "";
  
    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : TscT005 [sysdate [R]]", "");
            }
            hTempSysdate = "";
            if (args.length >= 1)
                hTempSysdate = args[0];
            if (args.length == 2 && args[1].equals("R"))
                forceFlag = 1;

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            selectPtrBusinday();
            showLogMessage("I", "", String.format("本日系統日[%s]", hTempSysdate));

            showLogMessage("I", "", String.format("開始FTP匯入檔案....."));
            ftpMget();
            totalCnt = 0;
            selectEcsFtpLog();
            commitDataBase();

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", String.format("========================================="));
            showLogMessage("I", "", "\n程式執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /**************************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = " select business_date, ";
        sqlCmd += "        decode( cast(? as varchar(8)), '', to_char(sysdate,'yyyymmdd'), ?) h_temp_sysdate ";
        sqlCmd += "   from ptr_businday ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hTempSysdate);
        setString(2, hTempSysdate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSysdate = getValue("h_temp_sysdate");
        }

    }

    /*************************************************************************/
    void ftpMget() throws Exception {
        int inta = 0;
        String[] extStr = { "ACAE", "ACAN", "ACCB", "ACCG", "ACCS", "ACFI", "ACLC", "ACPF", "ACRI", "ACTI", "ACRT" };
        String fileName = "";

        // ======================================================
        // FTP

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        hEflgTransSeqno         = String.format("%010d", comcr.getModSeq());
        commFTP.hEflgTransSeqno = hEflgTransSeqno; /* 串聯 log 檔所使用 鍵值         (必要) */
        commFTP.hEflgSystemId   = "DW_FTP_TSCC";      /* 區分不同類的 FTP 檔案-大類     (必要) */
        commFTP.hEflgSourceFrom = "DW_FTP";           /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
        commFTP.hEflgModPgm     = this.getClass().getName();
        String hEflgRefIpCode  = "DW_FTP_TSCC_IN";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        for (inta = 0; inta < 11; inta++) {

            commFTP.hEflgGroupId = String.format("%02d", inta + 1); /* 區分不同類的 FTP 檔案-次分類 (非必要) */

            if (extStr[inta].equals("ACCS") || extStr[inta].equals("ACPF") || extStr[inta].equals("ACRI")
                    || extStr[inta].equals("ACTI")) {
                fileName = String.format("%s.%8.8s.%8.8s01", extStr[inta], comc.TSCC_BANK_ID8, hTempSysdate);
            } else {
                fileName = String.format("%s.%8.8s.%8.8s01", extStr[inta], comc.TSCC_BANK_ID8, hTempSysdate);
            }

            String procCode = String.format("mget %s*", fileName);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始接收....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

            if (errCode != 0) {
                showLogMessage("I", "", String.format("[%s] => [無法接收資料]", procCode));
    //          showLogMessage("I", "", String.format("     => msg_code[%s] return_code[%d]", ecsftp.msg_code, err_code));
    //          showLogMessage("I", "", String.format("     => ERROR_MSG[%s]", ecsftp.msg_desc));

            }
            if (inta == 0)
                okFlag = 1;
        }
        showLogMessage("I", "", String.format("FTP完成.....\n"));

    }
    // ==================================================

    /**********
     * COMM_FTP common function usage
     ****************************************/

/*****************************************************************************/
    void selectEcsFtpLog() throws Exception {

        sqlCmd = " SELECT  file_name, ";
        sqlCmd += "         ref_ip_code, ";
        sqlCmd += "         source_from, ";
        sqlCmd += "         rowid rowid ";
        sqlCmd += " FROM    ecs_ftp_log    ";
        sqlCmd += " WHERE   trans_seqno     = ? ";
        sqlCmd += " AND     system_id       = 'DW_FTP_TSCC' ";
        sqlCmd += " AND     trans_resp_code = 'Y' ";
        setString(1, hEflgTransSeqno);

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hEflgFileName = getValue("file_name");
            hEflgRefIpCode = getValue("ref_ip_code");
            hEflgSourceFrom = getValue("source_from");
            hEflgRowid = getValue("rowid");
            hEflgProcCode = "0";
            hEflgProcDesc = "";

            totalCnt++;
            showLogMessage("I", "", String.format("刪除檔案[%s].....", hEflgFileName));

            // ======================================================
            // FTP

            CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());

            commFTP.hEflgTransSeqno = String.format("%010d", comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
            commFTP.hEflgSystemId = "DW_FTP_TSCC"; /* 區分不同類的 FTP 檔案-大類 (必要) */
            commFTP.hEflgSourceFrom = hEflgSourceFrom; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir = String.format("%s/media/tsc", comc.getECSHOME());
            commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgModPgm = this.getClass().getName();

            System.setProperty("user.dir", commFTP.hEriaLocalDir);

            String procCode = String.format("delete %s", hEflgFileName);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

            if (errCode != 0) {
                showLogMessage("I", "", String.format("刪除檔案[%s]失敗.....", hEflgFileName));
                hEflgProcCode = "B";
                hEflgProcDesc = ecsftp.msgDesc;
                updateEcsFtpLog();
                continue;

            }

            totalCnt++;

            showLogMessage("I", "", String.format("刪除檔案[%s]完成.....", hEflgFileName));

            updateEcsFtpLog();
        }
        closeCursor(cursorIndex);
    }

    /*****************************************************************************/
    void updateEcsFtpLog() throws Exception {
        daoTable = "ecs_ftp_log";
        updateSQL  = " proc_code  = ?, ";
        updateSQL += " proc_desc  = ? , ";
        updateSQL += " mod_pgm    = 'TscT005', ";
        updateSQL += " mod_time   = sysdate ";
        whereStr = " where  rowid = ? ";
        setString(1, hEflgProcCode);
        setString(2, hEflgProcDesc);
        setRowId(3, hEflgRowid);

        if (notFound.equals("Y")) {
            comcr.errRtn("update_ecs_ftp_log not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT005 proc = new TscT005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class EcsFtpBuf {
        String errCode;
        String msgCode;
        String msgDesc;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(errCode, 2);
            rtn += comc.fixLeft(msgCode, 2);
            rtn += comc.fixLeft(msgDesc, 301);
            return rtn;
        }

        void splitSndData(String str) throws UnsupportedEncodingException {
            byte[] bytes = str.getBytes("MS950");
            errCode = comc.subMS950String(bytes, 0, 2);
            msgCode = comc.subMS950String(bytes, 2, 2);
            msgDesc = comc.subMS950String(bytes, 4, 301);
        }
    }
}
