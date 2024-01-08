package rskr02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr2410 extends BaseAction implements InfacePdf {

	@Override
	public void userAction() throws Exception {
		switch (wp.buttonCode) {
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
     	    // -PDF列印-
	    	pdfPrint();
		    break;  
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
		
		String lsWhere = " where 1=1 "				
				+sqlCol(wp.itemStr("ex_date1"),"review_month")				
				;
		
		if(wp.itemEmpty("ex_corp_no") == false) {
			lsWhere += " and corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1 "
					+ sqlCol(wp.itemStr("ex_corp_no"),"corp_no")
					+")";
		}
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " uf_corp_no(corp_p_seqno) as corp_no , uf_corp_name(corp_p_seqno) as corp_name , card_no ,"
					 + " uf_chi_name(card_no) as chi_name , review_month , card_since , id_limit , card_limit , "
					 + " corp_tel , last_year_consume , this_year_consume , crt_date , crt_time "
					 ;
		
		wp.daoTable = "rsk_review_corp_list";
		
		pageQuery();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
		wp.setListCount(0);
		wp.setPageValue();
		queryReadAfter();
	}
	
	void queryReadAfter() throws Exception {
		
		for(int ii=0;ii<wp.selectCnt;ii++) {
			wp.colSet(ii, "card_no_6",wp.colStr(ii,"card_no").substring(wp.colStr(ii,"card_no").length()-6, wp.colStr(ii,"card_no").length()));
			wp.colSet(ii, "review_month",wp.colStr(ii,"review_month").substring(4));
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
		if(wp.itemEmpty("ex_user_bank_name"))
			getUserBankNo();

	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "Rskr2410";
	    wp.pageRows = 9999;
	    queryFunc();	    
	    wp.colSet("cond1", wp.itemStr("ex_user_bank_name"));
	    TarokoPDF pdf = new TarokoPDF();
	    pdf.pageCount =30;

	    wp.fileMode = "Y";
	    pdf.excelTemplate = "rskr2410.xlsx";
	    pdf.sheetNo = 0;
	    pdf.procesPDFreport(wp);
	    pdf = null;
	    return;
		
	}
	
	void getUserBankNo() {
		String bankUnitNo = "" , bankName = "";
		
		String sql1 = " select bank_unitno from sec_user where 1=1 " +sqlCol(wp.loginUser,"usr_id");
		sqlSelect(sql1);
		
		bankUnitNo = sqlStr("bank_unitno");
		
		if(bankUnitNo.isEmpty())
			return ;
		
		String sql2 = " select full_chi_name from gen_brn where 1=1 " +sqlCol(bankUnitNo,"branch");
		sqlSelect(sql2);
		
		bankName = sqlStr("full_chi_name");
		
		wp.colSet("ex_user_bank", bankUnitNo);
		wp.colSet("ex_user_bank_name", bankName);				
	}
	
}
