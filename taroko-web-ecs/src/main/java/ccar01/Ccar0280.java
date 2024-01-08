package ccar01;

/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*/
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommSqlStr;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar0280 extends BaseAction implements InfacePdf {
	String hhIdPseqno = "", hhIdPseqno2 = "", lsAcctIdx = "";
	String errMesg = "";
	@Override
	public void userAction() throws Exception {
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
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			saveFunc();
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
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			procFunc();
		} else if (eqIgno(wp.buttonCode, "PDF1")) { // -PDF-
			strAction = "PDF";
			pdfPrint();
		} else if (eqIgno(wp.buttonCode, "PDF2")) { // -PDF-
			strAction = "PDF";
			pdfPrint2();
		}

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {
		if (empty(wp.itemStr("ex_date1")) && empty(wp.itemStr("ex_date2")) && empty(wp.itemStr("ex_send_date1"))
				&& empty(wp.itemStr("ex_send_date2"))) {
			alertErr2("臨調登錄日, 通知寄送日: 不可同時空白");
			return;
		}
		if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr2("臨調登錄日起迄：輸入錯誤");
			return;
		}
		if (this.chkStrend(wp.itemStr("ex_send_date1"), wp.itemStr("ex_send_date2")) == false) {
			alertErr2("通知寄送日起迄：輸入錯誤");
			return;
		}
		String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_date1"), "crt_date", ">=")
				+ sqlCol(wp.itemStr("ex_date2"), "crt_date", "<=")
				+ sqlCol(wp.itemStr("ex_send_date1"), "send_date", ">=")
				+ sqlCol(wp.itemStr("ex_send_date2"), "send_date", "<=");

		if (!empty(wp.itemStr("ex_idno"))) {
			selectIdPseqno();
			selectCardAcctIdxi();
			if (empty(lsAcctIdx)) {
				alertErr2("身分證ID輸入錯誤");
				return;
			}
			lsWhere += sqlCol(lsAcctIdx, "card_acct_idx");
		}

		if (!empty(wp.itemStr("ex_card_no"))) {
			selectCardAcctIdxo();
			if (empty(lsAcctIdx)) {
				alertErr2("卡號輸入錯誤");
				return;
			}
			lsWhere += sqlCol(lsAcctIdx, "card_acct_idx");
		}

		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

	void selectIdPseqno() {
		String sql1 = "select " + wp.sqlID + " uf_idno_pseqno(:id_no) as hh_id_p_seqno " + " from dual ";
		setString("id_no", wp.itemStr("ex_idno"));
		sqlSelect(sql1);
		hhIdPseqno = sqlStr("hh_id_p_seqno");
	}

	void selectCardAcctIdxo() {
		String sql1 = "select " + " card_acct_idx as ls_acct_idx " + " from cca_card_base " + " where card_no = ? "
				+ " and debit_flag <>'Y' ";
		sqlSelect(sql1, new Object[] { wp.itemStr("ex_card_no") });
		lsAcctIdx = sqlStr("ls_acct_idx");

	}

	void selectCardAcctIdxi() {
		String sql1 = "select " + " card_acct_idx as ls_acct_idx " + " from cca_card_base " + " where id_p_seqno = ? "
				+ " and debit_flag <>'Y' ";
		sqlSelect(sql1, new Object[] { hhIdPseqno });
		lsAcctIdx = sqlStr("ls_acct_idx");

	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = "" + " crt_date , " + " send_date , " + " del_adj_date , " + " id_no , " + " major_chi_name , "
				+ " chi_name , " + " org_tot_consume , " + " lmt_tot_consume , " + " adj_eff_start_date , "
				+ " adj_eff_end_date , " + " adj_reason , " + " adj_remark , " + " crt_user , " + " card_acct_idx , "
				+ " id_p_seqno , " + " sup_flag , hex(rowid) as rowid , "
				+ " (select sys_data1 from cca_sys_parm3 where sys_id = 'ADJREASON' and sys_key = adj_reason ) as tt_adj_reason "

		;
		wp.daoTable = "cca_adj_notice";
		wp.whereOrder = "  order by crt_date ";
		if (empty(wp.whereStr)) {
			wp.whereStr = " ORDER BY 1";
		}

		logSql();
		pageQuery();

		wp.setListCount(1);
		if (sqlRowNum <= 0) {
			alertErr2("此條件查無資料");
			return;
		}
		queryAfter();
		wp.setPageValue();

	}

	void queryAfter() {

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wp.colSet(ii, "wk_adj_reason", wp.colStr(ii, "adj_reason") + "_" + wp.colStr(ii, "tt_adj_reason"));
			wp.colSet(ii, "wk_major_id", sqlStr("wk_major_id"));
			wp.colSet(ii, "wk_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_start_date")) + " -- "
					+ commString.strToYmd(wp.colStr(ii, "adj_eff_end_date")));
			if (empty(wp.colStr(ii, "card_acct_idx"))) {
				continue;
			}
			String sql1 = "select " + " uf_idno_id(id_p_seqno) as wk_major_id , " + " acct_type  "
					+ " from cca_card_acct " + " where 1=1 " + " and card_acct_idx = ? ";
			sqlSelect(sql1, new Object[] { wp.colStr(ii, "card_acct_idx") });

			if (sqlRowNum <= 0)
				continue;
			wp.colSet(ii, "wk_major_id", sqlStr("wk_major_id"));
			wp.colSet(ii, "acct_type", sqlStr("acct_type"));
		}
	}
	
	void mailDataSelect() throws Exception {
		int ilErr = 0;
		taroko.base.CommDate commDate = new taroko.base.CommDate();
		taroko.base.CommSqlStr commStr = new taroko.base.CommSqlStr();
		String lsPrintDate = "" , lsSysdate ="";
		String[] idPseqNo = wp.itemBuff("id_p_seqno");
		String[] supChiName = wp.itemBuff("chi_name");
		String[] majorChiName = wp.itemBuff("major_chi_name");
		String[] adjDate1 = wp.itemBuff("adj_eff_start_date");
		String[] adjDate2 = wp.itemBuff("adj_eff_end_date");
		String[] aftAmt = wp.itemBuff("lmt_tot_consume");
		String[] rowid = wp.itemBuff("rowid");
		String[] opt = wp.itemBuff("opt");
		wp.listCount[0] = wp.itemRows("crt_date");
		
		int rr = -1;
	    rr = optToIndex(opt[0]);
	    if (rr < 0) {
	    	errMesg = "請點選欲列印信函資料";
	    	return;
	    }
		
	    String sql1 = "select B.card_no , substring(B.card_no , length(B.card_no)-3) as last_card_no4 ,"
	    		+ " A.sex , A.mail_zip , A.mail_addr1||A.mail_addr2||A.mail_addr3||A.mail_addr4 as mail_addr14 , A.mail_addr5 "
	    		+ " from crd_idno A join crd_card B on A.id_p_seqno = B.id_p_seqno "
	    		+ " where B.id_p_seqno = ? and sup_flag ='1' and A.chi_name = ? "
	    		;	    
	    
	    int print_cnt = 0;
	    String sex = "";
	    lsSysdate = getSysDate();
	    lsSysdate = commDate.toTwDate(lsSysdate);
	    lsPrintDate = "中　　　華　　　民　　　國　　" + lsSysdate.substring(0, 3) + "　 年 　"
                + lsSysdate.substring(3, 5) + "　 月　" + lsSysdate.substring(5, 7) + "　日";	    
	    for (int ii = 0; ii < opt.length; ii++) {
	        rr = optToIndex(opt[ii]);
	        if (rr < 0) {
	          continue;
	        }	        
	        sqlSelect(sql1,new Object[] {idPseqNo[rr],supChiName[rr]});
	        if(sqlRowNum <=0) {	        	
	        	wp.colSet(rr,"ok_flag", "X");
	        	ilErr++;
        		continue;
	        }
	        	       	        
	        wp.colSet(print_cnt,"ls_zip", sqlStr("mail_zip"));
	        wp.colSet(print_cnt,"ls_addr", sqlStr("mail_addr14"));
	        wp.colSet(print_cnt,"ls_addr5", sqlStr("mail_addr5"));
	        wp.colSet(print_cnt, "ls_cname",supChiName[rr].trim());
	        if("1".equals(sqlStr("sex"))) {
	        	sex = "先生";
	        } else if("2".equals(sqlStr("sex"))) {
	        	sex = "小姐";
	        }
	        wp.colSet(print_cnt, "ex_ll_11", supChiName[rr].trim() + " " + sex + " 您好:");
	        wp.colSet(print_cnt, "ex_ll_13","　　您所持有附卡卡號末四碼:"+sqlStr("last_card_no4")+"，主卡人已進行臨時額度調整。");
	        wp.colSet(print_cnt, "ex_ll_14","於臨調期間 "+commDate.dspDate(adjDate1[rr])+" 至 "+commDate.dspDate(adjDate2[rr])+" 額度調整至 "+String.format("%,14.0f", Double.parseDouble(aftAmt[rr]))+"元。");
	        wp.colSet(print_cnt, "print_date", lsPrintDate);
	        print_cnt++;
	        
	        
	        
	    }
	    
	    wp.listCount[0] = print_cnt;	    
	}
	
	@Override
	public void querySelect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataRead() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFunc() throws Exception {		
		int ilOk =0 , ilErr =0;
		String[] opt = wp.itemBuff("opt");
		wp.listCount[0] = wp.itemRows("crt_date");
		
		int rr = -1;
	    rr = optToIndex(opt[0]);
	    if (rr < 0) {
	    	errMesg = "請點選欲修改寄送日期資料";
	    	return;
	    }
		
	    for (int ii = 0; ii < opt.length; ii++) {
	        rr = optToIndex(opt[ii]);
	        if (rr < 0) {
	          continue;
	        }
	        
	        rc = updateCcaAdjNotice(rr);
	        if(rc != 1) {
	        	ilErr++;
	        	wp.colSet(rr,"ok_flag", "X");
	        	dbRollback();
	        	continue;
	        }	else	{
	        	ilOk++;
	        	wp.colSet(rr,"ok_flag", "V");
	        	dbCommit();
	        	continue;
	        }	        	        
	    }
	    
	    alertMsg("成功筆數: "+ilOk+" 失敗筆數:"+ilErr);	    
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}
	
	public int updateCcaAdjNotice(int rr) {

		String sql1 = "";		
		sql1 = " update cca_adj_notice set send_date = to_char(sysdate,'yyyymmdd') , mod_time = sysdate , mod_user = ? ,";
		sql1 += " mod_seqno = nvl(mod_seqno,0)+1 where 1=1 and card_acct_idx = ? and crt_date = ? and id_p_seqno = ? ";
		setString(1,wp.loginUser);
		setDouble(2,wp.itemNum(rr,"card_acct_idx"));
		setString(3,wp.itemStr(rr,"crt_date"));
		setString(4,wp.itemStr(rr,"id_p_seqno"));		
		
		sqlExec(sql1);

		return rc;
	}
	
	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "ccar0280";

		String cond1 = "臨調日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
				+ commString.strToYmd(wp.itemStr("ex_date2"));
		wp.colSet("cond1", cond1);
		wp.pageRows = 9999;
		queryFunc();

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar0280.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

	public void pdfPrint2() throws Exception {
		wp.reportId = "ccar0280";		
		wp.pageRows = 9999;
		mailDataSelect();
		if(errMesg.isEmpty() == false ) {
			alertErr(errMesg);
			wp.respHtml = "TarokoErrorPDF";
			return ;
		}
		TarokoPDF pdf = new TarokoPDF();
		pdf.pageVert = true;
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccar0280_mail.xlsx";
		pdf.pageCount = 1;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;

	}

}
