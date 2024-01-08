/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 107-08-28  V1.00.01  Alex       bug fixed                                  *
* 108-12-12  V1.00.02  Alex       bug fixed                                  *
* 108-12-20  V1.00.03  Alex       ser_num fix                                *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112/01/17  V1.00.04  Simon      tabClick() alternative solution            *
* 112/03/25  V1.00.05  Simon      value_type="1" for orig interest_date, "2" for business_date to set interest_date*
* 112-07-23  V1.00.06  Simon      取消銷帳鍵值、借方科目                     *
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import busi.FuncEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actm0137 extends BaseEdit {
	CommString commString = new CommString();
  busi.CommCurr commCurr=new busi.CommCurr();
	String pPSeqno = "";
	String[] opt;
	String[] rowid;
	String lsSql = "";
	String hExistFlag = "";
	Actm0137Func func;

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
		} else if (eqIgno(wp.buttonCode, "X1")) {
			strAction = "X1";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "X2")) {
			strAction = "X2";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "X3")) {
			strAction = "X3";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "I")) {
			/* 動態查詢 */
		  getInfo();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

    tabClick();

		dddwSelect();
		initButton();
	}

	@Override

	public void queryFunc() throws Exception {

		// 設定queryRead() SQL條件
		String lsPSeqno = "";
		//if (empty(wp.item_ss("p_seqno"))) {
		//	ls_p_seqno = getInfo();
		//} else {
		//	ls_p_seqno = wp.item_ss("p_seqno");
		//}

		lsPSeqno = getInfo();
		if (lsPSeqno.equals("")) {
			alertErr2("無此帳號/卡號");
			return;
		}

		pPSeqno = lsPSeqno;

		// -page control-
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		queryReadA();
		queryReadB();
		queryReadC();
    if (hExistFlag.equals("Y")) {
        wp.notFound = "N";
    }

		dddwSelect();
	}

	void queryReadA() throws Exception {
 		selectNoLimit();
		daoTid = "A-";
		wp.pageControl();
		wp.sqlCmd = " select "
					 + " crt_date ,"
					 + " crt_time ,"
					 + " p_seqno ,"
					 + " acct_type ,"
					 + " uf_acno_key(p_seqno) as acct_key ,"
					 + " acct_date ,"
					 + " tran_class ,"
					 + " tran_type ,"
					 + " acct_code ,"
					 + " dr_cr ,"
					 + " uf_dc_amt(curr_code,transaction_amt,dc_transaction_amt) transaction_amt ,"
		  		 + " uf_dc_amt(curr_code,jrnl_bal,dc_jrnl_bal) jrnl_bal , "
					 + " interest_date ,"
					 + " reversal_flag ,"
					 + " payment_rev_amt ,"
					 + " reference_no ,"
					 + " mod_seqno ,"
					 + " hex(rowid) as rowid ,"
					 + " 0.00 as db_cramt ,"
					 + " 0.00 as db_damt ,"
					 + " uf_nvl(curr_code,'901') as curr_code , "
					 + " '' as set_amt "
					 + " from act_jrnl "
					 + " where 1=1 and tran_class ='P' and tran_type <>'DUMY' "
					 +sqlCol(pPSeqno,"p_seqno")
					 +sqlCol(wp.itemStr2("ex_curr_code"),"uf_nvl(curr_code,'901')")
					 +sqlCol(wp.itemStr2("ex_date_s"),"acct_date",">=")
					 +sqlCol(wp.itemStr2("ex_date_e"),"acct_date","<=")
					 + " order by acct_date Desc "
					 +commSqlStr.rownum(250)
				    ;
		
		pageQuery();		

		if (sqlNotFind()) {
			return;
		} else {
			hExistFlag = "Y";
		}
		
		wp.colSet("click_total_A", "0");
		wp.setListCount(1);
		listWkdataA();
	}

	void listWkdataA() throws Exception {
//		String trnamt = "", payamt = "";
		double trnamt = 0, payamt = 0, mJrnlbal = 0;

		for (int ii = 0; ii < wp.selectCnt; ii++) {
//			trnamt = wp.col_ss(ii, "a-transaction_amt");
//			payamt = wp.col_ss(ii, "a-payment_rev_amt");
//			wp.col_set(ii, "a-dramt", String.valueOf(Integer.parseInt(trnamt) - Integer.parseInt(payamt)));
			trnamt = wp.colNum(ii, "A-transaction_amt");
			mJrnlbal = wp.colNum(ii, "A-jrnl_bal");
			if (mJrnlbal > 0) {
			  mJrnlbal = 0;
			}

			payamt = wp.colNum(ii, "A-payment_rev_amt");
			wp.colSet(ii, "A-adramt", (trnamt + mJrnlbal - payamt));
			if (wp.colNum(ii, "A-adramt") < 0) {
			  wp.colSet(ii, "A-adramt", "0");
			}
			wp.colSet(ii, "A-dramt" , (trnamt + mJrnlbal - payamt));
			if (wp.colNum(ii, "A-dramt") < 0) {
			  wp.colSet(ii, "A-dramt", "0");
			}
			wp.colSet(ii,"set_amt_A", "");
		}
	}

	void queryReadB() throws Exception {
 		selectNoLimit();
		daoTid = "B-";
		wp.pageControl();
		wp.sqlCmd = " select "
					 + " A.reference_no , "
					 + " 0.00 cr_amt , "
					 + " 0.00 wk_aftamt , "
					 + " A.p_seqno , "
					 + " A.acct_type , "
					 + " uf_acno_key(A.p_seqno) as acct_key , "
					 + " A.post_date , "
					 + " A.item_order_normal , "
					 + " A.item_order_back_date , "
					 + " A.item_order_refund , "
					 + " A.item_class_normal , "
					 + " A.item_class_back_date , "
					 + " A.item_class_refund , "
					 + " A.acct_month , "
					 + " A.stmt_cycle , "
					 + " A.bill_type , "
					 + " A.txn_code , "
					 + " A.card_no , "
					 + " A.acct_code , "
					 + " B.chi_short_name , "
					 + " A.interest_date , "
					 + " A.purchase_date , "
					 + " A.mcht_no , "
					 + " A.interest_rs_date , "
					 + " A.crt_date , "
					 + " A.crt_user , "
					 + " A.beg_bal as beg_bal_tw , "
					 + " decode(A.curr_code,'','901',A.curr_code) as curr_code , "
					 + " uf_dc_amt(A.curr_code,A.beg_bal,A.dc_beg_bal) as beg_bal , "
					 + " uf_dc_amt(A.curr_code,A.end_bal,A.dc_end_bal) as end_bal , "
					 + " uf_dc_amt(A.curr_code,A.d_avail_bal,A.dc_d_avail_bal) as d_avail_bal , "
					 + " A.mod_seqno , "
					 + " hex(A.rowid) as rowid,                                                                            "
					 + " 'debt' as db_Table , "
					 + " '' as set_amt "
					 + " from act_debt A , ptr_actcode B                                                                            "
					 + " where 1=1 "
					 + " and A.acct_code=B.acct_code "
					 + " and A.acct_code != 'DP' "
					 +sqlCol(pPSeqno,"A.p_seqno")
					 +sqlCol(wp.itemStr2("ex_curr_code"),"uf_nvl(curr_code,'901')")
					 +sqlCol(wp.itemStr2("ex_date_s"),"A.post_date",">=")
					 +sqlCol(wp.itemStr2("ex_date_e"),"A.post_date","<=")
					 + " union "
					 + " select "
					 + " A.reference_no ,"
					 + " 0.00 cr_amt ,"
					 + " 0.00 wk_aftamt ,"
					 + " A.p_seqno ,"
					 + " A.acct_type ,"
					 + " uf_acno_key(A.p_seqno) as acct_key ,"
					 + " A.post_date ,"
					 + " A.item_order_normal ,"
					 + " A.item_order_back_date ,"
					 + " A.item_order_refund ,"
					 + " A.item_class_normal ,"
					 + " A.item_class_back_date ,"
					 + " A.item_class_refund ,"
					 + " A.acct_month ,"
					 + " A.stmt_cycle ,"
					 + " A.bill_type ,"
					 + " A.txn_code ,"
					 + " A.card_no ,"
					 + " A.acct_code ,"
					 + " B.chi_short_name ,"
					 + " A.interest_date ,"
					 + " A.purchase_date ,"
					 + " A.mcht_no ,"
					 + " A.interest_rs_date ,"
					 + " A.crt_date ,"
					 + " A.crt_user ,"
					 + " A.beg_bal as beg_bal_tw ,"
					 + " decode(A.curr_code,'','901',A.curr_code) as curr_code ,"
					 + " uf_dc_amt(A.curr_code,A.beg_bal,A.dc_beg_bal) as beg_bal ,"
					 + " uf_dc_amt(A.curr_code,A.end_bal,A.dc_end_bal) as end_bal ,"
					 + " uf_dc_amt(A.curr_code,A.d_avail_bal,A.dc_d_avail_bal) as d_avail_bal ,"
					 + " A.mod_seqno ,"
					 + " hex(A.rowid) as rowid ,"
					 + " 'debt_hist' db_Table , "
					 + " '' as set_amt "
					 + " from act_debt_hst A , ptr_actcode B "
					 + " where 1=1 "
					 + " and A.acct_code=B.acct_code "
					 + " and A.acct_code != 'DP' "
					 +sqlCol(pPSeqno,"A.p_seqno")
					 +sqlCol(wp.itemStr2("ex_curr_code"),"uf_nvl(curr_code,'901')")
					 +sqlCol(wp.itemStr2("ex_date_s"),"A.post_date",">=")
					 +sqlCol(wp.itemStr2("ex_date_e"),"A.post_date","<=")
					 + " order by post_date Asc  "
					 +commSqlStr.rownum(250)
					 ;

		pageQuery();
		

		if ( sqlNotFind() && !wp.itemStr2("ex_curr_code").equals("901") && !wp.itemStr2("ex_curr_code").equals("") )  {
			//errmsg("無外幣帳款!");
			//return ;
       wp.alertMesg("無此外幣帳款!");
		}
		
		if (sqlNotFind()) {
			return;
		} else {
			hExistFlag = "Y";
		}
		
		//wp.col_set("click_total_B", "0");
	//wp.colSet("ex_onflag", "Y");
		wp.setListSernum(1, "ser_num_b", wp.selectCnt);
//	wp.setListCount(2);
		
	//wp.colSet("ex_cr_item", "24817000");  //phopho add
		
		listWkdataB();
		
		if (empty(wp.colStr("ex_reason_code"))) {
			  wp.colSet("ex_reason_code", "3");
		}

	}

	void listWkdataB() throws Exception {
		String refno = "";
		double trnamt = 0, payamt = 0, lmAmt = 0;
		int cnt = 0;
	  int lmAmtTw = 0;
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			trnamt = wp.colNum(ii, "b-d_avail_bal");
			payamt = wp.colNum(ii, "b-end_bal");
			refno = wp.colStr(ii, "b-reference_no");
			wp.colSet(ii, "b-dramt", (trnamt - payamt));
			wp.colSet(ii, "b-adramt", (trnamt - payamt));
			wp.colSet(ii,"set_amt_B", "");
			lsSql = "  SELECT act_acaj.reference_no,       " + "         act_acaj.adj_reason_code,       "
					+ "         act_acaj.adj_comment,           " + "         act_acaj.c_debt_key,            "
					+ "         act_acaj.debit_item ,           " + "         act_acaj.cr_amt,                "
					+ "         act_acaj.dc_cr_amt,             " + "         act_acaj.interest_date,            "
					+ "         act_acaj.apr_flag,              " + "         hex(act_acaj.rowid) as rowid    "
					+ "    FROM act_acaj                        " + "   where act_acaj.reference_no = :refno  "
					+ "    and adjust_type='DR11' and function_code='U' ";					 

			setString("refno", refno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet(ii, "b-acajrowid", sqlStr("rowid"));
			//wp.col_set(ii, "b-adramt", sql_num("cr_amt"));
			  wp.colSet(ii, "b-adramt", sqlNum("dc_cr_amt"));
				wp.colSet(ii, "b-apr_flag", sqlStr("apr_flag"));
				wp.colSet(ii, "b-db_Table", "acaj");
			//wp.col_set(ii,"click_B", "checked");
				wp.colSet(ii,"opt_on_B", "checked");
				wp.colSet(ii,"set_amt_B", "1");
				wp.colSet(ii,"minus_amt_B", sqlNum("dc_cr_amt"));
				wp.colSet(ii,"b-interest_date", sqlStr("interest_date"));
			//lm_amt_tw += sql_num("cr_amt");
			  lmAmt += sqlNum("dc_cr_amt");
				if (cnt == 0) {
					wp.colSet("ex_reason_code", sqlStr("adj_reason_code"));
					wp.colSet("ex_comment", sqlStr("adj_comment"));
				//wp.colSet("ex_c_debt_key", sqlStr("c_debt_key"));
				//wp.colSet("ex_cr_item", sqlStr("debit_item"));
				}
				cnt++;
			}
      else {
				wp.colSet(ii, "b-acajrowid", sqlStr(""));
				wp.colSet(ii, "b-apr_flag", sqlStr(""));
      }
		}
		
		wp.colSet("click_total_B", lmAmt);
		
	}

	void queryReadC() throws Exception {
 		selectNoLimit();
		daoTid = "C-";
		wp.pageControl();
		wp.sqlCmd = " select " 
				+ " crt_date ,"
				+ " crt_time ,"
				+ " p_seqno ,"
				+ " acct_type ,"
				+ " uf_acno_key(p_seqno) as acct_key ,"
				+ " id_p_seqno ,"
				+ " corp_p_seqno ,"
				+ " acct_date ,"
				+ " tran_class ,"
				+ " tran_type ,"
				+ " dr_cr ,"
				+ " transaction_amt ,"
			//+ " jrnl_bal ,"
				+ " item_bal ,"
				+ " item_d_bal ,"
				+ " item_date ,"
				+ " interest_date ,"
				+ " adj_reason_code ,"
				+ " adj_comment ,"
				+ " reversal_flag ,"
				+ " payment_rev_amt ,"
				+ " reference_no ,"
				+ " pay_id ,"
				+ " stmt_cycle ,"
				+ " c_debt_key ,"
				+ " debit_item ,"
				+ " cash_type ,"
				+ " value_type ,"
				+ " trans_acct_type ,"
				+ " trans_acct_key ,"
				+ " update_user ,"
				+ " mod_user ,"
				+ " mod_time ,"
				+ " mod_pgm ,"
				+ " mod_seqno ,"
				+ " 0.00 as db_cramt ,"
				+ " hex(rowid) as rowid ,"
				+ " 0.00 db_damt ,"
				+ " enq_seqno ,"
				+ " jrnl_seqno ,"
				+ " decode(curr_code,'','901',curr_code) as curr_code ,"
				+ " uf_dc_amt(curr_code,transaction_amt,dc_transaction_amt) dc_transaction_amt , "
				+ " uf_dc_amt(curr_code,jrnl_bal,dc_jrnl_bal) dc_jrnl_bal , "
			//+ " uf_dc_amt(curr_code,transaction_amt,dc_transaction_amt) - payment_rev_amt as dramt , "
				+ " '' as set_amt "
				+ " from act_jrnl "
				+ " where 1=1 "
				+ " and tran_class ='P' and jrnl_seqno <>'' "
				+ sqlCol(pPSeqno,"p_seqno")
				+ sqlCol(wp.itemStr2("ex_curr_code"),"uf_nvl(curr_code,'901')")
				+ sqlCol(wp.itemStr2("ex_date_s"),"acct_date",">=")
				+ sqlCol(wp.itemStr2("ex_date_e"),"acct_date","<=")
				+ " order by interest_date Desc "
				+commSqlStr.rownum(250)
				;
	
		pageQuery();
		
		if (sqlNotFind()) {
			return;
		} else {
			hExistFlag = "Y";
		}
		
		//wp.col_set("click_total_C", "0");
	//wp.colSet("ex_onflagc", "Y");
		wp.setListSernum(2, "ser_num_c", wp.selectCnt);
//	wp.setListCount(3);
		
	//wp.colSet("ex_cr_item2", "24817000");  //phopho add
		
		listWkdataC();

		if (empty(wp.colStr("ex_reason_code2"))) {
			  wp.colSet("ex_reason_code2", "3");
		}

	}

	void listWkdataC() throws Exception {
		String refno = "";
		String cPSeqno = "";
		double trnamt = 0, payamt = 0, mJrnlbal = 0, lmAmt = 0;;
		int cnt = 0;

		for (int ii = 0; ii < wp.selectCnt; ii++) {
		//trnamt = wp.col_num(ii, "c-transaction_amt");
			trnamt = wp.colNum(ii, "c-dc_transaction_amt");
			mJrnlbal = wp.colNum(ii, "c-dc_jrnl_bal");
			if (mJrnlbal > 0) {
			  mJrnlbal = 0;
			}
			
			payamt = wp.colNum(ii, "c-payment_rev_amt");
		//refno = wp.col_ss(ii, "c-reference_no");
			cPSeqno = wp.itemStr2("p_seqno");
//		wp.col_set(ii, "c-adramt", (trnamt - payamt));
  		wp.colSet(ii, "c-adramt", "");
			wp.colSet(ii, "c-dramt", (trnamt + mJrnlbal - payamt));
			if (wp.colNum(ii, "c-dramt") < 0) {
			  wp.colSet(ii, "c-dramt", "0");
			}
			
			wp.colSet(ii,"set_amt_C", "");
			lsSql = "  SELECT "
					 + " reference_no ,"
					 + " adj_reason_code ,"
					 + " adj_comment ," 
					 + " c_debt_key ,"
					 + " debit_item ," 
					 + " cr_amt ,"
					 + " apr_flag ," 
					 + " hex(rowid) as rowid "
					 + " from act_acaj " 
				 //+ " where reference_no = ? "
					 + " where p_seqno = ? "
					 + " and adjust_type='DR11' and function_code='U' "					 
					 ;
			
		//sqlSelect(ls_sql,new Object[]{refno});
			sqlSelect(lsSql,new Object[]{cPSeqno});
			if (sqlRowNum > 0) {
				if (cnt == 0) {
				//wp.col_set("ex_reason_code", sql_ss("adj_reason_code"));
					wp.colSet("ex_reason_code2", sqlStr("adj_reason_code"));
					wp.colSet("ex_comment2", sqlStr("adj_comment"));
				//wp.colSet("ex_c_debt_key2", sqlStr("c_debt_key"));
				//wp.colSet("ex_cr_item2", sqlStr("debit_item"));
				}
				cnt++;
			}

			lsSql = " select "
					 + " acct_date ,"
					 + " interest_date ,"
					 + " curr_code ,"
					 + " transaction_amt , "
					 + " uf_dc_amt(curr_code,transaction_amt,dc_transaction_amt) as dc_transaction_amt , "
					 + " payment_rev_amt ,"
					 + " tran_type ,"
					 + " this_reversal_amt ,"
					 + " apr_flag , "
					 + " jrnl_seqno "
					 + " from act_pay_rev "
					 + " where jrnl_seqno = ? "			
					 + "   and proc_mark not in ('1','2') ";			
			sqlSelect(lsSql,new Object[]{wp.colStr(ii, "c-jrnl_seqno")});
			if (sqlRowNum > 0) {
				wp.colSet(ii, "c-acct_date", sqlStr("acct_date"));
				wp.colSet(ii, "c-interest_date", sqlStr("interest_date"));
				wp.colSet(ii, "c-curr_code", sqlStr("curr_code"));
				wp.colSet(ii, "c-transaction_amt", sqlNum("transaction_amt"));
				wp.colSet(ii, "c-payment_rev_amt", sqlNum("payment_rev_amt"));
				wp.colSet(ii, "c-adramt", sqlNum("this_reversal_amt"));
			//wp.col_set(ii, "c-dramt",sql_num("dc_transaction_amt")-sql_num("payment_rev_amt"));//以前面抓 act_jrnl算出即可

				wp.colSet(ii, "c-tran_type", sqlStr("tran_type"));
				wp.colSet(ii, "c-jrnl_seqno", sqlStr("jrnl_seqno"));
				wp.colSet(ii, "c-db_apr_flag", sqlStr("apr_flag"));
			//wp.col_set(ii,"click_C", "checked");
				wp.colSet(ii,"opt_on_C", "checked");
				wp.colSet(ii,"set_amt_C", "1");
				wp.colSet(ii,"minus_amt_C", sqlNum("this_reversal_amt"));
			  lmAmt += sqlNum("this_reversal_amt");
			}
		}
 		wp.colSet("click_total_C", lmAmt);

	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
	}

	@Override
	public void saveFunc() throws Exception {
		func = new Actm0137Func(wp);
		keepOpt();
		if (strAction.toUpperCase().equals("X1")) {
			goTab1Exec();
		}
		if (strAction.toUpperCase().equals("X2")) {
			goTab2Exec();
		}
		if (strAction.toUpperCase().equals("X3")) {
			goTab3Exec();
		}
		
	}
	
	public void goTab1Exec() throws Exception {
		int llOk =0 , llErr = 0;
		String[] lsRowidA = wp.itemBuff("a-rowid");		
		String[] liAdramt = wp.itemBuff("a-adramt");
		String[] liPaymentRevAmt = wp.itemBuff("a-payment_rev_amt");
		String[] liDramt = wp.itemBuff("a-dramt");
		String[] lsModSeqno = wp.itemBuff("a-mod_seqno");
		String[] aaOpt = wp.itemBuff("a-opt");
		double lmPayRevAmt = 0, lmDAvailAmt = 0;

		wp.listCount[0] = wp.itemRows("a-rowid");
		wp.listCount[1] = wp.itemRows("b-rowid");
		wp.listCount[2] = wp.itemRows("c-rowid");
		
		Actm0137Func func = new Actm0137Func(wp);
		
		if(ofValidationA()!=1)	return ;
		
		for(int ii=0;ii<wp.itemRows("a-rowid");ii++){
			
			if(checkBoxOptOn(ii, aaOpt)==false)	continue;
			
			if(empty(liAdramt[ii]) || commString.strToNum(liAdramt[ii])==0){
				wp.colSet(ii,"a-ok_flag", "X");
				continue;
			}
			
			lmPayRevAmt = commString.strToNum(liPaymentRevAmt[ii]) + commString.strToNum(liAdramt[ii]);
			lmDAvailAmt = commString.strToNum(liDramt[ii]) - commString.strToNum(liAdramt[ii]);
      lmPayRevAmt = convAmtDp2r(lmPayRevAmt);
      lmDAvailAmt = convAmtDp2r(lmDAvailAmt);
			
			func.varsSet("payment_rev_amt", ""+lmPayRevAmt);
			func.varsSet("rowid", lsRowidA[ii]);
			func.varsSet("mod_seqno", lsModSeqno[ii]);
			
			if(func.updateJrnlTab1()==1){
				llOk++;
				sqlCommit(1);
			  wp.colSet(ii, "a-ok_flag","V");
			  wp.colSet(ii, "A-payment_rev_amt", lmPayRevAmt);
			  wp.colSet(ii, "A-dramt", lmDAvailAmt);
				continue;
			}	else	{
				llErr++;
				dbRollback();
				wp.colSet(ii, "a-ok_flag","X");
				continue;
			}			
		}
		
		alertMsg("執行完成 , 成功:"+llOk+" 失敗:"+llErr);
		
	}
	
	public int ofValidationA(){
		String[] lsAdramt = wp.itemBuff("a-adramt");
		String[] lsCurrCode = wp.itemBuff("a-curr_code");
		String[] lsTransactionAmt = wp.itemBuff("a-transaction_amt");
		String[] lsPaymentRevAmt = wp.itemBuff("a-payment_rev_amt");
		String[] aaOpt = wp.itemBuff("a-opt");
		double lmAdramt = 0 , lmAmt1 =0 , lmAmt2 =0;
		int llOk=0;
		
		for(int ii=0;ii<wp.itemRows("a-rowid");ii++){
			if(checkBoxOptOn(ii, aaOpt)==false)	continue;
			lmAdramt = commString.strToNum(lsAdramt[ii]);
			if(lmAdramt==0){
				errmsg("繳款沖回金額 不可 == 0");
				return -1;
			}
			
			llOk++;
			if(eqIgno(lsCurrCode[ii],"901")){
				if(lmAdramt%1!=0){
					errmsg("台幣不可有小數");
					wp.colSet(ii,"a-ok_flag", "X");
					return -1;
				}
			}
			
			lmAmt1 = commString.strToNum(lsTransactionAmt[ii]);
			lmAmt2 = commString.strToNum(lsPaymentRevAmt[ii])+lmAdramt;
			log("A:"+lmAmt1);
			log("B:"+lmAmt2);
			if(lmAmt1<lmAmt2){
				errmsg("沖回已超過繳款金額");
				wp.colSet(ii,"a-ok_flag", "X");
				return -1;
			}						
		}
		
		if(llOk<=0){
			errmsg("未點選 [確定]修改資料");
			return -1;
		}
		
		return 1;
	}

	private String dc2tw(String amDcAmt, String currCode, String imDcAmt, String imTwAmt) throws Exception {
		String strrtn = "";
		Double lamDcAmt = 0D;
		Double limDcAmt = 0D;
		Double limTwAmt = 0D;
		Double ldRate = 0D;
		Double ldAmt = 0D;

		if (currCode.equals("901"))
			return amDcAmt;

		try {
			lamDcAmt = empty(amDcAmt) ? 0D : Double.parseDouble(amDcAmt);
			limDcAmt = empty(imDcAmt) ? 0D : Double.parseDouble(imDcAmt);
			limTwAmt = empty(imTwAmt) ? 0D : Double.parseDouble(imTwAmt);
		} catch (Exception ex) {
			return amDcAmt;
		}

		if (limDcAmt != 0 && limTwAmt != 0) {
			ldAmt = (lamDcAmt / limDcAmt) * limTwAmt;
			return String.valueOf(Math.round(ldAmt));
		}

		lsSql = "select exchange_rate from ptr_curr_rate where curr_code = :curr_code";
		setString("curr_code", currCode);

		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			try {
				ldRate = Double.parseDouble(sqlStr("exchange_rate"));
			} catch (Exception ex) {
				ldRate = 1.0;
			}

			ldAmt = lamDcAmt * ldRate;
			strrtn = String.valueOf(Math.round(ldAmt));
		}

		return strrtn;
	}
	
	public void goTab2Exec() throws Exception   {
		int llOk =0 , llErr = 0, lmAmtTw = 0;
		String lsDeptNo = "" , lsGlCode = "";
		double lmDcAftAmt = 0, lmAmt = 0, llDc2twAmt = 0, lmBegDal = 0, lmBegDalTw = 0;
		
		String[] lsRowidB = wp.itemBuff("b-rowid");				
		String[] bbOpt = wp.itemBuff("b-opt");
		String[] lsReferenceNo = wp.itemBuff("b-reference_no");
		String[] lsAcajRowid = wp.itemBuff("b-acajrowid");
		String[] lsCrAmt = wp.itemBuff("b-adramt");
		String[] lsPostDate = wp.itemBuff("b-post_date");
		String[] lsAcctCode = wp.itemBuff("b-acct_code");
		String[] lsBegBal = wp.itemBuff("b-beg_bal"); //--orginal_amt
		String[] lsBegBalTw = wp.itemBuff("b-beg_bal_tw"); //--orginal_amt_tw
		String[] lsEndBal = wp.itemBuff("b-end_bal"); //--aft_amt
		String[] lsDAvailBal = wp.itemBuff("b-d_avail_bal");	//--aft_d_amt
		String[] lsCurrCode = wp.itemBuff("b-curr_code");
		String[] lsInterestDate = wp.itemBuff("b-interest_date");
		wp.listCount[0] = wp.itemRows("a-rowid");
		wp.listCount[1] = wp.itemRows("b-rowid");
		wp.listCount[2] = wp.itemRows("c-rowid");
		
		Actm0137Func func = new Actm0137Func(wp);
		
		if(ofValidationB()!=1)	return ;
		if(wfValidationB()!=1)	return ;
		
		String lsBusinessDate = "";
		
		String sql_businday = " select "
						+ " business_date "
						+ " from ptr_businday "
						+ " where 1=1 "
						;
		
		sqlSelect(sql_businday);
		if(sqlRowNum<=0){			
			errmsg("無法取得 [系統營業日]!");
			return ;
		}
		
		lsBusinessDate = sqlStr("business_date");


		String sql1 = " select "
						+ " A.usr_deptno , "
						+ " B.gl_code "
						+ " from sec_user A , ptr_dept_code B "
						+ " where B.dept_code = A.usr_deptno  "
						+ " and A.usr_id = ? "
						;
		
		sqlSelect(sql1,new Object[]{wp.loginUser});
		
		if(sqlRowNum<=0){
			errmsg("無法取得 [使用者部門代碼], [起帳部門代碼]!");
			return ;
		}
		
		if(empty(sqlStr("usr_deptno")) || empty(sqlStr("gl_code"))){
			errmsg("無法取得 [使用者部門代碼], [起帳部門代碼]!");
			return ;
		}
		
	//ls_dept_no = sql_ss("usr_deptno").substring(0, 2);
    lsDeptNo = commString.mid(sqlStr("usr_deptno"), 0,2);
		lsGlCode = sqlStr("gl_code");
		
		for(int ii=0;ii<wp.itemRows("b-rowid");ii++) {
			
			if(!empty(lsAcajRowid[ii])){
				func.varsSet("acaj_rowid", lsAcajRowid[ii]);
				if(func.deleteAcaj()!=1){
					wp.colSet(ii,"b-ok_flag", "X");
					dbRollback();
					llErr++;
					continue;
				}
			}

			if ( !empty(lsAcajRowid[ii]) && checkBoxOptOn(ii, bbOpt)==false )    { 
			   wp.colSet(ii,"b-ok_flag", "V");
				 llOk++;
				 wp.colSet(ii, "b-acajrowid", "");
			}

			if(checkBoxOptOn(ii, bbOpt)==false)	continue;
			
			if(commString.strToNum(lsCrAmt[ii])==0){
			  wp.colSet(ii,"b-ok_flag", "V");
				wp.colSet(ii, "b-acajrowid", "");
				sqlCommit(1);
				llOk++;
				continue;
			}
			
			lmDcAftAmt =commString.strToNum(lsCrAmt[ii])+commString.strToNum(lsEndBal[ii]);
			
			func.varsSet("p_seqno",wp.itemStr2("p_seqno"));
			func.varsSet("acct_type",wp.itemStr2("ex_acct_type"));
			func.varsSet("reference_no",lsReferenceNo[ii]);
			func.varsSet("post_date",lsPostDate[ii]);
			func.varsSet("acct_code",lsAcctCode[ii]);
			func.varsSet("job_code",lsDeptNo);
			func.varsSet("vouch_job_code","0"+lsGlCode);
		//func.varsSet("debit_item",wp.itemStr2("ex_cr_item"));
		  func.varsSet("debit_item","");
			func.varsSet("adj_reason_code",wp.itemStr2("ex_reason_code"));
			func.varsSet("adj_comment",wp.itemStr2("ex_comment"));
		//func.varsSet("c_debt_key",wp.itemStr2("ex_c_debt_key"));
		  func.varsSet("c_debt_key","");
		//func.vars_set("value_type",wp.item_ss("ex_onflag"));
			func.varsSet("dc_orginal_amt",lsBegBal[ii]);
			func.varsSet("dc_cr_amt",lsCrAmt[ii]);
			func.varsSet("dc_bef_amt",lsEndBal[ii]);
			func.varsSet("dc_aft_amt",""+lmDcAftAmt);
			func.varsSet("dc_bef_d_amt",lsDAvailBal[ii]);
			func.varsSet("dc_aft_d_amt",lsDAvailBal[ii]);
			func.varsSet("curr_code",lsCurrCode[ii]);
			
		  if(!wp.itemEq("ex_onflag", "Y")){
				func.varsSet("interest_date",lsInterestDate[ii]);
		    func.varsSet("value_type","1");
			}	else	{
			//func.varsSet("interest_date",getSysDate());
				func.varsSet("interest_date",lsBusinessDate);
		    func.varsSet("value_type","2");
			}			

      lmBegDal = commString.strToNum(lsBegBal[ii]);
      lmBegDalTw = commString.strToNum(lsBegBalTw[ii]);
		//func.vars_set("orginal_amt",ls_beg_bal[ii]);
			func.varsSet("orginal_amt",lsBegBalTw[ii]);

		//func.vars_set("cr_amt",ls_cr_amt[ii]);
		  llDc2twAmt = commCurr.dc2twAmt( lmBegDalTw,lmBegDal,commString.strToNum(lsCrAmt[ii]) );
			func.varsSet("cr_amt",""+llDc2twAmt);

		//func.vars_set("bef_amt",ls_end_bal[ii]);
		  llDc2twAmt = commCurr.dc2twAmt( lmBegDalTw,lmBegDal,commString.strToNum(lsEndBal[ii]) );
			func.varsSet("bef_amt",""+llDc2twAmt);

		//func.vars_set("aft_amt",""+lm_dc_aft_amt);
		  llDc2twAmt = commCurr.dc2twAmt( lmBegDalTw,lmBegDal,lmDcAftAmt );
			func.varsSet("aft_amt",""+llDc2twAmt);

		//func.vars_set("bef_d_amt",ls_d_avail_bal[ii]);
		  llDc2twAmt = commCurr.dc2twAmt( lmBegDalTw,lmBegDal,commString.strToNum(lsDAvailBal[ii]) );
			func.varsSet("bef_d_amt",""+llDc2twAmt);

		//func.vars_set("aft_d_amt",ls_d_avail_bal[ii]);
		  func.varsSet("aft_d_amt",""+llDc2twAmt);
			
			if(func.insertActAcaj()==1) { 
			  wp.colSet(ii,"b-ok_flag", "V");
				llOk++;
				sqlCommit(1);
		    String refno = "";
 		  	refno = wp.colStr(ii, "b-reference_no");
	  		lsSql = "  SELECT hex(act_acaj.rowid) as rowid,   " 
	  		       + "         act_acaj.dc_cr_amt,             " + "   act_acaj.cr_amt      " 
					     + "    FROM act_acaj                     " + "   where act_acaj.reference_no = :refno  "
					     + "     and adjust_type='DR11' and function_code='U' ";					 

			  setString("refno", refno);
			  sqlSelect(lsSql);
			  if (sqlRowNum > 0) {
				   wp.colSet(ii, "b-acajrowid", sqlStr("rowid"));
	    		 wp.colSet(ii, "b-adramt", sqlStr("dc_cr_amt"));
	    	//lm_amt_tw += sql_num("cr_amt");
	    		 lmAmt += sqlNum("dc_cr_amt");
		 	  }
				continue;
			}	else	{
				wp.colSet(ii,"b-ok_flag", "X");
				llErr++;
				dbRollback();
				continue;
			}
			
		}

		wp.colSet("click_total_B", lmAmt);

	//queryReadB();
	
		alertMsg("執行完成 , 成功:"+llOk+" 失敗:"+llErr);
		
	}
	
	int ofValidationB() throws Exception{
/***
		String sql1 = " select "
						+ " ac_no "
						+ " from gen_acct_m "
						+ " where ac_no = ? "
						;
		
		sqlSelect(sql1,new Object[]{wp.itemStr2("ex_cr_item")});
		
		if(sqlRowNum<=0){
			errmsg("調整錯誤:貸方科目");
			return -1;
		}
		
		if(wp.itemEq("ex_cr_item", "14817000") && wp.itemEmpty("ex_c_debt_key")){
			errmsg("調整錯誤:貸方科目14817000,一定要有銷帳鍵值");
			return -1;
		}
		
		if(eqIgno(commString.mid(wp.itemStr2("ex_cr_item"), 0,4),"1751") && wp.itemEmpty("ex_c_debt_key")){
			errmsg("調整錯誤:貸方科目1751,一定要有銷帳鍵值");
			return -1;
		}
		
		if(eqIgno(commString.mid(wp.itemStr2("ex_cr_item"), 0,4),"1751") && wp.itemStr2("ex_c_debt_key").length()!=20){
			errmsg("調整錯誤:貸方科目1751,銷帳鍵值須為20碼");
			return -1;
		}
***/
		return 1;
	}
	
	int wfValidationB(){		
		double lmCrAmt =0, lmWkAftamt =0, lmOrgAmt =0, lmAftAmt =0 , llErr=0, llApr=0;
		String error1 = "" , error2 = "",lsItemEname = "";
		
		String[] lsCrAmt = wp.itemBuff("b-adramt");
		String[] lsWkAftamt = wp.itemBuff("b-dramt");
		String[] lsOrgAmt = wp.itemBuff("b-beg_bal");
		String[] lsAftAmt = wp.itemBuff("b-end_bal");
		String[] lsAcctCode = wp.itemBuff("b-acct_code");
		String[] lsAprFlag = wp.itemBuff("b-apr_flag");
		String[] bbOpt = wp.itemBuff("b-opt");
		String[] lsCurrCode = wp.itemBuff("b-curr_code");
	
		for(int ii=0;ii<wp.itemRows("b-rowid");ii++) { 
			if ( eqIgno(lsAprFlag[ii],"Y") ) 	{
					llApr++;
					break;
			}
		}
			

		for(int ii=0;ii<wp.itemRows("b-rowid");ii++) {
		//if(checkBox_opt_on(ii, bb_opt)==false)	continue;
			
			log("A:"+lsAprFlag[ii]);
			
			if ( (llApr > 0) && eqIgno(lsAprFlag[ii],"Y") && (checkBoxOptOn(ii, bbOpt)==false) ) {
  		 	wp.colSet(ii,"b-ok_flag", "X");
				error2 = "Y";
				llErr++;
				continue;
			}

			if ( (llApr > 0) && (!eqIgno(lsAprFlag[ii],"Y")) && (checkBoxOptOn(ii, bbOpt)==true) ) {
  		 	wp.colSet(ii,"b-ok_flag", "X");
				error2 = "Y";
				llErr++;
				continue;
			}

		  if(checkBoxOptOn(ii, bbOpt)==false)	continue;

			lmCrAmt = commString.strToNum(lsCrAmt[ii]);
			lmWkAftamt = commString.strToNum(lsWkAftamt[ii]);
			lmOrgAmt = commString.strToNum(lsOrgAmt[ii]);
			lmAftAmt = commString.strToNum(lsAftAmt[ii]);
			lsItemEname = lsAcctCode[ii];
			
			if(eqIgno(lsCurrCode[ii],"901")){
		   	if(lmCrAmt%1 >0){
		  	  wp.colSet(ii,"b-ok_flag", "X");
				  errmsg("Reversal金額 不可有小數");
				  return -1;
			  }	
			}
			
			if(eqIgno(lsItemEname,"RI") ||eqIgno(lsItemEname,"PN")){
				if(lmCrAmt<0 || (lmCrAmt + lmAftAmt)>lmOrgAmt){
		  		wp.colSet(ii,"b-ok_flag", "X");
					error1 = "Y";
					llErr++;
				}
			}	else	{
				if((lmCrAmt < 0) || (lmCrAmt > lmWkAftamt)){
  		  	wp.colSet(ii,"b-ok_flag", "X");
					error1 = "Y";
	  		  llErr++;
				}
			}
			
		}
		
		if(llErr==0)	return 1;
		
		if(eqIgno(error1,"Y")){
			errmsg("reversal 金額不能大於尚可 payment reversal 金額");			
		}
		
		if(eqIgno(error2,"Y")){
			errmsg("已有一筆以上覆核不可再調整");
		} 
		
		return -1;
	}
	
	public void goTab3Exec() throws Exception {
		int llOk =0 , llErr = 0;
		double lmAmt = 0;
		String lsDeptNo = "" , lsGlCode = "" , lsPSeqno = "";
		
		String[] lsAprFlag = wp.itemBuff("db_apr_flag");
		String[] ccOpt = wp.itemBuff("c-opt");
		String[] lsJrnlSeqno = wp.itemBuff("c-jrnl_seqno");
		String[] lsAdramt = wp.itemBuff("c-adramt");
		String[] lsEnqSeqno = wp.itemBuff("c-enq_seqno");
		String[] lsAcctDate = wp.itemBuff("c-acct_date");
		String[] lsTranType = wp.itemBuff("c-tran_type");
		String[] lsCurrCode = wp.itemBuff("c-curr_code");
		String[] lsTransactionAmt = wp.itemBuff("c-transaction_amt");
		String[] lsDcTransactionAmt = wp.itemBuff("c-dc_transaction_amt");
		String[] lsPaymentRevAmt = wp.itemBuff("c-payment_rev_amt");
		String[] lsInterestDate = wp.itemBuff("c-interest_date");
		wp.listCount[0] = wp.itemRows("a-rowid");
		wp.listCount[1] = wp.itemRows("b-rowid");
		wp.listCount[2] = wp.itemRows("c-rowid");
		
		Actm0137Func func = new Actm0137Func(wp);
		
		if(ofcValidationC()!=1)	return ;
		if(wfValidationC()!=1)	return ;
		
		String lsBusinessDate = "";
		
		String sql_businday = " select "
						+ " business_date "
						+ " from ptr_businday "
						+ " where 1=1 "
						;
		
		sqlSelect(sql_businday);
		if(sqlRowNum<=0){			
			errmsg("無法取得 [系統營業日]!");
			return ;
		}
		
		lsBusinessDate = sqlStr("business_date");

		String sql1 = " select "
						+ " A.usr_deptno , "
						+ " B.gl_code "
						+ " from sec_user A , ptr_dept_code B "
						+ " where B.dept_code = A.usr_deptno  "
						+ " and A.usr_id = ? "
						;
		
		sqlSelect(sql1,new Object[]{wp.loginUser});
		
		if(sqlRowNum<=0){
			errmsg("無法取得 [使用者部門代碼], [起帳部門代碼]!");
			return ;
		}
		
		if(empty(sqlStr("usr_deptno")) || empty(sqlStr("gl_code"))){
			errmsg("無法取得 [使用者部門代碼], [起帳部門代碼]!");
			return ;
		}
		
	//ls_dept_no = sql_ss("usr_deptno").substring(0, 2);
    lsDeptNo = commString.mid(sqlStr("usr_deptno"), 0,2);
		lsGlCode = sqlStr("gl_code");
		lsPSeqno = wp.itemStr2("p_seqno");
		
		String sql2 = " select "
						+ " count(*) as db_cnt "
						+ " from act_pay_rev "
						+ " where p_seqno = ? "
						+ " and jrnl_seqno <> ? "
					  + " and proc_mark not in ('1','2') "			
						;
		
		for(int ii=0;ii<wp.itemRows("c-rowid");ii++){

			if(checkBoxOptOn(ii, ccOpt)==false)	continue;
			
			sqlSelect(sql2,new Object[]{lsPSeqno,lsJrnlSeqno[ii]});
			if(sqlNum("db_cnt")>0 && commString.strToNum(lsAdramt[ii])>0 ) {
				errmsg("此帳號已沖回！ ");
				llErr++;
				wp.colSet(ii,"c-ok_flag", "X");
				return ;
			}
			
			if(!empty(lsJrnlSeqno[ii])) {
				func.varsSet("jrnl_seqno", lsJrnlSeqno[ii]);
				if(func.deletePayrev()!=1){
					wp.colSet(ii,"c-ok_flag", "X");
					dbRollback();
					continue;
				}
			}
			
			//--金額為0 不insert
		//if(zzstr.ss_2Num(ls_adramt[ii])==0)	continue;
			if(commString.strToNum(lsAdramt[ii])==0)	 {
			  wp.colSet(ii,"c-ok_flag", "V");
			  llOk++;
			  continue;
			} 
			
			func.varsSet("jrnl_seqno",lsJrnlSeqno[ii]);
			func.varsSet("enq_seqno",lsEnqSeqno[ii]);
			func.varsSet("p_seqno",lsPSeqno);
			func.varsSet("acct_type",wp.itemStr2("ex_acct_type"));
			func.varsSet("acct_date",lsAcctDate[ii]);
			func.varsSet("tran_type",lsTranType[ii]);
			func.varsSet("curr_code",lsCurrCode[ii]);
			func.varsSet("transaction_amt",lsTransactionAmt[ii]);
			func.varsSet("dc_transaction_amt",lsDcTransactionAmt[ii]);
			func.varsSet("payment_rev_amt",lsPaymentRevAmt[ii]);
			func.varsSet("this_reversal_amt",lsAdramt[ii]);
			if(!wp.itemEq("ex_onflagc", "Y")){
				func.varsSet("interest_date",lsInterestDate[ii]);
		    func.varsSet("value_type","1");
			}	else	{
			//func.varsSet("interest_date",getSysDate());
				func.varsSet("interest_date",lsBusinessDate);
		    func.varsSet("value_type","2");
			}			
			func.varsSet("adj_reason_code",wp.itemStr2("ex_reason_code2"));
			func.varsSet("adj_comment",wp.itemStr2("ex_comment2"));
		//func.varsSet("c_debt_key",wp.itemStr2("ex_c_debt_key2"));
		  func.varsSet("c_debt_key","");
		//func.varsSet("cr_item",wp.itemStr2("ex_cr_item2"));
		  func.varsSet("cr_item","");
			func.varsSet("job_code",lsDeptNo);
			func.varsSet("vouch_job_code","0"+lsGlCode);
		//func.vars_set("value_type",wp.item_ss("ex_onflagc"));
			
			if(func.insertPayrev()==1){
				llOk++;
				sqlCommit(1);
			  wp.colSet(ii,"c-ok_flag", "V");
	    	lmAmt += commString.strToNum(lsAdramt[ii]);
				continue;
			}	else	{
				llErr++;
				dbRollback();
				wp.colSet(ii,"c-ok_flag", "X");
				continue;
			}
		}

		wp.colSet("click_total_C", lmAmt);
		alertMsg("執行完成 , 成功:"+llOk+" 失敗:"+llErr);
	}
	
	int ofcValidationC() throws Exception{
/***
		String sql1 = " select "
						+ " ac_no "
						+ " from gen_acct_m "
						+ " where ac_no = ? "
						;
		
		sqlSelect(sql1,new Object[]{wp.itemStr2("ex_cr_item2")});
		
		if(sqlRowNum<=0){
			errmsg("調整錯誤:貸方科目");
			return -1;
		}
		
		if(wp.itemEq("ex_cr_item2", "14817000") && wp.itemEmpty("ex_c_debt_key2")){
			errmsg("調整錯誤:貸方科目14817000,一定要有銷帳鍵值");
			return -1;
		}
		
		if(eqIgno(commString.mid(wp.itemStr2("ex_cr_item2"), 0,4),"1751") && wp.itemEmpty("ex_c_debt_key2")){
			errmsg("調整錯誤:貸方科目1751,一定要有銷帳鍵值");
			return -1;
		}
		
		if(eqIgno(commString.mid(wp.itemStr2("ex_cr_item2"), 0,4),"1751") && wp.itemStr2("ex_c_debt_key2").length()!=20){
			errmsg("調整錯誤:貸方科目1751,銷帳鍵值須為20碼");
			return -1;
		}
***/		
		return 1;			
	}
	
	int wfValidationC(){
		int llCnt = 0, llFirstCnt = 0 ; double lmAdramt = 0 , lmDramt = 0;
		String error1 = "";
		String[] lsAdramt = wp.itemBuff("c-adramt");
		String[] lsDramt = wp.itemBuff("c-dramt");
		String[] ccOpt = wp.itemBuff("c-opt");
		String[] lsCurrCode = wp.itemBuff("c-curr_code");

		for(int ii=0;ii<wp.itemRows("c-rowid");ii++){
			if(checkBoxOptOn(ii, ccOpt)==false) {
		  	wp.colSet(ii,"c-adramt", "");
  			continue;
			}	
	
			//ll_cnt++;
			if(commString.strToNum(lsAdramt[ii]) > 0 )  {
		    llCnt++;
			}
		
			if (llCnt == 1 )  {
         llFirstCnt = ii;
      }
      
			//if(ll_cnt >1 )  {
		  //wp.col_set(ii,"c-ok_flag", "X");
			//	errmsg("同一帳號只可執行一筆沖回！");
			//	return -1;
			//}
			if(llCnt >1 && commString.strToNum(lsAdramt[ii]) > 0 )  {
		    wp.colSet(llFirstCnt,"c-ok_flag", "X");
		    wp.colSet(ii,"c-ok_flag", "X");
				error1 = "Y";
				continue ;
			}
			
			lmAdramt = commString.strToNum(lsAdramt[ii]);
			
			if ( lmAdramt%1!=0 && !eqIgno(lsCurrCode[ii],"840") )  {
		  	wp.colSet(ii,"c-ok_flag", "X");
				errmsg("繳款沖回金額  不可有小數");
				return -1;
			}
			
			lmDramt = commString.strToNum(lsDramt[ii]);
			
			if(lmAdramt > lmDramt){
		  	wp.colSet(ii,"c-ok_flag", "X");
				errmsg("reversal 金額不能大於尚可 payment reversal 金額");
				return -1;
			}
			
		}
		if(eqIgno(error1,"Y")){
			errmsg("同一帳號只可執行一筆沖回！");			
			return -1;
		}
		
		return 1;
	}
	
	@Override
	public void initButton() {
	//if (wp.respHtml.indexOf("_detl") > 0) {
	//	this.btnMode_aud();
	//}

    if (wp.respHtml.equals("actm0137"))  {
       wp.colSet("btnAdd_disable","");
       this.btnModeAud();
    }

	}

  @Override
  public void initPage() {
    tabClick();
  }

  void tabClick() {
    wp.colSet("act_click_a", "t_click_1");
    wp.colSet("act_click_b", "t_click_2");
    wp.colSet("act_click_c", "t_click_3");

    String lsClick = "";
    lsClick = wp.itemStr2("tab_click");
    if (eqIgno(lsClick, "b")) {
       wp.colSet("act_click_b", "tab_active");
    } else if (eqIgno(lsClick, "c")) {
       wp.colSet("act_click_c", "tab_active");
    } else if (eqIgno(lsClick, "a")) {
       wp.colSet("act_click_a", "tab_active");
    } else {
       wp.colSet("tab_click", "b");
       wp.colSet("act_click_b", "tab_active");
    }
  }

	@Override
	public void dddwSelect() {
		try {
			// wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_acct_type");
			dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_curr_code");
			dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	public String getInfo() throws Exception {
		String lsSql = "";
		String idPSeqno = "";
		String corpPSeqno = "";
		String pSeqno = "";
		String accttyp = "", acctkey = "";
		accttyp = wp.itemStr2("ex_acct_type");
//		acctkey = wp.item_ss("ex_acct_key");
		acctkey = fillZeroAcctKey(wp.itemStr2("ex_acct_key"));

		if (empty(acctkey) || empty(accttyp)) {
			return "";
		}

		if (!(empty(acctkey) && empty(accttyp))) {
			lsSql = "";
			lsSql += " select p_seqno, ACCT_KEY, ACCT_TYPE, id_p_seqno, corp_p_seqno from	act_acno "
					+ " where P_SEQNO = ACNO_P_SEQNO  and acct_type = :accttype and acct_key = :acctkey ";
			setString("accttype", accttyp);
			setString("acctkey", acctkey);
			sqlSelect(lsSql);

			if (sqlRowNum > 0) {
				pSeqno = sqlStr("p_seqno");
				idPSeqno = sqlStr("id_p_seqno");
				corpPSeqno = sqlStr("corp_p_seqno");
				wp.colSet("p_seqno", pSeqno);

				if (!empty(idPSeqno)) {
					lsSql = "select chi_name from crd_idno where id_p_seqno = :id_p_seqno";
					setString("id_p_seqno", idPSeqno);
					sqlSelect(lsSql);
					if (sqlRowNum > 0) {
						wp.colSet("ex_cname", sqlStr("chi_name"));
					}
				} if (!empty(corpPSeqno)) {
					lsSql = "select chi_name from crd_corp where corp_p_seqno = :corp_p_seqno";
					setString("corp_p_seqno", corpPSeqno);
					sqlSelect(lsSql);
					if (sqlRowNum > 0) {
						wp.colSet("ex_cname", sqlStr("chi_name"));
					}
				}

				lsSql = "select last_min_pay_date from act_acct where p_seqno =:p_seqno";
				setString("p_seqno", pSeqno);
				sqlSelect(lsSql);
				if (sqlRowNum > 0) {
					wp.colSet("ex_last_min_pay_date", sqlStr("last_min_pay_date"));
					wp.colSet("ex_last_min_pay_date2", sqlStr("last_min_pay_date"));
				}

				return pSeqno;
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
	
  /*** conv_amt_dp2r(x) 有以下兩點作用：
   1.校正微小誤差：double 變數運算後會發生 .99999999...的問題，例如 19.125, 
     實際會變成 19.1249999999999999...，所以執行 conv_amt_dp2r(x)變成 19.13
   2.四捨五入到小數以下第二位
  ***/
  public double  convAmtDp2r(double cvtAmt) throws Exception
  {
   long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.00001);
   double cvtDouble =  ((double) cvtLong) / 100;
   return cvtDouble;
  }

	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}
	
	void keepOpt(){
		//--opt_on_A,opt_on_B,opt_on_C
		String[] aOpt = wp.itemBuff("a-opt");
		String[] bOpt = wp.itemBuff("b-opt");
		String[] cOpt = wp.itemBuff("c-opt");
		
		for(int ii=0;ii<wp.itemRows("a-rowid");ii++){
			if(checkBoxOptOn(ii, aOpt))	wp.colSet(ii,"opt_on_A", "checked");			
		}
		
		for(int ii=0;ii<wp.itemRows("b-reference_no");ii++){
			if(checkBoxOptOn(ii, bOpt))	wp.colSet(ii,"opt_on_B", "checked");			
		}
		
		for(int ii=0;ii<wp.itemRows("c-rowid");ii++){
			if(checkBoxOptOn(ii, cOpt))	wp.colSet(ii,"opt_on_C", "checked");			
		}
		
	}
	
	
}
