/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/23  V1.00.01    Brian     error correction                          *
*  109-11-17  V1.00.02    tanwei    updated for project coding standard       *
*  110/01/14  V1.00.03    Wilson    mark ecs_crd_card                         *
*  112/05/04  V1.00.04    Wilson    mark update_tsc_notify_log not found      *
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

/*掛失贖回餘額資料檔(BTRD))接收處理*/
public class TscF038 extends AccessDAO {

    private final String progname = "掛失贖回餘額資料檔(BTRD))接收處理  112/05/04 V1.00.04";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommIps        comips   = new CommIps();

    String hCallBatchSeqno          = "";
    String hTempNotifyDate          = "";
    String hBusiBusinessDate        = "";
    String hTempSystemDate          = "";
    String hTempNotifyTime          = "";
    String hTnlgPerformFlag         = "";
    String hTnlgNotifyDate          = "";
    String hTnlgCheckCode           = "";
    String hTnlgProcFlag            = "";
    String hTnlgRowid                = "";
    String hTnlgFileName            = "";
    String hCgecPurchaseDate        = "";
    String hCgecCardNo              = "";
    String hTardAddvalueDate        = "";
    String hTardBalanceDate         = "";
    String hTardBlackltSDate       = "";
    String hTardBlackltEDate       = "";
    int    hTempDiffDays            = 0;
    String hTardCurrentCode         = "";
    String hTardNewEndDate         = "";
    String hTardLockDate            = "";
    String hTardAutoloadFlag        = "";
    String hTardReturnDate          = "";
    String hCgecTscCardNo          = "";
    String hOrgdTsccDataSeqno      = "";
    String hOrgdOrgData             = "";
    String hOrgdRptRespCode        = "";
    String hTempBatchSeq            = "";
    int    hCgecSeqNo               = 0;
    String hCgecBillType            = "";
    String hCgecTransactionCode     = "";
    String hCgecTscTxCode          = "";
    String hCgecPurchaseTime        = "";
    String hCgecMerchantChiName    = "";
    double hCgecDestinationAmt      = 0;
    String hCgecBillDesc            = "";
    String hCgecTrafficCd           = "";
    String hCgecTrafficAbbr         = "";
    String hCgecAddrCd              = "";
    String hCgecAddrAbbr            = "";
    String hCgecTscNotiDate        = "";
    String hCgecTscRespCode        = "";
    String hCgecTsccDataSeqno      = "";
    String hCgecOnlineMark          = "";
    String fixBillType               = "";
    double hPostTotRecord           = 0;
    double hPostTotAmt              = 0;
    String hBiunConfFlag            = "";
    int    hCnt                       = 0;
    int    hErrCnt                   = 0;
    double hBtrdTranAmt6h          = 0;
    double hBtrdTranFee6h          = 0;
    double hBtrdTranAmt             = 0;
    String hBtrdTsccRespCode       = "";
    double hBtrdTranAmt0h          = 0;
    String hCgprId                   = "";
    String hCgprIdCode              = "";
    String hCgprIdPSeqno           = "";
    String hCgprOnlineMark          = "";
    String hCgecBatchNo             = "";
    String hCgecPostFlag            = "";
    String hCgecFileName            = "";
    String hCgecTscError            = "";
    String hCgecReturnSource        = "";
    double hCgecServiceAmt          = 0;
    String hCgecMerchantNo          = "";
    String hCgecMerchantCategory    = "";
    String hCgecDestinationCurrency = "";

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
    String tSign0          = "";
    String tSign            = "";

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
                comc.errExit("Usage : TscF038 [[notify_date][force_flag]] [force_flag][seq(nn)]", "");
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

            tmpstr1 = String.format("BTRD.%8.8s.%8.8s%2.2s", comc.TSCC_BANK_ID8, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            fixBillType = "TSCC";
            hCgecBillType = fixBillType;

            deleteBilPostcntl();
            deleteTscCgecAll();
            deleteTscCgecPre();
            deleteTscBtrdLog();
            deleteTscOrgdataLog();

            selectPtrBillunit();

            hPostTotRecord = hPostTotAmt = 0;
            fileOpen();
            updateTscNotifyLoga();

            backupRtn();

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
        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8))" + ", ''"
                + ", to_char( decode( sign(substr(to_char(sysdate,'hh24miss'),1,4)-'1530')" + ", 1" + ", sysdate"
                + ", sysdate - 1 days)" + ", 'yyyymmdd')" + ", ?) h_temp_notify_date,";
        sqlCmd += " to_char(sysdate,'yyyymmdd') h_temp_system_date,";
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
    void deleteBilPostcntl() throws Exception {
        daoTable = "bil_postcntl";
        whereStr = "where this_close_date = ?  ";
        whereStr += "and batch_unit      = substr(?,1,2)  ";
        whereStr += "and mod_pgm         = ? ";
        setString(1, hTempNotifyDate);
        setString(2, fixBillType);
        setString(3, javaProgram);
        deleteTable();
    }

    /***********************************************************************/
    void deleteTscCgecAll() throws Exception {
        daoTable = "tsc_cgec_all a";
        whereStr = " where to_number(decode(a.tscc_data_seqno,'',null,a.tscc_data_seqno)) in (select b.tscc_data_seqno from tsc_orgdata_log b where b.file_name  = ?  ";
        whereStr += " and b.tscc_data_seqno = to_number(decode(a.tscc_data_seqno,'',null,a.tscc_data_seqno))) ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    void deleteTscCgecPre() throws Exception {
        daoTable = "tsc_cgec_pre a";
        whereStr = "where to_number(a.tscc_data_seqno) in (select b.tscc_data_seqno from tsc_orgdata_log b where b.file_name  = ?  ";
        whereStr += "and b.tscc_data_seqno = to_number(a.tscc_data_seqno)) ";
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
    void deleteTscBtrdLog() throws Exception {
        daoTable = "tsc_btrd_log a";
        whereStr = "where tscc_data_seqno = (select b.tscc_data_seqno " + " from tsc_orgdata_log b "
                + "where b.file_name = ?  ";
        whereStr += " and b.tscc_data_seqno = a.tscc_data_seqno) ";
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
//        if (notFound.equals("Y")) {
//            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
//        }

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
        showLogMessage("I", "", String.format("Open File=[%s]", tmpstr1));
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
            /*************************************************************************/
            hCgecTscCardNo = comc.rtrim(dtl.tscCardNo);

            if (selectTscCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
//            if (selectCrdCard() != 0 && (selectEcsCrdCard() != 0)) {
            if (selectCrdCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertTscOrgdataLog();
                continue;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranDate));
            hCgecTscNotiDate = tmpstr1;
            hCgecPurchaseDate = tmpstr1;
            if (!comc.commDateCheck(tmpstr1)) {
                hOrgdRptRespCode = "0203";
                insertTscOrgdataLog();
                continue;
            }
            hCgecPurchaseTime = hTempNotifyTime;
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt6hSign));
            tSign = tmpstr1;

            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt6h));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }
            hBtrdTranAmt6h = comcr.str2double(tmpstr1);
            if (tSign.equals("-")) {
                hBtrdTranAmt6h = comcr.str2double(tmpstr1) * -1;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranFee6hSign));
            tSign = tmpstr1;

            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranFee6h));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }
            hBtrdTranFee6h = comcr.str2double(tmpstr1);
            if (tSign.equals("-")) {
                hBtrdTranFee6h = comcr.str2double(tmpstr1) * -1;
            }
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmtSign));
            tSign = tmpstr1;

            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0202";
                insertTscOrgdataLog();
                continue;
            }

            hCgecTransactionCode = "06";
            hBtrdTranAmt = comcr.str2double(tmpstr1);
            if (tSign.equals("-")) {
                hCgecTransactionCode = "05";
                hBtrdTranAmt = comcr.str2double(tmpstr1) * -1;
            }
            hCgecDestinationAmt = comcr.str2double(tmpstr1);
            /*************************************************************************/
            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt0hSign));
            tSign0 = tmpstr1;

            tmpstr1 = String.format("%s", comc.rtrim(dtl.tranAmt0h));
            if (!comc.commDigitCheck(tmpstr1)) {
                hOrgdRptRespCode = "0201";
                insertTscOrgdataLog();
                continue;
            }
            hBtrdTranAmt0h = comcr.str2double(tmpstr1);
            if (tSign0.equals("-")) {
                hBtrdTranAmt0h = comcr.str2double(tmpstr1) * -1;
            }
            /*************************************************************************/
            hBtrdTsccRespCode = comc.rtrim(dtl.tsccRespCode);
            hCgecTscRespCode = hBtrdTsccRespCode;
            /*************************************************************************/
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + Math.abs(hCgecDestinationAmt);
            /*************************************************************************/
            tmpstr1 = comc.subMS950String(str600.getBytes("MS950"), 0, 112);
            tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
            if (!comc.subMS950String(str600.getBytes("MS950"), 112, 112 + 16).equals(tmpstr2)) {
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

            insertTscOrgdataLog();

            hCgecSeqNo = totalCnt;
            
            insertTscCgecAll();
            
            /*  
            //不寫tsc_cgec_pre,一律寫入cgec_all
            if (tSign.equals("-")) {
                tmpstr1 = String.format("掛失贖回餘額");
                hCgecMerchantChiName =  String.format("%-40.40s", tmpstr1);
                hCgecBillDesc = hCgecMerchantChiName;
                insertTscCgecPre();
            } else {
                tmpstr1 = String.format("掛失贖回餘額");
                hCgecMerchantChiName =  String.format("%-40.40s", tmpstr1);
                hCgecBillDesc = hCgecMerchantChiName;
                insertTscCgecAll();
            }
            */
            
            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }

        if (totalCnt > 0)
            insertBilPostcntl();
        if(br != -1)
            closeInputText(br);
    }

    /***********************************************************************/
    int selectCrdCard() throws Exception {
        // h_cgpr_id = "";
        hCgprIdCode = "";
        sqlCmd = "select major_id_p_seqno ";
        sqlCmd += " from crd_card  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hCgecCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            // h_cgpr_id = getValue("major_id");
            // h_cgpr_id_code = getValue("id_code");
            hCgprIdPSeqno = getValue("major_id_p_seqno");
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
//    int selectEcsCrdCard() throws Exception {
//        // h_cgpr_id = "";
//        hCgprIdCode = "";
//        sqlCmd = "select a.major_id_p_seqno ";
//        sqlCmd += " from ecs_crd_card a  ";
//        sqlCmd += "where a.card_no = ? ";
//        setString(1, hCgecCardNo);
//        int recordCnt = selectTable();
//        if (recordCnt > 0) {
//            // h_cgpr_id = getValue("major_id");
//            // h_cgpr_id_code = getValue("id_code");
//            hCgprIdPSeqno = getValue("major_id_p_seqno");
//        } else
//            return (1);
//        return (0);
//    }

    /***********************************************************************/
    int selectTscCard() throws Exception {
        hTardBalanceDate = "";
        hTardBlackltSDate = "";
        hTardBlackltEDate = "";
        hTardCurrentCode = "";
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
        setString(2, hCgecPurchaseDate.length()==0? null:hCgecPurchaseDate);
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
        } else
            return (1);
        return (0);
    }

    /***********************************************************************/
    void insertTscOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "BTRD");
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
    void insertTscCgecPre() throws Exception {
        if (hCgecTransactionCode.equals("05"))
            tmpstr1 = String.format("掛失贖回餘額");
        else
            tmpstr1 = String.format("掛失贖回餘額退款");
        hCgecMerchantChiName =  String.format("%-40.40s", tmpstr1);
        hCgecBillDesc = hCgecMerchantChiName;
        hCgecSeqNo = totalCnt;

        setValue("card_no", hCgecCardNo);
        setValue("id_p_seqno", hCgprIdPSeqno);
        setValue("tsc_card_no", hCgecTscCardNo);
        setValue("bill_type", hCgecBillType);
        setValue("txn_code", hCgecTransactionCode);
        setValue("tsc_tx_code", hCgecTscTxCode);
        setValue("purchase_date", hCgecPurchaseDate);
        setValue("purchase_time", hCgecPurchaseTime);
        setValue("mcht_no", "EASY8004");
        setValue("mcht_category", "4100");
        setValue("mcht_chi_name", hCgecMerchantChiName);
        setValueDouble("dest_amt", hCgecDestinationAmt);
        setValue("dest_curr", "901");
        setValue("bill_desc", hCgecBillDesc);
        setValue("post_flag", "N");
        setValue("file_name", hTnlgFileName);
        setValue("tsc_error", hOrgdRptRespCode);
        setValue("crt_date", hTempSystemDate);
        setValue("tsc_noti_date", hCgecTscNotiDate);
        setValue("tsc_resp_code", hCgecTscRespCode);
        setValue("tscc_data_seqno", hCgecTsccDataSeqno);
        setValue("online_mark", hCgprOnlineMark);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_cgec_pre";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_cgec_pre duplicate!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void insertTscCgecAll() throws Exception {

    	if (hCgecTransactionCode.equals("05"))
            tmpstr1 = String.format("悠遊卡掛失贖回負餘額");
        else
            tmpstr1 = String.format("悠遊卡掛失贖回餘額退款");
        hCgecMerchantChiName =  String.format("%-40.40s", tmpstr1);
        hCgecBillDesc = hCgecMerchantChiName;
        
        setValue("batch_no", hCgecBatchNo);
        setValueInt("seq_no", hCgecSeqNo);
        setValue("card_no", hCgecCardNo);
        setValue("tsc_card_no", hCgecTscCardNo);
        setValue("bill_type", hCgecBillType);
        setValue("txn_code", hCgecTransactionCode);
        setValue("tsc_tx_code", hCgecTscTxCode);
        setValue("purchase_date", hCgecPurchaseDate);
        setValue("purchase_time", hCgecPurchaseTime);
        setValue("mcht_no", "EASY8004");
        setValue("mcht_category", "4100");
        setValue("mcht_chi_name", hCgecMerchantChiName);
        setValueDouble("dest_amt", Math.abs(hCgecDestinationAmt));
        setValue("dest_curr", "901");
        setValue("bill_desc", hCgecBillDesc);
        setValue("traffic_cd", hCgecTrafficCd);
        setValue("traffic_abbr", hCgecTrafficAbbr);
        setValue("addr_cd", hCgecAddrCd);
        setValue("addr_abbr", hCgecAddrAbbr);
        setValue("post_flag", "N");
        setValue("file_name", hTnlgFileName);
        setValue("tsc_error", hOrgdRptRespCode);
        setValue("crt_date", hTempSystemDate);
        setValue("tsc_noti_date", hCgecTscNotiDate);
        setValue("tsc_resp_code", hCgecTscRespCode);
        setValue("tscc_data_seqno", hCgecTsccDataSeqno);
        setValue("online_mark", hCgecOnlineMark);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "tsc_cgec_all";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_cgec_all duplicate!", "", hCallBatchSeqno);
        }
        insertTscBtrdLog();
    }

    /***********************************************************************/
    void insertTscBtrdLog() throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("tsc_card_no", hCgecTscCardNo);
        setValueDouble("tran_amt_6h", hBtrdTranAmt6h);
        setValueDouble("tran_fee_6h", hBtrdTranFee6h);
        setValueDouble("tran_amt", hBtrdTranAmt);
        setValue("tran_date", hCgecPurchaseDate);
        setValue("tscc_resp_code", hBtrdTsccRespCode);
        setValueDouble("tran_amt_0h", hBtrdTranAmt0h);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "tsc_btrd_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_tsc_btrd_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        setValue("batch_date", hBusiBusinessDate);
        setValue("batch_unit", comc.getSubString(hCgecBillType, 0, 2));
        setValue("batch_seq", hTempBatchSeq);
        setValue("batch_no", hCgecBatchNo);
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
            comcr.errRtn("insert_bil_postcntl duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectBilPostcntl() throws Exception {
        hTempBatchSeq = "";
        sqlCmd = "select nvl(substr( to_char( max(batch_seq)+1, '0000'), 2, 4),'0000') h_temp_batch_seq ";
        sqlCmd += " from bil_postcntl ";
        sqlCmd += "where batch_unit = substr(?,1,2) ";
        sqlCmd += "  and batch_date = ? ";
        setString(1, fixBillType);
        setString(2, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_bil_postcntl not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempBatchSeq = getValue("h_temp_batch_seq");
        }
        hCgecBatchNo  = hBusiBusinessDate + comc.getSubString(hCgecBillType, 0, 2)
                         + hTempBatchSeq;
        showLogMessage("I", "", "888 BATCH_NO=" + hCgecBatchNo +","+hBusiBusinessDate);

    }

    /***********************************************************************/
    void initTscCgecAll() throws Exception {
//      h_cgec_batch_no = "";
        hCgecSeqNo = 0;
        hCgecCardNo = "";
        hCgecTscCardNo = "";
        hCgecBillType = fixBillType;
        hCgecTransactionCode = "05";
        hCgecTscTxCode = "";
        hCgecPurchaseDate = "";
        hCgecPurchaseTime = "";
        hCgecMerchantNo = "EASY8004";
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
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCardNo;
        String tranAmt6hSign;
        String tranAmt6h;
        String tranFee6hSign;
        String tranFee6h;
        String tranAmtSign;
        String tranAmt;
        String tranDate;
        String tsccRespCode;
        String tranAmt0hSign;
        String tranAmt0h;
        String filler;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(tranAmt6hSign, 1);
            rtn += comc.fixLeft(tranAmt6h, 12);
            rtn += comc.fixLeft(tranFee6hSign, 1);
            rtn += comc.fixLeft(tranFee6h, 12);
            rtn += comc.fixLeft(tranAmtSign, 1);
            rtn += comc.fixLeft(tranAmt, 12);
            rtn += comc.fixLeft(tranDate, 8);
            rtn += comc.fixLeft(tsccRespCode, 4);
            rtn += comc.fixLeft(tranAmt0hSign, 1);
            rtn += comc.fixLeft(tranAmt0h, 12);
            rtn += comc.fixLeft(filler, 43);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.attri = comc.subMS950String(bytes, 1, 2);
        dtl.tscCardNo = comc.subMS950String(bytes, 3, 20);
        dtl.tranAmt6hSign = comc.subMS950String(bytes, 23, 1);
        dtl.tranAmt6h = comc.subMS950String(bytes, 24, 12);
        dtl.tranFee6hSign = comc.subMS950String(bytes, 36, 1);
        dtl.tranFee6h = comc.subMS950String(bytes, 37, 12);
        dtl.tranAmtSign = comc.subMS950String(bytes, 49, 1);
        dtl.tranAmt = comc.subMS950String(bytes, 50, 12);
        dtl.tranDate = comc.subMS950String(bytes, 62, 8);
        dtl.tsccRespCode = comc.subMS950String(bytes, 70, 4);
        dtl.tranAmt0hSign = comc.subMS950String(bytes, 74, 1);
        dtl.tranAmt0h = comc.subMS950String(bytes, 75, 12);
        dtl.filler = comc.subMS950String(bytes, 87, 43);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF038 proc = new TscF038();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
