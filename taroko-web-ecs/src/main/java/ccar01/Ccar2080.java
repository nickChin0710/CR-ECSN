/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-06-01  V1.00.00  Ryan   program initial                                *
******************************************************************************/
package ccar01;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ccar2080 extends BaseAction implements InfacePdf , InfaceExcel {

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
		if (chkStrend(wp.itemStr("ex_open_date1"), wp.itemStr("ex_open_date2")) == false) {
			alertErr("開卡日期: 起迄錯誤");
			return;
		}

		String lsWhere = " where 1=1 AND A.CARD_NO = B.CARD_NO AND B.ID_P_SEQNO = C.ID_P_SEQNO AND A.MOD_PGM ='ccam2080' " 
				+ sqlBetween("ex_open_date1", "ex_open_date2", "A.open_date")
				+ sqlCol(wp.itemStr("ex_id_no"), "C.id_no") 
				+ sqlCol(wp.itemStr("ex_card_no"), "A.card_no") 
				+ sqlCol(wp.itemStr("ex_open_user"), "A.open_user");
	
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " A.OPEN_DATE , C.ID_NO , A.CARD_NO , A.OPEN_USER ,A.NEW_END_DATE ,A.OPEN_TIME ";

		wp.daoTable = "CCA_CARD_OPEN A ,CRD_CARD B ,CRD_IDNO C";
		wp.whereOrder = "";

		pageQuery();
		if (sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}

		wp.setListCount(0);
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
		wp.reportId = "ccar2080";		
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar2080.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
	}

	@Override
	public void xlsPrint() throws Exception {
		try {			
		    log("xlsFunction: started--------");
		    wp.reportId = "ccar2080";		      
		    TarokoExcel xlsx = new TarokoExcel();
		    wp.fileMode = "Y";
		    xlsx.excelTemplate = "ccar2080.xlsx";
		    wp.pageRows = 9999;
		    queryFunc();
		    StringBuffer  condStr  = new StringBuffer();
			if (!wp.iempty("ex_open_date1") || !wp.iempty("ex_open_date2")) {
				condStr.append(" 開卡日期 : ");
				condStr.append(wp.itemStr("ex_open_date1"));
				condStr.append("--");
				condStr.append(wp.itemStr("ex_open_date2"));
			}
			if (!wp.iempty("ex_id_no")) {
				condStr.append(" 身份證ID : ");
			    condStr.append(wp.itemStr("ex_id_no"));
			}
			if (!wp.iempty("ex_card_no")) {
				condStr.append(" 卡號 : ");
			    condStr.append(wp.itemStr("ex_card_no"));
			}
			if (!wp.iempty("ex_open_user")) {
				condStr.append(" 開卡人員 : ");
			    condStr.append(wp.itemStr("ex_open_user"));
			}
		    wp.colSet("cond_1", condStr.toString());
		    
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
