/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/08/15  V1.00.00    phopho     program initial                          *
*  109/12/13  V1.00.01    shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.02    Zuwei       “89822222”改為”23317531”            *
*  110/04/06  V1.00.03    Justin     use common value                         *
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

public class ColB110 extends AccessDAO {
    private String progname = "每月報送JCIC(S01)處理程式 110/04/06  V1.00.03  ";
    private final JcicEnum JCIC_TYPE = JcicEnum.JCIC_S01;
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hBusiBusinessDate = "";
    String hTempLastMonth = "";
    String hCjs1IdNo = "";
    String hCjs1ReportMonth = "";
    String hCjs1AcctType = "";
    String hCjs1AcctStatus = "";
    String hCjs1PaymentNo = "";
    String hCjs1McodeStatus = "";
    String hCjs1ReportStatus = "";
    String hCjs1Rowid = "";
    double hCjs1AcctJrnlBal = 0;
    
    String hCallBatchSeqno = "";
    String buf = "";
    int totalCnt = 0;

    private int fptr1 = 0;

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
                comc.errExit("Usage : ColB110 [business_date] [callbatch_seqno]", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            //test: Heuristic SQL Injection
//            h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
            String sGBatchSeq = "";
            sGBatchSeq = args.length > 0 ? args[args.length - 1] : "";
            sGBatchSeq = Normalizer.normalize(sGBatchSeq, java.text.Normalizer.Form.NFKD);
            hCallBatchSeqno = sGBatchSeq;
            
            //online call batch 時須記錄
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);

            hBusiBusinessDate = "";
            if ((args.length >= 1) && (args[0].length() == 8)) {
                hBusiBusinessDate = args[0];
            }
            selectPtrBusinday();

            fileOpen();
            selectColJcicS01();
            buf = String.format("%s%08d%116.116s", CommJcic.TAIL_LAST_MARK, totalCnt, " ");
            writeTextFile(fptr1, String.format("%s",buf));
            closeOutputText(fptr1);
            ftpProc();

            showLogMessage("I", "", String.format("累計處理筆數[%d]", totalCnt));
            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
            	comcr.callbatch(1, 0, 0);
            
//            showLogMessage("I", "", "程式執行結束");
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
    	hTempLastMonth = "";
    	
        sqlCmd = "select decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))) business_date, ";
        sqlCmd += "to_char(add_months(to_date(decode(cast(? as varchar(8)),'',business_date,cast(? as varchar(8))),'yyyymmdd'),-1),'yyyymm') last_month ";
        sqlCmd += "from ptr_businday ";
        sqlCmd += "fetch first 1 row only ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hTempLastMonth = getValue("last_month");
        }
    }

    /***********************************************************************/
    void fileOpen() throws Exception {
    	String temstr1 = String.format("%s/media/col/%s%07dS.s01", comc.getECSHOME(), CommJcic.JCIC_BANK_NO, 
    			comcr.str2long(hBusiBusinessDate) - 19110000);
    	temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    	
    	fptr1 = openOutputText(temstr1, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("error: [%s]在程式執行目錄下沒有權限讀寫", temstr1), "", hCallBatchSeqno);
        }
        
        JcicHeader jcicHeader = new JcicHeader();
        CommJcic commJcic = new CommJcic(getDBconnect(), getDBalias());
        commJcic.selectContactData(JCIC_TYPE);
        
        jcicHeader.setFileId(commJcic.getPadString(JCIC_TYPE.getJcicId(), 18));
        jcicHeader.setBankNo(commJcic.getPadString(CommJcic.JCIC_BANK_NO, 3));
        jcicHeader.setFiller1(commJcic.getFiller(" ", 5));
        jcicHeader.setSendDate(commJcic.getPadString(comcr.str2long(hBusiBusinessDate) - 19110000, "0", 7, LRPad.L));
        jcicHeader.setFileExt("01");
        jcicHeader.setFiller2(commJcic.getFiller(" ", 10)); 
        jcicHeader.setContactTel(commJcic.getPadString(commJcic.getContactTel(), 16));
        jcicHeader.setContactMsg(commJcic.getPadString(commJcic.getContactMsg(), 67));
        jcicHeader.setFiller3("");
        jcicHeader.setLen("");
        
        buf = jcicHeader.produceStr();

//        buf = String.format("%-18.18s017%5.5s%07d01%10.10s%-16.16s%-67.67s", "JCIC-DAT-S001-V01-", " ",
//                comcr.str2long(hBusiBusinessDate) - 19110000, " ", "02-23317531#2327", "審查單位聯絡人-陳柔伊");
        
        writeTextFile(fptr1, String.format("%s",buf));
    }

    /***********************************************************************/
    void selectColJcicS01() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "id_no,";
        sqlCmd += "report_month,";
        sqlCmd += "acct_type,";
        sqlCmd += "decode(acct_status,'3','A','4','B',' ') as acct_status,";
        sqlCmd += "payment_no,";
        sqlCmd += "mcode_status,";
        sqlCmd += "report_status,";
        sqlCmd += "acct_jrnl_bal,";
        sqlCmd += "rowid as rowid ";
        sqlCmd += "from col_jcic_s01 ";
        sqlCmd += "where report_month = ? ";
        sqlCmd += "order by id_no ";
        setString(1, hTempLastMonth);

        openCursor();
        while (fetchTable()) {
        	hCjs1IdNo = getValue("id_no");
        	hCjs1ReportMonth = getValue("report_month");
        	hCjs1AcctType = getValue("acct_type");
        	hCjs1AcctStatus = getValue("acct_status");
        	hCjs1PaymentNo = getValue("payment_no");
        	hCjs1McodeStatus = getValue("mcode_status");
        	hCjs1ReportStatus = getValue("report_status");
        	hCjs1AcctJrnlBal = getValueDouble("acct_jrnl_bal");
        	hCjs1Rowid = getValue("rowid");
        	
        	buf = String.format("A%s    %10.10s2%9.9s%1.1s%2.2s%-48.48s%1.1s%1.1s%09.0f%05d%55.55s",
        			CommJcic.JCIC_BANK_NO, 
                    hCjs1IdNo, " ", hCjs1AcctStatus, hCjs1AcctType, hCjs1PaymentNo,
                    hCjs1McodeStatus, hCjs1ReportStatus, hCjs1AcctJrnlBal,
        			comcr.str2long(hCjs1ReportMonth) - 191100, " ");
            writeTextFile(fptr1, String.format("%s",buf));
            
            totalCnt++;
            processDisplay(10000); // every nnnnn display message
            updateColJcicS01();
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void updateColJcicS01() throws Exception {
        daoTable = "col_jcic_s01";
        updateSQL = " proc_date = ?,";
        updateSQL += " mod_time = sysdate,";
        updateSQL += " mod_pgm  = ? ";
        whereStr = "where rowid = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hCjs1Rowid);
        
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_col_jcic_s01 error!", "", hCallBatchSeqno);
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
        commFTP.hEflgGroupId    = "S01";      /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "TOJCIC";   /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEriaLocalDir = String.format("%s/media/col", comc.getECSHOME());
        ;
        commFTP.hEflgModPgm = this.getClass().getName();
        String hEflgRefIpCode = "JCIC_FTP";

        System.setProperty("user.dir", commFTP.hEriaLocalDir);

        String procCode = String.format("mput %s%07dS.s01", CommJcic.JCIC_BANK_NO, comcr.str2long(hBusiBusinessDate) - 19110000);
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
        ColB110 proc = new ColB110();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
