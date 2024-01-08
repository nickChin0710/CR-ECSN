/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 2019-1210  V1.00.01  Alex  add queryWhere
* 109-04-27  V1.00.02  Tanwei       updated for project coding standard      *
* 109-12-23   V1.00.03  Justin         parameterize sql
* 110-01-06   V1.00.04  tanwei        修改zz開頭的變量                                                                           *
******************************************************************************/
package rskm02;

import ofcapp.BaseAction;

public class Rskp0922 extends BaseAction {

	taroko.base.CommDate commDate = new taroko.base.CommDate();

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

		if (chkStrend(wp.itemStr2("ex_log_date1"), wp.itemStr2("ex_log_date2")) == false) {
			alertErr2("登錄日期起迄錯誤");
			return;
		}

		String lsWhere = " where 1=1 and A.log_mode ='1' and A.log_type ='1' "
				+ " and A.kind_flag ='C' and A.emend_type ='4' and A.apr_date ='' and A.card_no <>'' "
				+ sqlStrend(wp.itemStr2("ex_log_date1"), wp.itemStr2("ex_log_date2"), "A.log_date")
				+ sqlCol(wp.itemStr2("ex_user"), "A.mod_user") + sqlCol(wp.itemStr2("ex_card_no"), "A.card_no");

		if (wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and B.major_id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"), "id_no") + ")";
		}

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " uf_idno_id(B.major_id_p_seqno) as major_id_no , " + " A.card_no , "
				+ " uf_card_name(A.card_no) as chi_name , " + " uf_idno_id(A.id_p_seqno) as id_no , "
				+ " A.bef_loc_amt , " + " A.aft_loc_amt , " + " A.bef_loc_cash , " + " A.aft_loc_cash , "
				+ " A.card_adj_limit , " + " A.card_adj_date1 , " + " A.card_adj_date2 , " + " A.mod_user , "
				+ " A.log_date , " + " A.sms_flag , " + " A.acno_p_seqno , " + " B.major_id_p_seqno , "
				+ " hex(A.rowid) as rowid , " + " A.son_card_flag , '' as tt_mod_branch , '' as apr_err_msg ";

		wp.daoTable = " rsk_acnolog A join crd_card B on A.card_no = B.card_no ";
		wp.whereOrder = " order by A.log_date Asc ";

		pageQuery();

		if (sqlNotFind()) {
			alertErr2("查無資料");
			return;
		}

		wp.setListCount(0);
		wp.setPageValue();
		queryAfter();
	}

	void queryAfter() {
		String sql1 = " select A.full_chi_name from gen_brn A left join sec_user B "
				+ " on A.branch = B.bank_unitno where B.usr_id = ? ";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "mod_user") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "tt_mod_branch", sqlStr("full_chi_name"));
			}
		}

	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void procFunc() throws Exception {
		int ilCnt = 0, ilOk = 0, ilErr = 0, ilSame = 0;

		rskm02.Rskp0922Func func = new rskm02.Rskp0922Func();
		func.setConn(wp);
		wp.logSql = true;
		String[] aaOpt = wp.itemBuff("opt");
		String[] lsSonCardFlag = wp.itemBuff("son_card_flag");
		String[] ldAftLocAmt = wp.itemBuff("aft_loc_amt");
		String[] ldAftLocCash = wp.itemBuff("aft_loc_cash");
		String[] ldCardAdjLimit = wp.itemBuff("card_adj_limit");
		String[] lsCardAdjDate1 = wp.itemBuff("card_adj_date1");
		String[] lsCardAdjDate2 = wp.itemBuff("card_adj_date2");
		String[] lsCardNo = wp.itemBuff("card_no");
		String[] lsSmsFlag = wp.itemBuff("sms_flag");
		String[] lsModUser = wp.itemBuff("mod_user");
		String[] lsRowid = wp.itemBuff("rowid");
		String[] lsAcnoPSeqno = wp.itemBuff("acno_p_seqno");
		String[] lsMajorIdPSeqno = wp.itemBuff("major_id_p_seqno");

		wp.listCount[0] = wp.itemRows("card_no");
		if (optToIndex(aaOpt[0]) < 0) {

			alertErr2("請點選覆核資料");
			return;
		}

		for (int ii = 0; ii < aaOpt.length; ii++) {
			int rr = optToIndex(aaOpt[ii]);
			if (rr < 0) {
				continue;
			}
			ilCnt++;
			if (eqIgno(wp.loginUser, lsModUser[rr])) {
				ilSame++;
				optOkflag(rr, 0);
				continue;
			}

			func.varsSet("son_card_flag", lsSonCardFlag[rr]);
			func.varsSet("aft_loc_amt", ldAftLocAmt[rr]);
			func.varsSet("aft_loc_cash", ldAftLocCash[rr]);
			func.varsSet("card_adj_limit", ldCardAdjLimit[rr]);
			func.varsSet("card_adj_date1", lsCardAdjDate1[rr]);
			func.varsSet("card_adj_date2", lsCardAdjDate2[rr]);
			func.varsSet("card_no", lsCardNo[rr]);
			func.varsSet("mod_user", lsModUser[rr]);
			func.varsSet("rowid", lsRowid[rr]);
			func.varsSet("sms_flag", lsSmsFlag[rr]);
			func.varsSet("acno_p_seqno", lsAcnoPSeqno[rr]);
			func.varsSet("major_id_p_seqno", lsMajorIdPSeqno[rr]);

			rc = func.dataProc();
			sqlCommit(rc);
			optOkflag(rr, rc);
			if (rc == 1) {
				ilOk++;
			} else {
				wp.colSet(rr,"apr_err_msg", func.getMsg());
				ilErr++;
			}
		}

		if (ilCnt == 0) {
			errmsg("請勾選欲覆核資料");
			return;
		}

		alertMsg("資料覆核完成, 成功:" + ilOk + " , 失敗:" + ilErr + " ,不可覆核:" + ilSame);

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		String lsDate1 = "";
		if (empty(strAction)) {
			lsDate1 = commDate.dateAdd(getSysDate(), 0, 0, -5);
			wp.colSet("ex_log_date1", lsDate1);
			wp.colSet("ex_log_date2", getSysDate());
		}

	}

}
