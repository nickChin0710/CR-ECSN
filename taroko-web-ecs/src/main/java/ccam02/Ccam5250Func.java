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

public class Ccam5250Func extends FuncEdit {
String binNo = "";

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
		binNo = wp.itemStr("kk_bin_no");
	}
	else {
		binNo = wp.itemStr("bin_no");
	}
	if (empty(binNo)) {
		errmsg("Bin no ： 不可空白");
		return;
	}
	if (this.isAdd()) {
		return;
	}
	sqlWhere = " where bin_no= ? and nvl(mod_seqno,0) = ? ";
	Object[] parms = new Object[] {binNo, wp.itemNum("mod_seqno")};
	if (this.isOtherModify("CCA_debit_parm", sqlWhere,parms)) {
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

	strSql = "insert into CCA_debit_parm ("
		+ " bin_no, " // 1
		+ " no_connect_flag, "
		+ " day_cnt, "
		+ " cnt_amount, "
		+ " day_amount, "
		+ " month_amount, "
		+ " withdraw_fee, "
		+ " markup, " // 8
		+ " open_chk , "
		+ " mcht_chk , "
		+ " oversea_chk , "
		+ " avg_consume_chk , "
		+ " month_risk_chk , "
		+ " day_risk_chk , "
		+ " crt_date, crt_user, "
		+ " apr_date, apr_user "
		+ ", mod_time, mod_user, mod_pgm, mod_seqno"
		+ " ) values ("
		+ " ?,?,?,?,?,?,?,? "
		+ ",?,?,?,?,?,? "
		+ ",to_char(sysdate,'yyyymmdd'),? "
		+ ",to_char(sysdate,'yyyymmdd'),? "
		+ ",sysdate,?,?,1"
		+ " )";
	Object[] param = new Object[] {
		binNo, // 1
		wp.itemNvl("no_connect_flag","N"),
		wp.itemNum("day_cnt"),
		wp.itemNum("cnt_amount"),
		wp.itemNum("day_amount"),
		wp.itemNum("month_amount"),
		wp.itemNum("withdraw_fee"),
		wp.itemNum("markup"),		//8
		wp.itemNvl("open_chk","0"),
		wp.itemNvl("mcht_chk","0"),
		wp.itemNvl("oversea_chk","0"),
		wp.itemNvl("avg_consume_chk","0"),
		wp.itemNvl("month_risk_chk","0"),
		wp.itemNvl("day_risk_chk","0"),
		modUser,modUser,
		modUser,modPgm 	//
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
		"update CCA_debit_parm set "
			+ " no_connect_flag =?, "
			+ " day_cnt =?, "
			+ " cnt_amount =?, "
			+ " day_amount =?, "
			+ " month_amount =?, "
			+ " withdraw_fee =?, "			
			+ " markup =?, "
			+ " open_chk =? ,"
			+ " mcht_chk =? ,"
			+ " oversea_chk =? ,"
			+ " avg_consume_chk =? ,"
			+ " month_risk_chk =? ,"
			+ " day_risk_chk =? ,"
			+ " mod_user =?, mod_time=sysdate, mod_pgm =? "
			+ ", mod_seqno =nvl(mod_seqno,0)+1 "
			+ " where bin_no= ? and nvl(mod_seqno,0) = ? ";
	Object[] param = new Object[] {
		wp.itemNvl("no_connect_flag","N"),
		wp.itemNum("day_cnt"),
		wp.itemNum("cnt_amount"),
		wp.itemNum("day_amount"),
		wp.itemNum("month_amount"),
		wp.itemNum("withdraw_fee"),
		wp.itemNum("markup"),
		wp.itemNvl("open_chk","0"),
		wp.itemNvl("mcht_chk","0"),
		wp.itemNvl("oversea_chk","0"),
		wp.itemNvl("avg_consume_chk","0"),
		wp.itemNvl("month_risk_chk","0"),
		wp.itemNvl("day_risk_chk","0"),
		modUser,modPgm ,
		binNo, wp.itemNum("mod_seqno")
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
	strSql = "delete CCA_debit_parm where bin_no= ? and nvl(mod_seqno,0) = ? ";
	Object[] parms = new Object[] {binNo, wp.itemNum("mod_seqno")};
	rc = sqlExec(strSql,parms);
	if (sqlRowNum <= 0) {
		errmsg(this.sqlErrtext);
	}

	return rc;
}

}
