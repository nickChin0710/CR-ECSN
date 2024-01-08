/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-01-05  V1.00.01  machao     新增功能：各營業單位招攬信用卡月報表       
* 111-05-12  V1.00.02  machao     mktr2110需調整                     *                         *
******************************************************************************/

package mktr01;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoJasperUtils;

public class Mktr2110 extends BaseAction implements InfaceExcel {
	private String PROGNAME = "各營業單位招攬信用卡月報表 111/01/05 V1.00.01";
	  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	  busi.ecs.CommRoutine comr = null;
	  String dataKK1;
	  int qFrom = 0;
	  String tranSeqStr = "";
	  String msg = "";
	  String batchNo = "";
	  int errorCnt = 0, recCnt = 0, notifyCnt = 0;
	  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	  String[] uploadFileCol = new String[50];
	  String[] uploadFileDat = new String[50];
	  String[] logMsg = new String[20];
	  StringBuffer caseWhen = new StringBuffer();
	  StringBuffer caseWhen2 = new StringBuffer();
 	
	@Override
	public void userAction() throws Exception {
		rc = 1;

	    strAction = wp.buttonCode;
	    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
	      strAction = "new";
	      clearFunc();
	    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
	      strAction = "Q";
	      queryFunc();
	    }else if (eqIgno(wp.buttonCode, "C")) {/* 資料處理- */
		  strAction = "C";
		  dataProcess();
		}else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
	      strAction = "R";
	      dataRead();
	    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
	      queryRead();
	    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
	      querySelect();
	    } else if (eqIgno(wp.buttonCode, "XLS")) {/* Excek- */
	      strAction = "XLS";
	      xlsPrint();
	    } else if (eqIgno(wp.buttonCode, "PDF")) { // 導出PDF報表
	      strAction = "PDF";
	      pdfPrint();
	    } else if (eqIgno(wp.buttonCode, "PDFDETL")) { // 導出PDF報表
		      strAction = "PDFDETL";
		      pdfPrintDetl();
		}else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
	      strAction = "";
	      clearFunc();
	    }

	    dddwSelect();
	    initButton();
		}
	@Override
	public void dddwSelect() {
		try {
//			dddwList("dddw_acct_type2", "ptr_acct_type", "acct_type", "chin_name",
//					"where 1=1 fetch first 1 rows only ");
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_acct_type");
			dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name",
					"where 1=1 order by acct_type ");
			wp.optionKey = itemkk("ex_branch_s");
			dddwList("dddw_branch_s", "gen_brn", "branch", "brief_chi_name",
					"where 1=1 order by branch ");
			wp.optionKey = itemkk("ex_branch_e");
			dddwList("dddw_branch_e", "gen_brn", "branch", "brief_chi_name",
					"where 1=1 order by branch ");

		} catch (Exception ex) {
		}
		
	}

	@Override
	public void queryFunc() throws Exception {
		String lsDate1 = wp.itemStr("ex_branch_s");
		String lsDate2 = wp.itemStr("ex_branch_e");

		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr2("[分行別-起迄]  輸入錯誤");
			return;
		}
		// -page control-
		// wp.queryWhere = wp.whereStr;
		// wp.setQueryMode();

		queryRead();		
	}

	@Override
	public void queryRead() throws Exception {
		String ex_acct_month = wp.itemStr("ex_acct_month");
		String ex_acct_month1 = of_relative_12(ex_acct_month, -11);
		String ex_acct_month2 = ex_acct_month;
		String ex_acct_type = wp.itemStr("ex_acct_type");
		String ex_branch_s = wp.itemStr("ex_branch_s");
		String ex_branch_e = wp.itemStr("ex_branch_e");
		String ex_introduce_emp_no = wp.itemStr("ex_introduce_emp_no");
		wp.pageControl();
		getWhereStr();
		//
		wp.sqlCmd = "select t.*, "
				+ "(t.common_fees + t.ca_fees) sum_fees, "
				+ "t.year_fees as sum_year_fees "
				+ "from ( "
				+ "SELECT "
//				+ "a.acct_type ,"
				+ " sum (a.m_act_card_cnt) AS h_act_card_cnt "
				+ " ,sum (a.m_noact_card_cnt) AS h_noact_card_cnt "
				+ " ,sum (a.m_stop_card_cnt) AS h_stop_card_cnt "
				+ " ,sum (a.m_sum_cnt) AS h_sum_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_act_card_cnt else 0 end) as m_act_card_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_noact_card_cnt else 0 end) as m_noact_card_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_stop_card_cnt else 0 end) as m_stop_card_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_sum_cnt else 0 end) as m_sum_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_act_card_cnt else 0 end) as y_act_card_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_noact_card_cnt else 0 end) as y_noact_card_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_stop_card_cnt else 0 end) as y_stop_card_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_sum_cnt else 0 end) as y_sum_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.common_fees else 0 end) as common_fees "
				+ " ,sum( " + caseWhen.toString() + " then a.ca_fees else 0 end) as ca_fees "
				+ " ,sum( " + caseWhen.toString() + " then a.year_fees else 0 end) as year_fees "
//				+ " ,nvl(a.branch || '_' || b.brief_chi_name,a.branch) as tt_branch "
				+ " ,a.branch "
//				+ " ,a.introduce_emp_no "
				+ " FROM mkt_mcard_static AS a LEFT JOIN gen_brn AS b ON a.branch = b.branch "
				+ " WHERE 1 = 1 ";
		wp.sqlCmd += sqlStrend(ex_acct_month1, ex_acct_month2, "a.acct_month");
		wp.sqlCmd += sqlStrend(ex_branch_s, ex_branch_e, "a.branch");
		wp.sqlCmd += sqlCol(ex_acct_type, "a.acct_type");
		wp.sqlCmd += sqlCol(ex_introduce_emp_no, "a.introduce_emp_no");
		wp.sqlCmd += "GROUP BY a.branch "
//				+ ", b.brief_chi_name, a.acct_type, a.introduce_emp_no "
				+ "ORDER BY a.branch ) t ";		// a.acct_type,

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			msg += "明細表,";
			return;
		}
		for(int ii=0;ii<wp.selectCnt;ii++) {
			String tt_branch = SelectGenBrn(wp.getValue("branch",ii));
			wp.colSet(ii,"tt_branch", tt_branch);
		}
		
		list_wkdata();
		wp.setPageValue();
	}
	
	 String SelectGenBrn(String branch) {
		String sql = "select nvl(branch || '_' || brief_chi_name,branch) as tt_branch from gen_brn"
				+ " where branch = ? ";
		sqlSelect(sql, new Object[]{branch});
		return sqlStr("tt_branch");
	}
	void list_wkdata() throws ParseException {
		String ss = "", ss2 = "";
		double sum_m_act_card_cnt = 0, sum_m_noact_card_cnt = 0, sum_m_stop_card_cnt = 0,
				sum_m_sum_cnt = 0, sum_y_act_card_cnt = 0, sum_y_noact_card_cnt = 0, sum_y_stop_card_cnt = 0, sum_y_sum_cnt = 0,
				sum_h_act_card_cnt = 0, sum_h_noact_card_cnt = 0, sum_h_stop_card_cnt = 0, sum_h_sum_cnt = 0,
				sum_fees = 0;
		String wk_branch = "", wk_acct_type = "", wk_introduce_emp_no = "";
		String ls_sql = "";
		String ex_acct_month = wp.itemStr("ex_acct_month");
		String ex_acct_month1 = of_relative_12(ex_acct_month, -11);
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// wk_acct_type = wp.col_ss(ii,daoTid+"acct_type");
			// wk_branch = wp.col_ss(ii,daoTid+"branch");
			// wk_introduce_emp_no = wp.col_ss(ii,daoTid+"introduce_emp_no");
			// ls_sql = "select sum(common_fees+ca_fees) sum_year "
			// + "from mkt_mcard_static "
			// + "where 1=1 ";
			// ls_sql += sql_col(wk_acct_type,"acct_type");
			// ls_sql += sql_strend(ex_acct_month,ex_acct_month1,"acct_month");
			// ls_sql += sql_col(wk_branch,"branch");
			// ls_sql += sql_col(wk_introduce_emp_no,"introduce_emp_no");
			// sqlSelect(ls_sql);
			// wp.col_set(ii,daoTid+"sum_year", sql_ss("sum_year"));

			ss = wp.colStr(ii, daoTid + "branch");
			// sum_fees =
			// (wp.col_num(ii,daoTid+"common_fees")+wp.col_num(ii,daoTid+"ca_fees"));
			// wp.col_set(ii,daoTid+"sum_fees", sum_fees);

			sum_m_act_card_cnt += wp.colNum(ii, daoTid + "m_act_card_cnt");
			sum_m_noact_card_cnt += wp.colNum(ii, daoTid + "m_noact_card_cnt");
			sum_m_stop_card_cnt += wp.colNum(ii, daoTid + "m_stop_card_cnt");
			sum_m_sum_cnt += wp.colNum(ii, daoTid + "m_sum_cnt");
			sum_y_act_card_cnt += wp.colNum(ii, daoTid + "y_act_card_cnt");
			sum_y_noact_card_cnt += wp.colNum(ii, daoTid + "y_noact_card_cnt");
			sum_y_stop_card_cnt += wp.colNum(ii, daoTid + "y_stop_card_cnt");
			sum_y_sum_cnt += wp.colNum(ii, daoTid + "y_sum_cnt");
			sum_h_act_card_cnt += wp.colNum(ii, daoTid + "h_act_card_cnt");
			sum_h_noact_card_cnt += wp.colNum(ii, daoTid + "h_noact_card_cnt");
			sum_h_stop_card_cnt += wp.colNum(ii, daoTid + "h_stop_card_cnt");
			sum_h_sum_cnt += wp.colNum(ii, daoTid + "h_sum_cnt");

			String colspan = "";
			if (daoTid.equals("a-"))
				colspan = "2";
			if (daoTid.equals("b-"))
				colspan = "3";

			ss = wp.colStr(ii, daoTid + "acct_type");
			ss2 = wp.colStr(ii + 1, daoTid + "acct_type");
			if (!ss.equals(ss2)) {
				wp.colSet(ii, daoTid + "tr", "<tr>"
						+ "<td nowrap align='right' colspan='" + colspan + "'>帳戶類別(" + ss + ")" + "&nbsp;&nbsp;小計:</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_m_act_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_m_noact_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_m_stop_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_m_sum_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_y_act_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_y_noact_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_y_stop_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_y_sum_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_h_act_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_h_noact_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_h_stop_card_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_h_sum_cnt) + "</span>"
						+ "</td>");
				wp.colSet(daoTid + "sum_m_act_card_cnt", sum_m_act_card_cnt);
				wp.colSet(daoTid + "sum_m_noact_card_cnt", sum_m_noact_card_cnt);
				wp.colSet(daoTid + "sum_m_stop_card_cnt", sum_m_stop_card_cnt);
				wp.colSet(daoTid + "sum_m_sum_cnt", sum_m_sum_cnt);
				wp.colSet(daoTid + "sum_y_act_card_cnt", sum_y_act_card_cnt);
				wp.colSet(daoTid + "sum_y_noact_card_cnt", sum_y_noact_card_cnt);
				wp.colSet(daoTid + "sum_y_stop_card_cnt", sum_y_stop_card_cnt);
				wp.colSet(daoTid + "sum_y_sum_cnt", sum_y_sum_cnt);
				wp.colSet(daoTid + "sum_h_act_card_cnt", sum_h_act_card_cnt);
				wp.colSet(daoTid + "sum_h_noact_card_cnt", sum_h_noact_card_cnt);
				wp.colSet(daoTid + "sum_h_stop_card_cnt", sum_h_stop_card_cnt);
				wp.colSet(daoTid + "sum_h_sum_cnt", sum_h_sum_cnt);
				sum_m_act_card_cnt = 0;
				sum_m_noact_card_cnt = 0;
				sum_m_stop_card_cnt = 0;
				sum_m_sum_cnt = 0;
				sum_y_act_card_cnt = 0;
				sum_y_noact_card_cnt = 0;
				sum_y_stop_card_cnt = 0;
				sum_y_sum_cnt = 0;
				sum_h_act_card_cnt = 0;
				sum_h_noact_card_cnt = 0;
				sum_h_stop_card_cnt = 0;
				sum_h_sum_cnt = 0;
				sum_fees = 0;
			}
		}	
	}
	
	void list_wkdata2() throws Exception {
		String ss = "", ss2 = "";
		long sum_m_sum_cnt = 0, sum_y_sum_cnt = 0;
		daoTid = "b-";

		for (int ii = 0; ii < wp.selectCnt; ii++) {

			sum_m_sum_cnt += wp.colNum(ii, daoTid + "m_sum_cnt");
			sum_y_sum_cnt += wp.colNum(ii, daoTid + "y_sum_cnt");

			ss = wp.colStr(ii, daoTid + "acct_type");
			ss2 = wp.colStr(ii + 1, daoTid + "acct_type");
			if (!ss.equals(ss2)) {
				wp.colSet(ii, "tr2", "<tr>"
						+ "<td colspan='4' align='right'>&nbsp;合計:</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_m_sum_cnt) + "</span>"
						+ "</td>"
						+ "<td nowrap align='right'>&nbsp;"
						+ "<span class='dsp_number'>" + amtFormat(sum_y_sum_cnt) + "</span>"
						+ "</td>");
				wp.colSet(daoTid + "sum_m_sum_cnt", sum_m_sum_cnt);
				wp.colSet(daoTid + "sum_y_sum_cnt", sum_y_sum_cnt);
				sum_m_sum_cnt = 0;
				sum_y_sum_cnt = 0;
			}
		}
	}

	void getWhereStr() throws ParseException {
		if (caseWhen.length() > 0)
			caseWhen.delete(1, caseWhen.length());
		if (caseWhen2.length() > 0)
			caseWhen2.delete(1, caseWhen2.length());
		wp.whereStr = " where 1=1 ";
		if (!empty(wp.itemStr("ex_acct_month"))) {
			// String ex_acct_month =
			// of_relative_12(wp.item_ss("ex_acct_month"),-1); //phopho mod
			// 2020.6.17
			// String ex_acct_month1 = of_relative_12(ex_acct_month,-12);
			String ex_acct_month = wp.itemStr("ex_acct_month");
			String ex_acct_month1 = of_relative_12(ex_acct_month, -11);
			String ex_acct_month2 = ex_acct_month;

			caseWhen.append(" case when ");
			caseWhen.append(" a.acct_month = '");
			caseWhen.append(ex_acct_month);
			caseWhen.append("' ");

			int m = toInt(strMid(ex_acct_month, 4, 2));
			of_relative_yymm(ex_acct_month, m);

			wp.whereStr += " and a.acct_month >= :ex_acct_month1 ";
			setString("ex_acct_month1", ex_acct_month1);

			wp.whereStr += " and a.acct_month <= :ex_acct_month2 ";
			setString("ex_acct_month2", ex_acct_month2);

			// wp.col_set("acct_month_m1", ex_acct_month);
			// wp.col_set("acct_month_y1", ex_acct_month1);
			// wp.col_set("acct_month_y2", of_relative_12(ex_acct_month1,m));
			// wp.col_set("acct_month_h1", ex_acct_month1);
			// wp.col_set("acct_month_h2", ex_acct_month2);
		}
		if (!empty(wp.itemStr("ex_acct_type"))) {
			wp.whereStr += " and a.acct_type = :ex_acct_type ";
			setString("ex_acct_type", wp.itemStr("ex_acct_type"));
		}
		if (!empty(wp.itemStr("ex_branch_s"))) {
			wp.whereStr += " and a.branch >= :ex_branch_s ";
			setString("ex_branch_s", wp.itemStr("ex_branch_s"));
		}
		if (!empty(wp.itemStr("ex_branch_e"))) {
			wp.whereStr += " and a.branch <= :ex_branch_e ";
			setString("ex_branch_e", wp.itemStr("ex_branch_e"));
		}
		
	}
	
	// 千分位轉換
	String amtFormat(double amt) {
		DecimalFormat df = new DecimalFormat("###,##0");
		return df.format(amt);
	}
	
	String of_relative_12(String ym, int m) throws ParseException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		Date date = format.parse(ym);
		cal.setTime(date);
		cal.add(Calendar.MARCH, m);
		return format.format(cal.getTime());
	}
	
	void of_relative_yymm(String ym, int m) throws ParseException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		caseWhen2.append(" case when ");
		for (int i = 0; i < m; i++) {
			caseWhen2.append("a.acct_month = '");
			caseWhen2.append(ym);
			caseWhen2.append("'");
			Date date = format.parse(ym);
			cal.setTime(date);
			cal.add(Calendar.MARCH, -1);
			ym = format.format(cal.getTime());
			if ((i + 1) == m)
				continue;
			caseWhen2.append(" or ");
		}		
	}
	
	public void dataProcess() throws Exception {
		queryFunc();
		if (wp.selectCnt == 0) {
			alertErr("報表無資料可比對");
			return;
		}

	}
	
	@Override
	public void querySelect() throws Exception {
		dataRead();
		
	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub
		String ex_acct_month = wp.itemStr("ex_acct_month");
		String ex_acct_month1 = of_relative_12(ex_acct_month, -11);
		String ex_acct_month2 = ex_acct_month;
		String ex_acct_type = wp.itemStr("ex_acct_type");
		String ex_branch_s[] = wp.itemStr("data_k2").split("_");
		String branch_s = ex_branch_s[0];
		String ex_branch_e = wp.itemStr("ex_branch_e");
		String ex_introduce_emp_no = wp.itemStr("ex_introduce_emp_no");
		wp.pageControl();
		getWhereStr();
		//
		wp.sqlCmd = "select t.*, "
				+ "(t.common_fees + t.ca_fees) sum_fees, "
				+ "t.year_fees as sum_year_fees "
				+ "from ( "
				+ "SELECT "
				+ "a.acct_type "
				+ " ,sum (a.m_act_card_cnt) AS h_act_card_cnt "
				+ " ,sum (a.m_noact_card_cnt) AS h_noact_card_cnt "
				+ " ,sum (a.m_stop_card_cnt) AS h_stop_card_cnt "
				+ " ,sum (a.m_sum_cnt) AS h_sum_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_act_card_cnt else 0 end) as m_act_card_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_noact_card_cnt else 0 end) as m_noact_card_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_stop_card_cnt else 0 end) as m_stop_card_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.m_sum_cnt else 0 end) as m_sum_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_act_card_cnt else 0 end) as y_act_card_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_noact_card_cnt else 0 end) as y_noact_card_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_stop_card_cnt else 0 end) as y_stop_card_cnt "
				+ " ,sum( " + caseWhen2.toString() + " then a.m_sum_cnt else 0 end) as y_sum_cnt "
				+ " ,sum( " + caseWhen.toString() + " then a.common_fees else 0 end) as common_fees "
				+ " ,sum( " + caseWhen.toString() + " then a.ca_fees else 0 end) as ca_fees "
				+ " ,sum( " + caseWhen.toString() + " then a.year_fees else 0 end) as year_fees "
				+ " ,nvl(a.branch || '_' || b.brief_chi_name,a.branch) as tt_branch "
				+ " ,a.branch "
				+ " ,a.introduce_emp_no "
				+ " FROM mkt_mcard_static AS a LEFT JOIN gen_brn AS b ON a.branch = b.branch "
				+ " WHERE 1 = 1 ";
		wp.sqlCmd += sqlStrend(ex_acct_month1, ex_acct_month2, "a.acct_month");
		wp.sqlCmd += sqlStrend(branch_s, branch_s, "a.branch");
		wp.sqlCmd += sqlCol(ex_acct_type, "a.acct_type");
		wp.sqlCmd += sqlCol(ex_introduce_emp_no, "a.introduce_emp_no");
		wp.sqlCmd += "GROUP BY a.branch, b.brief_chi_name, a.acct_type, a.introduce_emp_no "
				+ "ORDER BY a.acct_type, a.branch ) t ";		// 

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			msg += "明細表,";
			return;
		}
		wp.colSet("ex_acct_month", wp.itemStr("ex_acct_month"));
		String acctType = SelectptrAcctType(wp.itemStr("ex_acct_type"));
		wp.colSet("ex_acct_type", acctType);
		wp.colSet("ex_branch_s", branch_s);
		wp.colSet("ex_branch_e", branch_s);
		wp.colSet("ex_introduce_emp_no", wp.itemStr("ex_introduce_emp_no"));
		
		list_wkdata();
		wp.setPageValue();
	}

	 String SelectptrAcctType(String acctType) {
		// TODO Auto-generated method stub
		 String sql = "select nvl(acct_type || '_' || chin_name,acct_type) as acct_type from ptr_acct_type"
					+ " where acct_type = ? ";
			sqlSelect(sql, new Object[]{acctType});
			return sqlStr("acct_type");
	}
	@Override
	public void saveFunc() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub
		
	}
//	void pdfPrint() throws Exception {
//		/*
//		 * if(wp.item_ss("data_k1").equals("a")){ pdfPrint_a(); }
//		 * if(wp.item_ss("data_k1").equals("b")){ pdfPrint_b(); }
//		 */
//	}

	// 打印PDF
	  public void pdfPrint() throws Exception {
		  try {
		      String exDate1 = wp.itemStr("ex_acct_month"); // 查詢年月
		      String accttype = wp.itemStr("ex_acct_type"); // 賬戶類別

		      String cond1 = "" +
		              "查詢年月: " + dateFormatTo(exDate1, "yyyyMM", "yyyy/MM");

		      // 設定報表頭信息
		      HashMap<String, Object> params = new HashMap<String, Object>();
		      params.put("title", "各營業單位招攬信用卡月報表");
		      params.put("report", "Report: mktr2110");
		      params.put("user", "User: " + wp.loginUser);
		      params.put("date", "Date: " + new SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date()));
		      params.put("time", "Time: " + new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
		      params.put("cond1", cond1);
		      params.put("text", "新增卡數指首次發卡(非補/換/續卡)");

		      // sql 執行
		      ArrayList<Object> pa = new ArrayList<Object>();
		      getWhereStr();
	       	 String ex_acct_month = wp.itemStr("ex_acct_month");
	       	 String ex_acct_month1 = of_relative_12(ex_acct_month, -11);
			 String ex_acct_month2 = ex_acct_month;
			 String ex_acct_type = wp.itemStr("ex_acct_type");
			 String ex_branch_s = wp.itemStr("ex_branch_s");
			 String ex_branch_e = wp.itemStr("ex_branch_e");
			 String ex_introduce_emp_no = wp.itemStr("ex_introduce_emp_no");
			 String sqlStr =  "select t.*, "
						+ "(t.common_fees + t.ca_fees) as sum_fees, "
						+ "t.year_fees as sum_year_fees "
						+ "from ( "
						+ "SELECT "
						+ " sum (a.m_act_card_cnt) AS h_act_card_cnt "
						+ " ,sum (a.m_noact_card_cnt) AS h_noact_card_cnt "
						+ " ,sum (a.m_stop_card_cnt) AS h_stop_card_cnt "
						+ " ,sum (a.m_sum_cnt) AS h_sum_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_act_card_cnt else 0 end) as m_act_card_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_noact_card_cnt else 0 end) as m_noact_card_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_stop_card_cnt else 0 end) as m_stop_card_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_sum_cnt else 0 end) as m_sum_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_act_card_cnt else 0 end) as y_act_card_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_noact_card_cnt else 0 end) as y_noact_card_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_stop_card_cnt else 0 end) as y_stop_card_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_sum_cnt else 0 end) as y_sum_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.common_fees else 0 end) as common_fees "
						+ " ,sum( " + caseWhen.toString() + " then a.ca_fees else 0 end) as ca_fees "
						+ " ,sum( " + caseWhen.toString() + " then a.year_fees else 0 end) as year_fees "
						+ " ,a.branch "
						+ " FROM mkt_mcard_static AS a LEFT JOIN gen_brn AS b ON a.branch = b.branch "
						+ " WHERE 1 = 1 ";
			 sqlStr += sqlStrend(ex_acct_month1, ex_acct_month2, "a.acct_month");
			 sqlStr += sqlCol(ex_acct_type, "a.acct_type");
			    pa.add(ex_acct_month1);
			    pa.add(ex_acct_month2);
			    pa.add(ex_acct_type);
			    if (!isEmpty(ex_branch_s) || !isEmpty(ex_branch_e)) {
			    	sqlStr += sqlStrend(ex_branch_s, ex_branch_e, "a.branch");
				    pa.add(ex_branch_s);
				    pa.add(ex_branch_e);
			    	}
			    if (!isEmpty(ex_introduce_emp_no)) {
			    	sqlStr += sqlCol(ex_introduce_emp_no, "a.introduce_emp_no");
				    pa.add(ex_introduce_emp_no);
			    }
			sqlStr += "GROUP BY a.branch "
						+ "ORDER BY branch ) t ";
			sqlSelect(sqlStr, pa.toArray(new Object[pa.size()]));

		      ArrayList<HashMap> list = new ArrayList<HashMap>();
		      // sql查詢結果獲取
		      int sun = sqlRowNum;
		      for (int i = 0; i < sun; i++) {
		        HashMap<String, String> item = new HashMap<String, String>();
		        String tt_branch = SelectGenBrn(sqlStr(i, "branch"));
		        item.put("tt_branch", tt_branch );
		        item.put("introduce_emp_no",  sqlStr(i, "introduce_emp_no"));
		        item.put("m_act_card_cnt",  sqlStr(i, "m_act_card_cnt"));
		        item.put("m_noact_card_cnt",  sqlStr(i, "m_noact_card_cnt"));
		        item.put("m_stop_card_cnt",  sqlStr(i, "m_stop_card_cnt"));
		        item.put("m_sum_cnt",  sqlStr(i, "m_sum_cnt"));
		        item.put("y_act_card_cnt",  sqlStr(i, "y_act_card_cnt"));
		        item.put("y_noact_card_cnt",  sqlStr(i, "y_noact_card_cnt"));
		        item.put("y_stop_card_cnt",  sqlStr(i, "y_stop_card_cnt"));
		        item.put("y_sum_cnt",  sqlStr(i, "y_sum_cnt"));
		        item.put("h_act_card_cnt",  sqlStr(i, "h_act_card_cnt"));
		        item.put("h_noact_card_cnt",  sqlStr(i, "h_noact_card_cnt"));
		        item.put("h_stop_card_cnt",  sqlStr(i, "h_stop_card_cnt"));
		        item.put("h_sum_cnt",  sqlStr(i, "h_sum_cnt"));
		        item.put("sum_fees",  sqlStr(i, "sum_fees"));
		        item.put("sum_year_fees",  sqlStr(i, "sum_year_fees"));
		        list.add(item);
		      }

		      TarokoJasperUtils.exportPdf(wp, "mktr2110", "mktr2110", params, list);

		    } catch(Exception e) {
		      e.printStackTrace();
		    }	    
	  }
	
	  public void pdfPrintDetl() throws Exception {
		  try {
		      String exDate1 = wp.itemStr("ex_acct_month"); // 查詢年月
		      String accttype = wp.itemStr("ex_acct_type"); // 賬戶類別

		      String cond1 = "" +
		              "查詢年月: " + dateFormatTo(exDate1, "yyyyMM", "yyyy/MM");

		      // 設定報表頭信息
		      HashMap<String, Object> params = new HashMap<String, Object>();
		      params.put("title", "各營業單位招攬信用卡月報表");
		      params.put("report", "Report: mktr2110");
		      params.put("user", "User: " + wp.loginUser);
		      params.put("date", "Date: " + new SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date()));
		      params.put("time", "Time: " + new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
		      params.put("cond1", cond1);
		      params.put("text", "新增卡數指首次發卡(非補/換/續卡)");

		      // sql 執行
		      ArrayList<Object> pa = new ArrayList<Object>();
		      getWhereStr();
	       	 String ex_acct_month = wp.itemStr("ex_acct_month");
	       	 String ex_acct_month1 = of_relative_12(ex_acct_month, -11);
			 String ex_acct_month2 = ex_acct_month;
			 String acct_type[] = wp.itemStr("ex_acct_type").split("_");
			 String ex_acct_type = acct_type[0];
			 String ex_branch_s = wp.itemStr("ex_branch_s");
			 String ex_branch_e = wp.itemStr("ex_branch_e");
			 String ex_introduce_emp_no = wp.itemStr("ex_introduce_emp_no");
			 String sqlStr =  "select t.*, "
						+ "(t.common_fees + t.ca_fees) as sum_fees, "
						+ "t.year_fees as sum_year_fees "
						+ "from ( "
						+ "SELECT "
						+ "a.acct_type "
						+ " ,sum (a.m_act_card_cnt) AS h_act_card_cnt "
						+ " ,sum (a.m_noact_card_cnt) AS h_noact_card_cnt "
						+ " ,sum (a.m_stop_card_cnt) AS h_stop_card_cnt "
						+ " ,sum (a.m_sum_cnt) AS h_sum_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_act_card_cnt else 0 end) as m_act_card_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_noact_card_cnt else 0 end) as m_noact_card_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_stop_card_cnt else 0 end) as m_stop_card_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.m_sum_cnt else 0 end) as m_sum_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_act_card_cnt else 0 end) as y_act_card_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_noact_card_cnt else 0 end) as y_noact_card_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_stop_card_cnt else 0 end) as y_stop_card_cnt "
						+ " ,sum( " + caseWhen2.toString() + " then a.m_sum_cnt else 0 end) as y_sum_cnt "
						+ " ,sum( " + caseWhen.toString() + " then a.common_fees else 0 end) as common_fees "
						+ " ,sum( " + caseWhen.toString() + " then a.ca_fees else 0 end) as ca_fees "
						+ " ,sum( " + caseWhen.toString() + " then a.year_fees else 0 end) as year_fees "
						+ " ,nvl(a.branch || '_' || b.brief_chi_name,a.branch) as tt_branch "
						+ " ,a.branch "
						+ " ,a.introduce_emp_no "
						+ " FROM mkt_mcard_static AS a LEFT JOIN gen_brn AS b ON a.branch = b.branch "
						+ " WHERE 1 = 1 ";
				 sqlStr += sqlStrend(ex_acct_month1, ex_acct_month2, "a.acct_month");
				 sqlStr += sqlCol(ex_acct_type, "a.acct_type");
				    pa.add(ex_acct_month1);
				    pa.add(ex_acct_month2);
				    pa.add(ex_acct_type);
				    if (!isEmpty(ex_branch_s) || !isEmpty(ex_branch_e)) {
				    	sqlStr += sqlStrend(ex_branch_s, ex_branch_e, "a.branch");
					    pa.add(ex_branch_s);
					    pa.add(ex_branch_e);
				    	}
				    if (!isEmpty(ex_introduce_emp_no)) {
				    	sqlStr += sqlCol(ex_introduce_emp_no, "a.introduce_emp_no");
					    pa.add(ex_introduce_emp_no);
				    }
			    sqlStr += "GROUP BY a.branch, b.brief_chi_name, a.acct_type, a.introduce_emp_no "
						+ "ORDER BY a.acct_type, a.branch ) t ";
			sqlSelect(sqlStr, pa.toArray(new Object[pa.size()]));

		      ArrayList<HashMap> list = new ArrayList<HashMap>();
		      // sql查詢結果獲取
		      int sun = sqlRowNum;
		      for (int i = 0; i < sun; i++) {
		        HashMap<String, String> item = new HashMap<String, String>();
		        item.put("tt_branch", sqlStr(i, "tt_branch") );
		        item.put("introduce_emp_no",  sqlStr(i, "introduce_emp_no"));
		        item.put("m_act_card_cnt",  sqlStr(i, "m_act_card_cnt"));
		        item.put("m_noact_card_cnt",  sqlStr(i, "m_noact_card_cnt"));
		        item.put("m_stop_card_cnt",  sqlStr(i, "m_stop_card_cnt"));
		        item.put("m_sum_cnt",  sqlStr(i, "m_sum_cnt"));
		        item.put("y_act_card_cnt",  sqlStr(i, "y_act_card_cnt"));
		        item.put("y_noact_card_cnt",  sqlStr(i, "y_noact_card_cnt"));
		        item.put("y_stop_card_cnt",  sqlStr(i, "y_stop_card_cnt"));
		        item.put("y_sum_cnt",  sqlStr(i, "y_sum_cnt"));
		        item.put("h_act_card_cnt",  sqlStr(i, "h_act_card_cnt"));
		        item.put("h_noact_card_cnt",  sqlStr(i, "h_noact_card_cnt"));
		        item.put("h_stop_card_cnt",  sqlStr(i, "h_stop_card_cnt"));
		        item.put("h_sum_cnt",  sqlStr(i, "h_sum_cnt"));
		        item.put("sum_fees",  sqlStr(i, "sum_fees"));
		        item.put("sum_year_fees",  sqlStr(i, "sum_year_fees"));
		        list.add(item);
		      }

		      TarokoJasperUtils.exportPdf(wp, "mktr2110_detl", "mktr2110", params, list);

		    } catch(Exception e) {
		      e.printStackTrace();
		    }	    
	  }
	  
	public void xlsPrint() throws Exception {
//		if (wp.itemStr("data_k1").equals("a")) {
//			xlsPrint_a();
//		}
//		if (wp.itemStr2("data_k1").equals("b")) {
//			xlsPrint_b();
//		}
	}
//
//	void pdfPrint_a() throws Exception {
//		m_progName = "mktr2110a";
//		// -cond-
//		String ss = "統計年月: " + zzstr.ss_2ymd(wp.item_ss("ex_acct_month"));
//		wp.col_set("cond_1", ss);
//		ss = "帳戶類別: " + wp.item_ss("ex_acct_type");
//		wp.col_set("cond_2", ss);
//		wp.col_set("IdUser", wp.loginUser);
//		wp.pageRows = 9999;
//		// queryFunc();
//		queryRead_a();
//		// wp.setListCount(1);
//
//		TarokoPDF pdf = new TarokoPDF();
//		wp.fileMode = "N";
//		pdf.excelTemplate = m_progName + ".xlsx";
//		pdf.sheetNo = 0;
//		pdf.pageCount = 28;
//		pdf.procesPDFreport(wp);
//		pdf = null;
//	}
//
//	void pdfPrint_b() throws Exception {
//		m_progName = "mktr2110b";
//		// -cond-
//		String ss = "統計年月: " + zzstr.ss_2ymd(wp.item_ss("ex_acct_month"));
//		wp.col_set("cond_1", ss);
//		ss = "帳戶類別: " + wp.item_ss("ex_acct_type");
//		wp.col_set("cond_2", ss);
//		wp.col_set("IdUser", wp.loginUser);
//		wp.pageRows = 9999;
//		// queryFunc();
//		queryRead_b();
//		wp.setListCount(2);
//
//		TarokoPDF pdf = new TarokoPDF();
//		wp.fileMode = "N";
//		pdf.excelTemplate = m_progName + ".xlsx";
//		pdf.sheetNo = 0;
//		pdf.pageCount = 28;
//		pdf.procesPDFreport(wp);
//		pdf = null;
//	}
//
//	void xlsPrint_a() throws Exception {
//		m_progName = "mktr2110a";
//		wp.reportId = m_progName;
//		try {
//			ddd("xlsFunction: started--------");
//			wp.reportId = m_progName;
//			// -cond-
//			// subTitle();
//			String ss = "查詢年月: " + zzstr.ss_2ymd(wp.item_ss("ex_acct_month"));
//			wp.col_set("cond_1", ss);
//			wp.col_set("IdUser", wp.loginUser);
//
//			// ===================================
//			// TarokoExcel xlsx = new TarokoExcel();
//			TarokoExcel2 xlsx = new TarokoExcel2();
//			wp.fileMode = "Y";
//			xlsx.excelTemplate = m_progName + ".xlsx";
//
//			// ====================================
//			// -明細-
//			xlsx.breakField[0] = "acct_type";
//			xlsx.sheetName[0] = "明細";
//			queryRead_a();
//			wp.setListCount(1);
//			ddd("Detl: rowcnt:" + wp.listCount[0]);
//			xlsx.processExcelSheet(wp);
//
//			xlsx.pageBreak = "Y";
//			xlsx.outputExcel();
//			xlsx = null;
//			ddd("xlsFunction: ended-------------");
//
//		} catch (Exception ex) {
//			wp.expMethod = "xlsPrint";
//			wp.expHandle(ex);
//		}
	@Override
	public void logOnlineApprove() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	 /***
	   * 日期月份减指定月份
	   *
	   * @param datetime
	   *            日期(2014-11)
	   * @param num
	   *            要减去的月份数
	   * @return 2014-10
	   */
	  public String subMonth(String datetime, int num) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
	    Date date = null;
	    try {
	      date = sdf.parse(datetime);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    Calendar cl = Calendar.getInstance();
	    cl.setTime(date);
	    cl.add(Calendar.MONTH, -num);
	    date = cl.getTime();
	    return sdf.format(date);
	  }

	
	/**
	   * 轉換日期格式
	   * @param datetime 日期字符串
	   * @param format1 原有格式
	   * @param format2 目標格式
	   * @return 字符串
	   * @throws Exception
	   */
	  public String dateFormatTo(String datetime, String format1, String format2) throws Exception  {
	    SimpleDateFormat sdf1 = new SimpleDateFormat(format1);
	    SimpleDateFormat sdf2 = new SimpleDateFormat(format2);
	    Date date = sdf1.parse(datetime);
	    return sdf2.format(date);
	  }


	  /**
	   * 检查文件的存在性,不存在则创建相应的路由，文件
	   * @param filepath
	   * @return 创建失败则返回null
	   */
	  public File checkFileExistence(String filepath){
	    File file = new File(filepath);
	    try {
	      if (!file.exists()){
	        if (filepath.charAt(filepath.length()-1) == '/' || filepath.charAt(filepath.length()-1) == '\\') {
	          file.mkdirs();
	        } else {
	          String[] split = filepath.split("[^/\\\\]+$");
	          checkFileExistence(split[0]);
	          file.createNewFile();
	        }
	      }
	    }catch (IOException e) {
	      e.printStackTrace();
	      file = null;
	    }
	    return file;
	  }


	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
		      this.btnModeAud();
		    }
		
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub
		
	}
}
