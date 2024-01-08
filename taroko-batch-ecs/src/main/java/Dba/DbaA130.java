/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version     AUTHOR              DESCRIPTION                     *
*  --------  ---------- ---------  ------------------------------------------*
* 111/06/06  V1.00.00     Justin     initial                                 *
* 111/11/17  V1.00.01     Ryan       移除Record02 5~7欄位  ,郵遞區號3碼改為6碼 *
* 111/11/18  V1.00.02     Ryan       調整Record00 ~ 06,長度都220 Byte          *
* 112/03/28  V1.00.03     Ryan       調整Record04,06,對帳單訊息合併為一筆          *
* 112/05/05  V1.00.04     Ryan       Record00增加不寄對帳單旗標          *
* 112/05/08  V1.00.05     Ryan     修改檔案傳送路徑NCR2TCB-->STMT                 *
* 112/07/17  V1.00.06     Ryan     add Record99          *
* 112/08/16  V1.00.07     Ryan     每月01才執行          *
* 112/08/23  V1.00.08     Simon    改以ptr_businday.this_close_date判斷當日是否為關帳日*
* 112/08/26  V1.00.09     Simon    本程式為shell:cr_d_cyc003最後一支程式，    *
*                         執行完畢前更新ptr_businday.this_close_date為空值    *
* 112/09/25  V1.00.10     Simon    shell cyc002、cyc003並行執行日期控制       *
* 112/10/27  V1.00.11     Ryan     修改Record01帳單結帳日 +1 月                                *
******************************************************************************/
package Dba;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.*;

public class DbaA130 extends AccessDAO {
	
	private String progname = "產生VD對帳單列印廠商文字檔 112/10/27 V1.00.11";
	private final static String FILE_NAME_TEMPLATE = "VD_ALL_PRINT_YYYYMMDD.dat";
	private final static String MEDIA_FOLDER = "/media/dba/";
	
	private static final String SEND_BY_EMAIL = "BBB";
	
	String hCurrBusinessDate = "";
	String hInputExeDateFlag = "";
	String hLastAcctMonth = "";
  String hPtrBusiRowid = "";
  CommDate  commDate = new CommDate();
	
	private final static String COL_SEPERATOR = "|";
	private final static String LINE_SEPERATOR = "\r\n";
	
	CommCrd  commCrd  = new CommCrd(); 
	CommString commString = new CommString();
// ************************************************************************
	public static void main(String[] args) throws Exception {
		DbaA130 proc = new DbaA130();
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
			showLogMessage("I", "", "本日營業日 : [" + hCurrBusinessDate + "]");

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
			
			/** get acctMonth **/
			String acctMonth = businessDate.substring(0, 6);
//			String acctMonth = "201907";
			selectLastAcctMonth();
			
			/** Get file name and path **/
			String fileName = FILE_NAME_TEMPLATE.replace("YYYYMMDD", hLastAcctMonth);
			String filePath = String.format("%s%s%s", commCrd.getECSHOME(), MEDIA_FOLDER, fileName);
			
			/** Start to process **/
			dataProcess(acctMonth, filePath);
      updatePtrBusinday();

			finalProcess();
			return (0);
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess

// ************************************************************************

	private void dataProcess(String acctMonth, String filePath) throws Exception {	
		/** Record04 List and Record06 List **/
		ArrayList<String> messageList = null;
		ArrayList<String> bonusMessageList = null;
		HashMap<String, String> map = new HashMap<String, String>();
		boolean isRun = false;
		int fileWriter = getFileWriter(filePath);
		
		/** 讀取DBA_ACMM相關資料 **/
		int dbaAcmmCursor = selectDbaAcmm(hLastAcctMonth);   /* mike*/
		while (fetchTable(dbaAcmmCursor)) {
			DbaAcmm dbaAcmm = getDbaAcmm();
			
			/** DBA_ABEM取得 **/
			List<DbaAbem> dbaAbemList = getDbaAbemList(dbaAcmm.pSeqno, hLastAcctMonth);  /* mike*/
			
			/** Record04 MESSAGE (訊息項) **/
			if (messageList == null) messageList = getMessageList("4", hLastAcctMonth);  /* mike*/
			
			/** Record06 紅利積點通路訊息 **/
			if (bonusMessageList == null) bonusMessageList = getMessageList("5", hLastAcctMonth);  /* mike*/
			
			/** 產生VD_ALL_PRINT_YYYYMMDD.dat **/
			writeTextIntoFile(fileWriter, dbaAcmm, dbaAbemList, messageList, bonusMessageList);
			
			map.put(dbaAcmm.pSeqno, hLastAcctMonth);	
			isRun = true;
		}
		
		if(map.size() > 0) {
			writeTextFile(fileWriter,getRecord99Str(map.size()));
		}
		
		closeCursor(dbaAcmmCursor);
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
	private void writeTextIntoFile(int fileWriter, DbaAcmm dbaAcmm, List<DbaAbem> dbaAbemList, 
			ArrayList<String> messageList, ArrayList<String> bonusMessageList) throws Exception {
		
		//showLogMessage("I", "", String.format("pSeqno[%s]", dbaAcmm.pSeqno));
		StringBuilder sb = new StringBuilder();

		/** Record00(列印相關訊息)-DBA_ACMM **/
		sb.append(getRecord00Str(dbaAcmm));
		
		/** Record01(客戶帳務相關資料)-DBA_ACMM **/
		sb.append(getRecord01Str(dbaAcmm));
		
		/** Record02(帳單郵寄資料)-DBA_ACMM **/
		sb.append(getRecord02Str(dbaAcmm));

		/** Record03(消費項資料)-DBA_ABEM **/
		for (DbaAbem dbaAbem : dbaAbemList) {
			sb.append(getRecord03Str(dbaAcmm, dbaAbem));
		}

		/** Record04(訊息項資料)-PTR_BILLMSG **/
		for (int i = 0 ; i < messageList.size() ; i++) {
			sb.append(getRecord04Str(dbaAcmm, i+1, messageList.get(i)));
		}

		/** Record05(紅利積點資料)-DBA_ACMM **/
		sb.append(getRecord05Str(dbaAcmm));

		/** Record06(紅利積點通路訊息資料)-PTR_BILLMSG **/
		for (int i = 0 ; i < bonusMessageList.size() ; i++) {
			sb.append(getRecord06Str(dbaAcmm, i+1, bonusMessageList.get(i)));
		}	

		writeTextFile(fileWriter, sb.toString());
	}
	
// ************************************************************************	
	
	private String getRecord00Str(DbaAcmm dbaAcmm) throws UnsupportedEncodingException {		
			StringBuilder sb = new StringBuilder();
			sb.append(commCrd.fixLeft(dbaAcmm.pSeqno, 16))  //銷帳編號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft("00", 2)) //RECORD TYPE
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.cardNo, 16)) //卡號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(String.format("%04d", dbaAcmm.statementCount), 4)) //RECORD 03項的筆數
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.unprintFlagRegular, 1)) //不寄對帳單旗標
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(" ", 294)) //保留欄位
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.bankBranchNo, 4)) //分行代號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(SEND_BY_EMAIL, 6)) //郵遞區號
			  .append(LINE_SEPERATOR)
			  ;
			return sb.toString();
	}
		
// ************************************************************************	
		
	private String getRecord01Str(DbaAcmm dbaAcmm) throws UnsupportedEncodingException {		
			StringBuilder sb = new StringBuilder();
			sb.append(commCrd.fixLeft(dbaAcmm.pSeqno, 16))  //銷帳編號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft("01", 2)) //RECORD TYPE
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.cardNo, 16)) //卡號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.acctNo, 13)) //客戶帳號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(commDate.toTwDate(commDate.monthAdd(dbaAcmm.acctMonth, 1) + "01"), 7)) //帳單結帳日
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.ttlDeductDone.doubleValue() < 0.0 ? "-" : " ", 1)) //合計消費扣款成功金額正負
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(String.format("%013d", (int)(dbaAcmm.ttlDeductDone.abs().doubleValue())), 13)) //合計消費扣款成功金額
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(" ", 1)) //合計圈存尚未扣款金額正負
			  .append(COL_SEPERATOR)                     
			  .append(commCrd.fixLeft(String.format("%013d", 0), 13)) //合計圈存尚未扣款金額
			  .append(COL_SEPERATOR)      
			  .append(commCrd.fixLeft(" ", 1)) //存款不足正負(一定為正)
			  .append(COL_SEPERATOR)                     
			  .append(commCrd.fixLeft(String.format("%013d", 0), 13)) //存款不足
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(dbaAcmm.acctKey, 11)) //客戶ID
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(" ", 211)) //保留欄位
			  .append(COL_SEPERATOR) 
			  .append(commCrd.fixLeft(String.format("%09d", commString.ss2int(dbaAcmm.printSequential)), 7)) //帳單列印序號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.bankBranchNo, 4)) //分行代號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(SEND_BY_EMAIL, 6)) //郵遞區號
			  .append(LINE_SEPERATOR)
			  ;
			return sb.toString();
	}
// ************************************************************************	
		
	private String getRecord02Str(DbaAcmm dbaAcmm) throws UnsupportedEncodingException {
			StringBuilder sb = new StringBuilder();
			sb.append(commCrd.fixLeft(dbaAcmm.pSeqno, 16)) // 銷帳編號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft("02", 2)) // RECORD TYPE
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.cardNo, 16)) // 卡號
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.chineseName, 100)) // 客戶姓名(中文) 原格式: X(30)
			  .append(COL_SEPERATOR)
//			  .append(commCrd.fixLeft(dbaAcmm.zipCodeChin, 8)) // 郵遞區號(全形)
//			  .append(COL_SEPERATOR)
//			  .append(commCrd.fixLeft(dbaAcmm.mailAddr1, 30)) // 帳單地址一
//			  .append(COL_SEPERATOR)
//			  .append(commCrd.fixLeft(dbaAcmm.mailAddr2, 30)) // 帳單地址二
//			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.eMailAddr, 50)) // E-mail
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(" ", 149)) // 保留欄位
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(dbaAcmm.bankBranchNo, 4)) // 分行代號	
			  .append(COL_SEPERATOR)
			  .append(commCrd.fixLeft(SEND_BY_EMAIL, 6)) // 郵遞區號
			  .append(LINE_SEPERATOR)
			  ;
			return sb.toString();
	}
// ************************************************************************	
	
	private String getRecord03Str(DbaAcmm dbaAcmm, DbaAbem dbaAbem) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft(dbaAcmm.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("03", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAbem.cardNo, 16)) //卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(dbaAbem.purchaseDate), 7)) //交易日期
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(dbaAbem.acctDate), 7)) //請款日期
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAbem.mchtCountry, 3)) //外幣國別
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAbem.sourceCurr, 3)) //外幣幣別
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAbem.sourceAmt.doubleValue() < 0.0 ? "-" : " ", 1)) //外幣金額正負
		  .append(COL_SEPERATOR)                     
		  .append(commCrd.fixLeft(String.format("%018.2f", dbaAbem.sourceAmt.abs().setScale(2, RoundingMode.HALF_UP).doubleValue()).replace(".", ""), 17)) //外幣金額
		  .append(COL_SEPERATOR)      
		  .append(commCrd.fixLeft(dbaAbem.transactionAmt.doubleValue() < 0.0 ? "-" : " ", 1)) //提款金額正負
		  .append(COL_SEPERATOR)                     
		  .append(commCrd.fixLeft(String.format("%013d", (int)(dbaAbem.transactionAmt.abs().doubleValue())), 13)) //提款金額
		  .append(COL_SEPERATOR) 
		  .append(commCrd.fixLeft(" ", 1)) //提款金額註記
		  .append(COL_SEPERATOR) 
		  .append(commCrd.fixLeft(" ", 1)) //圈存金額正負
		  .append(COL_SEPERATOR) 
		  .append(commCrd.fixLeft(String.format("%013d", 0), 13)) //圈存金額
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 1)) //圈存金額註記
		  .append(COL_SEPERATOR) //
		  .append(commCrd.fixLeft(dbaAbem.description, 42)) //帳項說明
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 10)) //備註(中文)
		  .append(COL_SEPERATOR) //
		  .append(commCrd.fixLeft(" ", 167)) //保留欄位
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAcmm.bankBranchNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(SEND_BY_EMAIL, 6)) //郵遞區號
		  .append(LINE_SEPERATOR)
		  ;
		return sb.toString();
	}
		
// ************************************************************************	
		
	private String getRecord04Str(DbaAcmm dbaAcmm, int msgNo, String message) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft(dbaAcmm.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("04", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAcmm.cardNo, 16)) //卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%03d", msgNo), 3)) //訊息編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(message, 280)) //對帳單訊息
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 16)) //保留欄位
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAcmm.bankBranchNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(SEND_BY_EMAIL, 6)) //郵遞區號
		  .append(LINE_SEPERATOR)
		  ;
		return sb.toString();
	}
			
// ************************************************************************	
			
	private String getRecord05Str(DbaAcmm dbaAcmm) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft(dbaAcmm.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("05", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAcmm.cardNo, 16)) //卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%12d", (int)(dbaAcmm.lastMonthBonus.doubleValue())), 12)) //上期累積點數
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%12d", (int)(dbaAcmm.newAddBonus.doubleValue())), 12)) //本期新增點數
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%12d", (int)(dbaAcmm.adjustBonus.doubleValue())), 12)) //本期回饋及調整
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%12d", (int)(dbaAcmm.useBonus.doubleValue())), 12)) //本期兌換點數
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%12d", (int)(dbaAcmm.netBonus.doubleValue())), 12)) //本期結餘點數
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(commDate.toTwDate(dbaAcmm.eraseDate), 7)) //點數到期日
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%12d", (int)(dbaAcmm.eraseBonus.doubleValue())), 12)) //到期點數
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 215)) //保留欄位
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAcmm.bankBranchNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(SEND_BY_EMAIL, 6)) //郵遞區號
		  .append(LINE_SEPERATOR)
		  ;
		return sb.toString();
	}
	
// ************************************************************************	
	
	private String getRecord06Str(DbaAcmm dbaAcmm, int msgNo, String message) throws UnsupportedEncodingException {		
		StringBuilder sb = new StringBuilder();
		sb.append(commCrd.fixLeft(dbaAcmm.pSeqno, 16))  //銷帳編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft("06", 2)) //RECORD TYPE
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAcmm.cardNo, 16)) //卡號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(String.format("%03d", msgNo), 3)) //訊息編號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(message, 280)) //對帳單訊息
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(" ", 16)) //保留欄位
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(dbaAcmm.bankBranchNo, 4)) //分行代號
		  .append(COL_SEPERATOR)
		  .append(commCrd.fixLeft(SEND_BY_EMAIL, 6)) //郵遞區號
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
		private DbaAcmm getDbaAcmm() throws Exception {
			DbaAcmm dbaAcmm = new DbaAcmm();
			dbaAcmm.pSeqno = getValue("DBA_ACMM.P_SEQNO");
			dbaAcmm.acctMonth = getValue("DBA_ACMM.ACCT_MONTH");
			dbaAcmm.stmtCycle = getValue("DBA_ACMM.STMT_CYCLE");
			dbaAcmm.acctNo = getValue("DBA_ACMM.ACCT_NO");
			dbaAcmm.acctType = getValue("DBA_ACMM.ACCT_TYPE");
			dbaAcmm.acctKey = getValue("DBA_ACMM.ACCT_KEY");
			dbaAcmm.idPSeqno = getValue("DBA_ACMM.ID_P_SEQNO");
			dbaAcmm.cardNo = getValue("DBA_ACMM.CARD_NO");
			dbaAcmm.nation = getValue("DBA_ACMM.NATION");
			dbaAcmm.chineseName = getValue("DBA_ACMM.CHINESE_NAME");
			dbaAcmm.chiTitle = getValue("DBA_ACMM.CHI_TITLE");
			dbaAcmm.birthday = getValue("DBA_ACMM.BIRTHDAY");
			dbaAcmm.eMailAddr = getValue("DBA_ACMM.E_MAIL_ADDR");
			dbaAcmm.zipCode = getValue("DBA_ACMM.ZIP_CODE");
			dbaAcmm.zipCodeChin = getValue("DBA_ACMM.ZIP_CODE_CHIN");
			dbaAcmm.mailAddr1 = getValue("DBA_ACMM.MAIL_ADDR_1");
			dbaAcmm.mailAddr2 = getValue("DBA_ACMM.MAIL_ADDR_2");
			dbaAcmm.cycleDate = getValue("DBA_ACMM.CYCLE_DATE");
			dbaAcmm.bankBranchNo = getValue("DBA_ACMM.BANK_BRANCH_NO");
			dbaAcmm.sequentialNo = getValue("DBA_ACMM.SEQUENTIAL_NO");
			dbaAcmm.printSequential = getValue("DBA_ACMM.PRINT_SEQUENTIAL");
			dbaAcmm.ttlDeductDone = getValueBigDecimal("DBA_ACMM.TTL_DEDUCT_DONE");
			dbaAcmm.ttlDeductNotyet = getValueBigDecimal("DBA_ACMM.TTL_DEDUCT_NOTYET");
			dbaAcmm.ttlPurchase = getValueBigDecimal("DBA_ACMM.TTL_PURCHASE");
			dbaAcmm.ttlPurchCnt = getValueBigDecimal("DBA_ACMM.TTL_PURCH_CNT");
			dbaAcmm.problemTxCnt = getValueInt("DBA_ACMM.PROBLEM_TX_CNT");
			dbaAcmm.statementCount = getValueInt("DBA_ACMM.STATEMENT_COUNT");
			dbaAcmm.printFlag = getValue("DBA_ACMM.PRINT_FLAG");
			dbaAcmm.lastMonthBonus = getValueBigDecimal("DBA_ACMM.LAST_MONTH_BONUS");
			dbaAcmm.newAddBonus = getValueBigDecimal("DBA_ACMM.NEW_ADD_BONUS");
			dbaAcmm.adjustBonus = getValueBigDecimal("DBA_ACMM.ADJUST_BONUS");
			dbaAcmm.useBonus = getValueBigDecimal("DBA_ACMM.USE_BONUS");
			dbaAcmm.netBonus = getValueBigDecimal("DBA_ACMM.NET_BONUS");
			dbaAcmm.eraseBonus = getValueBigDecimal("DBA_ACMM.ERASE_BONUS");
			dbaAcmm.eraseDate = getValue("DBA_ACMM.ERASE_DATE");
			dbaAcmm.modPgm = getValue("DBA_ACMM.MOD_PGM");
			dbaAcmm.modSeqno = getValueInt("DBA_ACMM.MOD_SEQNO");
			dbaAcmm.unprintFlagRegular = getValue("DBA_ACMM.UNPRINT_FLAG_REGULAR");
			return dbaAcmm;
		}
		
// ************************************************************************
		private ArrayList<String> getMessageList(String msgType, String acctMonth) throws Exception {
			/** 讀取PTR_BILLMSG(多筆) **/
			int selectCnt = selectPtrBillmsg(msgType, acctMonth);
			String paramStr;
			ArrayList<String> list = new ArrayList<>(selectCnt);
			for (int i = 0 ; i < selectCnt ; i++) {
				paramStr = "";
				for(int j = 1; j <= 5 ; j++) {
					String str = getValue("PTR_BILLMSG.PARAM"+j, i);
					if (str != null && str.trim().isEmpty() == false) 
						paramStr += str; 
				}
				
				list.add(paramStr);
			}

			return list;
		}
		
// ************************************************************************
		private int selectPtrBillmsg(String msgType, String acctMonth) throws Exception {
			extendField = "PTR_BILLMSG.";
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT PARAM1, PARAM2, PARAM3, PARAM4, PARAM5 ")
			  .append(" FROM PTR_BILLMSG ")
			  .append(" WHERE ACCT_TYPE = '90' ")
			  .append(" AND MSG_MONTH = (select max(MSG_MONTH) from PTR_BILLMSG WHERE ACCT_TYPE = '90' AND MSG_TYPE = ? AND APR_FLAG = 'Y' ) ")
			  .append(" AND MSG_TYPE = ? ")
			  .append(" AND APR_FLAG = 'Y' ")
			  .append(" ORDER BY MSG_CODE ")
			  ;
			sqlCmd = sb.toString();
			setString(1, msgType);
			setString(2, msgType);
			return selectTable();
		}

// ************************************************************************
	private List<DbaAbem> getDbaAbemList(String pSeqno, String acctMonth) throws Exception {
		/** 讀取DBA_JRNL(多筆) **/
		int selectCnt = selectDbaAbem(pSeqno, acctMonth);
		List<DbaAbem> dbaAbemList = new ArrayList<>(selectCnt);
		for (int i = 0 ; i < selectCnt ; i++) {
			DbaAbem dbaAbem = new DbaAbem();
			dbaAbem.pSeqno = getValue("DBA_ABEM.P_SEQNO", i);
			dbaAbem.acctMonth = getValue("DBA_ABEM.ACCT_MONTH", i);
			dbaAbem.printType = getValue("DBA_ABEM.PRINT_TYPE", i);
			dbaAbem.printSeq = getValueInt("DBA_ABEM.PRINT_SEQ", i);
			dbaAbem.acctCode = getValue("DBA_ABEM.ACCT_CODE", i);
			dbaAbem.tranClass = getValue("DBA_ABEM.TRAN_CLASS", i);
			dbaAbem.referenceNo = getValue("DBA_ABEM.REFERENCE_NO", i);
			dbaAbem.cardNo = getValue("DBA_ABEM.CARD_NO", i);
			dbaAbem.purchaseDate = getValue("DBA_ABEM.PURCHASE_DATE", i);
			dbaAbem.acctDate = getValue("DBA_ABEM.ACCT_DATE", i);
			dbaAbem.transactionAmt = getValueBigDecimal("DBA_ABEM.TRANSACTION_AMT", i);
			dbaAbem.description = getValue("DBA_ABEM.DESCRIPTION", i);
			dbaAbem.txnCode = getValue("DBA_ABEM.TXN_CODE", i);
			dbaAbem.exchangeDate = getValue("DBA_ABEM.EXCHANGE_DATE", i);
			dbaAbem.sourceCurr = getValue("DBA_ABEM.SOURCE_CURR", i);
			dbaAbem.sourceAmt = getValueBigDecimal("DBA_ABEM.SOURCE_AMT", i);
			dbaAbem.mchtCountry = getValue("DBA_ABEM.MCHT_COUNTRY", i);
			dbaAbem.modPgm = getValue("DBA_ABEM.MOD_PGM", i);
			dbaAbem.modSeqno = getValueInt("DBA_ABEM.MOD_SEQNO", i);
			dbaAbemList.add(dbaAbem);
		}

		return dbaAbemList;
	}

// ************************************************************************
	private int selectDbaAbem(String pSeqno, String acctMonth) throws Exception {
		extendField = "DBA_ABEM.";
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT * ")
		  .append(" FROM DBA_ABEM ")
		  .append(" WHERE 1=1 ")
		  .append(" AND P_SEQNO = ? ")
		  .append(" AND ACCT_MONTH = ? ")
		  .append(" ORDER BY PRINT_TYPE, PRINT_SEQ ")
		  ;
		sqlCmd = sb.toString();
		setString(1, pSeqno);
		setString(2, acctMonth);
		return selectTable();
	}
	
//************************************************************************
	private int selectDbaAcmm(String acctMonth) throws Exception {
		fetchExtend = "DBA_ACMM.";
		sqlCmd = " SELECT * FROM DBA_ACMM WHERE ACCT_MONTH = ? and STATEMENT_COUNT >0 ";
		setString(1, acctMonth);
		return openCursor();
	}

//************************************************************************
//private String selectPtrBusinday() throws Exception {
	void selectPtrBusinday() throws Exception {
		selectSQL = " BUSINESS_DATE,THIS_CLOSE_DATE,rowid as rowid  ";
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		selectTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", "select ptr_businday error!");
			exitProgram(1);
		}

    hPtrBusiRowid = getValue("rowid");
	  hCurrBusinessDate = getValue("BUSINESS_DATE");
		businessDate      = getValue("THIS_CLOSE_DATE");
		return;
	}

// ************************************************************************ 
	public boolean isWordDay(String businessDate) throws Exception {
		CommString  commStr = new CommString();
		if(!"01".equals(commStr.right(businessDate, 2))) {
			return false;
		}
		extendField = "wday.";
		selectSQL = "stmt_cycle";
		daoTable = "ptr_workday";
		whereStr = "where this_close_date = ? ";

		setString(1, businessDate);

		selectTable();

		if (notFound.equals("Y"))
			return false;

		return true;
	}

//************************************************************************
  void updatePtrBusinday() throws Exception
  {
    updateSQL = "this_close_date = '',"
              + "mod_pgm         = ?,"
              + "mod_time        = timestamp_format(?,'yyyymmddhh24miss')";
    daoTable  = "ptr_businday";
    whereStr  = "WHERE ROWID = ? ";
   
    setString(1 , javaProgram);
    setString(2 , sysDate+sysTime);
    setRowId(3  , hPtrBusiRowid);
   
    int recCnt = updateTable();
   
    if ( notFound.equals("Y") )
       {
        showLogMessage("I","","update_ptr_businday error!" );
        showLogMessage("I","","rowid=["+hPtrBusiRowid+"]");
        exitProgram(1);
       }
    return;
  }

//************************************************************************ 
    void selectLastAcctMonth() throws Exception {
        hLastAcctMonth = "";
        hLastAcctMonth = commDate.dateAdd(businessDate, 0, -1, 0).substring(0,6) ;
        /*
        sqlCmd = "select to_char(add_months(to_date(business_date,'yyyymmdd'),-1),'yyyymm') LastAcctMonth ";
		    sqlCmd += "from ptr_businday fetch first 1 rows only";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hLastAcctMonth = getValue("LastAcctMonth");
        } */
    }

} // End of class FetchSample
