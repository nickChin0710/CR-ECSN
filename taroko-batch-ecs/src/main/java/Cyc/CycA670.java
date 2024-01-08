/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version     AUTHOR              DESCRIPTION                     *
*  --------  ---------- ---------  ------------------------------------------*
* 111/07/22  V1.00.00     Ryan     initial                                   *
* 111/11/17  V1.00.01     Ryan     郵遞區號3碼改為6碼                                                                                  * 
* 111/11/18  V1.00.02     Ryan     調整Record00 ~ 06,長度都520 Byte          *
* 112/02/01  V1.00.03     Ryan     Record00、Record01、Record03欄位調整                           *
* 112/03/01  V1.00.04     Ryan     調整Record01                               *
* 112/03/22  V1.00.05     Ryan     第一個欄位銷帳編號改放p_seqno                    *
* 112/05/08  V1.00.06     Ryan     修改檔案傳送路徑NCR2TCB-->STMT                 *
* 112/05/09  V1.00.07     Ryan     調整Record00、調整Record01                   *
* 112/05/22  V1.00.08     Ryan     acct_type = 03 改    acct_type <> 01         *
* 112/06/13  V1.00.09     Ryan     cyc_abem add order by print_type,print_seq *
* 112/06/14  V1.00.10     Ryan     bonus相關欄位改為讀取mkt_bonus_hst              *
* 112/07/17  V1.00.11     Ryan     Record03 DEST_AMT --> DC_DEST_AMT ,add Record99          *
* 112/07/31  V1.00.12     Ryan     Record01 產出疑義帳款小計資料                                                         *
* 112/08/22  V1.00.13     Ryan     Record03 調整AREA_CODE ==>CURRENCY_CODE   
* 112/08/22  V1.00.13     Ryan     Record01 調整 ==>PAYMENT_NO_II   
* 112/08/25  V1.00.14     Ryan     Record01 調整循環信用利率,次期循環利率,循環信用利率適用年月+預留利率適用日,次期利率用起日                                                         *
* 112/09/12  V1.00.15     Ryan     Record01 ~ Record06 8.卡號 改放 p_seqno *
* 112/09/25  V1.00.16     Simon    shell cyc002、cyc003並行執行日期控制      *
* 112/10/05  V1.00.17     Ryan     調整Record00~Record08分行代號 ,Record06委扣銀行代號     *
* 112/10/11  V1.00.18     Ryan     增加寫入筆數log                                 *
* 112/10/20  V1.00.19     Ryan     調整  Record01 調整 26 ,32,33 新增36現金回饋欄位, Record06 調整7-11條碼,Record03 調整消費日期*
* 112/10/24  V1.00.20     Ryan     修改車號&車籍訊息欄位
* 112/11/10  V1.00.21     Ryan     unprintFlag <> Y & unprintFlagRegular = X 不列印   *
* 112/11/17  V1.00.22     Ryan     Record01,Record03部分欄位改為V99   *
* 112/11/19  V1.00.27     Mike     Record01,Record03 V99欄位改為#.##  *
* 112/11/24  V1.00.28     Ryan     Record067-11條碼 1~3 調整 *
* 112/12/13  V1.00.29     Ryan     modify selectActAnalSub & selectCycBillExt == >acctMonth - 1 month *
* 112/12/20  V1.00.30     Ryan     循環信用利率 format修正 *
******************************************************************************/
package Cyc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.*;

public class CycA670 extends AccessDAO {
	
	private String progname = "產生信用卡(商務卡)對帳單列印廠商文字檔 "
	                        + "112/12/20 V1.00.30";
	
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final static String FILE_NAME_TEMPLATE = "BCARD_BASIC_PRINT_YYYYMMDD.dat";
	
	private final static String MEDIA_FOLDER = "/media/cyc/";
	
	private final static String COL_SEPERATOR = "|";
	private final static String LINE_SEPERATOR = "\r\n";
	
	CommCrd  commCrd  = new CommCrd(); 
	CommDate commDate = new CommDate();
	CommString commString = new CommString();
	
	String hCurrBusinessDate = "";
	String hInputExeDateFlag = "";
	String hNotifyflag = "";
	String hLastCloseDat = "";
	String hStmtCycle = "";
	String hThisAcctMonth = "";
	String hThisAcctYearRunMonth = "";
// ************************************************************************
	public static void main(String[] args) throws Exception {
		CycA670 proc = new CycA670();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("N");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			
			/** 檢查是否程式已啟動中 **/
			CommFunction commFunction = new CommFunction();
			if (commFunction.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程式啟動中, 不執行..");
				return (0);
			}

			if (!connectDataBase())
				return (1);

			/** Load input arguments: businessDate **/
			if (args.length > 1) {
				showLogMessage("I", "", "請輸入參數:");
				showLogMessage("I", "", "PARM 1 : [business_date]");
				return (1);
			}

			selectPtrBusinday();
			showLogMessage("I", "", "本日營業日 : ["+hCurrBusinessDate+"]");

			if (args.length >= 1 && args[0].length() == 8) {
				businessDate = args[0];
				hInputExeDateFlag = "Y";
			} 

			if (hInputExeDateFlag.equals("Y")) {
  			showLogMessage("I", "", "人工執行關帳日 : ["+businessDate+"]");
			} else {
  			showLogMessage("I", "", "系統執行關帳日 : ["+businessDate+"]");
			}
			
			/** Check whether businessDate is workday **/
			if (isWordDay(businessDate) == false) {
				showLogMessage("I", "", "本日非符合執行關帳日, 程式結束");
				return (0);
			}

			showLogMessage("I", "", "stmt_cycle = [" +hStmtCycle+ "]");
			
			selectCycRcrateParm();
			showLogMessage("I", "", "hThisAcctYearRunMonth = [" +hThisAcctYearRunMonth+ "]");
			
			loadMktBonusHst();
			loadCycAbem01();
			loadPtrRcrate();
			/** get acctMonth **/
			String acctMonth = businessDate.substring(0, 6);
			
			/** Get file name and path1 **/
			String fileName1 = FILE_NAME_TEMPLATE.replace("YYYYMMDD", businessDate);
			String filePath1 = String.format("%s%s%s", commCrd.getECSHOME(), MEDIA_FOLDER, fileName1);
			
			/** Start to process1 **/
			process1(acctMonth,filePath1);
			
			finalProcess();
			return (0);
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess

// ************************************************************************

	private void process1(String acctMonth ,String filePath) throws Exception {	
		int tolCnt = 0;
		/** Record01 List and Record06 List **/
		ArrayList<String> record04List = null;
		ArrayList<String> record05List = null;
		HashMap<String, String> map = new HashMap<String, String>();
		boolean isRun = false;
		int fileWriter = getFileWriter(filePath);
		
		/** 讀取CYC_ACMM_01相關資料 **/
		int cycAcmm01Cursor = selectCycAcmm01();
		while (fetchTable(cycAcmm01Cursor)) {
			CycAcmm01 cycAcmm01 = getCycAcmm01();
			cycAcmm01.cycFlag = getCycFlag(cycAcmm01.idPSeqno, cycAcmm01.pSeqno );
			cycAcmm01.currCode = "901";

			if("X".equals(cycAcmm01.unprintFlagRegular) 
					&& !"Y".equals(cycAcmm01.unprintFlag)) {
				continue;
			}
			
			/** CYC_ABEM01取得 **/
			List<CycAbem01> cycAbemList = getCycAbem01List(cycAcmm01.pSeqno, cycAcmm01.currCode);
			if(cycAbemList.size()==0) {
				continue;
			}

			tolCnt ++;
			selectActAnalSub(cycAcmm01,acctMonth);
			selectCycBillExt(cycAcmm01,acctMonth ,cycAcmm01.currCode);
			
			/**取得車號&車籍訊息顯示**/
			getRmCarno(cycAcmm01);
			
			/**讀取CYC_ACMM_CURR_01**/
			CycAcmmCurr01 cycAcmmCurr01 = getCycAcmmCurr01(cycAcmm01.pSeqno,cycAcmm01.currCode);

			/** Record04 (訊息項 FOR 簡訊前，主機一筆資料對映一筆列印) **/
			if (record04List == null) record04List = getMessageList(cycAcmm01, "1", acctMonth);
			
			/** Record05 (訊息項 FOR 簡訊後，主機PCMS一筆資料對映一筆列印) **/
			if (record05List == null) record05List = getMessageList(cycAcmm01, "2", acctMonth);
			
			/** 產生VD_ALL_PRINT_YYYYMMDD.dat **/
			writeTextIntoFile(fileWriter, cycAcmm01, cycAcmmCurr01, cycAbemList, record04List, record05List);
			
			if (tolCnt % OUTPUT_BUFF_SIZE == 0) {
				showLogMessage("I", "", String.format("process1 已寫入%d筆資料", tolCnt));
			}
			
			map.put(cycAcmm01.pSeqno, cycAcmm01.currCode);	
			isRun = true;
		}
		showLogMessage("I", "", String.format("process1 處理完成，共寫入%d筆資料", tolCnt));
		if(map.size() > 0) {
			writeTextFile(fileWriter,getRecord99Str(map.size()));
		}

		closeCursor(cycAcmm01Cursor);
		closeOutputText(fileWriter);
		
		if (isRun) procFTP(Paths.get(filePath).getFileName().toString(), filePath);
		else commCrd.fileDelete(filePath);
	}
	

// ************************************************************************	
	  void procFTP(String fileName, String filePath) throws Exception {
		  CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		  CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
		  
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "STMT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s%s", commCrd.getECSHOME(), MEDIA_FOLDER);
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "put " + fileName + " 開始傳送....");
	      int errCode = commFTP.ftplogName("STMT", "put " + fileName);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + fileName + " 資料"+" errcode:"+errCode);
	          insertEcsNotifyLog(fileName, commFTP, comr);          
	      }  else {
	    	  moveTxtToBackup(filePath, fileName);
	      }
	  }
// ************************************************************************		  
	  public int insertEcsNotifyLog(String fileName, CommFTP commFTP, CommRoutine comr) throws Exception {
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
	
// ************************************************************************		
	private void moveTxtToBackup(String filePath, String fileName) throws IOException, Exception {
		// media/dba/backup
		Path backupFileFolderPath = Paths.get(commCrd.getECSHOME(), MEDIA_FOLDER, "backup");
		// create the parent directory if parent the directory is not exist
		Files.createDirectories(backupFileFolderPath);
		// get output file path
		String backupFilePath = Paths.get(backupFileFolderPath.toString(), fileName + "." + sysDate + sysTime).toString();
		
		moveFile(filePath, backupFilePath);
	}
	
// ************************************************************************		
	private void moveFile(String srcFilePath, String targetFilePath) throws Exception {
		
		if (commCrd.fileMove(srcFilePath, targetFilePath) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + srcFilePath + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + srcFilePath + "] 已移至 [" + targetFilePath + "]");
	}
// ************************************************************************	
	private void writeTextIntoFile(int fileWriter, CycAcmm01 cycAcmm01, CycAcmmCurr01 cycAcmmCurr01, List<CycAbem01> cycAbem01List, 
			ArrayList<String> record04List, ArrayList<String> record05List) throws Exception {
		
		//showLogMessage("I", "", String.format("pSeqno[%s]", cycAcmm01.pSeqno));
		StringBuilder sb = new StringBuilder();

		hNotifyflag = getNotifyflag(cycAcmmCurr01,cycAcmm01.pSeqno);
		
		/** Record00 (帳單交易(03)筆數及訊息(04、05)筆數) **/
		sb.append(getRecord00Str(cycAcmm01,record04List.size(),record05List.size()));
		
		/** Record01 (客戶帳務相關資料) **/
		sb.append(getRecord01Str(cycAcmm01,cycAcmmCurr01));
		
		/** Record02(帳單郵寄資料) **/
		sb.append(getRecord02Str(cycAcmm01,cycAcmmCurr01));

		/** Record03(消費項 ，一筆資料對映一筆列印) **/
		for (CycAbem01 CycAbem01 : cycAbem01List) {
			sb.append(getRecord03Str(cycAcmm01,CycAbem01));
		}

		/** Record04(訊息項 FOR 簡訊前，主機一筆資料對映一筆列印)  **/
		for (int i = 0 ; i < record04List.size() ; i++) {
			sb.append(getRecord04Str(cycAcmm01, i+1, record04List.get(i)));
		}
		
		/** Record05(訊息項 FOR 簡訊後，主機PCMS一筆資料對映一筆列印)  **/
		for (int i = 0 ; i < record05List.size() ; i++) {
			sb.append(getRecord05Str(cycAcmm01, i+1, record05List.get(i)));
		}

		/** Record06(FOR ACH及7-11代收作業所需資料) **/
		sb.append(getRecord06Str(cycAcmm01,cycAcmmCurr01));

		writeTextFile(fileWriter, sb.toString());
	}
	
// ************************************************************************	
	
	private String getRecord00Str(CycAcmm01 cycAcmm01 , int record04List , int record05List) throws UnsupportedEncodingException {		
			StringBuilder sb = new StringBuilder();
			sb.append(commCrd.fixLeft(cycAcmm01.pSeqno, 16))  //銷帳編號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft("00", 2)) //RECORD TYPE
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.acctType, 2)) //卡帳戶類別
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.acctKey, 11)) //acct_key
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(cycAcmm01.crdIdNo, 10)) //客戶ID
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.corpNo, 11)) //公司ID
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(commDate.toTwDate(sysDate), 7)) //處理日期 YYYMMDD
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) //卡片號碼
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(String.format("%03d", cycAcmm01.statementCount), 3)) //RECORD 03項的筆數
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(String.format("%03d", record04List), 3)) //RECORD TYPE 04項筆數
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(String.format("%03d", record05List), 3)) //RECORD TYPE 05項筆數
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(" ", 401)) //FILLER
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(hNotifyflag, 1)) //是否權益通知
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.unprintFlagRegular, 1)) //對帳單不列印旗標
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.sendPaper.equals("Y")?"P":"E", 1)) //紙本或電子 P-紙本 ; E-電子
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(!commString.empty(cycAcmm01.autoPayAcct)?"Y":"N", 1)) //是否有委扣帳號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.acctStatus, 1)) //帳戶往來狀態 1:正常 2:逾放 3.催收 4.呆帳 5.結清
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(cycAcmm01.regBankNo, 4)) //分行代號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.unprintFlag, 1)) //對帳單不列印旗標
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.zipCode, 6)) //郵遞區號
			  .append(LINE_SEPERATOR)
			  ;
			return sb.toString();
	}
		
// ************************************************************************	
		
	private String getRecord01Str(CycAcmm01 cycAcmm01 ,CycAcmmCurr01 cycAcmmCurr01) throws UnsupportedEncodingException {		
			StringBuilder sb = new StringBuilder();
			sb.append(commCrd.fixLeft(cycAcmm01.pSeqno, 16))  //銷帳編號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft("01", 2)) //RECORD TYPE
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.acctType, 2)) //帳戶類別
			  .append(COL_SEPERATOR)   
			  .append(commCrd.fixLeft(cycAcmm01.acctKey, 11)) //acct_key
			  .append(COL_SEPERATOR)  
			  .append(commCrd.fixLeft(cycAcmm01.crdIdNo, 10)) //客戶ID
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.corpNo, 11)) //公司ID
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(commDate.toTwDate(sysDate), 7)) //處理日期 YYYMMDD
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) //卡片號碼
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(String.format("%011d",cycAcmm01.creditLimit), 11)) //客戶信用額度
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(commDate.toTwDate(cycAcmm01.lastpayDate), 7)) //繳款截止日
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(commDate.toTwDate(cycAcmm01.cycleDate), 7)) //帳單結帳日
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(commString.numFormat(cycAcmmCurr01.dcThisTtlAmt, "###,###"), 12)) //應繳總額
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(commString.numFormat(cycAcmmCurr01.dcThisMinimumPay, "###,###"), 12)) //最低應繳金額
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.officeAreaCode1 + cycAcmm01.officeTelNo1, 22)) //公司電話
			  .append(COL_SEPERATOR)   
			  .append(commCrd.fixLeft(cycAcmm01.homeAreaCode1 + cycAcmm01.homeTelNo1, 22)) //住家電話
			  .append(COL_SEPERATOR)   
			  .append(commCrd.fixLeft(cycAcmm01.paymentNoIi, 16)) //客戶繳款編號  
			  .append(COL_SEPERATOR)   
			  .append(commCrd.fixLeft(cycAcmmCurr01.autopayAcctNo, 16)) //委託扣繳帳號
			  .append(COL_SEPERATOR)   
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.acnoRcrateYear, ""), 5)) //循環信用利率
			  .append(COL_SEPERATOR)                    
			  .append(commCrd.fixLeft(hThisAcctYearRunMonth, 7)) //循環信用利率適用年月+預留利率適用日
			  .append(COL_SEPERATOR)   
			  .append(commCrd.fixLeft(commString.numFormat(cycAcmmCurr01.dcAutoPaymentAmt, "###,###"), 12)) //委託扣繳額(最低/全額)
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.paymentNoIi, 16)) //臨櫃繳款銷帳代號
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(cycAcmm01.cellarPhone, 15)) //手機號碼
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.revolveIntRate2 == 0 ? cycAcmm01.acnoRcrateYear :cycAcmm01.rcrateYear, ""), 5)) //次期循環利率
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(hThisAcctYearRunMonth, 7)) //次期利率用起日
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(cycAcmm01.abemCnt>0?"Y":" ", 1)) //疑義帳款註記
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixRight(String.format("%03d", cycAcmm01.abemCnt), 3)) //疑義帳款筆數小計
			  .append(COL_SEPERATOR)
			  //.append(commCrd.fixRight(String.format("%010.2f", cycAcmm01.sumDcDestAmt), ""), 9)) //疑義帳款金額小計
        .append(commCrd.fixRight(commString.numFormat(cycAcmm01.sumDcDestAmt,  "#.##"), 9)) //疑義帳款筆數小計
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.lastTtlAmt,  "#.##"), 13)) //上期金額小計
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.paymentAmt,  "#.##"), 13)) //本期繳款金額小計
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.int2Str(cycAcmm01.hisPurchaseCnt), 5)) //本年度已入帳消費次數總計
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.hisPurchaseAmt,  "#.##"), 13)) //本年度已入帳消費金額總計
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.interestAmt,  "#.##"), 11)) //本年度截至本期 帳單結帳日之利息
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.feeAmt,  "#.##"), 11)) //本年度截至本期 帳單結帳日之費用
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.int2Str(cycAcmm01.minPayCnt), 5)) //使用循環信用時~繳清全部帳款所需時間為
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.minPayAmt,  "#.##"), 13)) //使用循環信用時~應繳納總金額
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(commString.numFormat(cycAcmm01.endTranAmt,  "#.##"), 13)) //現金回饋未銷帳餘額
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(" ", 93)) //Filler
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixRight(cycAcmm01.regBankNo, 4)) //分行代號
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft("0", 1)) //列印註記
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(cycAcmm01.zipCode, 6)) //郵遞區號
			  .append(LINE_SEPERATOR)
			  ;
			return sb.toString();
	}
// ************************************************************************	
		
	private String getRecord02Str(CycAcmm01 cycAcmm01 ,CycAcmmCurr01 cycAcmmCurr01) throws UnsupportedEncodingException {
			StringBuilder sb = new StringBuilder();
			sb.append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) // 銷帳編號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft("02", 2)) // RECORD TYPE
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.acctType, 2)) // 帳戶類別
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.acctKey, 11)) //acct_key
			  .append(COL_SEPERATOR)  
			  .append(commCrd.fixLeft(cycAcmm01.crdIdNo, 10)) // 客戶ID
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.corpNo, 11)) //公司ID
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(commDate.toTwDate(sysDate), 7)) // 處理日期
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) // 卡片號碼
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.chineseName, 100)) // 客戶姓名(中文)
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.zipCodeChin, 8)) // 郵遞區號(全形)
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.mailAddr1 + cycAcmm01.mailAddr2 + cycAcmm01.mailAddr3 + cycAcmm01.mailAddr4, 44)) // 帳單地址一
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.mailAddr5, 56)) // 帳單地址二
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.abbrName, 100)) // 公司名稱
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(String.format("%09d",(int)cycAcmmCurr01.dcInterestAmt), 9)) //循環信用利息
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixRight(String.format("%09d",(int)cycAcmm01.penautyAmt), 9)) //違約金
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(cycAcmm01.rdsPcard, 1)) // 車籍訊息顯示
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.rmCarno, 8)) // 車號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixRight(String.format("%011d",(int)cycAcmm01.normalCashLimit), 11)) //預借現金額度
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(" ", 1)) // 電子帳單合併註記
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.marketAgreeBase, 1)) // 拒絕行銷註記
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.eMailAddr, 50)) // 電子郵件信箱
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(" ", 12)) // Filler
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.regBankNo, 4)) // 分行代號	
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft("0", 1)) // 列印註記
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(cycAcmm01.zipCode, 6)) // 郵遞區號
			  .append(LINE_SEPERATOR)
			  ;
			return sb.toString();
	}
// ************************************************************************	
	
	private String getRecord03Str(CycAcmm01 cycAcmm01 ,CycAbem01 cycAbem01) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft(cycAcmm01.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("03", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctType, 2)) //帳戶類別
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctKey, 11)) //acct_key
		  .append(COL_SEPERATOR)  
		  .append(commCrd.fixLeft(cycAcmm01.crdIdNo, 10)) // 客戶ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.corpNo, 11)) //公司ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(sysDate), 7)) //處理日期
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) //卡片號碼
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(cycAbem01.postDate), 7)) //入帳日
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(cycAbem01.exchangeDate), 7)) //外幣折算日
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAbem01.currencyCode, 3)) //消費地代碼
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commString.numFormat(cycAbem01.sourceAmt.doubleValue(), "###,##0.00"), 21)) //消費地金額
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commString.numFormat(cycAbem01.dcDestAmt, "###,###"), 12)) //台幣金額
		  .append(COL_SEPERATOR) 
		  .append(commCrd.fixLeft(cycAbem01.description, 40)) //帳項說明
		  .append(COL_SEPERATOR) 
		  .append(commCrd.fixLeft(cycAbem01.bilMchtCountry, 2)) //帳費地
		  .append(COL_SEPERATOR) 
		  .append(commCrd.fixLeft(cycAbem01.binType, 1)) //卡別
		  .append(COL_SEPERATOR) 
		  .append(commCrd.fixLeft(commString.right(cycAbem01.cardNo, 4), 4)) //卡號末四碼
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commString.ss2int(commDate.toTwDate(cycAbem01.purchaseDate))<=0?"":commDate.toTwDate(cycAbem01.purchaseDate), 7)) //消費日期
		  .append(COL_SEPERATOR) //
		  .append(commCrd.fixRight(cycAbem01.cardNo, 16)) //交易卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAbem01.printType.equals("08")?"Y":"", 1)) //帳款暫緩繳納註記
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAbem01.principalNobackFlag, 1)) //本金不回饋註記
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAbem01.acctCode.equals("IT")?"Y":"N", 1)) //是否為分期交易
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixRight(commString.numFormat(cycAbem01.unbillItEndBal,  "#.##"), 13)) //應付帳款分期未到期金額
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixRight(commString.numFormat(cycAbem01.yearFeesRate,  "#.##"), 5)) //應付總費用年百分率
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.groupCode, 4)) //團代
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 261)) //FILLER
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.regBankNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("0", 1)) //列印註記
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.zipCode, 6)) //郵遞區號
		  .append(LINE_SEPERATOR)
		  ;
		return sb.toString();
	}
		
// ************************************************************************	
		
	private String getRecord04Str(CycAcmm01 cycAcmm01 ,int msgNo, String message) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft(cycAcmm01.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("04", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctType, 2)) //帳戶類別
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctKey, 11)) //acct_key  
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.crdIdNo, 10)) //客戶ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.corpNo, 11)) //公司ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(sysDate), 7)) //處理日期
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) //卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixRight(String.format("%03d", msgNo), 3)) //訊息編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(message, 280)) //帳單訊息
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 138)) //FILLER
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.regBankNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("0", 1)) //列印註記
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.zipCode, 6)) //郵遞區號
		  .append(LINE_SEPERATOR)
		  ;
		return sb.toString();
	}
			
// ************************************************************************	
			
	private String getRecord05Str(CycAcmm01 cycAcmm01, int msgNo, String message) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft(cycAcmm01.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("05", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctType, 2)) //帳戶類別
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctKey, 11)) //acct_key
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.crdIdNo, 10)) //客戶ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.corpNo, 11)) //公司ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(sysDate), 7)) //處理日期
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) //卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixRight(String.format("%03d", msgNo), 3)) //訊息編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(message, 280)) //帳單訊息
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 138)) //FILLER
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.regBankNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("0", 1)) //列印註記
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.zipCode, 6)) //郵遞區號
		  .append(LINE_SEPERATOR)
		  ;
		return sb.toString();
	}
	
// ************************************************************************	
	
	private String getRecord06Str(CycAcmm01 cycAcmm01, CycAcmmCurr01 cycAcmmCurr01) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		String barcode1 = commString.right(commDate.toTwDate(commDate.dateAdd(cycAcmm01.lastpayDate, 0,1,0)), 6) + "C80";
		String barcode2 = "000" + cycAcmm01.paymentNoIi;
		String barcode3Tmp = commString.mid(String.format("%07d", commString.ss2int(commDate.toTwDate(cycAcmm01.cycleDate))),1,4)
		+ String.format("00%09d", (int)cycAcmmCurr01.dcThisTtlAmt);
		String barcode3 = getBarcode(barcode1,barcode2,barcode3Tmp,"3");
		String barcode4Tmp = commString.mid(String.format("%07d", commString.ss2int(commDate.toTwDate(cycAcmm01.cycleDate))),1,4)
		+ String.format("00%09d", (int)cycAcmmCurr01.dcThisMinimumPay);
		String barcode4 = getBarcode(barcode1,barcode2,barcode4Tmp,"4");
		
		sb.append(commCrd.fixLeft(cycAcmm01.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("06", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctType, 2)) //帳戶類別
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.acctKey, 11)) //acct_key
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.crdIdNo, 10)) //客戶ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.corpNo, 11)) //公司ID
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(sysDate), 7)) //處理日期
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.pSeqno, 16)) //卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixRight(cycAcmmCurr01.autopayAcctNo, 16)) //委扣他行代號 
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%03d", commString.ss2int(cycAcmmCurr01.autopayAcctBank)), 3)) //委扣銀行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(barcode1, 9)) //7-11條碼一
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(barcode2, 16)) //7-11條碼二
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(barcode3,15)) //7-11條碼三-應繳總額
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(barcode4,15)) //7-11條碼三-最低應繳
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 343)) //FILLER
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.regBankNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("0", 1)) //列印註記
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(cycAcmm01.zipCode, 6)) //郵遞區號
		  .append(LINE_SEPERATOR)
		  ;

		return sb.toString();
	}
	
	// ************************************************************************	
	private String getRecord99Str(int pSeqnoCnt) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft("99", 2)) // 固定99
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixRight(String.format("%09d", pSeqnoCnt), 9)) // P_SEQNO戶數合計
		  .append(LINE_SEPERATOR);
		return sb.toString();
	}
	

// ************************************************************************	
	private int getFileWriter(String filePath) throws Exception {

        int fileWriter = openOutputText(filePath, "MS950");

        return fileWriter;

    }

// ************************************************************************
		private CycAcmm01 getCycAcmm01() throws Exception {
		    setValue("bonu.p_seqno",getValue("CYC_ACMM_01.P_SEQNO"));
	        getLoadData("bonu.p_seqno"); // 讀 mkt_bonus_hst
	        setValue("SUM_ABEM_01.p_seqno",getValue("CYC_ACMM_01.P_SEQNO"));
	        getLoadData("SUM_ABEM_01.p_seqno");
	        setValue("RCRATE.RCRATE_DAY",getValue("CYC_ACMM_01.REVOLVE_INT_RATE_2"));
	        getLoadData("RCRATE.RCRATE_DAY");
	        
			CycAcmm01 cycAcmm01 = new CycAcmm01();
			cycAcmm01.pSeqno = getValue("CYC_ACMM_01.P_SEQNO");
			cycAcmm01.idPSeqno = getValue("CYC_ACMM_01.ID_P_SEQNO");
			cycAcmm01.acctType = getValue("CYC_ACMM_01.acct_type");
			cycAcmm01.acctKey = getValue("CYC_ACMM_01.ACCT_KEY");
			cycAcmm01.hiAcctKey = getValue("CYC_ACMM_01.HI_ACCT_KEY");
			cycAcmm01.dcCurrFlag = getValue("CYC_ACMM_01.DC_CURR_FLAG");
			cycAcmm01.ttlPurchase = getValueDouble("CYC_ACMM_01.TTL_PURCHASE");
			cycAcmm01.statementCount = getValueInt("CYC_ACMM_01.STATEMENT_COUNT");
			cycAcmm01.bankBranchNo = getValue("CYC_ACMM_01.BANK_BRANCH_NO");
			cycAcmm01.paymentNumber = getValue("CYC_ACMM_01.PAYMENT_NUMBER");
			cycAcmm01.zipCode = getValue("CYC_ACMM_01.ZIP_CODE");
			cycAcmm01.creditLimit = getValueInt("CYC_ACMM_01.CREDIT_LIMIT");
			cycAcmm01.lastpayDate = getValue("CYC_ACMM_01.LASTPAY_DATE");
			cycAcmm01.cycleDate = getValue("CYC_ACMM_01.CYCLE_DATE");
			cycAcmm01.officeAreaCode1 = getValue("CYC_ACMM_01.OFFICE_AREA_CODE1");
			cycAcmm01.officeTelNo1 = getValue("CYC_ACMM_01.OFFICE_TEL_NO1");
			cycAcmm01.homeAreaCode1 = getValue("CYC_ACMM_01.HOME_AREA_CODE1");
			cycAcmm01.homeTelNo1 = getValue("CYC_ACMM_01.HOME_TEL_NO1");
			cycAcmm01.autopayAcctBank = getValue("CYC_ACMM_01.AUTOPAY_ACCT_BANK");
			cycAcmm01.revolveIntRate = getValueDouble("CYC_ACMM_01.REVOLVE_INT_RATE");
			cycAcmm01.revolveRateSMonth = getValue("CYC_ACMM_01.REVOLVE_RATE_S_MONTH");
			cycAcmm01.cellarPhone = getValue("CYC_ACMM_01.CELLAR_PHONE");
			cycAcmm01.revolveIntRate2 = getValueDouble("CYC_ACMM_01.REVOLVE_INT_RATE_2");
			cycAcmm01.revolveRateSMonth2 = getValue("CYC_ACMM_01.REVOLVE_RATE_S_MONTH_2");
			cycAcmm01.flFlag = getValue("CYC_ACMM_01.FL_FLAG");
			cycAcmm01.autoPaymentAmtFl = getValueDouble("CYC_ACMM_01.AUTO_PAYMENT_AMT_FL");
			cycAcmm01.chineseName = getValue("CYC_ACMM_01.CHINESE_NAME");
			cycAcmm01.zipCodeChin = getValue("CYC_ACMM_01.ZIP_CODE_CHIN");
			cycAcmm01.mailAddr1 = getValue("CYC_ACMM_01.MAIL_ADDR_1");
			cycAcmm01.mailAddr2 = getValue("CYC_ACMM_01.MAIL_ADDR_2");
			cycAcmm01.mailAddr3 = getValue("CYC_ACMM_01.MAIL_ADDR_3");
			cycAcmm01.mailAddr4 = getValue("CYC_ACMM_01.MAIL_ADDR_4");
			cycAcmm01.mailAddr5 = getValue("CYC_ACMM_01.MAIL_ADDR_5");
			cycAcmm01.penautyAmt = getValueInt("CYC_ACMM_01.PENAUTY_AMT");
			cycAcmm01.normalCashLimit = getValueInt("CYC_ACMM_01.NORMAL_CASH_LIMIT");
			cycAcmm01.eMailAddr = getValue("CYC_ACMM_01.E_MAIL_ADDR");
			cycAcmm01.sendEmail = getValue("CYC_ACMM_01.SEND_EMAIL");
			cycAcmm01.lastMonthBonus = getValueDouble("bonu.last_month_bonus");
			cycAcmm01.newAddBonus = getValueDouble("bonu.new_add_bonus");
			cycAcmm01.adjustBonus = getValueDouble("bonu.adjust_bonus");
			cycAcmm01.useBonus = getValueDouble("bonu.use_bonus");
			cycAcmm01.netBonus = getValueDouble("bonu.net_bonus");
			cycAcmm01.eraseDate = "";
			cycAcmm01.eraseBonus = getValueDouble("bonu.remove_bonus");
			cycAcmm01.marketAgreeBase = getValue("CYC_ACMM_01.MARKET_AGREE_BASE");
			cycAcmm01.acnoFlFlag = getValue(".ACNO_FL_FLAG");
			cycAcmm01.crdIdNo = getValue("CYC_ACMM_01.ID_NO");
			cycAcmm01.crdHiIdNo = getValue("CYC_ACMM_01.HI_ID_NO");
			cycAcmm01.corpNo = getValue("CYC_ACMM_01.CORP_NO");
			cycAcmm01.abbrName = getValue("CYC_ACMM_01.ABBR_NAME");
			cycAcmm01.currCode = getValue("CYC_ACMM_01.CURR_CODE");
			cycAcmm01.unprintFlag = getValue("CYC_ACMM_01.UNPRINT_FLAG");
			cycAcmm01.sendPaper = getValue("CYC_ACMM_01.SEND_PAPER");
			cycAcmm01.autoPayAcct = getValue("CYC_ACMM_01.AUTO_PAY_ACCT"); 
			cycAcmm01.acctStatus = getValue("CYC_ACMM_01.ACCT_STATUS"); 
			cycAcmm01.lastTtlAmt = getValueDouble("CYC_ACMM_01.LAST_TTL_AMT"); 
			cycAcmm01.paymentAmt = getValueDouble("CYC_ACMM_01.PAYMENT_AMT");  
			cycAcmm01.groupCode = getValue("CYC_ACMM_01.GROUP_CODE");
			cycAcmm01.unprintFlagRegular = getValue("CYC_ACMM_01.UNPRINT_FLAG_REGULAR");
			cycAcmm01.sumDcDestAmt = getValueDouble("SUM_ABEM_01.sum_dc_dest_amt");
			cycAcmm01.abemCnt = getValueInt("SUM_ABEM_01.abem_cnt");
			cycAcmm01.paymentNoIi = getValue("CYC_ACMM_01.PAYMENT_NO_II");
			cycAcmm01.acnoRcrateYear = getValueDouble("CYC_ACMM_01.ACNO_RCRATE_YEAR");
			cycAcmm01.rcrateYear = getValueDouble("RCRATE.RCRATE_YEAR");
			cycAcmm01.regBankNo = getValue("CYC_ACMM_01.REG_BANK_NO");
			cycAcmm01.endTranAmt = getValueDouble("CYC_ACMM_01.SUM_END_TRAN_AMT");
			if(cycAcmm01.rcrateYear == 0) {
				cycAcmm01.rcrateYear = 14.75;
			}
			return cycAcmm01;
		}
		
// ************************************************************************
		private CycAcmmCurr01 getCycAcmmCurr01(String pSeqno, String currCode) throws Exception {
			int cycAcmmCurr01Cnt = selectCycAcmmCurr01(pSeqno, currCode);
			CycAcmmCurr01 cycAcmmCurr01 = new CycAcmmCurr01();
			
			if(cycAcmmCurr01Cnt>0) {
				cycAcmmCurr01.dcThisTtlAmt = getValueDouble("CYC_ACMM_CURR_01.DC_THIS_TTL_AMT");
				cycAcmmCurr01.dcThisMinimumPay = getValueDouble("CYC_ACMM_CURR_01.DC_THIS_MINIMUM_PAY");
				cycAcmmCurr01.autopayAcctNo = getValue("CYC_ACMM_CURR_01.AUTOPAY_ACCT_NO");
				cycAcmmCurr01.dcAutoPaymentAmt = getValueDouble("CYC_ACMM_CURR_01.DC_AUTO_PAYMENT_AMT");
				cycAcmmCurr01.dcInterestAmt = getValueDouble("CYC_ACMM_CURR_01.DC_INTEREST_AMT");
				cycAcmmCurr01.autopayAcctBank = getValue("CYC_ACMM_CURR_01.AUTOPAY_ACCT_BANK");
			}
			

			return cycAcmmCurr01;
		}
		

// ************************************************************************
		private ArrayList<String> getMessageList(CycAcmm01 cycAcmm01, String msgType , String acctMonth) throws Exception {
			/** 讀取PTR_BILLMSG(多筆) **/
			int selectCnt = selectPtrBillmsg(msgType, acctMonth ,cycAcmm01);
			ArrayList<String> list = new ArrayList<>(selectCnt);
			String paramStr;
			int x ;
			for (int i = 0 ; i < selectCnt ; i++) {
				paramStr = "";
				for(int j = 1; j <= 5 ; j++) {
					noTrim = "N";
					String str = getValue("PTR_BILLMSG.PARAM"+j, i).replaceAll("\n|\r","");
					if (str != null && str.trim().isEmpty() == false) 
						paramStr += str; 
				}
//				paramStr = paramStr.replace("{1}", commCrd.fixRight(commString.int2Str(cycAcmm01.minPayCnt),5))
//						.replace("{2}", commCrd.fixRight(commString.numFormat(cycAcmm01.minPayAmt, "#.##"),13))
//						.replace("{3}", commCrd.fixRight(commString.numFormat(cycAcmm01.interestAmt, "#.##"),11))
//						.replace("{4}", commCrd.fixRight(commString.numFormat(cycAcmm01.feeAmt, "#.##"),11));
//				for(x = 0; x < paramStr.length() ; x+=56) {
//					list.add(commString.mid(paramStr, x,56));
//				}
				list.add(paramStr);
			}
		
			return list;
		}
		
// ************************************************************************
		private int selectPtrBillmsg(String msgType, String acctMonth ,CycAcmm01 cycAcmm01) throws Exception {
			extendField = "PTR_BILLMSG.";
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT PARAM1, PARAM2, PARAM3, PARAM4, PARAM5 ")
			  .append(" FROM PTR_BILLMSG ")
			  .append(" WHERE ACCT_TYPE = ? ")
			  .append(" AND MSG_MONTH = (select max(MSG_MONTH) from PTR_BILLMSG WHERE ACCT_TYPE = ? AND MSG_TYPE = ? AND APR_FLAG = 'Y' ) ")
			  .append(" AND MSG_TYPE = ? ")
			  .append(" AND APR_FLAG = 'Y' ")
			  .append(" ORDER BY MSG_CODE ")
			  ;
			sqlCmd = sb.toString();
			setString(1, cycAcmm01.acctType);
			setString(2, cycAcmm01.acctType);
			setString(3, msgType);
			setString(4, msgType);
			return selectTable();
		}

// ************************************************************************
	private List<CycAbem01> getCycAbem01List(String pSeqno, String currCode) throws Exception {
		/** 讀取CYC_ABEM_01(多筆) **/
		int selectCnt = selectCycAbem01(pSeqno, currCode);
		List<CycAbem01> cycAbem01List = new ArrayList<>(selectCnt);
		for (int i = 0 ; i < selectCnt ; i++) {
			CycAbem01 cycAbem01 = new CycAbem01();
			cycAbem01.currCode = getValue("CYC_ABEM_01.CURR_CODE", i);
			cycAbem01.postDate = getValue("CYC_ABEM_01.POST_DATE", i);
			cycAbem01.exchangeDate = getValue("CYC_ABEM_01.EXCHANGE_DATE", i);
			cycAbem01.areaAode = getValue("CYC_ABEM_01.AREA_CODE", i);
			cycAbem01.sourceAmt = getValueBigDecimal("CYC_ABEM_01.SOURCE_AMT", i);
			cycAbem01.destAmt = getValueDouble("CYC_ABEM_01.DEST_AMT", i);
			cycAbem01.description = getValue("CYC_ABEM_01.DESCRIPTION", i);
			cycAbem01.cardNo = getValue("CYC_ABEM_01.CARD_NO", i);
			cycAbem01.hiCardNo = getValue("CYC_ABEM_01.HI_CARD_NO", i);
			cycAbem01.purchaseDate = getValue("CYC_ABEM_01.PURCHASE_DATE", i);
			cycAbem01.binType = getValue("CYC_ABEM_01.BIN_TYPE", i);
			cycAbem01.bilMchtCountry = getValue("CYC_ABEM_01.BIL_MCHT_COUNTRY", i);
			cycAbem01.principalNobackFlag = getValue("CYC_ABEM_01.PRINCIPAL_NOBACK_FLAG", i);
			cycAbem01.acctCode = getValue("CYC_ABEM_01.ACCT_CODE", i);
			cycAbem01.unbillItEndBal = getValueDouble("CYC_ABEM_01.UNBILL_IT_END_BAL", i);
			cycAbem01.yearFeesRate = getValueDouble("CYC_ABEM_01.YEAR_FEES_RATE", i);
			cycAbem01.printType = getValue("CYC_ABEM_01.PRINT_TYPE", i);
			cycAbem01.dcDestAmt = getValueDouble("CYC_ABEM_01.DC_DEST_AMT", i);
			cycAbem01.currencyCode = getValue("CYC_ABEM_01.CURRENCY_CODE", i);
			cycAbem01List.add(cycAbem01);
		}

		return cycAbem01List;
	}

// ************************************************************************
	private int selectCycAbem01(String pSeqno, String currCode) throws Exception {
		extendField = "CYC_ABEM_01.";
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT (SELECT BIN_TYPE FROM CRD_CARD WHERE CARD_NO = A.CARD_NO) AS BIN_TYPE ")
		  .append(" ,(select MCHT_COUNTRY from BIL_BILL where REFERENCE_NO = A.REFERENCE_NO ) AS BIL_MCHT_COUNTRY ")
		  .append(" ,UF_HI_CARDNO(A.CARD_NO) HI_CARD_NO,A.* FROM CYC_ABEM_")
		  .append(hStmtCycle)
		  .append(" AS A ")
		  .append(" WHERE A.P_SEQNO = ? ")
		  .append(" AND A.CURR_CODE = ? ")
		  .append(" ORDER BY A.PRINT_TYPE,A.PRINT_SEQ ")
		  ;
		sqlCmd = sb.toString();
		setString(1, pSeqno);
		setString(2, currCode);
		return selectTable();
	}
	
//************************************************************************
	private int selectCycAcmm01() throws Exception {
		fetchExtend = "CYC_ACMM_01.";
		sqlCmd = " select C.ID_NO, substring(C.ID_NO,1,3)||'****'||substring(C.ID_NO,8) as HI_ID_NO, C.MARKET_AGREE_BASE " ;
		sqlCmd += " ,(select reg_bank_no from crd_card where p_seqno = A.p_seqno order by current_code fetch first 1 rows only ) as reg_bank_no ";
		sqlCmd += " ,B.corp_no ,B.abbr_name ";
		sqlCmd += " ,substring(A.acct_key,1,3)||'****'||substring(A.acct_key,8) as HI_ACCT_KEY ";
		sqlCmd += " ,d.payment_no_ii ";
		sqlCmd += " ,d.rcrate_year as acno_rcrate_year ";
		sqlCmd += " ,(select sum(end_tran_amt) from mkt_cashback_dtl where p_seqno = A.p_seqno) as sum_end_tran_amt ";
		sqlCmd += " ,A.* from cyc_acmm_" + hStmtCycle;
		sqlCmd += " A left join CRD_CORP B ON A.CORP_P_SEQNO = B.CORP_P_SEQNO left join crd_idno C on A.id_p_seqno = C.id_p_seqno ";
		sqlCmd += " left join act_acno d on a.p_seqno = d.p_seqno ";
//		sqlCmd += " where 1=1 and A.acct_type != '01' and A.acct_status in('1','2') and A.dc_curr_flag = 'N' and A.ACNO_FLAG != '2'";
		sqlCmd += " where 1=1 and A.acct_type != '01' and A.dc_curr_flag = 'N' and A.ACNO_FLAG != '2' ";
//				+ "and A.send_paper = 'Y' ";
		sqlCmd += " order by A.zip_code, A.acct_type,B.corp_no, A.acct_key ";
		return openCursor();
	}
	
//************************************************************************
	private int selectCycAcmmCurr01(String pSeqno,String currCode) throws Exception {
		extendField = "CYC_ACMM_CURR_01.";
		sqlCmd = " select * from CYC_ACMM_CURR_" + hStmtCycle;
		sqlCmd += " where 1=1 and p_seqno = ? and curr_code = ? ";
		setString(1, pSeqno);
		setString(2, currCode);
		return selectTable();
	}
	
	
	//************************************************************************
		private String getCycFlag(String idPSeqno,String pSeqno) throws Exception {
//			extendField = "CYC_ACMM_01_ID.";
//			sqlCmd = " select p_seqno from CYC_ACMM_01 where 1=1 and id_p_seqno = ?  ";
//			sqlCmd += " and A.acct_type = '03' and A.acct_status in('1','2') and A.dc_curr_flag = 'N' and A.send_paper = 'Y' ";
//			sqlCmd += " order by zip_code, acct_type, acct_key ";
//			setString(1, idPSeqno);
//			
//			int cnt = selectTable();
//			if(pSeqno.equals(getValue("CYC_ACMM_01_ID.p_seqno",cnt-1))) {
//				return "Y";
//			}
			return " ";
		}
		
		private void getRmCarno(CycAcmm01 cycAcmm01) throws Exception {
			extendField = "CARNO.";
			sqlCmd = " SELECT cms.RM_CARNO,ptr.RDS_PCARD FROM CMS_ROADMASTER cms ";
			sqlCmd += " join crd_card crd on crd.card_no = cms.CARD_NO and crd.P_SEQNO = ? ";
			sqlCmd += " left join PTR_CARD_TYPE ptr on ptr.CARD_TYPE = crd.CARD_TYPE ";
			sqlCmd += " WHERE cms.id_p_seqno = ? AND cms.RM_TYPE = 'F' FETCH FIRST 1 ROWS ONLY ";
			setString(1, cycAcmm01.pSeqno);
			setString(2, cycAcmm01.idPSeqno);
			int cnt = selectTable();
			if(cnt > 0) {
				cycAcmm01.rdsPcard = getValue("CARNO.RDS_PCARD");
				cycAcmm01.rmCarno = getValue("CARNO.RM_CARNO");
			}
		}

     // ************************************************************************
		private String getNotifyflag(CycAcmmCurr01 cycAcmmCurr01, String pSeqno) throws Exception {

			if (cycAcmmCurr01.dcThisMinimumPay != 0) {
				return "";
			}
			extendField = "ACT_JRNL.";
			sqlCmd = " select max(acct_date) as acct_date from act_jrnl where 1=1 and p_seqno = ? ";
			setString(1, pSeqno);
			selectTable();
			String acctDate = getValue("ACT_JRNL.acct_date");
			if (commString.ss2int(acctDate) > commString.ss2int((hLastCloseDat))) {
				return "Y";
			}
			return "";
		}
	
//************************************************************************
//private String selectPtrBusinday() throws Exception {
	void selectPtrBusinday() throws Exception {
		selectSQL = " BUSINESS_DATE,THIS_CLOSE_DATE ";
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

	  hCurrBusinessDate = getValue("BUSINESS_DATE");
		businessDate      = getValue("THIS_CLOSE_DATE");
		return;
	}
	
	private void selectCycRcrateParm() throws Exception {
		hThisAcctYearRunMonth = "";
		extendField = "rcrate_parm.";
		sqlCmd = "select run_month from cyc_rcrate_parm ";

		selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select cyc_rcrate_parm error!");
			exitProgram(1);
		}

		String runMonth = getValue("rcrate_parm.run_month");
		String acctYear = commString.left(hThisAcctMonth, 4);
		String acctMonth = commString.right(hThisAcctMonth, 2);
		String monthStr = getAcctYearRunMonth(runMonth, acctYear, acctMonth, 0);
		if (monthStr == null) {
			getAcctYearRunMonth(runMonth, acctYear, acctMonth, 1);
		}

	}
	
	 private String getAcctYearRunMonth(String runMonth , String acctYear , String acctMonth ,int year) {
		 String monthStr = null;	
		 for(int i = 1 ;i<=12 ;i++) {
				char ch = runMonth.charAt(i-1);
				String charStr = String.valueOf(ch);
				if(charStr.equals("Y")) {
					if(year == 0)
						if(i < commString.ss2int(acctMonth)){continue;}
					monthStr = String.format("%02d", i);
					acctYear = commString.left(commDate.dateAdd(acctYear, year, 0, 0), 4);
					String acctYearTw = String.format("%03d", commString.ss2int(acctYear) - 1911);
					hThisAcctYearRunMonth =  acctYearTw + monthStr;
					break;
				}
			}
			return monthStr;
	 }

	// ************************************************************************ 
	public boolean isWordDay(String businessDate) throws Exception {
		extendField = "wday.";
		selectSQL = "last_close_date ,stmt_cycle,this_acct_month ";
		daoTable = "ptr_workday";
		whereStr = "where this_close_date = ? ";

		setString(1, businessDate);

		selectTable();

		if (notFound.equals("Y"))
			return false;

		hLastCloseDat = getValue("wday.last_close_date");
		hStmtCycle = getValue("wday.stmt_cycle");
		hThisAcctMonth = getValue("wday.this_acct_month");
		return true;
	}
	
	private void selectActAnalSub(CycAcmm01 cycAcmm01,String acctMonth) throws Exception {
		//acctMonth - 1個月
		acctMonth = commDate.monthAdd(acctMonth, -1);
		
		extendField = "ACT_ANAL_SUB.";
		selectSQL = "SUM(HIS_PURCHASE_CNT) as HIS_PURCHASE_CNT ,SUM(HIS_PURCHASE_AMT) as HIS_PURCHASE_AMT ";
		daoTable = "ACT_ANAL_SUB";
		whereStr = "WHERE ACCT_MONTH >= ? AND ACCT_MONTH <= ? AND P_SEQNO = ? ";
		setString(1, commString.left(acctMonth, 4)+ "01");
		setString(2, acctMonth);
		setString(3, cycAcmm01.pSeqno);
		selectTable();
		if (!notFound.equals("Y")) {
			cycAcmm01.hisPurchaseCnt = getValueInt("ACT_ANAL_SUB.HIS_PURCHASE_CNT");
			cycAcmm01.hisPurchaseAmt = getValueDouble("ACT_ANAL_SUB.HIS_PURCHASE_AMT");
		}
	}
	
	private void selectCycBillExt(CycAcmm01 cycAcmm01,String acctMonth ,String currCode) throws Exception {
		String year2 = "" ,month2 = "";
		StringBuffer sqlStr = new StringBuffer();
		//遇到一月要減1 取前年2月到今年1月
		if("01".equals(commString.right(acctMonth, 2))) {
			year2 = commString.left(acctMonth, 4);
			month2 = commString.right(acctMonth, 2);
			acctMonth = commDate.monthAdd(acctMonth, -1);
			sqlStr.append(" SELECT sum(A.SUM_INTEREST_AMT) AS SUM_INTEREST_AMT, ");
			sqlStr.append(" sum(A.SUM_FEE_AMT) AS SUM_FEE_AMT ");
			sqlStr.append(" ,sum(A.MIN_PAY_CNT) as MIN_PAY_CNT ");
			sqlStr.append(" ,sum(A.MIN_PAY_AMT) as MIN_PAY_AMT ");
			sqlStr.append(" FROM (( ");
		}
		
		String month = commString.right(acctMonth, 2);
		String year1 = commString.left(acctMonth, 4);
		extendField = "CYC_BILL_EXT.";

		sqlStr.append(" SELECT (");
		for(int i = 2;i<=commString.ss2int(month);i++) {
			sqlStr.append("INTEREST_AMT_");
			sqlStr.append(String.format("%02d", i));
			sqlStr.append(" + ");
		}
		sqlStr.append(" 0 ) AS SUM_INTEREST_AMT");
		sqlStr.append(",(");
		for(int i = 2;i<=commString.ss2int(month);i++) {
			sqlStr.append("FEE_AMT_");
			sqlStr.append(String.format("%02d", i));
			sqlStr.append(" + ");
		}
		sqlStr.append(" 0 ) AS SUM_FEE_AMT");
		if(commString.empty(year2) == false) {
			sqlStr.append(",0 as MIN_PAY_CNT");
			sqlStr.append(",0 as MIN_PAY_AMT");
		}else {
			sqlStr.append(",MIN_PAY_CNT_");
			sqlStr.append(month);
			sqlStr.append(" as MIN_PAY_CNT ");
			sqlStr.append(",MIN_PAY_AMT_");
			sqlStr.append(month);
			sqlStr.append(" as MIN_PAY_AMT ");
		}
		sqlCmd = sqlStr.toString();
		sqlCmd += " FROM CYC_BILL_EXT ";
		sqlCmd += " WHERE ACCT_YEAR = ? AND CURR_CODE = ? and P_SEQNO = ? ";
		setString(1, year1);
		setString(2, currCode);
		setString(3, cycAcmm01.pSeqno);
		
		//遇到一月要減1 取前年2月到今年1月
		if(commString.empty(year2) == false) {
			sqlCmd += " ) UNION ALL ";
			sqlCmd += " ( SELECT INTEREST_AMT_01 , FEE_AMT_01 ";
			sqlCmd += ",MIN_PAY_CNT_";
			sqlCmd += month2;
			sqlCmd += " as MIN_PAY_CNT ";
			sqlCmd +=",MIN_PAY_AMT_";
			sqlCmd += month2;
			sqlCmd += " as MIN_PAY_AMT ";
			sqlCmd += " FROM CYC_BILL_EXT ";
			sqlCmd += " WHERE ACCT_YEAR = ? AND CURR_CODE = ? and P_SEQNO = ? )) AS A ";
			setString(4, year2);
			setString(5, currCode);
			setString(6, cycAcmm01.pSeqno);
		}
		
		int n = selectTable();
		
		if (n > 0) {
			cycAcmm01.interestAmt = getValueDouble("CYC_BILL_EXT.SUM_INTEREST_AMT");
			cycAcmm01.feeAmt = getValueDouble("CYC_BILL_EXT.SUM_FEE_AMT");
			cycAcmm01.minPayCnt = getValueInt("CYC_BILL_EXT.MIN_PAY_CNT");
			cycAcmm01.minPayAmt = getValueDouble("CYC_BILL_EXT.MIN_PAY_AMT");
		}
	}
	
	 public int loadMktBonusHst() throws Exception
	  {
	    daoTable    = "mkt_bonus_hst";
	    extendField = "bonu.";
	    sqlCmd = "select "
	           + "p_seqno,"
	           + "last_month_bonus,"
	           + "new_add_bonus,"
	           + "adjust_bonus,"
	           + "remove_bonus,"
	           + "use_bonus,"
	           + "give_bonus,"
	           + "diff_bonus,"
	           + "net_bonus "
	           + "from  mkt_bonus_hst "
	           + "WHERE acct_month = ? and stmt_cycle = ? order by p_seqno ";

	    setString(1,getValue("wday.this_acct_month"));
	    setString(2,hStmtCycle);
	    int n = loadTable();
	    setLoadData("bonu.p_seqno");
	    return n;
	  }
	 
	 public int loadCycAbem01() throws Exception
	  {
	    daoTable    = "CYC_ABEM_01";
	    extendField = "SUM_ABEM_01.";
	    sqlCmd = "select p_seqno,sum(DC_DEST_AMT) as sum_dc_dest_amt,count(*) as abem_cnt "
	           + "from CYC_ABEM_01 "
	           + "where print_type='08' group by p_seqno ";
	    int n = loadTable();
	    setLoadData("SUM_ABEM_01.p_seqno");
	    return n;
	  }
	 
	 public int loadPtrRcrate() throws Exception
	  {
	    daoTable    = "PTR_RCRATE";
	    extendField = "RCRATE.";
	    sqlCmd = "select RCRATE_DAY,RCRATE_YEAR "
	           + "from PTR_RCRATE ";
	    int n = loadTable();
	    setLoadData("RCRATE.RCRATE_DAY");
	    return n;
	  }
	
	private String sqlAppend(String colStr) {
		StringBuffer sqlStr = new StringBuffer();
		for(int i=1 ; i<=12 ;i++) {
			if(i > 1)
				sqlStr.append(",");
			sqlStr.append(colStr);
			sqlStr.append(String.format("%02d", i));
		}
		return sqlStr.toString();
	}
	
	private String getBarcode(String barcode1 ,String barcode2 ,String barcode3 ,String type) {
		char[] barcode1Chars = str2Chars(barcode1);
		char[] barcode2Chars = str2Chars(barcode2);
		char[] barcode3Chars = str2Chars(barcode3);
		int[] array1 = new int[8];
		int[] array2 = new int[8];
		int n1 = 0;
		int n2 = 0;
		for (int i = 0; i < barcode1Chars.length; i++) {
			int barcode1Char = Character.getNumericValue(barcode1Chars[i]);
			int barcode2Char = Character.getNumericValue(barcode2Chars[i]);
			int barcode3Char = Character.getNumericValue(barcode3Chars[i]);
			if((i+1) % 2 == 0) {
				array2[n2] = barcode1Char + barcode2Char + barcode3Char;
			    n2++;
			}else {
				array1[n1] = barcode1Char + barcode2Char + barcode3Char;
			    n1++;
			}
		}
		int count1 = 0;
		int count2 = 0;
		for(int i=0 ; i<array1.length ;i++) {
			count1 += array1[i];
		}
		for(int i=0 ; i<array2.length ;i++) {
			count2 += array2[i];
		}
		int i1 = count1 % 11;
		int i2 = count2 % 11;
		String char5 = "";
		String char6 = "";
		if("3".equals(type)) {
			char5 = commString.decode(String.format("%d", i1), ",0,10", ",A,B");
			char6 = commString.decode(String.format("%d", i2), ",0,10", ",A,B");
		}
		if("4".equals(type)) {
			char5 = commString.decode(String.format("%d", i1), ",0,10", ",X,Y");
			char6 = commString.decode(String.format("%d", i2), ",0,10", ",X,Y");
		}
		
		String barcode3Str = commString.left(barcode3, 4);
		String barcode3End = commString.mid(barcode3, 6,10);
		
		return barcode3Str + char5 + char6 + barcode3End;
	}
	
	private char[] str2Chars(String barcode) {
		barcode = commString.rpad(barcode, 16 ,"0");
		String[] fmt1 = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		String[] fmt2 = {"1","2","3","4","5","6","7","8","9","1","2","3","4","5","6","7","8","9","2","3","4","5","6","7","8","9"};
		char[] barcode1Chars = barcode.toCharArray();
		for(int i = 0;i<barcode1Chars.length;i++) {
			String str = commString.decode(String.valueOf(barcode1Chars[i]), fmt1, fmt2);
			barcode1Chars[i] = str.charAt(0);
		}
		return barcode1Chars;
	}
	
	class CycAcmm01{ //CYC_ACMM_01 
		String pSeqno = ""; //P_SEQNO
		String idPSeqno = ""; //ID_P_SEQNO
		String acctType = ""; //ACCT_TYPE
		String acctKey = ""; //ACCT_KEY
		String hiAcctKey = ""; //HI_ACCT_KEY
		String dcCurrFlag = "N"; //DC_CURR_FLAG
		double ttlPurchase = 0; //TTL_PURCHASE
		int statementCount = 0; //STATEMENT_COUNT
		String bankBranchNo = ""; //BANK_BRANCH_NO
		String paymentNumber = ""; //PAYMENT_NUMBER
		String zipCode = ""; //ZIP_CODE
		int creditLimit = 0; //CREDIT_LIMIT
		String lastpayDate = ""; //LASTPAY_DATE
		String cycleDate = ""; //CYCLE_DATE
		String officeAreaCode1 = ""; //OFFICE_AREA_CODE1
		String officeTelNo1 = ""; //OFFICE_TEL_NO1
		String homeAreaCode1 = ""; //HOME_AREA_CODE1
		String homeTelNo1 = ""; //HOME_TEL_NO1
		String autopayAcctBank = ""; //AUTOPAY_ACCT_BANK
		double revolveIntRate = 0; //REVOLVE_INT_RATE
		String revolveRateSMonth = ""; //REVOLVE_RATE_S_MONTH
		String cellarPhone = ""; //CELLAR_PHONE
		double revolveIntRate2 = 0; //REVOLVE_INT_RATE_2
		String revolveRateSMonth2 = ""; //REVOLVE_RATE_S_MONTH_2
		String flFlag = ""; //FL_FLAG
		double autoPaymentAmtFl = 0; //AUTO_PAYMENT_AMT_FL
		String chineseName = ""; //CHINESE_NAME
		String zipCodeChin = ""; //ZIP_CODE_CHIN
		String mailAddr1 = ""; //MAIL_ADDR_1
		String mailAddr2 = ""; //MAIL_ADDR_2
		String mailAddr3 = ""; //MAIL_ADDR_3
		String mailAddr4 = ""; //MAIL_ADDR_4
		String mailAddr5 = ""; //MAIL_ADDR_5
		int penautyAmt = 0; //PENAUTY_AMT
		int normalCashLimit = 0; //NORMAL_CASH_LIMIT
		String eMailAddr = ""; //E_MAIL_ADDR
		String sendEmail = ""; //SEND_EMAIL
		double lastMonthBonus = 0; //LAST_MONTH_BONUS
		double newAddBonus = 0; //NEW_ADD_BONUS
		double adjustBonus = 0; //ADJUST_BONUS
		double useBonus = 0; //USE_BONUS
		double netBonus = 0; //NET_BONUS
		String eraseDate = ""; //ERASE_DATE
		double eraseBonus = 0; //ERASE_BONUS
		String marketAgreeBase = ""; //CRD_IDNO.MARKET_AGREE_BASE
		String acnoFlFlag = ""; //ACT_ACNO.FL_FLAG
		String crdIdNo = ""; //CRD_IDNO.ID_NO
		String crdHiIdNo = ""; //CRD_IDNO.ID_NO
		String corpNo = ""; //CRD_CORP.CORP_NO
		String abbrName = ""; //CRD_CORP.ABBR_NAME
		String rdsPcard = ""; //RDS_PCARD
		String rmCarno = ""; //RM_CARNO
		String cycFlag = ""; //帳單合併註記
		String hiCardNo0 = "0000000000000000"; //**********000000
		String currCode = ""; // CYC_CAMM_CURR_01.CURR_CODE
		String unprintFlag = "";//UNPRINT_FLAG
		String sendPaper = "";//SEND_PAPER
		String autoPayAcct = "";//AUTO_PAY_ACCT
		String acctStatus = "";//ACCT_STATUS
		double lastTtlAmt = 0; //LAST_TTL_AMT
		double paymentAmt = 0; //PAYMENT_AMT
		int hisPurchaseCnt = 0; //ACT_ANAL_SUB.HIS_PURCHASE_CNT
		double hisPurchaseAmt = 0; //ACT_ANAL_SUB.HIS_PURCHASE_AMT
		double interestAmt = 0; //CYC_BILL_EXT.INTEREST_AMT_01~12
		double feeAmt = 0; //CYC_BILL_EXT.FEE_AMT_01~12
		int minPayCnt = 0; //CYC_BILL_EXT.MIN_PAY_CNT_01~12
		double minPayAmt = 0; //CYC_BILL_EXT.MIN_PAY_AMT_01~12
		String groupCode = "";//GROUP_CODE
		String unprintFlagRegular = "";//UNPRINT_FLAG_REGULAR
		double sumDcDestAmt = 0;
		int abemCnt = 0;
		String paymentNoIi = "";
		double acnoRcrateYear = 0.0;
		double rcrateYear = 0.0;
		String regBankNo = "";
		double endTranAmt = 0;
	}
	
	class CycAcmmCurr01{ //CYC_ACMM_CURR_01   
		double dcThisTtlAmt = 0; //DC_THIS_TTL_AMT
		double dcThisMinimumPay = 0; //DC_THIS_MINIMUM_PAY
		String autopayAcctNo = ""; //AUTOPAY_ACCT_NO
		double dcAutoPaymentAmt = 0; //DC_AUTO_PAYMENT_AMT
		double dcInterestAmt = 0; //DC_INTEREST_AMT
		String autopayAcctBank = ""; //AUTOPAY_ACCT_BANK
	}
	
	class CycAbem01{ //CYC_ABEM_01
		String currCode = ""; //CURR_CODE
		String postDate = ""; //POST_DATE
		String exchangeDate = ""; //EXCHANGE_DATE
		String areaAode = ""; //AREA_CODE
		BigDecimal sourceAmt = BigDecimal.ZERO; //SOURCE_AMT
		double destAmt = 0; //DEST_AMT
		String description = ""; //DESCRIPTION
		String cardNo = ""; //CARD_NO
		String hiCardNo = ""; //HI_CARD_NO
		String purchaseDate = ""; //PURCHASE_DATE
		String binType = ""; //BIN_TYPE
		String bilMchtCountry = ""; //BIL_BILL.MCHT_COUNTRY
		String principalNobackFlag = ""; //PRINCIPAL_NOBACK_FLAG
		String acctCode = ""; //ACCT_CODE
		double unbillItEndBal = 0; //UNBILL_IT_END_BAL
		double yearFeesRate = 0;//YEAR_FEES_RATE
		String printType = ""; //PRINT_TYPE
		double dcDestAmt = 0; //DC_DEST_AMT
		String currencyCode = "";
	}
	
//	class ptrBillmsg{ //PTR_BILLMSG
//		String param1 = ""; //PARAM1
//		String param2 = ""; //PARAM2
//		String param3 = ""; //PARAM3
//		String param4 = ""; //PARAM4
//		String param5 = ""; //PARAM5
//	}

} 
