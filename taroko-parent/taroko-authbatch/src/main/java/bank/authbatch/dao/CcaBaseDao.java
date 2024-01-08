package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import bank.authbatch.main.AuthBatchDbHandler;

import bank.authbatch.main.ExecutionTimer;


import bank.authbatch.vo.CcaBaseVo;


public class CcaBaseDao extends AuthBatchDbHandler {

	public CcaBaseDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static boolean updateCcaBase(String sP_CardNo, String sP_BaseDog) {
		boolean bL_Result=true;
		
		try {
			String sL_Sql = "update CCA_BASE set dop= ? ,process_status= ? "
	                 			+"where card_no = ? " 
	                 			+"and dog = to_date( ? ,'yyyymmddhh24miss')";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			ps.setString(1, "");
			ps.setString(2, "0");
			ps.setString(3, sP_CardNo);
			ps.setString(4, sP_BaseDog);
			ps.executeUpdate();
	        ps.close();
	        

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result=false;
		}
		
		return bL_Result;
		
	}
	public static ResultSet getCcaBase(String sP_CardNo, String sP_BaseDog) {
		ResultSet L_ResultSet = null;
		//, card_acct_id => no field => account key
        //, card_hldr_id => no field => id_no
		try {
			String sL_Sql = " SELECT TO_CHAR(A.DOG,'YYYYMMDDHH24MISS') as CcaBaseDog, "
								+"A.account_type as CcaBaseAccountType, "
								+"A.acct_no as CcaBaseAcctNo, "
								+"C.ACCT_KEY as CcaBaseAcctKey, "
								+"D. ID_NO as CcaBaseIdNo "
								+"FROM CCA_BASE A, DBA_ACNO C, CRD_IDNO D "   
								+"WHERE A.card_no = ? "
								+"and A.P_SEQNO=C.P_SEQNO "
								+"and A.ID_P_SEQNO=D.ID_P_SEQNO "
								+"and A.dog = (select min(dog) " 
								+"from CCA_BASE "
								+"where card_no = ? "
								+"and dog    >= to_date( ? ,'yyyymmddhh24miss'))";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_CardNo);
			ps.setString(2, sP_CardNo);
			ps.setString(3, sP_BaseDog);
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆
			//ps.setString("p2", sP_SysKey);
		
			
			L_ResultSet = ps.executeQuery();
								

		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		
		return L_ResultSet;
	}
	public static ResultSet getCcaBase(int nP_ProcessStatus) {
		//CCS_BASE/CCA_BASE ECS-CCAS卡片介面檔
		ResultSet L_ResultSet = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		String sL_Sql = "select CARD_NO as CcaBaseCardNo,"
				+ "P_SEQNO as CcaBasePSeqNo,"
				+ "CARD_HLDR_ID as CcaBaseCardHolderId," 
				+ "RULE as CcaBaseRule," 
				+ "PAYMENT_RULE as CcaBasePaymentRule,"
				+ "ACCOUNT_TYPE as CcaBaseAccountType,"
				+ "CARD_ACCT_ID as CcaBaseCardAcctId,"
				+ "ID_P_SEQNO as CcaBaseIdPSeqNo, "
				+ "VALID_FROM as CcaBaseValidFrom,"
				+ "VALID_TO as CcaBaseValidTo,"
				+ "NVL(OLD_CARD_NO,' ') as CcaBaseOldCardNo,"
				+ "NVL(CVC2,'   ') as CcaBaseCvc2,"
				+ "SOURCE as CcaBaseSource,"
				+ "NVL(ENG_NAME,' ') as CcaBaseEngName,"
				+ "NVL(BUSINESS_CARD,'N') as CcaBaseBusinessCard,"
				+ "NVL(MEMBER_SINCE,' ') as CcaBaseMemberSince,"
				+ "NVL(CARD_TYPE,'N') as CcaBaseCardType,"
				+ "NVL(CREDIT_LIMIT,0) as CcaBaseCreditLimit,"
				+ "NVL(PIN_OF_ACTIVE,' ') as CcaBasePinOfActive"
				+ "NVL(PIN_OF_VOICE,' ') as CcaBasePinOfVoice,"
				+ "GROUP_CODE as CcaBaseGroupCode,"
				+ "PVKI as CcaBasePvki,"
				+ "PIN_BLOCK as CcaBasePinBlock,"
				+ "BANK_ACTNO as CcaBaseBankActNo,"
				+ "ACCT_NO as CcaBaseAcctNo,"
				+ "NVL(COMBO_INDICATOR,'N') as CcaBaseComboIndicator,"
				+ "DC_CURR_CODE as CcaBaseDcCurrCode, "
				+ "gp_no as CcaBaseGpNo, "
				+ "CORP_P_SEQNO as CcaBaseCorpPSeqNo, "
				+ "MAJOR_ID_P_SEQNO as CcaBaseMajorIdPSeqNo, "
				+ "ACNO_FLAG as CcaBaseAcNoFlag "
				
				+ "from CCA_BASE where PROCESS_STATUS= ?  order by DOG  "; //key....
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setInt(1, nP_ProcessStatus);
			ps.setFetchSize(1000);//設定每次只get 1000筆
			//ps.setString("p2", sP_SysKey);
		
			
			L_ResultSet = ps.executeQuery();
			/*
			//http://commons.apache.org/proper/commons-dbutils/examples.html
			//https://stackoverflow.com/questions/9588034/dbutils-using-resultsethandler
			while (rs.next()) {
				L_Obj = new CcaCardBaseVo();
				L_Obj.SysData1 = rs.getString("SysData1");
				L_Obj.SysData2 = rs.getString("SysData2");
				L_Obj.SysData3 = rs.getString("SysData3");
				L_Obj.SysData4 = rs.getString("SysData4");
				CcaCardBaseVo.ccaCardBaseList.add(L_Obj);
			}
			rs.close();
			ps.close();
			Db2Connection.commit();
			*/
		}
		catch (Exception e) {
			
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		return L_ResultSet;
	}

}
