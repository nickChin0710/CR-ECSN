/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/18  V1.00.00   Ryan     program initial                            *
*  112/04/20  V1.00.01   Ryan     【指定參數日期 or執行日期 (如searchDate)】-1         *
*  112/05/19  V1.00.02   Sunny    修正payment_no,payment_no_II                 *
*  112/05/19  V1.00.03   Ryan     增加一個檔案
*  112/05/23  V1.00.04   Ryan     mark掉以下getCpbdueCurrType的處理, 改直接讀col_cs_rpt.pay_by_stage_flag
*  112/06/17  V1.00.05   Sunny    調整檔名日期為YYYYMMDD                        *
*  112/07/18  V1.00.06   Ryan     ICW10 add Header                         *
*  112/08/22  V1.00.07   Ryan     檔案寫入筆數顯示10000 改為 1000                                   *
*  112/01/04  V1.00.08   Sunny    調整檔案送給卡部位置(NCR2TCB改為CREDITCARD)    *
*  112/01/05  V1.00.09   Sunny    移除debug處理                                                                      *
*****************************************************************************/
package Inf;

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

import Cca.CalBalance;

public class InfS009 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 1000;
	private final String progname = "產生送客服批次檔案-催收檔(ICW10) 112/01/05  V1.00.09";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTICRX";
	private static final String DATA_FORM2 = "ICW10_YYYYMMDD.TXT";												
	private final static String COL_SEPERATOR = "\006";
    private final static String COL_SEPERATOR2 = ",";
	private final static String FTP_FOLDER = "NEWCENTER";
	private final static String FTP_FOLDER2 = "CREDITCARD";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CalBalance calBalance = null;
	StringBuffer copySb = new StringBuffer();										 
	
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
			calBalance = new CalBalance(getDBconnect(), getDBalias());
			// =====================================
			
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");

			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);

			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
			
			// convert YYYYMMDD into YYMMDD
			//String fileNameSearchDate = searchDate.substring(2);

			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, searchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			
			// 產生主要檔案 .DAT 
			int dataCount = generateDatFile(fileFolder, datFileName ,searchDate);
		    //複製一份
			String datFileName2 = DATA_FORM2.replace("YYYYMMDD", searchDate);
			if(copyFile(fileFolder,datFileName2)==-1) {
				comc.errExit(String.format("檔案產生失敗 [%s]!", fileFolder+datFileName2), "");
			}														

			dateTime(); // update the system date and time
			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
			if (isGenerated == false) {
				comc.errExit("產生HDR檔錯誤!", "");
			}
			
			// 先傳*.DAT檔再傳*.HDR檔
			String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			
			// run FTP
			procFTP(fileFolder, datFileName ,hdrFileName,FTP_FOLDER);
			procFTP(fileFolder, datFileName2 ,"",FTP_FOLDER2);

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
	private int generateDatFile(String fileFolder, String datFileName ,String searchDate) throws Exception {

		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		//ICW10 Header顯示欄位的中文名稱
		String headerStr = getHeaderOfICW10();
		copySb.append(headerStr);
		
		selectInfS009Data(searchDate);
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				InfS009Data infS009Data = getInfData();
				String rowOfDAT = getRowOfDAT(infS009Data);
				sb.append(rowOfDAT);
			    copySb.append(rowOfDAT.replaceAll(COL_SEPERATOR, COL_SEPERATOR2));
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
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			}else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}
	
    /***
	 * 複製一分檔案
	 * @throws Exception 
	 */
	int copyFile(String fileFolder ,String datFileName) throws Exception {
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		try {
			byte[] tmpBytes = copySb.toString().getBytes();
			if(writeBinFile(tmpBytes, tmpBytes.length) == true ) {
				showLogMessage("I", "",String.format("複製產生.TXT檔完成！[%s]", datFilePath));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return 1;
	}

	/***							
	 * 取得M0~M5的最低應繳餘額
	 * @param infS009Data
	 * @throws Exception
	 */
	void selectActAcagCurr(InfS009Data infS009Data) throws Exception{
		HashMap<String,Double> map = new HashMap<String,Double>();
		StringBuffer buf = new StringBuffer();
		for(int i = 0 ; i <= 5 ; i++) 
			buf.append("'").append(commDate.monthAdd(infS009Data.thisAcctMonth, i*-1)).append("',");

		String thisAcctMonths = commStr.left(buf.toString(),buf.length()-1);
		sqlCmd = " SELECT ACCT_MONTH,DC_PAY_AMT FROM ACT_ACAG_CURR ";
		sqlCmd += " WHERE ACCT_MONTH IN (    ";
		sqlCmd += thisAcctMonths;
		sqlCmd += " ) AND CURR_CODE = ? AND P_SEQNO = ? ";
		setString(1,infS009Data.currCode);
		setString(2,infS009Data.pSeqno);
		int selectCnt = selectTable();
		for(int ii=0; ii<selectCnt ;ii++)
			map.put(getValue("ACCT_MONTH",ii), getValueDouble("DC_PAY_AMT",ii));
		infS009Data.m0MinPayAmt = getMinPayAmtMap(map.get(commDate.monthAdd(infS009Data.thisAcctMonth, 0)));
		infS009Data.m1MinPayAmt = getMinPayAmtMap(map.get(commDate.monthAdd(infS009Data.thisAcctMonth, -1)));
		infS009Data.m2MinPayAmt = getMinPayAmtMap(map.get(commDate.monthAdd(infS009Data.thisAcctMonth, -2)));
		infS009Data.m3MinPayAmt = getMinPayAmtMap(map.get(commDate.monthAdd(infS009Data.thisAcctMonth, -3)));
		infS009Data.m4MinPayAmt = getMinPayAmtMap(map.get(commDate.monthAdd(infS009Data.thisAcctMonth, -4)));
		infS009Data.m5MinPayAmt = getMinPayAmtMap(map.get(commDate.monthAdd(infS009Data.thisAcctMonth, -5)));
	}
	
	double getMinPayAmtMap(Double amt) {
		return amt==null?0.0:amt.doubleValue();
	}
	
	/***
	 * 取得M6以上的最低應繳餘額
	 * @param infS009Data
	 * @throws Exception
	 */
	void selectActAcagCurrM6(InfS009Data infS009Data) throws Exception {
		sqlCmd = " SELECT SUM(DC_PAY_AMT) AS M6_OVER_MIN_PAY_AMT  ";
		sqlCmd += " FROM ACT_ACAG_CURR WHERE ACCT_MONTH <= ?    ";
		sqlCmd += " AND CURR_CODE = ? AND P_SEQNO = ? ";
		setString(1,commDate.monthAdd(infS009Data.thisAcctMonth, -6));
		setString(2,infS009Data.currCode);
		setString(3,infS009Data.pSeqno);
		int selectCnt = selectTable();
		if(selectCnt > 0)
			infS009Data.m6OverMinPayAmt = getValueDouble("M6_OVER_MIN_PAY_AMT");
	}
	
//	/***
//	 * 查詢各項協商主檔的狀態，取最後一筆異動日期
//	 * @param infS009Data
//	 * @throws Exception 
//	 */
//	void getCpbdueCurrType(InfS009Data infS009Data) throws Exception {
//		sqlCmd = "SELECT * FROM ( ";
//		sqlCmd += " select CPBDUE_UPD_DTE,ID_CORP_NO,CPBDUE_ID_P_SEQNO ";
//		sqlCmd += " ,CASE WHEN cpbdue_type<>'' AND CPBDUE_CURR_TYPE<>'' AND CPBDUE_CURR_TYPE<>'0' ";
//		sqlCmd += " THEN decode(cpbdue_type,'1','1','2','5','3','7','')||decode(CPBDUE_CURR_TYPE,'0','',CPBDUE_CURR_TYPE) ";
//		sqlCmd += " ELSE '' END AS CPBDUE_CURR_TYPE ";
//		sqlCmd += " from col_cpbdue ";
//		sqlCmd += " UNION ";
//		sqlCmd += " SELECT a.APPLY_DATE,b.id_no,a.ID_P_SEQNO,'2'||liac_status AS liac_status ";
//		sqlCmd += " FROM col_liac_nego a,crd_idno b ";
//		sqlCmd += " WHERE a.ID_P_SEQNO=b.ID_P_SEQNO ";
//		sqlCmd += " UNION ";
//		sqlCmd += " SELECT STATUS_DATE,ID_NO,ID_P_SEQNO,LIAD_TYPE||decode(LIAD_STATUS,'A','1','B','2','C','3','D','4','E','5','F','6','G','7','H','8',LIAD_STATUS) AS LIAD_STATUS FROM ( ";
//		sqlCmd += " SELECT ROW_NUMBER() OVER(PARTITION BY A.ID_P_SEQNO ORDER BY A.ID_P_SEQNO,A.STATUS_DATE DESC) AS ROWID, ";
//		sqlCmd += " B.ID_NO,A.ID_P_SEQNO,A.LIAD_TYPE,A.LIAD_STATUS,A.STATUS_DATE ";
//		sqlCmd += " FROM COL_LIAD_RENEWLIQUI A,CRD_IDNO B ";
//		sqlCmd += " WHERE A.ID_P_SEQNO =B.ID_P_SEQNO ";
//		sqlCmd += " ) WHERE ROWID='1' ";
//		sqlCmd += " ) WHERE ID_CORP_NO = ? ";
//		sqlCmd += " ORDER BY DECODE(LEFT(CPBDUE_CURR_TYPE,1),'7',1,2) ,CPBDUE_UPD_DTE DESC ";
//		setString(1,"01".equals(infS009Data.acctType)?infS009Data.idNo:infS009Data.corpNo);
//		int selectCnt = selectTable();
//		if(selectCnt > 0)
//			infS009Data.payByStageFlag = getValue("CPBDUE_CURR_TYPE");
//	}
	
	/***
	 * ICW10 Header
	 * @return
	 * @throws Exception
	 */
	private String getHeaderOfICW10() throws Exception {
		StringBuffer sb = new StringBuffer();
		String[] headerName = {"資料日期","統一編號","公司名稱","持卡人ID","持卡人姓名","信評-新戶","信評-舊戶","行業別","公司名稱","職稱","生日","學歷","年資","年收入","住家電話區碼"//15
				,"住家電話號碼","住家電話分機","公司電話區碼","公司電話號碼","公司電話分機","手機","帳戶類別","帳戶狀態","信用額度","關帳Mcode","當前Mcode","協商狀態","免列報","不可轉逾","不可電催"//30
				,"不可轉催","暫不發簡訊","幣別","自動扣繳行庫","自動扣繳帳號","對帳單應繳總額","對帳單最低應繳金額","當前現欠餘額","當前現欠最低餘額","代表卡號"//40
				,"代表卡號團代","代表卡號受理行","代表卡號受理行名稱","代表卡號核卡行","代表卡號核卡行名稱","繳款編號","繳款編號II","戶籍地郵區","戶籍地址","居住地郵區"//50
				,"居住地址","公司地郵區","公司地址","其他地郵區","其他地址","帳單註記(TCB)","電子帳單註記","電子帳單E-MAIL","電子帳單E-MAIL異動日","雙幣卡換匯帳號"//60
				,"自動扣繳額度","結帳日期","M0最低應繳餘額","M1最低應繳餘額","M2最低應繳餘額","M3最低應繳餘額","M4最低應繳餘額","M5最低應繳餘額","M6以上最低應繳餘額","帳戶流水號"};//70
		
		for(String str : headerName) {
			sb.append(COL_SEPERATOR2);
			sb.append(str);
		}
		sb.append(LINE_SEPERATOR);
		return commStr.right(sb.toString(),sb.length()-1);
	}

	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfS009Data infS009Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		//取得M0~M5的最低應繳餘額
		selectActAcagCurr(infS009Data);
		//取得M6以上的最低應繳餘額
		selectActAcagCurrM6(infS009Data);
		//查詢各項協商主檔的狀態，取最後一筆異動日期
//		getCpbdueCurrType(infS009Data);
	
		sb.append(commCrd.fixLeft(infS009Data.createDate, 8)); //產生資料日期 X(8)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.corpNo, 11));//法人戶統一編號 X(11)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.corpChiName, 100));//公司中文姓名 X(100) 50個中文字
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.idNo, 10));//身分證號碼   X(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.chiName, 100));//中文姓名 X(100) 50個中文字
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.creditLevelNew, 2));//信評等級-新戶(新進件信評等級) X(2)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.creditLevelOld, 16));//信評等級-舊戶(覆審信評等級) X(2)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.businessCode, 4));//行業別(服務代碼) X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.companyName, 48));//公司名稱 X(48) 24個中文字
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.jobPosition, 48));//職稱  X(48) 24個中文字 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1)); //10
		sb.append(commCrd.fixLeft(infS009Data.birthday, 8));//出生日期 X(8)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.education, 1));//學歷等級 X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.serviceYear, 4));//服務年資 X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.valueOf(infS009Data.annualIncome), 10));//年收入 9(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.homeAreaCode1, 4));//住家電話區碼1 X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.homeTelNo1, 10));//住家電話號碼1 X(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.homeTelExt1, 6));//住家分機號碼1 X(6)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.officeAreaCode1, 4));//公司電話區碼1 X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.officeTelNo1, 10));//公司電話號碼1 X(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.officeTelExt1, 4));//公司電話分機1 X(6)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));//20
		sb.append(commCrd.fixLeft(infS009Data.cellarPhone, 15));//手機號碼 X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.acctType, 2));//帳戶帳號類別 X(2)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.acctStatus, 1));//帳戶往來狀態 (戶況) X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.valueOf(infS009Data.lineOfCreditAmt), 10));//帳戶循環信用額度  9(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.paymentRate1, 2));//關帳時MCODE X(2)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS009Data.intRateMcode, 3));//M00,M01~M999真實逾期MCODE X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.payByStageFlag, 2));//協商、分期註記  X(2)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.collectFlagx, 1));//免列報註記(免列報=Y，其他N) X(1) 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.noDelinquentFlag, 1));//帳戶不可轉逾放旗標  X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.noTelCollFlag, 1));//帳戶不可電催旗標 X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));//30
		sb.append(commCrd.fixLeft(infS009Data.noCollectionFlag, 1));//帳戶不可轉催收旗標  X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.noSmsFlag, 1));//暫不發簡訊旗標 X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.currCode, 3));//幣別 X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.autopayAcctBank, 8));//帳戶自動扣繳行庫 X(8)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.autopayAcctNo, 16));//帳戶自動扣繳帳號 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.ttlAmt, "#0.00"), 14));//對帳單應繳總額 9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.stmtOverDueAmt, "#0.00"), 14));//對帳單最低應繳金額 9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.dcTtlAmtBal, "#0.00"), 14));//當前現欠應繳總餘額 9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.dcMinPayBal, "#0.00"), 14));//當前現欠最低應繳餘額 9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.cardNo, 19));//代表卡號(逾期時欠款最大的卡號) X(19)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));//40
		sb.append(commCrd.fixLeft(infS009Data.groupCode, 4));//代表卡號的團體代號 X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.regBankNo, 4));//代表卡號的受理行(業績分行)代碼 X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.regBankName, 30));//代表卡號的受理行中文名稱 X(30)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.riskBankNo, 4));//代表卡號的風險行(核卡分行)代碼 X(4)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.riskBankName, 60));//代表卡號的風險行中文名稱 X(60)30個中文字
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.acnoAutopayAcctNo, 16));// X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.autopayAcctNoIi, 16));//銷帳編號 II <TCB客製> X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.residentZip, 6));//戶籍地-郵遞區號 X(6)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.residentAddr, 200));//戶籍地址 X(200) 100個中文字
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.mailZip, 6));//居住地-郵遞區號 X(6)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));//50
		sb.append(commCrd.fixLeft(infS009Data.mailAddr, 200));//居住地址 X(200) 100個中文字
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.companyZip, 6));//公司地-郵遞區號 X(6)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.companyAddr, 200));//公司地址 X(200)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.billSendingZip, 6));//其他地址-郵遞區號 X(6)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.billSendingAddr, 200));//其他地址 X(200)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.billApplyFlag, 1));//帳單註記 X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.statSendInternet, 1));//電子帳單註記 X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.eMailEbill, 50));//電子帳單E-MAIL X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.eMailEbillDate, 8));//電子帳單異動日期 X(8)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.currChangeAccout, 16));//雙幣卡換匯帳號 X(16)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));//60
		sb.append(commCrd.fixLeft(infS009Data.autopayIndicator, 1));//帳戶自動扣繳指示碼(扣繳額度) X(1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.thisCloseDate, 8));//結帳日期  X(8) (PTR_WORKDAY.THIS_ACCT_MONTH+ACT_ACNO.STMT_CYCLE)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.m0MinPayAmt, "#0.00"), 14));//M0 最低應繳餘額  9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.m1MinPayAmt, "#0.00"), 14));//M1 最低應繳餘額   9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.m2MinPayAmt, "#0.00"), 14));//M2 最低應繳餘額   9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.m3MinPayAmt, "#0.00"), 14));//M3 最低應繳餘額   9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.m4MinPayAmt, "#0.00"), 14));//M4 最低應繳餘額   9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.m5MinPayAmt, "#0.00"), 14));//M5 最低應繳餘額   9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infS009Data.m6OverMinPayAmt, "#0.00"), 14));//M6以上最低應繳餘額   9(14).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS009Data.pSeqno, 10));//歸戶層流水號 X(10)
		sb.append(LINE_SEPERATOR);//70
		return sb.toString();
	}
	
	InfS009Data getInfData() throws Exception {
		InfS009Data infS009Data = new InfS009Data();
		infS009Data.createDate = getValue("CREATE_DATE");
		infS009Data.corpNo = getValue("CORP_NO");
		infS009Data.corpChiName = getValue("CORP_CHI_NAME");
		infS009Data.idNo = getValue("ID_NO");
		infS009Data.chiName = getValue("CHI_NAME");
		infS009Data.creditLevelNew = getValue("CREDIT_LEVEL_NEW");
		infS009Data.creditLevelOld = getValue("CREDIT_LEVEL_OLD");
		infS009Data.businessCode = getValue("BUSINESS_CODE");
		infS009Data.companyName = getValue("COMPANY_NAME");
		infS009Data.jobPosition  = getValue("JOB_POSITION ");//10
		infS009Data.birthday = getValue("BIRTHDAY");
		infS009Data.education = getValue("EDUCATION");
		infS009Data.serviceYear = getValue("SERVICE_YEAR");
		infS009Data.annualIncome = getValueLong("ANNUAL_INCOME");
		infS009Data.homeAreaCode1 = getValue("HOME_AREA_CODE1");
		infS009Data.homeTelNo1 = getValue("HOME_TEL_NO1");
		infS009Data.homeTelExt1 = getValue("HOME_TEL_EXT1");
		infS009Data.officeAreaCode1 = getValue("OFFICE_AREA_CODE1");
		infS009Data.officeTelNo1 = getValue("OFFICE_TEL_NO1");
		infS009Data.officeTelExt1 = getValue("OFFICE_TEL_EXT1");//20
		infS009Data.cellarPhone = getValue("CELLAR_PHONE");
		infS009Data.acctType = getValue("ACCT_TYPE");
		infS009Data.acctStatus = getValue("ACCT_STATUS");
		infS009Data.lineOfCreditAmt = getValueLong("LINE_OF_CREDIT_AMT");
		infS009Data.paymentRate1 = getValue("PAYMENT_RATE1");
		infS009Data.intRateMcode = getValue("INT_RATE_MCODE");
		infS009Data.payByStageFlag = getValue("PAY_BY_STAGE_FLAG");
		infS009Data.collectFlagx = getValue("COLLECT_FLAGX");
		infS009Data.noDelinquentFlag = getValue("NO_DELINQUENT_FLAG");
		infS009Data.noTelCollFlag = getValue("NO_TEL_COLL_FLAG");//30
		infS009Data.noCollectionFlag = getValue("NO_COLLECTION_FLAG");
		infS009Data.noSmsFlag  = getValue("NO_SMS_FLAG ");
		infS009Data.currCode = getValue("CURR_CODE");
		infS009Data.autopayAcctBank = getValue("AUTOPAY_ACCT_BANK");
		infS009Data.autopayAcctNo = getValue("AUTOPAY_ACCT_NO");
		infS009Data.ttlAmt = getValueDouble("TTL_AMT");
		infS009Data.stmtOverDueAmt = getValueDouble("STMT_OVER_DUE_AMT");
		infS009Data.dcTtlAmtBal = getValueDouble("DC_TTL_AMT_BAL");
		infS009Data.dcMinPayBal = getValueDouble("DC_MIN_PAY_BAL");
		infS009Data.cardNo = getValue("CARD_NO");//40
		infS009Data.groupCode = getValue("GROUP_CODE");
		infS009Data.regBankNo = getValue("REG_BANK_NO");
		infS009Data.regBankName = getValue("REG_BANK_NAME");
		infS009Data.riskBankNo = getValue("RISK_BANK_NO");
		infS009Data.riskBankName = getValue("RISK_BANK_NAME");
		infS009Data.acnoAutopayAcctNo = getValue("ACNO_AUTOPAY_ACCT_NO");
		infS009Data.autopayAcctNoIi = getValue("AUTOPAY_ACCT_NO_II");
		infS009Data.residentZip = getValue("RESIDENT_ZIP");
		infS009Data.residentAddr = getValue("RESIDENT_ADDR");
		infS009Data.mailZip = getValue("MAIL_ZIP");
		infS009Data.mailAddr = getValue("MAIL_ADDR");
		infS009Data.companyZip = getValue("COMPANY_ZIP");
		infS009Data.companyAddr = getValue("COMPANY_ADDR");
		infS009Data.billSendingZip = getValue("BILL_SENDING_ZIP");
		infS009Data.billSendingAddr = getValue("BILL_SENDING_ADDR");
		infS009Data.billApplyFlag = getValue("BILL_APPLY_FLAG");
		infS009Data.statSendInternet = getValue("STAT_SEND_INTERNET");
		infS009Data.eMailEbill = getValue("E_MAIL_EBILL");
		infS009Data.eMailEbillDate = getValue("E_MAIL_EBILL_DATE");
		infS009Data.currChangeAccout = getValue("CURR_CHANGE_ACCOUT");
		infS009Data.autopayIndicator = getValue("AUTOPAY_INDICATOR");
		infS009Data.thisCloseDate = getValue("THIS_CLOSE_DATE");
		infS009Data.pSeqno = getValue("P_SEQNO");
		infS009Data.thisAcctMonth = getValue("THIS_ACCT_MONTH"); 
		return infS009Data;
	}

	/***
	 * 以卡人檔為基礎01、	取得帳戶檔有01的資料
	 * @throws Exception
	 */
	private void selectInfS009Data(String searchDate) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A.*,B.PAYMENT_NO AS ACNO_AUTOPAY_ACCT_NO,B.PAYMENT_NO_II AS AUTOPAY_ACCT_NO_II ")
		//,DECODE(A.ACCT_TYPE,'01',B.AUTOPAY_ACCT_NO,'') AS ACNO_AUTOPAY_ACCT_NO ")
		//.append(" ,DECODE(A.ACCT_TYPE,'01','99666'||A.ID_NO,'') AS AUTOPAY_ACCT_NO_II ")
		.append(" ,D.RESIDENT_ZIP ,(D.RESIDENT_ADDR1 || D.RESIDENT_ADDR2 || D.RESIDENT_ADDR3 || D.RESIDENT_ADDR4 || D.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,D.MAIL_ZIP ,(D.MAIL_ADDR1 || D.MAIL_ADDR2 || D.MAIL_ADDR3 || D.MAIL_ADDR4 || D.MAIL_ADDR5) AS MAIL_ADDR ")
		.append(" ,D.COMPANY_ZIP ,(D.COMPANY_ADDR1 || D.COMPANY_ADDR2 || D.COMPANY_ADDR3 || D.COMPANY_ADDR4 || D.COMPANY_ADDR5) AS COMPANY_ADDR ")
		.append(" ,B.BILL_SENDING_ZIP ,(B.BILL_SENDING_ADDR1 || B.BILL_SENDING_ADDR2 || B.BILL_SENDING_ADDR3 || B.BILL_SENDING_ADDR4 || B.BILL_SENDING_ADDR5 ) AS BILL_SENDING_ADDR ")
		.append(" ,B.BILL_APPLY_FLAG ,B.STAT_SEND_INTERNET ,B.E_MAIL_EBILL ,B.E_MAIL_EBILL_DATE ,B.P_SEQNO ")
		.append(" ,E.CURR_CHANGE_ACCOUT ,E.AUTOPAY_INDICATOR ")
		.append(" ,C.THIS_ACCT_MONTH ,C.THIS_ACCT_MONTH || B.STMT_CYCLE AS THIS_CLOSE_DATE ")
		.append(" FROM COL_CS_RPT A,ACT_ACNO B,PTR_WORKDAY C ")
		.append(" LEFT JOIN CRD_IDNO D ON A.ID_P_SEQNO = D.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT_CURR E ON A.ACNO_P_SEQNO = E.P_SEQNO AND A.CURR_CODE = E.CURR_CODE ")
		.append(" where a.ACNO_P_SEQNO = b.P_SEQNO ")
		.append(" AND b.STMT_CYCLE = c.STMT_CYCLE ")
		.append(" AND A.ACCT_STATUS <> '4' ")//排除呆帳
		.append(" AND a.CREATE_DATE = ? ");
		sqlCmd = sb.toString();
		setString(1,searchDate);
		openCursor();
	}
	
void procFTP(String fileFolder, String datFileName, String hdrFileName , String ftpFile) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = ftpFile; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = "";
		if(FTP_FOLDER.equals(ftpFile)) {
			// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
			ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);
		}
		if(FTP_FOLDER2.equals(ftpFile)) {
			ftpCommand = String.format("mput %s", datFileName);
		}

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(ftpFile, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
			if(FTP_FOLDER.equals(ftpFile))
				commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}
//
//	void copyFile(String datFileName1, String fileFolder1 ,String datFileName2, String fileFolder2) throws Exception {
//		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
//		String tmpstr2 = Paths.get(fileFolder2, datFileName2).toString();
//
//		if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
//			showLogMessage("I", "", "ERROR : 檔案[" + datFileName2 + "]copy失敗!");
//			return;
//		}
//		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已copy至 [" + tmpstr2 + "]");
//	}

	public static void main(String[] args) {
		InfS009 proc = new InfS009();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfS009Data{
	String createDate  = "";
	String corpNo = "";
	String corpChiName = "";
	String idNo = "";
	String chiName = "";
	String creditLevelNew = "";
	String creditLevelOld = "";
	String businessCode = "";
	String companyName = "";
	String jobPosition = "";
	String birthday = "";
	String education = "";
	String serviceYear = "";
	long annualIncome = 0;
	String homeAreaCode1 = "";
	String homeTelNo1 = "";
	String homeTelExt1 = "";
	String officeAreaCode1 = "";
	String officeTelNo1 = "";
	String officeTelExt1 = "";
	String cellarPhone = "";
	String acctType = "";
	String acctStatus = "";
	long lineOfCreditAmt = 0;
	String paymentRate1 = "";
	String intRateMcode = "";
	String payByStageFlag = "";
	String collectFlagx = "";
	String noDelinquentFlag = "";
	String noTelCollFlag = "";
	String noCollectionFlag = "";
	String noSmsFlag = "";
	String currCode = "";
	String autopayAcctBank = "";
	String autopayAcctNo = "";
	double ttlAmt = 0;
	double stmtOverDueAmt = 0;
	double dcTtlAmtBal = 0;
	double dcMinPayBal = 0;
	String cardNo = "";
	String groupCode = "";
	String regBankNo = "";
	String regBankName = "";
	String riskBankNo = "";
	String riskBankName = "";
	String acnoAutopayAcctNo = "";
	String autopayAcctNoIi = "";
	String residentZip = "";
	String residentAddr = "";
	String mailZip = "";
	String mailAddr = "";
	String companyZip = "";
	String companyAddr = "";
	String billSendingZip = "";
	String billSendingAddr = "";
	String billApplyFlag = "";
	String statSendInternet = "";
	String eMailEbill = "";
	String eMailEbillDate = "";
	String currChangeAccout = "";
	String autopayIndicator = "";
	String stmtCycle = "";
	String thisCloseDate = "";
	double m0MinPayAmt = 0;
	double m1MinPayAmt = 0;
	double m2MinPayAmt = 0;
	double m3MinPayAmt = 0;
	double m4MinPayAmt = 0;
	double m5MinPayAmt = 0;
	double m6OverMinPayAmt = 0;
	String pSeqno = "";
	String thisAcctMonth = "";
}




