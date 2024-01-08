/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR     DESCRIPTION                                *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-21  V1.00.00  Max Lin    program initial                            *
* 111-10-20  v1.00.01  Zuwei Su   sync from mega, update coding standard                    *
*                                                                            *
******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0410 extends BaseReport {

	InputStream inExcelFile = null;
	String mProgName = "actr0410";

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
		String exId = wp.itemStr("ex_id");
		String exModUser = wp.itemStr("ex_mod_user");
		String exModtype = wp.itemStr("ex_mod_type");
		String exEmailMark = wp.itemStr("ex_email_mark");

		if (empty(exDateS) == true && empty(exDateE) == true) {
			alertErr("請輸入登錄日期");
			return false;
		}
	
		//固定條件
		String lsWhere = " where 1=1 ";

		if (empty(exDateS) == false){
			lsWhere += " and ASTL.mod_date >= :ex_date_S ";
			setString("ex_date_S", exDateS);
		}		
		
		if (empty(exDateE) == false){
			lsWhere += " and ASTL.mod_date <= :ex_date_E ";
			setString("ex_date_E", exDateE);
		}	
		
		if (empty(exId) == false){
			lsWhere += " and CI.id_no = :ex_id ";
			setString("ex_id", exId);
		}
		
		if (empty(exModUser) == false){
			lsWhere += " and ASTL.mod_user = :ex_mod_user ";
			setString("ex_mod_user", exModUser);
		}
		
		if (!exModtype.equals("0")){
			lsWhere += " and ASTL.mod_type = :ex_mod_type ";
			setString("ex_mod_type", exModtype);
		}
		
		if (!exEmailMark.equals("0")){
			lsWhere += " and ASTL.e_mail_from_mark = :ex_email_mark ";
			setString("ex_email_mark", exEmailMark);
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
		
		wp.sqlCmd = "select ASTL.mod_date, "
				+ "(case when ASTL.mod_type = '1' then '寄送方式' when ASTL.mod_type = '2' then 'E-mail' else '' end) as mod_type, "
				+ "ASTL.acct_type, "
				+ "(case when ASTL.stat_send_type = '1' then 'Paper' when ASTL.stat_send_type = '2' then 'Internet' else '' end) as stat_send_type, "
				+ "substring(ASTL.stat_send_month_s, 1, 6) concat '--' concat substring(ASTL.stat_send_month_e, 1, 6) as stat_mm, "
				+ "ASTL.e_mail_addr, "
//				+ "(case when ASTL.e_mail_from_mark = '1' then 'IBM' when ASTL.e_mail_from_mark = 'M' then 'ECS' "
//				+ "		when ASTL.e_mail_from_mark = 'W' then 'Web' when ASTL.e_mail_from_mark = 'A' then 'ARS' "
//				+ "		else '' end) as e_mail_from_mark, "
				+" ASTL.e_mail_from_mark, "
				+ "ASTL.mod_user, "
				+ "varchar_format(ASTL.mod_time, 'HH24:MI:SS') as mod_time, "
				+ "CI.id_no, "
				+ "CI.chi_name "
				+ "from act_stat_type_log as ASTL "
				+ "left join crd_idno as CI on ASTL.id_p_seqno = CI.id_p_seqno "
				+ wp.whereStr
				+ " order by ASTL.mod_date, ASTL.mod_type, ASTL.acct_type ";

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
		wp.setPageValue();
		listWkdata();
	}
	
	void listWkdata() throws Exception{
		int rowCt = 0;
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			//計算欄位
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
			String exId = wp.itemStr("ex_id");
			String exModUser = wp.itemStr("ex_mod_user");
			String exModType = wp.itemStr("ex_mod_type");
			String exEmailMark = wp.itemStr("ex_email_mark");

			String cond1 = "登錄日期: " + exDateS + " ~ " + exDateE + "  身分證號: " + exId; 			 	  	
			String cond2 = "登錄人員: " + exModUser + "  異動類別: " + exModType + "  Email 異動管道: " + exEmailMark;		 	  	
			wp.colSet("cond_1", cond1);
			wp.colSet("cond_2", cond2);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			xlsx.excelTemplate = mProgName + ".xlsx";

			//====================================
			xlsx.sheetName[0] ="對帳單寄送方式&E-mail異動明細表";
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
		wp.reportId = mProgName;
		// -cond-
		String ss = "PDFTEST: ";
		wp.colSet("cond_1", ss);
		wp.pageRows = 9999;

		queryFunc();
		// wp.setListCount(1);

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "N";
		pdf.excelTemplate = mProgName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 30;
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
			// 登錄人員
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_mod_user");
			dddwList("dddw_mod_user", "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id");
		} catch (Exception ex) {
		}
	}

}

