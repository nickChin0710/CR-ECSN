/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/08/01  V1.00.00   Ryan     program initial                            *
*  112/09/07  V1.00.01   Ryan     欄位調整                                                                                                        *
*  112/09/14  V1.00.02   Ryan     繳款義務人之識別碼相同時帳單別註記不能重複(01,02,03....)                    *                                                                                    *
*****************************************************************************/
package Act;

import java.nio.file.Paths;
import java.util.HashMap;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

public class ActL100 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生報送中央存保信用卡戶帳款資料程式 112/09/14 V1.00.02";
	private static final String TMP_FOLDER = "/media/act/";
	private static final String FOLDER_FROM = "CRDATACREA";
	private static final String DATA_FORM = "0060000A52.YYYMMDD";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	
	HashMap<String,Integer> idCorpNoMap = new HashMap<String,Integer>();
	
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
			
			commCol = new CommCol(getDBconnect(), getDBalias());
			commTxInf = new CommTxInf(getDBconnect(), getDBalias());
			// =====================================
			
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");

			showLogMessage("I", "", String.format("今日營業日=[%s]", searchDate));
			
			// get the name and the path of the file
			String fileName = DATA_FORM.replace("YYYMMDD", commDate.toTwDate(searchDate));
			String fileFolder =  Paths.get(commCrd.getECSHOME(),TMP_FOLDER).toString();
			
			// 產生主要檔案 
			generateDatFile(fileFolder, fileName);

			dateTime(); // update the system date and time
			// run FTP
			procFTP(fileFolder, fileName);
			
			renameFile(fileFolder, fileName);

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
	 * generate file
	 * @param fileFolder 檔案的資料夾路徑
	 * @param fileName 
	 * @return the number of rows written. If the returned value is -1, it means the path or the file does not exist. 
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String fileName ) throws Exception {

		
		String datFilePath = Paths.get(fileFolder, fileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		selectActL100Data();
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			showLogMessage("I", "", "開始產生檔案......");
			//寫入明細
			StringBuffer sb = new StringBuffer();
			while (fetchTable()) {
				ActL100Data actL100Data = getInfData();
				String rowOfDAT = getRowOfDetailDAT(actL100Data);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes();
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes();
				writeBinFile(tmpBytes, tmpBytes.length);
			}

			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入檔案");
			}else {
				showLogMessage("I", "", String.format("檔案產生完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}

	/**
	 * 明細
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDetailDAT(ActL100Data actL100Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		selectActDebt1(actL100Data);
		selectActDebt2(actL100Data);
		selectActDebt3(actL100Data);
		selectActJcicLog(actL100Data);
		sumCrdbTotamt(actL100Data);

		sb.append(commCrd.fixLeft("006", 3)); //總機構代號 X(3)
		sb.append(commCrd.fixLeft("0000", 4));//分支機構代號 X(4)
		sb.append(commCrd.fixLeft(actL100Data.idCorpNo, 20));//繳款義務人之識別碼 X(20)
		sb.append(commCrd.fixLeft(String.format("%02d", actL100Data.idCorpNoCnt), 2));//帳單別註記 X(2)
		sb.append(commCrd.fixLeft(String.format("%08.5f", actL100Data.rcrateYear), 8));//循環信用利率 9(2).9(5)
		sb.append(commCrd.fixLeft(String.format("%08d", actL100Data.thisCloseDate), 8));//本期結帳日(最近一次結帳) 9(8)
		sb.append(commCrd.fixLeft(String.format("%08d", actL100Data.thisLastpayDate), 8));//本期繳款截止日 9(8)
		sb.append(commCrd.fixRight(String.format("%016.2f", actL100Data.crdbClpbal), 16));//截至最近一次結帳日（本期）未繳付之催收或轉銷呆帳或應收帳款之本金餘額  9(12).99
		sb.append(commCrd.fixRight(String.format("%015.2f", actL100Data.crdbCint), 15));//截至最近一次結帳日（本期）未繳付之循環信用利息餘額 9(12).99
		sb.append(commCrd.fixRight(String.format("%015.2f", actL100Data.crdbCpenalt), 15));//截至最近一次結帳日（本期）未繳付之違約金 9(12).99
		sb.append(commCrd.fixRight(String.format("%016.2f", actL100Data.crdbCcommi), 16));//截至最近一次結帳日（本期）未繳付之其他費用 9(12).99
		sb.append(commCrd.fixRight(String.format("%016.2f", actL100Data.crdbLlpbal), 16));  //最近一次結帳日至基準日新增之消費金額 9(12).99
		sb.append(commCrd.fixRight(String.format("%016.2f", actL100Data.crdbLadcash), 16));  //最近一次結帳日至基準日新增之預借現金金額 9(12).99
		sb.append(commCrd.fixRight(String.format("%015.2f", actL100Data.crdbLint), 15));  //最近一次結帳日至基準日應繳循環信用利息餘額 9(12).99
		sb.append(commCrd.fixRight(String.format("%015.2f", actL100Data.crdbLcommi), 15));  //最近一次結帳日至基準日新增之其他費用 9(12).99
		sb.append(commCrd.fixRight(String.format("%016.2f", actL100Data.crdbLadjamt), 16));  //最近一次結帳日至基準日新增之調整金額  9(12).99
		sb.append(commCrd.fixRight(String.format("%015.2f", actL100Data.crdbLitigfee), 15));  //訴訟墊款餘額(其他應收款)  9(12).99
		sb.append(commCrd.fixRight(String.format("%016.2f", actL100Data.crdbTotamt), 16));  //截至基準日之債權總餘額  9(12).99
		sb.append(commCrd.fixLeft(actL100Data.crdbLstatusa, 1));  //上期繳款狀況代號  X(1)
		sb.append(commCrd.fixLeft(actL100Data.crdbCreditor, 1));  //債權狀態註記  X(1)
		sb.append(commCrd.fixLeft(actL100Data.currCode, 3));  //幣別  X(3)
		sb.append(commCrd.fixLeft(actL100Data.autopayAcctBank, 7));  //自動扣帳之金融機構代碼 X(7)
		sb.append(commCrd.fixLeft(actL100Data.autopayAcctNo, 30));  //自動扣款之存款帳號 X(30)
		sb.append(commCrd.fixLeft(String.format("%08d", actL100Data.crdbLastpayd), 8));  //未繳足最低金額之繳款迄日 9(8)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	ActL100Data getInfData() throws Exception {
		ActL100Data actL100Data = new ActL100Data();
		actL100Data.idCorpNo = getValue("ID_CORP_NO");
		actL100Data.pSeqno = getValue("P_SEQNO");
		actL100Data.idCorpNoCnt = idCorpNoMap.get(actL100Data.idCorpNo)==null ? 1 : idCorpNoMap.get(actL100Data.idCorpNo).intValue() + 1;
		actL100Data.rcrateYear = getValueDouble("RCRATE_YEAR");
		actL100Data.thisCloseDate = getValueInt("THIS_CLOSE_DATE");
		actL100Data.thisLastpayDate = getValueInt("THIS_LASTPAY_DATE");
		actL100Data.currCode = getValue("CURR_CODE");
		actL100Data.autopayAcctBank = getValue("AUTOPAY_ACCT_BANK");
		actL100Data.autopayAcctNo = getValue("AUTOPAY_ACCT_NO");
		actL100Data.crdbCreditor = getValue("CRDB_CREDITOR");
		actL100Data.crdbLadjamt = getValueDouble("CRDB_LADJAMT");
		idCorpNoMap.put(actL100Data.idCorpNo, actL100Data.idCorpNoCnt);
		return actL100Data;
	}

	private void selectActL100Data() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A.P_SEQNO , DECODE(A.ACCT_TYPE,'01',decode(UF_IDNO_ID(C.ID_P_SEQNO),'',A.P_SEQNO,UF_IDNO_ID(C.ID_P_SEQNO)),decode(UF_CORP_NO(C.CORP_P_SEQNO),'',A.P_SEQNO,UF_CORP_NO(C.CORP_P_SEQNO))) AS ID_CORP_NO, ")
		.append(" C.RCRATE_YEAR ,B.THIS_CLOSE_DATE ,B.THIS_LASTPAY_DATE ,DECODE(A.CURR_CODE,'840','USD','392','JPY','NTD') AS CURR_CODE,C.AUTOPAY_ACCT_BANK ,C.AUTOPAY_ACCT_NO ")
		.append(" ,DECODE(C.ACCT_STATUS,'1','9','2','9','3','A','4','B','5','9',C.ACCT_STATUS) as CRDB_CREDITOR ")
		.append(" ,(D.ADJUST_DR_AMT - D.ADJUST_CR_AMT) AS CRDB_LADJAMT ")
		.append(" FROM ACT_CURR_HST A JOIN PTR_WORKDAY B ON A.STMT_CYCLE = B.STMT_CYCLE ")
		.append(" AND A.ACCT_MONTH = to_char(to_date(B.THIS_ACCT_MONTH,'yyyymm') - 1 months,'yyyymm') ")
		.append(" LEFT JOIN ACT_ACNO C ON A.P_SEQNO = C.P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT D ON A.P_SEQNO = D.P_SEQNO ")
		.append(" ORDER By ID_CORP_NO, A.ACCT_TYPE , A.STMT_CYCLE, A.P_SEQNO ");
		sqlCmd = sb.toString();
		openCursor();
	}
	
	private void sumCrdbTotamt(ActL100Data actL100Data) {
		String tmpAmt = String.format("%.2f",(
				actL100Data.crdbClpbal 
				+ actL100Data.crdbCint 
				+ actL100Data.crdbCpenalt 
				+ actL100Data.crdbCcommi
				+ actL100Data.crdbLlpbal 
				+ actL100Data.crdbLadcash 
				+ actL100Data.crdbLint 
				+ actL100Data.crdbLcommi 
				+ actL100Data.crdbLadjamt
				+ actL100Data.crdbLitigfee ));
		 
		 actL100Data.crdbTotamt = commStr.ss2Num(tmpAmt);
	}
	
	private void selectActJcicLog(ActL100Data actL100Data) throws Exception {
		extendField = "jcic.";
		sqlCmd = "select a.payment_amt_rate from act_jcic_log a,ptr_workday p ";
		sqlCmd += "where a.stmt_cycle = p.stmt_cycle and a.acct_month = p.this_acct_month and a.p_seqno = ? ";
		setString(1,actL100Data.pSeqno);
		selectTable();
		actL100Data.crdbLstatusa = getValue("jcic.payment_amt_rate");
	}
	
	private void selectActDebt1(ActL100Data actL100Data) throws Exception {
		extendField = "debt1.";
		sqlCmd = "select sum(a.end_bal) as sum_end_bal1 "
				+ "from act_debt a,ptr_actcode b,ptr_workday c " + "where a.acct_code = b.acct_code "
				+ "and a.stmt_cycle = c.stmt_cycle " + "and a.acct_month <= c.this_acct_month " 
				+ "and a.p_seqno = ? ";
		setString(1,actL100Data.pSeqno);
		selectTable();
		actL100Data.crdbClpbal = getValueDouble("debt1.sum_end_bal1");
	}
	
	private void selectActDebt2(ActL100Data actL100Data) throws Exception {
		extendField = "debt2.";
		sqlCmd = "select a.billed_end_bal_ri,a.billed_end_bal_pn,"
				+ " (a.billed_end_bal_pf + a.billed_end_bal_af + a.billed_end_bal_cf) as billed_end_bal_pac "
				+ " from act_acct_hst a, ptr_workday c " 
				+ " where a.stmt_cycle = c.stmt_cycle "
				+ " and a.acct_month = c.last_acct_month " 
				+ " and a.p_seqno = ? ";
		setString(1, actL100Data.pSeqno);
		int resultCnt = selectTable();
		if (resultCnt > 0) {
			actL100Data.crdbCint = getValueDouble("debt2.billed_end_bal_ri");
			actL100Data.crdbCpenalt = getValueDouble("debt2.billed_end_bal_pn");
			actL100Data.crdbCcommi = getValueDouble("debt2.billed_end_bal_pac");
		}
	}
	
	private void selectActDebt3(ActL100Data actL100Data) throws Exception {
		extendField = "debt3.";
		sqlCmd = "select a.acct_code, sum(nvl(a.end_bal,0)) sum_end_bal3 "
				+ "from act_debt a,ptr_actcode b,ptr_workday c " 
				+ "where a.acct_code in ('BL','CA','RI','PF','AF','CF','SF') "
				+" and a.acct_code = b.acct_code "
				+ "and a.stmt_cycle = c.stmt_cycle " /*+ "and a.acct_month < c.this_acct_month "*/
				+ "and a.p_seqno = ? "
		 		+ "group by a.acct_code ";
		setString(1,actL100Data.pSeqno);
		int resultCnt = selectTable();
		for(int i = 0 ; i < resultCnt ; i++) {
			switch(getValue("debt3.acct_code",i)) {
			case "BL":
				actL100Data.crdbLlpbal = getValueDouble("debt3.sum_end_bal3",i);
				break;
			case "CA":
				actL100Data.crdbLadcash = getValueDouble("debt3.sum_end_bal3",i);
				break;
			case "RI":
				actL100Data.crdbLint = getValueDouble("debt3.sum_end_bal3",i);
				break;
			case "PF":
			case "AF":
			case "CF":
				actL100Data.crdbLcommi += getValueDouble("debt3.sum_end_bal3",i);
				break;
			case "SF":
				actL100Data.crdbLitigfee = getValueDouble("debt3.sum_end_bal3",i);
				break;
			}
		}
	}
	
	void procFTP(String fileFolder, String datFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = FOLDER_FROM; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 傳送
		String ftpCommand = String.format("mput %s", datFileName);
		
		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(FOLDER_FROM, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void renameFile(String fileFolder, String datFileName) throws Exception {
		String tmpstr1 = Paths.get(fileFolder ,datFileName).toString();
		String tmpstr2 = Paths.get(fileFolder, "/backup/" , String.format("%s_%s%s", datFileName,sysDate,sysTime)).toString();

		if (commCrd.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) {
		ActL100 proc = new ActL100();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class ActL100Data{
	String idCorpNo = "";
	String pSeqno = "";
	int idCorpNoCnt = 0;
	double rcrateYear = 0.0;
	int thisCloseDate = 0;
	int thisLastpayDate = 0;
	double crdbClpbal = 0.0;
	double crdbCint = 0.0;
	double crdbCpenalt = 0.0;
	double crdbCcommi = 0.0;
	double crdbLlpbal = 0.0;
	double crdbLadcash = 0.0;
	double crdbLint = 0.0;
	double crdbLcommi = 0.0;
	double crdbLadjamt = 0.0;
	double crdbLitigfee = 0.0;
	double crdbTotamt = 0.0;
	String crdbLstatusa = "";
	String crdbCreditor = "";
	String currCode = "";
	String autopayAcctBank = "";
	String autopayAcctNo = "";
	int crdbLastpayd = 0;
}




