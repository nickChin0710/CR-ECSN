package ptrm01;

import busi.FuncAction;

public class Ptrm0041Func extends FuncAction {
	String fiscCode = "";	
	@Override
	public void dataCheck() {
		if(ibAdd) {
			fiscCode = wp.itemStr("kk_fisc_code");
		}	else	{
			fiscCode = wp.itemStr("fisc_code");
		}
		
		if(isEmpty(fiscCode)) {
			errmsg("財金代碼不可空白");
			return ;
		}
		
		if(wp.itemEmpty("tcb_bin")) {
			errmsg("Bin No : 不可空白");
			return ;
		}
		
		if(wp.itemStr("tcb_bin").length() != 6) {
			errmsg("Bin No 需為 6 碼");
			return ;
		}
		
		if(ibAdd) {
			if(checkDup()) {
				errmsg("此財金代碼已有對應的 bin_no , 不可重覆建立 !");
				return ;
			}
		}
	}
	
	boolean checkDup() {		
		String sql1 = "select count(*) as db_cnt from fsc_bin_group where fisc_code = ? ";
		sqlSelect(sql1,new Object[] {fiscCode});		
		if(colNum("db_cnt") > 0) {
			return true ;
		}		
		return false ;
	}
	
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc!=1)
			return rc;
		
		strSql = " insert into fsc_bin_group (fisc_code , tcb_bin ) values (:fisc_code , :tcb_bin)";
		setString("fisc_code",fiscCode);
		item2ParmStr("tcb_bin");
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg(sqlErrtext);
		}		
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1)
			return rc;
		
		strSql = "update fsc_bin_group set tcb_bin =:tcb_bin where fisc_code =:fisc_code ";
		item2ParmStr("tcb_bin");
		setString("fisc_code",fiscCode);
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg(sqlErrtext);
		}	
		
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1)
			return rc;
		
		strSql = "delete fsc_bin_group where fisc_code =:fisc_code ";
		setString("fisc_code",fiscCode);
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg(sqlErrtext);
		}
		
		return rc;
	}

	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

}
