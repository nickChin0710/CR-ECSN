package bank.authbatch.dao;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;

import bank.authbatch.main.HpeUtil;
import bank.authbatch.vo.CcaCardBaseVo;
import bank.authbatch.vo.Data004Vo;
import bank.authbatch.vo.Data080Vo;
import bank.authbatch.vo.CcaBaseVo;

public class CcaCardBaseDao extends AuthBatchDbHandler{

	public CcaCardBaseDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	
	public static boolean deleteCardBase(String sP_CardAcctIdx, String sP_CardNo) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "DELETE CCA_CARD_BASE WHERE CARD_ACCT_IDX = ? and CARD_NO= ? ";
			
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
	        ps.setString(1, sP_CardAcctIdx);
	        ps.setString(2, sP_CardNo);
	         
	        ps.executeUpdate();
	        ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
		
	}

	public static boolean insertCcaCardBase(CcaBaseVo P_CcaBaseVo, int nP_CardAcctIndex) {
		boolean bL_Result = true;
		try {
			String sL_Sql="";
			sL_Sql = "insert into CCA_CARD_BASE(card_no,debit_flag, bin_type, id_p_seqno, p_seqno, gp_no, CORP_P_SEQNO, MAJOR_ID_P_SEQNO, ACNO_FLAG,  CARD_INDICATOR, onus_opp_type, voice_open_code, voice_auth_code, voice_open_code2, voice_auth_code2,old_pin,card_acct_idx,DC_CURR_CODE ) ";
			sL_Sql += "values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, P_CcaBaseVo.getCardNo()); //card_no =>P_CcaBaseVo.getCardNo()
			ps.setString(2, P_CcaBaseVo.getDebitFlag()); //debit_flag => P_CcaBaseVo.getDebitFlag()
			ps.setString(3, P_CcaBaseVo.getBinType()); ////bin_type => P_CcaBaseVo.getBinType()
			ps.setString(4, P_CcaBaseVo.getIdPSqno()); //id_p_seqno=>P_CcaBaseVo.getIdPSqno()
			ps.setString(5, P_CcaBaseVo.getPSeqNo()); //p_seqno => P_CcaBaseVo.getPSeqNo()
			ps.setString(6, P_CcaBaseVo.getGpNo() ); //gp_no  
			ps.setString(7, P_CcaBaseVo.getCorpPSeqN0() ); //CORP_P_SEQNO 
			ps.setString(8, P_CcaBaseVo.getMajorIdPSeqNo()); //MAJOR_ID_P_SEQNO 
			ps.setString(9, P_CcaBaseVo.getAcnoFlag()); //ACNO_FLAG 
			ps.setString(10, P_CcaBaseVo.getComboIndicator()); //CARD_INDICATOR =>P_CcaBaseVo.getComboIndicator()
			ps.setString(11, ""); //onus_opp_type=> On Us 停掛類別
			
			ps.setString(12, P_CcaBaseVo.getPinOfActive()); //voice_open_code => P_CcaBaseVo.getPinOfActive()
			ps.setString(13, P_CcaBaseVo.getPinOfVoice()); //voice_auth_code, => P_CcaBaseVo.getPinOfVoice()
			ps.setString(14, P_CcaBaseVo.getPinOfActive()); //voice_open_code2, => P_CcaBaseVo.getPinOfActive()
			ps.setString(15, P_CcaBaseVo.getPinOfVoice()); //voice_auth_code2  => P_CcaBaseVo.getPinOfVoice()
			ps.setString(16, P_CcaBaseVo.getPinBlock()); //old_pin => P_CcaBaseVo.getPinBlock()

			ps.setInt(17, nP_CardAcctIndex); //card_acct_idx 
			
			ps.setString(18, P_CcaBaseVo.getDcCurrCode()); //DC_CURR_CODE => P_CcaBaseVo.getDcCurrCode()
			
		  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			ps.executeUpdate();
			ps.close();
					 
					
					
							
					         
					    
					     
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

	public static boolean insertCcaCardBase(Data080Vo P_Data080Vo, int nP_CardAcctIndex) {
		boolean bL_Result = true;
		try {
			/*
			String sL_Sql="";
			
			sL_Sql = "insert into CCA_CARD_BASE(card_no,debit_flag, bin_type, id_p_seqno, p_seqno, gp_no, CORP_P_SEQNO, MAJOR_ID_P_SEQNO, ACNO_FLAG,  CARD_INDICATOR, onus_opp_type, voice_open_code, voice_auth_code, voice_open_code2, voice_auth_code2,old_pin,card_acct_idx,DC_CURR_CODE ) ";
			sL_Sql += "values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, P_Data080Vo.getCardNo()); //card_no =>P_CcaBaseVo.getCardNo()
			ps.setString(2, P_Data080Vo.getDebitFlag()); //debit_flag => P_CcaBaseVo.getDebitFlag()
			ps.setString(3, P_Data080Vo.getBinType()); ////bin_type => P_CcaBaseVo.getBinType()
			ps.setString(4, P_Data080Vo.getIdPSqno()); //id_p_seqno=>P_CcaBaseVo.getIdPSqno()
			ps.setString(5, P_Data080Vo.getPSeqNo()); //p_seqno => P_CcaBaseVo.getPSeqNo()
			ps.setString(6, P_Data080Vo.getGpNo() ); //gp_no  
			ps.setString(7, P_Data080Vo.getCorpPSeqN0() ); //CORP_P_SEQNO 
			ps.setString(8, P_Data080Vo.getMajorIdPSeqNo()); //MAJOR_ID_P_SEQNO 
			ps.setString(9, P_Data080Vo.getAcnoFlag()); //ACNO_FLAG 
			ps.setString(10, P_Data080Vo.getComboIndicator()); //CARD_INDICATOR =>P_CcaBaseVo.getComboIndicator()
			ps.setString(11, ""); //onus_opp_type=> On Us 停掛類別
			
			ps.setString(12, P_Data080Vo.getPinOfActive()); //voice_open_code => P_CcaBaseVo.getPinOfActive()
			ps.setString(13, P_Data080Vo.getPinOfVoice()); //voice_auth_code, => P_CcaBaseVo.getPinOfVoice()
			ps.setString(14, P_Data080Vo.getPinOfActive()); //voice_open_code2, => P_CcaBaseVo.getPinOfActive()
			ps.setString(15, P_Data080Vo.getPinOfVoice()); //voice_auth_code2  => P_CcaBaseVo.getPinOfVoice()
			ps.setString(16, P_Data080Vo.getPinBlock()); //old_pin => P_CcaBaseVo.getPinBlock()

			ps.setInt(17, nP_CardAcctIndex); //card_acct_idx 
			
			ps.setString(18, P_Data080Vo.getDcCurrCode()); //DC_CURR_CODE => P_CcaBaseVo.getDcCurrCode()
			
		  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			ps.executeUpdate();
			ps.close();
			*/		 
					
			G_Ps4CardBase2.setString(1, P_Data080Vo.getCardNo()); //card_no =>P_CcaBaseVo.getCardNo()
			G_Ps4CardBase2.setString(2, P_Data080Vo.getDebitFlag()); //debit_flag => P_CcaBaseVo.getDebitFlag()
			G_Ps4CardBase2.setString(3, P_Data080Vo.getBinType()); ////bin_type => P_CcaBaseVo.getBinType()
			G_Ps4CardBase2.setString(4, P_Data080Vo.getIdPSqno()); //id_p_seqno=>P_CcaBaseVo.getIdPSqno()
			G_Ps4CardBase2.setString(5, P_Data080Vo.getPSeqNo()); //p_seqno => P_CcaBaseVo.getPSeqNo()
			G_Ps4CardBase2.setString(6, P_Data080Vo.getAcnoPSeqNo()); 
			G_Ps4CardBase2.setString(7, P_Data080Vo.getCorpPSeqN0() ); //CORP_P_SEQNO 
			G_Ps4CardBase2.setString(8, P_Data080Vo.getMajorIdPSeqNo()); //MAJOR_ID_P_SEQNO 
			G_Ps4CardBase2.setString(9, P_Data080Vo.getAcnoFlag()); //ACNO_FLAG 
			G_Ps4CardBase2.setString(10, P_Data080Vo.getComboIndicator()); //CARD_INDICATOR =>P_CcaBaseVo.getComboIndicator()
			G_Ps4CardBase2.setString(11, ""); //onus_opp_type=> On Us 停掛類別
			
			G_Ps4CardBase2.setString(12, P_Data080Vo.getPinOfActive()); //voice_open_code => P_CcaBaseVo.getPinOfActive()
			G_Ps4CardBase2.setString(13, P_Data080Vo.getPinOfVoice()); //voice_auth_code, => P_CcaBaseVo.getPinOfVoice()
			G_Ps4CardBase2.setString(14, P_Data080Vo.getPinOfActive()); //voice_open_code2, => P_CcaBaseVo.getPinOfActive()
			G_Ps4CardBase2.setString(15, P_Data080Vo.getPinOfVoice()); //voice_auth_code2  => P_CcaBaseVo.getPinOfVoice()
			G_Ps4CardBase2.setString(16, P_Data080Vo.getPinBlock()); //old_pin => P_CcaBaseVo.getPinBlock()

			G_Ps4CardBase2.setInt(17, nP_CardAcctIndex); //card_acct_idx 
			
			G_Ps4CardBase2.setString(18, P_Data080Vo.getDcCurrCode()); //DC_CURR_CODE => P_CcaBaseVo.getDcCurrCode()

			G_Ps4CardBase2.setTimestamp(19, HpeUtil.getCurTimestamp()); 
            G_Ps4CardBase2.setString(20, "ECS080"); 
            G_Ps4CardBase2.setString(21, P_Data080Vo.getAccountType());  //ACCT_TYPE
			
		  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			G_Ps4CardBase2.executeUpdate();					
							
					         
					    
					     
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception on insertCcaCardBase()=>" + e.getMessage());
			e.printStackTrace(System.out);
			bL_Result = false;
		}
		return bL_Result;
	}

	public static boolean updateCcaCardBase(CcaBaseVo P_CcaBaseVo) {
		//proc is UPDATE_CARD_BASE()
		boolean bL_Result = true;
		try {
			BigDecimal L_ModSeq = new BigDecimal(1); //for test
			Date L_CurDate =  HpeUtil.getCurDate4Sql();
			String sL_Sql="";
			sL_Sql = "update CCA_CARD_BASE set CORP_FLAG= nvl( ? , CORP_FLAG ) , VOICE_OPEN_CODE= ? , "
					+ "VOICE_OPEN_CODE2= ? , VOICE_AUTH_CODE= ? , VOICE_AUTH_CODE2= ? , OLD_PIN= ? , "
					+ "MOD_USER= ? , MOD_TIME= ? , MOD_PGM= ? , MOD_SEQNO= ?  ";
			sL_Sql += "where card_no= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);

			ps.setString(1, P_CcaBaseVo.getCardNo()); //CORP_FLAG=>??
			ps.setString(2, P_CcaBaseVo.getPinOfActive()); //voice_open_code => P_CcaBaseVo.getPinOfActive()
			ps.setString(3, P_CcaBaseVo.getPinOfActive()); //voice_open_code2, => P_CcaBaseVo.getPinOfActive()
			
			ps.setString(4, P_CcaBaseVo.getPinOfVoice()); //voice_auth_code, => P_CcaBaseVo.getPinOfVoice()
			ps.setString(5, P_CcaBaseVo.getPinOfVoice()); //voice_auth_code2  => P_CcaBaseVo.getPinOfVoice()
			ps.setString(6, P_CcaBaseVo.getCardNo()); //old_pin => ??
			ps.setString(7, ""); //MOD_USER ??
			ps.setDate(8, L_CurDate); //MOD_TIME
			ps.setString(9, ""); //MOD_PGM ??
			ps.setBigDecimal(10, L_ModSeq);//MOD_SEQNO
			
			ps.setString(11, P_CcaBaseVo.getCardNo()); //card_no =>P_CcaBaseVo.getCardNo()			
			
			
			//要釐清上述欄位值如何取得...  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			ps.executeUpdate();
			ps.close();
					 
					
					
							
					         
					    
					     
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

	public static boolean updateCcaCardBase(Data080Vo P_Data080Vo) {
		//proc is UPDATE_CARD_BASE()
		boolean bL_Result = true;
		try {
			BigDecimal L_ModSeq = new BigDecimal(1); //for test
			Date L_CurDate =  HpeUtil.getCurDate4Sql();
			
			/*
			String sL_Sql="";
			sL_Sql = "update CCA_CARD_BASE set CORP_FLAG= nvl( ? , CORP_FLAG ) , VOICE_OPEN_CODE= ? , "
					+ "VOICE_OPEN_CODE2= ? , VOICE_AUTH_CODE= ? , VOICE_AUTH_CODE2= ? , OLD_PIN= ? , "
					+ "MOD_USER= ? , MOD_TIME= ? , MOD_PGM= ? , MOD_SEQNO= ?  ";
			sL_Sql += "where card_no= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);

			ps.setString(1, P_CcaBaseVo.getCardNo()); //CORP_FLAG=>??
			ps.setString(2, P_CcaBaseVo.getPinOfActive()); //voice_open_code => P_CcaBaseVo.getPinOfActive()
			ps.setString(3, P_CcaBaseVo.getPinOfActive()); //voice_open_code2, => P_CcaBaseVo.getPinOfActive()
			
			ps.setString(4, P_CcaBaseVo.getPinOfVoice()); //voice_auth_code, => P_CcaBaseVo.getPinOfVoice()
			ps.setString(5, P_CcaBaseVo.getPinOfVoice()); //voice_auth_code2  => P_CcaBaseVo.getPinOfVoice()
			ps.setString(6, P_CcaBaseVo.getCardNo()); //old_pin => ??
			ps.setString(7, ""); //MOD_USER ??
			ps.setDate(8, L_CurDate); //MOD_TIME
			ps.setString(9, ""); //MOD_PGM ??
			ps.setBigDecimal(10, L_ModSeq);//MOD_SEQNO
			
			ps.setString(11, P_CcaBaseVo.getCardNo()); //card_no =>P_CcaBaseVo.getCardNo()			
			
			
			//要釐清上述欄位值如何取得...  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			ps.executeUpdate();
			ps.close();
			*/
			
			G_Ps4CardBaseUpdate.setString(1, P_Data080Vo.getCardNo()); //CORP_FLAG=>??
			G_Ps4CardBaseUpdate.setString(2, P_Data080Vo.getPinOfActive()); //voice_open_code => P_CcaBaseVo.getPinOfActive()
			G_Ps4CardBaseUpdate.setString(3, P_Data080Vo.getPinOfActive()); //voice_open_code2, => P_CcaBaseVo.getPinOfActive()
			
			G_Ps4CardBaseUpdate.setString(4, P_Data080Vo.getPinOfVoice()); //voice_auth_code, => P_CcaBaseVo.getPinOfVoice()
			G_Ps4CardBaseUpdate.setString(5, P_Data080Vo.getPinOfVoice()); //voice_auth_code2  => P_CcaBaseVo.getPinOfVoice()
			G_Ps4CardBaseUpdate.setString(6, P_Data080Vo.getCardNo()); //old_pin => ??
			G_Ps4CardBaseUpdate.setString(7, "ECS080"); //MOD_USER ??
			G_Ps4CardBaseUpdate.setDate(8, L_CurDate); //MOD_TIME
			G_Ps4CardBaseUpdate.setString(9, "ECS080"); //MOD_PGM ??
			G_Ps4CardBaseUpdate.setBigDecimal(10, L_ModSeq);//MOD_SEQNO
			
			G_Ps4CardBaseUpdate.setString(11, P_Data080Vo.getCardNo()); //card_no =>P_CcaBaseVo.getCardNo()			
			
			
			//要釐清上述欄位值如何取得...  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			G_Ps4CardBaseUpdate.executeUpdate();

			
					
					
							
					         
					    
					     
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

	
	public static boolean updateCcaCardBase(String sP_CardNo, String sP_VoiceAuthCode, String sP_VoiceAuthCode2) {

		boolean bL_Result = true;
		try {
			String sL_Sql="";
			sL_Sql = "update CCA_CARD_BASE set VOICE_AUTH_CODE= ? ,VOICE_AUTH_CODE2= ? ";
			sL_Sql += "where card_no= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);

			ps.setString(1, sP_VoiceAuthCode);
			ps.setString(2, sP_VoiceAuthCode2);
			ps.setString(3, sP_CardNo);
			
		
			ps.executeUpdate();
			ps.close();
					 
					
					
							
					         
					    
					     
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

	public static boolean updateCcaCardBase(ResultSet P_OnBatRs, ResultSet P_OppTypeReasonRs, String sP_OppStatus) {
		//ECS004 會用到
		boolean bL_Result = true;
		try {

			Date L_CurDate =  HpeUtil.getCurDate4Sql();
			String sL_CurDate = HpeUtil.getCurDateStr("");
			String sL_CurTime = HpeUtil.getCurTimeStr();
			
			String sL_Sql="";
			sL_Sql = "update CCA_CARD_BASE set ONUS_OPP_TYPE= ? , NCC_OPP_TYPE= ?, "
					+ "MOD_USER= ? , MOD_TIME= ? , MOD_PGM= ? ";
			sL_Sql += "where card_no= ?";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			
 
			ps.setString(1, P_OnBatRs.getString("OnBatOppType")); //ONUS_OPP_TYPE
			ps.setString(2, P_OppTypeReasonRs.getString("NccOppType")); //NCC_OPP_TYPE
			ps.setString(3, "ECS004"); //MOD_USER ??
			ps.setDate(4, L_CurDate); //MOD_TIME
			ps.setString(5, "ECS004"); //MOD_PGM ??
			ps.setString(6,P_OnBatRs.getString("OnBatCardNo") ); //card_no
			
			/*
			Howard: 2018/01/08 與仁和討論後， 移除 CCA_CARD_BASE 的三個欄位:OPP_STATUS, OPPOSITION_DATE, OPPOSITION_TIME
			
			sL_Sql = "update CCA_CARD_BASE set ONUS_OPP_TYPE= :p1 , NCC_OPP_TYPE=:p2, "
					+ "OPP_STATUS=:p3, OPPOSITION_DATE=:p4, OPPOSITION_TIME=:p5, "
					+ "MOD_USER=:p7, MOD_TIME=:p8, MOD_PGM=:p9 ";
			sL_Sql += "where card_no=:pCardNo";
			NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			ps.setString("pCardNo",P_OnBatRs.getString("OnBatCardNo") ); //card_no 
			ps.setString("p1", P_OnBatRs.getString("OnBatOppType")); //ONUS_OPP_TYPE
			ps.setString("p2", P_OppTypeReasonRs.getString("NccOppType")); //NCC_OPP_TYPE
			ps.setString("p3", sP_OppStatus); //OPP_STATUS
			
			ps.setString("p4", sL_CurDate); //OPPOSITION_DATE
			ps.setString("p5", sL_CurTime); //OPPOSITION_TIME
			ps.setString("p7", "ECS004"); //MOD_USER ??
			ps.setDate("p8", L_CurDate); //MOD_TIME
			ps.setString("p9", "ECS004"); //MOD_PGM ??
			*/
			
			
			
			//要釐清上述欄位值如何取得...  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			ps.executeUpdate();
			ps.close();
					 
					
					
							
					         
					    
					     
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}
	

    public static boolean updateCcaCardBase(Data004Vo P_Data004Vo, ResultSet P_OppTypeReasonRs, String sP_OppStatus) {
        //ECS004 會用到
        boolean bL_Result = true;
        try {

            Date L_CurDate =  HpeUtil.getCurDate4Sql();
            String sL_CurDate = HpeUtil.getCurDateStr("");
            String sL_CurTime = HpeUtil.getCurTimeStr();
            
            String sL_Sql="";
            sL_Sql = "update CCA_CARD_BASE set ONUS_OPP_TYPE= ? , NCC_OPP_TYPE= ?, "
                    + "MOD_USER= ? , MOD_TIME= ? , MOD_PGM= ? ";
            sL_Sql += "where card_no= ?";
            //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sL_Sql, false);
            
 
            ps.setString(1, P_Data004Vo.getOppType()); //ONUS_OPP_TYPE
            ps.setString(2, P_OppTypeReasonRs.getString("NccOppType")); //NCC_OPP_TYPE
            ps.setString(3, "ECS004"); //MOD_USER ??
            ps.setDate(4, L_CurDate); //MOD_TIME
            ps.setString(5, "ECS004"); //MOD_PGM ??
            ps.setString(6,P_Data004Vo.getCardNo() ); //card_no
            
            /*
            Howard: 2018/01/08 與仁和討論後， 移除 CCA_CARD_BASE 的三個欄位:OPP_STATUS, OPPOSITION_DATE, OPPOSITION_TIME
            
            sL_Sql = "update CCA_CARD_BASE set ONUS_OPP_TYPE= :p1 , NCC_OPP_TYPE=:p2, "
                    + "OPP_STATUS=:p3, OPPOSITION_DATE=:p4, OPPOSITION_TIME=:p5, "
                    + "MOD_USER=:p7, MOD_TIME=:p8, MOD_PGM=:p9 ";
            sL_Sql += "where card_no=:pCardNo";
            NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            ps.setString("pCardNo",P_OnBatRs.getString("OnBatCardNo") ); //card_no 
            ps.setString("p1", P_OnBatRs.getString("OnBatOppType")); //ONUS_OPP_TYPE
            ps.setString("p2", P_OppTypeReasonRs.getString("NccOppType")); //NCC_OPP_TYPE
            ps.setString("p3", sP_OppStatus); //OPP_STATUS
            
            ps.setString("p4", sL_CurDate); //OPPOSITION_DATE
            ps.setString("p5", sL_CurTime); //OPPOSITION_TIME
            ps.setString("p7", "ECS004"); //MOD_USER ??
            ps.setDate("p8", L_CurDate); //MOD_TIME
            ps.setString("p9", "ECS004"); //MOD_PGM ??
            */
            
            
            
            //要釐清上述欄位值如何取得...  
            //ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

        
            ps.executeUpdate();
            ps.close();
                     
                    
                    
                            
                             
                        
                         
        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        return bL_Result;
    }
	
	public static boolean updateProcessStatus(CcaBaseVo P_CcaBaseVo, String sP_ProcessStatus) {
		//proc is UPDATE_CARD_BASE()
		boolean bL_Result = true;
		try {
			BigDecimal L_ModSeq = new BigDecimal(1); //for test
			Date L_CurDate =  HpeUtil.getCurDate4Sql();
			String sL_Sql="";
			sL_Sql = "update CCA_CARD_BASE set PROCESS_STATUS= ? , MOD_USER=  ? , MOD_TIME= ? , MOD_PGM= ? , MOD_SEQNO= ?  ";
			sL_Sql += "where card_no= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);

			ps.setString(1, P_CcaBaseVo.getCardNo()); //CORP_FLAG=>??
			ps.setString(2, ""); //MOD_USER ??
			ps.setDate(3, L_CurDate); //MOD_TIME
			ps.setString(4, ""); //MOD_PGM ??
			ps.setBigDecimal(5, L_ModSeq); //MOD_SEQNO
			ps.setString(6, sP_ProcessStatus); //card_no =>P_CcaBaseVo.getCardNo()			
			
			
			//要釐清上述欄位值如何取得...  
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆

		
			ps.executeUpdate();
			ps.close();
					 
					
					
							
					         
					    
					     
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

	public static ResultSet getCcaCardBaseAndCardAcctAndAcno(String sP_CardNo) {
		ResultSet L_ResultSet=null;
		
		try {
			String sL_Sql="select A.CARD_NO, B.ccas_class_code as ClassCode, A.CARD_ACCT_IDX as CardAcctIdx, C.ACCT_KEY as AcctKey"
							+"FROM  CCA_CARD_BASE A,CCA_CARD_ACCT B, DBA_ACNO C "
							+"WHERE  A.CARD_NO = ? "
							+"AND    B.CARD_ACCT_IDX = A.CARD_ACCT_IDX "
							+"and A.P_SEQNO=C.P_SEQNO ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1,sP_CardNo);
		
			
			L_ResultSet = ps.executeQuery();

		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet=null;
		}
		
		return L_ResultSet;
	}
	public static CcaCardBaseVo getCcaCardBase(String sP_CardNo) {
		ResultSet L_ResultSet = null;
		CcaCardBaseVo L_CcaCardBaseVo = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		
		try {
			/*
					String sL_Sql = "select ROWID as CardBaseRowId, "
				+"CARD_NO as CcaCardBaseCardNo, "
				+"DEBIT_FLAG as CcaCardBaseDebitFlag, "
				+"ID_P_SEQNO as CcaCardBaseIdPSeqNo, "
				+"P_SEQNO as CcaCardBasePSeqNo, "
				+"CARD_ACCT_IDX as CcaCardBaseCardAcctIdx "
				+ "from CCA_CARD_BASE where CARD_NO= ?  "; //key....

			PreparedStatement ps = getPreparedStatement(sL_Sql);
			ps.setString(1, sP_CardNo);
			L_ResultSet = ps.executeQuery();
			*/
			
			System.out.println("sP_CardNo=>" + sP_CardNo + "--");
			G_Ps4CardBase.setString(1, sP_CardNo);
			
	
			
			L_ResultSet = G_Ps4CardBase.executeQuery();
			
			while (L_ResultSet.next()) {
				if (null == L_CcaCardBaseVo)
					L_CcaCardBaseVo = new CcaCardBaseVo();
				L_CcaCardBaseVo.setDebitFlag(L_ResultSet.getString("CcaCardBaseDebitFlag"));
				L_CcaCardBaseVo.setRowId(L_ResultSet.getString("CardBaseRowId"));
				
				L_CcaCardBaseVo.setIdPSeqNo(L_ResultSet.getString("CcaCardBaseIdPSeqNo"));
				L_CcaCardBaseVo.setPSeqNo(L_ResultSet.getString("CcaCardBasePSeqNo"));
				L_CcaCardBaseVo.setAcnoPSeqNo(L_ResultSet.getString("CcaCardBaseAcnoPSeqNo"));
				
				L_CcaCardBaseVo.setCardAcctIdx(L_ResultSet.getInt("CcaCardBaseCardAcctIdx"));
				L_CcaCardBaseVo.setCardNo(L_ResultSet.getString("CcaCardBaseCardNo"));
				

			}
			closeResource(L_ResultSet);
			//releaseConnection(L_ResultSet);			
			/*
			L_ResultSet.close();
			ps.close();
			Db2Connection.commit();
			*/
			
		}
		catch (Exception e) {

			System.out.println("Exception on getCcaCardBase => " + e.getMessage());
			e.printStackTrace(System.out);
		}
		L_Timer.end();
		//System.out.println("Timer : " + L_Timer.duration());
		return L_CcaCardBaseVo;
	}

	
	public static int getCardAcctIdx(String sP_CardNo) {
		int nL_CardAcctIdx = 0;
		ResultSet L_ResultSet=null;
		boolean bL_NewPs = true;
		/*
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		*/
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			if (null == G_Ps4CardBase4CardAcctIndex) {
				String sL_Sql = "select CARD_ACCT_IDX  "
						+ "from CCA_CARD_BASE where CARD_NO= ?  "; //key....

				PreparedStatement ps = getPreparedStatement(sL_Sql, true);
				ps.setString(1, sP_CardNo);
				L_ResultSet = ps.executeQuery();
				bL_NewPs = true;
			}
			else {
				G_Ps4CardBase4CardAcctIndex.setString(1, sP_CardNo);
				L_ResultSet = G_Ps4CardBase4CardAcctIndex.executeQuery();
				bL_NewPs = false;
			}
			
	
			
			
			
			while (L_ResultSet.next()) {
				nL_CardAcctIdx = L_ResultSet.getInt("CARD_ACCT_IDX");
				break;
				

			}
			
			if (bL_NewPs)
				releaseConnection(L_ResultSet);
			else
				closeResource(L_ResultSet);
			
		
		}
		catch (Exception e) {
			nL_CardAcctIdx=0;
			//System.out.println("Exception on getCardAcctIdx => " + e.getMessage());
		}
		/*
		L_Timer.end();
		//System.out.println("Timer : " + L_Timer.duration());
		*/
		return nL_CardAcctIdx;
	}
	
    public static ResultSet getCcaCardBase130(String sP_Sql, String sP_CardNo) {
        ResultSet L_ResultSet = null;

        try {
            
            PreparedStatement ps = getPreparedStatement(sP_Sql, true);

            System.out.println("sP_CardNo=>" + sP_CardNo + "--");
            ps.setString(1, sP_CardNo);
            L_ResultSet = ps.executeQuery();

        } catch (Exception e) {

            L_ResultSet = null;
        }
        // System.out.println("Timer : " + L_Timer.duration());
        return L_ResultSet;
    }
    
    public static int updateCcaCardBase130(String sP_Sql, String sP_CARD_OPEN_FLAG, String sP_CARD_OPEN_SOURCE, String sP_OPEN_DATE, String sP_card_no ) {

        int nL_Result = 0;
        try {
            //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sP_Sql, false);

            ps.setString(1, sP_CARD_OPEN_SOURCE);
            ps.setString(2, sP_CARD_OPEN_FLAG);
            ps.setString(3, sP_OPEN_DATE);
            ps.setString(4, sP_card_no);
            

            ps.executeUpdate();
            ps.close();
                     
                    
                    
                            
                             
                        
                         
        } catch (Exception e) {
            // TODO: handle exception
            nL_Result = -1;
        }
        return nL_Result;
    }

}
