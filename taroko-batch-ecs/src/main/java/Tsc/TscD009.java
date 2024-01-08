/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/11  V1.00.00    Wendy Lu                     program initial        *
*  112/12/19  V1.00.01    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;


public class TscD009 extends AccessDAO {
    private final String progname = "悠遊VD卡自動加值開啟回饋檔(DCAE)媒體產生程式   112/12/19 V1.00.01";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;
    CommIps        comips   = new CommIps();

    String hCallBatchSeqno = "";
    int forceFlag = 0;
    String hTnlgNotifyDate = "";
    String hTnlgFileName = "";
    String tmpstr1 = "";
    int hTnlgRecordCnt = 0;
    int totCnt = 0;
    int totalCnt = 0;
    String hBusiBusinessDate = "";
    String hTbigMediaCreateDate = "";
    String hTbigMediaCreateTime = "";
    String hDcaeTscCardNo = "";
    String hDcaeCardNo = "";
    String hDcaeVendorEmbossDate = "";
    String hDcaeIcSeqNo = "";
    String hDcaeBirthday = "";
    String hDcaeRowid = "";
    String tmpstr = "";
    String hTempX16 = "";
    String tempX04 = "";
    String tmpstr2 = "";
    String temstr1 = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    
    int out = -1;

    Buf1 detailSt = new Buf1();

    public int mainProcess(String[] args) throws Exception {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscD009 [notify_date] [force_flag (Y/N)]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 8).equals(comc.getSubString(checkHome, 0, 8))) {
                comcr.hCallBatchSeqno = "no-call";
            }
            comcr.hCallRProgramCode = javaProgram;
            String hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = "user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "where batch_seqno = ? ";

                setString(1, comcr.hCallBatchSeqno);
                if (selectTable() > 0)
                    hTempUser = getValue("user_id");
            }

            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8) {
                    String sgArgs0 = "";
                    sgArgs0 = args[0];
                    sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                    hTnlgNotifyDate = sgArgs0;
                }
            }
            if (args.length == 2) {
                String sgArgs0 = "";
                sgArgs0 = args[0];
                sgArgs0 = Normalizer.normalize(sgArgs0, java.text.Normalizer.Form.NFKD);
                hTnlgNotifyDate = sgArgs0;
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();
            tmpstr1 = String.format("DCAE.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscDcaeLoga();
            }

            fileOpen();

            selectTscDcaeLog();

            hTnlgRecordCnt = totalCnt;

            fileClose();

            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            showLogMessage("I", "", comcr.hCallErrorDesc);
            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 1);
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
        sqlCmd = "select to_char(add_days(to_date(business_date,'yyyymmdd'),1),'yyyymmdd') h_business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_tbig_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_tbig_media_create_time ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hTbigMediaCreateDate = getValue("h_tbig_media_create_date");
            hTbigMediaCreateTime = getValue("h_tbig_media_create_time");
        }

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd = "select media_crt_date, ";
        sqlCmd += "ftp_send_date ";
        sqlCmd += "from tsc_notify_log ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_create_date");
            hTnlgFtpSendDate = getValue("ftp_send_date");
        } else
            return (0);

        if (hTnlgFtpSendDate.length() != 0) {
            showLogMessage("I", "", String.format("通知檔 [%s] 已FTP至TSCC, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName));
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            showLogMessage("I", "", String.format("自動加值回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscDcaeLoga() throws Exception {
        daoTable = "tsc_dcae_log ";
        updateSQL = "proc_flag = 'N'";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcae_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        comc.mkdirsFromFilenameWithPath(temstr1);
        out = openOutputText(temstr1, "big5");
        if (out == -1) {
            comcr.errRtn(String.format("產生檔案有錯誤[%s]", temstr1), "", hCallBatchSeqno);
        }
        tmpstr1 = String.format("HDCAE%8.8s%8.8s%6.6s%61.61s", comc.TSCC_BANK_ID8, hTbigMediaCreateDate, hTbigMediaCreateTime, " ");

        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        String buf = String.format("%-88.88s%-16.16s\r\n", tmpstr1, tmpstr2);
        writeTextFile(out, buf);
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%08d%79.79s", totCnt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        writeTextFile(out, String.format("%-88.88s%16.16s\r\n", tmpstr1, new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }

    }

    /***********************************************************************/
    void selectTscDcaeLog() throws Exception {

        sqlCmd = "select a.tsc_card_no, ";
        sqlCmd += "a.card_no, ";
        sqlCmd += "a.vendor_emboss_date, ";
        sqlCmd += "a.ic_seq_no, ";
        sqlCmd += "a.birthday, ";
        sqlCmd += "a.rowid rowid ";
        sqlCmd += "from tsc_vd_card b,tsc_dcae_log a ";
        sqlCmd += "where a.tsc_card_no = b.tsc_card_no ";
        sqlCmd += "and a.proc_flag = 'N' ";
        sqlCmd += "and b.autoload_flag = 'Y' ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hDcaeTscCardNo = getValue("tsc_card_no", i);
            hDcaeCardNo = getValue("card_no", i);
            hDcaeVendorEmbossDate = getValue("vendor_emboss_date", i);
            hDcaeIcSeqNo = getValue("ic_seq_no", i);
            hDcaeBirthday = getValue("birthday", i);
            hDcaeRowid = getValue("rowid", i);

            writeRtn();

            updateTscDcaeLog();

            totalCnt++;
            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]", totCnt));
        }

    }

    /***********************************************************************/
    void writeRtn() throws Exception {
        byte[] endflag = new byte[2];
        endflag[0] = 0x0D;
        endflag[1] = 0x0A;
        
        detailSt = new Buf1();
        detailSt.type = "D";
        detailSt.attri = "01";


        tmpstr = String.format("%-20.20s", hDcaeTscCardNo);
        detailSt.tscCashNo = tmpstr;

        hTempX16 = hDcaeTscCardNo;
        tempX04 = "";
        sqlCmd = "select substr(new_end_date,3,4) temp_x04 ";
        sqlCmd += "from tsc_vd_card ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempX16);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            tempX04 = getValue("temp_x04");
        }

        tmpstr = String.format("%-4.4s", tempX04);
        detailSt.effcYymm = tmpstr;
        
        tmpstr = String.format("%-20.20s", hDcaeIcSeqNo);
        detailSt.chipSeq = tmpstr;

        tmpstr = String.format("%-20.20s", hDcaeTscCardNo);
        detailSt.tscCardNo = tmpstr;
        

        tmpstr = String.format("%-8.8s", hDcaeVendorEmbossDate);
        detailSt.embossDate = tmpstr;

        tmpstr = String.format("%-8.8s", hDcaeBirthday);
        detailSt.birthday = tmpstr;

        tmpstr1 = String.format("%-88.88s", detailSt.allText());
        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        tmpstr = String.format("%-16.16s", tmpstr2);
        detailSt.hashValue = tmpstr;

        tmpstr = String.format("%-2.2s", new String(endflag)); //換行
        detailSt.fillerEnd = tmpstr;

        String buf = detailSt.allText();
        writeTextFile(out, buf);

        return;
    }

    /***********************************************************************/
    void updateTscDcaeLog() throws Exception {
        daoTable = "tsc_dcae_log ";
        updateSQL  = "media_create_date = ?,";
        updateSQL += "media_create_time = ?,";
        updateSQL += "notify_date = ?,";
        updateSQL += "file_name = ?,";
        updateSQL += "proc_flag = 'Y',";
        updateSQL += "mod_pgm = ?,";
        updateSQL += "mod_time = sysdate";
        whereStr = "where rowid = ? ";
        setString(1, hTbigMediaCreateDate);
        setString(2, hTbigMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hDcaeRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcae_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscD009 proc = new TscD009();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String tscCashNo;
        String effcYymm;
        String chipSeq;
        String tscCardNo;
        String embossDate;
        String birthday;
        String filler1i;
        String hashValue;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(tscCashNo, 20);
            rtn += comc.fixLeft(effcYymm, 4);
            rtn += comc.fixLeft(chipSeq, 20);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(embossDate, 8);
            rtn += comc.fixLeft(birthday, 8);
            rtn += comc.fixLeft(filler1i, 5);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.type = comc.subMS950String(bytes, 0, 1);
        detailSt.attri = comc.subMS950String(bytes, 1, 2);
        detailSt.tscCashNo = comc.subMS950String(bytes, 3, 20);
        detailSt.effcYymm = comc.subMS950String(bytes, 23, 4);
        detailSt.chipSeq = comc.subMS950String(bytes, 27, 20);
        detailSt.tscCardNo = comc.subMS950String(bytes, 47, 20);
        detailSt.embossDate = comc.subMS950String(bytes, 67, 8);
        detailSt.birthday = comc.subMS950String(bytes, 75, 8);
        detailSt.filler1i = comc.subMS950String(bytes, 83, 5);
        detailSt.hashValue = comc.subMS950String(bytes, 88, 16);
        detailSt.fillerEnd = comc.subMS950String(bytes, 104, 2);
    }

}
