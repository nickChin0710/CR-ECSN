package cycm01;

import ofcapp.BaseAction;

public class Cycm0055 extends BaseAction {
	String acctType = "" , currCode = "";
	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
		switch (wp.buttonCode) {
		case "X": // 轉換顯示畫面 --
			strAction = "new";
			clearFunc();
			break;
		case "Q": // -查詢功能--
			queryFunc();
			break;
		case "R":
			dataRead();
			break;
		case "M":
			queryRead();
			break;
		case "S":
			querySelect();
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
		}
		return;		
	}

	@Override
	public void dddwSelect() {
		try {
			if(eqIgno(wp.respHtml,"cycm0055")) {
				wp.optionKey = wp.colStr("ex_acct_type");
		        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
		        wp.optionKey = wp.colStr("ex_curr_code");
		        dddwList("dddw_curr_code", "ptr_currcode", "curr_code", "curr_chi_name",
		            "where curr_code in ('392','840','901') order by curr_code");
			}	else	if(eqIgno(wp.respHtml,"cycm0055_detl")) {
				wp.optionKey = wp.colStr("kk_acct_type");
		        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
		        wp.optionKey = wp.colStr("kk_curr_code");
		        dddwList("dddw_curr_code", "ptr_currcode", "curr_code", "curr_chi_name",
			         "where curr_code in ('392','840','901') order by curr_code");
			}
		} catch(Exception ex) {};
	}

	@Override
	public void queryFunc() throws Exception {
		String lsWhere = "where 1=1"
				+sqlCol(wp.itemStr("ex_acct_type"),"acct_type")
				+sqlCol(wp.itemStr("ex_curr_code"),"curr_code")
				;
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " acct_type , curr_code , purch_bal_wave , total_bal , min_payment , "
				+ " (select curr_chi_name from ptr_currcode where curr_code = ptr_curr_general.curr_code) as tt_curr_code ";
		wp.daoTable = "ptr_curr_general";
		wp.whereOrder = " order by acct_type Asc , curr_code Asc ";
		pageQuery();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}		
		wp.setListCount(0);
		wp.setPageValue();		
	}

	@Override
	public void querySelect() throws Exception {
		acctType = wp.itemStr("data_k1");
		currCode = wp.itemStr("data_k2");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if(empty(acctType))
			acctType = itemkk("acct_type");
		if(empty(currCode))
			currCode = itemkk("curr_code");
		
		if(empty(acctType)) {
			alertErr("帳戶類別 : 不可空白");
			return ;
		}
		
		if(empty(currCode)) {
			alertErr("幣別 : 不可空白");
			return ;
		}
		
		wp.selectSQL = " hex(rowid) as rowid , mod_seqno , acct_type , curr_code , purch_bal_wave , total_bal , min_payment , "
				+ " (select curr_chi_name from ptr_currcode where curr_code = ptr_curr_general.curr_code) as tt_curr_code , "
				+ " crt_date , crt_user , to_char(mod_time,'yyyymmdd') as mod_date , mod_user "
				;
		wp.daoTable = "ptr_curr_general";
		wp.whereStr = " where 1=1 "
					+sqlCol(acctType,"acct_type")
					+sqlCol(currCode,"curr_code")
					;
		
		pageSelect();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}		
	}

	@Override
	public void saveFunc() throws Exception {		
		cycm01.Cycm0055Func func = new cycm01.Cycm0055Func();
		func.setConn(wp);
		
		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if(rc!=1) {
			errmsg(func.getMsg());
		}	else	saveAfter(false);		
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initButton() {
		if(eqIgno(wp.respHtml,"cycm0055_detl"))
			btnModeAud();		
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub
		
	}

}
