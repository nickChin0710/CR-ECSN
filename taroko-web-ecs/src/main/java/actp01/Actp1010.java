/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-03  V1.00.00  Andy       program initial                            *
* 107-05-16  V1.00.01  Andy       Update                                     *
* 111-10-25  v1.00.16  Yang Bo    Sync code from mega                        *
* 112-10-28  V1.00.17  Simon		  remove act_acag.seqno in tcb version       *
* 112-12-28  V1.00.18  Simon		  1.sqlCol(x,x) & setString(x,x) can't be    *
*                                   used together                            *
*                                 2.以 ptr_businday.business_date update     *
*                                   act_chg_cycle.chg_cycle_date, crt_date   *
******************************************************************************/

package actp01;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;

public class Actp1010 extends BaseEdit {
	CommDate commDate = new CommDate();
	String mAcctKey = "";
	String mIdCode = "";
	String mCardIndicator = "";
	String mAcnoPSeqno = "";
	String mIdPSeqno = "";
	String mCorpPSeqno = "";
	String mCorpIdSeqno = "";
	String mChgCycleDate = "";
	String hOldStmtCycle = "";
	String hBatchProcMark = "";
	String hNextCloseDate = "";
	String aaRowid = "";
	String aaModSeqno = "";
	String aaNewStmtCycle = "";
	String lsSql = "", usSql = "", isSql = "", lsBusinessDate = "";
	String[] aaAcctType;
	String[] aaStmtCycle;
	String[] aaDbThisAcctMonth;
	String[] aaDbThisInterestDate;
	String[] aaDbMCode;
	String[] aaAcnoPSeqno;
	String aaAcctType1 = "", aaStmtCycle1 = "", aaDbThisAcctMonth1 = "", aaDbThisInterestDate1 = "",
			aaDbMCode1 = "", aaPSeqno1 = "", aaNewCycleMonth1 = "";

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
			strAction = "A";
			saveFunc();
			// insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			strAction = "U";
			saveFunc();
			// updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			strAction = "D";
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
		} else if (eqIgno(wp.buttonCode, "itemchanged")) {
			/* 執行 */
			strAction = "itemchanged";
			itemchanged();
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
		wp.colSet("ex_idno_code", "0");
		wp.colSet("kk_idno_code", "0");
	}

	// for query use only
	private boolean getWhereStr() throws Exception {
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
		String exCardIndicator = wp.itemStr("ex_card_indicator");
		String exKey = wp.itemStr("ex_key");
		String exIdnoCode = wp.itemStr("ex_idno_code");

		if(exCardIndicator.equals("1")){
			if(empty(exIdnoCode)){
				alertErr("個別卡身份證檢碼不可空白");
				return;
			}
		}
		
		wp.sqlCmd = "SELECT rowid,"
				+ "mod_seqno, "
				+ "card_indicator,"
				+ "db_id_no, "
				+ "db_id_no_code, "
				+ "corp_no, "
				+ "chg_cycle_date, "
				+ "last_cycle_month, "
				+ "new_cycle_month, "
				+ "new_stmt_cycle, "
				+ "last_interest_date, "
				+ "crt_date, "
				+ "crt_user, "
				+ "apr_flag, "
				+ "apr_date, "
				+ "apr_user, "
				+ "batch_proc_date, "
				+ "stmt_cycle, "
				+ "id_p_seqno, "
				+ "corp_p_seqno, "
				+ "corp_id_seqno "
				+ "FROM (SELECT hex (rowid) AS rowid, "
				+ "mod_seqno, "
				+ "card_indicator, "
				+ "decode (card_indicator,'1',uf_idno_id (id_p_seqno),corp_no) AS db_id_no, "
				+ "decode(card_indicator,'1',(select id_no_code from crd_idno where id_p_seqno = act_chg_cycle.id_p_seqno),'') AS db_id_no_code, "
				+ "corp_no, "
				+ "chg_cycle_date, "
				+ "last_cycle_month, "
				+ "new_cycle_month, "
				+ "new_stmt_cycle, "
				+ "last_interest_date, "
				+ "crt_date, "
				+ "crt_user, "
				+ "apr_flag, "
				+ "apr_date, "
				+ "apr_user, "
				+ "batch_proc_date, "
				+ "stmt_cycle, "
				+ "id_p_seqno, "
				+ "corp_p_seqno, "
				+ "corp_id_seqno "
				+ "FROM act_chg_cycle "
				+ "WHERE 1 = 1 "
	//wp.sqlCmd += sqlCol(exCardIndicator, "card_indicator");
				+ "and card_indicator =:ex_card_indicator ";
			setString("ex_card_indicator", exCardIndicator);
		if (exCardIndicator.equals("1")) {
			wp.sqlCmd += " and id_p_seqno = "
					+ "(select id_p_seqno "
					+ "from crd_idno "
					+ "where 1 = 1 "
					+ "and id_no =:ex_key "
					+ "and id_no_code =:ex_idno_code) ";
			setString("ex_key", exKey);
			setString("ex_idno_code", exIdnoCode);
		} else {
			wp.sqlCmd += " and corp_p_seqno = "
					+ "(select corp_p_seqno "
					+ "from crd_corp "
					+ "where 1=1 "
					+ "and corp_no =:ex_key) ";
			setString("ex_key", exKey);
		}
		wp.sqlCmd += ") order by card_indicator, chg_cycle_date ";
		wp.pageCountSql = "select count(*) from (";
		wp.pageCountSql += wp.sqlCmd;
		wp.pageCountSql += ")";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		listWkdata();
	}

	void listWkdata() throws Exception {
		int rowCt = 0;
		String lsChiName = "";
		String lsBusinessDateSubstring="",lsNextCloseNSubstring46="",lsNextCloseNSubstring04=""
				,lsNextCloseNSubstring68="",lsNewStmtCycle="",lsCycle="";
		String ldtVal1 = "", ldtVal2 = "";
    int liMonthDiff = 0;
		lsSql = "select business_date "
				+ "from	ptr_businday";
		sqlSelect(lsSql);
		lsBusinessDate = sqlStr("business_date");
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// 計算欄位
			rowCt += 1;
			// card_indicator
			String[] cde1 = new String[] { "1", "2" };
			String[] txt1 = new String[] { "個別卡", "商務卡" };
			wp.colSet(ii, "db_card_indicator", commString.decode(wp.colStr(ii, "card_indicator"), cde1, txt1));
			wp.colSet(ii,"crt_user",sellectUsrcname(wp.colStr(ii,"crt_user")));
			wp.colSet(ii,"apr_user",sellectUsrcname(wp.colStr(ii,"apr_user")));
			if(wp.colStr(ii, "card_indicator").equals("1")){
				String sqlSelect4="select chi_name from crd_idno where id_p_seqno = :ls_id_p_seqno ";
				setString("ls_id_p_seqno",wp.colStr(ii,"id_p_seqno"));
				sqlSelect(sqlSelect4);
				lsChiName = sqlStr("chi_name");
			}
			if(wp.colStr(ii, "card_indicator").equals("2")){
				String sqlSelect4="select chi_name from crd_corp where corp_p_seqno = :ls_corp_p_seqno ";
				setString("ls_corp_p_seqno",wp.colStr(ii,"corp_p_seqno"));
				sqlSelect(sqlSelect4);
				lsChiName = sqlStr("chi_name");
			}
			wp.colSet(ii,"db_chi_name",lsChiName);
			//下次關帳日20200709 update
			String lsNextCloseN ="";
			lsNewStmtCycle = wp.colStr(ii,"new_stmt_cycle");
			lsCycle = wp.colStr(ii,"stmt_cycle");
			String sqlSelect2=" select this_acct_month,next_acct_month "
					+ " from ptr_workday where stmt_cycle = :ls_cycle ";
			setString("ls_cycle",lsCycle);
			sqlSelect(sqlSelect2);
			String lsThisAcctMonth = sqlStr("this_acct_month");
			String lsNextAcctMonth = sqlStr("next_acct_month");
			if(lsBusinessDate.length()>=8){
				lsBusinessDateSubstring = strMid(lsBusinessDate,6,2);
			}
							
			String sqlSelect3=" select next_close_date "
					+ " from ptr_workday where stmt_cycle = :ls_cycle ";
			setString("ls_cycle",lsNewStmtCycle);
			sqlSelect(sqlSelect3);
			String lsNextCloseDateNew = sqlStr("next_close_date");
							
			//ls_next_close_n_substring68 = ss_mid(ls_next_close_n,6,2);
			//判斷原cycl是否等於business_date的日期
			if(this.toInt(lsBusinessDateSubstring) != this.toInt(lsCycle)){
				lsNextCloseNSubstring04 = strMid(lsThisAcctMonth,0,4);
				lsNextCloseNSubstring46 = strMid(lsThisAcctMonth,4,2);				
				//大變小
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
				//小變大
				if(this.toInt(lsCycle) < this.toInt(lsNewStmtCycle)){
					if(lsNextCloseNSubstring46.equals("12")){
						lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)+1)+"01"+lsNewStmtCycle;
					} else {
						lsNextCloseN = lsNextCloseNSubstring04+(String.format("%02d",this.toInt(lsNextCloseNSubstring46)+1))+lsNewStmtCycle;
					}
				}
			} else {	//businessdate 等於 原cycle
				lsNextCloseNSubstring04 = strMid(lsNextAcctMonth,0,4);
				lsNextCloseNSubstring46 = strMid(lsNextAcctMonth,4,2);
				//大變小
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
			    ldtVal2 = wp.colStr(ii,"last_cycle_month")+"01";
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
				//小變大
				if(this.toInt(lsCycle) < this.toInt(lsNewStmtCycle)){
					if(lsNextCloseNSubstring46.equals("12")){
						lsNextCloseN = (this.toInt(lsNextCloseNSubstring04)+1)+"01"+lsNewStmtCycle;
					} else {
						lsNextCloseN = lsNextCloseNSubstring04+(String.format("%02d",this.toInt(lsNextCloseNSubstring46)+1))+lsNewStmtCycle;
					}
        //以下控制為作業日已更改但營業日尚未更改時
		      ldtVal1 = strMid(lsNextCloseN,0,6)+"01";
			    ldtVal2 = wp.colStr(ii,"last_cycle_month")+"01";
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
			wp.colSet(ii,"db_next_close_date",  lsNextCloseN);
			
			if (wp.colStr(ii,"new_cycle_month").equals("")) {
			  //wp.col_set(ii,"new_cycle_month",  ss_mid(ls_next_close_n,0,6));
			} else 
			    if (!wp.colStr(ii, "new_cycle_month").equals(strMid(lsNextCloseN,0,6))) { 
			       wp.colSet(ii,"db_next_close_date",  lsNextCloseDateNew);
			    }
			
		}
		wp.colSet("row_ct", intToStr(rowCt));

		
	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		mCardIndicator = itemKk("data_k1");
		mCorpIdSeqno = itemKk("data_k2");
		mChgCycleDate = itemKk("data_k3");
		hNextCloseDate = itemKk("data_k4");
		if (mCardIndicator.equals("1")) {
			mIdPSeqno = mCorpIdSeqno;
		} else {
			mCorpPSeqno = mCorpIdSeqno;
		}
		// 主table act_chg_cycle
		wp.sqlCmd = "select hex(rowid) as rowid, mod_seqno, "
				+ "card_indicator, "
				+ "corp_id_seqno, "
				+ "chg_cycle_date, "
				+ "last_cycle_month, "
				+ "new_cycle_month, "
				+ "new_stmt_cycle, "
				+ "last_interest_date, "
				+ "crt_date, "
				+ "crt_user, "
				+ "apr_flag, "
				+ "apr_date, "
				+ "apr_user, "
				+ "batch_proc_mark, "
				+ "batch_proc_date, "
				+ "stmt_cycle, "
				+ "decode(card_indicator,'1',uf_idno_id(id_p_seqno),corp_no) as db_id_no, "
				+ "decode(card_indicator,'1',(select id_no_code from crd_idno where id_p_seqno = act_chg_cycle.id_p_seqno),'') AS db_id_no_code, "
				+ "decode(card_indicator,'1',(select id_no_code from crd_idno where id_p_seqno = act_chg_cycle.id_p_seqno),'') AS kk_idno_code, "
				+ "decode (card_indicator,'1',uf_idno_name (id_p_seqno),uf_corp_name (corp_p_seqno)) AS db_chi_name, "
				+ "card_indicator, "
				+ "corp_no,"
				+ "id_p_seqno, "
				+ "corp_p_seqno  ";
		wp.sqlCmd += "from act_chg_cycle ";
		wp.sqlCmd += "where 1=1 ";
		wp.sqlCmd += sqlCol(mCardIndicator, "card_indicator");
		wp.sqlCmd += sqlCol(mCorpIdSeqno, "corp_id_seqno");
		wp.sqlCmd += sqlCol(mChgCycleDate, "chg_cycle_date");

		// System.out.println(wp.sqlCmd);
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料!!");
		}
		//
		String wAprFlag = wp.colStr("apr_flag");		
		if(wAprFlag.equals("Y")){
			//wp.col_set("d_disable", "disabled style='background-color: lightgray;'");
			wp.colSet("btd", "disabled style='background-color: lightgray;'");
			wp.colSet("btu", "disabled style='background-color: lightgray;'");
			alertMsg("已覆核資料不可異動或刪除!");
		}
		hOldStmtCycle = wp.colStr("stmt_cycle");		
		hBatchProcMark = wp.colStr("batch_proc_mark");		
		
		// card_indicator
		String[] cde1 = new String[] { "1", "2" };
		String[] txt1 = new String[] { "個別卡", "商務卡" };
		wp.colSet("db_card_indicator", commString.decode(wp.colStr("card_indicator"), cde1, txt1));
		wp.colSet("crt_user",sellectUsrcname(wp.colStr("crt_user")));
		wp.colSet("apr_user",sellectUsrcname(wp.colStr("apr_user")));

		// 讀取act_acno資料
		if (selectActAcno() != 0)
			return;

	}

	public int selectActAcno() throws Exception {
		// wp.dddSql_log = false;
		wp.pageRows = 999;
		if (empty(mCorpIdSeqno)) {
			mCorpIdSeqno = wp.colStr("corp_id_seqno");
		}
		if (empty(mCorpIdSeqno)) {
			alertErr("Key Error");
			return -1;
		}

		wp.sqlCmd = "select acct_type, "
				+ "stmt_cycle, "
				+ "lpad(' ',8) db_this_acct_month, "
				+ "acct_status, "
				+ "0 db_acct_jrnl_bal, "
				+ "0 db_min_pay_bal, "
				+ "acno_p_seqno, "
				+ "corp_p_seqno, "
				+ "0 db_m_code, "
				+ "card_indicator as act_card_indicator, "
				+ "lpad(' ',8) db_this_interest_date, "
				+ "lpad(' ',8) db_next_close_date "
				+ "from act_acno  ";
		wp.sqlCmd += "where 1=1 ";
		if (mCardIndicator.equals("1")) {
			wp.sqlCmd += sqlCol(mCorpIdSeqno, "id_p_seqno");
			wp.sqlCmd += " and acct_type in (select acct_type from ptr_acct_type where card_indicator = '1')";
		} else {
			wp.sqlCmd += sqlCol(mCorpIdSeqno, "corp_p_seqno");
			wp.sqlCmd += " and acct_type in (select acct_type from ptr_acct_type where card_indicator = '2')";
		}
		wp.colSet("sql_act_acno", wp.sqlCmd);
		// System.out.println("wp.sqlCmd act_acno : "+wp.sqlCmd);
		pageQuery();
		wp.setListCount(1);
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		if (sqlNotFind()) {
			alertErr("act_acno明細資料不存在");
			//System.out.println(appMsg.err_condNodata);
			return -1;
		}
		wp.colSet("row_ct", sqlRowNum);

		// 附加資料欄位設定
		wfSetData();
		return 0;
	}

	void wfSetData() throws Exception {
		String lsNewCycleDate = "", lsLastCycleDate = "", lsLastInterestDate = "", lsNextCloseDate = "";
		String lsStmtCycle = "", lsLastCycleMonth = "", lsBusinessDateSubstring78 = "";
		int liMCode = 0;
		String wkCardIndicator = "";
		// act_acno detail data set
		int selCt = wp.selectCnt;
		// System.out.println("sel_ct : " + sel_ct);
		lsSql = "select business_date "
				+ "from	ptr_businday";
		sqlSelect(lsSql);
		lsBusinessDate = sqlStr("business_date");
		lsBusinessDateSubstring78 = strMid(lsBusinessDate,6,2);
		wkCardIndicator = wp.itemStr("kk_card_indicator");
		if (empty(wkCardIndicator)) {
			wkCardIndicator = itemKk("data_k1");
		}
		for (int ii = 0; ii < selCt; ii++) {

			if (hBatchProcMark.equals("Y")) {
						wp.colSet(ii,"stmt_cycle", hOldStmtCycle);
      }
			lsStmtCycle = wp.colStr(ii,"stmt_cycle");
			// wk_card_indicator = wp.col_ss(ii, "card_indicator");
			wp.colSet(ii, "no", intToStr(ii + 1));
			//check mcode			
			liMCode = fChkMcode(wp.colStr(ii, "acno_p_seqno")); // 取m_code
			lsNewCycleDate = "";
			// System.out.println("m_corp_id_seqno :" + m_corp_id_seqno);
			if (wkCardIndicator.equals("1")) {
				lsSql = "select stmt_cycle, "
						+ "(new_cycle_month||stmt_cycle) ls_new_cycle_date "
						+ "from	act_acno "
						+ "where id_p_seqno = :id_p_seqno "
					//+ "and acct_type = '01' ";
			      + "and acct_type in (select acct_type from ptr_acct_type where card_indicator = '1') ";
				lsSql += "fetch first 1 rows only ";
				setString("id_p_seqno", mCorpIdSeqno);
			} else {
				lsSql = "select stmt_cycle, "
						+ "(new_cycle_month||stmt_cycle) ls_new_cycle_date "
						+ "from	act_acno "
						+ "where corp_p_seqno = :corp_p_seqno "
					//+ "and acct_type = '02' ";
			      + "and acct_type in (select acct_type from ptr_acct_type where card_indicator = '2') ";
				lsSql += "fetch first 1 rows only ";
				setString("corp_p_seqno", mCorpIdSeqno);
			}
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				lsNewCycleDate = sqlStr("ls_new_cycle_date");
			//ls_stmt_cycle = sql_ss("stmt_cycle");
				if (lsNewCycleDate.length() == 2) {
					lsNewCycleDate = "00000000";
				}
			} else {
				alertErr("read err!!");
				return;
			}
			// --如果營業日小於act_acno的新的關帳日，抓act_chg_cycle的上一次關帳日、利息日--
			lsLastCycleMonth = "";
			lsLastCycleDate = "";
			lsLastInterestDate = "";
			lsNextCloseDate = "";
		//if ((ls_business_date.compareTo(ls_new_cycle_date) < 0)  ||
		//     h_batch_proc_mark.equals("Y")     ) { 
		//if (h_batch_proc_mark.equals("Y") ) { 
		  if (lsBusinessDate.compareTo(lsNewCycleDate) < 0) {
				lsSql = "select last_cycle_month, last_cycle_month||stmt_cycle as last_cycle_date, "
						+ "last_interest_date "
						+ "from	act_chg_cycle "
						+ "where corp_id_seqno = :corp_id_seqno "
						+ "and card_indicator = :card_indicator "
						+ "and chg_cycle_date =:chg_cycle_date "
						+ "and batch_proc_mark = 'Y' ";
				setString("corp_id_seqno", mCorpIdSeqno);
				setString("card_indicator", mCardIndicator);
				setString("chg_cycle_date", mChgCycleDate);
				sqlSelect(lsSql);
				if (sqlRowNum > 0) {
					lsLastCycleMonth = sqlStr("last_cycle_month");
					lsLastCycleDate = sqlStr("last_cycle_date");
					lsLastInterestDate = sqlStr("last_interest_date");
				}
			} else {
			  // 抓ptr_workday的關帳日、利息日--換算出改週期生效時舊cycle最近關帳日、起息日
				lsSql = "select this_acct_month, this_close_date, this_interest_date, "
				    + "next_acct_month, next_close_date, next_interest_date "
						+ "from	ptr_workday "
						+ "where stmt_cycle = :ls_stmt_cycle ";
				setString("ls_stmt_cycle", wp.colStr(ii, "stmt_cycle"));
				sqlSelect(lsSql);
				if (sqlRowNum > 0) {
					lsLastCycleMonth = sqlStr("this_acct_month");
					lsLastCycleDate = sqlStr("this_close_date");
					lsLastInterestDate = sqlStr("this_interest_date");
			  //判斷原cycle是否等於business_date的日期
			    if(this.toInt(lsBusinessDateSubstring78) == this.toInt(lsStmtCycle)){
					  lsLastCycleMonth = sqlStr("next_acct_month");
					  lsLastCycleDate = sqlStr("next_close_date");
					  lsLastInterestDate = sqlStr("next_interest_date");
			    } 
				}
			}
			lsNextCloseDate = hNextCloseDate;
			wp.colSet(ii, "db_next_close_date", lsNextCloseDate);		//20200113 Mantis 2360清空不帶出
			wp.colSet("new_cycle_month", lsNewCycleDate);
			wp.colSet(ii, "db_this_acct_month", lsLastCycleDate);//"db_this_acct_month"定義為最後關帳日期
			wp.colSet("last_cycle_month", lsLastCycleMonth);
			wp.colSet(ii, "db_this_interest_date", lsLastInterestDate);
			wp.colSet("last_interest_date", lsLastInterestDate);
			wp.colSet(ii, "db_m_code", intToStr(liMCode));
			wp.colSet("old_stmt_cycle", lsStmtCycle);

			String ss = wp.colStr(ii, "acct_status");
			String[] cde = new String[] { "1", "2", "3", "4", "5" };
			String[] txt = new String[] { "1-正常", "2-逾放", "3-催收", "4-呆帳", "5-結清" };
			wp.colSet(ii, "db_acct_status", commString.decode(ss, cde, txt));
		}

		// 半年內更改次數
		if (mCardIndicator.equals("1")) {
			lsSql = "select count(*) ct "
					+ "from	act_chg_cycle "
					+ "where card_indicator = '1' "
					+ "and corp_id_seqno = :ls_id_p_seqno "
					+ "and months_between(to_date(:ls_business_date,'yyyymmdd'),to_date(chg_cycle_date,'yyyymmdd')) <= 6 ";
			setString("ls_id_p_seqno", mCorpIdSeqno);
			setString("ls_business_date", lsBusinessDate);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("db_chg_times", sqlStr("ct"));
			}
		} else {
			lsSql = "select count(*) ct "
					+ "from	act_chg_cycle "
					+ "where card_indicator = '2' "
					+ "and corp_id_seqno = :ls_corp_p_seqno "
					+ "and months_between(to_date(:ls_business_date,'yyyymmdd'),to_date(chg_cycle_date,'yyyymmdd')) <= 6 ";
			setString("ls_corp_p_seqno", mCorpIdSeqno);
			setString("ls_business_date", lsBusinessDate);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("db_chg_times", sqlStr("ct"));
			}
		}
		// chi_name
		if (mCardIndicator.equals("1")) {
			lsSql = "select chi_name,id_no,id_no_code "
					+ "from	crd_idno "
					+ "where id_p_seqno = :ls_id_p_seqno";
			setString("ls_id_p_seqno", mCorpIdSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("chi_name", sqlStr("chi_name"));
				wp.colSet("db_id_no", sqlStr("id_no"));
				wp.colSet("kk_id_no", sqlStr("id_no"));
				wp.colSet("id_code", sqlStr("id_no_code"));
			}
		} else {
			lsSql = "select chi_name,corp_no "
					+ "from	crd_corp "
					+ "where corp_p_seqno = :ls_corp_p_seqno";
			setString("ls_corp_p_seqno", mCorpIdSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("chi_name", sqlStr("chi_name"));
				wp.colSet("db_id_no", sqlStr("corp_no"));
				wp.colSet("kk_id_no", sqlStr("corp_no"));
			}
		}
	}

	public int selectActAcno2() throws Exception {
		// wp.dddSql_log = false;
		wp.pageRows = 999;
		if (empty(mCorpIdSeqno)) {
			mCorpIdSeqno = wp.colStr("corp_id_seqno");
		}
		if (empty(mCorpIdSeqno)) {
			alertErr("Key Error");
			return -1;
		}

		wp.sqlCmd = "select acct_type, "
				+ "stmt_cycle, "
				+ "lpad(' ',8) db_this_acct_month, "
				+ "acct_status, "
				+ "0 db_acct_jrnl_bal, "
				+ "0 db_min_pay_bal, "
				+ "acno_p_seqno, "
				+ "corp_p_seqno, "
				+ "0 db_m_code, "
				+ "card_indicator as act_card_indicator, "
				+ "lpad(' ',8) db_this_interest_date, "
				+ "lpad(' ',8) db_next_close_date "
				+ "from act_acno ";
		wp.sqlCmd += "where 1=1 ";
		if (mCardIndicator.equals("1")) {
			wp.sqlCmd += sqlCol(mCorpIdSeqno, "id_p_seqno");
			wp.sqlCmd += " and acct_type in (select acct_type from ptr_acct_type where card_indicator = '1')";
		} else {
			wp.sqlCmd += sqlCol(mCorpIdSeqno, "corp_p_seqno");
			wp.sqlCmd += " and acct_type in (select acct_type from ptr_acct_type where card_indicator = '2')";
		}
		wp.colSet("sql_act_acno", wp.sqlCmd);
		// System.out.println("wp.sqlCmd act_acno : "+wp.sqlCmd);
		pageQuery();
		wp.setListCount(1);
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
		if (sqlNotFind()) {
			alertErr("act_acno明細資料不存在");
			//System.out.println(appMsg.err_condNodata);
			return -1;
		}
		wp.colSet("row_ct", sqlRowNum);

		// 附加資料欄位設定
		wfSetData2();
		return 0;
	}

	void wfSetData2() throws Exception {
		String lsNewCycleDate = "", lsLastCycleDate = "", lsLastInterestDate = "";
		String lsStmtCycle = "", lsLastCycleMonth = "", lsBusinessDateSubstring78 = "";;
		int liMCode = 0;
		String wkCardIndicator = "";
		// act_acno detail data set
		int selCt = wp.selectCnt;
		// System.out.println("sel_ct : " + sel_ct);
		lsSql = "select business_date "
				+ "from	ptr_businday";
		sqlSelect(lsSql);
		lsBusinessDate = sqlStr("business_date");
		lsBusinessDateSubstring78 = strMid(lsBusinessDate,6,2);
		wkCardIndicator = wp.itemStr("kk_card_indicator");
		if (empty(wkCardIndicator)) {
			wkCardIndicator = itemKk("data_k1");
		}
		for (int ii = 0; ii < selCt; ii++) {
			// wk_card_indicator = wp.col_ss(ii, "card_indicator");
			wp.colSet(ii, "no", intToStr(ii + 1));
			lsStmtCycle = wp.colStr(ii,"stmt_cycle");
			//check mcode			
			liMCode = fChkMcode(wp.colStr(ii, "acno_p_seqno")); // 取m_code
			lsLastCycleMonth = "";
			lsNewCycleDate = "";
			lsLastCycleDate = "";
			lsLastInterestDate = "";
			// 抓ptr_workday的關帳日、利息日--換算出改週期生效時舊cycle最近關帳日、起息日
			lsSql = "select this_acct_month, this_close_date, this_interest_date, "
				    + "next_acct_month, next_close_date, next_interest_date "
						+ "from	ptr_workday "
						+ "where stmt_cycle = :ls_stmt_cycle ";
			setString("ls_stmt_cycle", wp.colStr(ii, "stmt_cycle"));
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				lsLastCycleMonth = sqlStr("this_acct_month");
				lsLastCycleDate = sqlStr("this_close_date");
				lsLastInterestDate = sqlStr("this_interest_date");
			//判斷原cycle是否等於business_date的日期
			  if(this.toInt(lsBusinessDateSubstring78) == this.toInt(lsStmtCycle)){
				  lsLastCycleMonth = sqlStr("next_acct_month");
				  lsLastCycleDate = sqlStr("next_close_date");
				  lsLastInterestDate = sqlStr("next_interest_date");
				}
			}
		 
			wp.colSet(ii, "db_this_acct_month", lsLastCycleDate);//"db_this_acct_month"定義為最後關帳日期
			wp.colSet("last_cycle_month", lsLastCycleMonth);
			wp.colSet(ii, "db_this_interest_date", lsLastInterestDate);
			wp.colSet("last_interest_date", lsLastInterestDate);
			wp.colSet(ii, "db_m_code", intToStr(liMCode));
			wp.colSet("old_stmt_cycle", lsStmtCycle);

			String ss = wp.colStr(ii, "acct_status");
			String[] cde = new String[] { "1", "2", "3", "4", "5" };
			String[] txt = new String[] { "1-正常", "2-逾放", "3-催收", "4-呆帳", "5-結清" };
			wp.colSet(ii, "db_acct_status", commString.decode(ss, cde, txt));
		}

		// 半年內更改次數
		if (mCardIndicator.equals("1")) {
			lsSql = "select count(*) ct "
					+ "from	act_chg_cycle "
					+ "where card_indicator = '1' "
					+ "and corp_id_seqno = :ls_id_p_seqno "
					+ "and months_between(to_date(:ls_business_date,'yyyymmdd'),to_date(chg_cycle_date,'yyyymmdd')) <= 6 ";
			setString("ls_id_p_seqno", mCorpIdSeqno);
			setString("ls_business_date", lsBusinessDate);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("db_chg_times", sqlStr("ct"));
			}
		} else {
			lsSql = "select count(*) ct "
					+ "from	act_chg_cycle "
					+ "where card_indicator = '2' "
					+ "and corp_id_seqno = :ls_corp_p_seqno "
					+ "and months_between(to_date(:ls_business_date,'yyyymmdd'),to_date(chg_cycle_date,'yyyymmdd')) <= 6 ";
			setString("ls_corp_p_seqno", mCorpIdSeqno);
			setString("ls_business_date", lsBusinessDate);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("db_chg_times", sqlStr("ct"));
			}
		}
		// chi_name
		if (mCardIndicator.equals("1")) {
			lsSql = "select chi_name,id_no,id_no_code "
					+ "from	crd_idno "
					+ "where id_p_seqno = :ls_id_p_seqno";
			setString("ls_id_p_seqno", mCorpIdSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("chi_name", sqlStr("chi_name"));
				wp.colSet("db_id_no", sqlStr("id_no"));
				wp.colSet("kk_id_no", sqlStr("id_no"));
				wp.colSet("id_code", sqlStr("id_no_code"));
			}
		} else {
			lsSql = "select chi_name,corp_no "
					+ "from	crd_corp "
					+ "where corp_p_seqno = :ls_corp_p_seqno";
			setString("ls_corp_p_seqno", mCorpIdSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				wp.colSet("chi_name", sqlStr("chi_name"));
				wp.colSet("db_id_no", sqlStr("corp_no"));
				wp.colSet("kk_id_no", sqlStr("corp_no"));
			}
		}
	}

	@Override
	public void saveFunc() throws Exception {
		Actp1010Func func = new Actp1010Func(wp);
		String lsSql = "";
		int liMCode = 0, liMCodeMax = 0;
		aaRowid = wp.itemStr("rowid");
		aaModSeqno = wp.itemStr("mod_seqno");
		
		if (strAction.equals("A")) {
			mCardIndicator = wp.itemStr("kk_card_indicator");
		} else {
			mCardIndicator = wp.itemStr("card_indicator");
		}
		mAcctKey = wp.itemStr("kk_id_no");
		if (empty(mAcctKey)) {
			mAcctKey = wp.colStr("db_id_no");
		}
		if(empty(mChgCycleDate)) {
		//mChgCycleDate = getSysDate();
			mChgCycleDate = getBusiDate();
		}
		mIdCode = wp.itemStr("kk_id_code");
		mCorpIdSeqno = wp.colStr("corp_id_seqno");
		mIdPSeqno = wp.colStr("id_p_seqno");
		mCorpPSeqno = wp.colStr("corp_p_seqno");
		if (strAction.equals("D")){
			if (!empty(wp.itemStr("apr_user"))) {
				alertErr("已覆核資料，不可刪除！ " + mAcctKey);
				return;
			}
			rc = func.dbSave(strAction);
			log(func.getMsg());
			if (rc != 1) {
				alertErr(func.getMsg());
			}
			this.sqlCommit(rc);
			return;
		}
		aaNewStmtCycle = wp.itemStr("new_stmt_cycle");
		aaAcctType = wp.itemBuff("acct_type");
		aaStmtCycle = wp.itemBuff("stmt_cycle");
		aaDbThisAcctMonth = wp.itemBuff("db_this_acct_month");
		aaDbThisInterestDate = wp.itemBuff("db_this_interest_date");
		aaDbMCode = wp.itemBuff("db_m_code");
		aaAcnoPSeqno = wp.itemBuff("acno_p_seqno");
		wp.listCount[0] = aaAcctType.length;
		
		//System.out.println("aa_stmt_cycle : "+aa_stmt_cycle[0]);
		

		//
		for (int i = 0; i < aaAcctType.length; i++) {
			liMCode = Integer.parseInt(aaDbMCode[i]);
			if (liMCode > liMCodeMax) {
				liMCodeMax = liMCode;
			}
		}

		// of_validation資料驗證
		if (ofValidation() != 0) {
			return;
		}

		aaNewCycleMonth1 = wp.itemStr("new_cycle_month");
		aaStmtCycle1 = wp.itemStr("old_stmt_cycle");
		//System.out.println("aa_stmt_cycle1 : "+aa_stmt_cycle1);
		aaDbThisAcctMonth1 = wp.itemStr("last_cycle_month");
		aaDbThisInterestDate1 = wp.itemStr("last_interest_date");

		if(strAction.equals("A")){  //因 act_chg_cycle 有建立 unique key，以下"已有相同鍵值資料!!"之判斷仍保留
			lsSql = "select count(*) ct from act_chg_cycle "
					+ "where corp_id_seqno =:corp_id_seqno "
					+ "and card_indicator =:card_indicator "
					+ "and apr_flag <> 'Y'  ";
			setString("corp_id_seqno", mCorpIdSeqno);
			setString("card_indicator", mCardIndicator);
			sqlSelect(lsSql);
			if (sqlNum("ct") > 0) {
				alertErr("未覆核資料不可重覆新增更改帳務週期!!");
				return;
			}
		}

		// update or insert
		wp.logSql = true;
		if(strAction.equals("A")){
			lsSql = "select count(*) ct from act_chg_cycle "
					+ "where corp_id_seqno =:corp_id_seqno "
					+ "and card_indicator =:card_indicator "
				  + "and chg_cycle_date =:chg_cycle_date ";
			setString("corp_id_seqno", mCorpIdSeqno);
			setString("card_indicator", mCardIndicator);
		  setString("chg_cycle_date", mChgCycleDate);
			sqlSelect(lsSql);
			if (sqlNum("ct") > 0) {
			  alertErr("已有相同鍵值資料!!");
				return;
			}
			usSql = "insert into act_chg_cycle ( "
					+ "card_indicator, chg_cycle_date, corp_id_seqno, corp_p_seqno, corp_no, "
					+ "corp_no_code, id_p_seqno, last_cycle_month, new_cycle_month, stmt_cycle, "
					+ "new_stmt_cycle, last_interest_date, batch_proc_mark, batch_proc_date, m_code, "
					+ "apr_flag, apr_date, apr_user, crt_date, crt_time, "
					+ "crt_user, mod_user, mod_time, mod_pgm, mod_seqno) "
					+ "values ("
					+ ":card_indicator , :chg_cycle_date, :corp_id_seqno, :corp_p_seqno, :corp_no, "
					+ ":corp_no_code, :id_p_seqno, :last_cycle_month, :new_cycle_month, :stmt_cycle, "
					+ ":new_stmt_cycle, :last_interest_date, :batch_proc_mark, :batch_proc_date, :m_code, "
					+ ":apr_flag, :apr_date, :apr_user, :crt_date, sysdate, "
					+ ":crt_user, :mod_user, sysdate, 'actp1010', 1)";
			setString("card_indicator", mCardIndicator);
		//setString("chg_cycle_date", getSysDate());
			setString("chg_cycle_date", getBusiDate());

			if (mCardIndicator.equals("1")) {
				setString("corp_id_seqno", mIdPSeqno);
				setString("id_p_seqno", mIdPSeqno);
				setString("corp_p_seqno", "");
				setString("corp_no", "");
			} else {
				setString("corp_id_seqno", mCorpPSeqno);
				setString("corp_p_seqno", mCorpPSeqno);
				setString("corp_no", mAcctKey);
				setString("id_p_seqno", "");
			}
			setString("corp_no_code", "");
			setString("last_cycle_month", strMid(aaDbThisAcctMonth1, 0, 6));
			//setString("new_cycle_month", ss_mid(aa_new_cycle_month1, 0, 6));	//20191211 改由Actp1020設定
			setString("new_cycle_month", "");
			setString("stmt_cycle", aaStmtCycle1);
			setString("new_stmt_cycle", aaNewStmtCycle);
			setString("last_interest_date", aaDbThisInterestDate1);
			setString("batch_proc_mark", "N");
			setString("batch_proc_date", "");
			if (empty(aaDbMCode1)) {
				aaDbMCode1 = "0";
			}
			setString("m_code", aaDbMCode1);
			setString("apr_flag", "");
			setString("apr_date", "");
			setString("apr_user", "");
		//setString("crt_date", getSysDate());
			setString("crt_date", getBusiDate());
			setString("crt_user", wp.loginUser);
			setString("mod_user", wp.loginUser);			
		}
		if(strAction.equals("U")){
			usSql = "update act_chg_cycle set "
					+ "last_cycle_month =:last_cycle_month, "
					+ "new_stmt_cycle =:new_stmt_cycle, "
					+ "last_interest_date =:last_interest_date, "
					+ "m_code =:m_code, "
					+ "batch_proc_mark = 'N', "
					+ "crt_user =:crt_user, "		//20200113 Mantis 2360 change
					+ "crt_date =:crt_date, "		//20200113 Mantis 2360 change
					+ "mod_user =:mod_user, "
					+ "mod_time = sysdate, "
					+ "mod_pgm = 'actp1010', "
					+ "mod_seqno = nvl(mod_seqno,0)+1 "
					+ "where 1=1 ";
			setString("last_cycle_month", strMid(aaDbThisAcctMonth[0], 0, 6));
			setString("new_stmt_cycle", aaNewStmtCycle);
			setString("last_interest_date", aaDbThisInterestDate[0]);
			setString("m_code", intToStr(liMCodeMax));
			setString("crt_user", wp.loginUser);
			setString("crt_date", getSysDate());
			setString("mod_user", wp.loginUser);
			usSql += sqlCol(aaRowid, "hex(rowid)");
			usSql += sqlCol(aaModSeqno, "mod_seqno");
		}
		sqlExec(usSql);
		if (sqlRowNum <= 0) {
			sqlCommit(0);
		} else {
			sqlCommit(1);
		}
		if(strAction.equals("U")){
			alertMsg("修改完成,請重新查詢.");
			wp.colSet("btu", "disabled style='background: lightgray;'");
			wp.colSet("btd", "disabled style='background: lightgray;'");
		}
		
		//20191211以下暫時保留
//		if (!empty(aa_rowid)) {
//			String wk_crt_date = wp.item_ss("crt_date");
//			// 20190515 問題單0001331 user提出非當月資料視為新增資料，不需解覆核。
//			if (!ss_mid(wp.item_ss("crt_date"), 0, 6).equals(ss_mid(get_sysDate(), 0, 6))) {
//				ls_sql = "select count(*) ct from act_chg_cycle "
//						+ "where corp_id_seqno =:corp_id_seqno "
//						+ "and card_indicator =:card_indicator "
//						+ "and chg_cycle_date =:chg_cycle_date ";
//				setString("corp_id_seqno", m_corp_id_seqno);
//				setString("card_indicator", m_card_indicator);
//				setString("chg_cycle_date", m_chg_cycle_date);
//				sqlSelect(ls_sql);
//				if (sql_num("ct") > 0) {
//					alert_err("已有相同鍵值資料!!");
//					return;
//				}
//				us_sql = "insert into act_chg_cycle ( "
//						+ "card_indicator, chg_cycle_date, corp_id_seqno, corp_p_seqno, corp_no, "
//						+ "corp_no_code, id_p_seqno, last_cycle_month, new_cycle_month, stmt_cycle, "
//						+ "new_stmt_cycle, last_interest_date, batch_proc_mark, batch_proc_date, m_code, "
//						+ "apr_flag, apr_date, apr_user, crt_date, crt_time, "
//						+ "crt_user, mod_user, mod_time, mod_pgm, mod_seqno) "
//						+ "values ("
//						+ ":card_indicator , :chg_cycle_date, :corp_id_seqno, :corp_p_seqno, :corp_no, "
//						+ ":corp_no_code, :id_p_seqno, :last_cycle_month, :new_cycle_month, :stmt_cycle, "
//						+ ":new_stmt_cycle, :last_interest_date, :batch_proc_mark, :batch_proc_date, :m_code, "
//						+ ":apr_flag, :apr_date, :apr_user, :crt_date, sysdate, "
//						+ ":crt_user, :mod_user, sysdate, 'actp1010', 1)";
//				setString("card_indicator", m_card_indicator);
//				setString("chg_cycle_date", get_sysDate());
//
//				if (m_card_indicator.equals("1")) {
//					setString("corp_id_seqno", m_id_p_seqno);
//					setString("id_p_seqno", m_id_p_seqno);
//					setString("corp_p_seqno", "");
//					setString("corp_no", "");
//				} else {
//					setString("corp_id_seqno", m_corp_p_seqno);
//					setString("corp_p_seqno", m_corp_p_seqno);
//					setString("corp_no", m_acct_key);
//					setString("id_p_seqno", "");
//				}
//				setString("corp_no_code", "");
//				setString("last_cycle_month", ss_mid(aa_db_this_acct_month1, 0, 6));
//				//setString("new_cycle_month", ss_mid(aa_new_cycle_month1, 0, 6));	//20191211 改由Actp1020設定
//				setString("new_cycle_month", "");
//				setString("stmt_cycle", aa_stmt_cycle1);
//				setString("new_stmt_cycle", aa_new_stmt_cycle);
//				setString("last_interest_date", aa_db_this_interest_date1);
//				setString("batch_proc_mark", "N");
//				setString("batch_proc_date", "");
//				if (empty(aa_db_m_code1)) {
//					aa_db_m_code1 = "0";
//				}
//				setString("m_code", aa_db_m_code1);
//				setString("apr_flag", "");
//				setString("apr_date", "");
//				setString("apr_user", "");
//				setString("crt_date", get_sysDate());
//				setString("crt_user", wp.loginUser);
//				setString("mod_user", wp.loginUser);
//				// System.out.println("is_sql : "+is_sql);
//
//			} else {
//				us_sql = "update act_chg_cycle set "
//						+ "last_cycle_month =:last_cycle_month, "
//						+ "new_stmt_cycle =:new_stmt_cycle, "
//						+ "last_interest_date =:last_interest_date, "
//						+ "m_code =:m_code, "
//						+ "batch_proc_mark = 'N', "
//						+ "mod_user =:mod_user, "
//						+ "mod_time = sysdate, "
//						+ "mod_pgm = 'actp1010', "
//						+ "mod_seqno = nvl(mod_seqno,0)+1 "
//						+ "where 1=1 ";
//				setString("last_cycle_month", ss_mid(aa_db_this_acct_month[0], 0, 6));
//				setString("new_stmt_cycle", aa_new_stmt_cycle);
//				setString("last_interest_date", aa_db_this_interest_date[0]);
//				setString("m_code", int_2Str(li_m_code_max));
//				setString("mod_user", wp.loginUser);
//				us_sql += sql_col(aa_rowid, "hex(rowid)");
//				us_sql += sql_col(aa_mod_seqno, "mod_seqno");
//			}
//			sqlExec(us_sql);
//			if (sql_nrow <= 0) {
//				ll_err++;
//				sql_commit(0);
//			} else {
//				ll_ok++;
//				sql_commit(1);
//			}
//
//		} else {
//			ls_sql = "select count(*) ct from act_chg_cycle "
//					+ "where corp_id_seqno =:corp_id_seqno "
//					+ "and card_indicator =:card_indicator "
//					+ "and chg_cycle_date =:chg_cycle_date ";
//			setString("corp_id_seqno", m_corp_id_seqno);
//			setString("card_indicator", m_card_indicator);
//			setString("chg_cycle_date", m_chg_cycle_date);
//			sqlSelect(ls_sql);
//			if (sql_num("ct") > 0) {
//				alert_err("已有相同鍵值資料!!");
//				return;
//			}
//			is_sql = "insert into act_chg_cycle ( "
//					+ "card_indicator, chg_cycle_date, corp_id_seqno, corp_p_seqno, corp_no, "
//					+ "corp_no_code, id_p_seqno, last_cycle_month, new_cycle_month, stmt_cycle, "
//					+ "new_stmt_cycle, last_interest_date, batch_proc_mark, batch_proc_date, m_code, "
//					+ "apr_flag, apr_date, apr_user, crt_date, crt_time, "
//					+ "crt_user, mod_user, mod_time, mod_pgm, mod_seqno) "
//					+ "values ("
//					+ ":card_indicator , :chg_cycle_date, :corp_id_seqno, :corp_p_seqno, :corp_no, "
//					+ ":corp_no_code, :id_p_seqno, :last_cycle_month, :new_cycle_month, :stmt_cycle, "
//					+ ":new_stmt_cycle, :last_interest_date, :batch_proc_mark, :batch_proc_date, :m_code, "
//					+ ":apr_flag, :apr_date, :apr_user, :crt_date, sysdate, "
//					+ ":crt_user, :mod_user, sysdate, 'actp1010', 1)";
//			setString("card_indicator", m_card_indicator);
//			setString("chg_cycle_date", get_sysDate());
//			if (m_card_indicator.equals("1")) {
//				setString("corp_id_seqno", m_id_p_seqno);
//				setString("id_p_seqno", m_id_p_seqno);
//				setString("corp_p_seqno", "");
//				setString("corp_no", "");
//			} else {
//				setString("corp_id_seqno", m_corp_p_seqno);
//				setString("corp_p_seqno", m_corp_p_seqno);
//				setString("corp_no", m_acct_key);
//				setString("id_p_seqno", "");
//			}
//			setString("corp_no_code", "");
//			setString("last_cycle_month", ss_mid(aa_db_this_acct_month1, 0, 6));
//			//setString("new_cycle_month", ss_mid(aa_new_cycle_month1, 0, 6));	//20191211改由Actp1020設定
//			setString("new_cycle_month", "");
//			setString("stmt_cycle", aa_stmt_cycle1);
//			setString("new_stmt_cycle", aa_new_stmt_cycle);
//			setString("last_interest_date", aa_db_this_interest_date1);
//			setString("batch_proc_mark", "N");
//			setString("batch_proc_date", "");
//			if (empty(aa_db_m_code1)) {
//				aa_db_m_code1 = "0";
//			}
//			setString("m_code", aa_db_m_code1);
//			setString("apr_flag", "");
//			setString("apr_date", "");
//			setString("apr_user", "");
//			setString("crt_date", get_sysDate());
//			setString("crt_user", wp.loginUser);
//			setString("mod_user", wp.loginUser);
//			sqlExec(is_sql);
//			if (sql_nrow <= 0) {
//				ll_err++;
//				sql_commit(0);
//			} else {
//				ll_ok++;
//				sql_commit(1);
//			}
//		}
		// alert_msg("處理成功筆數=" + ll_ok + "; 失敗筆數=" + ll_err + ";");
	}

	public int fChkMcode(String lsAcnoPSeqno) throws Exception {
		int liMcode = 0, i = 0;
		double ldcMpamt = 0;
		String lsAcctym = "", lsAcctMonth = "";
		String lsAcctymd = "", lsAcctMonthdate = "";
		if (empty(lsAcnoPSeqno))
			return 9999;
		// --get this_acct_month by Cycle--
		lsSql = "select decode(this_acct_month,'',' ', this_acct_month) as this_acct_month "
				+ "from ptr_workday "
				+ "where stmt_cycle in (select stmt_cycle "
				+ "from act_acno "
				+ "where acno_p_seqno =:acno_p_seqno) ";
		setString("acno_p_seqno", lsAcnoPSeqno);
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			return 9999;
		} else {
			lsAcctym = sqlStr("this_acct_month");
		}

		// --get PTR_ACTGENERAL_n.min_mp_balance----------
		lsSql = "select mix_mp_balance "
				+ "from ptr_actgeneral_n "
				+ "fetch first 1 rows only ";
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			ldcMpamt = 0;
		} else {
			ldcMpamt = Double.parseDouble(sqlStr("mix_mp_balance"));
		}

		// --DS: act_acag--
  //lsSql = "select p_seqno, seq_no, acct_month, stmt_cycle, pay_amt from act_acag "
		lsSql = "select p_seqno, acct_month, stmt_cycle, pay_amt from act_acag "
				+ "where p_seqno =:p_seqno ";
		setString("p_seqno", lsAcnoPSeqno); // 待確定20190617 Andy
		sqlSelect(lsSql);
		if (sqlRowNum == 0)
			return 0;
		// --CALC M-code-------------------------
		for (i = 0; i < sqlRowNum; i++) {
			lsAcctMonth = sqlStr(i, "acct_month");
			if (lsAcctMonth.compareTo(lsAcctym) > 0)
				break;
			if (Double.parseDouble(sqlStr(i, "pay_amt")) == 0)
				continue;
			ldcMpamt = ldcMpamt - Double.parseDouble(sqlStr(i, "pay_amt"));
			if (ldcMpamt < 0)
				break;
		}
		if (ldcMpamt >= 0)
			return 0;
		lsAcctMonthdate = lsAcctMonth + "01";
		lsAcctymd = lsAcctym + "01";
		liMcode = compMonth(lsAcctMonthdate, lsAcctymd);
		return liMcode;
	}

	// 計算日期區間之月數
	public int compMonth(String sdate, String edate) throws Exception {
		// 設定日期格式
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// 進行轉換
		Date startdate = (Date) sdf.parse(sdate);
		Date enddate = (Date) sdf.parse(edate);

		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		start.setTime(startdate);
		end.setTime(enddate);
		int result = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
		int month = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12;
		int range = Math.abs(month + result);
		return range;
	}

	public int ofValidation() throws Exception {
		String lsSql = "";
		// 20191210 因應問題單1728所加驗證
		// ls_sql = "select count(*)ct from act_chg_cycle "
		// + "where batch_proc_mark = 'Y' "
		// + "and corp_id_pseqno =:corp_id_pseqno "
		// + "and card_indicator =:"
		String cycle1 = "",cycle2 = "";
		// --檢查原帳務週期有不同週期--
		if (strAction.equals("U")||strAction.equals("D")) {
			if(wp.itemStr("apr_flag").equals("Y")){
				alertErr("已覆核資料,不可執行修改、刪除作業");
				return -1;
			}
		}
		String lsNewStmtCycle = wp.itemStr("new_stmt_cycle");
		for (int ii = 0; ii < aaAcctType.length; ii++) {
			if(lsNewStmtCycle.equals(aaStmtCycle[ii])){
				alertErr("錯誤~不可更為相同帳務週期！");
				return -1;
			}
		}

		if (aaAcctType.length > 1) {
			for (int ii = 0; ii < aaAcctType.length; ii++) {
				if(ii >=1){
					cycle1 = aaStmtCycle[ii - 1];
					cycle2 = aaStmtCycle[ii];
					if (cycle1.equals(cycle2)) {
						continue;
					} else {
						alertErr("錯誤~原帳務週期有不同週期，不可更改！");
						return -1;
					}
				}				
			}
		}
		// --檢查証號--
		// if (is_action.equals("A")) {
		// if (select_act_acno() != 0) {
		// alert_err("錯誤~在act_acno無此証號:" + m_acct_key);
		// return -1;
		// }
		// wf_set_data();
		// }
		if (aaAcctType.length == 0) {
			alertErr("錯誤~在act_acno無此証號:" + mAcctKey);
			return -1;
		}
		//
		if (mCardIndicator.equals("1")) {
			lsSql = "select count(*) ct "
					+ "from crd_idno "
					+ "where id_p_seqno = :id_p_seqno ";
			setString("id_p_seqno", mIdPSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				if (sqlStr("ct").equals("0")) {
					alertErr("crd_idno無此証號 : " + mAcctKey);
					return -1;
				}
			}
		} else {
			lsSql = "SELECT count(*) ct "
					+ "FROM	crd_corp "
					+ "WHERE corp_p_seqno = :corp_p_seqno ";
			setString("corp_p_seqno", mCorpPSeqno);
			sqlSelect(lsSql);
			if (sqlRowNum > 0) {
				if (sqlStr("ct").equals("0")) {
					alertErr("crd_corp無此証號 : " + mAcctKey);
					return -1;
				}
			}
		}
		// --檢查覆核--
		if (strAction.equals("U")) {
			if (!empty(wp.itemStr("apr_date"))) {
				alertErr("已覆核資料，不可更新或刪除！ " + mAcctKey);
				return -1;
			}
//			if (ss_mid(wp.item_ss("crt_date"), 0, 6).equals(ss_mid(get_sysDate(), 0, 6))) {
//				if (!empty(wp.item_ss("apr_date"))) {
//					alert_err("已覆核，不可更新或刪除！ " + m_acct_key);
//					return -1;
//				}
//			}
		}
		// --檢查新帳務週期存在否--
		lsSql = "select count(*) ct "
				+ "from	ptr_workday "
				+ "where stmt_cycle = :ls_new_stmt_cycle";
		setString("ls_new_stmt_cycle", wp.itemStr("new_stmt_cycle"));
		sqlSelect(lsSql);
		if (sqlRowNum > 0) {
			if (sqlStr("ct").equals("0")) {
				alertErr("無此帳務週期！");
				return -1;
			}
		}
		return 0;
	}

  private String getBusiDate() {
	  String sqlCmd = "select BUSINESS_DATE from PTR_BUSINDAY ";
	  sqlSelect(sqlCmd);
	  return sqlStr("BUSINESS_DATE");
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
			// wp.initOption = "--";

			// wp.optionKey = wp.item_ss("ex_dept_no");
			// this.dddw_list("dddw_dept_no", "ptr_dept_code", "dept_code",
			// "dept_name", "where 1=1 order by dept_code ");
			//
			// wp.initOption = "--";
			// wp.optionKey = wp.item_ss("ex_crt_user");
			// this.dddw_list("dddw_crt_user", "sec_user", "usr_id",
			// "usr_cname", "where 1=1 order by usr_id ");

		} catch (Exception ex) {
		}
	}

	void itemchanged() throws Exception {
		String lsId = wp.itemStr("kk_id_no");// 抓前端輸入的ID
		String lsIdnoCode = wp.itemStr("kk_idno_code");
		mAcctKey = wp.itemStr("kk_id_no");
		String lsCardIndicator = wp.itemStr("kk_card_indicator");
		mCardIndicator = wp.itemStr("kk_card_indicator");
		
		String lsSql = "";
		if (lsCardIndicator.equals("1")) {
			lsSql = "select id_p_seqno, "
					+ "chi_name "
					+ "from crd_idno "
					+ "where 1=1 ";
			lsSql += " and id_no = :id "
					+ " and id_no_code =:idno_code";
			setString("id", lsId);
			setString("idno_code", lsIdnoCode);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet("db_chi_name", "查無卡人資料!!");
			} else {
				wp.colSet("db_chi_name", sqlStr("chi_name"));
				wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
				wp.colSet("corp_p_seqno", "");
				wp.colSet("corp_id_seqno", sqlStr("id_p_seqno"));
				mCorpIdSeqno = sqlStr("id_p_seqno");
			}
		} else {
			lsSql = "select corp_p_seqno, "
					+ "chi_name "
					+ "from crd_corp "
					+ "where 1=1 ";
			lsSql += " and corp_no = :corp_no ";
			setString("corp_no", lsId);
			sqlSelect(lsSql);
			if (sqlRowNum <= 0) {
				wp.colSet("db_chi_name", "查無卡人資料!!");
			} else if (sqlRowNum == 1) {
				wp.colSet("db_chi_name", sqlStr("chi_name"));
				wp.colSet("id_p_seqno", "");
				wp.colSet("corp_p_seqno", sqlStr("corp_p_seqno"));
				wp.colSet("corp_id_seqno", sqlStr("corp_p_seqno"));
				mCorpIdSeqno = sqlStr("corp_p_seqno");
			}
		}
		selectActAcno2();
	}
	
	//20200113 Mantis 2360 add 
	String sellectUsrcname(String userid) throws Exception{
		String sqlSelect=" select usr_id||'['||usr_cname||']' as userid  from sec_user where usr_id = :usr_id ";
		setString("usr_id",userid);
		sqlSelect(sqlSelect);
		if(sqlRowNum>0){
			userid = sqlStr("userid");
		}
		return userid; 
	}
}
