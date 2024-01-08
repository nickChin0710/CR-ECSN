/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 107-08-15  V1.00.01  Alex       bug fixed                                  *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 111-11-14  V1.00.04  Simon      1.cancel autopay_indicator='3'             *
*                                 2.update autopay_acct_no data into act_acno & act_acct_curr for bank 006*
******************************************************************************/

package actm01;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Actm0010Func extends FuncEdit {
    String kk1 = "" , isOldAcctBank = "" , isOldAcctNo = "" , isOldAcctId = "" , isEffcFlag = ""; 

	public Actm0010Func(TarokoCommon wr) {
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
		kk1 = wp.itemStr2("rowid");
		
		if(isAdd() || isUpdate()){
			String sql1 = " select "
							+ " autopay_acct_e_date , "							
							+ " autopay_acct_bank , "
							+ " autopay_acct_no , "
							+ " autopay_id "
							+ " from act_acno "
							+ " where acno_p_seqno = ? "
							;
			
    //wp.ddd("disp01-->wp.item_ss(p_seqno)=[%s]", wp.item_ss("p_seqno"));
			sqlSelect(sql1,new Object[]{wp.itemStr2("p_seqno")});
			
			isOldAcctBank = colStr("autopay_acct_bank");
			isOldAcctNo = colStr("autopay_acct_no");
			isOldAcctId = colStr("autopay_id");
			
			isEffcFlag = "Y"; //預設"Y"或空值表示為 非扣繳終止戶(即沒有扣繳帳號或未終止之有效扣繳帳戶)
			if(!empty(isOldAcctBank) && !empty(isOldAcctNo)) {
			  if(!colEmpty("autopay_acct_e_date") && getSysDate().compareTo(colStr("autopay_acct_e_date"))>0){
				  isEffcFlag = "N";//扣繳已終止戶 
			  } 
			} 

		}
		
		if (this.isAdd()){
			//檢查新增資料是否重複
			kk1 = wp.itemStr2("p_seqno");
			if(empty(kk1)){
				kk1 = getPseqno();
				if(empty(kk1))	return ;
			}
			String lsSql = "select count(*) as tot_cnt from act_chkno where p_seqno = ? and curr_code = '901' ";
			Object[] param = new Object[] { kk1 };
			sqlSelect(lsSql, param);
			if (colNum("tot_cnt") > 0) {
				errmsg("資料已存在，無法新增");
			}
			return;
		} else {
			//-other modify-
			sqlWhere = "where 1=1 "
					  + " and nvl(mod_seqno,0) = ? "
					//+commSqlStr.whereRowid(kk1)
					  + " and hex(rowid) = ? "
					  ;
			Object[] param = new Object[] { wp.modSeqno(), kk1 };
			isOtherModify("act_chkno", sqlWhere, param);
		}
	}
	
	public String getPseqno()  {
		String lsKey = "";
		lsKey = commString.acctKey(wp.itemStr2("kk_acct_key"));
		if(lsKey.length()!=11){
			errmsg("帳戶帳號輸入錯誤");
			return "";
		}
				
		String sql1 = "select p_seqno from act_acno where acct_type = ? and acct_key = ?";
		       sql1 += " and acno_p_seqno = p_seqno ";
		sqlSelect(sql1,new Object[]{wp.itemStr2("kk_acct_type"),lsKey});
		
		if(sqlRowNum>0){
			return colStr("p_seqno");
		}
		
		return "";
	}
	
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		
		if(rc!=1) return rc;
		
   /*** java 程式執行的系統作業環境時間(例如192.168.30.20) 和 DB2 所在的系統作業環境時間[sysdate]可能會不一致
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysDate = df.format(new Date());
		
		df = new SimpleDateFormat("HHmmss");
		String sysTime = df.format(new Date());
    ***/ 

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_chkno");
		sp.ppstr("p_seqno", kk1);
		sp.ppstr("acct_type", wp.itemStr2("acct_type"));
		sp.ppstr("id_p_seqno", wp.itemStr2("id_p_seqno"));
		sp.ppstr("autopay_acct_bank", wp.itemStr2("autopay_acct_bank"));
		sp.ppstr("autopay_acct_no", wp.itemStr2("autopay_acct_no"));
		sp.ppstr("autopay_indicator", wp.itemStr2("autopay_indicator"));		
	//sp.ppss("autopay_fix_amt", wp.item_ss("autopay_fix_amt"));		
	//sp.ppss("autopay_rate", wp.item_ss("autopay_rate"));		
		sp.ppnum("autopay_fix_amt", wp.itemNum("autopay_fix_amt"));		
		sp.ppnum("autopay_rate", wp.itemNum("autopay_rate"));		
		sp.ppstr("autopay_acct_s_date", wp.itemStr2("autopay_acct_s_date"));		
		sp.ppstr("autopay_acct_e_date", wp.itemStr2("autopay_acct_e_date"));		
		sp.ppstr("valid_flag", wp.itemStr2("valid_flag"));
		sp.ppstr("from_mark", wp.itemStr2("from_mark"));
		sp.ppstr("autopay_id", wp.itemStr2("autopay_id"));
		sp.ppstr("autopay_id_code", wp.itemStr2("autopay_id_code"));
		sp.ppstr("edda_reentry_flag", wp.itemStr2("edda_reentry_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("verify_flag", wp.itemStr2("verify_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("verify_date", wp.itemStr2("verify_date"));
		sp.ppstr("verify_return_code", wp.itemStr2("verify_return_code"));
		sp.ppstr("ad_mark", "A");		
		sp.ppstr("curr_code", "901");
		sp.ppstr("old_acct_bank", isOldAcctBank);
		sp.ppstr("old_acct_no", isOldAcctNo);
		sp.ppstr("old_acct_id", isOldAcctId);
		sp.ppstr("effc_flag", isEffcFlag);
		sp.ppstr("stmt_cycle", wp.itemStr2("stmt_cycle"));
		sp.ppstr("sms_send_date", wp.itemStr2("sms_send_date"));
		sp.ppnum("sms_send_cnt", wp.itemNum("sms_send_cnt"));
	//sp.ppss("crt_date", sysDate);
		sp.addsql(", crt_date ",", to_char(sysdate,'yyyymmdd') ");
	//sp.ppss("crt_time", sysTime);
		sp.addsql(", crt_time ",", to_char(sysdate,'hh24miss') ");
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_seqno", "1");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc!=1) return rc;

   /*** java 程式執行的系統作業環境時間(例如192.168.30.20) 和 DB2 所在的系統作業環境時間[sysdate]可能會不一致
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String sysDate = df.format(new Date());
		
		df = new SimpleDateFormat("HHmmss");
		String sysTime = df.format(new Date());
    ***/ 

		SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("act_chkno");
		sp.ppstr("autopay_acct_bank", wp.itemStr2("autopay_acct_bank"));
		sp.ppstr("autopay_acct_no", wp.itemStr2("autopay_acct_no"));
		sp.ppstr("autopay_indicator", wp.itemStr2("autopay_indicator"));		
	//sp.ppnum("autopay_fix_amt", wp.itemNum("autopay_fix_amt"));		
	//sp.ppnum("autopay_rate", wp.itemNum("autopay_rate"));		
		sp.ppstr("autopay_acct_s_date", wp.itemStr2("autopay_acct_s_date"));		
		sp.ppstr("autopay_acct_e_date", wp.itemStr2("autopay_acct_e_date"));		
		sp.ppstr("valid_flag", wp.itemStr2("valid_flag"));
		sp.ppstr("autopay_id", wp.itemStr2("autopay_id"));
		sp.ppstr("autopay_id_code", wp.itemStr2("autopay_id_code"));
		sp.ppstr("edda_reentry_flag", wp.itemStr2("edda_reentry_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("verify_flag", wp.itemStr2("verify_flag").equals("Y") ? "Y" : "N");
		sp.ppstr("verify_date", wp.itemStr2("verify_date"));
		sp.ppstr("verify_return_code", wp.itemStr2("verify_return_code"));
		sp.ppstr("old_acct_bank", isOldAcctBank);
		sp.ppstr("old_acct_no", isOldAcctNo);
		sp.ppstr("old_acct_id", isOldAcctId);
		sp.ppstr("effc_flag", isEffcFlag);
		sp.ppstr("stmt_cycle", wp.itemStr2("stmt_cycle"));
		sp.ppstr("sms_send_date", wp.itemStr2("sms_send_date"));
		sp.ppnum("sms_send_cnt", wp.itemNum("sms_send_cnt"));
	//sp.ppss("crt_date", sysDate);
		sp.addsql(", crt_date = to_char(sysdate,'yyyymmdd') ", "");
	//sp.ppss("crt_time", sysTime);
		sp.addsql(", crt_time = to_char(sysdate,'hh24miss') ", "");
		sp.ppstr("crt_user", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ", "");
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
		sp.sql2Where(" where hex(rowid) = ?", kk1);
		sp.sql2Where(" and nvl(mod_seqno,0) = ?", wp.modSeqno());
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			rc = -1;
		}
		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc!=1) return rc;
		
		strSql = "delete act_chkno "
				+ sqlWhere;
		Object[] param = new Object[] {wp.modSeqno() };
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}
	
	public int updateSms() throws Exception{
		msgOK();
		strSql = " update act_chkno set "
				 + " sms_send_date =to_char(sysdate,'yyyymmdd') , "
				 + " sms_send_cnt = nvl(sms_send_cnt,0)+1 , "
				 + " mod_pgm =:mod_pgm , "
				 + " mod_user =:mod_user , "
				 + " mod_time = sysdate , "
				 + " mod_seqno =nvl(mod_seqno,0)+1 "
				 + " where mod_seqno =:mod_seqno "
			 //+commSqlStr.whereRowid(varsStr("rowid"))
				 + " and hex(rowid) = :hex_rowid "
				 ;
		
		setString("mod_pgm",wp.modPgm());
		setString("mod_user",wp.loginUser);
		item2ParmNum("mod_seqno");
		setString("hex_rowid",varsStr("rowid"));
		
		sqlExec(strSql);
		if(sqlRowNum<=0){
			errmsg("資料已被其他人修改, 請重新讀取");
		}
		
		return rc;
	}
	
}
