package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import bank.authbatch.main.AuthBatchDbHandler;
import bank.authbatch.vo.CcaSysParm2Vo;
import bank.authbatch.vo.CcaSysParm3Vo;

public class CcaSysParm2Dao extends AuthBatchDbHandler{

	public CcaSysParm2Dao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	/*
	public static boolean getCcaSysParm2(String sP_SysId, String sP_SysKey) {
		boolean bL_Result = true;
		
		String sL_Sql = "select NVL(SYS_DATA1,'A') as SysData1 from CCA_SYS_PARM2 where SYS_ID=:p1  AND SYS_KEY=:p2  "; //key....
		
		try {
			NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			ps.setString("p1", sP_SysId);
			ps.setString("p2", sP_SysKey);
		
			if (CcaSysParm2Vo.ccaSysParm2List ==null)
				CcaSysParm2Vo.ccaSysParm2List = new ArrayList<CcaSysParm2Vo>();
			else
				CcaSysParm2Vo.ccaSysParm2List.clear();
			CcaSysParm2Vo L_Obj = null;
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				L_Obj = new CcaSysParm2Vo();
				L_Obj.SysData1 = rs.getString("SysData1");
				CcaSysParm2Vo.ccaSysParm2List.add(L_Obj);
			}
			releaseConnection(rs);
		
		}
		catch (Exception e) {
			bL_Result=false;
		}

		return bL_Result;
	}
	*/
	public static ResultSet getCcaSysParm2(String sP_SysId, String sP_SysKey) {
		ResultSet L_ResultRs = null;
		
		//String sL_Sql = "select NVL(SYS_DATA1,'A') as SysData1 from CCA_SYS_PARM2 where SYS_ID= ?  AND SYS_KEY= ?  "; //key....
		
		try {
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			/*
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setString(1, sP_SysId);
			ps.setString(2, sP_SysKey);
		
			L_ResultRs = ps.executeQuery();
			*/
			
			G_Ps4SysParm2.setString(1, sP_SysId);
			G_Ps4SysParm2.setString(2, sP_SysKey);
		
			L_ResultRs = G_Ps4SysParm2.executeQuery();
//ECSACCT, 01
			System.out.println("getCcaSysParm2() =>" + sP_SysId + "===" + sP_SysKey + "---");
			
			//releaseConnection(rs);
			/*
			rs.close();
			ps.close();
			Db2Connection.commit();
			*/
		}
		catch (Exception e) {
			System.out.println("getCcaSysParm2() return null!!" + sP_SysId + "===" + sP_SysKey + "---");
			e.printStackTrace(System.out);
			L_ResultRs=null;
		}

		return L_ResultRs;
	}

}
