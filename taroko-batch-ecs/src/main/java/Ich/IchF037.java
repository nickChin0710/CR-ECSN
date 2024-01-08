/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/03  V1.00.00    Brian     program initial                           *
 *  109/12/16  V1.00.01    tanwei    updated for project coding standard       *
 *  112/11/24  V1.00.02    JeffKung  autoload的回傳值要等於8個"00000000"才能入帳
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

public class IchF037 extends AccessDAO {
    private String progname = "遞送自動加值結果(A07B)處理  112/11/24 V1.00.02";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;

    String hCallBatchSeqno = "";

    String hTempNotifyDate     = "";
    String hNextNotifyDate     = "";
    String hBusiBusinessDate   = "";
    String hTempSystemDate     = "";
    String hTempNotifyTime     = "";
    String hAdd2SystemDate     = "";
    String hTnlgPerformFlag    = "";
    String hTnlgNotifyDate     = "";
    String hTnlgCheckCode      = "";
    String hTnlgProcFlag       = "";
    String hTnlgRowid           = "";
    String hTnlgFileName       = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData        = "";
    String hOrgdRptRespCode   = "";
    int    hCnt                  = 0;
    int    hErrCnt              = 0;
    double hPostTotRecord = 0;
    double hPostTotAmt = 0;

    String hIchCardNo       = "";
    String hIchAddvalueDate = "";
    String hCardNo       = "";
    String hAgencyCd     = "";
    String hStoreCd      = "";
    String hMerchineType = "";
    String hMerchineNo   = "";
    String hSafeNo       = "";
    String hTxnDate      = "";
    String hTxnTime      = "";
    int    hTxnAmt       = 0;
    int    hBalanceAmt   = 0;
    String hAuthCode     = "";
    String hRtnValue     = "";
    String hOpMark       = "";
    String hOnlineMark   = "";
    String hRrnNo        = "";
    String hSysDate      = "";
    String hSysTime      = "";
    String hStoreName = "";
    String hRespCodeIch = "";

    String hTempBatchSeq  = "";
    String tmpstr1           = "";
    String tmpstr2           = "";
    int    forceFlag        = 0;
    int    totCnt           = 0;
    int    succCnt          = 0;
    int    hTnlgRecordCnt = 0;
    int    totalCnt         = 0;
    String tmpstr            = "";
    String hHash            = "";

    String out = "";

    Buf1 dtl = new Buf1();

    public int mainProcess(String[] args) {

        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ichF037 [[notify_date][fo1yy_flag]] [force_flag]", "");
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

            tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A07B", comc.ICH_BANK_ID3, hTempNotifyDate);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            deleteIchA07bAdd();
            deleteIchOrgdataLog();

            fileOpen();

            updateIchNotifyLogA();

            showLogMessage("I", "", String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
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
    void deleteIchA07bAdd() throws Exception {
        daoTable = "ich_a07b_add a";
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
            comcr.errRtn("update_ich_notify_log not found!", "", hCallBatchSeqno);
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
        tmpstr1 = String.format("ARPB_%3.3s_%8.8s_A07B", comc.ICH_BANK_ID3, hNextNotifyDate);
        String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = temstr2;

        hHash = "0000000000000000000000000000000000000000";
        tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%77.77s%-40.40s\r\n", "A07B", "02", "0001", comc.ICH_BANK_ID3,
                "00000000", " ", hHash);

        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));

        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            str600 = comc.rtrim(str600);
            if (str600.substring(0, 1).equals("H"))
                continue;

            totalCnt++;

            initIchA07bAdd();

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            hOrgdOrgData = str600;
            hOrgdRptRespCode = "0";

            hIchCardNo = comc.rtrim(dtl.ichCardNo);
            hAgencyCd = comc.rtrim(dtl.agencyCd);
            hStoreCd = comc.rtrim(dtl.storeCd);
            hMerchineType = comc.rtrim(dtl.merchineType);
            hMerchineNo = comc.rtrim(dtl.merchineNo);
            hSafeNo = comc.rtrim(dtl.safeNo);
            hTxnDate = comc.rtrim(dtl.txnDate); /* 自動加值交易日期 */
            hTxnTime = comc.rtrim(dtl.txnTime);
            hTxnAmt = comc.str2int(comc.rtrim(dtl.txnAmt));
            hBalanceAmt = comc.str2int(comc.rtrim(dtl.balanceAmt));
            hAuthCode = comc.rtrim(dtl.authCode);
            hRtnValue = comc.rtrim(dtl.rtnValue);
            hOpMark = comc.rtrim(dtl.opMark); /* 0:一般交易 1:剔退後再提示 */
            hOnlineMark = comc.rtrim(dtl.onlineMark);
            hRrnNo = comc.rtrim(dtl.rrnNo);
            hSysDate = comc.rtrim(dtl.sysDate);
            hSysTime = comc.rtrim(dtl.sysTime);

            if (!str600.substring(0, 1).equals("D")) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }

            if (selectIchCard() != 0) {   //檢查卡號是否存在及檢查剔退
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }

            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnDate));
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
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
            

            selectIchOppostLog();         /* 檢查剔退004: 掛失後三小時後交易 */
            if(hOpMark.equals("0")) {
                selectIchA07bAdd();       /* 檢查剔退010: 超過每日離線交易次數 */
                if(hOnlineMark.equals("0") && hTxnAmt != 500) {
                    hRespCodeIch = "012"; /* 檢查剔退012: 離線加值金額非500 */
                }
            }

            if ("000".equals(hRespCodeIch)) {
            	hOrgdRptRespCode = "0";
            } else if ("0".equals(hOrgdRptRespCode)) {
            	hOrgdRptRespCode = "1";
            }
            
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hTxnAmt;

            selectIchA01bAgent();
            insertIchOrgdataLog();

            insertIchA07bAdd();

            if (hOrgdRptRespCode.equals("0"))
                succCnt++;

            String buf = String.format(
                    "D%-16.16s%-6.6s%-20.20s%-20.20s%-8.8s%-16.16s%-14.14s%-8.8s%-3.3s%-12.12s%1.1s%14.14s\r\n", hIchCardNo,
                    hAuthCode, hAgencyCd, hStoreCd, hMerchineNo, hSafeNo, hTxnDate + hTxnTime, dtl.txnAmt,
                    hRespCodeIch, hRrnNo, hOnlineMark, hSysDate + hSysTime);
            lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

            allData += buf;
        }

        if (totalCnt > 0) {
            insertBilPostcntl();
        }

        closeInputText(br);
        moveBackup(hTnlgFileName);

        if (totalCnt > 0) {
            hHash = comc.encryptSHA(allData, "SHA-1", "MS950");
            tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%77.77s%-40.40s\r\n", "A07B", "02", "0001", comc.ICH_BANK_ID3,
                    comm.fillZero(Integer.toString(totalCnt), 8), " ", hHash);
            lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
        }

        comc.writeReport(out, lpar, "MS950", false);

    }
    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        String hBiunConfFlag = "";

        sqlCmd  = "select conf_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, "ICAH" );
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", "ICAH" , hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

        setValue("batch_date", hBusiBusinessDate);
        setValue("batch_unit", "IC");
        setValue("batch_seq", hTempBatchSeq);
        setValue("batch_no", hBusiBusinessDate + "IC" + hTempBatchSeq);
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
        sqlCmd += "  and batch_date = ? ";
        setString(1, "ICAH");
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
    void initIchA07bAdd() throws Exception {
        hIchCardNo = "";
        hCardNo = "";
        hAgencyCd = "";
        hStoreCd = "";
        hMerchineType = "";
        hMerchineNo = "";
        hSafeNo = "";
        hTxnDate = "";
        hTxnTime = "";
        hTxnAmt = 0;
        hBalanceAmt = 0;
        hAuthCode = "";
        hRtnValue = "";
        hOpMark = "";
        hOnlineMark = "";
        hRrnNo = "";
        hSysDate = "";
        hSysTime = "";
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
        hIchAddvalueDate = "";
        hRespCodeIch     = "000";

        sqlCmd = "select card_no,     ";
        sqlCmd += "      lock_flag,   ";
        sqlCmd += "      return_flag, ";
        sqlCmd += "      NEW_END_DATE,"; /* 卡片效期 */
        sqlCmd += "      ADDVALUE_DATE,   ";
        sqlCmd += "      BLACKLT_FLAG,    ";
        sqlCmd += "      BALANCE_RTN_DATE "; /* 餘額轉置回饋日期 */
        sqlCmd += " from ich_card  ";
        sqlCmd += "where ich_card_no = ? ";
        setString(1, hIchCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCardNo = getValue("card_no");
        } else
            return 1;

        if(hOpMark.equals("0")) {
            if(getValue("lock_flag").equals("Y")) {
                hRespCodeIch = "001"; /* 001: 鎖卡後交易 */
                return 0;
            }
            if(getValue("return_flag").equals("Y")) {
                hRespCodeIch = "002"; /* 002: 退卡後交易 */
                return 0;
            }
            if(comc.str2int(hTxnDate) > comc.str2int(getValue("NEW_END_DATE"))) {
                hRespCodeIch = "003"; /* 003: 屆期後交易 */
                return 0;
            }
            if(getValue("BLACKLT_FLAG").equals("Y")) {
                hRespCodeIch = "005"; /* 005: 鎖卡名單生效後交易 */
                return 0;
            }
        }

        String balRtnDate = getValue("BALANCE_RTN_DATE");
        if(balRtnDate.equals("") == false && comc.str2int(hTxnDate) > comc.str2int(balRtnDate)) {
            hRespCodeIch = "014"; /* 014: 餘額返還後之自動加值 */
            return 0;
        }
        hIchAddvalueDate = getValue("addvalue_date");

        return 0;
    }

    /***********************************************************************/
    void selectIchOppostLog() throws Exception {

        sqlCmd = "select curr_code from crd_card a, ich_card b where a.card_no = b.card_no and ich_card_no = ? ";
        setString(1, hIchCardNo);
        selectTable();
        if(getValue("curr_code").equals("0"))  /* 排除取消卡片掛失 */
            return;

        /* 抓掛失時間 */
        sqlCmd = "select crt_date, crt_time from ich_oppost_log where ich_card_no = ? and update_code = '1' ";
        setString(1, hIchCardNo);
        selectTable();
        if(notFound.equals("Y")) {
            return;
        }
        if(comc.str2int(hTxnDate) > comc.str2int(getValue("crt_date")) ) {
            hRespCodeIch = "004"; /* 004: 掛失後三小時後交易 */
            return;
        }
        if(comc.str2int(hTxnTime) - comc.str2int(getValue("crt_time")) > 30000 ) {
            hRespCodeIch = "004"; /* 004: 掛失後三小時後交易 */
            return;
        }

    }

    /***********************************************************************/
    void selectIchA07bAdd() throws Exception {

        sqlCmd  = "select count(*) as cnt ";
        sqlCmd += "  from ich_a07b_add ";
        sqlCmd += " where ich_card_no = ? ";
        sqlCmd += "   and txn_date = ? ";
        setString(1, hIchCardNo);
        setString(2, hTxnDate);
        selectTable();

        /* 相同卡號每日大於一筆 */
        if( hOnlineMark.equals("0") && comc.str2int(getValue("cnt")) > 0  ) {
            hRespCodeIch = "010"; /* 010: 超過每日離線交易次數 */
            return;
        }

    }

    /***********************************************************************/
    void insertIchOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "A07B");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ich_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ich_orgdata_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertIchA07bAdd() throws Exception {

        if (hIchAddvalueDate.length() == 0) {
            daoTable  = "ich_card";
            updateSQL = "addvalue_date     = ? ";
            whereStr  = "where ich_card_no = ? ";
            setString(1, sysDate);
            setString(2, hIchCardNo);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_ich_card not found!", hIchCardNo, hCallBatchSeqno);
            }
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
        setValue("auth_code"     , hAuthCode);
        setValue("op_mark", hOpMark);
        setValue("online_mark", hOnlineMark);
        setValue("rrn_no", hRrnNo);
        setValue("sys_date", hSysDate);
        setValue("sys_time", hSysTime);
        setValue("file_name", hTnlgFileName);
        setValue("crt_date", sysDate);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        //new column
        setValue("BATCH_NO", hBusiBusinessDate + "IC" + hTempBatchSeq); // 批號
        setValue("SEQ_NO", String.format("%d", totalCnt)); // 序號
        setValue("CARD_NO", hCardNo); // CARD_NO
        setValue("BILL_TYPE", "ICAH"); // BILL_TYPE
        setValue("TXN_CODE", "05"); // TXN_CODE
        setValue("MCHT_NO", "ICAH8003"); // MCHT_NO
        setValue("MCHT_CATEGORY", "4100"); // MCHT_CATEGORY
        setValue("MCHT_CHI_NAME", String.format("愛金卡儲值金%s", hStoreName)); // MCHT_CHI_NAME
        setValue("BILL_DESC", String.format("愛金卡儲值金%s", hStoreName)); // BILL_DESC
        setValue("REFERENCE_NO", ""); // REFERENCE_NO
        
        //回覆碼要等於"00000000"才需要入帳 (20231124)
        if(comc.getSubString(hRtnValue, 0, 8).equals("00000000") ||
                comc.getSubString(hRtnValue, 0, 1).equals("F") ||
                comc.getSubString(hRtnValue, 0, 1).equals("C")) {
            setValue("POST_FLAG", "N"); // 處理註記
            setValue("resp_code_ich", hRespCodeIch); //剔退碼
        } else {
            setValue("POST_FLAG", "Y"); // 處理註記
            setValue("resp_code_ich", "999"); //剔退碼
        }

        //new column
        daoTable = "ich_a07b_add";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ich_a07b_add duplicate!", "", hCallBatchSeqno);
        }

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
        IchF037 proc = new IchF037();
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
        String merchineNo;
        String safeNo;
        String txnDate;
        String txnTime;
        String txnAmt;
        String balanceAmt;
        String authCode;
        String rtnValue;    
        String opMark;
        String onlineMark;
        String rrnNo;
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
        dtl.merchineNo = comc.subMS950String(bytes, 58, 8);
        dtl.safeNo = comc.subMS950String(bytes, 66, 16);
        dtl.txnDate = comc.subMS950String(bytes, 82, 8);
        dtl.txnTime = comc.subMS950String(bytes, 90, 6);
        dtl.txnAmt = comc.subMS950String(bytes, 96, 8);
        dtl.balanceAmt = comc.subMS950String(bytes, 104, 8);
        dtl.authCode = comc.subMS950String(bytes, 112, 6);
        dtl.rtnValue = comc.subMS950String(bytes, 118, 8);
        dtl.opMark = comc.subMS950String(bytes, 126, 1);
        dtl.onlineMark = comc.subMS950String(bytes, 127, 1);
        dtl.rrnNo = comc.subMS950String(bytes, 128, 12);
        dtl.sysDate = comc.subMS950String(bytes, 140, 8);
        dtl.sysTime = comc.subMS950String(bytes, 148, 6);
    }
}
