/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/03/06  V1.00.00  Brian       Program initial                           *
*  109-12-16   V1.00.01  tanwei      updated for project coding standard      *
******************************************************************************/

package Ich;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;


public class IchL001 extends AccessDAO {
    private String progname = "LOG檔FTP接收處理程式  109/12/16 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String hCallBatchSeqno  = "";
	
    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            //if (comm.isAppActive(javaProgram)) {
            //    comc.err_exit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            //}
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : IchL001 [notify_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
            // "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            showLogMessage("I", "", String.format("LOG檔接收處理中.."));
            // ======================================================
            // FTP

			CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
            CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
			
            commFTP.hEflgTransSeqno = String.format("%010.0f", (double) comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
            commFTP.hEflgSystemId = "ICH_LOG"; /*  區分不同類的 FTP 檔案-大類 (必要)  */
            commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = "ICH_LOG"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir = comc.getECSHOME()+"/log/";
            commFTP.hEflgModPgm = this.getClass().getName();
            String hEflgRefIpCode = "ICH_LOG";

			comc.fileCopy(commFTP.hEriaLocalDir + "/MEGA_batch.log", commFTP.hEriaLocalDir + String.format("/MEGA_batch_%s.log", sysDate));
			
			
            System.setProperty("user.dir", commFTP.hEriaLocalDir);

            String procCode = String.format("put MEGA_batch_%s.log", sysDate);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

            if (errCode != 0) {
                comcr.errRtn(String.format("LOG檔傳送TCFS有誤(error), 請通知相關人員處理"), "", hCallBatchSeqno);
            }

			comc.fileDelete(commFTP.hEriaLocalDir + String.format("/MEGA_batch_%s.log", sysDate));
            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "執行結束");
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
        IchL001 proc = new IchL001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
