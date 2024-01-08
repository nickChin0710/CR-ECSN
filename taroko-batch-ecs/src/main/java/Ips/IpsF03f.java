/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/07/10  V1.00.01    JeffKung  initial draft                             *
******************************************************************************/

package Ips;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*帳務調整彙整檔(I2B015)處理*/
public class IpsF03f extends AccessDAO {
    private String progname = "帳務調整彙整檔(I2B015)處理  112/11/27 V1.00.01";
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
    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hIcgeCardNo = "";
    String hIardRowid = "";
    String hIcgeIpsCardNo = "";
    String hIcgeTxnType = "";
    double hIcgeTxnAmt = 0;
    String hIcgeTxnDate = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hIcgeTxnTime = "";
    String hIcgeTxnDateR = "";
    String hIcgeTxnTimeR = "";
    String hIcgeTrafficCd = "";
    String hIcgeTrafficCdSub = "";
    String hIcgeTrafficEqup = "";
    String hIcgeTrafficAbbr = "";
    String hIcgeAddrCd = "";
    double hIcgeTxnBal = 0;
    String hIcgeOnlineMark = "";
    String hTempBatchSeq = "";
    int hIcgeSeqNo = 0;
    String hIcgeBillType = "";
    String hIcgeTxnCode = "";
    String hIcgeMchtNo = "";
    String hIcgeMchtCategory = "";
    String hIcgeMchtChiName = "";
    String hIcgeBillDesc = "";
    String hIcgePostFlag = "";
    String hIcgeReferenceNo = "";
    String fixBillType = "";
    int hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hBiunConfFlag = "";
    String hIcgeTsccDataSeqno = "";
    int hCnt = 0;
    int hErrCnt = 0;

    String tmpstr1 = "";
    String tmpstr2 = "";
    String hBusiBusinessDate = "";
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
                comc.errExit("Usage : IpsF03f [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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

            tmpstr1 = String.format("I2B015_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            fixBillType = "IPSS";

            deleteBilPostcntl();
            deleteIpsCgecAll();
            deleteIpsOrgdataLog();
            
            selectPtrBillunit();

            hPostTotRecord = 0;
            hPostTotAmt = 0;
            
            fileOpen();

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
    void backupRtn() throws Exception {

        String root = String.format("%s/media/ips", comc.getECSHOME());

        root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

        tmpstr2 = String.format("%s/media/ips/backup/%s/%s", comc.getECSHOME(), hTempNotifyDate, hTnlgFileName);
        comc.fileRename(String.format("%s/%s", root, hTnlgFileName), tmpstr2);

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
    void deleteBilPostcntl() throws Exception {
        daoTable = "bil_postcntl";
        whereStr = "where this_close_date = ?  ";
        whereStr += "and batch_unit  = substr(?,1,2)  ";
        whereStr += "and mod_pgm   = ? ";
        setString(1, hTempNotifyDate);
        setString(2, fixBillType);
        setString(3, javaProgram);
        deleteTable();
    }

    /***********************************************************************/
    void deleteIpsCgecAll() throws Exception {
        daoTable = "ips_cgec_all a";
        whereStr = "where a.tscc_data_seqno in (select b.tscc_data_seqno from ips_orgdata_log b where b.file_name  = ?  ";
        whereStr += "and b.tscc_data_seqno = a.tscc_data_seqno) ";
        setString(1, hTnlgFileName);
        deleteTable();
    }

    /***********************************************************************/
    void deleteIpsOrgdataLog() throws Exception {
        daoTable = "ips_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();

    }
    
    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";

        selectBilPostcntl();

        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int f = openInputText(temstr1);
        if (f == -1) {
            comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
        }
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
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnAmt));
            hIcgeTxnAmt = comcr.str2double(tmpstr1);
            hIcgeTxnType = comc.rtrim(dtl.txnType);
            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hIcgeTxnAmt;
            hOrgdRptRespCode = "0000";

            insertIpsOrgdataLog();
            insertIpsCgecAll();

            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }
        
        if (totalCnt > 0)
            insertBilPostcntl();

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
    void selectPtrBillunit() throws Exception {
        hBiunConfFlag = "";

        sqlCmd = "select conf_flag ";
        sqlCmd += " from ptr_billunit  ";
        sqlCmd += "where bill_unit = substr(?,1,2) ";
        setString(1, fixBillType);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_billunit not found!", "", fixBillType);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

    }
    
    /***********************************************************************/
    void initIpsCgecAll() throws Exception {
        hIcgeIpsCardNo = "";
        hIcgeTxnType = "";
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
        hIcgeTxnCode = "";
        hIcgeMchtNo = "";
        hIcgeMchtCategory = "";
        hIcgeMchtChiName = "";
        hIcgeBillDesc = "";
        hIcgePostFlag = "N";
        hIcgeReferenceNo = "";
    }

    /***********************************************************************/
    int selectIpsCard() throws Exception {
        hIcgeCardNo = "";
        hIardRowid = "";

        sqlCmd = "select card_no,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from ips_card  ";
        sqlCmd += "where ips_card_no = ? ";
        setString(1, hIcgeIpsCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIcgeCardNo = getValue("card_no");
            hIardRowid = getValue("rowid");
        } else
            return 1;

        return recordCnt > 0 ? 0 : 1;
    }

    /***********************************************************************/
    void insertIpsOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "I2B015");
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

    /***
     * 
     * @throws Exception
     */
    void insertIpsCgecAll() throws Exception {

        hIcgeMchtCategory = "4100";
        hIcgeMchtNo = "IPSS8002";
        String txnDesc = "";
        if (hIcgeTxnType.equals("01")) {
        	txnDesc = "調整帳";
        } else if (hIcgeTxnType.equals("02")) {
        	txnDesc = "北捷回饋";
        } else if (hIcgeTxnType.equals("03")) {
        	txnDesc = "定期票退票";
        }

        if (hIcgeTxnAmt > 0) {
            tmpstr1 = String.format("一卡通退費-%s", txnDesc);
            hIcgeTxnCode = "06";
        } else {
            tmpstr1 = String.format("代收一卡通使用超額-%s", txnDesc);
            hIcgeTxnCode = "05";
        }
        hIcgeMchtChiName = tmpstr1;
        hIcgeBillDesc = tmpstr1;
        hIcgeSeqNo = totalCnt;

        setValue("ips_card_no", hIcgeIpsCardNo);
        setValue("txn_type", hIcgeTxnType);
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
        setValue("batch_no", hBusiBusinessDate + hIcgeBillType.substring(0, 2) + hTempBatchSeq);
        setValueInt("seq_no", hIcgeSeqNo);
        setValue("card_no", hIcgeCardNo);
        setValue("bill_type", hIcgeBillType);
        setValue("txn_code", hIcgeTxnCode);
        setValue("mcht_no", hIcgeMchtNo);
        setValue("mcht_category", hIcgeMchtCategory);
        setValue("mcht_chi_name", hIcgeMchtChiName);
        setValue("bill_desc", hIcgeBillDesc);
        setValue("post_flag", hIcgePostFlag);
        setValue("file_name", hTnlgFileName);
        setValue("reference_no", hIcgeReferenceNo);
        setValue("crt_date", hTempSystemDate);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ips_cgec_all";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_cgec_all duplicate!", "", hCallBatchSeqno);
        }

    }
    
    /***********************************************************************/
    void insertBilPostcntl() throws Exception {
        setValue("batch_date", hBusiBusinessDate);
        setValue("batch_unit", hIcgeBillType.substring(0, 2));
        setValue("batch_seq", hTempBatchSeq);
        setValue("batch_no", hBusiBusinessDate + hIcgeBillType.substring(0, 2) + hTempBatchSeq);
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
    public static void main(String[] args) throws Exception {
        IpsF03f proc = new IpsF03f();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String ipsCardNo;
        String txnType;
        String txnDate;
        String txnTime;
        String txnBal;
        String txnAmt;
        String filler0;
        String filler1;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(ipsCardNo, 11);
            rtn += comc.fixLeft(txnType, 2);
            rtn += comc.fixLeft(txnDate, 8);
            rtn += comc.fixLeft(txnTime, 6);
            rtn += comc.fixLeft(txnBal, 6);
            rtn += comc.fixLeft(txnAmt, 6);
            rtn += comc.fixLeft(filler0, 28);
            rtn += comc.fixLeft(filler1, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.ipsCardNo = comc.subMS950String(bytes, 1, 16).trim();
        dtl.txnType = comc.subMS950String(bytes, 17, 2);
        dtl.txnDate = comc.subMS950String(bytes, 19, 8);
        dtl.txnTime = comc.subMS950String(bytes, 27, 6);
        dtl.txnAmt = comc.subMS950String(bytes, 34, 6);
        dtl.filler0 = comc.subMS950String(bytes, 40, 28);
        dtl.filler1 = comc.subMS950String(bytes, 68, 2);
    }

}
