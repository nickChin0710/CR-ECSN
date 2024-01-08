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


import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommIps;
import com.CommRoutine;

/*智慧卡公司重新產生檔案FTP接收處理程式*/
public class TscT021 extends AccessDAO {

    private String progname = "智慧卡公司重新產生檔案FTP接收處理程式  109/11/18 V1.00.02";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommIps        comips   = new CommIps();

    String hCallBatchSeqno   = "";
    String hTnlgNotifyDate   = "";
    String hBusiBusinessDate = "";
    String hTnlgRespCode     = "";
    String hTnlgFileName     = "";
    String hTnlgRowid         = "";
    String hTnlgFileIden     = "";
    int    hTnlgNotifySeq    = 0;
    String hTfinFileDesc     = "";
    String hTfinFileIden     = "";
    String hEriaLocalDir     = "";

    int    hTfinRecordLength = 0;
    int    forceFlag           = 0;
    int    totalCnt            = 0;
    String tmpstr               = "";
    String tmpstr1              = "";
    String tmpstr2              = "";
    String temstr2              = "";
    String tmpstr3              = "";
    String fileSeq             = "";
    String temstr1              = "";
    String msgCode             = "";
    String msgDesc             = "";
    String fileName            = "";

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
            if (args.length != 3 && args.length != 4) {
                comc.errExit("Usage : TscT021 file_iden notify_date notify_seq [force_flag]", "");
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
            String sgArgs0 = "";
            sgArgs0 = args[0];
            sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
            hTnlgFileIden = sgArgs0;
            String sgArgs1 = "";
            sgArgs1 = args[1];
            sgArgs1 = Normalizer.normalize(sgArgs1, java.text.Normalizer.Form.NFKD);
            hTnlgNotifyDate = sgArgs1;
            hTnlgNotifySeq = comcr.str2int(args[2]);
            if (args.length == 4)
                if (args[3].equals("Y"))
                    forceFlag = 1;

            tmpstr1 = String.format("%s.%8.8s.%8.8s%02d", hTnlgFileIden, comc.TSCC_BANK_ID8, hTnlgNotifyDate,
                    hTnlgNotifySeq);
            hTnlgFileName = tmpstr1;

            selectPtrBusinday();

            if (forceFlag == 1)
                deleteTscNotifyLog();

            if (selectTscNotifyLogb() == 0) {
                comcr.errRtn(String.format("檔案[%s]重覆接收", hTnlgFileName), "", hCallBatchSeqno);
            }

            getIdenFile();
            insertTscNotifyLog();
            tmpstr1 = commTSCCFileCheck(hTnlgFileName, hTnlgFileIden, tmpstr1);
            hTnlgRespCode = tmpstr1;
            updateTscNotifyLog();
            if (!hTnlgRespCode.equals("0000")) {
                commitDataBase();
                showLogMessage("I", "", String.format("TSCC file[%s] error_code[%s]!", hTnlgFileName, hTnlgRespCode));
            }

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
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select business_date,";
        sqlCmd += " decode(cast(? as varchar(8)), '', to_char(sysdate,'yyyymmdd'), ?) h_tnlg_notify_date ";
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
            hTnlgNotifyDate = getValue("h_tnlg_notify_date");
        }

    }

    /***********************************************************************/
    void deleteTscNotifyLog() throws Exception {
        daoTable  = "tsc_notify_log";
        whereStr  = "where file_name = ?  ";
        whereStr += "  and tran_type = 'O' ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    int selectTscNotifyLogb() throws Exception {
        sqlCmd = "select rowid rowid";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where file_name = ?  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgRowid = getValue("rowid");
        } else
            return (1);
        return (0);
    }
/***********************************************************************/
void getIdenFile() throws Exception 
{
  // ======================================================
  // FTP

  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
  CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
  commFTP.hEflgSystemId   = "TSCC_FTP_OUT";      /* 區分不同類的 FTP 檔案-大類     (必要) */
  commFTP.hEflgGroupId    = "000000";      /* 區分不同類的 FTP 檔案-次分類 (非必要) */
  commFTP.hEflgSourceFrom = "TSCC_FTP";    /* 區分不同類的 FTP 檔案-細分類 (非必要) */
  commFTP.hEriaLocalDir   = String.format("%s/media/tsc", comc.getECSHOME());
  commFTP.hEflgModPgm     = this.getClass().getName();
  String hEflgRefIpCode  = "TSCC_FTP_OUT";

  System.setProperty("user.dir", commFTP.hEriaLocalDir);

  String procCode = String.format("get %s", hTnlgFileName);
  showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始下載....");

  int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

  if(errCode != 0) {
     comcr.errRtn(String.format("[%s] => msg_code[%s][%s] error\n", tmpstr1, msgCode, msgDesc)
       + String.format("[%s]  檔案接收有誤(error), 請通知相關人員處理",hTnlgFileName),"",hCallBatchSeqno);
  }
}
/***********************************************************************/
    void insertTscNotifyLog() throws Exception {
        setValue("file_iden"       , hTnlgFileIden);
        setValue("tran_type"       , "O");
        setValue("file_name"       , hTnlgFileName);
        setValue("notify_date"     , hTnlgNotifyDate);
        setValue("notify_time"     , sysTime);
        setValueInt("notify_seq"   , hTnlgNotifySeq);
        setValue("ftp_receive_date", sysDate);
        setValue("ftp_receive_time", sysTime);
        setValue("check_code"      , "XXXX");
        setValue("perform_flag"    , "Y");
        setValue("mod_pgm"         , javaProgram);
        setValue("mod_time"        , sysDate + sysTime);
        daoTable = "tsc_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    String commTSCCFileCheck(String tmpstr1, String fileIden, String returnCode) throws Exception {
        int int1 = 0;
        int inta = 0;
        int intb = 0;
        int headTag = 0;
        int tailTag = 0;
        String tmpstr4 = "";
        String str600 = "";
        returnCode = String.format("%4.4s", "0000");
        String fileName = String.format("%s", tmpstr1);
        int1 = commTSCCFileNameCheck(fileName, fileIden);
        if (int1 != 0) {
            returnCode = String.format("0104"); /* FILE_NAME_ERR */
            showLogMessage("I", "", String.format("0104 return_code[%d]", int1));
            return returnCode;
        }

        selectTscFileIden1(fileIden);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);

        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

        int f = openInputText(temstr1);
        if (f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }
        closeInputText(f);

        if ((comc.getFileLength(temstr1) % hTfinRecordLength) != 0) {
            returnCode = String.format("0101"); /* FILE_SIZE_ERR */
            return returnCode;
        }
        int file = openInputText(temstr1);
        if (file == -1) {
            returnCode = String.format("0105"); /* FILE_NOT_FOUND */
            return returnCode;
        }
        closeInputText(file);

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            tmpstr4 = String.format(str600);
            if (hTfinRecordLength - 18 >= 0) {
                if (tmpstr4.length() > hTfinRecordLength - 18 + 1)
                    tmpstr4 = comc.getSubString(tmpstr4, 0, hTfinRecordLength - 18) + ""
                            + comc.getSubString(tmpstr4, hTfinRecordLength - 18 + 1);
                if (tmpstr4.length() == hTfinRecordLength - 18 + 1)
                    tmpstr4 = comc.getSubString(tmpstr4, 0, hTfinRecordLength - 18) + "";
            }
            inta++;
            if ((str600.length() + 2) != hTfinRecordLength) {
                returnCode = String.format("0106"); /* RECORD_LEN_ERR */
                break;
            }
            if (!comc.getSubString(str600, 0, 1).equals("H") && !comc.getSubString(str600, 0, 1).equals("D")
                    && !comc.getSubString(str600, 0, 1).equals("T")) {
                returnCode = String.format("0102"); /* FIRST_CHR_ERR */
                break;
            }
            if ((comc.getSubString(str600, 0, 1).equals("H")) && (inta == 1)) {
                returnCode = String.format("0112"); /* HEADER_NOT_FOUND */
                break;
            }
            if (inta > 500000) {
                returnCode = String.format("0103"); /* RECORD_QTY_OVER */
                break;
            }

            if (comc.getSubString(str600, 0, 1).equals("D"))
                intb++;
            if (comc.getSubString(str600, 0, 1).equals("H")) {
                if (headTag != 0) {
                    returnCode = String.format("0111"); /* HEADER_DUPLICATE */
                    break;
                }
                headTag = 1;
                tmpstr3 = new String(comips.commHashUnpack(tmpstr4.getBytes()));
                /*
                 * if (strncmp(tmpstr3,str600+(h_tfin_record_length-18),16)!=0) {
                 * sprintf(return_code , "0113"); HEADER_HASH_OVER break; }
                 */
                if (!comc.getSubString(fileIden, 0, 4).equals(comc.getSubString(str600, 1, 1 + 4))) {
                    returnCode = String.format("0114"); /* HEADER_FILE_NAME_OVER */
                    break;
                }
                if (!comc.TSCC_BANK_ID8.substring(0, 8).equals(comc.getSubString(str600, 5, 5 + 8))) {
                    returnCode = String.format("0117"); /* HEADER_BANK_NAME_OVER */
                    break;
                }
                if (!comc.commDateCheck(comc.getSubString(str600, 13))) {
                    returnCode = String.format("0115"); /* HEADER_DATE_NAME_OVER */
                    break;
                }
                if (!comc.commTimeCheck(comc.getSubString(str600, 21))) {
                    returnCode = String.format("0116"); /* HEADER_TIME_NAME_OVER */
                    break;
                }
            }
            if (comc.getSubString(str600, 0, 1).equals("T")) {
                if (tailTag != 0) {
                    returnCode = String.format("0121"); /* TAILER_DUPLICATE */
                    break;
                }
                tailTag = 1;
                tmpstr3 = new String(comips.commHashUnpack(tmpstr4.getBytes()));
                /*
                 * if (strncmp(tmpstr3,str600+(h_tfin_record_length-18),16)!=0) {
                 * sprintf(return_code , "0123"); TAILER_HASH_OVER break; }
                 */
                tmpstr3 = String.format("%8.8s", comc.getSubString(str600, 1));
                if (comcr.str2long(tmpstr3) != intb) {
                    returnCode = String.format("0124"); /* TOTAL_QTY_ERR */
                    break;
                }
            }
        }
        closeInputText(br);
        if (!returnCode.equals("0000"))
            return returnCode;

        if (headTag != 1) {
            returnCode = String.format("0112"); /* HEADER_NOT_FOUND */
            return returnCode;
        }
        if (tailTag != 1) {
            returnCode = String.format("0122"); /* TAILER_NOT_FOUND */
            return returnCode;
        }
        returnCode = String.format("0000");
        return returnCode;
    }
/***********************************************************************/
int commTSCCFileNameCheck(String fileName, String fileIden) throws Exception 
{ /* 判別檔名是否依TSCC規定 */
        String tmpstr1;

        if ((fileName.toCharArray()[4] != '.') && (fileName.toCharArray()[13] != '.'))
            return (1);
        if (!comc.getSubString(fileName, 0, 4).equals(comc.getSubString(fileIden, 0, 4)))
            return (2);
        if (!comc.getSubString(fileName, 5, 5 + 8).equals(comc.TSCC_BANK_ID8.substring(0, 8)))
            return (3);
        tmpstr1 = String.format("%8.8s", comc.getSubString(fileName, 14));
        if (!comc.commDateCheck(tmpstr1))
            return (4);
        tmpstr1 = String.format("%2.2s", comc.getSubString(fileName, 22));
        if ((comcr.str2int(tmpstr1) < 0) || (comcr.str2int(tmpstr1) > 99))
            return (5);
        return (0);
}
/***********************************************************************/
    void selectTscFileIden1(String tmpstr1) throws Exception {
        hTfinFileIden = tmpstr1;
        hTfinRecordLength = 0;
        hTfinFileDesc = "";

        sqlCmd = "select record_length,";
        sqlCmd += " file_desc ";
        sqlCmd += " from tsc_file_iden  ";
        sqlCmd += "where file_iden = ? ";
        setString(1, hTfinFileIden);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_file_iden not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTfinRecordLength = getValueInt("record_length");
            hTfinFileDesc = getValue("file_desc");
        }

    }

    /***********************************************************************/
    void updateTscNotifyLog() throws Exception {
        daoTable = " tsc_notify_log";
        updateSQL = " check_code  = ?,";
        updateSQL += " proc_flag   = '1',";
        updateSQL += " mod_pgm     = ?,";
        updateSQL += " mod_time    = sysdate ";
        whereStr = "where file_name  = ? ";
        setString(1, hTnlgRespCode);
        setString(2, javaProgram);
        setString(3, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT021 proc = new TscT021();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
