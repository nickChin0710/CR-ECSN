package ccar01;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ccar2030 extends BaseAction implements InfacePdf , InfaceExcel {

	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
		switch (strAction) {
		case "X":
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
			break;
		case "Q":
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
			break;
		case "R":
			// -資料讀取-
			strAction = "R";
			dataRead();
			break;
		case "A":
			/* 新增功能 */
			saveFunc();
			break;
		case "U":
			/* 更新功能 */
			saveFunc();
			break;
		case "D":
			/* 刪除功能 */
			saveFunc();
			break;
		case "M":
			/* 瀏覽功能 :skip-page */
			queryRead();
			break;
		case "S":
			/* 動態查詢 */
			querySelect();
			break;
		case "L":
			/* 清畫面 */
			strAction = "";
			clearFunc();
			break;
		case "C":
			// -資料處理-
			procFunc();
			break;
		case "PDF":
			// --報表列印
			pdfPrint();
			break;
		case "XLS":
			// --列印Excel
			xlsPrint();
		default:
			break;
		}
	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		if (chkStrend(wp.itemStr("ex_log_date1"), wp.itemStr("ex_log_date2")) == false) {
			alertErr("指示日期: 起迄錯誤");
			return;
		}

		String lsWhere = " where 1=1 " + sqlBetween("ex_log_date1", "ex_log_date2", "log_date")
				+ sqlCol(wp.itemStr("ex_card_no"), "card_no") + sqlCol(wp.itemStr("ex_spec_status"), "spec_status")
				+ sqlCol(wp.itemStr("ex_log_user"), "log_user");

		if (wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and card_no in (select A.card_no from crd_card A join crd_idno B on A.major_id_p_seqno = B.id_p_seqno ";
			lsWhere += " where 1=1 " + sqlCol(wp.itemStr("ex_idno"), "B.id_no") + " union all select C.card_no ";
			lsWhere += " from dbc_card C join dbc_idno D on C.id_p_seqno = D.id_p_seqno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"), "D.id_no") + ") ";
		}
		
		if(wp.itemEq("ex_from_type", "1")) {
			lsWhere += " and from_type = '1' ";			
		}	else if(wp.itemEq("ex_from_type", "2")) {
			lsWhere += " and from_type = '2' ";			
		}		
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " log_date , log_time , card_no , bin_type , spec_status , spec_del_date , "
				+ " aud_code , decode(aud_code,'A','指定','D','刪除') as tt_aud_code , spec_remark , log_user , "
				+ " decode(from_type,'1','人工','2','批次',from_type) as tt_from_type "
//				+ " uf_idno_id2(card_no,'') as id_no , uf_idno_name2(card_no,'') as chi_name "
		;

		wp.daoTable = "cca_spec_his";
		wp.whereOrder = " order by log_date Asc , log_time Asc ";

		pageQuery();
		if (sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}

		wp.setListCount(0);
		wp.setPageValue();
		queryAfter();
	}

	void queryAfter() {
		int selectCnt = 0;
		selectCnt = wp.selectCnt;

		String sql1 = "select id_p_seqno from cca_card_base where card_no = ? ";
		String sql2 = "select id_no , chi_name from crd_idno where id_p_seqno = ? " + "union all "
				+ "select id_no , chi_name from dbc_idno where id_p_seqno = ? ";

		for (int ii = 0; ii < selectCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "card_no") });
			if (sqlRowNum <= 0)
				continue;
			sqlSelect(sql2, new Object[] { sqlStr("id_p_seqno"), sqlStr("id_p_seqno") });
			if (sqlRowNum > 0) {
				sql2wp(ii, "id_no");
				sql2wp(ii, "chi_name");
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
		wp.reportId = "ccar2030";		
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar2030.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
	}

	@Override
	public void xlsPrint() throws Exception {
		try {			
		    log("xlsFunction: started--------");
		    wp.reportId = "ccar2030";		      
		    TarokoExcel xlsx = new TarokoExcel();
		    wp.fileMode = "Y";
		    xlsx.excelTemplate = "ccar2030.xlsx";
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
