package rskr05;

import ofcapp.BaseAction;

public class Rskq9900 extends BaseAction {

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
		if(wp.itemEmpty("ex_idno")) {
			alertErr("身分證ID: 不可空白");
			return ;
		}
		
		String lsWhere = " where 1=1 ";
		if(wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"id_no")+")";
		}
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = "uf_idno_id(id_p_seqno) as id_no , pd_rating_new , pd_rating_old , crt_date_new , crt_date_old , "
				+ " (otb*10000) as otb , rev_rate ";
		
		wp.daoTable = "rsk_pcic";
		
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
