package bank.authbatch.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bank.authbatch.main.AuthBatchDbHandler;


import bank.authbatch.vo.CcaSysParm2Vo;

public class SysIbmDao  extends AuthBatchDbHandler{

	public SysIbmDao() throws Exception{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 取回 x 天前的日期字串
	 * @param nP_DiffDateCount 往前推的天數
	 * @return 回傳 nP_DiffDateCount 天前的日期字串
	 */
	public static String getDate(int nP_DiffDateCount) {
		String sL_Result = "";
		
		try {
			//SELECT TO_CHAR(CURRENT DATE - 3 DAY,'YYYYMMDD') as TargetDateStr FROM sysibm.sysdummy1
			String sL_Sql = "SELECT TO_CHAR(CURRENT DATE - ? DAY,'YYYYMMDD') as TargetDateStr FROM sysibm.sysdummy1 ";
			//NamedParamStatement ps = new NamedParamStatement(Db2Connection, sL_Sql);
			PreparedStatement ps = getPreparedStatement(sL_Sql, true);
			ps.setInt(1, nP_DiffDateCount);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {

				sL_Result = rs.getString("TargetDateStr");
				
			}
			releaseConnection(rs);
		
		} catch (Exception e) {
			// TODO: handle exception
			sL_Result="";
		}
		
		return sL_Result;
	}

}
