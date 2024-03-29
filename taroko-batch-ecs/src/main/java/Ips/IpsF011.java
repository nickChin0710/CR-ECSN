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

/*聯名卡卡號暨錄碼資料彙總檔(B2I001)媒體處理*/
public class IpsF011 extends AccessDAO {
    private String progname = "聯名卡卡號暨錄碼資料彙總檔(B2I001)媒體處理  109/12/15 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTnlgProcFlag = "";
    String hTnlgFileName = "";
    String hTnlgFileIden = "";
    String hTnlgFtpSendDate = "";
    String hTnlgFtpSendTime = "";
    String hTnlgFtpReceiveDate = "";
    String hTnlgFtpReceiveTime = "";
    String hTnlgCheckCode = "";
    String hTnlgRecordSucc = "";
    String hTnlgRecordFail = "";
    String hTnlgRowid = "";
    String hTempRptRespDate = "";
    String hTempRptRespTime = "";
    String hRetdCardIcNo = "";
    String hTempFileName = "";
    String hRetmFileType = "";
    String hErrType = "";
    int hRecordCnt = 0;
    int hRecordSucc = 0;
    int hRecordFail = 0;
    String hRetdIpsCardNo = "";
    String hRetdErrCode = "";
    String hIcdrIpsCardNo = "";
    String hIcdrCardNo = "";
    String hIcdrRowid = "";

    int hCnt = 0;
    int hErrCnt = 0;
    String nUserpid = "";
    String tmpstr = "";
    String fileSeq = "";
    String tempPrevDate = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String swAck = "";
    String swNg = "";
    String swErr = "";
    String tempFileName = "";
    int hTnlgRecordCnt = 0;
    int nRetcode = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    int totCnt = 0;

    Buf1 dtl1 = new Buf1();
    Buf2 dtl2 = new Buf2();
    Buf3 dtl3 = new Buf3();

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
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IpsF011 [notify_date [force_flag]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTnlgProcFlag = "N";
            hTnlgNotifyDate = "";
            if (args.length >= 1)
                hTnlgNotifyDate = args[0];
            if ((args.length == 2) && (args[1].toCharArray()[0] == 'Y'))
                hTnlgProcFlag = "Y";
            selectPtrBusinday();
            showLogMessage("I", "", String.format("處理IPS 檔案日期[%s][%s]", hTnlgNotifyDate, hTnlgProcFlag));

            selectIpsNotifyLog();

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
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? sysDate : hTnlgNotifyDate;
        hBusiBusinessDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }

    }

    /***********************************************************************/
    void selectIpsNotifyLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "file_name,";
        sqlCmd += "file_iden,";
        sqlCmd += "ftp_send_date,";
        sqlCmd += "ftp_send_time,";
        sqlCmd += "ftp_receive_date,";
        sqlCmd += "ftp_receive_time,";
        sqlCmd += "check_code,";
        sqlCmd += "record_succ,";
        sqlCmd += "record_fail,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += " from ips_notify_log ";
        sqlCmd += "where proc_flag   = decode(cast(? as varchar(1)),'Y',proc_flag,'3') ";
        sqlCmd += "  and file_iden   = 'B2I001' ";
        sqlCmd += "  and notify_date = decode(cast(? as varchar(10)) , '', notify_date,?) ";
        sqlCmd += " order by notify_date ";
        setString(1, hTnlgProcFlag);
        setString(2, hTnlgNotifyDate);
        setString(3, hTnlgNotifyDate);
        openCursor();
        while (fetchTable()) {
            hTnlgFileName = getValue("file_name");
            hTnlgFileIden = getValue("file_iden");
            hTnlgFtpSendDate = getValue("ftp_send_date");
            hTnlgFtpSendTime = getValue("ftp_send_time");
            hTnlgFtpReceiveDate = getValue("ftp_receive_date");
            hTnlgFtpReceiveTime = getValue("ftp_receive_time");
            hTnlgCheckCode = getValue("check_code");
            hTnlgRecordSucc = getValue("record_succ");
            hTnlgRecordFail = getValue("record_fail");
            hTnlgRowid = getValue("rowid");

            showLogMessage("I", "", String.format("[%s] 處理中 ..[%s]", hTnlgFileName, hTnlgCheckCode));
            if (!hTnlgCheckCode.equals("0000")) {
                updateIpsNotifyLog();
                String stderr = String.format("[%s] 整檔處理失敗, 錯誤代碼[%s](error)!", hTnlgFileName, hTnlgCheckCode);
                showLogMessage("I", "", stderr);
                continue;
            }
            fileOpen1();
            if (swAck.equals("N") && swNg.equals("N")) {
                String stderr = String.format("找不到回覆檔[ack,ng], 請通知相關人員處理(error)");
                showLogMessage("I", "", stderr);
                continue;
            }
            fileOpenAck();
            fileOpenNg();
            fileOpenErr();
            if (swAck.equals("N") && swNg.equals("N")) {
                showLogMessage("I", "", "找不到回覆檔[ack,ng], 請通知相關人員處理(error)");
                continue;
            }
            hTnlgCheckCode = "0000";
            updateIpsNotifyLog();

            String root = String.format("%s/media/ips", comc.getECSHOME());

            root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

            tempFileName = String.format("%-21.21s.zip.ack", hTnlgFileName);
            tmpstr1 = String.format("%s/%s", root, tempFileName);
            tmpstr2 = String.format("%s/media/ips/BACKUP/%s/%s", comc.getECSHOME(), hTnlgNotifyDate, tempFileName);
            comc.fileRename(tmpstr1, tmpstr2);

            tempFileName = String.format("%-21.21s.zip.ng", hTnlgFileName);
            tmpstr1 = String.format("%s/%s", root, tempFileName);
            tmpstr2 = String.format("%s/media/ips/BACKUP/%s/%s", comc.getECSHOME(), hTnlgNotifyDate, tempFileName);
            comc.fileRename(tmpstr1, tmpstr2);

            tempFileName = String.format("%-21.21s.zip.err", hTnlgFileName);
            tmpstr1 = String.format("%s/%s", root, tempFileName);
            tmpstr2 = String.format("%s/media/ips/BACKUP/%s/%s", comc.getECSHOME(), hTnlgNotifyDate, tempFileName);
            comc.fileRename(tmpstr1, tmpstr2);

        }
        closeCursor();
    }

    /***********************************************************************/
    @SuppressWarnings("resource")
    int fileOpen1() throws Exception {
        int intb = 0, headTag = 0, tailTag = 0;
        String str600 = "";

        hTnlgRecordCnt = 0;

        tempFileName = String.format("%-21.21s.zip.ack", hTnlgFileName);
        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tempFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        swAck = "Y";
        int f = openInputText(temstr1);
        if (f == -1) {
            swAck = "N";
        } else
            closeInputText(f);

        tempFileName = String.format("%-21.21s.zip.ng", hTnlgFileName);
        temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tempFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        swNg = "Y";
        f = openInputText(temstr1);
        if (f == -1) {
            swNg = "N";
        } else 
            closeInputText(f);

        tempFileName = String.format("%-21.21s.zip.err", hTnlgFileName);
        temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tempFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        swErr = "Y";
        f = openInputText(temstr1);
        if (f == -1) {
            swErr = "N";
            return 1;
        } else 
            closeInputText(f);

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;

            // h_tnlg_check_code = "0102";
            // h_tnlg_check_code = "0112";
            // h_tnlg_check_code = "0103";
            hTnlgCheckCode = "";

            if (str600.substring(0, 1).equals("D"))
                intb++;
            if (str600.substring(0, 1).equals("H")) {
                if (headTag != 0) {
                    hTnlgCheckCode = "0111";/* HEADER_DUPLICATE */
                    updateIpsNotifyLog();
                    return (1);
                }
                headTag = 1;
            }
            if (str600.substring(0, 1).equals("T")) {
                if (tailTag != 0) {
                    hTnlgCheckCode = "0121";/* TAILER_DUPLICATE */
                    updateIpsNotifyLog();
                    return (1);
                }
                tailTag = 1;
                String tmpstr3 = String.format("%6.6s", str600.substring(13));
                hTnlgRecordCnt = comcr.str2int(tmpstr3);
                if (comcr.str2long(tmpstr3) != intb) {
                    hTnlgCheckCode = "0124";/* TOTAL_QTY_ERR */
                    showLogMessage("I", "", String.format(" cnt = [%d][%d]", comcr.str2long(tmpstr3), intb));
                    updateIpsNotifyLog();
                    return (1);
                }
            }
        }

        closeInputText(br);

        hTnlgCheckCode = "0000";
        updateIpsNotifyLog();

        return (0);
    }

    /***********************************************************************/
    void fileOpenAck() throws Exception {
        hTnlgRecordCnt = 0;
        String str600 = "";

        tempFileName = String.format("%-21.21s.zip.ack", hTnlgFileName);
        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tempFileName);

        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

        swAck = "Y";
        int f = openInputText(temstr1);
        if(f == -1) {
            swAck = "N";
            return;
        }

        hErrType = "";
        hRecordCnt = 0;
        hRecordSucc = 0;
        hRecordFail = 0;

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            if (str600.length() < 8)
                continue;
            hRetmFileType = "ack";
            insertIpsFileResp();
        }
        closeInputText(br);
    }

    /***********************************************************************/
    void fileOpenNg() throws Exception {
        String str600 = "";
        hTnlgRecordCnt = 0;
        tempFileName = String.format("%-21.21s.zip.ng", hTnlgFileName);
        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tempFileName);

        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

        hErrType = "";
        hRecordCnt = 0;
        hRecordSucc = 0;
        hRecordFail = 0;

        swNg = "Y";
        int f = openInputText(temstr1);
        if(f == -1) {
            swNg = "N";
            return;
        }
        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            if (str600.length() < 8)
                continue;
            hRetmFileType = "ng";
            hErrType = String.format("%1.1s", str600.substring(14));
            insertIpsFileResp();
        }

        resetAll();

        closeInputText(br);
    }

    /***********************************************************************/
    void resetAll() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "ips_card_no,";
        sqlCmd += "card_no,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from ips_cdrp_log a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and file_name  = ? ";
        sqlCmd += "  and proc_flag  = 'Y' ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hIcdrIpsCardNo = getValue("ips_card_no", i);
            hIcdrCardNo = getValue("card_no", i);
            hIcdrRowid = getValue("rowid", i);

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

            daoTable   = "ips_cdrp_log";
            updateSQL  = " media_crt_date = '',";
            updateSQL += " media_crt_time = '',";
            updateSQL += " notify_date    = '',";
            updateSQL += " file_name      = '',";
            updateSQL += " proc_flag      = 'N',";
            updateSQL += " mod_pgm        = ?,";
            updateSQL += " mod_time       = sysdate";
            whereStr   = "where rowid     = ? ";
            setString(1, javaProgram);
            setRowId(2, hIcdrRowid);
            updateTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("update_ips_cdrp_log not found!", "", hCallBatchSeqno);
            }

        }
    }

    /***********************************************************************/
    void fileOpenErr() throws Exception {
        String str600 = "";
        String tmpstr3 = "";
        tempFileName = String.format("%-21.21s.zip.err", hTnlgFileName);
        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), tempFileName);

        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);

        swErr = "Y";

        int f = openInputText(temstr1);
        if(f == -1) {
            swErr = "N";
            return;
        }

        totalCnt = 0;
        hErrType = "";
        hRecordCnt = 0;
        hRecordSucc = 0;
        hRecordFail = 0;

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            str600 = comc.rtrim(str600);

            if (str600.substring(0, 1).equals("H"))
                continue;
            if (str600.substring(0, 1).equals("T")) {
                hRetmFileType = "err";

                tmpstr3 = String.format("%6.6s", str600.substring(1));
                hRecordCnt = comcr.str2int(tmpstr3);
                tmpstr3 = String.format("%6.6s", str600.substring(7));
                hRecordSucc = comcr.str2int(tmpstr3);
                tmpstr3 = String.format("%6.6s", str600.substring(13));
                hRecordFail = comcr.str2int(tmpstr3);

                insertIpsFileResp();

                continue;
            }

            totalCnt++;
            splitBuf3(str600);
            if ((totalCnt % 3000) == 0 || totalCnt == 1)
                showLogMessage("I", "", String.format("Process record[%d]\n", totalCnt));

            if (!str600.substring(0, 1).equals("D")) {
                showLogMessage("I", "", String.format("Line [%d] format error!\n", totalCnt));
                continue;
            }

            hRetdCardIcNo = comc.rtrim(dtl3.cardIcNo);
            hRetdIpsCardNo = comc.rtrim(dtl3.ipsCardNo);
            hRetdErrCode = comc.rtrim(dtl3.errCode);

            insertIpsFileErrdata();

            updateIpsCdrpLog();
        }
        closeInputText(br);
    }

    /***********************************************************************/
    void insertIpsFileResp() throws Exception {
        hTempFileName = tempFileName;

        setValue("send_date", hTnlgFtpSendDate);
        setValue("file_code", hTempFileName.substring(0, 6));
        setValue("file_name", hTempFileName);
        setValue("file_type", hRetmFileType);
        setValue("proc_date", hTnlgFtpReceiveDate);
        setValue("proc_time", hTnlgFtpReceiveTime);
        setValue("ng_err_code", hErrType);
        setValueInt("err_tot_cnt", hRecordCnt);
        setValueInt("err_ok_cnt", hRecordSucc);
        setValueInt("err_err_cnt", hRecordFail);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ips_file_resp";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_file_resp duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void insertIpsFileErrdata() throws Exception {

        hTempFileName = tempFileName;

        setValue("send_date", hTnlgFtpSendDate);
        setValue("file_code", hTempFileName.substring(0, 6));
        setValue("file_name", hTempFileName);
        setValue("card_ic_no", hRetdCardIcNo);
        setValue("ips_card_no", hRetdIpsCardNo);
        setValue("err_code", hRetdErrCode);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ips_file_errdata";
        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_ips_file_errdata duplicate!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateIpsCdrpLog() throws Exception {
        daoTable = "ips_cdrp_log";
        updateSQL = "rpt_resp_date = ?,";
        updateSQL += " rpt_resp_time = ?,";
        updateSQL += " proc_flag  = 'N',";
        updateSQL += " mod_pgm  = ?,";
        updateSQL += " mod_time  = sysdate";
        whereStr = "where ic_card_seqno = ?  ";
        whereStr += "and file_name  = ? ";
        setString(1, hTempRptRespDate);
        setString(2, hTempRptRespTime);
        setString(3, javaProgram);
        setString(4, hRetdCardIcNo);
        setString(5, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_cdrp_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateIpsNotifyLog() throws Exception {
        daoTable   = "ips_notify_log";
        updateSQL  = " check_code = ?,";
        updateSQL += " proc_flag  = '2',";
        updateSQL += " proc_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " proc_time  = to_char(sysdate,'hh24miss'),";
        updateSQL += " mod_pgm    = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr   = "where file_name  = ? ";
        setString(1, hTnlgCheckCode);
        setString(2, javaProgram);
        setString(3, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_notify_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF011 proc = new IpsF011();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String fileDate;
        String fileTime;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(fileDate, 8);
            rtn += comc.fixLeft(fileTime, 6);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl1.fileDate = comc.subMS950String(bytes, 0, 8);
        dtl1.fileTime = comc.subMS950String(bytes, 8, 6);
        dtl1.fillerEnd = comc.subMS950String(bytes, 14, 2);
    }

    /***********************************************************************/
    class Buf2 {
        String fileDate;
        String fileTime;
        String errType;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(fileDate, 8);
            rtn += comc.fixLeft(fileTime, 6);
            rtn += comc.fixLeft(errType, 1);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf2(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl2.fileDate = comc.subMS950String(bytes, 0, 8);
        dtl2.fileTime = comc.subMS950String(bytes, 8, 6);
        dtl2.errType = comc.subMS950String(bytes, 14, 1);
        dtl2.fillerEnd = comc.subMS950String(bytes, 15, 2);
    }

    /***********************************************************************/
    class Buf3 {
        String type;
        String cardIcNo;
        String ipsCardNo;
        String errCode;
        String filler1;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(cardIcNo, 32);
            rtn += comc.fixLeft(ipsCardNo, 11);
            rtn += comc.fixLeft(errCode, 2);
            rtn += comc.fixLeft(filler1, 28);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf3(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl3.type = comc.subMS950String(bytes, 0, 1);
        dtl3.cardIcNo = comc.subMS950String(bytes, 1, 32);
        dtl3.ipsCardNo = comc.subMS950String(bytes, 33, 11);
        dtl3.errCode = comc.subMS950String(bytes, 44, 2);
        dtl3.filler1 = comc.subMS950String(bytes, 46, 28);
        dtl3.fillerEnd = comc.subMS950String(bytes, 74, 2);
    }

}
