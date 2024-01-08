package bank.authbatch.main;

import java.sql.ResultSet;

import bank.authbatch.dao.CcaSysParm1Dao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.dao.CrdCardDao;
import bank.authbatch.dao.DbcCardDao;
import bank.authbatch.dao.SysIbmDao;
import bank.authbatch.vo.CcaSysParm3Vo;

public class AuthBatch170  extends BatchProgBase{

	private static void getSysParm3() {
		
		setSleepTime(10);
		setStopRun(1);
		setPauseRun(1);
		String sL_ProgName = "ECS060";
		if (CcaSysParm3Dao.getCcaSysParm3("ECSBILL","SLEEP", false, "0")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				if ("1".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData4)) {  
					
					writeLog("E", sL_ProgName + " is running! Could not execute AuthBatch_170!");
					
					setStopRun(0);
					setPauseRun(0);
					//setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1));
					setSleepTime(1800);
					return;
				}
			}
			
			
		}
		
		if (1==getStopRun()) {
			sL_ProgName = "ECS100";
		
			if (CcaSysParm3Dao.getCcaSysParm3("ECSCAI","SLEEP", false, "0")) {
				if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
					if ("1".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData4)) {  
						
						writeLog("E", sL_ProgName + " is running! Could not execute AuthBatch_170!");
						
						setStopRun(0);
						setPauseRun(0);
						//setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1));
						setSleepTime(1800);
						return;
					}
				}
				
				
			}
		}	
		
		if (CcaSysParm3Dao.getCcaSysParm3("ECS170","SLEEP", true, "0")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				
				setStopRun(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2));
				setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1));
			
			}
			
		}

		

	}

	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
	
		try {
			setStopRun(1);
			

			connDb();


			ResultSet L_SysParm1Rs=null, L_CrdCardRs=null, L_DbcCardRs=null;
			String sL_DbEffDate01="",sL_DbEffDate31="", sL_SysData1Value="", sL_DbCardTo="", sL_DbCardFrom="";
			String sL_CurDate=HpeUtil.getCurDateStr("");
			String sL_DbProcDate="", sL_CardStatus="0", sL_DbOpenDate="", sL_TargetCardNo="";
			String sL_OldEndDate="",sL_ActiveOldEndDate=""; 
			int nL_TmpCursor=0, nL_Continue=0, nL_OpDaysParm=0;
			while (1==getStopRun()) {
				getSysParm3();
				if (0==getStopRun()) {
					commitDb();
					break;
				}
				if (0==getPauseRun()) {
					continue;
				}
				
				CcaSysParm3Dao.updateSysParm3("ECS170", "SLEEP", "1");///*更新 SYS_DATA3-Repeat Flag=On資料*/

				sL_DbEffDate01="";
				sL_DbEffDate31="";
				L_SysParm1Rs = CcaSysParm1Dao.getCcaSysParm1("BATCH", "EFFDATE");
				if (null != L_SysParm1Rs) {
					sL_SysData1Value = L_SysParm1Rs.getString("SysData1").trim();
					if (("-1".equals(sL_SysData1Value)) || ("".equals(sL_SysData1Value)) || ("*".equals(sL_SysData1Value)) ) {
						sL_DbEffDate01="00000000";
						sL_DbEffDate31="99999999";
					}
					else {
						sL_DbEffDate01= sL_SysData1Value + "01";
						sL_DbEffDate31= sL_SysData1Value + "31";
					}
					
					if ("".equals(L_SysParm1Rs.getString("SysData3").trim())) {
						if ("".equals(L_SysParm1Rs.getString("SysData4").trim())) {
							nL_TmpCursor=1;
						}
						else {
							nL_TmpCursor=2;
							sL_DbCardTo = L_SysParm1Rs.getString("SysData4").trim();
							sL_DbCardTo = HpeUtil.fillCharOnRight(sL_DbCardTo, 16, "9");
						}
					}
					else {
						nL_TmpCursor=3;
						sL_DbCardFrom = L_SysParm1Rs.getString("SysData3").trim();
						sL_DbCardTo = L_SysParm1Rs.getString("SysData4").trim();
						sL_DbCardTo = HpeUtil.fillCharOnRight(sL_DbCardTo, 16, "9");
					}
					
					nL_Continue = L_SysParm1Rs.getInt("SysData5");
					
					nL_OpDaysParm = L_SysParm1Rs.getInt("SysData2");
					sL_DbOpenDate = SysIbmDao.getDate(nL_OpDaysParm);
				}
				
				
				
				
				
				
				
				
				if (sL_DbProcDate.equals(sL_CurDate))
					break;

				sL_TargetCardNo="";
				sL_OldEndDate="";
				sL_DbProcDate=HpeUtil.getCurDateStr("");
					
				procCrdCard(sL_CardStatus, nL_TmpCursor, sL_DbOpenDate, sL_DbEffDate01, sL_DbEffDate31, sL_DbProcDate, sL_DbCardTo,sL_DbCardFrom);
				procDbcCard(sL_CardStatus, nL_TmpCursor, sL_DbOpenDate, sL_DbEffDate01, sL_DbEffDate31, sL_DbProcDate, sL_DbCardTo,sL_DbCardFrom);
				
				
				
			}	

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
            closeDb();
        }
	}

	private void procCrdCard(String sL_CardStatus, int nL_TmpCursor,String  sL_DbOpenDate,String  sL_DbEffDate01, String  sL_DbEffDate31, String sL_DbProcDate, String sL_DbCardTo,String sL_DbCardFrom) throws Exception{
		ResultSet L_CrdCardRs = CrdCardDao.getCrdCard(sL_CardStatus, nL_TmpCursor, sL_DbOpenDate, sL_DbEffDate01, sL_DbEffDate31, sL_DbProcDate, sL_DbCardTo,sL_DbCardFrom);
		
		while (L_CrdCardRs.next()) {
			String sL_OldEndDate= L_CrdCardRs.getString("OldEndDate");
			String sL_ActiveOldEndDate = Integer.parseInt(sL_OldEndDate.substring(0,4))-1 + sL_OldEndDate.substring(4, 8);//將效期往前推一年，使卡片失效
			String sL_TargetCardNo = L_CrdCardRs.getString("OldEndDate");
			CrdCardDao.updateCrdCard(sL_TargetCardNo, sL_ActiveOldEndDate);
		
		}
		
		/*
		boolean bL_IsCrdCardEmpty = CrdCardDao.isEmptyResultSet(L_CrdCardRs);
		if (!bL_IsCrdCardEmpty) {
			while(L_CrdCardRs.next()) {
				String sL_OldEndDate= L_CrdCardRs.getString("OldEndDate");
				String sL_ActiveOldEndDate = Integer.parseInt(sL_OldEndDate.substring(0,4))-1 + sL_OldEndDate.substring(4, 8);//將效期往前推一年，使卡片失效
				String sL_TargetCardNo = L_CrdCardRs.getString("OldEndDate");
				CrdCardDao.updateCrdCard(sL_TargetCardNo, sL_ActiveOldEndDate);
				
			}
		}
		*/
		L_CrdCardRs.close();
	}
	
	private void procDbcCard(String sL_CardStatus, int nL_TmpCursor,String  sL_DbOpenDate,String  sL_DbEffDate01, String  sL_DbEffDate31, String sL_DbProcDate, String sL_DbCardTo,String sL_DbCardFrom) throws Exception{
		ResultSet L_DbcCardRs = DbcCardDao.getDbcCard(sL_CardStatus, nL_TmpCursor, sL_DbOpenDate, sL_DbEffDate01, sL_DbEffDate31, sL_DbProcDate, sL_DbCardTo,sL_DbCardFrom);
		
		while(L_DbcCardRs.next()) {
			String sL_OldEndDate= L_DbcCardRs.getString("OldEndDate");
			String sL_ActiveOldEndDate = Integer.parseInt(sL_OldEndDate.substring(0,4))-1 + sL_OldEndDate.substring(4, 8);//將效期往前推一年，使卡片失效
			String sL_TargetCardNo = L_DbcCardRs.getString("OldEndDate");
			DbcCardDao.updateDbcCard(sL_TargetCardNo, sL_ActiveOldEndDate);
			
		}

		/*
		boolean bL_IsDbcCardEmpty = DbcCardDao.isEmptyResultSet(L_DbcCardRs);
		if (!bL_IsDbcCardEmpty) {
			while(L_DbcCardRs.next()) {
				String sL_OldEndDate= L_DbcCardRs.getString("OldEndDate");
				String sL_ActiveOldEndDate = Integer.parseInt(sL_OldEndDate.substring(0,4))-1 + sL_OldEndDate.substring(4, 8);//將效期往前推一年，使卡片失效
				String sL_TargetCardNo = L_DbcCardRs.getString("OldEndDate");
				DbcCardDao.updateDbcCard(sL_TargetCardNo, sL_ActiveOldEndDate);
				
			}
		}
		*/
		L_DbcCardRs.close();
	}

	public AuthBatch170()  throws Exception{
		// TODO Auto-generated constructor stub
	}

}
