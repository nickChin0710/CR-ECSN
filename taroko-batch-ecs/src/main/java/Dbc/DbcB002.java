/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/08  V1.00.01  Pino       Initial                                    *
* 109/04/14  V1.00.02  Wilson     h_error_flag = 12 -> 32、檢核調整                                    *
* 109/11/12  V1.01.00  yanghan       修改了變量名稱和方法名稱                                                                 *
* 109/11/25  V1.01.01  Wilson     err_code調整                                                                                         *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  111/04/19 V1.00.04  Wilson     mark chkStmtCycle、chkSourceCode            *
* 111/12/26  V1.00.05  Wilson     VD不檢核英文姓名                                                                                       *
* 112/03/08  V1.00.06  Wilson     where條件增加reject_code <> 'Y'               *
* 112/03/09  V1.00.07  Wilson     where條件調整為reject_code = ''               *
* 112/12/11  V1.00.08  Wilson     crd_item_unit不判斷卡種                                                                 *
*****************************************************************************/
package Dbc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class DbcB002 extends AccessDAO {
	private String progname = "VD後製卡/預製卡(分行整批製卡)資料檢核作業  112/12/11 V1.00.08";
	private Map<String, Object> resultMap;

	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	int debug = 1;
	int debugD = 1;

	String checkHome = "";
	String hCallErrorDesc = "";
	String hCallBatchSeqno = "";
	String hCallRProgramCode = "";
	String hTempUser = "";
	String hBusiBusinessDate = "";
	String hBusiChiDate = "";
	int totalCnt = 0;
	int totalErr = 0;
	String tmpChar1 = "";
	String tmpChar = "";
	double tmpDoub = 0;
	long tmpLong = 0;
	int tmpInt = 0;
	int count = 0;

	int hErrorFlag = 0;
	String emapBatchno = "";
	String emapRowid = "";
	String emapGroupCode = "";
	String emapCardType = "";
	String emapSourceCode = "";
	String emapApplyId = "";
	String emapApplyIdCode = "";
	String emapPmId = "";
	String emapPmIdCode = "";
	String emapBirthday = "";
	String emapStmtCycle = "";
	String emapmajorCardNo = "";
	String emapValidFm = "";
	String emapValidTo = "";
	String emapMajorValidFm = "";
	String emapMajorValidTo = "";
	String emapAcctType = "";
	String emapApplySource = "";
	String emapCrtBankNo = "";
	String emapVdBankNo = "";
	String emAppmBirthday = "";
	String emapCheckCode = "";
	String emapMajorChgFlag = "";
	String emapFinalFeeCode = "";
	String emapFeeCode = "";
	String emapCorpNo = "";
	String emapFeeReasonCode = "";
	int emapCreditLmt = 0;
	String emapRiskBankNo = "";
	String emapActNo = "";
	String emapCardcat = "";
	String newCardcat = "";
	String emapUnitCode = "";
	String emapIcFlag = "";
	String emapServiceCode = "";
	double emapRevolveIntRateYear = 0.0;
	String emapBusinessCode = "";
	String emapRegBankNo = "";
	String emapBranch = "";
	String emapEngName = "";
	String emapNcccType = "";
	String hAcnoIdPSeqno = "";
	String hTempId = "";
	String hOriIbmIdCode = "";
	int tSupCd = 0;
	int ptrExtnYear = 0;
	int hLineOfCreditAmt = 0;
	String hNewEndDate = "";
	String cardComboAcctNo = "";
	String idnoIdPSeqno = "";
	String idnoIdNoCode = "";
	String dcCurrCodeGl = "";
	String tCardIndicator = "";
	String tAcctType = "";
	int tFirstFee = 0;
	int tOtherFee = 0;
	double tSupRate = 0.00;
	int tSupEndMonth = 0;
	double tSupEndRate = 0.00;

	String hEriaRefIp = "";
	String hEriaPortNo = "";
	String hRtnStr = "";
	String hRtnDesc = "";
	int tempInt = 0;
	String tempX01 = "";
	String tempX02 = "";
	String tempX10 = "";
	String tempX011 = "";
	int corpFlag = 0;
	buf2[] acct1 = null;
	String pComboIndicator = "";
	int acctTot = 0;
	String hCardIndicator = "";
	String hCardComboIndicator = "";

	// appcbuf snd_strc = new appcbuf();
	// appcbuf rcv_strc = new appcbuf();

	// ************************************************************************

	public static void main(String[] args) throws Exception {
		DbcB002 proc = new DbcB002();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

	// ************************************************************************

	public int mainProcess(String[] args) {
		try {

			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			checkHome = comc.getECSHOME();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (args.length > 2) {
				String err1 = "DbcB002 請輸入 : callseqno";
				String err2 = "";
				System.out.println(err1);
				comc.errExit(err1, err2);
			}

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

			String checkHome = comc.getECSHOME();
			if (comcr.hCallBatchSeqno.length() > 6) {
				comcr.hCallParameterData = javaProgram;
				for (int i = 0; i < args.length; i++) {
					comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
				}
				if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
					comcr.hCallBatchSeqno = "no-call";
				}
			}

			comcr.hCallRProgramCode = this.getClass().getName();
			hTempUser = "";
			if (comcr.hCallBatchSeqno.length() == 20) {
				comcr.callbatch(0, 0, 1);
				selectSQL = " user_id ";
				daoTable = "ptr_callbatch";
				whereStr = "WHERE batch_seqno   = ?  ";

				setString(1, comcr.hCallBatchSeqno);
				int recCnt = selectTable();
				hTempUser = getValue("user_id");
			}
			if (hTempUser.length() == 0) {
				hTempUser = comc.commGetUserID();
			}

			dateTime();
			selectPtrBusinday();

			// select_ecs_ref_ip_addr();
			totalCnt = 0;
			totalErr = 0;
			selectDbcEmapTmp();

			comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "],Error=" + totalErr;
			showLogMessage("I", "", comcr.hCallErrorDesc);

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
		// ************************************************************************

	public void selectPtrBusinday() throws Exception {
		selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
		daoTable = "PTR_BUSINDAY";
		whereStr = "FETCH FIRST 1 ROW ONLY";

		int recordCnt = selectTable();

		if (notFound.equals("Y")) {
			String err1 = "select_ptr_businday error!";
			String err2 = "";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}

		hBusiBusinessDate = getValue("BUSINESS_DATE");
		long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
		hBusiChiDate = Long.toString(hLongChiDate);

		showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
	}

	// ************************************************************************
	public void selectDbcEmapTmp() throws Exception {

		selectSQL = " batchno           ,recno                 , "
				+ " apply_id            ,nvl(apply_id_code,'0') as apply_id_code, " + " birthday            ,"
				+ " pm_id               ,pm_id_code            , " + " risk_bank_no        ,reg_bank_no           , "
				+ " card_type           ,nvl(group_code,'0000') as group_code, "
				+ " source_code         ,credit_lmt            , " + " unit_code           ,stmt_cycle            , "
				+ " corp_no             ,nvl(corp_no_code,'0')  as corp_no_code, "
				+ " corp_act_flag       ,corp_assure_flag      , " + " valid_fm            ,valid_to              , "
				+ " online_mark         ,nvl(fee_code,'0') as fee_code, "
				+ " pm_birthday         ,major_card_no         , " + " major_valid_fm      ,major_valid_to        , "
				+ " major_chg_flag      ,cardcat               , " + " act_no              ,apply_no              , "
				+ " rowid      as rowid   , " + " revolve_int_rate_year ,business_code       , "
				+ " reg_bank_no         ,branch                , " + " eng_name            ,nccc_type             , "
				+ " acct_type           ,apply_source          , " + " crt_bank_no         ,vd_bank_no              ";
		daoTable = "dbc_emap_tmp";
		whereStr = "where (check_code = '' or check_code != '000') and reject_code = '' ";

		openCursor();

		while (fetchTable()) {
			initRtn();
			emapBatchno = getValue("batchno");
			emapGroupCode = getValue("group_code");
			if (emapGroupCode.trim().length() < 1)
				emapGroupCode = "0000";
			emapCardType = getValue("card_type");
			emapSourceCode = getValue("source_code");
			emapUnitCode = getValue("unit_code");
			emapStmtCycle = getValue("stmt_cycle");
			emapApplyId = getValue("apply_id");
			emapApplyIdCode = getValue("apply_id_code");
			if (emapApplyIdCode.trim().length() < 1)
				emapApplyIdCode = "0";
			emapPmId = getValue("pm_id");
			emapPmIdCode = getValue("pm_id_code");
			emapBirthday = getValue("birthday");
			emAppmBirthday = getValue("birthday");
			emapFeeCode = getValue("fee_code");
			if (emapFeeCode.trim().length() < 1)
				emapFeeCode = "0";
			emapCorpNo = getValue("corp_no");
			emapCreditLmt = getValueInt("credit_lmt");
			emapRiskBankNo = getValue("risk_bank_no");
			emapActNo = getValue("act_no");
			emapCardcat = getValue("cardcat");
			if (emapCardcat.length() < 2)
				emapCardcat = "01";
			emapValidFm = getValue("valid_fm");
			emapValidTo = getValue("valid_to");
			hNewEndDate = getValue("valid_to");
			emapRowid = getValue("rowid");
			emapRevolveIntRateYear = getValueDouble("revolve_int_rate_year");
			emapBusinessCode = getValue("business_code");
			emapRegBankNo = getValue("reg_bank_no");
			emapBranch = getValue("branch");
			emapEngName = getValue("eng_name");
			emapNcccType = getValue("nccc_type");
			emapAcctType = getValue("acct_type");
			emapApplySource = getValue("apply_source");
			emapCrtBankNo = getValue("crt_bank_no");
			emapVdBankNo = getValue("vd_bank_no");

			totalCnt++;
			processDisplay(5000); // every nnnnn display message
			if (debug == 1) {
				showLogMessage("I", "", "888 Beg id=[" + emapApplyId + "]" + "pm=[" + emapPmId + "]" + totalCnt);
				showLogMessage("I", "", "     group=[" + emapGroupCode + "]");
				showLogMessage("I", "", "      type=[" + emapCardType + "]");
				showLogMessage("I", "", "      cat =[" + emapCardcat + "]");
			}

			hErrorFlag = 0;

			if (emapActNo.length() == 0) {
				if (hErrorFlag == 0)
					hErrorFlag = 29;
			}

			if (emapApplySource.length() == 0) {
				if (hErrorFlag == 0)
					hErrorFlag = 30;
			}

			sqlCmd = "select count(*) rec_cnt ";
			sqlCmd += " from dbc_apply_source  ";
			sqlCmd += "where apply_source = ? ";
			setString(1, emapApplySource);
			int recordCnt = selectTable();
			recordCnt = getValueInt("rec_cnt");
			if (recordCnt == 0) {
				if (hErrorFlag == 0)
					hErrorFlag = 31;
			}
			
			emapApplyId = emapApplyId.trim();
			if (emapApplyId.length() == 8) {
				corpFlag = 1;
			}
			
			if (emapApplyId.length() > 0) {
				if (corpFlag != 1) {
					if (comm.isNumber(comc.getSubString(emapApplyId, 1, 2)) == true) {
						if (emapApplyId.substring(1, 1) != "8" || emapApplyId.substring(1, 1) == "9") {
							if (comc.idCheck(emapApplyId) != 0) { // 本國人
								if (hErrorFlag == 0)
									hErrorFlag = 32;
							}
						} else {
							if (!comc.isValidNewId(emapApplyId)) { // 外國人新式統號
								if (hErrorFlag == 0)
									hErrorFlag = 32;
							}
						}
					} else { // 外國人
						tempX10 = emapApplyId;
						convertNoRtn();
						tempX011 = String.format("%1.1s", emapApplyId.substring(9));
						if (!tempX01.equals(tempX011)) {
							if (hErrorFlag == 0)
								hErrorFlag = 32;
						}
					}
				}
			}
			
			int rtn = 0;
			getGblAcctType();
			rtn = getAcctType();
			if (rtn != 0) {
				hErrorFlag = 1;
			}
			
			if (emapApplySource.equals("R")) {
				rtn = checkCardDup(1);
				if (rtn > 0 && hErrorFlag == 0) {
					if (rtn == 2)
						hErrorFlag = 33;
					else if (rtn == 3)
						hErrorFlag = 34;
					else if (rtn == 4)
						hErrorFlag = 35;
					else
						hErrorFlag = 36;
				}
				rtn = checkEmbossDup(1);
				if (rtn > 0) {
					if (hErrorFlag == 0)
						hErrorFlag = 36;
				}

				// 新製卡英文名檢核
//				if (hErrorFlag == 0)
//					hErrorFlag = chkEngName();

				if (debug == 1)
					showLogMessage("D", "", " 888 3.06 step=[" + hErrorFlag + "] ");

			}
			
			if (emapApplySource.equals("P")) {
				rtn = checkCardDup(2);
				if (rtn > 0 && hErrorFlag == 0) {
					if (rtn == 2)
						hErrorFlag = 33;
					else if (rtn == 3)
						hErrorFlag = 34;
					else if (rtn == 4)
						hErrorFlag = 35;
					else
						hErrorFlag = 36;
				}
				rtn = checkEmbossDup(2);
				if (rtn > 0) {
					if (hErrorFlag == 0)
						hErrorFlag = 36;
				}
			}
			else {
				// 相同ID不同生日不可進件
				if (hErrorFlag == 0)
					hErrorFlag = chkBirthday(emapApplyId, emapBirthday);
			}

			// 檢核建檔分行
			if (hErrorFlag == 0)
				hErrorFlag = chkCrtBankNo();

			// 檢核記帳分行
			if (hErrorFlag == 0)
				hErrorFlag = chkVdBankNo();

			tmpInt = selectPtrGroupCard();
			if (tmpInt > 0) {
				hErrorFlag = tmpInt;
			}
			tmpInt = selectCrdItemUnit();
			if (tmpInt > 0) {
				hErrorFlag = tmpInt;
			}
			if (debug == 1)
				showLogMessage("D", "", " 888 3.00 step=[" + hErrorFlag + "] ");
			// 檢核效期日期,是否大於系統日期
			if (hErrorFlag == 0)
				hErrorFlag = chkData();

			if (debug == 1)
				showLogMessage("D", "", " 888 3.01 step=[" + hErrorFlag + "] ");

			if (debug == 1)
				showLogMessage("D", "", " 888 3.02 step=[" + hErrorFlag + "] ");

			// 檢核行業別是否存在
//            if (h_error_flag == 0)
//                h_error_flag = chk_business_code();

//            if (DEBUG == 1)
//                showLogMessage("D", "", " 888 3.03 step=[" + h_error_flag + "] ");

			// 檢核發卡分行是否存在
			if (hErrorFlag == 0)
				hErrorFlag = chkRegBankNo();

			if (debug == 1)
				showLogMessage("D", "", " 888 3.04 step=[" + hErrorFlag + "] ");

			// 檢核寄送分行是否存在
			if (hErrorFlag == 0)
				if (emapBranch.length() > 0) // V2.01.02
					hErrorFlag = chkBranch();

			if (debug == 1)
				showLogMessage("D", "", " 888 3.05 step=[" + hErrorFlag + "] ");

			// 檢核STMT_CYCLE是否存在
//			if (hErrorFlag == 0 && emapStmtCycle.trim().length() > 0) {
//				hErrorFlag = chkStmtCycle();
//			}

			// 檢核SOURCE_CODE是否存在
//			if (hErrorFlag == 0 && emapSourceCode.trim().length() > 0) {
//				tmpInt = chkSourceCode();
//				if (tmpInt > 0) {
//					hErrorFlag = 19;
//				}
//			}

			if (hErrorFlag > 0) {
				totalErr++;
				updateDbcEmapErr();
			} else
				updateDbcEmapTmp();

		}

	}

	// ************************************************************************
	public int selectPtrGroupCard() throws Exception {
		selectSQL = " count(*) as cnt ";
		daoTable = "ptr_group_card ";
		whereStr = "WHERE group_code =  ? " + "  and card_type  =  ? ";
		setString(1, emapGroupCode);
		setString(2, emapCardType);

		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			return (4);
		}
		recCnt = getValueInt("cnt");

		// 處理 ic_flag
		tmpInt = selectPtrGroupCardDtl();
		if (tmpInt > 0)
			return (tmpInt);

		return (0);
	}

	// ************************************************************************
	public int selectCrdItemUnit() throws Exception {
		selectSQL = " extn_year ";
		daoTable = "crd_item_unit ";
		whereStr = "WHERE unit_code = ? ";
		setString(1, emapUnitCode);

		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			return (20);
		}

		ptrExtnYear = getValueInt("extn_year");

		return (0);
	}

	// ************************************************************************
	public int selectPtrGroupCardDtl() throws Exception {

		if (debug == 1)
			// showLogMessage("D", "", " cardcat=[" + emap_cardcat + "]");
			// String h_cardcat_code = emap_cardcat.substring(0, 2);
			// if (h_cardcat_code.trim().length() < 2)
			// h_cardcat_code = "01";

			selectSQL = " count(*) as all_cnt ";
		daoTable = "ptr_group_card_dtl";
		whereStr = "WHERE group_code = ? " + " and card_type = ? ";
		setString(1, emapGroupCode);
		setString(2, emapCardType);

		int recCnt = selectTable();

		if (getValueInt("all_cnt") == 0)
			return (25);

		selectSQL = " ic_flag ,service_code , electronic_code ";
		daoTable = "crd_item_unit";
		whereStr = "WHERE unit_code = ? ";
		setString(1, emapUnitCode);

		recCnt = selectTable();

		if (notFound.equals("Y")) {
			return (20);
		}

		emapIcFlag = getValue("ic_flag");
		emapServiceCode = getValue("service_code");
		newCardcat = getValue("electronic_code");
		
		if(emapServiceCode.equals("")) {
			comcr.errRtn("服務碼不可為空白!", "認同集團碼 = " + emapUnitCode + "，卡種 = " + emapCardType, hCallBatchSeqno);
		}
		
		if (emapIcFlag.trim().length() == 0)
			emapIcFlag = "N";

		return (0);
	}

	// ***********************************************************************
	void selectEcsRefIpAddr() throws Exception {

		sqlCmd = "SELECT ref_ip, ";
		sqlCmd += "       port_no ";
		sqlCmd += "  FROM ecs_ref_ip_addr ";
		sqlCmd += " WHERE ref_ip_code = 'APPC' ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ecs_ref_ip_addr not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hEriaRefIp = getValue("ref_ip");
			hEriaPortNo = getValue("port_no");
		}
		if (debug == 1)
			showLogMessage("I", "", "  APPC Port=[" + hEriaRefIp + "]" + hEriaPortNo);
	}

	// ************************************************************************
	public int getGblAcctType(int idx) throws Exception {

		if (debugD == 1)
			showLogMessage("I", "", "  888 gbl=[" + idx + "]" + emapGroupCode + " " + emapCardType);

		selectSQL = "a.acct_type       , a.card_indicator ";
		daoTable = "ptr_acct_type a,ptr_prod_type b ";
		if (idx == 0) {
			whereStr = "WHERE b.group_code  = ? " + "  and b.card_type   = ? " + "  and a.acct_type   = b.acct_type "
					+ "FETCH FIRST 1 ROW ONLY  ";
			setString(1, emapGroupCode);
			setString(2, emapCardType);
		} else if (idx == 1) {
			whereStr = "WHERE b.group_code  = ? " + "  and a.acct_type   = b.acct_type " + "FETCH FIRST 1 ROW ONLY  ";
			setString(1, emapGroupCode);
		} else if (idx == 2) {
			whereStr = "WHERE b.card_type   = ? " + "  and a.acct_type   = b.acct_type " + "FETCH FIRST 1 ROW ONLY  ";
			setString(1, emapCardType);
		}

		int recCnt = selectTable();

		tAcctType = getValue("acct_type");
		tCardIndicator = getValue("card_indicator");

		if (notFound.equals("Y")) {
			if (debugD == 1)
				showLogMessage("I", "", "  chk type11.222=[ " + recCnt + " ]");
			return (1);
		}

		if (debugD == 1)
			showLogMessage("I", "", "  chk type11.2=[" + getValue("acct_type") + "]");
		return (0);
	}

	// ************************************************************************
	private int chkData() throws Exception {

		tmpChar = getValue("valid_fm");
		tmpChar1 = getValue("valid_to");
		if (debug == 1)
			showLogMessage("D", "", " 888 fm=[" + tmpChar + "]" + "[" + tmpChar1 + "]");
		if (comc.getSubString(tmpChar, 0, 6).compareTo(comc.getSubString(tmpChar1, 0, 6)) > 0) {
			return (23);
		}
		if (comc.getSubString(tmpChar, 0, 6).compareTo(comc.getSubString(sysDate, 0, 6)) < 0) {
			return (24);
		}
		if (comc.getSubString(tmpChar1, 0, 6).compareTo(comc.getSubString(sysDate, 0, 6)) < 0) {
			return (24);
		}

		return (0);
	}

	// ************************************************************************
	private int chkSourceCode() throws Exception {
		selectSQL = "count(*) as all_cnt";
		daoTable = "ptr_src_code";
		whereStr = "WHERE source_code              = ?   ";

		setString(1, emapSourceCode);

		int recCnt = selectTable();

		if (debugD == 1)
			showLogMessage("I", "", "  source=[" + getValueInt("all_cnt") + "]");
		if (getValueInt("all_cnt") == 0)
			return (1);

		return (0);
	}

	// ************************************************************************
	private int chkStmtCycle() throws Exception {

		selectSQL = "count(*) as all_cnt";
		daoTable = "ptr_workday";
		whereStr = "WHERE stmt_cycle = ? ";

		setString(1, emapStmtCycle);
		int recCnt = selectTable();
		if (debugD == 1)
			showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
		if (getValueInt("all_cnt") < 1) {
			return (7);
		}

		return (0);
	}

	// ************************************************************************
	private int chkAcctType() throws Exception {

		if (debugD == 1)
			showLogMessage("I", "", "  chk type11.1=[" + emapGroupCode + "]");

		if (emapGroupCode.trim().compareTo("0000") != 0) {
			selectSQL = "a.acct_type ";
			daoTable = "ptr_acct_type a,ptr_prod_type b";
			whereStr = "WHERE b.group_code  = ? " + "  and a.acct_type   = b.acct_type "
					+ "  and (b.card_type  = '' or b.card_type = '') ";

			setString(1, emapGroupCode);
			int recCnt = selectTable();
			if (debugD == 1)
				showLogMessage("I", "", "  chk type11.210=[" + recCnt + "]");
			if (getValue("acct_type").trim().length() > 0) {
				return (0);
			}
		}

		selectSQL = "a.acct_type ";
		daoTable = "ptr_acct_type a,ptr_prod_type b";
		whereStr = "WHERE b.card_type  = ? " + "  and a.acct_type  = b.acct_type ";

		setString(1, emapCardType);
		int recCnt = selectTable();
		if (debugD == 1)
			showLogMessage("I", "", "  chk type11.211=[" + recCnt + "]");
		if (notFound.equals("Y")) {
			return (3);
		}
		if (debugD == 1)
			showLogMessage("I", "", "  chk type2=[" + emapGroupCode + "]");
		tmpChar = getValue("acct_type").trim();
		if (emapAcctType.compareTo(tmpChar.trim()) != 0) {
			return (3);
		}

		return (0);
	}

	// ************************************************************************
	void getGblAcctType() throws Exception {
		String pAcctType = "";
		String pGroupCode = "";
		String pCardType = "";
		String pCardIndicator = "";
		String pFCurrencyFlag = "";

		sqlCmd = "select ";
		sqlCmd += "a.acct_type,";
		sqlCmd += "a.card_indicator,";
		sqlCmd += "a.f_currency_flag,";
		sqlCmd += "decode(b.group_code,'','0000',b.group_code)   as p_group_code,";
		sqlCmd += "b.card_type ";
		sqlCmd += " from dbp_acct_type a,dbp_prod_type b ";
		sqlCmd += "where b.acct_type = a.acct_type ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			String stderr = String.format("no gbl_acct_type found");
			comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
		} else {
			acct1 = new buf2[recordCnt];
		}
		if (debug == 1)
			showLogMessage("I", "", "SELECT acct_type cnt=" + recordCnt);

		for (int i = 0; i < recordCnt; i++) {
			acct1[i] = new buf2();
			pAcctType = getValue("acct_type", i);
			pCardIndicator = getValue("card_indicator", i);
			pComboIndicator = "Y";
			pFCurrencyFlag = getValue("f_currency_flag", i);
			pGroupCode = getValue("p_group_code", i);
			pCardType = getValue("card_type", i);

			acct1[i].acctType = pAcctType;
			acct1[i].groupCode = pGroupCode;
			acct1[i].cardType = pCardType;
			acct1[i].cardIndicator = pCardIndicator;
			acct1[i].comboIndicator = pComboIndicator;
			acct1[i].fCurrencyFlag = pFCurrencyFlag;
		}

		acctTot = recordCnt;

		return;
	}

	// ************************************************************************
	int getAcctType() throws Exception {
		emapAcctType = "";
		hCardIndicator = "";
		hCardComboIndicator = "";

		if ((emapGroupCode.length() > 0) && (!emapGroupCode.equals("0000"))) {
			for (int i = 0; i < acctTot; i++) {
				if (acct1[i].groupCode.equals(emapGroupCode)) {
					emapAcctType = acct1[i].acctType;
					hCardIndicator = acct1[i].cardIndicator;
					hCardComboIndicator = acct1[i].comboIndicator;
					/*
					 * if(acct1[i].card_type.equals(h_dcep_card_type)) { h_acct_type =
					 * acct1[i].acct_type; h_card_indicator = acct1[i].card_indicator;
					 * h_card_combo_indicator = acct1[i].combo_indicator; }
					 */
					return (0);
				}
			}
		}

		if (emapCardType.length() > 0) {
			for (int i = 0; i < acctTot; i++) {
				if (acct1[i].cardType.equals(emapCardType)) {
					emapAcctType = acct1[i].acctType;
					hCardIndicator = acct1[i].cardIndicator;
					hCardComboIndicator = acct1[i].comboIndicator;
					return (0);
				}
			}
		}

		if (emapAcctType.length() == 0)
			return (1);

		return (0);
	}

	// ************************************************************************
	public int updateDbcEmapErr() throws Exception {

		if (debug == 1)
			showLogMessage("D", "", " UPADTE Err=[" + hErrorFlag + "]" + totalErr);

		emapCheckCode = "D" + String.format("%02d", hErrorFlag);

		updateSQL = " check_code       = ? , " + " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , "
				+ " mod_pgm          = ?   ";
		daoTable = "dbc_emap_tmp";
		whereStr = "WHERE rowid       = ? ";

		setString(1, emapCheckCode);
		setString(2, sysDate + sysTime);
		setString(3, javaProgram);
		setRowId(4, emapRowid);

		int recCnt = updateTable();

		if (notFound.equals("Y")) {
			String err1 = "update_crd_emap_err error[not find]";
			String err2 = "";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}

		return (0);
	}

	// ************************************************************************
	public int selectActAcno(String ckPSeqno) throws Exception {
		selectSQL = " line_of_credit_amt ";
		daoTable = "act_acno";
		whereStr = "WHERE acno_p_seqno    =  ? ";

		setString(1, ckPSeqno);

		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			return (2);
		}

		return (0);
	}

	// ************************************************************************
	public int selectCrdIdno(String ckId, String ckIdCode) throws Exception {
		selectSQL = " birthday   ";
		daoTable = "crd_idno";
		whereStr = "WHERE id_no      =  ? " + "  and id_no_code =  ? ";
		setString(1, ckId);
		setString(2, ckIdCode);

		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			return (2);
		}

		return (0);
	}

	// ************************************************************************
//	public int chkIdBirthday(String ckId, String ckBirthday) throws Exception {
//
//		if (debug == 1)
//			showLogMessage("D", "", " CHK BIRTH 1=[" + ckId + "]" + ckBirthday);
//		selectSQL = " count(*)  as idno_cnt   ";
//		daoTable = "crd_idno";
//		whereStr = "WHERE id_no      =  ? " + "  and birthday   =  ? ";
//		setString(1, ckId);
//		setString(2, ckBirthday);
//
//		int recCnt = selectTable();
//
//		if (getValueInt("idno_cnt") < 1) {
//			selectSQL = " count(*)  as idno_cnt1  ";
//			daoTable = "crd_idno";
//			whereStr = "WHERE id_no      =  ? ";
//			setString(1, ckId);
//
//			recCnt = selectTable();
//			if (debug == 1)
//				showLogMessage("D", "", " CHK BIRTH 2=[" + ckId + "]" + getValueInt("idno_cnt1"));
//
//			if (getValueInt("idno_cnt1") > 0) {
//				return (15);
//			}
//		}
//
//		return (0);
//	}

	// ************************************************************************
	public int selectDbcIdnoB(String ckId, String ckBirthday) throws Exception {
		idnoIdPSeqno = "";
		idnoIdNoCode = "";

		selectSQL = " id_p_seqno , id_no_code ";
		daoTable = "dbc_idno";
		whereStr = "WHERE id_no      =  ? " + "  and birthday   =  ? ";
		setString(1, ckId);
		setString(2, ckBirthday);

		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			return (1);
		}

		idnoIdPSeqno = getValue("id_p_seqno");
		idnoIdNoCode = getValue("id_no_code");

		return (0);
	}

	// ************************************************************************
	public int chkBirthday(String ckId, String ckBirthday) throws Exception {
		String birthdayTmp = "";

		selectSQL = " birthday ";
		daoTable = "dbc_idno";
		whereStr = "WHERE id_no  =  ? ";
		setString(1, ckId);

		int recCnt = selectTable();

		if (notFound.equals("Y")) {
			return (0);
		} else {
			birthdayTmp = getValue("birthday");
			if (birthdayTmp.equals(ckBirthday)) {
				return (0);
			} else {
				return (28);
			}
		}

	}

	// ************************************************************************
	public int chkBusinessCode() throws Exception {

		selectSQL = " count(*) as all_cnt ";
		daoTable = "crd_message";
		whereStr = "WHERE  msg_type  =  ? " + "  and msg_value  =  ? ";
		setString(1, "BUS_CODE");
		setString(2, emapBusinessCode);

		int recCnt = selectTable();
		if (getValueInt("all_cnt") == 0)
			return (14);

		return (0);

	}

	// ************************************************************************
	public int chkRegBankNo() throws Exception {

		selectSQL = " count(*) as all_cnt ";
		daoTable = "gen_brn";
		whereStr = "WHERE  branch  =  ? ";
		setString(1, emapRegBankNo);

		int recCnt = selectTable();

		if (getValueInt("all_cnt") == 0)
			return (15);

		return (0);

	}

	// ************************************************************************
	public int chkCrtBankNo() throws Exception {

		selectSQL = " count(*) as all_cnt ";
		daoTable = "gen_brn";
		whereStr = "WHERE  branch  =  ? ";
		setString(1, emapCrtBankNo);

		int recCnt = selectTable();

		if (getValueInt("all_cnt") == 0)
			return (13);

		return (0);

	}

	// ************************************************************************
	public int chkVdBankNo() throws Exception {

		selectSQL = " count(*) as all_cnt ";
		daoTable = "gen_brn";
		whereStr = "WHERE  branch  =  ? ";
		setString(1, emapVdBankNo);

		int recCnt = selectTable();

		if (getValueInt("all_cnt") == 0)
			return (18);

		return (0);

	}

	// ************************************************************************
	public int chkBranch() throws Exception {

		selectSQL = " count(*) as all_cnt ";
		daoTable = "gen_brn";
		whereStr = "WHERE  branch  =  ? ";
		setString(1, emapBranch);

		int recCnt = selectTable();

		if (getValueInt("all_cnt") == 0)
			return (17);

		return (0);

	}

	// ************************************************************************
	public int chkEngName() throws Exception {
		int englen = 0;

		tmpChar = getValue("eng_name");
		if (debug == 1)
			showLogMessage("D", "", " 888 eng_name=[" + tmpChar + "]");
		if (tmpChar.trim().length() <= 0) {
			return (6);
		}

		for (englen = 0; englen < emapEngName.length(); englen++) {
			if (emapEngName.toCharArray()[englen] >= 65 && emapEngName.toCharArray()[englen] <= 90
					|| emapEngName.toCharArray()[englen] == 32 || emapEngName.toCharArray()[englen] == 0
					|| emapEngName.toCharArray()[englen] == 39 || emapEngName.toCharArray()[englen] == 44
					|| emapEngName.toCharArray()[englen] == 45 || emapEngName.toCharArray()[englen] == 46
					|| emapEngName.toCharArray()[englen] == 47) {
			} else {
				return (11);
			}
		}
		return (0);
	}

	// ************************************************************************
	public int updateDbcEmapTmp() throws Exception {

		if (cardComboAcctNo.trim().length() > 0)
			emapActNo = cardComboAcctNo;

		if (emapPmIdCode.trim().length() < 1)
			emapPmIdCode = "0";

		if (emapRiskBankNo.trim().length() < 1)
			emapRiskBankNo = "009";

		emapCheckCode = "000";

		updateSQL = " acct_type        = ? , " + " check_code       = ? , " + " pm_id_code       = ? , "
				+ " pm_birthday      = ? , " + " fee_reason_code  = ? , " + " ic_flag          = ? , "
				+ " service_code     = ? , " + " electronic_code  = ? , "
				+ " mod_time         = timestamp_format(?,'YYYYMMDDHH24MISS') , " + " mod_pgm          = 'DbcB002'";
		daoTable = "dbc_emap_tmp";
		whereStr = "WHERE rowid    = ? ";

		if (emapPmIdCode.length() == 0)
			emapPmIdCode = "0";

		if (debug == 1)
			showLogMessage("D", "", " UUUU  unit =[" + emapUnitCode + "]");
		setString(1, emapAcctType);
		setString(2, emapCheckCode);
		setString(3, emapPmIdCode);
		setString(4, emAppmBirthday);
		setString(5, emapFeeReasonCode);
		setString(6, emapIcFlag);
		setString(7, emapServiceCode);
		setString(8, newCardcat);
		setString(9, sysDate + sysTime);
		setRowId(10, emapRowid);

		int recCnt = updateTable();

		if (notFound.equals("Y")) {
			String err1 = "update_dbc_emap_tmp error[not find]";
			String err2 = "";
			comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
		}

		return (0);
	}

	// ************************************************************************
	private String getComboIndicator(String groupCode) throws Exception {
		selectSQL = "case when combo_indicator='' then 'N' else combo_indicator " + " end as combo_indicator ";
		daoTable = "ptr_group_code";
		whereStr = "WHERE group_code = ? ";

		setString(1, groupCode);
		int recCnt = selectTable();
		if (notFound.equals("Y")) {
			hErrorFlag = 1;
			return "";
		}

		tmpChar = getValue("combo_indicator");
		if (tmpChar.trim().length() == 0)
			return "N";

		return tmpChar;
	}

	// ************************************************************************
	void convertNoRtn() throws Exception {
		String aftConvert = "";
		String tmpConvert = "";
		int[] prefixN = { 1, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

		if (debug == 1)
			showLogMessage("I", "", "convert x10=" + tempX10);
		tempX01 = tempX10.substring(0, 1);
		convertDig();
		tmpConvert = String.format("%2d", tempInt);

		tempX01 = tempX10.substring(1, 2);
		convertDig();

		tempX02 = String.format("%2d", tempInt);
		if (debug == 1)
			showLogMessage("I", "", " temp_x02=" + tempX02);
		tmpConvert += tempX02.substring(1, 2);
		tmpConvert += tempX10.substring(2, 2 + 7);
		if (debug == 1)
			showLogMessage("I", "", " convert =" + tmpConvert);

		for (int int2 = 0; int2 < 10; int2++) {
			tempX01 = String.format("%1.1s", tmpConvert.substring(int2));
			tempX02 = String.format("%2d", comcr.str2int(tempX01) * prefixN[int2]);
			aftConvert += tempX02.substring(1);
		}
		if (debug == 1)
			showLogMessage("I", "", " convert 1 =" + aftConvert);

		tempInt = 0;
		for (int int2 = 0; int2 < 10; int2++) {
			tempX01 = String.format("%1.1s", aftConvert.substring(int2, int2 + 1));
			tempInt = tempInt + comcr.str2int(tempX01);
		}

		tempX02 = String.format("%2d", tempInt);
		tempX01 = tempX02.substring(1);
		tempInt = 10 - comcr.str2int(tempX01);

		if (tempInt == 10) {
			tempX01 = String.format("0");
		} else {
			tempX01 = String.format("%1d", tempInt);
		}

	}

	// ************************************************************************
	void convertDig() throws Exception {
		if (debug == 1)
			showLogMessage("I", "", "convert dig=" + tempX01);
		if (tempX01.equals("A"))
			tempInt = 10;
		if (tempX01.equals("B"))
			tempInt = 11;
		if (tempX01.equals("C"))
			tempInt = 12;
		if (tempX01.equals("D"))
			tempInt = 13;
		if (tempX01.equals("E"))
			tempInt = 14;
		if (tempX01.equals("F"))
			tempInt = 15;
		if (tempX01.equals("G"))
			tempInt = 16;
		if (tempX01.equals("H"))
			tempInt = 17;
		if (tempX01.equals("I"))
			tempInt = 34;
		if (tempX01.equals("J"))
			tempInt = 18;
		if (tempX01.equals("K"))
			tempInt = 19;
		if (tempX01.equals("L"))
			tempInt = 20;
		if (tempX01.equals("M"))
			tempInt = 21;
		if (tempX01.equals("N"))
			tempInt = 22;
		if (tempX01.equals("O"))
			tempInt = 35;
		if (tempX01.equals("P"))
			tempInt = 23;
		if (tempX01.equals("Q"))
			tempInt = 24;
		if (tempX01.equals("R"))
			tempInt = 25;
		if (tempX01.equals("S"))
			tempInt = 26;
		if (tempX01.equals("T"))
			tempInt = 27;
		if (tempX01.equals("U"))
			tempInt = 28;
		if (tempX01.equals("V"))
			tempInt = 29;
		if (tempX01.equals("W"))
			tempInt = 30;
		if (tempX01.equals("X"))
			tempInt = 31;
		if (tempX01.equals("Y"))
			tempInt = 32;
		if (tempX01.equals("Z"))
			tempInt = 33;
	}

	/***********************************************************************/
	/***
	 * int1: 1:用ID 2:用帳號
	 * 
	 * @param int1
	 * @return
	 * @throws Exception
	 */
	int checkCardDup(int int1) throws Exception {
		int recCnt;
		String hIdCode = "";
		String hTempBirthday = "";
		String swNew = "";

		if (debug == 1)
			showLogMessage("I", "", " check_card_dup=[" + int1 + "]" + emapActNo);

		hAcnoIdPSeqno = "";
		hTempId = "";

		sqlCmd = "select id_p_seqno,";
		sqlCmd += "substr(acct_key,1,10)  as h_temp_id";
		sqlCmd += " from dba_acno  ";
		sqlCmd += "where acct_no  = ?  ";

		setString(1, emapActNo);
		int recordCnt = selectTable();
		hOriIbmIdCode = "";
		tempInt = 0;
		if (recordCnt > 0) {
			hAcnoIdPSeqno = getValue("id_p_seqno");
			hTempId = getValue("h_temp_id");

			swNew = "N";
			sqlCmd = "select count(*) temp_int ";
			sqlCmd += " from dbc_card  ";
			sqlCmd += "where acct_no      = ?  ";
			sqlCmd += "  and current_code = '0' ";
			setString(1, emapActNo);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				tempInt = getValueInt("temp_int");
			}
			if (!hTempId.equals(emapApplyId)) {
				if (tempInt == 0) {
					return (3);
				} else {
					return (4);
				}
			}

			sqlCmd = "select ibm_id_code ";
			sqlCmd += " from dbc_idno  ";
			sqlCmd += "where id_p_seqno = ? ";
			setString(1, hAcnoIdPSeqno);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				hOriIbmIdCode = getValue("ibm_id_code");
			} else
				return (1);
		} else {
			swNew = "Y";
			hOriIbmIdCode = "0";
		}

		if (int1 == 1) {
			hIdCode = "";
			hTempBirthday = "";

			sqlCmd = "select id_no_code, birthday ";
			sqlCmd += " from dbc_idno  ";
			sqlCmd += "where id_no       = ?  ";
			sqlCmd += "  and ibm_id_code = ?  ";
			sqlCmd += "fetch first 1 rows only ";
			setString(1, emapApplyId);
			setString(2, hOriIbmIdCode);
			recordCnt = selectTable();

			if (recordCnt > 0) {
				hIdCode = getValue("id_no_code");
				hTempBirthday = getValue("birthday");
			}
			if (swNew.equals("N")) {
				if (!hTempBirthday.equals(emapBirthday)) {
					if (tempInt == 0) {
						return (3);
					} else {
						return (4);
					}
				}
			}
			if (recordCnt > 0) {
				recCnt = 0;

				sqlCmd = "select count(*) rec_cnt ";
				sqlCmd += " from dbc_card  ";
				sqlCmd += "where acct_no      = ?  ";
				sqlCmd += "  and id_p_seqno   = ?  ";
				sqlCmd += "  and current_code = '0' ";
				setString(1, emapActNo);
				setString(2, hAcnoIdPSeqno);
				recordCnt = selectTable();
				if (recordCnt > 0) {
					recCnt = getValueInt("rec_cnt");
				}

				if (recCnt > 0)
					return (1);
			}

			hIdCode = "";
			sqlCmd = "select id_p_seqno, id_no_code ";
			sqlCmd += " from crd_idno  ";
			sqlCmd += "where id_p_seqno = ?  ";
			sqlCmd += "  and birthday   = ?  ";
			sqlCmd += "fetch first 1 rows only ";
			setString(1, hAcnoIdPSeqno);
			setString(2, emapBirthday);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				hIdCode = getValue("id_no_code");
				String hDcepApplyIdPSeqno = getValue("id_p_seqno");

				recCnt = 0;

				sqlCmd = "select count(*) rec_cnt ";
				sqlCmd += " from crd_card  ";
				sqlCmd += "where combo_acct_no = ?  ";
				sqlCmd += "  and id_p_seqno   = ?  ";
				sqlCmd += "  and current_code = 0 ";
				setString(1, emapActNo);
				setString(2, hDcepApplyIdPSeqno);
				setString(3, hIdCode);
				recordCnt = selectTable();
				if (recordCnt > 0) {
					recCnt = getValueInt("rec_cnt");
				}
				if (recCnt > 0)
					return (1);
			}
		} else {
			recCnt = 0;

			sqlCmd = "select count(*) rec_cnt ";
			sqlCmd += " from dbc_card  ";
			sqlCmd += "where acct_no      = ?  ";
			sqlCmd += "  and current_code = '0' ";
			setString(1, emapActNo);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				recCnt = getValueInt("rec_cnt");
			}

			if (recCnt > 0)
				return (1);

			recCnt = 0;

			sqlCmd = "select count(*) rec_cnt ";
			sqlCmd += " from crd_card  ";
			sqlCmd += "where combo_acct_no = ?  ";
			sqlCmd += "  and current_code  = '0' ";
			setString(1, emapActNo);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				recCnt = getValueInt("rec_cnt");
			}

			if (recCnt > 0)
				return (1);
		}

		return (0);
	}

	/***********************************************************************/
	/***
	 * int1: 1:用ID 2:用帳號
	 * 
	 * @param int1
	 * @return
	 * @throws Exception
	 */
	int checkEmbossDup(int int1) throws Exception {
		int recCnt;
		if (debug == 1)
			showLogMessage("I", "", " check_emboss_dup=[" + int1 + "]" + emapActNo);

		if (int1 == 1) {
			recCnt = 0;

			sqlCmd = "select count(*) rec_cnt ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where act_no       = ?  ";
			sqlCmd += "  and apply_id     = ?  ";
			sqlCmd += "  and birthday     = ?  ";
			sqlCmd += "  and reject_code  = ''  ";
			sqlCmd += "  and in_main_date = '' ";
			setString(1, emapActNo);
			setString(2, emapApplyId);
			setString(3, emapBirthday);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				recCnt = getValueInt("rec_cnt");
			}

			if (recCnt > 0)
				return (1);

			recCnt = 0;

			sqlCmd = "select count(*) rec_cnt ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where act_no       = ?  ";
			sqlCmd += "  and apply_source = 'P'  ";
			sqlCmd += "  and valid_to     > to_char(sysdate,'yyyymmdd')  ";
			sqlCmd += "  and reject_code  = ''  ";
			sqlCmd += "  and in_main_date = '' ";
			setString(1, emapActNo);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				recCnt = getValueInt("rec_cnt");
			}

			if (recCnt > 0)
				return (1);
		} else {
			recCnt = 0;

			sqlCmd = "select count(*) rec_cnt ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where act_no       = ?  ";
			sqlCmd += "  and reject_code  = ''  ";
			sqlCmd += "  and in_main_date = '' ";
			setString(1, emapActNo);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				recCnt = getValueInt("rec_cnt");
			}

			if (recCnt > 0)
				return (1);
		}

		return (0);
	}

	// ************************************************************************
	public void initRtn() throws Exception {
		hErrorFlag = 0;
		count = 0;
		corpFlag = 0;
		emapRowid = "";
		emapGroupCode = "";
		emapCardType = "";
		emapSourceCode = "";
		emapApplyId = "";
		emapApplyIdCode = "";
		emapPmId = "";
		emapPmIdCode = "";
		emapBirthday = "";
		emapStmtCycle = "";
		emapmajorCardNo = "";
		emapValidFm = "";
		emapValidTo = "";
		emapMajorValidFm = "";
		emapMajorValidTo = "";
		emapAcctType = "";
		emapApplySource = "";
		emAppmBirthday = "";
		emapCheckCode = "";
		emapMajorChgFlag = "";
		emapFinalFeeCode = "";
		emapFeeCode = "";
		emapCorpNo = "";
		emapFeeReasonCode = "";
		emapCreditLmt = 0;
		emapRiskBankNo = "";
		emapActNo = "";
		emapIcFlag = "";
		emapServiceCode = "";
		emapCardcat = "";
		emapUnitCode = "";
		emapRevolveIntRateYear = 0.0;
		emapBusinessCode = "";
		emapRegBankNo = "";
		emapBranch = "";
		emapEngName = "";
		emapNcccType = "";
		emapAcctType = "";
		emapApplySource = "";
		emapCrtBankNo = "";
		emapVdBankNo = "";
		ptrExtnYear = 0;
		hLineOfCreditAmt = 0;
		hNewEndDate = "";
		cardComboAcctNo = "";
	}

	/***********************************************************************/
	class buf2 {
		String acctType;
		String groupCode;
		String cardType;
		String cardIndicator;
		String comboIndicator;
		String fCurrencyFlag;

		String allText() throws UnsupportedEncodingException {
			String rtn = "";
			rtn += comc.fixLeft(acctType, 3);
			rtn += comc.fixLeft(groupCode, 5);
			rtn += comc.fixLeft(cardType, 3);
			rtn += comc.fixLeft(cardIndicator, 2);
			rtn += comc.fixLeft(comboIndicator, 2);
			rtn += comc.fixLeft(fCurrencyFlag, 2);
			return rtn;
		}
	}
	// ************************************************************************

} // End of class FetchSample
