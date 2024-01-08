package ccar01;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import ofcapp.InfacePdf;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Ccar2040 extends BaseAction implements InfacePdf , InfaceExcel {

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
		if(chkStrend(wp.itemStr("ex_log_date1"), wp.itemStr("ex_log_date2")) == false) {
			alertErr2("日期起迄錯誤");
			return ;
		}
		
		getWhere();

	}
	
	void getWhere() throws Exception {
		
		String lsWhere = " where 1=1 and kind_flag ='A' and log_mode ='1' and log_type in ('3','4') "
				+ sqlCol(wp.itemStr("ex_log_date1"),"log_date",">=")
				+ sqlCol(wp.itemStr("ex_log_date2"),"log_date","<=")		
				+ sqlCol(wp.itemStr("ex_block_code"),"block_reason||','||block_reason2||','||block_reason3||','||block_reason4||','||block_reason5","%like%")
				+ sqlCol(wp.itemStr("ex_spec_status"),"spec_status")
				+ sqlCol(wp.itemStr("ex_log_user"),"mod_user")
				;
		
		if(wp.itemEmpty("ex_card_no") == false) {
			lsWhere += " and acno_p_seqno in (select A.acno_p_seqno from cca_card_base A where 1=1 "
					+ sqlCol(wp.itemStr("ex_card_no"),"A.card_no")
					+ " ) ";
		}
		
		if(wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and id_p_seqno in (select A.id_p_seqno from crd_idno A where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"),"A.id_no")
					+ " union select B.id_p_seqno from dbc_idno B where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"),"B.id_no")
					+ " ) "
					;
		}
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
		
		
	}
	
	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		
		wp.selectSQL = " log_date , uf_idno_id2(id_p_seqno,acct_type) as id_no , "
				+ " uf_idno_name2(id_p_seqno,acct_type) as chi_name , block_reason , block_reason2 , block_reason3 , "
				+ " block_reason4 , block_reason5 ,spec_status , spec_del_date , decode(log_type,'3','指定','4','刪除') as tt_aud_code , "
				+ " log_remark , mod_user "
				;
		
		wp.daoTable = "rsk_acnolog";
		wp.whereOrder = " order by log_date Asc ";
		
		pageQuery();
		if (sqlRowNum <= 0) {
			alertErr2(this.appMsg.errCondNodata);
			return;
		}
		
		wp.setListCount(0);
		wp.setPageValue();
		
		queryAfter();
		
	}

	void queryAfter() throws Exception {
		
		String tmpBlockCode = "";
		
		for(int ii=0;ii<wp.selectCnt;ii++) {
			tmpBlockCode = "";
			if(wp.colEmpty(ii,"block_reason") == false) {
				if(tmpBlockCode.isEmpty())
					tmpBlockCode += wp.colStr(ii,"block_reason");
				else	
					tmpBlockCode += ","+ wp.colStr(ii,"block_reason");
			}
			if(wp.colEmpty(ii,"block_reason2") == false) {
				if(tmpBlockCode.isEmpty())
					tmpBlockCode += wp.colStr(ii,"block_reason2");
				else	
					tmpBlockCode += ","+ wp.colStr(ii,"block_reason2");
			}
			if(wp.colEmpty(ii,"block_reason3") == false) {
				if(tmpBlockCode.isEmpty())
					tmpBlockCode += wp.colStr(ii,"block_reason3");
				else	
					tmpBlockCode += ","+ wp.colStr(ii,"block_reason3");
			}
			if(wp.colEmpty(ii,"block_reason4") == false) {
				if(tmpBlockCode.isEmpty())
					tmpBlockCode += wp.colStr(ii,"block_reason4");
				else	
					tmpBlockCode += ","+ wp.colStr(ii,"block_reason4");
			}
			if(wp.colEmpty(ii,"block_reason5") == false) {
				if(tmpBlockCode.isEmpty())
					tmpBlockCode += wp.colStr(ii,"block_reason5");
				else	
					tmpBlockCode += ","+ wp.colStr(ii,"block_reason5");
			}
			wp.colSet(ii,"block_code", tmpBlockCode);
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
		    wp.reportId = "ccar2040";		      
		    TarokoExcel xlsx = new TarokoExcel();
		    wp.fileMode = "Y";
		    xlsx.excelTemplate = "ccar2040.xlsx";
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

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "ccar2040";		
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar2040.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
		
	}

}
