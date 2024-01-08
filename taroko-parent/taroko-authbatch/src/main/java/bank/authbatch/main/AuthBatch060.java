package bank.authbatch.main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.Timestamp;


import bank.authbatch.dao.AuthTxLogDao;
import bank.authbatch.dao.CardAcctDao;
import bank.authbatch.dao.CardAcctIndexDao;
import bank.authbatch.dao.CcaCardBaseDao;
import bank.authbatch.dao.CcaSysParm1Dao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.dao.CcaUnmatchDao;
import bank.authbatch.dao.CcaAccountDao;
import bank.authbatch.dao.OnBatDao;
import bank.authbatch.vo.CardAcctVo;
import bank.authbatch.vo.CcaCardBaseVo;
import bank.authbatch.vo.CcaSysParm3Vo;
import bank.authbatch.vo.CcaUnmatchVo;
import bank.authbatch.vo.Data060Vo;


public class AuthBatch060 extends BatchProgBase{

	boolean bG_IgnoreProc = false, bG_AddUnMatch=false;
	double dG_UBound=0, dG_LBound=0, dG_LParm=0, dG_UParm=0;
	double dG_LRate1=1,dG_URate1=1;
	int nG_CaTotAmtConsume=0, nG_CaTotAmtPreCash=0;
	int nG_DbCardLimit=0, nG_L1Count=0, nG_L2Count=0, nG_L3Count=0;
	String sG_CardAcctId = "0000000000";
	String sG_Bit127RecData="", sG_MessageHead5="", sG_DbMatchFlag="";
	int nG_CardAcctIdx=0;
	int nG_Continue=0, nG_CounterOk=0, nG_SkipCount=0;
	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
		try {

			setStopRun(1);


			connDb();



			getAuthParm();
			
			while (1==getStopRun()) {
				getSysParm3();//TB_SYS_PARM3
				if (0==getStopRun())
					break;
				if (0==getPauseRun()) {/******其他程式正在執行中,稍後再跑****************************/
					Thread.sleep(getSleepTime()*1000);
					continue;
					
				}
				if (checkOnBat())
					nG_Continue=1;
				else
					nG_Continue=0;

				if (nG_Continue==1) {
					if (!updateSysParm3("1"))
						break;
					ResultSet L_OnBatRs11 = getOnBatData();
					
					processOnBatData(L_OnBatRs11);
				}
				String sL_SysData4Value="0";
				if (!updateSysParm3(sL_SysData4Value))
					break;

				if (nG_CounterOk>=0) {
					int nL_SrcProcessStatus=1, nL_NewProcessStatus=2;
					Timestamp L_Dop = HpeUtil.getCurTimestamp();
					String sL_ProcDate = HpeUtil.getCurDateStr("");
					String sL_TransType="11";
					int nL_ToWhich=2;
					String sL_CardNo = "0000000000000000";
					
					OnBatDao.updateOnBat9(nL_SrcProcessStatus, nL_NewProcessStatus, L_Dop,  sL_ProcDate, sL_TransType,  nL_ToWhich, sL_CardNo);
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
		    closeDb();
		}
	}
	

	public int startProcess(Data060Vo  P_Data060Vo ) {
		int nL_Result = 0;
		bG_IgnoreProc = false;
		bG_AddUnMatch=false;
		boolean bL_CcasMatchResult = true;
		// 0=> 正常 match 到 data
		// 1=> 無法 match 到 data
		// -1 => error or exception
		
		String sL_OnBatCardNo="",sL_OnBatTxDate="", sL_OnBatCardAcctId=""; 
		try {

			
			
			//L_OnBatRowId = P_OnBatRs11.getRowId("OnBatRowId");
			/*
			sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
			*/
			//System.out.println("BB1");
			sL_OnBatCardNo = P_Data060Vo.getCardNo();
			//System.out.println("BB2");
			sL_OnBatTxDate = P_Data060Vo.getTransDate();
			//System.out.println("AA3");
			sL_OnBatCardAcctId = P_Data060Vo.getCardAcctId();
			//System.out.println("AA4");
			//sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
			/*
			nL_OnBatTxAmt = P_OnBatRs11.getInt("OnBatTransAmt");
			sL_OnBatAuthCode = P_OnBatRs11.getString("OnBatAuthNo");
			sL_OnBatTxType = P_OnBatRs11.getString("OnBatTransCode");
			sL_OnBatRefeNo = P_OnBatRs11.getString("OnBatRefeNo");
			sL_OnBatMccCode = P_OnBatRs11.getString("OnBatMccCode");
			*/
			//udbdba02, yycc5566
			nG_CardAcctIdx=0;
			sG_CardAcctId = "0000000000";
				
			//System.out.println("AA5===>OnBatCardNo=>" + sL_OnBatCardNo + "===");
			CcaCardBaseVo L_CcaCardBaseVo = CcaCardBaseDao.getCcaCardBase(sL_OnBatCardNo);
			//System.out.println("AA6");
			nG_CardAcctIdx= L_CcaCardBaseVo.getCardAcctIdx();
			//System.out.println("AA7");
			sG_CardAcctId= sL_OnBatCardAcctId;
				
			/*
			if (!startSyncWork())
				return false;
			*/
			System.out.println("Before MatchStep1 ...");
			bL_CcasMatchResult = ccasMatchStep1(P_Data060Vo);

			//System.out.println("BB8");
				
			if (bG_IgnoreProc) {
				System.out.println("bG_IgnoreProc == true....");
			}
			else {
				//System.out.println("BB9");
				System.out.println("Before MatchStep2 ...");
				bL_CcasMatchResult = ccasMatchStep2(P_Data060Vo);
				//System.out.println("AA10");							
				if (!bG_IgnoreProc) {
					System.out.println("Before MatchStep3 ...");
					bL_CcasMatchResult = ccasMatchStep3(P_Data060Vo);
				}
				//System.out.println("AA11");
				
			}
					
			if (bG_AddUnMatch) {
				//System.out.println("AA12");
				bL_CcasMatchResult =false;
				addUnMatch(P_Data060Vo);
				//System.out.println("AA13");
				
			}
			else {
				System.out.println("Add un match == false...");
			}
				
			//commitDb();//Howard: marked on 2019/11/11
			
			
			if (bL_CcasMatchResult)
				nL_Result = 0;
			else
				nL_Result = 1;
			
			//System.out.println("AA14");
			
		} catch (Exception e) {
			// TODO: handle exception
			//rollbackDb(); Howard: marked on 2019/11/11
			
			bL_CcasMatchResult = false;
			//System.out.println("AA15");
			//System.out.println("AuthBatch_060.startProcess exception=>" + e.getMessage() + "--");
			nL_Result = -1;
		}
		return nL_Result;
	}
	
	private boolean processOnBatData(ResultSet P_OnBatRs11) {
		boolean bL_Result = true;
		
		try {
			RowId L_OnBatRowId=null;
			String  sL_OnBatCardNo="",sL_OnBatTxDate="";
			int nL_OnBatTxAmt=0;
			String sL_OnBatAuthCode="", sL_OnBatTxType="", sL_OnBatRefeNo="", sL_OnBatMccCode="";
			while (P_OnBatRs11.next()) {
				/*帳單比對作業*/
				//proc is CCAS_BILL_MATCH()
				
				L_OnBatRowId = P_OnBatRs11.getRowId("OnBatRowId");
				/*
				sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
				*/
				sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
				/*
				nL_OnBatTxAmt = P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthCode = P_OnBatRs11.getString("OnBatAuthNo");
				sL_OnBatTxType = P_OnBatRs11.getString("OnBatTransCode");
				sL_OnBatRefeNo = P_OnBatRs11.getString("OnBatRefeNo");
				sL_OnBatMccCode = P_OnBatRs11.getString("OnBatMccCode");
				*/
				if (Integer.parseInt(sL_OnBatTxType)>50) {
					sG_Bit127RecData = "退貨, 不予處理";
					sG_MessageHead5 = "5";
					addUnMatch(P_OnBatRs11);
					nG_SkipCount++;
				}
				else {
					nG_CardAcctIdx=0;
					sG_CardAcctId = "0000000000";
					
					CcaCardBaseVo L_CcaCardBaseVo = CcaCardBaseDao.getCcaCardBase(P_OnBatRs11.getString("OnBatCardNo"));
					nG_CardAcctIdx= L_CcaCardBaseVo.getCardAcctIdx();
					sG_CardAcctId=P_OnBatRs11.getString("OnBatCardAcctId");
					
					if (!startSyncWork())
						return false;
					int nL_CcasMatch1Result = ccasMatchStep1(P_OnBatRs11);
					
					if (nL_CcasMatch1Result==0) {
						int nL_CcasMatch2Result = ccasMatchStep2(P_OnBatRs11);
						
						if (nL_CcasMatch2Result==0) {
							int nL_CcasMatch3Result = ccasMatchStep3(P_OnBatRs11);
						}
					}
						
					
					
				}
				
				
				int nL_ProcessStatus=2;
				Timestamp L_Dop = HpeUtil.getCurTimestamp();
				String sL_ProcDate = HpeUtil.getCurDateStr("");
				String sL_MatchFlag= sG_DbMatchFlag;
				bL_Result = OnBatDao.updateOnBat9(nL_ProcessStatus, L_Dop, sL_ProcDate, sL_MatchFlag, L_OnBatRowId);
				if (!bL_Result) {
					rollbackDb();
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
	
	public void initProg(Connection P_Db2Conn) throws Exception{
		setDbConn(P_Db2Conn);
		initialPrepareStatement(G_ECS060ID);
		getAuthParm();
		
	}
	private void getAuthParm() throws Exception {
		//proc is getAuthParm()
		
		String sL_SysId="REPORT", sL_SysKey="AMT1";
		int  nL_TmpRate=100;
		double dL_LParm=0, dL_UParm=0;
		
		nG_DbCardLimit = 20000;
		//ResultSet L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1(sL_SysId, sL_SysKey,"0");
		ResultSet L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1(sL_SysId, sL_SysKey);

		if (null != L_SysParm1Rs) {
			while (L_SysParm1Rs.next()) {
				nG_DbCardLimit = L_SysParm1Rs.getInt("SysData1");
				if (nG_DbCardLimit<0)
					nG_DbCardLimit=0;
			}
		
			/*
			if (CcaSysParm1Dao.isEmptyResultSet(L_SysParm1Rs))
				nG_DbCardLimit = 20000;
			else  {
				nG_DbCardLimit = L_SysParm1Rs.getInt("SysData1");
				if (nG_DbCardLimit<0)
					nG_DbCardLimit=0;
			}
			 */
			L_SysParm1Rs.close();
		}
		
		sL_SysKey="L_LIMIT";
		dL_LParm=100;
		
		L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1(sL_SysId, sL_SysKey);
		if (null != L_SysParm1Rs) {
			while (L_SysParm1Rs.next()) {
				dL_LParm = L_SysParm1Rs.getInt("SysData1");
				if (dL_LParm<0)
					dL_LParm = 0;
			
			}
			/*
			if (CcaSysParm1Dao.isEmptyResultSet(L_SysParm1Rs))
				dL_LParm=100;
			else  {
				dL_LParm = L_SysParm1Rs.getInt("SysData1");
				if (dL_LParm<0)
					dL_LParm = 0;
			}
		    */
			L_SysParm1Rs.close();
		}
		
		
		sL_SysKey="U_LIMIT";
		dL_UParm=100;
		L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1(sL_SysId, sL_SysKey);
		
		if (null != L_SysParm1Rs) {
			while (L_SysParm1Rs.next()) {
				dL_UParm = L_SysParm1Rs.getDouble("SysData1");
				if (dL_UParm<0)
					dL_UParm=0;
			}
			/*
			if (CcaSysParm1Dao.isEmptyResultSet(L_SysParm1Rs))
				dL_UParm=100;
			else {
				dL_UParm = L_SysParm1Rs.getDouble("SysData1");
				if (dL_UParm<0)
					dL_UParm=0;
			}
			*/
			L_SysParm1Rs.close();
		}
		
		
		dG_LParm=0;
		if (dL_LParm!=0)
			dG_LParm = dL_LParm/100;

		dG_UParm=0;
		if (dL_UParm!=0)
			dG_UParm = dL_UParm/100;
		

		
		sL_SysKey="RATE1";
		nL_TmpRate=100;
		L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1(sL_SysId, sL_SysKey);
		
		if (null != L_SysParm1Rs) {
			while (L_SysParm1Rs.next()) {
				nL_TmpRate = L_SysParm1Rs.getInt("SysData1");
				if (nL_TmpRate<0)
					nL_TmpRate=0;
			}
			/*
			if (CcaSysParm1Dao.isEmptyResultSet(L_SysParm1Rs))
				nL_TmpRate=100;
			else {
				nL_TmpRate = L_SysParm1Rs.getInt("SysData1");
				if (nL_TmpRate<0)
					nL_TmpRate=0;
			}
			*/
			L_SysParm1Rs.close();
		}
		
		dG_URate1= (double)(1+ (float)nL_TmpRate/100);
		dG_LRate1= (double)(1- (float)nL_TmpRate/100); 
		
		
		
		
	}
	
	private int ccasMatchStep2(ResultSet P_OnBatRs11) throws Exception{
		//proc is CCAS_match_step2()
		/*卡/日/授/金 or 卡/授/金上下*/
		int nL_Result = 0;
		try {
			String sL_BilRefNo = P_OnBatRs11.getString("OnBatRefeNo");
			ResultSet L_AuthTxLogRs31 = chkAuthTxLog(31, P_OnBatRs11);
			if (null !=L_AuthTxLogRs31) {
				updateCardAcct(L_AuthTxLogRs31, P_OnBatRs11);
				
				
				updateAuthTxLog(L_AuthTxLogRs31, sL_BilRefNo);
	            
	            
				nG_L2Count++;
				
				return 1;
			}
			else {
				ResultSet L_AuthTxLogRs32 = chkAuthTxLog(32, P_OnBatRs11);
				if (null !=L_AuthTxLogRs32) {
					updateCardAcct(L_AuthTxLogRs32, P_OnBatRs11);
					
					
					updateAuthTxLog(L_AuthTxLogRs32, sL_BilRefNo);
		            
		            
					nG_L2Count++;
					
					return 1;
					
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			nL_Result=0;
		}
		
		return nL_Result;
	}
	private boolean ccasMatchStep2(Data060Vo P_Data060Vo) throws Exception{
		//proc is CCAS_match_step2()
		/*卡/日/授/金 or 卡/授/金上下*/
		boolean bL_Result = false;
		try {
			String sL_BilRefNo = P_Data060Vo.getRefeNo();
			//String sL_BilRefNo = P_OnBatRs11.getString("OnBatRefeNo");
			boolean bL_HasData=false;
			
			ResultSet L_AuthTxLogRs31 = chkAuthTxLog(31, P_Data060Vo);
			while (L_AuthTxLogRs31.next()) {
				bL_HasData=true;
				System.out.println("call updateCardAcct()...");
				updateCardAcct(L_AuthTxLogRs31);
				System.out.println("begin call updateAuthTxLog()...");
				updateAuthTxLog(L_AuthTxLogRs31, sL_BilRefNo);
				System.out.println("end call updateAuthTxLog()...");
	            
	            bG_IgnoreProc = true;
	            bG_AddUnMatch = false;
				nG_L2Count++;
				
				break;
			}
			L_AuthTxLogRs31.close();
			
			if (!bL_HasData) {
				//abcd
				ResultSet L_AuthTxLogRs32 = chkAuthTxLog(32,P_Data060Vo);
				
				while (L_AuthTxLogRs32.next()) {
					updateCardAcct(L_AuthTxLogRs32);
					updateAuthTxLog(L_AuthTxLogRs32, sL_BilRefNo);
		            
					bG_IgnoreProc = true;
		            bG_AddUnMatch = false;
					nG_L2Count++;
					bL_HasData=true;
				}
				L_AuthTxLogRs32.close();
				
				/*
				if (null !=L_AuthTxLogRs32) {
					updateCardAcct(L_AuthTxLogRs32);
					updateAuthTxLog(L_AuthTxLogRs32);
		            
		            
					nG_L2Count++;
					
					return true;
					
				}
				else
					bL_Result=false;
				*/
			}
			bL_Result = bL_HasData;
			/*
			if (null !=L_AuthTxLogRs31) {
				updateCardAcct(L_AuthTxLogRs31);
				updateAuthTxLog(L_AuthTxLogRs31);
	            
	            
				nG_L2Count++;
				
				return true;
			}
			else {
				ResultSet L_AuthTxLogRs32 = chkAuthTxLog(32,P_Data060Vo);
				if (null !=L_AuthTxLogRs32) {
					updateCardAcct(L_AuthTxLogRs32);
					updateAuthTxLog(L_AuthTxLogRs32);
		            
		            
					nG_L2Count++;
					
					return true;
					
				}
				else
					bL_Result=false;
			}
			*/
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result=false;
			throw e;
		}
		
		return bL_Result;
	}

	private int ccasMatchStep3(ResultSet P_OnBatRs11) throws Exception{
		//proc is CCAS_match_step3()
		/*卡/金上下*/
		
		
		
		
		
		
		
		int nL_Result = 0;
		try {
			String sL_BilRefNo = P_OnBatRs11.getString("OnBatRefeNo");
			ResultSet L_AuthTxLogRs41 = chkAuthTxLog(41, P_OnBatRs11);
			if (null !=L_AuthTxLogRs41) {/*** 先比對金額相同的 **/
				updateCardAcct(L_AuthTxLogRs41, P_OnBatRs11);
				updateAuthTxLog(L_AuthTxLogRs41, sL_BilRefNo);
	            
	            
				nG_L3Count++;
				
				return 1;
			}
			else {/*** 再比對金額上下限 **/
				ResultSet L_AuthTxLogRs42 = chkAuthTxLog(42, P_OnBatRs11);
				if (null !=L_AuthTxLogRs42) {
					updateCardAcct(L_AuthTxLogRs42, P_OnBatRs11);
					updateAuthTxLog(L_AuthTxLogRs42, sL_BilRefNo);
		            
		            
					nG_L3Count++;
					
					return 1;
					
				}
				else {
					sG_Bit127RecData = "CCAS無交易記錄";
					sG_MessageHead5 = "6";
					addUnMatch(P_OnBatRs11);
					nG_SkipCount++;
					return 1;

				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			nL_Result=0;
		}
		
		return nL_Result;
	}

	private boolean ccasMatchStep3(Data060Vo P_Data060Vo) throws Exception{
		//proc is CCAS_match_step3()
		/*卡/金上下*/
		boolean bL_HasData = false;
		
		try {
			String sL_BilRefNo = P_Data060Vo.getRefeNo();
			ResultSet L_AuthTxLogRs41 = chkAuthTxLog(41, P_Data060Vo);
			while (L_AuthTxLogRs41.next()) {
				updateCardAcct(L_AuthTxLogRs41);
				updateAuthTxLog(L_AuthTxLogRs41, sL_BilRefNo);
	            
				bG_AddUnMatch=false;
				bG_IgnoreProc=true;
	            
				nG_L3Count++;
				bL_HasData = true;
				
			}
			L_AuthTxLogRs41.close();
			
			if (!bL_HasData) {
				/*** 再比對金額上下限 **/
				ResultSet L_AuthTxLogRs42 = chkAuthTxLog(42, P_Data060Vo);
				while (L_AuthTxLogRs42.next()) {
					updateCardAcct(L_AuthTxLogRs42);
					updateAuthTxLog(L_AuthTxLogRs42, sL_BilRefNo);
		            
					bL_HasData = true;
					bG_AddUnMatch=false;
					bG_IgnoreProc=true;
					nG_L3Count++;
					
				}
				L_AuthTxLogRs42.close();
			
				if (!bL_HasData) {
					sG_Bit127RecData = "CCAS無交易記錄";
					sG_MessageHead5 = "6";
					//addUnMatch(P_Data060Vo);
					bG_AddUnMatch=true;
					bG_IgnoreProc=true;
					nG_SkipCount++;
					bL_HasData =  false;
					
				}

			}
			

						
		} catch (Exception e) {
			// TODO: handle exception
			bL_HasData=false;
			throw e;
		}
		
		return bL_HasData;
	}

	private boolean ccasMatchStep3_Old(Data060Vo P_Data060Vo) throws Exception{
		//proc is CCAS_match_step3()
		/*卡/金上下*/
		
		
		
		
		
		
		
		boolean bL_Result = false;
		try {
			String sL_BilRefNo = P_Data060Vo.getRefeNo();
			ResultSet L_AuthTxLogRs41 = chkAuthTxLog(41, P_Data060Vo);
			if (null !=L_AuthTxLogRs41) {/*** 先比對金額相同的 **/
				updateCardAcct(L_AuthTxLogRs41);
				updateAuthTxLog(L_AuthTxLogRs41, sL_BilRefNo);
	            
	            
				nG_L3Count++;
				
				return true;
			}
			else {/*** 再比對金額上下限 **/
				ResultSet L_AuthTxLogRs42 = chkAuthTxLog(42, P_Data060Vo);
				if (null !=L_AuthTxLogRs42) {
					updateCardAcct(L_AuthTxLogRs42);
					updateAuthTxLog(L_AuthTxLogRs42, sL_BilRefNo);
		            
		            
					nG_L3Count++;
					
					return true;
					
				}
				else {
					sG_Bit127RecData = "CCAS無交易記錄";
					sG_MessageHead5 = "6";
					//addUnMatch(P_OnBatRs11);
					nG_SkipCount++;
					return false;

				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result=false;
		}
		
		return bL_Result;
	}

	private boolean ccasMatchStep1(Data060Vo  P_Data060Vo) throws Exception{
		//proc is CCAS_match_step1()
		//ECS bill match record process
		
		boolean bL_Result = true;
		
		try {
			String sL_BilRefNo = P_Data060Vo.getRefeNo();
			int nL_OnBatTxAmt=0;
			String sL_OnBatAuthCode = P_Data060Vo.getAuthNo();
			System.out.println("sL_OnBatAuthCode=>" + sL_OnBatAuthCode + "===");
			if (("000001".equals(sL_OnBatAuthCode.substring(0,6))) ||
			   ("00000Y".equals(sL_OnBatAuthCode.substring(0,6))) ) {
				nL_OnBatTxAmt = P_Data060Vo.getTransAmt();
				if (nL_OnBatTxAmt>nG_DbCardLimit) {
					System.out.println("=====999999===" + nL_OnBatTxAmt + "-----");
					sG_Bit127RecData = "Cycle 1 且 Amount > " + nG_DbCardLimit + ", 不予處理";
					sG_MessageHead5 = "1";
					
					bG_IgnoreProc = true;
					bG_AddUnMatch = true;
					nG_SkipCount++;
					return false;
				}
				boolean bL_HasData = false;
				ResultSet L_AuthTxLogRs = chkAuthTxLog(2, P_Data060Vo);
				while (L_AuthTxLogRs.next()) {
					//abcd
					bL_HasData = true;
					updateCardAcct(L_AuthTxLogRs);
					System.out.println("updateAuthTxLog on ccasMatchStep1()....");
					updateAuthTxLog(L_AuthTxLogRs, sL_BilRefNo);
		            
					bG_IgnoreProc = true;
					bG_AddUnMatch = false;
		            
					nG_L1Count++;
					System.out.println("=====AAAAAAA==="  + "-----");
					break;
				}
				L_AuthTxLogRs.close();

				if (!bL_HasData) {
					sG_Bit127RecData = "Cycle 1 但無交易記錄";
					sG_MessageHead5 = "2";

					bG_IgnoreProc = true;
					bG_AddUnMatch = true;

					nG_SkipCount++;
					return false;
				}
				
				return true;
				/*
				if (null !=L_AuthTxLogRs) {
					//有比對到授權交易
					//L_AuthTxLogRs.next();

					updateCardAcct(L_AuthTxLogRs);
					
					updateAuthTxLog(L_AuthTxLogRs);
		            
		            
					nG_L1Count++;
					return true;	
				}
				else {
					sG_Bit127RecData = "Cycle 1 但無交易記錄";
					sG_MessageHead5 = "2";
					//addUnMatch(P_OnBatRs11);
					nG_SkipCount++;
					return false;
				}
				*/
			}
			else {
				System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV===");
			}
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception on ccasMatchStep1()=>" + e.getMessage() + "===");
			bL_Result = false;
			throw e;
			
		}
		
		return bL_Result;
		
	}

	
	private int ccasMatchStep1(ResultSet P_OnBatRs11) throws Exception{
		//proc is CCAS_match_step1()
		//ECS bill match record process
		
		int nL_Result = 0;
		
		try {
			String sL_BilRefNo = P_OnBatRs11.getString("OnBatRefeNo");
			int nL_OnBatTxAmt=0;
			String sL_OnBatAuthCode = P_OnBatRs11.getString("OnBatAuthNo");
			if (("000001".equals(sL_OnBatAuthCode.substring(0,6))) ||
			   ("00000Y".equals(sL_OnBatAuthCode.substring(0,6))) ) {
				nL_OnBatTxAmt = P_OnBatRs11.getInt("OnBatTransAmt");
				if (nL_OnBatTxAmt>nG_DbCardLimit) {
					sG_Bit127RecData = "Cycle 1 且 Amount > " + nG_DbCardLimit + ", 不予處理";
					sG_MessageHead5 = "1";
					addUnMatch(P_OnBatRs11);
					nG_SkipCount++;
					return 1;
				}
				ResultSet L_AuthTxLogRs = chkAuthTxLog(2, P_OnBatRs11);
				if (null !=L_AuthTxLogRs) {
					//有比對到授權交易
					updateCardAcct(L_AuthTxLogRs, P_OnBatRs11);
					updateAuthTxLog(L_AuthTxLogRs, sL_BilRefNo);
		            
		            
					nG_L1Count++;
					return 1;	
				}
				else {
					sG_Bit127RecData = "Cycle 1 但無交易記錄";
					sG_MessageHead5 = "2";
					addUnMatch(P_OnBatRs11);
					nG_SkipCount++;
					return 1;
				}
				
			}
			
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			nL_Result = 1;
		}
		
		return nL_Result;
		
	}

	private boolean updateAuthTxLog(ResultSet P_AuthTxLogRs, String sP_BilRefNo) {
		//proc is updAuthTxLog();
		
		boolean bL_Result = true;
		
		
		
		
		try {
			//RowId L_AuthTxLogRowId = P_AuthTxLogRs.getRowId("AuthTxLogRowId");

			String sL_TxDate = P_AuthTxLogRs.getString("AuthTxLogTxDate");
			String sL_CardNo= P_AuthTxLogRs.getString("AuthTxLogCardNo");
			String sL_AuthNo= P_AuthTxLogRs.getString("AuthTxLogAuthNo");
			String sL_TraceNo= P_AuthTxLogRs.getString("AuthTxLogTraceNo");
			String sL_TxTime= P_AuthTxLogRs.getString("AuthTxLogTxTime");
			
			String sL_BilRefNo = sP_BilRefNo;
			String sL_CacuAmount="N", sL_CacuCash="N"; 
			String sL_MatchFlag= sG_DbMatchFlag;
			String sL_PgName="ECS060"; 
			
			//bL_Result = AuthTxLogDao.updateAuthTxLog(L_AuthTxLogRowId, sL_CacuAmount, sL_CacuCash, sL_MatchFlag, sL_PgName);
			bL_Result = AuthTxLogDao.updateAuthTxLog(sL_TxDate, sL_CardNo, sL_AuthNo,  sL_TraceNo,  sL_TxTime, sL_CacuAmount, sL_CacuCash, sL_MatchFlag, sL_PgName, sL_BilRefNo);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception on updateAuthTxLog() =>" + e.getMessage());
			bL_Result = false;
		}
		
		return bL_Result;
	}
	private boolean updateCardAcct(ResultSet P_AuthTxLogRs, ResultSet P_OnBatRs11) {
		//proc is updCardAcct()
		boolean bL_Result = true;
		CardAcctVo L_CardAcctVo = null;
		try {
			L_CardAcctVo = CardAcctDao.getCardAcct(nG_CardAcctIdx);
			
			if (L_CardAcctVo==null) {
				sG_Bit127RecData = "(" + nG_CardAcctIdx + ")-[" + sG_CardAcctId + "]卡戶檔找不到";
				sG_MessageHead5="3";
				addUnMatch(P_OnBatRs11);
				return false;
			}
			
			int nL_AuthTxLogNtAmt = P_AuthTxLogRs.getInt("AuthTxLogNtAmt");
			nG_CaTotAmtConsume = L_CardAcctVo.getTotAmtConsume() - nL_AuthTxLogNtAmt;
			
			nG_CaTotAmtPreCash = L_CardAcctVo.getTotAmtPreCash();			
			if ("Y".equals(P_AuthTxLogRs.getString("AuthTxLogCacuCash"))) {
				String sL_CacuCash="Y";
				int nL_SumNtAmt = AuthTxLogDao.summaryNtAmt(nG_CardAcctIdx, sL_CacuCash);
				nG_CaTotAmtPreCash = nL_SumNtAmt - nL_AuthTxLogNtAmt;
			}
			
			if (nG_CaTotAmtConsume<0)
				nG_CaTotAmtConsume=0;
			
			if (nG_CaTotAmtPreCash<0)
				nG_CaTotAmtPreCash=0;
			
			
			if (!CardAcctDao.updateCardAcct(nG_CardAcctIdx, "ECS060", nG_CaTotAmtConsume, nG_CaTotAmtPreCash)) {
				sG_Bit127RecData = "(" + nG_CardAcctIdx + ")-[" + sG_CardAcctId + "]更新卡戶檔失敗";
				sG_MessageHead5="4";
				addUnMatch(P_OnBatRs11);
				return false;
			}
			
			sG_DbMatchFlag = "Y";
		    
			int nL_AcctParentIndex = CardAcctIndexDao.getAcctParentIndex(nG_CardAcctIdx);
			
			if (nL_AcctParentIndex==0)
				return true;
			
			//down, process parent data
			CardAcctVo L_ParentCardAcctVo = CardAcctDao.getCardAcct(nL_AcctParentIndex);
			
			if (L_ParentCardAcctVo==null) {
				return true;
			}
			nG_CaTotAmtConsume = L_ParentCardAcctVo.getTotAmtConsume();
			nG_CaTotAmtConsume = nG_CaTotAmtConsume - nL_AuthTxLogNtAmt;
			
			nG_CaTotAmtPreCash = L_ParentCardAcctVo.getTotAmtPreCash();
			if ("Y".equals(P_AuthTxLogRs.getString("AuthTxLogCacuCash"))) {
				nG_CaTotAmtPreCash = nG_CaTotAmtPreCash - nL_AuthTxLogNtAmt;
			}
			
			if (nG_CaTotAmtConsume<0)
				nG_CaTotAmtConsume=0;
			
			if (nG_CaTotAmtPreCash<0)
				nG_CaTotAmtPreCash=0;

			
			if (!CardAcctDao.updateCardAcct(nL_AcctParentIndex, "ECS060", nG_CaTotAmtConsume, nG_CaTotAmtPreCash)) {
				return false;
			}

			//up, process parent data
			
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	private boolean updateCardAcct(ResultSet P_AuthTxLogRs) {
		//proc is updCardAcct()
		boolean bL_Result = true;
		CardAcctVo L_CardAcctVo = null;
		try {
			L_CardAcctVo = CardAcctDao.getCardAcct(nG_CardAcctIdx);
			
			if (L_CardAcctVo==null) {
				sG_Bit127RecData = "(" + nG_CardAcctIdx + ")-[" + sG_CardAcctId + "]卡戶檔找不到";
				sG_MessageHead5="3";
				//addUnMatch(P_OnBatRs11);
				return false;
			}
			
			int nL_AuthTxLogNtAmt = P_AuthTxLogRs.getInt("AuthTxLogNtAmt");
			
			
			nG_CaTotAmtConsume = L_CardAcctVo.getTotAmtConsume() - nL_AuthTxLogNtAmt;
			
			nG_CaTotAmtPreCash = L_CardAcctVo.getTotAmtPreCash();			
			if ("Y".equals(P_AuthTxLogRs.getString("AuthTxLogCacuCash"))) {
				String sL_CacuCash="Y";
				int nL_SumNtAmt = AuthTxLogDao.summaryNtAmt(nG_CardAcctIdx, sL_CacuCash);
				nG_CaTotAmtPreCash = nL_SumNtAmt - nL_AuthTxLogNtAmt;
			}
			
			if (nG_CaTotAmtConsume<0)
				nG_CaTotAmtConsume=0;
			
			if (nG_CaTotAmtPreCash<0)
				nG_CaTotAmtPreCash=0;
			
			
			if (!CardAcctDao.updateCardAcct(nG_CardAcctIdx, "ECS060", nG_CaTotAmtConsume, nG_CaTotAmtPreCash)) {
				sG_Bit127RecData = "(" + nG_CardAcctIdx + ")-[" + sG_CardAcctId + "]更新卡戶檔失敗";
				sG_MessageHead5="4";
				//addUnMatch(P_OnBatRs11);
				return false;
			}
			
			sG_DbMatchFlag = "Y";
		    
			int nL_AcctParentIndex = CardAcctIndexDao.getAcctParentIndex(nG_CardAcctIdx);
			
			//nL_AcctParentIndex=32132;//howard: for test...
			
			if (nL_AcctParentIndex==0)
				return true;
			
			//down, process parent data
			CardAcctVo L_ParentCardAcctVo = CardAcctDao.getCardAcct(nL_AcctParentIndex);
			
			if (L_ParentCardAcctVo==null) {
				return true;
			}
			nG_CaTotAmtConsume = L_ParentCardAcctVo.getTotAmtConsume();
			nG_CaTotAmtConsume = nG_CaTotAmtConsume - nL_AuthTxLogNtAmt;
			
			nG_CaTotAmtPreCash = L_ParentCardAcctVo.getTotAmtPreCash();
			if ("Y".equals(P_AuthTxLogRs.getString("AuthTxLogCacuCash"))) {
				nG_CaTotAmtPreCash = nG_CaTotAmtPreCash - nL_AuthTxLogNtAmt;
			}
			
			if (nG_CaTotAmtConsume<0)
				nG_CaTotAmtConsume=0;
			
			if (nG_CaTotAmtPreCash<0)
				nG_CaTotAmtPreCash=0;

			
			if (!CardAcctDao.updateCardAcct(nL_AcctParentIndex, "ECS060", nG_CaTotAmtConsume, nG_CaTotAmtPreCash)) {
				return false;
			}

			//up, process parent data
			
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	private ResultSet chkAuthTxLog(int nP_Type, ResultSet P_OnBatRs11) {
		//proc is chkAuthTxLog
		//Howard: 比對交易紀錄
		ResultSet L_AuthTxLogRs = null;
		String sL_OnBatCardNo="", sL_OnBatTxDate="", sL_CacuAmount="", sL_OnBatAuthNo="";
		int nL_CardLimit=0, nL_OnBatTransAmt=0;
		
		try {
			if (nP_Type==2) {/*AuthNo=000001 or 00000Y and amount <= 20000*/
				sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_CardLimit = nG_DbCardLimit;
				
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_OnBatTxDate, sL_CacuAmount, nL_CardLimit);
			}
			else if (nP_Type==31) { /*卡/日/金/授*/
				sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_OnBatRs11.getString("OnBatAuthNo");
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_OnBatTxDate, sL_CacuAmount, nL_OnBatTransAmt, sL_OnBatAuthNo);

			}
			else if (nP_Type==32) { /*(卡/授/金上下)*/
				sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_OnBatRs11.getString("OnBatAuthNo");
				
				dG_UBound = nL_OnBatTransAmt *dG_UParm;
				dG_LBound = nL_OnBatTransAmt *dG_LParm;
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_CacuAmount, sL_OnBatAuthNo, dG_UBound, dG_LBound);  
			}
			else if (nP_Type==41) { /*卡/金*/
				sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_OnBatRs11.getString("OnBatAuthNo");
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo,  sL_CacuAmount, nL_OnBatTransAmt);

			}
			else if (nP_Type==42) { /*卡/金上下*/ 
				sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_OnBatRs11.getString("OnBatAuthNo");
				
				
				dG_UBound = nL_OnBatTransAmt *dG_URate1;
				dG_LBound = nL_OnBatTransAmt *dG_LRate1;
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_CacuAmount, dG_UBound, dG_LBound);  
			}

		} catch (Exception e) {
			// TODO: handle exception
			L_AuthTxLogRs = null;
		}
		
		return L_AuthTxLogRs;
	}
	
	private ResultSet chkAuthTxLog(int nP_Type, Data060Vo P_Data060Vo) {
		//proc is chkAuthTxLog
		//Howard: 比對交易紀錄
		ResultSet L_AuthTxLogRs = null;
		String sL_OnBatCardNo="", sL_OnBatTxDate="", sL_CacuAmount="", sL_OnBatAuthNo="";
		int nL_CardLimit=0, nL_OnBatTransAmt=0;
		
		try {
			if (nP_Type==2) {/*AuthNo=000001 or 00000Y and amount <= 20000*/
				sL_OnBatCardNo = P_Data060Vo.getCardNo();// P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_Data060Vo.getTransDate();// P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_CardLimit = nG_DbCardLimit;
				
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_OnBatTxDate, sL_CacuAmount, nL_CardLimit);
			}
			else if (nP_Type==31) { /*卡/日/金/授*/
				sL_OnBatCardNo = P_Data060Vo.getCardNo();// P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_Data060Vo.getTransDate();// P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_Data060Vo.getTransAmt();// P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_Data060Vo.getAuthNo();// P_OnBatRs11.getString("OnBatAuthNo");
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_OnBatTxDate, sL_CacuAmount, nL_OnBatTransAmt, sL_OnBatAuthNo);

			}
			else if (nP_Type==32) { /*(卡/授/金上下)*/
				sL_OnBatCardNo = P_Data060Vo.getCardNo();// P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_Data060Vo.getTransDate();// P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_Data060Vo.getTransAmt();// P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_Data060Vo.getAuthNo();// P_OnBatRs11.getString("OnBatAuthNo");
				
				dG_UBound = nL_OnBatTransAmt *dG_UParm;
				dG_LBound = nL_OnBatTransAmt *dG_LParm;
				//abcd
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_CacuAmount, sL_OnBatAuthNo, dG_UBound, dG_LBound);  
			}
			else if (nP_Type==41) { /*卡/金*/
				sL_OnBatCardNo = P_Data060Vo.getCardNo();// P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_Data060Vo.getTransDate();// P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_Data060Vo.getTransAmt();// P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_Data060Vo.getAuthNo();// P_OnBatRs11.getString("OnBatAuthNo");
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo,  sL_CacuAmount, nL_OnBatTransAmt);

			}
			else if (nP_Type==42) { /*卡/金上下*/ 
				sL_OnBatCardNo = P_Data060Vo.getCardNo();// P_OnBatRs11.getString("OnBatCardNo");
				sL_OnBatTxDate = P_Data060Vo.getTransDate();// P_OnBatRs11.getString("OnBatTransDate");
				sL_CacuAmount="Y";
				nL_OnBatTransAmt = P_Data060Vo.getTransAmt();// P_OnBatRs11.getInt("OnBatTransAmt");
				sL_OnBatAuthNo = P_Data060Vo.getAuthNo();// P_OnBatRs11.getString("OnBatAuthNo");

			
				
				dG_UBound = nL_OnBatTransAmt *dG_URate1;
				dG_LBound = nL_OnBatTransAmt *dG_LRate1;
				L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_CacuAmount, dG_UBound, dG_LBound);  
			}

		} catch (Exception e) {
			// TODO: handle exception
			L_AuthTxLogRs = null;
		}
		
		return L_AuthTxLogRs;
	}

	private boolean startSyncWork() {
		//proc is start_sync_work()
		//檢查是否有同一ID之交易*
		boolean bL_Result = true;
		
		try {
			if (("0000000000".equals(sG_CardAcctId)) || (nG_CardAcctIdx==0)) {
				//card acct id is invlid
				return true;
			}
			
			//Howard: 不改寫此function
			
		} catch (Exception e) {
			// TODO: handle exception
			//bL_Result = false;
		}
		
		return bL_Result;
				
	}
	
	private void addUnMatch(Data060Vo  P_Data060Vo) throws Exception{
		//proc is addUnMatch()
		try {
			String sL_CacuAmount="Y", sL_MessageHead6="*", sL_MchtNo="*", sL_ProcCode="******";
			String sL_OnBatCardNo = P_Data060Vo.getCardNo();// P_OnBatRs11.getString("OnBatCardNo");
			String sL_OnBatAuthCode = P_Data060Vo.getAuthNo();// P_OnBatRs11.getString("OnBatAuthNo");
			// ResultSet L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_CacuAmount, sL_OnBatAuthCode);
			
			//if (!AuthTxLogDao.isEmptyResultSet(L_AuthTxLogRs)) {
			//while(L_AuthTxLogRs.next()) {
			if (true) {
				String sL_OnBatRowId="", sL_OnBatTxDate="";
				int nL_OnBatTxAmt=0;
				String sL_OnBatTxType="", sL_OnBatRefeNo="", sL_OnBatMccCode="";
				
				

				sL_OnBatTxDate = P_Data060Vo.getTransDate();// P_OnBatRs11.getString("OnBatTransDate");
				nL_OnBatTxAmt = P_Data060Vo.getTransAmt();// P_OnBatRs11.getInt("OnBatTransAmt");

				sL_OnBatTxType = P_Data060Vo.getTransCode();// P_OnBatRs11.getString("OnBatTransCode");
				sL_OnBatRefeNo = P_Data060Vo.getRefeNo();// P_OnBatRs11.getString("OnBatRefeNo");
				sL_OnBatMccCode = P_Data060Vo.getMccCode();// P_OnBatRs11.getString("OnBatMccCode");
				
				
				CcaUnmatchVo L_CcaUnmatchVo = new CcaUnmatchVo();
			
				L_CcaUnmatchVo.setAmtNt(nL_OnBatTxAmt);
				
				//L_CcaUnmatchVo.setAuthAmt(L_AuthTxLogRs.getInt("AuthTxLogNtAmt"));
				L_CcaUnmatchVo.setAuthAmt(nL_OnBatTxAmt);
				
				//L_CcaUnmatchVo.setAuthDate(L_AuthTxLogRs.getString("AuthTxLogTxDate"));
				L_CcaUnmatchVo.setAuthDate(sL_OnBatTxDate);
				
				L_CcaUnmatchVo.setAuthNo(sL_OnBatAuthCode);
				L_CcaUnmatchVo.setBit127RecData(sG_Bit127RecData);
				L_CcaUnmatchVo.setCardNo(sL_OnBatCardNo);
				L_CcaUnmatchVo.setMccCode(sL_OnBatMccCode);
				L_CcaUnmatchVo.setMchtNo(sL_MchtNo);
				L_CcaUnmatchVo.setMessageHead5(sG_MessageHead5);
				L_CcaUnmatchVo.setMessageHead6(sL_MessageHead6);
				L_CcaUnmatchVo.setProcCode(sL_ProcCode);
				L_CcaUnmatchVo.setRefNo(sL_OnBatRefeNo);
				L_CcaUnmatchVo.setTransType(sL_OnBatTxType);
				L_CcaUnmatchVo.setTxDate(sL_OnBatTxDate);
				L_CcaUnmatchVo.setUDate(sG_CurDate);
				L_CcaUnmatchVo.setUTime(sG_CurTime);
				CcaUnmatchDao.insertUnmatchRec(L_CcaUnmatchVo);
			
			}
			//L_AuthTxLogRs.close();
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("addUnMatch error=>" + e.getMessage());
			throw e;
		}
	}

	
	private void addUnMatch(ResultSet P_OnBatRs11) {
		//proc is addUnMatch()
		try {
			String sL_CacuAmount="Y", sL_MessageHead6="*", sL_MchtNo="*", sL_ProcCode="******";
			String sL_OnBatCardNo = P_OnBatRs11.getString("OnBatCardNo");
			String sL_OnBatAuthCode = P_OnBatRs11.getString("OnBatAuthNo");
			ResultSet L_AuthTxLogRs = AuthTxLogDao.getAuthTxLog(sL_OnBatCardNo, sL_CacuAmount, sL_OnBatAuthCode);
			
			//if (!AuthTxLogDao.isEmptyResultSet(L_AuthTxLogRs)) {
			while(L_AuthTxLogRs.next()) {
				String sL_OnBatRowId="", sL_OnBatTxDate="";
				int nL_OnBatTxAmt=0;
				String sL_OnBatTxType="", sL_OnBatRefeNo="", sL_OnBatMccCode="";
				
				sL_OnBatRowId = P_OnBatRs11.getString("OnBatRowId");

				sL_OnBatTxDate = P_OnBatRs11.getString("OnBatTransDate");
				nL_OnBatTxAmt = P_OnBatRs11.getInt("OnBatTransAmt");

				sL_OnBatTxType = P_OnBatRs11.getString("OnBatTransCode");
				sL_OnBatRefeNo = P_OnBatRs11.getString("OnBatRefeNo");
				sL_OnBatMccCode = P_OnBatRs11.getString("OnBatMccCode");
				
				
				CcaUnmatchVo L_CcaUnmatchVo = new CcaUnmatchVo();
			
				L_CcaUnmatchVo.setAmtNt(nL_OnBatTxAmt);
				L_CcaUnmatchVo.setAuthAmt(L_AuthTxLogRs.getInt("AuthTxLogNtAmt"));
				L_CcaUnmatchVo.setAuthDate(L_AuthTxLogRs.getString("AuthTxLogTxDate"));
				L_CcaUnmatchVo.setAuthNo(sL_OnBatAuthCode);
				L_CcaUnmatchVo.setBit127RecData(sG_Bit127RecData);
				L_CcaUnmatchVo.setCardNo(sL_OnBatCardNo);
				L_CcaUnmatchVo.setMccCode(sL_OnBatMccCode);
				L_CcaUnmatchVo.setMchtNo(sL_MchtNo);
				L_CcaUnmatchVo.setMessageHead5(sG_MessageHead5);
				L_CcaUnmatchVo.setMessageHead6(sL_MessageHead6);
				L_CcaUnmatchVo.setProcCode(sL_ProcCode);
				L_CcaUnmatchVo.setRefNo(sL_OnBatRefeNo);
				L_CcaUnmatchVo.setTransType(sL_OnBatTxType);
				L_CcaUnmatchVo.setTxDate(sL_OnBatTxDate);
				L_CcaUnmatchVo.setUDate(sG_CurDate);
				L_CcaUnmatchVo.setUTime(sG_CurTime);
				CcaUnmatchDao.insertUnmatchRec(L_CcaUnmatchVo);
			}
			L_AuthTxLogRs.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private ResultSet getOnBatData() {
		ResultSet L_OnBatRs = null;
		try {
			String sL_TransType="11", sL_OnBatTableName="ONBAT_2CCAS";
			String sL_SelectFields="ROWID as OnBatRowId,NVL(CARD_NO,' ') as OnBatCardNo,NVL(TRANS_DATE,'00000000') as OnBatTransDate,"
										+ "NVL(TRANS_AMT,0) as OnBatTransAmt,NVL(AUTH_NO,'000000') as OnBatAuthNo, CARD_ACCT_ID as OnBatCardAcctId, "
										+"NVL(TRANS_CODE,'01') as OnBatTransCode,NVL(REFE_NO,'0000000000') as OnBatRefeNo, DEBIT_FLAG as OnBatDebitFlag, " 
										+"MCC_CODE as OnBatMccCode";
			int nL_ToWhich=2, nL_ProcStatus=0;
			L_OnBatRs = OnBatDao.getOnBat2(sL_TransType, nL_ToWhich, nL_ProcStatus, sL_OnBatTableName, sL_SelectFields);
			
			nG_Continue=0;
			
			while (L_OnBatRs.next()) {
				nG_Continue=1;
				nG_CounterOk=1;
			}
			/*
			if (!OnBatDao.isEmptyResultSet(L_OnBatRs)) {
				nG_Continue=1;
				nG_CounterOk=1;
			}
			else {
				nG_Continue=0;
			}
			*/	
			L_OnBatRs.close();
		} catch (Exception e) {
			// TODO: handle exception
			L_OnBatRs = null;
		}
		
		return L_OnBatRs;
	}
	private boolean updateSysParm3(String sP_SysData4Value) {
		boolean bL_Result = CcaSysParm3Dao.updateSysParm3("ECSBILL", "SLEEP", sP_SysData4Value);
		
		
		return bL_Result;
	}
	private boolean checkOnBat() {
		boolean bL_Result = true;
		try {
			String sL_TransType="11", sL_OnBatTableName="ONBAT_2CCAS", sL_SelectFields=" count(*) as OnBatRecCount ", sL_CardNo="0000000000000000";
			int nL_ToWhich=2, nL_ProcStatus=1;
			int nL_OnBarRecCount=0;
			ResultSet L_OnBatRs = OnBatDao.getOnBat4(sL_TransType, nL_ToWhich, nL_ProcStatus, sL_OnBatTableName, sL_SelectFields, sL_CardNo);
			while (L_OnBatRs.next()) {
				nL_OnBarRecCount = L_OnBatRs.getInt("OnBatRecCount");
			}
			L_OnBatRs.close();
			
			if (nL_OnBarRecCount>0)
				bL_Result=true;
			else
				bL_Result=false;
				
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result= false;
		}
		
		return bL_Result;
	}
	private static void getSysParm3() {
		setSleepTime(10);
		setStopRun(1);
		setPauseRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("ECSCAI","SLEEP", false,"0")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				if ("1".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData4)) {  
					
					writeLog("E", "AuthBatch_100 is running! Could not execute AuthBatch_060!");
					setPauseRun(0);
					setSleepTime(1800);
					return;
				}
			}
			
		}
		boolean bL_CcsAccountHasData = CcaAccountDao.ifHasData("0000000000", "1");
		if (bL_CcsAccountHasData) {
			writeLog("E", "*** ERROR ecs100有card_acct_id='0000000000',process_status=1資料存在,先不執行此程式! ***");
			setPauseRun(0);
			setSleepTime(1800);
			return;
			
		}
		
		
		
		setSleepTime(10);
		setStopRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("ECSBILL","SLEEP", true, "N")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				if ((!"0".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1)) && (!"".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1.trim())) )
					setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1) );
				
				if ((!"0".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2)) && (!"0".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2.trim())) )
					setStopRun(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2) );
				
				
				//szSleep   = atoi(rTrim(DB_SYSDATA1.arr));
				//szStopRun = atoi(rTrim(DB_SYSDATA2.arr));;

			}
		
    
		}
		
	}

	public AuthBatch060()  throws Exception{
		// TODO Auto-generated constructor stub
	}

}
