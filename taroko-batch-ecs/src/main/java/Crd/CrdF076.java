/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/03/01  V1.00.00    Ryan       program initial                                         * 
*  112/04/19  V1.00.01    Ryan       增加ISSUE_DATE = 系統日(可輸入參數)                                          * 
*  112/06/15  V1.00.02    Wilson     判斷新卡開卡才送                                                                                   * 
*  112/08/09  V1.00.03    Wilson     調整檔案格式                                                                                            * 
*  112/08/12  V1.00.04    Wilson     1~7碼放日期(民國年)                      * 
******************************************************************************/

package Crd;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CrdF076 extends AccessDAO {
    private String progname = "產生轉卡通知檔送主機程式   112/08/12  V1.00.04 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommCol comCol = null;
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommString comStr = new CommString();
	private final static String NEW_LINE = "\r\n";
	private final static String FILE_NAME = "chg_card.txt";
    String hProcDate = "";
    String hChiYymmdd = "";
    String buf = "";
    int out = -1;
    
    /*******TABLE:CRD_CARD & CRD_IDNO********/
    String hTmpAcctNo = "";
    String hOldCardNo = "";
    String hNewCardNo = "";
    String hIdNo = "";
    String hChiName = "";
    String hIssueDate = "";
    String hHomeAreaCode1 = "";
    String hHomeTelNo1 = "";
    
    int totalCnt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comCol = new CommCol(getDBconnect(), getDBalias());
        	commFTP = new CommFTP(getDBconnect(), getDBalias());
        	comr = new CommRoutine(getDBconnect(), getDBalias());
            
        	selectPtrBusinday();
        	
        	String parm1 = "";
        	if(args.length==1&&args[0].length()==8) {
        		parm1 = args[0];
        		hProcDate = parm1;
        	}
        	
    		hChiYymmdd = "";
        	hChiYymmdd = commDate.toTwDate(hProcDate);
        	        	
        	showLogMessage("I", "", "輸入參數1 = [" + parm1 + "]");
    		showLogMessage("I", "", "取得系統日 =  [" + hProcDate + "]");
    		
            checkOpen();
            selectCrdCard();
            procFTP();
            renameFile();
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
		hProcDate = "";

        sqlCmd = "select to_char((sysdate-1 days),'yyyymmdd') as h_proc_date ";
        sqlCmd += " from dual ";
        if (selectTable() > 0) {
            hProcDate = getValue("h_proc_date");
        }                
	}
    
    /***********************************************************************/
    private void selectCrdCard() throws Exception {
        sqlCmd = "SELECT A.COMBO_ACCT_NO AS TMP_ACCT_NO,A.OLD_CARD_NO,A.CARD_NO,B.ID_NO,B.CHI_NAME,A.ISSUE_DATE,B.HOME_AREA_CODE1,B.HOME_TEL_NO1 ";
        sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,CCA_CARD_OPEN C ";
        sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.CARD_NO = C.CARD_NO ";
        sqlCmd += " AND A.OLD_CARD_NO <>'' AND C.OPEN_DATE = ?  ";
        setString(1,hProcDate);
        openCursor();
        while (fetchTable()) {
        	totalCnt++;
        	initialData();
        	getColCsRptData();
        	buf = procText();
            writeTextFile(out, buf);
            commitDataBase();
        }
        showLogMessage("I", "", String.format("累計處理筆數[%d] ", totalCnt));
        closeCursor();
        closeOutputText(out);
    }
    
    private String procText() throws UnsupportedEncodingException {
   	 StringBuilder textSb = new StringBuilder();
   	 textSb.append(comc.fixLeft(hChiYymmdd, 7))
   	 .append(comStr.rpad(hOldCardNo, 16))
   	 .append(comStr.rpad(hNewCardNo, 16))
   	 .append(comc.fixLeft("", 11))
   	 .append(NEW_LINE);
   	 
   	 return textSb.toString();
   }

    private void getColCsRptData() throws Exception {
    	hTmpAcctNo = getValue("TMP_ACCT_NO");
		hOldCardNo = getValue("OLD_CARD_NO");
		hNewCardNo = getValue("CARD_NO");
		hIdNo = getValue("ID_NO");
		hChiName = getValue("CHI_NAME");
		hIssueDate = getValue("ISSUE_DATE");
		hHomeAreaCode1 = getValue("HOME_AREA_CODE1");
		hHomeTelNo1 = getValue("HOME_TEL_NO1");
    }
    
    private void initialData() {
		hTmpAcctNo = "";
		hOldCardNo = "";
		hNewCardNo = "";
		hIdNo = "";
		hChiName = "";
		hIssueDate = "";
		hHomeAreaCode1 = "";
		hHomeTelNo1 = "";
    }
    
    private void procFTP() throws Exception {
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
    	commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    	commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
    	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    	commFTP.hEflgModPgm = javaProgram;

    	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
    	showLogMessage("I", "", "mput " + FILE_NAME + " 開始傳送....");
    	int errCode = commFTP.ftplogName("NCR2TCB", "mput " + FILE_NAME);

    	if (errCode != 0) {
    		showLogMessage("I", "", "ERROR:無法傳送 " + FILE_NAME + " 資料" + " errcode:" + errCode);
    		insertEcsNotifyLog();
    	}
    }
    
    private int insertEcsNotifyLog() throws Exception {
    	setValue("crt_date", sysDate);
    	setValue("crt_time", sysTime);
    	setValue("unit_code", comr.getObjectOwner("3", javaProgram));
    	setValue("obj_type", "3");
    	setValue("notify_head", "無法 FTP 傳送 " + FILE_NAME + " 資料");
    	setValue("notify_name", "媒體檔名:" + FILE_NAME);
    	setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + FILE_NAME + " 資料");
    	setValue("notify_desc2", "");
    	setValue("trans_seqno", commFTP.hEflgTransSeqno);
    	setValue("mod_time", sysDate + sysTime);
    	setValue("mod_pgm", javaProgram);
    	daoTable = "ecs_notify_log";

    	insertTable();

    	return (0);
    }

    private void renameFile() throws Exception {
    	String tmpstr1 = String.format("%s/media/crd/%s", comc.getECSHOME(), FILE_NAME);
    	String tmpstr2 = String.format("%s/media/crd/backup/%s.%s", comc.getECSHOME(), FILE_NAME,sysDate+sysTime);

    	if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
    		showLogMessage("I", "", "ERROR : 檔案[" + FILE_NAME + "]更名失敗!");
    		return;
    	}
    	showLogMessage("I", "", "檔案 [" + FILE_NAME + "] 已移至 [" + tmpstr2 + "]");
    }

    
    /***********************************************************************/
    private void checkOpen() throws Exception {
        String temstr = String.format("%s/media/crd/%s", comc.getECSHOME(),FILE_NAME);
        temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
        out = openOutputText(temstr, "MS950");
        if (out == -1) {
            comcr.errRtn(temstr, "檔案開啓失敗！", "");
        }
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF076 proc = new CrdF076();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
