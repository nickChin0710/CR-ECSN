/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  108/01/08  V1.01.00   Brian      program initial                           *
 *  109/12/16  V1.01.01   tanwei     updated for project coding standard       *
 ******************************************************************************/

package Ich;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;


public class IchF017 extends AccessDAO {
    private String progname = "卡號資料檔(B07B)媒體處理  109/12/16 V1.01.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hTnlgNotifyDate = "";
    String hPreNotifyDate = "";
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

    String hRetdIchCardNo  = "";
    String hRtnCode     = "";

    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String swRtn = "";
    String tempFileName = "";
    int hTnlgRecordCnt = 0;
    int totCnt = 0;
    int errCnt = 0;

    Buf1 dtl1 = new Buf1();

    public int mainProcess(String[] args)
    {
        try
        {
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : IchF017 [notify_date [force_flag]]", "");
            }

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
            showLogMessage("I", "", String.format("處理ICH 檔案日期[%s][%s]", hTnlgNotifyDate
                    , hTnlgProcFlag));

            selectIchNotifyLog();
            selectIchNotifyLogReceive();  //將本日收到的檔案搬到backup

            showLogMessage("I", "", "程式執行結束,筆數=[" + totCnt + "]");

            finalProcess();
            return 0;
        } catch (Exception ex)
        {
            expMethod = "mainProcess"; expHandle(ex); return exceptExit;
        }
    }
    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
    	hBusiBusinessDate = "";
        sqlCmd  = "select business_date ";
        sqlCmd += " from ptr_businday  ";

        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
        
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hPreNotifyDate = comm.nextNDate(hTnlgNotifyDate, -1);
        
    }
    /***********************************************************************/
    void selectIchNotifyLog() throws Exception
    {
        /* proc_flag = 0:收檔中 1: 已收檔 2: 已處理 3: 已回應 */

        sqlCmd  = "select ";
        sqlCmd += "file_name,";
        sqlCmd += "file_iden,";
        sqlCmd += "ftp_send_date,";
        sqlCmd += "ftp_send_time,";
        sqlCmd += "ftp_receive_date,";
        sqlCmd += "ftp_receive_time,";
        sqlCmd += "check_code,";
        sqlCmd += "record_succ,";
        sqlCmd += "record_fail,";
        sqlCmd += "rowid as rowid1 ";
        sqlCmd += "from ich_notify_log ";
        sqlCmd += "where proc_flag   = decode(cast(? as varchar(1))  ,'Y',proc_flag   ,'1') ";
        sqlCmd += "  and file_iden   = 'B07B' ";
        sqlCmd += "  and notify_date = decode(cast(? as varchar(10)) , '', notify_date,?) ";
        sqlCmd += " order by notify_date ";
        setString(1, hTnlgProcFlag);
        setString(2, hPreNotifyDate);
        setString(3, hPreNotifyDate);
        openCursor();
        while (fetchTable()) {
            hTnlgFileName        = getValue("file_name");
            hTnlgFileIden        = getValue("file_iden");
            hTnlgFtpSendDate    = getValue("ftp_send_date");
            hTnlgFtpSendTime    = getValue("ftp_send_time");
            hTnlgFtpReceiveDate = getValue("ftp_receive_date");
            hTnlgFtpReceiveTime = getValue("ftp_receive_time");
            hTnlgCheckCode       = getValue("check_code");
            hTnlgRecordSucc      = getValue("record_succ");
            hTnlgRecordFail      = getValue("record_fail");
            hTnlgRowid            = getValue("rowid1");

           if (!hTnlgCheckCode.equals("0000") && !hTnlgCheckCode.equals("0")) {
                updateIchNotifyLog();
                String stderr = String.format("[%s] 整檔處理失敗, 錯誤代碼[%s](error)!",hTnlgFileName
                        , hTnlgCheckCode);
                showLogMessage("I", "", stderr);
                continue;
            }

            fileOpen1();
            if (swRtn.equals("N")) {
                String stderr = String.format("找不到回覆檔, 請通知相關人員處理(error)");
                showLogMessage("I", "", stderr);
                continue;
            }

            hTnlgCheckCode = "0000";
            updateIchNotifyLog();

            String root = String.format("%s/media/ich", comc.getECSHOME());

            root = Normalizer.normalize(root, java.text.Normalizer.Form.NFKD);

        }
        closeCursor();
    }
    
    /***********************************************************************/
    void selectIchNotifyLogReceive() throws Exception
    {
        /* proc_flag = 0:收檔中 1: 已收檔 2: 已處理 3: 已回應 */

        sqlCmd  = "select ";
        sqlCmd += "file_name,";
        sqlCmd += "file_iden,";
        sqlCmd += "check_code,";
        sqlCmd += "rowid as rowid1 ";
        sqlCmd += "from ich_notify_log ";
        sqlCmd += "where file_iden   = 'B07B' ";
        sqlCmd += "  and notify_date = ? ";
        sqlCmd += "  and tran_type = 'O' ";
        setString(1, hTnlgNotifyDate);

        openCursor();
        while (fetchTable()) {
            hTnlgFileName        = getValue("file_name");
            hTnlgFileIden        = getValue("file_iden");
            hTnlgCheckCode       = getValue("check_code");
            hTnlgRowid           = getValue("rowid1");

            hTnlgCheckCode = "0000";
            updateIchNotifyLog();
            
            moveBackup(hTnlgFileName);

        }
        closeCursor();
    }
    
    /***********************************************************************/
    @SuppressWarnings("resource")
    int fileOpen1() throws Exception
    {

        errCnt = 0;
        int intb = 0, headTag = 0, tailTag = 0;
        String str600 = "";
        String stra   = "";

        hTnlgRecordCnt = 0;


// 1. xRQx/xRPx,xxxQ/xxxR  ex. BRQA, BRPA      2. FC, FB
        if(hTnlgFileName.substring(1,3).equals("RQ") )
        {
            tempFileName = hTnlgFileName.substring(0,1)+"RP"+
                    hTnlgFileName.substring(3, 3+6 ) + hTnlgNotifyDate + hTnlgFileName.substring(17);
        }
        else if(hTnlgFileName.substring(0,2).equals("FC") )
        {
            tempFileName = "FB"+hTnlgFileName.substring(2, 2 + 11) + hTnlgNotifyDate;
        }
        tempFileName = String.format("%s", tempFileName);
        String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), tempFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        swRtn = "Y";
        int f = openInputText(temstr1);
        if (f == -1) {
            swRtn = "N";
        } else
            closeInputText(f);

        int br = openInputText(temstr1, "MS950");
        while (true) {
            str600 = readTextFile(br);
            if (endFile[br].equals("Y")) break;
            hTnlgCheckCode = "";
            totCnt++;

            if (str600.substring(0, 1).equals("D"))     intb++;
            if (str600.substring(0, 1).equals("H"))
            { continue;
            }

            splitBuf1(str600);

            stra = dtl1.ichCardNo;
            hRetdIchCardNo  = stra.trim();

            stra = dtl1.rtnCode;
            hRtnCode        = stra.trim();
            if(hRtnCode.equals("1"))
                errCnt++;

            updateIchB07bCard();
        }

        closeInputText(br);

        return (0);
    }
    /***********************************************************************/
    void updateIchB07bCard() throws Exception {
        daoTable   = "ich_b07b_card";
        updateSQL  = " rpt_resp_date = ?,";
        updateSQL += " rpt_resp_time = ?,";
        updateSQL += " rpt_resp_code = ?,";
//      updateSQL += " proc_flag     = 'N',";
        updateSQL += " mod_pgm       = ?,";
        updateSQL += " mod_time      = sysdate";
        whereStr   = "where ich_card_no   = ?  ";
        whereStr  += "  and file_name   = ? ";
        setString(1, hTempRptRespDate);
        setString(2, hTempRptRespTime);
        setString(3, hRtnCode          );
        setString(4, javaProgram);
        setString(5, hRetdIchCardNo );
        setString(6, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ich_b07b_card not found!", hRetdIchCardNo
                    ,comcr.hCallBatchSeqno);
        }

    }
    /***********************************************************************/
    void updateIchNotifyLog() throws Exception {
        daoTable   = "ich_notify_log";
        updateSQL  = "check_code  = ?,";
        updateSQL += " record_fail  = ?,";
        updateSQL += " resp_code  = '0000',";
        updateSQL += " proc_flag  = '2',";
        updateSQL += " proc_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " proc_time  = to_char(sysdate,'hh24miss'),";
        updateSQL += " mod_pgm    = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr   = "where file_name  = ? ";
        setString(1, hTnlgCheckCode);
        setInt(2, errCnt);
        setString(3, javaProgram);
        setString(4, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ich_notify_log not found!", "",comcr.hCallBatchSeqno);
        }

    }
    
    /***********************************************************************/
    void moveBackup(String moveFile) throws Exception {
        String root = String.format("%s/media/ich", comc.getECSHOME());
        String src = String.format("%s/%s", root, moveFile);
        String target = String.format("%s/backup/%s/%s", root, hTnlgNotifyDate, moveFile);

        comc.fileRename(src, target);
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception
    {
        IchF017 proc = new IchF017();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
    class Buf1 {
        String type;
        String ichCardNo;
        String fileDate;
        String fileTime;
        String filler1;
        String rtnCode;
        String fillerEnd;
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        dtl1.type       = comc.subMS950String(bytes,  0,  1);
        dtl1.ichCardNo  = comc.subMS950String(bytes,  1,  16);
        dtl1.fileDate  = comc.subMS950String(bytes, 17,  8);
        dtl1.fileTime  = comc.subMS950String(bytes, 25,  6);
        dtl1.filler1   = comc.subMS950String(bytes, 31, 30);
        dtl1.rtnCode   = comc.subMS950String(bytes, 61,  1);
        dtl1.fillerEnd = comc.subMS950String(bytes, 62,  2);
    }
/***********************************************************************/

}
