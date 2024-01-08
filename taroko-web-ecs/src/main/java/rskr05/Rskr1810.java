package rskr05;

import java.math.BigDecimal;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr1810 extends BaseAction implements InfacePdf {

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
			// --報表列印
			pdfPrint();
			break;
	      default:
	        break;
	    }

	}

	@Override
	public void dddwSelect() {		
		try {			
		    if (eqIgno(wp.respHtml, "rskr1810")) {		    	
		        wp.initOption = "--";
		        wp.optionKey = wp.colStr(0, "ex_branch");
		        dddwList("d_dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1");
		    }
		} catch (Exception ex) {}
	}

	@Override
	public void queryFunc() throws Exception {
		if(wp.itemEmpty("ex_date1")) {
			alertErr("資料日期不可空白");
			return ;
		}
		
		String lsWhere = " where 1=1 "
					   +sqlCol(wp.itemStr("ex_branch"),"branch")
					   +sqlCol(wp.itemStr("ex_date1"),"data_date")
					   ;
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " data_date , branch , branch_name , corp_cnt , corp_card_cnt , corp_card_ratio , "
					 + " charge_card_cnt , charge_card_ratio , credit_corp_cnt , credit_corp_ratio "
					 ;
		
		wp.daoTable = "rsk_crm36a_rpt";
		wp.whereOrder = "order by branch Asc ";
		
		pageQuery();
		
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
		wp.setPageValue();
		wp.setListCount(0);
		queryAfter();
	}
	
	void queryAfter() throws Exception {
		int tlCorpCnt = 0 , tlCorpCardCnt = 0 , tlChargeCardCnt = 0 , tlCreditCorpCnt = 0;
		double tlCorpRatio = 0.0 , tlChargeCardRatio = 0.0 , tlCreditCorpRatio = 0.0;
		String sql1 = " select "
					+ " sum(corp_cnt) as tl_corp_cnt , "
					+ " sum(corp_card_cnt) as tl_corp_card_cnt , "
					+ " sum(charge_card_cnt) as tl_charge_card_cnt , "
					+ " sum(credit_corp_cnt) as tl_credit_corp_cnt "
					+ " from rsk_crm36a_rpt where data_date = ? ";
		
		if(wp.itemEmpty("ex_branch") == false) {
			sql1 += " and branch = ? ";
			sqlSelect(sql1,new Object[] {wp.itemStr("ex_date1"),wp.itemStr("ex_branch")});
		}	else	{
			sqlSelect(sql1,new Object[] {wp.itemStr("ex_date1")});
		}		
		
		if(sqlRowNum > 0) {
			tlCorpCnt = sqlInt("tl_corp_cnt");
			tlCorpCardCnt = sqlInt("tl_corp_card_cnt");
			tlChargeCardCnt = sqlInt("tl_charge_card_cnt");
			tlCreditCorpCnt = sqlInt("tl_credit_corp_cnt");
			
			//--計算
			tlCorpRatio = divide((double) tlCorpCardCnt / (double) tlCorpCnt*100);
			tlChargeCardRatio = divide((double) tlChargeCardCnt / (double) tlCorpCnt*100);
			tlCreditCorpRatio = divide((double) tlCreditCorpCnt / (double) tlCorpCnt*100);
			
			//--放值
			
			wp.colSet("tl_corp_cnt", tlCorpCnt);
			wp.colSet("tl_corp_card_cnt", tlCorpCardCnt);
			wp.colSet("tl_charge_card_cnt", tlChargeCardCnt);
			wp.colSet("tl_credit_corp_cnt", tlCreditCorpCnt);
			wp.colSet("tl_corp_ratio", tlCorpRatio);
			wp.colSet("tl_charge_card_ratio", tlChargeCardRatio);
			wp.colSet("tl_credit_corp_ratio", tlCreditCorpRatio);
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
		wp.colSet("ex_date1", getSysDate());
	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "Rskr1810";
	    wp.pageRows = 9999;
	    String cond1 = "";	    
	    wp.colSet("cond1", cond1);
	    wp.colSet("user_id", wp.loginUser);
	    queryFunc();
	    TarokoPDF pdf = new TarokoPDF();
	    wp.fileMode = "Y";
	    pdf.excelTemplate = "rskr1810.xlsx";
	    pdf.pageCount = 30;
	    pdf.sheetNo = 0;
	    pdf.procesPDFreport(wp);
	    pdf = null;
	    return;			
	}
	
	public static double divide(double c) {
		BigDecimal b = new BigDecimal(c);
		return b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
}
