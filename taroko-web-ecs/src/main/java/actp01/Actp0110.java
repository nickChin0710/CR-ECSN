/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*            V1.00.00  ???        program initial                            *
* 109-06-18  v1.00.01  Andy       Update : Mantis 3644                       *
* 111-10-25  v1.00.02  Yang Bo    Sync code from mega                        *
* 111-11-14  V1.00.03  Simon      1.cancel autopay_indicator='3'             *
*                                 2.update autopay_acct_no data into act_acno & act_acct_curr for bank 006*
******************************************************************************/
package actp01;

import java.text.SimpleDateFormat;
import java.util.Date;

import ofcapp.BaseAction;

public class Actp0110 extends BaseAction {
	String lsAcctKey = "";

	@Override
	public void userAction() throws Exception {
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
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			procFunc();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			procFunc();
		}

	}

	@Override
	public void dddwSelect() {
		try {
			if (eqIgno(wp.respHtml, "Actp0110")) {
				wp.optionKey = wp.colStr(0, "ex_acct_type");
				dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "acct_type", "where 1=1");
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		lsAcctKey = wp.itemStr("ex_acct_key");

		if (!wp.itemEmpty("ex_acct_key")) {

			if (lsAcctKey.length() < 8) {
				errmsg("帳戶帳號至少輸入8碼");
				return;
			}

			lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));
			if (lsAcctKey.length() != 11) {
				errmsg("帳戶帳號輸入錯誤 !");
				return;
			}
		}

		/*
		 * if(wp.item_empty("ex_acct_key") && wp.item_empty("ex_user")) {
		 * 
		 * //errmsg("帳戶帳號和登入人員至少需輸入一項"); err_alert("帳戶帳號和登入人員至少需輸入一項"); return;
		 * }
		 */

		// String ls_where = " where 1=1 and nvl(curr_code,'901')='901' "
		String lsWhere = " where 1=1 and uf_nvl(a.curr_code,'901')='901' "
				+ " and uf_nvl(a.ad_mark,'A') != 'D' "
				+ " and a.p_seqno = b.acno_p_seqno "
				+ sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type")
				// + sqlCol(ls_acct_key,"uf_acno_key(p_seqno)") 執行時間較久
				+ sqlCol(lsAcctKey,"b.acct_key") 
				+ sqlCol(wp.itemStr("ex_verify"), "uf_nvl(a.exec_check_flag,'N')")
				+ sqlCol(wp.itemStr("ex_user"), "a.crt_user", "like%");

    /***
		if (!empty(ls_acct_key)) {
			ls_where += " and p_seqno in "
					+ " (select acno_p_seqno from act_acno where 1=1 "
					+ sqlCol(wp.itemStr("ex_acct_type"), "acct_type")
					+ sqlCol(ls_acct_key, "acct_key")
					+ " ) ";
		}
    ***/

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " a.acct_type ,"
				+ " b.acct_key ,"
			//+ " (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = act_chkno.id_p_seqno) as id_no, "
				+ "(case when b.acno_flag = '2' then substr(b.acct_key,1,8) "
				+ " when b.acno_flag = '3' then (select id_no||'-'||id_no_code from crd_idno where id_p_seqno = a.id_p_seqno) "
				+ " else substr(b.acct_key,1,10)||'-'||substr(b.acct_key,11,1) end) as id_no, "
				+ " a.autopay_acct_bank ,"
				+ " a.autopay_acct_no ,"
				+ " a.valid_flag ,"
				+ " decode(a.valid_flag,'1','即時生效','2','Cycle生效') as tt_valid_flag , "
				+ " a.autopay_indicator ,"
				+ " decode(a.autopay_indicator,'1','TTL','2','MP') as tt_autopay_indicator ,"
			//+ " a.autopay_fix_amt ,"
			//+ " a.autopay_rate ,"
				+ " a.autopay_acct_s_date ,"
				+ " a.autopay_acct_e_date ,"
				+ " a.from_mark ,"
				+ " decode(a.from_mark,'1','新製卡','01','APS','02','授權書-新申請','03','授權書-修改帳號',a.from_mark) as tt_from_mark ,"
				+ " a.verify_flag ,"
				+ " a.autopay_id ,"
				+ " a.autopay_id_code ,"
				+ " a.crt_date ,"
				+ " a.p_seqno ,"
				+ " a.exec_check_flag ,"
				+ " decode(a.exec_check_flag,'','待放行','N','待放行','Y','解放行') as tt_exec_check_flag , "
				+ " a.verify_return_code ,"
				+ " decode(a.verify_return_code,'00','成功','01','失敗','99','免驗印') as tt_verify_return_code , "
				+ " a.id_p_seqno ,"
				+ " a.verify_date ,"
				+ " to_char(a.mod_time,'yyyy-MM-dd HH24:mi:ss') mod_time ,"
				+ " a.mod_user ,"
				+ " a.crt_user , "
				+ " a.crt_date , "
				+ " a.crt_time ,"
				// + " to_char (to_date ( (crt_date || crt_time),'YYYY-MM-DD
				// HH24:MI:SS'),'yyyy-MM-dd HH24:mi:ss') tt_crt_time, "
				+ " '' tt_crt_time , "
				+ " hex(a.rowid) as rowid ,"
			//+ " effc_flag ,"
				+ " decode(a.effc_flag,'','Y',a.effc_flag) as effc_flag , "
				+ " a.stmt_cycle ,"
			//+ " uf_idno_name(a.id_p_seqno) as chi_name ,"
				+ "(case when b.acno_flag = '2' then uf_corp_name(b.corp_p_seqno) "
				+ " else uf_idno_name(a.id_p_seqno) end) as chi_name, "
				+ " a.ibm_check_flag , "
				+ " a.ach_check_flag   ";
		wp.daoTable = " act_chkno a, act_acno b ";
		wp.whereOrder = " order by a.autopay_acct_bank Asc, a.crt_date Asc , a.autopay_id_code Asc , a.autopay_id Asc ";
		pageQuery();
		if (this.sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}
		queryAfter();
		wp.setListCount(0);
		wp.setPageValue();
    apprDisabled("crt_user");

	}

	void queryAfter() throws Exception {
		String sql1 = " select "
				+ " bank_name "
				+ " from act_ach_bank "
				+ " where bank_no like ?||'%' "
				+ " fetch first 1 row only ";
		String wkCrtDate = "", wkCrtTime = "", ttCrtTime = "",wkDateTime="";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "autopay_acct_bank") });
			if (sqlRowNum > 0) {
				wp.colSet(ii, "tt_autopay_acct_bank", sqlStr("bank_name"));
			}
			//20200623 針對新舊系統三種格式crt_time處理方式			
			wkCrtDate = wp.colStr(ii, "crt_date");
			wkCrtTime = wp.colStr(ii, "crt_time");
			wkDateTime =  wp.colStr(ii, "crt_date")+wp.colStr(ii, "crt_time");
			//14碼日期+時間
			if (wkCrtTime.length() == 14) {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = format.parse(wkCrtTime);
				SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				ttCrtTime = format1.format(date);
			}
			//8碼時間HH:mm:ss
			if (wkCrtTime.length() == 8) {
				SimpleDateFormat format3 = new SimpleDateFormat("yyyyMMdd");
				Date date = (Date) format3.parse(wkCrtDate);
				SimpleDateFormat format4 = new SimpleDateFormat("yyyy-MM-dd");
				ttCrtTime = format4.format(date) + " " + wkCrtTime;
			}
			//6碼時間
			if (wkCrtTime.length() == 6) {
				SimpleDateFormat form5 = new SimpleDateFormat("yyyyMMddHHmmss");				
				Date date = (Date) form5.parse(wkDateTime);
				SimpleDateFormat format6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				ttCrtTime = format6.format(date);
			}
			wp.colSet(ii, "tt_crt_time", ttCrtTime);
		}
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void procFunc() throws Exception {
		int llOk = 0, llErr = 0, llAprUserErr = 0;
		String[] lsRowid = wp.itemBuff("rowid");
		String[] lsAcctType = wp.itemBuff("acct_type");
		String[] lsAcctKey = wp.itemBuff("acct_key");
		String[] lsAutopayAcctBank = wp.itemBuff("autopay_acct_bank");
		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] lsAutopayAcctNo = wp.itemBuff("autopay_acct_no");
		String[] lsAutopayId = wp.itemBuff("autopay_id");
		String[] lsAutopayIdCode = wp.itemBuff("autopay_id_code");
		String[] lsAutopayIndicator = wp.itemBuff("autopay_indicator");
		String[] lsExecCheckFlag = wp.itemBuff("exec_check_flag");
		String[] lsVerifyFlag = wp.itemBuff("verify_flag");
		String[] lsIbmCheckFlag = wp.itemBuff("ibm_check_flag");
		String[] lsAchCheckFlag = wp.itemBuff("ach_check_flag");
		String[] lsCrtUser = wp.itemBuff("crt_user");
		String[] aaOpt = wp.itemBuff("opt");
		String[] lsAutopayAcctSDate = wp.itemBuff("autopay_acct_s_date");
		String[] lsAutopayAcctEDate = wp.itemBuff("autopay_acct_e_date");

		Actp0110Func func = new Actp0110Func();
		func.setConn(wp);

		wp.listCount[0] = wp.itemRows("rowid");
		for (int ii = 0; ii < wp.itemRows("rowid"); ii++) {
			if (checkBoxOptOn(ii, aaOpt) == false)
				continue;

      /*** 已改成 opt 欄位 protected
			if(wp.loginUser.equals(ls_crt_user[ii]) ) {
        ll_apr_user_err++;
				wp.colSet(ii,"ok_flag", "X");
				wp.colSet(ii, "err_msg", "覆核人員及經辦不能為同一人！");
				ll_err++;
				continue;
			}
      ***/ 
			
			func.varsSet("rowid", lsRowid[ii]);
			func.varsSet("acct_type", lsAcctType[ii]);
			func.varsSet("acct_key", lsAcctKey[ii]);
			func.varsSet("autopay_acct_bank", lsAutopayAcctBank[ii]);
			func.varsSet("p_seqno", lsPSeqno[ii]);
			func.varsSet("autopay_acct_no", lsAutopayAcctNo[ii]);
			func.varsSet("autopay_id", lsAutopayId[ii]);
			func.varsSet("autopay_id_code", lsAutopayIdCode[ii]);
			func.varsSet("autopay_indicator", lsAutopayIndicator[ii]);
			func.varsSet("exec_check_flag", lsExecCheckFlag[ii]);
			func.varsSet("verify_flag", lsVerifyFlag[ii]);
			func.varsSet("ibm_check_flag", lsIbmCheckFlag[ii]);
			func.varsSet("ach_check_flag", lsAchCheckFlag[ii]);
			func.varsSet("autopay_acct_s_date", lsAutopayAcctSDate[ii]);
			func.varsSet("autopay_acct_e_date", lsAutopayAcctEDate[ii]);

			rc = func.dataProc();
			if (rc != 1) {
				llErr++;
				wp.colSet(ii, "ok_flag", "X");
				wp.colSet(ii, "err_msg", func.getMsg());
  			sqlCommit(0);
				continue;
			} else {
				llOk++;
				wp.colSet(ii, "ok_flag", "V");
  			sqlCommit(1);
				continue;
			}
		}

    /*** 改成 opt 欄位 protected
		if(ll_apr_user_err > 0){
  		alert_msg("覆核人員及經辦不能為同一人！",true);
		}	
    ***/ 

		if (llOk > 0) {
			sqlCommit(1);
		}

		alertMsg("執行完畢,成功:" + llOk + " 失敗:" + llErr);

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
