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

public class Colp1220 extends BaseProc {
	Colp1220Func func;
	CommString commString = new CommString();

	//String kk1 = "";
	int ilOk = 0;
	int ilErr = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
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
			wp.optionKey = wp.colStr("exCrtUser");
			dddwList("SecUserIDNameList", "sec_user", "usr_id", "usr_id||'['||usr_cname||']'",
					"where usr_type = '4' order by usr_id");
		} catch (Exception ex) {
		}
	}

	private boolean getWhereStr() throws Exception {
		String lsDate1 = wp.itemStr("exDateS");
		String lsDate2 = wp.itemStr("exDateE");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[建檔日期-起迄]  輸入錯誤");
			return false;
		}

		wp.whereStr = "where 1=1 " + "and col_lgd_jrnl.apr_date = '' ";

		if (empty(wp.itemStr("exAcctKey")) == false) {
			wp.whereStr += " and act_acno.acct_key like :acct_key ";
			setString("acct_key", wp.itemStr("exAcctKey") + "%");
		}
		if (empty(wp.itemStr("exDateS")) == false) {
			wp.whereStr += " and col_lgd_jrnl.crt_date >= :crt_dates ";
			setString("crt_dates", wp.itemStr("exDateS"));
		}
		if (empty(wp.itemStr("exDateE")) == false) {
			wp.whereStr += " and col_lgd_jrnl.crt_date <= :crt_datee ";
			setString("crt_datee", wp.itemStr("exDateE"));
		}
		if (empty(wp.itemStr("exCrtUser")) == false) {
			wp.whereStr += " and col_lgd_jrnl.crt_user = :crt_user ";
			setString("crt_user", wp.itemStr("exCrtUser"));
		}

		// -page control-
		wp.queryWhere = wp.whereStr;

		return true;
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

		wp.selectSQL = "col_lgd_jrnl.p_seqno jrnl_p_seqno, " + "col_lgd_jrnl.acct_type, " + "col_lgd_jrnl.acct_date, "
				+ "col_lgd_jrnl.tran_type, " + "col_lgd_jrnl.interest_date, " + "col_lgd_jrnl.trans_amt, "
				+ "col_lgd_jrnl.send_date, " + "col_lgd_jrnl.crt_user, " + "col_lgd_jrnl.crt_date, "
				+ "col_lgd_jrnl.apr_date, " + "col_lgd_jrnl.mod_seqno, " + "hex(col_lgd_jrnl.rowid) as rowid, "
				+ "col_lgd_jrnl.lgd_coll_flag, " + "uf_acno_name(col_lgd_jrnl.p_seqno) db_cname, "
				+ "act_acno.acct_type acno_acct_type, " + "act_acno.acct_key acno_acct_key ";

		wp.daoTable = "col_lgd_jrnl "
//					+ "LEFT JOIN act_acno ON col_lgd_jrnl.p_seqno = act_acno.p_seqno";
				+ "LEFT JOIN act_acno ON col_lgd_jrnl.p_seqno = act_acno.acno_p_seqno";

		wp.whereOrder = "order by jrnl_p_seqno ";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		listWkdata();
		wp.setPageValue();
	}

	void listWkdata() throws Exception {
		String wkData = "";
//		String[] cde=new String[]{"AUT1","AUT2","COU1","TIKT","IBC1","IBC2","IBC3",
//				"IBA1","IBA2","IBA3","TEBK","INBK","IBOT","REFU","MIST","COMA","COMB","BON1","BON2","WAIP","BACK",
//				"DUMY","OTHR","COBO","AUT3","AUT4","AUT5","COU2","COU3","COU4","COU5","ACH1","IBC4","IBA4","EPAY","AUT6",
//				"COU6","AUT7","AUT8","IBC5","AUT9","AUT0"};
//		String[] txt=new String[]{"ICBC自動扣繳","它行自動扣繳","它行臨櫃繳款","郵局劃撥","自行臨櫃繳款-現金",
//				"自行臨櫃繳款-轉帳","自行臨櫃繳款-票據","金融卡ATM轉帳-自行現金","金融卡ATM轉帳-自行轉帳","金融卡ATM轉帳-它行轉帳",
//				"自行/它行電話銀行繳款","自行網路銀行繳款","IBM 繳款-其他","退貨或Reversal轉Payment","誤入帳補正","ATM繳款手續費轉Payment",
//				"Fancy卡退 手續費轉Payment","年費回饋","拉卡獎金轉Payment","D檔 退利息、違約金","Back Date 退利息、違約金","虛擬繳款",
//				"其他繳款","Fancy 卡基金","本行帳戶自動抵銷款","債務協商入帳-系統比例","債務協商入帳-欠款比例","統一超商臨櫃繳款","全家便利商店繳款",
//				"福客多便利商店繳款","萊爾富便利商店繳款","ACH他行自動扣款","自行臨櫃-其他票據","本行金融卡-它行轉帳-全國繳稅費平台","本行-E化繳費平台",
//				"債務協商入帳-本行最大債權","來來超商臨櫃繳款","前協-本行最大債權行","前協-他行最大債權行","個人信用貸款","更生-他行最大債權行","更生-本行最大債權行"};

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, "lgd_coll_flag");
			wp.colSet(ii, "tt_lgd_coll_flag", commString.decode(wkData, ",1,2,3", ",1.借款人自償,2.保證人他償,3.其他"));

			wkData = wp.colStr(ii, "tran_type");
//			wp.col_set(ii,"tt_tran_type", commString.decode(ss, cde, txt));
			wp.colSet(ii, "tt_tran_type", wfPtrPaymentBillDesc(wkData));
		}
	}

	String wfPtrPaymentBillDesc(String idcode) throws Exception {
		String rtn = "";
		String lsSql = "select payment_type||' ['||bill_desc||']' id_desc from ptr_payment "
				+ "where payment_type= :id_code ";
		setString("id_code", idcode);

		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			rtn = idcode;
		} else {
			rtn = sqlStr("id_desc");
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
		func = new Colp1220Func(wp);

		String[] lsRowid = wp.itemBuff("rowid");
		String[] lsModSeqno = wp.itemBuff("mod_seqno");
		String[] opt = wp.itemBuff("opt");

		// Detail Control
		int rowcntaa = 0;
		if (!(lsRowid == null) && !empty(lsRowid[0]))
			rowcntaa = lsRowid.length;
		wp.listCount[0] = rowcntaa;

		for (int ll = 0; ll < lsRowid.length; ll++) {
			if ((checkBoxOptOn(ll, opt) == false)) {
				continue;
			}

			func.varsSet("rowid", lsRowid[ll]);
			func.varsSet("mod_seqno", lsModSeqno[ll]);
			rc = func.dataProc();
			sqlCommit(rc);
			if (rc == 1) {
				wp.colSet(ll, "ok_flag", "V");
				ilOk++;
				continue;
			}
			ilErr++;
			wp.colSet(ll, "ok_flag", "X");
		}

//		//-insert-
//		int rr = -1;
//		for (int ii = 0; ii < opt.length; ii++) {
//			rr = (int) this.to_Num(opt[ii]) - 1;
//			if (rr<0) continue;
//		
//			func.vars_set("rowid", ls_rowid[rr]);
//			func.vars_set("mod_seqno", ls_mod_seqno[rr]);
//
//			rc =func.dataProc();
//			sql_commit(rc);
//			if (rc == 1) {
//				wp.col_set(rr,"ok_flag","V");
//				il_ok++;
//				continue;
//			}
//			il_err++;
//			wp.col_set(rr,"ok_flag","X");
//		
//		}
		// -re-Query-
		// queryRead();
		alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
