package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;

import bank.authbatch.main.AuthBatchDbHandler;


public class AdjNoticeDao extends AuthBatchDbHandler{

	public AdjNoticeDao()  throws Exception{
		// TODO Auto-generated constructor stub
		
		
	}

	//public static boolean updateAdjNotice(RowId P_RowId, String sP_Chiname, String sP_CurDate) {
	public static boolean updateAdjNotice( String sP_Chiname, String sP_CurDate, String sP_AdjNoticeCrtDate, String sP_AdjNoticeIdPSeqNo, int nP_AdjNoticeAcctIdx) {
		boolean bL_Result = true;
		
		try {
			
			String sL_Sql = "update CCA_ADJ_NOTICE set SEND_DATE= ? ,CHI_NAME= ? "
							+ "where CARD_ACCT_IDX= ? and ID_P_SEQNO= ? and CRT_DATE = ? ";
						//+ "where RowID= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
//sP_AdjNoticeCrtDate, String sP_AdjNoticeIdPSeqNo, double dP_AdjNoticeAcctIdx			
			ps.setString(1, sP_CurDate);
			ps.setString(2, sP_Chiname);

			ps.setInt(3, nP_AdjNoticeAcctIdx); //CARD_ACCT_IDX
			ps.setString(4, sP_AdjNoticeIdPSeqNo); //ID_P_SEQNO
			ps.setString(5, sP_AdjNoticeCrtDate); //CRT_DATE
			/*
			ps.setString("pSendDate", sP_CurDate);
			ps.setString("pChiName", sP_Chiname);
			ps.setRowId("pRowId", P_RowId);
			*/
	         ps.executeUpdate();
	         ps.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;

		
	}
	public static ResultSet getAdjNotice() {
		
		ResultSet L_AcctBalanceRs = null;
		String sL_Sql = "SELECT  P_SEQNO as AdjNoticePSeqNo,ID_P_SEQNO as AdjNoticeIdPSeqNo,SUP_FLAG  as AdjNoticeSupFlag, CRT_TIME  as AdjNoticeCrtTime, " 
				+"CARD_ACCT_IDX as AdjNoticeAcctIdx, CHI_NAME  as AdjNoticeChiName, MAJOR_CHI_NAME  as AdjNoticeMajorChiName,"
				+"ORG_TOT_CONSUME  as AdjNoticeOrgTotConsume, LMT_TOT_CONSUME as AdjNoticeLmtTotConsume, ADJ_EFF_START_DATE as AdjNoticeAdjEffStartDate, " 
				+"ADJ_EFF_END_DATE as AdjNoticeAdjEffEndDate, SEND_DATE as AdjNoticeSendDate, CRT_DATE as AdjNoticeCrtDate "
				+"FROM   CCA_ADJ_NOTICE "
				+"WHERE  SEND_DATE IS NULL and 1=? " 
				+"ORDER  BY CRT_TIME ";

		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);

			ps.setInt(1, 1);
			L_AcctBalanceRs = ps.executeQuery();

		} catch (Exception e) {
			// TODO: handle exception
			L_AcctBalanceRs = null;
		}
		
		return L_AcctBalanceRs;
	}


}
