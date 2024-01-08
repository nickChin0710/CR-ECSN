/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  108/11/26  V2.01.01    Pino      Initial                                   *
*  109/04/14  V2.01.02    Wilson    insert dbc_emboss新增欄位                                                      *
*  109/05/22  V3.01.03    Wilson    新增market_agree_base                       *
*  109/11/03  V3.01.04    Wilson    DBC_D001 -> DbcD001                       *
*  109/11/13  V3.01.05  yanghan       修改了變量名稱和方法名稱                                                                               *
*  109/12/24  V3.01.06  yanghan       修改了變量名稱和方法名稱            *
*  110/06/15  V3.01.07    Wilson    insert dbc_emboss新增digital_flag          *
*  110/06/24  V3.01.08    Wilson    insert dbc_emboss新增mail_branch           *
******************************************************************************/

package Dbc;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*新製卡接收DEBIT*/
public class DbcD001 extends AccessDAO {
	private String progname = "新製卡接收DEBIT CARD 送製卡資料處理程式 110/06/24 V3.01.08";

	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	int debug = 0;
	int debugD = 0;
	String hTempUser = "";

	String prgmId = "DbcD001";
	String stderr = "";
	long hModSeqno = 0;
	String hModUser = "";
	String hCallBatchSeqno = "";

	String hBatchno = "";
	String hEmbossSource = "";
	String hNcccBatchno = "";
	double hNcccRecno = 0;
	String hDcesSeqno = "";
	double hDcesSourceRecno = 0;
	String hDcesServiceYear = "";
	String hDcesSalary = "";
	String hDcesValue = "";
	String hDcesCreditLmt = "";
	String hDcesAuthCreditLmt = "";
	String hDcesEmbossSource = "";
	String hDcesEmbossReason = "";
	String hDcesResendNote = "";
	String hDcesSourceBatchno = "";
	String hDcesCardType = "";
	String hDcesAcctType = "";
	String hDcesAcctKey = "";
	String hDcesUnitCode = "";
	String hDcesCardNo = "";
	String hDcesOldCardNo = "";
	String hDcesMajorCardNo = "";
	String hDcesMajorValidFm = "";
	String hDcesMajorValidTo = "";
	String hDcesMajorChgFlag = "";
	String hDcesChangeReason = "";
	String hDcesStatusCode = "";
	String hDcesReasonCode = "";
	String hDcesMemberNote = "";
	String hDcesApplyId = "";
	String hDcesApplyIdCode = "";
	String hDcesPmId = "";
	String hDcesPmIdCode = "";
	String hDcesGroupCode = "";
	String hDcesSourceCode = "";
	String hDcesCorpNo = "";
	String hDcesCorpNoCode = "";
	String hDcesCorpActFlag = "";
	String hDcesCorpAssureFlag = "";
	String hDcesRegBankNo = "";
	String hDcesRiskBankNo = "";
	String hDcesChiName = "";
	String hDcesEngName = "";
	String hDcesBirthday = "";
	String hDcesMarriage = "";
	String hDcesRelWithPm = "";
	String hDcesEducation = "";
	String hDcesNation = "";
	String hDcesMailZip = "";
	String hDcesMailAddr1 = "";
	String hDcesMailAddr2 = "";
	String hDcesMailAddr3 = "";
	String hDcesMailAddr4 = "";
	String hDcesMailAddr5 = "";
	String hDcesResidentZip = "";
	String hDcesResidentAddr1 = "";
	String hDcesResidentAddr2 = "";
	String hDcesResidentAddr3 = "";
	String hDcesResidentAddr4 = "";
	String hDcesResidentAddr5 = "";
	String hDcesCompanyName = "";
	String hDcesJobPosition = "";
	String hDcesHomeAreaCode1 = "";
	String hDcesHomeTelNo1 = "";
	String hDcesHomeTelExt1 = "";
	String hDcesHomeAreaCode2 = "";
	String hDcesHomeTelNo2 = "";
	String hDcesHomeTelExt2 = "";
	String hDcesOfficeAreaCode1 = "";
	String hDcesOfficeTelNo1 = "";
	String hDcesOfficeTelExt1 = "";
	String hDcesOfficeAreaCode2 = "";
	String hDcesOfficeTelNo2 = "";
	String hDcesOfficeTelExt2 = "";
	String hDcesEMailAddr = "";
	String hDcesCellarPhone = "";
	String hDcesActNo = "";
	String hDcesVip = "";
	String hDcesFeeCode = "";
	String hDcesFinalFeeCode = "";
	String hDcesStandardFee = "";
	String hDcesAnnualFee = "";
	String hDcesForceFlag = "";
	String hDcesBusinessCode = "";
	String hDcesIntroduceNo = "";
	String hDcesValidFm = "";
	String hDcesValidTo = "";
	String hDcesSex = "";
	String hDcesAcceptDm = "";
	String hDcesApplyNo = "";
	String hDcesCardcat = "";
	String hDcesMailType = "";
	String hDcesIntroduceId = "";
	String hDcesIntroduceName = "";
	String hDcesSalaryCode = "";
	String hDcesStudent = "";
	String hDcesPoliceNo1 = "";
	String hDcesPoliceNo2 = "";
	String hDcesPoliceNo3 = "";
	String hDcesPmCash = "";
	String hDcesSupCash = "";
	String hDcesOnlineMark = "";
	String hDcesErrorCode = "";
	String hDcesRejectCode = "";
	String hDcesEmboss4thData = "";
	String hDcesMemberId = "";
	String hDcesPmBirthday = "";
	String hDcesSupBirthday = "";
	String hDcesFeeFeasonCode = "";
	String hDcesStmtCycle = "";
	String hDcesCreditFlag = "";
	String hDcesCommFlag = "";
	String hDcesResidentNo = "";
	int hDcesOtherCntryCode = 0;
	String hDcesPassportNo = "";
	String hDcesStaffFlag = "";
	String hDcesNcccType = "";
	String hDcesSonCardFlag = "";
	String hDcesOrgIndivCrdLmt = "";
	String hDcesIndivCrdLmt = "";
	String hCheckCode = "";
	String hDcesIcFlag = "";
	String hDcesBranch = "";
	String hDcesMailAttach1 = "";
	String hDcesMailAttach2 = "";
	String hComboIndicator = "";
	String hDcesRowid = "";
	String hDcesApplySource = "";
	String hDcesAgeIndicator = "";
	String hDcesServiceCode = "";
	String hDcesBankActno = "";
	String hDcesApplyIbmIdCode = "";
	String hDcesPmIbmIdCode = "";
	String hDcesVdcoPcFlag = "";
	String hDroupAbbrCode = "";
	String hDcdtToIbmDate = "";
	String hProgCode = "";
	String hWfValue = ""; 
	String hDcesMailBranch = "";

	String hENews = "";
	String hPromoteEmpNo = "";
	String hPromoteDept = "";
	String hCardRefNum = "";
	String hIntroduceEmpNo = "";
	String hUrFlag = "";
	String hSpouseIdNo = "";
	String hSpouseBirthday = "";
	String hResidentNoExpireDate = "";
	String hPassportDate = "";
	String hBillApplyFlag = "";
	String hCompanyZip = "";
	String hGraduationElementarty = "";
	String hSpouseName = "";
	String hCompanyAddr1 = "";
	String hCompanyAddr2 = "";
	String hCompanyAddr3 = "";
	String hCompanyAddr4 = "";
	String hCompanyAddr5 = "";
	String hStatSendInternet = "";
	String hCrtBankNo = "";
	String hVdBankNo = "";
	String hElectronicCode = "";
	String hAp1ApplyDate = "";
	String hMarketAgreeBase = "";
	String hDigitalFlag = "";

	String hCreateDate = "";
	String hSuperId = "";
	String hTmpDate = "";

	int totalCnt = 0;
	String hDcesToNcccCode = "";
	String hDcesDiffCode = "";
	String hDcesSupFlag = "";
	String temstr = "";
	String hDcesApsRecno = "";
	String hDcesApsBatchno = "";
	String hDcesChgAddrFlag = "";
	String hDcesPvv = "";
	String hDcesCvv = "";
	String hDcesCvv2 = "";
	String hDcesPvki = "";
	String hDcesOldBegDate = "";
	String hDcesOldEndDate = "";
	String hDcesOrgCashLmt = "";
	String hDcesCashLmt = "";
	String hDcesIcIndicator = "";
	String hDcesIcCvv = "";
	String hDcesIcPin = "";
	String hDcesDerivKey = "";
	String hDcesLOfflnLmt = "";
	String hDcesUOfflnLmt = "";
	String hTransType = "";
	String hSupFlag = "";

	// ************************************************************************

	public int mainProcess(String[] args) {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length > 4) {
				comc.errExit("Usage: DbcD001 src(1 or 2)", "example:00092701 1 shu batch_seqno");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

			String checkHome = comc.getECSHOME();
			if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
				comcr.hCallBatchSeqno = "no-call";
			}

			comcr.hCallRProgramCode = javaProgram;
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

			hModUser = comc.commGetUserID();
			hCreateDate = sysDate;
			if (args.length > 0) {
				hBatchno = args[0];
				hEmbossSource = args[1];
				hSuperId = args[2];
			}

			// h_call_batch_seqno = args[3];

			if (checkProcess(1, "DbcD001") != 0) {
				exceptExit = 0;
				comcr.hCallBatchSeqno = String.format("新製卡正在編列卡號(DbcD001),新製卡不可同時>編列或參數檔內DbcD001被鎖住");
				showLogMessage("I", "", comcr.hCallBatchSeqno);

				comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
			}
			process();

			checkProcess(2, "DbcD001");

			// ==============================================
			// 固定要做的
			comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
			showLogMessage("I", "", comcr.hCallErrorDesc);

			if (comcr.hCallBatchSeqno.length() == 20)
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
	int checkProcess(int type, String progCode) throws Exception {
		String hWfValue = "";
		String hProgCode = "";
		hProgCode = "";
		hProgCode = progCode;

		if (debug == 1)
			showLogMessage("I", "", "check_proess type =[" + type + "]");

		if (type == 2) {
			daoTable = "ptr_sys_parm";
			updateSQL = " wf_value = 'NO',";
			updateSQL += " mod_user = ?,";
			updateSQL += " mod_time = sysdate";
			whereStr = "where WF_PARM = 'CRD_BATCH'  ";
			whereStr += "  and WF_KEY  = ? ";
			setString(1, hModUser);
			setString(2, hProgCode);
			updateTable();

			return (0);
		}
		/**** 檢核是否有程式在執行當中 ***/
		hWfValue = "";
		sqlCmd = "select WF_VALUE ";
		sqlCmd += " from ptr_sys_parm  ";
		sqlCmd += "where WF_PARM = 'CRD_BATCH'  ";
		sqlCmd += "  and WF_KEY  = ? ";
		setString(1, hProgCode);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_sys_parm not found!", "", comcr.hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hWfValue = getValue("WF_VALUE");
		}

		if (hWfValue.trim().equals("YES")) {
			return (1);
		} else {
			daoTable = "ptr_sys_parm";
			updateSQL = " wf_value = 'YES',";
			updateSQL += " mod_user = ?,";
			updateSQL += " mod_time = sysdate";
			whereStr = "where WF_PARM = 'CRD_BATCH'  ";
			whereStr += "  and WF_KEY  = ? ";
			setString(1, hModUser);
			setString(2, hProgCode);
			updateTable();

			commitDataBase();
		}

		return (0);
	}

	/***********************************************************************/
	void process() throws Exception {
		getNcccBatchno();
		processDbcEmapTmp(); /* 申請書暫存檔 */
	}

	/***********************************************************************/
	void getNcccBatchno() throws Exception {
		String tmpBatchno = "";
		long hTmpNo = 0;

		hNcccRecno = 0;
		hTmpDate = "";
		hNcccBatchno = "";
		sqlCmd = "select to_char(sysdate,'yymmdd') h_tmp_date ";
		sqlCmd += "  from dual ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTmpDate = getValue("h_tmp_date");
		}

		sqlCmd = "select distinct(max(batchno)) h_nccc_batchno ";
		sqlCmd += " from dbc_emboss  ";
		sqlCmd += "where batchno  like ? || '%' ";
		sqlCmd += "  and nccc_type     = '1' ";
		sqlCmd += "  and to_nccc_date  = '' ";
		setString(1, hTmpDate);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hNcccBatchno = getValue("h_nccc_batchno");
		}
		if (hNcccBatchno.length() > 0) {
			sqlCmd = "select max(recno) h_nccc_recno ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where batchno = ? ";
			setString(1, hNcccBatchno);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				hNcccRecno = getValueLong("h_nccc_recno");
			}
			return;
		} else {
			sqlCmd = "select distinct(max(batchno)) h_nccc_batchno ";
			sqlCmd += " from dbc_emboss  ";
			sqlCmd += "where batchno like ? || '%' ";
			setString(1, hTmpDate);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				hNcccBatchno = getValue("h_nccc_batchno");
			}
			if (hNcccBatchno.length() > 0) {
				hTmpNo = comcr.str2long(hNcccBatchno) + 1;
				tmpBatchno = String.format("%08d", hTmpNo);
			} else {
				hTmpNo = 1;
				tmpBatchno = String.format("%s%02d", hTmpDate, hTmpNo);
			}

			hNcccBatchno = tmpBatchno;
		}

		if (debug == 1)
			showLogMessage("I", "", "  888 Batchno=[" + hNcccBatchno + "]");

		return;
	}

	/***********************************************************************/
	void processDbcEmapTmp() throws Exception {
		sqlCmd = "select ";
		sqlCmd += "a.seqno,";
		sqlCmd += "a.recno,";
		sqlCmd += "a.service_year,";
		sqlCmd += "a.salary,";
		sqlCmd += "a.value,";
		sqlCmd += "a.credit_lmt,";
		sqlCmd += "a.source,";
		sqlCmd += "a.resend_note,";
		sqlCmd += "a.batchno,";
		sqlCmd += "a.card_type,";
		sqlCmd += "a.acct_type,";
		sqlCmd += "a.acct_key,";
		sqlCmd += "a.unit_code,";
		sqlCmd += "a.card_no,";
		sqlCmd += "a.old_card_no,";
		sqlCmd += "a.major_card_no,";
		sqlCmd += "a.major_valid_fm,";
		sqlCmd += "a.major_valid_to,";
		sqlCmd += "a.major_chg_flag,";
		sqlCmd += "a.apply_id,";
		sqlCmd += "a.apply_id_code,";
		sqlCmd += "a.pm_id,";
		sqlCmd += "a.pm_id_code,";
		sqlCmd += "a.group_code,";
		sqlCmd += "a.source_code,";
		sqlCmd += "a.corp_no,";
		sqlCmd += "a.corp_no_code,";
		sqlCmd += "a.corp_act_flag,";
		sqlCmd += "a.corp_assure_flag,";
		sqlCmd += "a.reg_bank_no,";
		sqlCmd += "a.risk_bank_no,";
		sqlCmd += "a.chi_name,";
		sqlCmd += "a.eng_name,";
		sqlCmd += "a.birthday,";
		sqlCmd += "a.marriage,";
		sqlCmd += "a.rel_with_pm,";
		sqlCmd += "a.education,";
		sqlCmd += "a.nation,";
		sqlCmd += "a.mail_zip,";
		sqlCmd += "a.mail_addr1,";
		sqlCmd += "a.mail_addr2,";
		sqlCmd += "a.mail_addr3,";
		sqlCmd += "a.mail_addr4,";
		sqlCmd += "a.mail_addr5,";
		sqlCmd += "a.resident_zip,";
		sqlCmd += "a.resident_addr1,";
		sqlCmd += "a.resident_addr2,";
		sqlCmd += "a.resident_addr3,";
		sqlCmd += "a.resident_addr4,";
		sqlCmd += "a.resident_addr5,";
		sqlCmd += "a.company_name,";
		sqlCmd += "a.job_position,";
		sqlCmd += "a.home_area_code1,";
		sqlCmd += "a.home_tel_no1,";
		sqlCmd += "a.home_tel_ext1,";
		sqlCmd += "a.home_area_code2,";
		sqlCmd += "a.home_tel_no2,";
		sqlCmd += "a.home_tel_ext2,";
		sqlCmd += "a.office_area_code1,";
		sqlCmd += "a.office_tel_no1,";
		sqlCmd += "a.office_tel_ext1,";
		sqlCmd += "a.office_area_code2,";
		sqlCmd += "a.office_tel_no2,";
		sqlCmd += "a.office_tel_ext2,";
		sqlCmd += "a.e_mail_addr,";
		sqlCmd += "a.cellar_phone,";
		sqlCmd += "a.act_no,";
		sqlCmd += "a.vip,";
		sqlCmd += "a.fee_code,";
		sqlCmd += "a.final_fee_code,";
		sqlCmd += "a.standard_fee,";
		sqlCmd += "a.annual_fee,";
		sqlCmd += "a.force_flag,";
		sqlCmd += "a.business_code,";
		sqlCmd += "a.introduce_no,";
		sqlCmd += "a.valid_fm,";
		sqlCmd += "a.valid_to,";
		sqlCmd += "a.sex,";
		sqlCmd += "a.accept_dm,";
		sqlCmd += "a.apply_no,";
		sqlCmd += "a.cardcat,";
		sqlCmd += "decode(a.mail_type,'','4',a.mail_type) h_dces_mail_type,";
		sqlCmd += "a.introduce_id,";
		sqlCmd += "a.introduce_name,";
		sqlCmd += "decode(a.salary_code,'','N',a.salary_code) h_dces_salary_code,";
		sqlCmd += "a.student,";
		sqlCmd += "a.police_no1,";
		sqlCmd += "a.police_no2,";
		sqlCmd += "a.police_no3,";
		sqlCmd += "a.pm_cash,";
		sqlCmd += "a.sup_cash,";
		sqlCmd += "decode(a.online_mark,'','0',a.online_mark) h_dces_online_mark,";
		sqlCmd += "a.reject_code,";
		sqlCmd += "a.emboss_4th_data,";
		sqlCmd += "a.member_id,";
		sqlCmd += "a.pm_birthday,";
		sqlCmd += "a.sup_birthday,";
		sqlCmd += "a.fee_reason_code,";
		sqlCmd += "a.stmt_cycle,";
		sqlCmd += "decode(a.credit_flag,'','N',a.credit_flag) h_dces_credit_flag,";
		sqlCmd += "decode(a.comm_flag,'','N',a.comm_flag) h_dces_comm_flag,";
		sqlCmd += "a.resident_no,";
		sqlCmd += "a.other_cntry_code,";
		sqlCmd += "a.passport_no,";
		sqlCmd += "a.staff_flag,";
		sqlCmd += "a.nccc_type,";
		sqlCmd += "a.son_card_flag,";
		sqlCmd += "nvl(a.org_indiv_crd_lmt,0) h_dces_org_indiv_crd_lmt,";
		sqlCmd += "nvl(a.indiv_crd_lmt,0) h_dces_indiv_crd_lmt,";
		sqlCmd += "a.ic_flag,";
		sqlCmd += "a.branch,";
		sqlCmd += "a.mail_attach1,";
		sqlCmd += "a.mail_attach2,";
		sqlCmd += "a.rowid  as rowid,";
		sqlCmd += "a.apply_source,";
		sqlCmd += "a.service_code,";
		sqlCmd += "a.bank_actno,";
		sqlCmd += "a.apply_ibm_id_code,";
		sqlCmd += "a.pm_ibm_id_code,";
		sqlCmd += "a.vdco_pc_flag, ";
		sqlCmd += "a.e_news, ";
		sqlCmd += "a.promote_emp_no, ";
		sqlCmd += "a.promote_dept, ";
		sqlCmd += "a.card_ref_num, ";
		sqlCmd += "a.introduce_emp_no, ";
		sqlCmd += "a.ur_flag, ";
		sqlCmd += "a.spouse_id_no, ";
		sqlCmd += "a.spouse_birthday, ";
		sqlCmd += "a.resident_no_expire_date, ";
		sqlCmd += "a.passport_date, ";
		sqlCmd += "a.bill_apply_flag, ";
		sqlCmd += "a.company_zip, ";
		sqlCmd += "a.graduation_elementarty, ";
		sqlCmd += "a.spouse_name, ";
		sqlCmd += "a.company_addr1, ";
		sqlCmd += "a.company_addr2, ";
		sqlCmd += "a.company_addr3, ";
		sqlCmd += "a.company_addr4, ";
		sqlCmd += "a.company_addr5, ";
		sqlCmd += "a.stat_send_internet, ";
		sqlCmd += "a.crt_bank_no, ";
		sqlCmd += "a.vd_bank_no, ";
		sqlCmd += "a.electronic_code, ";
		sqlCmd += "a.ap1_apply_date, ";
		sqlCmd += "a.market_agree_base, ";
		sqlCmd += "decode(a.digital_flag ,'','N',a.digital_flag)  as digital_flag, "; 
		sqlCmd += "a.mail_branch ";
		sqlCmd += " from dbc_emap_tmp a,dbp_acct_type b ";
		sqlCmd += "where a.batchno     like ? ";
		sqlCmd += "  and a.source      like ? ";
		sqlCmd += "  and a.emboss_date = '' ";
		sqlCmd += "  and a.nccc_batchno  = '' ";
		sqlCmd += "  and a.card_no      <> '' ";
		sqlCmd += "  and a.check_code    = '000' ";
		sqlCmd += "  and a.oth_chk_code  = '0' ";
		sqlCmd += "  and b.acct_type   = a.acct_type ";
		sqlCmd += "order by a.acct_type,a.card_type,decode(a.unit_code,'','0000',a.unit_code),a.card_no ";
		setString(1, hBatchno + "%");
		setString(2, hEmbossSource + "%");
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hDcesSeqno = getValue("seqno", i);
			hDcesSourceRecno = getValueDouble("recno", i);
			hDcesServiceYear = getValue("service_year", i);
			hDcesSalary = getValue("salary", i);
			hDcesValue = getValue("value", i);
			hDcesCreditLmt = getValue("credit_lmt", i);
			hDcesAuthCreditLmt = "";
			hDcesEmbossSource = getValue("source", i);
			hDcesEmbossReason = "";
			hDcesResendNote = getValue("resend_note", i);
			hDcesSourceBatchno = getValue("batchno", i);
			hDcesCardType = getValue("card_type", i);
			hDcesAcctType = getValue("acct_type", i);
			hDcesAcctKey = getValue("acct_key", i);
			hDcesUnitCode = getValue("unit_code", i);
			hDcesCardNo = getValue("card_no", i);
			hDcesOldCardNo = getValue("old_card_no", i);
			hDcesMajorCardNo = getValue("major_card_no", i);
			hDcesMajorValidFm = getValue("major_valid_fm", i);
			hDcesMajorValidTo = getValue("major_valid_to", i);
			hDcesMajorChgFlag = getValue("major_chg_flag", i);
			hDcesChangeReason = "";
			hDcesStatusCode = "";
			hDcesReasonCode = "";
			hDcesMemberNote = "";
			hDcesApplyId = getValue("apply_id", i);
			hDcesApplyIdCode = getValue("apply_id_code", i);
			hDcesPmId = getValue("pm_id", i);
			hDcesPmIdCode = getValue("pm_id_code", i);
			hDcesGroupCode = getValue("group_code", i);
			hDcesSourceCode = getValue("source_code", i);
			hDcesCorpNo = getValue("corp_no", i);
			hDcesCorpNoCode = getValue("corp_no_code", i);
			hDcesCorpActFlag = getValue("corp_act_flag", i);
			hDcesCorpAssureFlag = getValue("corp_assure_flag", i);
			hDcesRegBankNo = getValue("reg_bank_no", i);
			hDcesRiskBankNo = getValue("risk_bank_no", i);
			hDcesChiName = getValue("chi_name", i);
			hDcesEngName = getValue("eng_name", i);
			hDcesBirthday = getValue("birthday", i);
			hDcesMarriage = getValue("marriage", i);
			hDcesRelWithPm = getValue("rel_with_pm", i);
			hDcesEducation = getValue("education", i);
			hDcesNation = getValue("nation", i);
			hDcesMailZip = getValue("mail_zip", i);
			hDcesMailAddr1 = getValue("mail_addr1", i);
			hDcesMailAddr2 = getValue("mail_addr2", i);
			hDcesMailAddr3 = getValue("mail_addr3", i);
			hDcesMailAddr4 = getValue("mail_addr4", i);
			hDcesMailAddr5 = getValue("mail_addr5", i);
			hDcesResidentZip = getValue("resident_zip", i);
			hDcesResidentAddr1 = getValue("resident_addr1", i);
			hDcesResidentAddr2 = getValue("resident_addr2", i);
			hDcesResidentAddr3 = getValue("resident_addr3", i);
			hDcesResidentAddr4 = getValue("resident_addr4", i);
			hDcesResidentAddr5 = getValue("resident_addr5", i);
			hDcesCompanyName = getValue("company_name", i);
			hDcesJobPosition = getValue("job_position", i);
			hDcesHomeAreaCode1 = getValue("home_area_code1", i);
			hDcesHomeTelNo1 = getValue("home_tel_no1", i);
			hDcesHomeTelExt1 = getValue("home_tel_ext1", i);
			hDcesHomeAreaCode2 = getValue("home_area_code2", i);
			hDcesHomeTelNo2 = getValue("home_tel_no2", i);
			hDcesHomeTelExt2 = getValue("home_tel_ext2", i);
			hDcesOfficeAreaCode1 = getValue("office_area_code1", i);
			hDcesOfficeTelNo1 = getValue("office_tel_no1", i);
			hDcesOfficeTelExt1 = getValue("office_tel_ext1", i);
			hDcesOfficeAreaCode2 = getValue("office_area_code2", i);
			hDcesOfficeTelNo2 = getValue("office_tel_no2", i);
			hDcesOfficeTelExt2 = getValue("office_tel_ext2", i);
			hDcesEMailAddr = getValue("e_mail_addr", i);
			hDcesCellarPhone = getValue("cellar_phone", i);
			hDcesActNo = getValue("act_no", i);
			hDcesVip = getValue("vip", i);
			hDcesFeeCode = getValue("fee_code", i);
			hDcesFinalFeeCode = getValue("final_fee_code", i);
			hDcesStandardFee = getValue("standard_fee", i);
			hDcesAnnualFee = getValue("annual_fee", i);
			hDcesForceFlag = getValue("force_flag", i);
			hDcesBusinessCode = getValue("business_code", i);
			hDcesIntroduceNo = getValue("introduce_no", i);
			hDcesValidFm = getValue("valid_fm", i);
			hDcesValidTo = getValue("valid_to", i);
			hDcesSex = getValue("sex", i);
			hDcesAcceptDm = getValue("accept_dm", i);
			hDcesApplyNo = getValue("apply_no", i);
			hDcesCardcat = getValue("cardcat", i);
			hDcesMailType = getValue("h_dces_mail_type", i);
			hDcesIntroduceId = getValue("introduce_id", i);
			hDcesIntroduceName = getValue("introduce_name", i);
			hDcesSalaryCode = getValue("h_dces_salary_code", i);
			hDcesStudent = getValue("student", i);
			hDcesPoliceNo1 = getValue("police_no1", i);
			hDcesPoliceNo2 = getValue("police_no2", i);
			hDcesPoliceNo3 = getValue("police_no3", i);
			hDcesPmCash = getValue("pm_cash", i);
			hDcesSupCash = getValue("sup_cash", i);
			hDcesOnlineMark = getValue("h_dces_online_mark", i);
			hDcesErrorCode = "";
			hDcesRejectCode = getValue("reject_code", i);
			hDcesEmboss4thData = getValue("emboss_4th_data", i);
			hDcesMemberId = getValue("member_id", i);
			hDcesPmBirthday = getValue("pm_birthday", i);
			hDcesSupBirthday = getValue("sup_birthday", i);
			hDcesFeeFeasonCode = getValue("fee_reason_code", i);
			hDcesStmtCycle = getValue("stmt_cycle", i);
			hDcesCreditFlag = getValue("h_dces_credit_flag", i);
			hDcesCommFlag = getValue("h_dces_comm_flag", i);
			hDcesResidentNo = getValue("resident_no", i);
			hDcesOtherCntryCode = getValueInt("other_cntry_code", i);
			hDcesPassportNo = getValue("passport_no", i);
			hDcesStaffFlag = getValue("staff_flag", i);
			hDcesNcccType = getValue("nccc_type", i);
			hDcesSonCardFlag = getValue("son_card_flag", i);
			hDcesOrgIndivCrdLmt = getValue("h_dces_org_indiv_crd_lmt", i);
			hDcesIndivCrdLmt = getValue("h_dces_indiv_crd_lmt", i);
			hDcesIcFlag = getValue("ic_flag", i);
			hDcesBranch = getValue("branch", i);
			hDcesMailAttach1 = getValue("mail_attach1", i);
			hDcesMailAttach2 = getValue("mail_attach2", i);
			hComboIndicator = "Y";
			hDcesRowid = getValue("rowid", i);
			hDcesApplySource = getValue("apply_source", i);
			hDcesAgeIndicator = "";
			hDcesServiceCode = getValue("service_code", i);
			hDcesBankActno = getValue("bank_actno", i);
			hDcesApplyIbmIdCode = getValue("apply_ibm_id_code", i);
			hDcesPmIbmIdCode = getValue("pm_ibm_id_code", i);
			hDcesVdcoPcFlag = getValue("vdco_pc_flag", i);
			hENews = getValue("e_news", i);
			hPromoteEmpNo = getValue("promote_emp_no", i);
			hPromoteDept = getValue("promote_dept", i);
			hCardRefNum = getValue("card_ref_num", i);
			hIntroduceEmpNo = getValue("introduce_emp_no", i);
			hUrFlag = getValue("ur_flag", i);
			hSpouseIdNo = getValue("spouse_id_no", i);
			hSpouseBirthday = getValue("spouse_birthday", i);
			hResidentNoExpireDate = getValue("resident_no_expire_date", i);
			hPassportDate = getValue("passport_date", i);
			hBillApplyFlag = getValue("bill_apply_flag", i);
			hCompanyZip = getValue("company_zip", i);
			hGraduationElementarty = getValue("graduation_elementarty", i);
			hSpouseName = getValue("spouse_name", i);
			hCompanyAddr1 = getValue("company_addr1", i);
			hCompanyAddr2 = getValue("company_addr2", i);
			hCompanyAddr3 = getValue("company_addr3", i);
			hCompanyAddr4 = getValue("company_addr4", i);
			hCompanyAddr5 = getValue("company_addr5", i);
			hStatSendInternet = getValue("stat_send_internet", i);
			hCrtBankNo = getValue("crt_bank_no", i);
			hVdBankNo = getValue("vd_bank_no", i);
			hElectronicCode = getValue("electronic_code", i);
			hAp1ApplyDate = getValue("ap1_apply_date", i);
			hMarketAgreeBase = getValue("market_agree_base", i); 
			hDigitalFlag = getValue("digital_flag", i);
			hDcesMailBranch = getValue("mail_branch", i);

			hNcccRecno++;
			totalCnt++;
			if (debug == 1)
				showLogMessage("I", "", "  888 Get tmp=[" + hDcesCardNo + "]" + hDcesGroupCode);

			switch (Integer.parseInt(hDcesOnlineMark)) {
			case 0:
				hDcesToNcccCode = "Y";
				break;
			case 1:
			case 2:
				hDcesToNcccCode = "N";
				break;
			}

			insertDbcEmboss();
			/**********************************************************
			 * 正卡combo卡申請第三軌資料
			 **********************************************************/
			if (hComboIndicator.equals("Y")) {
				hDcesDiffCode = "1";
				if (hDcesOnlineMark.equals("0")) {
					// insert_crd_combo();
				}
			}
			delDbcEmapTmp();

			updateDbcDebit();
			/*
			 * lai mark
			 */
		}
		showLogMessage("I", "", String.format("TOT RECNO COUNT [%d]", totalCnt));
	}

	/***********************************************************************/
	void delDbcEmapTmp() throws Exception {
		daoTable = "dbc_emap_tmp";
		whereStr = "where rowid  = ? ";
		setRowId(1, hDcesRowid);
		deleteTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("delete_dbc_emap_tmp not found!", "", comcr.hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void updateDbcDebit() throws Exception {
		daoTable = "dbc_debit";
		updateSQL = " batchno     = ?,";
		updateSQL += " recno       = ?";
		whereStr = "where card_no  = ? ";
		setString(1, hNcccBatchno);
		setDouble(2, hNcccRecno);
		setString(3, hDcesCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_debit not found!", "", comcr.hCallBatchSeqno);
		}

	}

	/***********************************************************************/
	void insertDbcEmboss() throws Exception {
		String temp = "";

		if (debug == 1)
			showLogMessage("I", "", " insert emboss=[" + hDcesChiName + "]");

		hModSeqno = comcr.getModSeq();

		hDcesSupFlag = "";
		if (hDcesPmId.equals(hDcesApplyId)) {
			hDcesSupFlag = "0";
		} else {
			hDcesSupFlag = "1";
		}

		if (hDcesChiName.trim().length() != 0) {
			if (((hDcesChiName.toCharArray()[0] == 162 && hDcesChiName.toCharArray()[1] >= 207
					&& hDcesChiName.toCharArray()[1] <= 256)
					|| (hDcesChiName.toCharArray()[0] == 161 && hDcesChiName.toCharArray()[1] == 65)
					|| (hDcesChiName.toCharArray()[0] == 161 && hDcesChiName.toCharArray()[1] == 208)
					|| (hDcesChiName.toCharArray()[0] == 163 && hDcesChiName.toCharArray()[1] <= 67))
					&& hDcesEngName.length() == 0) {
				hDcesEngName = comc.commBig5Asc(hDcesChiName);
			}
		}

		hDcesEngName = comc.commAdjEngname(hDcesEngName);

		if (hDcesSourceCode.length() == 0) {
			hDroupAbbrCode = "";
			sqlCmd = "select decode(GROUP_ABBR_CODE,'',' ',GROUP_ABBR_CODE) h_group_abbr_code ";
			sqlCmd += "  from ptr_group_code  ";
			sqlCmd += " where group_code = ? ";
			setString(1, hDcesGroupCode.length() == 0 ? "0000" : hDcesGroupCode);
			int recordCnt = selectTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("select_ptr_group_code not found!", "", comcr.hCallBatchSeqno);
			}
			if (recordCnt > 0) {
				hDroupAbbrCode = getValue("h_group_abbr_code");
			}
			hDcesSourceCode = String.format("%2.2s%4.4s", hDroupAbbrCode, hDcesGroupCode);
		}

		hDcdtToIbmDate = "";
		sqlCmd = "select to_ibm_date ";
		sqlCmd += "  from dbc_debit  ";
		sqlCmd += " where card_no = ? ";
		setString(1, hDcesCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hDcdtToIbmDate = getValue("to_ibm_date");
		}

		setValue("batchno", hNcccBatchno);
		setValueDouble("recno", hNcccRecno);
		setValue("crt_date", sysDate);
		setValue("mod_user", hModUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		setValue("seqno", hDcesSeqno);
		setValueDouble("source_recno", hDcesSourceRecno);
		setValue("aps_recno", hDcesApsRecno);
		setValue("service_year", hDcesServiceYear);
		setValue("salary", hDcesSalary);
		setValue("value", hDcesValue);
		setValue("credit_lmt", hDcesCreditLmt);
		setValue("standard_fee", hDcesStandardFee);
		setValue("annual_fee", hDcesAnnualFee);
		setValue("auth_credit_lmt", hDcesAuthCreditLmt);
		setValue("emboss_source", hDcesEmbossSource);
		setValue("emboss_reason", hDcesEmbossReason);
		setValue("resend_note", hDcesResendNote);
		setValue("source_batchno", hDcesSourceBatchno);
		setValue("aps_batchno", hDcesApsBatchno);
		setValue("to_nccc_code", hDcesToNcccCode);
		setValue("card_type", hDcesCardType);
		setValue("acct_type", hDcesAcctType);
		setValue("acct_key", hDcesAcctKey);
		setValue("unit_code", hDcesUnitCode);
		setValue("card_no", hDcesCardNo);
		setValue("old_card_no", hDcesOldCardNo);
		setValue("major_card_no", hDcesMajorCardNo);
		setValue("major_valid_fm", hDcesMajorValidFm);
		setValue("major_valid_to", hDcesMajorValidTo);
		setValue("major_chg_flag", hDcesMajorChgFlag);
		setValue("change_reason", hDcesChangeReason);
		setValue("status_code", hDcesStatusCode);
		setValue("reason_code", hDcesReasonCode);
		setValue("member_note", hDcesMemberNote);
		setValue("apply_id", hDcesApplyId);
		setValue("apply_id_code", hDcesApplyIdCode);
		setValue("pm_id", hDcesPmId);
		setValue("pm_id_code", hDcesPmIdCode);
		setValue("group_code", hDcesGroupCode);
		setValue("source_code", hDcesSourceCode);
		setValue("corp_no", hDcesCorpNo);
		setValue("corp_no_code", hDcesCorpNoCode);
		setValue("corp_act_flag", hDcesCorpActFlag);
		setValue("corp_assure_flag", hDcesCorpAssureFlag);
		setValue("reg_bank_no", hDcesRegBankNo);
		setValue("risk_bank_no", hDcesRiskBankNo);
		setValue("org_risk_bank_no", hDcesRiskBankNo);
		setValue("chi_name", hDcesChiName);
		setValue("eng_name", hDcesEngName);
		setValue("birthday", hDcesBirthday);
		setValue("marriage", hDcesMarriage);
		setValue("rel_with_pm", hDcesRelWithPm);
		setValue("education", hDcesEducation);
		setValue("nation", hDcesNation);
		setValue("mail_zip", hDcesMailZip);
		setValue("mail_addr1", hDcesMailAddr1);
		setValue("mail_addr2", hDcesMailAddr2);
		setValue("mail_addr3", hDcesMailAddr3);
		setValue("mail_addr4", hDcesMailAddr4);
		setValue("mail_addr5", hDcesMailAddr5);
		setValue("resident_zip", hDcesResidentZip);
		setValue("resident_addr1", hDcesResidentAddr1);
		setValue("resident_addr2", hDcesResidentAddr2);
		setValue("resident_addr3", hDcesResidentAddr3);
		setValue("resident_addr4", hDcesResidentAddr4);
		setValue("resident_addr5", hDcesResidentAddr5);
		setValue("company_name", hDcesCompanyName);
		setValue("job_position", hDcesJobPosition);
		setValue("home_area_code1", hDcesHomeAreaCode1);
		setValue("home_tel_no1", hDcesHomeTelNo1);
		setValue("home_tel_ext1", hDcesHomeTelExt1);
		setValue("home_area_code2", hDcesHomeAreaCode2);
		setValue("home_tel_no2", hDcesHomeTelNo2);
		setValue("home_tel_ext2", hDcesHomeTelExt2);
		setValue("office_area_code1", hDcesOfficeAreaCode1);
		setValue("office_tel_no1", hDcesOfficeTelNo1);
		setValue("office_tel_ext1", hDcesOfficeTelExt1);
		setValue("office_area_code2", hDcesOfficeAreaCode2);
		setValue("office_tel_no2", hDcesOfficeTelNo2);
		setValue("office_tel_ext2", hDcesOfficeTelExt2);
		setValue("e_mail_addr", hDcesEMailAddr);
		setValue("cellar_phone", hDcesCellarPhone);
		setValue("act_no", hDcesActNo);
		setValue("vip", hDcesVip);
		setValue("fee_code", hDcesFeeCode);
		setValue("final_fee_code", hDcesFinalFeeCode);
		setValue("force_flag", hDcesForceFlag);
		setValue("business_code", hDcesBusinessCode);
		setValue("introduce_no", hDcesIntroduceNo);
		setValue("valid_fm", hDcesValidFm);
		setValue("valid_to", hDcesValidTo);
		setValue("sex", hDcesSex);
		setValue("accept_dm", hDcesAcceptDm);
		setValue("apply_no", hDcesApplyNo);
		setValue("cardcat", hDcesCardcat);
		setValue("mail_type", hDcesMailType);
		setValue("introduce_id", hDcesIntroduceId);
		setValue("introduce_name", hDcesIntroduceName);
		setValue("salary_code", hDcesSalaryCode);
		setValue("student", hDcesStudent);
		setValue("police_no1", hDcesPoliceNo1);
		setValue("police_no2", hDcesPoliceNo2);
		setValue("police_no3", hDcesPoliceNo3);
		setValue("pm_cash", hDcesPmCash);
		setValue("sup_cash", hDcesSupCash);
		setValue("online_mark", hDcesOnlineMark);
		setValue("error_code", hDcesErrorCode);
		setValue("reject_code", hDcesRejectCode);
		setValue("emboss_4th_data", hDcesEmboss4thData);
		setValue("member_id", hDcesMemberId);
		setValue("pm_birthday", hDcesPmBirthday);
		setValue("sup_birthday", hDcesSupBirthday);
		setValue("fee_reason_code", hDcesFeeFeasonCode);
		setValue("chg_addr_flag", hDcesChgAddrFlag);
		setValue("pvv", hDcesPvv);
		setValue("cvv", hDcesCvv);
		setValue("cvv2", hDcesCvv2);
		setValue("pvki", hDcesPvki);
		setValue("old_beg_date", hDcesOldBegDate);
		setValue("old_end_date", hDcesOldEndDate);
		setValue("service_code", hDcesServiceCode);
		setValue("sup_flag", hDcesSupFlag);
		setValue("credit_flag", hDcesCreditFlag);
		setValue("comm_flag", hDcesCommFlag);
		setValue("resident_no", hDcesResidentNo);
		setValueInt("other_cntry_code", hDcesOtherCntryCode);
		setValue("passport_no", hDcesPassportNo);
		setValue("staff_flag", hDcesStaffFlag);
		setValue("stmt_cycle", hDcesStmtCycle);
		setValue("nccc_type", hDcesNcccType);
		setValue("diff_code", hDcesDiffCode);
		setValue("son_card_flag", hDcesSonCardFlag);
		setValue("org_indiv_crd_lmt", hDcesOrgIndivCrdLmt);
		setValue("indiv_crd_lmt", hDcesIndivCrdLmt);
		setValue("org_cash_lmt", hDcesOrgCashLmt);
		setValue("cash_lmt", hDcesCashLmt);
		setValue("ic_flag", hDcesIcFlag);
		setValue("branch", hDcesBranch);
		setValue("mail_attach1", hDcesMailAttach1);
		setValue("mail_attach2", hDcesMailAttach2);
		setValue("ic_indicator", hDcesIcIndicator);
		setValue("ic_cvv", hDcesIcCvv);
		setValue("ic_pin", hDcesIcPin);
		setValue("deriv_key", hDcesDerivKey);
		setValue("l_offln_lmt", hDcesLOfflnLmt);
		setValue("u_offln_lmt", hDcesUOfflnLmt);
		setValue("apply_source", hDcesApplySource);
		setValue("age_indicator", hDcesAgeIndicator);
		setValue("bank_actno", hDcesBankActno);
		setValue("apply_ibm_id_code", hDcesApplyIbmIdCode);
		setValue("pm_ibm_id_code", hDcesPmIbmIdCode);
		setValue("to_ibm_date", hDcdtToIbmDate);
		setValue("vdco_pc_flag", hDcesVdcoPcFlag);
		setValue("e_news", hENews);
		setValue("promote_emp_no", hPromoteEmpNo);
		setValue("promote_dept", hPromoteDept);
		setValue("card_ref_num", hCardRefNum);
		setValue("introduce_emp_no", hIntroduceEmpNo);
		setValue("ur_flag", hUrFlag);
		setValue("spouse_id_no", hSpouseIdNo);
		setValue("spouse_birthday", hSpouseBirthday);
		setValue("resident_no_expire_date", hResidentNoExpireDate);
		setValue("passport_date", hPassportDate);
		setValue("bill_apply_flag", hBillApplyFlag);
		setValue("company_zip", hCompanyZip);
		setValue("graduation_elementarty", hGraduationElementarty);
		setValue("spouse_name", hSpouseName);
		setValue("company_addr1", hCompanyAddr1);
		setValue("company_addr2", hCompanyAddr2);
		setValue("company_addr3", hCompanyAddr3);
		setValue("company_addr4", hCompanyAddr4);
		setValue("company_addr5", hCompanyAddr5);
		setValue("stat_send_internet", hStatSendInternet);
		setValue("crt_bank_no", hCrtBankNo);
		setValue("vd_bank_no", hVdBankNo);
		setValue("electronic_code", hElectronicCode);
		setValue("ap1_apply_date", hAp1ApplyDate);
		setValue("market_agree_base", hMarketAgreeBase); 
		setValue("digital_flag", hDigitalFlag);
		setValue("mail_branch", hDcesMailBranch);

		daoTable = "dbc_emboss";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_dbc_emboss duplicate!", "", comcr.hCallBatchSeqno);
		}
	}

	/***********************************************************************/
	void insertCrdCombo() throws Exception {

		hTransType = "";
		hSupFlag = "";
		hSupFlag = hDcesSupFlag;

		switch (Integer.parseInt(hDcesEmbossSource)) {
		case 1:
			hTransType = "01";
			/* 新製卡 */ break;
		case 2:
			hTransType = "08";
			/* 普昇金 */ break;
		case 3:
		case 4:
			hTransType = "08";
			/* 續卡 */ break;
		case 5:
			if (hDcesEmbossReason.equals("1"))
				hTransType = "02"; /* 掛失重製 */
			if (hDcesEmbossReason.equals("2"))
				hTransType = "07"; /* 毀損重製 */
			if (hDcesEmbossReason.equals("3"))
				hTransType = "04"; /* 偽卡重製 */
			break;
		}

		return;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcD001 proc = new DbcD001();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
	/***********************************************************************/
}
