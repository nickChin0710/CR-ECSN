/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-10  V1.00.00  Andy Liu      program initial                         *
* 111-10-24  V1.00.01  Yang Bo    sync code from mega                        *
******************************************************************************/

package actp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Actp0030Func extends FuncEdit {
	String mKkMchtNo = "";

	public Actp0030Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {
//		// check PK
//		if (this.ib_add) {
//			if (empty(wp.item_ss("kk_mcht_no")) == false)
//				m_kk_mcht_no = wp.item_ss("kk_mcht_no");
//			else {
//				errmsg("請輸入特店代號!!");
//				return;
//			}
//		} else {
//			m_kk_mcht_no = wp.item_ss("mcht_no");
//		}
//
//		// check duplicate
//		if (this.isAdd()) {
//			// 檢查新增資料是否重複
//			String lsSql = "select count(*) as tot_cnt from bil_model_parm where mcht_no = ? ";
//			Object[] param = new Object[] { m_kk_mcht_no };
//			sqlSelect(lsSql, param);
//			if (col_num("tot_cnt") > 0) {
//				errmsg("資料已存在，無法新增");
//			}
//			return;
//		}
//
//		// -other modify-
//		sql_where = " where mcht_no= ? "
//				+ " and nvl(mod_seqno,0) = ? ";
//		Object[] param = new Object[] { m_kk_mcht_no, wp.mod_seqno() };
//		if (this.other_modify("bil_model_parm", sql_where, param)) {
//			errmsg("請重新查詢 !");
//			return;
//		}
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		strSql = "";
		// -set ?value-
		Object[] param = new Object[] {};
//		Object[] param1 = param;
//		System.out.println("is_sql:" + is_sql);
//		for (int i = 0; i <= param1.length; i++) {
//			System.out.println(param1[i] + ",");
//		}
		sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
		}

		return rc;
	}

	public int dbInsert(String pSql, Object[] pParam) throws Exception {
		actionInit("A");
		if (rc != 1) {
			return rc;
		}

		strSql = pSql;

		Object[] param = pParam;

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

		strSql = "";

		Object[] param = new Object[] {};

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	public int dbUpdate(String pSql, Object[] pParam) throws Exception {
		actionInit("U");
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		strSql = pSql;

		Object[] param = pParam;

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		return rc;
	}

	public int dbDelete(String pSeqno) throws Exception {
		actionInit("D");

		strSql = "delete ACT_MODDATA_TMP where p_seqno = ? and ACT_MODTYPE='02'";

		Object[] param = new Object[] { pSeqno };

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
