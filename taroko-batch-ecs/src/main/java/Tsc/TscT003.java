/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/27  V1.00.01    Brian     error correction                          *
*  109-11-18  V1.00.02    tanwei    updated for project coding standard       *
*  112-12-19  V1.00.03    Wilson    傳送.rpt檔給卡部                                                                                     *
******************************************************************************/

package Tsc;


import java.nio.file.Paths;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommIps;
import com.CommRoutine;

/*悠遊卡會員銀行檔案回應檔(MBRP)FTP接收處理程式*/
public class TscT003 extends AccessDAO {

    private String progname = "悠遊卡會員銀行檔案回應檔(MBRP)FTP接收處理程式  112/12/19 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommRoutine    comr = null;
    CommCrdRoutine comcr = null;
    CommIps       comips = new CommIps();
    CommFTP commFTP = null;
    
    int debug   = 1;

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
    String hTnlgFileName = "";
    String hTnlgCheckCode = "";
    String hTempRowid = "";
    String hTnlgProcFlag = "";
    int hTnlgRecordCnt = 0;
    int hTnlgRecordSucc = 0;
    int hTnlgRecordFail = 0;

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
    String hEriaLocalDir = "";
    String msgCode = "";
    String msgDesc = "";

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
       comc.errExit("Usage : TscT003 [[notify_date][force_flag]] [force_flag]", "");
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

   tmpstr1 = String.format("MBRQ.%8.8s.%8.8s07", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
   hTempFileName = tmpstr1;
   tmpstr1 = String.format("MBRP.%8.8s.%8.8s07", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
   hTnlgFileName = tmpstr1;

if(debug==1) showLogMessage("I", "", "TEMP 01=["+hTempFileName +"]"+hTnlgFileName);

   if (forceFlag == 0) {
       nRetcode = selectTscNotifyLog();

       if (nRetcode == 1) {
    	   showLogMessage("E","",String.format("[%s]無會員銀行檔案回應檔(MBRQ)送出紀錄(error)",hTempFileName));
       }
       if (nRetcode == 2) {
    	   showLogMessage("E","",String.format("[%s]會員銀行檔案回應檔(MBRP)已有接收紀錄(error)", hTnlgFileName));
       }
   }

   showLogMessage("I", "", String.format("會員銀行通知檔回覆檔[%s]接收處理中..", hTnlgFileName));

   // ======================================================
   // FTP

   CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
   CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要)*/
   commFTP.hEflgSystemId   = "TSC_FTP_GET";      /* 區分不同類的 FTP 檔案-大類     (必要)*/
   commFTP.hEflgGroupId    = "000000";           /* 區分不同類的 FTP 檔案-次分類 (非必要)*/
   commFTP.hEflgSourceFrom = "TSCC_FTP";         /* 區分不同類的 FTP 檔案-細分類 (非必要)*/
   commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
   commFTP.hEflgModPgm     = this.getClass().getName();
   String hEflgRefIpCode  = "TSC_FTP_GET";

   System.setProperty("user.dir", commFTP.hEriaLocalDir);

   String procCode = String.format("get %s", hTnlgFileName);
   showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

   int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

   if (errCode != 0) {
       comcr.errRtn(
      String.format("[%s] => msg_code[%s][%s] error\n", tmpstr1, msgCode, msgDesc)
            + String.format("TSCC [%s]會員銀行檔案回應檔(MBRP)接收有誤(error), 請通知相關人員處理" , hTnlgFileName), "", hCallBatchSeqno);
   } else {
	   String fs = Paths.get(commFTP.hEriaRemoteDir, hTnlgFileName).toString();
       String ft = Paths.get(commFTP.hEriaRemoteDir, "backup", hTnlgFileName).toString();
       String cmdStr = String.format("mv -i -f %s %s", fs, ft);

       showLogMessage("I", "", "備份遠端檔案: mv 檔案指令=" + cmdStr);

       if (comc.fileMove(fs, ft) == false) {
           showLogMessage("E", "", "ERROR : mv 檔案指令=" + cmdStr);
       }
   }
	   
   // ==================================================

   deleteTscNotifyLog();
   insertTscNotifyLog();

   fileOpen1();
   fileOpen2();
   updateTscNotifyLog();
   selectTscNotifyLog();

   String rootDir = String.format("%s/media/tsc", comc.getECSHOME());
   rootDir  = Normalizer.normalize(rootDir, java.text.Normalizer.Form.NFKD);
   tmpstr1   = String.format("MBRP.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
   String fs = String.format("%s/%s", rootDir, tmpstr1);
   tmpstr2   = String.format("%s/backup/%s", rootDir, tmpstr1);
if(debug==1) showLogMessage("I", "", " BACKUP=["+fs+"] to "+tmpstr2);
   comc.fileRename(fs, tmpstr2);

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
        sqlCmd += " fetch first 1 rows only";
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

        sqlCmd  = "select ftp_send_date, ";
        sqlCmd += " ftp_receive_date, ";
        sqlCmd += " resp_code, ";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where file_name   = ?  ";
        sqlCmd += "  and notify_date = ? ";
        setString(1, hTempFileName);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
if(debug==1) 
  showLogMessage("I", "", " select=["+hTempFileName+"]"+recordCnt+","+hTnlgNotifyDate);
        if (recordCnt > 0) {
            hTnlgFtpSendDate    = getValue("ftp_send_date");
            hTnlgFtpReceiveDate = getValue("ftp_receive_date");
            hTnlgRespCode        = getValue("resp_code");
            hTnlgRowid            = getValue("rowid");
        } else
            return (1);
        if (hTnlgFtpSendDate.length() == 0)
            return (1);
        if ((hTnlgFtpReceiveDate.length() != 0) && (hTnlgRespCode.equals("0000")))
            return (2);
        return (0);
    }

    /***********************************************************************/
    void deleteTscNotifyLog() throws Exception {
        daoTable  = "tsc_notify_log";
        whereStr  = "where notify_date = ?  ";
        whereStr += "  and file_name   = ? ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    void insertTscNotifyLog() throws Exception {
        setValue("file_iden", "MBRP");
        setValue("tran_type", "R");
        setValue("file_name", hTnlgFileName);
        setValue("notify_date", hTnlgNotifyDate);
        setValue("notify_time", hTempNotifyTime);
        setValueInt("notify_seq", 01);
        setValue("ftp_receive_date", hTempNotifyDate);
        setValue("ftp_receive_time", hTempNotifyTime);
        setValue("check_code", "XXXX");
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscNotifyLog() throws Exception {
        daoTable   = "tsc_notify_log";
        updateSQL  = " ftp_receive_date  = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " ftp_receive_time  = to_char(sysdate, 'hh24miss'),";
        updateSQL += " resp_code         = '0000',";
        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where file_name    = ?  ";
        whereStr  += "  and notify_date  = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        setString(3, hTnlgNotifyDate);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", hTnlgNotifyDate, hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    @SuppressWarnings("null")
    void fileOpen1() throws Exception {
        int inta = 0, intb = 0, headTag = 0, tailTag = 0;
        String str600 = "";
        hTnlgRecordCnt = 0;
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            inta++;
if(debug==1) showLogMessage("I", "", "READ=["+comc.getSubString(str600, 0,71)+"]"+inta);
            if (!comc.getSubString(str600, 0, 1).equals("H") && 
                !comc.getSubString(str600, 0, 1).equals("D") &&
                !comc.getSubString(str600, 0, 1).equals("T")) {
                hTnlgRespCode = "0102"; /* FIRST_CHR_ERR */
                updateTscNotifyLog1();
                commitDataBase();
                comcr.errRtn("", "", hCallBatchSeqno);
            }
            if ((!comc.getSubString(str600, 0, 1).equals("H")) && (inta == 1)) {
                hTnlgRespCode = "0112"; /* HEADER_NOT_FOUND */
                updateTscNotifyLog1();
                commitDataBase();
                comcr.errRtn("", "", hCallBatchSeqno);
            }
            if (inta > 500000) {
                hTnlgRespCode = "0103"; /* RECORD_QTY_OVER */
                updateTscNotifyLog1();
                commitDataBase();
                comcr.errRtn("", "", hCallBatchSeqno);
            }

            if (comc.getSubString(str600, 0, 1).equals("D"))
                intb++;
            if (comc.getSubString(str600, 0, 1).equals("H")) {
                if (headTag != 0) {
                    hTnlgRespCode = "0111"; /* HEADER_DUPLICATE */
                    updateTscNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", hCallBatchSeqno);
                }
                headTag = 1;
                if (!"MBRP".equals(comc.getSubString(str600, 1, 1 + 4))) {
                    hTnlgRespCode = "0114"; /* HEADER_FILE_NAME_OVER */
                    updateTscNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", hCallBatchSeqno);
                }
                if (!comc.TSCC_BANK_ID8.substring(0, 8).equals(comc.getSubString(str600, 5, 5 + 8))) {
                    hTnlgRespCode = "0117"; /* HEADER_BANK_NAME_OVER */
                    updateTscNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", hCallBatchSeqno);
                }
                if (!comc.commDateCheck(comc.getSubString(str600, 13))) {
                    hTnlgRespCode = "0115"; /* HEADER_DATE_NAME_OVER */
                    updateTscNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", hCallBatchSeqno);
                }
                if (!comc.commTimeCheck(comc.getSubString(str600, 21))) {
                    hTnlgRespCode = "0116"; /* HEADER_TIME_NAME_OVER */
                    updateTscNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", hCallBatchSeqno);
                }
            }
            if (comc.getSubString(str600, 0, 1).equals("T")) {
                if (tailTag != 0) {
                    hTnlgRespCode = "0121"; /* TAILER_DUPLICATE */
                    updateTscNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", hCallBatchSeqno);
                }
                tailTag = 1;
                tmpstr3 = String.format("%8.8s", comc.getSubString(str600, 1));
                hTnlgRecordCnt = comcr.str2int(tmpstr3);
                if (comcr.str2long(tmpstr3) != intb) {
                    hTnlgRespCode = "0124"; /* TOTAL_QTY_ERR */
                    updateTscNotifyLog1();
                    commitDataBase();
                    comcr.errRtn("", "", hCallBatchSeqno);
                }
            }
        }

        closeInputText(br);
        if (headTag != 1) {
            hTnlgRespCode = "0112"; /* HEADER_NOT_FOUND */
            updateTscNotifyLog1();
            commitDataBase();
            comcr.errRtn("", "", hCallBatchSeqno);
        }
        if (tailTag != 1) {
            hTnlgRespCode = "0120"; /* TAILER_NOT_FOUND */
            updateTscNotifyLog1();
            commitDataBase();
            comcr.errRtn("", "", hCallBatchSeqno);
        }
        hTnlgRespCode = "0000";
        updateTscNotifyLog1();
    }

    /***********************************************************************/
    void updateTscNotifyLog1() throws Exception {
        daoTable   = "tsc_notify_log";
        updateSQL  = " check_code    = ?,";
        updateSQL += " record_cnt    = ?,";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_time      = sysdate";
        whereStr   = "where file_name  = ? ";
        setString(1, hTnlgRespCode);
        setInt(2, hTnlgRecordCnt);
        setString(3, javaProgram);
        setString(4, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
        	showLogMessage("E","",String.format("update_tsc_notify_log not found!,fileName=[%s]", hTnlgFileName));
        }

        if(!hTnlgRespCode.equals("0000"))
          {
           showLogMessage("I","","TSCC 會員銀行通知檔回覆檔[%s]"+ hTnlgRespCode);
           showLogMessage("I","",String.format("TSCC [%s]會員銀行通知檔回覆檔[%s]有錯(error), 請通知相關人員處理" ,hTnlgNotifyDate, hTnlgFileName));
          }
    }

    /***********************************************************************/
    void fileOpen2() throws Exception {
        String str600 = "";
        tmpstr1 = String.format("%s", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            hTnlgCheckCode = "";
            if ((comc.getSubString(str600, 0, 1).equals("H")) || 
                (comc.getSubString(str600, 0, 1).equals("T")))
                continue;
            tmpstr1 = String.format("%24.24s", comc.getSubString(str600, 3));
            hTempFileName = tmpstr1;
            tmpstr1 = String.format("%1.1s", comc.getSubString(str600, 27));
            hTnlgProcFlag = tmpstr1;
            tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 28));
            hTnlgRecordSucc = comcr.str2int(tmpstr1);
            tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 36));
            hTnlgRecordFail = comcr.str2int(tmpstr1);
            tmpstr1 = String.format("%4.4s", comc.getSubString(str600, 60));
            hTnlgRespCode = tmpstr1;

            // ======================================================
            // FTP

            CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
            CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

            commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用    (必要) */
            commFTP.hEflgSystemId   = "TSC_FTP_GET";     /* 區分不同類的 FTP 檔案-大類     (必要) */
            commFTP.hEflgGroupId    = "000000";          /* 區分不同類的 FTP 檔案-次分類 (非必要) */
            commFTP.hEflgSourceFrom = "TSCC_FTP";        /* 區分不同類的 FTP 檔案-細分類 (非必要) */
            commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
            commFTP.hEflgModPgm     = this.getClass().getName();
            String hEflgRefIpCode  = "TSC_FTP_GET";

            System.setProperty("user.dir", commFTP.hEriaLocalDir);

            String procCode = String.format("get %s.RPT", hTempFileName);
            showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載報表檔....");

            int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

            if (errCode != 0) {
                hTnlgCheckCode = "0105"; /* FILE_NOT_FOUND */
                updateTscNotifyLog1();
                updateTscNotifyLog2();
                commitDataBase();
                comcr.errRtn(String.format("[%s] => msg_code[%s][%s] error", tmpstr1, msgCode, msgDesc), "", hCallBatchSeqno);
            } else {
            	String fs = Paths.get(commFTP.hEriaRemoteDir, (hTempFileName+".RPT")).toString();
                String ft = Paths.get(commFTP.hEriaRemoteDir, "backup", (hTempFileName+".RPT")).toString();
                String cmdStr = String.format("mv -i -f %s %s", fs, ft);

                showLogMessage("I", "", "備份遠端檔案: mv 檔案指令=" + cmdStr);

                if (comc.fileMove(fs, ft) == false) {
                    showLogMessage("E", "", "ERROR : mv 檔案指令=" + cmdStr);
                }
                
                procFTP(hTempFileName+".RPT");
            }
            	
            // ==================================================
            if (selectTscNotifyLoga() != 0) {
                showLogMessage("I", "", String.format("回應檔案[%s]無傳送紀錄(error)", hTempFileName));
                continue;
            }
            tmpstr1 = String.format("%s/media/tsc", comc.getECSHOME());
            tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
            tmpstr1 = String.format("%44.44s", str600);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
            if (!comc.getSubString(str600, 44, 44 + 16).equals(comc.getSubString(tmpstr2, 0, 16))) {
                /*
                 * str2var(h_tnlg_check_code , "A001"); file hash value error
                 * update_tsc_notify_log_1(); update_tsc_notify_log_2();
                 * db_commit();
                 */
                showLogMessage("I", "", String.format("TSCC 回覆檔[%s] Hash 值有錯, 請通知相關人員處理"
                                      , hTempFileName));
                /*
                 * ECSprintf(stderr," hash[%16.16s]-[%s]\n",str600+44,tmpstr2);
                 * continue;
                 */
            }
            if (hTnlgRecordCnt != hTnlgRecordSucc + hTnlgRecordFail) {
                hTnlgCheckCode = "A002"; /* reveive record count not match send_record!=receive(fail+succ) */
                updateTscNotifyLog2();
                commitDataBase();
                showLogMessage("I", "",String.format("Responese file[%s.RPT] record not match error!"
                                      ,hTempFileName));
                showLogMessage("I", "",String.format("TSCC 回覆檔[%s]資料筆數有錯(error),請通知相關人員處理"
                                      ,hTempFileName));
                showLogMessage("I", "",String.format(" record_cnt[%d]=succ[%d]+fail[%d]"
                                      ,hTnlgRecordCnt, hTnlgRecordSucc, hTnlgRecordFail));
                continue;
            }
            updateTscNotifyLog2();
        }
        closeInputText(br);
    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        sqlCmd = "select rowid rowid ,";
        sqlCmd += " record_cnt ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTempFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempRowid = getValue("rowid");
            hTnlgRecordCnt = getValueInt("record_cnt");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void updateTscNotifyLog2() throws Exception {
        daoTable   = "tsc_notify_log";
        updateSQL  = " check_code         = ?,";
        updateSQL += " resp_code          = ?,";
        updateSQL += " ftp_receive_date   = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " ftp_receive_time   = to_char(sysdate, 'hh24miss'),";
        updateSQL += " record_succ        = ?,";
        updateSQL += " record_fail        = ?,";
        updateSQL += " proc_flag          = '1',";
        updateSQL += " mod_pgm            = ?,";
        updateSQL += " mod_time           = sysdate";
        whereStr = "where rowid           = ? ";
        setString(1, hTnlgCheckCode);
        setString(2, hTnlgRespCode);
        setInt(3, hTnlgRecordSucc);
        setInt(4, hTnlgRecordFail);
        setString(5, javaProgram);
        setRowId(6, hTempRowid);
        updateTable();
        if (notFound.equals("Y")) {
        	showLogMessage("E","",String.format("update_tsc_notify_log not found!,update by rowId"));
            //comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void procFTP(String rptFileName) throws Exception {
    	
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
    	
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "CREDITCARD"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/tsc", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + rptFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("CREDITCARD", "mput " + rptFileName);

        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + rptFileName + " 資料" + " errcode:" + errCode);
            insertEcsNotifyLog(rptFileName);
        }
    }

    /****************************************************************************/
    public int insertEcsNotifyLog(String fileName) throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("unit_code", comr.getObjectOwner("3", javaProgram));
        setValue("obj_type", "3");
        setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_name", "媒體檔名:" + fileName);
        setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_desc2", "");
        setValue("trans_seqno", commFTP.hEflgTransSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ecs_notify_log";

        insertTable();

        return (0);
    }

    /****************************************************************************/
    public static void main(String[] args) throws Exception {
        TscT003 proc = new TscT003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
