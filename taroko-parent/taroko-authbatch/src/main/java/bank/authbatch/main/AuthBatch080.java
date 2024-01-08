/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *       
* 111/01/18  V1.04.01  Justin     fix Erroneous String Compare               *
* 111-01-20  V1.00.02  Justin     fix (Code Correctness: Hidden Method)      * 
*****************************************************************************/
package bank.authbatch.main;



import java.sql.Connection;
import java.sql.ResultSet;


import bank.authbatch.dao.CcaCardBaseDao;
import bank.authbatch.dao.CcaSysParm2Dao;
import bank.authbatch.dao.CardAcctDao;

import bank.authbatch.vo.CcaCardBaseVo;
import bank.authbatch.vo.Data080Vo;
import bank.authbatch.vo.CardAcctVo;

public class AuthBatch080 extends BatchProgBase{

	

	public AuthBatch080() throws Exception {
		// TODO Auto-generated constructor stub

	}

	private String sG_TmpCardHolderIdSeq="";
	private String sG_TmpCardAcctId ="";
	private String sG_TmpCardHolderId="";
	private String sG_CardHolderId="", sG_CardAcctId="", sG_CardCorpId="";
	private String sG_CardCorpSeq="", sG_CardHolderSeq="", sG_CardAcctSeq="";
	private String sG_CardAcctClass="";
	private int nG_MsgCode=0;

	


	
	
//	private void processCcaBaseData() {
//		try {
//			CcaBaseDao L_CcaBaseDao = new CcaBaseDao();
//			ResultSet CcsResultSet = L_CcaBaseDao.getCcaBase(0);
//			String sL_BusinessCard="";/*1:一般卡 2:商務卡*/
//			String sL_ValidFrom="", sL_ValidTo="";
//			
//			CcaBaseVo L_CcaBaseVo = new CcaBaseVo(); 
//					
//			while (CcsResultSet.next()) {
//				
//				L_CcaBaseVo.setAccountType(CcsResultSet.getString("CcaBaseAccountType"));
//				L_CcaBaseVo.setAcctNo(CcsResultSet.getString("CcaBaseAcctNo"));
//				L_CcaBaseVo.setBankActNo(CcsResultSet.getString("CcaBaseBankActNo"));
//				L_CcaBaseVo.setBusinessCard(CcsResultSet.getString("CcaBaseBusinessCard"));
//				L_CcaBaseVo.setCardAcctId(CcsResultSet.getString("CcaBaseCardAcctId"));
//				L_CcaBaseVo.setIdPSqno(CcsResultSet.getString("CcaBaseIdPSeqNo"));
//				
//				L_CcaBaseVo.setCardNo(CcsResultSet.getString("CcaBaseCardNo"));
//				L_CcaBaseVo.setCardType(CcsResultSet.getString("CcaBaseCardType"));
//				L_CcaBaseVo.setComboIndicator(CcsResultSet.getString("CcaBaseComboIndicator"));
//				L_CcaBaseVo.setCreditLimit(CcsResultSet.getString("CcaBaseCreditLimit"));
//				L_CcaBaseVo.setCvc2(CcsResultSet.getString("CcaBaseCvc2"));
//				L_CcaBaseVo.setEngName(CcsResultSet.getString("CcaBaseEngName"));
//				L_CcaBaseVo.setGroupCode(CcsResultSet.getString("CcaBaseGroupCode"));
//				L_CcaBaseVo.setMemberSince(CcsResultSet.getString("CcaBaseMemberSince"));
//				L_CcaBaseVo.setOldCardNo(CcsResultSet.getString("CcaBaseOldCardNo"));
//				L_CcaBaseVo.setPaymentRule(CcsResultSet.getString("CcaBasePaymentRule"));
//				L_CcaBaseVo.setPinBlock(CcsResultSet.getString("CcaBasePinBlock"));
//				L_CcaBaseVo.setPinOfActive(CcsResultSet.getString("CcaBasePinOfActive"));
//				L_CcaBaseVo.setPinOfVoice(CcsResultSet.getString("CcaBasePinOfVoice"));
//				L_CcaBaseVo.setPvki(CcsResultSet.getString("CcaBasePvki"));
//				L_CcaBaseVo.setRule(CcsResultSet.getString("CcaBaseRule"));
//				L_CcaBaseVo.setSource(CcsResultSet.getString("CcaBaseSource"));
//				L_CcaBaseVo.setValidFrom(CcsResultSet.getString("CcaBaseValidFrom"));
//				L_CcaBaseVo.setValidTo(CcsResultSet.getString("CcaBaseValidTo"));
//				
//				L_CcaBaseVo.setDcCurrCode(CcsResultSet.getString("CcaBaseDcCurrCode"));
//				L_CcaBaseVo.setAcnoFlag(CcsResultSet.getString("CcaBaseAcNoFlag"));
//				L_CcaBaseVo.setMajorIdPSeqNo(CcsResultSet.getString("CcaBaseMajorIdPSeqNo"));
//				L_CcaBaseVo.setCorpPSeqN0(CcsResultSet.getString("CcaBaseCorpPSeqNo"));
//				L_CcaBaseVo.setGpNo(CcsResultSet.getString("CcaBaseGpNo"));
//				
//				//down, set value of BusinessCard 
//				if ("2".equals(CcsResultSet.getString("CcaBaseRule"))) {
//					sL_BusinessCard = "Y";/*1:一般卡 2:商務卡*/
//				}
//				else
//					sL_BusinessCard = "N";/*1:一般卡 2:商務卡*/
//
//				if (CcsResultSet.getInt("CcaBaseCreditLimit")>0)
//					sL_BusinessCard = "M";
//				//up, set value of BusinessCard
//				
//				//down, set value of ValidFrom and ValidTo
//				sL_ValidFrom = CcsResultSet.getString("CcaBaseValidFrom") + "01";
//				
//				sL_ValidTo = CcsResultSet.getString("CcaBaseValidTo");
//				sL_ValidTo = HpeUtil.getMonthEndDate(sL_ValidTo.substring(0, 4), sL_ValidTo.substring(4,6));
//				//up, set value of ValidFrom and ValidTo				
//				
//				
//				if (decomposeIdSeq(L_CcaBaseVo)==0) {
//					int nL_Source = Integer.parseInt(CcsResultSet.getString("CcaBaseSource"));
//					
//					System.out.println("nL_Source=>" + nL_Source + "---");
//					switch (nL_Source) {
//					case 1:
//						/*Process New card*/
//						newCard(L_CcaBaseVo, sG_TmpCardAcctId, sG_CardAcctId, sG_CardAcctClass, sG_TmpCardHolderId);
//						break;
//					case 2:
//						/*Process CHANGE_CARD*/
//						changeCard(L_CcaBaseVo);
//						break;
//					case 3:
//						/*Process DAMAGE_REMAKE*/
//						demageRemake(L_CcaBaseVo);
//						break;
//					case 4:
//						/*Process LOSE_REISSUE*/
//						loseReissue(L_CcaBaseVo, sG_TmpCardAcctId, sG_CardAcctId, sG_CardAcctClass, sG_TmpCardHolderId);
//						
//						break;
//					case 5:
//						/*Process SET_AMOUNT*/
//						setAmount(L_CcaBaseVo);
//						break;
//					
//					default:
//						break;
//					}
//					
//				}
//			}
//			CcsResultSet.close();
//			
//			
//			if (nG_MsgCode != 0) {
//				
//				
//				if (nG_MsgCode != 2) {
//					if (updateCcaBaseProcessStatus(L_CcaBaseVo))
//						L_CcaBaseDao.Db2Connection.commit();
//					else
//						L_CcaBaseDao.Db2Connection.rollback();
//				}
//			}
//			else {
//				if (updateCcaBaseProcessStatus(L_CcaBaseVo))
//					L_CcaBaseDao.Db2Connection.commit();
//				else
//					L_CcaBaseDao.Db2Connection.rollback();
//			}
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//	}

	private String newCard(Data080Vo P_Data080Vo, String sP_TmpCardAcctId, String sP_CardAcctId, String sP_CardAcctClass, String sP_TmpCardHolderId) throws Exception{
		String sL_Result = "";
		
		try {
			CcaCardBaseDao L_CcaCardBaseDao = new CcaCardBaseDao();
			CcaCardBaseVo L_CcaCardBaseVo = L_CcaCardBaseDao.getCcaCardBase(P_Data080Vo.getCardNo()); 
			if (null != L_CcaCardBaseVo) {
				nG_MsgCode=11;
				return sL_Result;
			}
			
			CardAcctDao L_CardAcctDao = new CardAcctDao();
			CardAcctVo L_CardAcctVo=null; 
			L_CardAcctVo = L_CardAcctDao.getCardAcct(P_Data080Vo.getAcnoPSeqNo(), P_Data080Vo.getDebitFlag());
			if ( null ==  L_CardAcctVo) {			
				nG_MsgCode=2;
				return sL_Result;
				
			}
			
			L_CcaCardBaseDao.insertCcaCardBase(P_Data080Vo, L_CardAcctVo.getCardAcctIdx());
			nG_MsgCode = 0;
		} catch (Exception e) {
			// TODO: handle exception
			nG_MsgCode = -1;
			System.out.println("Exception on newCard() =>" + e.getMessage() + "---");
			e.printStackTrace(System.out);
			throw e;
		}
		return sL_Result;
	}

//	private String newCard(CcaBaseVo P_CcaBaseVo, String sP_TmpCardAcctId, String sP_CardAcctId, String sP_CardAcctClass, String sP_TmpCardHolderId) {
//		String sL_Result = "";
//		
//		try {
//			
//			CcaCardBaseDao L_CcaCardBaseDao = new CcaCardBaseDao();
//			CcaCardBaseVo L_CcaCardBaseVo = L_CcaCardBaseDao.getCcaCardBase(P_CcaBaseVo.getCardNo()); 
//			if (null != L_CcaCardBaseVo) {
//				nG_MsgCode=11;
//				return sL_Result;
//			}
//			
//			CardAcctIndexDao L_CardAcctIndexDao = new CardAcctIndexDao();
//			if ( 0 != L_CardAcctIndexDao.getCardAcctIndex(1, P_CcaBaseVo, sP_TmpCardAcctId, sP_CardAcctId, sP_CardAcctClass, sP_TmpCardHolderId)) {
//				nG_MsgCode=2;
//				return sL_Result;
//				
//			}
//			
//			L_CcaCardBaseDao.insertCcaCardBase(P_CcaBaseVo, L_CardAcctIndexDao.nG_CardAcctIdx);
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			System.out.println("Exception on newCard()=>");
//			e.printStackTrace(System.out);
//		}
//		return sL_Result;
//	}
	
//	private String loseReissue(CcaBaseVo P_CcaBaseVo, String sP_TmpCardAcctId, String sP_CardAcctId, String sP_CardAcctClass, String sP_TmpCardHolderId) {
//		//proc is LOSE_REISSUE()
//		//Howard: proc 中 LOSE_REISSUE() 與 NEW_CARD() 做一樣的事
//		String sL_Result = newCard(P_CcaBaseVo,  sP_TmpCardAcctId,  sP_CardAcctId, sP_CardAcctClass, sP_TmpCardHolderId);
//		/*
//		try {
//			CcaCardBaseDao L_CcaCardBaseDao = new CcaCardBaseDao();
//			CcaCardBaseVo L_CcaCardBaseVo = L_CcaCardBaseDao.getCcaCardBase(P_CcaBaseVo.getCardNo()); 
//			if (null != L_CcaCardBaseVo) {
//				nG_MsgCode=11;
//				return sL_Result;
//			}
//			
//			CardAcctIndexDao L_CardAcctIndexDao = new CardAcctIndexDao();
//			if ( 0 != L_CardAcctIndexDao.getCardAcctIndex(1, P_CcaBaseVo, sP_TmpCardAcctId, sP_CardAcctId, sP_CardAcctClass, sP_TmpCardHolderId)) {
//				nG_MsgCode=2;
//				return sL_Result;
//				
//			}
//			
//			L_CcaCardBaseDao.insertCcaCardBase(P_CcaBaseVo, L_CardAcctIndexDao.nG_CardAcctIdx);
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		*/
//		return sL_Result;
//	}

	private String loseReissue(Data080Vo P_Data080Vo, String sP_TmpCardAcctId, String sP_CardAcctId, String sP_CardAcctClass, String sP_TmpCardHolderId) throws Exception{
		//proc is LOSE_REISSUE()
		//Howard: proc 中 LOSE_REISSUE() 與 NEW_CARD() 做一樣的事
		String sL_Result ="";
		try {
			sL_Result = newCard(P_Data080Vo,  sP_TmpCardAcctId,  sP_CardAcctId, sP_CardAcctClass, sP_TmpCardHolderId);			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception on loseReissue=>" + e.getMessage() + "---");
			e.printStackTrace(System.out);
			throw e;
		}
		
		/*
		try {
			CcaCardBaseDao L_CcaCardBaseDao = new CcaCardBaseDao();
			CcaCardBaseVo L_CcaCardBaseVo = L_CcaCardBaseDao.getCcaCardBase(P_CcaBaseVo.getCardNo()); 
			if (null != L_CcaCardBaseVo) {
				nG_MsgCode=11;
				return sL_Result;
			}
			
			CardAcctIndexDao L_CardAcctIndexDao = new CardAcctIndexDao();
			if ( 0 != L_CardAcctIndexDao.getCardAcctIndex(1, P_CcaBaseVo, sP_TmpCardAcctId, sP_CardAcctId, sP_CardAcctClass, sP_TmpCardHolderId)) {
				nG_MsgCode=2;
				return sL_Result;
				
			}
			
			L_CcaCardBaseDao.insertCcaCardBase(P_CcaBaseVo, L_CardAcctIndexDao.nG_CardAcctIdx);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		*/
		return sL_Result;
	}

	private String changeCard(Data080Vo P_Data080Vo) throws Exception{
		String sL_Result = "";
		
		try {
			CcaCardBaseDao L_CcaCardBaseDao = new CcaCardBaseDao();
			CcaCardBaseVo L_CcaCardBaseVo = L_CcaCardBaseDao.getCcaCardBase(P_Data080Vo.getCardNo()); 
			if (null != L_CcaCardBaseVo) {
				nG_MsgCode=12;
				return sL_Result;
			}
			
			L_CcaCardBaseDao.updateCcaCardBase(P_Data080Vo);
			nG_MsgCode=0;
			
		} catch (Exception e) {
			// TODO: handle exception
			nG_MsgCode=-1;
			throw e;
		}
		return sL_Result;
	}

	private String setAmount(Data080Vo P_Data080Vo) {
		
		if (1==1) {
			nG_MsgCode = 0;
			return ""; //Howard: 新系統中不需要同步金額，所以直接return
		}
		String sL_Result = "";
		
		try {
			CcaCardBaseDao L_CcaCardBaseDao = new CcaCardBaseDao();
			CcaCardBaseVo L_CcaCardBaseVo = L_CcaCardBaseDao.getCcaCardBase(P_Data080Vo.getCardNo()); 
			if (null != L_CcaCardBaseVo) {
				nG_MsgCode=12;
				return sL_Result;
			}
			
			/*
			Howard: 在此要 update Cca_Card_Base.CARD_CREDIT，但新系統中已經無此欄位，所以不需要 update 
			if (!L_CcaCardBaseDao.updateCardCredit()) { //此 function 還沒有寫
				nG_MsgCode=8;
				return "";
			} 
			*/
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return sL_Result;
	}

	private String demageRemake(Data080Vo P_Datga080Vo) throws Exception{
		//proc is DAMAGE_REMAKE()
		String sL_Result = "";
		try {
			sL_Result = changeCard(P_Datga080Vo); //Howard: proc 也是做changeCard 一樣的事情			
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
		
		/*
		try {
			CcaCardBaseDao L_CcaCardBaseDao = new CcaCardBaseDao();
			CcaCardBaseVo L_CcaCardBaseVo = L_CcaCardBaseDao.getCcaCardBase(P_CcaBaseVo.getCardNo()); 
			if (null != L_CcaCardBaseVo) {
				nG_MsgCode=12;
				return sL_Result;
			}
			
			L_CcaCardBaseDao.updateCcaCardBase(P_CcaBaseVo);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		*/
		return sL_Result;
		
	}

	private int decomposeIdSeq(Data080Vo  P_Data080Vo) throws Exception{
		//proc is DECOMPOSE_ID_SEQ()
		int nL_Result = 0;
		String sL_DbCcasAcct="";
		
		try {
			sL_DbCcasAcct = "A";
			//ResultSet L_SysParm2Rs = CcaSysParm2Dao.getCcaSysParm2("ECSACCT",P_CcaBaseVo.getAccountType());
			ResultSet L_SysParm2Rs = CcaSysParm2Dao.getCcaSysParm2("ECSACCT", P_Data080Vo.getAccountType() );
			
			while (L_SysParm2Rs.next() ) {
				sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
			}
			/*
			if (!CcaSysParm2Dao.isEmptyResultSet(L_SysParm2Rs)) {
				sL_DbCcasAcct = L_SysParm2Rs.getString("SysData1");
			}
			*/
			L_SysParm2Rs.close();
			int nL_Err=0, nL_PayRule=0;
			
			
			String sL_Tmp="", sL_Tmp1="";
			sG_CardAcctClass =sL_DbCcasAcct;
			if ("A".equals(sL_DbCcasAcct)) { //一般卡
			
				
				/*-調整card_acct_index鍵值-------------*/
				//sL_Tmp1 = P_CcaBaseVo.getCardHolderId().substring(0,10);
				sL_Tmp1 = P_Data080Vo.getCardHolderId().substring(0,10);
				
				sG_CardHolderId = sL_Tmp1;
				sG_TmpCardHolderId = sL_Tmp1;
				
				//sL_Tmp = P_CcaBaseVo.getCardHolderId().substring(10,11);
				sL_Tmp = P_Data080Vo.getCardHolderId().substring(10,11);
				if ("".equals(sL_Tmp))
					sG_CardHolderSeq="0";
				else
					sG_CardHolderSeq =sL_Tmp; 

				//sL_Tmp = P_CcaBaseVo.getCardAcctId().substring(0,10);
				sL_Tmp = P_Data080Vo.getCardAcctId().substring(0,10);
				
				sG_CardAcctId = sL_Tmp;
				
				//sL_Tmp1 = P_CcaBaseVo.getCardAcctId().substring(10,11);
				sL_Tmp1 = P_Data080Vo.getCardAcctId().substring(10,11);
				
				if ("".equals(sL_Tmp1)) {
					
					sG_CardAcctSeq="0";
				}
				else{
					sG_CardAcctSeq = sL_Tmp1;
				}
					
				
				//if ((int)(P_CcaBaseVo.getCardAcctId().substring(0,1).toCharArray()[0]) >= (int)('A')) {
				if ((int)(P_Data080Vo.getCardAcctId().substring(0,1).toCharArray()[0]) >= (int)('A')) {			
					if ("".equals(sL_Tmp1))
						sL_Tmp="0";
					else
						sL_Tmp = sL_Tmp1;
				}
				sG_TmpCardAcctId= sL_Tmp;	
			}
			else { //商務卡,   採購卡
				//sL_Tmp = P_CcaBaseVo.getCardAcctId().substring(0,8);
				sL_Tmp = P_Data080Vo.getCardAcctId().substring(0,8);
				/*
				sG_CardAcctId = P_CcaBaseVo.getCardAcctId();
				sG_CardHolderId=P_CcaBaseVo.getCardHolderId();
				*/
				sG_CardAcctId = P_Data080Vo.getCardAcctId();
				sG_CardHolderId=P_Data080Vo.getCardHolderId();

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
						return -1;
					}
					
					sL_Tmp = sG_CardAcctId.substring(0,8);
					sG_CardAcctId = sL_Tmp;
					
					
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
					if (nL_Err==1)
						return -1;
					
					//nL_PayRule = Integer.parseInt(P_CcaBaseVo.getPaymentRule());
					nL_PayRule = Integer.parseInt(P_Data080Vo.getPaymentRule());
					sL_Tmp = sL_DbCcasAcct;
					
					switch (nL_PayRule) {  /*1:個人繳 2:公司繳*/
						case 1:
							sL_Tmp += "2";
							sG_CardAcctClass = sL_Tmp;
							break;
						case 2:
							sL_Tmp += "1";
							sG_CardAcctClass = sL_Tmp;
							break;
							
						default:
							//System.out.println("DECOMPOSE_ID_SEQ() ERROR! WRONG h_a_payment_rule");
							return -1;
					}
					//sL_Tmp = P_CcaBaseVo.getCardAcctId().substring(0,11);
					sL_Tmp = P_Data080Vo.getCardAcctId().substring(0,11);
					
					sG_TmpCardAcctId = sL_Tmp;
					
					//sL_Tmp = P_CcaBaseVo.getCardHolderId().substring(0,10);
					sL_Tmp = P_Data080Vo.getCardHolderId().substring(0,10);
					
					sG_CardHolderId = sL_Tmp;
					sG_CardAcctId = sL_Tmp;
					
					//sL_Tmp1 = P_CcaBaseVo.getCardHolderId().substring(10,11);
					sL_Tmp1 = P_Data080Vo.getCardHolderId().substring(10,11);
					
					if ("".equals(sL_Tmp1)) {
						sG_CardHolderSeq="0";
						sG_CardAcctSeq = sL_Tmp1;
					}
					else {
						sG_CardHolderSeq = sL_Tmp1;
						
						sG_CardAcctSeq = sL_Tmp1;
						
						sL_Tmp = sL_Tmp+sL_Tmp1;
						
					}
					
					sG_TmpCardHolderId = sL_Tmp;
					
					//sL_Tmp = P_CcaBaseVo.getCardAcctId().substring(0,8);
					sL_Tmp = P_Data080Vo.getCardAcctId().substring(0,8);
					sG_CardCorpId = sL_Tmp;
					
					//sL_Tmp = P_CcaBaseVo.getCardAcctId().substring(8,11);
					sL_Tmp = P_Data080Vo.getCardAcctId().substring(8,11);
					sG_CardCorpSeq = sL_Tmp;
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
		
		return nL_Result;
	}

	private int processCcaBaseData(Data080Vo  P_Data080Vo) throws Exception{
		
		int nL_Result = 0;
		try {
			String sL_BusinessCard="";/*1:一般卡 2:商務卡*/
			String sL_ValidFrom="", sL_ValidTo="";
			
			if ("2".equals(P_Data080Vo.getRule())) {
				sL_BusinessCard = "Y";/*1:一般卡 2:商務卡*/
			}
			else
				sL_BusinessCard = "N";/*1:一般卡 2:商務卡*/
			
			if (P_Data080Vo.getCreditLimit()>0)
				sL_BusinessCard = "M";
			/*
				if ("2".equals(CcsResultSet.getString("CcaBaseRule"))) {
					sL_BusinessCard = "Y"; //1:一般卡 2:商務卡
				}
				else
					sL_BusinessCard = "N";//1:一般卡 2:商務卡
					
				if (CcsResultSet.getInt("CcaBaseCreditLimit")>0)
					sL_BusinessCard = "M";					
			*/
			
			//up, set value of BusinessCard
				
			//down, set value of ValidFrom and ValidTo
			sL_ValidFrom = P_Data080Vo.getValidFrom() + "01";
			
			sL_ValidTo = P_Data080Vo.getValidTo();
			sL_ValidTo = HpeUtil.getMonthEndDate(sL_ValidTo.substring(0, 4), sL_ValidTo.substring(4,6));

			
			/*
				sL_ValidFrom = CcsResultSet.getString("CcaBaseValidFrom") + "01";
				
				sL_ValidTo = CcsResultSet.getString("CcaBaseValidTo");
				sL_ValidTo = HpeUtil.getMonthEndDate(sL_ValidTo.substring(0, 4), sL_ValidTo.substring(4,6));
			*/
			//up, set value of ValidFrom and ValidTo				
				
				
				//if (decomposeIdSeq(L_CcaBaseVo)==0) {
				if (decomposeIdSeq(P_Data080Vo)==0) {
					
					//int nL_Source = Integer.parseInt(CcsResultSet.getString("CcaBaseSource"));
					int nL_Source = Integer.parseInt(P_Data080Vo.getSource());
					
					switch (nL_Source) {
					case 1:
						/*Process New card*/
						newCard(P_Data080Vo, sG_TmpCardAcctId, sG_CardAcctId, sG_CardAcctClass, sG_TmpCardHolderId);
						break;
					case 2:
						/*Process CHANGE_CARD*/
						changeCard(P_Data080Vo);
						break;
					case 3:
						/*Process DAMAGE_REMAKE*/
						demageRemake(P_Data080Vo);
						break;
					case 4:
						/*Process LOSE_REISSUE*/
						loseReissue(P_Data080Vo, sG_TmpCardAcctId, sG_CardAcctId, sG_CardAcctClass, sG_TmpCardHolderId);
						
						break;
					case 5:
						/*Process SET_AMOUNT*/
						setAmount(P_Data080Vo);
						break;
					
					default:
						break;
					}
					
				}
			
			
			if (nG_MsgCode != 0) {
				nL_Result = nG_MsgCode;
				//bL_Result = false;
				/*
				if (nG_MsgCode != 2) {
					if (updateCcaBaseProcessStatus(L_CcaBaseVo))
						L_CcaBaseDao.Db2Connection.commit();
					else
						L_CcaBaseDao.Db2Connection.rollback();
				}
				*/
			}
			else {
				nL_Result = 0;
				//bL_Result = true;
				/*
				if (updateCcaBaseProcessStatus(L_CcaBaseVo))
					L_CcaBaseDao.Db2Connection.commit();
				else
					L_CcaBaseDao.Db2Connection.rollback();
				*/
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			nL_Result = -1;
			//bL_Result = false;
			System.out.println("Exeption on processCcaBaseData()=>" + e.getMessage() + "---");
			e.printStackTrace(System.out);
			throw  e;
		}
		
		
		return nL_Result;
	}
	
	public void initProg(Connection P_Db2Conn) throws Exception{
		setDbConn(P_Db2Conn);
		initialPrepareStatement(G_ECS080ID);
		//getAuthParm();
		
	}

	public int startProcess(Data080Vo  P_Data080Vo ) {
		
		// 0=> 正常 執行結束
		// 大於 0 => 程式正常結束，但資料不正常
		// -1 => error or exception

		int nL_Result = 0;
		try {
			writeLog("I", "begin startProcess Data080Vo...");
			nL_Result = processCcaBaseData(P_Data080Vo);
			writeLog("I", "end startProcess Data080Vo...");
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exeption on startProcess()=>" + e.getMessage() + "---");
			e.printStackTrace(System.out);
			nL_Result = -1;
		}
		
		return nL_Result;
	}
	@Override
	public void startProcess(String[] sP_Parameters) 
	{

    }



}
