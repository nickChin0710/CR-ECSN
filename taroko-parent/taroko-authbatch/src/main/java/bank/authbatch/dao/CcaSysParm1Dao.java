package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import bank.authbatch.main.AuthBatchDbHandler;



public class CcaSysParm1Dao  extends AuthBatchDbHandler{

	public CcaSysParm1Dao()  throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	//public static ResultSet getCcaSysParm1(String sP_SysId, String sP_SysKey, String sP_SysData1DefValue) {
	public static ResultSet getCcaSysParm1(String sP_SysId, String sP_SysKey) {	
		ResultSet L_ResultSet = null;
		
		
		
		try {
			/*
			String sL_Sql = "select NVL(SYS_DATA1," + sP_SysData1DefValue + ") as SysData1,NVL(SYS_DATA2,'0') as SysData2,NVL(SYS_DATA3,' ') as SysData3, NVL(SYS_DATA4,' ') as SysData4,NVL(SYS_DATA5,'0') as SysData5 from CCA_SYS_PARM1 where SYS_ID= ?  AND SYS_KEY= ?  "; //key....
			PreparedStatement ps = getPreparedStatement(sL_Sql);
			ps.setString(1, sP_SysId);
			ps.setString(2, sP_SysKey);
		
			L_ResultSet = ps.executeQuery();
			*/
			G_Ps4SysParm1.setString(1, sP_SysId);
			G_Ps4SysParm1.setString(2, sP_SysKey);
		
			L_ResultSet = G_Ps4SysParm1.executeQuery();
			
			
		}
		catch (Exception e) {
			L_ResultSet=null;
			//System.out.println("Howard99=>" + sP_SysId+ "---" +sP_SysKey + "===");
		}

		return L_ResultSet;
	}

}
