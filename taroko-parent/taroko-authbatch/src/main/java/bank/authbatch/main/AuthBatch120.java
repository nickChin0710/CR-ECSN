package bank.authbatch.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.sql.ResultSet;


import bank.authbatch.dao.AuthTxLogDao;
import bank.authbatch.dao.CcaIbmReversalDao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.vo.CcaSysParm3Vo;

public class AuthBatch120  extends BatchProgBase{

	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
		try {
			setStopRun(1);
			

			connDb();

			
			/*
			當 NCCC 送入debit card 授權交易時，授權系統會透過 sendIBM/recvIBM 與銀行系統交換資料，
			但若發生timeout，則 授權系統 會把交易資料 insert into CCA_IBM_REVERSAL，
			然後由此程式，由 table CCA_IBM_REVERSAL 讀取資料，再送到 銀行系統，並將結果寫入 table auth_tx_log 
			*/
			
			while (1==getStopRun()) {
				getSysParm3();
				if (0==getStopRun()) {
					commitDb();
					break;
				}
			    	
				
				String sL_IbmSocketIp = getBankSocketServerIp();
				int nL_IbmSocketPort = Integer.parseInt(getBankSocketServerPort());;
				Socket L_SocketToIbm  =  new  Socket(sL_IbmSocketIp ,  nL_IbmSocketPort);
	    		
	    		
	    		L_SocketToIbm.setSoTimeout(4*1000);//設定 timeout == 4 secs
				BufferedInputStream L_IbmInputStream =  new  BufferedInputStream(L_SocketToIbm.getInputStream());
				BufferedOutputStream  L_IbmOutputStream = new  BufferedOutputStream(L_SocketToIbm.getOutputStream());
	  
				ResultSet L_IbmReversalRs =  CcaIbmReversalDao.getIbmReversal();
				while (L_IbmReversalRs.next()) {
					if (processData(L_IbmReversalRs, L_IbmInputStream, L_IbmOutputStream))
						commitDb();
					else
						rollbackDb();
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
        } finally {
            closeDb();
        }
		
	}

	private boolean processData(ResultSet P_IbmReversalRs, BufferedInputStream P_IbmInputStream, BufferedOutputStream  P_IbmOutputStream) {
		boolean bL_Result = true;
		
		String sL_Data2Ibm="", sL_IbmReversalRowId="", sL_IbmRespCode="";
		try {
			sL_Data2Ibm = P_IbmReversalRs.getString("MSG_DATA"); // 要將 sL_Data2Ibm 送出去給 IBM 
			
			String sL_SocketResp =  HpeUtil.exchangeDataWithEcs(P_IbmOutputStream, P_IbmInputStream, sL_Data2Ibm);

			
			sL_IbmRespCode = sL_SocketResp.substring(84,86); //Howard: 回應的 message type 一定是 0430
			
			
			if (!AuthTxLogDao.insertAuthTxLog(P_IbmReversalRs, sL_IbmRespCode))
				return false;
			
			if (!CcaIbmReversalDao.updateIbmReversal(P_IbmReversalRs, sL_IbmRespCode))
				return false;
			
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("Exception on AuthBatch120.processData().Exception is:" + e.getMessage());
			bL_Result=false;
			
		}
		
		return bL_Result;
	}
	private static void getSysParm3() {
		
		setSleepTime(10);
		setStopRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("IBMSEND","SLEEP", true, "1")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				if ("1".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData4)) {  
					
					writeLog("E", "AuthBatch_100 is running! Could not execute AuthBatch_080!");
					
					setStopRun(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2));
					setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1));
					return;
				}
			}
			
			
		}

		

	}

	public AuthBatch120() throws Exception {
		// TODO Auto-generated constructor stub
	}

}
