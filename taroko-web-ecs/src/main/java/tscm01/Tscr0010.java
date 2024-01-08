/*** 109-01-04  V1.00.01   shiyuqi       修改无意义命名 
 *   111-04-20  V1.00.02   dingwenhao    TSC畫面整合                                                                               *  
 * **/
package tscm01;

import java.util.ArrayList;

/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*/
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Tscr0010 extends BaseAction implements InfacePdf {
	double isUnitCnt = 0.0;
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
		} else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
			strAction = "PDF";
			pdfPrint();
		}

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		ArrayList<Object> whereParams = new ArrayList<Object>();
		if (this.chkStrend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2")) == false) {
			alertErr2("交易日期起迄：輸入錯誤");
			return;
		}
		String lsWhere = " where 1=1 and iso_resp_code ='00'" + sqlCol(wp.itemStr("ex_tx_date1"), "tx_date", ">=")
				+ sqlCol(wp.itemStr("ex_tx_date2"), "tx_date", "<=");
		if (!this.eqIgno(wp.itemStr("ex_report_type"), "T")) {
			lsWhere += " and trans_code in ('IN','IS') ";
		} else {
			lsWhere += " and trans_code in ('TA','TS') ";
		}
		String sumWhere = " where 1=1 and iso_resp_code ='00' ";
	    if(wp.itemEmpty("ex_tx_date1")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_tx_date1"), "tx_date",">=");
	    	whereParams.add(wp.itemStr("ex_tx_date1"));
	    }
	    if(wp.itemEmpty("ex_tx_date2")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_tx_date2"), "tx_date","<=");
	    	whereParams.add(wp.itemStr("ex_tx_date2"));
	    }
	    if (!this.eqIgno(wp.itemStr("ex_report_type"), "T")) {
	    	sumWhere += " and trans_code in ('IN','IS') ";
		} else {
			sumWhere += " and trans_code in ('TA','TS') ";
		}
	    whereParameterList = whereParams;
		sum(sumWhere);
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		//--db_unit_cnt
		wp.selectSQL = "tx_date ," + " count(*) as db_tot_cnt , ";
		if (!this.eqIgno(wp.itemStr("ex_report_type"), "T")) {
			wp.selectSQL += " sum(decode(trans_code,'IS',1,0)) as db_unit_cnt ";
		}	else	{
			wp.selectSQL += " sum(decode(trans_code,'TS',1,0)) as db_unit_cnt ";
		}				
		wp.daoTable = "cca_auth_txlog";
		wp.whereOrder = "group by tx_date order by tx_date Asc  ";
		if (empty(wp.whereStr)) {
			wp.whereStr = " ORDER BY 1";
		}

		wp.pageCountSql = "" + "select count(*) from ( " + " select distinct tx_date " + " from cca_auth_txlog "
				+ wp.whereStr + " )";

		logSql();
		pageQuery();
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}
		queryAfter();
		wp.setListCount(1);

		wp.setPageValue();

	}

	void queryAfter() {
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			if (this.eqIgno(wp.colStr(ii, "db_tot_cnt"), "0")) {
				continue;
			}

			isUnitCnt = (double) (wp.colNum(ii, "db_unit_cnt") / wp.colNum(ii, "db_tot_cnt")) * 100;
			wp.colSet(ii, "wk_ok_pct", "" + isUnitCnt);
			log("A:" + wp.colStr(ii, "wk_ok_pct"));

		}
	}

	void sum(String lsWhere) {
		//--sum_unit_cnt
		ArrayList<Object> tmpParams= (ArrayList<Object>) this.whereParameterList.clone();
		Object[] tmpObjParams = tmpParams.toArray();
		String sql1 = "select count(*) as sum_tot_cnt ,";
		if (!this.eqIgno(wp.itemStr("ex_report_type"), "T")) {
			sql1 += " sum(decode(trans_code,'IS',1,0)) as sum_unit_cnt ";
		}	else	{
			sql1 += " sum(decode(trans_code,'TS',1,0)) as sum_unit_cnt ";
		}
			sql1 += "from cca_auth_txlog " + lsWhere;
		sqlSelect(sql1,tmpObjParams);

		if (sqlNum("sum_tot_cnt") <= 0) {
			return;
		}

		double tlOkPct = 0.0;
		tlOkPct = (double) (sqlNum("sum_unit_cnt") / sqlNum("sum_tot_cnt")) * 100;

		wp.colSet("tl_tot_cnt", sqlStr("sum_tot_cnt"));
		wp.colSet("tl_unit_cnt", sqlStr("sum_unit_cnt"));
		wp.colSet("tl_ok_pct", "" + tlOkPct);
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
	public void pdfPrint() throws Exception {
		wp.reportId = "tscr0010";
		String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_tx_date1")) + " -- "
				+ commString.strToYmd(wp.itemStr("ex_tx_date2"));

		if (this.eqIgno(wp.itemStr("ex_report_type"), "T")) {
			cond1 += "  悠遊卡";
		} else {
			cond1 += "  一卡通";
		}

		wp.colSet("cond1", cond1);
		wp.colSet("user_id", wp.loginUser);
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "tscr0010.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

}
