/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 107-08-21  V1.00.01  Alex       dataCheck,update sql                       *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-03-19  V1.00.04  Simon      apply no field "act_pay_error.acct_key"    * 
******************************************************************************/

package actm01;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0140Func extends FuncEdit {
	String mKkMchtNo = "";
	String isAcctKey = "";

	public Actm0140Func(TarokoCommon wr) {
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
		if(ibDelete)	return ;
		
		isAcctKey = commString.acctKey(wp.itemStr2("acct_key"));
		if(isAcctKey.length()!=11){
			errmsg("帳戶帳號輸入錯誤");
			return ;
		}
		
		String sql1 = " select "
						+ " acno_p_seqno, "
						+ " p_seqno "
						+ " from act_acno "
						+ " where acct_type = ? "
						+ " and acct_key = ? "
						;
		
		sqlSelect(sql1,new Object[]{wp.itemStr2("acct_type"),isAcctKey});
		if(sqlRowNum<=0){
			errmsg("帳戶資料不存在 !");
			return ;
		}
		
		if(wp.itemNum("pay_amt")<=0){
			errmsg("金額不可 <= 0 !");
			return ;
		}
		
		if(wp.itemStr2("pay_date").compareTo(this.getSysDate())>=0){
			errmsg("付款日不可 >= 系統日 !");
			return ;
		}
		
	}

	@Override
	public int dbInsert() {

		return 1;
	}

	@Override
	public int dbUpdate() {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysdate = df.format(new Date());
		
		df = new SimpleDateFormat("HHmmss");
		String systime = df.format(new Date());
		
		actionInit("U");
		dataCheck();
		if(rc!=1) return rc;
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_pay_error");
		sp.ppstr("duplicate_mark", wp.itemStr2("duplicate_mark").equals("Y") ? "Y" : "N");
		sp.ppstr("branch", wp.itemStr2("branch"));
		sp.ppstr("acct_type", wp.itemNvl("acct_type","01"));
	//sp.ppstr("acct_key", wp.itemStr2("acct_key"));
		sp.ppstr("acno_p_seqno", wp.itemStr2("acno_p_seqno"));
		sp.ppstr("p_seqno", wp.itemStr2("p_seqno"));
		sp.ppstr("pay_card_no", wp.itemStr2("pay_card_no"));
		sp.ppstr("payment_type", wp.itemStr2("payment_type"));
	//sp.ppss("crt_date", sysdate);
	//sp.ppss("crt_time", systime);
	//sp.ppss("crt_user", wp.loginUser);
	  sp.ppstr("update_date", sysdate);
	  sp.ppstr("update_time", systime);
	  sp.ppstr("update_user", wp.loginUser);
		sp.ppstr("confirm_flag", "Y");
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
	//sp.addsql(", update_date = to_char(sysdate,'yyyymmdd') ");
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where batch_no = ?", wp.itemStr2("batch_no"));
		sp.sql2Where(" and serial_no = ?", wp.itemStr2("serial_no"));		
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		if (rc != 1) {
			return rc;
		}
		
		if(wp.itemEq("confirm_flag", "N")){
			strSql = "delete act_pay_error where hex(act_pay_error.rowid) = ? ";
			Object[] param = new Object[] { wp.itemStr2("rowid") };
			rc = sqlExec(strSql, param);
		}	else	{
			strSql = " update act_pay_error set "
					 + " confirm_flag = 'N' , "
					 + " mod_time = sysdate , "
					 + " mod_user =:mod_user ,"
					 + " mod_pgm =:mod_pgm , "
					 + " mod_seqno = nvl(mod_seqno,0)+1 "
					 + " where batch_no =:batch_no "
					 + " and serial_no =:serial_no "
					 + " and mod_seqno =:mod_seqno "
					 ;
			
			item2ParmStr("batch_no");
			item2ParmStr("serial_no");
			item2ParmNum("mod_seqno");
			setString("mod_user",wp.loginUser);
			setString("mod_pgm",wp.modPgm());
			sqlExec(strSql);
		}
		
      if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

}
