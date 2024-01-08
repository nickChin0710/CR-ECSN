/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/23  V1.00.01    Brian     error correction                          *
*  109-11-17  V1.00.02    tanwei    updated for project coding standard       *
*  110/01/14  V1.00.03    Wilson    新增select tsc_vd_card                     *
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

/*SVCS代行授權交易至發卡機構(STAD)入檔程式*/
public class TscF039 extends AccessDAO {

    private final String progname = "SVCS代行授權交易至發卡機構(STAD)入檔程式  110/01/14 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps comips = new CommIps();

    String prgmId = "TscF039";
    String rptName1 = "";
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
    String hStadPurchaseDate = "";
    String hTardCardNo = "";
    String hTardAddvalueDate = "";
    String hTardBalanceDate = "";
    String hTardBlackltSDate = "";
    String hTardBlackltEDate = "";
    int hTempDiffDays = 0;
    String hRardCurrentCode = "";
    String hTardNewEndDate = "";
    String hTardLockDate = "";
    String hTardAutoloadFlag = "";
    String hTardReturnDate = "";
    String hStadTscCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hStadCrtDate = "";
    String hStadCrtTime = "";
    String hStadCardNo = "";
    String hStadProcessCode = "";
    String hStadPurchaseTime = "";
    double hStadTranAmt = 0;
    String hStadTranStan = "";
    String hStadTranRrn = "";
    String hStadAcqId = "";
    String hStadTermId = "";
    String hStadMchtId = "";
    String hStadRespCode = "";
    String hStadTsccDataSeqno = "";
    String hStadPostFlag = "";
    int hCnt = 0;
    int hErrCnt = 0;
    String hStadRptFlag = "";
    String hStadRptRespCode = "";
    String hStadModTime = "";
    String hStadModPgm = "";

    int hTnlgRecordCnt = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int nUserpid = 0;
    int nRetcode = 0;
    int succCnt = 0;
    int rptCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    String temstr1 = "";
    String tSign0 = "";
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
                comc.errExit("Usage : TscF039 [[notify_date][force_flag]] [force_flag][seq(nn)]", "");
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

            tmpstr1 = String.format("STAD.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            deleteTscStadLog();
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
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8))" + ", ''"
                + ", to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,4)-'1530')" + ", 1" + ", sysdate"
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
        sqlCmd = " select count(*) h_cnt,";
        sqlCmd += " sum(decode(rpt_resp_code, '0000', 0, 1)) h_err_cnt ";
        sqlCmd += " from tsc_orgdata_log  ";
        sqlCmd += " where file_name  = ? ";
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
    void deleteTscOrgdataLog() throws Exception {
        daoTable = "tsc_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();
    }
    
    /***********************************************************************/
    void deleteTscStadLog() throws Exception {
        daoTable = "tsc_stad_log a";
        whereStr = "where to_number(a.tscc_data_seqno) in (select b.tscc_data_seqno " + " from tsc_orgdata_log b "
                + "where b.file_name  = ?  ";
        whereStr += " and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
        setString(1, hTnlgFileName);
        deleteTable();

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
        showLogMessage("I", "", String.format("Open File=[%s]", temstr1));
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

            initTscStadLog();

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            hStadTsccDataSeqno = hOrgdTsccDataSeqno;

            hOrgdOrgData = str600;
            if ((!comc.getSubString(str600, 0, 1).equals("D")) || (!comc.getSubString(str600, 1, 1 + 2).equals("01"))) {
                hOrgdRptRespCode = "0205";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            hStadTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscCard() != 0) {
            	if (selectTscVdCard() != 0) {
            		hOrgdRptRespCode = "0301";
                    insertTscOrgdataLog();
                    continue;
            	}                
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseDate));
            hStadPurchaseDate = tmpstr1;
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.purchaseTime));
            hStadPurchaseTime = tmpstr1;
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
            hStadTranAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            hStadTranStan = comc.rtrim(dtl.tranStan);
            hStadTranRrn = comc.rtrim(dtl.tranRrn);
            hStadAcqId = comc.rtrim(dtl.acqId);
            hStadTermId = comc.rtrim(dtl.termId);
            hStadMchtId = comc.rtrim(dtl.mchtId);
            hStadRespCode = comc.rtrim(dtl.respCode);
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 134);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            /*
             * test if(strncmp(str600+134,tmpstr2,16)!=0) {
             * str2var(h_orgd_rpt_resp_code , "0205");
             * ECSprintf(stderr,"HASH values error [%s]\n",h_orgd_rpt_resp_code.
             * arr); }
             */
            /*************************************************************************/
            hOrgdRptRespCode = "0000";
            insertTscOrgdataLog();

            insertTscStadLog();
            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    int selectTscCard() throws Exception {
        hTardCardNo = "";
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hRardCurrentCode = "";
        hTardNewEndDate = "";
        hTardLockDate = "";
        hTardAutoloadFlag = "";
        hTardReturnDate = "";
        hTardBalanceDate = "";
        hTempDiffDays = 0;

        sqlCmd = "select card_no,";
        sqlCmd += " addvalue_date,";
        sqlCmd += " balance_date,";
        sqlCmd += " blacklt_s_date,";
        sqlCmd += " blacklt_e_date,";
        sqlCmd += " days_between(to_date( ?, 'yyyymmdd') , to_date( ?, 'yyyymmdd')) h_temp_diff_days,";
        sqlCmd += " current_code,";
        sqlCmd += " new_end_date,";
        sqlCmd += " lock_date,";
        sqlCmd += " autoload_flag,";
        sqlCmd += " return_date ";
        sqlCmd += " balance_date ";
        sqlCmd += " from tsc_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempSystemDate);
        setString(2, hStadPurchaseDate.length() == 0 ? null : hStadPurchaseDate);
        setString(3, hStadTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTardCardNo = getValue("card_no");
            hTardAddvalueDate = getValue("addvalue_date");
            hTardBalanceDate = getValue("balance_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTempDiffDays = getValueInt("h_temp_diff_days");
            hRardCurrentCode = getValue("current_code");
            hTardNewEndDate = getValue("new_end_date");
            hTardLockDate = getValue("lock_date");
            hTardAutoloadFlag = getValue("autoload_flag");
            hTardReturnDate = getValue("return_date");
            hTardBalanceDate = getValue("balance_date");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    int selectTscVdCard() throws Exception {
        hTardCardNo = "";
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hRardCurrentCode = "";
        hTardNewEndDate = "";
        hTardLockDate = "";
        hTardAutoloadFlag = "";
        hTardReturnDate = "";
        hTardBalanceDate = "";
        hTempDiffDays = 0;

        sqlCmd = "select vd_card_no,";
        sqlCmd += " addvalue_date,";
        sqlCmd += " balance_date,";
        sqlCmd += " blacklt_s_date,";
        sqlCmd += " blacklt_e_date,";
        sqlCmd += " days_between(to_date( ?, 'yyyymmdd') , to_date( ?, 'yyyymmdd')) h_temp_diff_days,";
        sqlCmd += " current_code,";
        sqlCmd += " new_end_date,";
        sqlCmd += " lock_date,";
        sqlCmd += " autoload_flag,";
        sqlCmd += " return_date ";
        sqlCmd += " balance_date ";
        sqlCmd += " from tsc_vd_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempSystemDate);
        setString(2, hStadPurchaseDate.length() == 0 ? null : hStadPurchaseDate);
        setString(3, hStadTscCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTardCardNo = getValue("vd_card_no");
            hTardAddvalueDate = getValue("addvalue_date");
            hTardBalanceDate = getValue("balance_date");
            hTardBlackltSDate = getValue("blacklt_s_date");
            hTardBlackltEDate = getValue("blacklt_e_date");
            hTempDiffDays = getValueInt("h_temp_diff_days");
            hRardCurrentCode = getValue("current_code");
            hTardNewEndDate = getValue("new_end_date");
            hTardLockDate = getValue("lock_date");
            hTardAutoloadFlag = getValue("autoload_flag");
            hTardReturnDate = getValue("return_date");
            hTardBalanceDate = getValue("balance_date");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "STAD");
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
    void insertTscStadLog() throws Exception {

        hStadCrtDate = hTempSystemDate;
        hStadCrtTime = hTempNotifyTime;
        hStadCardNo = hTardCardNo;
        setValue("crt_date", hStadCrtDate);
        setValue("crt_time", hStadCrtTime);
        setValue("tsc_card_no", hStadTscCardNo);
        setValue("card_no", hStadCardNo);
        setValue("process_code", hStadProcessCode);
        setValue("purchase_date", hStadPurchaseDate);
        setValue("purchase_time", hStadPurchaseTime);
        setValueDouble("tran_amt", hStadTranAmt);
        setValue("tran_stan", hStadTranStan);
        setValue("tran_rrn", hStadTranRrn);
        setValue("acq_id", hStadAcqId);
        setValue("term_id", hStadTermId);
        setValue("mcht_id", hStadMchtId);
        setValue("resp_code", hStadRespCode);
        setValue("tscc_data_seqno", hStadTsccDataSeqno);
        setValue("post_flag", hStadPostFlag);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "tsc_stad_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_stad_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void initTscStadLog() throws Exception {
        hStadCrtDate = "";
        hStadCrtTime = "";
        hStadTscCardNo = "";
        hStadCardNo = "";
        hStadProcessCode = "";
        hStadPurchaseDate = "";
        hStadPurchaseTime = "";
        hStadTranAmt = 0;
        hStadTranStan = "";
        hStadTranRrn = "";
        hStadAcqId = "";
        hStadTermId = "";
        hStadMchtId = "";
        hStadRespCode = "";
        hStadTsccDataSeqno = "";
        hStadRptFlag = "";
        hStadRptRespCode = "";
        hStadPostFlag = "N";
        hStadModTime = "";
        hStadModPgm = "";
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String procCode;
        String purchaseDate;
        String purchaseTime;
        String destinationAmt;
        String tranStan;
        String tranRrn;
        String acqId;
        String termId;
        String mchtId;
        String respCode;
        String filler0;
        String hashValue;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(procCode, 6);
            rtn += comc.fixLeft(purchaseDate, 8);
            rtn += comc.fixLeft(purchaseTime, 6);
            rtn += comc.fixLeft(destinationAmt, 13);
            rtn += comc.fixLeft(tranStan, 6);
            rtn += comc.fixLeft(tranRrn, 12);
            rtn += comc.fixLeft(acqId, 8);
            rtn += comc.fixLeft(termId, 8);
            rtn += comc.fixLeft(mchtId, 15);
            rtn += comc.fixLeft(respCode, 2);
            rtn += comc.fixLeft(filler0, 27);
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
        dtl.procCode = comc.subMS950String(bytes, 23, 6);
        dtl.purchaseDate = comc.subMS950String(bytes, 29, 8);
        dtl.purchaseTime = comc.subMS950String(bytes, 37, 6);
        dtl.destinationAmt = comc.subMS950String(bytes, 43, 13);
        dtl.tranStan = comc.subMS950String(bytes, 56, 6);
        dtl.tranRrn = comc.subMS950String(bytes, 62, 12);
        dtl.acqId = comc.subMS950String(bytes, 74, 8);
        dtl.termId = comc.subMS950String(bytes, 82, 8);
        dtl.mchtId = comc.subMS950String(bytes, 90, 15);
        dtl.respCode = comc.subMS950String(bytes, 105, 2);
        dtl.filler0 = comc.subMS950String(bytes, 107, 27);
        dtl.hashValue = comc.subMS950String(bytes, 134, 16);
        dtl.filler1 = comc.subMS950String(bytes, 150, 2);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF039 proc = new TscF039();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
