/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-24  V1.00.01  ryan       program initial                            *
* 111-10-24  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package actm01;

import java.math.BigDecimal;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actm2160 extends BaseEdit {
	CommString commString = new CommString();
	String mAccttype ="";
  String mAcctkey ="";
  String mCardno ="";
  String pPSeqno = "";
	Actm2160Func func;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		// ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
		// wp.respCode + ",rHtml=" + wp.respHtml);
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
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
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
		} 

		dddwSelect();
		initButton();
	}
	void getWhereStr() {
		//wp.whereStr = " where 1=1 and act_acaj.adjust_type = 'AI01' "
		//		+ " and act_acaj.p_seqno = act_acno.acno_p_seqno ";

		wp.whereStr  = " where 1=1 and act_acno.acno_p_seqno = act_acct.p_seqno ";
		wp.whereStr += " and act_acct.adi_beg_bal > 0 ";
		if (empty(wp.itemStr("ex_ackey")) == false ) {
			wp.whereStr += " and act_acno.acct_key like :ex_ackey ";
			setString("ex_ackey", wp.itemStr("ex_ackey")+"%");
		}
		//if (empty(wp.item_ss("ex_curr_code")) == false ) {
		//	wp.whereStr += " and decode(act_acaj.curr_code,'','901', act_acaj.curr_code) = :ex_curr_code ";
		//	setString("ex_curr_code", wp.item_ss("ex_curr_code"));
		//}
	}
	
	@Override
	public void queryFunc() throws Exception {
		getWhereStr();
    if (wp.itemStr("ex_ackey").length() < 6) {  
    		alertErr("帳戶帳號至少要6碼");
		  	return;
    }
        
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}
	@Override
	public void initPage() {
		//wp.col_set("dc_orginal_amt","0");
		//wp.col_set("dc_bef_amt","0");
		//wp.col_set("dc_aft_amt","0");
		//wp.col_set("dc_bef_d_amt","0");
		//wp.col_set("dc_aft_d_amt","0");
		//wp.col_set("dc_dr_amt","0");
		//wp.col_set("dc_cr_amt","0");
	}
	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = 
            "  act_acct.p_seqno "
					+ " ,act_acct.acct_type "
					+ " ,act_acno.acct_key "
					+ " ,act_acct.acct_type||'-'||act_acno.acct_key wk_acctkey "
					+ " ,uf_acno_name(act_acct.p_seqno) db_cname "
					+ " ,'901' as curr_code "
					+ " ,'' as adjust_type "
					+ " ,adi_beg_bal as bef_amt "
					+ " ,adi_end_bal as end_amt "
					+ " ,adi_d_avail as bef_d_amt "
					+ " ,0 as dr_amt "
					+ " ,0 as cr_amt "
					+ " ,'' as crt_date "
					+ " ,'' as crt_time "
					+ " ,'act_acct' as tt_which_table "
					;
					
		wp.daoTable = " act_acct,act_acno ";
		wp.whereOrder = " order by act_acno.acct_key,act_acct.acct_type ";
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
        

	}

	void listWkdata() throws Exception {
		String ss = "";
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss  = wp.colStr(ii,"p_seqno");
			//check if act_acaj data exists ? if it exists, replace act_acaj into act_acct.
			if (wkChkActAcaj(ii,ss) == 0)  {
				wp.colSet(ii,"tt_which_table", "act_acaj");
			} else {
				wp.colSet(ii,"tt_which_table", "act_acct");
			}
			String sqlSelect = "select wf_id,wf_desc from ptr_sys_idtab where wf_type = 'DC_CURRENCY' and wf_id = :wf_id ";
      String s1 = "";
		  setString("wf_id",wp.colStr(ii,"curr_code"));
		  sqlSelect(sqlSelect);
		  if(sqlRowNum>0) {
		    s1 = sqlStr("wf_desc");
		  }
		  wp.colSet(ii,"tt_curr_code", s1);

		}
	}
    
  int wkChkActAcaj(int txIi, String txPSeqno)  throws Exception {
    	
   		String sql1 = " select "
	     			  + " decode(curr_code,'','901',curr_code) curr_code, "
   	 		 			+ " adjust_type, "
   	 		 			+ " dr_amt, "
   	 		 			+ " cr_amt, "
   	 			 		+ " crt_date, "
   	 			 		+ " crt_time "
   	 					+ " from act_acaj "
   	 					+ " where p_seqno = ? "
   	 					+ " and adjust_type = 'AI01' "
   	 					+ " and process_flag != 'Y' "
   				 		;
   	 
   		sqlSelect(sql1,new Object[]{txPSeqno});
   	 
   		if(sqlRowNum<=0) {
   			return -1;
   		}

   		wp.colSet(txIi,"curr_code", sqlStr("curr_code"));

    //wp.ddd("-->Actm2160-dsp01","");
    //wp.ddd("--:act_acaj.adjust_type[%s]",sql_ss("adjust_type"));
   		wp.colSet(txIi,"adjust_type", sqlStr("adjust_type"));
    //wp.ddd("-->Actm2160-dsp02","");
    //wp.ddd("--:act_acaj.adjust_type[%s]",wp.col_ss(tx_ii,"adjust_type"));
		
   		wp.colSet(txIi,"dr_amt", sqlStr("dr_amt"));
   		wp.colSet(txIi,"cr_amt", sqlStr("cr_amt"));
   		wp.colSet(txIi,"crt_date", sqlStr("crt_date"));
   		wp.colSet(txIi,"crt_time", sqlStr("crt_time"));
   	 
   		return 0;
    }

	@Override
	public void querySelect() throws Exception {
    	mAccttype = wp.itemStr("data_k1");
    	mAcctkey = wp.itemStr("data_k2");
    	wp.colSet("kk_acct_type", mAccttype);
     	wp.colSet("kk_acct_key", mAcctkey);
	  	dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		
		if(empty(mAccttype)){
  		if(empty(wp.itemStr("kk_acct_type"))) {
	  		wp.itemSet("kk_acct_type", wp.itemStr("kp_acct_type"));
	  		wp.colSet("kk_acct_type", wp.itemStr("kp_acct_type"));
		  } 				
		}	

		if(empty(mAcctkey)){
  		if(empty(wp.itemStr("kk_acct_key"))) {
	  		wp.itemSet("kk_acct_key", wp.itemStr("kp_acct_key"));
	  		wp.colSet("kk_acct_key", wp.itemStr("kp_acct_key"));
		  } 				
		}	

		if(empty(mCardno)){
  		if(empty(wp.itemStr("kk_card_no"))) {
	  		wp.itemSet("kk_card_no", wp.itemStr("kp_card_no"));
	  		wp.colSet("kk_card_no", wp.itemStr("kp_card_no"));
		  } 				
		}	

  	if (wfGetPseqno()<0) return;

		wp.selectSQL = 
            "  act_acct.p_seqno "
					+ " ,act_acct.acct_type "
					+ " ,act_acno.acct_key "
					+ " ,'901' as curr_code "
					+ " ,uf_acno_name(act_acct.p_seqno) db_cname "
					+ " ,adi_beg_bal as bef_amt "
					+ " ,adi_end_bal as end_amt "
					+ " ,adi_d_avail as bef_d_amt "
					+ " ,0 as dr_amt "
					+ " ,0 as cr_amt "
					+ " ,'' as crt_date "
					+ " ,'' as crt_time "
					;
					

		wp.daoTable =  " act_acct,act_acno ";
		wp.whereStr =  " where act_acct.p_seqno = act_acno.acno_p_seqno ";
		wp.whereStr += " and act_acct.p_seqno = :p_seqno " ;
		setString("p_seqno", pPSeqno);
		
		pageSelect();
		if (sqlNotFind()) {
			alertErr("帳戶資料 不存在");
			return;
		}
		else {
			double lmAftAmt = 0,lmAftDAmt=0; 
			this.selectOK();
			if (readActAcaj(pPSeqno) == 0)  {
			  lmAftAmt = doubleAdd(doubleSub(wp.colNum("end_amt") , wp.colNum("dr_amt")) , wp.colNum("cr_amt"));
			  lmAftDAmt = doubleAdd(doubleSub(wp.colNum("bef_d_amt") , wp.colNum("dr_amt")) , wp.colNum("cr_amt"));
      }
      else {
			  lmAftAmt = wp.colNum("end_amt");
			  lmAftDAmt = wp.colNum("bef_d_amt");
      }

			wp.colSet("aft_amt", lmAftAmt);
			wp.colSet("aft_d_amt",lmAftDAmt);
		}

		wfGetExDcount();
    String sqlSelect = "select wf_id,wf_desc from ptr_sys_idtab where wf_type = 'DC_CURRENCY' and wf_id = :wf_id ";
    String ss = "";
		setString("wf_id",wp.colStr("curr_code"));
		sqlSelect(sqlSelect);
		if(sqlRowNum>0) {
		  ss = sqlStr("wf_desc");
		}
		wp.colSet("tt_curr_code", ss);

		if(!empty(mCardno)) {
      wp.colSet("kk_acct_type", wp.colStr("acct_type") );
      wp.colSet("kk_acct_key", wp.colStr("acct_key") );
      mAccttype = wp.colStr("acct_type");
      mAcctkey = wp.colStr("acct_key");
		}
		
   //有異動權限者，讀出明細資料後，將鍵值存至 kp_ 並設定防止鍵值輸入屬性
    wp.colSet("kp_acct_type", mAccttype);
    wp.colSet("kp_acct_key", mAcctkey);
    wp.colSet("kp_card_no", mCardno);
    wp.colSet("kk_acct_type_attr", "disabled");
    wp.colSet("kk_acct_key_attr", "disabled");
    wp.colSet("kk_card_no_attr", "disabled");


   //以下防呆操作先點掉
   //有異動權限者，讀出明細資料後，將鍵值存至 kp_ 並設定防止鍵值輸入屬性
    /*** 
    if ( wp.col_num("bef_amt") > 0 && wp.aut_update() ) { 
      wp.col_set("kp_acct_type", m_accttype );
      wp.col_set("kp_acct_key", m_acctkey );
      wp.col_set("kp_card_no", m_cardno );
      wp.col_set("kk_acct_type_attr", "disabled");
      wp.col_set("kk_acct_key_attr", "disabled");
      wp.col_set("kk_card_no_attr", "disabled");
      btnOn_query(false); 
    }
    ***/ 
	}

  int wfGetPseqno() throws Exception {
		String lsSql = "";
		
		if (empty(mAccttype)) { mAccttype = wp.itemStr("kk_acct_type"); }
		if (empty(mAcctkey))  { mAcctkey = fillZeroAcctKey(wp.itemStr("kk_acct_key")); }
		if (empty(mCardno))   { mCardno = wp.itemStr("kk_card_no"); }
		
		if(empty(mAcctkey) && empty(mCardno)) {
			alertErr("帳號, 卡號不可均為空白");
			return -1;
		}
		
		if(!empty(mAcctkey) && !empty(mCardno)) {
			alertErr("帳號, 卡號不可同時輸入");
			return -1;
		}
		
		//以acct_type, acct_key 優先查詢
		if(empty(mAcctkey)==false) {
			if(empty(mAccttype)) {
				alertErr("請輸入帳號代碼");
				return -1;
			}
			lsSql  = " select p_seqno from act_acno ";
			lsSql += " where acct_type = :acct_type and acct_key = :acct_key ";
			lsSql += " and acno_p_seqno = p_seqno ";
			setString("acct_type", mAccttype);
			setString("acct_key", mAcctkey);
			mCardno = "";
		} 
		else {
			lsSql  = " select p_seqno from act_acno ";
			lsSql += " where p_seqno in (select p_seqno from crd_card where card_no = :card_no) ";
			lsSql += " and acno_p_seqno = p_seqno ";
			setString("card_no", mCardno);
		}
		
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			pPSeqno = sqlStr("p_seqno");
		} else {
			if(empty(mAcctkey)==false) {
				alertErr("此帳號不存在!");
			} else {
				alertErr("此卡號不存在!");
			}
			return -1;
		}
		return 0;
    }
    
  int readActAcaj(String txPSeqno) throws Exception {
    	
   		String sql1 = " select "
				  		+ " hex(rowid) as rowid, "
				  		+ " mod_seqno, "
	     			  + " decode(curr_code,'','901',curr_code) curr_code, "
   	 		 			+ " dr_amt, "
   	 		 			+ " cr_amt, "
   	 			 		+ " card_no, "
   	 			 		+ " apr_flag "
   	 					+ " from act_acaj "
   	 					+ " where p_seqno = ? "
   	 					+ " and adjust_type = 'AI01' "
   	 					+ " and process_flag != 'Y' "
   				 		;
   	 
   		sqlSelect(sql1,new Object[]{txPSeqno});
   	 
   		if(sqlRowNum<=0) {
   			return -1;
   		}
   	 
   		wp.colSet("rowid", sqlStr("rowid"));
   		wp.colSet("mod_seqno", sqlStr("mod_seqno"));
   		wp.colSet("curr_code", sqlStr("curr_code"));
   		wp.colSet("dr_amt", sqlStr("dr_amt"));
   		wp.colSet("cr_amt", sqlStr("cr_amt"));
   		wp.colSet("card_no", sqlStr("card_no"));
   		wp.colSet("apr_flag", sqlStr("apr_flag"));
   	 
   		return 0;
  }

	@Override
	public void saveFunc() throws Exception {

    if(empty(wp.itemStr("p_seqno")) ) {
			alertErr("請先查詢再輸入異動資料 !");
			return ;
		}
			
		func = new Actm2160Func(wp);
		
  //有異動權限者，檢核輸入明細資料前亦需先搬回鍵值並設定防止鍵值輸入屬性
    wp.colSet("kk_acct_type", wp.itemStr("kp_acct_type") ); 
    wp.colSet("kk_acct_key", wp.itemStr("kp_acct_key") );
    wp.colSet("kk_card_no", wp.itemStr("kp_card_no") );
    wp.itemSet("kk_acct_type", wp.itemStr("kp_acct_type") ); 
    wp.itemSet("kk_acct_key", wp.itemStr("kp_acct_key") );
    wp.itemSet("kk_card_no", wp.itemStr("kp_card_no") );
    wp.colSet("kk_acct_type_attr", "disabled");
    wp.colSet("kk_acct_key_attr", "disabled");
    wp.colSet("kk_card_no_attr", "disabled");

   //以下防呆操作先點掉
   //有異動權限者，讀出明細資料後，將鍵值存至 kp_ 並設定防止鍵值輸入屬性
    /***
    if ( wp.aut_update() ) { 
      wp.col_set("kk_acct_type", wp.item_ss("kp_acct_type") ); 
      wp.col_set("kk_acct_key", wp.item_ss("kp_acct_key") );
      wp.col_set("kk_card_no", wp.item_ss("kp_card_no") );
      wp.col_set("kk_acct_type_attr", "disabled");
      wp.col_set("kk_acct_key_attr", "disabled");
      wp.col_set("kk_card_no_attr", "disabled");
      btnOn_query(false); 此防呆操作先點掉
    }
    ***/

		if(ofValidation()!=1) {
			return;
		}
		
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr(func.getMsg());
		}
		//else{
	  //alert_msg("修改完成");
		//}
		this.sqlCommit(rc);
		
	}

	@Override
	public void initButton() {

    if (wp.respHtml.equals("actm2160"))  {
       this.btnModeAud();
    }

		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud(); //rowid 有值時，新增鍵 off(disabled)，修改鍵、刪除鍵 on
      /*** 此防呆操作先點掉
      if ( wp.col_num("bef_amt") == 0 )  {   //adi_beg_bal 無值時，表示不能新增 act_acaj，因此新增鍵 off(disabled)
           btnOn_add(false);
		  }
      ***/
		}
	}

	@Override
	public void dddwSelect() {

		try {
		//wp.optionKey = wp.item_ss("ex_curr_code");
		//this.dddw_list("dddw_ex_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id");
			if (wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = wp.colStr("kk_acct_type");
				this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1  order by acct_type");
			}else{
				wp.optionKey = wp.itemStr("acct_type");
				this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1  order by acct_type");
			}
		} catch (Exception ex) {
		}
	}

	int ofValidation() throws Exception {
		func = new Actm2160Func(wp);
		String lsDeptno="",lsGlcode="";
		if(wp.itemStr("apr_flag").equals("Y")){
			alertErr("此筆已放行不可再調整!!");
			return -1;
		}
		
		if (strAction.equals("A") || strAction.equals("U")) {
      /***
			if(!empty(wp.item_ss("kk_card_no"))){
				String sql_select ="select p_seqno from crd_card where card_no = :ls_card_no "; 
				setString("ls_card_no",wp.item_ss("kk_card_no"));
				sqlSelect(sql_select);
				if(sql_nrow<=0){
					alert_err("卡號不存在!!");
					return -1;	
				}
			}
			if (!empty(wp.item_ss("acct_key"))) {
				String sql_select2 = " select acno_p_seqno from act_acno "
						+ " where acct_type = :acct_type and acct_key = :acct_key ";
				setString("acct_type", wp.item_ss("acct_type"));
				setString("acct_key", wp.item_ss("acct_key"));
				sqlSelect(sql_select2);
				if (sql_nrow <= 0) {
					alert_err("帳戶帳號不存在!!");
					return -1;
				}else{
					func.vars_set("p_seqno", sql_ss("acno_p_seqno"));
				}
			}
      ***/

			if (wp.itemNum("dr_amt") > 0 && wp.itemNum("cr_amt") > 0) {
				alertErr("增加or減少帳外息金額 只能有一個不為 0 !");
				return -1;
			}
			
			if (wp.itemNum("dr_amt") == 0 && wp.itemNum("cr_amt") == 0) {
				alertErr("增加or減少帳外息金額 不可皆為 0 !");
				return -1;
			}
			
			double lmNum = doubleAdd(doubleSub(wp.itemNum("end_amt"),wp.itemNum("dr_amt")),wp.itemNum("cr_amt"));
			if(lmNum>wp.itemNum("bef_amt")){
				alertErr("帳外息金額調整後,不可使帳外息金額大於 帳外息期初金額 !");
				return -1;
			}
			func.varsSet("aft_amt", lmNum+"");
			lmNum = doubleSub(wp.itemNum("bef_d_amt"),doubleSub(wp.itemNum("dr_amt"),wp.itemNum("cr_amt")));
			if(lmNum<0){
				alertErr("帳外息可D數調整後,不可使帳外息可D數小於 0 !");
				return -1;
			}
			if(lmNum>wp.itemNum("bef_amt")){
				alertErr("帳外息可D數調整後,不可使帳外息可D數大於 帳外息期初金額 !");
				return -1;
			}
			//System.out.println("dc_aft_d_amt:"+lm_num);
			func.varsSet("aft_d_amt", lmNum+"");
			//--Set vouch code --
			String lsUser = wp.loginUser;
			String sqlSelect3="select a.usr_deptno"
							+ " , b.gl_code "
							+ " from ptr_dept_code b, sec_user a "
							+ " where  1=1 and b.dept_code = a.usr_deptno "
							+ " and	a.usr_id = :ls_user ";
			setString("ls_user",lsUser);
			sqlSelect(sqlSelect3);
			if(sqlRowNum<=0){
				alertErr("無法取得使用者部門代碼, 起帳部門代碼!");
				return -1;
			}else{
			//ls_deptno = sql_ss("usr_deptno");
			//ls_glcode = sql_ss("gl_code");
    	  lsDeptno = commString.mid(sqlStr("usr_deptno"), 0,2);
				lsGlcode = empty(sqlStr("gl_code")) ? "0" : "0" + sqlStr("gl_code").substring(0, 1);
			}
			func.varsSet("job_code", lsDeptno);
			//if(!empty(ls_glcode)){
			//	func.vars_set("vouch_job_code", "0"+ls_glcode.substring(0,1));
			//}
			func.varsSet("vouch_job_code", lsGlcode);
	
		}
		return 1;
	}
	
	void wfGetExDcount() throws Exception {
	
	/*	String sql_select = "select adi_beg_bal,"
						+ " adi_end_bal,"
						+ " adi_d_avail "
						+ " FROM act_acct "
						+ " where p_seqno =:is_p_seqno ";
		setString("is_p_seqno",wp.col_ss("p_seqno"));
		sqlSelect(sql_select);
		if(sql_nrow>0){
			wp.col_set("dc_orginal_amt",sql_ss("adi_beg_bal"));
			wp.col_set("dc_bef_amt",sql_ss("adi_end_bal"));
			wp.col_set("dc_bef_d_amt",sql_ss("adi_d_avail"));
		}*/
		String sqlSelect2="select count(*) li_cnt from act_acaj "
							+ " where  p_seqno = :is_p_seqno "
							+ " and  adjust_type = 'AI01' ";
		setString("is_p_seqno", pPSeqno);
		sqlSelect(sqlSelect2);
		String liCnt = sqlStr("li_cnt");
		if(this.toNum(sqlStr("li_cnt"))>0){
			wp.colSet("ex_dcount", liCnt);
		}else{
			wp.colSet("ex_dcount", "0");
		}
	}
	
	Double doubleSub(Double v1,Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.subtract(b2).doubleValue();

	}
	
	Double doubleAdd(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.add(b2).doubleValue();

	}

  String fillZeroAcctKey(String acctkey) throws Exception {
	  String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
    

}
