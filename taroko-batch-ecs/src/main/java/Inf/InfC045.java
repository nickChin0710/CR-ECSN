/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version      AUTHOR                DESCRIPTION                *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  110/08/20   V1.00.00     Yang Bo                  initial	              *
 *****************************************************************************/

package Inf;

import com.*;

public class InfC045 extends BaseBatch {
    private final String progname = "產生送CRDB 45異動帳單寄送註記資料檔程式 110/08/20 V1.00.00";
    CommCrd comc = new CommCrd();
    CommString commString = new CommString();
    CommFTP commFTP = null;
    CommRoutine comr = null;
    String isFileName = "";
    private int ilFile45;
    CommDate  commDate = new CommDate();
    String hSysDate = "";
    String hBusiBusinessDate = "";
    String hCardNo = "";
    String hIdNo = "";
    String hCrrSmsa = "";

    public static void main(String[] args) {
        InfC045 proc = new InfC045();
        proc.mainProcess(args);
        proc.systemExit();
    }

    @Override
    protected void dataProcess(String[] args) throws Exception {
        dspProgram(progname);
        dateTime();
        int liArg = args.length;
        if (liArg > 1) {
            printf("Usage : InfC045 [business_date]");
            errExit(1);
        }
        dbConnect();
        if (liArg == 1) {
            hSysDate = args[0];
        }
        if (empty(hSysDate)) {
            hSysDate = hBusiDate;
        }

        hSysDate = commDate.dateAdd(hSysDate, 0, 0, -1) ;
        isFileName = "CRU23B1_TYPE_45_" + hSysDate + ".txt";
        checkOpen();
        selectDataType45();
        closeOutputText(ilFile45);
        commFTP = new CommFTP(getDBconnect(), getDBalias());
        comr = new CommRoutine(getDBconnect(), getDBalias());
        procFTP();
        renameFile();
        endProgram();
    }

    void selectDataType45() throws Exception {
        getBusinessDay();

        sqlCmd = "select " +
                    "b.card_no as card_nmbr, c.id_no as id_nmbr, a.bill_apply_flag, " +
                    "stat_send_internet, stat_send_s_month, stat_send_e_month, " +
                    "stat_unprint_flag, stat_unprint_s_month, stat_unprint_e_month, " +
                    "case " +
                        "when bill_apply_flag = '1' then '0001' " +
                        "when bill_apply_flag = '2' then '0002' " +
                        "when bill_apply_flag = '3' then '0003' " +
                        "when stat_send_internet = 'Y' and stat_send_s_month||'01' = ? then '0004' " +
                        "when stat_unprint_flag = 'Y' and stat_unprint_s_month||'01' = ? then '0005' " +
                    "end as crrsmsa " +
                "from dba_acno a " +
                    "left join dbc_card b on a.p_seqno = b.p_seqno " +
                    "left join dbc_idno c on a.id_p_seqno = c.id_p_seqno " +
                "where " +
                    "(" +
                        "stat_send_internet = 'Y' " +
                        "and " +
                        "(" +
                            "stat_send_s_month||'01' = ? " +
                            "or " +
                            "case when stat_send_e_month = '999912' then stat_send_e_month||'01' " +
                                "when stat_send_e_month = '' then '' " +
                                "else to_char(next_month(date(stat_send_e_month||'01')), 'yyyymmdd') end = ?" +
                        ")" +
                    ") " +
                    "or " +
                    "(" +
                        "stat_unprint_flag = 'Y' " +
                        "and " +
                        "(" +
                            "stat_unprint_s_month||'01' = ? " +
                            "or " +
                            "case when stat_unprint_e_month = '999912' then stat_unprint_e_month||'01' " +
                                "when stat_unprint_e_month = '' then '' " +
                                "else to_char(next_month(date(stat_unprint_e_month||'01')), 'yyyymmdd') end = ?" +
                        ")" +
                    ") " +
                "order by a.acct_type, a.stmt_cycle, a.p_seqno";
        setString(1, hSysDate);
        setString(2, hSysDate);
        setString(3, hSysDate);
        setString(4, hSysDate);
        setString(5, hSysDate);
        setString(6, hSysDate);
        openCursor();
        while (fetchTable()) {
            hCardNo = colSs("card_nmbr");
            hIdNo = commString.rpad(colSs("id_nmbr"), 11);
            hCrrSmsa = colSs("crrsmsa");
            writeTextFile45();
        }
        closeCursor();
    }

    void getBusinessDay() throws Exception {
        hBusiBusinessDate = "";

        sqlCmd = "select business_date from ptr_businday";
        openCursor();
        while (fetchTable()) {
            hBusiBusinessDate = getValue("business_date");
        }
        closeCursor();
    }

    void checkOpen() throws Exception {
        String lsTemp = "";
        lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
        ilFile45 = openOutputText(lsTemp, "big5");
        if (ilFile45 < 0) {
            printf("CRU23B1-TYPE-45 產檔失敗 ! ");
            errExit(1);
        }
    }

    void writeTextFile45() throws Exception {
        StringBuffer tempBuf = new StringBuffer();
        String tmpStr = "", newLine = "\r\n";
        tempBuf.append("45"); // --代碼 固定 45
        tempBuf.append(comc.fixLeft(hCardNo, 16)); // --卡號
        tempBuf.append(comc.fixLeft(hIdNo, 11)); // --主身分證 11 碼
        tempBuf.append(comc.fixLeft(hCrrSmsa, 4)); // --帳單寄送註記 4 碼
        tempBuf.append(comc.fixLeft("", 117)); // --保留 117
        tempBuf.append(newLine);
        totalCnt++;
        this.writeTextFile(ilFile45, tempBuf.toString());
    }

    void procFTP() throws Exception {
        // 串聯 log 檔所使用 鍵值 (必要)
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");
        // 區分不同類的 FTP 檔案-大類 (必要)
        commFTP.hEflgSystemId = "NCR2TCB";
        commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
        // 區分不同類的 FTP 檔案-次分類 (非必要)
        commFTP.hEflgGroupId = "000000";
        // 區分不同類的 FTP 檔案-細分類 (非必要)
        commFTP.hEflgSourceFrom = "EcsFtp";
        commFTP.hEflgModPgm = javaProgram;

        showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
            insertEcsNotifyLog(isFileName);
        }
    }

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

    void renameFile() throws Exception {
        String tmpstr1 = String.format("%s/media/crdb/%s", getEcsHome(), isFileName);
        String tmpstr2 = String.format("%s/media/crdb/backup/%s", getEcsHome(), isFileName);

        if (!comc.fileRename2(tmpstr1, tmpstr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]更名失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + isFileName + "] 已移至 [" + tmpstr2 + "]");
    }

}
