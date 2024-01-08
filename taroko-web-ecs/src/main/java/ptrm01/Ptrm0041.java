package ptrm01;

import ofcapp.BaseAction;

public class Ptrm0041 extends BaseAction {
	String fiscCode = "";
	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
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
				+sqlCol(wp.itemStr("ex_tcb_bin"),"tcb_bin","like%")
				+sqlCol(wp.itemStr("ex_fisc_code"),"fisc_code")
				;
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = "tcb_bin , fisc_code ";
		wp.daoTable = "fsc_bin_group ";
		wp.whereOrder = " order by fisc_code Asc ";
		pageQuery();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		wp.setListCount(0);
		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		fiscCode = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if(fiscCode.isEmpty()) {
			fiscCode = itemkk("fisc_code");
		}
		
		if(fiscCode.isEmpty()) {
			errmsg("財金代號: 不可空白");
			return ;
		}
		
		wp.selectSQL = "tcb_bin , fisc_code , hex(rowid) as rowid ";
		wp.daoTable = "fsc_bin_group ";
		wp.whereStr = " where 1=1 "
				+sqlCol(fiscCode,"fisc_code")
				;
		
		pageSelect();
		if(sqlNotFind()) {
			errmsg("此條件查無資料");
			return ;
		}		
	}

	@Override
	public void saveFunc() throws Exception {
		ptrm01.Ptrm0041Func func = new ptrm01.Ptrm0041Func();
		func.setConn(wp);
		
		if(checkApproveZz() == false)			
			return ;		
		
		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if(rc!=1) {
			alertErr(func.getMsg());
		}	else
			saveAfter(false);
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		if(wp.respHtml.equals("ptrm0041_detl")) {
			btnModeAud();
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
