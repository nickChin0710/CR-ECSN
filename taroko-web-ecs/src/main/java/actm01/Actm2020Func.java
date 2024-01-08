/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 108/12/25  V1.00.01    phopho     bug fix: 0001649                         *
* 109-07-27  V1.00.00  Andy       program Re initial                         *
* 109-08-18  V1.00.01  Andy       Update:Mantis3944                          *
* 110-08-02  V1.00.02  Andy       Update:Mantis8067                          *
* 110-08-03  V1.00.03  Andy       Update:Mantis8067                          *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-06-28  V1.00.04  Simon      取消寫入 "借方科目"、"銷帳鍵值"            *
*****************************************************************************/

package actm01;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm2020Func extends FuncEdit {
	String alldata = "";
	
	public void setalldata(String alldata) {
		this.alldata = alldata;
	}


	public Actm2020Func(TarokoCommon wr) {
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
	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysDate = df.format(new Date());
		df = new SimpleDateFormat("HHmmss");
		String sysTime = df.format(new Date());
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_pay_detail");
		sp.ppstr("batch_no",varsStr("aa_batch_no"));
		sp.ppstr("serial_no",varsStr("aa_serial_no"));
		sp.ppstr("p_seqno",varsStr("aa_p_seqno"));
		sp.ppstr("acno_p_seqno",varsStr("aa_acno_p_seqno"));
		sp.ppstr("acct_type",varsStr("aa_acct_type"));
		sp.ppstr("id_p_seqno",varsStr("aa_id_p_seqno"));
		sp.ppstr("pay_card_no",varsStr("aa_pay_card_no"));
		sp.ppstr("pay_amt",varsStr("aa_pay_amt"));
		sp.ppstr("pay_date",varsStr("aa_pay_date"));
		sp.ppstr("payment_type",varsStr("aa_payment_type"));
	//sp.ppstr("debit_item",varsStr("aa_debit_item"));
	//sp.ppstr("debt_key",varsStr("aa_debt_key"));
		sp.ppstr("payment_no",varsStr("aa_payment_no"));
		sp.ppstr("curr_code","901");
		sp.ppstr("crt_date",getSysDate());
		sp.ppstr("crt_time",sysTime);
		sp.ppstr("crt_user",wp.loginUser);			
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppnum("mod_seqno", 1);
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());		
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1) return rc;		
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_pay_detail");
		sp.ppstr("pay_card_no", varsStr("pay_card_no"));
		sp.ppstr("payment_no", varsStr("payment_no"));
		sp.ppstr("acct_type", varsStr("acct_type"));
		sp.ppstr("id_p_seqno", varsStr("id_p_seqno"));
		sp.ppstr("acno_p_seqno", varsStr("acno_p_seqno"));
		sp.ppstr("p_seqno", varsStr("p_seqno"));
		sp.ppstr("pay_amt", varsStr("pay_amt"));
		sp.ppstr("pay_date", varsStr("pay_date"));
	//sp.ppstr("debit_item", varsStr("debit_item"));
	//sp.ppstr("debt_key", varsStr("debt_key"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
//		sp.rowid2Where(vars_ss("rowid"));
		sp.sql2Where(" where batch_no=?", varsStr("batch_no"));
		sp.sql2Where(" and serial_no=?", varsStr("serial_no"));
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

		strSql = "delete act_pay_detail "
				+ "where batch_no = ? "
				+ "and serial_no =? ";

		Object[] param = new Object[] { varsStr("aa_batch_no"),varsStr("aa_serial_no") };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return(rc);
	}
	int insertActPayBatch() throws Exception{	
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysDate = df.format(new Date());
		df = new SimpleDateFormat("HHmmss");
		String sysTime = df.format(new Date());
		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_pay_batch");
		sp.ppstr("batch_no",varsStr("aa_batch_no"));
		sp.ppnum("batch_tot_cnt",varsNum("aa_tot_cnt"));
		sp.ppnum("batch_tot_amt",varsNum("aa_tot_amt"));
		sp.ppstr("curr_code","901");  
		sp.ppstr("crt_date",getSysDate());
		sp.ppstr("crt_time",sysTime);
		sp.ppstr("crt_user",wp.loginUser);
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_user",wp.loginUser);
		sp.ppstr("mod_pgm",wp.modPgm());
		sp.ppnum("mod_seqno",1);
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return(rc);
	}
	
	int updateActPayBatch() throws Exception {		
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_pay_batch");
		sp.ppnum("batch_tot_cnt", varsNum("aa_tot_cnt"));
		sp.ppnum("batch_tot_amt", varsNum("aa_tot_amt"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where batch_no=?", varsStr("aa_batch_no"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		return rc;
	}
	
	int insertActBatchCntl() throws Exception{
		String lsBatchNo="",lsCtrlDate = "", lsBranch = "", lsBatch = "", lsSerial = "";
		lsBatchNo = wp.itemStr2("sel_batch_no");
		if(empty(lsBatchNo)){
			lsBatchNo = varsStr("aa_batch_no");
		}
		lsCtrlDate = strMid(lsBatchNo,0,8);
        lsBranch = strMid(lsBatchNo,8,4);
        lsBatch = strMid(lsBatchNo,12,4);
        //選取指定批號之最大序號
        String lsSql = "select max(serial_no) max_serial_no from act_pay_detail "
        		+ "where batch_no =:ls_batch_no";
        setString("ls_batch_no",lsBatchNo);
        sqlSelect(lsSql);        
        lsSerial = colStr("max_serial_no");

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_batch_cntl");
		sp.ppstr("ctrl_date",lsCtrlDate);
		sp.ppstr("branch",lsBranch);  
		sp.ppstr("batch",lsBatch);
		sp.ppstr("serial",lsSerial);
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		
		return rc;
	}
	
	int deleteActBatchCntl() throws Exception{
        String lsBatchNo="",lsCtrlDate = "", lsBranch = "", lsBatch = "", lsSerial = "";

        lsBatchNo = wp.itemStr2("sel_batch_no");
        if(empty(lsBatchNo)){
			lsBatchNo = varsStr("aa_batch_no");
		}
        lsCtrlDate = strMid(lsBatchNo,0,8);
        lsBranch = strMid(lsBatchNo,8,4);
        lsBatch = strMid(lsBatchNo,12,4);
        lsSerial = wp.itemStr2("sel_serial_no");

		strSql = "delete act_batch_cntl where ctrl_date = ? and branch = ? and batch = ? ";
		Object[] param = new Object[] {lsCtrlDate, lsBranch, lsBatch };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		return rc;
	}

}
