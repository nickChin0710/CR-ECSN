package bank.authbatch.dao;

import java.sql.PreparedStatement;

import bank.authbatch.main.AuthBatchDbHandler;


public class CcaStaTxUnormalDao extends AuthBatchDbHandler{

	public CcaStaTxUnormalDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	public static boolean deleteData(String sP_BeforeDate) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "delete CCA_STA_TX_UNORMAL "
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


}
