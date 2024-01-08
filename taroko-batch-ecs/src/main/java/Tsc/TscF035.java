/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/22  V1.00.01    Brian     error correction                          *
*  106/08/16  V1.04.01    詹曜維    RECS-s1060713-052 正項交易入請款檔                                                *
*  107/11/13  V1.05.01    Atom      BECS-1071113-082 mark traff_code>255 ->0305*
*  108/01/18  V1.05.02    Brian     update to V1.05.01                        *
*  109-11-17  V1.05.03    tanwei    updated for project coding standard       *                                                                            *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊卡問題交易資料檔(PRTN)媒體接收處理程式*/
public class TscF035 extends AccessDAO {

    private final String progname = "悠遊卡問題交易資料檔(PRTN)媒體接收處理程式  109/11/17 V1.05.03";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommIps        comips   = new CommIps();

    int    debug                  = 0;
    String hCallBatchSeqno     = "";
    String hTempNotifyDate     = "";
    String hBusiBusinessDate   = "";
    String hTempNotifyTime     = "";
    String hTnlgPerformFlag    = "";
    String hTnlgNotifyDate     = "";
    String hTnlgCheckCode      = "";
    String hTnlgProcFlag       = "";
    String hTnlgRowid           = "";
    String hTnlgFileName       = "";
    String hPrtnTscCardNo     = "";
    int    hCnt                  = 0;
    int    hErrCnt              = 0;
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData        = "";
    String hOrgdRptRespCode   = "";
    String hPrtnRranDate       = "";
    String hPrtnTranTime       = "";
    double hPrtnTranAmt        = 0;
    String hPrtnTraffCode      = "";
    String hPrtnPlaceCode      = "";
    String hPrtnTraffSubname   = "";
    String hPrtnPlaceSubname   = "";
    String hPrtnOnlineMark     = "";

    int    hTnlgRecordCnt = 0;
    int    forceFlag        = 0;
    int    totalCnt         = 0;
    int    succCnt          = 0;
    int    rptCnt           = 0;
    String tmpstr            = "";
    String tmpstr1           = "";
    String tmpstr2           = "";
    String fileSeq          = "";
    String temstr1           = "";
    String hSign            = "";
    String hSigns          = "";

    String fixBillType               = "";
    String hCgecBillType            = "";
    long   hPostTotRecord           = 0;
    double hPostTotAmt              = 0;
    String hTempSystemDate          = "";
    String hCgecTsccDataSeqno      = "";
    String hCgecCardNo              = "";
    String hCgecTscCardNo          = "";
    String hCgecPurchaseDate        = "";
    String hCgecPurchaseTime        = "";
    double hCgecDestinationAmt      = 0;
    String hCgecTrafficCd           = "";
    String hCgecTrafficAbbr         = "";
    String hCgecAddrCd              = "";
    String hCgecAddrAbbr            = "";
    String hCgecOnlineMark          = "";
    long   hCgecSeqNo               = 0;
    String hCgecMerchantChiName    = "";
    String hCgecBillDesc            = "";
    String hCgecTransactionCode     = "";
    String hCgecTscTxCode          = "";
    String hCgecMerchantNo          = "";
    String hCgecMerchantCategory    = "";
    String hCgecDestinationCurrency = "";
    String hCgecPostFlag            = "";
    String hCgecFileName            = "";
    String hCgecTscError            = "";
    String hCgecTscNotiDate        = "";
    String hCgecTscRespCode        = "";
    String hCgecReturnSource        = "";
    double hCgecServiceAmt          = 0;
    String hCgecTrafficCdNew       = "";
    String hCgecTrafficAbbrNew     = "";
    String hCgecAddrCdNew          = "";
    String hCgecAddrAbbrNew        = "";
    String hTempBatchSeq            = "";
    String hBiunConfFlag            = "";

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
                comc.errExit("Usage : TscF035 [[notify_date][force_flag]] [force_flag][seq]", "");
            }

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
                {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hTempNotifyDate = sgArgs0;
                }
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

            tmpstr1 = String.format("PRTN.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            fixBillType = "TSCC";
            hCgecBillType = fixBillType;

            deleteBilPostcntl();
            deleteTscCgecAll();
            deleteTscPrtnLog();
            deleteTscOrgdataLog();

            selectPtrBillunit();

            hPostTotRecord = 0;
            hPostTotAmt = 0;

            fileOpen();
            updateTscNotifyLoga();

            backupRtn();

            showLogMessage("I", "", String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));

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
        sqlCmd += " decode( cast(? as varchar(8))" + ", ''"
                + ", to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13')" + ", 1" + ", sysdate"
                + ", sysdate - 1 days)" + ", 'yyyymmdd')" + ", ?) h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_system_date, ";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
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
        }

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
            comcr.errRtn("select_tsc_orgdata_log not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        }

        return (hErrCnt);
    }

    /***********************************************************************/
    void deleteTscPrtnLog() throws Exception {
        daoTable = "tsc_prtn_log a";
        whereStr = "where tscc_data_seqno = (select b.tscc_data_seqno   from tsc_orgdata_log b "
                + "where b.file_name = ?  and b.tscc_data_seqno = a.tscc_data_seqno) ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    int deleteTscOrgdataLog() throws Exception {

        daoTable = "tsc_orgdata_log";
        whereStr = "where file_name  = ? ";
        setString(1, hTnlgFileName);
        deleteTable();

        return (0);
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = " tsc_notify_log";
        updateSQL = " proc_flag  = '2',";
        updateSQL += " proc_date  = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " proc_time  = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm    = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = " where file_name  = ? ";
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

        selectBilPostcntl();

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            initTscCgecAll();

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            tmpstr1 = hOrgdTsccDataSeqno;
            hCgecTsccDataSeqno = tmpstr1;

            /*************************************************************************/
            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tscCardNo));
            if (comc.commTSCCCardNoCheck(tmpstr1) != 0) {
                showLogMessage("I", "", String.format("tsc_card_no[%s]", tmpstr1));
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hPrtnTscCardNo = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranDate));
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            hPrtnRranDate = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranTime));
            if (!comc.commTimeCheck(tmpstr1)) {
                hOrgdRptRespCode = "0204";
                insertTscOrgdataLog();
                continue;
            }
            hPrtnTranTime = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }
            hPrtnTranAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hPrtnTranAmt;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.traffCode));
            /*
             * if ((!comc.COMM_digit_check(tmpstr1)) || (comcr.str2int(tmpstr1) > 255)) {
             * h_orgd_rpt_resp_code = "0305"; insert_tsc_orgdata_log(); continue; }
             */
            hPrtnTraffCode = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.placeCode));
            /*
             * if ((COMM_digit_check(tmpstr1)!=0)||(atoi(tmpstr1)>255)) {
             * str2var(h_orgd_rpt_resp_code , "0305"); insert_tsc_orgdata_log(); continue; }
             */
            hPrtnPlaceCode = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.traffSubname));
            hPrtnTraffSubname = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.placeSubname));
            hPrtnPlaceSubname = tmpstr1;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.onlineMark));
            hPrtnOnlineMark = tmpstr1;
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 110);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            /*
             * if (strncmp(str600+110,tmpstr2,16)!=0) { str2var(h_orgd_rpt_resp_code ,
             * "0205"); insert_tsc_orgdata_log(); continue; }
             */
            /*************************************************************************/
            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hOrgdRptRespCode = "0000";
            insertTscOrgdataLog();
            insertTscCgecAll();
            insertTscPrtnLog();
            succCnt++;
            if (debug == 1)
                showLogMessage("I", "", String.format("   resp_c=[%s][%d]", hOrgdRptRespCode, succCnt));
        }

        if (totalCnt > 0)
            insertBilPostcntl();
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    int selectTscCard() throws Exception {
        sqlCmd = "select card_no ";
        sqlCmd += " from tsc_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hPrtnTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCgecCardNo = getValue("card_no");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {
        if (!hOrgdRptRespCode.equals("0000"))
            showLogMessage("I", "", String.format("第 %d 行有 [%s]錯誤", totalCnt, hOrgdRptRespCode));
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "PRTN");
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

    /*******************************************************************/
    void insertTscCgecAll() throws Exception {
        hCgecTscCardNo = hPrtnTscCardNo;
        hCgecPurchaseDate = hPrtnRranDate;
        hCgecPurchaseTime = hPrtnTranTime;
        hCgecDestinationAmt = hPrtnTranAmt;
        hCgecTrafficCd = hPrtnTraffCode;
        hCgecTrafficAbbr = hPrtnTraffSubname;
        hCgecAddrCd = hPrtnPlaceCode;
        hCgecAddrAbbr = hPrtnPlaceSubname;
        hCgecOnlineMark = hPrtnOnlineMark;

        tmpstr1 = String.format("%s%s", "悠遊卡請款", hCgecTrafficAbbr);
        hCgecSeqNo = totalCnt;

        hCgecMerchantChiName = String.format("%-40.40s", tmpstr1);
        hCgecBillDesc = hCgecMerchantChiName;

        daoTable = "tsc_cgec_all";
        extendField = "tsc_cgec_all.";
        setValue(extendField + "batch_no",
                hBusiBusinessDate + comc.getSubString(hCgecBillType, 0, 2) + hTempBatchSeq);
        setValueLong(extendField + "seq_no", hCgecSeqNo);
        setValue(extendField + "card_no", hCgecCardNo);
        setValue(extendField + "tsc_card_no", hCgecTscCardNo);
        setValue(extendField + "bill_type", hCgecBillType);
        setValue(extendField + "txn_code", hCgecTransactionCode);
        setValue(extendField + "tsc_tx_code", hCgecTscTxCode);
        setValue(extendField + "purchase_date", hCgecPurchaseDate);
        setValue(extendField + "purchase_time", hCgecPurchaseTime);
        setValue(extendField + "mcht_no", "EASY8003");
        setValue(extendField + "mcht_category", "4100");
        setValue(extendField + "mcht_chi_name", hCgecMerchantChiName);
        setValueDouble(extendField + "dest_amt", hCgecDestinationAmt);
        setValue(extendField + "dest_curr", "901");
        setValue(extendField + "bill_desc", hCgecBillDesc);
        setValue(extendField + "traffic_cd", hCgecTrafficCd);
        setValue(extendField + "traffic_abbr", hCgecTrafficAbbr);
        setValue(extendField + "addr_cd", hCgecAddrCd);
        setValue(extendField + "addr_abbr", hCgecAddrAbbr);
        setValue(extendField + "post_flag", "N");
        setValue(extendField + "file_name", hTnlgFileName);
        setValue(extendField + "tsc_error", hOrgdRptRespCode);
        setValue(extendField + "create_date", hTempSystemDate);
        setValue(extendField + "tscc_data_seqno", hCgecTsccDataSeqno);
        setValue(extendField + "online_mark", hCgecOnlineMark);
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_cgec_all duplicate!", "", hCallBatchSeqno);
        }

    }

    /*******************************************************************/
    void selectBilPostcntl() throws Exception {
        hTempBatchSeq = "";
        sqlCmd = " SELECT substr(to_char(nvl(max(batch_seq),0)+1,'0000'),2,4) h_temp_batch_seq ";
        sqlCmd += "   from bil_postcntl ";
        sqlCmd += "  where batch_unit = substr(?,1,2) ";
        sqlCmd += "    and batch_date = ? ";
        setString(1, fixBillType);
        setString(2, hBusiBusinessDate);
        if (selectTable() > 0)
            hTempBatchSeq = getValue("h_temp_batch_seq");
        else
            comcr.errRtn("select_bil_postcntl not found", String.format("bill_type[%s]", fixBillType),
                    hCallBatchSeqno);
    }

    /***************************************************************************/
    void insertBilPostcntl() throws Exception {
        daoTable = "bil_postcntl";
        extendField = "bil_postcntl.";
        setValue(extendField + "batch_date", hBusiBusinessDate);
        setValue(extendField + "batch_unit", comc.getSubString(hCgecBillType, 0, 2));
        setValueInt(extendField + "batch_seq", comc.str2int(hTempBatchSeq));
        setValue(extendField + "batch_no",
                hBusiBusinessDate + comc.getSubString(hCgecBillType, 0, 2) + hTempBatchSeq);
        setValueLong(extendField + "tot_record", hPostTotRecord);
        setValueDouble(extendField + "tot_amt", hPostTotAmt);
        setValue(extendField + "confirm_flag_p", hBiunConfFlag.equals("N") ? "Y" : "N");
        setValue(extendField + "confirm_flag", hBiunConfFlag);
        setValue(extendField + "confirm_user", "");
        setValue(extendField + "confirm_date", "");
        setValue(extendField + "this_close_date", hTempNotifyDate);
        setValue(extendField + "mod_pgm", javaProgram);
        setValue(extendField + "mod_time", sysDate + sysTime);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_bil_postcntl duplicate!", "", hCallBatchSeqno);
        }
    }

    /*************************************************************************/
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";
        sqlCmd = " SELECT conf_flag FROM ptr_billunit WHERE bill_unit = substr(?,1,2) ";
        setString(1, fixBillType);
        if (selectTable() > 0)
            hBiunConfFlag = getValue("conf_flag");
        else
            comcr.errRtn("ptr_billunit not found", String.format("bill_type[%s]", hCgecBillType), hCallBatchSeqno);

    }

    /***********************************************************************/
    void deleteTscCgecAll() throws Exception {
        daoTable = "tsc_cgec_all a";
        whereStr = " WHERE to_number(a.tscc_data_seqno) ";
        whereStr += "        in (select b.tscc_data_seqno ";
        whereStr += "              from tsc_orgdata_log b ";
        whereStr += "             where b.file_name       = ? ";
        whereStr += "               and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
        setString(1, hTnlgFileName);

        deleteTable();
    }

    /*******************************************************************/
    void deleteBilPostcntl() throws Exception {

        daoTable = "bil_postcntl";
        whereStr = " WHERE this_close_date = ?    ";
        whereStr += "  AND batch_unit = substr(?,1,2)  ";
        whereStr += "  AND mod_pgm    = ?                ";
        setString(1, hTempNotifyDate);
        setString(2, fixBillType);
        setString(3, javaProgram);

        deleteTable();
    }

    /*************************************************************************/
    void initTscCgecAll() {
        // h_cgec_batch_no = "";
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
        hCgecTrafficCdNew = "";
        hCgecTrafficAbbrNew = "";
        hCgecAddrCdNew = "";
        hCgecAddrAbbrNew = "";
    }

    /*******************************************************************/
    void insertTscPrtnLog() throws Exception { /* put RPT 2 columns, What to do ? */
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("tsc_card_no", hPrtnTscCardNo);
        setValue("tran_date", hPrtnRranDate);
        setValue("tran_time", hPrtnTranTime);
        setValueDouble("tran_amt", hPrtnTranAmt);
        setValue("traff_code", hPrtnTraffCode);
        setValue("place_code", hPrtnPlaceCode);
        setValue("traff_subname", hPrtnTraffSubname);
        setValue("place_subname", hPrtnPlaceSubname);
        setValue("online_mark", hPrtnOnlineMark);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("notify_date", hTempNotifyDate);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_prtn_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_prtn_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String tranDate;
        String tranTime;
        String tranAmt;
        String traffCode;
        String placeCode;
        String traffSubname;
        String placeSubname;
        String onlineMark;
        String filler1;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(tranDate, 8);
            rtn += comc.fixLeft(tranTime, 6);
            rtn += comc.fixLeft(tranAmt, 13);
            rtn += comc.fixLeft(traffCode, 8);
            rtn += comc.fixLeft(placeCode, 6);
            rtn += comc.fixLeft(traffSubname, 20);
            rtn += comc.fixLeft(placeSubname, 20);
            rtn += comc.fixLeft(onlineMark, 1);
            rtn += comc.fixLeft(filler1, 5);
            rtn += comc.fixLeft(filler, 200);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.attri = comc.subMS950String(bytes, 1, 2);
        dtl.tscCardNo = comc.subMS950String(bytes, 3, 20);
        dtl.tranDate = comc.subMS950String(bytes, 23, 8);
        dtl.tranTime = comc.subMS950String(bytes, 31, 6);
        dtl.tranAmt = comc.subMS950String(bytes, 37, 13);
        dtl.traffCode = comc.subMS950String(bytes, 50, 8);
        dtl.placeCode = comc.subMS950String(bytes, 58, 6);
        dtl.traffSubname = comc.subMS950String(bytes, 64, 20);
        dtl.placeSubname = comc.subMS950String(bytes, 84, 20);
        dtl.onlineMark = comc.subMS950String(bytes, 104, 1);
        dtl.filler1 = comc.subMS950String(bytes, 105, 5);
        dtl.filler = comc.subMS950String(bytes, 110, 200);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF035 proc = new TscF035();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
