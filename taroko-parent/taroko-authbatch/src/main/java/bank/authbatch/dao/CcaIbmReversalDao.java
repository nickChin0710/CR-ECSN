package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;


public class CcaIbmReversalDao  extends AuthBatchDbHandler{

	public CcaIbmReversalDao()  throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	public static boolean deleteData(String sP_BeforeDate) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "delete CCA_IBM_REVERSAL "
					+"WHERE TX_DATE<  ? ";
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

	public static boolean updateIbmReversal(ResultSet P_IbmReversalRs, String sP_IbmRespCode) {
		//proc is update_ibm_reversal();
		
		boolean bL_Result = true;
		try {
			String sL_CurDate = HpeUtil.getCurDateStr("");
			String sL_SrcTxDate = P_IbmReversalRs.getString("TX_DATE");
			String sL_SrcTxTime = P_IbmReversalRs.getString("TX_TIME");
			String sL_SrcCardNo = P_IbmReversalRs.getString("CARD_NO");
			String sL_SrcAuthNo = P_IbmReversalRs.getString("AUTH_NO");
			
			String sL_Sql = "UPDATE CCA_IBM_REVERSAL SET IBM_RSP_CODE= ? , PROC_CODE= ? , PROC_DATE= ? ";
			sL_Sql += " WHERE TX_DATE = ? and TX_TIME= ? and CARD_NO=? and AUTH_NO=? " ;

			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			ps.setString(1, sP_IbmRespCode); //h_bit39_code
			ps.setString(2, "Y"); 
			ps.setString(3,sL_CurDate );
			ps.setString(4,sL_SrcTxDate );
			ps.setString(5,sL_SrcTxTime );
			ps.setString(6,sL_SrcCardNo );
			ps.setString(7,sL_SrcAuthNo );

			ps.executeUpdate();
			

			ps.close();



		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	public static ResultSet getIbmReversal() {
		
		ResultSet L_ResultSet=null;
		try {
			String sL_Sql = "SELECT	TX_DATE,              TX_TIME,"
								+"AUTH_NO,                CARD_NO,"
								+"TRANS_TYPE,             CARD_ACCT_IDX,"
								+"CARD_ACCT_ID,           CARD_ACCT_ID_SEQ,"
								+"CARD_HLDR_ID,           CARD_HLDR_ID_SEQ,"
								+"RISK_TYPE,              MCHT_NO,"
								+"TERM_ID,                ACQ_ID,"
								+"CARD_LEVEL,             TRACE_NO,"
								+"REF_NO ,                TRANS_AMT,"
								+"MSG_DATA                "
								+"FROM CCA_IBM_REVERSAL "
								+"WHERE PROC_CODE =? "
								+"ORDER BY TX_DATE,TX_TIME";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, "N");
			
	
			
			L_ResultSet = ps.executeQuery();

		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet=null;
		}
		
		return L_ResultSet;
	}
}
