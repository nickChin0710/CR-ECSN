package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;

import bank.authbatch.vo.CardAcctVo;
import bank.authbatch.vo.Data100Vo;

public class CcaConsumeDao extends AuthBatchDbHandler {

	public CcaConsumeDao() throws Exception {
		// TODO Auto-generated constructor stub
	}
	
	
	public static boolean deleteCcaConsume(String sP_CardAcctIdx) {
		boolean bL_Result = true;
		try {
			String sL_Sql = "DELETE CCA_CONSUME WHERE CARD_ACCT_IDX = ? ";
			
	         //NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
	        ps.setInt(1, Integer.parseInt(sP_CardAcctIdx));
	         
	        ps.executeUpdate();
	        ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
		
	}

	public static boolean insertCcaConsume(String sP_NewCardAcctIdx, ResultSet P_CcsAccountRs, String sP_CardAcctSeq, String sP_CardAcctClass, String sP_CardCorpId, String sP_CardCorpSeq) {
		//proc is INSERT_AUTH_CONSUME();
		boolean bL_Result = true;
		try {
			String sL_CurDate= HpeUtil.getCurDateStr("");
			String sL_CurTime=HpeUtil.getCurTimeStr();
			/*
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
		              //+"VALUES(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:p13,:p14,:p15,:p16,:p17,:p18,:p19,:p20,:p21,:p22,:p23,:p24,:p25,:p26,:p27,:p28,:p29,:p30,:p31,:p32,:p33,:p34,:p35,:p36,:p37,:p38,:p39,:p40,:p41,:p42,:p43,:p44,:p45,:p46,:p47,:p48,:p49,:p50,:p51,:p52,:p53,:p54,:p55,:p56)";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			
			ps.setString(1, sP_NewCardAcctIdx); //h_new_card_acct_idx
			//ps.setString("p2", P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_a_card_acct_id
			//ps.setString("p3", sP_CardAcctSeq); //h_a_card_acct_seq
			ps.setString(2, P_CcsAccountRs.getString("CloseWritsOff")); //h_a_close_writsoff
			ps.setString(3, P_CcsAccountRs.getString("CloseSrvFee")); //h_a_close_srv_fee
			ps.setString(4, P_CcsAccountRs.getString("CloseLawFee")); //h_a_close_law_fee
			ps.setString(5, P_CcsAccountRs.getString("ClosePunishFee")); //h_a_close_punish_fee
			ps.setString(6, P_CcsAccountRs.getString("CloseInterestFee")); //h_a_close_interest_fee
			ps.setString(7, P_CcsAccountRs.getString("CloseConsumeFee")); //h_a_close_consume_fee
			ps.setString(8, P_CcsAccountRs.getString("ClosePreCash")); //h_a_close_precash
			ps.setString(9, P_CcsAccountRs.getString("OpenPunishFee")); //h_a_open_punish_fee
			ps.setString(10, "0");
			ps.setString(11, P_CcsAccountRs.getString("OpenSrvFee"));//h_a_open_srv_fee
			ps.setString(12, P_CcsAccountRs.getString("OpenLawFee"));//h_a_open_law_fee
			ps.setString(13, P_CcsAccountRs.getString("OpenInterestFee"));//h_a_open_interest_fee
			ps.setString(14, P_CcsAccountRs.getString("OpenConsumeFee"));//h_a_open_consume_fee
			
			ps.setString(15, P_CcsAccountRs.getString("OpenPreCash"));//h_a_open_precash
			ps.setString(16, P_CcsAccountRs.getString("ArgueAmt"));//h_a_argue_amt
			ps.setString(17, P_CcsAccountRs.getString("PrePayAmt"));//h_a_pre_pay_amt
			
			ps.setString(18, P_CcsAccountRs.getString("BillLawPayAmt"));//h_a_m1_amt
			ps.setString(19, P_CcsAccountRs.getString("L1Mnth"));//h_a_lastest_1_mnth
			ps.setString(20, P_CcsAccountRs.getString("L2Mnth"));//h_a_lastest_2_mnth
			ps.setString(21, P_CcsAccountRs.getString("L3Mnth"));//h_a_lastest_3_mnth
			ps.setString(22, P_CcsAccountRs.getString("L4Mnth"));//h_a_lastest_4_mnth
			ps.setString(23, P_CcsAccountRs.getString("L5Mnth"));//h_a_lastest_5_mnth
			ps.setString(24, P_CcsAccountRs.getString("L6Mnth"));//h_a_lastest_6_mnth
			ps.setString(25, P_CcsAccountRs.getString("L7Mnth"));//h_a_lastest_7_mnth
			ps.setString(26, P_CcsAccountRs.getString("L8Mnth"));//h_a_lastest_8_mnth
			ps.setString(27, P_CcsAccountRs.getString("L9Mnth"));//h_a_lastest_9_mnth
			ps.setString(28, P_CcsAccountRs.getString("L10Mnth"));//h_a_lastest_10_mnth
			ps.setString(29, P_CcsAccountRs.getString("L11Mnth"));//h_a_lastest_11_mnth
			ps.setString(30, P_CcsAccountRs.getString("L12Mnth"));//h_a_lastest_12_mnth
			
			ps.setString(31, P_CcsAccountRs.getString("MaxConsumeAmt"));//h_a_max_consume_amt
			ps.setString(32, P_CcsAccountRs.getString("MaxConsumeDate"));//h_a_max_consume_date
			ps.setString(33, P_CcsAccountRs.getString("MaxPreCashAmt"));//h_a_max_precash_amt
			ps.setString(34, P_CcsAccountRs.getString("MaxPreCashDate"));//h_a_max_precash_date
			ps.setString(35, P_CcsAccountRs.getString("PayLastestAmt"));//h_a_pay_lastest_amt
			
			ps.setString(36, P_CcsAccountRs.getString("PayDate"));//h_a_pay_date
			ps.setString(37, P_CcsAccountRs.getString("PaySettleDate"));//h_a_pay_settle_date
			ps.setString(38, P_CcsAccountRs.getString("PaymentDueDate"));//h_a_payment_due_date
			ps.setString(39, P_CcsAccountRs.getString("TotalUnpaidAmt"));//h_a_total_unpaid_amt
			ps.setString(40, P_CcsAccountRs.getString("BillLowLimit"));//h_a_bill_low_limit
			ps.setString(41, P_CcsAccountRs.getString("BillLawPayAmt"));//h_a_bill_low_pay_amt
			ps.setString(42, P_CcsAccountRs.getString("TotLimitAmt"));//h_a_tot_limit_amt
			ps.setString(43, P_CcsAccountRs.getString("TotPreCashAmt"));//h_a_tot_precash_amt
			ps.setString(44, P_CcsAccountRs.getString("TotDue"));//h_a_tot_due
			
			
			ps.setString(45, P_CcsAccountRs.getString("Consume1"));//h_a_consume_1
			ps.setString(46, P_CcsAccountRs.getString("Consume2"));//h_a_consume_2
			ps.setString(47, P_CcsAccountRs.getString("Consume3"));//h_a_consume_3
			ps.setString(48, P_CcsAccountRs.getString("Consume4"));//h_a_consume_4
			ps.setString(49, P_CcsAccountRs.getString("Consume5"));//h_a_consume_5
			ps.setString(50, P_CcsAccountRs.getString("Consume6"));//h_a_consume_6
			ps.setString(51, sL_CurDate);//h_uid_date
			ps.setString(52, sL_CurTime);//h_update_time
			ps.setString(53, "ECS100");//h_uid_name
			ps.setString(54, sL_CurDate);//h_uid_date
			ps.setString(55, P_CcsAccountRs.getString("TotalUnpaidAmt"));//h_a_ibm_receive_amt
			ps.setString(56, P_CcsAccountRs.getString("UnpostInstFee"));//h_a_unpost_inst_fee

			ps.executeUpdate();
			ps.close();
			*/
			
			G_Ps4CcaConsume.setString(1, sP_NewCardAcctIdx); //h_new_card_acct_idx
			//ps.setString("p2", P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_a_card_acct_id
			//ps.setString("p3", sP_CardAcctSeq); //h_a_card_acct_seq
			G_Ps4CcaConsume.setString(2, P_CcsAccountRs.getString("CloseWritsOff")); //h_a_close_writsoff
			G_Ps4CcaConsume.setString(3, P_CcsAccountRs.getString("CloseSrvFee")); //h_a_close_srv_fee
			G_Ps4CcaConsume.setString(4, P_CcsAccountRs.getString("CloseLawFee")); //h_a_close_law_fee
			G_Ps4CcaConsume.setString(5, P_CcsAccountRs.getString("ClosePunishFee")); //h_a_close_punish_fee
			G_Ps4CcaConsume.setString(6, P_CcsAccountRs.getString("CloseInterestFee")); //h_a_close_interest_fee
			G_Ps4CcaConsume.setString(7, P_CcsAccountRs.getString("CloseConsumeFee")); //h_a_close_consume_fee
			G_Ps4CcaConsume.setString(8, P_CcsAccountRs.getString("ClosePreCash")); //h_a_close_precash
			G_Ps4CcaConsume.setString(9, P_CcsAccountRs.getString("OpenPunishFee")); //h_a_open_punish_fee
			G_Ps4CcaConsume.setString(10, "0");
			G_Ps4CcaConsume.setString(11, P_CcsAccountRs.getString("OpenSrvFee"));//h_a_open_srv_fee
			G_Ps4CcaConsume.setString(12, P_CcsAccountRs.getString("OpenLawFee"));//h_a_open_law_fee
			G_Ps4CcaConsume.setString(13, P_CcsAccountRs.getString("OpenInterestFee"));//h_a_open_interest_fee
			G_Ps4CcaConsume.setString(14, P_CcsAccountRs.getString("OpenConsumeFee"));//h_a_open_consume_fee
			
			G_Ps4CcaConsume.setString(15, P_CcsAccountRs.getString("OpenPreCash"));//h_a_open_precash
			G_Ps4CcaConsume.setString(16, P_CcsAccountRs.getString("ArgueAmt"));//h_a_argue_amt
			G_Ps4CcaConsume.setString(17, P_CcsAccountRs.getString("PrePayAmt"));//h_a_pre_pay_amt
			
			G_Ps4CcaConsume.setString(18, P_CcsAccountRs.getString("BillLawPayAmt"));//h_a_m1_amt
			G_Ps4CcaConsume.setString(19, P_CcsAccountRs.getString("L1Mnth"));//h_a_lastest_1_mnth
			G_Ps4CcaConsume.setString(20, P_CcsAccountRs.getString("L2Mnth"));//h_a_lastest_2_mnth
			G_Ps4CcaConsume.setString(21, P_CcsAccountRs.getString("L3Mnth"));//h_a_lastest_3_mnth
			G_Ps4CcaConsume.setString(22, P_CcsAccountRs.getString("L4Mnth"));//h_a_lastest_4_mnth
			G_Ps4CcaConsume.setString(23, P_CcsAccountRs.getString("L5Mnth"));//h_a_lastest_5_mnth
			G_Ps4CcaConsume.setString(24, P_CcsAccountRs.getString("L6Mnth"));//h_a_lastest_6_mnth
			G_Ps4CcaConsume.setString(25, P_CcsAccountRs.getString("L7Mnth"));//h_a_lastest_7_mnth
			G_Ps4CcaConsume.setString(26, P_CcsAccountRs.getString("L8Mnth"));//h_a_lastest_8_mnth
			G_Ps4CcaConsume.setString(27, P_CcsAccountRs.getString("L9Mnth"));//h_a_lastest_9_mnth
			G_Ps4CcaConsume.setString(28, P_CcsAccountRs.getString("L10Mnth"));//h_a_lastest_10_mnth
			G_Ps4CcaConsume.setString(29, P_CcsAccountRs.getString("L11Mnth"));//h_a_lastest_11_mnth
			G_Ps4CcaConsume.setString(30, P_CcsAccountRs.getString("L12Mnth"));//h_a_lastest_12_mnth
			
			G_Ps4CcaConsume.setString(31, P_CcsAccountRs.getString("MaxConsumeAmt"));//h_a_max_consume_amt
			G_Ps4CcaConsume.setString(32, P_CcsAccountRs.getString("MaxConsumeDate"));//h_a_max_consume_date
			G_Ps4CcaConsume.setString(33, P_CcsAccountRs.getString("MaxPreCashAmt"));//h_a_max_precash_amt
			G_Ps4CcaConsume.setString(34, P_CcsAccountRs.getString("MaxPreCashDate"));//h_a_max_precash_date
			G_Ps4CcaConsume.setString(35, P_CcsAccountRs.getString("PayLastestAmt"));//h_a_pay_lastest_amt
			
			G_Ps4CcaConsume.setString(36, P_CcsAccountRs.getString("PayDate"));//h_a_pay_date
			G_Ps4CcaConsume.setString(37, P_CcsAccountRs.getString("PaySettleDate"));//h_a_pay_settle_date
			G_Ps4CcaConsume.setString(38, P_CcsAccountRs.getString("PaymentDueDate"));//h_a_payment_due_date
			G_Ps4CcaConsume.setString(39, P_CcsAccountRs.getString("TotalUnpaidAmt"));//h_a_total_unpaid_amt
			G_Ps4CcaConsume.setString(40, P_CcsAccountRs.getString("BillLowLimit"));//h_a_bill_low_limit
			G_Ps4CcaConsume.setString(41, P_CcsAccountRs.getString("BillLawPayAmt"));//h_a_bill_low_pay_amt
			G_Ps4CcaConsume.setString(42, P_CcsAccountRs.getString("TotLimitAmt"));//h_a_tot_limit_amt
			G_Ps4CcaConsume.setString(43, P_CcsAccountRs.getString("TotPreCashAmt"));//h_a_tot_precash_amt
			G_Ps4CcaConsume.setString(44, P_CcsAccountRs.getString("TotDue"));//h_a_tot_due
			
			
			G_Ps4CcaConsume.setString(45, P_CcsAccountRs.getString("Consume1"));//h_a_consume_1
			G_Ps4CcaConsume.setString(46, P_CcsAccountRs.getString("Consume2"));//h_a_consume_2
			G_Ps4CcaConsume.setString(47, P_CcsAccountRs.getString("Consume3"));//h_a_consume_3
			G_Ps4CcaConsume.setString(48, P_CcsAccountRs.getString("Consume4"));//h_a_consume_4
			G_Ps4CcaConsume.setString(49, P_CcsAccountRs.getString("Consume5"));//h_a_consume_5
			G_Ps4CcaConsume.setString(50, P_CcsAccountRs.getString("Consume6"));//h_a_consume_6
			G_Ps4CcaConsume.setString(51, sL_CurDate);//h_uid_date
			G_Ps4CcaConsume.setString(52, sL_CurTime);//h_update_time
			G_Ps4CcaConsume.setString(53, "ECS100");//h_uid_name
			G_Ps4CcaConsume.setString(54, sL_CurDate);//h_uid_date
			G_Ps4CcaConsume.setString(55, P_CcsAccountRs.getString("TotalUnpaidAmt"));//h_a_ibm_receive_amt
			G_Ps4CcaConsume.setString(56, P_CcsAccountRs.getString("UnpostInstFee"));//h_a_unpost_inst_fee

			G_Ps4CcaConsume.executeUpdate();

			

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	public static boolean insertCcaConsume(String sP_NewCardAcctIdx, Data100Vo P_Data100Vo, String sP_CardAcctSeq, String sP_CardAcctClass, String sP_CardCorpId, String sP_CardCorpSeq) {
		//proc is INSERT_AUTH_CONSUME();
		boolean bL_Result = true;
		try {
			String sL_CurDate= HpeUtil.getCurDateStr("");
			String sL_CurTime=HpeUtil.getCurTimeStr();
			
			/*
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
		              +"CRT_DATE,          MOD_USER,          MOD_TIME,"
		              +"IBM_RECEIVE_AMT,"
		              +"UNPOST_INST_FEE)"
		              +"VALUES(? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? )";		              
		              //+"VALUES(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:p13,:p14,:p15,:p16,:p17,:p18,:p19,:p20,:p21,:p22,:p23,:p24,:p25,:p26,:p27,:p28,:p29,:p30,:p31,:p32,:p33,:p34,:p35,:p36,:p37,:p38,:p39,:p40,:p41,:p42,:p43,:p44,:p45,:p46,:p47,:p48,:p49,:p50,:p51,:p52,:p53,:p54,:p55,:p56)";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			
			ps.setInt(1, Integer.parseInt(sP_NewCardAcctIdx)); //h_new_card_acct_idx
			//ps.setString("p2", P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_a_card_acct_id
			//ps.setString("p3", sP_CardAcctSeq); //h_a_card_acct_seq
			ps.setInt(2, 0); //h_a_close_writsoff
			ps.setInt(3, P_Data100Vo.getCloseSrvFee()); //h_a_close_srv_fee
			ps.setInt(4, P_Data100Vo.getCloseLawFee()); //h_a_close_law_fee
			ps.setInt(5, P_Data100Vo.getClosePunishFee()); //h_a_close_punish_fee
			ps.setInt(6, P_Data100Vo.getCloseInterestFee()); //h_a_close_interest_fee
			ps.setInt(7, P_Data100Vo.getCloseConsumeFee()); //h_a_close_consume_fee
			ps.setInt(8, 0); //h_a_close_precash
			ps.setInt(9, P_Data100Vo.getOpenPunishFee()); //h_a_open_punish_fee
			ps.setInt(10, 0);
			ps.setInt(11, P_Data100Vo.getOpenSrvFee());//h_a_open_srv_fee
			ps.setInt(12, P_Data100Vo.getOpenLawFee());//h_a_open_law_fee
			ps.setInt(13, P_Data100Vo.getOpenInterestFee());//h_a_open_interest_fee
			ps.setInt(14, P_Data100Vo.getOpenConsumeFee());//h_a_open_consume_fee
			
			ps.setInt(15, 0);//h_a_open_precash
			ps.setInt(16, 0);//h_a_argue_amt
			ps.setInt(17, P_Data100Vo.getPrepayAmt());//h_a_pre_pay_amt
			
			ps.setInt(18, P_Data100Vo.getBillLawPayAmt());//h_a_m1_amt
			ps.setString(19, "");//h_a_lastest_1_mnth
			ps.setString(20, "");//h_a_lastest_2_mnth
			ps.setString(21, "");//h_a_lastest_3_mnth
			ps.setString(22, "");//h_a_lastest_4_mnth
			ps.setString(23, "");//h_a_lastest_5_mnth
			ps.setString(24, "");//h_a_lastest_6_mnth
			ps.setString(25, "");//h_a_lastest_7_mnth
			ps.setString(26, "");//h_a_lastest_8_mnth
			ps.setString(27, "");//h_a_lastest_9_mnth
			ps.setString(28, "");//h_a_lastest_10_mnth
			ps.setString(29, "");//h_a_lastest_11_mnth
			ps.setString(30, "");//h_a_lastest_12_mnth
			
			ps.setInt(31, 0);//h_a_max_consume_amt
			ps.setString(32, "");//h_a_max_consume_date
			ps.setInt(33, P_Data100Vo.getMaxAtmAmt() );//h_a_max_precash_amt
			ps.setString(34, "" );//h_a_max_precash_date
			ps.setInt(35, 0);//h_a_pay_lastest_amt
			
				ps.setString(36, "");//h_a_pay_date
				ps.setString(37, "");//h_a_pay_settle_date
				ps.setString(38, "");//h_a_payment_due_date
				ps.setInt(39, 0);//h_a_total_unpaid_amt
				ps.setInt(40, P_Data100Vo.getBillLowLimit() );//h_a_bill_low_limit
				ps.setInt(41, P_Data100Vo.getBillLawPayAmt());//h_a_bill_low_pay_amt
				ps.setInt(42, P_Data100Vo.getTot1LimitAmt());//h_a_tot_limit_amt
				ps.setInt(43, P_Data100Vo.getTot2LimitAmt());//h_a_tot_precash_amt
				ps.setInt(44, 0);//h_a_tot_due
				
				
				ps.setInt(45, P_Data100Vo.getConsume01());//h_a_consume_1
			ps.setInt(46, P_Data100Vo.getConsume02());//h_a_consume_2
			ps.setInt(47, P_Data100Vo.getConsume03());//h_a_consume_3
			ps.setInt(48, P_Data100Vo.getConsume04());//h_a_consume_4
			ps.setInt(49, P_Data100Vo.getConsume05());//h_a_consume_5
			ps.setInt(50, P_Data100Vo.getConsume06());//h_a_consume_6
			ps.setString(51, sL_CurDate);//h_uid_date
			//ps.setString(52, sL_CurTime);//h_update_time
			ps.setString(52, "ECS100");//h_uid_name
			ps.setTimestamp(53, HpeUtil.getCurTimestamp());//MOD_TIME,
			ps.setInt(54, 0);//h_a_ibm_receive_amt
			ps.setInt(55, 0);//h_a_unpost_inst_fee

			ps.executeUpdate();
			*/
			G_Ps4CcaConsumeInsert.setInt(1, Integer.parseInt(sP_NewCardAcctIdx)); //h_new_card_acct_idx
			//G_Ps4CcaConsumeInsert.setString("p2", P_CcsAccountRs.getString("CARD_ACCT_ID")); //h_a_card_acct_id
			//G_Ps4CcaConsumeInsert.setString("p3", sP_CardAcctSeq); //h_a_card_acct_seq
			G_Ps4CcaConsumeInsert.setInt(2, 0); //h_a_close_writsoff
			G_Ps4CcaConsumeInsert.setInt(3, P_Data100Vo.getCloseSrvFee()); //h_a_close_srv_fee
			G_Ps4CcaConsumeInsert.setInt(4, P_Data100Vo.getCloseLawFee()); //h_a_close_law_fee
			G_Ps4CcaConsumeInsert.setInt(5, P_Data100Vo.getClosePunishFee()); //h_a_close_punish_fee
			G_Ps4CcaConsumeInsert.setInt(6, P_Data100Vo.getCloseInterestFee()); //h_a_close_interest_fee
			G_Ps4CcaConsumeInsert.setInt(7, P_Data100Vo.getCloseConsumeFee()); //h_a_close_consume_fee
			G_Ps4CcaConsumeInsert.setInt(8, 0); //h_a_close_precash
			G_Ps4CcaConsumeInsert.setInt(9, P_Data100Vo.getOpenPunishFee()); //h_a_open_punish_fee
			G_Ps4CcaConsumeInsert.setInt(10, 0);
			G_Ps4CcaConsumeInsert.setInt(11, P_Data100Vo.getOpenSrvFee());//h_a_open_srv_fee
			G_Ps4CcaConsumeInsert.setInt(12, P_Data100Vo.getOpenLawFee());//h_a_open_law_fee
			G_Ps4CcaConsumeInsert.setInt(13, P_Data100Vo.getOpenInterestFee());//h_a_open_interest_fee
			G_Ps4CcaConsumeInsert.setInt(14, P_Data100Vo.getOpenConsumeFee());//h_a_open_consume_fee
			
			G_Ps4CcaConsumeInsert.setInt(15, 0);//h_a_open_precash
			G_Ps4CcaConsumeInsert.setInt(16, 0);//h_a_argue_amt
			G_Ps4CcaConsumeInsert.setInt(17, P_Data100Vo.getPrepayAmt());//h_a_pre_pay_amt
			
			G_Ps4CcaConsumeInsert.setInt(18, P_Data100Vo.getBillLawPayAmt());//h_a_m1_amt
			G_Ps4CcaConsumeInsert.setString(19, "");//h_a_lastest_1_mnth
			G_Ps4CcaConsumeInsert.setString(20, "");//h_a_lastest_2_mnth
			G_Ps4CcaConsumeInsert.setString(21, "");//h_a_lastest_3_mnth
			G_Ps4CcaConsumeInsert.setString(22, "");//h_a_lastest_4_mnth
			G_Ps4CcaConsumeInsert.setString(23, "");//h_a_lastest_5_mnth
			G_Ps4CcaConsumeInsert.setString(24, "");//h_a_lastest_6_mnth
			G_Ps4CcaConsumeInsert.setString(25, "");//h_a_lastest_7_mnth
			G_Ps4CcaConsumeInsert.setString(26, "");//h_a_lastest_8_mnth
			G_Ps4CcaConsumeInsert.setString(27, "");//h_a_lastest_9_mnth
			G_Ps4CcaConsumeInsert.setString(28, "");//h_a_lastest_10_mnth
			G_Ps4CcaConsumeInsert.setString(29, "");//h_a_lastest_11_mnth
			G_Ps4CcaConsumeInsert.setString(30, "");//h_a_lastest_12_mnth
			
			G_Ps4CcaConsumeInsert.setInt(31, 0);//h_a_max_consume_amt
			G_Ps4CcaConsumeInsert.setString(32, "");//h_a_max_consume_date
			G_Ps4CcaConsumeInsert.setInt(33, P_Data100Vo.getMaxAtmAmt() );//h_a_max_precash_amt
			G_Ps4CcaConsumeInsert.setString(34, "" );//h_a_max_precash_date
			G_Ps4CcaConsumeInsert.setInt(35, 0);//h_a_pay_lastest_amt
			
			G_Ps4CcaConsumeInsert.setString(36, "");//h_a_pay_date
			G_Ps4CcaConsumeInsert.setString(37, "");//h_a_pay_settle_date
			G_Ps4CcaConsumeInsert.setString(38, "");//h_a_payment_due_date
			G_Ps4CcaConsumeInsert.setInt(39, 0);//h_a_total_unpaid_amt
			G_Ps4CcaConsumeInsert.setInt(40, P_Data100Vo.getBillLowLimit() );//h_a_bill_low_limit
			G_Ps4CcaConsumeInsert.setInt(41, P_Data100Vo.getBillLawPayAmt());//h_a_bill_low_pay_amt
			G_Ps4CcaConsumeInsert.setInt(42, P_Data100Vo.getTot1LimitAmt());//h_a_tot_limit_amt
			G_Ps4CcaConsumeInsert.setInt(43, P_Data100Vo.getTot2LimitAmt());//h_a_tot_precash_amt
			G_Ps4CcaConsumeInsert.setInt(44, 0);//h_a_tot_due
				
				
			G_Ps4CcaConsumeInsert.setInt(45, P_Data100Vo.getConsume01());//h_a_consume_1
			G_Ps4CcaConsumeInsert.setInt(46, P_Data100Vo.getConsume02());//h_a_consume_2
			G_Ps4CcaConsumeInsert.setInt(47, P_Data100Vo.getConsume03());//h_a_consume_3
			G_Ps4CcaConsumeInsert.setInt(48, P_Data100Vo.getConsume04());//h_a_consume_4
			G_Ps4CcaConsumeInsert.setInt(49, P_Data100Vo.getConsume05());//h_a_consume_5
			G_Ps4CcaConsumeInsert.setInt(50, P_Data100Vo.getConsume06());//h_a_consume_6
			G_Ps4CcaConsumeInsert.setString(51, sL_CurDate);//h_uid_date
			//G_Ps4CcaConsumeInsert.setString(52, sL_CurTime);//h_update_time
			G_Ps4CcaConsumeInsert.setString(52, "ECS100");//h_uid_name
			G_Ps4CcaConsumeInsert.setTimestamp(53, HpeUtil.getCurTimestamp());//MOD_TIME,
			G_Ps4CcaConsumeInsert.setInt(54, 0);//h_a_ibm_receive_amt
			G_Ps4CcaConsumeInsert.setInt(55, 0);//h_a_unpost_inst_fee
			G_Ps4CcaConsumeInsert.setString(56, P_Data100Vo.getPSeqNo()); //P_SEQNO

			G_Ps4CcaConsumeInsert.executeUpdate();


			

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	public static boolean updateTotUnpaidAmt(int nP_CardAcctIdx, String sP_NewAmt) {
		boolean bL_Result = true;
		
		try {
			String sL_Sql = "update CCA_CONSUME set "
							+ "SET TOT_UNPAID_AMT=TOT_UNPAID_AMT+ ?  "
							+ "WHERE CARD_ACCT_IDX= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql,false);
			ps.setInt(1, Integer.parseInt(sP_NewAmt)); 
			
			ps.setInt(2, nP_CardAcctIdx);
			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}
	
	public static boolean updateCcaConsume(int nP_CardAcctIdx, int nP_IbmReceiveAmtDiff) {
		boolean bL_Result = true;
		
		try {
			
			if (null == G_Ps4CcaConsume4Update) {
				String sL_Sql = "update CCA_CONSUME "
						+ "SET IBM_RECEIVE_AMT=DECODE(SIGN(IBM_RECEIVE_AMT - ? ),-1,0 ,IBM_RECEIVE_AMT - ? ) "
						+ "WHERE CARD_ACCT_IDX= ? ";
				//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
				PreparedStatement ps = getPreparedStatement(sL_Sql, false);


				ps.setInt(1, nP_IbmReceiveAmtDiff); 
				ps.setInt(2, nP_IbmReceiveAmtDiff);
				ps.setInt(3, nP_CardAcctIdx);
				ps.executeUpdate();
				ps.close();

			}
			else {
				


				G_Ps4CcaConsume4Update.setInt(1, nP_IbmReceiveAmtDiff); 
				G_Ps4CcaConsume4Update.setInt(2, nP_IbmReceiveAmtDiff);
				G_Ps4CcaConsume4Update.setInt(3, nP_CardAcctIdx);
				G_Ps4CcaConsume4Update.executeUpdate();
				

			}
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		return bL_Result;
	}

	public static boolean updateCcaConsume(CardAcctVo P_CardAcctVo,ResultSet P_CcsAccountRs ) {
		//proc is UPDATE_AUTH_CONSUME()
		//CCA_CONSUME 卡戶帳務檔 
		boolean bL_Result = true;
		
		try {
			String sL_CurTime = HpeUtil.getCurTimeStr();
			String sL_Sql="";
			sL_Sql = "update CCA_CONSUME  SET PAID_ANNUAL_FEE= ? ,"
						+ "PAID_SRV_FEE       = ? ,"
						+ "PAID_LAW_FEE       = ? ,"
						+ "PAID_PUNISH_FEE    = ? ,"
						+ "PAID_INTEREST_FEE  = ? ,"
						+ "PAID_CONSUME_FEE   = ? ,"
						+ "PAID_PRECASH       = ? ,"
						+ "PAID_CYCLE         = ? ,"
						+ "UNPAID_ANNUAL_FEE  =0,"
						+ "UNPAID_SRV_FEE     = ? ,"
						+ "UNPAID_LAW_FEE     = ? ,"
						+ "UNPAID_INTEREST_FEE= ? ,"
						+ "UNPAID_CONSUME_FEE = ? ,"
						+ "UNPAID_PRECASH     = ? ,"
						+ "M1_AMT             = ? ,"
						+ "ARGUE_AMT          = ? ,"
						+ "PRE_PAY_AMT        = ? ,"
						+ "LATEST_1_MNTH      = ? ,"
						+ "LATEST_2_MNTH      = ? ,"
						+ "LATEST_3_MNTH      = ? ,"
						+ "LATEST_4_MNTH      = ? ,"
						+ "LATEST_5_MNTH      = ? ,"
						+ "LATEST_6_MNTH      = ? ,"
						+ "LATEST_7_MNTH      = ? ,"
						+ "LATEST_8_MNTH      = ? ,"
						+ "LATEST_9_MNTH      = ? ,"
						+ "LATEST_10_MNTH     = ? ,"
						+ "LATEST_11_MNTH     = ? ,"
						+ "LATEST_12_MNTH     = ? ,"
						+ "MAX_CONSUME_AMT    = ? ,"
						+ "MAX_CONSUME_DATE   = ? ,"
						+ "MAX_PRECASH_AMT    = ? ,"
						+ "MAX_PRECASH_DATE   = ? ,"
						+ "PAY_LATEST_AMT     = ? ,"
						+ "PAY_DATE           = ? ,"
						+ "PAY_SETTLE_DATE    = ? ,"
						+ "PAYMENT_DUE_DATE   = ? ,"
						+ "TOT_UNPAID_AMT     = ? , "
						+ "BILL_LOW_LIMIT     = ? ,  "
						+ "BILL_LOW_PAY_AMT   = ? , "
						+ "TOT_LIMIT_AMT      = ? ,    "
						+ "TOT_PRECASH_AMT    = ? ,  "
						+ "TOT_DUE            = ? ,          "
						+ "CONSUME_1          = ? ,"
						+ "CONSUME_2          = ? ,"
						+ "CONSUME_3          = ? ,"
						+ "CONSUME_4          = ? ,"
						+ "CONSUME_5          = ? ,"
						+ "CONSUME_6          = ? ,"
						+ "UNPOST_INST_FEE    = ? ,"
						+ "MOD_TIME        = ? ,"
						+ "	MOD_USER        = ?  ";
                
			sL_Sql += " where CARD_ACCT_IDX= ?";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, P_CcsAccountRs.getString("CloseWritsOff") ); //h_a_close_writsoff
			ps.setString(2, P_CcsAccountRs.getString("CloseSrvFee")); //h_a_close_srv_fee
			
			ps.setString(3, P_CcsAccountRs.getString("CloseLawFee")); //h_a_close_law_fee
			ps.setString(4, P_CcsAccountRs.getString("ClosePunishFee")); //h_a_close_punish_fee
			ps.setString(5, P_CcsAccountRs.getString("CloseInterestFee")); //h_a_close_interest_fee
			ps.setString(6, P_CcsAccountRs.getString("CloseConsumeFee")); //h_a_close_consume_fee
			ps.setString(7, P_CcsAccountRs.getString("ClosePreCash")); //h_a_close_precash
			ps.setString(8, P_CcsAccountRs.getString("OpenPunishFee")); //h_a_open_punish_fee
			ps.setString(9, P_CcsAccountRs.getString("OpenSrvFee")); //h_a_open_srv_fee
			ps.setString(10, P_CcsAccountRs.getString("OpenLawFee")); //h_a_open_law_fee
			ps.setString(11, P_CcsAccountRs.getString("OpenInterestFee")); //h_a_open_interest_fee
			ps.setString(12, P_CcsAccountRs.getString("OpenConsumeFee")); //h_a_open_consume_fee
			ps.setString(13, P_CcsAccountRs.getString("OpenPreCash")); //h_a_open_preca
			
			
			
			
			/*
			ps.setString("p3", P_CardAcctVo.getCloseLawFee()); //h_a_close_law_fee
			ps.setString("p4", P_CardAcctVo.getClosePunishFee()); //h_a_close_punish_fee
			ps.setString("p5", P_CardAcctVo.getCloseInterestFee()); //h_a_close_interest_fee
			ps.setString("p6", P_CardAcctVo.getCloseConsumeFee()); //h_a_close_consume_fee
			ps.setString("p7", P_CardAcctVo.getCclosePreCash()); //h_a_close_precash
			ps.setString("p8", P_CardAcctVo.getOpenPunishFee()); //h_a_open_punish_fee
			ps.setString("p9", P_CardAcctVo.getOpenSrvFee()); //h_a_open_srv_fee
			ps.setString("p10", P_CardAcctVo.getOpenLawFee()); //h_a_open_law_fee
			ps.setString("p11", P_CardAcctVo.getOpenInterestFee()); //h_a_open_interest_fee
			ps.setString("p12", P_CardAcctVo.getOpenConsumeFee()); //h_a_open_consume_fee
			ps.setString("p13", P_CardAcctVo.getOoenPreCash()); //h_a_open_precash
			*/
			ps.setString(14, P_CcsAccountRs.getString("BillLawPayAmt")); //h_a_m1_amt
			ps.setString(15, P_CcsAccountRs.getString("ArgueAmt")); //h_a_argue_amt 
			ps.setString(16, P_CcsAccountRs.getString("prePayAmt")); //h_a_pre_pay_amt
			ps.setString(17, P_CcsAccountRs.getString("L1Mnth")); //h_a_lastest_1_mnth
			ps.setString(18, P_CcsAccountRs.getString("L2Mnth")); //h_a_lastest_2_mnth
			ps.setString(19, P_CcsAccountRs.getString("L3Mnth")); //h_a_lastest_3_mnth
			ps.setString(20, P_CcsAccountRs.getString("L4Mnth")); //h_a_lastest_4_mnth
			ps.setString(21, P_CcsAccountRs.getString("L5Mnth")); //h_a_lastest_5_mnth
			ps.setString(22, P_CcsAccountRs.getString("L6Mnth")); //h_a_lastest_6_mnth
			ps.setString(23, P_CcsAccountRs.getString("L7Mnth")); //h_a_lastest_7_mnth
			ps.setString(24, P_CcsAccountRs.getString("L8Mnth")); //h_a_lastest_8_mnth
			ps.setString(25, P_CcsAccountRs.getString("L9Mnth")); //h_a_lastest_9_mnth
			ps.setString(26, P_CcsAccountRs.getString("L10Mnth")); //h_a_lastest_10_mnth
			ps.setString(27, P_CcsAccountRs.getString("L11Mnth")); //h_a_lastest_11_mnth
			ps.setString(28, P_CcsAccountRs.getString("L12Mnth")); //h_a_lastest_12_mnth
			ps.setString(29, P_CcsAccountRs.getString("MaxConsumeAmt")); //h_a_max_consume_amt
			
			ps.setString(30, P_CcsAccountRs.getString("MaxConsumeDate")); //h_a_max_consume_date
			ps.setString(31, P_CcsAccountRs.getString("MaxPreCashAmt")); //h_a_max_precash_amt
			ps.setString(32, P_CcsAccountRs.getString("MaxPreCashDate")); //h_a_max_precash_date
			ps.setString(33, P_CcsAccountRs.getString("payLastestAmt")); //h_a_pay_lastest_amt
			ps.setString(34, P_CcsAccountRs.getString("payDate")); //h_a_pay_date
			ps.setString(35, P_CcsAccountRs.getString("paySettleDate")); //h_a_pay_settle_date
			ps.setString(36, P_CcsAccountRs.getString("paymentDueDate")); //h_a_payment_due_date
			ps.setString(37, P_CcsAccountRs.getString("TotalUnpaidAmt")); //h_a_total_unpaid_amt
			ps.setString(38, P_CcsAccountRs.getString("BillLowLimit")); //h_a_bill_low_limit
			ps.setString(39, P_CcsAccountRs.getString("OpenWritsOff")); //h_a_bill_low_pay_amt => proc 中似乎把 OpenWritsOff assign 給 bill_low_pay_amt
			
			ps.setString(40, P_CcsAccountRs.getString("TotLimitAmt")); //h_a_tot_limit_amt
			ps.setString(41, P_CcsAccountRs.getString("TotPreCashAmt")); //h_a_tot_precash_amt
			ps.setString(42, P_CcsAccountRs.getString("TotDue")); //h_a_tot_due
			ps.setString(43, P_CcsAccountRs.getString("Consume1")); //h_a_consume_1
			ps.setString(44, P_CcsAccountRs.getString("Consume2")); //h_a_consume_2
			ps.setString(45, P_CcsAccountRs.getString("Consume3")); //h_a_consume_3
			ps.setString(46, P_CcsAccountRs.getString("Consume4")); //h_a_consume_4
			ps.setString(47, P_CcsAccountRs.getString("Consume5")); //h_a_consume_5
			ps.setString(48, P_CcsAccountRs.getString("Consume6")); //h_a_consume_6
			ps.setString(49, P_CcsAccountRs.getString("UnpostInstFee")); //h_a_unpost_inst_fee

			ps.setString(50, sL_CurTime); //MOD_TIME
			ps.setString(51, "ECS100"); //MOD_USER

						ps.setInt(52, P_CardAcctVo.getCardAcctIdx());

			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;

	}
	
	
	public static boolean updateCcaConsume(CardAcctVo P_CardAcctVo,Data100Vo P_Data100Vo ) {
		//proc is UPDATE_AUTH_CONSUME()
		//CCA_CONSUME 卡戶帳務檔 
		boolean bL_Result = true;
		
		try {
			 
			String sL_CurTime = HpeUtil.getCurTimeStr();
			
			String sL_Sql="";
			sL_Sql = "update CCA_CONSUME  SET PAID_ANNUAL_FEE= ? ,"
						+ "PAID_SRV_FEE       = ? ,"
						+ "PAID_LAW_FEE       = ? ,"
						+ "PAID_PUNISH_FEE    = ? ,"
						+ "PAID_INTEREST_FEE  = ? ,"
						+ "PAID_CONSUME_FEE   = ? ,"
						+ "PAID_PRECASH       = ? ,"
						+ "PAID_CYCLE         = ? ,"
						+ "UNPAID_ANNUAL_FEE  =0,"
						+ "UNPAID_SRV_FEE     = ? ,"
						+ "UNPAID_LAW_FEE     = ? ,"
						+ "UNPAID_INTEREST_FEE= ? ,"
						+ "UNPAID_CONSUME_FEE = ? ,"
						+ "UNPAID_PRECASH     = ? ,"
						+ "M1_AMT             = ? ,"
						+ "ARGUE_AMT          = ? ,"
						+ "PRE_PAY_AMT        = ? ,"
						+ "LATEST_1_MNTH      = ? ,"
						+ "LATEST_2_MNTH      = ? ,"
						+ "LATEST_3_MNTH      = ? ,"
						+ "LATEST_4_MNTH      = ? ,"
						+ "LATEST_5_MNTH      = ? ,"
						+ "LATEST_6_MNTH      = ? ,"
						+ "LATEST_7_MNTH      = ? ,"
						+ "LATEST_8_MNTH      = ? ,"
						+ "LATEST_9_MNTH      = ? ,"
						+ "LATEST_10_MNTH     = ? ,"
						+ "LATEST_11_MNTH     = ? ,"
						+ "LATEST_12_MNTH     = ? ,"
						+ "MAX_CONSUME_AMT    = ? ,"
						+ "MAX_CONSUME_DATE   = ? ,"
						+ "MAX_PRECASH_AMT    = ? ,"
						+ "MAX_PRECASH_DATE   = ? ,"
						+ "PAY_LATEST_AMT     = ? ,"
						+ "PAY_DATE           = ? ,"
						+ "PAY_SETTLE_DATE    = ? ,"
						+ "PAYMENT_DUE_DATE   = ? ,"
						+ "TOT_UNPAID_AMT     = ? , "
						+ "BILL_LOW_LIMIT     = ? ,  "
						+ "BILL_LOW_PAY_AMT   = ? , "
						+ "TOT_LIMIT_AMT      = ? ,    "
						+ "TOT_PRECASH_AMT    = ? ,  "
						+ "TOT_DUE            = ? ,          "
						+ "CONSUME_1          = ? ,"
						+ "CONSUME_2          = ? ,"
						+ "CONSUME_3          = ? ,"
						+ "CONSUME_4          = ? ,"
						+ "CONSUME_5          = ? ,"
						+ "CONSUME_6          = ? ,"
						+ "UNPOST_INST_FEE    = ? ,"
						+ "MOD_TIME        = ? ,"
						+ "	MOD_USER        = ?  ";
                
			sL_Sql += " where CARD_ACCT_IDX= ?";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setInt(1, 0); //h_a_close_writsoff
			ps.setInt(2, P_Data100Vo.getCloseSrvFee()  ); //h_a_close_srv_fee
			
			ps.setInt(3, P_Data100Vo.getCloseLawFee()); //h_a_close_law_fee
			ps.setInt(4, P_Data100Vo.getClosePunishFee()); //h_a_close_punish_fee
			ps.setInt(5, P_Data100Vo.getCloseInterestFee()); //h_a_close_interest_fee
			ps.setInt(6, 0); //h_a_close_consume_fee
			ps.setInt(7, 0); //h_a_close_precash
			ps.setInt(8, 0); //h_a_open_punish_fee
			ps.setInt(9, P_Data100Vo.getOpenSrvFee()); //h_a_open_srv_fee
			ps.setInt(10, P_Data100Vo.getOpenLawFee()); //h_a_open_law_fee
			ps.setInt(11, P_Data100Vo.getOpenInterestFee()); //h_a_open_interest_fee
			ps.setInt(12, P_Data100Vo.getOpenConsumeFee()); //h_a_open_consume_fee
			ps.setInt(13, 0); //h_a_open_preca
			
			
			
			
			ps.setInt(14, 0); //h_a_m1_amt
			ps.setInt(15, 0); //h_a_argue_amt 
			ps.setInt(16, P_Data100Vo.getPrepayAmt()); //h_a_pre_pay_amt
			ps.setString(17, ""); //h_a_lastest_1_mnth
			ps.setString(18, ""); //h_a_lastest_2_mnth
			ps.setString(19, ""); //h_a_lastest_3_mnth
			ps.setString(20, ""); //h_a_lastest_4_mnth
			ps.setString(21, ""); //h_a_lastest_5_mnth
			ps.setString(22, ""); //h_a_lastest_6_mnth
			ps.setString(23, ""); //h_a_lastest_7_mnth
			ps.setString(24, ""); //h_a_lastest_8_mnth
			ps.setString(25, ""); //h_a_lastest_9_mnth
			ps.setString(26, ""); //h_a_lastest_10_mnth
			ps.setString(27, ""); //h_a_lastest_11_mnth
			ps.setString(28, ""); //h_a_lastest_12_mnth
			ps.setInt(29, 0); //h_a_max_consume_amt
			
			ps.setString(30, ""); //h_a_max_consume_date
			ps.setInt(31, P_Data100Vo.getMaxAtmAmt()); //h_a_max_precash_amt
			ps.setString(32, ""); //h_a_max_precash_date
			ps.setInt(33, 0); //h_a_pay_lastest_amt
			ps.setString(34, ""); //h_a_pay_date
			ps.setString(35, ""); //h_a_pay_settle_date
			ps.setString(36, ""); //h_a_payment_due_date
			ps.setInt(37, 0); //h_a_total_unpaid_amt
			ps.setInt(38, P_Data100Vo.getBillLowLimit()); //h_a_bill_low_limit
			ps.setInt(39, P_Data100Vo.getOpenWritsOff()); //h_a_bill_low_pay_amt => proc 中似乎把 OpenWritsOff assign 給 bill_low_pay_amt
			
			ps.setInt(40, P_Data100Vo.getTot1LimitAmt()); //h_a_tot_limit_amt
			ps.setInt(41, P_Data100Vo.getTot2LimitAmt()); //h_a_tot_precash_amt
			ps.setString(42, ""); //h_a_tot_due
			ps.setInt(43, P_Data100Vo.getConsume01() ); //h_a_consume_1
			ps.setInt(44, P_Data100Vo.getConsume02()); //h_a_consume_2
			ps.setInt(45, P_Data100Vo.getConsume03()); //h_a_consume_3
			ps.setInt(46, P_Data100Vo.getConsume04()); //h_a_consume_4
			ps.setInt(47, P_Data100Vo.getConsume05()); //h_a_consume_5
			ps.setInt(48, P_Data100Vo.getConsume06()); //h_a_consume_6
			
			ps.setInt(49, 0); //h_a_unpost_inst_fee

			ps.setString(50, sL_CurTime); //MOD_TIME
			ps.setString(51, "ECS100"); //MOD_USER

			ps.setInt(52, P_CardAcctVo.getCardAcctIdx());

			ps.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;

	}

}
