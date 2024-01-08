package bank.authbatch.main;

import java.sql.ResultSet;

import bank.authbatch.dao.AuthTxLogDao;
import bank.authbatch.dao.CcaAcctBalanceDao;
import bank.authbatch.dao.CcaDebitBilDao;
import bank.authbatch.dao.CcaIbmReversalDao;
import bank.authbatch.dao.CcaMsgLogDao;
import bank.authbatch.dao.CcaStaDailyMccDao;
import bank.authbatch.dao.CcaStaRiskTypeDao;
import bank.authbatch.dao.CcaStaTxUnormalDao;
import bank.authbatch.dao.CcaSysParm1Dao;
import bank.authbatch.dao.OnBatDao;

public class AuthBatchAuiClearDb extends BatchProgBase{

	
	private String sG_TargetAuTxDate="";//prpc is UDATE
	private String sG_TargetDbLogDate=""; //prpc is LDATE
	
	private void getSysParamValue() {
		
		int nL_AuTxParm = 0, nL_DBlogParm=0;
		try {
			ResultSet L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1("REPORT", "PHYSICAL");
			while (L_SysParm1Rs.next()) {
				nL_AuTxParm = L_SysParm1Rs.getInt("SysData1");
				if (nL_AuTxParm<0)
					nL_AuTxParm=20;
				
			}
 
			L_SysParm1Rs.close();
			
			L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1("REPORT", "DB_LOG");
			while (L_SysParm1Rs.next()) {
				nL_DBlogParm = L_SysParm1Rs.getInt("SysData1");
				if (nL_DBlogParm<0)
					nL_DBlogParm=20;

			}
			
			L_SysParm1Rs.close();
			
			
			sG_TargetAuTxDate = HpeUtil.getPriorNDayString("", 0-nL_AuTxParm);
			sG_TargetDbLogDate= HpeUtil.getPriorNDayString("", 0-nL_DBlogParm);
			
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("Exception on getSysParamValue() => " + e.getMessage());
		}
	
	}
	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
		
		if (sG_CcasOutDir.equals("")) {
			writeLog("E", "Please 設定 CCAS_OUT_DIR 環境變數!");
			return;
		}


		sG_ProgId="AuiClearDb";
		
		try {
		    connDb();
			String sL_OutFile = sG_CcasOutDir + "/ccasClearDB." + HpeUtil.getCurDateStr("");
			System.out.println(sL_OutFile + "^^");
			getSysParamValue();
			deleteAuthTxLog();
			deleteStaDailyMcc();
			deleteCcaStaRiskType();
			deleteCcaStaTxUnormal();
			deleteCcaAcctBalance();
			deleteOnBatData();
			
			deleteCcaDebitBil();
			deleteCcaIbmReversal();
			deleteCcaMsgLog();

//			closeDb();
			writeLog("E", sG_ProgId + " 處理完成");
			//System.out.println(sG_ProgId + " 處理完成");			
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
            closeDb();
        }
		
	}

	private void deleteCcaMsgLog() {
		
		try {
			CcaMsgLogDao.deleteData("20170101");

			//Howard: 這是真正的程式: CcaMsgLogDao.deleteData(sG_TargetDbLogDate);
			//System.out.print("sG_TargetDbLogDate=>" + sG_TargetDbLogDate);			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void deleteCcaIbmReversal() {
		
		try {
			CcaIbmReversalDao.deleteData("20170101");

			//Howard: 這是真正的程式: CcaIbmReversalDao.deleteData(sG_TargetDbLogDate);
			//System.out.print("sG_TargetDbLogDate=>" + sG_TargetDbLogDate);			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void deleteCcaDebitBil() {
		
		try {
			CcaDebitBilDao.deleteData("20170101");

			//Howard: 這是真正的程式: CcaDebitBilDao.deleteData(sG_TargetDbLogDate);
			//System.out.print("sG_TargetDbLogDate=>" + sG_TargetDbLogDate);			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void deleteAuthTxLog() {
	
		try {
			AuthTxLogDao.deleteData("20170101");

			//Howard: 這是真正的程式: AuthTxLogDao.deleteData(sG_TargetAuTxDate);
			//System.out.print("sG_TargetAuTxDate=>" + sG_TargetAuTxDate);			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void deleteStaDailyMcc() {
		
		try {
			CcaStaDailyMccDao.deleteData("20170101");
			//Howard: 這是真正的程式: CcaStaDailyMccDao.deleteData(sG_TargetDbLogDate);
			//System.out.print("sG_TargetDbLogDate=>" + sG_TargetDbLogDate);
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void deleteCcaStaRiskType() {
		
		try {
			CcaStaRiskTypeDao.deleteData("20170101");

			//Howard: 這是真正的程式: CcaStaRiskTypeDao.deleteData(sG_TargetDbLogDate);
			//System.out.print("sG_TargetDbLogDate=>" + sG_TargetDbLogDate);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void deleteCcaStaTxUnormal() {
		
		try {
			CcaStaTxUnormalDao.deleteData("20170101");

			//Howard: 這是真正的程式: CcaStaTxUnormalDao.deleteData(sG_TargetDbLogDate);
			//System.out.print("sG_TargetDbLogDate=>" + sG_TargetDbLogDate);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	
	private void deleteCcaAcctBalance() {
		
		try {
			CcaAcctBalanceDao.deleteData("20170101");

			//Howard: 這是真正的程式: CcaAcctBalanceDao.deleteData(sG_TargetDbLogDate);
			//System.out.print("sG_TargetDbLogDate=>" + sG_TargetDbLogDate);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void deleteOnBatData() {
		
		try {
			
			
			OnBatDao.deleteData("20010101");

			//Howard: 這是真正的程式: OnBatDao.deleteData(sG_CurDate);
			//System.out.print("sG_CurDate=>" + sG_CurDate);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public AuthBatchAuiClearDb()  throws Exception{
		// TODO Auto-generated constructor stub
	}

}
