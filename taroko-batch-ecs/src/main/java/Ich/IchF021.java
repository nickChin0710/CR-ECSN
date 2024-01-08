/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/03  V1.00.00    Brian     program initial                           *
 *  109-12-16   V1.00.01  tanwei      updated for project coding standard      *
 ******************************************************************************/

package Ich;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import javax.xml.stream.events.EndDocument;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchF021 extends AccessDAO {
    private String progname = "店主檔(A01B)接收處理  109/12/16 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTempNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hTnlgPerformFlag = "";
    String hTnlgCheckCode = "";
    String hTnlgProcFlag = "";
    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hIcgeCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hTnlgNotifyDate = "";

    String tmpstr1 = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int hTnlgRecordCnt = 0;
    int totalCnt = 0;
    String tmpstr = "";

    Buf1 dtl = new Buf1();

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            //       if (comm.isAppActive(javaProgram)) {
            //           comc.err_exit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            //        }
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchF021 [[notify_date][fo1yy_flag]] [force_flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTempNotifyDate = args[0];
                if (args[0].length() == 2) {
                    showLogMessage("I", "", "參數(一) 不可兩碼");
                }
            }
            if (args.length == 2) {
                hTempNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();

            tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A01B", comc.ICH_BANK_ID3, hTempNotifyDate);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s] Force_flag=[%d]", tmpstr1, forceFlag));

            if (forceFlag == 0) {
                if (selectIchNotifyLogA() != 0) {
                    updateIchNotifyLogA();
                    commitDataBase();
                    return (0);
                }
            }

            deleteIchOrgdataLog();

            fileOpen();

            updateIchNotifyLogA();

            showLogMessage("I", "",
                    String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));

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
        hTempNotifyDate = hTempNotifyDate.length() == 0 ? sysDate : hTempNotifyDate;
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSystemDate = getValue("h_temp_system_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

    }

    /***********************************************************************/
    int selectIchNotifyLogA() throws Exception {
        /* proc_flag = 0:收檔中 1: 已收檔 2: 已處理 3: 已回應 */
        hTnlgPerformFlag = "";
        hTnlgNotifyDate = "";
        hTnlgCheckCode = "";
        hTnlgProcFlag = "";
        hTnlgRowid = "";

        sqlCmd = "select perform_flag,";
        sqlCmd += "notify_date,";
        sqlCmd += "check_code,";
        sqlCmd += "proc_flag,";
        sqlCmd += "rowid as rowid1";
        sqlCmd += " from ich_notify_log  ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn(String.format("未有[%s]檔案記錄 , 請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTnlgPerformFlag = getValue("perform_flag");
            hTnlgNotifyDate = getValue("notify_date");
            hTnlgCheckCode = getValue("check_code");
            hTnlgProcFlag = getValue("proc_flag");
            hTnlgRowid = getValue("rowid1");
        }

        if (!hTnlgPerformFlag.equals("Y")) {
            String stderr = String.format("通知檔收檔發生問題,[%s]暫不可處理 ,請通知相關人員處理(error)", hTnlgFileName);
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.equals("0")) {
            String stderr = String.format("通知檔收檔中[%s] , 請通知相關人員處理(error)", hTnlgFileName);
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.toCharArray()[0] >= '2') {
            String stderr = String.format("[%s]檔案已處理過,請通知相關人員處理(error)", hTnlgFileName);
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "", String.format("[%s]檔案整檔處理失敗  , 錯誤代碼[%s]", hTnlgFileName, hTnlgCheckCode));
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void updateIchNotifyLogA() throws Exception {
        daoTable = "ich_notify_log";
        updateSQL = "proc_flag  = '2',";
        updateSQL += " proc_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " proc_time  = to_char(sysdate,'hh24miss'),";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where file_name  = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteIchOrgdataLog() throws Exception {
        daoTable = "ich_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";

        tmpstr1 = String.format("%s", hTnlgFileName);
        String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        String fptr1Name = temstr1;
        int f = openInputText(fptr1Name);
        if (f == -1) {
            String stderr = "檔案不存在：" + fptr1Name;
            tmpstr1 = String.format("%21.21s.zip.ng", hTnlgFileName);
            temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
            temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
            try {
                int out = openOutputText(temstr1,"big5");
                writeTextFile(out, String.format("%8.8s%6.6s1", hTempSystemDate, hTempNotifyTime));
                closeOutputText(out);
            } catch (Exception ex) {
                comcr.errRtn(String.format("產生檔案[%s]有錯誤[%s]", temstr1, ex.getMessage()), "", hCallBatchSeqno);
            }
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        tmpstr1 = String.format("%21.21s.zip.ack", hTnlgFileName);
        temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        try {
            int out = openOutputText(temstr1,"big5");
            writeTextFile(out, String.format("%8.8s%6.6s", hTempSystemDate, hTempNotifyTime));
            closeOutputText(out);
        } catch (Exception ex) {
            comcr.errRtn(String.format("產生檔案[%s]有錯誤[%s]", temstr1, ex.getMessage()), "", hCallBatchSeqno);
        }

        int br = openInputText(fptr1Name, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            str600 = comc.rtrim(str600);

            if (str600.substring(0, 1).equals("H"))
                continue;

            totalCnt++;

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            hOrgdOrgData = str600;
            if (!str600.substring(0, 1).equals("D")) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }

            tmpstr1 = String.format("%s", comc.rtrim(dtl.updDate));
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.updTime));
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.sysDate));
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.sysTime));
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }

            hOrgdRptRespCode = "0";

            insertIchOrgdataLog();

            if (hOrgdRptRespCode.equals("0"))
                succCnt++;
        }
        closeInputText(br);
    }

    /***********************************************************************/
    void insertIchOrgdataLog() throws Exception {
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "A01B");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ich_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchF021 proc = new IchF021();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String agencyCd;
        String storeCd;
        String agencyName;
        String agencyAbbrName;
        String storeName;
        String updDate;
        String updTime;
        String sysDate;
        String sysTime;
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.agencyCd = comc.subMS950String(bytes, 1, 20);
        dtl.storeCd = comc.subMS950String(bytes, 21, 20);
        dtl.agencyName = comc.subMS950String(bytes, 41, 40);
        dtl.agencyAbbrName = comc.subMS950String(bytes, 81, 20);
        dtl.storeName = comc.subMS950String(bytes, 101, 20);
        dtl.updDate = comc.subMS950String(bytes, 121, 8);
        dtl.updTime = comc.subMS950String(bytes, 129, 6);
        dtl.sysDate = comc.subMS950String(bytes, 135, 8);
        dtl.sysTime = comc.subMS950String(bytes, 143, 6);
    }

}
