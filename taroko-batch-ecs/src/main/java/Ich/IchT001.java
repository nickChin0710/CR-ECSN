/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    Edson     program initial                           *
 *  109-12-16   V1.00.01    tanwei      updated for project coding standard    *
 *  112/05/17  V1.00.02    Wilson    mark createBackup、不重新產檔                                               *
 *  112/05/30  V1.00.03    Wilson    mark B05B、B95B，由資訊部產生                                                   *
 ******************************************************************************/

package Ich;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.IchEncrypt;

/*會員銀行通知檔(B2I)所屬媒體檔案產生*/
public class IchT001 extends AccessDAO {
    private String progname = "特約機構通知檔(BnnB)所屬媒體檔案產生  112/05/30 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;

    int    debug = 1;
    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTnlgNotifyTime = "";
    String hTfinFileIden = "";
    String hTfinDateType = "";
    String hTfinRunDay = "";
    String hTfinRecordLength = "";
    int hTempCnt = 0;
    String hTnlgFileName = "";
    int hTnlgRecordCnt = 0;

    String tmpstr1 = "";
    String tmpstr2 = "";
    String tmpstr3 = "";

    String fileSeq = "";
    int forceFlag = 0;
    int totCnt    = 0;
    int succCnt   = 0;
    int totalCnt  = 0;
    String nUserpid = "";
    String tmpstr  = "";
    String hHash  = "";
    int nRetcode  = 0;
    int totalCnt1 = 0;

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
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchT001 [[notify_date][force_flag]] [force_flag]", "");
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
                if (selectIchNotifyLogA() != 0) {
                    comcr.errRtn("本日會員銀行媒體已產生, 不可再處理!(error)",hTnlgNotifyDate
                            , comcr.hCallBatchSeqno);
                }
            }
            deleteIchNotifyLog();
//            create_backup();
            selectIchFileIden();

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

        sqlCmd  = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTnlgNotifyDate = getValue("h_tnlg_notify_date");
            hTnlgNotifyTime   = getValue("h_tnlg_notify_time");
        }
        if(debug==1)
            showLogMessage("I", ""," DATE_TIME=["+hBusiBusinessDate+"]"+hTnlgNotifyTime);

    }

    /***********************************************************************/
    int selectIchNotifyLogA() throws Exception {

        sqlCmd  = "select 1 cnt ";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where notify_date = ?  ";
        sqlCmd += "   and tran_type   = 'I'  ";
        sqlCmd += "   and FILE_NAME not like 'FC%' ";
        sqlCmd += "   fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }

        return 1;
    }

    /***********************************************************************/
    void deleteIchNotifyLog() throws Exception {
        daoTable  = "ich_notify_log";
        whereStr  = "where notify_date = ?  ";
        whereStr += "  and tran_type   = 'I' ";
        whereStr += "  and FILE_NAME not like 'FC%' ";
        setString(1, hTnlgNotifyDate);
        deleteTable();

    }

    /***********************************************************************/
//    void create_backup() throws Exception {
//
//        tmpstr1 = String.format("%s/media/ich/BACKUP/%s", comc.GetECSHOME(), h_tnlg_notify_date);
//        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
//        File file = new File(tmpstr1);
//
//if(DEBUG==1) showLogMessage("I", "","CREATE BACK=["+tmpstr1+"]");
//
//        file.getParentFile().mkdirs();
//        comc.chmod777(tmpstr1);
//    }
    /***********************************************************************/
    void selectIchFileIden() throws Exception {
        sqlCmd  = "select ";
        sqlCmd += "file_iden,";
        sqlCmd += "date_type,";
        sqlCmd += "run_day,";
        sqlCmd += "record_length ";
        sqlCmd += "  from ich_file_iden ";
        sqlCmd += " where tran_type = 'I' ";
        sqlCmd += "   and use_flag  = 'Y' ";
        openCursor();
        while (fetchTable()) {
            hTfinFileIden     = getValue("file_iden");
            hTfinDateType     = getValue("date_type");
            hTfinRunDay       = getValue("run_day");
            hTfinRecordLength = getValue("record_length");
            if(debug==1)
                showLogMessage("I","","Read file=["+hTfinFileIden+"]"+hTfinDateType+","
                        +hTfinRecordLength);

            if (hTfinDateType.trim().equals("F")) {
                if (!hTnlgNotifyDate.substring(6, 8).equals(hTfinRunDay))
                    continue;
            }
            if (hTfinDateType.trim().equals("W")) {
                tmpstr1 = comcr.increaseDays(hTnlgNotifyDate, -1);
                tmpstr2 = comcr.increaseDays(tmpstr1, 1);
                if (!hTnlgNotifyDate.equals(tmpstr2))
                    continue;
            }

            // Encrypted Session Key
            if(hTfinFileIden.equals("B05B") || hTfinFileIden.equals("B95B") ||
                    hTfinFileIden.equals("B02B") )
                continue;

            tmpstr1 = String.format("BRQA_%3.3s_%8.8s_%4.4s",comc.ICH_BANK_ID3,hTnlgNotifyDate
                    ,hTfinFileIden);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format("處理檔案=[%s]", hTnlgFileName));

            String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(),hTnlgFileName);
            temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
            int f = openInputText(temstr1);
            if (f == -1) {
                showLogMessage("I", "", String.format("      檔案不存在, 產生空檔!(error)[%s]"
                        , temstr1));
                fileOpen2();
            } else {
                closeInputText(f);
                fileOpen1();
                showLogMessage("I", "", String.format("      檔案筆數=[%d]", hTnlgRecordCnt));
            }

            insertIchNotifyLog();
            totalCnt1++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void insertIchNotifyLog() throws Exception {
        setValue("file_iden"        , hTfinFileIden);
        setValue("file_name"        , hTnlgFileName);
        setValue("tran_type"        , "I");
        setValue("notify_date"      , hTnlgNotifyDate);
        setValue("notify_time"      , hTnlgNotifyTime);
        setValueInt("notify_seq"    , 1);
        setValue("media_create_date", sysDate);
        setValue("media_create_time", sysTime);
        setValueInt("record_cnt"    , hTnlgRecordCnt);
        setValueInt("record_succ"   , hTnlgRecordCnt);
        setValue("mod_pgm"          , javaProgram);
        setValue("mod_time"         , sysDate + sysTime);
        daoTable = "ich_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "",comcr.hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void insertIchNotifyLogA(String fileIden) throws Exception {
        setValue("file_iden"        , fileIden);
        setValue("file_name"        , hTnlgFileName.substring(0,20) + "5B");
        setValue("tran_type"        , "I");
        setValue("notify_date"      , hTnlgNotifyDate);
        setValue("notify_time"      , hTnlgNotifyTime);
        setValueInt("notify_seq"    , 1);
        setValue("media_create_date", sysDate);
        setValue("media_create_time", sysTime);
        setValueInt("record_cnt"    , 1);
        setValueInt("record_succ"   , 1);
        setValue("mod_pgm"          , javaProgram);
        setValue("mod_time"         , sysDate + sysTime);
        daoTable = "ich_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
//          comcr.err_rtn("insert_" + daoTable + " duplicate!", "",comcr.h_call_batch_seqno);
        }

    }
    /***********************************************************************/
    void fileOpen2() throws Exception
    {
        if(debug==1) showLogMessage("I", ""," file_open_2=["+hTfinFileIden+"]"+hTfinDateType);

        String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        int out = openOutputText(temstr2, "MS950");
        if (out == -1) {
            comcr.errRtn("檔案失敗：" + temstr2, "", comcr.hCallBatchSeqno);
        }

        byte[] tmpBytes = new byte[200];
        Arrays.fill(tmpBytes, (byte) 32);

        // 01:要求電文、02:回答電文      0001:SFTP   A:整檔提供 B:逐筆匯入
        //h_hash = comc.encryptSHA("" , "SHA-1");
        hHash = "0000000000000000000000000000000000000000";

        //    1,    2,    3,    4,     5,     6,    7,    8,    9,   10,    95,   96
        // 91-8, 81-8, 63-8, 63-8, 275-4, 420-8, 63-8, 55-0, 63-8, 63-8, 275-4, 85-8
        switch (hTfinFileIden)
        {
            case "B01B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%28.28s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000A"," ",hHash);
                break;
// case "B02B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%18.18s%40.40s" ,
//                       h_tfin_file_iden,"01","0001",comc.ICH_BANK_ID3,"00000000B"," ",h_hash);
//             break;
            case "B03B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000A",hHash);
                break;
            case "B04B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000B",hHash);
                break;
            // Encrypted Session Key
            case "B06B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%537.537s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000B"," ",hHash);
                writeEncript( 5 , hTnlgFileName.substring(0,19) + "05B" );
                break;
            case "B07B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000B",hHash);
                break;
            case "B08B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%1.1s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"A",hHash);
                break;
            case "B09B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000B",hHash);
                break;
            case "B10B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000A",hHash);
                break;
            // Encrypted Session Key
            case "B95B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%5.5s%256.256s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"0000A",hHash);
                break;
            case "B96B":tmpstr2 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%22.22s%40.40s" ,
                    hTfinFileIden,"01","0001",comc.ICH_BANK_ID3,"00000000A"," ",hHash);
                writeEncript(95,  hTnlgFileName.substring(0,19) + "95B" );
                break;
        }

        if(debug==1) showLogMessage("I", ""," 8888 tmpstr2=["+tmpstr2+"]"+tmpstr2.length());
        writeTextFile(out, tmpstr2 + "\r\n");

        closeOutputText(out);

// move_backup(h_tnlg_file_name);

//   insert_ich_notify_log_a(h_tnlg_file_name.substring(18,20) + "5B");

        hTnlgRecordCnt = 0;
    }
    /***********************************************************************/
    void fileOpen1() throws Exception
    {
        int intb = 0, intd = 0;

        String root = String.format("%s/media/ich", comc.getECSHOME());
        root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

//        tmpstr1 = String.format("%s", hTnlgFileName);
        tmpstr2 = String.format("%s", hTnlgFileName);
//        String src    = String.format("%s/%s", root, tmpstr1);
        String target = String.format("%s/%s", root, tmpstr2);
//        comc.fileDelete(target);
//        comc.fileRename(src, target);

        String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        String outFilename = temstr2;

        String buffer   = "";
        String allData = "";
        int fileCnt = 0;

        lpar.clear();
        int br = openInputText(target, "MS950");
        if (br == -1) {
            comcr.errRtn("檔案不存在：" + target, "",comcr.hCallBatchSeqno);
        }
        while (true) {
            buffer = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            fileCnt++;
//            byte[] str600 = buffer.getBytes("MS950");
//            for (int intc = str600.length - 1; intc >= 0; intc--)
//                if (comc.byteToUnsignedInt(str600[intc]) < 20) {
//                    str600[intc] = 0x0;
//                }
//
//            if (str600[0] != (byte) 'H' && str600[0] != (byte) 'D')
//                intd = 1;
//            if (str600[0] == (byte) 'D') {
                intb++;
//                if(hTfinFileIden.equals("B06B") || hTfinFileIden.equals("B96B") )
//                {
//                    allData += new String(str600, "MS950") + "\r\n";
//                }
//                else
//                {
//                    allData += new String(str600, "MS950") + "\r\n";
//                }
//            }
//            lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", new String(str600, "MS950") + "\r\n"));
        }
//        closeInputText(br);
//
//        String headstr = "";
//        if(debug==1)
//            showLogMessage("I", ""," 8888 open_1 ALL LEN=["+allData.getBytes("MS950").length+"]");
//
//        if(fileCnt==1)
//            hHash = "0000000000000000000000000000000000000000";
//        else
//            hHash = comc.encryptSHA(allData, "SHA-1", "big5");
//
//        switch (hTfinFileIden)
//        {
//            case "B01B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%28.28s%40.40s", hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "A", " ", hHash);
//                break;
//            case "B02B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%18.18s%40.40s", hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "B", " ", hHash);
//                break;
//            case "B03B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s"       , hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "A", hHash);
//                break;
//            case "B04B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s"       , hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "B", hHash);
//                break;
//            // Encrypted Session Key
//            case "B06B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%537.537s%40.40s", hTfinFileIden,"01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "B", " ", hHash);
//                break;
//            case "B07B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s"       , hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "B", hHash);
//                break;
//            case "B08B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%1.1s%40.40s"       , hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, "A", hHash);
//                break;
//            case "B09B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s"       , hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "B", hHash);
//                break;
//            case "B10B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%40.40s"       , hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "A", hHash);
//                break;
//            // Encrypted Session Key
//            case "B95B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%5.5s%256.256s"     , hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, "0000A", hHash);
//                break;
//            case "B96B":
//                headstr = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%22.22s%40.40s", hTfinFileIden, "01"
//                        , "0001", comc.ICH_BANK_ID3, String.format("%08d", intb) + "B", " ", hHash);
//                break;
//        }
//        lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", headstr));
//        comc.writeReport(outFilename, lpar, "big5", false);
        hTnlgRecordCnt = intb;

        if (intd != 0)
            showLogMessage("I", "", "      檔案內有格式錯誤資料!(error)");
        
        closeInputText(br);

        /** write B05B & B95B **/
//        if(hTfinFileIden.equals("B06B") || hTfinFileIden.equals("B96B") ) {
//            String filename = String.format("%s/media/ich/%s", comc.getECSHOME(),
//                    hTnlgFileName.substring(0,20) + "5B" );
//            if(debug==1)
//                showLogMessage("I", ""," 8888 filename=["+filename+"]"+hTnlgFileName);
//            int out = openOutputText(filename, "MS950"); /* B05B & B95B */
//
//            String temstr3 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
//            temstr3 = Normalizer.normalize(temstr3, java.text.Normalizer.Form.NFKD);
//            if(openBinaryInput(temstr3) == false)/* B06B & B96B */
//                comcr.errRtn("檔案不存在", temstr3, comcr.hCallBatchSeqno);
//            long recSize = comc.getFileLength(temstr3); /* 檔案原始長度 */
//            byte[] readbytes = new byte[(int) recSize];
//            readBinFile(readbytes);
//
//            IchEncrypt ichEncrypt = new IchEncrypt();
//            byte[] aesEncrypted = ichEncrypt.aesEncrypt(readbytes); /* AES128 encrypt B06B & B96B */
//            byte[] sessionKey = ichEncrypt.getSessionKey();
//            String encryptedSessionKey = ichEncrypt.rsaEncrypt(sessionKey); /* RSA encrypt session_key */
//            closeBinaryInput();
//            readbytes = null;
//            /* delete B06B & B96B and rewrite AESencrypted */
//            comc.fileDelete(String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName));
//
//            openBinaryOutput(String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName));
//            writeBinFile(aesEncrypted, aesEncrypted.length);
//            closeBinaryOutput();
//
//            hHash = comc.encryptSHA(aesEncrypted, "SHA-1");
//            /* write B05B & B95B header */
//            writeTextFile(out, String.format("H%4.4s%2.2s%4.4s%3.3s%5.5s%256.256s\r\n", hTfinFileIden.substring(0,2) + "5B", "01", "0001", comc.ICH_BANK_ID3, "0001A", encryptedSessionKey));
//
//            /* write B05B & B95B data */
//            writeTextFile(out, String.format("D%-30.30s%08d%08d%40.40s%188.188s\r\n", hTnlgFileName, recSize, aesEncrypted.length, hHash, " "));
//            closeOutputText(out);
//
//            insertIchNotifyLogA(hTfinFileIden.substring(0,2) + "5B");
//        }
        /** end B05B & B95B **/

//move_backup(h_tnlg_file_name);

    }
    /***********************************************************************/
//    void moveBackup(String moveFile) throws Exception
//    {
//        String root   = String.format("%s/media/ich", comc.getECSHOME());
//        String src    = String.format("%s/%s.BAK", root, moveFile);
//        String target = String.format("%s/BACKUP/%s/%s.BAK", root, hTnlgNotifyDate, moveFile);
//
//        if(debug==1) showLogMessage("I", "","MOVE_BACK=["+src+"]"+target);
//
//        comc.fileRename(src, target);
//    }
    /***********************************************************************/
    void writeEncript(int idx ,   String addFile) throws Exception
    {
        hHash = comm.fillZero(""+0  , 256);
        switch (idx)
        {
            case  5 :tmpstr3 = String.format("H%4.4s%2.2s%4.4s%3.3s%5.5s%-256.256s" ,
                    "B05B","01","0001",comc.ICH_BANK_ID3,"0000A",hHash);
                break;
            case 95 :tmpstr3 = String.format("H%4.4s%2.2s%4.4s%3.3s%5.5s%-256.256s" ,
                    "B95B","01","0001",comc.ICH_BANK_ID3,"0000A",hHash);
                break;
        }

        if(debug==1)
            showLogMessage("I", ""," write_add=["+idx+"]"+addFile+","+tmpstr3);

        String temstr3 = String.format("%s/media/ich/%s", comc.getECSHOME(), addFile);
        temstr3 = Normalizer.normalize(temstr3, java.text.Normalizer.Form.NFKD);

        int outAdd = openOutputText(temstr3, "MS950");
        if (outAdd != -1) {
            writeTextFile(outAdd, tmpstr3 + "\r\n");

            closeOutputText(outAdd);
        }

        insertIchNotifyLogA(hTnlgFileName.substring(18,20) + "5B");

    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception
    {
        IchT001 proc = new IchT001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
/***********************************************************************/
}
