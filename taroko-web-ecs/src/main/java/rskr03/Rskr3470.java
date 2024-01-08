package rskr03;
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3470 extends BaseAction implements InfacePdf  {

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
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		if (chkStrend(wp.itemStr("ex_proc_date1"), wp.itemStr("ex_proc_date2")) == false) {
			alertErr("處理日期: 起迄錯誤");
			return;
		}
		
		String lsWhere = " where 1=1 "
				+sqlCol(wp.itemStr("ex_proc_date1"),"proc_date",">=")
				+sqlCol(wp.itemStr("ex_proc_date2"),"proc_date","<=")
				+sqlCol(wp.itemStr("ex_mod_user"),"mod_user","like%")
				;
		
		sqlParm.setSqlParmNoClear(true);
		listSum(lsWhere);
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();	
	}
	
	void listSum(String lsWhere) {
		String sql1 = " select "
				+ " count(*) as db_cnt1 , "
				+ " sum(decode(status_code,'9',1,0)) as db_cnt2 , "
				+ " sum(decode(status_code,'5',1,0)) as db_cnt3 , "
				+ " sum(decode(proc_result,'02',1,0)) as db_cnt4 , "
				+ " sum(decode(proc_result,'03',1,0)) as db_cnt5 , "
				+ " sum(decode(proc_result,'04',1,0)) as db_cnt6 , "
				+ " sum(decode(proc_result,'05',1,0)) as db_cnt7 "
				+ " from rsk_factormaster "
				+ lsWhere
				;
		
		sqlSelect(sql1);
		if(sqlRowNum > 0 ) {
			wp.colSet("tl_cnt1", sqlStr("db_cnt1"));
			wp.colSet("tl_cnt2", sqlStr("db_cnt2"));
			wp.colSet("tl_cnt3", sqlStr("db_cnt3"));
			wp.colSet("tl_cnt4", sqlStr("db_cnt4"));
			wp.colSet("tl_cnt5", sqlStr("db_cnt5"));
			wp.colSet("tl_cnt6", sqlStr("db_cnt6"));
			wp.colSet("tl_cnt7", sqlStr("db_cnt7"));
		}	else	{
			wp.colSet("tl_cnt1", "0");
			wp.colSet("tl_cnt2", "0");
			wp.colSet("tl_cnt3", "0");
			wp.colSet("tl_cnt4", "0");
			wp.colSet("tl_cnt5", "0");
			wp.colSet("tl_cnt6", "0");
			wp.colSet("tl_cnt7", "0");
		}		
	}
	
	@Override
	public void queryRead() throws Exception {
		wp.selectSQL = " mod_user , count(*) as db_cnt1 , "
				+ " sum(decode(status_code,'9',1,0)) as db_cnt2 , "
				+ " sum(decode(status_code,'5',1,0)) as db_cnt3 , "
				+ " sum(decode(proc_result,'02',1,0)) as db_cnt4 , "
				+ " sum(decode(proc_result,'03',1,0)) as db_cnt5 , "
				+ " sum(decode(proc_result,'04',1,0)) as db_cnt6 , "
				+ " sum(decode(proc_result,'05',1,0)) as db_cnt7 "
				;
		
		wp.daoTable = "rsk_factormaster";
		wp.whereOrder = " group by mod_user order by mod_user ";
		pageQuery();
		if (sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}
		
		wp.setListCount(0);		
		queryReadAfter();
	}
	
	void queryReadAfter() {
		int ii=0;
		ii = wp.selectCnt;
		String sql1 = " select usr_cname from sec_user where usr_id = ? ";
		for(int z=0 ; z<ii;z++) {
			sqlSelect(sql1,new Object[] {wp.colStr(z,"mod_user")});
			if(sqlRowNum > 0 ) {
				wp.colSet(z,"wk_mod_user", wp.colStr(z,"mod_user")+"_"+sqlStr("usr_cname"));
			} else	{
				wp.colSet(z,"wk_mod_user", wp.colStr(z,"mod_user"));
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
		wp.reportId = "rskr3470";		
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		taroko.base.CommDate commDate = new taroko.base.CommDate(); 
		String ss = "";
		ss = "處理日期 : " + commDate.dspDate(wp.itemStr("ex_proc_date1"));
		if(wp.itemEmpty("ex_proc_date2") == false )
			ss += " - " + commDate.dspDate(wp.itemStr("ex_proc_date2"));
		if(wp.itemEmpty("ex_mod_user") == false)
			ss += " 處理人員 : "+wp.itemStr("ex_mod_user");
		wp.colSet("cond1", ss);
		wp.fileMode = "Y";
		pdf.excelTemplate = "rskr3470.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
		
	}

}
