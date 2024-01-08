/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                         *
* 111-03-23  V1.00.04  Justin       add limit 1 into queryAfter()            *    
* 111-03-25  V1.00.05  Justin       optimize sql                             *
* 111-03-25  V1.00.06  Alex         mcht_name 抓法異動                                                                 *
******************************************************************************/
package rskm03;

import ofcapp.BaseAction;

public class Rskm3330 extends BaseAction {
	String cardNo = "", txDate = "", txTime = "", authNo = "", traceNo = "";
	taroko.base.CommDate commdate = new taroko.base.CommDate();
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
		} else if (eqIgno(wp.buttonCode, "R2")) {
			dataReadTxLog();
		}

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		getWhere();
		wp.setQueryMode();

		queryRead();

	}

	void getWhere() {
		sqlParm.clear();
		if (chkStrend(wp.itemStr("ex_txn_date1"), wp.itemStr("ex_txn_date2")) == false) {
			alertErr2("消費日期:起迄錯誤");
			return;
		}

		String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_txn_date1"), "tx_date", ">=")
				+ sqlCol(wp.itemStr("ex_txn_date2"), "tx_date", "<=")
				+ sqlCol(wp.itemStr("ex_risk_score"), "risk_score", ">=") + sqlCol(wp.itemStr("ex_card_no"), "card_no")

		;

		// --身份證ID
		if (wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and card_no in "
					+ " (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"), "B.id_no") + " union "
					+ " select A.card_no from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"), "B.id_no") + " ) ";
		}

		// --授權交易成功or失敗
		if (wp.itemEq("ex_auth", "Y")) {
			lsWhere += " and iso_resp_code = '00' ";
		} else if (wp.itemEq("ex_auth", "N")) {
			lsWhere += " and iso_resp_code <> '00' ";
		}

		// --處理狀態
//		if (wp.itemEq("ex_status_code", "A") == false) {
//			lsWhere += sqlCol(wp.itemStr("ex_status_code"), "uf_nvl(status_code,'0')");
//		}
		
		if (wp.itemEq("ex_status_code", "0")) {
			lsWhere += " and status_code in ('','0') ";
		}	else if (wp.itemEq("ex_status_code", "5")) {
			lsWhere += " and status_code = '5' ";
		}	else if (wp.itemEq("ex_status_code", "9")) {
			lsWhere += " and status_code = '9' ";
		}

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " tx_date , tx_time , card_no , risk_score , tx_currency , ori_amt , nt_amt , "
				+ " mcc_code , auth_status_code , auth_no , mcht_no , mcht_city , consume_country as mcht_country , proc_code , "
				+ " mtch_flag , decode(status_code,'5','處理中','9','處理完成','未處理') as tt_status_code , trace_no ,"
				+ " pos_mode , stand_in , mcht_name , uf_curr_name(tx_currency) as tt_currency "
				;

		wp.daoTable = " cca_auth_txlog ";
		if (wp.itemEq("ex_order", "0")) {
			wp.whereOrder = " order by risk_score Asc , tx_date Asc , tx_time Asc ";
		} else if (wp.itemEq("ex_order", "1")) {
			wp.whereOrder = " order by risk_score Desc , tx_date Asc , tx_time Asc ";
		} else if (wp.itemEq("ex_order", "2")) {
			wp.whereOrder = " order by card_no Asc ";
		} else if (wp.itemEq("ex_order", "3")) {
			wp.whereOrder = " order by tx_date Asc , tx_time Asc ";
		} else if (wp.itemEq("ex_order", "4")) {
			wp.whereOrder = " order by tx_date Desc , tx_time Desc ";
		} else if (wp.itemEq("ex_order", "5")) {
			wp.whereOrder = " order by nt_amt Asc ";
		} else if (wp.itemEq("ex_order", "6")) {
			wp.whereOrder = " order by nt_amt Desc ";
		}

		getWhere();

		pageQuery();

		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}

		wp.setPageValue();
		wp.setListCount(0);
//		queryAfter();
	}
	
	void queryAfter() throws Exception {
		
		// 2022/03/22 Justin improve performance by adding limit 1 
//		String sql1 = " select mcht_name as mcht_chi_name , mcht_eng_name from cca_mcht_bill where mcht_no = ? and acq_bank_id = ? limit 1 ";
//		String sql2 = " select mcht_name as mcht_chi_name , mcht_eng_name from cca_mcht_bill where mcht_no = ? limit 1 ";
		
		for(int ii=0 ; ii<wp.selectCnt ; ii++) {
//			sqlSelect(sql1,new Object[] {wp.colStr(ii,"mcht_no"),wp.colStr(ii,"stand_in")});
//			if(sqlRowNum >0) {
//				if(sqlStr("mcht_chi_name").isEmpty()) {
//					wp.colSet(ii,"mcht_name", sqlStr("mcht_eng_name"));
//				}	else	{
//					wp.colSet(ii,"mcht_name", sqlStr("mcht_chi_name"));
//				}
//			}	else	{
//				sqlSelect(sql2,new Object[] {wp.colStr(ii,"mcht_no")});
//				if(sqlRowNum >0) {
//					if(sqlStr("mcht_chi_name").isEmpty()) {
//						wp.colSet(ii,"mcht_name", sqlStr("mcht_eng_name"));
//					}	else	{
//						wp.colSet(ii,"mcht_name", sqlStr("mcht_chi_name"));
//					}
//				}
//			}
//			if (wp.colEmpty(ii, "mcht_name")) {
//				sqlSelect(sql2,new Object[] {wp.colStr(ii,"mcht_no")});
//				if(sqlRowNum >0) {
//					if(sqlStr("mcht_chi_name").isEmpty()) {
//						wp.colSet(ii,"mcht_name", sqlStr("mcht_eng_name"));
//					}	else	{
//						wp.colSet(ii,"mcht_name", sqlStr("mcht_chi_name"));
//					}
//				}
//			}
		}
		
		
	}
	
	@Override
	public void querySelect() throws Exception {
		cardNo = wp.itemStr("data_k1");
		txDate = wp.itemStr("data_k2");
		txTime = wp.itemStr("data_k3");
		authNo = wp.itemStr("data_k4");
		traceNo = wp.itemStr("data_k5");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (empty(cardNo))
			cardNo = wp.itemStr("card_no");
		if (empty(txDate))
			txDate = wp.itemStr("tx_date");
		if (empty(txTime))
			txTime = wp.itemStr("tx_time");
		if (empty(authNo))
			authNo = wp.itemStr("auth_no");
		if (empty(traceNo))
			traceNo = wp.itemStr("trace_no");

		// --查詢此筆交易資料
		wp.selectSQL = " A.tx_date , A.tx_time , A.risk_score , A.auth_status_code , A.auth_no , A.trace_no , A.tx_currency , "
				+ " A.ori_amt , A.nt_amt , A.mcht_no , A.consume_country as mcht_country , A.mcht_city , A.mcht_name , A.iso_resp_code , A.mcht_city_name , A.stand_in ,"
				+ " B.proc_date , B.proc_time , B.content_result , B.status_code , B.proc_result , "
				+ " substring(B.proc_desc,1,100) as proc_desc1 , substring(B.proc_desc,101,100) as proc_desc2 , "
				+ " substring(B.proc_desc,201,100) as proc_desc3 , B.problem_flag , B.mod_seqno , "
				+ " decode(A.iso_resp_code,'00','成功','失敗') as tt_iso_resp_code , B.mod_user , "
				+ " uf_curr_name(A.tx_currency) as tt_currency ";
		wp.daoTable = " cca_auth_txlog A left join rsk_factormaster B on A.card_no = B.card_no "
				+ " and A.tx_date = B.tx_date and A.tx_time = B.tx_time and A.auth_no = B.auth_no and A.trace_no = B.trace_no ";
		wp.whereStr = " where 1=1 " + sqlCol(cardNo, "A.card_no") + sqlCol(txDate, "A.tx_date") + sqlCol(txTime, "A.tx_time")
				+ sqlCol(authNo, "A.auth_no") + sqlCol(traceNo, "A.trace_no");

		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無交易資料");
			return;
		}

		// --查詢基本資料
		if (selectBase(cardNo) == -1) {
			alertErr2("非本行發行卡");
			return;
		}

		// --查詢帳戶資料
		if (selectAcno(wp.colStr("acno_p_seqno"), wp.colStr("debit_flag")) == -1) {
			alertErr2("非本行發行卡");
			return;
		}
		
		//--預設處理日期時間
		if(wp.colEmpty("proc_date") || wp.colEmpty("proc_time")) {
			getProcDateTime();
		}
		
		//--查詢最後維護人員
		if(wp.colEmpty("mod_user") == false ) {
			getUserCname();
		}
		
		//--特店名稱
//		getMchtName();
	}
	
	void getMchtName() {
		String sql1 = " select mcht_name as mcht_chi_name , mcht_eng_name from cca_mcht_bill where mcht_no = ? and acq_bank_id = ? ";
		String sql2 = " select mcht_name as mcht_chi_name , mcht_eng_name from cca_mcht_bill where mcht_no = ? ";
		
		sqlSelect(sql1,new Object[] {wp.colStr("mcht_no"),wp.colStr("stand_in")});
		if(sqlRowNum > 0 ) {
			if(sqlStr("mcht_chi_name").isEmpty()) {
				wp.colSet("mcht_name", sqlStr("mcht_eng_name"));
			}	else	{
				wp.colSet("mcht_name", sqlStr("mcht_chi_name"));
			}
		}	else	{
			sqlSelect(sql2,new Object[] {wp.colStr("mcht_no")});
			if(sqlRowNum >0) {
				if(sqlStr("mcht_chi_name").isEmpty()) {
					wp.colSet("mcht_name", sqlStr("mcht_eng_name"));
				}	else	{
					wp.colSet("mcht_name", sqlStr("mcht_chi_name"));
				}
			}
		}		
	}
	
	void getUserCname() {
		String sql1 = "select usr_cname from sec_user where usr_id = ? ";
		sqlSelect(sql1,new Object[] {wp.colStr("mod_user")});
		if(sqlRowNum >0) {
			wp.colSet("tt_mod_user", sqlStr("usr_cname"));
		}
	}
	
	void getProcDateTime() {
		String sql1 = "select to_char(sysdate,'yyyymmdd') as sysDate , to_char(sysdate,'hh24miss') as sysTime from dual ";
		sqlSelect(sql1);
		
		wp.colSet("proc_date", sqlStr("sysDate"));
		wp.colSet("proc_time", sqlStr("sysTime"));
		
	}
	
	int selectBase(String aCardNo) {
		if (empty(aCardNo))
			return -1;
		// --先判斷是信用卡還是VD卡
		String sql1 = " select debit_flag from cca_card_base where card_no = ? ";
		sqlSelect(sql1, new Object[] { aCardNo });

		if (sqlRowNum <= 0)
			return -1;

		boolean lbDebit = eqIgno(sqlStr("debit_flag"), "Y");

		String sql2 = "";
		if (lbDebit) {
			sql2 = " select A.card_no , B.id_no , B.chi_name , "
					+ " B.office_area_code1||'-'||B.office_tel_no1||'-'||B.office_tel_ext1 as office_tel1 , "
					+ " B.home_area_code1||'-'||B.home_tel_no1||'-'||B.home_tel_ext1 as home_tel1 , "
					+ " B.cellar_phone , A.current_code , A.new_end_date , A.p_seqno as acno_p_seqno "
					+ " from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno " + " where A.card_no = ? ";
		} else {
			sql2 = " select A.card_no , B.id_no , B.chi_name , "
					+ " B.office_area_code1||'-'||B.office_tel_no1||'-'||B.office_tel_ext1 as office_tel1 , "
					+ " B.home_area_code1||'-'||B.home_tel_no1||'-'||B.home_tel_ext1 as home_tel1 , "
					+ " B.cellar_phone , A.current_code , A.new_end_date , A.acno_p_seqno "
					+ " from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno " + " where A.card_no = ? ";
		}

		sqlSelect(sql2, new Object[] { aCardNo });
		if (sqlRowNum <= 0)
			return -1;

		wp.colSet("card_no", sqlStr("card_no"));
		wp.colSet("id_no", sqlStr("id_no"));
		wp.colSet("chi_name", sqlStr("chi_name"));
		wp.colSet("office_tel1", sqlStr("office_tel1"));
		wp.colSet("home_tel1", sqlStr("home_tel1"));
		wp.colSet("cellar_phone", sqlStr("cellar_phone"));
		wp.colSet("current_code", sqlStr("current_code"));
		wp.colSet("new_end_date", sqlStr("new_end_date"));
		wp.colSet("acno_p_seqno", sqlStr("acno_p_seqno"));
		if (lbDebit)
			wp.colSet("debit_flag", "Y");
		else
			wp.colSet("debit_flag", "N");

		return 1;
	}

	int selectAcno(String acnoPSeqno, String debit) {
		if (empty(acnoPSeqno) || empty(debit))
			return -1;
		String sql1 = "";
		if (eqIgno(debit, "Y")) {
			sql1 = " select line_of_credit_amt from dba_acno where p_seqno = ? ";
		} else {
			sql1 = " select line_of_credit_amt from act_acno where acno_p_seqno = ? ";
		}

		sqlSelect(sql1, new Object[] { acnoPSeqno });
		if (sqlRowNum <= 0)
			return -1;

		wp.colSet("line_of_credit_amt", sqlStr("line_of_credit_amt"));

		return 1;
	}

	@Override
	public void saveFunc() throws Exception {
		rskm03.Rskm3330Func func = new rskm03.Rskm3330Func();
		func.setConn(wp);

		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if (rc != 1) {
			alertErr2(func.getMsg());
		} else
			saveAfter(false);

	}
	
	public void dataReadTxLog() throws Exception {
		String tempDate = "";
		//--目前先6個月
		tempDate = commdate.dateAdd(getSysDate(), 0, -6, 0);
		wp.selectSQL = " tx_date , tx_time , card_no , risk_score , tx_currency , ori_amt , nt_amt , "
				+ " mcc_code , auth_status_code , auth_no , mcht_no , mcht_name , mcht_city , consume_country as mcht_country , proc_code , "
				+ " mtch_flag , decode(status_code,'5','處理中','9','處理完成','未處理') as tt_status_code , trace_no ,"
				+ " pos_mode , (select resp_remark from cca_resp_code where resp_code = cca_auth_txlog.auth_status_code) as resp_remark , "
				+ " auth_remark , stand_in , uf_curr_name(tx_currency) as tt_currency "
				;

		wp.daoTable = "cca_auth_txlog";
		wp.whereStr = "where 1=1 "
				+sqlCol(tempDate,"tx_date",">=")
				+sqlCol(wp.itemStr("card_no"),"card_no")
				;
		wp.whereOrder = "order by tx_date Desc , tx_time Desc ";
		selectNoLimit();
		pageQuery();
		
		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
			return;
		}
		
		wp.setListCount(0);
//		dataReadTxLogAfter();
	}
	
	void dataReadTxLogAfter() throws Exception {
		String sql1 = " select mcht_name as mcht_chi_name , mcht_eng_name from cca_mcht_bill where mcht_no = ? and acq_bank_id = ? ";
		String sql2 = " select mcht_name as mcht_chi_name , mcht_eng_name from cca_mcht_bill where mcht_no = ? ";
		
		for(int ii=0 ; ii<wp.selectCnt ; ii++) {
			sqlSelect(sql1,new Object[] {wp.colStr(ii,"mcht_no"),wp.colStr(ii,"stand_in")});
			if(sqlRowNum >0) {
				if(sqlStr("mcht_chi_name").isEmpty()) {
					wp.colSet(ii,"mcht_name", sqlStr("mcht_eng_name"));
				}	else	{
					wp.colSet(ii,"mcht_name", sqlStr("mcht_chi_name"));
				}
			}	else	{
				sqlSelect(sql2,new Object[] {wp.colStr(ii,"mcht_no")});
				if(sqlRowNum >0) {
					if(sqlStr("mcht_chi_name").isEmpty()) {
						wp.colSet(ii,"mcht_name", sqlStr("mcht_eng_name"));
					}	else	{
						wp.colSet(ii,"mcht_name", sqlStr("mcht_chi_name"));
					}
				}
			}
		}
	}
	
	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
