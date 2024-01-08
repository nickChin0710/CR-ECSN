package ccam01;
/** 
 * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
 * */

import java.net.ConnectException;

import bank.Auth.HSM.HsmUtil;
/**
 * 人工授權處理(auth_manual) 2020-0309 Alex open VD auth 2020-0107 Ru modify AJAX
 * 2019-1210 JH 安控 2019-0717 JH modify 2019-0610: JH p_seqno >>acno_p_seqno
 * V.2018-0903.JH 109-04-20 V1.00.00 Zhenwu Zhu updated for project coding
 * standar
 */
import busi.func.EcsComm;
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

public class Ccam1010 extends BaseAction {
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	String cardNo = "", authNo = "";
	Ccam1010Func func = new Ccam1010Func();
	busi.func.CrdFunc ffCrd = new busi.func.CrdFunc();

	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
		if (eqIgno(wp.requHtml, "ccam1010")) {
			switch (wp.buttonCode) {
			case "X": // 轉換顯示畫面 --
				strAction = "new";
				clearFunc();
				break;
			case "Q": // -查詢功能--
				queryRead();
				break;
			case "R":
				dataRead();
				break;
			case "A": // 新增--
			case "U": // 更新功能--
			case "D": // Force-referral --
				saveFunc();
				break;
			case "L": // 清畫面 --
				strAction = "";
				clearFunc();
				break;
			case "L2": // -多筆-
				strAction = "";
				clearFunc2();
				break;
			case "R1":
				selectCcaIdNote();
				break;
			case "AJAX":
				if ("1".equals(wp.getValue("ID_CODE"))) {
					wfItemChange();
				} else if ("2".equals(wp.getValue("ID_CODE"))) {
					wfAjaxMcc();
				}
				break;
			case "XXX":
				btnAddOn(false);				
				break;
			}
			
				
			return;
		}

		if (wp.requHtml.indexOf("m1010_confirm") > 0) {
			switch (wp.buttonCode) {
			case "C21": // 00.確認:[核准] --
				procFunc();
				break;
			case "S20": // 詳核--
				doQueryS20();
				break;
			case "XXX":
				btnAddOn(false);
				break;	
			}
			return;
		}

		// -ccaM1010_consume-
		if (wp.requHtml.indexOf("m1010_consume") > 0) {
			Ccam2090 oo2090 = new Ccam2090();
			oo2090.wp = wp;
			switch (wp.buttonCode) {
			case "C31": // -詳核:[核准/拒絶]-
				procFunc31();
				break;
			case "S01": // 前N消費
				oo2090.doQueryMonth();
				wp.colSet("xx_ccam1010", "|| 1==1");
				break;
			case "S02": // 前一年繳款評等
				oo2090.querySelectPaymentRate();
				wp.colSet("xx_ccam1010", "|| 1==1");
				break;
			case "S03": // 分期未POSTING--
				oo2090.doQueryPostingAmt();
				wp.colSet("xx_ccam1010", "|| 1==1");
				break;
			case "S05": // 近三次繳款方式-
				oo2090.doQueryPaymentRefund();
				wp.colSet("xx_ccam1010", "|| 1==1");
				break;
			case "S15":
				oo2090.doBillSendType();
				wp.colSet("xx_ccam1010", "|| 1==1");
				break;
			case "S16": // 授權第三人
				oo2090.doIdnoExt();
				wp.colSet("xx_ccam1010", "|| 1==1");
				break;
			case "S18": // 覆審結果
				oo2090.doQueryTrial();
				wp.colSet("xx_ccam1010", "|| 1==1");
				break;
			case "S22": // 查詢本卡交易明細
				doQueryS22();
				break;
			}
		}

//    if (eqIgno(wp.buttonCode, "X")) {
//      /* 轉換顯示畫面 */
//      strAction = "new";
//      clearFunc();
//    } else if (eqIgno(wp.buttonCode, "Q")) {
//      // -查詢功能--
//      if (eqIgno(wp.respHtml, "ccam2090_12_detl")) {
//        cmsQ0010Read();
//      } else {
//        strAction = "Q";
//        queryRead();
//      }
//    } else if (eqIgno(wp.buttonCode, "R")) {
//      // -card_no.onchange
//      strAction = "R";
//      dataRead();
//    } else if (eqIgno(wp.buttonCode, "R2")) {
//      // -card_no.onchange
//      strAction = "R2";
//      outgoingQuery();
//    } else if (eqIgno(wp.buttonCode, "R1")) {
//      // -card_no.onchange
//      strAction = "R1";
//      selectCcaIdNote();
//    } else if (eqIgno(wp.buttonCode, "A")) {
//      strAction = "A";
//      saveFunc();
//    } else if (eqIgno(wp.buttonCode, "U")) {
//      /* 更新功能 */
//      saveFunc();
//    } else if (eqIgno(wp.buttonCode, "D")) {
//      /* Force-referral */
//      saveFunc();
//    } else if (eqIgno(wp.buttonCode, "M")) {
//      /* 瀏覽功能 :skip-page */
//      queryRead();
//    } else if (eqIgno(wp.buttonCode, "L")) {
//      /* 清畫面 */
//      strAction = "";
//      clearFunc();
//    } else if (eqIgno(wp.buttonCode, "L2")) {
//      // -多筆-
//      strAction = "";
//      clearFunc2();
//    } else if (eqIgno(wp.buttonCode, "C21")) {
//      /* 00.確認:[核准] */
//      procFunc();
//    } else if (eqIgno(wp.buttonCode, "S22")) {
//      /* 00.確認:[核詳細資料] */
//      queryS22();
//    } else if (eqIgno(wp.buttonCode, "C31")) {
//      // -詳核:[核准]-
//      procFunc31();
//    } else if (eqIgno(wp.buttonCode, "C32")) {
//      // -詳核:[拒絕]-
//      procFunc32();
//    } else if (eqIgno(wp.buttonCode, "C33")) {
//      // -詳核:[斷線]-
//      procFunc33();
//    }
//    // 20200107 modify AJAX
//    else if (eqIgno(wp.buttonCode, "AJAX")) {
//      if ("1".equals(wp.getValue("ID_CODE"))) {
//        wfItemChange();
//      } else if ("2".equals(wp.getValue("ID_CODE"))) {
//        wfAjaxMcc();
//      }
//    }		
	}

	void clearFunc2() throws Exception {
		String lsMchtNo = wp.colStr("mcht_no");
		String lsMchtName = wp.colStr("mcht_name");
		String lsNccRiskLevel = wp.colStr("ncc_risk_level");
		String lsNccRiskCode = wp.colStr("ncc_risk_code");
		String lsMccCode = wp.colStr("mcc_code");
		String lsTtMccCode = wp.colStr("tt_mcc_code");

		clearFunc();
		wp.colSet("kk_action", "A");
		wp.colSet("mcht_no", lsMchtNo);
		wp.colSet("mcht_name", lsMchtName);
		wp.colSet("ncc_risk_level", lsNccRiskLevel);
		wp.colSet("ncc_risk_code", lsNccRiskCode);
		wp.colSet("mcc_code", lsMccCode);
		wp.colSet("tt_mcc_code", lsTtMccCode);
	}

	private void outgoingQuery() throws Exception {
		String lsCardNo = wp.itemStr2("card_no");
		Ccam2090 oo2090 = new Ccam2090();
		oo2090.wp = wp;
		oo2090.outgoingQuery(lsCardNo);
	}

	private void doQueryS20() throws Exception {
		String lsCardNo = wp.itemStr("card_no");
		Ccam2090 oo2090 = new Ccam2090();
		oo2090.wp = wp;
		if (eqIgno(wp.respHtml, "ccam1010_consume")) {
			wp.itemSet("kk_card_no", lsCardNo);
			oo2090.queryFunc();
			// 可用餘額-總結:[remain_tot,can_use_cash]
			double lmAmt = wp.colNum("remain_tot");
			lmAmt = lmAmt - wp.colNum("amt1");
			wp.colSet("remain_tot", lmAmt);
			// -預現可用-
			if (wp.colNum("remain_cash") > lmAmt) {
				wp.colSet("remain_cash", lmAmt);
			}
			// --子卡
			if (wp.colEq("son_card_flag", "Y")) {
				double lmSonAmt1 = wp.colNum("son_tot_amt_consume"), lmSonAmt2 = wp.colNum("son_remain_tot");
				lmSonAmt1 = lmSonAmt1 + wp.colNum("amt1");
				wp.colSet("son_tot_amt_consume", lmSonAmt1);
				lmSonAmt2 = lmSonAmt2 - wp.colNum("amt1");
				wp.colSet("son_remain_tot", lmSonAmt2);
			}
			// --
			if (lmAmt < 0) {
				wp.alertMesg("超過授權人員授權額度百分比(100%), 須檢查人員放行!");
			}
			wp.colSet("ex_auth_no", wp.itemStr("kk_auth_no"));
			//--get card_curr_code
			getCardCurrCode(lsCardNo);
		}
	}
	
	void getCardCurrCode(String aCardNo) throws Exception {
		String sql1 = "select curr_code as card_curr_code from crd_card where card_no = ? ";
		sqlSelect(sql1,new Object[] {aCardNo});
		if(sqlRowNum <=0) {
			wp.colSet("card_curr_code", "901");
			return ;
		}		
		String cardCurrCode = "";
		cardCurrCode = sqlStr("card_curr_code");
		if(cardCurrCode.isEmpty())
			cardCurrCode = "901";
		wp.colSet("card_curr_code", cardCurrCode);		
	}
	
	private void doQueryS22() throws Exception {
		String lsCardNo = wp.itemStr2("card_no");
		Ccam2090 oo2090 = new Ccam2090();
		oo2090.wp = wp;

		if (eqIgno(wp.respHtml, "ccam2090_4_detl")) {
			wp.itemSet("kk_card_no", lsCardNo);
			oo2090.doQueryAuthTxlog();
		} else if (eqIgno(wp.respHtml, "ccam2090_6_detl")) {
			// 查詢臨調內容
			String lsAcctIdx = wp.itemStr("card_acct_idx");
			oo2090.doQueryCardAcct();
		} else if (eqIgno(wp.respHtml, "ccam2090_8_detl")) {
			// 查詢風險性交易
			String lsIdPseqno = wp.itemStr("id_p_seqno");
			oo2090.doQueryRiskProd();
		} else if (eqIgno(wp.respHtml, "ccam2090_9_detl")) {
			// 查詢卡特指內容
			wp.itemSet("wk_card_no", wp.itemStr("card_no"));
			oo2090.doCardSpecStatus();
		} else if (eqIgno(wp.respHtml, "ccam2090_12_detl")) {
			// 歸戶餘額查詢
			wp.colSet("db_cname", wp.itemStr("chi_name"));
			oo2090.cmsQ0010Auth();
		} else if (eqIgno(wp.respHtml, "ccam2090_17_detl")) {
			// 備註記錄查詢
			oo2090.doQueryAuthRemarkLog();
		}
//    if (eqIgno(wp.respHtml, "ccam1010_consume")) {
//      // ls_card_no =wp.col_ss("card_no");
//      wp.itemSet("kk_card_no", ls_card_no);
//      oo_2090.queryFunc();
//    } else if (eqIgno(wp.respHtml, "ccam2090_4_detl")) {
//      // ls_card_no =wp.sss("wk_card_no");
//      wp.itemSet("kk_card_no", ls_card_no);
//      oo_2090.queryAuthTxlog();
//    } else if (eqIgno(wp.respHtml, "ccam2090_6_detl")) {
//      // 查詢臨調內容
//      String ls_acct_idx = wp.itemStr2("card_acct_idx");
//      oo_2090.querySelectCardAcct();
//    } else if (eqIgno(wp.respHtml, "ccam2090_8_detl")) {
//      // 查詢風險性交易
//      String ls_id_pseqno = wp.itemStr2("id_p_seqno");
//      oo_2090.querySelectRiskProd();
//    } else if (eqIgno(wp.respHtml, "ccam2090_9_detl")) {
//      // 查詢卡特指內容
//      wp.itemSet("wk_card_no", wp.itemStr2("card_no"));
//      oo_2090.readCardSpecStatus();
//    } else if (eqIgno(wp.respHtml, "ccam2090_12_detl")) {
//      // 歸戶餘額查詢
//      wp.colSet("db_cname", wp.itemStr2("chi_name"));
//      oo_2090.cmsQ0010Auth();
//    } else if (eqIgno(wp.respHtml, "ccam2090_17_detl")) {
//      // 備註記錄查詢
//      oo_2090.queryAuthRemarkLog();
//    }
	}

	@Override
	public void dddwSelect() {
		try {
			wp.optionKey = wp.colNvl("mcht_country", "TW");
			dddwList("dddw_country", "cca_country", "country_code", "country_remark",
					"where 1=1 and ccas_link_type = 'FISC' ");

			wp.optionKey = wp.colNvl("currency", "901");
			dddwList("dddw_currency", "ptr_curr_rate", "curr_code", "curr_code", "where 1=1");
			
			wp.optionKey = wp.colNvl("consume_curr", "901");
			dddwList("dddw_consume_curr", "ptr_currcode", "curr_code", "curr_code", "where 1=1");
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
	}

	@Override
	public void queryRead() throws Exception {
		cardNo = itemkk("kk_card_no");
		authNo = itemkk("kk_auth_no");
		if (empty(cardNo) || empty(authNo)) {
			alertErr2("卡號, 授權號碼: 不可空白");
			return;
		}

		wp.sqlCmd = "select hex(rowid) as rowid, mod_seqno" + ", tx_date, tx_time , trace_no " + ", card_no, auth_no "
				+ ", acct_type, acno_p_seqno, id_p_seqno, corp_p_seqno " + ", mcc_code     " + ", iso_resp_code"
				+ ", mcht_no, mcht_name, mcht_country "
				+ ", eff_date_end, user_expire_date, substr(eff_date_end,1,6) as eff_date " + ", tx_currency"
				+ ", ori_amt as amt1" + ", ori_amt " + ", nt_amt as amt2" + ", auth_remark as wk_auth_remark"
				+ ", auth_user   " + ", apr_user    " + ", logic_del   " + ", v_card_no   " + ", auth_seqno  "
				+ ", vdcard_flag " + ", card_acct_idx " + ", cacu_amount, mtch_flag , 'readOnly' as update_dis" 
				+ " from cca_auth_txlog"
				+ " where card_no =?" + " and auth_no =?";
		setString2(1, cardNo);
		setString2(2, authNo);

		pageSelect();
		if (sqlNotFind()) {
			alertErr("無該筆授權交易記錄資料 !");
			return;
		}

		String lsEffDate = wp.colStr("eff_date");
		wp.colSet("eff_date", commString.mid(lsEffDate, 4, 2) + commString.mid(lsEffDate, 2, 2));
		selectBinType();
		selectMchtBase(wp.colStr("mcht_no"), wp.colStr("bin_type"));

		String mccCode = getMccRemark(wp.colStr("mcc_code"));
		wp.colSet("tt_mcc_code", mccCode);

		// --
		if (wp.colEq("cacu_amount", "Y") == false) {
			alertMsg("CACU_AMOUNT = N,不可修改交易");
			return;
		}
		if (wp.colEq("mtch_flag", "N") == false) {
			alertMsg("已請款不可修改交易");
			return;
		}
	}

	String selectBinType() {
		String sql1 = "";
		if (wp.colEq("vdcard_flag", "D")) {
			sql1 = "select bin_type from dbc_card where card_no = ? ";
		} else {
			sql1 = "select bin_type from crd_card where card_no = ? ";
		}

		sqlSelect(sql1, new Object[] { wp.colStr("card_no") });

		if (sqlRowNum > 0) {
			wp.colSet("bin_type", sqlStr("bin_type"));
		}

		return "";
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		buttonOff("btnQuery_disable");

		cardNo = wp.itemStr2("kk_card_no");
		if (empty(cardNo)) {
			alertErr2("卡號: 不可空白");
			return;
		}

		selectCardNo(cardNo);
		if (rc != 1) {
			return;
		}

		wp.colSet("bin_type", sqlStr("bin_type"));
		wp.colSet("card_no", sqlStr("card_no"));
		wp.colSet("auth_no", sqlStr("yy_income_amt"));
		wp.colSet("chi_name", sqlStr("chi_name"));
		wp.colSet("id_no", sqlStr("id_no"));
		wp.colSet("birthday", sqlStr("birthday"));
		wp.colSet("group_code", sqlStr("group_code"));
		wp.colSet("acct_type", sqlStr("acct_type"));
		wp.colSet("acct_key", sqlStr("acct_key"));
		wp.colSet("wk_telno_h", sqlStr("wk_telno_h"));
		wp.colSet("wk_telno_o", sqlStr("wk_telno_o"));
		wp.colSet("wk_block_reason", sqlStr("wk_block_reason"));
		wp.colSet("wk_spec_status", sqlStr("wk_spec_status"));
		wp.colSet("card_spec_status", sqlStr("card_spec_status"));
		wp.colSet("wk_comp_name", sqlStr("company_name") + "-" + sqlStr("job_position"));
		wp.colSet("wk_bill_addr", sqlStr("wk_bill_addr"));
		wp.colSet("wk_auth_remark", sqlStr("wk_auth_remark"));
		wp.colSet("vip_code", sqlStr("vip_code"));
		wp.colSet("new_end_date", sqlStr("new_end_date"));
		wp.colSet("old_end_date", sqlStr("old_end_date"));
		wp.colSet("combo_indicator", sqlStr("combo_indicator"));
		wp.colSet("sup_flag", sqlStr("sup_flag"));
		wp.colSet("current_code", sqlStr("current_code"));
		wp.colSet("oppost_reason", sqlStr("oppost_reason"));
		wp.colSet("update_dis", "");
		wp.colSet("card_curr_code", sqlStr("card_curr_code"));
		// --
		String endDate = sqlStr("new_end_date");
		wp.colSet("wk_effdate_mmyy", commString.mid(endDate, 4, 2) + commString.mid(endDate, 2, 2));
		endDate = sqlStr("old_end_date");
		if (commString.strCompIngo(endDate, wp.sysDate) >= 0) {
			wp.colSet("old_effdate_mmyy", commString.mid(endDate, 4, 2) + commString.mid(endDate, 2, 2));
		}

		String lsCvv2 = sqlStr("trans_cvv2");
		lsCvv2 = ffCrd.transPasswd(1, lsCvv2);
		wp.colSet("trans_cvv2", lsCvv2);

		String sql1 = " select id_note " + " from cca_id_note " + " where id_no = ? " + " order by crt_date desc"
				+ commSqlStr.rownum(1);
		setString2(1, wp.colStr("id_no"));
		sqlSelect(sql1);
		if (sqlRowNum > 0) {
			wp.javascript("alert('特殊事項: " + sqlStr("id_note") + "')");
		}

		if (!wp.colEq("current_code", "0")) {
			String lsErr = "該卡已停掛！-" + wp.colStr("oppost_reason");
			sql1 = "select opp_remark from CCA_OPP_TYPE_REASON" + " where opp_status =?";
			setString2(1, wp.colStr("oppost_reason"));
			sqlSelect(sql1);
			if (sqlRowNum > 0) {
				lsErr += "-" + sqlStr("opp_remark");
			}
			alertErr2(lsErr);
		}

		btnQueryOn(true);
	}

	void dataCheck() throws Exception {
		// -no call autoAuthIC-
		if(wp.itemEq("current_code", "0") == false) {
			errmsg("非有效卡不可執行人工授權");
			return ;
		}
		
		
		// -特店ERROR-
		String sql1 = "select count(*) as xx_cnt from cca_mcht_bill" + " where mcht_no =?";
		setString2(1, wp.itemStr2("mcht_no"));
		sqlSelect(sql1);
		if (sqlRowNum <= 0 && sqlNum("xx_cnt") == 0) {
			this.setMesg("特店代號: 輸入錯誤[%s]", wp.colStr("mcht_no"));
			alertErr2("");
			return;
		}

		// -MCC-
		String lsMccCode = wp.itemStr("mcc_code");
		sql1 = "select count(*) as xx_cnt from cca_mcc_risk where mcc_code =?";
		sqlSelect(sql1, lsMccCode);
		if (sqlRowNum <= 0 || sqlInt("xx_cnt") <= 0) {
			alertErr2("MCC: 輸入錯誤[%s]", lsMccCode);
			return;
		}
		
		// -0522-
		double lmOriAmt = wp.num("ori_amt");
		if (eqIgno(strAction, "U")) {
			if (wp.num("amt1") > lmOriAmt) {
				alertErr2("輸入更正金額超過原消費金額!!!");
				return;
			}
		}
		
		String lsCvv2 = wp.itemStr("cvc2");
		String cardNo = wp.itemStr("card_no");
		String binType = wp.itemStr("bin_type");		
//		String newEndDate = wp.itemStr("new_end_date");
//		String effdateyymm = commString.mid(newEndDate, 2, 2) + commString.mid(newEndDate, 4, 2);
		String effdateyymm = commString.mid(wp.itemStr("eff_date"),2,2)+commString.mid(wp.itemStr("eff_date"),0,2);
		String cvkA = "", cvkB = "";
		
		if(empty(lsCvv2)){
			return;
		}
		
		// -CVV2-
		String sqlSelect = "select hsm_ip_addr1,hsm_port1,hsm_ip_addr2,hsm_port2,hsm_ip_addr3 ,hsm_port3 "
				+ " ,visa_cvka,visa_cvka_chk,visa_cvkb,visa_cvkb_chk "
				+ " ,master_cvka,master_cvka_chk,master_cvkb,master_cvkb_chk "
				+ " ,jcb_cvka,jcb_cvka_chk,jcb_cvkb,jcb_cvkb_chk "
				+ " from ptr_hsm_keys where hsm_keys_org = '00000000' ";
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			alertErr2("未設定信用卡產驗基碼參數檔 ,keys=00000000");
			return;
		}

		String hsmIpAddr1 = sqlStr("hsm_ip_addr1");
		int hsmPort1 = sqlInt("hsm_port1");
		String hsmIpAddr2 = sqlStr("hsm_ip_addr2");
		int hsmPort2 = sqlInt("hsm_port2");
		String hsmIpAddr3 = sqlStr("hsm_ip_addr3");
		int hsmPort3 = sqlInt("hsm_port3");
		String visaCvka = sqlStr("visa_cvka");
		String visaCvkb = sqlStr("visa_cvkb");
		String masterCvka = sqlStr("master_cvka");
		String masterCvkb = sqlStr("master_cvkb");
		String jcbCvka = sqlStr("jcb_cvka");
		String jcbCvkb = sqlStr("jcb_cvkb");

		switch (binType) {
		case "V":
			cvkA = visaCvka;
			cvkB = visaCvkb;
			break;
		case "M":
			cvkA = masterCvka;
			cvkB = masterCvkb;
			break;
		case "J":
			cvkA = jcbCvka;
			cvkB = jcbCvkb;
			break;
		}
		
		String dbSwitch2Dr = TarokoParm.getInstance().getDbSwitch2Dr();
		String chkCode = "";
		if("Y".equals(dbSwitch2Dr)) {
			HsmUtil hsmCy = new HsmUtil(hsmIpAddr3, hsmPort3);		
			try {
				chkCode = hsmCy.hsmCommandCY(cardNo, effdateyymm, "000", lsCvv2, cvkA, cvkB);
			} catch (ConnectException ex1) {
				alertErr2("hsmCommand connect err");
				return;
			}
		}	else	{
			HsmUtil hsmCy = new HsmUtil(hsmIpAddr1, hsmPort1);		
			try {
				chkCode = hsmCy.hsmCommandCY(cardNo, effdateyymm, "000", lsCvv2, cvkA, cvkB);
			} catch (ConnectException ex1) {
				hsmCy = new HsmUtil(hsmIpAddr2, hsmPort2);
				try {
					chkCode = hsmCy.hsmCommandCY(cardNo, effdateyymm, "000", lsCvv2, cvkA, cvkB);
				} catch (ConnectException ex2) {
					alertErr2("hsmCommand connect err");
					return;
				}
			}
		}
		
		

		if (!chkCode.equals("00")) {
			alertErr2("CVV2 輸入錯誤[%s]", lsCvv2);
			return;
		}

	}

	@Override
	public void saveFunc() throws Exception {
		func.setConn(wp);

		dataCheck();
		if (rc != 1)
			return;

		rc = func.dbSave(strAction);
		if (rc != 1) {
			alertErr2(func.getMsg());
		}

		return;
	}

	@Override
	public void procFunc() throws Exception {
		func.setConn(wp);

		rc = func.dataProc();
		if (rc != 1) {
			alertErr2(func.getMsg());
			return;
		}
		// -TTT-
		if (wp.colEmpty("auth_no") && wp.colEq("iso_resp_code", "00")) {
			wp.colSet("auth_no", "TT1234");
		}

		// alert_msg("授權成功, 授權碼="+wp.col_ss("auth_no"));
//		wp.colSet("open_1010", "|| 1==1");
		//--反灰新增按鈕
		btnAddOn(false);
	}

	void procFunc31() throws Exception {
		// -卡戶五筆授權-
		wp.listCount[0] = wp.itemRows("ser_num");

		// -詳核:[核准]-
		String lsAuth = wp.itemStr2("ex_auth_resp");
		if (commString.strIn2(lsAuth, ",A,F,R") == false) {
			alertErr2("授權結果應為: (A,F,R)");
			return;
		}

		// --
		func.setConn(wp);

		// --等於 'R','C','U' 不給授權碼--
		if (commString.strIn2(lsAuth, ",R,C,U") == false) {
			if (func.consumeOKcheck() == -1) {
				alertErr2(func.getMsg());
				return;
			}
			// IF is_flag <> '0' THEN //須檢查人員放行
			// IF em_3.text = '' THEN
			// CHOOSE CASE is_flag
			// CASE '1'
			// f_show_msgbox("警告訊息","超過授權人員單筆授權額度,須檢查人員放行 !")
			// em_3.SetFocus()
			// RETURN FALSE
			// CASE '2'
			// f_show_msgbox("警告訊息","超過授權人員累積消費授權金額,須檢查人員放行 !")
			// em_3.SetFocus()
			// RETURN FALSE
			// CASE '3'
			// f_show_msgbox("警告訊息","超過授權人員(主管)授權最高額度不可放行 !")
			// em_3.SetFocus()
			// RETURN FALSE
			// CASE '4'
			// f_show_msgbox("警告訊息"," !")
			// em_3.SetFocus()
			// RETURN FALSE
			// END CHOOSE
			// END IF
			// IF sle_pwd.text = '' THEN
			// f_show_msgbox("警告訊息","請輸入檢查人員放行密碼 !")
			// sle_pwd.SetFocus()
			// RETURN FALSE
			// END IF
			// IF sle_pwd.text = '*' THEN
			// f_show_msgbox("警告訊息","輸入檢查人員密碼錯誤~ !")
			// sle_pwd.SetFocus()
			// RETURN FALSE
			// END IF
			// END IF
		} else {
			wp.itemSet2("ex_auth_no", "******");
		}

		String lsWkFlag = wp.itemStr("wk_flag");
		String lsBit39 = "";

		if (eqIgno(lsAuth, "A")) {
			if (eqIgno(lsWkFlag, "T")) {
				if (wp.itemEq("bit39_adj_code", "AF")) // 第一畫面時iso_rsp_code<>'41'之拒絕授權
					lsBit39 = "FA"; // Auto decline approve
				else
					lsBit39 = "PA"; // Auto pickup approve
			} else if (eqIgno(lsWkFlag, "H")) {
				lsBit39 = "HA"; // auto approve approve (A)
			} else
				lsBit39 = "RA"; // referral approve (A)
		} else if (eqIgno(lsAuth, "P")) {
			if (eqIgno(lsWkFlag, "T")) {
				if (wp.itemEq("bit39_adj_code", "AF")) // 第一畫面時iso_rsp_code<>'41'之拒絕授權
					lsBit39 = "FP"; // Auto decline approve
				else
					lsBit39 = "PP"; // Auto pickup approve
			} else if (eqIgno(lsWkFlag, "H")) {
				lsBit39 = "HP"; // auto approve approve (A)
			} else
				lsBit39 = "RP"; // referral approve (A)
		} else if (eqIgno(lsAuth, "F")) {
			if (eqIgno(lsWkFlag, "T")) {
				if (wp.itemEq("bit39_adj_code", "AF")) // 第一畫面時iso_rsp_code<>'41'之拒絕授權
					lsBit39 = "FF"; // Auto decline approve
				else
					lsBit39 = "PF"; // Auto pickup approve
			} else if (eqIgno(lsWkFlag, "H")) {
				lsBit39 = "HF"; // auto approve approve (A)
			} else
				lsBit39 = "RF"; // referral approve (A)
		} else if (eqIgno(lsAuth, "R")) {
			if (eqIgno(lsWkFlag, "T")) {
				if (wp.itemEq("bit39_adj_code", "AF")) // 第一畫面時iso_rsp_code<>'41'之拒絕授權
					lsBit39 = "FR"; // Auto decline approve
				else
					lsBit39 = "PR"; // Auto pickup approve
			} else if (eqIgno(lsWkFlag, "H")) {
				lsBit39 = "HR"; // auto approve approve (A)
			} else
				lsBit39 = "RR"; // referral approve (A)
		} else if (eqIgno(lsAuth, "C")) {
			if (eqIgno(lsWkFlag, "T")) {
				if (wp.itemEq("bit39_adj_code", "AF")) // 第一畫面時iso_rsp_code<>'41'之拒絕授權
					lsBit39 = "FC"; // Auto decline approve
				else
					lsBit39 = "PC"; // Auto pickup approve
			} else if (eqIgno(lsWkFlag, "H")) {
				lsBit39 = "HC"; // auto approve approve (A)
			} else
				lsBit39 = "RC"; // referral approve (A)
		} else if (eqIgno(lsAuth, "U")) {
			if (eqIgno(lsWkFlag, "T")) {
				if (wp.itemEq("bit39_adj_code", "AF")) // 第一畫面時iso_rsp_code<>'41'之拒絕授權
					lsBit39 = "FU"; // Auto decline approve
				else
					lsBit39 = "PU"; // Auto pickup approve
			} else if (eqIgno(lsWkFlag, "H")) {
				lsBit39 = "HU"; // auto approve approve (A)
			} else
				lsBit39 = "RU"; // referral approve (A)
		}
		wp.itemSet("bit39_resp_code", lsBit39);

		// ** 拒絕交易,寫入AUTH_TXLOG and STA_TX_UNORMAL **
		int liRc = 0;
		if (commString.strIn2(lsAuth, ",C,R,U")) {
			liRc = func.consumeInsertAuthTxlog();
			sqlCommit(rc);
			if (liRc == -1) {
				alertErr2(func.mesg());
				return;
			}
			alertMsg("未核准交易已寫入交易記錄檔內, 授權交易處理結束(拒絕交易[" + lsAuth + "]) !");
		} else {
			// -送授權-
			liRc = func.consumeAuthOk();
			if (liRc == -1) {
				alertErr2(func.getMsg());
				return;
			}
			alertMsg("授權交易處理結束[" + lsAuth + "] !");
		}

		// btn_appr_disable,btn_rejt_disable,btn_break_disable
		this.buttonOff("btn_appr_disable");
		// this.button_off("btn_rejt_disable");
		// this.button_off("btn_break_disable");

	}

	void procFunc32() throws Exception {
		// -詳核:[拒絕]-
	}

	void procFunc33() throws Exception {
		// -詳核:[斷線]-
	}

	@Override
	public void initButton() {
		if (eqIgno(wp.respHtml, "ccam1010")) {
			btnOnAud(false, false, false);
			if (wp.colEmpty("rowid")) {
				btnAddOn(wp.autUpdate());
			} else {
				btnUpdateOn(wp.autUpdate());
			}
			
			if(wp.colEmpty("auth_no") == false) {
				btnAddOn(false);
			}			
		}
		if (wp.respHtml.indexOf("1010_confirm") > 0) {
			btnAddOn(wp.colEq("iso_resp_code", "00"));
			// -spec-data-
			wp.colSet("cc_spec_status", wp.itemStr2("card_spec_status"));
			wp.colSet("aa_spec_status", wp.itemStr2("wk_spec_status"));
			func.setConn(wp);
			func.selectSpecData();
		}

	}

	@Override
	public void initPage() {
		if (eqIgno(wp.respHtml, "ccam1010")) {
			wp.colSet("mcht_country", "TW");
		}
	}

	public void wfAjaxKey(TarokoCommon wr) throws Exception {
		super.wp = wr;

		// String ls_winid =

		selectCardNo(wp.itemStr("ax_key"));
		if (rc != 1) {
			wp.addJSON("card_no", "");
			wp.addJSON("auth_no", "");
			wp.addJSON("chi_name", "");
			wp.addJSON("id_no", "");
			wp.addJSON("birthday", "");
			wp.addJSON("group_code", "");
			wp.addJSON("acct_type", "");
			wp.addJSON("acct_key", "");
			wp.addJSON("wk_telno_h", "");
			wp.addJSON("wk_telno_o", "");
			wp.addJSON("wk_block_reason", "");
			wp.addJSON("wk_spec_status", "");
			wp.addJSON("wk_comp_name", "");
			wp.addJSON("wk_bill_addr", "");
			wp.addJSON("wk_auth_remark", "");
			wp.addJSON("vip_code", "");
			wp.addJSON("new_end_date", "");
			wp.addJSON("old_end_date", "");
			wp.addJSON("combo_indicator", "");
			wp.addJSON("sup_flag", "");
			wp.addJSON("wk_effdate_mmyy", "");

			return;
		}
		wp.addJSON("card_no", sqlStr("card_no"));
		wp.addJSON("auth_no", sqlStr("yy_income_amt"));
		wp.addJSON("chi_name", sqlStr("chi_name"));
		wp.addJSON("id_no", sqlStr("id_no"));
		wp.addJSON("birthday", sqlStr("birthday"));
		wp.addJSON("group_code", sqlStr("group_code"));
		wp.addJSON("acct_type", sqlStr("acct_type"));
		wp.addJSON("acct_key", sqlStr("acct_key"));
		wp.addJSON("wk_telno_h", sqlStr("wk_telno_h"));
		wp.addJSON("wk_telno_o", sqlStr("wk_telno_o"));
		wp.addJSON("wk_block_reason", sqlStr("wk_block_reason"));
		wp.addJSON("wk_spec_status", sqlStr("wk_spec_status"));
		wp.addJSON("wk_comp_name", sqlStr("company_name") + "-" + sqlStr("job_position"));
		wp.addJSON("wk_bill_addr", sqlStr("wk_bill_addr"));
		wp.addJSON("wk_auth_remark", sqlStr("wk_auth_remark"));
		wp.addJSON("vip_code", sqlStr("vip_code"));
		wp.addJSON("new_end_date", sqlStr("new_end_date"));
		wp.addJSON("old_end_date", sqlStr("old_end_date"));
		wp.addJSON("combo_indicator", sqlStr("combo_indicator"));
		wp.addJSON("sup_flag", sqlStr("sup_flag"));
		String endDate = sqlStr("new_end_date");
		wp.addJSON("wk_effdate_mmyy", commString.mid(endDate, 4, 2) + commString.mid(endDate, 2, 2));

	}
	
	boolean checkPurchaseCard(String lsGroupCode) {
		String sql1 = "select purchase_card_flag from ptr_group_code where 1=1 and group_code = ? ";
		sqlSelect(sql1,new Object[] {lsGroupCode});
		
		if(sqlRowNum <=0) {
			alertErr2("查無此卡團體代號參數");
			return false;
		}
		
		if(eqIgno(sqlStr("purchase_card_flag"),"Y"))	return true ;		
		
		return false ;
	}
	
	void selectCardNo(String aCardNo) {
		ffCrd.setConn(wp);
		boolean lbDebit = ffCrd.isDebitcard(aCardNo);
		
		String lsSql = "select A.card_no, A.acct_type ," + " A.group_code, " + " A.id_p_seqno, "
				+ " A.major_id_p_seqno, " + " A.acno_p_seqno, " + " A.corp_p_seqno ," + " A.new_end_date ,"
				+ " A.old_end_date , " + " A.sup_flag , " + " A.combo_indicator, "
				+ " A.current_code, A.oppost_reason, A.trans_cvv2," + " B.chi_name, " + " B.id_no, " + " B.birthday, "
				+ " B.home_area_code1||'-'||B.home_tel_no1||'-'||B.home_tel_ext1 as wk_telno_h, "
				+ " B.office_area_code1||'-'||B.office_tel_no1||'-'||B.office_tel_ext1 as wk_telno_o, "
				+ " B.company_name, " + " B.job_position "
				+ ", uf_spec_status(C.spec_status,C.spec_del_date) as card_spec_status"
				+ ", C.spec_del_date as card_spec_del_date , A.bin_type , uf_nvl(A.curr_code,'901') as card_curr_code "
				+ " from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno "
				+ " left join cca_card_base C on C.card_no=A.card_no" + " where A.card_no =?";
		if (lbDebit) {
			lsSql = "select A.card_no, A.acct_type , " + " A.group_code, " + " A.id_p_seqno, " + " A.major_id_p_seqno, "
					+ " A.p_seqno as acno_p_seqno, " + " A.corp_p_seqno ," + " A.new_end_date ," + " A.old_end_date , "
					+ " A.sup_flag , " + " '' as combo_indicator, " + " A.current_code, A.oppost_reason, A.trans_cvv2,"
					+ " B.chi_name, " + " B.id_no, " + " B.birthday, "
					+ " B.home_area_code1||'-'||B.home_tel_no1||'-'||B.home_tel_ext1 as wk_telno_h, "
					+ " B.office_area_code1||'-'||B.office_tel_no1||'-'||B.office_tel_ext1 as wk_telno_o, "
					+ " B.company_name, " + " B.job_position "
					+ ", uf_spec_status(C.spec_status,C.spec_del_date) as card_spec_status"
					+ ", C.spec_del_date as card_spec_del_date , A.bin_type , '' as card_curr_code "
					+ " from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno "
					+ " left join cca_card_base C on C.card_no=A.card_no" + " where A.card_no =?";
		}
		setString2(1, aCardNo);
		this.sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			alertErr2("此卡號查無資料");
			return;
		}
		boolean lbPurchaseCard = checkPurchaseCard(sqlStr("group_code"));
		if (lbDebit) {
			wp.colSet("debit_flag", "Y");
			wp.colSet("card_curr_code", "901");
			// --暫時打開 之後要封起來 2020/08/25
//			 alertErr("DEBIT卡不可做人工授權");
//			 return;
		} else
			wp.colSet("debit_flag", "N");

		if (lbPurchaseCard) {
			alertErr("採購卡不可做人工授權");
			return;
		}

		String lsSql2 = " select "
				+ " A.block_reason1||','||A.block_reason2||','||A.block_reason3||','||A.block_reason4||','||A.block_reason5 as wk_block_reason ,"
				+ " uf_spec_status(A.spec_status,A.spec_del_date) as wk_spec_status,"
				+ " A.auth_remark as wk_auth_remark ," + " A.card_acct_idx " + ", B.bill_sending_zip as wk_bill_zip"
				+ ", B.bill_sending_addr1||B.bill_sending_addr2||B.bill_sending_addr3||B.bill_sending_addr4||B.bill_sending_addr5 as wk_bill_addr"
				+ ", B.vip_code, B.acct_type, B.acct_key";
		if (lbDebit) {
			lsSql2 += " from cca_card_acct A join dba_acno B on A.acno_p_seqno =B.p_seqno and A.debit_flag = 'Y'"
					+ " where B.p_seqno = ? ";
		} else {
			lsSql2 += " from cca_card_acct A join act_acno B on A.acno_p_seqno =B.acno_p_seqno and A.debit_flag<>'Y'"
					+ " where B.acno_p_seqno = ? ";
		}
		setString2(1, sqlStr("acno_p_seqno"));
		sqlSelect(lsSql2);
		if (sqlRowNum <= 0) {
			alertErr2("查無卡人資料");
		}
		return;
	}

	void selectCcaIdNote() throws Exception {
		String lsIdno = wp.itemStr2("id_no");
		if (empty(lsIdno)) {
			alertErr2("身分證ID: 不可空白");
			return;
		}

		wp.sqlCmd = "select id_note, close_date" + ", crt_date, crt_user" + " from cca_id_note" + " where id_no =?"
				+ " order by crt_date";
		setString2(1, lsIdno);
		pageQuery();
		wp.setListCount(0);
		if (sqlRowNum <= 0) {
			errmsg("查無資料");
		}

	}

	// public void wf_ajax_mcht(TarokoCommon wr) throws Exception {
	// super.wp = wr;
	//
	// // String ls_winid =
	//
	// select_mcht_base(wp.item_ss("ax_key"));
	// if (rc != 1) {
	// wp.addJSON("mcht_name", "");
	// wp.addJSON("ncc_risk_code", "");
	// wp.addJSON("ncc_risk_level", "");
	// wp.addJSON("mcc_code", "");
	// wp.addJSON("tt_mcc_code","");
	// return;
	// }
	// wp.addJSON("mcht_name", sql_ss("mcht_name"));
	// wp.addJSON("ncc_risk_code", sql_ss("mcht_risk_code"));
	// wp.addJSON("ncc_risk_level", sql_ss("ncc_risk_level"));
	// wp.addJSON("mcc_code", sql_ss("mcc_code"));
	// wp.addJSON("tt_mcc_code", sql_ss("tt_mcc_code"));
	// }

	// public void wf_calc_amt(TarokoCommon wr) throws Exception {
	// super.wp =wr;
	// String ls_curr_code =wp.sss("ax_currency");
	// double lm_amt1 =wp.item_num("ax_amt1");
	//
	// busi.func.EcsFunc func=new busi.func.EcsFunc();
	// func.setConn(wp);
	// double lm_rate =func.get_Curr_rate(ls_curr_code);
	// if (lm_rate ==0) {
	// wp.addJSON("wk_amt2","0");
	// return;
	// }
	//
	// double lm_amt2 =commString.num_Scale(lm_amt1 * lm_rate,0);
	//
	// wp.addJSON("wk_amt2",""+commString.num_format(lm_amt2,"#########0"));
	// }

	void selectMchtBase(String aMchtNo, String aBinType) throws Exception {
		String lsSql = " select zip_code, " + " mcht_name, " + " mcc_code, mcht_eng_name , " + " ''  as mcht_risk_code,"
				+ " uf_tt_mcc_code(mcc_code) as tt_mcc_code, acq_bank_id " + " from cca_mcht_bill "
				+ " where mcht_no =? and bin_type = ? ";
		setString2(1, aMchtNo);
		setString2(2, aBinType);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			lsSql = " select zip_code, " + " mcht_name, " + " mcc_code, mcht_eng_name , " + " ''  as mcht_risk_code,"
					+ " uf_tt_mcc_code(mcc_code) as tt_mcc_code, acq_bank_id " + " from cca_mcht_bill "
					+ " where mcht_no =? ";
			setString2(1, aMchtNo);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				// --找不到仍然可以人工授權 , 特店資料帶Ｕｎｋｎｏｗｎ
				rc = -1;
				return;
			}
		}
//		wp.colSet("ncc_risk_code", sqlStr("ncc_risk_code"));

		lsSql = "select mcht_risk_code" // , risk_start_date, risk_end_date"
				+ " from cca_mcht_risk" + " where mcht_no =?"
				+ " and to_char(sysdate,'yyyymmdd') between risk_start_date and uf_nvl(risk_end_date,'20991231')";
		setString2(1, aMchtNo);
		sqlSelect(lsSql);
		wp.colSet("ncc_risk_level", "");
		if (sqlRowNum > 0) {
			wp.colSet("ncc_risk_level", sqlStr("mcht_risk_code"));
		}

//    wp.log("risk_level[%s], risk_code[%s]", wp.colStr("ncc_risk_level"),
//        wp.colStr("ncc_risk_code"));

		return;
	}

	// 20200107 modify AJAX
	public void wfItemChange() throws Exception {
		// super.wp = wr;
		msgOK();

		wp.colSet("kk_card_no", wp.itemStr2("ax_card_no"));
		String lsCol = wp.itemStr2("ax_col");
		String lsData = wp.itemStr2("ax_data");
		if (eqIgno(lsCol, "eff_date")) {
			itemChangeEffDate(lsData);
		} else if (eqIgno(lsCol, "mcht_no")) {
			String lsMchtNo = wp.itemStr2("ax_data");
			String lsBinType = wp.itemStr2("ax_data2");
			selectMchtBase(lsMchtNo, lsBinType);
			if (rc == 1) {
				wp.addJSON("mcht_name", sqlStr("mcht_name"));
				wp.addJSON("mcht_eng_name", sqlStr("mcht_eng_name"));
				wp.addJSON("ncc_risk_code", wp.colStr("ncc_risk_code"));
				wp.addJSON("ncc_risk_level", wp.colStr("ncc_risk_level"));
				wp.addJSON("mcc_code", sqlStr("mcc_code"));
				wp.addJSON("tt_mcc_code", sqlStr("tt_mcc_code"));
				wp.addJSON("acq_bank_id", sqlStr("acq_bank_id"));
			} else {
				wp.addJSON("mcht_name", "Ｕｎｋｎｏｗｎ");
				wp.addJSON("ncc_risk_code", "");
				wp.addJSON("ncc_risk_level", "");
				wp.addJSON("mcc_code", "");
				wp.addJSON("tt_mcc_code", "");
//				wp.addJSON("mcht_eng_name", "Ｕｎｋｎｏｗｎ");
				wp.addJSON("mcht_eng_name", "Unknown");
				wp.addJSON("acq_bank_id", "");
			}
		} else if (commString.strIn2(lsCol, ",currency,amt1")) {
			itemChangeCurrency();
		}
	}

	void itemChangeEffDate(String aEffDate) throws Exception {
		if (empty(aEffDate) || wp.colEmpty("kk_card_no")) {
			wp.addJSON("ax_rc", "1");
			wp.addJSON("ax_errmsg", "卡號,有效日期: 不可空白");
			return;
		}

		String sql1 = "select new_beg_date, new_end_date" + ", old_beg_date, old_end_date" + " from crd_card"
				+ " where card_no =?";
		setString2(1, wp.colStr("kk_card_no"));
		sqlSelect(sql1);
		if (sqlRowNum <= 0) {
			wp.addJSON("ax_rc", "1");
			wp.addJSON("ax_errmsg", "卡號: 輸入錯誤");
			return;
		}
		String lsEffDate = "20" + commString.mid(aEffDate, 2, 2) + commString.mid(aEffDate, 0, 2);
		String lsNewEdate = commString.left(sqlStr("new_end_date"), 6);
		String lsOldEdate = commString.left(sqlStr("old_end_date"), 6);
		if (eqIgno(lsEffDate, lsNewEdate) || eqIgno(lsEffDate, lsOldEdate)) {
			wp.addJSON("ax_rc", "0");
			wp.addJSON("ax_errmsg", "");
			return;
		}

		String lsNewSdate = commString.left(sqlStr("new_beg_date"), 6);
		String lsOldSdate = commString.left(sqlStr("old_beg_date"), 6);
		wp.addJSON("ax_rc", "1");
		if (commString.strComp(lsEffDate, lsOldEdate) < 0) {
			alertErr2("有效日期輸入錯誤\\n" + "應為(新卡=" + strMid(lsNewSdate, 4, 2) + "/" + strMid(lsNewSdate, 0, 4) + " - "
					+ strMid(lsNewEdate, 4, 2) + "/" + strMid(lsNewEdate, 0, 4) + ")" + "\\n" + "　　(舊卡="
					+ strMid(lsOldSdate, 4, 2) + "/" + strMid(lsOldSdate, 0, 4) + " - " + strMid(lsOldEdate, 4, 2) + "/"
					+ strMid(lsOldEdate, 0, 4) + ")");
		} else {
			alertErr2("有效日期輸入錯誤" + "\\n" + "(卡片效期=" + strMid(lsNewSdate, 4, 2) + "/" + strMid(lsNewSdate, 0, 4) + " - "
					+ strMid(lsNewEdate, 4, 2) + "/" + strMid(lsNewEdate, 0, 4) + ")");
		}
	}

	void itemChangeCurrency() throws Exception {
		String lsCurrCode = wp.itemStr2("ax_currency");
		double lmAmt1 = wp.itemNum("ax_amt1");

		EcsComm func = new EcsComm();
		func.setConn(wp);
		double lmRate = func.getCurrRate(lsCurrCode);
		if (lmRate == 0) {
			wp.addJSON("wk_amt2", "0");
			return;
		}

		double lmAmt2 = commString.numScale(lmAmt1 * lmRate, 0);
		wp.addJSON("wk_amt2", "" + commString.numFormat(lmAmt2, "#########0"));
	}

	// 20200107 modify AJAX
	public void wfAjaxMcc() throws Exception {
		// super.wp = wr;

		// String ls_winid =
		selectData(wp.itemStr("ax_mcc_code"));
		if (rc != 1) {
			wp.addJSON("tt_mcc_code", "");
			return;
		}
		wp.addJSON("tt_mcc_code", sqlStr("mcc_remark"));
	}

	void selectData(String data) {
		String sql1 = " select " + " mcc_remark " + " from cca_mcc_risk " + " where mcc_code = ? ";

		sqlSelect(sql1, new Object[] { data });

		if (sqlRowNum <= 0) {
			return;
		}
	}

	String getMccRemark(String data) throws Exception {
		String sql1 = " select " + " mcc_remark " + " from cca_mcc_risk " + " where mcc_code = ? ";

		sqlSelect(sql1, new Object[] { data });

		if (sqlRowNum <= 0) {
			return "";
		}
		return sqlStr("mcc_remark");
	}

}
