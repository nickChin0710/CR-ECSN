/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-28  V1.00.00  Andy Liu   program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *	
* 107-05-10  V1.00.02  Andy		  update : SQL ,UI,report                    *	
* 110-03-05  v1.00.03  Andy       Update PDF隠碼作業                                                                      *
* 111-10-20  v1.00.04  Zuwei Su   sync from mega, update coding standard                    *
******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0700 extends BaseReport {
	CommString commStr = new CommString();
	InputStream inExcelFile = null;
	String mProgName = "actr0700";

	String condWhere = "";
	String reportSubtitle = "";

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
		String exDateS = wp.itemStr("exDateS");
		String exDateE = wp.itemStr("exDateE");
		String exIdNo = wp.itemStr("ex_id_no");
		String exRtnStaus = wp.itemStr("ex_rtn_staus");
		String exAutopayAcctNo = wp.itemStr("ex_autopay_acct_no");
			
		wp.whereStr = "where 1=1  ";
		if(empty(exDateS) 
				&& empty(exDateE) 
				&& empty(exIdNo) 
				&& empty(exRtnStaus)
				&& empty(exAutopayAcctNo)){
			alertErr("請輸入至少一項查詢條件!!");
			return false;
		}
		if (!empty(exDateS) & !empty(exDateE)) {
			if (exDateE.compareTo(exDateS) < 0) {
				alertErr("處理日期起迄輸入錯誤!!");
				return false;
			}
		}
//		if (empty(ex_acct_key) == false) {
//			String ls_acct_key = fillZeroAcctKey(ex_acct_key);
//			wp.whereStr += " and b.acct_key =:ex_acct_key ";
//			setString("ex_acct_key", ls_acct_key);
//		}
		wp.whereStr += sqlStrend(exDateS,exDateE, "process_date");
		wp.whereStr += sqlCol(exIdNo, "uf_idno_id(id_p_seqno)");
		wp.whereStr += sqlCol(exRtnStaus, "rtn_status");
		wp.whereStr += sqlCol(exAutopayAcctNo, "autopay_acct_no");
		setParameter();
		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		if (getWhereStr() == false)
			return;
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
				+ "process_date, "
				+ "bank_no, "
				+ "acct_type, "
				+ "p_seqno, "
				+ "id_p_seqno, "
				+ "uf_idno_id(id_p_seqno) as id_no, "
				+ "uf_hi_idno(uf_idno_id(id_p_seqno)) as db_hi_id_no, "
				+ "corp_p_seqno, "
				+ "autopay_acct_no, "
				+ "autopay_id, "
				+ "uf_hi_acctno(autopay_acct_no) as db_hi_autopay_acct_no, "
				+ "uf_hi_idno(autopay_id) as db_hi_autopay_id, "
				+ "autopay_id_code, "
				+ "stmt_cycle, "
				+ "rtn_date, "
				+ "rtn_status, "
				+ "cellar_phone, "
				+ "sms_flag, "
				+ "mod_user, "
				+ "mod_time, "
				+ "mod_pgm";
		wp.daoTable = " act_post_dtl ";
	//wp.whereOrder = " order by process_date,autopay_id ";
		wp.whereOrder = " order by process_date,autopay_acct_no ";

		// setParameter();
		// System.out.println("select " + wp.selectSQL + " from "
		// +wp.daoTable+wp.whereStr);
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
		listWkdata();
	}

	void listWkdata() throws Exception {
		int rowCt = 0;
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// 計算欄位
			rowCt += 1;
			String op = wp.colStr(ii, "rtn_status");
			String[] cde1 = new String[] { "00", "03", "06", "07", "08", "09", "10", "11", "12", "13", "14", "16", "17"
					, "18", "19", "41", "42", "43", "44", "49", "91", "98"};
			String[] txt1 = new String[] { "00-成功", "03-已終止代繳", "06-凍結戶或警示戶",
					"07-業務支票專戶","08-帳號錯誤","09-終止戶","10-身分證號不符","11-轉出戶","12-拒絕往來戶","13-無此用戶編號",
					"14-用戶編號已存在","16-管制帳戶","17-掛失戶","18-異常交易帳戶","19-用戶編號非英數字元","41-(*)局帳號不符",
					"42-(*)戶名不符","43-(*)身分證號不符","44-(*)印鑑不符","49-(*)其他","91-規定期限內未有扣款","98-其他" };

			wp.colSet(ii, "rtn_status", commStr.decode(op, cde1, txt1));

		  String sql1 = " select "
						+ " chi_name , "
						+ " uf_hi_cname(chi_name) as db_hi_chi_name "
						+ " from  crd_idno "
						+ " where id_p_seqno = :ps_id_p_seqno "
						;
			setString("ps_id_p_seqno", wp.colStr(ii, "id_P_seqno"));
		  sqlSelect(sql1);
			wp.colSet(ii, "chi_name", sqlStr("chi_name"));
			wp.colSet(ii, "db_hi_chi_name", sqlStr("db_hi_chi_name"));
		}
		wp.colSet("row_ct", intToStr(rowCt));
		wp.colSet("user_id", wp.loginUser);
	}

	void subTitle() {
		String exDateS = wp.itemStr("exDateS");
		String exDateE = wp.itemStr("exDateE");
		String exIdNo = wp.itemStr("ex_id_no");
		String exRtnStaus = wp.itemStr("ex_rtn_staus");
		String exAutopayAcctNo = wp.itemStr("ex_autopay_acct_no");
		String ss = "";

		if (empty(exDateS) == false || empty(exDateE) == false) {
			ss += " 處理日期 : ";
			if (empty(exDateS) == false) {
				ss += exDateS + " 起 ";
			}
			if (empty(exDateE) == false) {
				ss += " ~ " + exDateE + " 迄 ";
			}
		}
		if (!empty(exIdNo)) {
			ss += " ID : ";
			ss += exIdNo;
		}
		if (!empty(exRtnStaus)) {
			String[] cde1 = new String[] { "00", "03", "06", "07", "08", "09", "10", "11", "12", "13", "14", "16", "17"
					, "18", "19", "41", "42", "43", "44", "49", "91", "98"};
			String[] txt1 = new String[] { "00-成功", "03-已終止代繳", "06-凍結戶或警示戶",
					"07-業務支票專戶","08-帳號錯誤","09-終止戶","10-身分證號不符","11-轉出戶","12-拒絕往來戶","13-無此用戶編號",
					"14-用戶編號已存在","16-管制帳戶","17-掛失戶","18-異常交易帳戶","19-用戶編號非英數字元","41-(*)局帳號不符",
					"42-(*)戶名不符","43-(*)身分證號不符","44-(*)印鑑不符","49-(*)其他","91-規定期限內未有扣款","98-其他"};
			 ss += " 狀態 : "+ commStr.decode(exRtnStaus, cde1, txt1);
		}
		if (!empty(exAutopayAcctNo)) {
			ss += " 帳號 : ";
			ss += exAutopayAcctNo;
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

		wp.pageRows = 9999;

		queryFunc();
		// wp.setListCount(1);

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
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
			// 雙幣幣別
//			wp.optionKey = wp.colStr("ex_curr_code");
//			dddw_list("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

	String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
		if (acctkey.trim().length() == 8)
			rtn += "000";
		if (acctkey.trim().length() == 10)
			rtn += "0";

		return rtn;
	}
}
