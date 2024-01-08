/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/29  V1.00.01    Brian     error correction                          *
*  109-11-18  V1.00.02    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import java.io.File;
import java.text.Normalizer;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*悠遊卡媒體文字檔歸檔整理程式*/
public class TscT020 extends AccessDAO {

    private String progname = "悠遊卡媒體文字檔歸檔整理程式  109/11/18 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscT020 [notify_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
            // "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            dirRead();
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

//    /***********************************************************************/
//    void dir_read() throws Exception {
//        String tmpstr1 = String.format("%s/media/tsc/BACKUP", comc.GetECSHOME());
//        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
//        File folder = new File(tmpstr1);
//        File[] listFiles = folder.listFiles();
//
//        for (File file : listFiles) {
//            if (!file.isFile())
//                continue;
//            String file_name = file.getName();
//            if (file_name.length() < 22)
//                continue;
//            String tmpstr = String.format("%8.8s", comc.getSubString(file_name, 14));
//
//            if (!comc.COMM_date_check(tmpstr))
//                continue;
//
//            String fs = String.format("%s/%s", tmpstr1, file_name);
//            String ft = String.format("%s/%s/%s", tmpstr1, tmpstr, file_name);
//            comc.file_rename(fs, ft);
//        }
//    }
    /***********************************************************************/
    void dirRead() throws Exception {
        String tmpstr1 = String.format("%s/media/tsc/BACKUP", comc.getECSHOME());
        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);

        List<String> files = comc.listFS(tmpstr1, "", "");
        

        for (String file : files) {
            String fileName = file;
            if (fileName.length() < 22)
                continue;
            String tmpstr = String.format("%8.8s", comc.getSubString(fileName, 14));

            if (!comc.commDateCheck(tmpstr))
                continue;

            String fs = String.format("%s/%s", tmpstr1, fileName);
            String ft = String.format("%s/%s/%s", tmpstr1, tmpstr, fileName);
            comc.fileRename(fs, ft);
        }
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT020 proc = new TscT020();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
