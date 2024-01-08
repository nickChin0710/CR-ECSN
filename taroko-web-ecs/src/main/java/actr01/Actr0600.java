/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR     DESCRIPTION                                *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-12  V1.00.00  Max Lin    program initial                            *
* 110-03-04  v1.00.02  Andy       Update PDF隠碼作業                                                                      *
* 111-10-20  v1.00.03  Zuwei Su   sync from mega, update coding standard                    *
******************************************************************************/
package actr01;

import java.io.InputStream;

import ofcapp.BaseReport;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Actr0600 extends BaseReport {

	InputStream inExcelFile = null;
	String mProgName = "actr0600";
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
			//xlsPrint();
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
		// init_button();
	}

	@Override
	public void initPage() {
		//設定初始搜尋條件值
		wp.colSet("ex_date_S", getSysDate());
		wp.colSet("ex_date_E", getSysDate());
	}
	
	public void clearFunc() throws Exception {
		wp.resetInputData();
		wp.resetOutputData();
	}

	private boolean getWhereStr() throws Exception {
		String exDateS = wp.itemStr("ex_date_S");
		String exDateE = wp.itemStr("ex_date_E");
		String exDate2S = wp.itemStr("ex_date2_S");
		String exDate2E = wp.itemStr("ex_date2_E");
		String exVip = wp.itemStr("ex_vip");
		String exAcctType = wp.itemStr("ex_acct_type");
		String exAcctKey = wp.itemStr("ex_acct_key");
		String exBankId = wp.itemStr("ex_bank_id");
		String exFromMark = wp.itemStr("ex_from_mark");
		String exSmsFlag = wp.itemStr("ex_sms_flag");
		String exRtnStatus = wp.itemStr("ex_rtn_status");
		String exAdMark = wp.itemStr("ex_ad_mark");

		if (this.chkStrend(exDateS, exDateE) == false) {
			alertErr("[提出驗印日期-起迄]  輸入錯誤");
			return false;
		}
		
		if (empty(exDateS) == true && empty(exDateE) == true) {
			alertErr("請輸入提出驗印日期");
			return false;
		}
	
		//固定條件
		String lsWhere = " where 1=1 ";
		
		if (empty(exDateS) == false){
			lsWhere += " and AAD.process_date >= :ex_date_S ";
			setString("ex_date_S", exDateS);
		}		
		
		if (empty(exDateE) == false){
			lsWhere += " and AAD.process_date <= :ex_date_E ";
			setString("ex_date_E", exDateE);
		}	
		
		if (empty(exDate2S) == false){
			lsWhere += " and AAD.rtn_date >= :ex_date2_S ";
			setString("ex_date2_S", exDate2S);
		}		
		
		if (empty(exDate2E) == false){
			lsWhere += " and AAD.rtn_date <= :ex_date2_E ";
			setString("ex_date2_E", exDate2E);
		}	
		
		if (exVip.equals("1")) {lsWhere += " and AAD.vip_code = '' ";}
		else if (exVip.equals("2")) {lsWhere += " and AAD.vip_code <> '' ";}
		
		if (empty(exAcctType) == false){
			lsWhere += " and AAD.acct_type = :ex_acct_type ";
			setString("ex_acct_type", exAcctType);
		}	
		
		if (empty(exAcctKey) == false){
			lsWhere += " and AA.acct_key like :ex_acct_key ";
			setString("ex_acct_key", exAcctKey + '%');
		}	
		
		if (empty(exBankId) == false){
			lsWhere += " and AAD.bank_no = :ex_bank_id ";
			setString("ex_bank_id", exBankId);
		}	
		
	//if (ex_from_mark.equals("1")) {ls_where += " and AC.from_mark = '01' ";}
	//else if (ex_from_mark.equals("2")) {ls_where += " and AC.from_mark <> '01' ";}
    /***
		if (ex_from_mark.equals("1")) {ls_where += " and AC.from_mark in ('1','01') ";}
		else if (ex_from_mark.equals("2")) {ls_where += " and AC.from_mark not in ('1','01') ";}
    ***/
		//from_mark: ActH003 會將 '' 轉換為 '01' 
		if (exFromMark.equals("1")) {lsWhere += " and AAD.from_mark in ('01','1') ";} 
		else if (exFromMark.equals("2")) {lsWhere += " and AAD.from_mark in ('02') ";}
		else if (exFromMark.equals("3")) {lsWhere += " and AAD.from_mark in ('03') ";}
		else if (exFromMark.equals("4")) {lsWhere += " and AAD.from_mark in ('04') ";}
		else if (exFromMark.equals("5")) {lsWhere += " and AAD.from_mark in ('W') ";}
		
		if (!exSmsFlag.equals("0")) {
			lsWhere += " and AAD.sms_flag = :ex_sms_flag ";
			setString("ex_sms_flag", exSmsFlag);
		}
		
		if (exRtnStatus.equals("1")) {lsWhere += " and AAD.rtn_status = '0' and AAD.rtn_date <> '' ";}
		else if (exRtnStatus.equals("2")) {lsWhere += " and AAD.rtn_status not in ('', '0', 'Z') ";}
		else if (exRtnStatus.equals("3")) {lsWhere += " and AAD.rtn_status = '' ";}
		else if (exRtnStatus.equals("4")) {lsWhere += " and AAD.rtn_status = 'Z' and (AAD.bank_no like '017%' or AAD.bank_no like '700%') ";}
		
	  if (exAdMark.equals("A")) {lsWhere += " and AAD.ad_mark <> 'D' ";}
	  else if (exAdMark.equals("D")) {lsWhere += " and AAD.ad_mark = 'D' ";}
	//if (ex_ad_mark.equals("A")) {ls_where += " and AC.ad_mark <> 'D' ";}
	//else if (ex_ad_mark.equals("D")) {ls_where += " and AC.ad_mark = 'D' ";}
		
				
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
		
		wp.sqlCmd = "select AAD.bank_no, "
				+ "AAD.vip_code, "
				+ "AAD.process_date, "
			  + "AAD.ad_mark as ad_mark, "
			  + "(case when AAD.ad_mark = 'D' then '刪除' else '新增' end) as tt_ad_mark, "
				+ "AAD.acct_type, "
				+ "AA.acct_key, "
				+ "AA.autopay_acct_no as aa_autopay_acct_no, "
				+ "AAD.autopay_indicator, "
				+ "AA.autopay_indicator as aa_autopay_indicator, "
			//+ "AC.autopay_indicator as ac_autopay_indicator, "
				+ "AAD.p_seqno, "
				+ "AAD.stmt_cycle, "
				+ "AAD.chi_name, "
				+ "AAB.bank_name, "
				+ "(AAB.bank_no||' '||AAB.bank_name) bank_id_name, "
				+ "AAD.autopay_acct_no, "
				+ "AAD.autopay_id, "
				+ "AAD.from_mark, "
				+ "AAD.rtn_description, "
				+ "AAD.rtn_date, "
				+ "1 row_cnt, " //for PDF report
			//+ " '銀行名稱：' as bank_title, " //for PDF report
				+ "AAD.effc_date, "
			//+ "AAD.first_flag, "
				+ "(case when AAD.sms_flag = '1' then '已傳送' when AAD.sms_flag = '2' then '無法傳送' "
				+ "		when AAD.sms_flag = '3' then '不須傳送' else '' end) as sms_flag "
				+ "from act_ach_dtl as AAD "
			//+ "left join act_ach_bank as AAB on substr(AAD.bank_no,1,3) = substr(AAB.bank_no,1,3) "
				+ "left join act_ach_bank as AAB on AAD.bank_no = AAB.bank_no "
				+ "left join act_acno as AA on AAD.p_seqno = AA.acno_p_seqno "
			//+ "left join act_chkno as AC on AAD.p_seqno = AC.p_seqno and AAD.ad_mark = AC.ad_mark and AAD.autopay_acct_no = AC.autopay_acct_no "
				+ wp.whereStr
				+ " order by AAD.bank_no, AAD.process_date ";		
		
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
	
	void listWkdata() throws Exception{
		int rowCt = 0;
		int sumFeeCnt = 0;
		int sumFeeAmt = 0;
		int sumOriginalAmt = 0;
		String  lsBankNo = "", lsBankNo3B = "";	
		String	lsSql = "select (bank_no||' '||bank_name) bank_id_name "
					         + "from act_ach_bank where substr(bank_no,1,3) = :bank_no_3B ";
			
		String	lsSql2 = "select autopay_indicator as ac_autopay_indicator "
					           + "from act_chkno where p_seqno = :ps_p_seqno "
					           + "and ad_mark = :ps_ad_mark "
					           + "and autopay_acct_no = :ps_autopay_acct_no "
					           + "and ach_send_date <= :ps_process_date "
					           + "order by ach_send_date desc "
					           + "fetch first 1 row only ";
			
		String ss = "", lsDispYyyy="",lsDispMm="",lsDispDd="",lsDispDate="";
		 //debug:畫面上無此欄位,造成作業error, 先mark起來 (Linda, 20180717)
		   for (int ii = 0; ii < wp.selectCnt; ii++) {
			//計算欄位
			   rowCt += 1;		//from_mark: ActH003 會將 '' 轉換為 '01'
      	 if(wp.colEq(ii,"from_mark", "01") || wp.colEq(ii,"from_mark", "1") ){
			     wp.colSet(ii,"tt_from_mark", "新製卡");			
		     }	else if(wp.colEq(ii,"from_mark", "02") ){
			     wp.colSet(ii,"tt_from_mark", "授權書-新申請");
		     }	else if(wp.colEq(ii,"from_mark", "03") ){
			     wp.colSet(ii,"tt_from_mark", "授權書-修改帳號");
		     }	else if(wp.colEq(ii,"from_mark", "04") ){
			     wp.colSet(ii,"tt_from_mark", "04");
		     }	else if(wp.colEq(ii,"from_mark", "W") ){
			     wp.colSet(ii,"tt_from_mark", "W");
		     }
		
			//非7碼bank_no用前3碼重抓一次 act_ach_bank
		     lsBankNo = commStr.mid(wp.colStr(ii,"bank_no"), 0,7);	
      	 if(lsBankNo.length() != 7 ) {
		       lsBankNo3B = commStr.mid(wp.colStr(ii,"bank_no"), 0,3);	
		       setString("bank_no_3B", lsBankNo3B);
			     sqlSelect(lsSql);
		       if (sqlRowNum > 0) {
			       wp.colSet(ii,"bank_id_name", sqlStr("bank_id_name"));			
		       }
		     }	


       //因為 autopay_indicator、from_mark為act_ach_dtl新增欄位，舊資料這兩欄位為空值
      	 if(wp.colStr(ii,"autopay_indicator").length() > 0){
			     //ss=wp.colStr(ii,"autopay_indicator"); 			
		     } else {
	  	     setString("ps_p_seqno", wp.colStr(ii,"p_seqno"));
		       setString("ps_ad_mark", wp.colStr(ii,"ad_mark"));
		       setString("ps_autopay_acct_no", wp.colStr(ii,"autopay_acct_no"));
		       setString("ps_process_date", wp.colStr(ii,"process_date"));
			     sqlSelect(lsSql2);
		       if (sqlRowNum > 0) {
			        wp.colSet(ii,"ac_autopay_indicator", sqlStr("ac_autopay_indicator"));			
		       }
			     wp.colSet(ii,"autopay_indicator", wp.colStr(ii,"ac_autopay_indicator"));			
		       if(wp.colStr(ii,"ac_autopay_indicator").length() == 0 ) {
			        wp.colSet(ii,"autopay_indicator", wp.colStr(ii,"aa_autopay_indicator"));			
           }
		     } 

     		 if(wp.colEq(ii,"autopay_indicator", "1")){
				   wp.colSet(ii,"tt_autopay_indicator", "扣TTL");
			   }	else if(wp.colEq(ii,"autopay_indicator", "2")){
				   wp.colSet(ii,"tt_autopay_indicator", "扣MP");
			   }	else if(wp.colEq(ii,"autopay_indicator", "3")){
				   wp.colSet(ii,"tt_autopay_indicator", "其他");
			   }

       //for PDF report below 			
        /***
      	 if(wp.colStr(ii,"process_date").length() > 0){
			     ls_disp_yyyy=zzstr.mid(wp.colStr(ii,"process_date"), 0,4);			
			     ls_disp_mm=zzstr.mid(wp.colStr(ii,"process_date"), 4,2);			
			     ls_disp_dd=zzstr.mid(wp.colStr(ii,"process_date"), 6,2);			
			     ls_disp_date=ls_disp_yyyy+"/"+ls_disp_mm+"/"+ls_disp_dd;			
			     wp.colSet(ii,"tt_process_date", ls_disp_date);			
		     } else  {
			     wp.colSet(ii,"tt_process_date", "");	
		     } 

      	 if(wp.colStr(ii,"rtn_date").length() > 0){
			     ls_disp_yyyy=zzstr.mid(wp.colStr(ii,"rtn_date"), 0,4);			
			     ls_disp_mm=zzstr.mid(wp.colStr(ii,"rtn_date"), 4,2);			
			     ls_disp_dd=zzstr.mid(wp.colStr(ii,"rtn_date"), 6,2);			
			     ls_disp_date=ls_disp_yyyy+"/"+ls_disp_mm+"/"+ls_disp_dd;			
			     wp.colSet(ii,"tt_rtn_date", ls_disp_date);			
		     } else  {
			     wp.colSet(ii,"tt_rtn_date", "");	
		     } 

      	 if(wp.colStr(ii,"effc_date").length() > 0){
			     ls_disp_yyyy=zzstr.mid(wp.colStr(ii,"effc_date"), 0,4);			
			     ls_disp_mm=zzstr.mid(wp.colStr(ii,"effc_date"), 4,2);			
			     ls_disp_dd=zzstr.mid(wp.colStr(ii,"effc_date"), 6,2);			
			     ls_disp_date=ls_disp_yyyy+"/"+ls_disp_mm+"/"+ls_disp_dd;			
			     wp.colSet(ii,"tt_effc_date", ls_disp_date);			
		     } else  {
			     wp.colSet(ii,"tt_effc_date", "");	
		     } 
        ***/

		  }
		
		wp.colSet("row_ct", intToStr(rowCt));
		
		/* debug:畫面上無此欄位,造成作業error, 先mark起來 (Linda, 20180717)
		 wp.colSet("sum_fee_cnt", int_2Str(sum_fee_cnt));
		 wp.colSet("sum_fee_amt", int_2Str(sum_fee_amt));
		 wp.colSet("sum_original_amt", int_2Str(sum_original_amt));
		 */
	}
	
 /*** 
	void xlsPrint() {
		try {
			ddd("xlsFunction: started--------");
			wp.reportId = m_progName;
			
			// -cond-
			String ex_date_S = wp.itemStr("ex_date_S");
			String ex_date_E = wp.itemStr("ex_date_E");
			String ex_vip = wp.itemStr("ex_vip");
			String ex_acct_type = wp.itemStr("ex_acct_type");
			String ex_acct_key = wp.itemStr("ex_acct_key") + '%';
			String ex_bank_id = wp.itemStr("ex_bank_id");
			String ex_from_mark = wp.itemStr("ex_from_mark");
			String ex_sms_flag = wp.itemStr("ex_sms_flag");
			String ex_rtn_status = wp.itemStr("ex_rtn_status");
			String ex_ad_mark = wp.itemStr("ex_ad_mark");

			String cond_1 = "提出驗印日期: " + ex_date_S + " ~ " + ex_date_E; 			 	  	
			
			if (ex_vip.equals("0")) {cond_1 += "  全部卡友";}
			else if (ex_vip.equals("1")) {cond_1 += "  一般卡友";}
			else if (ex_vip.equals("2")) {cond_1 += "  VIP 卡友";}
			
			cond_1 += "  帳戶類別: " + ex_acct_type + "  正卡 ID/統編: " + ex_acct_key;

			String cond_2 = "銀行代號: " + ex_bank_id + "  來源類別: "; 			 	  	
			if (ex_from_mark.equals("0")) {cond_2 += "全部";}
			else if (ex_from_mark.equals("1")) {cond_2 += "新製卡";}
			else if (ex_from_mark.equals("2")) {cond_2 += "授權書-新申請";}
			else if (ex_from_mark.equals("3")) {cond_2 += "授權書-修改帳號";}
			else if (ex_from_mark.equals("4")) {cond_2 += "雲端櫃台-04";}
			else if (ex_from_mark.equals("5")) {cond_2 += "網銀-W";}
			
			cond_2 += "  簡訊發送狀態: ";
			if (ex_sms_flag.equals("0")) {cond_2 += "全部";}
			else if (ex_sms_flag.equals("1")) {cond_2 += "已傳送";}
			else if (ex_sms_flag.equals("2")) {cond_2 += "無法傳送";}
			else if (ex_sms_flag.equals("3")) {cond_2 += "不須傳送";}
			
			cond_2 += "  回覆狀態: ";
			if (ex_rtn_status.equals("0")) {cond_2 += "全部";}
			else if (ex_rtn_status.equals("1")) {cond_2 += "成功 ";}
			else if (ex_rtn_status.equals("2")) {cond_2 += "失敗";}
			else if (ex_rtn_status.equals("3")) {cond_2 += "未回覆";}
			else if (ex_rtn_status.equals("4")) {cond_2 += "免驗印";}
			
			cond_2 += "  授扣款帳號狀態 : ";
			if (ex_ad_mark.equals("0")) {cond_2 += "全部";}
			else if (ex_ad_mark.equals("D")) {cond_2 += "刪除";}
			else {cond_2 += "新增";}
			
			wp.colSet("cond_1", cond_1);
			wp.colSet("cond_2", cond_2);

			// ===================================
			TarokoExcel xlsx = new TarokoExcel();
			wp.fileMode = "N";
			xlsx.excelTemplate = m_progName + ".xlsx";

			//====================================
			xlsx.sheetName[0] ="自動扣款帳號驗印明細表 ";
			queryFunc();
			wp.setListCount(1);
			ddd("Summ: rowcnt:" + wp.listCount[1]);
     		xlsx.processExcelSheet(wp);

			xlsx.outputExcel();
			xlsx = null;
			ddd("xlsFunction: ended-------------");

		} catch (Exception ex) {
			wp.expMethod = "xlsPrint";
			wp.expHandle(ex);
		}
	}
 ***/ 

	void pdfPrint() throws Exception {
		// 寫入Log紀錄檔 
		if (pdfLog() != 1) {
			wp.respHtml = "TarokoErrorPDF";
			return;
		}
		wp.reportId = mProgName;
		// -cond-
		String ex_date_S = wp.itemStr("ex_date_S");
		String ex_date_E = wp.itemStr("ex_date_E");
		String ex_date2_S = wp.itemStr("ex_date2_S");
		String ex_date2_E = wp.itemStr("ex_date2_E");
		String ex_vip = wp.itemStr("ex_vip");
		String ex_acct_type = wp.itemStr("ex_acct_type_t");
		String ex_acct_key = wp.itemStr("ex_acct_key");
		String ex_bank_id = wp.itemStr("ex_bank_id_t");
		String ex_from_mark = wp.itemStr("ex_from_mark");
		String ex_sms_flag = wp.itemStr("ex_sms_flag");
		String ex_rtn_status = wp.itemStr("ex_rtn_status");
		String ex_ad_mark = wp.itemStr("ex_ad_mark");

		if (this.chkStrend(ex_date_S, ex_date_E) == false) {
			alertErr("[提出驗印日期-起迄]  輸入錯誤");
			wp.respHtml = "TarokoErrorPDF";
			return ;
		}
		
		if (empty(ex_date_S) == true && empty(ex_date_E) == true) {
			alertErr("請輸入提出驗印日期");
			wp.respHtml = "TarokoErrorPDF";
			return ;
		}
		
		String cond_1 = "提出驗印日期: " + ex_date_S + " ~ " + ex_date_E; 			 	  	
		cond_1 += "    驗印提回日期: " + ex_date2_S + " ~ " + ex_date2_E; 			 	  	
		
		if (ex_vip.equals("0")) {cond_1 += "  全部卡友";}
		else if (ex_vip.equals("1")) {cond_1 += "  一般卡友";}
		else if (ex_vip.equals("2")) {cond_1 += "  VIP 卡友";}
		
		cond_1 += "  帳戶類別: " + ex_acct_type + "  正卡 ID/統編: " + ex_acct_key;

		String cond_2 = "銀行代號: " + ex_bank_id + "  來源類別: "; 			 	  	
		if (ex_from_mark.equals("0")) {cond_2 += "全部";}
		else if (ex_from_mark.equals("1")) {cond_2 += "新製卡";}
		else if (ex_from_mark.equals("2")) {cond_2 += "授權書-新申請";}
		else if (ex_from_mark.equals("3")) {cond_2 += "授權書-修改帳號";}
		else if (ex_from_mark.equals("4")) {cond_2 += "雲端櫃台-04";}
		else if (ex_from_mark.equals("5")) {cond_2 += "網銀-W";}
			
		
		cond_2 += "  簡訊發送狀態: ";
		if (ex_sms_flag.equals("0")) {cond_2 += "全部";}
		else if (ex_sms_flag.equals("1")) {cond_2 += "已傳送";}
		else if (ex_sms_flag.equals("2")) {cond_2 += "無法傳送";}
		else if (ex_sms_flag.equals("3")) {cond_2 += "不須傳送";}
		
		cond_2 += "  回覆狀態: ";
		if (ex_rtn_status.equals("0")) {cond_2 += "全部";}
		else if (ex_rtn_status.equals("1")) {cond_2 += "成功 ";}
		else if (ex_rtn_status.equals("2")) {cond_2 += "失敗";}
		else if (ex_rtn_status.equals("3")) {cond_2 += "未回覆";}
		else if (ex_rtn_status.equals("4")) {cond_2 += "免驗印";}
		
		cond_2 += "  授扣款帳號狀態: ";
		if (ex_ad_mark.equals("0")) {cond_2 += "全部";}
		else if (ex_ad_mark.equals("D")) {cond_2 += "刪除";}
		else {cond_2 += "新增";}
		
		wp.colSet("cond_1", cond_1);
		wp.colSet("cond_2", cond_2);
		wp.pageRows = 99999;

		queryFunc();
		// wp.setListCount(1);

		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "N";
		pdf.excelTemplate = mProgName + ".xlsx";
		pdf.sheetNo = 0;
		pdf.pageCount = 27;
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
			// 帳戶類別
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_acct_type");
			dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 order by acct_type");
			
			// 銀行代號
			wp.initOption = "--";
			wp.optionKey = wp.colStr("ex_bank_id");
			dddwList("dddw_bank_id", "act_ach_bank", "bank_no", "bank_name", "where 1=1 order by bank_no");
		} catch (Exception ex) {
		}
	}

	int pdfLog() throws Exception {
		String isSql = "INSERT INTO LOG_ONLINE_APPROVE "
				+ "(program_id, file_name, crt_date, crt_user, apr_flag, apr_date, apr_user) "
				+ "values ('actr0600', 'actr0600.pdf', :crt_date, :crt_user, 'Y', :apr_date, :apr_user )";
		setString("crt_date", wp.sysDate+wp.sysTime);
		setString("crt_user", wp.loginUser);
		setString("apr_date", wp.sysDate);
		setString("apr_user", wp.itemStr("zz_apr_user"));

		sqlExec(isSql);
		if (sqlRowNum <= 0) {
			alertErr("Log紀錄檔寫入失敗 !");
		}
		return 1;
	}

}

