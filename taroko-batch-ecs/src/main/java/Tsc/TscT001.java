/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/23  V1.00.01    Brian     error correction                          *
*  109-11-18  V1.00.02    tanwei    updated for project coding standard       *
*  112/05/17  V1.00.03    Wilson    不重新產檔                                                                                                 *
*  112/12/19  V1.00.04    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;


import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*會員銀行通知檔(MBRQ)所屬媒體檔案產生程式*/
public class TscT001 extends AccessDAO {

    private String progname = "會員銀行通知檔(MBRQ)所屬媒體檔案產生程式   112/12/19 V1.00.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps       comips = new CommIps();

    int debug   = 1;

    String buf = "";
    String hCallBatchSeqno = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTnlgNotifyTime = "";
    String hTfinFileIden = "";
    String hTfinDateType = "";
    String hTfinRunDay = "";
    int hTfinRecordLength = 0;
    int hTempCnt = 0;
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgMediaCreateTime = "";
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
                comc.errExit("Usage : TscT001 [[notify_date][force_flag]] [force_flag]", "");
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

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    comcr.errRtn(String.format("本日會員銀行媒體已產生, 不可再處理!(error)"), hTnlgNotifyDate, hCallBatchSeqno);
                }
            }
            deleteTscNotifyLog();
            selectTscFileIden();

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
        sqlCmd = "select to_char(add_days(to_date(business_date,'yyyymmdd'),1),'yyyymmdd') h_business_date,";
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hTnlgNotifyTime = getValue("h_tnlg_notify_time");
        }

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        sqlCmd = "select 1 cnt ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where notify_date = ?  ";
        sqlCmd += "  and tran_type   = 'I'  ";
        sqlCmd += " fetch first 1 rows only";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempCnt = getValueInt("cnt");
        } else
            return (0);
        return (1);
    }

    /***********************************************************************/
    void deleteTscNotifyLog() throws Exception {
        daoTable = "tsc_notify_log";
        whereStr = "where notify_date = ?  ";
        whereStr += " and tran_type   = 'I' ";
        setString(1, hTnlgNotifyDate);
        deleteTable();

    }

    /***********************************************************************/
    void selectTscFileIden() throws Exception {

        sqlCmd = "select file_iden, ";
        sqlCmd += " date_type, ";
        sqlCmd += " run_day, ";
        sqlCmd += " record_length ";
        sqlCmd += " from tsc_file_iden ";
        sqlCmd += "where tran_type = 'I' ";
        sqlCmd += "  and use_flag  = 'Y' ";
        int cursorIndex = openCursor();

        while (fetchTable(cursorIndex)) {
            hTfinFileIden = getValue("file_iden");
            hTfinDateType = getValue("date_type");
            hTfinRunDay = getValue("run_day");
            hTfinRecordLength = getValueInt("record_length");

            if (comc.getSubString(hTfinDateType, 0, 1).equals("F")) {
                if (!comc.getSubString(hTnlgNotifyDate, 6, 8).equals(hTfinRunDay))
                    continue;
            }
            if (comc.getSubString(hTfinDateType, 0, 1).equals("W")) {
                tmpstr1 = comcr.increaseDays(hTnlgNotifyDate, -1);
                tmpstr2 = comcr.increaseDays(tmpstr1, 1);
                if (!hTnlgNotifyDate.equals(tmpstr2))
                    continue;
            }

            tmpstr1 = String.format("%4.4s.%8.8s.%8.8s01", hTfinFileIden, comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format("處理檔案[%s]", hTnlgFileName));

            temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
            temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
            int f = openInputText(temstr1);
            if (f == -1) {
                showLogMessage("I", "", String.format("      檔案不存在, 產生空檔!(error)"));
                fileOpen2();
            } else {
				closeInputText(f);
                fileOpen1();
                showLogMessage("I", "", String.format("      檔案筆數[%d]", hTnlgRecordCnt));
            }

            insertTscNotifyLog();
            totalCnt++;
        }
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    void fileOpen2() throws Exception {
        int intd;
        temstr2 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr2, "big5");
        if(out == -1)
            comcr.errRtn(temstr2, "檔案開啓失敗！", hCallBatchSeqno);

        if (hTfinRecordLength - 18 >= 0) {
            if (tmpstr1.length() > hTfinRecordLength - 18 + 1)
                tmpstr1 = comc.getSubString(tmpstr1, 0, hTfinRecordLength - 18) + ""
                        + comc.getSubString(tmpstr1, hTfinRecordLength - 18 + 1);
            if (tmpstr1.length() == hTfinRecordLength - 18 + 1)
                tmpstr1 = comc.getSubString(tmpstr1, 0, hTfinRecordLength - 18) + "";
        }

        tmpstr2 = String.format("H%4.4s%8.8s%8.8s%6.6s", hTfinFileIden, comc.TSCC_BANK_ID8
                                , hTnlgNotifyDate, hTnlgNotifyTime);
        if (tmpstr2.length() >= 27) {
            if (tmpstr1.length() >= 27)
                tmpstr1 = comc.getSubString(tmpstr2, 0, 27) 
                        + comc.getSubString(tmpstr1, 27, tmpstr1.length());
            else
                tmpstr1 = comc.getSubString(tmpstr2, 0, 27);
        }

        tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
        buf = String.format("%s%16.16s", tmpstr1, tmpstr2);
        writeTextFile(out, buf + "\n");

        tmpstr1 = String.format("%" + (hTfinRecordLength - 18) + "." + (hTfinRecordLength - 18) + "s", " ");

        if ((hTfinFileIden.equals("STMT")) || (hTfinFileIden.equals("BTRQ"))) {
            tmpstr2 = String.format("T%08d%015.0f%015.0f", 0, 0.0, 0.0);
        } else {
            tmpstr2 = String.format("T%08d", 0);
        }
        intd = tmpstr2.length();
        if (tmpstr1.length() >= intd)
            tmpstr1 = comc.getSubString(tmpstr2, 0, intd) 
                    + comc.getSubString(tmpstr1, intd, tmpstr1.length());
        else
            tmpstr1 = comc.getSubString(tmpstr2, 0, intd);

        tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
        buf = String.format("%s%16.16s", tmpstr1, tmpstr2);
        writeTextFile(out, buf + "\n");
        closeOutputText(out);
        hTnlgRecordCnt = 0;
    }

    /***********************************************************************/
    void fileOpen1() throws Exception {
        int intb = 0, intd = 0;
        String str600 = "";

        String rootDir = String.format("%s/media/tsc", comc.getECSHOME());
        rootDir = Normalizer.normalize(rootDir, java.text.Normalizer.Form.NFKD);
//        tmpstr1 = String.format("%s/%s", rootDir, hTnlgFileName);
        tmpstr2 = String.format("%s", hTnlgFileName);
//        comc.fileDelete(tmpstr2);
//        if(comc.fileRename(tmpstr1, tmpstr2) == false) 
//        {
//         comcr.errRtn(String.format("[%s] rename error",tmpstr1),tmpstr2,hCallBatchSeqno);
//        }
//if(debug==1) showLogMessage("I", "", "  RENAME=["+tmpstr1+"]"+tmpstr2);
//
//        temstr1 = tmpstr2;

        temstr2 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);

        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);

//if(debug==1) showLogMessage("I", "", "  OPEN_1=["+temstr2+"]"+temstr1);
//        out = openOutputText(temstr2, "big5");
//        if(out == -1)
//            comcr.errRtn(temstr2, "檔案開啓失敗！", hCallBatchSeqno);

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

//            if (tmpstr1.length() > str600.length())
//                tmpstr1 = comc.getSubString(str600, 0, str600.length()) 
//                        + comc.getSubString(tmpstr1, str600.length(), tmpstr1.length());
//            else
//                tmpstr1 = str600;
//            if (hTfinRecordLength - 18 >= 0) {
//                if (tmpstr1.length() > hTfinRecordLength - 18 + 1)
//                    tmpstr1 = comc.getSubString(tmpstr1, 0, hTfinRecordLength - 18) + ""
//                            + comc.getSubString(tmpstr1, hTfinRecordLength - 18 + 1);
//                if (tmpstr1.length() == hTfinRecordLength - 18 + 1)
//                    tmpstr1 = comc.getSubString(tmpstr1, 0, hTfinRecordLength - 18) + "";
//            }
//
//            if (!comc.getSubString(str600, 0, 1).equals("H") && 
//                !comc.getSubString(str600, 0, 1).equals("D") && 
//                !comc.getSubString(str600, 0, 1).equals("T"))
//                intd = 1;
//            if (comc.getSubString(str600, 0, 1).equals("H")) {
//                tmpstr1 = String.format("%8.8s", comc.getSubString(str600, 13));
//                hTnlgMediaCreateDate = tmpstr1;
//                tmpstr1 = String.format("%6.6s", comc.getSubString(str600, 21));
//                hTnlgMediaCreateTime = tmpstr1;
//                tmpstr2 = String.format("H%4.4s%8.8s%8.8s%6.6s", hTfinFileIden
//                        , comc.TSCC_BANK_ID8, hTnlgNotifyDate, hTnlgNotifyTime);
//                if (tmpstr2.length() >= 27) {
//                    if (tmpstr1.length() >= 27)
//                        tmpstr1 = comc.getSubString(tmpstr2, 0, 27) 
//                                + comc.getSubString(tmpstr1, 27, tmpstr1.length());
//                    else
//                        tmpstr1 = comc.getSubString(tmpstr2, 0, 27);
//                }
//            }
//            if (comc.getSubString(str600, 0, 1).equals("D"))
                intb++;
//            if (comc.getSubString(str600, 0, 1).equals("T")) {
//                tmpstr2 = String.format("%08d", intb);
//                if (tmpstr2.length() >= 8) {
//                    if (tmpstr1.length() >= 8)
//                        tmpstr1 = comc.getSubString(tmpstr2, 0, 8) 
//                                + comc.getSubString(tmpstr1, 8, tmpstr1.length());
//                    else
//                        tmpstr1 = comc.getSubString(tmpstr2, 0, 8);
//                }
//            }
//            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("big5")));
//            buf = String.format("%s%16.16s", tmpstr1, tmpstr2);
//            writeTextFile(out, buf + "\n");
        }
//        if(br != -1)
//            closeInputText(br);
//        if(out != -1)
//            closeOutputText(out);
        hTnlgRecordCnt = intb;
        if (intd != 0)
            showLogMessage("I", "", String.format("      檔案內有格式錯誤資料!(error)"));
//        tmpstr1 = String.format("%s.BAK", hTnlgFileName);
//        tmpstr2 = String.format("BACKUP/%s.BAK", hTnlgFileName);
//        comc.fileRename(tmpstr1, tmpstr2);
        closeInputText(br);
        
    }

    /***********************************************************************/
    void insertTscNotifyLog() throws Exception {
        setValue("file_iden"     , hTfinFileIden);
        setValue("file_name"     , hTnlgFileName);
        setValue("tran_type"     , "I");
        setValue("notify_date"   , hTnlgNotifyDate);
        setValue("notify_time"   , hTnlgNotifyTime);
        setValueInt("notify_seq" , 1);
        setValue("ftp_send_date" , "");
        setValue("media_crt_date", hTnlgMediaCreateDate);
        setValue("media_crt_time", hTnlgMediaCreateTime);
        setValueInt("record_cnt" , hTnlgRecordCnt);
        setValue("mod_pgm"       , javaProgram);
        setValue("mod_time"      , sysDate + sysTime);
        daoTable = "tsc_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_notify_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT001 proc = new TscT001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/

}
