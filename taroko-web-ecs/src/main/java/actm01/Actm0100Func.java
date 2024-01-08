/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 109-09-03  V1.00.01  Andy       update Mantis4001                          *
* 111-03-29	           JH		      nvl(curr_code,'901')                       *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-07-04  V1.00.04  Simon      TCB溢付款退款客製化                        * 
******************************************************************************/

package actm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0100Func extends FuncEdit {
	String pPSeqno = "";
	String mAccttype="";
    String mAcctkey="";
    String mCardno="";
    String mCurrcode = "";
    String kkRowid = "";

	public Actm0100Func(TarokoCommon wr) {
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
		// check PK
		if (this.isAdd()) {
			pPSeqno = wp.itemStr2("p_seqno");
			if (empty(pPSeqno)) {  //手動新增
				if (wfGetPseqno()<0) return;
			} else {  //Acct_curr 有值
				mCurrcode = wp.itemNvl("kp_curr_code","901");
				mAccttype = wp.itemStr2("kp_acct_type");
				mAcctkey = wp.itemStr2("kp_acct_key");
				mCardno = wp.itemStr2("kp_card_no");
			}
		}
		
		kkRowid = wp.itemStr2("rowid");
		
		if (this.isAdd()){
			//檢查新增資料是否重複
//			String lsSql = "select count(*) as tot_cnt from act_acaj where p_seqno = ? and curr_code = ? ";
//			Object[] param = new Object[] { p_p_seqno, m_currcode };
//			sqlSelect(lsSql, param);
//			if (col_num("tot_cnt") > 0) {
//				errmsg("資料已存在，無法新增");
//			}
//			return;
		} else {
			//-other modify-
			sqlWhere = "where hex(rowid) = ? "
					  + "and nvl(mod_seqno,0) = ?";
			Object[] param = new Object[] { kkRowid, wp.modSeqno() };
			isOtherModify("act_acaj", sqlWhere, param);
		}
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc!=1) return rc;

		if (empty(mCurrcode)) mCurrcode="901";
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_acaj");
		sp.ppstr("p_seqno", pPSeqno);
		sp.ppstr("acct_type", mAccttype);
		sp.ppstr("card_no", mCardno);
		sp.ppstr("curr_code", mCurrcode);
		wp.log("B:vars_num('dr_amt') = "+varsNum("dr_amt"),"");
		sp.ppnum("dr_amt", varsNum("dr_amt"));
		sp.ppnum("cr_amt", 0);
		sp.ppnum("bef_amt", isEmpty(wp.itemStr2("bef_amt")) ? 0 : wp.itemNum("bef_amt"));
		sp.ppnum("aft_amt", varsNum("aft_amt"));
		sp.ppnum("bef_d_amt", isEmpty(wp.itemStr2("bef_d_amt")) ? 0 : wp.itemNum("bef_d_amt"));
		sp.ppnum("aft_d_amt", varsNum("aft_d_amt"));
		sp.ppnum("dc_dr_amt", isEmpty(wp.itemStr2("dc_dr_amt")) ? 0 : wp.itemNum("dc_dr_amt"));
		sp.ppnum("dc_cr_amt", 0);
		sp.ppnum("dc_bef_amt",isEmpty(wp.itemStr2("dc_bef_amt")) ? 0 : wp.itemNum("dc_bef_amt"));
		sp.ppnum("dc_aft_amt", varsNum("dc_aft_amt"));
		sp.ppnum("dc_bef_d_amt", isEmpty(wp.itemStr2("dc_bef_d_amt")) ? 0 : wp.itemNum("dc_bef_d_amt"));
		sp.ppnum("dc_aft_d_amt", varsNum("dc_aft_d_amt"));
		sp.ppstr("cash_type", wp.itemStr2("cash_type"));
	//sp.ppstr("trans_acct_key", wp.itemStr2("trans_acct_key"));
		sp.ppstr("mcht_no", varsStr("bank_no"));
		sp.ppstr("trans_acct_key", varsStr("trans_acct_key"));
		sp.ppstr("adj_comment", varsStr("adj_comment"));
		sp.ppstr("apr_flag", "N");
		sp.ppstr("job_code", varsStr("job_code"));
		sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
		sp.ppstr("adjust_type", "OP02");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ",", sysdate ");
		sp.addsql(",crt_date", ",to_char(sysdate,'yyyymmdd')");
		sp.addsql(",crt_time", ",to_char(sysdate,'hh24miss')");
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("update_date", wp.sysDate);
		sp.ppstr("update_user", wp.loginUser);
		sp.ppstr("mod_seqno", "1");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1) return rc;
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_acaj");
		sp.ppnum("dc_dr_amt", isEmpty(wp.itemStr2("dc_dr_amt")) ? 0 : wp.itemNum("dc_dr_amt"));
		sp.ppnum("dr_amt", varsNum("dr_amt"));
		sp.ppstr("cash_type", wp.itemStr2("cash_type"));
  //sp.ppstr("trans_acct_key", wp.itemStr2("trans_acct_key"));
		sp.ppstr("mcht_no", varsStr("bank_no"));
		sp.ppstr("trans_acct_key", varsStr("trans_acct_key"));
		sp.ppstr("adj_comment", varsStr("adj_comment"));
		sp.ppstr("apr_flag", "N");
		sp.ppnum("aft_amt", varsNum("aft_amt"));
		sp.ppnum("dc_aft_amt", varsNum("dc_aft_amt"));
		sp.ppnum("aft_d_amt", varsNum("aft_d_amt"));
		sp.ppnum("dc_aft_d_amt", varsNum("dc_aft_d_amt"));
		sp.ppstr("job_code", varsStr("job_code"));
		sp.ppstr("vouch_job_code", varsStr("vouch_job_code"));
//	sp.ppss("crt_date", wp.sysDate);
//	sp.ppss("crt_time", wp.sysTime);
		sp.ppstr("update_date", wp.sysDate);
		sp.ppstr("update_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where hex(rowid) = ?", kkRowid);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1) return rc;
		
		strSql = "delete act_acaj "
				+ sqlWhere;
		Object[] param = new Object[] { kkRowid, wp.modSeqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	int wfGetPseqno()  {
		mAccttype = wp.itemStr2("kp_acct_type");
		mAcctkey = wp.itemStr2("kp_acct_key");
		mCardno = wp.itemStr2("kp_card_no");
		mCurrcode = wp.itemNvl("kp_curr_code","901");
		
		if(empty(mAcctkey) && empty(mCardno)) {
			errmsg("帳號, 卡號不可均為空白");
			return -1;
		}
		if(empty(mCurrcode)) {
			errmsg("結算幣別不可為空白");
			return -1;
		}
		
		//以acct_type, acct_key 優先查詢
		if(empty(mAcctkey)==false) {
			if(empty(mAccttype)) {
				errmsg("請輸入帳號代碼");
				return -1;
			}
			strSql  = " select p_seqno from act_acno ";
			strSql += " where acct_type = ? and acct_key = ? ";
			strSql += " and acno_p_seqno = p_seqno ";
			Object[] param = new Object[] { mAccttype, mAcctkey };
			sqlSelect(strSql, param);
			if (sqlNotfind) {
				errmsg("此帳號不存在!");
				return -1;
			}
			pPSeqno = colStr("p_seqno");
		} else {
			strSql  = " select p_seqno from crd_card ";
			strSql += " where card_no = ? ";
			Object[] param = new Object[] { mCardno };
			sqlSelect(strSql, param);
			if (sqlNotfind) {
				errmsg("此卡號不存在!");
				return -1;
			}
			pPSeqno = colStr("p_seqno");
		}
		return 0;
    }

}
