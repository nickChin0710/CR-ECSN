package bank.authbatch.main;
import java.sql.ResultSet;


import bank.authbatch.main.BatchProgBase;
import bank.authbatch.dao.CcaAcctBalanceDao;
import bank.authbatch.dao.AuthTxLogDao;
import bank.authbatch.dao.CardAcctDao;
import bank.authbatch.dao.CardAcctIndexDao;
import bank.authbatch.dao.CcaSysParm1Dao;

import bank.authbatch.vo.CardAcctVo;

public class AuthBatch060Over30 extends BatchProgBase{

	String sG_DbMatchFlag="U";
	int nG_CaTotAmtConsume=0, nG_CaTotAmtPreCash=0;
	int nG_UBound=0;
	String sG_UDate="";//交易日期
	
	private void computeUDate(int nP_NDay) {
		/* 2. 依消帳日數求取交易日期*/
		sG_UDate = HpeUtil.getPriorNDayString("", nP_NDay);
	}
	private void getSysParam1() throws Exception{
		/* 1. 讀取參數-消帳日數 SYS_PARM1-REPORT-LOGIC_DAY-sys_data1*/
		ResultSet L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1("REPORT", "LOGIC_DAY");
		nG_UBound=30;
		
		while(L_SysParm1Rs.next()) {
			nG_UBound = L_SysParm1Rs.getInt("SysData1");
			if (nG_UBound<0)
				nG_UBound=0;
			
		}
		
		/*
		if (CcaSysParm1Dao.isEmptyResultSet(L_SysParm1Rs))
			nG_UBound=30;
		else {
			nG_UBound = L_SysParm1Rs.getInt("SysData1");
			if (nG_UBound<0)
				nG_UBound=0;
		}
		*/
		if (nG_UBound<30)
			nG_UBound=30;
		L_SysParm1Rs.close();
	}
	
	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub

		try {
			getSysParam1();
			computeUDate(0-nG_UBound);
			
			String sL_CacuAmount="Y";
			String sL_TxDate = sG_UDate;
			ResultSet L_AuthTxLogRs = getAuthTxLog(sL_TxDate, sL_CacuAmount);
			processAuthTxLog(L_AuthTxLogRs);
			
			commitDb();
			processAcctBalance();
			
			closeDb();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private boolean processAcctBalance() {
		/* 8. 求取昨天日期*/
		computeUDate(0-1);
		
		ResultSet L_AuthTxLogRs = null;
		CardAcctVo L_CardAcctVo=null;
		String sL_CacuCash="";
		int nL_CardAcctIdx=0, nL_RstAmt=0, nL_TxAmt=0;
		boolean bL_Result = true;
		try {
			ResultSet L_AcctBalanceRs =  CcaAcctBalanceDao.getAcctBalance(sG_UDate);
		
			while (L_AcctBalanceRs.next()) {
				nL_RstAmt=0;
				nL_TxAmt = 0;
				nL_CardAcctIdx =L_AcctBalanceRs.getInt("AcctBalanceCardAcctIdx");
				if (nL_CardAcctIdx>0) {
					/*讀取交易記錄檔 Auth_Txlog*/
					sL_CacuCash = "Y";
					
					//down, 讀取 AuthTxLog
					L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(nL_CardAcctIdx, sL_CacuCash);
					
					while (L_AuthTxLogRs.next()) {
						nL_RstAmt = L_AuthTxLogRs.getInt("SumNtAmt"); 
					}
					L_AuthTxLogRs.close();
					//up, 讀取 AuthTxLog
					
					
					//down, 讀取 CCA_CARD_ACCT
					L_CardAcctVo = CardAcctDao.getCardAcct(nL_CardAcctIdx);
					if (null != L_CardAcctVo) {
						nL_TxAmt = L_CardAcctVo.getTotAmtPreCash();	
					}
					L_CardAcctVo=null;
					//up, 讀取 CCA_CARD_ACCT
					
					if (nL_RstAmt != nL_TxAmt) {
						CardAcctDao.updateCardAcct(nL_CardAcctIdx, "ECS060Over30",  nL_RstAmt);
						commitDb();
					}
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	private boolean updateCardAcct(ResultSet P_AuthTxLogRs) {
		//proc is updCardAcct
		boolean bL_Result = true;
		
		try {
			int nL_CardAcctIdx = P_AuthTxLogRs.getInt("AuthTxLogCardAcctIdx");
			
			CardAcctVo L_CardAcctVo = CardAcctDao.getCardAcct(nL_CardAcctIdx);
			
			if (null == L_CardAcctVo)
				return false;
		
			int nL_AuthTxLogNtAmt = P_AuthTxLogRs.getInt("AuthTxLogNtAmt");
			nG_CaTotAmtConsume = L_CardAcctVo.getTotAmtConsume() - nL_AuthTxLogNtAmt;

			nG_CaTotAmtPreCash = L_CardAcctVo.getTotAmtPreCash();			
			if ("Y".equals(P_AuthTxLogRs.getString("AuthTxLogCacuCash"))) {
				nG_CaTotAmtPreCash = nG_CaTotAmtPreCash - nL_AuthTxLogNtAmt;
			}
			
			if (nG_CaTotAmtConsume<0)
				nG_CaTotAmtConsume=0;
			
			if (nG_CaTotAmtPreCash<0)
				nG_CaTotAmtPreCash=0;

			
			if (!CardAcctDao.updateCardAcct(nL_CardAcctIdx, "ECS060Over30", nG_CaTotAmtConsume, nG_CaTotAmtPreCash)) {
				return false;
			}
			
			
			int nL_AcctParentIndex = CardAcctIndexDao.getAcctParentIndex(nL_CardAcctIdx);
			
			if (nL_AcctParentIndex==0)
				return true;
			
			
			CardAcctVo L_ParentCardAcctVo = CardAcctDao.getCardAcct(nL_AcctParentIndex);
			
			if (L_ParentCardAcctVo==null) {
				return true;
			}
			
			nG_CaTotAmtConsume = L_ParentCardAcctVo.getTotAmtConsume();
			nG_CaTotAmtConsume = nG_CaTotAmtConsume - nL_AuthTxLogNtAmt;
			
			nG_CaTotAmtPreCash = L_ParentCardAcctVo.getTotAmtPreCash();
			if ("Y".equals(P_AuthTxLogRs.getString("AuthTxLogCacuCash"))) {
				nG_CaTotAmtPreCash = nG_CaTotAmtPreCash - nL_AuthTxLogNtAmt;
			}

			if (nG_CaTotAmtConsume<0)
				nG_CaTotAmtConsume=0;
			
			if (nG_CaTotAmtPreCash<0)
				nG_CaTotAmtPreCash=0;

			if (!CardAcctDao.updateCardAcct(nL_AcctParentIndex, "ECS060Over30", nG_CaTotAmtConsume, nG_CaTotAmtPreCash)) {
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	private boolean processAuthTxLog(ResultSet P_AuthTxLogRs) {
		boolean bL_Result = true;
		
		try {
			while (P_AuthTxLogRs.next()) {
				if (updateCardAcct(P_AuthTxLogRs)) {
					if (updateAuthTxLog(P_AuthTxLogRs)) {
						commitDb();
					}
					else {
						rollbackDb();
					}
				}
				else {
					rollbackDb();
				}
			}
			P_AuthTxLogRs.close();
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	private boolean updateAuthTxLog(ResultSet P_AuthTxLogRs) {
		//proc is updAuthTxLog
		boolean bL_Result = true;
		
		try {
			//RowId L_AuthTxLogRowId = P_AuthTxLogRs.getRowId("AuthTxLogRowId");
			String sL_TxDate = P_AuthTxLogRs.getString("AuthTxLogTxDate");
			String sL_CardNo= P_AuthTxLogRs.getString("AuthTxLogCardNo");
			String sL_AuthNo= P_AuthTxLogRs.getString("AuthTxLogAuthNo");
			String sL_TraceNo= P_AuthTxLogRs.getString("AuthTxLogTraceNo");
			String sL_TxTime= P_AuthTxLogRs.getString("AuthTxLogTxTime");

			String sL_CacuAmount="N", sL_CacuCash="N"; 
			String sL_MatchFlag= sG_DbMatchFlag;
			String sL_PgName="ECS060Over30"; 
			String sL_BilRefNo = "060Over30";
			//bL_Result = AuthTxLogDao.updateAuthTxLog(L_AuthTxLogRowId, sL_CacuAmount, sL_CacuCash, sL_MatchFlag, sL_PgName);
			bL_Result = AuthTxLogDao.updateAuthTxLog(sL_TxDate, sL_CardNo, sL_AuthNo,  sL_TraceNo,  sL_TxTime, sL_CacuAmount, sL_CacuCash, sL_MatchFlag, sL_PgName, sL_BilRefNo);
			

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
	private ResultSet getAuthTxLog(String sP_TxDate, String sP_CacuAmount) {
		return AuthTxLogDao.getAuthTxLog(sP_TxDate, sP_CacuAmount);
	}
	public AuthBatch060Over30() throws Exception{
		// TODO Auto-generated constructor stub
	}

}
