/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-10-25  v1.00.00  Yang Bo    Sync code from mega                        * 
* 111-11-16  V1.00.01  Simon      cancel autopay_indicator='3'               *
* 112-12-05  V1.00.02  Simon      get actp2050_detl_0710.curr_cdoe to update *
*                                 act_acno.no_interest_flag、s_month、e_month*
******************************************************************************/
package actp01;
/**
 * 2022-0415   JH    mt9363: chg_user
 * 2020-0721	JH		modify
 * 109-04-15  V1.00.01  Alex       add auth_query
 * func=0705, actm2070直接update act_acno
 * func=0706, actm2080直接update act_acno
 * func=0707, actm2090直接update act_acno
 * func=0709, actm2110 BRD.cancel
 */

import busi.func.ColFunc;
import ecsfunc.DeCodeAct;
import ofcapp.BaseAction;

public class Actp2050 extends BaseAction {
String kk1 = "", kk2 = "", kk3 = "";

@Override
public void userAction() throws Exception {
	defaultAction();
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {

   ColFunc func = new ColFunc();
   func.setConn(wp);
   if (func.fAuthQuery(wp.modPgm(), wp.itemStr2("ex_idno")) != 1) {
      alertErr2(func.getMsg());
      return;
   }

   String lsWhere = " where func_code not in ('0800','0705','0706','0707','0709') "
         + sqlCol(wp.itemStr2("ex_idno"), "substr(dual_key,3,10)")
         + sqlCol(wp.itemStr2("ex_chg_user"), "chg_user");

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.selectSQL = " func_code , "
         + " count(*) as db_cnt "
   ;
   wp.daoTable = "act_dual";
   wp.whereOrder = "  group by func_code order by func_code ";
   pageQuery();
   if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   queryAfter();
   wp.setListCount(0);
   wp.setPageValue();

}

void queryAfter() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "func_code", "0701")) {
         wp.colSet(ii, "func_desc", "帳戶免收違約金維護作業");
      }
      else if (wp.colEq(ii, "func_code", "0702")) {
         wp.colSet(ii, "func_desc", "帳戶自動扣繳確參數維護作業");
      }
      else if (wp.colEq(ii, "func_code", "0703")) {
         wp.colSet(ii, "func_desc", "帳戶循環信用利率加減碼維護作業");
      }
      else if (wp.colEq(ii, "func_code", "0704")) {
         wp.colSet(ii, "func_desc", "帳戶特殊MP百分比維護作業");
      }
      else if (wp.colEq(ii, "func_code", "0705")) {
         wp.colSet(ii, "func_desc", "帳戶特殊對帳單旗標維護作業");
      }
      else if (wp.colEq(ii, "func_code", "0706")) {
         wp.colSet(ii, "func_desc", "帳戶對帳單列印寄送方式維護作業");  //actm2080
      }
      else if (wp.colEq(ii, "func_code", "0707")) {
         wp.colSet(ii, "func_desc", "帳戶對帳單不列印旗標維護作業");  //-actm2090-
      }
      else if (wp.colEq(ii, "func_code", "0708")) {
         wp.colSet(ii, "func_desc", "帳戶RC碼異動維護作業");  //-actm2100-
      }
      else if (wp.colEq(ii, "func_code", "0709")) {
         wp.colSet(ii, "func_desc", "帳戶不自動銷帳旗標維護作業"); //--actm2110.cancel
      }
      else if (wp.colEq(ii, "func_code", "0710")) {
         wp.colSet(ii, "func_desc", "帳戶免收循環息維護作業");  //-actm2030-
      }
      else if (wp.colEq(ii, "func_code", "0800")) {
         wp.colSet(ii, "func_desc", "0800");
      }

   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr2("data_k1");  // chg_user
   kk2 = wp.itemStr2("data_k2");  // id_no
   kk3 = wp.itemStr2("data_k3");  // func_code
   if (empty(kk3)) kk3 = wp.itemStr2("func_code");

   if (eqIgno(kk3, "0701")) {
      dataRead();
   }
   else if (eqIgno(kk3, "0702")) {
      dataRead0702();
   }
   else if (eqIgno(kk3, "0703")) {
      dataRead0703();
   }
   else if (eqIgno(kk3, "0704")) {
      dataRead0704();
   }
   else if (eqIgno(kk3, "0705")) {
      dataRead0705();
   }
   else if (eqIgno(kk3, "0706")) {
      dataRead0706();
   }
   else if (eqIgno(kk3, "0707")) {
      dataRead0707();
   }
   else if (eqIgno(kk3, "0708")) {
      dataRead0708();
   }
   else if (eqIgno(kk3, "0709")) {
      dataRead0709();
   }
   else if (eqIgno(kk3, "0710")) {
      dataRead0710();
   }

}

@Override
public void dataRead() throws Exception {
   wp.selectSQL = ""
         + " dual_key ,"
         + " substrb(dual_key,1,2) acct_type ,"
         + " substrb(dual_key,3,11) acct_key ,"
         + " substr(log_data,1,1) as ex_no_penalty_flag ,"
         + " substr(log_data,2,6) as ex_no_penalty_s_month ,"
       //+ " substr(log_data,8,6) as ex_no_penalty_e_month ,"
         + " decode(substr(log_data,8,6),'999912','',substr(log_data,8,6)) as ex_no_penalty_e_month ,"
         + " chg_date ,"
         + " func_code ,"
         + " chg_user,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
		   + " to_char(mod_time,'hh24miss') as update_time ,"
         + " hex(rowid) as rowid "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0701' "
						+sqlCol(kk1,"chg_user")
						+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   wp.setListCount(0);
   wp.setPageValue();
   for(int ll=0; ll<wp.listCount[0]; ll++) {
   	String lsCname =idnoCname(wp.colStr(ll,"acct_key"),wp.colStr(ll,"acct_type"));
   	wp.colSet(ll,"idno_cname",lsCname);
   	if (apprBankUnit(wp.colStr(ll,"chg_user"),""))
   	   wp.colSet(ll,"can_appr","1");
   	else wp.colSet(ll,"can_appr","");
	}
}

String idnoCname(String aKey, String aType) throws Exception {
	//String sql1 ="select uf_idno_name2(id_p_seqno,acct_type) as idno_cname from act_acno"
	String sql1 ="select uf_acno_name(p_seqno) as idno_cname from act_acno"
			+" where acct_key=? and acct_type=?";

//ppp(1,wp.col_ss("acct_key"));
//ppp(wp.col_ss("acct_type"));
	setString(1,aKey);
	setString(aType);
	sqlSelect(sql1);
	if (sqlRowNum>0) {
		return sqlStr("idno_cname");
	}
	return "";
}

public void dataRead0702() throws Exception {
   wp.selectSQL = ""
         + " dual_key ,"
         + " substrb(dual_key,1,2) acct_type ,"
         + " substrb(dual_key,3,11) acct_key ,"
         + " chg_date ,"
         + " substr(log_data,1,1) as ex_autopay_indicator ,"
       //+ " uf_2num(substr(log_data,2,11)) as ex_autopay_fix_amt ,"
       //+ " uf_2num(substr(log_data,13,3)) as ex_autopay_rate ,"
         + " substr(log_data,2,8) as ex_autopay_acct_bank ,"
         + " substr(log_data,10,16) as ex_autopay_acct_no ,"
         + " substr(log_data,34,8) as ex_edate ,"
         + " substr(log_data,42,20) as ex_autopay_id ,"
         + " substr(log_data,63,1) as ex_modify_select ,"
         + " func_code ,"
         + " chg_user ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
         + " time(mod_time) as update_time , "
         + " mod_seqno , mod_user, mod_time, mod_pgm,"
         + " hex(rowid) as rowid  "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0702' "
						+sqlCol(kk1,"chg_user")
						+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   wp.setListCount(0);
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   wp.setPageValue();
   dateRead0702After();
}

void dateRead0702After() throws Exception{

   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "ex_modify_select", "1")) {
         wp.colSet(ii, "tt_modify_select", "取消帳號");
      }
      else {
         wp.colSet(ii, "tt_modify_select", "修改參數");
      }

      if (wp.colEq(ii, "ex_autopay_indicator", "1")) {
         wp.colSet(ii, "tt_autopay_indicator", "1-扣 TTL");
      }
      else if (wp.colEq(ii, "ex_autopay_indicator", "2")) {
         wp.colSet(ii, "tt_autopay_indicator", "2-扣 MP");
      }
    //else if (wp.colEq(ii, "ex_autopay_indicator", "3")) {
    //   wp.colSet(ii, "tt_autopay_indicator", "3-其他");
    //}

      String lsCname =idnoCname(wp.colStr(ii,"acct_key"),wp.colStr(ii,"acct_type"));
      wp.colSet(ii,"idno_cname",lsCname);
      if (apprBankUnit(wp.colStr(ii,"chg_user"),""))
         wp.colSet(ii,"can_appr","1");
      else wp.colSet(ii,"can_appr","");
   }
}

public void dataRead0703() throws Exception {
   wp.selectSQL = " hex(rowid) as rowid ,"
         + " dual_key ,"
         + " substrb(dual_key,1,2) acct_type ,"
         + " substrb(dual_key,3,11) acct_key ,"
         + " func_code ,"
         + " log_data, chg_user, chg_date ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
			+ " to_char(mod_time,'hh24miss') as update_time ,"
         + " substr(log_data,43,1) as sign_1 ,"
         + " substr(log_data,44,6) as int_rate_1 ,"
         + " substr(log_data,50,6) as ex_start_date_1 ,"
         + " substr(log_data,56,6) as ex_end_date_1 ,"
         + " substr(log_data,63,1) as sign_2 ,"
         + " substr(log_data,64,6) as int_rate_2 ,"
         + " substr(log_data,70,6) as ex_start_date_2 ,"
         + " substr(log_data,76,6) as ex_end_date_2 ,"
         + " substr(log_data,1,7) as wk_std_int_rate1 ,"
         + " substr(log_data,8,7) as wk_std_int_rate2 ,"
         + " substr(log_data,15,7) as wk_std_int_rate3 ,"
         + " substr(log_data,22,7) as wk_std_int_rate4 ,"
         + " substr(log_data,29,7) as wk_std_int_rate5 ,"
         + " substr(log_data,36,7) as wk_std_int_rate6 ,"
         + " substr(log_data,84,1) as O_sign ,"
         + " substr(log_data,85,6) as O_rate ,"
         + " substr(log_data,91,6) as O_sym ,"
         + " substr(log_data,97,6) as O_eym ,"
         + " substr(log_data,103,1) as O_sign_2 ,"
         + " substr(log_data,104,6) as O_rate_2 ,"
         + " substr(log_data,110,6) as O_sym_2 ,"
         + " substr(log_data,116,6) as O_eym_2 "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0703' "
						+sqlCol(kk1,"chg_user")
						+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   wp.setListCount(0);
   dateRead0703After();
   wp.setPageValue();
}

void dateRead0703After() throws Exception {
   for (int ii = 0; ii < wp.listCount[0]; ii++) {

      if (wp.colEq(ii, "sign_1", "+")) {
         wp.colSet(ii, "wk_int_rate", "加 " + wp.colNum(ii, "int_rate_1"));
      }
      else if (wp.colEq(ii, "sign_1", "-")) {
         wp.colSet(ii, "wk_int_rate", "減 " + wp.colNum(ii, "int_rate_1"));
      }

      if (wp.colEq(ii, "sign_2", "+")) {
         wp.colSet(ii, "wk_int2", "加 " + wp.colNum(ii, "int_rate_2"));
      }
      else if (wp.colEq(ii, "sign_1", "-")) {
         wp.colSet(ii, "wk_int2", "減 " + wp.colNum(ii, "int_rate_2"));
      }

      String ls_cname =idnoCname(wp.colStr(ii,"acct_key"),wp.colStr(ii,"acct_type"));
      wp.colSet(ii,"idno_cname",ls_cname);
      if (apprBankUnit(wp.colStr(ii,"chg_user"),""))
         wp.colSet(ii,"can_appr","1");
      else wp.colSet(ii,"can_appr","");
   }
}

public void dataRead0704() throws Exception {
   wp.selectSQL = ""
         + " chg_user ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "  
         + " dual_key ,"
         + " uf_2num(substr(log_data,1,6)) as ex_min_percent_payment ,"
         + " uf_2num(substr(log_data,51,6)) as ex_min_percent_payment2 ,"
         + " substr(log_data,24,1) as ex_mpflag ,"
         + " uf_2num(substr(log_data,8,3)) as ex_min_pay_rate ,"
         + " substr(log_data,11,6) as ex_min_pay_rate_s_month ,"
         + " substr(log_data,17,6) as ex_min_pay_rate_e_month ,"
         + " uf_2num(substr(log_data,25,14)) as ex_mp_1amt ,"
         + " substr(log_data,39,6) as ex_mp1_yms ,"
         + " substr(log_data,45,6) as ex_mp1_yme ,"
         + " chg_date ,"
			+ " time(mod_time) as update_time ,"
         + " func_code ,"
         + " substrb(dual_key,1,2) as acct_type ,"
         + " substrb(dual_key,3,11) as acct_key ,"
         + " uf_nvl(substrb(dual_key,14,3),'901') as curr_code ,"
         + " hex(rowid) as rowid  "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0704' "
      				+sqlCol(kk1,"chg_user")
      				+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC, dual_key ASC ";
   pageQuery();
   wp.setListCount(0);
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   wp.setPageValue();
   dateRead0704After();
}

void dateRead0704After() throws Exception {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "ex_mpflag", "0")) {
         wp.colSet(ii, "tt_ex_mpflag", "帳戶特殊MP % 調整");
      }
      else if (wp.colEq(ii, "ex_mpflag", "1")) {
         wp.colSet(ii, "tt_ex_mpflag", "特殊固定MP");
      }
      //--
      String lsCname =idnoCname(wp.colStr(ii,"acct_key"),wp.colStr(ii,"acct_type"));
      wp.colSet(ii,"idno_cname",lsCname);
      if (apprBankUnit(wp.colStr(ii,"chg_user"),""))
         wp.colSet(ii,"can_appr","1");
      else wp.colSet(ii,"can_appr","");
   }
}

public void dataRead0705() throws Exception {
   //-取消-
   wp.selectSQL = ""
         + " dual_key ,"
         + " chg_date ,"
         + " func_code ,"
         + " substr(log_data,1,1) as ex_special_stat_code,"
         + " chg_user ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
         + " substr(log_data,15,2) as ex_entry_dc,"
         + " substr(log_data,17,9) as ex_tip,"
         + " substr(log_data,26,40) as ex_special_comm,"
         + " substr(log_data,2,6) as ex_special_stat_s_month,"
         + " substr(log_data,8,6) as ex_special_stat_e_month,"
//				+ " update_time "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0705' "
      				+sqlCol(kk1,"chg_user")
      				+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   dateRead0705After();
   wp.setListCount(0);
   wp.setPageValue();
}

void dateRead0705After() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "ex_special_stat_code", "1")) {
         wp.colSet(ii, "tt_ex_special_stat_code", "航空");
      }
      else if (wp.colEq(ii, "ex_special_stat_code", "2")) {
         wp.colSet(ii, "tt_ex_special_stat_code", "掛號");
      }
      else if (wp.colEq(ii, "ex_special_stat_code", "3")) {
         wp.colSet(ii, "tt_ex_special_stat_code", "人工處理");
      }
      else if (wp.colEq(ii, "ex_special_stat_code", "4")) {
         wp.colSet(ii, "tt_ex_special_stat_code", "行員");
      }
      else if (wp.colEq(ii, "ex_special_stat_code", "5")) {
         wp.colSet(ii, "tt_ex_special_stat_code", "其他");
      }
   }
}

public void dataRead0706() throws Exception {
   //-actM2090-
   wp.selectSQL = ""
         + " dual_key, substr(dual_key,1,2) as acct_type,"
         +" substr(dual_key,3,11) as acct_key,"
         + " chg_date, func_code ,"
         + " substr(log_data,1,1) as ex_stat_send_paper ,"
         + " substr(log_data,2,1) as ex_stat_send_internet ,"
         + " substr(log_data,4,1) as ex_stat_send_fax ,"
         + " chg_user ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
         + " substr(log_data,6,6) as ex_stat_send_s_month ,"
         + " substr(log_data,12,6) as ex_stat_send_e_month ,"
		   + " time(mod_time) as update_time "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0706' "
      				+sqlCol(kk1,"chg_user")
      				+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
}

public void dataRead0707() throws Exception {
   wp.selectSQL = ""
         + " dual_key ,"
         + " chg_date ,"
         + " func_code ,"
         + " substr(log_data,1,1) as ex_stat_unprint_flag ,"
         + " substr(log_data,2,6) as ex_stat_unprint_s_month ,"
         + " substr(log_data,8,6) as ex_stat_unprint_e_month ,"
         + " chg_user ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
         + " lpad(' ',30) as cname "
//						 + " update_time "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0707' "
      				+sqlCol(kk1,"chg_user")
      				+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
}

public void dataRead0708() throws Exception {
   wp.selectSQL = ""
         + " dual_key ,"
         + " chg_date ,"
         + " func_code ,"
         + " substr(log_data,1,1) as ex_rc_use_b_adj ,"
         + " substr(log_data,2,1) as ex_rc_use_indicator ,"
         + " substr(log_data,3,8) as ex_rc_use_s_date ,"
         + " substr(log_data,11,8) as ex_rc_use_e_date ,"
         + " chg_user ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
		   + " time(mod_time) as update_time , "
         + " substrb(dual_key,1,2) acct_type ,"
         + " substrb(dual_key,3,11) acct_key , "
         + " hex(rowid) as rowid "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0708' "
      				+sqlCol(kk1,"chg_user")
      				+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   wp.setListCount(0);
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   wp.setPageValue();
   dateRead0708After();
}

void dateRead0708After() throws Exception {
   for (int ii = 0; ii < wp.listCount[0]; ii++) {
      String ss=wp.colStr(ii,"ex_rc_use_b_adj");
//      ss =ecsfunc.deCode_act.rc_use(ss);
      wp.colSet(ii,"tt_rc_use_b_adj",ss);

      ss=wp.colStr(ii,"ex_rc_use_indicator");
//      ss =ecsfunc.deCode_act.rc_use(ss);
      wp.colSet(ii,"tt_rc_use_indicator",ss);

      //--
      String lsCname =idnoCname(wp.colStr(ii,"acct_key"),wp.colStr(ii,"acct_type"));
      wp.colSet(ii,"idno_cname",lsCname);
      if (apprBankUnit(wp.colStr(ii,"chg_user"),""))
         wp.colSet(ii,"can_appr","1");
      else wp.colSet(ii,"can_appr","");
   }
}

public void dataRead0709() throws Exception {
   wp.selectSQL = ""
         + " dual_key ,"
         + " chg_date ,"
         + " func_code ,"
         + " substr(log_data,1,1) as ex_no_cancel_debt_flag ,"
         + " substr(log_data,2,6) as ex_no_cancel_debt_s_date ,"
         + " substr(log_data,8,6) as ex_no_cancel_debt_e_date ,"
         + " chg_user ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
         + " lpad(' ',30) as cname "
//						 + " update_time "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0709' "
      				+sqlCol(kk1,"chg_user")
      				+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by chg_date ASC ";
   pageQuery();
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
}

public void dataRead0710() throws Exception {
   wp.selectSQL = ""
         + " substrb(dual_key,14,3) curr_code ,"
         + " substrb(dual_key,1,2) acct_type ,"
         + " substrb(dual_key,3,11) acct_key ,"
         + " dual_key ,"
         + " substr(log_data,1,1) as no_int_flag ,"
         + " substr(log_data,2,6) as no_int_s_month ,"
       //+ " substr(log_data,8,6) as no_int_e_month ,"
         + " decode(substr(log_data,8,6),'999912','',substr(log_data,8,6)) as no_int_e_month ,"
         + " substr(log_data,14,1) as acct_status ,"
         + " chg_date ,"
         + " func_code ,"
         + " chg_user, time(mod_time) as update_time ,"
         + " UF_USER_NAME(chg_user) as tt_chg_user, "
         + " hex(rowid) as rowid "
   ;
   wp.daoTable = "act_dual";
   wp.whereStr = " where 1=1 "
         + " and func_code ='0710' "
      				+sqlCol(kk1,"chg_user")
      				+sqlCol(kk2,"substr(dual_key,3,10)")						
   ;
   wp.whereOrder = " order by 1 Asc, chg_date ASC ";
   pageQuery();
   wp.setListCount(0);
   if (this.sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setPageValue();
   for(int ii=0; ii<wp.listCount[0]; ii++) {
      String lsCname =idnoCname(wp.colStr(ii,"acct_key"),wp.colStr(ii,"acct_type"));
      wp.colSet(ii,"idno_cname",lsCname);
      if (apprBankUnit(wp.colStr(ii,"chg_user"),""))
         wp.colSet(ii,"can_appr","1");
      else wp.colSet(ii,"can_appr","");
   }
}

@Override
public void saveFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void procFunc() throws Exception {
   int llOk = 0, llErr = 0;
   String[] lsRowid = wp.itemBuff("rowid");
   String[] lsNoPenaltyFlag = wp.itemBuff("ex_no_penalty_flag");
   String[] lsNoPenaltySMonth = wp.itemBuff("ex_no_penalty_s_month");
   String[] lsNoPenaltyEMonth = wp.itemBuff("ex_no_penalty_e_month");
   String[] lsAcctType = wp.itemBuff("acct_type");
   String[] lsAcctKey = wp.itemBuff("acct_key");
   String[] lsAutopayIndicator = wp.itemBuff("ex_autopay_indicator");
 //String[] lsAutopayFixAmt = wp.itemBuff("ex_autopay_fix_amt");
 //String[] lsAutopayRate = wp.itemBuff("ex_autopay_rate");
   String[] lsAutopayEDate = wp.itemBuff("ex_edate");
   String[] lsLogData = wp.itemBuff("log_data");
   String[] lsOSign = wp.itemBuff("O_sign");
   String[] lsORate = wp.itemBuff("O_rate");
   String[] lsOSym = wp.itemBuff("O_sym");
   String[] lsOEym = wp.itemBuff("O_eym");
   String[] lsOSign2 = wp.itemBuff("O_sign_2");
   String[] lsORate2 = wp.itemBuff("O_rate_2");
   String[] lsOSym2 = wp.itemBuff("O_sym_2");
   String[] lsOEym2 = wp.itemBuff("O_eym_2");
   String[] lsSign1 = wp.itemBuff("sign_1");
   String[] lsIntRate1 = wp.itemBuff("int_rate_1");
   String[] lsExStartDate1 = wp.itemBuff("ex_start_date_1");
   String[] lsExEndDate1 = wp.itemBuff("ex_end_date_1");
   String[] lsSign2 = wp.itemBuff("sign_2");
   String[] lsIntRate2 = wp.itemBuff("int_rate_2");
   String[] lsExStartDate2 = wp.itemBuff("ex_start_date_2");
   String[] lsExEndDate2 = wp.itemBuff("ex_end_date_2");
   String[] lsCurrCode = wp.itemBuff("curr_code");
   String[] lsMpFlag = wp.itemBuff("ex_mpflag");
   String[] lsMinPayRate = wp.itemBuff("ex_min_pay_rate");
   String[] lsMinPayRateSMonth = wp.itemBuff("ex_min_pay_rate_s_month");
   String[] lsMinPayRateEMonth = wp.itemBuff("ex_min_pay_rate_e_month");
   String[] lsp1Amt = wp.itemBuff("ex_mp_1amt");
   String[] lsMp1Yms = wp.itemBuff("ex_mp1_yms");
   String[] lsMp1Yme = wp.itemBuff("ex_mp1_yme");
   String[] lsSpecialStatCode = wp.itemBuff("ex_special_stat_code");
   String[] lsSpecialStatSMonth = wp.itemBuff("ex_special_stat_s_month");
   String[] lsSpecialStatEMonth = wp.itemBuff("ex_special_stat_e_month");
   String[] lsEntryDc = wp.itemBuff("ex_entry_dc");
   String[] lsSpecialComm = wp.itemBuff("ex_special_comm");
   String[] lsTip = wp.itemBuff("ex_tip");
   String[] lsStatSendPaper = wp.itemBuff("ex_stat_send_paper");
   String[] lsStatSendInternet = wp.itemBuff("ex_stat_send_internet");
   String[] lsStatSendFax = wp.itemBuff("ex_stat_send_fax");
   String[] lsStatSendSMonth = wp.itemBuff("ex_stat_send_s_month");
   String[] lsStatSendEMonth = wp.itemBuff("ex_stat_send_e_month");
   String[] lsStatUnprintFlag = wp.itemBuff("ex_stat_unprint_flag");
   String[] lsStatUnprintSMonth = wp.itemBuff("ex_stat_unprint_s_month");
   String[] lsStatUnprintEMonth = wp.itemBuff("ex_stat_unprint_e_month");
   String[] lsRcUseBAdj = wp.itemBuff("ex_rc_use_b_adj");
   String[] lsRcUseIndicator = wp.itemBuff("ex_rc_use_indicator");
   String[] lsRcUseSDate = wp.itemBuff("ex_rc_use_s_date");
   String[] lsRcUseEDate = wp.itemBuff("ex_rc_use_e_date");
   String[] lsNoCancelDebtFlag = wp.itemBuff("ex_no_cancel_debt_flag");
   String[] lsNoCancelDebtSDate = wp.itemBuff("ex_no_cancel_debt_s_date");
   String[] lsNoCancelDebtEDate = wp.itemBuff("ex_no_cancel_debt_e_date");
   String[] lsNoIntFlag = wp.itemBuff("no_int_flag");
   String[] lsNoIntSMonth = wp.itemBuff("no_int_s_month");
   String[] lsNoIntEMonth = wp.itemBuff("no_int_e_month");
   String[] lsModUser = wp.itemBuff("mod_user");
   String[] liModSeqno = wp.itemBuff("mod_seqno");
   String[] lsModPgm = wp.itemBuff("mod_pgm");
   String[] lsModTime = wp.itemBuff("mod_time");
   String[] aaOpt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("rowid");
   if (optToIndex(aaOpt[0])<0) {
   	alertErr2(appMsg.optApprove);
   	return;
	}

   Actp2050Func func = new Actp2050Func();
   func.setConn(wp);

   if (eqIgno(wp.respHtml, "actp2050_detl_0704")) {
      checkCurrCode0704();
   }

   for (int ii = 0; ii < aaOpt.length; ii++) {
   	int rr =optToIndex(aaOpt[ii]);
   	if (rr<0) continue;

   	optOkflag(rr,-1);
   	if (!apprBankUnit(wp.itemStr(rr,"chg_user"), wp.loginUser)) {
   		return;
		}

      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("acct_type", lsAcctType[rr]);
      func.varsSet("acct_key", lsAcctKey[rr]);
      func.varsSet("chg_user", wp.itemStr(rr,"chg_user"));
      func.varsSet("chg_date", wp.itemStr(rr,"chg_date"));

      if (eqIgno(wp.respHtml, "actp2050_detl_0701")) {
         func.varsSet("no_penalty_flag", lsNoPenaltyFlag[rr]);
         func.varsSet("no_penalty_s_month", lsNoPenaltySMonth[rr]);
         func.varsSet("no_penalty_e_month", lsNoPenaltyEMonth[rr]);
         if (func.dataProc0701() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0702")) {
         func.varsSet("autopay_indicator", lsAutopayIndicator[rr]);
       //func.varsSet("autopay_fix_amt", lsAutopayFixAmt[rr]);
       //func.varsSet("autopay_rate", lsAutopayRate[rr]);
         func.varsSet("autopay_acct_e_date", lsAutopayEDate[rr]);
         func.varsSet("mod_user", lsModUser[rr]);
         func.varsSet("mod_seqno", liModSeqno[rr]);
         func.varsSet("mod_pgm", lsModPgm[rr]);
         func.varsSet("mod_time", lsModTime[rr]);
         if (func.dataProc0702() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0703")) {
         func.varsSet("log_data", lsLogData[rr]);
         func.varsSet("O_sign", lsOSign[rr]);
         func.varsSet("O_rate", lsORate[rr]);
         func.varsSet("O_sym", lsOSym[rr]);
         func.varsSet("O_eym", lsOEym[rr]);
         func.varsSet("O_sign_2", lsOSign2[rr]);
         func.varsSet("O_rate_2", lsORate2[rr]);
         func.varsSet("O_sym_2", lsOSym2[rr]);
         func.varsSet("O_eym_2", lsOEym2[rr]);
         func.varsSet("sign_1", lsSign1[rr]);
         func.varsSet("int_rate_1", lsIntRate1[rr]);
         func.varsSet("ex_start_date_1", lsExStartDate1[rr]);
         func.varsSet("ex_end_date_1", lsExEndDate1[rr]);
         func.varsSet("sign_2", lsSign2[rr]);
         func.varsSet("int_rate_2", lsIntRate2[rr]);
         func.varsSet("ex_start_date_2", lsExStartDate2[rr]);
         func.varsSet("ex_end_date_2", lsExEndDate2[rr]);
         if (func.dataProc0703() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0704")) {
         func.varsSet("curr_code", lsCurrCode[rr]);
         func.varsSet("mp_flag", lsMpFlag[rr]);
         func.varsSet("min_pay_rate", lsMinPayRate[rr]);
         func.varsSet("min_pay_rate_s_month", lsMinPayRateSMonth[rr]);
         func.varsSet("min_pay_rate_e_month", lsMinPayRateEMonth[rr]);
         func.varsSet("mp_1_amt", lsp1Amt[rr]);
         func.varsSet("mp_1_s_month", lsMp1Yms[rr]);
         func.varsSet("mp_1_e_month", lsMp1Yme[rr]);
         if (eqIgno(wp.colStr(rr, "proc_flag"), "N")) continue;
         if (func.dataProc0704() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0705")) {
         func.varsSet("special_stat_code", lsSpecialStatCode[rr]);
         func.varsSet("special_stat_s_month", lsSpecialStatSMonth[rr]);
         func.varsSet("special_stat_e_month", lsSpecialStatEMonth[rr]);
         func.varsSet("special_stat_division", lsEntryDc[rr]);
         func.varsSet("special_comment", lsSpecialComm[rr]);
         func.varsSet("special_stat_fee", lsTip[rr]);
         if (func.dataProc0705() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0706")) {
         func.varsSet("stat_send_paper", lsStatSendPaper[rr]);
         func.varsSet("stat_send_internet", lsStatSendInternet[rr]);
         func.varsSet("stat_send_fax", lsStatSendFax[rr]);
         func.varsSet("stat_send_s_month", lsStatSendSMonth[rr]);
         func.varsSet("stat_send_e_month", lsStatSendEMonth[rr]);

         if (func.dataProc0706() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0707")) {
         func.varsSet("stat_unprint_flag", lsStatUnprintFlag[rr]);
         func.varsSet("stat_unprint_s_month", lsStatUnprintSMonth[rr]);
         func.varsSet("stat_unprint_e_month", lsStatUnprintEMonth[rr]);
         if (func.dataProc0707() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0708")) {
         func.varsSet("rc_use_b_adj", lsRcUseBAdj[rr]);
         func.varsSet("rc_use_indicator", lsRcUseIndicator[rr]);
         func.varsSet("rc_use_s_date", lsRcUseSDate[rr]);
         func.varsSet("rc_use_e_date", lsRcUseEDate[rr]);
         if (func.dataProc0708() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0709")) {
         func.varsSet("no_cancel_debt_flag", lsNoCancelDebtFlag[rr]);
         func.varsSet("no_cancel_debt_s_date", lsNoCancelDebtSDate[rr]);
         func.varsSet("no_cancel_debt_e_date", lsNoCancelDebtEDate[rr]);
         if (func.dataProc0709() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
      else if (eqIgno(wp.respHtml, "actp2050_detl_0710")) {
         func.varsSet("curr_code", lsCurrCode[rr]);
         func.varsSet("no_interest_flag", lsNoIntFlag[rr]);
         func.varsSet("no_interest_s_month", lsNoIntSMonth[rr]);
         func.varsSet("no_interest_e_month", lsNoIntEMonth[rr]);
         if (func.dataProc0710() == 1) {
            llOk++;
            wp.colSet(rr, "ok_flag", "V");
            sqlCommit(1);
            continue;
         }
         else {
            llErr++;
            wp.colSet(rr, "ok_flag", "X");
            wp.colSet(rr, "err_msg", func.getMsg());
            dbRollback();
            continue;
         }
      }
   }

   //問題單: 0001327 >> actp2050無法覆核 2019.5.16
   String rtnMsg = "執行完成 , 成功:" + llOk + " 失敗:" + llErr;
//   if (ll_err > 0) rtnMsg = func.getMsg();
   alertMsg(rtnMsg);
   //end 0001327
//		alert_msg("執行完成 , 成功:"+ll_ok+" 失敗:"+ll_err);
//		errmsg(func.getMsg());
}

void checkCurrCode0704() {
   String lsProcFlag = "";
   String[] lsRowid = wp.itemBuff("rowid");
   String[] aaOpt = wp.itemBuff("opt");
   String[] lsCurrCode = wp.itemBuff("curr_code");
   String[] lsAcctType = wp.itemBuff("acct_type");
   String[] lsAcctKey = wp.itemBuff("acct_key");
   wp.listCount[0] = wp.itemRows("rowid");
   for (int ii = 0; ii < wp.itemRows("rowid"); ii++) {
      if (!eqIgno(lsCurrCode[ii], "901")) continue;
      lsProcFlag = "";
      if (checkBoxOptOn(ii, aaOpt)) lsProcFlag = "Y";
      else lsProcFlag = "N";
      for (int rr = 0; rr < wp.itemRows("rowid"); rr++) {
         if (!eqIgno(lsAcctType[ii], lsAcctType[rr]) || !eqIgno(lsAcctKey[ii], lsAcctKey[rr])) {
            continue;
         }
         wp.colSet(rr, "proc_flag", lsProcFlag);
      }
   }
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
