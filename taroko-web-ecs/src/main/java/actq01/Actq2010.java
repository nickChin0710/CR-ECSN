/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 106-11-05  V1.00.00  OrisChang  program initial                            *
 * 109-11-02  V1.00.01  Andy       Upadte Mantis4437                          *
 * 109-11-02  V1.00.01  Andy       Upadte Mantis4437  PDF page count          *
 * 111/10/24  V1.00.02  jiangyigndong  updated for project coding standard    *
 * 112-06-29  V1.00.03  Simon      取消銀行代號:顯示 in dddwSelect()          *
 ******************************************************************************/

package actq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actq2010 extends BaseEdit {
	String mProgName = "actq2010";
	String reportSubtitle = "";

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
	public void queryFunc() throws Exception {
		// 設定queryRead() SQL條件

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		// select columns

		// wp.selectSQL = "hex(rowid) as
		// rowid,ACCT_MONTH,CURR_CODE,DC_PAY_AMT,PAY_AMT,DC_PAY_AMT as
		// DC_PAY_AMT2,'N' as appr_yn, 'U' as status_ua ";
		wp.selectSQL = " batch_no,                                           " +
				" serial_no,                                          " +
				" pay_card_no,                                        " +
				" pay_amt,                                            " +
				" pay_date,                                           " +
				" payment_type,                                       " +
				// " ACCT_TYPE||'-'||ACCT_KEY as acct_no, " +
				" acct_type||'-'||uf_acno_key(p_seqno) as acct_no,    " +
				" debit_item,                                         " +
				" debt_key,                                           " +
				" uf_nvl(curr_code,'901') curr_code,                     " +
				" uf_dc_amt(curr_code,pay_amt,dc_pay_amt) dc_pay_amt, " +
				" uf_acno_name(p_seqno) db_acno_name,                  "
				+ "1 row_ct ";

		// table name
		wp.daoTable = "act_pay_detail";
		// where sql
		wp.whereStr = " WHERE 1=1 ";
		if (empty(wp.itemStr("ex_batch_no")) == false) {
			wp.whereStr += " and batch_no = :batch_no ";
			this.setString("batch_no", wp.itemStr("ex_batch_no"));
		}
		if (empty(wp.itemStr("ex_curr_code")) == false) {
			wp.whereStr += " and uf_nvl(curr_code,'901') = :curr_code ";
			this.setString("curr_code", wp.itemStr("ex_curr_code"));
		}
		// order column
		wp.whereOrder = " ORDER BY batch_no ASC, serial_no ASC ";

		pageQuery();
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		wp.setListCount(1);

		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
		wp.setPageValue();
		String lsSql = "select count(*) ct,"
				+ "sum(uf_dc_amt(curr_code,pay_amt,dc_pay_amt)) sum_dc_pay_amt,"
				+ "sum(pay_amt) sum_pay_amt "
				+ "from act_pay_detail "
				+ "where 1=1 ";
		lsSql += sqlCol(wp.itemStr("ex_batch_no"), "batch_no");
		lsSql += sqlCol(wp.itemStr("ex_curr_code"), "uf_nvl(curr_code,'901')");
		sqlSelect(lsSql);
		wp.colSet("sum_ct", sqlStr("ct"));
		wp.colSet("sum_amt", sqlStr("sum_dc_pay_amt"));
		wp.colSet("sum_amt1", sqlStr("sum_pay_amt"));
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
			wp.optionKey = wp.itemStr("ex_batch_no");
			// this.dddw_list("dddw_batch_no", "act_pay_batch", "distinct
			// batch_no", "substr(batch_no,9,4) ||'-'|| substr(batch_no,13,3)",
			// "where 1=1 order by batch_no");
		//this.dddwList("dddw_batch_no", "act_pay_batch", "distinct batch_no", "batch_no||' [來源:'||substr(batch_no,9,4)||'] [銀行:'||substr(batch_no,13,3)||']'", "where 1=1 order by batch_no");
			this.dddwList("dddw_batch_no", "act_pay_batch", "distinct batch_no", "batch_no||' [來源:'||substr(batch_no,9,4)||']'", "where 1=1 order by batch_no");

			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_curr_code");
			this.dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	void subTitle() {
		String ex_batchno = wp.itemStr("ex_batchno");
		String exCurrCode = wp.itemStr("ex_curr_code");
		String ss = "";

		if (empty(ex_batchno) == false) {
			ss += " 批號 : " + ex_batchno;
		}

		if (empty(exCurrCode) == false) {
			ss += " 結算幣別 : " + exCurrCode;
		}

		reportSubtitle = ss;
	}

	void xlsPrint() {
		try {
			log("xlsFunction: started--------");
			wp.reportId = mProgName;
			// -cond-
			subTitle();
			wp.colSet("cond_1", reportSubtitle);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "Y";
			xlsx.excelTemplate = mProgName + ".xlsx";

			// ====================================
			// -明細-
			xlsx.sheetName[0] = "明細";
			queryFunc();
			if(sqlRowNum <=0) wp.respHtml = "TarokoErrorPDF";
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
			xlsx.processExcelSheet(wp);
			/*
			 * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where);
			 * wp.listCount[1] =sqlRowNum; log("Summ: rowcnt:" +
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
		queryFunc();

		// wp.setListCount(1);
		wp.colSet("user_id", wp.loginUser);
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = mProgName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 30;
		// pdf.pageVert= true; //直印
		pdf.procesPDFreport(wp);

		pdf = null;
	}
}
