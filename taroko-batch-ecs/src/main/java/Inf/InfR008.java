/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  110/08/16  V1.00.00   JustinWu     program initial                        *
*  110/08/17  V1.00.01   JustinWu     use the common function: insertEcsNotifyLog*
*  110/08/20  V1.00.02   SunnyTs      將mainProcess private改 public           *
*  112/03/03  V1.00.03   Sunny        依CRM會議結論(marketAgreeBase)調整SQL條件     *
*  112/03/06  V1.00.04   Sunny        marketAgreeBase改定義，資轉尚未調整，先把Y(拒絕)/N(同意) 條件加入*
*  112/04/18  V1.00.05   Sunny        將產生在media的檔案搬到CRM指定的目錄                          *
*  112/04/20  V1.00.06   Ryan         【指定參數日期 or執行日期 (如searchDate)】-1  *	
*  112/07/06  V1.00.07   Sunny        修正因判斷卡特指會造成1個ID多筆情況，洽卡部弘奇討論取消判斷卡特指 *
*  112/07/07  V1.00.08   Sunny        修正結果，依活卡為主，再過濾是否為可使用的卡片*
*  112/10/24  V1.00.09   Sunny        增加程式參數檢核條件                                                 *       
*****************************************************************************/
package Inf;



import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommTxInf;



public class InfR008 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送CRM-客服中心「服務轉銷售」需求(CR_STATUS) 112/07/07  V1.00.08";
	private static final String CRM_FOLDER = "media/crm/";
	private static final String DATA_FORM = "CR_STATUS";
	private final static String COL_SEPERATOR = "\006";
	private final String lineSeparator = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
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
			
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			
			//檢查日期
			 if(args.length >  0) {
	               if(args[0].length() != 8) {
	            	   comc.errExit("參數日期長度錯誤, 請重新輸入! [yyyymmdd]", "");
	                 }
	            }
			 
			searchDate = getProgDate(searchDate, "D");
			
			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
			
			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));

			// convert YYYYMMDD into YYMMDD
			String fileNameSearchDate = searchDate.substring(2);
			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(), CRM_FOLDER).toString();
			
			// 產生主要檔案 .DAT 
			int dataCount = generateDatFile(fileFolder, datFileName);
			
			// 產生Header檔
			CommTxInf commTxInf = new CommTxInf(getDBconnect(), getDBalias());
			dateTime(); // update the system date and time
			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
			if (isGenerated == false) {
				comc.errExit("產生HDR檔錯誤!", "");
			}
			
			// CR_STATUS_YYMMDD.DAT -> CR_STATUS_YYMMDD.HDR
			String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			
			// run FTP
			procFTP(fileFolder, datFileName, hdrFileName);

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
				String noBlockCodeFlag = getValue("no_block_code_flag");
				String marketAgreeBase = getValue("market_agree_base");
				String rowOfDAT = getRowOfDAT(idNo, noBlockCodeFlag, marketAgreeBase);
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
	 * 目的：針對有控管碼或者是同意共銷(1,2)，符合任一項條件就產生。
	 * 產生檔案，如下格式<br>
	 * 01	正卡人ID		X(10)	1-10	　<br>
	 * 		區隔符號		X(1)	11		區隔符號「!」<br>
	 * 02	控管碼		X(01)	12-12	持有正卡且無控管碼註記 <br>
	 * 		區隔符號		X(1)	13		區隔符號「!」<br>
	 * 03	共銷揭露註記	X(01)	14-14	拒絕行銷的相反值。<br>
	 * 		區隔符號		X(1)	15		區隔符號「!」<br>
	 * 04	FILLER		X(5)	16-20	 
	 * @param idNo
	 * @param noBlockCodeFlag
	 * @param marketAgreeBase
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getRowOfDAT(String idNo, String noBlockCodeFlag, String marketAgreeBase) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append(commCrd.fixLeft(idNo, 10));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(noBlockCodeFlag, 1));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(marketAgreeBase, 1));
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(" ", 5));
		sb.append(lineSeparator);
		return sb.toString();
	}

	 /*
	   1. 控管碼欄位定義：即為有效卡&控管碼為空白之卡片為是(Y)，其他為否(N)
	     2. 只查ORG 106的資料
	     3. 控管碼與共銷揭露註記皆為N時，
	        停卡/上控管碼(N)的資料及不同意行銷(N)則會排除，
	        只產生控管碼(即無控管碼的資料)與共銷揭露註記(同意行銷)其中一值有Y的資料。
	    4. 卡部需求 :拒絶行銷此值未來將由Y不同意 / N 同意改為0 (不同意), 1 (同意共銷),  2(同意共享)，
	           未來新系統轉換成CRM時，0轉為N; 1 & 2轉為Y (目前轉檔尚未調整，如果是Y也視為Y)
	   */
     /* CRM欄位定義:    
		ID　身分證字號
		BLOCK_CODE　持有正卡且無控管碼註記 (Y/N)
		TCFHC_FLAG　金控信用卡業務共銷註記 (Y/N)
      */
	

//	private void selectCrStatusData_backup() throws Exception {
//		StringBuffer sb = new StringBuffer();
//		sb.append("SELECT * FROM ( ");
//		sb.append("SELECT DISTINCT d.ID_NO as id_no, ");
//		sb.append("(CASE WHEN a.current_code <> '0' THEN 'N' ");        /*戶凍結狀態Y，表不可用卡為N*/
////		sb.append("     WHEN LENGTH(b.SPEC_STATUS)>0 THEN 'N'");        /*卡特指有值，，表不可用卡為N*/
//		sb.append("     WHEN LENGTH(c.BLOCK_REASON1)>0 THEN 'N' ");     /*戶凍結1有值，表不可用卡為N*/
//		sb.append("     WHEN LENGTH(c.BLOCK_REASON2)>0 THEN 'N' ");     /*戶凍結2有值，表不可用卡為N*/
//		sb.append("     WHEN LENGTH(c.BLOCK_REASON3)>0 THEN 'N' ");     /*戶凍結3有值，表不可用卡為N*/
//		sb.append("     WHEN LENGTH(c.BLOCK_REASON4)>0 THEN 'N' ");     /*戶凍結4有值，表不可用卡為N*/
//		sb.append("     WHEN LENGTH(c.BLOCK_REASON5)>0 THEN 'N' ");     /*戶凍結5有值，表不可用卡為N*/
//		sb.append("     WHEN LENGTH(c.SPEC_STATUS)>0   THEN 'N' ");     /*戶特指有值，表不可用卡為N*/
//		sb.append("     ELSE 'Y' END) AS block_code, ");                /*其他，可正常用卡時，為Y*/
//		sb.append("decode(d.market_agree_base,'1','Y','2','Y','N','Y','N') AS market_agree_base");/*如果是Y也視同Y*/
//		/*卡部需求 :拒絶行銷此值未來將由Y/N改為0 (不同意), 1 (同意共銷),  2(同意共享)，未來新系統轉換成CRM時，0轉為N; 1 & 2轉為Y*/
//		sb.append("	FROM crd_card a, cca_card_base b, cca_card_acct c, crd_idno d");
//		sb.append("	WHERE a.CARD_NO = b.CARD_NO");
//		sb.append("	AND a.MAJOR_ID_P_SEQNO = b.ID_P_SEQNO ");
//		sb.append("	AND a.ACNO_P_SEQNO = c.ACNO_P_SEQNO");
//		sb.append("	AND a.MAJOR_ID_P_SEQNO = d.ID_P_SEQNO ");
//		sb.append("	AND a.ACCT_TYPE='01'");
//		sb.append("	) cr_status  WHERE block_code='Y' OR market_agree_base='Y' ");   /*其中一個條件為Y始成立*/
//		//sb.append("	AND a.CURRENT_CODE='0' "); /*有效卡*/
//		//sb.append("	AND b.SPEC_STATUS='' ");   /*卡特指無值，可用卡*/
//		sqlCmd = sb.toString();
//		openCursor();
//	}


	private void selectCrStatusData() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM (");		
		sb.append("SELECT ROW_NUMBER() OVER(PARTITION BY ID_NO ORDER BY no_block_code_flag desc) AS row_num,* FROM (");
		sb.append("SELECT d.ID_NO as id_no, ");
		sb.append("(CASE WHEN a.current_code <> '0' THEN 'N' ");        /*戶凍結狀態Y，表不可用卡為N*/
		sb.append("     WHEN LENGTH(b.SPEC_STATUS)>0 THEN 'N'");        /*卡特指有值，，表不可用卡為N*/
		sb.append("     WHEN LENGTH(c.BLOCK_REASON1)>0 THEN 'N' ");     /*戶凍結1有值，表不可用卡為N*/
		sb.append("     WHEN LENGTH(c.BLOCK_REASON2)>0 THEN 'N' ");     /*戶凍結2有值，表不可用卡為N*/
		sb.append("     WHEN LENGTH(c.BLOCK_REASON3)>0 THEN 'N' ");     /*戶凍結3有值，表不可用卡為N*/
		sb.append("     WHEN LENGTH(c.BLOCK_REASON4)>0 THEN 'N' ");     /*戶凍結4有值，表不可用卡為N*/
		sb.append("     WHEN LENGTH(c.BLOCK_REASON5)>0 THEN 'N' ");     /*戶凍結5有值，表不可用卡為N*/
		sb.append("     WHEN LENGTH(c.SPEC_STATUS)>0   THEN 'N' ");     /*戶特指有值，表不可用卡為N*/
		sb.append("     ELSE 'Y' END) AS no_block_code_flag, ");                /*其他，可正常用卡時，為Y*/
		sb.append("decode(d.market_agree_base,'1','Y','2','Y','N','Y','N') AS market_agree_base");/*如果是Y也視同N*/
		/*卡部需求 :拒絶行銷此值未來將由Y/N改為0 (不同意), 1 (同意共銷),  2(同意共享)，未來新系統轉換成CRM時，0轉為N; 1 & 2轉為Y*/
		sb.append("	FROM crd_card a, cca_card_base b, cca_card_acct c, crd_idno d");
		sb.append("	WHERE a.CARD_NO = b.CARD_NO");
		sb.append("	AND a.MAJOR_ID_P_SEQNO = b.ID_P_SEQNO ");
		sb.append("	AND a.ACNO_P_SEQNO = c.ACNO_P_SEQNO");
		sb.append("	AND a.MAJOR_ID_P_SEQNO = d.ID_P_SEQNO ");
		sb.append("	AND a.ACCT_TYPE='01' and current_code='0'"); //以有效卡為主
		sb.append("	) cr_status  ");
		sb.append("	WHERE no_block_code_flag='Y' OR market_agree_base='Y' ");   /*其中一個條件為Y始成立*/
		sb.append("	) cr_status_order WHERE row_num='1' ");   /*依no_block_code_flag(Y/N)倒序排序，即先取Y，再取N*/
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
	

	public static void main(String[] args) {
		InfR008 proc = new InfR008();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}


