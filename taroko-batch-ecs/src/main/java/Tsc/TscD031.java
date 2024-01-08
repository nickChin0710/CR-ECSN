/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/22  V1.00.00    Wendy Lu                     program initial        *
*  111/08/08  V1.00.01    JeffKung                     update                 *
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


public class TscD031 extends AccessDAO {

    private final String progname = "悠遊VD卡自動儲值請款檔(DCCG)媒體接收處理程式  111/08/08 V1.00.01";
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
    String hDccgPurchaseDate = "";
    String hDccgTscCardNo = "";
    double hDccgDestinationAmt = 0;
    String hDccgCardNo = "";
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
    String hDccgBillType = "";
    String hDccgTscTxCode = "";
    String hDccgPurchaseTime = "";
    String hDccgMerchantChiName = "";
    String hDccgBillDesc = "";
    String hDccgTrafficCd = "";
    String hDccgTrafficAbbr = "";
    String hDccgAddrCd = "";
    String hDccgAddrAbbr = "";
    String hDccgTsccDataSeqno = "";
    String hDccgOnlineMark = "";
    String fixBillType = "";
    double hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hBiunConfFlag = "";
    String hDctiChgbackReason = "";
    int hCnt = 0;
    int hErrCnt = 0;
    String hTempX08 = "";
    String hMaxDate = "";
    String hTempBlackFlag = "";
    String hOldSendDates = "";
    String hOldSendDateEe = "";
    String hDccgPostFlag = "";
    int hDccgSeqNo = 0;
    String hDccgFileName = "";
    String hDccgTscError = "";
    String hDccgTscNotiDate = "";
    String hDccgTscRespCode = "";
    String hDccgReturnSource = "";
    double hDccgServiceAmt = 0;
    String hDccgMerchantNo = "";
    String hDccgMerchantCategory = "";
    String hDccgDestinationCurrency = "";
    String hDccgTransactionCode = "";
    String hDccgBatchNo = "";
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
                comc.errExit("Usage : TscD031 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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
            
			// 設定為debug mode
			for (int argi = 0; argi < args.length; argi++) {
				if (args[argi].equals("debug")) {
					debug = 1;
				}
			}
            
            if(debug==1) 
            showLogMessage("I", "", "888 1111 date=["+hTempNotifyDate+"]");
            selectPtrBusinday();
            if(debug==1) 
            showLogMessage("I", "", "888 2222 date=["+hTempNotifyDate+"]");

            tmpstr1 = String.format("DCCG.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format("處理檔案=[%s]", tmpstr1));

            fixBillType = "TSCC";
            hDccgBillType = fixBillType;

            deleteBilPostcntl();
            deleteTscDctiLog();
            deleteTscDccgAll();
            deleteTscOrgdataLog();

            selectPtrBillunit();

            hPostTotRecord = hPostTotAmt = 0;
            fileOpen();
            updateTscNotifyLoga();

            backupRtn();

            showLogMessage("I", "", String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
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
        hBusiBusinessDate = "";
        hAdd2SystemDate = "";
        sqlCmd = "select business_date, ";
        sqlCmd += "decode( cast(? as varchar(8)) , '' "
                + ",to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,4)-'1530') , 1 , sysdate "
                + ",sysdate - 1 days) , 'yyyymmdd') , ?) h_temp_notify_date, ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time, ";
        sqlCmd += "to_char(add_months(sysdate,2),'yyyymmdd') h_add2_system_date ";
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
        sqlCmd = "select count(*) h_cnt, ";
        sqlCmd += "sum(decode(rpt_resp_code,'0000',0,1)) h_err_cnt ";
        sqlCmd += "from tsc_orgdata_log ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("TscD031檢核程式未執行或該日無資料需處理...", hTnlgFileName, hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
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
        daoTable = "bil_postcntl ";
        whereStr = "where this_close_date = ? ";
        whereStr += "and batch_unit = substr(?,1,2) ";
        whereStr += "and mod_pgm = ? ";
        setString(1, hTempNotifyDate);
        setString(2, fixBillType);
        setString(3, javaProgram);
        deleteTable();

    }

    /***********************************************************************/
    void deleteTscDctiLog() throws Exception {
        daoTable = "tsc_dcti_log a ";
        whereStr = "where crt_date = ? ";
        whereStr += "and crt_time = ? ";
        whereStr += "and tran_code = '7227' ";
        setString(1, hTempSystemDate);
        setString(2, hTempNotifyTime);
        deleteTable();
    }

    /***********************************************************************/
    void deleteTscDccgAll() throws Exception {
        daoTable = "tsc_dccg_all a ";
        whereStr = "where to_number(a.tscc_data_seqno) in (select b.tscc_data_seqno from tsc_orgdata_log b "
                + "where b.file_name  = ?  and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";

        sqlCmd = "select conf_flag ";
        sqlCmd += "from ptr_billunit ";
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
            if(debug==1) 
            	showLogMessage("I", "", "888 Read=["+totalCnt+"]"+str600+","+str600.length());
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            initTscDccgAll();

            splitBuf1(str600);

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            tmpstr1 = hOrgdTsccDataSeqno;
            hDccgTsccDataSeqno = tmpstr1;

            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hDccgTscTxCode = comc.rtrim(dtl.tscTxCode);
            if ((!hDccgTscTxCode.equals("8207")) && (!hDccgTscTxCode.equals("6207"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hDccgTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscVdCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseDate));
            hDccgPurchaseDate = tmpstr1;
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseTime));
            hDccgPurchaseTime = tmpstr1;
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
            hDccgDestinationAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hDccgDestinationAmt;
            /*************************************************************************/
            hDccgTrafficCd   = comc.rtrim(dtl.trafficCd);
            hDccgTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
            hDccgAddrCd      = comc.rtrim(dtl.addrCd);
            hDccgAddrAbbr    = comc.rtrim(dtl.addrAbbr);
            hDccgOnlineMark  = comc.rtrim(dtl.onlineMark);
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 110);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            if(debug==1) 
            	showLogMessage("I", "", "  888 hsah=["+tmpstr2+"]"+comc.subMS950String(str600.getBytes("MS950"), 110, 110 + 16));
            if (comc.subMS950String(str600.getBytes("MS950"), 110, 110 + 16).equals(tmpstr2) == false) {
                hOrgdRptRespCode = "0205";
                showLogMessage("I", "", String.format("HASH values error [%s]", hOrgdRptRespCode));
             
            }
            if(debug==1) 
            	showLogMessage("I", "", " 888 tx_cd = ["+hDccgTscTxCode+"]" + hDccgOnlineMark);

            /*************************************************************************/
            hOrgdRptRespCode = "0000";
            /* 8207 6207:自動剔退後的再提示 */
            if (!hDccgTscTxCode.equals("6207")) {
            	
            	/*2023/06/26授權比對不要做
                if (hDccgOnlineMark.equals("1")) {
                    tempInt = 0;
                    sqlCmd = "select count(*) temp_int ";
                    sqlCmd += "from cca_auth_txlog a, tsc_vd_card b ";
                    sqlCmd += "where a.card_no = b.vd_card_no ";
                    sqlCmd += "and a.tx_date = ? ";
                    sqlCmd += "and b.tsc_card_no = ? ";
                    sqlCmd += "and a.nt_amt = ? ";
                    sqlCmd += "and a.trans_code = 'VA' ";
                    sqlCmd += "and a.auth_status_code = '00' ";
                    sqlCmd += "and decode(a.reversal_flag, '', 'N', a.reversal_flag) = 'N' ";
                    setString(1, hDccgPurchaseDate);
                    setString(2, hDccgTscCardNo);
                    setDouble(3, hDccgDestinationAmt);
                    int recordCnt = selectTable();

                    if (recordCnt > 0) {
                        tempInt = getValueInt("temp_int");
                    }
                    
                    //授權比對失敗時
                    if (tempInt == 0) {
                        hDctiChgbackReason = "1796";
                        insertTscDctiLog();
                        insertTscOrgdataLog();
                        continue;
                    }

                }
                */

                hTempDiffDays = comcr.calDays(hDccgPurchaseDate,hBusiBusinessDate);

                if(debug==1)
                	showLogMessage("I", "", "  888 date = ["+ hDccgPurchaseDate+"]" + hTempDiffDays);
                if (hTempDiffDays > 45) { /* RECS-s1031113-081 請款期限由20延長至45天 */
                    hDctiChgbackReason = "1795";
                    insertTscDctiLog();
                    insertTscOrgdataLog();
                    continue;
                }
                /*** 交易時間超過掛卡三小時 ***/
                if (selectTscOppostLog() > 0) {
                    hDctiChgbackReason = "1797";
                    insertTscDctiLog();
                    insertTscOrgdataLog();
                    continue;
                }
                /*** 交易時間超過餘轉 ***/
                if (selectTscDcbqLog() > 0) {
                    hDctiChgbackReason = "1798";
                    insertTscDctiLog();
                    insertTscOrgdataLog();
                    continue;
                }

                if (hOrgdRptRespCode.equals("0000")) {
                    if (hTardBlackltSDate.length() > 0) {
                        if ((hDccgPurchaseDate.compareTo(hTardBlackltSDate) >= 0) &&
                            (hTardBlackltEDate.length() == 0 || 
                             hDccgPurchaseDate.compareTo(hTardBlackltEDate) <= 0)) {
                            hDctiChgbackReason = "1791";
                            insertTscDctiLog();
                            insertTscOrgdataLog();
                            if((hDccgPurchaseDate.compareTo(hTardBlackltSDate) >= 0) &&
                               (hTardBlackltEDate.length() == 0 && hDccgOnlineMark.equals("0"))) {
                                processTscBkecExpt();
                            }
                            continue;
                        }
                    }
                }
            }
            if (hTardBlackltSDate.length() > 0 &&
            	(hDccgPurchaseDate.compareTo(hTardBlackltSDate) >= 0) &&
                (hTardBlackltEDate.length() == 0 && hDccgOnlineMark.equals("0"))) {
                processTscBkecExpt();
            }
            insertTscOrgdataLog();
            insertTscDccgAll();
            if (hTardAddvalueDate.length() == 0) {
                daoTable = "tsc_vd_card ";
                updateSQL = "addvalue_date = ? ";
                whereStr = "where tsc_card_no = ? ";
                setString(1, hTempSystemDate);
                setString(2, hDccgTscCardNo);
                updateTable();
                if (notFound.equals("Y")) {
                    showLogMessage("I","","update_tsc_vd_card not found! TscCardNo=[" + hDccgTscCardNo + "]");
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
    int selectTscVdCard() throws Exception {
        hTardAddvalueDate = "";
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hTardCurrentCode = "";
        hTardNewEndDate = "";
        hTardLockDate = "";
        hTardAutoloadFlag = "";
        hTardReturnDate = "";


        sqlCmd  = "select vd_card_no, ";
        sqlCmd += "addvalue_date, ";
        sqlCmd += "balance_date, ";
        sqlCmd += "blacklt_s_date, ";
        sqlCmd += "blacklt_e_date, ";
        sqlCmd += "current_code, ";
        sqlCmd += "new_end_date, ";
        sqlCmd += "lock_date, ";
        sqlCmd += "autoload_flag, ";
        sqlCmd += "return_date ";
        sqlCmd += "from tsc_vd_card ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hDccgTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDccgCardNo = getValue("vd_card_no");
            hTardAddvalueDate = getValue("addvalue_date");
            hTardBalanceDate = getValue("balance_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTardCurrentCode = getValue("current_code");
            hTardNewEndDate = getValue("new_end_date");
            hTardLockDate = getValue("lock_date");
            hTardAutoloadFlag = getValue("autoload_flag");
            hTardReturnDate = getValue("return_date");
     
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectTscOppostLog() throws Exception {
        int hCnt = 0;
        sqlCmd = "select count(*) h_cnt ";
        sqlCmd += "from tsc_oppost_log ";
        sqlCmd += "where tsc_card_no = ? ";
        /*** 2為撤掛 ***/
        sqlCmd += "and decode(proc_flag, '', '2', proc_flag) <> '2' ";
        sqlCmd += "and resp_code = '00'  ";
        sqlCmd += "and oppost_date != '' ";
        /*** 3小時為0.125天 ***/
        sqlCmd += "and to_date( ? || ? ,'yyyymmddhh24miss') >= (to_date(proc_date||proc_time,'yyyymmddhh24miss') + 3 hour ) ";
        setString(1, hDccgTscCardNo);
        setString(2, hDccgPurchaseDate);
        setString(3, hDccgPurchaseTime);
        int recordCnt = selectTable();

        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
        
        return (hCnt);
    }

    /***********************************************************************/
    int selectTscDcbqLog() throws Exception {
        int hCnt = 0;
        sqlCmd = "select count(*) h_cnt ";
        sqlCmd += "from tsc_dcbq_log ";
        sqlCmd += "where tsc_card_no = ?  ";
        sqlCmd += "and balance_date != '' ";
        sqlCmd += "and balance_date < ? ";
        setString(1, hDccgTscCardNo);
        setString(2, hDccgPurchaseDate);
        int recordCnt = selectTable();

        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
        }
        return (hCnt);
    }

    /***********************************************************************/
    void insertTscDctiLog() throws Exception {
    	if(debug==1)
    		showLogMessage("I", "", " 888 ERROR insert=["+ hDctiChgbackReason+"]");
    	
        daoTable = "tsc_dcti_log";
        setValue("crt_date", hTempSystemDate);
        setValue("crt_time", hTempNotifyTime);
        setValue("tran_code", "7227");
        setValue("tsc_card_no", hDccgTscCardNo);
        setValue("tran_date", hDccgPurchaseDate);
        setValue("tran_time", hDccgPurchaseTime);
        setValueDouble("tran_amt", hDccgDestinationAmt);
        setValue("traff_code", hDccgTrafficCd);
        setValue("place_code", hDccgAddrCd);
        setValue("traff_subname", hDccgTrafficAbbr);
        setValue("place_subname", hDccgAddrAbbr);
        setValue("chgback_reason", hDctiChgbackReason);
        setValue("file_name", hTnlgFileName);
        setValue("proc_flag", "N");
        setValue("mod_time" , sysDate + sysTime);
        setValue("mod_pgm"  , javaProgram);
        setValue("online_mark", hDccgOnlineMark);
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_dcti_log duplicate!", "", hCallBatchSeqno);
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
            sqlCmd += "from tsc_bkec_log  ";
            sqlCmd += "where tsc_card_no = ? ";
            setString(1, hDccgTscCardNo);
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
        setValue("tsc_card_no", hDccgTscCardNo);
        setValue("card_no", hDccgCardNo);
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
        sqlCmd = "select black_flag, ";
        sqlCmd += "send_date_s, ";
        sqlCmd += "send_date_e ";
        sqlCmd += "from tsc_bkec_expt ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hDccgTscCardNo);
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
        daoTable = "tsc_bkec_expt ";
        updateSQL = "black_date = ?, ";
        updateSQL += "black_user_id = 'batch', ";
        updateSQL += "black_remark = '離線加值交易', ";
        updateSQL += "crt_user = 'TscD031',";
        updateSQL += "crt_date = ?, ";
        updateSQL += "black_flag = '1', ";
        updateSQL += "send_date_s = ?, ";
        updateSQL += "send_date_e = ?, ";
        updateSQL += "from_type = '2', ";
        updateSQL += "mod_pgm = 'TscD031', ";
        updateSQL += "mod_time = sysdate, ";
        updateSQL += "apr_date ='', ";
        updateSQL += "apr_user ='' ";
        whereStr = "where tsc_card_no  = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hTempSystemDate);
        setString(4, hMaxDate);
        setString(5, hDccgTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_bkec_expt not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "DCCG");
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
        setValue("batch_unit"  , comc.getSubString(hDccgBillType, 0, 2));
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
    void insertTscDccgAll() throws Exception {
        tmpstr1 = String.format("悠遊卡儲值金%s", hDccgTrafficAbbr);
        hDccgMerchantChiName =  String.format("%-40.40s", tmpstr1);
        hDccgBillDesc = hDccgMerchantChiName;
        hDccgSeqNo    = totalCnt;

        setValue("batch_no"     , hTempBatchNo);
        setValueInt("seq_no"    , hDccgSeqNo);
        setValue("card_no"      , hDccgCardNo);
        setValue("tsc_card_no"  , hDccgTscCardNo);
        setValue("bill_type"    , hDccgBillType);
        setValue("txn_code"     , "05"); // transaction_code
        setValue("tsc_tx_code"  , hDccgTscTxCode);
        setValue("purchase_date", hDccgPurchaseDate);
        setValue("purchase_time", hDccgPurchaseTime);
        setValue("mcht_no", "EASY8003"); // merchant_no
        setValue("mcht_category", "4100"); // merchant_category
        setValue("mcht_chi_name", hDccgMerchantChiName); // merchant_chi_name
        setValueDouble("dest_amt", hDccgDestinationAmt); // destination_amt
        setValue("dest_curr", "901"); // destination_currency
        setValue("bill_desc", hDccgBillDesc);
        setValue("traffic_cd", hDccgTrafficCd);
        setValue("traffic_abbr", hDccgTrafficAbbr);
        setValue("addr_cd", hDccgAddrCd);
        setValue("addr_abbr", hDccgAddrAbbr);
        setValue("post_flag", "N");
        setValue("file_name", hTnlgFileName);
        setValue("tsc_error", hOrgdRptRespCode);
        setValue("crt_date", hTempSystemDate);
        setValue("tscc_data_seqno", hDccgTsccDataSeqno);
        setValue("online_mark", hDccgOnlineMark);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_dccg_all";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_dccg_all duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void initTscDccgAll() throws Exception {
        hDccgBatchNo = "";
        hDccgSeqNo = 0;
        hDccgCardNo = "";
        hDccgTscCardNo = "";
        hDccgBillType = fixBillType;
        hDccgTransactionCode = "05";
        hDccgTscTxCode = "";
        hDccgPurchaseDate = "";
        hDccgPurchaseTime = "";
        hDccgMerchantNo = "EASY8003";
        hDccgMerchantCategory = "4100";
        hDccgMerchantChiName = "";
        hDccgDestinationAmt = 0;
        hDccgDestinationCurrency = "901";
        hDccgBillDesc = "";
        hDccgTrafficCd = "";
        hDccgTrafficAbbr = "";
        hDccgAddrCd = "";
        hDccgAddrAbbr = "";
        hDccgPostFlag = "N";
        hDccgFileName = "";
        hDccgTscError = "";
        hDccgTscNotiDate = "";
        hDccgTscRespCode = "";
        hDccgReturnSource = "";
        hDccgServiceAmt = 0;

        hDctiChgbackReason = "";
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
        sqlCmd = "select nvl(substr(to_char(nvl(max(batch_seq),0) + 1,'0000'),2,4),'0000') h_batch_seq ";
        sqlCmd += "from bil_postcntl ";
        sqlCmd += "where batch_unit = substr(?,1,2) ";
        sqlCmd += "and batch_date = ?";
        setString(1, fixBillType);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBatchSeq  = getValue("h_batch_seq");
        }
        hTempBatchNo = hBusiBusinessDate + comc.getSubString(hDccgBillType,0,2) + hBatchSeq;

        showLogMessage("I", "", " 888 BATCH_NO = ["+ hTempBatchNo +"]");

    }

    /***************************************************************************/

    public static void main(String[] args) throws Exception {
        TscD031 proc = new TscD031();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
