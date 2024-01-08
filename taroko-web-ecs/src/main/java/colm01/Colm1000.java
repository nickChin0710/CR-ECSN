/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-01-16  V1.00.01  Ryan       Initial                                  *
* 112-06-09  V1.00.02  Ryan       增加姓名欄位                                                                                          *
* 112-06-20  V1.00.03  Ryan       增加目前協商方式、目前協商狀態查詢選項                                          *
* 112-06-27  V1.00.04  Ryan       增加帳戶類別查詢選項、 id_corp_no為統編、姓名帶出公司名稱  *
* 112-07-17  V1.00.05  Ryan       增加客戶類型                                                                                           
* 112-10-27  V1.00.06  Ryan       增加試算月付金額功能                                                                          *
* 112-11-17  V1.00.07  Ryan       修正客戶編號, 客戶姓名顯示未帶出問題                                              *
* 112-11-21  V1.00.08  Ryan       增加試算期數功能                                                                                 *
* 112-11-30  V1.00.09  Ryan       修正系統現欠餘額未帶出金額問題                                                       *
* 112-12-12  V1.00.10  Ryan       試算期數增加背景顏色顯示                                                                 *
* 112-12-13  V1.00.11  Ryan       調整債權餘額                                                                *
* 112-12-15  V1.00.12  Ryan       畫面查詢增加帳戶狀態查詢條件                                                                *
* 112-12-19  V1.00.13  Ryan       欄位調整帳務類別帳戶狀態協商時的MCODE              
* 112-12-19  V1.00.14  Ryan       帳戶狀態改抓act_acno新增協商時帳戶狀態                                     
* 112-12-21  V1.00.15  Ryan       modify 首期繳款日,債權計算截止日,目前應繳期數(含簽約當月)(計算) 欄位 *
* 112-12-22  V1.00.16  Sunny      modify 商務卡公司現欠餘額 *
* 112-12-22  V1.00.17  Sunny      modify 利率為0的試算處理 *
***************************************************************************/
package colm01;

import java.math.BigDecimal;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm1000 extends BaseEdit {
	busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	busi.ecs.CommRoutine comr = null;
	// ************************************************************************
	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
			strAction = "R";
			dataRead();
		} else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
			strAction = "A";
			insertFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
			strAction = "U";
			updateFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
			deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "S1")) {/* 清畫面 */
			checkIdCorpNo();
		} else if (eqIgno(wp.buttonCode, "S2")) {/* 清畫面 */
			countAmt();
		} else if (eqIgno(wp.buttonCode, "S3")) {/* 清畫面 */
			countPeriod();
		} 

		dddwSelect();
		initButton();
	}

	// ************************************************************************
	@Override
	public void queryFunc() throws Exception {
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}
	
	private boolean getWhereStr() throws Exception {
	    wp.whereStr = " where 1=1 ";
	    String exIdCorpNo = wp.itemStr("ex_id_corp_no");
	    String exCpbdueBeginDate1 = wp.itemStr("ex_cpbdue_begin_date1");
	    String exCpbdueBeginDate2 = wp.itemStr("ex_cpbdue_begin_date2");
	    String exCpbdueType = wp.itemStr("ex_cpbdue_type");
	    String exCpbdueCurrType = wp.itemStr("ex_cpbdue_curr_type");
	    String exCpbdueAcctType = wp.itemStr("ex_cpbdue_acct_type");
	    String exAcctStatus = wp.itemStr("ex_acct_status");
	    
	    if (this.chkStrend(exCpbdueBeginDate1, exCpbdueBeginDate2) == false) {
	        alertErr("[簽約日期-起迄]  輸入錯誤");
	        return false;
	      }
	    if (!empty(exIdCorpNo)) {
	      wp.whereStr += " and  id_corp_no = :exIdCorpNo ";
	      setString("exIdCorpNo", exIdCorpNo);
	    }
	    if (!empty(exCpbdueBeginDate1)) {
		    wp.whereStr += " and  cpbdue_begin_date >= :exCpbdueBeginDate1 ";
		    setString("exCpbdueBeginDate1", exCpbdueBeginDate1);
		}
	    if (!empty(exCpbdueBeginDate2)) {
		    wp.whereStr += " and  cpbdue_begin_date <= :exCpbdueBeginDate2 ";
		    setString("exCpbdueBeginDate2", exCpbdueBeginDate2);
		}
	    if (!empty(exCpbdueType)) {
		    wp.whereStr += " and  cpbdue_type = :exCpbdueType ";
		    setString("exCpbdueType", exCpbdueType);
		}
	    if (!empty(exCpbdueCurrType)) {
		    wp.whereStr += " and  cpbdue_curr_type = :exCpbdueCurrType ";
		    setString("exCpbdueCurrType", exCpbdueCurrType);
		}
	    if(!empty(exCpbdueAcctType)) {
	    	wp.whereStr += " and  cpbdue_acct_type = :exCpbdueAcctType ";
			setString("exCpbdueAcctType", exCpbdueAcctType);
	    }
	    if(!empty(exAcctStatus)) {
	    	wp.whereStr += " and (acno1.acct_status = :exAcctStatus ";
	    	wp.whereStr += " OR acno2.acct_status = :exAcctStatus ) ";
			setString("exAcctStatus", exAcctStatus);
	    }
	    return true;
	  }

	// ************************************************************************
	@Override
	public void queryRead() throws Exception {
		if(!getWhereStr()) 
			return;
		
		wp.pageControl();
		wp.selectSQL = "hex(col_cpbdue.rowid) as rowid "
				+ ", col_cpbdue.id_corp_no"
				+ ",decode(length(id_corp_no),10,uf_idno_name(cpbdue_id_p_seqno),uf_corp_name(id_corp_no)) as chi_name "
				+ ", col_cpbdue.cpbdue_acct_type"
				+ ", col_cpbdue.cpbdue_type" 
				+ ", col_cpbdue.cpbdue_curr_type"
				+ ", col_cpbdue.cpbdue_lst_upt_dat_dte" 
				+ ", COL_CPBDUE.mod_time as mod_time "
	  			+ ", col_cpbdue.CPBDUE_APPLY_MCODE as int_rate_mcode "
				+ ", acno1.status_change_date as status_change_date1 "
				+ ", acno1.acct_status as acct_status1 "
				+ ", acno2.status_change_date as status_change_date2 "
				+ ", acno2.acct_status as acct_status2 "

				;
		wp.daoTable = "COL_CPBDUE "
	                + " left join act_acno as acno1 on acno1.acct_type='01' "
					+ " and acno1.id_p_seqno in (select id_p_seqno from crd_idno where id_no = COL_CPBDUE.id_corp_no) "
	                + " left join act_acno as acno2 on acno2.acct_type='03' and acno2.acno_flag='2' "
					+ " and acno2.corp_p_seqno in (select corp_p_seqno from crd_corp where corp_no = COL_CPBDUE.id_corp_no) ";
		wp.whereOrder = " ";

		pageQuery();
		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.setPageValue();
		listWkdata();
	}
	
	void selectActAcno() {
		String idCorpNo = wp.itemStr("kk_id_corp_no");
		String idCorpType = wp.itemStr("kk_id_corp_type");
		String sqlStr = "";
		
		if(empty(idCorpNo) || empty(idCorpType))
			return;
		
		if("1".equals(idCorpType)) {
			sqlStr = "select acct_type,acct_status,status_change_date,int_rate_mcode from act_acno where acct_type='01' ";
			sqlStr += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = :id_corp_no ) ";
		}
		if("2".equals(idCorpType)) {
			sqlStr = "select acct_type,acct_status,status_change_date,int_rate_mcode from act_acno where acct_type='03' and acno_flag='2' ";
			sqlStr += " and corp_p_seqno in (select corp_p_seqno from crd_corp where corp_no = :id_corp_no) ";
		}
		setString("id_corp_no",idCorpNo);
		sqlSelect(sqlStr);
		if(sqlRowNum<=0) {
			errmsg(String.format("此%s不存在act_acno,無法新增", "01".equals(idCorpNo)?"身份證號":"統一編號"));
			return;
		}
		
		wp.colSet("acct_status",sqlStr("acct_status"));
		wp.colSet("status_change_date",sqlStr("status_change_date"));
		wp.colSet("int_rate_mcode",sqlStr("int_rate_mcode"));
		wp.colSet("cpbdue_acct_type",sqlStr("acct_type"));
	}
	
	void getAcctStatus(int i,String idCorpNo){

		if(idCorpNo.length()==10) {
			wp.colSet(i, "acct_status",wp.colStr( i, "acct_status1"));
			wp.colSet(i, "status_change_date",wp.colStr( i, "status_change_date1"));
		}else {
			wp.colSet(i, "acct_status",wp.colStr( i, "acct_status2"));
			wp.colSet(i, "status_change_date",wp.colStr( i, "status_change_date2"));
		}
		wp.colSet("kk_id_corp_type", idCorpNo.length()==10?"1":"2");
		
		return;
	}
	
	void getActJrnlBal(String idCorpNo,String cpbdueIdPSeqno) {
		double cpbdueBalanceAmt = 0;
		String sqlSelect = "SELECT SUM(A.ACCT_JRNL_BAL) as ACCT_JRNL_BAL FROM ACT_ACCT A, ACT_ACNO B WHERE A.P_SEQNO=B.P_SEQNO AND B.ACCT_TYPE='03' AND B.CORP_P_SEQNO = :CPBDUE_ID_P_SEQNO ";
		if(idCorpNo.length()==10) {
			sqlSelect = "SELECT A.ACCT_JRNL_BAL FROM ACT_ACCT A, ACT_ACNO B WHERE A.P_SEQNO=B.P_SEQNO AND B.ACCT_TYPE='01' AND B.ID_P_SEQNO = :CPBDUE_ID_P_SEQNO ";
		}
		setString("CPBDUE_ID_P_SEQNO",cpbdueIdPSeqno);
		sqlSelect(sqlSelect);
		if(sqlRowNum>0) {
			cpbdueBalanceAmt = sqlNum("ACCT_JRNL_BAL");
		}
		wp.colSet("cpbdue_balance_amt", cpbdueBalanceAmt);
		//alertErr("TEST="+cpbdueBalanceAmt);
	}
	
	void getAddr(String idCorpNo) {
		if(idCorpNo.length()==10) {
			String sqlSelect = "select MAIL_ZIP,MAIL_ADDR1,MAIL_ADDR2,MAIL_ADDR3,MAIL_ADDR4,MAIL_ADDR5,"
					+ "COMPANY_ZIP,COMPANY_ADDR1,COMPANY_ADDR2,COMPANY_ADDR3,COMPANY_ADDR4,COMPANY_ADDR5,"
					+ "RESIDENT_ZIP,RESIDENT_ADDR1,RESIDENT_ADDR2,RESIDENT_ADDR3,RESIDENT_ADDR4,RESIDENT_ADDR5,"
					+ "office_area_code1,office_tel_no1,office_tel_ext1,office_area_code2,office_tel_no2,office_tel_ext2,"
					+ "home_area_code1,home_tel_no1,home_tel_ext1,home_area_code2,home_tel_no2,home_tel_ext2 from crd_idno where id_no = :idNo";
			;
			setString("idNo",idCorpNo);
			sqlSelect(sqlSelect);
		}else {
			String sqlSelect = "select '' MAIL_ZIP, '' MAIL_ADDR1,'' MAIL_ADDR2,'' MAIL_ADDR3,'' MAIL_ADDR4,'' MAIL_ADDR5,"
					+ "REG_ZIP as COMPANY_ZIP,REG_ADDR1 as COMPANY_ADDR1,REG_ADDR2 as COMPANY_ADDR2,REG_ADDR3 as COMPANY_ADDR3,"
					+ "REG_ADDR4 as COMPANY_ADDR4,REG_ADDR5 as COMPANY_ADDR5,"
					+ "'' RESIDENT_ZIP,'' RESIDENT_ADDR1,'' RESIDENT_ADDR2,'' RESIDENT_ADDR3,'' RESIDENT_ADDR4,'' RESIDENT_ADDR5,"
					+ "CORP_TEL_ZONE1 as office_area_code1,CORP_TEL_NO1 as office_tel_no1,CORP_TEL_EXT1 as office_tel_ext1,"
					+ "CORP_TEL_ZONE2 as office_area_code2,CORP_TEL_NO2 as office_tel_no2,CORP_TEL_EXT2 as office_tel_ext2,"
					+ "'' home_area_code1,'' home_tel_no1,'' home_tel_ext1,'' home_area_code2,'' home_tel_no2,'' home_tel_ext2 from crd_corp where corp_no = :corpNo";
			;
			setString("corpNo",idCorpNo);
			sqlSelect(sqlSelect);
		}
		
		wp.colSet("MAIL_ZIP", sqlStr("MAIL_ZIP"));
		wp.colSet("MAIL_ADDR1", sqlStr("MAIL_ADDR1"));
		wp.colSet("MAIL_ADDR2", sqlStr("MAIL_ADDR2"));
		wp.colSet("MAIL_ADDR3", sqlStr("MAIL_ADDR3"));
		wp.colSet("MAIL_ADDR4", sqlStr("MAIL_ADDR4"));
		wp.colSet("MAIL_ADDR5", sqlStr("MAIL_ADDR5"));
		wp.colSet("COMPANY_ZIP", sqlStr("COMPANY_ZIP"));
		wp.colSet("COMPANY_ADDR1", sqlStr("COMPANY_ADDR1"));
		wp.colSet("COMPANY_ADDR2", sqlStr("COMPANY_ADDR2"));
		wp.colSet("COMPANY_ADDR3", sqlStr("COMPANY_ADDR3"));
		wp.colSet("COMPANY_ADDR4", sqlStr("COMPANY_ADDR4"));
		wp.colSet("COMPANY_ADDR5", sqlStr("COMPANY_ADDR5"));
		wp.colSet("RESIDENT_ZIP", sqlStr("RESIDENT_ZIP"));
		wp.colSet("RESIDENT_ADDR1", sqlStr("RESIDENT_ADDR1"));
		wp.colSet("RESIDENT_ADDR2", sqlStr("RESIDENT_ADDR2"));
		wp.colSet("RESIDENT_ADDR3", sqlStr("RESIDENT_ADDR3"));
		wp.colSet("RESIDENT_ADDR4", sqlStr("RESIDENT_ADDR4"));
		wp.colSet("RESIDENT_ADDR5", sqlStr("RESIDENT_ADDR5"));
		wp.colSet("office_area_code1", sqlStr("office_area_code1"));
		wp.colSet("office_tel_no1", sqlStr("office_tel_no1"));
		wp.colSet("office_tel_ext1", sqlStr("office_tel_ext1"));
		wp.colSet("office_area_code2", sqlStr("office_area_code2"));
		wp.colSet("office_tel_no2", sqlStr("office_tel_no2"));
		wp.colSet("office_tel_ext2", sqlStr("office_tel_ext2"));
		wp.colSet("home_area_code1", sqlStr("home_area_code1"));
		wp.colSet("home_tel_no1", sqlStr("home_tel_no1"));
		wp.colSet("home_tel_ext1", sqlStr("home_tel_ext1"));
		wp.colSet("home_area_code2", sqlStr("home_area_code2"));
		wp.colSet("home_tel_no2", sqlStr("home_tel_no2"));
		wp.colSet("home_tel_ext2", sqlStr("home_tel_ext2"));
		
	}
	
	void listWkdata() {
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			
//			wp.colSet(ii,"cpbdue_balance_amt", wp.colNum(ii,"cpbdue_total_amt") - wp.colNum(ii,"cpbdue_lst_payamt"));
			getAcctStatus(ii,wp.colStr(ii,"id_corp_no"));
			if (wp.respHtml.indexOf("_detl") > 0) {
				getAddr(wp.colStr("id_corp_no"));
				getActJrnlBal(wp.colStr("id_corp_no"),wp.colStr("cpbdue_id_p_seqno"));
				wp.colSet("card_principal", wp.colNum(ii,"cpbdue_amt"));
				//double onlineComputing4 = wp.colNum(ii,"cpbdue_due_card_amt") * (wp.colNum(ii,"cpbdue_period") - wp.colNum(ii,"online_computing_2"));
				
				//當計算之已繳期數大於合約期數，以合約期數呈現。
				if( wp.colNum(ii,"onlineComputing2") >= wp.colNum(ii,"cpbdue_period"))
					wp.colSet("online_computing_2", wp.colNum(ii,"cpbdue_period"));
				
				double onlineComputing4 = (wp.colNum(ii,"cpbdue_due_card_amt") * (wp.colNum(ii,"cpbdue_period")) - wp.colNum(ii,"cpbdue_total_payamt"));
				wp.colSet("online_computing_4", onlineComputing4);
			}
		}
	}
	
	void checkIdCorpNo() throws Exception{
		this.msgOK();
		String kkIdCorpNo = wp.itemStr("kk_id_corp_no");
		String cpbdueIdPSeqno = "";
		String sqlSelect = "select corp_p_seqno as cpbdue_id_p_seqno,chi_name from crd_corp where corp_no = :kkIdCorpNo";
		if(kkIdCorpNo.length()==10) {
			sqlSelect = "select id_p_seqno as cpbdue_id_p_seqno,chi_name from crd_idno where id_no = :kkIdCorpNo";
		}
		setString("kkIdCorpNo",kkIdCorpNo);
		sqlSelect(sqlSelect);
		if(sqlRowNum<=0) {
			errmsg(String.format("查無此%s,無法新增", kkIdCorpNo.length()==10?"身份證號":"統一編號"));
			return;
		}
		cpbdueIdPSeqno = sqlStr("cpbdue_id_p_seqno");
		String chiName = sqlStr("chi_name");
		wp.colSet("cpbdue_id_p_seqno",cpbdueIdPSeqno);
		wp.colSet("id_corp_no", kkIdCorpNo);
		wp.colSet("chi_name", chiName);
		selectActAcno();
		getAddr(kkIdCorpNo);
		getActJrnlBal(kkIdCorpNo,cpbdueIdPSeqno);
	}
	
	void countAmt() {
		double cpbdueRate = wp.itemNum("cpbdue_rate");//利率
		int cardPrincipal = (int)wp.itemNum("card_principal");//信用卡本金
		int cpbduePeriod = (int)wp.itemNum("cpbdue_period");//期數
		if(cardPrincipal == 0 || cpbduePeriod == 0) {
//		if(cpbdueRate == 0 || cardPrincipal == 0 || cpbduePeriod == 0) {
//			if(cpbdueRate==0) wp.colSet("cpbdue_rate_pink","style=background-color:pink");
			if(cardPrincipal==0) wp.colSet("card_principal_pink","style=background-color:pink");
			if(cpbduePeriod==0) wp.colSet("cpbdue_period_pink","style=background-color:pink");
			alertErr2("您輸入的資料有誤，請輸入本金(A)、利率(B)、期數(D)！");
			return;
		}
		
		if(cpbdueRate==0) cpbdueRate=0.00000001; //為了讓0利率可以試算
		
		double ei1 = Math.pow((1+cpbdueRate/12/100),cpbduePeriod);
		double ei2 = ei1 * (cpbdueRate/12/100);
		double ei3 = ei1 -1;
		double ei = ei2/ei3;
		double avpayP =  ei*cardPrincipal;//每月應付本息 
		double avpayI = 0;
		double avpayM = 0;
		double totalP = 0;
		double totalI = 0;
		double totalM = 0;
		double remP = cardPrincipal;
		cpbduePeriod = cpbduePeriod + 1;
		wp.listCount[0] = cpbduePeriod;
		for (int i = 0 ;i<cpbduePeriod;i++) {
			if(i == 0) {
				wp.colSet(i,"SER_NUM", String.format("%02d", i));
				continue;
			}
			//每月應付利息
//			avpayI = Math.round(remP*cpbdueRate/12/100);
			avpayI = remP*cpbdueRate/12/100;
			//每月應付本金
			avpayM =  avpayP - avpayI;
			
			remP = remP - avpayM;
			
//			totalP += avpayP;
			totalI += avpayI;
			totalM += avpayM;
			
			wp.colSet(i,"SER_NUM", String.format("%02d", i));
			wp.colSet(i,"avpay_m", avpayM);
			wp.colSet(i,"avpay_i", avpayI);
			wp.colSet(i,"avpay_p", avpayP);
			
			if(wp.itemNum("online_computing_2") == i) {
				wp.colSet(i,"background_yellow", "style=background-color:yellow");
			}
			
		}
		
		double totalSub = cardPrincipal; 
		for (int i = 0 ;i<cpbduePeriod;i++) {
			if(i == 0) {
				wp.colSet(i,"total_sub", totalSub);
				continue;
			}
			totalSub = totalSub - wp.colNum(i,"avpay_m");
			wp.colSet(i,"total_sub", totalSub);
		}
		wp.colSet("total_m", totalM);
		wp.colSet("total_i", totalI);
		wp.colSet("total_p", totalM+totalI+totalSub);
		wp.colSet("cpbdue_due_card_amt_tmp", Math.round(avpayP));
		wp.colSet("list_count", cpbduePeriod);
		wp.colSet("dialog_falg", "Y");
		wp.colSet("countamt_falg", "Y");
	}
	
	void countPeriod() {
		double cpbdueRate = wp.itemNum("cpbdue_rate");//利率
		int cardPrincipal = (int)wp.itemNum("card_principal");//信用卡本金
		int cpbdueDueCardAmt = (int)wp.itemNum("cpbdue_due_card_amt");//每月應繳金額
		//if(cpbdueRate == 0 || cardPrincipal == 0 || cpbdueDueCardAmt == 0) {
		if(cardPrincipal == 0 || cpbdueDueCardAmt == 0) {
			if(cpbdueRate==0) wp.colSet("cpbdue_rate_pink","style=background-color:pink");
			if(cardPrincipal==0) wp.colSet("card_principal_pink","style=background-color:pink");
			if(cpbdueDueCardAmt==0) wp.colSet("cpbdue_due_card_amt_pink","style=background-color:pink");
			alertErr2("您輸入資料有誤，請輸入本金(A)、利率(B)、月付金額(C)！");
			return;
		}
		
		double avpayP = 0;
		int cpbduePeriod = 1 ;
		
		if(cpbdueRate==0) cpbdueRate=0.00000001; //為了讓0利率可以試算
		
		do {
			double ei1 = Math.pow((1+cpbdueRate/12/100),cpbduePeriod++);
			double ei2 = ei1 * (cpbdueRate/12/100);
			double ei3 = ei1 -1;
			double ei = ei2/ei3;
			avpayP =  Math.round(ei*cardPrincipal);
		}while(avpayP > cpbdueDueCardAmt);
		
		double avpayI = 0;
		double avpayM = 0;
		double totalP = 0;
		double totalI = 0;
		double totalM = 0;
		double remP = cardPrincipal;
		avpayP = cpbdueDueCardAmt;
		for (int i = 0 ;i<cpbduePeriod;i++) {
			if(i == 0) {
				wp.colSet(i,"SER_NUM", String.format("%02d", i));
				continue;
			}
			//每月應付利息
			avpayI = Math.round(remP*cpbdueRate/12/100);
			//每月應付本金
			avpayM = avpayP - avpayI;
			
			remP = remP - avpayM;
			
//			totalP += avpayP;
			totalI += avpayI;
			totalM += avpayM;
			
			wp.colSet(i,"SER_NUM", String.format("%02d", i));
			wp.colSet(i,"avpay_m", avpayM);
			wp.colSet(i,"avpay_i", avpayI);
			wp.colSet(i,"avpay_p", avpayP);
			
			if(wp.itemNum("online_computing_2") == i) {
				wp.colSet(i,"background_yellow", "style=background-color:yellow");
			}
		}
		
		double totalSub = cardPrincipal; 
		for (int i = 0 ;i<cpbduePeriod;i++) {
			if(i == 0) {
				wp.colSet(i,"total_sub", totalSub);
				continue;
			}
			totalSub = totalSub - wp.colNum(i,"avpay_m");
			wp.colSet(i,"total_sub", totalSub);
		}
		wp.colSet(cpbduePeriod-1,"total_sub2", String.format("尾期金額:%,.0f", totalSub+cpbdueDueCardAmt));
		wp.colSet("total_sub2_tmp", (totalSub<0)?String.format("%.0f", totalSub+cpbdueDueCardAmt):String.format("%.0f", totalSub));
		wp.listCount[0] = cpbduePeriod;
		wp.colSet("total_m", totalM);
		wp.colSet("total_i", totalI);
		wp.colSet("total_p", totalM+totalI+totalSub);
		wp.colSet("list_count", cpbduePeriod);
		wp.colSet("dialog_falg", "Y");
		wp.colSet("countperiod_falg", "Y"); 
		wp.colSet("cpbdue_period_tmp", cpbduePeriod - 1);
//		wp.colSet("cpbdue_period", cpbduePeriod);
	}
	
	public void countOnlineComputing1(){	
		String cpbduePayMonthDte = wp.colStr("cpbdue_pay_month_dte");
		String cpbdueAmtExpDte = wp.colStr("cpbdue_amt_exp_dte");
		int cpbduePeriod = wp.colInt("cpbdue_period");
		//if(wp.colEq("cpbdue_curr_type","3") && !empty(cpbduePayMonthDte) && !empty(cpbdueAmtExpDte)){
		if(!empty(cpbduePayMonthDte) && !empty(cpbdueAmtExpDte)){
			CommString comms = new CommString();
			String sqlCmd = " select left(business_date,6) as busYM ,right(business_date,2) as busD from ptr_businday ";
			sqlSelect(sqlCmd);
			String busYM = sqlStr("busYM");
			String busD = sqlStr("busD");
			int onlineComputing1 = 0;
		
			onlineComputing1 = comms.strToInt(busYM) - comms.strToInt(comms.left(cpbdueAmtExpDte, 6));
			
//			//首期繳款日與營業日若為同一個月份,日期未達繳款日時則-1
//			if(busYM.equals(comms.left(cpbdueAmtExpDte, 6)) && comms.strToInt(busD) < comms.strToInt(cpbduePayMonthDte)) {
//				onlineComputing1 = onlineComputing1 - 1;
//			}
			
			//與營業日期已達繳款日時則+1
			if(comms.strToInt(busD) > comms.strToInt(cpbduePayMonthDte)) {
				onlineComputing1 = onlineComputing1 + 1;
			}
			
//			alertErr2("busYM, key= " + "[" + comms.strToInt(busYM) + "]");
//			alertErr2("cpbdueAmtExpDte(YM), key= " + "[" + comms.left(cpbdueAmtExpDte, 6) + "]");
			
			//當累計繳滿期數為負值時設為0
			if(onlineComputing1 < 0) onlineComputing1= 0;
						
			if(onlineComputing1>= cpbduePeriod) onlineComputing1=cpbduePeriod;		
				
			wp.colSet("online_computing_1", onlineComputing1);
		}
	}

	// ************************************************************************
	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	// ************************************************************************
	@Override
	public void dataRead() throws Exception {
		
		String dataK1 = itemKk("data_k1");
		
		wp.selectSQL = " hex(COL_CPBDUE.rowid) as rowid, COL_CPBDUE.*"
				      + ",to_char(COL_CPBDUE.mod_time,'yyyymmdd') as mod_date "
				      + ",decode(length(id_corp_no),10,uf_idno_name(cpbdue_id_p_seqno),uf_corp_name(id_corp_no)) as chi_name "				      
				      + ",case when cpbdue_total_payamt>0 AND cpbdue_due_card_amt>0 then int(cpbdue_total_payamt/cpbdue_due_card_amt) ELSE '0' END as online_computing_2 "
				      + ",case when cpbdue_total_payamt>0 AND cpbdue_due_card_amt>0 AND int(cpbdue_total_payamt/cpbdue_due_card_amt)>=3 then 'Y' else 'N' end as online_computing_3 "
//					  + ",case when cpbdue_total_amt>0 AND cpbdue_total_payamt>0 then cpbdue_total_amt-cpbdue_total_payamt else '0' end as online_computing_4 "
					  + ",cpbdue_curr_type as ori_cpbdue_curr_type "
		  			  + ",cpbdue_bank_type as ori_cpbdue_bank_type "
		  			  + ",cpbdue_tcb_type as ori_cpbdue_tcb_type "
		  			  + ",cpbdue_medi_type as ori_cpbdue_medi_type "
		  			  + ",CPBDUE_APPLY_MCODE as int_rate_mcode "
					  + ", acno1.status_change_date as status_change_date1 "
					  + ", acno1.acct_status as acct_status1 "
					  + ", acno2.status_change_date as status_change_date2 "
				      + ", acno2.acct_status as acct_status2 "
				      + ", idno.CELLAR_PHONE "
					  ;
		wp.daoTable = "COL_CPBDUE "
		           + " left join act_acno as acno1 on acno1.acct_type='01' "
				   + " and acno1.id_p_seqno in (select id_p_seqno from crd_idno where id_no = COL_CPBDUE.id_corp_no) "
			       + " left join act_acno as acno2 on acno2.acct_type='03' and acno2.acno_flag='2' "
				   + " and acno2.corp_p_seqno in (select corp_p_seqno from crd_corp where corp_no = COL_CPBDUE.id_corp_no) "
			       + " left join crd_idno idno on COL_CPBDUE.id_corp_no = idno.id_no ";
		wp.whereStr = "where hex(COL_CPBDUE.rowid) = :rowid ";
		setString("rowid",dataK1);

		pageSelect();
		if (sqlNotFind()) {
			alertErr2("查無資料, key= " + "[" + dataK1 + "]");
			return;
		}
		listWkdata();
		countOnlineComputing1();
	}

	// ************************************************************************
	public void saveFunc() throws Exception {
		colm01.Colm1000Func func = new colm01.Colm1000Func(wp);
		func.setConn(wp);
	    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
	        return;
	    }
		rc = func.dbSave(strAction);
		if (rc != 1)
			alertErr2(func.getMsg());
		log(func.getMsg());
		this.sqlCommit(rc);
	}

	// ************************************************************************
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	// ************************************************************************
	@Override
	public void dddwSelect() {
		   try {
//		    wp.initOption = "--";
//		    wp.optionKey = wp.itemStr("ex_acct_type");
//			this.dddwList("dddw_ptr_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 ");			
		} catch (Exception e) {
		}
	}

	// ************************************************************************
	@Override
	public void initPage() {
		wp.colSet("list_count", 0);
		return;
	}
	// ************************************************************************

} // End of class
