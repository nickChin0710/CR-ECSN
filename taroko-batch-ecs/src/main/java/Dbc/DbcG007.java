/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------  *
*  109/04/23  V1.00.01    Pino      program initial                         *
*  109/06/10  V1.00.02    Pino      select onbat_2ecs                       *
*  109/11/23  V1.00.03  yanghan       修改了變量名稱和方法名稱            *                                                                          *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  110/09/17  V1.00.04  Wilson      修正updateOnbat2ecs、新增總筆數、處理筆數                     *
*  112/02/02  V1.00.05  Wilson      insert apr_user、apr_date改為空白                           *
*  112/04/22  V1.00.06  Wilson      增加extendField                          *
*  112/11/30  V1.00.07  Wilson      修正ACT_NO                               *
*  112/12/11  V1.00.08  Wilson      crd_item_unit不判斷卡種                                                       *
*  113/01/04  V1.00.09  Wilson      重複執行不當掉                                                                                    *
****************************************************************************/

package Dbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*停掛卡資料寫入主檔作業*/
public class DbcG007 extends AccessDAO {

	private String progname = "VD卡停掛重製處理程式 113/01/04 V1.00.09";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	
    int debug = 1;
	String prgmId = "DbcG007";
    String cardNo = "";
    String oppType = "";
    String oppReason = "";
    String oppDate = "";
    String oppTime = "";
    String isRenew = "";
    String mailBranch = "";
    int hStatus = 0;
    String hOnbaTscAutoloadFlag = "";
    String hOnbaRowid = "";
    String pgmName = "";
    String negOppReason = "";
	String rptName1 = "";
	List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
	String errCode = "";
	String errDesc = "";
	String procDesc = "";
	int rptSeq1 = 0;
	int errCnt = 0;
	String buf = "";
	String szTmp = "";
	long hModSeqno = 0;
	String hModTime = "";
	String hModPgm = "";
	String hCallBatchSeqno = "";
	String iFileName = "";
	String iPostDate = "";
	String hCurpModPgm = "";
	String hCurpModTime = "";
	String hCurpModUser = "";
	String hCurpModWs = "";
	long hCurpModSeqno = 0;
	String hCallRProgramCode = "";
	String hDebitFlag = "";
	int liCount = 0;
	String hMbtmGroupCode = "";
	String hMbtmOldCardNo = "";
	String hCardDigitalFlag = "";
	String hCardBinType = "";
	String hCardNegDelDate = "";
	String hTempIbmIdCode = "";
	String hCardIdPSeqno = "";
	String hCardPSeqno = "";
	String hCardAcctPSeqno = "";
	String hMbtmIcFlag = "";
	String hCardReissueReason = "";
	String hCardReissueStatus = "";
	String hCardReissueDate = "";
	String hCardCurrentCode = "";
	String hTempAcctNo = "";
	String hCardOldBankActno = "";
	String hCardBankActno = "";
	String hCardNewCardNo = "";
	String hMbtmAcctType = "";
	String hMbtmCardType = "";
	String hMbtmBinNo = "";
	String hMbtmSupFlag = "";
	String hMbtmUnitCode = "";
	String hMbtmRegBankNo = "";
	String hMbtmEngName = "";
	String hMbtmMemberId = "";
	String hMbtmChangeReason = "";
	String hMbtmPmId = "";
	String hMbtmPmIdCode = "";
	String hCardMajorIdPSeqno = "";
	String hMbtmMajorCardNo = "";
	String hMbtmSourceCode = "";
	String hMbtmCorpNo = "";
	String hMbtmForceFlag = "";
	String hMbtmOldBegDate = "";
	String hMbtmOldEndDate = "";
	String hMbtmEmboss4thData = "";
	String hCardMailType = "";
	String hCardMailNo = "";
	String hCardMailBranch = "";
	String hCardMailProcDate = "";
	String hCardExpireChgFlag = "";
	String hCardExpireChgDate = "";
	String hCardExpireReason = "";
	String hCardBranch = "";
	String hMbtmCrtDate = "";
	String hMbtmAprUser = "";
	String hMbtmAprDate = "";
	String hMbtmModSeqno = "";
	String hMbtmLostFeeCode = "";
	String hMbtmMajorValidFm = "";
	String hMbtmMajoValidTo = "";
	String hMbtmBirthday = "";
	String hMbtmChiName = "";
	String hMbtmRiskBankNo = "";
	double hMbtmCreditLmt = 0;
	String hAcnoStopStatus = "";
	String hIsRc = "";
	String hAcnoAcctPSeqno = "";
	String hAcnoPSeqno = "";
	String hAcnoRcUseIndicator = "";
	String hAcnoDebtCloseDate = "";
	String hAcnoAcctKey = "";
	String hAfterDate = "";
	String hCardAcctType = "";
	String pCardNo = "";
	String pCurrentCode = "";
	String pSupFlag = "";
	String hDiffDate = "";
	String hValidFm = "";
	String hEndDate = "";
	String hValidTo = "";
	String hTmpDate = "";
	int hTmpNo = 0;
	long hTmpRecno = 0;
	int hCount = 0;
	String hMbtmBatchno = "";
	long hMbtmRecno = 0;
	String hMbtmEmbossSource = "";
	String hMbtmEmbossReason = "";
	String hMbtmResendNote = "";
	String hMbtmToNcccCode = "";
	String hMbtmStatusCode = "";
	String hMbtmReasonCode = "";
	String hMbtmApplyId = "";
	String hMbtmApplyIdCode = "";
	String hMbtmValidFm = "";
	String hMbtmValidTo = "";
	String hMbtmChgAddrFlag = "";
	String hMbtmMailType = "";
	String hMbtmEmbossDate = "";
	String hMbtmNcccBatchno = "";
	String hMbtmNcccRecno = "";
	String hMbtmNcccType = "";
	String hMbtmModWs = "";
	String hReissueStatus = "";
	String hReissueReason = "";
	String hReissueDate = "";
	String hCardModPgm = "";
	String hMbtmCardNo = "";
	double tempAmt = 0;
	String hTempOppReason = "";
	String currDate = "";
	String hBatchno = "";
	long liSeqNo = 0;
	int totalCountA = 0;
    int totalCount = 0;
	int hInsOk = 0;
	int hDelOk = 0;
	int errorflag = 0;
	long hRecno = 0;
	int count = 0;
	int hPtrExtnYear = 0;
	int hPtrReissueExtnMm = 0;

	public int mainProcess(String[] args) {

		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式" + prgmId + "已有另依程序啟動中, 不執行..");
				return (0);
			}
			if (args.length != 0 && args.length != 1) {
				comc.errExit("Usage : DbcG007 [callbatch_seqno]", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			// comcr.callbatch(0, 0, 0);
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            selectOnbat2ecs();
			showLogMessage("I", "", String.format("程式執行結束,總筆數=[%d],處理筆數=[%d]", totalCountA, totalCount));
			// ==============================================
			// 固定要做的
			// comcr.callbatch(1, 0, 0);
			showLogMessage("I", "", "執行結束");
			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***************************************************************/
	void getBatchno() throws Exception {
		String hTmpDate = "";
		String hTmpBatchno = "";
		long hTmpRecno = 0;
		int hTmpNo = 0;

		sqlCmd = " select to_char(sysdate+1,'yymmdd') as h_tmp_date from dual ";
		if (selectTable() > 0)
			hTmpDate = getValue("h_tmp_date");

		sqlCmd = " select to_number(nvl(substr(max(batchno),7,2),0)) as h_tmp_no, ";
		sqlCmd += "        nvl(max(recno),0) as h_tmp_recno ";
		sqlCmd += "   from dbc_emboss_tmp ";
		sqlCmd += "  where substr(batchno,1,6) = ? ";
		setString(1, hTmpDate);
		if (selectTable() > 0) {
			hTmpNo = getValueInt("h_tmp_no");
			hTmpRecno = getValueLong("h_tmp_recno");
			hTmpDate = getValue("h_tmp_date");
		}

		if (hTmpNo == 0) {
			hTmpNo = 1;
			hTmpRecno = 0;
		}
		hTmpBatchno = String.format("%s%02d", hTmpDate, hTmpNo);
		hBatchno = hTmpBatchno;
		hRecno = hTmpRecno;

	}

    void selectOnbat2ecs() throws Exception {
        sqlCmd = "select ";
        sqlCmd += " card_no,";
        sqlCmd += " opp_type,";
        sqlCmd += " opp_reason,";
        sqlCmd += " opp_date,";
        sqlCmd += " decode(is_renew,'','N',is_renew) is_renew,";
        sqlCmd += " mail_branch,";
        sqlCmd += " tsc_autoload_flag,";
        sqlCmd += " rowid  rowid ";
        sqlCmd += " from onbat_2ecs  ";
        sqlCmd += "where trans_type  = '6' ";
        sqlCmd += "  and to_which    = '1'";
        sqlCmd += "  and proc_status = '0' ";
        sqlCmd += "order by card_no,dog ";
        extendField = "onbat.";
        int recordCnt = selectTable();
        // if (notFound.equals("Y")) {
        // comcr.err_rtn("select_onbat not found!", "", h_call_batch_seqno);
        // }
        if (debug == 1)
            showLogMessage("I", "", "888 CCA_ONBAT cnt= " + recordCnt);
        for (int i = 0; i < recordCnt; i++) {
        	cardNo    = getValue("onbat.card_no", i);
        	oppType   = getValue("onbat.opp_type", i);
        	oppReason = getValue("onbat.opp_reason", i);
        	oppDate   = getValue("onbat.opp_date", i);
        	isRenew   = getValue("onbat.is_renew", i);
        	mailBranch = getValue("onbat.mail_branch", i);
        	hOnbaTscAutoloadFlag = getValue("onbat.tsc_autoload_flag", i);
            hOnbaRowid = getValue("onbat.rowid", i);
            if (debug == 1)
                showLogMessage("I", "", " 888 card_no=" + cardNo);

		getBatchno();

		if (cardNo.length() == 0 || oppType.length() == 0 || oppReason.length() == 0 
				|| oppDate.length() == 0 || isRenew.length() == 0) {
			showLogMessage("I", "", " param can't null");
            hStatus = 2;
            updateOnbat2ecs();
            continue;
		}
		totalCountA++;
		hDebitFlag = "N";
		sqlCmd = "select decode(debit_flag,'','N',debit_flag) h_debit_flag ";
		sqlCmd += " from ptr_bintable  ";
		sqlCmd += " where 1=1          ";
		sqlCmd += "   and bin_no || bin_no_2_fm || '0000' <= ?  ";
		sqlCmd += "   and bin_no || bin_no_2_to || '9999' >= ?  ";
		setString(1, cardNo);
		setString(2, cardNo);
		int recordCnt2 = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_bintable not found!", "", hCallBatchSeqno);
		}
		if (recordCnt2 > 0) {
			hDebitFlag = getValue("h_debit_flag");
		}

		if (!hDebitFlag.equals("Y")) {
            continue;
		}
		
		totalCount++;
		
		hInsOk = 0;
		hDelOk = 0;
		errorflag = 0;
		errorflag = getCardData();
		if (errorflag != 0) {
            hStatus = 1;
            updateOnbat2ecs();
            continue;
		}
		liCount = 0;
		sqlCmd = "select count(*) li_count ";
		sqlCmd += " from dbc_group_code  ";
		sqlCmd += "where group_code = ? ";
		setString(1, hMbtmGroupCode);
		recordCnt2 = selectTable();
		if (recordCnt2 > 0) {
			liCount = getValueInt("li_count");
		}
		if (liCount > 0) {
            hStatus = 2;
            updateOnbat2ecs();
            continue;
		}
		if ((oppType.equals("2")) || (oppType.equals("5"))) {
			errorflag = chkValidCard();
			if (errorflag != 0) {
				errorflag = updateDbaAcno();
				if (errorflag != 0) {
	                hStatus = 2;
	                updateOnbat2ecs();
	                continue;
				}
			}
		}
		if (isRenew.equals("Y")) {
			hRecno++;
			if (hCardNewCardNo.length() == 0) {
				if (!hCardReissueStatus.equals("2")) {
					insertDbcEmbossTmp();
				}
			}
		}
		if (isRenew.equals("N")) {

			if (!hMbtmLostFeeCode.equals("Y")) {
				insertDbbOthexp();
			}
			delDbcEmbossTmp();
		}
		chkStopStatus();
		updateDbcCard();
		hStatus = 1;
        updateOnbat2ecs();
//V1.00.02刪除
//		if(!oppType.equals("4"))
//			insert_cca_outgoing();
		
		commitDataBase();
        }
	}
        /***********************************************************************/
        void updateOnbat2ecs() throws Exception {
            daoTable   = "onbat_2ecs";
            updateSQL  = " proc_status = ? ,";
            updateSQL += " proc_date   = to_char(sysdate,'yyyymmdd'),";
            updateSQL += " dop         = sysdate";
            whereStr   = " where proc_status = '0' ";
            whereStr  += "   and rowid       = ? ";

            setInt(1, hStatus);
            setRowId(2, hOnbaRowid);
            updateTable();
            // if (notFound.equals("Y")) {
            // comcr.err_rtn("update_onbat_2ecs not found!", "",
            // h_call_batch_seqno);
            // }

        }
	/***********************************************************************/
	int getCardData() throws Exception {
		sqlCmd = "select ";
		sqlCmd += " ibm_id_code,";
		sqlCmd += " id_p_seqno,";
		sqlCmd += " p_seqno,";
		sqlCmd += " ic_flag,";
		sqlCmd += " reissue_reason,";
		sqlCmd += " reissue_status,";
		sqlCmd += " reissue_date,";
		sqlCmd += " current_code,";
		sqlCmd += " old_bank_actno,";
		sqlCmd += " acct_no,";
		sqlCmd += " bank_actno,";
		sqlCmd += " new_card_no,";
		sqlCmd += " acct_type,";
		sqlCmd += " card_type,";
		sqlCmd += " bin_no,";
		sqlCmd += " sup_flag,";
		sqlCmd += " unit_code,";
		sqlCmd += " reg_bank_no,";
		sqlCmd += " eng_name,";
		sqlCmd += " member_id,";
		sqlCmd += " change_reason,";
		sqlCmd += " major_id_p_seqno,";
		sqlCmd += " major_card_no,";
		sqlCmd += " group_code,";
		sqlCmd += " source_code,";
		sqlCmd += " corp_no,";
		sqlCmd += " force_flag,";
		sqlCmd += " new_beg_date,";
		sqlCmd += " new_end_date,";
		sqlCmd += " emboss_data,";
		sqlCmd += " mail_type,";
		sqlCmd += " mail_no,";
		sqlCmd += " mail_branch,";
		sqlCmd += " mail_proc_date,";
		sqlCmd += " expire_chg_flag,";
		sqlCmd += " expire_chg_date,";
		sqlCmd += " expire_reason,";
		sqlCmd += " branch,";
		sqlCmd += " crt_date,";
		sqlCmd += " apr_user,";
		sqlCmd += " apr_date,";
		sqlCmd += " mod_seqno,";
		sqlCmd += " digital_flag,";
        sqlCmd += " bin_type,";
        sqlCmd += " uf_date_add(new_end_date,0,0,1) as neg_del_date,";
		sqlCmd += " lost_fee_code ";
		sqlCmd += " from dbc_card ";
		sqlCmd += "where card_no = ? ";
		setString(1, cardNo);
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_dbc_card not found!", "", hCallBatchSeqno);
		}
		if (recordCnt > 0) {
			hTempIbmIdCode = getValue("ibm_id_code");
			hCardIdPSeqno = getValue("id_p_seqno");
			hCardPSeqno = getValue("p_seqno");
			hCardAcctPSeqno = getValue("p_seqno");
			hMbtmIcFlag = getValue("ic_flag");
			hCardReissueReason = getValue("reissue_reason");
			hCardReissueStatus = getValue("reissue_status");
			hCardReissueDate = getValue("reissue_date");
			hCardCurrentCode = getValue("current_code");
			hCardOldBankActno = getValue("old_bank_actno");
			hTempAcctNo = getValue("acct_no");
			hCardBankActno = getValue("bank_actno");
			hCardNewCardNo = getValue("new_card_no");
			hMbtmAcctType = getValue("acct_type");
			hMbtmCardType = getValue("card_type");
			hMbtmBinNo = getValue("bin_no");
			hMbtmSupFlag = getValue("sup_flag");
			hMbtmUnitCode = getValue("unit_code");
			hMbtmRegBankNo = getValue("reg_bank_no");
			hMbtmEngName = getValue("eng_name");
			hMbtmMemberId = getValue("member_id");
			hMbtmChangeReason = getValue("change_reason");
			hCardMajorIdPSeqno = getValue("major_id_p_seqno");
			hMbtmMajorCardNo = getValue("major_card_no");
			hMbtmGroupCode = getValue("group_code");
			hMbtmSourceCode = getValue("source_code");
			hMbtmCorpNo = getValue("corp_no");
			hMbtmForceFlag = getValue("force_flag");
			hMbtmOldBegDate = getValue("new_beg_date");
			hMbtmOldEndDate = getValue("new_end_date");
			hMbtmEmboss4thData = getValue("emboss_data");
			hCardMailType = getValue("mail_type");
			hCardMailNo = getValue("mail_no");
			hCardMailBranch = getValue("mail_branch");
			hCardMailProcDate = getValue("mail_proc_date");
			hCardExpireChgFlag = getValue("expire_chg_flag");
			hCardExpireChgDate = getValue("expire_chg_date");
			hCardExpireReason = getValue("expire_reason");
			hCardBranch = getValue("branch");
			hMbtmCrtDate = getValue("crt_date");
			hMbtmAprUser = getValue("apr_user");
			hMbtmAprDate = getValue("apr_date");
			hMbtmModSeqno = getValue("mod_seqno");
			hCardDigitalFlag = getValue("digital_flag");
            hCardBinType        = getValue("bin_type");
            hCardNegDelDate    = getValue("neg_del_date");
			hMbtmLostFeeCode = getValue("lost_fee_code");
		}

		hCardAcctType = hMbtmAcctType;
		/*************** 抓取正卡效期 ************************/

		if (hMbtmSupFlag.equals("1")) {
			sqlCmd = "select new_beg_date,";
			sqlCmd += " new_end_date ";
			sqlCmd += " from dbc_card  ";
			sqlCmd += "where card_no = ? ";
			setString(1, hMbtmMajorCardNo);
			recordCnt = selectTable();
			if (recordCnt > 0) {
				hMbtmMajorValidFm = getValue("new_beg_date");
				hMbtmMajoValidTo = getValue("new_end_date");
			}
		}
		sqlCmd = "select id_no,";
		sqlCmd += " id_no_code";
		sqlCmd += "  from dbc_idno ";
		sqlCmd += " where id_p_seqno = ?";
		setString(1, hCardMajorIdPSeqno);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hMbtmPmId = getValue("id_no");
			hMbtmPmIdCode = getValue("id_no_code");
		}

		sqlCmd = "select birthday, ";
		sqlCmd += " chi_name, ";
		sqlCmd += " id_no,";
		sqlCmd += " id_no_code";
		sqlCmd += " from dbc_idno  ";
		sqlCmd += "where id_p_seqno = ?";
		setString(1, hCardIdPSeqno);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hMbtmBirthday = getValue("birthday");
			hMbtmChiName = getValue("chi_name");
			hMbtmApplyId = getValue("id_no");
			hMbtmApplyIdCode = getValue("id_no_code");
		}

		hAcnoStopStatus = "";
		hAcnoAcctPSeqno = "";
		hAcnoPSeqno = "";
		hAcnoAcctKey = "";
		hAcnoRcUseIndicator = "";
		hAcnoDebtCloseDate = "";
		hMbtmCardNo = cardNo;
		hMbtmOldCardNo = cardNo;
		getPtrExtn();
		if (hCardExpireChgFlag.length() == 0) {
			getValidDate();
		} else {
			hMbtmValidFm = hMbtmOldBegDate;
            if(oppType.equals("4")) {
                hMbtmValidTo = comm.lastdateOfmonth(comm.nextMonthDate(hMbtmOldEndDate,hPtrReissueExtnMm));
            }
            else {
            	hMbtmValidTo = hMbtmOldEndDate;
            }
		}
		hIsRc = "";
		sqlCmd = "select risk_bank_no,";
		sqlCmd += " line_of_credit_amt,";
		sqlCmd += " stop_status,";
		sqlCmd += " rc_use_indicator,";
		sqlCmd += " p_seqno,";
		sqlCmd += " acct_key,";
		sqlCmd += " rc_use_indicator,";
		sqlCmd += " debt_close_date ";
		sqlCmd += " from dba_acno  ";
		sqlCmd += "where p_seqno = ? ";
		setString(1, hCardPSeqno);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hMbtmRiskBankNo = getValue("risk_bank_no");
			hMbtmCreditLmt = getValueDouble("line_of_credit_amt");
			hAcnoStopStatus = getValue("stop_status");
			hIsRc = getValue("rc_use_indicator");
			hAcnoAcctPSeqno = getValue("p_seqno");
			hAcnoPSeqno = getValue("p_seqno");
			hAcnoAcctKey = getValue("acct_key");
			hAcnoRcUseIndicator = getValue("rc_use_indicator");
			hAcnoDebtCloseDate = getValue("debt_close_date");
		}

		return (0);
	}

	/***********************************************************************/
	int getPtrExtn() throws Exception {
		/* 取得展期參數 */
		sqlCmd = "select extn_year,reissue_extn_mm ";
		sqlCmd += " from crd_item_unit  ";
		sqlCmd += "where unit_code  = ? ";
		setString(1, hMbtmUnitCode);
		int recordCnt = selectTable();
        if (recordCnt > 0) {
            hPtrExtnYear = getValueInt("extn_year");
            hPtrReissueExtnMm = getValueInt("reissue_extn_mm");
        } else {
            hPtrExtnYear = 2;
            hPtrReissueExtnMm = 0;
        }
		return (0);
	}

	/***********************************************************************/
	void getValidDate() throws Exception {
		String hValidFm = "";
		String hValidTo = "";
		String hDiffDate = "";
		String hEndDate = "";

		hValidFm = "";
		hValidTo = "";
		hDiffDate = "";
		hEndDate = "";
		/****************************************************
		 * 效期大於今日,在六個月以內
		 ****************************************************/
		sqlCmd = "select to_char(add_months(sysdate,6),'yyyymmdd') h_diff_date ";
		sqlCmd += " from dual ";
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hDiffDate = getValue("h_diff_date");
		}
		/******************************************************
		 * 附卡抓正卡效期,再決定是否展期 2001/09/11
		 ******************************************************/
		if (hMbtmSupFlag.equals("1")) {
			hEndDate = hMbtmMajoValidTo;
		} else {
			hEndDate = hMbtmOldEndDate;
		}
		if (hEndDate.compareTo(hDiffDate) <= 0) {
			/************************************************
			 * 本月一號
			 ************************************************/
			sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_valid_fm ";
			sqlCmd += " from dual ";
			int recordCnt2 = selectTable();
			if (recordCnt2 > 0) {
				hValidFm = getValue("h_valid_fm");
			}
			/****************************************************
			 * 展期後
			 ************************************************/
			sqlCmd = "select to_char(add_months(to_date( ? ,'yyyymmdd'), ? *12),'yyyymmdd') h_valid_to";
			sqlCmd += " from dual ";
			setString(1, hEndDate);
			setInt(2, hPtrExtnYear);
			recordCnt2 = selectTable();
			if (recordCnt2 > 0) {
				hValidTo = getValue("h_valid_to");
			}
			hMbtmValidFm = hValidFm;
			hMbtmValidTo = hValidTo;
		} else {
			/************************************************
			 * 本月一號
			 ************************************************/
			sqlCmd = "select to_char(sysdate,'yyyymm')||'01' h_valid_fm ";
			sqlCmd += " from dual ";
			int recordCnt2 = selectTable();
			if (recordCnt2 > 0) {
				hValidFm = getValue("h_valid_fm");
			}
			hMbtmValidFm = hValidFm;
			hMbtmValidTo = hEndDate;
		}
		return;
	}

	/***********************************************************************/
	int chkValidCard() throws Exception {

		sqlCmd = "select ";
		sqlCmd += " card_no,";
		sqlCmd += " current_code,";
		sqlCmd += " sup_flag ";
		sqlCmd += " from dbc_card ";
		sqlCmd += "where acct_type = ? ";
		sqlCmd += "  and p_seqno   = ? ";
		// 這個應該是 p_seqno
		sqlCmd += "order by sup_flag ";
		setString(1, hCardAcctType);
		setString(2, hCardAcctPSeqno);
		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			pCardNo = getValue("card_no", i);
			pCurrentCode = getValue("current_code", i);
			pSupFlag = getValue("sup_flag", i);
			/********* 正常卡 ************/
			if ((pCurrentCode.substring(0, 1).equals("0")) && (!pCardNo.equals(cardNo))) {
				count++;
			}
		}
		/********************************
		 * 無有效卡
		 ********************************/
		if (count <= 0)
			return (1);
		return (0);
	}

	/***********************************************************************/
	int updateDbaAcno() throws Exception {
		String hAfterDate = "";

		if (hAcnoRcUseIndicator.equals("1")) {
			hAfterDate = "";
			sqlCmd = "select to_char(add_months(to_date( ? , 'yyyymmdd'),3),'yyyymmdd') ";
			sqlCmd += " from dual ";
			setString(1, oppDate);
			int recordCnt = selectTable();
			if (recordCnt > 0) {
				hAfterDate = getValue("h_after_date");
			}
			daoTable = "dba_acno";
			updateSQL = " rc_use_b_adj = ?,";
			updateSQL += " rc_use_indicator = '2',";
			updateSQL += " rc_use_s_date = ?,";
			updateSQL += " rc_use_e_date = ?,";
			updateSQL += " mod_time  = sysdate,";
			updateSQL += " mod_pgm   = 'dbc_card',";
			updateSQL += " mod_user  = ?";
			whereStr = "where p_seqno  = ? ";
			setString(1, hAcnoRcUseIndicator);
			setString(2, oppDate);
			setString(3, hAfterDate);
			setString(4, pgmName);
			setString(5, hCardAcctPSeqno);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_dba_acno not found!", "", hCallBatchSeqno);
			}
		}

		return (0);
	}

	/***********************************************************************/
	int insertDbcEmbossTmp() throws Exception {
		int hCount = 0;

		/* 刪除續卡 or 毀損重製待製卡 */
		sqlCmd = "select count(*) h_count ";
		sqlCmd += " from dbc_emboss_tmp  ";
		sqlCmd += "where old_card_no = ?  ";
		sqlCmd += "  and (   emboss_source <> '5' ";
		sqlCmd += "or (    emboss_source = '5'  ";
		sqlCmd += "and emboss_reason not in ('1','3'))) ";
		setString(1, hMbtmOldCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hCount = getValueInt("h_count");
		}
		if (hCount > 0) {
			daoTable = "dbc_emboss_tmp";
			whereStr = "where old_card_no = ? ";
            whereStr += "  and (emboss_source <> '5' or (emboss_source = '5'  ";
            whereStr += "  and emboss_reason not in ('1','3'))) ";
			setString(1, hMbtmOldCardNo);
			deleteTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("delete_dbc_emboss_tmp not found!o=", hMbtmOldCardNo, hCallBatchSeqno);
			}

			daoTable = "dbc_card";
			updateSQL = "expire_chg_flag   = decode(change_status,  '1', '', expire_chg_flag),";
			updateSQL += " expire_reason   = decode(change_status,  '1', '', expire_reason),";
			updateSQL += " expire_chg_date = decode(change_status,  '1', '', expire_chg_date),";
			updateSQL += " change_status   = decode(change_status,  '1', '', change_status),";
			updateSQL += " change_reason   = decode(change_status,  '1', '', change_reason),";
			updateSQL += " change_date     = decode(change_status,  '1', '', change_date),";
			updateSQL += " reissue_status  = decode(reissue_status, '1', '', reissue_status),";
			updateSQL += " reissue_reason  = decode(reissue_status, '1', '', reissue_reason),";
			updateSQL += " reissue_date    = decode(reissue_status, '1', '', reissue_date),";
            updateSQL += " mod_user        = ?,";
            updateSQL += " mod_pgm         = ?,";
			updateSQL += " mod_time  = sysdate";
			whereStr = "where card_no   = ? ";
            setString(1, pgmName);
            setString(2, prgmId);
			setString(3, hMbtmOldCardNo);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_dbc_card not found!", "", hCallBatchSeqno);
			}
		}
		/* 已存在不可重複入檔 */
		sqlCmd = "select count(*) h_count ";
		sqlCmd += " from dbc_emboss_tmp  ";
		sqlCmd += "where old_card_no = ? ";
		setString(1, hMbtmOldCardNo);
		recordCnt = selectTable();
		if (recordCnt > 0) {
			hCount = getValueInt("h_count");
		}
		if (hCount > 0) {
			return (1);
		}
		hMbtmBatchno = hBatchno;
		hMbtmRecno = hRecno;
		/************ 正卡不需寫以下欄位 ***************/
		if (hMbtmSupFlag.equals("0")) {
			hMbtmMajorCardNo = "";
			hMbtmMajorValidFm = "";
			hMbtmMajoValidTo = "";
		}
		hMbtmEmbossSource = "5";
		hMbtmEmbossReason = "";
		hMbtmNcccType = "1";

		switch (Integer.parseInt(oppType)) {
		case 2:
			hMbtmEmbossReason = "1";
			break;
		case 4:
			hMbtmEmbossReason = "2";
			break;
		case 5:
			hMbtmEmbossReason = "3";
			break;
		}
		hMbtmToNcccCode = "Y";

		setValue("batchno", hMbtmBatchno);
		setValueLong("recno", hMbtmRecno);
		setValue("emboss_source", hMbtmEmbossSource);
		setValue("emboss_reason", hMbtmEmbossReason);
		setValue("resend_note", hMbtmResendNote);
		setValue("to_nccc_code", hMbtmToNcccCode);
		setValue("reg_bank_no", hMbtmRegBankNo);
		setValue("risk_bank_no", hMbtmRiskBankNo);
		setValue("card_type", hMbtmCardType);
		setValue("bin_no", hMbtmBinNo);
		setValue("unit_code", hMbtmUnitCode);
		setValue("acct_type", hMbtmAcctType);
		setValue("acct_key", hAcnoAcctKey); // 舊卡放空徝 //SUP 20171018 //from 來哥
		setValue("card_no", "");
		setValue("old_card_no", hMbtmOldCardNo);
		setValue("status_code", hMbtmStatusCode);
		setValue("reason_code", hMbtmReasonCode);
		setValue("sup_flag", hMbtmSupFlag);
		setValue("apply_id", hMbtmApplyId);
		setValue("apply_id_code", hMbtmApplyIdCode);
		setValue("pm_id", hMbtmPmId);
		setValue("pm_id_code", hMbtmPmIdCode);
		setValue("group_code", hMbtmGroupCode);
		setValue("source_code", hMbtmSourceCode);
		setValue("corp_no", hMbtmCorpNo);
		setValue("chi_name", hMbtmChiName);
		setValue("eng_name", hMbtmEngName);
		setValue("birthday", hMbtmBirthday);
		setValue("force_flag", hMbtmForceFlag);
		setValue("valid_fm", hMbtmValidFm);
		setValue("valid_to", hMbtmValidTo);
		setValue("major_card_no", hMbtmMajorCardNo);
		setValue("major_valid_fm", hMbtmMajorValidFm);
		setValue("major_valid_to", hMbtmMajoValidTo);
		setValue("chg_addr_flag", hMbtmChgAddrFlag);
		setValue("mail_type", hMbtmMailType);
		setValueDouble("credit_lmt", hMbtmCreditLmt);
		setValue("emboss_4th_data", hMbtmEmboss4thData);
		setValue("old_beg_date", hMbtmOldBegDate);
		setValue("old_end_date", hMbtmOldEndDate);
		setValue("branch", hCardBranch);
		setValue("crt_date", sysDate);
		setValue("apr_user", "");
		setValue("apr_date", "");
		setValue("emboss_date", hMbtmEmbossDate);
		setValue("nccc_batchno", hMbtmNcccBatchno);
		setValue("nccc_recno", hMbtmNcccRecno);
		setValue("nccc_type", hMbtmNcccType);
		setValue("ic_flag", hMbtmIcFlag);
		setValue("apply_ibm_id_code", hTempIbmIdCode);
		setValue("digital_flag", hCardDigitalFlag);
		setValue("act_no", hTempAcctNo);
		setValue("mail_branch", mailBranch);
		setValue("mod_user", pgmName);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", prgmId);
		setValue("mod_seqno", hMbtmModSeqno);
        setValue("tsc_autoload_flag", hOnbaTscAutoloadFlag);
		daoTable = "dbc_emboss_tmp";
		insertTable();
		if (dupRecord.equals("Y")) {
			comcr.errRtn("insert_dbc_emboss_tmp duplicate!", "", hCallBatchSeqno);
		}

		hInsOk = 1;

		return (0);
	}

	/***********************************************************************/
	void insertDbbOthexp() throws Exception {
		int tempAmt = 0;

		tempAmt = 0;
		sqlCmd = "select decode(cast(? as varchar(8)), '0', NORMAL_MAJOR, NORMAL_SUB) ";
		sqlCmd += " from dba_lostgrp  ";
		sqlCmd += "where acct_type  = ?  ";
		sqlCmd += "  and group_code = ?  ";
		sqlCmd += "  and lost_code  = ? ";
		setString(1, hMbtmSupFlag);
		setString(2, hMbtmAcctType);
		setString(3, hMbtmGroupCode);
		setString(4, hTempOppReason);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			tempAmt = getValueInt("temp_amt");
		}

		if (tempAmt > 0) {
			sqlCmd = "select seq_no ";
			sqlCmd += " from dbb_othexp  ";
			sqlCmd += "where bill_type = 'OSSG'  ";
			sqlCmd += "  and txn_code  = 'LF'  ";
			sqlCmd += "  and add_item  = 'LF'  ";
			sqlCmd += "  and card_no   = ? ";
			setString(1, hMbtmOldCardNo);
			int recordCnt2 = selectTable();
			if (recordCnt2 > 0) {
				liSeqNo = getValueLong("seq_no");
			} else {
				setValue("bill_type", "OSSG");
				setValue("txn_code", "LF");
				setValue("add_item", "LF");
				setValue("card_no", hMbtmOldCardNo);
				setValueInt("seq_no", 1);
				setValueInt("dest_amt", tempAmt);
				setValue("dest_curr", "901");
				setValue("purchase_date", sysDate);
				setValue("chi_desc", "掛失費");
				setValue("bill_desc", "掛失費");
				setValue("dept_flag", "");
				setValue("apr_flag", "Y");
				setValue("post_flag", "N");
				setValue("mod_pgm", prgmId);
				setValue("mod_time", sysDate + sysTime);
				daoTable = "dbb_othexp";
				insertTable();
			}
		}
	}

	/***********************************************************************/
	int delDbcEmbossTmp() throws Exception {
		daoTable = "dbc_emboss_tmp";
		whereStr = "where old_card_no   = ?  ";
		whereStr += "  and emboss_source = '5' ";
		setString(1, cardNo);
		deleteTable();
		if (notFound.equals("Y")) {
		} else {
			hDelOk = 1;
		}
		return (0);
	}

	/***********************************************************************/
	void chkStopStatus() throws Exception {
		if (hAcnoStopStatus.equals("Y")) {

			daoTable = "dba_acno";
			updateSQL = "stop_status=''";
			whereStr = "where p_seqno = ? ";
			setString(1, hCardPSeqno);
			updateTable();
			if (notFound.equals("Y")) {
				comcr.errRtn("update_dba_acno not found!", "", hCallBatchSeqno);
			}
		}
		return;
	}

	/***********************************************************************/
	void updateDbcCard() throws Exception {
		String hReissueStatus = "";
		String hReissueReason = "";
		String hReissueDate = "";

		hReissueStatus = hCardReissueStatus;
		hReissueReason = hCardReissueReason;
		hReissueDate = hCardReissueDate;
		/***************************************************************************
		 * h_ins_ok=1,insert_dbc_Emboss_tmp成功 2001/11/15
		 ***************************************************************************/
		if ((isRenew.equals("Y")) && (hInsOk == 1)) {
			hReissueStatus = "1";
			hReissueReason = hMbtmEmbossReason;
			hReissueDate = currDate;
		}
		/******************************************************************
		 * 若由重製卡變成不重製卡時,必須先檢核是否在dbc_Emboss_tmp, 若存在,且確定刪除成功後才可將,reissue_status等清空,否則不變
		 ******************************************************************/
		if ((isRenew.equals("N")) && (hDelOk == 1)) {
			hReissueStatus = "";
			hReissueReason = "";
			hReissueDate = "";
		}
		daoTable = "dbc_card";
//V1.00.02刪除
//		updateSQL = " current_code   = ?,";
//		updateSQL += " oppost_reason  = ?,";
//		updateSQL += " oppost_date    = ?,";
		updateSQL += " reissue_status = ?,";
		updateSQL += " reissue_reason = ?,";
		updateSQL += " reissue_date   = ?,";
		updateSQL += " mod_user       = ?,";
		updateSQL += " mod_time       = sysdate,";
		updateSQL += " mod_pgm        = ?";
		whereStr = "where card_no   = ? ";
//		setString(1, oppType);
//		setString(2, oppReason);
//		setString(3, oppDate);
		setString(1, hReissueStatus);
		setString(2, hReissueReason);
		setString(3, hReissueDate);
		setString(4, pgmName);
		setString(5, prgmId);
		setString(6, cardNo);
		updateTable();

		return;
	}
	/**************************************************************************/
	void insertCcaOutgoing() throws Exception {
		selectCcaOppTypeReason();
		   setValue("crt_date"        , sysDate);
		   setValue("crt_time"        , sysTime);
		   setValue("card_no"         , cardNo);
		   setValue("key_value"       , "FISC");
		   setValue("key_table"       , "OPPOSITION");
		   setValue("bitmap"          , "");
		   setValue("act_code"        , "1");
		   setValue("crt_user"        , pgmName);
		   setValue("proc_flag"       , "1");
		   setValue("send_times"      , "1");
		   setValue("proc_date"       , sysDate);
		   setValue("proc_time"       , sysTime);
		   setValue("proc_user"       , pgmName);
		   setValue("data_from"       , "1");
		   setValue("resp_code"       , "");
		   setValue("data_type"       , "OPPO");
		   setValue("bin_type"        , hCardBinType);
		   setValue("reason_code"     , negOppReason);
		   setValue("del_date"        , hCardNegDelDate);
		   setValue("bank_acct_no"    , "");
		   setValue("vmj_regn_data"   , "");
		   setValue("vip_amt"         , "0");
		   setValue("mod_time"        , sysDate + sysTime);
		   setValue("mod_pgm"         , prgmId);
		   daoTable = "cca_outgoing";
		   insertTable();
		   if (dupRecord.equals("Y")) {
		       comcr.errRtn("insert_ich_b09b_bal duplicate!", "", comcr.hCallBatchSeqno);
		   }

		}
	/**************************************************************************/
	void selectCcaOppTypeReason() throws Exception 
	{
	  sqlCmd  = " SELECT neg_opp_reason ";
	  sqlCmd += "   FROM cca_opp_type_reason ";
	  sqlCmd += "  where opp_status = ? ";
	  setString(1, oppReason);

	  int recordCnt = selectTable();
	  if (recordCnt > 0) {
		  negOppReason = getValue("neg_opp_reason");
	    }

	  if(debug == 1) showLogMessage("I", "", String.format("select_cca_opp_type_reason neg_opp_reason=[%s]", negOppReason));
	}
	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcG007 proc = new DbcG007();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}

}
