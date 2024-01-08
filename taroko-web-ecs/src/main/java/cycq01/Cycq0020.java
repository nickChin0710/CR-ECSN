package cycq01;
/** 對帳單查詢
 * 2021-1102   JH    8992: queryFunc.data_clear()
 * 2021-0809   JH    act_acct_hst.stmt_cycle_date='', no run cycle
 * 2021-0719   JH    ++queryClear()
 * 2021-0419.6713    JH    彙總.調整金額
 * 2021-0317.6368    JH    消費明細.sort
 * 2021-0224.6108    JH    本期繳款金額:(20210226還原)
 * 2020-1208.4933    JH    問交結案說明
 * 2020-1116   JH    mcht_name: chi_name/eng_name
 * 2020-0730   JH    modify
 * 2020-0623   JH    bill.dc_curr_adjust_amt
 * 2020-0408:  Alex  add auth_query
 * 2020-0406   JH    QR_flag
 * 2020-0310:  ryan  update queryReadTab2() for mantis:0002951
 * 2020-0218   Alex  dataRead after 3 fix
 * 2022-1028   YangBo  sync data from mega
 */

import busi.func.ColFunc;
import ofcapp.BaseAction;

public class Cycq0020 extends BaseAction {

taroko.base.CommDate commDate = new taroko.base.CommDate();

String lsAcctKey = "", lsAcctType = "", lsAcctMonth = "", lsLastAcctMonth = "";
String kk1 = "", kk2 = "", kk3 = "";

@Override
public void userAction() throws Exception {
   switch(wp.buttonCode) {
      case "Q":
         queryFunc();
         tabClick();
         break;
      case "S3":
         querySelectS3();
         break;
      default:
         defaultAction();
   }
}

void queryClear() throws Exception {
   wp.resetOutputData();
   item2Col("ex_acct_type");
   item2Col("ex_acct_key");
   item2Col("ex_acct_month");
   item2Col("ex_card_no");
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "cycq0020")) {
         wp.optionKey = wp.colStr(0, "ex_acct_type");
         dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
   } catch (Exception ex) {
   }

}

boolean queryBefore() {


   String sql1 = " select "
         + " acno_p_seqno , p_seqno, "
         + " id_p_seqno , "
         + " corp_p_seqno , "
         + " bill_sending_zip||' '||bill_sending_addr1||bill_sending_addr2||bill_sending_addr3||bill_sending_addr4||bill_sending_addr5 as db_addr , "
         + " stmt_cycle "
         + " from act_acno "
         + " where acct_type = ? "
         + " and acct_key = ? ";

   sqlSelect(sql1, new Object[]{lsAcctType, lsAcctKey});
   if (sqlRowNum <= 0) return false;

//   is_acno_pseqno =sqlStr("acno_p_seqno");
//   is_p_seqno =sqlStr("p_seqno");
   wp.colSet("ex_acct_type", lsAcctType);
   wp.colSet("ex_acct_key", lsAcctKey);
   wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
   wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
   wp.colSet("ex_acno_p_seqno", sqlStr("acno_p_seqno"));
   wp.colSet("ex_corp_p_seqno", sqlStr("corp_p_seqno"));
   wp.colSet("ex_addr", sqlStr("db_addr"));
   wp.colSet("ex_stmt_cycle", sqlStr("stmt_cycle"));
   //--正卡姓名
   if (!empty(sqlStr("id_p_seqno"))) {
      String sql2 = " select "
            + " chi_name "
            + " from crd_idno "
            + " where id_p_seqno = ? ";
      sqlSelect(sql2, new Object[]{sqlStr("id_p_seqno")});
      if (sqlRowNum > 0) wp.colSet("ex_idno_name", sqlStr("chi_name"));
   }

   //--公司名稱
   if (!empty(sqlStr("corp_p_seqno"))) {
      String sql3 = " select "
            + " chi_name as corp_name "
            + " from crd_corp "
            + " where corp_p_seqno = ? ";
      sqlSelect(sql3, new Object[]{sqlStr("corp_p_seqno")});
      if (sqlRowNum > 0) wp.colSet("ex_corp_name", sqlStr("corp_name"));
   }

   return true;
}

@Override
public void queryFunc() throws Exception {
   wp.dataClear("ex_payment_no");
   wp.dataClear("acmm_print");
   wp.dataClear("ex_cycle_date");
   wp.dataClear("ex_stmt_last_payday");
   wp.dataClear("ex_idno_name");
   wp.dataClear("ex_corp_name");
   wp.dataClear("ex_stmt_cycle");
   wp.dataClear("ex_p_seqno");
   wp.dataClear("ex_acno_p_seqno");
   wp.dataClear("ex_id_p_seqno");
   wp.dataClear("ex_corp_p_seqno");
	
	ColFunc func =new ColFunc();
	func.setConn(wp);
	
	if(wp.itemEmpty("ex_acct_key")==false){
		if (func.fAuthQuery(wp.modPgm(), commString.mid(wp.itemStr("ex_acct_key"), 0,10))!=1) { 
      	alertErr(func.getMsg()); 
      	return ; 
      }
	}	else	if(wp.itemEmpty("ex_card_no")==false){
		if (func.fAuthQuery(wp.modPgm(), wp.itemStr("ex_card_no"))!=1) { 
      	alertErr(func.getMsg()); 
      	return ; 
      }
	}
	
	
   if (wp.itemEmpty("ex_acct_key")) {
      if (wp.itemEmpty("ex_card_no")) {
         alertErr("請輸入帳戶帳號 or 卡號");
         return;
      }

      String sql1 = " select acct_type , acct_key, p_seqno from act_acno where acno_p_seqno in "
            + " (select acno_p_seqno from crd_card where card_no = ? ) ";

      sqlSelect(sql1, new Object[]{wp.itemStr("ex_card_no")});

      if (sqlRowNum <= 0) {
         alertErr("卡號輸入錯誤");
         return;
      }

      lsAcctType = sqlStr("acct_type");
      lsAcctKey = sqlStr("acct_key");

      wp.itemSet("ex_acct_type", lsAcctType);
      wp.itemSet("ex_acct_key", lsAcctKey);
      wp.itemSet("ex_p_seqno", sqlStr("p_seqno"));

   } else {
      lsAcctType = wp.itemNvl("ex_acct_type", "01");
      lsAcctKey = wp.itemStr("ex_acct_key");
      lsAcctKey = commString.acctKey(lsAcctKey);
   }

   if (lsAcctKey.length() != 11) {
      alertErr("帳戶帳號:輸入錯誤");
      return;
   }
   if (wp.itemEmpty("ex_p_seqno")) {
      zzVipColor(wp.itemNvl("ex_acct_type", "01") + lsAcctKey);
   }
   else {
      zzVipColor(wp.itemStr("ex_p_seqno"));
   }

   if (queryBefore() == false) {
      queryClear();
      alertErr("此條件查無資料");
      return;
   }

   lsAcctMonth = wp.itemStr("ex_acct_month");
   lsLastAcctMonth = commString.mid(commDate.dateAdd(lsAcctMonth, 0, -1, 0), 0, 6);

   wp.setQueryMode();
   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.colSet("cb_print","");
   String lsPseqno =wp.colStr("ex_p_seqno");
   daoTid = "A1_";
   wp.sqlCmd = " select * , (select bank_name from act_ach_bank where substr(bank_no,1,3) = stmt_auto_pay_bank "
         +commSqlStr.rownum(1)+") as auto_pay_bank_name "
         + " from act_acct_hst "
         + " where 1=1 "
         + sqlCol(lsPseqno, "p_seqno")
         + sqlCol(lsLastAcctMonth, "acct_month")
   ;

   pageSelect();
   if (sqlRowNum <= 0) {
      queryClear();
      alertErr("此條件查無資料");
      return;
   }
   queryAfterTab1(lsPseqno, lsAcctMonth);
   queryReadTab2();
   queryReadTab3();
   queryReadTab4();
   queryReadTab5();
   queryReadTab6();
   checkPrint();
   wp.colSet("cb_print","1");
   //-JH210224;20210226取消-
//   double lm_amt =wp.colNum("A1_stmt_payment_amt") - wp.colNum("A1_stmt_adjust_amt");
//   wp.colSet("A1_stmt_payment_amt", lm_amt);

}

void checkPrint() {
	String sql1 = " select count(*) as db_cnt from cti_acmm "
					+ " where 1=1 and p_seqno = ? and acct_month = ? "
					+ " and proc_flag = '0' and from_mark = '07' and from_type = '01' "
					;
	
	sqlSelect(sql1,new Object[]{wp.colStr("ex_p_seqno") , lsLastAcctMonth});
	if(sqlNum("db_cnt")>0)	wp.colSet("acmm_print", "已登錄");
	else	wp.colSet("acmm_print", "未登錄");
	
}

void queryAfterTab1(String aPseqno, String aAcctMonth) {
   wp.colSet("ex_payment_no", wp.colStr("A1_stmt_payment_no"));
   wp.colSet("ex_cycle_date", wp.colStr("A1_stmt_cycle_date"));
   wp.colSet("ex_stmt_last_payday", wp.colStr("A1_stmt_last_payday"));
   wp.colSet("A1_db_ri", "0");
   wp.colSet("A1_db_lf", "0");
   wp.colSet("A1_db_sf", "0");			
   wp.colSet("A1_db_cf", "0");
   wp.colSet("A1_db_pf", "0");
   wp.colSet("A1_db_pn", "0");
   wp.colSet("A1_db_af", "0");
   wp.colSet("A1_db_ai", "0");
   wp.colSet("A1_db_0101", "0");
   wp.colSet("A1_db_0099", "0");
   wp.colSet("A1_db_coma", "0");
   wp.colSet("A1_db_0100", "0");
   wp.colSet("A1_db_bon1", "0");
	//-紅利積點-
   if (empty(aPseqno) || empty(aAcctMonth)) return;
   String sql1 ="select " +
         " last_month_bonus as A1_stmt_last_month_bp" +
         ", new_add_bonus as A1_stmt_new_add_bp" +
         ", remove_bonus+adjust_bonus as A1_stmt_adjust_bp" +
         ", give_bonus as A1_stmt_give_bp" +
//         ", '' as stmt_give_reason1, '' as stmt_give_reason2, '' as stmt_give_reason3,'' as stmt_give_reason4" +
         ", use_bonus as A1_stmt_use_bp" +
         ", net_bonus as A1_stmt_net_bp" +
         " FROM mkt_bonus_hst" +
         " WHERE bonus_type ='BONU' "+
         " and p_seqno=? and acct_month=?" +commSqlStr.rownum(1)
         ;

   setString(1,aPseqno);
   setString(aAcctMonth);
   sqlSelect(sql1);
   if (sqlRowNum >=0) {
      sql2wp("A1_stmt_last_month_bp");
      sql2wp("A1_stmt_new_add_bp");
      sql2wp("A1_stmt_adjust_bp");
      sql2wp("A1_stmt_give_bp");
      sql2wp("A1_stmt_use_bp");
      sql2wp("A1_stmt_net_bp");
   }
}

void queryReadTab2() throws Exception {
   String lsCycleDate = wp.colStr("A1_stmt_cycle_date");
   //-act_acct_hst.stmt_cycle_date='' 表示未關帳, 可能變更CYCLE-
   if (empty(lsCycleDate)) return;

   daoTid = "A2_";
   wp.sqlCmd = "select * from ( select "
         + " uf_curr_sort(curr_code) as wk_curr_sort ,"
         + " decode(locate(acct_code,'|BL|IT|ID|CA|AO'),0,'9','1') as wk_sort_2 ,"
         + " decode(locate(acct_code,'|BL|IT|ID|CA|AO|AF|OT'),0,'',card_no) AS card_no,"
         + " purchase_date , acct_code,"
         + " 'A' as xx , "
         + " interest_date ,"
         + " uf_mcht_name(mcht_chi_name,mcht_eng_name) as mcht_name , "
         + " decode(source_curr,'','','901','',process_date) as process_date ,"
         + " uf_tt_curr_code(source_curr) as source_curr ,"
         + " source_amt ,"
         + " curr_code ,"
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt ,"
         + " dest_amt ,"
         + " mcht_city ,"
         + " txn_code ,"
         + " reference_no , "
         + " decode(net_purchase_flag,'Y','*',net_purchase_flag) as net_purchase_flag , "  //decode(net_purchase_flag,'Y','*','')
         + " qr_flag "
         + " from bil_bill "
         + " where 1=1 "
         + sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
         + sqlCol(lsCycleDate, "billed_date")
         + " union all select "
         + " uf_curr_sort(curr_code) as wk_curr_sort ,"
         + " decode(locate(acct_code,'|BL|IT|ID|CA|AO'),0,'9','1') as wk_sort_2 ,"
         + " decode(locate(acct_code,'|BL|IT|ID|CA|AO|AF|OT'),0,'',card_no) AS card_no,"
         + " purchase_date ,"
         + " acct_code, "
         + " 'A' as xx , "
         + " interest_date ,"
         + " uf_mcht_name(mcht_chi_name,mcht_eng_name) as mcht_name , "
         + " decode(source_curr,'','','901','',process_date) as process_date ,"
         + " uf_tt_curr_code(source_curr) as source_curr ,"
         + " dc_curr_adjust_amt as source_amt ,"
         + " curr_code ,"
         + " uf_dc_amt2(curr_adjust_amt,dc_curr_adjust_amt) as dc_dest_amt ,"
         + " curr_adjust_amt as dest_amt ,"
         + " mcht_city ,"
         + " txn_code ,"
         + " reference_no , "
         + " '' as net_purchase_flag , "  //decode(net_purchase_flag,'Y','*','')
         + " qr_flag "
         + " from bil_bill "
         + " where 1=1 "
         + sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
         + sqlCol(lsCycleDate, "billed_date")
         +" and (curr_adjust_amt<>0 or dc_curr_adjust_amt<>0)"
         + " union all "
         + " select "
         + " uf_curr_sort(A.curr_code) as wk_curr_sort , "
         + " '9' as wk_sort_2 , "
         + " '' as card_no , "
         + " A.payment_date as purchase_date , "
         + " '' as acct_code, "
         + " 'B' as xx , "
         + " '' as interest_date , "
         + " B.bill_desc as mcht_name , "
         + " '' as process_date , "
         + " uf_tt_curr_code(A.curr_code) as source_curr , "
         + " (0 - A.dc_payment_amt) as source_amt , "
         + " A.curr_code , "         
         + " (0 - A.dc_payment_amt) as dc_dest_amt , "
         + " (0 - A.payment_amt) as dest_amt ,"
         + " '' as mcht_city , "
         + " A.payment_type as txn_code , "
         + " '' as reference_no, " //A.reference_no as reference_no , "
         + " '' as net_purchase_flag , "
         + " '' as qr_flag "
         + " from cyc_pyaj A left join ptr_payment B on A.payment_type = B.payment_type  "
         + " where 1=1 "
         + " and A.class_code = 'B' "
         + sqlCol(wp.colStr("ex_p_seqno"), "A.p_seqno")
         + sqlCol(lsCycleDate, "A.settle_date")
         + " ) "
         + " order by wk_curr_sort,wk_sort_2,decode(acct_code,'','zz',acct_code),card_no,purchase_date,txn_code,reference_no, Dest_amt Desc "
//         + " order by purchase_date Asc "
         ;
   pageQuery();
   if (sqlNotFind()) {
      wp.notFound = "N";
      return;
   }
   wp.setListCount(1);
   queryReadTab2After();
}

void queryReadTab2After() throws Exception {
	int ilSelectCnt = wp.selectCnt ;
	Double ldDestAmt = 0.0;

//	String sql1 ="select qr_flag from bil_nccc300_dtl where reference_no =?";
   //decode(net_purchase_flag,'Y','*','')
	//String sql2 ="select count(*) as net_cnt from cyc_pos_entry where reference_no =?"+zzsql.rownum(1);
   //String sql2 ="select net_purchase_flag from bil_bill where reference_no =?";
	for(int ii=0;ii<ilSelectCnt;ii++){
	   String lsRefNo =wp.colStr(ii,"A2_reference_no");
	   if (empty(lsRefNo)) {
	      wp.colSet(ii,"A2_detl_off","style='display:none;'");
      }
	   else wp.colSet(ii,"A2_detl_off","");
//	   //-網購回饋-
//      String ls_tab =wp.colStr(ii,"A2_xx");
//	   if (no_empty(ls_refNo) && eq_any(ls_tab,"A")) {
//	      sqlSelect(sql2,ls_refNo);
//	      if (sqlRowNum>0 && eq_igno(sqlStr("net_purchase_flag"),"Y")) {
//	         wp.colSet(ii,"A2_net_purchase_flag","*");
//         }
//      }

		ldDestAmt = wp.colNum(ii,"A2_dest_amt");
		if(wp.colEq(ii,"A2_acct_code", "RI")){
			wp.colSet("A1_db_ri", ldDestAmt+wp.colNum("A1_db_ri"));
		}	else if(wp.colEq(ii,"A2_acct_code", "LF")){
			wp.colSet("A1_db_lf", ldDestAmt+wp.colNum("A1_db_lf"));
		}	else if(wp.colEq(ii,"A2_acct_code", "SF")){
			wp.colSet("A1_db_sf", ldDestAmt+wp.colNum("A1_db_sf"));			
		}	else if(wp.colEq(ii,"A2_acct_code", "CF")){
			wp.colSet("A1_db_cf", ldDestAmt+wp.colNum("A1_db_cf"));
		}	else if(wp.colEq(ii,"A2_acct_code", "PF")){
			wp.colSet("A1_db_pf", ldDestAmt+wp.colNum("A1_db_pf"));
		}	else if(wp.colEq(ii,"A2_acct_code", "PN")){
			wp.colSet("A1_db_pn", ldDestAmt+wp.colNum("A1_db_pn"));
		}	else if(wp.colEq(ii,"A2_acct_code", "AF")){
			wp.colSet("A1_db_af", ldDestAmt+wp.colNum("A1_db_af"));
		}	else if(wp.colEq(ii,"A2_acct_code", "AI")){
			wp.colSet("A1_db_ai", ldDestAmt+wp.colNum("A1_db_ai"));
		}
		
		if(wp.colEq(ii,"A2_txn_code", "0101")){
			wp.colSet("A1_db_0101", ldDestAmt+wp.colNum("A1_db_0101"));
		}	else if(wp.colEq(ii,"A2_txn_code", "0099")){
			wp.colSet("A1_db_0099", ldDestAmt+wp.colNum("A1_db_0099"));
		}	else if(wp.colEq(ii,"A2_txn_code", "COMA")){
			wp.colSet("A1_db_coma", ldDestAmt+wp.colNum("A1_db_coma"));
		}	else if(wp.colEq(ii,"A2_txn_code", "0100")){
			wp.colSet("A1_db_0100", ldDestAmt+wp.colNum("A1_db_0100"));
		}	else if(wp.colEq(ii,"A2_txn_code", "BON1")){
			wp.colSet("A1_db_bon1", ldDestAmt+wp.colNum("A1_db_bon1"));
		}
		//--
      String lsRefno =wp.colStr(ii,"A2_reference_no");
		if (!empty(lsRefno)) {
//         sqlSelect(sql1,new Object[]{ls_refno});
//         if (sqlRowNum >0) {
//            wp.colSet(ii,"A2_qr_flag",sqlStr("qr_flag"));
//         }
//         //-QR-code-
//         String ls_qr_flag =wp.colStr(ii,"A2_qr_flag");
//         String ss =zzstr.decode(ls_qr_flag,"t,Q,Y,1",
//               "自行收單被掃(t),自行收單主掃(Q),NCCC掃碼付款(Y),收單紅利全額折抵");
//         wp.colSet(ii,"tt_A2_qr_flag",ss);
      }
	}
}

void queryReadTab3() throws Exception {
   daoTid = "A3_";
//   wp.sqlCmd = " select "
//         + " uf_curr_sort(C.curr_code) AS wk_curr_code , A.p_seqno , C.curr_code , A.acct_month"
//         + ", decode(C.curr_code,'901',a.stmt_auto_pay_bank,C.stmt_auto_pay_bank) AS stmt_auto_pay_bank"
//         + ", decode(C.curr_code,'901',a.stmt_auto_pay_no,C.stmt_auto_pay_no) AS stmt_auto_pay_no " +
//         ", decode(C.curr_code,'901',a.stmt_auto_pay_date, C.stmt_auto_pay_date) AS stmt_auto_pay_date " +
//         ", decode(C.curr_code,'901',a.stmt_auto_pay_amt, C.stmt_auto_pay_amt) AS stmt_auto_pay_amt " +
//         ", decode(C.curr_code,'901',a.stmt_last_ttl,C.stmt_last_ttl) AS stmt_last_ttl " +
//         ", decode(C.curr_code,'901',a.stmt_payment_amt, C.stmt_payment_amt) AS stmt_payment_amt " +
//         ", decode(C.curr_code,'901',a.stmt_over_due_amt, C.stmt_over_due_amt) AS stmt_over_due_amt " +
//         ", decode(C.curr_code,'901',a.stmt_adjust_amt, C.stmt_adjust_amt) AS stmt_adjust_amt " +
//         ", decode(C.curr_code,'901',a.stmt_new_amt, C.stmt_new_amt) AS stmt_new_amt " +
//         ", decode(C.curr_code,'901',a.stmt_this_ttl_amt, C.stmt_this_ttl_amt) AS stmt_this_ttl_amt " +
//         ", decode(C.curr_code,'901',a.stmt_mp, C.stmt_mp) AS stmt_mp " +
//         ", '' AS autopay_dc_flag"
//         + " from act_curr_hst C join act_acct_hst A on C.p_seqno=A.p_seqno and C.acct_month=A.acct_month"
//         + " where 1=1 "
//         + sqlCol(wp.colStr("ex_p_seqno"), "a.p_seqno")
//         + sqlCol(ls_last_acct_month, "a.acct_month")
//         + " order by 1 "
//   ;
   wp.sqlCmd = " select "
         + " uf_curr_sort(C.curr_code) AS wk_curr_code , C.p_seqno , C.curr_code , C.acct_month"
         + ", C.stmt_auto_pay_bank AS stmt_auto_pay_bank"
         + ", C.stmt_auto_pay_no AS stmt_auto_pay_no " +
         ", C.stmt_auto_pay_date AS stmt_auto_pay_date " + //A3_stmt_auto_pay_date
         ", C.stmt_auto_pay_amt AS stmt_auto_pay_amt " +
         ", C.stmt_last_ttl AS stmt_last_ttl " +
         ", C.stmt_payment_amt AS stmt_payment_amt " +
         ", C.stmt_over_due_amt AS stmt_over_due_amt " +
         ", C.stmt_adjust_amt AS stmt_adjust_amt " +
         ", C.stmt_new_amt AS stmt_new_amt " +
         ", C.stmt_this_ttl_amt AS stmt_this_ttl_amt " +
         ", C.stmt_mp AS stmt_mp " +
         ", '' as autopay_dc_flag"
         + " from act_curr_hst C"  // join act_acct_hst A on C.p_seqno=A.p_seqno and C.acct_month=A.acct_month"
         + " where 1=1 "
         + sqlCol(wp.colStr("ex_p_seqno"), "C.p_seqno")
         + sqlCol(lsLastAcctMonth, "C.acct_month")
         + " order by 1 "
   ;
   pageQuery();
   if (this.sqlNotFind()) {
      wp.notFound = "N";
      return;
   }
   wp.setListCount(2);
   queryAfterTab3(wp.itemStr("ex_p_seqno"));
}

void queryAfterTab3(String aPseqno) {
   int llNrow =wp.listCount[1];

   wp.colSet("A3_wk_acct_month", lsAcctMonth);
   wp.colSet("A3_stmt_credit_limit", wp.colStr("A1_stmt_credit_limit"));
   wp.colSet("A3_stmt_revol_rate", wp.colStr("A1_stmt_revol_rate"));
   wp.colSet("A3_stmt_last_payday", wp.colStr("A1_stmt_last_payday"));

   if (llNrow==0 || empty(aPseqno))
      return;

   String sql1 ="select autopay_dc_flag"
         +" from act_acct_curr"
         +" where p_seqno =? and curr_code=?";
   for (int ii = 0; ii < llNrow; ii++) {
      String lsCurrCode =wp.colStr(ii,"A3_curr_code");
      if (eqIgno(lsCurrCode,"901")) {
         continue;
      }
      setString(1,aPseqno);
      setString(lsCurrCode);
      sqlSelect(sql1);
      if (sqlRowNum >0) {
         wp.colSet(ii,"A3_autopay_dc_flag",sqlNvl("autopay_dc_flag","N"));
      }
   }
}

void queryReadTab4() throws Exception {
   String lsPseqno =wp.colStr("ex_p_seqno");
   String lsCycleDate =wp.colStr("ex_cycle_date");
   if (empty(lsCycleDate)) return;

   daoTid = "A4_";
   wp.sqlCmd = " select "
         + " uf_curr_sort(curr_code) as wk_curr_sort , "
         + " card_no, "
         + " purchase_date, "
         + " interest_date, "
         + commSqlStr.mchtName("", "") + " as wk_mcht_name , "
         + " (select C.curr_eng_name from ptr_currcode C where C.curr_code = source_curr) as source_curr, "
         + " source_amt, "
         + " curr_code, "
         + " dc_dest_amt, "
         + " mcht_city, "
         + " txn_code, "
         + " '' as db_rskdesc, "
         + " reference_no , "
         + " rsk_problem1_mark ,  "
         + " rsk_receipt_mark , "
         + " rsk_chgback_mark "
         + " from cyc_problem "
         + " where 1=1 "
         + " and p_seqno =? and billed_date =?"
         + " order by 1 Asc , 2 Asc, 3 Asc "
   ;

   setString(1,lsPseqno);
   setString(2,lsCycleDate);
   pageQuery();
   if (sqlNotFind()) {
      selectOK();
      return;
   }
   wp.setListCount(3);
   queryAfterTab4();
}

void queryAfterTab4() {
   String lsDesc = "";
   
   int llNrow =wp.selectCnt;
   for (int ii = 0; ii <llNrow; ii++) {
      lsDesc = "";
      if (wp.colStr(ii,"A4_rsk_chgback_mark").compareTo("130") >= 0) {
         lsDesc = "洽收單行處理中, 款項暫緩計收";
      } else if (wp.colStr(ii,"A4_rsk_receipt_mark").compareTo("30") >= 0) {
         lsDesc = "調單中, 款項暫緩計收";
      } else {
         lsDesc = "洽收單行處理中, 款項暫緩計收";
      }            
      if (commString.strToNum(commString.mid(wp.colStr(ii,"A4_rsk_problem1_mark"), 1,2))<80) {
         wp.colSet(ii, "A4_db_rskdesc", lsDesc);
         continue;
      }

      //ls_prbl1_mark = zzstr.mid(sqlStr("rsk_prbl1_mark"), 3, 2);
      int liClose =(int)commString.strToNum(commString.mid(wp.colStr(ii,"A4_rsk_problem1_mark"), 3,2));      
      if (liClose>=11 && liClose<=17) {
         lsDesc = "款項由本行墊付, 您不需付款";
      } else if (liClose ==19) {
         lsDesc = "款項由本行墊付, 您不需付款";
      } else if (liClose>=61 && liClose<=63) {
         lsDesc = "款項由本行墊付, 您不需付款";
      } else if (liClose>=65 && liClose<=67) {
         //ls_prbl1_mark.compareTo("65") >= 0 && ls_prbl1_mark.compareTo("67") <= 0
         lsDesc = "款項由本行墊付, 您不需付款";
      } else if (liClose==18) {
         lsDesc = "已向商店索回款項抵付本問題帳款";
      } else if (liClose==20) {
         lsDesc = "已向商店索回款項抵付本問題帳款";
      } else if (liClose>=31 && liClose<=34) {
         lsDesc = "經調查結果, 請您支付本息";
      } else if (liClose>=71 && liClose<=73) {
         lsDesc = "經調查結果, 請您支付本息";
      } else if (liClose>=41 && liClose<=44) {
         lsDesc = "經調查結果, 請您支付本金, 利息免計";
      } else if (liClose>=81 && liClose<=83) {
         lsDesc = "經調查結果, 請您支付本金, 利息免計";
      } else if (liClose==21) {
         lsDesc = "商店退款已抵付您的信用卡帳款, 詳帳單";
      } else {
         lsDesc = "";
      }
      //wp.colSet(ii, "A4_db_rskdesc",zzstr.mid(wp.colStr(ii,"A4_rsk_problem1_mark"), 3,2)+"."+ls_desc);
      wp.colSet(ii, "A4_db_rskdesc",lsDesc);
   }
}

void queryReadTab5() throws Exception {
	daoTid = "A5_";
	wp.sqlCmd = " select "
				 + " p_seqno ,"
				 + " acct_month ,"
				 + " fund_code ,"
				 + " fund_name ,"
				 + " bill_desc ,"
				 + " acct_type ,"
				 + " uf_acno_key(p_seqno) as acct_key ,"
				 + " uf_nvl(last_month_fund, 0) as last_month_fund ,"
				 + " uf_nvl(new_add_fund, 0) as new_add_fund ,"
				 + " uf_nvl(adjust_fund, 0) as adjust_fund ,"
				 + " uf_nvl(use_fund, 0) as use_fund ,"
				 + " uf_nvl(give_fund, 0) as give_fund ,"
				 + " uf_nvl(remove_fund, 0) as remove_fund ,"
				 + " uf_nvl(net_fund, 0) as net_fund "
				 + " from mkt_fund_hst "
				 + " where 1=1 "
				 + sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
				 + sqlCol(lsAcctMonth, "acct_month")
				 + " and curr_code = '901' "				 
				 ;
	
	pageQuery();
   if (this.sqlNotFind()) {
      wp.notFound = "N";   
      wp.colSet("A5_tl_cnt", "0");
      wp.colSet("tl_last_month_fund", "0");
      wp.colSet("tl_new_add_fund", "0");
      wp.colSet("tl_adjust_fund", "0");
      wp.colSet("tl_remove_fund", "0");
      wp.colSet("tl_net_fund", "0");
      wp.colSet("tl_use_fund", "0");
      wp.colSet("tl_give_fund", "0");
      return;
   }
   wp.setListCount(4);
   sumDataTab5();
}

void queryReadTab6() throws Exception {
   wp.sqlCmd = " select uf_curr_sort(curr_code) as wk_curr_sort,"
         + " fund_code ,"
         +" p_seqno , acct_month , curr_code,"
         + " fund_name ,"
         + " bill_desc ,"
         + " acct_type ,"
         + " uf_acno_key(p_seqno) as acct_key ,"
         + " last_month_fund,"
         + " new_add_fund , adjust_fund+diff_fund as adjust_fund , use_fund , give_fund , remove_fund ,"
         + " net_fund "
         + " from mkt_fund_hst "
         + " where 1=1 "
         + sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
         + sqlCol(lsAcctMonth, "acct_month")
         + " and curr_code <> '901' "
   +" order by 1,2"
   ;
   daoTid = "A6_";
   pageQuery();
   if (this.sqlNotFind()) {
      wp.notFound = "N";      
      return;
   }
   //wp.setListCount(5);
   wp.setListSernum(4,"A6_ser_num",sqlRowNum);
   sumDataTab6();
}
void sumDataTab6() throws Exception {

   String sql1 = " select "
         + " sum(last_month_fund) as last_month " +
         ", sum(new_add_fund) as add_amt"+
         ", sum(adjust_fund) as mod_amt"+
         ", sum(use_fund) as use_amt"+
         ", sum(give_fund) as give_amt"+
         ", sum(remove_fund) as del_amt"+
         ", sum(net_fund) as this_month"+
         " from mkt_fund_hst "+
         " where curr_code =? "+
         sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")+
         sqlCol(lsAcctMonth,"acct_month");
   //--日幣
   sqlSelect(sql1, "392");
   if (sqlRowNum >0) {
      wp.colSet("A6_ttl_last_month_392", sqlNum("last_month"));
      wp.colSet("A6_ttl_add_amt_392", sqlNum("add_amt"));
      wp.colSet("A6_ttl_mod_amt_392", sqlNum("mod_amt"));
      wp.colSet("A6_ttl_use_amt_392", sqlNum("use_amt"));
      wp.colSet("A6_ttl_del_amt_392", sqlNum("del_amt"));
      wp.colSet("A6_ttl_this_month_392", sqlNum("this_month"));
   }
   //--美金
   sqlSelect(sql1,"840");
   if (sqlRowNum >0) {
      wp.colSet("A6_ttl_last_month_840", sqlNum("last_month"));
      wp.colSet("A6_ttl_add_amt_840", sqlNum("add_amt"));
      wp.colSet("A6_ttl_mod_amt_840", sqlNum("mod_amt"));
      wp.colSet("A6_ttl_use_amt_840", sqlNum("use_amt"));
      wp.colSet("A6_ttl_del_amt_840", sqlNum("del_amt"));
      wp.colSet("A6_ttl_this_month_840", sqlNum("this_month"));
   }
}

void sumDataTab5() {
	int ilSelectCnt = 0 ;
	ilSelectCnt = wp.selectCnt ;
   String sql1 = " select "
   		+ " count(*) as db_cnt , "
         + " sum(last_month_fund) as tl_last_month_fund ,  "
         + " sum(new_add_fund) as tl_new_add_fund ,  "
         + " sum(adjust_fund) as tl_adjust_fund ,  "
         + " sum(remove_fund) as tl_remove_fund ,  "
         + " sum(net_fund) as tl_net_fund ,  "
         + " sum(use_fund) as tl_use_fund , "
         + " sum(give_fund) as tl_give_fund "
         + " from mkt_fund_hst "
         + " where 1=1 "
         + " and p_seqno = ? "
         + " and acct_month = ? "
         + " and curr_code = '901' ";

   sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), lsAcctMonth});
   
   wp.colSet("A5_tl_cnt", sqlStr("db_cnt"));
   wp.colSet("tl_last_month_fund", sqlStr("tl_last_month_fund"));
   wp.colSet("tl_new_add_fund", sqlStr("tl_new_add_fund"));
   wp.colSet("tl_adjust_fund", sqlStr("tl_adjust_fund"));
   wp.colSet("tl_remove_fund", sqlStr("tl_remove_fund"));
   wp.colSet("tl_net_fund", sqlStr("tl_net_fund"));
   wp.colSet("tl_use_fund", sqlStr("tl_use_fund"));
   wp.colSet("tl_give_fund", sqlStr("tl_give_fund"));
   
   String sql2 = "";
   sql2 = " select bill_desc from ptr_payment where 1=1 and payment_type = ? ";
   for(int ii=0;ii<ilSelectCnt;ii++){
   	if(wp.colEmpty(ii,"fund_name")==false)	continue;
   	sqlSelect(sql2,new Object[]{wp.colStr(ii,"fund_code")});
   	if(sqlRowNum>0){
   		wp.colSet(ii,"fund_name", sqlStr("bill_desc"));
   	}
   }
}


@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   dataRead();
}

public void querySelectS3() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   kk3 = wp.itemStr("data_k3");
   dataReadS3();
}

@Override
public void dataRead() throws Exception {
   rskm01.BilBill bill = new rskm01.BilBill();
   bill.setConn(wp);

   bill.varsSet("reference_no", kk1);
   if (bill.dataSelect() != 1) {
      wp.notFound = "Y";
      alertErr(bill.getMsg());
      return;
   }

}

public void dataReadS3() throws Exception {
      wp.sqlCmd ="select C.curr_code , C.stmt_last_ttl" +
            ", C.stmt_auto_pay_date" +
            ", C.stmt_auto_pay_bank" +
            ", C.stmt_auto_pay_no" +
            ", C.stmt_payment_amt" +
            ", C.stmt_adjust_amt" +
            ", C.stmt_new_amt" +
            ", C.stmt_auto_pay_amt" +
            ", C.stmt_this_ttl_amt" +
            ", A.stmt_credit_limit" +
            ", C.stmt_mp" +
            ", A.stmt_revol_rate, A.stmt_cycle_date" +
            " from act_acct_hst A join act_curr_hst C on A.p_seqno=C.p_seqno and A.acct_month=C.acct_month " +
            " where C.p_seqno =? and C.acct_month =? and C.curr_code=?";
      setString(1,kk1);
      setString(kk2);
      setString(kk3);

   pageSelect();
   if (this.sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
   }
   
   dataReadS3After();
   
}

void dataReadS3After() {
	String lsStmtCycleDate = wp.colStr("stmt_cycle_date");
	if (empty(lsStmtCycleDate)) return;

	String sql2 = " select "
         		+ " 'A' as xx , "
         		+ " uf_curr_sort(curr_code) as wk_curr_sort ,"
         		+ " decode(locate(acct_code,'|BL|IT|ID|CA|AO'),0,'9','1') as wk_sort_2 ,"
         		+ " decode(acct_code,'BL',card_no,'IT',card_no,'ID',card_no,'CA',card_no,'AO',card_no,'AF',card_no,'OT',card_no,'PF',card_no,'') as card_no ,"
         		+ " purchase_date ,"
         		+ " interest_date ,"
         		+ commSqlStr.mchtName("", "") + " as mcht_name , "
         		+ " decode(source_curr,'','','901','',process_date) as process_date ,"
         		+ " (select C.curr_eng_name from ptr_currcode C where C.curr_code = source_curr) as source_curr ,"
         		+ " source_amt ,"
         		+ " curr_code ,"
         		+ " dc_dest_amt ,"
         		+ " dest_amt ,"
         		+ " mcht_city ,"
         		+ " txn_code ,"
         		+ " reference_no , "
         		+ " decode(net_purchase_flag,'Y','*','') as net_purchase_flag , "
         		+ " acct_code "
         		+ " from bil_bill "
         		+ " where 1=1 "
         		+ sqlCol(kk1, "p_seqno")
         		+ sqlCol(lsStmtCycleDate, "billed_date")
         		+ sqlCol(kk3,"curr_code")
         		+ " union all "
         		+ " select "
         		+ " 'B' as xx , "
         		+ " uf_curr_sort(A.curr_code) as wk_curr_sort , "
         		+ " '' as wk_sort_2 , "
         		+ " '' as card_no , "
         		+ " A.payment_date as purchase_date , "
         		+ " '' as interest_date , "
         		+ " B.bill_desc as mcht_name , "
         		+ " '' as process_date , "
         		+ " (select C.curr_eng_name from ptr_currcode C where C.curr_code = A.curr_code) as source_curr , "
         		+ " (0 - A.dc_payment_amt) as source_amt , "
         		+ " A.curr_code , "
         		+ " (0 - A.dc_payment_amt) as dc_dest_amt , "
         		+ " (0 - A.payment_amt) as dest_amt ,"
         		+ " '' as mcht_city , "
         		+ " A.payment_type as txn_code , "
         		+ " A.reference_no as reference_no , "
         		+ " '' as net_purchase_flag , "
         		+ " '' as acct_code "
         		+ " from cyc_pyaj A left join ptr_payment B on A.payment_type = B.payment_type  "
         		+ " where 1=1 "
         		+ " and A.class_code = 'B' "
         		+ sqlCol(kk1, "A.p_seqno")
         		+ sqlCol(lsStmtCycleDate, "A.settle_date")
         		+ sqlCol(kk3,"A.curr_code")
         		+ " order by 1 Asc , 2, 3 ,4, 5 "
         		;
	
	sqlSelect(sql2);
	if(sqlRowNum<=0)	return ;
	int ilSelectCnt = sqlRowNum ;
	double ldDestAmt = 0.0;
	for(int ii=0;ii<ilSelectCnt;ii++){
		//--2020-0203 User 要求以該幣別顯示
//		ld_dest_amt = sqlNum(ii,"dest_amt");
		ldDestAmt = sqlNum(ii,"dc_dest_amt");
		if(eqIgno(sqlStr(ii,"acct_code"),"RI")){
			wp.colSet("db_ri", ldDestAmt+wp.colNum("db_ri"));
		}	else if(eqIgno(sqlStr(ii,"acct_code"),"LF")){
			wp.colSet("db_lf", ldDestAmt+wp.colNum("db_lf"));
		}	else if(eqIgno(sqlStr(ii,"acct_code"),"SF")){
			wp.colSet("db_sf", ldDestAmt+wp.colNum("db_sf"));			
		}	else if(eqIgno(sqlStr(ii,"acct_code"),"CF")){
			wp.colSet("db_cf", ldDestAmt+wp.colNum("db_cf"));
		}	else if(eqIgno(sqlStr(ii,"acct_code"),"PF")){
			wp.colSet("db_pf", ldDestAmt+wp.colNum("db_pf"));
		}	else if(eqIgno(sqlStr(ii,"acct_code"),"PN")){
			wp.colSet("db_pn", ldDestAmt+wp.colNum("db_pn"));
		}	else if(eqIgno(sqlStr(ii,"acct_code"),"AF")){
			wp.colSet("db_af", ldDestAmt+wp.colNum("db_af"));
		}	else if(eqIgno(sqlStr(ii,"acct_code"),"AI")){
			wp.colSet("db_ai", ldDestAmt+wp.colNum("db_ai"));
		}
		
		if(eqIgno(sqlStr(ii,"txn_code"),"0101")){
			wp.colSet("db_0101", ldDestAmt+wp.colNum("db_0101"));
		}	else if(eqIgno(sqlStr(ii,"txn_code"),"0099")){
			wp.colSet("db_0099", ldDestAmt+wp.colNum("db_0099"));
		}	else if(eqIgno(sqlStr(ii,"txn_code"),"COMA")){
			wp.colSet("db_coma", ldDestAmt+wp.colNum("db_coma"));
		}	else if(eqIgno(sqlStr(ii,"txn_code"),"0100")){
			wp.colSet("db_0100", ldDestAmt+wp.colNum("db_0100"));
		}	else if(eqIgno(sqlStr(ii,"txn_code"),"BON1")){
			wp.colSet("db_bon1", ldDestAmt+wp.colNum("db_bon1"));
		}
			
	}
	
}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   String lsPSeqno = "", lsAcctMonth = "", lsPrintYm = "", lsAcctType = "", lsIdPSeqno = "";
   lsPSeqno = wp.itemStr("ex_p_seqno");
   lsAcctMonth = wp.itemStr("A1_acct_month");
   lsPrintYm = commDate.dateAdd(lsAcctMonth, 0, 1, 0);
   lsPrintYm = commString.mid(lsPrintYm, 0, 6);
   lsAcctType = wp.itemStr("ex_acct_type");
   lsIdPSeqno = wp.itemStr("ex_id_p_seqno");
   Cycq0020Func func = new Cycq0020Func();
   func.setConn(wp);

   if (empty(lsPrintYm)) {
      errmsg("請輸入帳務年月");
      return;
   }

   String sql1 = " select hex(rowid) as rowid , proc_flag from cti_acmm "
         + " where p_seqno = ? and acct_month = ? and from_mark = '07' "
         + " and print_month = ? and uf_nvl(proc_flag,'4') = '0' "
         + commSqlStr.rownum(1);


   sqlSelect(sql1, new Object[]{lsPSeqno, lsAcctMonth, lsPrintYm});

   if (!empty(sqlStr("rowid"))) {
      alertMsg("補印帳單 帳務年月:" + lsPrintYm + " 登錄成功");
      return;
   }
       
   func.varsSet("p_seqno", lsPSeqno);
   func.varsSet("acct_month", lsAcctMonth);
   func.varsSet("print_month", lsPrintYm);
   func.varsSet("acct_type", lsAcctType);
   func.varsSet("id_p_seqno", lsIdPSeqno);
   func.varsSet("rowid", sqlStr("rowid"));

   rc = func.dataProc();
   sqlCommit(rc); 
   if (rc != 1) {
      errmsg("補印帳單 帳務年月: " + lsPrintYm + " 登錄失敗");
   } else {
      alertMsg("補印帳單 帳務年月: " + lsPrintYm + " 登錄成功");
   }
}

@Override
public void initButton() {
	
   if (eqIgno(wp.respHtml, "cycq0020")) {
      if (!wp.colEq("cb_print","1")) {
         buttonOff("btnPrint");
      }
//   	if(wp.colEmpty("ex_p_seqno")==false){
//   		wp.colSet("btnPrint", " ");
//   	}
   }

}

void tabClick(){		
	String lsClick = "";
	lsClick = wp.itemStr("tab_click");	
	if (eqIgno(wp.buttonCode, "Q")) wp.colSet("a_click_1", "");
	if(eqIgno(lsClick,"1")){
		wp.colSet("a_click_1", "id='tab_active'");
	}	else if(eqIgno(lsClick,"2")){
		wp.colSet("a_click_2", "id='tab_active'");
	}	else if(eqIgno(lsClick,"3")){
		wp.colSet("a_click_3", "id='tab_active'");
	}	else if(eqIgno(lsClick,"4")){
		wp.colSet("a_click_4", "id='tab_active'");
	}	else if(eqIgno(lsClick,"5")){			
		wp.colSet("a_click_5", "id='tab_active'");
	}	else if(eqIgno(lsClick,"6")){
		wp.colSet("a_click_6", "id='tab_active'");
	}	else	{			
		wp.colSet("a_click_1", "id='tab_active'");
	}
}
@Override
public void initPage() {
   tabClick();

}

}
