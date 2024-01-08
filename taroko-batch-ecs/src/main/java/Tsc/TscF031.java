/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/16  V1.00.01    Brain     error correction                          *
*  109-11-17  V1.00.02    tanwei    updated for project coding standard       *
*  110/01/12  V1.00.03    Wilson    tsc_ccas_log change to cca_auth_txlog     *
*                                                                             *
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
import com.CommFunction;
import com.CommIps;

/*悠遊卡自動儲值請款檔(CGEC)媒體接收處理程式*/
public class TscF031 extends AccessDAO {

    private final String progname = "悠遊卡自動儲值請款檔(CGEC)媒體接收處理程式  110/01/12 V1.00.03";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommCrdRoutine comcr  = null;
    CommIps        comips = new CommIps();

    int debug = 0;

    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";
    String hTempNotifyDate = "";
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
    int tempInt = 0;
    String hCgecPurchaseDate = "";
    String hCgecTscCardNo = "";
    double hCgecDestinationAmt = 0;
    String hCgecCardNo = "";
    String hTardAddvalueDate = "";
    String hTardBalanceDate = "";
    String hTardBlackltSDate = "";
    String hTardBlackltEDate = "";
    int hTempDiffDays = 0;
    String hTardCurrentCode = "";
    String hTardNewEndDate = "";
    String hTardLockDate = "";
    String hTardAutoloadFlag = "";
    String hTardReturnDate = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hBatchSeq = "";
    String hCgecBillType = "";
    String hCgecTscTxCode = "";
    String hCgecPurchaseTime = "";
    String hCgecMerchantChiName = "";
    String hCgecBillDesc = "";
    String hCgecTrafficCd = "";
    String hCgecTrafficAbbr = "";
    String hCgecAddrCd = "";
    String hCgecAddrAbbr = "";
    String hCgecTsccDataSeqno = "";
    String hCgecOnlineMark = "";
    String fixBillType = "";
    double hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hBiunConfFlag = "";
    String hEctiChgbackReason = "";
    int hCnt = 0;
    int hErrCnt = 0;
    String hTempX08 = "";
    String hMaxDate = "";
    String hTempBlackFlag = "";
    String hOldSendDates = "";
    String hOldSendDateEe = "";
    String hCgecPostFlag = "";
    int hCgecSeqNo = 0;
    String hCgecFileName = "";
    String hCgecTscError = "";
    String hCgecTscNotiDate = "";
    String hCgecTscRespCode = "";
    String hCgecReturnSource = "";
    double hCgecServiceAmt = 0;
    String hCgecMerchantNo = "";
    String hCgecMerchantCategory = "";
    String hCgecDestinationCurrency = "";
    String hCgecTransactionCode = "";
    String hCgecBatchNo = "";
    String hTempBatchNo  = "";

    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";

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
                comc.errExit("Usage : TscF031 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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
if(debug==1) showLogMessage("I", "", "888 1111 date=["+hTempNotifyDate+"]");
            selectPtrBusinday();
if(debug==1) showLogMessage("I", "", "888 2222 date=["+hTempNotifyDate+"]");

            tmpstr1 = String.format("CGEC.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            //TscF021不執行,此段不用跑
            /*  
            int rtn = selectTscOrgdataLog();
            if (rtn != 0) {
                backupRtn();
                comcr.errRtn(String.format("TscF031 檢核有錯本程式不執行..[%d]", rtn), "", hCallBatchSeqno);
            }
            */

            fixBillType = "TSCC";
            hCgecBillType = fixBillType;

            deleteBilPostcntl();
            deleteTscEctiLog();
            deleteTscCgecAll();
            
            deleteTscOrgdataLog();

            selectPtrBillunit();

            hPostTotRecord = hPostTotAmt = 0;
            fileOpen();
            updateTscNotifyLoga();

            backupRtn();

            showLogMessage("I", "", String.format("Total process record[%d] fail_cnt[%d]"
                                  , totalCnt, totalCnt - succCnt));
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
        hAdd2SystemDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)) , '' "
                + ", to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,4)-'1530') , 1 , sysdate"
                + ", sysdate - 1 days) , 'yyyymmdd') , ?) h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_system_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time,";
        sqlCmd += " to_char(add_months(sysdate,2),'yyyymmdd') h_add2_system_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only";
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
            hTempSystemDate = getValue("h_temp_system_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
            hAdd2SystemDate = getValue("h_add2_system_date");
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
        sqlCmd += " rowid rowid";
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
        } else {
            comcr.errRtn(String.format("未有[%s]檔案記錄 , 請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
        }

        if (hTnlgPerformFlag.toCharArray()[0] != 'Y') {
            comcr.errRtn(String.format("通知檔收檔發生問題,[%s]暫不可處理 , 請通知相關人員處理(error)", hTnlgFileName), "",
                    hCallBatchSeqno);
        }
        if (hTnlgProcFlag.toCharArray()[0] == '0') {
            comcr.errRtn(String.format("通知檔收檔中[%s] , 請通知相關人員處理(error)", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.toCharArray()[0] >= '2') {
            comcr.errRtn(String.format("[%s]自動儲值請款檔已處理過 , 請通知相關人員處理(error)", hTnlgFileName), "",
                    hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "",
                    String.format("[%s]自動儲值請款檔整檔處理失敗  , 錯誤代碼[%s]", hTnlgFileName, hTnlgCheckCode));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    int selectTscOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt,";
        sqlCmd += " sum(decode(rpt_resp_code,'0000',0,1)) h_err_cnt ";
        sqlCmd += " from tsc_orgdata_log  ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("TscF031檢核程式未執行或該日無資料需處理...", hTnlgFileName, hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt     = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        }

        return (hErrCnt);
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
        daoTable = "tsc_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
    }
    
    /***********************************************************************/
    void deleteBilPostcntl() throws Exception {
        daoTable = "bil_postcntl";
        whereStr = "where this_close_date = ?  ";
        whereStr += "and batch_unit = substr(?,1,2)  ";
        whereStr += "and mod_pgm = ? ";
        setString(1, hTempNotifyDate);
        setString(2, fixBillType);
        setString(3, javaProgram);
        deleteTable();

    }

    /***********************************************************************/
    void deleteTscEctiLog() throws Exception {
        daoTable = "tsc_ecti_log a";
        whereStr = "where crt_date  = ?  ";
        whereStr += " and tran_code = '7229' ";
        setString(1, hTempSystemDate);
        deleteTable();
    }

    /***********************************************************************/
    void deleteTscCgecAll() throws Exception {
        daoTable = "tsc_cgec_all a";
        whereStr = "where to_number(a.tscc_data_seqno) in (select b.tscc_data_seqno from tsc_orgdata_log b "
                + " where b.file_name  = ?  and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";

        sqlCmd = "select conf_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, fixBillType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = " tsc_notify_log";
        updateSQL = " proc_flag = '2',";
        updateSQL += " proc_date = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " proc_time = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where file_name  = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", hTnlgFileName, hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";
        tmpstr1 = String.format("%s", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("Open File=[%s]", temstr1));
        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }

        selectBilPostcntl();
        
        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || 
                (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            initTscCgecAll();

            splitBuf1(str600);

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            tmpstr1 = hOrgdTsccDataSeqno;
            hCgecTsccDataSeqno = tmpstr1;

            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hCgecTscTxCode = comc.rtrim(dtl.tscTxCode);
            if ((!hCgecTscTxCode.equals("8209")) && (!hCgecTscTxCode.equals("6209"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hCgecTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseDate));
            hCgecPurchaseDate = tmpstr1;
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseTime));
            hCgecPurchaseTime = tmpstr1;
            if (!comc.commTimeCheck(tmpstr1)) {
                hOrgdRptRespCode = "0204";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hCgecDestinationAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hCgecDestinationAmt;
            /*************************************************************************/
            hCgecTrafficCd   = comc.rtrim(dtl.trafficCd);
            hCgecTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
            hCgecAddrCd      = comc.rtrim(dtl.addrCd);
            hCgecAddrAbbr    = comc.rtrim(dtl.addrAbbr);
            hCgecOnlineMark  = comc.rtrim(dtl.onlineMark);
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 110);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            
            if (comc.subMS950String(str600.getBytes("MS950"), 110, 110 + 16).equals(tmpstr2) == false) {
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
            /* 8209 6209:自動剔退後的再提示 */
            if (!hCgecTscTxCode.equals("6209")) {
            	
            	/*2023/06/26授權比對不要做
                if (hCgecOnlineMark.equals("1")) {
                    tempInt = 0;
                    sqlCmd = "select count(*) temp_int ";
                    sqlCmd += " from cca_auth_txlog a,tsc_card b  ";
                    sqlCmd += "where a.card_no = b.card_no  ";
                    sqlCmd += "  and a.tx_date = ?  ";
                    sqlCmd += "  and b.tsc_card_no = ?  ";
                    sqlCmd += "  and a.nt_amt = ?  ";
                    sqlCmd += "  and a.trans_code = 'TA'  ";
                    sqlCmd += "  and a.auth_status_code = '00'  ";
                    sqlCmd += "  and decode(a.reversal_flag, '', 'N', a.reversal_flag) = 'N' ";
                    setString(1, hCgecPurchaseDate);
                    setString(2, hCgecTscCardNo);
                    setDouble(3, hCgecDestinationAmt);
                    int recordCnt = selectTable();
                    if (notFound.equals("Y")) {
                        comcr.errRtn("select_cca_auth_txlog not found!", "", hCallBatchSeqno);
                    }
                    if (recordCnt > 0) {
                        tempInt = getValueInt("temp_int");
                    }
                    if (tempInt == 0) {
                        hEctiChgbackReason = "1796";
                        insertTscEctiLog();
                        insertTscOrgdataLog();
                        continue;
                    }
                }
                */

                hTempDiffDays = comcr.calDays(hCgecPurchaseDate,hBusiBusinessDate);

                if (hTempDiffDays > 45) { /* RECS-s1031113-081 請款期限由20延長至45天 */
                    hEctiChgbackReason = "1795";
                    insertTscEctiLog();
                    insertTscOrgdataLog();
                    continue;
                }
                /*** 交易時間超過掛卡三小時 ***/
                if (selectTscOppostLog() > 0) {
                    hEctiChgbackReason = "1797";
                    insertTscEctiLog();
                    insertTscOrgdataLog();
                    continue;
                }
                /*** 交易時間超過餘轉 ***/
                if (selectTscBtrqLog() > 0) {
                    hEctiChgbackReason = "1798";
                    insertTscEctiLog();
                    insertTscOrgdataLog();
                    continue;
                }

                if (hOrgdRptRespCode.equals("0000")) {
                    if (hTardBlackltSDate.length() > 0) {
                        if ((hCgecPurchaseDate.compareTo(hTardBlackltSDate) >= 0) &&
                            (hTardBlackltEDate.length() == 0 || 
                             hCgecPurchaseDate.compareTo(hTardBlackltEDate) <= 0)) {
                            hEctiChgbackReason = "1791";
                            insertTscEctiLog();
                            insertTscOrgdataLog();
                            if((hCgecPurchaseDate.compareTo(hTardBlackltSDate) >= 0) &&
                               (hTardBlackltEDate.length() == 0 && hCgecOnlineMark.equals("0"))) {
                                processTscBkecExpt();
                            }
                            continue;
                        }
                    }
                }
            }
            if (hTardBlackltSDate.length() > 0 &&
            	(hCgecPurchaseDate.compareTo(hTardBlackltSDate) >= 0) &&
                (hTardBlackltEDate.length() == 0 && hCgecOnlineMark.equals("0"))) {
                processTscBkecExpt();
            }
            insertTscOrgdataLog();
            insertTscCgecAll();
            if (hTardAddvalueDate.length() == 0) {
                daoTable = "tsc_card";
                updateSQL = "addvalue_date     = ? ";
                whereStr = "where tsc_card_no = ? ";
                setString(1, hTempSystemDate);
                setString(2, hCgecTscCardNo);
                updateTable();
                if (notFound.equals("Y")) {
                    comcr.errRtn("update_tsc_card not found!", "", hCallBatchSeqno);
                }
            }
            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }

        if (totalCnt > 0)
            insertBilPostcntl();
        closeInputText(br);
    }
    
    /***********************************************************************/
    int selectTscCard() throws Exception {
        hTardAddvalueDate = "";
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hTardCurrentCode = "";
        hTardNewEndDate = "";
        hTardLockDate = "";
        hTardAutoloadFlag = "";
        hTardReturnDate = "";
        hTardBalanceDate = "";

        sqlCmd  = "select card_no,";
        sqlCmd += " addvalue_date,";
        sqlCmd += " balance_date,";
        sqlCmd += " blacklt_s_date,";
        sqlCmd += " blacklt_e_date,";
        sqlCmd += " current_code,";
        sqlCmd += " new_end_date,";
        sqlCmd += " lock_date,";
        sqlCmd += " autoload_flag,";
        sqlCmd += " return_date,";
        sqlCmd += " balance_date ";
        sqlCmd += "  from tsc_card ";
        sqlCmd += " where tsc_card_no = ? ";
        setString(1, hCgecTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCgecCardNo        = getValue("card_no");
            hTardAddvalueDate  = getValue("addvalue_date");
            hTardBalanceDate   = getValue("balance_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTardCurrentCode   = getValue("current_code");
            hTardNewEndDate   = getValue("new_end_date");
            hTardLockDate      = getValue("lock_date");
            hTardAutoloadFlag  = getValue("autoload_flag");
            hTardReturnDate    = getValue("return_date");
            hTardBalanceDate   = getValue("balance_date");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectTscOppostLog() throws Exception {
        int hCnt = 0;
        sqlCmd = "select count(*) h_cnt ";
        sqlCmd += " from tsc_oppost_log  ";
        sqlCmd += "where tsc_card_no = ?  ";
        /*** 2為撤掛 ***/
        sqlCmd += "  and decode(proc_flag, '', '2', proc_flag) <> '2'  ";
        sqlCmd += "  and resp_code = '00'  ";
        sqlCmd += "  and oppost_date != '' ";
        /*** 3小時為0.125天 ***/
        sqlCmd += "  and to_date( ? || ? ,'yyyymmddhh24miss') >= (to_date(proc_date||proc_time,'yyyymmddhh24miss') + 3 hour ) ";
        setString(1, hCgecTscCardNo);
        setString(2, hCgecPurchaseDate);
        setString(3, hCgecPurchaseTime);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_oppost_log not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
        return (hCnt);
    }

    /***********************************************************************/
    int selectTscBtrqLog() throws Exception {
        int hCnt = 0;
        sqlCmd = "select count(*) h_cnt ";
        sqlCmd += " from tsc_btrq_log  ";
        sqlCmd += "where tsc_card_no = ?  ";
        sqlCmd += "  and balance_date != '' ";
        sqlCmd += "  and balance_date < ? ";
        setString(1, hCgecTscCardNo);
        setString(2, hCgecPurchaseDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_btrq_log not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
        return (hCnt);
    }

    /***********************************************************************/
    void insertTscEctiLog() throws Exception {

        daoTable = "tsc_ecti_log";
        setValue("crt_date", hTempSystemDate);
        setValue("crt_time", hTempNotifyTime);
        setValue("tran_code", "7229");
        setValue("tsc_card_no", hCgecTscCardNo);
        setValue("tran_date", hCgecPurchaseDate);
        setValue("tran_time", hCgecPurchaseTime);
        setValueDouble("tran_amt", hCgecDestinationAmt);
        setValue("traff_code", hCgecTrafficCd);
        setValue("place_code", hCgecAddrCd);
        setValue("traff_subname", hCgecTrafficAbbr);
        setValue("place_subname", hCgecAddrAbbr);
        setValue("chgback_reason", hEctiChgbackReason);
        setValue("file_name", hTnlgFileName);
        setValue("proc_flag", "N");
        setValue("mod_time" , sysDate + sysTime);
        setValue("mod_pgm"  , javaProgram);
        setValue("online_mark", hCgecOnlineMark);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_ecti_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void processTscBkecExpt() throws Exception {
        if ((hTardCurrentCode.equals("0"))
                && (comcr.str2long(hTardNewEndDate) > comcr.str2long(hBusiBusinessDate))
                && (hTardLockDate.length() == 0 && hTardReturnDate.length() == 0
                        && hTardBalanceDate.length() == 0 && hTardAutoloadFlag.equals("Y"))) {
            hTempX08 = "";
            sqlCmd = "select min(decode(crt_date, '', '20000101', crt_date)) h_temp_x08 ";
            sqlCmd += " from tsc_bkec_log  ";
            sqlCmd += "where tsc_card_no = ? ";
            setString(1, hCgecTscCardNo);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                hTempX08 = getValue("h_temp_x08");
            }

            if ((comcr.str2long(hBusiBusinessDate) >= comcr.str2long(hTempX08)) && hTempX08.length() > 0) {
                int rtn = insertTscBkecExpt();
                if (rtn == -1)
                    chkTscBkecExpt();
            }
        }
    }

    /***********************************************************************/
    int insertTscBkecExpt() throws Exception {

        hMaxDate = hAdd2SystemDate;
        if (comcr.str2long(hTardNewEndDate) < comcr.str2long(hAdd2SystemDate)) {
            hMaxDate = hTardNewEndDate;
        }
        daoTable = "tsc_bkec_expt";
        setValue("tsc_card_no", hCgecTscCardNo);
        setValue("card_no", hCgecCardNo);
        setValue("black_date", hBusiBusinessDate);
        setValue("black_user_id", "batch");
        setValue("black_remark", "離線加值交易");
        setValue("crt_user", javaProgram);
        setValue("crt_date", hBusiBusinessDate);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        setValue("black_flag", "1");
        setValue("send_date_s", hTempSystemDate);
        setValue("send_date_e", hMaxDate);
        setValue("from_type", "2");
        insertTable();
        if (dupRecord.equals("Y")) {
            return -1;
        }
        return 0;
    }

    /***********************************************************************/
    void chkTscBkecExpt() throws Exception {
        hTempBlackFlag = "";
        hOldSendDates = "";
        hOldSendDateEe = "";
        sqlCmd = "select black_flag,";
        sqlCmd += " send_date_s,";
        sqlCmd += " send_date_e ";
        sqlCmd += " from tsc_bkec_expt  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hCgecTscCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_bkec_expt not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempBlackFlag = getValue("black_flag");
            hOldSendDates = getValue("send_date_s");
            hOldSendDateEe = getValue("send_date_e");
        }
        switch (hTempBlackFlag.toCharArray()[0]) {
        case '1':
            if (comcr.str2long(hMaxDate) > comcr.str2long(hOldSendDateEe)) {
                updateTscBkecExpt();
            }
            break;
        case '2':
            updateTscBkecExpt();
            break;
        case '3':
            if (comcr.str2long(hMaxDate) >= comcr.str2long(hOldSendDates)
                    && comcr.str2long(hMaxDate) <= comcr.str2long(hOldSendDateEe)) {
            } else {
                updateTscBkecExpt();
            }
            break;
        }
    }

    /***********************************************************************/
    void updateTscBkecExpt() throws Exception {
        daoTable = "tsc_bkec_expt";
        updateSQL = " black_date    = ?,";
        updateSQL += " black_user_id = 'batch',";
        updateSQL += " black_remark  = '離線加值交易',";
        updateSQL += " crt_user      = 'TscF031',";
        updateSQL += " crt_date      = ?,";
        updateSQL += " black_flag    = '1',";
        updateSQL += " send_date_s   = ?,";
        updateSQL += " send_date_e   = ?,";
        updateSQL += " from_type     = '2',";
        updateSQL += " mod_pgm       = 'TscF031',";
        updateSQL += " mod_time      = sysdate,";
        updateSQL += " apr_date      ='',";
        updateSQL += " apr_user      =''";
        whereStr = "where tsc_card_no  = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hTempSystemDate);
        setString(4, hMaxDate);
        setString(5, hCgecTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_bkec_expt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "CGEC");
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
    void insertBilPostcntl() throws Exception {
       
        daoTable = "bil_postcntl";
        setValue("batch_date"  , hBusiBusinessDate);
        setValue("batch_unit"  , comc.getSubString(hCgecBillType, 0, 2));
        setValueInt("batch_seq", comc.str2int(hBatchSeq));
        setValue("batch_no"        , hTempBatchNo);
        setValueDouble("tot_record", hPostTotRecord);
        setValueDouble("tot_amt"   , hPostTotAmt);
        setValue("confirm_flag_p"  , hBiunConfFlag.equals("N") ? "Y" : "N");
        setValue("confirm_flag"    , hBiunConfFlag);
        setValue("apr_user", "");
        setValue("apr_date", "");
        setValue("this_close_date", hTempNotifyDate);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate!", hTempBatchNo,hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertTscCgecAll() throws Exception {
        tmpstr1 = String.format("悠遊卡儲值金%s", hCgecTrafficAbbr);
        hCgecMerchantChiName =  String.format("%-40.40s", tmpstr1);
        hCgecBillDesc = hCgecMerchantChiName;
        hCgecSeqNo    = totalCnt;

        setValue("batch_no"     , hTempBatchNo);
        setValueInt("seq_no"    , hCgecSeqNo);
        setValue("card_no"      , hCgecCardNo);
        setValue("tsc_card_no"  , hCgecTscCardNo);
        setValue("bill_type"    , hCgecBillType);
        setValue("txn_code"     , "05"); // transaction_code
        setValue("tsc_tx_code"  , hCgecTscTxCode);
        setValue("purchase_date", hCgecPurchaseDate);
        setValue("purchase_time", hCgecPurchaseTime);
        setValue("mcht_no", "EASY8003"); // merchant_no
        setValue("mcht_category", "4100"); // merchant_category
        setValue("mcht_chi_name", hCgecMerchantChiName); // merchant_chi_name
        setValueDouble("dest_amt", hCgecDestinationAmt); // destination_amt
        setValue("dest_curr", "901"); // destination_currency
        setValue("bill_desc", hCgecBillDesc);
        setValue("traffic_cd", hCgecTrafficCd);
        setValue("traffic_abbr", hCgecTrafficAbbr);
        setValue("addr_cd", hCgecAddrCd);
        setValue("addr_abbr", hCgecAddrAbbr);
        setValue("post_flag", "N");
        setValue("file_name", hTnlgFileName);
        setValue("tsc_error", hOrgdRptRespCode);
        setValue("crt_date", hTempSystemDate);
        setValue("tscc_data_seqno", hCgecTsccDataSeqno);
        setValue("online_mark", hCgecOnlineMark);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_cgec_all";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_cgec_all duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void initTscCgecAll() throws Exception {
        hCgecBatchNo = "";
        hCgecSeqNo = 0;
        hCgecCardNo = "";
        hCgecTscCardNo = "";
        hCgecBillType = fixBillType;
        hCgecTransactionCode = "05";
        hCgecTscTxCode = "";
        hCgecPurchaseDate = "";
        hCgecPurchaseTime = "";
        hCgecMerchantNo = "EASY8003";
        hCgecMerchantCategory = "4100";
        hCgecMerchantChiName = "";
        hCgecDestinationAmt = 0;
        hCgecDestinationCurrency = "901";
        hCgecBillDesc = "";
        hCgecTrafficCd = "";
        hCgecTrafficAbbr = "";
        hCgecAddrCd = "";
        hCgecAddrAbbr = "";
        hCgecPostFlag = "N";
        hCgecFileName = "";
        hCgecTscError = "";
        hCgecTscNotiDate = "";
        hCgecTscRespCode = "";
        hCgecReturnSource = "";
        hCgecServiceAmt = 0;

        hEctiChgbackReason = "";
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String purchaseDate;
        String purchaseTime;
        String destinationAmt;
        String trafficCd;
        String addrCd;
        String trafficAbbr;
        String addrAbbr;
        String tscTxCode;
        String onlineMark;
        String filler0;
        String hashValue;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(purchaseDate, 8);
            rtn += comc.fixLeft(purchaseTime, 6);
            rtn += comc.fixLeft(destinationAmt, 13);
            rtn += comc.fixLeft(trafficCd, 8);
            rtn += comc.fixLeft(addrCd, 6);
            rtn += comc.fixLeft(trafficAbbr, 20);
            rtn += comc.fixLeft(addrAbbr, 20);
            rtn += comc.fixLeft(tscTxCode, 4);
            rtn += comc.fixLeft(onlineMark, 1);
            rtn += comc.fixLeft(filler0, 1);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(filler1, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.attri = comc.subMS950String(bytes, 1, 2);
        dtl.tscCardNo = comc.subMS950String(bytes, 3, 20);
        dtl.purchaseDate = comc.subMS950String(bytes, 23, 8);
        dtl.purchaseTime = comc.subMS950String(bytes, 31, 6);
        dtl.destinationAmt = comc.subMS950String(bytes, 37, 13);
        dtl.trafficCd = comc.subMS950String(bytes, 50, 8);
        dtl.addrCd = comc.subMS950String(bytes, 58, 6);
        dtl.trafficAbbr = comc.subMS950String(bytes, 64, 20);
        dtl.addrAbbr = comc.subMS950String(bytes, 84, 20);
        dtl.tscTxCode = comc.subMS950String(bytes, 104, 4);
        dtl.onlineMark = comc.subMS950String(bytes, 108, 1);
        dtl.filler0 = comc.subMS950String(bytes, 109, 1);
        dtl.hashValue = comc.subMS950String(bytes, 110, 16);
        dtl.filler1 = comc.subMS950String(bytes, 126, 2);
    }

    /*******************************************************************/
    void selectBilPostcntl() throws Exception {
        hBatchSeq = "";
        sqlCmd = "select substr(to_char(nvl(max(batch_seq),0) + 1,'0000'),2,4) h_batch_seq ";
        sqlCmd += " from bil_postcntl ";
        sqlCmd += "where batch_unit = substr(?,1,2) ";
        sqlCmd += "  and batch_date = ?";
        setString(1, fixBillType);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBatchSeq  = getValue("h_batch_seq");
        }
        hTempBatchNo = hBusiBusinessDate + comc.getSubString(hCgecBillType,0,2)
                                               + hBatchSeq;


    }

    /***************************************************************************/

    public static void main(String[] args) throws Exception {
        TscF031 proc = new TscF031();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
