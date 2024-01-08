/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/10/26  V1.00.00    Ryan     program initial                           *
 *  112/05/17  V1.00.01    Ryan     調整回覆碼格式與邏輯                                                                                *
 *  112/07/05  V1.00.02    Ryan     新增 一扣判斷, 三扣判斷 及 新增三扣                                                          *
 *  112/07/12  V1.00.03    Ryan     新增1扣回覆eMail功能                                                                              *
 *  112/07/17  V1.00.04    Ryan     調整1扣回覆eMail格式                                                                         *
 *  112/07/20  V1.00.05    Ryan     eMail功能TCB沒提供,先mark掉                                                        *
 *  112/08/15  V1.00.06    Ryan     BATCH_NO前8碼改為business_date               *      
 *  112/09/01  V1.00.07    Ryan     updateActChkautopay 增加   ori_transaction_amt  *                                       *
 *  112/09/28  V1.00.08    Simon    cellar_phone 空值時處理錯誤更正 in procSmsMsgDtl()*      
 ******************************************************************************/

package Act;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
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

/*雙幣自動扣繳媒體回饋處理*/
public class ActA215 extends AccessDAO {

	public static final boolean DEBUG_MODE = false;

	private final String PROGNAME = "雙幣自動扣繳媒體回饋處理  112/09/28 V1.00.08";
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
	String prgmId = "ActA215";
	String fileName = "ECSAUTRETD.DAT";
	String hModUser = "";
	long hModSeqno = 0;
	String hCallBatchSeqno = "";
	String hModPgm = "";

	String hBusiBusinessDate = "";
	String hCkapCurrCode = "";
	String hTempX02 = "";
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
	String hCkapId = "";
	String hCkapIdCode = "";
	String hCkapChiName = "";
	String hCkapCreateDate = "";
	int hAgnnAutopayDeductDays = 0;
	int hAgnnSmsDeductDays = 0;
	String hCkapRowid = "";
	String hIdnoChiName = "";
	String hIdnoCellarPhone = "";
	String hIdnoHomeAreaCode1 = "";
	String hIdnoHomeTelNo1 = "";
	String hIdnoHomeTelExt1 = "";
	String hApdlPayDate = "";
	String hCkapPSeqno = "";
	String hCkapStatusCode = "";
	String hckapStatusCodeOri = "";
	double hApdlPayAmt = 0;
	int hTempSmsFlag = 0;
	double hCkapDcMinPay = 0;
	double hApdlDcPayAmt = 0;
	String hCkapAutopayAcctNo = "";
	String hCkapAutopayId = "";
	String hCkapActNol = "";
	double hCkapAmt1 = 0;
	double hA051CurrCodeRate = 0;
	double hDcPayAmt1 = 0;
	double hDcPayAmt2 = 0;
	String hCkapAutopayIdCode = "";
	String hA051ErrType = "";
	String hApbtBatchNo = "";
	String hApdlSerialNo = "";
	String hAperErrorReason = "";
	String hAperErrorRemark = "";
	double h901Amt = 0;
	long hTempBatchNoSeq = 0;
	String hTempBatchNo = "";
	long hApbtBatchTotCnt = 0;
	double hApbtBatchTotAmt = 0;
	String hTempCurrCode = "";
	double tempDouble = 0;
	String hSmidMsgId = "";
	String hSmidMsgDept = "";
	String hSmidMsgSendFlag = "";
	String hSmidMsgSelAcctType = "";
	String hSmidMsgUserid = "";
	String hSmidMsgSelAmt01 = "";
	String hApbtModSeqno = "";
	String hSmdlCellphoneCheckFlag = "";
	String hSmdlVoiceFlag = "";
	String hSmdlMsgDesc = "";
	String hWdayThisLastpayDate = "";
	String hAcnoStmtCycle = "";
	String hAcnoAcctStatus = "";
	String hCkapEnterAcctDate = "";
	double hTtttDcExchangeRate = 0;
	String chargeBackFlag = "";
	int hInt1 = 0;

	String tmpstr = "";
	int totalRgt = 0;
	int totalCnt = 0;
	int currIdx = 0;
	int rtn = 0;
	double totalAmt1 = 0;
	double totalAmt2 = 0;
	int nSerialNo = 0;
	double hTempAmtf = 0;
	double hExchangeRate = 0;
	String str600 = "";
	String temstr1 = "";
	String tempX14 = "";

	String[] currArray = new String[30];
	long[] batchArray = new long[30];
	long[] seqArray = new long[30];
	long[] cntArray = new long[30];
	double[] amtArray = new double[30];
	double[] amtDcArray = new double[30];

	buf1 recvData = new buf1();
	int smsMsgIdCnt = 0;
	String msgPgm = "";
	String hCorpIdNo = "";
	String hIdnoEMailAddr = "";
	double hOriTransactionAmt = 0;
	double hDcMinPay = 0;
	String hThisAcctMonth = "";
	int mailCount = 0;
	String rowOfTXT = "";

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
				comc.errExit("Usage : ActA105 [business_date]", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

//            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

//            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);

			hBusiBusinessDate = "";
			if ((args.length == 1) && (args[0].length() == 8)) {
				String sGArgs0 = "";
				sGArgs0 = args[0];
				sGArgs0 = Normalizer.normalize(sGArgs0, java.text.Normalizer.Form.NFKD);
				hBusiBusinessDate = sGArgs0;
			}
			selectPtrBusinday();
			selectSmsMsgId();

			for (int i = 0; i < 30; i++) {
				currArray[i] = "";
				batchArray[i] = 0;
				seqArray[i] = 0;
				cntArray[i] = 0;
				amtArray[i] = 0;
				amtDcArray[i] = 0;
			}

			if(checkFopen() == 0) {
				return 0;
			}
			readFile();
			renameFile();
			if (hApbtBatchTotCnt != 0) {
				insertActPayBatch();
			}

			showLogMessage("I", "", String.format("      本日處理扣款總筆數 [%d][%d]", totalCnt, totalRgt));

			String fileFolder = Paths.get(comc.getECSHOME(), DATA_FOLDER).toString();
			String datFileName = String.format("%s__%s.dat", DATA_FORM, hBusiBusinessDate);
//			sendMail(datFileName, fileFolder);
//			procFTP(datFileName, fileFolder);
//			moveFile(datFileName, fileFolder);
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}finally {
			finalProcess();
		}
	}

	/***********************************************************************/
	void selectPtrBusinday() throws Exception {
		sqlCmd = "select decode( cast(? as varchar(8)) , '' ,business_date, ?) h_busi_business_date";
		sqlCmd += "  from ptr_businday  ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, hBusiBusinessDate);
		setString(2, hBusiBusinessDate);
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		hBusiBusinessDate = getValue("h_busi_business_date");

	}

	/***********************************************************************/
	int checkFopen() throws Exception {
		tmpstr = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
		tmpstr = Normalizer.normalize(tmpstr, java.text.Normalizer.Form.NFKD);
		if (comc.fileMove(String.format("/crdataupload/%s", fileName), tmpstr) == false) {
//			comcr.errRtn(String.format("檔案錯誤 error[/crdataupload/%s]", fileName), "", hCallBatchSeqno);
		}

		temstr1 = String.format("%s/media/act/%s", comc.getECSHOME(), fileName);
		temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
		int f = openInputText(temstr1);
		if (f == -1) {
			showLogMessage("I", "", "ERROR : 檔案檔案不存在[" + temstr1 + "]");
			return 0;
		}
		showLogMessage("I", "", String.format("path = %s", temstr1));
		closeInputText(f);
		return 1;
	}

	/***********************************************************************/
	void selectSmsMsgId() throws Exception {
		extendField = "msgid.";
		sqlCmd = "select msg_id,";
		sqlCmd += " msg_dept,";
		sqlCmd += " msg_send_flag,";
		sqlCmd += " decode(ACCT_TYPE_SEL,'','Y',ACCT_TYPE_SEL) h_smid_msg_sel_acct_type,";
		sqlCmd += " msg_userid, ";
		sqlCmd += " msg_pgm ";
		sqlCmd += "  from sms_msg_id  ";
		sqlCmd += " where msg_pgm in ('ActA205','ActA215')  ";
		sqlCmd += "   and decode(msg_send_flag,'','N',msg_send_flag) ='Y' ";
		sqlCmd += " and msg_id in ('1808','3809') ";
		smsMsgIdCnt = selectTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", "--簡訊暫停發送 (SMS_P010)");
			hSmidMsgSendFlag = "";
			return;
		}

	}

	/***********************************************************************/
	void readFile() throws Exception {
		String temp1Date = "";
		String stra = "";

		nSerialNo = 0;

		/*
		 * select_act_pay_batch();
		 */
		hApbtBatchTotCnt = 0;
		hApbtBatchTotAmt = 0;

		int readlen = 0;
		byte[] bytes = new byte[202];
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(temstr1));
		while ((readlen = br.read(bytes, 0, bytes.length)) > 0) {
			str600 = new String(bytes, 0, readlen, "MS950");
			if (str600.length() < 120)
				continue;

			totalCnt++;
			if ((totalCnt % 5000) == 0 || totalCnt == 1) {
				showLogMessage("I", "", String.format("Process record[%d]", totalCnt));
			}

			getField();

			rtn = selectActChkautopay();
			/*
			 * lai test rtn = 0;
			 */
			if (rtn == 1) {
				hA051ErrType = "0";
				insertActA105r1();
				if (chargeBackFlag.equals("Y")) {
					hAperErrorReason = "102";
					hAperErrorRemark = "AUT1 act_chkautopay not found";
					insertActPayError();
				}
				continue;
			} else {
				if (comc.getSubString(hTempOrignStatusCode, 0, 2).equals("99") == false) {
					hA051ErrType = "1";
					insertActA105r1();

					if (chargeBackFlag.equals("Y")) {
						hAperErrorReason = "101";
						hAperErrorRemark = "AUT1 duplicated update act_chkautopay";
						insertActPayError();
					}
					continue;
				}
			}
			hTempSmsFlag = 0;

			hSmidMsgSendFlag = "Y";
			/*showLogMessage("I", "", String.format(" getAutopayCounts-1[%s]", hSmidMsgSendFlag));*/

			selectPtrWorkday();

			int count = getAutopayCounts(hAcnoStmtCycle, hWdayThisLastpayDate);
			/*showLogMessage("I", "", String.format(" getAutopayCounts-2 [%d]", count));*/
			msgPgm = "";
			if (count == 1)
				msgPgm = "ActA205";
			if (count == 3)
				msgPgm = "ActA215";
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
					|| ((chargeBackFlag.equals("Y")) && (hCkapTransactionAmt > hApdlPayAmt))) {
				if (hCkapAcctType.equals("02")) {
//                    insertActCorpAutopay();
				} else if (hSmidMsgSendFlag.equals("Y")) {

					/*showLogMessage("I", "", String.format(" getAutopayCounts-7 [%s]", hSmidMsgId));*/

					if (hSmidMsgSendFlag.equals("Y")) {
						hWdayThisLastpayDate = comcr.increaseDays(hWdayThisLastpayDate, -1);
						temp1Date = comcr.increaseDays(hWdayThisLastpayDate, hAgnnSmsDeductDays);
						/*showLogMessage("I", "", String.format(" getAutopayCounts-8 [%s]", hSmidMsgSelAcctType));*/

						/*
						 * if ((hBusiBusinessDate.equals(temp1Date)) && (hAcnoAcctStatus.compareTo("3")
						 * < 0)) {
						 */
						/*showLogMessage("I", "", String.format(" getAutopayCounts-9 [%s]", chargeBackFlag));*/
						
						if ((chargeBackFlag.equals("Y") && hApdlPayAmt < hTempAmtf)
								|| chargeBackFlag.equals("N")) {
							procSmsMsgDtl();
							if (count == 1) {
//									rowOfTXT += getRowOfDetail00();
//									rowOfTXT += getRowOfDetail01();
//									rowOfTXT += getRowOfDetail02();
//									mailCount++;
							}
						}
					}
				}
			}

			updateActChkautopay();

			if (hTempSmsFlag == 0)
				hIdnoCellarPhone = "";

			if (!chargeBackFlag.equals("Y")) {
				hA051ErrType = "2";
				insertActA105r1();
				continue;
			}
			/*
			 * 不能扣 , act_e004 處理 if(h_apdl_pay_amt>0) { update_act_acct();
			 * update_act_acct_curr(); }
			 */
			if (insertActPayDetail() == 1)
				continue;
			// insert_onbat(); 新系統可用餘額直接抓 act_pay_detail 判斷
			hApbtBatchTotCnt++;
			hApbtBatchTotAmt = hApbtBatchTotAmt + hApdlPayAmt;
		} /*- while -*/

//        stra = comc.rtrim(str600.substring(18, 18 + 10));
//        if (totalAmt1 != comcr.str2long(stra)) {
//            comcr.errRtn(String.format("error 3 第一次扣帳總金額格式錯誤=[%f][%d][%s]", totalAmt1, comcr.str2long(stra), stra),
//                    "", hCallBatchSeqno);
//        }
		br.close();
	}

	/***********************************************************************/
	void getField() throws Exception {
		String stra = "";
		String strb = "";

		if (!str600.substring(0, 1).equals("2")) {
			comcr.errRtn(String.format("error 2 媒體格式錯誤 !"), "", hCallBatchSeqno);
		}

		hCkapCurrCode = "";
		hApdlPayDate = "";
		hCkapAutopayId = "";
		hCkapStatusCode = "";
		hckapStatusCodeOri = "";
		hTempAmtf = 0;

		recvData.splitBuf1(str600);

		stra = recvData.procDate;
		strb = String.format("%08d", comcr.str2long(stra) + 19110000);

		if (comcr.str2Date(strb) == null) {
			comcr.errRtn(String.format("入扣帳日期錯誤[%s] !", stra), "", hCallBatchSeqno);
		}
		hApdlPayDate = strb;

		hCkapAutopayId = recvData.id;

		hTempX02 = recvData.currCode;
		hExchangeRate = 0;
		sqlCmd = "select curr_code ";
		sqlCmd += "  from ptr_currcode  ";
//        sqlCmd += " where curr_code_gl = ? ";
		sqlCmd += " where curr_eng_name = ? ";
		setString(1, hTempX02);
		selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_currcode not found!", "", hCallBatchSeqno);
		}
		hCkapCurrCode = getValue("curr_code");

		hCkapAutopayAcctNo = recvData.acctNof;
		hTempAmtf = comcr.str2double(recvData.amtf) / 100; //本次應扣款金額
//        hCkapDcMinPay = comcr.str2double(recvData.minAmtf) / 100;
		hCkapActNol = recvData.acctNol;
		// h_ckap_amt_1 = comcr.str2double(recv_data.amt_l) / 100;
		hCkapAmt1 = comcr.str2double(recvData.amtl); // mantis 0002672 modified
		// h_a051_curr_code_rate = comcr.str2double(recv_data.curr_rate) / 10000;
		hA051CurrCodeRate = comcr.str2double(recvData.currRate) / 10000; // mantis 0002672 modified

		stra = recvData.deductAmt1;
		hDcPayAmt1 = comcr.str2double(stra) / 100;
//        totalAmt1 = totalAmt1 + comcr.str2double(stra);

//        stra = recvData.deductAmt2;
//        hDcPayAmt2 = comcr.str2double(stra) / 100;
//        totalAmt2 = totalAmt2 + comcr.str2double(stra);

		hApdlPayAmt = hDcPayAmt1 + hDcPayAmt2;
		hApdlDcPayAmt = hDcPayAmt1 + hDcPayAmt2;
		hCkapPSeqno = recvData.pSeqno;
		hckapStatusCodeOri = recvData.respCode;
		hCkapStatusCode = hckapStatusCodeOri;
		if (hApdlPayAmt > 0) {
			chargeBackFlag = "Y";
		} else {
			chargeBackFlag = "N";
		}

		selectPtrCurrRate();
		h901Amt = hApdlPayAmt * hTtttDcExchangeRate;
		// comcr.comm_curr_amt("901", h_901_amt, 0);
		h901Amt = comcr.commCurrAmt("901", h901Amt, 0);

		if (chargeBackFlag.equals("Y"))
			chkCurrCode();

	}

	/***********************************************************************/
	void selectPtrCurrRate() throws Exception {
		// 雙幣卡匯率檔
		hTtttDcExchangeRate = 0;
		sqlCmd = "select exchange_rate ";
		sqlCmd += "  from ptr_curr_rate  ";
		sqlCmd += " where curr_code = ? ";
		setString(1, hCkapCurrCode);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_curr_rate not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hTtttDcExchangeRate = getValueDouble("exchange_rate");
		}
	}

	/***********************************************************************/
	void chkCurrCode() throws Exception {
		for (int int1 = 1; int1 <= currIdx; int1++) {
			if (currArray[int1].equals(hCkapCurrCode)) {
				cntArray[int1]++;
				amtArray[int1] = amtArray[int1] + h901Amt;
				amtDcArray[int1] = amtDcArray[int1] + hApdlDcPayAmt;
				return;
			}
		}
		currIdx++;
		selectActPayBatch();
		currArray[currIdx] = hCkapCurrCode;
		batchArray[currIdx] = comc.str2long(hApbtBatchNo);
		cntArray[currIdx] = 1;
		amtArray[currIdx] = h901Amt;
		amtDcArray[currIdx] = hApdlDcPayAmt;

	}

	/***********************************************************************/
	void selectActPayBatch() throws Exception {
		String tempstr = "";

		hTempBatchNo = String.format("%s9001%c", hBusiBusinessDate, '%');

		sqlCmd = "select to_number(substr(max(batch_no),13,4)) h_temp_batch_no_seq ";
		sqlCmd += "  from act_pay_batch  ";
		sqlCmd += " where batch_no like ? ";
		setString(1, hTempBatchNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTempBatchNoSeq = getValueInt("h_temp_batch_no_seq");
		}

		if (hTempBatchNoSeq != 0)
			tempstr = String.format("%12.12s%04d", hTempBatchNo, hTempBatchNoSeq + currIdx);
		else
			tempstr = String.format("%12.12s%04d", hTempBatchNo, currIdx);

		hApbtBatchNo = tempstr;
	}

	/***********************************************************************/
	int selectActChkautopay() throws Exception {
		int readcnt = 0; // added on 20190717
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
		hCkapId = "";
		hCkapIdCode = "";
		hCkapChiName = "";
		hCkapCreateDate = "";
		hAgnnAutopayDeductDays = 0;
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
		sqlCmd += " c.id_no,";
		sqlCmd += " c.id_no_code,";
		sqlCmd += " a.chi_name,";
		sqlCmd += " b.autopay_deduct_days,";
		sqlCmd += " decode(b.sms_deduct_days,0,b.autopay_deduct_days, b.sms_deduct_days) h_agnn_sms_deduct_days,";
		sqlCmd += " a.rowid rowid,";
		sqlCmd += " c.chi_name,";
		sqlCmd += " c.crt_date,";
		sqlCmd += " c.cellar_phone,";
		sqlCmd += " c.e_mail_addr,";
		sqlCmd += " a.ori_transaction_amt,";
		sqlCmd += " a.dc_min_pay,";
		sqlCmd += " decode(c.home_tel_no1,'',c.home_area_code2,c.home_area_code1) h_idno_home_area_code1,";
		sqlCmd += " decode(c.home_tel_no1,'',c.home_tel_no2   ,c.home_tel_no1)    h_idno_home_tel_no1,";
		sqlCmd += " decode(c.home_tel_no1,'',c.home_tel_ext2  ,c.home_tel_ext1)   h_idno_home_tel_ext1 ";
		sqlCmd += "  from ptr_actgeneral_n b, act_chkautopay a ";
		sqlCmd += "  left join crd_idno c";
		sqlCmd += "    on c.id_p_seqno = a.id_p_seqno";
		sqlCmd += "  left join act_acno d";
		sqlCmd += "    on d.acno_p_seqno = a.p_seqno";
		sqlCmd += " where a.enter_acct_date = ?  ";
		sqlCmd += "   and a.p_seqno    = ?  ";
		sqlCmd += "   and a.curr_code  = ?  ";
		sqlCmd += "   and a.acct_type  = b.acct_type  ";
		sqlCmd += "   and a.from_mark  = '01' ";
		setString(1, hApdlPayDate);
		setString(2, hCkapPSeqno);
		setString(3, hCkapCurrCode);
		// int cursorIndex = openCursor();
		// while (fetchTable(cursorIndex)) {
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			readcnt++;
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
			hCkapId = getValue("id_no");
			hCkapIdCode = getValue("id_code");
			hCkapChiName = getValue("chi_name");
			hCkapCreateDate = getValue("crt_date");
			hAgnnAutopayDeductDays = getValueInt("autopay_deduct_days");
			// h_agnn_sms_deduct_days = getValueInt("autopay_deduct_days");
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
		}
		// closeCursor(cursorIndex);

//      return (0); modified on 2019/07/17
		if (readcnt > 0) {
			return (0);
		} else {
			return (1);
		}

	}

	/***********************************************************************/
	void insertActPayError() throws Exception {

		nSerialNo++;
		tmpstr = String.format("%07d", nSerialNo);
		hApdlSerialNo = tmpstr;

		searchCurrCode();

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
		setValue("CURR_CODE", hCkapCurrCode);
		setValueDouble("DC_PAY_AMT", hApdlDcPayAmt);
		setValue("error_reason", hAperErrorReason);
		setValue("error_remark", hAperErrorRemark);
		setValue("crt_user", "AIX");
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("mod_user", "AIX");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_pay_error duplicate!", "", hCallBatchSeqno);
		}

	}

//    /***********************************************************************/
//    void insertActCorpAutopay() throws Exception {
//        daoTable = "act_corp_autopay";
//        setValue("business_date", hBusiBusinessDate);
//        setValue("p_seqno", hCkapPSeqno);
//        setValue("acct_type", hCkapAcctType);
//        setValueDouble("autopay_amt", hCkapTransactionAmt);
//        setValueDouble("autopay_amt_bal", hCkapTransactionAmt - hApdlPayAmt);
//        setValue("mod_time", sysDate + sysTime);
//        setValue("mod_pgm", javaProgram);
//        insertTable();
//        if (dupRecord.equals("Y")) {
//            comcr.errRtn("insert_act_corp_autopay duplicate!", "", hCallBatchSeqno);
//        }
//
//    }

	/***********************************************************************/
	void selectPtrWorkday() throws Exception {
		hWdayThisLastpayDate = "";
		hAcnoStmtCycle = "";
		hAcnoAcctStatus = "";
		hThisAcctMonth = "";
		sqlCmd = "select a.this_lastpay_date,a.this_acct_month,";
		sqlCmd += " b.stmt_cycle,";
		sqlCmd += " b.acct_status ";
		sqlCmd += "  from act_acno b,ptr_workday a  ";
		sqlCmd += " where b.acno_p_seqno    = ?  ";
		sqlCmd += "   and a.stmt_cycle = b.stmt_cycle ";
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

	int getAutopayCounts(String cycle, String thisLastpayDate) throws Exception {
		sqlCmd = "select ";
		if ("01".equals(cycle))
			sqlCmd += " autopay_counts as max_autopay_counts ";
		if ("20".equals(cycle))
			sqlCmd += " autopay_counts_20 as max_autopay_counts ";
		if ("25".equals(cycle))
			sqlCmd += " autopay_counts_25 as max_autopay_counts ";
		sqlCmd += " from act_auto_comf where file_type= '3'";
		sqlCmd += " and enter_acct_date = ? ";
		/* setString(1,thisLastpayDate); */
		setString(1, hApdlPayDate);
		int n = selectTable();
		if (n > 0) {
			return getValueInt("max_autopay_counts");
		}
		return 0;
	}

	/***********************************************************************/
	int checkAcctType() throws Exception {

		if (hSmidMsgSelAcctType.equals("0"))
			return (0);

		if (hSmidMsgSelAcctType.equals("1")) {
			sqlCmd = "select data_code " + "from sms_dtl_data " + "where table_name='SMS_MSG_ID' " + "and data_key = ? "
					+ "and data_type='1'";
			setString(1, "ActA215");
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
	void procSmsMsgDtl() throws Exception {
		/*
		 * if (checkAcctType() != 0) { return; }
		 */

		int int1 = 0;
		String tmpstr1 = "";
		String[] dataCode = { "106", "118", "124", "203", "205", "206", "207", "404", "K59", "K60", "XXX", "1261",
				"1262", "1263", "102", "103", "XX5", "3671", "212", "234", "284", "M20", "M21", "M22" };
		String[] dataMsg = { "無此帳號", "存款餘額不足", "ID錯誤", "繼承戶、死亡戶", "結清戶", "轉籍移出", "銷號戶", "拒往戶", "連續三次", "暫時停扣", "資料錯誤",
				"幣別代號錯誤", "幣別匯率有誤", "幣別匯率有誤", "資料錯誤", "扣款執行日非當天營業日", "單筆結匯逾五十萬元", "客戶統編不一致", "非正常帳戶", "警示帳戶", "監控處分帳戶",
				"數位存款超過單筆限額", "數位存款超過當日累計限額", "數位存款超過當月累計限額" };

		hSmdlCellphoneCheckFlag = "Y";
		hSmdlVoiceFlag = "";

		if (hIdnoCellarPhone.length() != 10) {
			hSmdlCellphoneCheckFlag = "N";
		} else {
  		for (int1 = 0; int1 < 10; int1++) {
	  		if ((hIdnoCellarPhone.toCharArray()[int1] < '0') || 
	  		    (hIdnoCellarPhone.toCharArray()[int1] > '9')) {
		  		hSmdlCellphoneCheckFlag = "N";
			  	break;
			  }
  		}
		}

/***
		tmpstr1 = String.format("其他");
		for (int1 = 0; int1 < dataCode.length; int1++)
			if (hCkapStatusCode.equals(dataCode[int1])) {
				tmpstr1 = String.format("%*.*s", dataMsg[int1]);
				break;
			}
***/
		tmpstr = String.format("%s,%s,%s,%s", hSmidMsgUserid, hSmidMsgId, hIdnoCellarPhone, hIdnoChiName);
		/*
		 * tmpstr = String.format("%s,%s,%s,%s,合庫商銀,%s", hSmidMsgUserid, hSmidMsgId,
		 * hIdnoCellarPhone, hIdnoChiName, tmpstr1);
		 */
		hSmdlMsgDesc = tmpstr;
		hTempSmsFlag = 1;
		insertSmsMsgDtl();
		/*
		 * if (selectSmsMsgDtl() == 0) insertSmsMsgDtl();
		 */
	}

	/***********************************************************************/
	int selectSmsMsgDtl() throws Exception {

		sqlCmd = "select 1 cnt";
		sqlCmd += "  from sms_msg_dtl  ";
		sqlCmd += " where id_p_seqno  = ?  ";
		sqlCmd += "   and send_flag != 'Y'  ";
		sqlCmd += " fetch first 1 rows only ";
		setString(1, hCkapIdPSeqno);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hInt1 = getValueInt("cnt");
		} else
			return (0);

		return (1);
	}

	/***********************************************************************/
	void insertSmsMsgDtl() throws Exception {
		hModSeqno = comcr.getModSeq();
		daoTable = "sms_msg_dtl";
		setValue("msg_seqno", hModSeqno + "");
		setValue("msg_pgm", msgPgm);
		setValue("msg_dept", hSmidMsgDept);
		setValue("msg_userid", hSmidMsgUserid);
		setValue("msg_id", hSmidMsgId);
		setValue("CELLAR_PHONE", hIdnoCellarPhone);// cellphone_no
		setValue("cellphone_check_flag", hSmdlCellphoneCheckFlag);
		setValue("CHI_NAME", hIdnoChiName);// holder_name
		setValue("msg_desc", hSmdlMsgDesc);
		setValue("acct_type", hCkapAcctType);
		setValue("p_seqno", hCkapPSeqno);
		setValue("id_p_seqno", hCkapIdPSeqno);
		setValue("ID_NO", hCkapId);
		setValue("add_mode", "B");
		setValue("CRT_DATE", sysDate);// add_date
		setValue("CRT_TIME", sysTime);// add_date
		setValue("CRT_USER", "AIX");// add_user_id
		setValue("APR_DATE", sysDate);// conf_date
		setValue("APR_USER", "AIX");// conf_user_id
		setValue("APR_FLAG", "Y");// approve_flag
		setValue("SEND_FLAG", "N");// msg_status "30"
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
		updateSQL = "status_code         = ?,";
		updateSQL += " status_code_ori    = ?,";
		updateSQL += " ori_transaction_amt    = ?,";
		updateSQL += " transaction_amt    = ?,";
		updateSQL += " sms_add_date       = decode(cast(? as varchar(8)),0,sms_add_date, to_char(sysdate,'yyyymmdd')),";
		updateSQL += " dc_min_pay         = ?,";
		updateSQL += " dc_transaction_amt = ?,";
		updateSQL += " dc_autopay_acct_no = ?,";
		updateSQL += " dc_autopay_id      = ?,";
		updateSQL += " act_no_l           = ?,";
		updateSQL += " amt_1              = ?,";
		updateSQL += " curr_rate          = ?,";
		updateSQL += " deduct_amt_1       = ?,";
		updateSQL += " deduct_amt_2       = ?,";
		updateSQL += " mod_time           = sysdate,";
		updateSQL += " mod_pgm            = ?";
		whereStr = "where rowid         = ? ";
		setString(1, hCkapStatusCode);
		setString(2, hckapStatusCodeOri);
		setDouble(3, hTempAmtf);
		setDouble(4, hApdlPayAmt);
		setInt(5, hTempSmsFlag);
		setDouble(6, hCkapDcMinPay);
		setDouble(7, hApdlDcPayAmt);
		setString(8, hCkapAutopayAcctNo);
		setString(9, hCkapAutopayId);
		setString(10, hCkapActNol);
		setDouble(11, hCkapAmt1);
		setDouble(12, hA051CurrCodeRate);
		setDouble(13, 0);
		setDouble(14, 0);
		setString(15, javaProgram);
		setRowId(16, hCkapRowid);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_act_chkautopay not found!", "", "");
		}

	}

	/***********************************************************************/
	void insertActA105r1() throws Exception {
		daoTable = "act_a005r1";
		setValue("print_date", hApdlPayDate);
		setValue("enter_acct_date", hApdlPayDate);
		setValue("autopay_acct_no", hCkapAutopayAcctNo);
		setValue("from_mark", "01");
		setValue("send_unit", hCkapSendUnit);
		setValue("commerce_type", hCkapCommerceType);
		setValue("data_type", hCkapDataType);
		setValue("desc_code", hCkapDescCode);
		// setValueDouble("transaction_amt", h_apdl_pay_amt);
		setValueDouble("transaction_amt", hTempAmtf); // modify by mantis 002306
		setValue("autopay_id", hCkapAutopayId);
		setValue("autopay_id_code", hCkapAutopayIdCode);
		setValue("p_seqno", hCkapPSeqno);
		setValue("acct_type", hCkapAcctType);
		// setValue("acct_key", h_ckap_acct_key);
		setValue("id_p_seqno", hCkapIdPSeqno);
		// setValue("id", h_ckap_id);
		// setValue("id_code", h_ckap_id_code);
		setValue("chi_name", hCkapChiName);
		setValue("status_code", hCkapStatusCode);
		setValue("status_code_ori", hckapStatusCodeOri);
		setValue("err_type", hA051ErrType);
		setValue("cellphone_no", hIdnoCellarPhone);
		setValue("curr_code", hCkapCurrCode);
		setValueDouble("curr_code_rate", hA051CurrCodeRate);
		setValue("act_no_l", hCkapActNol);
		setValueDouble("amt_1", hCkapAmt1);
		setValueDouble("curr_rate", hA051CurrCodeRate);
		setValueDouble("deduct_amt_1", hDcPayAmt1);
		setValueDouble("deduct_amt_2", hDcPayAmt2);
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_act_a005r1 duplicate!", "", "");
		}

	}

	/***********************************************************************/
	int insertActPayDetail() throws Exception {

		nSerialNo++;
		tmpstr = String.format("%07d", nSerialNo);
		hApdlSerialNo = tmpstr;

		totalRgt++;
		searchCurrCode();

		daoTable = "act_pay_detail";
		setValue("batch_no", hApbtBatchNo);
		setValue("serial_no", hApdlSerialNo);
		setValue("p_seqno", hCkapPSeqno);
		setValue("acno_p_seqno", hCkapPSeqno);
		setValue("acct_type", hCkapAcctType);
		setValue("id_p_seqno", hCkapIdPSeqno);
		setValueDouble("pay_amt", h901Amt);
		setValue("pay_date", hApdlPayDate);
		setValue("payment_type", "AUT1");
		setValue("CURR_CODE", hCkapCurrCode);
		setValueDouble("DC_PAY_AMT", hApdlDcPayAmt);
		setValue("crt_user", "AIX");
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", "AIX");
		setValue("mod_pgm", javaProgram);
		insertTable();
		if (dupRecord.equals("Y")) {
//            comcr.errRtn("insert_act_pay_detail duplicate!", "", hCallBatchSeqno);
			showLogMessage("I", "", String.format("insert_act_pay_detail duplicate! ,batch_no=[%s] ,serial_no=[%s]",
					hApbtBatchNo, hApdlSerialNo));

			return 1;
		}

		return 0;
	}

	/***********************************************************************/
	void searchCurrCode() throws Exception {
		for (int int1 = 1; int1 <= currIdx; int1++) {
			if (currArray[int1].equals(hCkapCurrCode)) {
				String tmp = String.valueOf(batchArray[int1]);
				if (tmp.length() >= 14)
					tempX14 = tmp;
				else
					tempX14 = String.format("%014d", batchArray[int1]);
				hApbtBatchNo = tempX14;
				seqArray[int1]++;
				tmpstr = String.format("%05d", seqArray[int1]);
				hApdlSerialNo = tmpstr;
			}
		}
	}

	/***********************************************************************/
	void insertActPayBatch() throws Exception {
		double tempDouble = 0;
		for (int int1 = 1; int1 < currIdx + 1; int1++) {
			hTempCurrCode = currArray[int1];
			String tmp = String.valueOf(batchArray[int1]);
			if (tmp.length() >= 14)
				tempX14 = tmp;
			else
				tempX14 = String.format("%014d", batchArray[int1]);
			hApbtBatchNo = tempX14;
			hApbtBatchTotCnt = cntArray[int1];
			hApbtBatchTotAmt = amtArray[int1];
			tempDouble = amtDcArray[int1];

			daoTable = "act_pay_batch";
			setValue("batch_no", hApbtBatchNo);
			setValueLong("batch_tot_cnt", hApbtBatchTotCnt);
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
			setValue("curr_code", hTempCurrCode);
			setValueDouble("dc_pay_amt", tempDouble);
			setValue("mod_time", sysDate + sysTime);
			setValue("mod_user", "AIX");
			setValue("mod_pgm", javaProgram);
			insertTable();
			if (!dupRecord.equals("Y")) {
				continue;
			}

			daoTable = "act_pay_batch";
			updateSQL = "batch_tot_cnt  = batch_tot_cnt + ?,";
			updateSQL += " batch_tot_amt = batch_tot_amt + ?,";
			updateSQL += " mod_time      = sysdate,";
			updateSQL += " mod_pgm       = ?";
			whereStr = "where batch_no = ? ";
			setLong(1, hApbtBatchTotCnt);
			setDouble(2, hApbtBatchTotAmt);
			setString(3, javaProgram);
			setString(4, hApbtBatchNo);
			updateTable();

		}
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
			byte[] tmpBytes = rowOfTXT.getBytes();
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
		ActA215 proc = new ActA215();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	/***********************************************************************/
	class buf1 {
		String procKind; // 資料辨別碼
		String procDate; // 扣款日
		String id; // 身分證編號
		String currCode; // 幣別
		String acctNof; // 外幣扣帳帳號
		String amtf; // 本次應扣款金額
		String acctNol; // 台幣扣帳帳號
		String amtl; // 台幣扣帳金額
		String currRate; // 扣帳匯率
		String deductAmt1;// 合計扣外幣金額
		String pSeqno; // 帳務流水號
		String filler1; // 保留欄
		String respCode; // 回應訊息

		void splitBuf1(String str) throws UnsupportedEncodingException {
			byte[] bytes = str.getBytes("MS950");
			procKind = comc.subMS950String(bytes, 0, 1);
			procDate = comc.subMS950String(bytes, 1, 7);
			id = comc.subMS950String(bytes, 8, 10);
			currCode = comc.subMS950String(bytes, 18, 3);
			acctNof = comc.subMS950String(bytes, 21, 13);
			amtf = comc.subMS950String(bytes, 34, 13);
			acctNol = comc.subMS950String(bytes, 47, 13);
			amtl = comc.subMS950String(bytes, 60, 13);
			currRate = comc.subMS950String(bytes, 73, 10);
			deductAmt1 = comc.subMS950String(bytes, 83, 13);
			pSeqno = comc.subMS950String(bytes, 96, 10);
			filler1 = comc.subMS950String(bytes, 106, 10);
			respCode = comc.subMS950String(bytes, 116, 4);
		}

	}

}
