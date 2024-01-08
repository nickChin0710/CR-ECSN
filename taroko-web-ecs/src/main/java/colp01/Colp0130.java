/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Zhanghuheng     updated for project coding standard *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
* 112-10-24  V1.00.04   Ryan           增加覆核權限檢核                                                                              *     
******************************************************************************/
package colp01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colp0130 extends BaseProc {
	CommString commString = new CommString();
	Colp0130Func func;

	String dataKK1 = "";
	int ilOk = 0;
	int ilErr = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		// ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
		// wp.respCode + ",rHtml=" + wp.respHtml);
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			dataProcess();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
			// } else if (eq_igno(wp.buttonCode, "A")) {
			// /* 新增功能 */
			// insertFunc();
			// } else if (eq_igno(wp.buttonCode, "U")) {
			// /* 更新功能 */
			// updateFunc();
			// } else if (eq_igno(wp.buttonCode, "D")) {
			// /* 刪除功能 */
			// deleteFunc();
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
		}

		// dddw_select();
		initButton();
	}

	@Override
	public void dddwSelect() {

	}

	private boolean getWhereStr() throws Exception {
		wp.whereStr = "where 1=1 ";

		if (empty(wp.itemStr("exUser")) == false) {
			wp.whereStr += " and col_debt_t.crt_user = :crt_user ";
			setString("crt_user", wp.itemStr("exUser"));
		}

		if (wp.itemStr("exApr").equals("Y")) {
			wp.whereStr += " and col_debt_t.aud_code = '2' ";
		} else if (wp.itemStr("exApr").equals("N")) {
			wp.whereStr += " and col_debt_t.aud_code = '1' ";
		}

		wp.whereOrder = " order by col_debt_t.p_seqno ";
		// -page control-
		wp.queryWhere = wp.whereStr;

		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (getWhereStr() == false)
			return;

//		wp.selectSQL = "'0' db_optcode, "
		wp.selectSQL = "col_debt_t.acct_type, " + "act_acno.acct_key, " + "col_debt_t.p_seqno, "
				+ "col_debt_t.chi_name, " + "col_debt_t.post_date, " + "col_debt_t.txn_code, " + "col_debt_t.beg_bal, "
				+ "col_debt_t.end_bal, " + "col_debt_t.d_avail_bal, " + "col_debt_t.acct_code, "
				+ "ptr_actcode.chi_short_name, " + "col_debt_t.acct_status, " + "col_debt_t.reference_no, "
				+ "col_debt_t.mod_user, " + "col_debt_t.mod_time, " + "col_debt_t.crt_date, " + "col_debt_t.crt_user, "
				+ "col_debt_t.mod_pgm, " + "col_debt_t.mod_seqno, " + "col_debt_t.apr_date, " + "col_debt_t.apr_user, "
				+ "col_debt_t.aud_code ";

		wp.daoTable = "col_debt_t "
//				+ "left join act_acno on col_debt_t.p_seqno = act_acno.p_seqno "
				+ "left join act_acno on col_debt_t.p_seqno = act_acno.acno_p_seqno "
				+ "left join ptr_actcode on col_debt_t.acct_code = ptr_actcode.acct_code ";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		listWkdata();
		querySummary();
		wp.setPageValue();
		apprDisabled("mod_user");
	}

	void listWkdata() {
		String wkData = "";
		String[] cde = new String[] { "1", "2", "3", "4", "5" };
		String[] txt = new String[] { "1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清" };

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, "acct_status");
			wp.colSet(ii, "tt_acct_status", commString.decode(wkData, cde, txt));

			wkData = wp.colStr(ii, "aud_code");
			wp.colSet(ii, "tt_aud_code", commString.decode(wkData, ",1,2", ",,disabled"));
		}

	}

	void querySummary() {
		// select count(p_seqno) exAcctCnt, sum(cc) exCnt, sum(ebal) exAmt from (
		// select p_seqno, count(p_seqno) cc, sum(end_bal) ebal from col_debt_t
		// where col_debt_t.aud_code = '1'
		// group by p_seqno ) tt

		// -查詢-
		String wStr = "";
		if (wp.itemStr("exApr").equals("Y")) {
			wStr += " and col_debt_t.aud_code = '2' ";
		} else if (wp.itemStr("exApr").equals("N")) {
			wStr += " and col_debt_t.aud_code = '1' ";
		}
		wStr += sqlCol(wp.itemStr("exUser"), "col_debt_t.crt_user");

		wp.selectSQL = "" + " count(p_seqno) as exAcctCnt, " + " sum(cc) as exCnt, " + " sum(ebal) as exAmt ";

		wp.daoTable = "( select p_seqno, count(p_seqno) cc, sum(end_bal) ebal from col_debt_t " + "where 1=1 " + wStr
				+ " group by p_seqno ) tt";
		wp.whereStr = "";
		pageSelect();
	}

	@Override
	public void querySelect() throws Exception {
		dataKK1 = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void dataProcess() throws Exception {
		func = new Colp0130Func(wp);

		String[] lsReferenceNo = wp.itemBuff("reference_no");
		String[] lsModSeqno = wp.itemBuff("mod_seqno");
		String[] opt = wp.itemBuff("opt");
		int rowcntaa = 0;
		if (!(lsReferenceNo == null) && !empty(lsReferenceNo[0]))
			rowcntaa = lsReferenceNo.length;
		wp.listCount[0] = rowcntaa;

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			// 2018.5.8 有換頁的opt處理, 須扣掉(行數 x 頁數)//
			rr = rr - (wp.pageRows * (wp.currPage - 1));
			// 2018.5.8 end//
			if (rr < 0)
				continue;

			wp.colSet(rr, "ok_flag", "-");

			func.varsSet("reference_no", lsReferenceNo[rr]);
			func.varsSet("mod_seqno", lsModSeqno[rr]);

			rc = func.dataProc();
			sqlCommit(rc);
			if (rc == 1) {
				wp.colSet(rr, "ok_flag", "V");
				ilOk++;
				continue;
			}
			ilErr++;
			wp.colSet(rr, "ok_flag", "X");

		}

		// -re-Query-
		// queryRead();
		alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
