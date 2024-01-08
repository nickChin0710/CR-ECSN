/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-03-25  V1.00.02  Yanghan       Modify some functions                   *
* 109-05-06  V1.00.03  Zhanghuheng   updated for project coding standard     *
* 109-01-04  V1.00.03  shiyuqi       修改无意义命名                                                            *
* 111-12-08  V1.00.04  sunny         增加不能覆核自己及不同部門的案件                                 *  
*/                                                                           
package colp01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Colp0080 extends BaseProc {	
	CommString commString = new CommString();
	Colp0080Func func;

//	String kk1 = "";
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

		// dddw_select();
		initButton();
	}

	private boolean getWhereStr() throws Exception {
		String lsDate1 = wp.itemStr("exDateS");
		String lsDate2 = wp.itemStr("exDateE");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[建檔日期-起迄]  輸入錯誤");
			return false;
		}

		wp.whereStr = "where 1=1 ";
		if (empty(wp.itemStr("exDateS")) == false) {
			wp.whereStr += " and col_wait_trans.crt_date >= :crt_dates ";
			setString("crt_dates", wp.itemStr("exDateS"));
		}
		if (empty(wp.itemStr("exDateE")) == false) {
			wp.whereStr += " and col_wait_trans.crt_date <= :crt_datee ";
			setString("crt_datee", wp.itemStr("exDateE"));
		}
		if (empty(wp.itemStr("exUser")) == false) {
			wp.whereStr += " and col_wait_trans.crt_user = :crt_user ";
			setString("crt_user", wp.itemStr("exUser"));
		}
		if (wp.itemStr("exAprState").equals("Y")) {
			wp.whereStr += " and col_wait_trans.apr_flag ='Y' ";
		} else if (wp.itemStr("exAprState").equals("N")) {
			wp.whereStr += " and col_wait_trans.apr_flag <>'Y' ";
		}
		if (!wp.itemStr("exType").equals("0")) {
			wp.whereStr += " and col_wait_trans.trans_type = :trans_type ";
			setString("trans_type", wp.itemStr("exType"));
		}
		// 由於商務卡只顯示一條的整合信息，其中欲轉催呆金額為總金額，其他的以acno_flag=2為主，所以第一次查詢只查詢acno_flag=2的商務卡信息，
		// 在數據處理的時候在進行欲轉催呆金額的加總,一般户的acno_flag為一，商務戶只需查詢為2時的值
		
//		wp.whereStr += " and ((col_wait_trans.acno_flag='2' and col_wait_trans.corp_p_seqno!='') or"
//				+ " (col_wait_trans.corp_p_seqno = '' and col_wait_trans.acno_flag ='1'))";
		
		wp.whereStr += " and ((act_acno.acno_flag='2' and act_acno.corp_p_seqno!='') or"
		+ " (act_acno.corp_p_seqno = '' and act_acno.acno_flag ='1'))";
		wp.whereOrder = " order by acct_type, acct_key";
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

		wp.selectSQL = "col_wait_trans.p_seqno, " + "col_wait_trans.acct_type, " + "act_acno.acct_key, "
				+ "col_wait_trans.src_acct_stat, " + "col_wait_trans.trans_type, " + "col_wait_trans.alw_bad_date, "
				+ "col_wait_trans.paper_conf_date, " + "col_wait_trans.valid_cancel_date, "
				+ "col_wait_trans.paper_name, " + "col_wait_trans.crt_date, " + "col_wait_trans.crt_time, "
				+ "col_wait_trans.crt_user, " + "col_wait_trans.apr_flag, " + "col_wait_trans.apr_date, "
				+ "col_wait_trans.apr_time, " + "col_wait_trans.apr_user, " + "col_wait_trans.sys_trans_flag, "
				+ "col_wait_trans.mod_user, " + "col_wait_trans.mod_time, " + "col_wait_trans.mod_pgm, "
				+ "col_wait_trans.mod_seqno, " + "col_wait_trans.chi_name, " 
//				+ "col_wait_trans.corp_act_type, "// 商務卡類旗標
//				+ "col_wait_trans.corp_p_seqno, "// CORP_P_SEQNO
//				+ "col_wait_trans.acno_flag,"// 商務卡總個繳細項
//				+ "act_acno.corp_act_type, "// act_acno.商務卡類旗標
				+ "act_acno.corp_p_seqno, "// act_acno.CORP_P_SEQNO
				+ "act_acno.acno_flag,"// act_acno.商務卡總個繳細項
				+ "col_wait_trans.bad_debt_amt ";

		wp.daoTable = "col_wait_trans "
//				+ "left join act_acno on col_wait_trans.p_seqno = act_acno.p_seqno ";
				+ "left join act_acno on col_wait_trans.p_seqno = act_acno.acno_p_seqno ";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		// 欲轉催呆金額加總
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			String acnoFlag = wp.colStr(ii, "acno_flag");
			if (eqIgno(acnoFlag, "2")) {
				String corpSeqNo = wp.colStr(ii, "corp_p_seqno");
//				System.out.println("zhanghu leibei "+acnoFlag+"seqno"+corpSeqNo);
				// bad_debt_amt 该值为varchar型
				String lsSql = "select nvl(sum(cast(bad_debt_amt as bigint)),0) as sum_bal from col_wait_trans "
						+ "where corp_p_seqno=? ";
				Object[] param = new Object[] { corpSeqNo };
				sqlSelect(lsSql, param);
				String sumBadDebtAmt = sqlStr("sum_bal");
				wp.colSet(ii, "bad_debt_amt", sumBadDebtAmt);
			}
		}
		listWkdata();
		querySummary();
		wp.setPageValue();
		apprDisabled("mod_user");
	}

	void listWkdata() {
		String wkData = "";
		String[] cde = new String[] { "1", "2", "3", "4", "5" };
		String[] txt = new String[] { "1.正常", "2.逾放", "3.催收", "4.呆帳", "5.結清" };

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, "src_acct_stat");
			wp.colSet(ii, "tt_src_acct_stat", commString.decode(wkData, cde, txt));

			wkData = wp.colStr(ii, "trans_type");
			wp.colSet(ii, "tt_trans_type", commString.decode(wkData, cde, txt));

			wkData = wp.colStr(ii, "acct_status");
			wp.colSet(ii, "tt_acct_status", commString.decode(wkData, cde, txt));

			wkData = wp.colStr(ii, "apr_user");
			wp.colSet(ii, "isDisable", isEmpty(wkData) ? "" : "disabled");
		}
	}

	void querySummary() {
		int iiamt = 0;
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			iiamt += toNum(wp.colStr(ii, "bad_debt_amt"));
		}
		wp.colSet("exCnt", intToStr(wp.selectCnt));
		wp.colSet("exAmt", intToStr(iiamt));
	}

	@Override
	public void querySelect() throws Exception {

	}

	@Override
	public void dataRead() throws Exception {

	}

	@Override
	public void dataProcess() throws Exception {
		func = new Colp0080Func(wp);
		// System.out.println("kaishizhixing");
		String[] lsPSeqno = wp.itemBuff("p_seqno");
		String[] lsAprFlag = wp.itemBuff("apr_flag");
		String[] lsModSeqno = wp.itemBuff("mod_seqno");
		String[] lsTransType = wp.itemBuff("trans_type");
		String[] lsCorpPSeqno = wp.itemBuff("corp_p_seqno");
		String[] lsAcnoFlag = wp.itemBuff("acno_flag");
		String[] opt = wp.itemBuff("opt");
		// System.out.println("ls_trans_type"+ls_trans_type[0]);
		// Detail Control
		int rowcntaa = 0;
		if (!(lsPSeqno == null) && !empty(lsPSeqno[0]))
			rowcntaa = lsPSeqno.length;
		wp.listCount[0] = rowcntaa;

		// -insert-
		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			// 2018.5.8 有換頁的opt處理, 須扣掉(行數 x 頁數)//
			rr = rr - (wp.pageRows * (wp.currPage - 1));
			// 2018.5.8 end//
			if (rr < 0) {
				continue;
			}
			if ("Y".equals(lsAprFlag[rr])) { // 已覆核跳過
				continue;
			}
			func.varsSet("p_seqno", lsPSeqno[rr]);
			func.varsSet("mod_seqno", lsModSeqno[rr]);
			func.varsSet("trans_type", lsTransType[rr]); // 2018.12.25 mod
			func.varsSet("corp_p_seqno", lsCorpPSeqno[rr]);
			func.varsSet("acno_flag", lsAcnoFlag[rr]);
			rc = func.dataProc();
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

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

}
