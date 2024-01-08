/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/03  V1.00.00    Brian     program initial                           *
 *  109/12/16  V1.00.01    tanwei    updated for project coding standard       *
 ******************************************************************************/

package Ich;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchF03a extends AccessDAO {
    private String progname = "聯名卡帳務調整清分明細(A10B)處理  109/12/16 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;

    String hCallBatchSeqno = "";

    String hTempNotifyDate = "";
    String hNextNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hAdd2SystemDate = "";
    String hTnlgPerformFlag = "";
    String hTnlgNotifyDate = "";
    String hTnlgCheckCode = "";
    String hTnlgProcFlag = "";
    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    int hCnt = 0;
    int hErrCnt = 0;
    double hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hTempBatchSeq  = "";
    String hCardNo = "";

    String hIchCardNo  = "";
    String hAdjustNo    = "";
    String hAdjustRsn   = "";
    int    hAdjustAmt   = 0;
    String hPrePayDate = "";
    String hPayVendor   = "";
    String hSysDate     = "";
    String hSysTime     = "";


    String tmpstr1 = "";
    String tmpstr2 = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int hTnlgRecordCnt = 0;
    int totalCnt = 0;
    String tmpstr = "";
    String hHash = "";


    String out = "";

    Buf1 dtl = new Buf1();

    public int mainProcess(String[] args) {

        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ichF03a [[notify_date][fo1yy_flag]] [force_flag]", "");
            }

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

            tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A10B", comc.ICH_BANK_ID3, hTempNotifyDate);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            deleteIchA10bAdjust();
            deleteIchOrgdataLog();

            fileOpen();

            updateIchNotifyLogA();

            showLogMessage("I", "",
                    String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
        hAdd2SystemDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time,";
        sqlCmd += "to_char(add_months(sysdate,2),'yyyymmdd') h_add2_system_date ";
        sqlCmd += " from ptr_businday  ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempSystemDate = getValue("h_temp_system_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
            hAdd2SystemDate = getValue("h_add2_system_date");
        }
        
        hTempNotifyDate = hTempNotifyDate.length() == 0 ? hBusiBusinessDate : hTempNotifyDate;
        hNextNotifyDate = comm.nextNDate(hTempNotifyDate, 1);

    }

    /***********************************************************************/
    int selectIchOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt,";
        sqlCmd += "sum(decode(rpt_resp_code,'0',0,1)) h_err_cnt ";
        sqlCmd += " from ich_orgdata_log  ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("ichF021 檢核程式未執行或該日無資料需處理...", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        }

        return (hErrCnt);
    }

    /***********************************************************************/
    void deleteIchA10bAdjust() throws Exception {
        daoTable = "ich_a10b_adjust a";
        whereStr = "where a.tscc_data_seqno in (select b.tscc_data_seqno from ich_orgdata_log b where b.file_name  = ?  ";
        whereStr += "and b.tscc_data_seqno = a.tscc_data_seqno) ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void deleteIchOrgdataLog() throws Exception {
        daoTable = "ich_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();

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
    void fileOpen() throws Exception {
        String str600 = "";
        String allData = "";

        /* read ARQB */
        String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int br = openInputText(temstr1, "MS950");
        if (br == -1) {
            comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
        }
        selectBilPostcntl();

        /* write ARPB */
        tmpstr1 = String.format("ARPB_%3.3s_%8.8s_A10B", comc.ICH_BANK_ID3, hNextNotifyDate);
        String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = temstr2;
        hHash = "0000000000000000000000000000000000000000";
        tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A10B", "02", "0001",
                comc.ICH_BANK_ID3, String.format("%08d", getValueInt("all_cnt")) , hHash);

        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));

        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            str600 = comc.rtrim(str600);
            if (str600.substring(0, 1).equals("H"))
                continue;

            totalCnt++;

            initIchA10bAdjust();

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
            
            hIchCardNo = comc.rtrim(dtl.ichCardNo);
            if (selectIchCard() != 0) {
                hOrgdRptRespCode = "2";
                insertIchOrgdataLog();
                continue;
            }

            tmpstr1 = String.format("%s", comc.rtrim(dtl.adjustAmt));
            if (comc.commDigitCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "3";
                insertIchOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.prePayDate));
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "4";
                insertIchOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.sysDate));
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "5";
                insertIchOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.sysTime));
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "6";
                insertIchOrgdataLog();
                continue;
            }
            hAdjustNo = comc.rtrim(dtl.adjustNo);
            hAdjustRsn = comc.rtrim(dtl.adjustRsn);
            hAdjustAmt = comc.str2int(comc.rtrim(dtl.adjustAmt));
            hPrePayDate = comc.rtrim(dtl.prePayDate);
            hPayVendor = comc.rtrim(dtl.payVendor);
            hSysDate = comc.rtrim(dtl.sysDate);
            hSysTime = comc.rtrim(dtl.sysTime);


            hOrgdRptRespCode = "0";
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hAdjustAmt;

            insertIchOrgdataLog();

            insertIchA10bAdjust();

            if (hOrgdRptRespCode.equals("0"))
                succCnt++;

            String buf = String.format("D%-16.16s%-20.20s%-14.14s%-10.10s%1.1s\r\n", hIchCardNo, hAdjustNo,
                    sysDate + sysTime, " ", hOrgdRptRespCode);
            lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

            allData += buf;
        }


        if(totalCnt > 0) {
            insertBilPostcntl();
        }

        closeInputText(br);
        moveBackup(hTnlgFileName);

        if(totalCnt > 0)
        {
            hHash  = comc.encryptSHA(allData, "SHA-1", "MS950");
            tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A10B", "02", "0001",
                    comc.ICH_BANK_ID3, comm.fillZero(Integer.toString(totalCnt), 8) , hHash);
            lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
        }

        comc.writeReport(out, lpar, "MS950", false);
    }
    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        String hBiunConfFlag = "";
        String fixBillType = "ICAH";

        sqlCmd = "select conf_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, fixBillType );
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found3", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

        setValue("batch_date", hBusiBusinessDate);
        setValue("batch_unit", fixBillType.substring(0, 2));
        setValue("batch_seq" , hTempBatchSeq);
        setValue("batch_no"  , hBusiBusinessDate + "IC" + hTempBatchSeq);
        setValueDouble("tot_record", hPostTotRecord);
        setValueDouble("tot_amt", hPostTotAmt);
        setValue("confirm_flag_p", hBiunConfFlag.equals("N") ? "Y" : "N");
        setValue("confirm_flag", hBiunConfFlag);
        setValue("this_close_date", hTempNotifyDate);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "bil_postcntl";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ich_orgdata_log duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectBilPostcntl() throws Exception {
        hTempBatchSeq = "";
        sqlCmd = "select substr(to_char(nvl(max(batch_seq),0)+1,'0000'),2,4) h_temp_batch_seq ";
        sqlCmd += " from bil_postcntl  ";
        sqlCmd += "where batch_unit = substr(?,1,2)  ";
        sqlCmd += "and batch_date = ? ";
        setString(1, "ICAH");
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            hTempBatchSeq = "0000";
        }
        if (recordCnt > 0) {
            hTempBatchSeq = getValue("h_temp_batch_seq");
        }
        showLogMessage("I", "", "888 Batch seq=[" + hTempBatchSeq + "]");

    }


    /***********************************************************************/
    void initIchA10bAdjust() throws Exception {
        hIchCardNo   = "";
        hAdjustNo     = "";
        hAdjustRsn    = "";
        hAdjustAmt    = 0;
        hPrePayDate  = "";
        hPayVendor    = "";
        hSysDate      = "";
        hSysTime      = "";
    }

    /***********************************************************************/
    void insertIchOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "A10B");
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
    void insertIchA10bAdjust() throws Exception {
        String hTxnCode = "05";
        if(hPayVendor.equals("IC"))
        {
            hTxnCode = "06";
        }
        setValue("ich_card_no", hIchCardNo);
        setValue("adjust_no", hAdjustNo);
        setValue("adjust_rsn", hAdjustRsn);
        setValueInt("adjust_amt", hAdjustAmt);
        setValue("pre_pay_date", hPrePayDate);
        setValue("pay_vendor", hPayVendor);
        setValue("sys_date", hSysDate);
        setValue("sys_time", hSysTime);
        setValue("file_name", hTnlgFileName);
        setValue("crt_date", sysDate);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        //new column
        setValue("BATCH_NO", hBusiBusinessDate + "IC" + hTempBatchSeq); // 批號
        setValue("SEQ_NO"   , String.format("%d", totalCnt)); // 序號
        setValue("CARD_NO"  , hCardNo); // CARD_NO
        setValue("BILL_TYPE", "ICAH"); // BILL_TYPE
        setValue("TXN_CODE" , hTxnCode); // TXN_CODE
        setValue("MCHT_NO"  , "ICAH8002"); // MCHT_NO
        setValue("MCHT_CATEGORY", "4100"); // MCHT_CATEGORY
        setValue("MCHT_CHI_NAME", String.format("%20.20s",hAdjustRsn)); // MCHT_CHI_NAME
        setValue("BILL_DESC"    , String.format("%20.20s",hAdjustRsn)); // BILL_DESC
        setValue("REFERENCE_NO", ""); // REFERENCE_NO
        setValue("POST_FLAG", "N"); // 處理註記
        daoTable = "ich_a10b_adjust";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int selectIchCard() throws Exception {
        hCardNo = "";

        sqlCmd = "select card_no ";
        sqlCmd += " from ich_card  ";
        sqlCmd += "where ich_card_no = ? ";
        setString(1, hIchCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardNo = getValue("card_no");
        } else
            return 1;

        return 0;
    }

    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String root = String.format("%s/media/ich", comc.getECSHOME());
        String src = String.format("%s/%s", root, moveFile);
        String target = String.format("%s/backup/%s/%s", root, hTempNotifyDate, moveFile);

        comc.fileRename(src, target);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchF03a proc = new IchF03a();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type        ;
        String ichCardNo ;
        String adjustNo   ;
        String adjustRsn  ;
        String adjustAmt  ;
        String prePayDate;
        String payVendor  ;
        String sysDate    ;
        String sysTime    ;
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.ichCardNo = comc.subMS950String(bytes, 1, 16);
        dtl.adjustNo = comc.subMS950String(bytes, 17, 20);
        dtl.adjustRsn = comc.subMS950String(bytes, 37, 50);
        dtl.adjustAmt = comc.subMS950String(bytes, 87, 10);
        dtl.prePayDate = comc.subMS950String(bytes, 97, 8);
        dtl.payVendor = comc.subMS950String(bytes, 105, 2);
        dtl.sysDate = comc.subMS950String(bytes, 107, 8);
        dtl.sysTime = comc.subMS950String(bytes, 115, 6);
    }
}
