/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
 *  109/07/22  V1.00.01    Zuwei     coding standard, rename field method & format                   *
 *  109/12/22   V1.00.02   Justin      zz chg name
******************************************************************************/
package com;

import java.sql.Connection;

public class CheckVdAuth extends AccessDAO {
	
	com.CommDate commDate = new com.CommDate();
	com.CommString commString = new com.CommString();
	com.CommSqlStr commSqlStr = new com.CommSqlStr();
	double vdHighRate = 0.0;
	double vdLowRate = 0.0;
	double vdHighDay = 0;
	double vdLowDay = 0;
	String[] DBNAME = new String[10];
	
	public CheckVdAuth(Connection conn[], String[] dbAlias) throws Exception {
        // TODO Auto-generated constructor stub
        super.conn = conn;
        setDBalias(dbAlias);
        setSubParm(dbAlias);

        DBNAME[0] = dbAlias[0];
        
        return;
    }
	
	public String checkAuth(String cardNo , String authCode , String txDate , Double ntAmt , String tranCode) throws Exception {
		String lsHighDate = "" , lsLowDate = "";
		Double ldHighAmt = 0.0 , ldLowAmt = 0.0 ;
		String hRowid = "";
		
		lsHighDate = commDate.dateAdd(txDate, 0, 0, (int) vdHighDay);
		lsLowDate = commDate.dateAdd(txDate, 0, 0, (int) -vdLowDay);
		ldHighAmt = Math.abs(ntAmt) * (1+vdHighRate / 100) ;
		ldLowAmt = Math.abs(ntAmt) * (1-vdLowRate / 100) ;
		
		sqlCmd = " select hex(rowid) as rowid from cca_auth_txlog where card_no = ? and auth_no = ? ";
		sqlCmd += " and tx_date >= ? and tx_date <= ? and abs(nt_amt) >= ? and abs(nt_amt) <= ? ";
		
		if(commString.pos("|05|07|08|09|26",tranCode)>0) {
			sqlCmd += " and mtch_flag <> 'Y' and nt_amt >0 ";
		} else if(commString.pos("|25|27|28|29|06",tranCode)>0) {
			sqlCmd += " and mtch_flag = 'Y' ";
			if(ntAmt>0) sqlCmd += " and nt_amt <0 ";
			else if(ntAmt<0) sqlCmd += " and nt_amt >0 ";
		}
		
		sqlCmd += commSqlStr.rownum(1);
		
		setString(1,cardNo);
		setString(2,authCode);		
		setString(3,lsLowDate);
		setString(4,lsHighDate);
		setDouble(5,ldLowAmt);
		setDouble(6,ldHighAmt);
		
		int r =selectTable();
		if(r<=0)	return "";
		
		hRowid = getValue("rowid");
		
		if(commString.pos("|05|07|08|09",tranCode)>0) {
			updateCcaAuthTxlog(hRowid);			
		}
		
		return hRowid;
	}
	
	public void updateCcaAuthTxlog(String lsRowid) throws Exception {
		
		daoTable = "cca_auth_txlog";
		updateSQL = "mtch_flag = 'Y' ,";
		updateSQL += "mod_user = ? , ";
		updateSQL += "mod_pgm = ? , ";
		updateSQL += "mod_time = sysdate ";
		whereStr = "where rowid = ? ";
		setString(1,"system");
		setString(2,"CheckVDAuth");
		setRowId(3,lsRowid);
		updateTable();        
	}
	
	public void getAuthParm() throws Exception {
		String sysId = "REPORT" , sysKey = "";
		
		//--金額上限百分比
		sysKey = "VD_U_LIMIT";
		sqlCmd = "select sys_data1 as vd_u_limit from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1,sysId);
		setString(2,sysKey);
		
		int recordCnt = selectTable();
		
		if(recordCnt>0) {
			vdHighRate = getValueDouble("vd_u_limit");
		} else {
			vdHighRate = 5;
		}
		
		//--金額下限百分比
		sysKey = "VD_L_LIMIT";
		sqlCmd = "select sys_data1 as vd_l_limit from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1,sysId);
		setString(2,sysKey);
		
		recordCnt = selectTable();
		
		if(recordCnt>0) {
			vdLowRate = getValueDouble("vd_l_limit");
		} else {
			vdLowRate = 5;
		}
		
		//--天數 +
		sysKey = "VD_U_DAY";
		sqlCmd = "select sys_data1 as vd_u_day from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1,sysId);
		setString(2,sysKey);
		
		recordCnt = selectTable();
		
		if(recordCnt>0) {
			vdHighDay = getValueDouble("vd_u_day");
		} else {
			vdHighDay = 1;
		}
		
		//--天數 -
		sysKey = "VD_L_DAY";
		sqlCmd = "select sys_data1 as vd_l_day from cca_sys_parm1 where sys_id = ? and sys_key = ? ";
		setString(1,sysId);
		setString(2,sysKey);
		
		recordCnt = selectTable();
		
		if(recordCnt>0) {
			vdLowDay = getValueDouble("vd_l_day");
		} else {
			vdLowDay = 8;
		}		
		
	}
	
}
