
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-26  V1.00.01  Ryan       program initial                            *
* 110-02-25  V1.00.02  Andy       update :PDF隠碼作業==>本表不隠碼寫入log          * 
* 110-04-14  V1.00.03  Andy       update :Mantis6671                         *
* 111-10-20  v1.00.04  Zuwei Su   sync from mega, update coding standard                    *
******************************************************************************/

package actr01;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr1030 extends BaseReport {
	String m_progName = "actr1030";
	int ii = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		// ddd("action=" + strAction + ", level=" + wp.levelCode + ", resp=" +
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
			// strAction = "R";
			// dataRead();
			// } else if (eqIgno(wp.buttonCode, "A")) {
			// /* 新增功能 */
			// insertFunc();
			// } else if (eqIgno(wp.buttonCode, "U")) {
			// /* 更新功能 */
			// updateFunc();
			// } else if (eqIgno(wp.buttonCode, "D")) {
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
		} else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
			strAction = "XLS";
			xlsPrint();
		} else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
			// -check approve-
			if (!checkApprove(wp.itemStr("zz_apr_user"), wp.itemStr("zz_apr_passwd"))) {
				wp.respHtml = "TarokoErrorPDF";
				return;
			}
			strAction = "PDF";
			pdfPrint();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void dddwSelect() {

	}

	@Override
	public void initPage() {

	}

	private int getWhereStr() throws Exception {
		String exChgdateS = wp.itemStr("ex_chgdate_S");
		String exChgdateE = wp.itemStr("ex_chgdate_E");
		if (this.chkStrend(exChgdateS, exChgdateE) == false) {
			alertErr("[查詢日期-起迄]  輸入錯誤");
			return -1;
		}
		
		//固定條件
		String lsWhere = " where 1=1 ";

		if (empty(wp.itemStr("ex_card_indicator")) == false) {
			if(wp.itemStr("ex_card_indicator").equals("1")){
				if (empty(wp.itemStr("ex_acct_key")) == false) {
					String sql_select_1 = " select id_p_seqno from crd_idno where id_no = :ex_acct_key ";
					setString("ex_acct_key", wp.itemStr("ex_acct_key"));
					sqlSelect(sql_select_1);
 				  if(sqlRowNum <= 0){
					  alertErr("此條件查無資料");
					  return -1;
				  }				
					String id_p_seqno = sqlStr("id_p_seqno");
				//ls_where += " and id_p_seqno = :id_p_seqno ";
					lsWhere += " and corp_id_seqno = :id_p_seqno ";
					setString("id_p_seqno", id_p_seqno);
				}
			} else {
				if (empty(wp.itemStr("ex_acct_key")) == false) {
					String sql_select_2 = " select corp_p_seqno from crd_corp where corp_no = :ex_acct_key ";
					setString("ex_acct_key", wp.itemStr("ex_acct_key"));
					sqlSelect(sql_select_2);
 				  if(sqlRowNum <= 0){
					  alertErr("此條件查無資料");
					  return -1;
				  }				
					String corp_p_seqno = sqlStr("corp_p_seqno");
				//ls_where += " and corp_p_seqno = :corp_p_seqno ";
					lsWhere += " and corp_id_seqno = :corp_p_seqno ";
					setString("corp_p_seqno", corp_p_seqno);
				}
			}
		}
    // 執行sqlSelect(x)結束時，setString 裡設定的參數會清空
		if (empty(wp.itemStr("ex_card_indicator")) == false) {
			lsWhere += " and card_indicator = :ex_card_indicator ";
			setString("ex_card_indicator", wp.itemStr("ex_card_indicator"));
		}

		if (empty(exChgdateS) == false) {
			lsWhere += " and chg_cycle_date >= :ex_chgdate_S ";
			setString("ex_chgdate_S", exChgdateS);
		}		
	
		if (empty(exChgdateE) == false) {
			lsWhere += " and chg_cycle_date <= :ex_chgdate_E ";
			setString("ex_chgdate_E", exChgdateE);
		}		
	
		wp.whereStr = lsWhere;
		return 1;
	}

	public void queryFunc() throws Exception {
		// -page control-
		if (getWhereStr() != 1)
			return;

		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {

		wp.pageControl();

		if (getWhereStr() != 1)
			return;

		wp.sqlCmd = "select hex(rowid) as rowid "
				+ ", decode(card_indicator,'1',UF_IDNO_ID(id_p_seqno),corp_no) as acct_key "
				+ ", card_indicator "
				+ ", chg_cycle_date "
				+ ", corp_p_seqno "
				+ ", corp_no "
				+ ", corp_no_code "
				+ ", id_p_seqno "
				+ ", last_cycle_month "
				+ ", new_cycle_month "
				+ ", stmt_cycle "
				+ ", new_stmt_cycle "
				+ ", last_interest_date "
				+ ", crt_date "
				+ ", crt_user "
				+ ", apr_flag "
				+ ", apr_date "
				+ ", apr_user "
				+ ", batch_proc_mark "
				+ ", batch_proc_date "
				+ ", mod_user "
				+ ", mod_time "
				+ ", mod_pgm "
				+ ", mod_seqno "
				+ "from act_chg_cycle "
				+ wp.whereStr;


		if (strAction.equals("XLS")) {
			selectNoLimit();
		}

	  wp.pageCountSql = "select count(*) from (";
	  wp.pageCountSql += wp.sqlCmd;
	  wp.pageCountSql += ")";

	//wp.sqlCmd += " order by chg_cycle_date, acct_key "; 這裡的 acct_key 非 act_chg_cycle 的index key, 若資料範圍大時，會執行較久
		wp.sqlCmd += " order by chg_cycle_date ";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		// list_wkdata();
		wp.totalRows = wp.dataCnt;
		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	public void dataRead() throws Exception {

	}

	public void dataProcess() throws Exception {
		queryFunc();
		if (wp.selectCnt == 0) {
			alertErr("報表無資料可比對");
			return;
		}

	}

	void xlsPrint() throws Exception {
		try {
			log("xlsFunction: started--------");
			wp.reportId = m_progName;
			// -cond-
			// String ss = "執行年度: " + zzstr.ss_2ymd(wp.itemStr("ex_yy_s"))
			// + " -- " + zzstr.ss_2ymd(wp.itemStr("ex_yy_e"));
			// wp.colSet("cond_1", ss);
			/*
			 * String ss2 = "回報日期: " +
			 * zzStr.ss_2ymd(wp.itemStr("ex_send_date1")) + " -- " +
			 * zzStr.ss_2ymd(wp.itemStr("ex_send_date1")); wp.colSet("cond_2",
			 * ss2);
			 */
			wp.colSet("IdUser", wp.loginUser);
			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			// xlsx.report_id ="rskr0020";
			xlsx.excelTemplate = m_progName + ".xlsx";

			// ====================================
			// -明細-
			xlsx.sheetName[0] = "明細";

			queryFunc();
			wp.setListCount(1);
			log("Detl: rowcnt:" + wp.listCount[0]);
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
		// 寫入Log紀錄檔 20210225 add
		if (pdfLog() != 1) {
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		wp.reportId = m_progName;
		// -cond-

		String exCardIndicator = wp.itemStr("ex_card_indicator");
		String exAcctKey = wp.itemStr("ex_acct_key");
		String exChgdateS = wp.itemStr("ex_chgdate_S");
		String exChgdateE = wp.itemStr("ex_chgdate_E");

		String cond1 = "  身分證字號: " + exAcctKey + "  銀行代號: ";

		if (exCardIndicator.equals("1")) {
			cond1 += "個別卡";
		} else if (exCardIndicator.equals("2")) {
			cond1 += "商務卡";
		}

		String cond2 = "原始新增日期: " + exChgdateS + " ~ " + exChgdateE; 			 	  	

		wp.colSet("cond_1", cond1);
		wp.colSet("cond_2", cond2);
		wp.colSet("IdUser", wp.loginUser);
		wp.pageRows = 99999;

		//if (empty(wp.itemStr("ex_acct_key"))) {
		//	wp.respHtml = "TarokoErrorPDF";
		//	alertErr("請輸入身分證字號");
		//	return;
		//}

		queryFunc();
		// wp.setListCount(1);

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "N";
		pdf.excelTemplate = m_progName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	void listWkdata() {

	}

	int pdfLog() throws Exception {
		String strSql = "INSERT INTO LOG_ONLINE_APPROVE "
				+ "(program_id, file_name, crt_date, crt_user, apr_flag, apr_date, apr_user) "
				+ "values ('actr1030', 'actr1030.pdf', :crt_date, :crt_user, 'Y', :apr_date, :apr_user )";
		setString("crt_date", wp.sysDate+wp.sysTime);
		setString("crt_user", wp.loginUser);
		setString("apr_date", wp.sysDate);
		setString("apr_user", wp.itemStr("zz_apr_user"));

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			alertErr("Log紀錄檔寫入失敗!");
		}
		return 1;
	}
}
