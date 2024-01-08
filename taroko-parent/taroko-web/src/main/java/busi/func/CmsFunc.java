/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/09/16  V1.00.00 JustinWu               program initial                          *
******************************************************************************/

package busi.func;


import busi.FuncBase;

public class CmsFunc extends FuncBase {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	
	public CmsFunc(taroko.com.TarokoCommon wp) {
		this.wp = wp;
		setConn(wp.getConn());
	}

	public String getEducationDesc(String educationCode) {
		try {
			wp.logSql = false;
			String sql = " select MSG " 
			        + " from crd_message " 
					+ " where 1 = 1" 
			        + " and msg_type = 'EDUCATION' "
					+ " and MSG_VALUE = ? ";

			setString2(1, educationCode);

			sqlSelect(sql);

			if (sqlRowNum <= 0) {
				return "";
			}
		} catch (Exception ex) {
			throw ex;
		}

		return colStr("MSG");

	}
	
	public String getBusinessDesc(String businessCode) {
		try {
			wp.logSql = false;
			String sql = " select MSG " 
			        + " from crd_message " 
					+ " where 1 = 1" 
			        + " and msg_type = 'BUS_CODE' "
					+ " and MSG_VALUE = ? ";

			setString2(1, businessCode);

			sqlSelect(sql);

			if (sqlRowNum <= 0) {
				return "";
			}
		} catch (Exception ex) {
			throw ex;
		}

		return colStr("MSG");

	}
		
  

}
