/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-07-10  V1.00.00  machao       合庫金控子公司推廣單位放行作業                            *
******************************************************************************/
package mktp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktp0540Func extends FuncEdit {
	String ex_corp_no = "";
	String control_tab_name = "mkt_office_d";
	String corp_no  ="";

	public Mktp0540Func(TarokoCommon wr) {
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
	String[] CorpNo = wp.itemBuff("corp_no")	;
    String[] opt = wp.itemBuff("opt");
    String opt1 = opt[0];
    int opt2 = Integer.parseInt(opt1);
    corp_no = CorpNo[opt2-1];
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc!=1)return -1;
		strSql = " insert into mkt_office_d ( " 
				+ " office_m_code, " 
				+ " office_code, " 
				+ " office_name, " 
				+ " mod_time, mod_user, mod_pgm, mod_seqno " 
				+ " ) " 
				+ " values( " 
				+ " ?,?,? " 
				+ " , sysdate, ?, ?, 1 "
				+ " ) ";

		Object[] param = new Object[] {
				wp.itemStr2("corp_no"), 
				colStr("aa_office_code"),
				colStr("aa_office_name"),
				wp.loginUser,
				wp.modPgm()
		};

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg("新增 "+control_tab_name+" 錯誤");
		}
		return rc;

	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		msgOK();
		dataCheck();
		if (rc!=1) return rc;
		strSql = " update mkt_office_d set " 
				+ " apr_date = ? "
				+ " ,apr_flag = 'Y' "
				+ " ,mod_time = sysdate "
				+ " ,mod_user = ? "
				+ " ,mod_pgm = ?"
				+ " ,mod_seqno = nvl(mod_seqno,0)+1 "
				+ " where 1 =  1 " 
				+ " and corp_no = ? "
				;

		Object[] param = new Object[] {
				getSysDate(), 
				wp.loginUser,
				wp.modPgm(),
				corp_no
		};

		sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg("資料存檔失敗,放行失敗");
		}else {
			msgOK(); // "放行處理檔完成");
		}
		
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		msgOK();
		Object[] param = new Object[] {colStr("aa_rowid") };

		strSql = "delete mkt_office_d where hex(rowid) = ?";

		rc = sqlExec(strSql, param);

		if (sqlRowNum <= 0) {
			errmsg("删除 "+control_tab_name+" 錯誤");
			rc = -1;
		}

		return rc;
	}

}
