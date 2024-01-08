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
import com.CommCpi;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

import Crd.CrdF079.Buf;
import Crd.CrdF079.Buf1;
import Crd.CrdF079.Buf2;

public class CrdF078 extends AccessDAO{

    public final boolean debugD = false;

    private String progname = "產生送高三信信用卡新申請核準資料檔程式 112/10/18  V1.00.02 ";
    CommFunction  comm   = new CommFunction();
    CommCrd       comc   = new CommCrd();
    CommString    comStr = new CommString();
    CommDate      comDate= new CommDate();
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;
    CommRoutine comr = null;
    CommCpi comcpi = new CommCpi();

	private static final String CRM_FOLDER = "/media/crd";
	private static final String FOLDER = "/crdatacrea/NCR2TCB/";
	private static final String DATA_FORM = "BACTCB_0YYYMMDD.TXT";//民國年系統日
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
                comc.errExit("Usage : CrdF078 [sysdate ex:yyyymmdd] ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

//            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

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
            String fileFolder = Paths.get(FOLDER).toString();
            
            tmpTailerBuf = tailer.allTailer();
            writeTextFile(fptr1, tmpTailerBuf);
            
            closeOutputText(fptr1);
            
            procFTP(fileFolder);
            renameFile();
            showLogMessage("I", "", String.format("Process records = [%d]",totCnt ));

            // ==============================================
            // 固定要做的
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
        sqlCmd = "SELECT B.ID_NO,B.CHI_NAME,B.SEX,SUBSTRING(B.CELLAR_PHONE,1,10) AS TMP_CELLAR_PHONE,";
        sqlCmd +="B.OFFICE_TEL_NO1,D.BILL_SENDING_ZIP,D.BILL_SENDING_ADDR1,D.BILL_SENDING_ADDR2,";
        sqlCmd += "D.BILL_SENDING_ADDR3,D.BILL_SENDING_ADDR4,D.BILL_SENDING_ADDR5,A.SUP_FLAG,";
        sqlCmd += "C.ID_NO AS MAJOR_ID_NO,A.APPLY_NO,A.CARD_NO,A.CURRENT_CODE,A.MEMBER_ID,";
        sqlCmd += "A.ISSUE_DATE ";
        sqlCmd += "FROM CRD_CARD A,CRD_IDNO B,CRD_IDNO C,ACT_ACNO D ";
        sqlCmd += "WHERE A.ID_P_SEQNO = B.ID_P_SEQNO AND A.MAJOR_ID_P_SEQNO = C.ID_P_SEQNO ";
        sqlCmd += "AND A.ACNO_P_SEQNO = D.ACNO_P_SEQNO AND A.GROUP_CODE ='1683' ";
        sqlCmd += "AND ((A.CRT_DATE = ?) ";//系統日減一日
        sqlCmd += "OR (to_char(A.MOD_TIME,'YYYYMMDD') <> A.CRT_DATE AND to_char(A.MOD_TIME,'YYYYMMDD') = ?) ";//系統日減一日
        sqlCmd += "OR (to_char(B.MOD_TIME,'YYYYMMDD') <> B.CRT_DATE AND to_char(B.MOD_TIME,'YYYYMMDD') = ?) ";//系統日減一日
        sqlCmd += "OR (D.CHG_ADDR_DATE = ?)) ";//系統日減一日
        
        int i=1;
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        openCursor();
        while (fetchTable()) {
            data.initData();
            data.idNo = getValue("ID_NO");
            data.chiName = getValue("CHI_NAME");
            data.sex = getValue("SEX");
            data.tmpCellarPhone = getValue("TMP_CELLAR_PHONE");
            data.officeTelNo1 = getValue("OFFICE_TEL_NO1");
            data.billSendingZip = getValue("BILL_SENDING_ZIP");
            data.billSendingAddr1 = getValue("BILL_SENDING_ADDR1");
            data.billSendingAddr2 = getValue("BILL_SENDING_ADDR2");
            data.billSendingAddr3 = getValue("BILL_SENDING_ADDR3");
            data.billSendingAddr4 = getValue("BILL_SENDING_ADDR4");
            data.billSendingAddr5 = getValue("BILL_SENDING_ADDR5");
            data.supFlag = getValue("SUP_FLAG");
            data.majorIdNo = getValue("MAJOR_ID_NO");
            data.applyNo = getValue("APPLY_NO");
            data.cardNo = getValue("CARD_NO");
            data.currentCode = getValue("CURRENT_CODE");
            data.memberId = getValue("MEMBER_ID");
            data.issueDate = getValue("ISSUE_DATE");

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
              * 讀取新戶註記
     * @throws Exception 
     */
    private String selectCrdCard() throws Exception {
    	if(comStr.empty(data.issueDate))
    		return "O";
    	sqlCmd = "SELECT COUNT(*) CNT FROM CRD_CARD WHERE ID_P_SEQNO = (SELECT ID_P_SEQNO FROM CRD_IDNO WHERE ID_NO = ?) ";
    	sqlCmd += " AND ((CURRENT_CODE <> '0' AND OPPOST_DATE BETWEEN TO_CHAR(TO_DATE(?,'YYYYMMDD') - 6 MONTHS,'YYYYMMDD') ";
    	sqlCmd += " AND TO_CHAR(TO_DATE(?,'YYYYMMDD') - 1 DAYS,'YYYYMMDD')) OR (CURRENT_CODE = '0' ";
    	sqlCmd += " AND ISSUE_DATE BETWEEN TO_CHAR(TO_DATE(?,'YYYYMMDD') - 6 MONTHS,'YYYYMMDD') ";
    	sqlCmd += " AND TO_CHAR(TO_DATE(?,'YYYYMMDD') - 1 DAYS,'YYYYMMDD'))) ";
    	setString(1,data.idNo);
      	setString(2,data.issueDate);
      	setString(3,data.issueDate);
      	setString(4,data.issueDate);
      	setString(5,data.issueDate);
    	selectTable();
    	if(getValueInt("CNT")>0)
    		return "0";
    	return "1";
    }
    
    /*******************************************************************/
    private void fileOpen() throws Exception {
    	fmtFileName = DATA_FORM.replace("YYYMMDD", comDate.toTwDate(hProcDate));
    	System.out.println("fmtFileName:"+fmtFileName);
    	
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

void procFTP(String fileFolder) throws Exception {
	commFTP = new CommFTP(getDBconnect(), getDBalias());
	comr = new CommRoutine(getDBconnect(), getDBalias());
	
	System.out.println("fileFolder:"+fileFolder);
	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
    commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    commFTP.hEriaLocalDir = comc.getECSHOME()+CRM_FOLDER;//fileName;
    commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    commFTP.hEflgModPgm = javaProgram;
    
    // System.setProperty("user.dir",commFTP.h_eria_local_dir);
    showLogMessage("I", "", "mput " + fmtFileName + " 開始傳送....");
    int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fmtFileName);
    
    if (errCode != 0) {
        showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileName + " 資料"+" errcode:"+errCode);
        commFTP.insertEcsNotifyLog(fmtFileName, "3", javaProgram, sysDate, sysTime);          
    }
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
        CrdF078 proc = new CrdF078();
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
        String idNo;
        String chiName;
        String sex;
        String tmpCellarPhone;
        String officeTelNo1;
        String billSendingZip;
        String billSendingAddr1;
        String billSendingAddr2;
        String billSendingAddr3;
        String billSendingAddr4;
        String billSendingAddr5;
        String supFlag;
        String majorIdNo;
        String applyNo;
        String cardNo;
        String currentCode;
        String memberId;
        String issueDate;
        
        void initData() {
			idNo = "";
	        chiName = "";
	        sex = "";
	        tmpCellarPhone = "";
	        officeTelNo1 = "";
	        billSendingZip = "";
	        billSendingAddr1 = "";
	        billSendingAddr2 = "";
	        billSendingAddr3 = "";
	        billSendingAddr4 = "";
	        billSendingAddr5 = "";
	        supFlag = "";
	        majorIdNo = "";
	        applyNo = "";
	        cardNo = "";
	        currentCode = "";
	        memberId = "";
	        issueDate = "";
        }
        
        String allText() throws Exception {
        	//讀取全新戶註記
        	String newCrdFlag = selectCrdCard();
        	
        	String tmpBillSendingAddr = billSendingAddr1 + billSendingAddr2 + billSendingAddr3 + billSendingAddr4 + billSendingAddr5;
        	
            StringBuffer strBuf = new StringBuffer();
            strBuf.append(comc.fixLeft(idNo + "0", 11)) //持卡人ID
            .append(comc.fixLeft(comcpi.commTransChinese(String.format("%-12.12s", chiName)), 12)) //持卡人中文姓名
            .append(comc.fixLeft(sex, 1)) //持卡人性別
            .append(comc.fixLeft(!comStr.empty(tmpCellarPhone)?tmpCellarPhone:officeTelNo1, 10)) //持卡人手機號碼或公司電話
            .append(comc.fixLeft("", 1)) //空白
            .append(comc.fixLeft(billSendingZip, 3)) //帳單地郵遞區號 
            .append(comc.fixLeft("", 2)) //空白
            .append(comc.fixLeft(comcpi.commTransChinese(String.format("%-62.62s", tmpBillSendingAddr )), 62)) //帳單地
            .append(comc.fixLeft("3", 1)) //發卡組織
            .append(comc.fixLeft("4", 1)) //卡別
            .append(comc.fixLeft("0".equals(supFlag)?"1":"1".equals(supFlag)?"2":"", 1)) //正附卡別
            .append(comc.fixLeft(majorIdNo + "0", 11)) //正卡人 ID
            .append(comc.fixLeft("0", 1)) 
            .append(comc.fixLeft(comDate.toTwDate(hLastSysdate), 7)) //送件日期
            .append(comc.fixLeft("1", 1)) //流水號型態/送件對象
            .append(comc.fixLeft(cardNo, 16)) //信用卡卡號
            .append(comc.fixLeft(memberId, 6)) //三信員工編號
            .append(comc.fixLeft("3", 1)) //核准記號
            .append(comc.fixLeft("0", 1))
            .append(comc.fixLeft(comDate.toTwDate(issueDate), 7)) //核准日期
            .append(comc.fixRight("", 80)) //退件理由
            .append(comc.fixLeft(newCrdFlag, 1)) //新戶註記  
            .append(comc.fixLeft("", 6))
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
