package ccar01;

import ofcapp.BaseAction;

public class Ccar5510 extends BaseAction {

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
		if(chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr("建檔日期: 起迄錯誤");
			return ;
		}
		
		String lsWhere = " where 1=1 "
				+ sqlCol(wp.itemStr("ex_cellar_phone"),"cellar_phone","like%")
				+ sqlCol(wp.itemStr("ex_date1"),"crt_date",">=")
				+ sqlCol(wp.itemStr("ex_date2"),"crt_date","<=")
				;
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr ;
		wp.setQueryMode();
		
		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " cellar_phone , remark , crt_user , crt_date , decode(aud_code,'A','新增','U','修改','D','刪除') as tt_aud_code ";
		wp.daoTable = " cca_mobile_black_list_log ";
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

}
