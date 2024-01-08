/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-09-13  V1.00.01  Ryan       Initial                              *
***************************************************************************/
package ptrm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Ptrm0181Func extends FuncEdit {

	String mailType = "";
	String minCardSheets = "";
	String maxCardSheets = "";

	public Ptrm0181Func(TarokoCommon wr) {
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
		minCardSheets = wp.itemStr("min_card_sheets");
		maxCardSheets = wp.itemStr("max_card_sheets");

		if (empty(mailType))
			mailType = wp.itemStr("kk_mail_type");
		if (empty(minCardSheets))
			minCardSheets = wp.itemStr("kk_min_card_sheets");
		if (empty(maxCardSheets))
			maxCardSheets = wp.itemStr("kk_max_card_sheets");

		if (empty(mailType) || empty(minCardSheets) || empty(maxCardSheets)) {
			errmsg("郵寄種類、卡片張數(MIN)、卡片張數(MAX) 不可為空白");
			return;
		}

		if (this.ibAdd) {
			// 卡片張數(MIN)不可存在已設定的卡片張數區間
			String lsSql = "select count(*) as tot_cnt from crd_postage where mail_type = ? and min_card_sheets <= ? and max_card_sheets >= ? ";
			Object[] param = new Object[] { mailType, minCardSheets, minCardSheets };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("此卡片張數(MIN)已存在設定範圍內，不可重複指定");
				return;
			}

			// 卡片張數(MAX)不可存在已設定的卡片張數區間
			lsSql = "select count(*) as tot_cnt from crd_postage where mail_type = ? and min_card_sheets <= ? and max_card_sheets >= ? ";
			param = new Object[] { mailType, maxCardSheets, maxCardSheets };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("此卡片張數(MAX)已存在設定範圍內，不可重複指定");
				return;
			}

			// 檢查新增資料是否重複
			lsSql = "select count(*) as tot_cnt from crd_postage where mail_type = ? and min_card_sheets = ? and max_card_sheets = ? ";
			param = new Object[] { mailType, minCardSheets, maxCardSheets };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增");
				return;
			}
		} else {

			// -other modify-
			sqlWhere = " where mail_type = ? and min_card_sheets = ? and max_card_sheets = ? ";
			Object[] param = new Object[] { mailType, minCardSheets, maxCardSheets };
			if (this.isOtherModify("crd_postage", sqlWhere, param)) {
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

		strSql = " insert into  crd_postage ( mail_type, " + " min_card_sheets, " + " max_card_sheets,"
				+ " bigtaipei_area_post, " + " other_area_post, " + " env_weight, " + " card_weight, "
				+ " crt_date,mod_time,mod_user,mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,sysdate,?,?)";

		Object[] param = new Object[] { mailType, minCardSheets, maxCardSheets, wp.itemNum("bigtaipei_area_post"),
				wp.itemNum("other_area_post"), wp.itemNum("env_weight"), wp.itemNum("card_weight"), getSysDate(),
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

		strSql = "update crd_postage set " + "bigtaipei_area_post = ?, " + "other_area_post = ?, " + "env_weight = ?, "
				+ "card_weight = ?, " + "mod_user = ?, " + "mod_time  = sysdate, " + "mod_pgm   = ? " + sqlWhere;

		Object[] param = new Object[] { wp.itemNum("bigtaipei_area_post"), wp.itemNum("other_area_post"),
				wp.itemNum("env_weight"), wp.itemNum("card_weight"), wp.loginUser, wp.itemStr("mod_pgm"), mailType,
				minCardSheets, maxCardSheets };

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

		strSql = "delete crd_postage " + sqlWhere;

		Object[] param = new Object[] { mailType, minCardSheets, maxCardSheets };

		sqlExec(strSql, param);

		return rc;
	}
	// ************************************************************************

} // End of class
