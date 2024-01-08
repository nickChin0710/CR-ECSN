/* 自助加油交易報表 V.2018-0731.jh
 * 109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard   
 * 109-01-04  V1.00.01   shiyuqi       修改无意义命名         
 * 110-01-14  V1.00.02  Justin                    fix parameterize sql bugs  *
 * 110-01-30  V1.00.03  Justin                    fix a bug which misuse a column name     
 * */
package ccar01;

import java.util.ArrayList;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ccar0350 extends BaseAction implements InfacePdf {
	ArrayList<Object> whereParameterList = null;

	@Override
	public void userAction() throws Exception {
		wp.pgmVersion("V.18-0731");

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

		if (this.chkStrend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2")) == false) {
			alertErr2("交易日期起迄：輸入錯誤");
			return;
		}
//		String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_tx_date1"), "tx_date", ">=")
//				+ sqlCol(wp.itemStr("ex_tx_date2"), "tx_date", "<=")
//				+ sqlCol(wp.itemStr("ex_bin_no"), "card_no", "like%");
//		if (!empty(wp.itemStr("ex_mcc_code"))) {
//			lsWhere += sqlCol(wp.itemStr("ex_mcc_code"), "mcc_code");
//		} else {
//			lsWhere += " and mcc_code in (select sys_key from cca_sys_parm2 where sys_id='AUR350-MCC') ";
//		}
		
		String lsWhere = " where 1=1 " ;
		
		if (wp.itemEmpty("ex_tx_date1") == false) {
			lsWhere += " and tx_date >= :ex_tx_date1 ";
			setString("ex_tx_date1", wp.itemStr("ex_tx_date1"));
		}
		if (wp.itemEmpty("ex_tx_date2") == false) {
			lsWhere += " and tx_date <= :ex_tx_date2 ";
			setString("ex_tx_date2", wp.itemStr("ex_tx_date2"));
		}
		if (wp.itemEmpty("ex_bin_no") == false) {
			lsWhere += " and card_no like :ex_bin_no ";
			setString("ex_bin_no", wp.itemStr("ex_bin_no") + "%");
		}
		
		if (!empty(wp.itemStr("ex_mcc_code"))) {
			lsWhere += " and mcc_code = :ex_mcc_code ";
			setString("ex_mcc_code", wp.itemStr("ex_mcc_code"));
		} else {
			lsWhere += " and mcc_code in (select sys_key from cca_sys_parm2 where sys_id='AUR350-MCC') ";
		}
		
		sqlParm.setSqlParmNoClear(true);
		listSum(lsWhere);
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	void listSum(String lsWhere) {

		
		String sql1 = " select " + " count(*) as tl_cnt , " + " sum(db_cnt) as tl_txn_cnt " + " from "
				+ " (select tx_date , card_no , count(*) as db_cnt from cca_auth_txlog  " + lsWhere
				+ " group by tx_date , card_no having count(*) >= :ex_tx_cnt ) ";
		setString("ex_tx_cnt", wp.itemStr("ex_tx_cnt"));

		sqlSelect(sql1);

		wp.colSet("tl_cnt", sqlStr("tl_cnt"));
		wp.colSet("tl_txn_cnt", sqlStr("tl_txn_cnt"));

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = "" // "substr (card_no, 1 , 6) as wk_bin_no ,"
				+ " tx_date , " + " card_no , "
				// + " mcc_code , "
				+ " count(*) as db_cnt , " + " max(tx_time) as db_time , " + " 0 as db_curr_limit , "
				+ " 0 as db_curr_unpad ";
		wp.daoTable = "cca_auth_txlog";
		wp.whereOrder = " group by tx_date, card_no having count(*) >= :ex_tx_cnt ";			
		wp.whereOrder += " order by tx_date,card_no";
		
		wp.pageCountSql = "" + "select count(*) as tl_cnt from ( "
				+ " select tx_date , card_no , count(*) as db_cnt" + " from cca_auth_txlog " + wp.whereStr
				+ " group by tx_date , card_no " + " having count(*) >= :ex_tx_cnt ) " + " ";		
		
		setString("ex_tx_cnt", wp.itemStr("ex_tx_cnt"));
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
		wp.logSql = false;
		for (int ii = 0; ii < wp.selectCnt; ii++) {

			wp.colSet(ii, "hh_cardno", commString.hideCardNo(wp.colStr(ii, "card_no")));

			String sql1 = "select curr_tot_std_amt as db_curr_limit ,"
					+ " curr_otb_amt - nt_amt - (curr_tot_lmt_amt - curr_tot_std_amt) as db_curr_unpad "
					+ " from cca_auth_txlog " + " where tx_date =? " + " and tx_time =?" + " and card_no =?"
					+ this.sqlRownum(1);
			sqlSelect(sql1,
					new Object[] { wp.colStr(ii, "tx_date"), wp.colStr(ii, "db_time"), wp.colStr(ii, "card_no") });

			if (sqlRowNum > 0) {
				wp.colSet(ii, "db_curr_limit", sqlStr("db_curr_limit"));
				wp.colSet(ii, "db_curr_unpad", sqlStr("db_curr_unpad"));
			} else {
				wp.colSet(ii, "db_curr_limit", "" + 0);
				wp.colSet(ii, "db_curr_unpad", "" + 0);
			}
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
		wp.colSet("ex_tx_cnt", "3");
		wp.colSet("ex_mcc_code", "5542");
		wp.colSet("ex_bin_no", "460199");

	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "ccar0350";
		String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_tx_date1")) + " -- "
				+ commString.strToYmd(wp.itemStr("ex_tx_date2")) + " 累積次數 >= " + wp.itemStr("ex_tx_cnt") + " MCC Code:"
				+ wp.itemStr("ex_mcc_code") + " Bin No : " + wp.itemStr("ex_bin_no");
		wp.colSet("cond1", cond1);
		wp.colSet("user_id", wp.loginUser);
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar0350.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

}
