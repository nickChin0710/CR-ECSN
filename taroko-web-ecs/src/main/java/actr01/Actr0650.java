/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR     DESCRIPTION                                *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-12  V1.00.00  Max Lin    program initial                            *
* 111-10-20  v1.00.01  Zuwei Su   sync from mega, update coding standard                    *
*                                                                            *
******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0650 extends BaseReport {

	InputStream inExcelFile = null;
	String mProgName = "actr0650";

	String condWhere = "";
//String sum_where = ""; //金額合計 (Linda, 20180912)
	String strWhereA = "", strWhereB = "";
	String sumWhereA = "", sumWhereB = "";

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
		String exMchtNo = wp.itemStr("ex_mcht_no");
		String exAdjType = wp.itemStr("ex_adj_type");
		String exAcctKey = wp.itemStr("ex_acct_key");
		String exDateS = wp.itemStr("ex_date_S");
		String exDateE = wp.itemStr("ex_date_E");
		String exModUser = wp.itemStr("ex_mod_user");
		String exCrtDateS = wp.itemStr("ex_crt_date_S");
		String exCrtDateE = wp.itemStr("ex_crt_date_E");
		String exCurrCode = wp.itemStr("ex_curr_code");

//		if (empty(ex_mcht_no) == true) {
//			alertErr("請輸入專案代號(特店代號)");
//			return false;
//		}
	
		
		//專案代號(特店代號)、帳戶帳號、覆核日期、登錄日期、登錄人員 任一即可(Linda, 20180907)
		if (empty(exMchtNo) == true && empty(exAcctKey) == true &&
		    empty(exDateS) == true && empty(exDateE) == true &&
		    empty(exCrtDateS) == true && empty(exCrtDateE) == true &&
		    empty(exModUser) == true ){
			alertErr("請輸入查詢條件");
			return false;
		}		
		/*if (empty(ex_date_S) == true && empty(ex_date_E) == true) {
			alertErr("請輸入覆核日期");
			return false;
		}	
		if (empty(ex_crt_date_S) == true && empty(ex_crt_date_E) == true) {
			alertErr("請輸入登錄日期");
			return false;
		}*/
	
		//固定條件
	//String ls_where = " where 1=1 ";
	  strWhereA = " where AAH.p_seqno =  AA.acno_p_seqno ";
	  strWhereB = " where AAH.p_seqno = SAA.acno_p_seqno ";
		
		if (empty(exMchtNo) == false){
			strWhereA += " and AAH.mcht_no = :ex_mcht_no ";
			setString("ex_mcht_no", exMchtNo);
			strWhereB += " and AAH.mcht_no = :ex_mcht_no ";
			setString("ex_mcht_no", exMchtNo);
		}		
		
		if (empty(exAdjType) == false){
			strWhereA += " and AAH.adjust_type = :ex_adj_type ";
			setString("ex_adj_type", exAdjType);
			strWhereB += " and AAH.adjust_type = :ex_adj_type ";
			setString("ex_adj_type", exAdjType);
		}		
		
		if (empty(exAcctKey) == false){
			strWhereA += " and  AA.acct_key like :ex_acct_key ";
			setString("ex_acct_key", exAcctKey + "%");
			strWhereB += " and SAA.acct_key like :ex_acct_key ";
			setString("ex_acct_key", exAcctKey + "%");
		}		
		
		if (empty(exDateS) == false){
			strWhereA += " and to_char(AAH.mod_time,'yyyymmdd') >= :ex_date_S ";
			setString("ex_date_S", exDateS);
			strWhereB += " and to_char(AAH.mod_time,'yyyymmdd') >= :ex_date_S ";
			setString("ex_date_S", exDateS);
		}		
		
		if (empty(exDateE) == false){
			strWhereA += " and to_char(AAH.mod_time,'yyyymmdd') <= :ex_date_E ";
			setString("ex_date_E", exDateE);
			strWhereB += " and to_char(AAH.mod_time,'yyyymmdd') <= :ex_date_E ";
			setString("ex_date_E", exDateE);
		}	
		
		if (empty(exModUser) == false){
			strWhereA += " and AAH.update_user = :ex_mod_user ";
			setString("ex_mod_user", exModUser);
			strWhereB += " and AAH.update_user = :ex_mod_user ";
			setString("ex_mod_user", exModUser);
		}		
		
		if (empty(exCrtDateS) == false){
			strWhereA += " and AAH.crt_date >= :ex_crt_date_S ";
			setString("ex_crt_date_S", exCrtDateS);
			strWhereB += " and AAH.crt_date >= :ex_crt_date_S ";
			setString("ex_crt_date_S", exCrtDateS);
		}		
		
		if (empty(exCrtDateE) == false){
			strWhereA += " and AAH.crt_date <= :ex_crt_date_E ";
			setString("ex_crt_date_E", exCrtDateE);
			strWhereB += " and AAH.crt_date <= :ex_crt_date_E ";
			setString("ex_crt_date_E", exCrtDateE);
		}	
		
		if (empty(exCurrCode) == false){
			strWhereA += " and uf_nvl(AAH.curr_code, '901') = :ex_curr_code ";
			setString("ex_curr_code", exCurrCode);
			strWhereB += " and uf_nvl(AAH.curr_code, '901') = :ex_curr_code ";
			setString("ex_curr_code", exCurrCode);
		}			
	
				
				
	//wp.whereStr = ls_where;
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
		String exMchtNo = wp.itemStr("ex_mcht_no");
		String exAdjType = wp.itemStr("ex_adj_type");
		String exAcctKey = wp.itemStr("ex_acct_key");
		String exDateS = wp.itemStr("ex_date_S");
		String exDateE = wp.itemStr("ex_date_E");
		String exModUser = wp.itemStr("ex_mod_user");
		String exCrtDateS = wp.itemStr("ex_crt_date_S");
		String exCrtDateE = wp.itemStr("ex_crt_date_E");
		String exCurrCode = wp.itemStr("ex_curr_code");
		//金額合計 (Linda, 20180912)--------------start
		//固定條件
    
	//sum_where = " where 1=1 ";
	  sumWhereA = " where AAH.p_seqno =  AA.acno_p_seqno ";
	  sumWhereB = " where AAH.p_seqno = SAA.acno_p_seqno ";
		
		if (empty(exMchtNo) == false){
			sumWhereA  += sqlCol(exMchtNo,"AAH.mcht_no","=");
			sumWhereB  += sqlCol(exMchtNo,"AAH.mcht_no","=");
		}	
		if (empty(exAdjType) == false){
			sumWhereA  += sqlCol(exAdjType,"AAH.adjust_type","=");
			sumWhereB  += sqlCol(exAdjType,"AAH.adjust_type","=");
		}	
		if (empty(exAcctKey) == false){
			sumWhereA  += sqlCol(exAcctKey,"AA.acct_key","like%");
			sumWhereB  += sqlCol(exAcctKey,"SAA.acct_key","like%");
		}	
		if (empty(exDateS) == false){
			sumWhereA  += sqlCol(exDateS,"to_char(AAH.mod_time,'yyyymmdd')",">=");
			sumWhereB  += sqlCol(exDateS,"to_char(AAH.mod_time,'yyyymmdd')",">=");
		}	
		if (empty(exDateE) == false){
			sumWhereA  += sqlCol(exDateE,"to_char(AAH.mod_time,'yyyymmdd')","<=");
			sumWhereB  += sqlCol(exDateE,"to_char(AAH.mod_time,'yyyymmdd')","<=");
		}	
		if (empty(exModUser) == false){
			sumWhereA  += sqlCol(exModUser,"AAH.update_user","=");
			sumWhereB  += sqlCol(exModUser,"AAH.update_user","=");
		}		
		if (empty(exCrtDateS) == false){
			sumWhereA  += sqlCol(exCrtDateS,"AAH.crt_date",">=");
			sumWhereB  += sqlCol(exCrtDateS,"AAH.crt_date",">=");
		}
		if (empty(exCrtDateE) == false){
			sumWhereA  += sqlCol(exCrtDateE,"AAH.crt_date","<=");
			sumWhereB  += sqlCol(exCrtDateE,"AAH.crt_date","<=");
		}			
		if (empty(exCurrCode) == false){
			sumWhereA  += sqlCol(exCurrCode,"uf_nvl(AAH.curr_code, '901')","=");
			sumWhereB  += sqlCol(exCurrCode,"uf_nvl(AAH.curr_code, '901')","=");
		}			
		
		//金額合計------------------------------------end
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if (getWhereStr() == false)
			return;
		
	  wp.sqlCmd = "select "
	      + "(AA.acct_type || '-' || AA.acct_key) as acct_type_key, "
			//+ "uf_nvl(CI.chi_name, CC.chi_name) || (case when CC.corp_no is not null then '_' || CC.corp_no else '' end) as chi_name, "
				+ "AA.id_p_seqno as id_p_seqno, "
				+ "AA.corp_p_seqno as corp_p_seqno, "
				+ "uf_nvl(AAH.curr_code, '901') as curr_code, "
				+ "uf_dc_amt(uf_nvl(AAH.curr_code, '901'), AAH.orginal_amt, AAH.dc_orginal_amt) as dc_orginal_amt, "
				+ "uf_dc_amt(uf_nvl(AAH.curr_code, '901'), AAH.dr_amt, AAH.dc_dr_amt) as dc_dr_amt, "
				+ "uf_dc_amt(uf_nvl(AAH.curr_code, '901'), AAH.cr_amt, AAH.dc_cr_amt) as dc_cr_amt, "
				+ "AAH.acct_code, "
				+ "AAH.reference_no, "
				+ "AAH.adjust_type, "
				+ "AAH.crt_date, "
				+ "AAH.update_user, "
				+ "AAH.mod_user, "
				+ "AAH.mcht_no, "
				+ "AAH.adj_comment "
				+ "from act_acaj_hst as AAH, act_acno as AA "
			//+ "left join crd_idno as CI on AA.id_p_seqno = CI.id_p_seqno "
			//+ "left join crd_corp as CC on AA.corp_p_seqno = CC.corp_p_seqno "
				+ strWhereA
		    + " union "
	      + " select "
	      + "(SAA.acct_type || '-' || SAA.acct_key) as acct_type_key, "
			//+ "uf_nvl(CI.chi_name, CC.chi_name) || (case when CC.corp_no is not null then '_' || CC.corp_no else '' end) as chi_name, "
				+ "SAA.id_p_seqno as id_p_seqno, "
				+ "SAA.corp_p_seqno as corp_p_seqno, "
				+ "uf_nvl(AAH.curr_code, '901') as curr_code, "
				+ "uf_dc_amt(uf_nvl(AAH.curr_code, '901'), AAH.orginal_amt, AAH.dc_orginal_amt) as dc_orginal_amt, "
				+ "uf_dc_amt(uf_nvl(AAH.curr_code, '901'), AAH.dr_amt, AAH.dc_dr_amt) as dc_dr_amt, "
				+ "uf_dc_amt(uf_nvl(AAH.curr_code, '901'), AAH.cr_amt, AAH.dc_cr_amt) as dc_cr_amt, "
				+ "AAH.acct_code, "
				+ "AAH.reference_no, "
				+ "AAH.adjust_type, "
				+ "AAH.crt_date, "
				+ "AAH.update_user, "
				+ "AAH.mod_user, "
				+ "AAH.mcht_no, "
				+ "AAH.adj_comment "
				+ "from act_acaj_hst as AAH, ecs_act_acno as SAA "
			//+ "left join crd_idno as CI on SAA.id_p_seqno = CI.id_p_seqno "
			//+ "left join crd_corp as CC on SAA.corp_p_seqno = CC.corp_p_seqno "
				+ strWhereB
		    + " order by acct_type_key ";
		
	//ddd("wp.sqlCmd=" + wp.sqlCmd);
		wp.pageCountSql = "select count(*) from (" + wp.sqlCmd + ")";

		if (strAction.equals("XLS")) {
			selectNoLimit();
		}
		
		pageQuery();
		// list_wkdata();
		wp.setListCount(1);
		
		if (sqlRowNum <= 0) {
	  	wp.colSet("ft_cnt", "");
	  	wp.colSet("sum_orginal_amt", "");
		  wp.colSet("sum_dr_amt", "");
		  wp.colSet("sum_dc_dr_amt", "");
		  wp.colSet("sum_cr_amt", "");
		  wp.colSet("sum_dc_cr_amt", "");
			alertErr("此條件查無資料");
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.colSet("ft_cnt", Integer.toString(wp.dataCnt));
		
		//金額合計 (Linda, 20180912)
		setParameter();
		sum();
		
		wp.setPageValue();
		listWkdata();
	}
	
	//金額合計 (Linda, 20180912)
	void sum() throws Exception{
	// String sql1 ="select sum(orginal_amt) as sum_orginal_amt, sum(dr_amt) as sum_dr_amt,  " 
	//		+ "sum(cr_amt) as sum_cr_amt, sum(dc_dr_amt) as sum_dc_dr_amt, sum(dc_cr_amt) as sum_dc_cr_amt "
	  String sql1 ="select " 
	      + "sum(orginal_amt) as sum_orginal_amt, sum(dr_amt) as sum_dr_amt, sum(cr_amt) as sum_cr_amt, " 
				+ "sum(uf_dc_amt(uf_nvl(curr_code, '901'), dr_amt, dc_dr_amt)) as sum_dc_dr_amt, "
				+ "sum(uf_dc_amt(uf_nvl(curr_code, '901'), cr_amt, dc_cr_amt)) as sum_dc_cr_amt "
				+ "from "
				+ "( "
				+ " select AAH.* from act_acaj_hst as AAH, act_acno as AA "
				+ sumWhereA
				+ " union "
				+ " select AAH.* from act_acaj_hst as AAH, ecs_act_acno as SAA "
				+ sumWhereB
				+ ") ";
		sqlSelect(sql1);
	//System.out.println("1.sql1=" + sql1);
	//ddd("sum.sql1=" + sql1);
		wp.colSet("sum_orginal_amt", sqlStr("sum_orginal_amt"));
		wp.colSet("sum_dr_amt", sqlStr("sum_dr_amt"));
		wp.colSet("sum_cr_amt", sqlStr("sum_cr_amt"));
		wp.colSet("sum_dc_dr_amt", sqlStr("sum_dc_dr_amt"));
		wp.colSet("sum_dc_cr_amt", sqlStr("sum_dc_cr_amt"));
	}

	void listWkdata() throws Exception {
		int rowCt = 0;
		String lsIdPSeqno = "", lsCorpPseqno = "";
		String sqlSelect1 = "", sqlSelect2 = "";
		//int sum_orginal_amt = 0;
		//int sum_dr_amt = 0;
		
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			//計算欄位
			rowCt += 1;			
			//sum_orginal_amt += Integer.parseInt(wp.colStr(ii, "orginal_amt"));
			//sum_dr_amt += Integer.parseInt(wp.colStr(ii, "dr_amt"));
		  lsIdPSeqno   = wp.colStr(ii,"id_p_seqno");
		  lsCorpPseqno = wp.colStr(ii,"corp_p_seqno");
		
  		if (empty(lsIdPSeqno) == false) {
		    sqlSelect1=" select chi_name from  crd_idno where id_p_seqno = :id_p_seqno ";
		    setString("id_p_seqno",lsIdPSeqno);
		    sqlSelect(sqlSelect1);
		    if(sqlRowNum>0){
		      wp.colSet(ii,"chi_name", sqlStr("chi_name"));
		    } 		  	
		  } else if (empty(lsCorpPseqno) == false) {
			  sqlSelect2=" select corp_no,chi_name from  crd_corp where corp_p_seqno = :corp_p_seqno ";
		    setString("corp_p_seqno",lsCorpPseqno);
		    sqlSelect(sqlSelect2);
		    if(sqlRowNum>0){
		      wp.colSet(ii,"chi_name", sqlStr("chi_name")+"_"+sqlStr("corp_no") );
		    }
  		}
  	}
		
		wp.colSet("row_ct", intToStr(rowCt));
	}
	
	void xlsPrint() {
		try {
			log("xlsFunction: started--------");
			wp.reportId = mProgName;
			
			// -cond-
			String exMchtNo = wp.itemStr("ex_mcht_no");
			String exAdjType = wp.itemStr("ex_adj_type");
			String exAcctKey = wp.itemStr("ex_acct_key") + "%";
			String exDateS = wp.itemStr("ex_date_S");
			String exDateE = wp.itemStr("ex_date_E");
			String exModUser = wp.itemStr("ex_mod_user");
			String exCrtDateS = wp.itemStr("ex_crt_date_S");
			String exCrtDateE = wp.itemStr("ex_crt_date_E");
			String exCurrCode = wp.itemStr("ex_curr_code");

			String cond1 = "專案代號(特店代號): " + exMchtNo + "  D 檔類別: " + exAdjType + "  帳戶帳號: " + exAcctKey;		 	  	
			String cond2 = "覆核日期: " + exDateS + " ~ " + exDateE + "  登錄日期: " + exCrtDateS + " ~ " + exCrtDateE
					+ "  登錄人員: " + exModUser + "  結算幣別: " + exCurrCode;		 	  	
			wp.colSet("cond_1", cond1);
			wp.colSet("cond_2", cond2);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			xlsx.excelTemplate = mProgName + ".xlsx";

			//====================================
			xlsx.sheetName[0] ="D 檔明細表";
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
			// 專案代號(特店代號)
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_mcht_no");
			dddwList("dddw_mcht_no", "bil_merchant", "mcht_no", "mcht_chi_name", "where mcht_status = '1' and mcht_type = '1' order by mcht_no");
			
			// D 檔類別
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_adj_type");
			dddwList("dddw_adj_type", "act_acaj_hst", "distinct adjust_type", "", "where 1 = 1 order by adjust_type");
			
			// 登錄人員
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_mod_user");
			dddwList("dddw_mod_user", "sec_user", "usr_id", "usr_cname", "where 1=1 order by usr_id");

			// 結算幣別
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_curr_code");
			dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
					"where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id");
		} catch (Exception ex) {
		}
	}

}

