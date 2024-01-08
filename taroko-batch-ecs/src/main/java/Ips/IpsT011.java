/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-15  V1.00.01    tanwei      updated for project coding standard    *
*  112/05/12  V1.00.02    Wilson      change to IPS_FTP_GET                   *
******************************************************************************/

package Ips;

import java.util.concurrent.TimeUnit;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*一卡通檔案(I2B)FTP接收處理*/
public class IpsT011 extends AccessDAO {
    private String progname = "一卡通檔案(I2B)FTP接收處理  112/05/12 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;

    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyD = "";
    String hTempNotifyDate = "";
    String hTempNotifyTime = "";
    String hTfinFileIden = "";
    String hTfinDateType = "";
    String hTfinRunDay = "";
    String hTfinFileDesc = "";
    String hTfinRecordLength = "";
    int hCnt = 0;
    String hTempRowid = "";
    String hTempNotifyDate2 = "";
    String hTnlgRespCode = "";
    String hNewFileName = "";
    String hTnlgNotifySeq = "";
    String hInt = "";
    String hTempSomeday = "";
    String hEflgTransSeqno = "";
    String hEflgFileName = "";
    String hEflgRowid = "";
    String hEflgFileDate = "";
    String hEflgProcCode = "";
    String hEflgProcDesc = "";

    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String hEflgSystemId = "";
    String hEflgGroupId = "";
    String hEflgSourceFrom = "";
    String hEriaLocalDir = "";
    String hTnlgFileName = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int totalCnt = 0;
    String nUserpid = "";
    String tmpstr = "";
    int nRetcode = 0;
    int totalCnt1 = 0;
    int hTnlgRecordCnt = 0;
    int errCode = 0;

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
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IpsT011 [[notify_date][force_flag]] [force_flag]", "");
            }
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTnlgNotifyDate = args[0];
            }
            if (args.length == 2) {
                hTnlgNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();

            if (forceFlag == 0) {
                if (selectIpsNotifyLogA() == 0) {
                    exceptExit = 0;
                    comcr.errRtn(String.format("[%s]IPS 通知檔已經接收, 不可重複執行(error)", hTempNotifyDate), "", hCallBatchSeqno);
                }
            }

            showLogMessage("I", "",
                    String.format(" Process Date  =[%s][%d][%s]", hTnlgNotifyDate, forceFlag, hTempNotifyDate));

            deleteIpsNotifyLog1();

            fileOpen();

            selectIpsFileIden();

            // ==============================================
            // 固定要做的
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
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(DAYOFWEEK(to_date(business_date,'yyyymmdd'))) h_temp_notify_d,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyD = getValue("h_temp_notify_d");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;

    }

    /***********************************************************************/
    int selectIpsNotifyLogA() throws Exception {
        sqlCmd = "select 1 h_cnt";
        sqlCmd += " from ips_notify_log  ";
        sqlCmd += "where tran_type   = 'O'  ";
        sqlCmd += "  and notify_date = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return (1);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
        return (0);
    }

    /***********************************************************************/
    void deleteIpsNotifyLog1() throws Exception {
        daoTable  = "ips_notify_log";
        whereStr  = "where notify_date = ?  ";
        whereStr += "  and tran_type   = 'O' ";
        setString(1, hTnlgNotifyDate);
        deleteTable();

    }

    /***********************************************************************/
    void selectIpsFileIden() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_iden,";
        sqlCmd += "date_type,";
        sqlCmd += "run_day,";
        sqlCmd += "file_desc,";
        sqlCmd += "record_length ";
        sqlCmd += "from ips_file_iden ";
        sqlCmd += "where tran_type = 'O' ";
        sqlCmd += "  and use_flag  = 'Y' ";
        openCursor();

        while (fetchTable()) {
            hTfinFileIden = getValue("file_iden");
            hTfinDateType = getValue("date_type");
            hTfinRunDay = getValue("run_day");
            hTfinFileDesc = getValue("file_desc");
            hTfinRecordLength = getValue("record_length");

            for (int int1a = 0; int1a < hTfinDateType.length(); int1a++) {
                if (hTfinDateType.trim().equals("F")) {
                    if (!hTnlgNotifyDate.substring(6, 8).equals(hTfinRunDay.substring(int1a * 2, int1a * 2 + 2)))
                        continue;
                }
                if (hTfinDateType.trim().equals("W")) {
                    tmpstr1 = comcr.increaseDays(hTnlgNotifyDate, -1);
                    tmpstr2 = comcr.increaseDays(tmpstr1, 1);
                    if (!hTnlgNotifyDate.equals(tmpstr2))
                        continue;
                }
                if (hTfinDateType.trim().equals("K")) {
                    if ((int) hTempNotifyD.toCharArray()[0] < (int) hTfinRunDay.toCharArray()[int1a * 2 + 1])
                        continue;
                    selectSomeday(int1a * 2 + 2);
                    tmpstr1 = comcr.increaseDays(hTempSomeday, -1);
                    tmpstr2 = comcr.increaseDays(tmpstr1, 1);
                    if (!hTnlgNotifyDate.equals(tmpstr2))
                        continue;
                }
                if (hTfinDateType.trim().equals("H")) {
                    tmpstr1 = String.format("%6.6s%2.2s", hTnlgNotifyDate,
                            hTfinRunDay.substring(int1a * 2, int1a * 2 + 2));
                    tmpstr2 = comcr.increaseDays(tmpstr1, -1);
                    tmpstr1 = comcr.increaseDays(tmpstr2, 1);
                    if (!hTnlgNotifyDate.equals(tmpstr1))
                        continue;
                }
                if (hTfinDateType.trim().equals("T")) {
                    if (!hTnlgNotifyDate.substring(7, 8).equals("1"))
                        continue;
                }

                hTempNotifyDate2 = hTnlgNotifyDate;
                if (selectIpsNotifyLogC() == 0)
                    continue;

                ftpGetRtn();

                selectEcsFtpLog();

            }
        }
        closeCursor();

    }

    /***********************************************************************/
    void fileOpen() throws Exception {

        tmpstr1 = String.format("%s/media/ips/backup/%s", comc.getECSHOME(), hTnlgNotifyDate);
        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
        
        comc.mkdirsFromFilenameWithPath(tmpstr1);

        //comc.chmod777(tmpstr1);

    }

    /***********************************************************************/
    int selectIpsNotifyLogC() throws Exception {
        sqlCmd = "select rowid as rowid ";
        sqlCmd += " from ips_notify_log  ";
        sqlCmd += "where notify_date = ?  ";
        sqlCmd += "and  file_iden = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempNotifyDate2);
        setString(2, hTfinFileIden);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return (1);
        }
        if (recordCnt > 0) {
            hTempRowid = getValue("rowid");
        }

        return 0;
    }
/***********************************************************************/
void ftpGetRtn() throws Exception 
{
  String fileSeq = "";

  TimeUnit.SECONDS.sleep(10);

  String tmpstr0 = String.format("%s/media/ips", comc.getECSHOME());
  tmpstr0 = Normalizer.normalize(tmpstr0, java.text.Normalizer.Form.NFKD);
  hEriaLocalDir = tmpstr0;

  fileSeq = "01";
  tmpstr2 = String.format("%6.6s_%4.4s%8.8s%2.2s.dat", hTfinFileIden  , comc.IPS_BANK_ID4
                                                     , hTnlgNotifyDate, fileSeq);
  hNewFileName = tmpstr2;
  //tmpstr2 = String.format("%6.6s_%4.4s%8.8s%2.2s.zip", hTfinFileIden  , comc.IPS_BANK_ID4
  //                                                   , hTnlgNotifyDate, fileSeq);
  hTnlgFileName = tmpstr2;

  CommFTP  commFTP = new CommFTP(getDBconnect(), getDBalias());
  CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  commFTP.hEflgSystemId   = "IPS_FTP_GET";       /* 區分不同類的 FTP 檔案-大類     (必要) */
  commFTP.hEflgGroupId    = "0000";              /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  commFTP.hEflgSourceFrom = "IPS";               /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  commFTP.hEriaLocalDir   = hEriaLocalDir;
  commFTP.hEflgModPgm     = this.getClass().getName();
  String hEflgRefIpCode  = "IPS_FTP_GET";

  System.setProperty("user.dir", commFTP.hEriaLocalDir);

  String procCode = String.format("get %s", hTnlgFileName);
  showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

  int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
  if (errCode != 0) {
      showLogMessage("I", "", String.format("[%s] => msg_code[%d]", procCode, errCode));
      showLogMessage("I", "", String.format("[%s]IPS 今日無此檔案", hTnlgFileName));
      return;
  }

  hTnlgRespCode = "0000";
  moveBackup(hTnlgFileName);

  String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
  temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

  int f = openInputText(temstr1);
  if (f == -1) {
      closeInputText(f);
      return;
  } else {
      insertIpsNotifyLog1();

      /*
      tmpstr2 = String.format("%s/media/ips/backup/%s/%s.BAK"
                              , comc.getECSHOME() , hTnlgNotifyDate, hTnlgFileName);
      comc.fileCopy(temstr1, tmpstr2);

      //PKZIP 解壓縮 
      String hPasswd = "";
      String zipFile = hEriaLocalDir + "/" + hTnlgFileName;

      int tmpInt = comm.unzipFile(zipFile, hEriaLocalDir, hPasswd);
      if (tmpInt != 0) {
          showLogMessage("I", "", String.format("無法壓縮檔案[%s]", zipFile));
         }
      */
      
      closeInputText(f);
  }

}
/***********************************************************************/
    void insertIpsNotifyLog1() throws Exception {

        setValue("file_iden", hTfinFileIden);
        setValue("tran_type", "O");
        setValue("notify_date", hTempNotifyDate2);
        setValue("notify_time", hTempNotifyTime);
        setValue("check_code", hTnlgRespCode);
        setValue("perform_flag", "Y");
        setValue("file_name", hNewFileName);
        setValue("notify_seq", hTnlgNotifySeq);
        setValue("ftp_receive_date", hTempNotifyDate);
        setValue("ftp_receive_time", hTempNotifyTime);
        setValue("proc_flag", "1");
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ips_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_notify_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectEcsFtpLog() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_name,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from ecs_ftp_log ";
        sqlCmd += "where trans_seqno = ? ";
        sqlCmd += "and trans_resp_code = 'Y' ";
        setString(1, hEflgTransSeqno);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hEflgFileName = getValue("file_name", i);
            hEflgRowid = getValue("rowid", i);
            hEflgProcCode = "0";

            hEflgProcDesc = "FTP檔案完成";
            updateEcsFtpLog();
        }

    }

    /***********************************************************************/
    void updateEcsFtpLog() throws Exception {
        daoTable = "ecs_ftp_log";
        updateSQL = "file_date = ?,";
        updateSQL += " proc_code = ?,";
        updateSQL += " proc_desc = ?,";
        updateSQL += " mod_pgm = ?,";
        updateSQL += " mod_time = sysdate";
        whereStr = "where rowid  = ? ";
        setString(1, hEflgFileDate);
        setString(2, hEflgProcCode);
        setString(3, hEflgProcDesc);
        setString(4, javaProgram);
        setRowId(5, hEflgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ecs_ftp_log not found!", "", hCallBatchSeqno);
        }

    }
    
    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String src = String.format("/crdataupload/%s", moveFile);
        String target = String.format("/crdataupload/backup/%s", moveFile);

        comc.fileRename(src, target);
        
    }

    /***********************************************************************/
    void selectSomeday(int hInt) throws Exception {
        int days = comcr.str2int(hTempNotifyD) - comcr.str2int(comc.getSubString(hTfinRunDay, hInt, 1));
        sqlCmd = "select to_char(to_date(?,'yyyymmdd')- ? days,'yyyymmdd') ";
        sqlCmd += " from DUAL ";
        setString(1, hTnlgNotifyDate);
        setInt(2, days);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_DUAL not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempSomeday = getValue("h_temp_someday");
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsT011 proc = new IpsT011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
