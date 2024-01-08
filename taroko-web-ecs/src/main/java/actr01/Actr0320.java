/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR     DESCRIPTION                                *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-25  V1.00.00  Max Lin    program initial                            *
* 109-04-15  V1.00.01  Alex       add auth_query									  *
* 110-03-04  v1.00.02  Andy       Update PDF隠碼作業                                                                      *
* 111-10-20  v1.00.03  Zuwei Su   sync from mega, update coding standard                    *
******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0320 extends BaseReport {

	InputStream inExcelFile = null;
	String mProgName = "actr0320";

	String condWhere = "";

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;

		strAction = wp.buttonCode;
		log("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);

		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			// strAction="new";
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
		// init_button();
	}

	@Override
	public void clearFunc() throws Exception {
		wp.resetInputData();
		wp.resetOutputData();
	}

	private boolean getWhereStr() throws Exception {
		String exDateS = wp.itemStr("ex_date_S");
		String exDateE = wp.itemStr("ex_date_E");
		// String ex_id = wp.itemStr("ex_id");

		if (this.chkStrend(exDateS, exDateE) == false) {
			alertErr("[處理日期-起迄]  輸入錯誤");
			return false;
		}
		if (empty(exDateS) == true && empty(exDateE) == true) {
			alertErr("請輸入處理日期");
			return false;
		}

		// 固定條件
		String lsWhere = " where 1=1 ";

		if (empty(exDateS) == false) {
			lsWhere += " and print_date >= :ex_date_S ";
			setString("ex_date_S", exDateS);
		}

		if (empty(exDateE) == false) {
			lsWhere += " and print_date <= :ex_date_E ";
			setString("ex_date_E", exDateE);
		}

		busi.func.ColFunc func = new busi.func.ColFunc();
		func.setConn(wp);
		if (func.fAuthQuery(wp.modPgm(), wp.itemStr("ex_id")) != 1) {
			alertErr(func.getMsg());
			return false;
		}

		if (wp.itemEmpty("ex_id") == false) {
			lsWhere += " and uf_acno_key(p_seqno) like :ex_id || '%' ";
			setString("ex_id", wp.itemStr("ex_id"));
		}

		wp.whereStr = lsWhere;
		setParameter();
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		if (getWhereStr() == false)
			return;

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

		wp.sqlCmd = "select print_date, "
				// + "(acct_type concat ' - ' concat acct_key) as acct_type_key,
				+ "(acct_type concat ' - ' concat uf_acno_key(p_seqno)) as acct_type_key, "
				+ "(acct_type concat ' - ' concat uf_hi_idno(substr(uf_acno_key(p_seqno),1,10))||substr(uf_acno_key(p_seqno),11,1)) as db_hi_acct_type_key, "
				// + "(id_no concat ' ' concat id_no_code) as id_no_code, "
				+ "(case when acct_type in ('02','03') then substr(uf_acno_key(p_seqno),1,8) "
				+ " when acct_type not in ('02','03') then substr(uf_acno_key(p_seqno),1,10) concat ' ' concat "
				+ " substr(uf_acno_key(p_seqno),11,1) else '' end) as id_no_code, "
				+ "(case when acct_type in ('02','03') then substr(uf_acno_key(p_seqno),1,8) "
				+ " when acct_type not in ('02','03') then uf_hi_idno(substr(uf_acno_key(p_seqno),1,10)) concat ' ' concat "
				+ " substr(uf_acno_key(p_seqno),11,1) else '' end) as db_hi_id_no_code, "
				+ "chi_name, "
				+ "uf_hi_cname(chi_name) as db_hi_chi_name, "
				+ "(case when acct_status = '1' then '1-正常' when acct_status = '2' then '2-逾放' when acct_status = '3' then '3-催收' when acct_status = '4' then '4-呆帳' else '' end) as acct_status, "
				+ "rc_use_s_date, "
				+ "rc_use_e_date "
				+ "from act_m002r2 "
				+ wp.whereStr
				// +" order by print_date, acct_type, acct_key ";
				+ " order by print_date, acct_type, uf_acno_key(p_seqno) ";
		wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";

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
		wp.colSet("loginUser", wp.loginUser);
		wp.setPageValue();
		listWkdata();
	}

	void listWkdata() throws Exception {
		int rowCt = 0;

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// 計算欄位
			rowCt += 1;
		}

		wp.colSet("row_ct", intToStr(rowCt));
	}

	void xlsPrint() {
		try {
			log("xlsFunction: started--------");
			wp.reportId = mProgName;

			// -cond-
			String exDateS = wp.itemStr("ex_date_S");
			String exDateE = wp.itemStr("ex_date_E");
			// String ex_id = wp.itemStr("ex_id") + "%";
			String exId = "";
			if (wp.itemEmpty("ex_id") == false) {
				exId = wp.itemStr("ex_id") + "%";
			}

			String cond1 = "處理日期: " + exDateS + " ~ " + exDateE + "  身分證字號: " + exId;
			wp.colSet("cond_1", cond1);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			xlsx.excelTemplate = mProgName + ".xlsx";

			// ====================================
			xlsx.sheetName[0] = "無有效卡例外允用 RC 分析表";
			queryFunc();
			wp.setListCount(1);
			log("Summ: rowcnt:" + wp.listCount[1]);
			xlsx.processExcelSheet(wp);

			xlsx.outputExcel();
			xlsx = null;
			log("xlsFunction: ended-------------");

		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}

	void pdfPrint() throws Exception {
		String exDateS = wp.itemStr("ex_date_S");
		String exDateE = wp.itemStr("ex_date_E");
		// String ex_id = wp.itemStr("ex_id") + "%";
		String exId = "";
		if (wp.itemEmpty("ex_id") == false) {
			exId = wp.itemStr("ex_id") + "%";
		}

		if (this.chkStrend(exDateS, exDateE) == false) {
			alertErr("[處理日期-起迄]  輸入錯誤");
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		if (empty(exDateS) == true && empty(exDateE) == true) {
			alertErr("請輸入處理日期");
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		wp.reportId = mProgName;
		// -cond-
		String cond_1 = "處理日期: " + exDateS + " ~ " + exDateE + "  身分證字號: " + exId;
		wp.colSet("cond_1", cond_1);
		wp.pageRows = 99999;

		queryFunc();
		// wp.setListCount(1);

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "N";
		pdf.excelTemplate = mProgName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 28;
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
			// dddw_bank_id
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_bank_id");
			dddwList("dddw_bank_id", "ptr_bank_allot", "bank_id", "bank_name", "where 1=1 order by bank_id");

		} catch (Exception ex) {
		}
	}

}
