/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-14  V1.00.01    tanwei      updated for project coding standard     *
******************************************************************************/

package Ips;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*卡片退費每10日彙總檔(I2B011)處理*/
public class IpsF03a extends AccessDAO {
    private String progname = "卡片退費每10日彙總檔(I2B011)處理  109/12/14 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    int tempInt = 0;
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
    String hIcgeTransactionCode = "";
    String hIcgeMchtNo = "";
    String hIcgeMchtCategory = "";
    String hIcgeMchtChiName = "";
    String hIcgeBillDesc = "";
    String hIcgePostFlag = "";
    String hIcgeReferenceNo = "";
    String fixBillType = "";
    double hPostTotRecord = 0;
    double hPostTotAmt = 0;
    String hBiunConfFlag = "";
    String hBusiBusinessDate = "";
    int hCnt = 0;
    int hErrCnt = 0;
    String nUserpid = "";
    String tmpstr = "";
    String fileSeq = "";
    String tempPrevDate = "";
    String tempDate1 = "";
    String tempDate2 = "";
    String tempDate3 = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    int nRetcode = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    int rtn = 0;

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
                comc.errExit("Usage : IpsF03a [[notify_date][fo1yy_flag]] [force_flag][seq(nn)]", "");
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

            tempInt = 0;
            sqlCmd = "select count(*) temp_int ";
            sqlCmd += " from ptr_holiday  ";
            sqlCmd += "where holiday = ? ";
            setString(1, hTempNotifyDate);
            int recordCnt = selectTable();
            if (recordCnt > 0) {
                tempInt = getValueInt("temp_int");
            }
            if (tempInt > 0) {
                exceptExit = 0;
                String stderr = String.format("今日為假日, 不需執行 [%s]!!", hTempNotifyDate);
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }

            tempPrevDate = comcr.increaseDays(hTempNotifyDate, -1);
            tempDate1 = String.format("%6.6s01", hTempNotifyDate);
            tempDate2 = String.format("%6.6s11", hTempNotifyDate);
            tempDate3 = String.format("%6.6s21", hTempNotifyDate);

            if ((comcr.str2long(hTempNotifyDate) >= comcr.str2long(tempDate1)
                    && comcr.str2long(tempPrevDate) < comcr.str2long(tempDate1))
                    || (comcr.str2long(hTempNotifyDate) >= comcr.str2long(tempDate2)
                            && comcr.str2long(tempPrevDate) < comcr.str2long(tempDate2))
                    || (comcr.str2long(hTempNotifyDate) >= comcr.str2long(tempDate3)
                            && comcr.str2long(tempPrevDate) < comcr.str2long(tempDate3))) {
            } else {
                exceptExit = 0;
                String stderr = String.format(" 非 01,11,21   不需執行 [%s]!!", hTempNotifyDate);
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }

            tmpstr1 = String.format("I2B011_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTempNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            /*
            rtn = selectIpsOrgdataLog();
            if (rtn != 0) {
                String stderr = String.format("%s 檢核有錯本程式不執行..[%d]", javaProgram, rtn);
                backupRtn();
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }
            */

            fixBillType = "IPSS";

            //deleteBilPostcntl();
            //deleteIpsCgecAll();
            deleteIpsOrgdataLog();

            selectPtrBillunit();

            hPostTotRecord = 0;
            hPostTotAmt = 0;
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
    void updateIpsNotifyLogA() throws Exception {
        daoTable = "ips_notify_log";
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
            comcr.errRtn("update_ips_notify_log not found!", "", hCallBatchSeqno);
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
            comcr.errRtn("select_ptr_billunit not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBiunConfFlag = getValue("conf_flag");
        }

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
    int selectIpsOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt,";
        sqlCmd += "sum(decode(rpt_resp_code,'0000',0,1)) h_err_cnt ";
        sqlCmd += " from ips_orgdata_log  ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        } else {
            String stderr = String.format("IpsF021 檢核程式未執行或該日無資料需處理...");
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
        return (hErrCnt);
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
            if ((str600.substring(0, 1).equals("H")) || (str600.substring(0, 1).equals("T")))
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
            String hIcgeTsccDataSeqno = tmpstr1;

            hOrgdOrgData = str600;
            hIcgeIpsCardNo = comc.rtrim(dtl.ipsCardNo);
            hIcgeTxnType = comc.rtrim(dtl.txnType);

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
            tmpstr1 = String.format("%s", comc.rtrim(dtl.txnBal));
            hIcgeTxnBal = comcr.str2double(tmpstr1);

            hPostTotRecord++;
            hPostTotAmt = hPostTotAmt + hIcgeTxnAmt;
            hOrgdRptRespCode = "0000";

            insertIpsOrgdataLog();

            //insertIpsCgecAll();

            insertIpsI2b00aLog();

            updateIpsCard();

            if (hOrgdRptRespCode.equals("0000"))
                succCnt++;
        }

        //if (totalCnt > 0)
        //    insertBilPostcntl();

        closeInputText(br);
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
        } else {
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void insertIpsOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "I2B011");
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
     * 51：退卡退費 90：線上黑名單退費 91：批次黑名單退費 92：逾效期退費
     * 
     * @throws Exception
     */
    void insertIpsCgecAll() throws Exception {

        hIcgeMchtCategory = "4100";
        hIcgeMchtNo = "IPSS8001";
        if (hIcgeTxnType.equals("51")) {
            hIcgeMchtNo = "IPSS8002";
        }
        if (hIcgeTxnType.equals("90") || hIcgeTxnType.equals("91") || hIcgeTxnType.equals("92")) {
            hIcgeMchtNo = "IPSS8004";
        }

        if (hIcgeTxnAmt > 0) {
            tmpstr1 = String.format("一卡通退費%s%s", hIcgeTrafficCd, hIcgeAddrCd);
            hIcgeTransactionCode = "06";
        } else {
            tmpstr1 = String.format("代收一卡通使用超額%s%s", hIcgeTrafficCd, hIcgeAddrCd);
            hIcgeTransactionCode = "05";
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
        setValue("transaction_code", hIcgeTransactionCode);
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
    void insertIpsI2b00aLog() throws Exception {

        setValue("ips_card_no", hIcgeIpsCardNo);
        setValue("card_no", hIcgeCardNo);
        setValue("txn_type", hIcgeTxnType);
        setValue("txn_date", hIcgeTxnDate);
        setValue("txn_time", hIcgeTxnTime);
        setValueDouble("txn_amt", hIcgeTxnAmt);
        setValueDouble("txn_bal", hIcgeTxnBal);
        setValue("post_flag", "N");
        setValue("file_name", hTnlgFileName);
        setValue("crt_date", sysDate);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ips_i2b00a_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_i2b00a_log duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int updateIpsCard() throws Exception {

        daoTable = "ips_card";
        updateSQL = "balance_amt  = decode(cast(? as varchar(10)), '90', cast(? as double) , '91', cast(? as double) , '92', cast(? as double) , '51', cast(? as double) , balance_amt) ,";
        updateSQL += " balance_date  = decode(cast(? as varchar(10)), '90', cast(? as varchar(10)) , '91', cast(? as varchar(10)) , '92', cast(? as varchar(10)) , '51', cast(? as varchar(10)) , balance_date)";
        whereStr = "where rowid = ? ";
        setString(1, hIcgeTxnType);
        setDouble(2, hIcgeTxnAmt);
        setDouble(3, hIcgeTxnAmt);
        setDouble(4, hIcgeTxnAmt);
        setDouble(5, hIcgeTxnAmt);
        setString(6, hIcgeTxnType);
        setString(7, hIcgeTxnDate);
        setString(8, hIcgeTxnDate);
        setString(9, hIcgeTxnDate);
        setString(10, hIcgeTxnDate);
        setRowId(11, hIardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
        }

        return (0);
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

    /*************************************************************************/
    void initIpsCgecAll() {
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
        hIcgeTransactionCode = "";
        hIcgeMchtNo = "";
        hIcgeMchtCategory = "";
        hIcgeMchtChiName = "";
        hIcgeBillDesc = "";
        hIcgePostFlag = "N";
        hIcgeReferenceNo = "";
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF03a proc = new IpsF03a();
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
        dtl.ipsCardNo = comc.subMS950String(bytes, 1, 11);
        dtl.txnType = comc.subMS950String(bytes, 12, 2);
        dtl.txnDate = comc.subMS950String(bytes, 14, 8);
        dtl.txnTime = comc.subMS950String(bytes, 22, 6);
        dtl.txnBal = comc.subMS950String(bytes, 28, 6);
        dtl.txnAmt = comc.subMS950String(bytes, 34, 6);
        dtl.filler0 = comc.subMS950String(bytes, 40, 28);
        dtl.filler1 = comc.subMS950String(bytes, 68, 2);
    }

}
