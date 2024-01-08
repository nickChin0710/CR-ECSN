package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;
import bank.authbatch.main.HpeUtil;



public class CcaAccountDao extends AuthBatchDbHandler{

	public CcaAccountDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static boolean updateCcaAccount(int nP_MesgCode, String sP_CcaAccountRowId){
		//proc is UPDATE_ECS_CARD_ACCTOUNT()
		
		boolean bL_Result = true;
		
		try {
			String sL_CurDate = HpeUtil.getCurDateStr("");
			String sL_Statcode="";
			if (0 ==nP_MesgCode)
				sL_Statcode="1";
			else if (9 ==nP_MesgCode)
				sL_Statcode="1";
			else
				sL_Statcode="2";
			
			String sL_Sql="update CCA_ACCOUNT set DOP= ? and PROCESS_STATUS= ? "
							+ " where ROWID= ? ";
			
			 //  table  	CCS_ACCOUNT/CCA_ACCOUNT 就是 ECS_CARD_ACCT
	           			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			
			ps.setString(1, sL_CurDate); 
			ps.setString(2, sL_Statcode);
			ps.setString(3, sP_CcaAccountRowId);
			
			ps.executeUpdate();
			
			ps.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean  ifHasData(String sP_CardAcctId, String sP_ProcessStatus) {
		boolean bL_Result = false;
		
		try {
			String sL_Sql = "select count(*) as  CcaAccountRecCount from CCA_ACCOUNT ";
			sL_Sql += "where PROCESS_STATUS= ? AND CARD_ACCT_ID= ? FETCH FIRST ? ROWS ONLY ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			
			ps.setString(1, sP_ProcessStatus);
			ps.setString(2, sP_CardAcctId);
			ps.setInt(3, 2);
	
			
			ResultSet L_ResultSet = ps.executeQuery();
			
			while (L_ResultSet.next()) {
				if(L_ResultSet.getInt("CcaAccountRecCount")>0)
					bL_Result = true;
			}
			
			releaseConnection(L_ResultSet);

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		
		return bL_Result;
	}
	public static ResultSet getCcaAccount() {
		ResultSet L_ResultSet = null;

		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "ROWID as CcaAccountRowId,TO_CHAR(DOG,'YYYYMMDDHH24MISS') as CcaAccountDog,RULE,PAYMENT_RULE, "
						+ "ACCOUNT_TYPE,CARD_ACCT_ID,CARD_HLDR_ID,CARD_ACCT_LEVEL, "
						+ "POSITION,CARD_ACCT_SINCE,NVL(STATUS_01,'  ') as Status01,NVL(STATUS_02,' ') as Status02, "
						+ "NVL(STATUS_03,' ') as Status03,NVL(STATUS_04,' ') as Status04,TRANSFER, NVL(STATUS_11,' ') as Status11, "
						+ "NVL(STATUS_12,' ') as Status12,NVL(STATUS_13,' ') as Status13,NVL(STATUS_14,' ') as Status14,STATUS_REASON, "
						+ "AUTO_PAY_BANKID,NVL(MAX_CONSUME_AMT,0) as MaxConsumeAmt,MAX_CONSUME_DATE as MaxConsumeDate, "
						+ "NVL(MAX_PRECASH_AMT,0) as MaxPreCashAmt,MAX_PRECASH_DATE as MaxPreCashDate, NVL(CLOSE_PUNISH_FEE,0) as ClosePunishFee, "
						+ "NVL(CLOSE_INTEREST_FEE,0) as CloseInterestFee,NVL(CLOSE_SRV_FEE,0) as CloseSrvFee,NVL(CLOSE_LAW_FEE,0) as CloseLawFee, "
						+ "NVL(CLOSE_CONSUME_FEE,0) as CloseConsumeFee,NVL(CLOSE_PRECASH,0) as ClosePreCash,NVL(CLOSE_WRITSOFF,0) as CloseWritsOff, "
						+ "NVL(OPEN_PUNISH_FEE,0) as OpenPunishFee,NVL(OPEN_INTEREST_FEE,0) as OpenInterestFee, NVL(OPEN_SRV_FEE,0) as OpenSrvFee, "
						+ "NVL(OPEN_LAW_FEE,0) as OpenLawFee,NVL(OPEN_CONSUME_FEE,0) as OpenConsumeFee,NVL(OPEN_PRECASH,0) as OpenPreCash, "
						+ "NVL(OPEN_WRITSOFF,0) as OpenWritsOff,NVL(BILL_LAW_PAY_AMT,0) as BillLawPayAmt,MCODE, "
						+ "NVL(ARGUE_AMT,0) as ArgueAmt,NVL(PRE_PAY_AMT,0) as PrePayAmt, "
						+ "LASTEST_1_MNTH as L1Mnth,LASTEST_2_MNTH as L2Mnth,LASTEST_3_MNTH as L3Mnth,LASTEST_4_MNTH as L4Mnth, "
						+ "LASTEST_5_MNTH as L5Mnth,LASTEST_6_MNTH as L6Mnth,LASTEST_7_MNTH as L7Mnth,LASTEST_8_MNTH as L8Mnth, "
						+ "LASTEST_9_MNTH as L9Mnth,LASTEST_10_MNTH as L10Mnth,LASTEST_11_MNTH as L11Mnth,LASTEST_12_MNTH as L12Mnth, "
						+ "NVL(PAY_LASTEST_AMT,0) as PayLastestAmt,PAY_DATE as PayDate,NVL(LMT_TOT_CONSUME,0) as CcaAccountLmtTotConsume, "
						+ "NVL(BILL_LOW_LIMIT,0) as BillLowLimit,PAY_SETTLE_DATE as PaySettleDate,payment_due_date as PaymentDueDate, "
						+ "NVL(TOTAL_UNPAID_AMT,0) as TotalUnpaidAmt,NVL(TOT_LIMIT_AMT,0) as TotLimitAmt,NVL(TOT_PRECASH_AMT,0) as TotPreCashAmt, "
						+ "NVL(CONSUME_1,0) as Consume1,NVL(CONSUME_2,0) as Consume2,NVL(CONSUME_3,0) as Consume3, "
						+ "NVL(CONSUME_4,0) as Consume4,NVL(CONSUME_5,0) as Consume5,NVL(CONSUME_6,0) as Consume6, "
						+ "NVL(TOT_DUE,0) as TotDue,AUTH_REMARK,CORP_NAME,BILL_ADDRESS, "
						+ "ACCT_ADDRESS,OVER_DUE,BRANCH,ORGAN_ID,LMT_TOT_CONSUME_CASH as CcaAccountLmlTotConsumeCash, "
						+ "ACCT_NO,NVL(UNPOST_INST_FEE,0) as UnpostInstFee,SALE_DATE, "
						+ "BILL_SENDING_ZIP,AUTO_INSTALLMENT,PD_RATING,NEW_VDCHG_FLAG, ACNO_FLAG from CCA_ACCOUNT "; 
		sL_Sql += "where PROCESS_STATUS= ? AND CARD_ACCT_ID<>'0000000000' ORDER BY CARD_ACCT_ID,CARD_HLDR_ID,ACCOUNT_TYPE,DOG";
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			
			ps.setString(1, "0");
	
			
			L_ResultSet = ps.executeQuery();
			
			
		}
		catch (Exception e) {

			//System.out.println("Exception on getCcaAccount => " + e.getMessage());
		}
		L_Timer.end();
		//System.out.println("Timer : " + L_Timer.duration());
		return L_ResultSet;
	}

	public static boolean ifCcaAccountHasRecord(String sP_CardAcctId, String sP_ProcessStatus) {
		boolean bL_Result = false;
		ResultSet L_ResultSet = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "select ROWID from CCA_ACCOUNT "
				+ " where CARD_ACCT_ID= ?  and PROCESS_STATUS= ? FETCH FIRST ? ROWS ONLY "; 

		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setString(1, sP_CardAcctId);
			ps.setString(2, sP_ProcessStatus);
			ps.setInt(3, 2);

			/*
			ps.setString("p1", "0000000000");
			ps.setString("p2", "1");
			*/
			
			L_ResultSet = ps.executeQuery();
			while (L_ResultSet.next()) {
				bL_Result=true;
				break;

			}
			releaseConnection(L_ResultSet);
			/*
			L_ResultSet.close();
			ps.close();
			Db2Connection.commit();
			*/
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result=false;
		}
		
		return bL_Result;
	}
}
