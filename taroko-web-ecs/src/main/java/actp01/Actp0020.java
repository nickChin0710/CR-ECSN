/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 107-08-24  V1.00.01  Alex       bug fixed                                  *
* 109-04-15  V1.00.02  Alex       add auth_query                             *
* 111-10-24  V1.00.03  Yang Bo    sync code from mega                        *
******************************************************************************/

package actp01;

import java.util.HashMap;
import java.util.Map;

import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actp0020 extends BaseEdit {
	CommString commString = new CommString();
	CommDate commDate = new CommDate();
	String pPSeqno = "";
	String isStmtCycle = "";
	String isMCode = "";
	String isTempdata = "";
	int ilListCnt1 =0 , ilListCnt2 =0 ;
//Map<String, String> modData = new HashMap<String, String>();
  HashMap<String,Double>  acagHash   = new  HashMap<String,Double>();
  HashMap<String,Double> acaghashO = new  HashMap<String,Double>();

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
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
			saveFunc();
//			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "I")) {
			/* 動態查詢 */
			forinitinfo();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		String lsUpdUser = "";
		String lsSql = "Select a.update_user, b.usr_cname "
				+ "from act_moddata_tmp a "
				+ "left join sec_user b on a.update_user=b.usr_id "
		  	+ "where a.act_modtype = '01' "
		  	+ "and a.mod_pgm = 'actm0050' "
				+ "order by a.update_user "
				+ "fetch first 1 row only ";
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			 lsUpdUser = sqlStr("update_user");
       wp.colSet("ex_update_user", sqlStr("update_user"));
       wp.colSet("q_user_cname", sqlStr("usr_cname"));
		} 

		if(!empty(lsUpdUser)){
      showRemainCount(lsUpdUser);
	  }			
	
	}

	@Override
	public void queryFunc() throws Exception {
		
		String acctType = "", acctKey = "", cardNo = "";		
		acctType = wp.itemStr("ex_acct_type");
		acctKey = fillZeroAcctKey(wp.itemStr("ex_acct_key"));
		cardNo = wp.itemStr("ex_card_no");

    String lsSql = "Select b.update_user "
		    		+ "from act_acno a, act_moddata_tmp b "
		    		+ "where a.acno_p_seqno=b.p_seqno "
		      	+ "and b.act_modtype = '01' "
		  	    + "and b.mod_pgm = 'actm0050' "
				    + "order by b.update_user, a.acct_key "
				    + "fetch first 1 row only ";
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
      clearFunc();
		  wp.colSet("ex_acct_type",acctType);
		  wp.colSet("ex_acct_key",acctKey);
		  wp.colSet("ex_card_no",cardNo);
		//err_alert("無待覆核資料!");
			alertMsg("無待覆核資料!");
			return;
		} else { 
		  wp.itemSet("ex_update_user",sqlStr("update_user"));
		  wp.colSet("ex_update_user",sqlStr("update_user"));
		} 

	  String lsUpdUser = wp.itemStr("ex_update_user");
		if(wp.itemEmpty("ex_acct_key") && wp.itemEmpty("ex_card_no")) {
		  if(!wp.itemEmpty("ex_update_user") ) {
	    	 lsUpdUser = wp.itemStr("ex_update_user");
    		 lsSql = "Select a.acct_type, a.acct_key "
		    		+ "from act_acno a, act_moddata_tmp b "
		    		+ "where a.acno_p_seqno=b.p_seqno "
		      	+ "and b.act_modtype = '01' "
		  	    + "and b.mod_pgm = 'actm0050' "
		  	    + "and b.update_user = :upd_user "
				    + "order by a.acct_key "
				    + "fetch first 1 row only ";
		    setString("upd_user", lsUpdUser);
		    sqlSelect(lsSql);
		    if (sqlRowNum > 0) {
			     lsUpdUser = sqlStr(0,"update_user");
      		 wp.colSet("ex_acct_type", sqlStr("acct_type"));
      		 wp.itemSet("ex_acct_type", sqlStr("acct_type"));
      		 wp.colSet("ex_acct_key", sqlStr("acct_key"));
      		 wp.itemSet("ex_acct_key", sqlStr("acct_key"));
       		 acctType = sqlStr("acct_type");
		       acctKey  = sqlStr("acct_key");
		    } 
		  }
		}
		
		if (empty(acctKey) && empty(cardNo)) {
			alertErr("帳戶帳號 和 卡號 不可皆為空白 !");
			return;
		}
		
		ColFunc func =new ColFunc();
		func.setConn(wp);		
		if(empty(acctKey)==false){
			if (func.fAuthQuery(wp.modPgm(), commString.mid(acctKey, 0,10))!=1) { 
		     	alertErr(func.getMsg()); 
		     	return ; 
		   }
		}	else if(empty(cardNo)==false){
			if (func.fAuthQuery(wp.modPgm(), cardNo)!=1) { 
	      	alertErr(func.getMsg()); 
	      	return ; 
	      }
		}
		
		// 設定queryRead() SQL條件
		//p_p_seqno = getInitParm();
		//p_p_seqno = wp.item_ss("q_p_seqno");
		//is_stmt_cycle = wp.item_ss("q_stmt_cycle");

		pPSeqno = "";
		isStmtCycle = "";
		forinitinfo();

		if(empty(pPSeqno)) {
      clearFunc();
		  wp.colSet("ex_acct_type",acctType);
		  wp.colSet("ex_acct_key",acctKey);
		  wp.colSet("ex_card_no",cardNo);
		  wp.colSet("ex_update_user",lsUpdUser);
			alertErr("無此帳號！");
			return;
		}

//		if (!p_p_seqno.equals("")) {
//			getDtlData(p_p_seqno);
//		}
		
		//is_m_code = getMcode(p_p_seqno, is_stmt_cycle);

		//wp.col_set("q_mcode_o", is_m_code);

		wp.setQueryMode();

		queryRead();
	}
	
	@Override
	public void queryRead() throws Exception {
		
    wp.pageRows = 9999;
		wp.colSet("q_mp", "0");
		wp.colSet("q_mp_o", "0");
		wp.colSet("q_mcode", "0");
		wp.colSet("q_mcode_o", "0");
		wp.colSet("update_user", "");
		wp.colSet("update_date", "");
		//--先讀 左邊 act_moddata_tmp
		wp.selectSQL = " hex(rowid) as rowid, acct_data, update_user, update_date, mod_user, ";
		wp.selectSQL += " substr(acct_data,4,6) as data_acct_month ";
		wp.daoTable = " act_moddata_tmp ";
		wp.whereStr = " where 1=1 and act_modtype ='01' and mod_pgm='actm0050' "
						+sqlCol(pPSeqno,"p_seqno")
						;
		wp.whereOrder = " order by data_acct_month ";
		
		pageQuery();
		if(sqlRowNum<=0){
		//select_OK();
			alertErr("此帳戶無待覆核資料!");
			return;
		}	else	{
			ilListCnt1 = wp.selectCnt;
			wp.setListCount(1);
			ProcMoveTmp();
		}
	
		wfAcaghashSet();
		wfGetMcode();
		
		//--再讀 右邊 act_acag_curr
		
		wp.selectSQL = " acct_month as acct_month_o, "
						 + " curr_code as curr_code_o, "
						 + " dc_pay_amt as dc_pay_amt_o, "
						 + " pay_amt as pay_amt_o ,"
						 + " pay_amt as db_org_amt_tw , "
						 + " dc_pay_amt as db_org_amt_dc "
						 ;
		wp.daoTable = " act_acag_curr ";
		wp.whereStr = " where 1=1 "
				      +sqlCol(pPSeqno,"p_seqno")
						;
		wp.whereOrder = " order by acct_month ";
		
		pageQuery();
		if(sqlRowNum<=0){
			selectOK();
		}	else	{
			ilListCnt2 = wp.selectCnt;
			wp.setListCount(2);
			queryAfter2();
		}
		
		wfAcaghashOSet();
		wfGetMcodeO();

  //ddd("wp.col_ss(update_user):" + wp.col_ss("update_user"));
		wp.colSet("ex_update_user", wp.colStr("update_user"));
		String lsUpdUser = wp.colStr("ex_update_user");
		String lsSql = "Select usr_cname "
				+ "from sec_user "
		  	+ "where usr_id = :upd_user ";
		setString("upd_user", lsUpdUser);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
       wp.colSet("q_user_cname", sqlStr("usr_cname"));
		} 

    showRemainCount(lsUpdUser);
		
	}
	
	void queryAfter2(){
		double liSumMpO = 0;
		for(int ii = 0; ii< ilListCnt2; ii++){
			liSumMpO += wp.colNum(ii,"pay_amt_o");
		}
		
		wp.colSet("q_mp_o", ""+liSumMpO);
	}
	
	void ProcMoveTmp() throws Exception{
		String[] tt = new String[2];
		double liSumMp = 0;
		
		String sql1 = " select "
						+ " pay_amt , "
						+ " dc_pay_amt "
						+ " from act_acag_curr "
						+ " where p_seqno = ? "
						+ " and acct_month = ? "
						+ " and curr_code = ? "
						;
		
		for(int ii=0;ii<wp.selectCnt;ii++){
			tt[0] = wp.colStr(ii,"acct_data");
			tt = commString.token(tt, "@");	// ser_num
			wp.colSet(ii,"seq_no", tt[1]);
			tt = commString.token(tt, "@"); // acct_month
			wp.colSet(ii,"acct_month",tt[1]);
			tt = commString.token(tt, "@"); // curr_code
			wp.colSet(ii,"curr_code",tt[1]);
			tt = commString.token(tt, "@"); // dc_pay_amt
			wp.colSet(ii, "dc_pay_amt",tt[1]);
			tt = commString.token(tt, "@"); // pay_amt
			wp.colSet(ii, "pay_amt",tt[1]);
			liSumMp += commString.strToNum(tt[1]);
			tt = commString.token(tt, "@"); // dc_pay_amt2
			wp.colSet(ii, "dc_pay_amt2",tt[1]);
			tt = commString.token(tt, "@"); // pay_amt2
			wp.colSet(ii, "pay_amt2",tt[1]);
			tt = commString.token(tt, "@"); // status_ua
			wp.colSet(ii, "status_ua",tt[1]);
			wp.colSet(ii, "appr_yn","Y");
			
			sqlSelect(sql1,new Object[]{wp.itemStr("q_p_seqno"),wp.colStr(ii,"acct_month"),wp.colStr(ii,"curr_code")});
			if(sqlRowNum>0){
				wp.colSet(ii,"pay_amt_s", sqlStr("pay_amt"));
				wp.colSet(ii,"dc_pay_amt_s", sqlStr("dc_pay_amt"));
			}
			
		}
//		wp.col_set("q_mcode", getMcode(wp.item_ss("q_p_seqno"),wp.item_ss("q_stmt_cycle")));
		wp.colSet("q_mp", ""+liSumMp);
		
	}
	
  void wfAcaghashSet() throws Exception {
   	//double ls_pay_amt = 0, tw_amt_sub = 0;
   	  double lsPayAmt = 0;
      String lsKey  = "";
   	
    	for (int ii = 0; ii < ilListCnt1; ii++) {
         lsKey  = wp.colStr(ii,"acct_month");
	    	 lsPayAmt = commString.strToNum(wp.colStr(ii,"pay_amt"));
         Double twAmtSub = (Double)acagHash.get(lsKey);
         if ( twAmtSub == null )
            { acagHash.put(lsKey,lsPayAmt); } 
         else
            { twAmtSub +=  lsPayAmt;
            	acagHash.put(lsKey,twAmtSub); } 
       }
  }

	void wfGetMcode() throws Exception {
	//String ls_cycl_ym = "" , ls_acct_ym="" , ldt_val1 ="" , ldt_val2 ="" , tt_mcode="" ;
		String lsCyclYm = "" , ldtVal1 ="" , ldtVal2 ="" , ttMcode="" ;
		double ldcMpamt = 0 , lmAmt = 0 ;
		int liMcode = 0 ;
		lsCyclYm = wp.colStr("q_this_acct_yymm");
		if(empty(lsCyclYm)){
			alertErr("未取得關帳週期年月");
			return ;
		}
		
		String sql1 = " select "
						+ " mix_mp_balance "
						+ " from ptr_actgeneral "
						+ " where 1=1 "
						+commSqlStr.rownum(1)
						;
		
		sqlSelect(sql1);
		
		if(sqlRowNum<=0){
			ldcMpamt = 0 ;
		}	else	{
			ldcMpamt = sqlNum("mix_mp_balance");
		}
		
		String lsMinAcctYymm = "" ;

    for ( Map.Entry m : acagHash.entrySet() )
    {
			String lsAcctYm = (String)m.getKey();
			if(lsAcctYm.compareTo(lsCyclYm)>=0)	continue;
        	
	 		lmAmt = (Double)acagHash.get(lsAcctYm);
			if(lmAmt <= ldcMpamt)	continue;
        	
			if(lmAmt > ldcMpamt) {
	  	  if(empty(lsMinAcctYymm)) {
	    	  lsMinAcctYymm = lsAcctYm;
	  		}	
	  		else if(commString.strToNum(lsAcctYm) < commString.strToNum(lsMinAcctYymm)) {
	    	  lsMinAcctYymm = lsAcctYm;
	  		}	
			}	
    }

		if(empty(lsMinAcctYymm))	liMcode=0;
		else	{
			ldtVal1 = lsCyclYm+"01";
			ldtVal2 = lsMinAcctYymm+"01";
			liMcode = commDate.monthsBetween(ldtVal1,ldtVal2);
		}
		
	//tt_mcode = String.format("%02d", li_mcode);
		ttMcode = String.format("%3d", liMcode);
		wp.colSet("q_mcode", ttMcode);
		
	}
	
  void wfAcaghashOSet() throws Exception {
   	//double ls_pay_amt = 0, tw_amt_sub = 0;
   	  double lsPayAmt = 0;
      String lsKey  = "";
   	
    	for (int ii = 0; ii < ilListCnt2; ii++) {
         lsKey  = wp.colStr(ii,"acct_month_o");
	    	 lsPayAmt = commString.strToNum(wp.colStr(ii,"pay_amt_o"));
         Double twAmtSub = (Double) acaghashO.get(lsKey);
         if ( twAmtSub == null )
            { acaghashO.put(lsKey,lsPayAmt); } 
         else
            { twAmtSub +=  lsPayAmt;
            	acaghashO.put(lsKey,twAmtSub); } 
       }
  }

	void wfGetMcodeO() throws Exception {
	//String ls_cycl_ym = "" , ls_acct_ym="" , ldt_val1 ="" , ldt_val2 ="" , tt_mcode="" ;
		String lsCyclYm = "" , ldtVal1 ="" , ldtVal2 ="" , ttMcode="" ;
		double ldcMpAmt = 0 , lmAmt = 0 ;
		int liMCode = 0 ;
		lsCyclYm = wp.colStr("q_this_acct_yymm");
		if(empty(lsCyclYm)){
			alertErr("未取得關帳週期年月");
			return ;
		}
		
		String sql1 = " select "
						+ " mix_mp_balance "
						+ " from ptr_actgeneral "
						+ " where 1=1 "
						+commSqlStr.rownum(1)
						;
		
		sqlSelect(sql1);
		
		if(sqlRowNum<=0){
			ldcMpAmt = 0 ;
		}	else	{
			ldcMpAmt = sqlNum("mix_mp_balance");
		}
		
		String lsMinAcctYymm = "" ;

    for ( Map.Entry m : acaghashO.entrySet() )
    {
			String lsAcctYm = (String)m.getKey();
			if(lsAcctYm.compareTo(lsCyclYm)>=0)	continue;
        	
	 		lmAmt = (Double) acaghashO.get(lsAcctYm);
			if(lmAmt <= ldcMpAmt)	continue;
        	
			if(lmAmt > ldcMpAmt) {
	  	  if(empty(lsMinAcctYymm)) {
	    	  lsMinAcctYymm = lsAcctYm;
	  		}	
	  		else if(commString.strToNum(lsAcctYm) < commString.strToNum(lsMinAcctYymm)) {
	    	  lsMinAcctYymm = lsAcctYm;
	  		}	
			}	
    }

		if(empty(lsMinAcctYymm))	liMCode=0;
		else	{
			ldtVal1 = lsCyclYm+"01";
			ldtVal2 = lsMinAcctYymm+"01";
			liMCode = commDate.monthsBetween(ldtVal1,ldtVal2);
		}
		
	//tt_mcode = String.format("%02d", li_mcode);
		ttMcode = String.format("%3d", liMCode);
		wp.colSet("q_mcode_o", ttMcode);
		
	}
	
	
	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		// ProcModDataTmp();
	}
	
	public int wfValidation(){

		if(wp.loginUser.equals(wp.itemStr("update_user")) ) {
		//errmsg("覆核人員及經辦不能為同一人！");
  	//alert_msg("覆核人員及經辦不能為同一人！",true);
  	//alert_msg("覆核人員及經辦不能為同一人！");
			alertErr("覆核人員及經辦不能為同一人！");
			return -1;				
		}
			

		//--list1
		String[] lsAcctMonth = wp.itemBuff("acct_month");
		String[] lsCurrCode = wp.itemBuff("curr_code");
		String[] lsStatusUa = wp.itemBuff("status_ua");
		String[] lsDcPayAmt2 = wp.itemBuff("dc_pay_amt2");
		String[] lsPayAmtS = wp.itemBuff("pay_amt_s");
		String[] lsDcPayAmtS = wp.itemBuff("dc_pay_amt_s");	
		wp.listCount[0] = wp.itemRows("acct_month");
		//--list2
		String[] lsAcctMonthO = wp.itemBuff("acct_month_o");
		String[] lsCurrCodeO = wp.itemBuff("curr_code_o");
		String[] lsDcPayAmtO = wp.itemBuff("dc_pay_amt_o");
		String[] lsDbOrgAmtDc = wp.itemBuff("db_org_amt_dc");
		wp.listCount[1] = wp.itemRows("acct_month_o");
		
		int lsFind = 0;
		double lsDouble1 = 0, lsDouble2 = 0;
		
		for(int ii=0;ii<wp.itemRows("acct_month");ii++){
			if(!eqIgno(lsStatusUa[ii],"U"))	continue;
			lsFind = 0;
			
			for(int ll=0;ll<wp.itemRows("acct_month_o");ll++){
				if(!eqIgno(lsAcctMonth[ii],lsAcctMonthO[ll]) ||!eqIgno(lsCurrCode[ii],lsCurrCodeO[ll])){
					continue;
				}				
				lsFind ++;
				
        /***
				if(!eq_igno(ls_dc_pay_amt_s[ii],ls_db_org_amt_dc[ll])){
					ddd(ii+":A:"+ls_dc_pay_amt_s[ii]);
					ddd(ll+":B:"+ls_db_org_amt_dc[ll]);
					errmsg("卡戶之MP已變更, 請重新調整");
					return -1;
				}
        ***/
        lsDouble1 = commString.strToNum(lsDcPayAmt2[ii]);
        lsDouble2 = commString.strToNum(lsDcPayAmtO[ll]);
				if(lsDouble1 != lsDouble2){
					log(ii+":A:"+lsDouble1);
					log(ll+":B:"+lsDouble2);
					errmsg("卡戶之MP已變更, 請重新調整");
					return -1;
				}
				
			}
			
			if(lsFind==0){
				errmsg("卡戶之MP已變更, 請重新調整;");
				return -1;				
			}
			
		}
		
		return 1;
	}
	
	@Override
	public void saveFunc() throws Exception {
		if (empty(wp.itemStr("q_p_seqno"))) {
		   alertErr("請先查詢 再執行覆核!");
			 return;
		}

		String[] lsRowid = wp.itemBuff("rowid");
		String[] lsAcctMonth = wp.itemBuff("acct_month");
		String[] lsCurrCode = wp.itemBuff("curr_code");
		String[] liDcPayAmt = wp.itemBuff("dc_pay_amt");
		String[] liPayAmt = wp.itemBuff("pay_amt");
		String[] liDcPayAmt2 = wp.itemBuff("dc_pay_amt2");
		String[] liPayAmt2 = wp.itemBuff("pay_amt2");
		String[] lsApprYn = wp.itemBuff("appr_yn");
		String[] lsStatusUa = wp.itemBuff("status_ua");
		String[] liSeqNo = wp.itemBuff("seq_no");
		String[] lModUser = wp.itemBuff("mod_user");
		
		double lmAmt = 0 , lmDcAmt = 0 , liSeqno=0 , imTotMp=0 ;
		int ii=-1;
		String lsStmtCycle = "" , lsK1 = "" , lsCurr = "" , lsAcctYm="" ,lsModUser="" ;
		lsStmtCycle = wp.itemStr("q_stmt_cycle");
		String[] aaKk = new String[300];
		int [] mmKk = new int[300];
		
		wp.listCount[0] = wp.itemRows("rowid");
		wp.listCount[1] = wp.itemRows("acct_month_o");
		
		if(wfValidation()!=1)	return ;
		
		Actp0020Func func = new Actp0020Func(wp);		
		
		//--wf_upd_act_acag_curr
		func.varsSet("p_seqno", wp.itemStr("q_p_seqno"));
		
		if(func.deleteAcagCurr()!=1){
			errmsg("delete ACT_ACAG_CURR error");
			return ;
		}
		
		for(int ll=0;ll<wp.itemRows("rowid");ll++){
			lmAmt = commString.strToNum(liPayAmt[ll]);
			lmDcAmt = commString.strToNum(liDcPayAmt[ll]);
			
			if(lmAmt==0 && lmDcAmt==0)	continue;
			lsCurr = lsCurrCode[ll];
			liSeqno = commString.strToNum(liSeqNo[ll]);
			lsAcctYm = lsAcctMonth[ll];
			lsModUser = lModUser[ll];
			if(!eqIgno(lsK1,lsAcctYm)){
				ii++;
				aaKk[ii] = lsAcctYm;
				lsK1 = lsAcctYm;
			}
			mmKk[ii] += lmAmt;
			imTotMp +=lmAmt;
			
			func.varsSet("p_seqno", wp.itemStr("q_p_seqno"));   
			func.varsSet("curr_code", lsCurrCode[ll]);
			func.varsSet("acct_type", wp.itemStr("ex_acct_type"));
			func.varsSet("seq_no", liSeqNo[ll]);
			func.varsSet("acct_month", lsAcctYm);
			func.varsSet("stmt_cycle", lsStmtCycle);
			func.varsSet("pay_amt", ""+lmAmt);
			func.varsSet("dc_pay_amt", ""+lmDcAmt);
			func.varsSet("mod_user", lModUser[ll]);
			
			if(func.insertAcagCurr()!=1){
				errmsg(func.getMsg());
				dbRollback();
				return;
			}			
		}
		
		if(func.deleteAcag()!=1){
			errmsg(func.getMsg());
			dbRollback();
			return ;
		}		
		int liAaKk = 0;
		liAaKk = aaKk.length-1;		
		for(ii=0;ii<liAaKk;ii++){		
			if(empty(aaKk[ii])||aaKk[ii]==null)	continue;
			
			lmAmt = mmKk[ii];
			func.varsSet("seq_no", ""+ii);
			func.varsSet("pay_amt", ""+mmKk[ii]);
			func.varsSet("acct_month", aaKk[ii]);
			if(func.insertAcag()!=1){
				errmsg(func.getMsg());
				dbRollback();
				return;
			}			
		}
		
		//--wf_add_act_acct_mrk
		
		String sql1 = " select "
						+ " corp_p_seqno , uf_corp_no(corp_p_seqno) as corp_no , "
						+ " id_p_seqno , uf_idno_id(id_p_seqno) as id_no , "
						+ " acct_type , acct_key , id_p_seqno "						
						+ " from act_acno "
						+ " where acno_p_seqno = ? "
						;
		
		sqlSelect(sql1,new Object[]{wp.itemStr("q_p_seqno")});
		
		func.varsSet("mod_audcode", "D");
		func.varsSet("acct_type", sqlStr("acct_type"));
		func.varsSet("acct_key", sqlStr("acct_key"));
		func.varsSet("acct_p_seqno", wp.itemStr("q_p_seqno"));
		func.varsSet("corp_p_seqno", sqlStr("corp_p_seqno"));
		func.varsSet("corp_no", sqlStr("corp_no"));
	//func.varsSet("acct_holder_id", sqlStr("id_no"));
		func.varsSet("id_p_seqno", sqlStr("id_p_seqno"));
		func.varsSet("min_pay_bal",wp.itemStr("q_mp_o"));
	//func.varsSet("m_code", wp.itemStr("q_mcode_o"));
		double lbMcode = commString.strToNum(wp.itemStr("q_mcode_o"));
		if(lbMcode > 99){
			func.varsSet("m_code", "99");
		} else {
			func.varsSet("m_code", wp.itemStr("q_mcode_o"));
		}
		//-before image-		
		if(func.insertMRK()!=1){
			errmsg(func.getMsg());
			dbRollback();
			return;
		}
		//-after image-
		func.varsSet("mod_audcode", "A");
		func.varsSet("min_pay_bal",""+imTotMp);
	//func.vars_set("m_code", wp.item_ss("q_mcode"));
		lbMcode = commString.strToNum(wp.itemStr("q_mcode"));
		if(lbMcode > 99){
			func.varsSet("m_code", "99");
		} else {
			func.varsSet("m_code", wp.itemStr("q_mcode"));
		}
		if(func.insertMRK()!=1){
			errmsg(func.getMsg());
			dbRollback();
			return;
		}
		
		
		//--wf_upd_act_acct 
		func.varsSet("min_pay_bal", ""+imTotMp);
		if(func.updateActAcct()!=1){
			errmsg(func.getMsg());
			dbRollback();
			return;
		}
		
		String sql2 = " select "
						+ " curr_code "
						+ " from act_acct_curr "
						+ " where p_seqno = ? "
						+ " order by curr_code "
						;
		
		String sql3 = " select "
						+ " sum(nvl(pay_amt,0)) as tl_pay_amt , "
						+ " sum(nvl(dc_pay_amt,0)) as tl_dc_pay_amt "
						+ " from act_acag_curr "
						+ " where p_seqno = ? "
						+ " and curr_code = ? "
						;
		
		sqlSelect(sql2,new Object[]{wp.itemStr("q_p_seqno")});
		int ilRowsCnt = 0;
		ilRowsCnt = sqlRowNum;
		int zz=-0;
		while(zz<ilRowsCnt){
			if(ilRowsCnt==0)	break;
			
			lsCurr = sqlStr(zz,"curr_code");
			lmAmt = 0 ; 
			lmDcAmt = 0 ;
			
			sqlSelect(sql3,new Object[]{wp.itemStr("q_p_seqno"),lsCurr});
			
			if(sqlRowNum<0){
				errmsg("select act_acag_curr.sum() error");
				dbRollback();
				break;
			}	
			
			lmAmt = sqlNum("tl_pay_amt");
			lmDcAmt = sqlNum("tl_dc_pay_amt");
			
			func.varsSet("min_pay_bal", ""+lmAmt);
			func.varsSet("dc_min_pay_bal", ""+lmDcAmt);
			func.varsSet("curr_code", lsCurr);
			if(func.updateActAcctCurr()!=1){
				errmsg(func.getMsg());
				dbRollback();
				return;
			}		
			zz++;
		}
		
		//--deleteTmp
		if(func.deleteTmp()!=1){
			errmsg(func.getMsg());
			dbRollback();
			return ;
		}
		
		sqlCommit(1);
    String lsSql1 = "";
		String lsUpdUser = wp.itemStr("ex_update_user");
		String lsUserCname = wp.itemStr("q_user_cname");
    showRemainCount(lsUpdUser);
		String lsUpdRemain = wp.colStr("q_update_remain");
  //wp.col_set("q_p_seqno", "");
    clearFunc();
		wp.listCount[0] = 0;
		wp.listCount[1] = 0;

		if (!lsUpdRemain.equals("0") ) {
      showRemainCount(lsUpdUser);
		  wp.colSet("ex_update_user",lsUpdUser);
		  wp.colSet("q_user_cname",lsUserCname);
		  wp.colSet("q_update_remain",lsUpdRemain);
		} else {
		  lsSql1 = "Select a.update_user, b.usr_cname "
				+ "from act_moddata_tmp a "
				+ "left join sec_user b on a.update_user=b.usr_id "
		  	+ "where a.act_modtype = '01' "
		  	+ "and a.mod_pgm = 'actm0050' "
				+ "order by a.update_user "
				+ "fetch first 1 row only ";
		  sqlSelect(lsSql1);
		  if (sqlRowNum > 0) {
		    wp.colSet("ex_update_user",sqlStr("update_user"));
		    lsUpdUser = sqlStr("update_user");
		    wp.colSet("q_user_cname",sqlStr("usr_cname"));
        showRemainCount(lsUpdUser);
		  } else {
		    alertMsg("覆核成功，已無待覆核資料!");
 			  return ;
		  }
		}

		alertMsg("覆核成功!");
		
	}

	void showRemainCount(String txUpdUser) {
		String lsCnt = "0";

		String lsSql = "Select count(distinct p_seqno) as cnt "
				+ "from act_moddata_tmp "
				+ "where 1=1 "
		  	+ "and act_modtype = '01' "
		  	+ "and mod_pgm = 'actm0050' "
				+ "and update_user =:upd_user ";
		setString("upd_user", txUpdUser);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			wp.colSet("q_update_remain", sqlStr("cnt"));
		} 

  }

	@Override
	public void initButton() {
	
	//if(wp.col_empty(0,"rowid")){
	//	this.btnMode_aud();
	//}			
	}

	@Override
	public void dddwSelect() {
		try {
//			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_acct_type");
			this.dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.optionKey = wp.colStr("ex_update_user");						
			dddwList("dddw_update_user",
		  			"select distinct update_user as db_code, update_user as db_desc  from act_moddata_tmp where 1=1 "
					 +"and act_modtype = '01' and mod_pgm = 'actm0050' order by 1 ");

		} catch (Exception ex) {
		}
	}

	public int updUserChanged(TarokoCommon wr) throws Exception {
		super.wp = wr;

		String lsCnt = "0", lsUserCname="";
		String lsUpdUser = wp.itemStr("upd_user");

		String lsSql = "Select count(distinct p_seqno) as cnt "
				+ "from act_moddata_tmp "
				+ "where 1=1 "
		  	+ "and act_modtype = '01' "
		  	+ "and mod_pgm = 'actm0050' "
				+ "and update_user =:upd_user ";
		setString("upd_user", lsUpdUser);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsCnt = sqlStr("cnt");
		} 

	  lsSql = "Select usr_cname "
				+ "from sec_user "
		  	+ "where usr_id = :upd_user ";
		setString("upd_user", lsUpdUser);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			lsUserCname = sqlStr("usr_cname");
		} 

		wp.addJSON("q_user_cname", lsUserCname);
		wp.addJSON("q_upd_remain", lsCnt);

		return 1;
	}

	public void forinitinfo() throws Exception {

		String lsSql = "";
		String idPSeqno = "";
		String corpPSeqno = "";
		String pSeqno = "";
		String accttyp = "", acctkey = "", cardno = "";
		accttyp = wp.itemStr("ex_acct_type");
		acctkey = fillZeroAcctKey(wp.itemStr("ex_acct_key"));
		cardno = wp.itemStr("ex_card_no");
									
		if (empty(acctkey) && empty(cardno)) {
			return;
		}
		
		ColFunc func =new ColFunc();
		func.setConn(wp);		
		if(empty(acctkey)==false){
			if (func.fAuthQuery(wp.modPgm(), commString.mid(acctkey, 0,10))!=1) { 
		     	alertErr(func.getMsg()); 
		     	return ; 
		   }
		}	else if(empty(cardno)==false){
			if (func.fAuthQuery(wp.modPgm(), cardno)!=1) { 
	      	alertErr(func.getMsg()); 
	      	return ; 
	      }
		}
		
		if(!(empty(acctkey) && empty(accttyp))){
			lsSql = "";
			lsSql += " select a.p_seqno, b.ACCT_KEY, b.ACCT_TYPE, b.corp_p_seqno, b.id_p_seqno " + 
					" from	 crd_card a, act_acno b " + 
					" where a.P_SEQNO = b.ACNO_P_SEQNO " +
					" and b.acct_type = :accttype " +
					" and b.acct_key = :acctkey ";
			setString("accttype", accttyp);
			setString("acctkey", acctkey);
			sqlSelect(lsSql);

			if(sqlRowNum > 0) {
				pSeqno = sqlStr("p_seqno");
				idPSeqno = sqlStr("id_p_seqno");
				corpPSeqno = sqlStr("corp_p_seqno");
				wp.colSet("q_p_seqno", pSeqno);
				
				if(!empty(idPSeqno)) {
					lsSql = "select chi_name from crd_idno where id_p_seqno = :id_p_seqno";
					setString("id_p_seqno", idPSeqno);
					sqlSelect(lsSql);
					if(sqlRowNum > 0) {
						wp.colSet("q_id_cname", sqlStr("chi_name"));
					}
				}
				

				if(!empty(corpPSeqno)) {
					lsSql = "select chi_name from crd_corp where corp_p_seqno = :corp_p_seqno";
					setString("corp_p_seqno", corpPSeqno);
					sqlSelect(lsSql);
					if(sqlRowNum > 0) {
						wp.colSet("q_corp_cname", sqlStr("chi_name"));
					}
				}
				pPSeqno = pSeqno;
				getDtlData(pSeqno);

			}
		}else if(!empty(cardno)) {
			lsSql = "";
			lsSql += " select a.p_seqno, b.ACCT_KEY, b.ACCT_TYPE, b.corp_p_seqno, b.id_p_seqno " + 
					" from	 crd_card a, act_acno b " + 
					" where a.P_SEQNO = b.ACNO_P_SEQNO " +
					" and a.CARD_NO = :cardno ";
			setString("cardno", cardno);
			sqlSelect(lsSql);
			if(sqlRowNum > 0) {
				pSeqno = sqlStr("p_seqno");
				wp.colSet("q_p_seqno", pSeqno);
				corpPSeqno = sqlStr("corp_p_seqno");
				idPSeqno = sqlStr("id_p_seqno");
				
				if(!empty(idPSeqno)) {
					lsSql = "select chi_name from crd_idno where id_p_seqno = :id_p_seqno";
					setString("id_p_seqno", idPSeqno);
					sqlSelect(lsSql);
					if(sqlRowNum > 0) {
						wp.colSet("q_id_cname", sqlStr("chi_name"));
					}
				}
				

				if(!empty(corpPSeqno)) {
					lsSql = "select chi_name from crd_corp where corp_p_seqno = :corp_p_seqno";
					setString("corp_p_seqno", corpPSeqno);
					sqlSelect(lsSql);
					if(sqlRowNum > 0) {
						wp.colSet("q_corp_cname", sqlStr("chi_name"));
					}
				}
				pPSeqno = pSeqno;
				getDtlData(pSeqno);
			}

		}

	}

	private void getDtlData(String pSeqno) throws Exception {
		String sYyymm = "";
		Object[] param = null;
		String lsSql = "";
		lsSql += " SELECT a.acct_status,   a.stmt_cycle, b.this_acct_month " + " FROM act_acno a, ptr_workday b "
				+ " WHERE b.stmt_cycle = a.stmt_cycle " + " and a.acno_p_seqno = ? " + "";
		param = new Object[] { pSeqno };
		sqlSelect(lsSql, param);

		if (empty(sqlStr("acct_status"))) {
			wp.colSet("q_acct_status", "");
			wp.colSet("q_stmt_cycle", "");
			wp.colSet("q_this_acct_month", "");
			wp.colSet("q_p_seqno", pSeqno);
		} else {
			String sStatus = "";
			if (Integer.parseInt(sqlStr("acct_status")) == 1)
				sStatus = "1-正常";
			if (Integer.parseInt(sqlStr("acct_status")) == 2)
				sStatus = "2-逾放";
			if (Integer.parseInt(sqlStr("acct_status")) == 3)
				sStatus = "3-催收";
			if (Integer.parseInt(sqlStr("acct_status")) == 4)
				sStatus = "4-呆帳";
			wp.colSet("q_acct_status", sStatus);
			wp.colSet("q_stmt_cycle", sqlStr("stmt_cycle"));
			sYyymm = sqlStr("this_acct_month");
			wp.colSet("q_this_acct_yymm", sYyymm);
      /***
			if(!s_yyymm.trim().equals("")) {
				s_yyymm = String.valueOf(Integer.parseInt(s_yyymm) - 191100);
				s_yyymm = s_yyymm.substring(0, 3) + "/" + s_yyymm.substring(3);
			}
      ***/
			if(!sYyymm.trim().equals("")) {
				sYyymm = sYyymm.substring(0, 4) + "/" + sYyymm.substring(4);
			}
			wp.colSet("q_this_acct_month", sYyymm);
			wp.colSet("q_p_seqno", pSeqno);
			isStmtCycle = sqlStr("stmt_cycle");
		}

	}

//	private String getMcode(String p_seqno, String stmt_cycle) {
//		String ls_sql = "";
//		float lf_mp = 0;
//		float lf_pay = 0;
//		String ls_acc_ym = "";
//		String ls_cyc_ym = "";
//		int diffMonth = 0;
//		
//		ls_sql = "select this_acct_month from PTR_WORKDAY where STMT_CYCLE = :stmt_cycle";
//		setString("stmt_cycle", stmt_cycle);
//		
//		sqlSelect(ls_sql);
//		if (sql_nrow > 0) {
//			ls_cyc_ym = sql_ss("this_acct_month");
//		}
//
//		try {
//		//get mp
//		//select mp from (SELECT ptr_actgeneral.mix_mp_balance as mp, row_number() over() rownum FROM ptr_actgeneral) a WHERE a.rownum < 2
//		ls_sql =" select mp from (SELECT ptr_actgeneral.mix_mp_balance as mp, row_number() over() rownum FROM ptr_actgeneral) a WHERE a.rownum < 2";
//		
//		sqlSelect(ls_sql);
//		if (sql_nrow > 0) {
//			lf_mp = Float.parseFloat(sql_ss("mp"));
//		}
//		}catch(Exception ex) {lf_mp = 0;}
//		
//		//get acag_curr
//		//select acct_month, pay_amt from act_acag_curr where P_SEQNO = '0001781236' order by acct_month
//		ls_sql = "select acct_month, pay_amt from act_acag_curr where P_SEQNO = :p_seqno order by acct_month";
//		setString("p_seqno", p_seqno);
//		
//		sqlSelect(ls_sql);
//		if (sql_nrow > 0) {
//			for(int i=0; i<sql_nrow; i++) {
//				ls_acc_ym = sql_ss(i,"acct_month");	
//				lf_pay = Float.parseFloat(sql_ss(i,"pay_amt"));
//				if(Integer.parseInt(ls_acc_ym) > Integer.parseInt(ls_cyc_ym)) break;
//				if(lf_pay > 0) {
//					lf_mp = lf_mp - lf_pay;
//					if(lf_mp < 0) break;
//				}else {
//					continue;
//				}
//			}
//			
//			if(lf_mp>0) return "0";
//			
//			diffMonth = (Integer.parseInt(ls_cyc_ym.substring(0, 4)) - Integer.parseInt(ls_acc_ym.substring(0, 4))) * 12;
//			diffMonth = diffMonth + (Integer.parseInt(ls_cyc_ym.substring(4, 6)) - Integer.parseInt(ls_acc_ym.substring(4, 6)));
//
//		}else {
//			return "";
//		}
//		
//		
//		
//		return String.valueOf(diffMonth);
//	}
//	
//	private String getMcode2(String p_seqno, String stmt_cycle, Map<String, String> cMap) {
//		String ls_sql = "";
//		float lf_mp = 0;
//		float lf_pay = 0;
//		String ls_acc_ym = "";
//		String ls_cyc_ym = "";
//		int diffMonth = 0;
//		
//		ls_sql = "select this_acct_month from PTR_WORKDAY where STMT_CYCLE = :stmt_cycle";
//		setString("stmt_cycle", stmt_cycle);
//		
//		sqlSelect(ls_sql);
//		if (sql_nrow > 0) {
//			ls_cyc_ym = sql_ss("this_acct_month");
//		}
//
//		try {
//		//get mp
//		//select mp from (SELECT ptr_actgeneral.mix_mp_balance as mp, row_number() over() rownum FROM ptr_actgeneral) a WHERE a.rownum < 2
//		ls_sql =" select mp from (SELECT ptr_actgeneral.mix_mp_balance as mp, row_number() over() rownum FROM ptr_actgeneral) a WHERE a.rownum < 2";
//		
//		sqlSelect(ls_sql);
//		if (sql_nrow > 0) {
//			lf_mp = Float.parseFloat(sql_ss("mp"));
//		}
//		}catch(Exception ex) {lf_mp = 0;}
//		
//		//get acag_curr
//		//select acct_month, pay_amt from act_acag_curr where P_SEQNO = '0001781236' order by acct_month
//		ls_sql = "select acct_month, pay_amt from act_acag_curr where P_SEQNO = :p_seqno order by acct_month";
//		setString("p_seqno", p_seqno);
//		
//		sqlSelect(ls_sql);
//		if (sql_nrow > 0) {
//			for(int i=0; i<sql_nrow; i++) {
//				ls_acc_ym = sql_ss(i,"acct_month");	
//				lf_pay = Float.parseFloat(sql_ss(i,"pay_amt"));
//				for(String s : cMap.keySet()) {
//					if(s.equals(ls_acc_ym)) {
//						lf_pay = Float.parseFloat(cMap.get(s));
//					}
//				}
//				
//				if(Integer.parseInt(ls_acc_ym) > Integer.parseInt(ls_cyc_ym)) break;
//				if(lf_pay > 0) {
//					lf_mp = lf_mp - lf_pay;
//					if(lf_mp < 0) break;
//				}else {
//					continue;
//				}
//			}
//			
//			if(lf_mp>0) return "0";
//			
//			diffMonth = (Integer.parseInt(ls_cyc_ym.substring(0, 4)) - Integer.parseInt(ls_acc_ym.substring(0, 4))) * 12;
//			diffMonth = diffMonth + (Integer.parseInt(ls_cyc_ym.substring(4, 6)) - Integer.parseInt(ls_acc_ym.substring(4, 6)));
//
//		}else {
//			return "";
//		}
//		
//		return String.valueOf(diffMonth);
//	}
	
	
	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}

}
