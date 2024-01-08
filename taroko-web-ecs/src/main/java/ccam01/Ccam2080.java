package ccam01;

/** 單筆開卡(card_open_single)
 * 19-1210:    Alex  add initButton
 * 19-0611:    JH    p_seqno >>acno_p_xxx
 * V.2018-0506-JH
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 111-04-07 V1.00.01  Justin           select activate_date and old_activate_date
 * */

import ofcapp.BaseAction;

public class Ccam2080 extends BaseAction {
	String cardNo = "", debitFlag = "", lsWhere1 = "", lsWhere2 = "";

	@Override
	public void userAction() throws Exception {
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
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
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
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			procFunc();
		}

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_idno")) {
			alertErr2("[卡號,身分證ID] 不可同空白");
			return;
		}

//		getWhere();

		wp.setQueryMode();

		queryRead();

	}

	void getWhere() {
		lsWhere1 = " where 1=1 ";
		lsWhere2 = " where 1=1 ";
		if (!empty(wp.itemStr("ex_card_no"))) {
			lsWhere1 += sqlCol(wp.itemStr2("ex_card_no"), "card_no");
			lsWhere2 += sqlCol(wp.itemStr2("ex_card_no"), "card_no");
		} else if (!empty(wp.itemStr("ex_idno"))) {
//			lsWhere1 += commSqlStr.inIdnoCrd("id_p_seqno", wp.itemStr2("ex_idno"), "");
//			lsWhere2 += commSqlStr.inIdnoDbc("id_p_seqno", wp.itemStr2("ex_idno"), "");
			lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"id_no")+") ";
			lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"id_no")+") ";
		}

		if (wp.itemEq("ex_new_old", "N")) {
			lsWhere1 += "";
			lsWhere2 += "";
		} else if (wp.itemEq("ex_new_old", "O")) {
			lsWhere1 += "";
			lsWhere2 += "";
		}

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		getWhere();

		wp.sqlCmd = " select " + " card_no , " + " current_code , " + " 'N' as debit_flag , " + " card_type , "
				+ " new_end_date , " + " activate_flag , " + " acct_type , " + " combo_indicator , "
				+ " combo_acct_no , " + " 'N' as db_mob_card , " + " group_code , " + " acno_p_seqno , "
				+ " old_end_date , " + " old_activate_flag , unit_code " + " from crd_card " + lsWhere1 + " union "
				+ " select " + " card_no , " + " current_code , " + " 'Y' as debit_flag , " + " card_type , "
				+ " new_end_date , " + " activate_flag , " + " acct_type , " + " '' as combo_indicator , "
				+ " acct_no as combo_acct_no , " + " 'N' as db_mob_card , " + " group_code , "
				+ " p_seqno as acno_p_seqno , " + " old_end_date , " + " old_activate_flag , unit_code "
				+ " from dbc_card " + lsWhere2 + " order by current_code , card_no ";

		wp.pageCountSql = " select count(*) from " + " (select distinct card_no from crd_card " + lsWhere1 + ""
				+ " union " + " select distinct card_no from dbc_card " + lsWhere2 + "" + " ) ";

		pageQuery();

		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}
		wp.setListCount(1);
		listWkdata(wp.selectCnt);
		colReadOnly("cond_edit");
		wp.setPageValue();
	}

	void listWkdata(int llRow) {

		String sql1 = " select " + " block_reason1||','||" + " block_reason2||','||" + " block_reason3||','||"
				+ " block_reason4||','||" + " block_reason5 as wk_block_reason , " + " spec_status as acct_spec_status "
				+ " from cca_card_acct " + " where uf_nvl(debit_flag,'N') = ? " + " and acno_p_seqno = ? ";

		String sql2 = " select " + " spec_status as card_spec_status " + " from cca_card_base " + " where card_no = ? ";

		String sql3 = " select count(*) as ic_cnt from crd_item_unit where card_type = ? and unit_code = ? and ic_kind in ('B','C') ";
		
		String sql4 = " select open_type from cca_card_open where card_no = ? and new_end_date = ? ";
		
		for (int ii = 0; ii < llRow; ii++) {
			if (wp.colEq(ii, "activate_flag", "1")) {
				wp.colSet(ii, "tt_activate_flag", "未開");
			} else if (wp.colEq(ii, "activate_flag", "2")) {
				wp.colSet(ii, "tt_activate_flag", "已開");
			}

			if (wp.colEq(ii, "old_activate_flag", "1")) {
				wp.colSet(ii, "tt_old_active_flag", "未開");
			} else if (wp.colEq(ii, "old_activate_flag", "2")) {
				wp.colSet(ii, "tt_old_active_flag", "已開");
			}

			sqlSelect(sql1, new Object[] { wp.colStr(ii, "debit_flag"), wp.colStr(ii, "acno_p_seqno") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "wk_block_reason", sqlStr("wk_block_reason"));
				wp.colSet(ii, "acct_spec_status", sqlStr("acct_spec_status"));
			}

			sqlSelect(sql2, new Object[] { wp.colStr(ii, "card_no") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "card_spec_status", sqlStr("card_spec_status"));
			}

			sqlSelect(sql3, new Object[] { wp.colStr(ii, "card_type"), wp.colStr(ii, "unit_code") });
			if (sqlRowNum > 0 && sqlNum("ic_cnt") > 0) {
				wp.colSet(ii, "db_mob_card", "Y");
			}
			
			sqlSelect(sql4, new Object[] {wp.colStr(ii,"card_no"),wp.colStr(ii,"new_end_date")});
			if (sqlRowNum > 0) {
				wp.colSet(ii, "new_open_type", sqlStr("open_type"));
				if (wp.colEq(ii,"new_open_type", "O")) {
					wp.colSet(ii,"tt_new_open_type", "人工");
				} else if (wp.colEq(ii,"new_open_type", "V")) {
					wp.colSet(ii,"tt_new_open_type", "語音");
				}
			}
			
		}
	}

	@Override
	public void querySelect() throws Exception {
		cardNo = wp.itemStr("data_k1");
		debitFlag = wp.itemStr("data_k2");
		dataRead();

	}

	@Override
	public void dataRead() throws Exception {
		if (empty(cardNo)) {
			cardNo = wp.itemStr("card_no");
		}
		if (empty(debitFlag)) {
			debitFlag = wp.itemStr("debit_flag");
		}
		if (debitFlag.equals("N")) {
			wp.selectSQL = "" + " card_no , " + " 'N' as debit_flag , " + " card_type , " + " new_end_date , "
					+ " old_end_date , " + " old_activate_flag , " + " old_card_no , " + " acct_type , "
					+ " combo_indicator , " + " activate_flag , " + " activate_date , " + " activate_type , "
					+ " acno_p_seqno," + " sup_flag," + " current_code," + " new_beg_date," + " old_beg_date,"
					+ " id_p_seqno," + " group_code," + " combo_acct_no, " + " old_activate_date ";
			wp.daoTable = "crd_card";
			wp.whereStr = "where 1=1" + sqlCol(cardNo, "card_no");
		} else {
			wp.selectSQL = "" + " card_no , " + " 'Y' as debit_flag , " + " card_type , " + " new_end_date , "
					+ " old_end_date , " + " old_activate_flag , " + " old_card_no , " + " acct_type , "
					+ " '' as combo_indicator , " + " activate_flag , " + " activate_date , " + " activate_type , "
					+ " p_seqno as acno_p_seqno," + " sup_flag," + " current_code," + " new_beg_date,"
					+ " old_beg_date," + " id_p_seqno," + " group_code," + " acct_no as combo_acct_no," + " old_activate_date";
			wp.daoTable = "dbc_card";
			wp.whereStr = "where 1=1" + sqlCol(cardNo, "card_no");
		}

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + cardNo);
			return;
		}
		wp.colSet("kk_new_old", "N");
		dataReadAfter();
		this.colReadOnly("kk_edit");
	}

	void sql2Col(String col1, String col2) {
		wp.colSet(col2, sqlStr(col1));
	}

	void dataReadAfter() {
		wp.sqlCmd = "select id_no ," + " chi_name, birthday , "
				+ " home_area_code1||'-'||home_tel_no1||'-'||home_tel_ext1 as tel_home , "
				+ " office_area_code1||'-'||office_tel_no1||'-'||office_tel_ext1 as tel_offi ";
		if (debitFlag.equals("N"))
			wp.sqlCmd += " from crd_idno ";
		else
			wp.sqlCmd += " from dbc_idno ";
		wp.sqlCmd += " where 1=1" + sqlCol(wp.colStr("id_p_seqno"), "id_p_seqno");
		daoTid = "A.";
		sqlSelect();
		if (sqlRowNum > 0) {
			sql2Col("A.id_no", "id_no");
			sql2Col("A.chi_name", "chi_name");
			sql2Col("A.birthday", "birthday");
			sql2Col("A.tel_home", "tel_home");
			sql2Col("A.tel_offic", "tel_offi");
		}

		wp.sqlCmd = "select B.spec_status, B.spec_del_date"
				+ ", A.bill_sending_zip||'  '||A.bill_sending_addr1||A.bill_sending_addr2||A.bill_sending_addr3||A.bill_sending_addr4||A.bill_sending_addr5 as bill_addr "
				+ " from act_acno A left join cca_card_acct B on A.acno_p_seqno =B.acno_p_seqno and B.debit_flag<>'Y'"
				+ " where 1=1" + sqlCol(wp.colStr("acno_p_seqno"), "A.acno_p_seqno");

		daoTid = "B.";
		sqlSelect();
		if (sqlRowNum > 0) {
			sql2Col("B.spec_status", "spec_status");
			sql2Col("B.bill_addr", "bill_addr");
		}
		this.selectOK();

		if (wp.colEq("activate_flag", "1")) {
			wp.colSet("tt_activate_flag", ".未開");
		} else if (wp.colEq("activate_flag", "2")) {
			wp.colSet("tt_activate_flag", ".已開");
		}

		if (wp.colEq("old_activate_flag", "1")) {
			wp.colSet("tt_old_active_flag", ".未開");
		} else if (wp.colEq("old_activate_flag", "2")) {
			wp.colSet("tt_old_active_flag", ".已開");
		}

		String sql1 = " select " + " block_reason1||','||" + " block_reason2||','||" + " block_reason3||','||"
				+ " block_reason4||','||" + " block_reason5 as wk_block_reason , "
				+ " spec_status as acct_spec_status , "
				+ " block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as hi_block_reason "
				+ " from cca_card_acct " + " where uf_nvl(debit_flag,'N') = ? " + " and acno_p_seqno = ? ";

		String sql2 = " select " + " spec_status as card_spec_status " + " from cca_card_base " + " where card_no = ? ";

		sqlSelect(sql1, new Object[] { wp.colStr("debit_flag"), wp.colStr("acno_p_seqno") });
		if (sqlRowNum > 0) {
			wp.colSet("wk_block_reason", sqlStr("wk_block_reason"));
			wp.colSet("acct_spec_status", sqlStr("acct_spec_status"));
			wp.colSet("hi_block_reason", sqlStr("hi_block_reason"));
		}

		sqlSelect(sql2, new Object[] { wp.colStr("card_no") });
		if (sqlRowNum > 0) {
			wp.colSet("card_spec_status", sqlStr("card_spec_status"));
		}

		String sql3 = " select " + " open_date , " + " open_time , " + " open_user , " + " open_type "
				+ " from cca_card_open " + " where card_no = ? " + " and new_end_date = ? ";

		sqlSelect(sql3, new Object[] { wp.colStr("card_no"), wp.colStr("new_end_date") });
		if (sqlRowNum > 0) {
			wp.colSet("new_open_date", sqlStr("open_date"));
			wp.colSet("new_open_time", sqlStr("open_time"));
			wp.colSet("new_open_user", sqlStr("open_user"));
			wp.colSet("new_open_type", sqlStr("open_type"));
		}

		if (!wp.colEmpty("old_end_date")) {
			if (wp.colEmpty("old_card_no")) {
				log("A:" + wp.colStr("card_no") + "   " + wp.colStr("old_end_date"));
				sqlSelect(sql3, new Object[] { wp.colStr("card_no"), wp.colStr("old_end_date") });
			} else {
				log("B:" + wp.colStr("old_card_no") + "   " + wp.colStr("old_end_date"));
				sqlSelect(sql3, new Object[] { wp.colStr("old_card_no"), wp.colStr("old_end_date") });
			}
			if (sqlRowNum > 0) {
				wp.colSet("old_open_date", sqlStr("open_date"));
				wp.colSet("old_open_time", sqlStr("open_time"));
				wp.colSet("old_open_user", sqlStr("open_user"));
				wp.colSet("old_open_type", sqlStr("open_type"));
			}
		}

		if (wp.colEq("new_open_type", "O")) {
			wp.colSet("tt_new_open_type", "人工");
		} else if (wp.colEq("new_open_type", "V")) {
			wp.colSet("tt_new_open_type", "語音");
		}

		if (wp.colEq("old_open_type", "O")) {
			wp.colSet("tt_old_open_type", "人工");
		} else if (wp.colEq("old_open_type", "V")) {
			wp.colSet("tt_old_open_type", "語音");
		}

	}

	/*
	 * int save_Check() { if (wp.item_eq("conf_1","Y")) return 1;
	 * 
	 * wp.col_set("js_confirm","|| 1==1"); wp.respMesg ="開卡核身確認!!!"; return 0; }
	 */
	@Override
	public void saveFunc() throws Exception {

		ccam01.Ccam2080Func func = new ccam01.Ccam2080Func();
		func.setConn(wp);

		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if (rc != 1) {
			errmsg(func.getMsg());
			return;
		}

		this.saveAfter(false);
		wp.respMesg = "開卡成功";
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		if (eqIgno(wp.respHtml, "ccam2080_detl")) {
			btnModeAud("XX");
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
