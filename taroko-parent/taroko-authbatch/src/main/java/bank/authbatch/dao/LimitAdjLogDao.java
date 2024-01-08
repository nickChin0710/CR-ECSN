package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

//import org.omg.CORBA.portable.ValueInputStream;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;
import bank.authbatch.vo.CardAcctIndexVo;
import bank.authbatch.vo.CardAcctVo;
import bank.authbatch.vo.CcaCardBaseVo;

public class LimitAdjLogDao extends AuthBatchDbHandler{

	public LimitAdjLogDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	

	public static boolean insertLimitAdjLog(CardAcctVo P_CardAcctVo,   String sP_ProgName, String sP_CardAcctId, int nP_LmtTotConsume) {	
		//proc is insert_acct_limit_adj_log_16() and insert_acct_limit_adj_log_12()
		boolean bL_Result = true;
		
		try {
			//String sL_CorrelateId = P_CcsAccountRs.getString("CARD_ACCT_ID");
			String sL_CorrelateId = sP_CardAcctId;
			
			String sL_FhFlag= CrdCorrelateDao.getFhFlag(sL_CorrelateId);
			
			if ("".equals(sL_FhFlag)) {
				bL_Result = false;
			}
			else {
				String sL_CurDate= HpeUtil.getCurDateStr("");
				String sL_CurTime = HpeUtil.getCurTimeStr();
				//insert into CCA_LIMIT_ADJ_LOG 授權額度臨調異動記錄檔
				String sL_Sql = "insert into CCA_LIMIT_ADJ_LOG(LOG_DATE, LOG_TIME, AUD_CODE, CARD_ACCT_IDX, DEBIT_FLAG, P_SEQNO,  ID_P_SEQNO,";
					   sL_Sql += "MOD_TYPE,  RELA_FLAG, LMT_TOT_CONSUME, TOT_AMT_MONTH_B, TOT_AMT_MONTH,";
					   sL_Sql += "ADJ_INST_PCT_B, ADJ_INST_PCT,  ADJ_EFF_DATE1, ADJ_EFF_DATE2,";
					   sL_Sql += "ADJ_REASON, ADJ_REMARK, ADJ_AREA, ECS_ADJ_RATE, ADJ_USER,  ADJ_DATE,ADJ_TIME)";
					   //sL_Sql +=  "values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:p13,:p14,:p15,:p16,:p17,:p18,:p19,:p20,:p21,:p22,:p23)";
					   sL_Sql +=  "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						
						//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
					   PreparedStatement ps = getPreparedStatement(sL_Sql, true);
						ps.setString(1, sL_CurDate); //LOG_DATE
						ps.setString(2, sL_CurTime); //LOG_TIME
						ps.setString(3, "U"); //AUD_CODE
						ps.setInt(4, P_CardAcctVo.getCardAcctIdx() ); //CARD_ACCT_IDX
						ps.setString(5, P_CardAcctVo.getDebitFlag()); //DEBIT_FLAG 
						ps.setString(6, P_CardAcctVo.getPSeqNo()); //P_SEQNO
						ps.setString(7, P_CardAcctVo.getIdPSeqNo()); //ID_P_SEQNO
						ps.setString(8, "2"); //MOD_TYPE
						ps.setString(9, sL_FhFlag); //RELA_FLAG
						
						//ps.setString("p10", P_CcsAccountRs.getString("CcsAccountLmtTotConsume")); //LMT_TOT_CONSUME
						ps.setInt(10, nP_LmtTotConsume); //LMT_TOT_CONSUME
						
						ps.setString(11, P_CardAcctVo.getTotAmtMonth()); //TOT_AMT_MONTH_B
						
						ps.setString(12, P_CardAcctVo.getTotAmtMonth()); //TOT_AMT_MONTH
						ps.setString(13, P_CardAcctVo.getAdjInstPct()); //ADJ_INST_PCT_B
						ps.setString(14, P_CardAcctVo.getAdjInstPct()); //ADJ_INST_PCT
						ps.setString(15, P_CardAcctVo.getAdjEffStartDate() ); //ADJ_EFF_DATE1
						ps.setString(16, P_CardAcctVo.getAdjEffEndDate() ); //ADJ_EFF_DATE2
						ps.setString(17, P_CardAcctVo.getAdjReason()); //ADJ_REASON 
						ps.setString(18, P_CardAcctVo.getAdjRemark()); //ADJ_REMARK
						ps.setString(19, P_CardAcctVo.getAdjArea()); //ADJ_AREA
						ps.setInt(20, 0); //ECS_ADJ_RATE
						ps.setString(21, sP_ProgName); //ADJ_USER
						ps.setString(22, sL_CurDate); //ADJ_DATE
						ps.setString(23, sL_CurTime); //ADJ_TIME
						
						//要釐清上述欄位值如何取得...  
						//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

					
						ps.executeUpdate();
						ps.close();
			            
			            

						
			}
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

	public static boolean insertLimitAdjLog12(CardAcctVo P_CardAcctVo, int nP_LmtTotConsume, String sP_OnBatCardAcctId) {
		//proc is insert_acct_limit_adj_log_12()
		boolean bL_Result = true;
		
		try {
					
			//String sL_CorrelateId = P_CcsAccountRs.getString("CARD_ACCT_ID");
			String sL_CorrelateId = sP_OnBatCardAcctId;
			
			String sL_FhFlag= CrdCorrelateDao.getFhFlag(sL_CorrelateId);
			
			if ("".equals(sL_FhFlag)) {
				bL_Result = false;
			}
			else {
				String sL_CurDate= HpeUtil.getCurDateStr("");
				String sL_CurTime = HpeUtil.getCurTimeStr();
				//insert into CCA_LIMIT_ADJ_LOG 授權額度臨調異動記錄檔
				String sL_Sql = "insert into CCA_LIMIT_ADJ_LOG(LOG_DATE, LOG_TIME, AUD_CODE, CARD_ACCT_IDX, DEBIT_FLAG, P_SEQNO,  ID_P_SEQNO,";
					   sL_Sql += "MOD_TYPE,  RELA_FLAG, LMT_TOT_CONSUME, TOT_AMT_MONTH_B, TOT_AMT_MONTH,";
					   sL_Sql += "ADJ_INST_PCT_B, ADJ_INST_PCT,  ADJ_EFF_DATE1, ADJ_EFF_DATE2,";
					   sL_Sql += "ADJ_REASON, ADJ_REMARK, ADJ_AREA, ECS_ADJ_RATE, ADJ_USER,  ADJ_DATE,ADJ_TIME)";
					   //sL_Sql +=  "values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:p13,:p14,:p15,:p16,:p17,:p18,:p19,:p20,:p21,:p22,:p23)";
					   sL_Sql +=  "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						
						//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
					   PreparedStatement ps = getPreparedStatement(sL_Sql, true);
						ps.setString(1, sL_CurDate); //LOG_DATE
						ps.setString(2, sL_CurTime); //LOG_TIME
						ps.setString(3, "U"); //AUD_CODE
						ps.setInt(4, P_CardAcctVo.getCardAcctIdx() ); //CARD_ACCT_IDX
						
						ps.setString(5, P_CardAcctVo.getDebitFlag()); //DEBIT_FLAG 
						ps.setString(6, P_CardAcctVo.getPSeqNo()); //P_SEQNO
						ps.setString(7, P_CardAcctVo.getIdPSeqNo()); //ID_P_SEQNO
						ps.setString(8, "2"); //MOD_TYPE
						ps.setString(9, sL_FhFlag); //RELA_FLAG
						
						//ps.setString("p10", P_CcsAccountRs.getString("CcsAccountLmtTotConsume")); //LMT_TOT_CONSUME
						ps.setInt(10, nP_LmtTotConsume); //LMT_TOT_CONSUME
						
						ps.setString(11, P_CardAcctVo.getTotAmtMonth()); //TOT_AMT_MONTH_B
						
						ps.setString(12, P_CardAcctVo.getTotAmtMonth()); //TOT_AMT_MONTH
						ps.setString(13, P_CardAcctVo.getAdjInstPct()); //ADJ_INST_PCT_B
						ps.setString(14, P_CardAcctVo.getAdjInstPct()); //ADJ_INST_PCT
						ps.setString(15, P_CardAcctVo.getAdjEffStartDate() ); //ADJ_EFF_DATE1
						ps.setString(16, P_CardAcctVo.getAdjEffEndDate() ); //ADJ_EFF_DATE2
						ps.setString(16, P_CardAcctVo.getAdjReason()); //ADJ_REASON 
						ps.setString(18, P_CardAcctVo.getAdjRemark()); //ADJ_REMARK
						ps.setString(19, P_CardAcctVo.getAdjArea()); //ADJ_AREA
						ps.setInt(20, 0); //ECS_ADJ_RATE
						ps.setString(21, "ECS100"); //ADJ_USER
						ps.setString(22, sL_CurDate); //ADJ_DATE
						ps.setString(23, sL_CurTime); //ADJ_TIME
						
						//要釐清上述欄位值如何取得...  
						//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

					
						ps.executeUpdate();
						ps.close();
			            
			            

						
			}
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

}
