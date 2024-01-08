/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-15  V1.00.01    tanwei      updated for project coding standard    *
*  112/05/16  V1.00.02    Wilson    調整檔案備份路徑                                                                                       *
*  112/05/17  V1.00.03    Wilson    mark createBackup、不重新產檔                                               *
*  112/07/31  V1.00.04    Wilson    產生.zip.ok檔                                                                                        *
******************************************************************************/

package Ips;


import java.util.Arrays;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*會員銀行通知檔(B2I)所屬媒體檔案產生*/
public class IpsT001 extends AccessDAO {
    private String progname = "會員銀行通知檔(B2I)所屬媒體檔案產生  112/07/31 V1.00.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
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
    String hTnlgFileName1 = "";
    int hTnlgRecordCnt = 0;

    String tmpstr1 = "";
    String tmpstr2 = "";
    String tmpstr3 = "";

    String fileSeq = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int totalCnt = 0;
    String nUserpid = "";
    String tmpstr = "";
    int nRetcode = 0;
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
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IpsT001 [[notify_date][force_flag]] [force_flag]", "");
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
                if (selectIpsNotifyLogA() != 0) {
                    comcr.errRtn("本日會員銀行媒體已產生, 不可再處理!(error)", "", hCallBatchSeqno);
                }
            }
            deleteIpsNotifyLog();
//            createBackup();
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
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tnlg_notify_time ";
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
            hTnlgNotifyTime = getValue("h_tnlg_notify_time");
        }

    }

    /***********************************************************************/
    int selectIpsNotifyLogA() throws Exception {

        sqlCmd = "select 1 cnt ";
        sqlCmd += " from ips_notify_log  ";
        sqlCmd += "where notify_date = ?  ";
        sqlCmd += "and tran_type = 'I'  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }

        return 1;
    }

    /***********************************************************************/
    void deleteIpsNotifyLog() throws Exception {
        daoTable = "ips_notify_log";
        whereStr = "where notify_date = ?  ";
        whereStr += "and  tran_type = 'I' ";
        setString(1, hTnlgNotifyDate);
        deleteTable();

    }

    /***********************************************************************/
//    void createBackup() throws Exception {
//
//        tmpstr1 = String.format("%s/media/ips/backup/%s", comc.getECSHOME(), hTnlgNotifyDate);
//        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
//        
//        comc.mkdirsFromFilenameWithPath(tmpstr1);
//        comc.chmod777(tmpstr1);
//    }

    /***********************************************************************/
    void selectIpsFileIden() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "file_iden,";
        sqlCmd += "date_type,";
        sqlCmd += "run_day,";
        sqlCmd += "record_length ";
        sqlCmd += "from ips_file_iden ";
        sqlCmd += "where tran_type = 'I' ";
        sqlCmd += "and use_flag = 'Y' ";
        openCursor();
        while (fetchTable()) {
            hTfinFileIden = getValue("file_iden");
            hTfinDateType = getValue("date_type");
            hTfinRunDay = getValue("run_day");
            hTfinRecordLength = getValueInt("record_length");

if(debug == 1)
   showLogMessage("I", "", "888 Read iden=" + hTfinFileIden);

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

            tmpstr1 = String.format("%6.6s_%4.4s%8.8s01.dat", hTfinFileIden, comc.IPS_BANK_ID4, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;
            
            tmpstr3 = String.format("%6.6s_%4.4s%8.8s01.zip.ok", hTfinFileIden, comc.IPS_BANK_ID4, hTnlgNotifyDate);
            hTnlgFileName1 = tmpstr3;
            showLogMessage("I", "", String.format("處理檔案=[%s]", hTnlgFileName));

            String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
            temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
            int f = openInputText(temstr1, "MS950");
            if (f == -1) {
                showLogMessage("I", "", String.format("      檔案不存在, 產生空檔!(error)[%s]", temstr1));
                fileOpen2();
            } else {
            	closeInputText(f);
                fileOpen1();
                showLogMessage("I", "", String.format("      檔案筆數=[%d]", hTnlgRecordCnt));
            }
            
            fileOpen3();

            insertIpsNotifyLog();
            totalCnt1++;
        }
        closeCursor();
    }

    /***********************************************************************/
    void insertIpsNotifyLog() throws Exception {
        setValue("file_iden", hTfinFileIden);
        setValue("file_name", hTnlgFileName);
        setValue("tran_type", "I");
        setValue("notify_date", hTnlgNotifyDate);
        setValue("notify_time", hTnlgNotifyTime);
        setValueInt("notify_seq", 1);
        setValue("media_create_date", sysDate);
        setValue("media_create_time", sysTime);
        setValueInt("record_cnt", hTnlgRecordCnt);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ips_notify_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_notify_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen2() throws Exception {
        String temstr2 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        comc.mkdirsFromFilenameWithPath(temstr2);
        int out = openOutputText(temstr2, "MS950");

        byte[] tmpBytes = new byte[580];
        Arrays.fill(tmpBytes, (byte) 32);

        tmpstr2 = String.format("H%6.6s_", hTfinFileIden);
        byte[] tmpBytes2 = tmpstr2.getBytes();
        for (int intc = 0; intc < tmpBytes2.length; intc++)
            tmpBytes[intc] = tmpBytes2[intc];
        writeTextFile(out, new String(tmpBytes) + "\r\n");

        tmpBytes = new byte[hTfinRecordLength - 2];
        Arrays.fill(tmpBytes, (byte) 32);

        tmpstr2 = String.format("T%06d", 0);
        if ((hTfinFileIden.equals("STMT")) || (hTfinFileIden.equals("BTRQ"))) {
            tmpstr2 = String.format("T%06d%015.0f%015.0f", 0, 0, 0);
        }
        tmpBytes2 = tmpstr2.getBytes();
        for (int intc = 0; intc < tmpBytes2.length; intc++)
            tmpBytes[intc] = tmpBytes2[intc];
        writeTextFile(out, tmpstr2 + "\r\n");

        closeOutputText(out);
        hTnlgRecordCnt = 0;
    }

    /***********************************************************************/
    void fileOpen1() throws Exception {
        int intb = 0, intd = 0;

        String root = String.format("%s/media/ips", comc.getECSHOME());

        root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

//        tmpstr1 = String.format("%s", hTnlgFileName);
        tmpstr2 = String.format("%s", hTnlgFileName);
//        String src = String.format("%s/%s", root, tmpstr1);
        String target = String.format("%s/%s", root, tmpstr2);
//        comc.fileDelete(target);
//        comc.fileMove(src, target);

        String temstr2 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
//        int out = openOutputText(temstr2, "MS950");

//        int f = openInputText(target, "MS950");
//        if (f == -1) {
//            comcr.errRtn("檔案不存在：" + target, "", hCallBatchSeqno);
//        }
//        closeInputText(f);
        String buffer = "";
        int br = openInputText(target, "MS950");
        while (true) {
            String str600 = readTextFile(br).trim();
//            byte[] str600_byte = str600.getBytes("MS950");
            if (endFile[br].equals("Y")) break;

//            tmpstr1 = String.format("%580.580s", " ");
//            
//            tmpstr1 = new String(comc.subArray(str600_byte, 0, str600_byte.length), "MS950") + 
//                    new String(comc.subArray(tmpstr1.getBytes("MS950"), str600_byte.length, hTfinRecordLength-str600_byte.length));
//            
//            if (!comc.getSubString(str600, 0, 1).equals("H") && 
//                !comc.getSubString(str600, 0, 1).equals("D") && 
//                !comc.getSubString(str600, 0, 1).equals("T"))
//                intd = 1;
//            if (comc.getSubString(str600, 0, 1).equals("H")) {
//                tmpstr2 = String.format("H%6.6s_", hTfinFileIden);
//
//                tmpstr1 = tmpstr2;
//            }
//            if (comc.getSubString(str600, 0, 1).equals("D"))
                intb++;
//            if (comc.getSubString(str600, 0, 1).equals("T")) {
//                tmpstr2 = String.format("%06d", intb);
//
//                tmpstr1 = tmpstr1.substring(0,1) + tmpstr2 + tmpstr1.substring(7, tmpstr1.length());
//            } 
//            writeTextFile(out, tmpstr1 + "\r\n");
        }
//        closeInputText(br);
//        closeOutputText(out);
        hTnlgRecordCnt = intb;

        if (intd != 0)
            showLogMessage("I", "", "      檔案內有格式錯誤資料!(error)");

//        String src = String.format("%s/%s", root, hTnlgFileName);
//        target = String.format("%s/backup/%s/%s", root, hTnlgNotifyDate, hTnlgFileName);
//        comc.fileCopy(src, target);
        closeInputText(br);

    }

    /***********************************************************************/
    void fileOpen3() throws Exception {
        String temstr2 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName1);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        comc.mkdirsFromFilenameWithPath(temstr2);
        int out = openOutputText(temstr2, "MS950");

        closeOutputText(out);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsT001 proc = new IpsT001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
