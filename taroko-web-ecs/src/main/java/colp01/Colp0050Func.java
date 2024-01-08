/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/06  V1.00.00    phopho     program initial                          *
*  108/11/27  V1.00.01    phopho     fix bug 0001701                          *
*  109-05-06  V1.00.02    Zhanghuheng      updated for project coding standard 
** 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                          *
*  112/01/13  V1.00.04   Sunny      統編修改時，項下所有個卡帳戶層都需要更新                          *    
******************************************************************************/

package colp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colp0050Func extends FuncProc {
	String pSeqno;
	String kkAcctKey,kkAcctType,kkAcnoFlag;

	public Colp0050Func(TarokoCommon wr) {
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
		kkAcctKey = varsStr("acct_key");
		kkAcctType= varsStr("acct_type");
		kkAcnoFlag= varsStr("acno_flag");
		
		// -other modify-
		sqlWhere = " where acno_p_seqno = ? ";
		Object[] param = new Object[] { pSeqno };
		if (isOtherModify("act_acno", sqlWhere, param)) {
			return;
		}
	}

	@Override
	public int dataProc() {
		dataCheck();
		if (rc != 1)
			return rc;

		rc = updateFunc();
		if (rc != 1)
			return rc;

		rc = deleteFunc();

		return rc;
	}

	int updateFunc() {
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_acno");
		sp.ppstr("no_delinquent_flag", varsStr("no_delinquent_flag"));
		sp.ppstr("no_delinquent_s_date", varsStr("no_delinquent_s_date"));
		sp.ppstr("no_delinquent_e_date", varsStr("no_delinquent_e_date"));
		sp.ppstr("no_collection_flag", varsStr("no_collection_flag"));
		sp.ppstr("no_collection_s_date", varsStr("no_collection_s_date"));
		sp.ppstr("no_collection_e_date", varsStr("no_collection_e_date"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		//sp.sql2Where(" where acno_p_seqno=?", pSeqno);
		
		// 20220131 sunny 統編修改時，項下所有個卡帳戶層都需要更新。
	    
	   // if(kkAcnoFlag.equalsIgnoreCase("1"))
		if(kkAcctType.equalsIgnoreCase("01"))
	    {
	     sp.sql2Where(" where acno_p_seqno=?", pSeqno);
	    }
	    else
	    {
	      sp.sql2Where(" where CORP_P_SEQNO IN (SELECT CORP_P_SEQNO FROM crd_corp WHERE corp_no=?)", kkAcctKey);
	      sp.sql2Where(" and acct_type =?", kkAcctType);	      
	    }
		    
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			rc = -1;
		}
		return rc;
	}

	int deleteFunc() {
		strSql = "delete col_acno_t "
//				+ sql_where;  //問題單0001701 bug fix
				+ " where p_seqno = ? ";
		Object[] param = new Object[] { pSeqno };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			rc = 0;
		}
		return rc;
	}

}
