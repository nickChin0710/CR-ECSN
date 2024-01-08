/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/19   V1.00.00  Nick        initial                                  *
*  112/06/13   V1.00.01  Wilson      指定路徑修正                                                                                           *
*  112/10/18   V1.00.02  Wilson      檔案格式調整                                                                                           *
******************************************************************************/

package Crd;

import java.nio.file.Paths;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CrdF079 extends AccessDAO{

    public final boolean debugD = false;

    private String progname = "產生送高三信信用卡狀態變動資料檔程式 112/10/18  V1.00.02 ";
    CommFunction  comm   = new CommFunction();
    CommCrd       comc   = new CommCrd();
    CommString    comStr = new CommString();
    CommDate      comDate= new CommDate();
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;
    CommRoutine comr = null;

	private static final String CRM_FOLDER = "/media/crd";
	private static final String FOLDER = "/crdatacrea/NCR2TCB/";
	private static final String DATA_FORM = "STATCB_0YYYMMDD.TXT";//民國年系統日
	private final static String COL_SEPERATOR = ",";
	private final static String LINE_SEPERATOR = System.lineSeparator();
    int debug = 0;

    Buf header = new Buf();
    Buf1 data = new Buf1();
    Buf2 tailer = new Buf2();
    String hCallBatchSeqno = "";
    String tmpHeaderBuf = "";
    String tmpTailerBuf = "";

    private String hProcDate = "";
    private String hLastSysdate = "";

    private int fptr1 = -1;
    private long totCnt = 0;

    private String fileName = "";
    private String fmtFileName = "";

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
                comc.errExit("Usage : CrdF079 [sysdate ex:yyyymmdd] ", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hProcDate = sysDate;
            String sGArgs0 = "";
            if(args.length >=  1 && args[0].length() == 8) {
                sGArgs0 = args[0];
                sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
                hProcDate   = sGArgs0;            
            }
			sqlCmd = "select to_char((to_date(?,'yyyymmdd')-1 days),'yyyymmdd') as h_last_sysdate ";
			sqlCmd += " from dual ";
			setString(1, hProcDate);
			if (selectTable() > 0)
				hLastSysdate = getValue("h_last_sysdate");      
            
            showLogMessage("I", "", String.format("輸入參數1 = [%s] ", sGArgs0));  
            showLogMessage("I", "", String.format("處理日 = [%s] ", hLastSysdate)); 
            
            fileOpen();
            
            tmpHeaderBuf = header.allHeader();
            writeTextFile(fptr1, tmpHeaderBuf);
            
            selectCrdData();
            
            tmpTailerBuf = tailer.allTailer();
            writeTextFile(fptr1, tmpTailerBuf);
            
            closeOutputText(fptr1);
            
            procFTP();
            renameFile();
            showLogMessage("I", "", String.format("Process records = [%d]",totCnt ));

            // ==============================================
            comcr.hCallErrorDesc = String.format("程式執行結束=[%d]",totCnt );
            comcr.callbatchEnd();
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    /***
              * 讀取前一日新增&異動的高三信卡人、卡片相關資料
     ***/
    private void selectCrdData() throws Exception {
        String tmpBuf = "";
        sqlCmd = "SELECT CARD_NO,CURRENT_CODE ";
        sqlCmd += "FROM CRD_CARD ";
        sqlCmd += "WHERE GROUP_CODE ='1683' ";
        sqlCmd += "AND CRT_DATE = ? ";//系統日減一
        sqlCmd += "UNION ";
        sqlCmd += "SELECT A.CARD_NO,A.CURRENT_CODE ";
        sqlCmd += "FROM CRD_CARD A, CMS_CHGCOLUMN_LOG B ";
        sqlCmd += "WHERE A.CARD_NO = B.CARD_NO ";
        sqlCmd += "AND A.GROUP_CODE ='1683' ";
        sqlCmd += "AND LOWER(B.CHG_COLUMN) ='current_code' ";
        sqlCmd += "AND B.CHG_DATE = ?";//系統日減一
        
        int i=1;
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        openCursor();
        while (fetchTable()) {
            data.initData();
            data.cardNo = getValue("CARD_NO");
            data.currentCode = getValue("CURRENT_CODE");

            tmpBuf = data.allText();
            writeTextFile(fptr1, tmpBuf);
            if(debugD)
                showLogMessage("I", "", String.format("DETAIL DATA=[%s]", tmpBuf));
            
            totCnt++;
            if(totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

        }
        closeCursor();
    }
    
    /***
              * 取得卡片狀態
     */
    private String getCardStatus() {
    	switch(data.currentCode) {
    	case "0":
    		return "3";
    	case "1":
    		return "9";
    	case "2":
    		return "9";
    	case "3":
    		return "9";
    	case "4":
    		return "9";
    	case "5":
    		return "9";
    	}
    	return " ";
    }
    
    
    /*******************************************************************/
    private void fileOpen() throws Exception {
    	fmtFileName = DATA_FORM.replace("YYYMMDD", comDate.toTwDate(hProcDate));
    	
        String temstr1 = String.format("%s%s/%s"
                       , comc.getECSHOME(),CRM_FOLDER, fmtFileName);
        fileName = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
        fptr1 = openOutputText(fileName, "MS950");
        if (fptr1 == -1) {
            comcr.errRtn(String.format("[%s]在程式執行目錄下沒有權限讀寫",fileName), ""
                                , comcr.hCallBatchSeqno);
        }
    }
/*******************************************************************/
    private void procFTP() throws Exception {
    	commFTP = new CommFTP(getDBconnect(), getDBalias());
    	comr = new CommRoutine(getDBconnect(), getDBalias());
    	
    	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = comc.getECSHOME()+CRM_FOLDER;//fileName;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        
        showLogMessage("I", "", "mput " + fmtFileName + " 開始傳送....");
        int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fmtFileName);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileName + " 資料"+" errcode:"+errCode);
            commFTP.insertEcsNotifyLog(fmtFileName, "3", javaProgram, sysDate, sysTime);          
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
	String tmpstr1 = String.format("%s%s/%s", comc.getECSHOME(),CRM_FOLDER, fmtFileName);
	String tmpstr2 = String.format("%s%s/backup/%s", comc.getECSHOME(),CRM_FOLDER, fmtFileName);
	
	if (comc.fileCopy(tmpstr1, tmpstr2) == false) {
		showLogMessage("I", "", "ERROR : 檔案[" + fmtFileName + "]備份失敗!");
		return;
	}
	comc.fileDelete(tmpstr1);
	showLogMessage("I", "", "檔案 [" + fmtFileName + "] 已移至 [" + tmpstr2 + "]");
}

/***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdF079 proc = new CrdF079();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
    class Buf {
        
        String allHeader() throws Exception {
        	
            StringBuffer strBuf = new StringBuffer();
            strBuf.append(comc.fixRight("BOF", 3))
            .append(comc.fixLeft("0", 1)) 
            .append(comc.fixLeft(comDate.toTwDate(hProcDate), 7)) //資料日期(民國年)
            .append(comc.fixRight("", 232))
            .append(LINE_SEPERATOR);
            return strBuf.toString();
        }
    }
   
    class Buf1 {
        String cardNo;
        String currentCode;
        
        void initData() {
	        cardNo = "";
	        currentCode = "";
        }
        
        String allText() throws Exception {
        	//卡片狀態
        	String cardStatus = getCardStatus();
        	
            StringBuffer strBuf = new StringBuffer();
            strBuf.append(comc.fixRight("1", 1))//流水號型態/送件對象
            .append(comc.fixLeft(cardNo, 16)) //信用卡卡號
            .append(comc.fixLeft(cardStatus, 1)) //卡片狀態
            .append(comc.fixRight("", 225))
            .append(LINE_SEPERATOR);
            return strBuf.toString();
        }
    }
                
    class Buf2 {
    	
        String allTailer() throws Exception {
        	
            StringBuffer strBuf = new StringBuffer();
            strBuf.append(comc.fixRight("EOF", 3))
            .append(comc.fixLeft(String.format("%08d",totCnt), 8)) 
            .append(comc.fixRight("", 232))
            .append(LINE_SEPERATOR);
            return strBuf.toString();
        }       
    }

}
