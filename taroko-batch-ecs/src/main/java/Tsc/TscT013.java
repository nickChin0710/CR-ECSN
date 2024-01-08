/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/28  V1.00.01    Brian     error correction                          *
*  109-11-18  V1.00.02    tanwei    updated for project coding standard       *
*  112/08/10  V1.00.03    Wilson    換行要0D0A                                  *
*  112/08/12  V1.00.04    Wilson    notify_date改營業日                                                                       *
******************************************************************************/

package Tsc;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommIps;

/*TSCC*/
public class TscT013 extends AccessDAO {

    private String progname = "TSCC 檔案通知檔回覆檔(TCRP)FTP傳送處理程式  112/08/12 V1.00.04";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommCrdRoutine comcr  = null;
    CommIps        comips = new CommIps();

    String buf = "";
    String hCallBatchSeqno = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyDate = "";
    String hTempNotifyTime = "";
    String hTnlgFtpSendDate = "";
    String hTnlgFtpReceiveDate = "";
    String hTnlgRespCode = "";
    String hTnlgRowid = "";
    String hTempFileName = "";
    int hTnlgRecordSucc = 0;
    int hTnlgRecordFail = 0;
    String hTempRowid = "";
    String hTnlgFileName = "";
    String hTnlgCheckCode = "";

    int forceFlag = 0;
    int totalCnt = 0;
    int nRetcode = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr2 = "";
    String tmpstr3 = "";
    String fileSeq = "";
    String temstr1 = "";
    String msgCode = "";
    String msgDesc = "";
    int out = -1;

//***********************************************************************
public int mainProcess(String[] args) 
{
 try 
  {
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
   if (args.length != 0 && args.length != 1 && args.length != 2) {
       comc.errExit("Usage : TscT013 [[notify_date][force_flag]] [force_flag]", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }

   // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
   // "";
   // comcr.callbatch(0, 0, 0);
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   hTnlgNotifyDate = "";
   forceFlag = 0;
   if (args.length == 1) {
       if ((args[0].length() == 1) && (args[0].equals("Y")))
           forceFlag = 1;
       if (args[0].length() == 8) {
           String sgArgs0 = "";
           sgArgs0 = args[0];
           sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
           hTnlgNotifyDate = sgArgs0;
       }
   }
   
   if (args.length == 2) {
       String sgArgs0 = "";
       sgArgs0 = args[0];
       sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
       hTnlgNotifyDate = sgArgs0;
       if (args[1].equals("Y"))
           forceFlag = 1;
   }
   
   selectPtrBusinday();

   tmpstr1 = String.format("TCRQ.%8.8s.%8.8s15", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
   hTempFileName = tmpstr1;
   tmpstr1 = String.format("TCRP.%8.8s.%8.8s15", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
   hTnlgFileName = tmpstr1;

   if(forceFlag == 0) {
      nRetcode = selectTscNotifyLog();
      if(nRetcode == 1) {
         comcr.errRtn(String.format("[%s]無TSCC檔案通知檔接收紀錄(error)",hTempFileName),"",hCallBatchSeqno);
       }
       if (nRetcode == 2) {
           comcr.errRtn(String.format("[%s]TSCC檔案通知檔回覆檔已有接收紀錄(error)",hTempFileName),""
                                       ,hCallBatchSeqno);
       }
   }

   deleteTscNotifyLog();
   fileOpen();
   // COMM_ftp_init();
   selectTscNotifyLog1();
   fileClose();

   // comc.chdir(tmpstr1);
   // ======================================================
   // FTP

   CommFTP  commFTP = new CommFTP(getDBconnect(), getDBalias());
   CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId   = "TSC_FTP_PUT";           /* 區分不同類的 FTP 檔案-大類     (必要) */
   commFTP.hEflgGroupId    = "000000";            /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "TSCC_FTP";          /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
   commFTP.hEflgModPgm     = this.getClass().getName();
   String hEflgRefIpCode  = "TSC_FTP_PUT";

   System.setProperty("user.dir", commFTP.hEriaLocalDir);

   String procCode = String.format("put %s", hTnlgFileName);
   showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

   int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

   if (errCode != 0) {
       comcr.errRtn(String.format("[%s] => msg_code[%s][%s] error\n", tmpstr1, msgCode, msgDesc)
              + String.format("TSCC [%s]TSCC檔案通知檔回覆檔傳送有誤(error), 請通知相關人員處理\n"
              , hTnlgFileName), "", hCallBatchSeqno);
   }
   // ==================================================

   insertTscNotifyLog();
   tmpstr1 = String.format("TCRQ.%8.8s.%8.8s15", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
   hTempFileName = tmpstr1;
   updateTscNotifyLog1();
   selectTscNotifyLog2();

   String fs = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
   String ft = String.format("%s/media/tsc/backup/%s", comc.getECSHOME(), hTnlgFileName);
   showLogMessage("I", "", String.format("[%s]", fs));
   if (comc.fileRename(fs, ft) == false)
       showLogMessage("I", "", String.format("move error[%s]", fs));

   // ==============================================
   // 固定要做的
   // comcr.callbatch(1, 0, 0);
   showLogMessage("I", "", "執行結束");
   finalProcess();
   return 0;
  } catch (Exception ex) { expMethod = "mainProcess"; expHandle(ex); return exceptExit; }
}
/***********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTnlgNotifyDate = getValue("h_tnlg_notify_date");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }
    }

    /***********************************************************************/
    int selectTscNotifyLog() throws Exception {
        hTnlgFtpSendDate = "";
        hTnlgFtpReceiveDate = "";
        hTnlgCheckCode = "";
        hTnlgRowid = "";

        sqlCmd  = "select ftp_send_date,";
        sqlCmd += " ftp_receive_date,";
        sqlCmd += " resp_code,";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where file_name   = ?  ";
        sqlCmd += "  and notify_date = ? ";
        setString(1, hTempFileName);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgFtpSendDate    = getValue("ftp_send_date");
            hTnlgFtpReceiveDate = getValue("ftp_receive_date");
            hTnlgRespCode        = getValue("resp_code");
            hTnlgRowid            = getValue("rowid");
        } else
            return (1);
        if (hTnlgFtpReceiveDate.length() == 0)
            return (1);
        if ((hTnlgFtpSendDate.length() != 0) && (hTnlgRespCode.equals("0000")))
            return (2);
        return (0);
    }

    /***********************************************************************/
    void deleteTscNotifyLog() throws Exception {
        daoTable = "tsc_notify_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();

    }
    /***********************************************************************/
    void fileOpen() throws Exception {
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr1, "big5");
        if(out == -1)
            comcr.errRtn(temstr1, "檔案開啓失敗！", hCallBatchSeqno);

        buf = String.format("HTCRP%8.8s%8.8s%6.6s%37.37s", comc.TSCC_BANK_ID8, hTnlgNotifyDate
                            , hTempNotifyTime," ");
        writeTextFile(out, buf + "\r\n");

    }
    /***********************************************************************/
    void selectTscNotifyLog1() throws Exception {
        String fileCode = "";

        sqlCmd = "select a.file_name,";
        sqlCmd += " a.resp_code,";
        sqlCmd += " a.record_succ,";
        sqlCmd += " a.record_fail,";
        sqlCmd += " a.rowid rowid";
        sqlCmd += " from tsc_notify_log a, tsc_file_iden b ";
        sqlCmd += "where a.notify_date = ? ";
        sqlCmd += "  and a.file_iden   = b.file_iden ";
        sqlCmd += "  and a.file_name  != '' ";
        sqlCmd += "  and b.tran_type   = 'O' ";
        setString(1, hTnlgNotifyDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hTempFileName   = getValue("file_name");
            hTnlgRespCode   = getValue("resp_code");
            hTnlgRecordSucc = getValueInt("record_succ");
            hTnlgRecordFail = getValueInt("record_fail");
            hTempRowid       = getValue("rowid");

            fileCode = "1";
            if (!hTnlgRespCode.equals("0000"))
                fileCode = "9";
            tmpstr1 = String.format("D01%-24.24s%1.1s%08d%08d", hTempFileName, fileCode
                    , hTnlgRecordSucc, hTnlgRecordFail);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
            buf = String.format("%s%16.16s0000", tmpstr1, tmpstr2);
            writeTextFile(out, buf + "\r\n");

            // ======================================================
            // FTP

            CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
            CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

            commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
            commFTP.hEflgSystemId   = "TSC_FTP_PUT";           /* 區分不同類的 FTP 檔案-大類     (必要) */
            commFTP.hEflgGroupId    = "000000";            /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = "TSCC_FTP";          /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
            commFTP.hEflgModPgm     = this.getClass().getName();
            String hEflgRefIpCode  = "TSC_FTP_PUT";

            System.setProperty("user.dir", commFTP.hEriaLocalDir);

            String procCode = String.format("put %s.RPT", hTempFileName);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);//test

            if (errCode != 0) {
                comcr.errRtn(String.format("TSCC [%s]TSCC檔案回覆檔傳送有誤(error), 請通知相關人員處理"
                                            , hTempFileName), "", hCallBatchSeqno);
            }
            // ==================================================
            updateTscNotifyLog();
            totalCnt++;
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void updateTscNotifyLog() throws Exception {
        daoTable   = " tsc_notify_log";
        updateSQL  = " ftp_send_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " ftp_send_time = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_time      = sysdate";
        whereStr   = " where rowid   = ? ";
        setString(1, javaProgram);
        setRowId(2, hTempRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }
    }
    /***********************************************************************/
    void fileClose() throws Exception {
        buf = String.format("T%08d%55.55s", totalCnt, " ");
        writeTextFile(out, buf + "\r\n");
        closeOutputText(out);
    }
    /***********************************************************************/
    void insertTscNotifyLog() throws Exception {
        setValue("file_iden", "TCRP");
        setValue("tran_type", "S");
        setValue("file_name", hTnlgFileName);
        setValue("notify_date", hTnlgNotifyDate);
        setValue("notify_time", hTempNotifyTime);
        setValueInt("notify_seq", 15);
        setValue("ftp_send_date", hTempNotifyDate);
        setValue("ftp_send_time", hTempNotifyTime);
        setValue("check_code", "0000");
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscNotifyLog1() throws Exception {
        daoTable   = " tsc_notify_log";
        updateSQL  = " resp_code  = '0000', ";
        updateSQL += " mod_pgm    = ?, ";
        updateSQL += " mod_time   = sysdate ";
        whereStr   = " where file_name  = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }
    }
    /***********************************************************************/
    void selectTscNotifyLog2() throws Exception {

        sqlCmd = "select a.file_name ";
        sqlCmd += " from tsc_notify_log a,tsc_file_iden b ";
        sqlCmd += "where a.notify_date = ? ";
        sqlCmd += "  and a.file_iden   = b.file_iden ";
        sqlCmd += "  and a.file_name  != '' ";
        sqlCmd += "  and b.tran_type   = 'O' ";
        setString(1, hTnlgNotifyDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hTempFileName = getValue("file_name");

            String rootDir = String.format("%s/media/tsc", comc.getECSHOME());
            rootDir = Normalizer.normalize(rootDir, java.text.Normalizer.Form.NFKD);
            String fs = String.format("%s/%s.RPT", rootDir, hTempFileName);
            String ft = String.format("%s/backup/%s.RPT", rootDir, hTempFileName);
            showLogMessage("I", "", String.format("[%s]", fs));
            if (comc.fileRename(fs, ft) == false)
                showLogMessage("I", "", String.format("move error[$s]", fs));
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT013 proc = new TscT013();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
