package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;

import bank.authbatch.vo.CardAcctIndexVo;
import bank.authbatch.vo.CardAcctVo;
import bank.authbatch.vo.CcaCardBaseVo;
import bank.authbatch.vo.Data100Vo;

public class CreditLogDao extends AuthBatchDbHandler{

	public static String sG_LmtFlag="";
	public CreditLogDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	//public static boolean insertCreditLog(CardAcctVo P_CardAcctVo, int nP_OnBatTransAmt, CcaCardBaseVo P_CardBaseVo, String sP_OnBatCardAcctId) throws Exception{
	public static boolean insertCreditLog(String sP_AccountType, String sP_CardHolderId, String sP_CardAcctId, String sP_CardAcctDog, CardAcctVo P_CardAcctVo, ResultSet P_CcsAccountRs, CardAcctIndexVo P_CardAcctIndexVo, String sP_CallerProgName, int nP_ActAcnoLineOfCreditAmt) throws Exception{
		//proc is insert_credit_log
		//CCA_CREDIT_LOG : 帳戶臨調額度記錄檔
		boolean bL_Result = true;
		String sL_CurDate = HpeUtil.getCurDateStr("");
		String sL_CurTime = HpeUtil.getCurDateTimeStr(false);
		String sL_CurDateTime = HpeUtil.getCurDateTimeStr(false);
		
		/* marked on 2019/6/6
		ResultSet L_OnBatResultSet = OnBatDao.getOnBat("12", sP_AccountType, sP_CardHolderId, sP_CardAcctId);
		if (null == L_OnBatResultSet) {
			return false;
		}
		if (Integer.parseInt(sP_CardAcctDog) > Integer.parseInt(L_OnBatResultSet.getString("OnBatDog"))) {
			sG_LmtFlag="Y";
		}
		else {
			sG_LmtFlag="N";
			return true;
		}
		*/
		
		if ("N".equals(P_CardAcctVo.getAdjQuota()))
			return true; ///*** 無臨調不需寫入 ***/
		else {
			if ((Integer.parseInt(sL_CurDate) < Integer.parseInt(P_CardAcctVo.getAdjEffStartDate())) ||
					(Integer.parseInt(sL_CurDate) > Integer.parseInt(P_CardAcctVo.getAdjEffStartDate())) )
				return true;
			
			if (((Integer.parseInt(P_CardAcctVo.getTotAmtMonth())==0) ||
					(Integer.parseInt(P_CardAcctVo.getTotAmtMonth())==100) ) &&
					((Integer.parseInt(P_CardAcctVo.getAdjInstPct())==0) ||
					(Integer.parseInt(P_CardAcctVo.getAdjInstPct())==100) ))
				return true;
		}
		
		
		/* Howard : 2018.07.12 LmtTotConsume 搬到ACT_ACNO中， 所以要改寫取值的寫法
		if ((Integer.parseInt(P_CardAcctVo.getLmtTotConsume()) == Integer.parseInt(P_CcsAccountRs.getString("CcsAccountLmtTotConsume")) ) &&
				(Integer.parseInt(P_CardAcctVo.getLmtTotCash()) == Integer.parseInt(P_CcsAccountRs.getString("CcsAccountLmlTotConsumeCash")) ) )
				return true;
		*/
				
		
		
		
		try {

			String sL_Sql="";
			sL_Sql = "insert into CCA_CREDIT_LOG(TX_DATE,  TX_TIME,  CARD_ACCT_IDX, ADJ_QUOTA, ADJ_EFF_START_DATE, ADJ_EFF_END_DATE, ADJ_REASON,  ORG_AMT_MONTH, TOT_AMT_MONTH, ADJ_USER, MOD_TIME, MOD_PGM) ";
			//sL_Sql += "values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12)";
			sL_Sql += "values(?,?,?,?,?,?,?,?,?,?,?,?)";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sL_CurDate); //TX_DATE
			ps.setString(2, sL_CurTime); //TX_TIME
			ps.setInt(3, P_CardAcctVo.getCardAcctIdx() ); //CARD_ACCT_IDX
			ps.setString(4, P_CardAcctVo.getAdjQuota()); //ADJ_QUOTA
			ps.setString(5, P_CardAcctVo.getAdjEffStartDate()); //ADJ_EFF_START_DATE
			ps.setString(6, P_CardAcctVo.getAdjEffEndDate()); //ADJ_EFF_END_DATE
			ps.setString(7, P_CardAcctVo.getAdjReason()); //ADJ_REASON
			ps.setString(8, P_CardAcctVo.getTotAmtMonth() ); //ORG_AMT_MONTH
			ps.setString(9, ""); //TOT_AMT_MONTH
			
			/*
			Howard: p9 == decode(:h_temp_tot_amt_month,0,:h_temp_tot_amt_month,100,:h_temp_tot_amt_month,
            decode(sign(ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)-100),
                    -1,100,ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)))

			*/

			ps.setString(10, sP_CallerProgName); //ADJ_USER
			ps.setString(11, sL_CurDateTime); //MOD_TIME
			ps.setString(12, sP_CallerProgName); //MOD_PGM

			
			
            //CARD_ACCT_ID,
            //CARD_ACCT_ID_SEQ,
            //CARD_CORP_ID,
            //CARD_CORP_ID_SEQ,
            //CARD_ACCT_CLASS,
            //ECS_ACCT_CLASS,
            //ORG_TOT_CONSUME,
            //LMT_TOT_CONSUME,
            //ORG_TOT_CASH,
            //LMT_TOT_CASH,
			ps.executeUpdate();
			ps.close();
			bL_Result=true;

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			
			return bL_Result;
		}
		
		int nL_LmtTotConsume = P_CcsAccountRs.getInt("CcsAccountLmtTotConsume");
		if (!CardAcctDao.updateCardAcct12(P_CardAcctVo, nL_LmtTotConsume,nP_ActAcnoLineOfCreditAmt))
			return false;
		//insert_db_trans_log_12() => Howard:新系統先不做
		
 
 
		if (!LimitAdjLogDao.insertLimitAdjLog(P_CardAcctVo, sP_CallerProgName, sP_CardAcctId, nL_LmtTotConsume))
			return false;
		
		
		return bL_Result;
		
	}

	
	//public static boolean insertCreditLog(CardAcctVo P_CardAcctVo, int nP_OnBatTransAmt, CcaCardBaseVo P_CardBaseVo, String sP_OnBatCardAcctId) throws Exception{
	public static boolean insertCreditLog(String sP_AccountType, String sP_CardHolderId, String sP_CardAcctId, String sP_CardAcctDog, CardAcctVo P_CardAcctVo, Data100Vo P_Data100Vo, CardAcctIndexVo P_CardAcctIndexVo, String sP_CallerProgName, int nP_ActAcnoLineOfCreditAmt) throws Exception{
		//proc is insert_credit_log
		//CCA_CREDIT_LOG : 帳戶臨調額度記錄檔
		boolean bL_Result = true;
		String sL_CurDate = HpeUtil.getCurDateStr("");
		String sL_CurTime = HpeUtil.getCurDateTimeStr(false);
		String sL_CurDateTime = HpeUtil.getCurDateTimeStr(false);
		ResultSet L_OnBatResultSet = OnBatDao.getOnBat("12", sP_AccountType, sP_CardHolderId, sP_CardAcctId);
		if (null == L_OnBatResultSet) {
			return false;
		}
		if (Integer.parseInt(sP_CardAcctDog) > Integer.parseInt(L_OnBatResultSet.getString("OnBatDog"))) {
			sG_LmtFlag="Y";
		}
		else {
			sG_LmtFlag="N";
			return true;
		}

		
		if ("N".equals(P_CardAcctVo.getAdjQuota()))
			return true; ///*** 無臨調不需寫入 ***/
		else {
			if ((Integer.parseInt(sL_CurDate) < Integer.parseInt(P_CardAcctVo.getAdjEffStartDate())) ||
			(Integer.parseInt(sL_CurDate) > Integer.parseInt(P_CardAcctVo.getAdjEffStartDate())) )
				return true;
			
			if (((Integer.parseInt(P_CardAcctVo.getTotAmtMonth())==0) ||
					(Integer.parseInt(P_CardAcctVo.getTotAmtMonth())==100) ) &&
					((Integer.parseInt(P_CardAcctVo.getAdjInstPct())==0) ||
					(Integer.parseInt(P_CardAcctVo.getAdjInstPct())==100) ))
				return true;
		}
		
		
		/* Howard : 2018.07.12 LmtTotConsume 搬到ACT_ACNO中， 所以要改寫取值的寫法
		if ((Integer.parseInt(P_CardAcctVo.getLmtTotConsume()) == Integer.parseInt(P_CcsAccountRs.getString("CcsAccountLmtTotConsume")) ) &&
				(Integer.parseInt(P_CardAcctVo.getLmtTotCash()) == Integer.parseInt(P_CcsAccountRs.getString("CcsAccountLmlTotConsumeCash")) ) )
				return true;
		*/
				
		
		
		
		try {
			/*
			String sL_Sql="";
			sL_Sql = "insert into CCA_CREDIT_LOG(TX_DATE,  TX_TIME,  CARD_ACCT_IDX, ADJ_QUOTA, ADJ_EFF_START_DATE, ADJ_EFF_END_DATE, ADJ_REASON,  ORG_AMT_MONTH, TOT_AMT_MONTH, ADJ_USER, MOD_TIME, MOD_PGM) ";
			//sL_Sql += "values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12)";
			sL_Sql += "values(?,?,?,?,?,?,?,?,?,?,?,?)";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sL_CurDate); //TX_DATE
			ps.setString(2, sL_CurTime); //TX_TIME
			ps.setInt(3, P_CardAcctVo.getCardAcctIdx() ); //CARD_ACCT_IDX
			ps.setString(4, P_CardAcctVo.getAdjQuota()); //ADJ_QUOTA
			ps.setString(5, P_CardAcctVo.getAdjEffStartDate()); //ADJ_EFF_START_DATE
			ps.setString(6, P_CardAcctVo.getAdjEffEndDate()); //ADJ_EFF_END_DATE
			ps.setString(7, P_CardAcctVo.getAdjReason()); //ADJ_REASON
			ps.setString(8, P_CardAcctVo.getTotAmtMonth() ); //ORG_AMT_MONTH
			ps.setString(9, ""); //TOT_AMT_MONTH
			
			
			//Howard: p9 == decode(:h_temp_tot_amt_month,0,:h_temp_tot_amt_month,100,:h_temp_tot_amt_month,
            //decode(sign(ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)-100),
                    -1,100,ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)))

			

			ps.setString(10, sP_CallerProgName); //ADJ_USER
			ps.setString(11, sL_CurDateTime); //MOD_TIME
			ps.setString(12, sP_CallerProgName); //MOD_PGM

			
			
            //CARD_ACCT_ID,
            //CARD_ACCT_ID_SEQ,
            //CARD_CORP_ID,
            //CARD_CORP_ID_SEQ,
            //CARD_ACCT_CLASS,
            //ECS_ACCT_CLASS,
            //ORG_TOT_CONSUME,
            //LMT_TOT_CONSUME,
            //ORG_TOT_CASH,
            //LMT_TOT_CASH,
			ps.executeUpdate();
			ps.close();
			
			*/
			
			G_Ps4CcaCreditLog.setString(1, sL_CurDate); //TX_DATE
			G_Ps4CcaCreditLog.setString(2, sL_CurTime); //TX_TIME
			G_Ps4CcaCreditLog.setInt(3, P_CardAcctVo.getCardAcctIdx() ); //CARD_ACCT_IDX
			G_Ps4CcaCreditLog.setString(4, P_CardAcctVo.getAdjQuota()); //ADJ_QUOTA
			G_Ps4CcaCreditLog.setString(5, P_CardAcctVo.getAdjEffStartDate()); //ADJ_EFF_START_DATE
			G_Ps4CcaCreditLog.setString(6, P_CardAcctVo.getAdjEffEndDate()); //ADJ_EFF_END_DATE
			G_Ps4CcaCreditLog.setString(7, P_CardAcctVo.getAdjReason()); //ADJ_REASON
			G_Ps4CcaCreditLog.setString(8, P_CardAcctVo.getTotAmtMonth() ); //ORG_AMT_MONTH
			G_Ps4CcaCreditLog.setString(9, ""); //TOT_AMT_MONTH
			
			
			//Howard: p9 == decode(:h_temp_tot_amt_month,0,:h_temp_tot_amt_month,100,:h_temp_tot_amt_month,
            //decode(sign(ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)-100),
            //        -1,100,ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)))

			

			G_Ps4CcaCreditLog.setString(10, sP_CallerProgName); //ADJ_USER
			G_Ps4CcaCreditLog.setString(11, sL_CurDateTime); //MOD_TIME
			G_Ps4CcaCreditLog.setString(12, sP_CallerProgName); //MOD_PGM

			
			
            //CARD_ACCT_ID,
            //CARD_ACCT_ID_SEQ,
            //CARD_CORP_ID,
            //CARD_CORP_ID_SEQ,
            //CARD_ACCT_CLASS,
            //ECS_ACCT_CLASS,
            //ORG_TOT_CONSUME,
            //LMT_TOT_CONSUME,
            //ORG_TOT_CASH,
            //LMT_TOT_CASH,
			G_Ps4CcaCreditLog.executeUpdate();

			bL_Result=true;

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			
			return bL_Result;
		}
		
		
		int nL_Data100VoLineOfCreditAmt = P_Data100Vo.getLineOfCreditAmt() ;// P_CcsAccountRs.getInt("CcsAccountLmtTotConsume");
		if (!CardAcctDao.updateCardAcct12(P_CardAcctVo, nL_Data100VoLineOfCreditAmt, nP_ActAcnoLineOfCreditAmt))
			return false;
		//insert_db_trans_log_12() => Howard:新系統先不做
		
 
		if (!LimitAdjLogDao.insertLimitAdjLog(P_CardAcctVo, sP_CallerProgName, sP_CardAcctId, nL_Data100VoLineOfCreditAmt))
			return false;
		
		
		return bL_Result;
		
	}

	
	public static boolean insertCreditLog(CardAcctVo P_CardAcctVo, int nP_OnBatTransAmt, CcaCardBaseVo P_CardBaseVo, String sP_OnBatCardAcctId, int nP_ActAcnoLineOfCreditAmt) throws Exception{
		//proc is insert_credit_log
		//CCA_CREDIT_LOG : 帳戶臨調額度記錄檔
		boolean bL_Result = true;
		String sL_CurDate = HpeUtil.getCurDateStr("");
		String sL_CurTime = HpeUtil.getCurDateTimeStr(false);
		String sL_CurDateTime = HpeUtil.getCurDateTimeStr(false);
		try {

			String sL_Sql="";
			sL_Sql = "insert into CCA_CREDIT_LOG(TX_DATE,  TX_TIME,  CARD_ACCT_IDX, ADJ_QUOTA, ADJ_EFF_START_DATE, ADJ_EFF_END_DATE, ADJ_REASON,  ORG_AMT_MONTH, TOT_AMT_MONTH, ADJ_USER, MOD_TIME, MOD_PGM) ";
			//sL_Sql += "values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12)";
			sL_Sql += "values(?,?,?,?,?,?,?,?,?,?,?,?)";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sL_CurDate); //TX_DATE
			ps.setString(2, sL_CurTime); //TX_TIME
			ps.setInt(3, P_CardAcctVo.getCardAcctIdx() ); //CARD_ACCT_IDX
			ps.setString(4, P_CardAcctVo.getAdjQuota()); //ADJ_QUOTA
			ps.setString(5, P_CardAcctVo.AdjEffStartDate); //ADJ_EFF_START_DATE
			ps.setString(6, P_CardAcctVo.getAdjEffEndDate()); //ADJ_EFF_END_DATE
			ps.setString(7, P_CardAcctVo.getAdjReason()); //ADJ_REASON
			ps.setString(8, P_CardAcctVo.getTotAmtMonth() ); //ORG_AMT_MONTH
			ps.setString(9, ""); //TOT_AMT_MONTH
			/*
			Howard: p9 == decode(:h_temp_tot_amt_month,0,:h_temp_tot_amt_month,100,:h_temp_tot_amt_month,
            decode(sign(ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)-100),
                    -1,100,ceil((:h_temp_lmt_tot_consume*:h_temp_tot_amt_month)/:h_a_lmt_tot_consume)))

			*/

			ps.setString(10, "ECS100"); //ADJ_USER
			ps.setString(11, sL_CurDateTime); //MOD_TIME
			ps.setString(12, "ECS100"); //MOD_PGM

			
			
            //CARD_ACCT_ID,
            //CARD_ACCT_ID_SEQ,
            //CARD_CORP_ID,
            //CARD_CORP_ID_SEQ,
            //CARD_ACCT_CLASS,
            //ECS_ACCT_CLASS,
            //ORG_TOT_CONSUME,
            //LMT_TOT_CONSUME,
            //ORG_TOT_CASH,
            //LMT_TOT_CASH,
			ps.executeUpdate();
			ps.close();
			bL_Result=true;

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			
			return bL_Result;
		}
		
		
		
		if (!CardAcctDao.updateCardAcct12(P_CardAcctVo, nP_OnBatTransAmt, nP_ActAcnoLineOfCreditAmt))
			return false;
		//insert_db_trans_log_12() => Howard:新系統先不做
		
		

		if (!LimitAdjLogDao.insertLimitAdjLog12(P_CardAcctVo, nP_OnBatTransAmt, sP_OnBatCardAcctId))
			return false;
		
		
		return bL_Result;
		
	}

}
