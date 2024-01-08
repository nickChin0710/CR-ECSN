package rskr05;
/**
 * 2023-0905   JH    bug-fix
 * */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr2200 extends BaseAction implements InfacePdf {

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
		
	}

	@Override
	public void queryFunc() throws Exception {
		if(wp.itemEmpty("ex_date1") && wp.itemEmpty("ex_date2") && wp.itemEmpty("ex_idno")) {
			alertErr("查詢條件不可全部空白");
			return ;
		}
		
		String lsWhere = " where 1=1 "
				+sqlCol(wp.itemStr("ex_date1"),"A.log_date",">=")
				+sqlCol(wp.itemStr("ex_date2"),"A.log_date","<=")				
				;
		
		if(wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and A.id_p_seqno in (select id_p_seqno from crd_idno_seqno where 1=1 "
					+sqlCol(wp.itemStr("ex_idno"),"id_no")
					+ " ) ";
		}
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr ;
		wp.setQueryMode();
		queryRead();		
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " A.log_date "
            +", ( select id_no from crd_idno_seqno where id_p_seqno =A.id_p_seqno  FETCH FIRST 1 ROWS ONLY ) as id_no "
				+", A.last_pd_rating_old , A.pd_rating_old "
            +", decode(A.status,'U','升高','D','降低','S','不變') as tt_status "
      +", log_date "
				;
		wp.daoTable = "rsk_pcic_log A";
		wp.whereOrder = " order by A.log_date Desc ";
		
		pageQuery();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
		wp.setListCount(0);
		wp.setPageValue();		
		queryAfter();
	}
	
	void queryAfter() {
		String sql1 = "select chi_name from crd_idno where 1=1 and id_no = ? ";
		String sql2 = "select chi_name from dbc_idno where 1=1 and id_no = ? ";
		for(int ii=0;ii<wp.selectCnt;ii++) {
			sqlSelect(sql1,new Object[] {wp.colStr(ii,"id_no")});
			if(sqlRowNum > 0) {
				wp.colSet(ii, "chi_name",sqlStr("chi_name"));
			}	else	{
				sqlSelect(sql2,new Object[] {wp.colStr(ii,"chi_name")});
				if(sqlRowNum > 0) {
					wp.colSet(ii, "chi_name",sqlStr("chi_name"));
				}
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
		wp.reportId = "Rskr2200";
	    wp.pageRows = 9999;
	    String cond1 = "";	    
	    wp.colSet("cond1", cond1);
	    wp.colSet("user_id", wp.loginUser);
	    queryFunc();
	    TarokoPDF pdf = new TarokoPDF();
	    wp.fileMode = "Y";
	    pdf.excelTemplate = "rskr2200.xlsx";
	    pdf.pageCount = 30;
	    pdf.sheetNo = 0;
	    pdf.procesPDFreport(wp);
	    pdf = null;
	    return;
		
	}
	
	String getBranchName() {
		String sql1 = "select full_chi_name as ex_branch_name from gen_brn where branch = ? ";
		sqlSelect(sql1,new Object[] {wp.itemStr("ex_branch")});
		if(sqlRowNum > 0)
			return sqlStr("ex_branch_name");
		return "";
	}
	
}
