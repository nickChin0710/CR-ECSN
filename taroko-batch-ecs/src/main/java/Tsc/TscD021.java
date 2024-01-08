/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/15  V1.00.00    Wendy Lu                     program initial        *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;


public class TscD021 extends AccessDAO {

    private final String progname = "悠遊VD卡自動儲值請款檔(DCCG)媒體接收檢核程式  110/01/15 V1.00.00";
    CommFunction    comm = new CommFunction();
    CommCrd         comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps       comips = new CommIps();

    int    debug = 1;

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
    String hDccgPurchaseDate = "";
    String hDccgCardNo = "";
    String hTardAddvalueDate = "";
    String hTardBalanceDate = "";
    String hTardBlackltSDate = "";
    String hTardBlackltEDate = "";
    int hTempDiffDays = 0;
    String hDccgTscCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hDccgBatchNo = "";
    int hDccgSeqNo = 0;
    String hDccgTransactionCode = "";
    String hDccgTscTxCode = "";
    String hDccgPurchaseTime = "";
    String hDccgMerchantCategory = "";
    String hDccgMerchantChiName = "";
    double hDccgDestinationAmt = 0;
    String hDccgDestinationCurrency = "";
    String hDccgBillDesc = "";
    String hDccgTrafficCd = "";
    String hDccgTrafficAbbr = "";
    String hDccgAddrCd = "";
    String hDccgAddrAbbr = "";
    String hDccgPostFlag = "N";
    String hDccgFileName = "";
    String hDccgTscError = "";
    String hDccgTscNotiDate = "";
    String hDccgTscRespCode = "";
    String hDccgReturnSource = "";
    double hDccgServiceAmt = 0;

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
    String hDccgBillType = "";

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
                comc.errExit("Usage : TscD021 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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

            tmpstr1 = String.format("DCCG.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s] ", tmpstr1));

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
        sqlCmd += "decode( cast(? as varchar(8)) , '' "
                + ",to_char(decode( sign(substr(to_char(sysdate,'hh24miss'),1,4)-'1530'), 1 , sysdate"
                + ",sysdate-1 days) ,'yyyymmdd'), ?) h_temp_notify_date, ";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date, ";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += "from ptr_businday ";
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
if(debug==1) showLogMessage("I", "", "Read=[" + hTnlgFileName+"]"+hTnlgPerformFlag);

        if (!hTnlgPerformFlag.equals("Y")) {
            comcr.errRtn(String.format("通知檔收檔發生問題,[%s]暫不可處理 ,請通知相關人員處理(error)", hTnlgFileName), hTnlgPerformFlag, hCallBatchSeqno);
        }
        if (hTnlgProcFlag.equals("0")) {
            comcr.errRtn(String.format("通知檔收檔中[%s] , 請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (comc.str2int(hTnlgProcFlag) >= 2) {
            comcr.errRtn(String.format("[%s]自動儲值請款檔已處理過,請通知相關人員處理(error) ", hTnlgFileName), "", hCallBatchSeqno);
        }
        if (!hTnlgCheckCode.equals("0000")) {
            showLogMessage("I", "",
                    String.format("[%s]自動儲值請款檔整檔處理失敗  , 錯誤代碼[%s] ", hTnlgFileName, hTnlgCheckCode));
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable   = "tsc_notify_log ";
        updateSQL  = "proc_flag = '2', ";
        updateSQL += "proc_date = to_char(sysdate, 'yyyymmdd'), ";
        updateSQL += "proc_time = to_char(sysdate, 'hh24miss'), ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr   = "where file_name = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found=", hTnlgFileName, hCallBatchSeqno);
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

if(debug==1) showLogMessage("I", "", "OPEN=["+temstr1+"]");

        int f = openInputText(temstr1);
        if(f == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫 ", temstr1), "", hCallBatchSeqno);
        }

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            initTscDccgAll();

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
            hDccgTscTxCode = comc.rtrim(dtl.tscTxCode);
            if ((!hDccgTscTxCode.equals("8209")) && (!hDccgTscTxCode.equals("6209"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hDccgTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscCard() != 0) {
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
            /*************************************************************************/
            hDccgDestinationAmt = comcr.str2double(tmpstr1);
            hDccgTrafficCd = comc.rtrim(dtl.trafficCd);
            hDccgTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
            hDccgAddrCd = comc.rtrim(dtl.addrCd);
            hDccgAddrAbbr = comc.rtrim(dtl.addrAbbr);
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 110);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            if (!comc.subMS950String(str600.getBytes("MS950"), 110, 126).equals(tmpstr2)) {
                hOrgdRptRespCode = "0205";
                showLogMessage("I", "", String.format("HASH values error!!!"));
                showLogMessage("I", "", "  Hash=["+tmpstr2+"]"+comc.subBIG5String(str600.getBytes("MS950"), 110, 126));
            }
            /*
             * insert_tsc_orgdata_log(); continue;
             */
            /*************************************************************************/
            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hOrgdRptRespCode = "0000";
            /* 8209 6209:自動剔退後的再提示 */
            if (!hDccgTscTxCode.equals("6209")) {
                if (hTempDiffDays > 20) {
                    insertTscOrgdataLog();
                    continue;
                }

                if (hOrgdRptRespCode.equals("0000")) {
                    if (hTardBlackltSDate.length() > 0) {
                        if ((hDccgPurchaseDate.compareTo(hTardBlackltSDate) >= 0)
                                && (hTardBlackltEDate.length() == 0
                                        || hDccgPurchaseDate.compareTo(hTardBlackltEDate) <= 0)) {
                            insertTscOrgdataLog();
                            continue;
                        }
                    }
                }
            }
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

        sqlCmd = "select vd_card_no, ";
        sqlCmd += "addvalue_date, ";
        sqlCmd += "balance_date, ";
        sqlCmd += "blacklt_s_date, ";
        sqlCmd += "blacklt_e_date, ";
        sqlCmd += "days_between(to_date( ? ,'yyyymmdd') , to_date( ? ,'yyyymmdd')) h_temp_diff_days ";
        sqlCmd += "from tsc_vd_card ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempSystemDate);
        setString(2, hDccgPurchaseDate.length() == 0 ? null : hDccgPurchaseDate);
        setString(3, hDccgTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hDccgCardNo = getValue("card_no");
            hTardAddvalueDate = getValue("addvalue_date");
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
        setValue("file_iden", "DCCG");
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
    void initTscDccgAll() throws Exception {
        hDccgBatchNo = "";
        hDccgSeqNo = 0;
        hDccgCardNo = "";
        hDccgTscCardNo = "";
        hDccgTransactionCode = "05";
        hDccgTscTxCode = "";
        hDccgPurchaseDate = "";
        hDccgPurchaseTime = "";
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

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscD021 proc = new TscD021();
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
        dtl.type            = comc.subMS950String(bytes, 0, 1);
        dtl.attri           = comc.subMS950String(bytes, 1, 2);
        dtl.tscCardNo     = comc.subMS950String(bytes, 3, 20);
        dtl.purchaseDate   = comc.subMS950String(bytes, 23, 8);
        dtl.purchaseTime   = comc.subMS950String(bytes, 31, 6);
        dtl.destinationAmt = comc.subMS950String(bytes, 37, 13);
        dtl.trafficCd      = comc.subMS950String(bytes, 50, 8);
        dtl.addrCd         = comc.subMS950String(bytes, 58, 6);
        dtl.trafficAbbr    = comc.subMS950String(bytes, 64, 20);
        dtl.addrAbbr       = comc.subMS950String(bytes, 84, 20);
        dtl.tscTxCode     = comc.subMS950String(bytes, 104, 4);
        dtl.onlineMark     = comc.subMS950String(bytes, 108, 1);
        dtl.filler0        = comc.subMS950String(bytes, 109, 1);
        dtl.hashValue      = comc.subMS950String(bytes, 110, 16);
        dtl.filler1        = comc.subMS950String(bytes, 126, 2);
    }

}
