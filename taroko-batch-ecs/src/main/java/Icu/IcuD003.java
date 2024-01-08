/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
 *  109/05/28  V1.00.00    Pino      program initial                                  *
 *  109/06/17  V1.00.01    Pino      update_cca_card_acct_C                           *
 *  109/06/29  V1.00.02    Pino      update_cca_card_acct_C,update_cca_consume        *
 *  109/07/09  V1.00.03    Zuwei     rename to IcuD003                                *
 *  109/07/10  V1.00.04    shiyuqi   updated for project coding standard              *
 *  109/07/15  V1.00.05    Pino      cardNO14最後一碼改為1                                                                                  *
 *  109/07/15  V1.00.06    Pino      insert act_acno class_code預設F                   *
 *  109/07/16  V1.00.07    Pino      insert act_acno 預設                                                                              *
 *  109/07/16  V1.00.08    Pino      insertCcaConsume                                 *
 *  109/07/20  V1.00.09    Pino      selectActAcno                                    *
 *  109/07/21  V1.00.10    Pino      updateCrdCard、updateCcaCardBase                  *
 *  109/07/22  V1.00.11    Pino      updateCrdCard、updateCcaCardBase           		  *
 *  109/07/28  V1.00.12    Pino      updateCcaCardBaseA                                *
 *  109/08/27  V1.00.13    Wilson    line_of_credit_amt、line_of_credit_amt_cash       *
 *  109/09/07  V1.00.14    Alex      ICACTQNA 不須使用拿掉驗證邏輯                                                                   *	
 *  109/09/09  V1.00.15    Alex      remove rest auth_txlog_amt , auth_txlog_amt_cash *	
 *  109/09/16  V1.00.16    Alex      update cca_consume                               *
 *  109/09/21  V1.00.17    Alex      update act_acno.int_rate_mcode					  *
 *  109/09/25  V1.00.18    Alex      class_code 預設 C                                  *
 *  109/10/16  V1.00.19    Alex      update cca_consume column						  *
 *  109-10-19  V1.00.20    shiyuqi   updated for project coding standard              *
 *  109-11-11  V1.00.21    tanwei    updated for project coding standard              *
 *  109-12-17  V1.00.31    Alex      insert act_acno new_acct_flag ='Y'		          *
 *  110/10/05  V1.00.32    Wilson    mark and uf_nvl(p_seqno,'') = ''                 *  
 *  110/11/25  V1.00.33    Justin    ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX                *
 *  110/12/01  V1.00.34    Alex      106 5999 邏輯調整                                                                                         *
 *  110/12/02  V1.00.35    Alex      add acct_type not 01 && group_code not 1599 > N2 *
 *  111/02/14  V1.00.36    Alex      檔案按照檔案時間順序處理                                                                                  *  
 *  111/02/14  V1.00.37    Ryan      big5 to MS950                                           *
 *  111/02/17  V1.00.38    Alex      處理模式修正                                                                                                        *
 *  111/02/18  V1.00.39    Alex      delete 1 month ago data                          *
 *  111/02/18  V1.00.40    Alex      add 5000 commit and show Message                 *
 *  111/02/18  V1.00.41    Alex      add insert act_acno 5000 commit                  *
 *  111/02/19  V1.00.42    Alex      add update where acct_type ='01'                 *
 *  111/02/19  V1.00.43    Alex      for loop > openCursor                            *
 *  111/03/01  V1.00.44    Alex      tempSerialNo ++ 改到 insert 前增加                                                *
 *  111/03/03  V1.00.45    Alex      clearData 移至  CcaAuiClearDB 處理                                                *
 *  111/03/11  V1.00.46    Alex      106599 in icud015 file_no 轉換                                                    *
 *  111/06/17  V1.00.47    Alex      預借現金邏輯變更									  *
 **************************************************************************************/

package Icu;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;

import com.CommDate;
import com.CommFTP;

public class IcuD003 extends AccessDAO {
	private final String progname = "每天接收一般卡帳戶異動資料通知檔作業  111/06/17  V1.00.47";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate  commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	FscIcud03 fsIcud03 = new FscIcud03();
	FscIcud15 fsIcud15 = new FscIcud15();
 
	String modUser = "";
	String hBusiBusinessDate = "";
	String temstr = "";
	String tempId = "";
	int debug = 0;
	int fi;
	int totalFile = 0;
	String fileName1 = "", fileName2 = "";
	String yyyymmdd = "";
	String nn = "";
	int serialNo = 0;
	String cardNO14 = "";
	String cardCardNo = "";
	String cardAcctType = "";
	String cardIdPSeqno = "";
	String cardAcnoPSeqno = "";
	String cardPSeqno = "";
	String cardCorpPSeqno = "";
	String cardCorpNo = "";
	String cardGroupCode = "";
	String idnoIdNo = "";
	String errorFileName = "";
	double idnoCreditLimit = 0;
	
	double hCardAcctIdx = 0;
	double hAcctJrnlBal = 0;
	double hUnpayAmt = 0;
	double hAuthNotDeposit = 0;
	double hUnpayAmtCash = 0;
	int hIntRateMcode = 0;
	String hAcnoPSeqno = "";
	int tempSerialNo = 0;
	int fileCnt1 = 0;
	int filehHeadCnt = 0;
	int totalCnt = 0;
	int addAcnoCnt = 0;
	int readProcCnt = 0 ;
	int proc99Cnt = 0;
	int sumCnt = 0 ;
	
	protected final String dt1Str = "mod_acdr,issuer_code,card_type,card_seqno,status_cfq,line_of_credit_amt,unpay_amt,auth_not_deposit,"
			+ "acct_jrnl_bal,jrnl_bal_sign,unpay_amt_cash,open_date,status_cfq_date,locamt_cash_rate,locamt_cash_day";

	protected final int[] dt1Ength = { 1, 8, 2, 7, 1, 9, 11, 11, 11, 1, 11, 8, 8, 2, 2 };

	protected String[] dt1 = new String[] {};

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			if (args.length == 1) {
				hBusiBusinessDate = args[0];
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			modUser = comc.commGetUserID();
			if (hBusiBusinessDate.isEmpty())
				selectPtrBusinday();
//			selectFSCICUD03A();
//			selectFSCIcuD03D();
//			selectFSCIcuD03C();
//			commitDataBase();
			readFile();
			selectFSCICUD03A();
//			selectFSCIcuD03D();
			showLogMessage("I", "", "開始處理 Table 內資料");
			selectFSCIcuD03C();
			commitDataBase();
			showLogMessage("I", "", "開始處理帳務資料");
			procFileNo99();
			commitDataBase();
			procErrorReport();
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			procFTP();
			backupFile();
			//--清理資料
//			clearData();
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	
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
	void readFile() throws Exception {
		String yyyy = "";
		String hTempFilename = "";
		String tmpstr = String.format("%s/media/icu", comc.getECSHOME());

		tmpstr = Normalizer.normalize(tmpstr, java.text.Normalizer.Form.NFKD);

		List<String> listOfFiles = comc.listFsSort(tmpstr);

		if (debug == 1)
			showLogMessage("I", "", " 888 PATH=[" + tmpstr + "]" + listOfFiles.size());
		if (listOfFiles.size() == 0) {
			showLogMessage("D", "", "無檔案可處理  " + "");
			return ;
		}

		for (String file : listOfFiles) {			
			if (debug == 1)
				showLogMessage("I", "", " 888 NAME=[" + file + "]" + file.length());
			if (file.length() != 27)
				continue;
			if (!file.substring(0, 19).equals("M00600000.ICACTQND."))
				continue;
			yyyymmdd = String.valueOf(2011 + Integer.parseInt(file.substring(19, 21))) + file.substring(21, 25);
			nn = file.substring(25, 27);
			if(checkFSCIcuD03()==false) {
				yyyymmdd = "";
				nn = "";
				showLogMessage("D", "", "此帳戶異動資料通知檔已執行更新作業 ["+file+"]");
				continue;
			}
			if (yyyymmdd.equals(hBusiBusinessDate) ||  commDate.dateAdd(yyyymmdd, 0, 0, 1).equals(hBusiBusinessDate)) {
			} else {
				yyyymmdd = "";
				nn = "";
				continue;
			}
			showLogMessage("I", "", "正在處理檔案 : ["+file+"]");
			hTempFilename = file;
			if (getFileName(hTempFilename) == 1)
				continue;
			if (debug == 1)
				showLogMessage("I", "", " 888 read file=[" + totalFile + "]" + hTempFilename);
		}
		if (totalFile < 1) {
			showLogMessage("D", "", "無檔案可處理  " + "");
		}
		commitDataBase(); 
	}

	/**********************************************************************/
	public int getFileName(String fileNameI) throws Exception {
		String rec = "";
		fileName1 = fileNameI;
		fileName2 = comc.getECSHOME() + "/media/icu/" + fileName1;
		if (debug == 1)
			showLogMessage("I", "", "Process file=" + fileName2);

		int f = openInputText(fileName2);
		if (f == -1) {
			return 1;
		}
		closeInputText(f);

		setConsoleMode("N");
		fi = openInputText(fileName2, "MS950");
		setConsoleMode("Y");
		if (fi == -1) {
			return 1;
		}

		totalFile++;
		showLogMessage("I", "", "888 Cnt_file=[" + totalFile + "]");
		dt1 = dt1Str.split(",");
		while (true) {
			rec = readTextFile(fi);
			if (rec.trim().length() == 0) {
				commitDataBase();
				closeInputText(fi);
				renameFile(fileName1);
				return 0;
			}
			if (rec.trim().length() == 93) {
				if (rec.substring(1, 9).equals("00600000")) {
					totalCnt++;
					if (moveData(processDataRecord(getFieldValue(rec, dt1Ength), dt1)) == 1)
						processDisplay(1000);
				} else {
					comcr.errRtn("發卡單位代號不符", rec.substring(1, 9), "");
				}
			} else {
				showLogMessage("D", "", " Error : 此檔案 =  " + fileName1 + " 資料長度不正確，不可轉入");
				return 1;
			}

		}

	}

	/*************************************************************************/
	int moveData(Map<String, Object> map) throws Exception {
		fsIcud03.initData();
		serialNo++;
		fsIcud03.fileNo = yyyymmdd + nn;
		fsIcud03.serialNO = serialNo;
		fsIcud03.modAcdr = (String) map.get("mod_acdr");
		fsIcud03.cardType = (String) map.get("card_type");
		fsIcud03.cardSeqno = (String) map.get("card_seqno");
		cardNO14 = selectTCBBIN() + fsIcud03.cardSeqno + "1"; // V1.00.05 cardSeqno後面一碼由0改1
		selectCrdCard();
		fsIcud03.statusCfq = (String) map.get("status_cfq");
		fsIcud03.lineOfCreditAmt = Double.parseDouble((String) map.get("line_of_credit_amt"));
		fsIcud03.unpayAmt = Double.parseDouble((String) map.get("unpay_amt")) / 100;
		fsIcud03.authNotDeposit = Double.parseDouble((String) map.get("auth_not_deposit")) / 100;
		fsIcud03.acctJrnlBal = Double.parseDouble((String) map.get("acct_jrnl_bal")) / 100;
		fsIcud03.jrnlBalSign = (String) map.get("jrnl_bal_sign");
		fsIcud03.unpayAmtCash = Double.parseDouble((String) map.get("unpay_amt_cash")) / 100;
		fsIcud03.openDate = (String) map.get("open_date");
		fsIcud03.statusCfqDate = (String) map.get("status_cfq_date");
		fsIcud03.locamtCashRate = Double.parseDouble((String) map.get("locamt_cash_rate"));
		fsIcud03.locamtCashDay = (String) map.get("locamt_cash_day");
		fsIcud03.isAddCard = "";
		if (fsIcud03.modAcdr.equals("A") || fsIcud03.modAcdr.equals("C") || fsIcud03.modAcdr.equals("D")) {

		} else {
			fsIcud03.isPass = "N";
			fsIcud03.noPassReason = "N1";
		}
		if (fsIcud03.modAcdr.equals("D")) {
			if (fsIcud03.statusCfq.equals("C") || fsIcud03.statusCfq.equals("F") || fsIcud03.statusCfq.equals("Q")) {

			} else {
				fsIcud03.isPass = "N";
				fsIcud03.noPassReason = "N3";
			}
		}

		if (fsIcud03.modAcdr.equals("A") && !cardAcnoPSeqno.isEmpty()) {
			fsIcud03.isAddCard = "Y";
		}
		if (!fsIcud03.isPass.equals("N")) {
			fsIcud03.isPass = "Y";
		}
		// showLogMessage("D", "", " is_pass = " + Fsc_Icud03.is_pass + " ");
		fsIcud03.isProcess = "";
		fsIcud03.cardNo = cardCardNo;
		fsIcud03.acctType = cardAcctType;
		fsIcud03.idPSeqno = cardIdPSeqno;
		fsIcud03.idNo = idnoIdNo;
		fsIcud03.acnoPSeqno = cardAcnoPSeqno;
		fsIcud03.pSeqno = cardPSeqno;
		fsIcud03.idNoCrediLimit = idnoCreditLimit;
		fsIcud03.crtDate = sysDate;
		fsIcud03.crtTime = sysTime;
		fsIcud03.crtUser = "";
		fsIcud03.modUser = modUser;
		fsIcud03.modTime = sysDate + sysTime;
		fsIcud03.modPgm = "IcuD003";
		
		//--new for 106 599
		if("1599".equals(cardGroupCode) && "Y".equals(fsIcud03.isPass)) {
			fsIcud03.isProcess = "Y";
			fsIcud03.noPassReason = "99";			
			fsIcud15.initData();
			moveColumn03To15();
			insertFSCICUD15();
		}
		
		insertFSCICUD03();
		
		readProcCnt++;
		
		if(readProcCnt % 5000 ==0) {
			commitDataBase();
			showLogMessage("I", "", "讀檔寫入 Fsc_icud03 筆數 = ["+readProcCnt+"]");
		}
			
		
//		commitDataBase();						
		return 1;
	}

	/***********************************************************************/
	void insertFSCICUD03() throws Exception {
		daoTable = "fsc_icud03";
		extendField = daoTable + ".";
		setValue(extendField + "file_no", fsIcud03.fileNo);
		setValueInt(extendField + "serial_no", fsIcud03.serialNO);
		setValue(extendField + "mod_acdr", fsIcud03.modAcdr);
		setValue(extendField + "card_type", fsIcud03.cardType);
		setValue(extendField + "card_seqno", fsIcud03.cardSeqno);
		setValue(extendField + "status_cfq", fsIcud03.statusCfq);
		setValueDouble(extendField + "line_of_credit_amt", fsIcud03.lineOfCreditAmt);
		setValueDouble(extendField + "unpay_amt", fsIcud03.unpayAmt);
		setValueDouble(extendField + "auth_not_deposit", fsIcud03.authNotDeposit);
		setValueDouble(extendField + "acct_jrnl_bal", fsIcud03.acctJrnlBal);
		setValue(extendField + "jrnl_bal_sign", fsIcud03.jrnlBalSign);
		setValueDouble(extendField + "unpay_amt_cash", fsIcud03.unpayAmtCash);
		setValue(extendField + "open_date", fsIcud03.openDate);
		setValue(extendField + "status_cfq_date", fsIcud03.statusCfqDate);
		setValueDouble(extendField + "locamt_cash_rate", fsIcud03.locamtCashRate);
		setValue(extendField + "locamt_cash_day", fsIcud03.locamtCashDay);
		setValue(extendField + "is_add_card", fsIcud03.isAddCard);
		setValue(extendField + "is_pass", fsIcud03.isPass);
		setValue(extendField + "is_process", fsIcud03.isProcess);
		setValue(extendField + "card_no", fsIcud03.cardNo);
		setValue(extendField + "acct_type", fsIcud03.acctType);
		setValue(extendField + "id_p_seqno", fsIcud03.idPSeqno);
		setValue(extendField + "id_no", fsIcud03.idNo);
		setValue(extendField + "acno_p_seqno", fsIcud03.acnoPSeqno);
		setValue(extendField + "p_seqno", fsIcud03.pSeqno);
		setValueDouble(extendField + "idno_credit_limit", fsIcud03.idNoCrediLimit);
		setValue(extendField + "nopass_reason", fsIcud03.noPassReason);
		setValue(extendField + "crt_date", fsIcud03.crtDate);
		setValue(extendField + "crt_time", fsIcud03.crtTime);
		setValue(extendField + "crt_user", fsIcud03.crtUser);
		setValue(extendField + "mod_user", fsIcud03.modUser);
		setValue(extendField + "mod_time", fsIcud03.modTime);
		setValue(extendField + "mod_pgm", fsIcud03.modPgm);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_fsc_icud03 duplicate!", "", "");
		}

	}
	
	/***********************************************************************/
	void insertFSCICUD15() throws Exception {
		daoTable = "fsc_icud15";
		extendField = daoTable + ".";
		setValue(extendField + "file_no", fsIcud15.fileNo);
		setValueInt(extendField + "serial_no", fsIcud15.serialNO);
		setValue(extendField + "mod_acdr", fsIcud15.modAcdr);
		setValue(extendField + "card_type", fsIcud15.cardType);
		setValue(extendField + "card_seqno", fsIcud15.cardSeqno);
		setValue(extendField + "status_cfq", fsIcud15.statusCfq);
		setValueDouble(extendField + "line_of_credit_amt", fsIcud15.lineOfCreditAmt);
		setValueDouble(extendField + "unpay_amt", fsIcud15.unpayAmt);
		setValueDouble(extendField + "auth_not_deposit", fsIcud15.authNotDeposit);
		setValueDouble(extendField + "acct_jrnl_bal", fsIcud15.acctJrnlBal);
		setValue(extendField + "jrnl_bal_sign", fsIcud15.jrnlBalSign);
		setValueDouble(extendField + "unpay_amt_cash", fsIcud15.unpayAmtCash);
		setValue(extendField + "open_date", fsIcud15.openDate);
		setValue(extendField + "status_cfq_date", fsIcud15.statusCfqDate);
		setValueDouble(extendField + "locamt_cash_rate", fsIcud15.locamtCashRate);
		setValue(extendField + "locamt_cash_day", fsIcud15.locamtCashDay);
		setValue(extendField + "is_add_card", fsIcud15.isAddCard);
		setValue(extendField + "is_pass", fsIcud15.isPass);
		setValue(extendField + "is_process", fsIcud15.isProcess);
		setValue(extendField + "card_no", fsIcud15.cardNo);
		setValue(extendField + "acct_type", fsIcud15.acctType);
		setValue(extendField + "id_p_seqno", fsIcud15.idPSeqno);
		setValue(extendField + "id_no", fsIcud15.idNo);
		setValue(extendField + "acno_p_seqno", fsIcud15.acnoPSeqno);
		setValue(extendField + "p_seqno", fsIcud15.pSeqno);		
		setValue(extendField + "nopass_reason", fsIcud15.noPassReason);
		setValue(extendField + "corp_p_seqno", fsIcud15.corpPSeqno);
		setValue(extendField + "corp_no", fsIcud15.corpNo);
		setValue(extendField + "crt_date", fsIcud15.crtDate);
		setValue(extendField + "crt_time", fsIcud15.crtTime);
		setValue(extendField + "crt_user", fsIcud15.crtUser);
		setValue(extendField + "mod_user", fsIcud15.modUser);
		setValue(extendField + "mod_time", fsIcud15.modTime);
		setValue(extendField + "mod_pgm", fsIcud15.modPgm);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_fsc_icud15 duplicate!", "", "");
		}

	}
	
	/***********************************************************************/
	String selectTCBBIN() throws Exception {
		selectSQL = " TCB_BIN";
		daoTable = "FSC_BIN_GROUP";
		whereStr = "WHERE FISC_CODE = ? fetch first 1 rows only";
		setString(1, fsIcud03.cardType);
		int recCnt = selectTable();
		if (recCnt > 0) {
			return getValue("TCB_BIN");
		}
		if (recCnt == 0) {
			showLogMessage("I", "", " (TCB_BIN)不存在 BIN=" + fsIcud03.cardType + "");
			fsIcud03.isPass = "N";
			return "";
//			comcr.errRtn("(TCB_BIN)不存在 BIN="+fsIcud03.cardType, "", "");
		}
		return getValue("TCB_BIN");
	}

	/***********************************************************************/
	void selectCrdCard() throws Exception {
		cardCardNo = "";
		cardAcctType = "";
		cardIdPSeqno = "";
		cardAcnoPSeqno = "";
		cardPSeqno = "";
		idnoIdNo = "";
		cardCorpPSeqno = "";
		cardCorpNo = "";
		cardGroupCode = "";
		selectSQL = " a.card_no,a.acct_type,a.id_p_seqno,a.acno_p_seqno,a.p_seqno,a.corp_p_seqno,a.corp_no,a.group_code,b.id_no,b.idno_credit_limit";
		daoTable = "crd_card a ,crd_idno b";
		whereStr = "WHERE a.card_no like ? and b.id_p_seqno = a.id_p_seqno fetch first 1 rows only";
//		whereStr = "WHERE SUBSTR(a.card_no,1,14) = ? and b.id_p_seqno = a.id_p_seqno fetch first 1 rows only";
		setString(1, cardNO14+"%");
		int recCnt = selectTable();
		if (notFound.equals("Y")) {
			fsIcud03.isPass = "N";
			fsIcud03.noPassReason = "N2";
		}
		if (recCnt > 0) {
			cardCardNo = getValue("card_no");
			cardAcctType = getValue("acct_type");
			cardIdPSeqno = getValue("id_p_seqno");
			cardAcnoPSeqno = getValue("acno_p_seqno");
			cardPSeqno = getValue("p_seqno");
			cardCorpPSeqno = getValue("corp_p_seqno");
			cardCorpNo = getValue("corp_no");
			cardGroupCode = getValue("group_code");
			idnoIdNo = getValue("id_no");
			idnoCreditLimit = getValueDouble("idno_credit_limit");
		}
		if (cardAcctType.isEmpty() || cardIdPSeqno.isEmpty() || ("01".equals(cardAcctType)==false && "1599".equals(cardGroupCode)==false)) {
			fsIcud03.isPass = "N";
			fsIcud03.noPassReason = "N2";
		}
	}

	/*************************************************************************/
	void selectFSCICUD03A() throws Exception {
		sqlCmd = " select ";
		sqlCmd += " file_no,";
		sqlCmd += " serial_no,";
		sqlCmd += " mod_acdr,";
		sqlCmd += " card_type,";
		sqlCmd += " card_seqno,";
		sqlCmd += " status_cfq,";
		sqlCmd += " line_of_credit_amt,";
		sqlCmd += " unpay_amt,";
		sqlCmd += " auth_not_deposit,";
		sqlCmd += " acct_jrnl_bal,";
		sqlCmd += " jrnl_bal_sign,";
		sqlCmd += " unpay_amt_cash,";
		sqlCmd += " open_date,";
		sqlCmd += " status_cfq_date,";
		sqlCmd += " locamt_cash_rate,";
		sqlCmd += " locamt_cash_day,";
		sqlCmd += " is_add_card,";
		sqlCmd += " is_pass,";
		sqlCmd += " is_process,";
		sqlCmd += " card_no,";
		sqlCmd += " acct_type,";
		sqlCmd += " id_p_seqno,";
		sqlCmd += " id_no,";
		sqlCmd += " acno_p_seqno,";
		sqlCmd += " p_seqno,";
		sqlCmd += " crt_date,";
		sqlCmd += " crt_time,";
		sqlCmd += " crt_user,";
		sqlCmd += " mod_user,";
		sqlCmd += " mod_time,";
		sqlCmd += " mod_pgm,";
		sqlCmd += " idno_credit_limit,";
		sqlCmd += " rowid as rowid ";
		sqlCmd += " from FSC_ICUD03 ";
		sqlCmd += " where is_pass = 'Y' ";
		sqlCmd += " and is_process <> 'Y' ";
//		sqlCmd += " and uf_nvl(p_seqno,'') = '' ";
//		sqlCmd += " and mod_acdr = 'A' ";
//		sqlCmd += " and is_add_card <> 'Y' ";
		sqlCmd += " order by file_no,id_no ";

		openCursor();
		
		while(fetchTable()) {
			fsIcud03.initData();
			fsIcud03.fileNo = getValue("file_no");
			fsIcud03.serialNO = getValueInt("serial_no");
			fsIcud03.modAcdr = getValue("mod_acdr");
			fsIcud03.cardType = getValue("card_type");
			fsIcud03.cardSeqno = getValue("card_seqno");
			fsIcud03.statusCfq = getValue("status_cfq");
			fsIcud03.lineOfCreditAmt = getValueDouble("line_of_credit_amt");
			fsIcud03.unpayAmt = getValueDouble("unpay_amt");
			fsIcud03.authNotDeposit = getValueDouble("auth_not_deposit");
			fsIcud03.acctJrnlBal = getValueDouble("acct_jrnl_bal");
			fsIcud03.jrnlBalSign = getValue("jrnl_bal_sign");
			fsIcud03.unpayAmtCash = getValueDouble("unpay_amt_cash");
			fsIcud03.openDate = getValue("open_date");
			fsIcud03.statusCfqDate = getValue("status_cfq_date");
			fsIcud03.locamtCashRate = getValueDouble("locamt_cash_rate");
			fsIcud03.locamtCashDay = getValue("locamt_cash_day");
			fsIcud03.isAddCard = getValue("is_add_card");
			fsIcud03.isPass = getValue("is_pass");
			fsIcud03.isProcess = getValue("is_process");
			fsIcud03.cardNo = getValue("card_no");
			fsIcud03.acctType = getValue("acct_type");
			fsIcud03.idPSeqno = getValue("id_p_seqno");
			fsIcud03.idNo = getValue("id_no");
			fsIcud03.acnoPSeqno = getValue("acno_p_seqno");
			fsIcud03.pSeqno = getValue("p_seqno");
			fsIcud03.crtDate = getValue("crt_date");
			fsIcud03.crtTime = getValue("crt_time");
			fsIcud03.crtUser = getValue("crt_user");
			fsIcud03.modUser = getValue("mod_user");
			fsIcud03.modTime = getValue("mod_time");
			fsIcud03.modPgm = getValue("mod_pgm");
			fsIcud03.idNoCrediLimit = getValueDouble("idno_credit_limit");
			fsIcud03.rowid = getValue("rowid");
			
			if (selectActAcno() == 0) {

			} else {
				insertActAcno();
				insertCcaCardAcct();
				insertCcaConsume();
				// V1.00.02刪除 insert_act_acct();
			}

			updateCcaCardBaseA();
			updateFscIcuD03Pseqo();
			addAcnoCnt++;			
			if(addAcnoCnt%5000 == 0) {
				commitDataBase();
				showLogMessage("I", "", "新增帳戶資料處理筆數 = ["+addAcnoCnt+"]");
			}
		}
		closeCursor();
	}

	/*************************************************************************/
	void selectFSCIcuD03D() throws Exception {
		sqlCmd = " select ";
		sqlCmd += " file_no,";
		sqlCmd += " serial_no,";
		sqlCmd += " mod_acdr,";
		sqlCmd += " card_type,";
		sqlCmd += " card_seqno,";
		sqlCmd += " status_cfq,";
		sqlCmd += " line_of_credit_amt,";
		sqlCmd += " unpay_amt,";
		sqlCmd += " auth_not_deposit,";
		sqlCmd += " acct_jrnl_bal,";
		sqlCmd += " jrnl_bal_sign,";
		sqlCmd += " unpay_amt_cash,";
		sqlCmd += " open_date,";
		sqlCmd += " status_cfq_date,";
		sqlCmd += " locamt_cash_rate,";
		sqlCmd += " locamt_cash_day,";
		sqlCmd += " is_add_card,";
		sqlCmd += " is_pass,";
		sqlCmd += " is_process,";
		sqlCmd += " card_no,";
		sqlCmd += " acct_type,";
		sqlCmd += " id_p_seqno,";
		sqlCmd += " id_no,";
		sqlCmd += " acno_p_seqno,";
		sqlCmd += " p_seqno,";
		sqlCmd += " crt_date,";
		sqlCmd += " crt_time,";
		sqlCmd += " crt_user,";
		sqlCmd += " mod_user,";
		sqlCmd += " mod_time,";
		sqlCmd += " mod_pgm,";
		sqlCmd += " idno_credit_limit,";
		sqlCmd += " rowid as rowid ";
		sqlCmd += " from FSC_ICUD03 ";
		sqlCmd += " where is_pass = 'Y' ";
		sqlCmd += " and is_process <> 'Y' ";
		sqlCmd += " and mod_acdr = 'D' ";
		sqlCmd += " order by file_no,id_no ";

		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			fsIcud03.fileNo = getValue("file_no", i);
			fsIcud03.serialNO = getValueInt("serial_no", i);
			fsIcud03.modAcdr = getValue("mod_acdr", i);
			fsIcud03.cardType = getValue("card_type", i);
			fsIcud03.cardSeqno = getValue("card_seqno", i);
			fsIcud03.statusCfq = getValue("status_cfq", i);
			fsIcud03.lineOfCreditAmt = getValueDouble("line_of_credit_amt", i);
			fsIcud03.unpayAmt = getValueDouble("unpay_amt", i);
			fsIcud03.authNotDeposit = getValueDouble("auth_not_deposit", i);
			fsIcud03.acctJrnlBal = getValueDouble("acct_jrnl_bal", i);
			fsIcud03.jrnlBalSign = getValue("jrnl_bal_sign", i);
			fsIcud03.unpayAmtCash = getValueDouble("unpay_amt_cash", i);
			fsIcud03.openDate = getValue("open_date", i);
			fsIcud03.statusCfqDate = getValue("status_cfq_date", i);
			fsIcud03.locamtCashRate = getValueDouble("locamt_cash_rate", i);
			fsIcud03.locamtCashDay = getValue("locamt_cash_day", i);
			fsIcud03.isAddCard = getValue("is_add_card", i);
			fsIcud03.isPass = getValue("is_pass", i);
			fsIcud03.isProcess = getValue("is_process", i);
			fsIcud03.cardNo = getValue("card_no", i);
			fsIcud03.acctType = getValue("acct_type", i);
			fsIcud03.idPSeqno = getValue("id_p_seqno", i);
			fsIcud03.idNo = getValue("id_no", i);
			fsIcud03.acnoPSeqno = getValue("acno_p_seqno", i);
			fsIcud03.pSeqno = getValue("p_seqno", i);
			fsIcud03.crtDate = getValue("crt_date", i);
			fsIcud03.crtTime = getValue("crt_time", i);
			fsIcud03.crtUser = getValue("crt_user", i);
			fsIcud03.modUser = getValue("mod_user", i);
			fsIcud03.modTime = getValue("mod_time", i);
			fsIcud03.modPgm = getValue("mod_pgm", i);
			fsIcud03.idNoCrediLimit = getValueDouble("idno_credit_limit", i);
			fsIcud03.rowid = getValue("rowid", i);
			updatCcaCardAcctD();

		}
	}
	
	/*************************************************************************/
	
	void procFileNo99() throws Exception {
		
		sqlCmd = "select acno_p_seqno , ";
		sqlCmd += "sum(unpay_amt) as tl_unpay_amt , ";
		sqlCmd += "sum(auth_not_deposit) as tl_auth_not_deposit , ";
		sqlCmd += "sum(decode(jrnl_bal_sign,'D',acct_jrnl_bal,'C',acct_jrnl_bal*-1,acct_jrnl_bal)) as tl_acct_jrnl_bal , ";
		sqlCmd += "sum(unpay_amt_cash) as tl_unpay_amt_cash ";
		sqlCmd += "from fsc_icud03 ";
		sqlCmd += "where file_no = '9999999999' group by acno_p_seqno";
		
		openCursor();
		
		while(fetchTable()) {
			initData99();
			hAcnoPSeqno = getValue("acno_p_seqno");
			hAcctJrnlBal = getValueDouble("tl_acct_jrnl_bal");
			hUnpayAmt = getValueDouble("tl_unpay_amt");
			hAuthNotDeposit = getValueDouble("tl_auth_not_deposit");
			hUnpayAmtCash = getValueDouble("tl_unpay_amt_cash");
			if (hUnpayAmt > 0) 
				hIntRateMcode = 1 ;
			else
				hIntRateMcode = 0 ;
								
			updateIntRateMcode();
			updateCcaCardAcct99();
			updateCcaConsume99();
			sumCnt++;
			
			if(sumCnt % 5000 ==0) {
				commitDataBase();
				showLogMessage("I", "", "帳務處理筆數 = ["+sumCnt+"]");
			}
			
		}
		closeCursor();
	}
	
	/*************************************************************************/
	void updateCcaCardAcct99() throws Exception {
		daoTable = "cca_card_acct";
		updateSQL = " unpay_amt = ?,";
		updateSQL += " tot_amt_consume = ?,";
		updateSQL += " jrnl_bal = ?,"; // V1.00.02
		updateSQL += " total_cash_utilized = ?,"; // V1.00.02
		updateSQL += " pay_amt = '0',"; // V1.00.01
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = " where p_seqno = ? ";
		setDouble(1, hUnpayAmt);
		setDouble(2, hAuthNotDeposit);
		setDouble(3, hAcctJrnlBal);
		setDouble(4, hUnpayAmtCash);
		setString(5, modUser);
		setString(6, "IcuD003");
		setString(7, hAcnoPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("updateCcaCardAcct99 not found!", "p_seqno:" + hAcnoPSeqno, "");
		}
	}
	/*************************************************************************/
	void updateCcaConsume99() throws Exception {
		daoTable = "cca_consume";
		updateSQL = " tot_amt_consume = ?,";
		updateSQL += " auth_txlog_amt_2  = 0, ";
		updateSQL += " auth_txlog_amt_cash_2  = 0, ";
		updateSQL += " unpaid_precash = ? ,";
		updateSQL += " tot_unpaid_amt = 0 , ";
		if(hAcctJrnlBal<0) {
			updateSQL += " unpaid_consume_fee = 0 ,";
			updateSQL += " paid_consume_fee =0 , ";
			updateSQL += " pre_pay_amt = ? ,";		
		}	else	{			
			updateSQL += " unpaid_consume_fee = ? ,";
			updateSQL += " paid_consume_fee =0 ,";	
			updateSQL += " pre_pay_amt =0 ,";
		}
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = " where p_seqno = ? ";
		setDouble(1, hAuthNotDeposit); // Fsc_Icud03.auth_not_deposit累計
		setDouble(2, hUnpayAmtCash);
		if(hAcctJrnlBal<0) {
			setDouble(3, hAcctJrnlBal*-1);
		}	else	{
			setDouble(3, hAcctJrnlBal);
		}		
		setString(4, modUser);
		setString(5, "IcuD003");
		setString(6, hAcnoPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_cca_consume not found!", "p_seqno:" + hAcnoPSeqno, "");
		}
	}
	
	/*************************************************************************/
	void updateIntRateMcode() throws Exception {
		daoTable = "act_acno";
		updateSQL = "int_rate_mcode = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = "where acno_p_seqno = ? ";
		setInt(1, hIntRateMcode);
		setString(2, modUser);
		setString(3, "IcuD003");
		setString(4, hAcnoPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_acno not found!", "acno_p_seqno:" + hAcnoPSeqno, "");
		}
	}
	
	/*************************************************************************/
	void getCardAcctIdx() throws Exception {		
		sqlCmd = "select card_acct_idx from cca_card_acct where acno_p_seqno = ? ";
		setString(1,hAcnoPSeqno);
		int cnt = selectTable();
		
		if(cnt > 0) {
			hCardAcctIdx = getValueDouble("card_acct_idx");
		}	else	{
			comcr.errRtn("select card_acct_idx not found , acno_p_seqno = ["+hAcnoPSeqno+"]", "", "");
		}				
	}
	
	/*************************************************************************/
	void selectFSCIcuD03C() throws Exception {
		sqlCmd = " select ";
		sqlCmd += " file_no,";
		sqlCmd += " serial_no,";
		sqlCmd += " mod_acdr,";
		sqlCmd += " card_type,";
		sqlCmd += " card_seqno,";
		sqlCmd += " status_cfq,";
		sqlCmd += " line_of_credit_amt,";
		sqlCmd += " unpay_amt,";
		sqlCmd += " auth_not_deposit,";
		sqlCmd += " acct_jrnl_bal,";
		sqlCmd += " jrnl_bal_sign,";
		sqlCmd += " unpay_amt_cash,";
		sqlCmd += " open_date,";
		sqlCmd += " status_cfq_date,";
		sqlCmd += " locamt_cash_rate,";
		sqlCmd += " locamt_cash_day,";
		sqlCmd += " is_add_card,";
		sqlCmd += " is_pass,";
		sqlCmd += " is_process,";
		sqlCmd += " card_no,";
		sqlCmd += " acct_type,";
		sqlCmd += " id_p_seqno,";
		sqlCmd += " id_no,";
		sqlCmd += " acno_p_seqno,";
		sqlCmd += " p_seqno,";
		sqlCmd += " crt_date,";
		sqlCmd += " crt_time,";
		sqlCmd += " crt_user,";
		sqlCmd += " mod_user,";
		sqlCmd += " mod_time,";
		sqlCmd += " mod_pgm,";
		sqlCmd += " idno_credit_limit,";
		sqlCmd += " rowid as rowid ";
		sqlCmd += " from FSC_ICUD03 ";
		sqlCmd += " where is_pass = 'Y' ";
		sqlCmd += " and is_process <> 'Y' ";
		sqlCmd += " and mod_acdr in ('A','D','C') ";
		sqlCmd += " order by file_no,id_no ";
		
		openCursor();
		
		while(fetchTable()) {
			fsIcud03.initData();
			fsIcud03.fileNo = getValue("file_no");
			fsIcud03.serialNO = getValueInt("serial_no");
			fsIcud03.modAcdr = getValue("mod_acdr");
			fsIcud03.cardType = getValue("card_type");
			fsIcud03.cardSeqno = getValue("card_seqno");
			fsIcud03.statusCfq = getValue("status_cfq");
			fsIcud03.lineOfCreditAmt = getValueDouble("line_of_credit_amt");
			fsIcud03.unpayAmt = getValueDouble("unpay_amt");
			fsIcud03.authNotDeposit = getValueDouble("auth_not_deposit");
			fsIcud03.acctJrnlBal = getValueDouble("acct_jrnl_bal");
			fsIcud03.jrnlBalSign = getValue("jrnl_bal_sign");
			fsIcud03.unpayAmtCash = getValueDouble("unpay_amt_cash");
			fsIcud03.openDate = getValue("open_date");
			fsIcud03.statusCfqDate = getValue("status_cfq_date");
			fsIcud03.locamtCashRate = getValueDouble("locamt_cash_rate");
			fsIcud03.locamtCashDay = getValue("locamt_cash_day");
			fsIcud03.isAddCard = getValue("is_add_card");
			fsIcud03.isPass = getValue("is_pass");
			fsIcud03.isProcess = getValue("is_process");
			fsIcud03.cardNo = getValue("card_no");
			fsIcud03.acctType = getValue("acct_type");
			fsIcud03.idPSeqno = getValue("id_p_seqno");
			fsIcud03.idNo = getValue("id_no");
			fsIcud03.acnoPSeqno = getValue("acno_p_seqno");
			fsIcud03.pSeqno = getValue("p_seqno");
			fsIcud03.crtDate = getValue("crt_date");
			fsIcud03.crtTime = getValue("crt_time");
			fsIcud03.crtUser = getValue("crt_user");
			fsIcud03.modUser = getValue("mod_user");
			fsIcud03.modTime = getValue("mod_time");
			fsIcud03.modPgm = getValue("mod_pgm");
			fsIcud03.idNoCrediLimit = getValueDouble("idno_credit_limit");
			fsIcud03.rowid = getValue("rowid");
			
			if (fsIcud03.modAcdr.equals("A") || fsIcud03.modAcdr.equals("C")) {
				updateCrdCard();
				updateCcaCardBase();
			}
			
			//--取號
			if(tempSerialNo == 0)
				tempSerialNo = getMaxSerialNo();
//			tempSerialNo ++ ;
			//--proc file_no 9999999999
			procFileNoMaster();			
			updateFscIcud03();		
			proc99Cnt++;
			if(proc99Cnt % 5000 ==0) {
				commitDataBase();
				showLogMessage("I", "", "Fsc_icud03 資料已處理筆數 = ["+proc99Cnt+"]");
			}
		}
		closeCursor();
	}
	
	/*************************************************************************/
	void procFileNoMaster() throws Exception {
		
		//--先去讀取 file_no 為 9999999999 且 card_no = 卡號前13碼資料 有就 update 沒有就新增
		sqlCmd = "select count(*) as db_99 from fsc_icud03 where file_no = '9999999999' and card_no = ? ";
		setString(1,fsIcud03.cardNo.substring(0,13));
		selectTable();		
		int tempCnt = getValueInt("db_99");		
		if(tempCnt <=0)
			insertFileNo99();
		else
			updateFileNo99();				
	}
	
	/*************************************************************************/
	
	void updateFileNo99() throws Exception {
		daoTable = "fsc_icud03";
		updateSQL = "mod_acdr = ? ,";
		updateSQL += "status_cfq = ? ,";
		updateSQL += "line_of_credit_amt = ? ,";
		updateSQL += "unpay_amt = ? ,";
		updateSQL += "auth_not_deposit = ? ,";
		updateSQL += "acct_jrnl_bal = ? ,";
		updateSQL += "jrnl_bal_sign = ? ,";		
		updateSQL += "status_cfq_date = ? ,";	
		updateSQL += "locamt_cash_rate = ? ,";
		updateSQL += "idno_credit_limit = ? ,";
		updateSQL += "mod_user = ? ,";
		updateSQL += "mod_time = sysdate ,";
		updateSQL += "mod_pgm = ? ";
		
		if("88".equals(fsIcud03.locamtCashDay)) {
			updateSQL += ", locamt_cash_day = ? ,";		
			updateSQL += "unpay_amt_cash = ? ";			
		}	else if("00".equals(fsIcud03.locamtCashDay)) {
			updateSQL += ", locamt_cash_day = ? ,";		
			updateSQL += "unpay_amt_cash = 0 ";			
		}	else	{
			updateSQL += ", locamt_cash_day = ? ";		
		}
						
		whereStr = " where file_no = '9999999999' and card_no = ? ";
		
		setString(1,fsIcud03.modAcdr);
		setString(2,fsIcud03.statusCfq);
		setDouble(3,fsIcud03.lineOfCreditAmt);
		setDouble(4,fsIcud03.unpayAmt);
		setDouble(5,fsIcud03.authNotDeposit);
		setDouble(6,fsIcud03.acctJrnlBal);
		setString(7,fsIcud03.jrnlBalSign);
		setString(8,fsIcud03.statusCfqDate);
		setDouble(9,fsIcud03.locamtCashRate);
		setDouble(10,fsIcud03.idNoCrediLimit);
		setString(11,"batch");
		setString(12,fsIcud03.modPgm);
		if("88".equals(fsIcud03.locamtCashDay)) {
			setString(13,fsIcud03.locamtCashDay);
			setDouble(14,fsIcud03.unpayAmtCash);
			setString(15,fsIcud03.cardNo.substring(0,13));
		}	else	{
			setString(13,fsIcud03.locamtCashDay);
			setString(14,fsIcud03.cardNo.substring(0,13));
		}												
		
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_fsc_icud03_9999999999 not found!", "card_no13:" + fsIcud03.cardNo.substring(0,13), "");
		}
		
	}
	
	/*************************************************************************/
	
	void insertFileNo99() throws Exception {		
		tempSerialNo ++ ;
		daoTable = "fsc_icud03";
		extendField = daoTable + ".";
		setValue(extendField + "file_no", "9999999999");
		setValueInt(extendField + "serial_no", tempSerialNo);
		setValue(extendField + "mod_acdr", fsIcud03.modAcdr);
		setValue(extendField + "card_type", fsIcud03.cardType);
		setValue(extendField + "card_seqno", fsIcud03.cardSeqno);
		setValue(extendField + "status_cfq", fsIcud03.statusCfq);
		setValueDouble(extendField + "line_of_credit_amt", fsIcud03.lineOfCreditAmt);
		setValueDouble(extendField + "unpay_amt", fsIcud03.unpayAmt);
		setValueDouble(extendField + "auth_not_deposit", fsIcud03.authNotDeposit);
		setValueDouble(extendField + "acct_jrnl_bal", fsIcud03.acctJrnlBal);
		setValue(extendField + "jrnl_bal_sign", fsIcud03.jrnlBalSign);		
		setValue(extendField + "open_date", fsIcud03.openDate);
		setValue(extendField + "status_cfq_date", fsIcud03.statusCfqDate);
		setValueDouble(extendField + "locamt_cash_rate", fsIcud03.locamtCashRate);		
		setValue(extendField + "locamt_cash_day", fsIcud03.locamtCashDay);
		if("88".equals(fsIcud03.locamtCashDay)) {
			setValueDouble(extendField + "unpay_amt_cash", fsIcud03.unpayAmtCash);
		}	else if("00".equals(fsIcud03.locamtCashDay)) {
			setValueDouble(extendField + "unpay_amt_cash", 0);
		}	else	{
			setValueDouble(extendField + "unpay_amt_cash", 0);
		}		
		setValue(extendField + "is_add_card", fsIcud03.isAddCard);
		setValue(extendField + "is_pass", "Y");
		setValue(extendField + "is_process", "Y");
		setValue(extendField + "card_no", fsIcud03.cardNo.substring(0,13));
		setValue(extendField + "acct_type", fsIcud03.acctType);
		setValue(extendField + "id_p_seqno", fsIcud03.idPSeqno);
		setValue(extendField + "id_no", fsIcud03.idNo);
		setValue(extendField + "acno_p_seqno", fsIcud03.acnoPSeqno);
		setValue(extendField + "p_seqno", fsIcud03.pSeqno);
		setValueDouble(extendField + "idno_credit_limit", fsIcud03.idNoCrediLimit);		
		setValue(extendField + "crt_date", sysDate);
		setValue(extendField + "crt_time", sysTime);
		setValue(extendField + "crt_user", fsIcud03.crtUser);
		setValue(extendField + "mod_user", fsIcud03.modUser);
		setValue(extendField + "mod_time", sysDate + sysTime);
		setValue(extendField + "mod_pgm", fsIcud03.modPgm);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_fsc_icud03_99 duplicate!", "", "");
		}
	}
	
	/*************************************************************************/
	int getMaxSerialNo() throws Exception {
		int maxSeqNo = 0;
		sqlCmd = "select max(serial_no) as max_serial_no from fsc_icud03 where file_no = '9999999999' ";
		selectTable();		
		maxSeqNo = getValueInt("max_serial_no");
		this.showLogMessage("I", "", "seq = ["+maxSeqNo+"]");
		return maxSeqNo;
	}
	
	/*************************************************************************/
	int selectActAcno() throws Exception {
		sqlCmd = " select acno_p_seqno,p_seqno";
		sqlCmd += " from act_acno ";
		sqlCmd += " where acct_type = '01' ";
		sqlCmd += " and acct_key = ? ";
		setString(1, fsIcud03.idNo + "0");
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			fsIcud03.acnoPSeqno = getACNOPSEQNO();
			fsIcud03.pSeqno = fsIcud03.acnoPSeqno;
			// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//			hCardAcctIdx = getCardAcctIdx();
			hCardAcctIdx = Double.parseDouble(fsIcud03.acnoPSeqno); 
			return 1;
		} else {
			fsIcud03.acnoPSeqno = getValue("acno_p_seqno");
			fsIcud03.pSeqno = getValue("p_seqno");
			selectCcaCardAcct();
			return 0;
		}

	}

	/*************************************************************************/
	void selectCcaCardAcct() throws Exception {
		sqlCmd = " select card_acct_idx";
		sqlCmd += " from cca_card_acct ";
		sqlCmd += " where acct_type = '01' ";
		sqlCmd += " and acno_p_seqno = ? ";
		setString(1, fsIcud03.acnoPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("selectCcaCardAcct not found!", "acno_p_seqno:" + fsIcud03.acnoPSeqno, "");
		} else {
			hCardAcctIdx = getValueDouble("card_acct_idx");
		}

	}

	/***********************************************************************/
	void insertActAcno() throws Exception {
		daoTable = "act_acno";
		extendField = daoTable + ".";
		setValue(extendField + "acno_p_seqno", fsIcud03.acnoPSeqno);
		setValue(extendField + "p_seqno", fsIcud03.pSeqno);
		setValue(extendField + "acct_type", fsIcud03.acctType);
		setValue(extendField + "acct_key", fsIcud03.idNo + "0");
		setValue(extendField + "id_p_seqno", fsIcud03.idPSeqno);
		setValue(extendField + "class_code", "C"); // 預設C
		setValue(extendField + "stmt_cycle", "01"); // 預設01
		setValue(extendField + "card_indicator", "1"); // 預設1
		setValue(extendField + "acno_flag", "1"); // 預設1
		setValue(extendField + "new_acct_flag", "Y"); // 預設1
		setValueDouble(extendField + "line_of_credit_amt", Math.floor(fsIcud03.idNoCrediLimit));		
		if(fsIcud03.locamtCashRate == 0) {
			setValueDouble(extendField + "line_of_credit_amt_cash",Math.floor(fsIcud03.idNoCrediLimit));
		}	else if(fsIcud03.locamtCashRate == 1) {
			setValueDouble(extendField + "line_of_credit_amt_cash",0);
		}	else	{
			setValueDouble(extendField + "line_of_credit_amt_cash",Math.floor(fsIcud03.idNoCrediLimit * fsIcud03.locamtCashRate / 100));
		}				
		setValue(extendField + "acno_flag", "1");
		setValue(extendField + "crt_date", sysDate);
		setValue(extendField + "crt_time", sysTime);
		setValue(extendField + "crt_user", "IcuD003");
		setValue(extendField + "MOD_USER", fsIcud03.modUser);
		setValue(extendField + "MOD_TIME", sysDate + sysTime);
		setValue(extendField + "MOD_PGM", fsIcud03.modPgm);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_acno duplicate!", "", "acno_p_seqno:" + fsIcud03.acnoPSeqno + "acct_type:"
					+ fsIcud03.acctType + "acct_key:" + fsIcud03.idNo + "0");
		}
	}

	/***********************************************************************/
	void updateFscIcuD03Pseqo() throws Exception {
		daoTable = "fsc_icud03";
		updateSQL = "acno_p_seqno = ?,";
		updateSQL += " p_seqno = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = "where rowid = ? ";
		setString(1, fsIcud03.acnoPSeqno);
		setString(2, fsIcud03.pSeqno);
		setString(3, fsIcud03.modUser);
		setString(4, fsIcud03.modPgm);
		setRowId(5, fsIcud03.rowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_fsc_icud03Pseqo not found!", "rowid:" + fsIcud03.rowid, "");
		}
	}

	/***********************************************************************/
	// void insert_act_acct() throws Exception {
	// setValue("p_seqno", Fsc_Icud03.p_seqno);
	// setValue("acct_type", Fsc_Icud03.acct_type);
	// setValue("id_p_seqno", Fsc_Icud03.id_p_seqno);
	// setValue("MOD_USER", Fsc_Icud03.mod_user);
	// setValue("MOD_TIME", sysDate + sysTime);
	// setValue("MOD_PGM", Fsc_Icud03.mod_pgm);
	// daoTable = "act_acct";
	// insertTable();
	// if (dupRecord.equals("Y")) {
	// comcr.err_rtn("insert_act_acct duplicate!", "", "");
	// }
	// }
	/***********************************************************************/
	void insertCcaCardAcct() throws Exception {
		daoTable = "cca_card_acct";
		extendField = daoTable + ".";
		setValueDouble(extendField + "card_acct_idx", hCardAcctIdx);
		setValue(extendField + "acno_p_seqno", fsIcud03.acnoPSeqno);
		setValue(extendField + "p_seqno", fsIcud03.acnoPSeqno);
		setValue(extendField + "id_p_seqno", fsIcud03.idPSeqno);
		setValue(extendField + "acct_type", fsIcud03.acctType);
		setValue(extendField + "acno_flag", "1"); // 1.一般 2.總繳公司 3.商務個繳 4.總繳個人
		setValue(extendField + "debit_flag", "N");
		setValue(extendField + "crt_date", sysDate);
		setValue(extendField + "crt_user", "IcuD003");
		setValue(extendField + "MOD_USER", fsIcud03.modUser);
		setValue(extendField + "MOD_TIME", sysDate + sysTime);
		setValue(extendField + "MOD_PGM", fsIcud03.modPgm);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_cca_card_acct duplicate!", "", "acno_p_seqno:" + fsIcud03.acnoPSeqno);
		}
	}

	/***********************************************************************/
	void updateCrdCard() throws Exception {
		daoTable = "crd_card";
		updateSQL = "acno_p_seqno = ?,";
		updateSQL += " p_seqno = ?,";
		updateSQL += " acno_flag = '1',";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = "where acct_type = '01' and major_id_p_seqno = ? "; // V1.00.11
//    whereStr = "where SUBSTR(card_no,1,13) = ? "; //V1.00.10
		setString(1, fsIcud03.acnoPSeqno);
		setString(2, fsIcud03.pSeqno);
		setString(3, fsIcud03.modUser);
		setString(4, fsIcud03.modPgm);
		setString(5, fsIcud03.idPSeqno); // V1.00.11
//    setString(5, fsIcud03.cardNo.substring(0, 13)); //V1.00.10
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_crd_card not found!", "fsIcud03.idPSeqno:" + fsIcud03.idPSeqno, "");
		}
	}

	/***********************************************************************/
	void updateCcaCardBaseA() throws Exception {
		daoTable = "cca_card_base";
		updateSQL = "card_acct_idx = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = "where card_indicator = '1' and major_id_p_seqno = ? and acct_type ='01' "; // V1.00.12
		setDouble(1, hCardAcctIdx);
		setString(2, fsIcud03.modUser);
		setString(3, fsIcud03.modPgm);
		setString(4, fsIcud03.idPSeqno); // V1.00.12
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_cca_card_base not found!(1)", "fsIcud03.idPSeqno:" + fsIcud03.idPSeqno, "");
		}
	}

	/***********************************************************************/
	void insertCcaConsume() throws Exception {
		daoTable = "cca_consume";
		extendField = daoTable + ".";
		setValueDouble(extendField + "card_acct_idx", hCardAcctIdx);
		setValue(extendField + "p_seqno", fsIcud03.acnoPSeqno);
		setValue(extendField + "MOD_USER", fsIcud03.modUser);
		setValue(extendField + "MOD_TIME", sysDate + sysTime);
		setValue(extendField + "MOD_PGM", fsIcud03.modPgm);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_cca_consume duplicate!", "",
					"card_acct_idx:" + hCardAcctIdx + "p_seqno:" + fsIcud03.acnoPSeqno);
		}
	}

	/***********************************************************************/
	void updateCcaCardBase() throws Exception {
		daoTable = "cca_card_base";
		updateSQL = "acno_p_seqno = ?,";
		updateSQL += " p_seqno = ?,";
		updateSQL += " acno_flag = '1',";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = "where card_indicator = '1' and major_id_p_seqno = ? and acct_type ='01' "; 
		setString(1, fsIcud03.acnoPSeqno);
		setString(2, fsIcud03.pSeqno);
		setString(3, fsIcud03.modUser);
		setString(4, fsIcud03.modPgm);
		setString(5, fsIcud03.idPSeqno); // V1.00.11
//    setString(5, fsIcud03.cardNo.substring(0, 13)); //V1.00.10
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_cca_card_base not found!(2)", "fsIcud03.idPSeqno:" + fsIcud03.idPSeqno, "");
		}
	}

	/***********************************************************************/
	void updatActAcno() throws Exception {
		daoTable = "act_acno";
		updateSQL = "int_rate_mcode = ?,";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = "where acno_p_seqno = ? ";
		setInt(1, hIntRateMcode);
		setString(2, fsIcud03.modUser);
		setString(3, fsIcud03.modPgm);
		setString(4, fsIcud03.acnoPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_acno not found!", "acno_p_seqno:" + fsIcud03.acnoPSeqno, "");
		}
	}

	/***********************************************************************/
	void updatCcaCardAcctD() throws Exception {
		daoTable = "cca_card_acct";
		updateSQL = "block_date = ?,";
		// (C-信用不良銷戶)(F-欺詐行為銷戶)(Q-欺詐行為銷戶)
		updateSQL += " block_reason1 = decode(?,'C','14','F','26','Q','T1'),";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = " where acno_p_seqno = ? ";
		setString(1, hBusiBusinessDate);
		setString(2, fsIcud03.statusCfq);
		setString(3, fsIcud03.modUser);
		setString(4, fsIcud03.modPgm);
		setString(5, fsIcud03.acnoPSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_cca_card_acct_D not found!", "acno_p_seqno:" + fsIcud03.acnoPSeqno, "");
		}
	}

	/***********************************************************************/
	void updateCcaCardAcctC() throws Exception {
		daoTable = "cca_card_acct";
		updateSQL = " unpay_amt = ?,";
		updateSQL += " tot_amt_consume = ?,";
		updateSQL += " jrnl_bal = ?,"; // V1.00.02
		updateSQL += " total_cash_utilized = ?,"; // V1.00.02
		updateSQL += " pay_amt = '0',"; // V1.00.01
//		updateSQL += " auth_txlog_amt = '0',"; // V1.00.02
//		updateSQL += " auth_txlog_amt_cash = '0',"; // V1.00.02
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = " where p_seqno = ? ";
		// showLogMessage("D", "", "p_seqno = " + Fsc_Icud03.p_seqno + "");
		setDouble(1, hUnpayAmt);
		setDouble(2, hAuthNotDeposit);
		setDouble(3, hAcctJrnlBal);
		setDouble(4, hUnpayAmtCash);
		setString(5, fsIcud03.modUser);
		setString(6, fsIcud03.modPgm);
		setString(7, fsIcud03.pSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_cca_card_acct_C not found!", "p_seqno:" + fsIcud03.pSeqno, "");
		}
	}

	/***********************************************************************/
	void updateFscIcud03() throws Exception {
		daoTable = "fsc_icud03";
		updateSQL = " is_process = 'Y',";
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = " where rowid = ? ";
		setString(1, fsIcud03.modUser);
		setString(2, fsIcud03.modPgm);
		setRowId(3, fsIcud03.rowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_fsc_icud03 not found!", "rowid:" + fsIcud03.rowid, "");
		}
	}

	/***********************************************************************/
	// void update_act_acct() throws Exception {
	// daoTable = "act_acct";
	// updateSQL = " acct_jrnl_bal = ?,";
	// updateSQL += " mod_time = sysdate,";
	// updateSQL += " mod_user = ?,";
	// updateSQL += " mod_pgm = ?";
	// whereStr = " where p_seqno = ? ";
	// setDouble(1, h_acct_jrnl_bal); //Fsc_Icud03.acct_jrnl_bal累計
	// setString(2, Fsc_Icud03.mod_user);
	// setString(3, Fsc_Icud03.mod_pgm);
	// setString(4, Fsc_Icud03.p_seqno);
	// updateTable();
	// if (notFound.equals("Y")) {
	// comcr.err_rtn("update_act_acct not found!", "", "");
	// }
	// }
	/***********************************************************************/
	void updateCcaConsume() throws Exception {
		daoTable = "cca_consume";
		updateSQL = " tot_amt_consume = ?,";
		updateSQL += " auth_txlog_amt_2  = 0, ";
		updateSQL += " auth_txlog_amt_cash_2  = 0, ";
		updateSQL += " unpaid_precash = ? ,";
		updateSQL += " tot_unpaid_amt = 0 , ";
		if(hAcctJrnlBal<0) {
			updateSQL += " unpaid_consume_fee = 0 ,";
			updateSQL += " paid_consume_fee =0 , ";
			updateSQL += " pre_pay_amt = ? ,";		
		}	else	{			
			updateSQL += " unpaid_consume_fee = ? ,";
			updateSQL += " paid_consume_fee =0 ,";	
			updateSQL += " pre_pay_amt =0 ,";
		}
		updateSQL += " mod_time  = sysdate,";
		updateSQL += " mod_user  = ?,";
		updateSQL += " mod_pgm  = ?";
		whereStr = " where p_seqno = ? ";
		setDouble(1, hAuthNotDeposit); // Fsc_Icud03.auth_not_deposit累計
		setDouble(2, hUnpayAmtCash);
		if(hAcctJrnlBal<0) {
			setDouble(3, hAcctJrnlBal*-1);
		}	else	{
			setDouble(3, hAcctJrnlBal);
		}		
		setString(4, fsIcud03.modUser);
		setString(5, fsIcud03.modPgm);
		setString(6, fsIcud03.pSeqno);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_cca_consume not found!", "p_seqno:" + fsIcud03.pSeqno, "");
		}
	}

	/*************************************************************************/
	boolean checkFSCIcuD03() throws Exception {
		int cnt;
		sqlCmd = " select count(*) as cnt";
		sqlCmd += " from FSC_ICUD03 ";
		sqlCmd += " where file_no =? ";
		sqlCmd += " and is_process = 'Y' ";
		setString(1, yyyymmdd + nn);
		int recordCnt = selectTable();
		cnt = getValueInt("cnt");
		if (cnt > 0)	return false;
//			comcr.errRtn("此帳戶異動資料通知檔已執行更新作業", "", "");
		return true ;
	}

	/*************************************************************************/
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

	/************************************************************************/
	public String getACNOPSEQNO() throws Exception {
		String seq = "";
		sqlCmd = "select to_char(ECS_ACNO.nextval,'0000000000') as seq";
		sqlCmd += "  from dual ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			seq = getValue("seq");
		}
		return seq;
	}

	/************************************************************************/
	
	// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//	public double getCardAcctIdx() throws Exception {
//		double seq = 0;
//		sqlCmd = "select ECS_CARD_ACCT_IDX.nextval as seq";
//		sqlCmd += "  from dual ";
//		int recordCnt = selectTable();
//		if (recordCnt > 0) {
//			seq = getValueDouble("seq");
//		}
//		return seq;
//	}

	/************************************************************************/
	public void renameFile(String removeFileName) throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/icu/" + removeFileName;
		String tmpstr2 = comc.getECSHOME() + "/media/icu/backup/" + removeFileName + "." + sysDate;

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!" + tmpstr2);
			return;
		}
		showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/************************************************************************/
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

	/***********************************************************************/
	void procErrorReport() throws Exception {
		String errorFile = "";		
		int errFile03 = 0;
		if(yyyymmdd.isEmpty())	yyyymmdd = hBusiBusinessDate;
		if(nn.isEmpty())	nn = "01";
		errorFileName = "ICACTQND.ERR." + yyyymmdd+nn + ".TXT";
		errorFile = String.format("%s/media/icu/error/%s", comc.getECSHOME(),errorFileName);
		errFile03 = openOutputText(errorFile, "MS950");
		if (errFile03 < 0) {
			comcr.errRtn("creat report error", "", "無法產出錯誤報表");
			return;
		}
		extendField = "report.";
		sqlCmd = "select * from fsc_icud03 where 1=1 and file_no like ? and is_pass = 'N' ";
		setString(1, hBusiBusinessDate + "%");
		int reportCnt = selectTable();
		if (reportCnt <= 0) {
			closeOutputText(errFile03);
			return;
		}
		StringBuilder tempBuf = new StringBuilder();
		String errorReason = "", newLine = "\r\n";
		for (int ii = 0; ii < reportCnt; ii++) {
			errorReason = "";
			tempBuf.setLength(0);
			tempBuf.append(comc.fixLeft(getValue("report.mod_acdr", ii), 1));
			tempBuf.append(comc.fixLeft(getValue("report.id_no", ii), 11));
			if (getValue("report.card_no", ii).isEmpty()) {
				tempBuf.append(
						comc.fixLeft(getValue("report.card_type", ii) + "-" + getValue("report.card_seqno", ii), 16));
			} else {
				tempBuf.append(comc.fixLeft(getValue("report.card_no", ii), 16));
			}
			if (getValue("report.nopass_reason", ii).equals("N1")) {
				errorReason = getValue("report.nopass_reason", ii) + "-異動碼錯誤";
			} else if (getValue("report.nopass_reason", ii).equals("N2")) {
				errorReason = getValue("report.nopass_reason", ii) + "-卡片或持卡人資料不存在";
			} else if (getValue("report.nopass_reason", ii).equals("N3")) {
				errorReason = getValue("report.nopass_reason", ii) + "-異動碼為D時，帳戶狀態碼錯誤";
			}
			tempBuf.append(comc.fixLeft(errorReason, 200));
			tempBuf.append(comc.fixLeft(hBusiBusinessDate, 8));
			tempBuf.append(newLine);
			writeTextFile(errFile03, tempBuf.toString());
		}
		closeOutputText(errFile03);
	}

	/***********************************************************************/
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

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		IcuD003 proc = new IcuD003();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class FscIcud03 extends hdata.BaseBin {
		public String fileNo = "";
		public int serialNO = 0;
		public String modAcdr = "";
		public String cardType = "";
		public String cardSeqno = "";
		public String statusCfq = "";
		public double lineOfCreditAmt = 0;
		public double unpayAmt = 0;
		public double authNotDeposit = 0;
		public double acctJrnlBal = 0;
		public String jrnlBalSign = "";
		public double unpayAmtCash = 0;
		public String openDate = "";
		public String statusCfqDate = "";
		public double locamtCashRate = 0;
		public String locamtCashDay = "";
		public String isAddCard = "";
		public String isPass = "";
		public String isProcess = "";
		public String cardNo = "";
		public String acctType = "";
		public String idPSeqno = "";
		public String idNo = "";
		public String acnoPSeqno = "";
		public String pSeqno = "";
		public String crtDate = "";
		public String crtTime = "";
		public String crtUser = "";
		public double idNoCrediLimit = 0;
		public String noPassReason = "";

		@Override
		public void initData() {
			fileNo = "";
			// serial_no =0;
			modAcdr = "";
			cardType = "";
			cardSeqno = "";
			statusCfq = "";
			lineOfCreditAmt = 0;
			unpayAmt = 0;
			authNotDeposit = 0;
			acctJrnlBal = 0;
			jrnlBalSign = "";
			unpayAmtCash = 0;
			openDate = "";
			statusCfqDate = "";
			locamtCashRate = 0;
			locamtCashDay = "";
			isAddCard = "";
			isPass = "";
			isProcess = "";
			cardNo = "";
			acctType = "";
			idPSeqno = "";
			idNo = "";
			acnoPSeqno = "";
			pSeqno = "";
			crtDate = "";
			crtTime = "";
			crtUser = "";
			idNoCrediLimit = 0;
			hIntRateMcode = 0;
			noPassReason = "";
		}

	}
	
	class FscIcud15 extends hdata.BaseBin {
		public String fileNo = "";
		public int serialNO = 0;
		public String modAcdr = "";
		public String cardType = "";
		public String cardSeqno = "";
		public String statusCfq = "";
		public double lineOfCreditAmt = 0;
		public double unpayAmt = 0;
		public double authNotDeposit = 0;
		public double acctJrnlBal = 0;
		public String jrnlBalSign = "";
		public double unpayAmtCash = 0;
		public String openDate = "";
		public String statusCfqDate = "";
		public double locamtCashRate = 0;
		public String locamtCashDay = "";
		public String isAddCard = "";
		public String isPass = "";
		public String isProcess = "";
		public String cardNo = "";
		public String acctType = "";
		public String idPSeqno = "";
		public String idNo = "";
		public String acnoPSeqno = "";
		public String pSeqno = "";
		public String crtDate = "";
		public String crtTime = "";
		public String crtUser = "";		
		public String noPassReason = "";
		public String corpPSeqno = "";
		public String corpNo = "";
		
		@Override
		public void initData() {
			fileNo = "";
			// serial_no =0;
			modAcdr = "";
			cardType = "";
			cardSeqno = "";
			statusCfq = "";
			lineOfCreditAmt = 0;
			unpayAmt = 0;
			authNotDeposit = 0;
			acctJrnlBal = 0;
			jrnlBalSign = "";
			unpayAmtCash = 0;
			openDate = "";
			statusCfqDate = "";
			locamtCashRate = 0;
			locamtCashDay = "";
			isAddCard = "";
			isPass = "";
			isProcess = "";
			cardNo = "";
			acctType = "";
			idPSeqno = "";
			idNo = "";
			acnoPSeqno = "";
			pSeqno = "";
			crtDate = "";
			crtTime = "";
			crtUser = "";			
			hIntRateMcode = 0;
			noPassReason = "";
			corpPSeqno = "";
			corpNo = "";
		}

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
	
	String convertSeqNo(int oldSeqNo) {
		String newSeqNo = "" , oldSeqString = "" , temp1 = "" , temp2 = "";
		com.CommString commString = new com.CommString();
		if(oldSeqNo == 100) {
			newSeqNo = "AA";
		}	else if (oldSeqNo < 10) {
			newSeqNo = "A";
			oldSeqString = commString.int2Str(oldSeqNo);
			newSeqNo += seq2Eng(oldSeqString);
		}	else	{
			oldSeqString = commString.int2Str(oldSeqNo);
			temp1 = commString.mid(oldSeqString, 0,1);
			temp2 = commString.mid(oldSeqString, 1,1);
			newSeqNo += seq2Eng(temp1);
			newSeqNo += seq2Eng(temp2);
		}
		
		return newSeqNo;
	}
	
	String seq2Eng(String seq) {		
		switch(seq) {
			case "0":
				return "A";			
			case "1":
				return "B";			
			case "2":
				return "C";			
			case "3":
				return "D";			
			case "4":
				return "E";			
			case "5":
				return "F";			
			case "6":
				return "G";			
			case "7":
				return "H";			
			case "8":
				return "I";			
			case "9":
				return "J";			
			default :
				return "Z";					
		}
	}
	
	void moveColumn03To15() throws Exception {
		//--file_no = fsIcud03.fileNo 前 8 碼 + (100 - (fsIcud03.fileNo 後2碼))
		String tempSeq = "";
		com.CommString commString = new com.CommString();
		int tempFileSeq = 0;
		tempFileSeq = 100 - Integer.parseInt(fsIcud03.fileNo.substring(fsIcud03.fileNo.length()-2,fsIcud03.fileNo.length()));
		tempSeq = convertSeqNo(tempFileSeq);
//		fsIcud15.fileNo = fsIcud03.fileNo.substring(0,8) + commString.int2Str(tempFileSeq);
		fsIcud15.fileNo = fsIcud03.fileNo.substring(0,8) + tempSeq;
		fsIcud15.serialNO = fsIcud03.serialNO;
		fsIcud15.modAcdr = fsIcud03.modAcdr;
		fsIcud15.cardType = fsIcud03.cardType;
		fsIcud15.cardSeqno = fsIcud03.cardSeqno;
		fsIcud15.statusCfq = fsIcud03.statusCfq;
		fsIcud15.lineOfCreditAmt = fsIcud03.lineOfCreditAmt;
		fsIcud15.unpayAmt = fsIcud03.unpayAmt;
		fsIcud15.authNotDeposit = fsIcud03.authNotDeposit;
		fsIcud15.acctJrnlBal = fsIcud03.acctJrnlBal;
		fsIcud15.jrnlBalSign = fsIcud03.jrnlBalSign;
		fsIcud15.unpayAmtCash = fsIcud03.unpayAmtCash;
		fsIcud15.openDate = fsIcud03.openDate;
		fsIcud15.statusCfqDate = fsIcud03.statusCfqDate;
		fsIcud15.locamtCashRate = fsIcud03.locamtCashRate;
		fsIcud15.locamtCashDay = fsIcud03.locamtCashDay;
		fsIcud15.isAddCard = fsIcud03.isAddCard;
		fsIcud15.isPass = "Y";
		fsIcud15.isProcess = "";
		fsIcud15.cardNo = fsIcud03.cardNo;
		fsIcud15.acctType = "06";
		fsIcud15.idPSeqno = fsIcud03.idPSeqno;
		fsIcud15.idNo = fsIcud03.idNo;
		fsIcud15.acnoPSeqno = fsIcud03.acnoPSeqno;
		fsIcud15.pSeqno = fsIcud03.pSeqno;
		fsIcud15.crtDate = fsIcud03.crtDate;
		fsIcud15.crtTime = fsIcud03.crtTime;
		fsIcud15.crtUser = fsIcud03.crtUser;
		fsIcud15.noPassReason = fsIcud03.noPassReason;
		fsIcud15.corpPSeqno = cardCorpPSeqno;
		fsIcud15.corpNo = cardCorpNo;
	}		
	
	void initData99() throws Exception {
		hCardAcctIdx = 0;
		hAcctJrnlBal = 0;
		hUnpayAmt = 0;
		hAuthNotDeposit = 0;
		hUnpayAmtCash = 0;
		hIntRateMcode = 0;
		hAcnoPSeqno = "";
	}
	
	void clearData() throws Exception {
		String tempMonth = "";
		tempMonth = commDate.dateAdd(hBusiBusinessDate, 0, -1, 0);
		daoTable = "fsc_icud03";
		whereStr = "where file_no < ? ";
		setString(1,tempMonth);
		deleteTable();
		showLogMessage("I", "", "清除 日期 ["+tempMonth+" (不含)] 以前的檔案資料");
	}
	
}
