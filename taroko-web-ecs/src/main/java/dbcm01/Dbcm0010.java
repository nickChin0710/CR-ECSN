/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-15  V1.00.00  yash       program initial                            *
* 107-09-20  V1.00.01  Andy       pgm logic                                  *
* 108-12-17  V1.00.02  ryan		  update : ptr_group_card==>crd_item_unit    *
* 108-12-19  V1.00.03  Amber	  update : ptr_branch==>gen_brn 		     *
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 110-10-19  V1.00.05  YangBo	  joint sql replace to parameters way        *
* 112-04-22  V1.00.06  Wilson	  mark檢核晶片效期                                                                                     *
* 112-07-11  V1.00.07  Wilson	     是否收重製費一律預設N                             *
******************************************************************************/

package dbcm01;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Dbcm0010 extends BaseEdit {
	Dbcm0010Func func;
	String mExBatchno = "";
	String mExRecno = "";
	String mrowid = "";
	String mIdNo = "";
	String mCardNo = "";

	String lsCardType = "", lsIdNo = "", lsIdNoCode = "", lsNewBegDate = "", lsNewEndDate = "", lsIdPSeqno = "",
			lsCorpNo = "", lsEngName = "", lsGroupCode = "", lsUnitCode = "", lsReissueDate = "", lsReissueReason = "",
			lsReissueStatus = "", lsCurrentCode = "", lsOppostDate = "", lsRegBankNo = "", regBankNo = "",
			lsEmbossData = "", lsChangeStatus = "", lsExpireChgFlag = "", lsSupFlag = "", lsMajorCardNo = "",
			lsMajorId = "", lsMajorIdCode = "", lsNewCardNo = "", lsIcFlag = "", lsAcctNo = "", lsIbmIdCode = "",
			lsBinType = "", lsFlagAct = "", lsDigitalFlag = "", lsForceFlag = "", lsRedoFlag = "", lsEmbossReason = "",
			lsChiName = "", lsSpecFlag = "", lsMajorValidFm = "", lsMajorValidTo = "", lsValidFm = "", lsValidTo = "",
			lsMailType = "", lsCardNo = "", lsMajorIdPSeqno = "", lsSourceCode = "", lsAcctType = "", lsPSeqno = "",
			lsCvv = "", lsCvv2 = "", lsPvki = "", lsPvv = "", lsBatchno = "", lsRecno = "", lsMemberId = "",
			lsBirthday = "", lsVoicePawd = "", lsSex = "", lsAgeIndicator = "", lsServiceCode = "", lsNation = "",
			lsBinNo = "";
	String lsMsg = "", lsSubmsg = "";

	int liExtnYear = 0;
	int liPrc = 0, liCount = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			// dataRead1();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			strAction = "D";
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			strAction = "S";
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			/* 清畫面 */
			strAction = "card_no";
			itemchanged();
		} else if (eqIgno(wp.buttonCode, "C1")) {
			/* 清畫面 */
			strAction = "emboss_reason";
			itemchanged();
		} else if (eqIgno(wp.buttonCode, "C2")) {
			/* 清畫面 */
			strAction = "unit_code";
			itemchanged();
		}

		dddwSelect();
		initButton();
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
		wp.whereStr = " where 1=1  and emboss_source ='5' ";

		if (empty(wp.itemStr2("ex_batchno")) == false) {
			wp.whereStr += " and  batchno = :batchno ";
			setString("batchno", wp.itemStr2("ex_batchno"));
		}

		if (empty(wp.itemStr2("ex_recno")) == false) {
			wp.whereStr += " and  recno = :recno ";
			setString("recno", wp.itemStr2("ex_recno"));
		}

		if (empty(wp.itemStr2("ex_reason")) == false) {
			wp.whereStr += " and  emboss_reason = :reason ";
			setString("reason", wp.itemStr2("ex_reason"));
		}

		if (empty(wp.itemStr2("ex_old_card_no")) == false) {
			wp.whereStr += " and  old_card_no = :old_card_no ";
			setString("old_card_no", wp.itemStr2("ex_old_card_no"));
		}

		if (empty(wp.itemStr2("ex_id")) == false) {
			wp.whereStr += " and  apply_id = :ex_id ";
			setString("ex_id", wp.itemStr2("ex_id"));
		}

		if (empty(wp.itemStr2("ex_corp")) == false) {
			wp.whereStr += " and  corp_no = :corp ";
			setString("corp", wp.itemStr2("ex_corp"));
		}

		if (empty(wp.itemStr2("ex_chi_name")) == false) {
			wp.whereStr += " and chi_name = :chi_name ";
			setString("chi_name", wp.itemStr2("ex_chi_name"));
		}

		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		getWhereStr();
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " batchno" + ", recno" + ", emboss_reason"
				+ ",(case when emboss_reason = '1' then '掛失' else decode(emboss_reason,'2','毀損',decode(emboss_reason,'3','偽卡',emboss_reason)) end) as db_emboss_reason"
				+ ", old_card_no" + ", apply_id" + ", corp_no" + ", chi_name" + ", card_type" + ", group_code"
				+ ", unit_code" + ", digital_flag" + ", mod_user";

		wp.daoTable = "dbc_emboss_tmp";
		wp.whereOrder = " order by batchno,recno";
		getWhereStr();
		// System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
		// + wp.whereStr + wp.whereOrder);
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();

	}

	@Override
	public void querySelect() throws Exception {
		dataRead();

	}

	@Override
	public void dataRead() throws Exception {
		wp.colSet("set", "disabled=\"disabled\"");
		if (empty(mExBatchno)) {
			mExBatchno = itemKk("data_k1");
		}
		if (empty(mExRecno)) {
			mExRecno = itemKk("data_k2");
		}
		if (empty(mCardNo)) {
			mCardNo = itemKk("data_k3");
		}

		wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ",batchno" + ",recno" + ",emboss_source" + ",emboss_reason"
				+ ",resend_note" + ",source_batchno" + ",source_recno" + ",aps_batchno" + ",aps_recno" + ",seqno"
				+ ",to_nccc_code" + ",card_type" + ",bin_no" + ",acct_type" + ",acct_key" + ",class_code" + ",sup_flag"
				+ ",unit_code" + ",card_no" + ",major_card_no" + ",major_valid_fm" + ",major_valid_to"
				+ ",major_chg_flag" + ",old_card_no" + ",change_reason" + ",status_code" + ",reason_code"
				+ ",member_note" + ",apply_id" + ",apply_id_code" + ",apply_ibm_id_code" + ",pm_id" + ",pm_id_code"
				+ ",pm_ibm_id_code" + ",group_code" + ",source_code" + ",corp_no" + ",corp_no_code" + ",corp_act_flag"
				+ ",corp_assure_flag" + ",reg_bank_no" + ",risk_bank_no" + ",org_risk_bank_no" + ",chi_name"
				+ ",eng_name" + ",birthday" + ",marriage" + ",rel_with_pm" + ",service_year" + ",education" + ",nation"
				+ ",salary" + ",mail_zip" + ",mail_addr1" + ",mail_addr2" + ",mail_addr3" + ",mail_addr4"
				+ ",mail_addr5" + ",resident_zip" + ",resident_addr1" + ",resident_addr2" + ",resident_addr3"
				+ ",resident_addr4" + ",resident_addr5" + ",company_name" + ",job_position" + ",home_area_code1"
				+ ",home_tel_no1" + ",home_tel_ext1" + ",home_area_code2" + ",home_tel_no2" + ",home_tel_ext2"
				+ ",office_area_code1" + ",office_tel_no1" + ",office_tel_ext1" + ",office_area_code2"
				+ ",office_tel_no2" + ",office_tel_ext2" + ",e_mail_addr" + ",cellar_phone" + ",act_no" + ",bank_actno"
				+ ",vip" + ",fee_code" + ",force_flag" + ",business_code" + ",introduce_no" + ",valid_fm" + ",valid_to"
				+ ",sex" + ",value" + ",accept_dm" + ",apply_no" + ",cardcat" + ",mail_type" + ",mail_no"
				+ ",mail_branch" + ",mail_proc_date" + ",introduce_id" + ",introduce_name" + ",salary_code" + ",student"
				+ ",credit_lmt" + ",apply_id_ecode" + ",corp_no_ecode" + ",pm_id_ecode" + ",police_no1" + ",police_no2"
				+ ",police_no3" + ",pm_cash" + ",sup_cash" + ",online_mark" + ",error_code" + ",reject_code"
				+ ",emboss_4th_data" + ",member_id" + ",stmt_cycle" + ",credit_flag" + ",comm_flag" + ",resident_no"
				+ ",other_cntry_code" + ",passport_no" + ",staff_flag" + ",pm_birthday" + ",sup_birthday"
				+ ",standard_fee" + ",final_fee_code" + ",fee_reason_code" + ",annual_fee" + ",chg_addr_flag"
				+ ",pin_block" + ",pvv" + ",cvv" + ",cvv2" + ",pvki" + ",open_passwd" + ",voice_passwd" + ",cht_passwd"
				+ ",cht_date" + ",service_code" + ",cntl_area_code" + ",stock_no" + ",old_beg_date" + ",old_end_date"
				+ ",emboss_date" + ",nccc_batchno" + ",nccc_recno" + ",nccc_type" + ",to_nccc_date" + ",nccc_filename"
				+ ",rtn_nccc_date" + ",emboss_result" + ",diff_code" + ",credit_error" + ",auth_credit_lmt"
				+ ",main_credit_lmt" + ",fail_proc_code" + ",complete_code" + ",mail_code" + ",fee_error_code"
				+ ",fee_date" + ",in_main_date" + ",in_main_error" + ",in_main_msg" + ",in_other_date"
				+ ",in_other_error" + ",in_other_msg" + ",in_auth_date" + ",in_auth_error" + ",in_auth_msg"
				+ ",star_date" + ",prn_pin_date" + ",prn_post_date" + ",prn_mailer_date" + ",prn_cardno_date"
				+ ",son_card_flag" + ",org_indiv_crd_lmt" + ",indiv_crd_lmt" + ",nccc_oth_date" + ",to_ibm_date"
				+ ",warehouse_date" + ",org_emboss_data" + ",org_cash_lmt" + ",cash_lmt" + ",branch" + ",mail_attach1"
				+ ",mail_attach2" + ",ic_indicator" + ",ic_cvv" + ",ic_pin" + ",deriv_key" + ",l_offln_lmt"
				+ ",u_offln_lmt" + ",ic_flag" + ",key_type" + ",mail_seqno" + ",vendor" + ",filename" + ",csc"
				+ ",chk_nccc_flag" + ",barcode_num" + ",fh_over_flag" + ",fh_flag" + ",barcode_numps" + ",batchno_seqno"
				+ ",to_mondex_date" + ",age_indicator" + ",apply_source" + ",cardno_code"
				+ ",decode(redo_flag,'','N',redo_flag) as redo_flag" + ",reissue_code" + ",third_rsn" + ",digital_flag"
				+ ",receipt_branch" + ",receipt_remark" + ",crt_user" + ",crt_date" + ",apr_note" + ",apr_date"
				+ ",apr_user" + ",confirm_user" + ",confirm_date" + ",mod_user" + ",uf_2ymd(mod_time) as mod_date"
				+ ",mod_pgm";
		wp.daoTable = "dbc_emboss_tmp";
		wp.whereStr = "where 1=1";
		wp.whereStr += " and  batchno = :batchno  and  recno = :recno";
		setString("batchno", mExBatchno);
		setString("recno", mExRecno);

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, batchno=" + mExBatchno);
		}
		wp.colSet("type_flag", "Y");
		if (wfChkCardStatus() != 1) {
			return;
		}
	}

	int wfChkCardStatus() throws Exception {
		String lsSql = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			lsSql = "select current_code, " + "change_status, " + "expire_chg_flag, " + "oppost_date, "
					+ "reissue_status, " + "acct_no, " + "digital_flag," + "bin_type " + "from dbc_card "
					+ "where card_no = :ls_old_card_no";
			setString("ls_old_card_no", mCardNo);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				alertErr("抓取不到卡片檔資料");
				return -1;
			}
			wp.colSet("current_code", sqlStr("current_code"));
			wp.colSet("change_status", sqlStr("change_status"));
			wp.colSet("expire_chg_flag", sqlStr("expire_chg_flag"));
			wp.colSet("oppost_date", sqlStr("oppost_date"));
			wp.colSet("reissue_status", sqlStr("reissue_status"));
			wp.colSet("acct_no", sqlStr("acct_no"));
			wp.colSet("digital_flag", sqlStr("digital_flag"));
			wp.colSet("bin_type", sqlStr("bin_type"));

			if (sqlStr("digital_flag").equals("Y")) {
				wp.colSet("mail_type", "2");
			}
			lsGroupCode = wp.colStr("group_code");
			lsCardType = wp.colStr("card_type");
//			if(wf_get_spec_flag(ls_group_code, ls_card_type) !=1){
//				return -1;
//			}

		}
		return 1;
	}

	@Override
	public void saveFunc() throws Exception {
		Dbcm0010Func func = new Dbcm0010Func(wp);
		if (ofValidation() != 1) {
			if (strAction.equals("A")) {
				// clearData();
			}
			if (strAction.equals("U")) {
				mExBatchno = wp.itemStr2("batchno");
				mExRecno = wp.itemStr2("recno");
				mCardNo = wp.itemStr2("old_card_no");
				dataRead();
			}
			return;
		}
		rc = func.dbSave(strAction);
		// ddd(func.getMsg());
		if (rc != 1) {
			errmsg(func.getMsg());
		}
		this.sqlCommit(rc);
	}

	@Override
	public void initButton() {

		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	@Override
	public void dddwSelect() {
		String wkEmbossReason = "";
		try {
			wp.initOption = "--";
			wp.optionKey = wp.colStr("emboss_reason");
			this.dddwList("dddw_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
					"where 1=1 and wf_type='REISSUE_REASON' order by wf_id");

			wkEmbossReason = wp.colStr("emboss_reason");
			wp.initOption = "--";
			wp.optionKey = wp.colStr("reissue_code");
			// 為下面dddwList方法傳參數
			setString("reissue_reason", wkEmbossReason);
			this.dddwList("dddw_reissue_code", "ptr_reissue_code", "reissue_code", "content",
					"where 1=1 and reissue_reason = :reissue_reason order by to_number(reissue_code)");

			wp.initOption = "--";
			wp.optionKey = wp.colStr("receipt_branch");
			this.dddwList("dddw_receipt", "gen_brn", "branch", "full_chi_name", "where 1=1  order by branch");

		} catch (Exception ex) {
		}
	}

	void itemchanged() throws Exception {
		switch (strAction) {
		case "card_no":
			mCardNo = wp.itemStr2("kk_old_card_no");
			if (wfCardnoData(mCardNo) != 1) {
				wp.colSet("card_desp", lsMsg);
				break;
			}
		case "unit_code":
			lsUnitCode = wp.itemStr2("unit_code");
			if (wfChkUnitCode() != 1) {
				wp.colSet("unit_desp", lsMsg);
				break;
			}
		case "emboss_reason":
			lsEmbossReason = wp.itemStr2("emboss_reason");
			wp.initOption = "--";
			wp.optionKey = wp.colStr("reissue_code");
			// 為下面dddwList方法傳參數
			setString("reissue_reason", lsEmbossReason);
			this.dddwList("dddw_reissue_code", "ptr_reissue_code", "reissue_code", "content",
					"where 1=1 and reissue_reason = :reissue_reason order by to_number(reissue_code)");

			if (lsEmbossReason.equals("2")) {
//				wp.colSet("redo_flag", "Y");
				wp.colSet("redo_flag", "N");
				break;
			}
			if (lsEmbossReason.equals("1") || lsEmbossReason.equals("3")) {
				wp.colSet("redo_flag", "N");
				break;
			}
		}
	}

	int wfCardnoData(String asCardno) throws Exception {
		String lsSql = "";
		// select dbc_card
		lsSql = "select card_type, "
				+ "(select nvl(B.id_no,'') from dbc_idno B where B.id_p_seqno =major_id_p_seqno) id_no, "
				+ "(select nvl(B.id_no_code,'') from dbc_idno B where B.id_p_seqno=major_id_p_seqno) id_no_code, "
				+ "new_beg_date, " + "new_end_date, " + "id_p_seqno, " + "corp_no, " + "eng_name, " + "group_code, "
				+ "unit_code, " + "reissue_date, " + "reissue_reason, " + "reissue_status, " + "current_code, "
				+ "oppost_date, " + "reg_bank_no, " + "force_flag, " + "emboss_data, " + "change_status, "
				+ "expire_chg_flag, " + "sup_flag, " + "major_card_no, "
				+ "(select nvl(B.id_no,'') from dbc_idno B where B.id_p_seqno =major_id_p_seqno) major_id, "
				+ "(select nvl(B.id_no_code,'') from dbc_idno B where B.id_p_seqno=major_id_p_seqno) major_id_code, "
				+ "new_card_no, " + "ic_flag, " + "acct_no, " + "ibm_id_code, " + "bin_type, " + "digital_flag, "
				+ "p_seqno, " + "acct_type, " + "source_code, " + "member_id, "
				// + "member_note, " //這欄位已經没了
				+ "pvv, " + "cvv, " + "cvv2, " + "pvki, " + "bin_no," + "'INS' flag_act " + "from dbc_card "
				+ "where card_no =:as_cardno ";
		setString("as_cardno", asCardno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsCardType = sqlStr("card_type");
			lsIdNo = sqlStr("id_no");
			lsIdNoCode = sqlStr("id_no_code");
			lsNewBegDate = sqlStr("new_beg_date");
			lsNewEndDate = sqlStr("new_end_date");
			lsIdPSeqno = sqlStr("id_p_seqno");
			lsCorpNo = sqlStr("corp_no");
			lsEngName = sqlStr("eng_name");
			lsGroupCode = sqlStr("group_code");
			lsUnitCode = sqlStr("unit_code");
			lsReissueDate = sqlStr("reissue_date");
			lsReissueReason = sqlStr("reissue_reason");
			lsReissueStatus = sqlStr("reissue_status");
			lsCurrentCode = sqlStr("current_code");
			lsOppostDate = sqlStr("oppost_date");
			lsRegBankNo = sqlStr("reg_bank_no");
			lsForceFlag = sqlStr("force_flag");
			lsEmbossData = sqlStr("emboss_data");
			lsChangeStatus = sqlStr("change_status");
			lsExpireChgFlag = sqlStr("expire_chg_flag");
			lsSupFlag = sqlStr("sup_flag");
			lsMajorCardNo = sqlStr("major_card_no");
			lsMajorId = sqlStr("major_id");
			lsMajorIdCode = sqlStr("major_id_code");
			lsNewCardNo = sqlStr("new_card_no");
			lsIcFlag = sqlStr("ic_flag");
			lsAcctNo = sqlStr("acct_no");
			lsIbmIdCode = sqlStr("ibm_id_code");
			lsBinType = sqlStr("bin_type");
			lsDigitalFlag = sqlStr("digital_flag");
			lsPSeqno = sqlStr("p_seqno");
			lsAcctType = sqlStr("acct_type");
			lsSourceCode = sqlStr("source_code");
			lsMemberId = sqlStr("member_id");
			lsPvv = sqlStr("pvv");
			lsCvv = sqlStr("cvv");
			lsCvv2 = sqlStr("cvv2");
			lsPvki = sqlStr("pvki");
			lsBinNo = sqlStr("bin_no");
			lsFlagAct = sqlStr("flag_act");
		} else {
			lsMsg = "此卡號不存在於卡檔中";
			return -1;
		}

		// -- redo_flag, emboss_reason set
		String lsSubmsg = "", lsEmbossReason = "";
		if (lsCurrentCode.equals("0")) {
			lsEmbossReason = "2";
			wp.colSet("emboss_reason", "2");
		} else if (lsCurrentCode.equals("1")) {
			lsEmbossReason = "1";
			wp.colSet("emboss_reason", "1");
		} else if (lsCurrentCode.equals("2")) {
			lsEmbossReason = "1";
			wp.colSet("emboss_reason", "1");
		} else if (lsCurrentCode.equals("5")) {
			lsEmbossReason = "3";
			wp.colSet("emboss_reason", "3");
		} else {
			if (lsCurrentCode.equals("1")) {
				lsSubmsg = "申停";
			}
			if (lsCurrentCode.equals("3")) {
				lsSubmsg = "強停";
			}
			wp.alertMesg += "<script language='javascript'> alert('此卡片之狀態碼為" + lsSubmsg + "不可做重製卡作業~')</script>";
			return -1;
		}
//		switch (ls_current_code) {
//		case "0":
//			ls_redo_flag = "N";
//			wp.col_set("redo_flag", "N");
//			ls_msg = "此卡片之狀態碼為有效卡不可做重製卡作業~";
//			return -1;
//		case "1":
//			ls_emboss_reason = "2";
//			wp.col_set("emboss_reason", "2");
//			break;
//		case "2":
//			ls_emboss_reason = "1";
//			wp.col_set("emboss_reason", "1");
//			break;
//		case "5":
//			ls_emboss_reason = "3";
//			wp.col_set("emboss_reason", "3");
//			break;
//		default:
//			if (ls_current_code.equals("1")) {
//				ls_submsg = "申停";
//			}
//			if (ls_current_code.equals("3")) {
//				ls_submsg = "強停";
//			}
//			ls_msg = "此卡片之狀態碼為" + ls_submsg + "不可做重製卡作業";
//			return -1;
//		}
		if (lsEmbossReason.equals("2")) {
			lsRedoFlag = "Y";
//			wp.colSet("redo_flag", "Y");
			wp.colSet("redo_flag", "N");
		}

		wp.initOption = "--";
		wp.optionKey = wp.colStr("reissue_code");
		// 為下面dddwList方法傳參數
		setString("reissue_reason", lsEmbossReason);
		this.dddwList("dddw_reissue_code", "ptr_reissue_code", "reissue_code", "content",
				"where 1=1 and reissue_reason = :reissue_reason order by to_number(reissue_code)");

		// select dbc_idno
		lsSql = "select chi_name,birthday,voice_passwd,sex " + "from dbc_idno " + "where id_p_seqno = :ls_id_p_seqno";
		setString("ls_id_p_seqno", lsIdPSeqno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			lsMsg = "卡人姓名抓取不到";
			return -1;
		} else {
			lsChiName = sqlStr("chi_name");
			lsBirthday = sqlStr("birthday");
			lsVoicePawd = sqlStr("voice_passwd");
			lsSex = sqlStr("sex");
		}

		// select dbc_group_code
		lsSql = "select count(*) as ct " + "from dbc_group_code " + "where group_code = :ls_group_code ";
		setString("ls_group_code", lsGroupCode);
		sqlSelect(lsSql);
		if (sqlNum("ct") > 0) {
			lsMsg = "此團代" + lsGroupCode + "不可重製";
			return -1;
		}

		// select ptr_group_card
		lsSql = "select spec_flag " + "from ptr_group_card " + "where group_code = :ls_group_code "
				+ "and card_type = :ls_card_type ";
		setString("ls_group_code", lsGroupCode);
		setString("ls_card_type", lsCardType);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			lsMsg = "卡樣卡種抓取不到";
			return -1;
		} else {
			lsSpecFlag = sqlStr("spec_flag");
		}
		// Set value
		wp.colSet("old_card_no", mCardNo);
		wp.colSet("card_type", lsCardType);
		wp.colSet("apply_id", lsIdNo);
		wp.colSet("apply_id_code", lsIdNoCode);
		wp.colSet("force_flag", lsForceFlag);
		wp.colSet("group_code", lsGroupCode);
		wp.colSet("unit_code", lsUnitCode);
		wp.colSet("spec_flag", lsSpecFlag);
		wp.colSet("chi_name", lsChiName);
		wp.colSet("corp_no", lsCorpNo);
		wp.colSet("eng_name", lsEngName);
		wp.colSet("emboss_4th_data", lsEmbossData);
		wp.colSet("current_code", lsCurrentCode);
		wp.colSet("oppost_date", lsOppostDate);
		wp.colSet("reissue_status", lsReissueStatus);
		wp.colSet("change_status", lsChangeStatus);
		wp.colSet("expire_chg_flag", lsExpireChgFlag);
		wp.colSet("reissue_date", lsReissueDate);
		wp.colSet("reissue_reason", lsReissueReason);
		wp.colSet("sup_flag", lsSupFlag);
		wp.colSet("old_beg_date", lsNewBegDate);
		wp.colSet("old_end_date", lsNewEndDate);
		wp.colSet("db_new_card_no", lsNewCardNo);
		wp.colSet("ic_flag", lsIcFlag);
		wp.colSet("act_no", lsAcctNo);
		wp.colSet("mail_type", "4");
		wp.colSet("branch", strMid(lsAcctNo, 0, 3));
		wp.colSet("reg_bank_no", strMid(lsAcctNo, 0, 3));
		wp.colSet("bin_type", lsBinType);
		wp.colSet("digital_flag", lsDigitalFlag);
		wp.colSet("p_seqno", lsPSeqno);
		wp.colSet("acct_type", lsAcctType);
		wp.colSet("source_code", lsSourceCode);
		wp.colSet("member_id", lsMemberId);
		wp.colSet("pvv", lsPvv);
		wp.colSet("cvv", lsCvv);
		wp.colSet("cvv2", lsCvv2);
		wp.colSet("pvki", lsPvki);
		wp.colSet("birthday", lsBirthday);
		wp.colSet("voice_passwd", lsVoicePawd);
		wp.colSet("bin_no", lsBinNo);

		if (lsDigitalFlag.equals("Y")) {
			wp.colSet("mail_type", "2");
		}
		//
		if (lsNewEndDate.compareTo(getSysDate()) < 0) {
			lsMsg = "本卡片原效期不可小於系統日";
			return -1;
		}
		//
//		if (lsEmbossReason.equals("1") || lsEmbossReason.equals("3")) {
//			lsRedoFlag = "N";
//			lsMsg = "此卡片已重製成功過,不可重複重製";
//			return -1;
//		}
		// 正副卡
		if (lsSupFlag.equals("1")) {
			lsSql = "select new_beg_date, " + "new_end_date from dbc_card " + "where card_no = :ls_major_card_no ";
			setString("ls_major_card_no", lsMajorCardNo);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				lsMsg = "抓取不到正卡資料";
				return -1;
			} else {
				lsMajorValidFm = sqlStr("new_beg_date");
				lsMajorValidTo = sqlStr("new_end_date");
				wp.colSet("major_card_no", lsMajorCardNo);
				wp.colSet("major_valid_fm", lsMajorValidFm);
				wp.colSet("major_valid_to", lsMajorValidTo);
				wp.colSet("pm_id", lsMajorId);
				wp.colSet("pm_id_code", lsMajorIdCode);
				wp.colSet("apply_ibm_id_code", lsIbmIdCode);
			}
		} else {
			wp.colSet("pm_id", lsMajorId);
			wp.colSet("apply_ibm_id_code", lsIbmIdCode);
			wp.colSet("pm_ibm_id_code", lsIbmIdCode);
		}

		// expire_chg_flag check --- 預約不續卡註記,效期不變
		if (!empty(lsExpireChgFlag)) {
			lsValidFm = lsNewBegDate;
			lsValidTo = lsNewEndDate;
			wp.colSet("valid_fm", lsNewBegDate);
			wp.colSet("valid_to", lsNewEndDate);
		} else {
			// -- 附卡需抓取正卡效期
			if (lsSupFlag.equals("1")) {
				lsNewEndDate = lsMajorValidTo;
			}
			if (wfChkExpireDate(lsNewBegDate, lsNewEndDate, lsCardType, lsUnitCode) != 1) {
				return -1;
			} else {
				wp.colSet("valid_fm", lsValidFm);
				wp.colSet("valid_to", lsValidTo);
			}

		}
		// 效期check
		if (wfChkOppostDate(lsCurrentCode, lsOppostDate) != 1) {
			return -1;
		}
		// validation set value move to here //20180918 Andy
		// set batchno
		String lsCreateDate = "";
		lsCreateDate = strMid(getSysDate(), 2, 6);
		lsSql = " select max(batchno) as batchno " + "from dbc_emboss_tmp " + "where substr(batchno,1,6) =:wk_batchno ";
		setString("wk_batchno", lsCreateDate);
		sqlSelect(lsSql);
		if (!empty(sqlStr("batchno"))) {
			lsBatchno = sqlStr("batchno");
		} else {
			lsBatchno = lsCreateDate + "01";
		}
		lsSql = " select nvl(max(recno),0)+1 as recno " + "from dbc_emboss_tmp "
				+ "where substr(batchno,1,6) =:wk_batchno ";
		setString("wk_batchno", lsCreateDate);
		sqlSelect(lsSql);
		if (!empty(sqlStr("recno"))) {
			lsRecno = sqlStr("recno");
		} else {
			lsRecno = "1";
		}
		// comput age set age_indicator
		lsSql = "select timestampdiff(256, char(to_date(to_char(sysdate,'yyyymmdd') ,'yyyymmdd') - to_date(:ls_birthday, 'yyyymmdd'))) as ls_age  " + " from dual ";
		setString("ls_birthday", lsBirthday);
		sqlSelect(lsSql);
		int lnAge = 0;
		lnAge = sqlInt("ls_age");
		if (lnAge >= 20) {
			lsAgeIndicator = "N";
		} else {
			lsAgeIndicator = "Y";
		}
		// select ptr_card_type
		String lsId2 = "";
		lsId2 = strMid(lsIdNo, 1, 1);
		if (isNumber(lsId2)) {
			lsNation = "1";
			lsSex = lsId2;
			lsSql = "select service_code " + "from crd_item_unit " + "where card_type =:ls_card_type "
					+ "and unit_code =:ls_unit_code";
			setString("ls_card_type", lsCardType);
			setString("ls_unit_code", lsUnitCode);
			sqlSelect(lsSql);
			lsServiceCode = sqlStr("service_code");
		} else {
			lsNation = "2";
			if (lsId2.equals("C") || lsId2.equals("A")) {
				lsSex = "1";
			}
			if (lsId2.equals("B") || lsId2.equals("D")) {
				lsSex = "2";
			}
			lsServiceCode = "621";
		}
		// select risk_bank_no
		String lsChgAddrDate = "", lsRiskBankNo = "";
		double liActCreditAmt = 0;
		lsSql = "select line_of_credit_amt, " + "chg_addr_date, " + "risk_bank_no " + "from dba_acno "
				+ "where p_seqno =:ls_p_seqno ";
		setString("ls_p_seqno", lsPSeqno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			return -1;
		} else {
			liActCreditAmt = sqlNum("line_of_credit_amt");
			lsChgAddrDate = sqlStr("chg_addr_date");
			lsRiskBankNo = sqlStr("risk_bank_no");
		}
		// set value
		wp.colSet("reason_code", "");
		wp.colSet("age_indicator", lsAgeIndicator);
		wp.colSet("batchno", lsBatchno);
		wp.colSet("recno", lsRecno);
		wp.colSet("emboss_source", "5");
		wp.colSet("to_nccc_code", "Y");
		wp.colSet("nccc_type", "3");
		wp.colSet("status_code", "1");
		wp.colSet("sex", lsSex);
		wp.colSet("service_code", lsServiceCode);
		wp.colSet("nation", lsNation);
		wp.colSet("credit_amt", numToStr(liActCreditAmt, "###"));
		wp.colSet("risk_bank_no", lsRiskBankNo);
		return 1;
	}

	// check 展期年
	int wfChkExpireDate(String asBegDate, String asEndDate, String cardType, String unitCode) throws ParseException {
		String lsSql = "", lsSysYyyymm = "", lsDate2 = "";
		// 展期年
		lsSql = "select extn_year " + "from crd_item_unit " + "where card_type =:card_type "
				+ "and unit_code =:unit_code ";
		setString("card_type", lsCardType);
		setString("unit_code", lsUnitCode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			liExtnYear = Integer.parseInt(sqlStr("extn_year"));
		}
		// 系統日+6個月
		try {
			lsSysYyyymm = strMid(addMonth(getSysDate()), 0, 6);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (strMid(asEndDate, 0, 6).compareTo(lsSysYyyymm) <= 0) {
			try {
				lsDate2 = addYear(asEndDate, liExtnYear);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			lsDate2 = asEndDate;
		}
		if (wp.colStr("emboss_reason").equals("2")) {
			lsSql = "select REISSUE_EXTN_MM from CRD_ITEM_UNIT where CARD_ITEM = :cardItem";
			setString("cardItem", unitCode + cardType);
			sqlSelect(lsSql);
			lsDate2 = ofRelativeMm(asEndDate, 0, sqlInt("reissue_extn_mm"));
		}
		String validFm = strMid(getSysDate(), 0, 6) + "01";
		int liSystemDd = toInt(strMid(getSysDate(), 6, 2));
		if (liSystemDd >= 25) {
			String sqlSelect = "select to_char(add_months(to_date( :ls_date1 ,'yyyymmdd'),1),'yyyymm')||'01' as ls_date1 from dual ";
			setString("ls_date1", validFm);
			sqlSelect(sqlSelect);
			validFm = sqlStr("ls_date1");
			if (sqlRowNum < 0) {
				alertErr("日期資料轉換錯誤 !");
				return -1;
			}
		 }
		lsValidFm = validFm;
		lsValidTo = lsDate2;
		return 1;
	}

	// 日期+6個月
	public String addMonth(String date) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date startdate = (Date) sdf.parse(date);
		Calendar start = Calendar.getInstance();
		start.setTime(startdate);
		start.add(Calendar.MONTH, 6);
		String dbAddMonthDate = sdf.format(start.getTime());
		return dbAddMonthDate;
	}

	// 展期年並求該月最後1天
	public String addYear(String date, int extnYear) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date startdate = (Date) sdf.parse(date);
		Calendar start = Calendar.getInstance();
		start.setTime(startdate);
		start.add(Calendar.YEAR, extnYear); // 展期年
		start.getActualMaximum(Calendar.DATE); // 該月最後1天
		String dbAddMonthDate = sdf.format(start.getTime());
		return dbAddMonthDate;
	}

	// 檢核:掛失和偽卡重製日期需在6個月內
	int wfChkOppostDate(String asCurrentCode, String asOppostDate) {
		String lsDate = "";
		if (asCurrentCode.equals("1") || asCurrentCode.equals("2") || asCurrentCode.equals("5")) {
			try {
				lsDate = addMonth(asOppostDate);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (lsDate.compareTo(getSysDate()) < 0) {
				lsMsg = "掛失和偽卡重製日期需在6個月內";
				return -1;
			}
		}
		return 1;
	}

	int ofValidation() {
		String lsSql = "", lsExpireDate = "", lsCheckKeyExpire = "";
		mCardNo = wp.itemStr2("old_card_no");
		// emboss_source
		if (!wp.itemStr2("emboss_source").equals("5") && empty(wp.itemStr2("emboss_source"))) {
			alertErr("不可處理不是重製卡資料 !!");
			return -1;
		}
		//
		if (strAction.equals("D")) {
			if (!wp.itemStr2("emboss_source").equals("5")) {
				alertErr("不可刪除不是重製卡資料 !!");
				return -1;
			}
		}
		// ic_flag
		lsValidFm = wp.itemStr2("valid_fm");
		lsValidTo = wp.itemStr2("valid_to");
		lsCardType = wp.itemStr2("card_type");
		lsUnitCode = wp.itemStr2("unit_code");
		lsBinType = wp.itemStr2("bin_type");

//		if (!wp.itemStr2("ic_flag").equals("Y")) {
//			lsSql = "select b.expire_date, " + "a.check_key_expire " + "from crd_item_unit a , ptr_ickey b "
//					+ "where a.card_type  =:ls_card_type " + "and a.unit_code =:ls_unit_code "
//					+ "and b.key_type =:ls_bin_type " + "and b.key_id = a.key_id ";
//			setString("ls_card_type", lsCardType);
//			setString("ls_unit_code", lsUnitCode);
//			setString("ls_bin_type", lsBinType);
//			sqlSelect(lsSql);
//			if (sqlRowNum <= 0) {
//				alertErr("ptr_ickey error ! -> " + lsCardType + " " + lsUnitCode);
//				return -1;
//			} else {
//				lsExpireDate = sqlStr("expire_date");
//				lsCheckKeyExpire = sqlStr("check_key_expire");
//			}
//			if (lsCheckKeyExpire.equals("Y") && lsValidTo.compareTo(lsExpireDate) > 0) {
//				alertErr("新效期超過晶片卡效期  ! ->" + lsValidTo + "," + lsExpireDate);
//				return -1;
//			}
//		}
		// check reissue_code
		if (!strAction.equals("D")) {
			if (empty(wp.itemStr2("reissue_code"))) {
				alertErr("需輸入重製說明!");
				return -1;
			}
		}
		//
		if (strAction.equals("D")) {
			if (wfUpdReissue() != 1) {
				return -1;
			} else {
				sqlCommit(1);
				return 1;
			}
		}
		// check group_code
		lsGroupCode = wp.itemStr2("group_code");
		lsSql = "select count(*) ct " + "from dbc_group_code " + "where group_code =:ls_group_code ";
		setString("ls_group_code", lsGroupCode);
		sqlSelect(lsSql);
		if (sqlNum("ct") > 0) {
			alertErr("此團代" + lsGroupCode + "不可重製");
			return -1;
		}
		// --check old_cardno--
		lsCurrentCode = wp.itemStr2("current_code");
		lsOppostDate = wp.itemStr2("oppost_date");
		lsUnitCode = wp.itemStr2("unit_code");
		lsMailType = wp.itemStr2("mail_type");
		if (empty(lsMailType)) {
			lsMailType = "4";
		}
		if (wfChkOppostDate(lsCurrentCode, lsOppostDate) != 1) {
			alertErr("掛失和偽卡重製日期需在6個月內,不可新增/異動此資料");
			return -1;
		}
		if (lsValidFm.compareTo(lsValidTo) > 0) {
			alertErr("有效期迄需大於起日");
			return -1;
		}
		if (wfChkUnitCode() != 1) {
			return -1;
		}
		if (strAction.equals("A")) {
			if (lsCurrentCode.equals("0")) {
				if (wp.itemStr2("reissue_status").equals("1")) {
					alertErr("此卡片已在重製卡中");
					return -1;
				}
				if (wp.itemStr2("reissue_status").equals("2")) {
					alertErr("此卡片已在送製卡中,不可再做重製卡");
					return -1;
				}
			}
			if (wp.itemStr2("emboss_reason").equals("1") || wp.itemStr2("emboss_reason").equals("3")) {
				if (!empty(wp.itemStr2("db_new_card_no"))) {
					alertErr("此卡片已重製成功過,不可重複重製");
					return -1;
				}
			}
			//
//			if (wp.item_ss("emboss_reason").equals("2")) {
//				ls_sql = "select count(*) ct "
//						+ "from dbc_card "
//						+ "where current_code = '0' "
//						+ "and card_no =:ls_card_no ";
//				setString("ls_card_no", m_card_no);
//				sqlSelect(ls_sql);
//				if (sql_int("ct") > 0) {
//					alert_err("此卡號還未停用");
//					return -1;
//				}
//			}
			//
			lsSql = "select count(*) ct from dbc_emboss_tmp where old_card_no =:ls_card_no ";
			setString("ls_card_no", mCardNo);
			sqlSelect(lsSql);
			if (sqlNum("ct") > 0) {
				alertErr("此資料在待製卡中");
				return -1;
			}
			//
			lsReissueStatus = wp.itemStr2("reissue_status");
			if (lsReissueStatus.equals("1") || lsReissueStatus.equals("2")) {
				alertErr("此資料在製卡狀態中");
				return -1;
			}
			// wf_move_emboss_tmp() 拆至validution及wf_cardno_data_JSON處理 Andy 20180919

			String changeStatus = wfGetChangeStatus();
			if (changeStatus.equals("1") || changeStatus.equals("2")) {
				alertErr("該卡續卡中，不得登錄重製");
				return -1;
			}

		}
		if (wfUpdReissue() != 1) {
			return -1;
		}
		return 1;
	}

	int wfUpdReissue() {
		String lsDate = getSysDate();
		String lsCardno = wp.itemStr2("old_card_no");
		String lsReason = wp.itemStr2("emboss_reason");
		String usSql = "";
		if (strAction.equals("A") || strAction.equals("U")) {
			usSql = "update dbc_card " + "set reissue_reason = :ls_reason, " + "reissue_status = '1', "
					+ "reissue_date = :ls_date " + "where card_no = :ls_cardno";
			setString("ls_reason", lsReason);
			setString("ls_date", lsDate);
			setString("ls_cardno", lsCardno);
			sqlExec(usSql);
			if (sqlRowNum <= 0) {
				alertErr("寫入卡片重製日期失敗");
				sqlCommit(0);
				return -1;
			}
		}
		if (strAction.equals("D")) {
			usSql = "update dbc_card " + "set reissue_reason = '', " + "reissue_status = '', " + "reissue_date = '' "
					+ "where card_no = :ls_cardno";
			setString("ls_cardno", lsCardno);
			sqlExec(usSql);
			if (sqlRowNum <= 0) {
				alertErr("寫入卡片重製日期失敗");
				sqlCommit(0);
				return -1;
			}
		}

		return 1;
	}

	int wfChkUnitCode() {
		String lsSql = "";
		if (empty(lsGroupCode)) {
			lsGroupCode = wp.itemStr2("group_code");
		}
		if (empty(lsCardType)) {
			lsCardType = wp.itemStr2("card_type");
		}
		lsSql = "select unit_code " + "from ptr_group_card_dtl " + "where group_code=:group_code "
				+ "and card_type=:card_type ";
		setString("group_code", lsGroupCode);
		setString("card_type", lsCardType);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			lsMsg = "此認同代碼,不存在所設定特殊之認同代碼內";
			alertErr("此認同代碼,不存在所設定特殊之認同代碼內");
			return -1;
		}
		return 1;
	}

	int wfGetSpecFlag(String asGroupCode, String asCardType) {
		String lsSql = "";
		if (empty(asGroupCode) || asGroupCode.equals("0000")) {
			return 1;
		}
		lsSql = "select spec_flag " + "from ptr_group_card " + "where group_code = :ls_group_code "
				+ "and card_type = :ls_card_type";
		setString("ls_group_code", asGroupCode);
		setString("ls_card_type", asCardType);
		sqlSelect(lsSql);
		if (sqlRowNum < 0) {
			alertErr("卡樣卡種抓取不到");
			return -1;
		} else {
			lsSpecFlag = sqlStr("spec_flag");
			wp.colSet("spec_flag", lsSpecFlag);
		}
		return 1;
	}

	String ofRelativeMm(String ymd, int y, int m) throws ParseException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date date = format.parse(ymd);
		cal.setTime(date);
		cal.add(Calendar.YEAR, y);
		cal.add(Calendar.MARCH, m);
		String lsChk = format.format(cal.getTime());
		return lsChk;
	}

	String wfGetChangeStatus() {
		String lsCardno = wp.colStr("kk_old_card_no");
		String sqlSelect = "SELECT change_status " + " FROM dbc_card " + " WHERE card_no = :ls_cardno ";
		setString("ls_cardno", lsCardno);
		sqlSelect(sqlSelect);
		String lsVal1 = sqlStr("change_status");

		return lsVal1;
	}
}
