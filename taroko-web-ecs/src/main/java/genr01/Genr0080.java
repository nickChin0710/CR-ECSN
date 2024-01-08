/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-29  V1.00.00  Andy Liu   program initial                            *
* 109-10-20  V1.00.01  Andy       update:Mantis4445                          *	
* 109-11-18  V1.00.02  Andy       update:Mantis4712                          *	
* 112-03-15  V1.00.03  Zuwei Su   sync from mega, update:coding standard rule，輸出Excel和PDF問題修復                *  
* 112-03-22  V1.00.04  Zuwei Su   增欄位’年費餘額(AF)’、’預借現金手續費餘額(CF)’、’一般手續費餘額(PF)’，‘基金餘額’ 修訂為 ‘現金回饋餘額’                *  
******************************************************************************/
package genr01;

import java.io.InputStream;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel2;
import taroko.com.TarokoPDF;

public class Genr0080 extends BaseEdit {

	InputStream inExcelFile = null;
	String mProgName = "genr0080";

	String condWhere = "";
	String reportSubtitle = "";
	String mCheckDate = "";
	String mCurrCode = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			// is_action="new";
			// clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			dataRead();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			strAction = "XLS";
			// wp.setExcelMode();
			xlsPrint();
		} else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
			strAction = "PDF";
			// wp.setExcelMode();
			pdfPrint();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void clearFunc() throws Exception {
		wp.resetInputData();
		wp.resetOutputData();
	}

	@Override
	public void initPage() {
		// 設定初始搜尋條件值
		// String sysdate1="",sysdate0="";
		// sysdate1 = ss_mid(get_sysDate(),0,8);
		// 續卡日期起-迄日
		// wp.col_set("exDateS", "");
		// wp.col_set("exDateE", sysdate1);
	}

	private boolean getWhereStr() throws Exception {
		String exDate1 = wp.itemStr("ex_date1");
		String exDate2 = wp.itemStr("ex_date2");
		String exCurr = wp.itemStr("ex_curr");

		String lsWhere = "where 1=1  ";
		// 固定搜尋條件

		// user搜尋條件

		// 回饋日期
		if (chkStrend(exDate1, exDate2) == false) {
			alertErr("日期起迄輸入錯誤!!");
			return false;
		}
		lsWhere += sqlStrend(exDate1, exDate2, "check_date");

		// curr_code幣別
		if (empty(exCurr) == false) {
			lsWhere += sqlCol(exCurr, "curr_code");
		}
		// } else {
		// // 未選強制給值"901"新台幣
		// ls_where += "and curr_code = '901'";
		// }
		wp.whereStr = lsWhere;
		setParameter();
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
//		if (getWhereStr() == false)
//			return;
		// cond_where = wp.whereStr + "";
		// wp.whereStr =cond_where;

		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	private void setParameter() throws Exception {

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (getWhereStr() == false)
			return;

		wp.selectSQL = ""
				+ "check_date, "
				+ "bl_bal, "
				+ "ca_bal, "
				+ "id_bal, "
				+ "it_bal, "
				+ "ao_bal, "
				+ "ot_bal, "
				+ "(bl_bal + ca_bal + id_bal + it_bal + ao_bal + ot_bal) used_bal, "
				+ "lf_bal, "
				+ "cb_bal, "
				+ "ci_bal, "
				+ "cc_bal, "
				+ "sf_bal, "
				+ "(cc_bal + sf_bal) cc_sf_bal, "
				+ "ri_bal, "
                + "af_bal, "
                + "cf_bal, "
                + "pf_bal, "
				+ "pn_bal, "
				+ "op_bal, "
				+ "lk_bal, "
				+ "(op_bal + lk_bal) op_lk_bal, "
				+ "fund_bal, "
				+ "bonus_notax, "
				+ "bonus_tax, "
//				+ "bonus_notax_adv, " // 欄位缺失
//				+ "bonus_tax_adv, "
				+ "vd_bonus_notax, "
				+ "vd_bonus_tax, "
				+ "(bonus_notax + bonus_tax "
//				+ " + bonus_notax_adv + bonus_tax_adv "
				+ " + vd_bonus_notax + vd_bonus_tax) bonus_bal, "
				+ "bonus_merchant, "
				+ "bonus_mmk, "
				+ "in_bal, "
				+ "problem_bal, "
				+ "illicit_bal, "
				+ "(problem_bal + illicit_bal) problem_tot, "
				+ "cash_use_bal, "
				+ "cycle_billed_amt, "
				+ "(cash_use_bal + cycle_billed_amt) ibm_cash_use_bal, "
				+ "vd_bl_bal, "
				+ "vd_ca_bal, "
				+ "vd_lf_bal, "
				+ "vd_refund_bal, "
				+ "vd_problem_bal, "
				+ "vd_illicit_bal, "
				+ "(vd_problem_bal + vd_illicit_bal) vd_problem_tot, "
				+ "vd_deduct_amt, "
				+ "vd_return_amt, "
				+ "(vd_bl_bal + vd_ca_bal + vd_ot_bal + vd_return_amt) vd_used_bal, "
				+ "autopay_amt, "
				+ "offset_amt, "
				+ "onuspay_amt, "
				+ "(autopay_amt + offset_amt + onuspay_amt) bankpay_amt, "
				+ "vd_ot_bal, "
				+ "bonus_ibon, "
				+ "(bonus_merchant + bonus_mmk + bonus_ibon) bonus_pay, "
				+ "curr_code, "
				+ "' ' wk_empty,"
				+ "mod_user, "
				+ "mod_time, "
				+ "mod_pgm, "
				+ "mod_seqno ";
		wp.daoTable = " act_master_bal ";
		wp.whereOrder += " order by check_date,curr_code desc ";

		// setParameter();
		// System.out.println("select " + wp.selectSQL + " from "
		// +wp.daoTable+wp.whereStr+wp.whereOrder);
		// wp.daoTable);

		if (strAction.equals("XLS")) {
			selectNoLimit();
		}
		pageQuery();
		// list_wkdata();
		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr("此條件查無資料");
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
		wp.setPageValue();
		// list_wkdata();
	}

	void listWkdata() throws Exception {
		int rowCt = 0;

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// 計算欄位
			rowCt += 1;
			wp.colSet(ii, "group_ct", "1");
		}
		wp.colSet("row_ct", intToStr(rowCt));
	}

	@Override
	public void dataRead() throws Exception {
		mCheckDate = itemKk("data_k1");
		mCurrCode = itemKk("data_k2");

		wp.selectSQL = ""
				+ "check_date, "
				+ "bl_bal, "
				+ "ca_bal, "
				+ "id_bal, "
				+ "it_bal, "
				+ "ao_bal, "
				+ "ot_bal, "
				+ "(bl_bal + ca_bal + id_bal + it_bal + ao_bal + ot_bal) used_bal, "
				+ "lf_bal, "
				+ "cb_bal, "
				+ "ci_bal, "
				+ "cc_bal, "
				+ "sf_bal, "
				+ "(cc_bal + sf_bal) cc_sf_bal, "
				+ "ri_bal, "
                + "af_bal, "
                + "cf_bal, "
                + "pf_bal, "
				+ "pn_bal, "
				+ "op_bal, "
				+ "lk_bal, "
				+ "(op_bal + lk_bal) op_lk_bal, "
				+ "fund_bal, "
				+ "bonus_notax, "
				+ "bonus_tax, "
//				+ "bonus_notax_adv, "
//				+ "bonus_tax_adv, "
				+ "vd_bonus_notax, "
				+ "vd_bonus_tax, "
				+ "(bonus_notax + bonus_tax "
//				+ "+ bonus_notax_adv + bonus_tax_adv "
				+ "+ vd_bonus_notax + vd_bonus_tax) bonus_bal, "
				+ "bonus_merchant, "
				+ "bonus_mmk, "
				+ "in_bal, "
				+ "problem_bal, "
				+ "illicit_bal, "
				+ "(problem_bal + illicit_bal) problem_tot, "
				+ "cash_use_bal, "
				+ "cycle_billed_amt, "
				+ "(cash_use_bal + cycle_billed_amt) ibm_cash_use_bal, "
				+ "vd_bl_bal, "
				+ "vd_ca_bal, "
				+ "vd_lf_bal, "
				+ "vd_refund_bal, "
				+ "vd_problem_bal, "
				+ "vd_illicit_bal, "
				+ "(vd_problem_bal + vd_illicit_bal) vd_problem_tot, "
				+ "vd_deduct_amt, "
				+ "vd_return_amt, "
				+ "(vd_bl_bal + vd_ca_bal + vd_ot_bal + vd_return_amt) vd_used_bal, "
				+ "autopay_amt, "
				+ "offset_amt, "
				+ "onuspay_amt, "
				+ "(autopay_amt + offset_amt + onuspay_amt) bankpay_amt, "
				+ "vd_ot_bal, "
				+ "bonus_ibon, "
				+ "(bonus_merchant + bonus_mmk + bonus_ibon) bonus_pay, "
				+ "curr_code, "
				+ "' ' wk_empty,"
				+ "mod_user, "
				+ "mod_time, "
				+ "mod_pgm, "
				+ "mod_seqno";
		wp.daoTable = " act_master_bal ";
		wp.whereStr = "where 1=1";
		wp.whereStr += sqlCol(mCheckDate, "check_date");
		wp.whereStr += sqlCol(mCurrCode, "curr_code");

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料");
		}
	}

	void subTitle() {
		String ss = "";
		// ex_toibmdate送IBM取三軌日期
		// batchno製卡別
		ss += " 核帳日期 : " + itemKk("data_k1");
		ss += " 結算幣別 : " + itemKk("data_k2");

		reportSubtitle = ss;
	}

	void xlsPrint() {
		try {
			log("xlsFunction: started--------");
			wp.reportId = mProgName;
			// -cond-
			subTitle();
			wp.colSet("cond_1", reportSubtitle);
			wp.colSet("user_id", wp.loginUser);

			// ===================================
			TarokoExcel2 xlsx = new TarokoExcel2();
			wp.fileMode = "Y";
			xlsx.excelTemplate = mProgName + ".xlsx";
			//分頁欄位
			xlsx.breakField[0] ="check_date";
			xlsx.breakField[1] ="curr_code";
			// -明細-
			xlsx.pageBreak = "Y";
			xlsx.pageCount = 34;
			xlsx.sheetName[0] = "明細";
			dataRead();	
			
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
			xlsx.processExcelSheet(wp);
			/*
			 * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where);
			 * wp.listCount[1] =sql_nrow; ddd("Summ: rowcnt:" +
			 * wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
			 */
			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");

		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}

	void pdfPrint() throws Exception {
		wp.reportId = mProgName;
		// -cond-
		subTitle();
		wp.colSet("cond_1", reportSubtitle);
		// ===========================
		wp.pageRows = 99999;
		dataRead();

		wp.setListCount(1);

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = mProgName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 44;
		pdf.pageVert = true; // 直印
		pdf.procesPDFreport(wp);

		pdf = null;
	}

	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dddwSelect() {
		try {
			// ptr_currcode
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_curr");
			this.dddwList("dddw_curr", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton()  {
		// TODO Auto-generated method stub

	}

}
