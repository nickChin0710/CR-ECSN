package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.spi.DirStateFactory.Result;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;
import bank.authbatch.vo.Data004Vo;


public class CcaOppositionDao extends AuthBatchDbHandler{

	public CcaOppositionDao() throws Exception {
		// TODO Auto-generated constructor stub
	}
	
	public static boolean processOppData(ResultSet P_OnBatRs, ResultSet P_CardInfoRs, ResultSet P_OppTypeReasonRs, String sP_PrgID, String sP_OppRemark, String sP_NegDelDate) throws Exception{
		boolean bL_Result = true;
		try {
			String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");
			if (hasData(sL_CardNo))
				updateData(P_OnBatRs, P_CardInfoRs, P_OppTypeReasonRs, sP_PrgID, sP_OppRemark, sP_NegDelDate);
			else
				insertData(P_OnBatRs, P_CardInfoRs, P_OppTypeReasonRs, sP_PrgID, sP_OppRemark, sP_NegDelDate);
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result =false;
		}
		
		return bL_Result;
	}
	
	   
    public static boolean processOppData(Data004Vo P_Data004Vo, ResultSet P_CardInfoRs, ResultSet P_OppTypeReasonRs, String sP_PrgID, String sP_OppRemark, String sP_NegDelDate) throws Exception{
        boolean bL_Result = true;
        try {
            String sL_CardNo = P_Data004Vo.getCardNo();
            if (hasData(sL_CardNo))
                updateData(P_Data004Vo, P_CardInfoRs, P_OppTypeReasonRs, sP_PrgID, sP_OppRemark, sP_NegDelDate);
            else
                insertData(P_Data004Vo, P_CardInfoRs, P_OppTypeReasonRs, sP_PrgID, sP_OppRemark, sP_NegDelDate);
            
        } catch (Exception e) {
            // TODO: handle exception
            bL_Result =false;
        }
        
        return bL_Result;
    }
    

	private static boolean hasData(String sP_CardNo) throws Exception {
		boolean bL_Result = true;
		try {
			String sL_Sql="select count(*) as OppRecCount from CCA_OPPOSITION "
							+"where CARD_NO= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_CardNo);
	
		
			ResultSet L_ResultSet = ps.executeQuery();
		
			while (L_ResultSet.next()) {
				if (L_ResultSet.getInt("OppRecCount")>0)
					bL_Result = true;
				else
					bL_Result = false;
			}
			releaseConnection(L_ResultSet);
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result =false;
		}
		
		return bL_Result;
		
	}
	private static boolean insertData(ResultSet P_OnBatRs, ResultSet P_CardInfoRs, ResultSet P_OppTypeReasonRs, String sP_PrgID, String sP_OppRemark, String sP_NegDelDate) throws Exception {
		boolean bL_Result = true;
		try {
			String sL_CurDate = HpeUtil.getCurDateStr("");
			String sL_CurTime = HpeUtil.getCurTimeStr();
			
			String sL_Sql = "insert into CCA_OPPOSITION (CARD_NO,CARD_ACCT_IDX ,DEBIT_FLAG,ID_P_SEQNO,CORP_P_SEQNO,"
						+"P_SEQNO ,CARD_TYPE,BIN_TYPE ,GROUP_CODE,FROM_TYPE ,OPPO_TYPE,"
						+"OPPO_STATUS,OPPO_USER,OPPO_DATE ,OPPO_TIME,NEG_DEL_DATE,"
						+"RENEW_FLAG,RENEW_URGEN,CYCLE_CREDIT,OPP_REMARK ,MAIL_BRANCH ,"
						+"LOST_FEE_FLAG ,MST_REASON_CODE ,VIS_REASON_CODE ,"
						+"VIS_AREA_1 ,VIS_PURG_DATE_1,"
						+"VIS_AREA_2 ,VIS_PURG_DATE_2,"
						+"VIS_AREA_3 ,VIS_PURG_DATE_3,"
						+"VIS_AREA_4 ,VIS_PURG_DATE_4 ,"
						+"VIS_AREA_5  ,VIS_PURG_DATE_5 ,"
						+"VIS_AREA_6  ,VIS_PURG_DATE_6 ,"
						+"VIS_AREA_7  ,VIS_PURG_DATE_7 ,"
						+"VIS_AREA_8  ,VIS_PURG_DATE_8 ,"
						+"VIS_AREA_9  ,VIS_PURG_DATE_9,"
						+"EXCEPT_PROC_FLAG ,NEG_RESP_CODE,"
						+"VISA_RESP_CODE ,MCAS_NEG_RESP_CODE  ,"
						+"CURR_TOT_TX_AMT ,CURR_TOT_CASH_AMT ,"
						+"BANK_ACCT_NO,LOGIC_DEL ,LOGIC_DEL_DATE ,"
						+"LOGIC_DEL_TIME ,LOGIC_DEL_USER ,"
						+"CRT_DATE,CRT_TIME,CRT_USER,"
						+"CHG_DATE,CHG_USER,"
						+"MOD_TIME,MOD_USER,MOD_PGM ,MOD_SEQNO)";
			//sL_Sql +=" values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:p13,:p14,:p15,:p16,:p17,:p18,:p19,:p20,:p21,:p22,:p23,:p24,:p25,:p26,:p27,:p28,:p29,:p30,:p31,:p32,:p33,:p34,:p35,:p36,:p37,:p38,:p39,:p40,:p41,:p42,:p43,:p44,:p45,:p46,:p47,:p48,:p49,:p50,:p51,:p52,:p53,:p54,:p55,:p56,:p57,:p58,:p59,:p60,:p61,:p62) ";
			sL_Sql +=" values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,      ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?) ";
					
			String sL_No="N", sL_Blank2=" ", sL_BlankDt=" ", sL_Blank=" ", sL_VisReasonCode="";
			
			String sL_CardNewEndDate = P_CardInfoRs.getString("CardNewEndDate");
			String sL_CardBinType = P_CardInfoRs.getString("CardBinType");
			
			sL_VisReasonCode=  getVisReasonCode(P_OppTypeReasonRs, sL_CardBinType);
			
			//String sL_NegDelDate = HpeUtil.getNextMonthDate(sL_CardNewEndDate);
	
			String sL_NegOppReason = P_OppTypeReasonRs.getString("NegOppReason");
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, P_OnBatRs.getString("OnBatCardNo"));/* 卡號 */  
			ps.setString(2, P_OnBatRs.getString("OnBatCardAcctIdx")); //CARD_ACCT_IDX   /* CCAS帳戶流水號   */  
			ps.setString(3, P_OnBatRs.getString("OnBatDebitFlag")); //DEBIT_FLAG  /* debit卡  */ -- 
			ps.setString(4, P_OnBatRs.getString("OnBatIdPSeqNo")); //ID_P_SEQNO  /* 卡人流水號   */ -- 
			ps.setString(5, P_OnBatRs.getString("OnBatCorpPSeqNo")); //CORP_P_SEQNO/* 公司流水號   */ -- 
			ps.setString(6, P_OnBatRs.getString("OnBatPSeqNo")); //P_SEQNO /* 帳戶流水號   */ -- 
			ps.setString(7, P_CardInfoRs.getString("CardCardType")); //CARD_TYPE   /* 卡種 */ -- 
			ps.setString(8, P_OnBatRs.getString("OnBatBinType")); //BIN_TYPE   /* 卡別 */ -- 
			ps.setString(9, P_CardInfoRs.getString("CardGroupCode")); //GROUP_CODE  /* 團體代號 */ -- 
			ps.setString(10, "2"); //FROM_TYPE  /* 來源類別 */ -- 1.人工, 2.批次
			ps.setString(11, P_OnBatRs.getString("OnBatOppType")); //OPPO_TYPE   /* On Us 停掛類別   */ -- 
			ps.setString(12,  P_OnBatRs.getString("OnBatOppReason")); //OPPO_STATUS /* On Us 停掛原因碼 */ -- 
			ps.setString(13, sP_PrgID); //OPPO_USER   /* 停掛人員代碼 */ -- 
			ps.setString(14, sL_CurDate); //OPPO_DATE   /* 停掛日期 */ -- 
			ps.setString(15, sL_CurTime); //OPPO_TIME   /* 停掛時間 */ -- 
			ps.setString(16, sP_NegDelDate); //NEG_DEL_DATE/* NEG刪除日期  */ -- 
			ps.setString(17, sL_No); //RENEW_FLAG  /* 補發否註記   */ -- 
			ps.setString(18, sL_No); //RENEW_URGEN /* 緊急補發否註記   */ -- 
			ps.setString(19, "N"); //CYCLE_CREDIT/* 循環信用否註記   */ -- 
			ps.setString(20, sP_OppRemark); //OPP_REMARK /* 備註說明 */ -- 
			
			ps.setString(21, ""); //MAIL_BRANCH /* 寄件分行 */ --
			ps.setString(22, "N"); //LOST_FEE_FLAG  /* 收取掛失費否 */ -- Y/N
			ps.setString(23, sL_NegOppReason); //MST_REASON_CODE /* NEG 原因碼   */ -- 
			ps.setString(24, sL_VisReasonCode); //VIS_REASON_CODE /* VISA/Mast/JCB 原因碼 */ -- 
			ps.setString(25, ""); //VIS_AREA_1  /* VISA 區域碼  */ -- 
			ps.setString(26, sL_BlankDt); //VIS_PURG_DATE_1 /* VISA 刪除日期*/ -- 
			ps.setString(27, sL_Blank); //VIS_AREA_2  /* VISA 區域碼  */ -- 
			ps.setString(28, sL_BlankDt); //VIS_PURG_DATE_2 /* VISA 刪除日期*/ -- 
			ps.setString(29, sL_Blank); //VIS_AREA_3  /* VISA 區域碼  */ -- 
			ps.setString(30, sL_BlankDt); //VIS_PURG_DATE_3 /* VISA 刪除日期*/ -- 
			ps.setString(31, sL_Blank); //VIS_AREA_4  /* VISA 區域碼  */ -- 
			ps.setString(32, sL_BlankDt); //VIS_PURG_DATE_4 /* VISA 刪除日期*/ -- 
			ps.setString(33, sL_Blank); //VIS_AREA_5  /* VISA 區域碼(5)   */ -- 
			ps.setString(34, sL_BlankDt); //VIS_PURG_DATE_5 /* VISA 刪除日期(5) */ -- 
			ps.setString(35, sL_Blank); //VIS_AREA_6  /* VISA 區域碼  */ -- 
			ps.setString(36, sL_BlankDt); //VIS_PURG_DATE_6 /* VISA 刪除日期*/ -- 
			ps.setString(37, sL_Blank); //VIS_AREA_7  /* VISA 區域碼(7)   */ -- 
			ps.setString(38, sL_BlankDt); //VIS_PURG_DATE_7 /* VISA 刪除日期(7) */ -- 
			ps.setString(39, sL_Blank); //VIS_AREA_8  /* VISA 區域碼  */ --
			
			ps.setString(40, sL_BlankDt); //VIS_PURG_DATE_8 /* VISA 刪除日期*/ -- 
			ps.setString(41, sL_Blank); //VIS_AREA_9  /* VISA 區域碼(9)   */ -- 
			ps.setString(42, sL_BlankDt); //VIS_PURG_DATE_9 /* VISA 刪除日期(9) */ -- 
			ps.setString(43, ""); //EXCEPT_PROC_FLAG   /* Exception處理註記*/ -- Y/N
			ps.setString(44, sL_Blank2); //NEG_RESP_CODE   /* NEG 回覆碼   */ -- 
			ps.setString(45, sL_Blank2); //VISA_RESP_CODE  /* VISA/Mast/JCB 回覆碼 */ -- 
			ps.setString(46, sL_Blank2); //MCAS_NEG_RESP_CODE  /* MCAS 回覆碼  */ -- 
			ps.setString(47, "0"); //CURR_TOT_TX_AMT /* 停掛後累計消費金額   */ -- 
			ps.setString(48, "0"); //CURR_TOT_CASH_AMT   /* 停掛後累計預借金額   */ -- 
			ps.setString(49, P_CardInfoRs.getString("CardBankActNo")); //BANK_ACCT_NO/* 金融卡卡號   */ --
			ps.setString(50, ""); //LOGIC_DEL   /* 撤掛註記 */ -- 
			ps.setString(51, ""); //LOGIC_DEL_DATE  /* 撤掛日期 */ -- 
			ps.setString(52, ""); //LOGIC_DEL_TIME  /* 撤掛時間 */ -- 
			ps.setString(53, ""); //LOGIC_DEL_USER  /* 撤掛人員代碼 */ -- 
			ps.setString(54, sL_CurDate); //CRT_DATE/* 鍵檔日期 */ -- 
			ps.setString(55, sL_CurTime); //CRT_TIME/* 建檔時間 */ -- 
			ps.setString(56, sP_PrgID); //CRT_USER/* 建檔人員 */ -- 
			ps.setString(57, sL_CurDate); //CHG_DATE/* 異動日期 */ -- 
			ps.setString(58, sP_PrgID); //CHG_USER/* 異動人員 */ -- 
			ps.setString(59, sL_CurTime); //MOD_TIME/* 帳戶維護日期 */ -- 
			ps.setString(60, sP_PrgID); //MOD_USER/* 最近更新人員 */ -- 
			ps.setString(61, sP_PrgID); //MOD_PGM /* 異動程式 */ -- 
			ps.setString(62, "0"); //MOD_SEQNO /* 異動註記 */ -- 

			ps.executeUpdate();
			ps.close();
					
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result =false;
		}
		
		return bL_Result;
		
	}
	

    private static boolean insertData(Data004Vo P_Data004Vo, ResultSet P_CardInfoRs, ResultSet P_OppTypeReasonRs, String sP_PrgID, String sP_OppRemark, String sP_NegDelDate) throws Exception {
        boolean bL_Result = true;
        try {
            String sL_CurDate = HpeUtil.getCurDateStr("");
            String sL_CurTime = HpeUtil.getCurTimeStr();
            
            String sL_Sql = "insert into CCA_OPPOSITION (CARD_NO,CARD_ACCT_IDX ,DEBIT_FLAG,ID_P_SEQNO,CORP_P_SEQNO,"
                        +"P_SEQNO ,CARD_TYPE,BIN_TYPE ,GROUP_CODE,FROM_TYPE ,OPPO_TYPE,"
                        +"OPPO_STATUS,OPPO_USER,OPPO_DATE ,OPPO_TIME,NEG_DEL_DATE,"
                        +"RENEW_FLAG,RENEW_URGEN,CYCLE_CREDIT,OPP_REMARK ,MAIL_BRANCH ,"
                        +"LOST_FEE_FLAG ,MST_REASON_CODE ,VIS_REASON_CODE ,"
                        +"VIS_AREA_1 ,VIS_PURG_DATE_1,"
                        +"VIS_AREA_2 ,VIS_PURG_DATE_2,"
                        +"VIS_AREA_3 ,VIS_PURG_DATE_3,"
                        +"VIS_AREA_4 ,VIS_PURG_DATE_4 ,"
                        +"VIS_AREA_5  ,VIS_PURG_DATE_5 ,"
                        +"VIS_AREA_6  ,VIS_PURG_DATE_6 ,"
                        +"VIS_AREA_7  ,VIS_PURG_DATE_7 ,"
                        +"VIS_AREA_8  ,VIS_PURG_DATE_8 ,"
                        +"VIS_AREA_9  ,VIS_PURG_DATE_9,"
                        +"EXCEPT_PROC_FLAG ,NEG_RESP_CODE,"
                        +"VISA_RESP_CODE ,MCAS_NEG_RESP_CODE  ,"
                        +"CURR_TOT_TX_AMT ,CURR_TOT_CASH_AMT ,"
                        +"BANK_ACCT_NO,LOGIC_DEL ,LOGIC_DEL_DATE ,"
                        +"LOGIC_DEL_TIME ,LOGIC_DEL_USER ,"
                        +"CRT_DATE,CRT_TIME,CRT_USER,"
                        +"CHG_DATE,CHG_USER,"
                        +"MOD_TIME,MOD_USER,MOD_PGM ,MOD_SEQNO)";
            //sL_Sql +=" values(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:p13,:p14,:p15,:p16,:p17,:p18,:p19,:p20,:p21,:p22,:p23,:p24,:p25,:p26,:p27,:p28,:p29,:p30,:p31,:p32,:p33,:p34,:p35,:p36,:p37,:p38,:p39,:p40,:p41,:p42,:p43,:p44,:p45,:p46,:p47,:p48,:p49,:p50,:p51,:p52,:p53,:p54,:p55,:p56,:p57,:p58,:p59,:p60,:p61,:p62) ";
            sL_Sql +=" values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,     ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,      ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?) ";
                    
            String sL_No="N", sL_Blank2=" ", sL_BlankDt=" ", sL_Blank=" ", sL_VisReasonCode="";
            
            String sL_CardNewEndDate = P_CardInfoRs.getString("CardNewEndDate");
            String sL_CardBinType = P_CardInfoRs.getString("CardBinType");
            
            sL_VisReasonCode=  getVisReasonCode(P_OppTypeReasonRs, sL_CardBinType);
            
            //String sL_NegDelDate = HpeUtil.getNextMonthDate(sL_CardNewEndDate);
    
            String sL_NegOppReason = P_OppTypeReasonRs.getString("NegOppReason");
            //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
            PreparedStatement ps = getPreparedStatement(sL_Sql, true);
            ps.setString(1, P_Data004Vo.getCardNo());/* 卡號 */  
            ps.setString(2, P_Data004Vo.getCardAcctIdx()); //CARD_ACCT_IDX   /* CCAS帳戶流水號   */  
            ps.setString(3, P_Data004Vo.getDebitFlag()); //DEBIT_FLAG  /* debit卡  */ -- 
            ps.setString(4, P_Data004Vo.getIdPSeqno()); //ID_P_SEQNO  /* 卡人流水號   */ -- 
            ps.setString(5, P_Data004Vo.getCorpPSeqno()); //CORP_P_SEQNO/* 公司流水號   */ -- 
            ps.setString(6, P_Data004Vo.getPSeqno()); //P_SEQNO /* 帳戶流水號   */ -- 
            ps.setString(7, P_CardInfoRs.getString("CardCardType")); //CARD_TYPE   /* 卡種 */ -- 
            ps.setString(8, P_Data004Vo.getBinType()); //BIN_TYPE   /* 卡別 */ -- 
            ps.setString(9, P_CardInfoRs.getString("CardGroupCode")); //GROUP_CODE  /* 團體代號 */ -- 
            ps.setString(10, "2"); //FROM_TYPE  /* 來源類別 */ -- 1.人工, 2.批次
            ps.setString(11, P_Data004Vo.getOppType()); //OPPO_TYPE   /* On Us 停掛類別   */ -- 
            ps.setString(12,  P_Data004Vo.getOppReason()); //OPPO_STATUS /* On Us 停掛原因碼 */ -- 
            ps.setString(13, sP_PrgID); //OPPO_USER   /* 停掛人員代碼 */ -- 
            ps.setString(14, sL_CurDate); //OPPO_DATE   /* 停掛日期 */ -- 
            ps.setString(15, sL_CurTime); //OPPO_TIME   /* 停掛時間 */ -- 
            ps.setString(16, sP_NegDelDate); //NEG_DEL_DATE/* NEG刪除日期  */ -- 
            ps.setString(17, sL_No); //RENEW_FLAG  /* 補發否註記   */ -- 
            ps.setString(18, sL_No); //RENEW_URGEN /* 緊急補發否註記   */ -- 
            ps.setString(19, "N"); //CYCLE_CREDIT/* 循環信用否註記   */ -- 
            ps.setString(20, sP_OppRemark); //OPP_REMARK /* 備註說明 */ -- 
            
            ps.setString(21, ""); //MAIL_BRANCH /* 寄件分行 */ --
            ps.setString(22, "N"); //LOST_FEE_FLAG  /* 收取掛失費否 */ -- Y/N
            ps.setString(23, sL_NegOppReason); //MST_REASON_CODE /* NEG 原因碼   */ -- 
            ps.setString(24, sL_VisReasonCode); //VIS_REASON_CODE /* VISA/Mast/JCB 原因碼 */ -- 
            ps.setString(25, ""); //VIS_AREA_1  /* VISA 區域碼  */ -- 
            ps.setString(26, sL_BlankDt); //VIS_PURG_DATE_1 /* VISA 刪除日期*/ -- 
            ps.setString(27, sL_Blank); //VIS_AREA_2  /* VISA 區域碼  */ -- 
            ps.setString(28, sL_BlankDt); //VIS_PURG_DATE_2 /* VISA 刪除日期*/ -- 
            ps.setString(29, sL_Blank); //VIS_AREA_3  /* VISA 區域碼  */ -- 
            ps.setString(30, sL_BlankDt); //VIS_PURG_DATE_3 /* VISA 刪除日期*/ -- 
            ps.setString(31, sL_Blank); //VIS_AREA_4  /* VISA 區域碼  */ -- 
            ps.setString(32, sL_BlankDt); //VIS_PURG_DATE_4 /* VISA 刪除日期*/ -- 
            ps.setString(33, sL_Blank); //VIS_AREA_5  /* VISA 區域碼(5)   */ -- 
            ps.setString(34, sL_BlankDt); //VIS_PURG_DATE_5 /* VISA 刪除日期(5) */ -- 
            ps.setString(35, sL_Blank); //VIS_AREA_6  /* VISA 區域碼  */ -- 
            ps.setString(36, sL_BlankDt); //VIS_PURG_DATE_6 /* VISA 刪除日期*/ -- 
            ps.setString(37, sL_Blank); //VIS_AREA_7  /* VISA 區域碼(7)   */ -- 
            ps.setString(38, sL_BlankDt); //VIS_PURG_DATE_7 /* VISA 刪除日期(7) */ -- 
            ps.setString(39, sL_Blank); //VIS_AREA_8  /* VISA 區域碼  */ --
            
            ps.setString(40, sL_BlankDt); //VIS_PURG_DATE_8 /* VISA 刪除日期*/ -- 
            ps.setString(41, sL_Blank); //VIS_AREA_9  /* VISA 區域碼(9)   */ -- 
            ps.setString(42, sL_BlankDt); //VIS_PURG_DATE_9 /* VISA 刪除日期(9) */ -- 
            ps.setString(43, ""); //EXCEPT_PROC_FLAG   /* Exception處理註記*/ -- Y/N
            ps.setString(44, sL_Blank2); //NEG_RESP_CODE   /* NEG 回覆碼   */ -- 
            ps.setString(45, sL_Blank2); //VISA_RESP_CODE  /* VISA/Mast/JCB 回覆碼 */ -- 
            ps.setString(46, sL_Blank2); //MCAS_NEG_RESP_CODE  /* MCAS 回覆碼  */ -- 
            ps.setString(47, "0"); //CURR_TOT_TX_AMT /* 停掛後累計消費金額   */ -- 
            ps.setString(48, "0"); //CURR_TOT_CASH_AMT   /* 停掛後累計預借金額   */ -- 
            ps.setString(49, P_CardInfoRs.getString("CardBankActNo")); //BANK_ACCT_NO/* 金融卡卡號   */ --
            ps.setString(50, ""); //LOGIC_DEL   /* 撤掛註記 */ -- 
            ps.setString(51, ""); //LOGIC_DEL_DATE  /* 撤掛日期 */ -- 
            ps.setString(52, ""); //LOGIC_DEL_TIME  /* 撤掛時間 */ -- 
            ps.setString(53, ""); //LOGIC_DEL_USER  /* 撤掛人員代碼 */ -- 
            ps.setString(54, sL_CurDate); //CRT_DATE/* 鍵檔日期 */ -- 
            ps.setString(55, sL_CurTime); //CRT_TIME/* 建檔時間 */ -- 
            ps.setString(56, sP_PrgID); //CRT_USER/* 建檔人員 */ -- 
            ps.setString(57, sL_CurDate); //CHG_DATE/* 異動日期 */ -- 
            ps.setString(58, sP_PrgID); //CHG_USER/* 異動人員 */ -- 
            ps.setString(59, sL_CurTime); //MOD_TIME/* 帳戶維護日期 */ -- 
            ps.setString(60, sP_PrgID); //MOD_USER/* 最近更新人員 */ -- 
            ps.setString(61, sP_PrgID); //MOD_PGM /* 異動程式 */ -- 
            ps.setString(62, "0"); //MOD_SEQNO /* 異動註記 */ -- 

            ps.executeUpdate();
            ps.close();
                    
        } catch (Exception e) {
            // TODO: handle exception
            bL_Result =false;
        }
        
        return bL_Result;
        
    }

	private static String getVisReasonCode(ResultSet P_OppTypeReasonRs, String sP_CardBinType) throws Exception {
		
		String sL_VisReasonCode="";
		if ("V".equals(sP_CardBinType))
			sL_VisReasonCode= P_OppTypeReasonRs.getString("VisExcepCode");
		else if ("M".equals(sP_CardBinType))
			sL_VisReasonCode= P_OppTypeReasonRs.getString("MstAuthCode");
		else if ("J".equals(sP_CardBinType))
			sL_VisReasonCode= P_OppTypeReasonRs.getString("JcbExcpCode");
		
		return sL_VisReasonCode;

	}
	
    private static boolean updateData(Data004Vo P_Data004Vo, ResultSet P_CardInfoRs, ResultSet P_OppTypeReasonRs,
            String sP_PrgID, String sP_OppRemark, String sP_NegDelDate) throws Exception {
        String sL_OnBatOppType = P_Data004Vo.getOppType();
        String sL_OnBatOppReason = P_Data004Vo.getOppReason();
        return updateData(sL_OnBatOppType, sL_OnBatOppReason, P_CardInfoRs, P_OppTypeReasonRs, sP_PrgID, sP_OppRemark,
                sP_NegDelDate);
    }
    
    private static boolean updateData(ResultSet P_OnBatRs, ResultSet P_CardInfoRs, ResultSet P_OppTypeReasonRs,
            String sP_PrgID, String sP_OppRemark, String sP_NegDelDate) throws Exception {
        String sL_OnBatOppType = P_OnBatRs.getString("OnBatOppType");
        String sL_OnBatOppReason = P_OnBatRs.getString("OnBatOppReason");
        return updateData(sL_OnBatOppType, sL_OnBatOppReason, P_CardInfoRs, P_OppTypeReasonRs, sP_PrgID, sP_OppRemark,
                sP_NegDelDate);
    }
    
	private static boolean updateData(String sP_OnBatOppType,String sP_OnBatOppReason, ResultSet P_CardInfoRs, ResultSet P_OppTypeReasonRs, String sP_PrgID, String sP_OppRemark, String sP_NegDelDate) throws Exception {
		boolean bL_Result = true;
		try {

			String sL_CurDate = HpeUtil.getCurDateStr("");
			String sL_CurTime = HpeUtil.getCurTimeStr();

			String sL_Sql = "update CCA_OPPOSITION set OPPO_TYPE= ? , OPPO_STATUS= ?, "
						+"OPPO_USER= ?, OPPO_DATE=?, OPPO_TIME=?, "
						+"NEG_DEL_DATE=?, "
						+ "VIS_AREA_1=?, VIS_PURG_DATE_1=?,"
						+ "VIS_AREA_2=?, VIS_PURG_DATE_2=?,"
						+ "VIS_AREA_3=?, VIS_PURG_DATE_3=?,"
						+ "VIS_AREA_4=?, VIS_PURG_DATE_4=?,"
						+ "VIS_AREA_5=?, VIS_PURG_DATE_5=?,"
						+ "VIS_AREA_6=?, VIS_PURG_DATE_6=?,"
						+ "VIS_AREA_7=?, VIS_PURG_DATE_7=?,"
						+ "VIS_AREA_8=?, VIS_PURG_DATE_8=?,"
						+ "VIS_AREA_9=?, VIS_PURG_DATE_9=?,"
						+ "NEG_RESP_CODE=?, VISA_RESP_CODE=?,"
						+ "LOGIC_DEL=?, LOGIC_DEL_DATE=?,"
						+ "LOGIC_DEL_TIME=?, LOGIC_DEL_USER=?,"
						+ "RENEW_URGEN= ?, MST_REASON_CODE= ?,"
						+ "VIS_REASON_CODE= ?, RENEW_FLAG= ?,OPP_REMARK= ? ";
			sL_Sql += " where CARD_NO=?";

			String sL_No="N", sL_Blank2=" ", sL_BlankDt=" ", sL_Blank=" ", sL_VisReasonCode="";
			String sL_CardNewEndDate = P_CardInfoRs.getString("CardNewEndDate");
			//String sL_NegDelDate = HpeUtil.getNextMonthDate(sL_CardNewEndDate);
			
			String sL_NegOppReason = P_OppTypeReasonRs.getString("NegOppReason");

			String sL_CardBinType = P_CardInfoRs.getString("CardBinType");
			
			sL_VisReasonCode=  getVisReasonCode(P_OppTypeReasonRs, sL_CardBinType);
			
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			ps.setString(1, sP_OnBatOppType); //OPPO_TYPE   /* On Us 停掛類別   */ -- 
			ps.setString(2,  sP_OnBatOppReason); //OPPO_STATUS /* On Us 停掛原因碼 */ -- 
			ps.setString(3, sP_PrgID); //OPPO_USER   /* 停掛人員代碼 */ -- 
			ps.setString(4, sL_CurDate); //OPPO_DATE   /* 停掛日期 */ -- 
			ps.setString(5, sL_CurTime); //OPPO_TIME   /* 停掛時間 */ -- 
			ps.setString(6, sP_NegDelDate); //NEG_DEL_DATE/* NEG刪除日期  */ -- 
			ps.setString(7, ""); //VIS_AREA_1  /* VISA 區域碼  */ -- 
			ps.setString(8, sL_BlankDt); //VIS_PURG_DATE_1 /* VISA 刪除日期*/ -- 
			ps.setString(9, sL_Blank); //VIS_AREA_2  /* VISA 區域碼  */ -- 
			ps.setString(10, sL_BlankDt); //VIS_PURG_DATE_2 /* VISA 刪除日期*/ -- 
			ps.setString(11, sL_Blank); //VIS_AREA_3  /* VISA 區域碼  */ -- 
			ps.setString(12, sL_BlankDt); //VIS_PURG_DATE_3 /* VISA 刪除日期*/ -- 
			ps.setString(13, sL_Blank); //VIS_AREA_4  /* VISA 區域碼  */ -- 
			ps.setString(14, sL_BlankDt); //VIS_PURG_DATE_4 /* VISA 刪除日期*/ -- 
			ps.setString(15, sL_Blank); //VIS_AREA_5  /* VISA 區域碼(5)   */ -- 
			ps.setString(16, sL_BlankDt); //VIS_PURG_DATE_5 /* VISA 刪除日期(5) */ -- 
			ps.setString(17, sL_Blank); //VIS_AREA_6  /* VISA 區域碼  */ -- 
			ps.setString(18, sL_BlankDt); //VIS_PURG_DATE_6 /* VISA 刪除日期*/ -- 
			ps.setString(19, sL_Blank); //VIS_AREA_7  /* VISA 區域碼(7)   */ -- 
			ps.setString(20, sL_BlankDt); //VIS_PURG_DATE_7 /* VISA 刪除日期(7) */ -- 
			ps.setString(21, sL_Blank); //VIS_AREA_8  /* VISA 區域碼  */ --
			
			ps.setString(22, sL_BlankDt); //VIS_PURG_DATE_8 /* VISA 刪除日期*/ -- 
			ps.setString(23, sL_Blank); //VIS_AREA_9  /* VISA 區域碼(9)   */ -- 
			ps.setString(24, sL_BlankDt); //VIS_PURG_DATE_9 /* VISA 刪除日期(9) */ -- 
			ps.setString(25, sL_Blank2); //NEG_RESP_CODE   /* NEG 回覆碼   */ -- 
			ps.setString(26, sL_Blank2); //VISA_RESP_CODE  /* VISA/Mast/JCB 回覆碼 */ --
			
			
			ps.setString(27, ""); //LOGIC_DEL   /* 撤掛註記 */ --
			ps.setString(28, ""); //LOGIC_DEL_DATE  /* 撤掛日期 */ -- 
			ps.setString(29, ""); //LOGIC_DEL_TIME  /* 撤掛時間 */ -- 
			ps.setString(30, ""); //LOGIC_DEL_USER  /* 撤掛人員代碼 */ -- 
			ps.setString(31, sL_No); //RENEW_URGEN /* 緊急補發否註記   */ --
			ps.setString(32, sL_NegOppReason); //MST_REASON_CODE /* NEG 原因碼   */ -- 
			ps.setString(33, sL_VisReasonCode); //VIS_REASON_CODE /* VISA/Mast/JCB 原因碼 */ -- 
			ps.setString(34, sL_No); //RENEW_FLAG  /* 補發否註記   */ -- 
			ps.setString(36, sP_OppRemark); //OPP_REMARK /* 備註說明 */ --
			ps.setString(37, P_CardInfoRs.getString("CardNo")); 
			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result =false;
		}
		
		return bL_Result;
		
	}

}
