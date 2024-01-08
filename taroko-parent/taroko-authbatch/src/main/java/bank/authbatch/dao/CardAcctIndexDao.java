package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.spi.DirStateFactory.Result;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;

import bank.authbatch.vo.CardAcctIndexVo;
import bank.authbatch.vo.CcaBaseVo;
import bank.authbatch.vo.Data080Vo;
import bank.authbatch.vo.Data100Vo;

public class CardAcctIndexDao extends AuthBatchDbHandler{

	public CardAcctIndexDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static int nG_CardAcctIdx=0;
	public static int nG_CardAcctParentIdx=0;
	
	public static boolean deleteCardAcctIdx(String sP_CardAcctIdx) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "DELETE CCA_CARD_ACCT_INDEX WHERE CARD_ACCT_IDX = ? ";
			
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
	         ps.setString(1, sP_CardAcctIdx);
	         
	         ps.executeUpdate();
	         ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
		
	}
	public static boolean insertCardAcctIdx(String sP_NewCardAcctIdx, ResultSet P_CcsAccountRs, String sP_CardHolderSeq, String sP_CardAcctClass, int nP_CardAcctParentIdx) {
		
		//proc is INSERT_CARD_ACCT_INDEX()
		boolean bL_Result = true;
		try {
	         String sL_Sql = "INSERT INTO CCA_CARD_ACCT_INDEX( "
                      + "CARD_ACCT_IDX,      CARD_ACCT_ID, "
                      + "CARD_ACCT_ID_SEQ,   CARD_CORP_ID,"
                      + "CARD_CORP_ID_SEQ,   CARD_ACCT_CLASS,"
                      //+ "ECS_ACCT_CLASS ,    ACCT_NO," // ECS_Acct_class 改為 ACCT_TYPE
                      + "ACCT_TYPE ,    ACCT_NO," // ECS_Acct_class 改為 ACCT_TYPE
                      + "ACCT_PARENT_INDEX  )"
                      + "VALUES ( ?,?,?,?,?,?,?,?,? )";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
	         PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
	         ps.setString(1, sP_NewCardAcctIdx); //h_new_card_acct_idx
	         ps.setString(2, P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_tmp_card_acct_id
	         ps.setString(3, ""); //
	         ps.setString(4, P_CcsAccountRs.getString("CARD_HLDR_ID")); //h_tmp_card_hldr_id
	         
	        	 
	         ps.setString(5, sP_CardHolderSeq); //h_tmp_card_hldr_seq
	         ps.setString(6, sP_CardAcctClass); //h_a_card_acct_class
	         ps.setString(7, P_CcsAccountRs.getString("ACCOUNT_TYPE")); //h_a_account_type  
	         ps.setString(8, P_CcsAccountRs.getString("ACCT_NO")); //h_a_acct_no
	         ps.setInt(9, nP_CardAcctParentIdx); //h_card_acct_parent_idx
//	         ps.setString(10, P_CcsAccountRs.getString("DEBIT_FLAG")); // debit_flag
	         
	         ps.executeUpdate();
	         ps.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			System.out.println("Exception on insertCardAcctIdx()...");
			e.printStackTrace(System.out);
		}
		
		return bL_Result;
	}

	public static boolean insertCardAcctIdx(String sP_NewCardAcctIdx, Data100Vo P_Data100Vo, String sP_CardHolderSeq, String sP_CardAcctClass, int nP_CardAcctParentIdx) {
		
		//proc is INSERT_CARD_ACCT_INDEX()
		boolean bL_Result = true;
		try {
			/*
	         String sL_Sql = "INSERT INTO CCA_CARD_ACCT_INDEX( "
                      + "CARD_ACCT_IDX,      CARD_ACCT_ID, "
                      + "CARD_ACCT_ID_SEQ,   CARD_CORP_ID,"
                      + "CARD_CORP_ID_SEQ,   CARD_ACCT_CLASS,"
                      + "ECS_ACCT_CLASS ,    ACCT_NO,"
                      + "ACCT_PARENT_INDEX  )"
                      + "VALUES ( ?,?,?,?,?,?,?,?,? )";
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
	         PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
	         ps.setString(1, sP_NewCardAcctIdx); //h_new_card_acct_idx
	         ps.setString(2, P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_tmp_card_acct_id
	         ps.setString(3, ""); //
	         ps.setString(4, P_CcsAccountRs.getString("CARD_HLDR_ID")); //h_tmp_card_hldr_id
	         
	        	 
	         ps.setString(5, sP_CardHolderSeq); //h_tmp_card_hldr_seq
	         ps.setString(6, sP_CardAcctClass); //h_a_card_acct_class
	         ps.setString(7, P_CcsAccountRs.getString("ACCOUNT_TYPE")); //h_a_account_type  
	         ps.setString(8, P_CcsAccountRs.getString("ACCT_NO")); //h_a_acct_no
	         ps.setInt(9, nP_CardAcctParentIdx); //h_card_acct_parent_idx

	         ps.executeUpdate();
	         ps.close();
	         */
			
			
			
			G_Ps4CardAcctIndex3.setString(1, sP_NewCardAcctIdx); //h_new_card_acct_idx
			G_Ps4CardAcctIndex3.setString(2, P_Data100Vo.getCardAcctId());// P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_tmp_card_acct_id			G_Ps4CardAcctIndex3.setString(3, ""); //
			G_Ps4CardAcctIndex3.setString(3, "");//CARD_ACCT_ID_SEQ
			G_Ps4CardAcctIndex3.setString(4, P_Data100Vo.getCorpPSeqN0());// P_CcsAccountRs.getString("CARD_HLDR_ID")); //h_tmp_card_hldr_id
	         
	        	 
			G_Ps4CardAcctIndex3.setString(5, sP_CardHolderSeq); //h_tmp_card_hldr_seq
			G_Ps4CardAcctIndex3.setString(6, sP_CardAcctClass); //h_a_card_acct_class
			G_Ps4CardAcctIndex3.setString(7, P_Data100Vo.getAccountType());// P_CcsAccountRs.getString("ACCOUNT_TYPE")); //h_a_account_type  
			G_Ps4CardAcctIndex3.setString(8, P_Data100Vo.getAcctNo());// P_CcsAccountRs.getString("ACCT_NO")); //h_a_acct_no
			G_Ps4CardAcctIndex3.setInt(9, nP_CardAcctParentIdx); //h_card_acct_parent_idx
			
			G_Ps4CardAcctIndex3.executeUpdate();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			System.out.println("Exception on insertCardAcctIdx()...");
			e.printStackTrace(System.out);

		}
		
		return bL_Result;
	}

	
	public static ResultSet getCardAcctIndex5(String sP_CardAcctId, String sP_CardCorpId, String sP_EcsAcctClass, String sP_AcctNo) {
		
		ResultSet L_ResultSet=null;
		try {

			String sL_Sql = "SELECT NVL(CARD_ACCT_IDX,0) as CardAcctIdx, NVL(ACCT_PARENT_INDEX,0) as AcctParentIndex "
							+"FROM CCA_CARD_ACCT_INDEX "
							+"where CARD_ACCT_ID= ? "
							//+"AND ECS_ACCT_CLASS= ? "; // Howard: ECS_ACCT_CLASS change to ACCT_TYPE
							+"AND ACCT_TYPE= ? "; // Howard: ECS_ACCT_CLASS change to ACCT_TYPE
			
			if (!sP_CardCorpId.equals("")) {
				sL_Sql += " or  (CARD_CORP_ID||CARD_CORP_ID_SEQ= ?) ";
				
			}
			else {
				sL_Sql += " or (NVL(CARD_CORP_ID,'')= ? ) ";
			}

			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			
			ps.setString(1, sP_CardAcctId);
			ps.setString(2, sP_EcsAcctClass);

			ps.setString(3, sP_CardCorpId);

			if (!sP_AcctNo.equals("")) {
				sL_Sql += "AND ACCT_NO= ? ";
				ps.setString(4, sP_AcctNo);
			}
						
			
			
			
			
			
			
			
			L_ResultSet = ps.executeQuery();
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		
		return L_ResultSet;
	}

	public static CardAcctIndexVo getCardAcctIndex1(String sP_CardAcctId, String sP_CardCorpId, String sP_CardCorpIdSeq, String sP_AcctClass, String sP_AcctNo) throws Exception{
		//proc is GET_CARD_ACCT_IDX(1) for AuthBatch_100
		
		ResultSet L_ResultSet = null;
		CardAcctIndexVo L_CardAcctIndexVo = null;
		
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		
		/*
		String sL_Sql = "select A.CARD_ACCT_IDX as CARDACCTIDX,A.ACCT_PARENT_INDEX as ACCTPARENTIDX, A.CARD_ACCT_ID as CARDACCTID,  A.CARD_CORP_ID as CARDCORPID, SUM(DECODE(TRANS_TYPE,'11',1,0)) as TRANSTYPE1,  SUM(DECODE(TRANS_TYPE,'17',1,0)) as TRANSTYPE2, "
				 + " C.Debit_Flag as DEBITFLAG, C.P_SEQNO as PSEQNO, C.ID_P_SEQNO as PIDSEQNO"
				+ "from CCA_CARD_ACCT_INDEX A,CCA_CARD_BASE C, ONBAT_2CCAS E ";
		sL_Sql += " where A.CARD_ACCT_ID= ? AND   A.CARD_CORP_ID= ? ";
		sL_Sql += " AND   NVL(A.CARD_CORP_ID_SEQ,'0') = NVL( ? ,'0') AND ECS_ACCT_CLASS = ? ";
		sL_Sql += " AND NVL(A.ACCT_NO,'00000000000') = nvl( ? ,'00000000000') ";
		
		sL_Sql += " AND A.CARD_ACCT_IDX = (SELECT MIN(CARD_ACCT_IDX) FROM CCA_CARD_ACCT_INDEX ";
		sL_Sql += " WHERE CARD_ACCT_ID = ? AND   CARD_CORP_ID = ? ";
		sL_Sql += " AND   NVL(CARD_CORP_ID_SEQ,'0') = NVL( ? ,'0') AND ECS_ACCT_CLASS = ? ";
		sL_Sql += " AND NVL(ACCT_NO,'00000000000') = nvl( ?,'00000000000')) ";
		sL_Sql += " AND A.CARD_ACCT_IDX = C.CARD_ACCT_IDX(+) ";
		sL_Sql += " AND C.CARD_NO = E.CARD_NO(+) ";
		sL_Sql += " AND E.PROCESS_STATUS(+) = 0 ";
		sL_Sql += " GROUP BY A.CARD_ACCT_IDX ,A.ACCT_PARENT_INDEX, A.CARD_ACCT_ID,  A.CARD_CORP_ID ";
		*/
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			/*
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_CardAcctId);
			ps.setString(2, sP_CardCorpId);

			ps.setString(3, sP_CardCorpIdSeq);
			ps.setString(4, sP_AcctClass);
			ps.setString(5, sP_AcctNo);
			ps.setString(6, sP_CardAcctId);
			ps.setString(7, sP_CardCorpId);
			ps.setString(8, sP_CardCorpIdSeq);
			ps.setString(9, sP_AcctClass);			
			ps.setString(10, sP_AcctNo);
			L_ResultSet = ps.executeQuery();
			*/
			/*
			G_Ps4CardAcctIndex4.setString(1, sP_CardAcctId);
			G_Ps4CardAcctIndex4.setString(2, sP_CardCorpId);

			G_Ps4CardAcctIndex4.setString(3, sP_CardCorpIdSeq);
			G_Ps4CardAcctIndex4.setString(4, sP_AcctClass);
			G_Ps4CardAcctIndex4.setString(5, sP_AcctNo);
			G_Ps4CardAcctIndex4.setString(6, sP_CardAcctId);
			G_Ps4CardAcctIndex4.setString(7, sP_CardCorpId);
			G_Ps4CardAcctIndex4.setString(8, sP_CardCorpIdSeq);
			G_Ps4CardAcctIndex4.setString(9, sP_AcctClass);			
			G_Ps4CardAcctIndex4.setString(10, sP_AcctNo);
			*/
			
			G_Ps4CardAcctIndex4.setString(1, sP_CardAcctId);
			G_Ps4CardAcctIndex4.setString(2, sP_CardCorpId);
			
			G_Ps4CardAcctIndex4.setString(3, sP_CardCorpIdSeq);
			
			
			
			G_Ps4CardAcctIndex4.setString(4, sP_AcctClass);			
			G_Ps4CardAcctIndex4.setString(5, sP_AcctNo);
			L_ResultSet = G_Ps4CardAcctIndex4.executeQuery();

			if (null != L_ResultSet ) {
				while (L_ResultSet.next()) {
					if (null == L_CardAcctIndexVo) 
						L_CardAcctIndexVo = new CardAcctIndexVo();
				
			
				
					L_CardAcctIndexVo.setAcctParentIdx(L_ResultSet.getInt("AcctParentIndex"));
					L_CardAcctIndexVo.setCardAcctIdx(L_ResultSet.getInt("MinCardAcctIdx"));
					L_CardAcctIndexVo.setCardCorpId(L_ResultSet.getString("CARDCORPID"));
					L_CardAcctIndexVo.setCardAcctId(sP_CardAcctId);
				
				
				
					/*  Howard: 從別的 Data100Vo 中取得

					L_CardAcctIndexVo.setTransType1(L_ResultSet.getString("TRANSTYPE1"));
					L_CardAcctIndexVo.setTransType2(L_ResultSet.getString("TRANSTYPE2"));
					L_CardAcctIndexVo.setDebitFlag(L_ResultSet.getString("DEBITFLAG"));
					L_CardAcctIndexVo.setPSeqNo(L_ResultSet.getString("PSEQNO"));
					L_CardAcctIndexVo.setIdPSeqNo(L_ResultSet.getString("IDPSEQNO"));
					 */
					break;
				}
			
				closeResource(L_ResultSet);
			}
			//releaseConnection(L_ResultSet);
			
		} catch (Exception e) {
			// TODO: handle exception
			L_CardAcctIndexVo=null;
			throw e;
		}
		
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		return L_CardAcctIndexVo;
		
	}
	/*
	public static CardAcctIndexVo getCardAcctIndex2(String sP_CardAcctId, String sP_CardCorpId, String sP_CardCorpIdSeq, String sP_AcctClass, String sP_AcctNo) {
		//proc is GET_CARD_ACCT_IDX(2) for AuthBatch_100
		
		ResultSet L_ResultSet = null;
		CardAcctIndexVo L_CardAcctIndexVo = null;
		
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		
		String sL_Sql = "SELECT CARD_ACCT_IDX ,"
                       +"CARD_ACCT_ID,  CARD_CORP_ID"
                       +"FROM  CCA_CARD_ACCT_INDEX "
                       +"WHERE CARD_ACCT_ID = ? "
                       +"AND   CARD_CORP_ID = ? "
                       +"AND   NVL(CARD_CORP_ID_SEQ,'000') = NVL( ? ,'000') "
                       +"AND   ECS_ACCT_CLASS = ? "
                       +" AND   CARD_ACCT_IDX = (SELECT MIN(CARD_ACCT_IDX)" 
                       +"          FROM CCA_CARD_ACCT_INDEX"
                       +"         WHERE CARD_ACCT_ID = ? "
                       +"         AND   CARD_CORP_ID = ? "
                       +"         AND   NVL(CARD_CORP_ID_SEQ,'000') = NVL( ? ,'000')"
                       +"         AND   ECS_ACCT_CLASS = ? ); ";
		
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_CardAcctId);//:h_card_acct_id
			ps.setString(2, sP_CardCorpId);//:h_card_corp_id
			ps.setString(3, sP_CardCorpIdSeq);//h_card_corp_seq
			
			ps.setString(4, sP_AcctClass);//h_acct_class
			//ps.setString("p4", sP_AcctNo);
			ps.setString(5, sP_CardAcctId);//:h_card_acct_id
			ps.setString(6, sP_CardCorpId);//h_card_corp_id
			ps.setString(7, sP_AcctNo);//h_card_corp_seq
			ps.setString(8, sP_AcctClass);//h_acct_class
			
			L_ResultSet = ps.executeQuery();
			while (L_ResultSet.next()) {
				if (null == L_CardAcctIndexVo) 
					L_CardAcctIndexVo = new CardAcctIndexVo();
				
				
				L_CardAcctIndexVo.setAcctParentIdx(L_ResultSet.getInt("ACCTPARENTIDX"));
				L_CardAcctIndexVo.setCardAcctId(L_ResultSet.getString("CARDACCTID"));
				L_CardAcctIndexVo.setCardAcctIdx(L_ResultSet.getInt("CARDACCTIDX"));
				L_CardAcctIndexVo.setCardCorpId(L_ResultSet.getString("CARDCORPID"));
				L_CardAcctIndexVo.setTransType1(L_ResultSet.getString("TRANSTYPE1"));
				L_CardAcctIndexVo.setTransType2(L_ResultSet.getString("TRANSTYPE2"));
				L_CardAcctIndexVo.setDebitFlag(L_ResultSet.getString("DEBITFLAG"));
				L_CardAcctIndexVo.setPSeqNo(L_ResultSet.getString("PSEQNO"));
				L_CardAcctIndexVo.setIdPSeqNo(L_ResultSet.getString("IDPSEQNO"));
				
				
			}
			releaseConnection(L_ResultSet);
			
		} catch (Exception e) {
			// TODO: handle exception
			L_CardAcctIndexVo=null;
		}
		
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		return L_CardAcctIndexVo;
		
	}
	*/
	
	public static int getAcctParentIndex(int nP_CardAcctIdx) {
		
		int nL_Result = 0;

		
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		
		String sL_Sql = "SELECT NVL(ACCT_PARENT_INDEX,0) as ACCTPARENTIDX "
                       +"FROM  CCA_CARD_ACCT_INDEX "
                       +"WHERE CARD_ACCT_IDX= ? ";

		
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setInt(1, nP_CardAcctIdx);
			ResultSet L_ResultSet = ps.executeQuery();
			while (L_ResultSet.next()) {
		
				nL_Result = L_ResultSet.getInt("ACCTPARENTIDX");
				
				
			}
			releaseConnection(L_ResultSet);
			
		} catch (Exception e) {
			// TODO: handle exception
			nL_Result=0;
		}
		
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		return nL_Result;
		
	}

	public static int getCardAcctIndex(int nP_Type,CcaBaseVo P_CcaBaseVo, String sP_TmpCardAcctId, String sP_CardAcctId, String sP_CardAcctClass,String sP_TmpCardHolderId) { 
		//proc is GET_CARD_ACCT_IDX() for AuthBatch_080
		int nL_Result = -1;
		String sL_CardCorpId="", sL_CardAcctId="", sL_Tmp="";
		String sL_CardCorpIdSeq="", sL_AcctClass="";
		boolean bL_CheckCardAcct=false;
		
		nG_CardAcctParentIdx=0;
		
		String sL_AcctNo = P_CcaBaseVo.getAcctNo();
		if (nP_Type==1) {
			sL_CardAcctId = sP_TmpCardAcctId;
			sL_Tmp = P_CcaBaseVo.getCardHolderId().substring(10,11);
			sL_CardCorpIdSeq = sL_Tmp;
			
			sL_Tmp = sP_CardAcctId.substring(0,10);
			
			if ("A".equals(sP_CardAcctClass)) {
				sL_CardCorpId = sL_Tmp;
			}
			else {
				sL_CardCorpId = sP_TmpCardHolderId;
			}
			sL_AcctClass = P_CcaBaseVo.getAccountType();
		}
		else if (nP_Type==2) {
			sL_AcctClass = P_CcaBaseVo.getAccountType();
			sL_Tmp = P_CcaBaseVo.getCardAcctId().substring(0,8);
			sL_CardCorpIdSeq = sL_Tmp;
			sL_CardAcctId = "000";
			sL_CardCorpIdSeq="";
		}
		
		try {
			ResultSet L_ResultSet = null;
			String sL_Sql="";
			if ("A".equals(sP_CardAcctClass)) {
				sL_Sql = "select CARD_ACCT_IDX as CcaCardAcctIdx_CardAcctIdx "
						//+ "from CCA_CARD_ACCT_INDEX where CARD_ACCT_ID= ? and ECS_ACCT_CLASS= ?  and NVL(ACCT_NO,'00000000000') = nvl( ? ,'00000000000') "; //h_a_acct_no == CCS_BASE.ACCT_NO
						+ "from CCA_CARD_ACCT_INDEX where CARD_ACCT_ID= ? and ACCT_TYPE= ?  and NVL(ACCT_NO,'00000000000') = nvl( ? ,'00000000000') "; //h_a_acct_no == CCS_BASE.ACCT_NO  // Howard: ECS_ACCT_CLASS change to ACCT_TYPE
				//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
				PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
				ps.setString(1, sL_CardAcctId);
				ps.setString(2, sL_AcctClass);
				ps.setString(3, sL_AcctNo);

				ps.setFetchSize(1000);//設定每次只get 1000筆
				
			
				
				L_ResultSet = ps.executeQuery();

					
			}
			else {
				sL_Sql = "select CARD_ACCT_IDX as CcaCardAcctIdx_CardAcctIdx "
						+ "from CCA_CARD_ACCT_INDEX "
						//+ "where CARD_ACCT_ID= ? and ECS_ACCT_CLASS= ? "
						+ "where CARD_ACCT_ID= ? and ACCT_TYPE= ? "// Howard: ECS_ACCT_CLASS change to ACCT_TYPE
						+ " or (CARD_CORP_ID= ? and NVL(CARD_CORP_ID_SEQ,'0') = NVL( ? ,'0') )"; 
				
				//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
				PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
				ps.setString(1, sL_CardAcctId);
				ps.setString(2, sL_AcctClass);
				ps.setString(3, sL_CardCorpId);
				ps.setString(4, sL_CardCorpIdSeq);				

				ps.setFetchSize(1000);//設定每次只get 1000筆
				
			
				
				L_ResultSet = ps.executeQuery();
				
			}
			
			while (L_ResultSet.next()) {
				nG_CardAcctIdx = L_ResultSet.getInt("CcaCardAcctIdx_CardAcctIdx");
				nG_CardAcctParentIdx = nG_CardAcctIdx;
				bL_CheckCardAcct=true;
				nL_Result=0;
			}
			L_ResultSet.close();

			
			if (bL_CheckCardAcct) {
				sL_Sql = "select count(*) as CcaCardAcctCount "
						+ "from CCA_CARD_ACCT where CARD_ACCT_IDX= ? ";
				
				//NamedParamStatement ps1 = new NamedParamStatement(Db2Connection, sL_Sql);
				PreparedStatement ps1 = getDbConnection().prepareStatement(sL_Sql);
				ps1.setInt(1, nG_CardAcctIdx);


				ps1.setFetchSize(1000);//設定每次只get 1000筆
				
			
				
				L_ResultSet = ps1.executeQuery();
				
				if (L_ResultSet.getInt("CcaCardAcctCount")>0)
					nL_Result=0;
				else
					nL_Result=-1;
				
				releaseConnection(L_ResultSet);
				/*
				L_ResultSet.close();
				Db2Connection.commit();
				*/	
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			nL_Result=-1;
		}
		
		
		return nL_Result;
	}
	
	public static int getCardAcctIndex(int nP_Type, Data080Vo P_Data080Vo, String sP_TmpCardAcctId, String sP_CardAcctId, String sP_CardAcctClass,String sP_TmpCardHolderId) { 
		//proc is GET_CARD_ACCT_IDX() for AuthBatch_080
		int nL_Result = -1;
		String sL_CardCorpId="", sL_CardAcctId="", sL_Tmp="";
		String sL_CardCorpIdSeq="", sL_AcctClass="";
		boolean bL_CheckCardAcct=false;
		
		nG_CardAcctParentIdx=0;
		
		//System.out.println("getCardAcctIndex==========>AA1");
		String sL_AcctNo = P_Data080Vo.getAcctNo();
		if (nP_Type==1) {
			sL_CardAcctId = sP_TmpCardAcctId;
			//sL_Tmp = P_CcaBaseVo.getCardHolderId().substring(10,11);
			sL_Tmp = P_Data080Vo.getCardHolderId().substring(10,11);
			
			sL_CardCorpIdSeq = sL_Tmp;
			
			sL_Tmp = sP_CardAcctId.substring(0,10);
			//System.out.println("getCardAcctIndex==========>AA2");
			if ("A".equals(sP_CardAcctClass)) {
				sL_CardCorpId = sL_Tmp;
			}
			else {
				sL_CardCorpId = sP_TmpCardHolderId;
			}
			//sL_AcctClass = P_CcaBaseVo.getAccountType();
			sL_AcctClass = P_Data080Vo.getAccountType();
		}
		else if (nP_Type==2) {
			//System.out.println("getCardAcctIndex==========>AA3");
			//sL_AcctClass = P_CcaBaseVo.getAccountType();
			sL_AcctClass = P_Data080Vo.getAccountType();
			
			//sL_Tmp = P_CcaBaseVo.getCardAcctId().substring(0,8);
			sL_Tmp = P_Data080Vo.getCardAcctId().substring(0,8);
			
			sL_CardCorpIdSeq = sL_Tmp;
			sL_CardAcctId = "000";
			sL_CardCorpIdSeq="";
		}
		
		try {
			//System.out.println("getCardAcctIndex==========>AA4");
			ResultSet L_ResultSet = null;
			String sL_Sql="";
			if ("A".equals(sP_CardAcctClass)) {
				/*
				sL_Sql = "select CARD_ACCT_IDX as CcaCardAcctIdx_CardAcctIdx "
						+ "from CCA_CARD_ACCT_INDEX where CARD_ACCT_ID= ? and ECS_ACCT_CLASS= ?  and NVL(ACCT_NO,'00000000000') = nvl( ? ,'00000000000') "; //h_a_acct_no == CCS_BASE.ACCT_NO
				//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
				PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
				ps.setString(1, sL_CardAcctId);
				ps.setString(2, sL_AcctClass);
				ps.setString(3, sL_AcctNo);

				ps.setFetchSize(1000);//設定每次只get 1000筆
				
			
				
				L_ResultSet = ps.executeQuery();

				*/
				//System.out.println("getCardAcctIndex==========>AA5");
				G_Ps4CardAcctIndex.setString(1, sL_CardAcctId);
				G_Ps4CardAcctIndex.setString(2, sL_AcctClass);
				G_Ps4CardAcctIndex.setString(3, sL_AcctNo);

				G_Ps4CardAcctIndex.setFetchSize(1000);//設定每次只get 1000筆
				
			
				
				L_ResultSet = G_Ps4CardAcctIndex.executeQuery();

				//System.out.println("getCardAcctIndex==========>AA6");
			}
			else {
				/*
				sL_Sql = "select CARD_ACCT_IDX as CcaCardAcctIdx_CardAcctIdx "
						+ "from CCA_CARD_ACCT_INDEX "
						+ "where CARD_ACCT_ID= ? and ECS_ACCT_CLASS= ? "
						+ "and CARD_CORP_ID= ? and NVL(CARD_CORP_ID_SEQ,'0') = NVL( ? ,'0')"; 
				
				//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
				PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
				ps.setString(1, sL_CardAcctId);
				ps.setString(2, sL_AcctClass);
				ps.setString(3, sL_CardCorpId);
				ps.setString(4, sL_CardCorpIdSeq);				

				ps.setFetchSize(1000);//設定每次只get 1000筆
					
				L_ResultSet = ps.executeQuery();
				*/
				//System.out.println("getCardAcctIndex==========>AA7");
				G_Ps4CardAcctIndex2.setString(1, sL_CardAcctId);
				G_Ps4CardAcctIndex2.setString(2, sL_AcctClass);
				G_Ps4CardAcctIndex2.setString(3, sL_CardCorpId);
				G_Ps4CardAcctIndex2.setString(4, sL_CardCorpIdSeq);				

				G_Ps4CardAcctIndex2.setFetchSize(1000);//設定每次只get 1000筆
					
				L_ResultSet = G_Ps4CardAcctIndex2.executeQuery();

				//System.out.println("getCardAcctIndex==========>AA8");
			}
			
			while (L_ResultSet.next()) {
				nG_CardAcctIdx = L_ResultSet.getInt("CcaCardAcctIdx_CardAcctIdx");
				nG_CardAcctParentIdx = nG_CardAcctIdx;
				bL_CheckCardAcct=true;
				nL_Result=0;
			}
			L_ResultSet.close();

			/*
			System.out.println("sL_CardAcctId=>"+ sL_CardAcctId);
			System.out.println("sL_AcctClass=>"+ sL_AcctClass);
			System.out.println("sL_CardCorpId=>"+ sL_CardCorpId);
			System.out.println("sL_CardCorpIdSeq=>"+ sL_CardCorpIdSeq);
			*/
			if (bL_CheckCardAcct) {
				
				/*
				sL_Sql = "select count(*) as CcaCardAcctCount "
						+ "from CCA_CARD_ACCT where CARD_ACCT_IDX= ? ";

				//NamedParamStatement ps1 = new NamedParamStatement(Db2Connection, sL_Sql);
				PreparedStatement ps1 = Db2Connection.prepareStatement(sL_Sql);
				ps1.setInt(1, nG_CardAcctIdx);


				ps1.setFetchSize(1000);//設定每次只get 1000筆
				
			
				
				L_ResultSet = ps1.executeQuery();
				*/
				
				G_Ps4CardAcct2.setInt(1, nG_CardAcctIdx);


				G_Ps4CardAcct2.setFetchSize(1000);//設定每次只get 1000筆
				L_ResultSet = G_Ps4CardAcct2.executeQuery();

				int nL_CcaCardAcctCount=0;
				while (L_ResultSet.next()) {
					nL_CcaCardAcctCount = L_ResultSet.getInt("CcaCardAcctCount"); 
				}
				L_ResultSet.close();
				
				//System.out.println("CcaCardAcctCount=>" + nL_CcaCardAcctCount + "---");
				
				
				if (nL_CcaCardAcctCount>0)
					nL_Result=0;
				else
					nL_Result=-1;
				
				
				
				/*
				L_ResultSet.close();
				Db2Connection.commit();
				*/	
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			
			

			System.out.println("Exception on getCardAcctIndex()=>" + e.getMessage() + "--");
			e.printStackTrace(System.out);
			nL_Result=-1;
		}
		
		
		return nL_Result;
	}

}
