/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/10  V1.00.00   Castor       program initial                        *
*  112/04/18  V1.00.01   SUNNY    將產生在media的檔案搬到CRM指定的目錄,取消產生月檔       *
*  112/04/20  V1.00.02   Ryan         【指定參數日期 or執行日期 (如searchDate)】-1。                                                     *	
*****************************************************************************/
package Inf;



import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Locale;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;



public class InfR012 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送CRM-債務協商作業全部資料 112/04/20  V1.00.02";
	private static final String CRM_FOLDER = "media/crm/";
	private static final String DATA_FORM = "PCY5";
	private final String lineSeparator = System.lineSeparator();
    CommCrdRoutine comcr = null;	
	CommCrd commCrd = new CommCrd();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CommDate commDate = new CommDate();
	private boolean isLastBusinday = false;

    String hBusiBusinessDate = "";

	String searchDate = "";

	public int mainProcess(String[] args) {

		try {
			CommCrd comc = new CommCrd();
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			// =====================================
			  // 檢核參數的日期格式
		       if (args.length >= 1) {
		     	   showLogMessage("I", "", "PARM 1 : [無參數] 表示抓取businday ");
		    	   showLogMessage("I", "", "PARM 1 : [SYSDAY] 表示抓取系統日");
		    	   showLogMessage("I", "", "PARM 1 : [YYYYMMDD] 表示人工指定執行日");
				}
				if(args.length == 1) {
					if (!args[0].toUpperCase(Locale.TAIWAN).equals("SYSDAY")) {
							if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
				        showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
				        return -1;
				    }
					}
				    searchDate = args[0];
				}
				
				//檢查日期
				 if(args.length >  0) {
		               if(args[0].length() != 8) {
		            	   comc.errExit("參數日期長度錯誤, 請重新輸入! [yyyymmdd]", "");
		                 }
		            }

			commCol = new CommCol(getDBconnect(), getDBalias());
			commTxInf = new CommTxInf(getDBconnect(), getDBalias());
				
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));

			searchDate = getProgDate(searchDate, "D");
			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
			
			String searchDate2 = getProgDate(searchDate, "M");

			showLogMessage("I", "", String.format("執行日期D[%s]", searchDate));
			showLogMessage("I", "", String.format("執行日期M[%s]", searchDate2));

			//檢核searchDate是否為每月最後一天營業日
			//20230418 sunny 取消產生月檔
			//isLastBusinday = commCol.isLastBusinday(searchDate);
			
			// convert YYYYMMDD into YYMMDD
			String fileNameSearchDate = searchDate.substring(2);

			// convert YYYYMM into YYMM
			String fileNameSearchDate2 = searchDate2.substring(2);
			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(), CRM_FOLDER).toString();

			String datFileName2 = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate2, CommTxInf.DAT_EXTENSION);
			String fileFolder2 =  Paths.get(commCrd.getECSHOME(), CRM_FOLDER).toString();
			
			// 產生主要檔案 .DAT 
			int dataCount = generateDatFile(fileFolder, datFileName);
			
			// 產生Header檔
			CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
			dateTime(); // update the system date and time
			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
			if (isGenerated == false) {
				comc.errExit("產生HDR檔錯誤!", "");
			}

			//每月最後一個營業日多產生一份
			if(isLastBusinday) {
				copyFile(datFileName,fileFolder,datFileName2,fileFolder2);
				boolean isGenerated2 = commTxInf.generateTxtCrmHdr(fileFolder2, datFileName2, searchDate, sysDate, sysTime.substring(0,4), dataCount);
				if (isGenerated2 == false) {
					comc.errExit("每月最後一個營業日產生HDR檔錯誤!", "");
				}
			}
			
			// PCY5_YYMMDD.DAT -> PCY5_YYMMDD.HDR
			String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			String hdrFileName2 = datFileName2.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			
			// run FTP
			procFTP(fileFolder, datFileName, hdrFileName);
			if(isLastBusinday)
				procFTP(fileFolder2, datFileName2, hdrFileName2);
			showLogMessage("I", "", "執行結束");
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}

	/**
	 * generate a .Dat file
	 * @param fileFolder 檔案的資料夾路徑
	 * @param datFileName .dat檔的檔名
	 * @return the number of rows written. If the returned value is -1, it means the path or the file does not exist. 
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFileName) throws Exception {
		
		selectCrStatusData();
		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				String idNo = getValue("id_no");
				String cpbdueType = getValue("cpbdue_type");
				String cpbdueBankType = getValue("cpbdue_bank_type");
				String cpbdueTcbType = getValue("cpbdue_tcb_type");
				String cpbdueRate = String.format("% 2.2f",getValueDouble("cpbdue_rate")).replace(" ", "0");
				String cpbduePeriod = String.format("%03d",getValueInt("cpbdue_period"));
				String cpbdueTotalPayamt = String.format("%08d",getValueInt("cpbdue_total_payamt"));
				String cpbdueDueCardAmt = String.format("%08d",getValueInt("cpbdue_due_card_amt"));
				String cpbdueOwnerBank = getValue("cpbdue_owner_bank");
				String cpbdueMediType = getValue("cpbdue_medi_type");
				String rowOfDAT = getRowOfDAT(idNo,cpbdueType,cpbdueBankType,cpbdueTcbType,cpbdueRate,cpbduePeriod,cpbdueTotalPayamt,cpbdueDueCardAmt,cpbdueOwnerBank,cpbdueMediType);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes("MS950");
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}
			
			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			}else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}

	/**
	 * 產生檔案，如下格式<br>
	 * 01	客戶編號		            X(11)	1-11	　<br>
	 * 		區隔符號	     	        X(1)	12		區隔符號「ASC(6)」<br>
	 * 02	協商方式		            X(01)	13-13	 1：銀行公會協商, 2：本行債務協商 ,3 :  前置調解  <br>
	 * 		區隔符號	     	        X(1)	14		區隔符號「ASC(6)」<br>
	 * 03	銀行公會債務協商	        X(01)	15-15	 公會協商狀態 1：受理 2：停催 3：協商成立 4：復催 5：毀諾 6：還清 <br>
	 * 		區隔符號	     	        X(1)	16		區隔符號「ASC(6)」<br>
	 * 04	本行債務協商註記         X(01)	17-17	 個別協商狀態 1：受理 2：停催 3：協商成立 4：復催 5：毀諾 6：還清  <br>
	 * 		區隔符號	     	        X(1)	18		區隔符號「ASC(6)」<br>
	 * 05	債協利率(年)	            9(05)	19-23     <br>
	 * 		區隔符號	     	        X(1)	24		區隔符號「ASC(6)」<br>
	 * 06	債協期數	                9(03)	25-27	  <br>
	 * 		區隔符號	     	        X(1)	28		區隔符號「ASC(6)」<br>
	 * 07	累計繳款金額	            9(08)	29-36	 現行長度 8 (含負號不含小數點)  <br>
	 * 		區隔符號	     	        X(1)	37		區隔符號「ASC(6)」<br>
	 * 08	每期最低應繳金額_信用卡	9(08)	38-45	 現行長度 8 (含負號不含小數點)  <br>
	 * 		區隔符號	     	        X(1)	46		區隔符號「ASC(6)」<br>
	 * 09	最大債權銀行	            X(03)	47-49	  <br>
	 * 		區隔符號	     	        X(1)	50		區隔符號「ASC(6)」<br>
	 * 10	本行前置調解註記         X(01)	51-51	  <br>
	 * 		區隔符號	     	        X(1)	52		區隔符號「ASC(6)」<br>
	 * @param idNo
	 * @param cpbdueType
	 * @param cpbdueBankType
	 * @param cpbdueTcbType
	 * @param cpbdueRate
	 * @param cpbduePeriod
	 * @param cpbdueTotalPayamt
	 * @param cpbdueDueCardAmt
	 * @param cpbdueOwnerBank
	 * @param cpbdueMediType
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getRowOfDAT(String idNo, String cpbdueType, String cpbdueBankType, String cpbdueTcbType, String cpbdueRate , String cpbduePeriod, String cpbdueTotalPayamt, String cpbdueDueCardAmt, String cpbdueOwnerBank, String cpbdueMediType) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft(idNo, 11));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbdueType, 1));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbdueBankType, 1));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbdueTcbType, 1));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixRight(cpbdueRate,5));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbduePeriod, 3));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbdueTotalPayamt, 8));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbdueDueCardAmt, 8));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbdueOwnerBank, 3));
		sb.append(commCrd.fixLeft("\006", 1));
		sb.append(commCrd.fixLeft(cpbdueMediType, 1));
		sb.append(commCrd.fixLeft("\006", 1));	
		sb.append(lineSeparator);
		return sb.toString();
	}

	private void selectCrStatusData() throws Exception {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT B.ID_NO,A.CPBDUE_TYPE,A.CPBDUE_BANK_TYPE,A.CPBDUE_TCB_TYPE,A.CPBDUE_RATE,A.CPBDUE_PERIOD,A.CPBDUE_TOTAL_PAYAMT,A.CPBDUE_DUE_CARD_AMT,A.CPBDUE_OWNER_BANK,A.CPBDUE_MEDI_TYPE ");
		sb.append("FROM COL_CPBDUE A ,CRD_IDNO B ");
		sb.append("WHERE A.CPBDUE_ID_P_SEQNO = B.ID_P_SEQNO ");
		sb.append("AND A.CPBDUE_ACCT_TYPE ='01' ");   /*CPBDUE_ACCT_TYPE=01,就找出持卡人ID*/
		sb.append("UNION ALL ");
		sb.append("SELECT B.CORP_NO,A.CPBDUE_TYPE,A.CPBDUE_BANK_TYPE,A.CPBDUE_TCB_TYPE,A.CPBDUE_RATE,A.CPBDUE_PERIOD,A.CPBDUE_TOTAL_PAYAMT,A.CPBDUE_DUE_CARD_AMT,A.CPBDUE_OWNER_BANK,A.CPBDUE_MEDI_TYPE ");
		sb.append("FROM COL_CPBDUE A ,CRD_CORP B  ");
		sb.append("WHERE A.CPBDUE_ID_P_SEQNO = B.CORP_P_SEQNO  ");
		sb.append("AND  A.CPBDUE_ACCT_TYPE ='03' ");   /*如果CPBDUE_ACCT_TYPE=03,就找出公司統編*/

		sqlCmd = sb.toString();
		openCursor();
	}
	
	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CRM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRM", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}
	
	///***********************************************************************/
	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = " select business_date ";
		sqlCmd += " from ptr_businday ";
		sqlCmd += " fetch first 1 rows only ";
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
	
		hBusiBusinessDate = getValue("business_date");
			
	}

	void copyFile(String datFileName1, String fileFolder1 ,String datFileName2, String fileFolder2) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder2, datFileName2).toString();

		if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName2 + "]copy失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已copy至 [" + tmpstr2 + "]");
	}
	
	public static void main(String[] args) {
		InfR012 proc = new InfR012();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}


