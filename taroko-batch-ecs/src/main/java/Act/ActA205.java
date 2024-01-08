/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/12/26  V1.00.00    Ryan     program initial                            *
 *  112/05/18  V1.00.01    Ryan     明細增加p_seqno欄位                        *
 *  112/05/22  V1.00.02    Ryan     TCB的回覆代碼修改                          *
 *  112/07/05  V1.00.03    Ryan     新增 一扣判斷, 三扣判斷 及 新增三扣        *
 *  112/07/12  V1.00.04    Ryan     新增1扣回覆eMail功能                       *
 *  112/07/17  V1.00.05    Ryan     調整1扣回覆eMail格式                       *
 *  112/08/15  V1.00.06    Ryan     BATCH_NO前8碼改為business_date             *   
 *  112/08/25  V1.00.07    Ryan     UTF8 改 MS950                              *      
 *  112/08/27  V1.00.08    Simon    發email條件判斷調整                        *      
 *  112/09/01  V1.00.09    Ryan     updateActChkautopay 增加   ori_transaction_amt,deduct_amt_1,deduct_amt_2 欄位 ,調整回覆碼內容            *      
 ******************************************************************************/

package Act;

import java.nio.file.Paths;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

/*自行自動扣繳媒體回饋處理程式*/
public class ActA205 extends AccessDAO {

	public static final boolean DEBUG_MODE = false;

	private final String PROGNAME = "接收自行台幣自扣回覆檔  112/09/01 V1.00.09";
	private static final String DATA_FOLDER = "/media/act/";
	private static final String CRDATACREA = "CRDATACREA";
	private static final String DATA_FORM = "CARDM06";
	private final static String COL_SEPERATOR = "|&";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommString comStr = new CommString();
	CommDate comDate = new CommDate();
	String fileName = "ECSAUTRET.DAT";
	String prgmId = "ActA205";
	String hModUser = "";
	long hModSeqno = 0;
	String hCallBatchSeqno = "";
	String hModPgm = "";

	String hmCurpModPgm = "";
	String hmCurpModTime = "";
	String hmCurpModUser = "";
	long hmCurpModSeqno = 0;

	String hBusiBusinessDate = "";
	String hCkapAcctType = "";
	String hCkapAcctKey = "";
	String hCkapFromMark = "";
	String hCkapSendUnit = "";
	String hCkapCommerceType = "";
	String hCkapDataType = "";
	String hCkapDescCode = "";
	double hCkapTransactionAmt = 0;
	String hTempOrignStatusCode = "";
	String hCkapIdPSeqno = "";
	String hCkapChiName = "";
	String hCkapCreateDate = "";
	String hAgnnAutopayDeductDays = "";
	String hCkapRowid = "";
	String hIdnoChiName = "";
	String hIdnoCellarPhone = "";
	String hIdnoHomeAreaCode1 = "";
	String hIdnoHomeTelNo1 = "";
	String hIdnoHomeTelExt1 = "";
	String hApdlPayDate = "";
	String hCkapPSeqno = "";
	String hCkapStatusCode = "";
	double hApdlPayAmt = 0;
	int hTempSmsFlag = 0;
	String hCkapAutopayAcctNo = "";
	String hCkapAutopayId = "";
	String hCkapAutopayIdCode = "";
	String hA051ErrType = "";
	String hApbtBatchNo = "";
	String hApdlSerialNo = "";
	String hAperErrorReason = "";
	String hAperErrorRemark = "";
	long hTempBatchNoSeq = 0;
	String hTempBatchNo = "";
	int hApbtBatchTotCnt = 0;
	double hApbtBatchTotAmt = 0;
	String hSmidMsgId = "";
	String hSmidMsgDept = "";
	String hSmidMsgSendFlag = "";
	String hSmidMsgSelAcctType = "";
	String hSmidMsgUserid = "";
	String hApbtModSeqno = "";
	String hSmdlCellphoneCheckFlag = "";
	String hSmdlVoiceFlag = "";
	String hSmdlMsgDesc = "";
	String hWdayThisLastpayDate = "";
	String hAcnoStmtCycle = "";
	String hAcnoAcctStatus = "";
	String hSmidMsgSelAmt01 = "";
	String hCkapEnterAcctDate = "";
	String hCardno = "";
	int hAgnnSmsDeductDays = 0;
	String str600 = "";
	int nSerialNo = 0;
	int totalCnt = 0;
	String temstr1 = "";
	String chargeBackFlag = "";
	String isDigitalAcno = "";
	int smsMsgIdCnt = 0;
	String msgPgm = "";
	String hCorpIdNo = "";
	String hIdnoEMailAddr = "";
	double hOriTransactionAmt = 0;
	double hDcMinPay = 0;
	String hThisAcctMonth = "";
	int mailCount = 0;
	String rowOfTXT = "";
	double hTempAmtf = 0;

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================
			if (args.length > 2) {
				comc.errExit("Usage : ActA205 [business_date]", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

			hBusiBusinessDate = "";
			if ((args.length == 1) && (args[0].length() == 8)) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hBusiBusinessDate = sGArgs0;
			}
			selectPtrBusinday();
			selectSmsMsgId();

			checkFopen();
			if(readFile() == 0) {
				return 0;
			}
			renameFile();
			if (hApbtBatchTotCnt != 0)
				insertActPayBatch();

			showLogMessage("I", "", String.format("      本日處理扣款總筆數 [%d]", totalCnt));

			String fileFolder = Paths.get(comc.getECSHOME(), DATA_FOLDER).toString();
			String datFileName = String.format("%s__%s.dat", DATA_FORM, hBusiBusinessDate);
			sendMail(datFileName, fileFolder);
			procFTP(datFileName, fileFolder);
			moveFile(datFileName, fileFolder);
			// ==============================================
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}  finally {
			finalProcess();
		}
	}

	/***********************************************************************/
	void checkFopen() throws Exception {

		temstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		if (comc.fileMove(String.format("/crdataupload/%s", fileName), temstr1) == false) {
//			comcr.errRtn(String.format("檔案錯誤 error[/crdataupload/%s]", fileName), "", hCallBatchSeqno);
		}
		return;
	}

	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
		String tmpstr2 = String.format("%s/media/act/backup/%s.%s", comc.getECSHOME(), fileName, sysDate + sysTime);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileName + "] 已移至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		sqlCmd = "select decode( cast(? as varchar(8)) , '',business_date, ? ) h_busi_business_date";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += "fetch first 1 rows only ";
		setString(1, hBusiBusinessDate);
		setString(2, hBusiBusinessDate);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("h_busi_business_date");
		}

	}

	/***********************************************************************/
	void selectSmsMsgId() throws Exception {
		hSmidMsgId = "";
		hSmidMsgDept = "";
		hSmidMsgSendFlag = "";
		hSmidMsgSelAcctType = "";
		hSmidMsgUserid = "";
		hSmidMsgSelAmt01 = "";
		hSmidMsgSendFlag = "N";
		extendField = "msgid.";
		sqlCmd = "select msg_id,";
		sqlCmd += " msg_dept,";
		sqlCmd += " msg_send_flag,";
		sqlCmd += " decode(ACCT_TYPE_SEL,'','0',ACCT_TYPE_SEL) h_smid_msg_sel_acct_type,"; // msg_sel_acct_type
		sqlCmd += " msg_userid, ";
		sqlCmd += " msg_pgm ";
		sqlCmd += "  from sms_msg_id  ";
		sqlCmd += " where msg_pgm in ('ActA205','ActA205-2')  ";
		sqlCmd += " and decode(msg_send_flag,'','N',msg_send_flag) ='Y' ";
		sqlCmd += " and msg_id in ('1808','3808') ";
		smsMsgIdCnt = selectTable();
		if (smsMsgIdCnt == 0) {
			showLogMessage("I", "", "--簡訊暫停發送 (SMS_P010)");
			hSmidMsgSendFlag = "";
		}
	}

	/***********************************************************************/
	void insertActPayBatch() throws Exception {
		daoTable = "act_pay_batch";
		setValue("batch_no", hApbtBatchNo);
		setValueInt("batch_tot_cnt", hApbtBatchTotCnt);
		setValueDouble("batch_tot_amt", hApbtBatchTotAmt);
		setValue("crt_user", "AIX");
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("trial_user", "AIX");
		setValue("trial_date", sysDate);
		setValue("trial_time", sysTime);
		setValue("confirm_user", "AIX");
		setValue("confirm_date", sysDate);
		setValue("confirm_time", sysTime);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", "AIX");
		setValue("mod_pgm", prgmId);
		insertTable();
		// if (dupRecord.equals("Y")) { //★ modified on 2019/07/15
		if (!dupRecord.equals("Y")) {
			return;
		}

		daoTable = "act_pay_batch";
		updateSQL = " batch_tot_cnt = batch_tot_cnt + ?,";
		updateSQL += " batch_tot_amt = batch_tot_amt + ?,";
		updateSQL += " mod_time      = sysdate,";
		updateSQL += " mod_pgm       = ? ";
		whereStr = "where batch_no = ? ";
		setInt(1, hApbtBatchTotCnt);
		setDouble(2, hApbtBatchTotAmt);
		setString(3, prgmId);
		setString(4, hApbtBatchNo);
		updateTable();

	}

	/***********************************************************************/
	int readFile() throws Exception {
		String temp1Date = "";
		nSerialNo = 0;

		selectActPayBatch();
		hApbtBatchTotCnt = 0;
		hApbtBatchTotAmt = 0;

		if (openBinaryInput(temstr1) == false) {
			showLogMessage("I", "", "ERROR : 檔案檔案不存在[" + temstr1 + "]");
			return 0;
		}

		int readlen = 0;
		byte[] bytes = new byte[202];
		while ((readlen = readBinFile(bytes)) > 0) {
			str600 = new String(bytes, 0, readlen, "MS950");
//            str600 = comc.rtrim(str600);
			if (str600.length() < 180)
				continue;

			totalCnt++;
			if ((totalCnt % 5000) == 0) {
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
			}

			if (nSerialNo == 0) {
				getFirstLine();
				nSerialNo++;
				continue;
			} else {
				if (str600.substring(0, 1).equals("D")) {
					getField();
				} else
					break;
			}

//            if(selectCrdCard() == 1) {
//            	continue;
//            }
			if (hCkapPSeqno.trim().length() == 0) {
				continue;
			}

			if (selectActChkautopay() == 1) {
				hA051ErrType = "0";
				insertActA005r1();
				if (chargeBackFlag.equals("Y")) {
					hAperErrorReason = "102";
					hAperErrorRemark = "AUT1 act_chkautopay not found";
					insertActPayError();
				}
				continue;
			} else {
				if (!comStr.left(hTempOrignStatusCode, 2).equals("99")) {
					hA051ErrType = "1";
					insertActA005r1();

					if (chargeBackFlag.equals("Y")) {
						hAperErrorReason = "101";
						hAperErrorRemark = "AUT1 duplicated update act_chkautopay";
						insertActPayError();
					}
					continue;
				}
			}
			hTempSmsFlag = 0;
			/*showLogMessage("I", "", String.format(" getAutopayCounts-1 [%s]", hSmidMsgSendFlag));*/
			selectPtrWorkday();
			/*showLogMessage("I", "", String.format(" getAutopayCounts-1.1 [%s]", hAcnoStmtCycle));
			showLogMessage("I", "", String.format(" getAutopayCounts-1.2 [%s]", hWdayThisLastpayDate));
			showLogMessage("I", "", String.format(" getAutopayCounts-1.3 [%s]", hApdlPayDate));*/
			
			int count = getAutopayCounts(hAcnoStmtCycle, hApdlPayDate);
			/*showLogMessage("I", "", String.format(" getAutopayCounts-2 [%d]", count));*/
			msgPgm = "";
			if (count == 1)
				msgPgm = "ActA205";
			if (count == 3)
				msgPgm = "ActA205-2";

			for (int i = 0; i < smsMsgIdCnt; i++) {
				hSmidMsgId = "";
				hSmidMsgDept = "";
				hSmidMsgSendFlag = "";
				hSmidMsgSelAcctType = "";
				hSmidMsgUserid = "";
				if (msgPgm.length() > 0 && msgPgm.equals(getValue("msgid.msg_pgm", i))) {
					hSmidMsgId = getValue("msgid.msg_id", i);
					hSmidMsgDept = getValue("msgid.msg_dept", i);
					hSmidMsgSendFlag = getValue("msgid.msg_send_flag", i);
					hSmidMsgSelAcctType = getValue("msgid.h_smid_msg_sel_acct_type", i);
					hSmidMsgUserid = getValue("msgid.msg_userid", i);
					break;
				}
			}
			/*showLogMessage("I", "", String.format(" getAutopayCounts-3 [%s]", hSmidMsgSendFlag));
			showLogMessage("I", "", String.format(" getAutopayCounts-4 [%s]", chargeBackFlag));
			showLogMessage("I", "", String.format(" getAutopayCounts-5 [%f]", hCkapTransactionAmt));
			showLogMessage("I", "", String.format(" getAutopayCounts-6 [%f]", hApdlPayAmt));*/

			if ((!chargeBackFlag.equals("Y"))
				//|| ((chargeBackFlag.equals("Y")) && (hCkapTransactionAmt > hApdlPayAmt))) {
					|| ((chargeBackFlag.equals("Y")) && (hOriTransactionAmt > hApdlPayAmt))) {
				if (hCkapAcctType.equals("02")) {
//                    insertActCorpAutopay();
				} else {

					/*showLogMessage("I", "", String.format(" getAutopayCounts-7 [%s]", hSmidMsgId));*/

					if (hSmidMsgSendFlag.equals("Y")) {
						temp1Date = comcr.increaseDays(hWdayThisLastpayDate, -1);
						hWdayThisLastpayDate = temp1Date;
						temp1Date = comcr.increaseDays(hWdayThisLastpayDate, hAgnnSmsDeductDays);
						/*showLogMessage("I", "", String.format(" getAutopayCounts-8 [%s]", hSmidMsgSelAcctType));*/
						if ((chargeBackFlag.equals("Y") && hApdlPayAmt < hTempAmtf)
								|| chargeBackFlag.equals("N")) {
								procSmsMsgDtl();
								if (count == 1) {
									rowOfTXT += getRowOfDetail00();
									rowOfTXT += getRowOfDetail01();
									rowOfTXT += getRowOfDetail02();
									mailCount++;
								}
						}
						/*
						 * if ((hBusiBusinessDate.equals(temp1Date)) && (hAcnoAcctStatus.compareTo("3")
						 * < 0))
						 */

					}
				}
			}
			updateActChkautopay();
			if (hTempSmsFlag == 0)
				hIdnoCellarPhone = "";
			if (!chargeBackFlag.equals("Y")) {
				hA051ErrType = "2";
				insertActA005r1();
				continue;
			}
			/*
			 * 不能扣 , act_e004 處理 if (h_apdl_pay_amt>0) { update_act_acct();
			 * update_act_acct_curr(); }
			 */
			insertActPayDetail();
			// insert_onbat(); 新系統可用餘額直接抓 act_pay_detail 判斷
			hApbtBatchTotCnt++;
			hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
		} /*- while -*/
		closeBinaryInput();
		return 1;
	}

//    int selectCrdCard() throws Exception {
//    	hCkapPSeqno = "";
//    	sqlCmd = "select p_seqno from crd_card where card_no = ? and curr_code = '901' ";
//    	setString(1,hCardno);
//    	int recordCnt = selectTable();
//    	if(recordCnt==0) {
//    		 showLogMessage("I", "", String.format("selectCrdCard not found ,card_no = [%s]", hCardno));
//    		 return 1;
//    	}
//    	hCkapPSeqno = getValue("p_seqno");
//    	return 0;
//    }

	/***********************************************************************/
	void selectActPayBatch() throws Exception {
		String tempstr = "";
		hTempBatchNo = String.format("%s9001%c", hBusiBusinessDate, '%');

		sqlCmd = "select to_number(substr(max(batch_no),13,4))+1 h_temp_batch_no_seq ";
		sqlCmd += " from act_pay_batch  ";
		sqlCmd += "where batch_no like ? ";
		setString(1, hTempBatchNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTempBatchNoSeq = getValueLong("h_temp_batch_no_seq");
		}

		if (hTempBatchNoSeq != 0)
			tempstr = String.format("%12.12s%04d", hTempBatchNo, hTempBatchNoSeq);
		else
			tempstr = String.format("%12.12s0001", hTempBatchNo);

		hApbtBatchNo = tempstr;
	}

	/***********************************************************************/
	void getFirstLine() throws Exception {
		String stra = "";
		String strb = "";

		if (!str600.substring(0, 1).equals("H")) {
			comcr.errRtn(String.format("error 媒體格式錯誤[%s] !", str600), "", hCallBatchSeqno);
		}

		stra = comStr.mid(str600, 8, 7);
		strb = String.format("%08d", comcr.str2long(stra) + 19110000);

		if (comcr.str2Date(strb) == null) {
			comcr.errRtn(String.format("轉帳生效日期錯誤[%s] !", stra), "", hCallBatchSeqno);
		}
		hCkapEnterAcctDate = strb;
	}

	/***********************************************************************/
	@SuppressWarnings("unused")
	void getField() throws Exception {
		String stra = "";
		String strb = "";
		int int1;
		double doublea;
		hTempAmtf = 0;
		if (!str600.substring(0, 1).equals("D")) {
			comcr.errRtn(String.format("error 2 媒體格式錯誤 !"), "", hCallBatchSeqno);
		}

		stra = comStr.mid(str600, 8, 7);
		strb = String.format("%08d", comcr.str2long(stra) + 19110000);

		if (comcr.str2Date(strb) == null) {
			comcr.errRtn(String.format("入扣帳日期錯誤[%s] !", stra), "", hCallBatchSeqno);
		}
		hApdlPayDate = strb;

		hCkapAutopayAcctNo = comc.rtrim(comStr.mid(str600, 15, 13));
		hCkapAutopayId = comc.rtrim(comStr.bbMid(str600, 28, 10));
		stra = comc.rtrim(comStr.bbMid(str600, 43, 13)); //交易金額
		hTempAmtf = comcr.str2double(stra);
		hTempAmtf = hTempAmtf / 100;
		stra = comc.rtrim(comStr.mid(str600, 163, 13));
		hApdlPayAmt = comcr.str2double(stra);
		hApdlPayAmt = hApdlPayAmt / 100;
		hCkapPSeqno = comc.rtrim(comStr.bbMid(str600, 103, 10));
		isDigitalAcno = comc.rtrim(comStr.bbMid(str600, 158, 1));
		hCkapStatusCode = comc.rtrim(comStr.bbMid(str600, 176, 4));

		if (hApdlPayAmt > 0) {
			chargeBackFlag = "Y";
		} else {
			chargeBackFlag = "N";
		}
	}

	/***********************************************************************/
	int selectActChkautopay() throws Exception {
		hCkapAcctType = "";
		hCkapAcctKey = "";
		hCkapFromMark = "";
		hCkapSendUnit = "";
		hCkapCommerceType = "";
		hCkapDataType = "";
		hCkapDescCode = "";
		hCkapTransactionAmt = 0;
		hTempOrignStatusCode = "";
		hCkapIdPSeqno = "";
		hCkapChiName = "";
		hCkapCreateDate = "";
		hAgnnSmsDeductDays = 0;
		hCkapRowid = "";
		hIdnoChiName = "";
		hIdnoCellarPhone = "";
		hIdnoHomeAreaCode1 = "";
		hIdnoHomeTelNo1 = "";
		hIdnoHomeTelExt1 = "";
		hCorpIdNo = "";
		hIdnoEMailAddr = "";
		hOriTransactionAmt = 0;
		hDcMinPay = 0;

		sqlCmd = "select a.acct_type,";
		sqlCmd += " d.acct_key,";
		sqlCmd += " a.from_mark,";
		sqlCmd += " a.send_unit,";
		sqlCmd += " a.commerce_type,";
		sqlCmd += " a.data_type,";
		sqlCmd += " a.desc_code,";
		sqlCmd += " a.transaction_amt,";
		sqlCmd += " a.status_code,";
		sqlCmd += " a.id_p_seqno,";
		sqlCmd += " a.chi_name,";
		sqlCmd += " a.crt_date,";
		sqlCmd += " b.autopay_deduct_days,";
		sqlCmd += " decode(b.sms_deduct_days, 0, b.autopay_deduct_days, b.sms_deduct_days) h_agnn_sms_deduct_days,";
		sqlCmd += " a.rowid rowid,";
		sqlCmd += " c.id_no,";
		sqlCmd += " c.e_mail_addr,";
		sqlCmd += " c.chi_name,";
		sqlCmd += " c.cellar_phone,";
		sqlCmd += " a.ori_transaction_amt, ";
		sqlCmd += " a.dc_min_pay, ";
		sqlCmd += " decode(c.home_tel_no1, '', c.home_area_code2, c.home_area_code1) h_idno_home_area_code1,";
		sqlCmd += " decode(c.home_tel_no1, '', c.home_tel_no2   , c.home_tel_no1)    h_idno_home_tel_no1,";
		sqlCmd += " decode(c.home_tel_no1, '', c.home_tel_ext2  , c.home_tel_ext1)   h_idno_home_tel_ext1 ";
		sqlCmd += "  from ptr_actgeneral_n b, act_chkautopay a";
		sqlCmd += "  left outer join crd_idno c";
		sqlCmd += "    on c.id_p_seqno = a.id_p_seqno ";
		sqlCmd += "  left join act_acno d";
		sqlCmd += "    on d.acno_p_seqno = a.p_seqno";
		sqlCmd += " where a.enter_acct_date  = ?  ";
		sqlCmd += "   and decode(a.curr_code,'','901',a.curr_code) = '901'  ";
		sqlCmd += "   and a.p_seqno   = ?  ";
		sqlCmd += "   and a.acct_type = b.acct_type  ";
		sqlCmd += "   and a.from_mark = '01' ";
		setString(1, hApdlPayDate);
		setString(2, hCkapPSeqno);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hCkapAcctType = getValue("acct_type");
			hCkapAcctKey = getValue("acct_key");
			hCkapFromMark = getValue("from_mark");
			hCkapSendUnit = getValue("send_unit");
			hCkapCommerceType = getValue("commerce_type");
			hCkapDataType = getValue("data_type");
			hCkapDescCode = getValue("desc_code");
			hCkapTransactionAmt = getValueDouble("transaction_amt");
			hTempOrignStatusCode = getValue("status_code");
			hCkapIdPSeqno = getValue("id_p_seqno");
			hCkapChiName = getValue("chi_name");
			hCkapCreateDate = getValue("crt_date");
			hAgnnAutopayDeductDays = getValue("autopay_deduct_days");
			hAgnnSmsDeductDays = getValueInt("h_agnn_sms_deduct_days");
			hCkapRowid = getValue("rowid");
			hIdnoChiName = getValue("chi_name");
			hIdnoCellarPhone = getValue("cellar_phone");
			hIdnoHomeAreaCode1 = getValue("h_idno_home_area_code1");
			hIdnoHomeTelNo1 = getValue("h_idno_home_tel_no1");
			hIdnoHomeTelExt1 = getValue("h_idno_home_tel_ext1");
			hCorpIdNo = getValue("id_no");
			hIdnoEMailAddr = getValue("e_mail_addr");
			hOriTransactionAmt = getValueDouble("ori_transaction_amt");
			hDcMinPay = getValueDouble("dc_min_pay");
		} else
			return (1);
		return (0);
	}

	/***********************************************************************/
	void insertActPayError() throws Exception {
		hApdlSerialNo = String.format("%07d", nSerialNo);
		nSerialNo++;

		daoTable = "act_pay_error";
		setValue("batch_no", hApbtBatchNo);
		setValue("serial_no", hApdlSerialNo);
		setValue("p_seqno", hCkapPSeqno);
		setValue("acno_p_seqno", hCkapPSeqno);
		setValue("acct_type", hCkapAcctType);
		setValue("acct_key", hCkapAcctKey);
		setValueDouble("pay_amt", hApdlPayAmt);
		setValue("pay_date", hApdlPayDate);
		setValue("payment_type", "AUT1");
		setValue("error_reason", hAperErrorReason);
		setValue("error_remark", hAperErrorRemark);
		setValue("crt_user", "AIX");
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("mod_user", "AIX");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertActCorpAutopay() throws Exception {
		daoTable = "act_corp_autopay";
		setValue("business_date", hBusiBusinessDate);
		setValue("p_seqno", hCkapPSeqno);
		setValue("acct_type", hCkapAcctType);
		// setValue ("acct_key" , h_ckap_acct_key);
		setValueDouble("autopay_amt", hCkapTransactionAmt);
		setValueDouble("autopay_amt_bal", hCkapTransactionAmt - hApdlPayAmt);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_corp_autopay duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void selectPtrWorkday() throws Exception {
		hWdayThisLastpayDate = "";
		hAcnoStmtCycle = "";
		hAcnoAcctStatus = "";
		hThisAcctMonth = "";
		sqlCmd = "select a.this_lastpay_date,a.this_acct_month, ";
		sqlCmd += " b.stmt_cycle,";
		sqlCmd += " b.acct_status ";
		sqlCmd += "  from act_acno b, ptr_workday a  ";
		sqlCmd += " where b.acno_p_seqno =  ? ";
		sqlCmd += "   and a.stmt_cycle   = b.stmt_cycle ";
		setString(1, hCkapPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_workday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hWdayThisLastpayDate = getValue("this_lastpay_date");
			hAcnoStmtCycle = getValue("stmt_cycle");
			hAcnoAcctStatus = getValue("acct_status");
			hThisAcctMonth = getValue("this_acct_month");
		}

	}

	int getAutopayCounts(String cycle, String hApdlPayDate) throws Exception {
		sqlCmd = "select ";
		if ("01".equals(cycle))
			sqlCmd += " autopay_counts as autopay_counts ";
		if ("20".equals(cycle))
			sqlCmd += " autopay_counts_20 as autopay_counts ";
		if ("25".equals(cycle))
			sqlCmd += " autopay_counts_25 as autopay_counts ";
		sqlCmd += " from act_auto_comf where file_type= '1'";
		sqlCmd += " and enter_acct_date = ? ";
		setString(1, hApdlPayDate);
		int n = selectTable();
		if (n > 0) {
			return getValueInt("autopay_counts");
		}
		return 0;
	}

	/***********************************************************************/
	void procSmsMsgDtl() throws Exception {
		/*
		 * if (checkAcctType() != 0) { return; }
		 */

		int int1 = 0;
		String tmpstr1 = "";
//		String[] dataCode = { "118", "106", "205", "152", "E001", "E002", "E003", "E004", "178", "205", "203" };
//		String[] dataMsg = { "存款不足", "無此帳號/資料找不到", "帳號結清銷戶", "資料庫錯誤", "首筆日期錯誤", "總筆數不符", "總金額不符", "總手續費不符", "科子目錯誤",
//				"已銷戶", "死亡戶" };
		String[] dataCode = { "118", "106", "178", "152", "201", "203", "205", "206", "207", "211", "221", "234", "404", "462", "E001" ,"E002" ,"E003" ,"E004" ,"M20" ,"M21" ,"M22" };
		String[] dataMsg = { "存款不足", "無此帳號/資料找不到", "科子目錯誤", "資料庫錯誤", "止付", "死亡戶", "帳號結清銷戶", "轉籍移出", "靜止戶", "已消號", "設質", "警示戶", "拒往戶", "有逾期放款未繳", "首筆日期錯誤", "總筆數不符", "總金額不符", 
				"總手續費不符", "數存單筆超過限額", "數存單日累積超過限額", "數存單月累積超過限額" };
		
		hSmdlCellphoneCheckFlag = "Y";
		hSmdlVoiceFlag = "";

		if (hIdnoCellarPhone.length() != 10)
			hSmdlCellphoneCheckFlag = "N";
		else {
			for (int1 = 0; int1 < 10; int1++)
				if ((hIdnoCellarPhone.toCharArray()[int1] < '0') || (hIdnoCellarPhone.toCharArray()[int1] > '9')) {
					hSmdlCellphoneCheckFlag = "N";
					break;
				}
		}

		tmpstr1 = String.format("其他");
		for (int1 = 0; int1 < dataCode.length; int1++)
			if (hCkapStatusCode.equals(dataCode[int1])) {
				tmpstr1 = String.format("%s", dataMsg[int1]);
				break;
			}

		if (hSmdlCellphoneCheckFlag.equals("N")) {
			/*
			 * if (h_idno_home_tel_no1.len!=0) { remark
			 */
			hSmdlVoiceFlag = "Y";
			insertActAutopayVoice();
			/* } */
		}

		hSmdlMsgDesc = String.format("%s,%s,%s,%s", hSmidMsgUserid, hSmidMsgId, hIdnoCellarPhone, hIdnoChiName);
		/*
		 * hSmdlMsgDesc = String.format("%s,%s,%s,%s,合庫商銀,%s", hSmidMsgUserid,
		 * hSmidMsgId, hIdnoCellarPhone, hIdnoChiName, tmpstr1);
		 */
		hTempSmsFlag = 1;
		insertSmsMsgDtl();
	}

	/***********************************************************************/
	int checkAcctType() throws Exception {

		if (hSmidMsgSelAcctType.equals("0"))
			return (0);

		if (hSmidMsgSelAcctType.equals("1")) {
			sqlCmd = "select data_code " + "from sms_dtl_data " + "where table_name='SMS_MSG_ID' " + "and data_key = ? "
					+ "and data_type='1'";
			setString(1, "ActA205");
			int recordCnt = selectTable();
			for (int i = 0; i < recordCnt; i++) {
				String dataCode = getValue("data_code", i);
				if (dataCode.equals(hCkapAcctType)) {
					return 0;
				}
			}
		}

		return (1);
	}

	/***********************************************************************/
	void insertActAutopayVoice() throws Exception {
		daoTable = "act_autopay_voice";
		setValue("business_date", hBusiBusinessDate);
		setValue("p_seqno", hCkapPSeqno);
		setValue("acct_type", hCkapAcctType);
		// setValue("acct_key" , h_ckap_acct_key);
		setValue("chi_name", hIdnoChiName);
		setValue("home_area_code", hIdnoHomeAreaCode1);
		setValue("home_tel_no", hIdnoHomeTelNo1);
		setValue("home_tel_ext", hIdnoHomeTelExt1);
		setValue("ftp_date", "");
		setValue("ftp_flag", "N");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_autopay_voice duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertSmsMsgDtl() throws Exception {
		hModSeqno = comcr.getModSeq();

		daoTable = "sms_msg_dtl";
//        setValue("msg_seqno", hApbtModSeqno);
		setValue("msg_seqno", hModSeqno + "");
		setValue("msg_pgm", msgPgm);
		setValue("msg_dept", hSmidMsgDept);
		setValue("msg_userid", hSmidMsgUserid);
		setValue("msg_id", hSmidMsgId);
		setValue("cellar_phone", hIdnoCellarPhone);
		setValue("cellphone_check_flag", hSmdlCellphoneCheckFlag);
		setValue("chi_name", hIdnoChiName);
		setValue("msg_desc", hSmdlMsgDesc);
		setValue("p_seqno", hCkapPSeqno); // acct_p_seqno
		setValue("acct_type", hCkapAcctType);
		setValue("id_p_seqno", hCkapIdPSeqno);
		setValue("add_mode", "B");
		setValue("crt_date", sysDate); // add_date
		setValue("crt_time", sysTime); // add_time
		setValue("CRT_USER", "AIX"); // add_user_id
		setValue("apr_date", sysDate); // conf_date
		setValue("APR_USER", "AIX"); // conf_user_id
		setValue("apr_flag", "Y");
		setValue("SEND_FLAG", "N"); // msg_status
		setValue("proc_flag", "N"); //
		setValue("mod_user", "AIX");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_sms_msg_dtl duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void updateActChkautopay() throws Exception {
		daoTable = "act_chkautopay";
		updateSQL = "status_code      = ?,";
		updateSQL += " ori_transaction_amt = ?,";
		updateSQL += " deduct_amt_1 = ?,";
		updateSQL += " deduct_amt_2 = ?,";
		updateSQL += " transaction_amt = ?,";
		updateSQL += " sms_add_date    = decode(cast(? as varchar(8)), 0, sms_add_date, to_char(sysdate,'yyyymmdd')),";
		updateSQL += " is_digital_acno  = ?,";
		updateSQL += " mod_time        = sysdate,";
		updateSQL += " mod_pgm         = ?";
		whereStr = "where rowid      = ? ";
		setString(1, hCkapStatusCode);
		setDouble(2, hTempAmtf);
		setDouble(3, 0);
		setDouble(4, 0);
		setDouble(5, hApdlPayAmt);
		setInt(6, hTempSmsFlag);
		setString(7, isDigitalAcno);
		setString(8, javaProgram);
		setRowId(9, hCkapRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_chkautopay not found!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertActA005r1() throws Exception {
		daoTable = "act_a005r1";
		setValue("print_date", hApdlPayDate);
		setValue("enter_acct_date", hApdlPayDate);
		setValue("autopay_acct_no", hCkapAutopayAcctNo);
		setValue("from_mark", "01");
		setValue("send_unit", hCkapSendUnit);
		setValue("commerce_type", hCkapCommerceType);
		setValue("data_type", hCkapDataType);
		setValue("desc_code", hCkapDescCode);
		setValueDouble("transaction_amt", hApdlPayAmt);
		setValue("autopay_id", hCkapAutopayId);
		setValue("autopay_id_code", hCkapAutopayIdCode);
		setValue("p_seqno", hCkapPSeqno);
		setValue("acct_type", hCkapAcctType);
		setValue("id_p_seqno", hCkapIdPSeqno);
		setValue("chi_name", hCkapChiName);
		setValue("status_code", hCkapStatusCode);
		setValue("err_type", hA051ErrType);
		setValue("cellphone_no", hIdnoCellarPhone);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_a005r1 duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertActPayDetail() throws Exception {
		hApdlSerialNo = String.format("%07d", nSerialNo);
		nSerialNo++;

		daoTable = "act_pay_detail";
		setValue("batch_no", hApbtBatchNo);
		setValue("serial_no", hApdlSerialNo);
		setValue("p_seqno", hCkapPSeqno);
		setValue("acno_p_seqno", hCkapPSeqno);
		setValue("acct_type", hCkapAcctType);
		setValue("id_p_seqno", hCkapIdPSeqno);
		setValueDouble("pay_amt", hApdlPayAmt);
		setValue("pay_date", hApdlPayDate);
		setValue("payment_type", "AUT1");
		setValue("crt_user", "AIX"); // update_user
		setValue("crt_date", sysDate); // update_date
		setValue("crt_time", sysTime); // update_time
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", "AIX");
		setValue("mod_pgm", prgmId);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertOnbat() throws Exception {
		daoTable = "onbat_2ccas";
		setValue("trans_type", "16");
		setValueInt("to_which", 2);
		setValue("dog", sysDate);
		setValue("proc_mode", "B");
		setValueInt("proc_status", 0);
		// setValue("card_indicator", h_ckap_acct_type.equals("01") ? "1" : "2");
		setValue("card_catalog", hCkapAcctType.equals("01") ? "1" : "2");
		setValue("payment_type", hCkapIdPSeqno.equals("") ? "2" : "1");
		setValue("acct_type", hCkapAcctType);
		setValue("acno_p_seqno ", hCkapPSeqno);
		setValue("id_p_seqno ", hCkapIdPSeqno);// uf_idno_pseqno(card_hldr_id)
		setValue("trans_date", hApdlPayDate);
		setValueDouble("trans_amt", hApdlPayAmt);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_onbat_2ccas duplicate!", "", hCallBatchSeqno);
		}

	}

	/****
	 * 扣回覆eMail格式00
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getRowOfDetail00() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("00", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(hCorpIdNo, 11));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("01", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(hIdnoEMailAddr, 30));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("合作金庫銀行信用卡自動扣款不足約定應扣金額通知", 48));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("親愛的客戶您好！", 30));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft(" ", 17));
		sb.append(comc.fixLeft(" ", 60));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/***
	 * 扣回覆eMail格式01
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getRowOfDetail01() throws Exception {
		String thisAcctMonth = comDate.toTwDate(hThisAcctMonth + "01");
		String dateY = comStr.left(thisAcctMonth, 3);
		String dateM = comStr.mid(thisAcctMonth, 3, 2);
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("01", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("　　台端", 8));
		sb.append(comc.fixLeft(String.format("%03d", comStr.ss2int(dateY)), 3));
		sb.append(comc.fixLeft("年", 2));
		sb.append(comc.fixLeft(dateM, 2));
		sb.append(comc.fixLeft("月之「合作金庫銀行」信用卡帳款應繳總額為", 40));
		sb.append(comc.fixRight(comStr.numFormat(hOriTransactionAmt, "#,###"), 9));
		sb.append(comc.fixLeft("元，最低應繳金額為", 18));
		sb.append(comc.fixRight(comStr.numFormat(hDcMinPay, "#,###"), 9));
		sb.append(comc.fixLeft("元，於約定之金融機構存款帳戶自動扣繳不足約定應扣金額，", 54));
		sb.append(comc.fixLeft("請儘速繳納帳款，", 18));
		sb.append(comc.fixLeft(" ", 27));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/**
	 * 扣回覆eMail格式02
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getRowOfDetail02() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(comc.fixLeft("02", 2));
		sb.append(COL_SEPERATOR);
		sb.append(comc.fixLeft("如已繳納，請忽略本通知。", 210));
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}

	/***
	 * 扣回覆eMail功能
	 * 
	 * @param fileFolder
	 * @param datFileName
	 * @throws Exception
	 */
	void sendMail(String datFileName, String fileFolder) throws Exception {
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return;
		}
		showLogMessage("I", "", "扣回覆eMail功能，開始產生檔案......");

		if (mailCount == 0) {
			showLogMessage("I", "", "扣回覆eMail功能，無資料可寫入檔案");
		} else {
			byte[] tmpBytes = rowOfTXT.getBytes("MS950");
			writeBinFile(tmpBytes, tmpBytes.length);
		}
		showLogMessage("I", "", String.format("扣回覆eMail功能，產生檔案完成！，共產生%d筆資料", mailCount));
		closeBinaryOutput();
	}

	void procFTP(String hdrFileName, String fileFolder) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = CRDATACREA; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		String ftpCommand = String.format("mput %s", hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName(CRDATACREA, ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void moveFile(String datFileName1, String fileFolder1) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder1, "/backup", datFileName1).toString();

		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName1 + "]備份失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已備份至 [" + tmpstr2 + "]");
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		ActA205 proc = new ActA205();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
