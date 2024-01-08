package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;


public class CrdCorrelateDao extends AuthBatchDbHandler{

	public CrdCorrelateDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	public static String getFhFlag(String sP_CorrelateId) {
		String sL_FhFlag = "";
		
		try {
			String sL_Sql = "SELECT FH_FLAG from CRD_CORRELATE WHERE CORRELATE_ID = ? AND CRT_DATE = (SELECT MAX(CRT_DATE) FROM CRD_CORRELATE WHERE CORRELATE_ID = ? )";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_CorrelateId);
			ps.setString(2, sP_CorrelateId);
	
			
			
			ResultSet L_ResultSet = ps.executeQuery();
			
			while (L_ResultSet.next()) {
				sL_FhFlag = L_ResultSet.getString("FH_FLAG").trim();
			}
			releaseConnection(L_ResultSet);
		} catch (Exception e) {
			// TODO: handle exception
			sL_FhFlag = "";
		}
		
		return sL_FhFlag;
	}

}
