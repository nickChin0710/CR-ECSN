package ccam02;
/*
 * 特店風險等級參數維護　mcht_rsk_level_code
 2020-0420  V1.00.01 yanghan 修改了變量名稱和方法名稱
 * */

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam3030Func extends FuncEdit {
	String mchtRskLevelCode = "";

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
			mchtRskLevelCode = wp.itemStr("kk_mcht_risk_code");
		} else {
			mchtRskLevelCode = wp.itemStr("mcht_risk_code");
		}

		if (isAdd() || isUpdate()) {
			if (isEmpty(mchtRskLevelCode)) {
				errmsg("特店風險等級：不可空白");
				return;
			}
			if (empty(wp.itemStr("risk_remark"))) {
				errmsg("說明：不可空白");
				return;
			}

			if (wp.itemEmpty("resp_code")) {
				errmsg("回覆碼: 不可空白");
				return;
			}
		}

		if (this.isAdd()) {
			return;
		}

		// -other modify-
		// sql_where = " where sys_id='"+ kk1 + "'"
		// + " and sys_key ='" + kk2 + "'"
		// + " and nvl(mod_seqno,0) =" + wp.mod_seqno();
		sqlWhere = " where mcht_risk_code=?" + " and nvl(mod_seqno,0) =?";

		Object[] parms = new Object[] { mchtRskLevelCode, wp.itemNum("mod_seqno") };
		if (this.isOtherModify("CCA_MCHT_RISK_LEVEL", sqlWhere, parms)) {
			wp.log(sqlWhere, parms);
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

		strSql = "insert into CCA_MCHT_RISK_LEVEL (" + " mcht_risk_code, " // 1
				+ " resp_code, " + " risk_remark, " + " crt_date, crt_user, " + " apr_date, apr_user "
				+ ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
				+ " :mcht_risk_code, :resp_code, :risk_remark " + ",to_char(sysdate,'yyyymmdd'),:crt_user "
				+ ",to_char(sysdate,'yyyymmdd'),:apr_user " + ",sysdate, :mod_user, :mod_pgm, 1" + " )";
		// -set ?value-
		try {
			this.setString("mcht_risk_code", mchtRskLevelCode);
			item2ParmStr("resp_code");
			item2ParmStr("risk_remark");
			setString("crt_user", wp.loginUser);
			setString("apr_user", wp.loginUser);
			setString("mod_user", wp.loginUser);
			setString("mod_pgm", wp.modPgm());
		} catch (Exception ex) {
			wp.log("sqlParm", ex);
		}
		sqlExec(strSql);
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

		strSql = "update CCA_MCHT_RISK_LEVEL set " + " resp_code = :resp_code, " + " risk_remark = :risk_remark, "
				+ " apr_date = to_char(sysdate,'yyyymmdd')," + " apr_user = :apr_user,"
				+ " mod_user = :mod_user, mod_time=sysdate, mod_pgm =:mod_pgm " + ", mod_seqno =nvl(mod_seqno,0)+1 "
				+ " where mcht_risk_code =:kk " + " and nvl(mod_seqno,0) =:mod_seqno";

		item2ParmStr("resp_code");
		item2ParmStr("risk_remark");
		setString("mod_user", wp.loginUser);
		setString("apr_user", wp.loginUser);
		setString("mod_pgm", wp.modPgm());
		item2ParmNum("mod_seqno");
		item2ParmStr("kk", "mcht_risk_code");

		rc = sqlExec(strSql);
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
		strSql = "delete CCA_MCHT_RISK_LEVEL " + " where mcht_risk_code =:kk1 " + " and nvl(mod_seqno,0) =:mod_seqno ";
		// ddd("del-sql="+is_sql);
		setString("kk1", mchtRskLevelCode);
		item2ParmNum("mod_seqno");
		rc = sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
