package ccam01;
/** 授權額度計算程式
 * 2023-1113   JH    cacu_amount<>'N'
 * 2023-1016   JH    分期未POSTING不管一般/商務卡一律算
 * 2022-0520.0606   JH    9402: 代收代付
 * 2021-1019   JH    CR-1336: act_pay_ibm
 * 2021-0907   JH    RC_end_bal
 * 2020-1006   JH    combo_cash_limit
 * 2020-0910   JH    canUse_cash() by card_note
 * 2020-0909   JH    canUse_cash()
 * 2020-0804   JH    ccas_class_code
 * 2020-0615   JH    分期unbill_amt
 * 2020-0323   JH    idno_All_limit()
* */

public class CcasLimit extends busi.FuncBase {
public String modVersion() { return "v22.0606"; }
public CcasLimitVar hh = new CcasLimitVar();
private boolean ibDebitFlag=false;

public double idnoLimitValid(String aCardNo) {
   if (empty(aCardNo))
      return 0;

   strSql ="select acno_flag, major_id_p_seqno, acno_p_seqno from crd_card"+
         " where card_no =?";
   sqlSelect(strSql,aCardNo);
   if (sqlRowNum <=0) return 0;

   String lsAcno =colStr("acno_flag");
   if (eq(lsAcno,"Y")) {
      strSql = " select line_of_credit_amt as idno_limit from act_acno"
             + " where acno_p_seqno in (select acno_p_seqno from crd_card where current_code='0' and card_no =?)" + commSqlStr.rownum(1);
      sqlSelect(strSql,aCardNo);
      if (sqlRowNum >0)
         return colNum("idno_limit");
      return 0;
   }

   String lsMidPSeqno =colStr("major_id_p_seqno");
   strSql ="select sum(line_of_credit_amt)  as idno_limit"
         +" from act_acno"
         +" where acno_flag in ('1','3')"
         +" and p_seqno in (select p_seqno from crd_card where current_code='0'"
         +" and major_id_p_seqno =?)";
   sqlSelect(strSql,lsMidPSeqno);
   if (sqlRowNum >0)
      return colNum("idno_limit");
   return 0;
}

public double idnoLimitAll(String aCardNo) {
   if (empty(aCardNo))
      return 0;

   strSql ="select major_id_p_seqno from crd_card where card_no =?";
   sqlSelect(strSql,aCardNo);
   if (sqlRowNum <0) {
      errmsg("查無卡片資料[%s]", aCardNo);
      return -1;
   }

   //-個繳金額-
   String lsIdPSeqno =colStr("major_id_p_seqno");
   strSql ="select A.line_of_credit_amt"+
         ", B.TOT_AMT_MONTH, B.ADJ_EFF_START_DATE, B.ADJ_EFF_END_DATE"+
         " from act_acno A join cca_card_acct B on A.acno_p_seqno=B.acno_p_seqno and B.debit_flag<>'Y'"+
         " where A.acno_p_seqno in ("+
         " select acno_p_seqno from crd_card where major_id_p_seqno =? and current_code='0' and acno_flag in ('1','3')"+
         " )"
         ;
   sqlSelect(strSql,lsIdPSeqno);
   if (sqlRowNum <=0) {
      errmsg("查無帳戶資料[act_acno+cca_card_acct], [%s]",aCardNo);
      return -1;
   }

   int llNrow =sqlRowNum;
   double lmLimit=0;
   for(int ll=0; ll<llNrow; ll++) {
      String lsDate1 =colStr("adj_eff_start_date");
      String lsDate2 =colStr("adj_eff_end_date");
      if (commString.strComp(wp.sysDate,lsDate1)>=0 && commString.strComp(wp.sysDate,lsDate2)<=0) {
         lmLimit +=colNum("tot_amt_month");
      }
      else lmLimit +=colNum("line_of_credit_amt");
   }

   return lmLimit;
}

public int canUseLimit(String aCardNo) {
   dateTime();
   msgOK();
   hh.initData();
   double lmAmt=0;

   if (empty(aCardNo))
      return 0;

   hh.cardNo =aCardNo;
   selectCcaCardBase(aCardNo);
   if (rc !=1)
      return 0;

   //-已授權未請款:一般消費/預借現金-
   selectCcaAuthTxlog();
   //-20. AuConsume_IBM_RECEIVE_AMT: 指撥額度-個人-
   selectActAcno();
   if (rc !=1)
      return 0;
   //-欠款金額--
   selectActDebt();
   //-22. AuConsume_UNPOST_INST_FEE: 分期付款未到期-個人-
   if (!ibDebitFlag) {
      selectBilContract();
      //-24. AuConsume_PRE_PAY_AMT: 溢繳款-個人(預付款金額)-
      selectActAcct();
      //-26. AuConsume_TOT_UNPAID_AMT: 已付款未銷帳-個人(payment末消)-
      selectActPayDetail();
      //--爭議款
      selectRskProblem();
   }
   
   
   //可用餘額: 總月限額(lm_tmpamt2) - 總消費金額(lm_tmpamt1)
   /*-- = 本次交易金額 + 已授權未請款
               + 總未付本金(結帳消費+未結帳消費+結帳預現+未結帳預現+指撥金額)
			      + 分期未結帳金額
    --*/
   //-總消費金額-
   double lmAmt1 =hh.txNtAmt + hh.totAmtConsume
         + hh.paidConsume 
         + hh.ibmReceiveAmt
         + hh.unpostInstFee
         + hh.problemAmt
         ;

   /*總月限額 = (額度 * 調整倍數) + 預付款金額 + payment末消 */
   double lmAmt2 =hh.prePayAmt +hh.totUnpaidAmt;
   //-有臨調-
   if (hh.adjLimit >0) {
      lmAmt2 +=hh.adjLimit;
   }
   else lmAmt2 +=hh.creditLimit;
   //--總額度 - 消費額度(不含專款專用) - 專款專用最大額度 - 期間內超出專款專用額度  + 已入帳的專款專用分期期金 + 轉檔缺 act_acct_sum.end_bal_spec 補上
   lmAmt2 =lmAmt2 - lmAmt1 - hh.totalSpecialAmt - hh.overSpecialAmt + hh.returnSpecAmt + hh.returnAmt ;   
   hh.canUseLimit =commString.numScale(lmAmt2,0);
   hh.canUseSpecLimit = lmAmt2 + hh.totalSpecialAmt + hh.overSpecialAmt - hh.specialAmt ;
   hh.canUseSpecLimit = commString.numScale(hh.canUseSpecLimit, 0);
   return 1;
}

public void canUseCash(String aCardNo) {
   msgOK();
   if (ibDebitFlag || empty(aCardNo))
      return;

   double lmTmpPrecash =0;
   lmTmpPrecash =hh.paidPrecash + hh.ibmReceiveAmt;

   double lmCashRate = 0 , lmAddTotAmt = 0 ;
   //-cca_risk_consume_parm-
   strSql ="select card_note, lmt_amt_month_pct, 0 as add_tot_amt"
         +" from cca_risk_consume_parm"
         +" where 1=1 and area_type ='T'"
         +" and card_note in (select card_note from crd_card where card_no =?"
         +" union select '*' from dual )"
         +" and risk_level =?"
         +" and risk_type ='C'"
         +" order by decode(card_note,'*','zz',card_note)"
         +commSqlStr.rownum(1);

   setString(1,aCardNo);
   setString(2,hh.classCode);
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      lmCashRate =colNum("lmt_amt_month_pct");
      lmAddTotAmt =colNum("add_tot_amt");
   }
   double lmA=0;
   if (lmCashRate >0) {
      lmA = commString.numScale(hh.creditCash * (lmCashRate / 100),0);
   }
   if (lmAddTotAmt >0 && lmA>lmAddTotAmt) {
      lmA =lmAddTotAmt;
   }
   //--臨調額度(預現)--
   if (hh.adjLimit >0) {
      strSql ="select adj_month_amt"
            +" from cca_adj_parm"
            +" where card_acct_idx =? and risk_type ='C'"
            ;
      setDouble(1,hh.cardAcctIdx);
      sqlSelect(strSql);
      if (sqlRowNum >0) {
         lmA =colNum("adj_month_amt");
      }
   }

   // 若預現總額度 > 臨調後總額度, 則預現總額度 = 臨調後總額度
   //IF lmA > ld_consume_quota THEN lmA = ld_consume_quota
   if (hh.adjLimit >0) {
      if (lmA >hh.adjLimit)
         lmA =hh.adjLimit;
   }
   else if (lmA >hh.creditLimit) {
      lmA =hh.creditLimit;
   }
   hh.creditCash =lmA;

   lmA =lmA - hh.totAmtPrecash - lmTmpPrecash;
   if (lmA >0) {
      if (lmA > hh.canUseLimit)
         hh.canUseCash =hh.canUseLimit;
      else hh.canUseCash =commString.numScale(lmA,0);
   }
   else {
      hh.canUseCash =commString.numScale(lmA,0);
   }
}

private void selectCcaAuthTxlog() {
   //已授權未請款金額/預借現金
   //ai_type: 1.一般卡, 2.商務卡
   //   8.	CardAcct_TOT_AMT_PRECASH已授權未請款預借現金-個人
   //   select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and card_acct_idx = 個人的IDX
   //   9.	CardAcctI_TOT_AMT_PRECASH已授權未請款預借現金-公司
   //   select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and card_acct_idx in (select card_acct_idx from card_acct_index where acct_parent_idx = 公司的IDX)
   //   10.	CardAcct_TOT_AMT_CONSUME已授權未請款含預借現金-個人
   //   select sum(nt_amt) from auth_txlog where CACU_AMOUNT=’Y’ and card_acct_idx = 個人的IDX
   //   11.	CardAcctI_TOT_AMT_CONSUME已授權未請款含預借現金-公司
   //   select sum(nt_amt) from auth_txlog where CACU_AMOUNT=’Y’ and card_acct_idx in (select card_acct_idx from card_acct_index where acct_parent_idx = 公司的IDX)

   strSql = "select sum(decode(cacu_cash,'N',0,nt_amt)) as xx_tot_precash"
         + ", sum(decode(cacu_amount,'N',0,nt_amt)) as xx_tot_amt"
         + " from cca_auth_txlog"
         + " where mtch_flag not in ('Y','U')";
   strSql +=" and card_acct_idx =? and card_acct_idx>0 and cacu_flag <> 'Y' ";

   setDouble(1, hh.cardAcctIdx);
   sqlSelect(strSql);
   if (sqlRowNum > 0) {
      hh.totAmtConsume = colNum("xx_tot_amt");
      hh.totAmtPrecash = colNum("xx_tot_precash");
   }
   else {
      hh.totAmtConsume = 0;
      hh.totAmtPrecash = 0;
   }

}

private void getCardConsume() {
   //-子卡餘額-
   if (hh.cardLimit <0)
      return;

   strSql ="select sum(decode(cacu_amount,'N',0,nt_amt)) as xx_card_consume"
         +" from cca_auth_txlog"
         +" where card_no =?"
         +" and tx_date like to_char(sysdate,'yyyymm')||'%'"
         ;
   setString(1,hh.cardNo);
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      hh.cardTotConsume =colNum("xx_card_consume");
   }
}

private void selectActPayDetail() {
   if (ibDebitFlag)
      return;
   if (eq(hh.acnoFlag,"Y")) {
      return;
   }
/**
   SELECT sum(txn_amt) FROM act_pay_ibm WHERE 1=1 AND p_seqno =:p_seqno AND proc_mark <>'Y' and error_code in ('','0','N')
      -->如果櫃枱輸入錯誤, 有可能產生超額
   select sum(pay_amt) from act_pay_detail where acno_p_seqno = 個人  再加上
   select sum(pay_amt) from act_debt_cancel where nvl(process_flag,'N') != 'Y' and acno_p_seqno = 個人
* */

   double lmAmt1=0, lmAmt2=0;
   strSql ="select sum(A.pay_amt) as xx_pay_amt1 "
         +" from act_pay_detail A join act_pay_batch B on A.batch_no=B.batch_no "
         +" where B.batch_tot_cnt >0";  //p_seqno =?";
   strSql +=" and A.p_seqno =?";
   setString(1,hh.acctPSeqno);

   sqlSelect(strSql);
   if (sqlRowNum >0) {
      lmAmt1 =colNum("xx_pay_amt1");
   }

   strSql ="select sum(pay_amt) as xx_pay_amt2"
         +" from act_debt_cancel"
         +" where process_flag <>'Y' and p_seqno =?"
         ;
   setString(1,hh.acctPSeqno);

   sqlSelect(strSql);
   if (sqlRowNum >0) {
      lmAmt2 =colNum("xx_pay_amt2");
   }
   //--
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
   
   //--
   double lmAmt4=0;
   strSql = "select sum(pay_amt) as xx_pay_amt4 from cca_card_acct where acno_p_seqno = ? ";
   sqlSelect(strSql,hh.acctPSeqno);
   if (sqlRowNum >0) {
	   lmAmt4 =colNum("xx_pay_amt4");
   }
   
   hh.totUnpaidAmt =lmAmt1 +lmAmt2+ lmAmt3+ lmAmt4;
}

private void selectActAcct() {
   if (ibDebitFlag)
      return;
   if (eq(hh.acnoFlag,"Y")) {
      //???商務卡繳固定人帳, 未設定
      return;
   }
/*
==>24. AuConsume_PRE_PAY_AMT: 溢繳款-個人(預付款金額)
   select end_bal_op+end_bal_lk from newtable where acno_p_seqno =  再加上
   select end_bal_op+end_bal_lk from act_acct where gp_no = 個人gp_no
* */
   strSql ="select end_bal_op+end_bal_lk as xx_pre_pay_amt"
         +" from act_acct"
         +" where p_seqno =?";
   setString(1,hh.acctPSeqno);
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      hh.prePayAmt =colNum("xx_pre_pay_amt");
   }
}

private void selectCcaCardBase(String aCardNo) {
   strSql ="select A.acno_p_seqno, A.debit_flag, A.card_acct_idx"
         +", A.p_seqno as acct_p_seqno, A.corp_p_seqno, A.acct_type, A.acno_flag "
         +", A.CARD_ADJ_LIMIT, A.CARD_ADJ_DATE1, A.CARD_ADJ_DATE2"
         +", B.INDIV_CRD_LMT, B.SON_CARD_FLAG"
         +" from cca_card_base A left join crd_card B on B.card_no=A.card_no"
         +" where A.card_no =?";
   setString(1,aCardNo);
   sqlSelect(strSql);
   if (sqlRowNum <=0) {
      errmsg("card no not exist[%s]",aCardNo);
      return;
   }

   hh.acnoPSeqno =colStr("acno_p_seqno");
   hh.acctPSeqno =colNvl("acct_p_seqno",hh.acnoPSeqno);
   ibDebitFlag =colEq("debit_flag","Y");
   hh.cardAcctIdx =colNum("card_acct_idx");
   hh.corpPSeqno =colStr("corp_p_seqno");
   hh.acnoFlag =colNvl("acno_flag","1");
   hh.acctType =colNvl("acct_type","01");

   if (colEq("son_card_flag","Y")) {
      hh.cardLimit =colNum("indiv_crd_lmt");
      if (colNum("card_adj_limit") >0
      && commString.between(sysDate,colStr("card_adj_date1"),colStr("card_adj_date2"))) {
         hh.cardLimit =colNum("card_adj_limit");
      }
      getCardConsume();
   }

   return;
}

private void selectActAcno() {
   //20. AuConsume_IBM_RECEIVE_AMT: 指撥額度-個人
   if (!ibDebitFlag) {
      strSql ="select line_of_credit_amt, line_of_credit_amt_cash, combo_cash_limit"
            +", combo_indicator, class_code"
            +" from act_acno"
            +" where acno_p_seqno =?";

      setString(1,hh.acnoPSeqno);
      sqlSelect(strSql);
      if (sqlRowNum <1) {
         errmsg("act_acno N-find");
         return;
      }
      hh.creditLimit =colNum("line_of_credit_amt");
      hh.creditCash =colNum("line_of_credit_amt_cash");
      hh.ibmReceiveAmt =colNum("combo_cash_limit");
      hh.classCode =colStr("class_code");
   }
   else {
      hh.creditLimit =0;
   }

   //-臨調--
   strSql ="select tot_amt_month"
         +", ADJ_EFF_END_DATE, ADJ_EFF_START_DATE"
         +","+commSqlStr.sysYYmd+" as xx_sysdate"
         +", ccas_class_code, CLASS_VALID_DATE"
         +" from cca_card_acct"
         +" where card_acct_idx =?";
   setDouble(1,hh.cardAcctIdx);

   sqlSelect(strSql);
   if (sqlRowNum <=0) {
      return;
   }

   String lsSysDate =colStr("xx_sysdate");
   //-臨時卡人等級-
   String lsClass =colStr("ccas_class_code");
   String lsDate =colNvl("class_valid_date",lsSysDate);
   if (notEmpty(lsClass) && commString.strComp(lsDate,lsSysDate)>=0) {
      hh.classCode =lsClass;
   }

   //-臨調期間-
   String lsDate1 =colStr("adj_eff_start_date");
   String lsDate2 =colStr("adj_eff_end_date");
   if (!commString.between(lsSysDate,lsDate1,lsDate2)) {
      return;
   }

   if (ibDebitFlag) {
      hh.adjLimit =hh.creditLimit * colNum("tot_amt_month") / 100;
   }
   else {
      hh.adjLimit =colNum("tot_amt_month");
   }
   
   //--計算專款專用額度
   calSpecialAmt();
   
}

private void selectActDebt() {
   if (ibDebitFlag)
      return;
/*
==>13.AuConsume_PAID_CONSUME_FEE: 已關帳未繳款不含預借現金-個人
==>14.AuConsume_UNPAID_CONSUME_FEE: 已請款未關帳不含預借現金-個人
==>16.AuConsume_PAID_PRECASH: 已關帳未繳款預借現金-個人
==>18. AuConsume_UNPAID_PRECASH: 已請款未關帳預借現金-個人
     */
   //-代收代付: 未遲繳不列入佔額-
//   strSql ="select sum(end_bal) as xx_paid_consume"
//         +", sum(decode(acct_code,'CA',end_bal,0)) as xx_paid_precash"
//         +" from act_debt"
//         +" where 1=1" +
//         " and acct_code in (select acct_code from ptr_actcode where interest_method='Y')"
//         +" and acct_code <>'ID' and substr(bill_type,2,1) <>'2'"  //-代收-
//         ;
//   if (eq(hh.acnoFlag,"Y")) {
//      strSql +=" and acno_p_seqno=?";
//      setString(1,hh.acnoPSeqno);
//   }
//   else {
//      strSql +=" and p_seqno =?";
//      setString(1,hh.acctPSeqno);
//   }
//   sqlSelect(strSql);
//   if (sqlRowNum >0) {
//      hh.paidConsume =colNum("xx_paid_consume");      
//      hh.paidPrecash =colNum("xx_paid_precash");      
//   }
//   //-JH:22.0520:代收代付遲繳-
//   strSql ="select sum(A.end_bal) as xx_paid_id2"
//           +" from act_debt A join ptr_workday B on A.stmt_cycle=B.stmt_cycle"
//           +" where 1=1"
//           +" and A.acct_code ='ID' and substr(A.bill_type,2,1) ='2'"
//           +" and (A.acct_month <B.this_acct_month"
//           +" or (A.acct_month =B.this_acct_month and to_char(sysdate,'yyyymmdd') >B.this_delaypay_date) )"
//           ;
//   if (eq(hh.acnoFlag,"Y")) {
//      strSql +=" and A.acno_p_seqno=?";
//      setString(1,hh.acnoPSeqno);
//   }
//   else {
//      strSql +=" and A.p_seqno =?";
//      setString(1,hh.acctPSeqno);
//   }
//   sqlSelect(strSql);
//   if (sqlRowNum >0) {
//      hh.paidConsume +=colNum("xx_paid_id2");
//   }
   
   double tlEndBalSpec = 0.0 , aAcctJrnlBal = 0.0 ;
   strSql = "select sum(end_bal_spec) as tl_end_bal_spec from act_acct_sum where p_seqno = ? ";
   setString(1,hh.acctPSeqno);
   sqlSelect(strSql);
   if(sqlRowNum >0)
	   tlEndBalSpec = colNum("tl_end_bal_spec");
   
   //--排除溢繳款
   strSql = "select (acct_jrnl_bal+end_bal_op) as acct_jrnl_bal from act_acct where p_seqno = ? ";
   setString(1,hh.acctPSeqno);
   sqlSelect(strSql);
   if(sqlRowNum >0)
	   aAcctJrnlBal = colNum("acct_jrnl_bal");
   hh.acctJrnlBal = aAcctJrnlBal;
   hh.paidConsume += (aAcctJrnlBal - tlEndBalSpec);   	
   
   //--預借現金
   strSql ="select sum(end_bal) as xx_paid_consume"
		  +", sum(decode(acct_code,'CA',end_bal,0)) as xx_paid_precash"
		  +" from act_debt"
		  +" where 1=1" +
		  " and acct_code in (select acct_code from ptr_actcode where interest_method='Y')"
		  +" and acct_code <>'ID' and substr(bill_type,2,1) <>'2'"  //-代收-
		  ;
   if (eq(hh.acnoFlag,"Y")) {
	   strSql +=" and acno_p_seqno=?";
	   setString(1,hh.acnoPSeqno);
   }	else {
	   strSql +=" and p_seqno =?";
	   setString(1,hh.acctPSeqno);
   }
   sqlSelect(strSql);	
   if (sqlRowNum >0) {
	   hh.paidPrecash =colNum("xx_paid_precash");      
   }
}

private void selectBilContract() {
//   if (ibDebitFlag || !eq(hh.acnoFlag,"1"))
//      return;
   if (ibDebitFlag)
      return;
   //22. AuConsume_UNPOST_INST_FEE: 分期付款未到期-個人
   //-2019-0904-
   //-installment_kind:F.自費分期+欠款分期還款-
   strSql = " select sum("
         + " (install_tot_term - install_curr_term) * unit_price"
         + " +remd_amt +decode(install_curr_term,0,first_remd_amt+extra_fees,0)"
         + " ) as xx_inst_unpost "
         + " from bil_contract "
         + " where 1=1 "
//         + "auth_code not in ('','N','REJECT','P','reject','LOAN') "
         +" and install_tot_term <> install_curr_term"
         +" and ("
         + " (post_cycle_dd >0 or installment_kind ='F')  "
         + " or (post_cycle_dd=0 AND DELV_CONFIRM_FLAG='Y' AND auth_code='DEBT') "
         +" ) and spec_flag <> 'Y' "
         ;
   if (eq(hh.acnoFlag,"Y")) {
      strSql +=" and acno_p_seqno =?";
      setString(1,hh.acnoPSeqno);
   }
   else {
      strSql +=" and p_seqno =?";
      setString(1,hh.acctPSeqno);
   }
   sqlSelect(strSql);
   if (sqlRowNum >0) {
      hh.unpostInstFee =colNum("xx_inst_unpost");
   }
}

//--爭議款
private void selectRskProblem() {
//	strSql = " select sum(prb_amount) as tl_prb_amount from rsk_problem where add_apr_date <>'' and close_apr_date ='' "
//		   + " and card_no in (select card_no from crd_card where acno_p_seqno =?) "
//		   + " and reference_no not in (select reference_no from act_debt where 1=1) "
//		   ;
//	
//	setString(1,hh.acnoPSeqno);
//	
//	sqlSelect(strSql);
//	if(sqlRowNum >0) {
//		hh.problemAmt = colNum("tl_prb_amount");
//	}
	
	//--計算批次已處理之爭議款
	strSql = " select (unbill_end_bal+billed_end_bal) as dp_end_bal from act_acct_sum where p_seqno = ? and acct_code ='DP' ";
	setString(1,hh.acnoPSeqno);
	
	sqlSelect(strSql);
	if(sqlRowNum >0) {
		hh.dbEndBal = colNum("dp_end_bal");
	}
	
	hh.problemAmt = hh.problemAmt + hh.dbEndBal;
	
}

//--專款專用
private void calSpecialAmt() {
	String riskType = "" , date1 = "" , date2 = "";
	double maxMonthAmt = 0 , useAmt = 0  ;
	int txnCnt = 0;
	
	String sql1 = "select risk_type , adj_month_amt , adj_eff_start_date , adj_eff_end_date from cca_adj_parm where card_acct_idx = ? and spec_flag ='Y' ";
	String sql2 = "select sum(nt_amt) as tl_spec_amt from cca_auth_txlog where card_acct_idx = ? and tx_date >= ? and tx_date <= ? and risk_type = ? and cacu_amount <>'N' ";
	String sql3 = "select count(*) as db_txn_cnt from cca_auth_txlog where card_acct_idx = ? and tx_date >= ? and tx_date <= ? and risk_type = ? and cacu_amount <>'N' ";
	sqlSelect(sql1,new Object[] {hh.cardAcctIdx});
	
	int cnt = sqlRowNum;
	
	for(int ii=0 ;ii < cnt ; ii++) {
		txnCnt = 0;
		riskType = colStr(ii,"risk_type");
		maxMonthAmt = colNum(ii,"adj_month_amt");
		hh.totalSpecialAmt += maxMonthAmt ;
		date1 = colStr(ii,"adj_eff_start_date");
		date2 = colStr(ii,"adj_eff_end_date");
		sqlSelect(sql2,new Object[] {hh.cardAcctIdx,date1,date2,riskType});
		if(sqlRowNum >0) {
			useAmt =colNum("tl_spec_amt");
		}
		sqlSelect(sql3,new Object[] {hh.cardAcctIdx,date1,date2,riskType});
		if(sqlRowNum >0) {
			txnCnt = colInt("db_txn_cnt");
		}						
		
		if(useAmt > maxMonthAmt) {
			hh.overSpecialAmt += (useAmt - maxMonthAmt);
		}
		
		hh.specialAmt += useAmt ;
		
//		//--有消費不扣專款專用總額度 , 沒消費扣專款專用總額度
//		if(txnCnt <= 0) {
//			hh.specialAmt += maxMonthAmt;
//		}	else	{
//			if(useAmt < maxMonthAmt) {
//				hh.specialAmt += (maxMonthAmt - useAmt);
//			}
//		}
		
	}
	
	String sql4 = " select sum(install_curr_term * unit_price) as inst_fee_spec , sum(unit_price) as tl_unit_price from bil_contract where 1=1 "				
				+ " and install_tot_term <> install_curr_term "
				+ " and ((post_cycle_dd >0 or installment_kind ='F') or (post_cycle_dd=0 AND DELV_CONFIRM_FLAG='Y' AND auth_code='DEBT')) "
				+ " and spec_flag = 'Y' "
				+ " and p_seqno = ? "
				;
	
	sqlSelect(sql4 , new Object[] {hh.acnoPSeqno});
	if(sqlRowNum > 0) {
		hh.postInstFeeSpec = colNum("inst_fee_spec");
		hh.returnAmt = colNum("tl_unit_price");
	}
	
	if(hh.postInstFeeSpec > hh.totalSpecialAmt)
		hh.returnSpecAmt = (hh.postInstFeeSpec-hh.totalSpecialAmt);
	
	if(hh.returnDate.compareTo(getSysDate()) <0 || hh.returnDate.isEmpty())
		hh.returnAmt = 0;
	
}

}
