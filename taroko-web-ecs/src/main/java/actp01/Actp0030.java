/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-05  V1.00.00  OrisChang  program initial                            *
* 107-08-20  V1.00.01  Alex       fixed queryFunc errmsg                     *
* 107-08-21  V1.00.02  Alex       fixed queryFunc errmsg                     *
* 108-03-04  V1.00.03  Andy       fixed queryFunc                            * 
* 108-11-15  V1.00.04  Andy       add insert table: ACT_PAY_RECORD           *  
* 109-01-16  V1.00.05  Andy       update: Mantis2416                         *
* 109-04-15  V1.00.06  Alex       add auth_query						     *
* 111-10-24  V1.00.07  Yang Bo    sync code from mega                        *
******************************************************************************/

package actp01;

import busi.SqlPrepare;
import busi.func.ColFunc;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actp0030 extends BaseEdit {
	String pPSeqno = "";

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
			saveFunc();
			// updateFunc();
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
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
		//設定初始值
		wp.pageRows = 5;
	}

	@Override
	public void queryFunc() throws Exception {
		String lsAcctKey = "";
		if (chkStrend(wp.itemStr("q_s_date"), wp.itemStr("q_e_date")) == false) {
			alertErr("異動日期: 起迄錯誤");
			return;
		}

		if (!wp.itemEmpty("ex_acct_key")) {
			lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));
			if (lsAcctKey.length() != 11) {
				alertErr("帳戶帳號輸入錯誤");
				return;
			}
		}

		ColFunc func =new ColFunc();
		func.setConn(wp);
		if (func.fAuthQuery(wp.modPgm(), commString.mid(lsAcctKey, 0,10))!=1) { 
		   	alertErr(func.getMsg()); 
		   	return ; 
		} 
			
	//String ls_where = " where 1=1 and B.act_modtype='02' "
		String lsWhere = " where 1=1 and B.act_modtype in ('02','0b') "
				+ sqlStrend(wp.itemStr("q_s_date"), wp.itemStr("q_e_date"), "B.update_date")
				+ sqlCol(wp.itemStr("ex_update_user"), "B.update_user")
				+ sqlCol(lsAcctKey, "A.acct_key");

		// -page control-
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " B.p_seqno ,"
				+ " B.act_modtype ,"
				+ " B.acct_type ,"
				+ " B.acct_data ,"
				+ " B.update_user ,"
				+ " B.update_date ,"
				+ " A.corp_p_seqno ,"
				+ " A.id_p_seqno ,"
				+ " A.acct_key ,"
				+ " A.payment_rate1 as PRO1 ,"
				+ " A.payment_rate2 as PRO2 ,"
				+ " A.payment_rate3 as PRO3 ,"
				+ " A.payment_rate4 as PRO4 ,"
				+ " A.payment_rate5 as PRO5 ,"
				+ " A.payment_rate6 as PRO6 ,"
				+ " A.payment_rate7 as PRO7 ,"
				+ " A.payment_rate8 as PRO8 ,"
				+ " A.payment_rate9 as PRO9 ,"
				+ " A.payment_rate10 as PRO10 ,"
				+ " A.payment_rate11 as PRO11 ,"
				+ " A.payment_rate12 as PRO12 ,"
				+ " A.payment_rate13 as PRO13 ,"
				+ " A.payment_rate14 as PRO14 ,"
				+ " A.payment_rate15 as PRO15 ,"
				+ " A.payment_rate16 as PRO16 ,"
				+ " A.payment_rate17 as PRO17 ,"
				+ " A.payment_rate18 as PRO18 ,"
				+ " A.payment_rate19 as PRO19 ,"
				+ " A.payment_rate20 as PRO20 ,"
				+ " A.payment_rate21 as PRO21 ,"
				+ " A.payment_rate22 as PRO22 ,"
				+ " A.payment_rate23 as PRO23 ,"
				+ " A.payment_rate24 as PRO24 ,"
				+ " A.payment_rate25 as PRO25 ,"
				+ " A.stmt_cycle ,"
				+ " uf_acno_name(A.p_seqno) as chi_name ,"
				+ " A.card_indicator ,"
				+ " C.this_acct_month as this_acct_month, " 
				+ " uf_date_add(C.this_acct_month,0,-1,0) as mh1 ," // **本次帳務月份 -1 Andy 20190304
				+ " uf_date_add(C.this_acct_month,0,-2,0) as mh2 ,"
				+ " uf_date_add(C.this_acct_month,0,-3,0) as mh3 ,"
				+ " uf_date_add(C.this_acct_month,0,-4,0) as mh4 ,"
				+ " uf_date_add(C.this_acct_month,0,-5,0) as mh5 ,"
				+ " uf_date_add(C.this_acct_month,0,-6,0) as mh6 ,"
				+ " uf_date_add(C.this_acct_month,0,-7,0) as mh7 ,"
				+ " uf_date_add(C.this_acct_month,0,-8,0) as mh8 ,"
				+ " uf_date_add(C.this_acct_month,0,-9,0) as mh9 ,"
				+ " uf_date_add(C.this_acct_month,0,-10,0) as mh10 ,"
				+ " uf_date_add(C.this_acct_month,0,-11,0) as mh11 ,"
				+ " uf_date_add(C.this_acct_month,0,-12,0) as mh12 ,"
				+ " uf_date_add(C.this_acct_month,0,-13,0) as mh13 ,"
				+ " uf_date_add(C.this_acct_month,0,-14,0) as mh14 ,"
				+ " uf_date_add(C.this_acct_month,0,-15,0) as mh15 ,"
				+ " uf_date_add(C.this_acct_month,0,-16,0) as mh16 ,"
				+ " uf_date_add(C.this_acct_month,0,-17,0) as mh17 ,"
				+ " uf_date_add(C.this_acct_month,0,-18,0) as mh18 ,"
				+ " uf_date_add(C.this_acct_month,0,-19,0) as mh19 ,"
				+ " uf_date_add(C.this_acct_month,0,-20,0) as mh20 ,"
				+ " uf_date_add(C.this_acct_month,0,-21,0) as mh21 ,"
				+ " uf_date_add(C.this_acct_month,0,-22,0) as mh22 ,"
				+ " uf_date_add(C.this_acct_month,0,-23,0) as mh23 ,"
				+ " uf_date_add(C.this_acct_month,0,-24,0) as mh24 ,"
				+ " uf_date_add(C.this_acct_month,0,-25,0) as mh25  ";
			//+ " to_char(B.mod_time,'yyyymmdd') as crt_date , "
			//+ " B.mod_user as crt_user ";
		wp.daoTable = " act_moddata_tmp B join act_acno A on A.acno_p_seqno=B.p_seqno join ptr_workday C on C.stmt_cycle=A.stmt_cycle ";
		pageQuery();

		if (this.sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}

		wp.setListCount(0);
		wp.setPageValue();
		queryAfter();
    apprDisabled("update_user");

	}

	void queryAfter() {
		int ilSelectCnt = 0;
		ilSelectCnt = wp.selectCnt;
		String dbMh1 = "", dbMh2 = "", dbMh3 = "", dbMh4 = "", dbMh5 = "", dbMh6 = "", dbMh7 = "", dbMh8 = "",
				dbMh9 = "", dbMh10 = "", dbMh11 = "", dbMh12 = "", dbMh13 = "", dbMh14 = "", dbMh15 = "", dbMh16 = "",
				dbMh17 = "", dbMh18 = "", dbMh19 = "", dbMh20 = "", dbMh21 = "", dbMh22 = "", dbMh23 = "", dbMh24 = "",
				dbMh25 = "";

		String dbPro1 = "", dbPro2 = "", dbPro3 = "", dbPro4 = "", dbPro5 = "", dbPro6 = "", dbPro7 = "",
				dbPro8 = "", dbPro9 = "", dbPro10 = "", dbPro11 = "", dbPro12 = "", dbPro13 = "", dbPro14 = "",
				dbPro15 = "", dbPro16 = "", dbPro17 = "", dbPro18 = "", dbPro19 = "", dbPro20 = "", dbPro21 = "",
				dbPro22 = "", dbPro23 = "", dbPro24 = "", dbPro25 = "";

		String[] tt = new String[2];
		for (int ii = 0; ii < ilSelectCnt; ii++) {
			tt[0] = wp.colStr(ii, "acct_data");
			tt = commString.token(tt, "@");
			dbMh1 = tt[1];
			tt = commString.token(tt, "@");
			dbPro1 = tt[1];
			tt = commString.token(tt, "@");
			dbMh2 = tt[1];
			tt = commString.token(tt, "@");
			dbPro2 = tt[1];
			tt = commString.token(tt, "@");
			dbMh3 = tt[1];
			tt = commString.token(tt, "@");
			dbPro3 = tt[1];
			tt = commString.token(tt, "@");
			dbMh4 = tt[1];
			tt = commString.token(tt, "@");
			dbPro4 = tt[1];
			tt = commString.token(tt, "@");
			dbMh5 = tt[1];
			tt = commString.token(tt, "@");
			dbPro5 = tt[1];
			tt = commString.token(tt, "@");
			dbMh6 = tt[1];
			tt = commString.token(tt, "@");
			dbPro6 = tt[1];
			tt = commString.token(tt, "@");
			dbMh7 = tt[1];
			tt = commString.token(tt, "@");
			dbPro7 = tt[1];
			tt = commString.token(tt, "@");
			dbMh8 = tt[1];
			tt = commString.token(tt, "@");
			dbPro8 = tt[1];
			tt = commString.token(tt, "@");
			dbMh9 = tt[1];
			tt = commString.token(tt, "@");
			dbPro9 = tt[1];
			tt = commString.token(tt, "@");
			dbMh10 = tt[1];
			tt = commString.token(tt, "@");
			dbPro10 = tt[1];
			tt = commString.token(tt, "@");
			dbMh11 = tt[1];
			tt = commString.token(tt, "@");
			dbPro11 = tt[1];
			tt = commString.token(tt, "@");
			dbMh12 = tt[1];
			tt = commString.token(tt, "@");
			dbPro12 = tt[1];
			tt = commString.token(tt, "@");
			dbMh13 = tt[1];
			tt = commString.token(tt, "@");
			dbPro13 = tt[1];
			tt = commString.token(tt, "@");
			dbMh14 = tt[1];
			tt = commString.token(tt, "@");
			dbPro14 = tt[1];
			tt = commString.token(tt, "@");
			dbMh15 = tt[1];
			tt = commString.token(tt, "@");
			dbPro15 = tt[1];
			tt = commString.token(tt, "@");
			dbMh16 = tt[1];
			tt = commString.token(tt, "@");
			dbPro16 = tt[1];
			tt = commString.token(tt, "@");
			dbMh17 = tt[1];
			tt = commString.token(tt, "@");
			dbPro17 = tt[1];
			tt = commString.token(tt, "@");
			dbMh18 = tt[1];
			tt = commString.token(tt, "@");
			dbPro18 = tt[1];
			tt = commString.token(tt, "@");
			dbMh19 = tt[1];
			tt = commString.token(tt, "@");
			dbPro19 = tt[1];
			tt = commString.token(tt, "@");
			dbMh20 = tt[1];
			tt = commString.token(tt, "@");
			dbPro20 = tt[1];
			tt = commString.token(tt, "@");
			dbMh21 = tt[1];
			tt = commString.token(tt, "@");
			dbPro21 = tt[1];
			tt = commString.token(tt, "@");
			dbMh22 = tt[1];
			tt = commString.token(tt, "@");
			dbPro22 = tt[1];
			tt = commString.token(tt, "@");
			dbMh23 = tt[1];
			tt = commString.token(tt, "@");
			dbPro23 = tt[1];
			tt = commString.token(tt, "@");
			dbMh24 = tt[1];
			tt = commString.token(tt, "@");
			dbPro24 = tt[1];
			tt = commString.token(tt, "@");
			dbMh25 = tt[1];
			tt = commString.token(tt, "@");
			dbPro25 = tt[1];

			for (int ll = 1; ll < 26; ll++) {
				if (eqIgno(dbMh1, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro1);
					if (eqIgno(dbPro1, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh2, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro2);
					if (eqIgno(dbPro2, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh3, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro3);
					if (eqIgno(dbPro3, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh4, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro4);
					if (eqIgno(dbPro4, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh5, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro5);
					if (eqIgno(dbPro5, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh6, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro6);
					if (eqIgno(dbPro6, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh7, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro7);
					if (eqIgno(dbPro7, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh8, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro8);
					if (eqIgno(dbPro8, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh9, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro9);
					if (eqIgno(dbPro9, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh10, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro10);
					if (eqIgno(dbPro10, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh11, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro11);
					if (eqIgno(dbPro11, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh12, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro12);
					if (eqIgno(dbPro12, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh13, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro13);
					if (eqIgno(dbPro13, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh14, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro14);
					if (eqIgno(dbPro14, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh15, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro15);
					if (eqIgno(dbPro15, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh16, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro16);
					if (eqIgno(dbPro16, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh17, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro17);
					if (eqIgno(dbPro17, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh18, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro18);
					if (eqIgno(dbPro18, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh19, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro19);
					if (eqIgno(dbPro19, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh20, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro20);
					if (eqIgno(dbPro20, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh21, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro21);
					if (eqIgno(dbPro21, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh22, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro22);
					if (eqIgno(dbPro22, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh23, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro23);
					if (eqIgno(dbPro23, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh24, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro24);
					if (eqIgno(dbPro24, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
				if (eqIgno(dbMh25, wp.colStr(ii, "mh" + ll))) {
					wp.colSet(ii, "PR" + ll, dbPro25);
					if (eqIgno(dbPro25, wp.colStr(ii, "PRO" + ll)) == false) {
						wp.colSet(ii, "PRcolor" + ll, "red");
					}
					continue;
				}
			}

			for (int mm = 1; mm < 26; mm++) {
				if (!empty(wp.colStr(ii, "PRO" + mm)) && empty(wp.colStr(ii, "PR" + mm))) {
					 wp.colSet(ii, "PR" + mm, wp.colStr(ii, "PRO" + mm));
				}
		  }

		}

	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		// ProcModDataTmp();
	}

	@Override
	public void saveFunc() throws Exception {
		Actp0030Func func = new Actp0030Func(wp);
		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] opt = wp.itemBuff("opt");
		String[] aaPSeqno = wp.itemBuff("p_seqno");
		String[] aaAcctKey = wp.itemBuff("acct_key");
		String[] aaAcctType = wp.itemBuff("acct_type");
		//String[] aa_old_payment_rate = wp.item_buff("old_payment_rate");
		//String[] aa_payment_rate = wp.item_buff("payment_rate");
		//String[] aa_txn_type = wp.item_buff("txn_type");
		String[] aaThisAcctMonth = wp.itemBuff("this_acct_month");
		String[] aaUpdateDate = wp.itemBuff("update_date");
		String[] aaAaUpdateUserpdateUser = wp.itemBuff("update_user");

		String lsSql = "", lsSql1 = "";
		String lsUpd = "", lsBusDate = "";
		String lsMc = "", lsOmc = "";
		int rr = -1;
		int ii2 = 0;
		int liOk = 0;
		lsSql = "select business_date from ptr_businday ";
		sqlSelect(lsSql);
		lsBusDate = sqlStr("business_date");

		for (int ll = 0; ll < opt.length; ll++) {
			// -option-ON-
			rr = this.optToIndex(opt[ll]);
			if (rr < 0)
				continue;
			// 20191115 add
			if (insertActPayRecord(rr) != 1) {
				alertErr("覆核處理失敗!! 寫入 act_pay_record 失敗!! (同一個人一天只能調整一次評等..)");
				return;
			}
			// 20200115 Mantis2416
			String lsThisAcctMm = aaThisAcctMonth[rr];
			
			for (int ii = 0; ii < 13; ii++) {
				lsOmc = wp.itemBuff("PRO" + String.valueOf(ii + 1))[rr];
				lsMc = wp.itemBuff("PR" + String.valueOf(ii + 1))[rr];
				if (lsOmc.equals(lsMc) || empty(lsMc)) {
					continue;
				}
				lsSql = "select count(*) ct from act_jcic_txn where p_seqno =:p_seqno "
						+ "and business_date =:business_date "
						+ "and payment_num =:payment_num ";
				setString("p_seqno",aaPSeqno[rr]);
				setString("business_date",lsBusDate);
				setString("payment_num",intToStr(ii+1));
				sqlSelect(lsSql);
				if(sqlNum("ct") > 0){
					String dsSql = "delete act_jcic_txn where p_seqno =:p_seqno "
						+ "and business_date =:business_date "
						+ "and payment_num =:payment_num ";
					setString("p_seqno",aaPSeqno[rr]);
					setString("business_date",lsBusDate);
					setString("payment_num",intToStr(ii+1));
					sqlExec(dsSql);
				}
				SqlPrepare sp = new SqlPrepare();
				sp.sql2Insert("act_jcic_txn");
				sp.ppstr("p_seqno", aaPSeqno[rr]);
				sp.ppstr("business_date", lsBusDate);
				sp.ppnum("payment_num", ii+1);
				sp.ppstr("acct_key", aaAcctKey[rr]);
				sp.ppstr("acct_type", aaAcctType[rr]);
			//ii2 = ii + 1;
			//sp.ppss("this_acct_month", wp.col_ss(ll, "mh" + ii2));
			  sp.ppstr("this_acct_month", lsThisAcctMm);
				sp.ppstr("proc_flag", "N");
				sp.ppstr("proc_date", "");
				sp.ppstr("old_payment_rate", lsOmc);
				sp.ppstr("payment_rate", lsMc);
				sp.ppstr("txn_type", "");
				sp.ppstr("apr_flag", "Y");
			//sp.ppss("apr_date", get_sysDate());
			//sp.ppss("apr_date", sysDate);
	  		sp.addsql(", apr_date ", ", to_char(sysdate,'yyyymmdd') ");			
				sp.ppstr("apr_user", wp.loginUser);
			//sp.ppss("update_date", get_sysDate());
			//sp.ppss("update_user", wp.loginUser);
				sp.ppstr("update_date", aaUpdateDate[rr]);
				sp.ppstr("update_user", aaAaUpdateUserpdateUser[rr]);
				sp.ppstr("mod_user", wp.loginUser);
				sp.addsql(", mod_time ", ", sysdate ");
				sp.ppstr("mod_pgm", wp.modPgm());
				sp.ppnum("mod_seqno", 1);
				sqlExec(sp.sqlStmt(), sp.sqlParm());
				if (sqlRowNum <= 0) {
					errmsg(sqlErrtext);
					return;
				}				
			}
			
			//
			lsSql = "update act_acno set ";
			lsUpd = "";
			for (int i = 0; i < 25; i++) {
				if (!wp.itemBuff("PRO" + String.valueOf(i + 1))[rr].equals(wp.itemBuff("PR" + String.valueOf(i + 1))[rr])) {
					if (lsUpd.length() > 0)
						lsUpd += ",";
					lsUpd += " payment_rate" + String.valueOf(i + 1) + "= :pr" + String.valueOf(i + 1);
					setString("pr" + String.valueOf(i + 1), wp.itemBuff("PR" + String.valueOf(i + 1))[rr]);
				}
			}
			if (empty(lsUpd)) {
				alertErr("覆核處理失敗!! 繳款評等資料無異動!!");
				return;
			}
			lsSql += lsUpd + " where acno_p_seqno = :p_seqno ";
			setString("p_seqno", lsPSeqno[rr]);
			sqlExec(lsSql);
			if (sqlRowNum <= 0) {
				alertErr("覆核處理失敗!! update act_acno error!");
				return;
			}

			if (sqlRowNum > 0)
				rc = func.dbDelete(lsPSeqno[rr]);
			this.sqlCommit(rc);
		  liOk++;
		}
	//alert_msg("覆核處理成功!!");
    if (liOk > 0 ) {
		  alertMsg("覆核處理成功!!");
    } else {
			alertErr("繳款評等資料無異動!!");
    }
		return;
		// queryRead();

	}

	int insertActPayRecord(int ll) throws Exception {
		// 20191115 add table:ACT_PAY_RECORD insert
		String lsSql = "",lsSql1 = "",lsBusDate = "";
		String[] aaPSeqno = wp.itemBuff("p_seqno");
		String[] aaIdPSeqno = wp.itemBuff("id_p_seqno");
		String[] aaAcctType = wp.itemBuff("acct_type");
		String[] aaAcctKey = wp.itemBuff("acct_key");
		String[] aaCorpPSeqno = wp.itemBuff("corp_p_seqno");

		lsSql = "select business_date from ptr_businday ";
		sqlSelect(lsSql);
		lsBusDate = sqlStr("business_date");

		lsSql1 = "select count(*) ct from act_pay_record where p_seqno =:p_seqno and acct_date =:acct_date";
		setString("p_seqno", aaPSeqno[ll]);
    setString("acct_date", lsBusDate);
		sqlSelect(lsSql1);
		if (sqlNum("ct") > 0) {
			return -1;
		}
		SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("act_pay_record");
		sp.ppstr("p_seqno", aaPSeqno[ll]);
		sp.ppstr("id_p_seqno", aaIdPSeqno[ll]);
		sp.addsql(", acct_date ", ", to_char(sysdate,'yyyymmdd') ");
		sp.ppstr("acct_type", aaAcctType[ll]);
		sp.ppstr("acct_key", aaAcctKey[ll]);
		if (!empty(aaCorpPSeqno[ll])) {
			sp.ppstr("corp_p_seqno", aaCorpPSeqno[ll]);
			lsSql1 = "select corp_no from crd_corp where corp_p_seqno =:corp_p_seqno ";
			setString("corp_p_seqno", aaCorpPSeqno[ll]);
			sqlSelect(lsSql1);
			sp.ppstr("corp_no", sqlStr("corp_no"));
		}
		for (int i = 0; i < 25; i++) {
			sp.ppstr("payment_rate" + String.valueOf(i + 1), wp.itemBuff("PR" + String.valueOf(i + 1))[ll]);
		}
		sp.ppstr("mod_user", wp.loginUser);
		sp.addsql(", mod_time ", ", sysdate ");
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppnum("mod_seqno", 1);
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			return -1;
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
			wp.optionKey = wp.itemStr("ex_update_user");
			this.dddwList("dddw_update_user", "sec_user", "usr_id", "usr_id||'_'||usr_cname", "where 1=1 order by usr_id");

		} catch (Exception ex) {
		}
	}

}
