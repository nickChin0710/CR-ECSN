package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;

import bank.authbatch.vo.CrdIdnoVo;


public class CrdIdnoDao extends AuthBatchDbHandler{

	public CrdIdnoDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	public static boolean deleteCrdIdno(String sP_Idno) {
		boolean bL_Result = true;
		try {
			String sL_Sql="DELETE CRD_IDNO "
							+"WHERE ID_NO = ? "
							//+"AND NVL(ID_NO_CODE,'0') = NVL(:CB_CARD_ACCT_ID_SEQ,'0')";
							+"AND ID_NO_CODE = ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_Idno);
			ps.setString(2, "0");
			ps.executeUpdate();
	        ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	public static String getChineseName(String sP_IdPSeqNo) {
		String sL_ChineseName="";
		
		String sL_Sql = "select CHI_NAME as CrdIdNoChiName from CRD_IDNO where ID_P_SEQNO= ? ";
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1 , sP_IdPSeqNo);
			
			ResultSet L_IdNoRs = ps.executeQuery();
			while(L_IdNoRs.next()) {
				sL_ChineseName = L_IdNoRs.getString("CrdIdNoChiName"); 
			}
			releaseConnection(L_IdNoRs);

		} catch (Exception e) {
			// TODO: handle exception
			sL_ChineseName="";
		}
		
		return sL_ChineseName;
		
	}
	public static CrdIdnoVo getCrdIdno(String sP_Idno, String sP_IdnoCode) {
		//proc is GET_CARD_HLDR_since() 
		ResultSet L_ResultSet = null;
		CrdIdnoVo L_CrdIdnoVo = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		/*
		String sL_Sql = "select NVL(CARD_SINCE,' '),NVL(JOB_POSITION,' ') "
				+ "from CRD_IDNO where ID_NO= ? and ID_NO_CODE= ?  "; //CRD_IDNO 的舊table name is CARD_HLDR
		*/
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			/*
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1,sP_Idno);
			ps.setString(2,sP_IdnoCode);

			L_ResultSet = ps.executeQuery();
			*/
			
			G_Ps4CrdIdno.setString(1,sP_Idno);
			G_Ps4CrdIdno.setString(2,sP_IdnoCode);

			L_ResultSet = G_Ps4CrdIdno.executeQuery();

			
			while (L_ResultSet.next()) {
				if (null == L_CrdIdnoVo)
					L_CrdIdnoVo = new CrdIdnoVo();
				L_CrdIdnoVo.setCardSince(L_ResultSet.getString("CARD_SINCE"));
				L_CrdIdnoVo.setJobposition(L_ResultSet.getString("JOB_POSITION"));
			}
			closeResource(L_ResultSet);
			
			
		} catch (Exception e) {
			// TODO: handle exception
			L_CrdIdnoVo = null;
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		return L_CrdIdnoVo;
	}
}
