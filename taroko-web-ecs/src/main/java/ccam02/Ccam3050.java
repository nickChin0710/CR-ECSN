package ccam02;
/* 
 * ccam02.ccam3050 特店風險註記維護
 * V00.00	XX		2017-0803: initial
 * V00.01   Alex  2019-1120: mcht option fixed
 * V00.02   Alex  2019-1230: ajax add no limit
 * V00.03   yanghan  2020-0420: 修改了變量名稱和方法名稱
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名
 * 2023-1219   JH    acq_bank_id.edit
 * */

import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam3050 extends BaseEdit {
	Ccam3050Func func;
	String mchtNo = "", bankId = "", mccCode = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
			wp.colSet("risk_factor", "0");
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "R2")) {
			// -資料讀取-
			strAction = "R2";
			ccam3050Card();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "U2")) {
			/* 更新功能 */
			ccam3050CardUpdate();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {
			/* TEST */
			strAction = "AJAX";
			processAjaxOption();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void dddwSelect() {
		try {
			if (wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = wp.colStr("mcht_risk_code");
				dddwList("dddw_mcht_risk_code", "cca_mcht_risk_level", "mcht_risk_code", "risk_remark", "where 1=1");
			}
		} catch (Exception ex) {
		}

//		try {
//			if (wp.respHtml.indexOf("_detl") > 0) {
//				wp.optionKey = wp.colStr("kk_acq_bank_id");
//				dddwList("dddw_mbase_acqid", "ptr_sys_idtab", "wf_id", "wf_desc",
//						"where wf_type='ACQ_BANK_ID' and wf_id <> '999999'");
//			}
//		} catch (Exception ex) {
//		}

//		try {
//			if (eqIgno(wp.respHtml, "ccam3050")) {
//				wp.optionKey = wp.colStr(0, "ex_acq_bank_id");
//				dddwList("dddw_mbase_acqid", "ptr_sys_idtab", "wf_id", "wf_desc",
//						"where wf_type='ACQ_BANK_ID' and wf_id <> '999999'");
//			}
//		} catch (Exception ex) {
//		}

	}

	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_mcht_no"), "A.mcht_no", "like%")
				+ sqlCol(wp.itemStr("ex_mcc_code"), "A.mcc_code")
				+ sqlCol(wp.itemStr("ex_acq_bank_id"), "A.acq_bank_id");
		if (wp.itemEq("ex_auth_flag", "Y")) {
			wp.whereStr += " and A.risk_end_date < to_char(sysdate,'yyyymmdd') ";
		}
		if (wp.itemEq("ex_auth_flag", "N")) {
			wp.whereStr += " and A.risk_start_date<= to_char(sysdate,'yyyymmdd') and A.risk_end_date >= to_char(sysdate,'yyyymmdd') ";
		}
		if (wp.itemEmpty("ex_card_no") == false) {
			wp.whereStr += " and (A.mcht_no,A.acq_bank_id,A.mcc_code) in (select mcht_no , acq_bank_id , mcc_code from cca_mcht_risk_detl where data_code = ?)";
			setString2(1, wp.itemStr("ex_card_no"));
		}

		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = "A.acq_bank_id," + "A.mcht_no, " + "A.risk_start_date , " + "A.risk_end_date , "
				+ "A.mcht_risk_code , " + "A.auth_amt_s , " + "A.auth_amt_e , " + "A.auth_amt_rate,  "
				+ "A.edc_pos_no1,  " + "A.edc_pos_no2,  " + "A.edc_pos_no3,  " + "A.risk_chg_date,  "
				+ "A.risk_chg_user," + "A.risk_factor," + "A.mod_user," + "to_char(A.mod_time,'yyyymmdd') as mod_date,"
				+ "A.mcc_code," + "A.mcht_name";
		wp.daoTable = "cca_mcht_risk A  ";
		wp.whereOrder = " order by A.mcht_no,A.acq_bank_id ";

		pageQuery();
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		wp.setListCount(1);
		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		mchtNo = wp.itemStr("data_k1");
		bankId = wp.itemStr("data_k2");
		mccCode = wp.itemStr("data_k3");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (empty(mchtNo)) {
			mchtNo = itemKk("mcht_no");
		}
		if (empty(bankId)) {
			bankId = itemKk("acq_bank_id");
		}
		if (empty(mccCode)) {
			mccCode = itemKk("mcc_code");
		}

		wp.selectSQL = " hex(A.rowid) as rowid ," + " A.mod_seqno ," + " A.mcht_no ," + " A.acq_bank_id ,"
				+ " A.mcht_name ," + " B.zip_code ," + " B.zip_city ," + " B.mcht_addr ,"
				// + " B.tel_no ,"
				// + " B.contr_type ,"
				// + " B.risk_flag ,"
				+ " A.mcht_risk_code ," + " A.risk_start_date ," + " A.risk_end_date ," + " A.risk_remark ,"
				+ " A.mcc_code ," + " A.mcc_code as old_mcc_code , " + " A.auth_amt_s ," + " A.auth_amt_e ,"
				+ " A.auth_amt_rate ," + " A.edc_pos_no1 ," + " A.edc_pos_no2 ," + " A.edc_pos_no3 ,"
				+ " A.day_limit_cnt ," + " A.day_tot_amt ," + " A.risk_factor ,"
				+ " to_char(A.mod_time,'yyyymmdd') as mod_date ," + " A.mod_user ," + " A.crt_date ," + " A.crt_user ";
		wp.daoTable = " cca_mcht_risk A left join cca_mcht_bill B on A.mcht_no = B.mcht_no and A.acq_bank_id = B.acq_bank_id  ";
		wp.whereStr = " where 1=1" + sqlCol(mchtNo, "A.mcht_no") + sqlCol(bankId, "A.acq_bank_id")
				+ sqlCol(mccCode, "A.mcc_code");

		pageSelect();
		if (sqlRowNum <= 0) {
			alertErr("查無資料,請新增 !");
			readBaseData();
			return;
		}
	}

	void readBaseData() {
		wp.selectSQL = " mcht_no , " + " mcht_name , " + " acq_bank_id , " + " zip_code , " + " zip_city , "
				+ " mcht_addr  ";
//    	+ " contr_type , " + " tel_no "
		wp.daoTable = "cca_mcht_bill";
		wp.whereStr = " where 1=1 " + sqlCol(mchtNo, "mcht_no") + sqlCol(bankId, "acq_bank_id");

		pageSelect();

		if (sqlRowNum <= 0) {
			alertErr2("查無特店資料");
			return;
		}
	}

	@Override
	public void saveFunc() throws Exception {
		func = new Ccam3050Func();
		func.setConn(wp);

		if (isUpdate() || isDelete()) {
			if (checkApproveZz() == false)
				return;
		}

		rc = func.dbSave(strAction);
		if (rc != 1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc);
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}

	}

	void ccam3050Card() throws Exception {
		wp.pageRows = 999;
		mchtNo = wp.itemStr("mcht_no");
		bankId = wp.itemStr("acq_bank_id");
		mccCode = wp.itemStr("mcc_code");
		wp.selectSQL = "hex(rowid) as rowid," + "mcht_no," + "acq_bank_id," + "data_type," + "data_code as db_card_no,"
				+ "data_code2 as auth_date1," + "data_code3 as auth_date2," + "data_amt as card_tot_amt," + "mod_user,"
				+ "to_char(mod_time,'yyyymmdd') as mod_date," + "mcc_code, " + "'U' as ls_type ";

		wp.daoTable = "CCA_MCHT_RISK_DETL";
		wp.whereStr = " where 1=1 and data_type ='1'" + sqlCol(mchtNo, "mcht_no") + sqlCol(bankId, "acq_bank_id")
				+ sqlCol(mccCode, "mcc_code");

		wp.whereOrder = " order by mcht_no";
		pageQuery();
		if (sqlRowNum <= 0) {
			wp.notFound = "N";
		}
		wp.setListCount(1);
		// wp.ddd("call-log="+wp.selectCnt+", sqlnrow="+this.sql_nrow);
		wp.colSet("IND_NUM", "" + wp.selectCnt);
		wp.colSet("ex_sysdate", getSysDate());
	}

	void ccam3050CardUpdate() throws Exception {
		int isFail = 0, isRepetition = 0, isExcit = 0;
		int ii = 0;
		// String ls_opt="";

		func = new Ccam3050Func();
		func.setConn(wp);

		String[] rowidArray = wp.itemBuff("rowid");
		String[] optArray = wp.itemBuff("opt");
		String[] cardNoArray = wp.itemBuff("db_card_no");
		String[] authDateArray = wp.itemBuff("auth_date1");
		String[] authDateArray2 = wp.itemBuff("auth_date2");
		String[] cardTotAmtArray = wp.itemBuff("card_tot_amt");
		String[] lsType = wp.itemBuff("ls_type");
		// String[] aa_olddata = wp.item_buff("old_data");
		wp.listCount[0] = cardNoArray.length;
		wp.colSet("IND_NUM", "" + cardNoArray.length);

		// -check duplication-
		ii = -1;
		for (String parm : cardNoArray) {
			ii++;
			wp.colSet(ii, "ok_flag", "");
			// -option-ON-
			if (checkBoxOptOn(ii, optArray)) {
				cardNoArray[ii] = "";
				continue;
			}

			if (ii != Arrays.asList(cardNoArray).indexOf(parm)) {
				wp.colSet(ii, "ok_flag", "!");
				isRepetition++;
			}
		}
		if (isRepetition > 0) {
			alertErr("資料值重複: " + isRepetition);
			return;
		}

		for (int row = 0; row < wp.itemRows("db_card_no"); row++) {
			if (checkBoxOptOn(row, optArray))
				continue;
			if (eqIgno(lsType[row], "U"))
				continue;

			if (checkCard(cardNoArray[row]) == false) {
				isExcit++;
				wp.colSet(row, "ok_flag", "X");
				continue;
			}
		}

		if (isExcit > 0) {
			alertErr("卡片不存在 or 非有效卡 :" + isExcit);
			return;
		}

		// -delete no-approve-
		if (func.dbDeleteCard() < 0) {
			alertErr(func.getMsg());
			return;
		}

		for (int ll = 0; ll < cardNoArray.length; ll++) {
			wp.colSet(ll, "ok_flag", "");
			if (empty(cardNoArray[ll])) {
				continue;
			}
			// -option-ON-
			if (checkBoxOptOn(ll, optArray)) {
				continue;
			}

			func.varsSet("db_card_no", cardNoArray[ll]);
			func.varsSet("auth_date1", authDateArray[ll]);
			func.varsSet("auth_date2", authDateArray2[ll]);
			func.varsSet("card_tot_amt", cardTotAmtArray[ll]);
			if (func.dbInsertDetl() == 1) {
				isFail++;
			} else {
				isRepetition++;
			}
		}

		if (isFail > 0) {
			sqlCommit(1);
		}

		alertMsg("資料存檔處理完成");
		ccam3050Card();
	}

	boolean checkCard(String cardNo) {

		if (empty(cardNo))
			return false;

		String sql1 = " select current_code from crd_card where card_no = ? " + " union all "
				+ " select current_code from dbc_card where card_no = ? ";

		sqlSelect(sql1, new Object[] { cardNo, cardNo });

		if (sqlRowNum <= 0 || !eqIgno(sqlStr("current_code"), "0"))
			return false;

		return true;

	}

	public void processAjaxOption() throws Exception {
		if (wp.itemEmpty("kk_mcht_no3")) {
			selectNoLimit();
			wp.varRows = 200;
			wp.selectSQL = "mcht_no , decode(mcht_name,'',mcht_eng_name,mcht_name) AS mcht_name "
         +", acq_bank_id";
			wp.daoTable = "cca_mcht_bill";
			wp.whereStr = "where 1=1 " + sqlCol(wp.itemStr("kk_mcht_no2"), "mcht_no", "like%");
         String ls_bankId=wp.itemStr("kk_acq_bank_id");
         if (ls_bankId.length()==6 || ls_bankId.length()==8) {
            wp.whereStr +=sqlCol(ls_bankId,"acq_bank_id");
         }
			wp.whereOrder ="order by mcht_no "
             +commSqlStr.rownum(200); //"fetch first 200 rows only";
			pageQuery();
			for (int i = 0; i < wp.selectCnt; i++) {
				wp.addJSON("OPTION_TEXT", wp.colStr(i, "mcht_no")
               +"_"+wp.colStr(i,"acq_bank_id")
                + "_" + wp.colStr(i, "mcht_name"));
				wp.addJSON("OPTION_VALUE", wp.colStr(i, "mcht_no"));
			}
		}
		else if(wp.itemEmpty("kk_mcht_no3")==false) {
			selectNoLimit();
			wp.varRows = 200;
			wp.selectSQL = "acq_bank_id";
			wp.daoTable = "cca_mcht_bill";
			wp.whereStr = "where 1=1 " + sqlCol(wp.itemStr("kk_mcht_no3"), "mcht_no");
			pageQuery();
			for (int i = 0; i < wp.selectCnt; i++) {
				wp.addJSON("OPTION_TEXT2", wp.colStr(i, "acq_bank_id"));
				wp.addJSON("OPTION_VALUE2", wp.colStr(i, "acq_bank_id"));
			}
		}

		return;
	}

	@Override
	public void initPage() {
		if (eqIgno(strAction, "new")) {
			wp.colSet("risk_start_date", getSysDate());
			wp.colSet("risk_end_date", getSysDate());
		}
	}

}
