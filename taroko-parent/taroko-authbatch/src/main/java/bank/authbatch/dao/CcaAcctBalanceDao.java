package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;



import bank.authbatch.main.AuthBatchDbHandler;



public class CcaAcctBalanceDao extends AuthBatchDbHandler{

	public CcaAcctBalanceDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	public static boolean deleteData(String sP_BeforeDate) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "delete CCA_ACCT_BALANCE "
					+"WHERE STA_DATE< ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_BeforeDate);
				
			ps.executeUpdate();
			ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static ResultSet getAcctBalance(String sP_SysDate) {
		
		ResultSet L_AcctBalanceRs = null;
		String sL_Sql = "SELECT NVL(CARD_ACCT_IDX,0) as AcctBalanceCardAcctIdx from CCA_ACCT_BALANCE where SYS_DATE= ? ";
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_SysDate);
			
			L_AcctBalanceRs = ps.executeQuery();

		} catch (Exception e) {
			// TODO: handle exception
			L_AcctBalanceRs = null;
		}
		
		return L_AcctBalanceRs;
	}

}
