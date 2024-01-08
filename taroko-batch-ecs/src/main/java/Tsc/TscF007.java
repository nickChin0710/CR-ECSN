/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00  Edson       program initial                           *
*  107/11/09  V1.01.03  詹曜維      RECS-s1071026-104 新增國籍欄位                                                            *
*  108/02/14  V1.01.04  詹曜維      RECS-s1080130-010 修改電話邏輯                                                            *
*  108/05/27  V1.01.04  Brian       update to V1.01.04                        *
*  109-11-16  V1.01.05  tanwei    updated for project coding standard         *
*  112/05/03  V1.01.06  Wilson      CDPF change to CDPS                       *
*  112/09/01  V1.01.07  Wilson      修正hash值有誤問題                                                                               *
*  112/12/19  V1.01.08  Wilson      檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;

/*悠遊卡卡人資料檔(CDPS)媒體產生程式*/
public class TscF007 extends AccessDAO {
    private final String progname = "悠遊卡卡人資料檔(CDPS)媒體產生程式   112/12/19 V1.01.08";
    CommFunction   comm     = new CommFunction();
    CommCrd        comc     = new CommCrd();
    CommIps        comips   = new CommIps();
    CommCrdRoutine comcr    = null;

    String hCallBatchSeqno = "";

    String hTempUser              = "";
    String hTnlgNotifyDate       = "";
    String hBusiBusinessDate     = "";
    String hCdpfMediaCreateDate = "";
    String hCdpfMediaCreateTime = "";
    String hCdpfTscCardNo       = "";
    String hCdpfCardNo           = "";
    String hCdpfTxType           = "";
    String hCdpfTxRsn            = "";
    String hCdpfId                = "";
    String hIdnoTscMarketFlag   = "";
    String hIdnoChiName          = "";
    String hIdnoIndigenousName    = "";
    String hIdnoBirthday          = "";
    String hIdnoOfficeAreaCode1 = "";
    String hIdnoOfficeTelNo1    = "";
    String hIdnoOfficeTelExt1   = "";
    String hIdnoOfficeAreaCode2 = "";
    String hIdnoOfficeTelNo2    = "";
    String hIdnoOfficeTelExt2   = "";
    String hIdnoHomeAreaCode1   = "";
    String hIdnoHomeTelNo1      = "";
    String hIdnoHomeTelExt1     = "";
    String hIdnoHomeAreaCode2   = "";
    String hIdnoHomeTelNo2      = "";
    String hIdnoHomeTelExt2     = "";
    String hIdnoCellarPhone      = "";
    String hIdnoEMailAddr       = "";
    String hTempOtherCntryCode  = "";
    String hAcnoBillSendingZip  = "";
    String hTtttTempX100         = "";
    String hCdpfRowid             = "";
    String tempX04                 = "";
    String tempX01                 = "";
    String hTempX16               = "";
    String hTnlgFileName         = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate     = "";

    int    forceFlag        = 0;
    int    hTnlgRecordCnt = 0;
    int    totCnt           = 0;
    int    diffDate         = 0;
    int    totalCnt         = 0;
    String tmpstr            = "";
    String tmpstr1           = "";
    String tmpstr2           = "";
    String temstr1           = "";
    String ftpStr           = "";

    int out       = -1;
    Buf1           detailSt = new Buf1();

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
                comc.errExit("Usage : TscF007 [notify_date] [force_flag (Y/N)]", "");
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
            tmpstr1 = String.format("CDPS.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;

            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscCdpfLoga();
            }

            fileOpen();
            selectTscCdpfLog();
            hTnlgRecordCnt = totalCnt;
            fileClose();

            // ==============================================
            // 固定要做的

            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            if (out != -1) {
                closeOutputText(out);
                out = -1;
            }
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = "select to_char(add_days(to_date(business_date,'yyyymmdd'),1),'yyyymmdd') h_business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_cdpf_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_cdpf_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hCdpfMediaCreateDate = getValue("h_cdpf_media_create_date");
        hCdpfMediaCreateTime = getValue("h_cdpf_media_create_time");
    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd = "select media_crt_date,";
        sqlCmd += "ftp_send_date ";
        sqlCmd += " from tsc_notify_log  ";
        sqlCmd += "where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_crt_date");
            hTnlgFtpSendDate = getValue("ftp_send_date");
        } else
            return (0);

        if (hTnlgFtpSendDate.length() != 0) {
            showLogMessage("I", "", String.format("通知檔 [%s] 已FTP至TSCC, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            showLogMessage("I", "", String.format("卡號資料檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)", hTnlgFileName));
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateTscCdpfLoga() throws Exception {
        daoTable = "tsc_cdpf_log";
        updateSQL = "proc_flag = 'N'";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_cdpf_log not found!", "", hCallBatchSeqno);
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
        tmpstr1 = String.format("HCDPS%8.8s%8.8s%6.6s%445.445s", comc.TSCC_BANK_ID8, hCdpfMediaCreateDate, hCdpfMediaCreateTime, " ");

        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);
        String buf = String.format("%-472.472s%-16.16s\r\n", tmpstr1, tmpstr2);
        writeTextFile(out, buf);
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%08d%463.463s", totalCnt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        writeTextFile(out, String.format("%-472.472s%16.16s\r\n", tmpstr1, new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }

    }

    /***********************************************************************/
    void selectTscCdpfLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.tsc_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.tx_type,";
        sqlCmd += "a.tx_rsn,";
        sqlCmd += "c.id_no,";
        sqlCmd += "c.tsc_market_flag,";
        sqlCmd += "c.chi_name,";
        sqlCmd += "c.indigenous_name,";
        sqlCmd += "c.birthday,";
        sqlCmd += "c.office_area_code1,";
        sqlCmd += "c.office_tel_no1,";
        sqlCmd += "c.office_tel_ext1,";
        sqlCmd += "c.office_area_code2,";
        sqlCmd += "c.office_tel_no2,   ";
        sqlCmd += "c.office_tel_ext2,  ";
        sqlCmd += "c.home_area_code1,  ";
        sqlCmd += "c.home_tel_no1,     ";
        sqlCmd += "c.home_tel_ext1,    ";
        sqlCmd += "c.home_area_code2,  ";
        sqlCmd += "c.home_tel_no2,     ";
        sqlCmd += "c.home_tel_ext2,    ";
        sqlCmd += "c.cellar_phone,";
        sqlCmd += "c.e_mail_addr,";
        sqlCmd += " case when c.other_cntry_code = ''    then 'TW' when c.other_cntry_code = '     ' then 'TW' ";
        sqlCmd += "      when c.other_cntry_code = 'CAN' then 'CA' when c.other_cntry_code = 'FRA'   then 'FR'   ";
        sqlCmd += "      when c.other_cntry_code = 'BEL' then 'BE' when c.other_cntry_code = 'BLR'   then 'BR'   ";
        sqlCmd += "      when c.other_cntry_code = 'CHN' then 'CN' when c.other_cntry_code = 'JPN'   then 'JP'   ";
        sqlCmd += "      when c.other_cntry_code = 'USA' then 'US' when c.other_cntry_code = 'MYS'   then 'MY'   ";
        sqlCmd += "      when c.other_cntry_code = 'SAU' then 'SA' when c.other_cntry_code = 'SYR'   then 'SY'   ";
        sqlCmd += "      else substr(c.other_cntry_code,1,2) end as other_cntry_code,                          ";
        sqlCmd += "d.bill_sending_zip,";
        sqlCmd += "d.bill_sending_addr1 || " + "d.bill_sending_addr2 || " + "d.bill_sending_addr3 || "
                + "d.bill_sending_addr4 || " + "d.bill_sending_addr5 as bill_sending_addr,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from act_acno d, crd_idno c,crd_card b,tsc_cdpf_log a,tsc_card e ";
        sqlCmd += "where b.card_no       = a.card_no ";
        sqlCmd += "  and d.acno_p_seqno  = b.acno_p_seqno ";
        sqlCmd += "  and c.id_p_seqno    = b.id_p_seqno ";
        sqlCmd += "  and a.tsc_card_no   = e.tsc_card_no ";
        sqlCmd += "  and a.proc_flag     = 'N' ";
        openCursor();
        while (fetchTable()) {
            hCdpfTscCardNo = getValue("tsc_card_no");
            hCdpfCardNo = getValue("card_no");
            hCdpfTxType = getValue("tx_type");
            hCdpfTxRsn = getValue("tx_rsn");
            hCdpfId = getValue("id_no");
            hIdnoTscMarketFlag = getValue("tsc_market_flag");
            hIdnoChiName = getValue("chi_name");
            hIdnoIndigenousName = getValue("indigenous_name");
            hIdnoBirthday = getValue("birthday");
            hIdnoOfficeAreaCode1 = getValue("office_area_code1");
            hIdnoOfficeTelNo1 = getValue("office_tel_no1");
            hIdnoOfficeTelExt1 = getValue("office_tel_ext1");
            hIdnoOfficeAreaCode2 = getValue("office_area_code2");
            hIdnoOfficeTelNo2 = getValue("office_tel_no2");
            hIdnoOfficeTelExt2 = getValue("office_tel_ext2");
            hIdnoHomeAreaCode1 = getValue("home_area_code1");
            hIdnoHomeTelNo1 = getValue("home_tel_no1");
            hIdnoHomeTelExt1 = getValue("home_tel_ext1");
            hIdnoHomeAreaCode2 = getValue("home_area_code2");
            hIdnoHomeTelNo2 = getValue("home_tel_no2");
            hIdnoHomeTelExt2 = getValue("home_tel_ext2");
            hIdnoCellarPhone = getValue("cellar_phone");
            hIdnoEMailAddr = getValue("e_mail_addr");
            hTempOtherCntryCode = getValue("other_cntry_code");
            hAcnoBillSendingZip = getValue("bill_sending_zip");
            hTtttTempX100 = getValue("bill_sending_addr");
            hCdpfRowid = getValue("rowid");

            writeRtn();

            updateTscCdpfLog();

            totalCnt++;
            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]", totCnt));
        }
        closeCursor();
    }

    /***********************************************************************/
    void writeRtn() throws Exception {
        byte[] endflag = new byte[2];
        endflag[0] = 0x0D;
        endflag[1] = 0x0A;

        String tempX20 = "";
        detailSt = new Buf1();
        detailSt.type = "D";
        detailSt.attri = "01";

        tmpstr = String.format("%-1.1s", hCdpfTxType);
        detailSt.txType = tmpstr;

        tmpstr = String.format("%-1.1s", hCdpfTxRsn);
        detailSt.txRsn = tmpstr;

        tmpstr = String.format("%-20.20s", hCdpfTscCardNo);
        detailSt.tscCashNo = tmpstr;

        hTempX16 = hCdpfTscCardNo;
        tempX04 = "";
        tempX01 = "";
        sqlCmd = "select substr(new_end_date,3,4) temp_x04,";
        sqlCmd += "decode(tsc_sign_flag,'','N', tsc_sign_flag) temp_x01 ";
        sqlCmd += " from tsc_card  ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempX16);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            tempX04 = getValue("temp_x04");
            tempX01 = getValue("temp_x01");
        }
        tmpstr = String.format("%-4.4s", tempX04);
        detailSt.effcYymm = tmpstr;

        tmpstr = String.format("%-1.1s", "1");
        if (hIdnoTscMarketFlag.substring(0, 1).equals("Y"))
            tmpstr = String.format("%-1.1s", "0");
        detailSt.tscMarketFlag = tmpstr;

        tmpstr = String.format("%-20.20s", hCdpfId);
        detailSt.id = tmpstr;

        tmpstr = comc.fixLeft(hIdnoChiName, 100);
        detailSt.chiName = tmpstr;

        tmpstr = comc.fixLeft(hIdnoIndigenousName, 100);
        detailSt.chiNameSpec = tmpstr;

        tmpstr = String.format("%-8.8s", hIdnoBirthday);
        detailSt.birthday = tmpstr;

        if (hIdnoOfficeTelNo1.length() == 0 && hIdnoOfficeTelNo2.length() == 0
                && hIdnoHomeTelNo1.length() == 0) {
            if (hIdnoHomeAreaCode2.length() > 0) {
                tempX20 = String.format("(%s)%s", hIdnoHomeAreaCode2, hIdnoHomeTelNo2);
            } else {
                tempX20 = String.format("%s", hIdnoHomeTelNo2);
            }

            if (hIdnoHomeTelExt2.length() > 0) {
                tempX20 += "#";
                tempX20 += hIdnoHomeTelExt2;
            }
        } else if (hIdnoOfficeTelNo1.length() == 0 && hIdnoOfficeTelNo2.length() == 0
                && hIdnoHomeTelNo1.length() > 0) {
            if (hIdnoHomeAreaCode1.length() > 0) {
                tempX20 = String.format("(%s)%s", hIdnoHomeAreaCode1, hIdnoHomeTelNo1);
            } else {
                tempX20 = String.format("%s", hIdnoHomeTelNo1);
            }

            if (hIdnoHomeTelExt1.length() > 0) {
                tempX20 += "#";
                tempX20 += hIdnoHomeTelExt1;
            }
        } else if (hIdnoOfficeTelNo1.length() == 0 && hIdnoOfficeTelNo2.length() > 0) {
            if (hIdnoOfficeAreaCode2.length() > 0) {
                tempX20 = String.format("(%s)%s", hIdnoOfficeAreaCode2, hIdnoOfficeTelNo2);
            } else {
                tempX20 = String.format("%s", hIdnoOfficeTelNo2);
            }

            if (hIdnoOfficeTelExt2.length() > 0) {
                tempX20 += "#";
                tempX20 += hIdnoOfficeTelExt2;
            }
        } else {
            if (hIdnoOfficeAreaCode1.length() > 0) {
                tempX20 = String.format("%s", hIdnoOfficeTelNo1);
            } else {
                tempX20 = String.format("%s", hIdnoOfficeTelNo1);
            }
            if (hIdnoOfficeTelExt1.length() > 0) {
                tempX20 += "#";
                tempX20 += hIdnoOfficeTelExt1;
            }
        }
        tmpstr = String.format("%-20.20s", tempX20);
        detailSt.telephone = tmpstr;

        tmpstr = String.format("%-20.20s", hIdnoCellarPhone);
        detailSt.mobil = tmpstr;

        tmpstr = String.format("%-5.5s", hAcnoBillSendingZip);
        detailSt.zipCode = tmpstr;

        tmpstr = comc.fixLeft(hTtttTempX100, 100);
        detailSt.addr = tmpstr;

        tmpstr = String.format("%-50.50s", hIdnoEMailAddr);
        detailSt.email = tmpstr;

        tmpstr = String.format("%-2.2s", hTempOtherCntryCode);
        detailSt.otherCntryCode = tmpstr;

        tmpstr1 = comc.fixLeft(detailSt.allText(), 472);                
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
    void updateTscCdpfLog() throws Exception {
        daoTable = "tsc_cdpf_log";
        updateSQL = "media_crt_date = ?,";
        updateSQL += " media_crt_time = ?,";
        updateSQL += " notify_date  = ?,";
        updateSQL += " file_name   = ?,";
        updateSQL += " proc_flag   = 'Y',";
        updateSQL += " mod_pgm   = ?,";
        updateSQL += " mod_time   = sysdate";
        whereStr = "where rowid  = ? ";
        setString(1, hCdpfMediaCreateDate);
        setString(2, hCdpfMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hCdpfRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_cdpf_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscF007 proc = new TscF007();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Buf1 {
        String type;
        String attri;
        String txType;
        String txRsn;
        String tscCashNo;
        String effcYymm;
        String tscMarketFlag;
        String id;
        String chiName;
        String chiNameSpec;
        String birthday;
        String telephone;
        String mobil;
        String zipCode;
        String addr;
        String email;
        String otherCntryCode;
        String filler1;
        String hashValue;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(type, 1);
            rtn += fixLeft(attri, 2);
            rtn += fixLeft(txType, 1);
            rtn += fixLeft(txRsn, 1);
            rtn += fixLeft(tscCashNo, 20);
            rtn += fixLeft(effcYymm, 4);
            rtn += fixLeft(tscMarketFlag, 1);
            rtn += fixLeft(id, 20);
            rtn += fixLeft(chiName, 100);
            rtn += fixLeft(chiNameSpec, 100);
            rtn += fixLeft(birthday, 8);
            rtn += fixLeft(telephone, 20);
            rtn += fixLeft(mobil, 20);
            rtn += fixLeft(zipCode, 5);
            rtn += fixLeft(addr, 100);
            rtn += fixLeft(email, 50);
            rtn += fixLeft(otherCntryCode, 2);
            rtn += fixLeft(filler1, 17);
            rtn += fixLeft(hashValue, 16);
            rtn += fixLeft(fillerEnd, 2);
            return rtn;
        }

        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
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
