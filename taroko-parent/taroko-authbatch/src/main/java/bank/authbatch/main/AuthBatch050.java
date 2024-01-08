package bank.authbatch.main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.Timestamp;

//import org.omg.CORBA.ObjectHolder;
//import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;

import bank.authbatch.dao.CardAcctDao;
import bank.authbatch.dao.CardAcctIndexDao;
import bank.authbatch.dao.CcaConsumeDao;
import bank.authbatch.dao.CcaCardBaseDao;
import bank.authbatch.dao.CcaOutgoingDao;
import bank.authbatch.dao.CcaSysParm2Dao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.dao.CcaAccountDao;
import bank.authbatch.dao.CrdCardDao;
import bank.authbatch.dao.CreditLogDao;
import bank.authbatch.dao.DbcCardDao;
import bank.authbatch.dao.LimitAdjLogDao;
import bank.authbatch.dao.OnBatDao;
import bank.authbatch.dao.OppTypeReasonDao;
import bank.authbatch.vo.CardAcctVo;
import bank.authbatch.vo.CcaCardBaseVo;
import bank.authbatch.vo.CcaSysParm3Vo;
import bank.authbatch.vo.Data050VoOf17;

public class AuthBatch050 extends BatchProgBase{
	int nG_CounterStep=0, nG_CounterStepErr=0;
	int nG_CounterOk=0, nG_CounterErr=0;
	int nG_ECS100=1, nG_Continue=0;
	String sG_DbIcbcRespDesc="";
	int nG_CaiCardAcctIdx=0;
	RowId G_OnBatRowId;
	String sG_OnBatCardNo="" ;
	String sG_OnBatOppType= "", sG_OnBatOppReason= "", sG_OnBatOppDate="",sG_OnBatContract="";
	String sG_OnBatCardAcctId="",sG_OnBatCardHldrId="",sG_OnBatAccountType="";
	String sG_OnBatPaymentType="",sG_OnBatCardCatalog="",sG_OnBatCreditLimit="",sG_OnBatCreditLimitCash="", sG_OnbatTransAmt="";
	String sG_CaiCardAcctClass="", sG_CaiEcsAcctClass="", sG_CaiCardAcctId="";
	String sG_CaiCardAcctIdSeq="", sG_CaiCardCorpId="", sG_CaiCardCorpIdSeq;
	int nG_CaiAcctIdx=0, nG_CaiAcctParentIndex=0;
	String sG_DbNccOppType="", sG_DbNegOppReason="";
	String sG_DbVisExcepCode="",sG_DbMstAuthCode="",sG_DbJcbExcpCode="";
	
	String sG_OnBatActivePin="",sG_OnBatVoicePin="";
	int nG_OnBatTransAmt=0;
	@Override
	public void startProcess(String[] sP_Parameters) {
		//sP_Parameters => "050 xxx" 
		// TODO Auto-generated method stub
		
		try {

			setStopRun(1);
			setSleepTime(10);


			nG_ECS100=1;
			
			if (sP_Parameters.length>=2) {
				nG_ECS100=0;
				nG_CaiCardAcctIdx= Integer.parseInt(sP_Parameters[1]);
			}
			connDb();
			
			
			while (1==getStopRun()) {
				
				getSysParm3();
			
				if (0==getStopRun()) 
					break;//Stop By PARM3-Flag Off

				nG_CounterOk=0;
				nG_CounterErr=0;

				if (nG_ECS100==1) {
					getSysParm3();
				}
				
				if (nG_ECS100==1) {
					/*上次錯的再執行一次*/
					OnBatDao.updateOnBatProcStatus(2,  0, "");
				}
				
				//17 in ONBAT 預現指撥撥回(批次)*/
				processOnBat17(); //Howard: 改由外部call

				
				//down, Process 19.預現指撥撥回
				processOnBat19();
				
				//up, Process 19.預現指撥撥回
				
				//down, /*10 = Read 10 in ONBAT 卡片語音密碼============================================*/
				processOnBat10();
				//up, /*10 = Read 10 in ONBAT 卡片語音密碼============================================*/				
				
				
				//down, /*12 = Read 12 in ONBAT 卡戶永久額度========================================*/
//				processOnBat12();
				//up, /*12 = Read 12 in ONBAT 卡戶永久額度========================================*/
				
				
				//down, /*13 = Read 13 in ONBAT 卡片子卡額度============================================*/
				processOnBat13();
				//up, /*13 = Read 13 in ONBAT 卡片子卡額度============================================*/
				
				
				//down,  /*16 = Read 16 in ONBAT 卡戶Payment未消金額========================================*/
//				processOnBat16();
				//up,  /*16 = Read 16 in ONBAT 卡戶Payment未消金額========================================*/
				
				/* down, 6 = Read 6 in ECS_ONBAT 普升金卡片停用=====================================*/
				processOnBat6();
				/* up, 6 = Read 6 in ECS_ONBAT 普升金卡片停用=====================================*/
				
				
			}
			
//			closeDb();			
		} catch (Exception e) {
			// TODO: handle exception
        } finally {
            closeDb();
        }
	}
	private boolean processOnBat17() throws Exception{
		
		boolean bL_Result = true;
		nG_CounterStep=0;
		nG_CounterStepErr=0;
		nG_Continue=1;
		if (nG_ECS100==1) {
			//szRTN = TB_CCAS_ONBAT(170); /*Read 17 in ONBAT 預現指撥撥回 do-flag*/
			if (getOnBatFor170()) {
				nG_Continue=1;
			}
			else
				nG_Continue=0;
			
		}
		while(nG_Continue==1) {
			nG_Continue=0;
			sG_DbIcbcRespDesc=""; //proc is DB_ICBC_RESP_DESC
			if (getOnBatFor17()) {//Howard: 取回CardNo and TransAmt from OnBat 
				nG_Continue=1;
				if (processCcaConsume()) {
					int nL_ProcessStatus=1;
					if (OnBatDao.updateOnBat7(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId)) { //proc is TB_CCAS_ONBAT(22);   /*Update process flag on ONBAT*/
						commitDb();
						nG_CounterOk ++ ;
						nG_CounterStep++;
					}
					else {
						
						
						sG_DbIcbcRespDesc = "*OnBat更新失敗*";
						nL_ProcessStatus=2;
						errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
									                    
	                }
				}
				else {
					
					sG_DbIcbcRespDesc = "*無此卡號*";
					int nL_ProcessStatus=2;
					errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
								                    
				}
			}
			
		}
		/**** 無論是否有資料均須將ECS_ONBAT.PROCESS_STATUS='2'--表示已完成 ***/
		if (nG_ECS100 == 1){
			
			int nL_ProcessStatus=2, nL_ToWhich=2;
			String sL_TransType="17", sL_CardAcctId="0000000000";
			OnBatDao.updateOnBat6(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, sL_TransType, nL_ToWhich, sL_CardAcctId);//proc is  TB_CCAS_ONBAT(270);           /*Update do flag on ONBAT*/
			commitDb();
		}
		if (nG_ECS100 == 0)
			return true;
		
		return bL_Result;
	}
	private boolean processOnBat6() throws Exception{
		/* 6 = Read 6 in ECS_ONBAT 普升金卡片停用=====================================*/
		boolean bL_Result = true;
		nG_CounterStep=0;
		nG_CounterStepErr=0;
		nG_Continue=1;
		int nL_ProcessStatus=1;
		try {
			while (nG_Continue==1) {
				nG_Continue=0;
				ResultSet L_OnBatRs = getOnBatData6(); //TB_CCAS_ONBAT(6);
				while (L_OnBatRs.next()) {
				
					nG_Continue=1;
					if (getOppTypeInfo()) { //TB_OPPTYPE_CODE()
						if (writeToOutgoing(L_OnBatRs)) {
							if (OnBatDao.updateOnBat7(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId)) { //proc is TB_CCAS_ONBAT(22);   /*Update process flag on ONBAT*/
								commitDb();
								nG_CounterOk ++ ;
								nG_CounterStep++;
							}
							else {
								
								
								sG_DbIcbcRespDesc = "*OnBat更新失敗*";
								nL_ProcessStatus=2;
								errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
											                    
			                }

						}
						else {
							sG_DbIcbcRespDesc = "*無此卡號*";
							nL_ProcessStatus=2;
							errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
							
						}
					}
					
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	
	private ResultSet getCardInfoRs(String sP_CardNo, String sP_DebitFlag) {
		ResultSet L_CardInfoRs = null;
		if ("Y".equals(sP_DebitFlag)) {
			L_CardInfoRs =  DbcCardDao.getDbcCardByCardNo(sP_CardNo);
		}
		else {
			L_CardInfoRs =  CrdCardDao.getCrdCardByCardNo(sP_CardNo);
		}
		return L_CardInfoRs;
		
	}
	private boolean writeToOutgoing(ResultSet P_OnBatRs) throws Exception{
		//proc is TB_OUTGOING()
		boolean bL_Result = true;
		String sL_OtActCode="A";
		String sL_CardNo = P_OnBatRs.getString("OnBatCardNo");
		String sL_DebitFlag = P_OnBatRs.getString("OnBatDebitFlag");
		ResultSet L_CardInfoRs = null;
		try {
			if ( ("37".equals(sL_CardNo.substring(0, 2))) || ("4313".equals(sL_CardNo.substring(0, 4))) )
				return true; //不處理 AE card and ... 



			L_CardInfoRs = getCardInfoRs(sL_CardNo, sL_DebitFlag);
			if (null == L_CardInfoRs)
				return false;
			
			String sL_DbNegCapCode="";			
			String sL_TmpExcpCode = sG_DbNegOppReason;
			if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.equals(" "))) {
				
				bL_Result = CcaOutgoingDao.insertOutGoing(1, 1, P_OnBatRs, L_CardInfoRs, sG_DbNegOppReason, sL_DbNegCapCode,sG_DbJcbExcpCode,sG_DbMstAuthCode,sG_DbVisExcepCode,sL_OtActCode,"ECS050");
				
				if (!bL_Result)
					return false;
			}
			String sL_CardCardType = L_CardInfoRs.getString("CardCardType");
			if ("N".equals(sL_CardCardType.substring(0, 1))) /*NCCC-U卡*/
				return true;

			String sL_CardBinType = L_CardInfoRs.getString("CardBinType");

			sL_TmpExcpCode="";
			if ("J".equals(sL_CardBinType))
				sL_TmpExcpCode = sG_DbJcbExcpCode;//DB_JCB_EXCP_CODE
			else if ("M".equals(sL_CardBinType))
				sL_TmpExcpCode = sG_DbMstAuthCode;//DB_MST_AUTH_CODE
			else if ("V".equals(sL_CardBinType))
				sL_TmpExcpCode = sG_DbVisExcepCode; //DB_VIS_EXCEP_CODE
			if ((sL_TmpExcpCode.length()>0) && (!sL_TmpExcpCode.substring(0, 1).equals(" "))) {
				bL_Result = CcaOutgoingDao.insertOutGoing(2, 1, P_OnBatRs, L_CardInfoRs, sG_DbNegOppReason, sL_DbNegCapCode,sG_DbJcbExcpCode,sG_DbMstAuthCode,sG_DbVisExcepCode,sL_OtActCode,"ECS050");

				if (!bL_Result)
					return false;
			}

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	private boolean getOppTypeInfo() {
		//proc is TB_OPPTYPE_CODE()
		boolean bL_Result = true;
		try {
			String sL_OppType = sG_OnBatOppType;
			String sL_OppStatus = sG_OnBatOppReason; 
			
			ResultSet L_OppTypeReasonRs =  OppTypeReasonDao.getOppTypeReason(sL_OppType, sL_OppStatus,"1", "11");
			
			while (L_OppTypeReasonRs.next()) {
			//if (!OppTypeReasonDao.isEmptyResultSet(L_OppTypeReasonRs)) {
				sG_DbNccOppType = L_OppTypeReasonRs.getString("NccOppType");
				sG_DbNegOppReason = L_OppTypeReasonRs.getString("NegOppReason");
				
				sG_DbVisExcepCode = L_OppTypeReasonRs.getString("VisExcepCode");
				sG_DbMstAuthCode = L_OppTypeReasonRs.getString("MstAuthCode");
				sG_DbJcbExcpCode = L_OppTypeReasonRs.getString("JcbExcpCode");
				break;
			}
			L_OppTypeReasonRs.close();
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	private boolean processOnBat10() throws Exception{
		/*10 = Read 10 in ONBAT 卡片語音密碼============================================*/
		boolean bL_Result = true;
		nG_CounterStep=0;
		nG_CounterStepErr=0;
		nG_Continue=1;

		
		try {
			int nL_ProcessStatus=0;
			while (nG_Continue==1) {
				nG_Continue=0;
				if (getOnBatData10()) {
					nG_Continue=1;
					
					if (processCardBase10()) { //proc is TB_CARD_BASE(10)
						nL_ProcessStatus=1;
						if (OnBatDao.updateOnBat7(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId)) { //proc is TB_CCAS_ONBAT(22);   /*Update process flag on ONBAT*/
							commitDb();
							nG_CounterOk ++ ;
							nG_CounterStep++;
						}
						else {
							
							
							sG_DbIcbcRespDesc = "*OnBat更新失敗*";
							nL_ProcessStatus=2;
							errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
										                    
		                }	
					}
					else {
						sG_DbIcbcRespDesc = "*無此卡號*";
						nL_ProcessStatus=2;
						errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
						
					}
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
	
	private boolean chkCAIindex(int nP_Type, ResultSet P_OnBatRs) {
		///*讀取 CARD_ACCT_INDEX*/
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

			while(L_CardAcctIndexRs.next()) {
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
				if ("000".equals(sL_PirCardAcctId.substring(9, 12)))
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
//	private boolean processOnBat16() throws Exception{
//		/*16 = Read 16 in ONBAT 卡戶Payment未消金額========================================*/
//		boolean bL_Result = true;
//		nG_CounterStep=0;
//		nG_CounterStepErr=0;
//		ResultSet L_OnBatRs = null;
//		
//		
//		try {
//			int nL_ProcStatus=1, nL_ToWhich=2, nL_ProcessStatus=0;
//			String sL_TransType = "16";
//			String sL_CardAcctId = "0000000000";
//			int nL_OnBarRecCount = OnBatDao.getOnBatRecCount(nL_ProcStatus, sL_TransType, nL_ToWhich, sL_CardAcctId); //TB_CCAS_ONBAT(160) /*Read 16 in ONBAT 預現指撥撥回 do-flag*/
//			if (nL_OnBarRecCount>0)
//				nG_Continue=1;
//			else
//				nG_Continue=0;
//			while (nG_Continue==1) {
//				nG_Continue=0;
//				L_OnBatRs = getOnBatData("16"); //TB_CCAS_ONBAT(16); /*Read 16 in ONBAT 卡戶Payment未消金額*/
//				while(L_OnBatRs.next()) {
//					G_OnBatRowId = L_OnBatRs.getRowId("OnBatRowID");
//					sG_OnBatCardNo = L_OnBatRs.getString("OnBatCardNo");
//					sG_OnBatCardAcctId = L_OnBatRs.getString("OnBatCardAcctId");
//					sG_OnBatCardHldrId = L_OnBatRs.getString("OnBatCardHldrId");
//					sG_OnBatAccountType = L_OnBatRs.getString("OnBatAccountType");
//					sG_OnBatPaymentType = L_OnBatRs.getString("OnBatPaymentType");
//					
//					sG_OnBatCardCatalog = L_OnBatRs.getString("OnBatCardCatalog");
//					sG_OnBatCreditLimit = L_OnBatRs.getString("OnBatCreditLimit");
//					sG_OnBatCreditLimitCash = L_OnBatRs.getString("OnBatCreditLimitCash");
//					sG_OnbatTransAmt = L_OnBatRs.getString("OnBatTransAmt");
//				}
//				
//				nG_Continue=1;
//				genRecFields(L_OnBatRs);///*設定卡戶索引鍵值*/
//				if (chkCAIindex(1, L_OnBatRs)) {/*讀取 CARD_ACCT_INDEX*/
//					if (processCardAcct16()) {
//						nL_ProcessStatus=1;
//						if (OnBatDao.updateOnBat7(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId)) { //proc is TB_CCAS_ONBAT(22);   /*Update process flag on ONBAT*/
//							commitDb();
//							nG_CounterOk ++ ;
//							nG_CounterStep++;
//						}
//						else {
//							
//							
//							sG_DbIcbcRespDesc = "*OnBat更新失敗*";
//							nL_ProcessStatus=2;
//							errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
//										                    
//		                }
//
//					}
//					else {
//						sG_DbIcbcRespDesc = "*卡戶更新失敗*";
//						nL_ProcessStatus = 2;
//						errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
//					}
//				}
//				else {
//					sG_DbIcbcRespDesc = "*無此卡戶*";
//					nL_ProcessStatus = 2;
//					errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
//				}
//				L_OnBatRs.close();
//				
//			}
//			/**** down, 無論是否有資料均須將ECS_ONBAT.PROCESS_STATUS='2'--表示已完成 ***/
//			//proc is TB_CCAS_ONBAT(260)
//			nL_ProcessStatus=2;
//			nL_ToWhich=2;  
//			String sL_ProcDate = sG_CurDate;
//			sL_TransType="16";
//			Timestamp L_Dop = HpeUtil.getCurTimestamp();
//			sL_CardAcctId = "0000000000";
//			OnBatDao.updateOnBat6(nL_ProcessStatus, L_Dop, sL_ProcDate, sL_TransType, nL_ToWhich, sL_CardAcctId);
//			
//			commitDb();
//			/**** up, 無論是否有資料均須將ECS_ONBAT.PROCESS_STATUS='2'--表示已完成 ***/
//		} catch (Exception e) {
//			// TODO: handle exception
//			bL_Result = false;
//		}
//		
//		return bL_Result;
//	}
	
//	private boolean processCardAcct16() {
//		//proc is TB_CARD_ACCT(16)
//		boolean bL_Result = true;
//		CardAcctVo L_CardAcctVo=null;
//		try {
//			L_CardAcctVo = CardAcctDao.getCardAcct(nG_CaiAcctIdx); //	szRTN = TB_CARD_ACCT(16);      /*Update CardAcct Card Limit*/
//			
//			if (null != L_CardAcctVo) {
//				
//				if (CcaConsumeDao.updateTotUnpaidAmt(nG_CaiAcctIdx, sG_OnbatTransAmt)) {
//					/*** 刪除未銷帳臨調 ***/
//					String sL_AdjQuota = L_CardAcctVo.getAdjQuota();
//					String sL_AdjReason = L_CardAcctVo.getAdjReason();
//					String sL_AdjEffEndDate = L_CardAcctVo.getAdjEffEndDate();
//					String sL_AdjEffStartDate = L_CardAcctVo.getAdjEffStartDate();
//					int nL_TotalAmtMonth = Integer.parseInt(L_CardAcctVo.getTotAmtMonth());
//					int nL_AdjInstPct = Integer.parseInt(L_CardAcctVo.getAdjInstPct());
//					
//					
//					
//					if ( ("Y".equals(sL_AdjQuota.substring(0,1))) && (HpeUtil.compareDateString(sG_CurDate, sL_AdjEffStartDate)>0) &&
//							(HpeUtil.compareDateString(sG_CurDate, sL_AdjEffEndDate)<0) && ((nL_TotalAmtMonth>10) || (nL_AdjInstPct>100)) &&
//							("02".equals((sL_AdjReason.substring(0, 2)))) ) {
//						bL_Result = CardAcctDao.updateCardAcct16(nG_CaiAcctIdx, 100, 100) ;									
//						if (bL_Result) {
//							String sL_CallerProgName="ECS050";
//							/* Howard : 2018.07.12 LmtTotConsume 搬到ACT_ACNO中， 所以要改寫取值的寫法
//							int nL_LmtTotConsume= Integer.parseInt(L_CardAcctVo.getLmtTotConsume());
//							if (!LimitAdjLogDao.insertLimitAdjLog(L_CardAcctVo, sL_CallerProgName, sG_OnBatCardAcctId, nL_LmtTotConsume))
//								return false;
//							*/
//						}
//						/*
//						if (update_card_acct_16()!=0) return(FALSE); 
//        				if (insert_db_trans_log_16()!=0) return(FALSE); 
//        				if (insert_acct_limit_adj_log_16()!=0) return(FALSE);
//						* */
//					}
//					
//					
//				}
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			bL_Result = false;
//		}
//		
//		return bL_Result;
//	}
	private boolean processOnBat13() throws Exception{
		/*13 = Read 13 in ONBAT 卡片子卡額度============================================*/
		boolean bL_Result = true;
		nG_CounterStep=0;
		nG_CounterStepErr=0;
		nG_Continue=1;
		String sL_TransType="13";
		ResultSet L_OnBatRs = null;
		int nL_ProcessStatus=1;
		try {
			while (nG_Continue==1) {
				nG_Continue=0;
				L_OnBatRs =getOnBatData(sL_TransType);
				if (null != L_OnBatRs) {
					nG_Continue=1;
					if (updateChildCardLimit(L_OnBatRs)) { //TB_CARD_BASE(2)
						
						if (OnBatDao.updateOnBat7(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId)) { //proc is TB_CCAS_ONBAT(22);   /*Update process flag on ONBAT*/
							commitDb();
							nG_CounterOk ++ ;
							nG_CounterStep++;
						}
						else {
							
							
							sG_DbIcbcRespDesc = "*OnBat更新失敗*";
							nL_ProcessStatus=2;
							errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
										                    
		                }
					}
					else {
						sG_DbIcbcRespDesc = "*無此卡號*";
						nL_ProcessStatus = 2;
						errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
					}
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	private boolean updateChildCardLimit(ResultSet P_OnBatRs) {
		//proc is TB_CARD_BASE(2)
		//更新 子卡個人額度
		boolean bL_Result = true;
		
		try {
			String sL_OnBatCardNo = P_OnBatRs.getString("OnBatCardNo");
			int nL_OnBatCreditLimit = P_OnBatRs.getInt("OnBatCreditLimit");
			CcaCardBaseVo L_CardBaseVo = CcaCardBaseDao.getCcaCardBase(sL_OnBatCardNo);
			
			if (null != L_CardBaseVo) {
				if ("Y".equals(L_CardBaseVo.getDebitFlag()))
					bL_Result = DbcCardDao.updateCardLimit(sL_OnBatCardNo, nL_OnBatCreditLimit);
				else
					bL_Result = CrdCardDao.updateCardLimit(sL_OnBatCardNo, nL_OnBatCreditLimit);
			}
			else {
				bL_Result = false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
//	private boolean processOnBat12() throws Exception{
//		/*12 = Read 12 in ONBAT 卡戶永久額度========================================*/
//		boolean bL_Result = false;
//		nG_CounterStep=0;
//		nG_CounterStepErr=0;
//		nG_Continue=1;
//		String sL_TransType="12";
//		
//		try {
//			int nL_ProcessStatus=0, nL_RtnCode=0;
//			while (nG_Continue==1) {
//				nG_Continue=0;
//				sG_DbIcbcRespDesc="";
//				
//				ResultSet L_OnBatRs =getOnBatData(sL_TransType);
//				
//				while (L_OnBatRs.next()) {
//				//if (!OnBatDao.isEmptyResultSet(L_OnBatRs)) {
//					
//					nG_Continue=1;
//					genRecFields(L_OnBatRs);///*設定卡戶索引鍵值*/
//					if (chkCAIindex(1, L_OnBatRs)) {
//						nL_RtnCode=1;
//						//TB_CARD_ACCT(12)
//						CardAcctVo L_CardAcctVo = CardAcctDao.getCardAcct(nG_CaiAcctIdx);
//						CcaCardBaseVo L_CardBaseVo = CcaCardBaseDao.getCcaCardBase(sG_OnBatCardNo);
//						if ((null != L_CardAcctVo) && (null != L_CardBaseVo ) ){
//							//TB_CREDIT_LOG()
//							if (!processCreditLog(L_CardAcctVo, L_CardBaseVo))
//								nL_RtnCode=2;
//							
//						}
//						if ((null != L_CardAcctVo) && (null != L_CardBaseVo ) ){
//							if (OnBatDao.updateOnBat7(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId)) { //proc is TB_CCAS_ONBAT(22);   /*Update process flag on ONBAT*/
//								commitDb();
//								nG_CounterOk ++ ;
//								nG_CounterStep++;
//							}
//							else {
//								sG_DbIcbcRespDesc = "*OnBat更新失敗*";
//								nL_ProcessStatus=2;
//								errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
//							}
//						}
//						else {
//							if (nL_RtnCode==1) {
//								sG_DbIcbcRespDesc = "*卡戶更新失敗*";
//							}
//							else { 
//								sG_DbIcbcRespDesc = "*寫入CREDIT_LOG失敗*";
//							}
//							nL_ProcessStatus=2;
//							errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
//
//						}
//						
//						
//						
//						
//					}
//					else {//chkCAIindex is false
//						sG_DbIcbcRespDesc = "*無此卡戶*";
//						nL_ProcessStatus=2;
//						errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
//						
//					}
//					
//					
//					
//				}
//				L_OnBatRs.close();
//			}
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			bL_Result = false;
//		}
//		
//		return bL_Result;
//		
//	}

	private boolean processCreditLog(CardAcctVo L_CardAcctVo, CcaCardBaseVo P_CardBaseVo)  throws Exception{
		//proc is TB_CREDIT_LOG()
		boolean bL_Result = true;
		
		
		try {
			if ("N".equals(L_CardAcctVo.getAdjQuota())) { ///*** 無臨調不需寫入 ***/
				return true;
			}
			
			if ((HpeUtil.compareDateString(sG_CurDate, L_CardAcctVo.getAdjEffStartDate())<0) || 
					(HpeUtil.compareDateString(sG_CurDate, L_CardAcctVo.getAdjEffEndDate())>0) )
				return true;

			if  (((L_CardAcctVo.getTotAmtMonth()=="0") || (L_CardAcctVo.getTotAmtMonth()=="100") ) &&
				((L_CardAcctVo.getAdjInstPct()=="0") || (L_CardAcctVo.getAdjInstPct()=="100")) )
				return true;

			
			/* Howard: 2018.07.02 marked, 之後一併檢討 050 的程式要如何與來哥整併
			if ((Integer.parseInt(L_CardAcctVo.getLmtTotConsume())==nG_OnBatTransAmt) &&
					L_CardAcctVo.getLmtTotCash().equals(sG_OnBatCreditLimitCash) )
				return true;
			

			 bL_Result = CreditLogDao.insertCreditLog(L_CardAcctVo, nG_OnBatTransAmt, P_CardBaseVo, sG_OnBatCardAcctId);
			 */
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	private boolean processCardBase10() throws Exception{
		//proc is TB_CARD_BASE(10)
		boolean bL_Result = true;
		
		try {
			String sL_Sql = "";
			
			CcaCardBaseVo L_CardBaseVo = CcaCardBaseDao.getCcaCardBase(sG_OnBatCardNo);
			
			if (null == L_CardBaseVo) {
				return false;
			}
			bL_Result = CcaCardBaseDao.updateCcaCardBase(sG_OnBatCardNo, sG_OnBatVoicePin, sG_OnBatVoicePin);
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	private boolean processOnBat19() throws Exception{
		
		/*卡戶預現指撥撥回作業*/
		boolean bL_Result = true;
		nG_CounterStep=0;
		nG_CounterStepErr=0;
		nG_Continue=1;

		//down, Process 19.預現指撥撥回
		while (nG_Continue==1) {
			nG_Continue=0;
			if (getOnBatData()) {
				nG_Continue=1;
				if (processCcaConsume()) {
					int nL_ProcessStatus=1;
					if (OnBatDao.updateOnBat7(nL_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId)) {
						commitDb();
						nG_CounterOk++;
						nG_CounterStep++;
					}
					else {

						sG_DbIcbcRespDesc = "*OnBat更新失敗*";
						
						nL_ProcessStatus=2;
						errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
					}
				}
				else {
					
					sG_DbIcbcRespDesc = "*無此卡號*";
					int nL_ProcessStatus=2;
					errorProcess(nL_ProcessStatus, sG_DbIcbcRespDesc);
								                    
				}
			}
		}
		//up, Process 19.預現指撥撥回
		
		return bL_Result;
	}
	
	//private void updateOnBatData(int nP_ProcessStatus) {
		//OnBatDao.updateOnBat8(nP_ProcessStatus, G_CurTimestamp, sG_CurDate, sG_OnBatRowId, sG_DbIcbcRespDesc); //proc is TB_CCAS_ONBAT(99);   /*Update ONBAT error*/		
	//}
	
	private void errorProcess(int nP_ProcessStatus, String sP_DbIcbcRespDesc) {
		
		rollbackDb();
		OnBatDao.updateOnBat8(nP_ProcessStatus, G_CurTimestamp, sG_CurDate, G_OnBatRowId, sP_DbIcbcRespDesc); //proc is TB_CCAS_ONBAT(99);   /*Update ONBAT error*/					                    
        commitDb();

        nG_CounterErr++;
        nG_CounterStepErr++;

		
	}

	private boolean getOnBatData()  throws Exception{
		boolean bL_Result = false;
		String sL_TransType="", sL_OnBatTableName="", sL_SelectFields="", sL_ExtraWhereCond="";
		int nL_ToWhich=0, nL_ProcStatus=0;
		
		try {
			sL_TransType = "19";
			nL_ToWhich=2;
			nL_ProcStatus=0;
			sL_OnBatTableName ="ONBAT_2CCAS";
			sL_SelectFields = " ROWID as OnBatRowID,NVL(CARD_NO,' ') as OnBatCardNo, NVL(TRANS_AMT,0) as OnBatTransAmt ";
			sL_ExtraWhereCond = " FETCH FIRST 1 ROWS ONLY ";
			ResultSet L_ResultSet =  OnBatDao.getOnBat3(sL_TransType, nL_ToWhich, nL_ProcStatus, sL_OnBatTableName, sL_SelectFields, sL_ExtraWhereCond);
			while (L_ResultSet.next()) {
				sG_OnBatCardNo = L_ResultSet.getString("OnBatCardNo");
				nG_OnBatTransAmt = L_ResultSet.getInt("OnBatTransAmt");
				G_OnBatRowId = L_ResultSet.getRowId("OnBatRowID");
				bL_Result = true;
			}
			L_ResultSet.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	private boolean getOnBatData10()  throws Exception{
		//proc is TB_CCAS_ONBAT(10)
		/*語音密碼*/
		boolean bL_Result = false;
		String sL_TransType="", sL_OnBatTableName="", sL_SelectFields="", sL_ExtraWhereCond="";
		int nL_ToWhich=0, nL_ProcStatus=0;
		
		try {
			sL_TransType = "10";
			nL_ToWhich=2;
			nL_ProcStatus=0;
			sL_OnBatTableName ="ONBAT_2CCAS";
			sL_SelectFields = " ROWID as OnBatRowID ,NVL(CARD_NO,' ') as OnBatCardNo, NVL(CARD_VALID_TO,'00000000') as OnBatCardValidTo , NVL(VOICE_PIN,'      ') as OnBatVoicePin ,NVL(ACTIVE_PIN,'      ') as OnBatActivePin " ;
			sL_ExtraWhereCond = " FETCH FIRST 1 ROWS ONLY ";
			ResultSet L_ResultSet =  OnBatDao.getOnBat3(sL_TransType, nL_ToWhich, nL_ProcStatus, sL_OnBatTableName, sL_SelectFields, sL_ExtraWhereCond);
			while (L_ResultSet.next()) {
				sG_OnBatCardNo = L_ResultSet.getString("OnBatCardNo");
				G_OnBatRowId = L_ResultSet.getRowId("OnBatRowID");
				sG_OnBatVoicePin= L_ResultSet.getString("OnBatVoicePin");
				sG_OnBatActivePin= L_ResultSet.getString("OnBatActivePin");
				bL_Result = true;
				
			}
			L_ResultSet.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	private ResultSet getOnBatData6()  throws Exception{
		//proc is TB_CCAS_ONBAT(6)
		/*普升金卡片停用*/
		
		String sL_TransType="", sL_OnBatTableName="", sL_SelectFields="", sL_ExtraWhereCond="";
		int nL_ToWhich=0, nL_ProcStatus=0;
		ResultSet L_ResultSet = null;
		try {
			sL_TransType = "6";
			nL_ToWhich=2;
			nL_ProcStatus=0;
			sL_OnBatTableName ="ONBAT_2CCAS";
			sL_SelectFields = " ROWID as OnBatRowId, NVL(CARD_NO,' ')  as OnBatCardNo, NVL(OPP_TYPE,' ') as OnBatOppType, NVL(OPP_REASON,'  ') as OnBatOppReason, NVL(OPP_DATE,'00000000') as OnBatOppDate, CONTRACT as OnBatContract " ;
			sL_ExtraWhereCond = " FETCH FIRST 1 ROWS ONLY ";
			L_ResultSet =  OnBatDao.getOnBat3(sL_TransType, nL_ToWhich, nL_ProcStatus, sL_OnBatTableName, sL_SelectFields, sL_ExtraWhereCond);
			/*
			while (L_ResultSet.next()) {
				sG_OnBatCardNo = L_ResultSet.getString("OnBatCardNo");
				sG_OnBatRowId = L_ResultSet.getString("OnBatRowID");
				sG_OnBatOppType= L_ResultSet.getString("OnBatOppType");
				sG_OnBatOppReason= L_ResultSet.getString("OnBatOppReason");
				
				sG_OnBatOppDate= L_ResultSet.getString("OnBatOppDate");
				sG_OnBatContract= L_ResultSet.getString("OnBatContract");
				bL_Result = true;
				
			}
			L_ResultSet.close();
			*/
			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		
		return L_ResultSet;
	}

	private ResultSet getOnBatData(String sP_TransType)  throws Exception{
	//private ResultSet getOnBatData12(String sP_TransType)  throws Exception{
		//proc is TB_CCAS_ONBAT(12) and (13) and (16)
		/*Read 12 in ONBAT 卡戶永久額度*/
		ResultSet L_OnBatRs=null;

		boolean bL_Result = false;
		String sL_OnBatTableName="", sL_SelectFields="", sL_ExtraWhereCond="";
		int nL_ToWhich=0, nL_ProcStatus=0;
		
		try {
			
			nL_ToWhich=2;
			nL_ProcStatus=0;
			sL_OnBatTableName =" ONBAT_2CCAS ";
			sL_SelectFields = " ROWID as OnBatRowID ,NVL(CARD_NO,' ') as OnBatCardNo,  NVL(CARD_ACCT_ID,'           ') as OnBatCardAcctId , " 
								+ "NVL(CARD_HLDR_ID,'           ') as OnBatCardHldrId, NVL(ACCOUNT_TYPE,'01') as OnBatAccountType, "
								+ "NVL(PAYMENT_TYPE,'1') as OnBatPaymentType,NVL(CARD_CATALOG,'1') as OnBatCardCatalog,"
								+ "NVL(CREDIT_LIMIT,0) as OnBatCreditLimit,NVL(CREDIT_LIMIT_CASH,0) as OnBatCreditLimitCash, NVL(TRANS_AMT,0) as OnBatTransAmt " ;
			sL_ExtraWhereCond = " FETCH FIRST 1 ROWS ONLY ";
			L_OnBatRs =  OnBatDao.getOnBat3(sP_TransType, nL_ToWhich, nL_ProcStatus, sL_OnBatTableName, sL_SelectFields, sL_ExtraWhereCond);
			/*
			while (L_ResultSet.next()) {
				sG_OnBatCardNo = L_ResultSet.getString("OnBatCardNo");
				sG_OnBatRowId = L_ResultSet.getString("OnBatRowID");
				sG_OnBatCardAcctId= L_ResultSet.getString("OnBatCardAcctId");
				sG_OnBatCardHldrId= L_ResultSet.getString("OnBatCardHldrId");
				 
				sG_OnBatAccountType= L_ResultSet.getString("OnBatAccountType");
				sG_OnBatPaymentType= L_ResultSet.getString("OnBatPaymentType");
				sG_OnBatCardCatalog= L_ResultSet.getString("OnBatCardCatalog");
				sG_OnBatCreditLimit= L_ResultSet.getString("OnBatCreditLimit");
				sG_OnBatCreditLimitCash= L_ResultSet.getString("OnBatCreditLimitCash");
				
				

				bL_Result = true;
				
			}
			
			L_ResultSet.close();
			*/
			
		} catch (Exception e) {
			// TODO: handle exception
			L_OnBatRs=null;
		}
		
		return L_OnBatRs;
	}

	private boolean processCcaConsume() {
		//proc is => TB_AUTH_CONSUME(17);     /*Update Auth_Consume ibm_receive_amt*/		
		boolean bL_Result = true;
		
		try {

			int nL_CardAcctIdx = CcaCardBaseDao.getCardAcctIdx(sG_OnBatCardNo);
			
			if (nL_CardAcctIdx==0)
				return false;
			else {
				if (CcaConsumeDao.updateCcaConsume(nL_CardAcctIdx, nG_OnBatTransAmt))
					bL_Result = true;
				else
					bL_Result = false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}
	private static void getSysParm3() {
		setSleepTime(1000);
		setStopRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("ECSONBAT","SLEEP", true, "N")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1) );
				setStopRun(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2) );
			}
			
		}

		
	}


	private boolean getOnBatFor170() throws Exception {
		/*預現指撥撥回(批次) do flag*/
		boolean bL_Result = false;
		ResultSet L_OnBatTmpRs = OnBatDao.getOnBat3("17", 2, 1, "ONBAT_2CCAS", " COUNT(1) as RecCount ", " CARD_ACCT_ID='0000000000' ");
		while(L_OnBatTmpRs.next()) {
			if (L_OnBatTmpRs.getInt("RecCount")>0) {
				bL_Result=true;
			}
		}
		L_OnBatTmpRs.close();
		
		return bL_Result;
	}
	
	public void initProg(Connection P_Db2Conn) throws Exception{
		setDbConn(P_Db2Conn);
		initialPrepareStatement(G_ECS050IDFor17);

		
	}

	public int startProcessOf17(Data050VoOf17  P_Data050VoOf17 ) {
		int nL_Result = 0;
		// 0=> 正常 處理完畢
		// -1 => 無此卡號，或者CcaCardConsume更新失敗 
		sG_OnBatCardNo = P_Data050VoOf17.getCardNo();
		nG_OnBatTransAmt = P_Data050VoOf17.getTransAmt();
		
		if (processCcaConsume()) {
			nL_Result = 0; 
		}
		else {
			
			sG_DbIcbcRespDesc = "*無此卡號，或者CcaCardConsume更新失敗*";
			nL_Result = -1;			                    
		}
		return nL_Result;
	}
	
	private boolean getOnBatFor17() throws Exception {
		/*卡戶預現指撥撥回作業(批次)*/
		boolean bL_Result = false;
		CcaAccountDao L_CcsAccountDao = new CcaAccountDao();
		bL_Result = L_CcsAccountDao.ifCcaAccountHasRecord("0000000000","1");
		if (bL_Result) {
			/*** 若存在則不可執行此程式 ***/
			return false;
		}
		
		String sL_TransType="17"; 
		int nL_ToWhich=2, nL_ProcessStatus=0;
		
		ResultSet L_ResultSet = OnBatDao.getOnBat(sL_TransType, nL_ToWhich, nG_CaiCardAcctIdx, nG_ECS100, nL_ProcessStatus);
		while (L_ResultSet.next()) {
			G_OnBatRowId= L_ResultSet.getRowId("OnBatRowId");
			sG_OnBatCardNo= L_ResultSet.getString("OnBatCardNo");
			nG_OnBatTransAmt= L_ResultSet.getInt("OnBatTransAmt");
			

			
			bL_Result = true;
		}
		return bL_Result;
	}

	public AuthBatch050() throws Exception{
		// TODO Auto-generated constructor stub
	}

	
}
