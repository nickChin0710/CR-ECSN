package ccam01;

public class CcasLimitCorp extends busi.FuncBase {
public CcasLimitVar hh=new CcasLimitVar();

public int canuse_Limit(String aCorpNo, String aAcctType) {
   msgOK();
   hh.initData();
   dateTime();

   if (empty(aCorpNo))
      return 0;
   hh.corpNo =aCorpNo;
   hh.acctType =nvl(aAcctType,"03");

   selectCcaCardAcct(hh.corpNo,hh.acctType);
   if (rc !=1)
      return -1;

   //-已授權未請款-
   selectCcaAuthTxlog();
   //-已關帳未繳款不含預借現金-個人-
   selectActDebt();
   //-22. AuConsume_UNPOST_INST_FEE: 分期付款未到期-個人-
   selectBilContract();
   //-24. AuConsume_PRE_PAY_AMT: 溢繳款-個人(預付款金額)-
   selectActAcct();
   //-26. AuConsume_TOT_UNPAID_AMT: 已付款未銷帳-個人(payment末消)-
   selectActPayDetail();

   //可用餘額: 總月限額(lm_tmpamt2) - 總消費金額(lm_tmpamt1)
   /*-- = 本次交易金額 + 已授權未請款
               + 總未付本金(結帳消費+未結帳消費+結帳預現+未結帳預現+指撥金額)
			      + 分期未結帳金額
    --*/
   //-總消費金額-
   double lmAmt1 =hh.txNtAmt + hh.totAmtConsume
         + hh.paidConsume  //+unpaid_consume_fee
         + hh.paidPrecash //+unpaid_precash
         + hh.ibmReceiveAmt
         + hh.unpostInstFee;

   /*總月限額 = (額度 * 調整倍數) + 預付款金額 + payment末消 */
   //lm_tmpamt2   =tmpTotLimit + AuConsume_PRE_PAY_AMT + AuConsume_TOT_UNPAID_AMT;
   double lmAmt2 =hh.prePayAmt +hh.totUnpaidAmt;
   //-有臨調-
   if (hh.adjLimit >0) {
      lmAmt2 +=hh.adjLimit;
   }
   else lmAmt2 +=hh.creditLimit;

   lmAmt2 =lmAmt2 - lmAmt1;
   hh.canUseLimit =commString.numScale(lmAmt2,0);
   return 1;
}

private void selectCcaCardAcct(String aCorpNo, String aAcctType) {
   strSql ="select A.acno_p_seqno, A.card_acct_idx"
         +", A.p_seqno as acct_p_seqno, A.corp_p_seqno, A.acct_type, A.acno_flag "
         +", A.tot_amt_month, A.adj_eff_start_date, A.adj_eff_end_date "
         +", C.line_of_credit_amt, C.class_code"
         +" from cca_card_acct A join crd_corp B on A.corp_p_seqno=B.corp_p_seqno and A.acno_flag='2'"
         +" join act_acno C on C.acno_p_seqno=A.acno_p_seqno"
         +" where A.acct_type =?"
         +" and B.corp_no =?"
         ;
   setString(1,aAcctType);
   setString(2,aCorpNo);
   sqlSelect(strSql);
   if (sqlRowNum <=0) {
      errmsg("公司帳戶資料 not exist[%s]",aCorpNo);
      return;
   }

   hh.acctPSeqno =colStr("acct_p_seqno");
   hh.cardAcctIdx =colNum("card_acct_idx");
   hh.corpPSeqno =colStr("corp_p_seqno");
   hh.acnoFlag =colNvl("acno_flag","1");
   hh.creditLimit =colNum("line_of_credit_amt");
   hh.adjLimit =colNum("tot_amt_month");
   hh.classCode =colStr("class_code");

   String lsDate1 =colStr("adj_eff_start_date");
   String lsDate2 =colStr("adj_eff_end_date");
   if (!commString.between(sysDate,lsDate1,lsDate2)) {
      hh.adjLimit =0;
   }

   return;
}

private void selectCcaAuthTxlog() {
   strSql = "select sum(decode(cacu_cash,'Y',nt_amt,0)) as xx_tot_cash"
         + ", sum(decode(cacu_amount,'Y',nt_amt,0)) as xx_tot_amt"
         + " from cca_auth_txlog"
         + " where mtch_flag not in ('Y','U') and cacu_amount='Y' "
         + " and card_no in (select card_no from crd_card where corp_p_seqno =?)"
   ;
   setString(1,hh.corpPSeqno);
   sqlSelect(strSql);
   if (sqlRowNum > 0) {
      hh.totAmtConsume = colNum("xx_tot_amt");
      hh.totAmtPrecash = colNum("xx_tot_cash");
   }
   else {
      hh.totAmtConsume = 0;
      hh.totAmtPrecash = 0;
   }
}

private void selectActDebt() {

   strSql ="select "
         +" sum(decode(acct_code,'CA',end_bal,0)) as xx_paid_precash"
         +" from act_debt"
         +" where 1=1" +
         " and acct_code in (select acct_code from ptr_actcode where interest_method='Y')"+
         " and card_no in (select card_no from crd_card where corp_p_seqno=? and acct_type=?)";

   setString(1,hh.corpPSeqno);
   setString(2,hh.acctType);
   sqlSelect(strSql);
   if (sqlRowNum >0) {      
      hh.paidPrecash =colNum("xx_paid_precash");            
   }
   
   strSql = " select sum(acct_jrnl_bal+end_bal_op) as acct_jrnl_bal from act_acct where corp_p_seqno = ? ";
   setString(1,hh.corpPSeqno);
   sqlSelect(strSql);
   if(sqlRowNum > 0) {
	   hh.paidConsume = colNum("acct_jrnl_bal");
	   if(hh.paidConsume < 0)
		   hh.paidConsume = 0;
   }
   
}

private void selectActAcct() {
/**
 * 總繳戶 //+個繳戶
==>24. AuConsume_PRE_PAY_AMT: 溢繳款-個人(預付款金額)
   select end_bal_op+end_bal_lk from newtable where acno_p_seqno =  再加上
   select end_bal_op+end_bal_lk from act_acct where gp_no = 個人gp_no
* */
   strSql ="select end_bal_op+end_bal_lk as xx_pre_pay_amt"
         +" from act_acct"
         +" where 1=1"+
         " and p_seqno =? and p_seqno<>''"
         //+" and p_seqno in (select distinct p_seqno from crd_card where corp_p_seqno=? and acct_type =?)"
   ;
//   setString(1,hh.corp_pseqno);
//   setString(hh.acct_type);
   setString(1,hh.acctPSeqno);
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      hh.prePayAmt =colNum("xx_pre_pay_amt");
   }
}

private void selectActPayDetail() {
   //-只取總繳payment未消-
   double lmAmt1=0, lmAmt2=0;
   strSql ="select sum(A.pay_amt) as xx_pay_amt1 "
         +" from act_pay_detail A join act_pay_batch B on A.batch_no=B.batch_no "
         +" where B.batch_tot_cnt >0"+
         " and A.p_seqno =? and A.p_seqno<>''"
   ;
   setString(1,hh.acctPSeqno);
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      lmAmt1 =colNum("xx_pay_amt1");
   }

   strSql ="select sum(pay_amt) as xx_pay_amt2"
         +" from act_debt_cancel"
         +" where process_flag <>'Y'"
         +" and p_seqno =? and p_seqno<>''"
         ;
   setString(1,hh.acctPSeqno);
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      lmAmt2 =colNum("xx_pay_amt2");
   }

   //-jh[20211019]CR-1336-
   double lmAmt3=0;
   strSql ="SELECT sum(txn_amt) as xx_pay_amt3 FROM act_pay_ibm"
           +" WHERE p_seqno =?"
           +" AND nvl(proc_mark,'') <>'Y' and nvl(error_code,'') in ('','0','N')"
           +" and txn_source not in ('0101', '0102', '0103', '0502')"
   ;
   sqlSelect(strSql, hh.acctPSeqno);
   if (sqlRowNum >0) {
	   lmAmt3 =colNum("xx_pay_amt3");
   }
   
   double lmAmt4=0;
   strSql = "select sum(pay_amt) as xx_pay_amt4 from cca_card_acct where acno_p_seqno = ? ";
   sqlSelect(strSql,hh.acctPSeqno);
   if (sqlRowNum >0) {
	   lmAmt4 =colNum("xx_pay_amt4");
   }
   
   hh.totUnpaidAmt =lmAmt1 +lmAmt2 +lmAmt3+lmAmt4;
}

private void selectBilContract() {
   strSql = " select sum("
         + " (install_tot_term - install_curr_term) * unit_price"
         + " +remd_amt +decode(install_curr_term,0,first_remd_amt+extra_fees,0)"
         + " ) as xx_inst_unpost "
         + " from bil_contract "
         + " where install_tot_term <> install_curr_term "
         + " and auth_code not in ('','N','REJECT','P','reject') "
         + " and (post_cycle_dd >0 or installment_kind ='F') "
         +" and refund_flag<>'Y' "  //退貨
   +" and card_no in (select card_no from crd_card where corp_p_seqno=? and acct_type=?)"
   ;

   setString(1,hh.corpPSeqno);
   setString(2,hh.acctType);
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      hh.unpostInstFee =colNum("xx_inst_unpost");
   }
}

}
