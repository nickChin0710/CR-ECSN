/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-09-14  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Ptrm0182Func extends FuncEdit {

	String mailType = "";
	String minMailNo = "";
	String maxMailNo = "";
//	public boolean updateFlag = true;
	public Ptrm0182Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	// ************************************************************************
	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	// ************************************************************************
	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 1;
	}

	// ************************************************************************
	@Override
	public void dataCheck() {
		mailType = wp.itemStr("mail_type");
		minMailNo = wp.itemStr("min_mail_no");
		maxMailNo = wp.itemStr("max_mail_no");
		
		if (empty(mailType))
			mailType = wp.itemStr("kk_mail_type");
		if (empty(minMailNo))
			minMailNo = wp.itemStr("kk_min_mail_no");
		if (empty(maxMailNo))
			maxMailNo = wp.itemStr("kk_max_mail_no");

		if (empty(mailType) || empty(minMailNo) || empty(maxMailNo)) {
			errmsg("郵寄種類、掛號號碼(MIN)、掛號號碼(MAX) 不可為空白");
			return;
		}
		
		if(minMailNo.length()<6 || maxMailNo.length()<6) {
			errmsg("掛號號碼(MIN)、掛號號碼(MAX) 長度須為六碼");
			return;
		}

		if(wp.itemLen("used_max_mail_no")<6) {
			errmsg("已使用最大掛號號碼  長度須為六碼");
			return;
		}
		
		if((int)wp.itemNum("used_max_mail_no")< (this.strToInt(minMailNo)-1)
				|| wp.itemStr("used_max_mail_no").compareTo(maxMailNo) > 0) {
			errmsg("已使用最大掛號號碼不可超出該區間");
			return;
		}
		
		if (this.ibAdd) {
			// 掛號號碼(MIN)不可存在已設定的掛號號碼區間
			String lsSql = "select count(*) as tot_cnt from crd_mailno_range where mail_type = ? and min_mail_no <= ? and max_mail_no >= ? ";
			Object[] param = new Object[] { mailType, minMailNo, minMailNo };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("此掛號號碼(MIN)已存在設定範圍內，不可重複指定");
				return;
			}

			// 掛號號碼(MAX)不可存在已設定的掛號號碼區間
			lsSql = "select count(*) as tot_cnt from crd_mailno_range where mail_type = ? and min_mail_no <= ? and max_mail_no >= ? ";
			param = new Object[] { mailType, maxMailNo, maxMailNo };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("此掛號號碼(MAX)已存在設定範圍內，不可重複指定");
				return;
			}

			// 檢查新增資料是否重複
			lsSql = "select count(*) as tot_cnt from crd_mailno_range where mail_type = ? and min_mail_no = ? and max_mail_no = ? ";
			param = new Object[] { mailType, minMailNo, maxMailNo };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增");
				return;
			}
		} else {

			// -other modify-
			sqlWhere = " where mail_type = ? and min_mail_no = ? and max_mail_no = ? ";
			Object[] param = new Object[] { mailType, minMailNo, maxMailNo };
			if (this.isOtherModify("crd_mailno_range", sqlWhere, param)) {
				errmsg("資料已被異動 or 不存在，請重新查詢 !");
				return;
			}
		}
	}

	// ************************************************************************
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1)
			return rc;

		strSql = " insert into  crd_mailno_range ( mail_type, " + " min_mail_no, " + " max_mail_no,"
				+ " inuse_flag, " + " used_max_mail_no, "
				+ " crt_date,mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,sysdate,?,?)";

		Object[] param = new Object[] { mailType, minMailNo, maxMailNo,
				wp.itemStr("inuse_flag"), wp.itemStr("used_max_mail_no"), getSysDate(),
				wp.loginUser, wp.modPgm() };

		sqlExec(strSql, param);

		return rc;
	}

	// ************************************************************************
	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if (rc != 1)
			return rc;

		strSql = "update crd_mailno_range set " + "inuse_flag = ?, " + "used_max_mail_no = ?, "
				+ "mod_user = ?, " + "mod_time  = sysdate, " + "mod_pgm   = ? " + sqlWhere;

		Object[] param = new Object[] { wp.itemStr("inuse_flag"), wp.itemStr("used_max_mail_no"),
				wp.loginUser, wp.itemStr("mod_pgm"), mailType,
				minMailNo, maxMailNo };

		sqlExec(strSql, param);

		return rc;
	}

	// ************************************************************************
	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if (rc != 1)
			return rc;

		strSql = "delete crd_mailno_range " + sqlWhere;

		Object[] param = new Object[] { mailType, minMailNo, maxMailNo };

		sqlExec(strSql, param);

		return rc;
	}
	// ************************************************************************

} // End of class
