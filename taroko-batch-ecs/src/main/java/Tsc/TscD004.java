/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/01/06  V1.00.00    Wendy Lu                         program initial    *
*  112/05/05  V1.00.01    Wilson     檔案格式調整                                                                                          *
*  112/05/09  V1.00.02    Wilson    續卡舊卡不停用                                                                                          *
*  112/06/27  V1.00.03    Wilson    mark判斷電子票證註記                                                                           *
*  112/12/19  V1.00.04    Wilson    檔名日期加一日                                                                                          *
******************************************************************************/

package Tsc;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommIps;


public class TscD004 extends AccessDAO {
	private String PROGNAME = "悠遊VD卡卡號資料檔(DCNR)媒體產生程式  112/12/19 V1.00.04";
    CommFunction   comm   = new CommFunction();
    CommCrd        comc   = new CommCrd();
    CommIps        comips = new CommIps();
    CommCrdRoutine comcr  = null;

    String hCallBatchSeqno = "";
    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hDcnrMediaCreateDate = "";
    String hDcnrMediaCreateTime = "";
    String hDcnrTscCardNo = "";
    String hDcnrCardNo = "";
    String hDcnrTscEmbossRsn = "";
    String hDcnrTscVendorCd = "";
    String hDcnrUpperLmt = "";
    String hDcnrUpperLmtAcmm = "";
    String hDcnrVendorEmbossDate = "";
    String hDcnrVendorDateTo = "";
    String hDcnrVendorDateRtn = "";
    String hDcnrOkFlag = "";
    double hDcnrTscAmt = 0;
    double hDcnrTscPledgeAmt = 0;
    String hDcnrIcSeqNo = "";
    String hDcnrIsamSeqNo = "";
    String hDcnrIsamBatchNo = "";
    String hDcnrIsamBatchSeq = "";
    long   hDcnrAutoloadAmt = 0;
    String hTardOldTscCardNo = "";
    String hTardTscSignFlag = "";
    String hDcnrRowid = "";
    String hTardCurrentCode = "";
    String hMbosElectronicCodeOld = "";
    String hMbosElectronicCode     = "";
    int hDcnrDiffDate = 0;
    String tempX04 = "";
    String hTempX16 = "";
    String hBinType = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    String hTardCreateDate = "";

    int forceFlag = 0;
    int hTnlgRecordCnt = 0;
    int totCnt = 0;
    int diffDate = 0;
    int totalCnt = 0;
    String tmpstr = "";
    String tmpstr1 = "";
    String tmpstr2 = "";
    String temstr1 = "";
    String ftpStr = "";

    int tscCnt = 0;
    int out = -1;
    buf1 detailSt = new buf1();

    public int mainProcess(String[] args) throws Exception {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : TscD004 [notify_date] [force_flag (Y/N)]", "");
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
            tmpstr1 = String.format("DCNR.%8.8s.%8.8s01", comc.TSCC_BANK_ID8, hTnlgNotifyDate);
            hTnlgFileName = tmpstr1;
            if (forceFlag == 0) {
                if (selectTscNotifyLoga() != 0) {
                    String errMsg = String.format("select_tsc_notify_log_a error !");
                    comcr.errRtn(errMsg, "", hCallBatchSeqno);
                }
            } else {
                updateTscDcnrLoga();
            }

            fileOpen();
            selectTscDcnrLog();
            hTnlgRecordCnt = totalCnt;
            fileClose();

            showLogMessage("I", "", String.format("程式執行結束 = [%d]\n", totCnt));
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
        sqlCmd += "to_char(sysdate,'yyyymmdd') h_dcnr_media_create_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_dcnr_media_create_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hDcnrMediaCreateDate = getValue("h_dcnr_media_create_date");
        hDcnrMediaCreateTime = getValue("h_dcnr_media_create_time");

    }

    /***********************************************************************/
    int selectTscNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd = "select media_crt_date,";
        sqlCmd += "ftp_send_date ";
        sqlCmd += "from tsc_notify_log ";
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
    void updateTscDcnrLoga() throws Exception {
        daoTable = "tsc_dcnr_log ";
        updateSQL = "proc_flag = 'N' ";
        whereStr = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcnr_log not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    void selectTscDcnrLog() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "a.tsc_card_no, ";
        sqlCmd += "a.card_no, ";
        sqlCmd += "a.tsc_emboss_rsn, ";
        sqlCmd += "a.tsc_vendor_cd, ";
        sqlCmd += "a.upper_lmt, ";
        sqlCmd += "a.upper_lmt_acmm, ";
        sqlCmd += "a.vendor_emboss_date, ";
        sqlCmd += "a.vendor_date_to, ";
        sqlCmd += "a.vendor_date_rtn, ";
        sqlCmd += "a.ok_flag, ";
        sqlCmd += "a.tsc_amt, ";
        sqlCmd += "a.tsc_pledge_amt, ";
        sqlCmd += "a.ic_seq_no, ";
        sqlCmd += "a.isam_seq_no, ";
        sqlCmd += "a.isam_batch_no, ";
        sqlCmd += "a.isam_batch_seq, ";
        sqlCmd += "a.autoload_amt, ";
        sqlCmd += "b.old_tsc_card_no, ";
        sqlCmd += "decode(b.tsc_sign_flag,'','N',b.tsc_sign_flag) h_tard_tsc_sign_flag, ";
        sqlCmd += "a.rowid as rowid, ";
        sqlCmd += "b.current_code, ";
        sqlCmd += "nvl(trunc(sysdate) - to_date(decode(a.vendor_emboss_date,'',to_char(a.mod_time,'yyyymmdd'),a.vendor_emboss_date),'yyyymmdd'),'0') h_dcnr_diff_date ";
        sqlCmd += "from tsc_vd_card b,tsc_dcnr_log a ";
        sqlCmd += "where a.tsc_card_no = b.tsc_card_no ";
        sqlCmd += "and a.proc_flag = 'N' ";
        sqlCmd += "and a.ok_flag  = 'Y' ";
        sqlCmd += "and (a.tsc_emboss_rsn <> '9' ";
        sqlCmd += "or (a.tsc_emboss_rsn = '9' and nvl(trunc(sysdate) - to_date(decode(a.vendor_emboss_date,'',to_char(a.mod_time,'yyyymmdd'),a.vendor_emboss_date),'yyyymmdd'),'0')>=180)) ";
        sqlCmd += "and a.vendor_date_rtn <> '' ";
        sqlCmd += "order by a.tsc_emboss_rsn ";
        openCursor();
        while (fetchTable()) {
            hDcnrTscCardNo = getValue("tsc_card_no");
            hDcnrCardNo = getValue("card_no");
            hDcnrTscEmbossRsn = getValue("tsc_emboss_rsn");
            hDcnrTscVendorCd = getValue("tsc_vendor_cd");
            hDcnrUpperLmt = getValue("upper_lmt");
            hDcnrUpperLmtAcmm = getValue("upper_lmt_acmm");
            hDcnrVendorEmbossDate = getValue("vendor_emboss_date");
            hDcnrVendorDateTo = getValue("vendor_date_to");
            hDcnrVendorDateRtn = getValue("vendor_date_rtn");
            hDcnrOkFlag = getValue("ok_flag");
            hDcnrTscAmt = getValueDouble("tsc_amt");
            hDcnrTscPledgeAmt = getValueDouble("tsc_pledge_amt");
            hDcnrIcSeqNo = getValue("ic_seq_no");
            hDcnrIsamSeqNo = getValue("isam_seq_no");
            hDcnrIsamBatchNo = getValue("isam_batch_no");
            hDcnrIsamBatchSeq = getValue("isam_batch_seq");
            hDcnrAutoloadAmt = getValueLong("autoload_amt");
            hTardOldTscCardNo = getValue("old_tsc_card_no");
            hTardTscSignFlag = getValue("h_tard_tsc_sign_flag");
            hDcnrRowid = getValue("rowid");
            hTardCurrentCode = getValue("current_code");
            hDcnrDiffDate = getValueInt("h_dcnr_diff_date");

            /* 停卡滿六個月即報送 停卡tsc_emboss_rsn為9 */
            if ((hTardCurrentCode.equals("0") && hDcnrTscEmbossRsn.toCharArray()[0] == '9') && diffDate < 180) {
                continue;
            }

            writeRtn();

            updateTscDcnrLog();

            totalCnt++;
            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]", totCnt));
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateTscDcnrLog() throws Exception {
        daoTable = "tsc_dcnr_log ";
        updateSQL = "media_create_date = ?, ";
        updateSQL += "media_create_time = ?, ";
        updateSQL += "notify_date = ?, ";
        updateSQL += "file_name = ?, ";
        updateSQL += "proc_flag = 'Y', ";
        updateSQL += "mod_pgm = ?, ";
        updateSQL += "mod_time = sysdate ";
        whereStr = "where rowid = ? ";
        setString(1, hDcnrMediaCreateDate);
        setString(2, hDcnrMediaCreateTime);
        setString(3, hTnlgNotifyDate);
        setString(4, hTnlgFileName);
        setString(5, javaProgram);
        setRowId(6, hDcnrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_dcnr_log not found!", "", hCallBatchSeqno);
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
        tmpstr1 = String.format("HDCNR%8.8s%8.8s%6.6s%203.203s", comc.TSCC_BANK_ID8, hDcnrMediaCreateDate,
        		hDcnrMediaCreateTime, " ");

        byte[] tmp = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        tmpstr2 = new String(tmp);

        String buf = String.format("%-230.230s%-16.16s\r\n", tmpstr1, tmpstr2);
        writeTextFile(out, buf);
    }

    /*******************************************************************/
    void fileClose() throws Exception {
        tmpstr1 = String.format("T%08d%221.221s", totalCnt, " ");
        byte[] tmpstr2 = comips.commHashUnpack(tmpstr1.getBytes("big5"));
        writeTextFile(out, String.format("%-230.230s%16.16s\r\n", tmpstr1, new String(tmpstr2, "big5")));
        if (out != -1) {
            closeOutputText(out);
            out = -1;
        }

    }

    /***********************************************************************/
    void writeRtn() throws Exception {
        byte[] endflag = new byte[2];
        endflag[0] = 0x0D;
        endflag[1] = 0x0A;

        detailSt = new buf1();
        detailSt.type = "D";
        detailSt.attri = "01";

        if (hTardCurrentCode.equals("0") && hDcnrTscEmbossRsn.toCharArray()[0] == '9' && diffDate >= 180)
            tmpstr = "C";
        else
            tmpstr = "A";

        detailSt.txType = tmpstr;

        tmpstr = String.format("%-20.20s", hDcnrTscCardNo);
        detailSt.tscCashNo = tmpstr;

        hTempX16 = hDcnrTscCardNo;
        tempX04 = "";
        sqlCmd = "select substr(new_end_date,3,4) temp_x04 ";
        sqlCmd += "from tsc_vd_card ";
        sqlCmd += "where tsc_card_no = ? ";
        setString(1, hTempX16);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_tsc_vd_card not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	tempX04 = getValue("temp_x04");
        }
        tmpstr = String.format("%-4.4s", tempX04);
        detailSt.effcYymm = tmpstr;

        tmpstr = String.format("%-20.20s", hDcnrIcSeqNo);
        detailSt.chipSeq = tmpstr;

        tmpstr = String.format("%-20.20s", hDcnrTscCardNo);
        detailSt.tscCardNo = tmpstr;

        switch (comcr.str2int(hDcnrTscEmbossRsn)) {
        case 1:
        case 2:
            tmpstr = "N";
            break;
        case 3:
        case 4:
            tmpstr = "C";
            break;
        case 5:
            tmpstr = "R";
//            selectDbcEmboss(); 
//            if((hMbosElectronicCode.equals("01")) &&
//               (!hMbosElectronicCode.equals(hMbosElectronicCodeOld)) )
//              {
//               selectTscCardc();
//               if(tscCnt < 1)
//                  tmpstr = "N"; // 換卡樣,新卡='01' -> 新製(悠遊卡檔未曾存在過則以新戶報送悠遊卡)
//              }
            break;
        case 9:
            tmpstr = "E";   // 停卡
            break;
        }
        detailSt.txRsn = tmpstr;

        tmpstr = String.format("%-20.20s", hTardOldTscCardNo);
        detailSt.tscCashNoO = tmpstr;

        switch (hDcnrOkFlag.toCharArray()[0]) {
        case 'Y':
            tmpstr = String.format("%-10.10s", "OK");
            break;
        case 'E':
            tmpstr = String.format("%-10.10s", "EC Error");
            break;
        case 'S':
            tmpstr = String.format("%-10.10s", "SVC Error");
            break;
        case 'N':
            tmpstr = String.format("%-10.10s", "Error");
            break;
        }
        detailSt.cardStatus = tmpstr;

        if (hDcnrOkFlag.substring(0, 1).equals("Y"))
            tmpstr = String.format("%-1.1s", "0");
        else
            tmpstr = String.format("%-1.1s", "1");
        detailSt.embossStatus = tmpstr;

        tmpstr = String.format("%-2.2s", comc.TSCC_BANK_ID2);
        detailSt.bankNo2 = tmpstr;

        tmpstr = String.format("%-8.8s", hDcnrVendorEmbossDate);
        detailSt.embossDate = tmpstr;

        tmpstr = String.format("%05.0f", hDcnrTscAmt);
        detailSt.tscAmt = tmpstr;

        tmpstr = String.format("%05.0f", hDcnrTscPledgeAmt);
        detailSt.tscPledgeAmt = tmpstr;

        tmpstr = String.format("%-10.10s", hDcnrTscVendorCd);
        detailSt.vendorCd = tmpstr;

        tmpstr = String.format("%-8.8s", hDcnrIsamSeqNo);
        detailSt.isamSeqNo = tmpstr;

        tmpstr = String.format("%-10.10s", hDcnrIsamBatchNo);
        detailSt.isamBatchNo = tmpstr;

        tmpstr = String.format("%-5.5s", hDcnrIsamBatchSeq);
        detailSt.isamBatchSeq = tmpstr;

        tmpstr = String.format("%04d", hDcnrAutoloadAmt);
        detailSt.autoloadAmt = tmpstr;

        tmpstr = String.format("%1.1s", "0");
        if (hTardTscSignFlag.substring(0, 1).equals("Y"))
            tmpstr = String.format("%1.1s", "1");
        detailSt.tscSignFlag = tmpstr;

        tmpstr = String.format("%2.2s", "01");
        detailSt.areaCode = tmpstr;

        hBinType = "";

        sqlCmd = "select bin_type ";
        sqlCmd += "from PTR_BINTABLE ";
        sqlCmd += "where BIN_NO = substr(?,1,6) ";
        setString(1, hDcnrCardNo);
        recordCnt = selectTable();
        if (recordCnt > 0) {
        	hBinType = getValue("bin_type");
        }

        tmpstr = String.format("%-1.1s", hBinType);
        detailSt.binType = tmpstr;

        tmpstr1 = String.format("$-230.230s", detailSt.allText());
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
    void selectTscCardc() throws Exception {

        tscCnt = 0;
        sqlCmd  = "select count(*) as tsc_cnt ";
        sqlCmd += "from tsc_vd_card ";
        sqlCmd += "where vd_card_no = ? ";
        setString(1, hDcnrCardNo);
        int recordCnt = selectTable();
        tscCnt = getValueInt("tsc_cnt");
    }
    /***********************************************************************/
//    void selectDbcEmboss() throws Exception {
//
//        hMbosElectronicCodeOld = "";
//        hMbosElectronicCode = "";
//
//        sqlCmd  = "select electronic_code, ";
//        sqlCmd += "electronic_code_old ";
//        sqlCmd += "from dbc_emboss ";
//        sqlCmd += "where card_no = ? ";
//        setString(1, hDcnrCardNo);
//        int recordCnt = selectTable();
//        if (notFound.equals("Y")) {
//            comcr.errRtn("select_crd_emboss not found!", hDcnrCardNo, hCallBatchSeqno);
//        }
//        if (recordCnt > 0) {
//            hMbosElectronicCodeOld = getValue("electronic_code_old");
//            hMbosElectronicCode = getValue("electronic_code");
//        }
//    }
    /***********************************************************************/
     public static void main(String[] args) throws Exception {
        TscD004 proc = new TscD004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class buf1 {
        String type;
        String attri;
        String txType;
        String tscCashNo;
        String effcYymm;
        String chipSeq;
        String tscCardNo;
        String txRsn;
        String tscCashNoO;
        String cardStatus;
        String embossStatus;
        String bankNo2;
        String embossDate;
        String tscAmt;
        String tscPledgeAmt;
        String vendorCd;
        String isamSeqNo;
        String isamBatchNo;
        String isamBatchSeq;
        String autoloadAmt;
        String tscSignFlag;
        String areaCode;
        String binType;
        String filler1;
        String hashValue;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(type, 1);
            rtn += comc.fixLeft(attri, 2);
            rtn += comc.fixLeft(txType, 1);
            rtn += comc.fixLeft(tscCashNo, 20);
            rtn += comc.fixLeft(effcYymm, 4);
            rtn += comc.fixLeft(chipSeq, 20);
            rtn += comc.fixLeft(tscCardNo, 20);
            rtn += comc.fixLeft(txRsn, 1);
            rtn += comc.fixLeft(tscCashNoO, 20);
            rtn += comc.fixLeft(cardStatus, 10);
            rtn += comc.fixLeft(embossStatus, 1);
            rtn += comc.fixLeft(bankNo2, 2);
            rtn += comc.fixLeft(embossDate, 8);
            rtn += comc.fixLeft(tscAmt, 5);
            rtn += comc.fixLeft(tscPledgeAmt, 5);
            rtn += comc.fixLeft(vendorCd, 10);
            rtn += comc.fixLeft(isamSeqNo, 8);
            rtn += comc.fixLeft(isamBatchNo, 10);
            rtn += comc.fixLeft(isamBatchSeq, 5);
            rtn += comc.fixLeft(autoloadAmt, 4);
            rtn += comc.fixLeft(tscSignFlag, 1);
            rtn += comc.fixLeft(areaCode, 2);
            rtn += comc.fixLeft(binType, 1);
            rtn += comc.fixLeft(filler1, 69);
            rtn += comc.fixLeft(hashValue, 16);
            rtn += comc.fixLeft(fillerEnd, 2);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        detailSt.type = comc.subMS950String(bytes, 0, 1);
        detailSt.attri = comc.subMS950String(bytes, 1, 2);
        detailSt.txType = comc.subMS950String(bytes, 3, 1);
        detailSt.tscCashNo = comc.subMS950String(bytes, 4, 20);
        detailSt.effcYymm = comc.subMS950String(bytes, 24, 4);
        detailSt.chipSeq = comc.subMS950String(bytes, 28, 20);
        detailSt.tscCardNo = comc.subMS950String(bytes, 48, 20);
        detailSt.txRsn = comc.subMS950String(bytes, 68, 1);
        detailSt.tscCashNoO = comc.subMS950String(bytes, 69, 20);
        detailSt.cardStatus = comc.subMS950String(bytes, 89, 10);
        detailSt.embossStatus = comc.subMS950String(bytes, 99, 1);
        detailSt.bankNo2 = comc.subMS950String(bytes, 100, 2);
        detailSt.embossDate = comc.subMS950String(bytes, 102, 8);
        detailSt.tscAmt = comc.subMS950String(bytes, 110, 5);
        detailSt.tscPledgeAmt = comc.subMS950String(bytes, 115, 5);
        detailSt.vendorCd = comc.subMS950String(bytes, 120, 10);
        detailSt.isamSeqNo = comc.subMS950String(bytes, 130, 8);
        detailSt.isamBatchNo = comc.subMS950String(bytes, 138, 10);
        detailSt.isamBatchSeq = comc.subMS950String(bytes, 148, 5);
        detailSt.autoloadAmt = comc.subMS950String(bytes, 153, 4);
        detailSt.tscSignFlag = comc.subMS950String(bytes, 157, 1);
        detailSt.areaCode = comc.subMS950String(bytes, 158, 2);
        detailSt.binType = comc.subMS950String(bytes, 160, 1);
        detailSt.filler1 = comc.subMS950String(bytes, 161, 69);
        detailSt.hashValue = comc.subMS950String(bytes, 230, 16);
        detailSt.fillerEnd = comc.subMS950String(bytes, 246, 2);
    }

}