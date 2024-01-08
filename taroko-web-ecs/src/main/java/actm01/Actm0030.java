/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-07-20  V1.00.04  Simon      1.取消本金類調整                           *
*                                 2.取消銷帳鍵值、借方科目                   *
* 112-12-18  V1.00.05  Simon      1.恢復本金類調整                           *
*                                 2.各科目調整設定獨立調整類別               *
*                                 3.新增減免分期付款利息調整類別             *
*                                 4.exclude act_acaj.process_flag='Y'        *
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon; 

public class Actm0030 extends BaseEdit {
	CommString commString = new CommString();
    String mPSeqno = "";
    String mDbTable = "";
    String mReferenceNo = "";
    String mRowid = "";
    String mAcnoName = "";
    String mAcctKey = "";
    String mAdjComment = "";
    String mBillType = "";
    
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc=1;

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
            //-資料讀取- 
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
            /* 瀏覽功能 :skip-page*/
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
    
//    @Override
//	public void initPage() {
//    	if (is_action.equals("new") || empty(is_action)) {
//			wp.col_set("value_type", "2");
//		}
//	}

    @Override
    public void queryFunc() throws Exception {
    	//設定queryRead() SQL條件
    	wp.setQueryMode();  //顯示正確頁數,必須放在這邊
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
      wp.pageControl();

      Object[] param = null;
      String lsSql = "";
      String acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));
      /* 
      ls_sql  = "select p_seqno from act_acno where ACCT_KEY=? ";
      ls_sql += " and acno_p_seqno = p_seqno ";
      param = new Object[] { acctkey };
      */
      String accttype = wp.itemStr2("ex_acct_type");
      
      lsSql  = "select p_seqno from act_acno where ACCT_KEY=? and acct_type=? ";
      lsSql += " and acno_p_seqno = p_seqno ";
      param = new Object[] { acctkey,accttype };
          
      sqlSelect(lsSql, param);
		  
		  if (empty(sqlStr("p_seqno"))) {
		  	mPSeqno = "";
		  } else {
		  	mPSeqno = sqlStr("p_seqno");
		  }
		  String strWhereA = "and act_debt.p_seqno= :p_seqno and act_debt.acct_code not in "
		                   + "('DP') and act_debt.dc_end_bal >= 0";
		  setString("p_seqno", mPSeqno);
		  String strWhereB = "and act_debt_hst.p_seqno= :p_seqno and act_debt_hst.acct_code not in " 
		                   + "('DP') and act_debt_hst.dc_end_bal >= 0";
		  setString("p_seqno", mPSeqno);
		  
		  if(empty(wp.itemStr2("ex_curr_code")) == false){
		  	strWhereA += " and act_debt.curr_code = :curr_code ";
		  	setString("curr_code", wp.itemStr2("ex_curr_code"));
		  	strWhereB += " and act_debt_hst.curr_code = :curr_code ";
		  	setString("curr_code", wp.itemStr2("ex_curr_code"));
		  }
		  if(empty(wp.itemStr2("ex_s_yyymm")) == false){
		  	strWhereA += " and act_debt.acct_month >= :acct_months ";
		  	setString("acct_months", wp.itemStr2("ex_s_yyymm"));
		  	strWhereB += " and act_debt_hst.acct_month >= :acct_months ";
		  	setString("acct_months", wp.itemStr2("ex_s_yyymm"));
		  }
		  if(empty(wp.itemStr2("ex_e_yyymm")) == false){
		  	strWhereA += " and act_debt.acct_month <= :acct_monthe ";
		  	setString("acct_monthe", wp.itemStr2("ex_e_yyymm"));
		  	strWhereB += " and act_debt_hst.acct_month <= :acct_monthe ";
		  	setString("acct_monthe", wp.itemStr2("ex_e_yyymm"));
		  }
		  		
		  String acctCodeIn = "";
		  if(eqIgno(wp.itemStr2("ex_acitem01"),"Y")){
		  	acctCodeIn += ",'AF','LF','CF','PF','SF','CC'";
		  }
		  if(eqIgno(wp.itemStr2("ex_acitem02"),"Y")){
		  	acctCodeIn += ",'RI','AI','CI'";
		  }
		  if(eqIgno(wp.itemStr2("ex_acitem03"),"Y")){
		  	acctCodeIn += ",'PN'";
		  }
		  if(eqIgno(wp.itemStr2("ex_acitem04"),"Y")){
		  	acctCodeIn += ",'BL','CA','IT','ID','AO','OT','CB','DB' ";
		  }
		//if(eqIgno(wp.itemStr2("ex_acitem05"),"Y")){
		//	acctCodeIn += ",'ID'";
		//}
		  if(acctCodeIn.length() > 0) {
		  	strWhereA += " and act_debt.acct_code in ("+ acctCodeIn.substring(1) +") ";
		  	strWhereB += " and act_debt_hst.acct_code in ("+ acctCodeIn.substring(1) +") ";
		  }
		  
		  wp.sqlCmd = " SELECT act_debt.reference_no, " +
          		"          act_debt.p_seqno, " +
          		"          act_debt.acct_type, " +
          		"          act_debt.post_date, " +
          		"          act_debt.acct_month, " +
          		"          act_debt.card_no, " +
          		"          act_debt.interest_date, " +
          		"          act_debt.beg_bal, " +
          		"          act_debt.end_bal, " +
          		"          act_debt.d_avail_bal, " +
          		"          act_debt.txn_code, " +
          		"          act_debt.acct_code, " +
          		"          act_debt.bill_type, " +
          		"          'debt' as db_table, " +
          		"          hex(act_debt.rowid) as rowid, " +
          		"          act_debt.interest_rs_date, " +
          		"          ptr_actcode.chi_long_name as acct_item_cname, " +
          		"          act_debt.curr_code, " +
          		"          'U' function_code, " +
          		"          act_debt.dc_beg_bal, " +
          		"          act_debt.dc_end_bal, " +
          		"          act_debt.dc_d_avail_bal " +
          		" FROM act_debt, ptr_actcode " +
          		" where act_debt.acct_code = ptr_actcode.acct_code " +
          		strWhereA +
          		" union " +
          		" SELECT act_debt_hst.reference_no, " +
          		"          act_debt_hst.p_seqno, " +
          		"          act_debt_hst.acct_type, " +
          		"          act_debt_hst.post_date, " +
          		"          act_debt_hst.acct_month, " +
          		"          act_debt_hst.card_no, " +
          		"          act_debt_hst.interest_date, " +
          		"          act_debt_hst.beg_bal, " +
          		"          act_debt_hst.end_bal, " +
          		"          act_debt_hst.d_avail_bal, " +
          		"          act_debt_hst.txn_code, " +
          		"          act_debt_hst.acct_code, " +
          		"          act_debt_hst.bill_type, " +
          		"          'debt_hst' as db_table, " +
          		"          hex(act_debt_hst.rowid) as rowid, " +
          		"          act_debt_hst.interest_rs_date, " +
          		"          ptr_actcode.chi_long_name as acct_item_cname, " +
          		"          act_debt_hst.curr_code, " +
          		"          'U' function_code , " +
          		"          act_debt_hst.dc_beg_bal, " +
          		"          act_debt_hst.dc_end_bal, " +
          		"          act_debt_hst.dc_d_avail_bal " +
          		" FROM act_debt_hst, ptr_actcode " +
          		" where act_debt_hst.acct_code = ptr_actcode.acct_code " +
          		strWhereB +
          		" order by acct_month,post_date ";
      
		  wp.pageCountSql ="select count(*) from ("
		  		+" select hex(act_debt.rowid) from act_debt, ptr_actcode " +
          		" where act_debt.ACCT_CODE = ptr_actcode.ACCT_CODE " +strWhereA
		  		+" union select hex(act_debt_hst.rowid) from act_debt_hst, ptr_actcode " +
          		" where act_debt_hst.ACCT_CODE = ptr_actcode.ACCT_CODE " + strWhereB
		  		+" )";
		  
		  pageQuery();
		  
          wp.setListCount(1);
          if (sqlNotFind()) {
              alertErr(appMsg.errCondNodata);
              return;
          }
      
          ofcQueryafter();
          wp.setPageValue();
      
    }
    
    void ofcQueryafter() throws Exception {
    	String lsCname="", lsCorpCname="";
    	
    	//Get id_p_seqno, corp_p_seqno
    	String lsSql = "select act_acno.id_p_seqno, crd_idno.chi_name id_cname, "
    				+ "act_acno.corp_p_seqno, nvl(crd_corp.chi_name,'') corp_cname, "
    				+ "act_acno.acct_type, act_acno.acct_key from act_acno "
    				+ "left join crd_idno on crd_idno.id_p_seqno = act_acno.id_p_seqno "
    				+ "left join crd_corp on crd_corp.corp_p_seqno = act_acno.corp_p_seqno "
    				+ "where act_acno.acno_p_seqno = :p_seqno ";
    	setString("p_seqno", mPSeqno);
    	sqlSelect(lsSql);
    	if (sqlRowNum >= 0) {
    		lsCname=sqlStr("id_cname");
    		lsCorpCname=sqlStr("corp_cname");
    	}
    	wp.colSet("ex_cname", lsCname);
    	wp.colSet("ex_corp_cname", lsCorpCname);
 
      /*
      String[] aa_val = {sql_ss("acct_type")};
      wp.setInBuffer("ex_acct_type", aa_val);
      */
 
    	//--Read act_acaj---------------------------------------------------------------
    	//TAG2000:
    	for (int ii = 0; ii < wp.selectCnt; ii++) {
    		if (!eqIgno(wp.colStr(ii,"curr_code"),"840")) {  //整數	
    			wp.colSet(ii,"dc_beg_bal", commString.numFormat(wp.colNum(ii,"dc_beg_bal"),"#,##0"));
        		wp.colSet(ii,"dc_end_bal", commString.numFormat(wp.colNum(ii,"dc_end_bal"),"#,##0"));
        		wp.colSet(ii,"dc_d_avail_bal", commString.numFormat(wp.colNum(ii,"dc_d_avail_bal"),"#,##0"));
    		} else {  //小數兩位
    			wp.colSet(ii,"dc_beg_bal", commString.numFormat(wp.colNum(ii,"dc_beg_bal"),"#,##0.00"));
        		wp.colSet(ii,"dc_end_bal", commString.numFormat(wp.colNum(ii,"dc_end_bal"),"#,##0.00"));
        		wp.colSet(ii,"dc_d_avail_bal", commString.numFormat(wp.colNum(ii,"dc_d_avail_bal"),"#,##0.00"));
    		}
    		
    		lsSql = "select act_acaj.orginal_amt, act_acaj.aft_amt, act_acaj.aft_d_amt, act_acaj.acct_code, hex(act_acaj.rowid) as rowid, "
    				+ "'acaj' as db_table, act_acaj.curr_code, act_acaj.dc_orginal_amt, act_acaj.dc_aft_amt, act_acaj.dc_aft_d_amt, "
    				+ "ptr_actcode.chi_short_name from act_acaj "
    				+ "left join ptr_actcode on act_acaj.acct_code = ptr_actcode.acct_code "
    				+ "where reference_no = :reference_no "
    				+ " and process_flag != 'Y' ";
       	setString("reference_no", wp.colStr(ii,"reference_no"));
    		sqlSelect(lsSql);
        	if (sqlRowNum > 0) {
        		wp.colSet(ii,"beg_bal", sqlStr("orginal_amt"));
        		wp.colSet(ii,"end_bal", sqlStr("aft_amt"));
        		wp.colSet(ii,"d_avail_bal", sqlStr("aft_d_amt"));
        		wp.colSet(ii,"acct_code", sqlStr("acct_code"));
        		wp.colSet(ii,"rowid", sqlStr("rowid"));
        		wp.colSet(ii,"db_table", sqlStr("db_table"));
        		wp.colSet(ii,"curr_code", sqlStr("curr_code"));
        		if (!eqIgno(sqlStr("curr_code"),"840")) {  //整數
        			wp.colSet(ii,"dc_beg_bal", commString.numFormat(sqlNum("dc_orginal_amt"),"#,##0"));
            		wp.colSet(ii,"dc_end_bal", commString.numFormat(sqlNum("dc_aft_amt"),"#,##0"));
            		wp.colSet(ii,"dc_d_avail_bal", commString.numFormat(sqlNum("dc_aft_d_amt"),"#,##0"));
        		} else {  //小數兩位
        			wp.colSet(ii,"dc_beg_bal", commString.numFormat(sqlNum("dc_orginal_amt"),"#,##0.00"));
            		wp.colSet(ii,"dc_end_bal", commString.numFormat(sqlNum("dc_aft_amt"),"#,##0.00"));
            		wp.colSet(ii,"dc_d_avail_bal", commString.numFormat(sqlNum("dc_aft_d_amt"),"#,##0.00"));
        		}
        		wp.colSet(ii,"acct_item_cname", sqlStr("chi_short_name"));
        	}
		}
    }

    @Override
    public void querySelect() throws Exception {
    	mDbTable = itemKk("data_k1");
    	mReferenceNo  = itemKk("data_k2");
    	mRowid    = itemKk("data_k3");
    	mBillType    = itemKk("data_k4");
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
		if (empty(mDbTable))		mDbTable = wp.itemStr2("db_table");
		if (empty(mReferenceNo))	mReferenceNo = wp.itemStr2("reference_no");
		if (empty(mRowid))			mRowid = wp.itemStr2("rowid");
		if (empty(mBillType))		mBillType = wp.itemStr2("bill_type");
		
    /***
		if (eq_igno(m_db_table,"acaj")) {
			dataReadAcaj();
		}
		if (eq_igno(m_db_table,"debt")) {
			dataReadDebt();
		}
		if (eq_igno(m_db_table,"debt_hst")) {
			dataReadDebtHst();
		}
    ***/

		wp.colSet("db_table", "");
		dataReadAcaj();
		if (eqIgno(wp.colStr("db_table"),"acaj") &&
		    commString.mid(wp.colStr("adjust_type"), 0,2).equals("DR") )  {
			 alertErr("已被D檔Reversal,不可D檔處理, reference_no= " + mReferenceNo);
       return;
		}

		if (!eqIgno(wp.colStr("db_table"),"acaj")) {
			dataReadDebt();
		  if (!eqIgno(wp.colStr("db_table"),"debt")) {
	  		dataReadDebtHst();
		  }
		}

		if (!eqIgno(wp.colStr("db_table"),"acaj") &&
		    !eqIgno(wp.colStr("db_table"),"debt") &&
		    !eqIgno(wp.colStr("db_table"),"debt_hst") )  {
			 alertErr("查無資料, reference_no= " + mReferenceNo);
       return;
		}
		
		getAcnobyPseqno(wp.colStr("p_seqno"));
		wp.colSet("ex_acno_name", mAcnoName);
		wp.colSet("acct_key", mAcctKey);
		wp.colSet("bill_type", mBillType);
	//if (!eq_igno(m_db_table,"acaj")) { wp.col_set("adj_comment", m_adj_comment); }
		if (!eqIgno(wp.colStr("db_table"),"acaj")) { wp.colSet("adj_comment", mAdjComment); }
		wp.colSet("ex_debt_chi", getChiShortName(wp.colStr("acct_code")));

/***
		if ( eqIgno(wp.colStr("acct_code"),"CB") || eqIgno(wp.colStr("acct_code"),"DB") ) 
		{ 
			wp.colSet("debit_item", "55030700"); 
			wp.colSet("debit_item_disabled", "disabled"); 
		}
		wp.colSet("sv_debit_item", wp.colStr("debit_item"));
***/
    return;
	}
    
	void dataReadAcaj() throws Exception {
		wp.selectSQL = " 'acaj' as db_table, " +
				" crt_date as create_date, " +  //" create_date, " +
				" crt_time as create_time, " +  //" create_time, " +
				" p_seqno, " +
				" acct_type, " +
    			" acct_code, " +
	      		" adjust_type, " +
	      		" reference_no, " +
	      		" post_date, " +
	      		" orginal_amt, " +
	      		" dr_amt, " +
	      		" cr_amt, " +
	      		" bef_amt, " +
	      		" aft_amt, " +
	      		" bef_d_amt, " +
	      		" aft_d_amt, " +
	      		" acct_code as acct_item_ename, " +  //" acct_item_ename, " +
	      		" function_code, " +
	      		" card_no, " +
	      		" cash_type, " +
	      		" value_type, " +
	      		" trans_acct_type, " +
	      		" trans_acct_key, " +
	      		" interest_date, " +
	      		" adj_reason_code, " +
	      		" adj_comment, " +
	      		" c_debt_key, " +
	      		" debit_item, " +
	      		" apr_flag as confirm_flag, " +  //" confirm_flag, " +
	      		" update_date, " +
	      		" update_user, " +
	      		" mod_user, " +
	      		" mod_time, " +
	      		" mod_pgm, " +
	      		" mod_seqno, " +
	      		" 'Y' as ex_dcount, " +
	      		" lpad(' ',30,' ') as ex_cname, " +
	      		" lpad(' ',40,' ') as ex_corp_cname, " +
	      		" hex(rowid) as rowid, " +
	      		" job_code, " +
	      		" vouch_job_code, " +
	      		" curr_code, " +
	      		" dc_orginal_amt, " +
	      		" dc_dr_amt, " +
	      		" dc_cr_amt, " +
	      		" dc_bef_amt, " +
	      		" dc_aft_amt, " +
	      		" dc_bef_d_amt, " +
	      		" dc_aft_d_amt, "
	      		+ "apr_flag ";
      
		wp.daoTable = "act_acaj";
//		wp.whereStr = "where hex(rowid) = :rowid " ;
//		setString("rowid", m_rowid);
		wp.whereStr = "where reference_no = :reference_no " 
		            + " and process_flag != 'Y' ";
		setString("reference_no", mReferenceNo);

		wp.whereOrder=" ";

		pageSelect();
		if (sqlNotFind()) {
		//alert_err("查無資料, db_table= "+m_db_table+", reference_no= " + m_reference_no);
      return;
		}
		
		if (!eqIgno(wp.colStr("curr_code"),"840")) {  //整數
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0"));
		} else {  //小數兩位
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0.00"));
		}
	}

	void dataReadDebt() throws Exception {

		wp.selectSQL = " 'debt' as db_table, " +
				" p_seqno, " +
    		    " acct_type, " +
    		    " reference_no, " +
    		    " post_date, " +
    		    " beg_bal as orginal_amt, " +
    			" 0 as dr_amt, " +
    			" 0 as cr_amt, " +
    			" end_bal as bef_amt, " +
    			" end_bal as aft_amt, " +
    			" d_avail_bal as bef_d_amt, " +
    			" d_avail_bal as aft_d_amt, " +
    			" acct_code, " +
    			" 'U' as function_code, " +
    			" card_no, " +
        		" interest_date, " +
        		" 'N' as ex_dcount, " +
        		" curr_code, " +
        		" dc_beg_bal as dc_orginal_amt, " +
	      		" 0 as dc_dr_amt, " +
	      		" 0 as dc_cr_amt, " +
	      		" dc_end_bal as dc_bef_amt, " +
	      		" dc_end_bal as dc_aft_amt, " +
	      		" dc_d_avail_bal as dc_bef_d_amt, " +
	      		" dc_d_avail_bal as dc_aft_d_amt ";
		
		wp.daoTable = "act_debt";
		wp.whereStr = "where reference_no = :reference_no " ;
		setString("reference_no", mReferenceNo);

		wp.whereOrder=" ";

		pageSelect();
		if (sqlNotFind()) {
		//alert_err("查無資料, db_table= "+m_db_table+", reference_no= " + m_reference_no);
      return;
		}

		wp.colSet("value_type", "2");
		wp.colSet("adj_reason_code", "1");
	//wp.colSet("debit_item", "14817000");
		wp.colSet("pho_disable", "disabled style='background-color: lightgray;'");
		
		if (!eqIgno(wp.colStr("curr_code"),"840")) {  //整數
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0"));
		} else {  //小數兩位
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0.00"));
		}
	}
	
	void dataReadDebtHst() throws Exception {
		wp.selectSQL = " 'debt_hst' as db_table, " +
				" p_seqno, " +
    		    " acct_type, " +
    		    " reference_no, " +
    		    " post_date, " +
    		    " beg_bal as orginal_amt, " +
    			" 0 as dr_amt, " +
    			" 0 as cr_amt, " +
    			" end_bal as bef_amt, " +
    			" end_bal as aft_amt, " +
    			" d_avail_bal as bef_d_amt, " +
    			" d_avail_bal as aft_d_amt, " +
    			" acct_code, " +
    			" 'U' as function_code, " +
    			" card_no, " +
        		" interest_date, " +
        		" 'N' as ex_dcount, " +
        		" curr_code, " +
        		" dc_beg_bal as dc_orginal_amt, " +
	      		" 0 as dc_dr_amt, " +
	      		" 0 as dc_cr_amt, " +
	      		" dc_end_bal as dc_bef_amt, " +
	      		" dc_end_bal as dc_aft_amt, " +
	      		" dc_d_avail_bal as dc_bef_d_amt, " +
	      		" dc_d_avail_bal as dc_aft_d_amt ";
		
		wp.daoTable = "act_debt_hst";
		wp.whereStr = "where reference_no = :reference_no " ;
		setString("reference_no", mReferenceNo);

		wp.whereOrder=" ";

		pageSelect();
		if (sqlNotFind()) {
		//alert_err("查無資料, db_table= "+m_db_table+", reference_no= " + m_reference_no);
      return;
		}

		wp.colSet("value_type", "2");
		wp.colSet("adj_reason_code", "1");
	//wp.colSet("debit_item", "14817000");
		wp.colSet("pho_disable", "disabled style='background-color: lightgray;'");
		
		if (!eqIgno(wp.colStr("curr_code"),"840")) {  //整數
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0"));
		} else {  //小數兩位
			wp.colSet("ex_dc_bef_amt", commString.numFormat(wp.colNum("dc_bef_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_amt", commString.numFormat(wp.colNum("dc_aft_amt"),"#,##0.00"));
			wp.colSet("ex_dc_bef_d_amt", commString.numFormat(wp.colNum("dc_bef_d_amt"),"#,##0.00"));
			wp.colSet("ex_dc_aft_d_amt", commString.numFormat(wp.colNum("dc_aft_d_amt"),"#,##0.00"));
		}
	}
	
	void getAcnobyPseqno(String pseqno) throws Exception {
		mAcnoName = "";
	    mAcctKey = "";
	    mAdjComment = "";
		String lsSql = "select uf_acno_name(acno_p_seqno) acno_name, acct_key, special_comment "
				  + "from act_acno where acno_p_seqno = :p_seqno ";
		setString("p_seqno", pseqno);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			mAcnoName = sqlStr("acno_name");
		    mAcctKey = sqlStr("acct_key");
		    mAdjComment = sqlStr("special_comment");
		}
	}
	
	String getChiShortName(String acctcode) throws Exception {
		String rtn="";
		String lsSql = "select chi_short_name from ptr_actcode where acct_code = :acct_code ";
		setString("acct_code", acctcode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn=sqlStr("chi_short_name");
		}
		return rtn;
	}

    @Override
    public void saveFunc() throws Exception {
//    	String a = wp.item_ss("dc_dr_amt");
//    	String b = wp.item_ss("dc_bef_d_amt");
//    	int dramt = Integer.parseInt(a);  <-- 有小數點時程式會當
//    	int befamt = Integer.parseInt(b);
/***
      if ( eqIgno(wp.colStr("acct_code"),"CB") || eqIgno(wp.colStr("acct_code"),"DB") ) 
		  { 
			    wp.colSet("debit_item_disabled", "disabled"); 
		      wp.colSet("debit_item", wp.colStr("sv_debit_item"));
		  }
***/
    	Actm0030Func func = new Actm0030Func(wp);
    	
    	if (strAction.equals("U")) {
    		double dramt = wp.itemNum("dc_dr_amt");
        	double befamt = wp.itemNum("dc_bef_d_amt");
        	
        	if(dramt > befamt) {
        		alertErr("調整錯誤，調整金額大於可調整金額");
        		return;
        	}

          long   drCvtLong   = (long) Math.round(dramt);
          double drCvtDouble =  ((double) drCvtLong);
        	if((!eqIgno(wp.itemStr2("curr_code"),"840")) && (dramt != drCvtDouble  ) ) {
        		alertErr("D檔金額 輸入錯誤,不可輸入小數");
        		return;
        	}

         /*** 已於 actm0030_detl.html 設定限制 (zEdit="dignumber")
        	if(dramt < 0) {
        		alert_err("調整錯誤，調整金額不可小於 0");
        		return;
        	}
         ***/ 

		}

        rc = func.dbSave(strAction);
        log(func.getMsg());
        if (rc!=1) {
            alertErr2(func.getMsg());
        }
        this.sqlCommit(rc);

        if ((rc == 1) && strAction.equals("U")) {
        	mDbTable = "acaj";
        	wp.colSet("pho_disable", "");
			dataRead();
		}
        if ((rc == 1) && strAction.equals("D")) {
        	clearFunc();
		}
   
    }

    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
        }
    }

    @Override
    public void dddwSelect() {
        try {	
//			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_acct_type");
			dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_curr_code");
			dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
        }
        catch(Exception ex) {}
    }

    String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}

}

