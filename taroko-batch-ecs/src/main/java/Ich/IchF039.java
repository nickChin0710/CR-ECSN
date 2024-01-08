/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/03  V1.00.00    Brian     program initial                           *
 *  109/12/16  V1.00.01    tanwei    updated for project coding standard       *
 *  112/05/22  V1.00.02    Wilson    mark update_ich_notify_log not found      *
 *  112/11/24  V1.00.03    JeffKung  回傳值要等於8個"00000000"才能入帳
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

public class IchF039 extends AccessDAO {
    private String progname = "餘額返還卡片最後一筆扣款交易紀錄(A09B)處理  112/11/24 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
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

    String hIchCardNo   = "";
    String hAgencyCd     = "";
    String hStoreCd      = "";
    String hMerchineType = "";
    String hMerchineNo   = "";
    String hSafeNo       = "";
    String hTxnDate      = "";
    String hTxnTime      = "";
    int    hTxnAmt       = 0;
    int    hBalanceAmt   = 0;
    String hRtnValue     = "";
    String hSysDate      = "";
    String hSysTime      = "";
    String hStoreName      = "";

    double hPostTotRecord = 0;
    double hPostTotAmt    = 0;
    String fixBillType     = "ICAH";
    String hTempBatchSeq  = "";
    String hCardNo         = "";
    String hTxnCode        = "";
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
                comc.errExit("Usage : ichF039 [[notify_date][fo1yy_flag]] [force_flag]", "");
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

            tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A09B", comc.ICH_BANK_ID3, hTempNotifyDate);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            deleteIchA09bBal();
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
            comcr.errRtn("select_ptr_businday not found4", "", hCallBatchSeqno);
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
    void deleteIchA09bBal() throws Exception {
        daoTable = "ich_a09b_bal a";
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

        updateSQL  = "proc_flag  = '2',";
        updateSQL += " proc_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " proc_time  = to_char(sysdate,'hh24miss'),";
        updateSQL += " mod_pgm    = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where file_name  = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        daoTable   = "ich_notify_log";
        updateTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("update_ich_notify_log not found", hTnlgFileName, hCallBatchSeqno);
//        }

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
        tmpstr1 = String.format("ARPB_%3.3s_%8.8s_A09B", comc.ICH_BANK_ID3, hNextNotifyDate);
        String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = temstr2;
        hHash = "0000000000000000000000000000000000000000";
        tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A09B", "02", "0001",
                comc.ICH_BANK_ID3, String.format("%08d", getValueInt("all_cnt")) , hHash);

        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));

        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            str600 = comc.rtrim(str600);
            if (str600.substring(0, 1).equals("H"))
                continue;

            totalCnt++;

            initIchA09bBal();

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
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }

            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnDate));
            if(!tmpstr1.substring(0,1).equals("0"))
            {
                if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                    hOrgdRptRespCode = "1";
                    insertIchOrgdataLog();
                    continue;
                }
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnTime));
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnAmt));
            if (comc.commDigitCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }

            tmpstr1 = String.format("%s", comc.rtrim(dtl.balanceAmt));
            if (comc.commDigitCheck(tmpstr1) == false) {
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
            
            hAgencyCd = comc.rtrim(dtl.agencyCd);
            hStoreCd = comc.rtrim(dtl.storeCd);
            hMerchineType = comc.rtrim(dtl.merchineType);
            hMerchineNo = comc.rtrim(dtl.merchineNo);
            hSafeNo  = comc.rtrim(dtl.safeNo);
            hTxnDate = comc.rtrim(dtl.txnDate);
            hTxnTime = comc.rtrim(dtl.txnTime);
            hTxnAmt  = comc.str2int(comc.rtrim(dtl.txnAmt));
            hBalanceAmt = comc.str2int(comc.rtrim(dtl.balanceAmt));
            hRtnValue = comc.rtrim(dtl.rtnValue);
            hSysDate  = comc.rtrim(dtl.sysDate);
            hSysTime  = comc.rtrim(dtl.sysTime);

            hOrgdRptRespCode = "0";
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hBalanceAmt;

            selectIchA01bAgent();
            insertIchOrgdataLog();

            insertIchA09bBal();

            if (hOrgdRptRespCode.equals("0"))
                succCnt++;

            String buf = String.format("D%-16.16s%-14.14s%-30.30s%1.1s\r\n", hIchCardNo, sysDate + sysTime, " ",
                    hOrgdRptRespCode);
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
            tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A09B", "02", "0001",
                    comc.ICH_BANK_ID3, comm.fillZero(Integer.toString(totalCnt), 8) , hHash);
            lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
        }

        comc.writeReport(out, lpar, "MS950", false);
    }
    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        String hBiunConfFlag = "";

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
        setString(1, fixBillType);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            hTempBatchSeq = "0000";
        }
        if (recordCnt > 0) {
            hTempBatchSeq = getValue("h_temp_batch_seq");
        }

    }
    /***********************************************************************/
    int selectIchA01bAgent() throws Exception {
        hStoreName = hAgencyCd;
        sqlCmd = "select store_name ";
        sqlCmd += " from ich_a01b_agent  ";
        sqlCmd += "where agency_cd = ? ";
        sqlCmd += "  and store_cd  = ? ";
        setString(1, hAgencyCd);
        setString(2, hStoreCd);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hStoreName = getValue("store_name");
        } else
            return 1;

        return 0;
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
    int updateIchCard() throws Exception {

        updateSQL  = " balance_rtn_date  = to_char(sysdate,'yyyymmdd') ";
        whereStr   = "where ich_card_no  = ? ";
        setString(1, hIchCardNo);
        daoTable   = "ich_card";
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ich_card not found ", hIchCardNo, hCallBatchSeqno);
        }

        return 0;
    }
    /***********************************************************************/
    void initIchA09bBal() throws Exception {
        hIchCardNo   = "";
        hAgencyCd     = "";
        hStoreCd      = "";
        hMerchineType = "";
        hMerchineNo   = "";
        hSafeNo       = "";
        hTxnDate      = "";
        hTxnTime      = "";
        hTxnAmt       = 0;
        hBalanceAmt   = 0;
        hRtnValue     = "";
        hSysDate      = "";
        hSysTime      = "";
        hCardNo       = "";
        hTxnCode      = "";
    }

    /***********************************************************************/
    void insertIchOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "A09B");
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
    void insertIchA09bBal() throws Exception {

        hTxnCode = "06";
        if(hBalanceAmt < 0)
        {
            hTxnCode    = "05";
            hBalanceAmt = hBalanceAmt * -1;
        }
        setValue("ich_card_no"   , hIchCardNo);
        setValue("agency_cd"     , hAgencyCd);
        setValue("store_cd"      , hStoreCd);
        setValue("merchine_type" , hMerchineType);
        setValue("merchine_no"   , hMerchineNo);
        setValue("safe_no"       , hSafeNo);
        setValue("txn_date"      , hTxnDate);
        setValue("txn_time"      , hTxnTime);
        setValueInt("txn_amt"    , hTxnAmt);
        setValueInt("balance_amt", hBalanceAmt);
        setValue("resp_code_ich" , hRtnValue);
        setValue("sys_date", hSysDate);
        setValue("sys_time", hSysTime);
        setValue("file_name", hTnlgFileName);
        setValue("crt_date" , sysDate);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_time" , sysDate + sysTime);
        setValue("mod_pgm"  , javaProgram);
        setValue("BATCH_NO"     , hBusiBusinessDate + "IC" + hTempBatchSeq); // 批號
        setValue("SEQ_NO"       , String.format("%d", totalCnt)); // 序號
        setValue("CARD_NO"      , hCardNo); // CARD_NO
        setValue("BILL_TYPE"    , fixBillType); // BILL_TYPE
        setValue("TXN_CODE"     , hTxnCode); // TXN_CODE
        setValue("MCHT_NO"      , "ICAH8001"); // MCHT_NO
        setValue("MCHT_CATEGORY", "4100"); // MCHT_CATEGORY
        setValue("MCHT_CHI_NAME", String.format("愛金卡餘額金%8.8s", hStoreName)); // MCHT_CHI_NAME
        setValue("BILL_DESC"    , String.format("愛金卡餘額金%8.8s", hStoreName)); // BILL_DESC
        setValue("REFERENCE_NO" , ""); // REFERENCE_NO
        
      //回覆碼要等於"00000000"才需要入帳 (20231124)
        if(comc.getSubString(hRtnValue, 0, 8).equals("00000000") ||
           comc.getSubString(hRtnValue, 0, 1).equals("F") ||
           comc.getSubString(hRtnValue, 0, 1).equals("C")) {
        	setValue("POST_FLAG", "N"); // 處理註記
        } else {
        	setValue("POST_FLAG", "Y"); // 處理註記
        }
        daoTable = "ich_a09b_bal";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

        updateIchCard();
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
        IchF039 proc = new IchF039();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String ichCardNo;
        String agencyCd;
        String storeCd;
        String merchineType;
        String safeNo;
        String merchineNo;
        String txnDate;
        String txnTime;
        String txnAmt;
        String balanceAmt;
        String rtnValue;    
        String sysDate;
        String sysTime;
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.ichCardNo = comc.subMS950String(bytes, 1, 16);
        dtl.agencyCd = comc.subMS950String(bytes, 17, 20);
        dtl.storeCd = comc.subMS950String(bytes, 37, 20);
        dtl.merchineType = comc.subMS950String(bytes, 57, 1);
        dtl.safeNo = comc.subMS950String(bytes, 58, 16);
        dtl.merchineNo = comc.subMS950String(bytes, 74, 8);
        dtl.txnDate = comc.subMS950String(bytes, 82, 8);
        dtl.txnTime = comc.subMS950String(bytes, 90, 6);
        dtl.txnAmt = comc.subMS950String(bytes, 96, 8);
        dtl.balanceAmt = comc.subMS950String(bytes, 104, 8);
        dtl.rtnValue = comc.subMS950String(bytes, 112, 8);
        dtl.sysDate = comc.subMS950String(bytes, 120, 8);
        dtl.sysTime = comc.subMS950String(bytes, 128, 6);
    }


}
