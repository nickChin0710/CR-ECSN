package rskr01;
/**
 * 2020-0320	JH		rsk_type=Y
 * 2020-0318	JH		ex_type
 * 2019-1202:  Alex  dest_amt -> dc_dest_amt , prb_amount -> dc_prb_amount
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskr0040 extends BaseQuery implements InfacePdf {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
   // wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      // is_action="new";
      // clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) { // -資料讀取-
      strAction = "R";
      // dataRead();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }
//	else if (eq_igno(wp.buttonCode, "XLS")) { // -Excel-
//		is_action = "XLS";
//		// xlsPrint();
//	}
   else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskr0040")) {
         wp.optionKey = wp.colStr(0, "ex_curr_code");
         dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("印表期間起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1 and prb_status >='30' and prb_status <'80'  "
         + sqlCol(wp.itemStr("ex_date1"), "add_apr_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "add_apr_date", "<=")
         + sqlCol(wp.itemStr("ex_add_user"), "add_user", "like%")
         + sqlCol(wp.itemStr("ex_curr_code"), "curr_code");
// 問題交易 Q, 特殊交易 S, 不合格帳單 E
   if (wp.itemEq("ex_type", "1")) {
      //1.人工列
      lsWhere += " and prb_src_code = 'RQ' ";
   }
   else if (wp.itemEq("ex_type", "2")) {
      //2.系統列
      lsWhere += " and prb_src_code = 'SQ' ";
   }
   else if (wp.itemEq("ex_type", "3")) {
      //3.不合格
      lsWhere += " and prb_src_code ='SE' and  prb_mark='E' ";
   }
   else if (wp.itemEq("ex_type", "4")) {
      //4.特殊帳單
      lsWhere += " and prb_src_code ='SS' and  prb_mark='S' ";
   }

   if (wp.itemEq("ex_card_flag", "1")) {
      lsWhere += " and debit_flag <> 'Y' and bill_type <> 'TSCC' ";
   }
   else if (wp.itemEq("ex_card_flag", "2")) {
      lsWhere += " and debit_flag = 'Y' and bill_type <> 'TSCC' ";
   }
   else if (wp.itemEq("ex_card_flag", "3")) {
      lsWhere += " and bill_type = 'TSCC' ";
   }

   /*
    * sum_NU_read(lsWhere); sum_U_read(lsWhere); sum_read(lsWhere);
    */

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {

   wp.pageControl();

   wp.selectSQL = " card_no ,"
         //+ " uf_hi_cardno(card_no) as hh_card_no , "
         + " replace(uf_idno_name2(card_no,debit_flag),'　','') as db_idno_name , "
         + " replace(uf_hi_cname(uf_idno_name2(card_no,debit_flag)),'　','') as hh_idno_name , "
         + " acct_type , "
         + " uf_acno_key2(p_seqno,debit_flag) as acct_key , "
//         + " acct_type||'-'||uf_acno_key2(p_seqno,debit_flag) as hh_acct_key , "
         + " ctrl_seqno,"
         + " purchase_date,"
         + " txn_code,"
         + " dest_amt,"
         + " dc_dest_amt,"
         + " add_apr_date,"
         + " uf_tt_idtab('PRBQ_REASON_CODE',prb_reason_code) as tt_prb_reason_code,"
         + " '' as process_desc,"
         + " prb_mark,"
         + " prb_status,"
         + " bin_type,"
         + " sign_flag ,"
         + " reference_no,"
         + " reference_seq,"
         + " prb_amount,"
         + " dc_prb_amount ,"
         + " rsk_type,"
         + " bill_type,"
         + " add_user,"
         + " auth_code, "
         + " decode(bill_type,'TSCC',1,0) as db_tscc_cnt, "
         + " decode(bill_type,'TSCC',0,1) as db_card_cnt,"
         + " decode(bill_type,'TSCC',dc_dest_amt,0) as db_tscc_destamt, "
         + " decode(bill_type,'TSCC',0,dc_dest_amt) as db_card_destamt,"
         + " decode(bill_type,'TSCC',dc_prb_amount,0) as db_tscc_prbamount, "
         + " decode(bill_type,'TSCC',0,dc_prb_amount) as db_card_prbamount, "
         + " uf_dc_curr(curr_code) as curr_code , "
         + " prb_reason_code "
         + ", (select 1 from crd_card B where B.card_no=rsk_problem.card_no union select 1 from dbc_card C where C.card_no=rsk_problem.card_no ) as xx_no_mega "
   ;
   wp.daoTable = "rsk_problem";
   wp.whereOrder = " order by sign_flag ,acct_type,add_apr_date,ctrl_seqno ";
   pageQuery();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
   queryReadAfter();
}

void queryReadAfter() throws Exception {
   Rskr0040Func loFunc = new Rskr0040Func();
   loFunc.setConn(wp.getConn());

   int ll_nrow = wp.listCount[0];
   for (int ii = 0; ii < ll_nrow; ii++) {
      wp.colSet(ii, "wk_acct_type_key", wp.colStr(ii, "acct_type") + "-"
            + wp.colStr(ii, "acct_key"));
      int liMega = wp.colInt(ii, "xx_no_mega");
      if (liMega <= 0) {
         wp.colSet(ii, "rsk_type", "Y");
      }

      if (!eqIgno(strAction, "PDF")) {
         String ls_refno = wp.colStr(ii, "reference_no");
         String ss = loFunc.rskLastStatus(ls_refno);
         wp.colSet(ii, "process_desc", ss);
      }	else	{
      	wp.colSet(ii, "wk_acct_type_key", wp.colStr(ii, "acct_type") + "-"
               + commString.hideIdno(wp.colStr(ii, "acct_key")));
      	wp.colSet(ii, "card_no",commString.hideCardNo(wp.colStr(ii,"card_no")));
      }
   }
}

/*------sign_flag='+'	
	void sum_NU0_read(String lsWhere) throws Exception {
		String NU0_where = lsWhere +" and bill_type <> 'TSCC' and sign_flag='+' ";
		wp.selectSQL = ""
			+ " count(*) as wk_Nu0,"
			+ " sum(dest_amt) as wk_Nd0,"
			+ " sum(prb_amount) as wk_Np0"
			;
		wp.daoTable = "rsk_problem";
		wp.whereStr =NU0_where;
		wp.whereOrder = " group by acct_type order by acct_type ";
		pageSelect();
	}
	
	void sum_U0_read(String lsWhere) throws Exception {
		String U0_where = lsWhere +" and bill_type ='TSCC' and sign_flag='+' ";
		wp.selectSQL = ""
			+ " count(*) as wk_U0,"
			+ " sum(dest_amt) as wk_Ud0,"
			+ " sum(prb_amount) as wk_Up0"
			;
		wp.daoTable = "rsk_problem";
		wp.whereStr =U0_where;
		wp.whereOrder = " group by acct_type order by acct_type ";
		pageSelect();
	}
	
	void sum_0_read(String lsWhere) throws Exception {
		String A0_where = lsWhere +" and sign_flag='+' ";
		wp.selectSQL = ""
			+ " count(*) as wk_A0,"
			+ " sum(dest_amt) as wk_Ad0,"
			+ " sum(prb_amount) as wk_Ap0"
			;
		wp.daoTable = "rsk_problem";
		wp.whereStr =A0_where;		
		pageSelect();
	}
//------sign_flag='-'	
	void sum_NU1_read(String lsWhere) throws Exception {
		String NU1_where = lsWhere +" and bill_type <> 'TSCC' and sign_flag='-' ";
		wp.selectSQL = ""
			+ " count(*) as wk_Nu1,"
			+ " sum(dest_amt) as wk_Nd1,"
			+ " sum(prb_amount) as wk_Np1"
			;
		wp.daoTable = "rsk_problem";
		wp.whereStr =NU1_where;
		wp.whereOrder = " group by acct_type order by acct_type ";
		pageSelect();
	}
	
	void sum_U1_read(String lsWhere) throws Exception {
		String U1_where = lsWhere +" and bill_type ='TSCC' and sign_flag='-' ";
		wp.selectSQL = ""
			+ " count(*) as wk_U1,"
			+ " sum(dest_amt) as wk_Ud1,"
			+ " sum(prb_amount) as wk_Up1"
			;
		wp.daoTable = "rsk_problem";
		wp.whereStr =U1_where;
		wp.whereOrder = " group by acct_type order by acct_type ";
		pageSelect();
	}
	
	void sum_1_read(String lsWhere) throws Exception {
		String A1_where = lsWhere +" and sign_flag='-' ";
		wp.selectSQL = ""
			+ " count(*) as wk_A1,"
			+ " sum(dest_amt) as wk_Ad1,"
			+ " sum(prb_amount) as wk_Ap1"
			;
		wp.daoTable = "rsk_problem";
		wp.whereStr =A1_where;		
		pageSelect();
	}
	//------total	
	void sum_read(String lsWhere) throws Exception {
		wp.selectSQL = ""
			+ " count(*) as wk_A,"
			+ " sum(dest_amt) as wk_Ad,"
			+ " sum(prb_amount) as wk_Ap"
			;
		wp.daoTable = "rsk_problem";
		wp.whereStr =lsWhere;		
		pageSelect();
	}
*/
@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "rskr0040";
   String ss = "";
   if (wp.itemEq("ex_card_flag", "0")) {
      ss = "交易卡別 : 全部 ";
   }
   else if (wp.itemEq("ex_card_flag", "1")) {
      ss = "交易卡別 : 信用卡 ";
   }
   else if (wp.itemEq("ex_card_flag", "2")) {
      ss = "交易卡別 : VD 卡 ";
   }
   else if (wp.itemEq("ex_card_flag", "3")) {
      ss = "交易卡別 : 悠遊卡 ";
   }

   String lsType = wp.itemStr("ex_type");
   ss += " 印表期間:"
         + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
         + commString.strToYmd(wp.itemStr("ex_date2")) + " 新增登錄者:"
         + wp.itemStr("ex_id1") +
         "  交易類別: " + commString.decode(lsType, "0,1,2,3,4"
         , "0.全部,1.人工列,2.系統列,3.不合格,4.特殊帳單");

   wp.colSet("cond1", ss);
   wp.colSet("user_id", wp.loginUser);
   wp.pageRows = 9999;
   queryFunc();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0040.xlsx";
   pdf.pageCount = 33;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
