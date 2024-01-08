/* 失敗原因類別統計表 V.2018-0802.Alex
 * 109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard   
 * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *   
 * */
package ccar01;

import java.util.ArrayList;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ccar0082 extends BaseAction implements InfaceExcel, InfacePdf {
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
		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			wp.colSet("tot_cnt", "0");
			wp.colSet("tot_amt", "0");
			alertErr2("日期起迄：輸入錯誤");
			return;
		}

		if (wp.itemStr("ex_date2").compareTo(getSysDate()) >= 0) {
			wp.colSet("tot_cnt", "0");
			wp.colSet("tot_amt", "0");
			alertErr2("日期迄日：不可大於等於系統日");
			return;
		}

		String lsWhere = " where 1=1  " + sqlCol(wp.itemStr("ex_date1"), "tx_date", ">=")
				+ sqlCol(wp.itemStr("ex_date2"), "tx_date", "<=")
				+ sqlCol(wp.itemStr("ex_resp_code"), "auth_status_code");
		
		String sumWhere = " where 1=1 ";
		if(wp.itemEmpty("ex_date1")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_date1"), "tx_date",">=");
	    	whereParams.add(wp.itemStr("ex_date1"));
	    }
		
	    if(wp.itemEmpty("ex_date2")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_date2"), "tx_date","<=");
	    	whereParams.add(wp.itemStr("ex_date2"));
	    }
	    
	    if(wp.itemEmpty("ex_resp_code")==false) {
	    	sumWhere += commSqlStr.col(wp.itemStr("ex_resp_code"), "auth_status_code");
	    	whereParams.add(wp.itemStr("ex_resp_code"));
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
		String sql1 = " select " + " count(*) as tot_cnt , " + " sum(nt_amt) as tot_amt "
				+ " from (select distinct auth_status_code , tx_date , nt_amt , mcht_no , card_no , auth_no from cca_auth_txlog "
				+ lsWhere + ")";

		sqlSelect(sql1,tmpObjParams);
		wp.colSet("tot_cnt", "" + sqlStr("tot_cnt"));
		wp.colSet("tot_amt", "" + sqlStr("tot_amt"));
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		// BRD：同天、同卡、同店、同金額、同失敗原因之失敗交易，僅計算一筆。少寫了一個判斷值，請多增加判斷「同授權碼」。(因為統計出來的值不對).
		wp.sqlCmd = " select " + " auth_status_code as resp_code , "
				+ " uf_tt_resp_code(auth_status_code) as tt_resp_code , " + " count(*) as tx_cnt , "
				+ " sum(nt_amt) as tx_amt , " + " tx_date " + " from "
				+ " (select distinct auth_status_code , tx_date , nt_amt , mcht_no , card_no , auth_no from cca_auth_txlog "
				+ wp.whereStr + ")" + " group by auth_status_code , tx_date order by auth_status_code ";

		logSql();

		wp.pageCountSql = "" + " select count(*) from ( " + " select auth_status_code as resp_code , tx_date "
				+ " from ( "
				+ " select distinct auth_status_code , tx_date , nt_amt , mcht_no , card_no , auth_no from cca_auth_txlog "
				+ wp.whereStr + " )" + " group by auth_status_code,tx_date " + " )";

		pageQuery();

		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			wp.colSet("tot_cnt", "0");
			wp.colSet("tot_amt", "0");
			alertErr2("此條件查無資料");
			return;
		}

		wp.setPageValue();

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
		wp.reportId = "Ccar0082";

		String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
				+ commString.strToYmd(wp.itemStr("ex_date2"));
		wp.colSet("cond1", cond1);
		wp.colSet("user_id", wp.loginUser);
		wp.pageRows = 9999;
		queryFunc();

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar0082_pdf.xlsx";
		pdf.pageCount = 28;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

	@Override
	public void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = "Ccar0082";
			String cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
					+ commString.strToYmd(wp.itemStr("ex_date2"));
			wp.colSet("cond1", cond1);
			wp.colSet("user_id", wp.loginUser);
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
			xlsx.excelTemplate = "ccar0082_excel.xlsx";
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
