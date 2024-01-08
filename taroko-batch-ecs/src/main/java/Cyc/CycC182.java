/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  107/04/19  V1.01.01  林志鴻                   ECS-s1070205-013 program initial           *
 *  107/05/08  V1.01.02  Hesyuan     ECS-s1070205-013 壓縮加密                                                     *
 *  107/09/19  V1.01.03  David       transfer to JAVA                          *
 *  109-12-22  V1.01.04  tanwei      updated for project coding standard       *
 ******************************************************************************/

package Cyc;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class CycC182 extends AccessDAO{

    public final boolean debug = false;

    private String progname = "消費款轉聯名主紅利產生檔案處理程式  109/12/22  V1.01.04";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    private String hBusiBusinessDate = "";
    private String hParmZipPswd = "";
    private String hTempYesterday = "";

    private int fptr1 = -1;
    private long totalCount = 0;
    private long totalAll = 0;

    private String filename;

    private String hTempBusinessDate = "";
    private String hTempCobrandCode = "";

    private String hFundCobrandCode = "";
    private String hCofdAcctKey = "";
    private String hCofdProcDate = "";
    private double hCofdFundAmt = 0;
    private String hCofdProgramCode = "";
    private String hCofdAcctMonth = "";
    private String hCofdMajorCardNo = "";

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : CycC182 [[business_date]/[cobrand_code]] ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

            if (args.length == 1) {
                if (args[0].length() == 8) { hTempBusinessDate = args[0]; }
                else { hTempCobrandCode = args[0]; }
            }

            selectPtrBusinday();

            showLogMessage("I", "", "====================================");
            showLogMessage("I", "", String.format("參數[%d] ",args.length));
            for (int inti = 0; inti < args.length; inti++) showLogMessage("I", "", String.format("[%s] ",args[inti]));
            showLogMessage("I", "", "====================================");

            selectPtrFundp();

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = String.format("程式執行結束" );
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    /*****************************************************************************/
    private void selectPtrBusinday() throws Exception {
        sqlCmd = "SELECT decode(cast(? as varchar(8)), '', business_date, cast(? as varchar(8))) as h_busi_business_date, ";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(8)), '', business_date, cast(? as varchar(8))),'yyyymmdd')-1 days,'yyyymmdd') as h_temp_yesterday, ";
        sqlCmd += "'MEGA' ||decode(cast(? as varchar(8)), '', business_date, cast(? as varchar(8))) ||'OPAY' as h_parm_zip_pswd ";
        sqlCmd += " FROM   ptr_businday ";
        setString(1, hTempBusinessDate);
        setString(2, hTempBusinessDate);
        setString(3, hTempBusinessDate);
        setString(4, hTempBusinessDate);
        setString(5, hTempBusinessDate);
        setString(6, hTempBusinessDate);
        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("h_busi_business_date");
        hTempYesterday = getValue("h_temp_yesterday");
        hParmZipPswd = getValue("h_parm_zip_pswd");

        showLogMessage("I", "", String.format("Business_date[%s]", hBusiBusinessDate));
    }
    /*************************************************************************/
    private void selectPtrFundp() throws Exception {
        sqlCmd = "SELECT  distinct cobrand_code ";
        sqlCmd += "FROM    ptr_fundp ";
        sqlCmd += "WHERE   valid_period = 'E' ";  //基金產生方式 Y.本行基金   E.聯名主紅利   S.聯名主計算
        sqlCmd += "AND     cobrand_code = decode(cast(? as varchar(10)), '', cobrand_code,cast(? as varchar(10))) ";
        setString(1, hTempCobrandCode);
        setString(2, hTempCobrandCode);
        openCursor();
        while (fetchTable()) {
            hFundCobrandCode = getValue("cobrand_code");

            checkOpen();
            /*** 1.1撈檔案 ***/
            selectCycCobrandFund();
            /*** 關閉檔案 ***/
            closeOutputText(fptr1);
            if (totalAll > 0)  {
                /*** 1.2檔案PKZIP壓縮加密 ***/

                showLogMessage("I", "", String.format("password = %s",hParmZipPswd));

                /*** PKZIP 壓縮 ***/
                if(debug) showLogMessage("I", "", String.format("壓縮 str=[%s]", filename));
                String zipFile = String.format("%s/media/%s/%s_raise_%8.8s.zip", comc.getECSHOME(), hFundCobrandCode, hFundCobrandCode, hBusiBusinessDate);
                zipFile = Normalizer.normalize(zipFile, java.text.Normalizer.Form.NFKD);
                int tmpInt = comm.zipFile(filename, zipFile, hParmZipPswd);
                if (tmpInt == 0) {
                    /*** 1.3上傳至TM系統 ***/
                    ftpProc();
                }
            }
        }
        closeCursor();
    }
    /*****************************************************************************/
    private void selectCycCobrandFund() throws Exception {
        String outData = "";
        showLogMessage("I", "", String.format("business_date = [%s] yesterday = [%s]", hBusiBusinessDate, hTempYesterday));

        sqlCmd = "SELECT  UF_ACNO_KEY(a.p_seqno) acct_key, ";
        sqlCmd += "        a.proc_date, ";
        sqlCmd += "        a.fund_amt, ";
        sqlCmd += "        a.program_code, ";
        sqlCmd += "        a.acct_month, ";
        sqlCmd += "        a.major_card_no ";
        sqlCmd += "FROM    cyc_cobrand_fund a, ptr_fundp b ";
        sqlCmd += "WHERE   a.program_code = b.FUND_CODE ";
        sqlCmd += "and     a.cobrand_code = ? ";
        sqlCmd += "and     b.valid_period = 'E' ";
        sqlCmd += "and     a.proc_date = decode(b.feedback_type,'1',cast(? as varchar(8)),cast(? as varchar(8))) ";
        setString(1, hFundCobrandCode);
        setString(2, hBusiBusinessDate);
        setString(3, hTempYesterday);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCofdAcctKey = getValue("acct_key", i);
            hCofdProcDate = getValue("proc_date", i);
            hCofdFundAmt = getValueDouble("fund_amt", i);
            hCofdProgramCode = getValue("program_code", i);
            hCofdAcctMonth = getValue("acct_month", i);
            hCofdMajorCardNo = getValue("major_card_no", i);

            totalAll++;

            outData = String.format("%10.10s%8.8s%11.0f%4.4s%6.6s%4.4s\n",
                    hCofdAcctKey, hCofdProcDate, hCofdFundAmt,
                    hCofdProgramCode, hCofdAcctMonth, comc.getSubString(hCofdMajorCardNo, 12));
            writeTextFile(fptr1, outData);

            totalCount++;
            if (totalCount % 25000 ==0) showLogMessage("I", "", String.format("    處理筆數 [%d]", totalCount));

        }
    }
    /*******************************************************************/
    private void checkOpen() throws Exception {
        String temstr1 = String.format("%s/media/%s/%s_raise_%s.txt", comc.getECSHOME(), hFundCobrandCode, hFundCobrandCode, hBusiBusinessDate);
        filename = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        showLogMessage("I","", "Write File : " + filename);
        comc.mkdirsFromFilenameWithPath(filename);
        fptr1 = openOutputText(filename, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫",filename), "", comcr.hCallBatchSeqno);
        }
    }
    /*******************************************************************/
    private void ftpProc() throws Exception {

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */

        /********** COMM_FTP common function usage ****************************************/
        commFTP.hEflgSystemId = String.format("%s_SFTP", hFundCobrandCode); /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId = "raise";                                        /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = hFundCobrandCode;                         /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/%s", comc.getECSHOME(), hFundCobrandCode);
        commFTP.hEflgModPgm = javaProgram;
        String hEflgRefIpCode = commFTP.hEflgSystemId;

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("put %s_raise_%8.8s.zip", hFundCobrandCode, hBusiBusinessDate);
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);

        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s]檔案傳送%s有誤(error), 請通知相關人員處理\n", procCode, hEflgRefIpCode));
        }
        /*** SENDMSG ***/
        String cmdStr = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \" %s 執行完成 傳送%s %s [%s]\"",
                javaProgram, hEflgRefIpCode, errCode == 0 ? "無誤" : "失敗" , procCode);
        showLogMessage("I", "",  cmdStr);
//        boolean ret_code = comc.systemCmd(cmd_str);
//        showLogMessage("I", "", String.format("%s [%d]", cmd_str, ret_code == true ? 0 : 1));
        /********** COMM_FTP common function usage ****************************************/
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CycC182 proc = new CycC182();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

}
