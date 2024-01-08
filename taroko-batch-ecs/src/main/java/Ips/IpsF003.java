/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-14  V1.00.01    tanwei      updated for project coding standard     *
*  112-05-18  V1.00.02    Alex      壓縮檔案並產生OK檔後FTP到指定路徑                                           *
*  112-05-24  V1.00.03    Alex      FTP 傳送參數變更                                                                            *
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
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*要求列入黑名單功\能檔(B2I003)產生*/
public class IpsF003 extends AccessDAO {
    private String progname = "要求列入黑名單功能檔(B2I003)產生  112/05/24 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;
    CommRoutine comr = null;
    
    String hCallBatchSeqno = "";

    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hIcdrMediaCreateDate = "";
    String hIcdrMediaCreateTime = "";
    String hMIcdrIpsCardNo = "";
    String hMIcdrRowid = "";
    String hIcdrIpsCardNo = "";
    String hTnlgFileName = "";
    String hIcdrRowid = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";

    String tmpstr = "";    
    String filePath = String.format("%s/media/ips/", comc.getECSHOME());
    String fileSeq = "";
    String tempPrevDate = "";
    String tempDate1 = "";
    String tempDate2 = "";
    String tempDate3 = "";
    String tmpstr1 = "";
    int tmpInt = 0;
    int nRetcode = 0;
    int forceFlag = 0;
    int totalCnt = 0;
    int succCnt = 0;
    int totCnt = 0;
    int hTnlgRecordCnt = 0;

    int out = -1;
    Buf1 detailSt = new Buf1();

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
                comc.errExit("Usage : IpsF003 [notify_date] [force_flag (Y/N)]", "");
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
            tmpstr1 = String.format("B2I003_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTnlgNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;                        
            if (forceFlag == 0) {
                if (selectIpsNotifyLogA() != 0) {
                    String errMsg = String.format("select_ips_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateIpsB2i003LogA();
            }

            fileOpen();

            selectIpsB2i003Log();

            hTnlgRecordCnt = totCnt;

            fileClose();
            
//            commFTP = new CommFTP(getDBconnect(), getDBalias());
//            comr = new CommRoutine(getDBconnect(), getDBalias());
            
//            moveFile();
           
            
            
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
    void moveFile() throws Exception {    	    	
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "IPS_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/ips", comc.getECSHOME());
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;

        // System.setProperty("user.dir",commFTP.h_eria_local_dir);
        showLogMessage("I", "", "mput " + hTnlgFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("IPS_FTP_PUT", "mput " + hTnlgFileName);

        if (errCode != 0) {
           showLogMessage("I", "", "ERROR:無法傳送 " + hTnlgFileName + " 資料" + " errcode:" + errCode);
           insertEcsNotifyLog(hTnlgFileName);
        }
        
        //--備份
        moveBackup(hTnlgFileName);               
    }
    
    /***********************************************************************/
    public int insertEcsNotifyLog(String fileName) throws Exception {    
    	setValue("crt_date", sysDate);
    	setValue("crt_time", sysTime);
    	setValue("unit_code", comr.getObjectOwner("3", javaProgram));
    	setValue("obj_type", "3");
    	setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
    	setValue("notify_name", "媒體檔名:" + fileName);
    	setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
    	setValue("notify_desc2", "");
    	setValue("trans_seqno", commFTP.hEflgTransSeqno);
    	setValue("mod_time", sysDate + sysTime);
    	setValue("mod_pgm", javaProgram);
    	daoTable = "ecs_notify_log";
    	insertTable();
    	return (0);
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
            String stderr = String.format("通知檔 [%s] 已FTP至IPS , 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            String stderr = String.format("關閉自動加值檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateIpsB2i003LogA() throws Exception {
        daoTable = "ips_b2i003_log";
        updateSQL = "proc_flag = 'N'";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_b2i003_log not found!", "", hCallBatchSeqno);
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

        tmpstr1 = String.format("H%6.6s_%32.32s", "B2I003", " ");

        writeTextFile(out, String.format("%-40.40s\r\n", tmpstr1));
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%06d%33.33s", totCnt, " ");

        writeTextFile(out, String.format("%-40.40s\r\n", tmpstr1));
        closeOutputText(out);
    }

    /***********************************************************************/
    void selectIpsB2i003Log() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "ips_card_no,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from ips_b2i003_log a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and (a.proc_flag = '' or a.proc_flag = 'N') ";
        openCursor();
        while (fetchTable()) {
            hIcdrIpsCardNo = getValue("ips_card_no");
            hIcdrRowid = getValue("rowid");

            writeRtn();

            updateIpsB2i003Log();

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

        tmpstr = String.format("%-11.11s", hIcdrIpsCardNo);
        detailSt.ipsCardNo = tmpstr;

        String buf = comc.fixLeft(detailSt.allText(), 40);
        writeTextFile(out, buf + "\r\n");

        return;
    }

    /***********************************************************************/
    void updateIpsB2i003Log() throws Exception {
        daoTable   = "ips_b2i003_log";
        updateSQL  = " media_create_date = ?,";
        updateSQL += " media_create_time = ?,";
        updateSQL += " notify_date       = ?,";
        updateSQL += " file_name         = ?,";
        updateSQL += " proc_flag         = 'Y',";
        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where rowid        = ? ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hIcdrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_b2i003_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void updateIpsCard() throws Exception {
        daoTable = "ips_card";
        updateSQL = "blacklt_flag  = 'Y' ,";
        updateSQL += " blacklt_date  = ? ,";
        updateSQL += " mod_pgm   = ? ,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where ips_card_no  = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setString(3, hIcdrIpsCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception	{
        String src    = String.format("%s/%s", filePath, moveFile);
        //String target = String.format("%s/BACKUP/%s/%s.BAK", root, h_tnlg_notify_date, move_file);
        String target = String.format("%s/backup/%s", filePath, moveFile);
        
        comc.fileMove(src, target);
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF003 proc = new IpsF003();
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
