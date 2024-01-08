/***********************************************************************************
*                                                                                  *
*                              MODIFICATION LOG                                    *
*                                                                                  *
*    DATE    Version     AUTHOR              DESCRIPTION                           *
*  --------  ---------  ----------  -----------------------------------------      *
*  109/04/30   V1.01.00    Rou         Initial                                     *
*  109/06/17   V1.01.01    Pino        getFileDate比對、update_cca_card_acct         *
*  109/06/24   V1.01.02    Pino        讀不到檔案不須停住                                                                                 *
*  109-07-22               yanghan     修改了字段名称                                                                                         *
*  109-08-19   V1.01.03    shiyuqi     線上繳款還額作業(沖正)                                                                           *
*  109-08-24   V1.01.04    Alex        收檔前先處理DB內未還額資料(含線上、批次)、收檔後直接進行還額  *
*  109-08-31   V1.01.05    Alex        Online 異動後 1小時才可以還額					    *
*  109-09-04   V1.01.06    Zuwei       code scan issue					   			*
*  109-10-16   V1.01.07    Alex        update cca_consume							*
*  109-10-19   V1.01.08    shiyuqi       updated for project coding standard     *
*  111-01-05   V1.01.09    Alex        1hr > 20 min                                 *
*  111-01-28   V1.01.10    Alex        Reset 時間改為參數                                                                              *
*  111-02-14   V1.01.11    Ryan        big5 to MS950                                           *
*  111-04-06   V1.01.12    Alex        公司總戶還額                                                                                              *
*  111-04-25   V1.01.13    Alex        處理已覆核資料									*
*  111-06-17   V1.01.14    Alex        改為多筆 commit									*
*  111-07-13   V1.01.15    Alex        清空殘留值										*
*  111-12-07   V1.01.16    Alex        根據參數判斷是否清除來源為全國繳費網資料                                         *
*  111-12-15   V1.01.17    Alex        取消reset參數判斷 , 改為批次一律清除線上輸入資料                      *
*  112-07-03   V1.01.18    Alex        繳款方式為沖正代號時做沖正處理                                                            *
************************************************************************************/
package Act;

import com.*;

import Dxc.Util.SecurityUtil;

import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class ActA200 extends AccessDAO {
	private final String progname = "每天接收繳款還額檔案及執行還額作業 112/07/03  V1.01.18";

	CommFunction comm = new CommFunction();
	CommCrd comc = null;
	CommRoutine comr = null;
	CommCrdRoutine comcr = null;
	CommDate  commDate = new CommDate();
	CommString commstring = new CommString();
	
	int debug = 1;
	int debugd = 0;

	String checkHome = "";
	String hCallErrorDesc = "";
	String hCallBatchSeqno = "";
	String hCallRProgramCode = "";
	String hTempUser = "";
	String getBusinessDate;
	String hBusiChiDate = "";
	int totalFile = 0;

	String getFileName = "";
	String getFileDate = "";
	String getFileNo;
	double actRepayPayAmt;
	String actRepayAcnoPSeqno;
	String actRepayCorpPSeqno = "";
	String actRepayCorpAcnoPSeqno = "";
	String actRepayAcctType = "";
	String fileName1 = "", fileName2 = "";
	String hEflgRowid = "";
	
	int hHeadCnt = 0;
//  protected final String dT1Str = "col1, col2, col3, col4, col5, col6, col7, col8, col9, col10";
	protected final String dT1Str = "col1, col2, col3, col4, col5, col6, col7, col8, col9";
//  protected final int[] dt1Length = {16, 1, 15, 2, 8, 6, 1, 1, 4, 4};
	protected final int[] dt1Length = { 16, 14, 1, 2, 7, 1, 1, 4, 4 };

	protected String[] dT1 = new String[] {};

	String hBatchno = "";
	double hRecno = 0;
	String hGroupCode = "";
	String hRegBankNo = "";
	String hIntroduceNo = "";
	String tmpChar1 = "";
	String tmpChar = "";
	double tmpDoub = 0;
	int tmpCardAcctIdx = 0;
	int tmpCorpCardAcctIdx = 0;
	long tmpLong = 0;
	int tmpInt = 0;
	int totalTemp;
	int successTemp;
	int errorTemp;
	int hErrorCnt;
	int hSuccessCnt;
	int tmpserialNo = 0;
	String tmppaymentType2 = "";
	String tmppaymentType = "";
	String tmppayCardNo = "";
	String tmpfileNo = getFileDate + getFileNo;
	String tmpacctType = "";
	String tmpidPSeqno = "";
	String tmpcorpPSeqno = "";
	String tmppSeqno = "";
	String tmpacnoPSeqno = "";
	String tmpprocMark = "";
	String tmpisPass = "";
	String tmpDataFrom = "";
	String tmpFileNo = "";
	String tmpSerialNo = "";
	String tmpModTime = "";
	String tmpSysDate = "";
	String resetFlag = "";
	double tmpPayAmt = 0.0;
	int resetMin = 0 ;
	int fi;
	int readCnt = 0;
	boolean clearTypeE1 = true;
	
	// ************************************************************************

	public static void main(String[] args) throws Exception {
		ActA200 proc = new ActA200();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	// ************************************************************************
	public int mainProcess(String[] args) {
		try {
			dT1 = dT1Str.split(",");

			comc = new CommCrd();
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			// 固定要做的
			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			
			//--判斷是否清除全國還額資料
			if(args.length ==1 ) {
				if("N".equals(args[0]))
					clearTypeE1 = false;
				else
					clearTypeE1 = true;
			}	else if(args.length > 1) {
				if("N".equals(args[0]))
					clearTypeE1 = false;
				else
					clearTypeE1 = true;
				
				hCallBatchSeqno = args[1];
			}
			
			if(clearTypeE1) {
				showLogMessage("I", "", "參數設定為[Y] , 將會清除全國繳費網線上還額資料");
			}	else	{
				showLogMessage("I", "", "參數設定為[N] , 不清除全國繳費網線上還額資料");
			}
			
//			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comr = new CommRoutine(getDBconnect(), getDBalias());
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

			selectPtrBusinday();			
			showLogMessage("I", "", "Business_date=[" + getBusinessDate + "]");
			/***取得及時還額參數***/
			//--改為批次執行時一律清除線上輸入資料
//			getResetParm();
			/*** 處理已存在於資料庫內的未還額資料 ***/
			procNotRepayData();
			/*** 取得檔案 ***/
			openFile();
			/*** 進行還額本次收檔資料 ***/
			procRepayData();
			showLogMessage("I", "", "程式執行結束,筆數 = [ " + totalFile + " ]");
			commitDataBase();

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
	
	void getResetParm() throws Exception {
		
		selectSQL = "wf_value , wf_value6 ";
		daoTable = "ptr_sys_parm";
		whereStr = "where wf_parm ='SYSPARM' and wf_key = 'RESETMIN' ";
		selectTable();
		
		if("Y".equals(notFound)) {
			showLogMessage("I", "", "及時還額參數未設定 , ptr_sys_parm wf_parm[SYSPARM] wf_key[RESETMIN]");
			showLogMessage("I", "", "帶入預設參數 , 還額RESET 時間:20分鐘");
			resetFlag = "Y";
			resetMin = 20;
			return ;
		}
		
		resetFlag = getValue("wf_value");
		resetMin = getValueInt("wf_value6");
		
		if("N".equals(resetFlag)) {
			showLogMessage("I", "", "及時還額RESET參數設定為 N , Online來源不進行Reset");
			return ;
		}	else if("Y".equals(resetFlag)) {
			showLogMessage("I", "", "及時還額RESET參數設定為 Y , Online來源進行Reset , Reset 間隔時間為: ["+resetMin+"] 分鐘");
			return ;
		}	else {
			resetFlag = "Y";
			showLogMessage("I","","旗標設定錯誤 , 自動進行Reset , Reset 間隔時間為: ["+resetMin+"] 分鐘");
			return ;
		}
		
	}
	
	/***********************************************************************/
	public int selectCrdFileCtl() throws Exception {

		selectSQL = "count(*) as all_cnt";
		daoTable = "crd_file_ctl";
		whereStr = "WHERE file_name  = ? ";
		setString(1, getFileName);
		selectTable();

		if (getValueInt("all_cnt") > 0) {
			showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + getFileName + "]");
			return 1;
		}
		return 0;
	}

	void procRepayData() throws Exception {
		// --處理本次收檔資料進行還額
		int procCnt2 = 0;
		fetchExtend = "arc2.";
		sqlCmd = " select pay_amt , acno_p_seqno , data_from , file_no , serial_no , corp_p_seqno , acct_type from act_repay_creditlimit ";
		sqlCmd += " where proc_mark = '' and is_pass ='Y' and is_repay <> 'Y' and data_from = 'B' and apr_flag in ('','Y') ";
		openCursor();
		while (fetchTable()) {
			initData();
			actRepayPayAmt = getValueDouble("arc2.pay_amt");
			tmpDataFrom = getValue("arc2.data_from");
			tmpFileNo = getValue("arc2.file_no");
			tmpSerialNo = getValue("arc2.serial_no");
			actRepayAcnoPSeqno = getValue("arc2.acno_p_seqno");
			actRepayCorpPSeqno = getValue("arc2.corp_p_seqno");			
			actRepayAcctType = getValue("arc2.acct_type");		
			getCardAcctIdx();
			updateCcaCardAcct(1);
			updateCcaConsume(1);			
			if(actRepayCorpPSeqno.isEmpty() == false) {
				//--公司總戶也要還額
				getCorpBaseData();
				if(actRepayCorpAcnoPSeqno.isEmpty() || tmpCorpCardAcctIdx == 0)
					continue;				
				updateCcaCardAcctCorp(1);
				updateCcaConsumeCorp(1);				
			}
			updateActRepayCreditlimit();
			
			procCnt2++;
			
			if(procCnt2 % 5000 ==0) {
				commitDataBase();
				showLogMessage("I", "", "本次收檔還額處理 筆數 = ["+procCnt2+"]");
			}
			
		}
		closeCursor();
	}
	
	void getCorpBaseData() throws Exception {
		if (actRepayCorpPSeqno.isEmpty())
			return;
		sqlCmd = "select acno_p_seqno as corp_acno_p_seqno from act_acno where corp_p_seqno = ? and acct_type = ? and acno_flag = '2' ";
		setString(1,actRepayCorpPSeqno);
		setString(2,actRepayAcctType);
		int recordCnt = selectTable();
		if (recordCnt <= 0) {
			showLogMessage("I", "", "查詢公司戶基本資料錯誤 , corp_p_seqno = [" + actRepayCorpPSeqno + "]");
			return;
		}

		actRepayCorpAcnoPSeqno = getValue("corp_acno_p_seqno");

		sqlCmd = "select card_acct_idx as corp_card_acct_idx from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		setString(1,actRepayCorpAcnoPSeqno);
		recordCnt = selectTable();
		if (recordCnt <= 0) {
			showLogMessage("I", "", "查詢公司戶授權帳戶資料錯誤 , 帳戶流水號 = [" + actRepayCorpAcnoPSeqno + "]");			
			return;
		}

		tmpCorpCardAcctIdx = getValueInt("corp_card_acct_idx");
	}
	
	void getCardAcctIdx() throws Exception {
		
		sqlCmd = "select card_acct_idx from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		setString(1,actRepayAcnoPSeqno);
		int recordCnt = selectTable();
		if(recordCnt >0) {			
			tmpCardAcctIdx = getValueInt("card_acct_idx"); 
			return ;
		}
				
		return ;
	}
	
	void procNotRepayData() throws Exception {
		// --處理批次和線上未還額資料
		// -- apr_flag ='Y' 已覆核 , apr_flag = '' 舊資料
		int procCnt = 0;
		fetchExtend = "arc.";
		sqlCmd = " select pay_amt , acno_p_seqno , data_from , file_no , serial_no , ";
		sqlCmd += " to_char(mod_time,'yyyy-mm-dd hh24:mi:ss') as mod_time , to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as sys_date , ";
		sqlCmd += " corp_p_seqno , acct_type ";
		sqlCmd += " from act_repay_creditlimit ";
		sqlCmd += " where proc_mark = '' and is_pass ='Y' and is_repay <> 'Y' and apr_flag in ('','Y') ";
		
		//--根據參數判斷此次是否清除全國繳費網還額資料
		if(clearTypeE1 == false) {
			sqlCmd += " and payment_type2 <> 'E1' ";
		}
		
		openCursor();
		while (fetchTable()) {
			initData();
			actRepayPayAmt = getValueDouble("arc.pay_amt");
			tmpDataFrom = getValue("arc.data_from");
			tmpFileNo = getValue("arc.file_no");
			tmpSerialNo = getValue("arc.serial_no");
			actRepayAcnoPSeqno = getValue("arc.acno_p_seqno");
			tmpModTime = getValue("arc.mod_time");
			tmpSysDate = getValue("arc.sys_date");
			actRepayCorpPSeqno = getValue("arc.corp_p_seqno");			
			actRepayAcctType = getValue("arc.acct_type");
			
			if (tmpDataFrom.equals("B")) {
				getCardAcctIdx();
				updateCcaCardAcct(1);
				updateCcaConsume(1);
				if(actRepayCorpPSeqno.isEmpty() == false) {
					//--公司總戶也要還額
					getCorpBaseData();
					if(actRepayCorpAcnoPSeqno.isEmpty() || tmpCorpCardAcctIdx == 0)
						continue;				
					updateCcaCardAcctCorp(1);
					updateCcaConsumeCorp(1);				
				}				
			}	else if (tmpDataFrom.equals("O")) {
				//--改為批次執行時一律清除線上輸入資料
//				if("N".equals(resetFlag))
//					continue;
//				if (checkTime(tmpModTime, tmpSysDate) == false)
//					continue;
				getCardAcctIdx();
				updateCcaCardAcct(2);
				updateCcaConsume(2);
				if(actRepayCorpPSeqno.isEmpty() == false) {
					//--公司總戶也要還額
					getCorpBaseData();
					if(actRepayCorpAcnoPSeqno.isEmpty() || tmpCorpCardAcctIdx == 0)
						continue;				
					updateCcaCardAcctCorp(2);
					updateCcaConsumeCorp(2);				
				}
			} else
				continue; // --沒有標示來源跳過
			updateActRepayCreditlimit();
			totalFile++;
			procCnt ++;
			
			if(procCnt % 5000 ==0) {
				commitDataBase();
				showLogMessage("I", "", "未還額資料處理 筆數 = ["+procCnt+"]");
			}
			
		}
		closeCursor();
		commitDataBase();
	}

	// 計算時間差 Online 部分需異動後 依照參數進行還額
	boolean checkTime(String modTime, String sysTime) throws Exception {
		SimpleDateFormat form1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date d1 = form1.parse(modTime);
		Date d2 = form1.parse(sysTime);
		
		long diff = d2.getTime() - d1.getTime();
		long min = diff / (1000*60);
		if (min >= resetMin)
			return true;

		return false;
	}

	// 檢查是否有檔案需要處理
	/***********************************************************************/
	void openFile() throws Exception {

		String filePath = String.format("%s/media/act", comc.getECSHOME());
		filePath = SecurityUtil.verifyPath(filePath);
		File checkFilePath = new File(filePath);
		filePath = Normalizer.normalize(filePath, java.text.Normalizer.Form.NFKD);
		if (!checkFilePath.isDirectory())
			comcr.errRtn(String.format("[%s]目錄不存在", filePath), "", hCallBatchSeqno);

		List<String> listOfFiles = comc.listFS(filePath, "", "");
		showLogMessage("I", "", "Process file Path =" + filePath);
		for (String file : listOfFiles) {
			if (file.length() != 29)
				continue;
			if (!file.substring(0, 15).equals("ECSCREDITLIMIT_"))
				continue;

			getFileName = file;
			showLogMessage("I", "", "File = [" + getFileName + "]");
			getFileDate = getFileName.substring(15, 23); // 檔名日期
			getFileNo = getFileName.substring(23, 25); // 檔名序號
			tmpfileNo = getFileDate + getFileNo;
			if (getFileDate.equals(getBusinessDate) ||  commDate.dateAdd(getFileDate, 0, 0, 1).equals(getBusinessDate)) {
			} else {
				showLogMessage("I", "",
						"File Date = [" + getFileDate + "] 日期不符合 BusinessDate = [" + getBusinessDate + "]");
				continue;
			}

			if (checkFile() == 1)
				continue;

			if (readFileData(getFileName) == 1)
				continue;

		}
		if (totalFile < 1) {
			comcr.hCallErrorDesc = "無檔案可處理 end";
			showLogMessage("I", "", "無檔案可處理!!");
		}
	}

	/**********************************************************************/
	public int readFileData(String fileName) throws Exception {
		String rec = "";
		fileName1 = comc.getECSHOME() + "/media/act/" + fileName;
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

		while (true) {
			rec = readTextFile(fi); // read file data
			if (endFile[fi].equals("Y"))
				break;

			if (rec.length() != 50) {
				showLogMessage("D", "", "Error : 此檔案 =  " + fileName + " , 資料長度 =  " + rec.length() + " 不可轉入");
				return 1;
			} else {
				totalFile++;
				moveData(processDataRecord(getFieldValue(rec, dt1Length), dT1));
			}
		}
		closeInputText(fi);
		renameFile(fileName);
		commitDataBase();
		return 0;
	}

	/**********************************************************************/
	void moveData(Map<String, Object> map) throws Exception {
		dateTime();

		String tmpStr;
		tmpPayAmt = 0.0;
		tmpserialNo++; // 流水號
		setValue("data_from", "B");
		setValue("file_no", tmpfileNo);
		setValueInt("serial_no", tmpserialNo);

		tmppayCardNo = (String) map.get("col1");
		setValue("pay_card_no", tmppayCardNo.trim());
		showLogMessage("I", "", "pay_card_no =[" + tmppayCardNo + "]");

		tmpStr = (String) map.get("col3");
		setValue("sign", tmpStr.trim());

		tmpStr = (String) map.get("col2");
		tmpPayAmt = commstring.ss2Num(tmpStr.trim().substring(0, tmpStr.length() - 2));
//		setValue("pay_amt", tmpStr.trim().substring(0, tmpStr.length() - 2));

		tmppaymentType2 = (String) map.get("col4");
		setValue("payment_type2", tmppaymentType2.trim());
		showLogMessage("I", "", "payment_type2 =[" + tmppaymentType2 + "]");

		selectPaymentType();
		setValue("payment_type", tmppaymentType.trim());
		setValueDouble("pay_amt", tmpPayAmt);
		
		tmpStr = (String) map.get("col5");
		// --民國年轉西元年
		tmpStr =  commDate.tw2adDate(tmpStr);
		setValue("pay_date", tmpStr.trim());

//    tmpStr = (String) map.get("col6");
//    setValue("pay_time", tmpStr.trim());

		tmpprocMark = (String) map.get("col6");
		setValue("proc_mark", tmpprocMark.trim());

		tmpStr = (String) map.get("col7");
		setValue("unite_mark", tmpStr.trim());

		tmpStr = (String) map.get("col8");
		setValue("def_branch", tmpStr.trim());

		tmpStr = (String) map.get("col9");
		setValue("pay_branch", tmpStr.trim());
		
		//--清空資料欄位
		tmpacctType = "";
		tmpidPSeqno = "";
		tmpcorpPSeqno = "";
		tmppSeqno = "";
		tmpacnoPSeqno = "";
		tmpisPass = "";
		
		selectBaseData();
		setValue("acct_type", tmpacctType.trim());
		setValue("id_p_seqno", tmpidPSeqno.trim());
		setValue("corp_p_seqno", tmpcorpPSeqno.trim());
		setValue("p_seqno", tmppSeqno.trim());
		setValue("acno_p_seqno", tmpacnoPSeqno.trim());
		setValue("is_pass", tmpisPass.trim());

		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("crt_user", "ecs");
		
		setValue("apr_flag", "Y");
		setValue("apr_date", sysDate);
		setValue("apr_user", "ecs");
		
		setValue("mod_user", "ActA200");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);

		insertActRepayCreditlimit();
		
		readCnt++;
		
		if(readCnt % 5000 ==0) {
			commitDataBase();
			showLogMessage("I", "", "讀檔寫入  act_repay_creditlimit 筆數 = ["+readCnt+"]");
		}
			
		
		return;
	}

	/**********************************************************************/
	String selectBaseData() throws Exception {

		sqlCmd = "select acct_type, id_p_seqno, corp_p_seqno, p_seqno, acno_p_seqno ";
		sqlCmd += "from crd_card ";
		sqlCmd += "where card_no = ? ";
		setString(1, tmppayCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			tmpacctType = getValue("acct_type");
			tmpidPSeqno = getValue("id_p_seqno");
			tmpcorpPSeqno = getValue("corp_p_seqno");
			tmppSeqno = getValue("p_seqno");
			tmpacnoPSeqno = getValue("acno_p_seqno");
		} else {
			sqlCmd = "select acct_type, id_p_seqno, corp_p_seqno, p_seqno, acno_p_seqno ";
			sqlCmd += "from act_acno ";
			sqlCmd += "where payment_no = ? ";
			setString(1, tmppayCardNo);
			int recordCnt2 = selectTable();
			if (recordCnt2 > 0) {
				tmpacctType = getValue("acct_type");
				tmpidPSeqno = getValue("id_p_seqno");
				tmpcorpPSeqno = getValue("corp_p_seqno");
				tmppSeqno = getValue("p_seqno");
				tmpacnoPSeqno = getValue("acno_p_seqno");
			} else {
				sqlCmd = "select acct_type, id_p_seqno, corp_p_seqno, p_seqno, acno_p_seqno ";
				sqlCmd += "from act_acno ";
				sqlCmd += "where payment_no_II = ? ";
				setString(1, tmppayCardNo);
				int recordCnt3 = selectTable();
				if (recordCnt3 > 0) {
					tmpacctType = getValue("acct_type");
					tmpidPSeqno = getValue("id_p_seqno");
					tmpcorpPSeqno = getValue("corp_p_seqno");
					tmppSeqno = getValue("p_seqno");
					tmpacnoPSeqno = getValue("acno_p_seqno");
				}
			}
		}
		if (!tmpacctType.equals("") && !tmppSeqno.equals("") && !tmpacnoPSeqno.equals("") && commstring.empty(tmpprocMark))
			return tmpisPass = "Y";
		else
			return tmpisPass = "N";
	}

	/**********************************************************************/
	String selectPaymentType() throws Exception {

		sqlCmd = "select id_code ";
		sqlCmd += "from ptr_sys_idtab ";
		sqlCmd += "where wf_type = 'PAYMENT_TYPE2' ";
		sqlCmd += "and wf_id = ? ";
		setString(1, tmppaymentType2);
		int recordCnt = selectTable();

		if (recordCnt > 0)
			tmppaymentType = getValue("id_code");
		else {
			sqlCmd = "select id_code ";
			sqlCmd += "from ptr_sys_idtab ";
			sqlCmd += "where wf_type = 'PAYMENT_TYPE2' ";
			sqlCmd += "and id_code2 = ? ";
			setString(1, tmppaymentType2);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				tmppaymentType = getValue("id_code");
				tmpPayAmt = tmpPayAmt*-1;
			}	else
				tmppaymentType = "";
		}
			
		return tmppaymentType;
	}

	/**********************************************************************/
	private Map processDataRecord(String[] row, String[] DT) throws Exception {
		Map<String, Object> map = new HashMap<>();
		int i = 0;
		int j = 0;
		for (String s : DT) {
			map.put(s.trim(), row[i]);
			i++;
		}
		return map;
	}

	/**********************************************************************/
	void insertActRepayCreditlimit() throws Exception {

		daoTable = "act_repay_creditlimit";
		;

		insertTable();

		if (dupRecord.equals("Y")) {
			String err1 = "insert_act_repay_creditlimit error[dupRecord]";
			String err2 = "";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}
		return;
	}

	/**********************************************************************/
	int checkFile() throws Exception {
		int tmpInt = 0;

		sqlCmd = "select count(*) as tmpInt ";
		sqlCmd += "from act_repay_creditlimit ";
		sqlCmd += "where file_no = ? ";
		sqlCmd += "and is_repay = 'N' ";
		sqlCmd += "and data_from='B' ";
		setString(1, tmpfileNo);

		int recordCnt = selectTable();
		if (recordCnt > 0) {
			tmpInt = getValueInt("tmpInt");
			if (tmpInt > 0) {
				showLogMessage("W", "", " >>>此批繳款還額檔已執行還額作業<<< ");
				return 1;
				// } else {
				// daoTable = "act_repay_creditlimit";
				// deleteTable();
			}
		}
		return 0;
	}

	/**********************************************************************/
//  void selectActRepayCreditlimit() throws Exception {
//
//    sqlCmd = "select pay_amt, acno_p_seqno ";
//    sqlCmd += "from act_repay_creditlimit ";
//    sqlCmd += "where proc_mark = '' ";
//    sqlCmd += "and is_pass = 'Y' ";
//    sqlCmd += "and is_repay = 'N' ";
//    sqlCmd += "and data_from= 'B' ";
//
//    int recordCnt = selectTable();
//    if (recordCnt > 0) {
//      actRepayPayAmt = getValueDouble("pay_amt");
//      actRepayAcnoPSeqno = getValue("acno_p_seqno");
//      updateCcaCardAcct();
//      updateActRepayCreditlimit();
//    }
//  }

	/**********************************************************************/
	void updateCcaCardAcct(int iType) throws Exception {
		// --iType = 1 批次 , iType = 2 Online;
		daoTable = "cca_card_acct";
		if (iType == 1)
			updateSQL = " pay_amt = pay_amt + ? , ";
		else if (iType == 2)
			updateSQL = " pay_amt = pay_amt - ? , ";
		
		updateSQL += " mod_time = sysdate , mod_user = 'ecs' , mod_pgm = 'ActA200' , mod_seqno = nvl(mod_seqno,0)+1 ";
		
		whereStr = " where acno_p_seqno = ? ";
		setDouble(1, actRepayPayAmt);
		setString(2, actRepayAcnoPSeqno);
		updateTable();
	}

	void updateCcaConsume(int iType) throws Exception {
		// --iType = 1 批次 , iType = 2 Online;
		daoTable = "cca_consume";
		if (iType == 1)
			updateSQL = " tot_unpaid_amt = tot_unpaid_amt + ? , ";
		else if (iType == 2)
			updateSQL = " tot_unpaid_amt = tot_unpaid_amt - ? , ";
		
		updateSQL += " mod_time = sysdate , mod_user = 'ecs' , mod_pgm = 'ActA200' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where card_acct_idx = ? ";
		setDouble(1, actRepayPayAmt);
		setInt(2, tmpCardAcctIdx);
		updateTable();
	}
	
	void updateCcaCardAcctCorp(int iType) throws Exception {
		// --iType = 1 批次 , iType = 2 Online;
		daoTable = "cca_card_acct";
		if (iType == 1)
			updateSQL = " pay_amt = pay_amt + ? , ";
		else if (iType == 2)
			updateSQL = " pay_amt = pay_amt - ? , ";
		updateSQL += " mod_time = sysdate , mod_user = 'ecs' , mod_pgm = 'ActA200' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where acno_p_seqno = ? ";
		setDouble(1, actRepayPayAmt);
		setString(2, actRepayCorpAcnoPSeqno);
		updateTable();
	}

	void updateCcaConsumeCorp(int iType) throws Exception {
		// --iType = 1 批次 , iType = 2 Online;
		daoTable = "cca_consume";
		if (iType == 1)
			updateSQL = " tot_unpaid_amt = tot_unpaid_amt + ? , ";
		else if (iType == 2)
			updateSQL = " tot_unpaid_amt = tot_unpaid_amt - ? , ";
		updateSQL += " mod_time = sysdate , mod_user = 'ecs' , mod_pgm = 'ActA200' , mod_seqno = nvl(mod_seqno,0)+1 ";
		whereStr = " where card_acct_idx = ? ";
		setDouble(1, actRepayPayAmt);
		setInt(2, tmpCorpCardAcctIdx);
		updateTable();
	}
	
	/**********************************************************************/
	void updateActRepayCreditlimit() throws Exception {

		daoTable = "act_repay_creditlimit";
		updateSQL = " is_repay = 'Y' ";
		whereStr = "where data_from = ? and file_no = ? and serial_no = ? ";
		setString(1, tmpDataFrom);
		setString(2, tmpFileNo);
		setString(3, tmpSerialNo);
		updateTable();
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

	void initData() {
		actRepayPayAmt = 0;
		tmpDataFrom = "";
		tmpFileNo = "";
		tmpSerialNo = "";
		actRepayAcnoPSeqno = "";
		tmpCardAcctIdx = 0;
		actRepayCorpPSeqno = "";
		actRepayCorpAcnoPSeqno = "";
		actRepayAcctType = "";
		tmpCorpCardAcctIdx = 0;
	}

	/**********************************************************************/
	void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/act/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/act/backup/" + removeFileName + "." + sysDate;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	// ************************************************************************
} // End of class FetchSample
