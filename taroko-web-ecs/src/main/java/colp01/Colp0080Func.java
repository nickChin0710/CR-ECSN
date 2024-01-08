/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Zhanghuheng     updated for project coding standard *
******************************************************************************/
package colp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colp0080Func extends FuncProc {
	String pSeqno;
	// colm0030 -->做覆核 mod by phopho 2018.12.25 --> colp0080
	// modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要修改COL_BAD_TRANS，覆核欄位。
	String kkTransType;

	public Colp0080Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

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
		pSeqno = varsStr("p_seqno");
		kkTransType = varsStr("trans_type");

		// -other modify-
		sqlWhere = " where p_seqno= ? " + " and nvl(mod_seqno,0) = ? ";
		Object[] param = new Object[] { pSeqno, varModseqno() };
		if (isOtherModify("col_wait_trans", sqlWhere, param)) {
			return;
		}
	}

	@Override
	public int dataProc() {
		dataCheck();
		if (rc != 1) {
			return rc;
		}

		rc = updateFunc();
		if (rc != 1) {
			return rc;
		}

		return rc;
	}

	int updateFunc() {
		if (rc != 1) {
			return rc;
		}
		String corpSeqno = varsStr("corp_p_seqno");
		String acnoFlag = varsStr("acno_flag");
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_wait_trans");
		sp.ppstr("apr_flag", "Y");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.addsql(", apr_time = to_char(sysdate,'hh24miss') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where p_seqno=?", pSeqno);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", varModseqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}

		// 判斷之前的操作是否成功，若不成功則退出
		if (rc != 1) {
			return rc;
		}
		// 商務戶 corp_p_seqno有值 、acnoFlag為2 修改col_wait_trans
		if (eqIgno(acnoFlag, "2") && (corpSeqno != null && corpSeqno.length() != 0)) {
			// System.out.println("111corpSeqno"+corpSeqno);
			strSql = " update col_wait_trans set " + "  apr_flag= 'Y'," + "  apr_date = to_char(sysdate,'yyyymmdd') ,"
					+ "  apr_time = to_char(sysdate,'hh24miss')," + "  apr_user =  :aprUser , "
					+ " mod_user = :modUser , " + " mod_time = sysdate ," + " mod_pgm  = :mod_pgm ,"
					+ " mod_seqno =nvl(mod_seqno,0)+1 " + " where  corp_p_seqno=:corp_p_seqno ";
			setString("aprUser", wp.loginUser);
			setString("modUser", wp.loginUser);
			setString("mod_pgm", wp.modPgm());
			setString("corp_p_seqno", corpSeqno);
			rc = sqlExec(strSql);
			if (rc < 0) {
				return rc;
			}
			if (sqlRowNum <= 0) {
				errmsg(this.sqlErrtext);
				rc = -1;
				return rc;
			}
		}

		if (rc != 1) {
			return rc;
		}
		// modify, COL_BAD_TRANS 催收轉呆記錄檔，當選擇轉呆時，要修改COL_BAD_TRANS，覆核欄位。
		// trans_types為4 商務戶 一般戶都需進行操作
		if (eqIgno(kkTransType, "4")) {
			if (!eqIgno(acnoFlag, "2")) {
				// 一般戶
				rc = updateColBadTrans();
			} else {
				// 商务户 trans_type=4時 需要修改ColBadTrans
				rc = updateCorpColBadTrans(corpSeqno);
			}
		}

		return rc;
	}

	// 商務卡修改 ColBadTrans
	int updateCorpColBadTrans(String corpSeqno) {
		if (rc != 1) {
			return rc;
		}
//		System.out.println("xiugai bad trans");
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_bad_trans");
		sp.ppstr("apr_flag", "Y");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.addsql(", apr_time = to_char(sysdate,'hh24miss') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where p_seqno in " + "(select p_seqno from col_wait_trans where corp_p_seqno=?)", corpSeqno);
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}

		return rc;
	}

	int updateColBadTrans() {
		if (rc != 1) {
			return rc;
		}

		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_bad_trans");
		sp.ppstr("apr_flag", "Y");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.addsql(", apr_time = to_char(sysdate,'hh24miss') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where p_seqno=?", pSeqno);
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}

		return rc;
	}

}
