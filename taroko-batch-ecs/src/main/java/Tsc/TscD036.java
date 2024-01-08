/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/18   V1.00.00   Wendy Lu                     program initial        *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;

import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;


public class TscD036 extends AccessDAO {

    private final String progname = "悠遊VD卡爭議交易回覆資料檔(DCCB)媒體接收處理程式  110/01/18 V1.00.00";
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
    String hDccbTscCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hDccbTranCode = "";
    String hDccbTranDate = "";
    String hDccbTranTime = "";
    double hDccbTranAmt = 0;
    String hDccbTraffCode = "";
    String hDccbPlaceCode = "";
    String hDccbTraffSubname = "";
    String hDccbPlaceSubname = "";
    String hDccbChgbackDate = "";
    String hDccbChgbackReason = "";
    String hDccbTsccRespCode = "";
    String hDccbTsccProcDesc = "";
    String hDccbOnlineMark = "";
    int hCnt = 0;
    int hErrCnt = 0;

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
    String hSign = "";
    String hSigns = "";

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
                comc.errExit("Usage : TscD036 [[notify_date][force_flag]] [force_flag][seq]", "");
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
                if ((args[1].length() == 1) && (args[1].equals("Y")))
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
                if (args[1].equals("Y"))
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

            tmpstr1 = String.format("DCCB.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));


            if (selectTscOrgdataLog() != 0) {
                backupRtn();
                comcr.errRtn(String.format("TscD036 檢核有錯本程式不執行.."), "", hCallBatchSeqno);
            }

            deleteTscDccbLog();
            deleteTscOrgdataLog();

            fileOpen();
            updateTscNotifyLoga();

            backupRtn();

            showLogMessage("I", "", String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
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
        sqlCmd = "select business_date, ";
        sqlCmd += "decode( cast(? as varchar(8)), '' "
                + ",to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13'), 1, sysdate "
                + ",sysdate - 1 days), 'yyyymmdd'), ?) h_temp_notify_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempNotifyDate);
        setString(2, hTempNotifyDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
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
    int selectTscOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt, ";
        sqlCmd += "sum(decode(rpt_resp_code,'0000',0,1)) h_err_cnt ";
        sqlCmd += "from tsc_orgdata_log ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_orgdata_log not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        }

        return (hErrCnt);
    }

    /***********************************************************************/
    void deleteTscDccbLog() throws Exception {
        daoTable = "tsc_dccb_log a ";
        whereStr = "where tscc_data_seqno = (select b.tscc_data_seqno from tsc_orgdata_log b "
                + "where b.file_name = ? and b.tscc_data_seqno = a.tscc_data_seqno) ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    void deleteTscOrgdataLog() throws Exception {

        daoTable = "tsc_orgdata_log ";
        whereStr = "where file_name  = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = " tsc_notify_log ";
        updateSQL = "proc_flag = '2',";
        updateSQL += "proc_date = to_char(sysdate, 'yyyymmdd'), ";
        updateSQL += "proc_time = to_char(sysdate, 'hh24miss'), ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where file_name = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
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
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranCode));
            if (!tmpstr1.equals("7207")) {
                hOrgdRptRespCode = "0305";
                insertTscOrgdataLog();
                continue;
            }
            hDccbTranCode = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tscCardNo));
            if (comc.commTSCCCardNoCheck(tmpstr1) != 0) {
                showLogMessage("I", "", String.format("tsc_card_no[%s]", tmpstr1));
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hDccbTscCardNo = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranDate));
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            hDccbTranDate = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranTime));
            if (!comc.commTimeCheck(tmpstr1)) {
                hOrgdRptRespCode = "0204";
                insertTscOrgdataLog();
                continue;
            }
            hDccbTranTime = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }
            hDccbTranAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.traffCode));
            hDccbTraffCode = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.placeCode));
            hDccbPlaceCode = tmpstr1;
            /*************************************************************************/
            hDccbTraffSubname = comc.rtrim(dtl.traffSubname);
            hDccbPlaceSubname = comc.rtrim(dtl.placeSubname);
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.chgbackDate));
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            hDccbChgbackDate = tmpstr1;
            /*************************************************************************/
            hDccbChgbackReason = comc.rtrim(dtl.chgbackReason);
            hDccbTsccRespCode = comc.rtrim(dtl.tsccRespCode);
            hDccbTsccProcDesc = comc.rtrim(dtl.tsccProcDesc);
            hDccbOnlineMark = comc.rtrim(dtl.onlineMark);
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
            insertTscDccbLog();
            succCnt++;
        }
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    int selectTscCard() throws Exception {
        sqlCmd = "select rowid rowid ";
        sqlCmd += "from tsc_vd_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hDccbTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTardRowid = getValue("rowid");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "DCCB");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_orgdata_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertTscDccbLog()
            throws Exception { /* put RPT 2 columns, What to do ? */
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("tsc_card_no", hDccbTscCardNo);
        setValue("tran_code", hDccbTranCode);
        setValue("tran_date", hDccbTranDate);
        setValue("tran_time", hDccbTranTime);
        setValueDouble("tran_amt", hDccbTranAmt);
        setValue("traff_code", hDccbTraffCode);
        setValue("place_code", hDccbPlaceCode);
        setValue("traff_subname", hDccbTraffSubname);
        setValue("place_subname", hDccbPlaceSubname);
        setValue("chgback_date", hDccbChgbackDate);
        setValue("chgback_reason", hDccbChgbackReason);
        setValue("tscc_resp_code", hDccbTsccRespCode);
        setValue("tscc_proc_desc", hDccbTsccProcDesc);
        setValue("online_mark", hDccbOnlineMark);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_dccb_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_dccb_log duplicate!", "", hCallBatchSeqno);
        }

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

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscD036 proc = new TscD036();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
