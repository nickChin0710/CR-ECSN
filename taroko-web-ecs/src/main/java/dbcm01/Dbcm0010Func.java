/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-15  V1.00.00  yash       program initial                            *
* 107-09-20  V1.00.01  Andy       Update                                     *
*  110/1/4  V1.00.02  yanghan       修改了變量名稱和方法名稱            *
* 112-05-10  V1.00.02  Wilson     取消mark other modify                        *
******************************************************************************/

package dbcm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Dbcm0010Func extends FuncEdit {
	String mKkBatchno = "";
	String mKkRecno = "";

	public Dbcm0010Func(TarokoCommon wr) {
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
		if (this.isAdd()) {
			// m_kk_batchno = wp.item_ss("kk_batchno");
		} else {
			mKkBatchno = wp.itemStr2("batchno");
			mKkRecno = wp.itemStr2("recno");
		}
		if (this.isAdd()) {

		} else {
			// -other modify-
			sqlWhere = " where batchno = ? and recno=?  and nvl(mod_seqno,0) = ?";
			Object[] param = new Object[] { mKkBatchno, mKkRecno, wp.modSeqno() };
			isOtherModify("dbc_emboss_tmp", sqlWhere, param);
		}
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		if ("2".equals(wp.itemStr2("emboss_reason"))) {
			rc = dbUpdateDbcCard("A");
			if (rc != 1)
				return rc;
		}

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("dbc_emboss_tmp");
		if ("2".equals(wp.itemStr2("emboss_reason")) && !empty(wp.itemStr2("old_card_no"))) {
			sp.ppstr("card_no", wp.itemStr2("old_card_no"));
		}
		sp.ppstr("old_card_no", wp.itemStr2("old_card_no"));
		sp.ppstr("card_type", wp.itemStr2("card_type"));
		sp.ppstr("batchno", wp.itemStr2("batchno"));
		sp.ppnum("recno", wp.itemNum("recno"));
		sp.ppstr("emboss_source", wp.itemStr2("emboss_source"));
		sp.ppstr("ic_flag", wp.itemStr2("ic_flag"));
		sp.ppstr("emboss_reason", wp.itemStr2("emboss_reason"));
		sp.ppstr("force_flag", wp.itemStr2("force_flag"));
		sp.ppstr("reissue_code", wp.itemStr2("reissue_code"));
		sp.ppstr("digital_flag", wp.itemStr2("digital_flag"));
		sp.ppstr("redo_flag", wp.itemStr2("redo_flag"));
		sp.ppstr("chi_name", wp.itemStr2("chi_name"));
		sp.ppstr("apply_id", wp.itemStr2("apply_id"));
		sp.ppstr("apply_id_code", wp.itemStr2("apply_id_code"));
//		sp.ppss("expire_chg_flag", wp.item_ss("expire_chg_flag"));		dbc_emboss_tmp無此欄位
		sp.ppstr("eng_name", wp.itemStr2("eng_name"));
		sp.ppstr("sup_flag", wp.itemStr2("sup_flag"));
		sp.ppstr("corp_no", wp.itemStr2("corp_no"));
		sp.ppstr("unit_code", wp.itemStr2("unit_code"));
		sp.ppstr("group_code", wp.itemStr2("group_code"));
		sp.ppstr("old_beg_date", wp.itemStr2("old_beg_date"));
		sp.ppstr("old_end_date", wp.itemStr2("old_end_date"));
		sp.ppstr("major_valid_fm", wp.itemStr2("major_valid_fm"));
		sp.ppstr("major_valid_to", wp.itemStr2("major_valid_to"));
		sp.ppstr("valid_fm", wp.itemStr2("valid_fm"));
		sp.ppstr("valid_to", wp.itemStr2("valid_to"));
		sp.ppstr("emboss_4th_data", wp.itemStr2("emboss_4th_data"));
		sp.ppstr("receipt_branch", wp.itemStr2("receipt_branch"));
		sp.ppstr("act_no", wp.itemStr2("act_no"));
		sp.ppstr("receipt_remark", wp.itemStr2("receipt_remark"));
		// --------------hidden cols --------------------
		sp.ppstr("branch", wp.itemStr2("branch"));
		sp.ppstr("reg_bank_no", wp.itemStr2("reg_bank_no"));
		sp.ppstr("mail_type", wp.itemStr2("mail_type"));
		sp.ppstr("major_card_no", wp.itemStr2("major_card_no"));
		sp.ppstr("pm_id", wp.itemStr2("pm_id"));
		sp.ppstr("pm_id_code", wp.itemStr2("pm_id_code"));
		sp.ppstr("apply_ibm_id_code", wp.itemStr2("apply_ibm_id_code"));
		sp.ppstr("pm_ibm_id_code", wp.itemStr2("pm_ibm_id_code"));
		sp.ppstr("reason_code", wp.itemStr2("reason_code"));
		sp.ppstr("to_nccc_code", wp.itemStr2("to_nccc_code"));
		sp.ppstr("nccc_type", wp.itemStr2("nccc_type"));
		sp.ppstr("acct_type", wp.itemStr2("acct_type"));
		sp.ppstr("acct_key", "");
		sp.ppstr("source_code", wp.itemStr2("source_code"));
		sp.ppstr("member_id", wp.itemStr2("member_id"));
		sp.ppstr("service_code", wp.itemStr2("service_code"));
		sp.ppstr("nation", wp.itemStr2("nation"));
		sp.ppnum("credit_lmt", wp.itemNum("credit_amt"));
		sp.ppstr("birthday", wp.itemStr2("birthday"));
		sp.ppstr("voice_passwd", wp.itemStr2("voice_passwd"));
		sp.ppstr("sex", wp.itemStr2("sex"));
		sp.ppstr("age_indicator", wp.itemStr2("age_indicator"));
		sp.ppstr("status_code", wp.itemStr2("status_code"));
		sp.ppstr("bin_no", wp.itemStr2("bin_no"));
		sp.ppstr("corp_no_code", "");
		sp.ppstr("corp_no_ecode", "");
		sp.ppstr("resend_note", "");
		sp.ppstr("chg_addr_flag", "");
		sp.ppstr("fee_code", "");
		sp.ppnum("standard_fee", 0);
		sp.ppstr("member_note", "");
		sp.ppstr("change_reason", "");
		sp.ppstr("business_code", "");
		sp.ppstr("fee_reason_code", "");
		sp.ppnum("annual_fee", 0);
		sp.ppstr("cardno_code", "");
		sp.ppstr("fee_date", "");
		sp.ppstr("emboss_date", "");
		sp.ppstr("nccc_batchno", "");
		sp.ppnum("nccc_recno", 0);
		sp.ppstr("risk_bank_no", "");
		sp.ppstr("vip", "");
		sp.ppstr("mail_attach1", "");
		sp.ppstr("mail_attach2", "");
		sp.ppstr("crt_date", getSysDate());
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_pgm", wp.itemStr2("mod_pgm"));
		sp.ppstr("mod_seqno", "1");

		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
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
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("dbc_emboss_tmp");
		sp.ppstr("emboss_reason", wp.itemStr2("emboss_reason"));
		sp.ppstr("reissue_code", wp.itemStr2("reissue_code"));
		sp.ppstr("digital_flag", wp.itemStr2("digital_flag"));
		sp.ppstr("redo_flag", wp.itemStr2("redo_flag"));
		sp.ppstr("eng_name", wp.itemStr2("eng_name"));
		sp.ppstr("unit_code", wp.itemStr2("unit_code"));
		sp.ppstr("valid_fm", wp.itemStr2("valid_fm"));
		sp.ppstr("valid_to", wp.itemStr2("valid_to"));
		sp.ppstr("emboss_4th_data", wp.itemStr2("emboss_4th_data"));
		sp.ppstr("receipt_branch", wp.itemStr2("receipt_branch"));
		sp.ppstr("receipt_remark", wp.itemStr2("receipt_remark"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time =sysdate", ", mod_seqno =nvl(mod_seqno,0)+1");
		sp.sql2Where(" where hex(rowid)=?", wp.itemStr2("rowid"));
		sp.sql2Where(" and mod_seqno=?", wp.itemStr2("mod_seqno"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
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

		if ("2".equals(wp.itemStr2("emboss_reason"))) {
			rc = dbUpdateDbcCard("D");
			if (rc != 1)
				return rc;
		}

		strSql = "delete dbc_emboss_tmp " + sqlWhere;
		Object[] param = new Object[] { mKkBatchno, mKkRecno, wp.modSeqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

	public int dbUpdateDbcCard(String type) {
		strSql = "update dbc_card set ";
		if ("A".equals(type)) {
			strSql += "current_code = '4', " + "oppost_reason = 'S1', " + "oppost_date = to_char(sysdate,'yyyymmdd') ";
		} else {
			strSql += "current_code = '0', " + "oppost_reason = '', " + "oppost_date = '' ";
		}
		strSql += "where 1=1 " + "and card_no = ? ";

		Object[] param = new Object[] { wp.itemStr2("old_card_no") };

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}

		return rc;
	}
}
