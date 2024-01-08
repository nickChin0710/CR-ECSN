package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;

import bank.authbatch.vo.CcaUnmatchVo;

public class CcaUnmatchDao   extends AuthBatchDbHandler{

	public CcaUnmatchDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static boolean insertUnmatchRec(CcaUnmatchVo P_CcaUnmatchVo) {
		
		boolean bL_Result = true;
		
		try {
			/*
			String sL_Sql = "INSERT INTO CCA_UNMATCH(U_DATE,CARD_NO,TX_DATE,AUTH_NO, "
                    		+" AMT_NT,TRANS_TYPE,REF_NO,PROC_CODE,MCC_CODE,MCHT_NO,"
                    		+"MESSAGE_HEAD5,MESSAGE_HEAD6,BIT127_REC_DATA,U_TIME,"
                    		+"AUTH_DATE,AUTH_AMT)";
			sL_Sql += "VALUES(? ,? , ? , ? ,"
                    +"? ,? , ? , ? , ? , ? , "
                    +"?, ? , ? ,? ,"
                    +"?, ?)";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
        	ps.setString(1, P_CcaUnmatchVo.getUDate());			
        	ps.setString(2, P_CcaUnmatchVo.getCardNo());
        	ps.setString(3, P_CcaUnmatchVo.getTxDate());
        	ps.setString(4, P_CcaUnmatchVo.getAuthNo());
        	ps.setInt(5, P_CcaUnmatchVo.getAmtNt());
        	ps.setString(6, P_CcaUnmatchVo.getTransType());
        	ps.setString(7, P_CcaUnmatchVo.getRefNo());
        	ps.setString(8, P_CcaUnmatchVo.getProcCode());
        	ps.setString(9, P_CcaUnmatchVo.getMccCode());
        	ps.setString(10, P_CcaUnmatchVo.getMchtNo());
        	ps.setString(11, P_CcaUnmatchVo.getMessageHead5());
        	
        	ps.setString(12, P_CcaUnmatchVo.getMessageHead6());
        	ps.setString(13, P_CcaUnmatchVo.getBit127RecData());
        	ps.setString(14, P_CcaUnmatchVo.getUTime());
        	ps.setString(15, P_CcaUnmatchVo.getAuthDate());
        	ps.setInt(16, P_CcaUnmatchVo.getAuthAmt());
        	
			ps.executeUpdate();
			ps.close();
			*/
			G_Ps4InsertCcaUnMatch.setString(1, P_CcaUnmatchVo.getUDate());			
			G_Ps4InsertCcaUnMatch.setString(2, P_CcaUnmatchVo.getCardNo());
			
			G_Ps4InsertCcaUnMatch.setString(3, P_CcaUnmatchVo.getTxDate());
			G_Ps4InsertCcaUnMatch.setString(4, P_CcaUnmatchVo.getAuthNo());
			G_Ps4InsertCcaUnMatch.setInt(5, P_CcaUnmatchVo.getAmtNt());
			G_Ps4InsertCcaUnMatch.setString(6, P_CcaUnmatchVo.getTransType());
			G_Ps4InsertCcaUnMatch.setString(7, P_CcaUnmatchVo.getRefNo());
			G_Ps4InsertCcaUnMatch.setString(8, P_CcaUnmatchVo.getProcCode());
			G_Ps4InsertCcaUnMatch.setString(9, P_CcaUnmatchVo.getMccCode());
			G_Ps4InsertCcaUnMatch.setString(10, P_CcaUnmatchVo.getMchtNo());
			G_Ps4InsertCcaUnMatch.setString(11, P_CcaUnmatchVo.getMessageHead5());
        	
			G_Ps4InsertCcaUnMatch.setString(12, P_CcaUnmatchVo.getMessageHead6());
			G_Ps4InsertCcaUnMatch.setString(13, P_CcaUnmatchVo.getBit127RecData());
			G_Ps4InsertCcaUnMatch.setString(14, P_CcaUnmatchVo.getUTime());
			G_Ps4InsertCcaUnMatch.setString(15, P_CcaUnmatchVo.getAuthDate());
			G_Ps4InsertCcaUnMatch.setInt(16, P_CcaUnmatchVo.getAuthAmt());
        	
        	G_Ps4InsertCcaUnMatch.executeUpdate();
			

			
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
}
