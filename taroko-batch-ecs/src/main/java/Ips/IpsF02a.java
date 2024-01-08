/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-14  V1.00.01    tanwei      updated for project coding standard     *
******************************************************************************/

package Ips;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡片退費每10日彙總檔(I2B011)接收處理*/
public class IpsF02a extends AccessDAO {
    private String progname = "卡片退費每10日彙總檔(I2B011)接收處理  109/12/14 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    int tempInt = 0;
    String hTempNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hTnlgPerformFlag = "";
    String hTnlgNotifyDate = "";
    String hTnlgCheckCode = "";
    String hTnlgProcFlag = "";
    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hIcgeCardNo = "";
    String hIcgeIpsCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String nUserpid = "";
    String tmpstr = "";
    String fileSeq = "";
    String tempPrevDate = "";
    String tempDate1 = "";
    String tempDate2 = "";
    String tempDate3 = "";
    String tmpstr1 = "";
    int nRetcode = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;

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
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            if (args.length != 0 && args.length != 1 && args.length != 2 && args.length != 3) {
                comc.errExit("Usage : IpsF02a [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempNotifyDate = "";
            fileSeq = "01";
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
                if ((args[1].length() == 1) && (args[1].equals("Y")))
                    forceFlag = 1;
                if (args[1].length() == 2)
                    fileSeq = args[1];
                if (args[1].length() != 1 && args[1].length() != 2) {
                    showLogMessage("I", "", "參數(二) 為[force_flag] or [seq(nn)] ");
                }
            }
            if (args.length == 3) {
                hTempNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
                if (args[2].length() != 2) {
                    showLogMessage("I", "", "file seq 必須兩碼");
                }
                fileSeq = args[2];
            }

            selectPtrBusinday();

            showLogMessage("I", "", String.format(" 處理日期=[%s]", hTempNotifyDate));

            tempInt = 0;
            sqlCmd = "select count(*) temp_int ";
            sqlCmd += " from ptr_holiday  ";
            sqlCmd += "where holiday = ? ";
            setString(1, hTempNotifyDate);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                tempInt = getValueInt("temp_int");
            }
            if (tempInt > 0) {
                String stderr = String.format("今日為假日, 不需執行 [%s]!!", hTempNotifyDate);
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }

            tempPrevDate = comcr.increaseDays(hTempNotifyDate, -1);
            tempDate1 = String.format("%6.6s01", hTempNotifyDate);
            tempDate2 = String.format("%6.6s11", hTempNotifyDate);
            tempDate3 = String.format("%6.6s21", hTempNotifyDate);

            if ((comcr.str2long(hTempNotifyDate) >= comcr.str2long(tempDate1)
                    && comcr.str2long(tempPrevDate) < comcr.str2long(tempDate1))
                    || (comcr.str2long(hTempNotifyDate) >= comcr.str2long(tempDate2)
                            && comcr.str2long(tempPrevDate) < comcr.str2long(tempDate2))
                    || (comcr.str2long(hTempNotifyDate) >= comcr.str2long(tempDate3)
                            && comcr.str2long(tempPrevDate) < comcr.str2long(tempDate3))) {
            } else {
                String stderr = String.format(" 非 01,11,21   不需執行 [%s]!!", hTempNotifyDate);
                exceptExit = 0;
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }

            tmpstr1 = String.format("I2B011_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s] Force_flag=[%d]", tmpstr1, forceFlag));

            if (forceFlag == 0) {
                if (selectIpsNotifyLogA() != 0) {
                    updateIpsNotifyLogA();
                    commitDataBase();
                    return (0);
                }
            }

            deleteIpsOrgdataLog();

            fileOpen();

            updateIpsNotifyLogA();

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
        sqlCmd += "fetch first 1 rows only ";
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
    int selectIpsNotifyLogA() throws Exception {
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
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from ips_notify_log  ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgPerformFlag = getValue("perform_flag");
            hTnlgNotifyDate = getValue("notify_date");
            hTnlgCheckCode = getValue("check_code");
            hTnlgProcFlag = getValue("proc_flag");
            hTnlgRowid = getValue("rowid");
        } else {
            String stderr = String.format("未有[%s]檔案記錄 , 請通知相關人員處理(error)", hTnlgFileName);
            comcr.errRtn(stderr, "", hCallBatchSeqno);
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
            String stderr = String.format("[%s]卡片退費檔已處理過,請通知相關人員處理(error)", hTnlgFileName);
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "", String.format("[%s]卡片退費檔整檔處理失敗  , 錯誤代碼[%s]", hTnlgFileName, hTnlgCheckCode));
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void updateIpsNotifyLogA() throws Exception {
        daoTable = "ips_notify_log";
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
            comcr.errRtn("update_ips_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteIpsOrgdataLog() throws Exception {
        daoTable = "ips_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";

        tmpstr1 = String.format("%s", hTnlgFileName);
        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        String fptr1Name = temstr1;
        int f = openInputText(fptr1Name);
        if (f == -1) {
            String stderr = "檔案不存在：" + fptr1Name;
            tmpstr1 = String.format("%21.21s.zip.ng", hTnlgFileName);
            temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tmpstr1);
            temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
            try {
                int out = openOutputText(temstr1, "big5");
                writeTextFile(out, String.format("%8.8s%6.6s1", hTempSystemDate, hTempNotifyTime));
                closeOutputText(out);
            } catch (Exception ex) {
                comcr.errRtn(String.format("產生檔案[%s]有錯誤[%s]", temstr1, ex.getMessage()), "", hCallBatchSeqno);
            }
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        tmpstr1 = String.format("%21.21s.zip.ack", hTnlgFileName);
        temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        try {
            int out = openOutputText(temstr1, "big5");
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
            if ((str600.substring(0, 1).equals("H")) || (str600.substring(0, 1).equals("T")))
                continue;

            totalCnt++;

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            hOrgdOrgData = str600;
            if (!str600.substring(0, 1).equals("D")) {
                hOrgdRptRespCode = "0205";
                insertIpsOrgdataLog();
                continue;
            }
            hIcgeIpsCardNo = comc.rtrim(dtl.ipsCardNo);

            if (selectIpsCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertIpsOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnDate));
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "0203";
                insertIpsOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnTime));
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0204";
                insertIpsOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnAmt));
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnBal));
            hOrgdRptRespCode = "0000";

            insertIpsOrgdataLog();

            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }
        closeInputText(br);
    }

    /***********************************************************************/
    int selectIpsCard() throws Exception {

        sqlCmd = "select card_no ";
        sqlCmd += " from ips_card  ";
        sqlCmd += "where ips_card_no = ? ";
        setString(1, hIcgeIpsCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIcgeCardNo = getValue("card_no");
        } else {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void insertIpsOrgdataLog() throws Exception {
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "I2B011");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ips_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_orgdata_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF02a proc = new IpsF02a();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String ipsCardNo;
        String txnType;
        String txnDate;
        String txnTime;
        String txnAmt;
        String txnBal;
        String filler0;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(ipsCardNo, 11);
            rtn += comc.fixLeft(txnType, 2);
            rtn += comc.fixLeft(txnDate, 8);
            rtn += comc.fixLeft(txnTime, 6);
            rtn += comc.fixLeft(txnAmt, 6);
            rtn += comc.fixLeft(txnBal, 6);
            rtn += comc.fixLeft(filler0, 28);
            rtn += comc.fixLeft(filler1, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.ipsCardNo = comc.subMS950String(bytes, 1, 11);
        dtl.txnType = comc.subMS950String(bytes, 12, 2);
        dtl.txnDate = comc.subMS950String(bytes, 14, 8);
        dtl.txnTime = comc.subMS950String(bytes, 22, 6);
        dtl.txnAmt = comc.subMS950String(bytes, 28, 6);
        dtl.txnBal = comc.subMS950String(bytes, 34, 6);
        dtl.filler0 = comc.subMS950String(bytes, 40, 28);
        dtl.filler1 = comc.subMS950String(bytes, 68, 2);
    }

}
