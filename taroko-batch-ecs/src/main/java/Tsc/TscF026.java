/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/13  V1.00.01    SUP       error correction                          *
*  109-11-17  V1.00.02    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊卡爭議交易回覆資料檔(ECCB)媒體接收檢核程式*/
public class TscF026 extends AccessDAO {

    private final String progname = "悠遊卡爭議交易回覆資料檔(ECCB)媒體接收檢核程式  109/11/17 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps comips = new CommIps();

    String hCallBatchSeqno = "";
    String hTempNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyTime = "";
    String hTnlgPerformFlag = "";
    String hTnlgNotifyDate = "";
    String hTnlgCheckCode = "";
    String hTnlgProcFlag = "";
    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hTardRowid = "";
    String hEccbTscCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    double hEccbTranAmt = 0;
    String hEccbTraffCode = "";
    String hEccbPlaceCode = "";
    String hEccbTraffSubname = "";
    String hEccbPlaceSubname = "";
    String hEccbChgbackDate = "";
    String hEccbChgbackReason = "";
    String hEccbTsccRespCode = "";
    String hEccbTsccProcDesc = "";
    String hEccbOnlineMark = "";
    String hEccbTranCode = "";
    String hEccbTranDate = "";
    String hEccbTranTime = "";

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String hCgecBillType = "";

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
                comc.errExit("Usage : TscF026 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            // h_call_batch_seqno = args.length > 0 ? args[args.length - 1] :
            // "";
            // comcr.callbatch(0, 0, 0);
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

            tmpstr1 = String.format("ECCB.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    updateTscNotifyLoga();
                    finalProcess();
                    return 0;
                }
            }
            deleteTscOrgdataLog();

            fileOpen();
            updateTscNotifyLoga();

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
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)) , ''"
                + ", to_char(decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13'), 1 , sysdate"
                + ", sysdate-1 days) ,'yyyymmdd'), ?) h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempNotifyDate);
        setString(2, hTempNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyDate = getValue("h_temp_notify_date");
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

        sqlCmd = "select perform_flag,";
        sqlCmd += " notify_date,";
        sqlCmd += " check_code,";
        sqlCmd += " proc_flag,";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from tsc_notify_log  ";
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
            comcr.errRtn(String.format("通知檔收檔發生問題,[%s]暫不可處理 ,請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.equals("0")) {
            comcr.errRtn(String.format("通知檔收檔中[%s] , 請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.toCharArray()[0] >= '2') {
            comcr.errRtn(String.format("[%s]爭議交易回覆資料檔已處理過,請通知相關人員處理(error) ", hTnlgFileName), "",
                    hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "",
                    String.format("[%s]爭議交易回覆資料整檔處理失敗  , 錯誤代碼[%s] ", hTnlgFileName, hTnlgCheckCode));
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = "tsc_notify_log";
        updateSQL = " proc_flag = '2',";
        updateSQL += " proc_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " proc_time = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where file_name = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", hTnlgFileName, hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void deleteTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log";
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

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;
            splitBuf1(str600);
            if ((totalCnt % 3000) == 0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            /*************************************************************************/
            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1+2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranCode));
            if (!tmpstr1.equals("7209")) {
                hOrgdRptRespCode = "0305";
                insertTscOrgdataLog();
                continue;
            }
            hEccbTranCode = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tscCardNo));
            if (comc.commTSCCCardNoCheck(tmpstr1) != 0) {
                showLogMessage("I", "", String.format("tsc_card_no[%s]", tmpstr1));
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hEccbTscCardNo = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranDate));
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            hEccbTranDate = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranTime));
            if (!comc.commTimeCheck(tmpstr1)) {
                hOrgdRptRespCode = "0204";
                insertTscOrgdataLog();
                continue;
            }
            hEccbTranTime = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }
            hEccbTranAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.traffCode));
            /*
             * if((COMM_digit_check(tmpstr1)!=0)||(atoi(tmpstr1)>255)) {
             * str2var(h_orgd_rpt_resp_code , "0305"); insert_tsc_orgdata_log();
             * continue; }
             */
            hEccbTraffCode = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.placeCode));
            /*
             * if((COMM_digit_check(tmpstr1)!=0)||(atoi(tmpstr1)>255)) {
             * str2var(h_orgd_rpt_resp_code , "0305"); insert_tsc_orgdata_log();
             * continue; }
             */
            hEccbPlaceCode = tmpstr1;
            /*************************************************************************/
            hEccbTraffSubname = comc.rtrim(dtl.traffSubname);
            hEccbPlaceSubname = comc.rtrim(dtl.placeSubname);
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.chgbackDate));
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            hEccbChgbackDate = tmpstr1;
            /*************************************************************************/
            hEccbChgbackReason = comc.rtrim(dtl.chgbackReason);
            hEccbTsccRespCode = comc.rtrim(dtl.tsccRespCode);
            hEccbTsccProcDesc = comc.rtrim(dtl.tsccProcDesc);
            hEccbOnlineMark = comc.rtrim(dtl.onlineMark);
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 206);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            if (!comc.subMS950String(str600.getBytes("MS950"), 206, 206 + 16).equals(tmpstr2)) {
                hOrgdRptRespCode = "0205";
                showLogMessage("I", "", String.format("HASH values error [%s]", hOrgdRptRespCode));
                /*
                 * insert_tsc_orgdata_log(); continue;
                 */
            }
            /*************************************************************************/
            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hOrgdRptRespCode = "0000";
            insertTscOrgdataLog();
            succCnt++;
        }
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    int selectTscCard() throws Exception {
        sqlCmd = "select rowid rowid";
        sqlCmd += " from tsc_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hEccbTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTardRowid = getValue("rowid");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log";
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "ECCB");
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
    public static void main(String[] args) throws Exception {
        TscF026 proc = new TscF026();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tranCode;
        String tscCardNo;
        String tranDate;
        String tranTime;
        String tranAmt;
        String traffCode;
        String placeCode;
        String traffSubname;
        String placeSubname;
        String chgbackDate;
        String chgbackReason;
        String tsccRespCode;
        String tsccProcDesc;
        String onlineMark;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tranCode, 4);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(tranDate, 8);
            rtn += comc.fixLeft(tranTime, 6);
            rtn += comc.fixLeft(tranAmt, 13);
            rtn += comc.fixLeft(traffCode, 8);
            rtn += comc.fixLeft(placeCode, 6);
            rtn += comc.fixLeft(traffSubname, 20);
            rtn += comc.fixLeft(placeSubname, 20);
            rtn += comc.fixLeft(chgbackDate, 8);
            rtn += comc.fixLeft(chgbackReason, 4);
            rtn += comc.fixLeft(tsccRespCode, 4);
            rtn += comc.fixLeft(tsccProcDesc, 80);
            rtn += comc.fixLeft(onlineMark, 1);
            rtn += comc.fixLeft(filler, 200);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.attri = comc.subMS950String(bytes, 1, 2);
        dtl.tranCode = comc.subMS950String(bytes, 3, 4);
        dtl.tscCardNo = comc.subMS950String(bytes, 7, 20);
        dtl.tranDate = comc.subMS950String(bytes, 27, 8);
        dtl.tranTime = comc.subMS950String(bytes, 35, 6);
        dtl.tranAmt = comc.subMS950String(bytes, 41, 13);
        dtl.traffCode = comc.subMS950String(bytes, 54, 8);
        dtl.placeCode = comc.subMS950String(bytes, 62, 6);
        dtl.traffSubname = comc.subMS950String(bytes, 68, 20);
        dtl.placeSubname = comc.subMS950String(bytes, 88, 20);
        dtl.chgbackDate = comc.subMS950String(bytes, 108, 8);
        dtl.chgbackReason = comc.subMS950String(bytes, 116, 4);
        dtl.tsccRespCode = comc.subMS950String(bytes, 120, 4);
        dtl.tsccProcDesc = comc.subMS950String(bytes, 124, 80);
        dtl.onlineMark = comc.subMS950String(bytes, 204, 1);
        dtl.filler = comc.subMS950String(bytes, 205, 200);
    }

}
