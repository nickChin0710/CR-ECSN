/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  106/11/28  V1.00.01    Brian     error correction                          *
*  107/09/27  V1.00.02   David FU   RECS-1060426-008 悠遊卡存款戶自動加值                               *
*  109-11-18  V1.00.03    tanwei    updated for project coding standard       *
*  112/08/10  V1.00.04    Wilson    換行要0D0A                                  *
*  112/08/12  V1.00.05    Wilson    notify_date改營業日                                                                       *
******************************************************************************/

package Tsc;

import java.io.RandomAccessFile;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;
import com.CommString;

/*會員銀行媒體回覆檔案(RPT)產生程式*/
public class TscT012 extends AccessDAO {

    private String progname = "會員銀行媒體回覆檔案(RPT)產生程式  112/08/12 V1.00.05";
    CommFunction    comm = new CommFunction();
    CommCrd         comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommIps       comips = new CommIps();
    CommString         comStr = new CommString();
    String buf = "";
    String hCallBatchSeqno = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hTempNotifyTime = "";
    String hTnlgFileName = "";
    String hTnlgFileIden = "";
    String hTnlgCheckCode = "";
    String hTnlgRowid = "";
    String hOrgdOrgData = "";
    String hOrgdRptRespCode = "";
    String hTnlgFtpSendDate = "";
    long hTnlgRecordSucc = 0;
    long hTnlgRecordFail = 0;

    int forceFlag = 0;
    int totalCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr2 = "";
    String tmpstr3 = "";
    String fileSeq = "";
    String temstr1 = "";
    String hEriaLocalDir = "";
    RandomAccessFile raf = null;
    private String hTempFileMark = "";

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
                comc.errExit("Usage : TscT012 [[notify_date][force_flag]] [force_flag]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

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

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    comcr.errRtn(String.format("本日會員銀行通知檔媒體已傳送完成, 不可再處理!(error)"), "", hCallBatchSeqno);
                }
            }

            selectTscNotifyLog();

            // ==============================================
            // 固定要做的
            // comcr.callbatch(1, 0, 0);
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
        sqlCmd = "select business_date,";
        sqlCmd += " decode( cast(? as varchar(8)), '', business_date, ?) h_tnlg_notify_date,";
        sqlCmd += " to_char(sysdate,'hh24miss') h_temp_notify_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, hTnlgNotifyDate);
        setString(2, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTnlgNotifyDate = getValue("h_tnlg_notify_date");
            hTempNotifyTime = getValue("h_temp_notify_time");
        }

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        sqlCmd = "select ftp_send_date,";
        sqlCmd += " check_code ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where notify_date = ?  ";
        sqlCmd += " and file_iden   = 'TCRP' ";
        setString(1, hTnlgNotifyDate);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgFtpSendDate = getValue("ftp_send_date");
            hTnlgCheckCode = getValue("check_code");
        } else
            return (0);
        
        if ((hTnlgFtpSendDate.length() == 0) || !(hTnlgCheckCode.equals("0000")))
            return (0);
        
        return (1);
    }

    /***********************************************************************/
    void selectTscNotifyLog() throws Exception {
        sqlCmd = "select file_name,";
        sqlCmd += " file_iden,";
        sqlCmd += " check_code,";
        sqlCmd += " decode(file_iden,'ACCG','1','ACCB','1','ACFI','1','ACAE','1', ";
        sqlCmd += "        'ACAN','1','ACLC','1','ACRT','1','0') as file_mark, ";
        sqlCmd += " rowid rowid ";
        sqlCmd += " from tsc_notify_log ";
        sqlCmd += "where notify_date = ? ";
        sqlCmd += "  and file_name  != '' ";
        sqlCmd += "  and tran_type   = 'O' ";
        setString(1, hTnlgNotifyDate);
        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hTnlgFileName  = getValue("file_name");
            hTnlgFileIden  = getValue("file_iden");
            hTnlgCheckCode = getValue("check_code");
            hTempFileMark  = getValue("file_mark");
            hTnlgRowid      = getValue("rowid");

            showLogMessage("I", "", String.format("處理[%s]回覆檔, 回應代碼[%s]", hTnlgFileName, hTnlgCheckCode));
            hTnlgRecordSucc = hTnlgRecordFail = 0;
            
            if (hTempFileMark.equals("0")) fileOpen();
            
            if (hTnlgCheckCode.equals("0000")) {
                if (hTempFileMark.equals("1"))
                    checkFileRecord();
                else
                    selectTscOrgdataLog();
            }

            if (hTempFileMark.equals("0")) {
                tmpstr1 = String.format("T%08d%406.406s", hTnlgRecordFail, " ");
                tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
                buf = String.format("%-415.415s%16.16s\r\n", tmpstr1, tmpstr2);
                raf.write(buf.getBytes("MS950"));
    
                if (hTnlgCheckCode.equals("0000")) {
                    raf.seek(0);
                    tmpstr1 = String.format("H%4.4s%8.8s%8.8s%6.6s%4.4s%08d%08d%368.368s", hTnlgFileIden,
                            comc.TSCC_BANK_ID8, hTnlgNotifyDate, hTempNotifyTime, hTnlgCheckCode,
                            hTnlgRecordSucc, hTnlgRecordFail, " ");
                    tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
                    buf = String.format("%-415.415s%16.16s\r\n", tmpstr1, tmpstr2);
                    raf.write(buf.getBytes("MS950"));
                }
                raf.close();
            }
            showLogMessage("I", "", String.format("    處理筆數 [%d] 成功\\筆數[%d] 失敗筆數[%d]",
                    hTnlgRecordSucc + hTnlgRecordFail, hTnlgRecordSucc, hTnlgRecordFail));

            updateTscNotifyLog();
            totalCnt++;
        }
        closeCursor(cursorIndex);

    }
    /*******************************************************************/
    private void  checkFileRecord() throws Exception {
        temstr1 = String.format("%s/media/tsc/%s.RPT", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        int fptr2 = openInputText(temstr1,"MS950");
        if (fptr2 == -1) {
            comcr.errRtn(temstr1 + "檔案不存在!!", "", hCallBatchSeqno);
        }

        String str600 = readTextFile(fptr2);
        String tmp = comc.getSubString(str600, 31, 31 + 8);
        hTnlgRecordSucc = comc.str2long(tmp);
        tmp = comc.getSubString(str600, 39, 39 + 8);
        hTnlgRecordFail = comc.str2long(tmp);
        hTnlgCheckCode = comc.getSubString(str600, 27, 27 + 4);
        closeInputText(fptr2);
    }
    
    /***********************************************************************/
    void fileOpen() throws Exception {
        tmpstr1 = String.format("%s.RPT", hTnlgFileName);
        temstr1 = String.format("%s/media/tsc/%s", comc.getECSHOME(), tmpstr1);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        raf = new RandomAccessFile(temstr1, "rw");
        tmpstr1 = String.format("H%4.4s%8.8s%8.8s%6.6s%4.4s%08d%08d%368.368s",hTnlgFileIden,comc.TSCC_BANK_ID8
                , hTnlgNotifyDate, hTempNotifyTime, hTnlgCheckCode, 0, 0, " ");
        tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
        buf = String.format("%-415.415s%16.16s\r\n", tmpstr1, tmpstr2);
        raf.write(buf.getBytes("MS950"));
    }

    /***********************************************************************/
    void selectTscOrgdataLog() throws Exception {
        sqlCmd = "select org_data,";
        sqlCmd += " rpt_resp_code ";
        sqlCmd += " from tsc_orgdata_log ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hOrgdOrgData = getValue("org_data", i);
            hOrgdRptRespCode = getValue("rpt_resp_code", i);
            if (hOrgdRptRespCode.equals("0000")) {
                hTnlgRecordSucc++;
            } else {
                hTnlgRecordFail++;
                tmpstr1 = String.format("D01%4.4s%s", hOrgdRptRespCode, comc.fixLeft(hOrgdOrgData, 408));
                tmpstr2 = new String(comips.commHashUnpack(tmpstr1.getBytes("MS950")));
                buf = String.format("%s%16.16s\r\n", comc.fixLeft(tmpstr1, 415), tmpstr2);
                raf.write(buf.getBytes("MS950"));
            }
        }
    }
    /***********************************************************************/
    void updateTscNotifyLog() throws Exception {
        daoTable   = " tsc_notify_log";
        updateSQL  = " media_crt_date  = to_char(sysdate, 'yyyymmdd'),";
        updateSQL += " media_crt_time  = to_char(sysdate, 'hh24miss'),";
        updateSQL += " record_succ     = ?,";
        updateSQL += " record_fail     = ?,";
        updateSQL += " resp_code       = ?,";
        updateSQL += " mod_pgm         = 'TscT012',";
        updateSQL += " mod_time        = sysdate";
        whereStr   = " where rowid     = ? ";
        setLong(1, hTnlgRecordSucc);
        setLong(2, hTnlgRecordFail);
        setString(3, hTnlgCheckCode);
        setRowId(4, hTnlgRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_notify_log not found!", "", hCallBatchSeqno);
        }
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscT012 proc = new TscT012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
