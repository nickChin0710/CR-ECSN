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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*關閉自動加值功\能卡號檔(B2I002)產生*/
public class IpsF002 extends AccessDAO {
    private String progname = "關閉自動加值功\\能卡號檔(B2I002)產生  109/12/14 V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hIcdrMediaCreateDate = "";
    String hIcdrMediaCreateTime = "";
    String hIcdrIpsCardNo = "";
    String hIcdrCardNo = "";
    String hIcdrRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";

    String tmpstr1 = "";
    String fileSeq = "";
    int forceFlag = 0;
    int totCnt = 0;
    int hTnlgRecordCnt = 0;

    Buf1 detailSt = new Buf1();
    int out = -1;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IpsF002 [notify_date] [force_flag (Y/N)]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hTempUser = "";
            if (hCallBatchSeqno.length() == 20) {

                comcr.hCallBatchSeqno = hCallBatchSeqno;
                comcr.hCallRProgramCode = javaProgram;

                comcr.callbatch(0, 0, 1);
                sqlCmd = "select user_id ";
                sqlCmd += " from ptr_callbatch  ";
                sqlCmd += "where batch_seqno = ? ";
                setString(1, hCallBatchSeqno);
                int recordCnt = selectTable();
                if (recordCnt > 0) {
                    hTempUser = getValue("user_id");
                }
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            hTnlgNotifyDate = "";
            forceFlag = 0;
            if (args.length == 1) {
                if ((args[0].length() == 1) && (args[0].equals("Y")))
                    forceFlag = 1;
                if (args[0].length() == 8)
                    hTnlgNotifyDate = args[0];
            }
            if (args.length == 2) {
                hTnlgNotifyDate = args[0];
                if (args[1].equals("Y"))
                    forceFlag = 1;
            }
            selectPtrBusinday();
            fileSeq = "01";
            tmpstr1 = String.format("B2I002_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTnlgNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectIpsNotifyLogA() != 0) {
                    String errMsg = String.format("select_ips_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
              updateIpsAutooffLogA();
            }

            fileOpen();

            selectIpsAutooffLog();

            hTnlgRecordCnt = totCnt;

            fileClose();

            showLogMessage("I", "", String.format("Process records = [%d]\n", totCnt));

            // ==============================================
            // 固定要做的
            if (hCallBatchSeqno.length() == 20)
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
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_icdr_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_icdr_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
            hIcdrMediaCreateDate = getValue("h_icdr_media_create_date");
            hIcdrMediaCreateTime = getValue("h_icdr_media_create_time");
        }

    }

    /***********************************************************************/
    int selectIpsNotifyLogA() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd = "select media_crt_date,";
        sqlCmd += "ftp_send_date ";
        sqlCmd += " from ips_notify_log  ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_crt_date");
            hTnlgFtpSendDate = getValue("ftp_send_date");
        }

        if (hTnlgFtpSendDate.length() != 0) {
            String stderr = String.format("通知檔 [%s] 已FTP至IPS , 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            String stderr = String.format("關閉自動加值檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateIpsAutooffLogA() throws Exception {
        daoTable = "ips_autooff_log";
        updateSQL = "proc_flag = 'N'";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_autooff_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        try {
            out = openOutputText(temstr1, "big5");
        } catch (Exception ex) {
            comcr.errRtn(String.format("產生檔案有錯誤[%s]", ex.getMessage()), "", hCallBatchSeqno);
        }

        tmpstr1 = String.format("H%6.6s_%32.32s", "B2I002", " ");

        writeTextFile(out, String.format("%-40.40s\r\n", tmpstr1));
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%06d%33.33s", totCnt, " ");

        writeTextFile(out, String.format("%-40.40s\r\n", tmpstr1));
        closeOutputText(out);
    }

    /***********************************************************************/
    void selectIpsAutooffLog() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "ips_card_no,";
        sqlCmd += "card_no,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from ips_autooff_log a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and (a.proc_flag = '' or a.proc_flag = 'N') ";
        openCursor();
        while (fetchTable()) {
            hIcdrIpsCardNo = getValue("ips_card_no");
            hIcdrCardNo = getValue("card_no");
            hIcdrRowid = getValue("rowid");

            writeRtn();

            updateIpsAutooffLog();

            updateIpsCard();

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));
        }
        closeCursor();
    }

    /***********************************************************************/
    void writeRtn() throws Exception {

        detailSt = new Buf1();
        detailSt.type = "D";

        String tmpstr = String.format("%-11.11s", hIcdrIpsCardNo);
        detailSt.ipsCardNo = tmpstr;

        String buf = comc.fixLeft(detailSt.allText(), 40);
        writeTextFile(out, buf + "\r\n");

        return;
    }

    /***********************************************************************/
    void updateIpsAutooffLog() throws Exception {
        daoTable = "ips_autooff_log";
        updateSQL = "media_crt_date = ?,";
        updateSQL += " media_crt_time = ?,";
        updateSQL += " notify_date  = ?,";
        updateSQL += " send_date   = ?,";
        updateSQL += " file_name   = ?,";
        updateSQL += " proc_flag   = 'Y',";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid    = ? ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgNotifyDate);
        setString(5, hTnlgFileName);
        setString(6, javaProgram);
        setRowId(7, hIcdrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_autooff_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateIpsCard() throws Exception {
        daoTable   = "ips_card";
        updateSQL  = " autoload_from     = decode(autoload_from,'2','2','1')  ,";
        updateSQL += " autoload_flag     = 'N' ,";
        updateSQL += " autoload_clo_date = ?   ,";
        updateSQL += " mod_pgm           = ?   ,";
        updateSQL += " mod_time          = sysdate";
        whereStr = "where ips_card_no    = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setString(3, hIcdrIpsCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF002 proc = new IpsF002();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String ipsCardNo;
        String filler1;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(ipsCardNo, 11);
            rtn += comc.fixLeft(filler1, 28);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.type = comc.subMS950String(bytes, 0, 1);
        detailSt.ipsCardNo = comc.subMS950String(bytes, 1, 11);
        detailSt.filler1 = comc.subMS950String(bytes, 12, 28);
        detailSt.fillerEnd = comc.subMS950String(bytes, 40, 2);
    }

}
