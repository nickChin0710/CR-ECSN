/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/29  V1.00.01    Brian     error correction                          *
*  109-11-18  V1.00.02    tanwei    updated for project coding standard       *
*  112/05/11  V1.00.03    Wilson    change to TSC_FTP_PUT                     *                                                                           *
******************************************************************************/

package Tsc;

import java.io.RandomAccessFile;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommIps;

/*會員銀行重新產生檔案媒體回覆檔案(FTP)傳送程式*/
public class TscT022 extends AccessDAO {

    private String progname = "會員銀行重新產生檔案媒體回覆檔案(FTP)傳送程式  112/05/11 V1.00.03";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommIps        comips   = new CommIps();

    String buf                = "";
    String hCallBatchSeqno = "";

    String hTnlgNotifyDate   = "";
    String hBusiBusinessDate = "";
    String hTempNotifyTime   = "";
    String hTnlgFileName     = "";
    String hTnlgCheckCode    = "";
    String hTnlgRowid         = "";
    String hOrgdOrgData      = "";
    String hOrgdRptRespCode = "";
    String hTnlgFileIden     = "";
    String hEriaLocalDir     = "";
    int    hTnlgNotifySeq    = 0;
    int    hTnlgRecordSucc   = 0;
    int    hTnlgRecordFail   = 0;

    int    forceFlag         = 0;
    int    totalCnt          = 0;
    String tmpstr             = "";
    String tmpstr1            = "";
    String tmpstr2            = "";
    String temstr2            = "";
    String tmpstr3            = "";
    String fileSeq           = "";
    String temstr1            = "";
    String msgCode           = "";
    String msgDesc           = "";
    String hTempNotifyDate = "";

    RandomAccessFile raf = null;

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
   if (args.length != 3 && args.length != 4) {
       comc.errExit("Usage : TscT022 file_iden notify_date notify_seq [force_flag]", "");
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
   hTnlgFileIden = args[0];
   hTnlgNotifyDate = args[1];
   hTnlgNotifySeq = comcr.str2int(args[2]);
   if (args.length == 4)
       if (args[3].equals("Y"))
           forceFlag = 1;

   tmpstr1 = String.format("%s.%8.8s.%8.8s%02d", hTnlgFileIden, comc.TSCC_BANK_ID8, hTnlgNotifyDate,
           hTnlgNotifySeq);
   hTnlgFileName = tmpstr1;

   selectPtrBusinday();

   selectTscNotifyLog();
   // ======================================================
   // FTP

   CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
   CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId   = "TSC_FTP_PUT";       /* 區分不同類的 FTP 檔案-大類     (必要) */
   commFTP.hEflgGroupId    = "000000";            /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "TSCC_FTP";          /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
   commFTP.hEflgModPgm     = this.getClass().getName();
   String hEflgRefIpCode  = "TSC_FTP_PUT";

   System.setProperty("user.dir", commFTP.hEriaLocalDir);

   String procCode = String.format("put %s.RPT", hTnlgFileName);
   showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

   int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

   if (errCode != 0) {
       comcr.errRtn(String.format("[%s] => msg_code[%s][%s] error\n", tmpstr1, msgCode, msgDesc)
     + String.format("TSCC [%s]檔案通知檔回覆檔傳送有誤(error), 請通知相關人員處理",hTnlgFileName),"",hCallBatchSeqno);
   }
   // ==================================================

   String rootDir = String.format("%s/media/tsc", comc.getECSHOME());
   rootDir  = Normalizer.normalize(rootDir, java.text.Normalizer.Form.NFKD);
   String fs = String.format("%s/%s.RPT", rootDir, hTnlgFileName);
   String ft = String.format("%s/backup/%s.RPT", rootDir, hTnlgFileName);
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
        sqlCmd  = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)) , ''"
                + ", to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'07') , 1 , sysdate"
                + ", sysdate-1 days), 'yyyymmdd') , ?) h_tnlg_notify_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTnlgNotifyDate   = getValue("h_tnlg_notify_date");
            hTempNotifyTime   = getValue("h_temp_notify_time");
        }

    }
/***********************************************************************/
void selectTscNotifyLog() throws Exception 
{

  sqlCmd = "select check_code,";
  sqlCmd += " rowid rowid ";
  sqlCmd += " from tsc_notify_log ";
  sqlCmd += "where file_name = ? ";
  setString(1, hTnlgFileName);
  int cursorIndex = openCursor();
  while (fetchTable(cursorIndex)) 
    {
      hTnlgCheckCode = getValue("check_code");
      hTnlgRowid = getValue("rowid");

      showLogMessage("I", "", String.format("處理[%s]回覆檔, 回應代碼[%s]", hTnlgFileName, hTnlgCheckCode));
      fileOpen();
      if (hTnlgCheckCode.equals("0000"))
          selectTscOrgdataLog();

      tmpstr1 = String.format("T%08d%406.406s", hTnlgRecordFail, " ");
      tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
      tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
      buf = String.format("%-415.415s%16.16s\n", tmpstr1, tmpstr2);
      raf.write(buf.getBytes());

      if(hTnlgCheckCode.equals("0000")) {
    //   fseek(fptr1, 0L, SEEK_SET);
         raf.seek(0);
         tmpstr1 = String.format("H%4.4s%8.8s%8.8s%6.6s%4.4s%08d%08d%368.368s",hTnlgFileIden,comc.TSCC_BANK_ID8
             ,hTnlgNotifyDate,hTempNotifyTime,hTnlgCheckCode,hTnlgRecordSucc,hTnlgRecordFail," ");
         tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
         buf = String.format("%-415.415s%16.16s\n", tmpstr1, tmpstr2);
         raf.write(buf.getBytes());
      }
      raf.close();
      showLogMessage("I", "", String.format("    處理筆數 [%d] 成功\\筆數[%d] 失敗筆數[%d]\n\n"
                        ,hTnlgRecordSucc + hTnlgRecordFail, hTnlgRecordSucc, hTnlgRecordFail));

      updateTscNotifyLog();
      totalCnt++;
    }
   
  closeCursor(cursorIndex);

}
/***********************************************************************/
    void fileOpen() throws Exception {
        tmpstr1 = String.format("%s.RPT", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        raf = new RandomAccessFile(temstr1, "rw");

        buf = String.format("H%4.4s%8.8s%8.8s%6.6s%4,4s%08d%08d%368.368s%16.16s%c\n", hTnlgFileIden
            , comc.TSCC_BANK_ID8, hTempNotifyDate, hTempNotifyTime, hTnlgCheckCode, 0, 0, " ", ' ');
        raf.write(buf.getBytes());
    }

    /***********************************************************************/
    void selectTscOrgdataLog() throws Exception {

        sqlCmd = "select org_data,";
        sqlCmd += " rpt_resp_code ";
        sqlCmd += " from tsc_orgdata_log ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hOrgdOrgData = getValue("org_data", i);
            hOrgdRptRespCode = getValue("rpt_resp_code", i);

            if (hOrgdRptRespCode.equals("0000")) {
                hTnlgRecordSucc++;
            } else {
                hTnlgRecordFail++;
                tmpstr1 = String.format("D01%4.4s%-408.408s", hOrgdRptRespCode, hOrgdOrgData);
                tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
                buf = String.format("%-415.415s%16.16s\n", tmpstr1, tmpstr2);
                raf.write(buf.getBytes());
            }

        }
    }

    /***********************************************************************/
    void updateTscNotifyLog() throws Exception {
        daoTable   = " tsc_notify_log";
        updateSQL  = " media_crt_date  = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " media_crt_time  = to_char(sysdate, 'hh24miss'),";
        updateSQL += " record_succ        = ?,";
        updateSQL += " record_fail        = ?,";
        updateSQL += " resp_code          = ?,";
        updateSQL += " mod_pgm            = ?,";
        updateSQL += " mod_time           = sysdate";
        whereStr = "where rowid           = ? ";
        setInt(1, hTnlgRecordSucc);
        setInt(2, hTnlgRecordFail);
        setString(3, hTnlgCheckCode);
        setString(4, javaProgram);
        setRowId(5, hTnlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT022 proc = new TscT022();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
