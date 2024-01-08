/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/03  V1.00.00    Brian     program initial                           *
 *  109-12-16   V1.00.01  tanwei      updated for project coding standard      *
 ******************************************************************************/

package Ich;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchF031 extends AccessDAO {
    private String progname = "店主檔(A01B)處理  109/12/16 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;

    String hCallBatchSeqno = "";

    String hTempNotifyDate = "";
    String hNextNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hAdd2SystemDate = "";
    String hTnlgPerformFlag = "";
    String hTnlgCheckCode = "";
    String hTnlgProcFlag = "";
    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    int hCnt = 0;
    int hErrCnt = 0;

    String hAgencyCd        = "";
    String hStoreCd         = "";
    String hAgencyName      = "";
    String hAgencyAbbrName = "";
    String hStoreName       = "";
    String hUpdDate         = "";
    String hUpdTime         = "";
    String hSysDate         = "";
    String hSysTime         = "";

    String tmpstr1 = "";
    String tmpstr2 = "";
    int forceFlag = 0;
    int totCnt = 0;
    int succCnt = 0;
    int hTnlgRecordCnt = 0;
    int totalCnt = 0;
    String tmpstr = "";
    String hHash = "";


    String out = "";

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
            //        if (comm.isAppActive(javaProgram)) {
            //            comc.err_exit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            //        }
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : ichF031 [[notify_date][fo1yy_flag]] [force_flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempNotifyDate = "";
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
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();

            tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A01B", comc.ICH_BANK_ID3, hTempNotifyDate);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            /*
            int rtn = selectIchOrgdataLog();
            if (rtn != 0) {
                String stderr = String.format("%s 檢核有錯本程式不執行..[%d]", javaProgram, rtn);
                backupRtn();
                comcr.errRtn(stderr, "", hCallBatchSeqno);
            }
            */

            deleteIchA01bAgent();

            fileOpen();

            updateIchNotifyLogA();

            backupRtn();

            showLogMessage("I", "",
                    String.format("Total process record[%d] fail_cnt[%d]", totalCnt, totalCnt - succCnt));
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
        hAdd2SystemDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_temp_system_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_temp_notify_time,";
        sqlCmd += "to_char(add_months(sysdate,2),'yyyymmdd') h_add2_system_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += " fetch first 1 rows only ";

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
        hNextNotifyDate = comm.nextDate(hTempNotifyDate);

    }

    /***********************************************************************/
    void backupRtn() throws Exception {

        String root = String.format("%s/media/ich", comc.getECSHOME());

        root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

        tmpstr2 = String.format("%s/media/ich/backup/%s/%s", comc.getECSHOME(), hTempNotifyDate, hTnlgFileName);
        comc.fileRename(String.format("%s/%s", root, hTnlgFileName), tmpstr2);

//        tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A01B", comc.ICH_BANK_ID3, h_temp_notify_date);
//        tmpstr2 = String.format("%s/media/ich/BACKUP/%s/%s", comc.GetECSHOME(), h_temp_notify_date, tmpstr1);
//        comc.file_rename(String.format("%s/%s", root, tmpstr1), tmpstr2);
    }

    /***********************************************************************/
    int selectIchOrgdataLog() throws Exception {
        sqlCmd = "select count(*) h_cnt,";
        sqlCmd += "sum(decode(rpt_resp_code,'0',0,1)) h_err_cnt ";
        sqlCmd += " from ich_orgdata_log  ";
        sqlCmd += "where file_name  = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("ichF021 檢核程式未執行或該日無資料需處理...", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hCnt = getValueInt("h_cnt");
            hErrCnt = getValueInt("h_err_cnt");
        }

        return (hErrCnt);
    }

    /***********************************************************************/
    void deleteIchA01bAgent() throws Exception {
        
    	while( true) {
    		daoTable = "ich_a01b_agent a";
            whereStr = "where 1=1 fetch first 5000 rows only ";
            int cnt = deleteTable();
            
            if (cnt == 0) break;
            
            commitDataBase();
    	}

    }

    /***********************************************************************/
    void updateIchNotifyLogA() throws Exception {
        daoTable = "ich_notify_log";
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
            comcr.errRtn("update_" + daoTable + " not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";
        String allData = "";

        /* read ARQB */
        String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int br = openInputText(temstr1, "MS950");
        if (br == -1) {
            comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
        }


        /* write ARPB */
        tmpstr1 = String.format("ARPB_%3.3s_%8.8s_A01B", comc.ICH_BANK_ID3, hNextNotifyDate);
        String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = temstr2;

        hHash = "0000000000000000000000000000000000000000";
        tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A01B", "02", "0001", comc.ICH_BANK_ID3,
                "00000000", hHash);

        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));

        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            str600 = comc.rtrim(str600);
            if (str600.substring(0, 1).equals("H"))
                continue;

            totalCnt++;

            initIchA01bAgent();

            splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
                commitDataBase();
            }

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();


            hOrgdOrgData = str600;

            hAgencyCd = comc.rtrim(dtl.agencyCd);
            hStoreCd = comc.rtrim(dtl.storeCd);
            hAgencyName = comc.rtrim(dtl.agencyName);
            hAgencyAbbrName = comc.rtrim(dtl.agencyAbbrName);
            hStoreName = comc.rtrim(dtl.storeName);
            hUpdDate = comc.rtrim(dtl.updDate);
            hUpdTime = comc.rtrim(dtl.updTime);
            hSysDate = comc.rtrim(dtl.sysDate);
            hSysTime = comc.rtrim(dtl.sysTime);

            hOrgdRptRespCode = "0";

            //insertIchOrgdataLog();

            insertIchA01bAgent();

            if (hOrgdRptRespCode.equals("0"))
                succCnt++;

            String buf = String.format("D%-20.20s%-20.20s%-14.14s%-6.6s%1.1s\r\n", hAgencyCd, hStoreCd, sysDate + sysTime,
                    " ", hOrgdRptRespCode);
            lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

            allData += buf;
        }

        closeInputText(br);
        moveBackup(hTnlgFileName);

        if(totalCnt > 0)
        {
            hHash  = comc.encryptSHA(allData, "SHA-1", "MS950");
            tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A01B", "02", "0001",
                    comc.ICH_BANK_ID3, comm.fillZero(Integer.toString(totalCnt), 8), hHash);
            lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
        }

        comc.writeReport(out, lpar, "MS950", false);
    }

    /***********************************************************************/
    void initIchA01bAgent() throws Exception {

        hAgencyCd        = "";
        hStoreCd         = "";
        hAgencyName      = "";
        hAgencyAbbrName = "";
        hStoreName       = "";
        hUpdDate         = "";
        hUpdTime         = "";
        hSysDate         = "";
        hSysTime         = "";
    }

    /***********************************************************************/
    void insertIchOrgdataLog() throws Exception {

        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "A01B");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ich_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_" + daoTable + " duplicate!", "", hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void updateIchA01bAgent() throws Exception {
        daoTable   = "ich_a01b_agent";
        updateSQL  = " agency_name      = ?, ";
        updateSQL += " agency_abbr_name = ?, ";
        updateSQL += " store_name       = ?, ";
        updateSQL += " upd_date         = ?, ";
        updateSQL += " upd_time         = ?,  ";
        updateSQL += " mod_time         = sysdate  ";
        whereStr   = "where agency_cd   = ?  ";
        whereStr  += "  and store_cd    = ?  ";
        setString(1, hAgencyName);
        setString(2, hAgencyAbbrName);
        setString(3, hStoreName);
        setString(4, sysDate);
        setString(5, sysTime);
        //       setString(6, sysDate + sysTime);
        setString(6, hAgencyCd);
        setString(7, hStoreCd);
        updateTable();
        if (notFound.equals("Y")) {
            String stderr = "update_ich_a01b_agent not found!";
            comcr.errRtn(stderr, "", hCallBatchSeqno);
        }
    }
    /***********************************************************************/
    void insertIchA01bAgent() throws Exception {

        setValue("agency_cd"  , hAgencyCd);
        setValue("store_cd"   , hStoreCd);
        setValue("agency_name", hAgencyName);
        setValue("agency_abbr_name", hAgencyAbbrName);
        setValue("store_name", hStoreName);
        setValue("upd_date", hUpdDate);
        setValue("upd_time", hUpdTime);
        setValue("sys_date", hSysDate);
        setValue("sys_time", hSysTime);
        setValue("file_name", hTnlgFileName);
        setValue("crt_date", sysDate);
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ich_a01b_agent";
        insertTable();
        if (dupRecord.equals("Y")) {
            updateIchA01bAgent();
            //      comcr.err_rtn("insert_" + daoTable + " duplicate!", "", h_call_batch_seqno);
        }

    }

    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String root = String.format("%s/media/ich", comc.getECSHOME());
        String src = String.format("%s/%s", root, moveFile);
        String target = String.format("%s/backup/%s/%s", root, hTempNotifyDate, moveFile);

        // if (DEBUG == 1)
        //     showLogMessage("I", "", "MOVE_BACK=[" + src + "]" + target);

        comc.fileRename(src, target);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchF031 proc = new IchF031();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String agencyCd;
        String storeCd;
        String agencyName;
        String agencyAbbrName;
        String storeName;
        String updDate;
        String updTime;
        String sysDate;
        String sysTime;
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl.type = comc.subMS950String(bytes, 0, 1);
        dtl.agencyCd = comc.subMS950String(bytes, 1, 20);
        dtl.storeCd = comc.subMS950String(bytes, 21, 20);
        dtl.agencyName = comc.subMS950String(bytes, 41, 40);
        dtl.agencyAbbrName = comc.subMS950String(bytes, 81, 20);
        dtl.storeName = comc.subMS950String(bytes, 101, 20);
        dtl.updDate = comc.subMS950String(bytes, 121, 8);
        dtl.updTime = comc.subMS950String(bytes, 129, 6);
        dtl.sysDate = comc.subMS950String(bytes, 135, 8);
        dtl.sysTime = comc.subMS950String(bytes, 143, 6);
    }



}
