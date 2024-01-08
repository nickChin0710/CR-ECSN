package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;


public class ActAcnoDao extends AuthBatchDbHandler{

	
	
	
	public ActAcnoDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static ResultSet getActAcno(String sP_IdPSeqNo) {
		ResultSet L_ResultSet=null;
		
		String sL_Sql = "select BILL_SENDING_ZIP, BILL_SENDING_ADDR1,BILL_SENDING_ADDR2,BILL_SENDING_ADDR3,BILL_SENDING_ADDR4,BILL_SENDING_ADDR5, LINE_OF_CREDIT_AMT from ACT_ACNO where ID_P_SEQNO= ? ";
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setString(1, sP_IdPSeqNo);
			
			L_ResultSet = ps.executeQuery();

		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet=null;
		}
		
		return L_ResultSet;

		
	}

	
	public static ResultSet getActAcnoByAcnoPSeqNo(String sP_AcnoPSeqNo) {
		ResultSet L_ResultSet=null;
		
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			/*
			String sL_Sql = "select LINE_OF_CREDIT_AMT from ACT_ACNO where P_SEQNO= ? ";
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_PSeqNo);
			
			L_ResultSet = ps.executeQuery();
			*/
			G_Ps4ActAcno.setString(1, sP_AcnoPSeqNo);
			
			L_ResultSet = G_Ps4ActAcno.executeQuery();

		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet=null;
		}
		
		return L_ResultSet;

		
	}



}
