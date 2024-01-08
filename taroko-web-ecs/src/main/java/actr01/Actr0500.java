/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR     DESCRIPTION                                *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-08  V1.00.00  Max Lin    program initial                            *
* 109/03/20  V1.00.01  phopho     add sub_sum & total_sum                    *
* 110-03-04  v1.00.02  Andy       Update PDF隠碼作業                                                                      *
* 111-10-20  v1.00.03  Zuwei Su   sync from mega, update coding standard                    *
* 112-08-08  v1.00.04  Ryan       新增acct_status欄位                            *
* 112-11-01  v1.00.05  Ryan       ESC 不存在   ==> ESC ID 不存在                         *
* 112-01-02  v1.00.06  Ryan       增加帳戶往來狀態查詢條件,成功失敗合計                                               *
*****************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0500 extends BaseReport {

	InputStream inExcelFile = null;
	String mProgName = "actr0500";
	CommString commStr = new CommString();
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
		String exBankId = wp.itemStr("ex_bank_id");
		String exErrtype = wp.itemStr("ex_err_type");
		String exAcctStatus = wp.itemStr("ex_acct_status");
		if (this.chkStrend(exDateS, exDateE) == false) {
			alertErr("[入帳日期-起迄]  輸入錯誤");
			return false;
		}
		
		if (empty(exDateS) == true && empty(exDateE) == true) {
			alertErr("請輸入入帳日期");
			return false;
		}
	
		//固定條件
		String lsWhere = " where 1=1 ";

		if (empty(exDateS) == false){
			lsWhere += " and AA500.enter_acct_date >= :ex_date_S ";
			setString("ex_date_S", exDateS);
		}		
		
		if (empty(exDateE) == false){
			lsWhere += " and AA500.enter_acct_date <= :ex_date_E ";
			setString("ex_date_E", exDateE);
		}	

		if (empty(exId) == false){
		//ls_where += " and AA500.id_no like :ex_id ";
			lsWhere += " and uf_nvl(uf_idno_id(AA500.id_p_seqno),AA500.id_p_seqno) like :ex_id ";
			setString("ex_id", exId + "%");
		}		
		
		if (empty(exBankId) == false){
			lsWhere += " and substring(AA500.issue_id, 1, 3) = :ex_bank_id ";
			setString("ex_bank_id", exBankId.substring(0, 3));
		}	
		
		if (empty(exAcctStatus) == false){
			lsWhere += " and acno.acct_status = :exAcctStatus ";
			setString("exAcctStatus", exAcctStatus);
		}	
		
		if (exErrtype.equals("1")) {lsWhere += " and AA500.err_rsn = '1' ";}
		else if (exErrtype.equals("2")) {lsWhere += " and AA500.err_rsn <> '1' ";}
		
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
		
		wp.sqlCmd = "select AA500.enter_acct_date, "
				+ "AA500.issue_id, "
				+ "(SELECT bc_abname FROM ptr_bankcode WHERE bc_bankcode = AA500.issue_id) bank_name,"
				+ "(AA500.issue_id||' '||(SELECT bc_abname FROM ptr_bankcode WHERE bc_bankcode = AA500.issue_id)) bank_id_name, "
				+ "AA500.acct_type, AA500.p_seqno,"
			//+ "AA500.acct_key, "
				+ "uf_acno_key(AA500.p_seqno) as acct_key, "
				+ "AA500.acct_no, "
				+ "uf_hi_acctno(AA500.acct_no) as db_hi_acct_no, "
			//+ "AA500.id_no, "
			//+ "substr(uf_idno_id(AA500.id_p_seqno),1,10) as id_no, "
				+ "substr(uf_nvl(uf_idno_id(AA500.id_p_seqno),AA500.id_p_seqno),1,10) as id_no, "
				+ "uf_hi_idno(substr(uf_nvl(uf_idno_id(AA500.id_p_seqno),AA500.id_p_seqno),1,10)) as db_hi_id_no, "
				+ "AA500.chi_name, "
				+ "uf_hi_cname(AA500.chi_name) as db_hi_chi_name, "
				+ "AA500.tx_amt, "
				+ "AA500.tot_amt, "
				+ "AA500.enter_acct_date||AA500.issue_id as rpt_group, "
				//+ "(case when AA500.err_rsn = '1' then '依欠款比例入帳' when AA500.err_rsn = '2' then 'ESC ID 不存在' "
				+ "(case when AA500.err_rsn = '1' then '依繳款金額入帳' when AA500.err_rsn = '2' then 'ESC ID 不存在' "
				+ "		when AA500.err_rsn = '3' then 'ID 重號且同時均欠款' else '' end) as err_rsn,"
				+ "1 row_cnt "
				+ " ,acno.acct_status "
				+ " ,AA500.err_rsn as err_code "
				+ "from act_a500r1 as AA500 "
//				+ "left join ptr_bankcode as PB on AA500.issue_id = PB.bc_bankcode "
//				+ "left join act_ach_bank as AAB on AA500.issue_id = AAB.bank_no "
				+ " left join act_acno acno on AA500.p_seqno = acno.p_seqno "
				+ wp.whereStr
				+ " order by AA500.enter_acct_date, AA500.issue_id ";

		wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";

		if (strAction.equals("XLS")) {
			selectNoLimit();
		}
		
		pageQuery();

		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr("此條件查無資料");
			return;
		}

		wp.colSet("loginUser", wp.loginUser);
		listWkdata(wp.selectCnt);
		wp.setPageValue();
	}
	
	void listWkdata(int selectCnt) throws Exception{
		int row_ct = 0;
		int sum_tx_amt = 0;
		int sum_tot_amt = 0;
		int okNum = 0;
		int errNum = 0;
		int okTxAmt = 0;
		int errTxAmt = 0;
		int okTotAmt = 0;
		int errTotAmt = 0;
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			//計算欄位
			row_ct += 1;			
			sum_tx_amt += toInt(wp.colStr(ii, "tx_amt"));
			sum_tot_amt += toInt(wp.colStr(ii, "tot_amt"));
			
			String errCode = wp.colStr(ii,"err_code");
			if(!"1".equals(errCode)) {
				errNum ++;
				errTxAmt += toInt(wp.colStr(ii, "tx_amt"));
				errTotAmt += toInt(wp.colStr(ii, "tot_amt"));
			}else {
				okNum ++;
				okTxAmt += toInt(wp.colStr(ii, "tx_amt"));
				okTotAmt += toInt(wp.colStr(ii, "tot_amt"));
			}
		}
		
		wp.colSet("row_ct", row_ct);
		wp.colSet("sum_tx_amt", sum_tx_amt);
		wp.colSet("sum_tot_amt", sum_tot_amt);
		wp.colSet("ok_num", okNum );
		wp.colSet("err_num", errNum);
		wp.colSet("ok_tx_amt", okTxAmt );
		wp.colSet("err_tx_amt", errTxAmt);
		wp.colSet("ok_tot_amt", okTotAmt );
		wp.colSet("err_tot_amt", errTotAmt);
		
//		String issueId = "", issueName = "";
//		int sumRowCt = 0, allRowCt = 0;
//		double sumTxAmt = 0, allTxAmt = 0;
//		String bank_code="", bank_name="";
//		String sql_select = " select bc_bankcode, bc_abname from ptr_bankcode order by bc_bankcode ";
//		sqlSelect(sql_select);
//		for(int i=0;i<sqlRowNum;i++){
//			bank_code += ","+sql_ss(i,"bc_bankcode");
//			bank_name += ","+sql_ss(i,"bc_abname");
//		}
		
//		int llRowcnt = wp.selectCnt;
//		for (int ii = 0; ii < llRowcnt; ii++) {
//			//小計總計
//			sumRowCt++;
//			allRowCt++;
//			sumTxAmt += wp.colNum(ii,"tx_amt");
//			allTxAmt += wp.colNum(ii,"tx_amt");
//			if (!wp.colStr(ii,"enter_acct_date").equals(wp.colStr(ii+1,"enter_acct_date")) ||
//					!wp.colStr(ii,"issue_id").equals(wp.colStr(ii+1,"issue_id"))) {
//				issueId = wp.colStr(ii,"issue_id");
//				issueName = wp.colStr(ii,"bank_name");
////				issue_name = zzStr.decode(issue_id, bank_code, bank_name);
//				wp.colSet(ii,"tr","<tr><td nowrap colspan=\"2\" class=\"list_rr\" style=\"color:blue\">"+issueId+"&nbsp;</td>"
//						+ "<td nowrap colspan=\"3\" class=\"list_ll\" style=\"color:blue\">"+issueName+"</td>"
//						+ "<td nowrap class=\"list_rr\" style=\"color:#CC0000\">&nbsp;小    計：</td>"
//						+ "<td nowrap class=\"list_rr\" style=\"color:blue\">"+numToStr(sumTxAmt,"#,##0")+"</td>"
//						+ "<td nowrap class=\"list_rr\" style=\"color:blue\">"+numToStr(sumRowCt,"###0")+"</td>"
//						+ "<td nowrap class=\"list_ll\">&nbsp;</td></tr>");
//				sumRowCt=0;
//				sumTxAmt=0;
//			}
//		}
//		
//		if (llRowcnt > 0) {
//			String strTotal="";
//			strTotal += "<tr><td nowrap colspan=\"6\" class=\"list_rr\" style=\"color:#CC0000\">&nbsp;總 筆 數：</td>"
//					+ "<td nowrap class=\"list_rr\" style=\"color:blue\">"+numToStr(allTxAmt,"#,##0")+"</td>"
//					+ "<td nowrap class=\"list_rr\" style=\"color:blue\">"+numToStr(allRowCt,"###0")+"</td>"
//					+ "<td nowrap class=\"list_ll\">&nbsp;</td></tr>";
//			
//			wp.colSet("total",strTotal);
//		}

//		for (int ii = 0; ii < selectCnt; ii++) {
//			String acctStatus = "";
//			String pSeqno = wp.colStr(ii,"p_seqno");
//			String sqlCmd = "select acct_status from act_acno where p_seqno = :p_seqno ";
//			setString("p_seqno",pSeqno);
//			sqlSelect(sqlCmd);
//			if(sqlRowNum > 0) {
//				acctStatus = sqlStr("acct_status");
//			}
//			wp.colSet(ii,"acct_status", acctStatus);
//		}
		

	}
	
	void xlsPrint() {
		try {
			log("xlsFunction: started--------");
			wp.reportId = mProgName;
			
			// -cond-
			String exDateS = wp.itemStr("ex_date_S");
			String exDateE = wp.itemStr("ex_date_E");
			String exId = wp.itemStr("ex_id") + "%";
			String exBankId = wp.itemStr("ex_bank_id");
			String exErrType = wp.itemStr("ex_err_type");

			String cond1 = "入帳日期: " + exDateS + " ~ " + exDateE + "  身分證字號: " + exId + "  銀行代號: " + exBankId + "  錯誤類別: ";
			
			if (exErrType.equals("0")) {cond1 += "全部";}
			//else if (exErrType.equals("1")) {cond1 += "依欠款比例入帳";}
			else if (exErrType.equals("1")) {cond1 += "依繳款金額入帳";}
			else if (exErrType.equals("2")) {cond1 += "其他";}
				
			wp.colSet("cond_1", cond1);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			xlsx.excelTemplate = mProgName + ".xlsx";

			//====================================
			xlsx.sheetName[0] ="債務協商回灌結果查詢(不含依債協比例入帳)";
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
		String exDateS = wp.itemStr("ex_date_S");
		String exDateE = wp.itemStr("ex_date_E");
		String exId = wp.itemStr("ex_id");
		String exBankId = wp.itemStr("ex_bank_id_t");
		String exErrType = wp.itemStr("ex_err_type");

		if (this.chkStrend(exDateS, exDateE) == false) {
			alertErr("[入帳日期-起迄]  輸入錯誤");
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		
		if (empty(exDateS) == true && empty(exDateE) == true) {
			alertErr("請輸入入帳日期");
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		
		String cond1 = "入帳日期: " + exDateS + " ~ " + exDateE + "  身分證字號: " + exId + "  銀行代號: " + exBankId + "  錯誤類別: ";
		
		if (exErrType.equals("0")) {cond1 += "全部";}
		else if (exErrType.equals("1")) {cond1 += "依繳款金額入帳";} //old:依欠款比例入帳
		else if (exErrType.equals("2")) {cond1 += "其他";}
			
		wp.colSet("cond_1", cond1);
		
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
			// 銀行代號
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_bank_id");
			dddwList("dddw_bank_id", "act_ach_bank", "bank_no", "bank_name", "where 1=1 order by bank_no");
		} catch (Exception ex) {
		}
	}

}

