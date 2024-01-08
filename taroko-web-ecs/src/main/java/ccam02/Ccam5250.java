package ccam02;

/* DEBIT卡參數維護　debit_parm
 * Table: cca_debit_parm
 * ----------------------------------------------------------------------
 * V00.00	Alex		2017-08xx
 * V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 * */

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam5250 extends BaseEdit {
	Ccam5250Func func;
	String binNo = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		wp = wr;
		rc = 1;
		wp.logActive();

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
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			updateFunc();
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
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			wp.colSet("no_connect_flag", "Y");
		}
	}

	@Override
	public void queryFunc() throws Exception {
		wp.whereStr = " where 1=1" + sqlCol(wp.itemStr2("ex_bin_no"), "bin_no");

		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = "bin_no,   " + "no_connect_flag, " + "cnt_amount," + "day_amount," + "month_amount,"
				+ "withdraw_fee," + "day_cnt," + "markup," + "to_char(mod_time,'yyyymmdd') as mod_date," + " mod_user,"
				+ " decode(open_chk,'1','Y','0','N','N') as open_chk ,"
				+ " decode(mcht_chk,'1','Y','0','N','N') as mcht_chk ,"
				+ " decode(oversea_chk,'1','Y','0','N','N') as oversea_chk ,"
				+ " decode(avg_consume_chk,'1','Y','0','N','N') as avg_consume_chk ,"
				+ " decode(month_risk_chk,'1','Y','0','N','N') as month_risk_chk ,"
				+ " decode(day_risk_chk,'1','Y','0','N','N') as day_risk_chk ";

		wp.daoTable = "CCA_debit_parm";
		wp.whereOrder = " order by bin_no";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		binNo = wp.itemStr("data_k1");
		dataRead();

	}

	@Override
	public void dataRead() throws Exception {
		if (empty(binNo)) {
			binNo = itemKk("bin_no");
		}
		if (empty(binNo)) {
			alertErr("Bin no：不可空白");
			return;
		}
		wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "bin_no,   " + "no_connect_flag, " + "cnt_amount, "
				+ "day_amount, " + "month_amount, " + "withdraw_fee, " + "day_cnt, " + "markup, " + "crt_date,"
				+ "mod_user, to_char(mod_time,'yyyymmdd') as mod_date " + ", crt_user ,"
				+ "open_chk , mcht_chk , oversea_chk , avg_consume_chk , month_risk_chk , day_risk_chk ";
		wp.daoTable = "CCA_debit_parm";
		wp.whereStr = "where 1=1" + sqlCol(binNo, "bin_no");

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + binNo);
		}
	}

	@Override
	public void saveFunc() throws Exception {
		func = new Ccam5250Func();
		func.setConn(wp);
		if (checkApproveZz() == false) {
			return;
		}
		rc = func.dbSave(strAction);
		this.sqlCommit(rc);

		if (rc != 1) {
			alertErr2(func.getMsg());
		}
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
