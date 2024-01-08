/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112-02-07  V1.00.00  Alex        initial                                  * 
*****************************************************************************/
package Cca;

import java.sql.Connection;

import com.AccessDAO;
import com.CommString;

public class CalBalance extends AccessDAO {
	String acctType ="";
	String acctKey = "";
	double acctAmt = 0.0;
	double lineOfCreditAmt = 0.0;
	String acnoPSeqno = "";
	double comboCashLimit = 0.0;
	double cardAcctIdx = 0.0;
	String adjEffStartDate = "";
	String adjEffEndDate = "";
	double totAmtMonth = 0.0;
	double txlogAmt = 0.0;
	double acctSpecSum = 0.0;
	double acctJrnlBal = 0.0;
	double overDraftNotSpec = 0.0;
	double prePayAmt = 0.0;
	double overPay = 0.0;
	double payAmt1 = 0.0;
	double payAmt2 = 0.0;
	double payAmt3 = 0.0;
	double instUnPost = 0.0;
	boolean lbAdj = false;
	double specAmt = 0.0;
	double tlConsumeAmt = 0.0;
	double availableBalance = 0.0;
	String cardNo = "";
	String corpPSeqno = "";
	String corpNo = "";
	String acnoFlag = "";
	double cardAdjLimit = 0.0;
	String cardAdjDate1 = "";
	String cardAdjDate2 = "";
	double indivCrdLmt = 0.0;
	String sonCardFlag = "";
	double cardLimit = 0.0;
	double cardTotConsume = 0.0;
	double cardAvailableBalance = 0.0;
	//--公司戶
	double corpAcctAmt = 0.0;
	double corpAvailableBalance = 0.0;
	String corpAcctType = "";
	String corpCorpNo = "";
	String corpAcnoPSeqno = "";
	double corpCardAcctIdx = 0.0;
	String corpCorpPSeqno = "";
	String corpAcnoFlag = "";
	double corpTotAmtMonth = 0.0;
	String corpAdjEffStartDate = "";
	String corpAdjEffEndDate = "";
	double corpLineOfCreditAmt = 0.0;
	String corpClassCode = "";
	boolean lbCorpAdj = false;
	double corpTxLogAmt = 0.0;
	double corpPaidConsume = 0.0;
	double corpPaidPreCash = 0.0;
	double corpTotPaidConsume = 0.0;
	double corpInstUnpost = 0.0;
	double corpPrePayAmt = 0.0;
	double corpPayAmt1 = 0.0;
	double corpPayAmt2 = 0.0;
	double corpPayAmt3 = 0.0;
	double corpAlreadyPay = 0.0;
	double corpTlConsumeAmt = 0.0;
	CommString commstring = null;
	
	public CalBalance(Connection conn[], String[] dbAlias) throws Exception {	
	    super.conn = conn;
	    setDBalias(dbAlias);
	    setSubParm(dbAlias);
	    return;
	}
	
	public double idNoBalance(String idNo) throws Exception {		
		if(idNo.length() != 10)
			return 0.0;
		dateTime();		
		commstring = new com.CommString();
		initData();
		acctType = "01";
		acctKey = idNo+"0";
		
		if(selectActAcno() != 0)
			return 0.0;
		
		if(selectCcaCardAcct() != 0)
			return 0.0;
						
		if(selectAuthTxLog() != 0)
			return 0.0;
		
		if(selectOverDraft() != 0)
			return 0.0;
		
		if(selectBilContract() != 0)
			return 0.0;
		
		if(selectOverPay() !=0)
			return 0.0;
		
		if(selectAlreadyPay() !=0)
			return 0.0;
		
		if(selectSpecAmt() !=0)
			return 0.0;
		
		calAvailableBalance();
		
		return availableBalance;
	}
	
	public double cardBalance(String aCardNo) throws Exception {
		if(aCardNo.isEmpty())
			return 0.0;
		dateTime();		
		commstring = new com.CommString();
		initData();
		cardNo = aCardNo;
		
		if(selectCcaCardBase() !=0)
			return 0.0;
		
		if(selectAuthTxLog() !=0)
			return 0.0;
		
		if(selectActAcnoCard() != 0)
			return 0.0;
		
		if(selectCcaCardAcct() != 0)
			return 0.0;
		
		if(selectOverDraft() != 0)
			return 0.0;
		
		if(selectBilContract() != 0)
			return 0.0;
		
		if(selectOverPay() !=0)
			return 0.0;
		
		if(selectAlreadyPay() !=0)
			return 0.0;
		
		if(selectSpecAmt() !=0)
			return 0.0;
		
		calAvailableBalance();
		
		if("Y".equals(sonCardFlag)) {
			cardAvailableBalance = cardLimit - cardTotConsume; 
			if(cardAvailableBalance > availableBalance)
				return availableBalance;
			else
				return cardAvailableBalance;
		}
		
		if("01".equals(acctType) == false) {
			corpCalBalance(corpNo,acctType);
			if(availableBalance > corpAvailableBalance)
				return corpAvailableBalance ;
			else
				return availableBalance ;
		}				
		
		return availableBalance;
	}
	
	public double corpCalBalance(String aCorpNo , String aAcctType) throws Exception {
		initDataCorp();
		corpAcctType = aAcctType ;
		corpCorpNo = aCorpNo ;
		
		if(corpCorpNo.isEmpty())
			return 0.0;
		
		if(selectCcaCardAcctCorp() != 0)
			return 0.0;
		
		if(selectAuthTxLogCorp() != 0)
			return 0.0;
		
		if(selectActDebtCorp() != 0)
			return 0.0;
		
		if(selectBilContractCorp() != 0)
			return 0.0;
		
		if(selectOverPayCorp() != 0)
			return 0.0;
		
		if(selectAlreadyPayCorp() != 0)
			return 0.0;
		
		calAvailableBalanceCorp();
		
		return corpAvailableBalance;		
	}
	
	public double totalCorpBalanceById(String aIdNo) throws Exception {
		double totalCorpAvailableBalance = 0.0;
		String idPSeqno = "" , acnoPSeqno = "" , cardNo = "";
			
		//--取得 id_p_seqno		
		idPSeqno = getIdPSeqno(aIdNo);
		if(idPSeqno.isEmpty())
			return 0;
		
		//--取得ID向下所有公司卡的acno_p_seqno
		sqlCmd = "select distinct acno_p_seqno from crd_card where id_p_seqno = ? and acct_type in ('03','06') ";
		setString(1,idPSeqno);
		int recordCnt = selectTable();
		if(recordCnt <=0)
			return 0;
		
		//--加總ID向下所有公司戶可用餘額
		for(int ii=0;ii<recordCnt;ii++) {
			acnoPSeqno = getValue("acno_p_seqno",ii);
			cardNo = getCardNo(acnoPSeqno);
			if(cardNo.isEmpty())
				continue;
			totalCorpAvailableBalance += cardBalance(cardNo);
		}		
		
		return totalCorpAvailableBalance;
	}
	
	public double totalCorpBalanceByCorp(String aCorp) throws Exception {
		double totalCorpAvailableBalance  = 0.0;
		String aCorpPSeqno = "" , aAcctType = "";
		//--取得 corp_p_seqno 
		aCorpPSeqno = getCorpPSeqno(aCorp);
		
		//--取得公司戶下所有的帳戶類別
		sqlCmd = "select distinct acct_type from crd_card where corp_p_seqno = ? ";
		setString(1,aCorpPSeqno);
		int recordCnt = selectTable();
		if(recordCnt <=0)
			return 0;
		
		//--計算公司戶可用餘額相加
		for(int ii=0;ii<recordCnt;ii++) {
			aAcctType = getValue("acct_type",ii);
			totalCorpAvailableBalance += corpCalBalance(aCorp, aAcctType);
		}		
		
		return totalCorpAvailableBalance;
	}
	
	int selectActAcno() throws Exception {
		
		sqlCmd = "select line_of_credit_amt , acno_p_seqno , combo_cash_limit from act_acno where acct_type = ? and acct_key = ? ";
		setString(1,acctType);
		setString(2,acctKey);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			lineOfCreditAmt = getValueDouble("line_of_credit_amt");
			acnoPSeqno = getValue("acno_p_seqno");
			comboCashLimit = getValueDouble("combo_cash_limit");
			return 0;
		}
		
		return -1;
	}
	
	int selectActAcnoCard() throws Exception {
		
		sqlCmd = "select line_of_credit_amt , acno_p_seqno , combo_cash_limit from act_acno where acno_p_seqno = ? ";
		setString(1,acnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			lineOfCreditAmt = getValueDouble("line_of_credit_amt");			
			comboCashLimit = getValueDouble("combo_cash_limit");
			return 0;
		}
		
		return -1;
	}
	
	int selectCcaCardAcct() throws Exception {
		if(acnoPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select card_acct_idx , adj_eff_start_date , adj_eff_end_date , tot_amt_month from cca_card_acct ";
		sqlCmd += "where acno_p_seqno = ? ";
		setString(1,acnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			cardAcctIdx = getValueDouble("card_acct_idx");
			adjEffStartDate = getValue("adj_eff_start_date");
			adjEffEndDate = getValue("adj_eff_end_date");
			totAmtMonth = getValueDouble("tot_amt_month");
			
			if(adjEffStartDate.isEmpty() == false && adjEffEndDate.isEmpty() == false) {
				if(commstring.between(sysDate, adjEffStartDate, adjEffEndDate)) {
					lbAdj = true ;
				}
			}
						
			return 0;
		}
		
		return -1;
	}
	
	int selectCcaCardAcctCorp() throws Exception {		
		if(corpCorpNo.isEmpty())
			return -1;
		
		sqlCmd = "select A.acno_p_seqno , A.card_acct_idx , A.corp_p_seqno , A.acct_type , ";
		sqlCmd += "A.acno_flag , A.tot_amt_month , A.adj_eff_start_date , A.adj_eff_end_date , C.line_of_credit_amt , ";
		sqlCmd += "C.class_code from cca_card_acct A join crd_corp B on A.corp_p_seqno=B.corp_p_seqno and A.acno_flag='2' ";
		sqlCmd += "join act_acno C on C.acno_p_seqno=A.acno_p_seqno where A.acct_type =? and B.corp_no =? ";
		
		setString(1,corpAcctType);
		setString(2,corpCorpNo);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			corpAcnoPSeqno = getValue("acno_p_seqno");
			corpCardAcctIdx = getValueDouble("card_acct_idx");
			corpCorpPSeqno = getValue("corp_p_seqno");
			corpAcnoFlag = getValue("acno_flag");
			corpTotAmtMonth = getValueDouble("tot_amt_month");
			corpAdjEffStartDate = getValue("adj_eff_start_date");
			corpAdjEffEndDate = getValue("adj_eff_end_date");
			corpLineOfCreditAmt = getValueDouble("line_of_credit_amt");
			corpClassCode = getValue("class_code");
			if(commstring.between(sysDate, corpAdjEffStartDate, corpAdjEffEndDate) == false) {
				lbCorpAdj = true ;
			}
		}
		
		return 0;
	}
	
	int selectAuthTxLog() throws Exception {
		
		sqlCmd = "select sum(decode(cacu_amount,'Y',nt_amt,0)) as tot_amt from cca_auth_txlog where mtch_flag not in ('Y','U') ";
		sqlCmd += "and card_acct_idx = ? and card_acct_idx > 0 and cacu_flag <> 'Y' ";
		
		setDouble(1,cardAcctIdx);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			txlogAmt = getValueDouble("tot_amt");			
			return 0;
		}
		
		return 0;
	}
	
	int selectAuthTxLogCorp() throws Exception {
		
		if(corpCorpPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select sum(decode(cacu_amount,'Y',nt_amt,0)) as corp_tot_amt from cca_auth_txlog ";
		sqlCmd += "where mtch_flag not in ('Y','U') and cacu_amount = 'Y' and ";
		sqlCmd += "card_no in (select card_no from crd_card where corp_p_seqno = ? and acct_type = ? ) ";
		
		setString(1,corpCorpPSeqno);
		setString(2,corpAcctType);
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			corpTxLogAmt = getValueDouble("corp_tot_amt");
			return 0;
		}
		
		return 0;
		
	}
	
	int selectOverDraft() throws Exception {		
		if(acnoPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select sum(end_bal_spec) as tl_end_bal_spec from act_acct_sum where p_seqno = ? ";
		setString(1,acnoPSeqno);
		int recordCnt = selectTable();
		
		if(recordCnt > 0)
			acctSpecSum = getValueDouble("tl_end_bal_spec");
		
		sqlCmd = "select acct_jrnl_bal from act_acct where p_seqno = ? ";
		setString(1,acnoPSeqno);
		
		recordCnt = selectTable();
		if(recordCnt > 0)
			acctJrnlBal = getValueDouble("acct_jrnl_bal");
		
		overDraftNotSpec = acctJrnlBal - acctSpecSum; 
		
		return 0;
	}
	
	int selectBilContract() throws Exception {
		if(acnoPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select sum((install_tot_term - install_curr_term) * unit_price + remd_amt ";
		sqlCmd += "+decode(install_curr_term,0,first_remd_amt+extra_fees,0)) as inst_unpost from bil_contract where auth_code not in ";
		sqlCmd += "('','N','REJECT','P','reject','LOAN') and install_tot_term <> ";
		sqlCmd += "install_curr_term  and ((post_cycle_dd >0 or installment_kind ='F') or ";
		sqlCmd += "(post_cycle_dd=0 AND DELV_CONFIRM_FLAG='Y' AND auth_code='DEBT') ) and p_seqno = ? and spec_flag <> 'Y' ";
		
		setString(1,acnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0)
			instUnPost = getValueDouble("inst_unpost");
		
		return 0;
	}
	
	int selectBilContractCorp() throws Exception {
		if(corpCorpPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select sum((install_tot_term - install_curr_term) * unit_price + remd_amt ";
		sqlCmd += "+decode(install_curr_term,0,first_remd_amt+extra_fees,0)) as inst_unpost ";
		sqlCmd += "from bil_contract where install_tot_term <> install_curr_term and auth_code not in ";
		sqlCmd += "('','N','REJECT','P','reject') and (post_cycle_dd >0 or installment_kind ='F') and refund_flag<>'Y' ";
		sqlCmd += "and card_no in (select card_no from crd_card where corp_p_seqno=? and acct_type =?) ";
		
		setString(1,corpCorpPSeqno);
		setString(2,corpAcctType);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			corpInstUnpost = getValueDouble("inst_unpost");
			return 0;
		}
		
		return 0;
	}
	
	int selectOverPay() throws Exception {
		if(acnoPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select end_bal_op+end_bal_lk as pre_pay_amt from act_acct where p_seqno = ? ";
		setString(1,acnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			prePayAmt = getValueDouble("pre_pay_amt");
			return 0;
		}
		
		return 0;				
	}
	
	int selectAlreadyPay() throws Exception {
		if(acnoPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select sum(A.pay_amt) as pay_amt1 from act_pay_detail A join act_pay_batch B on A.batch_no=B.batch_no where B.batch_tot_cnt >0 ";
		sqlCmd += "and A.p_seqno = ?";
		setString(1,acnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			payAmt1 = getValueDouble("pay_amt1");			
		}
		
		sqlCmd = "select sum(pay_amt) as pay_amt2 from act_debt_cancel where process_flag <>'Y' and p_seqno = ?";
		setString(1,acnoPSeqno);
		
		recordCnt = selectTable();
		if(recordCnt > 0) {
			payAmt2 = getValueDouble("pay_amt2");			
		}
		
		sqlCmd = "select sum(txn_amt) as pay_amt3 from act_pay_ibm where p_seqno = ? and nvl(proc_mark,'') <>'Y' and nvl(error_code,'') in ";
		sqlCmd += "('','0','N') and txn_source not in ('0101', '0102', '0103', '0502')";
		setString(1,acnoPSeqno);
		if(recordCnt > 0) {
			payAmt3 = getValueDouble("pay_amt3");			
		}
		
		overPay = payAmt1 + payAmt2 + payAmt3 ;		
		
		return 0;
	}
	
	int selectSpecAmt() throws Exception {
		if(lbAdj == false) {
			specAmt = 0;
			return 0;
		}
		
		sqlCmd = "select risk_type , adj_month_amt , adj_eff_start_date , adj_eff_end_date from cca_adj_parm ";
		sqlCmd += "where card_acct_idx = ? and spec_flag ='Y' ";
		
		setDouble(1,cardAcctIdx);
		int recordCnt = selectTable();
		if(recordCnt ==0) {
			specAmt = 0;
			return 0;
		}
		
		String tempRisk = "" , tempStartDate = "" , tempEndDate = "";
		for(int ii=0 ; ii<recordCnt ; ii++) {
			tempRisk = getValue("risk_type",ii);
			tempStartDate = getValue("adj_eff_start_date",ii);
			tempEndDate = getValue("adj_eff_end_date",ii);
			
			sqlCmd = "select sum(nt_amt) as tl_spec_amt from cca_auth_txlog where risk_type = ? ";
			sqlCmd += "and card_acct_idx = ? and tx_date >= ? and tx_date <= ? and cacu_amount <> 'N' and cacu_flag ='Y'";
			
			setString(1,tempRisk);
			setDouble(2,cardAcctIdx);
			setString(3,tempStartDate);
			setString(4,tempEndDate);
			int recordCnt2 = selectTable();
			if(recordCnt2 > 0)
				specAmt += getValueDouble("tl_spec_amt");			
		}
						
		return 0;
	}
	
	int selectCcaCardBase() throws Exception {
		
		sqlCmd = "select A.acno_p_seqno, A.debit_flag, A.card_acct_idx , A.corp_p_seqno, A.acct_type, A.acno_flag, ";
		sqlCmd += " A.card_adj_limit, A.card_adj_date1, A.card_adj_date2 , B.indiv_crd_lmt, B.son_card_flag , B.corp_no ";
		sqlCmd += " from cca_card_base A left join crd_card B on B.card_no=A.card_no where A.card_no =? ";
		
		setString(1,cardNo);
		int recordCnt = selectTable();
		
		if(recordCnt > 0) {
			acnoPSeqno = getValue("acno_p_seqno");
			cardAcctIdx = getValueDouble("card_acct_idx");
			corpPSeqno = getValue("corp_p_seqno");
			corpNo = getValue("corp_no");
			acctType = getValue("acct_type");
			acnoFlag = getValue("acno_flag");
			cardAdjLimit = getValueDouble("card_adj_limit");
			cardAdjDate1 = getValue("card_adj_date1");
			cardAdjDate2 = getValue("card_adj_date2");
			indivCrdLmt = getValueDouble("indiv_crd_lmt");
			sonCardFlag = getValue("son_card_flag");			
			if("Y".equals(sonCardFlag)) {
				cardLimit = indivCrdLmt ;
				if(cardAdjLimit > 0 && commstring.between(sysDate, cardAdjDate1, cardAdjDate2)) 
					cardLimit = cardAdjLimit;
			}
			getSonCardConsume();			
		}		
		return 0;
	}
	
	int getSonCardConsume() throws Exception {
		if(cardLimit <=0) 
			return 0 ;
		
		sqlCmd = "select sum(decode(cacu_amount,'Y',nt_amt,'M',nt_amt,0)) as card_consume ";
		sqlCmd += "from cca_auth_txlog where card_no = ? and tx_date like to_char(sysdate,'yyyymm')||'%' ";
		setString(1,cardNo);
		int recordCnt = selectTable();
		
		if(recordCnt > 0) {
			cardTotConsume = getValueDouble("card_consume");
		}
		return 0;
	}
	
	int selectActDebtCorp() throws Exception {
		if(corpCorpPSeqno.isEmpty())
			return -1;
		
		if(corpAcctType.isEmpty())
			corpAcctType = "03";
		
		sqlCmd = "select sum(end_bal) as corp_paid_consume , sum(decode(acct_code,'CA',end_bal,0)) as corp_paid_precash ";
		sqlCmd += "from act_debt where acct_code in (select acct_code from ptr_actcode where interest_method='Y') ";
		sqlCmd += "and card_no in (select card_no from crd_card where corp_p_seqno=? and acct_type=?) ";
		
		setString(1,corpCorpPSeqno);
		setString(2,corpAcctType);	
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			corpPaidConsume = getValueDouble("corp_paid_consume");
			corpPaidPreCash = getValueDouble("corp_paid_precash");
			corpTotPaidConsume = corpPaidConsume - corpPaidPreCash ;
			if(corpTotPaidConsume < 0)
				corpTotPaidConsume = 0;
		}
		
		
		return 0;
	}
	
	int selectOverPayCorp() throws Exception {
		if(corpAcnoPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select end_bal_op+end_bal_lk as pre_pay_amt from act_acct where p_seqno = ? and p_seqno <> '' ";
		setString(1,corpAcnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			corpPrePayAmt = getValueDouble("pre_pay_amt");
			return 0;
		}
		return 0;
	}
	
	int selectAlreadyPayCorp() throws Exception {
		if(corpAcnoPSeqno.isEmpty())
			return -1;
		
		sqlCmd = "select sum(A.pay_amt) as corp_pay_amt1 from act_pay_detail A join act_pay_batch B on ";
		sqlCmd += "A.batch_no = B.batch_no where B.batch_tot_cnt > 0 and A.p_seqno = ? and A.p_seqno <> '' ";
		setString(1,corpAcnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {
			corpPayAmt1 = getValueDouble("corp_pay_amt1");
		}
		
		sqlCmd = "select sum(pay_amt) as corp_pay_amt2 from act_debt_cancel where process_flag <> 'Y' ";
		sqlCmd += "and p_seqno = ? and p_seqno <> '' ";
		
		setString(1,corpAcnoPSeqno);
		recordCnt = selectTable();
		
		if(recordCnt > 0) {
			corpPayAmt2 = getValueDouble("corp_pay_amt2");
		}
		
		sqlCmd = "select sum(txn_amt) as corp_pay_amt3 from act_pay_ibm where p_seqno = ? and nvl(proc_mark,'') <> 'Y' ";
		sqlCmd += " and nvl(error_code,'') in ('','0','N') and txn_source not in ('0101','0102','0103','0502') ";
		
		setString(1,corpAcnoPSeqno);
		recordCnt = selectTable();
		
		if(recordCnt > 0) {
			corpPayAmt3 = getValueDouble("corp_pay_amt3");			
		}
		
		corpAlreadyPay = corpPayAmt1 + corpPayAmt2 + corpPayAmt3 ;
		return 0;
	}
	
	String getIdPSeqno(String idNo) throws Exception {
		String dbIdPSeqno = "";
		sqlCmd = "select id_p_seqno from crd_idno_seqno where id_no = ? ";
		setString(1,idNo);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) {		
			dbIdPSeqno = getValue("id_p_seqno");
			return dbIdPSeqno;
		}
		return "";
	}
	
	String getCardNo(String aAcnoPSeqno) throws Exception {
		
		sqlCmd = "select card_no from crd_card where acno_p_seqno = ? fetch first 1 rows only ";
		setString(1,aAcnoPSeqno);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) 
			return getValue("card_no");
		
		return "";		
	}
	
	String getCorpPSeqno(String aCorpNo) throws Exception {
		
		sqlCmd = "select corp_p_seqno from crd_corp where corp_no = ? ";
		setString(1,aCorpNo);
		
		int recordCnt = selectTable();
		if(recordCnt > 0) 
			return getValue("corp_p_seqno");
		
		return "";				
	}
	
	void calAvailableBalance() {
		//--總額度
		if(lbAdj)
			acctAmt = totAmtMonth;
		else
			acctAmt = lineOfCreditAmt;
		
		acctAmt = acctAmt + overPay + prePayAmt;
		
		//--總消費
		tlConsumeAmt = txlogAmt + overDraftNotSpec + instUnPost ;
		
		//--可用餘額
		availableBalance = acctAmt - specAmt - tlConsumeAmt ;		
	}
	
	void calAvailableBalanceCorp() {
		//--總額度
		if(lbCorpAdj)
			corpAcctAmt = corpTotAmtMonth;
		else
			corpAcctAmt = corpLineOfCreditAmt;
		
		corpAcctAmt = corpAcctAmt + corpAlreadyPay + corpPrePayAmt ;
		
		//--總消費
		corpTlConsumeAmt = corpTxLogAmt + corpTotPaidConsume + corpInstUnpost;
		
		//--可用餘額
		corpAvailableBalance = corpAcctAmt - corpTlConsumeAmt;
		
	}
	
	void initData() {
		acctType ="";
		acctKey = "";
		acctAmt = 0.0;
		lineOfCreditAmt = 0.0;
		acnoPSeqno = "";
		comboCashLimit = 0.0;
		cardAcctIdx = 0.0;
		adjEffStartDate = "";
		adjEffEndDate = "";
		totAmtMonth = 0.0;
		txlogAmt = 0.0;
		acctSpecSum = 0.0;
		acctJrnlBal = 0.0;
		overDraftNotSpec = 0.0;
		prePayAmt = 0.0;
		overPay = 0.0;
		payAmt1 = 0.0;
		payAmt2 = 0.0;
		payAmt3 = 0.0;
		instUnPost = 0.0;
		lbAdj = false;
		specAmt = 0.0;
		tlConsumeAmt = 0.0;
		availableBalance = 0.0;
		cardNo = "";
		corpPSeqno = "";
		corpNo = "";
		acnoFlag = "";
		cardAdjLimit = 0.0;
		cardAdjDate1 = "";
		cardAdjDate2 = "";
		indivCrdLmt = 0.0;
		sonCardFlag = "";
		cardLimit = 0.0;
		cardTotConsume = 0.0;
		cardAvailableBalance = 0.0;
	}
	
	void initDataCorp() {
		corpAcctAmt = 0.0;
		corpAvailableBalance = 0.0;
		corpAcctType = "";
		corpCorpNo = "";
		corpAcnoPSeqno = "";
		corpCardAcctIdx = 0.0;
		corpCorpPSeqno = "";
		corpAcnoFlag = "";
		corpTotAmtMonth = 0.0;
		corpAdjEffStartDate = "";
		corpAdjEffEndDate = "";
		corpLineOfCreditAmt = 0.0;
		corpClassCode = "";
		lbCorpAdj = false;
		corpTxLogAmt = 0.0;
		corpPaidConsume = 0.0;
		corpPaidPreCash = 0.0;
		corpTotPaidConsume = 0.0;
		corpInstUnpost = 0.0;
		corpPrePayAmt = 0.0;
		corpPayAmt1 = 0.0;
		corpPayAmt2 = 0.0;
		corpPayAmt3 = 0.0;
		corpAlreadyPay = 0.0;
		corpTlConsumeAmt = 0.0;
	}
}
