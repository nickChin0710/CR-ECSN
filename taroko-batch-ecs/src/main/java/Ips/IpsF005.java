/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*   107/03/27  V1.01.00   詹曜維      RECS-s1070103-001  新增個人資料變動檔     *
*   107/05/21  V1.01.01   詹曜維      RECS-s1070103-001-2排除掛失、重複資料     *
*   107/09/21  V1.01.02   Brian       transfer to java                        *
*   109-12-14  V1.01.03   tanwei      updated for project coding standard     *
*   112/05/16  V1.01.04   Wilson    格式調整                                                                                                     *
******************************************************************************/

package Ips;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*聯名卡卡片個人資料變動檔(B2I005)產生*/
public class IpsF005 extends AccessDAO {
    private String progname = "聯名卡卡片個人資料變動檔(B2I005)產生 112/05/16 V1.01.04";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommCrdRoutine comcr    = null;

    int debug = 0;

    String hCallBatchSeqno          = "";
    String hTnlgNotifyDate          = "";
    int    forceFlag                  = 0;
    String fileSeq                    = "";
    String tmpstr1                     = "";
    String hTnlgFileName            = "";
    int    hTnlgRecordCnt           = 0;
    int    totCnt                     = 0;
    String hBusiBusinessDate        = "";
    String hIcdrMediaCreateDate    = "";
    String hIcdrMediaCreateTime    = "";
    String hIcdrIpsCardNo          = "";
    String hIcdrCardNo              = "";
    String hIcdrPersonalId          = "";
    String hIdnoChiName             = "";
    String hIdnoBirthday             = "";
    String hTempPhone1               = "";
    String hIdnoEMailAddr          = "";
    String hTempBillSendingAddress = "";
    String hTempOtherCntryCode     = "";
    String hTnlgMediaCreateDate    = "";
    String hTnlgFtpSendDate        = "";

    Buf1           detailSt = new Buf1();
    int out       = -1;

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
                comc.errExit("Usage : IpsF005 [notify_date] [force_flag (Y/N)]", "");
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

            comcr.hCallRProgramCode = javaProgram;
            String hTempUser = "";
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
            tmpstr1 = String.format("B2I005_%4.4s%8.8s%2.2s.dat", comc.IPS_BANK_ID4, hTnlgNotifyDate, fileSeq);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectIpsNotifyLogA() != 0) {
                    comcr.errRtn(String.format("select_ips_notify_log_a error !"), "", hCallBatchSeqno);
                }
            } else {
                updateIpsB2i005LogA();
            }

            fileOpen();

            selectIpsB2i005Log();

            hTnlgRecordCnt = totCnt;

            fileClose();

            showLogMessage("I", "", String.format("Process records = [%d]", totCnt));

            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            showLogMessage("I", "", String.format("%s", comcr.hCallErrorDesc));

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

    /*************************************************************************/
    void selectIpsB2i005Log() throws Exception {
        sqlCmd  = " SELECT distinct a.ips_card_no, ";
        sqlCmd += "        a.card_no, ";
        sqlCmd += "        c.id_no, ";
        sqlCmd += "        c.chi_name, ";
        sqlCmd += "        c.birthday, ";
        sqlCmd += "        case when c.cellar_phone = '' and c.home_tel_no1 = ''  then c.home_area_code2||c.home_tel_no2  ";
        sqlCmd += "             when c.cellar_phone = '' and c.home_tel_no1 != '' then c.home_area_code1||c.home_tel_no1 ";
        sqlCmd += "             else c.cellar_phone end as phone1, ";
        sqlCmd += "        c.e_mail_addr, ";
        sqlCmd += "        d.bill_sending_addr1 || d.bill_sending_addr2 || d.bill_sending_addr3 || d.bill_sending_addr4 || d.bill_sending_addr5 as address, ";
        sqlCmd += "        case when c.other_cntry_code is null then 'TW' when c.other_cntry_code = '     ' then 'TW'  ";
        sqlCmd += "             when c.other_cntry_code = 'CAN' then 'CA' when c.other_cntry_code = 'FRA' then 'FR'  ";
        sqlCmd += "             when c.other_cntry_code = 'BEL' then 'BE' when c.other_cntry_code = 'BLR' then 'BR'  ";
        sqlCmd += "             when c.other_cntry_code = 'CHN' then 'CN' when c.other_cntry_code = 'JPN' then 'JP'  ";
        sqlCmd += "             when c.other_cntry_code = 'USA' then 'US' when c.other_cntry_code = 'MYS' then 'MY' ";
        sqlCmd += "             when c.other_cntry_code = 'SAU' then 'SA' when c.other_cntry_code = 'SYR' then 'SY' ";
        sqlCmd += "             else substr(c.other_cntry_code,1,2) end as other_cntry_code ";
        sqlCmd += " from   ips_b2i005_log a, crd_card b,crd_idno c,act_acno d,ips_card e ";
        sqlCmd += " where  a.card_no      = b.card_no ";
        sqlCmd += " and    a.id_p_seqno   = c.id_p_seqno "; // find a.id_no in crd_idno 
        sqlCmd += " and    b.id_p_seqno   = c.id_p_seqno ";
        sqlCmd += " and    b.acno_p_seqno = d.acno_p_seqno ";
        sqlCmd += " and    a.ips_card_no  = e.ips_card_no ";
        sqlCmd += " and    e.current_code = '0' ";
        sqlCmd += " and    e.blacklt_date = '' ";
        sqlCmd += " and    e.lock_date    = '' ";
        sqlCmd += " and    e.return_date  = ''  ";
        sqlCmd += " and    1=1   ";
        sqlCmd += " and    (a.proc_flag   = 'N' or a.proc_flag   = '')  ";

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hIcdrIpsCardNo          = getValue("ips_card_no");
            hIcdrCardNo              = getValue("card_no");
            hIcdrPersonalId          = getValue("id_no");
            hIdnoChiName             = getValue("chi_name");
            hIdnoBirthday             = getValue("birthday");
            hTempPhone1               = getValue("phone1");
            hIdnoEMailAddr          = getValue("e_mail_addr");
            hTempBillSendingAddress = getValue("address");
            hTempOtherCntryCode     = getValue("other_cntry_code");

            writeRtn();

            updateIpsB2i005Log();

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]", totCnt));
        }
        closeCursor(cursorIndex);
    }

    /*************************************************************************/
    void writeRtn() throws Exception {

        detailSt.type = "D";
        detailSt.ipsCardNo          = String.format("%-11.11s", hIcdrIpsCardNo);
        detailSt.personalId          = String.format("%-12.12s", hIcdrPersonalId);
        detailSt.chiName             = String.format("%-16.16s", hIdnoChiName);
        detailSt.birthday             = String.format("%-8.8s", hIdnoBirthday);
        detailSt.phone1               = String.format("%-10.10s", hTempPhone1);
        detailSt.eMailAddr          = String.format("%-50.50s", hIdnoEMailAddr);
        detailSt.billSendingAddress = String.format("%-100.100s", hTempBillSendingAddress);
        detailSt.otherCntryCode     = String.format("%-2.2s", hTempOtherCntryCode);
        detailSt.traditionName       = "";
        detailSt.traditionPinyin     = "";

        String buf = comc.fixLeft(detailSt.allText(), 448);
        writeTextFile(out, buf + "\r\n");

        if (debug == 1)
            showLogMessage("I", "", String.format(" ALL =[%s]", detailSt.allText()));

        return;
    }

    /*******************************************************************/
    void updateIpsB2i005Log() throws Exception {
        daoTable = "ips_b2i005_log";
        updateSQL = " media_create_date = ?, ";
        updateSQL += " media_create_time = ?, ";
        updateSQL += " notify_date       = ?, ";
        updateSQL += " file_name         = ?, ";
        updateSQL += " proc_flag         = 'Y', ";
        updateSQL += " mod_pgm           = 'IpsF005', ";
        updateSQL += " mod_time          = sysdate ";
        whereStr = "  where ips_card_no       = ? ";
        whereStr += "    and proc_flag         = 'N' ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, hIcdrIpsCardNo);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_b2i005_log not found!", "", hCallBatchSeqno);
        }

    }

    /*******************************************************************/
    void updateIpsB2i005LogA() throws Exception {
        daoTable  = "ips_b2i005_log";
        updateSQL = " proc_flag        = 'N' ";
        whereStr  = " where file_name  = ? ";
        setString(1, hTnlgFileName);

        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_b2i005_log _a" + " not found!", "", hCallBatchSeqno);
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

        tmpstr1 = String.format("H%6.6s_%440.440s", "B2I005", " ");

        writeTextFile(out, String.format("%-448.448s\r\n", tmpstr1));
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%06d%441.441s", totCnt, " ");

        writeTextFile(out, String.format("%-448.448s\r\n", tmpstr1));
        closeOutputText(out);
    }

    /*******************************************************************/
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
            String stderr = String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsF005 proc = new IpsF005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String ipsCardNo;
        String personalId;
        String chiName;
        String birthday;
        String phone1;
        String eMailAddr;
        String billSendingAddress;
        String otherCntryCode;
        String traditionName;
        String traditionPinyin;
        String filler1;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(type, 1);
            rtn += fixLeft(ipsCardNo, 11);
            rtn += fixLeft(personalId, 12);
            rtn += fixLeft(chiName, 16);
            rtn += fixLeft(birthday, 8);
            rtn += fixLeft(phone1, 10);
            rtn += fixLeft(eMailAddr, 50);
            rtn += fixLeft(billSendingAddress, 100);
            rtn += fixLeft(otherCntryCode, 2);
            rtn += fixLeft(traditionName, 100);
            rtn += fixLeft(traditionPinyin, 100);
            rtn += fixLeft(filler1, 38);
            rtn += fixLeft(fillerEnd, 2);
            return rtn;
        }

        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            int size = (Math.floorDiv(len, 100) + 1) * 100;
            String spc = "";
            for (int i = 0; i < size; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "MS950");
        }
    }

}
