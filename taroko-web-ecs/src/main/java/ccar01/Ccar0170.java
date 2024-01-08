package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*/

import java.util.ArrayList;

import ofcapp.BaseAction;

public class Ccar0170 extends BaseAction {
	ArrayList<Object> whereParameterList = null;

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
		ArrayList<Object> whereParams = new ArrayList<Object>();
		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("日期起迄：輸入錯誤");
			return;
		}
		String lsWhere = " where 1=1 and MCC_CODE in ('6010','6011')  " + sqlCol(wp.itemStr("ex_date1"), "tx_date", ">=")
				+ sqlCol(wp.itemStr("ex_date2"), "tx_date", "<=") + sqlCol(wp.itemStr("ex_card_no"), "card_no");
		
		String sumWhere = " where 1=1 and mcc_code in ('6010','6011') ";
	    if(wp.itemEmpty("ex_date1")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_date1"), "tx_date",">=");
	    	whereParams.add(wp.itemStr("ex_date1"));
	    }
		
	    if(wp.itemEmpty("ex_date2")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_date2"), "tx_date","<=");
	    	whereParams.add(wp.itemStr("ex_date2"));
	    }
	    
	    if(wp.itemEmpty("ex_card_no")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_card_no"), "card_no");
	    	whereParams.add(wp.itemStr("ex_card_no"));
	    }
	    
	    whereParameterList = whereParams;
	    sumData(sumWhere);
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " card_no ," + " tx_date ," + " tx_time ," + " stand_in ," + " pos_term_id ," + " nt_amt ,"
				+ " auth_no ";
		wp.daoTable = "cca_auth_txlog";
		wp.whereOrder = "  order by card_no ";
		if (empty(wp.whereStr)) {
			wp.whereStr = " ORDER BY 1";
		}

		logSql();
		pageQuery();

		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			set0();
			alertErr2("此條件查無資料");
			return;
		}
		wp.setPageValue();

	}

	void set0() {
		wp.colSet("db_cnt", "0");
		wp.colSet("sum_nt_amt", "0");
	}

	void sumData(String lsWhere) {
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = "select count(*) as db_cnt, sum(nt_amt) as sum_nt_amt from cca_auth_txlog "+lsWhere;
		sqlSelect(sql1,tmpObjParams);
		
		wp.colSet("db_cnt", "" + sqlStr("db_cnt"));
		wp.colSet("sum_nt_amt", "" + sqlStr("sum_nt_amt"));
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
