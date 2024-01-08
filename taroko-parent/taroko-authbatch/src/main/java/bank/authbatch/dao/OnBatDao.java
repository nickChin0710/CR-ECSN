
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-08-03  V1.00.01  yanghan  "将栏位ICBC_RESP_CODE->BANK_RESP_CODE ICBC_RESP_DESC->BANK_RESP_DESC*
******************************************************************************/

package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.Timestamp;



import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;
import bank.authbatch.main.HpeUtil;


public class OnBatDao extends AuthBatchDbHandler{

	public OnBatDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static boolean updateOnBat(String sP_CardNo, String sP_Dog){
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set dop= ?,process_status= ? "
						          +"where card_no = ? "
						          +"and trans_type in (?,?,?) "
						          +"and dog >= to_date(?,'yyyymmddhh24miss')";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql,false);
	         ps.setString(1, "");
	         ps.setString(2, "0");
	         ps.setString(3, sP_CardNo);
	         ps.setString(4, "6");
	         ps.setString(5, "9");
	         ps.setString(6, "51");
	         ps.setString(7, sP_Dog);
	         ps.executeUpdate();
	         ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean deleteData(String sP_BeforeDate) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "delete ONBAT_2ECS "
			//String sL_Sql = "delete ONBAT_2CCAS "
					+"WHERE TO_CHAR(DOG,'YYYYMMDD') < ? AND PROC_STATUS > ? ";
			
			

			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_BeforeDate);
			ps.setInt(2, 0);
				
			ps.executeUpdate();
			ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBat3(String sP_AcctKey, String sP_BaseDog){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set process_status= ? "
						          +"where CARD_ACCT_ID = ? "
						          +"and trans_type = ? "
						          +"and process_status in (?,?) "
						          +"and dog < to_date(?,'yyyymmddhh24miss')";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setString(1, "1");
	         ps.setString(2, sP_AcctKey);
	         ps.setString(3, "2");
	         ps.setString(4, "0");
	         ps.setString(5, "2");
	         ps.setString(6, sP_BaseDog);
	         ps.executeUpdate();
	         ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBat9(int nP_ProcessStatus, Timestamp P_Dop, String sP_ProcDate, String sP_MatchFlag, RowId P_RowId){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set process_status= ?, DOP= ?, PROCESS_DATE= ? ,"
							+"MATCH_FLAG= ? , MATCH_DATE= ? "
						          +"where ROWID = ? ";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setInt(1, nP_ProcessStatus);
	         ps.setTimestamp(2, P_Dop);
	         
	         ps.setString(3, sP_ProcDate);

	         
	         ps.setString(4, sP_MatchFlag);
	         ps.setString(5, sP_ProcDate);
	         ps.setRowId(6, P_RowId);
	         ps.executeUpdate();
	         ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBatProcStatus(int nP_ProcessStatus,int nP_TargetProcStatus, String sP_TargetIcbcRespDesc){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set PROC_STATUS= ?, BANK_RESP_CODE= ? "
						          +"where PROC_STATUS = ? and TRANS_TYPE IN (?,?,? ) ";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);

	         
	         ps.setInt(1, nP_TargetProcStatus);
	         ps.setString(2, sP_TargetIcbcRespDesc);
	         ps.setInt(3, nP_ProcessStatus);
	         ps.setString(4, "6");
	         ps.setString(5, "12");
	         ps.setString(6, "13");
	         ps.executeUpdate();
	         ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBat8(int nP_ProcessStatus, Timestamp P_Dop, String sP_ProcDate, RowId P_RowId, String sP_IcbcRespDesc){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set process_status=?, DOP= ?, PROCESS_DATE= ?, BANK_RESP_DESC= ? "
						          +"where ROWID = ? ";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setInt(1, nP_ProcessStatus);
	         ps.setTimestamp(2, P_Dop);
	         ps.setString(3, sP_ProcDate);
	        
	         ps.setString(4, sP_IcbcRespDesc);
	         ps.setRowId(5, P_RowId);
	         ps.executeUpdate();
	         ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static int getOnBatRecCount(int nP_ProcStatus, String sP_TransType, int nP_ToWhich, String sP_CardAcctId){
		int nL_RecCount=0;
		
		try {
			ResultSet L_ResultSet = null;
			ExecutionTimer L_Timer = new ExecutionTimer();
			L_Timer.start();
			String sL_Sql = "select COUNT(1) as OnBatRecCount from ONBAT_2CCAS " 
								+ " WHERE trans_type = ? AND to_which = ? "
								+ "AND PROC_STATUS = ? and CARD_ACCT_ID= ? "
								+ "ORDER  BY DOG ";	
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_TransType);
			ps.setInt(2, nP_ToWhich);
			ps.setInt(3, nP_ProcStatus);
			ps.setString(4, sP_CardAcctId);
			
		
			
			L_ResultSet = ps.executeQuery();
			
			while(L_ResultSet.next()) {
				nL_RecCount = L_ResultSet.getInt("OnBatRecCount");
			}
			
			releaseConnection(L_ResultSet);

		} catch (Exception e) {
			// TODO: handle exception
			nL_RecCount=0;
		}
		
		return nL_RecCount;
	}
	
	public static boolean updateOnBat7(int nP_ProcessStatus, Timestamp P_Dop, String sP_ProcDate, RowId P_RowId){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set process_status= ? , DOP= ? , PROCESS_DATE= ?  "
									          +"where ROWID = ?  ";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setInt(1, nP_ProcessStatus);
	         ps.setTimestamp(2, P_Dop);
	         ps.setString(3, sP_ProcDate);
	         ps.setRowId(4, P_RowId);

	         ps.executeUpdate();
	         ps.close();
	         			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBat6(int nP_ProcessStatus, Timestamp P_Dop, String sP_ProcDate, String sP_TransType, int nP_ToWhich, String sP_CardAcctId){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set process_status= ? , DOP= ? , PROCESS_DATE= ? "
						          +"where TRANS_TYPE = ? and TO_WHICH= ? and CARD_ACCT_ID= ? ";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setInt(1, nP_ProcessStatus);
	         ps.setTimestamp(2, P_Dop);
	         ps.setString(3, sP_ProcDate);
	         ps.setString(4,sP_TransType);
	         ps.setInt(5, nP_ToWhich);
	         ps.setString(6, sP_CardAcctId);
	         ps.executeUpdate();
	         ps.close();
	         			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	
	public static boolean updateOnBat9(int nP_SrcProcessStatus,int nP_NewProcessStatus, Timestamp P_Dop, String sP_ProcDate, String sP_TransType, int nP_ToWhich, String sP_CardNo){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set process_status= ? , DOP= ? , PROCESS_DATE= ? "
						          +"where TRANS_TYPE = ? and TO_WHICH= ? and process_status= ? ,CARD_NO= ? ";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setInt(1, nP_NewProcessStatus);
	         ps.setTimestamp(2, P_Dop);
	         ps.setString(3, sP_ProcDate);
	         ps.setString(4,sP_TransType);
	         ps.setInt(5, nP_ToWhich);
	         ps.setInt(6, nP_SrcProcessStatus);
	         ps.setString(7,sP_CardNo);
	         ps.executeUpdate();
	         ps.close();
	         			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBat4(String sP_AcctKey){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set process_status= ? "
						          +"where CARD_ACCT_ID = ? "
						          +"and trans_type = ? "
						          +"and process_status in ( ? , ? ) ";

	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setString(1, "1");
	         ps.setString(2, sP_AcctKey);
	         ps.setString(3, "2");
	         ps.setString(4, "0");
	         ps.setString(5, "2");
	         
	         ps.executeUpdate();
	         ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBat5(String sP_RowId, String sP_ProcessResult){
		//proc is update_ecs_onbat()
	
		
		boolean bL_Result = true;
		try {
			String sL_CurDateTime = HpeUtil.getCurDateTimeStr(false);
			String sL_Sql = "update ONBAT_2CCAS SET  dop = ? , "
								+"process_status = ? "
								+"WHERE rowid = ? ";

	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	         
	         ps.setString(1, sL_CurDateTime);
	         ps.setString(2, sP_ProcessResult);
	         ps.setString(3, sP_RowId);
	         ps.executeUpdate();
	         ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateOnBat2(String sP_IdNo,String sP_AcctKey, String sP_Dog){
	
		
		boolean bL_Result = true;
		try {
			String sL_Sql = "update ONBAT_2CCAS set dop= ? ,process_status= ? "
						          +"where CARD_HLDR_ID = ? "
						          +"and trans_type = ? "
						          +"and dog >= to_date( ? ,'yyyymmddhh24miss')";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
	        ps.setString(1, "");
	        ps.setString(2, "0");
	        ps.setString(3, sP_IdNo);
	        ps.setString(4, "14");
	        ps.setString(5, sP_Dog);
	        ps.executeUpdate();
	        ps.close();
			
	         
	         
	         sL_Sql = "update ONBAT_2CCAS set dop= ? ,process_status= ? "
			          +"where CARD_ACCT_ID = ? "
			          +"and trans_type in ( ? , ? ) "
			          +"and dog >= to_date( ? ,'yyyymmddhh24miss')";

	         PreparedStatement ps2 = getPreparedStatement(sL_Sql, false);
	         ps2.setString(1, "");
	         ps2.setString(2, "0");
	         ps2.setString(3, sP_AcctKey);
	         ps2.setString(4, "2");
	         ps2.setString(5, "55");
	         ps2.setString(6, sP_Dog);
	         ps2.executeUpdate();
	         ps2.close();
	         
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	public static ResultSet getOnBat2(String sP_TransType, int nP_ToWhich, int nP_ProcStatus, String sP_OnBatTableName, String sP_SelectFields) {
		//用在 ECS140 and ECS004
		
		ResultSet L_ResultSet = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "select " + sP_SelectFields + " from " + sP_OnBatTableName 
							+ " WHERE trans_type = ? AND to_which = ? "
							+ "AND PROC_STATUS = ? "
							+ "ORDER  BY DOG ";	
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_TransType);
			ps.setInt(2, nP_ToWhich);
			ps.setInt(3, nP_ProcStatus);

			
		
			
			L_ResultSet = ps.executeQuery();
			
			

			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		
		return L_ResultSet;
	}

	public static ResultSet getOnBat3(String sP_TransType, int nP_ToWhich, int nP_ProcStatus, String sP_OnBatTableName, String sP_SelectFields, String sP_ExtraWhereCond) {
		//用在 ECS004
		
		ResultSet L_ResultSet = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "select " + sP_SelectFields + " from " + sP_OnBatTableName 
							+ " WHERE trans_type = ? AND to_which = ? "
							+ "AND PROC_STATUS = ? " 
							+ sP_ExtraWhereCond
							+ " ORDER  BY DOG ";	
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_TransType);
			ps.setInt(2, nP_ToWhich);
			ps.setInt(3, nP_ProcStatus);

			
		
			
			L_ResultSet = ps.executeQuery();
			
			

			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		
		return L_ResultSet;
	}

	public static ResultSet getOnBat4(String sP_TransType, int nP_ToWhich, int nP_ProcStatus, String sP_OnBatTableName, String sP_SelectFields, String sP_CardNo) {
		//用在 ECS060
		
		ResultSet L_ResultSet = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "select " + sP_SelectFields + " from " + sP_OnBatTableName 
							+ " WHERE trans_type = ? AND to_which = ? "
							+ "AND PROC_STATUS = ? and CARD_NO= ? " ; 
	
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_TransType);
			ps.setInt(2, nP_ToWhich);
			ps.setInt(3, nP_ProcStatus);
			ps.setString(4, sP_CardNo);

			
		
			
			L_ResultSet = ps.executeQuery();
			
			

			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		
		return L_ResultSet;
	}

	public static ResultSet getOnBat(String sP_TransType, int nP_ToWhich, int nP_CardAcctIdx, int nP_Ecs100, int nP_ProcessStatus) {

		ResultSet L_ResultSet=null;
		try {
			String sL_Sql = "SELECT ROWID as OnBatRowId,NVL(CARD_NO,' ') as OnBatCardNo , NVL(TRANS_AMT,0) as OnBatTransAmt  FROM ONBAT_2CCAS "
							+ "WHERE TRANS_TYPE= ? and AND TO_WHICH= ? AND CARD_NO IN (SELECT CARD_NO "
							+ "FROM CCA_CARD_BASE WHERE CARD_ACCT_IDX = "
							+ " DECODE(? ,1,CARD_ACCT_IDX, ? )) "
							+ " AND PROCESS_STATUS= ? FETCH FIRST ? ROWS ONLY ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_TransType);
			ps.setInt(2, nP_ToWhich);
			ps.setInt(3, nP_Ecs100);
			ps.setInt(4, nP_CardAcctIdx);
			ps.setInt(5, nP_ProcessStatus);
			ps.setInt(6, 2);
			
			L_ResultSet = ps.executeQuery();
			
			


			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		return L_ResultSet;
	}
	public static ResultSet getOnBat(String sP_TransType, String sP_AccountType, String sP_CardHolderId, String sP_CardAcctId) {
		//proc is select_ecs_onbat_12()
		
		ResultSet L_ResultSet = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "select to_char(max(dog),'yyyymmddhh24miss') as OnBatDog from ONBAT_2CCAS "
							+ "WHERE trans_type = ? AND account_type = ? "
							+ "AND card_hldr_id = ? "
							+ "AND card_acct_id = ? ";	
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_TransType);
			ps.setString(2, sP_AccountType);
			ps.setString(3, sP_CardHolderId);
			ps.setString(4, sP_CardAcctId);
			
		
			
			L_ResultSet = ps.executeQuery();
			
			

			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		
		return L_ResultSet;
	}
}
