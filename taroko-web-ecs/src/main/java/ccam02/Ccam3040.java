package ccam02;
/*
 * 收單特店資料維護   mcht_qry_upd
 * V00.0		JH		2017-0802: initial
 * V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 * */


import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ccam3040 extends BaseEdit {
Ccam3040Func func;
String mchtNo = "", bankId = "";

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
	super.wp = wr;
	rc = 1;

	strAction = wp.buttonCode;
	if (eqIgno(wp.buttonCode, "X")) {
		/* 轉換顯示畫面 */
		strAction = "new";
		clearFunc();
	}
	else if (eqIgno(wp.buttonCode, "Q")) {
		/* 查詢功能 */
		strAction = "Q";
		queryFunc();
	}
	else if (eqIgno(wp.buttonCode, "R")) {
		// -資料讀取-
		strAction = "R";
		dataRead();
	}
	else if (eqIgno(wp.buttonCode, "A")) {
		/* 新增功能 */
		insertFunc();
	}
	else if (eqIgno(wp.buttonCode, "U")) {
		/* 更新功能 */
		updateFunc();
	}
	else if (eqIgno(wp.buttonCode, "D")) {
		/* 刪除功能 */
		deleteFunc();
	}
	else if (eqIgno(wp.buttonCode, "M")) {
		/* 瀏覽功能 :skip-page */
		queryRead();
	}
	else if (eqIgno(wp.buttonCode, "S")) {
		/* 動態查詢 */
		querySelect();
	}
	else if (eqIgno(wp.buttonCode, "L")) {
		/* 清畫面 */
		strAction = "";
		clearFunc();
	}

	dddwSelect();
	initButton();
}

@Override
public void dddwSelect() {
	try {
		if (wp.respHtml.indexOf("_detl") > 0) {
			wp.optionKey = wp.colStr("kk_acq_bank_id");
			dddwList(
					"dddw_mbase_acqid",
					"ptr_sys_idtab",
					"wf_id", "wf_desc",
					"where wf_type='ACQ_BANK_ID' and wf_id <> '999999'");
		}
	}
	catch (Exception ex) {
	}

	try {
		if (eqIgno(wp.respHtml, "ccam3040")) {
			wp.initOption = "--";
			wp.optionKey = wp.colStr(0, "ex_acq_bank_id");
			dddwList(
					"dddw_mbase_acqid",
					"ptr_sys_idtab",
					"wf_id", "wf_desc",
					"where wf_type='ACQ_BANK_ID' and wf_id <> '999999' ");
		}
	}
	catch (Exception ex) {
	}

}

@Override
public void queryFunc() throws Exception {
	wp.whereStr =
		" where 1=1"
			+ sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no", "like%")
			+ sqlCol(wp.itemStr("ex_acq_bank_id"), "acq_bank_id")
			+ sqlCol(wp.itemStr("ex_mcht_name"), "mcht_name", "%like%");
	wp.whereOrder = " order by acq_bank_id, mcht_no ";

	wp.queryWhere = wp.whereStr;
	wp.setQueryMode();

	queryRead();

}

@Override
public void queryRead() throws Exception {
	wp.pageControl();
	wp.selectSQL = "acq_bank_id, "
			+ " mcht_no, "
			+ " mcht_name,"
			+ " zip_code , "
			+ " zip_city , "
			+ " mcht_addr , "
			+ " tel_no , "
			+ " contr_type , "
			+ " mcc_code ,"
			+ " to_char(mod_time,'yyyymmdd') as mod_date ,"
			+ " mod_user , "
			+ " mcht_remark "
	// + "uf_tt_resp_code(A.resp_code) as resp_desc "
	;
	wp.daoTable = "cca_mcht_base";
	wp.whereOrder = " order by acq_bank_id , mcht_no ";
	pageQuery();

	wp.setListCount(1);
	if (sqlNotFind()) {
		alertErr(appMsg.errCondNodata);
		return;
	}
	
	//wp.totalRows = wp.dataCnt;
	//wp.listCount[1] = wp.dataCnt;
	wp.setPageValue();
}

@Override
public void querySelect() throws Exception {
	mchtNo = wp.itemStr("data_k1");
	bankId = wp.itemStr("data_k2");
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
	if (empty(mchtNo) || empty(bankId)) {
		alertErr("收單行,特店代碼：需同時輸入");
		return;
	}
	wp.selectSQL =
		"hex(rowid) as rowid, mod_seqno, "
			+ "acq_bank_id,   "
			+ "mcht_no, "
			+ "mcht_name, "
			+ "zip_code,"
			+ "zip_city,"
			+ "mcht_addr,"
			+ "tel_no,"
			+ "contr_type,"
			+ "mcc_code,"
			+ "to_char(mod_time ,'yyyymmdd') as mod_date,"
			+ "mod_user,"
			+ "mod_pgm ,"
			+ "crt_user ,"
			+ "crt_date , "
			+ "ncc_risk_level"
			;
	wp.daoTable = " cca_mcht_base";
	wp.whereStr = " where 1=1" 
			+ sqlCol(mchtNo, "mcht_no") 
			+ sqlCol(bankId, "acq_bank_id");
	this.logSql();

	pageSelect();
	if (sqlNotFind()) {
		alertErr("查無資料, key=" + mchtNo + "," + bankId);
		return;
	}
}

@Override
public void saveFunc() throws Exception {
	func = new Ccam3040Func();
	func.setConn(wp);
	
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
}
