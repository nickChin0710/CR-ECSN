/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-12-16  V1.00.00  Ryan   program initial                                *
******************************************************************************/

package cycm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Cycm0200 extends BaseEdit {

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		
		super.wp = wr;
		rc = 1;
		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			// clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			// queryFunc();
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
			// deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			// queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			// querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			// clearFunc();
		} else if (eqIgno(wp.buttonCode, "S1")) {
			/* 存檔 */
			strAction = "S1";
			saveFunc();
		}

		initButton();
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
		wp.whereStr = commSqlStr.rownum(1);
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
	}

	@Override
	public void queryRead() throws Exception {
	}

	@Override
	public void querySelect() throws Exception {
		// dataRead();
	}
	@Override
	public void initPage() {
		try {
			dataRead();
		} catch (Exception ex) {
		}
	}
	@Override
	public void dataRead() throws Exception {
		wp.selectSQL = "hex(rowid) as rowid," + " substrb(run_month,1,1) mm1,                   "
				+ " substrb(run_month,2,1) mm2,                   " + " substrb(run_month,3,1) mm3,                   "
				+ " substrb(run_month,4,1) mm4,                   " + " substrb(run_month,5,1) mm5,                   "
				+ " substrb(run_month,6,1) mm6,                   " + " substrb(run_month,7,1) mm7,                   "
				+ " substrb(run_month,8,1) mm8,                   " + " substrb(run_month,9,1) mm9,                   "
				+ " substrb(run_month,10,1) mm10,                 " + " substrb(run_month,11,1) mm11,                 "
				+ " substrb(run_month,12,1) mm12,                 "
				+ " run_day,run_day2,run_day3,use_rcmonth,acct_type_flag,exclude_acct_type,penalty_month,penalty_month2,"
				+ " to_char(mod_time,'yyyymmdd') as mod_date, mod_user,apr_user,apr_date ";

		wp.daoTable = "cyc_rcrate_parm";
		getWhereStr();

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料");
		}
		String excludeAcctType = wp.colStr("exclude_acct_type");
		selectPtrAcctType(excludeAcctType);
	}

	@Override
	public void saveFunc() throws Exception {
		selectPtrAcctType(wp.itemStr("exclude_acct_type"));
		// -check approve-
		if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
			return;
		}
		Cycm0200Func func = new Cycm0200Func(wp);
		String lsSql = "select count(*) as tot_cnt from cyc_rcrate_parm where 1=1";
		sqlSelect(lsSql);
		if (sqlInt("tot_cnt") > 0) {
			strAction = "U";
		} else {
			strAction = "A";
		}
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr2(func.getMsg());
		}else {
			this.alertMsg("");
		}

		this.sqlCommit(rc);
	}

	@Override
	public void initButton() {
//		this.btnModeAud();
		this.btnUpdateOn(true);
	}

	public void selectPtrAcctType(String excludeAcctType) throws Exception {
		wp.selectSQL = "acct_type " + ", chin_name" + ", card_indicator";
		wp.daoTable = "ptr_acct_type";
		wp.whereStr = " where 1=1 ";
		wp.whereOrder = " order by acct_type ";
		pageQuery();
		wp.setListCount(1);
		for (int ll = 0; ll < wp.selectCnt; ll++) {
			String acctType = wp.colStr(ll, "acct_type");
			if (excludeAcctType.indexOf(acctType)>=0) {
				wp.colSet(ll, "opt_checked", "checked");
			}
			String cardIndicator = wp.colStr(ll, "card_indicator");
			wp.colSet(ll, "card_indicator", commString.decode(cardIndicator, ",1,2", ",一般卡,商務卡"));
		}
	}
}
