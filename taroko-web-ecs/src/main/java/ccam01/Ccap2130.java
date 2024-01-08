package ccam01;

import ofcapp.BaseAction;

public class Ccap2130 extends BaseAction {

	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
		switch (strAction) {
		case "X":			
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
		case "C1":
			// -資料處理-
			procFunc2();
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
			alertErr("建檔日期起迄錯誤");
			return;
		}
		
		String lsWhere = " where 1=1 and data_from ='O' and is_repay <> 'Y' "
				+ sqlCol(wp.itemStr("ex_date1"),"crt_date",">=")
				+ sqlCol(wp.itemStr("ex_date2"),"crt_date","<=")
				+ sqlCol(wp.itemStr("ex_crt_user"),"crt_user")
				+ sqlCol(wp.itemStr("ex_card_no"),"pay_card_no","like%")
				;
		
		if(wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"),"id_no")
					+" ) ";						
		}
		
		if(wp.itemEq("ex_apr_flag", "Y")) {
			lsWhere += " and apr_flag in ('Y','') ";
		}	else if(wp.itemEq("ex_apr_flag", "N")) {
			lsWhere += " and apr_flag = 'N' ";
		}
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();				
	}

	@Override
	public void queryRead() throws Exception {

		wp.selectSQL = " pay_date , pay_time , pay_card_no , uf_chi_name(uf_idno_id(id_p_seqno)) as chi_name , payment_type , sign , pay_amt , "
					+ " remark , crt_date , crt_time , crt_user , "
					+ " decode((select wf_desc from ptr_sys_idtab where wf_type = 'PAYMENT_TYPE2' and wf_id = payment_type2),null,payment_type2,(select wf_desc from ptr_sys_idtab where wf_type = 'PAYMENT_TYPE2' and wf_id = payment_type2)) as tt_payment_type , "
					+ " file_no , serial_no , data_from , '' as error_desc , "
					+ " to_char(mod_time,'yyyymmdd') as mod_date , to_char(mod_time,'hh24miss') as mod_time2 , mod_user "
					;
		
		wp.daoTable = "act_repay_creditlimit";
		wp.whereOrder = " order by pay_date Asc , pay_time Asc ";
		
		pageQuery();
		if (sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}

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
		int llOk = 0 , llErr = 0;
		
		ccam01.Ccap2130Func func = new ccam01.Ccap2130Func();
		func.setConn(wp);
		
		String[] aaOpt = wp.itemBuff("opt");
		String[] fineNo = wp.itemBuff("file_no");
		String[] serialNo = wp.itemBuff("serial_no");
		String[] dataFrom = wp.itemBuff("data_from");
		String[] payAmt = wp.itemBuff("pay_amt");
		String[] sign = wp.itemBuff("sign");
		String[] crtUser = wp.itemBuff("crt_user");
		String[] modUser = wp.itemBuff("mod_user");
	    wp.listCount[0] = wp.itemRows("file_no");
	    
	    optNumKeep(wp.listCount[0]);
	    if (optToIndex(aaOpt[0]) < 0) {
	      alertErr2("請選取欲覆核之資料");
	      return;
	    }
	    
	    int rr = -1;
	    for (int ii = 0; ii < aaOpt.length; ii++) {
	      rr = this.optToIndex(aaOpt[ii]);
	      if (rr < 0) {
	        continue;
	      }
	      
	      if(wp.loginUser.equals(crtUser[rr])) {
	    	  wp.colSet(rr,"ok_flag", "X");
	    	  wp.colSet(rr, "error_desc","建檔人員和覆核人員相同 , 不可覆核 !");
	    	  llErr++ ;
	    	  continue ;
	      }
	      
	      if(wp.loginUser.equals(modUser[rr])) {
	    	  wp.colSet(rr,"ok_flag", "X");
	    	  wp.colSet(rr, "error_desc","異動人員和覆核人員相同 , 不可覆核 !");
	    	  llErr++ ;
	    	  continue ;
	      }
	      
	      
	      func.varsSet("file_no", fineNo[rr]);
	      func.varsSet("serial_no", serialNo[rr]);
	      func.varsSet("data_from", dataFrom[rr]);
	      func.varsSet("pay_amt", payAmt[rr]);
	      func.varsSet("sign", sign[rr]);
	      optOkflag(rr);
	      int liRc = func.dataProc();
	      sqlCommit(liRc);
	      optOkflag(rr, liRc);
	      if (liRc == 1) {
	        llOk++;
	      } else {
	        llErr++;
	        wp.colSet(rr,"error_desc", func.errorDesc);
	      }
	    }
	    alertMsg("覆核完成; OK=" + llOk + ", ERR=" + llErr);
	    
	}
	
	void procFunc2() throws Exception {
		int llOk = 0 , llErr = 0;
		
		ccam01.Ccap2130Func func = new ccam01.Ccap2130Func();
		func.setConn(wp);
		
		String[] aaOpt = wp.itemBuff("opt");
		String[] fineNo = wp.itemBuff("file_no");
		String[] serialNo = wp.itemBuff("serial_no");
		String[] dataFrom = wp.itemBuff("data_from");
		String[] payAmt = wp.itemBuff("pay_amt");
		String[] sign = wp.itemBuff("sign");		
	    wp.listCount[0] = wp.itemRows("file_no");
	    
	    optNumKeep(wp.listCount[0]);
	    if (optToIndex(aaOpt[0]) < 0) {
	      alertErr2("請選取欲覆核之資料");
	      return;
	    }
	    
	    int rr = -1;
	    for (int ii = 0; ii < aaOpt.length; ii++) {
	      rr = this.optToIndex(aaOpt[ii]);
	      if (rr < 0) {
	        continue;
	      }
	      	     
	      func.varsSet("file_no", fineNo[rr]);
	      func.varsSet("serial_no", serialNo[rr]);
	      func.varsSet("data_from", dataFrom[rr]);
	      func.varsSet("pay_amt", payAmt[rr]);
	      func.varsSet("sign", sign[rr]);
	      optOkflag(rr);
	      int liRc = func.dataProc2();
	      sqlCommit(liRc);
	      optOkflag(rr, liRc);
	      if (liRc == 1) {
	        llOk++;
	      } else {
	        llErr++;
	        wp.colSet(rr,"error_desc", func.errorDesc);
	      }
	    }
	    alertMsg("解覆核完成; OK=" + llOk + ", ERR=" + llErr);
	}
	
	@Override
	public void initButton() {
		if(wp.itemEq("ex_apr_flag", "N")) {
			wp.colSet("btn_On1", "");
			wp.colSet("btn_On2", "disabled");
		}	else	if(wp.itemEq("ex_apr_flag", "Y")) {
			wp.colSet("btn_On1", "disabled");
			wp.colSet("btn_On2", "");
		}	else	{
			wp.colSet("btn_On1", "disabled");
			wp.colSet("btn_On2", "disabled");
		}		
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
