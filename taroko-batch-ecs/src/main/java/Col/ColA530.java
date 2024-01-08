/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/10/06  V1.00.00    phopho     program initial                          *
*  109/01/10  V1.00.01    phopho     fix writeTextFile add "\n"               *
*  109/06/18  V1.00.02    phopho     Mantis 0003648: downgrade filename       *
*  109/12/10  V1.00.03    shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.04    Zuwei       “89822222”改為”23317531”            *
*  110/04/06  V1.00.05    Justin     use common value
******************************************************************************/

package Col;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommJcic;
import com.CommRoutine;

import hdata.jcic.JcicEnum;
import hdata.jcic.JcicHeader;
import hdata.jcic.LRPad;

public class ColA530 extends AccessDAO {
	private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_Z582;
    private String progname = "無擔保債務-Z582 媒體產生處理程式  110/04/06  V1.00.05";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    String hCurpModPgm = "";
    String hCurpModTime = "";
    String hCurpModUser = "";
    String hCurpModWs = "";
    long hCurpModSeqno = 0;
    String hCurpModLog = "";
    String buf = "";

    String hBusiBusinessDate = "";
    String hCluoId = "";
    String hCluoIdPSeqno = "";
    String hCluoApplyDate = "";
    String hCluoAgreeFlag = "";
    String hCluoBankCode = "";
    String hCluoAprDate = "";
    String hCluoCloseReason = "";
    String hCluoPSeqno = "";
    String hCluoRowid = "";
    double hAcctAcctJrnlBal = 0;
    int totalCnt = 0;

    private int fptr2 = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : ColA530 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            selectPtrBusinday();
            if ((args.length == 1) && (args[0].length() == 8)) {
                hBusiBusinessDate = args[0];
            }

            fileOpen();
            selectColLiauNego();
            buf = String.format("%s%08d%129.129s", CommJcic.TAIL_LAST_MARK, totalCnt, " ");
            writeTextFile(fptr2, String.format("%s",buf));
            closeOutputText(fptr2);
            ftpProc();

            showLogMessage("I", "", String.format("處理筆數[%d]", totalCnt));
            // ==============================================
            // 固定要做的
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

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
        String temstr2 = String.format("%s/media/col/LIAD/%s%4.4sa.582", comc.getECSHOME(), CommJcic.JCIC_BANK_NO , comc.getSubString(hBusiBusinessDate,4));  //phopho mod 2020.6.18 Mantis 0003648: A -> a
        temstr2 = Normalizer.normalize(temstr2, java.text.Normalizer.Form.NFKD);

        fptr2 = openOutputText(temstr2, "MS950");
        if (fptr2 == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", temstr2), "", hCallBatchSeqno);
        }
        
        // set header
        JcicHeader jcicHeader = new JcicHeader();
        CommJcic commJcic = new CommJcic(getDBconnect(), getDBalias());
        commJcic.selectContactData(JCIC_TYPE);
        
        jcicHeader.setFileId(commJcic.getPadString(JCIC_TYPE.getJcicId(), 18));
        jcicHeader.setBankNo(commJcic.getPadString(CommJcic.JCIC_BANK_NO, 3));
        jcicHeader.setFiller1(commJcic.getFiller(" ", 5));
        jcicHeader.setSendDate(commJcic.getPadString(comcr.str2long(hBusiBusinessDate) - 19110000, "0", 7, LRPad.L));
        jcicHeader.setFileExt(commJcic.getPadString("01", "0", 2, LRPad.L));
        jcicHeader.setFiller2(commJcic.getFiller(" ", 10)); 
        jcicHeader.setContactTel(commJcic.getPadString(commJcic.getContactTel(), 16));
        jcicHeader.setContactMsg(commJcic.getPadString(commJcic.getContactMsg(), 80));
        jcicHeader.setFiller3("");
        jcicHeader.setLen("");
        
        buf = jcicHeader.produceStr();
        //

//        buf = String.format("%-18.18s017%5.5s%07d01%10.10s%-16.16s%-80.80s", "JCIC-DAT-Z582-V01-", " ",
//                comcr.str2long(hBusiBusinessDate) - 19110000, " ", "02-23317531#2320", "審查單位聯絡人-王琇卿");
        
        
        writeTextFile(fptr2, String.format("%s",buf + "\n"));  //base level bug
    }

    /***********************************************************************/
    void selectColLiauNego() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_no,";
        sqlCmd += "id_p_seqno,";
        sqlCmd += "apply_date,";
        sqlCmd += "agree_flag,";
        sqlCmd += "bank_code,";
        sqlCmd += "apr_date,";
        sqlCmd += "close_reason,";
        sqlCmd += "p_seqno,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_liau_nego ";
        sqlCmd += "where report_date2 = '' ";
        sqlCmd += "and decode(proc_flag,'','x',proc_flag) != 'Y' ";
        sqlCmd += "and apr_date <> '' ";

        openCursor();
        while (fetchTable()) {
            hCluoId = getValue("id_no");
            hCluoIdPSeqno = getValue("id_p_seqno");
            hCluoApplyDate = getValue("apply_date");
            hCluoAgreeFlag = getValue("agree_flag");
            hCluoBankCode = getValue("bank_code");
            hCluoAprDate = getValue("apr_date");
            hCluoCloseReason = getValue("close_reason");
            hCluoPSeqno = getValue("p_seqno");
            hCluoRowid = getValue("rowid");

            selectActAcct();
            buf = String.format("582A%s%-10.10s%07d%3.3s%5.5s3%-50.50s%07d%09.0f%1.1s%30.30s", 
            		CommJcic.JCIC_BANK_NO, hCluoId,
                    comcr.str2long(hCluoApplyDate) - 19110000, hCluoBankCode, " ", hCluoPSeqno,
                    comcr.str2long(hCluoAprDate) - 19110000, hAcctAcctJrnlBal, hCluoCloseReason, " ");
            writeTextFile(fptr2, String.format("%s",buf + "\n"));  //base level bug
            totalCnt++;

            if (hCluoAgreeFlag.equals("Y"))
                updateColLiauNegoAct();
            updateColLiauNego();
        }
        closeCursor();
    }

    /***********************************************************************/
    void updateColLiauNego() throws Exception {
        daoTable = "col_liau_nego";
        updateSQL = "proc_flag = 'Y',";
        updateSQL += " proc_date = ?,";
        updateSQL += " report_date2 = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, javaProgram);
        setRowId(4, hCluoRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liau_nego not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateColLiauNegoAct() throws Exception {
        daoTable = "col_liau_nego_act a";
        updateSQL = "payment_rate1 = (select payment_rate1 from act_acno ";
//        whereStr = "where p_seqno = a.p_seqno), mod_time = sysdate, mod_pgm = ? where id_no = ? ";
        whereStr = "where acno_p_seqno = a.p_seqno), mod_time = sysdate, mod_pgm = ? ";
        whereStr += "where id_p_seqno = ? and apply_date = ? ";
        setString(1, javaProgram);
//        setString(2, h_cluo_id);
        setString(2, hCluoIdPSeqno);
        setString(3, hCluoApplyDate);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_liau_nego_act a not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void selectActAcct() throws Exception {
        hAcctAcctJrnlBal = 0;
        sqlCmd = "select sum(a.acct_jrnl_bal) acct_jrnl_bal ";
        sqlCmd += " from act_acct a, act_acno b ";
//        sqlCmd += "where a.p_seqno = b.p_seqno ";
        sqlCmd += "where a.p_seqno = b.acno_p_seqno ";
        sqlCmd += "and b.acno_flag <> 'Y' ";
        sqlCmd += "and b.id_p_seqno = ? ";
//        setString(1, h_cluo_id);
        setString(1, hCluoIdPSeqno);
        
        extendField = "act_acct.";
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hAcctAcctJrnlBal = getValueDouble("act_acct.acct_jrnl_bal");
        }
    }

    /***********************************************************************/
    @SuppressWarnings("unused")
    void ftpProc() throws Exception {
        String tojcicmsg = "";
        boolean retCode;
        // ======================================================
        // FTP

        CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

        commFTP.hEflgTransSeqno = String.format("%10d", comcr.getModSeq()); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId   = "JCIC_FTP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEflgGroupId    = "Z582";     /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "TOJCIC";   /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/col/LIAD", comc.getECSHOME());
        ;
        commFTP.hEflgModPgm = this.getClass().getName();
        String hEflgRefIpCode = "JCIC_FTP";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("mput %s%4.4sA.582", CommJcic.JCIC_BANK_NO, comc.getSubString(hBusiBusinessDate,4));
        showLogMessage("I", "", procCode + " " + hEflgRefIpCode + " 開始上傳....");

        int errCode = commFTP.ftplogName(hEflgRefIpCode, procCode);
        
        //phopho add 2019.5.27 無檔案的return message須自己判斷
        if (errCode == 0 && commFTP.fileList.size() == 0) {
        	showLogMessage("I", "", String.format("[%-20.20s] => [無資料可傳送]", procCode));
        }

        if (errCode != 0) {
            showLogMessage("I", "", String.format("[%s]檔案傳送JCIC_FTP有誤(error), 請通知相關人員處理", procCode));

            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"" + javaProgram + "執行完成 傳送JCIC失敗[%s]\"",
                    procCode);
            retCode = comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", String.format("%s [%s]", tojcicmsg, retCode));
        } else {
            tojcicmsg = String.format("/ECS/ecs/shell/SENDMSG.sh 1 \"" + javaProgram + "執行完成 傳送JCIC無誤[%s]\"",
                    procCode);
            retCode = comc.systemCmd(tojcicmsg);
            showLogMessage("I", "", String.format("%s [%s]", tojcicmsg, retCode));
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColA530 proc = new ColA530();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
