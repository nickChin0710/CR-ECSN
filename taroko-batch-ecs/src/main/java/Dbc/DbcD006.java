/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/13  V1.00.02  yanghan       修改了變量名稱和方法名稱                                                                  *
*  109/11/30  V1.00.03  Wilson      mark select check chk_nccc_flag           *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *                                                                 *
******************************************************************************/

package Dbc;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommSecr;

public class DbcD006 extends AccessDAO {
	private String progname = "製卡回饋資料轉入(Debit)  109/12/24 V1.00.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommSecr comsecr = null;

	int debug = 1;
	String hCallErrorDesc = "";
	int tmpInt = 0;
	int totalCnt = 0;

	String prgmId = "DbcD006";
	String hCallBatchSeqno = "";

	String hChiDate = "";
	String hNewFilename = "";
	int hNewRecordCnt = 0;
	String hDembModUser = "";
	String hFilename = "";
	String hNewNcccDate = "";
	String hNewBatchno = "";
	String hDembSlipNo = "";
	String hDembCardNo = "";
	String hDembCntlAreaCode = "";
	String hDembStatus = "";
	String hDembReasonCode = "";
	String hDembValidTo = "";
	String hDembStockNo = "";
	String hCardType = "";
	String hDembPvki = "";
	String hDembPvv = "";
	String hDembCvv = "";
	String hDembCvv2 = "";
	String hDembBankId = "";
	String hDembErrorCode = "";
	String hDembRejectCode = "";
	String hDembActiveFlag = "";
	String hDembCreditNote = "";
	String hDembFileDate = "";
	String hDembModPgm = "";
	String hDembModSeqno = "";
	String hhBatchno = "";
	long hhRecno = 0;
	String hhRowid = "";
	String hEmbossResult = "";
	String hId = "";
	String hDembApplyId = "";
	String hDembApplyIdCode = "";
	String hDembValidFm = "";
	String hDembZipCode = "";
	String hDembBirthday = "";
	String hDembNation = "";
	String hDembBusinessCode = "";
	String hDembEducation = "";
	String hDembActNo = "";
	String hDembUnitCode = "";
	String hDembBusCardType = "";
	String hDembTelArea = "";
	String hDembTelNo = "";
	String hDembEmboss4thData = "";
	String hDembNcccTypeNo = "";
	String hDembClass = "";
	String hDembPmId = "";
	String hDembPmIdCode = "";
	String hDembCorpNo = "";
	String hDembCorpNoCode = "";
	String hDembForceFlag = "";
	String hDembMarriage = "";
	String hDembRelWithPm = "";
	String hDembSex = "";
	String hDembServiceCode = "";
	String hDembOldCardNo = "";
	String hDembChiName = "";
	String hDembEngName = "";
	String hDembAddr1 = "";
	String hDembAddr2 = "";
	String hDembAddr3 = "";
	String hBinNo = "";
	String hBatchno = "";
	long pRecno = 0;
	long hDembConsumeMmAmt = 0;
	String hNowDate = "";
	String hComboRowid = "";
	String hDcesBatchno = "";
	double hDcesRecno = 0;
	String hDcesEmbossSource = "";
	String hDcesEmbossReason = "";
	String hDcesCardNo = "";
	String hDcesMajorCardNo = "";
	String hDcesApplyId = "";
	String hDcesApplyIdCode = "";
	String hDcesPmId = "";
	String hDcesPmIdCode = "";
	String hDcesGroupCode = "";
	String hDcesSourceCode = "";
	String hDcesBirthday = "";
	String hDcesCardType = "";
	String hDcesRowid = "";
	String hDcesNcccType = "";

	int errCode = 0;
	String temstr = "";
	String hChgBatchno = "";
	String hChgNcccDate = "";
	int hNewSucCnt = 0;
	int embossOk = 0;
	int hNewFailCnt = 0;
	int hNewMoreCnt = 0;
	int hChgRecordCnt = 0;
	int hChgSucCnt = 0;
	int hChgFailCnt = 0;
	int hChgMoreCnt = 0;
	int hVendorRecordCnt = 0;
	int hFailRecVendorCnt = 0;
	int hSucRecVendorCnt = 0;

	buf1 newBuf = new buf1();
	buf2 chgBuf = new buf2();

	private int actCnt = 0;
	// ******************************************************

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0) {
				comc.errExit("Usage : DbcD006 ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			comsecr = new CommSecr(getDBconnect(), getDBalias());

			sqlCmd = "select to_char(to_number(to_char(sysdate,'yyyymmdd'))-19110000) h_chi_date ";
			sqlCmd += " from dual ";
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hChiDate = getValue("h_chi_date");
			}
			hDembModUser = comc.commGetUserID();
			hDembModPgm = prgmId;

			readNcccfile();
			processVendor();

			// ==============================================
			// 固定要做的
			hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
			showLogMessage("I", "", hCallErrorDesc);

			if (hCallBatchSeqno.length() == 20)
				comcr.callbatch(1, 0, 1); // 1: 結束

			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void processVendor() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "a.batchno,";
		sqlCmd += "a.recno,";
		sqlCmd += "a.emboss_source,";
		sqlCmd += "a.emboss_reason,";
		sqlCmd += "a.card_no,";
		sqlCmd += "a.major_card_no,";
		sqlCmd += "a.apply_id,";
		sqlCmd += "decode(a.apply_id_code,'','0',a.apply_id_code) h_dces_apply_id_code,";
		sqlCmd += "a.pm_id,";
		sqlCmd += "decode(a.pm_id_code,'','0',a.pm_id_code) h_dces_pm_id_code,";
		sqlCmd += "a.group_code,";
		sqlCmd += "a.source_code,";
		sqlCmd += "a.card_type,";
		sqlCmd += "a.birthday,";
		sqlCmd += "a.rowid  as rowid,";
		sqlCmd += "a.nccc_type ";
		sqlCmd += " from dbc_emboss a ";
		sqlCmd += "where a.card_no       <> '' ";
		sqlCmd += "  and a.nccc_filename <> '' ";
		sqlCmd += "  and a.rtn_nccc_date  = '' ";
		sqlCmd += "  and a.reject_code    = '' ";
		sqlCmd += "  and (a.vendor <> '2' and a.vendor <> '') ";
//		sqlCmd += "  and decode(a.chk_nccc_flag,'','N',a.chk_nccc_flag) ='N' ";
		sqlCmd += "order by a.card_type,a.group_code,a.batchno,a.recno ";
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hDcesBatchno = getValue("batchno", i);
			hDcesRecno = getValueDouble("recno", i);
			hDcesEmbossSource = getValue("emboss_source", i);
			hDcesEmbossReason = getValue("emboss_reason", i);
			hDcesCardNo = getValue("card_no", i);
			hDcesMajorCardNo = getValue("major_card_no", i);
			hDcesApplyId = getValue("apply_id", i);
			hDcesApplyIdCode = getValue("h_dces_apply_id_code", i);
			hDcesPmId = getValue("pm_id", i);
			hDcesPmIdCode = getValue("h_dces_pm_id_code", i);
			hDcesGroupCode = getValue("group_code", i);
			hDcesSourceCode = getValue("source_code", i);
			hDcesBirthday = getValue("birthday", i);
			hDcesCardType = getValue("card_type", i);
			hDcesRowid = getValue("rowid", i);
			hDcesNcccType = getValue("nccc_type", i);

			totalCnt++;
			hVendorRecordCnt++;
			updateDbcEmbossVendor();

			commitDataBase();
		}

		showLogMessage("I", "", String.format("VENDOR 總讀取筆數 [%d]", hVendorRecordCnt));
		showLogMessage("I", "",
				String.format("VENDOR 總回饋SUCC筆數 [%d] FAIL筆數 [%d]", hSucRecVendorCnt, hFailRecVendorCnt));

		return;
	}

	/***********************************************************************/
	void updateDbcEmbossVendor() throws Exception {
		int foundRec;

		foundRec = 0;
		try {
			if (hDcesNcccType.equals("1")) {
				if ((hDcesEmbossSource.equals("1")) || (hDcesEmbossSource.equals("2"))) {
					daoTable = "dbc_emboss";
					updateSQL = "rtn_nccc_date = ?,";
					updateSQL += " error_code = '',";
					updateSQL += " reject_code = '',";
					updateSQL += " emboss_result = '0',";
					updateSQL += " mod_time = sysdate,";
					updateSQL += " mod_pgm  = ?";
					whereStr = "where rowid  = ? ";
					setString(1, sysDate);
					setString(2, prgmId);
					setRowId(3, hDcesRowid);
					updateTable();

					foundRec = 1;
				}

			}
			if (hDcesNcccType.equals("2")) {
				if ((hDcesEmbossSource.equals("3")) || (hDcesEmbossSource.equals("4"))) {

					daoTable = "dbc_emboss";
					updateSQL = "rtn_nccc_date = ?,";
					updateSQL += " error_code = '',";
					updateSQL += " reject_code = '',";
					updateSQL += " emboss_result = '0',";
					updateSQL += " mod_time = sysdate,";
					updateSQL += " mod_pgm  = ?";
					whereStr = "where rowid  = ? ";
					setString(1, sysDate);
					setString(2, prgmId);
					setRowId(3, hDcesRowid);
					updateTable();

					foundRec = 1;
				}

			}

			if (hDcesNcccType.equals("3")) {
				if ((hDcesEmbossSource.compareTo("5") >= 0)) {
					daoTable = "dbc_emboss";
					updateSQL = "rtn_nccc_date = to_char(sysdate,'yyyymmdd'),";
					updateSQL += " error_code = '',";
					updateSQL += " reject_code = '',";
					updateSQL += " emboss_result = '0',";
					updateSQL += " mod_time = sysdate,";
					updateSQL += " mod_pgm  = ?";
					whereStr = "where rowid  = ? ";
					setString(1, prgmId);
					setRowId(2, hDcesRowid);
					updateTable();

					foundRec = 1;

				}
			}

		} catch (Exception ex) {
			foundRec = 0;
		}

		if (foundRec == 1)
			hSucRecVendorCnt++;
		else
			hFailRecVendorCnt++;

		updateDbcDebitVendor();
		return;
	}

	/***********************************************************************/
	void updateDbcDebitVendor() throws Exception {

		String hComboRowid = "";
		hComboRowid = "";
		sqlCmd = "select rowid  as rowid ";
		sqlCmd += " from dbc_debit  ";
		sqlCmd += "where card_no = ? ";
		setString(1, hDcesCardNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_dbc_debit not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hComboRowid = getValue("rowid");

			daoTable = "dbc_debit";
			updateSQL = "third_data = '',";
			updateSQL += " rtn_nccc_date = to_char(sysdate,'yyyymmdd'),";
			updateSQL += " mod_time = sysdate,";
			updateSQL += " mod_pgm = ?";
			whereStr = "where rowid  = ? ";
			setString(1, prgmId);
			setRowId(2, hComboRowid);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_dbc_debit not found!", "", hCallBatchSeqno);
			}

		}
		return;
	}

	/***********************************************************************/
	void readNcccfile() throws Exception {
		String tmp = "";
		String str600 = "";
		getFilename();
		String dir = String.format("%s/media/crd/NCCC/", comc.getECSHOME());
		List<String> files = comc.listFS(dir, "", "");

		for (String fileEntry : files) {
			tmp = fileEntry;
			String t1 = comc.getSubString(tmp, 0, 3);
			String t2 = comc.getSubString(tmp, tmp.length() - 2, tmp.length());
			if (t1.equals("O02") == false || t2.equals("F1") == false)
				continue;
			str600 = dir + "/" + tmp;
			processFormate(str600);

			tmp = comc.getSubString(str600, 0, str600.length() - 1);
			String cmdStr = String.format("mv %s.SEC %s/media/crd/NCCC/backup/NCCC_O%7s", tmp, comc.getECSHOME(),
					hChiDate);
			String src = String.format("%s.SEC", tmp);
			String target = String.format("%s/media/crd/NCCC/backup/NCCC_O%7s", comc.getECSHOME(), hChiDate);
			showLogMessage("I", "", cmdStr);
			comc.fileRename2(src, target);
			cmdStr = String.format("rm %s", tmp);
			showLogMessage("I", "", cmdStr);
			comc.fileDelete(src);
		}

		return;
	}

	/***********************************************************************/
	void getFilename() throws Exception {
		String tmpStr = "";

		sqlCmd = "select ";
		sqlCmd += "file_name ";
		sqlCmd += "from crd_file_ctl ";
		sqlCmd += "where file_name like 'I02%' ";
		sqlCmd += "and receive_nccc_date = to_char(sysdate,'yyyymmdd') ";
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hFilename = getValue("file_name", i);

			tmpStr = String.format("O%7.7s", hFilename.substring(1));
			temstr = String.format("%s/media/crd/NCCC/%s.SEC", comc.getECSHOME(), tmpStr);
			temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
			int br = openInputText(temstr, "MS950");
			if (br != -1) {
				closeInputText(br);
				errCode = comsecr.toDecrypt(temstr);
				if (errCode != 0)
					showLogMessage("I", "", String.format("Encrypt filename[%s]error[%d]", temstr, errCode));
			} else
				showLogMessage("I", "", String.format("No Such file[%s]", temstr));
		}

	}

	/***********************************************************************/
	void processFormate(String filename) throws Exception {
		String pFilename = "";
		String str600 = "";
		int len = 0;

		pFilename = filename;
		len = pFilename.length();

		checkFopen(pFilename);
		if (len > 0) {
			int br = openInputText(temstr, "MS950");
			if (br == -1) {
				comcr.errRtn("檔案不存在：" + temstr, "", hCallBatchSeqno);
			}
			while (true) {
				str600 = readTextFile(br);
				if (endFile[br].equals("Y"))
					break;

				splitBuf1(str600);
				hDembSlipNo = newBuf.slipNo.trim();
			}
			closeInputText(br);

			/***************************************
			 * 新製卡作業
			 ***************************************/
			if ((hDembSlipNo.equals("PS12")) || (hDembSlipNo.equals("UD03")) || (hDembSlipNo.equals("PS16"))) {
				readFile1();
			}
			/***************************************
			 * 換卡作業
			 ***************************************/
			if (hDembSlipNo.equals("PS03")) {
				readFile2();
			}
		}
		insertFileCtl1();
		return;
	}

	/***********************************************************************/
	void insertFileCtl1() throws Exception {
		setValue("file_name", hNewFilename);
		setValue("crt_date", sysDate);
		setValueInt("head_cnt", hNewRecordCnt);
		setValueInt("record_cnt", hNewRecordCnt);
		setValue("rcv_send_date", sysDate);
		setValue("rcv_send_id", hDembModUser);
		setValue("trans_in_date", sysDate);
		daoTable = "crd_file_ctl";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_crd_file_ctl duplicate!", "", hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void readFile2() throws Exception {
		String str600 = "";

		getBatchno();
		int br = openInputText(temstr, "MS950");
		if (br == -1) {
			comcr.errRtn("檔案不存在：" + temstr, "", hCallBatchSeqno);
		}
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			splitBuf2(str600);
			hChgRecordCnt++;
			hNewRecordCnt++;
			getField2();

			procDbcEmboss2();
		}
		closeInputText(br);

		showLogMessage("I", "", String.format("換卡總回饋筆述 [%d] 差異筆數 [%d]", hChgRecordCnt, hChgMoreCnt));
		showLogMessage("I", "", String.format("CHANGE CARD FAIL CNT [%d] ", hChgFailCnt));

		return;
	}

	/***********************************************************************/
	void getField2() throws Exception {
		String tmpStr = "";
		String tmpStr1 = "";

		tmpStr = chgBuf.slipNo.trim();
		hDembSlipNo = tmpStr;

		tmpStr = chgBuf.cardNo.trim();
		hDembCardNo = tmpStr;

		tmpStr = chgBuf.cntlAreaCode.trim();
		hDembCntlAreaCode = tmpStr;

		tmpStr = chgBuf.status.trim();
		hDembStatus = tmpStr;

		tmpStr = chgBuf.reasonCode.trim();
		hDembReasonCode = tmpStr;

		tmpStr = chgBuf.stockNo.trim();
		hDembStockNo = tmpStr;

		tmpStr = chgBuf.pvki.trim();
		hDembPvki = tmpStr;

		tmpStr = chgBuf.pvv.trim();
		hDembPvv = tmpStr;

		tmpStr = chgBuf.cvv.trim();
		hDembCvv = tmpStr;

		tmpStr = chgBuf.cvv2.trim();
		hDembCvv2 = tmpStr;

		tmpStr = chgBuf.bankId.trim();
		hDembBankId = tmpStr;

		tmpStr = chgBuf.fileDate.trim();
		if (tmpStr.length() > 0) {
			tmpStr1 = String.format("%2.2s", tmpStr);
			if (comcr.str2long(tmpStr1) >= 69)
				hDembFileDate = String.format("%d", comcr.str2long(tmpStr) + 19000000);
			else
				hDembFileDate = String.format("%d", comcr.str2long(tmpStr) + 20000000);

		}
		tmpStr = chgBuf.errorCode.trim();
		hDembErrorCode = tmpStr;

		tmpStr = chgBuf.rejectCode.trim();
		hDembRejectCode = tmpStr;
		hEmbossResult = "";
		if (hDembRejectCode.length() == 0) {
			hEmbossResult = "0"; /* 成功 */
		} else {
			hEmbossResult = "1"; /* 失敗 */
		}

		tmpStr = chgBuf.activeFlag.trim();
		hDembActiveFlag = tmpStr;
	}

	/***********************************************************************/
	void procDbcEmboss2() throws Exception {
		if (hDembRejectCode.length() == 0) {
			updateDbcEmboss2();
			hChgSucCnt++;
		} else {
			updateDbcEmboss2();
			if ((hhBatchno.length() > 0) && (embossOk == 0)) {
				hChgFailCnt++;
			}
		}
		updateDbcDebit();
		return;
	}

	/***********************************************************************/
	void updateDbcEmboss2() throws Exception {
		hhBatchno = "";
		hhRecno = 0;
		hhRowid = "";
		embossOk = 0;
		sqlCmd = "select batchno,";
		sqlCmd += "recno,";
		sqlCmd += "rowid  as rowid ";
		sqlCmd += " from dbc_emboss  ";
		sqlCmd += "where card_no = ?  ";
		sqlCmd += "and nccc_type = '2'  ";
		sqlCmd += "and (emboss_source = '3' or emboss_source = '4')  ";
		sqlCmd += "and rtn_nccc_date =''  ";
		sqlCmd += "and reject_code ='' ";
		setString(1, hDembCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hhBatchno = getValue("batchno");
			hhRecno = getValueInt("recno");
			hhRowid = getValue("rowid");

			daoTable = "dbc_emboss";
			updateSQL = "stock_no = ?,";
			updateSQL += " rtn_nccc_date = ?,";
			updateSQL += " error_code = ?,";
			updateSQL += " reject_code = ?,";
			updateSQL += " emboss_result = ?,";
			updateSQL += " mod_time = sysdate,";
			updateSQL += " mod_pgm  = ?";
			whereStr = "where rowid  = ? ";
			setString(1, hDembStockNo);
			setString(2, hDembFileDate);
			setString(3, hDembErrorCode);
			setString(4, hDembRejectCode);
			setString(5, hEmbossResult);
			setString(6, prgmId);
			setRowId(7, hhRowid);
			updateTable();
		}
		if (notFound.equals("Y")) {
			embossOk = 1;
			hChgSucCnt--;
			hChgMoreCnt++;
		}
	}

	/***********************************************************************/
	void readFile1() throws Exception {
		String str600 = "";

		getBatchno();
		int br = openInputText(temstr, "MS950");
		if (br == -1) {
			comcr.errRtn("檔案不存在：" + temstr, "", hCallBatchSeqno);
		}
		while (true) {
			str600 = readTextFile(br);
			if (endFile[br].equals("Y"))
				break;

			splitBuf1(str600);
			hNewRecordCnt++;
			getField1();
			procDbcEmboss1();
		}
		closeInputText(br);

		showLogMessage("I", "", String.format("NEW_CARD 總回饋筆數 [%d] 差異筆數 [%d]", hNewRecordCnt, hNewMoreCnt));
		showLogMessage("I", "", String.format("NEW CARD FAIL CNT [%d] ", hNewFailCnt));

		return;
	}

	/***********************************************************************/
	void procDbcEmboss1() throws Exception {
		updateDbcEmboss1();
		if (hDembRejectCode.length() == 0) {
			hNewSucCnt++;
		} else {
			if ((hhBatchno.length() > 0) && (embossOk == 0)) {
				hNewFailCnt++;
			}
		}
		updateDbcDebit();
		return;
	}

	/***********************************************************************/
	void updateDbcEmboss1() throws Exception {
		boolean bError = false;
		hhBatchno = "";
		hhRecno = 0;
		hhRowid = "";
		embossOk = 0;
		if (hDembSlipNo.equals("PS12")) {
			sqlCmd = "select batchno,";
			sqlCmd += "recno,";
			sqlCmd += "rowid  as rowid ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where card_no = ?  ";
			sqlCmd += "and nccc_type = '1'  ";
			sqlCmd += "and (emboss_source = '1' or emboss_source = '2')  ";
			sqlCmd += "and rtn_nccc_date =''  ";
			sqlCmd += "and reject_code ='' ";
			setString(1, hDembCardNo);
			int recordCnt = selectTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("select_dbc_emboss not found!", "", hCallBatchSeqno);
			}
			if (recordCnt > 0) {
				hhBatchno = getValue("batchno");
				hhRecno = getValueInt("recno");
				hhRowid = getValue("rowid");
				daoTable = "dbc_emboss";
				updateSQL = "stock_no = ?,";
				updateSQL += " rtn_nccc_date = ?,";
				updateSQL += " error_code = ?,";
				updateSQL += " reject_code = ?,";
				updateSQL += " emboss_result = ?,";
				updateSQL += " mod_time = sysdate,";
				updateSQL += " mod_pgm  = ?";
				whereStr = "where rowid  = ? ";
				setString(1, hDembStockNo);
				setString(2, hDembFileDate);
				setString(3, hDembErrorCode);
				setString(4, hDembRejectCode);
				setString(5, hEmbossResult);
				setString(6, prgmId);
				setRowId(7, hhRowid);
				updateTable();
				if (notFound.equals("Y")) {
					bError = true;
				}
			}
		}
		if (hDembSlipNo.equals("PS16")) {
			/* AE card only */
			/* For 新製卡 */
			sqlCmd = "select batchno,";
			sqlCmd += "recno,";
			sqlCmd += "rowid  as rowid ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where card_no = ?  ";
			sqlCmd += "and nccc_type = '1'  ";
			sqlCmd += "and (emboss_source = '1' or emboss_source = '2')  ";
			sqlCmd += "and rtn_nccc_date =''  ";
			sqlCmd += "and reject_code ='' ";
			setString(1, hDembCardNo);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hhBatchno = getValue("batchno");
				hhRecno = getValueInt("recno");
				hhRowid = getValue("rowid");

				daoTable = "dbc_emboss";
				updateSQL = "stock_no = ?,";
				updateSQL += " rtn_nccc_date = ?,";
				updateSQL += " error_code = ?,";
				updateSQL += " reject_code = ?,";
				updateSQL += " emboss_result = ?,";
				updateSQL += " mod_time = sysdate,";
				updateSQL += " mod_pgm  = ?";
				whereStr = "where rowid  = ? ";
				setString(1, hDembStockNo);
				setString(2, hDembFileDate);
				setString(3, hDembErrorCode);
				setString(4, hDembRejectCode);
				setString(5, hEmbossResult);
				setString(6, prgmId);
				setRowId(7, hhRowid);
				updateTable();
				if (notFound.equals("Y")) {
					bError = true;
				}
			} else {
				/* For 重製 */
				sqlCmd = "select batchno,";
				sqlCmd += "recno,";
				sqlCmd += "rowid  as rowid ";
				sqlCmd += " from dbc_emboss  ";
				sqlCmd += "where card_no = ?  ";
				sqlCmd += "and nccc_type = '3'  ";
				sqlCmd += "and emboss_source >= '5'  ";
				sqlCmd += "and rtn_nccc_date =''  ";
				sqlCmd += "and reject_code ='' ";
				setString(1, hDembCardNo);
				recordCnt = selectTable();
				if (recordCnt > 0) {
					hhBatchno = getValue("batchno");
					hhRecno = getValueInt("recno");
					hhRowid = getValue("rowid");

					daoTable = "dbc_emboss";
					updateSQL = "stock_no = ?,";
					updateSQL += " rtn_nccc_date = ?,";
					updateSQL += " error_code = ?,";
					updateSQL += " reject_code = ?,";
					updateSQL += " emboss_result = ?,";
					updateSQL += " mod_time = sysdate,";
					updateSQL += " mod_pgm  = ?";
					whereStr = "where rowid  = ? ";
					setString(1, hDembStockNo);
					setString(2, hDembFileDate);
					setString(3, hDembErrorCode);
					setString(4, hDembRejectCode);
					setString(5, hEmbossResult);
					setString(6, prgmId);
					setRowId(7, hhRowid);
					updateTable();
					if (notFound.equals("Y")) {
						bError = true;
					}
				} else {
					/* For 換卡 */
					sqlCmd = "select batchno,";
					sqlCmd += "recno,";
					sqlCmd += "rowid  as rowid ";
					sqlCmd += " from dbc_emboss  ";
					sqlCmd += "where card_no = ?  ";
					sqlCmd += "and nccc_type = '2'  ";
					sqlCmd += "and (emboss_source = '3' or emboss_source = '4')  ";
					sqlCmd += "and rtn_nccc_date =''  ";
					sqlCmd += "and reject_code ='' ";
					setString(1, hDembCardNo);
					recordCnt = selectTable();
					if (notFound.equals("Y")) {
						comcr.errRtn("select_dbc_emboss not found!", "", hCallBatchSeqno);
					}
					if (recordCnt > 0) {
						hhBatchno = getValue("batchno");
						hhRecno = getValueInt("recno");
						hhRowid = getValue("rowid");

						daoTable = "dbc_emboss";
						updateSQL = "stock_no = ?,";
						updateSQL += " rtn_nccc_date = ?,";
						updateSQL += " error_code = ?,";
						updateSQL += " reject_code = ?,";
						updateSQL += " emboss_result = ?,";
						updateSQL += " mod_time = sysdate,";
						updateSQL += " mod_pgm  = ?";
						whereStr = "where rowid  = ? ";
						setString(1, hDembStockNo);
						setString(2, hDembFileDate);
						setString(3, hDembErrorCode);
						setString(4, hDembRejectCode);
						setString(5, hEmbossResult);
						setString(6, prgmId);
						setRowId(7, hhRowid);
						updateTable();
						if (notFound.equals("Y")) {
							bError = true;
						}
					}
				}
			}
		}
		if (hDembSlipNo.equals("UD03")) {
			sqlCmd = "select batchno,";
			sqlCmd += "recno,";
			sqlCmd += "rowid  ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where card_no = ?  ";
			sqlCmd += "and nccc_type = '3'  ";
			sqlCmd += "and emboss_source >= '5'  ";
			sqlCmd += "and rtn_nccc_date =''  ";
			sqlCmd += "and reject_code ='' ";
			setString(1, hDembCardNo);
			int recordCnt = selectTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("select_dbc_emboss not found!", "", hCallBatchSeqno);
			}
			if (recordCnt > 0) {
				hhBatchno = getValue("batchno");
				hhRecno = getValueInt("recno");
				hhRowid = getValue("rowid");

				daoTable = "dbc_emboss";
				updateSQL = "stock_no = ?,";
				updateSQL += " rtn_nccc_date = ?,";
				updateSQL += " error_code = ?,";
				updateSQL += " reject_code = ?,";
				updateSQL += " emboss_result = ?,";
				updateSQL += " mod_time = sysdate,";
				updateSQL += " mod_pgm  = ?";
				whereStr = "where rowid  = ? ";
				setString(1, hDembStockNo);
				setString(2, hDembFileDate);
				setString(3, hDembErrorCode);
				setString(4, hDembRejectCode);
				setString(5, hEmbossResult);
				setString(6, prgmId);
				setRowId(7, hhRowid);
				updateTable();
				if (notFound.equals("Y")) {
					bError = true;
				}
			}
		}
		if (bError) {
			embossOk = 1;
			hNewMoreCnt++;
			hNewSucCnt--;
		}
		return;
	}

	/***********************************************************************/
	void updateDbcDebit() throws Exception {
		String hComboRowid = "";
		hComboRowid = "";
		sqlCmd = "select rowid  as rowid ";
		sqlCmd += " from dbc_debit  ";
		sqlCmd += "where card_no = ? ";
		setString(1, hDembCardNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_dbc_debit not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hComboRowid = getValue("rowid");
			if (hDembRejectCode.length() == 0) {
				daoTable = "dbc_debit";
				updateSQL = "third_data = '',";
				updateSQL += " rtn_nccc_date = to_char(sysdate,";
				updateSQL += "'yyyymmdd'),";
				updateSQL += " mod_time = sysdate,";
				updateSQL += " mod_pgm = ?";
				whereStr = "where rowid  = ? ";
				setString(1, prgmId);
				setRowId(2, hComboRowid);
				updateTable();

			} else {
				daoTable = "dbc_debit";
				updateSQL = "third_data = '',";
				updateSQL += " rtn_nccc_date = to_char(sysdate,'yyyymmdd'),";
				updateSQL += " emboss_code = '1',";
				updateSQL += " emboss_date = to_char(sysdate,'yyyymmdd'),";
				updateSQL += " mod_time = sysdate,";
				updateSQL += " mod_pgm = ?";
				whereStr = "where rowid  = ? ";
				setString(1, prgmId);
				setRowId(2, hComboRowid);
				updateTable();
			}
			if (notFound.equals("Y")) {
				comcr.errRtn("update_" + daoTable + " not found!", "", hCallBatchSeqno);
			}
		}
		return;
	}

	/***********************************************************************/
	void getField1() throws Exception {
		String tmpStr = "";
		String tmpStr1 = "";
		long tmpValue = 0;

		tmpStr = newBuf.slipNo.trim();
		hDembSlipNo = tmpStr;

		tmpStr = newBuf.bankId.trim();
		hDembBankId = tmpStr;

		tmpStr = newBuf.cntlAreaCode.trim();
		hDembCntlAreaCode = tmpStr;

		tmpStr = newBuf.applyId.trim();
		hDembApplyId = tmpStr;

		tmpStr = newBuf.applyIdCode.trim();
		if (tmpStr.equals("A")) {
			hDembApplyIdCode = "0";
		} else {
			hDembApplyIdCode = tmpStr;
		}
		tmpStr = newBuf.validTo.trim();
		tmpValue = 0;

		tmpValue = comcr.str2long(tmpStr) + 20000000;
		tmpStr = String.format("%d", tmpValue);
		hDembValidTo = tmpStr;

		tmpStr = newBuf.zipCode.trim();
		hDembZipCode = tmpStr;

		tmpStr = newBuf.birthday.trim();
		tmpValue = 0;

		tmpValue = comcr.str2long(tmpStr) + 19000000;
		tmpStr = String.format("%d", tmpValue);
		hDembBirthday = tmpStr;

		tmpStr = newBuf.nation.trim();
		hDembNation = tmpStr;

		tmpStr = newBuf.businessCode.trim();
		hDembBusinessCode = tmpStr;

		tmpStr = newBuf.education.trim();
		hDembEducation = tmpStr;

		tmpStr = newBuf.actNo.trim();
		hDembActNo = tmpStr;

		tmpStr = newBuf.unitCode.trim();
		hDembUnitCode = tmpStr;

		tmpStr = newBuf.busCardType.trim();
		hDembBusCardType = tmpStr;

		tmpStr = newBuf.telArea.trim();
		hDembTelArea = tmpStr;

		tmpStr = newBuf.telNo.trim();
		hDembTelNo = tmpStr;

		tmpStr = newBuf.emboss4thData.trim();
		hDembEmboss4thData = tmpStr;

		tmpStr = newBuf.ncccTypeNo.trim();
		hDembNcccTypeNo = tmpStr;

		tmpStr = newBuf.class1.trim();
		hDembClass = tmpStr;

		tmpStr = newBuf.pmId.trim();
		hDembPmId = tmpStr;

		tmpStr = newBuf.pmIdCode.trim();
		hDembPmIdCode = tmpStr;

		tmpStr = newBuf.corpNo.trim();
		hDembCorpNo = tmpStr;

		tmpStr = newBuf.corpNoCode.trim();
		hDembCorpNoCode = tmpStr;

		tmpStr = newBuf.forceFlag.trim();
		hDembForceFlag = tmpStr;

		tmpStr = newBuf.validFm.trim();
		tmpValue = 0;

		tmpValue = comcr.str2long(tmpStr) + 20000000;
		tmpStr = String.format("%d", tmpValue);
		hDembValidFm = tmpStr;

		tmpStr = newBuf.serviceCode.trim();
		hDembServiceCode = tmpStr;

		tmpStr = newBuf.engName.trim();
		hDembEngName = tmpStr;

		tmpStr = newBuf.cvv2.trim();
		hDembCvv2 = tmpStr;

		tmpStr = newBuf.marriage.trim();
		hDembMarriage = tmpStr;

		tmpStr = newBuf.relWithPm.trim();
		hDembRelWithPm = tmpStr;

		tmpStr = newBuf.sex.trim();
		hDembSex = tmpStr;

		tmpStr = newBuf.pvv.trim();
		hDembPvv = tmpStr;

		tmpStr = newBuf.cvv.trim();
		hDembCvv = tmpStr;

		tmpStr = newBuf.pvki.trim();
		hDembPvki = tmpStr;

		tmpStr = newBuf.cardNo.trim();
		hDembCardNo = tmpStr;

		tmpStr = newBuf.rejectCode.trim();
		hDembRejectCode = tmpStr;
		hEmbossResult = "";
		if (hDembRejectCode.length() == 0) {
			hEmbossResult = "0";
		} else {
			hEmbossResult = "1";
		}

		tmpStr = newBuf.oldCardNo.trim();
		hDembOldCardNo = tmpStr;

		tmpStr = newBuf.stockNo.trim();
		hDembStockNo = tmpStr;

		tmpStr = newBuf.chiName.trim();
		hDembChiName = tmpStr;

		tmpStr = newBuf.addr1.trim();
		hDembAddr1 = tmpStr;

		tmpStr = newBuf.addr2.trim();
		hDembAddr2 = tmpStr;

		tmpStr = newBuf.addr3.trim();
		hDembAddr3 = tmpStr;

		tmpStr = newBuf.fileDate.trim();
		if (tmpStr.length() > 0) {
			tmpStr1 = String.format("%2.2s", tmpStr);

			if (comcr.str2long(tmpStr1) >= 69)
				hDembFileDate = String.format("%d", comcr.str2long(tmpStr) + 19000000);
			else
				hDembFileDate = String.format("%d", comcr.str2long(tmpStr) + 20000000);
		}
		tmpStr = newBuf.consumeMmAmt.trim();
		hDembConsumeMmAmt = comcr.str2long(tmpStr);

		tmpStr = newBuf.activeFlag.trim();
		hDembActiveFlag = tmpStr;

		tmpStr = newBuf.creditNote.trim();
		hDembCreditNote = tmpStr;
	}

	/***********************************************************************/
	void getBatchno() throws Exception {
		String hNowDate = "";

		pRecno = 0;

		sqlCmd = "select max(batchno) as batchno,";
		sqlCmd += "max(recno) as recno ";
		sqlCmd += " from dbc_emboss  ";
		sqlCmd += "where nccc_filename = ? ";
		setString(1, hNewFilename);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hBatchno = getValue("batchno");
			pRecno = getValueInt("recno");
		}

		if (hBatchno.length() == 0) {
			hNowDate = "";
			sqlCmd = "select to_char(sysdate,'yymmdd') h_now_date ";
			sqlCmd += " from dual ";
			recordCnt = selectTable();
			if (recordCnt > 0) {
				hNowDate = getValue("h_now_date");
			}
			hBatchno = hNowDate.substring(0, 6);
			hBatchno += "01";
			pRecno++;
		}

		if (debug == 1)
			showLogMessage("I", "", "  Get batchno = " + hBatchno);
	}

	/***********************************************************************/
	void checkFopen(String filename1) throws Exception {
		String p = "";

		temstr = comc.getSubString(filename1, 0, filename1.length() - 1);
		hNewBatchno = "";
		hChgBatchno = "";
		hChgNcccDate = "";
		hNewNcccDate = "";
		hNewNcccDate = sysDate.substring(0, 6);
		p = filename1 + "NCCC/O";
		hNewNcccDate += comc.getSubString(p, 8, 10);
		hNewFilename = "";
		hNewFilename = comc.getSubString(p, 5);

		return;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcD006 proc = new DbcD006();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class buf1 {
		String slipNo;
		String bankId;
		String icIndicator;
		String cntlAreaCode;
		String applyId;
		String applyIdCode;
		String validTo;
		String zipCode;
		String filler2;
		String birthday;
		String nation;
		String businessCode;
		String education;
		String actNo;
		String unitCode;
		String busCardType;
		String telArea;
		String telNo;
		String emboss4thData;
		String ncccTypeNo;
		String class1;
		String pmId;
		String pmIdCode;
		String corpNo;
		String corpNoCode;
		String forceFlag;
		String sourceType;
		String filler3;
		String validFm;
		String filler4;
		String serviceCode;
		String engName;
		String cvv2;
		String marriage;
		String relWithPm;
		String sex;
		String pvv;
		String cvv;
		String pvki;
		String cardNo;
		String rejectCode;
		String oldCardNo;
		String stockNo;
		String fileDate;
		String filler5;
		String chiName;
		String addr1;
		String addr2;
		String addr3;
		String filler6;
		String atmDdCnt;
		String atmMmCnt;
		String atmCntAmt;
		String atmDdAmt;
		String atmMmAmt;
		String consumeDdCnt;
		String consumeMmCnt;
		String consumeDdAmt;
		String consumeMmAmt;
		String orgDdCnt;
		String orgMmCnt;
		String orgCntAmt;
		String orgDdAmt;
		String orgMmAmt;
		String activeFlag;
		String creditNote;
		String endFlag;
		String len;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(slipNo, 4);
			rtn += comc.fixLeft(bankId, 2);
			rtn += comc.fixLeft(icIndicator, 1);
			rtn += comc.fixLeft(cntlAreaCode, 4);
			rtn += comc.fixLeft(applyId, 10);
			rtn += comc.fixLeft(applyIdCode, 1);
			rtn += comc.fixLeft(validTo, 4);
			rtn += comc.fixLeft(zipCode, 3);
			rtn += comc.fixLeft(filler2, 12);
			rtn += comc.fixLeft(birthday, 6);
			rtn += comc.fixLeft(nation, 1);
			rtn += comc.fixLeft(businessCode, 2);
			rtn += comc.fixLeft(education, 1);
			rtn += comc.fixLeft(actNo, 15);
			rtn += comc.fixLeft(unitCode, 4);
			rtn += comc.fixLeft(busCardType, 1);
			rtn += comc.fixLeft(telArea, 4);
			rtn += comc.fixLeft(telNo, 8);
			rtn += comc.fixLeft(emboss4thData, 20);
			rtn += comc.fixLeft(ncccTypeNo, 2);
			rtn += comc.fixLeft(class1, 1);
			rtn += comc.fixLeft(pmId, 10);
			rtn += comc.fixLeft(pmIdCode, 1);
			rtn += comc.fixLeft(corpNo, 8);
			rtn += comc.fixLeft(corpNoCode, 1);
			rtn += comc.fixLeft(forceFlag, 1);
			rtn += comc.fixLeft(sourceType, 1);
			rtn += comc.fixLeft(filler3, 21);
			rtn += comc.fixLeft(validFm, 4);
			rtn += comc.fixLeft(filler4, 8);
			rtn += comc.fixLeft(serviceCode, 3);
			rtn += comc.fixLeft(engName, 25);
			rtn += comc.fixLeft(cvv2, 3);
			rtn += comc.fixLeft(marriage, 1);
			rtn += comc.fixLeft(relWithPm, 1);
			rtn += comc.fixLeft(sex, 1);
			rtn += comc.fixLeft(pvv, 4);
			rtn += comc.fixLeft(cvv, 3);
			rtn += comc.fixLeft(pvki, 1);
			rtn += comc.fixLeft(cardNo, 16);
			rtn += comc.fixLeft(rejectCode, 2);
			rtn += comc.fixLeft(oldCardNo, 16);
			rtn += comc.fixLeft(stockNo, 6);
			rtn += comc.fixLeft(fileDate, 6);
			rtn += comc.fixLeft(filler5, 20);
			rtn += comc.fixLeft(chiName, 40);
			rtn += comc.fixLeft(addr1, 32);
			rtn += comc.fixLeft(addr2, 24);
			rtn += comc.fixLeft(addr3, 14);
			rtn += comc.fixLeft(filler6, 5);
			rtn += comc.fixLeft(atmDdCnt, 9);
			rtn += comc.fixLeft(atmMmCnt, 9);
			rtn += comc.fixLeft(atmCntAmt, 9);
			rtn += comc.fixLeft(atmDdAmt, 9);
			rtn += comc.fixLeft(atmMmAmt, 9);
			rtn += comc.fixLeft(consumeDdCnt, 9);
			rtn += comc.fixLeft(consumeMmCnt, 9);
			rtn += comc.fixLeft(consumeDdAmt, 9);
			rtn += comc.fixLeft(consumeMmAmt, 9);
			rtn += comc.fixLeft(orgDdCnt, 9);
			rtn += comc.fixLeft(orgMmCnt, 9);
			rtn += comc.fixLeft(orgCntAmt, 9);
			rtn += comc.fixLeft(orgDdAmt, 9);
			rtn += comc.fixLeft(orgMmAmt, 9);
			rtn += comc.fixLeft(activeFlag, 1);
			rtn += comc.fixLeft(creditNote, 1);
			rtn += comc.fixLeft(endFlag, 1);
			rtn += comc.fixLeft(len, 1);
			return rtn;
		}
	}

	void splitBuf1(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		newBuf.slipNo = comc.subMS950String(bytes, 0, 4);
		newBuf.bankId = comc.subMS950String(bytes, 4, 2);
		newBuf.icIndicator = comc.subMS950String(bytes, 6, 1);
		newBuf.cntlAreaCode = comc.subMS950String(bytes, 7, 4);
		newBuf.applyId = comc.subMS950String(bytes, 11, 10);
		newBuf.applyIdCode = comc.subMS950String(bytes, 21, 1);
		newBuf.validTo = comc.subMS950String(bytes, 22, 4);
		newBuf.zipCode = comc.subMS950String(bytes, 26, 3);
		newBuf.filler2 = comc.subMS950String(bytes, 29, 12);
		newBuf.birthday = comc.subMS950String(bytes, 41, 6);
		newBuf.nation = comc.subMS950String(bytes, 47, 1);
		newBuf.businessCode = comc.subMS950String(bytes, 48, 2);
		newBuf.education = comc.subMS950String(bytes, 50, 1);
		newBuf.actNo = comc.subMS950String(bytes, 51, 15);
		newBuf.unitCode = comc.subMS950String(bytes, 66, 4);
		newBuf.busCardType = comc.subMS950String(bytes, 70, 1);
		newBuf.telArea = comc.subMS950String(bytes, 71, 4);
		newBuf.telNo = comc.subMS950String(bytes, 75, 8);
		newBuf.emboss4thData = comc.subMS950String(bytes, 83, 20);
		newBuf.ncccTypeNo = comc.subMS950String(bytes, 103, 2);
		newBuf.class1 = comc.subMS950String(bytes, 105, 1);
		newBuf.pmId = comc.subMS950String(bytes, 106, 10);
		newBuf.pmIdCode = comc.subMS950String(bytes, 116, 1);
		newBuf.corpNo = comc.subMS950String(bytes, 117, 8);
		newBuf.corpNoCode = comc.subMS950String(bytes, 125, 1);
		newBuf.forceFlag = comc.subMS950String(bytes, 126, 1);
		newBuf.sourceType = comc.subMS950String(bytes, 127, 1);
		newBuf.filler3 = comc.subMS950String(bytes, 128, 21);
		newBuf.validFm = comc.subMS950String(bytes, 149, 4);
		newBuf.filler4 = comc.subMS950String(bytes, 153, 8);
		newBuf.serviceCode = comc.subMS950String(bytes, 161, 3);
		newBuf.engName = comc.subMS950String(bytes, 164, 25);
		newBuf.cvv2 = comc.subMS950String(bytes, 189, 3);
		newBuf.marriage = comc.subMS950String(bytes, 192, 1);
		newBuf.relWithPm = comc.subMS950String(bytes, 193, 1);
		newBuf.sex = comc.subMS950String(bytes, 194, 1);
		newBuf.pvv = comc.subMS950String(bytes, 195, 4);
		newBuf.cvv = comc.subMS950String(bytes, 199, 3);
		newBuf.pvki = comc.subMS950String(bytes, 202, 1);
		newBuf.cardNo = comc.subMS950String(bytes, 203, 16);
		newBuf.rejectCode = comc.subMS950String(bytes, 219, 2);
		newBuf.oldCardNo = comc.subMS950String(bytes, 221, 16);
		newBuf.stockNo = comc.subMS950String(bytes, 237, 6);
		newBuf.fileDate = comc.subMS950String(bytes, 243, 6);
		newBuf.filler5 = comc.subMS950String(bytes, 249, 20);
		newBuf.chiName = comc.subMS950String(bytes, 269, 40);
		newBuf.addr1 = comc.subMS950String(bytes, 309, 32);
		newBuf.addr2 = comc.subMS950String(bytes, 341, 24);
		newBuf.addr3 = comc.subMS950String(bytes, 365, 14);
		newBuf.filler6 = comc.subMS950String(bytes, 379, 5);
		newBuf.atmDdCnt = comc.subMS950String(bytes, 384, 9);
		newBuf.atmMmCnt = comc.subMS950String(bytes, 393, 9);
		newBuf.atmCntAmt = comc.subMS950String(bytes, 402, 9);
		newBuf.atmDdAmt = comc.subMS950String(bytes, 411, 9);
		newBuf.atmMmAmt = comc.subMS950String(bytes, 420, 9);
		newBuf.consumeDdCnt = comc.subMS950String(bytes, 429, 9);
		newBuf.consumeMmCnt = comc.subMS950String(bytes, 438, 9);
		newBuf.consumeDdAmt = comc.subMS950String(bytes, 447, 9);
		newBuf.consumeMmAmt = comc.subMS950String(bytes, 456, 9);
		newBuf.orgDdCnt = comc.subMS950String(bytes, 465, 9);
		newBuf.orgMmCnt = comc.subMS950String(bytes, 474, 9);
		newBuf.orgCntAmt = comc.subMS950String(bytes, 483, 9);
		newBuf.orgDdAmt = comc.subMS950String(bytes, 492, 9);
		newBuf.orgMmAmt = comc.subMS950String(bytes, 501, 9);
		newBuf.activeFlag = comc.subMS950String(bytes, 510, 1);
		newBuf.creditNote = comc.subMS950String(bytes, 511, 1);
		newBuf.endFlag = comc.subMS950String(bytes, 512, 1);
		newBuf.len = comc.subMS950String(bytes, 513, 1);
	}

	/***********************************************************************/
	class buf2 {
		String slipNo;
		String cardNo;
		String cntlAreaCode;
		String oldValidTo;
		String status;
		String reasonCode;
		String validTo;
		String stockNo;
		String pvki;
		String pvv;
		String cvv;
		String cvv2;
		String bankId;
		String fileDate;
		String errorCode;
		String rejectCode;
		String activeFlag;
		String pin;
		String filler;
		String endFlag;
		String len;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(slipNo, 4);
			rtn += comc.fixLeft(cardNo, 16);
			rtn += comc.fixLeft(cntlAreaCode, 4);
			rtn += comc.fixLeft(oldValidTo, 4);
			rtn += comc.fixLeft(status, 1);
			rtn += comc.fixLeft(reasonCode, 2);
			rtn += comc.fixLeft(validTo, 4);
			rtn += comc.fixLeft(stockNo, 6);
			rtn += comc.fixLeft(pvki, 1);
			rtn += comc.fixLeft(pvv, 4);
			rtn += comc.fixLeft(cvv, 3);
			rtn += comc.fixLeft(cvv2, 3);
			rtn += comc.fixLeft(bankId, 2);
			rtn += comc.fixLeft(fileDate, 6);
			rtn += comc.fixLeft(errorCode, 1);
			rtn += comc.fixLeft(rejectCode, 2);
			rtn += comc.fixLeft(activeFlag, 1);
			rtn += comc.fixLeft(pin, 6);
			rtn += comc.fixLeft(filler, 30);
			rtn += comc.fixLeft(endFlag, 1);
			rtn += comc.fixLeft(len, 1);
			return rtn;
		}
	}

	void splitBuf2(String str) throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes("MS950");
		chgBuf.slipNo = comc.subMS950String(bytes, 0, 4);
		chgBuf.cardNo = comc.subMS950String(bytes, 4, 16);
		chgBuf.cntlAreaCode = comc.subMS950String(bytes, 20, 4);
		chgBuf.oldValidTo = comc.subMS950String(bytes, 24, 4);
		chgBuf.status = comc.subMS950String(bytes, 28, 1);
		chgBuf.reasonCode = comc.subMS950String(bytes, 29, 2);
		chgBuf.validTo = comc.subMS950String(bytes, 31, 4);
		chgBuf.stockNo = comc.subMS950String(bytes, 35, 6);
		chgBuf.pvki = comc.subMS950String(bytes, 41, 1);
		chgBuf.pvv = comc.subMS950String(bytes, 42, 4);
		chgBuf.cvv = comc.subMS950String(bytes, 46, 3);
		chgBuf.cvv2 = comc.subMS950String(bytes, 49, 3);
		chgBuf.bankId = comc.subMS950String(bytes, 52, 2);
		chgBuf.fileDate = comc.subMS950String(bytes, 54, 6);
		chgBuf.errorCode = comc.subMS950String(bytes, 60, 1);
		chgBuf.rejectCode = comc.subMS950String(bytes, 61, 2);
		chgBuf.activeFlag = comc.subMS950String(bytes, 63, 1);
		chgBuf.pin = comc.subMS950String(bytes, 64, 6);
		chgBuf.filler = comc.subMS950String(bytes, 70, 30);
		chgBuf.endFlag = comc.subMS950String(bytes, 100, 1);
		chgBuf.len = comc.subMS950String(bytes, 101, 1);
	}
	// *******************************************************************
}
