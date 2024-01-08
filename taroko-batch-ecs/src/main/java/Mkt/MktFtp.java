/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/04/13  V1.00.00    Brian     program initial                           *
*  109-12-09  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/

package Mkt;

import java.sql.Connection;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*mkt_ftp  行銷傳送資料 至 FTP server 程式*/
public class MktFtp extends AccessDAO {
    private String progname = "mkt_ftp  行銷傳送資料 至 FTP server 程式 109/12/09 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String tmpRefIpCode = "";
    String tmpFilename = "";
    String hEflgTransSeqno = "";
    String tmpstr = "";
    String tmpstr1 = "";

    // *********************************************************

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 2 && args.length != 3) {
                comc.errExit("參數錯誤,正確為：mkt_ftp ref_ip_code filename [callbatch_seqno]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            tmpRefIpCode = args[0];
            tmpFilename = args[1];

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            // ======================================================
            // FTP

            CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
            CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

            commFTP.hEflgTransSeqno = String.format("%010d", comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
            commFTP.hEflgSystemId = javaProgram; /* 區分不同類的 FTP 檔案-大類 (必要) */
            commFTP.hEflgGroupId = ""; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = ""; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir = "";
            commFTP.hEflgModPgm = this.getClass().getName();
            String hEflgRefIpCode = tmpRefIpCode;

            String procCode = String.format("put %s", tmpFilename);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

            if (errCode != 0) {
                showLogMessage("I", "", String.format("[%s] => msg_code \n", procCode));
            } else {
                if (comc.fileRename(String.format("%s/%s", commFTP.hEriaLocalDir, tmpFilename),
                        String.format("%s/backup/%s", commFTP.hEriaLocalDir, tmpFilename)) == false)
                    showLogMessage("I", "", String.format("無法搬移[%s/%s]", commFTP.hEriaLocalDir, tmpFilename));

            }
            // ==================================================
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktFtp proc = new MktFtp();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
