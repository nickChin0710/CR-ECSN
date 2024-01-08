/****************************************************************************************************************
 *                                                                                                              *
 *                              MODIFICATION LOG                                                                *
 *                                                                                                              *
 *     DATE     Version    AUTHOR                   DESCRIPTION                                                 *
 *  ---------  --------- ----------- -------------------------------------------------------------------------  *
 *  113/01/05  V1.00.00  Zuwei Su    program initial                                              *             
 *****************************************************************************************************************/
package Mkt;


import com.*;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class MktC920a extends AccessDAO {
    private final String PROGNAME = "金庫幣活動-申辦電子帳單回饋篩選處理 113/01/05 V1.00.00 ";
    CommCrd comc = new CommCrd();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;
    Buf1 data = new Buf1();

    private static final String PATH_FOLDER = "/media/mkt";
    private static final String FILE_NAME_AP4 = "AW-AP4-CRYYYYMMDD.01";
    private static final String FILE_NAME_MPP500 = "MPP500_YYYYMMDD.TXT";
    private final static String LINE_SEPERATOR = System.lineSeparator();
    private final static String MOD_PGM = "MktC920a";

    private String activeType;
    private String activeCode;
    private String purchaseDateS;
    private String purchaseDateE;

    private String hProcDate = "";
    private String hLastSysdate = "";
    private String hLast2Sysdate = "";
    private String hLastSysdateDay1 = "";
    private String hLast2SysdateDay1 = "";

    private long totCnt = 0;
    private int fptr1 = -1;

    private String fmtFileNameAP4 = "";
    private String fmtFileNameMPP500 = "";
    String headerTmpBuf = "";
    List<String> bodyTmpBuf = new ArrayList<>();
    String footerTmpBuf = "";

    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 1) {
//                comc.errExit("Usage : MktC920 [sysdate ex:yyyymmdd] ", "");
                showLogMessage("I", "", "Usage : MktC920a [sysdate ex:yyyymmdd] ");
                return 0;
            }

            // 固定要做的
            if (!connectDataBase()) {
//                comc.errExit("connect DataBase error", "");
                showLogMessage("E", "", "connect DataBase error");
                return 0;
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            String sGArgs0 = "";
            if (args.length == 0){
                hProcDate = selectPtrBusinDay();
            } else if (args.length == 1 && args[0].length() == 8) {
                sGArgs0 = args[0];
                sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                hProcDate = sGArgs0;
            }
            selectLastSysDate();

            showLogMessage("I", "", String.format("輸入參數1 = [%s] ", sGArgs0));
            showLogMessage("I", "", String.format("取得系統日 = [%s] ", hProcDate));
            showLogMessage("I", "", String.format("取得系統日上個月1日 = [%s] ", hLastSysdateDay1));
            showLogMessage("I", "", String.format("取得系統日上個月最後一日 = [%s] ", hLastSysdate));
            showLogMessage("I", "", String.format("取得系統日上上個月1日 = [%s] ", hLast2SysdateDay1));
            showLogMessage("I", "", String.format("取得系統日上上個月最後一日 = [%s] ", hLast2Sysdate));

            if (!fileOpenAP4()) {
                return 0;
            };
            fileOpen2MPP500();
            if (!selectMktGoldBillParm()) {
                return 0;
            }
            selectActAcno();
            writeTextAP4();
            if(!copyFileAP4()) {
                return 0;
            }
            closeOutputText(fptr1);
//            if (!ftpProcAP4()) {
//                return 0;
//            };
            if (!ftpProcMPP500()) {
                return 0;
            };
            renameFileAP4();
            renameFileMPP500();

            showLogMessage("I", "", String.format("Process records = [%d]", totCnt));

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]", totCnt);
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    private String selectPtrBusinDay() throws Exception {
        sqlCmd = " select business_date from ptr_businday where 1=1 ";

        if (selectTable() <= 0) {
//            comc.errExit("PTR_BUSINDAY 無资料!!", "");
            showLogMessage("E", "", "PTR_BUSINDAY 無资料!!");
            exitProgram(-1);
        }

        return getValue("business_date");
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC920a proc = new MktC920a();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    private void selectLastSysDate() throws Exception {
        sqlCmd = "  select to_char(last_day((to_date(?,'yyyymmdd')-1 months)),'yyyymmdd') as h_last_sysdate, ";
        sqlCmd += "        to_char(last_day((to_date(?,'yyyymmdd')-2 months)),'yyyymmdd') as h_last2_sysdate, ";
        sqlCmd += "        to_char((to_date(?,'yyyymmdd')-1 months),'yyyymm') || '01' as h_last_sysdate_day1, ";
        sqlCmd += "        to_char((to_date(?,'yyyymmdd')-2 months),'yyyymm') || '01' as h_last2_sysdate_day1 ";
        sqlCmd += " from ptr_businday ";
        setString(1, hProcDate);
        setString(2, hProcDate);
        setString(3, hProcDate);
        setString(4, hProcDate);
        if (selectTable() > 0) {
            hLastSysdate = getValue("h_last_sysdate");//上個月最後一天
            hLast2Sysdate = getValue("h_last2_sysdate");//上上個月最後一天
            hLastSysdateDay1 = getValue("h_last_sysdate_day1");//上個月第一天
            hLast2SysdateDay1 = getValue("h_last2_sysdate_day1");//上上個月第一天
        }
    }

    /*******************************************************************/
    private boolean fileOpenAP4() throws Exception {
        fmtFileNameAP4 = FILE_NAME_AP4.replace("YYYYMMDD", hProcDate);

        String temstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
        String fileName = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        fptr1 = openOutputText(fileName, "MS950");
        if (fptr1 == -1) {
//            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName), "", comcr.hCallBatchSeqno);
            showLogMessage("I", "", String.format("[%s]在程式執行目錄下沒有權限讀寫", fileName));
            return false;
        }
        return true;
    }

    /*******************************************************************/
    private void fileOpen2MPP500() {
        fmtFileNameMPP500 = FILE_NAME_MPP500.replace("YYYYMMDD", hProcDate);
    }

    private boolean selectMktGoldBillParm() throws Exception {
        sqlCmd = " select active_type, active_code, purchase_date_s, purchase_date_e ";
        sqlCmd += " from mkt_goldbill_parm ";
        sqlCmd += " where (stop_flag <> 'Y' or stop_date >= ?) ";
        sqlCmd += "   and active_type = '1' ";
        sqlCmd += "   and feedback_cycle = 'M' ";
        sqlCmd += "   and ?||feedback_dd = ? ";
        sqlCmd += "   and ? between active_date_s and active_date_e ";
        setString(1, hLastSysdate);
        setString(2, comStr.left(hProcDate, 6));
        setString(3, hProcDate);
        setString(4, hLastSysdate);
        int selectCnt = selectTable();
        if (selectCnt > 0) {
            activeType = getValue("active_type");
            activeCode = getValue("active_code");
            purchaseDateS = getValue("purchase_date_s");
            purchaseDateE = getValue("purchase_date_e");
        } else {
//            comc.errExit("MKT_GOLDBILL_PARM 無符合條件的參數檔", "");
            showLogMessage("E", "", "MKT_GOLDBILL_PARM 無符合條件的參數檔");
            return false;
        }
        return true;
    }

    private void selectActAcno() throws Exception {
        sqlCmd = "  select a.p_seqno, a.id_p_seqno, b.id_no ";
        sqlCmd += " from act_acno a ";
        sqlCmd += " left join crd_idno b ";
        sqlCmd += "      on a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += " left join bil_bill c ";
        sqlCmd += "      on c.p_seqno = a.p_seqno ";
        sqlCmd += "      and c.acct_type = '01' ";
        sqlCmd += "      and c.purchase_date between ? and ? ";
        sqlCmd += "      and c.purchase_date between ? and ? ";
        sqlCmd += "      and c.mcht_no not in ( ";
        sqlCmd += "          select distinct data_code from mkt_mchtgp_data ";
        sqlCmd += "          where table_name = 'MKT_MCHT_GP' ";
        sqlCmd += "            and data_key = 'MKTNCUS00' ";
        sqlCmd += "            and data_type = '1' ";
        sqlCmd += "          ) ";
        sqlCmd += "      and (Substr(c.mcht_chi_name,1,2)) NOT in ('f%', 'G%', 'd%', 'M%', 'b%', 'e%', 'V%', 'A%', '$%', '#%')  ";
        sqlCmd += " where a.stat_send_internet = 'Y' ";
        sqlCmd += "   and a.stat_send_s_month2 = ? ";
        sqlCmd += "   and not exists ( select 1 from mkt_state_internet_h where id_p_seqno = a.id_p_seqno ) "; // 剔除歷史資料 (原系統已回饋者)
        sqlCmd += "   and not exists ( select 1 from mkt_goldbill_list where active_type = '1' and id_p_seqno = a.id_p_seqno ) "; // 剔除已回饋者
        setString(1, purchaseDateS);	//活動參數消費起迄
        setString(2, purchaseDateE);
        setString(3, hLast2SysdateDay1);
        setString(4, hLastSysdate);
        setString(5, comStr.left(hLast2SysdateDay1, 6));
        openCursor();
        while (fetchTable()) {
            data.initData();
            data.pSeqno = getValue("p_seqno");
            data.idNo = getValue("id_no");
            data.idPSeqno = getValue("id_p_seqno");

            bodyTmpBuf.add(data.bodyText());

            insertMktGoldbillList();

            totCnt++;
        }
        showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));
        closeCursor();
    }

    private void insertMktGoldbillList() {
        extendField = "mkt_goldbill_list.";
        setValue("mkt_goldbill_list.active_type", activeType);
        setValue("mkt_goldbill_list.active_code", activeCode);
        setValue("mkt_goldbill_list.id_p_seqno", data.idPSeqno);
        setValue("mkt_goldbill_list.p_seqno", data.pSeqno);
        setValue("mkt_goldbill_list.id_no", data.idNo);
        setValue("mkt_goldbill_list.feedback_date", sysDate);
        setValue("mkt_goldbill_list.mod_time", sysDate + sysTime);
        setValue("mkt_goldbill_list.mod_user", MOD_PGM);
        setValue("mkt_goldbill_list.mod_pgm", MOD_PGM);
        setValueInt("mkt_goldbill_list.mod_seqno", 1);
        daoTable = "mkt_goldbill_list";
        try {
            insertTable();
        } catch (Exception ex) {
            showLogMessage("E", "", "insert MKT_GOLDBILL_LIST error");
        }
    }

    private void writeTextAP4() throws Exception {
        //表頭
        headerTmpBuf = data.headerText();
        writeTextFile(fptr1, headerTmpBuf);

        //明細
        for (int i = 0; i < bodyTmpBuf.size(); i++) {
            writeTextFile(fptr1, bodyTmpBuf.get(i));
        }

        //表尾
        footerTmpBuf = data.footerText();
        writeTextFile(fptr1, footerTmpBuf);
    }

    private boolean  copyFileAP4() {
        String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
        String tmpstr2 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameMPP500);

        if (!comc.fileCopy(tmpstr1, tmpstr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + fmtFileNameAP4 + "]COPY失敗!");
            return false;
        }
        showLogMessage("I", "", "檔案 [" + fmtFileNameAP4 + "] 已COPY至 [" + tmpstr2 + "]");
        return true;
    }

    /*******************************************************************/
    private boolean ftpProcAP4() throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        /**********
         * COMM_FTP common function usage
         ****************************************/
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
        //commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgSystemId = "CRDATACREA"; /* 區分不同類的 FTP 檔案-大類 (必要) */	//20230911, grace
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), PATH_FOLDER);
        commFTP.hEflgModPgm = javaProgram;
        
        showLogMessage("I", "", "                                                   ");
        showLogMessage("I", "", "===================================================");
        showLogMessage("I", "", "mput " + fmtFileNameAP4 + " 開始傳送....");
        //int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fmtFileNameAP4);
        int errCode = commFTP.ftplogName("CRDATACREA", "mput " + fmtFileNameAP4);	//20230911, grace

        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileNameAP4 + " 資料" + " errcode:" + errCode);
            return false;
        }
        return true;
    }

    /*******************************************************************/
    private boolean ftpProcMPP500() throws Exception {
        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        /**********
         * COMM_FTP common function usage
         ****************************************/
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s%s", comc.getECSHOME(), PATH_FOLDER);
        commFTP.hEflgModPgm = javaProgram;

        showLogMessage("I", "", "                                                   ");
        showLogMessage("I", "", "===================================================");
        showLogMessage("I", "", "mput " + fmtFileNameMPP500 + " 開始傳送....");
        int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fmtFileNameMPP500);

        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileNameMPP500 + " 資料" + " errcode:" + errCode);
            return false;
        }
        return true;
    }

    private void renameFileAP4() {
        String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4);
        String tmpstr2 = String.format("%s%s/backup/%s.%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameAP4, sysDate + sysTime);

        if (!comc.fileRename2(tmpstr1, tmpstr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + fmtFileNameAP4 + "]備份失敗!");
            return;
        }
        showLogMessage("I", "", "                                                   ");
        showLogMessage("I", "", "===================================================");
        showLogMessage("I", "", "檔案 [" + fmtFileNameAP4 + "] 已移至 [" + tmpstr2 + "]");
    }

    private void renameFileMPP500() {
        String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameMPP500);
        String tmpstr2 = String.format("%s%s/backup/%s.%s", comc.getECSHOME(), PATH_FOLDER, fmtFileNameMPP500, sysDate + sysTime);

        if (!comc.fileRename2(tmpstr1, tmpstr2)) {
            showLogMessage("I", "", "ERROR : 檔案[" + fmtFileNameMPP500 + "]備份失敗!");
            return;
        }
        showLogMessage("I", "", "檔案 [" + fmtFileNameMPP500 + "] 已移至 [" + tmpstr2 + "]");
    }

    class Buf1 {
        String idNo;
        String idPSeqno = "";
        String pSeqno = "";

        void initData() {
            idNo = "";
            idPSeqno = "";
            pSeqno = "";
        }

        String headerText() throws Exception {
            return comc.fixLeft("TXH", 3) + // 檔頭字串 固定值’TXH’
                    comc.fixLeft(sysDate, 8) + // 資料日期(產檔日) 系統日期YYYYMMDD
                    comc.fixLeft("AP4", 5) + // 處理的資訊單位代碼 固定值’AP4’
                    comc.fixLeft("CR", 5) + // 需求部門代碼 固定值’CR’
                    comc.fixLeft("01", 2) + // 檔名的序號 固定值’01’
                    comc.fixLeft(" ", 377) + // 保留欄位 空白
                    LINE_SEPERATOR;
        }

        String bodyText() throws Exception {
            return comc.fixLeft("TXD", 3) + // 資料字串 固定值’TXD’
                    comc.fixLeft("CRBILL01", 10) + // 交易類型 固定值’CRBILL01’
                    comc.fixLeft(sysDate, 8) + // 交易日期 系統日期YYYYMMDD
                    comc.fixLeft(String.format("%06d", 0), 6) + // 交易時間 固定值’000000’
                    comc.fixLeft(idNo, 30) + // 交易序號 mkt_goldbill_list.id_no
                    comc.fixLeft(idNo, 10) + // 身份證號或護照號 mkt_goldbill_list.id_no
                    comc.fixLeft(" ", 46) + // 銀行轉出入帳戶 空白
                    comc.fixLeft(String.format("%016d", 0), 16) + // 交易金額 固定值, 16個’0’
                    comc.fixLeft(String.format("%030d", 0), 30) + // 原幣金額 固定值, 30個’0’
                    comc.fixLeft(" ", 83) + // 匯款、信託、家族相關欄位 空白
                    comc.fixLeft(String.format("%024d", 0), 24) + // 信託資產 固定值, 24個’0’
                    comc.fixLeft(String.format("%024d", 0), 24) + // 整戶往來總資產 固定值, 24個’0’
                    comc.fixLeft(" ", 55) + // 保留欄位 空白
                    comc.fixLeft(String.format("%07d500", 0), 10) + // 專案紅利點數 固定值, 7個’0’+’500’
                    comc.fixLeft(String.format("%010d", 0), 10) + // Income收入 固定值, 10個’0’
                    comc.fixLeft(" ", 1) + // DG_FLAG 空白
                    comc.fixLeft(" ", 34) + // Data_spaces 空白
                    LINE_SEPERATOR;
        }

        String footerText() throws Exception {
            return comc.fixLeft("TXE", 3) + // 檔尾字串 固定值’TXE’
                    comc.fixLeft("0000", 4) + // 紅利帳號 固定值’0000’
                    comc.fixLeft("0000", 4) + // 固定值’0000’
                    comc.fixLeft(" ", 389);
        }
    }
}
