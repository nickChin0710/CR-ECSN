package bank.authbatch.main;

import java.sql.Connection;
import java.sql.ResultSet;



import bank.authbatch.dao.ActAcnoDao;
import bank.authbatch.dao.CardAcctDao;
import bank.authbatch.dao.CcaConsumeDao;
import bank.authbatch.dao.CcaSysParm2Dao;
import bank.authbatch.dao.CcaSysParm3Dao;
import bank.authbatch.dao.CcaAccountDao;
import bank.authbatch.dao.CrdIdnoDao;
import bank.authbatch.dao.CreditLogDao;
import bank.authbatch.dao.SystemDao;
import bank.authbatch.vo.CardAcctIndexVo;
import bank.authbatch.vo.CardAcctVo;

import bank.authbatch.vo.CcaSysParm3Vo;

import bank.authbatch.vo.CrdIdnoVo;

import bank.authbatch.vo.Data100Vo;

public class AuthBatch100 extends BatchProgBase {

	public AuthBatch100() throws Exception{
		// TODO Auto-generated constructor stub
	}

	String sG_LmtFlag="", sG_NewCardAcctIdx="";
	String sG_CardAcctClass="", sG_CardHolderId="", sG_TmpCardHolderId="", sG_CardHolderSeq="", sG_CardAcctId="";
	String sG_CardAcctSeq="", sG_TmpCardAcctId="", sG_CardCorpId="", sG_CardCorpSeq="";
	int nG_MesgCode=0, nG_ErrorCount=0, nG_AcceptCount=0;

	public int startProcess(Data100Vo  P_Data100Vo ) {
		/*
		return 0 => 正常處理完成
		return > 0 => 程式正常處理完成，但有資料面的問題
		return -1 => 有 error (exception)
		*/
		int nL_ReturnCode=0;
		boolean bL_Result = true;
		try {
			bL_Result = processData100Vo(P_Data100Vo);
			
			System.out.println("nG_MesgCode=>" +nG_MesgCode + "===");
			nL_ReturnCode = nG_MesgCode;
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			nL_ReturnCode = -1;
			System.out.println("Exception on startProcess()...");
			
			e.printStackTrace(System.out);
		}
		
		return nL_ReturnCode;
	}

	private boolean ifCcsAccountHasData() throws Exception{
		//proc is get_ctrl()
		
		boolean bL_Result = false;
		CcaAccountDao L_CcsAccountDao = new CcaAccountDao();
		bL_Result = L_CcsAccountDao.ifCcaAccountHasRecord("0000000000","1");
			
		
		
		return bL_Result;
		
	}
	private static void getSysParm3() {
		setSleepTime(10);
		setStopRun(1);
		setPauseRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("ECSBILL","SLEEP", false,"0")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				if ("1".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData4)) {  
					
					writeLog("E", "AuthBatch_060 is running! Could not execute AuthBatch_100!");
					setPauseRun(0);
					setSleepTime(1800);
					return;
				}
			}
			
		}

		setSleepTime(10);
		setStopRun(1);

		if (CcaSysParm3Dao.getCcaSysParm3("ECSCAI","SLEEP", false, "N")) {
			if (CcaSysParm3Vo.ccaSysParm3List.size()>0) {
				if ((!"0".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1)) && (!"".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1.trim())) )
					setSleepTime(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData1) );
				
				if ((!"0".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2)) && (!"0".equals(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2.trim())) )
					setStopRun(Integer.parseInt(CcaSysParm3Vo.ccaSysParm3List.get(0).SysData2) );
				
				
				//szSleep   = atoi(rTrim(DB_SYSDATA1.arr));
				//szStopRun = atoi(rTrim(DB_SYSDATA2.arr));;

			}
		
    
		}
		
	}

//	private int decomposeIdSeq(ResultSet P_CcsAccountRs) throws Exception{
//		//proc is DECOMPOSE_ID_SEQ()
//		
//		int nL_Result = 0;
//		String sL_DbCcasAcct="";
//		
//		String sL_DbAccountType = P_CcsAccountRs.getString("ACCOUNT_TYPE");
//		String sL_DbCardAcctId = P_CcsAccountRs.getString("CARD_ACCT_ID");
//		String sL_DbCardHolderId = P_CcsAccountRs.getString("CARD_HLDR_ID");
//		String sL_DbPaymentRule = P_CcsAccountRs.getString("PAYMENT_RULE");
//		
//		sL_DbCcasAcct = "A";
//		ResultSet L_SysParm2Rs = CcaSysParm2Dao.getCcaSysParm2("ECSACCT",sL_DbAccountType);
//		
//		while (L_SysParm2Rs.next()) {
//			sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
//		}
//		/*
//		if (!CcaSysParm2Dao.isEmptyResultSet(L_SysParm2Rs)) {
//			sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
//		}
//		*/
//		L_SysParm2Rs.close();
//		/* Howard: 這個 else 不會發生
//		else {
//			nG_MesgCode=3;
//			return -1;
//		}
//		*/
//		
//		
//		
//		int nL_Err=0, nL_PayRule=0;
//		
//		
//		String sL_Tmp="", sL_Tmp1="";
//		sG_CardAcctClass =sL_DbCcasAcct;
//		if ("A".equals(sL_DbCcasAcct)) { //一般卡
//		
//			
//			/*-調整card_acct_index鍵值-------------*/
//			sL_Tmp = sL_DbCardAcctId; //sL_DbCardAcctId 就是 proc.h_a_card_acct_id_seq
//			
//			sL_Tmp1 = sL_DbCardHolderId.substring(0,10); //sL_DbCardHolderId就是 proc.h_a_card_hldr_id_seq
//			
//			
//			if (!sL_Tmp.equals(sL_Tmp1)) {
//				nG_MesgCode=2;
//				return -1;
//			}
//			
//			
//			
//			//sG_CardHolderId = sL_Tmp1;
//			//sG_TmpCardHolderId = sL_Tmp1;
//			
//			sL_Tmp1 = sL_DbCardAcctId.substring(10,11);
//			
//			if ((" ".equals(sL_Tmp1)) || ("0".equals(sL_Tmp1)) ) 
//				sG_CardAcctSeq="0";
//			else
//				sG_CardAcctSeq =sL_Tmp1; 
//
//			
//			sL_Tmp = sL_DbCardAcctId.substring(0,10);
//			
//			
//			if (sG_CardAcctId.charAt(0)>='A') {
//				if ((" ".equals(sL_Tmp1)) || ("0".equals(sL_Tmp1)) )
//					sL_Tmp = sL_Tmp.substring(0,10);
//				else
//					sL_Tmp = sL_Tmp + sL_Tmp1;  
//					
//			}
//			sG_CardAcctId = sL_Tmp;
//			
//			
//			
//			
//			
//			
//			sL_Tmp1 = sL_DbCardHolderId.substring(10,11);
//			if ((" ".equals(sL_Tmp1)) || ("0".equals(sL_Tmp1))) {
//				sG_CardHolderSeq="";
//				
//			}
//			else{
//				sG_CardHolderSeq = sL_Tmp1;
//			}
//				
//			sG_CardHolderId = sL_DbCardHolderId;
//			
//			
//			
//			
//		}
//		else { //商務卡,   採購卡
//			
//			sL_Tmp = sL_DbCardAcctId.substring(0,8);
//			sG_CardAcctId = sL_DbCardAcctId;
//			sG_CardHolderId=sL_DbCardHolderId;
//			if ((sL_Tmp.equals(sG_CardHolderId)) || ("".equals(sG_CardHolderId))) {
//				if (sL_Tmp.equals(sG_CardHolderId)) {
//					/*-調整card_acct_index鍵值-----  */
//		            /* card_acct_id=cid(8)+"000" card_hld_id=cid(8)*/
//					if (sG_CardAcctId.length()!=11)
//						nL_Err=1;
//					
//					sL_Tmp = sG_CardAcctId.substring(8, 11);
//					if ("000".equals(sL_Tmp)) {
//							nL_Err=1;
//					}
//					
//				}
//				if (nL_Err==1) {
//					nG_MesgCode=13;
//					return -1;
//				}
//				
//				sL_Tmp = sG_CardAcctId.substring(0,8);
//				sG_TmpCardAcctId = sL_Tmp;
//				
//				
//				
//				sG_CardAcctSeq="0";
//				sG_CardHolderId = sL_Tmp;
//				sG_TmpCardHolderId = sL_Tmp;
//				sG_CardCorpId = sL_Tmp;
//				sL_Tmp = sG_CardAcctId;
//				
//				sG_TmpCardAcctId = sG_CardAcctId;
//				
//				sL_Tmp="000";
//				sG_CardHolderSeq=sL_Tmp;
//				sG_CardCorpSeq=sL_Tmp;
//			}
//			else {
//				/*-調整card_acct_index鍵值-------------*/
//				if (sG_CardAcctId.length()!=11)
//					nL_Err=1;
//				if (nL_Err==1) {
//					nG_MesgCode=16;
//					return -1;
//				}
//				
//				nL_PayRule = Integer.parseInt(sL_DbPaymentRule);
//				sL_Tmp = sL_DbCcasAcct.substring(0,1);
//				
//				switch (nL_PayRule) {  /*1:個人繳 2:公司繳*/
//					case 1:
//						sL_Tmp += "2";
//						sG_CardAcctClass = sL_Tmp; /* B2 or C2 */
//						break;
//					case 2:
//						sL_Tmp += "1";
//						sG_CardAcctClass = sL_Tmp;/* B1 or C1 */
//						break;
//						
//					default:
//						System.out.println("DECOMPOSE_ID_SEQ() ERROR! WRONG h_a_payment_rule");
//						nG_MesgCode=4;
//						return -1;
//				}
//				
//				sL_Tmp = sL_DbCardAcctId.substring(0,11);
//				sG_TmpCardAcctId = sL_Tmp;
//				sG_CardAcctSeq="0";
//				
//				sL_Tmp = sL_DbCardHolderId.substring(0,10);
//				sG_TmpCardHolderId = sL_Tmp;
//				
//				sG_CardAcctId = sL_Tmp;
//				
//				sL_Tmp1 = sL_DbCardHolderId.substring(10,11);
//				
//				if ( ("".equals(sL_Tmp1)) || (" ".equals(sL_Tmp1.substring(0,1)))  || ("0".equals(sL_Tmp1.substring(0,1))) ){
//					sG_CardHolderSeq="0";
//					
//					
//				}
//				else {
//					sG_CardHolderSeq = sL_Tmp1;
//					sL_Tmp = sL_Tmp1;
//					
//					
//					
//					sG_CardAcctSeq = sL_Tmp1;
//					
//					
//					
//				}
//				
//		
//				
//				sL_Tmp = sL_DbCardAcctId.substring(0,8);
//				sG_CardCorpId = sL_Tmp;
//				
//				sL_Tmp1 = sL_DbCardAcctId.substring(8,11);
//				sG_CardCorpSeq = sL_Tmp1;
//			}
//		}
//		return nL_Result;
//	}
	
	private int decomposeIdSeq(Data100Vo P_Data100Vo) throws Exception{
		//proc is DECOMPOSE_ID_SEQ()
		int nL_Result = 0;
		try {
			
			String sL_DbCcasAcct="";
			
			String sL_DbAccountType = P_Data100Vo.getAccountType();
			String sL_DbCardAcctId = P_Data100Vo.getCardAcctId();
			String sL_DbCardHolderId = P_Data100Vo.getCardHldrId();
			String sL_DbPaymentRule = P_Data100Vo.getPaymentRule();
			
			sL_DbCcasAcct = "A";
			ResultSet L_SysParm2Rs = CcaSysParm2Dao.getCcaSysParm2("ECSACCT",sL_DbAccountType);
			
			while (L_SysParm2Rs.next()) {
				sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
				
				sL_DbCcasAcct = sL_DbCcasAcct.substring(0, 1);
			}
			
			//System.out.println("Final sL_DbCcasAcct is =>" +sL_DbCcasAcct + "===");
			/*
			if (!CcaSysParm2Dao.isEmptyResultSet(L_SysParm2Rs)) {
				sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
			}
			*/
			L_SysParm2Rs.close();
			/* Howard: 這個 else 不會發生
			else {
				nG_MesgCode=3;
				return -1;
			}
			*/
			
			
			
			int nL_Err=0, nL_PayRule=0;
			
			
			String sL_Tmp="", sL_Tmp1="";
			sG_CardAcctClass =sL_DbCcasAcct;
			System.out.println("sG_CardAcctClass--1 ==>" + sG_CardAcctClass + "---");
			if ("A".equals(sL_DbCcasAcct)) { //一般卡
			
				
				/*-調整card_acct_index鍵值-------------*/
				sL_Tmp = sL_DbCardAcctId.substring(0,10); //sL_DbCardAcctId 就是 proc.h_a_card_acct_id_seq
				
				sL_Tmp1 = sL_DbCardHolderId.substring(0,10); //sL_DbCardHolderId就是 proc.h_a_card_hldr_id_seq
				
				
				if (!sL_Tmp.equals(sL_Tmp1)) {
					System.out.println(sL_DbCardAcctId + "---" + sL_Tmp1);
					nG_MesgCode=2;
					return -1;
				}
				
				System.out.println("AA1");
				
				//sG_CardHolderId = sL_Tmp1;
				//sG_TmpCardHolderId = sL_Tmp1;
				
				sL_Tmp1 = sL_DbCardAcctId.substring(10,11);
				
				if ((" ".equals(sL_Tmp1)) || ("0".equals(sL_Tmp1)) ) { 
					sG_CardAcctSeq="0";
					System.out.println("AA2");
				}
				else {
					sG_CardAcctSeq =sL_Tmp1;
					System.out.println("AA3");
				}

				
				
				sL_Tmp = sL_DbCardAcctId.substring(0,10);
				sG_CardAcctId = sL_Tmp;
				
				if (sG_CardAcctId.charAt(0)>='A') {
					if ((" ".equals(sL_Tmp1)) || ("0".equals(sL_Tmp1)) ) {
						sL_Tmp = sL_Tmp.substring(0,10);
						System.out.println("AA4");
					}
					else {
						sL_Tmp = sL_Tmp + sL_Tmp1;
						System.out.println("AA5");
					}
						
				}
				
				
				
				
				
				
				
				sL_Tmp1 = sL_DbCardHolderId.substring(10,11);
				if ((" ".equals(sL_Tmp1)) || ("0".equals(sL_Tmp1))) {
					System.out.println("AA6");
					sG_CardHolderSeq="";
					
				}
				else{
					sG_CardHolderSeq = sL_Tmp1;
					System.out.println("AA7");
				}
					
				sG_CardHolderId = sL_DbCardHolderId;
				
				
				System.out.println("AA8");
				
			}
			else { //商務卡,   採購卡
				
				sL_Tmp = sL_DbCardAcctId.substring(0,8);
				sG_CardAcctId = sL_DbCardAcctId;
				sG_CardHolderId=sL_DbCardHolderId;
				if ((sL_Tmp.equals(sG_CardHolderId)) || ("".equals(sG_CardHolderId))) {
					if (sL_Tmp.equals(sG_CardHolderId)) {
						/*-調整card_acct_index鍵值-----  */
			            /* card_acct_id=cid(8)+"000" card_hld_id=cid(8)*/
						if (sG_CardAcctId.length()!=11)
							nL_Err=1;
						
						sL_Tmp = sG_CardAcctId.substring(8, 11);
						if ("000".equals(sL_Tmp)) {
								nL_Err=1;
						}
						
					}
					if (nL_Err==1) {
						nG_MesgCode=13;
						return -1;
					}
					
					sL_Tmp = sG_CardAcctId.substring(0,8);
					sG_TmpCardAcctId = sL_Tmp;
					
					
					
					sG_CardAcctSeq="0";
					sG_CardHolderId = sL_Tmp;
					sG_TmpCardHolderId = sL_Tmp;
					sG_CardCorpId = sL_Tmp;
					sL_Tmp = sG_CardAcctId;
					
					sG_TmpCardAcctId = sG_CardAcctId;
					
					sL_Tmp="000";
					sG_CardHolderSeq=sL_Tmp;
					sG_CardCorpSeq=sL_Tmp;
				}
				else {
					/*-調整card_acct_index鍵值-------------*/
					if (sG_CardAcctId.length()!=11)
						nL_Err=1;
					if (nL_Err==1) {
						nG_MesgCode=16;
						return -1;
					}
					
					nL_PayRule = Integer.parseInt(sL_DbPaymentRule);
					sL_Tmp = sL_DbCcasAcct.substring(0,1);
					
					switch (nL_PayRule) {  /*1:個人繳 2:公司繳*/
						case 1:
							sL_Tmp += "2";
							sG_CardAcctClass = sL_Tmp; /* B2 or C2 */
							System.out.println("sG_CardAcctClass--2 ==>" + sG_CardAcctClass + "---");
							break;
						case 2:
							sL_Tmp += "1";
							sG_CardAcctClass = sL_Tmp;/* B1 or C1 */
							System.out.println("sG_CardAcctClass--3 ==>" + sG_CardAcctClass + "---");
							break;
							
						default:
							System.out.println("DECOMPOSE_ID_SEQ() ERROR! WRONG h_a_payment_rule");
							nG_MesgCode=4;
							return -1;
					}
					
					sL_Tmp = sL_DbCardAcctId.substring(0,11);
					sG_TmpCardAcctId = sL_Tmp;
					sG_CardAcctSeq="0";
					
					sL_Tmp = sL_DbCardHolderId.substring(0,10);
					sG_TmpCardHolderId = sL_Tmp;
					
					sG_CardAcctId = sL_Tmp;
					
					sL_Tmp1 = sL_DbCardHolderId.substring(10,11);
					
					if ( ("".equals(sL_Tmp1)) || (" ".equals(sL_Tmp1.substring(0,1)))  || ("0".equals(sL_Tmp1.substring(0,1))) ){
						sG_CardHolderSeq="0";
						
						
					}
					else {
						sG_CardHolderSeq = sL_Tmp1;
						sL_Tmp = sL_Tmp1;
						
						
						
						sG_CardAcctSeq = sL_Tmp1;
						
						
						
					}
					
			
					
					sL_Tmp = sL_DbCardAcctId.substring(0,8);
					sG_CardCorpId = sL_Tmp;
					
					sL_Tmp1 = sL_DbCardAcctId.substring(8,11);
					sG_CardCorpSeq = sL_Tmp1;
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			
			e.printStackTrace(System.out);
			throw e;
		}
		return nL_Result;
	}

	
	private boolean processData100Vo(Data100Vo P_Data100Vo) throws Exception{
		//proc is select_card_acct()
		boolean bL_Result = true;
		try {
			String sL_AccountType="", sL_AccountNo="",  sL_CcsAccountDog="";
			String sL_CardAcctAdjQuota="", sL_PSeqNo="", sL_AcnoPSeqNo="";
			int nL_CardAcctIdx=0;
			CardAcctVo L_CardAcctVo=null; 
            CardAcctVo L_CardAcctVo2=null; 
			
			if (decomposeIdSeq(P_Data100Vo)==0) {
				CrdIdnoVo L_CrdIdnoVo = CrdIdnoDao.getCrdIdno(sG_CardHolderId, sG_CardHolderSeq);
				sL_AccountType = P_Data100Vo.getAccountType();// P_CcsAccountRs.getString("ACCOUNT_TYPE");
				sL_AccountNo = P_Data100Vo.getAcctNo();// P_CcsAccountRs.getString("ACCT_NO");
				sL_CcsAccountDog = P_Data100Vo.getDog();// P_CcsAccountRs.getString("CcsAccountDog");

				
				L_CardAcctVo = CardAcctDao.getCardAcct(P_Data100Vo.getAcnoPSeqNo(), P_Data100Vo.getDebitFlag());
				System.out.print("BB1");
				if (null != L_CardAcctVo) {
					if (nG_MesgCode==0) {
						nL_CardAcctIdx = L_CardAcctVo.getCardAcctIdx();
//						nL_CardAcctParentIdx = L_CardAcctIndexVo.getAcctParentIdx();
						
						//sL_PSeqNo = L_CardAcctIndexVo.getPSeqNo();
						sL_PSeqNo = P_Data100Vo.getPSeqNo();
						sL_AcnoPSeqNo = P_Data100Vo.getAcnoPSeqNo();
						ResultSet L_ActAcnoRs =  ActAcnoDao.getActAcnoByAcnoPSeqNo(sL_AcnoPSeqNo);
						L_ActAcnoRs.next();
						int nL_ActAcnoLineOfCreditAmt = L_ActAcnoRs.getInt("ActAcnoLineOfCreditAmt");
						L_ActAcnoRs.close();
						
						System.out.print("BB2");
						
						sL_CardAcctAdjQuota = L_CardAcctVo.getAdjQuota();
						/*if (!CreditLogDao.insertCreditLog(sL_AccountType, sG_CardHolderId, sG_CardAcctId, sL_CcsAccountDog, L_CardAcctVo, P_Data100Vo, null, "ECS100", nL_ActAcnoLineOfCreditAmt)) {
							nG_MesgCode=10;
							
						}*/
						
						System.out.print("BB3");
						sG_LmtFlag = CreditLogDao.sG_LmtFlag;
						if (nG_MesgCode==0) {
							if (!CardAcctDao.updateCardAcct(L_CardAcctVo, P_Data100Vo, nL_CardAcctIdx)) {
								System.out.print("BB4");
								nG_MesgCode=10;
							}
							if (nG_MesgCode==0) {
								CcaConsumeDao.updateCcaConsume(L_CardAcctVo, P_Data100Vo);
								System.out.print("BB5");
								
							}
						}
						
					}
				}
				else {
					if (("A".equals(sG_CardAcctClass.substring(0, 1))) ||
							("B".equals(sG_CardAcctClass.substring(0, 1))) ||
							("C".equals(sG_CardAcctClass.substring(0, 1))) ){
						//sG_NewCardAcctIdx = CardAcctDao.getNewCardAcctIdxSeqVal();
						//sG_NewCardAcctIdx = HpeUtil.getNextSeqValOfDb2(getDbConnection(), "ECS_CARD_ACCT_IDX.NEXTVAL");

						System.out.print("BB6");
						// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//						sG_NewCardAcctIdx = SystemDao.getNextSeqVal("ECS_CARD_ACCT_IDX");
						sG_NewCardAcctIdx = P_Data100Vo.getAcnoPSeqNo();
						
						System.out.print("BB7");
						if (!"0".equals(sG_NewCardAcctIdx)) {
							insertData(P_Data100Vo);
							
							System.out.print("BB8");
						}
						else {
							nG_MesgCode=5;
						}
					}
					else {
						System.out.print("BB9");

		                L_CardAcctVo2 = CardAcctDao.getCardAcct(P_Data100Vo.getAcnoPSeqNo(), P_Data100Vo.getDebitFlag()); /*IF FIND CARD_PARENT_INDEX*/
						if (null != L_CardAcctVo2) {
							System.out.print("BB10");
							nL_CardAcctIdx = L_CardAcctVo2.getCardAcctIdx();
//							nL_CardAcctParentIdx = L_CardAcctIndexVo2.getAcctParentIdx();
							L_CardAcctVo = CardAcctDao.getCardAcct(nL_CardAcctIdx);
							
							sL_CardAcctAdjQuota = L_CardAcctVo.getAdjQuota();

							// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX 
//							sG_NewCardAcctIdx = CardAcctDao.getNewCardAcctIdxSeqVal();
							sG_NewCardAcctIdx = P_Data100Vo.getAcnoPSeqNo();
							
							if (!"0".equals(sG_NewCardAcctIdx)) {
								System.out.print("BB11");
								insertData(P_Data100Vo);									
							}
						}
						else {
							nG_MesgCode=12;
							System.out.print("BB12");
						}
					}
				}
				
				System.out.println("sG_CardAcctClass=>" +sG_CardAcctClass+ "---");

				//if  ((nG_MesgCode==0) && ("A".equals(sG_CardAcctClass.substring(0, 1))) &&
						//(!"A4".equals(sG_CardAcctClass.substring(0, 2))) ) {
				if  ((nG_MesgCode==0) && ("A".equals(sG_CardAcctClass)) &&				
						(!"A4".equals(sG_CardAcctClass)) ) {
					CardAcctDao.updateCardAcctA();
					System.out.print("BB13");
				}
				//if  ((nG_MesgCode==0) && (!"A".equals(sG_CardAcctClass.substring(0, 1))) ){
				if  ((nG_MesgCode==0) && (!"A".equals(sG_CardAcctClass)) ){
					CardAcctDao.updateCardAcctB();
					System.out.print("BB14");
				}
				
				
				
			}
			/*
			if ((nG_MesgCode == 0) || (nG_MesgCode == 2) || (nG_MesgCode == 13)|| (nG_MesgCode == 16)) {
				String sL_CcsAccountRowId = P_CcsAccountRs.getString("CcsAccountRowId");
				if (CcaAccountDao.updateCcaAccount(nG_MesgCode, sL_CcsAccountRowId)) {
					commitDb();
					if (nG_MesgCode == 0) {
						nG_AcceptCount++;
					}
				}
				else {
					rollbackDb();
					nG_ErrorCount++;
				}
			}
			else {
				rollbackDb();
				nG_ErrorCount++;
			}
			*/
							
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			System.out.println("Exception on processData100Vo()...");
			e.printStackTrace(System.out);
			throw e;
		}
		
		return bL_Result ;
	}
//    private boolean processCcsAccount(ResultSet P_CcsAccountRs) {
//        //proc is select_card_acct()
//        boolean bL_Result = true;
//        try {
//            String sL_AccountType="", sL_AccountNo="",  sL_CcsAccountDog="";
//            String sL_CardAcctAdjQuota="";
//            int nL_CardAcctParentIdx=0, nL_CardAcctIdx=0;
//            CardAcctIndexVo L_CardAcctIndexVo=null;
//            CardAcctIndexVo L_CardAcctIndexVo2=null;
//            CardAcctVo L_CardAcctVo=null; 
//            
//            while (P_CcsAccountRs.next()) {
//                if (decomposeIdSeq(P_CcsAccountRs)==0) {
//                    CrdIdnoVo L_CrdIdnoVo = CrdIdnoDao.getCrdIdno(sG_CardHolderId, sG_CardHolderSeq);
//                    sL_AccountType = P_CcsAccountRs.getString("ACCOUNT_TYPE");
//                    sL_AccountNo = P_CcsAccountRs.getString("ACCT_NO");
//                    sL_CcsAccountDog = P_CcsAccountRs.getString("CcsAccountDog");
//                    
//                    L_CardAcctIndexVo = CardAcctIndexDao.getCardAcctIndex1(sG_CardAcctId, sG_CardCorpId, sG_CardCorpSeq, sL_AccountType, sL_AccountNo);
//                    if (null != L_CardAcctIndexVo) {
//                        if (nG_MesgCode==0) {
//                            nL_CardAcctIdx = L_CardAcctIndexVo.getCardAcctIdx();
//                            nL_CardAcctParentIdx = L_CardAcctIndexVo.getAcctParentIdx();
//                            L_CardAcctVo = CardAcctDao.getCardAcct(nL_CardAcctIdx);
//                            
//                            sL_CardAcctAdjQuota = L_CardAcctVo.getAdjQuota();
//                            
//                            
//                            String sL_PSeqNo = L_CardAcctIndexVo.getPSeqNo();
//                            ResultSet L_ActAcnoRs =  ActAcnoDao.getActAcnoByAcnoPSeqNo(sL_PSeqNo);
//                            int nL_ActAcnoLineOfCreditAmt = L_ActAcnoRs.getInt("ActAcnoLineOfCreditAmt");
//                            L_ActAcnoRs.close();
//
//                            
//                            if (!CreditLogDao.insertCreditLog(sL_AccountType, sG_CardHolderId, sG_CardAcctId, sL_CcsAccountDog, L_CardAcctVo, P_CcsAccountRs, L_CardAcctIndexVo, "ECS100", nL_ActAcnoLineOfCreditAmt)) {
//                                nG_MesgCode=10;
//                                
//                            }
//                            
//                            sG_LmtFlag = CreditLogDao.sG_LmtFlag;
//                            if (nG_MesgCode==0) {
//                                if (!CardAcctDao.updateCardAcct(L_CardAcctVo, P_CcsAccountRs)) {
//                                    nG_MesgCode=10;
//                                }
//                                if (nG_MesgCode==0) {
//                                    CcaConsumeDao.updateCcaConsume(L_CardAcctVo, P_CcsAccountRs);       
//                                }
//                            }
//                            
//                            
//                        }
//                    }
//                    else {
//                        if (("A".equals(sG_CardAcctClass.substring(0, 1))) ||
//                                ("B".equals(sG_CardAcctClass.substring(0, 1))) ||
//                                ("C".equals(sG_CardAcctClass.substring(0, 1))) ){
//                            sG_NewCardAcctIdx = CardAcctDao.getNewCardAcctIdxSeqVal();
//                            
//                            if (!"0".equals(sG_NewCardAcctIdx)) {
//                                insertData(P_CcsAccountRs, nL_CardAcctParentIdx, L_CardAcctIndexVo);
//                                
//          
//                            }
//                            else {
//                                nG_MesgCode=5;
//                            }
//                        }
//                        else {
//                            L_CardAcctIndexVo2 = CardAcctIndexDao.getCardAcctIndex1(sG_CardAcctId, sG_CardCorpId, sG_CardCorpSeq, sL_AccountType, sL_AccountNo); /*IF FIND CARD_PARENT_INDEX*/
//                            if (null != L_CardAcctIndexVo2) {
//                                nL_CardAcctIdx = L_CardAcctIndexVo2.getCardAcctIdx();
//                                nL_CardAcctParentIdx = L_CardAcctIndexVo2.getAcctParentIdx();
//                                L_CardAcctVo = CardAcctDao.getCardAcct(nL_CardAcctIdx);
//                                
//                                sL_CardAcctAdjQuota = L_CardAcctVo.getAdjQuota();
//
//                                sG_NewCardAcctIdx = CardAcctDao.getNewCardAcctIdxSeqVal();
//                                
//                                if (!"0".equals(sG_NewCardAcctIdx)) {
//                                    insertData(P_CcsAccountRs, nL_CardAcctParentIdx, L_CardAcctIndexVo2);                                   
//                                }
//                            }
//                            else {
//                                nG_MesgCode=12;
//                            }
//                        }
//                    }
//                    if  ((nG_MesgCode==0) && ("A".equals(sG_CardAcctClass.substring(0, 1))) &&
//                            (!"A4".equals(sG_CardAcctClass.substring(0, 2))) ) {
//                        CardAcctDao.updateCardAcctA();
//                    }
//                    if  ((nG_MesgCode==0) && (!"A".equals(sG_CardAcctClass.substring(0, 1))) ){
//                        CardAcctDao.updateCardAcctB();
//                    }
//                    
//                    
//                    
//                }
//                if ((nG_MesgCode == 0) || (nG_MesgCode == 2) || (nG_MesgCode == 13)|| (nG_MesgCode == 16)) {
//                    String sL_CcsAccountRowId = P_CcsAccountRs.getString("CcsAccountRowId");
//                    if (CcaAccountDao.updateCcaAccount(nG_MesgCode, sL_CcsAccountRowId)) {
//                        commitDb();
//                        if (nG_MesgCode == 0) {
//                            nG_AcceptCount++;
//                        }
//                    }
//                    else {
//                        rollbackDb();
//                        nG_ErrorCount++;
//                    }
//                }
//                else {
//                    rollbackDb();
//                    nG_ErrorCount++;
//                }
//            }//end while loop
//
//            
//        } catch (Exception e) {
//            // TODO: handle exception
//            bL_Result=false;
//        }
//        return bL_Result;
//    }
	
//	private void insertData(ResultSet P_CcsAccountRs, int nP_CardAcctParentIdx, CardAcctIndexVo P_CardAcctIndexVo) {
//		if(nG_MesgCode==0)  {
//			if (!CardAcctIndexDao.insertCardAcctIdx(sG_NewCardAcctIdx,P_CcsAccountRs,  sG_CardHolderSeq, sG_CardAcctClass, nP_CardAcctParentIdx))
//				nG_MesgCode = 8;
//		}
//        if(nG_MesgCode==0)  {
//        	
//        	if (!CardAcctDao.insertCardAcct(sG_NewCardAcctIdx, P_CcsAccountRs, sG_CardAcctSeq, sG_CardAcctClass, sG_CardCorpId, sG_CardCorpSeq, P_CardAcctIndexVo)) {
//        		nG_MesgCode = 6;
//        	}
//        }
//        if(nG_MesgCode==0) CcaConsumeDao.insertCcaConsume(sG_NewCardAcctIdx, P_CcsAccountRs, sG_CardAcctSeq, sG_CardAcctClass, sG_CardCorpId, sG_CardCorpSeq);		
//	}
	
	public void initProg(Connection P_Db2Conn) throws Exception{
		setDbConn(P_Db2Conn);
		initialPrepareStatement(G_ECS100ID);
		//getAuthParm();
		
	}

	private void insertData(Data100Vo P_Data100Vo) {
		
//		if(nG_MesgCode==0)  {
//			if (!CardAcctIndexDao.insertCardAcctIdx(sG_NewCardAcctIdx,P_Data100Vo,  sG_CardHolderSeq, sG_CardAcctClass, nP_CardAcctParentIdx))
//				nG_MesgCode = 8;
//		}
        if(nG_MesgCode==0)  {
        	
        	if (!CardAcctDao.insertCardAcct(sG_NewCardAcctIdx, P_Data100Vo, sG_CardAcctSeq, sG_CardAcctClass, sG_CardCorpId, sG_CardCorpSeq, null)) {
        		nG_MesgCode = 6;
        	}
        }
        if(nG_MesgCode==0) { 
        	if (!CcaConsumeDao.insertCcaConsume(sG_NewCardAcctIdx, P_Data100Vo, sG_CardAcctSeq, sG_CardAcctClass, sG_CardCorpId, sG_CardCorpSeq)) {
        		nG_MesgCode = 21;// add by Howard
        	}
        }
	}

	@Override
	public void startProcess(String[] sP_Parameters) 
	{

//		try {
//			
//			System.out.println("a");
//			setStopRun(1);
//			System.out.println("a-1");
//
//			connDb();
//
//			System.out.println("b");
//			setSleepTime(3600);
//			int nL_WaitingSec=3;
//			ResultSet L_CcsAccountRs = null;
//			while (1==getStopRun()) {
//				System.out.println("c");
//				setSleepTime(10);
//				setStopRun(1);
//				setPauseRun(1);
//				System.out.println("d");
//				getSysParm3();
//				if (0==getStopRun())
//					break;
//				if (0==getPauseRun()) {
//					Thread.sleep(getSleepTime()*1000);
//					continue;
//					
//				}
//				if (!ifCcsAccountHasData()) {
//					Thread.sleep(nL_WaitingSec*1000);
//					continue;
//					/*
//					getSysParm3();
//					if (0==getStopRun())
//						break;
//					*/
//				}
//				if (!CcaSysParm3Dao.updateSysParm3("ECSCAI","1")) {
//					break;
//				}
//
//				L_CcsAccountRs = CcaAccountDao.getCcaAccount();
//				processCcsAccount(L_CcsAccountRs);
//				CcaAccountDao.releaseConnection(L_CcsAccountRs);
//				break;
//			}
//			closeDb();
//			System.out.println("100..12");
//
//		}
//		catch (Exception e) {
//			
//		}

    }
	
	

}
