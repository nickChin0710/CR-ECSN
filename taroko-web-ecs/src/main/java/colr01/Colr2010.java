/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-07-08  V1.00.01  ryan                                                  * 
******************************************************************************/
package colr01;

import java.math.BigDecimal;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Colr2010 extends BaseAction implements InfacePdf {
	CommString commString = new CommString();
	String lsWhere = "";
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
		} else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			procFunc();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			procFunc();
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			strAction = "XLS";
			// xlsPrint();
		} else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
			strAction = "PDF";
			pdfPrint();
		}

	}

	@Override
	public void dddwSelect() {

	}

	@Override
	public void queryFunc() throws Exception {
		if(wp.itemLen("ex_in_year")!=4) {
			alertErr("日期格式錯誤");
			return;
		}
		String sqlSelect = "select count(*) cnt from col_rtn_rate where in_year = ?";
		setString(1,wp.itemStr("ex_in_year"));
		sqlSelect(sqlSelect);
		if(sqlInt("cnt")==0) {
			alertErr("此年份未產生報表資料 ");
			return;
		}
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.sqlCmd = " select in_year ,  in_stop_cnt ,  in_active_cnt ,  in_percent  from col_rtn_rate "
				+ " where 1=1 and in_year between ? and ?  order by in_year ";
		setString(1, String.format("%s", toInt(wp.itemStr("ex_in_year"))-2));
		setString(2, wp.itemStr("ex_in_year"));
		pageQuery();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr("此年份未產生報表資料 ");
			return;
		}
		listWkdata(wp.selectCnt);
	}
	
	void listWkdata(int selectCnt) throws Exception {
		double inPercent = 0;
		double inPercentAverage = 0;
		 for (int ii = 0; ii < selectCnt; ii++) {
			 inPercent = doubleAdd(inPercent,wp.colNum(ii,"in_percent"));
		 }
		 inPercentAverage = inPercent/selectCnt;
		 wp.colSet("in_percent_average", inPercentAverage);
	}

	public Double doubleAdd(Double d1, Double d2) {

		BigDecimal b1 = new BigDecimal(d1.toString());
		BigDecimal b2 = new BigDecimal(d2.toString());
		return b1.add(b2).doubleValue();
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
		wp.reportId = "colr2010";
		wp.pageRows = 9999;
		wp.respHtml = "TarokoErrorPDF";

		if (wp.iempty("ex_in_year")) {
			alertErr("請輸入統計年份");
			return;
		}
		wp.colSet("cond1", String.format("%s %s", "統計年份:",wp.itemStr("ex_in_year")));

		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "colr2010.xlsx";
		pdf.pageCount = 25;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
		return;

	}

}
