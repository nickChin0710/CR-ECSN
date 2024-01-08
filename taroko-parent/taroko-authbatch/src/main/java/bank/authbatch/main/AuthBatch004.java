/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-08-03  V1.00.01  yanghan  "将栏位ICBC_RESP_CODE->BANK_RESP_CODE ICBC_RESP_DESC->BANK_RESP_DESC*
******************************************************************************/
package bank.authbatch.main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.time.format.TextStyle;

import bank.AuthIntf.AuthGate;
import bank.AuthIntf.BicFormat;
import bank.authbatch.dao.CardAcctDao;
import bank.authbatch.dao.CardAcctIndexDao;
import bank.authbatch.dao.CcaCardBaseDao;
import bank.authbatch.dao.CcaOppositionDao;
import bank.authbatch.dao.CcaOutgoingDao;
import bank.authbatch.dao.CcaSpecCodeDao;
import bank.authbatch.dao.CcaSysParm1Dao;
import bank.authbatch.dao.CcaSysParm2Dao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.dao.CrdCardDao;
import bank.authbatch.dao.DbcCardDao;
import bank.authbatch.dao.OnBatDao;
import bank.authbatch.dao.OppTypeReasonDao;
import bank.authbatch.dao.SystemDao;
import bank.authbatch.vo.CardAcctVo;
import bank.authbatch.vo.CcaOutgoingVo;
import bank.authbatch.vo.CcaSysParm3Vo;
import bank.authbatch.vo.Data004Vo;

public class AuthBatch004 extends BatchProgBase{

	ResultSet G_CardInfoRs = null;
	String sG_DbOppReason="", sG_CardStatus="";
	String sG_DbNegOppReason="";//sG_DbNegOppReason == DB_NEG_OPP_REASON
	String sG_DbNccOppType="";
	String sG_DbVisExcepCode="";// pros is DB_VIS_EXCEP_CODE
    String sG_DbMstAuthCode="";// proc is DB_MST_AUTH_CODE
    String sG_DbJcbExcpCode="";//proc is DB_JCB_EXCP_CODE
    String sG_OtActCode="";//proc is OT_ACT_CODE 
	String sG_CaiCardAcctClass="", sG_CaiCardAcctId="", sG_CaiCardCorpId="", sG_CaiCardAcctIdSeq="", sG_CaiCardCorpIdSeq="", sG_CaiEcsAcctClass="";
	int nG_CaiAcctIdx=0, nG_CaiAcctParentIndex=0;
	String sG_NegDelDate="", sG_CardNewEndDate="";
	String sG_DbNegCapCode="";//proc is DB_NEG_CAP_CODE  
	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
		
		try {

			setStopRun(1);


			connDb();

			ResultSet L_OnBatRs=null;
			int nL_Continue=0;

			
			while (1==getStopRun()) {
				System.out.println("c");
				setSleepTime(10);
				setStopRun(1);
				setPauseRun(1);
				System.out.println("d");
				getSysParm3();
				if (0==getStopRun())
					break;
				nL_Continue=1;
				L_OnBatRs =null;
				
				//down, 處理卡戶解禁超, proc is TB_CCAS_ONBAT(2) 
				while(nL_Continue==1) {
					nL_Continue=0;
					String sL_SelectFields = " a.ROWID as OnBatRowId,a.CARD_NO as OnBatCardNo, a.CARD_ACCT_ID as OnBatCardAcctId, a.CARD_HLDR_ID as OnBatCardHldrId, NVL(a.ACCOUNT_TYPE,'01') as OnBatAccountType, NVL(a.PAYMENT_TYPE,'1') as OnBatPaymentType, "
							+ "NVL(a.CARD_CATALOG,'1') as OnBatCardCatalog, NVL(a.MATCH_FLAG,'1') as OnBatMatchFlag, TRIM(a.BLOCK_CODE_1) as OnBatBlockCode1, "
							+ "TRIM(a.BLOCK_CODE_2) as OnBatBlockCode2,TRIM(a.BLOCK_CODE_3)  as OnBatBlockCode3, TRIM(a.BLOCK_CODE_4)  as OnBatBlockCode4,"
							+ "TRIM(a.BLOCK_CODE_5)  as OnBatBlockCode5, a.CONTRACT  as OnBatContract, a.ACCT_NO  as OnBatBlockAcctNo "
							+ "NVL(a.CARD_CATALOG,'1') as OnBatCardCatalog, NVL(a.OPP_TYPE,' ') as OnBatOppType, NVL(a.OPP_REASON,'  ') as OnBatOppReason, NVL(a.OPP_DATE,'00000000') as OnBatOppDate, b.Debit_FLAG as OnBatDebitFlag, "
							+ "b.BIN_TYPE  as OnBatBinType,b.ID_P_SEQNO as OnBatIdPSeqNo, b.P_SEQNO as OnBatPSeqNo, b.CORP_P_SEQNO  as OnBatCorpPSeqNo, b.CARD_ACCT_IDX as OnBatCardAcctIdx  ";

					/*
					String sL_SelectFields = " ROWID as OnBatRowId,CARD_ACCT_ID as OnBatCardAcctId, CARD_HLDR_ID as OnBatCardHldrId, NVL(ACCOUNT_TYPE,'01') as OnBatAccountType, NVL(PAYMENT_TYPE,'1') as OnBatPaymentType, "
							+ "NVL(CARD_CATALOG,'1') as OnBatCardCatalog, NVL(MATCH_FLAG,'1') as OnBatMatchFlag, TRIM(BLOCK_CODE_1) as OnBatBlockCode1, "
							+ "TRIM(BLOCK_CODE_2) as OnBatBlockCode2,TRIM(BLOCK_CODE_3)  as OnBatBlockCode3, TRIM(BLOCK_CODE_4)  as OnBatBlockCode4,"
							+ "TRIM(BLOCK_CODE_5)  as OnBatBlockCode5, CONTRACT  as OnBatContract, ACCT_NO  as OnBatBlockAcctNo ";
					 */							
					//L_OnBatRs = OnBatDao.getOnBat2("2","2", "0", " ONBAT_2CCAS ", sL_SelectFields);
					L_OnBatRs = OnBatDao.getOnBat3("2",2, 0, " ONBAT_2CCAS a, CCA_CARD_BASE b ", sL_SelectFields, " and a.CARD_NO=b.CARD_NO ");					
					processOnBatData1(L_OnBatRs);//解禁超result set
				}
				//up, 處理卡戶解禁超
				
				
				//down, 處理卡戶強停, proc is TB_CCAS_ONBAT(4)
				while(nL_Continue==1) {
					nL_Continue=0;
					String sL_SelectFields = " a.ROWID as OnBatRowId,a.CARD_NO as OnBatCardNo, a.CARD_ACCT_ID as OnBatCardAcctId, a.CARD_HLDR_ID as OnBatCardHldrId, NVL(a.ACCOUNT_TYPE,'01') as OnBatAccountType, NVL(a.PAYMENT_TYPE,'1') as OnBatPaymentType, "
							+ "NVL(a.CARD_CATALOG,'1') as OnBatCardCatalog, NVL(a.MATCH_FLAG,'1') as OnBatMatchFlag, TRIM(a.BLOCK_CODE_1) as OnBatBlockCode1, "
							+ "TRIM(a.BLOCK_CODE_2) as OnBatBlockCode2,TRIM(a.BLOCK_CODE_3)  as OnBatBlockCode3, TRIM(a.BLOCK_CODE_4)  as OnBatBlockCode4,"
							+ "TRIM(a.BLOCK_CODE_5)  as OnBatBlockCode5, a.CONTRACT  as OnBatContract, a.ACCT_NO  as OnBatBlockAcctNo "
							+ "NVL(a.CARD_CATALOG,'1') as OnBatCardCatalog, NVL(a.OPP_TYPE,' ') as OnBatOppType, NVL(a.OPP_REASON,'  ') as OnBatOppReason, NVL(a.OPP_DATE,'00000000') as OnBatOppDate, b.Debit_FLAG as OnBatDebitFlag, "
							+ "b.BIN_TYPE  as OnBatBinType,b.ID_P_SEQNO as OnBatIdPSeqNo, b.P_SEQNO as OnBatPSeqNo, b.CORP_P_SEQNO  as OnBatCorpPSeqNo, b.CARD_ACCT_IDX as OnBatCardAcctIdx  ";
					L_OnBatRs = OnBatDao.getOnBat3("4",2, 0, " ONBAT_2CCAS a, CCA_CARD_BASE b ", sL_SelectFields, " and a.CARD_NO=b.CARD_NO ");
					processOnBatData2(L_OnBatRs);//強停result set
				}				
				//up, 處理卡戶強停

				break;
			}
//			closeDb();
			System.out.println("004..12");

		} catch (Exception e) {
			// TODO: handle exception
        } finally {
            closeDb();
        }
	}
	
    public int startProcess(Data004Vo P_Data004Vo) {
        try {
            String sL_OnBatCardNo = P_Data004Vo.getCardNo();
            String sL_OnbatTransType = P_Data004Vo.getTransType();


            if (sL_OnbatTransType.equals("2")) {
                processOnBatData1(P_Data004Vo);// 解禁超result set
            } else if (sL_OnbatTransType.equals("4")) {
                processOnBatData2(P_Data004Vo);// 強停result set
            }
            
            return 0;

        } catch (Exception e) {
            // TODO: handle exception
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String sRtn = sw.toString();
            sw = null;
            
            System.out.println(sRtn);
            return -1;
        }

    }
	
	private void processOnBatData2(ResultSet P_OnBatRs) throws Exception{
		//處理強停 onbat result set
		while(P_OnBatRs.next()) {
			RowId L_OnBatRowId = P_OnBatRs.getRowId("OnBatRowId");
			genRecFields(P_OnBatRs);
			if (chkCAIindex(1, P_OnBatRs)) {
				String sL_OppType = P_OnBatRs.getString("OnBatOppType");
				String sL_OppStatus = P_OnBatRs.getString("OnBatOppReason"); 
				
				ResultSet L_OppTypeReasonRs =  OppTypeReasonDao.getOppTypeReason(sL_OppType, sL_OppStatus,"3", "  ");
				/*
				if (OppTypeReasonDao.isEmptyResultSet(L_OppTypeReasonRs)) {
					//str2var(DB_ICBC_RESP_DESC,"*無停掛原因*");
					String sL_IcbcRespDesc = "*無停掛原因*";
					OnBatDao.updateOnBat8(2,G_CurTimestamp , sG_CurDate, L_OnBatRowId, sL_IcbcRespDesc);
				}
				*/
				boolean bL_HasData = false;
				while (L_OppTypeReasonRs.next()) {
					bL_HasData = true;
					//szRTN = TB_CARD_BASE_OPP(pWA);/*Process CardBase Card Status*/
					
					sG_DbNccOppType = L_OppTypeReasonRs.getString("NccOppType");
					sG_DbNegOppReason = L_OppTypeReasonRs.getString("NegOppReason");
					
					sG_DbVisExcepCode = L_OppTypeReasonRs.getString("VisExcepCode");
					sG_DbMstAuthCode = L_OppTypeReasonRs.getString("MstAuthCode");
					sG_DbJcbExcpCode = L_OppTypeReasonRs.getString("JcbExcpCode");
					
					sG_CardNewEndDate = G_CardInfoRs.getString("CardNewEndDate");
					sG_NegDelDate = HpeUtil.getNextMonthDate(sG_CardNewEndDate);
					
					if (updateCardInfo(P_OnBatRs, L_OppTypeReasonRs, sL_OppStatus)) {
						if (CcaOppositionDao.processOppData(P_OnBatRs, G_CardInfoRs, L_OppTypeReasonRs, "ECS004", "ECS 強停", sG_NegDelDate)) {
							writeToOutgoing(P_OnBatRs);
						}
					}
					break;
				}
				L_OppTypeReasonRs.close();
				
				if (!bL_HasData) {
					String sL_IcbcRespDesc = "*無停掛原因*";
					OnBatDao.updateOnBat8(2,G_CurTimestamp , sG_CurDate, L_OnBatRowId, sL_IcbcRespDesc);
				}
			}
			else {
				//str2var(DB_ICBC_RESP_DESC,"*無此卡戶*");
				String sL_IcbcRespDesc = "*無此卡戶*";
				OnBatDao.updateOnBat8(2,G_CurTimestamp, sG_CurDate, L_OnBatRowId, sL_IcbcRespDesc);
			}
		}

	}
	
	   
    private void processOnBatData2(Data004Vo P_Data004Vo) throws Exception{
        //處理強停 onbat result set
            genRecFields(P_Data004Vo);
            if (chkCAIindex(1, P_Data004Vo)) {
                String sL_OppType = P_Data004Vo.getOppType();
                String sL_OppStatus = P_Data004Vo.getOppReason();
                
                ResultSet L_OppTypeReasonRs =  OppTypeReasonDao.getOppTypeReason(sL_OppType, sL_OppStatus,"3", "  ");
                /*
                if (OppTypeReasonDao.isEmptyResultSet(L_OppTypeReasonRs)) {
                    //str2var(DB_ICBC_RESP_DESC,"*無停掛原因*");
                    String sL_IcbcRespDesc = "*無停掛原因*";
                    OnBatDao.updateOnBat8(2,G_CurTimestamp , sG_CurDate, L_OnBatRowId, sL_IcbcRespDesc);
                }
                */
                boolean bL_HasData = false;
                while (L_OppTypeReasonRs.next()) {
                    bL_HasData = true;
                    //szRTN = TB_CARD_BASE_OPP(pWA);/*Process CardBase Card Status*/
                    
                    sG_DbNccOppType = L_OppTypeReasonRs.getString("NccOppType");
                    sG_DbNegOppReason = L_OppTypeReasonRs.getString("NegOppReason");
                    
                    sG_DbVisExcepCode = L_OppTypeReasonRs.getString("VisExcepCode");
                    sG_DbMstAuthCode = L_OppTypeReasonRs.getString("MstAuthCode");
                    sG_DbJcbExcpCode = L_OppTypeReasonRs.getString("JcbExcpCode");
                    
                    sG_CardNewEndDate = G_CardInfoRs.getString("CardNewEndDate");
                    sG_NegDelDate = HpeUtil.getNextMonthDate(sG_CardNewEndDate);
                    
                    if (updateCardInfo(P_Data004Vo, L_OppTypeReasonRs, sL_OppStatus)) {
                        if (CcaOppositionDao.processOppData(P_Data004Vo, G_CardInfoRs, L_OppTypeReasonRs, "ECS004", "ECS 強停", sG_NegDelDate)) {
                            writeToOutgoing(P_Data004Vo);
                        }
                    }
                    break;
                }
                L_OppTypeReasonRs.close();
                
                if (!bL_HasData) {
                    String sL_IcbcRespDesc = "*無停掛原因*";
                }
            }
            else {
                //str2var(DB_ICBC_RESP_DESC,"*無此卡戶*");
                String sL_IcbcRespDesc = "*無此卡戶*";
            }

    }
	
	private boolean writeToOutgoing(ResultSet P_OnBatRs) throws Exception{
		//proc is TB_OUTGOING()
		boolean bL_Result = true;
		String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");
		try {
			if ("37".equals(sL_CardNo.substring(0, 2)))
				return true; //不處理 AE card 

			String sL_TmpExcpCode = sG_DbNegOppReason;
			if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.equals(" "))) {
				//bL_Result = insertOutGoing(1, 1, P_OnBatRs);


				bL_Result = CcaOutgoingDao.insertOutGoing(1, 1, P_OnBatRs, G_CardInfoRs, sG_DbNegOppReason, sG_DbNegCapCode,sG_DbJcbExcpCode,sG_DbMstAuthCode,sG_DbVisExcepCode,sG_OtActCode,"ECS004");
				if (!bL_Result)
					return false;
			}
			String sL_CardCardType = G_CardInfoRs.getString("CardCardType");
			if ("N".equals(sL_CardCardType.substring(0, 1))) /*NCCC-U卡*/
				return true;

			String sL_CardBinType = G_CardInfoRs.getString("CardBinType");

			sL_TmpExcpCode="";
			if ("J".equals(sL_CardBinType))
				sL_TmpExcpCode = sG_DbJcbExcpCode;//DB_JCB_EXCP_CODE
			else if ("M".equals(sL_CardBinType))
				sL_TmpExcpCode = sG_DbMstAuthCode;//DB_MST_AUTH_CODE
			else if ("V".equals(sL_CardBinType))
				sL_TmpExcpCode = sG_DbVisExcepCode; //DB_VIS_EXCEP_CODE
			if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.substring(0, 1).equals(" "))) {
				bL_Result = insertOutGoing(2, 1, P_OnBatRs);
				if (!bL_Result)
					return false;
			}

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

    private boolean writeToOutgoing(Data004Vo P_Data004Vo) throws Exception{
        //proc is TB_OUTGOING()
        boolean bL_Result = true;
        String sL_CardNo = P_Data004Vo.getCardNo();
        try {
            if ("37".equals(sL_CardNo.substring(0, 2)))
                return true; //不處理 AE card 

            String sL_TmpExcpCode = sG_DbNegOppReason;
            if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.equals(" "))) {
                //bL_Result = insertOutGoing(1, 1, P_OnBatRs);


                bL_Result = CcaOutgoingDao.insertOutGoing(1, 1, P_Data004Vo, G_CardInfoRs, sG_DbNegOppReason, sG_DbNegCapCode,sG_DbJcbExcpCode,sG_DbMstAuthCode,sG_DbVisExcepCode,sG_OtActCode,"ECS004");
                if (!bL_Result)
                    return false;
            }
            String sL_CardCardType = G_CardInfoRs.getString("CardCardType");
            if ("N".equals(sL_CardCardType.substring(0, 1))) /*NCCC-U卡*/
                return true;

            String sL_CardBinType = G_CardInfoRs.getString("CardBinType");

            sL_TmpExcpCode="";
            if ("J".equals(sL_CardBinType))
                sL_TmpExcpCode = sG_DbJcbExcpCode;//DB_JCB_EXCP_CODE
            else if ("M".equals(sL_CardBinType))
                sL_TmpExcpCode = sG_DbMstAuthCode;//DB_MST_AUTH_CODE
            else if ("V".equals(sL_CardBinType))
                sL_TmpExcpCode = sG_DbVisExcepCode; //DB_VIS_EXCEP_CODE
            if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.substring(0, 1).equals(" "))) {
                bL_Result = insertOutGoing(2, 1, P_Data004Vo);
                if (!bL_Result)
                    return false;
            }

            
        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        
        return bL_Result;
    }
    
	private boolean updateCardInfo(ResultSet P_OnBatRs, ResultSet P_OppTypeReasonRs, String sL_OppStatus) throws Exception{
		
		boolean bL_Result = true;
		try {
			CcaCardBaseDao.updateCcaCardBase(P_OnBatRs, P_OppTypeReasonRs, sL_OppStatus);
			
			String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");
			String sL_DebitFlag = P_OnBatRs.getString("OnBatDebitFlag");
			String sL_NewCurCode = P_OnBatRs.getString("OnBatOppType");
			if ("Y".equals(sL_DebitFlag)) {
				DbcCardDao.updateCurCode(sL_CardNo, sL_NewCurCode, sG_CurDate);
				G_CardInfoRs =  DbcCardDao.getDbcCardByCardNo(sL_CardNo);
			}
			else {
				CrdCardDao.updateCurCode(sL_CardNo, sL_NewCurCode, sG_CurDate);
				G_CardInfoRs =  CrdCardDao.getCrdCardByCardNo(sL_CardNo);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

    private boolean updateCardInfo(Data004Vo P_Data004Vo, ResultSet P_OppTypeReasonRs, String sL_OppStatus) throws Exception{
        
        boolean bL_Result = true;
        try {
            CcaCardBaseDao.updateCcaCardBase(P_Data004Vo, P_OppTypeReasonRs, sL_OppStatus);
            
            String sL_CardNo = P_Data004Vo.getCardNo();
            String sL_DebitFlag = P_Data004Vo.getDebitFlag();
            String sL_NewCurCode = P_Data004Vo.getOppType();
            if ("Y".equals(sL_DebitFlag)) {
                DbcCardDao.updateCurCode(sL_CardNo, sL_NewCurCode, sG_CurDate);
                G_CardInfoRs =  DbcCardDao.getDbcCardByCardNo(sL_CardNo);
            }
            else {
                CrdCardDao.updateCurCode(sL_CardNo, sL_NewCurCode, sG_CurDate);
                G_CardInfoRs =  CrdCardDao.getCrdCardByCardNo(sL_CardNo);
            }
            
        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        
        return bL_Result;
    }
    
	private void processOnBatData1(ResultSet P_OnBatRs) throws Exception{
		//處理解禁超 onbat result set
		
		while(P_OnBatRs.next()) {
			RowId L_OnBatRowId = P_OnBatRs.getRowId("OnBatRowId");
			genRecFields(P_OnBatRs);
			if (chkCAIindex(1, P_OnBatRs)) {

				if (processCardAcct(P_OnBatRs)) {
					OnBatDao.updateOnBat7(1,G_CurTimestamp, sG_CurDate, L_OnBatRowId);
				}
				else {
					String sL_IcbcRespDesc = "*卡戶更新失敗*";
					OnBatDao.updateOnBat8(2,G_CurTimestamp, sG_CurDate, L_OnBatRowId, sL_IcbcRespDesc);
				}
			}
			else {
				String sL_IcbcRespDesc = "*無此卡戶索引*";
				OnBatDao.updateOnBat8(2,G_CurTimestamp, sG_CurDate, L_OnBatRowId, sL_IcbcRespDesc);

			}
		}
	}
	

    private void processOnBatData1(Data004Vo P_Data004Vo) throws Exception{
        //處理解禁超 onbat result set
        
            genRecFields(P_Data004Vo);
            if (chkCAIindex(1, P_Data004Vo)) {

                if (processCardAcct(P_Data004Vo)) {
                }
                else {
                    String sL_IcbcRespDesc = "*卡戶更新失敗*";
                }
            }
            else {
                String sL_IcbcRespDesc = "*無此卡戶索引*";

            }
    }
	
	private boolean chkCAIindex(int nP_Type, ResultSet P_OnBatRs) {
		boolean bL_Result = true;
		
		String sL_TmpCardAcctId="", sL_TmpCardCorpId="", sL_TmpCardAcctClass="", sL_TmpEcsAcctClass="";
		String sL_TmpCardAcctIdSeq="", sL_TmpCardCorpIdSeq="";
		String sL_AcctNo="";
		ResultSet L_CardAcctIndexRs = null;
		try {
			if (nP_Type==1) {
				sL_TmpCardAcctId=sG_CaiCardAcctClass;
				sL_TmpCardCorpId=sG_CaiCardCorpId;
				sL_TmpCardAcctClass=sG_CaiCardAcctClass;
				sL_TmpEcsAcctClass= sG_CaiEcsAcctClass;
			}
			else if (nP_Type==2){
				sL_TmpCardAcctIdSeq="";		
				sL_TmpCardCorpId = sG_CaiCardAcctId.substring(0, 8);
				sL_TmpCardCorpIdSeq="";
				sL_TmpCardAcctClass=sG_CaiCardAcctClass.substring(0, 1);
				sL_TmpEcsAcctClass = sG_CaiEcsAcctClass;
			}

			if (!"90".equals(P_OnBatRs.getString("OnBatAccountType"))) {
				sL_AcctNo="";
				L_CardAcctIndexRs = CardAcctIndexDao.getCardAcctIndex5(sL_TmpCardAcctId, sL_TmpCardCorpId, sL_TmpEcsAcctClass, sL_AcctNo);
			}
			else {
				sL_AcctNo = P_OnBatRs.getString("OnBatBlockAcctNo");
				L_CardAcctIndexRs = CardAcctIndexDao.getCardAcctIndex5(sL_TmpCardAcctId, sL_TmpCardCorpId, sL_TmpEcsAcctClass, sL_AcctNo);				
			}
			nG_CaiAcctIdx=0;
			nG_CaiAcctParentIndex=0;
			
			while (L_CardAcctIndexRs.next()) {
				nG_CaiAcctIdx = L_CardAcctIndexRs.getInt("CardAcctIdx");
				nG_CaiAcctParentIndex = L_CardAcctIndexRs.getInt("AcctParentIndex");
			}
			/*
			if (!CardAcctIndexDao.isEmptyResultSet(L_CardAcctIndexRs)) {
				
				
				nG_CaiAcctIdx = L_CardAcctIndexRs.getInt("CardAcctIdx");
				nG_CaiAcctParentIndex = L_CardAcctIndexRs.getInt("AcctParentIndex");
				
			}
			else {
				nG_CaiAcctIdx=0;
				nG_CaiAcctParentIndex=0;
			}
			*/
			L_CardAcctIndexRs.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		 
	}
    
    private boolean chkCAIindex(int nP_Type, Data004Vo P_Data004Vo) {
        boolean bL_Result = true;
        
        String sL_TmpCardAcctId="", sL_TmpCardCorpId="", sL_TmpCardAcctClass="", sL_TmpEcsAcctClass="";
        String sL_TmpCardAcctIdSeq="", sL_TmpCardCorpIdSeq="";
        String sL_AcctNo="";
        ResultSet L_CardAcctIndexRs = null;
        try {
            if (nP_Type==1) {
                sL_TmpCardAcctId=sG_CaiCardAcctClass;
                sL_TmpCardCorpId=sG_CaiCardCorpId;
                sL_TmpCardAcctClass=sG_CaiCardAcctClass;
                sL_TmpEcsAcctClass= sG_CaiEcsAcctClass;
            }
            else if (nP_Type==2){
                sL_TmpCardAcctIdSeq="";     
                sL_TmpCardCorpId = sG_CaiCardAcctId.substring(0, 8);
                sL_TmpCardCorpIdSeq="";
                sL_TmpCardAcctClass=sG_CaiCardAcctClass.substring(0, 1);
                sL_TmpEcsAcctClass = sG_CaiEcsAcctClass;
            }

            if (!"90".equals(P_Data004Vo.getAccountType())) {
                sL_AcctNo="";
                L_CardAcctIndexRs = CardAcctIndexDao.getCardAcctIndex5(sL_TmpCardAcctId, sL_TmpCardCorpId, sL_TmpEcsAcctClass, sL_AcctNo);
            }
            else {
                sL_AcctNo = P_Data004Vo.getAcctNo();
                L_CardAcctIndexRs = CardAcctIndexDao.getCardAcctIndex5(sL_TmpCardAcctId, sL_TmpCardCorpId, sL_TmpEcsAcctClass, sL_AcctNo);              
            }
            nG_CaiAcctIdx=0;
            nG_CaiAcctParentIndex=0;
            
            while (L_CardAcctIndexRs.next()) {
                nG_CaiAcctIdx = L_CardAcctIndexRs.getInt("CardAcctIdx");
                nG_CaiAcctParentIndex = L_CardAcctIndexRs.getInt("AcctParentIndex");
            }
            /*
            if (!CardAcctIndexDao.isEmptyResultSet(L_CardAcctIndexRs)) {
                
                
                nG_CaiAcctIdx = L_CardAcctIndexRs.getInt("CardAcctIdx");
                nG_CaiAcctParentIndex = L_CardAcctIndexRs.getInt("AcctParentIndex");
                
            }
            else {
                nG_CaiAcctIdx=0;
                nG_CaiAcctParentIndex=0;
            }
            */
            L_CardAcctIndexRs.close();
            
        } catch (Exception e) {
            // TODO: handle exception
            bL_Result = false;
        }
        
        return bL_Result;
         
    }
	
	private boolean processCardAcct(ResultSet P_OnBatRs) {
		//proc is TB_CARD_ACCT()
		boolean bL_Result = true;
		try {
			String sL_CaStatus="", sL_UpSpecRemark="", sL_UpSpecStatus="", sL_UpSpecDelDate=""; 
			String sL_TmpBlockCode="", sL_OrgBlockCode="";
			String sL_UpBlockCode1="", sL_UpBlockCode2="",sL_UpBlockCode3="",sL_UpBlockCode4="",sL_UpBlockCode5="";
			CardAcctVo L_CardAcctVo = CardAcctDao.getCardAcct(nG_CaiAcctIdx);
			sL_UpBlockCode1= L_CardAcctVo.getBlockReason1().trim();
			sL_UpBlockCode2= L_CardAcctVo.getBlockReason2().trim();
			sL_UpBlockCode3= L_CardAcctVo.getBlockReason3().trim();
			sL_UpBlockCode4= L_CardAcctVo.getBlockReason4().trim();
			sL_UpBlockCode5= L_CardAcctVo.getBlockReason5().trim();

			String sL_OnBatMatchFlag = P_OnBatRs.getString("OnBatMatchFlag").trim();
			String sL_OnBatBlock1 = P_OnBatRs.getString("OnBatBlockCode1").trim();
			String sL_OnBatBlock2 = P_OnBatRs.getString("OnBatBlockCode2").trim();
			String sL_OnBatBlock3 = P_OnBatRs.getString("OnBatBlockCode3").trim();
			String sL_OnBatBlock4 = P_OnBatRs.getString("OnBatBlockCode4").trim();
			String sL_OnBatBlock5 = P_OnBatRs.getString("OnBatBlockCode5").trim();
			
			if ((sL_OnBatMatchFlag.length()==0) || (sL_OnBatMatchFlag.equals("1")) ) {
				sL_UpBlockCode1=sL_OnBatBlock1;
				sL_TmpBlockCode=sL_OnBatBlock1;
				sL_OrgBlockCode=L_CardAcctVo.getBlockReason1().trim();
			}
			else if ((sL_OnBatMatchFlag.equals("2")) ) {
				sL_UpBlockCode2=sL_OnBatBlock2;
				sL_TmpBlockCode=sL_OnBatBlock2;
				sL_OrgBlockCode=L_CardAcctVo.getBlockReason2().trim();
			}
			else if ((sL_OnBatMatchFlag.equals("3")) ) {
				sL_UpBlockCode3=sL_OnBatBlock3;
				sL_TmpBlockCode=sL_OnBatBlock3;
				sL_OrgBlockCode=L_CardAcctVo.getBlockReason3().trim();
			}
			else if ((sL_OnBatMatchFlag.equals("4")) ) {
				sL_UpBlockCode4=sL_OnBatBlock4;
				sL_TmpBlockCode=sL_OnBatBlock4;
				sL_OrgBlockCode=L_CardAcctVo.getBlockReason4().trim();
			}
			else if ((sL_OnBatMatchFlag.equals("5")) ) {
				sL_UpBlockCode5=sL_OnBatBlock5;
				sL_TmpBlockCode=sL_OnBatBlock5;
				sL_OrgBlockCode=L_CardAcctVo.getBlockReason5().trim();
			}
			
			sG_DbOppReason = sL_TmpBlockCode;//sG_DbOppReason == DB_OPP_REASON
			
			ResultSet L_SpecCodeRs = null;
			if (sL_TmpBlockCode.length()!=0) {
				L_SpecCodeRs = CcaSpecCodeDao.getReason(sL_TmpBlockCode);
			}
			else {
				L_SpecCodeRs = CcaSpecCodeDao.getReason(sL_OrgBlockCode);
			}
			//if (!CcaSpecCodeDao.isEmptyResultSet(L_SpecCodeRs)) {
			while (L_SpecCodeRs.next()) {
			
				sG_DbNegOppReason = L_SpecCodeRs.getString("NegReason");
				ResultSet L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1("NCCC", sG_DbNegOppReason);
				sG_DbNegCapCode = L_SysParm1Rs.getString("SysData2");
				int nL_BlockValue=0; //proc is szBlock
				
				if (sL_UpBlockCode1.trim().length()==0)
					nL_BlockValue++;
				if (sL_UpBlockCode2.trim().length()==0)
					nL_BlockValue++;
				if (sL_UpBlockCode3.trim().length()==0)
					nL_BlockValue++;
				if (sL_UpBlockCode4.trim().length()==0)
					nL_BlockValue++;
				if (sL_UpBlockCode5.trim().length()==0)
					nL_BlockValue++;
				if (nL_BlockValue==5) {
					sL_CaStatus="N";
					sG_CardStatus="N";
					sL_UpSpecRemark="ECS卡戶解凍";
					sL_UpSpecDelDate=sG_CurDate;
				}
				else {
					sL_CaStatus="Y";
					sG_CardStatus="Y";
					sL_UpSpecRemark="ECS卡戶凍結";
					sL_UpSpecDelDate="";
				}
				sL_UpSpecStatus = sL_TmpBlockCode;
				
				CardAcctDao.updateCardAcctBlockData(nG_CaiAcctIdx, sL_OnBatBlock1, sL_OnBatBlock2, sL_OnBatBlock3, sL_OnBatBlock4, sL_OnBatBlock5, 
							sL_CaStatus, sL_UpSpecRemark, sL_UpSpecStatus, 
							sG_CurDate, sL_UpSpecDelDate, "ECS004");

				
				ResultSet L_CardInfoRs = null;
				String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");
				String sL_DebitFlag = P_OnBatRs.getString("OnBatDebitFlag");
				if ("Y".equals(sL_DebitFlag)) {
					L_CardInfoRs =  DbcCardDao.getDbcCardByCardNo(sL_CardNo);
				}
				else {
					L_CardInfoRs =  CrdCardDao.getCrdCardByCardNo(sL_CardNo);
				}
				String sL_CurrentCode = L_CardInfoRs.getString("CardCurrentCode");
				L_CardInfoRs.close();

				if ("N".equals(sL_CaStatus)) {
					if ("0".equals(sL_CurrentCode))
						write2Outgoing(2, P_OnBatRs);
						
					//TB_CARD_BASE_SPEC_N
					//if DB_OPP_TYPE.arr,"0",1) == 0)  =>CrdCardDao.CurrentCode=0
					//=>TB_OUTGOING_SPEC(2);
					
				}
				else{
					if (("0".equals(sL_CurrentCode)) && (sG_DbOppReason.length()>0))
						write2Outgoing(1, P_OnBatRs);

					//TB_CARD_BASE_SPEC_Y
					
					
					//if DB_OPP_TYPE.arr,"0",1) == 0)&&(DB_OPP_REASON.len>0)
					//=>TB_OUTGOING_SPEC(1);
				}
				
			}
			L_SpecCodeRs.close();			
			
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result=false;
		}
		
		return bL_Result;
	}
	

    private boolean processCardAcct(Data004Vo P_Data004Vo) {
        //proc is TB_CARD_ACCT()
        boolean bL_Result = true;
        try {
            String sL_CaStatus="", sL_UpSpecRemark="", sL_UpSpecStatus="", sL_UpSpecDelDate=""; 
            String sL_TmpBlockCode="", sL_OrgBlockCode="";
            String sL_UpBlockCode1="", sL_UpBlockCode2="",sL_UpBlockCode3="",sL_UpBlockCode4="",sL_UpBlockCode5="";
            CardAcctVo L_CardAcctVo = CardAcctDao.getCardAcct(nG_CaiAcctIdx);
            sL_UpBlockCode1= L_CardAcctVo.getBlockReason1().trim();
            sL_UpBlockCode2= L_CardAcctVo.getBlockReason2().trim();
            sL_UpBlockCode3= L_CardAcctVo.getBlockReason3().trim();
            sL_UpBlockCode4= L_CardAcctVo.getBlockReason4().trim();
            sL_UpBlockCode5= L_CardAcctVo.getBlockReason5().trim();

            String sL_OnBatMatchFlag = P_Data004Vo.getMatchFlag().trim();
            String sL_OnBatBlock1 = P_Data004Vo.getBlockCode1().trim();
            String sL_OnBatBlock2 = P_Data004Vo.getBlockCode2().trim();
            String sL_OnBatBlock3 = P_Data004Vo.getBlockCode3().trim();
            String sL_OnBatBlock4 = P_Data004Vo.getBlockCode4().trim();
            String sL_OnBatBlock5 = P_Data004Vo.getBlockCode5().trim();
            
            if ((sL_OnBatMatchFlag.length()==0) || (sL_OnBatMatchFlag.equals("1")) ) {
                sL_UpBlockCode1=sL_OnBatBlock1;
                sL_TmpBlockCode=sL_OnBatBlock1;
                sL_OrgBlockCode=L_CardAcctVo.getBlockReason1().trim();
            }
            else if ((sL_OnBatMatchFlag.equals("2")) ) {
                sL_UpBlockCode2=sL_OnBatBlock2;
                sL_TmpBlockCode=sL_OnBatBlock2;
                sL_OrgBlockCode=L_CardAcctVo.getBlockReason2().trim();
            }
            else if ((sL_OnBatMatchFlag.equals("3")) ) {
                sL_UpBlockCode3=sL_OnBatBlock3;
                sL_TmpBlockCode=sL_OnBatBlock3;
                sL_OrgBlockCode=L_CardAcctVo.getBlockReason3().trim();
            }
            else if ((sL_OnBatMatchFlag.equals("4")) ) {
                sL_UpBlockCode4=sL_OnBatBlock4;
                sL_TmpBlockCode=sL_OnBatBlock4;
                sL_OrgBlockCode=L_CardAcctVo.getBlockReason4().trim();
            }
            else if ((sL_OnBatMatchFlag.equals("5")) ) {
                sL_UpBlockCode5=sL_OnBatBlock5;
                sL_TmpBlockCode=sL_OnBatBlock5;
                sL_OrgBlockCode=L_CardAcctVo.getBlockReason5().trim();
            }
            
            sG_DbOppReason = sL_TmpBlockCode;//sG_DbOppReason == DB_OPP_REASON
            
            ResultSet L_SpecCodeRs = null;
            if (sL_TmpBlockCode.length()!=0) {
                L_SpecCodeRs = CcaSpecCodeDao.getReason(sL_TmpBlockCode);
            }
            else {
                L_SpecCodeRs = CcaSpecCodeDao.getReason(sL_OrgBlockCode);
            }
            //if (!CcaSpecCodeDao.isEmptyResultSet(L_SpecCodeRs)) {
            while (L_SpecCodeRs.next()) {
            
                sG_DbNegOppReason = L_SpecCodeRs.getString("NegReason");
                ResultSet L_SysParm1Rs =  CcaSysParm1Dao.getCcaSysParm1("NCCC", sG_DbNegOppReason);
                sG_DbNegCapCode = L_SysParm1Rs.getString("SysData2");
                int nL_BlockValue=0; //proc is szBlock
                
                if (sL_UpBlockCode1.trim().length()==0)
                    nL_BlockValue++;
                if (sL_UpBlockCode2.trim().length()==0)
                    nL_BlockValue++;
                if (sL_UpBlockCode3.trim().length()==0)
                    nL_BlockValue++;
                if (sL_UpBlockCode4.trim().length()==0)
                    nL_BlockValue++;
                if (sL_UpBlockCode5.trim().length()==0)
                    nL_BlockValue++;
                if (nL_BlockValue==5) {
                    sL_CaStatus="N";
                    sG_CardStatus="N";
                    sL_UpSpecRemark="ECS卡戶解凍";
                    sL_UpSpecDelDate=sG_CurDate;
                }
                else {
                    sL_CaStatus="Y";
                    sG_CardStatus="Y";
                    sL_UpSpecRemark="ECS卡戶凍結";
                    sL_UpSpecDelDate="";
                }
                sL_UpSpecStatus = sL_TmpBlockCode;
                
                CardAcctDao.updateCardAcctBlockData(nG_CaiAcctIdx, sL_OnBatBlock1, sL_OnBatBlock2, sL_OnBatBlock3, sL_OnBatBlock4, sL_OnBatBlock5, 
                            sL_CaStatus, sL_UpSpecRemark, sL_UpSpecStatus, 
                            sG_CurDate, sL_UpSpecDelDate, "ECS004");

                
                ResultSet L_CardInfoRs = null;
                String sL_CardNo = P_Data004Vo.getCardNo();
                String sL_DebitFlag = P_Data004Vo.getDebitFlag();
                if ("Y".equals(sL_DebitFlag)) {
                    L_CardInfoRs =  DbcCardDao.getDbcCardByCardNo(sL_CardNo);
                }
                else {
                    L_CardInfoRs =  CrdCardDao.getCrdCardByCardNo(sL_CardNo);
                }
                String sL_CurrentCode = L_CardInfoRs.getString("CardCurrentCode");
                L_CardInfoRs.close();

                if ("N".equals(sL_CaStatus)) {
                    if ("0".equals(sL_CurrentCode))
                        write2Outgoing(2, P_Data004Vo);
                        
                    //TB_CARD_BASE_SPEC_N
                    //if DB_OPP_TYPE.arr,"0",1) == 0)  =>CrdCardDao.CurrentCode=0
                    //=>TB_OUTGOING_SPEC(2);
                    
                }
                else{
                    if (("0".equals(sL_CurrentCode)) && (sG_DbOppReason.length()>0))
                        write2Outgoing(1, P_Data004Vo);

                    //TB_CARD_BASE_SPEC_Y
                    
                    
                    //if DB_OPP_TYPE.arr,"0",1) == 0)&&(DB_OPP_REASON.len>0)
                    //=>TB_OUTGOING_SPEC(1);
                }
                
            }
            L_SpecCodeRs.close();           
            
            
            
            
        } catch (Exception e) {
            // TODO: handle exception
            bL_Result=false;
        }
        
        return bL_Result;
    }
	
	private boolean write2Outgoing(int nP_Type, Data004Vo P_Data004Vo) throws Exception{
		//proc is TB_OUTGOING_SPEC(1) and TB_OUTGOING_SPEC(2)
		boolean bL_Result = true;
		String sL_CardNo = P_Data004Vo.getCardNo();
		
		if ("37".equals(sL_CardNo.substring(0, 2)))
			return true; //不處理 AE card 
		
		String sL_TmpExcpCode = sG_DbNegOppReason;
		if (nP_Type==1) {
			sG_OtActCode="A";
			
		}
		else if (nP_Type==2) {
			sG_OtActCode="D";	
		}
		
		if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.substring(0, 1).equals(" "))) {
			bL_Result = insertOutGoing(1, nP_Type, P_Data004Vo);
			if (!bL_Result)
				return false;
		}
		String sL_CardCardType = G_CardInfoRs.getString("CardCardType");
		if ("N".equals(sL_CardCardType.substring(0, 1))) /*NCCC-U卡*/
			return true;
		
		String sL_CardBinType = G_CardInfoRs.getString("CardBinType");

		sL_TmpExcpCode="";
		if ("J".equals(sL_CardBinType))
			sL_TmpExcpCode = sG_DbJcbExcpCode;//DB_JCB_EXCP_CODE
		else if ("M".equals(sL_CardBinType))
			sL_TmpExcpCode = sG_DbMstAuthCode;//DB_MST_AUTH_CODE
		else if ("V".equals(sL_CardBinType))
			sL_TmpExcpCode = sG_DbVisExcepCode; //DB_VIS_EXCEP_CODE
		if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.substring(0, 1).equals(" "))) {
			bL_Result = insertOutGoing(2, 1, P_Data004Vo);
			if (!bL_Result)
				return false;
		}

		
		return bL_Result;
	}
	

    private boolean write2Outgoing(int nP_Type, ResultSet P_OnBatRs ) throws Exception{
        //proc is TB_OUTGOING_SPEC(1) and TB_OUTGOING_SPEC(2)
        boolean bL_Result = true;
        String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");
        
        if ("37".equals(sL_CardNo.substring(0, 2)))
            return true; //不處理 AE card 
        
        String sL_TmpExcpCode = sG_DbNegOppReason;
        if (nP_Type==1) {
            sG_OtActCode="A";
            
        }
        else if (nP_Type==2) {
            sG_OtActCode="D";   
        }
        
        if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.substring(0, 1).equals(" "))) {
            bL_Result = insertOutGoing(1, nP_Type, P_OnBatRs);
            if (!bL_Result)
                return false;
        }
        String sL_CardCardType = G_CardInfoRs.getString("CardCardType");
        if ("N".equals(sL_CardCardType.substring(0, 1))) /*NCCC-U卡*/
            return true;
        
        String sL_CardBinType = G_CardInfoRs.getString("CardBinType");

        sL_TmpExcpCode="";
        if ("J".equals(sL_CardBinType))
            sL_TmpExcpCode = sG_DbJcbExcpCode;//DB_JCB_EXCP_CODE
        else if ("M".equals(sL_CardBinType))
            sL_TmpExcpCode = sG_DbMstAuthCode;//DB_MST_AUTH_CODE
        else if ("V".equals(sL_CardBinType))
            sL_TmpExcpCode = sG_DbVisExcepCode; //DB_VIS_EXCEP_CODE
        if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.substring(0, 1).equals(" "))) {
            bL_Result = insertOutGoing(2, 1, P_OnBatRs);
            if (!bL_Result)
                return false;
        }

        
        return bL_Result;
    }
	
	private boolean insertOutGoing(int nP_Opt, int nP_Type, ResultSet P_OnBatRs) throws Exception{
		//proc is Gen_IsoDetailString()
	    
	    String ls_ncccnew = "0";
	    String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");
        String sL_CardCardType = G_CardInfoRs.getString("CardCardType");
        String sL_CardBinType = G_CardInfoRs.getString("CardBinType");


        if (CcaSysParm3Dao.getCcaSysParm3("NCCC", "VERSION", true, "N")) {
            ls_ncccnew = CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1;
        }

        AuthGate gate = new AuthGate();
        if (ls_ncccnew.equals("1"))
            gate.bicHead = "ISO086000051";
        else
            gate.bicHead = "ISO085000051";
        gate.mesgType = "0300";

        gate.isoField[2] = sL_CardNo;
        /**** AE open card ****/
        if (gate.isoField[2].substring(0, 6).equals("900002") == false) {
            if (sL_CardNo.substring(0, 4).equals("4000")) {
                gate.isoField[2] = "9" + sL_CardNo.substring(1, 16);
            } else {
                gate.isoField[2] = sL_CardNo.substring(0, 16);
            }
        }
        String db_SYS_SEQ_NO = SystemDao.getNextSeqVal("ECS_TRACE_NO");

        gate.isoField[7] = HpeUtil.getCurDateStr("").substring(4,8) + HpeUtil.getCurTimeStr();
        gate.isoField[11] = String.format("%06d", Integer.parseInt(db_SYS_SEQ_NO));

        gate.isoField[48] = "000BK0231"
                + "0000000000000000000000000000000000000000000000000000000000000000000";
        gate.isoField[49] = "901";
        if (ls_ncccnew.equals("1"))
            gate.isoField[60] = "90020000PRO200000000000000YY";
        else
            gate.isoField[60] = "90020000PRO100000000000000YY";
        gate.isoField[60] += "000000000000000000000000000000";

        if (nP_Opt != 1){
            if ((sL_CardBinType.equals("V") ||
                (sL_CardBinType.equals("J")))){   /*VISA/JCB卡*/
                if ("J".equals(sL_CardBinType))
                    gate.isoField[73]="000000";
                else
                    gate.isoField[73]=sG_NegDelDate;
            }
         }
        
        gate.isoField[91]="1"; //add file
        if (nP_Type==2) { //delete file
            gate.isoField[91]="3";//delete file
            if (("J".equals(sL_CardBinType)) && (nP_Opt!=1) ){
                gate.isoField[91]="0";
            }
        }
        
        gate.isoField[101] = "NF";
        gate.isoField[120] = "NF";

        if (nP_Opt == 1) {
            gate.isoField[101]= "NF";
            
            
            if (nP_Type==1) {/** add(凍結) 才需待出以下資料 **/
                //down, process field 120
                gate.isoField[120] = HpeUtil.fillCharOnRight(sL_CardCardType, 2, " ");
                gate.isoField[120] += sG_DbNegOppReason + sG_DbNegCapCode;
                gate.isoField[120] += sG_CurDate.substring(2, 8) + sG_NegDelDate.substring(2, 6);
                //up, process field 120
            }
        }
        else {/*Outgoing VISA or Master or JCB*/
            if ("J".equals(sL_CardBinType)) { //JCB Exception File
                gate.isoField[101]= "6332";
                
                //down, process field 120
                gate.isoField[120] = "PATH" + sL_CardNo.substring(0, 6);
                
                if (nP_Type==1) 
                    gate.isoField[120] += "301"; /*1 for Add 2 for update*/
                else
                    gate.isoField[120] += "170"; /* for delete */
                gate.isoField[120] += sL_CardNo;
                
                if (nP_Type==1) {
                    gate.isoField[120] += sG_DbJcbExcpCode;
                    gate.isoField[120] += sG_NegDelDate.substring(2, 6);
                    gate.isoField[120] += "00000";/*JCB region code*/
                }
                else {
                    gate.isoField[120] += "             "; /* 13 spaces */
                }
                //up, process field 120

            }
            else if ("M".equals(sL_CardBinType)) {
                gate.isoField[101]= "MC";
                //down, process field 120
//                if (nP_Type==1)
//                    sL_IsoField120 = "0092"; //Howard: 新的spec應該是 "0092", old proc is "0075"
//                else
//                    sL_IsoField120 = "0047";
                
                gate.isoField[120] += "PATHMCC" ;
                if (nP_Type==1) 
                    gate.isoField[120] += "1"; /*1 for Add 2 for update*/
                else
                    gate.isoField[120] += "3"; /* for delete */
                gate.isoField[120] += "MCC102         " + "00000";
                gate.isoField[120] += sL_CardNo + "   ";
                
                if (nP_Type==1) { /* add data need to send */
                    gate.isoField[120] += sG_DbMstAuthCode;
                    gate.isoField[120] += "000000000000";
                    gate.isoField[120] += "000000000000";
                    gate.isoField[120] += "   ";
                    gate.isoField[120] += "        ";//"PurgeDate(YYYYMMDD)";
                    gate.isoField[120] += "   ";// "CardSeqNo(3 bytes)";
                    gate.isoField[120] += sG_CardNewEndDate.substring(0,6);//expire date (YYYYMM)
                    
                }
                //up, process field 120
            }
            else { //for VISA card (VISA Exception File)
                gate.isoField[101]= "VP";
                //down, process field 120
                gate.isoField[120] = "PCAS";
                if (nP_Type==1) 
                    gate.isoField[120] += "BA"; /* for add or update */
                else
                    gate.isoField[120] += "BD"; /* for delete */
                
                
                gate.isoField[120] += HpeUtil.fillCharOnRight(sL_CardNo, 28, " ");
                gate.isoField[120] += "   "; /*3 Byte Country code*/
                gate.isoField[120] += "  ";/*2 Byte Operator ID*/
                
                if (sG_DbVisExcepCode.length()==0) {
                    gate.isoField[120] += "05";
                
                }
                else {
                    gate.isoField[120] += sG_DbVisExcepCode;
                }
                gate.isoField[120] += "0        "; /*9 Byte Region code*/
                //up, process field 120
            }
        }

        BicFormat bic = new BicFormat(null, gate, null);
        if (bic.host2Iso()==false) {
            return false;
        }

        
		boolean bL_Result=true;
		
		//down, insert into table outgoing
        String sql = "   INSERT INTO CCA_OUTGOING " 
                + "         (CARD_NO,KEY_VALUE,KEY_TABLE, "
                + "          BITMAP,PROC_FLAG,SEND_TIMES,CRT_DATE,CRT_TIME, "
                + "          CRT_USER,PROC_DATE,PROC_TIME,PROC_USER,ACT_CODE) "
                + "   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ";
        CcaOutgoingDao.insertOutgoing(sql, sL_CardNo, "NCCC", "OPPOSITION", gate.isoString.substring(2), "0", 0,
                HpeUtil.getCurDateStr(""), HpeUtil.getCurTimeStr(), "ECS004", HpeUtil.getCurDateStr(""),
                HpeUtil.getCurTimeStr(), "", "A");
		//up, insert into table outgoing
		
		return bL_Result;
		
	}
	

    private boolean insertOutGoing(int nP_Opt, int nP_Type, Data004Vo P_Data004Vo) throws Exception{
        //proc is Gen_IsoDetailString()
        
        String ls_ncccnew = "0";
        String sL_CardNo = P_Data004Vo.getCardNo();
        String sL_CardCardType = G_CardInfoRs.getString("CardCardType");
        String sL_CardBinType = G_CardInfoRs.getString("CardBinType");


        if (CcaSysParm3Dao.getCcaSysParm3("NCCC", "VERSION", true, "N")) {
            ls_ncccnew = CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1;
        }

        AuthGate gate = new AuthGate();
        if (ls_ncccnew.equals("1"))
            gate.bicHead = "ISO086000051";
        else
            gate.bicHead = "ISO085000051";
        gate.mesgType = "0300";

        gate.isoField[2] = sL_CardNo;
        /**** AE open card ****/
        if (gate.isoField[2].substring(0, 6).equals("900002") == false) {
            if (sL_CardNo.substring(0, 4).equals("4000")) {
                gate.isoField[2] = "9" + sL_CardNo.substring(1, 16);
            } else {
                gate.isoField[2] = sL_CardNo.substring(0, 16);
            }
        }
        String db_SYS_SEQ_NO = SystemDao.getNextSeqVal("ECS_TRACE_NO");

        gate.isoField[7] = HpeUtil.getCurDateStr("").substring(4,8) + HpeUtil.getCurTimeStr();
        gate.isoField[11] = String.format("%06d", Integer.parseInt(db_SYS_SEQ_NO));

        gate.isoField[48] = "000BK0231"
                + "0000000000000000000000000000000000000000000000000000000000000000000";
        gate.isoField[49] = "901";
        if (ls_ncccnew.equals("1"))
            gate.isoField[60] = "90020000PRO200000000000000YY";
        else
            gate.isoField[60] = "90020000PRO100000000000000YY";
        gate.isoField[60] += "000000000000000000000000000000";

        if (nP_Opt != 1){
            if ((sL_CardBinType.equals("V") ||
                (sL_CardBinType.equals("J")))){   /*VISA/JCB卡*/
                if ("J".equals(sL_CardBinType))
                    gate.isoField[73]="000000";
                else
                    gate.isoField[73]=sG_NegDelDate;
            }
         }
        
        gate.isoField[91]="1"; //add file
        if (nP_Type==2) { //delete file
            gate.isoField[91]="3";//delete file
            if (("J".equals(sL_CardBinType)) && (nP_Opt!=1) ){
                gate.isoField[91]="0";
            }
        }
        
        gate.isoField[101] = "NF";
        gate.isoField[120] = "NF";

        if (nP_Opt == 1) {
            gate.isoField[101]= "NF";
            
            
            if (nP_Type==1) {/** add(凍結) 才需待出以下資料 **/
                //down, process field 120
                gate.isoField[120] = HpeUtil.fillCharOnRight(sL_CardCardType, 2, " ");
                gate.isoField[120] += sG_DbNegOppReason + sG_DbNegCapCode;
                gate.isoField[120] += sG_CurDate.substring(2, 8) + sG_NegDelDate.substring(2, 6);
                //up, process field 120
            }
        }
        else {/*Outgoing VISA or Master or JCB*/
            if ("J".equals(sL_CardBinType)) { //JCB Exception File
                gate.isoField[101]= "6332";
                
                //down, process field 120
                gate.isoField[120] = "PATH" + sL_CardNo.substring(0, 6);
                
                if (nP_Type==1) 
                    gate.isoField[120] += "301"; /*1 for Add 2 for update*/
                else
                    gate.isoField[120] += "170"; /* for delete */
                gate.isoField[120] += sL_CardNo;
                
                if (nP_Type==1) {
                    gate.isoField[120] += sG_DbJcbExcpCode;
                    gate.isoField[120] += sG_NegDelDate.substring(2, 6);
                    gate.isoField[120] += "00000";/*JCB region code*/
                }
                else {
                    gate.isoField[120] += "             "; /* 13 spaces */
                }
                //up, process field 120

            }
            else if ("M".equals(sL_CardBinType)) {
                gate.isoField[101]= "MC";
                //down, process field 120
//                if (nP_Type==1)
//                    sL_IsoField120 = "0092"; //Howard: 新的spec應該是 "0092", old proc is "0075"
//                else
//                    sL_IsoField120 = "0047";
                
                gate.isoField[120] += "PATHMCC" ;
                if (nP_Type==1) 
                    gate.isoField[120] += "1"; /*1 for Add 2 for update*/
                else
                    gate.isoField[120] += "3"; /* for delete */
                gate.isoField[120] += "MCC102         " + "00000";
                gate.isoField[120] += sL_CardNo + "   ";
                
                if (nP_Type==1) { /* add data need to send */
                    gate.isoField[120] += sG_DbMstAuthCode;
                    gate.isoField[120] += "000000000000";
                    gate.isoField[120] += "000000000000";
                    gate.isoField[120] += "   ";
                    gate.isoField[120] += "        ";//"PurgeDate(YYYYMMDD)";
                    gate.isoField[120] += "   ";// "CardSeqNo(3 bytes)";
                    gate.isoField[120] += sG_CardNewEndDate.substring(0,6);//expire date (YYYYMM)
                    
                }
                //up, process field 120
            }
            else { //for VISA card (VISA Exception File)
                gate.isoField[101]= "VP";
                //down, process field 120
                gate.isoField[120] = "PCAS";
                if (nP_Type==1) 
                    gate.isoField[120] += "BA"; /* for add or update */
                else
                    gate.isoField[120] += "BD"; /* for delete */
                
                
                gate.isoField[120] += HpeUtil.fillCharOnRight(sL_CardNo, 28, " ");
                gate.isoField[120] += "   "; /*3 Byte Country code*/
                gate.isoField[120] += "  ";/*2 Byte Operator ID*/
                
                if (sG_DbVisExcepCode.length()==0) {
                    gate.isoField[120] += "05";
                
                }
                else {
                    gate.isoField[120] += sG_DbVisExcepCode;
                }
                gate.isoField[120] += "0        "; /*9 Byte Region code*/
                //up, process field 120
            }
        }

        BicFormat bic = new BicFormat(null, gate, null);
        if (bic.host2Iso()==false) {
            return false;
        }

        
        boolean bL_Result=true;
        
        //down, insert into table outgoing
        String sql = "   INSERT INTO CCA_OUTGOING " 
                + "         (CARD_NO,KEY_VALUE,KEY_TABLE, "
                + "          BITMAP,PROC_FLAG,SEND_TIMES,CRT_DATE,CRT_TIME, "
                + "          CRT_USER,PROC_DATE,PROC_TIME,PROC_USER,ACT_CODE) "
                + "   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ";
        CcaOutgoingDao.insertOutgoing(sql, sL_CardNo, "NCCC", "OPPOSITION", gate.isoString.substring(2), "0", 0,
                HpeUtil.getCurDateStr(""), HpeUtil.getCurTimeStr(), "ECS004", HpeUtil.getCurDateStr(""),
                HpeUtil.getCurTimeStr(), "", "A");
        //up, insert into table outgoing
        
        return bL_Result;
        
    }
	
	public void testInsert() {
		connDb();
		CcaOutgoingVo L_OutgoingVo = new CcaOutgoingVo();
		L_OutgoingVo.setActCode("A"); //作業碼
		L_OutgoingVo.setCardNo("12345");
		L_OutgoingVo.setCrtUser("ECS004");
		L_OutgoingVo.setIsoField002("");
		L_OutgoingVo.setIsoField007("");
		L_OutgoingVo.setIsoField011("");
		L_OutgoingVo.setIsoField048("");
		L_OutgoingVo.setIsoField049("");
		L_OutgoingVo.setIsoField060("");
		L_OutgoingVo.setIsoField073("");
		L_OutgoingVo.setIsoField091("");
		L_OutgoingVo.setIsoField101("");
		L_OutgoingVo.setIsoField120("");
		
		L_OutgoingVo.setMsgHeader("ISO");
		L_OutgoingVo.setMsgType("0300");
		L_OutgoingVo.setModPgm("ECS004");
 
		
		
		L_OutgoingVo.setModTime(HpeUtil.getCurTimestamp());
		L_OutgoingVo.setProcDate("");
		L_OutgoingVo.setProcTime("");
		L_OutgoingVo.setProcFlag("N");
		L_OutgoingVo.setProcUser("ECS004");
		L_OutgoingVo.setSendTimes(0);
		
		CcaOutgoingDao.insertOutgoing(L_OutgoingVo);
		commitDb();
	}
	
	public void testUpdate(String sP_MsgHeader, String sP_MsgType, String sP_CardNo) {
		connDb();
		
		CcaOutgoingDao.updateOutgoing(sP_MsgHeader, sP_MsgType,sP_CardNo);
		commitDb();
	}

	public void testDelete(String sP_CardNo) {
		connDb();
		
		CcaOutgoingDao.deleteOutgoing(sP_CardNo);
		commitDb();
	}

	public void testSelect(String sP_CardNo) throws Exception{
		connDb();
		
		ResultSet L_RS =  CcaOutgoingDao.getOutgoing(sP_CardNo);
		
		while(L_RS.next()) {
			String sL_Tmp = L_RS.getString("MSG_HEADER");
			//System.out.println(sL_Tmp);
		}
		
		L_RS.close();
		commitDb();
		closeDb();
	}

	private void genRecFields(ResultSet P_OnBatRs) throws Exception {
		//proc is genRecFields()
		String sL_PirCardAcctClass=P_OnBatRs.getString("OnBatAccountType");
		String sL_PirCardAcctId=P_OnBatRs.getString("OnBatCardAcctId");
		String sL_PirCardCorpId=P_OnBatRs.getString("OnBatCardHldrId");
		

		String sL_DbCcasAcct = "A";
		ResultSet L_SysParm2Rs = CcaSysParm2Dao.getCcaSysParm2("ECSACCT",sL_PirCardAcctClass);
		
		while (L_SysParm2Rs.next()) {
			sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
		}
		/*
		if (!CcaSysParm2Dao.isEmptyResultSet(L_SysParm2Rs)) {
			sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
		}
		*/
		L_SysParm2Rs.close();
		
		//String sG_CaiCardAcctClass="", sG_CaiCardAcctId="", sG_CaiCardCorpId="";
		if ("A".equals(sL_DbCcasAcct)) {//一般卡
			sG_CaiCardAcctClass = sL_DbCcasAcct;
			
			if ("05".equals(sL_PirCardAcctClass.substring(0,2))) 
				sG_CaiCardAcctClass = sG_CaiCardAcctClass + "2";
			else
				sG_CaiCardAcctClass = sG_CaiCardAcctClass + "1";
			
			sL_PirCardCorpId = sL_PirCardAcctId;
		}
		else {/*商務卡 or 採購卡*/
			if ((sL_PirCardAcctId.substring(0,8).equals(sL_PirCardCorpId.substring(0,8))) || (sL_PirCardCorpId.trim().length()==0)) {
				sG_CaiCardAcctClass = sL_DbCcasAcct;
				sL_PirCardCorpId = sL_PirCardAcctId.substring(0,8); 
			}
			else {
				sG_CaiCardAcctClass = sL_DbCcasAcct;
				if ("000".equals(sL_PirCardAcctId.substring(8, 11)))
					sG_CaiCardAcctClass = sG_CaiCardAcctClass + "1";
				else
					sG_CaiCardAcctClass = sG_CaiCardAcctClass + "2";
			}
		}
		
		sG_CaiEcsAcctClass = sL_PirCardAcctClass;
		sG_CaiCardAcctId = sL_PirCardAcctId;
		sG_CaiCardAcctIdSeq=" ";
		sG_CaiCardCorpId = sL_PirCardCorpId;
		sG_CaiCardCorpIdSeq = " ";
	}
	
    private void genRecFields(Data004Vo P_Data004Vo) throws Exception {
        // proc is genRecFields()
        String sL_PirCardAcctClass = P_Data004Vo.getAccountType();
        String sL_PirCardAcctId = P_Data004Vo.getCardAcctId();
        String sL_PirCardCorpId = P_Data004Vo.getCardHldrId();

        String sL_DbCcasAcct = "A";
        ResultSet L_SysParm2Rs = CcaSysParm2Dao.getCcaSysParm2("ECSACCT", sL_PirCardAcctClass);

        while (L_SysParm2Rs.next()) {
            sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
        }
        /*
         * if (!CcaSysParm2Dao.isEmptyResultSet(L_SysParm2Rs)) { sL_DbCcasAcct =
         * L_SysParm2Rs.getString("SysData1"); }
         */
        L_SysParm2Rs.close();

        // String sG_CaiCardAcctClass="", sG_CaiCardAcctId="", sG_CaiCardCorpId="";
        if ("A".equals(sL_DbCcasAcct)) {// 一般卡
            sG_CaiCardAcctClass = sL_DbCcasAcct;

            if ("05".equals(sL_PirCardAcctClass.substring(0, 2)))
                sG_CaiCardAcctClass = sG_CaiCardAcctClass + "2";
            else
                sG_CaiCardAcctClass = sG_CaiCardAcctClass + "1";

            sL_PirCardCorpId = sL_PirCardAcctId;
        } else {/* 商務卡 or 採購卡 */
            if ((sL_PirCardAcctId.substring(0, 8).equals(sL_PirCardCorpId.substring(0, 8)))
                    || (sL_PirCardCorpId.trim().length() == 0)) {
                sG_CaiCardAcctClass = sL_DbCcasAcct;
                sL_PirCardCorpId = sL_PirCardAcctId.substring(0, 8);
            } else {
                sG_CaiCardAcctClass = sL_DbCcasAcct;
                if ("000".equals(sL_PirCardAcctId.substring(8, 11)))
                    sG_CaiCardAcctClass = sG_CaiCardAcctClass + "1";
                else
                    sG_CaiCardAcctClass = sG_CaiCardAcctClass + "2";
            }
        }

        sG_CaiEcsAcctClass = sL_PirCardAcctClass;
        sG_CaiCardAcctId = sL_PirCardAcctId;
        sG_CaiCardAcctIdSeq = " ";
        sG_CaiCardCorpId = sL_PirCardCorpId;
        sG_CaiCardCorpIdSeq = " ";
    }
	   
	private static void getSysParm3() {
		setSleepTime(3600);
		setStopRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("ECS004","SLEEP", true, "N")) {
			setStopRun(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2));
			setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1));
			
		}

		
	}
	
    public void initProg(Connection P_Db2Conn) throws Exception {
        setDbConn(P_Db2Conn);
//        initPrepareStatement(G_ECS080ID);
//         getAuthParm();

    }

	public AuthBatch004() throws Exception{
		// TODO Auto-generated constructor stub
	}

}
