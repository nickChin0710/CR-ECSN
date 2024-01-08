/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-17  V1.00.01    tanwei    updated for project coding standard       *
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

/*掛失贖回餘額交易資料檔(BDEC)接收處理*/
public class TscF03b extends AccessDAO {
    private final String progname = "掛失贖回餘額交易資料檔(BDEC)接收處理  109/11/17 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommIps comips = new CommIps();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

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
    String hCgecPurchaseDate = "";
    String hCgecCardNo = "";
    String hTardAddvalueDate = "";
    String hTardBalanceDate = "";
    String hTardBlackltSDate = "";
    String hTardBlackltEDate = "";
    int hTempDiffDays = 0;
    String hTardCurrentCode = "";
    String hTardNewEndDate = "";
    String hTardLockDate = "";
    String hCgecTransactionCode = "";
    String hTardAutoloadFlag = "";
    String hTardReturnDate = "";
    String hCardIdPSeqno = "";
    String hCgecTscCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hCgecPurchaseTime = "";
    double hCgecDestinationAmt = 0;
    String hCgecTrafficAbbr = "";
    String hCgecAddrAbbr = "";
    String hCgecTsccDataSeqno = "";
    String hCgecFileName = "";
    String hCgecTscError = "";
    String hCgecTscNotiDate = "";
    String hCgecTscRespCode = "";
    String hCgecReturnSource = "";
    int hCnt = 0;
    int hErrCnt = 0;

    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    int hCgecSeqNo = 0;
    String hCgecMerchantChiName = "";
    String hCgecBillDesc = "";
    String tmpstr = "";
    String tmpstr1 = "";
    String fileSeq = "";
    String temstr1 = "";
    String tmpstr2 = "";
    String hCgecBillType = "";
    String fixBillType = "";
    String tSign = "";

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
                comc.errExit("Usage : TscF03b [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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
            selectPtBusinday();

            tmpstr1 = String.format("BDEC.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            fixBillType = "TSCC";
            hCgecBillType = fixBillType;

            deleteTscMinusLog();
            deleteTscOrgdataLog();
            
            fileOpen();
            updateTscNotifyLoga();

            backupRtn();

            showLogMessage("I", "",
                    String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));

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
    void selectPtBusinday() throws Exception {
        hBusiBusinessDate = "";
        hAdd2SystemDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char( decode(sign(substr(to_char(sysdate,'hh24miss'),1,4)-'1530'),1 ,sysdate,sysdate-1 days),'yyyymmdd') h_temp_notify_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time,";
        sqlCmd += "to_char(add_months(sysdate,2),'yyyymmdd') h_add2_system_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hTempNotifyDate.length() == 0 ? getValue("business_date") : hTempNotifyDate;
            hTempNotifyDate = hTempNotifyDate.length() == 0 ? getValue("business_date") : hTempNotifyDate;
            hTempSystemDate = getValue("h_temp_system_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
            hAdd2SystemDate = getValue("h_add2_system_date");
        }

    }

    /***********************************************************************/
    int selectTscOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt,";
        sqlCmd += "sum(decode(rpt_resp_code,'0000',0,1)) h_err_cnt ";
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
    void deleteTscMinusLog() throws Exception {
        daoTable = "tsc_minus_log a";
        whereStr = "where to_number(a.tscc_data_seqno) in (select b.tscc_data_seqno from tsc_orgdata_log b where b.file_name  = ?  ";
        whereStr += "and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void updateTscNotifyLoga() throws Exception {
        daoTable = "tsc_notify_log";
        updateSQL = "proc_flag  = '2',";
        updateSQL += " proc_date  = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " proc_time  = to_char(sysdate, 'hh24miss'),";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where file_name  = ? ";
        setString(1, javaProgram);
        setString(2, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";

        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", String.format("Open File=[%s]", temstr1));
        int f = openInputText(temstr1);
        if(f == -1) {
            String stderr = String.format("[%s]檔案不存在", temstr1);
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            initTscMinusLog();

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            tmpstr1 = hOrgdTsccDataSeqno;
            hCgecTsccDataSeqno = tmpstr1;

            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            hCgecTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.notifyDate));
            hCgecTscNotiDate = tmpstr1;
            if (comc.commDateCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseDate));
            hCgecPurchaseDate = tmpstr1;
            if (comc.commDateCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseTime));
            hCgecPurchaseTime = tmpstr1;
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0204";
                insertTscOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmtSign));
            tSign = tmpstr1;
            tmpstr1 = String.format("%s", comc.rtrim(dtl.destinationAmt));
            if (comc.commDigitCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hCgecDestinationAmt = comcr.str2double(tmpstr1);

            hCgecTransactionCode = "06";
            if (tSign.equals("-")) {
                hCgecDestinationAmt = comcr.str2double(tmpstr1) * (-1);
            }

            hCgecTrafficAbbr = comc.rtrim(dtl.trafficAbbr);

            hCgecAddrAbbr = comc.rtrim(dtl.addrAbbr);

            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 110);
            byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("MS950"));
            tmpstr2 = new String(tmp);
            if (!comc.subMS950String(str600.getBytes("MS950"), 110, 110 + 16).equals(tmpstr2)) {
                hOrgdRptRespCode = "0205";
                showLogMessage("I", "", String.format("HASH values error [%s]", hOrgdRptRespCode));
            }
            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            hOrgdRptRespCode = "0000";

            insertTscOrgdataLog();
            insertTscMinusLog();
            updateTscCard();

            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }

        closeInputText(br);
    }

    /***********************************************************************/
    void insertTscMinusLog() throws Exception {
        tmpstr1 = String.format("掛失贖回餘額交易%s%s", hCgecTrafficAbbr, hCgecAddrAbbr);
        hCgecMerchantChiName = tmpstr1;
        hCgecBillDesc = tmpstr1;
        hCgecSeqNo = totalCnt;

        setValue("card_no", hCgecCardNo);
        setValue("id_p_seqno", hCardIdPSeqno);
        setValue("tsc_card_no", hCgecTscCardNo);
        setValue("purchase_date", hCgecPurchaseDate);
        setValue("purchase_time", hCgecPurchaseTime);
        setValueDouble("dest_amt", hCgecDestinationAmt);
        setValue("traffic_abbr", hCgecTrafficAbbr);
        setValue("addr_abbr", hCgecAddrAbbr);
        setValue("file_name", hTnlgFileName);
        setValue("tsc_error", hOrgdRptRespCode);
        setValue("crt_date", hTempSystemDate);
        setValue("tscc_data_seqno", hCgecTsccDataSeqno);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_minus_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_minus_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateTscCard() throws Exception {
        daoTable = "tsc_card";
        updateSQL = "purchase_date = ? ,";
        updateSQL += " purchase_time = ? ,";
        updateSQL += " dest_amt = ? ,";
        updateSQL += " traffic_abbr_neg = ? ,";
        updateSQL += " addr_abbr_neg = ? ,";
        updateSQL += " mod_pgm   = ? ,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where tsc_card_no  = ? ";
        setString(1, hCgecPurchaseDate);
        setString(2, hCgecPurchaseTime);
        setDouble(3, hCgecDestinationAmt);
        setString(4, hCgecTrafficAbbr);
        setString(5, hCgecAddrAbbr);
        setString(6, javaProgram);
        setString(7, hCgecTscCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "BDEC");
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
    void initTscMinusLog() throws Exception {
        hCgecCardNo = "";
        hCardIdPSeqno = "";
        hCgecTscCardNo = "";
        hCgecPurchaseDate = "";
        hCgecPurchaseTime = "";
        hCgecDestinationAmt = 0;
        hCgecTrafficAbbr = "";
        hCgecAddrAbbr = "";
        hCgecFileName = "";
        hCgecTscError = "";
        hCgecTscNotiDate = "";
        hCgecTscRespCode = "";
        hCgecReturnSource = "03b";

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
        hCardIdPSeqno = "";
        hTempDiffDays = 0;

        sqlCmd = "select a.card_no,";
        sqlCmd += "a.addvalue_date,";
        sqlCmd += "a.balance_date,";
        sqlCmd += "a.blacklt_s_date,";
        sqlCmd += "a.blacklt_e_date,";
        sqlCmd += "days_between(to_date(?,'yyyymmdd') , to_date(?,'yyyymmdd')) h_temp_diff_days,";
        sqlCmd += "a.current_code,";
        sqlCmd += "a.new_end_date,";
        sqlCmd += "a.lock_date,";
        sqlCmd += "a.autoload_flag,";
        sqlCmd += "a.return_date,";
        sqlCmd += "a.balance_date,";
        sqlCmd += "b.id_p_seqno ";
        sqlCmd += " from crd_card b ,tsc_card a  ";
        sqlCmd += "where tsc_card_no = ?  ";
        sqlCmd += "and b.card_no = a.card_no ";
        setString(1, hTempSystemDate);
        setString(2, hCgecPurchaseDate.length()==0?null:hCgecPurchaseDate);
        setString(3, hCgecTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCgecCardNo = getValue("card_no");
            hTardAddvalueDate = getValue("addvalue_date");
            hTardBalanceDate = getValue("balance_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTempDiffDays = getValueInt("h_temp_diff_days");
            hTardCurrentCode = getValue("current_code");
            hTardNewEndDate = getValue("new_end_date");
            hTardLockDate = getValue("lock_date");
            hTardAutoloadFlag = getValue("autoload_flag");
            hTardReturnDate = getValue("return_date");
            hTardBalanceDate = getValue("balance_date");
            hCardIdPSeqno = getValue("id_p_seqno");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF03b proc = new TscF03b();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String notifyDate;
        String purchaseDate;
        String purchaseTime;
        String destinationAmtSign;
        String destinationAmt;
        String trafficAbbr;
        String addrAbbr;
        String filler0;
        String hashValue;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(notifyDate, 8);
            rtn += comc.fixLeft(purchaseDate, 8);
            rtn += comc.fixLeft(purchaseTime, 6);
            rtn += comc.fixLeft(destinationAmtSign, 1);
            rtn += comc.fixLeft(destinationAmt, 12);
            rtn += comc.fixLeft(trafficAbbr, 20);
            rtn += comc.fixLeft(addrAbbr, 20);
            rtn += comc.fixLeft(filler0, 30);
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
        dtl.notifyDate = comc.subMS950String(bytes, 23, 8);
        dtl.purchaseDate = comc.subMS950String(bytes, 31, 8);
        dtl.purchaseTime = comc.subMS950String(bytes, 39, 6);
        dtl.destinationAmtSign = comc.subMS950String(bytes, 45, 1);
        dtl.destinationAmt = comc.subMS950String(bytes, 46, 12);
        dtl.trafficAbbr = comc.subMS950String(bytes, 58, 20);
        dtl.addrAbbr = comc.subMS950String(bytes, 78, 20);
        dtl.filler0 = comc.subMS950String(bytes, 98, 30);
        dtl.hashValue = comc.subMS950String(bytes, 128, 16);
        dtl.filler1 = comc.subMS950String(bytes, 144, 2);
    }

}
