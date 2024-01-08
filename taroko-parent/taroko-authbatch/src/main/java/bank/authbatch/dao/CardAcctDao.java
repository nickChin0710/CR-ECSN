package bank.authbatch.dao;



import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.ExecutionTimer;
import bank.authbatch.main.HpeUtil;
import bank.authbatch.vo.CardAcctIndexVo;
import bank.authbatch.vo.CardAcctVo;
import bank.authbatch.vo.Data100Vo;

public class CardAcctDao extends AuthBatchDbHandler{

	public CardAcctDao() throws Exception{
		// TODO Auto-generated constructor stub
	}

	public static boolean updateCardAcct(int nP_CardAcctIdx, String sP_CurDate) {
		boolean bL_Result = true;
		
		try {
			String sL_Sql = "update CCA_CARD_ACCT set NOTICE_SND_DATE= ? ,NOTICE_FLAG= ? "
					+ "where card_acct_idx= ? ";
		//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setString(1, sP_CurDate);
			ps.setString(2, "Y");
			ps.setInt(3, nP_CardAcctIdx);
		
			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	public static boolean deleteCardAcct(String sP_CardAcctIdx) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "DELETE CCA_CARD_ACCT WHERE CARD_ACCT_IDX = ? ";
			
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

	public static boolean updateCardAcctA(){
		//proc is UPDATE_CARD_ACCT_A
		
		boolean bL_Result = true;
		
//		try {
//			/*  Howard: 原  SQL 如下，但新table 已經沒有 欄位 VOCATION  and  COPT_NAME....
//			         UPDATE CARD_ACCT
//            SET VOCATION         =:h_a_position,
//                CORP_NAME        =:h_a_corp_name
//          WHERE CARD_ACCT_ID     =:h_a_card_acct_id
//            AND CARD_ACCT_ID_SEQ =:h_a_card_acct_seq
//            AND CARD_ACCT_CLASS like 'A%';
//
//			 */
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			bL_Result = false;
//		}
		
		return bL_Result;
	}
	
	public static boolean updateCardAcctB(){
		//proc is UPDATE_CARD_ACCT_B
		
		boolean bL_Result = true;
		
//		try {
//			/*
//			 Howard: 原  SQL 如下，但新table 已經沒有 欄位  COPT_NAME....
//			      UPDATE CARD_ACCT
//            SET CORP_NAME        =:h_a_corp_name
//          WHERE CARD_CORP_ID     =:h_a_card_corp_id
//            AND CARD_ACCT_CLASS not like 'A%';
//			 * */
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			bL_Result = false;
//		}
		
		return bL_Result;
	}
	public static boolean insertCardAcct(String sP_NewCardAcctIdx, ResultSet P_CcsAccountRs, String sP_CardAcctSeq, String sP_CardAcctClass, String sP_CardCorpId, String sP_CardCorpSeq, CardAcctIndexVo P_CardAcctIndexVo) {
		//proc is INSERT_CARD_ACCT();
		boolean bL_Result = true;
		
		try {
			String sL_Source = P_CcsAccountRs.getString("Status01")+ P_CcsAccountRs.getString("Status02")
								+P_CcsAccountRs.getString("Status03")+P_CcsAccountRs.getString("Status04");

			String sL_Project = P_CcsAccountRs.getString("Status11")+ P_CcsAccountRs.getString("Status12")
								+P_CcsAccountRs.getString("Status13")+P_CcsAccountRs.getString("Status14");

			String sL_Sql = "INSERT INTO CCA_CARD_ACCT(CARD_ACCT_IDX,"
                    			+ "TOT_AMT_CONSUME,TOT_AMT_PRECASH,"
                    			+ "ORGAN_ID,"
                    			+ "NOCANCEL_CREDIT_FLAG, P_SEQNO, DEBIT_FLAG, ID_P_SEQNO,CCAS_MCODE, ACNO_FLAG)"
                    			+ " VALUES(?,?,?,?,?,?,?,?,?, ? )";
			String sL_CurDate= HpeUtil.getCurDateStr("");
			String sL_CurTime=HpeUtil.getCurTimeStr();
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setString(1, sP_NewCardAcctIdx); //h_new_card_acct_idx
			//ps.setString("p2", P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_a_card_acct_id
			//ps.setString("p3", sP_CardAcctSeq); //h_a_card_acct_seq
			//ps.setString("p4", sP_CardAcctClass); //h_a_card_acct_class
			//ps.setString("p5", sP_CardCorpId); //h_a_card_corp_id
			//ps.setString("p6", sP_CardCorpSeq); //h_a_card_corp_seq
			//ps.setString("p7", P_CcsAccountRs.getString("BRANCH")); //h_a_branch
			//ps.setString("p8", P_CcsAccountRs.getString("OVER_DUE")); //h_a_over_due
			//ps.setString("p9", P_CcsAccountRs.getString("POSITION")); //h_a_position
			//ps.setString("p10", P_CcsAccountRs.getString("CORP_NAME")); //h_a_corp_name
			//ps.setString("p11", P_CcsAccountRs.getString("ACCT_ADDRESS")); //h_a_acct_address
			//ps.setString("p12", P_CcsAccountRs.getString("BILL_ADDRESS")); //h_a_bill_address
			//ps.setString("p13", P_CcsAccountRs.getString("AUTO_PAY_BANKID")); //h_a_auto_pay_bankid
			//ps.setString("p14", sL_Source); //h_source
			//ps.setString("p15", P_CcsAccountRs.getString("TRANSFER")); //h_a_transfer
			//ps.setString("p16", P_CcsAccountRs.getString("STATUS_REASON")); //h_a_status_reason
			//
			//ps.setString("p18", sL_Project); //h_project
			//ps.setString("p19", P_CcsAccountRs.getString("CARD_ACCT_LEVEL")); //h_a_card_acct_level
			//ps.setString("p20", P_CcsAccountRs.getString("CcsAccountLmtTotConsume")); //h_a_lmt_tot_consume
			//ps.setString("p21", sL_CurDate); //h_uid_date
			//ps.setString("p22", sL_CurTime); //h_update_time
			//ps.setString("p23", "ECS100"); //h_uid_name
			//ps.setString("p24", P_CcsAccountRs.getString("CARD_ACCT_SINCE")); //h_a_card_acct_since
			//ps.setString("p25", sL_CurDate); //h_uid_date
			//ps.setString("p26", sL_CurTime); //h_update_time
			//ps.setString("p27", "ECS100"); //h_uid_name
			//ps.setString("p28", P_CcsAccountRs.getString("CARD_ACCT_SINCE")); //h_a_card_acct_since
			ps.setString(2, "0"); //lh_zeros
			ps.setString(3, "0"); //lh_zeros
			ps.setString(4, P_CcsAccountRs.getString("ORGAN_ID")); //h_a_organ_id
			//ps.setString("p32", P_CcsAccountRs.getString("CcsAccountLmlTotConsumeCash")); //h_a_lmt_tot_cash
			//ps.setString("p33", P_CcsAccountRs.getString("ACCT_NO")); //h_a_acct_no
			//ps.setString("p34", P_CcsAccountRs.getString("SALE_DATE")); //h_a_sale_date
			//ps.setString("p35", P_CcsAccountRs.getString("BILL_SENDING_ZIP")); //h_a_bill_sending_zip
			//ps.setString("p36", P_CcsAccountRs.getString("AUTO_INSTALLMENT")); //h_a_auto_installment
			//ps.setString("p37", P_CcsAccountRs.getString("PD_RATING")); //h_a_pd_rating
			ps.setString(5, P_CcsAccountRs.getString("NEW_VDCHG_FLAG")); //h_a_new_vdchg_flag
			
			ps.setString(6, P_CardAcctIndexVo.getPSeqNo()); //P_SEQNO
			
			
			ps.setString(7, P_CcsAccountRs.getString("DebitFlag"));// 來哥會傳進來
			//ps.setString(7, P_CardAcctIndexVo.getDebitFlag()); //DEBIT_FLAG
			
			
			ps.setString(8, P_CcsAccountRs.getString("IdPSeqNo"));// 來哥會傳進來
			//ps.setString(8, P_CardAcctIndexVo.getIdPSeqNo()); //ID_P_SEQNO
			
			ps.setString(9, P_CcsAccountRs.getString("MCODE")); //h_a_mcode
			ps.setString(10, P_CcsAccountRs.getString("ACNO_FLAG")); 
			ps.executeUpdate();
			ps.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	public static boolean insertCardAcct(String sP_NewCardAcctIdx, Data100Vo P_Data100Vo, String sP_CardAcctSeq, String sP_CardAcctClass, String sP_CardCorpId, String sP_CardCorpSeq, CardAcctIndexVo P_CardAcctIndexVo) {
		//proc is INSERT_CARD_ACCT();
		boolean bL_Result = true;
		
		try {
			
			String sL_Source = P_Data100Vo.getStatus01()  + P_Data100Vo.getStatus02()
								+ P_Data100Vo.getStatus03() + P_Data100Vo.getStatus04();

			String sL_Project = P_Data100Vo.getStatus11()  + P_Data100Vo.getStatus12()
								+ P_Data100Vo.getStatus13() + P_Data100Vo.getStatus14();

			String sL_CurDate= HpeUtil.getCurDateStr("");
			String sL_CurTime=HpeUtil.getCurTimeStr();

			//Howard: 為測試，改為 insert to CCA_CARD_ACCT_H2
			
			System.out.println("sP_NewCardAcctIdx ==>" + sP_NewCardAcctIdx + "====");
			System.out.println("P_Data100Vo.getOrganId() ==>" + P_Data100Vo.getOrganId() + "====");
			System.out.println("P_Data100Vo.getNewVdchgFlag() ==>" + P_Data100Vo.getNewVdchgFlag() + "====");
			System.out.println("P_Data100Vo.getPSeqNo() ==>" + P_Data100Vo.getPSeqNo() + "====");
			System.out.println("P_Data100Vo.getDebitFlag() ==>" + P_Data100Vo.getDebitFlag() + "====");
			System.out.println("P_Data100Vo.getIdPSeqNo() ==>" + P_Data100Vo.getIdPSeqNo() + "====");
			System.out.println("P_Data100Vo.getMCode() ==>" + P_Data100Vo.getMCode() + "====");
			System.out.println("P_Data100Vo.getAcnoFlag() ==>" + P_Data100Vo.getAcnoFlag() + "====");
			System.out.println("HpeUtil.getCurTimestamp() ==>" + HpeUtil.getCurTimestamp() + "====");
			
			System.out.println("P_Data100Vo.getAcnoPSeqNo() ==>" + P_Data100Vo.getAcnoPSeqNo() + "====");
			
			G_Ps4CardAcct3.setInt(1, Integer.parseInt(sP_NewCardAcctIdx)); //h_new_card_acct_idx
			//G_Ps4CardAcct3.setInt(1, 7777777); //for test
			
			G_Ps4CardAcct3.setInt(2, 0); //lh_zeros
			G_Ps4CardAcct3.setInt(3, 0); //lh_zeros
			G_Ps4CardAcct3.setString(4,  P_Data100Vo.getOrganId());// P_CcsAccountRs.getString("ORGAN_ID")); //h_a_organ_id
			G_Ps4CardAcct3.setString(5, P_Data100Vo.getNewVdchgFlag());// P_CcsAccountRs.getString("NEW_VDCHG_FLAG")); //h_a_new_vdchg_flag

			//G_Ps4CardAcct3.setString(6, P_CardAcctIndexVo.getPSeqNo()); //P_SEQNO
			G_Ps4CardAcct3.setString(6, P_Data100Vo.getPSeqNo()); //P_SEQNO
			
			
			G_Ps4CardAcct3.setString(7, P_Data100Vo.getDebitFlag() ); //DEBIT_FLAG
			
			G_Ps4CardAcct3.setString(8, P_Data100Vo.getIdPSeqNo()); //ID_P_SEQNO
			G_Ps4CardAcct3.setString(9, P_Data100Vo.getMCode());// P_CcsAccountRs.getString("MCODE")); //h_a_mcode
			G_Ps4CardAcct3.setString(10,P_Data100Vo.getAcnoFlag());//  P_CcsAccountRs.getString("ACNO_FLAG")); 
			G_Ps4CardAcct3.setTimestamp(11,HpeUtil.getCurTimestamp());//  MOD_TIME
            G_Ps4CardAcct3.setString(12,    "ECS100");//  MOD_PGM
			G_Ps4CardAcct3.setString(13, 	P_Data100Vo.getAcnoPSeqNo() );
            G_Ps4CardAcct3.setString(14,    P_Data100Vo.getAccountType() );
            G_Ps4CardAcct3.setString(15,    P_Data100Vo.getCorpPSeqN0() );
			G_Ps4CardAcct3.executeUpdate();



			/*
			String sL_Sql = "INSERT INTO CCA_CARD_ACCT(CARD_ACCT_IDX,"
                    			+ "TOT_AMT_CONSUME,TOT_AMT_PRECASH,"
                    			+ "ORGAN_ID,"
                    			+ "NOCANCEL_CREDIT_FLAG, P_SEQNO, DEBIT_FLAG, ID_P_SEQNO,CCAS_MCODE, ACNO_FLAG)"
                    			+ " VALUES(?,?,?,?,?,?,?,?,?, ? )";
			String sL_CurDate= HpeUtil.getCurDateStr("");
			String sL_CurTime=HpeUtil.getCurTimeStr();
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setString(1, sP_NewCardAcctIdx); //h_new_card_acct_idx
			//ps.setString("p2", P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_a_card_acct_id
			//ps.setString("p3", sP_CardAcctSeq); //h_a_card_acct_seq
			//ps.setString("p4", sP_CardAcctClass); //h_a_card_acct_class
			//ps.setString("p5", sP_CardCorpId); //h_a_card_corp_id
			//ps.setString("p6", sP_CardCorpSeq); //h_a_card_corp_seq
			//ps.setString("p7", P_CcsAccountRs.getString("BRANCH")); //h_a_branch
			//ps.setString("p8", P_CcsAccountRs.getString("OVER_DUE")); //h_a_over_due
			//ps.setString("p9", P_CcsAccountRs.getString("POSITION")); //h_a_position
			//ps.setString("p10", P_CcsAccountRs.getString("CORP_NAME")); //h_a_corp_name
			//ps.setString("p11", P_CcsAccountRs.getString("ACCT_ADDRESS")); //h_a_acct_address
			//ps.setString("p12", P_CcsAccountRs.getString("BILL_ADDRESS")); //h_a_bill_address
			//ps.setString("p13", P_CcsAccountRs.getString("AUTO_PAY_BANKID")); //h_a_auto_pay_bankid
			//ps.setString("p14", sL_Source); //h_source
			//ps.setString("p15", P_CcsAccountRs.getString("TRANSFER")); //h_a_transfer
			//ps.setString("p16", P_CcsAccountRs.getString("STATUS_REASON")); //h_a_status_reason
			//
			//ps.setString("p18", sL_Project); //h_project
			//ps.setString("p19", P_CcsAccountRs.getString("CARD_ACCT_LEVEL")); //h_a_card_acct_level
			//ps.setString("p20", P_CcsAccountRs.getString("CcsAccountLmtTotConsume")); //h_a_lmt_tot_consume
			//ps.setString("p21", sL_CurDate); //h_uid_date
			//ps.setString("p22", sL_CurTime); //h_update_time
			//ps.setString("p23", "ECS100"); //h_uid_name
			//ps.setString("p24", P_CcsAccountRs.getString("CARD_ACCT_SINCE")); //h_a_card_acct_since
			//ps.setString("p25", sL_CurDate); //h_uid_date
			//ps.setString("p26", sL_CurTime); //h_update_time
			//ps.setString("p27", "ECS100"); //h_uid_name
			//ps.setString("p28", P_CcsAccountRs.getString("CARD_ACCT_SINCE")); //h_a_card_acct_since
			ps.setString(2, "0"); //lh_zeros
			ps.setString(3, "0"); //lh_zeros
			ps.setString(4, P_CcsAccountRs.getString("ORGAN_ID")); //h_a_organ_id
			//ps.setString("p32", P_CcsAccountRs.getString("CcsAccountLmlTotConsumeCash")); //h_a_lmt_tot_cash
			//ps.setString("p33", P_CcsAccountRs.getString("ACCT_NO")); //h_a_acct_no
			//ps.setString("p34", P_CcsAccountRs.getString("SALE_DATE")); //h_a_sale_date
			//ps.setString("p35", P_CcsAccountRs.getString("BILL_SENDING_ZIP")); //h_a_bill_sending_zip
			//ps.setString("p36", P_CcsAccountRs.getString("AUTO_INSTALLMENT")); //h_a_auto_installment
			//ps.setString("p37", P_CcsAccountRs.getString("PD_RATING")); //h_a_pd_rating
			ps.setString(5, P_CcsAccountRs.getString("NEW_VDCHG_FLAG")); //h_a_new_vdchg_flag
			
			ps.setString(6, P_CardAcctIndexVo.getPSeqNo()); //P_SEQNO
			ps.setString(7, P_CardAcctIndexVo.getDebitFlag()); //DEBIT_FLAG
			ps.setString(8, P_CardAcctIndexVo.getIdPSeqNo()); //ID_P_SEQNO
			ps.setString(9, P_CcsAccountRs.getString("MCODE")); //h_a_mcode
			ps.setString(10, P_CcsAccountRs.getString("ACNO_FLAG")); 
			ps.executeUpdate();
			ps.close();
			*/
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
			System.out.println("Exception on insertCardAcct()...");
			
			e.printStackTrace(System.out);
		}
		
		return bL_Result;
	}

	
	public static boolean updateCardAcct12(CardAcctVo P_CardAcctVo, int nP_Data100VoLineOfCreditAmt, int nP_ActAcnoLineOfCreditAmt) {
		//proc is update_card_acct_12()
		
		boolean bL_Result = true;
		
		try {
			
			//String h_temp_tot_amt_month = P_CardAcctVo.getTotAmtMonth();
			int nL_TotAmtMonth = Integer.parseInt(P_CardAcctVo.getTotAmtMonth());
			
			
			
			
			//String h_temp_adj_inst_pct = P_CardAcctVo.getAdjInstPct();
			double dL_AdjInstPct = Double.parseDouble(P_CardAcctVo.getAdjInstPct());
			
			int nL_CardAcctIdx = P_CardAcctVo.getCardAcctIdx();
			
			
			G_Ps4CardAcct4.setInt(1, nL_TotAmtMonth);
			G_Ps4CardAcct4.setInt(2, nL_TotAmtMonth);
			G_Ps4CardAcct4.setInt(3, nL_TotAmtMonth);
			G_Ps4CardAcct4.setInt(4, nP_ActAcnoLineOfCreditAmt);
			G_Ps4CardAcct4.setInt(5, nL_TotAmtMonth);
			G_Ps4CardAcct4.setInt(6, nP_Data100VoLineOfCreditAmt);
			G_Ps4CardAcct4.setInt(7, nP_ActAcnoLineOfCreditAmt);
			G_Ps4CardAcct4.setInt(8, nL_TotAmtMonth);
			G_Ps4CardAcct4.setInt(9, nP_Data100VoLineOfCreditAmt);
			G_Ps4CardAcct4.setDouble(10, dL_AdjInstPct);
			G_Ps4CardAcct4.setDouble(11, dL_AdjInstPct);
			G_Ps4CardAcct4.setDouble(12, dL_AdjInstPct);
			G_Ps4CardAcct4.setInt(13, nP_ActAcnoLineOfCreditAmt);
			G_Ps4CardAcct4.setDouble(14, dL_AdjInstPct);
			G_Ps4CardAcct4.setInt(15, nP_Data100VoLineOfCreditAmt);
			G_Ps4CardAcct4.setInt(16, nP_ActAcnoLineOfCreditAmt);
			G_Ps4CardAcct4.setDouble(17, dL_AdjInstPct);
			G_Ps4CardAcct4.setInt(18, nP_Data100VoLineOfCreditAmt);
			G_Ps4CardAcct4.setInt(19, nL_CardAcctIdx);
			
			G_Ps4CardAcct4.executeQuery();
			
			/* Howard : 2018.07.12 LmtTotConsume 搬到ACT_ACNO中， 所以要改寫取值的寫法
			String h_temp_lmt_tot_consume = P_CardAcctVo.getLmtTotConsume();
			*/
			
			
			/*
			String sL_Sql = "UPDATE CCA_CARD_ACCT set tot_amt_month = decode(" + ? + ",0," + ? + ",100," + ? + ","
            		+ "decode(sign(ceil((" + ? + "*" + ? + ")/" + ? + ")-100),"
                    + "-1,100,ceil((" + ? + "*" + ? + ")/" + ? + "))),"
                    + "adj_inst_pct = decode(" + ? + ",0," + ? + ",100," + ? + ","
                    + "decode(sign(ceil((" + ?  + "*" + ? + ")/" + ? + ")-100),"
                   	+ "-1,100,ceil((" + ? + "*" + ? + ")/" + ? + ")))";
			


h_temp_tot_amt_month == AA => 1,2,3,5,8 == nL_TotAmtMonth
h_temp_lmt_tot_consume == BB=>4,7,13,16  ==  nP_ActAcnoLineOfCreditAmt 

h_temp_adj_inst_pct =CC=> 10,11,12,14,17 ==dL_AdjInstPct

h_a_lmt_tot_consume==DD=>6,9,15,18 == nP_Data100VoLineOfCreditAmt

 
			 * */
			/*
			String sL_Sql = "UPDATE CCA_CARD_ACCT set tot_amt_month = decode(" + h_temp_tot_amt_month + ",0," + h_temp_tot_amt_month + ",100," + h_temp_tot_amt_month + ","
            		+ "decode(sign(ceil((" + h_temp_lmt_tot_consume + "*" + h_temp_tot_amt_month + ")/" + h_a_lmt_tot_consume + ")-100),"
                    + "-1,100,ceil((" + h_temp_lmt_tot_consume + "*" + h_temp_tot_amt_month + ")/" + h_a_lmt_tot_consume + "))),"
                    + "adj_inst_pct = decode(" + h_temp_adj_inst_pct + ",0," + h_temp_adj_inst_pct + ",100," + h_temp_adj_inst_pct + ","
                    + "decode(sign(ceil((" + h_temp_lmt_tot_consume  + "*" + h_temp_adj_inst_pct + ")/" + h_a_lmt_tot_consume + ")-100),"
                   	+ "-1,100,ceil((" + h_temp_lmt_tot_consume + "*" + h_temp_adj_inst_pct + ")/" + h_a_lmt_tot_consume + ")))";
			
			sL_Sql += " WHERE CARD_ACCT_IDX  = ? " ;
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setInt(1, h_card_acct_idx); 
			
			ps.executeUpdate();
			
			ps.close();
			*/
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
	
	public static boolean updateCardAcct16(int nP_CardAcctIdx, int nP_TotalAmtMonth, int nP_AdjInstPct) {
		//proc is update_card_acct_16
		
		boolean bL_Result = true;
		
		try {
			
			String sL_Sql = "UPDATE CCA_CARD_ACCT set tot_amt_month = ? , adj_inst_pct = ? ";

			sL_Sql += " WHERE CARD_ACCT_IDX  = ? " ;
			
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
			ps.setInt(1, nP_TotalAmtMonth);
			ps.setInt(2, nP_AdjInstPct);
			ps.setInt(3, nP_CardAcctIdx); 
			
			ps.executeUpdate();
			
			ps.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	public static boolean updateCardAcctBlockData(int nP_CardAcctIdx, 
			String sP_BlockCode1,String sP_BlockCode2,String sP_BlockCode3,
			String sP_BlockCode4,String sP_BlockCode5, String sP_CaStatus,
			String sP_SpecRemark, String sP_SpecStatus, String sP_SpecDate, 
			String sP_SpecDelDate, String sP_PgmId) throws Exception {
		boolean bL_Result = true;
		String sL_CurDate=HpeUtil.getCurDateStr("");
		try {
			String sL_Sql="UPDATE CCA_CARD_ACCT SET BLOCK_REASON1= ? ,"
						+"BLOCK_REASON2= ? , BLOCK_REASON3= ? ,"
						+"BLOCK_REASON4= ? , BLOCK_REASON5= ? ,"
						+"BLOCK_STATUS= ? , SPEC_REMARK= ? ,"
						+"SPEC_STATUS=:p8,SPEC_DATE= ? ,"
						+"SPEC_DEL_DATE= ? ,MOD_USER= ? ,"
						+"MOD_TIME= ? ,MOD_PGM= ?  "
						+"where CARD_ACCT_IDX= ?  ";

	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
	         ps.setString(1, sP_BlockCode1);
	         ps.setString(2, sP_BlockCode2); 
	         ps.setString(3, sP_BlockCode3); 
	         ps.setString(4, sP_BlockCode4); 
	         ps.setString(5, sP_BlockCode5); 
	         
	         ps.setString(6, sP_CaStatus);
	         ps.setString(7, sP_SpecRemark);
	         ps.setString(8, sP_SpecStatus);
	         ps.setString(9, sP_SpecDate);
	         ps.setString(10, sP_SpecDelDate);
	         
	         ps.setString(11, sP_PgmId);
	         ps.setString(12, sL_CurDate);
	         ps.setString(13, sP_PgmId);
	         ps.setInt(14, nP_CardAcctIdx);

	         ps.executeUpdate();
	         ps.close();

			/*
			                    //Howard: java is updateCardAcctBlockData()
                     SET STATUS_1=:UP_BLOCK_CODE_1,
			 STATUS_2=:UP_BLOCK_CODE_2,
			 STATUS_3=:UP_BLOCK_CODE_3,
			 STATUS_4=:UP_BLOCK_CODE_4,
			 STATUS_5=:UP_BLOCK_CODE_5,
                         STATUS=:CA_STATUS, =>p6
			 SPEC_REMARK=:UP_SPEC_REMARK, =>p7
                         SPEC_STATUS=:UP_SPEC_STATUS,
			 SPEC_DATE=:DB_PROC_DATE, =>p9
			 SPEC_DEL_DATE=:UP_SPEC_DEL_DATE,
			 SPEC_USER_ID=decode(:DB_CONTRACT,null,'ECS004',:DB_CONTRACT),
                         UPDATE_DATE=:DB_PROC_DATE,
                         UPDATE_TIME=:DB_PROC_TIME,
                         UPDATE_UID=:DB_USERID
                   WHERE CARD_ACCT_IDX=:CAI_CARD_ACCT_IDX;

			 * */
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	public static boolean updateCardAcct(CardAcctVo P_CardAcctVo, ResultSet P_CcsAccountRs) {
		//proc is UPDATE_CARD_ACCT()
		//UPDATE_CARD_ACCT (void) //Howard: 本function 要update 的欄位大都已經不在 CARD_ACCT/CCA_CARD_ACCT 中了...所以要如何update，需再討論
		boolean bL_Result = true;
		
		try {
			
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	public static boolean updateCardAcct(CardAcctVo P_CardAcctVo, Data100Vo P_Data100Vo, int nP_CardAcctIdx) {
		//proc is UPDATE_CARD_ACCT()
		//UPDATE_CARD_ACCT (void) //Howard: 本function 要update 的欄位大都已經不在 CARD_ACCT/CCA_CARD_ACCT 中了...所以要如何update，需再討論
		boolean bL_Result = true;
		
		try {
			
			
			G_Ps4CardAcct5.setString(1, P_Data100Vo.getOrganId()); 
			G_Ps4CardAcct5.setString(2, P_Data100Vo.getNewVdchgFlag()  );
			G_Ps4CardAcct5.setString(3, P_Data100Vo.getMCode()  );
			G_Ps4CardAcct5.setTimestamp(4, HpeUtil.getCurTimestamp()); 
			G_Ps4CardAcct5.setString(5, "ECS100");
			G_Ps4CardAcct5.setInt(6, nP_CardAcctIdx);

			G_Ps4CardAcct5.executeUpdate();



					
			/*
			          UPDATE CARD_ACCT
            SET BUNISSES       =:h_a_branch,  //風險行碼
                POSITION       =:h_a_over_due, //催呆戶註記
                VOCATION       =:h_a_position, //職業職級
                CORP_NAME      =:h_a_corp_name,
                BILL_ADDRESS   =:h_a_bill_address,
                ACCT_ADDRESS   =:h_a_acct_address,
                AUTO_PAY_BANKID=:h_a_auto_pay_bankid,
                SOURCE         =:h_source,
                TRANSFER       =:h_a_transfer,
                STATUS_REASON  =:h_a_status_reason,
                MCODE_NOW      =:h_a_mcode,
                PROJECT        =:h_project,
                RISK_LEVEL     =:h_a_card_acct_level,
                LMT_TOT_CONSUME=decode(:lmt_flag,'N',lmt_tot_consume,:h_a_lmt_tot_consume),

//                AUTH_REMARK    =:h_a_auth_remark,

                ORGAN_ID       =:h_a_organ_id,
                SALE_DATE      =:h_a_sale_date,
                BILL_ZIP_CODE  = :h_a_bill_sending_zip,
                AUTO_INSTALLMENT = :h_a_auto_installment,
                LMT_TOT_CASH   =decode(:lmt_flag,'N',lmt_tot_cash
                                                ,:h_a_lmt_tot_cash),
                PD_RATING      =:h_a_pd_rating,
                NOCANCEL_CREDIT_FLAG =:h_a_new_vdchg_flag,
                UPDATE_DATE    =:h_uid_date,
                UPDATE_TIME    =:h_update_time,
                UPDATE_UID     =:h_uid_name
          WHERE CARD_ACCT_IDX  =:h_card_acct_idx;

			 */
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	public static boolean updateCardAcct(int nP_CardAcctIdx, String sP_PgName, int nP_CaTotAmtConsume, int nP_CaTotAmtPreCash) {
		boolean bL_Result = true;
		
		try {
			/*
			String sL_Sql = "UPDATE CCA_CARD_ACCT set TOT_AMT_CONSUME= ? , TOT_AMT_PRECASH= ? , "
							+ "MOD_TIME= ? , MOD_PGM= ?  where CARD_ACCT_IDX= ?  ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
		
			ps.setInt(1, nP_CaTotAmtConsume); 
			ps.setInt(2, nP_CaTotAmtPreCash);
			ps.setTimestamp(3, HpeUtil.getCurTimestamp()); 
			ps.setString(4, sP_PgName);
			ps.setInt(5, nP_CardAcctIdx);

			ps.executeUpdate();

			ps.close();
			*/
			G_Ps4CardAcctUpdated.setInt(1, nP_CaTotAmtConsume); 
			G_Ps4CardAcctUpdated.setInt(2, nP_CaTotAmtPreCash);
			G_Ps4CardAcctUpdated.setTimestamp(3, HpeUtil.getCurTimestamp()); 
			G_Ps4CardAcctUpdated.setString(4, sP_PgName);
			G_Ps4CardAcctUpdated.setInt(5, nP_CardAcctIdx);

			G_Ps4CardAcctUpdated.executeUpdate();

			

			

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	public static boolean updateCardAcct(int nP_CardAcctIdx, String sP_PgName, int nP_CaTotAmtPreCash) {
		boolean bL_Result = true;
		
		try {
			String sL_Sql = "UPDATE CCA_CARD_ACCT set TOT_AMT_PRECASH= ? , "
							+ "MOD_TIME= ? , MOD_PGM= ? where CARD_ACCT_IDX= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
		
			
			ps.setInt(1, nP_CaTotAmtPreCash);
			ps.setTimestamp(2, HpeUtil.getCurTimestamp()); 
			ps.setString(3, sP_PgName);
			ps.setInt(4, nP_CardAcctIdx);

			ps.executeUpdate();

			ps.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	
	
    public static CardAcctVo getCardAcct(int nP_CardAcctIdx) throws Exception{
        //proc is select_card_acct_1()
        ResultSet L_ResultSet=null;
        CardAcctVo L_CardAcctVo = null;
        ExecutionTimer L_Timer = new ExecutionTimer();
        L_Timer.start();
        
        try {
            
            /*
            String sL_Sql = "select ROWID as CardAcctRowId, NVL(LMT_TOT_CONSUME,0) as CardAcctLmtTotConsume, NVL(LMT_TOT_CASH,0) as LMTTOTCASH,"
                        + "NVL(tot_amt_month,0) as totamtmonth,NVL(adj_inst_pct,0) as adjinstpct,"
                        + "NVL(ADJ_QUOTA,'N') as ADJQUOTA, NVL(ADJ_EFF_START_DATE,'00000000') as ADJEFFSTARTDATE,"
                        + " NVL(ADJ_EFF_END_DATE,'00000000') as AdjEffEndDate, ADJ_REASON, ADJ_REMARK, ADJ_AREA,ADJ_DATE,ADJ_TIME, ADJ_USER, "
                        + "CLOSE_SRV_FEE,CLOSE_LAW_FEE,CLOSE_PUNISH_FEE,CLOSE_INTEREST_FEE,"
                        + "CLOSE_CONSUME_FEE,CLOSE_PRECASH,OPEN_PUNISH_FEE,OPEN_SRV_FEE,"
                        + "OPEN_LAW_FEE,OPEN_INTEREST_FEE,OPEN_CONSUME_FEE,OPEN_PRECASH, "
                        + "NVL(TOT_AMT_CONSUME,0)  as CardAcctTotAmtConsume, NVL(TOT_AMT_PRECASH,0) as CardAcctTotAmtPreCash,  "
                        + "P_SEQNO as CardAcctPSeqNo, DEBIT_FLAG as CardAcctDebitFlag, ID_P_SEQNO as CardAcctIdPSeqNo, "
                        + "BLOCK_REASON1,BLOCK_REASON2,BLOCK_REASON3,BLOCK_REASON4,BLOCK_REASON5 ";
        
            sL_Sql += " from CCA_CARD_ACCT where CARD_ACCT_IDX= ? ";
            PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
            ps.setInt(1, nP_CardAcctIdx);
            
            L_ResultSet = ps.executeQuery();
            */
            G_Ps4CardAcct6.setInt(1, nP_CardAcctIdx);
            L_ResultSet = G_Ps4CardAcct6.executeQuery();
            
            while (L_ResultSet.next()) {
                if (null == L_CardAcctVo) 
                    L_CardAcctVo = new CardAcctVo();
                
                //down, 只會有一筆，所以這樣寫code...
                L_CardAcctVo.setAdjEffEndDate(L_ResultSet.getString("AdjEffEndDate"));
                L_CardAcctVo.setAdjEffStartDate(L_ResultSet.getString("ADJEFFSTARTDATE"));
                L_CardAcctVo.setAdjInstPct(L_ResultSet.getString("adjinstpct"));
                L_CardAcctVo.setAdjQuota(L_ResultSet.getString("ADJQUOTA"));
                
                /* Howard : 2018.07.12 LmtTotConsume 搬到ACT_ACNO中， 所以要改寫取值的寫法 */
                //L_CardAcctVo.setLmtTotCash(L_ResultSet.getString("LMTTOTCASH"));
                //L_CardAcctVo.setLmtTotConsume(L_ResultSet.getString("CardAcctLmtTotConsume"));
                
                
                L_CardAcctVo.setTotAmtMonth(L_ResultSet.getString("totamtmonth"));
                L_CardAcctVo.setCardAcctIdx(L_ResultSet.getInt("CARD_ACCT_IDX"));
                L_CardAcctVo.setAdjReason(L_ResultSet.getString("ADJ_REASON"));
                L_CardAcctVo.setAdjRemark(L_ResultSet.getString("ADJ_REMARK"));
                L_CardAcctVo.setAdjArea(L_ResultSet.getString("ADJ_AREA"));
                L_CardAcctVo.setAdjDate(L_ResultSet.getString("ADJ_DATE"));
                L_CardAcctVo.setAdjTime(L_ResultSet.getString("ADJ_TIME"));
                L_CardAcctVo.setAdjUser(L_ResultSet.getString("ADJ_USER"));
                /*
                L_CardAcctVo.setCloseSrvFee(L_ResultSet.getString("CLOSE_SRV_FEE"));
                L_CardAcctVo.setCloseLawFee(L_ResultSet.getString("CLOSE_LAW_FEE"));
                L_CardAcctVo.setClosePunishFee(L_ResultSet.getString("CLOSE_PUNISH_FEE"));
                L_CardAcctVo.setCloseInterestFee(L_ResultSet.getString("CLOSE_INTEREST_FEE"));
                L_CardAcctVo.setCloseConsumeFee(L_ResultSet.getString("CLOSE_CONSUME_FEE"));
                L_CardAcctVo.setCclosePreCash(L_ResultSet.getString("CLOSE_PRECASH"));
                L_CardAcctVo.setOpenPunishFee(L_ResultSet.getString("OPEN_PUNISH_FEE"));
                L_CardAcctVo.setOpenSrvFee(L_ResultSet.getString("OPEN_SRV_FEE"));
                L_CardAcctVo.setOpenLawFee(L_ResultSet.getString("OPEN_LAW_FEE"));
                L_CardAcctVo.setOpenInterestFee(L_ResultSet.getString("OPEN_INTEREST_FEE"));
                L_CardAcctVo.setOpenConsumeFee(L_ResultSet.getString("OPEN_CONSUME_FEE"));
                L_CardAcctVo.setOpenPreCash(L_ResultSet.getString("OPEN_PRECASH"));
                */
                L_CardAcctVo.setBlockReason1(L_ResultSet.getString("BLOCK_REASON1"));
                L_CardAcctVo.setBlockReason2(L_ResultSet.getString("BLOCK_REASON2"));
                L_CardAcctVo.setBlockReason3(L_ResultSet.getString("BLOCK_REASON3"));
                L_CardAcctVo.setBlockReason4(L_ResultSet.getString("BLOCK_REASON4"));
                L_CardAcctVo.setBlockReason5(L_ResultSet.getString("BLOCK_REASON5"));
                L_CardAcctVo.setIdPSeqNo(L_ResultSet.getString("CardAcctIdPSeqNo"));
                L_CardAcctVo.setPSeqNo(L_ResultSet.getString("CardAcctPSeqNo"));
                L_CardAcctVo.setDebitFlag(L_ResultSet.getString("CardAcctDebitFlag"));
                
                L_CardAcctVo.setTotAmtConsume(L_ResultSet.getInt("CardAcctTotAmtConsume"));
                L_CardAcctVo.setTotAmtPreCash(L_ResultSet.getInt("CardAcctTotAmtPreCash"));
                

            }
            closeResource(L_ResultSet);
            //releaseConnection(L_ResultSet);
        } catch (Exception e) {
            // TODO: handle exception
            L_CardAcctVo=null;
            throw e;
        }
        L_Timer.end();
        System.out.println("Timer : " + L_Timer.duration());
        
        return L_CardAcctVo;
    }
    
    
	public static CardAcctVo getCardAcct(String sP_AcnoPSeqno, String sP_DebtiFlag) throws Exception{
		//proc is select_card_acct_1()
		ResultSet L_ResultSet=null;
		CardAcctVo L_CardAcctVo = null;
		ExecutionTimer L_Timer = new ExecutionTimer();
		L_Timer.start();
		
		try {
			
			/*
			String sL_Sql = "select ROWID as CardAcctRowId, NVL(LMT_TOT_CONSUME,0) as CardAcctLmtTotConsume, NVL(LMT_TOT_CASH,0) as LMTTOTCASH,"
						+ "NVL(tot_amt_month,0) as totamtmonth,NVL(adj_inst_pct,0) as adjinstpct,"
						+ "NVL(ADJ_QUOTA,'N') as ADJQUOTA, NVL(ADJ_EFF_START_DATE,'00000000') as ADJEFFSTARTDATE,"
						+ " NVL(ADJ_EFF_END_DATE,'00000000') as AdjEffEndDate, ADJ_REASON, ADJ_REMARK, ADJ_AREA,ADJ_DATE,ADJ_TIME, ADJ_USER, "
						+ "CLOSE_SRV_FEE,CLOSE_LAW_FEE,CLOSE_PUNISH_FEE,CLOSE_INTEREST_FEE,"
						+ "CLOSE_CONSUME_FEE,CLOSE_PRECASH,OPEN_PUNISH_FEE,OPEN_SRV_FEE,"
						+ "OPEN_LAW_FEE,OPEN_INTEREST_FEE,OPEN_CONSUME_FEE,OPEN_PRECASH, "
						+ "NVL(TOT_AMT_CONSUME,0)  as CardAcctTotAmtConsume, NVL(TOT_AMT_PRECASH,0) as CardAcctTotAmtPreCash,  "
						+ "P_SEQNO as CardAcctPSeqNo, DEBIT_FLAG as CardAcctDebitFlag, ID_P_SEQNO as CardAcctIdPSeqNo, "
						+ "BLOCK_REASON1,BLOCK_REASON2,BLOCK_REASON3,BLOCK_REASON4,BLOCK_REASON5 ";
		
			sL_Sql += " from CCA_CARD_ACCT where CARD_ACCT_IDX= ? ";
			PreparedStatement ps = Db2Connection.prepareStatement(sL_Sql);
			ps.setInt(1, nP_CardAcctIdx);
			
			L_ResultSet = ps.executeQuery();
			*/
            G_Ps4CardAcct.setString(1, sP_AcnoPSeqno);
            G_Ps4CardAcct.setString(2, sP_DebtiFlag);
			L_ResultSet = G_Ps4CardAcct.executeQuery();
			
			while (L_ResultSet.next()) {
				if (null == L_CardAcctVo) 
					L_CardAcctVo = new CardAcctVo();
				
				//down, 只會有一筆，所以這樣寫code...
				L_CardAcctVo.setAdjEffEndDate(L_ResultSet.getString("AdjEffEndDate"));
				L_CardAcctVo.setAdjEffStartDate(L_ResultSet.getString("ADJEFFSTARTDATE"));
				L_CardAcctVo.setAdjInstPct(L_ResultSet.getString("adjinstpct"));
				L_CardAcctVo.setAdjQuota(L_ResultSet.getString("ADJQUOTA"));
				
				/* Howard : 2018.07.12 LmtTotConsume 搬到ACT_ACNO中， 所以要改寫取值的寫法 */
				//L_CardAcctVo.setLmtTotCash(L_ResultSet.getString("LMTTOTCASH"));
				//L_CardAcctVo.setLmtTotConsume(L_ResultSet.getString("CardAcctLmtTotConsume"));
				
				
				L_CardAcctVo.setTotAmtMonth(L_ResultSet.getString("totamtmonth"));
				L_CardAcctVo.setCardAcctIdx(L_ResultSet.getInt("CARD_ACCT_IDX"));
				L_CardAcctVo.setAdjReason(L_ResultSet.getString("ADJ_REASON"));
				L_CardAcctVo.setAdjRemark(L_ResultSet.getString("ADJ_REMARK"));
				L_CardAcctVo.setAdjArea(L_ResultSet.getString("ADJ_AREA"));
				L_CardAcctVo.setAdjDate(L_ResultSet.getString("ADJ_DATE"));
				L_CardAcctVo.setAdjTime(L_ResultSet.getString("ADJ_TIME"));
				L_CardAcctVo.setAdjUser(L_ResultSet.getString("ADJ_USER"));
				/*
				L_CardAcctVo.setCloseSrvFee(L_ResultSet.getString("CLOSE_SRV_FEE"));
				L_CardAcctVo.setCloseLawFee(L_ResultSet.getString("CLOSE_LAW_FEE"));
				L_CardAcctVo.setClosePunishFee(L_ResultSet.getString("CLOSE_PUNISH_FEE"));
				L_CardAcctVo.setCloseInterestFee(L_ResultSet.getString("CLOSE_INTEREST_FEE"));
				L_CardAcctVo.setCloseConsumeFee(L_ResultSet.getString("CLOSE_CONSUME_FEE"));
				L_CardAcctVo.setCclosePreCash(L_ResultSet.getString("CLOSE_PRECASH"));
				L_CardAcctVo.setOpenPunishFee(L_ResultSet.getString("OPEN_PUNISH_FEE"));
				L_CardAcctVo.setOpenSrvFee(L_ResultSet.getString("OPEN_SRV_FEE"));
				L_CardAcctVo.setOpenLawFee(L_ResultSet.getString("OPEN_LAW_FEE"));
				L_CardAcctVo.setOpenInterestFee(L_ResultSet.getString("OPEN_INTEREST_FEE"));
				L_CardAcctVo.setOpenConsumeFee(L_ResultSet.getString("OPEN_CONSUME_FEE"));
				L_CardAcctVo.setOpenPreCash(L_ResultSet.getString("OPEN_PRECASH"));
				*/
				L_CardAcctVo.setBlockReason1(L_ResultSet.getString("BLOCK_REASON1"));
				L_CardAcctVo.setBlockReason2(L_ResultSet.getString("BLOCK_REASON2"));
				L_CardAcctVo.setBlockReason3(L_ResultSet.getString("BLOCK_REASON3"));
				L_CardAcctVo.setBlockReason4(L_ResultSet.getString("BLOCK_REASON4"));
				L_CardAcctVo.setBlockReason5(L_ResultSet.getString("BLOCK_REASON5"));
				L_CardAcctVo.setIdPSeqNo(L_ResultSet.getString("CardAcctIdPSeqNo"));
				L_CardAcctVo.setPSeqNo(L_ResultSet.getString("CardAcctPSeqNo"));
				L_CardAcctVo.setDebitFlag(L_ResultSet.getString("CardAcctDebitFlag"));
				
				L_CardAcctVo.setTotAmtConsume(L_ResultSet.getInt("CardAcctTotAmtConsume"));
				L_CardAcctVo.setTotAmtPreCash(L_ResultSet.getInt("CardAcctTotAmtPreCash"));
				

			}
			closeResource(L_ResultSet);
			//releaseConnection(L_ResultSet);
		} catch (Exception e) {
			// TODO: handle exception
			L_CardAcctVo=null;
			throw e;
		}
		L_Timer.end();
		System.out.println("Timer : " + L_Timer.duration());
		
		return L_CardAcctVo;
	}
	
	// 2021/11/25 Justin ACNO.ACNO_P_SEQNO取代CARD_ACCT_IDX
//	public static String getNewCardAcctIdxSeqVal() {
//		//get sequence value
//		
//		String sL_SeqVal = "0";
//		try {
//			String sL_Sql = "SELECT ecs_CARD_ACCT_IDX.NEXTVAL as SeqVal  FROM DUAL ";
//			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
//			PreparedStatement ps = getDbConnection().prepareStatement(sL_Sql);
//			//ps.setInt(1, 1);
//			
//			
//		
//			
//			ResultSet L_ResultSet = ps.executeQuery();
//			
//			sL_SeqVal = L_ResultSet.getString("SeqVal");
//			
//			releaseConnection(L_ResultSet);
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//			sL_SeqVal = "0";
//		}
//		return sL_SeqVal;
//		
//	}
}
