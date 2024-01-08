/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/24  V1.00.01    Brian     error correction                          *
*  109-11-18  V1.00.02    tanwei    updated for project coding standard       *
*  112/05/11  V1.00.03    Wilson    change to TSC_FTP_PUT                     *
*  112/05/17  V1.00.04    Wilson    procFTP調整                                                                                         * 
*  112/08/10  V1.00.05    Wilson    換行要0D0A                                  *
*  112/12/19  V1.00.06    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommIps;
import com.CommRoutine;

/*悠遊卡會員銀行通知檔(MBRQ)產生及媒體FTP傳送程式*/
public class TscT002 extends AccessDAO {

    private String progname = "悠遊卡會員銀行通知檔(MBRQ)產生及媒體FTP傳送程式  112/12/19 V1.00.06";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps       comips = new CommIps();

    String buf = "";
    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyDate = "";
    String hTnlgNotifyTime = "";
    String hTnlgFileName = "";
    String hTnlgRowid = "";
    String hTnlgFtpSendDate = "";
    String hTempFileName = "";
    int hTnlgRecordCnt = 0;

    int forceFlag = 0;
    int totalCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr2 = "";
    String fileSeq = "";
    String temstr1 = "";

    int out = -1;
//*****************************************************************
public int mainProcess(String[] args) {
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
       comc.errExit("Usage : TscT002 [[notify_date][force_flag]] [force_flag]", "");
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
       if ((args[0].length() == 1) && (args[0].substring(0, 1).equals("Y")))
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

   if (forceFlag == 0) {
       if (selectTscNotifyLog() == 1) {
           comcr.errRtn(String.format("本日會員銀行通知檔媒體已處理完成, 不可再處理!(error)"), hTnlgNotifyDate, hCallBatchSeqno);

       }
   }
   deleteTscNotifyLog();
   fileOpen();
   selectTscNotifyLog1();
   fileClose();

   hTnlgRecordCnt = totalCnt;
   insertTscNotifyLog();

   // ======================================================
   // FTP

   CommFTP  commFTP = new CommFTP(getDBconnect(), getDBalias());
   CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要)*/
   commFTP.hEflgSystemId   = "TSC_FTP_PUT";      /* 區分不同類的 FTP 檔案-大類     (必要)*/
   commFTP.hEflgGroupId    = "000000";           /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
   commFTP.hEflgSourceFrom = "TSCC_FTP";         /* 區分不同類的 FTP 檔案-細分類 (非必要)*/
   commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
   commFTP.hEflgModPgm     = this.getClass().getName();
   String hEflgRefIpCode  = "TSC_FTP_PUT";

   System.setProperty("user.dir", commFTP.hEriaLocalDir);

   String procCode = String.format("put %s", hTempFileName);
   showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

   int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

   if (errCode != 0) {
       comcr.errRtn(String.format("FTP error"), "", hCallBatchSeqno);
   }
   
//   renameFile2(hTempFileName);
   // ==================================================
   selectTscNotifyLog2();

   // ==============================================
   // 固定要做的
   // comcr.callbatch(1, 0, 0);
   showLogMessage("I", "", "執行結束");
   finalProcess();
   return 0;
  } catch (Exception ex)
      { expMethod = "mainProcess"; expHandle(ex); return exceptExit; }
}
/***********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select to_char(add_days(to_date(business_date,'yyyymmdd'),1),'yyyymmdd') h_business_date,";
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTnlgNotifyTime = getValue("h_tnlg_notify_time");
        }

    }

    /***********************************************************************/
    void insertTscNotifyLog() throws Exception {
        setValue("notify_date", hTnlgNotifyDate);
        setValue("notify_time", hTnlgNotifyTime);
        setValueInt("notify_seq", 7);
        setValue("file_iden", "MBRQ");
        setValue("tran_type", "S");
        setValue("file_name", hTempFileName);
        setValueInt("record_cnt", hTnlgRecordCnt);
        setValue("ftp_send_date", hTnlgNotifyDate);
        setValue("ftp_send_time", hTnlgNotifyTime);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int selectTscNotifyLog() throws Exception {
        sqlCmd = "select ftp_send_date ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where notify_date    = ?  ";
        sqlCmd += "  and ftp_send_date != ''  ";
        sqlCmd += " fetch first 1 rows only  ";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgFtpSendDate = getValue("ftp_send_date");
        } else
            return (0);
        return (1);
    }

    /***********************************************************************/
//  	void renameFile2(String removeFileName) throws Exception {
//  		String tmpstr1 = comc.getECSHOME() + "/media/tsc/" + removeFileName;
//  		String tmpstr2 = comc.getECSHOME() + "/media/tsc/backup/" + removeFileName;
//  		
//  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
//  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
//  			return;
//  		}
//  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
//  	}
  	
    /****************************************************************************/ 
    void selectTscNotifyLog2() throws Exception {
        sqlCmd = "select file_name  ";
        sqlCmd += " from tsc_notify_log ";
        sqlCmd += "where notify_date   = ? ";
        sqlCmd += "  and ftp_send_date = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int cursorIndex = openCursor();

        while (fetchTable(cursorIndex)) {
            hTnlgFileName = getValue("file_name");

            tmpstr1 = String.format("%s/media/tsc", comc.getECSHOME());
            tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
            String fs = String.format("%s/%s", tmpstr1, hTnlgFileName);
            String ft = String.format("%s/backup/%s", tmpstr1, hTnlgFileName);
            showLogMessage("I", "", String.format("[%s]", fs));
            if (comc.fileRename(fs, ft) == false)
                showLogMessage("I", "", String.format("move error[%s]", fs));
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void deleteTscNotifyLog() throws Exception {
        daoTable  = "tsc_notify_log";
        whereStr  = "where notify_date = ?  ";
        whereStr += "  and file_iden like 'MBR%' ";
        setString(1, hTnlgNotifyDate);
        deleteTable();

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        tmpstr1 = String.format("MBRQ.%8.8s.%8.8s07", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
        hTempFileName = tmpstr1;
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr1, "big5");
        if(out == -1)
            comcr.errRtn(temstr1, "檔案開啓失敗！", hCallBatchSeqno);

        buf = String.format("HMBRQ%8.8s%8.8s%6.6s%37.37s", comc.TSCC_BANK_ID8
                           , hTnlgNotifyDate, hTnlgNotifyTime," ");
        writeTextFile(out, buf + "\r\n");
    }
/***********************************************************************/
void selectTscNotifyLog1() throws Exception 
{
  sqlCmd = "select file_name,";
  sqlCmd += " rowid rowid";
  sqlCmd += " from tsc_notify_log ";
  sqlCmd += "where notify_date   = ? ";
  sqlCmd += "  and tran_type     = 'I' ";
  sqlCmd += "  and ftp_send_date = '' ";
  setString(1, hTnlgNotifyDate);
  int cursorIndex = openCursor();

  while (fetchTable(cursorIndex)) {
      hTnlgFileName = getValue("file_name");
      hTnlgRowid     = getValue("rowid");

      tmpstr1 = String.format("D01%-24.24s00000000000000000", hTnlgFileName);
      tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
      buf = String.format("%s%16.16s0000", tmpstr1, tmpstr2);
      writeTextFile(out, buf + "\r\n");

      // ======================================================
      // FTP

      CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
      CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

      commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用  (必要)*/
      commFTP.hEflgSystemId   = "TSC_FTP_PUT";   /* 區分不同類的 FTP 檔案-大類     (必要)*/
      commFTP.hEflgGroupId    = "000000";        /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
      commFTP.hEflgSourceFrom = "TSCC_FTP";      /* 區分不同類的 FTP 檔案-細分類 (非必要)*/
      commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
      commFTP.hEflgModPgm     = this.getClass().getName();
      String hEflgRefIpCode  = "TSC_FTP_PUT";

      System.setProperty("user.dir", commFTP.hEriaLocalDir);

      String procCode = String.format("put %s", hTnlgFileName);
      showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

      int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

      if (errCode != 0) {
          comcr.errRtn(String.format("FTP error"), "", hCallBatchSeqno);
      }
      
//      renameFile1(hTnlgFileName);
      // ==================================================
      updateTscNotifyLog();
      totalCnt++;
  }
  closeCursor(cursorIndex);

}
/***********************************************************************/
    void fileClose() throws Exception {
        buf = String.format("T%08d%55.55s", totalCnt, " ");
        writeTextFile(out, buf + "\r\n");
        closeOutputText(out);
    }

    /***********************************************************************/
//  	void renameFile1(String removeFileName) throws Exception {
//  		String tmpstr1 = comc.getECSHOME() + "/media/tsc/" + removeFileName;
//  		String tmpstr2 = comc.getECSHOME() + "/media/tsc/backup/" + removeFileName;
//  		
//  		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
//  			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
//  			return;
//  		}
//  		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
//  	}
  	
    /****************************************************************************/ 
    void updateTscNotifyLog() throws Exception {
        daoTable   = "tsc_notify_log";
        updateSQL  = " ftp_send_date = ? , ";
        updateSQL += " ftp_send_time = ? , ";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_time      = sysdate";
        whereStr   = " where rowid   = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyTime);
        setString(3, javaProgram);
        setRowId(4, hTnlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT002 proc = new TscT002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
