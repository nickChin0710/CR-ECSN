package rskr01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr1900 extends BaseAction implements InfacePdf {

	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;		
		defaultAction();
		//--
		switch (wp.buttonCode) {			
		    case "PDF": //-PDF-		    	
		        strAction = "PDF";
		        pdfPrint();
		        break;
		}
	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		
		String lsWhere = " where 1=1 "
					   + sqlCol(wp.itemStr("ex_date1"),"A.gen_date",">=")
					   + sqlCol(wp.itemStr("ex_date2"),"A.gen_date","<=")		
					   + sqlCol(wp.itemStr("ex_card_no"),"A.card_no","like%")
					   ;
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " A.ctrl_seqno , A.card_no , A.gen_date , A.ac_no , decode(dbcr,'D','借','C','貸') as tt_dbcr , A.std_vouch_cd , "
					 + " decode(dbcr,'D',A.prb_amount,0) as dbcr_amt1 , "
					 + " decode(dbcr,'C',A.prb_amount,0) as dbcr_amt2 , "
					 + " decode(B.mcht_chi_name,'',B.mcht_eng_name,B.mcht_chi_name) as mcht_name , "
					 + " B.purchase_date "
					 ;
		wp.daoTable = "rsk_gen_detail A join rsk_problem B on A.ctrl_seqno = B.ctrl_seqno";
		wp.whereOrder = "order by A.gen_date Asc , A.std_vouch_cd Asc , A.card_no Asc ";
		
		pageQuery();
		
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;		
		}
		
		wp.setPageValue();
		wp.setListCount(0);
		
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
		wp.reportId = "rskr0040";
		wp.colSet("user_id", wp.loginUser);
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "rskr1900.xlsx";
		pdf.pageCount = 33;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
		return;		
	}

}
