package ccam02;

import busi.FuncAction;

public class Ccam5510Func extends FuncAction {
	String cellarPhone = "";
	@Override
	public void dataCheck() {
		if(ibAdd) {
			cellarPhone = wp.itemStr("kk_cellar_phone");
		}	else	{
			cellarPhone = wp.itemStr("cellar_phone");
		}
		
		if(empty(cellarPhone)) {
			errmsg("手機號碼: 不可空白");
			return ;
		}
		
		if(ibAdd)
			return ;
		
		sqlWhere = " where cellar_phone = ? and nvl(mod_seqno,0) = ? ";
		Object[] parms = new Object[] {cellarPhone, wp.itemNum("mod_seqno")};
	    if (this.isOtherModify("cca_mobile_black_list", sqlWhere, parms)) {	      
	      return;
	    } 

	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc!=1)
			return rc;
		
		insertMobileBlackList();
		if(rc!=1)
			return rc;
		
		insertMobileBlackListLog("A");
		if(rc!=1)
			return rc;
		
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1)
			return rc;
		
		updateMobileBlackList();
		if(rc!=1)
			return rc;
		
		insertMobileBlackListLog("U");
		if(rc!=1)
			return rc;		
		
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1)
			return rc;
		
		deleteMoblieBlackList();
		if(rc!=1)
			return rc;
		
		insertMobileBlackListLog("D");
		if(rc!=1)
			return rc;
		
		
		return rc;
	}

	@Override
	public int dataProc() {
		msgOK();
		
		deleteMoblieBlackListImport();
		if(rc!=1)
			return rc;
		
		insertMobileBlackListLogImport("D");
		if(rc!=1)
			return rc;
		
		insertMobileBlackListImport();
		if(rc!=1)
			return rc;
		
		insertMobileBlackListLogImport("A");
		if(rc!=1)
			return rc;
		
		return rc;
	}
	
	void insertMobileBlackList() {
		msgOK();
		
		strSql = "insert into cca_mobile_black_list (cellar_phone,remark,crt_date,crt_user,mod_user,mod_pgm,"
				+"mod_time,mod_seqno) values (:cellar_phone,:remark,to_char(sysdate,'yyyymmdd'),:crt_user,:mod_user,"
				+":mod_pgm,sysdate,1) "
				;
		
		setString("cellar_phone",cellarPhone);
		item2ParmStr("remark");
		setString("crt_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("insert cca_mobile_black_list error !");
			return ;
		}
		
	}
	
	void insertMobileBlackListLog(String audCode) {
		msgOK();
		
		strSql = "insert into cca_mobile_black_list_log (cellar_phone,remark,aud_code,crt_date,crt_user) values "
				+"(:cellar_phone,:remark,:aud_code,to_char(sysdate,'yyyymmdd'),:crt_user) ";
		
		setString("cellar_phone",cellarPhone);
		item2ParmStr("remark");
		setString("aud_code",audCode);
		setString("crt_user",wp.loginUser);
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("insert cca_mobile_black_list_log error !");
			return ;
		}
	}		
	
	void updateMobileBlackList() {
		msgOK();
		
		strSql = "update cca_mobile_black_list set remark =:remark , mod_user =:mod_user , mod_pgm =:mod_pgm , "
				+ " mod_time = sysdate , mod_seqno = nvl(mod_seqno,0)+1 where cellar_phone =:cellar_phone and nvl(mod_seqno,0) = :mod_seqno ";				
				
		item2ParmStr("remark");
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		setString("cellar_phone",cellarPhone);
		item2ParmNum("mod_seqno");
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("update cca_mobile_black_list error ");
			return ;
		}		
	}
	
	void deleteMoblieBlackList() {
		msgOK();
		
		strSql = "delete cca_mobile_black_list where cellar_phone =:cellar_phone and nvl(mod_seqno,0) =:mod_seqno ";
		setString("cellar_phone",cellarPhone);
		item2ParmNum("mod_seqno");
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("delete cca_mobile_black_list error ");
			return ;
		}		
	}
	
	void insertMobileBlackListImport() {
		msgOK();
		
		strSql = "insert into cca_mobile_black_list (cellar_phone,remark,crt_date,crt_user,mod_user,mod_pgm,"
				+"mod_time,mod_seqno) values (:cellar_phone,:remark,to_char(sysdate,'yyyymmdd'),:crt_user,:mod_user,"
				+":mod_pgm,sysdate,1) "
				;
		
		setString("cellar_phone",wp.itemStr("cellar_phone"));
		item2ParmStr("remark");
		setString("crt_user",wp.loginUser);
		setString("mod_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("insert cca_mobile_black_list error !");
			return ;
		}
		
	}
	
	void deleteMoblieBlackListImport() {
		msgOK();
		
		strSql = "delete cca_mobile_black_list where cellar_phone =:cellar_phone and nvl(mod_seqno,0) =:mod_seqno ";
		setString("cellar_phone",wp.itemStr("cellar_phone"));
		item2ParmNum("mod_seqno");
		sqlExec(strSql);
		if(sqlRowNum < 0) {
			errmsg("delete cca_mobile_black_list error ");
			return ;
		}		
	}
	
	void insertMobileBlackListLogImport(String audCode) {
		msgOK();
		
		strSql = "insert into cca_mobile_black_list_log (cellar_phone,remark,aud_code,crt_date,crt_user) values "
				+"(:cellar_phone,:remark,:aud_code,to_char(sysdate,'yyyymmdd'),:crt_user) ";
		
		setString("cellar_phone",wp.itemStr("cellar_phone"));
		item2ParmStr("remark");
		setString("aud_code",audCode);
		setString("crt_user",wp.loginUser);
		
		sqlExec(strSql);
		if(sqlRowNum <=0) {
			errmsg("insert cca_mobile_black_list_log error !");
			return ;
		}
	}
	
}
