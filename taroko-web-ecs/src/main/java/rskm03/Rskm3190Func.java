package rskm03;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Rskm3190Func extends FuncEdit {
String kk1 = "";

public Rskm3190Func(TarokoCommon wr) {
   wp = wr;
   this.conn = wp.getConn();
}

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataSelect()  {
   String kk1 = wp.itemStr("card_no");
   kk1 = wp.itemStr("card_no");
   if (empty(kk1)) {
      errmsg("卡號: 不可空白");
      return -1;
   }
   wp.colSet("sysdate", sysDate);
   strSql = "select A.*, hex(A.rowid) as rowid, A.mod_seqno"
         + ", A.for_ch_amt+A.ch_deuct_amt+A.for_mcht_amt+A.cb_ok_amt" +
         "+A.non_disput_amt+A.unbill_amt+A.for_fraud_amt as wk_bank_nopay " +
         ", to_char(mod_time,'yyyymmdd') as mod_date" +
         " from rsk_ctfi_case A" +
         " where A.card_no =?";

   setParm(1, kk1);
   sqlSelectWp(strSql);
   if (sqlNotfind) {
      return 0;
   }
   return 1;
}

@Override
public void dataCheck()  {
   kk1 = wp.itemStr("card_no");

   if (wp.itemNum("miscell_unpay_amt") > wp.itemNum("actual_amt")) {
      errmsg("轉雜支金額不可大於實際發生金額");
      return;
   }

   sqlWhere = " where 1=1"
         + " and card_no='" + kk1 + "'"
         + " and nvl(mod_seqno,0) =" + wp.modSeqno();
   log("sql-where=" + sqlWhere);
   log("mod_seqno:" + wp.modSeqno());
   if (this.isOtherModify("rsk_ctfi_case", sqlWhere)) {
      return;
   }
}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate()  {
   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }
   strSql = "update rsk_ctfi_case set "
         + " survey_ing = :survey_ing, "
         + " survey_date1 = :survey_date1, "
         + " actual_amt = :actual_amt, "
         + " debit_card_date = :debit_card_date, "
         + " doc_waiting = :doc_waiting, "
         + " survey_date2 = :survey_date2, "
         + " doc_desc = :doc_desc, "
         + " acct_date = :acct_date, "
         + " chgback_ing = :chgback_ing, "
         + " survey_date3 = :survey_date3, "
         + " chgback_schedule = :chgback_schedule, "
         + " for_ch_amt = :for_ch_amt, "
         + " recov_cash_amt = :recov_cash_amt, "
         + " police_ing = :police_ing, "
         + " survey_date4 = :survey_date4, "
         + " coordi_unit = :coordi_unit, "
         + " ch_deuct_amt = :ch_deuct_amt, "
         + " recov_unpay_amt = :recov_unpay_amt, "
         + " survey_close = :survey_close, "
         + " survey_date5 = :survey_date5, "
         + " for_mcht_amt = :for_mcht_amt, "
         + " fraud_unpay_amt = :fraud_unpay_amt, "
         + " survey_date7 = :survey_date7, "
         + " cb_ok_amt = :cb_ok_amt, "
         + " oth_unpay_amt = :oth_unpay_amt, "
         + " survey_date8 = :survey_date8, "
         + " non_disput_amt = :non_disput_amt, "
         + " turn_miscell_cnt2 = :turn_miscell_cnt2, "
         + " survey_remark = :survey_remark, "
         + " unbill_amt = :unbill_amt, "
         + " miscell_unpay_amt = :miscell_unpay_amt, "
         + " for_fraud_amt = :for_fraud_amt, "
         + " turn_miscell_amt2 = :turn_miscell_amt2, "
         + " turn_miscell_amt = :turn_miscell_amt, "
         + " turn_miscell_date2 = :turn_miscell_date2, "
         + " survey_close_appr = :survey_close_appr, "
         + " survey_date6 =:survey_date6, "
         + " turn_legal_flag = :turn_legal_flag, "
         + " turn_legal_date = :turn_legal_date, "
         + " turn_coll_flag = :turn_coll_flag, "
         + " turn_coll_date = :turn_coll_date, "
         + " case_close_flag = :case_close_flag, "
         + " case_close_date = :case_close_date, "
         + " except_allow = :except_allow, "
         + " except_date = :except_date, "
         + " survey_flag7 = :survey_flag7 , "
         + " survey_flag8 = :survey_flag8 , "
         + " survey_remark7 = :survey_remark7 , "
         + " survey_remark8 = :survey_remark8 ,"
         //       + " case_score = case_score, "
         + " mod_user = :mod_user, "
         + " mod_time = sysdate, "
         + " mod_pgm = :mod_pgm, "
         + " mod_seqno =nvl(mod_seqno,0)+1"
         + " where card_no ='" + kk1 + "'"
         + " and nvl(mod_seqno,0) ='" + wp.modSeqno() + "'"

   ;
   log("card_no:" + kk1);
   log("mod_seqno:" + wp.modSeqno());


   item2ParmNvl("survey_ing", "N");
   item2ParmStr("survey_date1");
   item2ParmNum("actual_amt");
   item2ParmStr("debit_card_date");
   item2ParmNvl("doc_waiting", "N");
   item2ParmStr("survey_date2");
   item2ParmStr("doc_desc");
   item2ParmStr("acct_date");
   item2ParmNvl("chgback_ing", "N");
   item2ParmStr("survey_date3");
   item2ParmStr("chgback_schedule");
   item2ParmNum("for_ch_amt");
   item2ParmNum("recov_cash_amt");
   item2ParmNvl("police_ing", "N");
   item2ParmStr("survey_date4");
   item2ParmStr("coordi_unit");
   item2ParmNum("ch_deuct_amt");
   item2ParmNum("recov_unpay_amt");
   item2ParmNvl("survey_close", "N");
   item2ParmStr("survey_date5");
   item2ParmNum("for_mcht_amt");
   item2ParmNum("fraud_unpay_amt");
   item2ParmStr("survey_date7");
   item2ParmNum("cb_ok_amt");
   item2ParmNum("oth_unpay_amt");
   item2ParmStr("survey_date8");
   item2ParmNum("non_disput_amt");
   item2ParmNum("turn_miscell_cnt2");
   item2ParmStr("survey_remark");
   item2ParmNum("unbill_amt");
   item2ParmNum("miscell_unpay_amt");
   item2ParmNum("for_fraud_amt");
   item2ParmNum("turn_miscell_amt2");
   item2ParmNum("turn_miscell_amt");
   item2ParmStr("turn_miscell_date2");
   item2ParmNvl("survey_close_appr", "N");
   item2ParmStr("survey_date6");
   item2ParmNvl("turn_legal_flag", "N");
   item2ParmStr("turn_legal_date");
   item2ParmNvl("turn_coll_flag", "N");
   item2ParmStr("turn_coll_date");
   item2ParmNvl("case_close_flag", "N");
   item2ParmStr("case_close_date");
   item2ParmNvl("except_allow", "N");
   item2ParmStr("except_date");
   item2ParmNvl("survey_flag7", "N");
   item2ParmNvl("survey_flag8", "N");
   item2ParmStr("survey_remark7");
   item2ParmStr("survey_remark8");
   setString("mod_user", wp.loginUser);
   item2ParmStr("mod_pgm");
   item2ParmNum("mod_seqno");


   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfi_txn error: " + this.sqlErrtext);
   }
   return rc;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

}
