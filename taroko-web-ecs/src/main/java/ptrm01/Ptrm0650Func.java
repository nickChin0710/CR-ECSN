/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-04-25  V1.00.00  David FU   program initial                            *
* 109-04-20  V1.00.01  Amber      Update:Add throws Exception   
* 111-12-07  V1.00.02  Machao    sync from mega & updated for project coding standard             *
******************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ptrm0650Func extends FuncEdit {
	String mKkBinType = "";
	
	public Ptrm0650Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}
	@Override
	public int querySelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {
		if (this.ibAdd) {
			mKkBinType = wp.itemStr2("kk_bin_type");
		}
		else {
			mKkBinType = wp.itemStr2("bin_type");
		}
		
		log(this.actionCode+", bin_type = "+mKkBinType+", mod_seqno="+wp.modSeqno());
		
		if (isEmpty(mKkBinType)) {
			errmsg("國際組織別不可空白");
			return ;
		}
		if (isNumber(wp.itemStr2("db_v1")) == false) {
			errmsg("主要版本要為數字且不可空白");
			return ;
		}
		if (isNumber(wp.itemStr2("db_v2")) == false) {
			errmsg("次要版本要為數字且不可空白");
			return ;
		}
		if (isNumber(wp.itemStr2("db_v3")) == false) {
			errmsg("版本修訂要為數字且不可空白");
			return ;
		}
		
		if (this.isAdd()){
			//檢查新增資料是否重複
			String lsSql = "select count(*) as tot_cnt from ptr_service_ver where bin_type = ?";
			Object[] param = new Object[] { mKkBinType };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增");
			}
			return;
		}
		else {
			//-other modify-
			sqlWhere = " where bin_type = ?  and nvl(mod_seqno,0) = ?";
			Object[] param = new Object[] { mKkBinType, wp.modSeqno() };
			isOtherModify("ptr_service_ver", sqlWhere, param);
		}
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1){
			return rc;	
		}
		strSql = "insert into ptr_service_ver ("
				+ " bin_type " 
				+ ", service_ver " 
				+ ", mod_time, mod_user, mod_pgm, mod_seqno" 
				+ " ) values ("
				+ " ?, ? "
				+ ",sysdate, ?, ?, 1"  
				+ " )";
		//-set ? value-
		Object[] param = new Object[] { 
			mKkBinType
			, wp.itemStr2("db_v1") + "." + wp.itemStr2("db_v2") + "." + wp.itemStr2("db_v3")
			, wp.loginUser
			, wp.itemStr2("mod_pgm") 
		};
		
		this.log("vendor=" + mKkBinType);
		
		sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
		}

		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc != 1){
			return rc;
		}
		
		strSql = "update ptr_service_ver set "
				+ " service_ver =? " 
				+ ", mod_user =?, mod_time=sysdate, mod_pgm =? "
				+ ", mod_seqno =nvl(mod_seqno,0)+1 " 
				+ sqlWhere;
		Object[] param = new Object[] { 
			wp.itemStr2("db_v1") + "." + wp.itemStr2("db_v2") + "." + wp.itemStr2("db_v3")
			, wp.loginUser
			, wp.itemStr2("mod_pgm") 
			, mKkBinType, wp.modSeqno()
		};
		
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc != 1){
			return rc;
		}
		strSql = "delete ptr_service_ver " + sqlWhere;
		Object[] param = new Object[] { mKkBinType, wp.modSeqno()	};
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
