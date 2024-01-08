/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package rskm02;
/**
 * 子卡限額參數維護
 * 2019-1224:  Alex  add indiv_inst_lmt
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;

public class Rskm0922 extends BaseAction {
	String cardNo = "";
	busi.func.CcasFunc ooCcas = null;

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
		} else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			procFunc();
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
		try {
			if (eqIgno(wp.respHtml, "rskm0922_detl")) {
				wp.optionKey = wp.colStr(0, "sms_parm");
				dddwList("dddw_sms_parm", "sms_msg_id", "msg_pgm", "msg_pgm", "where msg_pgm like 'RSKM0922%' ");
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		if (wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_card_no")) {
			alertErr2("正卡人ID , 子卡卡號 不可全部空白 ");
			return;
		}

		String lsWhere = " where 1=1 " + " and A.card_no = B.card_no and A.current_code = '0' "
		// + " and A.son_card_flag ='Y' "
				+ sqlCol(wp.itemStr("ex_card_no"), "A.card_no");

		if (!wp.itemEmpty("ex_idno")) {
			lsWhere += " and A.major_id_p_seqno in (select C.id_p_seqno from crd_idno C where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"), "C.id_no") + ") ";
		}

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " uf_idno_id(A.major_id_p_seqno) as major_id_no , " + " A.card_no , "
				+ " uf_idno_name(A.id_p_seqno) as chi_name , " + " uf_idno_id(A.id_p_seqno) as id_no , "
				+ " A.indiv_crd_lmt , " + " A.indiv_inst_lmt , " + " B.card_adj_limit , " + " B.card_adj_date1 , "
				+ " B.card_adj_date2 , " + " B.adj_chg_user , "
				+ " decode(B.card_adj_limit,0,'',B.adj_chg_user) as adj_chg_user " + ", A.son_card_flag";

		wp.daoTable = " crd_card A , cca_card_base B ";

		pageQuery();
		if (sqlNotFind()) {
			alertErr2("此條件查無資料 !");
			return;
		}
		wp.setListCount(0);
		wp.setPageValue();

		queryAfter();

	}

	void queryAfter() {
		int ilSelectCnt = 0;
		ilSelectCnt = wp.selectCnt;
		String sql1 = " select apr_flag " + " from rsk_acnolog where kind_flag ='C' and apr_flag <>'Y' and card_no = ? "
				+ " and log_mode ='1' and log_type ='1' and emend_type ='4' ";
		// --確認此筆是否待覆核
		for (int ii = 0; ii < ilSelectCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "card_no") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "apr_flag", "Y");
			}

		}

	}

	@Override
	public void querySelect() throws Exception {
		cardNo = wp.itemStr("data_k1");
		dataRead();

	}

	@Override
	public void dataRead() throws Exception {
		if (empty(cardNo))
			cardNo = itemkk("card_no");
		if (empty(cardNo)) {
			alertErr2("卡號: 不可空白");
			return;
		}
		wp.selectSQL = " A.card_no , " + " uf_idno_id(A.major_id_p_seqno) as major_id_no , "
				+ " uf_idno_name(A.id_p_seqno) as chi_name , " + " uf_idno_id(A.id_p_seqno) as id_no , "
				+ " A.indiv_crd_lmt , " + " A.indiv_inst_lmt , " + " A.indiv_inst_lmt as ori_indiv_inst_lmt , "
				+ " B.card_adj_limit , " + " B.card_adj_date1 , " + " B.card_adj_date2 , " + " A.acno_p_seqno , "
				+ " A.acct_type , " + " A.corp_p_seqno , " + " A.id_p_seqno , " + " A.major_id_p_seqno , "
				+ " decode(A.son_card_flag,'Y',A.son_card_flag,'N') as son_card_flag"
				+ ", A.indiv_crd_lmt as new_indiv_crd_lmt" + ", 'N' as apr_flag , B.card_acct_idx ";

		wp.daoTable = " crd_card A join cca_card_base B on B.card_no=A.card_no";
		wp.whereStr = " where 1=1 " + sqlCol(cardNo, "A.card_no");
		pageSelect();
		if (this.sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}
		dataReadAfter();
	}

	void dataReadAfter() {
		String sql1 = " select " + " cellar_phone " + " from crd_idno " + " where id_no = ? ";

		sqlSelect(sql1, new Object[] { wp.colStr("id_no") });

		if (sqlRowNum > 0) {
			wp.colSet("cellar_phone", sqlStr("cellar_phone"));
		}

		String sql2 = " select " + " A.line_of_credit_amt , " + " B.tot_amt_month , " + " B.adj_eff_start_date , "
				+ " B.adj_eff_end_date " + " from act_acno A join cca_card_acct B on A.acno_p_seqno=B.acno_p_seqno  "
				+ " where A.acno_p_seqno = ? " + " and B.debit_flag <> 'Y' ";

		sqlSelect(sql2, new Object[] { wp.colStr("acno_p_seqno") });

		if (sqlRowNum > 0) {
			wp.colSet("line_of_credit_amt", sqlStr("line_of_credit_amt"));
			wp.colSet("tot_amt_month", sqlStr("tot_amt_month"));
			wp.colSet("adj_eff_start_date", sqlStr("adj_eff_start_date"));
			wp.colSet("adj_eff_end_date", sqlStr("adj_eff_end_date"));
		}
		
		//--預設分期百分比為100
		if(wp.colNum("indiv_inst_lmt") == 0) {
			wp.colSet("indiv_inst_lmt", 100);
		}
		
		// --確認是否是未覆核
		String sql3 = " select aft_loc_amt , card_adj_limit , card_adj_date1 , card_adj_date2 , son_card_flag ,"
				+ " sms_flag , aft_loc_cash " + " from rsk_acnolog where kind_flag ='C' and apr_flag <>'Y' and card_no = ? "
				+ " and log_mode ='1' and log_type ='1' and emend_type ='4' ";

		sqlSelect(sql3, new Object[] { wp.colStr("card_no") });

		if (sqlRowNum > 0) {
			wp.respMesg = "此筆資料待覆核...";
			wp.colSet("apr_flag", "Y");
			wp.colSet("new_indiv_crd_lmt", sqlStr("aft_loc_amt"));
			wp.colSet("card_adj_limit", sqlStr("card_adj_limit"));
			wp.colSet("card_adj_date1", sqlStr("card_adj_date1"));
			wp.colSet("card_adj_date2", sqlStr("card_adj_date2"));
			wp.colSet("son_card_flag", sqlStr("son_card_flag"));
			wp.colSet("sms_flag", sqlStr("sms_flag"));
			wp.colSet("indiv_inst_lmt", sqlStr("aft_loc_cash"));
		}
		
		//-- 溢繳款
		String sql4 = "select jrnl_bal from cca_card_acct where card_acct_idx = ? ";
		sqlSelect(sql4,new Object[] {wp.colInt("card_acct_idx")});
		
		if(sqlRowNum > 0 ) {
			if(sqlInt("jrnl_bal") > 0) {
				wp.colSet("over_pay", 0);
			}	else	{
				wp.colSet("over_pay", sqlInt("jrnl_bal") * -1);
			}
		}
		
	}

	@Override
	public void saveFunc() throws Exception {

		ooCcas = new busi.func.CcasFunc();
		ooCcas.setConn(wp);

		//--移至 Rskm0922Func.dataCheck
//		if (ooCcas.limitApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"),
//				wp.itemNum("card_adj_limit")) != 1) {
//			alertErr(ooCcas.getMsg());
//			return;
//		}

		rskm02.Rskm0922Func func = new rskm02.Rskm0922Func();
		func.setConn(wp);

		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if (rc != 1) {
			errmsg(func.getMsg());
		} else
			clearFunc();

	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		btnModeAud("XX");

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
