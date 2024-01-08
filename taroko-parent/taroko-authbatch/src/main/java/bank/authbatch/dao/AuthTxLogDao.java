package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;


import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;


public class AuthTxLogDao  extends AuthBatchDbHandler{


	static String sG_SelectData4Auo030 = " SELECT  CARD_NO, TX_DATE, "
                								+" TX_TIME,         AUTH_NO, "
                								+" MCHT_NO,         TRANS_TYPE,"
                								+" NT_AMT,          AUTH_STATUS_CODE,"
                								+" EFF_DATE_END "
                								+" FROM  CCA_AUTH_TXLOG "
                								+" WHERE  CRT_USER <> ? " 
                								+" AND  TX_DATE = ? "
                								+" AND  (LOGIC_DEL in ( ?, ?, ?, ?, ? )) "
                								+" AND  (TRANS_TYPE LIKE ? )";
                								//+"WHERE  CREATE_UID <> 'AUTO'"
                								//+"AND  TX_DATE = TO_CHAR((sysdate-1),'YYYYMMDD')"
                								//+"AND  (LOGIC_DEL in ('0','C','M','R','Z'))"
                								//+"AND  (TRANS_TYPE LIKE 'A%')";
	
	static PreparedStatement G_SelectPs4Auo030 = null;
	
	public static ResultSet getData4Auo030() {
		
		ResultSet L_Rs = null;
		try {
			G_SelectPs4Auo030.setString(1, "AUTO");
			
			//G_SelectPs4Auo030.setString("pTxDate", "TX_DATE = TO_CHAR((sysdate-1),'YYYYMMDD')");
			G_SelectPs4Auo030.setString(2, "TX_DATE = TO_CHAR((sysdate-1),'YYYYMMDD')");
			//G_SelectPs4Auo030.setString(2, "20180223"); //Howard:這有data
			
			G_SelectPs4Auo030.setString(3, "0");
			G_SelectPs4Auo030.setString(4, "C");
			G_SelectPs4Auo030.setString(5, "M");
			G_SelectPs4Auo030.setString(6, "R");
			G_SelectPs4Auo030.setString(7, "Z");
			G_SelectPs4Auo030.setString(8, "A%");

			/*
			+"WHERE  CREATE_UID <> :pCreateUid"
			+"AND  TX_DATE = :pTxDate"
			+"AND  (LOGIC_DEL in (:pLd1,:pLd2,:pLd3,:pLd4,:pLd5))"
			+"AND  (TRANS_TYPE LIKE :pTransType)";
			*/
			//+"WHERE  CREATE_UID <> 'AUTO'"
			//+"AND  TX_DATE = TO_CHAR((sysdate-1),'YYYYMMDD')"
			//+"AND  (LOGIC_DEL in ('0','C','M','R','Z'))"
			//+"AND  (TRANS_TYPE LIKE 'A%')";

			L_Rs = G_SelectPs4Auo030.executeQuery();
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("Exception:" + e.getMessage());
			L_Rs = null;
		}
		
		return L_Rs;
	}
	public static void closePs() {
		try {
			if (null != G_SelectPs4Auo030)
				G_SelectPs4Auo030.close();

		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	public static void initPs() {
		
		try {
			if (null == G_SelectPs4Auo030)
				G_SelectPs4Auo030 = getDbConnection().prepareStatement(sG_SelectData4Auo030);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public AuthTxLogDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	public static boolean deleteData(String sP_BeforeDate) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "delete CCA_AUTH_TXLOG "
					+"WHERE TX_DATE< ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setString(1, sP_BeforeDate);
				
			ps.executeUpdate();
			ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	//public static boolean updateAuthTxLog(RowId P_RowId, String sP_CacuAmount, String sP_CacuCash, String sP_MatchFlag, String sP_PgName) {
	public static boolean updateAuthTxLog(String sP_TxDate, String sP_CardNo, String sP_AuthNo, String sP_TraceNo, String sP_TxTime, String sP_CacuAmount, String sP_CacuCash, String sP_MatchFlag, String sP_PgName, String sP_BilRefNo) {
		
		boolean bL_Result = true;
		
		try {
			/*
			String sL_Sql = "UPDATE CCA_AUTH_TXLOG SET MTCH_FLAG= ? ,"
						+"CACU_AMOUNT= ? ,"
						+"CACU_CASH= ? ,"
						+"MTCH_DATE= ? ,"
						+"MOD_USER= ? ,"
						+"MOD_TIME= ? ,"
						+"MOD_PGM= ? "
						+"WHERE ROWID= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_MatchFlag);
			ps.setString(2, sP_CacuAmount);
			ps.setString(3, sP_CacuCash);
			
			
			ps.setString(4, HpeUtil.getCurDateStr(""));
			ps.setString(5, sP_PgName);
			ps.setTimestamp(6, HpeUtil.getCurTimestamp());
			ps.setString(7, sP_PgName);
			
			ps.setRowId(8, P_RowId);
					
			ps.executeUpdate();
			ps.close();
			*/
			

			G_Ps4AuthTxLogUpdated.setString(1, sP_MatchFlag);
			/* Howard: marked on 2019/5/20
			G_Ps4AuthTxLogUpdated.setString(2, sP_CacuAmount);
			G_Ps4AuthTxLogUpdated.setString(3, sP_CacuCash);
			*/
			
			G_Ps4AuthTxLogUpdated.setString(2, HpeUtil.getCurDateStr(""));
			G_Ps4AuthTxLogUpdated.setString(3, sP_PgName);
			G_Ps4AuthTxLogUpdated.setTimestamp(4, HpeUtil.getCurTimestamp());
			G_Ps4AuthTxLogUpdated.setString(5, sP_PgName);
			
			//G_Ps4AuthTxLogUpdated.setString(6, sP_BilRefNo);

			G_Ps4AuthTxLogUpdated.setString(6, sP_TxDate);
			G_Ps4AuthTxLogUpdated.setString(7,sP_CardNo);
			G_Ps4AuthTxLogUpdated.setString(8, sP_AuthNo);
			G_Ps4AuthTxLogUpdated.setString(9, sP_TraceNo);
			G_Ps4AuthTxLogUpdated.setString(10, sP_TxTime);
			
			 
			System.out.println("***" + sP_MatchFlag + "===");
			System.out.println("***" + sP_PgName + "===");
			System.out.println("***" + sP_BilRefNo + "===(No use!)");
			System.out.println("***" + sP_TxDate + "===");
			System.out.println("***" + sP_CardNo + "===");
			System.out.println("***" + sP_AuthNo + "===");
			System.out.println("***" + sP_TraceNo + "===");
			System.out.println("***" + sP_TxTime + "===");
			
			
			System.out.println();
			G_Ps4AuthTxLogUpdated.executeUpdate();
			
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception on G_Ps4AuthTxLogUpdated.updateAuthTxLog() =>" + e.getMessage() + "====");
			bL_Result = false;
		}
		
		return bL_Result;
	}
	public static ResultSet getAuthTxLog(String sP_CardNo, String sP_CacuAmount, String sP_AuthNo) {
		
		ResultSet L_RS= null;
		try {
			String sL_Sql = "select NVL(TX_DATE,'00000000') as AuthTxLogTxDate ,NVL(NT_AMT,0) as AuthTxLogNtAmt from CCA_AUTH_TXLOG "
					+" WHERE CARD_NO= ? AND CACU_AMOUNT=? AND AUTH_NO= ?  FETCH FIRST ? ROWS ONLY ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setString(1, sP_CardNo);
			ps.setString(2, sP_CacuAmount);
			ps.setString(3, sP_AuthNo);
			ps.setInt(4, 2);

			L_RS = ps.executeQuery();
			
		} catch (Exception e) {
			// TODO: handle exception
			L_RS = null;
		}
		
		return L_RS;
	}

	
	public static ResultSet getAuthTxLog(int nP_CardAcctIdx, String sP_CacuCash) {
		
		ResultSet L_RS= null;
		try {
			String sL_Sql = "select SUM(NT_AMT) as SumNtAmt from CCA_AUTH_TXLOG "
					+" WHERE CARD_ACCT_IDX= ? AND CACU_CASH= ? ";
			
			//NamedParamStatement ps = new NamedParamStatement(getDbConnection(), sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setInt(1, nP_CardAcctIdx);
			ps.setString(2, sP_CacuCash);
			

			L_RS = ps.executeQuery();
			
		} catch (Exception e) {
			// TODO: handle exception
			L_RS = null;
		}
		
		return L_RS;
	}

	public static int summaryNtAmt(int nP_CardAcctIdx, String sP_CacuCash) {
		int nL_Result = 0;
		
		try {
			String sL_Sql = "SELECT SUM(NT_AMT) as SumNtAmt from CCA_AUTH_TXLOG "
						+ "where CARD_ACCT_IDX= ? and CACU_CASH= ? ";
			
			//NamedParamStatement ps = new NamedParamStatement(getDbConnection(), sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setInt(1, nP_CardAcctIdx);
			ps.setString(2, sP_CacuCash);

			ResultSet L_RS = ps.executeQuery();
			while (L_RS.next()) {
				nL_Result = L_RS.getInt("SumNtAmt");
			}
			
			releaseConnection(L_RS);

		} catch (Exception e) {
			// TODO: handle exception
			nL_Result = 0;
		}
		
		return nL_Result;
		
	}
	public static ResultSet getAuthTxLog(String sP_CardNo, String sP_TxDate, String sP_CacuAmount, int nP_NtAmt) {
		
		ResultSet L_RS= null;
		try {
			/*
			String sL_Sql = "select ROWID as AuthTxLogRowId,NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
                        +"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
                        +"NVL(REF_NO,' ') as AuthTxLogRefNo,"
                        +"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
                        +"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
                        +"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
                        +"from CCA_AUTH_TXLOG "
                        +" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND TX_DATE= ? and NT_AMT<= ? FETCH FIRST ? ROWS ONLY ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_CardNo);
			ps.setString(2, sP_CacuAmount);
			ps.setString(3, sP_TxDate);
			ps.setInt(4, nP_NtAmt);
			ps.setInt(5, 2);

			L_RS = ps.executeQuery();
			*/
			
			G_Ps4AuthTxLog.setString(1, sP_CardNo);
			G_Ps4AuthTxLog.setString(2, sP_CacuAmount);
			G_Ps4AuthTxLog.setString(3, sP_TxDate);
			G_Ps4AuthTxLog.setInt(4, nP_NtAmt);
			G_Ps4AuthTxLog.setInt(5, 1);

			System.out.println("G_Ps4AuthTxLog=>" + sP_CardNo + "======" + sP_CacuAmount + "======" + sP_TxDate + "======" + nP_NtAmt + "------");
			L_RS = G_Ps4AuthTxLog.executeQuery();
			
		} catch (Exception e) {
			// TODO: handle exception
			L_RS = null;
		}
		
		return L_RS;
	}

	public static ResultSet getAuthTxLog(String sP_TxDate, String sP_CacuAmount) {
		
		ResultSet L_RS= null;
		try {
			String sL_Sql = "select ROWID as AuthTxLogRowId,NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount,NVL(CACU_CASH,'N') as AuthTxLogCacuCash,"
							+"NVL(CARD_NO,' ') as AuthTxLogCardNo,"
							+"NVL(TX_DATE,'00000000') as  as AuthTxLogTxDate,NVL(AUTH_NO,'*')  as AuthTxLogAuthNo,"
							+"NVL(NT_AMT,0) as AuthTxLogNtAmt,NVL(TRANS_TYPE,' ') as AuthTxLogTransType,NVL(REF_NO,' ') as AuthTxLogRefNo,"
							+"NVL(PROC_CODE,' ') as AuthTxLogProcCode,NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,' ') as AuthTxLogMchtNo,"
							+"NVL(MESSAGE_HEAD5,' ') as AuthTxLogMessageHead5,NVL(MESSAGE_HEAD6,' ') as AuthTxLogMessageHead6,"
							+"NVL(RISK_TYPE,'P') as AuthTxLogRiskType, NVL(CARD_ACCT_IDX,0) as AuthTxLogCardAcctIdx "
							+"from CCA_AUTH_TXLOG "
							+" WHERE TX_DATE< ? AND CACU_AMOUNT= ? ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setString(1, sP_TxDate);
			ps.setString(2, sP_CacuAmount);

			L_RS = ps.executeQuery();
			
		} catch (Exception e) {
			// TODO: handle exception
			L_RS = null;
		}
		
		return L_RS;
	}

	public static ResultSet getAuthTxLog(String sP_CardNo, String sP_TxDate, String sP_CacuAmount, int nP_NtAmt, String sP_AuthNo) {
		
		ResultSet L_RS= null;
		try {
			/*
			String sL_Sql = "select ROWID as AuthTxLogRowId,NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
                        +"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
                        +"NVL(REF_NO,' ') as AuthTxLogRefNo,"
                        +"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
                        +"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
                        +"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
                        +"from CCA_AUTH_TXLOG "
                        +" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND TX_DATE= ? and NT_AMT<= ? and AUTH_NO= ? FETCH FIRST ? ROWS ONLY  ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_CardNo);
			ps.setString(2, sP_CacuAmount);
			ps.setString(3, sP_TxDate);
			ps.setInt(4, nP_NtAmt);
			ps.setString(5, sP_AuthNo);
			ps.setInt(6, 2);
			L_RS = ps.executeQuery();
			*/
			G_Ps4AuthTxLog2.setString(1, sP_CardNo);
			G_Ps4AuthTxLog2.setString(2, sP_CacuAmount);
			G_Ps4AuthTxLog2.setString(3, sP_TxDate);
			G_Ps4AuthTxLog2.setInt(4, nP_NtAmt);
			G_Ps4AuthTxLog2.setString(5, sP_AuthNo);
			G_Ps4AuthTxLog2.setInt(6, 1);
			L_RS = G_Ps4AuthTxLog2.executeQuery();
			System.out.println("G_Ps4AuthTxLog2=>" + sP_CardNo + "======" + sP_CacuAmount + "======" + sP_TxDate + "======" + nP_NtAmt + "======" + sP_AuthNo + "------");
			
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception on getAuthTxLog() [G_Ps4AuthTxLog2.executeQuery()]  !!");
			L_RS = null;
		}
		
		return L_RS;
	}

	public static ResultSet getAuthTxLog(String sP_CardNo,  String sP_CacuAmount, String sP_AuthNo, double dP_UBound, double dP_LBound) {
		
		ResultSet L_RS= null;
		try {
			/*
			String sL_Sql = "select ROWID as AuthTxLogRowId,NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
                        +"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
                        +"NVL(REF_NO,' ') as AuthTxLogRefNo,"
                        +"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
                        +"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
                        +"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
                        +"from CCA_AUTH_TXLOG "
                        +" WHERE CARD_NO= ? AND CACU_AMOUNT= ? and AUTH_NO= ? AND (NT_AMT>= ? AND NT_AMT<=? ) FETCH FIRST ? ROWS ONLY ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_CardNo);
			ps.setString(2, sP_CacuAmount);
			ps.setString(3, sP_AuthNo);
			ps.setDouble(4, dP_LBound);
			ps.setDouble(5, dP_UBound);
			ps.setInt(6, 2);
			 	
			L_RS = ps.executeQuery();
			*/
			
			G_Ps4AuthTxLog3.setString(1, sP_CardNo);
			G_Ps4AuthTxLog3.setString(2, sP_CacuAmount);
			G_Ps4AuthTxLog3.setString(3, sP_AuthNo);
			G_Ps4AuthTxLog3.setDouble(4, dP_LBound);
			G_Ps4AuthTxLog3.setDouble(5, dP_UBound);
			G_Ps4AuthTxLog3.setInt(6, 1);
			 	
			L_RS = G_Ps4AuthTxLog3.executeQuery();

			
			
		} catch (Exception e) {
			// TODO: handle exception
			L_RS = null;
		}
		
		return L_RS;
	}

	
	public static ResultSet getAuthTxLog(String sP_CardNo,  String sP_CacuAmount, double dP_UBound, double dP_LBound) {
		
		ResultSet L_RS= null;
		try {
			/*
			String sL_Sql = "select ROWID as AuthTxLogRowId,NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
                        +"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
                        +"NVL(REF_NO,' ') as AuthTxLogRefNo,"
                        +"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
                        +"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
                        +"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
                        +"from CCA_AUTH_TXLOG "
                        +" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND (NT_AMT>= ? AND NT_AMT<= ? ) FETCH FIRST ? ROWS ONLY ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_CardNo);
			ps.setString(2, sP_CacuAmount);
			ps.setDouble(3, dP_LBound);
			ps.setDouble(4, dP_UBound);
			ps.setInt(5, 2);
			 	
			L_RS = ps.executeQuery();
			*/
			
			G_Ps4AuthTxLog5.setString(1, sP_CardNo);
			G_Ps4AuthTxLog5.setString(2, sP_CacuAmount);
			G_Ps4AuthTxLog5.setDouble(3, dP_LBound);
			G_Ps4AuthTxLog5.setDouble(4, dP_UBound);
			G_Ps4AuthTxLog5.setInt(5, 1);
			 	
			L_RS = G_Ps4AuthTxLog5.executeQuery();

			
			
		} catch (Exception e) {
			// TODO: handle exception
			L_RS = null;
		}
		
		return L_RS;
	}

	public static ResultSet getAuthTxLog(String sP_CardNo,  String sP_CacuAmount, double dP_NtAmt) {
		
		ResultSet L_RS= null;
		try {
			/*
			String sL_Sql = "select ROWID as AuthTxLogRowId,NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
                        +"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
                        +"NVL(REF_NO,' ') as AuthTxLogRefNo,"
                        +"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
                        +"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
                        +"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
                        +"from CCA_AUTH_TXLOG "
                        +" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND NT_AMT= ? FETCH FIRST ? ROWS ONLY ";
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_CardNo);
			ps.setString(2, sP_CacuAmount);

			ps.setDouble(3, dP_NtAmt);
			ps.setInt(4, 2);

			 	
			L_RS = ps.executeQuery();
			*/
			
			G_Ps4AuthTxLog4.setString(1, sP_CardNo);
			G_Ps4AuthTxLog4.setString(2, sP_CacuAmount);

			G_Ps4AuthTxLog4.setDouble(3, dP_NtAmt);
			G_Ps4AuthTxLog4.setInt(4, 1);

			System.out.println("G_Ps4AuthTxLog4=>" + sP_CardNo + "===" + sP_CacuAmount + "---" + dP_NtAmt + "======");
			
			L_RS = G_Ps4AuthTxLog4.executeQuery();

			
			
		} catch (Exception e) {
			// TODO: handle exception
			L_RS = null;
		}
		
		return L_RS;
	}

	public static boolean insertAuthTxLog(ResultSet P_IbmReversalRs, String sP_IbmRespCode) {
		//proc is TB_auth_txlog()
		
		boolean bL_Result = true;
		
		try {
			String sL_Sql = "INSERT INTO CCA_AUTH_TXLOG(TX_DATE," 
					 			+"TX_TIME           ,"
					 			+"TX_DATETIME      ," 
					 			+"CARD_NO           ,"
					 			+"AUTH_NO           ,"
					 			+"CORP_FLAG         ,"
					 			+"SUP_FLAG          ,"
					 			+"ACCT_TYPE         ,"
					 			+"P_SEQNO           ,"
					 			+"ID_P_SEQNO        ,"
					 			+"CORP_P_SEQNO      ,"
					 			+"CLASS_CODE        ,"
					 			+"TRANS_TYPE        ,"
					 			+"PROC_CODE         ,"
					 			+"TRACE_NO          ,"
					 			+"MCC_CODE          ,"
					 			+"BANK_COUNTRY      ,"
					 			+"POS_MODE          ,"
					 			+"COND_CODE         ,"
					 			+"STAND_IN          ,"
					 			+"ISO_RESP_CODE     ,"
					 			+"ISO_ADJ_CODE      ,"
					 			+"POS_TERM_ID       ,"
					 			+"TERM_ID           ,"
					 			+"MCHT_NO           ,"
					 			+"MCHT_NAME         ,"
					 			+"MCHT_CITY_NAME    ,"
					 			+"MCHT_CITY         ,"
					 			+"MCHT_COUNTRY      ,"
					 			+"EFF_DATE_END      ,"
					 			+"USER_EXPIRE_DATE  ,"
					 			+"RISK_TYPE         ,"
					 			+"TX_CURRENCY       ,"
					 			+"CONSUME_COUNTRY   ,"
					 			+"ORI_AMT           ,"
					 			+"NT_AMT            ,"
					 			+"AUTH_STATUS_CODE  ,"
					 			+"TX_REMARK         ,"
					 			+"AUTH_REMARK       ,"
					 			+"AUTH_USER         ,"
					 			+"APR_USER          ,"
					 			+"CCAS_AREA_FLAG    ,"
					 			+"AUTH_TYPE         ,"
					 			+"CARD_STATUS       ,"
					 			+"LOGIC_DEL         ,"
					 			+"MTCH_FLAG         ,"
					 			+"MTCH_DATE         ,"
					 			+"BALANCE_FLAG      ,"
					 			+"TO_ACCU_MTCH      ,"
					 			+"CURR_OTB_AMT      ,"
					 			+"CURR_TOT_LMT_AMT  ,"
					 			+"CURR_TOT_STD_AMT  ,"
					 			+"CURR_TOT_TX_AMT   ,"
					 			+"CURR_TOT_CASH_AMT ,"
					 			+"CURR_TOT_UNPAID   ,"
					 			+"STAND_IN_REASON   ,"
					 			+"AUTH_UNIT         ,"
					 			+"CACU_AMOUNT       ,"
					 			+"CACU_CASH         ,"
					 			+"CACU_FLAG         ,"
					 			+"STAND_IN_RSPCODE  ,"
					 			+"STAND_IN_ONUSCODE ,"
					 			+"TX_AMT_PCT        ,"
					 			+"TX_CVC2           ,"
					 			+"AE_TRANS_AMT      ,"
					 			+"ROC               ,"
					 			+"ONLINE_REDEEM     ,"
					 			+"IBM_BIT33_CODE    ,"
					 			+"IBM_BIT39_CODE    ,"
					 			+"ACCT_NO           ,"
					 			+"VIP_CODE          ,"
					 			+"EC_FLAG           ,"
					 			+"CVD_PRESENT       ,"
					 			+"EC_IND            ,"
					 			+"UCAF              ,"
					 			+"CAVV_RESULT       ,"
					 			+"TRAIN_FLAG        ,"
					 			+"GROUP_CODE        ,"
					 			+"FALLBACK          ,"
					 			+"FRAUD_CHK_RSLT    ,"
					 			+"AC_VERY_RSLT      ,"
					 			+"V_CARD_NO         ,"
					 			+"AUTH_SEQNO        ,"
					 			+"VDCARD_FLAG       ,"
					 			+"REVERSAL_FLAG     ,"
					 			+"TRANS_CODE        ,"
					 			+"REF_NO            ,"
					 			+"ORI_AUTH_NO       ,"
					 			+"CRT_DATE          ,"
					 			+"CRT_TIME          ,"
					 			+"CRT_USER          ,"
					 			+"CHG_DATE          ,"
					 			+"CHG_TIME          ,"
					 			+"CHG_USER          ,"
					 			+"MOD_USER          ,"
					 			+"MOD_TIME          ,"
					 			+"MOD_PGM           ,"
					 			+"MOD_SEQNO)";
			sL_Sql += " VALUES(? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? , ? , ? ,? , ? , ? , ? , ? , ? , ? , ? )";

			String sL_CurDate= HpeUtil.getCurDateStr("");
			String sL_CurTime=HpeUtil.getCurTimeStr();

			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
        	ps.setString(1, sL_CurDate);//			TX_DATE           ,       /* 建檔日期               */ -- 
        	ps.setString(2, sL_CurTime);//TX_TIME           ,       /* 建檔時間               */ -- 
        	ps.setString(3, "");//TX_DATETIME      ,                          , /* 消費日期時間(FROM ibt */ -- 
        	ps.setString(4, P_IbmReversalRs.getString("CARD_NO"));//CARD_NO           ,      /* 卡號                   */ -- 
        	ps.setString(5, P_IbmReversalRs.getString("AUTH_NO"));//AUTH_NO           ,       /* 授權號碼               */ -- 
        	ps.setString(6, "");//CORP_FLAG         ,       /* 商務卡註記             */ -- 
        	ps.setString(7, "");//SUP_FLAG          ,       /* 正附卡別               */ -- 
        	ps.setString(8, "");//ACCT_TYPE         ,       /* 帳戶類別               */ -- +new
        	ps.setString(9, "");//P_SEQNO           ,      /* 帳戶流水號             */ -- +new
        	ps.setString(10, "");//ID_P_SEQNO        ,      /* 身份證流水號           */ -- +new
        	ps.setString(11, "");//CORP_P_SEQNO      ,      /* 公司流水號             */ -- +new
        	ps.setString(12, "");//CLASS_CODE        ,       /* 卡戶等級               */ -- 
        	ps.setString(13, "");//TRANS_TYPE        ,       /* ISO-交易類別           */ -- 
        	ps.setString(14, "");//PROC_CODE         ,       /* 處理狀況               */ -- 00: 額度相同 01~09: IBM 回覆錯誤碼 20:再比對額度相同  90:額度不同 99:IBM未回覆
        	ps.setString(15, P_IbmReversalRs.getString("TRACE_NO"));//TRACE_NO          ,       /* 追蹤號碼               */ -- 
        	ps.setString(16, "");//MCC_CODE          ,       /* MCC代號                */ -- 
        	ps.setString(17, "");//BANK_COUNTRY      ,       /* 收單行國家             */ -- 
        	ps.setString(18, "");//POS_MODE          ,       /* Pos Entry mode         */ -- 
        	ps.setString(19, "");//COND_CODE         ,       /* cond code              */ -- 
        	ps.setString(20, "");//STAND_IN          ,      /* 收單行                 */ -- 
        	ps.setString(21, sP_IbmRespCode);//ISO_RESP_CODE     ,       /* ISO交易回覆碼          */ -- 
        	ps.setString(22, "");//ISO_ADJ_CODE      ,       /* ISO交易回覆碼          */ -- 
        	ps.setString(23, "");//POS_TERM_ID       ,      /* POS/ATM 代號           */ -- 
        	ps.setString(24, P_IbmReversalRs.getString("TERM_ID"));//TERM_ID           ,      /* 工作站代碼             */ -- 
        	ps.setString(25, P_IbmReversalRs.getString("MCHT_NO"));//MCHT_NO           ,      /* 特店代碼               */ -- 
        	ps.setString(26, "");//MCHT_NAME         ,      /* 特店名稱               */ -- 
        	ps.setString(27, "");//MCHT_CITY_NAME    ,      /* 特店城市名稱           */ -- +new
        	ps.setString(28, "");//MCHT_CITY         ,       /* 特店城市               */ -- +new
        	ps.setString(29, "");//MCHT_COUNTRY      ,       /* 特店國家               */ -- +new
        	ps.setString(30, "");//EFF_DATE_END      ,       /* CCAS-卡片有效日期      */ -- 
        	ps.setString(31, "");//USER_EXPIRE_DATE  ,       /* ISO輸入卡片有效日期    */ -- 
        	ps.setString(32, P_IbmReversalRs.getString("RISK_TYPE"));//RISK_TYPE         ,       /* 風險類別               */ -- 
        	ps.setString(33, "");//TX_CURRENCY       ,       /* 幣別                   */ -- 
        	ps.setString(34, "");//CONSUME_COUNTRY   ,       /* 消費國家代碼           */ -- 
        	ps.setString(35, "");//ORI_AMT           , /* 消費金額(原幣         */ -- 
        	ps.setString(36, P_IbmReversalRs.getString("TRANS_AMT"));//NT_AMT            , /* 消費金額(台幣         */ -- 
        	ps.setString(37, "");//AUTH_STATUS_CODE  ,       /* 授權結果               */ -- 
        	ps.setString(38, "");//TX_REMARK         ,      /* 交易備註               */ -- 
        	ps.setString(39, "");//AUTH_REMARK       ,   /* 授權人員備註說明       */ -- 
        	ps.setString(40, "");//AUTH_USER         ,      /* 授權人員代碼           */ -- 
        	ps.setString(41, "");//APR_USER          ,      /* 放行人員代碼           */ -- 
        	ps.setString(42, "");//CCAS_AREA_FLAG    ,       /* 國內/國外註記          */ -- 
        	ps.setString(43, "");//AUTH_TYPE         ,       /* 授權類別               */ -- 
        	ps.setString(44, "");//CARD_STATUS       ,       /* 卡片狀態               */ -- 
        	ps.setString(45, "");//LOGIC_DEL         ,       /* 撤掛註記               */ -- 
        	ps.setString(46, "");//MTCH_FLAG         ,       /* 帳單比對註記           */ -- 
        	ps.setString(47, "");//MTCH_DATE         ,       /* 帳單比對日期           */ -- 
        	ps.setString(48, "");//BALANCE_FLAG      ,       /* 餘額註記               */ -- 
        	ps.setString(49, "");//TO_ACCU_MTCH      ,       /* 累計帳單比對註記       */ -- 
        	ps.setString(50, "");//CURR_OTB_AMT      , /* 當時可用額度餘額       */ -- 
        	ps.setString(51, "");//CURR_TOT_LMT_AMT  , /* 當時依據總額度         */ -- 
        	ps.setString(52, "");//CURR_TOT_STD_AMT  , /* 當時依據標準額度       */ -- 
        	ps.setString(53, "");//CURR_TOT_TX_AMT   , /* 當時累計一般消費金額   */ -- 
        	ps.setString(54, "");//CURR_TOT_CASH_AMT , /* 當時累計預借金額       */ -- 
        	ps.setString(55, "");//CURR_TOT_UNPAID   , /* 當時總未付金額         */ -- 
        	ps.setString(56, "");//STAND_IN_REASON   ,       /* 代行原因               */ -- 
        	ps.setString(57, "");//AUTH_UNIT         ,       /* 授權單位               */ -- 
        	ps.setString(58, "");//CACU_AMOUNT       ,       /* 計入OTB註記            */ -- 
        	ps.setString(59, "");//CACU_CASH         ,       /* 計入OTB預現註記        */ -- 
        	ps.setString(60, "");//CACU_FLAG         ,       /* 計入分類註記           */ -- 
        	ps.setString(61, "");//STAND_IN_RSPCODE  ,       /* STAND IN 回覆碼(NCCC  */ -- 
        	ps.setString(62, "");//STAND_IN_ONUSCODE ,       /* Stand In 回覆碼(OnUs  */ -- 
        	ps.setString(63, "");//TX_AMT_PCT        , /* 交易金額%              */ -- 
        	ps.setString(64, "");//TX_CVC2           ,       /* 交易CVC2               */ -- 
        	ps.setString(65, "");//AE_TRANS_AMT      ,      /* AE交易金額             */ -- 
        	ps.setString(66, "");//ROC               ,       /* ROC                    */ -- 
        	ps.setString(67, "");//ONLINE_REDEEM     ,       /* 交易型態               */ -- 
        	ps.setString(68, "");//IBM_BIT33_CODE    ,      /* 銀行交易碼             */ -- 
        	ps.setString(69, "");//IBM_BIT39_CODE    ,       /* 銀行回覆碼             */ -- 
        	ps.setString(70, "");//ACCT_NO           ,      /* 帳戶帳號               */ -- 
        	ps.setString(71, "");//VIP_CODE          ,       /* vip評等                */ -- 
        	ps.setString(72, "");//EC_FLAG           ,       /* Reversal旗標           */ -- 
        	ps.setString(73, "");//CVD_PRESENT       ,       /* CVD                    */ -- 
        	ps.setString(74, "");//EC_IND            ,       /* mail_phone_ec 辨別碼   */ -- 
        	ps.setString(75, "");//UCAF              ,       /* UCAF                   */ -- +new
        	ps.setString(76, "");//CAVV_RESULT       ,       /* CAVV結果               */ -- 
        	ps.setString(77, "");//TRAIN_FLAG        ,       /* 高鐵交易               */ -- 
        	ps.setString(78, "");//GROUP_CODE        ,       /* 團體代號               */ -- 
        	ps.setString(79, "");//FALLBACK          ,       /* FALLBACK               */ -- 
        	ps.setString(80, "");//FRAUD_CHK_RSLT    ,       /* 偽卡檢查結果           */ -- 
        	ps.setString(81, "");//AC_VERY_RSLT      ,       /* AC驗証結果             */ -- 
        	ps.setString(82, "");//V_CARD_NO         ,      /* HCE卡號                */ -- 
        	ps.setString(83, "");//AUTH_SEQNO        ,      /* 授權流水號             */ -- +new: YYmmdd999999
        	ps.setString(84, "D");//VDCARD_FLAG       ,       /* 信用金融卡旗標         */ -- +new: C.信用卡,D.金融卡
        	ps.setString(85, "");//REVERSAL_FLAG     ,       /* 沖銷註記               */ -- +new
        	ps.setString(86, "");//TRANS_CODE        ,       /* 交易碼                 */ -- +new
        	ps.setString(87, P_IbmReversalRs.getString("REF_NO"));//REF_NO            ,      /* 沖銷原始序號(ISO-37   */ -- +new
        	ps.setString(88, "");//ORI_AUTH_NO       ,       /* ISO原始授權碼          */ -- +new
        	ps.setString(89, "");//CRT_DATE          ,       /* 鍵檔日期               */ -- 
        	ps.setString(90, "");//CRT_TIME          ,       /* 建檔時間               */ -- 
        	ps.setString(91, "");//CRT_USER          ,      /* 建檔人員               */ -- 
        	ps.setString(92, "");//CHG_DATE          ,       /* 帳戶維護日期           */ -- 
        	ps.setString(93, "");//CHG_TIME          ,       /* 最近更新時間           */ -- 
        	ps.setString(94, "");//CHG_USER          ,      /* 最近更新人員           */ -- 
        	ps.setString(95, "");//MOD_USER          ,      /* 異動使用者             */ -- 
        	ps.setString(96, "");//MOD_TIME          , /* 異動時間               */ -- 
        	ps.setString(97, "");//MOD_PGM           ,      /* 異動程式               */ -- 
        	ps.setString(98, "");//MOD_SEQNO


			ps.executeUpdate();
			ps.close();


			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

}
