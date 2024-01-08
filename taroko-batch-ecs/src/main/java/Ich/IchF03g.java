/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/07/20  V1.00.01  Zuwei Su    program initial (檔名無.txt)                *
 *  112/09/18  V1.00.02  Zuwei Su    補充遺漏method                *
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

public class IchF03g extends AccessDAO {
    private final String progname = "icash愛金卡點數檔(A16B) 寫檔回覆處理  112/09/18  V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId = "";
    String rptName = "";
    int rptSeq = 0;

    String hCallBatchSeqno = "";

    String hTempNotifyDate = "";
    String hNextNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempSystemDate = "";
    String hTempNotifyTime = "";
    String hAdd2SystemDate = "";
//    String hTnlgPerformFlag = "";
//    String hTnlgNotifyDate = "";
//    String hTnlgCheckCode = "";
//    String hTnlgProcFlag = "";
//    String hTnlgRowid = "";
    String hTnlgFileName = "";
    String hOrgdTsccDataSeqno = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    int hCnt = 0;
    int hErrCnt = 0;

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

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempNotifyDate = "";
            forceFlag = 0;

            selectPtrBusinday();

            if (args.length == 0) {
                hTempNotifyDate = hBusiBusinessDate;
            } else if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTempNotifyDate = args[0];
                if (args[0].length() == 2) {
                    showLogMessage("I", "", "參數(一) 不可兩碼");
                }
            } else if (args.length == 2) {
                hTempNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            } else {
                comc.errExit("Usage : IchF03g [[notify_date][fo1yy_flag]] [force_flag]", "");
            }

            hNextNotifyDate = comm.nextNDate(hTempNotifyDate, 1);

            tmpstr1 = String.format("ARQB_%3.3s_%8.8s_A16B", comc.ICH_BANK_ID3, hTempNotifyDate);
            hTnlgFileName = tmpstr1;
            showLogMessage("I", "", String.format(" 處理檔案=[%s]", tmpstr1));

            //deleteIchA03bLock();
            deleteIchOrgdataLog();
            
            fileOpen();

            updateIchNotifyLogA();

            showLogMessage("I", "", String.format("Total process record[%d], fail_cnt[%d]", totalCnt,
                    totalCnt - succCnt));
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
        // hTempNotifyDate = hTempNotifyDate.length() == 0 ? sysDate : hTempNotifyDate;
        hBusiBusinessDate = "";
        hAdd2SystemDate = "";
        sqlCmd = "select business_date, ";
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

    }

    /***********************************************************************/
    void deleteIchA03bLock() throws Exception {
      daoTable = "ich_a03b_lock a";
      whereStr =
          "where a.tscc_data_seqno in (select b.tscc_data_seqno from ich_orgdata_log b where b.file_name  = ?  ";
      whereStr += "and b.tscc_data_seqno = a.tscc_data_seqno) ";
      setString(1, hTnlgFileName);
      deleteTable();
    }
    
    /***********************************************************************/
    void deleteIchOrgdataLog() throws Exception {
        daoTable = "ich_orgdata_log";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        deleteTable();

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
//      if (notFound.equals("Y")) {
//        comcr.errRtn("update_ich_notify_log not found!", "", hCallBatchSeqno);
//      }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String str600 = "";
        String allData = "";

        /* read ARQB */
        String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int br = openInputText(temstr1, "MS950");
        //沒有檔案不abend 20230918
        if (br == -1) {
            showLogMessage("I","","檔案不存在："+temstr1);
            return;
        	//comcr.errRtn("檔案不存在：" + temstr1, "", hCallBatchSeqno);
        }

        /* write ARPB */
        tmpstr1 = String.format("ARPB_%3.3s_%8.8s_A16B", comc.ICH_BANK_ID3, hBusiBusinessDate);
        String temstr2 = String.format("%s/media/ich/%s", comc.getECSHOME(), tmpstr1);
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);
        out = temstr2;

        hHash = "0000000000000000000000000000000000000000";
        tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A16B", "02", "0001",
                comc.ICH_BANK_ID3, "00000000", hHash);

        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));

        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) {
                break;
            }

            str600 = comc.rtrim(str600);
            if (str600.substring(0, 1).equals("H")) {
                continue;
            }
            totalCnt++;

            Buf1 dtl = splitBuf1(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1) {
                showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
            }

            hOrgdTsccDataSeqno = comcr.getTSCCSeq();

            if (!str600.substring(0, 1).equals("D")) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }

            hOrgdOrgData = str600;

            if (insertMktOpenpointData(dtl) != 1) {
                hOrgdRptRespCode = "1";
                insertIchOrgdataLog();
                continue;
            }
            succCnt++;

            String buf = String.format("D%-8s%-20s%-16s%-8s%8s%1s\r\n", dtl.txDate, dtl.orgNo,
                    dtl.icashCardNo, dtl.activeType, comm.fillLeftSpace("", 8), "0");
            lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

            allData += buf;
        }

        closeInputText(br);
        moveBackup(hTnlgFileName);

        if (totalCnt > 0) {
            hHash = comc.encryptSHA(allData, "SHA-1", "MS950");
            tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%8.8s%-40.40s\r\n", "A16B", "02", "0001",
                    comc.ICH_BANK_ID3, comm.fillZero(Integer.toString(totalCnt), 8), hHash);
            lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
        }

        comc.writeReport(out, lpar, "MS950", false);
    }


    /***********************************************************************/
    void insertIchOrgdataLog() throws Exception {
        setValue("tscc_data_seqno", hOrgdTsccDataSeqno);
        setValue("file_iden", "A16B");
        setValue("notify_date", hTempNotifyDate);
        setValue("file_name", hTnlgFileName);
        setValue("org_data", hOrgdOrgData);
        setValue("rpt_resp_code", hOrgdRptRespCode);
        setValue("mod_pgm", javaProgram);
        setValue("mod_time", sysDate + sysTime);
        daoTable = "ich_orgdata_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ich_orgdata_log duplicate!", "file_name = " + hTnlgFileName,
                    hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    int insertMktOpenpointData(Buf1 dtl) throws Exception {
        setValue("tx_date", dtl.txDate);
        setValue("org_no", dtl.orgNo);
        setValue("icash_cardno", dtl.icashCardNo);
        setValue("tx_type", dtl.txType);
        setValue("active_type", dtl.activeType);
        setValue("tx_amt", dtl.txAmt);
        setValue("mod_user", null);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "mkt_openpoint_data";
        int retCode = insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_mkt_openpoint_data duplicate!", "icash_card_no = " + dtl.icashCardNo,
                    hCallBatchSeqno);
        }
        
        return retCode;
    }

    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String root = String.format("%s/media/ich", comc.getECSHOME());
        String src = String.format("%s/%s", root, moveFile);
        String target = String.format("%s/backup/%s/%s", root, hTempNotifyDate, moveFile);

        comc.fileRename2(src, target);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IchF03g proc = new IchF03g();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String icashCardNo;
        String txDate;
        String orgNo;
        String txType;
        String activeType;
        String txAmt;
    }

    Buf1 splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        Buf1 dtl = new Buf1();
        dtl.type = comc.subMS950String(bytes, 0, 1); // 檔頭標籤
        dtl.txDate = comc.subMS950String(bytes, 1, 8); // 交易日期
        dtl.orgNo = comc.subMS950String(bytes, 9, 20); // 特約機構
        dtl.icashCardNo = comc.subMS950String(bytes, 29, 16); // icash卡號
        dtl.txType = comc.subMS950String(bytes, 45, 2); // 交易類別
        dtl.activeType = comc.subMS950String(bytes, 47, 8); // 活動代號
        dtl.txAmt = comc.subMS950String(bytes, 55, 8); // 金額
        
        return dtl;
    }
}
