package actq01;
/**
 * 單月帳務資料查詢
 * 2021-0927   JH    分期未POSTING利息
 * 2021-0520   jh    acno.popup
 * 2020-1218.5047    JH    A5_XXX from act_curr_hst
 * 2020-0914   JH    姓名:卡號姓名(+商務卡公司名稱)
 * 2020-0831   JH    姓名:卡號姓名
 * 2020-0601   JH    modify
 * 2020-0309   JH    呆帳本金,利息,費用
 * 2020-0211   Alex  fix dataRead
 * 111-10-12   Machao     功能查詢失敗bug调整   *
 * 111-10-13   Zuwei      line 1070 wp.itemStr("ex_curr_code")   *
 * 111-10-15   Simon      1.cancel PopAcno function   *
 * 2.tabClick() alternative solution  *
 * 111-11-04   Simon      get payment_no from act_acno instead of EcsComm.getPayKey(x,x) *
 * 111-11-06   Simon      add showing act_acno.payment_no_ii *
 * 112-06-23   Simon      1.頁籤1.帳務資料顯示調整 *
 * 2.頁籤2.科目餘額顯示調整 *
 * 3.頁籤3.對帳單訊息新增顯示異常戶不列印帳單、一般不列印帳單旗標 *
 * 4.頁籤4.統計資料顯示調整 *
 * 5.分期付款Posting本金需包含授權碼空白的款項 *
 * 6.本行扣款未銷金額從頁籤3.對帳單訊息明細移至頁籤2.科目餘額明細 *
 * 2023-1225   JH    循環信用利率:act_acct_his.stmt_revol_rate
 * 2023-1227   JH    循環信用利率:(revolving_interest1 - act_acct_his.stmt_revol_rate)*365/100
 */

import ofcapp.BaseAction;
import busi.func.ColFunc;

public class Actq0020 extends BaseAction {
taroko.base.CommDate commDate = new taroko.base.CommDate();
actq01.Actq0020Func func = new actq01.Actq0020Func();
String isLastMm = "", isThisMm = "", isNextMm = "", isAcctMonth = "", isPayKey = "", isBusiDate = "";
String isThisDelayDate = "", isThisIntrDate = "", kk1 = "", kk2 = "", kk3 = "";
int ilRowCnt = -1;
String lsAcctKey = "", lsAcctType = "", lsCardNo = "", lsHistory = "";
String lsYymm = "", lsCurrCode = "", isClick = "";
String MprogName = "actq0020";

@Override
public void userAction() throws Exception {
   //-pgm-action-
   switch (wp.buttonCode) {
      case "Q":
         queryFunc();
         tabClick();
         break;
      case "S3":
         doQueryS3();
         break;
      case "S5":
         doSelectS5();
         break;
      case "S6":
         doQueryPostingAmt();
         break;
      default:
         defaultAction();
   }
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "actq0020")) {
         wp.optionKey = wp.colStr(0, "ex_acct_type");
         dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
      }
   } catch (Exception ex) {
   }

   try {
      if (eqIgno(wp.respHtml, "actq0020")) {
         wp.optionKey = wp.colStr(0, "ex_curr_code");
         dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type='DC_CURRENCY' ORDER BY uf_curr_sort(wf_id)");
      }
   } catch (Exception ex) {
   }

}

void setData() {
   wp.itemSet("ex_acct_type", lsAcctType);
   wp.itemSet("ex_acct_key", lsAcctKey);
   wp.itemSet("ex_card_no", lsCardNo);
   wp.itemSet("ex_yymm", lsYymm);
   wp.itemSet("ex_curr_code", lsCurrCode);
   wp.itemSet("ex_history", lsHistory);
   wp.itemSet("tab_click", isClick);
   wp.colSet("ex_acct_type", lsAcctType);
   wp.colSet("ex_acct_key", lsAcctKey);
   wp.colSet("ex_card_no", lsCardNo);
   wp.colSet("ex_yymm", lsYymm);
   wp.colSet("ex_curr_code", lsCurrCode);
   wp.colSet("ex_history", lsHistory);
   wp.colSet("tab_click", isClick);
}

@Override
public void queryFunc() throws Exception {

   // --keep data
   lsAcctType = wp.itemNvl("ex_acct_type", "01");
   lsAcctKey = wp.itemStr2("ex_acct_key");
   lsCardNo = wp.itemStr2("ex_card_no");
   lsYymm = wp.itemStr2("ex_yymm");
   lsCurrCode = wp.itemStr2("ex_curr_code");
   lsHistory = wp.itemStr2("ex_history");
   isClick = wp.itemStr2("tab_click");
   clearFunc();
   // --set data
   setData();

   if (!empty(lsAcctKey)) {
      lsAcctKey = commString.acctKey(lsAcctKey);
      if (lsAcctKey.length() != 11) {
         alertErr2("帳戶帳號輸入錯誤");
         return;
      }
   }

   //start-查詢權限檢查，參考【f_auth_query】
   String lmQryKey = "";
   ColFunc colfunc = new ColFunc();
   colfunc.setConn(wp);
   if (!empty(wp.itemStr2("ex_acct_key"))) {
      lmQryKey = wp.itemStr2("ex_acct_key");
   } else if (!empty(wp.itemStr2("ex_card_no"))) {
      lmQryKey = wp.itemStr2("ex_card_no");
   } else {
      lmQryKey = "";
   }

   if (colfunc.fAuthQuery(MprogName, lmQryKey) != 1) {
      alertErr2(colfunc.getMsg());
      return;
   }
   //end-查詢權限檢查，參考【f_auth_query】

   func.setConn(wp);
   // --select busi_date
   String sql0 = " select business_date "
       +" from ptr_businday "
       +commSqlStr.rownum(1);
   sqlSelect(sql0, new Object[]{});
   isBusiDate = sqlStr("business_date");

   // --cond check
   if (!empty(lsAcctKey)) {
      String sql1 = " select "
          +" stmt_cycle as ex_stmt_cycle , "
          +" p_seqno as ex_p_seqno , "
          +" uf_acno_name(p_seqno) as ex_acno_name, "
          +" uf_corp_name(corp_p_seqno) as ex_corp_name, "
          +" payment_no as ex_payment_no , "
          +" payment_no_ii as ex_payment_no_ii , "
          +" acct_type as ex_acct_type , "
          +" acct_key as ex_acct_key "
          +" from act_acno "
          +" where acct_key = ? and acct_type =? ";
      sqlSelect(sql1, new Object[]{lsAcctKey, wp.itemStr("ex_acct_type")});
   } else if (!wp.itemEmpty("ex_card_no")) {
      //-200831:cardNo=附卡, show附卡姓名---
      String sql2 = " select "
          +" A.stmt_cycle as ex_stmt_cycle , "
          +" A.p_seqno as ex_p_seqno , "
          +" A.payment_no as ex_payment_no , "
          +" A.payment_no_ii as ex_payment_no_ii , "
          +" A.acct_type as ex_acct_type , "
          +" A.acct_key as ex_acct_key , "
          +" uf_idno_name(B.card_no) as ex_acno_name,"
          +" uf_corp_name(B.corp_p_seqno) as ex_corp_name"
          +" from act_acno A join crd_card B on A.acno_p_seqno=B.acno_p_seqno"
          +" where B.card_no = ? ";
      sqlSelect(sql2, new Object[]{wp.itemStr2("ex_card_no")});
   } else {
      alertErr2("帳戶帳號, 卡號不可同時空白");
      return;
   }
   if (sqlRowNum <= 0) {
      alertErr2("帳戶帳號 or 卡號: 輸入錯誤");
      return;
   }

   wp.colSet("ex_stmt_cycle", sqlStr("ex_stmt_cycle"));
   wp.colSet("ex_p_seqno", sqlStr("ex_p_seqno"));
   wp.colSet("ex_acct_type", sqlStr("ex_acct_type"));
   wp.colSet("ex_acct_key", sqlStr("ex_acct_key"));
   wp.colSet("ex_acno_name", sqlStr("ex_acno_name"));
   if (!empty(sqlStr("ex_corp_name"))) {
      wp.colSet("ex_acno_name", sqlStr("ex_acno_name")+" ("+sqlStr("ex_corp_name")+")");
   }

   // --關帳年月
   String sql3 = " select "
       +" last_acct_month as is_last_mm , "
       +" this_acct_month as is_this_mm , "
       +" next_acct_month as is_next_mm , "
       +" this_delaypay_date as is_this_delay_date , "
       +" this_interest_date as is_this_intr_date "
       +" from ptr_workday "
       +" where stmt_cycle = ? ";
   sqlSelect(sql3, new Object[]{wp.colStr("ex_stmt_cycle")});
   if (sqlRowNum <= 0) {
      alertErr("%s Cycle 不存在 ", wp.itemStr("ex_stmt_cycle"));
      return;
   }

   isLastMm = sqlStr("is_last_mm");
   isThisMm = sqlStr("is_this_mm");
   isNextMm = sqlStr("is_next_mm");
   isThisDelayDate = sqlStr("is_this_delay_date");
   isThisIntrDate = sqlStr("is_this_intr_date");

   wp.colSet("ex_last_mm", isLastMm);
   wp.colSet("ex_this_mm", isThisMm);
   wp.colSet("ex_next_mm", isNextMm);
   wp.colSet("ex_delay_date", isThisDelayDate);
   wp.colSet("ex_intr_date", isThisIntrDate);

   // --is_acct_month
   if (eqIgno(wp.itemStr("ex_history"), "N")) { // --當期
      isAcctMonth = isThisMm;
      wp.colSet("ex_yymm", "");
   } else {
      isAcctMonth = wp.itemStr("ex_yymm");
   }
   wp.colSet("ex_acct_month", isAcctMonth);
   if (eqIgno(wp.itemStr("ex_history"), "Y") && empty(isAcctMonth)) {
      alertErr2("非當期, 請輸入關帳年月");
      return;
   }

   if (chkStrend(isAcctMonth, isThisMm) == false) {
      alertErr2("輸入關帳年月尚未發生!");
      return;
   }

   func.wfSaveInqlog(0, "0");
   readTab1();
   if (rc != 1) return;

   readTab2();
   readTab3();
   readTab4();
   readTab5();
   readTab6();
   readTab7();
   readTab8();
   readTab9();
   // --結清總應繳款=已通知期末/目前餘額+未通知期末/目前餘額+分期付款未Posting本金+RC轉分期未Posting利息+預算結清利息+預算結清違約金
   double ldAdvTotal = 0;
   if (wp.itemEq("ex_history", "N")) {
      ldAdvTotal = wp.colNum("A2_tl_bill_end_bal")+wp.colNum("A2_tl_unbill_end_bal")+wp.colNum("A1_db_inst_unpost")
          +wp.colNum("A1_db_aofee")+wp.colNum("A1_db_adv_intr")+wp.colNum("A1_db_adv_penl");
   }
   wp.colSet("A1_db_adv_total", ldAdvTotal);
}

public void readTab1() throws Exception {
   String lsPseqno = wp.colStr("ex_p_seqno");
   // --pay_type+paykey
   //busi.func.EcsComm ecs = new busi.func.EcsComm();
   //ecs.setConn(wp);
   //isPayKey = ecs.getPayKey(wp.colStr("ex_acct_type"), lsPseqno);
   //wp.colSet("A1_wk_pay", isPayKey);
   wp.colSet("A1_wk_pay", sqlStr("ex_payment_no"));
   wp.colSet("A1_wk_pay_2", sqlStr("ex_payment_no_ii"));

   double lm_amt = 0;
   // --當期:
   if (eqIgno(wp.itemStr("ex_history"), "N") ||
       (eqIgno(wp.itemStr("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm))) {
      daoTid = "A1_";
      String sql1 = " select *"
          +" from act_acct "
          +" where p_seqno = ? ";
      sqlSelect(sql1, new Object[]{lsPseqno});
      if (sqlRowNum <= 0) {
         //wp.notFound = "N";
         alertErr2("帳務主檔中無此帳戶資料");
         return;
      }

      //若是鍵入總繳個人戶之帳號或卡號查詢時，需再以總帳戶ex_p_seqno讀取總帳戶acct_key
      daoTid = "A1_";
      String sql_1a = " select acct_key from act_acno "
          +" where acno_p_seqno = ? ";
      sqlSelect(sql_1a, new Object[]{lsPseqno});

      wp.colSet("A1_acct_type", sqlStr("A1_acct_type"));
      wp.colSet("A1_acct_key", sqlStr("A1_acct_key"));
      wp.colSet("A1_pay_by_stage_date", sqlStr("A1_pay_by_stage_date"));
      wp.colSet("A1_last_payment_date", sqlStr("A1_last_payment_date"));
      wp.colSet("A1_last_min_pay_date", sqlStr("A1_last_min_pay_date"));
      wp.colSet("A1_last_cancel_debt_date", sqlStr("A1_last_cancel_debt_date"));
      wp.colSet("A1_acct_jrnl_bal", sqlStr("A1_acct_jrnl_bal"));
      if (sqlNum("A1_acct_jrnl_bal") < 0) {
         wp.colSet("A1_bal_style", "col_key");
      } else {
         wp.colSet("A1_bal_style", "dsp_text");
      }
      wp.colSet("A1_rc_min_pay", sqlStr("A1_rc_min_pay"));
      wp.colSet("A1_rc_min_pay_bal", sqlStr("A1_rc_min_pay_bal"));
      wp.colSet("A1_rc_min_pay_m0", sqlStr("A1_rc_min_pay_m0"));
      wp.colSet("A1_pay_by_stage_amt", sqlStr("A1_pay_by_stage_amt"));
      wp.colSet("A1_pay_by_stage_bal", sqlStr("A1_pay_by_stage_bal"));

      // --ibm_pay
      daoTid = "A1_";
      String sql2 = " select "
          +" nvl(sum(txn_amt),0) as db_ibm_pay "
          +" from act_pay_ibm "
          +" where p_seqno = ? "
          +" and value_date = ? "
          +" and proc_mark <> 'Y' "
          +" and error_code in ('','0') ";
      sqlSelect(sql2, new Object[]{lsPseqno, isBusiDate});

      wp.colSet("A1_db_ibm_pay", sqlStr("A1_db_ibm_pay"));

      // --inst_unpost
      daoTid = "A1_";
      String sql3 = " select "
          +" sum((install_tot_term - install_curr_term) * unit_price + remd_amt + decode(install_curr_term,0,first_remd_amt+extra_fees,0)) as db_inst_unpost "
          +" from bil_contract "
          +" where p_seqno = ? "
          +" and install_tot_term <> install_curr_term "
          +" and contract_kind = '1' "
          //+ " and auth_code not in ('','N','REJECT','P','reject') "
          +" and auth_code not in ('N','REJECT','P','reject') "
          +" and apr_date<>'' "  //" and post_cycle_dd > 0 "
          ;
      sqlSelect(sql3, new Object[]{lsPseqno});
      wp.colSet("A1_db_inst_unpost", sqlStr("A1_db_inst_unpost"));

      // --db_aut2_pay
      daoTid = "A1_";
      String sql4 = " select "
          +" nvl(sum(pay_amt),0) as db_aut2_pay "
          +" from act_pay_detail "
          +" where p_seqno = ? "
          //+ " and payment_type ='COU1' ";
          +" and payment_type ='AUT2' ";
      sqlSelect(sql4, new Object[]{lsPseqno});
      wp.colSet("A1_db_aut2_pay", sqlStr("A1_db_aut2_pay"));

      // --ach_total
      daoTid = "A1_";
      String sql5 = " select "
          +" nvl(sum(pay_amt),0) as db_ach_total "
          +" from act_pay_detail "
          +" where p_seqno = ? "
          //+ " and batch_no like '%1002%' ";
          +" and payment_type in ('ACH1') ";
      sqlSelect(sql5, new Object[]{lsPseqno});
      wp.colSet("A1_db_ach_total", sqlStr("A1_db_ach_total"));

      //--db_cou_pay
      daoTid = "A1_";
      String sql6 = " select "
          +" sum(pay_amt) as db_cou_pay "
          +" from act_pay_detail "
          +" where p_seqno = ? "
          //+ " and payment_type in ('COU2','COU3','COU4','COU5','COU6') ";
          +" and payment_type not in ('AUT1','AUT2','ACH1') ";
      sqlSelect(sql6, new Object[]{lsPseqno});
      wp.colSet("A1_db_cou_pay", sqlStr("A1_db_cou_pay"));

      //--db_ibm_pay_other
      daoTid = "A1_";
      String sql7 = " select "
          +" nvl(sum(txn_amt),0) as db_ibm_pay_other "
          +" from act_pay_ibm "
          +" where p_seqno = ? "
          +" and value_date in "
          +" (select value_date "
          +" from act_pay_ibm "
          +" where p_seqno = ? "
          +" and value_date > ? "
          +" and proc_mark <> 'Y' "
          +" and error_code in ('','0')"
          +" ) ";
      sqlSelect(sql7, new Object[]{lsPseqno, lsPseqno, isBusiDate});
      wp.colSet("A1_db_ibm_pay_other", sqlStr("A1_db_ibm_pay_other"));

      // --wf_close_enq
      func.varsSet("is_busi_date", isBusiDate);
      func.varsSet("is_this_delay_date", isThisDelayDate);
      func.varsSet("is_p_seqno", lsPseqno);
      func.varsSet("is_next_mm", isNextMm);
      func.varsSet("is_this_intr_date", isThisIntrDate);
      func.varsSet("is_this_mm", isThisMm);
      func.varsSet("is_acct_type", wp.colStr("ex_acct_type"));
      func.wfCloseEnq();
      //--
   } else if (eqIgno(wp.itemStr("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm)) {
      daoTid = "A1_";
      String sql8 = " select "
          +" p_seqno ,"
          +" acct_month ,"
          +" acct_type ,"
          +" uf_acno_key(p_seqno) as acct_key ,"
          +" id_p_seqno ,"
          +" acct_jrnl_bal ,"
          +" min_pay ,"
          +" min_pay_bal ,"
          +" rc_min_pay ,"
          +" rc_min_pay_bal ,"
          +" rc_min_pay_m0 ,"
          +" autopay_beg_amt ,"
          +" autopay_bal ,"
          +" pay_by_stage_amt ,"
          +" pay_by_stage_bal ,"
          +" pay_by_stage_date ,"
          +" payment_status ,"
          +" last_payment_date ,"
          +" last_min_pay_date ,"
          +" last_cancel_debt_date ,"
          +" 0.00 as db_pay_uncan ,"
          +" 0.00 as db_ibm_pay ,"
          +" 0.00 as db_inst_unpost ,"
          +" '' as pay_type ,"
          +" '' as pay_key ,"
          +" 0.00 as db_adv_intr ,"
          +" 0.00 as db_adv_penl ,"
          +" 0.00 as db_adv_total ,"
          +" 0.00 as db_ach_total ,"
          //+ " 0.00 as db_post_pay ,"
          +" 0.00 as db_aut2_pay ,"
          +" 0.00 as db_cou_pay ,"
          +" 0.00 as db_aofee ,"
          +" ''as db_value_date ,"
          +" 0.00 db_ibm_pay_other "
          +" from act_acct_hst "
          +" where 1=1 "
          +" and p_seqno = ? "
          +" and acct_month = ? ";
      sqlSelect(sql8, new Object[]{lsPseqno, isAcctMonth});
      if (sqlRowNum <= 0) {
         //wp.notFound = "N";
         alertErr2("歷史帳務主檔中無此帳戶資料");
         return;
      }
      wp.colSet("A1_acct_type", wp.colStr("ex_acct_type"));
      sql2wp("A1_acct_key");
      wp.colSet("A1_pay_by_stage_date", sqlStr("A1_pay_by_stage_date"));
      wp.colSet("A1_last_payment_date", sqlStr("A1_last_payment_date"));
      wp.colSet("A1_last_min_pay_date", sqlStr("A1_last_min_pay_date"));
      wp.colSet("A1_last_cancel_debt_date", sqlStr("A1_last_cancel_debt_date"));
      wp.colSet("A1_acct_jrnl_bal", sqlStr("A1_acct_jrnl_bal"));
      wp.colSet("A1_rc_min_pay", sqlStr("A1_rc_min_pay"));
      wp.colSet("A1_rc_min_pay_bal", sqlStr("A1_rc_min_pay_bal"));
      wp.colSet("A1_rc_min_pay_m0", sqlStr("A1_rc_min_pay_m0"));
      wp.colSet("A1_pay_by_stage_amt", sqlStr("A1_pay_by_stage_amt"));
      wp.colSet("A1_pay_by_stage_bal", sqlStr("A1_pay_by_stage_bal"));
      wp.colSet("A1_db_inst_unpost", "0");
   }

   //-RC轉分期未Posting利息:A1_db_aofee-
   //   ecsfunc.Bil_contract oo_cont=new Bil_contract();
   //   oo_cont.setConn(wp);
   //   oo_cont.calc_ITintr(ls_pseqno);
   //   wp.col_set("A1_db_aofee",oo_cont.im_IT_interest);

   func.wfSaveInqlog(1, "1");
}

public void readTab2() throws Exception {
   // --read acct
   if (eqIgno(wp.itemStr("ex_history"), "N") ||
       (eqIgno(wp.itemStr("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm))) {
      daoTid = "A2_";
      String sql1 = " select "
          +" acct_jrnl_bal ,"
          +" beg_bal_lk ,"
          +" end_bal_lk ,"
          +" beg_bal_op ,"
          +" end_bal_op ,"
          +" adi_beg_bal ,"
          +" adi_end_bal ,"
          +" adi_d_avail ,"
          +" overpay_lock_sta_date ,"
          +" overpay_lock_due_date "
          +" from act_acct "
          +" where p_seqno = ? ";
      sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno")});
      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         func.wfSaveInqlog(0, "2");
         return;
      }
      wp.colSet("A2_acct_jrnl_bal", sqlStr("A2_acct_jrnl_bal"));
      wp.colSet("A2_beg_bal_lk", sqlStr("A2_beg_bal_lk"));
      wp.colSet("A2_end_bal_lk", sqlStr("A2_end_bal_lk"));
      wp.colSet("A2_beg_bal_op", sqlStr("A2_beg_bal_op"));
      wp.colSet("A2_end_bal_op", sqlStr("A2_end_bal_op"));
      wp.colSet("A2_adi_beg_bal", sqlStr("A2_adi_beg_bal"));
      wp.colSet("A2_adi_end_bal", sqlStr("A2_adi_end_bal"));
      wp.colSet("A2_adi_d_avail", sqlStr("A2_adi_d_avail"));
      wp.colSet("A2_overpay_lock_sta_date", sqlStr("A2_overpay_lock_sta_date"));
      wp.colSet("A2_overpay_lock_due_date", sqlStr("A2_overpay_lock_due_date"));

      // --wf_read_debt
      func.varsSet("is_p_seqno", wp.colStr("ex_p_seqno"));
      func.varsSet("is_last_mm", isLastMm);
      func.varsSet("is_this_mm", isThisMm);
      func.varsSet("is_next_mm", isNextMm);
      func.wfReadDebt();
      //-act_curr.OP/LK-
      getEndBalOplk(wp.colStr("ex_p_seqno"));
      // --
   } else if ((eqIgno(wp.itemStr("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm))) {
      // --歷史+關帳後
      daoTid = "A2_";
      String sql1 = " select "
          +" * "
          +" from act_acct_hst "
          +" where 1=1 "
          +" and p_seqno = ? "
          +" and acct_month = ? "
          +" ";
      sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), isAcctMonth});
      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         func.wfSaveInqlog(0, "2");
         return;
      }

      wp.colSet("A2_acct_jrnl_bal", sqlStr("A2_acct_jrnl_bal"));
      wp.colSet("A2_adi_beg_bal", sqlStr("A2_adi_beg_bal"));
      wp.colSet("A2_adi_end_bal", sqlStr("A2_adi_end_bal"));
      wp.colSet("A2_adi_d_avail", sqlStr("A2_adi_d_avail"));
      wp.colSet("A2_beg_bal_op", sqlStr("A2_beg_bal_op"));
      wp.colSet("A2_end_bal_op", sqlStr("A2_end_bal_op"));
      wp.colSet("A2_beg_bal_lk", sqlStr("A2_beg_bal_lk"));
      wp.colSet("A2_end_bal_lk", sqlStr("A2_end_bal_lk"));
      wp.colSet("A2_overpay_lock_sta_date", sqlStr("A2_overpay_lock_sta_date"));
      wp.colSet("A2_overpay_lock_due_date", sqlStr("A2_overpay_lock_due_date"));
      String[] aa_col = new String[]{"af", "lf", "cf", "pf", "bl", "ca", "it", "id", "ri", "pn", "ot", "ai", "sf", "dp", "cb", "ci", "cc", "db", "db_b", "db_i", "db_c", "ao"};
      for (String cc : aa_col) {
         wp.colSet("A2_billed_beg_bal_"+cc, sqlStr("A2_billed_beg_bal_"+cc));
         wp.colSet("A2_billed_end_bal_"+cc, sqlStr("A2_billed_end_bal_"+cc));
         wp.colSet("A2_unbill_beg_bal_"+cc, sqlStr("A2_unbill_beg_bal_"+cc));
         wp.colSet("A2_unbill_end_bal_"+cc, sqlStr("A2_unbill_end_bal_"+cc));
      }
      //-歷史:act_curr.OP/LK-
      getEndBalOplk("");
   }

   double A2_tl_bill_beg_bal = 0, A2_tl_bill_end_bal = 0, A2_tl_unbill_beg_bal = 0, A2_tl_unbill_end_bal = 0;
   String[] aa_col = new String[]{"af", "lf", "cf", "pf", "bl", "ca", "it", "id", "ri", "pn", "ot", "ai", "sf", "dp", "cb", "ci", "cc", "db", "db_b", "db_i", "db_c", "ao"};
   for (String cc : aa_col) {
      A2_tl_bill_beg_bal += wp.colNum("A2_billed_beg_bal_"+cc);
      A2_tl_bill_end_bal += wp.colNum("A2_billed_end_bal_"+cc);
      A2_tl_unbill_beg_bal += wp.colNum("A2_unbill_beg_bal_"+cc);
      A2_tl_unbill_end_bal += wp.colNum("A2_unbill_end_bal_"+cc);
   }
   wp.colSet("A2_tl_bill_beg_bal", A2_tl_bill_beg_bal);
   wp.colSet("A2_tl_bill_end_bal", A2_tl_bill_end_bal);
   wp.colSet("A2_tl_unbill_beg_bal", A2_tl_unbill_beg_bal);
   wp.colSet("A2_tl_unbill_end_bal", A2_tl_unbill_end_bal);

   func.wfSaveInqlog(2, "2");
}

void getEndBalOplk(String a_pseqno) throws Exception {
   wp.colSet("A2_end_op", 0);
   wp.colSet("A2_end_op_us", 0);
   wp.colSet("A2_end_op_jp", 0);
   wp.colSet("A2_end_lk", 0);
   wp.colSet("A2_end_lk_us", 0);
   wp.colSet("A2_end_lk_jp", 0);
   if (empty(a_pseqno)) return;

   String sql1 = "select end_bal_lk, end_bal_op"
       +" from act_acct_curr where p_seqno=? and curr_code=?";
   //--
   sqlSelect(sql1, new Object[]{a_pseqno, "901"});
   if (sqlRowNum > 0) {
      wp.colSet("A2_end_op", sqlNum("end_bal_op"));
      wp.colSet("A2_end_lk", sqlNum("end_bal_lk"));
   }
   sqlSelect(sql1, new Object[]{a_pseqno, "840"});
   if (sqlRowNum > 0) {
      wp.colSet("A2_end_op_us", sqlNum("end_bal_op"));
      wp.colSet("A2_end_lk_us", sqlNum("end_bal_lk"));
   }
   sqlSelect(sql1, new Object[]{a_pseqno, "392"});
   if (sqlRowNum > 0) {
      wp.colSet("A2_end_op_jp", sqlNum("end_bal_op"));
      wp.colSet("A2_end_lk_jp", sqlNum("end_bal_lk"));
   }
}

public void readTab3() throws Exception {
   if (eqIgno(wp.itemStr("ex_history"), "N") ||
       (eqIgno(wp.itemStr("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm))) {
      daoTid = "A3_";
      wp.selectSQL = ""
          +" curr_code"
          +", dc_acct_jrnl_bal as acct_jrnl_bal "
      ;
      wp.daoTable = " act_acct_curr ";
      wp.whereStr = " where 1=1 "
          +sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
          +sqlCol(wp.itemStr("ex_curr_code"), "curr_code")
      ;
      wp.whereOrder = " order by curr_code Desc ";
      pageQuery();
      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         func.wfSaveInqlog(0, "3");
         return;
      }
      for (int ii = 0; ii < wp.selectCnt; ii++) {
         // --billed_beg_bal

         if (!empty(isLastMm)) {
            String sql1 = " select "
                +" acct_month , "
                +" billed_end_bal_af + unbill_end_bal_af + "
                +" billed_end_bal_lf + unbill_end_bal_lf + "
                +" billed_end_bal_pf + unbill_end_bal_pf + "
                +" billed_end_bal_bl + unbill_end_bal_bl + "
                +" billed_end_bal_ca + unbill_end_bal_ca + "
                +" billed_end_bal_it + unbill_end_bal_it + "
                +" billed_end_bal_id + unbill_end_bal_id + "
                +" billed_end_bal_ri + unbill_end_bal_ri + "
                +" billed_end_bal_pn + unbill_end_bal_pn + "
                +" billed_end_bal_ao + unbill_end_bal_ao + "
                +" billed_end_bal_ai + unbill_end_bal_ai + "
                +" billed_end_bal_sf + unbill_end_bal_sf + "
                +" billed_end_bal_dp + unbill_end_bal_dp + "
                +" billed_end_bal_cb + unbill_end_bal_cb + "
                +" billed_end_bal_ci + unbill_end_bal_ci + "
                +" billed_end_bal_cf + unbill_end_bal_cf + "
                +" billed_end_bal_db + unbill_end_bal_db + "
                +" billed_end_bal_cc + unbill_end_bal_cc + "
                +" billed_end_bal_ot + unbill_end_bal_ot as billed_beg_bal "
                +" from act_curr_hst "
                +" where p_seqno = ? "
                +" and curr_code = ? "
                +" and acct_month = ?";
            sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), wp.colStr(ii, "A3_curr_code"), isLastMm});
            if (sqlRowNum <= 0) {
               wp.notFound = "N";
               wp.colSet(ii, "A3_billed_beg_bal", "0");
            } else {
               wp.colSet(ii, "A3_billed_beg_bal", sqlStr("billed_beg_bal"));
            }
         }

         // --billed_end_bal
         if (!empty(isThisMm)) {
            String sql2 = " select "
                +" sum("
                +commSqlStr.ufunc("uf_dc_amt2(end_bal,dc_end_bal)) as billed_end_bal ")
                +" from act_debt "
                +" where p_seqno = ? "
                +" and acct_month <= ? "
                +" and curr_code = ? "
                +" and acct_code in ('AF','LF','CF','PF','BL','CA','ID','IT','RI','PN','AO','AI','SF','DP','CB','CI','CC','DB','OT')";
            sqlSelect(sql2, new Object[]{wp.colStr("ex_p_seqno"), isThisMm, wp.colStr(ii, "A3_curr_code")});
            if (sqlRowNum <= 0) {
               wp.notFound = "N";
               wp.colSet(ii, "A3_billed_end_bal", "0");
            } else {
               wp.colSet(ii, "A3_billed_end_bal", sqlStr("billed_end_bal"));
            }
         }

         // --unbill_beg_bal, unbill_end_bal
         if (!empty(isNextMm)) {
            daoTid = "A3_";
            String sql3 = " select "
                +" sum(uf_dc_amt2(beg_bal,dc_beg_bal)) as unbill_beg_bal ,"
                +" sum(uf_dc_amt2(end_bal,dc_end_bal)) as unbill_end_bal "
                +" from act_debt "
                +" where p_seqno = ? "
                //+ " and acct_month <= ? "  modified on 2019/07/16
                +" and acct_month = ? "
                +" and curr_code = ? "
                +" and acct_code in ('AF','LF','CF','PF','BL','CA','ID','IT','RI','PN','AO','AI','SF','DP','CB','CI','CC','DB','OT') ";
            sqlSelect(sql3, new Object[]{wp.colStr("ex_p_seqno"), isNextMm, wp.colStr(ii, "A3_curr_code")});
            if (sqlRowNum <= 0) {
               wp.notFound = "N";
               wp.colSet(ii, "A3_unbill_beg_bal", "0");
               wp.colSet(ii, "A3_unbill_end_bal", "0");
            } else {
               wp.colSet(ii, "A3_unbill_beg_bal", sqlStr("A3_unbill_beg_bal"));
               wp.colSet(ii, "A3_unbill_end_bal", sqlStr("A3_unbill_end_bal"));
            }
         }
      }

   } else if (eqIgno(wp.itemStr("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm)) {
      daoTid = "A3_";
      // xx=[af,lf,pf,bl,ca,it,id,ri,pn,ao,ai,sf,dp,cb,ci,cf,db,cc,ot]
      wp.selectSQL = ""
          +" curr_code , "
          +" acct_jrnl_bal ,"
          +" billed_beg_bal_af + billed_beg_bal_lf + billed_beg_bal_pf + "
          +" billed_beg_bal_bl + billed_beg_bal_ca + billed_beg_bal_it + "
          +" billed_beg_bal_ri + billed_beg_bal_pn + billed_beg_bal_ao + "
          +" billed_beg_bal_ai + billed_beg_bal_sf + billed_beg_bal_dp + "
          +" billed_beg_bal_cb + billed_beg_bal_ci + billed_beg_bal_cf + billed_beg_bal_id+"
          +" billed_beg_bal_db + billed_beg_bal_cc + billed_beg_bal_ot as billed_beg_bal ,"
          +" billed_end_bal_af + billed_end_bal_lf + billed_end_bal_pf + "
          +" billed_end_bal_bl + billed_end_bal_ca + billed_end_bal_it + "
          +" billed_end_bal_ri + billed_end_bal_pn + billed_end_bal_ao + "
          +" billed_end_bal_ai + billed_end_bal_sf + billed_end_bal_dp + "
          +" billed_end_bal_cb + billed_end_bal_ci + billed_end_bal_cf + billed_end_bal_id+"
          +" billed_end_bal_db + billed_end_bal_cc + billed_end_bal_ot as billed_end_bal , "
          +" unbill_beg_bal_af + unbill_beg_bal_lf + unbill_beg_bal_pf + "
          +" unbill_beg_bal_bl + unbill_beg_bal_ca + unbill_beg_bal_it + "
          +" unbill_beg_bal_ri + unbill_beg_bal_pn + unbill_beg_bal_ao + "
          +" unbill_beg_bal_ai + unbill_beg_bal_sf + unbill_beg_bal_dp + "
          +" unbill_beg_bal_cb + unbill_beg_bal_ci + unbill_beg_bal_cf + unbill_beg_bal_id+"
          +" unbill_beg_bal_db + unbill_beg_bal_cc + unbill_beg_bal_ot as unbill_beg_bal , "
          +" unbill_end_bal_af + unbill_end_bal_lf + unbill_end_bal_pf + "
          +" unbill_end_bal_bl + unbill_end_bal_ca + unbill_end_bal_it + "
          +" unbill_end_bal_ri + unbill_end_bal_pn + unbill_end_bal_ao + "
          +" unbill_end_bal_ai + unbill_end_bal_sf + unbill_end_bal_dp + "
          +" unbill_end_bal_cb + unbill_end_bal_ci + unbill_end_bal_cf + unbill_end_bal_id+"
          +" unbill_end_bal_db + unbill_end_bal_cc + unbill_end_bal_ot as unbill_end_bal "
      ;
      wp.daoTable = " act_curr_hst ";
      wp.whereStr = " where 1=1 "
          +sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
          +sqlCol(wp.itemStr("ex_curr_code"), "curr_code")
          +sqlCol(isAcctMonth, "acct_month")
      ;
      wp.whereOrder = " order by curr_code Desc ";
      pageQuery();
      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         func.wfSaveInqlog(0, "3");
         return;
      }

      //--查詢上個月的期末相加蓋掉這個月的期初
      int ilRows = 0;
      ilRows = wp.selectCnt;
      String lsTempMonth = "";
      lsTempMonth = commString.mid(commDate.dateAdd(isAcctMonth, 0, -1, 0), 0, 6);
      String sql1 = " select "
          +" billed_end_bal_af + billed_end_bal_lf + billed_end_bal_pf + "
          +" billed_end_bal_bl + billed_end_bal_ca + billed_end_bal_it + "
          +" billed_end_bal_ri + billed_end_bal_pn + billed_end_bal_ao + "
          +" billed_end_bal_ai + billed_end_bal_sf + billed_end_bal_dp + "
          +" billed_end_bal_cb + billed_end_bal_ci + billed_end_bal_cf + "
          +" billed_end_bal_db + billed_end_bal_cc + billed_end_bal_ot +billed_end_bal_id+ "
          +" unbill_end_bal_af + unbill_end_bal_lf + unbill_end_bal_pf + "
          +" unbill_end_bal_bl + unbill_end_bal_ca + unbill_end_bal_it + "
          +" unbill_end_bal_ri + unbill_end_bal_pn + unbill_end_bal_ao + "
          +" unbill_end_bal_ai + unbill_end_bal_sf + unbill_end_bal_dp + "
          +" unbill_end_bal_cb + unbill_end_bal_ci + unbill_end_bal_cf +unbill_end_bal_id+ "
          +" unbill_end_bal_db + unbill_end_bal_cc + unbill_end_bal_ot as last_end_bal "
          +" from act_curr_hst "
          +" where p_seqno = ? "
          +" and curr_code = ? "
          +" and acct_month = ? ";
      for (int ii = 0; ii < ilRows; ii++) {
         sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), wp.colStr(ii, "A3_curr_code"), lsTempMonth});
         if (sqlRowNum > 0) {
            wp.colSet(ii, "A3_billed_beg_bal", sqlNum("last_end_bal"));
         }
      }
   }
   wp.listCount[0] = wp.selectCnt;
   func.wfSaveInqlog(3, "3");
}

public void readTab4() throws Exception {
   daoTid = "A4_";
   String sql1 = " select "
       +" stmt_payment_no , "
       +" stmt_cycle_date , "
       +" stmt_auto_pay_date , "
       +" stmt_auto_pay_amt , "
       +" stmt_credit_limit , "
       +" stmt_revol_rate , "
       +" stmt_last_ttl , "
       +" stmt_payment_amt , "
       +" stmt_adjust_amt , "
       +" stmt_new_amt , "
       +" stmt_this_ttl_amt , "
       +" stmt_mp , "
       +" stmt_last_payday , "
       +" stmt_last_month_bp , "
       +" stmt_new_add_bp , "
       +" stmt_adjust_bp , "
       +" stmt_use_bp , "
       +" stmt_give_bp , "
       +" stmt_net_bp , "
       +" stmt_auto_pay_bank , "
       +" stmt_auto_pay_no , "
       +" stmt_give_reason1 , "
       +" stmt_give_reason2 , "
       +" stmt_give_reason3 , "
       +" stmt_give_reason4 , "
       +" unprint_flag , "
       +" unprint_flag_regular , "
       +" stmt_over_due_amt "
       +" from act_acct_hst "
       +" where 1=1 "
       +" and p_seqno = ? "
       +" and acct_month = ? ";

   if (eqIgno(wp.itemStr("ex_history"), "N")) {
      sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), isLastMm});
   } else if (eqIgno(wp.itemStr("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm)) {
      sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), isLastMm});
   } else if (eqIgno(wp.itemStr("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm)) {
      String lsLastDate = "";
      lsLastDate = commDate.dateAdd(wp.itemStr("ex_yymm"), 0, -1, 0).substring(0, 6);
      sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), lsLastDate});

   }

   if (sqlRowNum <= 0) {
      wp.notFound = "N";
      func.wfSaveInqlog(0, "4");
      return;
   }

   wp.colSet("A4_stmt_payment_no", sqlStr("A4_stmt_payment_no"));
   wp.colSet("A4_stmt_cycle_date", sqlStr("A4_stmt_cycle_date"));
   wp.colSet("A4_stmt_auto_pay_date", sqlStr("A4_stmt_auto_pay_date"));
   wp.colSet("A4_stmt_auto_pay_amt", sqlStr("A4_stmt_auto_pay_amt"));
   wp.colSet("A4_stmt_credit_limit", sqlStr("A4_stmt_credit_limit"));
   wp.colSet("A4_stmt_revol_rate", sqlStr("A4_stmt_revol_rate"));
   wp.colSet("A4_stmt_last_ttl", sqlStr("A4_stmt_last_ttl"));
   wp.colSet("A4_stmt_payment_amt", sqlStr("A4_stmt_payment_amt"));
   wp.colSet("A4_stmt_adjust_amt", sqlStr("A4_stmt_adjust_amt"));
   wp.colSet("A4_stmt_new_amt", sqlStr("A4_stmt_new_amt"));
   wp.colSet("A4_stmt_this_ttl_amt", sqlStr("A4_stmt_this_ttl_amt"));
   wp.colSet("A4_stmt_mp", sqlStr("A4_stmt_mp"));
   wp.colSet("A4_stmt_last_payday", sqlStr("A4_stmt_last_payday"));
   wp.colSet("A4_stmt_last_month_bp", sqlStr("A4_stmt_last_month_bp"));
   wp.colSet("A4_stmt_new_add_bp", sqlStr("A4_stmt_new_add_bp"));
   wp.colSet("A4_stmt_adjust_bp", sqlStr("A4_stmt_adjust_bp"));
   wp.colSet("A4_stmt_use_bp", sqlStr("A4_stmt_use_bp"));
   wp.colSet("A4_stmt_give_bp", sqlStr("A4_stmt_give_bp"));
   wp.colSet("A4_stmt_net_bp", sqlStr("A4_stmt_net_bp"));
   wp.colSet("A4_stmt_auto_pay_bank", sqlStr("A4_stmt_auto_pay_bank"));
   wp.colSet("A4_stmt_auto_pay_no", sqlStr("A4_stmt_auto_pay_no"));
   wp.colSet("A4_stmt_give_reason1", sqlStr("A4_stmt_give_reason1"));
   wp.colSet("A4_stmt_give_reason2", sqlStr("A4_stmt_give_reason2"));
   wp.colSet("A4_stmt_give_reason3", sqlStr("A4_stmt_give_reason3"));
   wp.colSet("A4_stmt_give_reason4", sqlStr("A4_stmt_give_reason4"));
   wp.colSet("A4_stmt_over_due_amt", sqlStr("A4_stmt_over_due_amt"));
   wp.colSet("A5_stmt_acct_month", isAcctMonth);
   wp.colSet("A5_unprint_flag", sqlStr("A4_unprint_flag"));
   wp.colSet("A5_unprint_flag_regular", sqlStr("A4_unprint_flag_regular"));
   func.wfSaveInqlog(4, "4");
   return;
}

public void readTab5() throws Exception {
   String lsPseqno = wp.colStr("ex_p_seqno");
   if (empty(lsPseqno)) return;

   if (eqIgno(wp.itemStr("ex_history"), "N") ||
       eqIgno(wp.itemStr("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm)) {

      daoTid = "A5_";
      wp.selectSQL = "acct_month, "
          +" curr_code , "
          +" stmt_mp , "
          +" min_pay_bal , "
          +" stmt_auto_pay_bank , "
          +" stmt_auto_pay_no , "
          +" stmt_auto_pay_date, "+
          " stmt_this_ttl_amt"
      ;
      wp.daoTable = " act_curr_hst ";
      wp.whereStr = " where 1=1 "
          +sqlCol(lsPseqno, "p_seqno")
          +sqlCol(isLastMm, "acct_month")
      ;
      wp.whereOrder = " order by curr_code Desc ";
      pageQuery();

      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         daoTid = "A5_";
         wp.selectSQL = "'' as acct_month, curr_code ,"
             +" min_pay as stmt_mp ,"
             +" min_pay_bal ,"
             +" autopay_acct_bank ,"
             +" autopay_acct_no, "
             +" 0 as stmt_this_ttl_amt"
         ;
         wp.daoTable = " act_acct_curr ";
         wp.whereStr = " where 1=1 "
             +sqlCol(lsPseqno, "p_seqno")
         ;
         wp.whereOrder = " order by curr_code Desc ";
         pageQuery();
         if (sqlRowNum <= 0) {
            wp.notFound = "N";
            func.wfSaveInqlog(5, "0");
            return;
         }
      }

      int llNrow = wp.selectCnt;
      String sql1 = " select "
          +" sum(uf_dc_amt2(pay_amt,dc_pay_amt)) as db_pay_uncan "
          +" from act_pay_detail "
          +" where p_seqno = ? "
          +" and uf_nvl(curr_code,'901') = ? "
          +" and payment_type = 'AUT1' ";
      for (int ii = 0; ii < llNrow; ii++) {
         daoTid = "A5_";
         sqlSelect(sql1, new Object[]{lsPseqno, wp.itemStr2("ex_curr_code")});
         if (sqlRowNum <= 0) {
            wp.colSet(ii, "A4_db_pay_uncan", "0");
         } else {
            wp.colSet(ii, "A4_db_pay_uncan", sqlStr("A5_db_pay_uncan"));
         }
         wp.colSet(ii, "A5_stmt_payment_no", wp.colStr("A4_stmt_payment_no"));
      }

   } else if (eqIgno(wp.itemStr("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm)) {
      String lsLastDate = "";
      lsLastDate = commDate.dateAdd(wp.itemStr("ex_yymm"), 0, -1, 0).substring(0, 6);
      daoTid = "A5_";
      wp.selectSQL = "acct_month, curr_code , "
          +" stmt_mp , min_pay_bal , "
          +" stmt_auto_pay_bank , "
          +" stmt_auto_pay_no , "
          +" stmt_auto_pay_date,"
          +" stmt_this_ttl_amt"
      ;
      wp.daoTable = " act_curr_hst ";
      wp.whereStr = " where 1=1 "
          +sqlCol(lsPseqno, "p_seqno")
          +sqlCol(lsLastDate, "acct_month")
      ;
      wp.whereOrder = " order by curr_code Desc ";
      pageQuery();
      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         wp.selectSQL = "'' as acct_month,"
             +"uf_curr_sort(curr_code) as curr_sort , curr_code,"
             +"0 as stmt_mp , min_pay_bal ,"
             +" autopay_acct_bank ,"
             +" autopay_acct_no,"
             +" 0 as stmt_this_ttl_amt"
         ;
         wp.daoTable = " act_acct_curr ";
         wp.whereStr = " where 1=1 "
             +sqlCol(lsPseqno, "p_seqno")
         ;
         wp.whereOrder = " order by 2 Desc ";
         daoTid = "A5_";
         pageQuery();
         if (sqlRowNum <= 0) {
            wp.notFound = "N";
            func.wfSaveInqlog(5, "5");
            return;
         }
      }
      for (int ii = 0; ii < wp.selectCnt; ii++) {
         wp.colSet(ii, "A5_stmt_payment_no", wp.colStr("A4_stmt_payment_no"));
      }
   }
   wp.listCount[1] = wp.selectCnt;
   if (wp.listCount[1] > 1) {
      wp.alertMesg("雙幣卡客戶, 請注意帳務幣別");
   }
   //-台幣-
   //   String sql2 ="select stmt_mp , min_pay_bal ," +
//	         " stmt_auto_pay_bank, stmt_auto_pay_no, stmt_auto_pay_date," +
//	         " stmt_this_ttl_amt " +
//	         " from act_acct_hst" +
//	         " where p_seqno =? and acct_month=?";
   //   for(int ll=0; ll<wp.listCount[1]; ll++) {
//	      String ls_acctMon =wp.col_ss(ll,"A5_acct_month");
//	      if (empty(ls_acctMon)) continue;
//	      String ls_curr =wp.col_ss("A5_curr_code");
//	      if (eq_igno(ls_curr,"901")==false) continue;
//	      ppp(1,ls_pseqno);
//	      ppp(ls_acctMon);
//	      sqlSelect(sql2);
//	      if (sql_nrow >0) {
//	         //wp.col_set(ll,"A5_stmt_mp",sql_num("stmt_mp"));
//	         wp.col_set(ll,"A5_stmt_auto_pay_bank",sql_ss("stmt_auto_pay_bank"));
//	         wp.col_set(ll,"A5_stmt_auto_pay_no",sql_ss("stmt_auto_pay_no"));
//	         wp.col_set(ll,"A5_stmt_auto_pay_date",sql_ss("stmt_auto_pay_date"));
//	         wp.col_set(ll,"A5_stmt_this_ttl_amt",sql_num("stmt_this_ttl_amt"));
//	      }
   //   }
}

public void readTab6() throws Exception {

   if (eqIgno(wp.itemStr("ex_history"), "N") ||
       (eqIgno(wp.itemStr("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm))) {
      daoTid = "A6_";
      String sql1 = " select "
          +" pay_amt as his_pay_amt , "
          +" pay_cnt as his_pay_cnt , "
          +" adjust_dr_amt as his_adj_dr_amt , "
          +" adjust_cr_amt as his_adj_cr_amt , "
          +" adjust_dr_cnt as his_adj_dr_cnt , "
          +" adjust_cr_cnt as his_adj_cr_cnt "
          +" from act_acct "
          +" where p_seqno = ? ";
      sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno")});
      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         func.wfSaveInqlog(0, "6");
         return;
      }

   } else if (eqIgno(wp.itemStr("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm)) {
      daoTid = "A6_";
      String sql1 = " select"
          +" his_purchase_cnt , "
          +" his_purchase_amt , "
          +" his_pur_no_m2 , "
          +" his_cash_cnt , "
          +" his_cash_amt , "
          +" his_pay_percentage , "
          +" his_rc_percentage , "
          +" his_pay_amt , "
          +" his_pay_cnt , "
          +" his_adj_dr_amt , "
          +" his_adj_cr_amt , "
          +" his_adj_dr_cnt , "
          +" his_adj_cr_cnt "
          +" from act_anal_sub "
          +" where 1=1 "
          +" and p_seqno = ? "
          +" and acct_month = ? ";
      sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), isAcctMonth});
      if (sqlRowNum <= 0) {
         wp.notFound = "N";
         func.wfSaveInqlog(0, "6");
         return;
      }
   }
   wp.colSet("A6_his_purchase_cnt", sqlStr("A6_his_purchase_cnt"));
   wp.colSet("A6_his_purchase_amt", sqlStr("A6_his_purchase_amt"));
   wp.colSet("A6_his_pur_no_m2", sqlStr("A6_his_pur_no_m2"));
   wp.colSet("A6_his_cash_cnt", sqlStr("A6_his_cash_cnt"));
   wp.colSet("A6_his_cash_amt", sqlStr("A6_his_cash_amt"));
   wp.colSet("A6_his_pay_percentage", sqlStr("A6_his_pay_percentage"));
   wp.colSet("A6_his_rc_percentage", sqlStr("A6_his_rc_percentage"));
   wp.colSet("A6_his_pay_amt", sqlStr("A6_his_pay_amt"));
   wp.colSet("A6_his_pay_cnt", sqlStr("A6_his_pay_cnt"));
   wp.colSet("A6_his_adj_dr_amt", sqlStr("A6_his_adj_dr_amt"));
   wp.colSet("A6_his_adj_cr_amt", sqlStr("A6_his_adj_cr_amt"));
   wp.colSet("A6_his_adj_dr_cnt", sqlStr("A6_his_adj_dr_cnt"));
   wp.colSet("A6_his_adj_cr_cnt", sqlStr("A6_his_adj_cr_cnt"));
   func.wfSaveInqlog(6, "6");
   return;
}

public void readTab7() throws Exception {
   sumA7Amt();
   daoTid = "A7_";
   wp.selectSQL = ""
       +"uf_curr_sort(curr_code) as curr_sort ,"
       +"seqno ,"
       +"uf_dc_amt2(end_bal,dc_end_bal) as wk_end_bal ,"
       +"uf_dc_amt2(mp_amt,dc_mp_amt) as wk_mp_amt ,"
       +" acct_month ,"
       +" stmt_cycle ,"
       +" acct_code ,"
       +" current_item ,"
       +" mp_desc ,"
       +" end_bal ,"
       +" mcht_no ,"
       +" mp_rate ,"
       +" line_of_credit_amt ,"
       +" mp_amt ,"
       +" exceed_flag ,"
       +" unpost_installment ,"
       +" uf_dc_curr(curr_code) as curr_code ,"
       +" dc_end_bal ,"
       +" dc_mp_amt "
   ;
   wp.daoTable = " cyc_mp_method ";
   wp.whereStr = " where 1=1 "
       +sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
       +sqlCol(isAcctMonth, "acct_month")
       +sqlCol(wp.itemStr("ex_curr_code"), "curr_code")
   ;
   wp.whereOrder = " order by 1,2";
   pageQuery();
   if (sqlRowNum <= 0) {
      wp.notFound = "N";
      func.wfSaveInqlog(0, "7");
      return;
   }
   wp.listCount[2] = wp.selectCnt;
   func.wfSaveInqlog(7, "7");
}

void sumA7Amt() throws Exception {
   String sql1 = " select "
       +" sum(uf_dc_amt2(mp_amt,dc_mp_amt)) as A7_tl_wk_mp_amt , "
       +" count(*) as A7_tl_cnt "
       +" from cyc_mp_method "
       +" where 1=1 "
//	        + sqlCol(wp.colStr("ex_p_seqno"), "p_seqno") + sqlCol(isAcctMonth, "acct_month")     原程式寫法運行後發現無法獲得參數，故修改
       +" and p_seqno = ? "
       +" and acct_month = ? "
       +" and curr_code = ?  ";

   sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), isAcctMonth, "901"});

   wp.colSet("A7_tl_cnt_tw", sqlStr("A7_tl_cnt"));
   wp.colSet("A7_tl_wk_mp_amt_tw", sqlStr("A7_tl_wk_mp_amt"));

   sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), isAcctMonth, "840"});

   wp.colSet("A7_tl_cnt_a", sqlStr("A7_tl_cnt"));
   wp.colSet("A7_tl_wk_mp_amt_a", sqlStr("A7_tl_wk_mp_amt"));

   sqlSelect(sql1, new Object[]{wp.colStr("ex_p_seqno"), isAcctMonth, "392"});

   wp.colSet("A7_tl_cnt_jp", sqlStr("A7_tl_cnt"));
   wp.colSet("A7_tl_wk_mp_amt_jp", sqlStr("A7_tl_wk_mp_amt"));
}

public void readTab8() throws Exception {
   daoTid = "A8_";
   wp.selectSQL = " acct_month ,"
       +" real_int_rate ,"
       +" standard_int_rate ,"
       +" revolve_rate_s_date , "
       +" revolve_rate_e_date , "
       +" proc_desc , "
       +" real_int_rate_2 , "
       +" real_int_rate_3 , "
       +" revolve_rate_s_date_2 , "
       +" revolve_rate_e_date_2 , "
       +" proc_desc2 "
   ;
   wp.daoTable = " cyc_acno_rate ";
   wp.whereStr = " where 1=1 "
       +sqlCol(wp.colStr("ex_p_seqno"), "p_seqno")
       +sqlCol(isAcctMonth, "acct_month", "<=");
   wp.whereOrder = " order by acct_month Desc ";
   pageQuery();
   if (sqlRowNum <= 0) {
      wp.notFound = "N";
      func.wfSaveInqlog(8, "0");
      return;
   }
   wp.listCount[3] = wp.selectCnt;
   func.wfSaveInqlog(8, "8");
}

public void readTab9() throws Exception {

   if (wp.colStr("ex_acct_key").length() < 10) return;
   String lsIdno = wp.colStr("ex_acct_key").substring(0, 10);

   wfReadColLiac(lsIdno);//前協
   wfReadColCpbdue(lsIdno);//債協,個協,前調
   wfReadColLiadRenew(lsIdno);//更生
   wfReadColLiadLiquidate(lsIdno);//清算

//	    wfReadColLiab();
//	    wfReaCcolLiac();
//	    wfReadColLiadRenew();
//	    wfReadColLiadLiquidate();
   if (wp.selectCnt <= 0) func.wfSaveInqlog(0, "9");
   else func.wfSaveInqlog(9, "9");
   wp.setListCount(5);
   listwkTab9((wp.selectCnt));
}

void listwkTab9(int ilCnt) {
   for (int ii = 0; ii < ilCnt; ii++) {

      wp.colSet(ii, "ser_num", String.format("%02d", (ii+1)));

      if (wp.colEq(ii, "A9_ex_type", "LIAB")) {
         wp.colSet(ii, "A9_ex_type", "債務協商");
      } else if (wp.colEq(ii, "A9_ex_type", "LIAC")) {
         wp.colSet(ii, "A9_ex_type", "前置協商");
      } else if (wp.colEq(ii, "A9_ex_type", "LIAD3")) {
         wp.colSet(ii, "A9_ex_type", "更生計劃");
      } else if (wp.colEq(ii, "A9_ex_type", "LIAD4")) {
         wp.colSet(ii, "A9_ex_type", "清算");
      }
	      
	      /*

	      if (wp.colEq(ii, "A9_ex_status", "LIAB-1")) {
	        wp.colSet(ii, "A9_ex_status", "停催");
	      } else if (wp.colEq(ii, "A9_ex_status", "LIAB-2")) {
	        wp.colSet(ii, "A9_ex_status", "復催");
	      } else if (wp.colEq(ii, "A9_ex_status", "LIAB-3")) {
	        wp.colSet(ii, "A9_ex_status", "協商成功 ");
	      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-1")) {
	        wp.colSet(ii, "A9_ex_status", "受理申請");
	      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-2")) {
	        wp.colSet(ii, "A9_ex_status", "停催通知");
	      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-3")) {
	        wp.colSet(ii, "A9_ex_status", "簽約完成");
	      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-4")) {
	        wp.colSet(ii, "A9_ex_status", "結案/復催");
	      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-5")) {
	        wp.colSet(ii, "A9_ex_status", "正常結案");
	      }
	      */

      if (wp.colEq(ii, "A9_ex_status", "LIAB-1")) {
         wp.colSet(ii, "A9_ex_status", "1.受理申請");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAB-2")) {
         wp.colSet(ii, "A9_ex_status", "2.停催通知");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAB-3")) {
         wp.colSet(ii, "A9_ex_status", "3.簽約成功");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAB-4")) {
         wp.colSet(ii, "A9_ex_status", "4.結案/復催");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAB-5")) {
         wp.colSet(ii, "A9_ex_status", "5.結案/毀諾");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAB-6")) {
         wp.colSet(ii, "A9_ex_status", "6.結案/結清");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-1")) {
         wp.colSet(ii, "A9_ex_status", "1.受理申請");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-2")) {
         wp.colSet(ii, "A9_ex_status", "2.停催通知");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-3")) {
         wp.colSet(ii, "A9_ex_status", "3.簽約成功");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-4")) {
         wp.colSet(ii, "A9_ex_status", "4.結案/復催");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-5")) {
         wp.colSet(ii, "A9_ex_status", "5.結案/毀諾");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAC-6")) {
         wp.colSet(ii, "A9_ex_status", "6.結案/結清");
      } else if (wp.colEq(ii, "A9_ex_status", "RENEW-1")) {
         wp.colSet(ii, "A9_ex_status", "1.更生開始");
      } else if (wp.colEq(ii, "A9_ex_status", "RENEW-2")) {
         wp.colSet(ii, "A9_ex_status", "2.更生撤回");
      } else if (wp.colEq(ii, "A9_ex_status", "RENEW-3")) {
         wp.colSet(ii, "A9_ex_status", "3.更生認可");
      } else if (wp.colEq(ii, "A9_ex_status", "RENEW-4")) {
         wp.colSet(ii, "A9_ex_status", "4.更生履行完畢");
      } else if (wp.colEq(ii, "A9_ex_status", "RENEW-5")) {
         wp.colSet(ii, "A9_ex_status", "5.更生裁定免責");
      } else if (wp.colEq(ii, "A9_ex_status", "RENEW-6")) {
         wp.colSet(ii, "A9_ex_status", "6.更生調查程序");
      } else if (wp.colEq(ii, "A9_ex_status", "RENEW-7")) {
         wp.colSet(ii, "A9_ex_status", "7.更生駁回");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-A")) {
         wp.colSet(ii, "A9_ex_status", "A.清算程序開始");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-B")) {
         wp.colSet(ii, "A9_ex_status", "B.清算程序終止(結)");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-C")) {
         wp.colSet(ii, "A9_ex_status", "C.清算程序開始同時終止");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-D")) {
         wp.colSet(ii, "A9_ex_status", "D.清算撤銷免責確定");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-E")) {
         wp.colSet(ii, "A9_ex_status", "E.清算調查程序");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-F")) {
         wp.colSet(ii, "A9_ex_status", "F.清算駁回");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-G")) {
         wp.colSet(ii, "A9_ex_status", "G.清算撤回");
      } else if (wp.colEq(ii, "A9_ex_status", "LIAD-H")) {
         wp.colSet(ii, "A9_ex_status", "H.清算復權");
      }

   }


}

/*Cpbdue當前協商*/
void wfReadColCpbdue(String lsIdno) {
   wp.selectSQL = "select "+" decode(cpbdue_type,'1','公會協商','2','個別協商','3','前置調解','') as cpbdue_type,"+" cpbdue_curr_type as ls_status,"+" decode(cpbdue_bank_type,'3',cpbdue_begin_date,cpbdue_upd_dte) as ls_rcv_date "
       +" from col_cpbdue"+" where cpbdue_acct_type='01' "
       +" and cpbdue_id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";

   sqlSelect(wp.selectSQL, new Object[]{lsIdno});

   log("Cpbdue3_aaa:"+lsIdno);

   if (sqlRowNum <= 0 || empty(sqlStr("ls_status"))) {
      log("Cpbdue3_bbb: not found"+lsIdno);
      return;
   }

   //wp.colSet(wp.selectCnt, "wk_type", "個別協商");
   wp.colSet(wp.selectCnt, "A9_ex_type", sqlStr("cpbdue_type"));
   wp.colSet(wp.selectCnt, "A9_ex_status", "LIAB-"+sqlStr("ls_status")); //定義與債協相同
   wp.colSet(wp.selectCnt, "A9_ex_rcv_date", sqlStr("ls_rcv_date"));
   wp.colSet(wp.selectCnt, "A9_ex_end_date", sqlStr("ls_end_date"));
   wp.selectCnt++;
}

/*前協*/
void wfReadColLiac(String lsIdno) {
   String sql1 = "select "+" liac_status as ls_status,"+" notify_date as ls_rcv_date,"
       +" end_date as ls_end_date "+" from col_liac_nego "
       +" where id_p_seqno =(select id_p_seqno from crd_idno where id_no=? ) ";
   sqlSelect(sql1, new Object[]{lsIdno});

   log("liac_aaa:"+lsIdno);

   if (sqlRowNum <= 0) {
      String sql2 = "select "+" max(notify_date) as ls_notify_date "+" from col_liac_nego_hst "
          +" where id_p_seqno= (select id_p_seqno from crd_idno where id_no=? ) "+" and liac_status ='6' ";
      sqlSelect(sql2, new Object[]{lsIdno});

      log("liac_bbb:"+lsIdno);

      if (sqlRowNum <= 0 || empty(sqlStr("ls_notify_date"))) {
         log("liac_ccc: not found"+lsIdno);
         return;
      }

      String sql3 = "select "+" liac_status as ls_status , "+" notify_date as ls_rcv_date , "
          +" end_date as ls_end_date "+" from col_liac_nego_hst "+" where 1=1 "
          //+ " and id_no=? ," + " and liac_status ='6' ," + " and notify_date = ? "
          +" and id_p_seqno= (select id_p_seqno from crd_idno where id_no=? ),"
          +" and liac_status ='6' ,"+" and notify_date = ? "
          +" fetch first 1 rows only ";
      sqlSelect(sql3, new Object[]{lsIdno, sqlStr("ls_notify_date")});

      log("liac_ddd:"+lsIdno);

      if (sqlRowNum <= 0)
         log("liac_ddd: not found"+lsIdno);
      return;
   }

   if (empty(sqlStr("ls_status"))) {
      return;
   }
   wp.colSet(wp.selectCnt, "A9_ex_type", "前置協商");
   wp.colSet(wp.selectCnt, "A9_ex_status", "LIAC-"+sqlStr("ls_status"));
   wp.colSet(wp.selectCnt, "A9_ex_rcv_date", sqlStr("ls_rcv_date"));
   if (!eqIgno(sqlStr("ls_status"), "6")) {
      wp.colSet(wp.selectCnt, "A9_ex_end_date", "");
   } else {
      wp.colSet(wp.selectCnt, "A9_ex_end_date", sqlStr("ls_end_date"));
   }
   wp.selectCnt++;
}


/*更生*/

void wfReadColLiadRenew(String lsIdno) {
   String sql1 = "select "+" max(status_date) as ls_rcv_date  "+" from col_liad_renewliqui "
       +" where 1=1 "+" and  liad_type='3' and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";
   sqlSelect(sql1, new Object[]{lsIdno});

   log("LiadRenew_aaa:"+lsIdno);

   if (empty(sqlStr("ls_rcv_date"))) {
      log("LiadRenew_bbb: not found"+lsIdno);
      return;
   }

//		    String sql2 = "select " + " a.court_status||'.'||b.wf_desc as ls_status ,"
//		        + " a.confirm_date as ls_end_date " + " from ptr_sys_idtab b, col_liad_renew a  "
//		        + " where 1=1 " + " and a.court_status = b.wf_id " + " and a.id_no = ? "
//		        + " and a.recv_date = ? " + " and b.wf_type='LIAD_RENEW_STATUS' "
//		        + " fetch first 1 rows only ";
   String sql2 = "select liad_status as ls_status,status_date as ls_rcv_date from col_liad_renewliqui "
       +"where liad_type='3' "
       +"and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) "
       +"and status_date = ? "
       +"order by status_date desc "
       +" fetch first 1 rows only ";

   sqlSelect(sql2, new Object[]{lsIdno, sqlStr("ls_rcv_date")});

   log("LiadRenew_bbb:"+lsIdno);

   if (sqlRowNum <= 0) {
      log("LiadRenew_ccc: not found"+lsIdno);
      return;
   }

   wp.colSet(wp.selectCnt, "A9_ex_type", "更生計劃");
   wp.colSet(wp.selectCnt, "A9_ex_status", "RENEW-"+sqlStr("ls_status"));
   wp.colSet(wp.selectCnt, "A9_ex_rcv_date", sqlStr("ls_rcv_date"));
   wp.colSet(wp.selectCnt, "A9_ex_end_date", sqlStr("ls_end_date"));
   wp.selectCnt++;

}

/*清算*/

void wfReadColLiadLiquidate(String lsIdno) {
   String sql1 = "select "+" max(status_date) as ls_rcv_date  "+" from col_liad_renewliqui "
       +" where 1=1 "+" and  liad_type='4' and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) ";
   sqlSelect(sql1, new Object[]{lsIdno});

   log("Liquidate_aaa:"+lsIdno);

   if (empty(sqlStr("ls_rcv_date"))) {
      return;
   }

//		    String sql2 = "select " + " a.court_status||'.'||b.wf_desc as ls_status ,"
//		        + " a.confirm_date as ls_end_date " + " from ptr_sys_idtab b, col_liad_renew a  "
//		        + " where 1=1 " + " and a.court_status = b.wf_id " + " and a.id_no = ? "
//		        + " and a.recv_date = ? " + " and b.wf_type='LIAD_RENEW_STATUS' "
//		        + " fetch first 1 rows only ";
   String sql2 = "select liad_status as ls_status,status_date as ls_rcv_date from col_liad_renewliqui "
       +"where liad_type='4' "
       +"and id_p_seqno = (select id_p_seqno from crd_idno where id_no=?) "
       +"and status_date = ? "
       +"order by status_date desc "
       +" fetch first 1 rows only ";

   sqlSelect(sql2, new Object[]{lsIdno, sqlStr("ls_rcv_date")});

   log("Liquidate_bbb:"+lsIdno);

   if (sqlRowNum <= 0) {
      return;
   }

   wp.colSet(wp.selectCnt, "A9_ex_type", "清算");
   wp.colSet(wp.selectCnt, "A9_ex_status", "LIAD-"+sqlStr("ls_status"));
   wp.colSet(wp.selectCnt, "A9_ex_rcv_date", sqlStr("ls_rcv_date"));
   wp.colSet(wp.selectCnt, "A9_ex_end_date", sqlStr("ls_end_date"));
   wp.selectCnt++;

}

/*
//	  void wfReadColLiab() throws Exception {
//	    if (wp.colStr("ex_acct_key").length() < 10) return;
//	    String lsId = wp.colStr("ex_acct_key").substring(0, 10);
//	    daoTid = "LIAB_";
//	    wp.selectSQL = " liab_status as ex_status , " 
//	    + " notify_date as ex_rcv_date "
//	    ;
//	    wp.daoTable = " col_liab_nego ";
//	    wp.whereStr = " where 1=1 " 
//	    + sqlCol(lsId, "id_no");
//	    wp.whereOrder = " order by decode(liab_status,'2','a','3','b','c') ";
//
//	    pageQuery();
//	    if (sqlRowNum <= 0) {
//	      wp.notFound = "N";
//	      daoTid = "LIAB_";
//	      wp.selectSQL = " liab_status as ex_status , " 
//	      + " liab_end_date as ex_end_date "
//	     ;
//	      wp.daoTable = " act_acno ";
//	      wp.whereStr = " where 1=1 " 
//	      + sqlCol(wp.colStr("ex_p_seqno"), "p_seqno");
//	      pageQuery();
//	      if (sqlRowNum <= 0) {
//	        wp.notFound = "N";
//	      }
//	    }
//
//	    for (int ii = 0; ii < wp.selectCnt; ii++) {
//	      if (empty(wp.colStr(ii, "LIAB_ex_status"))) continue;
//	      ilRowCnt++;
//	      if (ilRowCnt < 9) {
//	        wp.colSet(ilRowCnt, "ser_num", "0" + (ilRowCnt + 1));
//	      } else {
//	        wp.colSet(ilRowCnt, "ser_num", "" + (ilRowCnt + 1));
//	      }
//	      wp.colSet(ilRowCnt, "A9_ex_type", "LIAB");
//	      wp.colSet(ilRowCnt, "A9_ex_status", "LIAB-" + wp.colStr(ii, "LIAB_ex_status"));
//	      wp.colSet(ilRowCnt, "A9_ex_rcv_date", wp.colStr(ii, "LIAB_ex_rcv_date"));
//	      wp.colSet(ilRowCnt, "A9_ex_end_date", wp.colStr(ii, "LIAB_ex_end_date"));
//	    }
//
//
//	  }
//
//	  void wfReaCcolLiac() throws Exception {
//	    if (wp.colStr("ex_acct_key").length() < 10) return;
//	    String lsId = wp.colStr("ex_acct_key").substring(0, 10);
//
//	    daoTid = "LIAC_";
//	    wp.selectSQL = " liac_status as ex_status , " 
//	    //+ " notify_date as ex_rcv_date , "
//			+ " file_date as ex_rcv_date , " 
//	        + " end_date as ex_end_date "
//	        ;
//	    wp.daoTable = " col_liac_nego ";
//	    wp.whereStr = " where 1=1 " 
//	    + sqlCol(lsId, "id_no")
//	    ;
//	    pageQuery();
//	    if (sqlRowNum < 0) {
//	      wp.notFound = "N";
//	      daoTid = "LIAC_";
//	      wp.selectSQL = " max(notify_date) as ls_noty_date  ";
//	      wp.daoTable = " col_liac_nego_hst ";
//	      wp.whereStr = " where 1=1 and liac_status = '5' " 
//	          + sqlCol(lsId, "id_no");
//	      if (sqlRowNum <= 0) {
//	        wp.notFound = "N";
//	        return;
//	      }
//
//	      daoTid = "LIAC_";
//	      wp.selectSQL = " liac_status as ex_status ," 
////	      + " notify_date as ex_rcv_date , "
//			  + " file_date as ex_rcv_date , "
//	          + " end_date as ex_end_date "
//	          ;
//	      wp.daoTable = " col_liac_nego_hst ";
//	      wp.whereStr = " where 1=1 and liac_status='5' " 
//	      + sqlCol(lsId, "id_no") 
//	      + commSqlStr.rownum(1);
//	      pageQuery();
//	      if (sqlRowNum <= 0) {
//	        wp.notFound = "N";
//	        return;
//	      }
//	    }
//
//	    for (int ii = 0; ii < wp.selectCnt; ii++) {
//	      if (empty(wp.colStr(ii, "LIAC_ex_status"))) continue;
//	      ilRowCnt++;
//	      wp.colSet(ilRowCnt, "A9_ex_type", "LIAC");
//	      wp.colSet(ilRowCnt, "A9_ex_status", "LIAC-" + wp.colStr(ii, "LIAC_ex_status"));
//	      wp.colSet(ilRowCnt, "A9_ex_rcv_date", wp.colStr(ii, "LIAC_ex_rcv_date"));
//	      if (eqIgno(wp.colStr(ii, "LIAC_ex_status"), "5")) {
//	        wp.colSet(ilRowCnt, "A9_ex_end_date", wp.colStr(ii, "LIAC_ex_end_date"));
//	      } else {
//	        wp.colSet(ilRowCnt, "A9_ex_end_date", "");
//	      }
//	      if (ilRowCnt < 9) {
//	        wp.colSet(ilRowCnt, "ser_num", "0" + (ilRowCnt + 1));
//	      } else {
//	        wp.colSet(ilRowCnt, "ser_num", "" + (ilRowCnt + 1));
//	      }
//	    }
//
//	  }
//
//	  void wfReadColLiadRenew() throws Exception {
//	    if (wp.colStr("ex_acct_key").length() < 10)  return;
//	    String lsId = wp.colStr("ex_acct_key").substring(0, 10);
//
//	    daoTid = "LIAD3_";
//	  //wp.selectSQL = " a.court_status||'.'||"
//	    wp.selectSQL = " a.renew_status||'.'||"
//	     //+ zzsql.ufunc("uf_tt_idtab('LIAD_RENEW_STATUS',court_status) as ex_status , ")//此function 抓 ptr_sys_idtab
//	       + " b.id_desc as ex_status , "
//	       + " a.recv_date as ex_rcv_date, "
//	     //+ " a.confirm_date as ex_end_date "
//	     //+ " a.renew_damage_date as ex_end_date "
//	 			+ "(case when a.renew_status = '2' then a.RENEW_CANCEL_DATE "
//	 			+ " when a.renew_status = '3' and a.RUN_RENEW_FLAG = 'Y' then a.DELIVER_DATE "
//	 			+ " when a.renew_status = '3' and a.RUN_RENEW_FLAG != 'Y' then a.RENEW_DAMAGE_DATE "
//	 			+ " when a.renew_status = '4' then a.DELIVER_DATE "
//	 			+ "	when a.renew_status = '7' then a.RENEW_DAMAGE_DATE else '' end) as ex_end_date "
//	    ;
//	    wp.daoTable = " col_liad_renew a left join col_liab_idtab b on b.id_key = '3' "
//	                + " and a.renew_status = b.id_code ";
//	    wp.whereStr = " where 1=1 "
//	                + sqlCol(lsId, "a.id_no");
//	    wp.whereOrder = " order by recv_date desc " + commSqlStr.rownum(1);
//	    pageQuery();    
//	    if (sqlRowNum <= 0) {
//	      wp.notFound = "N";
//	      return;
//	    }
//	    for (int ii = 0; ii < wp.selectCnt; ii++) {
//	      ilRowCnt++;
//	      wp.colSet(ilRowCnt, "A9_ex_type", "LIAD3");
//	      wp.colSet(ilRowCnt, "A9_ex_status", wp.colStr(ii, "LIAD3_ex_status"));
//	      wp.colSet(ilRowCnt, "A9_ex_rcv_date", wp.colStr(ii, "LIAD3_ex_rcv_date"));
//	      wp.colSet(ilRowCnt, "A9_ex_end_date", wp.colStr(ii, "LIAD3_ex_end_date"));
//	      if (ilRowCnt < 9) {
//	        wp.colSet(ilRowCnt, "ser_num", "0" + (ilRowCnt + 1));
//	      } else {
//	        wp.colSet(ilRowCnt, "ser_num", "" + (ilRowCnt + 1));
//	      }
//	    }
//
//	  }
//
//	  void wfReadColLiadLiquidate() throws Exception {
//	    if (empty(wp.colStr("ex_p_seqno"))) return;
//	    if (wp.colStr("ex_acct_key").length() < 10) return;
//	    String lsId = wp.colStr("ex_acct_key").substring(0, 10);
//	    daoTid = "LIAD4_";
//	    wp.selectSQL = " a.court_status||'.'||"
//	        + commSqlStr.ufunc("uf_tt_idtab('LIAD_LIQU_STATUS',court_status) as ex_status")
//	        + ", a.recv_date as ex_rcv_date "
//	        ;
//	    wp.daoTable = " col_liad_liquidate a ";
//	    wp.whereStr = " where 1=1 "
//	    + sqlCol(lsId, "a.id_no")
//	   ;
//	    wp.whereOrder = " order by a.recv_date desc " + commSqlStr.rownum(1);
//	    pageQuery();
//	    if (sqlRowNum <= 0) {
//	      wp.notFound = "N";
//	      return;
//	    }
//	    for (int ii = 0; ii < wp.selectCnt; ii++) {
//	      ilRowCnt++;
//	      wp.colSet(ilRowCnt, "A9_ex_type", "LIAD4");
//	      wp.colSet(ilRowCnt, "A9_ex_status", wp.colStr(ii, "LIAD4_ex_status"));
//	      wp.colSet(ilRowCnt, "A9_ex_rcv_date", wp.colStr(ii, "LIAD4_ex_rcv_date"));
//	      if (ilRowCnt < 9) {
//	        wp.colSet(ilRowCnt, "ser_num", "0" + (ilRowCnt + 1));
//	      } else {
//	        wp.colSet(ilRowCnt, "ser_num", "" + (ilRowCnt + 1));
//	      }
//	    }
//
//	  }
*/
@Override
public void queryRead() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

public void doQueryS3() throws Exception {
   kk1 = wp.itemStr("ex_p_seqno"); // p_seqno
   kk2 = wp.itemStr("data_k1"); // curr_code
   dataRead3();
}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

public void dataRead3() throws Exception {
   isAcctMonth = wp.itemStr2("ex_acct_month");
   isThisMm = wp.itemStr2("ex_this_mm");
   isLastMm = wp.itemStr2("ex_last_mm");
   isNextMm = wp.itemStr2("ex_next_mm");
   String sql1 = "";

   func.setConn(wp);
   //-當期-
   if (eqIgno(wp.itemStr2("ex_history"), "N") ||
       (eqIgno(wp.itemStr2("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm))) {
      func.wfReadDebtCurr(kk2);
      //--
      sql1 = "select curr_code,"+
          " beg_bal_lk , end_bal_lk ,"+
          " beg_bal_op , end_bal_op ,"+
          " overpay_lock_sta_date , overpay_lock_due_date "+
          " from act_acct_curr "+
          " where p_seqno =? and curr_code=?";
      sqlSelect(sql1, new Object[]{kk1, kk2});
      if (sqlRowNum > 0) {
         wp.colSet("beg_bal_op", sqlNum("beg_bal_op"));
         wp.colSet("end_bal_op", sqlNum("end_bal_op"));
         wp.colSet("beg_bal_lk", sqlNum("beg_bal_lk"));
         wp.colSet("end_bal_lk", sqlNum("end_bal_lk"));
         wp.colSet("overpay_lock_sta_date", sqlStr("overpay_lock_sta_date"));
         wp.colSet("overpay_lock_due_date", sqlStr("overpay_lock_due_date"));
      }

      String sql2 = " select "
          +" sum(uf_dc_amt2(pay_amt,dc_pay_amt)) as db_pay_uncan"
          +" from act_pay_detail "
          +" where p_seqno = ? "
          +" and uf_nvl(curr_code,'901') = ? "
          +" and payment_type = 'AUT1' ";

      sqlSelect(sql2, new Object[]{kk1, kk2});
      wp.colSet("db_pay_uncan", sqlStr("db_pay_uncan"));

   } else if ((eqIgno(wp.itemStr2("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm))) {
      //-6114-
      wp.colSet("beg_bal_op", 0);
      wp.colSet("end_bal_op", 0);
      wp.colSet("beg_bal_lk", 0);
      wp.colSet("end_bal_lk", 0);
      wp.colSet("overpay_lock_sta_date", "");
      wp.colSet("overpay_lock_due_date", "");

      // --歷史+關帳後
      wp.sqlCmd = " select * from act_curr_hst where acct_month =? and p_seqno = ? and curr_code = ? ";
      setString(1, isAcctMonth);
      setString(2, kk1);
      setString(3, kk2);
      pageSelect();
      if (sqlNotFind()) {
         alertErr2("此條件查無資料 ");
         return;
      }
      //-呆帳本金,利息,費用-
      if (eqIgno(kk2, "901")) {
         sql1 = "select unbill_beg_bal_db_B, unbill_end_bal_db_B, billed_end_bal_db_B"+
             ", unbill_beg_bal_db_I, unbill_end_bal_db_I, billed_end_bal_db_I"+
             ", unbill_beg_bal_db_C, unbill_end_bal_db_C, billed_end_bal_db_C"+
             ", billed_beg_bal_db_B, billed_beg_bal_db_I, billed_beg_bal_db_C"+
             " from act_acct_hst"+
             " where p_seqno =? and acct_month =?";
         sqlSelect(sql1, new Object[]{kk1, isAcctMonth});
         if (sqlRowNum > 0) {
            sql2wp("billed_end_bal_db_B");
            sql2wp("unbill_beg_bal_db_B");
            sql2wp("unbill_end_bal_db_B");
            sql2wp("billed_end_bal_db_I");
            sql2wp("unbill_beg_bal_db_I");
            sql2wp("unbill_end_bal_db_I");
            sql2wp("billed_end_bal_db_C");
            sql2wp("unbill_beg_bal_db_C");
            sql2wp("unbill_end_bal_db_C");
//	            sql2wp("billed_beg_bal_db_B");
//	            sql2wp("billed_beg_bal_db_I");
//	            sql2wp("billed_beg_bal_db_C");
         }
      }

      // ---
      sql1 = " select "
          +" billed_end_bal_af + unbill_end_bal_af as last_bal_af , "
          +" billed_end_bal_lf + unbill_end_bal_lf as last_bal_lf , "
          +" billed_end_bal_pf + unbill_end_bal_pf as last_bal_pf , "
          +" billed_end_bal_bl + unbill_end_bal_bl as last_bal_bl , "
          +" billed_end_bal_ca + unbill_end_bal_ca as last_bal_ca , "
          +" billed_end_bal_it + unbill_end_bal_it as last_bal_it , "
          +" billed_end_bal_ri + unbill_end_bal_ri as last_bal_ri , "
          +" billed_end_bal_pn + unbill_end_bal_pn as last_bal_pn , "
          +" billed_end_bal_ao + unbill_end_bal_ao as last_bal_ao , "
          +" billed_end_bal_ai + unbill_end_bal_ai as last_bal_ai , "
          +" billed_end_bal_sf + unbill_end_bal_sf as last_bal_sf , "
          +" billed_end_bal_dp + unbill_end_bal_dp as last_bal_dp , "
          +" billed_end_bal_cb + unbill_end_bal_cb as last_bal_cb , "
          +" billed_end_bal_ci + unbill_end_bal_ci as last_bal_ci , "
          +" billed_end_bal_cf + unbill_end_bal_cf as last_bal_cf , "
          +" billed_end_bal_db + unbill_end_bal_db as last_bal_db , "
          +" billed_end_bal_cc + unbill_end_bal_cc as last_bal_cc , "
          +" billed_end_bal_ot + unbill_end_bal_ot as last_bal_ot, "
          +" billed_end_bal_id + unbill_end_bal_id as last_bal_id "
          +" from act_curr_hst "
          +" where p_seqno = ? "
          +" and curr_code = ? "
          +" and acct_month = ? "
      ;
      String lsPrevMonth = commString.mid(commDate.dateAdd(isAcctMonth, 0, -1, 0), 0, 6);
      sqlSelect(sql1, new Object[]{kk1, kk2, lsPrevMonth});
      if (sqlRowNum > 0) {
         wp.colSet("billed_beg_bal_af", sqlStr("last_bal_af"));
         wp.colSet("billed_beg_bal_lf", sqlStr("last_bal_lf"));
         wp.colSet("billed_beg_bal_pf", sqlStr("last_bal_pf"));
         wp.colSet("billed_beg_bal_bl", sqlStr("last_bal_bl"));
         wp.colSet("billed_beg_bal_ca", sqlStr("last_bal_ca"));
         wp.colSet("billed_beg_bal_it", sqlStr("last_bal_it"));
         wp.colSet("billed_beg_bal_ri", sqlStr("last_bal_ri"));
         wp.colSet("billed_beg_bal_pn", sqlStr("last_bal_pn"));
         wp.colSet("billed_beg_bal_ao", sqlStr("last_bal_ao"));
         wp.colSet("billed_beg_bal_ai", sqlStr("last_bal_ai"));
         wp.colSet("billed_beg_bal_sf", sqlStr("last_bal_sf"));
         wp.colSet("billed_beg_bal_dp", sqlStr("last_bal_dp"));
         wp.colSet("billed_beg_bal_cb", sqlStr("last_bal_cb"));
         wp.colSet("billed_beg_bal_ci", sqlStr("last_bal_ci"));
         wp.colSet("billed_beg_bal_cf", sqlStr("last_bal_cf"));
         wp.colSet("billed_beg_bal_db", sqlStr("last_bal_db"));
         wp.colSet("billed_beg_bal_cc", sqlStr("last_bal_cc"));
         wp.colSet("billed_beg_bal_ot", sqlStr("last_bal_ot"));
         wp.colSet("billed_beg_bal_id", sqlStr("last_bal_id"));
      }

      //-呆帳本金,利息,費用-
      if (eqIgno(kk2, "901")) {
         sql1 = "select unbill_end_bal_db_B+billed_end_bal_db_B as billed_beg_bal_db_B"+
             ", unbill_end_bal_db_I+billed_end_bal_db_I as billed_beg_bal_db_I"+
             ", unbill_end_bal_db_C+billed_end_bal_db_C as billed_beg_bal_db_C"+
             " from act_acct_hst"+
             " where p_seqno =? and acct_month =?";
         sqlSelect(sql1, new Object[]{kk1, lsPrevMonth});
         if (sqlRowNum > 0) {
            sql2wp("billed_beg_bal_db_B");
            sql2wp("billed_beg_bal_db_I");
            sql2wp("billed_beg_bal_db_C");
         }
      }

   }

   double A2_tl_bill_beg_bal = 0, A2_tl_bill_end_bal = 0, A2_tl_unbill_beg_bal = 0, A2_tl_unbill_end_bal = 0;
   A2_tl_bill_beg_bal = wp.colNum("billed_beg_bal_af")+wp.colNum("billed_beg_bal_lf")+wp.colNum("billed_beg_bal_cf")+wp.colNum("billed_beg_bal_pf")+wp.colNum("billed_beg_bal_bl")+
       wp.colNum("billed_beg_bal_ca")+wp.colNum("billed_beg_bal_it")+wp.colNum("billed_beg_bal_id")+wp.colNum("billed_beg_bal_ri")+wp.colNum("billed_beg_bal_pn")+
       wp.colNum("billed_beg_bal_ot")+wp.colNum("billed_beg_bal_ai")+wp.colNum("billed_beg_bal_sf")+wp.colNum("billed_beg_bal_dp")+wp.colNum("billed_beg_bal_cb")+
       wp.colNum("billed_beg_bal_ci")+wp.colNum("billed_beg_bal_cc")+wp.colNum("billed_beg_bal_db")+
       //wp.col_num("billed_beg_bal_db_b") + wp.col_num("billed_beg_bal_db_i") + wp.col_num("billed_beg_bal_db_c") +
       wp.colNum("billed_beg_bal_ao");
   A2_tl_bill_end_bal = wp.colNum("billed_end_bal_af")+wp.colNum("billed_end_bal_lf")+wp.colNum("billed_end_bal_cf")+wp.colNum("billed_end_bal_pf")+wp.colNum("billed_end_bal_bl")+
       wp.colNum("billed_end_bal_ca")+wp.colNum("billed_end_bal_it")+wp.colNum("billed_end_bal_id")+wp.colNum("billed_end_bal_ri")+wp.colNum("billed_end_bal_pn")+
       wp.colNum("billed_end_bal_ot")+wp.colNum("billed_end_bal_ai")+wp.colNum("billed_end_bal_sf")+wp.colNum("billed_end_bal_dp")+wp.colNum("billed_end_bal_cb")+
       wp.colNum("billed_end_bal_ci")+wp.colNum("billed_end_bal_cc")+wp.colNum("billed_end_bal_db")
       //+ wp.col_num("billed_end_bal_db_b") + wp.col_num("billed_end_bal_db_i") + wp.col_num("billed_end_bal_db_c")
       +wp.colNum("billed_end_bal_ao");
   A2_tl_unbill_beg_bal = wp.colNum("unbill_beg_bal_af")+wp.colNum("unbill_beg_bal_lf")+wp.colNum("unbill_beg_bal_cf")+wp.colNum("unbill_beg_bal_pf")+wp.colNum("unbill_beg_bal_bl")+
       wp.colNum("unbill_beg_bal_ca")+wp.colNum("unbill_beg_bal_it")+wp.colNum("unbill_beg_bal_id")+wp.colNum("unbill_beg_bal_ri")+wp.colNum("unbill_beg_bal_pn")+
       wp.colNum("unbill_beg_bal_ot")+wp.colNum("unbill_beg_bal_ai")+wp.colNum("unbill_beg_bal_sf")+wp.colNum("unbill_beg_bal_dp")+wp.colNum("unbill_beg_bal_cb")+
       wp.colNum("unbill_beg_bal_ci")+wp.colNum("unbill_beg_bal_cc")+wp.colNum("unbill_beg_bal_db")
       //+ wp.col_num("unbill_beg_bal_db_b") + wp.col_num("unbill_beg_bal_db_i") + wp.col_num("unbill_beg_bal_db_c")
       +wp.colNum("unbill_beg_bal_ao");
   A2_tl_unbill_end_bal = wp.colNum("unbill_end_bal_af")+wp.colNum("unbill_end_bal_lf")+wp.colNum("unbill_end_bal_cf")+wp.colNum("unbill_end_bal_pf")+wp.colNum("unbill_end_bal_bl")+
       wp.colNum("unbill_end_bal_ca")+wp.colNum("unbill_end_bal_it")+wp.colNum("unbill_end_bal_id")+wp.colNum("unbill_end_bal_ri")+wp.colNum("unbill_end_bal_pn")+
       wp.colNum("unbill_end_bal_ot")+wp.colNum("unbill_end_bal_ai")+wp.colNum("unbill_end_bal_sf")+wp.colNum("unbill_end_bal_dp")+wp.colNum("unbill_end_bal_cb")+
       wp.colNum("unbill_end_bal_ci")+wp.colNum("unbill_end_bal_cc")+wp.colNum("unbill_end_bal_db")
       //+ wp.col_num("unbill_end_bal_db_b") + wp.col_num("unbill_end_bal_db_i") + wp.col_num("unbill_end_bal_db_c")
       +wp.colNum("unbill_end_bal_ao");
   wp.colSet("wk_billed_bal_amt", A2_tl_bill_beg_bal);
   wp.colSet("wk_billed_end_bal", A2_tl_bill_end_bal);
   wp.colSet("wk_unbill_beg_amt", A2_tl_unbill_beg_bal);
   wp.colSet("wk_unbill_end_amt", A2_tl_unbill_end_bal);
}

void doSelectS5() throws Exception {
   kk1 = wp.itemStr("ex_p_seqno"); // p_seqno
   kk2 = wp.itemStr("data_k1"); // curr_code
   dataRead5();
}

public void dataRead5() throws Exception {
   // -act_acct_curr-
   String sql2 = " select curr_code, min_pay ,"
       +" min_pay_bal,"
       +" autopay_acct_bank ,"
       +" autopay_acct_no , "
       +" dc_autopay_bal, dc_autopay_beg_amt "
       +", (SELECT revolving_interest1 FROM ptr_actgeneral_n WHERE acct_type=act_acct_curr.acct_type) AS revol_intr1 "
       +" from act_acct_curr "
       +" where p_seqno = ? "
       +" and curr_code = ? ";
   sqlSelect(sql2, new Object[]{kk1, kk2});
   if (sqlRowNum <= 0) {
      errmsg("查無外幣帳戶資料 ");
      return;
   }
   wp.colSet("curr_code", sqlStr("curr_code"));
   wp.colSet("min_pay", sqlStr("min_pay"));
   wp.colSet("min_pay_bal", sqlStr("min_pay_bal"));
   wp.colSet("dc_autopay_bal", sqlStr("dc_autopay_bal"));
   wp.colSet("dc_autopay_beg_amt", sqlStr("dc_autopay_beg_amt"));
   wp.colSet("stmt_auto_pay_bank", sqlStr("autopay_acct_bank"));
   wp.colSet("stmt_auto_pay_no", sqlStr("autopay_acct_no"));
   double lm_revolIntr1=sqlNum("revol_intr1");

   isAcctMonth = wp.itemStr2("ex_acct_month");
   isThisMm = wp.itemStr2("ex_this_mm");
   if (eqIgno(wp.itemStr("ex_history"), "N") ||
       (eqIgno(wp.itemStr("ex_history"), "Y") && eqIgno(isAcctMonth, isThisMm))) {
      // -當期-act_curr_hst-
      String sql1 = " select "
          +" curr_code , "
          +" min_pay , "
          +" min_pay_bal , "
          +" stmt_auto_pay_bank , "
          +" stmt_auto_pay_no , "
          +" stmt_auto_pay_date , "
          +" stmt_auto_pay_amt , "
          +" stmt_last_ttl , "
          +" stmt_payment_amt , "
          +" stmt_over_due_amt , "
          +" stmt_adjust_amt , "
          +" stmt_new_amt , "
          +" stmt_this_ttl_amt , "
          +" stmt_mp , "
          +" acct_month "
          +" from act_curr_hst "
          +" where p_seqno = ? "
          +" and acct_month = ? "
          +" and curr_code = ? ";
      setString(1, kk1);
      setString(wp.itemStr("ex_last_mm"));
      setString(kk2);
      sqlSelect(sql1);
      if (sqlRowNum > 0) {
         wp.colSet("curr_code", sqlStr("curr_code"));
         wp.colSet("min_pay", sqlStr("min_pay"));
         wp.colSet("min_pay_bal", sqlStr("min_pay_bal"));
         wp.colSet("stmt_auto_pay_bank", sqlStr("stmt_auto_pay_bank"));
         wp.colSet("stmt_auto_pay_no", sqlStr("stmt_auto_pay_no"));
         wp.colSet("stmt_auto_pay_date", sqlStr("stmt_auto_pay_date"));
         wp.colSet("stmt_auto_pay_amt", sqlStr("stmt_auto_pay_amt"));
         wp.colSet("stmt_last_ttl", sqlStr("stmt_last_ttl"));
         wp.colSet("stmt_payment_amt", sqlStr("stmt_payment_amt"));
         wp.colSet("stmt_over_due_amt", sqlStr("stmt_over_due_amt"));
         wp.colSet("stmt_adjust_amt", sqlStr("stmt_adjust_amt"));
         wp.colSet("stmt_new_amt", sqlStr("stmt_new_amt"));
         wp.colSet("stmt_this_ttl_amt", sqlStr("stmt_this_ttl_amt"));
         wp.colSet("stmt_mp", sqlStr("stmt_mp"));
         wp.colSet("acct_month", sqlStr("acct_month"));
      }

      //--
      String sql3 = " select "
          +" stmt_cycle_date , "
          +" stmt_credit_limit , "
          +" stmt_revol_rate , "
          +" stmt_payment_no , "
          +" stmt_last_payday , "
          +" acct_month , "
          +" acct_jrnl_bal , "
          +" min_pay_bal "
          +" from act_acct_hst "
          +" where p_seqno = ? "
          +" and acct_month = ? ";
      sqlSelect(sql3, new Object[]{kk1, wp.itemStr("ex_last_mm")});
      wp.colSet("stmt_cycle_date", sqlStr("stmt_cycle_date"));
      wp.colSet("stmt_credit_limit", sqlStr("stmt_credit_limit"));
      wp.colSet("stmt_revol_rate", sqlStr("stmt_revol_rate"));
      wp.colSet("stmt_payment_no", sqlStr("stmt_payment_no"));
      wp.colSet("stmt_last_payday", sqlStr("stmt_last_payday"));
   } else if (eqIgno(wp.itemStr("ex_history"), "Y") && !eqIgno(isAcctMonth, isThisMm)) {
      // -歷史-
      String lsLastMm = commDate.dateAdd(wp.itemStr("ex_acct_month"), 0, -1, 0).substring(0, 6);
      String sql1 = " select "
          +" curr_code , "
          +" min_pay , "
          +" min_pay_bal , "
          +" stmt_auto_pay_bank , "
          +" stmt_auto_pay_no , "
          +" stmt_auto_pay_date , "
          +" stmt_auto_pay_amt , "
          +" stmt_last_ttl , "
          +" stmt_payment_amt , "
          +" stmt_over_due_amt , "
          +" stmt_adjust_amt , "
          +" stmt_new_amt , "
          +" stmt_this_ttl_amt , "
          +" stmt_mp , "
          +" acct_month "
          +" from act_curr_hst "
          +" where p_seqno = ? "
          +" and acct_month = ? "
          +" and curr_code = ? ";
      sqlSelect(sql1, new Object[]{kk1, lsLastMm, kk2});
      if (sqlRowNum > 0) {
         wp.colSet("curr_code", sqlStr("curr_code"));
         wp.colSet("min_pay", sqlStr("min_pay"));
         wp.colSet("min_pay_bal", sqlStr("min_pay_bal"));
         wp.colSet("stmt_auto_pay_bank", sqlStr("stmt_auto_pay_bank"));
         wp.colSet("stmt_auto_pay_no", sqlStr("stmt_auto_pay_no"));
         wp.colSet("stmt_auto_pay_date", sqlStr("stmt_auto_pay_date"));
         wp.colSet("stmt_auto_pay_amt", sqlStr("stmt_auto_pay_amt"));
         wp.colSet("stmt_last_ttl", sqlStr("stmt_last_ttl"));
         wp.colSet("stmt_payment_amt", sqlStr("stmt_payment_amt"));
         wp.colSet("stmt_over_due_amt", sqlStr("stmt_over_due_amt"));
         wp.colSet("stmt_adjust_amt", sqlStr("stmt_adjust_amt"));
         wp.colSet("stmt_new_amt", sqlStr("stmt_new_amt"));
         wp.colSet("stmt_this_ttl_amt", sqlStr("stmt_this_ttl_amt"));
         wp.colSet("stmt_mp", sqlStr("stmt_mp"));
         wp.colSet("acct_month", sqlStr("acct_month"));
      }

      String sql3 = " select "
          +" stmt_cycle_date , "
          +" stmt_credit_limit , "
          +" stmt_revol_rate , "
          +" stmt_payment_no , "
          +" stmt_last_payday , "
          +" acct_month , "
          +" acct_jrnl_bal , "
          +" min_pay_bal "
          +" from act_acct_hst "
          +" where p_seqno = ? "
          +" and acct_month = ? ";
      sqlSelect(sql3, new Object[]{kk1, lsLastMm});
      if (sqlRowNum > 0) {
         wp.colSet("stmt_cycle_date", sqlStr("stmt_cycle_date"));
         wp.colSet("stmt_credit_limit", sqlStr("stmt_credit_limit"));
         wp.colSet("stmt_revol_rate", sqlStr("stmt_revol_rate"));
         wp.colSet("stmt_payment_no", sqlStr("stmt_payment_no"));
         wp.colSet("stmt_last_payday", sqlStr("stmt_last_payday"));
         // wp.colSet("acct_month", sqlStr("acct_month"));
         // wp.colSet("acct_jrnl_bal", sqlStr("acct_jrnl_bal"));
         // wp.colSet("min_pay_bal", sqlStr("min_pay_bal"));
      }
   }
   //-jh:231227:顯示 日利率 及年利率 --
   double lm_revolRate =wp.colNum("stmt_revol_rate");
   double lm_yyRate =(lm_revolIntr1 - lm_revolRate) * 365 / 100;
   lm_yyRate =commString.numScale(lm_yyRate,2);
   wp.colSet("wk_yy_revol_rate",lm_yyRate);

   // --
   String lsAcctMm = wp.colStr("acct_month");
   if (empty(lsAcctMm)) {
      wp.colSet("wk_acct_month", "當期");
   } else {
      lsAcctMm = commDate.dateAdd(lsAcctMm, 0, 1, 0).substring(0, 6);
      wp.colSet("wk_acct_month", lsAcctMm);
   }
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
   tabClick();
}

void tabClick() {
   wp.colSet("a_click_1", "t_click_1");
   wp.colSet("a_click_2", "t_click_2");
   wp.colSet("a_click_3", "t_click_3");
   wp.colSet("a_click_4", "t_click_4");
   wp.colSet("a_click_5", "t_click_5");
   wp.colSet("a_click_6", "t_click_6");
   wp.colSet("a_click_7", "t_click_7");
   wp.colSet("a_click_8", "t_click_8");
   wp.colSet("a_click_9", "t_click_9");

   String lsClick = "";
   lsClick = wp.itemStr2("tab_click");
   if (eqIgno(lsClick, "1")) {
      wp.colSet("a_click_1", "tab_active");
   } else if (eqIgno(lsClick, "2")) {
      wp.colSet("a_click_2", "tab_active");
   } else if (eqIgno(lsClick, "3")) {
      wp.colSet("a_click_3", "tab_active");
   } else if (eqIgno(lsClick, "4")) {
      wp.colSet("a_click_4", "tab_active");
   } else if (eqIgno(lsClick, "5")) {
      wp.colSet("a_click_5", "tab_active");
   } else if (eqIgno(lsClick, "6")) {
      wp.colSet("a_click_6", "tab_active");
   } else if (eqIgno(lsClick, "7")) {
      wp.colSet("a_click_7", "tab_active");
   } else if (eqIgno(lsClick, "8")) {
      wp.colSet("a_click_8", "tab_active");
   } else if (eqIgno(lsClick, "9")) {
      wp.colSet("a_click_9", "tab_active");
   } else {
      wp.colSet("a_click_1", "tab_active");
   }
}

void doQueryPostingAmt() throws Exception {

   wp.selectSQL = " card_no , "
       +" auth_code , "
       +" mcht_no , "
       +" mcht_chi_name , "
       +" unit_price , "
       +" tot_amt ,"
       +" unit_price as per_term_amt , "
       +" install_tot_term , "
       +" install_curr_term, "+
       " purchase_date, contract_no"
   ;

   wp.daoTable = " bil_contract ";
   wp.whereStr = " where 1=1 "
       +" and contract_kind='1' "
       +" and install_tot_term <> install_curr_term "
       +" and apr_date<>''" //apr_flag ='Y' "
       +sqlCol(wp.itemStr2("ex_p_seqno"), "p_seqno");
   wp.whereOrder = " order by purchase_date";

   pageQuery();
   wp.setListCount(0);

}

//String getAcctKey(String _id_no, String _acct_type) {
//   zzstr.acct_key()
//   if (empty(_id_no)) return "";
//
//   String sql1 = " select acct_key from act_acno where acct_type = ? "
//      + " and acct_key like ? and id_p_seqno in "
//      + " (select id_p_seqno from crd_card where acct_type = ? and current_code = '0')  "
//      + " order by acct_key Asc "
//      + zzsql.rownum(1);
//
//   sqlSelect(sql1, new Object[]{_acct_type, _id_no + "%", _acct_type});
//
//   if (sql_nrow > 0) return sql_ss("acct_key");
//
//   return "";
//}

}
