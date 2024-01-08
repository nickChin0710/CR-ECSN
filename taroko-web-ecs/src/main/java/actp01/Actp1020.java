/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-02-13  V1.00.01  ryan       program initial                            *
* 107-08-22  V1.00.02  Alex       remove zz_apr_user                         *
* 109-06-29  V1.00.03  Andy		  update Mantis 3676                         *
* 109-07-13  V1.00.04  Andy		  update Mantis 3676                         *
* 111-10-25  v1.00.05  Yang Bo    Sync code from mega                        *
******************************************************************************/
package actp01;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Actp1020 extends BaseProc {

	taroko.base.CommDate commDate = new taroko.base.CommDate();
	int rr = -1;
	String msg = "";
	String kk1 = "", kk2 = "";
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

	// for query use only
	private int getWhereStr() throws Exception {
		String lsDate1 = wp.itemStr2("ex_chg_cycle_date");
		String lsDate2 = wp.itemStr2("ex_chg_cycle_date2");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[不代收日期-起迄]  輸入錯誤");
			return -1;
		}
		wp.whereStr = " where 1=1 ";
		if (empty(wp.itemStr2("ex_chg_cycle_date")) == false) {
			wp.whereStr += " and chg_cycle_date >= :ex_chg_cycle_date ";
			setString("ex_chg_cycle_date", wp.itemStr2("ex_chg_cycle_date"));
		}
		if (empty(wp.itemStr2("ex_chg_cycle_date2")) == false) {
			wp.whereStr += " and chg_cycle_date <= :ex_chg_cycle_date2 ";
			setString("ex_chg_cycle_date2", wp.itemStr2("ex_chg_cycle_date2"));
		}
		if (empty(wp.itemStr2("ex_crt_id")) == false) {
			wp.whereStr += " and crt_user = :ex_crt_id ";
			setString("ex_crt_id", wp.itemStr2("ex_crt_id"));
		}
		if (wp.itemStr2("ex_optcode").equals("0")) {
			wp.whereStr += " and apr_flag <> 'Y' ";
		} else {
			wp.whereStr += " and apr_flag = 'Y' and batch_proc_mark <> 'Y' ";
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

		wp.selectSQL = "hex(rowid) as rowid, "
			//+ " UF_IDNO_ID(id_p_seqno) as acct_key, "
		  //+ " decode(card_indicator,'1',UF_IDNO_ID(id_p_seqno),UF_CORP_NO(corp_p_seqno)) as acct_key, "
			  + " UF_IDNO_ID(id_p_seqno) as acct_key1, "
			  + " UF_CORP_NO(corp_p_seqno) as acct_key2, "
				+ " corp_p_seqno, "
				+ " corp_no, "
				+ " corp_no_code, "
				// + " acct_holder_id, "
				// + " acct_holder_id_code, "
				+ " id_p_seqno, "
				+ " new_cycle_month,"
				+ " stmt_cycle,"
				+ " new_stmt_cycle,"
				+ " crt_date,"
				+ " crt_user,"
				+ " apr_flag,"
				+ " apr_date,"
				+ " apr_user, "
				+ " batch_proc_mark,"
				+ " batch_proc_date,"
				+ " mod_user,"
				+ " mod_time, "
				+ " mod_pgm, "
				+ " mod_seqno, "
				+ " 0 db_chg_times, "
				+ " m_code, "
				+ " lpad(' ',40) db_chi_name, "
				+ " lpad(' ',8) db_next_close_date, "
				+ " card_indicator, "
				+ " chg_cycle_date, "
				+ " last_cycle_month, "
				+ " last_interest_date ";
		wp.daoTable = " act_chg_cycle ";

		wp.whereOrder = "  ";
		if (getWhereStr() != 1)
			return;
		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		// wp.totalRows = wp.dataCnt;
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
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		SqlPrepare sp = new SqlPrepare();
		String[] aaRowid = wp.itemBuff("rowid");
		String[] opt = wp.itemBuff("opt");
		String[] aaModSeqno = wp.itemBuff("mod_seqno");
		String[] aaStmtCycle = wp.itemBuff("stmt_cycle");
		String[] aaNewStmtCycle = wp.itemBuff("new_stmt_cycle");
		String[] aaNewCycleMonth = wp.itemBuff("new_cycle_month");
		String newCycleMonth = "", lsNextCloseDate="";
		wp.listCount[0] = aaModSeqno.length;
		String sqlSelect = " select business_date from ptr_businday ";
		sqlSelect(sqlSelect);
		String lsBusinessDate = sqlStr("business_date");
		// -update-
		rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			rr = rr - (wp.pageRows * (wp.currPage - 1));
			if (rr < 0) {
				continue;
			}
			newCycleMonth = aaNewCycleMonth[rr];
			if (!aaStmtCycle[rr].equals(aaNewStmtCycle[rr])) {
				sqlSelect = "select this_acct_month,next_close_date from ptr_workday where stmt_cycle  = :aa_stmt_cycle ";
				setString("aa_stmt_cycle", aaStmtCycle[rr]);
				sqlSelect(sqlSelect);
				if (sqlRowNum > 0) {
					lsNextCloseDate = sqlStr("next_close_date");
					newCycleMonth = sqlStr("this_acct_month");
					Date date = format.parse(newCycleMonth);
					cal.setTime(date);
					if (aaStmtCycle[rr].compareTo(aaNewStmtCycle[rr]) > 0) {
						cal.add(Calendar.MARCH, +2);
					}
					if (aaStmtCycle[rr].compareTo(aaNewStmtCycle[rr]) < 0) {
						cal.add(Calendar.MARCH, +1);
					}
        //cycle_date 當天覆核時，生效日會再延後一個月
					if (lsBusinessDate.equals(lsNextCloseDate) ) {
						cal.add(Calendar.MARCH, +1);
					}

					newCycleMonth = format.format(cal.getTime());
				}
			}

			sp.sql2Update("act_chg_cycle");
			if (wp.itemStr2("ex_optcode").equals("0")) {
				sp.ppstr("apr_flag", "Y");
				sp.ppstr("apr_user", wp.loginUser);
				sp.ppstr("apr_date", lsBusinessDate);
			//sp.ppss("new_cycle_month", new_cycle_month);
			} else {
				sp.ppstr("apr_flag", "");
				sp.ppstr("apr_user", "");
				sp.ppstr("apr_date", "");
			}
			sp.ppstr("mod_user", wp.loginUser);
			sp.ppstr("mod_pgm", wp.modPgm());
			sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", ", mod_time = sysdate");
			sp.sql2Where(" where hex(rowid)=?", aaRowid[rr]);
			sp.sql2Where(" and mod_seqno=?", aaModSeqno[rr]);
			sqlExec(sp.sqlStmt(), sp.sqlParm());
			if (sqlRowNum <= 0) {
				wp.colSet(rr, "ok_flag", "!");
				alertErr("Update act_chg_cycle error");
				sqlCommit(0);
				ilErr++;
				return;
			}
		}
		sqlCommit(1);
		queryFunc();
		errmsg("處理成功 ");
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	void listWkdata() throws Exception {
		String lsIdPSeqno = "", lsCorpPSeqno = "", lsNewStmtCycle = "", lsCycle = "";
		String lsBusinessDateSubstring = "", lsNextCloseNSubstring46 = "", lsNextCloseNSubstring04 = "", lsNextCloseNSubstring68 = "";
		String liCnt = "", lsChiName = "";
		String ldtVal1 = "", ldtVal2 = "";
    int liMonthDiff = 0;

		String sqlSelect = " select business_date from ptr_businday ";
		sqlSelect(sqlSelect);
		String lsBusinessDate = sqlStr("business_date");

		for (int i = 0; i < wp.selectCnt; i++) {
			// --半年內更改數--
			lsIdPSeqno = wp.colStr(i, "id_p_seqno");
			lsCorpPSeqno = wp.colStr(i, "corp_p_seqno");
			lsNewStmtCycle = wp.colStr(i, "new_stmt_cycle");

			lsCycle = wp.colStr(i, "stmt_cycle");
			// 下次關帳日20200709 update
			String lsNextCloseN = "";
			lsNewStmtCycle = wp.colStr(i, "new_stmt_cycle");
			lsCycle = wp.colStr(i, "stmt_cycle");
			String sqlSelect2 = " select this_acct_month,next_acct_month "
					+ " from ptr_workday where stmt_cycle = :ls_cycle ";
			setString("ls_cycle", lsCycle);
			sqlSelect(sqlSelect2);
			String lsThisAcctMonth = sqlStr("this_acct_month");
			String lsNextAcctMonth = sqlStr("next_acct_month");
			if (lsBusinessDate.length() >= 8) {
				lsBusinessDateSubstring = strMid(lsBusinessDate, 6, 2);
			}

			// ls_next_close_n_substring68 = ss_mid(ls_next_close_n,6,2);
			// 判斷原cycl是否等於business_date的日期
			if (this.toInt(lsBusinessDateSubstring) != this.toInt(lsCycle)) {
				lsNextCloseNSubstring04 = strMid(lsThisAcctMonth, 0, 4);
				lsNextCloseNSubstring46 = strMid(lsThisAcctMonth, 4, 2);
				// 大變小
				if(this.toInt(lsCycle) > this.toInt(lsNewStmtCycle)){	
					if(lsNextCloseNSubstring46.equals("11")){
						lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)+1)+"01"+lsNewStmtCycle;
					}
					else if(lsNextCloseNSubstring46.equals("12")) {
						      lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)+1)+"02"+lsNewStmtCycle;
					     } else {
						      lsNextCloseN = lsNextCloseNSubstring04+(String.format("%02d",this.toInt(lsNextCloseNSubstring46)+2))+lsNewStmtCycle;
					     }
				}
				// 小變大
				if (this.toInt(lsCycle) < this.toInt(lsNewStmtCycle)) {
					if (lsNextCloseNSubstring46.equals("12")) {
						lsNextCloseN = (this.toInt(lsNextCloseNSubstring04) + 1) + "01" + lsNewStmtCycle;
					} else {
						lsNextCloseN = lsNextCloseNSubstring04 + (String.format("%02d", this.toInt(lsNextCloseNSubstring46) + 1)) + lsNewStmtCycle;
					}
				}
			} else { // businessdate 等於 原cycle
				lsNextCloseNSubstring04 = strMid(lsNextAcctMonth, 0, 4);
				lsNextCloseNSubstring46 = strMid(lsNextAcctMonth, 4, 2);
				// 大變小
				if(this.toInt(lsCycle) > this.toInt(lsNewStmtCycle)){
					if(lsNextCloseNSubstring46.equals("11")){
						lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)+1)+"01"+lsNewStmtCycle;
					}
					else if(lsNextCloseNSubstring46.equals("12")){
						      lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)+1)+"02"+lsNewStmtCycle;
					     } else {
						      lsNextCloseN = lsNextCloseNSubstring04+(String.format("%02d",this.toInt(lsNextCloseNSubstring46)+2))+lsNewStmtCycle;
					     }
        //以下控制為作業日已更改但營業日尚未更改時
		      ldtVal1 = strMid(lsNextCloseN,0,6)+"01";
			    ldtVal2 = wp.colStr(i,"last_cycle_month")+"01";
          liMonthDiff = commDate.monthsBetween(ldtVal1,ldtVal2);
					if(liMonthDiff >= 4) {
		    		lsNextCloseNSubstring04 = strMid(lsNextCloseN,0,4);
				    lsNextCloseNSubstring46 = strMid(lsNextCloseN,4,2);
					  if(lsNextCloseNSubstring46.equals("01")){
						  lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)-1)+"12"+lsNewStmtCycle;
					  } else {
						  lsNextCloseN = lsNextCloseNSubstring04+(String.format("%02d",this.toInt(lsNextCloseNSubstring46)-1))+lsNewStmtCycle;
				  	}
					}
        //以上控制為作業日已更改但營業日尚未更改時
				}
				// 小變大
				if (this.toInt(lsCycle) < this.toInt(lsNewStmtCycle)) {
					if (lsNextCloseNSubstring46.equals("12")) {
						lsNextCloseN = (this.toInt(lsNextCloseNSubstring04) + 1) + "01" + lsNewStmtCycle;
					} else {
						lsNextCloseN = lsNextCloseNSubstring04 + (String.format("%02d", this.toInt(lsNextCloseNSubstring46) + 1)) + lsNewStmtCycle;
					}
        //以下控制為作業日已更改但營業日尚未更改時
		      ldtVal1 = strMid(lsNextCloseN,0,6)+"01";
			    ldtVal2 = wp.colStr(i,"last_cycle_month")+"01";
          liMonthDiff = commDate.monthsBetween(ldtVal1,ldtVal2);
					if(liMonthDiff >= 3) {
		    		lsNextCloseNSubstring04 = strMid(lsNextCloseN,0,4);
				    lsNextCloseNSubstring46 = strMid(lsNextCloseN,4,2);
					  if(lsNextCloseNSubstring46.equals("01")){
						  lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)-1)+"12"+lsNewStmtCycle;
					  } else {
						  lsNextCloseN = lsNextCloseNSubstring04+(String.format("%02d",this.toInt(lsNextCloseNSubstring46)-1))+lsNewStmtCycle;
				  	}
					}
        //以上控制為作業日已更改但營業日尚未更改時
				}
			}
			// String sql_select2=" select next_close_date "
			// + "
			// ,to_char(add_months(to_date(next_close_date,'yyyymmdd'),1),'yyyymmdd')
			// as ls_next_close_n_add "
			// + " from ptr_workday where stmt_cycle = :ls_new_stmt_cycle ";
			// setString("ls_new_stmt_cycle",ls_new_stmt_cycle);
			// sqlSelect(sql_select2);
			// String ls_next_close_n = sql_ss("next_close_date");
			// String ls_next_close_n_add = sql_ss("ls_next_close_n_add");
			//
			// if(ls_business_date.length()>=8){
			// ls_business_date_substring = ss_mid(ls_business_date,6,2);
			// }
			// if(ls_next_close_n.length()>=8){
			// ls_next_close_n_substring46 = ss_mid(ls_next_close_n,4,2);
			// ls_next_close_n_substring04 = ss_mid(ls_next_close_n,0,4);
			// ls_next_close_n_substring68 = ss_mid(ls_next_close_n,6,2);
			// }
			// if(this.to_Int(ls_cycle)>this.to_Int(ls_new_stmt_cycle)){
			// if(this.to_Int(ls_business_date_substring)>=this.to_Int(ls_cycle)){
			// if(ls_next_close_n_substring46.equals("12")){
			// ls_next_close_n =
			// (this.to_Int(ls_next_close_n_substring04)+1)+"01"+ls_next_close_n_substring68;
			// }else{
			// ls_next_close_n = ls_next_close_n_substring04
			// +(String.format("%02d",this.to_Int(ls_next_close_n_substring46)+1))
			// +ls_next_close_n_substring68;
			// }
			// }
			// }else{
			// if(this.to_Int(ls_business_date_substring)>=this.to_Int(ls_cycle)
			// &&this.to_Int(ls_business_date_substring)<this.to_Int(ls_new_stmt_cycle)){
			// ls_next_close_n = ls_next_close_n_add;
			// }
			// }
			// 一般卡
			if (wp.colStr(i, "card_indicator").equals("1")) {
			  wp.colSet(i, "acct_key", wp.colStr(i, "acct_key1"));
				String sqlSelect3 = "select count(*) as li_cnt from act_chg_cycle "
						+ " where card_indicator = '1' "
						+ " and id_p_seqno = :ls_id_p_seqno "
						+ " and months_between(to_date(:ls_business_date,'yyyymmdd'),to_date(chg_cycle_date,'yyyymmdd')) <= 6 ";
				setString("ls_id_p_seqno", lsIdPSeqno);
				setString("ls_business_date", lsBusinessDate);
				sqlSelect(sqlSelect3);
				liCnt = sqlStr("li_cnt");

				String sqlSelect4 = "select chi_name from crd_idno where id_p_seqno = :ls_id_p_seqno ";
				setString("ls_id_p_seqno", lsIdPSeqno);
				sqlSelect(sqlSelect4);
				lsChiName = sqlStr("chi_name");
			} else {
			  wp.colSet(i, "acct_key", wp.colStr(i, "acct_key2"));
				String sqlSelect3 = "select count(*) as li_cnt from act_chg_cycle "
						+ " where card_indicator = '2' "
						+ " and corp_p_seqno = :ls_corp_p_seqno "
						+ " and months_between(to_date(:ls_business_date,'yyyymmdd'),to_date(chg_cycle_date,'yyyymmdd')) <= 6 ";
				setString("ls_corp_p_seqno", lsCorpPSeqno);
				setString("ls_business_date", lsBusinessDate);
				sqlSelect(sqlSelect3);
				liCnt = sqlStr("li_cnt");

				String sqlSelect4 = "select chi_name from crd_corp where corp_p_seqno = :ls_corp_p_seqno ";
				setString("ls_corp_p_seqno", lsCorpPSeqno);
				sqlSelect(sqlSelect4);
				lsChiName = sqlStr("chi_name");
			}
			wp.colSet(i, "db_chg_times", liCnt);
			wp.colSet(i, "db_chi_name", lsChiName);
			wp.colSet(i, "db_next_close_date", lsNextCloseN);
		}
	}
}
