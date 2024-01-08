/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/14  V1.00.00    Wendy Lu                     program initial        *
*  112/08/13  V1.00.01    Wilson    收不到檔不當掉(營業日才會有檔)                                                             *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;


public class TscD027 extends AccessDAO {

    private final String progname = "悠遊VD卡發卡撥款檔(DCFI)媒體接收檢核程式  112/08/13 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    String rptId2 = "TSC_D027R1";
    String rptName2 = "發卡VD悠遊卡撥款檔明細表";
    int rptSeq2 = 0;
    String buf = "";

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
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hPrintName = "";
    String hRptName = "";

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    int rptCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String hCgecBillType = "";
    String fixBillType = "";

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
                comc.errExit("Usage : TscD027 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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
                if ((args[0].length() == 1) && (args[0].substring(0, 1).equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8) {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hTempNotifyDate = sgArgs0;
                }
                if (args[0].length() == 2) {
                    showLogMessage("I", "", String.format("參數(一) 不可兩碼"));
                }
            }
            if (args.length == 2) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTempNotifyDate = sgArgs0;
                if ((args[1].length() == 1) && (args[1].substring(0, 1).equals("Y")))
                    forceFlag = 1;
                if (args[1].length() == 2) {
                    String sgArgs1 = "";
                    sgArgs1 = args[1];
                    sgArgs1 = Normalizer.normalize(sgArgs1, java.text.Normalizer.Form.NFKD);
                    fileSeq = sgArgs1;
                }
                if (args[1].length() != 1 && args[1].length() != 2) {
                    showLogMessage("I", "", String.format("參數(二) 為[force_flag] or [seq(nn)] "));
                }
            }
            if (args.length == 3) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTempNotifyDate = sgArgs0;
                if (args[1].substring(0, 1).equals("Y"))
                    forceFlag = 1;
                if (args[2].length() != 2) {
                    showLogMessage("I", "", String.format("file seq 必須兩碼"));
                }
                String sgArgs2 = "";
                sgArgs2 = args[2];
                sgArgs2 = Normalizer.normalize(sgArgs2, java.text.Normalizer.Form.NFKD);
                fileSeq = sgArgs2;
            }
            selectPtrBusinday();

            tmpstr1 = String.format("DCFI.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    updateTscNotifyLoga();
                    backupRtn();
                    finalProcess();
                    return 0;
                }
            }

            fixBillType = "TSCC";

            deleteTscOrgdataLog();

            fileOpen();
            updateTscNotifyLoga();

            backupRtn();

            showLogMessage("I", "",
                    String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
            showLogMessage("I", "", String.format("程式執行結束"));

            String ftpName = String.format("%s.%s_%s", "DCFI", sysDate, hBusiBusinessDate);
            String filename = String.format("%s/reports/%s.%s_%s", comc.getECSHOME(), "DCFI", sysDate, hBusiBusinessDate);

            comc.writeReport(filename, lpar2);
            
            ftpMput(ftpName);

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
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "decode( cast(? as varchar(8)) , ''"
                + ",to_char(decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13') , 1  , sysdate"
                + ", sysdate-1 days) ,'yyyymmdd') , ?) h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += "from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempNotifyDate);
        setString(2, hTempNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	if ("".equals(hTempNotifyDate)) {
                hBusiBusinessDate = getValue("business_date");
        	} else {
        		hBusiBusinessDate = hTempNotifyDate;
        	}

        	hTempNotifyDate = hTempNotifyDate.length() == 0 ? hBusiBusinessDate : hTempNotifyDate;
            //hTempNotifyDate = getValue("h_temp_notify_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        /* proc_flag = 0:收檔中 1: 已收檔 2: 已處理 3: 已回應 */
        hTnlgPerformFlag = "";
        hTnlgNotifyDate = "";
        hTnlgCheckCode = "";
        hTnlgProcFlag = "";
        hTnlgRowid = "";

        sqlCmd = "select perform_flag, ";
        sqlCmd += "notify_date, ";
        sqlCmd += "check_code, ";
        sqlCmd += "proc_flag, ";
        sqlCmd += "rowid ";
        sqlCmd += "from tsc_notify_log ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgPerformFlag = getValue("perform_flag");
            hTnlgNotifyDate = getValue("notify_date");
            hTnlgCheckCode = getValue("check_code");
            hTnlgProcFlag = getValue("proc_flag");
            hTnlgRowid = getValue("rowid");
        }

        if (!hTnlgPerformFlag.equals("Y")) {
        	exceptExit = 0;
            comcr.errRtn(String.format("通知檔收檔發生問題,[%s]暫不可處理 ,請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.equals("0")) {
            comcr.errRtn(String.format("通知檔收檔中[%s] , 請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.toCharArray()[0] >= '2') {
            comcr.errRtn(String.format("[%s]撥款資料檔已處理過,請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "", String.format("[%s]撥款資料整檔處理失敗  , 錯誤代碼[%s] ", hTnlgFileName, hTnlgCheckCode));
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = "tsc_notify_log ";
        updateSQL = "proc_flag = '2', ";
        updateSQL += "proc_date = to_char(sysdate, 'yyyymmdd'), ";
        updateSQL += "proc_time = to_char(sysdate, 'hh24miss'), ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where file_name = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", hTnlgFileName, hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void backupRtn() throws Exception {
        tmpstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        tmpstr1 = Normalizer.normalize(tmpstr1, java.text.Normalizer.Form.NFKD);
        tmpstr2 = String.format("%s/media/tsc/backup/%s", comc.getECSHOME(), hTnlgFileName);
        comc.fileRename(tmpstr1, tmpstr2);
    }

    /***********************************************************************/
    void deleteTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log ";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";
        tmpstr1 = String.format("%s", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }

        printHead();

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1+2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            hOrgdRptRespCode = "0000";
            String tReport0 = "";
            String tReport1 = "";
            String tReport2 = "";
            String tReport3 = "";
            String tReport4 = "";
            double tReport5 = 0;
            double tReport6 = 0;
            String tReport7 = "";
            tReport0 = String.format("%s", comc.rtrim(dtl.report0));
            tReport1 = String.format("%s", comc.rtrim(dtl.report1));
            tReport2 = String.format("%s", comc.rtrim(dtl.report2));
            tReport3 = String.format("%s", comc.rtrim(dtl.report3));
            tReport4 = String.format("%s", comc.rtrim(dtl.report4));
            tReport7 = String.format("%s", comc.rtrim(dtl.report7));
            tmpstr1 = String.format("%s", comc.rtrim(dtl.report5));
            tReport5 = comcr.str2double(tmpstr1);
            tmpstr1 = String.format("%s", comc.rtrim(dtl.report6));

            insertTscOrgdataLog();

            String szTmp = comcr.commFormat(",3$,3$,3$", tReport5);
            String szTmp1 = comcr.commFormat("6z", tReport6);
            rptCnt++;
            succCnt++;
            buf = String.format("%-4.4s%4.4s %-8.8s  %-2.2s  %-8.8s  %-1.1s  %-13.13s %6.6s %8.8s\n", tReport0, " ",
                    tReport1, tReport2, tReport3, tReport4, szTmp, szTmp1, tReport7);
            lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
        }

        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    void printHead() throws Exception {
        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: TSC_D027R1", 1);
        String szTmp = String.format("%22s", "發卡VD悠遊卡撥款檔明細表");
        buf = comcr.insertStrCenter(buf, szTmp, 80);
        buf = comcr.insertStr(buf, "印表日期:", 60);
        buf = comcr.insertStr(buf, chinDate, 70);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "撥款類別 入帳代號 屬性 扣帳代號 屬性 入 扣 帳金額 筆  數 清算日期", 01);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "======== ======== ==== ======== ==== ============ ====== ========", 01);
        lpar2.add(comcr.putReport(rptId2, rptName2, sysDate, ++rptSeq2, "0", buf));
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log ";
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "DCFI");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_orgdata_log duplicate!", "", hCallBatchSeqno);
        }

    }
    
    /***********************************************************************/
    int ftpMput(String filename) throws Exception {
        String procCode = "";

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = javaProgram; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "CREDITCARD"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/reports/", comc.getECSHOME());
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = "CREDITCARD";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        procCode = "mput " + filename;

        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始FTP....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        if (errCode != 0) {
            comcr.errRtn(String.format("%s FTP =[%s]無法連線 error", javaProgram, procCode), "", hCallBatchSeqno);
        }
        return (0);
    }

    /***********************************************************************/
     public static void main(String[] args) throws Exception {
        TscD027 proc = new TscD027();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String report0;
        String report1;
        String report2;
        String report3;
        String report4;
        String report5;
        String report6;
        String report7;
        String filler2;
        String hashValue;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(report0, 4);
            rtn += comc.fixLeft(report1, 8);
            rtn += comc.fixLeft(report2, 1);
            rtn += comc.fixLeft(report3, 8);
            rtn += comc.fixLeft(report4, 1);
            rtn += comc.fixLeft(report5, 15);
            rtn += comc.fixLeft(report6, 8);
            rtn += comc.fixLeft(report7, 8);
            rtn += comc.fixLeft(filler2, 104);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(filler1, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.attri = comc.subMS950String(bytes, 1, 2);
        dtl.report0 = comc.subMS950String(bytes, 3, 4);
        dtl.report1 = comc.subMS950String(bytes, 7, 8);
        dtl.report2 = comc.subMS950String(bytes, 15, 1);
        dtl.report3 = comc.subMS950String(bytes, 16, 8);
        dtl.report4 = comc.subMS950String(bytes, 24, 1);
        dtl.report5 = comc.subMS950String(bytes, 25, 15);
        dtl.report6 = comc.subMS950String(bytes, 40, 8);
        dtl.report7 = comc.subMS950String(bytes, 48, 8);
        dtl.filler2 = comc.subMS950String(bytes, 56, 104);
        dtl.hashValue = comc.subMS950String(bytes, 160, 16);
        dtl.filler1 = comc.subMS950String(bytes, 176, 2);
    }

}
