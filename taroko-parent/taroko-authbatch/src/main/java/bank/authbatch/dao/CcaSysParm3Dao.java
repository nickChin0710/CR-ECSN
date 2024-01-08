package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;



import bank.authbatch.vo.CcaSysParm3Vo;
import bank.authbatch.vo.CcaBaseVo;
import bank.authbatch.main.*;

public  class CcaSysParm3Dao extends AuthBatchDbHandler{

	public CcaSysParm3Dao() throws Exception {
		// TODO Auto-generated constructor stub
	}

	public static boolean updateSysParm3(String sP_SysId,String sP_SysKey,  String sP_SysData3Value) {
		boolean bL_Result = true;
		
		try {
			String sL_Sql="";
			sL_Sql = "UPDATE SYS_PARM3 SET SYS_DATA3 = ? WHERE SYS_ID= ? and SYS_KEY= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql,false);
			ps.setString(1, sP_SysData3Value);
			ps.setString(2, sP_SysId);
			ps.setString(3, sP_SysKey);
			
			ps.executeUpdate();
			ps.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	public static boolean updateSysParm3(String sP_SysId, String sP_SysData4Value) {
		boolean bL_Result = true;
		
		try {
			String sL_Sql="";
			sL_Sql = "UPDATE SYS_PARM3 SET SYS_DATA4 = ? WHERE SYS_ID= ? ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, false);
			ps.setString(1, sP_SysData4Value);
			ps.setString(2, sP_SysId);
			
			ps.executeUpdate();
			ps.close();

			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	

	public static boolean getCcaSysParm3(String sP_SysId, String sP_SysKey, boolean bP_IncludeSysKey, String sP_SysData3DefaultValue) {
		boolean bL_Result = true;
		
		String sL_Sql=""; 
		if (bP_IncludeSysKey)
			sL_Sql = "select NVL(SYS_DATA1,'5') as SysData1, NVL(SYS_DATA2,'1') as SysData2, NVL(SYS_DATA3,'" + sP_SysData3DefaultValue + "') as SysData3, NVL(SYS_DATA4,'0') as SysData4 from CCA_SYS_PARM3 where SYS_ID= ?  AND SYS_KEY= ?  ";
		else
			sL_Sql = "select NVL(SYS_DATA1,'5') as SysData1, NVL(SYS_DATA2,'1') as SysData2, NVL(SYS_DATA3,'" + sP_SysData3DefaultValue + "') as SysData3, NVL(SYS_DATA4,'0') as SysData4 from CCA_SYS_PARM3 where SYS_ID= ? "; 

		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_SysId);
			
			if (bP_IncludeSysKey)
				ps.setString(2, sP_SysKey);
		
			if (CcaSysParm3Vo.ccaSysParm3List ==null)
				CcaSysParm3Vo.ccaSysParm3List = new ArrayList<CcaSysParm3Vo>();
			else
				CcaSysParm3Vo.ccaSysParm3List.clear();
			CcaSysParm3Vo L_Obj = null;
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				L_Obj = new CcaSysParm3Vo();
				L_Obj.SysData1 = rs.getString("SysData1");
				L_Obj.SysData2 = rs.getString("SysData2");
				L_Obj.SysData3 = rs.getString("SysData3");
				L_Obj.SysData4 = rs.getString("SysData4");
				CcaSysParm3Vo.ccaSysParm3List.add(L_Obj);
			}
			rs.close();
			ps.close();
			//Db2Connection.commit();
		}
		catch (Exception e) {

		}

		return bL_Result;
	}

}
