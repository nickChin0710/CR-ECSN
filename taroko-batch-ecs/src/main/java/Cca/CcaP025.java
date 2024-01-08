/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112-03-22   V1.00.01  Alex        initial                                  *
* 112-05-18   V1.00.02  Alex        開頭 delete cca_card_balance_cal           *
 * 2023-1113  V1.00.03  JH    cacu_amount<>N
*****************************************************************************/
package Cca;

import com.BaseBatch;
import com.CommCrd;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CcaP025 extends BaseBatch {
	private final String progname = "子卡可用餘額計算 2023-1113  V1.00.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commString = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	
	boolean lbTruncate = false;
	String cardNo = "";
	String acnoPSeqno = "";
	String idPSeqno = "";
	String corpPSeqno = "";
	String acctType = "";
	double cardAcctIdx = 0;
	double indivCrdLmt = 0;
	double cardAdjLimit = 0;
	String cardAdjDate1 = "";
	String cardAdjDate2 = "";
	double acctAmtBalance = 0;
	double acctCashBalance = 0;
	
	double authTxLogAmt = 0;	
	double authTxLogCash = 0;
	double cardCashAmt = 0;
	double cardAmtBalance = 0;
	
	public static void main(String[] args) {
		CcaP025 proc = new CcaP025();
		// proc.debug = true;
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 2) {
			printf("Usage : Ccap025 [business_date,truncate]");
			errExit(1);
		}

		dbConnect();
		
		if (liArg == 1) {
			this.setBusiDate(args[0]);
		}	else if(liArg == 2) {
			this.setBusiDate(args[0]);
			if("Y".equals(args[1]))
				lbTruncate = true;
			else
				lbTruncate = false;
		}
		
		if(hBusiDate.isEmpty())
			hBusiDate = comc.getBusiDate();
		
		showLogMessage("I", "", "=====清除 cca_card_balance_cal開始=====");
		if(lbTruncate)
			truncateCcaAcctBalanceCal();
		else
			deleteCcaCardBalanceCal();
		
		showLogMessage("I", "", "=====子卡可用餘額開始=====");
		processSonCardData();
		sqlCommit();
		endProgram();

	}
	
	void truncateCcaAcctBalanceCal() throws Exception {				
		commitDataBase();
		String sql1 = "truncate cca_card_balance_cal immediate";
		executeSqlCommand(sql1);
		commitDataBase();
		showLogMessage("I","","truncate cca_card_balance_cal complete");
	}
	
	void deleteCcaCardBalanceCal() throws Exception {	
		int deleteCnt = 0;
		String sql1 = "select count(*) as db_cnt from cca_card_balance_cal where 1=1 ";		
		while(true) {
			sqlSelect(sql1);
			if(colNum("db_cnt") > 0) {
				daoTable = "cca_card_balance_cal";
			    whereStr = "where 1=1 fetch first 5000 rows only ";	    
			    deleteTable();
			    sqlCommit(1);
			    deleteCnt+=5000;
			    if(deleteCnt%100000 == 0)
			    	showLogMessage("I", "", "已清除 ["+deleteCnt+"] 筆");
			    continue;
			}	else	
				break;
		}
		showLogMessage("I", "", "清除 cca_card_balance_cal 結束");
	}
	
	void processSonCardData() throws Exception {
		
		sqlCmd = "select A.card_no , A.acct_type , A.acno_p_seqno , A.corp_p_seqno , A.id_p_seqno , "
			   + "A.indiv_crd_lmt , B.card_acct_idx , B.card_adj_limit , B.card_adj_date1 , B.card_adj_date2 , C.acct_amt_balance , C.acct_cash_balance "
			   + "from crd_card A join cca_card_base B on A.card_no = B.card_no left join cca_acct_balance_cal C on A.acno_p_seqno = C.acno_p_seqno "	
			   + "where A.son_card_flag = 'Y' "
			   ;
		
		openCursor();
		
		while(fetchTable()) {
			initData();
			cardNo = colSs("card_no");
			acnoPSeqno = colSs("acno_p_seqno");
			idPSeqno = colSs("id_p_seqno");
			corpPSeqno = colSs("corp_p_seqno");
			acctType = colSs("acct_type");
			indivCrdLmt = colNum("indiv_crd_lmt");
			cardAcctIdx = colNum("card_acct_idx");
			cardAdjLimit = colNum("card_adj_limit");
			cardAdjDate1 = colSs("card_adj_date1");
			cardAdjDate2 = colSs("card_adj_date2");
			acctAmtBalance = colNum("acct_amt_balance");
			acctCashBalance = colNum("acct_cash_balance");
			//--取子卡消費額度
			selectTxLogAmt();
			//--取卡片預借現金額度
			selectCash();
			//--子卡可用餘額計算
			if(cardAdjDate1.isEmpty() == false && cardAdjDate2.isEmpty() == false) {
				if(commString.between(hBusiDate, cardAdjDate1, cardAdjDate2)) {
					cardAmtBalance = cardAdjLimit;
				} else	{
					cardAmtBalance = indivCrdLmt;
				}
			}	else
				cardAmtBalance = indivCrdLmt;
			
			cardAmtBalance = (cardAmtBalance - authTxLogAmt);
			
			if(cardCashAmt > cardAmtBalance)
				cardCashAmt = cardAmtBalance;
			
			insertCcaCardBalanceCal();
			totalCnt ++;
			
			if(totalCnt % 5000 == 0) {
				sqlCommit();
				showLogMessage("I", "", "card cal proc cnt =["+totalCnt+"]");
			}
			
		}		
		sqlCommit();
		showLogMessage("I", "", "===== card cal proc complete cnt =["+totalCnt+"] =====");
		closeCursor();
		
	}
	
	void insertCcaCardBalanceCal() throws Exception {
		daoTable = "cca_card_balance_cal";
		setValue("cal_date",hBusiDate);
		setValue("card_no",cardNo);
		setValue("acno_p_seqno",acnoPSeqno);
		setValue("id_p_seqno",idPSeqno);
		setValue("corp_p_seqno",corpPSeqno);
		setValue("acct_type",acctType);		
		setValueDouble("card_amt_balance",cardAmtBalance);
		setValueDouble("card_cash_balance",cardCashAmt);		
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("mod_user","ecs");
		setValue("mod_pgm","CcaP020");
		setValue("mod_time",sysDate+sysTime);
		setValueInt("mod_seqno",1);
		insertTable();		
	}
	
	void selectCash() throws Exception {
		
		double tmpCash = 0 , tmpIdx = 0 , tmpRate = 0 , tmpAmt =0 ;
		String tmpClassCode = "" , tmpDate1 = "" , tmpDate2 = "";
		
		String sql1 = "select A.line_of_credit_amt_cash , A.class_code , B.tot_amt_month , B.card_acct_idx , B.adj_eff_start_date , B.adj_eff_end_date "
					+ "from act_acno A join cca_card_acct B on A.acno_p_seqno = B.acno_p_seqno "
					+ "where A.acno_p_seqno = ? ";
		
		sqlSelect(sql1,new Object[] {acnoPSeqno});
		
		if(sqlNrow <=0)
			return ;
		
		tmpCash = colNum("line_of_credit_amt_cash");
		tmpClassCode = colSs("class_code");
		tmpIdx = colNum("card_acct_idx");
		tmpAmt = colNum("tot_amt_month");
		tmpDate1 = colSs("adj_eff_start_date");
		tmpDate2 = colSs("adj_eff_end_date");
		
		//--計算卡片可用預借現金
		String sql2 = "select lmt_amt_month_pct from cca_risk_consume_parm where area_type ='T' "
					+ "and card_note in (select card_note from crd_card where card_no =? "
					+ "union select '*' from dual ) and risk_level =? and risk_type ='C' "
					+ "order by decode(card_note,'*','zz',card_note) "
					+ "fetch first 1 rows only "
					;
		
		sqlSelect(sql2,new Object[] {cardNo,tmpClassCode});
		
		if(sqlNrow > 0)
			tmpRate = colNum("lmt_amt_month_pct");
		
		if(tmpRate > 0) {
			tmpCash = commString.numScale(tmpCash * (tmpRate / 100),0);
		}
		
		if(tmpDate1.isEmpty() == false && tmpDate2.isEmpty() == false) {
			if(commString.between(hBusiDate, tmpDate1, tmpDate2)) {
				String sql3 = "select adj_month_amt from cca_adj_parm where card_acct_idx = ? ";
				sqlSelect(sql3,new Object[] {tmpIdx});
				if(sqlNrow > 0)
					tmpCash = colNum("adj_month_amt");
				if(tmpCash > tmpAmt)
					tmpCash = tmpAmt ;
			}
		}
		
		
		
		tmpCash = tmpCash - authTxLogCash;
		if(tmpCash > acctAmtBalance)
			tmpCash = acctAmtBalance ;
		
		cardCashAmt = tmpCash;
	}
	
	void selectTxLogAmt() throws Exception {
		
		String sql1 = "select sum(decode(cacu_amount,'N',0,nt_amt)) as card_consume "
					+ "from cca_auth_txlog where card_no =? "
					+ "and tx_date like to_char(sysdate,'yyyymm')||'%' "
					;
		
		sqlSelect(sql1,new Object[] {cardNo});
		
		authTxLogAmt = colNum("card_consume");		
		
		String sql2 = "select sum(decode(cacu_cash,'Y',nt_amt,0)) as tot_precash from cca_auth_txlog "
					+ "where mtch_flag not in ('Y','U') and card_acct_idx =? and card_acct_idx>0 and cacu_flag <> 'Y' ";
		
		sqlSelect(sql2,new Object[] {cardAcctIdx});
		authTxLogCash = colNum("tot_precash");
	}
	
	void initData() {
		cardNo = "";
		acnoPSeqno = "";
		idPSeqno = "";
		corpPSeqno = "";
		acctType = "";
		indivCrdLmt = 0;
		cardAdjLimit = 0;
		cardAdjDate1 = "";
		cardAdjDate2 = "";
		acctAmtBalance = 0;
	}
	
}
