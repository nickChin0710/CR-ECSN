/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-06  V1.00.00  Andy       program initial                            *
* 107-07-20  V1.00.01  Andy       Update DeBug,program logic                 *
* 108-06-17  V1.00.03  Andy		    update : p_seqno ==> acno_p_seqno          * 
* 109-05-29  V1.00.04  Andy		    update : Mantis3540                        * 
* 111-10-24  V1.00.05  Yang Bo    sync code from mega                        *
* 112-07-23  V1.00.06  Simon      parms reuse control in getWhereStr()       *
******************************************************************************/

package actp01;

import busi.SqlPrepare;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Actp0060 extends BaseEdit {
	CommString commString = new CommString();
	String mExBatchno = "";
	String mExEmbossSource = "";
	String mExEmbossReason = "";
	String msg = "";

	String[] opt = null;
	String[] aaRowid = null;
	String[] aaRowid1 = null;
	String[] aaModSeqno = null;
	String[] aaModSeqno1 = null;
	String[] aaBatchNo = null;
	String[] aaSerialNo = null;
	String[] aaPSeqno = null;
	String[] aaAcnoPSeqno = null;
	String[] aaAcctType = null;
	String[] aaIdPSeqno = null;
	String[] aaId = null;
	String[] aaIdCode = null;
	String[] aaPayCardNo = null;
	String[] aaPayAmt = null;
	String[] aaPayDate = null;
	String[] aaPaymentType = null;
	String[] aaVouchMemo3 = null;
//String[] aa_crt_user = null;
//String[] aa_crt_date = null;
//String[] aa_crt_time = null;
	String[] aaUpdateUser = null;
	String[] aaUpdateDate = null;
	String[] aaUpdateTime = null;
	String[] aaBranch = null;
	String[] aaAcctKey = null;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {
			/* 新增功能 */
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
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
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 執行 */
			strAction = "S2";
			saveFunc();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		// 設定初始搜尋條件值
		// String sysdate1="",sysdate0="";
		// sysdate1 = ss_mid(get_sysDate(),0,8);
		// 續卡日期起-迄日
		// wp.col_set("exDateS", "");
		// wp.col_set("ex_max_rows", "200");
		// wp.item_set("ex_dept_no", "DP");
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
		String exBatchno = wp.itemStr("ex_batchno");
		String exCrtuser = wp.itemStr("ex_crtuser");

		wp.whereStr = " where 1=1  ";
		// 固定條件
		//wp.whereStr += "and ( a.acno_p_seqno = b.acno_p_seqno ) and confirm_flag = 'Y'";
		//wp.whereStr += "and (decode(a.acno_p_seqno,'',a.p_seqno,a.acno_p_seqno) = decode(a.acno_p_seqno,'',b.p_seqno,b.acno_p_seqno )) and confirm_flag = 'Y'";
		wp.whereStr += "and (decode(a.acno_p_seqno,'',a.p_seqno,a.acno_p_seqno) = b.acno_p_seqno ) and confirm_flag = 'Y'";
		// 自選條件
		wp.whereStr += sqlCol(exBatchno, "a.batch_no");
	//wp.whereStr += sql_col(ex_crtuser, "a.crt_user");
		wp.whereStr += sqlCol(exCrtuser, "a.update_user");

		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		getWhereStr();
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		wp.selectSQL = "hex(a.rowid) as rowid, a.mod_seqno,"
				+ "hex(b.rowid) as rowid1, b.mod_seqno as mod_seqno1," // act_acno
				+ "a.batch_no, "
				+ "a.serial_no, "
				+ "(a.batch_no||'-'||a.serial_no) wk_batchno, "
				+ "a.p_seqno, "
				+ "a.acno_p_seqno, "
				+ "a.acct_type, "
				+ "b.acct_key, "
				+ "(a.acct_type||'-'||b.acct_key) wk_ackey,"
				+ "a.pay_card_no, "
				+ "a.pay_amt, "
				+ "a.pay_date, "
				+ "a.payment_type, "
				+ "a.error_reason, "
				+ "a.error_remark, "
				+ "a.duplicate_mark, "
				+ "a.confirm_flag, "
			//+ "a.crt_user, "
			//+ "a.crt_date, "
				+ "a.update_user, "
				+ "a.update_date, "
				+ "'0' db_optcode, "
			//+ "a.crt_time, "
				+ "a.update_time, "
				+ "'0' as db_id_code, "
				+ "a.vouch_memo3, "
				+ "a.mod_user, "
				+ "a.mod_time, "
				+ "a.mod_pgm, "
				+ "b.payment_no, "
				+ "a.branch, "
				+ "b.id_p_seqno,"
				+ "UF_IDNO_ID(b.ID_P_SEQNO) db_id, "
				+ "UF_IDNO_NAME(b.ID_P_SEQNO) db_cname, "
				+ "'' err_msg ";
		wp.daoTable = " act_pay_error a, act_acno b  ";
		wp.whereOrder = "ORDER BY a.crt_user ASC, "
			//+ "a.crt_date ASC, "
				+ "a.update_date ASC, "
				+ "a.batch_no ASC, "
				+ "a.serial_no ASC ";
		
    if (!eqIgno(wp.buttonCode, "Q")) {
		  getWhereStr();
    }

		// System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
		// + wp.whereStr + wp.whereOrder);
		// wp.pageCount_sql = "select count(*) from ( select " + wp.selectSQL
		// +"from" + wp.daoTable + wp.whereStr + wp.whereOrder + " )";
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
    apprDisabled("update_user");

	}

	void listWkdata() throws Exception {
		int rowCt = 0;
		String lsSql = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// 計算欄位
			rowCt += 1;
			lsSql = "select chi_name, id_no, id_no_code "
					+ "from crd_idno "
					+ "where id_p_seqno = :ls_id_p_seqno ";
			setString("ls_id_p_seqno", wp.colStr(ii, "id_p_seqno"));
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet(ii, "id_no_code", sqlStr("id_no_code"));
			}

		}
		wp.colSet("row_ct", intToStr(rowCt));
	}

	@Override
	public void querySelect() throws Exception {
		// m_ex_mcht_no = wp.item_ss("mcht_no");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void saveFunc() throws Exception {
		opt = wp.itemBuff("opt");
		aaRowid = wp.itemBuff("rowid");
		aaRowid1 = wp.itemBuff("rowid1");
		aaModSeqno = wp.itemBuff("mod_seqno");
		aaModSeqno1 = wp.itemBuff("mod_seqno1");
		aaBatchNo = wp.itemBuff("batch_no");
		aaSerialNo = wp.itemBuff("serial_no");
		aaPSeqno = wp.itemBuff("p_seqno");
		aaAcnoPSeqno = wp.itemBuff("acno_p_seqno");
		aaAcctType = wp.itemBuff("acct_type");
		aaIdPSeqno = wp.itemBuff("id_p_seqno");
		aaId = wp.itemBuff("db_id");
		aaIdCode = wp.itemBuff("id_no_code");
		aaPayCardNo = wp.itemBuff("pay_card_no");
		aaPayAmt = wp.itemBuff("pay_amt");
		aaPayDate = wp.itemBuff("pay_date");
		aaPaymentType = wp.itemBuff("payment_type");
		aaVouchMemo3 = wp.itemBuff("vouch_memo3");
		aaUpdateUser = wp.itemBuff("update_user");
		aaUpdateDate = wp.itemBuff("update_date");
		aaUpdateTime = wp.itemBuff("update_time");
		aaBranch = wp.itemBuff("branch");
		aaAcctKey = wp.itemBuff("acct_key");

		String dsSql = "", lsSql = "";
		String lsDeptno = "", lsGlcode = "", lsAtmFlag = "", liAtmFee = "", lsVal = "";
		wp.listCount[0] = aaBatchNo.length;
		// check && update
		int llOk = 0, llErr = 0, rr = 0;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0) {
				continue;
			}
		}
		for (int ii = 0; ii < aaBatchNo.length; ii++) {
			if (!checkBoxOptOn(ii, opt)) {
				continue;
			}
			if (wfDebtCancel(ii) != 1) {
				llErr++;
				sqlCommit(0);
				continue;
			} else {
				llOk++;
				sqlCommit(1);
			}

		}
		alertMsg("放行處理: 成功筆數=" + llOk + "; 失敗筆數=" + llErr + ";");
	}

	public int wfDebtCancel(int ii) throws Exception {
		String dsSql = "", lsSql = "", lsDeptno = "", lsGlcode = "", 
				   lsAtmFlag = "", lsVal1 = "";

		double liAtmFee = 0;
		// -- delete from act_pay_error
		 dsSql = "delete from act_pay_error "
		 + "where hex(rowid) = :rowid "
		 + "and mod_seqno = :mod_seqno";
		 setString("rowid", aaRowid[ii]);
		 setString("mod_seqno", aaModSeqno[ii]);
		 sqlExec(dsSql);
		 if (sqlRowNum <= 0) {
			 wp.colSet(ii, "err_msg", "Delete act_pay_error Error!!");
			 wp.colSet(ii, "ok_flag", "X");
		 return -1;
		 }

		/*** insert act_debt_cancel
		if (act_debt_cancel_proc(ii) != 1) {
			wp.col_set(ii, "err_msg", "Inser act_debt_cancel 1 Error!!");
			wp.col_set(ii, "ok_flag", "X");
			return -1;
		}
    ***/
 
		// chk usr_deptno
		lsSql = "Select usr_deptno "
				+ "from sec_user "
				+ "where  usr_id = :ls_user ";
		setString("ls_user", wp.loginUser);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			wp.colSet(ii, "err_msg", "無法取得使用者部門代碼");
			wp.colSet(ii, "ok_flag", "X Error");
			return -1;
		} else {
			lsDeptno = sqlStr("usr_deptno");
		}
		// chk gl_code
		lsSql = "select gl_code "
				+ "from  ptr_dept_code "
				+ "where dept_code = :ls_deptno ";
		setString("ls_deptno", lsDeptno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			wp.colSet(ii, "err_msg", "無法取得起帳部門代碼");
			wp.colSet(ii, "ok_flag", "X");
			return -1;
		} else {
			lsGlcode = sqlStr("gl_code");
		}

		// insert act_debt_cancel
		if (actDebtCancelProc(ii,lsDeptno, lsGlcode) != 1) {
			wp.colSet(ii, "err_msg", "Inser act_debt_cancel 1 Error!!");
			wp.colSet(ii, "ok_flag", "X");
			return -1;
		}

		// chk atm_pay_flag
		lsSql = "select  atm_pay_flag "
				+ "from   act_acno "
				+ "where  acct_type = :acct_type "
				+ "and    acct_key  = :acct_key ";
		setString("acct_type", aaAcctType[ii]);
		setString("acct_key", aaAcctKey[ii]);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			wp.colSet(ii, "err_msg", "無法取得ATM旗標");
			wp.colSet(ii, "ok_flag", "X");
			return -1;
		} else {
			lsAtmFlag = sqlStr("atm_pay_flag");
		}
		if (empty(lsAtmFlag)) {
			lsAtmFlag = "N";
		}

		// chk atm_fee //PB没下條件 資料有數筆,暫抓第1筆資料 andy 20180208
		lsSql = "select atm_fee "
				+ "from  ptr_actgeneral_n "
				+ "fetch first 1 rows only ";
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			msg = "無法取得ATM FEE!";
			wp.colSet(ii, "ok_flag", "X");
			return -1;
		} else {
			liAtmFee = sqlNum("atm_fee");
		}
		// get new batch_no
		lsVal1 = strMid(aaBatchNo[ii], 0, 8) + "90050001";

		// insert to act_debt_cancel 2
		if (!lsAtmFlag.equals("Y") && aaPaymentType[ii].equals("IBA3")) {
			if (actDebtCancelProc1(ii, lsVal1, liAtmFee, lsDeptno, lsGlcode) != 1) {
				wp.colSet(ii, "err_msg", "Inser act_debt_cancel 2 Error!!");
				wp.colSet(ii, "ok_flag", "X");
				return -1;
			}
			if (updActAcno(ii) != 1) {
				wp.colSet(ii, "err_msg", "Update act_acno Error!!");
				wp.colSet(ii, "ok_flag", "X");
				return -1;
			}
		}
		return 1;
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	@Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_batchno");
			this.dddwList("dddw_batchno", "act_pay_error", "batch_no", "", "where 1=1 and confirm_flag = 'Y' order by batch_no ");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_crtuser");
			this.dddwList("dddw_crtuser", "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id ");

		} catch (Exception ex) {
		}
	}

	public int actDebtCancelProc(int rr, String lsDeptno, String lsGlcode) throws Exception {
		// delete
		String lsSql = "", isSql = "", lsFitDeptno = "";
    lsFitDeptno = commString.mid(lsDeptno, 0,2);

		lsSql = "select count(*) ct from act_debt_cancel where batch_no =:batch_no and serial_no =:serial_no ";
		setString("batch_no", aaBatchNo[rr]);
		setString("serial_no", aaSerialNo[rr]);
		sqlSelect(lsSql);		
		if (sqlNum("ct") > 0) {
			SqlPrepare sp = new SqlPrepare();
			sp.sql2Update("act_debt_cancel");
			sp.ppstr("p_seqno", aaPSeqno[rr]);
			sp.ppstr("acno_p_seqno", aaAcnoPSeqno[rr]);
			sp.ppstr("acct_type", aaAcctType[rr]);
			sp.ppstr("id_p_seqno", aaIdPSeqno[rr]);
			sp.ppstr("pay_card_no", aaPayCardNo[rr]);
			sp.ppnum("pay_amt", toNum(aaPayAmt[rr]));
			sp.ppstr("pay_date", aaPayDate[rr]);
			sp.ppstr("vouch_memo3", aaVouchMemo3[rr]);
			sp.ppstr("payment_type", aaPaymentType[rr]);
			sp.ppstr("update_user", aaUpdateUser[rr]);
			sp.ppstr("update_date", aaUpdateDate[rr]);
			sp.ppstr("update_time", aaUpdateTime[rr]);
			sp.ppstr("job_code", lsFitDeptno);
			sp.ppstr("vouch_job_code", "0" + strMid(lsGlcode, 0, 1));
			sp.ppstr("branch", aaBranch[rr]);
			sp.addsql(", mod_time =sysdate", "");
			sp.ppstr("mod_user", wp.loginUser);
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
			sp.sql2Where("where batch_no=?", aaBatchNo[rr]);
			sp.sql2Where("and serial_no=?", aaSerialNo[rr]);
			sqlExec(sp.sqlStmt(), sp.sqlParm());
		}else{
			SqlPrepare sp = new SqlPrepare();
			sp.sql2Insert("act_debt_cancel");
			sp.ppstr("batch_no", aaBatchNo[rr]);
			sp.ppstr("serial_no", aaSerialNo[rr]);
			sp.ppstr("p_seqno", aaPSeqno[rr]);
			sp.ppstr("acno_p_seqno", aaAcnoPSeqno[rr]);
			sp.ppstr("acct_type", aaAcctType[rr]);
			sp.ppstr("id_p_seqno", aaIdPSeqno[rr]);
			sp.ppstr("pay_card_no", aaPayCardNo[rr]);
			sp.ppnum("pay_amt", toNum(aaPayAmt[rr]));
			sp.ppstr("pay_date", aaPayDate[rr]);
			sp.ppstr("vouch_memo3", aaVouchMemo3[rr]);
			sp.ppstr("payment_type", aaPaymentType[rr]);
			sp.ppstr("update_user", aaUpdateUser[rr]);
			sp.ppstr("update_date", aaUpdateDate[rr]);
			sp.ppstr("update_time", aaUpdateTime[rr]);
			sp.ppstr("job_code", lsFitDeptno);
			sp.ppstr("vouch_job_code", "0" + strMid(lsGlcode, 0, 1));
			sp.ppstr("mod_user", wp.loginUser);
			sp.addsql(", mod_time ", ", sysdate ");			
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.ppnum("mod_seqno", 1);
			sp.ppstr("branch", aaBranch[rr]);
			sqlExec(sp.sqlStmt(), sp.sqlParm());		
		}	
		if (sqlRowNum < 0) {
			wp.colSet(rr, "err_msg", " insert to act_debt_cancel 1 error");
			wp.colSet(rr, "ok_flag", "X");
			return -1;
		} else {
			wp.colSet(rr, "ok_flag", "V");
		}
		return 1;
	}

	public int actDebtCancelProc1(int rr, String lsVal, double liAtmFee, String lsDeptno, String lsGlcode) throws Exception {
		// insert to act_debt_cancel 2
		String lsSql = "", isSql = "", lsFitDeptno = "";
	//ls_fit_deptno = ls_deptno.substring(0, 2);
    lsFitDeptno = commString.mid(lsDeptno, 0,2);


		lsSql = "select count(*) ct from act_debt_cancel where batch_no =:batch_no and serial_no =:serial_no ";
		setString("batch_no", lsVal);
		setString("serial_no", aaSerialNo[rr]);
		sqlSelect(lsSql);
		if (sqlNum("ct") > 0) {			
			SqlPrepare sp = new SqlPrepare();
			sp.sql2Update("act_debt_cancel");
			sp.ppstr("p_seqno", aaPSeqno[rr]);
			sp.ppstr("acno_p_seqno", aaAcnoPSeqno[rr]);
			sp.ppstr("acct_type", aaAcctType[rr]);
			sp.ppstr("id_p_seqno", aaIdPSeqno[rr]);
			sp.ppstr("pay_card_no", aaPayCardNo[rr]);
			sp.ppnum("pay_amt", toNum(aaPayAmt[rr]));
			sp.ppstr("pay_date", aaPayDate[rr]);
			sp.ppstr("vouch_memo3", aaVouchMemo3[rr]);
			sp.ppstr("payment_type", aaPaymentType[rr]);
			sp.ppstr("update_user", aaUpdateUser[rr]);
			sp.ppstr("update_date", aaUpdateDate[rr]);
			sp.ppstr("update_time", aaUpdateTime[rr]);
			sp.ppstr("job_code", lsFitDeptno);
			sp.ppstr("vouch_job_code", "0" + strMid(lsGlcode, 0, 1));
			sp.addsql(", mod_time =sysdate", "");
			sp.ppstr("mod_user", wp.loginUser);
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
			sp.sql2Where("where batch_no=?", lsVal);
			sp.sql2Where("and serial_no=?", aaSerialNo[rr]);
			sqlExec(sp.sqlStmt(), sp.sqlParm());
		}else{
			SqlPrepare sp = new SqlPrepare();
			sp.sql2Insert("act_debt_cancel");
			sp.ppstr("batch_no", lsVal);
			sp.ppstr("serial_no", aaSerialNo[rr]);
			sp.ppstr("p_seqno", aaPSeqno[rr]);
			sp.ppstr("acno_p_seqno", aaAcnoPSeqno[rr]);
			sp.ppstr("acct_type", aaAcctType[rr]);
			sp.ppstr("id_p_seqno", aaIdPSeqno[rr]);
			sp.ppstr("pay_card_no", aaPayCardNo[rr]);
			sp.ppnum("pay_amt", liAtmFee);
			sp.ppstr("pay_date", aaPayDate[rr]);
			sp.ppstr("vouch_memo3", "");
			sp.ppstr("payment_type", "COMA");
			sp.ppstr("update_user", aaUpdateUser[rr]);
			sp.ppstr("update_date", aaUpdateDate[rr]);
			sp.ppstr("update_time", aaUpdateTime[rr]);
			sp.ppstr("mod_user", wp.loginUser);
			sp.addsql(", mod_time ", ", sysdate ");			
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.ppnum("mod_seqno", 1);
			sp.ppstr("job_code", lsFitDeptno);
			sp.ppstr("vouch_job_code", "0" + strMid(lsGlcode, 0, 1));
			sqlExec(sp.sqlStmt(), sp.sqlParm());			
			
//			is_sql = "insert into act_debt_cancel ( "
//					+ "batch_no, 	serial_no, 		p_seqno, acno_p_seqno,		acct_type, 		id_p_seqno, "
//				//+ "id, 			id_code, 		pay_card_no,	pay_amt, 		pay_date, "
//					+ "pay_card_no,	pay_amt, 		pay_date, "
//					+ "vouch_memo3, payment_type, 	update_user, 	update_date, 	update_time, "
//					+ "mod_user, 	mod_time, 		mod_pgm, 		mod_seqno, 		job_code, vouch_job_code "
//					+ ") values ( "
//					+ ":batch_no, 	:serial_no, 	:p_seqno, :acno_p_seqno,		:acct_type, 	:id_p_seqno, "
//				//+ ":id, 		:id_code, 		:pay_card_no, 	:pay_amt, 		:pay_date, "
//					+ ":pay_card_no, 	:pay_amt, 		:pay_date, "
//					+ ":vouch_memo3,:payment_type,  :update_user, 	:update_date, 	:update_time, "
//					+ ":mod_user, 	sysdate, 		'actp0060', 	0, 				:job_code, 	 "
//					+ ":vouch_job_code ) ";
//			setString("batch_no", ls_val);
//			setString("serial_no", aa_serial_no[rr]);
//			setString("p_seqno", aa_p_seqno[rr]);
//			setString("acno_p_seqno", aa_acno_p_seqno[rr]);
//			setString("acct_type", aa_acct_type[rr]);
//			setString("id_p_seqno", aa_id_p_seqno[rr]);
//		//setString("id", aa_id[rr]);
//		//setString("id_code", aa_id_code[rr]);
//			setString("pay_card_no", aa_pay_card_no[rr]);
//			setString("pay_amt", num_2str(li_atm_fee, "###"));
//			setString("pay_date", aa_pay_date[rr]);
//			setString("vouch_memo3", "");
//			setString("payment_type", "COMA");
//		//setString("update_user", aa_crt_user[rr]);
//		//setString("update_date", aa_crt_date[rr]);
//		//setString("update_time", aa_crt_time[rr]);
//			setString("update_user", aa_update_user[rr]);
//			setString("update_date", aa_update_date[rr]);
//			setString("update_time", aa_update_time[rr]);
//			setString("mod_user", wp.loginUser);
//			setString("job_code", ls_fit_deptno);
//			setString("vouch_job_code", "0" + ss_mid(ls_glcode, 0, 1));
//			sqlExec(is_sql);
		}		
		if (sqlRowNum <= 0) {
			wp.colSet(rr, "err_msg", "Insert(Update) to act_debt_cancel 2 error");
			wp.colSet(rr, "ok_flag", "X");
			return -1;
		} else {
			wp.colSet(rr, "ok_flag", "V");
		}
		return 1;
	}

	public int updActAcno(int rr) throws Exception {
		// update act_acno
		String usSql = "";
		usSql = "update act_acno "
				+ "set atm_pay_flag = 'Y', "
				+ "mod_user =:mod_user, "
				+ "mod_time = sysdate, "
				+ "mod_pgm = 'actp0060', "
				+ "mod_seqno = nvl(mod_seqno,0)+1 "
				+ "where  acct_type = :acct_type "
				+ "and    acct_key  = :acct_key ";
		setString("mod_user", wp.loginUser);
		setString("acct_type", aaAcctType[rr]);
		setString("acct_key", aaAcctKey[rr]);
		sqlExec(usSql);
		if (sqlRowNum <= 0) {
			wp.colSet(rr, "err_msg", "Update act_acno Error");
			wp.colSet(rr, "ok_flag", "X");
			return -1;
		} else {
			wp.colSet(rr, "ok_flag", "V");
		}
		return 1;
	}
}
