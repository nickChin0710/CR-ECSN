/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-01-15  V1.00.00       Suzuwei fix race condition: static database connection
* 111-01-19  V1.00.01  Justin       fix J2EE Bad Practices: Leftover Debug Code
* 112-03-09  V1.00.02  Wilson       insert cca_card_base add acct_type
* 112-03-09  V1.00.03  Wilson       insert cca_consume add p_seqno
******************************************************************************/
package bank.authbatch.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;

public class AuthBatchDbHandler extends BatchProgBase{


	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub

	}
	public AuthBatchDbHandler() throws Exception{
		// TODO Auto-generated constructor stub
	}
	public void subInitialize()
	{
	}

	public static boolean isEmptyResultSet_Old(ResultSet P_Rs) throws Exception {
		boolean bL_Result = false;
		boolean bL_HasData=true;

		if (P_Rs == null || !P_Rs.first()) {
			bL_HasData=false;
			bL_Result = true;

		}
		/*
		while(P_Rs.next()) {

			bL_HasData=true;
			bL_Result = false;
			break;

		}
		 */
		return bL_Result;
	}




	// protected static Connection Db2Connection;
	static final ThreadLocal<Connection> connections = new ThreadLocal<Connection>();

	public static PreparedStatement G_Ps4CardBase = null;
	public static PreparedStatement G_Ps4CardBase4CardAcctIndex = null;

	public static PreparedStatement G_Ps4CardBase2 = null;
	public static PreparedStatement G_Ps4CardBaseUpdate = null;

	public static PreparedStatement G_Ps4AuthTxLog = null;
	public static PreparedStatement G_Ps4AuthTxLog2 = null;
	public static PreparedStatement G_Ps4AuthTxLog3 = null;
	public static PreparedStatement G_Ps4AuthTxLog4 = null;
	public static PreparedStatement G_Ps4AuthTxLog5 = null;

	public static PreparedStatement G_Ps4CardAcct = null;
	public static PreparedStatement G_Ps4CardAcct2 = null;
	public static PreparedStatement G_Ps4CardAcct3 = null;
	public static PreparedStatement G_Ps4CardAcct4 = null;
	public static PreparedStatement G_Ps4CardAcct5 = null;
    public static PreparedStatement G_Ps4CardAcct6 = null;

	public static PreparedStatement G_Ps4CardAcctIndex = null;
	public static PreparedStatement G_Ps4CardAcctIndex2 = null;
	public static PreparedStatement G_Ps4CardAcctIndex3 = null;
	public static PreparedStatement G_Ps4CardAcctIndex4 = null;


	public static PreparedStatement G_Ps4CardAcctUpdated = null;
	public static PreparedStatement G_Ps4AuthTxLogUpdated = null;
	public static PreparedStatement G_Ps4SysParm1 = null;
	public static PreparedStatement G_Ps4SysParm2 = null;

	public static PreparedStatement G_Ps4InsertCcaUnMatch = null;

	public static PreparedStatement G_Ps4CrdIdno = null;

	public static PreparedStatement G_Ps4CcaCreditLog = null;

	public static PreparedStatement G_Ps4CcaConsume = null;
	public static PreparedStatement G_Ps4CcaConsumeInsert = null;
	public static PreparedStatement G_Ps4CcaConsume4Update = null;

	public static PreparedStatement G_Ps4ActAcno = null;

//	public static PreparedStatement G_Ps4GetSequenceNextValue1 = null; // 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
	public static PreparedStatement G_Ps4GetSequenceNextValue2 = null;

	private static void initSelectSequenceValue() {

		//down, set G_Ps4GetSequenceNextValue1
		//String sL_Sql = " SELECT NEXT VALUE FOR ? FROM sysibm.sysdummy1 "; Howard : 這樣做會於 setString 時會錯
		// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX
//		String sL_Sql = " SELECT NEXT VALUE FOR ECS_CARD_ACCT_IDX FROM sysibm.sysdummy1 ";
//
//		G_Ps4GetSequenceNextValue1 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4GetSequenceNextValue1		





		//down, set G_Ps4GetSequenceNextValue2
		String sL_Sql = " SELECT NEXT VALUE FOR ECS_TRACE_NO FROM sysibm.sysdummy1 ";

		G_Ps4GetSequenceNextValue2 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4GetSequenceNextValue2







	}


	private static void initActAcnoPs() {

		//down, set G_Ps4ActAcno 
		String sL_Sql = "select LINE_OF_CREDIT_AMT as ActAcnoLineOfCreditAmt from ACT_ACNO where ACNO_P_SEQNO= ? ";

		G_Ps4ActAcno = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4ActAcno		


	}

	private static void initCrdIdnoPs() {

		//down, set G_Ps4CrdIdno 
		String sL_Sql = "select NVL(CARD_SINCE,' '),NVL(JOB_POSITION,' ') "
				+ "from CRD_IDNO where ID_NO= ? and ID_NO_CODE= ?  "; //CRD_IDNO 的舊table name is CARD_HLDR


		G_Ps4CrdIdno = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CrdIdno		


	}


	private static void initCreditLogPs() {

		//down, set G_Ps4CcaCreditLog

		String sL_Sql = "insert into CCA_CREDIT_LOG(TX_DATE,  TX_TIME,  CARD_ACCT_IDX, ADJ_QUOTA, ADJ_EFF_START_DATE, ADJ_EFF_END_DATE, ADJ_REASON,  ORG_AMT_MONTH, TOT_AMT_MONTH, ADJ_USER, MOD_TIME, MOD_PGM) ";
		//sL_Sql += "values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12)";
		sL_Sql += "values(?,?,?,?,?,?,?,?,?,?,?,?)";



		G_Ps4CcaCreditLog = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CcaCreditLog		


	}

	private static void initCcaConsumePs() {



		//down, set G_Ps4CcaConsume

		String sL_Sql = "INSERT INTO CCA_CONSUME( "
				+"CARD_ACCT_IDX,    PAID_ANNUAL_FEE,"
				+"PAID_SRV_FEE,     PAID_LAW_FEE,    PAID_PUNISH_FEE,     PAID_INTEREST_FEE,"
				+"PAID_CONSUME_FEE, PAID_PRECASH,    PAID_CYCLE,          UNPAID_ANNUAL_FEE,"
				+"UNPAID_SRV_FEE,   UNPAID_LAW_FEE,  UNPAID_INTEREST_FEE, UNPAID_CONSUME_FEE,"
				+"UNPAID_PRECASH,   ARGUE_AMT,       PRE_PAY_AMT,         M1_AMT,"
				+"LATEST_1_MNTH,    LATEST_2_MNTH,   LATEST_3_MNTH,       LATEST_4_MNTH,"
				+"LATEST_5_MNTH,    LATEST_6_MNTH,   LATEST_7_MNTH,       LATEST_8_MNTH,"
				+"LATEST_9_MNTH,    LATEST_10_MNTH,  LATEST_11_MNTH,      LATEST_12_MNTH,"
				+"MAX_CONSUME_AMT,  MAX_CONSUME_DATE,MAX_PRECASH_AMT,     MAX_PRECASH_DATE,"
				+"PAY_LATEST_AMT,   PAY_DATE,        PAY_SETTLE_DATE,     PAYMENT_DUE_DATE,"
				+"TOT_UNPAID_AMT,   BILL_LOW_LIMIT,  BILL_LOW_PAY_AMT,    TOT_LIMIT_AMT,"
				+"TOT_PRECASH_AMT,  TOT_DUE,"
				+"CONSUME_1,        CONSUME_2,       CONSUME_3,           CONSUME_4,"
				+"CONSUME_5,        CONSUME_6,"
				+"CRT_DATE,      CRT_TIME,     MOD_USER,          MOD_TIME,"
				+"IBM_RECEIVE_AMT,"
				+"UNPOST_INST_FEE)"
				+"VALUES(? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? )";		              



		G_Ps4CcaConsume = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CcaConsume		


		//down, set G_Ps4CcaConsumeInsert

		sL_Sql = "INSERT INTO CCA_CONSUME( "
				+"CARD_ACCT_IDX,    PAID_ANNUAL_FEE,"
				+"PAID_SRV_FEE,     PAID_LAW_FEE,    PAID_PUNISH_FEE,     PAID_INTEREST_FEE,"
				+"PAID_CONSUME_FEE, PAID_PRECASH,    PAID_CYCLE,          UNPAID_ANNUAL_FEE,"
				+"UNPAID_SRV_FEE,   UNPAID_LAW_FEE,  UNPAID_INTEREST_FEE, UNPAID_CONSUME_FEE,"
				+"UNPAID_PRECASH,   ARGUE_AMT,       PRE_PAY_AMT,         M1_AMT,"
				+"LATEST_1_MNTH,    LATEST_2_MNTH,   LATEST_3_MNTH,       LATEST_4_MNTH,"
				+"LATEST_5_MNTH,    LATEST_6_MNTH,   LATEST_7_MNTH,       LATEST_8_MNTH,"
				+"LATEST_9_MNTH,    LATEST_10_MNTH,  LATEST_11_MNTH,      LATEST_12_MNTH,"
				+"MAX_CONSUME_AMT,  MAX_CONSUME_DATE,MAX_PRECASH_AMT,     MAX_PRECASH_DATE,"
				+"PAY_LATEST_AMT,   PAY_DATE,        PAY_SETTLE_DATE,     PAYMENT_DUE_DATE,"
				+"TOT_UNPAID_AMT,   BILL_LOW_LIMIT,  BILL_LOW_PAY_AMT,    TOT_LIMIT_AMT,"
				+"TOT_PRECASH_AMT,  TOT_DUE,"
				+"CONSUME_1,        CONSUME_2,       CONSUME_3,           CONSUME_4,"
				+"CONSUME_5,        CONSUME_6,"
				+"CRT_DATE,          MOD_USER,          MOD_TIME,"
				+"IBM_RECEIVE_AMT,"
				+"UNPOST_INST_FEE,  P_SEQNO)"
				+"VALUES(? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)";		              

		G_Ps4CcaConsumeInsert = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CcaConsumeInsert


	}



	private static void initSystemParm1Ps() {

		//down, set G_Ps4SysParm1 
		String sL_Sql = "select NVL(SYS_DATA1,'-1') as SysData1,NVL(SYS_DATA2,'0') as SysData2,NVL(SYS_DATA3,' ') as SysData3, NVL(SYS_DATA4,' ') as SysData4,NVL(SYS_DATA5,'0') as SysData5 from CCA_SYS_PARM1 where SYS_ID= ?  AND SYS_KEY= ?  "; //key....

		G_Ps4SysParm1 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4SysParm1		


	}

	private static void initSystemParm2Ps() {

		//down, set G_Ps4SysParm2 
		String sL_Sql = "select NVL(SYS_DATA1,'A') as SysData1 from CCA_SYS_PARM2 where SYS_ID= ?  AND SYS_KEY= ?  "; //key....

		G_Ps4SysParm2 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4SysParm2		


	}

	private static void initCcaUnMatchPs() {

		//down, set G_Ps4InsertCcaUnMatch
		String sL_Sql = "INSERT INTO CCA_UNMATCH(U_DATE,CARD_NO,TX_DATE,AUTH_NO, "
				+" AMT_NT,TRANS_TYPE,REF_NO,PROC_CODE,MCC_CODE,MCHT_NO,"
				+"MESSAGE_HEAD5,MESSAGE_HEAD6,BIT127_REC_DATA,U_TIME,"
				+"AUTH_DATE,AUTH_AMT)";
		sL_Sql += "VALUES(? ,? , ? , ? ,"
				+"? ,? , ? , ? , ? , ? , "
				+"?, ? , ? ,? ,"
				+"?, ?)";

		G_Ps4InsertCcaUnMatch = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4InsertCcaUnMatch		


	}

	private static void initCardBasePs() {
		String sL_Sql = "select ROWID as CardBaseRowId, "
				+"CARD_NO as CcaCardBaseCardNo, "
				+"DEBIT_FLAG as CcaCardBaseDebitFlag, "
				+"ID_P_SEQNO as CcaCardBaseIdPSeqNo, "
				+"P_SEQNO as CcaCardBasePSeqNo, "
				+"ACNO_P_SEQNO as CcaCardBaseAcnoPSeqNo, "

				+"CARD_ACCT_IDX as CcaCardBaseCardAcctIdx "
				+ "from CCA_CARD_BASE where CARD_NO= ?  "; //key....

		G_Ps4CardBase = getPreparedStatement(sL_Sql, true);

	}

	private static void initCcaConsumePs4Update() {

		String sL_Sql = "update CCA_CONSUME  "
				+ "SET IBM_RECEIVE_AMT=DECODE(SIGN(IBM_RECEIVE_AMT - ? ),-1,0 ,IBM_RECEIVE_AMT - ? ) "
				+ "WHERE CARD_ACCT_IDX= ? ";

		G_Ps4CcaConsume4Update = getPreparedStatement(sL_Sql, true);

	}


	private static void initCardBasePs4CardAcctIndex() {
		String sL_Sql = "select  CARD_ACCT_IDX "
				+ "from CCA_CARD_BASE where CARD_NO= ?  "; //key....

		G_Ps4CardBase4CardAcctIndex = getPreparedStatement(sL_Sql, true);

	}

	private static void initCardBasePs2() {

		//down, set G_Ps4CardBase2
		String sL_Sql = "insert into CCA_CARD_BASE(card_no,debit_flag, bin_type, id_p_seqno, p_seqno, acno_p_seqno, CORP_P_SEQNO, MAJOR_ID_P_SEQNO, ACNO_FLAG,  CARD_INDICATOR, onus_opp_type, voice_open_code, voice_auth_code, voice_open_code2, voice_auth_code2,old_pin,card_acct_idx,DC_CURR_CODE ,MOD_TIME, MOD_PGM, ACCT_TYPE) ";
		sL_Sql += "values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		G_Ps4CardBase2 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CardBase2

		//down, set G_Ps4CardBaseUpdate
		sL_Sql = "update CCA_CARD_BASE set CORP_FLAG= nvl( ? , CORP_FLAG ) , VOICE_OPEN_CODE= ? , "
				+ "VOICE_OPEN_CODE2= ? , VOICE_AUTH_CODE= ? , VOICE_AUTH_CODE2= ? , OLD_PIN= ? , "
				+ "MOD_USER= ? , MOD_TIME= ? , MOD_PGM= ? , MOD_SEQNO= ?  ";
		sL_Sql += "where card_no= ? ";
		G_Ps4CardBaseUpdate = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CardBaseUpdate

	}

	private static void initAuthTxLogPs() {

		//down, set G_Ps4AuthTxLog
		//String sL_Sql = "select ROWID as AuthTxLogRowId,NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
		String sL_Sql = "select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
				+"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
				+"NVL(REF_NO,' ') as AuthTxLogRefNo,"
				+"NVL(NT_AMT,0) as AuthTxLogNtAmt, CARD_NO as AuthTxLogCardNo, AUTH_NO as AuthTxLogAuthNo, "
				+"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
				+"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag, "
				+"NVL(TRACE_NO,'') as AuthTxLogTraceNo, NVL(TX_TIME,'') as AuthTxLogTxTime "
				+"from CCA_AUTH_TXLOG "
				+" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND TX_DATE= ? and NT_AMT<= ? FETCH FIRST ? ROWS ONLY  ";

		G_Ps4AuthTxLog = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4AuthTxLog


		//down, set G_Ps4AuthTxLogUpdated

		String sL_UpdateSql = "UPDATE CCA_AUTH_TXLOG SET MTCH_FLAG= ? ,"
				//+"CACU_AMOUNT= ? ,"  Howard: marked on 2019/05/20
				//+"CACU_CASH= ? ," Howard: marked on 2019/05/20
				+"MTCH_DATE= ? ,"
				+"MOD_USER= ? ,"
				+"MOD_TIME= ? ,"
				+"MOD_PGM= ? "
				//+"BIL_REFERENCE_NO= ? "
				+"WHERE TX_DATE= ? and CARD_NO=? and AUTH_NO=? and TRACE_NO=? and TX_TIME=? ";
		//+"WHERE ROWID= ? ";

		G_Ps4AuthTxLogUpdated = getPreparedStatement(sL_UpdateSql, false);
		//up, set G_Ps4AuthTxLogUpdated


		//down, set G_Ps4AuthTxLog2
		sL_Sql = "select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
				+ "CARD_NO as AuthTxLogCardNo, AUTH_NO as AuthTxLogAuthNo, "
				+ "TX_TIME as AuthTxLogTxTime, TRACE_NO as AuthTxLogTraceNo, "
				+"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
				+"NVL(REF_NO,' ') as AuthTxLogRefNo,"
				+"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
				+"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
				+"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
				+"from CCA_AUTH_TXLOG "
				+" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND TX_DATE= ? and NT_AMT<= ? and AUTH_NO= ? FETCH FIRST ? ROWS ONLY  ";

		G_Ps4AuthTxLog2 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4AuthTxLog2

		//down, set G_Ps4AuthTxLog3
		sL_Sql = "select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
				+"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
				+"NVL(REF_NO,' ') as AuthTxLogRefNo,"
				+ "CARD_NO as AuthTxLogCardNo, AUTH_NO as AuthTxLogAuthNo, TRACE_NO as AuthTxLogTraceNo, TX_TIME as AuthTxLogTxTime, " 
				+"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
				+"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
				+"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
				+"from CCA_AUTH_TXLOG "
				+" WHERE CARD_NO= ? AND CACU_AMOUNT= ? and AUTH_NO= ? AND (NT_AMT>= ? AND NT_AMT<=? ) FETCH FIRST ? ROWS ONLY ";

		G_Ps4AuthTxLog3 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4AuthTxLog3

		//down, set G_Ps4AuthTxLog4
		sL_Sql = "select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
				+"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
				+"CARD_NO as AuthTxLogCardNo, AUTH_NO as AuthTxLogAuthNo,"
				+"NVL(TRACE_NO,'') as AuthTxLogTraceNo, NVL(TX_TIME,'') as AuthTxLogTxTime, "
				+"NVL(REF_NO,' ') as AuthTxLogRefNo,"
				+"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
				+"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
				+"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
				+"from CCA_AUTH_TXLOG "
				+" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND NT_AMT= ? FETCH FIRST ? ROWS ONLY ";


		G_Ps4AuthTxLog4 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4AuthTxLog4

		//down, set G_Ps4AuthTxLog5
		sL_Sql = "select NVL(CACU_AMOUNT,'N') as AuthTxLogCacuAmount ,NVL(TX_DATE,'00000000')  as AuthTxLogTxDate,"
				+"NVL(CACU_CASH,'N') as AuthTxLogCacuCash,NVL(TRANS_TYPE,'*') as AuthTxLogTransType,"
				+"NVL(REF_NO,' ') as AuthTxLogRefNo,"
				+"NVL(NT_AMT,0) as AuthTxLogNtAmt,"
				+"NVL(RISK_TYPE,'*') as AuthTxLogRiskType,NVL(PROC_CODE,'*') as AuthTxLogProcCode,"
				+"NVL(MCC_CODE,' ') as AuthTxLogMccCode,NVL(MCHT_NO,'*') as AuthTxLogMchtNo,NVL(CORP_FLAG,'N') as AuthTxLogCorpFlag "
				+"from CCA_AUTH_TXLOG "
				+" WHERE CARD_NO= ? AND CACU_AMOUNT= ? AND (NT_AMT>= ? AND NT_AMT<= ? ) FETCH FIRST ? ROWS ONLY ";



		G_Ps4AuthTxLog5 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4AuthTxLog5

	}
	private static void initCardAcctPs() {
		String sL_Sql = "select count(*) as CcaCardAcctCount "
				+ "from CCA_CARD_ACCT where CARD_ACCT_IDX= ? ";

		G_Ps4CardAcct2 = getPreparedStatement(sL_Sql, true);

	}

	private static void initCardAcctPs4Ecs100() {
		//Howard: 為測試，改為 insert to CCA_CARD_ACCT_H2
		//down, set G_Ps4CardAcct3
		String sL_Sql = "INSERT INTO CCA_CARD_ACCT(CARD_ACCT_IDX," 
				+ "TOT_AMT_CONSUME,TOT_AMT_PRECASH,"
				+ "ORGAN_ID,"
				+ "NOCANCEL_CREDIT_FLAG, P_SEQNO, DEBIT_FLAG, ID_P_SEQNO,CCAS_MCODE, ACNO_FLAG, MOD_TIME, MOD_PGM, ACNO_P_SEQNO, ACCT_TYPE, CORP_P_SEQNO)"
				+ " VALUES(?,?,?,?,?,?,?,?,?, ?, ?, ?, ?,?,? )";

		G_Ps4CardAcct3 = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CardAcct3


		//down, set G_Ps4CardAcct4


		/*
		 * 
		 * 
			String sL_Sql = "UPDATE CCA_CARD_ACCT set tot_amt_month = decode(" + h_temp_tot_amt_month + ",0," + h_temp_tot_amt_month + ",100," + h_temp_tot_amt_month + ","
            		+ "decode(sign(ceil((" + h_temp_lmt_tot_consume + "*" + h_temp_tot_amt_month + ")/" + h_a_lmt_tot_consume + ")-100),"
                    + "-1,100,ceil((" + h_temp_lmt_tot_consume + "*" + h_temp_tot_amt_month + ")/" + h_a_lmt_tot_consume + "))),"
                    + "adj_inst_pct = decode(" + h_temp_adj_inst_pct + ",0," + h_temp_adj_inst_pct + ",100," + h_temp_adj_inst_pct + ","
                    + "decode(sign(ceil((" + h_temp_lmt_tot_consume  + "*" + h_temp_adj_inst_pct + ")/" + h_a_lmt_tot_consume + ")-100),"
                   	+ "-1,100,ceil((" + h_temp_lmt_tot_consume + "*" + h_temp_adj_inst_pct + ")/" + h_a_lmt_tot_consume + ")))";

			sL_Sql += " WHERE CARD_ACCT_IDX  = ? " ;

		String sL_Sql = "UPDATE CCA_CARD_ACCT set tot_amt_month = decode(" + ? + ",0," + ? + ",100," + ? + ","
            		+ "decode(sign(ceil((" + ? + "*" + ? + ")/" + ? + ")-100),"
                    + "-1,100,ceil((" + ? + "*" + ? + ")/" + ? + "))),"
                    + "adj_inst_pct = decode(" + ? + ",0," + ? + ",100," + ? + ","
                    + "decode(sign(ceil((" + ?  + "*" + ? + ")/" + ? + ")-100),"
                   	+ "-1,100,ceil((" + ? + "*" + ? + ")/" + ? + ")))";



		h_temp_tot_amt_month == AA => 1,2,3,5,8
		h_temp_lmt_tot_consume == BB=>4,7,13,16

		h_temp_adj_inst_pct =CC=> 10,11,12,14,17

		h_a_lmt_tot_consume==DD=>6,9,15,18

		 * */
		sL_Sql = "UPDATE CCA_CARD_ACCT set tot_amt_month = decode( ? ,0, ? ,100, ? ,"
				+ "decode(sign(ceil(( ? * ? )/? )-100),"
				+ "-1,100,ceil((? * ? )/ ? ))),"
				+ "adj_inst_pct = decode( ? ,0,? ,100, ? ,"
				+ "decode(sign(ceil(( ? * ? )/ ? )-100),"
				+ "-1,100,ceil((? * ? + )/ ? )))";
		sL_Sql += " WHERE CARD_ACCT_IDX  = ? " ;

		G_Ps4CardAcct4 = getPreparedStatement(sL_Sql, true);

		//up, set G_Ps4CardAcct4



		//down, set G_Ps4CardAcct5 
		String sL_UpdateSql = "UPDATE CCA_CARD_ACCT set ORGAN_ID= ? , NOCANCEL_CREDIT_FLAG= ? , CCAS_MCODE= ? ,  "
				+ "MOD_TIME= ? , MOD_PGM= ?  where CARD_ACCT_IDX= ?  ";

		G_Ps4CardAcct5 = getPreparedStatement(sL_UpdateSql, false);
		//up, set G_Ps4CardAcct5		

	}

	private static void initCardAcctIndexPs() {
		String sL_Sql = "select CARD_ACCT_IDX as CcaCardAcctIdx_CardAcctIdx "
				//+ "from CCA_CARD_ACCT_INDEX where CARD_ACCT_ID= ? and ECS_ACCT_CLASS= ?  and NVL(ACCT_NO,'00000000000') = nvl( ? ,'00000000000') "; //h_a_acct_no == CCS_BASE.ACCT_NO
				+ "from CCA_CARD_ACCT_INDEX where CARD_ACCT_ID= ? and ACCT_TYPE= ?  and NVL(ACCT_NO,'00000000000') = nvl( ? ,'00000000000') "; //h_a_acct_no == CCS_BASE.ACCT_NO  // Howard: ECS_ACCT_CLASS change to ACCT_TYPE

		G_Ps4CardAcctIndex = getPreparedStatement(sL_Sql, true);



		sL_Sql = "select CARD_ACCT_IDX as CcaCardAcctIdx_CardAcctIdx "
				+ "from CCA_CARD_ACCT_INDEX "

				//+ "where CARD_ACCT_ID= ? and ECS_ACCT_CLASS= ? "
				+ "where CARD_ACCT_ID= ? and ACCT_TYPE= ? "// Howard: ECS_ACCT_CLASS change to ACCT_TYPE
				+ "or (CARD_CORP_ID= ? and NVL(CARD_CORP_ID_SEQ,'0') = NVL( ? ,'0'))"; 
		G_Ps4CardAcctIndex2 = getPreparedStatement(sL_Sql, true);
	}

	private static void initCardAcctIndexPs4Ecs100() {

		//down, set G_Ps4CardAcctIndex3
		String sL_Sql = "INSERT INTO CCA_CARD_ACCT_INDEX( "
				+ "CARD_ACCT_IDX,      CARD_ACCT_ID, "
				+ "CARD_ACCT_ID_SEQ,   CARD_CORP_ID,"
				+ "CARD_CORP_ID_SEQ,   CARD_ACCT_CLASS,"
				//+ "ECS_ACCT_CLASS ,    ACCT_NO,"
				+ "ACCT_TYPE ,    ACCT_NO,"// Howard: ECS_ACCT_CLASS change to ACCT_TYPE
				//+ "ACCT_PARENT_INDEX, DEBIT_FLAG  )"
				+ "ACCT_PARENT_INDEX )"				
				+ "VALUES ( ?,?,?,?,?,?,?,?,?)";

		G_Ps4CardAcctIndex3 = getPreparedStatement(sL_Sql, true);

		//up, set G_Ps4CardAcctIndex3

		//down, set G_Ps4CardAcctIndex4


		//Howard => 應該改以此sql 找出CARD_ACCT_IDX 再判斷有沒有找到值 => 

		sL_Sql = "SELECT MIN(CARD_ACCT_IDX) as MinCardAcctIdx, ACCT_PARENT_INDEX as AcctParentIndex, CARD_CORP_ID as CARDCORPID FROM CCA_CARD_ACCT_INDEX ";
		sL_Sql += " WHERE CARD_ACCT_ID = ? AND CARD_CORP_ID = ? and CARD_CORP_ID_SEQ != '' ";
		sL_Sql += " AND NVL(CARD_CORP_ID_SEQ,'00000') = NVL(? ,'00000') ";
		//sL_Sql += " AND ECS_ACCT_CLASS = ? ";
		sL_Sql += " AND ACCT_TYPE = ? ";// Howard: ECS_ACCT_CLASS change to ACCT_TYPE
		sL_Sql += " AND NVL(ACCT_NO,'00000000000000000000') = nvl(?,'00000000000000000000')" ;
		sL_Sql += "group by ACCT_PARENT_INDEX, CARD_CORP_ID ";

		/*
		sL_Sql = "select A.CARD_ACCT_IDX as CARDACCTIDX,A.ACCT_PARENT_INDEX as ACCTPARENTIDX, A.CARD_ACCT_ID as CARDACCTID,  A.CARD_CORP_ID as CARDCORPID, SUM(DECODE(TRANS_TYPE,'11',1,0)) as TRANSTYPE1,  SUM(DECODE(TRANS_TYPE,'17',1,0)) as TRANSTYPE2, "
				 + " C.Debit_Flag as DEBITFLAG, C.P_SEQNO as PSEQNO, C.ID_P_SEQNO as PIDSEQNO "
				+ "from CCA_CARD_ACCT_INDEX A,CCA_CARD_BASE C, ONBAT_2CCAS E ";
		sL_Sql += " where A.CARD_ACCT_ID= ? AND   A.CARD_CORP_ID= ? ";
		sL_Sql += " AND   NVL(A.CARD_CORP_ID_SEQ,'0') = NVL( ? ,'0') AND ECS_ACCT_CLASS = ? ";
		sL_Sql += " AND NVL(A.ACCT_NO,'00000000000') = nvl( ? ,'00000000000') ";

		sL_Sql += " AND A.CARD_ACCT_IDX = (SELECT MIN(CARD_ACCT_IDX) FROM CCA_CARD_ACCT_INDEX ";
		sL_Sql += " WHERE CARD_ACCT_ID = ? AND   CARD_CORP_ID = ? and CARD_CORP_ID_SEQ != '' ";
		sL_Sql += " AND   NVL(CARD_CORP_ID_SEQ,'0') = NVL( ? ,'0') AND ECS_ACCT_CLASS = ? and ";
		sL_Sql += " AND NVL(ACCT_NO,'00000000000') = nvl( ?,'00000000000')) ";
		sL_Sql += " AND A.CARD_ACCT_IDX = C.CARD_ACCT_IDX(+) ";
		sL_Sql += " AND C.CARD_NO = E.CARD_NO(+) ";
		sL_Sql += " AND E.PROC_STATUS(+) = 0 ";
		sL_Sql += " GROUP BY A.CARD_ACCT_IDX ,A.ACCT_PARENT_INDEX, A.CARD_ACCT_ID,  A.CARD_CORP_ID ";
		 */
		G_Ps4CardAcctIndex4 = getPreparedStatement(sL_Sql, true);

		//up, set G_Ps4CardAcctIndex4
	}

	private static void initCardAcctLogPs() {

		//down, set G_Ps4CardAcct		

		String sL_Sql = "select ROWID as CardAcctRowId,"
				+"NVL(tot_amt_month,0) as totamtmonth,NVL(adj_inst_pct,0) as adjinstpct,"
				+"NVL(ADJ_QUOTA,'N') as ADJQUOTA, NVL(ADJ_EFF_START_DATE,'00000000') as ADJEFFSTARTDATE,"
				+"NVL(ADJ_EFF_END_DATE,'00000000') as AdjEffEndDate, ADJ_REASON, ADJ_REMARK, ADJ_AREA,ADJ_DATE,ADJ_TIME, ADJ_USER," 
				+"NVL(TOT_AMT_CONSUME,0)  as CardAcctTotAmtConsume, NVL(TOT_AMT_PRECASH,0) as CardAcctTotAmtPreCash, "  
				+"P_SEQNO as CardAcctPSeqNo, DEBIT_FLAG as CardAcctDebitFlag, ID_P_SEQNO as CardAcctIdPSeqNo, "
				+"BLOCK_REASON1,BLOCK_REASON2,BLOCK_REASON3,BLOCK_REASON4,BLOCK_REASON5, CARD_ACCT_IDX ";


		sL_Sql += " from CCA_CARD_ACCT where ACNO_P_SEQNO= ? and DEBIT_FLAG= ? ";

		G_Ps4CardAcct = getPreparedStatement(sL_Sql, true);
		//up, set G_Ps4CardAcct
		
	      //down, set G_Ps4CardAcct6       

        sL_Sql = "select ROWID as CardAcctRowId,"
                +"NVL(tot_amt_month,0) as totamtmonth,NVL(adj_inst_pct,0) as adjinstpct,"
                +"NVL(ADJ_QUOTA,'N') as ADJQUOTA, NVL(ADJ_EFF_START_DATE,'00000000') as ADJEFFSTARTDATE,"
                +"NVL(ADJ_EFF_END_DATE,'00000000') as AdjEffEndDate, ADJ_REASON, ADJ_REMARK, ADJ_AREA,ADJ_DATE,ADJ_TIME, ADJ_USER," 
                +"NVL(TOT_AMT_CONSUME,0)  as CardAcctTotAmtConsume, NVL(TOT_AMT_PRECASH,0) as CardAcctTotAmtPreCash, "  
                +"P_SEQNO as CardAcctPSeqNo, DEBIT_FLAG as CardAcctDebitFlag, ID_P_SEQNO as CardAcctIdPSeqNo, "
                +"BLOCK_REASON1,BLOCK_REASON2,BLOCK_REASON3,BLOCK_REASON4,BLOCK_REASON5, CARD_ACCT_IDX ";


        sL_Sql += " from CCA_CARD_ACCT where CARD_ACCT_IDX = ? ";

        G_Ps4CardAcct6 = getPreparedStatement(sL_Sql, true);
        //up, set G_Ps4CardAcct6
        


		//down, set G_Ps4CardAcctUpdated 
		String sL_UpdateSql = "UPDATE CCA_CARD_ACCT set TOT_AMT_CONSUME= ? , TOT_AMT_PRECASH= ? , "
				+ "MOD_TIME= ? , MOD_PGM= ?  where CARD_ACCT_IDX= ?  ";

		G_Ps4CardAcctUpdated = getPreparedStatement(sL_UpdateSql, false);
		//up, set G_Ps4CardAcctUpdated		
	}

	public static void initPrepareStatement(String sP_ProgId) {
		if (sP_ProgId.equals(G_ECS050IDFor17)) {
			initCardBasePs4CardAcctIndex();
			initCcaConsumePs4Update();
		}

		if (sP_ProgId.equals(G_ECS060ID)) {
			initCardBasePs();
			initAuthTxLogPs();
			initCardAcctLogPs();
			initSystemParm1Ps();
			initCcaUnMatchPs();

		}
		if (sP_ProgId.equals(G_ECS080ID)) {
			
			initCardBasePs();
			initCardAcctIndexPs();
			initCardAcctPs();
			initCardAcctLogPs();
			initCardBasePs2();
			initSystemParm2Ps();
			
		}

		if (sP_ProgId.equals(G_ECS100ID)) {
			initCardAcctIndexPs4Ecs100();
			initCardAcctPs4Ecs100();
			initCardAcctLogPs();
			initSystemParm2Ps();
			initCrdIdnoPs();
			initCreditLogPs();
			initActAcnoPs();
			initSelectSequenceValue();
			initCcaConsumePs();
		}


	}
	public static void releaseConnection(ResultSet P_Rs) throws Exception{
		P_Rs.close();
		P_Rs.getStatement().close();




	}

	public static void closeResource(ResultSet P_Rs) throws Exception{
		P_Rs.close();
		//P_Rs.getStatement().close();




	}

	public static String getNextSeqValOfDb2(Connection P_Connection, String sP_SequenceName)  throws Exception {
		//get sequence value

		String sL_SeqVal = "0";
		try {


			//String sL_Sql = " select VALUES NEXTVAL FOR  "+ sP_SequenceName  ;
			String sL_Sql = "  SELECT NEXT VALUE FOR " + sP_SequenceName + " FROM sysibm.sysdummy1 "  ;

			////System.out.println("getNextSeqVal sql:" + sL_Sql + "==");

			/* worked
			java.sql.Statement Db2Stmt = P_Connection.createStatement();
			ResultSet L_ResultSet = Db2Stmt.executeQuery(sL_Sql);
			//System.out.println("a2");

			if (L_ResultSet.next()) {
				sL_SeqVal = L_ResultSet.getString(1);
				//System.out.println("a3");
			}
			 */



			PreparedStatement Db2Stmt = P_Connection.prepareStatement(sL_Sql);	


			ResultSet L_ResultSet = Db2Stmt.executeQuery();


			if (L_ResultSet.next()) {
				sL_SeqVal = L_ResultSet.getString(1);

			}


			L_ResultSet.close();

			Db2Stmt.close();


		} catch (Exception e) {
			// TODO: handle exception
			////System.out.println("getNextSeqVal exception:" + e.getMessage());
			sL_SeqVal = "0";
		}
		return sL_SeqVal;

	}

	public static boolean commitDatabase() {
		boolean bL_Result = true;
		try 
		{  

			// Db2Connection.commit();
		    connections.get().commit();
			writeLog("I", "Db commit!");

		}	
		catch(SQLException ex)                                                      
		{
			bL_Result=false;
			writeLog("E", "SQLException information");

			while(ex!=null) {
				writeLog("E", "Error msg: " + ex.getMessage());
				writeLog("E", "SQLSTATE: " + ex.getSQLState());
				writeLog("E", "Error code: " + ex.getErrorCode());
				//ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}

		return bL_Result;
	}

	public static boolean rollbackDatabase() {
		boolean bL_Result = true;
		try 
		{  

			// Db2Connection.rollback();
		    connections.get().rollback();
			writeLog("I", "Db rollback!");

		}	
		catch(SQLException ex)                                                      
		{
			bL_Result=false;
			writeLog("E", "SQLException information");

			while(ex!=null) {
				writeLog("E", "Error msg: " + ex.getMessage());
				writeLog("E", "SQLSTATE: " + ex.getSQLState());
				writeLog("E", "Error code: " + ex.getErrorCode());
				//ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}

		return bL_Result;
	}

	public static boolean closeDatabase() {
		boolean bL_Result = true;
		try  {
		

			// Db2Connection.close();
		    connections.get().close();
		    connections.remove();
			writeLog("I", "Db closed!");

		}	
		catch(SQLException ex)                                                      
		{
			bL_Result=false;
			writeLog("E", "SQLException information");

			while(ex!=null) {
				writeLog("E", "Error msg: " + ex.getMessage());
				writeLog("E", "SQLSTATE: " + ex.getSQLState());
				writeLog("E", "Error code: " + ex.getErrorCode());
				//ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}

		return bL_Result;
	}

	protected static PreparedStatement getPreparedStatement(String sP_Sql, boolean bP_ReadOnly) {

		PreparedStatement ps = null;
		try {

			//www.dayexie.com/detail1510024.html

			//ps = Db2Connection.prepareStatement(sP_Sql);			



			if (bP_ReadOnly)
				//ps = Db2Connection.prepareStatement(sP_Sql, ResultSet.TYPE_FORWARD_ONLY);
				// ps = Db2Connection.prepareStatement(sP_Sql);
			    ps = connections.get().prepareStatement(sP_Sql);

			else
				//ps = Db2Connection.prepareStatement(sP_Sql);
			    ps = connections.get().prepareStatement(sP_Sql);
			//ps = Db2Connection.prepareStatement(sP_Sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE,ResultSet.HOLD_CURSORS_OVER_COMMIT);

		} catch (Exception e) {
			// TODO: handle exception
			////System.out.println("getPreparedStatement exception=>" +e.getMessage());
		}


		return ps;
	}
	public static boolean setDatabaseConn(Connection P_Connection) {
		boolean bL_Result = true;
		try {
			// Db2Connection = P_Connection;
		    connections.set(P_Connection);
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}

		return bL_Result;

	}

//	public static void main(String[] args) {
//		/*
//		if (connDatabaseTest("10.5.109.3", "60000", "testdb", "sdbsa02", "mega0011"))
//			//System.out.println("ok");
//		else
//			//System.out.println("not ok");
//		 */
//
//		//getResultRecCount();
//	}

//	public static boolean connDatabaseTest(String sP_ServerIp, String sP_ServerPort, String sP_DbName, String sP_UserId, String sP_UesrHideWd) {
//		boolean bL_Result = true;
//		String urlPrefix = "jdbc:db2:";
//
//		String url= urlPrefix + "//"+ sP_ServerIp + ":" + sP_ServerPort + "/" + sP_DbName;
//		String user= sP_UserId;
//		String sL_HideWd= sP_UesrHideWd;
//		try 
//		{  
//			// Load the driver
//			//Marked By Howard(11/13)Class.forName("com.p6spy.engine.spy.P6SpyDriver");
//			//Class.forName("com.ibm.db2.jcc.DB2Driver");                              
//			////System.out.println("**** Loaded the JDBC driver");
//
//			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
//			// Db2Connection = DriverManager.getConnection (url, user, sL_HideWd);
//		    Connection con = DriverManager.getConnection (url, user, sL_HideWd);
//			//Commit changes manually
//			// Db2Connection.setAutoCommit(false);
//		    con.setAutoCommit(false);
//		    connections.set(con);
//		    
//			writeLog("I", "Db connected!");
//
//
//
//
//
//		}
//		/* Marked By Howard(11/13)
//		catch (ClassNotFoundException e)
//	    {
//			bL_Result=false;
//			writeLog("E", "Could not load JDBC driver");			
//			writeLog("E", "Exception: " + e);
//			e.printStackTrace();
//	    }
//		 */
//		catch(SQLRecoverableException ex)                                                      
//		{
//			bL_Result=false;
//		}
//
//		catch(SQLException ex)                                                      
//		{
//			bL_Result=false;
//			writeLog("E", "SQLException information");
//			while(ex!=null) {
//				writeLog("E", "Error msg: " + ex.getMessage());
//				writeLog("E", "SQLSTATE: " + ex.getSQLState());
//				writeLog("E", "Error code: " + ex.getErrorCode());
//
//				//ex.printStackTrace();
//				ex = ex.getNextException(); // For drivers that support chained exceptions
//			}
//		}
//		return bL_Result;
//	}
//
	public static boolean connDatabase(String sP_ServerIp, String sP_ServerPort, String sP_DbName, String sP_UserId, String sP_UesrHideWd) {
		boolean bL_Result = true;
		String urlPrefix = "jdbc:db2:";

		String url= urlPrefix + "//"+ sP_ServerIp + ":" + sP_ServerPort + "/" + sP_DbName;
		String user= sP_UserId;
		String sL_HideWd = sP_UesrHideWd;
		try 
		{  
			// Load the driver
			//Marked By Howard(11/13)Class.forName("com.p6spy.engine.spy.P6SpyDriver");
			//Class.forName("com.ibm.db2.jcc.DB2Driver");                              
			////System.out.println("**** Loaded the JDBC driver");

			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
			// Db2Connection = DriverManager.getConnection (url, user, sL_HideWd);                 
			Connection con = DriverManager.getConnection (url, user, sL_HideWd);
			//Commit changes manually
			// Db2Connection.setAutoCommit(false);
			con.setAutoCommit(false);
			
			connections.set(con);
			
			writeLog("I", "Db connected!");





		}
		/* Marked By Howard(11/13)
		catch (ClassNotFoundException e)
	    {
			bL_Result=false;
			writeLog("E", "Could not load JDBC driver");			
			writeLog("E", "Exception: " + e);
			e.printStackTrace();
	    }
		 */
		catch(SQLRecoverableException ex)                                                      
		{
			bL_Result=false;
		}

		catch(SQLException ex)                                                      
		{
			bL_Result=false;
			writeLog("E", "SQLException information");
			while(ex!=null) {
				writeLog("E", "Error msg: " + ex.getMessage());
				writeLog("E", "SQLSTATE: " + ex.getSQLState());
				writeLog("E", "Error code: " + ex.getErrorCode());

				//ex.printStackTrace();
				ex = ex.getNextException(); // For drivers that support chained exceptions
			}
		}
		return bL_Result;
	}
//
//
//	public static boolean connDatabase_PureDb2(String sP_ServerIp, String sP_ServerPort, String sP_DbName, String sP_UserId, String sP_HideWd) {
//		boolean bL_Result = true;
//		String urlPrefix = "jdbc:db2:";
//
//		String url= urlPrefix + "//"+ sP_ServerIp + ":" + sP_ServerPort + "/" + sP_DbName;
//		String user= sP_UserId;
//		String sL_HideWd= sP_HideWd;
//		try 
//		{  
//			// Load the driver
//			//Marked By Howard(11/13)   Class.forName("com.ibm.db2.jcc.DB2Driver");                              
//			////System.out.println("**** Loaded the JDBC driver");
//
//			// Create the connection using the IBM Data Server Driver for JDBC and SQLJ
//			// Db2Connection = DriverManager.getConnection (url, user, sL_HideWd);                 
//			Connection con = DriverManager.getConnection (url, user, sL_HideWd);
//			//Commit changes manually
//			// Db2Connection.setAutoCommit(false);               
//            con.setAutoCommit(false);
//            
//            connections.set(con);
//            
//			writeLog("I", "Db connected!");
//
//
//
//
//
//		}
//		/* Marked By Howard(11/13)
//		catch (ClassNotFoundException e)
//	    {
//			bL_Result=false;
//			writeLog("E", "Could not load JDBC driver");			
//			writeLog("E", "Exception: " + e);
//			e.printStackTrace();
//	    }
//		 */
//		catch(SQLRecoverableException ex)                                                      
//		{
//			bL_Result=false;
//		}
//
//		catch(SQLException ex)                                                      
//		{
//			bL_Result=false;
//			writeLog("E", "SQLException information");
//			while(ex!=null) {
//				writeLog("E", "Error msg: " + ex.getMessage());
//				writeLog("E", "SQLSTATE: " + ex.getSQLState());
//				writeLog("E", "Error code: " + ex.getErrorCode());
//
//				//ex.printStackTrace();
//				ex = ex.getNextException(); // For drivers that support chained exceptions
//			}
//		}
//		return bL_Result;
//	}

}
