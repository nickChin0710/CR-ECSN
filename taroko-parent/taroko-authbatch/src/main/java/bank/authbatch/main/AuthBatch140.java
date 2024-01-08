package bank.authbatch.main;

import java.sql.ResultSet;

import bank.authbatch.dao.CardAcctDao;
import bank.authbatch.dao.CardAcctIndexDao;
import bank.authbatch.dao.CcaConsumeDao;
import bank.authbatch.dao.CcaCardBaseDao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.dao.CcaBaseDao;
import bank.authbatch.dao.CrdCardDao;
import bank.authbatch.dao.CrdIdnoDao;
import bank.authbatch.dao.DbcCardDao;
import bank.authbatch.dao.OnBatDao;
import bank.authbatch.vo.CcaSysParm3Vo;


public class AuthBatch140  extends BatchProgBase {

	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
		try {
			setStopRun(1);
			

			connDb();

			ResultSet L_OnBatResultSet = null;
			String sL_TransType="20",  sL_OnBatTableName="ONBAT_2CCAS";
			int nL_ToWhich=2, nL_ProcStatus=0;
			while (1==getStopRun()) {
				getSysParm3();
				if (0==getStopRun()) {
					commitDb();
					break;
				}
				String sL_ProcessResult="1";
				String sL_SelectFields = " ROWID as OnBatRowId, CARD_NO as OnBatCardNo, TO_CHAR(DOG,'YYYYMMDDHH24MISS') as OnBatDog ";
				L_OnBatResultSet = OnBatDao.getOnBat2(sL_TransType, nL_ToWhich, nL_ProcStatus, sL_OnBatTableName, sL_SelectFields);
				while (L_OnBatResultSet.next()) {
					sL_ProcessResult="1";
					if (!processData(L_OnBatResultSet)) {
						sL_ProcessResult="2";
						String sL_OnBatCardNo = L_OnBatResultSet.getString("OnBatCardNo");
						
						if ((sL_OnBatCardNo.substring(0, 6).equals("421333")) || (sL_OnBatCardNo.substring(0, 6).equals("421351"))) {
							continue;	
						
						}
						
						
						
					}
					String sL_OnBatRowId = L_OnBatResultSet.getString("OnBatRowId");
					OnBatDao.updateOnBat5(sL_OnBatRowId, sL_ProcessResult);
					
					commitDb();
				} 
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
            closeDb();
        }
		

	}

	private boolean processData(ResultSet P_OnBatRs) {
		//proc is proc_card()
		boolean bL_Result = true;
		
		String sL_OnBatCardNo="", sL_OnBatDog="";
		ResultSet L_CardBaseAndCardAcctRs = null;
		try {
			sL_OnBatCardNo = P_OnBatRs.getString("OnBatCardNo");
			sL_OnBatDog = P_OnBatRs.getString("OnBatDog");
			
			L_CardBaseAndCardAcctRs = CcaCardBaseDao.getCcaCardBaseAndCardAcctAndAcno(sL_OnBatCardNo);
			
			boolean bL_HasData=false;
			String sL_ClassCode="", sL_CardAcctIdx="", sL_AcctKey="";// Howard: sL_AcctKey== CARD_ACCT_ID + CARD_ACCT_ID_SEQ
			while ( L_CardBaseAndCardAcctRs.next() )
			{
				bL_HasData = true;
				sL_ClassCode = L_CardBaseAndCardAcctRs.getString("ClassCode");
				sL_CardAcctIdx = L_CardBaseAndCardAcctRs.getString("CardAcctIdx");
				sL_AcctKey = L_CardBaseAndCardAcctRs.getString("AcctKey");
			}
			L_CardBaseAndCardAcctRs.close();
			
			if (!bL_HasData) {
			    System.out.println("No Data");
			    return false;
			}
			
			if ((sL_ClassCode.length()>=2) && (!"A4".equals(sL_ClassCode.substring(0, 2)))) {
				 return false;
			}
			
			if (!deleteData(sL_ClassCode, sL_CardAcctIdx, sL_AcctKey, sL_OnBatCardNo, sL_OnBatDog)) {
				 return false;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	private boolean deleteData(String sL_ClassCode, String sP_CardAcctIdx, String sP_AcctKey, String sP_CardNo, String sP_OnBatDog) {
		boolean bL_Result=true;
		
		try {
			bL_Result = CardAcctIndexDao.deleteCardAcctIdx(sP_CardAcctIdx);
			if (!bL_Result)
				return false;

			
			bL_Result = CardAcctDao.deleteCardAcct(sP_CardAcctIdx);
			if (!bL_Result)
				return false;

			
			bL_Result = CcaConsumeDao.deleteCcaConsume(sP_CardAcctIdx);
			if (!bL_Result)
				return false;

			
			bL_Result = CcaCardBaseDao.deleteCardBase(sP_CardAcctIdx, sP_CardNo);
			if (!bL_Result)
				return false;

			ResultSet L_CcaBaseRs = CcaBaseDao.getCcaBase(sP_CardNo, sP_OnBatDog);
			
			boolean bL_HasData=false;
			String sL_CcaBaseDog="", sL_CcaBaseAccountType="", sL_CcaBaseAcctNo="";
			String sL_CcaBaseAcctKey="", sL_CcaBaseIdNo="";
			while(L_CcaBaseRs.next()) {
				sL_CcaBaseDog=L_CcaBaseRs.getString("CcaBaseDog");
				sL_CcaBaseAccountType=L_CcaBaseRs.getString("CcaBaseAccountType");
				sL_CcaBaseAcctNo=L_CcaBaseRs.getString("CcaBaseAcctNo");
				sL_CcaBaseAcctKey=L_CcaBaseRs.getString("CcaBaseAcctKey"); // == card_acct_id
				sL_CcaBaseIdNo=L_CcaBaseRs.getString("CcaBaseIdNo");// == card_hldr_id
				bL_HasData = true;
			}
			L_CcaBaseRs.close();
			
			if (bL_HasData) {
				bL_Result = CcaBaseDao.updateCcaBase(sP_CardNo, sP_OnBatDog);
				if (!bL_Result)
					return false;
				
				
				bL_Result = OnBatDao.updateOnBat(sP_CardNo, sP_OnBatDog);
				if (!bL_Result)
					return false;
				
				bL_Result = OnBatDao.updateOnBat3(sP_AcctKey, sL_CcaBaseDog);
				if (!bL_Result)
					return false;

			}
			else{
				bL_Result = OnBatDao.updateOnBat4(sP_AcctKey);
				if (!bL_Result)
					return false;

			}

			
			ResultSet L_CrdCardRs = CrdCardDao.getCrdCard(sL_CcaBaseIdNo);
			while (L_CrdCardRs.next() ) {
				if (!"A4".equals(L_CrdCardRs.getString("CARD_TYPE"))) {
					bL_Result=true;
				}
	
			}
			L_CrdCardRs.close();
			
			if (!bL_Result) {
				ResultSet L_DbcCardRs = DbcCardDao.getDbcCard(sL_CcaBaseIdNo);
				while (L_DbcCardRs.next()) {
					if (!"A4".equals(L_DbcCardRs.getString("CARD_TYPE"))) {
						bL_Result=true;
					}
					
				}
				L_DbcCardRs.close();
			}
			
			if (!bL_Result)
				CrdIdnoDao.deleteCrdIdno(sL_CcaBaseIdNo);

			
			/*
			boolean bL_IsEmpty1 = CrdCardDao.isEmptyResultSet(L_CrdCardRs);
			
			ResultSet L_DbcCardRs = DbcCardDao.getDbcCard(sL_CcaBaseIdNo);
			boolean bL_IsEmpty2 = CrdCardDao.isEmptyResultSet(L_DbcCardRs);
			
			
			if ((!bL_IsEmpty1) || (!bL_IsEmpty2)) {
				if (!bL_IsEmpty1) {
					if (!"A4".equals(L_CrdCardRs.getString("CARD_TYPE"))) {
						bL_ReturnTrue=true;
					}
				
				}
				if (!bL_IsEmpty2) {
					if (!"A4".equals(L_DbcCardRs.getString("CARD_TYPE"))) {
						bL_ReturnTrue=true;
					}
				
				}
				L_CrdCardRs.close();
				L_DbcCardRs.close();
				if (bL_ReturnTrue)
					return true;

			}
			else {
				CrdIdnoDao.deleteCrdIdno(sL_CcaBaseIdNo);

			}
			*/
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result=false;
		}
		
		return bL_Result;
	}
	private static void getSysParm3() {
		
		setSleepTime(10);
		setStopRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("ECS140","SLEEP", true, "1")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				if ("1".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData4)) {  
					
					writeLog("E", "AuthBatch_1xx is running! Could not execute AuthBatch_140!");
					
					setStopRun(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2));
					setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1));
					return;
				}
			}
			
			
		}

		

	}

	public AuthBatch140()  throws Exception{
		// TODO Auto-generated constructor stub
	}

}
