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

/*從SVCS收檔傳給資訊處FTP Server處理程式*/
public class TscT015 extends AccessDAO {

    private String progname = "從SVCS收檔傳給資訊處FTP Server處理程式  109/11/18 V1.00.01";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    String prgmId = "TscT015";

    EcsFtpBuf ecsftp = new EcsFtpBuf();

    int    forceFlag           = 0;
    int    totalCnt            = 0;
    String hTempSysdate       = "";
    String hBusiBusinessDate = "";
    String hCallBatchSeqno   = "";
    
    String hTnlgFileName = "";
    String hTnlgFileIden = "";
    String hIdenShaFlag = "";
    String tmpstr1 = "";
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
                comc.errExit("Usage : TscT015 [sysdate [R]]", "");
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

            totalCnt=0;
            showLogMessage("I", "", String.format("=========================================\n"));
            showLogMessage("I", "", String.format("讀取今日接收資料\n"));
            selectTscNotifyLog();
            showLogMessage("I", "", String.format("=========================================\n"));
            showLogMessage("I", "", String.format("讀取今日接收報表\n"));
            procFtpReport();
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
    void selectTscNotifyLog() throws Exception {

        sqlCmd  = " SELECT  file_name, ";
        sqlCmd += "         file_iden ";
        sqlCmd += "   FROM  tsc_notify_log   ";
        sqlCmd += "  WHERE  ftp_receive_date  = ? ";
        sqlCmd += "    AND  file_iden in ('ACAE','ACAN','ACCB','ACCG','ACCS', ";
        sqlCmd += "                       'ACFI','ACLC','ACPF','ACRI','ACRT', ";
        sqlCmd += "                       'ACTI','MBRP','TCRQ') ";
        setString(1, hTempSysdate);

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hTnlgFileName = getValue("file_name");
            hTnlgFileIden = getValue("file_iden");

        showLogMessage("I", "", "888 Read notify ="+hTnlgFileName+","+hTnlgFileIden);

            if ((hTnlgFileIden.equals("ACCS")) || (hTnlgFileIden.equals("ACPF")) || 
                (hTnlgFileIden.equals("ACRI")) || (hTnlgFileIden.equals("ACTI"))) {
                tmpstr1 = String.format("%s.RPT", hTnlgFileName);
                hTnlgFileName = tmpstr1;
            }

            procFtp();

            if ((hTnlgFileIden.equals("MBRP") == false) && 
                (hTnlgFileIden.equals("TCRQ") == false)) {
                hIdenShaFlag = "";
                sqlCmd  = " SELECT sha_flag ";
                sqlCmd += "   FROM tsc_file_iden ";
                sqlCmd += "  WHERE file_iden = ? ";
                setString(1, hTnlgFileIden);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hIdenShaFlag = getValue("sha_flag");
                }
                if (hIdenShaFlag.equals("Y")) {
                    procFtp1();

                    tmpstr1 = String.format("%s/media/tsc/", comc.getECSHOME());

                    comc.fileRename(String.format("%s.SHA", tmpstr1 + hTnlgFileName),
                            String.format("BACKUP/%s.SHA", tmpstr1 + hTnlgFileName));

                    tmpstr1 = String.format("mv %s.SHA BACKUP/%s.SHA"
                                    , hTnlgFileName, hTnlgFileName);
                    showLogMessage("I", "", String.format("[%s]", tmpstr1));
                }

                tmpstr1 = String.format("%s/media/tsc/", comc.getECSHOME());

                comc.fileRename(String.format("%s", tmpstr1 + hTnlgFileName),
                        String.format("BACKUP/%s", tmpstr1 + hTnlgFileName));

                tmpstr1 = String.format("mv %s BACKUP/%s", hTnlgFileName, hTnlgFileName);
                showLogMessage("I", "", String.format("[%s]", tmpstr1));
            }
        }
        closeCursor(cursorIndex);
    }
    /*****************************************************************************/
    void procFtp() throws Exception {

        // ======================================================
        // FTP

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = String.format("%010d", comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId   = "DW_FTP_TSCC";    /* 區分不同類的 FTP 檔案-大類     (必要) */
        commFTP.hEflgGroupId    = hTnlgFileIden; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "DW_FTP";         /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        
        if ((hTnlgFileIden.equals("MBRP")) || (hTnlgFileIden.equals("TCRQ"))) {
            commFTP.hEriaLocalDir = String.format("%s/media/tsc/BACKUP", comc.getECSHOME());
        } else {
            commFTP.hEriaLocalDir = String.format("%s/media/tsc/", comc.getECSHOME());
        }
        
        commFTP.hEflgModPgm = this.getClass().getName();
        String hEflgRefIpCode = "DW_FTP_TSCC_OUT";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s", hTnlgFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳 1 ....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("[%s] => msg_code[%d]\n", procCode,  errCode));
        }
     //================================================== 
    }
   /***************************************************************************/
    void procFtp1() throws Exception {

        // ======================================================
        // FTP

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = String.format("%010d", comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId   = "DW_FTP_TSCC";    /* 區分不同類的 FTP 檔案-大類     (必要) */
        commFTP.hEflgGroupId    = hTnlgFileIden; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "DW_FTP";         /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir   = String.format("%s/media/tsc/", comc.getECSHOME());
        commFTP.hEflgModPgm     = this.getClass().getName();
        String hEflgRefIpCode  = "DW_FTP_TSCC_OUT";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s.SHA", hTnlgFileName);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳 2 ....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

        if (errCode != 0) {
            showLogMessage("I", "",
                    String.format("[%s] => msg_code[%d]\n", procCode, errCode));
        }
        // ==================================================
    }
/***************************************************************************/
void procFtpReport() throws Exception 
{
  String[] extStr = { "TSCCSD625I", "TSCCSM631I", "TSCCSD606I" };
  // ======================================================
  // FTP

  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());

  commFTP.hEflgTransSeqno = String.format("%010d", comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
  commFTP.hEflgSystemId   = "DW_FTP_TSCC";                     /* 區分不同類的 FTP 檔案-大類     (必要) */
  commFTP.hEflgGroupId    = hTnlgFileIden;                  /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  commFTP.hEflgSourceFrom = "DW_FTP";                          /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  commFTP.hEriaLocalDir   = String.format("%s/media/tsc/report", comc.getECSHOME());
  commFTP.hEflgModPgm     = this.getClass().getName();
  String hEflgRefIpCode  = "DW_FTP_TSCC_REPORT";

  System.setProperty("user.dir", commFTP.hEriaLocalDir);

  for (int inta = 0; inta < 3; inta++) {
      String fileName = String.format("%s.%8.8s.%8.8s", extStr[inta], comc.TSCC_BANK_ID8, hTempSysdate);
      String procCode = String.format("put %s", fileName);
      showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳 3 ....");

      int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

      if (errCode != 0) {
          showLogMessage("I","",String.format("[%s] => msg_code[%d]\n", procCode, errCode));
      }
  }
  // ================================================== 
}
/***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT015 proc = new TscT015();
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
