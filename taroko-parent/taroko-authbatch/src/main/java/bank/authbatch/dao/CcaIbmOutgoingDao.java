package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;

public class CcaIbmOutgoingDao extends AuthBatchDbHandler{

	public CcaIbmOutgoingDao()  throws Exception{
		// TODO Auto-generated constructor stub
	}

	static String sG_SelectData4Auo040 ="SELECT NVL(SEQ_NO,0) as IbmOutgoingSeqNo, "
						+ "NVL(CARD_NO,' ') as IbmOutgoingCardNo, "
						+ "NVL(KEY_TABLE,' ') as IbmOutgoingKeyTable, "
						+ "NVL(BITMAP,' ') as IbmOutgoingBitMap, "
						+ "NVL(PROC_FLAG,'0') as IbmOutgoingProcFlag, "
						+ "NVL(SEND_TIMES,0) as IbmOutgoingSendTimes, "
						+ "NVL(ACT_CODE,'A') as IbmOutgoingActCode "
						+ "FROM CCA_IBM_OUTGOING "
						+ "where  SEND_TIMES<=? "
						+ "ORDER BY SEQ_NO";
						
	 
    
    
    
    
    
     




	static PreparedStatement G_SelectPs4Auo040 = null;
	
	public static ResultSet getData4Auo040() {
		
		ResultSet L_Rs = null;
		try {
			int nL_SendTimes = 10;
			G_SelectPs4Auo040.setInt(1, nL_SendTimes);

			L_Rs = G_SelectPs4Auo040.executeQuery();
		} catch (Exception e) {
			// TODO: handle exception
			//System.out.println("Exception:" + e.getMessage());
			L_Rs = null;
		}
		
		return L_Rs;
	}

	public static void closePs() {
		try {
			if (null != G_SelectPs4Auo040)
				G_SelectPs4Auo040.close();

		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	public static void initPs() {
		
		try {
			if (null == G_SelectPs4Auo040)
				G_SelectPs4Auo040 = getDbConnection().prepareStatement(sG_SelectData4Auo040);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
