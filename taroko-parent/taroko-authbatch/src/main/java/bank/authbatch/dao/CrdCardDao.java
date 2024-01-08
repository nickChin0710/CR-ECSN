package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.main.HpeUtil;
import bank.authbatch.vo.CcaBaseVo;

public class CrdCardDao extends AuthBatchDbHandler{

	public CrdCardDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	public static ResultSet getCrdCardByCardNo(String sP_CardNo) {
		ResultSet L_ResultSet = null;
		try {
			String sL_Sql = "select NEW_END_DATE as CardNewEndDate, BIN_TYPE as CardBinType, "
						+ "CARD_TYPE as CardCardType, BANK_ACTNO as CardBankActNo, "
						+ "GROUP_CODE as CardGroupCode, CURRENT_CODE as CardCurrentCode "
						+ "from CRD_CARD where CARD_NO= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_CardNo);
			
			L_ResultSet = ps.executeQuery();
			
		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		return L_ResultSet;
	}

	public static boolean updateCurCode(String sP_CardNo, String sP_NewCurCode, String sP_CurDate) {
		//更新卡片狀態, ECS004會用到
		boolean bL_Result=true;
		try {
			String sL_Sql = "UPDATE CRD_CARD "
                       + "SET CURRENT_CODE= ? " 
                       + "WHERE CARD_NO= ? and CURRENT_CODE= ? and NEW_END_DATE>= ? " ;
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			ps.setString(1, sP_NewCurCode);
			ps.setString(2, sP_CardNo);
			ps.setString(3, "0"); //正常卡
			ps.setString(4, sP_CurDate );  
			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	
	public static boolean updateCardLimit(String sP_CardNo, int nP_CardLimit) {
		//更新卡片額度, ECS050會用到
		boolean bL_Result=true;
		try {
			String sL_Sql = "UPDATE CRD_CARD "
                       + "SET INDIV_CRD_LMT= ? " 
                       + "WHERE CARD_NO= ? " ;
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			ps.setInt(1, nP_CardLimit);
			ps.setString(2, sP_CardNo);
  
			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	/*
	public static boolean updateCrdCardBlockCode(String sP_CardNo, String sP_NewBlockCode, String sP_CurDate) {
		boolean bL_Result=true;
		try {

			String sL_Sql = "UPDATE CRD_CARD "
                       + "SET BLOCK_CODE=:p1, BLOCK_DATE=:p2 "
                       + "WHERE CARD_NO=:p3 and NEW_END_DATE>=:p4 " ;
			NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			ps.setString("p1", sP_NewBlockCode);
			ps.setString("p2", sP_CurDate);
			ps.setString("p3", sP_CardNo);
			ps.setString("p4", sP_CurDate);
			bL_Result = ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}

	public static String getCrdCardBlockCode(String sP_CardNo) {
		String sL_Result="";
		try {

			String sL_Sql = "select BLOCK_CODE from  CRD_CARD "
                       + "WHERE CARD_NO=:p3" ;
			NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			ps.setString("p3", sP_CardNo);
			ResultSet L_ResultSet = ps.executeQuery();
			
			while (L_ResultSet.next()) {
				sL_Result = L_ResultSet.getString("BLOCK_CODE");
				
			}
			releaseConnection(L_ResultSet);

		} catch (Exception e) {
			// TODO: handle exception
			sL_Result="";
		}
		
		return sL_Result;
		
	}
	*/
	public static boolean updateCrdCard(String sP_CardNo, String sP_NewValueOfOldEndDate) {
		boolean bL_Result=true;
		try {
			String sL_Sql = "UPDATE CRD_CARD "
                       + "SET OLD_END_DATE= ? " //Howard:把 OLD_CARD_EFF_DATE_END update 為
                       + "WHERE CARD_NO= ? " ;
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			ps.setString(1, sP_NewValueOfOldEndDate);
			ps.setString(2, sP_CardNo); 
			ps.executeUpdate();
			
			ps.close();

		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
		
	}
	public static ResultSet getCrdCard(String sP_CardStatus, int nP_Type, String sP_DbOpenDate, String sP_ActivateDate01, String sP_ActivateDate31, String sP_DbProcDate, String sP_DbCardTo, String sP_DbCardFrom) {
		
		ResultSet L_ResultSet = null;
		String sL_Sql = "";
		
		PreparedStatement ps =null;
		try {
			if (nP_Type==1) {
				sL_Sql = "select NVL(CARD_NO,'') as CardNo,"
						+"NVL(CURRENT_CODE,'*')," //卡片狀態  == CRD_CARD.CURRENT_CODE (0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡)
						+ "NVL(NEW_END_DATE,'00000000')," //新卡有效年月日(迄) == CRD_CARD.NEW_END_DATE
						+ "NVL(SOURCE_CODE,'*'),"//新卡開卡來源 == CRD.CARD.SOURCE_CODE
						+ "NVL(ACTIVATE_DATE,'00000000')," ////新卡開卡日期 == CRD_CARD.ACTIVATE_DATE
						+"NVL(OLD_END_DATE,'') as OldEndDate "; //舊卡有效迄日
				sL_Sql += "from CRD_CARD where ";
				sL_Sql += "CARD_STATUS= ? "
                        	+"AND ACTIVATE_FLAG= ? " //新卡開卡註記 (Y/N) == CRD_CARD.ACTIVATE_FLAG (1:關閉 2:開卡)
                        	+"AND ACTIVATE_DATE<= ? " //ACTIVATE_DATE
                        	+"AND OLD_END_DATE>= ? " //OLD_END_DATE :舊卡有效年月日(迄)>=
                        	+"AND OLD_END_DATE<= ? "
                        	+"AND OLD_END_DATE>= ? "
                        	+"AND NEW_END_DATE<>OLD_END_DATE"; //NEW_END_DATE
				
				//ps = new NamedParamStatement(Db2Connection, sL_Sql);
				ps = getPreparedStatement(sL_Sql, true);
				ps.setString(1, "0");//卡片狀態  == CRD_CARD.CURRENT_CODE (0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡)
				ps.setString(2, "2");//本卡開卡註記== CRD_CARD.ACTIVATE_FLAG (1:關閉 2:開卡)
				ps.setString(3, sP_DbOpenDate);//新卡開卡日期 == CRD_CARD.ACTIVATE_DATE
				ps.setString(4, sP_ActivateDate01); //OLD_END_DATE:DB_EFF_DATE01
				ps.setString(5, sP_ActivateDate31);//OLD_END_DATE:DB_EFF_DATE31
				ps.setString(6, sP_DbProcDate);//OLD_END_DATE:DB_PROC_DATE


			}
			else if (nP_Type==2) {
				sL_Sql = "select NVL(CARD_NO,'') as CardNo,"
						+"NVL(CURRENT_CODE,'*')," //卡片狀態  == CRD_CARD.CURRENT_CODE (0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡)
						+ "NVL(NEW_END_DATE,'00000000')," //新卡有效年月日(迄) == CRD_CARD.NEW_END_DATE
						+ "NVL(SOURCE_CODE,'*'),"//新卡開卡來源 == CRD.CARD.SOURCE_CODE
						+ "NVL(ACTIVATE_DATE,'00000000')," ////新卡開卡日期 == CRD_CARD.ACTIVATE_DATE
						+"NVL(OLD_END_DATE,'') as OldEndDate "; //舊卡有效迄日
				sL_Sql += "from CRD_CARD where ";
				sL_Sql += "CARD_STATUS= ? "
                        	+"AND ACTIVATE_FLAG= ? " //新卡開卡註記 (Y/N) == CRD_CARD.ACTIVATE_FLAG (1:關閉 2:開卡)
                        	+"AND ACTIVATE_DATE<= ? " //ACTIVATE_DATE
                        	+"AND OLD_END_DATE>= ? " //OLD_END_DATE :舊卡有效年月日(迄)>=
                        	+"AND OLD_END_DATE<= ? "
                        	+"AND OLD_END_DATE>= ? "
                        	+"AND NEW_END_DATE<>OLD_END_DATE" //NEW_END_DATE
                        	+"AND CARD_NO<= ? ";
				
				//ps = new NamedParamStatement(Db2Connection, sL_Sql);
				ps = getPreparedStatement(sL_Sql, true);
				ps.setString(1, "0");//卡片狀態  == CRD_CARD.CURRENT_CODE (0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡)
				ps.setString(2, "2");//本卡開卡註記== CRD_CARD.ACTIVATE_FLAG (1:關閉 2:開卡)
				ps.setString(3, sP_DbOpenDate);//新卡開卡日期 == CRD_CARD.ACTIVATE_DATE
				ps.setString(4, sP_ActivateDate01); //OLD_END_DATE:DB_EFF_DATE01
				ps.setString(5, sP_ActivateDate31);//OLD_END_DATE:DB_EFF_DATE31
				ps.setString(6, sP_DbProcDate);//OLD_END_DATE:DB_PROC_DATE
				ps.setString(7, sP_DbCardTo);
			}
			else if (nP_Type==3) {
				sL_Sql = "select NVL(CARD_NO,'') as CardNo,"
						+"NVL(CURRENT_CODE,'*')," //卡片狀態  == CRD_CARD.CURRENT_CODE (0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡)
						+ "NVL(NEW_END_DATE,'00000000')," //新卡有效年月日(迄) == CRD_CARD.NEW_END_DATE
						+ "NVL(SOURCE_CODE,'*'),"//新卡開卡來源 == CRD.CARD.SOURCE_CODE
						+ "NVL(ACTIVATE_DATE,'00000000')," ////新卡開卡日期 == CRD_CARD.ACTIVATE_DATE
						+"NVL(OLD_END_DATE,'') as OldEndDate "; //舊卡有效迄日
				sL_Sql += "from CRD_CARD where ";
				sL_Sql += "CARD_STATUS= ? "
                        	+"AND ACTIVATE_FLAG= ? " //新卡開卡註記 (Y/N) == CRD_CARD.ACTIVATE_FLAG (1:關閉 2:開卡)
                        	+"AND ACTIVATE_DATE<= ? " //ACTIVATE_DATE
                        	+"AND OLD_END_DATE>= ? " //OLD_END_DATE :舊卡有效年月日(迄)>=
                        	+"AND OLD_END_DATE<= ? "
                        	+"AND OLD_END_DATE>= ? "
                        	+"AND NEW_END_DATE<>OLD_END_DATE" //NEW_END_DATE
                        	+"AND CARD_NO<= ? "
                        	+"AND CARD_NO>= ? ";
				
				
				//ps = new NamedParamStatement(Db2Connection, sL_Sql);
				ps = getPreparedStatement(sL_Sql, true);
				ps.setString(1, "0");//卡片狀態  == CRD_CARD.CURRENT_CODE (0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡)
				ps.setString(2, "2");//本卡開卡註記== CRD_CARD.ACTIVATE_FLAG (1:關閉 2:開卡)
				ps.setString(3, sP_DbOpenDate);//新卡開卡日期 == CRD_CARD.ACTIVATE_DATE
				ps.setString(4, sP_ActivateDate01); //OLD_END_DATE:DB_EFF_DATE01
				ps.setString(5, sP_ActivateDate31);//OLD_END_DATE:DB_EFF_DATE31
				ps.setString(6, sP_DbProcDate);//OLD_END_DATE:DB_PROC_DATE
				ps.setString(7, sP_DbCardTo);
				ps.setString(8, sP_DbCardFrom);
				
			}
			if (ps == null) {
			    throw new RuntimeException("nP_Type[" + nP_Type + "] is invalid.");
			}
			L_ResultSet = ps.executeQuery();
			/*
			String sL_Sql="select A.CARD_TYPE from CRD_CARD A, CRD_IDNO B "
						+"where A.ID_P_SEQNO=B.ID_P_SEQNO and B.ID_NO=:p1 "
						+"AND (CARD_TYPE != :p2 "
						+"OR (CARD_TYPE = :p3 AND SUP_FLAG = :p4)) ";
			NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			ps.setString("p1", sP_CardNo);
			ps.setString("p2", "A4");
			ps.setString("p3", "A4");
			ps.setString("p4", "1");//0:正卡 1:附卡
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆
			//ps.setString("p2", sP_SysKey);
		
			
			L_ResultSet = ps.executeQuery();
			*/					

		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		
		return L_ResultSet;
	}

	public static ResultSet getCrdCard(String sP_IdNo) {
		
		ResultSet L_ResultSet = null;
		try {
			String sL_Sql="select A.CARD_TYPE from CRD_CARD A, CRD_IDNO B "
						+"where A.ID_P_SEQNO=B.ID_P_SEQNO and B.ID_NO= ? "
						+"AND (CARD_TYPE != ? "
						+"OR (CARD_TYPE = ? AND SUP_FLAG = ? )) ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_IdNo);
			ps.setString(2, "A4");
			ps.setString(3, "A4");
			ps.setString(4, "1");//0:正卡 1:附卡
			//ps.getPreparedStatement().setFetchSize(1000);//設定每次只get 1000筆
			//ps.setString("p2", sP_SysKey);
		
			
			L_ResultSet = ps.executeQuery();
								

		} catch (Exception e) {
			// TODO: handle exception
			L_ResultSet = null;
		}
		
		return L_ResultSet;
	}

}
