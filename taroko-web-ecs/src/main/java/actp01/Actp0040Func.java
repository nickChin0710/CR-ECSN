/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-10  V1.00.00  Andy Liu      program initial                         *
* 107-08-21  V1.00.01  Alex        update , delete                           *
* 111-10-24  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/

package actp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Actp0040Func extends FuncEdit {
	String mKkMchtNo = "";

	public Actp0040Func(TarokoCommon wr) {
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

		strSql = "delete ACT_MODDATA_TMP where p_seqno = ? and act_modtype='03'";

		Object[] param = new Object[] { pSeqno };

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	public int updateData() throws Exception{
		msgOK();
		
		strSql = " update act_acct_curr set "
				 + " dc_autopay_bal =:dc_autopay_bal , "
			 //+ " autopay_bal =uf_curr_dc2tw(curr_code,:autopay_bal,db_autopay_beg_amt,autopay_beg_amt) , "
			   + " autopay_bal =uf_dc2tw_amt(autopay_beg_amt,dc_autopay_beg_amt,:dc_autopay_bal) , "
				 + " apr_flag ='Y' , "
				 + " apr_date = to_char(sysdate,'yyyymmdd') , "
				 + " apr_user =:apr_user ,"
				 + " mod_time = sysdate , "
				 + " mod_pgm =:mod_pgm , "
			 //+ " mod_seqno = nvl(mod_seqno,0)+1 "
			   + " mod_seqno = uf_nvl(mod_seqno,0)+1 "
				 + " where p_seqno =:p_seqno "
				 + " and acct_type =:acct_type "				 
				 + " and curr_code =:curr_code "				 
				 ;
		
		var2ParmNum("dc_autopay_bal");
	//setNumber("autopay_bal",vars_num("dc_autopay_bal"));
		setString("apr_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		var2ParmStr("p_seqno");
		var2ParmStr("acct_type");		
		var2ParmStr("curr_code");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acct_curr error !");
			return rc;
		}
		
		//--合計 act_acct_curr
		
		String sql1 = "select sum(autopay_bal) as tl_autopay from act_acct_curr where p_seqno = ?";
		sqlSelect(sql1,new Object[]{varsStr("p_seqno")});
		if(sqlRowNum<0){
			errmsg("select act_acct_curr.sum error !");
			return rc;
		}
		
		double ldTlAutopay = 0;
		ldTlAutopay = colNum("tl_autopay");
		
		strSql = " update act_acct set "
				 + " autopay_bal =:autopay_bal , "
			 //+ " update_date = to_char(sysdate,'yyyymmdd) , "
				 + " update_date = to_char(sysdate,'yyyymmdd') , "
				 + " update_user =:update_user , "
				 + " mod_time = sysdate , "
				 + " mod_user =:mod_user , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_seqno =nvl(mod_seqno,0)+1 "
				 + " where p_seqno =:p_seqno "
				 ;
		
		setNumber("autopay_bal",ldTlAutopay);
		setString("update_user",wp.loginUser);
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		var2ParmStr("p_seqno");
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("update act_acct error !");
		}
		
		return rc;
	}
	
	public int deleteData() throws Exception{
		msgOK();
		
		strSql = " delete act_moddata_tmp where p_seqno=:p_seqno and curr_code =:curr_code and act_modtype='03'";
		
		var2ParmStr("p_seqno");
		var2ParmStr("curr_code");
		
		sqlExec(strSql);
		
		if(sqlRowNum<=0){
			errmsg("delete act_moddata_tmp error !");			
		}
		return rc;
	}
	
}
