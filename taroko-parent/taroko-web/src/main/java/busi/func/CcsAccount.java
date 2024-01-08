/** 原ECS_2CCAS資料
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
 * V.2018-1001.jh
 *  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
 * 109-04-20  V1.00.01  Zuwei       code format                              *
 * 109-12-25   V1.00.02 Justin        zz->comm
 * */
package busi.func;

import busi.FuncBase;

public class CcsAccount extends FuncBase {

  taroko.base.CommDate commDate = new taroko.base.CommDate();

  public String acnoPseqno = "";
  String compMonth12 = "", compMonth24 = "", busiDate = "";
  String[] compYymm = new String[12];

  // public double to_which =0; //INT 系統類別
  // public double dog =0; //TIMESTAMP 產生系統日期
  // public double dop =0; //TIMESTAMP 接收系統日期
  // public String process_mode =0; //VARCHAR(1) 處理模式
  // public String process_status =0; //VARCHAR(1) 處理結果
  // public String rule =0; //VARCHAR(1) 處理方式
  // public String payment_rule =0; //VARCHAR(1) 繳款方式
  // public String account_type =0; //VARCHAR(2) 帳戶類別
  // public String card_acct_id =0; //VARCHAR(19) 卡戶ID
  // public String card_hldr_id =0; //VARCHAR(20) 卡人ID
  // public String card_acct_level =0; //VARCHAR(2) 卡戶等級
  // public String position =0; //VARCHAR(24) 職稱
  // public String card_acct_since =0; //VARCHAR(8) 最早往來日
  // public String status_01 =0; //VARCHAR(2) (狀態碼)分期還款戶
  // public String status_02 =0; //VARCHAR(1) (狀態碼)允RC
  // public String status_03 =0; //VARCHAR(1) (狀態碼)行員
  // public String status_04 =0; //VARCHAR(1) (狀態碼)授信戶
  // public String transfer =0; //VARCHAR(1) (狀態碼)新轉戶
  // public String status_11 =0; //VARCHAR(1) (狀態碼)永不禁超
  // public String status_12 =0; //VARCHAR(1) (狀態碼)永不強停
  // public String status_13 =0; //VARCHAR(1) (狀態碼)永不解超
  // public String status_14 =0; //VARCHAR(1) (狀態碼)永不調額
  // public String status_reason =0; //VARCHAR(2) (狀態碼)VIP
  // public String auto_pay_bankid =0; //VARCHAR(19) 自動扣款ACCOUNT
  public double maxConsumeAmt = 0; // DECIMAL(9) 最近一年最高月消費額 :act_anal_sub
  public String maxConsumeDate = ""; // VARCHAR(6) 最高消費年月 :act_anal_sub
  public double maxPrecashAmt = 0; // DECIMAL(9) 最近一年最高月ATM金額 :act_anal_sub
  public String maxPrecashDate = ""; // VARCHAR(6) 最高ATM年月 :act_anal_sub
  public double closePunishFee = 0; // DECIMAL(9) 結帳(違約金) :act_acct_sum
  public double closeInterestFee = 0; // DECIMAL(9) 結帳(利息) :act_acct_sum
  public double closeSrvFee = 0; // DECIMAL(9) 結帳(費用) :act_acct_sum
  public double closeLawFee = 0; // DECIMAL(9) 結帳(催收款項) :act_acct_sum
  public double closeConsumeFee = 0; // DECIMAL(9) 結帳(消費) :act_acct_sum
  public double closePrecash = 0; // DECIMAL(9) 結帳(ATM) :act_acct_sum
  public double closeWritsoff = 0; // DECIMAL(9) 結帳(呆帳) :act_acct_sum
  public double openPunishFee = 0; // DECIMAL(9) 未結帳(違約金) :act_acct_sum
  public double openInterestFee = 0; // DECIMAL(9) 未結帳(利息) :act_acct_sum
  public double openSrvFee = 0; // DECIMAL(9) 未結帳(費用) :act_acct_sum
  public double openLawFee = 0; // DECIMAL(9) 未結帳(催收款項) :act_acct_sum
  public double openConsumeFee = 0; // DECIMAL(9) 未結帳(消費) :act_acct_sum
  public double openPrecash = 0; // DECIMAL(9) 未結帳(ATM) :act_acct_sum
  public double openWritsoff = 0; // DECIMAL(9) 未結帳(呆帳) :act_acct_sum
  public double billLawPayAmt = 0; // DECIMAL(9) 最低應繳額 :act_acct.min_pay_bal
  // public String mcode =""; //VARCHAR(3) 逾期繳款月數
  public double argueAmt = 0; // DECIMAL(9) 爭議金額 :act_acct_sum
  public double prePayAmt = 0; // DECIMAL(9) 溢繳款 :act_acct
  // public String lastest_1_mnth =""; //VARCHAR(2) 繳款行為近1個月代碼
  // public String lastest_2_mnth =""; //VARCHAR(2) 繳款行為近2個月代碼
  // public String lastest_3_mnth =""; //VARCHAR(2) 繳款行為近3個月代碼
  // public String lastest_4_mnth =""; //VARCHAR(2) 繳款行為近4個月代碼
  // public String lastest_5_mnth =""; //VARCHAR(2) 繳款行為近5個月代碼
  // public String lastest_6_mnth =""; //VARCHAR(2) 繳款行為近6個月代碼
  // public String lastest_7_mnth =""; //VARCHAR(2) 繳款行為近7個月代碼
  // public String lastest_8_mnth =""; //VARCHAR(2) 繳款行為近8個月代碼
  // public String lastest_9_mnth =""; //VARCHAR(2) 繳款行為近9個月代碼
  // public String lastest_10_mnth =""; //VARCHAR(2) 繳款行為近10個月代碼
  // public String lastest_11_mnth =""; //VARCHAR(2) 繳款行為近11個月代碼
  // public String lastest_12_mnth =""; //VARCHAR(2) 繳款行為近12個月代碼
  public double payLastestAmt = 0; // DECIMAL(9) 最近一次付款金額 :act_anal_sub
  // --acno.last_pay_amt
  public String payDate = ""; // VARCHAR(8) 最近一次付款日期 :act_anal_sub
  // -acno.last_pay_date
  // public double lmt_tot_consume =0; //DECIMAL(10) 總消費額度 :act_acno.line_of_credit_amt
  // public double lmt_tot_consume_cash=0; //DECIMAL(10) 預借現金總消費限額 :acno.line_of_credit_cash
  public double billLowLimit = 0; // DECIMAL(10) 月平均消費額 :act_acno
  public String paySettleDate = ""; // VARCHAR(8) 結帳日期 :ptr_workday
  // -acno.h_wday_this_close_date-
  public String paymentDueDate = ""; // VARCHAR(8) 最近付款到期日 :h_wday_this_lastpay_date
  public double totalUnpaidAmt = 0; // DECIMAL(16) PAYMENT未消 :???
  public double totLimitAmt = 0; // DECIMAL(9) 前一年累計消費額 :act_anal_sub
  public double totPrecashAmt = 0; // DECIMAL(9) 前二年累計消費額 :act_anal_sub
  public double consume1 = 0; // DECIMAL(9) 前1月單月消費額 :act_anal_sub
  public double consume2 = 0; // DECIMAL(9) 前2月單月消費額 :act_anal_sub
  public double consume3 = 0; // DECIMAL(9) 前3月單月消費額 :act_anal_sub
  public double consume4 = 0; // DECIMAL(9) 前4月單月消費額 :act_anal_sub
  public double consume5 = 0; // DECIMAL(9) 前5月單月消費額 :act_anal_sub
  public double consume6 = 0; // DECIMAL(9) 前6月單月消費額 :act_anal_sub
  public double totDue = 0; // DECIMAL(9) 截至目前本年累計消費額 :act_anal_sub
  // public String auth_remark =""; //VARCHAR(40) 備註
  // public String corp_name =""; //VARGRAPHIC(40) 公司名稱
  // public String bill_address =""; //VARGRAPHIC(70) 住址
  // public String acct_address =""; //VARCHAR(70) 正卡戶籍住址
  public String overDue = ""; // VARCHAR(2) 愈期戶 :act_acno.acct_status[3,4]
  // public String branch =""; //VARCHAR(3) 分局代碼
  // public String organ_id =""; //VARCHAR(10) 主織別ID
  // public String acct_no =""; //VARCHAR(20) 帳戶號碼
  public double unpostInstFee = 0; // DECIMAL(10) 未入帳分期手續費 :bil_contract

  // public String sale_date =""; //VARCHAR(8) 出售帳戶日期
  // public String bill_sending_zip =""; //VARCHAR(5) 帳戶對帳單寄送地址郵遞區號
  // public String auto_installment =""; //VARCHAR(1) 自動分期付款
  // public String pd_rating =""; //VARCHAR(2) PD_RATING
  // public String new_vdchg_flag =""; //VARCHAR(1)
  // ----------------

  public void procData(String aPSeqno) {
    dataClear();
    acnoPseqno = aPSeqno;
    strSql = "select business_date from ptr_businday where 1=1" + commSqlStr.rownum(1);
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      busiDate = colStr("business_date");
    }
    selectActAcno(aPSeqno);
    selectActAcctSum(colStr("p_seqno"));
    selectBilContract(colStr("p_seqno"));
  }

  void selectActAcno(String aPSeqno) {

    strSql =
        "select A.last_pay_amt, A.last_pay_date," + " A.acct_status, A.month_purchase_lmt,"
            + " A.p_seqno" + ", B.this_close_date, B.this_acct_month, B.this_lastpay_date"
            + " from act_acno A left join ptr_workday B on B.stmt_cycle=A.stmt_cycle"
            + " where acno_p_seqno =?";
    setString(aPSeqno);
    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return;

    this.payLastestAmt = colNum("last_pay_amt");
    this.payDate = colStr("last_pay_date");
    this.paySettleDate = colStr("this_close_date");
    this.paymentDueDate = colStr("this_lastpay_date");

    compMonth12 = commString.left(commDate.dateAdd(colStr("this_acct_month"), 0, -12, 0), 6);
    compMonth24 = commString.left(commDate.dateAdd(colStr("this_acct_month"), 0, -24, 0), 6);
    for (int ii = 1; ii < 12; ii++)
      compYymm[ii] = commString.left(commDate.dateAdd(colStr("this_acct_month"), 0, 0 - ii, 0), 6);

    if (commString.strIn2(colStr("acct_status"), ",3,4"))
      this.overDue = "Y";
    else
      overDue = "N";

    this.billLowLimit = colNum("month_purchase_lmt");

    // lmt_tot_consume =col_num("line_of_credit_amt");
    // lmt_tot_consume_cash =col_num("line_of_credit_amt_cash");

    procActAnalSub1(colStr("p_seqno"));
  }

  void selectActAcctSum(String aPseqno) {
    strSql =
        ""
            + "SELECT sum(case when acct_code in ('AF','LF','CF','PF') then nvl(billed_end_bal,0) else 0 end) as close_srv_fee,"
            + " sum(case when acct_code in ('AF','LF','CF','PF') then nvl(unbill_end_bal,0) else 0 end) as open_srv_fee,"
            + " sum(case when acct_code in ('BL','ID','IT','OT','AO') then nvl(billed_end_bal,0) else 0 end) as close_consume_fee,"
            + " sum(case when acct_code in ('BL','ID','IT','OT','AO') then nvl(unbill_end_bal,0) else 0 end) as open_consume_fee,"
            + " sum(decode(acct_code,'CA',nvl(billed_end_bal,0),0)) as close_precash,"
            + " sum(decode(acct_code,'CA',nvl(unbill_end_bal,0),0)) as open_precash,"
            + " sum(decode(acct_code,'RI',nvl(billed_end_bal,0),0)) as close_interest_fee,"
            + " sum(decode(acct_code,'RI',nvl(unbill_end_bal,0),0)) as open_interest_fee,"
            + " sum(decode(acct_code,'PN',nvl(billed_end_bal,0),0)) as close_punish_fee,"
            + " sum(decode(acct_code,'PN',nvl(unbill_end_bal,0),0)) as open_punish_fee,"
            + " sum(decode(acct_code,'DB',nvl(billed_end_bal,0),0)) as close_writsoff,"
            + " sum(decode(acct_code,'DB',nvl(unbill_end_bal,0),0)) as open_writsoff,"
            + " sum(decode(acct_code,'DP',nvl(billed_end_bal,0)+nvl(unbill_end_bal,0),0)) as argue_amt,"
            + " sum(case when acct_code in ('AI','SF','CB','CT','CC') then nvl(billed_end_bal,0) else 0 end) as close_law_fee,"
            + " sum(case when acct_code in ('AI','SF','CB','CT','CC') then nvl(unbill_end_bal,0) else 0 end) as open_law_fee,"
            + " sum(case when acct_code in ('BL','CA','IT','ID','OT','AO','CB','DB') then nvl(unbill_end_bal,0) else 0 end) as unbill_end_bal"
            + " from act_acct_sum" + " where p_seqno =?";
    setString2(1, aPseqno);
    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return;
    this.closeSrvFee = colNum("close_srv_fee");
    this.openSrvFee = colNum("open_srv_fee");
    this.closeConsumeFee = colNum("close_consume_fee");
    this.openConsumeFee = colNum("open_consume_fee");
    this.closePrecash = colNum("close_precash");
    this.openPrecash = colNum("open_precash");
    this.closeInterestFee = colNum("close_interest_fee");
    this.openInterestFee = colNum("open_interest_fee");
    this.closePunishFee = colNum("close_punish_fee");
    this.openPunishFee = colNum("open_punish_fee");
    this.closeWritsoff = colNum("close_writsoff");
    this.openWritsoff = colNum("open_writsoff");
    this.argueAmt = colNum("argue_amt");
    this.closeLawFee = colNum("close_law_fee");
    this.openLawFee = colNum("open_law_fee");
  }

  void procActAnalSub1(String aPseqno) {
    strSql =
        "SELECT acct_month," + " sum(nvl(his_purchase_amt,0)) as his_purch_amt,"
            + " sum(nvl(his_cash_amt,0)) as his_cash_amt,"
            + " sum(nvl(his_purchase_amt,0)+nvl(his_cash_amt,0)) as his_amt"
            + " FROM   act_anal_sub" + " WHERE  p_seqno =?" + " and acct_month >=?"
            + " group by acct_month" + " order by acct_month";
    setString2(1, aPseqno);
    setString(compMonth24);
    sqlSelect(strSql);
    int llNrow = sqlRowNum;
    for (int ll = 0; ll < llNrow; ll++) {
      String lsAcctMm = colStr(ll, "acct_month");
      double lmAmt = colNum(ll, "his_amt");
      if (commString.strComp(lsAcctMm, compMonth12) < 0) {
        this.totPrecashAmt += lmAmt;
      } else
        this.totLimitAmt += lmAmt;
      if (commString.strComp(lsAcctMm, compMonth12) < 0)
        continue;
      lmAmt = colNum(ll, "his_purch_amt");
      if (lmAmt > this.maxConsumeAmt) {
        maxConsumeAmt = lmAmt;
        maxConsumeDate = commString.left(commDate.dateAdd(lsAcctMm, 0, 1, 0), 6);
      }
      lmAmt = colNum(ll, "his_cash_amt");
      if (lmAmt > this.maxPrecashAmt) {
        maxPrecashAmt = lmAmt;
        maxPrecashDate = lsAcctMm;
      }

      if (eqIgno(lsAcctMm, compYymm[1]))
        consume1 += colNum(ll, "his_amt");
      else if (eqIgno(lsAcctMm, compYymm[2]))
        consume2 += colNum(ll, "his_amt");
      else if (eqIgno(lsAcctMm, compYymm[3]))
        consume3 += colNum(ll, "his_amt");
      else if (eqIgno(lsAcctMm, compYymm[4]))
        consume4 += colNum(ll, "his_amt");
      else if (eqIgno(lsAcctMm, compYymm[5]))
        consume5 += colNum(ll, "his_amt");
      else if (eqIgno(lsAcctMm, compYymm[6]))
        consume6 += colNum(ll, "his_amt");

      if (eqIgno(commString.left(lsAcctMm, 4), commString.left(busiDate, 4))) {
        this.totDue += colNum(ll, "his_amt");
      }

    }
  }

  void selectActAcct(String aPseqno) {
    strSql =
        "SELECT end_bal_op," + " end_bal_lk," + " min_pay_bal," + " ttl_amt_bal" + " FROM act_acct"
            + " WHERE  p_seqno =?";
    setString2(1, aPseqno);
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      this.billLawPayAmt = colNum("min_pay_bal");
      this.prePayAmt = colNum("end_bal_op") + colNum("end_bal_lk");
    }

  }

  void selectBilContract(String aPseqno) {
    strSql =
        "SELECT  sum(nvl(A.unit_price,0)*(nvl(A.install_tot_term,0) - "
            + " nvl(A.install_curr_term,0))+nvl(A.remd_amt,0)+decode(A.install_curr_term,0,A.first_remd_amt,0)) as unpost_inst_fee"
            + " from bil_contract A left join bil_merchant B on B.mcht_no=A.mcht_no"
            + " where A.p_seqno =?" + " and A.install_tot_term != A.install_curr_term"
            + " and (A.post_cycle_dd >0 or A.installment_kind ='F')"
            + " and B.loan_flag in ('','N','C')";
    setString2(1, aPseqno);
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      this.unpostInstFee = colNum("unpost_inst_fee");
    }
  }

  public void dataClear() {
    maxConsumeAmt = 0; // DECIMAL(9) 最近一年最高月消費額
    maxConsumeDate = ""; // VARCHAR(6) 最高消費年月
    maxPrecashAmt = 0; // DECIMAL(9) 最近一年最高月ATM金額
    maxPrecashDate = ""; // VARCHAR(6) 最高ATM年月
    closePunishFee = 0; // DECIMAL(9) 結帳(違約金)
    closeInterestFee = 0; // DECIMAL(9) 結帳(利息)
    closeSrvFee = 0; // DECIMAL(9) 結帳(費用)
    closeLawFee = 0; // DECIMAL(9) 結帳(催收款項)
    closeConsumeFee = 0; // DECIMAL(9) 結帳(消費)
    closePrecash = 0; // DECIMAL(9) 結帳(ATM)
    closeWritsoff = 0; // DECIMAL(9) 結帳(呆帳)
    openPunishFee = 0; // DECIMAL(9) 未結帳(違約金)
    openInterestFee = 0; // DECIMAL(9) 未結帳(利息)
    openSrvFee = 0; // DECIMAL(9) 未結帳(費用)
    openLawFee = 0; // DECIMAL(9) 未結帳(催收款項)
    openConsumeFee = 0; // DECIMAL(9) 未結帳(消費)
    openPrecash = 0; // DECIMAL(9) 未結帳(ATM)
    openWritsoff = 0; // DECIMAL(9) 未結帳(呆帳)
    billLawPayAmt = 0; // DECIMAL(9) 最低應繳額
    // mcode =""; //VARCHAR(3) 逾期繳款月數
    argueAmt = 0; // DECIMAL(9) 爭議金額
    prePayAmt = 0; // DECIMAL(9) 溢繳款
    // lastest_1_mnth =""; //VARCHAR(2) 繳款行為近1個月代碼
    // lastest_2_mnth =""; //VARCHAR(2) 繳款行為近2個月代碼
    // lastest_3_mnth =""; //VARCHAR(2) 繳款行為近3個月代碼
    // lastest_4_mnth =""; //VARCHAR(2) 繳款行為近4個月代碼
    // lastest_5_mnth =""; //VARCHAR(2) 繳款行為近5個月代碼
    // lastest_6_mnth =""; //VARCHAR(2) 繳款行為近6個月代碼
    // lastest_7_mnth =""; //VARCHAR(2) 繳款行為近7個月代碼
    // lastest_8_mnth =""; //VARCHAR(2) 繳款行為近8個月代碼
    // lastest_9_mnth =""; //VARCHAR(2) 繳款行為近9個月代碼
    // lastest_10_mnth =""; //VARCHAR(2) 繳款行為近10個月代碼
    // lastest_11_mnth =""; //VARCHAR(2) 繳款行為近11個月代碼
    // lastest_12_mnth =""; //VARCHAR(2) 繳款行為近12個月代碼
    payLastestAmt = 0; // DECIMAL(9) 最近一次付款金額
    payDate = ""; // VARCHAR(8) 最近一次付款日期
    // lmt_tot_consume =0; //DECIMAL(10) 總消費額度 act_acno.line_of_credit_amt
    billLowLimit = 0; // DECIMAL(10) 月平均消費額
    paySettleDate = ""; // VARCHAR(8) 結帳日期
    paymentDueDate = ""; // VARCHAR(8) 最近付款到期日
    totalUnpaidAmt = 0; // DECIMAL(16) PAYMENT未消
    totLimitAmt = 0; // DECIMAL(9) 前一年累計消費額
    totPrecashAmt = 0; // DECIMAL(9) 前二年累計消費額
    consume1 = 0; // DECIMAL(9) 前1月單月消費額
    consume2 = 0; // DECIMAL(9) 前2月單月消費額
    consume3 = 0; // DECIMAL(9) 前3月單月消費額
    consume4 = 0; // DECIMAL(9) 前4月單月消費額
    consume5 = 0; // DECIMAL(9) 前5月單月消費額
    consume6 = 0; // DECIMAL(9) 前6月單月消費額
    totDue = 0; // DECIMAL(9) 截至目前本年累計消費額
    overDue = ""; // VARCHAR(2) 愈期戶
    unpostInstFee = 0; // DECIMAL(10) 未入帳分期手續費
  }

}
