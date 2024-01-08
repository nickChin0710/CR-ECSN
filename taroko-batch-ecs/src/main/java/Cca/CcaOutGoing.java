/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
*  111-04-06  V1.00.01    Ryan     program initial                            *
*  111-04-12  V1.00.02    Ryan     NCCC格式修正 , hNegReason = fiscReason         *
*  111-05-30  V1.00.03    Ryan     增加cca_outgoing撤掛邏輯                                                              *
*  111-06-08  V1.00.04    Ryan     拿掉updateCrdCard insertOnbat2ecs 修改insertCcaOpposition *
*  111-06-10  V1.00.05    Ryan     增加NCCC或Fisc原因碼為空值不送outgoing邏輯，增加作業別2  *
*  111-07-28  V1.00.06    Ryan     票證 current_code = '0' -> current_code = '2' *
*  111-10-21  V1.00.07    Ryan     增加凍結、特指送黑名單功能                                                                        *
*  111-12-07  V1.00.08    Ryan     cca_outgoing data_from 改為2 代表批次執行                          *
*  111-12-14  V1.00.09    Ryan     增加解凍功能、票證可能有多筆，修改只讀有效起日最新的一筆                         *
*  112-08-22  V1.00.10    Ryan     FISC與NCCC的reason_code改為一致             *
*  112-12-11  V1.00.11    Ryan     修正未設定原因碼問題
 ******************************************************************************/
package Cca;

import java.sql.Connection;

import com.AccessDAO;
import com.CommCrd;

public class CcaOutGoing extends AccessDAO {
	private final String progname = "call ccaoutgoing 112/12/11 V.00.11";
	CommCrd comc = new CommCrd();
	protected com.CommString zzStr = new com.CommString();
	
	private boolean ibDebit = false;
	private String hCardNo = "";
	private String hCurrentCode = "";
	private String hOppostReason = "";
	private String hOppostDate = "";
	private String hBinType = "";
	private String hNegDelDate = "";
	private String hLostFeeCode = "";
	private String hExcepFlag = "";
	private String hNegReason = "";
	private String hFiscReason = "";
	private String hVmjReason = "";
	private double hCardAcctIdx = 0;
	private String hCardType = "";
	private String hGroupCode = "";
//	private String hAcctType = "";
//	private String hAcnoPSeqno = "";
//	private String hIdPSeqno = "";
	private String hElectronicCode = "";
	private String hElectronicCardNo = "";
	private String hNewEndDate = "";
	private String modUser = "";
	private int cardNotFoundCnt = 0;
	boolean isUpdate = false;

	public CcaOutGoing( Connection conn[], String[] dbAlias) throws Exception {
	    super.conn = conn;
	    setDBalias(dbAlias);
	    setSubParm(dbAlias);
	}

	public int InsertCcaOutGoing(String cardNo, String currentCode, String oppostDate, String oppostReason) throws Exception {
		showLogMessage("I", "", javaProgram + " " + progname);
		initData();
		hCardNo = cardNo;
		hCurrentCode = currentCode;
		hOppostDate = oppostDate;
		hOppostReason = oppostReason;
		dateTime();
		
		modUser = comc.commGetUserID();
		javaProgram = this.getClass().getName();

		if(zzStr.empty(hOppostDate)) {
			hOppostDate = sysDate;
		}
		
		int cnt = selectCardData();
		if(cnt != 1) {
			cardNotFoundCnt++;
			return -1;
		}
		
		if(zzStr.empty(hCurrentCode)) {
			showLogMessage("I", "", String.format("停掛類別為空值 ,card_no = [%s]", hCardNo));
			return -1;
		}
		
		selectOpptypeReason();
		if(zzStr.empty(hNegReason) || zzStr.empty(hFiscReason) ) {
			showLogMessage("I", "", String.format("NCCC或Fisc 未設定原因碼 ,card_no = [%s]", hCardNo));
			return -1;
		}
		
		if(!hCurrentCode.equals("0")) {
			isUpdate = selectCmsChgcolumnLog();
		}
//		updateCrdCard();
		insertCcaOpposition();
//		insertOnbat2ecs();
		oppoNegReq();
		oppoFiscReq();
		if(hBinType.equals("J")) {
			oppoJcbReq();
		}
		if(hBinType.equals("V")) {
			oppoVisaReq();
		}
		if(hBinType.equals("M")) {
			oppoMaster2Req();
		}
		if(hCurrentCode.equals("2")) {
			if(hElectronicCode.equals("01")) {
				oppoTscReq();
			}
			if(hElectronicCode.equals("02")) {
				oppoIpsReq();
			}
			if(hElectronicCode.equals("03")) {
				oppoIchReq();
			}
		}
		showLogMessage("I", "", "");
		return 1;
	}
	//2022-10-21 V1.00.09 Ryan 增加凍結、特指送黑名單功能
	public int InsertCcaOutGoingBlock(String cardNo, String currentCode, String oppostDate, String oppostReason) throws Exception {
		showLogMessage("I", "", javaProgram + " " + progname);
		initData();
		hCardNo = cardNo;
		hCurrentCode = currentCode;
		hOppostDate = oppostDate;
		hOppostReason = oppostReason;
		dateTime();
		
		modUser = comc.commGetUserID();
		javaProgram = this.getClass().getName();

		if(zzStr.empty(hOppostDate)) {
			hOppostDate = sysDate;
		}

		int cnt = selectCardData();
		if(cnt != 1) {
			cardNotFoundCnt++;
			return -1;
		}
		
		getSpecReason(hBinType,hOppostReason);
		if(zzStr.empty(hNegReason) || zzStr.empty(hFiscReason) ) {
			showLogMessage("I", "", String.format("NCCC或Fisc 未設定原因碼 ,card_no = [%s]", hCardNo));
			return -1;
		}
		
		insertCcaOutgoingBlock("NCCC",hNegReason,"");
		insertCcaOutgoingBlock("FISC",hFiscReason,"");
		if(hBinType.equals("J")) {
			if(!zzStr.empty(hVmjReason))
				insertCcaOutgoingBlock("JCB",hVmjReason,"00000");
		}
		if(hBinType.equals("V")) {
			if(!zzStr.empty(hVmjReason))
				insertCcaOutgoingBlock("VISA",hVmjReason,"         ");
		}
		if(hBinType.equals("M")) {
			if(!zzStr.empty(hVmjReason))
				insertCcaOutgoingBlock("MASTER2",hVmjReason,"");
		}

		return 1;
	}

	//111-12-14  V1.00.09    Ryan     增加解凍功能
	public int deleteCcaOutGoingBlock(String cardNo, String currentCode, String oppostDate, String oppostReason)
			throws Exception {
		showLogMessage("I", "", javaProgram + " " + progname);
		initData();
		hCardNo = cardNo;
		hCurrentCode = currentCode;
		hOppostDate = oppostDate;
		hOppostReason = oppostReason;
		dateTime();

		modUser = comc.commGetUserID();
		javaProgram = this.getClass().getName();

		if (zzStr.empty(hOppostDate)) {
			hOppostDate = sysDate;
		}

		int cnt = selectCardData();
		if (cnt != 1) {
			cardNotFoundCnt++;
			return -1;
		}

		getSpecReason(hBinType, hOppostReason);
		if (zzStr.empty(hNegReason) || zzStr.empty(hFiscReason)) {
			showLogMessage("I", "", String.format("NCCC或Fisc 未設定原因碼 ,card_no = [%s]", hCardNo));
			return -1;
		}

		deleteCcaOutgoingBlock("NCCC", hNegReason, "");
		deleteCcaOutgoingBlock("FISC", hFiscReason, "");
		if (hBinType.equals("J")) {
			if (!zzStr.empty(hVmjReason))
				deleteCcaOutgoingBlock("JCB", hVmjReason, "00000");
		}
		if (hBinType.equals("V")) {
			if (!zzStr.empty(hVmjReason))
				deleteCcaOutgoingBlock("VISA", hVmjReason, "         ");
		}
		if (hBinType.equals("M")) {
			if (!zzStr.empty(hVmjReason))
				deleteCcaOutgoingBlock("MASTER2", hVmjReason, "");
		}

		return 1;
	}
	/***********************************************************************/
	private boolean isDebitcard() throws Exception {
		String lsCardNo = hCardNo;
		if (lsCardNo.length() < 6)
			return false;

		sqlCmd = "select count(*) as xx_cnt" + " from ptr_bintable"
				+ " where ? between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')"
				+ " and debit_flag ='Y'";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt <= 0)
			return false;

		if (getValueDouble("xx_cnt") > 0)
			return true;

		return false;
	}
	
	/***********************************************************************/
	private void selectOpptypeReason() throws Exception {

		sqlCmd = "select neg_opp_reason, fisc_opp_code, vis_excep_code, mst_auth_code, jcb_excp_code"
				+ " from cca_opp_type_reason" + " where 1=1 " + " and opp_status = ?";
		setString(1, hOppostReason);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hExcepFlag = "Y";
//			hNegReason = getValue("neg_opp_reason");
			String fiscOppCode = getValue("fisc_opp_code");
			hNegReason = fiscOppCode;
			if (hBinType.equals("V")) {
				hVmjReason = getValue("vis_excep_code");
				if(fiscOppCode.equals("Q") || fiscOppCode.equals("R")) {
					hNegReason = "U";
				}
			}
			if (hBinType.equals("M")) {
				hVmjReason = getValue("mst_auth_code");
				if(fiscOppCode.equals("Q") || fiscOppCode.equals("R")) {
					hNegReason = "U";
				}
			}
			if (hBinType.equals("J")) {
				hVmjReason = getValue("jcb_excp_code");
				if(fiscOppCode.equals("L")) {
					hNegReason = "41";
		    	}
		    	if(fiscOppCode.equals("S")) {
		    		hNegReason = "43";
		    	}
		    	if(fiscOppCode.equals("C")) {
		    		hNegReason = "04";
		    	}
		    	if(fiscOppCode.equals("F")) {
		    		hNegReason = "07";
		    	}
		    	if(fiscOppCode.equals("U")) {
		    		hNegReason = "05";
		    	}
		    	if(fiscOppCode.equals("R")) {
		    		hNegReason = "01";
		    	}
		    	if(fiscOppCode.equals("Q")) {
		    		hNegReason = "05";
		    	}
			}
			hFiscReason = hNegReason;
		} else {
			hExcepFlag = "N";
			hNegReason = "";
			hFiscReason = "";
			hVmjReason = "";
		}

	}
	
	//2022-10-21 V1.00.09 Ryan 增加凍結、特指送黑名單功能
	private boolean getSpecReason(String binType ,String oppostReason) throws Exception {
		sqlCmd =  "SELECT VISA_REASON, MAST_REASON , JCB_REASON , SEND_IBM , NEG_REASON ";
		sqlCmd += "from cca_spec_code ";
		sqlCmd += "where spec_code = ? ";
		setString(1, oppostReason);
		int cnt = selectTable();

		if (cnt > 0) {
			switch (binType) {
			case "V":
				hVmjReason = getValue("VISA_REASON");
				break;
			case "M":
				hVmjReason = getValue("VISA_REASON");
				break;
			case "J":
				hVmjReason = getValue("VISA_REASON");
				break;
			}

			String negReason = getValue("NEG_REASON");
			hNegReason = negReason;
			if (hBinType.equals("V") || hBinType.equals("M")) {
				if(negReason.equals("Q") || negReason.equals("R")) {
					hNegReason = "U";
				}
			}
			if (hBinType.equals("J")) {
				if(negReason.equals("L")) {
					hNegReason = "41";
		    	}
		    	if(negReason.equals("S")) {
		    		hNegReason = "43";
		    	}
		    	if(negReason.equals("C")) {
		    		hNegReason = "04";
		    	}
		    	if(negReason.equals("F")) {
		    		hNegReason = "07";
		    	}
		    	if(negReason.equals("U")) {
		    		hNegReason = "05";
		    	}
		    	if(negReason.equals("R")) {
		    		hNegReason = "01";
		    	}
		    	if(negReason.equals("Q")) {
		    		hNegReason = "05";
		    	}
			}
			hFiscReason = hNegReason;
		}else {
			showLogMessage("I", "", String.format("SELECT cca_spec_code not found, where spec_code = %s ",oppostReason));
			return false;
		}
		return true;
	}
	
	/***********************************************************************/
	private int selectCardData() throws Exception {
		ibDebit = isDebitcard();
		if (ibDebit) {
			sqlCmd = "select C.bin_type , C.current_code ,C.oppost_reason ,uf_date_add(C.new_end_date,0,0,1) as neg_del_date ,C.lost_fee_code ,C.card_type ,C.group_code ,C.acct_type "
					+ " ,C.p_seqno as acno_p_seqno ,C.id_p_seqno ,C.electronic_code ,C.new_end_date "
					+ " from dbc_idno A, dba_acno B, dbc_card C " + " where card_no = ? "
					+ " and A.id_p_seqno = C.id_p_seqno and B.p_seqno = C.p_seqno ";
		} else {
			sqlCmd = "select C.bin_type , C.current_code ,C.oppost_reason ,uf_date_add(C.new_end_date,0,0,1) as neg_del_date ,C.lost_fee_code ,C.card_type ,C.group_code ,C.acct_type "
					+ " ,C.acno_p_seqno ,C.id_p_seqno ,C.electronic_code ,C.new_end_date " 
					+ " from crd_idno A, act_acno B, crd_card C "
					+ " where card_no = ? " + " and A.id_p_seqno = C.id_p_seqno and B.acno_p_seqno = C.acno_p_seqno ";
		}

		setString(1, hCardNo);
		int recordCnt = selectTable();

		if (recordCnt <= 0) {
			showLogMessage("I", "", String.format("select CRD[DBC]_CARD not found,card_no = [%s]", hCardNo));
			return -1;
		}
		if(zzStr.empty(hCurrentCode)) {
			hCurrentCode =  getValue("current_code");
		}
		if(zzStr.empty(hOppostReason)) {
			hOppostReason = getValue("oppost_reason");
		}
		hBinType = getValue("bin_type");
		hNegDelDate = getValue("neg_del_date");
		hLostFeeCode = getValue("lost_fee_code");
		hCardType = getValue("card_type");
		hGroupCode = getValue("group_code");
//		hAcctType = getValue("acct_type");
//		hAcnoPSeqno = getValue("acno_p_seqno");
//		hIdPSeqno = getValue("id_p_seqno");
		hElectronicCode = getValue("electronic_code");
		hNewEndDate = getValue("new_end_date");
		return 1;
	}
	
	/***********************************************************************/
//	private void updateCrdCard() throws Exception {
//		if (ibDebit == false) {
//			daoTable = "crd_card";
//		} else {
//			daoTable = "dbc_card";
//		}
//
//		if(hCurrentCode.equals("0")) {
//			updateSQL = " current_code = '0' ,oppost_date = '' ,oppost_reason = '' ,lost_fee_code = '' "
//					+ ",mod_pgm = ? , mod_user = ? ";
//			whereStr = " where card_no = ? ";
//
//			setString(1, javaProgram);
//			setString(2, modUser);
//			setString(3, hCardNo);
//		}else {
//			updateSQL = " current_code = ? , " + " oppost_date = ? , " + " oppost_reason = ? , "
//					+ " mod_pgm = ? , mod_user = ? ";
//			whereStr = " where card_no = ? ";
//
//			setString(1, hCurrentCode);
//			setString(2, hOppostDate);
//			setString(3, hOppostReason);
//			setString(4, javaProgram);
//			setString(5, modUser);
//			setString(6, hCardNo);
//		}
//		updateTable();
//		if (notFound.equals("Y")) {
//			showLogMessage("I", "", String.format("update CRD[DBC]_CARD not found,card_no = [%s]", hCardNo));
//		}
//		if(hCurrentCode.equals("0")) {
//			return;
//		}
//		wfDelSpecial();
//	}
	
	/***********************************************************************/
//	private void wfDelSpecial() throws Exception {
//
//		sqlCmd = "select spec_flag,card_acct_idx from cca_card_base" + " where card_no = ?";
//		setString(1, hCardNo);
//		int recordCnt = selectTable();
//		if (recordCnt <= 0)
//			return;
//
//		hCardAcctIdx = getValueDouble("card_acct_idx");
//		// -無特指, 偽卡停用-
//		if (!getValue("spec_flag").equals("Y"))
//			return;
//
//		// --
//		daoTable = "cca_card_base";
//		updateSQL = " spec_status =''" + ", spec_flag ='N'" + ", spec_mst_vip_amt =0" + ", spec_del_date =''"
//				+ ", spec_remark =''" + ", mod_pgm = ? , mod_user = ? ";
//		whereStr = " where card_no = ?";
//		setString(1, javaProgram);
//		setString(2, modUser);
//		setString(3, hCardNo);
//		updateTable();
//		if (notFound.equals("Y")) {
//			showLogMessage("I", "", String.format("update CCA_CARD_BASE not found,card_no = [%s]", hCardNo));
//			return;
//		}
//
//		daoTable = "cca_special_visa";
//		whereStr = " where card_no = ? ";
//		setString(1, hCardNo);
//		deleteTable();
//		if (notFound.equals("Y")) {
////			showLogMessage("I", "", String.format("delete cca_special_visa not found,card_no = [%s]", hCardNo));
//			return;
//		}
//
//		// -insert cca_spec_his-
//		setValue("log_date", sysDate);
//		setValue("log_time", sysTime);
//		setValue("card_no", hCardNo);
//		setValue("bin_type", hBinType);
//		setValue("from_type", "1");
//		setValue("aud_code", "D");
//		setValue("pgm_id", javaProgram);
//		setValue("log_user", modUser);
//		daoTable = "cca_spec_his";
//		insertTable();
//		if (dupRecord.equals("Y")) {
//			showLogMessage("I", "", String.format("insert cca_spec_his error,card_no = [%s]", hCardNo));
//			return;
//		}
//	}
	
	/***********************************************************************/
	private void insertCcaOpposition() throws Exception {
//		int i = 1;
		daoTable = "cca_opposition";
//		updateSQL = "from_type = '1' ";
//		updateSQL += ",oppo_type = ? ";
//		updateSQL += ",oppo_status = ? ";
//		if(!hCurrentCode.equals("0")){
//			updateSQL += ",oppo_user = ? ";
//			updateSQL += ",oppo_date = ? ";
//			updateSQL += ",oppo_time = to_char(sysdate,'hh24miss') ";
//		}
//		updateSQL += ",neg_del_date = ? ";
//		updateSQL += ",renew_flag = 'N' ";
//		updateSQL += ",cycle_credit = '' ";
//		updateSQL += ",opp_remark = '' ";
//		updateSQL += ",mail_branch = '' ";
//		updateSQL += ",lost_fee_flag = ? ";
//		updateSQL += ",excep_flag = ? ";
//		updateSQL += ",except_proc_flag = '0' ";
//		updateSQL += ",neg_resp_code = '' ";
//		updateSQL += ",visa_resp_code = '' ";
//		updateSQL += ",mst_reason_code = ? ";
//		updateSQL += ",vis_reason_code = ? ";
//		updateSQL += ",fisc_reason_code = ? ";
//		updateSQL += ",curr_tot_tx_amt = 0 ";
//		updateSQL += ",curr_tot_cash_amt = 0 ";
//		updateSQL += ",bank_acct_no = '' ";
//		if(hCurrentCode.equals("0")) {
//			updateSQL += ",logic_del = 'Y' ";
//			updateSQL += ",logic_del_date = to_char(sysdate,'yyyymmdd') ";
//			updateSQL += ",logic_del_time = to_char(sysdate,'hh24miss') ";
//			updateSQL += ",logic_del_user = 'ecs' ";
//		}else {
//			updateSQL += ",logic_del = '' ";
//			updateSQL += ",logic_del_date = '' ";
//			updateSQL += ",logic_del_time = '' ";
//			updateSQL += ",logic_del_user = '' ";
//		}
//		if(!hCurrentCode.equals("0")){
//			updateSQL += ",chg_date = ? ";
//			updateSQL += ",chg_time = to_char(sysdate,'hh24miss') ";
//			updateSQL += ",chg_user = ? ";
//		}
		updateSQL = "mod_user = ? ";
		updateSQL += ",mod_pgm = ? ";
		updateSQL += ",mod_time = sysdate ";
		whereStr = "where card_no = ? ";
//		setString(i++, hCurrentCode);
//		setString(i++, hOppostReason);
//		if(!hCurrentCode.equals("0")){
//			setString(i++, modUser);
//			setString(i++, hOppostDate);
//		}
//		setString(i++, hNegDelDate);
//		setString(i++, hLostFeeCode);
//		setString(i++, hExcepFlag);
//		setString(i++, hNegReason);
//		setString(i++, hVmjReason);
//		setString(i++, hFiscReason);
//		if(!hCurrentCode.equals("0")){
//			setString(i++, hOppostDate);
//			setString(i++, modUser);
//		}
		setString(1, modUser);
		setString(2, javaProgram);
		setString(3, hCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			setValue("card_no", hCardNo);
			setValueDouble("card_acct_idx", hCardAcctIdx);
			setValue("debit_flag", (ibDebit == true) ? "Y" : "N");
			setValue("card_type", hCardType);
			setValue("bin_type", hBinType);
			setValue("group_code", hGroupCode);
			setValue("from_type", "1");
			setValue("oppo_type", hCurrentCode);
			setValue("oppo_status", hOppostReason);
			if(!hCurrentCode.equals("0")){
				setValue("oppo_user", modUser);
				setValue("oppo_date", hOppostDate);
				setValue("oppo_time", sysTime);
			}
			setValue("neg_del_date", hNegDelDate);
			setValue("renew_flag", "N");
			setValue("cycle_credit", "");
			setValue("opp_remark", "");
			setValue("mail_branch", "");
			setValue("lost_fee_flag", hLostFeeCode);
			setValue("excep_flag", hExcepFlag);
			setValue("except_proc_flag", "0");
			setValue("neg_resp_code", "");
			setValue("visa_resp_code", "");
			setValue("mst_reason_code", hNegReason);
			setValue("vis_reason_code", hVmjReason);
			setValue("fisc_reason_code", hFiscReason);
			setValueDouble("curr_tot_tx_amt", 0);
			setValueDouble("curr_tot_cash_amt", 0);
			setValue("bank_acct_no", "");
			if(hCurrentCode.equals("0")) {
				setValue("logic_del", "Y");
				setValue("logic_del_date", sysDate);
				setValue("logic_del_time", sysTime);
				setValue("logic_del_user", modUser);
			}
			setValue("crt_date", sysDate);
			setValue("crt_time", sysTime);
			setValue("crt_user", modUser);
			setValue("mod_time", sysDate + sysTime);
			setValue("mod_user", modUser);
			setValue("mod_pgm", javaProgram);
			setValueInt("mod_seqno", 1);
			daoTable = "cca_opposition";

			insertTable();

			if (dupRecord.equals("Y")) {
				showLogMessage("I", "", String.format("insert cca_opposition error,card_no = [%s]", hCardNo));
			}
		}
	}

	
	/***********************************************************************/
//	private void insertOnbat2ecs() throws Exception {
//
//		if (hCurrentCode.equals("3")) {
//			setValue("trans_type", "5");
//		} else {
//			setValue("trans_type", "6");
//		}
//		
//		setValue("card_no", hCardNo);
//		setValue("to_which", "1");
//		setValue("dog", sysDate + sysTime);
//		setValue("acct_type", hAcctType);
//		setValue("acno_p_seqno", hAcnoPSeqno);
//		setValue("id_p_seqno", hIdPSeqno);
//		setValue("opp_type", hCurrentCode);
//		if(hCurrentCode.equals("0")) {
//			setValue("proc_mode", "N");
//			setValueInt("proc_status", 0);
//		}else {
//			setValue("proc_mode", "O");
//			setValue("opp_reason", hOppostReason);
//			setValue("opp_date", hOppostDate);
//			setValue("is_renew", "N");
//			setValueInt("curr_tot_lost_amt", 100);
//			setValue("lost_fee_flag", hLostFeeCode);
//			setValue("debit_flag", (ibDebit == true) ? "Y" : "N");
//		}
//		setValue("mail_branch", "");
//		daoTable = "onbat_2ecs";
//
//		insertTable();
//
//		if (dupRecord.equals("Y")) {
//			showLogMessage("I", "", String.format("insert onbat_2ecs error,card_no = [%s]", hCardNo));
//		}
//	}
	
	/***********************************************************************/
	private void oppoNegReq() throws Exception {
		insertCcaOutgoing("NCCC",hNegReason,"");
	}

	/***********************************************************************/
	private void oppoFiscReq() throws Exception {
		insertCcaOutgoing("FISC",hFiscReason,"");
	}
	
	/***********************************************************************/
	private void oppoVisaReq() throws Exception {
		if(!zzStr.empty(hVmjReason))
			insertCcaOutgoing("VISA",hVmjReason,"         ");
	}
	
	/***********************************************************************/
	private void oppoJcbReq() throws Exception {
		if(!zzStr.empty(hVmjReason))
			insertCcaOutgoing("JCB",hVmjReason,"00000");
	}
	
	/***********************************************************************/
	private void oppoMaster2Req() throws Exception {
		if(!zzStr.empty(hVmjReason))
			insertCcaOutgoing("MASTER2",hVmjReason,"");
	}
	
	/***********************************************************************/
	//111-12-14  V1.00.09    Ryan     票證可能有多筆，修改只讀有效起日最新的一筆                       
	private void oppoTscReq() throws Exception {
		sqlCmd = "select tsc_card_no ";
		sqlCmd += " from tsc_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and card_no = ? ";
		if (ibDebit) {
			sqlCmd = "select tsc_card_no ";
			sqlCmd += " from tsc_vd_card where new_end_date > to_char(sysdate,'yyyymm') ";
			sqlCmd += " and vd_card_no = ? ";
		}
		sqlCmd += " and current_code = '2' order by new_beg_date desc fetch first 1 rows only ";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("tsc_card_no");
			insertCcaOutgoing("TSCC","","");
		}
	}
	
	/***********************************************************************/
	//111-12-14  V1.00.09    Ryan     票證可能有多筆，修改只讀有效起日最新的一筆       
	private void oppoIpsReq() throws Exception {
		sqlCmd = "select ips_card_no " + " from ips_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and current_code = '2' ";
		sqlCmd += " and card_no = ? order by new_beg_date desc fetch first 1 rows only ";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("ips_card_no");
			insertCcaOutgoing("IPASS","","");
		}
	}
	
	/***********************************************************************/
	//111-12-14  V1.00.09    Ryan     票證可能有多筆，修改只讀有效起日最新的一筆       
	private void oppoIchReq() throws Exception {
		sqlCmd = "select ich_card_no " + " from ich_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and current_code = '2' ";
		sqlCmd += " and card_no = ? order by new_beg_date desc fetch first 1 rows only ";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("ich_card_no");
			insertCcaOutgoing("ICASH","","");
		}
	}
	
	/***********************************************************************/
	private void insertCcaOutgoing( String keyValue , String reasonCode , String vmjRegnData) throws Exception {

		setValue("card_no", hCardNo);
		setValue("key_value", keyValue);
		setValue("key_table", "OPPOSITION");
		setValue("bitmap", "");
		if(hCurrentCode.equals("0")) {
			if(keyValue.equals("JCB")) {
				setValue("act_code", "0");
			}else {
				setValue("act_code", "3");
			}
		}else {
			if(isUpdate) {
				setValue("act_code", "2");
			}else {
				setValue("act_code", "1");
			}
		}
		setValue("proc_flag", "1");
		setValue("send_times", "1");
		setValue("proc_date", sysDate);
		setValue("proc_time", sysTime);
		setValue("proc_user", modUser);
		setValue("data_from", "2");
		setValue("resp_code", "");
		setValue("data_type", "OPPO");
		setValue("bin_type", hBinType);
		setValue("reason_code", reasonCode);//
		setValue("del_date", hNegDelDate);
		setValue("bank_acct_no", "");
		setValue("vmj_regn_data", vmjRegnData);//
		setValue("vip_amt", "0");
		setValue("electronic_card_no", hElectronicCardNo);
		setValue("current_code", hCurrentCode);
		setValue("new_end_date", hNewEndDate);
		setValue("oppost_date", hOppostDate);
		setValue("oppost_reason", hOppostReason);
		setValue("v_card_no", "");
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("crt_user", modUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		setValueInt("mod_seqno", 1);
		
		daoTable = "cca_outgoing";

		insertTable();
		
		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("insert cca_outgoing error,card_no = [%s]", hCardNo));
		}
	}
	
	//2022-10-21 V1.00.09 Ryan 增加凍結、特指送黑名單功能
	//111-12-07  V1.00.08    Ryan     cca_outgoing data_from 改為2 代表批次執行
	private void insertCcaOutgoingBlock( String keyValue , String reasonCode , String vmjRegnData ) throws Exception {

		setValue("card_no", hCardNo);
		setValue("key_value", keyValue);
		setValue("key_table", "CARD_BASE_SPEC");
		setValue("bitmap", "");
		setValue("act_code", "1");
		setValue("proc_flag", "1");
		setValue("send_times", "1");
		setValue("proc_date", sysDate);
		setValue("proc_time", sysTime);
		setValue("proc_user", modUser);
		setValue("data_from", "2");
		setValue("resp_code", "");
		setValue("data_type", "BLOCK");
		setValue("bin_type", hBinType);
		setValue("reason_code", reasonCode);//
		setValue("del_date", hNegDelDate);
		setValue("bank_acct_no", "");
		setValue("vmj_regn_data", vmjRegnData);//
		setValue("vip_amt", "0");
		setValue("electronic_card_no", hElectronicCardNo);
		setValue("current_code", hCurrentCode);
		setValue("new_end_date", hNewEndDate);
		setValue("oppost_date", hOppostDate);
		setValue("oppost_reason", hOppostReason);
		setValue("block_code", hOppostReason);
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("crt_user", modUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		setValueInt("mod_seqno", 1);
		
		daoTable = "cca_outgoing";

		insertTable();
		
		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("insert cca_outgoing error,card_no = [%s]", hCardNo));
		}
	}
	
	//111-12-14  V1.00.09    Ryan     增加解凍功能
	private void deleteCcaOutgoingBlock( String keyValue , String reasonCode , String vmjRegnData ) throws Exception {

		setValue("card_no", hCardNo);
		setValue("key_value", keyValue);
		setValue("key_table", "CARD_BASE_SPEC");
		setValue("bitmap", "");
		if(keyValue.equals("JCB")) {
			setValue("act_code", "0");
		}else {
			setValue("act_code", "3");
		}	
		setValue("proc_flag", "1");
		setValue("send_times", "1");
		setValue("proc_date", sysDate);
		setValue("proc_time", sysTime);
		setValue("proc_user", modUser);
		setValue("data_from", "2");
		setValue("resp_code", "");
		setValue("data_type", "BLOCK");
		setValue("bin_type", hBinType);
		setValue("reason_code", reasonCode);//
		setValue("del_date", hNegDelDate);
		setValue("bank_acct_no", "");
		setValue("vmj_regn_data", vmjRegnData);//
		setValue("vip_amt", "0");
		setValue("electronic_card_no", hElectronicCardNo);
		setValue("current_code", hCurrentCode);
		setValue("new_end_date", hNewEndDate);
		setValue("oppost_date", hOppostDate);
		setValue("oppost_reason", hOppostReason);
		setValue("block_code", hOppostReason);
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("crt_user", modUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		setValueInt("mod_seqno", 1);
		
		daoTable = "cca_outgoing";

		insertTable();
		
		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("insert cca_outgoing error,card_no = [%s]", hCardNo));
		}
	}
	
	public void finalCnt() {
		showLogMessage("I", "", "");
		showLogMessage("I", "", String.format("CAR_CARD NOT FOUND CNT = [%d]", cardNotFoundCnt));
		finalProcess();
	}
	
	public void finalCnt2() {
		showLogMessage("I", "", "");
		finalProcess();
	}
	
	
	boolean selectCmsChgcolumnLog() throws Exception {
		String chgDataOld = "";
		String chgData = "";
		sqlCmd = "select chg_data_old,chg_data " + " from CMS_CHGCOLUMN_LOG where CHG_COLUMN = 'current_code' ";
		sqlCmd += " and card_no = ? order by chg_date desc,chg_time desc fetch first 1 rows only";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			chgDataOld = getValue("chg_data_old");
			chgData = getValue("chg_data");
			if(!chgDataOld.equals("0")&&!chgData.equals("0")) {
				return true;
			}
		}
		return false;
	}
	
	/***********************************************************************/
	private void initData() {
		ibDebit = false;
		hCurrentCode = "";
		hOppostReason = "";
		hOppostDate = "";
		hBinType = "";
		hNegDelDate = "";
		hLostFeeCode = "";
		hExcepFlag = "";
		hNegReason = "";
		hFiscReason = "";
		hVmjReason = "";
		hCardAcctIdx = 0;
		hCardType = "";
		hGroupCode = "";
//		hAcctType = "";
//		hAcnoPSeqno = "";
//		hIdPSeqno = "";
		hElectronicCode = "";
		hElectronicCardNo = "";
		hNewEndDate = "";
	}
}
