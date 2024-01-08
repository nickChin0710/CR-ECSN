/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-26  V1.00.01  ryan       program initial                            *
* 111-10-25  v1.00.02  Yang Bo    Sync code from mega                        *
******************************************************************************/
package actp01;

import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Actp0410 extends BaseProc {

	int rr = -1;
	String msg = "", msgOk ="";
	String kk1 = "",kk2="";
	int ilOk = 0;
	int ilErr = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		// ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
		// wp.respCode + ",rHtml=" + wp.respHtml);
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			dataProcess();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
			// } else if (eq_igno(wp.buttonCode, "A")) {
			// /* 新增功能 */
			// insertFunc();
			// } else if (eq_igno(wp.buttonCode, "U")) {
			// /* 更新功能 */
			// updateFunc();
			// } else if (eq_igno(wp.buttonCode, "D")) {
			// /* 刪除功能 */
			// deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {

	}

	@Override
	public void dddwSelect() {

	}

	//for query use only
		private int getWhereStr() throws Exception {
			String lsDate1 = wp.itemStr("ex_chg_cycle_date");
			String lsDate2 = wp.itemStr("ex_chg_cycle_date2");
			
			if (this.chkStrend(lsDate1, lsDate2) == false) {
				alertErr("[登錄日期-起迄]  輸入錯誤");
				return -1;
			}
			wp.whereStr = " where 1=1 and b.id_p_seqno = act_pay_sms.id_p_seqno ";
			if (empty(wp.itemStr("ex_chg_cycle_date")) == false) {
				wp.whereStr += " and act_pay_sms.crt_date >= :ex_chg_cycle_date ";
				setString("ex_chg_cycle_date", wp.itemStr("ex_chg_cycle_date"));
			}
			if (empty(wp.itemStr("ex_chg_cycle_date2")) == false) {
				wp.whereStr += " and act_pay_sms.crt_date <= :ex_chg_cycle_date2 ";
				setString("ex_chg_cycle_date2", wp.itemStr("ex_chg_cycle_date2"));
			}
			if (empty(wp.itemStr("ex_crt_id")) == false) {
				wp.whereStr += " and act_pay_sms.crt_user = :ex_crt_id ";
				setString("ex_crt_id", wp.itemStr("ex_crt_id"));
			}
			if (wp.itemStr("ex_optcode").equals("0")) {
				wp.whereStr += " and act_pay_sms.mod_pgm in ('actm0410','w_actm0410')  ";
			}else{
				wp.whereStr += " and act_pay_sms.mod_pgm = 'actp0410' ";
			}
			return 1;
		}
	
	@Override
	public void queryFunc() throws Exception {
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " hex(act_pay_sms.rowid) as rowid, " 
		             + " lpad(' ',40) db_chi_name, "
		             + " lpad(' ',8) db_next_close_date, "
		             + " b.id_no, "
		             + " b.chi_name, "
		             + " act_pay_sms.id_p_seqno, "
		             + " act_pay_sms.sms_s_acct_month, "
		             + " act_pay_sms.sms_e_acct_month, "
				     + " act_pay_sms.lastpay_date_m3,"
				     + " act_pay_sms.lastpay_date_m2,"
				     + " act_pay_sms.lastpay_date_m1,"
				     + " act_pay_sms.lastpay_date_m0,"
				     + " act_pay_sms.lastpay_date_p1,"
				     + " act_pay_sms.lastpay_date_p2,"
				     + " act_pay_sms.lastpay_date_p3,"
				     + " act_pay_sms.m0_acct_month, "
				     + " act_pay_sms.sms_acct_month,"
				     + " act_pay_sms.stop_s_date,"
				     + " act_pay_sms.stop_e_date,"
				     + " act_pay_sms.proc_flag, "
				     + " act_pay_sms.proc_date, "
				     + " act_pay_sms.apr_date, "
				     + " act_pay_sms.apr_time, "
				     + " act_pay_sms.apr_user, "
				     + " act_pay_sms.crt_user, "
				     + " act_pay_sms.crt_date, "
				     + " act_pay_sms.crt_time, "
				     + " act_pay_sms.mod_seqno, "
				     + " act_pay_sms.mod_time, "
				     + " act_pay_sms.mod_pgm, "
				     + " lpad(' ',20) db_cell_phone "
				     ;
		wp.daoTable = " act_pay_sms , crd_idno b ";
		wp.whereOrder = "  ";
		if(getWhereStr()!=1)return;
		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		//wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
    apprDisabled("crt_user");

	}


	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		
	}

	@Override
	public void dataProcess() throws Exception {
		 //-check approve-
	/*	 if (!check_approve(wp.item_ss("zz_apr_user"),
		 wp.item_ss("zz_apr_passwd")))
		 {
		 return;
		 }*/
		SqlPrepare sp = new SqlPrepare();
		String modPgm="",loginUser="";
		String[] aaRowid = wp.itemBuff("rowid");
		String[] opt = wp.itemBuff("opt");
		String[] aaModSeqno = wp.itemBuff("mod_seqno");
		wp.listCount[0] = aaModSeqno.length;
		// -update-

		for (int ii = 0; ii < aaModSeqno.length; ii++) {
			if (!checkBoxOptOn(ii, opt)) {
				continue;
			}
			sp.sql2Update("act_pay_sms");
			if(wp.itemStr("ex_optcode").equals("0")){
				msgOk = "覆核處理成功";
				msg = "覆核處理失敗,update act_pay_sms err";
				modPgm = "actp0410";
				loginUser = wp.loginUser;
				sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ");
				sp.addsql(", apr_time = to_char(sysdate,'hh24miss') " );
			}else{
				msgOk = "解覆核處理成功";
				msg = "解覆核處理失敗,update act_pay_sms err";
				modPgm = "actm0410";
				loginUser = "";
				sp.addsql(", apr_date = '' ");
				sp.addsql(", apr_time = '' " );
			}
			sp.ppstr("apr_user", loginUser);
			sp.ppstr("mod_pgm",modPgm);
			sp.ppstr("mod_user", loginUser);
			sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
			sp.sql2Where(" where hex(rowid)=?", aaRowid[ii]);
			sp.sql2Where(" and mod_seqno=?", aaModSeqno[ii]);
			sqlExec(sp.sqlStmt(), sp.sqlParm());
			if(sqlRowNum<=0){
				alertMsg(msg);
				wp.colSet(rr, "ok_flag", "!");
				sqlCommit(0);
				return;
			}	
		}
	    sqlCommit(1);
	    queryFunc(); 
	    alertMsg(msgOk);

	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}
	
	void listWkdata() throws Exception{
		String lsIdPSeqno="",lsVal1="",lsVal2="",lsVal3="";

		for(int i = 0 ;i<wp.selectCnt;i++){
			lsIdPSeqno = wp.colStr(i,"id_p_seqno");

			String sqlSelect=" select stmt_cycle "
					+ " from act_acno where id_p_seqno = :ls_id_p_seqno ";
			setString("ls_id_p_seqno",lsIdPSeqno);
			sqlSelect(sqlSelect);
			lsVal1 = sqlStr("stmt_cycle");
			if(sqlRowNum<=0){
				wp.alertMesg = "<script language='javascript'> alert('請輸入 身分證號')</script>";
			}
			sqlSelect=" select next_lastpay_date "
					+ " from ptr_workday  where stmt_cycle = :ls_val1 ";
			setString("ls_val1",lsVal1);
			sqlSelect(sqlSelect);
			lsVal2 = sqlStr("next_lastpay_date");
			if(sqlRowNum<=0){
				wp.alertMesg = "<script language='javascript'> alert('ptr_workday Error"+lsVal1+"')</script>";
			}
			sqlSelect = "select chi_name from crd_idno where id_p_seqno = :ls_id_p_seqno ";
			setString("ls_id_p_seqno",lsIdPSeqno);
			sqlSelect(sqlSelect);
			lsVal3 = sqlStr("chi_name");
			if(sqlRowNum<=0){
				wp.alertMesg = "<script language='javascript'> alert('chi_name Error"+lsVal3+"')</script>";
			}
			wp.colSet(i,"db_chi_name", lsVal3);
			wp.colSet(i,"db_next_close_date", lsVal2);

		}
	}
}
