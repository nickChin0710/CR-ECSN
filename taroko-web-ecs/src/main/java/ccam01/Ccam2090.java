package ccam01;
/**ccam2090 卡戶/卡片基本資料查詢
 * 2020-0107:  Ru    modify AJAX、
 * 109-04-20  shiyuqi       updated for project coding standard 
 * 109-11-19   JustinWu     ignore Cmsm6020 for phase 1
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
 **/

import busi.func.OutgoingOppo;
//import cmsm02.Cmsm6020; //20201119 ignore Cmsm6020 for phase 1
import ofcapp.BaseAction;
import rskm02.Rskm0930;
import taroko.com.TarokoCommon;

public class Ccam2090 extends BaseAction {
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	Ccam2090Func func = new Ccam2090Func();

	String isDebitFlag = "", isCardNo = "", isNote = "";
	boolean ibDebit = false;

	@Override
	public void userAction() throws Exception {
		wp.pgmVersion("V.19-0611");
		strAction = wp.buttonCode;
		switch (wp.buttonCode) {
		case "X": // 轉換顯示畫面
			strAction = "new";
			clearFunc();
			break;
		case "Q": // 查詢功能
			if (wp.itemEq("pageType", "detl_cmsq0010")) {
				cmsQ0010Read();
			} else
				queryFunc();
			break;
		case "R": // -資料讀取-
			dataRead();
			break;
		case "R2": // -outgoing查詢-Read IDNO_EXT error
			outgoingQuery(wp.itemStr("card_no"));
			break;
		case "A": // 新增功能
			saveFunc();
			break;
		case "U": // 更新功能
			saveFunc();
			break;
		case "U1": // 更新功能
			updateMcode();
			break;
		case "U2": // 更新功能
			updateNote();
			break;
		case "M": // 瀏覽功能 :skip-page
			queryRead();
			break;
		case "S": // 動態查詢
			querySelect();
			break;
		case "S1": // 動態查詢
			doQueryMonth();
			break;
		case "S2": // 動態查詢
			querySelectPaymentRate();
			break;
		case "S3": // 動態查詢
			doQueryPostingAmt();
			break;
		case "S4": // 動態查詢
			doQueryAuthTxlog();
			break;
		case "S5": // 近三次繳款方式--
			doQueryPaymentRefund();
			break;
		case "S6": // 查詢臨調內容
			doQueryCardAcct();
			break;
		case "S8": // 查詢風險性交易
			doQueryRiskProd();
			break;
		case "S9": // 查詢卡特指內容
			doCardSpecStatus();
			break;
		case "S10":
			func.setConn(wp);
			if (func.readChgMcode() == -1) {
				alertErr2("查無資料");
				return;
			}
			break;
		case "S11": // --特殊事項
			querySelectNote();
			break;
		case "S12":
			cmsQ0010Auth();
			break;
// 20201119 ignore Cmsm6020 for phase 1
//		case "S13":
//			doOpenCmsm6020();
//			break;
//		case "A13": // 案件存檔
//			doSaveCmsm6020();
//			break;
		case "S14":
			doOpenRskm0930();
			break;
		case "S15":
			doBillSendType();
			break;
		case "S16": // 授權第三人
			doIdnoExt();
			break;
		case "S17": // -備註記錄查詢-
			doQueryAuthRemarkLog();
			break;
		case "S18": // -覆審總評分數-
			doQueryTrial();
			break;
// 20201119 ignore Cmsm6020 for phase 1
//		case "S19": // -案件LOG-
//			doQueryCmsm6020();
//			break;
		case "L": // 清畫面
			strAction = "";
			clearFunc();
			break;
		case "C": // -資料處理-
			procFunc();
			break;
		case "AJAX":
			if ("1".equals(wp.getValue("ID_CODE"))) {
		        wfAjaxIdno();
		    }
		}
	}
	
// 20201119 ignore Cmsm6020 for phase 1
//	void doSaveCmsm6020() throws Exception {
//		cmsm02.Cmsm6020 oo6020 = new Cmsm6020();
//		oo6020.wp = wp;
//		oo6020.strAction = "A1";
//		oo6020.saveFunc();
//	}
//
//	void doOpenCmsm6020() throws Exception {
//		if (userAuthRun("cmsm6020") == false) {
//			alertErr2("没有權限使用 [cmsm602s0]");
//			return;
//		}
//
//		String lsCardNo = wp.itemStr("wk_card_no");
//		String lsDebit = wp.itemStr("debit_flag");
//		wp.itemSet("data_k1", lsCardNo);
//		wp.itemSet("data_k2", lsDebit);
//		cmsm02.Cmsm6020 oo6020 = new Cmsm6020();
//		oo6020.wp = wp;
//		oo6020.querySelect();
//		wp.colSet("case_date", this.getSysDate());
//		wp.colSet("case_user", wp.loginUser);
//	}

	void dataReadAfter() {
		String sql1 = "";

		// -正卡資料-
		if (ibDebit) {
			sql1 = "select id_no," + " chi_name," + " home_area_code1," + " home_tel_no1 ," + " home_tel_ext1 "
					+ " from dbc_idno" + " where 1=1" + " and id_p_seqno =?";
		} else {
			sql1 = "select id_no," + " chi_name," + " home_area_code1," + " home_tel_no1 ," + " home_tel_ext1 "
					+ " from crd_idno" + " where id_p_seqno =?";
		}
		setString2(1, wp.colStr("major_id_p_seqno"));
		sqlSelect(sql1);
		if (sqlRowNum <= 0) {
			alertErr2("查無正卡人資料");
			if (wp.itemStr("ex_id_card").length() == 10) {
				wp.colSet("case_idno", wp.itemStr("ex_id_card"));
			}
			return;
		}

		wp.colSet("maj_idno", this.sqlStr("id_no"));
		wp.colSet("maj_chi_name", sqlStr("chi_name"));
		wp.colSet("maj_telno",
				sqlStr("home_area_code1") + "-" + sqlStr("home_tel_no1") + "-" + sqlStr("home_tel_ext1"));
		wp.colSet("case_idno", sqlStr("id_no"));
		wp.colSet("recv_cname", sqlStr("chi_name"));

		// -帳務資料:XXX_ACNO-
		if (ibDebit) {
			sql1 = "select bill_sending_zip as bill_zip, " + "bill_sending_addr1 as bill_addr1 "
					+ ", bill_sending_addr2 as bill_addr2" + ", bill_sending_addr3 as bill_addr3"
					+ ", bill_sending_addr4 as bill_addr4" + ", bill_sending_addr5 as bill_addr5" + ", '' as payment_no"
					+ ", autopay_acct_no" + ", line_of_credit_amt" + ", stmt_cycle " + " from dba_acno"
					+ " where p_seqno =?";
		} else {
			sql1 = "select bill_sending_zip as bill_zip, " + "bill_sending_addr1 as bill_addr1 "
					+ ", bill_sending_addr2 as bill_addr2" + ", bill_sending_addr3 as bill_addr3"
					+ ", bill_sending_addr4 as bill_addr4" + ", bill_sending_addr5 as bill_addr5" + ", payment_no"
					+ ", autopay_acct_no" + ", line_of_credit_amt" + ", stmt_cycle " + " from act_acno"
					+ " where acno_p_seqno =?";
		}
		setString2(1, wp.colStr("acno_p_seqno"));
		sqlSelect(sql1);
		if (sqlRowNum > 0) {
			wp.colSet("bill_addr", sqlStr("bill_zip") + " " + sqlStr("bill_addr1") + sqlStr("bill_addr2")
					+ sqlStr("bill_addr2") + sqlStr("bill_addr4") + sqlStr("bill_addr5"));
			wp.colSet("bill_zip", sqlStr("bill_zip"));
			wp.colSet("bill_addr1", sqlStr("bill_addr1"));
			wp.colSet("bill_addr2", sqlStr("bill_addr2"));
			wp.colSet("bill_addr3", sqlStr("bill_addr3"));
			wp.colSet("bill_addr4", sqlStr("bill_addr4"));
			wp.colSet("bill_addr5", sqlStr("bill_addr5"));
			wp.colSet("ex_acno_payno", sqlStr("payment_no"));
			wp.colSet("ex_auto_acctno", sqlStr("autopay_acct_no"));
			wp.colSet("ex_credit_limit", sqlStr("line_of_credit_amt"));
			wp.colSet("ex_stmt_cycle", sqlStr("stmt_cycle"));
		}

		// -act_acct-
		if (!ibDebit) {
			sql1 = "select ttl_amt, min_pay" + " from act_acct" + " where p_seqno =?";
			setString2(1, wp.colStr("acct_pseqno"));
			sqlSelect(sql1);
			if (sqlRowNum > 0) {
				wp.colSet("ex_ttl_amt", sqlStr("ttl_amt"));
				wp.colSet("ex_min_pay", sqlStr("min_pay"));
			}
		}

		// -BONUS-
		if (!ibDebit) {
			sql1 = "select sum(end_tran_bp+res_tran_bp) as ttl_bp" + " from mkt_bonus_dtl" + " where 1=1"
					+ sqlCol(wp.colStr("acct_pseqno"), "p_seqno") + " and bonus_type ='BONU'";
			sqlSelect(sql1);
			if (sqlRowNum > 0) {
				wp.colSet("ex_bonus", sqlInt("ttl_bp"));
			} else
				wp.colSet("ex_bonus", 0);
		}

		// -有效卡數-
		if (ibDebit) {
			sql1 = "select count(*) as db_valid_cards" + " from dbc_card " + " where current_code='0'"
					+ " and p_seqno =?";
		} else {
			sql1 = "select count(*) as db_valid_cards" + " from crd_card " + " where current_code='0'"
					+ " and acno_p_seqno =?";
		}
		sqlSelect(sql1, new Object[] { wp.colStr("acno_p_seqno") });
		int liCards = sqlInt("db_valid_cards");
		wp.colSet("ex_valid_cards", "" + liCards);
	}

	void selectCmsCaseMaster(String aIdno) {
		if (empty(aIdno))
			return;

		this.daoTid = "B.";
		wp.sqlCmd = "select" + " case_date ," + " case_seqno ," + " card_no ," + " case_type ," + " case_desc ,"
				+ " case_result ," + " finish_date ," + " case_user ," + " send_code ," + " case_idno ,"
				+ " to_char(mod_time,'hh24miss') as mod_time ," + " ugcall_flag ," + " reply_flag "
				+ " from cms_casemaster" + " where 1=1" + sqlCol(aIdno, "case_idno")
				+ " order by case_date desc, case_seqno desc" + sqlRownum(5);
		pageQuery();
		wp.setListSernum(1, "B.ser_num");
		selectOK();
	}

	// ---------------------------------------------------------------------------------
	void doOpenRskm0930() throws Exception {
		if (userAuthRun("rskm0930") == false) {
			alertErr2("没有權限使用 臨調會簽單維護[rskm0930]");
			return;
		}

		rskm02.Rskm0930 oo0930 = new Rskm0930();
		wp.itemSet("id_no", wp.itemStr("major_idno"));
		oo0930.wp = wp;
		oo0930.readInitData();
//		oo0930.initPage();
		String sql1 = " select usr_cname " + " from sec_user " + " where usr_id = ? ";
		sqlSelect(sql1, wp.loginUser);

		wp.colSet("tel_user", sqlStr("usr_cname"));
		wp.colSet("tel_date", wp.sysDate);
		wp.colSet("tel_time", wp.sysTime);
		wp.colSet("adj_date1", wp.sysDate);
		wp.colSet("reply_date", wp.sysDate);
		if (wp.colEmpty("audit_remark")) {
			wp.colSet("audit_remark", "詳附件");
		}

		// -dddw-
		wp.optionKey = wp.colStr("trial_action");
		ddlbList("dddw_trial_action", wp.colStr("trial_action"), "ecsfunc.DeCodeRsk.trialAction");
		// -card_no-
		String lsCardNo = wp.itemStr("wk_card_no");
		wp.colSet("card_no", lsCardNo);
		int liRc = selectCrdCard(lsCardNo);
		if (liRc == 1) {
			String lsAcnoFlag = sqlStr("acno_flag");
			if (!eqIgno(lsAcnoFlag, "1"))
				wp.colSet("corp_card_flag", "Y");
			else
				wp.colSet("corp_card", "N");
			wp.colSet("pd_rating", sqlStr("curr_pd_rating"));
			wp.colSet("card_amt1", sqlNum("card_amt"));
			wp.colSet("acno_amt1", sqlNum("acno_amt"));
			wp.colSet("comp_name", sqlStr("compant_name"));
			String lsRiskLevel = sqlStr("risk_level");
			if (eqIgno(lsRiskLevel, "H"))
				wp.colSet("card_cond_07", "Y");
			else
				wp.colSet("card_cond_07", "N");
			sql2wp("comp_name");
		}

	}

	private int selectCrdCard(String aCardno) throws Exception {
		String lsSql = "select " + " A.acno_flag , "
				+ " decode(A.acno_flag,'1',B.curr_pd_rating,'') as curr_pd_rating , " + " A.corp_p_seqno , "
				+ " A.id_p_seqno , " + " A.major_id_p_seqno , " + " B.line_of_credit_amt as card_amt , "
				+ " A.acno_p_seqno, " + " C.company_name as comp_name "
				+ " from crd_card A join act_acno B on A.acno_p_seqno=B.acno_p_seqno "
				+ " join crd_idno C on C.id_p_seqno=A.id_p_seqno" + " where A.card_no = ? ";
		this.sqlSelect(lsSql, aCardno);
		if (sqlRowNum <= 0) {
			alertErr2("此卡號查無資料");
			return -1;
		}

		String sql2 = "";
		String sql3 = "";
		if (!empty(sqlStr("corp_p_seqno"))) {
			sql2 = " select risk_level from crd_corp_ext where corp_p_seqno = ? ";
			sql3 = " select chi_name as company_name from crd_corp where corp_p_seqno = ? ";
			sqlSelect(sql2, new Object[] { sqlStr("corp_p_seqno") });
			sqlSelect(sql3, new Object[] { sqlStr("corp_p_seqno") });
		} else {
			sql2 = " select risk_level from crd_idno_ext where id_p_seqno = ? ";
			sql3 = " select company_name from crd_idno where id_p_seqno = ? ";
			sqlSelect(sql2, new Object[] { sqlStr("id_p_seqno") });
			sqlSelect(sql3, new Object[] { sqlStr("major_id_p_seqno") });
		}
		// -戶額度-
		ccam01.CcasLimit ooLimit = new CcasLimit();
		ooLimit.setConn(wp.getConn());
		double lmAmt = ooLimit.idnoLimitValid(aCardno);
		sqlSetNum(0, "acno_amt", lmAmt);

		return 1;
	}

	void userName() {
		String sql1 = " select " + " usr_cname " + " from sec_user " + " where usr_id = ? ";

		sqlSelect(sql1, new Object[] { wp.loginUser });

		if (sqlRowNum > 0) {
			wp.colSet("user_name", sqlStr("usr_cname"));
		}

	}

	void wfCardConsume(String lsIdPSeqno) {
		if (empty(lsIdPSeqno))
			return;

		String lsDate = "", lsYm1 = "", lsYm2 = "";
		double lmLastAmt = 0, lmThisAmt = 0;

		lsDate = commString.mid(commDate.dateAdd(this.getSysDate(), -1, 0, 0), 0, 4);
		lsYm1 = lsDate + "01";
		lsYm2 = lsDate + "02";

		String sql1 = " select " + " sum("
				+ " uf_nvl(consume_bl_amt,0)+uf_nvl(consume_ca_amt,0)+uf_nvl(consume_it_amt,0)+"
				+ " uf_nvl(consume_ao_amt,0)+uf_nvl(consume_id_amt,0)+uf_nvl(consume_ot_amt,0)" + " ) - sum("
				+ " uf_nvl(sub_bl_amt,0)+uf_nvl(sub_ca_amt,0)+uf_nvl(sub_it_amt,0)+ "
				+ " uf_nvl(sub_ao_amt,0)+uf_nvl(sub_id_amt,0)+uf_nvl(sub_ot_amt,0) " + " ) as lm_last_amt "
				+ " from mkt_post_consume " + " where card_no in ( "
				+ " select card_no from crd_card where major_id_p_seqno = ? " + " ) "
				+ " and acct_month >= ? and acct_month <= ? ";

		sqlSelect(sql1, new Object[] { lsIdPSeqno, lsYm1, lsYm2 });
		lmLastAmt = sqlNum("lm_last_amt");
		if (eqIgno(sqlStr("lm_last_amt"), null)) {
			lmLastAmt = 0;
		}
		wp.colSet("lastyy_consum_amt", "" + lmLastAmt);

		// --近12個月消費
		lsDate = commDate.dateAdd(getSysDate(), 0, -1, 0);
		lsYm1 = commString.mid(commDate.dateAdd(lsDate, 0, -12, 0), 0, 6);
		lsDate = commString.mid(lsDate, 0, 6);

		String sql2 = " select " + " sum(uf_nvl(his_purchase_amt,0))+sum(uf_nvl(his_cash_amt,0)) as lm_amt2 "
				+ " from act_anal_sub "
				+ " where p_seqno in (select p_seqno from crd_card where major_id_p_seqno = ? ) "
				+ " and acct_month >=? and acct_month <=? ";

		sqlSelect(sql2, new Object[] { lsIdPSeqno, lsYm1, lsDate });
		lmThisAmt = sqlNum("lm_amt2");
		if (eqIgno(sqlStr("lm_amt2"), null)) {
			lmThisAmt = 0;
		}

		wp.colSet("yy_consum_amt", "" + (int) lmThisAmt);

	}

	@Override
	public void dddwSelect() {
		String lsSql = "";
		try {
			String lsIdno = commString.mid(wp.itemStr2("ex_idno"), 0,10);
			if (wp.itemEmpty("ex_idno") == false) {
				lsSql = "" + " select A.card_no as db_code, "
						+ "decode(A.current_code,'0','','X ')||A.card_no||decode(E.spec_status,'','',' ['||E.spec_status||']') as db_desc"
						+ " from crd_card A , crd_idno B , cca_card_base E " 
						+ " where A.card_no = E.card_no and B.id_p_seqno in (A.id_p_seqno,A.major_id_p_seqno) "
						+ sqlCol(lsIdno, "B.id_no") + " union "
						+ " select C.card_no as db_code, "
						+ "decode(C.current_code,'0','','X ')||C.card_no||decode(F.spec_status,'','',' ['||F.spec_status||']') as db_desc"
						+ " from dbc_card C join dbc_idno D on C.id_p_seqno =D.id_p_seqno join cca_card_base F on C.card_no = F.card_no "
						+ " where 1=1"
						+ sqlCol(lsIdno, "D.id_no") + " order by 2";
				wp.optionKey = wp.colStr("wk_card_no");
				dddwList("dddw_card_no", lsSql);
				wp.colSet("kk_card_no", "");

			}
		} catch (Exception ex) {
		}

		if (eqIgno(wp.respHtml, "ccam2090_13_detl")) {
			try {
				wp.optionKey = wp.itemStr("case_type");
				this.dddwList("dddw_casetype", "cms_casetype", "case_id", "case_desc",
						" where case_type='1' and apr_flag='Y'");

				wp.optionKey = wp.itemStr("db_case_id_A");
				this.dddwList("dddw_casetype_A", "cms_casetype", "case_id", "case_desc",
						" where case_type='A' and apr_flag='Y'");
				wp.optionKey = wp.itemStr("db_case_id_B");
				this.dddwList("dddw_casetype_B", "cms_casetype", "case_id", "case_desc",
						" where case_type='B' and apr_flag='Y'");
				wp.optionKey = wp.itemStr("db_case_id_C");
				this.dddwList("dddw_casetype_C", "cms_casetype", "case_id", "case_desc",
						" where case_type='C' and apr_flag='Y'");
				wp.optionKey = wp.itemStr("db_case_id_D");
				this.dddwList("dddw_casetype_D", "cms_casetype", "case_id", "case_desc",
						" where case_type='D' and apr_flag='Y'");
			} catch (Exception ex) {
			}
		}

	}

	@Override
	public void queryFunc() throws Exception {
		if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("kk_card_no")) {
			if (wp.itemEmpty("ex_idno"))
				alertErr2("卡號 : 不可空白 !");
			clearFunc();
			return;
		}

		func.setConn(wp);
		if (func.readData() == -1) {
			alertErr2("資料查詢錯誤 !  " + func.getMsg());
			return;
		}

		queryRead();
		if (wp.itemEmpty("ex_idno")) {
			wp.colSet("ex_idno", wp.colStr("major_idno"));
			wp.itemSet("ex_idno", wp.colStr("major_idno"));
		}

		// --變色區

		if (wp.colEq("current_code", "0")) {
			wp.colSet("current_code_color", "zz_data");
		} else {
			wp.colSet("current_code_color", "col_key");
		}

		if (wp.colEq("acct_status", "1")) {
			wp.colSet("color_acct_status", "zz_data");
		} else {
			wp.colSet("color_acct_status", "col_key");
		}

		// --
		if (checkSelectNote() == false) {
			wp.alertMesg("特殊事項:" + isNote);
			// wp.setAlert("", "特殊事項中有資料!");
			return;
		}
	}

	@Override
	public void queryRead() throws Exception {
		String lsCardNo = "";

		if (!wp.itemEmpty("ex_card_no"))
			lsCardNo = wp.itemStr("ex_card_no");
		else
			lsCardNo = wp.itemStr("kk_card_no");
		if (empty(lsCardNo)) {
			alertErr2("卡號: 不可空白");
			return;
		}
		wp.colSet("wk_card_no", lsCardNo);
		String lsMajPseqno = wp.colStr("major_id_p_seqno");
		selectAuthTxlog5(lsMajPseqno, wp.colStr("debit_flag"));

		ageCount();
	}

	void selectAuthTxlog5(String aIdPSeqno, String aDebitFlag) throws Exception {

		wp.selectSQL = "" + " card_no , " + " tx_date , " + " tx_time , " + " nt_amt , " + " iso_resp_code , "
				+ " mcc_code , " + " pos_mode , " + " auth_status_code, " + " logic_del , " + " mtch_flag , "
				+ " auth_seqno , " + " substr(pos_mode,1,2) as pos_mode_1_2 , " + " roc , " + " fallback ,"
				+ " ori_amt , consume_country , auth_no , tx_currency , trans_code , cacu_amount"
				;
		wp.daoTable = " cca_auth_txlog ";
		wp.whereStr = " where 1=1 ";

		if (eqIgno(aDebitFlag, "Y")) {
			wp.whereStr += " and card_no in (select card_no from cca_card_base where debit_flag = 'Y' "
					+ sqlCol(aIdPSeqno, "major_id_p_seqno") + " )";
		} else {
			wp.whereStr += " and card_no in (select card_no from cca_card_base where debit_flag <> 'Y' "
					+ sqlCol(aIdPSeqno, "major_id_p_seqno") + " )";
		}

		wp.whereOrder = " order by tx_date Desc , tx_time Desc " + commSqlStr.rownum(5);
		daoTid = "T1-";
		pageQuery();
		wp.setListCount(0);
		if (sqlRowNum <= 0) {
			this.selectOK();
			return;
		}

		// --取卡號後四碼:

		String lsMtchFlag = "", lsLogicDel = "", lsCardNo = "", lsAuthSeqno = "", lsServiceCode = "",
				lsCompute0035 = "";
		
		String sql1 = "select sys_data1 as tt_trans_code from cca_sys_parm3 where sys_key =? and sys_id = 'TRANCODE'";
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wp.colSet(ii, "T1-card_no_4", commString.right(wp.colStr(ii, "T1-card_no"), 4));
			
			sqlSelect(sql1,new Object[] {wp.colStr(ii,"T1-trans_code")});
			if(sqlRowNum>0) {
				wp.colSet(ii,"T1-tt_trans_code", sqlStr("tt_trans_code"));
			}
			
			lsMtchFlag = wp.colStr(ii, "T1-mtch_flag");
			lsLogicDel = wp.colStr(ii, "T1-logic_del");
			lsCardNo = wp.colStr(ii, "T1-card_no");
			lsAuthSeqno = wp.colStr(ii, "T1-auth_seqno");

			//--cca_auth_bitdata Table 用途變更判斷方式改變 lsServiceCode 相關先不處理
//			lsServiceCode = selectSC(lsCardNo, wp.colStr(ii, "T1-tx_date"), wp.colStr(ii, "T1-tx_time"));
			lsCompute0035 = wp.colStr(ii, "T1-pos_mode_1_2");
			String lsCacuAmt = wp.colStr(ii, "T1-cacu_amount");
			String lsStatusCode = wp.colStr(ii, "T1-auth_status_code");

//			if (commString.pos(",Y,U", lsMtchFlag) <= 0 && ",x,B".indexOf(lsLogicDel) <= 0) {
			if (commString.pos(",Y,U", lsMtchFlag) <= 0 && ",B".indexOf(lsLogicDel) <= 0 && lsCacuAmt.equals("Y")) {
				wp.colSet(ii, "color_txlog", "yellow");
			}
			
			wp.colSet(ii, "bk_color", "background-color: rgb(255,255,255)");
//			if (eqIgno(lsServiceCode, "101") && pos(",05,95", lsCompute0035) > 0) {
//				wp.colSet(ii, "bk_color", "background-color: rgb(0,0,0)");
//			} else {
//				wp.colSet(ii, "bk_color", "background-color: rgb(255,255,255)");
//			}
//
//			if (eqIgno(lsServiceCode, "101") && pos("|05|95", lsCompute0035) > 0) {
//				wp.colSet(ii, "font_color", "color: rgb(255,255,255)");
//			} else 
			if (eqIgno(commString.mid(wp.colStr(ii, "T1-card_no"), 1), "5") && pos("|79|80", lsCompute0035) > 0) {
				wp.colSet(ii, "font_color", "color: rgb(255,0,0)");
			} else if (eqIgno(wp.colStr(ii, "T1-roc"), "1504")) {
				wp.colSet(ii, "font_color", "color: rgb(255,0,0)");
			} else if (eqIgno(wp.colStr(ii, "T1-fallback"), "Y")) {
				wp.colSet(ii, "font_color", "color: rgb(0,0,255)");
			}

			if (wp.colNum(ii, "T1-nt_amt") < 0) {
				wp.colSet(ii, "color_nt", "color: rgb(255,0,0)");
			}
		}
	}

	String selectSC(String aCardNo, String aDate, String aTime) {

		String sql1 = " select " + " substr(bit35_track_II,23,3) as bit35_track_II " + " from cca_auth_bitdata "
				+ " where card_no = ? " + " and tx_date =? and tx_time =?"
				// + " and
				// decode(auth_seqno,'',to_char(tx_datetime,'yymmddhh24miss'),auth_seqno) = ? "
				+ commSqlStr.rownum(1);
		wp.logSql = false;
		// sqlSelect(sql1,new Object[]{a_card_no , a_auth_seqno});
		sqlSelect(sql1, new Object[] { aCardNo, aDate, aTime });

		if (sqlRowNum > 0) {
			return sqlStr("bit35_track_II");
		}
		return "";
	}

	@Override
	public void querySelect() throws Exception {
	}

	public void doQueryMonth() throws Exception {
		// --
		wp.pageRows = 999;
		isDebitFlag = wp.itemStr("debit_flag");
		// --acct_month 月份計算
		int liMonthCnt = (int) wp.itemNum("data_k1");
		String lsTempDate = "", lsAcctMonth = "";
		lsTempDate = wp.itemStr("acct_month");
		lsAcctMonth = commDate.dateAdd(lsTempDate, 0, -liMonthCnt, 0).substring(0, 6);
		String lsAcnoPseqno = wp.itemStr("acno_p_seqno");
		if (empty(lsAcnoPseqno)) {
			alertErr2("帳戶流水號(acno_p_seqno): 不可空白");
			return;
		}
		// --查詢
		wp.selectSQL = " purchase_date , " + " post_date , " + " interest_date , " + " card_no , " + " source_amt , "
				+ " dest_amt , " + " curr_code , " + " dc_dest_amt , " + " mcht_chi_name , " + " mcht_city , "
				+ " auth_code , " + " txn_code , " + " sign_flag ," + " acct_month ";
		if (eqIgno(isDebitFlag, "N")) {
			wp.daoTable = " bil_bill ";
		} else {
			wp.daoTable = " dbb_bill ";
		}

		wp.whereStr = " where 1=1 "
				+ " and acct_code in (select acct_code from ptr_actcode where interest_method ='Y') "
				+ sqlCol(lsAcnoPseqno, "p_seqno") + sqlCol(lsAcctMonth, "acct_month");

		wp.whereOrder = " order by card_no , purchase_date ";
		pageQuery();
		wp.setListCount(0);

	}

	public void querySelectPaymentRate() throws Exception {

		wp.selectSQL = " to_char(to_date(A.acct_month,'yyyymm') + 1 months ,'yyyymm') as acct_month, " + " B.stmt_last_payday , " + " A.curr_code , " + " A.stmt_this_ttl_amt , "
				+ " A.stmt_mp , " + " A.stmt_payment_amt , " + " B.stmt_payment_no , " + " A.min_pay , "
				+ " B.stmt_last_payday , " + " A.stmt_auto_pay_bank , " + " A.stmt_auto_pay_no , "
				+ " A.stmt_auto_pay_date , " + " A.stmt_auto_pay_amt , " + " A.stmt_over_due_amt ";

		wp.daoTable = " act_curr_hst A left join act_acct_hst B on A.p_seqno =B.p_seqno and A.acct_month = B.acct_month ";

		wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("acctp_seqno"), "A.p_seqno");

		wp.whereOrder = " order by A.acct_month desc, A.curr_code " + commSqlStr.rownum(12);

		logSql();

		pageQuery();
		wp.setListCount(0);

	}

	public void doQueryPostingAmt() throws Exception {
		String lsPseqno = wp.itemStr("acctp_seqno");
		if (empty(lsPseqno)) {
			alertErr2("查無帳戶序號[p_seqno]");
			return;
		}

		wp.selectSQL = " card_no, purchase_date, contract_no, auth_code, mcht_no , decode(mcht_chi_name,'',mcht_eng_name,mcht_chi_name) as mcht_chi_name , "
				+ " unit_price , tot_amt , unit_price as per_term_amt , install_tot_term , install_curr_term , "
				+ " (install_tot_term - install_curr_term) * unit_price +remd_amt +decode(install_curr_term,0,first_remd_amt+extra_fees,0) as wk_inst_unpost  "				
				;
		wp.daoTable = " bil_contract ";

		wp.whereStr = " where 1=1 and install_tot_term <> install_curr_term and apr_flag ='Y' and refund_flag<>'Y' "
				+ " and ((post_cycle_dd >0 or installment_kind ='F') OR (post_cycle_dd=0 AND DELV_CONFIRM_FLAG='Y' AND auth_code='DEBT')) "
				+ sqlCol(lsPseqno, "p_seqno");

		pageQuery();
		wp.setListCount(0);

	}

	public void doQueryAuthTxlog() throws Exception {

		if (!empty(wp.itemStr("kk_card_no"))) {
			isCardNo = wp.itemStr("kk_card_no");
		} else {
			isCardNo = wp.itemStr("ex_card_no");
		}

		wp.selectSQL = "A.*," + " substr(A.pos_mode,1,2) as pos_mode_1_2 ," + " substr(A.pos_mode,3,1) as pos_mode_3 ,"
				+ " uf_idno_id2(A.card_no,'') as id_no ," + " uf_idno_name(A.id_p_seqno) as db_idno_name ,"
				+ " uf_tt_ccas_parm3('LOGICDEL',A.logic_del) as tt_logic_del ,"
				+ " uf_tt_ccas_parm3('AUTHUNIT',A.auth_unit) as tt_auth_unit ,"
				+ " decode(curr_tot_std_amt,0,0,((curr_tot_unpaid+decode(cacu_amount,'Y',nt_amt,0)) / curr_tot_std_amt)) * 100 as cond_curr_rate , "
				+ " iso_resp_code||'-'||auth_status_code||'-'||iso_adj_code as wk_resp , "
				+ " ibm_bit39_code||'-'||ibm_bit33_code as wk_IBM  ";
		wp.daoTable = " cca_auth_txlog A  ";
		wp.whereStr = " where 1=1 " + sqlCol(isCardNo, "A.card_no");
		wp.whereOrder = " order by A.tx_date desc, A.tx_time Desc "
				+ sqlRownum(500);
		pageQuery();
		wp.setListCount(0);
		if (sqlRowNum <= 0)
			return;
		queryAuthTxlogAfter(wp.selectCnt);
	}

	void queryAuthTxlogAfter(int aNrow) throws Exception {
		String sql1 = " select mcht_name as mcht_chi_name , mcht_eng_name " + " from cca_mcht_bill " + " where mcht_no = ? and acq_bank_id = ? ";		
		String sql2 = " select entry_type from cca_entry_mode where entry_mode = ? " + commSqlStr.rownum(1);
		String sql3 = " select sys_data1 as tt_trans_code from cca_sys_parm3 where sys_key =? and sys_id = 'TRANCODE' ";
		String sql4 = " select mcht_name as mcht_chi_name , mcht_eng_name " + " from cca_mcht_bill " + " where mcht_no = ? ";
		for (int ll = 0; ll < aNrow; ll++) {
			sqlSelect(sql3,new Object[] {wp.colStr(ll,"trans_code")});
			if(sqlRowNum>0) {
				wp.colSet(ll, "tt_trans_code",sqlStr("tt_trans_code"));
			}
			
			// -己授權未請款-
			String lsMtchFlag = wp.colStr(ll, "mtch_flag");
			String lsLogicDel = wp.colStr(ll, "logic_del");
			String lsCacu = wp.colStr(ll, "cacu_amount");
			String lsStatus = wp.colStr(ll, "auth_status_code");
			if (commString.pos(",Y,U", lsMtchFlag) <= 0 && ",x,B".indexOf(lsLogicDel) <= 0 && eqIgno(lsCacu, "Y")
					&& pos(",AB", lsStatus) <= 0) {
				wp.colSet(ll, "color_list_4", "yellow");
			}

			sqlSelect(sql1, new Object[] { wp.colStr(ll, "mcht_no"),wp.colStr(ll,"stand_in") });
			if (sqlRowNum > 0) {				
				if(sqlStr("mcht_chi_name").isEmpty()) {
					wp.colSet(ll, "mcht_chi_name", sqlStr("mcht_eng_name"));
				}	else	{
					wp.colSet(ll, "mcht_chi_name", sqlStr("mcht_chi_name"));
				}								
			}	else	{
				sqlSelect(sql4, new Object[] {wp.colStr(ll, "mcht_no")});
				if (sqlRowNum > 0) {
					if(sqlStr("mcht_chi_name").isEmpty()) {
						wp.colSet(ll, "mcht_chi_name", sqlStr("mcht_eng_name"));
					}	else	{
						wp.colSet(ll, "mcht_chi_name", sqlStr("mcht_chi_name"));
					}
				}	else	{
					wp.colSet(ll,"mcht_chi_name", wp.colStr(ll,"mcht_name"));
				}
						
			}

			sqlSelect(sql2, new Object[] { wp.colStr(ll, "pos_mode_1_2") });
			if (sqlRowNum > 0) {
				wp.colSet(ll, "db_entry_mode_type", sqlStr("entry_type"));
			}

			String lsCompute0035 = wp.colStr(ll, "pos_mode_1_2");
			String lsServiceCode = "";
			
			wp.colSet(ll,"bk_color", "background-color: rgb(255,255,255)");
//			lsServiceCode = selectSC(wp.colStr(ll, "card_no"), wp.colStr(ll, "auth_seqno"))
//			if (eqIgno(lsServiceCode, "101") && pos("|05|95", lsCompute0035) > 0) {
//				wp.colSet(ll, "bk_color", "background-color: rgb(0,0,0)");
//			} else {
//				wp.colSet(ll, "bk_color", "background-color: rgb(255,255,255)");
//			}
			
//			if (eqIgno(lsServiceCode, "101") && pos("|05|95", lsCompute0035) > 0) {
//				wp.colSet(ll, "color_cardno", "color: rgb(255,255,255)");
//			} else 
			if (eqIgno(commString.mid(wp.colStr(ll, "card_no"), 1), "5") && pos("|79|80", lsCompute0035) > 0) {
				wp.colSet(ll, "color_cardno", "color: rgb(255,0,0)");
			} else if (eqIgno(wp.colStr(ll, "roc"), "1504")) {
				wp.colSet(ll, "color_cardno", "color: rgb(255,0,0)");
			} else if (eqIgno(wp.colStr(ll, "fallback"), "Y")) {
				wp.colSet(ll, "color_cardno", "color: rgb(0,0,255)");
			}
			
			// -curr_otb_amt-
			double lmAmt = wp.colNum(ll, "curr_otb_amt");
			if (lmAmt < 0) {
				wp.colSet(ll, "color_otb", "color: red");
			}
			if (wp.colNum(ll, "nt_amt") < 0) {
				wp.colSet(ll, "color_nt", "color: red");
			}
		}
	}

	String selectSC(String aCardNo, String aAuthSeqno) throws Exception {
		String sql1 = " select " + " substr(bit35_track_II,23,3) as bit35_track_II " + " from cca_auth_bitdata "
				+ " where card_no = ? " + " and auth_seqno = ? " + commSqlStr.rownum(1);
		sqlSelect(sql1, new Object[] { aCardNo, aAuthSeqno });
		if (sqlRowNum > 0) {
			return sqlStr("bit35_track_II");
		}
		return "";
	}

	public void doQueryPaymentRefund() throws Exception {
		if (eqIgno(isDebitFlag, "Y")) {
			okAlert("VD卡: 不顯示[近三次繳款方式 ]");
			return;
		}

		wp.sqlCmd = "select " + " acct_date , " + " dr_cr , " + " reversal_flag , " + " tran_type , "
				+ " payment_rev_amt , " + " reference_no , " + " crt_date , " + " crt_time , " + " curr_code , "
				+ " interest_date , " + " dc_transaction_amt , " + " p_seqno , " + " jrnl_seqno , " + " enq_seqno , "
				+ " transaction_amt , " + " dc_transaction_amt - payment_rev_amt as wk_amt ,"
				+ " (select bill_desc from ptr_payment where payment_type = act_jrnl.tran_type) as tt_tran_type " 
				+ " from act_jrnl"
				+ " where 1=1" + " and p_seqno =? "
				+ " and tran_class = 'P' "
				+ " and tran_type in (select payment_type from ptr_payment where fund_flag<>'Y')"
				+ " order by acct_date desc" + commSqlStr.rownum(3);
		setString2(1, wp.itemStr2("acctp_seqno"));
		pageQuery();
		wp.setListCount(0);
	}

	void doQueryAuthRemarkLog() throws Exception {
		wp.sqlCmd = "select chg_date, chg_time, chg_user" + ", user_deptno, auth_remark" + " from cca_auth_remark_log"
				+ " where card_acct_idx =?" + " order by chg_date desc, chg_time desc";
		setDouble(1, wp.itemNum("card_acct_idx"));

		wp.pageRows = 9999;
		this.pageQuery();
		wp.setListCount(0);
		if (sqlRowNum <= 0) {
			alertErr2("無授權備註記錄");
		}
	}

	void doQueryTrial() throws Exception {
		String lsBatchNo = "", lsIdPSeqno = "";
		String sql1 = " select batch_no , id_p_seqno from rsk_trial_list where id_p_seqno = ? order by query_date Desc "
				+ commSqlStr.rownum(1);
		lsIdPSeqno = wp.itemStr("id_p_seqno");
		sqlSelect(sql1, new Object[] { lsIdPSeqno });
		// String sql1 = " select batch_no , id_p_seqno from rsk_trial_list where id_no
		// = ? order by
		// query_date Desc "+commSqlStr.rownum(1);
		// sqlSelect(sql1,new Object[]{wp.sss("id_no")});

		if (sqlRowNum <= 0) {
			alertErr2("查無資料");
			return;
		}

		lsBatchNo = sqlStr("batch_no");
		// ls_id_p_seqno = sql_ss("id_p_seqno");

		wp.sqlCmd = "select " + " C.ecs_jcic_score , A.ecs004 , B.tol_score , C.trial_date "
				+ " from rsk_trial_list C left join rsk_trial_data_ecs A"
				+ "    on A.batch_no =C.batch_no and A.id_p_seqno =C.id_p_seqno" + " left join rsk_trial_data_jcic B"
				+ "    on A.batch_no =B.batch_no and A.id_p_seqno =B.id_p_seqno"
				+ " where C.batch_no =? and C.id_p_seqno =?";

		setString2(1, lsBatchNo);
		setString(lsIdPSeqno);
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + lsBatchNo);
			return;
		}

		if (wp.colNum("ecs_jcic_score") == 0) {
			wp.colSet("ecs_jcic_score", wp.colNum("ecs004") + wp.colNum("tol_score"));
		}

		// //--評等
		// selectLevel(ls_batch_no,ls_id_p_seqno);
		// //--中文
		// ttData();

	}

	void selectLevel(String lsBatchNo, String lsIdPSeqno) {
		String sql1 = " select " + " ecs_jcic_level " + " from rsk_trial_list " + " where batch_no = ? "
				+ " and id_p_seqno = ? ";
		sqlSelect(sql1, new Object[] { lsBatchNo, lsIdPSeqno });
		if (sqlRowNum <= 0) {
			return;
		}
		wp.colSet("ecs_jcic_level", sqlStr("ecs_jcic_level"));
	}

	void ttData() {
		if (wp.colEq("ecs005", "1")) {
			wp.colSet("tt_ecs005", ".男");
		} else if (wp.colEq("ecs005", "2")) {
			wp.colSet("tt_ecs005", ".女");
		}

		if (wp.colEq("ecs006", "1")) {
			wp.colSet("tt_ecs006", ".博士");
		} else if (wp.colEq("ecs006", "2")) {
			wp.colSet("tt_ecs006", ".碩士");
		} else if (wp.colEq("ecs006", "3")) {
			wp.colSet("tt_ecs006", ".大學");
		} else if (wp.colEq("ecs006", "4")) {
			wp.colSet("tt_ecs006", ".專科");
		} else if (wp.colEq("ecs006", "5")) {
			wp.colSet("tt_ecs006", ".高中高職");
		} else if (wp.colEq("ecs006", "6")) {
			wp.colSet("tt_ecs006", ".其他");
		}

		if (wp.colEq("ecs046", "0")) {
			wp.colSet("tt_ecs046", ".無");
		} else if (wp.colEq("ecs046", "1")) {
			wp.colSet("tt_ecs046", ".設質");
		} else if (wp.colEq("ecs046", "2")) {
			wp.colSet("tt_ecs046", ".保人");
		} else if (wp.colEq("ecs046", "3")) {
			wp.colSet("tt_ecs046", ".風險行");
		}

		if (wp.colEq("jcic036", "A")) {
			wp.colSet("tt_jcic036", ".無註記");
		} else if (wp.colEq("jcic036", "B")) {
			wp.colSet("tt_jcic036", ".非信用疑慮註記");
		} else if (wp.colEq("jcic036", "C")) {
			wp.colSet("tt_jcic036", ".有信用疑慮註記");
		}

	}

//	void doQueryCmsm6020() throws Exception {
//		cmsm02.Cmsm6020 oo6020 = new Cmsm6020();
//		oo6020.wp = wp;
//		oo6020.querySelect2();
//	}

	void querySelect2After() {

		String sql1 = " select " + " dept_name " + " from ptr_dept_code " + " where dept_code = ? ";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "proc_deptno") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "tt_proc_deptname", sqlStr("dept_name"));
			}
		}

	}

	public void doQueryCardAcct() throws Exception {
		isDebitFlag = "N";
		boolean lbDebit = wp.itemEq("debit_flag", "Y");
		if (lbDebit)
			isDebitFlag = "Y";

		if (!lbDebit) {
			wp.selectSQL = "" + " card_acct_idx , " + " acno_flag , " + " acno_p_seqno , " + " id_p_seqno , "
					+ " adj_area , " + " adj_area as org_area ," + " adj_reason , " + " adj_reason as org_reason ,"
					+ " tot_amt_month , " + " tot_amt_month as org_amt_mon ," + " adj_eff_start_date , "
					+ " adj_eff_start_date as ori_start_date , " + " adj_eff_end_date ,"
					+ " adj_eff_end_date as ori_end_date , " + " adj_inst_pct , " + " adj_remark , "
					+ " adj_remark as org_remark ," + " spec_status , " + " adj_risk_flag ," + " mod_user,"
					+ " crt_user," + " crt_date," + " adj_date," + " debit_flag,"
					+ " to_char(mod_time,'yyyymmdd') as mod_date ," + " uf_idno_name(id_p_seqno) as chi_name ,"
					+ " 0 as comp_amt , " + " 0 as comp_inst_amt , " + " 0 as line_credit_amt , " + " block_reason1 , "
					+ " block_reason2 , " + " block_reason3 , " + " block_reason4 , " + " block_reason5 , "
					+ " block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as wk_block_reason ,"
					+ " spec_status ," + " hex(rowid) as rowid, mod_seqno";
			wp.daoTable = " cca_card_acct";
			wp.whereStr = " where 1=1 " + col(wp.itemNum("card_acct_idx"), "card_acct_idx",true);

			pageSelect();
			if (sqlRowNum <= 0) {
				alertErr2("查無資料");
				return;
			}

			if (getSysDate().compareTo(wp.colStr("adj_eff_start_date")) >= 0
					&& wp.colStr("adj_eff_end_date").compareTo(getSysDate()) >= 0) {
				dataReadAfter6();
				selectCcaAdjParm();
				// checkSupCard();
				String lsCardNote = "";
				lsCardNote = getCardNote(wp.colStr("acno_p_seqno"));
				wp.colSet("card_note", lsCardNote);
				selectAdjReason();
			} else {
				alertErr2("查無臨調");
				return;
			}

		} else if (lbDebit) {
			wp.selectSQL = "" + " acno_p_seqno ," + " debit_flag ," + " acno_flag ," + " card_acct_idx ,"
					+ " block_status ," + " spec_status ," + " adj_quota ," + " adj_eff_start_date ,"
					+ " adj_eff_end_date ," + " adj_area ," + " tot_amt_month ," + " adj_inst_pct ," + " adj_remark ,"
					+ " uf_acno_key2(acno_p_seqno,debit_flag) as acct_key ";
			wp.daoTable = " cca_card_acct ";
			wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("acno_p_seqno"), "acno_p_seqno")
					+ sqlCol(isDebitFlag, "debit_flag");
			pageQuery();
			wp.setListCount(0);
		}
	}

	void selectAdjReason() {
		String sql1 = " select sys_data1 from cca_sys_parm3 where sys_id = 'ADJREASON' and sys_key = ? ";
		sqlSelect(sql1, new Object[] { wp.colStr("adj_reason") });

		if (sqlRowNum > 0) {
			wp.colSet("tt_adj_reason", sqlStr("sys_data1"));
		}

	}

	void dataReadAfter6() {
		String sql0 = " select count(*) as db_cnt from crd_card where acno_p_seqno = ? and current_code='0' and sup_flag='1' ";
		sqlSelect(sql0, new Object[] { wp.colStr("acno_p_seqno") });
		if (sqlRowNum > 0) {
			wp.colSet("card_sup_cnt", sqlStr("db_cnt"));
		}

		String sql1 = "select acct_type , " + " acct_key ," + " line_of_credit_amt as line_credit_amt , "
				+ " uf_acno_name(acno_p_seqno) as acno_name , " + " class_code " + " from act_acno " + " where 1=1 "
				+ " and acno_p_seqno = ? ";
		setString2(1, wp.colStr("acno_p_seqno"));
		sqlSelect(sql1);
		if (sqlRowNum <= 0) {
			alertErr2("查無帳戶資料(act_acno), kk=" + wp.colStr("acno_p_seqno"));
			return;
		}
		wp.colSet("acct_type", sqlStr("acct_type"));
		wp.colSet("acct_key", sqlStr("acct_key"));
		wp.colSet("line_credit_amt", sqlStr("line_credit_amt"));
		wp.colSet("class_code", sqlStr("class_code"));
		wp.colSet("risk_level", sqlStr("class_code"));
		if (wp.colNum("adj_inst_pct") == 0) {
			wp.colSet("adj_inst_pct", sqlStr("line_credit_amt"));
		}

		// --2018-07-12
		String sql3 = " select " + " tot_amt_month " + " from cca_card_acct " + " where "
				+ " to_char(sysdate,'yyyymmdd') between adj_eff_start_date and adj_eff_end_date "
				+ col(wp.colNum("card_acct_idx"), "card_acct_idx",true);

		sqlSelect(sql3);
		if (sqlNum("tot_amt_month") > sqlNum("line_credit_amt")) {
			wp.colSet("line_credit_amt_t", sqlStr("tot_amt_month"));
		} else {
			wp.colSet("line_credit_amt_t", sqlStr("line_credit_amt"));
		}

		// --
		String sql2 = "select A.fh_flag as rela_flag , " + " A.non_asset_balance as asset_balance , "
				+ " B.asset_value as bond_amt " + " from crd_idno B left join crd_correlate A  "
				+ " on A.correlate_id =B.id_no " + " where B.id_p_seqno =? " + " order by A.crt_date desc "
				+ commSqlStr.rownum(1);
		sqlSelect(sql2, new Object[] { wp.colStr("id_p_seqno") });

		if (sqlRowNum <= 0) {
			wp.colSet("rela_flag", "");
			wp.colSet("asset_balance", "");
			wp.colSet("bond_amt", "");
			return;
		}
		wp.colSet("rela_flag", sqlStr("rela_flag"));
		wp.colSet("asset_balance", sqlStr("asset_balance"));
		wp.colSet("bond_amt", sqlStr("bond_amt"));

		// --
//		double lmAmt = wfPayAmt(wp.colStr("acctp_seqno"));
//		wp.colSetNum("db_pay_amt", lmAmt, 0);
	}

	String getCardNote(String aPseqno) {
		String lsSql = "select A.card_note, count(*) as cnt_parm"
				+ " from crd_card A join cca_risk_consume_parm B on B.card_note=A.card_note and B.apr_date<>''"
				+ " where A.current_code='0'" + " and A.acno_p_seqno =?" + " group by A.card_note"
				+ " order by decode(A.card_note,'C',9,'G',8,'P',7,'S',6,'I',5,10)";
		setString2(1, aPseqno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0)
			return "*";
		for (int ll = 0; ll < sqlRowNum; ll++) {
			if (sqlInt("cnt_parm") > 0)
				return sqlStr("card_note");
		}
		// 通用--
		return "*";
	}

//	double wfPayAmt(String aPseqno) {
//		if (empty(aPseqno))
//			return 0;
//
//		String sql1 = "select sum(nvl(a.pay_amt,0)) as lm_pay_amt " + " from  act_pay_detail a, act_pay_batch b "
//				+ " where 1=1 " + " and a.p_seqno = ? " + " and a.batch_no = b.batch_no " + " and b.batch_tot_cnt > 0 "
//				+ " and b.batch_no not like '%9001%'";
//		sqlSelect(sql1, new Object[] { aPseqno });
//
//		double lmPayAmt = sqlNum("lm_pay_amt");
//
//		String sql2 = "select sum(nvl(txn_amt,0)) as lm_pay_amt2 " + " from act_pay_ibm " + " where 1=1 "
//				+ " and p_seqno = ? " + " and nvl(proc_mark,'N') <> 'Y' " + " and nvl(error_code,'0') = '0'";
//		sqlSelect(sql2, new Object[] { aPseqno });
//
//		double lmPayAmt2 = sqlNum("lm_pay_amt2");
//
//		return lmPayAmt + lmPayAmt2;
//	}

	void selectCcaAdjParm() throws Exception {				
		wp.pageRows = 999;
		wp.sqlCmd = " select " + " card_acct_idx , " + " risk_type, card_note, risk_level,"
				+ " uf_tt_risk_type(risk_type) as tt_risk_type , " + " adj_month_amt , " // -月限額-
				+ " adj_day_amt, " // -次限額-
				+ " adj_day_cnt , " // -日限次-
				+ " adj_month_cnt , " // -月限次-
				+ " spec_flag "
				+ " from cca_adj_parm " + " where 1=1 " + col(wp.colNum("card_acct_idx"), "card_acct_idx", true)
				+ " order by risk_type";
		pageQuery();

		if (sqlNotFind()) {
			selectOK();
			wp.setListCount(0);
			return ;
//			String lsCardNote = getCardNote(wp.colStr("acno_p_seqno"));
//			wp.sqlCmd = " select " + " A.* , " + " uf_tt_risk_type(A.risk_type) as tt_risk_type "
//					+ " from cca_risk_consume_parm A " + " where A.area_type ='T' " + " and A.card_note = ?"
//					+ " and A.risk_level =?" + " order by A.risk_type";
//			setString2(1, lsCardNote);
//			setString(wp.colStr("class_code"));
//
//			pageQuery();
//			int llNrow = sqlRowNum;
//			double lmLineAmt = wp.colNum("line_credit_amt");
//			double lmTotAmt = lmLineAmt; // wp.col_num("tot_amt_month");
//			for (int ii = 0; ii < llNrow; ii++) {
//				wp.colSetNum(ii, "adj_month_amt", lmTotAmt, 0);
//				wp.colSetNum(ii, "adj_day_amt", (lmTotAmt * wp.colNum(ii, "lmt_amt_time_pct") / 100), 0);
//				wp.colSet(ii, "adj_day_cnt", wp.colStr(ii, "lmt_cnt_day"));
//				wp.colSet(ii, "adj_month_cnt", wp.colStr(ii, "lmt_cnt_month"));
//			}
		}
		wp.setListCount(0);

	}

	void checkSupCard() {
		String sql0 = " select count(*) as db_cnt from crd_card where acno_p_seqno = ?"
				+ " and current_code='0' and sup_flag='1'";
		sqlSelect(sql0, new Object[] { wp.colStr("acno_p_seqno") });
		if (sqlRowNum > 0) {
			wp.colSet("card_sup_cnt", sqlStr("db_cnt"));
		}
	}

	void selectCardAcctAfter() {
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if (eqIgno(wp.colStr(ii, "acno_flag"), "1")) {
				String sql1 = "select acct_type , " + " line_of_credit_amt ," + " corp_act_flag , "
						+ "uf_idno_name(id_p_seqno) as idno_name ," + "uf_corp_name(corp_p_seqno) as corp_name "
						+ " from act_acno " + " where 1=1 " + " and acno_p_seqno = ? ";
				sqlSelect(sql1, new Object[] { wp.colStr(ii, "acno_p_seqno") });

				if (eqIgno(sqlStr("corp_act_flag"), "Y")) {
					wp.colSet(ii, "acct_type", sqlStr("acct_type"));
					wp.colSet(ii, "line_of_credit_amt", sqlStr("line_of_credit_amt"));
					wp.colSet(ii, "acno_name", sqlStr("corp_name"));
					wp.colSet(ii, "acno_flag", "12");
				} else if (eqIgno(sqlStr("corp_act_flag"), "N")) {
					wp.colSet(ii, "acct_type", sqlStr("acct_type"));
					wp.colSet(ii, "acno_name", sqlStr("idno_name"));
					wp.colSet(ii, "line_of_credit_amt", sqlStr("line_of_credit_amt"));
				}
			} else if (eqIgno(wp.colStr(ii, "acno_flag"), "2")) {
				String sql2 = "select acct_type , " + " line_of_credit_amt ," + " corp_act_flag , " + wp.sqlID
						+ "uf_idno_name(id_p_seqno) as idno_name ," + wp.sqlID
						+ "uf_corp_name(corp_p_seqno) as corp_name " + " from act_acno " + " where acno_p_seqno = ? ";
				sqlSelect(sql2, new Object[] { wp.colStr(ii, "acno_p_seqno") });

				wp.colSet(ii, "acct_type", sqlStr("acct_type"));
				wp.colSet(ii, "line_of_credit_amt", sqlStr("line_of_credit_amt"));
				wp.colSet(ii, "acno_name", sqlStr("idno_name"));
			}

			wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));
			wp.colSet(ii, "adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_start_date")) + " -- "
					+ commString.strToYmd(wp.colStr(ii, "adj_eff_end_date")));
		}
	}

	public void doQueryRiskProd() throws Exception {

		isDebitFlag = wp.itemStr("debit_flag");

		if (eqIgno(isDebitFlag, "Y")) {
			alertErr2("非信用卡無高風險性產品 !");
			return;
		}

		wp.sqlCmd = "select A.acct_type" + ", uf_month_add(A.first_post_date,0) as contract_ym_s "
				+ ", uf_month_add(A.first_post_date,A.install_tot_term -1) as contract_ym_e "
				+ ", A.install_tot_term as inst_tot_term" + ", A.tot_amt" + ", A.install_curr_term as inst_curr_term"
				+ ", C.prod_name"
				+ " FROM bil_contract A  join rsk_hirisk_prod C on C.mcht_no=A.mcht_no and C.tot_term=A.install_tot_term"
				+ " where A.id_p_seqno =?" + " and A.install_tot_term > A.install_curr_term"
				+ " and nvl(A.refund_apr_flag,'N') <> 'Y'" + " AND C.active_flag = 'Y' AND C.apr_flag = 'Y'"
				+ " order by a.install_curr_term desc";
		setString(1, wp.itemStr("id_p_seqno"));
		pageQuery();
		wp.setListCount(0);

	}

	boolean checkSelectNote() {
		String sql1 = " select " + " id_note " + " from cca_id_note " + " where id_no = ? and close_date >= ? ";

		sqlSelect(sql1, new Object[] { wp.colStr("id_no") , getSysDate() });

		if (sqlRowNum > 0) {
			isNote = sqlStr("id_note");
			return false;
		}

		return true;
	}

	public void querySelectNote() throws Exception {

		wp.selectSQL = "" + " crt_date , " + " id_note , " + " close_date , " + " crt_user , "
				+ " hex(rowid) as rowid , " + " id_note||close_date as old_data ";

		wp.daoTable = " cca_id_note ";

		wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("id_no"), "id_no");
		pageQuery();
		if (this.sqlNotFind()) {
			selectOK();
		}
		wp.colSet("IND_NUM", "" + wp.selectCnt);
		wp.setListCount(0);

	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFunc() throws Exception {
		if (eqIgno(wp.respHtml, "ccam2090_14_detl")) {
			rskm02.Rskm0930Func func = new rskm02.Rskm0930Func();
			func.setConn(wp);

			rc = func.dbSave(strAction);
			sqlCommit(rc);
			if (rc != 1) {
				errmsg(func.getMsg());
			} else
				this.saveAfter(false);

		} else if (eqIgno(wp.respHtml, "ccam2090")) {
			Ccam2090Func func = new Ccam2090Func();
			func.setConn(wp);
			rc = func.dbSave(strAction);
			sqlCommit(rc);
			if (rc != 1) {
				errmsg(func.getMsg());
			} else
				this.saveAfter(false);
		} 
		// 20201119 ignore Cmsm6020 for phase 1
//		else if (eqIgno(wp.respHtml, "ccam2090_13_detl")) {
//			cmsm02.Cmsm6020Func func = new cmsm02.Cmsm6020Func();
//			func.setConn(wp);
//
//			if (dataCheck() == -1) {
//				return;
//			}
//			rc = func.dbInsert();
//			if (rc == 1) {
//				wp.listCount[0] = 0;
//				wp.listCount[1] = 0;
//			}
//
//			this.sqlCommit(rc);
//			if (rc != 1) {
//				alertErr2(func.getMsg());
//			}
//			this.saveAfter(false);
//
//		}
	}

	int dataCheck() {

		String[] aaDept = wp.itemBuff("dept_code");
		String[] aaOpt = wp.itemBuff("opt_dept");
		if (aaDept == null)
			return 0;

		wp.listCount[0] = wp.itemRows("dept_code");
		wp.listCount[1] = wp.itemRows("B.case_date");
		if (wp.itemEq("send_code", "Y") == false)
			return 1;

		for (int ll = 0; ll < aaDept.length; ll++) {
			if (checkBoxOptOn(ll, aaOpt)) {
				return 1;
			}
		}

		// -no-select-
		alertErr2("請點選 案件移送部門");

		return rc;
	}

	public void updateMcode() throws Exception {
		func.setConn(wp);

		if (!wp.itemEmpty("opt1")) {
			if (wp.itemEmpty("mcode_valid_date")) {
				errmsg("M Code 有效截止日 : 不可空白");
				return;
			}
		}

		if (!wp.itemEmpty("opt2")) {
			if (wp.itemEmpty("ccas_class_code")) {
				errmsg("卡人等級 有效截止日 : 不可空白");
				return;
			}
		}

		rc = func.updateMC();
		sqlCommit(rc);
		if (rc != 1) {
			errmsg(func.getMsg());
		} else {
			wp.colSet("opt1", "");
			wp.colSet("opt2", "");
			func.readChgMcode();
			alertMsg("修改成功");
		}
	}

	public void updateNote() throws Exception {
		int llOk = 0, llErr = 0;

		func.setConn(wp);
		String[] lsIdNote = wp.itemBuff("id_note");
		String[] lsCloseDate = wp.itemBuff("close_date");
		String[] lsRowid = wp.itemBuff("rowid");
		String[] aaOpt = wp.itemBuff("opt");
		String[] lsOldData = wp.itemBuff("old_data");
		wp.listCount[0] = lsIdNote.length;
		for (int rr = 0; rr < lsIdNote.length; rr++) {
			if (empty(lsIdNote[rr])) {
				continue;
			}
			func.varsSet("id_note", lsIdNote[rr]);
			func.varsSet("close_date", lsCloseDate[rr]);
			if (!empty(lsOldData[rr]))
				func.varsSet("rowid", lsRowid[rr]);

			if (checkBoxOptOn(rr, aaOpt)) {
				if (func.deleteNote() == 1) {
					llOk++;
					continue;
				} else {
					llErr++;
					continue;
				}
			}

			if (!eqIgno(lsOldData[rr], lsIdNote[rr] + lsCloseDate[rr])) {
				if (!empty(lsOldData[rr]))
					func.deleteNote();
				if (func.insertNote() == 1) {
					llOk++;
					continue;
				} else {
					llErr++;
					continue;
				}
			}

		}

		if (llOk > 0)
			sqlCommit(1);
		querySelectNote();
		alertMsg("存檔完成  成功:" + llOk + " 失敗:" + llErr);

	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		if (!eqIgno(wp.respHtml, "ccam2090_14_detl") && !eqIgno(wp.respHtml, "ccam2090_13_detl")) {
			this.btnModeAud();
			if(wp.colEmpty("wk_card_no")) {
				btnUpdateOn(false);
			}
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

	void cmsQ0010Auth() {
		if (!userAuthRun("cmsq0010")) {
			alertErr2("没有權限使用 [歸戶餘額查詢]");
			return;
		}

		// cmsm01.Cmsq0010Func func = new cmsm01.Cmsq0010Func();
		// func.setConn(wp);
		// rc =func.readData(wp.sss("id_no"));
		// if (rc !=1) {
		// wp.listCount[0] =0;
		// ok_alert(func.getMsg());
		// }
	}

	void cmsQ0010Read() {

		cmsm01.Cmsq0010Func func = new cmsm01.Cmsq0010Func();
		func.setConn(wp);
		rc = func.readData(wp.itemStr2("id_no"));
		if (rc != 1) {
			wp.listCount[0] = 0;
			okAlert(func.getMsg());
		}
	}

	// 20200107 modify AJAX
	public void wfAjaxIdno() throws Exception {
		// wp = wr;

		if (itemIsempty("ax_idno")) {
			return;
		}

		String lsIdno = wp.itemStr("ax_idno");
		String lsIdcode = "0";
		if (lsIdno.length() == 11) {
			lsIdcode = commString.right(lsIdno, 1);
			lsIdno = commString.left(lsIdno, 10);
		}

		wp.sqlCmd = " select A.card_no , A.current_code as curr_code from crd_card A join crd_idno B "
				+ " on A.id_p_seqno = B.id_p_seqno where id_no =? and id_no_code =?" + " union "
				+ " select A.card_no , A.current_code as curr_code from dbc_card A join dbc_idno B "
				+ " on A.id_p_seqno = B.id_p_seqno where id_no =? and id_no_code =?" + " order by 2 Asc ";
		setString(1, lsIdno);
		setString(lsIdcode);
		setString(lsIdno);
		setString(lsIdcode);

		this.sqlSelect();

		if (sqlRowNum <= 0) {
			wp.addJSON("ls_card_no", "");
			wp.addJSON("ls_curr_code", "");
			alertErr2("查無卡號 !");
			clearFunc();
			return;
		}
		for (int ii = 0; ii < sqlRowNum; ii++) {
			log("curr_code :" + sqlStr(ii, "curr_code"));
			wp.addJSON("ls_card_no", sqlStr(ii, "card_no"));
			wp.addJSON("ls_curr_code", sqlStr(ii, "curr_code"));
		}
	}

	void ageCount() {
		String[] lsLife = new String[] { "鼠", "牛", "虎", "兔", "龍", "蛇", "馬", "羊", "猴", "雞", "狗", "豬" };
		int liYear = 1912, liBirthDay = 0, liLifeYear = 0, liAge = 0;
		String lsBirthday = "", lsSysDate = "", lsLifeS = "";
		if(wp.colEmpty("birthday"))	{
			wp.colSet("wk_id_age", "");
			wp.colSet("birth_rule", "");
			return ;
		}
		lsSysDate = this.getSysDate();
		lsBirthday = wp.colStr("birthday");
		liBirthDay = commString.strToInt(commString.mid(lsBirthday, 0, 4));
		liAge = commString.strToInt(commString.mid(lsSysDate, 0, 4)) - liBirthDay;
		liLifeYear = (liBirthDay - liYear) % 12;
		lsLifeS = lsLife[liLifeYear];

		wp.colSet("wk_id_age", "" + liAge);
		wp.colSet("birth_rule", "" + lsLifeS);
	}

	void doBillSendType() {
		wp.selectSQL = " stat_send_paper , " + " stat_send_s_month , " + " stat_send_e_month , " + " paper_upd_date , "
				+ " paper_upd_user , " + " stat_send_internet , " + " stat_send_s_month2 , " + " stat_send_e_month2 , "
				+ " internet_upd_date , " + " internet_upd_user , " + " stat_send_fax , " + " special_stat_code , "
				+ " special_stat_s_month , " + " special_stat_e_month , " + " stat_unprint_flag , "
				+ " stat_unprint_s_month , " + " stat_unprint_e_month ";
		wp.daoTable = "act_acno";
		wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("acno_p_seqno"), "acno_p_seqno");
		pageSelect();
		if (sqlRowNum <= 0) {
			errmsg("查無資料");
			return;
		}

		if (wp.colEq("special_stat_code", "1")) {
			wp.colSet("tt_special_stat_code", ".航空");
		} else if (wp.colEq("special_stat_code", "2")) {
			wp.colSet("tt_special_stat_code", ".掛號");
		} else if (wp.colEq("special_stat_code", "3")) {
			wp.colSet("tt_special_stat_code", ".人工處理");
		} else if (wp.colEq("special_stat_code", "4")) {
			wp.colSet("tt_special_stat_code", ".行員");
		} else if (wp.colEq("special_stat_code", "5")) {
			wp.colSet("tt_special_stat_code", ".其他");
		}

	}

	void doIdnoExt() throws Exception {
		wp.selectSQL = "*";
		if (wp.itemEq("debit_flag", "N")) {
			wp.daoTable = "crd_idno_ext";
		} else if (wp.itemEq("debit_flag", "Y")) {
			wp.daoTable = "dbc_idno_ext";
		}
		wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("id_p_seqno"), "id_p_seqno");
		pageSelect();
		if (sqlRowNum <= 0) {
			errmsg("查無資料");
			return;
		}
	}

	void doCardSpecStatus() {
		String lsCardNo = "";
		boolean lbDebit = false;

		lsCardNo = wp.itemStr2("wk_card_no");
		lbDebit = wp.itemEq("debit_flag", "Y");

		if (!lbDebit) {
			wp.selectSQL = " hex(B.rowid) as rowid , " + " A.bin_type , " + " A.card_no , " + " A.card_type , "
					+ " A.bank_actno , "
					+ " uf_spec_status(B.spec_status,B.spec_del_date) as card_spec_status, spec_del_date,"
					+ " uf_corp_no(A.corp_p_seqno) as corp_no , "
					+ " uf_idno_id2(A.id_p_seqno,A.acct_type) as wk_idno_code , " + " B.spec_mst_vip_amt , "
					+ " B.spec_remark , " + " B.spec_user , " + " B.spec_date , "
					+ " to_char(A.mod_time ,'yyyymmdd') as mod_date , " + " A.mod_user , " + " A.mod_pgm , "
					+ " A.new_end_date , " + " uf_date_add(A.new_end_date,0,0,1) as new_end_date_add , "
					+ " 'N' as debit_flag , " + " A.group_code , "
					+ " uf_tt_group_code(A.group_code) as tt_group_code , " + " A.combo_acct_no as acct_no , "
					+ " '' as xx";
			wp.daoTable = " crd_card A , cca_card_base B ";
			wp.whereStr = " where A.card_no = B.card_no " + sqlCol(lsCardNo, "A.card_no");
		} else if (lbDebit) {
			wp.selectSQL = " hex(B.rowid) as rowid , " + " A.bin_type , " + " A.card_no , " + " A.card_type , "
					+ " A.bank_actno , " + " A.acct_no ,  "
					+ " uf_spec_status(B.spec_status,B.spec_del_date) as card_spec_status , B.spec_del_date, "
					+ " uf_corp_no(A.corp_p_seqno) as corp_no , "
					+ " uf_idno_id2(A.id_p_seqno,A.acct_type) as wk_idno_code , " + " B.spec_mst_vip_amt , "
					+ " B.spec_remark , " + " B.spec_user , " + " B.spec_date , "
					+ " to_char(A.mod_time ,'yyyymmdd') as mod_date , " + " A.mod_user , " + " A.mod_pgm , "
					+ " A.new_end_date , " + " uf_date_add(A.new_end_date,0,0,1) as new_end_date_add , "
					+ " 'Y' as debit_flag , " + " A.group_code , " + " uf_tt_group_code(A.group_code) as tt_group_code"
					;
			wp.daoTable = " dbc_card A , cca_card_base B ";
			wp.whereStr = " where A.card_no = B.card_no " + sqlCol(lsCardNo, "A.card_no");
		}

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + lsCardNo);
			return;
		}

		String lsIdCode = wp.colStr("wk_idno_code");
		wp.colSet("id_no", commString.left(lsIdCode, 10));

		selectSmsFlag(wp.colStr("card_no"));
	}

	void selectSmsFlag(String lsCardNo) {
		String sql1 = " select " + " sms_flag " + " from cca_special_visa " + " where card_no = ? ";

		sqlSelect(sql1, new Object[] { lsCardNo });

		if (sqlRowNum <= 0) {
			wp.colSet("sms_flag", "N");
			return;
		}

		wp.colSet("sms_flag", sqlStr("sms_flag"));

	}

	public void outgoingQuery(String lsCardNo) {
		OutgoingOppo ooOutgo = new OutgoingOppo();
		ooOutgo.setConn(wp);
		ooOutgo.wpCallStatus("");
		if (empty(lsCardNo))
			return;
		// -NEG-
		ooOutgo.parmClear();
		ooOutgo.p1CardNo = lsCardNo;
		ooOutgo.oppoNegId("5");
		// -VMJ-
		String lsBinType = wp.colStr("bin_type");
		String lsOppoDate = wp.colStr("oppo_date");
		// String ls_neg_reason =wp.col_ss("mst_reason_code");
		String lsVisReason = wp.colStr("vis_reason_code");
		String lsArea = "";

		ooOutgo.p4Reason = lsVisReason;
		if (eqIgno(lsBinType, "M")) {
			ooOutgo.oppoMasterReq2("5");
		} else if (eqIgno(lsBinType, "J")) {
			lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
					+ wp.colStr("vis_area_4") + wp.colStr("vis_area_5");
			ooOutgo.p5DelDate = lsOppoDate;
			ooOutgo.p7Region = lsArea;
			ooOutgo.oppoJcbReq("5");
		} else {
			lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
					+ wp.colStr("vis_area_4") + wp.colStr("vis_area_5") + wp.colStr("vis_area_6")
					+ wp.colStr("vis_area_7") + wp.colStr("vis_area_8") + wp.colStr("vis_area_9");
			ooOutgo.p5DelDate = lsOppoDate;
			if (empty(lsArea)) {
				ooOutgo.p4Reason = "41";
				ooOutgo.p7Region = "0" + commString.space(8);
			} else {
				ooOutgo.p7Region = lsArea;
			}
			ooOutgo.oppoVisaReq("5");
		}
	}

}
