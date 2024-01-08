/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version     AUTHOR              DESCRIPTION                     *
*  --------  ---------  ----------  -----------------------------------------*
*  109/06/05   V1.01.00    Rou      Initial                                  *
*  109/06/15   V1.01.01    Pino     update_crd_idno                          *
*  109/06/17   V1.01.02    Pino     update_cca_card_acct_C                   *
*  109/07/01   V1.01.03    Pino     update_cca_card_acct_C,update_cca_consume*
*  109/07/09   V1.01.04    Zuwei    rename to IcuD015                        *
*  109/07/10   V1.01.05    shiyuqi  updated for project coding standard      *
*  109/07/15   V1.01.06    Pino     tmpCardNo最後一碼由0改1                                                              *
*  109/07/15   V1.01.07    Pino     insert act_acno class_code預設F           *
*  109/07/16   V1.01.08    Pino     cardIdPSeqno                             *
*  109/07/16   V1.01.09    Pino     insertCcaConsume                         *
*  109/07/20   V1.01.10    Pino     voice_open_code、voice_open_code2         *
*  109/07/22   V1.01.11    Pino     selectIdPSeqno                           *
*  109/07/22   V1.01.12    Pino     updateActAcnoLineOfCreditAmt             *
*  109/07/23   V1.01.13    Pino     insertActAcno() corp_p_seqno             *
*  109/07/24   V1.01.14    Pino     setValueIsPass corpCorpNo                *
*  109/07/27   V1.01.15    Pino     setValueIsPass cardIdPSeqno.isEmpty()    *
*  109/09/04   V1.00.06    Zuwei     code scan issue    
*  109/09/08   V1.01.17    Wilson   insert crd_idno_seqno欄位調整                                        *  
*  109/09/08   V1.01.18    Alex     ICACTQNA 不須使用拿掉驗證邏輯                                              *
*  109/09/09   V1.00.19    Alex     remove rest auth_txlog_amt , auth_txlog_amt_cash *  
*  109/09/10   V1.00.20    Alex     修正電話號碼擷取方式                                                                     *
*  109/09/10   V1.00.21    Alex     update cca_consume                       *
*  109/09/21   V1.00.22    Alex     update act_acno.int_rate_mcode			 *
*  109/09/21   V1.00.23    Wilson   insert crd_idno_seqno 新增debit_idno_flag *
*  109/09/25   V1.00.24    Alex     class_code 預設改為 C                       *
*  109/10/14   V1.00.25	   Alex     檔案日期和營業日不同跳過不處理                                                  *
*  109/10/16   V1.00.26    Alex     update cca_consume column                *
*  109-10-19   V1.00.27    shiyuqi       updated for project coding standard  *
*  109-10-28   V1.00.28    Alex     bug fix                                  *
*  109-11-11   V1.00.29    tanwei    updated for project coding standard      *
*  109-11-16   V1.00.30    Alex     統計公司戶cca_card_acct , cca_consume       *
*  109-12-17   V1.00.31    Alex     insert act_acno new_acct_flag ='Y'		 *
*  110-01-25   V1.00.32    Alex     公司戶 cca_consume 判斷 jrnl_bal             *
*  110-03-12   V1.00.33    Justin   民國年若不滿3位，則左邊補0             *
*  110-04-08   V1.00.34    Alex     一卡一戶金額不再累積 , 新增帳戶主檔部分移至讀檔案時	 *
*  110-04-09   V1.00.35    Alex     補上N0、N4 log						     *
*  110-04-09   V1.00.36    Alex     無論A C 都去update crd_card cca_card_base  *    
*  110-11-25   V1.00.37    Justin   ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX               *
*  110-12-02   V1.00.38    Alex     add 106 599                              *
*  110-12-02   V1.00.39    Alex     if birthday empty do not update voice_open_code *
*  110-12-07   V1.00.40    Alex     update 106 599 公司 act_acno 歸戶額度                     * 
*  110-12-08   V1.00.41    Alex     update consume corp id_no > corp_no      *
*  110-12-08   V1.00.42    Alex     update act_acno for corp add id_p_seqno  *
*  110-12-10   V1.00.43    Alex     update fsc_icud15 acno_p_seqno			 *
*  110-12-10   V1.00.44    Alex     update 106 599 acno_flag 2 line_of_credit_amt_cash , stmt_cycle *
*  110-12-10   V1.00.45    Alex     106 599 no update crd_idno               *
*  111-01-18   V1.00.46    Alex     手機號碼更新邏輯異動                                                                     *
*  111-02-14   V1.00.47    Alex     檔名排序處理                                                                                    *
*  111-02-15   V1.00.48    Alex     欄位長度修正                                                                                    *
*  111-02-15   V1.00.49    Ryan      big5 to MS950                                           *
*  111-02-18   V1.00.50    Alex     電話切欄位規則改變                                                                         *
*  111-02-19   V1.00.51    Alex     for > openCursor                         *
*  111-02-20   V1.00.52    Alex     add error report N9 , and id_no not found not abend *
*  111-02-20   V1.00.53    Justin   修改新增crd_idno
*  111-02-23   V1.00.54    Alex     acno_flag='2' 不 update id_p_seqno        *
*  111-03-10   V1.00.55    Alex     setValueIsPass 位置變動                                                    *
*  111-03-14   V1.00.56    Alex     setValueIsPass 位置還原                                                    *
*  111-06-17   V1.00.57    Alex     預借現金邏輯調整								 *
*  111-11-08   V1.00.58    Alex     106599 錯誤原因欄位檢核調整                                                  *
*****************************************************************************/
package Icu;

import com.*;
import com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException;

import Dxc.Util.SecurityUtil;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class IcuD015 extends AccessDAO {
	private final String progname = "每天接收商務卡帳戶異動資料通知檔作業 111-11-08   V1.00.58";

	CommFunction comm = new CommFunction();
	CommCrd comc = null;
	CommRoutine comr = null;
	CommCrdRoutine comcr = null;
	CommDate  commDate = new CommDate();
    CommString commString = new CommString();
    CommFTP commFTP = null;	
	String hCallErrorDesc = "";
	String hCallBatchSeqno = "";
	String hCallRProgramCode = "";
	String getBusinessDate = "";
	String errorFileName = "";
	int totalFile = 0;

	String getFileName = "";
	String getFileDate = "";
	String getFileNo = "";
	String fileName1 = "", fileName2 = "" , errorFileName2 = "" , fileContent = "";	
	String errTable = "";	
	int fe ;
	
	protected final String dt1Str = "mod_acdr, card_unit, card_type, card_seqno, status_cfq, line_of_credit_amt, unpay_amt,"
			+ "auth_not_deposit, acct_jrnl_bal, jrnl_bal_sign, unpay_amt_cash, open_date, status_cfq_date, locamt_cash_rate,"
			+ "locamt_cash_day, id_no_icud15, eng_name, sex, birthday, chi_name, class_icud15, home_tel_no, office_tel_no,"
			+ "corp_no_icud15, cellar_phone, errcode";

	protected final int[] dt1Length = { 1, 8, 2, 7, 1, 9, 11, 11, 11, 1, 11, 8, 8, 2, 2, 11, 30, 1, 8, 80, 2, 12, 12,
			11, 20, 4 };

	protected String[] dt1 = new String[] {};
	int tmpserialNo = 0;
	String tmpfileNo = "";
	String tmpCardType = "";
	String tmpCardNo = "";
	String tmpTcbBin = "";
	String tmpCardSeqno = "";
	String tmpCorpNoIcud15 = "";
	String tmpModAcdr = "";
	String tmpStatusCfq = "";
	String tmpLineOfCreditAmt = "";
	double tmpUnpayAmt = 0.0;
	double tmpAuthNotDeposit = 0.0;
	double tmpAcctJrnlBal = 0.0;
	String tmpJrnlBalSign = "";
	double tmpUnpayAmtCash = 0.0;
	String tmpOpenDate = "";
	String tmpStatusCfqDate = "";
	String tmpLocamtCashRate = "";
	String tmpLocamtCashDay = "";
	String tmpIdNoIcud15 = "";
	String tmpEngName = "";
	String tmpSex = "";
	String tmpBirthday = "";
	String tmpChiName = "";
	String tmpClassIcud15 = "";
	String tmpHomeTelNo = "";
	String tmpPfficeTelNo = "";
	String tmpCellarPhone = "";
	String tmpIsPass = "";
	String tmpNoPassReason = "";
	String tmpFileNo = "";
	String tmpSerialNo = "";
	String cardCardNo = "";
	String cardAcctType = "";
	String cardIdPSeqno = "";
	String cardAcnoPSeqno = "";
	String cardPSeqno = "";
	String idnoIdNo = "";
	String corpCorpPSeqno = "";
	String corpCorpNo = "";	
	int tmpIntRateMcode = 0;
	int fi = 0;
//	int totalNA = 0;
	int totalND = 0;

	String fscIcud15AcctType = "";
	String fscIcud15IdNo = "";
	String fscIcud15IdPSeqno = "";
	String fscIcud15PSeqno = "";
	String fscIcud15AcnoPSeqno = "";
	String fscIcud15CardNo = "";
	String fscIcud15Rowid = "";
	String fscIcud15StatusCfq = "";
	String fscIcud15EngName = "";
	String fscIcud15Sex = "";
	String fscIcud15Birthday = "";
	String fscIcud15ChiName = "";
	String fscIcud15HomeTelNO = "";
	String fscIcud15OfficeTelNo = "";
	String fscIcud15CellarPhone = "";
	String fscIcud15LocamtCashDay = "";
	String fscIcud15CorpPSeqno = "";
	String fscIcud15CorpNo = "";
	double fscIcud15UnpayAmt = 0;
	double fscIcud15AcctJrnlBal = 0;
	String fscIcud15AcctJrnlBalSign = "";
	double fscIcud15AuthNotDeposit = 0;
	double fscIcud15UnpayAmtCash = 0;
	double fscIcud15LineOfCreditAmt = 0;
	double fscIcud15LocamtCashRate = 0;
	String fscIcud15ModAcdr = "";
	String fscIcud15NopassReason = "";
	String strPSeqno = "";
	String strCardAcctIdx = "";
	String acnoAcnoPSeqno = "";
	String acnoPSeqno = "";
	double totalunpayAmt = 0;
	double totalacctJrnlBal = 0;
	double totalauthNotDeposit = 0;
	double totalunpayAmtCash = 0;
	
	String calAcctType = "";
	String calCorpPSeqno = "";
	double calUnpayAmt = 0;
	double calTotAmtConsume = 0;
	double calJrnlBal = 0 ;
	double calTotalCashUtilized = 0 ;
	double calPayAmt = 0;
	double calCardAcctIdx = 0;
	double consumeTotAmt = 0;
	double consumeTxLogAmt2 = 0;
	double consumeTxLogAmtCash2 = 0;
	double consumeUnpaidPrecash = 0;
	double consumeTotUnpaidAmt = 0;
	double consumeUnpaidConsumeFee = 0;
	double consumePaidConsumeFee = 0;
	double consumePrePayAmt = 0;
	boolean corpContinue = false;
	boolean newCardFlag = false ;
	
	DecimalFormat doubleFmt = new DecimalFormat("#.##");

	// ************************************************************************

	public static void main(String[] args) throws Exception {
		IcuD015 proc = new IcuD015();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dt1 = dt1Str.split(",");

			comc = new CommCrd();
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			
			if(args.length==1) {
				getBusinessDate = args[0];
			}	else if(args.length==2) {
				getBusinessDate = args[0];
				hCallBatchSeqno = args[1];
			}	else if(args.length>2) {
				comc.errExit("參數錯誤 , 1:日期 2:CallBatchSeqno", "");
			}
			comr = new CommRoutine(getDBconnect(), getDBalias());
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());			
			if(hCallBatchSeqno.isEmpty()==false)	comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);								
			if(getBusinessDate.isEmpty())	selectPtrBusinday();
			
			/*** 處理 106 599 從 IcuD003 新增的 資料 ***/
			selectFscIcudReason99();
			
//			selectFscIcud15A();
			selectFscIcud15B();
			selectDscIcud15C();
			commitDataBase();
			errorFileName2 = "M00600000.ICCACQND.ERROR."+getBusinessDate;
			/*** 取得檔案 ***/
			openFile();					
			
//			selectFscIcud15A();
			selectFscIcud15B();
			selectDscIcud15C();						
			commitDataBase();
			/*** 公司戶統計   ***/
			calCorpAmount();
			/*** 106 599 公司戶額度處理***/
			procCorpAmt();
			/*** 產出錯誤檔案 ***/
			procErrorReport();
			procFTP();
			backupFile();
			showLogMessage("I", "", "程式執行結束,筆數 = [ " + totalFile + " ]");
			if (comcr.hCallBatchSeqno.length() == 20)
				comcr.callbatch(1, 0, 1); // 1: 結束

			finalProcess();
			return 0;
		}

		catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}

	} // End of mainProcess
	
	/***********************************************************************/
	void backupFile() throws Exception {
		String tmpstr1 = String.format("%s/media/icu/error/%s", comc.getECSHOME(), errorFileName);
		String tmpstr2 = String.format("%s/media/icu/backup/%s", comc.getECSHOME(), errorFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + errorFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + errorFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
	/***********************************************************************/
	void selectPtrBusinday() throws Exception {

		selectSQL = "business_date ";
		daoTable = "ptr_businday";

		selectTable();

		if (notFound.equals("Y")) {
			String err1 = "select_ptr_businday error!";
			String err2 = "";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		} else
			getBusinessDate = getValue("business_date");

		showLogMessage("I", "", "本日營業日 : [" + getBusinessDate + "]");
	}

	// 檢查是否有檔案需要處理
	/***********************************************************************/
	void openFile() throws Exception {
		String filePath = String.format("%s/media/icu", comc.getECSHOME());
		filePath = SecurityUtil.verifyPath(filePath);
		File checkFilePath = new File(filePath);
		if (!checkFilePath.isDirectory())
			comcr.errRtn(String.format("[%s]目錄不存在", filePath), "", hCallBatchSeqno);
		
		//--產出剔退資料
		openOutErrorFile();
		
//		List<String> listOfFiles = comc.listFS(filePath, "", "");
		List<String> listOfFiles = comc.listFsSort(filePath);
		showLogMessage("I", "", "Process file Path =" + filePath);
		for (String file : listOfFiles) {
			getFileName = file;
			if (file.length() != 27)
				continue;

			if (file.substring(0, 19).equals("M00600000.ICCACQND."))
				readFileData(getFileName);
			else
				continue;
		}
		if (totalFile < 1) {
			showLogMessage("D", "", "無檔案可處理  " + "");
		}
		closeOutputText(fe);
	}
	
	void openOutErrorFile() throws Exception {
		
		fe = openOutputText(comc.getECSHOME() + "/media/icu/error/" + errorFileName2, "MS950");
		if(fe<0) {
			comcr.errRtn("產生 ICCACQND 檔案失敗", "", "");
			return;
		}
	}
	
	/**********************************************************************/
	public int readFileData(String fileName) throws Exception {
		String rec = "";
		totalND = 0;

		showLogMessage("I", "", "File = [" + fileName + "]");
		fileName1 = comc.getECSHOME() + "/media/icu/" + fileName;
		showLogMessage("I", "", "Process file =" + fileName);

		int f = openInputText(fileName1);
		if (f == -1) {
			return 1;
		}
		closeInputText(f);

		fi = openInputText(fileName1, "MS950");
		if (fi == -1) {
			return 1;
		}

		getFileDate = String.valueOf(2011 + Integer.parseInt(getFileName.substring(19, 21)))
				+ getFileName.substring(21, 25); // 檔名日期
		getFileNo = getFileName.substring(25, 27); // 檔名序號
		tmpfileNo = getFileDate + getFileNo;
		if (getFileDate.equals(getBusinessDate) ||  commDate.dateAdd(getFileDate, 0, 0, 1).equals(getBusinessDate)) {
		} else {
			getFileDate = "";
			getFileNo = "";
			showLogMessage("D", "", "File Date = [" + getFileDate + "] 日期不符合 BusinessDate = [" + getBusinessDate + "]");
			return 1;
			
//			comcr.errRtn("File Date = [" + getFileDate + "] 日期不符合 BusinessDate = [" + getBusinessDate + "]", "", "");
		}

		if(checkFile()==false) {
			showLogMessage("D", "", "此帳戶異動資料通知檔已執行更新作業 ["+getFileName+"]");
			return 1;
		}

//		if (getFileName.substring(0, 19).equals("M00600000.ICCACQNA.")) {
//			rec = readTextFile(fi); // read file data
//			if (rec.length() != 26) {
//				comcr.errRtn("Error : 此檔案 =  " + fileName + " , 資料長度錯誤", "內容:" + rec + "長度:" + rec.length(), "");
//			} else {
//				if (rec.substring(0, 8).equals("00600000")) {
//				} else {
//					comcr.errRtn("Error : 此檔案 =  " + fileName + " , 資料產生單位錯誤", "", "");
//				}
//			}
//			totalNA = Integer.parseInt(rec.substring(16, 22));
//			showLogMessage("D", "", "ICACTQNA筆數  " + totalNA + "");
//			closeInputText(fi);
//		} else {
		while (true) {
			rec = readTextFile(fi); // read file data
			fileContent = rec;
			if (getFileName.substring(0, 19).equals("M00600000.ICCACQND.")) {
				if (endFile[fi].equals("Y")) {
					closeInputText(fi);
//						if (totalNA != totalND)
//							comcr.errRtn("Error : 此檔案 =  " + fileName + " , 資料筆數不相等", totalNA + "", totalND + "");
					break;
				}
				totalFile++;
				if (new String(rec).length() > 284 || new String(rec).length() < 244)
					writeErrorFile(fileContent);
//					comcr.errRtn("Error : 此檔案 =  " + fileName + " , 資料長度錯誤", "", "");
				else {
					moveData(processDataRecord(getFieldValue(rec, dt1Length), dt1));
					totalND++;
				}
			}
		}
//		}
		commitDataBase();
		renameFile(fileName);		
		return 0;
	}
	
	void writeErrorFile(String desc) throws Exception {
		writeTextFile(fe, desc+"\r\n");
	}
	
	/**********************************************************************/
	void moveData(Map<String, Object> map) throws Exception {
		dateTime();
		String tmpStr;

		tmpModAcdr = (String) map.get("mod_acdr");

		tmpStr = (String) map.get("card_unit");
		if (!tmpStr.equals("00600000"))
			comcr.errRtn("Error : 此檔案 =  " + getFileName + " , 資料長度錯誤", "", "");
		tmpNoPassReason = "";
		tmpCardType = (String) map.get("card_type");
		tmpCardSeqno = (String) map.get("card_seqno");
		showLogMessage("I", "", "card_seqno =" + tmpCardSeqno);
		tmpCardNo = selectFscBinGroup() + tmpCardSeqno + "1"; // V1.01.06
		showLogMessage("I", "", "card_no =" + tmpCardNo);
		tmpStatusCfq = (String) map.get("status_cfq");
		tmpLineOfCreditAmt = (String) map.get("line_of_credit_amt");
		tmpAuthNotDeposit = Double.parseDouble((String) map.get("auth_not_deposit"))/100;
		tmpAcctJrnlBal = Double.parseDouble((String) map.get("acct_jrnl_bal"))/100;
		tmpJrnlBalSign = (String) map.get("jrnl_bal_sign");
		tmpUnpayAmt = Double.parseDouble((String) map.get("unpay_amt"))/100;
		tmpUnpayAmtCash = Double.parseDouble((String) map.get("unpay_amt_cash"))/100;
		tmpOpenDate = (String) map.get("open_date");
		tmpStatusCfqDate = (String) map.get("status_cfq_date");
		tmpLocamtCashRate = (String) map.get("locamt_cash_rate");
		tmpLocamtCashDay = (String) map.get("locamt_cash_day");
		tmpIdNoIcud15 = (String) map.get("id_no_icud15");
		tmpEngName = (String) map.get("eng_name");
		tmpEngName = tmpEngName.trim();
		showLogMessage("I", "", "eng_name =" + tmpEngName);
		tmpSex = (String) map.get("sex");
		if(tmpSex.equals("M"))	tmpSex = "1";
		else if(tmpSex.equals("F"))	tmpSex = "2";
		tmpBirthday = (String) map.get("birthday");
		tmpChiName = (String) map.get("chi_name");
		showLogMessage("I", "", "chi_name =" + tmpChiName);
		tmpChiName = tmpChiName.trim();
		tmpClassIcud15 = (String) map.get("class_icud15");
		tmpHomeTelNo = (String) map.get("home_tel_no");
		tmpPfficeTelNo = (String) map.get("office_tel_no");
		tmpCorpNoIcud15 = (String) map.get("corp_no_icud15");
		tmpCellarPhone = (String) map.get("cellar_phone");
		newCardFlag = false;
		errTable = "";
		
		selectCrdCard();

//		selectCrdCorp();

//		tmpCellarPhone = (String) map.get("cellar_phone");

		insertFscIcud15();

//		commitDataBase();

		return;
	}
	
	void initTemp() {
		tmpModAcdr = "";				
		tmpCardType = "";
		tmpCardSeqno = "";		
		tmpCardNo = "";		
		tmpStatusCfq = "";
		tmpLineOfCreditAmt = "";
		tmpAuthNotDeposit = 0;
		tmpAcctJrnlBal = 0;
		tmpJrnlBalSign = "";
		tmpUnpayAmt = 0;
		tmpUnpayAmtCash = 0;
		tmpOpenDate = "";
		tmpStatusCfqDate = "";
		tmpLocamtCashRate = "";
		tmpLocamtCashDay = "";
		tmpIdNoIcud15 = "";
		tmpEngName = "";
		tmpEngName = "";		
		tmpSex = "";		
		tmpBirthday = "";
		tmpChiName = "";		
		tmpChiName = "";
		tmpClassIcud15 = "";
		tmpHomeTelNo = "";
		tmpPfficeTelNo = "";
		tmpCorpNoIcud15 = "";
		tmpFileNo = "";
		tmpSerialNo = "";
		tmpNoPassReason = "";
		newCardFlag = false;		
	}
	
	/**********************************************************************/
	void selectFscIcudReason99() throws Exception {
		sqlCmd = "select mod_acdr , card_type , card_seqno , card_no , status_cfq , line_of_credit_amt , "
				+ " auth_not_deposit , acct_jrnl_bal , jrnl_bal_sign , unpay_amt , unpay_amt_cash , "
				+ " open_date , status_cfq_date , locamt_cash_rate , locamt_cash_day , id_no , eng_name , "
				+ " sex , birthday , chi_name , class_icud15 , home_tel_no , office_tel_no , corp_no , "
				+ " file_no , serial_no , nopass_reason "
				+ " from fsc_icud15 where is_process ='' and is_pass ='Y' and nopass_reason = '99' "
				;
		
		openCursor();
		
		while(fetchTable()) {
			initTemp();
			tmpFileNo = getValue("file_no");
			tmpSerialNo = getValue("serial_no");
			tmpModAcdr = getValue("mod_acdr");
			tmpCardType = getValue("card_type");
			tmpCardSeqno = getValue("card_seqno");
			tmpCardNo = getValue("card_no");
			tmpStatusCfq = getValue("status_cfq");
			tmpLineOfCreditAmt = getValue("line_of_credit_amt");
			tmpAuthNotDeposit = getValueDouble("auth_not_deposit");
			tmpAcctJrnlBal = getValueDouble("acct_jrnl_bal");
			tmpJrnlBalSign = getValue("jrnl_bal_sign");
			tmpUnpayAmt = getValueDouble("unpay_amt");
			tmpUnpayAmtCash = getValueDouble("unpay_amt_cash");
			tmpOpenDate = getValue("open_date");
			tmpStatusCfqDate = getValue("status_cfq_date");
			tmpLocamtCashRate = getValue("locamt_cash_rate");
			tmpLocamtCashDay = getValue("locamt_cash_day");
			tmpIdNoIcud15 = getValue("id_no");
			tmpEngName = getValue("eng_name");			
			tmpSex = getValue("sex");		
			tmpBirthday = getValue("birthday");
			tmpChiName = getValue("chi_name");					
			tmpClassIcud15 = getValue("class_icud15");
			tmpHomeTelNo = getValue("home_tel_no");
			tmpPfficeTelNo = getValue("office_tel_no");
			tmpCorpNoIcud15 = getValue("corp_no");
			tmpNoPassReason = getValue("nopass_reason");
			newCardFlag = false;	
			errTable = "";
			selectCrdCard();
			//--將 acno_p_seqno update 回 fsc_icud15
			updateFscIcud15ForAcnoPSeqno();
		}
		
		closeCursor();
		
	}
	
	/**********************************************************************/
	void updateFscIcud15ForAcnoPSeqno() throws Exception {
		daoTable = "fsc_icud15";
		updateSQL = "acno_p_seqno = ? , p_seqno = ? ";
		whereStr = "where file_no = ? and serial_no = ? ";
		setString(1,cardAcnoPSeqno);
		setString(2,cardPSeqno);
		setString(3,tmpFileNo);
		setString(4,tmpSerialNo);
		updateTable();
		if (notFound.equals("Y"))
			comcr.errRtn("update_fsc_icud15 not found!", "", "file_no:" + tmpFileNo+" serial_no:"+tmpSerialNo);
		
	}
	
	/**********************************************************************/
	void insertFscIcud15() throws Exception {
		tmpserialNo++; // 流水號

		daoTable = "fsc_icud15";
		extendField = daoTable + ".";
		setValue(extendField + "file_no", tmpfileNo);
		setValueInt(extendField + "serial_no", tmpserialNo);
		setValue(extendField + "mod_acdr", tmpModAcdr.trim());
		setValue(extendField + "card_type", tmpCardType.trim());
		setValue(extendField + "card_seqno", tmpCardSeqno.trim());
		setValue(extendField + "status_cfq", tmpStatusCfq.trim());
		setValue(extendField + "line_of_credit_amt", tmpLineOfCreditAmt.trim());
		setValueDouble(extendField + "unpay_amt", tmpUnpayAmt);
		setValueDouble(extendField + "auth_not_deposit", tmpAuthNotDeposit);
		setValueDouble(extendField + "acct_jrnl_bal", tmpAcctJrnlBal);
		setValue(extendField + "jrnl_bal_sign", tmpJrnlBalSign.trim());
		setValueDouble(extendField + "unpay_amt_cash", tmpUnpayAmtCash);
		setValue(extendField + "open_date", tmpOpenDate.trim());
		setValue(extendField + "status_cfq_date", tmpStatusCfqDate.trim());
		setValue(extendField + "locamt_cash_rate", tmpLocamtCashRate.trim());
		setValue(extendField + "locamt_cash_day", tmpLocamtCashDay.trim());
		setValue(extendField + "id_no_icud15", tmpIdNoIcud15.trim());
		setValue(extendField + "eng_name", String.format("%-25.25s", tmpEngName).trim());
//		setValue(extendField + "eng_name", tmpEngName.trim());
		setValue(extendField + "sex", tmpSex.trim());
		setValue(extendField + "birthday", tmpBirthday.trim());
		setValue(extendField + "chi_name", String.format("%-50.50s", tmpChiName).trim());
//		setValue(extendField + "chi_name", tmpChiName.trim());
		setValue(extendField + "class_icud15", tmpClassIcud15.trim());
		setValue(extendField + "home_tel_no", tmpHomeTelNo.trim());
		setValue(extendField + "office_tel_no", tmpPfficeTelNo.trim());
		setValue(extendField + "corp_no_icud15", tmpCorpNoIcud15.trim());
		setValue(extendField + "cellar_phone", tmpCellarPhone.trim());
		setValue(extendField + "card_no", cardCardNo.trim());
		setValue(extendField + "acct_type", cardAcctType.trim());
		setValue(extendField + "id_p_seqno", cardIdPSeqno.trim());
		setValue(extendField + "acno_p_seqno", cardAcnoPSeqno.trim());
		setValue(extendField + "p_seqno", cardPSeqno.trim());
		setValue(extendField + "id_no", idnoIdNo.trim());
		setValue(extendField + "corp_p_seqno", corpCorpPSeqno.trim());
		setValue(extendField + "corp_no", corpCorpNo.trim());
		if(newCardFlag == true)
			setValue(extendField + "is_add_card", "Y");
		else
			setValue(extendField + "is_add_card", "N");
		setValueIsPass();
		setValue(extendField + "is_pass", tmpIsPass.trim());
		setValue(extendField + "nopass_reason",tmpNoPassReason);
		setValue(extendField + "crt_date", sysDate);
		setValue(extendField + "crt_time", sysTime);
		setValue(extendField + "crt_user", "esc");
		setValue(extendField + "mod_user", "IcuD015");
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", javaProgram);
		insertTable();

		if (dupRecord.equals("Y"))
			comcr.errRtn("insert_fsc_icud15 error[dupRecord]", "", comcr.hCallBatchSeqno);

		return;
	}

	/**********************************************************************/
	String setValueIsPass() throws Exception {
		String idNoIcud15 = "";
		
		if("N2".equals(tmpNoPassReason) == false && "N9".equals(tmpNoPassReason) == false) {
			if("A".equals(tmpModAcdr) == false && "C".equals(tmpModAcdr) == false && "D".equals(tmpModAcdr) == false) {
				showLogMessage("I", "", "is_pass為N的原因:mod_acdr[" + tmpModAcdr + "]");
				tmpNoPassReason = "N1";				
			}
			
			if("D".equals(tmpModAcdr)) {
				if("C".equals(tmpStatusCfq) == false && "F".equals(tmpStatusCfq) == false && "Q".equals(tmpStatusCfq) == false) {
					showLogMessage("I", "", "is_pass為N的原因:mod_acdr為D且status_cfq不等於C或F或Q ");
					tmpNoPassReason = "N3";					
				}
			}
			
			idNoIcud15 = commString.mid(tmpIdNoIcud15, 10,1);
			if(" ".equals(idNoIcud15) == false && "R".equals(idNoIcud15) == false) {
				showLogMessage("I", "", "is_pass為N的原因:id_no_icud15 第11碼不為空白或R");
				tmpNoPassReason = "N6";
			}
			
			idNoIcud15 = commString.mid(tmpIdNoIcud15, 0,10);
			if(idnoIdNo.equals(idNoIcud15) == false) {
				showLogMessage("I", "", "is_pass為N的原因:id_no_icud15 和卡人檔中ID_NO不符");
				tmpNoPassReason = "N8";
			}
			
			if (!corpCorpNo.trim().equals(tmpCorpNoIcud15.trim())) {			
				showLogMessage("I", "", "is_pass為N的原因:corpCorpNo != CorpNoIcud15 corpCorpNo:[" + corpCorpNo
						+ "]CorpNoIcud15:[" + tmpCorpNoIcud15 + "]");
				tmpNoPassReason = "N7";				
			}
			
			if("N0".equals(tmpNoPassReason)) {
				showLogMessage("I", "", "is_pass為N的原因:Data No Match");
			}
			
			if("N4".equals(tmpNoPassReason)) {
				showLogMessage("I", "", "is_pass為N的原因:insert error , table:[" + errTable + "]");
			}
			
		}	else if("N2".equals(tmpNoPassReason))	{
			showLogMessage("I", "", "is_pass為N的原因:cardCardNo or cardAcctType is Empty ");			
		}	else if("N9".equals(tmpNoPassReason))	{
			showLogMessage("I", "", "is_pass為N的原因: 卡檔有 id_p_seqno 但crd_idno找不到 id_p_seqno =["+cardIdPSeqno+"] , card_no = ["+cardCardNo+"]");			
		}
		
		if(tmpNoPassReason.isEmpty() == false)
			return tmpIsPass = "N";
		
		return tmpIsPass = "Y";
		
//		if (tmpModAcdr.equals("A") || tmpModAcdr.equals("C") || tmpModAcdr.equals("D")) {
//
//		} else {
//			showLogMessage("I", "", "is_pass為N的原因:mod_acdr[" + tmpModAcdr + "]");
//			tmpNoPassReason = "N1";
//			return tmpIsPass = "N";
//		}
//
//		idNoIcud15 = commString.mid(tmpIdNoIcud15, 10,1);
//		//--tmpIdNoIcud15.substring(10, 11);
//		if (idNoIcud15.equals(" ") || idNoIcud15.equals("R")) {
//
//		} else {
//			showLogMessage("I", "", "is_pass為N的原因:id_no_icud15 第11碼不為空白或R");
//			tmpNoPassReason = "N6";
//			return tmpIsPass = "N";
//		}
//
//		if (cardCardNo.isEmpty() || cardAcctType.isEmpty()) { // 刪除cardIdPSeqno.isEmpty()
//			showLogMessage("I", "", "is_pass為N的原因:cardCardNo or cardAcctType is Empty ");
//			tmpNoPassReason = "N2";
//			return tmpIsPass = "N";
//		}
//
//		if (!corpCorpNo.trim().equals(tmpCorpNoIcud15.trim())) {
//			showLogMessage("I", "", "is_pass為N的原因:corpCorpNo != CorpNoIcud15 corpCorpNo:[" + corpCorpNo
//					+ "]CorpNoIcud15:[" + tmpCorpNoIcud15 + "]");
//			tmpNoPassReason = "N7";
//			return tmpIsPass = "N";
//		}
//
//		if (tmpModAcdr.equals("D")) {
//			if (tmpStatusCfq.equals("C") || tmpStatusCfq.equals("F") || tmpStatusCfq.equals("Q")) {
//
//			} else {
//				showLogMessage("I", "", "is_pass為N的原因:mod_acdr為D且status_cfq不等於C或F或Q ");
//				tmpNoPassReason = "N3";
//				return tmpIsPass = "N";
//			}
//		}
//
//		if (tmpModAcdr.equals("A")) {
//			if (!cardAcnoPSeqno.isEmpty())
//				setValue(extendField + "is_add_card", "Y");
//		}
//		
//		if (tmpTcbBin.isEmpty()) {
//			showLogMessage("I", "", "is_pass為N的原因:(TCB_BIN)不存在 BIN="+tmpCardType);
//			tmpNoPassReason = "N2"; 
//			return tmpIsPass = "N";
//		}
				
	}

	/**********************************************************************/
	String selectFscBinGroup() throws Exception {

		sqlCmd = "select tcb_bin ";
		sqlCmd += "from fsc_bin_group ";
		sqlCmd += "where fisc_code = ? ";
		sqlCmd += "fetch first 1 rows only ";
		setString(1, tmpCardType);
		int recordCnt = selectTable();
		if (recordCnt > 0)
			tmpTcbBin = getValue("tcb_bin");		
		else {
			showLogMessage("I", "", " (TCB_BIN)不存在 BIN="+tmpCardType + "");
			return "";
		}			

		return tmpTcbBin;
	}

	/**********************************************************************/
//	void selectCrdCorp() throws Exception {
//
//		sqlCmd = "select corp_p_seqno, corp_no ";
//		sqlCmd += "from crd_corp ";
//		sqlCmd += "where corp_no = ? ";
//		setString(1, tmpCorpNoIcud15);
//
//		int recordCnt = selectTable();
//		if (recordCnt > 0) {
//			corpCorpPSeqno = getValue("corp_p_seqno");
//			corpCorpNo = getValue("corp_no");
//		} else {
//			corpCorpPSeqno = "";
//			corpCorpNo = "";
//		}
//
//		return;
//	}

	/**********************************************************************/
	void selectCrdCard() throws Exception {

		sqlCmd = "select card_no, acct_type, id_p_seqno, corp_p_seqno, ";
		sqlCmd += "corp_no, acno_p_seqno, p_seqno, corp_p_seqno, corp_no ";
		sqlCmd += "from crd_card ";
		sqlCmd += "where card_no like ? ";
		sqlCmd += "fetch first 1 rows only ";
		setString(1, tmpCardNo+"%");

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			cardCardNo = getValue("card_no");
			cardAcctType = getValue("acct_type");
			cardIdPSeqno = getValue("id_p_seqno");
			cardAcnoPSeqno = getValue("acno_p_seqno");
			cardPSeqno = getValue("p_seqno");
			corpCorpPSeqno = getValue("corp_p_seqno");
			corpCorpNo = getValue("corp_no");
		} else {
			cardCardNo = "";
			cardAcctType = "";
			cardIdPSeqno = "";
			cardAcnoPSeqno = "";
			cardPSeqno = "";
			corpCorpPSeqno = "";
			corpCorpNo = "";
		}
				
		if(recordCnt <=0 || cardAcctType.isEmpty()) {			
			tmpNoPassReason = "N2";			
			return ;
		}
				
		
		if (cardIdPSeqno.length() == 0) {
			selectIdPSeqno(); // V1.01.11
		} else {
			selectCrdIdno();
//--    因106599的 noPassReason 為 99 若在此被擋掉會無法取帳戶流水號 , 錯誤檢核以下方排除 noPassReason 99 處檢核			
//			if(tmpNoPassReason.isEmpty() == false)
//				return;
		}
		
		if(tmpNoPassReason.equals("99") == false) {
			setValueIsPass();
			if(tmpNoPassReason.isEmpty() == false)
				return;
		}
		
		if (cardAcnoPSeqno.isEmpty() == false) {
			//--讀取 act_acno , cca_card_acct , cca_consume
			if(checkActAcno() == false) {
				tmpNoPassReason = "N0";				
				return ;
			}
			
			if(checkCcaCardAcct() == false) {
				tmpNoPassReason = "N0";				
				return ;
			}
			
			if(checkCcaConsume() == false) {
				tmpNoPassReason = "N0";				
				return ;
			}						
		} else {
			//--取號
			sqlCmd = "select to_char(ECS_ACNO.nextval,'0000000000') tmp_p_seqno ";
			sqlCmd += "from dual ";
			if (selectTable() > 0) {
				strPSeqno = getValue("tmp_p_seqno");
				cardAcnoPSeqno = strPSeqno;
				cardPSeqno = strPSeqno;
			}
			
			// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX
			strCardAcctIdx = Integer.toString(Integer.parseInt(strPSeqno));	
//			sqlCmd = "select ecs_card_acct_idx.nextval tmp_card_acct_idx ";
//			sqlCmd += "from dual ";
//			if (selectTable() > 0)
//				strCardAcctIdx = getValue("tmp_card_acct_idx");
			
			//--新增 act_acno、cca_card_acct、cca_consume
			insertActAcno();
			insertCcaCardAcct();
			insertCcaConsume();
			newCardFlag = true;
			//--將 card_acct_idx update 回 cca_card_base
			updateCcaCardBase();		
		}
		
		return;
	}	
	
	boolean checkCcaConsume() throws Exception {
		sqlCmd = "select card_acct_idx from cca_consume where card_acct_idx = ? ";
		setString(1,strCardAcctIdx);
		selectTable();
		if("Y".equals(notFound)) {
			writeErrorFile(fileContent);
			return false;
		}
							
		return true;
	}
	
	boolean checkCcaCardAcct() throws Exception {
		sqlCmd = "select card_acct_idx from cca_card_acct where acno_p_seqno = ? ";
		setString(1,cardAcnoPSeqno);
		selectTable();
		if("Y".equals(notFound)) {
			writeErrorFile(fileContent);
			return false;
		}
					
		strCardAcctIdx = getValue("card_acct_idx");		
		return true;		
	}
	
	boolean checkActAcno() throws Exception {
		sqlCmd = "select acno_p_seqno from act_acno where acno_p_seqno = ? ";
		setString(1,cardAcnoPSeqno);
		selectTable();
		if("Y".equals(notFound)) {
			writeErrorFile(fileContent);
			return false;
		}
			
		return true;		
	}
	
	/**********************************************************************/
	void selectIdPSeqno() throws Exception {
		String tmpIdno = "";
		tmpIdno = commString.mid(tmpIdNoIcud15, 0,10);
		sqlCmd = "select id_p_seqno ";
		sqlCmd += " from crd_idno ";
		sqlCmd += " where id_no = ? ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1,tmpIdno);
//		setString(1, tmpIdNoIcud15.substring(0, 10));
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			sqlCmd = "select id_p_seqno ";
			sqlCmd += " from crd_idno_seqno ";
			sqlCmd += " where id_no = ? ";
			sqlCmd += " fetch first 1 rows only ";
			setString(1,tmpIdno);
//			setString(1, tmpIdNoIcud15.substring(0, 10));
			recordCnt = selectTable();
			if (notFound.equals("Y")) {
				sqlCmd = "select substr(to_char(ecs_acno.nextval,'0000000000'), 2,10) as temp_x10 ";
				sqlCmd += " from dual ";
				recordCnt = selectTable();
				if (notFound.equals("Y")) {
					comcr.errRtn("Error : select_ecs_acno error[notFound]", "", "");
				}	else if (recordCnt > 0) {
					cardIdPSeqno = getValue("temp_x10");
					idnoIdNo = tmpIdno;
					insertCrdIdnoSeqno(cardIdPSeqno);								
				}
			}	else if	(recordCnt > 0) {
				cardIdPSeqno = getValue("id_p_seqno");
				idnoIdNo = tmpIdno;
//				idnoIdNo = tmpIdNoIcud15.substring(0, 10);				
			}
			insertCrdIdno(cardIdPSeqno);
		}	else if (recordCnt > 0) {
			cardIdPSeqno = getValue("id_p_seqno");
			idnoIdNo = tmpIdno;
//			idnoIdNo = tmpIdNoIcud15.substring(0, 10);
		}
	}

	/***********************************************************************/
	void insertCrdIdnoSeqno(String idPSeqno) throws Exception {
		String tmpIdNo = "";
		tmpIdNo = commString.mid(tmpIdNoIcud15, 0,10);
		daoTable = "crd_idno_seqno";
		extendField = daoTable + ".";
		setValue(extendField + "id_no", tmpIdNo);
//		setValue(extendField + "id_no", tmpIdNoIcud15.substring(0, 10));
		setValue(extendField + "id_p_seqno", idPSeqno);
		setValue(extendField + "id_flag", "");
		setValue(extendField + "bill_apply_flag", "");
		setValue(extendField + "debit_idno_flag", "N");
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_crd_idno_seqno error", "", "id_no:" + tmpIdNoIcud15 + "id_p_seqno:" + idPSeqno);
		}

	}

	/***********************************************************************/
	void insertCrdIdno(String idPSeqno) throws Exception {
		String tmpIdNo = "";
		tmpIdNo = commString.mid(tmpIdNoIcud15,0,10);
		String[] officeTel = comc.transTelNo(tmpPfficeTelNo);
		String[] homeTel = comc.transTelNo(tmpHomeTelNo);
		
		daoTable = "crd_idno";
		extendField = daoTable + ".";
		setValue(extendField + "id_no", tmpIdNo);
//		setValue(extendField + "id_no", tmpIdNoIcud15.substring(0, 10));
		setValue(extendField + "id_no_code", "0");
		setValue(extendField + "id_p_seqno", idPSeqno);
		//==========
		// 2022/02/20 move update to this palace
		setValue(extendField + "eng_name", String.format("%-25.25s", tmpEngName).trim());
		setValue(extendField + "sex", tmpSex);
		setValue(extendField + "birthday", tmpBirthday);
		setValue(extendField + "chi_name", String.format("%-50.50s", tmpChiName).trim());
		setValue(extendField + "home_area_code1", homeTel[0]);
		setValue(extendField + "home_tel_no1", homeTel[1]);
		setValue(extendField + "home_tel_ext1", homeTel[2]);
		setValue(extendField + "office_area_code1", officeTel[0]);
		setValue(extendField + "office_tel_no1", officeTel[1]);
		setValue(extendField + "office_tel_ext1", officeTel[2]);	
		setValue(extendField + "CELLAR_PHONE", String.format("%-15.15s", tmpCellarPhone).trim());
		setValue(extendField + "mod_user", "ecs");
		//==========
		setValue(extendField + "msg_flag", "Y");		
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", "IcuD015");
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_crd_idno error", "", "id_no:" + tmpIdNoIcud15 + "id_p_seqno:" + idPSeqno);
		}
	}

	/**********************************************************************/
//  void insertCrdIdno() throws Exception {
//		sqlCmd = "select to_char(ECS_ACNO.nextval,'0000000000') tmp_id_p_seqno ";
//		sqlCmd += "from dual ";
//		if (selectTable() > 0)
//			cardIdPSeqno = getValue("tmp_id_p_seqno");
//
//		daoTable = "crd_idno";
//		extendField = daoTable + ".";
//		setValue(extendField + "id_p_seqno", cardIdPSeqno);
//		setValue(extendField + "id_no", tmpIdNoIcud15);
//		setValue(extendField + "mod_user", "ecs");
//		setValue(extendField + "mod_time", sysDate + sysTime);
//		setValue(extendField + "mod_pgm", "IcuD015");
//		insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.errRtn("insert_crd_idno duplicate!", "", "id_p_seqno:"+cardIdPSeqno);
//        }
//
//    return;
//  }
	/**********************************************************************/
	void selectCrdIdno() throws Exception {
		sqlCmd = "select id_no ";
		sqlCmd += "from crd_idno ";
		sqlCmd += "where id_p_seqno  = ? ";
		sqlCmd += "fetch first 1 rows only ";
		setString(1, cardIdPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
//			comcr.errRtn("select_crd_idno not found!", "", "id_p_seqno:" + cardIdPSeqno);
			tmpNoPassReason = "N9";
			writeErrorFile(fileContent);
			insertCrdIdno(cardIdPSeqno);
		} else {
			idnoIdNo = getValue("id_no");
		}
	}

	/**********************************************************************/
	boolean checkFile() throws Exception {
		int tmpInt = 0;

		sqlCmd = "select count(*) as tmpInt ";
		sqlCmd += "from fsc_icud15 ";
		sqlCmd += "where file_no = ? ";
		sqlCmd += "and is_process = 'Y' ";
		setString(1, tmpfileNo);

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			tmpInt = getValueInt("tmpInt");
			if (tmpInt > 0)
				return false ;
//				comcr.errRtn(">>>此帳戶異動資料通知檔已執行更新作業<<<", "", "");
		}

		return true;
	}

	/**********************************************************************/
//	void selectFscIcud15A() throws Exception {
//
//		sqlCmd = "select rowid as rowid,* ";
//		sqlCmd += "from fsc_icud15 ";
//		// sqlCmd += "where file_no = ? ";
//		sqlCmd += "where is_pass = 'Y' ";
//		sqlCmd += "and is_process <> 'Y' ";
//		sqlCmd += " and uf_nvl(p_seqno,'') = '' ";
////		sqlCmd += "and mod_acdr = 'A' ";
////		sqlCmd += "and is_add_card <> 'Y' ";
//		sqlCmd += "order by file_no, id_no ";
//		// setString(1, tmpfile_no);
//
//		int recordCnt = selectTable();
//
//		for (int i = 0; i < recordCnt; i++) {
//			fscIcud15AcctType = getValue("acct_type", i);
//			fscIcud15IdNo = getValue("id_no", i);
//			fscIcud15IdPSeqno = getValue("id_p_seqno", i);
//			fscIcud15PSeqno = getValue("p_seqno", i);
//			fscIcud15AcnoPSeqno = getValue("acno_p_seqno", i);
//			fscIcud15CardNo = getValue("card_no", i);
//			fscIcud15CorpPSeqno = getValue("corp_p_seqno", i);
//			fscIcud15Rowid = getValue("rowid", i);
//
//			if (selectActAcnoA() == 1) {
//				insertActAcno();
//				insertCcaCardAcct();
//				insertCcaConsume();
//			}
//			// V1.00.03刪除 insert_act_acct();
//			updateCcaCardBase();
//			updateFscIcuD15Pseqo();
//			commitDataBase();
//		}
//	}

	/**********************************************************************/
	int selectActAcnoA() throws Exception {

		sqlCmd = "select acno_p_seqno, p_seqno ";
		sqlCmd += "from act_acno ";
		sqlCmd += "where acct_type = ? ";
		sqlCmd += "and acct_key = ? ";
		sqlCmd += "and corp_p_seqno = ? ";
		setString(1, fscIcud15AcctType);
		setString(2, fscIcud15IdNo + "0");
		setString(3, fscIcud15CorpPSeqno);

		if (selectTable() > 0) {
			strPSeqno = getValue("acno_p_seqno");
			strPSeqno = getValue("p_seqno");
			if (strPSeqno.length() > 0) {
				sqlCmd = "select card_acct_idx ";
				sqlCmd += "from cca_card_acct ";
				sqlCmd += "where acno_p_seqno = ? ";
				setString(1, strPSeqno);
				if (selectTable() > 0) {
					strCardAcctIdx = getValue("card_acct_idx");
				}
			}
		}
		if (notFound.equals("Y")) {

			sqlCmd = "select to_char(ECS_ACNO.nextval,'0000000000') tmp_p_seqno ";
			sqlCmd += "from dual ";
			if (selectTable() > 0)
				strPSeqno = getValue("tmp_p_seqno");
			
			// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX
			strCardAcctIdx = Integer.toString(Integer.parseInt(strPSeqno));	
//			sqlCmd = "select ecs_card_acct_idx.nextval tmp_card_acct_idx ";
//			sqlCmd += "from dual ";
//			if (selectTable() > 0)
//				strCardAcctIdx = getValue("tmp_card_acct_idx");
			
			return 1;

		}
		return 0;

	}

	/**********************************************************************/
	void insertActAcno() throws Exception {
		daoTable = "act_acno";
		extendField = daoTable + ".";
		setValue(extendField + "acno_p_seqno", strPSeqno);
		setValue(extendField + "p_seqno", strPSeqno);
		setValue(extendField + "acct_type", cardAcctType);
		setValue(extendField + "acct_key", strPSeqno + "0");
		setValue(extendField + "id_p_seqno", cardIdPSeqno);
		setValue(extendField + "class_code", "C"); // 預設C
		setValue(extendField + "new_acct_flag", "Y"); // 預設C
		setValue(extendField + "stmt_cycle", "01"); // 預設01
		setValue(extendField + "card_indicator", "2"); // 預設2
		setValue(extendField + "acno_flag", "3"); // 預設3
		setValue(extendField + "corp_p_seqno", corpCorpPSeqno);
		setValue(extendField + "crt_date", sysDate);
		setValue(extendField + "crt_time", sysTime);
		setValue(extendField + "crt_user", "IcuD015");
		setValue(extendField + "mod_user", "ecs");
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", "IcuD015");
		
		try {
			insertTable();
		} catch (Exception e) {
			if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
				showLogMessage("I", "", "insert_act_acno error[dupRecord]");
				tmpNoPassReason = "N4";
				errTable = "act_acno";
			}
		}

//		if (dupRecord.equals("Y"))
//			comcr.errRtn("insert_act_acno error[dupRecord]", "", comcr.hCallBatchSeqno);

		return;
	}

	/***********************************************************************/
	void updateFscIcuD15Pseqo() throws Exception {
		daoTable = "fsc_icud15";
		updateSQL = "acno_p_seqno = ?,";
		updateSQL += " p_seqno = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_pgm  = ?";
		whereStr = "where rowid = ? ";
		setString(1, strPSeqno);
		setString(2, strPSeqno);
		setString(3, "IcuD015");
		setRowId(4, fscIcud15Rowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_fsc_icud15Pseqo not found!", "", "");
		}
	}

	/**********************************************************************/
	// void insert_act_acct() throws Exception {
	//
	// setValue("p_seqno", str_p_seqno);
	// setValue("acct_type", fsc_icud15_acct_type);
	// setValue("id_p_seqno", fsc_icud15_id_p_seqno);
	// setValue("mod_user", "ecs");
	// setValue("mod_time", sysDate + sysTime);
	// setValue("mod_pgm", "IcuD015");
	//
	// daoTable = "act_acct";
	//
	// insertTable();
	//
	// if (dupRecord.equals("Y"))
	// comcr.err_rtn("insert_act_acct error[dupRecord]", "",
	// comcr.h_call_batch_seqno);
	//
	// return;
	// }
	/**********************************************************************/
	void insertCcaCardAcct() throws Exception {

		daoTable = "cca_card_acct";
		extendField = daoTable + ".";
		setValue(extendField + "card_acct_idx", strCardAcctIdx);
		setValue(extendField + "p_seqno", strPSeqno);
		setValue(extendField + "acno_p_seqno", strPSeqno);
		setValue(extendField + "id_p_seqno", cardIdPSeqno);
		setValue(extendField + "acct_type", cardAcctType);
		setValue(extendField + "corp_p_seqno", corpCorpPSeqno);
		setValue(extendField + "acno_flag", "3");
		setValue(extendField + "crt_date", sysDate);
		setValue(extendField + "crt_user", "IcuD015");
		setValue(extendField + "mod_user", "ecs");
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", "IcuD015");
		
		try {
			insertTable();
		} catch (Exception e) {
			if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
				showLogMessage("I", "", "insert_cca_card_acct error[dupRecord] " + 
						"p_seqno:" + strPSeqno + "id_p_seqno:" + fscIcud15IdPSeqno);
				tmpNoPassReason = "N4";
				errTable = "cca_card_acct";
			}
		}				
		return;
	}

	/**********************************************************************/
	void updateCcaCardBase() throws Exception {

		daoTable = "cca_card_base ";
		updateSQL = "card_acct_idx = ?, ";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where card_no = ? ";
		setString(1, strCardAcctIdx);
		setString(2, sysDate + sysTime);
		setString(3, cardCardNo);

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_cca_card_base not found!", "", "card_no:" + cardCardNo);

		return;
	}

	/***********************************************************************/
	void insertCcaConsume() throws Exception {
		daoTable = "cca_consume";
		extendField = daoTable + ".";
		setValue(extendField + "card_acct_idx", strCardAcctIdx);
		setValue(extendField + "p_seqno", strPSeqno);		
		setValue(extendField + "MOD_USER", "ecs");
		setValue(extendField + "MOD_TIME", sysDate + sysTime);
		setValue(extendField + "MOD_PGM", "IcuD015");
		try {
			insertTable();
		} catch (Exception e) {
			if (e != null && e.getCause() instanceof SqlIntegrityConstraintViolationException) {
				showLogMessage("I", "", "insert_cca_consume duplicate[dupRecord] " + 
						"card_acct_idx:" + strCardAcctIdx + "p_seqno:" + strPSeqno);
				tmpNoPassReason = "N4";
				errTable = "cca_consume";
			}
		}					
	}

	/**********************************************************************/
	void selectFscIcud15B() throws Exception {

		sqlCmd = "select acno_p_seqno, status_cfq ";
		sqlCmd += "from fsc_icud15 ";
		// sqlCmd += "where file_no = ? ";
		sqlCmd += "where is_pass = 'Y' ";
		sqlCmd += "and is_process <> 'Y' ";
		sqlCmd += "and mod_acdr = 'D' ";
		sqlCmd += "order by file_no, id_no ";
		// setString(1, tmpfile_no);
		
		openCursor();
		while(fetchTable()) {
			fscIcud15AcnoPSeqno = "";
			fscIcud15StatusCfq = "";
			
			fscIcud15AcnoPSeqno = getValue("acno_p_seqno");
			fscIcud15StatusCfq = getValue("status_cfq");

			showLogMessage("I", "", "acno_p_seqno =" + fscIcud15AcnoPSeqno);

			updatCcaCardAcct();
			commitDataBase();
		}
		
		closeCursor();
		return;
	}

	/**********************************************************************/
	void updatCcaCardAcct() throws Exception {

		daoTable = "cca_card_acct ";
		updateSQL = "block_date = ?, ";
		updateSQL += "block_reason1 = decode(?,'C','14','F','26','Q','T1'), ";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where acno_p_seqno = ? ";
		setString(1, getBusinessDate);
		setString(2, fscIcud15StatusCfq);
		setString(3, sysDate + sysTime);
		setString(4, fscIcud15AcnoPSeqno);

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_cca_card_acct not found!", "", "acno_p_seqno:" + fscIcud15AcnoPSeqno);

		return;
	}

	/**********************************************************************/
	void selectDscIcud15C() throws Exception {
		
		int procCnt = 0;
		sqlCmd = "select card_no, id_no, acno_p_seqno, eng_name, sex, birthday, chi_name, home_tel_no, ";
		sqlCmd += "office_tel_no, cellar_phone, id_p_seqno, p_seqno, corp_p_seqno, acct_type, ";
		sqlCmd += "corp_no, unpay_amt, auth_not_deposit, acct_jrnl_bal,jrnl_bal_sign, mod_acdr, cellar_phone, ";
		sqlCmd += "locamt_cash_day, unpay_amt_cash, line_of_credit_amt, locamt_cash_rate , rowid as rowid , ";
		sqlCmd += "nopass_reason ";
		sqlCmd += "from fsc_icud15 ";
		// sqlCmd += "where file_no = ? ";
		sqlCmd += "where is_pass = 'Y' ";
		sqlCmd += "and is_process <> 'Y' ";
		sqlCmd += "and mod_acdr in ('A', 'C', 'D') ";
		sqlCmd += "order by file_no, corp_p_seqno , id_p_seqno ";
		// setString(1, tmpfile_no);
		
		openCursor();
		while(fetchTable()) {
			procCnt++;
			fscIcud15ModAcdr = getValue("mod_acdr");
			fscIcud15IdNo = getValue("id_no");
			fscIcud15IdPSeqno = getValue("id_p_seqno");
			fscIcud15PSeqno = getValue("p_seqno");
			fscIcud15AcnoPSeqno = getValue("acno_p_seqno");
			fscIcud15CardNo = getValue("card_no");
			fscIcud15EngName = getValue("eng_name");
			fscIcud15Sex = getValue("sex");
			fscIcud15Birthday = getValue("birthday");
			fscIcud15ChiName = getValue("chi_name");
			fscIcud15HomeTelNO = getValue("home_tel_no");
			fscIcud15OfficeTelNo = getValue("office_tel_no");
			fscIcud15CorpPSeqno = getValue("corp_p_seqno");
			fscIcud15AcctType = getValue("acct_type");
			fscIcud15CorpNo = getValue("corp_no");
			fscIcud15UnpayAmt = getValueDouble("unpay_amt");
			fscIcud15AcctJrnlBal = getValueDouble("acct_jrnl_bal");
			fscIcud15AcctJrnlBalSign = getValue("jrnl_bal_sign");
			fscIcud15AuthNotDeposit = getValueDouble("auth_not_deposit");
			fscIcud15CellarPhone = getValue("cellar_phone");
			fscIcud15LocamtCashDay = getValue("locamt_cash_day");
			fscIcud15UnpayAmtCash = getValueDouble("unpay_amt_cash");
			fscIcud15LineOfCreditAmt = getValueDouble("line_of_credit_amt");
			fscIcud15LocamtCashRate = getValueDouble("locamt_cash_rate");
			fscIcud15Rowid = getValue("rowid");
			fscIcud15NopassReason = getValue("nopass_reason");
			if (fscIcud15ModAcdr.equals("A") || fscIcud15ModAcdr.equals("C")) {
				updateActAcnoLineOfCreditAmt();
				//--nopass_reason = 99 是 106 599 資料 沒有 crd_idno 要update 的資料  所以不做
				if("99".equals(fscIcud15NopassReason) == false) {
					if(countCrdCard())
						updateCrdIdno();
					//--手機號碼更新
					if(countCrdCardForUpdatePhone())
						updateCrdIdnoCellarPhone();
				}
					
				selectActAcno();
			}

			updateCrdCard();
			updateCcaCardBaseC();
			
			if (fscIcud15LocamtCashDay.equals("88")) {
				totalunpayAmtCash += fscIcud15UnpayAmtCash;
			} else if (fscIcud15LocamtCashDay.equals("00")) {
				totalunpayAmtCash = 0;
			} else {
				totalunpayAmtCash = fscIcud15UnpayAmtCash;
			}
			totalunpayAmt += fscIcud15UnpayAmt;
			if (fscIcud15AcctJrnlBalSign.equals("D") || fscIcud15AcctJrnlBalSign.isEmpty()) {
				totalacctJrnlBal += fscIcud15AcctJrnlBal;
			} else if (fscIcud15AcctJrnlBalSign.equals("C")) {
				totalacctJrnlBal -= fscIcud15AcctJrnlBal;
			}
			if(totalunpayAmt>0) {
				tmpIntRateMcode = 1;
			}	else	{
				tmpIntRateMcode = 0;
			}
			totalauthNotDeposit += fscIcud15AuthNotDeposit;
			updateCcaCardAcctC();
			// V1.00.03刪除 update_act_acct_C();
			updatCcaConsume(); // V1.00.02新增
			updatActAcno();
			updateFscIcud15();				
			totalunpayAmt = 0;
			totalacctJrnlBal = 0;
			totalauthNotDeposit = 0;
			
			if(procCnt % 5000 ==0) {
				showLogMessage("I", "", "proc Icud15C cnt = ["+procCnt+"]");
			}
			
			commitDataBase();
		}
		
		closeCursor();
		return;
	}
	
	boolean countCrdCardForUpdatePhone() throws Exception {
		int tempInt = 0  ;
		String tempCellarPhone = "";
		
		//--商務卡有活卡
		sqlCmd = "select count(*) as db_cnt from crd_card where id_p_seqno = ? and acct_type in ('03','05','06') and current_code = '0' ";
		setString(1,fscIcud15IdPSeqno);
		selectTable();
		tempInt = getValueInt("db_cnt");
		
		sqlCmd = "select cellar_phone from crd_idno where id_p_seqno = ? ";
		setString(1,fscIcud15IdPSeqno);
		selectTable();
		tempCellarPhone = getValue("cellar_phone");
		
		//--商務卡有活卡且 crd_idno無值就 update 且 fsc_icud15.cellar_phone 有值
		if(tempInt >0 && tempCellarPhone.isEmpty() && fscIcud15CellarPhone.isEmpty() == false)
			return true;
		
		return false;
	}
	
	boolean countCrdCard() throws Exception {
		
		if(fscIcud15IdPSeqno.isEmpty())
			return false ;
		
		int tempCnt = 0 , tempCnt2 = 0 , tempCnt3 = 0 ;
		//--一般卡有活卡時不更新
		sqlCmd = "select count(*) as db_cnt from crd_card where id_p_seqno = ? and current_code ='0' and acct_type ='01' ";
		setString(1,fscIcud15IdPSeqno);
		selectTable();
		tempCnt = getValueInt("db_cnt");
		if(tempCnt > 0)
			return false;
		
		//--一般卡沒有活卡且商務卡沒有活卡時不更新
		sqlCmd = "select count(*) as db_cnt2 from crd_card where id_p_seqno = ? and current_code ='0' and acct_type in ('03','05','06') ";
		setString(1,fscIcud15IdPSeqno);
		selectTable();
		tempCnt2 = getValueInt("db_cnt2");
		if(tempCnt2 <=0)
			return false ;
		
		return true ;				
	}
	
	/**********************************************************************/
	void updatActAcno() throws Exception {
		daoTable = "act_acno";
		updateSQL = "int_rate_mcode = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = 'ecs',";
		updateSQL += " mod_pgm  = 'IcuD015'";
		whereStr = "where acno_p_seqno = ? ";
		setInt(1,tmpIntRateMcode);		
		setString(2, fscIcud15PSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_acno not found!", "acno_p_seqno:" + fscIcud15PSeqno, "");
		}
	}
	/**********************************************************************/
	void updateCrdIdnoCellarPhone() throws Exception {
		daoTable = "crd_idno";
		updateSQL = "cellar_phone = ? ";
		whereStr = "where id_p_seqno = ? ";
		setString(1, String.format("%-15.15s", fscIcud15CellarPhone).trim());
		setString(2, fscIcud15IdPSeqno);
		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_crd_idno not found!", "", "");
	}
	
	/**********************************************************************/
	void updateCrdIdno() throws Exception {
		//--手機號碼拉出來另外做
		String[] officeTel = comc.transTelNo(fscIcud15OfficeTelNo);
		String[] homeTel = comc.transTelNo(fscIcud15HomeTelNO);
		daoTable = "crd_idno ";
		updateSQL = "eng_name = ?, ";
		updateSQL += "sex = ?, ";
		updateSQL += "birthday = ?, ";
		updateSQL += "chi_name = ?, ";
		updateSQL += "home_area_code1 = ?, ";
		updateSQL += "home_tel_no1 = ?, ";
		updateSQL += "home_tel_ext1 = ?, ";
		updateSQL += "office_area_code1 = ?, ";
		updateSQL += "office_tel_no1 = ?, ";
		updateSQL += "office_tel_ext1 = ?, ";
//		updateSQL += "cellar_phone = ?, ";
		updateSQL += " mod_time  = sysdate, ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where id_p_seqno = ? ";
		setString(1, String.format("%-25.25s", fscIcud15EngName).trim());
		setString(2, fscIcud15Sex);
		setString(3, fscIcud15Birthday);
		setString(4, String.format("%-50.50s", fscIcud15ChiName).trim());
		setString(5, homeTel[0]);
		setString(6, homeTel[1]);
		setString(7, homeTel[2]);
		setString(8, officeTel[0]);
		setString(9, officeTel[1]);
		setString(10, officeTel[2]);
		setString(11, fscIcud15IdPSeqno);
//		if (fscIcud15HomeTelNO.isEmpty()) {
//			setString(5, "");
//			setString(6, "");
//		} else {
//			if(commString.ssIn("-", fscIcud15HomeTelNO)==true) {
//				int signCnt = 0;
//				signCnt = fscIcud15HomeTelNO.indexOf("-");
//				setString(5, commString.mid(fscIcud15HomeTelNO,0,signCnt));
//				setString(6, commString.mid(fscIcud15HomeTelNO,signCnt+1,fscIcud15HomeTelNO.length()));
//			} else {
//				if(fscIcud15HomeTelNO.length()<=10) {
//					setString(5, "");
//					setString(6, fscIcud15HomeTelNO);
//				}	else	{
//					setString(5, commString.mid(fscIcud15HomeTelNO, 0,3));
//					setString(6, commString.mid(fscIcud15HomeTelNO, 4,fscIcud15HomeTelNO.length()));
//				}
//				
//			}			
//		}

//		if (fscIcud15OfficeTelNo.isEmpty()) {
//			setString(7, "");
//			setString(8, "");
//		} else {
//			if(commString.ssIn("-", fscIcud15OfficeTelNo)==true) {
//				int signCnt2 = 0;
//				signCnt2 = fscIcud15OfficeTelNo.indexOf("-");
//				setString(7, commString.mid(fscIcud15OfficeTelNo,0,signCnt2));
//				setString(8, commString.mid(fscIcud15OfficeTelNo,signCnt2+1,fscIcud15OfficeTelNo.length()));
//			} else {
//				if(fscIcud15OfficeTelNo.length()<=10) {
//					setString(7, "");
//					setString(8, fscIcud15OfficeTelNo);
//				}	else	{					
//					setString(7, commString.mid(fscIcud15OfficeTelNo, 0,3));
//					setString(8, commString.mid(fscIcud15OfficeTelNo,4,fscIcud15OfficeTelNo.length()));
//				}
//				
//			}
//		}

//		setString(9, String.format("%-15.15s", fscIcud15CellarPhone).trim());
		

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_crd_idno not found!", "", "");

		return;
	}

	/**********************************************************************/
	void updateCrdCard() throws Exception {

		daoTable = "crd_card ";
		updateSQL = "acno_p_seqno = ?, ";
		updateSQL += "p_seqno = ?, ";
		updateSQL += "corp_p_seqno = ?, ";
		updateSQL += "corp_no = ?, ";
		updateSQL += "corp_no_code = '0', ";
		updateSQL += "acno_flag = '3', ";
		updateSQL += "id_p_seqno = ?, ";
		updateSQL += "major_id_p_seqno = ?, ";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where card_no = ? ";
		setString(1, fscIcud15AcnoPSeqno);
		setString(2, fscIcud15PSeqno);
		setString(3, fscIcud15CorpPSeqno);
		setString(4, fscIcud15CorpNo);
		setString(5, fscIcud15IdPSeqno);
		setString(6, fscIcud15IdPSeqno);
		setString(7, sysDate + sysTime);
		setString(8, fscIcud15CardNo);

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_crd_card not found!", "", "");

		return;
	}

	/**********************************************************************/
	void updateCcaCardBaseC() throws Exception {
		String birthday = "";
		if(fscIcud15Birthday.isEmpty() || fscIcud15Birthday.length() != 8) {
			daoTable = "cca_card_base ";
			updateSQL = "acno_p_seqno = ?, ";
			updateSQL += "p_seqno = ?, ";
			updateSQL += "corp_p_seqno = ?, ";
			updateSQL += "acno_flag = '3', ";
			updateSQL += "id_p_seqno = ?, ";
			updateSQL += "major_id_p_seqno = ?, ";
			updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
			updateSQL += " mod_user  = 'ecs', ";
			updateSQL += " mod_pgm  = 'IcuD015' ";
			whereStr = "where card_no = ? ";
			setString(1, fscIcud15AcnoPSeqno);
			setString(2, fscIcud15PSeqno);
			setString(3, fscIcud15CorpPSeqno);
			setString(4, fscIcud15IdPSeqno);
			setString(5, fscIcud15IdPSeqno);			
			setString(6, sysDate + sysTime);
			setString(7, fscIcud15CardNo);
		}	else	{
			birthday = String.format("%03d", Integer.valueOf(fscIcud15Birthday.substring(0, 4)) - 1911)
					+ fscIcud15Birthday.substring(4, 6) + fscIcud15Birthday.substring(6, 8);
			daoTable = "cca_card_base ";
			updateSQL = "acno_p_seqno = ?, ";
			updateSQL += "p_seqno = ?, ";
			updateSQL += "corp_p_seqno = ?, ";
			updateSQL += "acno_flag = '3', ";
			updateSQL += "id_p_seqno = ?, ";
			updateSQL += "major_id_p_seqno = ?, ";
			updateSQL += "voice_open_code = ?, ";
			updateSQL += "voice_open_code2 = ?, ";
			updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
			updateSQL += " mod_user  = 'ecs', ";
			updateSQL += " mod_pgm  = 'IcuD015' ";
			whereStr = "where card_no = ? ";
			setString(1, fscIcud15AcnoPSeqno);
			setString(2, fscIcud15PSeqno);
			setString(3, fscIcud15CorpPSeqno);
			setString(4, fscIcud15IdPSeqno);
			setString(5, fscIcud15IdPSeqno);
			setString(6, comc.transPasswd(0, birthday.substring(1)));
			setString(7, comc.transPasswd(0, birthday.substring(1)));
			setString(8, sysDate + sysTime);
			setString(9, fscIcud15CardNo);
		}
		

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_cca_card_base not found!", "", "");

		return;
	}

	/**********************************************************************/
	void updateCcaCardAcctC() throws Exception {
		String strunpayAmt = doubleFmt.format(totalunpayAmt);
		String strauthNotDeposit = doubleFmt.format(totalauthNotDeposit);
		String stracctJrnlBal = doubleFmt.format(totalacctJrnlBal);
		String strunpayAmtCash = doubleFmt.format(totalunpayAmtCash);

		daoTable = "cca_card_acct ";
		updateSQL = "unpay_amt = ?, ";
		updateSQL += " tot_amt_consume = ?, ";
		updateSQL += " jrnl_bal = ?, "; // V1.01.03
		updateSQL += " total_cash_utilized = ?, "; // V1.01.03
		updateSQL += " pay_amt = 0, "; // V1.00.01
		updateSQL += " acct_type = ? , ";
		updateSQL += " debit_flag = 'N' , ";
//		updateSQL += " auth_txlog_amt = 0, "; // V1.01.03
//		updateSQL += " auth_txlog_amt_cash = 0, "; // V1.01.03
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where p_seqno = ? ";
		setString(1, strunpayAmt);
		setString(2, strauthNotDeposit);
		setString(3, stracctJrnlBal);
		setString(4, strunpayAmtCash);
		setString(5, fscIcud15AcctType);
		setString(6, sysDate + sysTime);
		setString(7, fscIcud15PSeqno);

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_cca_card_acct not found!", "", "p_seqno:" + fscIcud15PSeqno);

		return;
	}

	/**********************************************************************/
	void selectActAcno() throws Exception {

		sqlCmd = "select acno_p_seqno, p_seqno ";
		sqlCmd += "from act_acno ";
		sqlCmd += "where corp_p_seqno = ? ";
		sqlCmd += "and acct_type = '' ";
		sqlCmd += "and acno_flag = '2' ";
		sqlCmd += "fetch first 1 rows only";
		setString(1, fscIcud15CorpPSeqno);

		if (selectTable() > 0) {
			acnoAcnoPSeqno = getValue("acno_p_seqno");
			acnoPSeqno = getValue("p_seqno");
			acnoAcnoPSeqno = acnoAcnoPSeqno.trim();
			acnoPSeqno = acnoPSeqno.trim();
			if (acnoAcnoPSeqno.length() > 0) {
				updateActAcnoCorp();
				updateCcaCardAcctCorp();						
			}
			if (acnoPSeqno.length() > 0) {
				// V1.00.03刪除 update_act_acct();
			}
		}

//		if (notFound.equals("Y"))
//			showLogMessage("D", "select_act_acno not found!", "select_act_acno not found! corp_p_seqno =" + fscIcud15CorpPSeqno);

		return;
	}
	
	/**********************************************************************/
	void updateCcaCardAcctCorp() throws Exception {
		daoTable = "cca_card_acct";
		updateSQL = "acct_type = ? ,";
		updateSQL += "debit_flag = 'N' , ";
//		updateSQL += "id_p_seqno = ? , ";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where acno_p_seqno = ? ";
		setString(1,fscIcud15AcctType);
//		setString(2,fscIcud15IdPSeqno);
		setString(2, sysDate + sysTime);
		setString(3,acnoAcnoPSeqno);
		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_cca_card_acct not found!", fscIcud15AcnoPSeqno, "");

		return;
	}
	
	/**********************************************************************/
	void updateActAcnoLineOfCreditAmt() throws Exception {
		daoTable = "act_acno ";
		updateSQL = " line_of_credit_amt = ?, ";
		updateSQL += " line_of_credit_amt_cash = ?, ";
		updateSQL += " mod_time  = sysdate, ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where acno_p_seqno = ? ";
		setDouble(1, Math.floor(fscIcud15LineOfCreditAmt)); // V1.01.12
		if(fscIcud15LocamtCashRate ==0) {
			setDouble(2, Math.floor(fscIcud15LineOfCreditAmt));
		}	else if(fscIcud15LocamtCashRate ==1) {
			setDouble(2, 0);
		}	else	{
			setDouble(2, Math.floor(fscIcud15LineOfCreditAmt * fscIcud15LocamtCashRate / 100)); // V1.01.12
		}		
		setString(3, fscIcud15AcnoPSeqno);

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_act_acno not found!", fscIcud15AcnoPSeqno, "");

		return;
	}

	/**********************************************************************/
	void updateActAcnoCorp() throws Exception {

		daoTable = "act_acno ";
		updateSQL = " acct_type = ?, ";
//		updateSQL += " id_p_seqno = ? ,";
		updateSQL += " stmt_cycle = '01' ,";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where acno_p_seqno = ? ";
		setString(1, fscIcud15AcctType);
//		setString(2, fscIcud15IdPSeqno);
		setString(2, sysDate + sysTime);
		setString(3, acnoAcnoPSeqno);

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_act_acno not found!", acnoAcnoPSeqno, "");

		return;
	}

	/**********************************************************************/
	// void update_act_acct() throws Exception {
	//
	// daoTable = "act_acct ";
	// updateSQL = " acct_type = ?, ";
	// updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
	// updateSQL += " mod_user = 'ecs', ";
	// updateSQL += " mod_pgm = 'IcuD015' ";
	// whereStr = "where p_seqno = ? ";
	// setString(1, fsc_icud15_acct_type);
	// setString(2, sysDate + sysTime);
	// setString(3, acno_p_seqno);
	//
	// updateTable();
	//
	// if (notFound.equals("Y"))
	// comcr.err_rtn("update_act_acct not found!", acno_p_seqno, "");
	//
	// return;
	// }
	/**********************************************************************/

	// void update_act_acct_C() throws Exception {
	// String stracct_jrnl_bal = doubleFmt.format(totalacct_jrnl_bal);
	//
	// daoTable = "act_acct ";
	// updateSQL = "acct_jrnl_bal = ?, ";
	// updateSQL += " mod_time = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
	// updateSQL += " mod_user = 'ecs', ";
	// updateSQL += " mod_pgm = 'IcuD015' ";
	// whereStr = "where p_seqno = ? ";
	// setString(1, stracct_jrnl_bal);
	// setString(2, sysDate + sysTime);
	// setString(3, fsc_icud15_p_seqno);
	//
	// updateTable();
	//
	// if (notFound.equals("Y"))
	// comcr.err_rtn("update_act_acct not found!", "", "");
	//
	// return;
	// }
	/***********************************************************************/
	void updatCcaConsume() throws Exception {		
		daoTable = "cca_consume";
		updateSQL = " tot_amt_consume = ?,";
		updateSQL += " auth_txlog_amt_2  = 0, ";
		updateSQL += " auth_txlog_amt_cash_2  = 0, ";
		updateSQL += " unpaid_precash = ? ,";
		updateSQL += " tot_unpaid_amt = 0 , ";
		if(totalacctJrnlBal<0) {
			updateSQL += " unpaid_consume_fee = 0 ,";
			updateSQL += " paid_consume_fee =0 , ";
			updateSQL += " pre_pay_amt = ? ,";			
		}	else	{
			updateSQL += " unpaid_consume_fee = ? ,";
			updateSQL += " paid_consume_fee =0 ,";
			updateSQL += " pre_pay_amt = 0 ,";	
		}
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = 'ecs',";
		updateSQL += " mod_pgm  = 'IcuD015'";
		whereStr = " where p_seqno = ? ";
		setDouble(1, totalauthNotDeposit); // Fsc_Icud15 auth_not_deposit累計
		setDouble(2, totalunpayAmtCash);
		if(totalacctJrnlBal<0) {
			setDouble(3, totalacctJrnlBal*-1);
		}	else	{
			setDouble(3, totalacctJrnlBal);
		}		
		setString(4, fscIcud15PSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_cca_consume not found!", "", "");
		}
	}

	/**********************************************************************/
	void updateFscIcud15() throws Exception {

		daoTable = "fsc_icud15 ";
		updateSQL = "is_process = 'Y', ";
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += " mod_user  = 'ecs', ";
		updateSQL += " mod_pgm  = 'IcuD015' ";
		whereStr = "where rowid = ? ";
		setString(1, sysDate + sysTime);
		setRowId(2, fscIcud15Rowid);

		updateTable();

		if (notFound.equals("Y"))
			comcr.errRtn("update_fsc_icud15 not found!", "", "");

		return;
	}
	
	/**********************************************************************/
	void calCorpAmount() throws Exception {
		fetchExtend = "corp.";
		sqlCmd = "select acct_type , corp_p_seqno , sum(unpay_amt) as unpay_amt , "
			   + "sum(tot_amt_consume) as tot_amt_consume , sum(jrnl_bal) as jrnl_bal , "
			   + "sum(total_cash_utilized) as total_cash_utilized , sum(pay_amt) as pay_amt "
			   + "from cca_card_acct where acno_flag in ('3','Y') "
			   + "group by acct_type , corp_p_seqno "
			   + "order by acct_type , corp_p_seqno "
			   ;
		
		openCursor();
		
		while(fetchTable()) {
			initCal();
			calAcctType = getValue("corp.acct_type");
			calCorpPSeqno = getValue("corp.corp_p_seqno");
			calUnpayAmt = getValueDouble("corp.unpay_amt");
			calTotAmtConsume = getValueDouble("corp.tot_amt_consume");
			calJrnlBal = getValueDouble("corp.jrnl_bal");
			calTotalCashUtilized = getValueDouble("corp.total_cash_utilized");
			calPayAmt = getValueDouble("corp.pay_amt");
			calCardAcctIdx = getCropCardAcctIdx();
			updateCcaCardAcctForCorp();
			if(corpContinue)	continue;
			calConsume();
			updateCcaConsumeForCorp();
		}
		closeCursor();
		commitDataBase();
	}
	
	void updateCcaConsumeForCorp() throws Exception {
		daoTable = "cca_consume";
		updateSQL = "tot_amt_consume = ? , auth_txlog_amt_2 = ? , auth_txlog_amt_cash_2 = ? , ";
		updateSQL += "unpaid_precash = ? , tot_unpaid_amt = ? , ";		
		if(calJrnlBal >=0) {
			updateSQL += " unpaid_consume_fee = ? , paid_consume_fee = 0 , pre_pay_amt = 0 ,";
		} else {
			updateSQL += " unpaid_consume_fee = 0 , paid_consume_fee = 0 , pre_pay_amt = ? ,";
		}		
		updateSQL += " mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
		updateSQL += "mod_user  = 'ecs', mod_pgm  = 'IcuD015' ";
		whereStr = "where card_acct_idx = ? ";
		
		setDouble(1,consumeTotAmt);
		setDouble(2,consumeTxLogAmt2);
		setDouble(3,consumeTxLogAmtCash2);
		setDouble(4,consumeUnpaidPrecash);
		setDouble(5,consumeTotUnpaidAmt);
		if(calJrnlBal >=0) {
			setDouble(6,calJrnlBal);
		} else {
			setDouble(6,calJrnlBal*-1);
		}		
//		setDouble(6,consumeUnpaidConsumeFee);
//		setDouble(7,consumePaidConsumeFee);
//		setDouble(8,consumePrePayAmt);
		setString(7,sysDate + sysTime);
		setDouble(8,calCardAcctIdx);
		
		updateTable();

		if (notFound.equals("Y")) {
			corpContinue = true;
			showLogMessage("I", "", "update_cca_consume_Corp not found! Corp_p_seqno = "+calCorpPSeqno);
		}						
	}
	
	void calConsume() throws Exception {
		sqlCmd = "select sum(tot_amt_consume) as tot_amt_consume , sum(auth_txlog_amt_2) as auth_txlog_amt_2 ,"
			   + "sum(auth_txlog_amt_cash_2) as auth_txlog_amt_cash_2 , sum(unpaid_precash) as unpaid_precash , "
			   + "sum(tot_unpaid_amt) as tot_unpaid_amt , sum(unpaid_consume_fee) as unpaid_consume_fee , "
			   + "sum(paid_consume_fee) as paid_consume_fee , sum(pre_pay_amt) as pre_pay_amt "
			   + "from cca_consume where card_acct_idx in "
			   + "(select card_acct_idx from cca_card_acct where corp_p_seqno = ? and acct_type = ? and acno_flag in ('3','Y')) "
			   ;
		
		setString(1,calCorpPSeqno);
		setString(2,calAcctType);
		
		if(selectTable()>0) {
			consumeTotAmt = getValueDouble("tot_amt_consume");
			consumeTxLogAmt2 = getValueDouble("auth_txlog_amt_2");
			consumeTxLogAmtCash2 = getValueDouble("auth_txlog_amt_cash_2");
			consumeUnpaidPrecash = getValueDouble("unpaid_precash");
			consumeTotUnpaidAmt = getValueDouble("tot_unpaid_amt");
			consumeUnpaidConsumeFee = getValueDouble("unpaid_consume_fee");
			consumePaidConsumeFee = getValueDouble("paid_consume_fee");
			consumePrePayAmt = getValueDouble("pre_pay_amt");
		}
	}
	
	double getCropCardAcctIdx() throws Exception {
		
		sqlCmd = "select card_acct_idx from cca_card_acct where corp_p_seqno = ? and acct_type = ? and acno_flag = '2' ";
		setString(1,calCorpPSeqno);
		setString(2,calAcctType);
		
		if(selectTable()>0)	return getValueDouble("card_acct_idx");
		
		return 0 ;
	}
	
	void updateCcaCardAcctForCorp() throws Exception {
		daoTable = "cca_card_acct";
		updateSQL = "unpay_amt = ? , tot_amt_consume = ? , jrnl_bal = ? , total_cash_utilized = ? ,";
		updateSQL += "pay_amt = ? , mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), mod_user  = 'ecs', ";
		updateSQL += "mod_pgm  = 'IcuD015' ";
		whereStr = "where card_acct_idx = ? ";
		setDouble(1,calUnpayAmt);
		setDouble(2,calTotAmtConsume);
		setDouble(3,calJrnlBal);
		setDouble(4,calTotalCashUtilized);
		setDouble(5,calPayAmt);
		setString(6,sysDate+sysTime);
		setDouble(7,calCardAcctIdx);
		
		updateTable();

		if (notFound.equals("Y")) {
			corpContinue = true;
			showLogMessage("I", "", "update_cca_card_acct_Corp not found! Corp_p_seqno = "+calCorpPSeqno);
		}							
	}
	
	void procCorpAmt() throws Exception {
		//-- 處理 106 599 的公司歸戶額度
		//-- sum acno_flag 3 them update acno_flag 2 act_acno.line_of_credit_amt
		String tmpCorpPSeqno = "" ; double tmpTotalAmt = 0.0 , tmpTotalCash = 0.0;
		fetchExtend = "corp2.";
		sqlCmd = "select corp_p_seqno , sum(line_of_credit_amt) as total_amt , sum(line_of_credit_amt_cash) as total_cash "
				+ "from act_acno where acno_flag in ('3','Y') and acct_type ='06' and "
				+ "acno_p_seqno in (select acno_p_seqno from crd_card where group_code ='1599') "
				+ "group by corp_p_seqno  ";
		
		openCursor();
		
		while(fetchTable()) {			
			tmpCorpPSeqno = "";
			tmpTotalAmt = 0.0;
			tmpTotalCash = 0.0;
			//--			
			tmpCorpPSeqno = getValue("corp2.corp_p_seqno");
			tmpTotalAmt = getValueDouble("corp2.total_amt");
			tmpTotalCash = getValueDouble("corp2.total_cash");
			//--update
			daoTable = "act_acno";
			updateSQL = "line_of_credit_amt = ? , line_of_credit_amt_cash = ? , mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), mod_user  = 'ecs',";
			updateSQL += "mod_pgm  = 'IcuD015' ";
			whereStr = "where acno_flag ='2' and acct_type = '06' and corp_p_seqno = ? ";
			setDouble(1,tmpTotalAmt);
			setDouble(2,tmpTotalCash);
			setString(3,sysDate+sysTime);			
			setString(4,tmpCorpPSeqno);
			updateTable();
			if (notFound.equals("Y")) {				
				showLogMessage("I", "", "update_act_acno_Corp_106_599 not found! Corp_p_seqno = "+tmpCorpPSeqno);
			}		
		}
		closeCursor();
		commitDataBase();
	}
	
	/**********************************************************************/
	void renameFile(String removeFileName) throws Exception {

		String tmpstr1 = comc.getECSHOME() + "/media/icu/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;
		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");

		// removeFileName = String.format("M00600000.ICCACQN%s.%s", "A",
		// removeFileName.substring(19,
		// 27));
		// String tmpstr3 = comc.GetECSHOME() + "/media/Fis/" + removeFileName;
		// String tmpstr4 = comc.GetECSHOME() + "/media/Fis/backup/" + removeFileName +
		// "." + sysDate;
		// if (comc.file_rename(tmpstr3, tmpstr4) == false) {
		// showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
		// return;
		// }
		// showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr4 + "]");
	}

	/**********************************************************************/
	public String[] getFieldValue(String rec, int[] parm) {
		int x = 0;
		int y = 0;
		byte[] bt = null;
		String[] ss = new String[parm.length];
		try {
			bt = rec.getBytes("MS950");
		} catch (Exception e) {
			showLogMessage("I", "", comc.getStackTraceString(e));
		}
		for (int i : parm) {
			try {
				ss[y] = new String(bt, x, i, "MS950");
			} catch (Exception e) {
				showLogMessage("I", "", comc.getStackTraceString(e));
			}
			y++;
			x = x + i;
		}

		return ss;
	}
	
	void procErrorReport() throws Exception {		
		String errorFile = "";
		int errFile03 = 0 ;
		if(getFileDate.isEmpty())	getFileDate = getBusinessDate;
		if(getFileNo.isEmpty())	getFileNo = "01";
		
		errorFileName = "ICCACQND.ERR."+getFileDate+getFileNo+".TXT";
		errorFile = String.format("%s/media/icu/error/%s", comc.getECSHOME(), errorFileName);
		errFile03 = openOutputText(errorFile, "MS950");
		if (errFile03 < 0) {
			comcr.errRtn("creat report error", "","無法產出錯誤報表");
			return ;
		}
		extendField = "report.";
		sqlCmd = "select * from fsc_icud15 where 1=1 and file_no like ? and is_pass = 'N' ";
		setString(1,getBusinessDate+"%");
		int reportCnt = selectTable();
		if(reportCnt <=0) {
			closeOutputText(errFile03);
			return ;
		}
		StringBuilder tempBuf = new StringBuilder();
		String errorReason = "" , newLine = "\r\n"  , partFileName = "M00600000.ICCACQND." , tempFile = "";
		for(int ii=0;ii<reportCnt;ii++) {
			errorReason = "";
			tempFile = "";
			tempFile = getValue("report.file_no",ii);
			tempFile = commString.mid(tempFile, 2);
			tempBuf.setLength(0);
			tempBuf.append(comc.fixLeft(getValue("report.mod_acdr",ii), 1));
			tempBuf.append(comc.fixLeft(getValue("report.id_no",ii), 11));
			if(getValue("report.card_no",ii).isEmpty()) {
				tempBuf.append(comc.fixLeft(getValue("report.card_type",ii)+"-"+getValue("report.card_seqno",ii), 16));
			}	else	{
				tempBuf.append(comc.fixLeft(getValue("report.card_no",ii), 16));
			}			
			tempBuf.append(comc.fixLeft(getValue("report.corp_no_icud15",ii), 11));
			if(getValue("report.nopass_reason",ii).equals("N1")) {
				errorReason = getValue("report.nopass_reason",ii)+"-異動碼錯誤";
			}	else if(getValue("report.nopass_reason",ii).equals("N2")) {
				errorReason = getValue("report.nopass_reason",ii)+"-卡片資料不存在";
			}	else if(getValue("report.nopass_reason",ii).equals("N3")) {
				errorReason = getValue("report.nopass_reason",ii)+"-異動碼為D時，帳戶狀態碼錯誤";
			}	else if(getValue("report.nopass_reason",ii).equals("N4")) {
				errorReason = getValue("report.nopass_reason",ii)+"-Insert Table Error";
			}	else if(getValue("report.nopass_reason",ii).equals("N6")) {
				errorReason = getValue("report.nopass_reason",ii)+"-客戶代號第11碼延伸碼不為空白或'R'";
			}	else if(getValue("report.nopass_reason",ii).equals("N7")) {
				errorReason = getValue("report.nopass_reason",ii)+"-營利事業編號與企業代號不同";
			}	else if(getValue("report.nopass_reason",ii).equals("N8")) {
				errorReason = getValue("report.nopass_reason",ii)+"-身分證號碼(ID_NO)與客戶代號(ID_NO_ICUD15)不同";
			}	else if(getValue("report.nopass_reason",ii).equals("N0")) {
				errorReason = getValue("report.nopass_reason",ii)+"-Data Not Match";
			}	else if(getValue("report.nopass_reason",ii).equals("N9")) {
				errorReason = getValue("report.nopass_reason",ii)+"-卡檔中有 id_p_seqno 對應不到卡人檔";
			}
			tempBuf.append(comc.fixLeft(errorReason, 189));
			tempBuf.append(comc.fixLeft("檔名 =["+partFileName+tempFile+"]", 50));
			tempBuf.append(comc.fixLeft(getBusinessDate, 8));
			tempBuf.append(newLine);
			writeTextFile(errFile03, tempBuf.toString());
		}
		closeOutputText(errFile03);
	}
	
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + errorFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2EMP", "mput " + errorFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + errorFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(errorFileName);
		}
		
		//--剔退檔案
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/icu/error", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;
		
		showLogMessage("I", "", "mput " + errorFileName2 + " 開始傳送....");
		errCode = commFTP.ftplogName("NCR2EMP", "mput " + errorFileName2);
		
		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + errorFileName2 + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(errorFileName);
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
	
	void initCal() {
		calAcctType = "";
		calCorpPSeqno = "";
		calUnpayAmt = 0;
		calTotAmtConsume = 0;
		calJrnlBal = 0 ;
		calTotalCashUtilized = 0 ;
		calPayAmt = 0;
		calCardAcctIdx = 0 ;
		consumeTotAmt = 0;
		consumeTxLogAmt2 = 0;
		consumeTxLogAmtCash2 = 0;
		consumeUnpaidPrecash = 0;
		consumeTotUnpaidAmt = 0;
		consumeUnpaidConsumeFee = 0;
		consumePaidConsumeFee = 0;
		consumePrePayAmt = 0;
		corpContinue = false;
	}
	
	/**********************************************************************/
	private Map processDataRecord(String[] row, String[] dt) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		int j = 0;
		for (String s : dt) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}		
	
} // End of class FetchSample
