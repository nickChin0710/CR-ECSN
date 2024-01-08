/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-15   V1.00.01    tanwei      updated for project coding standard    *
******************************************************************************/

package Ips;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*自動加值交易每10日請款檔(I2B002)處理*/
public class IpsF032 extends AccessDAO {
    private String progname = "自動加值交易每10日請款檔(I2B002)處理  109/12/15 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTempNotifyDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hAdd2SystemDate = "";
    String hTnlgPerformFlag = "";
    String hTnlgNotifyDate = "";
    String hTnlgCheckCode = "";
    String hTnlgProcFlag = "";
    String hTnlgFileName = "";
    String hIcgeCardNo = "";
    String hBusiBusinessDate = "";
    String hIcgeIpsCardNo = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hIcgeTxnDate = "";
    String hIcgeTxnTime = "";
    String hIcgeTxnDateR = "";
    String hIcgeTxnTimeR = "";
    String hIcgeTrafficCd = "";
    String hIcgeTrafficCdSub = "";
    String hIcgeTrafficEqup = "";
    String hIcgeTrafficAbbr = "";
    String hIcgeAddrCd = "";
    double hIcgeTxnAmt = 0;
    double hIcgeTxnBal = 0;
    String hIcgeOnlineMark = "";
    String hTempBatchSeq = "";
    String hIcgeSeqNo = "";
    String hIcgeBillType = "";
    String hIcgeTransactionCode = "";
    String hIcgeMerchantNo = "";
    String hIcgeMerchantCategory = "";
    String hIcgeMerchantChiName = "";
    String hIcgeBillDesc = "";
    String hIcgePostFlag = "";
    String hIcgeReferenceNo = "";
    String fixBillType = "";
    int hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hBiunConfFlag = "";
    int hCnt = 0;
    int hErrCnt = 0;

    String hIcgeTsccDataSeqno = "";
    String hIcgeFileName = "";
    String hIcgeCreateDate = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String fileSeq = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int hTnlgRecordCnt = 0;
    int totalCnt = 0;
    String nUserpid = "";
    String tmpstr = "";
    int nRetcode = 0;

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
                comc.errExit("Usage : IpsF032 [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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
                if (args[0].length() == 8)
                    hTempNotifyDate = args[0];
                if (args[0].length() == 2) {
                    showLogMessage("I", "", "參數(一) 不可兩碼");
                }
            }
            if (args.length == 2) {
                hTempNotifyDate = args[0];
                if ((args[1].length() == 1) && (args[1].equals("Y")))
                    forceFlag = 1;
                if (args[1].length() == 2)
                    fileSeq = args[1];
                if (args[1].length() != 1 && args[1].length() != 2) {
                    showLogMessage("I", "", "參數(二) 為[force_flag] or [seq(nn)] ");
                }
            }
            if (args.length == 3) {
                hTempNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
                if (args[2].length() != 2) {
                    showLogMessage("I", "", "file seq 必須兩碼");
                }
                fileSeq = args[2];
            }
            selectPtrBusinday();

            if (hTempNotifyDate.substring(7, 8).equals("1") == false
                    || hTempNotifyDate.substring(6, 8).equals("31")) {
                exceptExit = 0;
                String stderr = String.format("本日[%s]非該月第 1,11,21 營業日", hTempNotifyDate);
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }

            tmpstr1 = String.format("I2B002_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            /*
            int rtn = selectIpsOrgdataLog();
            if (rtn != 0) {
                exceptExit = 0;
                String stderr = String.format("%s 檢核有錯本程式不執行..[%d]", javaProgram, rtn);
                backupRtn();
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }
            */

            deleteIpsOrgdataLog();
            
            fileOpen();

            updateIpsNotifyLogA();

            backupRtn();

            showLogMessage("I", "",
                    String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));

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
        sqlCmd = "select business_date,";
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
            hBusiBusinessDate = getValue("business_date");
            hTempSystemDate = getValue("h_temp_system_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
            hAdd2SystemDate = getValue("h_add2_system_date");
        }
        hTempNotifyDate = hTempNotifyDate.length() == 0 ? hBusiBusinessDate : hTempNotifyDate;

    }
    
    /***********************************************************************/
    void deleteIpsOrgdataLog() throws Exception {
        daoTable = "ips_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();

    }

    /***********************************************************************/
    int selectIpsOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt,";
        sqlCmd += "sum(decode(rpt_resp_code,'0000',0,1)) h_err_cnt ";
        sqlCmd += " from ips_orgdata_log  ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("IpsF021 檢核程式未執行或該日無資料需處理...", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        }
        return (hErrCnt);
    }

    /***********************************************************************/
    void backupRtn() throws Exception {

        String root = String.format("%s/media/ips", comc.getECSHOME());

        root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

        tmpstr2 = String.format("%s/media/ips/backup/%s/%s", comc.getECSHOME(), hTempNotifyDate, hTnlgFileName);
        comc.fileRename(String.format("%s/%s", root, hTnlgFileName), tmpstr2);

        //tmpstr1 = String.format("I2B003_%4.4s%8.8s%2.2s.zip", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
        //tmpstr2 = String.format("%s/media/ips/backup/%s/%s", comc.getECSHOME(), hTempNotifyDate, tmpstr1);
        //comc.fileRename(String.format("%s/%s", root, tmpstr1), tmpstr2);
    }

    /***********************************************************************/
    void updateIpsNotifyLogA() throws Exception {
        daoTable = "ips_notify_log";
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
            comcr.errRtn("update_ips_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";

        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if (f == -1) {
            comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
        }
        selectBilPostcntl();

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            str600 = comc.rtrim(str600);
            if ((comc.getSubString(str600, 0, 1).equals("H")) || (comc.getSubString(str600, 0, 1).equals("T")))
                continue;

            totalCnt++;

            initIpsCgecAll();
            hIcgeBillType = fixBillType;

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

//            tmpstr1 = String.format("%010.0f", h_orgd_tscc_data_seqno);
            tmpstr1 = String.format("%010.0f", comcr.str2double(hOrgdTsccDataSeqno));  //phopho mod
            hIcgeTsccDataSeqno = tmpstr1;

            hOrgdOrgData = str600;
            hIcgeIpsCardNo = comc.rtrim(dtl.ipsCardNo);

            if (selectIpsCard() != 0) {
                hOrgdRptRespCode = "0301";
                insertIpsOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnDate));
            hIcgeTxnDate = tmpstr1;
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "0203";
                insertIpsOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnTime));
            hIcgeTxnTime = tmpstr1;
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0204";
                insertIpsOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnDateR));
            hIcgeTxnDateR = tmpstr1;
            if (comm.checkDateFormat(tmpstr1, "yyyyMMdd") == false) {
                hOrgdRptRespCode = "0203";
                insertIpsOrgdataLog();
                continue;
            }
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnTimeR));
            hIcgeTxnTimeR = tmpstr1;
            if (comc.commTimeCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0204";
                insertIpsOrgdataLog();
                continue;
            }
            hIcgeTrafficCd = comc.rtrim(dtl.trafficCd);
            hIcgeTrafficCdSub = comc.rtrim(dtl.trafficCdSub);
            hIcgeTrafficEqup = comc.rtrim(dtl.trafficEqup);
            hIcgeTrafficAbbr = comc.rtrim(dtl.trafficAbbr);
            hIcgeAddrCd = comc.rtrim(dtl.addrCd);
            hIcgeOnlineMark = comc.rtrim(dtl.onlineMark);
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnAmt));
            if (comc.commDigitCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0201";
                insertIpsOrgdataLog();
                continue;
            }
            hIcgeTxnAmt = comcr.str2double(tmpstr1);
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnBal));
            if (comc.commDigitCheck(tmpstr1) == false) {
                hOrgdRptRespCode = "0201";
                insertIpsOrgdataLog();
                continue;
            }
            hIcgeTxnBal = comcr.str2double(tmpstr1);
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hIcgeTxnAmt;
            hOrgdRptRespCode = "0000";

            insertIpsOrgdataLog();

            insertIpsI2b002Log();
            succCnt++;
        }

        closeInputText(br);
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
            comcr.errRtn("select_bil_postcntl not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hTempBatchSeq = getValue("h_temp_batch_seq");
        }

    }

    /***********************************************************************/
    void initIpsCgecAll() throws Exception {

        hIcgeIpsCardNo = "";
        hIcgeTxnDate = "";
        hIcgeTxnTime = "";
        hIcgeTxnDateR = "";
        hIcgeTxnTimeR = "";
        hIcgeTrafficCd = "";
        hIcgeTrafficCdSub = "";
        hIcgeTrafficEqup = "";
        hIcgeTrafficAbbr = "";
        hIcgeAddrCd = "";
        hIcgeTxnAmt = 0;
        hIcgeTxnBal = 0;
        hIcgeOnlineMark = "";
        hIcgeCardNo = "";
        hIcgeBillType = "";
        hIcgeTransactionCode = "";
        hIcgeMerchantNo = "";
        hIcgeMerchantCategory = "";
        hIcgeMerchantChiName = "";
        hIcgeBillDesc = "";
        hIcgePostFlag = "n";
        hIcgeFileName = "";
        hIcgeReferenceNo = "";
        hIcgeCreateDate = "";
    }

    /***********************************************************************/
    int selectIpsCard() throws Exception {
        hIcgeCardNo = "";

        sqlCmd = "select card_no ";
        sqlCmd += " from ips_card  ";
        sqlCmd += "where ips_card_no = ? ";
        setString(1, hIcgeIpsCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIcgeCardNo = getValue("card_no");
        } else {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void insertIpsOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "I2B002");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ips_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_orgdata_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertIpsI2b002Log() throws Exception {
        setValue("ips_card_no", hIcgeIpsCardNo);
        setValue("card_no", hIcgeCardNo);
        setValue("txn_date", hIcgeTxnDate);
        setValue("txn_time", hIcgeTxnTime);
        setValue("txn_date_r", hIcgeTxnDateR);
        setValue("txn_time_r", hIcgeTxnTimeR);
        setValue("traffic_cd", hIcgeTrafficCd);
        setValue("traffic_cd_sub", hIcgeTrafficCdSub);
        setValue("traffic_equp", hIcgeTrafficEqup);
        setValue("traffic_abbr", hIcgeTrafficAbbr);
        setValue("addr_cd", hIcgeAddrCd);
        setValueDouble("txn_amt", hIcgeTxnAmt);
        setValueDouble("txn_bal", hIcgeTxnBal);
        setValue("online_mark", hIcgeOnlineMark);
        setValue("post_flag", "N");
        setValue("file_name", hTnlgFileName);
        setValue("crt_date", sysDate);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ips_i2b002_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_i2b002_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF032 proc = new IpsF032();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String ipsCardNo;
        String txnDate;
        String txnTime;
        String txnDateR;
        String txnTimeR;
        String trafficCd;
        String trafficCdSub;
        String trafficEqup;
        String trafficAbbr;
        String addrCd;
        String txnAmt;
        String txnBal;
        String onlineMark;
        String filler0;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(ipsCardNo, 11);
            rtn += comc.fixLeft(txnDate, 8);
            rtn += comc.fixLeft(txnTime, 6);
            rtn += comc.fixLeft(txnDateR, 8);
            rtn += comc.fixLeft(txnTimeR, 6);
            rtn += comc.fixLeft(trafficCd, 2);
            rtn += comc.fixLeft(trafficCdSub, 2);
            rtn += comc.fixLeft(trafficEqup, 30);
            rtn += comc.fixLeft(trafficAbbr, 20);
            rtn += comc.fixLeft(addrCd, 50);
            rtn += comc.fixLeft(txnAmt, 6);
            rtn += comc.fixLeft(txnBal, 6);
            rtn += comc.fixLeft(onlineMark, 1);
            rtn += comc.fixLeft(filler0, 28);
            rtn += comc.fixLeft(filler1, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.ipsCardNo = comc.subMS950String(bytes, 1, 11);
        dtl.txnDate = comc.subMS950String(bytes, 12, 8);
        dtl.txnTime = comc.subMS950String(bytes, 20, 6);
        dtl.txnDateR = comc.subMS950String(bytes, 26, 8);
        dtl.txnTimeR = comc.subMS950String(bytes, 34, 6);
        dtl.trafficCd = comc.subMS950String(bytes, 40, 2);
        dtl.trafficCdSub = comc.subMS950String(bytes, 42, 2);
        dtl.trafficEqup = comc.subMS950String(bytes, 44, 30);
        dtl.trafficAbbr = comc.subMS950String(bytes, 74, 20);
        dtl.addrCd = comc.subMS950String(bytes, 94, 50);
        dtl.txnAmt = comc.subMS950String(bytes, 144, 6);
        dtl.txnBal = comc.subMS950String(bytes, 150, 6);
        dtl.onlineMark = comc.subMS950String(bytes, 156, 1);
        dtl.filler0 = comc.subMS950String(bytes, 157, 28);
        dtl.filler1 = comc.subMS950String(bytes, 185, 2);
    }

}
