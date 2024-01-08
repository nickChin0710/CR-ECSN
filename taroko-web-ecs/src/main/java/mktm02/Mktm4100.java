package mktm02;

import java.util.Arrays;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Mktm4100 extends BaseAction implements InfacePdf  {
	String aprFlag = "" , mchtNo = "" , productNo = "" , seqNo = "" , isWhere1 = "" , isWhere2 = "";
	@Override
	public void userAction() throws Exception {
		switch (wp.buttonCode) {
	      case "X":
	        /* 轉換顯示畫面 */
	        strAction = "new";
	        clearFunc();
	        break;
	      case "Q":
	        /* 查詢功能 */
	        strAction = "Q";
	        queryFunc();
	        break;
	      case "R":
	        // -資料讀取-
	        strAction = "R";
	        dataRead();
	        break;
	      case "R2":
	    	// -明細資料-
	    	strAction = "R";
		    detlRead();
		    break;  
	      case "A":
	        /* 新增功能 */
	        saveFunc();
	        break;
	      case "U":
	        /* 更新功能 */
	        saveFunc();
	        break;
	      case "U2":
		    /* 更新功能 */
	    	detlSaveFunc();
		    break;  
	      case "D":
	        /* 刪除功能 */
	        saveFunc();
	        break;
	      case "M":
	        /* 瀏覽功能 :skip-page */
	        queryRead();
	        break;
	      case "S":
	        /* 動態查詢 */
	        querySelect();
	        break;
	      case "L":
	        /* 清畫面 */
	        strAction = "";
	        clearFunc();
	        break;
	      case "C":
	        // -資料處理-
	        procFunc();
	        break;
	      case "AJAX":
	    	wfGetMcht();
	    	break;
	      case "PDF":
	    	pdfPrint();
		    break;		    	
	      default:
	        break;
	    }

	}

	@Override
	public void dddwSelect() {
		try {
			if(eqIgno(wp.respHtml, "mktm4100_acct")) {
				wp.optionKey = wp.itemStr("ex_data_code");
				dddwList("dddw_data_value", "ptr_acct_type", "acct_type", "chin_name"," where  1=1  order by acct_type");
			} else if(eqIgno(wp.respHtml, "mktm4100_group")) {
				wp.optionKey = wp.itemStr("ex_data_code");
				dddwList("dddw_data_value", "ptr_group_code", "group_code", "group_name"," where  1=1  order by group_code");
			} else if(eqIgno(wp.respHtml, "mktm4100_card")) {
				wp.optionKey = wp.itemStr("ex_data_code");
				dddwList("dddw_data_value", "ptr_card_type", "card_type", "name"," where  1=1  order by card_type");
			}
		} catch (Exception ex) {}		
	}

	@Override
	public void queryFunc() throws Exception {
		if(chkStrend(wp.itemStr("ex_start_date1"), wp.itemStr("ex_start_date2")) == false) {
			alertErr("活動期間(起) : 起迄錯誤");
			return ;
		}		
		if(chkStrend(wp.itemStr("ex_end_date1"), wp.itemStr("ex_end_date2")) == false) {
			alertErr("活動期間(迄) : 起迄錯誤");
			return ;
		}
		
		getWhere();
		wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();
	    queryRead();
	}
	
	void getWhere() {
		if(wp.itemEq("ex_apr_flag", "0")) {
			isWhere1 =  " where 1=1 "					
					+sqlCol(wp.itemStr("ex_mcht_no"),"mcht_no","like%")
					+sqlBetween("ex_start_date1", "ex_start_date2", "start_date")
					+sqlBetween("ex_end_date1", "ex_end_date2", "end_date")
					+sqlCol(wp.itemStr("ex_crt_date"),"crt_date")
					+sqlCol(wp.itemStr("ex_crt_user"),"crt_user");
			
			isWhere2 =  " where 1=1 "
					+sqlCol(wp.itemStr("ex_mcht_no"),"mcht_no","like%")
					+sqlBetween("ex_start_date1", "ex_start_date2", "start_date")
					+sqlBetween("ex_end_date1", "ex_end_date2", "end_date")
					+sqlCol(wp.itemStr("ex_crt_date"),"crt_date")
					+sqlCol(wp.itemStr("ex_crt_user"),"crt_user");			
		}	else if(wp.itemEq("ex_apr_flag", "Y")) {		
			isWhere1 =  " where 1=1 "					
					+sqlCol(wp.itemStr("ex_mcht_no"),"mcht_no","like%")
					+sqlBetween("ex_start_date1", "ex_start_date2", "start_date")
					+sqlBetween("ex_end_date1", "ex_end_date2", "end_date")
					+sqlCol(wp.itemStr("ex_crt_date"),"crt_date")
					+sqlCol(wp.itemStr("ex_crt_user"),"crt_user");	
			wp.whereStr = isWhere1 ;
		}	else if(wp.itemEq("ex_apr_flag", "N")) {
			isWhere2 =  " where 1=1 "
					+sqlCol(wp.itemStr("ex_mcht_no"),"mcht_no","like%")
					+sqlBetween("ex_start_date1", "ex_start_date2", "start_date")
					+sqlBetween("ex_end_date1", "ex_end_date2", "end_date")
					+sqlCol(wp.itemStr("ex_crt_date"),"crt_date")
					+sqlCol(wp.itemStr("ex_crt_user"),"crt_user");
			
			wp.whereStr = isWhere2 ;
		}									    
	}
	
	@Override
	public void queryRead() throws Exception {
		wp.pageControl();		
		//--ex_apr_flag: 0:全部	Y:已覆核	N:未覆核
		if(wp.itemEq("ex_apr_flag", "0")) {
			if(isWhere1.isEmpty() || isWhere2.isEmpty()) {
				getWhere();
			}
			wp.sqlCmd = " select 'Y' as apr_flag , '已覆核' as tt_apr_flag , mcht_no , product_no , product_name , seq_no , start_date , end_date , "
					+ " tot_amt , tot_term , interest_rate , clt_fees_fix_amt , clt_interest_rate , limit_min , confirm_flag , "
					+ " installment_flag , trans_rate from bil_prod_nccc "
					+ isWhere1
					+ " union all "
					+ " select 'N' as apr_flag , '未覆核' as tt_apr_flag , mcht_no , product_no , product_name , seq_no , start_date , end_date , "
					+ " tot_amt , tot_term , interest_rate , clt_fees_fix_amt , clt_interest_rate , limit_min , confirm_flag , "
					+ " installment_flag , trans_rate from bil_prod_nccc_t "
					+ isWhere2
					+ " order by mcht_no , product_no "
					;
			wp.pageCountSql = " select sum(ab_cnt) from ("
							+ " select count(*) as ab_cnt from bil_prod_nccc " +isWhere1
							+ " union all "
							+ " select count(*) as ab_cnt from bil_prod_nccc_t " +isWhere2
							+ " ) "
							;
		}	else if(wp.itemEq("ex_apr_flag", "Y")) {
			wp.selectSQL = " 'Y' as apr_flag , '已覆核' as tt_apr_flag , mcht_no , product_no , product_name , seq_no , start_date , end_date , "
					+ " tot_amt , tot_term , interest_rate , clt_fees_fix_amt , clt_interest_rate , limit_min , confirm_flag , "
					+ " installment_flag , trans_rate "
					;
			wp.daoTable = "bil_prod_nccc";
			wp.whereOrder = " order by mcht_no , product_no ";
		}	else if(wp.itemEq("ex_apr_flag", "N")) {
			wp.selectSQL = " 'N' as apr_flag , '未覆核' as tt_apr_flag , mcht_no , product_no , product_name , seq_no , start_date , end_date , "
					+ " tot_amt , tot_term , interest_rate , clt_fees_fix_amt , clt_interest_rate , limit_min , confirm_flag , "
					+ " installment_flag , trans_rate "
					;
			wp.daoTable = "bil_prod_nccc_t";
			wp.whereOrder = " order by mcht_no , product_no ";
		}
		
		pageQuery();		
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
		wp.setListCount(0);
		wp.setPageValue();
		
		queryAfter();
	}
	
	void queryAfter() {
		String sql1 = "select mcht_chi_name from bil_merchant where mcht_no = ? ";
		for(int ii = 0 ; ii < wp.selectCnt ; ii++) {
			sqlSelect(sql1,new Object[]{wp.colStr(ii,"mcht_no")});
			if(sqlRowNum > 0) {
				wp.colSet(ii,"mcht_name", sqlStr("mcht_chi_name"));
			}
		}		
	}
	
	@Override
	public void querySelect() throws Exception {
		aprFlag = wp.itemStr("data_k1");
		mchtNo = wp.itemStr("data_k2"); 
		productNo = wp.itemStr("data_k3");
		seqNo = wp.itemStr("data_k4");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {		
		if(empty(mchtNo)) {
			alertErr("特店代號不可空白 !");
			return ;
		}
		if(empty(productNo)) {
			alertErr("分期期數不可空白 !");
			return ;
		}
		if(empty(seqNo)) {
			alertErr("序號不可空白 !");
			return ;
		}
		
		wp.selectSQL = "hex(rowid) as rowid , mod_seqno , mcht_no , product_no , product_name , seq_no , "
				+ "start_date , end_date , tot_amt , tot_term , clt_fees_fix_amt , fees_fix_amt , clt_interest_rate , "
				+ "interest_rate , limit_min , year_fees_rate , confirm_flag , installment_flag , trans_rate ,"
				+ "mod_user , to_char(mod_time,'yyyymmdd') as mod_date , crt_date , crt_user " ;
		
		if("N".equals(aprFlag)) {
			wp.daoTable = "bil_prod_nccc_t";
		} else {
			wp.daoTable = "bil_prod_nccc";	
		}
		wp.whereStr = "where 1=1"
					+ sqlCol(mchtNo,"mcht_no")
					+ sqlCol(productNo,"product_no")
					+ sqlCol(seqNo,"seq_no")
					;
		pageSelect();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
		dataReadAfter();						
	}
	
	void dataReadAfter() throws Exception {
		//--把 apr_flag 放到 Page2 
		wp.colSet("apr_flag", aprFlag);		
		if("Y".equals(aprFlag)) {
			wp.colSet("tt_apr_flag", "已覆核");
		} else if("N".equals(aprFlag)) {
			wp.colSet("tt_apr_flag", "未覆核");
		}
		String sql1 ="select mcht_chi_name from bil_merchant where mcht_no = ? ";
		sqlSelect(sql1,new Object[]{wp.colStr("mcht_no")});	
		if(sqlRowNum >0) {
			wp.colSet("mcht_name", sqlStr("mcht_chi_name"));
		} else {
			wp.colSet("mcht_name", "");
		}		
	}
	
	void detlRead() throws Exception {
		mchtNo = wp.itemStr("mcht_no");
		aprFlag = wp.itemStr("apr_flag");
		productNo = wp.itemStr("product_no");
		seqNo = wp.itemStr("seq_no");
		String kind = getKind();
		wp.selectSQL = "mcht_no , product_no , seq_no , bin_no , dtl_kind , dtl_value ";
		if("Y".equals(aprFlag)) {
			wp.daoTable = "bil_prod_nccc_bin";
		}	else	{
			wp.daoTable = "bil_prod_nccc_bin_t";
		}
		wp.whereOrder = " order by dtl_value ";
		wp.whereStr = " where 1=1 "
					+sqlCol(mchtNo,"mcht_no")
					+sqlCol(productNo,"product_no")
					+sqlCol(seqNo,"seq_no")
					+sqlCol(kind,"dtl_kind")
					;
		pageQuery();
		if(sqlNotFind()) {
			wp.colSet("ind_num", "0");
			selectOK();
			return ;
		}		
		wp.setListCount(0);
		wp.colSet("ind_num", "" + wp.selectCnt);
		detlReadAfter();
	}
	
	void detlReadAfter() throws Exception {
		//--取中文名稱
		if("mktm4100_acct".equals(wp.respHtml)) {
			String sql1 = "select acct_type||'-'||chin_name as tt_dtl_value from ptr_acct_type where acct_type = ? ";
			for(int ii = 0 ; ii < wp.selectCnt ; ii++ ) {
				sqlSelect(sql1,new Object[] {wp.colStr(ii,"dtl_value")});
				if(sqlRowNum > 0) {
					wp.colSet(ii,"tt_dtl_value", sqlStr("tt_dtl_value"));
				}
			}
		} else if("mktm4100_group".equals(wp.respHtml)) {
			String sql1 = "select group_code||'-'||group_name as tt_dtl_value from ptr_group_code where group_code = ? ";
			for(int ii = 0 ; ii < wp.selectCnt ; ii++ ) {
				sqlSelect(sql1,new Object[] {wp.colStr(ii,"dtl_value")});
				if(sqlRowNum > 0) {
					wp.colSet(ii,"tt_dtl_value", sqlStr("tt_dtl_value"));
				}
			}
		} else if("mktm4100_card".equals(wp.respHtml)) {
			String sql1 = "select card_type||'-'||name as tt_dtl_value from ptr_card_type where card_type = ? ";
			for(int ii = 0 ; ii < wp.selectCnt ; ii++ ) {
				sqlSelect(sql1,new Object[] {wp.colStr(ii,"dtl_value")});
				if(sqlRowNum > 0) {
					wp.colSet(ii,"tt_dtl_value", sqlStr("tt_dtl_value"));
				}
			}
		}
	}
	
	String getKind() {
		if (wp.respHtml.indexOf("_acct") > 0)			
		    return "ACCT-TYPE";
		else if (wp.respHtml.indexOf("_group") > 0)
		    return "GROUP-CODE";
		else if (wp.respHtml.indexOf("_card") > 0)
		    return "CARD-TYPE";
		
		return "";
	}
	
	@Override
	public void saveFunc() throws Exception {		
		mktm02.Mktm4100Func func = new mktm02.Mktm4100Func();
		func.setConn(wp);		
		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if(rc!=1) {
			 errmsg(func.getMsg());
		}	else	{
			saveAfter(false);
		}
	}
	
	public void detlSaveFunc() throws Exception {
		int llOk = 0 , llErr = 0;
		mktm02.Mktm4100Func func = new mktm02.Mktm4100Func();
		func.setConn(wp);
		String mchtNo = wp.itemStr("mcht_no");
		String productNo = wp.itemStr("product_no");
		String seqNo = wp.itemStr("seq_no");
		String kind = wp.itemStr("dtl_kind");
		String[] opt = wp.itemBuff("opt");
		String[] dtlValue = wp.itemBuff("dtl_value");
		wp.listCount[0] = wp.itemRows("dtl_value");
		func.varsSet("mcht_no", mchtNo);
		func.varsSet("product_no", productNo);
		func.varsSet("seq_no", seqNo);
		func.varsSet("dtl_kind", kind);
		
		int aa = -1;
		//--check data dup
		for (String tmpStr : dtlValue) {			
		    aa++;
		    wp.colSet(aa, "ok_flag", "");		    
		    if (checkBoxOptOn(aa, opt)) {		    	
		    	dtlValue[aa] = "";
		        continue;
		    }
		    if (aa != Arrays.asList(dtlValue).indexOf(tmpStr)) {
		        wp.colSet(aa, "ok_flag", "!");
		        llErr++;
		    }		      
		}
		
		if (llErr > 0) {			
		    alertErr("資料值重複: " + llErr);
		    return;
		}
		
		//--先刪除同類明細
		if(func.deleteBinTempKind() != 1) {
			alertErr(func.getMsg());
			return ;
		}
		
		//--新增明細
		for(int ii=0 ; ii < wp.itemRows("dtl_value") ; ii++) {
			//--On的是刪除明細
			if(checkBoxOptOn(ii, opt))
				continue;
			
			func.varsSet("dtl_value", dtlValue[ii]);
			if(func.insertBinTempKind() != 1) {
				llErr ++;
				dbRollback();
				wp.colSet(ii,"ok_flag", "X");
				continue;
			}	else	{
				llOk ++;
				dbCommit();
				wp.colSet(ii,"ok_flag", "V");
				continue;
			}
		}		
		alertMsg("處理完畢 , 成功:"+llOk+" 失敗:"+llErr);
	}
	
	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		if("mktm4100_detl".equals(wp.respHtml)) {
			btnModeAud(wp.colStr("rowid"));
			//--
			if(wp.colEq("apr_flag", "Y")) {
				//--不是異動檔不可刪除
				btnDeleteOn(false);
			}
		} else if("mktm4100_acct".equals(wp.respHtml)) {
			if(wp.colEq("apr_flag", "Y")) {
				//--不是異動檔不可異動明細
				btnUpdateOn(false);
			}
		} else if("mktm4100_group".equals(wp.respHtml)) {
			if(wp.colEq("apr_flag", "Y")) {
				//--不是異動檔不可異動明細
				btnUpdateOn(false);
			}
		}
		 else if("mktm4100_card".equals(wp.respHtml)) {
			if(wp.colEq("apr_flag", "Y")) {
				//--不是異動檔不可異動明細
				btnUpdateOn(false);
			}
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}
	
	void wfGetMcht() throws Exception {
		String lsMchtNo = wp.itemStr("ax_mcht_no");
		String lsProductNo = wp.itemStr("ax_product_no");
		//--確認特店代號存在  bil_merchant
		if(checkMcht(lsMchtNo) == false) {
			wp.addJSON("seq_no", "");
		    wp.addJSON("mcht_name", "");
		    wp.addJSON("mail_buy", "");
		    return ;
		}
		
		//--分期個位數前補 0
		if(lsProductNo.length()==1) {
			lsProductNo = "0"+lsProductNo;
		}
		
		int tempSeq = 0;
		//--取號
		String sql1 = "select max(temp_seq)+1 as seq_no from ("
					+ "select max(seq_no) as temp_seq from bil_prod_nccc where mcht_no = ? and product_no = ? "
					+ "union all "
					+ "select max(seq_no) as temp_seq from bil_prod_nccc_t where mcht_no = ? and product_no = ? "
					+ ") "
					;
		
		sqlSelect(sql1,new Object[] {lsMchtNo,lsProductNo,lsMchtNo,lsProductNo});
		tempSeq = sqlInt("seq_no");
		
		if(tempSeq == 0)
			tempSeq = 1;
		
		wp.addJSON("seq_no", commString.intToStr(tempSeq));
	    wp.addJSON("mcht_name", sqlStr("mcht_chi_name"));
	    wp.addJSON("mail_buy", sqlStr("mcht_type"));
	    wp.addJSON("kk_product_no", lsProductNo);
	    
	}
	
	boolean checkMcht(String mchtNo) {
		String sql1 ="select mcht_chi_name , uf_nvl(mcht_type,'1') as mcht_type from bil_merchant where mcht_no = ? ";
		sqlSelect(sql1,new Object[]{mchtNo});		
		if(sqlRowNum <=0) {
			alertErr("特店代號不存在 !");
			return false;
		}		
		return true;
	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "mktm4100";		
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "mktm4100.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;		
	}
	
}
