/* 失敗原因類別統計表 V.2018-0731.jh
 * 109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard   
 * 109-01-04  V1.00.02   shiyuqi                修改无意义命名
 * 110-01-05  V1.00.03  Tanwei                 zzDate,zzStr,zzComm,zzCurr變量更改         *  *   
 * 110-01-14  V1.00.04  Justin                    fix parameterize sql bugs   
 * 110-01-28  V1.00.05  Justin                    fix errors when producing PDF and Excel reports which do not contain any data 
 * */
package ccar01;

import java.util.ArrayList;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ccar0080 extends BaseAction implements InfaceExcel, InfacePdf {
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	boolean ibPrint = false;
	ArrayList<Object> whereParameterList = null;

	@Override
	public void userAction() throws Exception {
		// wp.pgm_version("V.2018-0731");

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
		
		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("日期起迄：輸入錯誤");
			return;
		}

		if (wp.itemStr("ex_date2").compareTo(getSysDate()) >= 0) {
			wp.colSet("tot_cnt", "0");
			wp.colSet("tot_amt", "0");
			alertErr2("日期迄日：不可大於等於系統日");
			return;
		}

		if (commDate.daysBetween(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2")) > 31) {
			alertErr2("日期起迄：最多31天");
			return;
		}

		String lsWhere = " where 1=1 " 
		+ sqlStrend(wp.itemStr2("ex_date1"), wp.itemStr2("ex_date2"), "tx_date");
		
		if (wp.itemEq("ex_area_type", "A") == false) {
			lsWhere += sqlCol(wp.itemStr2("ex_area_type"), "ccas_area_flag");
		}
		// 卡別
		String binType = wp.itemStr2("ex_bin_type");
		if (notEmpty(binType)) {
			lsWhere += " and substr(card_no,1,6) in (select substr(bin_no,1,6) from ptr_bintable where 1=1 "
					+ sqlCol(binType, "bin_type") + ")";
		}
		// BIN_NO:
		lsWhere += sqlCol(wp.itemStr2("ex_bin_no"), "card_no", "like%");
		// 團體代號:
		lsWhere += sqlCol(wp.itemStr2("ex_group_code"), "group_code");
		// 回覆碼:,回覆狀態:
		binType = wp.itemStr2("ex_resp_code");
		if (notEmpty(binType)) {
			lsWhere += sqlCol(binType, "auth_status_code");
		}
		
		sqlParm.setSqlParmNoClear(true);
		listSum(lsWhere);

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	void listSum(String lsWhere) {
		
		String sql1 = " select " + " count(*) as tl_cnt ," + " sum(nt_amt) as tl_amt" + " from "
				+ " (select distinct tx_date,card_no,mcht_no,nt_amt,auth_status_code as resp_code, group_code,auth_no,"
				+ " substr(card_no,1,6) as bin_no,ccas_area_flag as area_type from cca_auth_txlog " + lsWhere + ") ";

		sqlSelect(sql1);

		wp.colSet("tl_cnt", "" + sqlStr("tl_cnt"));
		wp.colSet("tl_amt", "" + sqlStr("tl_amt"));
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (ibPrint == false) {
			wp.sqlCmd = "select bin_no, group_code, resp_code, area_type, count(*) as tx_cnt, sum(nt_amt) as tx_amt"
					+ " from (" + " select distinct"
					+ " tx_date,card_no,mcht_no,nt_amt,auth_status_code as resp_code,auth_no,"
					+ " group_code,substr(card_no,1,6) as bin_no,ccas_area_flag as area_type" + " from cca_auth_txlog"
					+ wp.whereStr + " ) group by bin_no,group_code,resp_code,area_type" + " order by 1,2,3,4";
		} else {
			wp.sqlCmd = "select bin_no, group_code, resp_code, area_type , count(*) as tx_cnt, sum(nt_amt) as tx_amt"
					+ ", uf_bin_type(bin_no) as bin_type" + ", uf_tt_resp_code(resp_code) as tt_resp_code"
					+ ",resp_code||area_type||uf_bin_type(bin_no) as ls_RAB " + " from (" + " select distinct"
					+ " tx_date,card_no,mcht_no,nt_amt,auth_status_code as resp_code,auth_no,"
					+ " group_code,substr(card_no,1,6) as bin_no,ccas_area_flag as area_type" + " from cca_auth_txlog"
					+ wp.whereStr + " ) group by bin_no,group_code,resp_code,area_type" + " order by 1,2,3,4";
		}

		logSql();

		wp.pageCountSql = "" + "select count(*) from ( "
				+ "select bin_no, group_code, resp_code, area_type, count(*) as tx_cnt" + " from (" + " select distinct"
				+ " tx_date,card_no,mcht_no,nt_amt,auth_status_code as resp_code,auth_no,"
				+ " group_code,substr(card_no,1,6) as bin_no,ccas_area_flag as area_type" + " from cca_auth_txlog"
				+ wp.whereStr + " ) group by bin_no,group_code,resp_code,area_type" + " )";

		pageQuery();

		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}

		wfQueryAfter();

		wp.setPageValue();

	}

	void wfQueryAfter() {
		int llNrow = wp.selectCnt;
		for (int ll = 0; ll < llNrow; ll++) {
//			String sql1 = "select uf_bin_type(?) as bin_type" + ", uf_tt_resp_code(?) as tt_resp_code from dual";
			String sql1 = "select uf_tt_resp_code(?) as tt_resp_code from dual";
			sqlSelect(sql1, new Object[] {wp.colStr(ll, "resp_code") });
			if (sqlRowNum > 0) {				
				wp.colSet(ll, "tt_resp_code", sqlStr("tt_resp_code"));
			}
			
			
			String sql2 = "select bin_type from ptr_bintable where 1=1 and bin_no = ? fetch first 1 rows only ";
			sqlSelect(sql2,new Object[] {wp.colStr(ll, "bin_no")});
			if(sqlRowNum > 0) {
				wp.colSet(ll, "bin_type", sqlStr("bin_type"));
			}
			
			if (wp.colEq(ll, "area_type", "F")) {
				wp.colSet(ll, "area_type", "國外");
			} else if (wp.colEq(ll, "area_type", "T")) {
				wp.colSet(ll, "area_type", "國內");
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
		// TODO Auto-generated method stub

	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "ccar0080";
		ibPrint = true;
		String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
				+ commString.strToYmd(wp.itemStr("ex_date2"));
		wp.colSet("cond1", cond1);
		wp.colSet("user_id", wp.loginUser);
		wp.pageRows = 9999;
		queryFunc();		
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar0080_pdf.xlsx";
		pdf.pageCount = 28;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

	@Override
	public void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = "ccar0080";
			String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
					+ commString.strToYmd(wp.itemStr("ex_date2"));
			wp.colSet("cond1", cond1);
			wp.colSet("user_id", wp.loginUser);
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
			xlsx.excelTemplate = "ccar0080_excel.xlsx";
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
