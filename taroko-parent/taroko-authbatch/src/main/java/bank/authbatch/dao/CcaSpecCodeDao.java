package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;



public class CcaSpecCodeDao extends AuthBatchDbHandler{

	public CcaSpecCodeDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	public static ResultSet getReason(String sP_SpecCode) {
		ResultSet L_ResultSet=null;
		try {
			String sL_Sql = "select NVL(NEG_REASON,'') as NegReason, NVL(VISA_REASON,'') as VisaReason, NVL(MAST_REASON,'') as MasterReason, NVL(JCB_REASON,'') as JcbReason, NVL(ACED_REASON,'') as AcedReason "
							+ " from CCA_SPEC_CODE where SPEC_CODE= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_SpecCode);
			
			L_ResultSet = ps.executeQuery();

			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet=null;
		}
		
		return L_ResultSet;
	}

}
