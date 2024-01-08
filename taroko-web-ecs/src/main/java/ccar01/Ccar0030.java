/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package ccar01;

import java.util.ArrayList;

/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*/

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.base.CommString;
import taroko.com.TarokoExcel;

public class Ccar0030 extends BaseAction implements InfaceExcel {
	double liTempMath = 0.0;
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
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			strAction = "XLS";
			xlsPrint();
		}

	}

	@Override
	public void dddwSelect() {
		try {
			wp.optionKey = wp.colStr("ex_group_code");
			this.dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name", " where 1=1 ");
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		ArrayList<Object> whereParams = new ArrayList<Object>();
		if (wp.itemEmpty("ex_tx_date1") || wp.itemEmpty("ex_tx_date1")) {
			alertErr2("交易日期: 不可空白");
			return;
		}

		if (this.chkStrend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2")) == false) {
			alertErr2("交易日期起迄：輸入錯誤");
			return;
		}

		String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_tx_date1"), "sta_date", ">=")
				+ sqlCol(wp.itemStr("ex_tx_date2"), "sta_date", "<=") + sqlCol(wp.itemStr("ex_bin_no"), "bin_no")
				+ sqlCol(wp.itemStr("ex_risk_type"), "risk_type") + sqlCol(wp.itemStr("ex_group_code"), "group_code");
		
		String sumWhere = " where 1=1 ";
	    if(wp.itemEmpty("ex_tx_date1")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_tx_date1"), "sta_date",">=");
	    	whereParams.add(wp.itemStr("ex_tx_date1"));
	    }
	    if(wp.itemEmpty("ex_tx_date2")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_tx_date2"), "sta_date","<=");
	    	whereParams.add(wp.itemStr("ex_tx_date2"));
	    }
	    if(wp.itemEmpty("ex_bin_no")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_bin_no"), "bin_no");
	    	whereParams.add(wp.itemStr("ex_bin_no"));
	    }
	    if(wp.itemEmpty("ex_risk_type")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_risk_type"), "risk_type");
	    	whereParams.add(wp.itemStr("ex_risk_type"));
	    }
	    if(wp.itemEmpty("ex_group_code")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_group_code"), "group_code");
	    	whereParams.add(wp.itemStr("ex_group_code"));
	    }
	    whereParameterList = whereParams;
		listSum(sumWhere);
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	void listSum(String lsWhere) {
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = " select " + " sum(consume_cnt) as tl_consume_cnt , " + " sum(consume_amt) as tl_consume_amt , "
				+ " sum(auth_cnt) as tl_auth_cnt , " + " sum(decline_cnt) as tl_decline_cnt , "
				+ " sum(callbank_cnt) as tl_callbank_cnt , " + " sum(callbank_cntx) as tl_callbank_cntx , "
				+ " sum(pickup_cnt) as tl_pickup_cnt , " + " sum(expired_cnt) as tl_expired_cnt "
				+ " from cca_sta_risk_type " + lsWhere;

		sqlSelect(sql1,tmpObjParams);

		wp.colSet("tl_consume_cnt", "" + sqlStr("tl_consume_cnt"));
		wp.colSet("tl_consume_amt", "" + sqlStr("tl_consume_amt"));
		wp.colSet("tl_auth_cnt", "" + sqlStr("tl_auth_cnt"));
		wp.colSet("tl_decline_cnt", "" + sqlStr("tl_decline_cnt"));
		wp.colSet("tl_callbank_cnt", "" + sqlStr("tl_callbank_cnt"));
		wp.colSet("tl_callbank_cntx", "" + sqlStr("tl_callbank_cntx"));
		wp.colSet("tl_pickup_cnt", "" + sqlStr("tl_pickup_cnt"));
		wp.colSet("tl_expired_cnt", "" + sqlStr("tl_expired_cnt"));

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " sta_date , " + " bin_no , " + " group_code , " + " risk_type , "
				+ " sum(consume_cnt) as consume_cnt , " + " sum(consume_amt) as consume_amt , "
				+ " sum(auth_cnt) as auth_cnt , " + " sum(decline_cnt) as decline_cnt , "
				+ " sum(callbank_cnt) as callbank_cnt , " + " sum(callbank_cntx) as callbank_cntx , "
				+ " sum(pickup_cnt) as pickup_cnt , " + " sum(expired_cnt) as expired_cnt , "
				+ " sum(auth_cnt + decline_cnt + callbank_cnt + pickup_cnt + expired_cnt) as wk_tot_cnt ";
		wp.daoTable = "cca_sta_risk_type";
		wp.whereOrder = " group by sta_date , risk_type , group_code , bin_no order by sta_date ";
		if (empty(wp.whereStr)) {
			wp.whereStr = " ORDER BY 1";
		}

		wp.pageCountSql = " select count(*) from (select distinct sta_date , risk_type , group_code , bin_no "
				+ "from cca_sta_risk_type " + wp.whereStr + ")";

		logSql();

		pageQuery();
		queryReadAfter();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}
		wp.setPageValue();
	}

	void queryReadAfter() {
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// --auth
			liTempMath = wp.colNum(ii, "auth_cnt") / wp.colNum(ii, "wk_tot_cnt") * 100;
			wp.colSet(ii, "auth_pct", "" + liTempMath);
			liTempMath = 0;
			// --decline
			liTempMath = wp.colNum(ii, "decline_cnt") / wp.colNum(ii, "wk_tot_cnt") * 100;
			wp.colSet(ii, "decline_pct", "" + liTempMath);
			liTempMath = 0;
			// --callbank
			liTempMath = wp.colNum(ii, "callbank_cnt") / wp.colNum(ii, "wk_tot_cnt") * 100;
			wp.colSet(ii, "callbank_pct", "" + liTempMath);
			liTempMath = 0;
			// --callbank_x
			liTempMath = wp.colNum(ii, "callbank_cntx") / wp.colNum(ii, "wk_tot_cnt") * 100;
			wp.colSet(ii, "callbank_x_pct", "" + liTempMath);
			liTempMath = 0;
			// --pick_up
			liTempMath = wp.colNum(ii, "pickup_cnt") / wp.colNum(ii, "wk_tot_cnt") * 100;
			wp.colSet(ii, "pickup_pct", "" + liTempMath);
			liTempMath = 0;
			// --expired
			liTempMath = wp.colNum(ii, "expired_cnt") / wp.colNum(ii, "wk_tot_cnt") * 100;
			wp.colSet(ii, "expired_pct", "" + liTempMath);
			liTempMath = 0;
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

	@Override
	public void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = "ccar0030";
			String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_tx_date1")) + " -- "
					+ commString.strToYmd(wp.itemStr("ex_tx_date2"));
			wp.colSet("cond1", cond1);
			wp.colSet("user_id", wp.loginUser);
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
			xlsx.excelTemplate = "ccar0030.xlsx";
			wp.pageRows = 9999;
			queryFunc();
			xlsx.processExcelSheet(wp);
			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");
		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}

	}

	@Override
	public void logOnlineApprove() throws Exception {
		// TODO Auto-generated method stub

	}

}
