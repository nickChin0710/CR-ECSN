/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/10  V1.00.01    SUP       error correction                          *
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

/*悠遊卡退卡資料檔(RTEC)媒體接收檢核程式*/
public class TscF022 extends AccessDAO {

    private final String progname = "悠遊卡退卡資料檔(RTEC)媒體接收檢核程式  109/11/17 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps comips = new CommIps();

    int debug = 0;
    String hCallBatchSeqno = "";
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
    String hCgecPurchaseDate = "";
    String hCgecCardNo = "";
    String hTardBalanceDate = "";
    String hTardBlackltSDate = "";
    String hTardBlackltEDate = "";
    int hTempDiffDays = 0;
    String hCgecTscCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    int hCgecSeqNo = 0;
    String hCgecTransactionCode = "";
    String hCgecTscTxCode = "";
    String hCgecPurchaseTime = "";
    String hCgecMerchantCategory = "";
    String hCgecMerchantChiName = "";
    double hCgecDestinationAmt = 0;
    String hCgecDestinationCurrency = "";
    String hCgecBillDesc = "";
    String hCgecTrafficCd = "";
    String hCgecTrafficAbbr = "";
    String hCgecAddrCd = "";
    String hCgecAddrAbbr = "";
    String hCgecPostFlag = "N";
    String hCgecFileName = "";
    String hCgecTscError = "";
    String hCgecTscNotiDate = "";
    String hCgecTscRespCode = "";
    String hCgecReturnSource = "";
    double hCgecServiceAmt = 0;
    String hCgecTrafficCdNew = "";
    String hCgecTrafficAbbrNew = "";
    String hCgecAddrCdNew = "";
    String hCgecAddrAbbrNew = "";
    String hCgecMerchantNo = "";
    String hCgecBatchNo = "";

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int nUserpid = 0;
    int nRetcode = 0;
    int succCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String hCgecBillType = "";
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
                comc.errExit("Usage : TscF022 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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
                    showLogMessage("I", "", String.format("參數(一) 不可兩碼 "));
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
                    showLogMessage("I", "", String.format("參數(二) 為[force_flag] or [seq(nn)]  "));
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
                    showLogMessage("I", "", String.format("file seq 必須兩碼 "));
                }
                String sgArgs2 = "";
                sgArgs2 = args[2];
                sgArgs2 = Normalizer.normalize(sgArgs2, java.text.Normalizer.Form.NFKD);
                fileSeq = sgArgs2;
            }
            selectPtrBusinday();

            tmpstr1 = String.format("RTEC.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s][%d] ", tmpstr1, forceFlag));

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
        sqlCmd += " decode( cast(? as varchar(8))  , ''"
                + ", to_char(decode( sign(substr(to_char(sysdate,'hh24miss'),1,2)-'13') , 1 , sysdate"
                + ", sysdate-1 days) ,'yyyymmdd') , ?) h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_system_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTempNotifyDate);
        setString(2, hTempNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempNotifyDate = getValue("h_temp_notify_date");
            hTempSystemDate = getValue("h_temp_system_date");
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
            comcr.errRtn(String.format("通知檔收檔發生問題,[%s]暫不可處理 ,請通知相關人員處理(error)", hTnlgFileName), "",
                    hCallBatchSeqno);
        }
        if (hTnlgProcFlag.equals("0")) {
            comcr.errRtn(String.format("通知檔收檔中[%s] , 請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (hTnlgProcFlag.toCharArray()[0] >= '2') {
            comcr.errRtn(String.format("[%s]退卡資料檔已處理過 , 請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "", String.format("[%s]退卡資料整檔處理失敗  , 錯誤代碼[%s] ", hTnlgFileName, hTnlgCheckCode));
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
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
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
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫 ", temstr1), "", hCallBatchSeqno);
        }

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if(debug ==1) showLogMessage("I", "", "888 str600 =[" + str600 + "]");
            
            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            initTscCgecAll();

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d] ", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            hOrgdOrgData = str600;
            
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1+2).equals("01"))) {
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
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmtSign));
            hSign = tmpstr1;
            if (hSign.equals("-")) {
                hCgecTransactionCode = "05";
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }
            hCgecDestinationAmt = comcr.str2double(tmpstr1);
            hCgecTrafficCd = comc.rtrim(dtl.trafficCd);
            hCgecTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
            hCgecAddrCd = comc.rtrim(dtl.addrCd);
            hCgecAddrAbbr = comc.rtrim(dtl.addrAbbr);
            hCgecReturnSource = comc.rtrim(dtl.returnSource);
            hSigns = String.format("%s", comc.rtrim(dtl.serviceAmtSign));
            tmpstr1 = String.format("%s", comc.rtrim(dtl.serviceAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }
            hCgecServiceAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            /* 20150303 新增 */
            hCgecTrafficCdNew = comc.rtrim(dtl.trafficCdNew);
            hCgecTrafficAbbrNew = comc.rtrim(dtl.trafficAbbrNew);
            hCgecAddrCdNew = comc.rtrim(dtl.addrCdNew);
            hCgecAddrAbbrNew = comc.rtrim(dtl.addrAbbrNew);
            /*************************************************************************/
            /* 原96改162 */
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 162);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));

            if(debug ==1) showLogMessage("I", "", "888 hash =[" + tmpstr2 + "]" + comc.subMS950String(str600.getBytes("MS950"), 162, 162 + 16));
            
            if (!comc.subMS950String(str600.getBytes("MS950"), 162, 162 + 16).equals(tmpstr2)) {
                hOrgdRptRespCode = "0205";
                showLogMessage("I", "", String.format("HASH values error [%s] ", hOrgdRptRespCode));
                /*
                 * insert_tsc_orgdata_log(); continue;
                 */
            }
            /**************************************************************************/
            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            hOrgdRptRespCode = "0000";

            insertTscOrgdataLog();
            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    int selectTscCard() throws Exception {
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hTempDiffDays = 0;

        sqlCmd = "select card_no,";
        sqlCmd += " balance_date,";
        sqlCmd += " blacklt_s_date,";
        sqlCmd += " blacklt_e_date,";
        sqlCmd += " days_between(to_date( ? ,'yyyymmdd') , to_date( ? ,'yyyymmdd')) h_temp_diff_days ";
        sqlCmd += " from tsc_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempSystemDate);
        setString(2, hCgecPurchaseDate.length() == 0 ? null : hCgecPurchaseDate);
        setString(3, hCgecTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCgecCardNo = getValue("card_no");
            hTardBalanceDate = getValue("balance_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTempDiffDays = getValueInt("h_temp_diff_days");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log";
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "RTEC");
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
    void initTscCgecAll() throws Exception {
        hCgecBatchNo = "";
        hCgecSeqNo = 0;
        hCgecCardNo = "";
        hCgecTscCardNo = "";
        hCgecTransactionCode = "06";
        hCgecTscTxCode = "";
        hCgecPurchaseDate = "";
        hCgecPurchaseTime = "";
        hCgecMerchantNo = "EASY8002";
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

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF022 proc = new TscF022();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String purchaseDate;
        String purchaseTime;
        String destinationAmtSign;
        String destinationAmt;
        String trafficCd;
        String addrCd;
        String trafficAbbr;
        String addrAbbr;
        String returnSource;
        String serviceAmtSign;
        String serviceAmt;
        String trafficCdNew; /* 交易業者代碼_new */
        String addrCdNew; /* 交易地點代碼_new */
        String trafficAbbrNew; /* 交易業者簡稱_new */
        String addrAbbrNew; /* 交易地點簡稱_new */
        String filler2;
        String hashValue;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(purchaseDate, 8);
            rtn += comc.fixLeft(purchaseTime, 6);
            rtn += comc.fixLeft(destinationAmtSign, 1);
            rtn += comc.fixLeft(destinationAmt, 12);
            rtn += comc.fixLeft(trafficCd, 3);
            rtn += comc.fixLeft(addrCd, 3);
            rtn += comc.fixLeft(trafficAbbr, 10);
            rtn += comc.fixLeft(addrAbbr, 10);
            rtn += comc.fixLeft(returnSource, 3);
            rtn += comc.fixLeft(serviceAmtSign, 1);
            rtn += comc.fixLeft(serviceAmt, 12);
            rtn += comc.fixLeft(trafficCdNew, 8);
            rtn += comc.fixLeft(addrCdNew, 10);
            rtn += comc.fixLeft(trafficAbbrNew, 20);
            rtn += comc.fixLeft(addrAbbrNew, 20);
            rtn += comc.fixLeft(filler2, 12);
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
        dtl.destinationAmtSign = comc.subMS950String(bytes, 37, 1);
        dtl.destinationAmt = comc.subMS950String(bytes, 38, 12);
        dtl.trafficCd = comc.subMS950String(bytes, 50, 3);
        dtl.addrCd = comc.subMS950String(bytes, 53, 3);
        dtl.trafficAbbr = comc.subMS950String(bytes, 56, 10);
        dtl.addrAbbr = comc.subMS950String(bytes, 66, 10);
        dtl.returnSource = comc.subMS950String(bytes, 76, 3);
        dtl.serviceAmtSign = comc.subMS950String(bytes, 79, 1);
        dtl.serviceAmt = comc.subMS950String(bytes, 80, 12);
        dtl.trafficCdNew = comc.subMS950String(bytes, 92, 8);
        dtl.addrCdNew = comc.subMS950String(bytes, 100, 10);
        dtl.trafficAbbrNew = comc.subMS950String(bytes, 110, 20);
        dtl.addrAbbrNew = comc.subMS950String(bytes, 130, 20);
        dtl.filler2 = comc.subMS950String(bytes, 150, 12);
        dtl.hashValue = comc.subMS950String(bytes, 162, 16);
        dtl.filler1 = comc.subMS950String(bytes, 178, 2);
    }

}
