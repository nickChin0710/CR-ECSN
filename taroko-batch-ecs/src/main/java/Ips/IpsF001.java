/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-14  V1.00.01    tanwei      updated for project coding standard     *
*  112/05/12  V1.00.02    Wilson    調整變數宣告方式                                                                                      *
******************************************************************************/

package Ips;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*聯名卡卡號暨錄碼資料彙總檔(B2I001)產生*/
public class IpsF001 extends AccessDAO {
    private String progname = "聯名卡卡號暨錄碼資料彙總檔(B2I001)產生  112/05/12 V1.00.02";
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
    String hIcdrIcCardSeqno = "";
    String hIcdrIssueCode = "";
    String hIcdrRwId = "";
    String hIcdrRwTime = "";
    String hIcdrEffcTime = "";
    String hIcdrRwStatus = "";
    String hIcdrAutopayFlag = "";
    String hIcdrMaxAutopay = "";
    String hIcdrMaxAmt  = "";
    String hIcdrMaxAmtM = "";
    String hIcdrPersonalType = "";
    String hIcdrExpiryTime = "";
    String hIcdrPersonalId = "";
    String hIcdrPersonalName = "";
    String hIcdrBankId = "";
    String hIcdrTicketType = "";
    String hIcdrAreaCode = "";
    String hIcdrIpsTime = "";
    String hIcdrIpsCumTime = "";
    int hIcdrAddPointTot = 0;
    String hIcdrS8TicketType = "";
    String hIcdrS8Unit = "";
    String hIcdrS8Sid = "";
    String hIcdrS8Amt = "";
    String hIcdrLocId = "";
    String hIcdrMentuuId = "";
    String hIcdrRwIsstc = "";
    String hIcdrTac = "";
    String hIcdrTacR = "";
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
                comc.errExit("Usage : IpsF001 [notify_date] [force_flag (Y/N)]", "");
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
            tmpstr1 = String.format("B2I001_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTnlgNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectIpsNotifyLogA() != 0) {
                    String errMsg = String.format("select_ips_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateIpsCdrpLogA();
            }

            fileOpen();

            selectIpsCdrpLog();

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
            String stderr = String.format("通知檔 [%s] 已FTP至IPS , 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            String stderr = String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateIpsCdrpLogA() throws Exception {
        daoTable = "ips_cdrp_log";
        updateSQL = "proc_flag = 'N'";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_cdrp_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String temstr1 = String.format("%s/media/ips/%s", comc.getECSHOME(), hTnlgFileName);
        temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        try {
            comc.mkdirsFromFilenameWithPath(temstr1);
            out = openOutputText(temstr1, "big5");
        } catch (Exception ex) {
            comcr.errRtn(String.format("產生檔案有錯誤[%s]", ex.getMessage()), "", hCallBatchSeqno);
        }

        tmpstr1 = String.format("H%6.6s_%243.243s", "B2I001", " ");

        writeTextFile(out, String.format("%-251.251s\r\n", tmpstr1));
    }

    /***********************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%06d%244.244s", totCnt, " ");

        writeTextFile(out, String.format("%-251.251s\r\n", tmpstr1));
        closeOutputText(out);

    }

    /***********************************************************************/
    void selectIpsCdrpLog() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "ips_card_no,";
        sqlCmd += "card_no,";
        sqlCmd += "ic_card_seqno,";
        sqlCmd += "issue_code,";
        sqlCmd += "rw_id,";
        sqlCmd += "rw_time,";
        sqlCmd += "effc_time,";
        sqlCmd += "rw_status,";
        sqlCmd += "autopay_flag,";
        sqlCmd += "max_autopay,";
        sqlCmd += "max_amt,";
        sqlCmd += "max_amt_m,";
        sqlCmd += "personal_type,";
        sqlCmd += "expiry_time,";
        sqlCmd += "personal_id,";
        sqlCmd += "personal_name,";
        sqlCmd += "bank_id,";
        sqlCmd += "ticket_type,";
        sqlCmd += "area_code,";
        sqlCmd += "ips_time,";
        sqlCmd += "ips_cum_time,";
        sqlCmd += "add_point_tot,";
        sqlCmd += "s8_ticket_type,";
        sqlCmd += "s8_unit,";
        sqlCmd += "s8_sid,";
        sqlCmd += "s8_amt,";
        sqlCmd += "loc_id,";
        sqlCmd += "mentuu_id,";
        sqlCmd += "rw_isstc,";
        sqlCmd += "tac,";
        sqlCmd += "tac_r,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += " from ips_cdrp_log a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and (a.proc_flag = 'N' or a.proc_flag = '') ";
        sqlCmd += "  and a.vendor_date_rtn <> '' ";
        openCursor();
        while (fetchTable()) {
            hIcdrIpsCardNo    = getValue("ips_card_no");
            hIcdrCardNo        = getValue("card_no");
            hIcdrIcCardSeqno  = getValue("ic_card_seqno");
            hIcdrIssueCode     = getValue("issue_code");
            hIcdrRwId          = getValue("rw_id");
            hIcdrRwTime        = getValue("rw_time");
            hIcdrEffcTime      = getValue("effc_time");
            hIcdrRwStatus      = getValue("rw_status");
            hIcdrAutopayFlag   = getValue("autopay_flag");
            hIcdrMaxAutopay    = getValue("max_autopay");
            hIcdrMaxAmt        = getValue("max_amt");
            hIcdrMaxAmtM       = getValue("max_amt_m");
            hIcdrPersonalType  = getValue("personal_type");
            hIcdrExpiryTime    = getValue("expiry_time");
            hIcdrPersonalId    = getValue("personal_id");
            hIcdrPersonalName  = getValue("personal_name");
            hIcdrBankId        = getValue("bank_id");
            hIcdrTicketType    = getValue("ticket_type");
            hIcdrAreaCode      = getValue("area_code");
            hIcdrIpsTime       = getValue("ips_time");
            hIcdrIpsCumTime   = getValue("ips_cum_time");
            hIcdrAddPointTot  = getValueInt("add_point_tot");
            hIcdrS8TicketType = getValue("s8_ticket_type");
            hIcdrS8Unit        = getValue("s8_unit");
            hIcdrS8Sid         = getValue("s8_sid");
            hIcdrS8Amt         = getValue("s8_amt");
            hIcdrLocId         = getValue("loc_id");
            hIcdrMentuuId      = getValue("mentuu_id");
            hIcdrRwIsstc       = getValue("rw_isstc");
            hIcdrTac            = getValue("tac");
            hIcdrTacR          = getValue("tac_r");
            hIcdrRowid          = getValue("rowid");

            writeRtn();

            updateIpsCdrpLog();

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));
        }
        closeCursor();
    }

    /***********************************************************************/
    void writeRtn() throws Exception {
        String tmpstr = "";

        detailSt = new Buf1();

        detailSt.type = "D";

        tmpstr = String.format("%-32.32s", hIcdrIcCardSeqno);
        detailSt.icCardSeqno = tmpstr;

        tmpstr = String.format("%-11.11s", hIcdrIpsCardNo);
        detailSt.ipsCardNo = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrIssueCode);
        detailSt.issueCode = tmpstr;

        tmpstr = String.format("%-8.8s", hIcdrRwId);
        detailSt.rwId = tmpstr;

        tmpstr = String.format("%-14.14s", hIcdrRwTime);
        detailSt.rwTime = tmpstr;

        tmpstr = String.format("%-14.14s", hIcdrEffcTime);
        detailSt.effcTime = tmpstr;

        tmpstr = String.format("%2.2s", "02");
        detailSt.version = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrRwStatus);
        detailSt.rwStatus = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrAutopayFlag);
        detailSt.autopayFlag = tmpstr;

        tmpstr = String.format("%4.4s", hIcdrMaxAutopay);
        detailSt.maxAutopay = tmpstr;

        tmpstr = String.format("%4.4s", "2710");
        detailSt.maxAmt = tmpstr;

        tmpstr = String.format("%4.4s", "01F4");
        detailSt.maxAmtM = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrPersonalType);
        detailSt.personalType = tmpstr;

        tmpstr = String.format("%-14.14s", hIcdrExpiryTime);
        detailSt.expiryTime = tmpstr;

        tmpstr = String.format("%-12.12s", hIcdrPersonalId);
        detailSt.personalId = tmpstr;

        tmpstr = String.format("%-16.16s", hIcdrPersonalName);
        detailSt.personalName = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrBankId);
        detailSt.bankId = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrTicketType);
        detailSt.ticketType = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrAreaCode);
        detailSt.areaCode = tmpstr;

        tmpstr = String.format("%-14.14s", hIcdrIpsTime);
        detailSt.ipsTime = tmpstr;

        tmpstr = String.format("%06d", hIcdrAddPointTot);
        detailSt.addPointTot = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrS8TicketType);
        detailSt.s8TicketType = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrS8Unit);
        detailSt.s8Unit = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrS8Sid);
        detailSt.s8Sid = tmpstr;

        tmpstr = String.format("%4.4s", hIcdrS8Amt);
        detailSt.s8Amt = tmpstr;

        tmpstr = String.format("%2.2s", hIcdrLocId);
        detailSt.locId = tmpstr;

        tmpstr = String.format("%10.10s", hIcdrMentuuId);
        detailSt.mentuuId = tmpstr;

        tmpstr = String.format("%8.8s", hIcdrRwIsstc);
        detailSt.rwIsstc = tmpstr;

        tmpstr = String.format("%8.8s", hIcdrTac);
        detailSt.tac = tmpstr;

        tmpstr = String.format("%1.1s", hIcdrTacR);
        detailSt.tacR = tmpstr;

        String buf = comc.fixLeft(detailSt.allText(), 251);
        writeTextFile(out, buf + "\r\n");

        return;
    }

    /***********************************************************************/
    void updateIpsCdrpLog() throws Exception {
        daoTable   = "ips_cdrp_log";
        updateSQL  = " media_crt_date = ?,";
        updateSQL += " media_crt_time = ?,";
        updateSQL += " notify_date    = ?,";
        updateSQL += " file_name      = ?,";
        updateSQL += " proc_flag      = 'Y',";
        updateSQL += " mod_pgm        = ?,";
        updateSQL += " mod_time       = sysdate";
        whereStr   = "where rowid     = ? ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hIcdrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_cdrp_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF001 proc = new IpsF001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String icCardSeqno;
        String ipsCardNo;
        String issueCode;
        String rwId;
        String rwTime;
        String effcTime;
        String version;
        String rwStatus;
        String autopayFlag;
        String maxAutopay;
        String maxAmt;
        String maxAmtM;
        String personalType;
        String expiryTime;
        String personalId;
        String personalName;
        String bankId;
        String ticketType;
        String areaCode;
        String ipsTime;
        String ipsCumTime;
        String addPointTot;
        String s8TicketType;
        String s8Unit;
        String s8Sid;
        String s8Amt;
        String locId;
        String mentuuId;
        String rwIsstc;
        String filler1;
        String tac;
        String tacR;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(icCardSeqno, 32);
            rtn += comc.fixLeft(ipsCardNo, 11);
            rtn += comc.fixLeft(issueCode, 2);
            rtn += comc.fixLeft(rwId, 8);
            rtn += comc.fixLeft(rwTime, 14);
            rtn += comc.fixLeft(effcTime, 14);
            rtn += comc.fixLeft(version, 2);
            rtn += comc.fixLeft(rwStatus, 2);
            rtn += comc.fixLeft(autopayFlag, 2);
            rtn += comc.fixLeft(maxAutopay, 4);
            rtn += comc.fixLeft(maxAmt, 4);
            rtn += comc.fixLeft(maxAmtM, 4);
            rtn += comc.fixLeft(personalType, 2);
            rtn += comc.fixLeft(expiryTime, 14);
            rtn += comc.fixLeft(personalId, 12);
            rtn += comc.fixLeft(personalName, 16);
            rtn += comc.fixLeft(bankId, 2);
            rtn += comc.fixLeft(ticketType, 2);
            rtn += comc.fixLeft(areaCode, 2);
            rtn += comc.fixLeft(ipsTime, 14);
            rtn += comc.fixLeft(ipsCumTime, 14);
            rtn += comc.fixLeft(addPointTot, 6);
            rtn += comc.fixLeft(s8TicketType, 2);
            rtn += comc.fixLeft(s8Unit, 2);
            rtn += comc.fixLeft(s8Sid, 2);
            rtn += comc.fixLeft(s8Amt, 4);
            rtn += comc.fixLeft(locId, 2);
            rtn += comc.fixLeft(mentuuId, 10);
            rtn += comc.fixLeft(rwIsstc, 8);
            rtn += comc.fixLeft(filler1, 28);
            rtn += comc.fixLeft(tac, 8);
            rtn += comc.fixLeft(tacR, 1);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.type           = comc.subMS950String(bytes, 0, 1);
        detailSt.icCardSeqno  = comc.subMS950String(bytes, 1, 32);
        detailSt.ipsCardNo    = comc.subMS950String(bytes, 33, 11);
        detailSt.issueCode     = comc.subMS950String(bytes, 44, 2);
        detailSt.rwId          = comc.subMS950String(bytes, 46, 8);
        detailSt.rwTime        = comc.subMS950String(bytes, 54, 14);
        detailSt.effcTime      = comc.subMS950String(bytes, 68, 14);
        detailSt.version        = comc.subMS950String(bytes, 82, 2);
        detailSt.rwStatus      = comc.subMS950String(bytes, 84, 2);
        detailSt.autopayFlag   = comc.subMS950String(bytes, 86, 2);
        detailSt.maxAutopay    = comc.subMS950String(bytes, 88, 4);
        detailSt.maxAmt        = comc.subMS950String(bytes, 92, 4);
        detailSt.maxAmtM      = comc.subMS950String(bytes, 96, 4);
        detailSt.personalType  = comc.subMS950String(bytes, 100, 2);
        detailSt.expiryTime    = comc.subMS950String(bytes, 102, 14);
        detailSt.personalId    = comc.subMS950String(bytes, 116, 12);
        detailSt.personalName  = comc.subMS950String(bytes, 128, 16);
        detailSt.bankId        = comc.subMS950String(bytes, 144, 2);
        detailSt.ticketType    = comc.subMS950String(bytes, 146, 2);
        detailSt.areaCode      = comc.subMS950String(bytes, 148, 2);
        detailSt.ipsTime       = comc.subMS950String(bytes, 150, 14);
        detailSt.ipsCumTime   = comc.subMS950String(bytes, 164, 14);
        detailSt.addPointTot  = comc.subMS950String(bytes, 178, 6);
        detailSt.s8TicketType = comc.subMS950String(bytes, 184, 2);
        detailSt.s8Unit        = comc.subMS950String(bytes, 186, 2);
        detailSt.s8Sid         = comc.subMS950String(bytes, 188, 2);
        detailSt.s8Amt         = comc.subMS950String(bytes, 190, 4);
        detailSt.locId         = comc.subMS950String(bytes, 194, 2);
        detailSt.mentuuId      = comc.subMS950String(bytes, 196, 10);
        detailSt.rwIsstc       = comc.subMS950String(bytes, 206, 8);
        detailSt.filler1       = comc.subMS950String(bytes, 214, 28);
        detailSt.tac            = comc.subMS950String(bytes, 242, 8);
        detailSt.tacR          = comc.subMS950String(bytes, 250, 1);
    }

}
