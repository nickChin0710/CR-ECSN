package rskm02;

import ofcapp.BaseAction;

public class Rskp2400 extends BaseAction {

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
		if(chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
			alertErr("建檔日期: 起訖錯誤");
			return ;			
		}
		
		String lsWhere = " where 1=1 and apr_flag <> 'Y' "
				+ sqlCol(wp.itemStr("ex_crt_date1"),"crt_date",">=")
				+ sqlCol(wp.itemStr("ex_crt_date2"),"crt_date","<=")				
				+ sqlCol(wp.itemStr("ex_review_date"),"review_date")				
				;
		
		if(wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"),"id_no")+")";
		}
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " hex(rowid) as rowid , acct_type , uf_acno_key(acno_p_seqno) as acct_key , block_code , review_date , crt_date , acno_p_seqno ";
		wp.daoTable = " rsk_review_block ";
		wp.whereOrder = " order by crt_date ";
		
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
		int llOk = 0 , llErr = 0 ;
		rskm02.Rskp2400Func func = new rskm02.Rskp2400Func();
		func.setConn(wp);
		
		String[] aaOpt = wp.itemBuff("opt");		
	    wp.listCount[0] = wp.itemRows("rowid");
	    optNumKeep(wp.listCount[0]);
	    if (optToIndex(aaOpt[0]) < 0) {
	      alertErr2("請選取欲處理之批號");
	      return;
	    }
	    
	    int rr =-1;
	    for (int ii = 0; ii < aaOpt.length; ii++) {
	    	 rr = this.optToIndex(aaOpt[ii]);
	         if (rr < 0) {
	           continue;
	         }
	         func.varsSet("acno_p_seqno", wp.itemStr(rr, "acno_p_seqno"));
	         func.varsSet("block_reason4", wp.itemStr(rr, "block_code"));	         
	         func.varsSet("review_date", wp.itemStr(rr, "review_date"));
	         
	         optOkflag(rr);
	         int liRc = func.dataProc();
	         sqlCommit(liRc);
	         optOkflag(rr, liRc);
	         if (liRc == 1) {
	           llOk++;
	         } else {
	        	 wp.colSet(rr, "error_desc",func.errorDesc);
	        	 llErr++;
	         }
	    }
	    alertMsg("覆核完成; OK=" + llOk + ", ERR=" + llErr);
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
