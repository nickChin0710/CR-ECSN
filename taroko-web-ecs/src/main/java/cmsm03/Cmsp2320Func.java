/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-08-18  V1.00.00    yanghan     Initial *
* 109-09-17  V1.00.01    shiyuqi    添加判斷條件*
* 111-01-10  V1.00.02    Justin     增加欄位及改善使用者介面                 *
******************************************************************************/
package cmsm03;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Cmsp2320Func extends FuncProc {
	public Cmsp2320Func(TarokoCommon wr) {
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
	}

	@Override
	public int dataProc() {
      return 0;
	}
	
	public int dataProcess(int rr) {
	  
	        dataCheck();
	        if (rc != 1) {
	            return rc;
	        }
	        rc = updateFunc(rr);

	        if (rc != 1) {
	            return rc;
	        }
	        
	        rc = updateCrdCard(rr);
	        if (rc != 1) {
	            return rc;
	        }
	        return rc;
	    }
	//修改cms_card_pwcntreset
	int updateFunc(int rr) {
		if (rc != 1) {
			return rc;
		}
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("cms_card_pwcntreset");
		sp.ppstr("mod_pgm", wp.itemStr(rr,"mod_pgm") );
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.ppstr("apr_flag", "Y");
		sp.ppstr("apr_user", wp.loginUser);
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.addsql(", apr_time = to_char(sysdate,'hhmmss') ", "");		
		sp.addsql(", PASSWD_ERR_COUNT_RESETDATE = to_char(sysdate,'yyyymmdd') ", "");		
		sp.sql2Where(" where card_no=?",  wp.itemStr(rr,"card_no"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
//		System.out.println(getMsg());
		if (sqlRowNum == 0) {
			 errmsg(getMsg());
			rc = -1;
		}
		return rc;
	}
	//修改crd_card
	int updateCrdCard(int rr) {
		if (rc != 1) {
			return rc;
		}
		
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("crd_card");
		sp.ppstr("passwd_err_count", "0");		
		sp.addsql(", PASSWD_ERR_COUNT_RESETDATE = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("apr_user", wp.loginUser);
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ", "");
		sp.ppstr("mod_user", wp.loginUser); 
	    sp.ppstr("mod_pgm", wp.itemStr(rr,"mod_pgm") );
	    sp.addsql(", mod_time = to_char(sysdate,'yyyymmddhhmmss') ", "");
		sp.sql2Where(" where card_no=?",  wp.itemStr(rr,"card_no"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
//		System.out.println(getMsg());
		if (sqlRowNum == 0) {
			 errmsg(getMsg());
			rc = -1;
		}
		return rc;
	}

}
