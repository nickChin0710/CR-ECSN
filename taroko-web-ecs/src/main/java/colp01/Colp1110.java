/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Zhanghuheng     updated for project coding standard *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package colp01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colp1110 extends BaseProc {
	CommString commString = new CommString();
	Colp1110Func func;

//	String kk1="";
	String kkOptname = "";
	int totalCnt = 0;
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
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.colStr(0, "exCrtUser");
			dddwList("SecUserIDNameList", "sec_user", "usr_id", "usr_id||'['||usr_cname||']'",
					"where usr_type = '4' order by usr_id");
		} catch (Exception ex) {
		}
	}

	private boolean getWhereStr() throws Exception {
		String lsDate1 = wp.itemStr("exApplyDateS");
		String lsDate2 = wp.itemStr("exApplyDateE");
		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[協商申請日期-起迄]  輸入錯誤");
			return false;
		}

		String lsDate3 = wp.itemStr("exCrtDateS");
		String lsDate4 = wp.itemStr("exCrtDateE");
		if (this.chkStrend(lsDate3, lsDate4) == false) {
			alertErr2("[維護日期-起迄]  輸入錯誤");
			return false;
		}

		return true;
	}

	String getWhereStrA() throws Exception {
		String whereStr = "";

		whereStr = "where 1=1 " + "and proc_flag = '1' " + "and apr_flag = 'N' and crt_user <> '' ";

		if (empty(wp.itemStr("exId")) == false) {
			whereStr += "and id_p_seqno in (Select id_p_seqno From crd_idno Where id_no Like :id_no) ";
			setString("id_no", wp.itemStr("exId") + "%");
		}
		if (empty(wp.itemStr("exFromType")) == false) {
			whereStr += " and from_type = :from_type ";
			setString("from_type", wp.itemStr("exFromType"));
		}
		if (empty(wp.itemStr("exApplyDateS")) == false) {
			whereStr += " and apply_date >= :apply_dates ";
			setString("apply_dates", wp.itemStr("exApplyDateS"));
		}
		if (empty(wp.itemStr("exApplyDateE")) == false) {
			whereStr += " and apply_date <= :apply_datee ";
			setString("apply_datee", wp.itemStr("exApplyDateE"));
		}
		if (empty(wp.itemStr("exCrtUser")) == false) {
			whereStr += " and crt_user = :crt_user ";
			setString("crt_user", wp.itemStr("exCrtUser"));
		}
		if (empty(wp.itemStr("exCrtDateS")) == false) {
			whereStr += " and crt_date >= :crt_dates ";
			setString("crt_dates", wp.itemStr("exCrtDateS"));
		}
		if (empty(wp.itemStr("exCrtDateE")) == false) {
			whereStr += " and crt_date <= :crt_datee ";
			setString("crt_datee", wp.itemStr("exCrtDateE"));
		}

		return whereStr;
	}

	String getWhereStrB() throws Exception {
		String whereStr = "";

		whereStr = "where 1=1 " + "and proc_flag = '0' " + "and apr_flag = 'N' and crt_user <> '' ";

		if (empty(wp.itemStr("exId")) == false) {
			whereStr += "and id_p_seqno in (Select id_p_seqno From crd_idno Where id_no Like :id_no) ";
			setString("id_no", wp.itemStr("exId") + "%");
		}
		if (empty(wp.itemStr("exApplyDateS")) == false) {
			whereStr += " and apply_date >= :apply_dates ";
			setString("apply_dates", wp.itemStr("exApplyDateS"));
		}
		if (empty(wp.itemStr("exApplyDateE")) == false) {
			whereStr += " and apply_date <= :apply_datee ";
			setString("apply_datee", wp.itemStr("exApplyDateE"));
		}
		if (empty(wp.itemStr("exCrtUser")) == false) {
			whereStr += " and crt_user = :crt_user ";
			setString("crt_user", wp.itemStr("exCrtUser"));
		}
		if (empty(wp.itemStr("exCrtDateS")) == false) {
			whereStr += " and crt_date >= :crt_dates ";
			setString("crt_dates", wp.itemStr("exCrtDateS"));
		}
		if (empty(wp.itemStr("exCrtDateE")) == false) {
			whereStr += " and crt_date <= :crt_datee ";
			setString("crt_datee", wp.itemStr("exCrtDateE"));
		}

		return whereStr;
	}

	@Override
	public void queryFunc() throws Exception {
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (getWhereStr() == false)
			return;

		queryReadA();
		queryReadB();
	}

	void queryReadA() throws Exception {
		daoTid = "A-";

		wp.selectSQL = "liac_seqno, " + "id_p_seqno, " + "id_no, " + "chi_name, "
				+ "decode(id_no,'',chi_name,decode(chi_name,'',id_no,id_no||'_'||chi_name)) wk_id_cname, "
				+ "apply_date, " + "interest_base_date, " + "bank_code, " + "bank_name, "
				+ "decode(bank_code,'',bank_name,decode(bank_name,'',bank_code,bank_code||' '||bank_name)) wk_bank_code_name, "
				+ "auth_uncap_amt, " + "in_end_bal, " + "out_end_bal, " + "lastest_pay_amt, " + "in_end_bal_new, "
				+ "out_end_bal_new, " + "lastest_pay_amt_new, " + "crt_user, " + "crt_date, " + "apr_flag, "
				+ "apr_date, " + "apr_user, " + "mod_seqno, " + "ethic_risk_mark, " + "proc_flag, " + "proc_date, "
				+ "from_type, " + "decode(proc_flag, '', '0', proc_flag) db_proc, "
				+ "decode(from_type, '', '2', from_type) db_from, "
				+ "decode(not_send_flag, '', 'x', not_send_flag) db_send_flag, " + "acct_status_flag, "
				+ "not_send_flag, " + "has_rela_flag, " + "has_sup_flag, " + "debt_remark, " + "hex(rowid) as rowid, "
				+ "report_date, " + "no_calc_flag, " + "no_include_flag, " + "paper_report_flag, " + "out_capital, "
				+ "out_interest, " + "out_fee, " + "out_pn, " + "out_capital_new, " + "out_interest_new, "
				+ "out_fee_new, " + "out_pn_new ";

		wp.daoTable = "col_liac_debt";
		wp.whereOrder = "order by liac_seqno, id_no ";
		wp.whereStr = getWhereStrA();

		pageQuery();
		wp.setListCount(1);
		totalCnt = wp.selectCnt;

		listWkdataA();
	}

	void queryReadB() throws Exception {
		daoTid = "B-";

		wp.selectSQL = " liac_seqno " + " ,id_p_seqno " + " ,id_no " + " ,chi_name " + " ,jcic_notify_date "
				+ " ,apply_date " + " ,bank_code " + " ,bank_name " + " ,clearing_ym " + " ,apply_change_date "
				+ " ,remark " + " ,change_cnt " + " ,agree_flag " + " ,receipt_amt " + " ,card_debt "
				+ " ,cash_card_debt " + " ,payment_cnt " + " ,has_rela_flag " + " ,rela_consent_flag " + " ,proc_flag "
				+ " ,proc_date " + " ,crt_user " + " ,crt_date " + " ,apr_flag " + " ,apr_date " + " ,apr_user "
				+ " ,hex(rowid) as rowid " + " ,mod_seqno ";

		wp.daoTable = " col_liac_debt_ch ";
		wp.whereOrder = "order by liac_seqno, id_no ";
		wp.whereStr = getWhereStrB();

		pageQuery();
		wp.setListCount(2);
		totalCnt += wp.selectCnt;

		wp.notFound = "N";
		if (totalCnt == 0) {
			wp.notFound = "Y";
			alertErr(appMsg.errCondNodata);
			return;
		}

		listWkdataB();
	}

	void listWkdataA() throws Exception {
		String dbFrom = "";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			dbFrom = wp.colStr(ii, "a-db_from");
			wp.colSet(ii, "a-tt_db_from", commString.decode(dbFrom, ",1,2", ",人工複製,聯徵轉入"));
		}
	}

	void listWkdataB() throws Exception {
		String wkData = "";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, "b-liac_status");
			wp.colSet(ii, "b-tt_liac_status", commString.decode(wkData, ",1,2,3,4,5", ",1.受理申請,2.停催通知,3.簽約完成,4.結案/復催,5.結案/結清"));

			wkData = wp.colStr(ii, "b-rela_consent_flag");
			wp.colSet(ii, "b-tt_rela_consent_flag", commString.decode(wkData, ",1,2", ",1.是，且已徵提保證人同意書,2.是，未徵提保證人同意書"));

			wkData = wp.colStr(ii, "b-proc_flag");
			wp.colSet(ii, "b-tt_proc_flag", commString.decode(wkData, ",0,1", ",0.待通知,1.已通知"));

			wkData = wp.colStr(ii, "b-apr_user");
			wp.colSet(ii, "b-tt_apr_user", wfSecUserName(wkData));
		}
	}

	String wfSecUserName(String idcode) throws Exception {
		String rtn = "";
		String lsSql = "select usr_cname from sec_user " + "where usr_id = :id_code ";
		setString("id_code", idcode);
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			rtn = sqlStr("usr_cname");
		}
		return rtn;
	}

	@Override
	public void querySelect() throws Exception {

	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void dataProcess() throws Exception {
		func = new Colp1110Func(wp);

		// Detail Control
		detailControl();

		kkOptname = wp.itemStr("optname");
		switch (kkOptname) {
		case "aopt":
			dataProcessA();
			break;
		case "bopt":
			dataProcessB();
			break;
		}
	}

	public void dataProcessA() throws Exception {
		String[] lsRowid = wp.itemBuff("a-rowid");
		String[] lsModSeqno = wp.itemBuff("a-mod_seqno");
		String[] lsLiacSeqno = wp.itemBuff("a-liac_seqno");
		String[] lsNoIncludeFlag = wp.itemBuff("a-no_include_flag");
		String[] lsIdPSeqno = wp.itemBuff("a-id_p_seqno");
		String[] lsIdNo = wp.itemBuff("a-id_no");
		String[] opt = wp.itemBuff("aopt");

		// -insert-
		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			// 2018.5.8 有換頁的opt處理, 須扣掉(行數 x 頁數)//
//			rr = rr - (wp.pageRows * (wp.currPage -1));
			// 2018.5.8 end//
			if (rr < 0)
				continue;

			func.varsSet("rowid", lsRowid[rr]);
			func.varsSet("mod_seqno", lsModSeqno[rr]);
			func.varsSet("liac_seqno", lsLiacSeqno[rr]);
			func.varsSet("id_p_seqno", lsIdPSeqno[rr]);
			func.varsSet("id_no", lsIdNo[rr]);
			rc = func.dataProcA();
//			若【no_include_flag(不納入)】，
//			需update col_liac_nego、
//			  insert col_liac_remod、
//			  insert col_liac_nego_hst。
			if (eqIgno(lsNoIncludeFlag[rr], "Y") && rc == 1) {
				rc = func.updateColLiacNego();
				if (rc == 1)
					rc = func.insertColLiacRemod();
				if (rc == 1)
					rc = func.insertColLiacNegoHst();
			}
			sqlCommit(rc);

			if (rc == 1) {
				wp.colSet(rr, "ok_flag", "V");
				ilOk++;
				continue;
			}
			ilErr++;
			wp.colSet(rr, "ok_flag", "X");

		}
		// -re-Query-
		// queryRead();
		alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
	}

	public void dataProcessB() throws Exception {
		String[] lsRowid = wp.itemBuff("b-rowid");
		String[] lsModSeqno = wp.itemBuff("b-mod_seqno");
		String[] opt = wp.itemBuff("bopt");

		// -insert-
		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			// 2018.5.8 有換頁的opt處理, 須扣掉(行數 x 頁數)//
//			rr = rr - (wp.pageRows * (wp.currPage -1));
			// 2018.5.8 end//
			if (rr < 0)
				continue;

			func.varsSet("rowid", lsRowid[rr]);
			func.varsSet("mod_seqno", lsModSeqno[rr]);
			rc = func.dataProcB();
			sqlCommit(rc);
			if (rc == 1) {
				wp.colSet(rr, "b-ok_flag", "V");
				ilOk++;
				continue;
			}
			ilErr++;
			wp.colSet(rr, "b-ok_flag", "X");
		}
		// -re-Query-
		// queryRead();
		alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
	}

	void detailControl() {
		int rowcntaa = 0, rowcntbb = 0;
		String[] aaRowid = wp.itemBuff("a-rowid");
		String[] bbRowid = wp.itemBuff("b-rowid");
		if (!(aaRowid == null) && !empty(aaRowid[0]))
			rowcntaa = aaRowid.length;
		if (!(bbRowid == null) && !empty(bbRowid[0]))
			rowcntbb = bbRowid.length;
		wp.listCount[0] = rowcntaa;
		wp.listCount[1] = rowcntbb;
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
