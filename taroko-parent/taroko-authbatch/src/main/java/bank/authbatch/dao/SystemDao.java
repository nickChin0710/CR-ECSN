package bank.authbatch.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

//import javax.xml.bind.ParseConversionEvent;

import bank.authbatch.main.AuthBatchDbHandler;

public class SystemDao extends AuthBatchDbHandler{

	public SystemDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static String getNextSeqVal(String sP_SequenceName)  throws Exception {
		//get sequence value
		
		String sL_SeqVal = "0";
		try {
			/*
			String sL_Sql = "  SELECT NEXT VALUE FOR " + sP_SequenceName + " FROM sysibm.sysdummy1 "  ;
			   
			//System.out.println("getNextSeqVal sql:" + sL_Sql + "==");

			
			
			PreparedStatement Db2Stmt = P_Conn.prepareStatement(sL_Sql);	
		
		
			ResultSet L_ResultSet = Db2Stmt.executeQuery();
		
								
			if (L_ResultSet.next()) {
				sL_SeqVal = L_ResultSet.getString(1);
		
			}
			
			
			L_ResultSet.close();
			*/
			
			//G_Ps4GetSequenceNextValue.setNString(1, sP_SequenceName);
			
			ResultSet L_ResultSet = null;
			
			// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//			if (sP_SequenceName.toUpperCase(Locale.TAIWAN).equals("ECS_CARD_ACCT_IDX"))
//				L_ResultSet = G_Ps4GetSequenceNextValue1.executeQuery();
//			else if (sP_SequenceName.toUpperCase(Locale.TAIWAN).equals("ECS_TRACE_NO"))
//				L_ResultSet = G_Ps4GetSequenceNextValue2.executeQuery();
			
			if (sP_SequenceName.toUpperCase(Locale.TAIWAN).equals("ECS_TRACE_NO"))
				L_ResultSet = G_Ps4GetSequenceNextValue2.executeQuery();

			
			

            if (L_ResultSet == null) {
                throw new RuntimeException("sP_SequenceName[" + sP_SequenceName + "] is invalid.");
            }
			if (L_ResultSet.next()) {
				sL_SeqVal = L_ResultSet.getString(1);
		
			}
			
			
			L_ResultSet.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			sL_SeqVal = "0";
		}
		return sL_SeqVal;
		
	}

}
