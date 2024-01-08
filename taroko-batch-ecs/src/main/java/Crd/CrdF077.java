/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/04/13  V1.00.00    Ryan                    program initial             *
*  112/11/07  V1.00.01    Wilson      調整中文欄位                                                                                        *
*  113/01/04  V1.00.02    Wilson      改為產生txt檔                                                                                    *
******************************************************************************/

package Crd;

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

public class CrdF077 extends AccessDAO{

    public final boolean debugD = false;

    private String progname = "產生送漢來美食持卡人基本資料檔程式 113/01/04  V1.00.02 ";
    CommFunction  comm   = new CommFunction();
    CommCrd       comc   = new CommCrd();
    CommString    comStr = new CommString();
    CommDate      comDate= new CommDate();
    CommCrdRoutine comcr = null;
    CommCpi comcpi = new CommCpi();

	private static final String CRM_FOLDER = "/media/crd";
	private static final String DATA_FORM = "CARDHOLDER.006.YYYYMMDD.txt";
	private final static String COL_SEPERATOR = ",";
	private final static String LINE_SEPERATOR = System.lineSeparator();
    int debug = 0;

    Buf1 data = new Buf1();
    String hCallBatchSeqno = "";

    private String hProcDate = "";
    private String hParmZipPswd = "";
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
                comc.errExit("Usage : CrdF077 [sysdate ex:yyyymmdd] ", "");
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
            
            selectCrdData();
            
            closeOutputText(fptr1);
            
            ftpProc();
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
              * 讀取前一日新增&異動的漢來美食卡人、卡片相關資料
     ***/
    private void selectCrdData() throws Exception {
        String tmpBuf = "";
        sqlCmd = "SELECT B.ID_NO, ";
        sqlCmd += " C.ID_NO AS MAJOR_ID_NO, ";
        sqlCmd += " A.SUP_FLAG, ";
        sqlCmd += " A.CARD_NO, ";
        sqlCmd += " A.BIN_TYPE, ";
        sqlCmd += " SUBSTRING(A.GROUP_CODE,1,3) AS TYPE, ";
        sqlCmd += " A.ISSUE_DATE, ";
        sqlCmd += " A.OPPOST_DATE, ";
        sqlCmd += " A.CURRENT_CODE, ";
        sqlCmd += " A.LAST_CONSUME_DATE, ";
        sqlCmd += " B.CHI_NAME, ";
        sqlCmd += " B.ENG_NAME, ";
        sqlCmd += " DECODE(B.SEX,1,'M','F') AS TMP_SEX, ";
        sqlCmd += " B.BIRTHDAY, ";
        sqlCmd += " SUBSTRING(B.CELLAR_PHONE,1,10) AS TMP_CELLAR_PHONE, ";
        sqlCmd += " SUBSTRING(B.E_MAIL_ADDR,1,30) AS TMP_E_MAIL_ADDR, ";
        sqlCmd += " SUBSTRING(B.OFFICE_AREA_CODE1,1,3)||'-'||SUBSTRING(B.OFFICE_TEL_NO1,1,8)|| ";
        sqlCmd += " '-'||SUBSTRING(B.OFFICE_TEL_EXT1,1,5) AS OFFICE_TEL_NO, ";
        sqlCmd += " B.COMPANY_NAME, ";
        sqlCmd += " B.MARRIAGE,  ";
        sqlCmd += " B.SPOUSE_ID_NO, ";
        sqlCmd += " B.BUSINESS_CODE, ";
        sqlCmd += " B.ANNUAL_INCOME, ";
        sqlCmd += " B.EDUCATION, ";
        sqlCmd += " B.MAIL_ZIP, ";
        sqlCmd += " (B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5) AS MAIL_ADDR , ";
        sqlCmd += " D.BILL_SENDING_ZIP AS TMP_BILL_SENDING_ZIP, ";
        sqlCmd += " (D.BILL_SENDING_ADDR1 || D.BILL_SENDING_ADDR2 || D.BILL_SENDING_ADDR3 || D.BILL_SENDING_ADDR4 || D.BILL_SENDING_ADDR5) AS TMP_BILL_SENDING_ADDR , ";
        sqlCmd += " SUBSTRING(B.HOME_AREA_CODE1,1,3)||'-'||SUBSTRING(B.HOME_TEL_NO1,1,8)|| ";
        sqlCmd += " '-'||SUBSTRING(B.HOME_TEL_EXT1,1,5) AS HOME_TEL_NO ";
        sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,CRD_IDNO C,ACT_ACNO D ";
        sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
        sqlCmd += " AND A.MAJOR_ID_P_SEQNO = C.ID_P_SEQNO ";
        sqlCmd += " AND A.ACNO_P_SEQNO = D.ACNO_P_SEQNO ";
        sqlCmd += " AND A.GROUP_CODE IN ('1631','1677','1893') ";
        sqlCmd += " AND A.SUP_FLAG ='0' ";
        sqlCmd += " AND ((A.CRT_DATE = ?) OR ";
        sqlCmd += " (to_char(A.MOD_TIME,'YYYYMMDD') <> A.CRT_DATE AND to_char(A.MOD_TIME,'YYYYMMDD') = ?) OR ";
        sqlCmd += " (to_char(B.MOD_TIME,'YYYYMMDD') <> B.CRT_DATE AND to_char(B.MOD_TIME,'YYYYMMDD') = ?) OR ";
        sqlCmd += " (D.CHG_ADDR_DATE = ?)) ";
        sqlCmd += " UNION ";
        sqlCmd += " SELECT B.ID_NO, ";
        sqlCmd += " C.ID_NO AS MAJOR_ID_NO, ";
        sqlCmd += " A.SUP_FLAG, ";
        sqlCmd += " A.CARD_NO, ";
        sqlCmd += " A.BIN_TYPE, ";
        sqlCmd += " SUBSTRING(A.GROUP_CODE,1,3) AS TYPE, ";
        sqlCmd += " A.ISSUE_DATE, ";
        sqlCmd += " A.OPPOST_DATE, ";
        sqlCmd += " A.CURRENT_CODE, ";
        sqlCmd += " A.LAST_CONSUME_DATE, ";
        sqlCmd += " B.CHI_NAME, ";
        sqlCmd += " B.ENG_NAME, ";
        sqlCmd += " DECODE(B.SEX,1,'M','F') AS TMP_SEX, ";
        sqlCmd += " B.BIRTHDAY, ";
        sqlCmd += " SUBSTRING(B.CELLAR_PHONE,1,10) AS TMP_CELLAR_PHONE, ";
        sqlCmd += " SUBSTRING(B.E_MAIL_ADDR,1,30) AS TMP_E_MAIL_ADDR, ";
        sqlCmd += " SUBSTRING(B.OFFICE_AREA_CODE1,1,3)||'-'||SUBSTRING(B.OFFICE_TEL_NO1,1,8)|| ";
        sqlCmd += " '-'||SUBSTRING(B.OFFICE_TEL_EXT1,1,5) AS OFFICE_TEL_NO, ";
        sqlCmd += " B.COMPANY_NAME, ";
        sqlCmd += " B.MARRIAGE, ";
        sqlCmd += " B.SPOUSE_ID_NO, ";
        sqlCmd += " B.BUSINESS_CODE, ";
        sqlCmd += " B.ANNUAL_INCOME, ";
        sqlCmd += " B.EDUCATION, ";
        sqlCmd += " B.MAIL_ZIP, ";
        sqlCmd += " (B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5) AS MAIL_ADDR , ";
        sqlCmd += " ''AS TMP_BILL_SENDING_ZIP, ";
        sqlCmd += " ''AS TMP_BILL_SENDING_ADDR, ";
        sqlCmd += " SUBSTRING(B.HOME_AREA_CODE1,1,3)||'-'||SUBSTRING(B.HOME_TEL_NO1,1,8)|| ";
        sqlCmd += " '-'||SUBSTRING(B.HOME_TEL_EXT1,1,5) AS HOME_TEL_NO ";
        sqlCmd += " FROM CRD_CARD A,CRD_IDNO B,CRD_IDNO C ";
        sqlCmd += " WHERE A.ID_P_SEQNO = B.ID_P_SEQNO ";
        sqlCmd += " AND A.MAJOR_ID_P_SEQNO = C.ID_P_SEQNO ";
        sqlCmd += " AND A.GROUP_CODE IN ('1631','1677','1893') ";
        sqlCmd += " AND A.SUP_FLAG ='1' ";
        sqlCmd += " AND ((A.CRT_DATE = ?) OR ";
        sqlCmd += " (to_char(A.MOD_TIME,'YYYYMMDD')  <> A.CRT_DATE AND to_char(A.MOD_TIME,'YYYYMMDD') = ?) OR ";
        sqlCmd += " (to_char(B.MOD_TIME,'YYYYMMDD') <> B.CRT_DATE AND to_char(B.MOD_TIME,'YYYYMMDD') = ?)) ";
        int i=1;
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        setString(i++, hLastSysdate);
        openCursor();
        while (fetchTable()) {
            data.initData();
            data.idNo = getValue("ID_NO");
            data.majorIdNo = getValue("MAJOR_ID_NO");
            data.supFlag = getValue("SUP_FLAG");
            data.cardNo = getValue("CARD_NO");
            data.binType = getValue("BIN_TYPE");
            data.groupCodeType = getValue("TYPE");
            data.issueDate = getValue("ISSUE_DATE");
            data.oppostDate = getValue("OPPOST_DATE");
            data.currentCode = getValue("CURRENT_CODE");
            data.lastConsumeDate = getValue("LAST_CONSUME_DATE");
            data.chiName = getValue("CHI_NAME");
            data.engName = getValue("ENG_NAME");
            data.tmpSex = getValue("TMP_SEX");
            data.birthday = getValue("BIRTHDAY");
            data.tmpCellarPhone = getValue("TMP_CELLAR_PHONE");
            data.tmpEMailAddr = getValue("TMP_E_MAIL_ADDR");
            data.officeTelNo = getValue("OFFICE_TEL_NO");
            data.companyName = getValue("COMPANY_NAME");
            data.marriage = getValue("MARRIAGE");
            data.spouseIdNo = getValue("SPOUSE_ID_NO");
            data.businessCode = getValue("BUSINESS_CODE");
            data.annualIncome = getValueLong("ANNUAL_INCOME");
            data.education = getValue("EDUCATION");
            data.mailZip = getValue("MAIL_ZIP");
            data.mailAddr = getValue("MAIL_ADDR");
            data.tmpBillSendingZip = getValue("TMP_BILL_SENDING_ZIP");
            data.tmpBillSendingAddr = getValue("TMP_BILL_SENDING_ADDR");
            data.homeTelNo = getValue("HOME_TEL_NO");

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
              * 讀取全新戶註記
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
    		return "N";
    	return "O";
    }
    
    /***
              * 取得卡片狀態
     */
    private String getCardStatus() {
    	switch(data.currentCode) {
    	case "0":
    		if(data.lastConsumeDate.compareTo(comDate.dateAdd(hProcDate, 0,-6,0))>0)
    		return "A";
    	case "1":
    	case "3":
    	case "4":
    		return "T";
    	case "2":
    	case "5":
    		return "L";
    	}
    	return "O";
    }
    
    
    /*******************************************************************/
    private void fileOpen() throws Exception {
    	fmtFileName = DATA_FORM.replace("YYYYMMDD", hProcDate);
    	
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
private void ftpProc() throws Exception 
{
//  fmtFileName = fmtFileName.replace("TXT", "ZIP");
//  /*** PKZIP 壓縮 ***/
//  String zipFile = String.format("%s%s/%s", comc.getECSHOME(), CRM_FOLDER ,fmtFileName);
//  zipFile = Normalizer.normalize(zipFile, java.text.Normalizer.Form.NFKD);
//  int tmpInt = comm.zipFile(fileName, zipFile, hParmZipPswd);
//  if (tmpInt != 0) {
//      comcr.errRtn(String.format("無法壓縮檔案[%s]", fileName), "", hCallBatchSeqno);
//  }
//  //刪除原始TXT
//  comc.fileDelete(zipFile.replace("ZIP", "TXT"));
    CommFTP  commFTP = new CommFTP(getDBconnect(), getDBalias());
    CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());

  /********** COMM_FTP common function usage ****************************************/
  commFTP.hEflgTransSeqno     = comr.getSeqno("ECS_MODSEQ");/* 串聯 log 檔所使用 鍵值 (必要) */
  for (int inti = 0; inti < 1; inti++) {
      commFTP.hEflgSystemId   = "NCR2TCB";        /* 區分不同類的 FTP 檔案-大類     (必要) */
      commFTP.hEflgGroupId    = "000000";            /* 區分不同類的 FTP 檔案-次分類 (非必要) */
      commFTP.hEflgSourceFrom = "EcsFtp";             /* 區分不同類的 FTP 檔案-細分類 (非必要) */
      commFTP.hEriaLocalDir   = String.format("%s%s", comc.getECSHOME(),CRM_FOLDER);
      commFTP.hEflgModPgm     = javaProgram;

  	  showLogMessage("I", "", "mput " + fmtFileName + " 開始傳送....");
  	  int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fmtFileName);

      if (errCode != 0) {
    	  showLogMessage("I", "", "ERROR:無法傳送 " + fmtFileName + " 資料" + " errcode:" + errCode);
      if (inti == 0) break;
     }

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
        CrdF077 proc = new CrdF077();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
    class Buf1 {
        String idNo;
        String majorIdNo;
        String supFlag;
        String cardNo;
        String binType;
        String groupCodeType;
        String issueDate;
        String oppostDate;
        String currentCode;
        String lastConsumeDate;
        String chiName;
        String engName;
        String tmpSex;
        String birthday;
        String tmpCellarPhone;
        String tmpEMailAddr;
        String officeTelNo;
        String companyName;
        String marriage;
        String spouseIdNo;
        String businessCode;
        long annualIncome;
        String education;
        String mailZip;
        String mailAddr;
        String tmpBillSendingZip;
        String tmpBillSendingAddr;
        String homeTelNo;
        
        void initData() {
			idNo = "";
			majorIdNo = "";
			supFlag = "";
			cardNo = "";
			binType = "";
			groupCodeType = "";
			issueDate = "";
			oppostDate = "";
			currentCode = "";
			lastConsumeDate = "";
			chiName = "";
			engName = "";
			tmpSex = "";
			birthday = "";
			tmpCellarPhone = "";
			tmpEMailAddr = "";
			officeTelNo = "";
			companyName = "";
			marriage = "";
			spouseIdNo = "";
			businessCode = "";
			annualIncome = 0;
			education = "";
			mailZip = "";
			mailAddr = "";
			tmpBillSendingZip = "";
			tmpBillSendingAddr = "";
			homeTelNo = "";
        }
        String allText() throws Exception {
        	//讀取全新戶註記
        	String newCrdFlag = selectCrdCard();
        	//卡片狀態
        	String cardStatus = getCardStatus();
        	
            StringBuffer strBuf = new StringBuffer();
            strBuf.append(comc.fixLeft(idNo, 10)) //持卡人ID
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(majorIdNo, 10)) //主卡人ID
            .append(COL_SEPERATOR)
            .append(comc.fixLeft("0".equals(supFlag)?"PP":"1".equals(supFlag)?"NP":supFlag, 2)) //主附卡別
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(cardNo, 16)) //卡號
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(binType, 1)) //卡種
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(groupCodeType, 3)) //卡別
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(issueDate, 8)) //核卡日
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(newCrdFlag, 1)) //全新戶註記
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(oppostDate, 8)) //卡片狀態異動日期
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(cardStatus, 1)) //卡片狀態
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(comcpi.commTransChinese(String.format("%-28.28s", chiName)), 28)) //持卡人姓名
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(engName, 26)) //持卡人英文姓名
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(tmpSex, 1)) //持卡人性別
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(birthday, 8)) //持卡人出生年月日
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(tmpCellarPhone, 10)) //持卡人手機號碼
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(tmpEMailAddr, 30)) //持卡人E-Mail
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(officeTelNo, 18)) //持卡人公司電話
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(comcpi.commTransChinese(String.format("%-18.18s", companyName)), 18)) //持卡人公司名稱 
            .append(COL_SEPERATOR)
            .append(comc.fixLeft("1".equals(marriage)&&!comStr.empty(spouseIdNo)?"1"
            		:"1".equals(marriage)&&comStr.empty(spouseIdNo)?"3":"2".equals(marriage)?"2":marriage, 1)) //持卡人婚姻狀況
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(businessCode, 4)) //持卡人職業類別
            .append(COL_SEPERATOR)
            .append(comc.fixRight(String.valueOf(annualIncome/10000), 3)) //持卡人年收入
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(comStr.pos(",5,6", education) > 0 ? "1" : comStr.pos(",3,4", education) > 0 ? "2" 
            		:comStr.pos(",1,2", education) > 0 ? "3" : education, 1)) //持卡人教育程度
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(mailZip, 3)) //居住地郵遞區號
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(comStr.left(comcpi.commTransChinese(String.format("%-28.28s", mailAddr)), 14), 28)) //居住地址1   
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(comStr.mid(comcpi.commTransChinese(String.format("%-28.28s", mailAddr)), 14), 28)) //居住地址2
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(tmpBillSendingZip, 3)) //帳單郵遞區號
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(comStr.left(comcpi.commTransChinese(String.format("%-28.28s", tmpBillSendingAddr)), 14), 28)) //帳單通訊地址1 
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(comStr.mid(comcpi.commTransChinese(String.format("%-28.28s", tmpBillSendingAddr)), 14), 28)) //帳單通訊地址2
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(homeTelNo, 12)) //帳單通訊電話
            .append(COL_SEPERATOR)
            .append(comc.fixLeft(hLastSysdate, 8)) //資料日期
            .append(COL_SEPERATOR)
            .append(comc.fixLeft("", 7)) //空白
            .append(COL_SEPERATOR)
            .append(comc.fixLeft("", 16)) //空白
            .append(LINE_SEPERATOR);
            return strBuf.toString();
        }
    }

}
