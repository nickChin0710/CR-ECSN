/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5120Func extends FuncEdit {
String binType = "", regnCode = "";

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
		binType = wp.itemStr("kk_bin_type");
	}
	else {
		binType = wp.itemStr("bin_type");
	}
	if (this.ibAdd) {
		regnCode = wp.itemStr("kk_regn_code");
	}
	else {
		regnCode = wp.itemStr("regn_code");
	}
	if (empty(binType)) {
		errmsg("卡別：不可空白");
		return;
	}
	if (empty(regnCode)) {
		errmsg("Region代碼：不可空白");
		return;
	}
	if (wp.itemEmpty("regn_desc")) {
		errmsg("說明：不可空白");
		return;
	}
	if (this.isAdd()) {
		return;
	}
	sqlWhere = " where sys_id=? and sys_key =? and nvl(mod_seqno,0) = ? ";
	Object[] parms = new Object[] {binType, regnCode, wp.itemNum("mod_seqno")};
	if (this.isOtherModify("CCA_SYS_PARM2", sqlWhere,parms)) {
		return;
	}
}

@Override
public int dbInsert() {
	actionInit("A");
	dataCheck();
	if (rc != 1) {
		return rc;
	}

	strSql = "insert into CCA_SYS_PARM2 ("
			+ " sys_id, " // 1
			+ " sys_key, "
			+ " sys_data1, " // 3
			+ " crt_date, crt_user, "
			+ " apr_date, apr_user "
			+ ", mod_time, mod_user, mod_pgm, mod_seqno"
			+ " ) values ("
			+ " ?,?,? "
			+ ",to_char(sysdate,'yyyymmdd'),? "
			+ ",to_char(sysdate,'yyyymmdd'),? "
			+ ",sysdate,?,?,1"
			+ " )";
	Object[] param = new Object[] {
			binType // 1
			,
			regnCode,
			wp.itemStr("regn_desc"),
			modUser,
			modUser,
			modUser,
			modPgm // 7
	};

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
	if (rc != 1) {
		return rc;
	}

	strSql =
			"update CCA_SYS_PARM2 set "
					+ " sys_data1 =?, "
					+ " mod_user =?, mod_time=sysdate, mod_pgm =? "
					+ ", mod_seqno =nvl(mod_seqno,0)+1 "
					+ " where sys_id=? and sys_key =? and nvl(mod_seqno,0) = ?";
	Object[] param = new Object[] {
			wp.itemStr("regn_desc"),
			modUser,
			modPgm,
			binType, regnCode, wp.itemNum("mod_seqno")
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
	if (rc != 1) {
		return rc;
	}
	strSql = "delete CCA_SYS_PARM2 where sys_id=? and sys_key =? and nvl(mod_seqno,0) = ? ";
	Object[] param = new Object[] {binType, regnCode, wp.itemNum("mod_seqno")};
	rc = sqlExec(strSql,param);
	if (sqlRowNum <= 0) {
		errmsg(this.sqlErrtext);
	}

	return rc;
}

}
