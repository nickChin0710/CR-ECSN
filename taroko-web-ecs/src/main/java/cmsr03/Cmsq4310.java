/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-02-01  V1.00.00  Machao       program initial     
* 112-07-12  V1.00.01  machao       卡號重覆调整*
******************************************************************************/
package cmsr03;

import ofcapp.BaseAction;

public class Cmsq4310 extends BaseAction {

	@Override
	public void userAction() throws Exception {
		 if (eqIgno(wp.buttonCode, "X")) {
		      /* 轉換顯示畫面 */
		      strAction = "new";
		      clearFunc();
		    } else if (eqIgno(wp.buttonCode, "Q")) {
		      /* 查詢功能 */
		      strAction = "Q";
		      queryFunc();
		    } else if (eqIgno(wp.buttonCode, "R")) {
		      // -資料讀取-
		      strAction = "R";
		      dataRead();
		       } else if (eqIgno(wp.buttonCode, "A")) {
		       /* 新增功能 */
		       saveFunc();
		       } else if (eqIgno(wp.buttonCode, "U")) {
		       /* 更新功能 */
		       saveFunc();
		       } else if (eqIgno(wp.buttonCode, "D")) {
		       /* 刪除功能 */
		       saveFunc();
		    } else if (eqIgno(wp.buttonCode, "M")) {
		      /* 瀏覽功能 :skip-page */
		      queryRead();
		    } else if (eqIgno(wp.buttonCode, "S")) {
		      /* 動態查詢 */
		      querySelect();
		    } else if (eqIgno(wp.buttonCode, "L")) {
		      /* 清畫面 */
		      strAction = "";
		      clearFunc();
		    } else if (eqIgno(wp.buttonCode, "C")) {
		      // -資料處理-
		      procFunc();
		    }

		    dddwSelect();
		    initButton();


		
	}

	@Override
	public void dddwSelect() {
		try {
	    	wp.initOption = "--";
	    	wp.optionKey = wp.colStr(0, "ex_acct_type");
	        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
//	    	------------------------------------------------------------------------------
	      wp.initOption = "--";
	      wp.optionKey = wp.colStr("ex_item_no");
	      dropdownList("dddw_item_no", "ptr_sys_idtab", "wf_id", "wf_id" + "||'.'||" + "wf_desc", 
	    		  "where wf_type = 'RIGHT_ITEM_NO' and wf_id  in ('08','09','10','11','12','13','14','15')");
	     
	    } catch (Exception ex) {
	    }
		
	}

	@Override
	public void queryFunc() throws Exception {
		// TODO Auto-generated method stub
		if (chkStrend(wp.itemStr("ex_acct_month_s"), wp.itemStr("ex_acct_month_e")) == false) {
		      alertErr2("查詢年月: 起迄錯誤");
		      return;
		    }
		if (wp.itemStr("id_no").length() < 8) {
		      alertErr2("身分證字號至少8碼");
		      return;
		    }
		String lsWhere = " where 1=1 " 
				+sqlCol(wp.itemStr("ex_acct_month_s"), "c.acct_month", " >= ");
//				+ sqlCol(wp.itemStr("id_no"), " a.id_no");
		
		if (!wp.itemEmpty("ex_acct_month_e")) {
	    	lsWhere += sqlCol(wp.itemStr("ex_acct_month_e"), "c.acct_month", " <= ");
	    }
		String idPSeqno = SelectCrdIdno(wp.itemStr("id_no"));
		if(!empty(idPSeqno)) {
			lsWhere += sqlCol(idPSeqno,"b.id_P_Seqno");
		}
		
		if(!wp.itemEmpty("ex_acct_type")) {
			lsWhere += sqlCol(wp.itemStr("ex_acct_type"), "b.acct_type");
		}
		if(!wp.itemEmpty("ex_item_no")) {
			lsWhere += sqlCol(wp.itemStr("ex_item_no"), "c.item_no");
		}
		if(!wp.itemEmpty("card_no")) {
			lsWhere += sqlCol(wp.itemStr("card_no"), "b.card_no");
		}
		if(wp.itemEq("curr_code", "0")) {
			lsWhere += " and b.CURRENT_CODE = '0' ";
		}
	    wp.whereStr = lsWhere;
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();

	    queryRead();
	}

	private String SelectCrdIdno(String idNo) {
		String sql = "select id_p_seqno, chi_name from crd_idno where id_no = ? ";
		sqlSelect(sql, new Object[] {idNo});
		wp.colSet("chi_name", sqlStr("chi_name"));
		return sqlStr("id_p_seqno");
	}

	@Override
	public void queryRead() throws Exception {
		    wp.pageControl();
		    wp.selectSQL = "distinct a.id_no, " + " a.chi_name , " + " b.card_no," + " b.acct_type," + " b.card_type,"
			        + " b.group_code," + " b.sup_flag," + " b.current_code," + " c.acct_month, " 
//		    		+  " c.curr_month_cnt," + " c.curr_month_amt,"
//			        + " d.last_year_cnt," + " d.last_year_consume," 
			        + " b.issue_date, "
			        + " d.rcv_annual_fee";
			    wp.daoTable = "crd_idno a left join crd_card b on a.id_p_seqno = b.id_p_seqno "
			    		+ " left join CMS_RIGHT_YEAR_DTL c on a.id_p_seqno = c.id_p_seqno "
			    		+ " left join CMS_RIGHT_YEAR d on a.id_p_seqno = d.id_p_seqno ";
//			    wp.whereStr = wp.whereStr;
			pageSelect();
			if (sqlRowNum <= 0) {
			      alertErr2("此條件查無資料");
			      return;
			    }
			for (int i = 0; i < wp.selectCnt; i++) {
				String acctType = wp.getValue("acct_type",i);
				String chinName  = SelectptrType(acctType);
				wp.colSet(i,"chin_name", chinName);
				//近N月累計消費
				String acctMonth = wp.getValue("acct_month",i);
				String cardNo = wp.getValue("card_no",i);
				String[] sumC = SelectCurrCnt(cardNo,acctMonth);
				wp.colSet(i,"sum_curr_month_cnt",sumC[0]);
				wp.colSet(i,"sum_curr_month_amt",sumC[1]);
				
				//近一年累計消費
				String[] sumL = SelectLastCnt(cardNo,acctMonth);
				wp.colSet(i,"last_year_cnt",sumL[0]);
				wp.colSet(i,"last_year_consume",sumL[1]);
				
		    	String supFlag = wp.getValue("sup_flag",i);
			 	if(supFlag.equals("0")) {
			 	    wp.colSet(i,"sup_flag", "正卡");
			 	}else if(supFlag.equals("1")) {
			 	   	wp.colSet(i, "sup_flag", "副卡");
			 	}
			 	
				String currentCode = wp.getValue("current_code",i);
			 	if(currentCode.equals("0")) {
			 	    wp.colSet(i,"current_code", "0.正常");
			 	}else {
			 	   	wp.colSet(i, "current_code", currentCode);
			 	}
			 	double rcvAnnualFee = wp.getNumber("rcv_annual_fee",i);
			 	if(rcvAnnualFee>0) {
			 		wp.colSet(i,"rcv_annual_fee", "Y");
			 	}else {
			 		wp.colSet(i,"rcv_annual_fee", "N");
			 	}
			 	
			 	 wp.colSet(i, "SER_NUM", String.format("%02d", i + 1));
			}
			    
			wp.setListCount(1);
		    wp.setPageValue();    
		    tab2Select();
		}

	  String SelectptrType(String acctType) {
		  String sql = "select chin_name from ptr_acct_type where acct_type = ? ";
			sqlSelect(sql, new Object[] {acctType});
			return sqlStr("chin_name");
	}

	String[] SelectLastCnt(String cardNo, String acctMonth) {
		 String sql = "select sum(last_year_cnt) as sum_last_year_cnt, sum(last_year_consume) as sum_last_year_consume"
			  		+ " from CMS_RIGHT_YEAR "
			  		+ " where card_no = ? and acct_year >= ? "
			  		+ " group by card_no ";
			  String acctYear = acctMonth.substring(0, 4);
			  int acctyear = Integer.parseInt(acctYear)-1;
			  sqlSelect(sql, new Object[] {cardNo,acctyear});
			  String[] sumL = {sqlStr("sum_last_year_cnt"),sqlStr("sum_last_year_consume")};
			return sumL;
	}

	String[] SelectCurrCnt(String cardNo,String acctmonth) {
		  String sql = "select sum(curr_month_cnt) as sum_curr_month_cnt, sum(curr_month_amt) as sum_curr_month_amt"
		  		+ " from CMS_RIGHT_YEAR_DTL "
		  		+ " where card_no = ? and acct_month >= ? and acct_month <= ? "
		  		+ " group by card_no ";
		  String acctmonth2 = acctmonth.substring(0, 4)+"01";
		  sqlSelect(sql, new Object[] {cardNo,acctmonth2,acctmonth});
		  String[] sumC = {sqlStr("sum_curr_month_cnt"),sqlStr("sum_curr_month_amt")};
		return sumC;
	}

	void tab2Select() throws Exception {
		// TODO Auto-generated method stub
		 wp.selectSQL = " distinct b.card_no," + " b.group_code," + " c.item_no,"
			        + " d.curr_proj_amt," + " c.curr_month_amt," + " d.free_cnt," + " d.gift_cnt," + " d.bonus_cnt,"
			        + " d.use_cnt," + " d.used_next_cnt," + " d.rm_carno, "
			        + " d.proj_code";
		    wp.daoTable = "crd_idno a left join crd_card b on a.id_p_seqno = b.id_p_seqno "
		    		+ " left join CMS_RIGHT_YEAR_DTL c on a.id_p_seqno = c.id_p_seqno "
		    		+ " left join CMS_RIGHT_YEAR d on a.id_p_seqno = d.id_p_seqno ";
		    wp.whereStr = " where 1=1 " 
		    		+ sqlCol(wp.itemStr("id_no"), " a.id_no")
		    		+sqlCol(wp.itemStr("ex_acct_month_s"), "c.acct_month", " >= ")
		    		+sqlCol(wp.itemStr("ex_acct_month_e"), "c.acct_month", " <= ")
		    		+sqlCol(wp.itemStr("ex_item_no"), " c.item_no")
		    		+sqlCol(wp.itemStr("ex_acct_type"), "b.acct_type")
		    		+sqlCol(wp.itemStr("card_no"), "b.card_no");
		    wp.whereOrder = " order by card_no ";
		    pageQuery();
		    if (sqlRowNum <= 0) {
		    	alertErr2("此條件查無資料");
			      return;
			    }
		    for (int i = 0; i < wp.selectCnt; i++) {
		    	
		    	String itemNo = wp.getValue("item_no",i);
		    	String wfDesc = SelectPtrIdtab(itemNo);
		    	wp.colSet(i,"wf_desc", wfDesc);
		    	
		    	double currProjAmt = wp.getNumber("curr_proj_amt",i);
		    	double currMonthAmt = wp.getNumber("curr_month_amt",i);
		    	double currAmt = currProjAmt - currMonthAmt;
		    	if(currAmt > 0) {
		    		wp.colSet(i,"curr_amt", currAmt);
		    	}else {
		    		wp.colSet(i,"curr_amt", "0");
		    	}
		    	
		    	double freeCnt = wp.getNumber("free_cnt",i);
		    	double giftCnt = wp.getNumber("gift_cnt",i);
		    	double bonusCnt = wp.getNumber("bonus_cnt",i);
		    	double useCnt = wp.getNumber("use_cnt", i);
		    	double usedNextCnt = wp.getNumber("used_next_cnt", i);
//		    	剩餘次數=免費總次數-已用次數+已預支次數
		    	double surplusCnt  = freeCnt + giftCnt + bonusCnt -useCnt + usedNextCnt;
		    	wp.colSet(i,"surplus_cnt", surplusCnt);
		    }
		    wp.setListCount(2);
		    wp.setPageValue();    
	}

		String SelectPtrIdtab(String itemNo) {
		String sql = "select wf_desc from ptr_sys_idtab where wf_id = ? ";
		sqlSelect(sql, new Object[] {itemNo});
		return sqlStr("wf_desc");
		
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
