/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                            *
* 111-09-27  V1.00.02   Alex      幣別為台幣時 , 金額不可輸入小數點                                                     *  
******************************************************************************/
package ccam01;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import busi.FuncAction;
import taroko.com.TarokoParm;
import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGate;
import bank.AuthIntf.AuthGateway;
import bank.AuthIntf.BicFormat;

public class Ccam1010Func extends FuncAction {
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	String cardNo = "", authNo = "";
	CcasWkVars oowk = new CcasWkVars();
	AuthGate gate = null;

	String isEffDate = "", isNewEndDate = "", isOldEndDate = "";
	String lsYymmx = "", lsYymm = "", isFlag = "", isMccCode = "";
	String isComboIndicator = "", isSupFlag = "", isCvc2 = "", lsCurrency = "" , lsConsumeCurr = ""; //--消費幣別
	double isAmt0 = 0,isAmt1 = 0, imOrgAmt = 0, imCurrRate = 0;
	String respP39 = "", isoAuthNo = "", errRespCode = "", authMesgData = "", authRespData = "";
	// String autu_resp_p39="", autu_auth_no="", autu_err_resp_code="";

	@Override
	public void dataCheck() {
		cardNo = wp.itemStr2("card_no");
		authNo = wp.itemStr2("auth_no");
		if (empty(cardNo))
			cardNo = wp.itemStr2("kk_card_no");
		if (empty(authNo))
			authNo = wp.itemStr2("kk_auth_no");

		busi.func.CrdFunc oocrd = new busi.func.CrdFunc();
		oocrd.setConn(wp);

		boolean lbDebit = oocrd.isDebitcard(cardNo);
		if (lbDebit) {
			selectDbcCard();
		} else
			selectCrdCard();
		if (rc != 1)
			return;
		if (eqIgno(colStr("card.db_card_indr"), "3")) {
			errmsg("採購卡不可做人工授權");
			return;
		}

		// ls_status_5 =trim(dw_1.object.card_acct_status_5[1])
		// if mid(ls_status_5,1,1) = '6' then
		// rtn = MessageBox("警告","凍結原因碼 " + ls_status_5 + "請再確認是否可做授權",Exclamation!,
		// OKCancel!, 2)
		// IF rtn <> 1 THEN
		// cb_query.enabled = FALSE
		// return FALSE
		// END IF
		// end if

		// --效期 check
		String lsMmyy = wp.itemStr2("eff_date");
		if (empty(lsMmyy)) {
			errmsg("有效期限應為: " + isNewEndDate);
			return;
		}

		isEffDate = "20" + commString.mid(lsMmyy, 2, 2) + commString.mid(lsMmyy, 0, 2);
		isNewEndDate = colStr("card.new_end_date");
		isOldEndDate = colStr("card.old_end_date");

		lsYymmx = commDate.sysDate().substring(0, 6);
		lsYymm = strMid(isEffDate, 0, 6);
//		if (commDate.sysComp(isEffDate) > 0) {
//			errmsg("錯誤 , 此卡為過期卡 ");
//			return;
//		}

		String lsNewYymm = strMid(isNewEndDate, 0, 6);
		String lsOldYymm = strMid(isOldEndDate, 0, 6);
		if (!eqIgno(lsYymm, lsNewYymm)) {
			if (empty(isOldEndDate) || empty(isEffDate)) {
				// errmsg("有效日期錯誤, 應為:" +commDate.dspDate(ls_new_yymm));
				errmsg("有效日期錯誤, 應為: " + strMid(lsNewYymm, 4, 2) + "/" + strMid(lsNewYymm, 0, 4));
				return;
			} else {
				if (!eqIgno(lsYymm, lsOldYymm)) {
					errmsg("有效日期錯誤, 應為新卡:" + strMid(lsNewYymm, 4, 2) + "/" + strMid(lsNewYymm, 0, 4) + " 舊卡 :"
							+ strMid(lsOldYymm, 4, 2) + "/" + strMid(lsOldYymm, 0, 4));
					return;
				} else {
					isFlag = "O";
				}
			}
		} else {
			isFlag = "N";
		}
		
		if ("O".equals(isFlag)) {
			if (commDate.sysComp(isOldEndDate) > 0) {
				errmsg("錯誤 , 此卡舊卡效期為過期卡 ");
				return;
			}
		}
		else {
			if (commDate.sysComp(isNewEndDate) > 0) {
				errmsg("錯誤 , 此卡新卡效期為過期卡 ");
				return;
			}
		}

		// --Mcht
		if (wp.itemEmpty("mcht_no")) {
			errmsg("商店代號:不可空白");
			return;
		}

		// --check mcc_code
		isMccCode = wp.itemStr("mcc_code");
		if (checkMccCode(isMccCode) == false) {
			errmsg("輸入 MCC 代碼錯誤");
			return;
		}

		if (eqIgno(isMccCode, "CASH")) {
			isComboIndicator = wp.itemNvl("combo_indicator", "N");
			isSupFlag = wp.itemStr("sup_flag");

			if (!eqIgno(isComboIndicator, "Y") || !eqIgno(isSupFlag, "0")) {
				errmsg("非Combo正卡, 不可輸入此MCC代碼...!");
				return;
			}
		}

		// --check amt
		isAmt0 = wp.itemNum("amt0");
		if(isAmt0 <= 0) {
			errmsg("消費地金額: 須>0");
			return ;
		}
		isAmt1 = wp.itemNum("amt1");
		if (isAmt1 <= 0) {
			errmsg("清算金額: 須>0");
			return;
		}

		lsCurrency = wp.itemNvl("currency", "901");
		
		//--9/27 判斷清算幣別為台幣 , 清算金額不可輸入小數點
		if(lsCurrency.equals("901")) {
			int tempInt = 0 ;
			tempInt = (int) isAmt1;
			if(isAmt1 % tempInt != 0) {
				errmsg("清算幣別為台幣 , 清算金額不可輸入小數點");
				return ;
			}
		}
		
		//--9/27 判斷消費貨幣幣別為台幣 , 消費地金額不可輸入小數點
		lsConsumeCurr = wp.itemStr("consume_curr");
		if(lsConsumeCurr.equals("901")) {
			int tempInt2 = 0;
			tempInt2 = (int) isAmt0;
			if(isAmt0 % tempInt2 != 0) {
				errmsg("消費幣別為台幣 , 清費地金額不可輸入小數點");
				return ;
			}
		}
		
		selectCurrRate(lsCurrency);
		imOrgAmt = Math.round(imCurrRate * isAmt1);
		if (ibUpdate) {
			if (imOrgAmt > wp.itemNum("amt2")) {
				errmsg("輸入更正金額超過原消費金額");
				return;
			}
		}

		if (empty(oowk.authNo))
			oowk.authNo = "******";
		if (wp.itemLen("auth_no") > 4 && wp.itemEq("auth_no", "******") == false) {
			oowk.authNo = wp.itemStr2("auth_no");
		}

	}

	void selectCrdCard() {
		strSql = "select A.current_code, A.new_end_date, uf_card_indicator(A.acct_type) as db_card_indr"
				+ ", A.new_end_date, A.old_end_date" + " from crd_card A" // left join cca_card_acct B"+
				+ " where A.card_no =?";
		setString2(1, cardNo);

		daoTid = "card.";
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			errmsg("查無卡片資料, kk[%s]", cardNo);
			return;
		}
	}

	void selectDbcCard() {
		strSql = "select A.current_code, A.new_end_date, uf_card_indicator(A.acct_type) as db_card_indr"
				+ ", A.new_end_date, A.old_end_date" + " from dbc_card A" // left join cca_card_acct B"+
				+ " where A.card_no =?";
		setString2(1, cardNo);

		daoTid = "card.";
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			errmsg("查無卡片資料, kk[%s]", cardNo);
			return;
		}
	}

	boolean checkMccCode(String lsMccCode) {

		String sql1 = " select " + " count(*) as db_cnt " + " from cca_mcc_risk " + " where mcc_code = ? ";
		sqlSelect(sql1, new Object[] { lsMccCode });

		if (colNum("db_cnt") <= 0) {
			return false;
		}

		return true;
	}

	void selectCurrRate(String lsCurrCode) {
		imCurrRate = 0;
		String sql2 = " select " + " exchange_rate " + " from ptr_curr_rate " + " where 1=1 " + " and curr_code = ? ";
		sqlSelect(sql2, new Object[] { lsCurrCode });
		if (sqlRowNum > 0) {
			imCurrRate = colNum("exchange_rate");
		}
		if (imCurrRate <= 0) {
			imCurrRate = 1;
		}
		return;
	}

	@Override
	public int dbInsert() {
		wp.colSet("cb_add", 0);
		wp.colSet("cb_delete", 0);

		dataCheck();
		if (rc != 1)
			return rc;

		if (wfConvIso8583(1) == false)
			return -1;
		if (wp.itemEmpty("tt_resp_code")) {
			if (callAutoAuth() != 1) {
				return -1;
			}
		} else {
			// -TTT-
			gate.auth_no = "123456";
			respP39 = wp.itemStr2("tt_resp_code");
			errRespCode = respP39;
		}

		wfRespMsg();
		// -------------自動核准後人工再確認start------------------------
		oowk.isoRspCode = respP39;
		wp.colSet("iso_resp_code", respP39);
		wp.colSet("bit39_resp_code", respP39);
		wp.colSet("tt_auth_color", "dsp_text");
		// wp.col_set("open_confirm","|| 1==1");
		if (eqIgno(respP39, "00")) {
			wp.colSet("tt_auth_text", "系統自動核准");
			wp.colSet("tt_auth_errmsg", "核准");
			wp.colSet("kk_cond", gate.auth_no);
			oowk.bit39AdjCode = "AE";
			wp.colSet("bit39_adj_code", "AE");
			// 系統自動核准後人工再確認畫面: open.w_auth_confirm
			return 1;
		}
		if (eqIgno(errRespCode, "CL")) {
			wp.alertMesg("拒絕授權: " + respP39 + ", " + oowk.errMsg);
			wp.colSet("tt_auth_color", "err_msg");
			wp.colSet("tt_auth_text", "系統自動拒絶");
			wp.colSet("tt_auth_errmsg", oowk.errMsg);
			// wp.col_set("open_consume","|| 1==1");
			if (eq(respP39, "41"))
				wp.colSet("bit39_adj_code", "AG");
			else
				wp.colSet("bit39_adj_code", "AF");
			return 1;
		}
		
		if(("00".equals(respP39) == false && "85".equals(respP39)==false) && "00".equals(errRespCode)) {
			wp.colSet("tt_auth_color", "err_msg");
			wp.alertMesg("授權失敗 - 其他原因");
			wp.colSet("tt_auth_text", "授權失敗");
			wp.colSet("tt_auth_errmsg", "其他原因");
			wp.colSet("auth_no", "");
			return 1;
		}
		
		wp.colSet("tt_auth_color", "err_msg");
		wp.alertMesg("拒絕授權: " + respP39 + ", " + oowk.errMsg);
		wp.colSet("tt_auth_text", "系統自動拒絶");
		wp.colSet("tt_auth_errmsg", oowk.errMsg);
		return 1;
	}
	
	@Override
	public int dbUpdate() {
		//--只可以修改MCC 其餘欄位不可修改 , 不用再送自動授權
		
		if(wp.itemEmpty("mcc_code")) {
			errmsg("mcc code : 不可空白");
			return rc;
		}
		
		strSql = " update cca_auth_txlog set "
			+ " mcc_code =:mcc_code , "
			+ " mod_pgm =:mod_pgm , "
			+ " mod_time = sysdate , "
			+ " mod_user =:mod_user , "
			+ " mod_seqno = nvl(mod_seqno,0)+1 "
			+ " where tx_date =:tx_date "
			+ " and tx_time =:tx_time "
			+ " and card_no =:card_no "
			+ " and auth_no =:auth_no "
			+ " and trace_no =:trace_no "
			;
		
		item2ParmStr("mcc_code");
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		item2ParmStr("tx_date");
		item2ParmStr("tx_time");
		item2ParmStr("card_no");
		item2ParmStr("auth_no");
		item2ParmStr("trace_no");
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("update Cca_auth_txlog error");
		}		
		
		return rc;
	}
	
//	@Override
//	public int dbUpdate() {
//		// ls_varN = 'N'
//		// if is_old_logic_del = '0' then //一般
//		// ls_logic_del = 'J'
//		// elseif is_old_logic_del = 'M' then //郵購
//		// ls_logic_del = 'D'
//		// elseif is_old_logic_del = 'C' then //預現
//		// ls_logic_del = 'A'
//		// end if
//
//		String lsMmyy = wp.itemStr("eff_date");
//		isEffDate = "20" + commString.mid(lsMmyy, 2, 2) + commString.mid(lsMmyy, 0, 2);
//		if (wfConvIso8583(2) == false) {
//			return -1;
//		}
//		if (callAutoAuth() != 1) {
//			return -1;
//		}
//		wfRespMsg();
//		if (eqIgno(oowk.isoRspCode, "00")) {
//			if (!empty(oowk.apprCode)) {
//				wp.alertMesg("授權交易修改完成 - " + respP39);
//			} else
//				wp.alertMesg("授權交易修改完成");
//		} else {
//			errmsg("授權交易修改失敗: " + respP39 + ", " + oowk.errMsg);
//		}
//		return rc;
//	}

	@Override
	public int dbDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataProc() {
		String lsMmyy = wp.itemStr2("eff_date");
		isEffDate = "20" + commString.mid(lsMmyy, 2, 2) + commString.mid(lsMmyy, 0, 2);

		// [確認]核准
		if (wfConvIso8583(2) == false)
			return -1;

		if (callAutoAuth() != 1) {
			return -1;
		}
		wfRespMsg(); // err_resp_code
		
		if (eqIgno(respP39, "00")) {
			wp.colSet("tt_auth_text", "系統自動核准");
			wp.colSet("tt_auth_errmsg", "核准");
			wp.colSet("kk_cond", gate.auth_no);
			oowk.bit39AdjCode = "AE";
			wp.colSet("bit39_adj_code", "AE");
			wp.colSet("open_1010", "|| 1==1");
			// 系統自動核准後人工再確認畫面: open.w_auth_confirm
			return 1;
		}
		if (eqIgno(errRespCode, "CL")) {
			wp.alertMesg("拒絕授權: " + respP39 + ", " + oowk.errMsg);
			wp.colSet("tt_auth_color", "err_msg");
			wp.colSet("tt_auth_text", "系統自動拒絶");
			wp.colSet("tt_auth_errmsg", oowk.errMsg);
			// wp.col_set("open_consume","|| 1==1");
			if (eq(respP39, "41"))
				wp.colSet("bit39_adj_code", "AG");
			else
				wp.colSet("bit39_adj_code", "AF");
			return 1;
		}
		
		if(("00".equals(respP39) == false && "85".equals(respP39)==false) && "00".equals(errRespCode)) {
			wp.colSet("tt_auth_color", "err_msg");
			wp.alertMesg("授權失敗 - 其他原因");
			wp.colSet("tt_auth_text", "授權失敗");
			wp.colSet("tt_auth_errmsg", "其他原因");
			wp.colSet("auth_no", "");
			return 1;
		}
		
		wp.colSet("tt_auth_color", "err_msg");
		wp.alertMesg("拒絕授權: " + respP39 + ", " + oowk.errMsg);
		wp.colSet("tt_auth_text", "系統自動拒絶");
		wp.colSet("tt_auth_errmsg", oowk.errMsg);		
		wp.colSet("auth_no", "");
		//--
		
		
		return 1;
	}

	public int consumeInsertAuthTxlog() {
		msgOK();
		wfConvIso8583(9);

		String lsMccCode = wp.itemStr2("mcc_code");
		wp.sqlCmd = "select risk_type, amount_rule, nccc_ftp_code " + " from cca_mcc_risk" + " where mcc_code =?";
		setString2(1, lsMccCode);
		sqlSelect(wp.sqlCmd);
		if (sqlRowNum > 0) {
			wp.itemSet2("iso.msgtype", colStr("nccc_ftp_code"));
			oowk.riskType = colStr("risk_type");
			oowk.amountRule = colStr("amount_rule");
			oowk.ncccFtpCode = colStr("nccc_ftp_code");
		} else {
			wp.itemSet2("iso.msgtype", "AP");
			oowk.riskType = "";
			oowk.amountRule = "P";
			oowk.ncccFtpCode = "AP";
		}

		String lsBit38ApprCode = "00";
		oowk.isoRspCode = "06";
		oowk.rspUnnormalFlag = "";
		wp.sqlCmd = "select nccc_p38 as sys_data2, nccc_p39 as sys_data3" + ", abnorm_flag" + " from cca_resp_code"
				+ " where resp_code =?";
		setString2(1, wp.itemStr2("bit39_resp_code"));
		sqlSelect(wp.sqlCmd);
		if (sqlRowNum > 0) {
			lsBit38ApprCode = colStr("sys_data2");
			oowk.isoRspCode = colStr("sys_data3");
			oowk.rspUnnormalFlag = colStr("abnorm_flag");
		}
		// -----------new--------------------------
		String lsCacuAmount = "", lsCacuCash = "";
		if (eq(oowk.isoRspCode, "00") == false || (wp.colEq("acct_type", "05") && wp.colEq("mcc_code", "CASH"))) {
			lsCacuAmount = "N";
			lsCacuCash = "N";
		} else {
			lsCacuAmount = "Y";
			lsCacuCash = "N";
			if (eq(oowk.amountRule, "C") || commString.strIn2(wp.colStr("mcc_code"), ",6010,6011"))
				lsCacuCash = "Y";
		}
		String lsCcasAreaFlag = "";
		if (wp.colEq("mcht_country", "TW")) {
			lsCcasAreaFlag = "T";
		} else
			lsCcasAreaFlag = "F";
		String lsAuthUnit = "K"; // 表人工授權('K'eyin)
		String lsLogicDel = commString.mid(oowk.ncccFtpCode, 2, 1);
		if (commString.strIn2(lsLogicDel, ",M,C") == false)
			lsLogicDel = "0";

		String lsEffdateCard = wp.itemStr("new_end_date");
		String lsEffdateUser = wp.colStr("eff_date");
		lsEffdateUser = commString.mid(lsEffdateCard, 0, 2) + commString.mid(lsEffdateUser, 2, 2)
				+ commString.mid(lsEffdateUser, 0, 2);
		// -當時OTB, 當時額度, 標準額度-
//		double lmCurrOtbAmt = wp.num("remain_tot");
		double lmCurrOtbAmt = wp.num("remain_tot") + wp.itemNum("amt2");
		double lmCurrLmtAmt = wp.num("line_of_credit_amt");
		double lmCurrStdAmt = wp.num("line_of_credit_amt");
		if (wp.num("tot_amt_month") > 0) {
			lmCurrLmtAmt = wp.num("tot_amt_month");
		}
		// --
		sql2Insert("cca_auth_txlog");
		addsqlParm(" ?", " tx_date", wp.sysDate);
		addsqlParm(",?", ", tx_time", wp.sysTime);
		addsqlParm(", tx_datetime", ", to_date(" + wp.sysDate + wp.sysTime + ",'yyyymmddhh24miss')");
		addsqlParm(",?", ", card_no", wp.colStr("card_no"));
		addsqlParm(",?", ", auth_no", wp.itemStr2("ex_auth_no"));
		addsqlParm(",?", ", sup_flag", wp.colStr("sup_flag"));
		addsqlParm(",?", ", acct_type", wp.colStr("acct_type"));
		addsqlParm(",?", ", acno_p_seqno", wp.colStr("acno_p_seqno"));
		addsqlParm(",?", ", id_p_seqno", wp.colStr("id_p_seqno"));
		addsqlParm(",?", ", corp_p_seqno", wp.colStr("corp_p_seqno"));
		addsqlParm(",?", ", class_code", wp.colStr("class_code"));
		addsqlParm(",?", ", trans_type", wp.itemStr2("iso.msgtype"));
		addsqlParm(",?", ", proc_code", gate.isoField[3]); // is_str_iso.bit3_proc_code);
		addsqlParm(",?", ", trace_no", gate.isoField[11]); // :is_str_iso.bit11_trace_no);
		addsqlParm(",?", ", mcc_code", wp.colStr("mcc_code"));
		addsqlParm(",?", ", bank_country", wp.colStr("mcht_country")); // :is_str_iso.bit19_country);
		addsqlParm(",?", ", pos_mode", "002");
		addsqlParm(",?", ", cond_code", gate.isoField[25]); // is_str_iso.bit25_serv_cond);
		addsqlParm(",?", ", stand_in", gate.isoField[32]); // is_str_iso.bit32_acq_id);
		addsqlParm(",?", ", iso_resp_code", oowk.isoRspCode); // is_str_wk.wk_iso_rsp_code);
		addsqlParm(",?", ", iso_adj_code", wp.colStr("bit39_adj_code"));
		addsqlParm(",?", ", pos_term_id", gate.isoField[41]); // is_str_iso.bit41_term_id);
		addsqlParm(",?", ", term_id", "");
		addsqlParm(",?", ", mcht_no", wp.colStr("mcht_no")); // mid(is_str_iso.bit48_add_data,1,10)
		addsqlParm(",?",", mcht_name", wp.itemStr("mcht_name"));
		addsqlParm(", mcht_city_name", ", 'Taipei'");
		addsqlParm(", mcht_city", ", 'TW'");
		addsqlParm(", mcht_country", ", 'TW'");
		addsqlParm(",?", ", eff_date_end", lsEffdateCard);
		addsqlParm(",?", ", user_expire_date", lsEffdateUser);
		addsqlParm(",?", ", risk_type", oowk.riskType);
		addsqlParm(",?", ", tx_currency", wp.colStr("currency"));
		addsqlParm(",?", ", consume_country", wp.colStr("mcht_country"));
		addsqlParm(",?", ", ori_amt", oowk.oriAmt);
		addsqlParm(",?", ", nt_amt", wp.colNum("amt1"));
		addsqlParm(",?", ", auth_status_code", wp.itemStr2("bit39_resp_code"));
		// tx_remark varchar 60 0 n
		addsqlParm(",?", ", auth_remark", wp.colStr("ex_auth_remark"));
		addsqlParm(",?", ", auth_user", modUser);
		addsqlParm(",?", ", apr_user", wp.colStr("ex_apr_user"));
		addsqlParm(",?", ", ccas_area_flag", lsCcasAreaFlag);
		addsqlParm(",?", ", auth_type", "1");
		addsqlParm(",?", ", card_status", wp.colStr("current_code"));
		addsqlParm(",?", ", logic_del", lsLogicDel);
		addsqlParm(",?", ", mtch_flag", "N");
		// mtch_date varchar 8 0 n
		// balance_flag varchar 1 0 n
		// to_accu_mtch varchar 1 0 n
		addsqlParm(",?", ", curr_otb_amt", lmCurrOtbAmt);
		addsqlParm(",?", ", curr_tot_lmt_amt", lmCurrLmtAmt);
		addsqlParm(",?", ", curr_tot_std_amt", lmCurrStdAmt);
		// aaa(",?",", curr_otb_amt", is_str_cal.txlog_curr_otb_amt);
		// aaa(",?",", curr_tot_lmt_amt", is_str_cal.txlog_curr_tot_lmt_amt);
		// aaa(",?",", curr_tot_std_amt", is_str_cal.acct_lmt_tot_consume);
		// aaa(",?",", curr_tot_tx_amt", is_str_cal.acct_tot_amt_consume);
		// aaa(",?",", curr_tot_cash_amt", is_str_cal.acct_tot_amt_precash);
		// aaa(",?",", curr_tot_unpaid", is_str_cal.txlog_curr_tot_unpaid);
		// aaa(",?",", tx_amt_pct", is_str_cal.txlog_tx_amt_pct);
		// stand_in_reason varchar 2 0 n
		addsqlParm(",?", ", auth_unit", lsAuthUnit);
		addsqlParm(",?", ", cacu_amount", lsCacuAmount);
		addsqlParm(",?", ", cacu_cash", lsCacuCash);
		// cacu_flag varchar 1 0 n
		// stand_in_rspcode varchar 2 0 n
		// stand_in_onuscode varchar 2 0 n
//		addsqlParm(",?", ", tx_cvc2", wp.colStr("cvc2"));
		// ae_trans_amt varchar 12 0 n
		// roc varchar 4 0 n
		// online_redeem varchar 1 0 n
		// ibm_bit33_code varchar 13 0 n
		// ibm_bit39_code varchar 3 0 n
		// aaa(",?",", acct_no", wp.col_ss("card_no"));
		// vip_code varchar 2 0 n
		// ec_flag varchar 1 0 n
		// cvd_present varchar 1 0 n
		// ec_ind varchar 1 0 n
		// ucaf varchar 3 0 n
		// cavv_result varchar 1 0 n
		// train_flag varchar 1 0 n
		// group_code varchar 4 0 n
		// fallback varchar 5 0 n
		addsqlParm(", fallback", ",'N'");
		// fraud_chk_rslt varchar 2 0 n
		// ac_very_rslt varchar 2 0 n
		// v_card_no varchar 32 0 n
		addsqlParm(", auth_seqno", ", uf_auth_seqno()");
		addsqlParm(",?", ", vdcard_flag", wp.colStr("debit_flag"));
		// reversal_flag varchar 1 0 n
		addsqlParm(", trans_code", ",'MA'");
		addsqlParm(",?", ", card_acct_idx", wp.colNum("card_acct_idx"));
		// ref_no varchar 12 0 n
		// ori_auth_no varchar 6 0 n
		addsqlYmd(", crt_date");
		addsqlTime(", crt_time");
		addsqlParm(",?", ", crt_user", modUser);
		// chg_date varchar 8 0 n
		// chg_time varchar 6 0 n
		// chg_user varchar 10 0 n
		addsqlModXXX(modUser, modPgm);

		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("交易寫入交易記錄檔內失敗");
			return rc;
		}

		// -統計檔: 改批次處理-
		// wf_sta_tx_unnormal();
		// if (rc !=1) {
		// errmsg("寫入不成功統計(CCA_STA_TX_UNMORMAL)資料錯誤");
		// return rc;
		// }
		// wf_2sta_risk_type();
		// if (rc !=1) {
		// errmsg("寫入不成功統計(CCA_STA_RISK_TYPE)資料錯誤");
		// return rc;
		// }
		// wf_3sta_daily_mcc();
		// if (rc !=1) {
		// errmsg("寫入不成功統計(CCA_STA_DAILY_MCC)資料錯誤");
		// return rc;
		// }

		return rc;
	}

	public int consumeAuthOk() {
		String lsMmyy = wp.itemStr2("eff_date");
		isEffDate = "20" + commString.mid(lsMmyy, 2, 2) + commString.mid(lsMmyy, 0, 2);

		if (consumeIso8583() == false)
			return -1;

		if (callAutoAuth() != 1) {
			return -1;
		}
		wfRespMsg();

		wp.colSet("ex_auth_no", isoAuthNo);
		if (eq(respP39, "00")) {
			wp.alertMesg("授權核准: 授權碼=" + isoAuthNo);
		}
		return 1;
	}

	void wfRespMsg() {
		wp.colSet("wk_flag", "T"); // 表拒絶:強制輸入進入詳核
		if (eq(respP39, "00")) {
			wp.colSet("wk_flag", "H"); // 表核准:強制輸入進入詳核
		} else {
			if (eq(respP39, "41")) {
				wp.colSet("bit39_adj_code", "AG");
			} else {
				wp.colSet("bit39_adj_code", "AF");
			}
		}
		if (eqIgno(respP39, "00")) {
			oowk.isoRspCode = "00";
			return;
		}
		if (eq(respP39, "00") == false && empty(errRespCode)) {
			errmsg("error: resp_p39[%s], err_resp_code[%s]", respP39, errRespCode);
			return;
		}
		String lsErrCode = errRespCode;

		strSql = "select resp_remark, nccc_p38, nccc_p39, resp_status" + " from cca_resp_code" + " where resp_code =?";
		setString2(1, errRespCode);
		daoTid = "resp.";

		sqlSelect(strSql);
		if (sqlRowNum == 1) {
			oowk.apprCode = colStr("resp.nccc_p38");
			oowk.isoRspCode = colStr("resp.nccc_p39");
			oowk.rspUnnormalFlag = colStr("resp.resp_status");
			oowk.errMsg = "[" + errRespCode + "] " + colStr("resp.resp_remark");
			// errmsg(err_resp_code+" "+oowk.err_msg);
		} else {
			oowk.errMsg = "授權回覆資料=[" + authRespData + "]"; // col_ss("resp.resp_remark");
			oowk.apprCode = "00";
			oowk.isoRspCode = "96";
			oowk.rspUnnormalFlag = "N";
		}
	}

	boolean wfConvIso8583(int aiType) {
		gate = new AuthGate();

		// 1.授權查核, 9.拒絕交易[RCU]
		String lsConsumeKind, lsConsumeType, lsCardNo;
		String lsMccCode, lsPosCode, lsMchtNo;
		String lsBankNo, lsCurrency4 ,lsCurrency6 , lsCountry, lsSrvType;
		String lsCcvCode, lsPvkiCode, lsPvvCode, lsEdcAuthCode;
		String lsAuthCode, lsEdcTxCode, lsCardType;
		double lmConsumeAmt4 , lmConsumeAmt5 , lmConsumeAmt6, lmDdjAmt;

		if (eqIgno(oowk.riskType, "M")) {
			lsConsumeKind = "800030";
			lsConsumeType = "08";
			if (ibUpdate) {
				lsConsumeKind = "020030";
			}
		} else if (eqIgno(oowk.riskType, "C")) {
			lsConsumeKind = "010030";
			lsConsumeType = "00";
			if (ibUpdate) {
				lsConsumeKind = "140030";
			}
		} else {
			if (ibUpdate) {
				lsConsumeKind = "020030";
			} else
				lsConsumeKind = "000030";
			lsConsumeType = "00";
		}

		lsCardNo = wp.itemStr2("card_no");
		lsMccCode = wp.itemStr2("mcc_code");
		lsPosCode = "web-apuser"; // gs_computername
		lsMchtNo = wp.itemStr2("mcht_no");
		lsBankNo = wp.itemStr("acq_bank_id");
		if(empty(lsBankNo))	lsBankNo = "493817";
		lsCurrency4 = wp.itemStr2("consume_curr");
		lsCurrency6 = wp.itemStr2("currency");
		lsCountry = wp.itemStr2("mcht_country");
		lsSrvType = "123";
		lsCcvCode = "4";
		lsPvkiCode = "5";
		lsPvvCode = "6789";
		lsEdcAuthCode = "000000";
		lsAuthCode = "******"; // ""000000";
		lsEdcTxCode = "11";
		lsCardType = oowk.cardType;
		lmConsumeAmt4 = wp.itemNum("amt0");
		lmConsumeAmt5 = wp.itemNum("amt2");
		lmConsumeAmt6 = wp.itemNum("amt1");
//		lmConsumeAmt = wp.itemNum("amt2");
		lmDdjAmt = 0;
		String lsKkAuthno = wp.itemStr("kk_auth_no");
		// -拒絕交易-
		if (aiType == 9) {
			lsAuthCode = "******";
		} else if (!empty(lsKkAuthno)) {
			lsAuthCode = lsKkAuthno;
		}
		if (ibUpdate) {
			if (commString.strIn2(oowk.onlineRedeem, ",1,2"))
//				lmConsumeAmt = wp.num("ori_amt"); // wp.itemNum("amt1") / 100; // dec(is_str_iso.bit4_tran_amt)/100
				lmConsumeAmt4 = wp.num("ori_amt");
			else
//				lmConsumeAmt = wp.num("ori_amt"); // oowk.nt_amt;
				lmConsumeAmt4 = wp.num("ori_amt");
			lmDdjAmt = wp.itemNum("amt2");
		}
		if (lsCardNo.length() != 16 && lsCardNo.length() != 15) {
			errmsg("Account ID must be 16 or 15 Bytes.");
			return false;
		}
		if (lsConsumeKind.length() != 6) {
			errmsg("Consume Kind must be 6 Bytes.");
			return false;
		}
		if (lsConsumeType.length() != 2) {
			errmsg("Consume Type must be 2 Bytes.");
			return false;
		}
		if (lsMccCode.length() != 4) {
			errmsg("Merchant Code must be 4 Bytes.");
			return false;
		}
		if (lsAuthCode.length() != 6) {
			errmsg("Approve Code must be 6 Bytes.");
			return false;
		}
		if (lsCountry.length() != 3) {
			lsCountry = commString.rpad(lsCountry, 3);
		}
		if (empty(lsConsumeType)) {
			lsConsumeType = "00";
		}
		lsConsumeType = commString.rpad(lsConsumeType, 2);

		this.dateTime();
		// ------------------------------
		boolean lbNewNccc = true;
		if (lbNewNccc)
			gate.bicHead = "ISO026000076";
		else
			gate.bicHead = "ISO025000076";
		gate.mesgType = "0200";
		gate.isoField[2] = lsCardNo;
		gate.isoField[3] = lsConsumeKind;
		//----[4]消費地金額 [5] 台幣金額 [6]清算金額
		gate.isoField[4] = commString.lpad(String.format("%.0f", (lmConsumeAmt4 * 100)), 12, "0");
		gate.isoField[5] = commString.lpad(String.format("%.0f", (lmConsumeAmt5 * 100)), 12, "0");
		if(wp.itemEq("card_curr_code", "901") == false && "901".equals(lsCurrency6) == false) {
			gate.isoField[6] = commString.lpad(String.format("%.0f", (lmConsumeAmt6 * 100)), 12, "0");
		}
		gate.isoField[7] = commDate.sysDatetime().substring(4);
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			// random = new Random(new Date().getTime());
			throw new RuntimeException("init SecureRandom failed.", e);
		}
		gate.isoField[11] = commString.numFormat(random.nextDouble() * 1000000, "000000");
		gate.isoField[12] = this.sysDate;
		gate.isoField[13] = strMid(sysDate, 4, 4);
		gate.isoField[15] = strMid(sysDate, 4, 4);
		gate.isoField[17] = strMid(sysDate, 4, 4); // IsoRec.bit15_setl_date
		gate.isoField[18] = lsMccCode;
		if (eqIgno(lsCountry, "TW"))
			gate.isoField[19] = "158";
		else
			gate.isoField[19] = lsCountry;
		gate.isoField[22] = "002";
		gate.isoField[25] = lsConsumeType;
		gate.isoField[26] = ""; // IsoRec.bit26_pin_len = "00"
		if (aiType == 1) {
			// -1st.授權查核-
			gate.isoField[27] = "N";
		} else {
			if (wp.colEq("kk_action", "U"))
				gate.isoField[27] = "A";
			else if (wp.colEq("kk_action", "A")) {
				if (wp.itemEmpty("auth_no") == false && wp.itemNe("auth_no", "000000"))
					gate.isoField[27] = "F";
				else
					gate.isoField[27] = "Y";
			}
		}
		gate.isoField[32] = lsBankNo;
//		gate.isoField[35] = lsCardNo + "=" + commString.mid(isEffDate, 4, 2) + commString.mid(isEffDate, 2, 2) + lsPvkiCode + lsPvvCode + lsCcvCode;
		gate.isoField[35] = lsCardNo + "=" + commString.mid(isEffDate, 2, 4) + lsPvkiCode + lsPvvCode + lsCcvCode;
		
		gate.isoField[37] = getSysDate().substring(6,8)+commString.numFormat(random.nextDouble() * 1000000000, "0000000000");
		if (ibUpdate || eqIgno(gate.isoField[27], "F"))
			gate.isoField[38] = wp.itemStr2("auth_no");
		else
			gate.isoField[38] = lsAuthCode;
		gate.isoField[39] = "";
		gate.isoField[41] = lsPosCode;
		gate.isoField[42] = lsMchtNo;
		gate.isoField[43] = commString.rpad(commString.mid(wp.itemStr("mcht_eng_name"), 0,22), 22) + commString.rpad("Taipei", 13) + lsCountry
				+ commString.mid(lsCountry, 0, 2);
		gate.isoField[48] = lsMchtNo;
		//----[49]消費地幣別 [50] 901  [51]清算幣別
		gate.isoField[49] = lsCurrency4;
		gate.isoField[50] = "901";
		if(wp.itemEq("card_curr_code", "901") == false && "901".equals(lsCurrency6) == false) {
			gate.isoField[51] = lsCurrency6;
		}
		gate.isoField[60] = "BK02" + "BK02" + "00000000";
		if (lbNewNccc) {
			gate.isoField[61] = "BK02" + "PRO200000000000";
		} else
			gate.isoField[61] = "BK02" + "PRO100000000000";
		gate.isoField[66] = "9";
		gate.isoField[90] = commString.space(4) + commString.space(12) + commString.space(4) + commString.space(8) + commString.space(4)
				+ commString.space(10);
		gate.isoField[95] = "" + (lmDdjAmt * 100) + "000000000000000000000000000000";
		gate.isoField[127] = commString.rpad(modUser, 10," ");

		for (int ii = 0; ii < gate.isoField.length; ii++) {
			if (!empty(gate.isoField[ii])) {
				wp.log("%s[%s]", ii, gate.isoField[ii]);
			}
		}
		//
		// is_str_iso = IsoRec
		// is_str_iso.user_expire_date = mid(ls_create_date,1,2)+dw_2.object.eff_date[1]
		return true;
	}

	int callAutoAuth() {
		respP39 = "99";
		isoAuthNo = "";
		int liRc=1;
		String lsAuthIp = "", lsAuthPortNo = "";

		if (empty(lsAuthIp) || empty(lsAuthPortNo)) {
			strSql = "select wf_value, wf_value2" + ", wf_value3, wf_value4" + " from ptr_sys_parm"
					+ " where wf_parm ='SYSPARM' and wf_key='CCASLINK'";
			sqlSelect(strSql);
			if (sqlRowNum <= 0) {
				sqlErr("ptr_sysparm.SYSPARM,CCASLINK");
				return -1;
			}
			String dbSwitch2Dr = TarokoParm.getInstance().getDbSwitch2Dr();
			wp.log("Switch Flag =["+dbSwitch2Dr+"]");
			if (wp.localHost() || "Y".equals(dbSwitch2Dr)) {
				lsAuthIp = "127.0.0.1";
				lsAuthPortNo = "15001";
			} else if("3".equals(dbSwitch2Dr) || "6".equals(dbSwitch2Dr)){
				lsAuthIp = colStr("wf_value3");
				lsAuthPortNo = colStr("wf_value4");
			} else {
				lsAuthIp = colStr("wf_value");
				lsAuthPortNo = colStr("wf_value2");
			}
		}
		if (empty(lsAuthIp) || empty(lsAuthPortNo)) {
			errmsg("自動授權[IP,Port-No]: 不可空白");
			return rc;
		}
		
		wp.log("Auto-Auth call ["+lsAuthIp+"]" + "["+lsAuthPortNo+"]");
		
		BicFormat bic = new BicFormat(null, gate, null);
		bic.host2Iso();
		
		wp.commitOnly();

		AuthData authdata = new AuthData();
		authdata.setFullIsoCommand(gate.isoString.substring(2));
		AuthGateway authway=null;
		try {
			authway=new AuthGateway();
			String lsRespData = authway.startProcess(authdata, lsAuthIp, lsAuthPortNo);
			wp.log("AUTH-resp-data:[%s]", lsRespData);
			/*
			 * String sL_IsoField39 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[39], 2);
			 * //BIT39_ADJ_CODE String sL_IsoField38 =
			 * HpeUtil.fillZeroOnLeft(G_Gate.isoField[38], 6); //BIT38_APPR_CODE String
			 * sL_IsoField73 = HpeUtil.fillZeroOnLeft(G_Gate.isoField[73], 6);
			 * //BIT73_ACT_DATE String sL_IsoField92 =
			 * HpeUtil.fillZeroOnLeft(G_Gate.isoField[92], 2); // String sL_IsoField120 =
			 * G_Gate.isoField[120]; //BIT120_MESS_DATA
			 */
			authRespData = lsRespData;
			respP39 = lsRespData.substring(0, 2);
			isoAuthNo = commString.mid(lsRespData, 2, 6);
			errRespCode = commString.mid(lsRespData, 14, 2);
			authMesgData = commString.mid(lsRespData, 16);
		} catch(Exception ex) {
			errmsg("call_autoAuth error; "+ex.getMessage());
			liRc= -1;
		}	finally {
			authway.releaseConnection();			
		}

		wp.colSet("iso_resp_code", respP39);
		if (eqIgno(respP39, "00")) {
			wp.colSet("auth_no", isoAuthNo);
		}

		return liRc;
	}

	boolean consumeIso8583() {
		gate = new AuthGate();

		// 1.授權查核, 9.拒絕交易[RCU]
		String file = "";
		String lsConsumeKind, lsConsumeType, lsCardNo;
		String lsMccCode, lsPosCode, lsMchtNo;
		String lsBankNo, lsCurrency4 , lsCurrency6 , lsCountry, lsSrvType;
		String lsCcvCode, lsPvkiCode, lsPvvCode, lsEdcAuthCode;
		String lsAuthCode, lsEdcTxCode, lsCardType;
		double lmConsumeAmt4 , lmConsumeAmt5 , lmConsumeAmt6, lmAdjAmt;

		if (eq(oowk.riskType, "M")) {
			lsConsumeKind = "800030";
			lsConsumeType = "08";
		} else if (eqIgno(oowk.riskType, "C")) {
			lsConsumeKind = "010030";
			lsConsumeType = "00";
		} else {
			lsConsumeKind = "000030";
			lsConsumeType = "00";
		}

		lsCardNo = wp.itemStr2("card_no");
		lsMccCode = wp.itemStr2("mcc_code");
		lsPosCode = "web-apuser"; // gs_computername
		lsMchtNo = wp.itemStr2("mcht_no");
		lsBankNo = wp.itemStr("acq_bank_id");
		if(empty(lsBankNo))	lsBankNo = "493817";
		lsCurrency4 = wp.itemStr2("consume_curr");
		lsCurrency6 = wp.itemStr2("currency");
//		lsCurrency = wp.itemStr2("currency");
		lsCountry = wp.itemStr2("mcht_country");
		lsSrvType = "123";
		lsCcvCode = "4";
		lsPvkiCode = "5";
		lsPvvCode = "6789";
		lsEdcAuthCode = "000000";
		lsAuthCode = "000000";
		lsEdcTxCode = "11";
		lsCardType = oowk.cardType;
		lmConsumeAmt4 = wp.itemNum("amt0");
		lmConsumeAmt5 = wp.itemNum("amt2");
		lmConsumeAmt6 = wp.itemNum("amt1");
//		lmConsumeAmt = wp.colNum("amt2");
		lmAdjAmt = 0;
		if (!wp.itemEmpty("kk_auth_no")) {
			lsAuthCode = wp.itemStr("kk_auth_no");
		}
		lsCountry = commString.rpad(lsCountry, 3);
		lsConsumeType = commString.rpad(commString.nvl(lsConsumeType, "00"), 2);

		this.dateTime();
		// ------------------------------
		boolean lbNewNccc = true;
		if (lbNewNccc)
			gate.bicHead = "ISO026000076";
		else
			gate.bicHead = "ISO025000076";
		gate.mesgType = "0200";
		gate.isoField[2] = lsCardNo;
		gate.isoField[3] = lsConsumeKind;
		//----[4]消費地金額 [5] 台幣金額 [6]清算金額
		gate.isoField[4] = commString.lpad(String.format("%.0f", (lmConsumeAmt4 * 100)), 12, "0");
		gate.isoField[5] = commString.lpad(String.format("%.0f", (lmConsumeAmt5 * 100)), 12, "0");
		if(wp.itemEq("card_curr_code", "901") == false && "901".equals(lsCurrency6) == false) {
			gate.isoField[6] = commString.lpad(String.format("%.0f", (lmConsumeAmt6 * 100)), 12, "0");
		}
		gate.isoField[7] = commDate.sysDatetime().substring(4);
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			// random = new Random(new Date().getTime());
			throw new RuntimeException("init SecureRandom failed.", e);
		}
		gate.isoField[11] = commString.numFormat(random.nextDouble() * 1000000, "000000");
		gate.isoField[12] = this.sysDate;
		gate.isoField[13] = strMid(sysDate, 4, 4);
		gate.isoField[15] = strMid(sysDate, 4, 4);
		gate.isoField[17] = strMid(sysDate, 4, 4); // IsoRec.bit15_setl_date
		gate.isoField[18] = lsMccCode;
		if (eqIgno(lsCountry, "TW"))
			gate.isoField[19] = "158";
		else
			gate.isoField[19] = lsCountry;
		gate.isoField[22] = "002";
		gate.isoField[25] = lsConsumeType;
		gate.isoField[26] = ""; // IsoRec.bit26_pin_len = "00"
		 // 強制授權
		if(wp.itemEmpty("ex_auth_resp")) {
			gate.isoField[27] = "F";
		}	else	{
			gate.isoField[27] = wp.itemStr("ex_auth_resp");
		}
		
		gate.isoField[32] = lsBankNo;
		
//		gate.isoField[35] = lsCardNo + "=" + commString.mid(isEffDate, 4, 2) + commString.mid(isEffDate, 2, 2) + lsPvkiCode + lsPvvCode + lsCcvCode;
		gate.isoField[35] = lsCardNo + "=" + commString.mid(isEffDate, 2, 4) + lsPvkiCode + lsPvvCode + lsCcvCode;
		gate.isoField[37] = getSysDate().substring(6,8)+commString.numFormat(random.nextDouble() * 1000000000, "0000000000");
		file = wp.colStr("ex_auth_no");
		if (notEmpty(file) && !eq(file, "000000") && !eq(file, "******"))
			gate.isoField[38] = file;
		else
			gate.isoField[38] = lsAuthCode;
		gate.isoField[39] = wp.colStr("bit39_resp_code");
		gate.isoField[41] = lsPosCode;
		gate.isoField[42] = lsMchtNo;
		gate.isoField[43] = commString.rpad(commString.mid(wp.itemStr("mcht_eng_name"), 0,22), 22) + commString.rpad("Taipei", 13) + lsCountry
				+ commString.mid(lsCountry, 0, 2);
		gate.isoField[48] = lsMchtNo;
		
		//----[49]消費地幣別 [50] 901  [51]清算幣別
		gate.isoField[49] = lsCurrency4;
		gate.isoField[50] = "901";
		if(wp.itemEq("card_curr_code", "901") == false && "901".equals(lsCurrency6) == false) {
			gate.isoField[51] = lsCurrency6;
		}
		gate.isoField[60] = "BK02" + "BK02" + "00000000";
		if (lbNewNccc) {
			gate.isoField[61] = "BK02" + "PRO200000000000";
		} else
			gate.isoField[61] = "BK02" + "PRO100000000000";
		gate.isoField[66] = "9";
		gate.isoField[90] = commString.space(4) + commString.space(12) + commString.space(4) + commString.space(8) + commString.space(4)
				+ commString.space(10);
		gate.isoField[95] = "" + (lmAdjAmt * 100) + "000000000000000000000000000000";
		gate.isoField[127] = commString.rpad(modUser, 10," ");
		if (wp.colEmpty("auth_remark") == false) {
			gate.isoField[120] = wp.colStr("auth_remark");
		}
		// -檢查人員-
		gate.isoField[121] = commString.rpad(wp.colStr("ex_apr_user"), 10);;
		// -授權留言-
		String lsAuthRemark = "";
		lsAuthRemark = wp.colStr("ex_auth_remark");		
		gate.isoField[122] = lsAuthRemark;

		for (int ii = 0; ii < gate.isoField.length; ii++) {
			if (!empty(gate.isoField[ii])) {
				wp.log("%s[%s]", ii, gate.isoField[ii]);
			}
		}
		return true;
	}

	public void selectSpecData() {
		msgOK();
		if (wp.colEmpty("aa_spec_status") && wp.colEmpty("cc_spec_status"))
			return;

		String lsSql = "";
		if (wp.colEmpty("cc_spec_status") == false) {
			lsSql = "select spec_status, spec_del_date, spec_remark, spec_date, spec_user, spec_dept_no"
					+ ", uf_tt_spec_code(spec_status) as tt_spec_status" + " from cca_card_base" + " where card_no =?";
			setString2(1, wp.colStr("card_no"));
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("cc_spec_del_date", colStr("spec_del_date"));
				wp.colSet("cc_spec_remark", colStr("spec_remark"));
				wp.colSet("cc_spec_date", colStr("spec_date"));
				wp.colSet("cc_spec_user", colStr("spec_user"));
				wp.colSet("cc_spec_dept_no", colStr("spec_dept_no"));
				wp.colSet("cc_tt_spec_status", colStr("tt_spec_status"));
			}
		}
		if (wp.colEmpty("aa_spec_status") == false) {
			daoTid = "AA.";
			lsSql = "select spec_status" + ", spec_del_date, spec_remark, spec_date, spec_user"
					+ ", uf_tt_spec_code(spec_status) as tt_spec_status" + " from cca_card_acct"
					+ " where card_acct_idx in (select card_acct_idx from cca_card_base where card_no =?)";
			setString2(1, wp.colStr("card_no"));
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("aa_spec_del_date", colStr("AA.spec_del_date"));
				wp.colSet("aa_spec_remark", colStr("AA.spec_remark"));
				wp.colSet("aa_spec_date", colStr("AA.spec_date"));
				wp.colSet("aa_spec_user", colStr("AA.spec_user"));
				wp.colSet("aa_tt_spec_status", colStr("AA.tt_spec_status"));
			}
		}

	}

	public int consumeOKcheck() throws Exception {
		// -超額檢查人員-
		double lmRemainTot = wp.num("remain_tot");
		if (lmRemainTot >= 0)
			return 1;
		String lsAuthResp = wp.itemStr("ex_auth_resp");
		if (pos(",A,P,F", lsAuthResp) <= 0)
			return 1;
		String lsAprId = wp.itemStr("ex_apr_user");
		String lsAprHd = wp.itemStr("ex_apr_passwd");
		if (empty(lsAprId) || empty(lsAprHd)) {
			errmsg("[檢查人員,密碼] 不可空白");
			return -1;
		}

//		CcasLimit ooLimit = new CcasLimit();
//		ooLimit.setConn(wp);
//		double lmLimit = ooLimit.idnoLimitAll(wp.itemStr("card_no"));
//		lmLimit = lmLimit - lmRemainTot;
		double lmLimit = wp.itemNum("line_of_credit_amt");
		if(wp.colNum("tot_amt_month")>0)	lmLimit = wp.colNum("tot_amt_month");
		lmLimit = lmLimit + lmRemainTot*-1;

		ofcapp.EcsApprove ooAppr = new ofcapp.EcsApprove(wp);
		// -主管覆核-
		if (ooAppr.adjLimitApprove(wp.modPgm(), lsAprId, lsAprHd, lmLimit) != 1) {
			errmsg(ooAppr.getMesg());
			return -1;
		}

		return 1;
	}

}
