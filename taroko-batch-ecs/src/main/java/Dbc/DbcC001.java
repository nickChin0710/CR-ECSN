/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  108/12/20  V1.00.00    Rou       Update get_ptr_extn()                     *
*  109/05/12  V1.00.01    Wilson    set apply_source = 'T'                    *
*  109/11/12  V1.00.02  yanghan       修改了變量名稱和方法名稱
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  111/06/16  V1.00.04    Justin    弱點修正                                  *
*  112/02/18  V1.00.05    Wilson    調整不續卡條件                                                                                           *
*  112/03/25  V1.00.06    Wilson    insert dbc_emboss_tmp add confirm_date    *
*  112/04/27  V1.00.07    Wilson    增加判斷凍結不續卡                                                                                   *
*  112/04/28  V1.00.08    Wilson    mark基本資料不全不續卡                                                                        *
*  112/05/09  V1.00.09    Ryan      增加判斷sysDate要是當月的第一個營業日才往下執行，否則就直接結束*
*  112/11/11  V1.00.10    Wilson    調整為判斷當月第二個營業日才執行                                                        *
*  112/11/25  V1.00.11    Wilson    調整為判斷參數為第幾個營業日                                                                *
*  112/12/11  V1.00.12    Wilson    crd_item_unit不判斷卡種                                                              *
******************************************************************************/

package Dbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*VD 產生整批續卡/不續卡*/
public class DbcC001 extends AccessDAO {
	private String progname = "VD 產生整批續卡/不續卡 112/12/11 V1.00.12";

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
	String tmpChar1 = "";
	String tmpChar = "";
	double tmpDoub = 0;
	long tmpLong = 0;
	int tmpInt = 0;

	String prgmId = "DbcC001";
	String stderr = "";
	long hModSeqno = 0;
	String hModUser = "";
	String hModTime = "";
	String hModPgm = "";

	String hParamDateLike = "";
	String hParamGroupCode = "";
	int hParamCnt = 0;
	String hDcioMarriage = "";
	String hDcioNation = "";
	String hDcioChiName = "";
	String hDcioSex = "";
	String hDcioBirthday = "";
	String hDcioStudent = "";
	String hDcioIdPSeqno = "";
	double hActLineOfCreditAmt = 0;
	String hChgAddrDate = "";
	String hActNo = "";
	String hRIiskBankNo = "";
	String hDaaoBlockReason1 = "";
	String hDaaoBlockReason2 = "";
	String hDaaoBlockReason3 = "";
	String hDaaoBlockReason4 = "";
	String hDaaoBlockReason5 = "";
	String hPaymentRate = "";
	String hDaaoChgAddrDate = "";
	String hDaaoStatSendPaper = "";
	String hDaaoStatSendInternet = "";
	String hDaaoBillSendingZip = "";
	String hBillAddress = "";
	String hAcnoZip = "";
	String hAcnoAddr1 = "";
	String hAcnoAddr2 = "";
	String hAcnoAddr3 = "";
	String hAcnoAddr4 = "";
	String hAcnoAddr5 = "";
	String hBegDate = "";
	String hSystemDate = "";
	String hLimitConsumeDate = "";
	int hSystemDd = 0;
	int hAddMonths = 0;
	String hValidTo = "";
	String hDccdCardNo = "";
	String hDccdId = "";
	String hDccdIdCode = "";
	String hDccdCorpNo = "";
	String hDccdGroupCode = "";
	String hDccdSourceCode = "";
	String hDccdSupFlag = "";
	String hDccdMajorRelation = "";
	String hDccdMajord = "";
	String hDccdMajorIdCode = "";
	String hDccdMajorCardNo = "";
	String hDccdEngName = "";
	String hDccdRegBankNo = "";
	String hDccdForceFlag = "";
	String hDccdUnitCode = "";
	String hDccdEmbossData = "";
	String hDccdAcctType = "";
	String hDccdAcctKey = "";
	String hDccdPSeqno = "";
	String hDccdGpNo = "";
	String hDccdNewBegDate = "";
	String hDccdNewEndDate = "";
	String hDccdCurrentCode = "";
	String hDccdFeeCode = "";
	String hDccdCardType = "";
	String hDccdIdPSeqno = "";
	String hDccdCorpPSeqno = "";
	String hDccdCorpActFlag = "";
	String hDccdChangeReason = "";
	String hDccdChangeStatus = "";
	String hDccdChangeDate = "";
	String hDccdBlockReason1 = "";
	String hDccdBlockReason2 = "";
	String hDccdBlockReason3 = "";
	String hDccdBlockReason4 = "";
	String hDccdBlockReason5 = "";
	String hDccdReissueStatus = "";
	String hDccdReissueDate = "";
	String hDccdIcFlag = "";
	String hDccdIbmIdCode = "";
	String hDccdOldBankActno = "";
	String hDccdAcctNo = "";
	String hDccdBankActno = "";
	String hDccdBinNo = "";
	String hDccdDigitalFlag = "";
	String hDccdBranch = "";
	String hDccdLastConsumeDate = "";
	String hDccdRowid = "";
	String hMaxPurchaseDate = "";
	long tempLong = 0;
	String hAgeIndicator = "";
	String hForceFlag = "";
	String hContiBatchno = "";
	int hContiRecno = 0;
	String hSource = "";
	String hValidFm = "";
	String hChgAddrFlag = "";
	String hThirdRsn = "";
	String hMailType = "";
	int h3rsnMonth1T = 0;
	int h3rsnMonth2F = 0;
	int h3rsnMonth2T = 0;
	int h3rsnMonth3F = 0;
	int h3rsnMonth3T = 0;
	int h3rsnMonth4F = 0;
	long hPtrExtnYear = 0;
	int tempInt = 0;
	int bCd = 0;
	String hExpireReason = "";
	String hExpireChgFlag = "";
	String hExpireChgDate = "";
	String hChangeDate = "";
	String hChangeReason = "";
	String hChangeStatus = "";
	String pCardNo = "";
	String hTmpDate = "";
	String hVip = "";
	int hTmpNo = 0;
	int hTmpRecno = 0;
	String hCptrPtrType = "";
	String hCptrPtrId = "";
	String hCptrPtrValue = "";
	String hCptrDelNote = "";
	String hCptrNotchgRsn = "";

	String hParamDate = "";
	String hExpireAddr = "";
	String currDate = "";
	long readCnt = 0;
	long totCnt = 0;
	int hCount = 0;
	int hNotchgRecno = 0;
	int hDccdCount = 0;
	int hContiRecnoPm = 0;
	int chgCardFlag = 0;
	int cntPtr = 0;
	long intCnt = 0;
	int hSupCount = 0;
	private long intCnt1 = 0;
    String hIdnoBusinessCode = "";
    String hIdnoCompanyName = "";
    String hIdnoJobPosition = "";
    int hIdnoAnnualIncome = 0;
    String hCcaSpecFlag = "";
    String hCcaBlockReason = "";
    String toDay = "";
    String tmpWfValue = "";
	
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

			hModPgm = javaProgram;
			toDay = sysDate;
			
			selectPtrSysParm();
			
			if (args.length != 0)
				hParamDate = args[0].substring(0, 6);
			else {
				if(isLastBusinday(toDay)) {
					showLogMessage("I", "", String.format("今日[%s]為當月的第[%s]個營業日，開始續卡篩選作業", toDay, tmpWfValue));
					hParamDate = selectAddMonth(toDay,1);
				}					
				else {
					  showLogMessage("I", "", String.format("今日[%s]非當月的第[%s]個營業日，程式執行結束", toDay, tmpWfValue));
                      finalProcess();
                      return 0;
				}
			}

			hParamDateLike = String.format("%s%s", hParamDate, "%");

			if (args.length == 1) {
				hCount = 1000;
			} else if (args.length == 2 && args[1].length() < 6) {
				hCount = comcr.str2int(args[1]);
			} else if (args.length == 3) {
				hCount = comcr.str2int(args[1]);
			}

			stderr = String.format("Process DATE=[%s],[%s],[%d]\n", hParamDate, hParamDateLike, hCount);
			showLogMessage("I", "", stderr);
			
			hSystemDate = "";
			hLimitConsumeDate = "";

			sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date, ";
			sqlCmd += "to_char(sysdate -1 YEARS,'yyyymmdd') as h_limit_consume_date ";
			sqlCmd += " from dual ";
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hSystemDate = getValue("h_system_date");
				hLimitConsumeDate = getValue("h_limit_consume_date");
			}

			mainProcess();

			showLogMessage("I", "", String.format("程式執行結束"));
			// ==============================================
			// 固定要做的

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
	void mainProcess() throws Exception {
		hNotchgRecno = 0;
		hContiRecno = 0;
		hDccdCount = 0;
		hContiRecnoPm = 0;

		sqlCmd = "select ";
		sqlCmd += "a.card_no,";
		sqlCmd += "c.id_no,";
		sqlCmd += "c.id_no_code,";
		sqlCmd += "a.corp_no,";
		sqlCmd += "a.group_code,";
		sqlCmd += "a.source_code,";
		sqlCmd += "a.sup_flag,";
		sqlCmd += "a.major_relation,";
		sqlCmd += "d.id_no as major_id,";
		sqlCmd += "d.id_no_code as major_id_code,";
		sqlCmd += "a.major_card_no,";
		sqlCmd += "a.eng_name,";
		sqlCmd += "a.reg_bank_no,";
		sqlCmd += "a.force_flag,";
		sqlCmd += "a.unit_code,";
		sqlCmd += "a.emboss_data,";
		sqlCmd += "a.acct_type,";
		sqlCmd += "b.acct_key,";
		sqlCmd += "a.p_seqno,";
		sqlCmd += "a.new_beg_date,";
		sqlCmd += "a.new_end_date,";
		sqlCmd += "a.current_code,";
		sqlCmd += "a.fee_code,";
		sqlCmd += "a.card_type,";
		sqlCmd += "a.id_p_seqno,";
		sqlCmd += "a.corp_p_seqno,";
		sqlCmd += "a.corp_act_flag,";
		sqlCmd += "a.change_reason,";
		sqlCmd += "a.change_status,";
		sqlCmd += "a.change_date,";
		sqlCmd += "a.reissue_status,";
		sqlCmd += "a.reissue_date,";
		sqlCmd += "a.ic_flag,";
		sqlCmd += "a.ibm_id_code,";
		sqlCmd += "a.old_bank_actno,";
		sqlCmd += "a.acct_no,";
		sqlCmd += "a.bank_actno,";
		sqlCmd += "a.bin_no,";
		sqlCmd += "a.digital_flag,";
		sqlCmd += "a.branch,";
		sqlCmd += "a.last_consume_date,";
		sqlCmd += "a.rowid  as rowid ";
		sqlCmd += " from dba_acno b,dbc_card a, dbc_idno c, dbc_idno d ";
		sqlCmd += "where b.p_seqno       = a.p_seqno ";
		sqlCmd += "  and c.id_p_seqno    = a.id_p_seqno ";
		sqlCmd += "  and d.id_p_seqno    = a.major_id_p_seqno ";
		sqlCmd += "  and current_code    = '0'  ";
		sqlCmd += "  and expire_chg_flag = ''  ";
		sqlCmd += "  and new_card_no     = ''  ";
		sqlCmd += "  and change_date     = ''  ";
		sqlCmd += "  and sup_flag        = '0' ";
		sqlCmd += "  and new_end_date  like ? ";
		sqlCmd += "order by group_code,card_type,id_no,id_no_code,sup_flag ";			
		setString(1, hParamDateLike);
		int recordCnt = selectTable();
		if (debug == 1)
			showLogMessage("I", "", "  Cnt=[" + recordCnt + "]");
		for (int i = 0; i < recordCnt; i++) {
			hDccdCardNo = getValue("card_no", i);
			hDccdId = getValue("id_no", i);
			hDccdIdCode = getValue("id_no_code", i);
			hDccdCorpNo = getValue("corp_no", i);
			hDccdGroupCode = getValue("group_code", i);
			hDccdSourceCode = getValue("source_code", i);
			hDccdSupFlag = getValue("sup_flag", i);
			hDccdMajorRelation = getValue("major_relation", i);
			hDccdMajord = getValue("major_id", i);
			hDccdMajorIdCode = getValue("major_id_code", i);
			hDccdMajorCardNo = getValue("major_card_no", i);
			hDccdEngName = getValue("eng_name", i);
			hDccdRegBankNo = getValue("reg_bank_no", i);
			hDccdForceFlag = getValue("force_flag", i);
			hDccdUnitCode = getValue("unit_code", i);
			hDccdEmbossData = getValue("emboss_data", i);
			hDccdAcctType = getValue("acct_type", i);
			hDccdAcctKey = getValue("acct_key", i);
			hDccdPSeqno = getValue("p_seqno", i);
			hDccdGpNo = getValue("p_seqno", i);
			hDccdNewBegDate = getValue("new_beg_date", i);
			hDccdNewEndDate = getValue("new_end_date", i);
			hDccdCurrentCode = getValue("current_code", i);
			hDccdFeeCode = getValue("fee_code", i);
			hDccdCardType = getValue("card_type", i);
			hDccdIdPSeqno = getValue("id_p_seqno", i);
			hDccdCorpPSeqno = getValue("corp_p_seqno", i);
			hDccdCorpActFlag = getValue("corp_act_flag", i);
			hDccdChangeReason = getValue("change_reason", i);
			hDccdChangeStatus = getValue("change_status", i);
			hDccdChangeDate = getValue("change_date", i);
			hDccdReissueStatus = getValue("reissue_status", i);
			hDccdReissueDate = getValue("reissue_date", i);
			hDccdIcFlag = getValue("ic_flag", i);
			hDccdIbmIdCode = getValue("ibm_id_code", i);
			hDccdOldBankActno = getValue("old_bank_actno", i);
			hDccdAcctNo = getValue("acct_no", i);
			hDccdBankActno = getValue("bank_actno", i);
			hDccdBinNo = getValue("bin_no", i);
			hDccdDigitalFlag = getValue("digital_flag", i);
			hDccdBranch = getValue("branch", i);
			hDccdLastConsumeDate = getValue("last_consume_date", i);
			hDccdRowid = getValue("rowid", i);

			readCnt++;
			if (debug == 1)
				showLogMessage("I", "", " 1 Card_no =[" + hDccdCardNo + "]" + hDccdNewEndDate);

			/****** 續卡中資料不要重覆送 *****/
			if ((hDccdChangeStatus.equals("1")) || (hDccdChangeStatus.equals("2"))) {
				continue;
			}

			if ((hDccdReissueStatus.equals("1")) || (hDccdReissueStatus.equals("2"))) {
				continue;
			}
				
			getPtrExtn();
						
			if (hDccdSupFlag.equals("0")) {
				getDbaAcno();
			}
			
			process();
			hDccdCount++;
		}

		stderr = String.format("篩選總筆數=[%d] 續卡筆數=[%d] 不續卡筆數=[%d]", hDccdCount, hContiRecno ,hNotchgRecno);
		showLogMessage("I", "", stderr);			
	}

	/***********************************************************************/
	void getDbaAcno() throws Exception {
		hActLineOfCreditAmt = 0;
		hChgAddrDate = "";
		hVip = "";
		hActNo = "";
		hRIiskBankNo = "";
		hDaaoBlockReason1 = "";
		hDaaoBlockReason2 = "";
		hDaaoBlockReason3 = "";
		hDaaoBlockReason4 = "";
		hDaaoBlockReason5 = "";
		hPaymentRate = "";
		hBillAddress = "";
		hAcnoZip = "";
		hAcnoAddr1 = "";
		hAcnoAddr2 = "";
		hAcnoAddr3 = "";
		hAcnoAddr4 = "";
		hAcnoAddr5 = "";
		hDaaoBillSendingZip = "";
		hDaaoChgAddrDate = "";
		hDaaoChgAddrDate = "";

		sqlCmd = "select a.line_of_credit_amt,";
		sqlCmd += "a.chg_addr_date,";
		sqlCmd += "a.vip_code,";
		sqlCmd += "a.autopay_acct_no,";
		sqlCmd += "a.risk_bank_no,";
		sqlCmd += "rtrim(b.block_reason1) as h_daao_block_reason1,";
		sqlCmd += "rtrim(b.block_reason2) as h_daao_block_reason2,";
		sqlCmd += "rtrim(b.block_reason3) as h_daao_block_reason3,";
		sqlCmd += "rtrim(b.block_reason4) as h_daao_block_reason4,";
		sqlCmd += "rtrim(b.block_reason5) as h_daao_block_reason5,";
		sqlCmd += "decode(payment_rate1,'','00',payment_rate1)||" + "decode(payment_rate2,'','00',payment_rate2)|| "
				+ "decode(payment_rate3,'','00',payment_rate3)||" + "decode(payment_rate4,'','00',payment_rate4)|| "
				+ "decode(payment_rate5,'','00',payment_rate5)||" + "decode(payment_rate6,'','00',payment_rate6)|| "
				+ "decode(payment_rate7,'','00',payment_rate7)||" + "decode(payment_rate8,'','00',payment_rate8)|| "
				+ "decode(payment_rate9,'','00',payment_rate9)||" + "decode(payment_rate10,'','00',payment_rate10)|| "
				+ "decode(payment_rate11,'','00',payment_rate11)||" + "decode(payment_rate12,'','00',payment_rate12)|| "
				+ "decode(payment_rate13,'','00',payment_rate13)||" + "decode(payment_rate14,'','00',payment_rate14)|| "
				+ "decode(payment_rate15,'','00',payment_rate15)||" + "decode(payment_rate16,'','00',payment_rate16)|| "
				+ "decode(payment_rate17,'','00',payment_rate17)||" + "decode(payment_rate18,'','00',payment_rate18)|| "
				+ "decode(payment_rate19,'','00',payment_rate19)||" + "decode(payment_rate20,'','00',payment_rate20)|| "
				+ "decode(payment_rate21,'','00',payment_rate21)||" + "decode(payment_rate22,'','00',payment_rate22)|| "
				+ "decode(payment_rate23,'','00',payment_rate23)||" + "decode(payment_rate24,'','00',payment_rate24)|| "
				+ "decode(payment_rate25,'','00',payment_rate25) h_payment_rate,";
		sqlCmd += "a.chg_addr_date,";
		sqlCmd += "a.stat_send_paper,";
		sqlCmd += "a.stat_send_internet,";
		sqlCmd += "a.bill_sending_zip,";
		sqlCmd += "a.bill_sending_addr1,";
		sqlCmd += "a.bill_sending_addr2,";
		sqlCmd += "a.bill_sending_addr3,";
		sqlCmd += "a.bill_sending_addr4,";
		sqlCmd += "a.bill_sending_addr5,";
		sqlCmd += "a.bill_sending_addr1||" + "a.bill_sending_addr2||" + "a.bill_sending_addr3||"
				+ "a.bill_sending_addr4||" + "a.bill_sending_addr5 h_bill_address ";
		sqlCmd += " from cca_card_acct b , dba_acno a ";
		sqlCmd += "where a.p_seqno      = ? ";
		sqlCmd += "  and b.debit_flag   = 'Y'       ";
		sqlCmd += "  and b.acno_p_seqno = a.p_seqno ";
		setString(1, hDccdPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_dba_acno not found!", "", comcr.hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hActLineOfCreditAmt = getValueDouble("line_of_credit_amt");
			hChgAddrDate = getValue("chg_addr_date");
			hVip = getValue("vip_code");
			hActNo = getValue("autopay_acct_no");
			hRIiskBankNo = getValue("risk_bank_no");
			hDaaoBlockReason1 = getValue("h_daao_block_reason1");
			hDaaoBlockReason2 = getValue("h_daao_block_reason2");
			hDaaoBlockReason3 = getValue("h_daao_block_reason3");
			hDaaoBlockReason4 = getValue("h_daao_block_reason4");
			hDaaoBlockReason5 = getValue("h_daao_block_reason5");
			hPaymentRate = getValue("h_payment_rate");
			hDaaoChgAddrDate = getValue("chg_addr_date");
			hDaaoStatSendPaper = getValue("stat_send_paper");
			hDaaoStatSendInternet = getValue("stat_send_internet");
			hDaaoBillSendingZip = getValue("bill_sending_zip");
			hAcnoZip = hDaaoBillSendingZip;
			hAcnoAddr1 = getValue("bill_sending_addr1");
			hAcnoAddr2 = getValue("bill_sending_addr2");
			hAcnoAddr3 = getValue("bill_sending_addr3");
			hAcnoAddr4 = getValue("bill_sending_addr4");
			hAcnoAddr5 = getValue("bill_sending_addr5");
			hBillAddress = getValue("h_bill_address");
		}

		return;
	}

	/***********************************************************************/
	void process() throws Exception {
		long intRem;

		if (debug == 1)
			showLogMessage("I", "", " process=[" + "]");

		/* 判斷是否續卡 */
		chgCardFlag = conditionCheck();

		if (chgCardFlag == 0) { /* 可續卡 */
		
			if (hContiRecno == 0) {
				getBatchno(); /* 產生續卡的批號 */
			}
			
			hContiRecno++;
			hContiRecnoPm++;
			getValidDate();
			/* 檢查是否需上地址變更註記 */
			getIdnoData(hDccdIdPSeqno);
			/* 製卡暫存檔 */
			insertDbcEmbossTmp();
			updateDbcCard(1, chgCardFlag, 0);
			procChgSupCard(1, chgCardFlag);
		} 
		else {
			hNotchgRecno++;
			getIdnoData(hDccdIdPSeqno);
			updateDbcCard(2, chgCardFlag, 0);
			procChgSupCard(2, chgCardFlag);
		}

		return;
	}

	/***********************************************************************/
	int conditionCheck() throws Exception {
		int rtn = 0;

	    //卡片超過1年無交易不續卡
	    if(hDccdLastConsumeDate.length() > 0) {
		    if(Integer.parseInt(hDccdLastConsumeDate) < Integer.parseInt(hLimitConsumeDate)) {
			    return (1);
		    }
	    }
	    else {
	    	return (1);
	    }
	  
	    //基本資料不全不續卡(工作狀態、公司名稱、行業別、職稱、年收入)缺一不可,但工作狀態為退休其他可空白
	    //20230428卡部改可續卡
//	    selectDbcIdno();
//	  
//	    if(!hIdnoBusinessCode.equals("1700")) {
//		    if(hIdnoBusinessCode.equals("") || hIdnoCompanyName.equals("") || hIdnoJobPosition.equals("") || hIdnoAnnualIncome == 0) {
//			    return (1);
//		    }
//	    }
	  
	    //特指不續卡
	    selectCcaCardBase();
	  
	    if(hCcaSpecFlag.equals("Y")) {
		    return (1);
	    }
	    
        //凍結不續卡
        selectCcaCardAcct();
        
        if(!hCcaBlockReason.equals("")) {
        	return 1;
        }

		return (0); /* 可續卡 */
	}

	/***********************************************************************/
    void selectDbcIdno() throws Exception {
	    hIdnoBusinessCode = ""; 
	    hIdnoCompanyName = "";  
	    hIdnoJobPosition = "";  
	    hIdnoAnnualIncome = 0;
	
        sqlCmd = "select business_code, ";
        sqlCmd += " company_name,  ";
        sqlCmd += " job_position,  ";
        sqlCmd += " annual_income  ";
        sqlCmd += " from dbc_idno  ";
        sqlCmd += "where id_p_seqno = ? ";
        setString(1, hDccdIdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dbc_idno not found!", "[" + hDccdCardNo + "],[" + hDccdIdPSeqno + "]", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hIdnoBusinessCode = getValue("business_code");
            hIdnoCompanyName  = getValue("company_name");
            hIdnoJobPosition  = getValue("job_position");
            hIdnoAnnualIncome = getValueInt("annual_income");
        }
    }

    /***********************************************************************/
    void selectCcaCardBase() throws Exception {
	    hCcaSpecFlag = ""; 
	
        sqlCmd = "select spec_flag ";
        sqlCmd += " from cca_card_base  ";
        sqlCmd += "where card_no = ? ";
        setString(1, hDccdCardNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_cca_card_base not found!", "[" + hDccdCardNo + "]", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	hCcaSpecFlag = getValue("spec_flag");
        }
    }

    /***********************************************************************/
    void selectCcaCardAcct() throws Exception {
    	hCcaBlockReason = ""; 
	
        sqlCmd = "select block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as block_reason ";
        sqlCmd += " from cca_card_acct  ";
        sqlCmd += "where acno_p_seqno = ? ";
        setString(1, hDccdPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_cca_card_base not found!", "[" + hDccdCardNo + "]", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
        	hCcaBlockReason = getValue("block_reason");
        }
    }

    /***********************************************************************/
	/***
	 * 帶出所有之附卡,並同時做續卡/不續卡
	 * 
	 * @param type
	 * @param rtnValue
	 * @return
	 * @throws Exception
	 */
	int procChgSupCard(int type, int rtnValue) throws Exception {
		sqlCmd = "select ";
		sqlCmd += "a.card_no,";
		sqlCmd += "b.id_no,";
		sqlCmd += "b.id_no_code,";
		sqlCmd += "a.corp_no,";
		sqlCmd += "a.group_code,";
		sqlCmd += "a.source_code,";
		sqlCmd += "a.sup_flag,";
		sqlCmd += "a.major_relation,";
		sqlCmd += "d.id_no       as major_id,";
		sqlCmd += "d.id_no_code  as major_id_code,";
		sqlCmd += "a.major_card_no,";
		sqlCmd += "a.eng_name,";
		sqlCmd += "a.reg_bank_no,";
		sqlCmd += "a.force_flag,";
		sqlCmd += "a.unit_code,";
		sqlCmd += "a.emboss_data,";
		sqlCmd += "a.change_status,";
		sqlCmd += "a.change_date,";
		sqlCmd += "a.reissue_status,";
		sqlCmd += "a.reissue_date,";
		sqlCmd += "a.p_seqno,";
		sqlCmd += "a.acct_type,";
		sqlCmd += "c.acct_key,";
		sqlCmd += "a.current_code,";
		sqlCmd += "a.card_type,";
		sqlCmd += "a.new_beg_date,";
		sqlCmd += "a.new_end_date,";
		sqlCmd += "a.id_p_seqno,";
		sqlCmd += "a.corp_p_seqno,";
		sqlCmd += "a.ic_flag,";
		sqlCmd += "a.ibm_id_code,";
		sqlCmd += "a.old_bank_actno,";
		sqlCmd += "a.acct_no,";
		sqlCmd += "a.bank_actno ";
		sqlCmd += "from dbc_card a, dbc_idno b, dba_acno c, dbc_idno d ";
		sqlCmd += "where a.major_card_no = ? ";
		sqlCmd += "and b.id_p_seqno      = a.id_p_seqno and c.p_seqno = a.p_seqno ";
		sqlCmd += "and d.id_p_seqno      = a.major_id_p_seqno ";
		sqlCmd += "and a.current_code    = '0'  ";
		sqlCmd += "and a.sup_flag        = '1'  ";
		sqlCmd += "and a.expire_chg_flag = '' ";
		sqlCmd += "order by id_no,id_no_code,card_no ";
		setString(1, hDccdMajorCardNo);
		int recordCnt1 = selectTable();
		for (int i = 0; i < recordCnt1; i++) {
			hDccdCardNo = getValue("card_no", i);
			hDccdId = getValue("id_no", i);
			hDccdIdCode = getValue("id_no_code", i);
			hDccdCorpNo = getValue("corp_no", i);
			hDccdGroupCode = getValue("group_code", i);
			hDccdSourceCode = getValue("source_code", i);
			hDccdSupFlag = getValue("sup_flag", i);
			hDccdMajorRelation = getValue("major_relation", i);
			hDccdMajord = getValue("major_id", i);
			hDccdMajorIdCode = getValue("major_id_code", i);
			hDccdMajorCardNo = getValue("major_card_no", i);
			hDccdEngName = getValue("eng_name", i);
			hDccdRegBankNo = getValue("reg_bank_no", i);
			hDccdForceFlag = getValue("force_flag", i);
			hDccdUnitCode = getValue("unit_code", i);
			hDccdEmbossData = getValue("emboss_data", i);
			hDccdChangeStatus = getValue("change_status", i);
			hDccdChangeDate = getValue("change_date", i);
			hDccdReissueStatus = getValue("reissue_status", i);
			hDccdReissueDate = getValue("reissue_date", i);
			hDccdPSeqno = getValue("p_seqno", i);
			hDccdGpNo = getValue("p_seqno", i);
			hDccdAcctType = getValue("acct_type", i);
			hDccdAcctKey = getValue("acct_key", i);
			hDccdCurrentCode = getValue("current_code", i);
			hDccdCardType = getValue("card_type", i);
			hDccdNewBegDate = getValue("new_beg_date", i);
			hDccdNewEndDate = getValue("new_end_date", i);
			hDccdIdPSeqno = getValue("id_p_seqno", i);
			hDccdCorpPSeqno = getValue("corp_p_seqno", i);
			hDccdIcFlag = getValue("ic_flag", i);
			hDccdIbmIdCode = getValue("ibm_id_code", i);
			hDccdOldBankActno = getValue("old_bank_actno", i);
			hDccdAcctNo = getValue("acct_no", i);
			hDccdBankActno = getValue("bank_actno", i);

			processDisplay(5000); // every nnnnn display message
			if (debug == 1) {
				showLogMessage("I", "", "888 Card=[" + hDccdCardNo + "]" + hDccdCardType);
				showLogMessage("I", "", "888   id=[" + hDccdId + "]" + hDccdGroupCode);
				showLogMessage("I", "", "888   dt=[" + hDccdNewEndDate + "]" + hDccdGroupCode);
			}
			if (hDccdNewEndDate.compareTo(hDccdNewEndDate) > 0) {
				continue;
			}
			if ((hDccdChangeStatus.equals("1")) || (hDccdChangeStatus.equals("2"))) {
				continue;
			}
			if ((hDccdReissueStatus.equals("1")) || (hDccdReissueStatus.equals("2"))) {
				continue;
			}

			getIdnoData(hDccdIdPSeqno);

			switch (type) {
			case 1:
				hContiRecno++;
				insertDbcEmbossTmp();
				updateDbcCard(1, rtnValue, 1);
				hSupCount++;
				break;
			case 2:
				updateDbcCard(2, rtnValue, 1);
				hNotchgRecno++;
				break;
			}
		}

		return 0;
	}

	/***********************************************************************/
	void updateDbcCard(int flag, int value, int sup) throws Exception {
		String pCardNo = "";

		hChangeReason = "";
		hChangeStatus = "";
		hExpireChgFlag = "";
		hExpireChgDate = "";
		pCardNo = "";
		/****************** 正附卡號 *********************/
		if (sup == 0) {
			pCardNo = hDccdCardNo;
		} else {
			pCardNo = hDccdCardNo;
		}
		/* 續卡 */
		if (flag == 1) {
			hChangeReason = "1";
			hChangeStatus = "1";
			hChangeDate = sysDate;
			hExpireReason = "";
			hExpireChgFlag = "";
			hExpireChgDate = "";
		}
		/* 不續卡 */
		if (flag == 2) {
			hChangeDate = "";
			hChangeReason = "";
			hChangeStatus = "";
			hExpireChgFlag = "1";
			hExpireChgDate = sysDate;
			insertCrdVendorMail();
		}

		daoTable = "dbc_card";
		updateSQL = " expire_reason   = ?,";
		updateSQL += " expire_chg_flag = ?,";
		updateSQL += " expire_chg_date = ?,";
		updateSQL += " change_date     = ?,";
		updateSQL += " change_reason   = ?,";
		updateSQL += " change_status   = ?,";
		updateSQL += " mod_time        = sysdate,";
		updateSQL += " mod_user        = ?,";
		updateSQL += " mod_pgm         = ?";
		whereStr = "where card_no    = ? ";
		setString(1, hExpireReason);
		setString(2, hExpireChgFlag);
		setString(3, hExpireChgDate);
		setString(4, hChangeDate);
		setString(5, hChangeReason);
		setString(6, hChangeStatus);
		setString(7, hModUser);
		setString(8, prgmId);
		setString(9, pCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("update_dbc_card not found!", "", comcr.hCallBatchSeqno);
		}
		return;
	}

	/***********************************************************************/
	void insertCrdVendorMail() throws Exception {
		if (debug == 1)
			showLogMessage("I", "", " insert vendor_mail =[" + hDccdCardNo + "]");

		setValue("effc_yyyymm", hParamDate);
		setValue("card_no", hDccdCardNo);
		setValue("id_p_seqno", hDccdIdPSeqno);
		setValue("batchno", hContiBatchno);
		setValue("from_mark", "D");
		setValue("crt_date", sysDate);
		setValue("chi_name", hDcioChiName);
		setValue("bill_sending_zip", hAcnoZip);
		setValue("bill_sending_addr1", hAcnoAddr1);
		setValue("bill_sending_addr2", hAcnoAddr2);
		setValue("bill_sending_addr3", hAcnoAddr3);
		setValue("bill_sending_addr4", hAcnoAddr4);
		setValue("bill_sending_addr5", hAcnoAddr5);
		setValue("modify_mark", "N");
		setValue("to_vendor_flag", "N");
		setValue("apr_date", sysDate);
		setValue("apr_user", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", hModUser);
		setValue("mod_pgm", prgmId);
		daoTable = "crd_vendor_mail";
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", " insert vendor_mail duplicate=[" + hDccdCardNo + "]");
		}
		return;
	}

	/***********************************************************************/
	void insertDbcEmbossTmp() throws Exception {
		String hMaxPurchaseDate = "";
		int tempLong = 0;

		hModSeqno = comcr.getModSeq();
		if (debug == 1)
			showLogMessage("I", "", " insert tmp =[" + hDccdCardNo + "]");

		hMaxPurchaseDate = "";
		sqlCmd = "select max(purchase_date) h_max_purchase_date ";
		sqlCmd += " from dbb_bill  ";
		sqlCmd += "where card_no = ? ";
		setString(1, hDccdCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hMaxPurchaseDate = getValue("h_max_purchase_date");
		}

		hSource = "3";
		hAgeIndicator = "";
		intCnt++;

		setValue("chi_name", hDcioChiName);
		setValue("birthday", hDcioBirthday);
		setValueDouble("credit_lmt", hActLineOfCreditAmt);
		setValue("force_flag", hForceFlag);
		setValue("apply_id", hDccdId);
		setValue("apply_id_code", hDccdIdCode);
		setValue("sup_flag", hDccdSupFlag);
		setValue("group_code", hDccdGroupCode);
		setValue("source_code", hDccdSourceCode);
		setValue("eng_name", hDccdEngName);
		setValue("unit_code", hDccdUnitCode);
		setValue("emboss_4th_data", hDccdEmbossData);
		setValue("card_type", hDccdCardType);
		setValue("batchno", hContiBatchno);
		setValueDouble("recno", hContiRecno);
		setValue("corp_no", hDccdCorpNo);
		setValue("pm_id", hDccdMajord);
		setValue("pm_id_code", hDccdMajorIdCode);
		setValue("acct_type", hDccdAcctType);
		setValue("acct_key", hDccdAcctKey);
		setValue("reg_bank_no", hDccdRegBankNo);
		setValue("risk_bank_no", hRIiskBankNo);
		setValue("emboss_source", hSource);
		setValue("to_nccc_code", "Y");
		setValue("card_no", hDccdCardNo);
		setValue("old_card_no", hDccdCardNo);
		setValue("old_beg_date", hDccdNewBegDate);
		setValue("old_end_date", hDccdNewEndDate);
		setValue("change_reason", "1");
		setValue("status_code", "1");
		setValue("reason_code", "");
		setValue("valid_fm", hValidFm);
		setValue("valid_to", hValidTo);
		setValue("major_card_no", hDccdMajorCardNo);
		setValue("major_valid_fm", hDccdMajorCardNo.equals("") ? "" : hValidFm);
		setValue("major_valid_to", hDccdMajorCardNo.equals("") ? "" : hValidTo);
		setValue("chg_addr_flag", hChgAddrFlag);
		setValue("fee_code", hDccdFeeCode);
		setValue("nccc_type", "2");
		setValue("vip", hVip);
		setValue("ic_flag", hDccdIcFlag);
		setValue("apply_ibm_id_code", hDccdIbmIdCode);
		setValue("act_no", hDccdAcctNo);
		setValue("bin_no", hDccdBinNo);
		setValue("digital_flag", hDccdDigitalFlag);
		setValue("branch", hDccdBranch);
		setValue("apply_source", "T");
		setValue("crt_date", sysDate);
		setValue("third_rsn", hThirdRsn);
		// setValue("age_indicator" , h_age_indicator);
		setValue("mail_type", hMailType);
        setValue("confirm_date", sysDate);
        setValue("confirm_user", hModUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_user", hModUser);
		setValue("mod_pgm", prgmId);
		daoTable = "dbc_emboss_tmp";
		insertTable();
		if (dupRecord.equals("Y")) {
			intCnt1++;
		}

	}

	/***********************************************************************/
	void getIdnoData(String idPSeqno) throws Exception {
		hDcioIdPSeqno = "";
		hDcioMarriage = "";
		hDcioNation = "";
		hDcioChiName = "";
		hDcioIdPSeqno = idPSeqno;
		hDcioStudent = "";

		sqlCmd = "select marriage,";
		sqlCmd += "nation,";
		sqlCmd += "chi_name,";
		sqlCmd += "sex,";
		sqlCmd += "birthday,";
		sqlCmd += "decode(student,'','N',student)   as h_dcio_student ";
		sqlCmd += " from dbc_idno  ";
		sqlCmd += "where id_p_seqno = ? ";
		setString(1, hDcioIdPSeqno);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_dbc_idno not found!", "", comcr.hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hDcioMarriage = getValue("marriage");
			hDcioNation = getValue("nation");
			hDcioChiName = getValue("chi_name");
			hDcioSex = getValue("sex");
			hDcioBirthday = getValue("birthday");
			hDcioStudent = getValue("h_dcio_student");
		}
	}

	/***********************************************************************/
	void getValidDate() throws Exception {
		String hBegDate = "";

		hBegDate = "";
		sqlCmd = "select to_char(sysdate,'yyyymm')||'01'   as h_beg_date,";
		sqlCmd += "       to_char(sysdate,'yyyymmdd')       as h_system_date,";
		sqlCmd += "     to_number(to_char(sysdate,'dd'))    as h_system_dd ";
		sqlCmd += "  from dual ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hBegDate = getValue("h_beg_date");
			hSystemDate = getValue("h_system_date");
			hSystemDd = getValueInt("h_system_dd");
		}

		hAddMonths = (int) (hPtrExtnYear * 12);
		if (hSystemDd >= 25) {
			sqlCmd = "select to_char(add_months(sysdate,1),'yyyymm')||'01'  as h_beg_date ";
			sqlCmd += " from dual ";
			recordCnt = selectTable();
			if (recordCnt > 0) {
				hBegDate = getValue("h_beg_date");
			}
		}
		hValidFm = hBegDate;

		hValidTo = "";
		sqlCmd = "select to_char(add_months(to_date(cast(? as varchar(10)) ,'yyyymmdd'),cast(? as int)),'yyyymmdd') as h_valid_to";
		sqlCmd += " from dual ";
		setString(1, hDccdNewEndDate);
		setInt(2, hAddMonths);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hValidTo = getValue("h_valid_to");
		}
		if (debug == 1)
			showLogMessage("I", "",
					" ValidTo=[" + hValidTo + "] cnt=" + recordCnt + ",Mon=" + hAddMonths + "," + hDccdNewEndDate);
	}

	/***********************************************************************/
	void getBatchno() throws Exception {
		int hTmpRecno = 0;
		int hTmpNo = 0;
		String hTmpDate = "";

		sqlCmd = "select to_char(sysdate,'yymmdd') h_tmp_date ";
		sqlCmd += " from dual ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hTmpDate = getValue("h_tmp_date");
		}
			
		sqlCmd = "select max(to_number(nvl(substr(batchno,7,2),0)))+1 h_tmp_no,";
		sqlCmd += "max(recno)+1 h_tmp_recno ";
		sqlCmd += " from dbc_emboss_tmp  ";
		sqlCmd += "where substr(batchno,1,6) = ?  ";
		setString(1, hTmpDate);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hTmpNo = getValueInt("h_tmp_no");
			hTmpRecno = getValueInt("h_tmp_recno");
		}

		if (hTmpNo == 0) {
			hTmpNo = 1;
		}

		hMailType = "1";

		hContiBatchno = String.format("%s%02d", hTmpDate, hTmpNo);
		hContiRecno = hTmpRecno;

		if (debug == 1)
			showLogMessage("I", "", " Batchno =[" + hContiBatchno + "]");
	}

	/***********************************************************************/
	int getPtrExtn() throws Exception {
		/* 取得展期參數 */
		hPtrExtnYear = 0;

		sqlCmd = " select extn_year ";
		sqlCmd += " from crd_item_unit ";
		sqlCmd += " where unit_code = ? ";
		setString(1, hDccdUnitCode);
		int recordCnt = selectTable();
		if (recordCnt > 0)
			hPtrExtnYear = getValueInt("extn_year");
		else
			hPtrExtnYear = 2;
		return (0);
	}
	
    /***********************************************************************/
    void selectPtrSysParm() throws Exception {
      
        sqlCmd  = "select wf_value ";
        sqlCmd += "  from ptr_sys_parm   ";
        sqlCmd += " where wf_parm = 'SYSPARM'  ";
        sqlCmd += "   and wf_key = 'RENEW_CARD' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
      	  tmpWfValue = getValue("wf_value");
        }
        return;
    }
    /***********************************************************************/
	
	/**
	 *  檢核是否為當月第N個營業日。
	 * @return true or false
	 * @throws Exception 
	 */
	public boolean isLastBusinday(String toDay) throws Exception {
		String firstDate = "";
		int day = 1;
		int businDay = 0;
		while(true) {
			firstDate = String.format("%6.6s%02d", toDay,day);
			sqlCmd = "select count(*) as holiday_cnt from ptr_holiday where holiday = ? ";
			setString(1, firstDate);
			selectTable();
			int cnt = getValueInt("holiday_cnt");
			if(cnt <= 0) {
				businDay++;
				if(businDay == 2) {
					break;
				}				
			}
			day++;
		}
		if(toDay.equals(firstDate)) {
			return true;
		}
		return false;
	}

	/***********************************************************************/
	   private String selectAddMonth(String inDate, int idx) throws Exception {
	        selectSQL = "to_char(add_months(to_date( ? ,'yyyymmdd'), ? ),'yyyymm')  as out_date";
	        daoTable = "sysibm.dual";
	        whereStr = "FETCH FIRST 1 ROW ONLY";

	        setString(1, inDate);
	        setInt(2, idx);
	        selectTable();

	        return getValue("out_date");
	    }

	/***************************************************************************/
	public static void main(String[] args) throws Exception {
		DbcC001 proc = new DbcC001();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
	// ************************************************************************
}
