/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  110/08/31  V1.00.01    Ryan      update mantis 0008548                     *
*  112/03/31  V1.00.02    Zuwei Su  naming rule update                        *
*  112/04/16  V1.00.03    Simon     callbatch changed into online proc        *
*  112/04/17  V1.00.04    Simon     getPseqnobyAcctKey() get acno_flag error fixed*
*  112/08/24  V1.00.05    Simon     新增檢核報送理由選擇為02時，結案註記須為Y,U,W三者之一*
*  112/09/16  V1.00.06    Simon     新增維護臨調額度                          *
*  112/10/30  V1.00.07    Simon     move act_jcic_log.stmt_last_payday to act_jcic_cmp.stmt_last_payday*
******************************************************************************/

package colm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

import java.util.ArrayList;
import java.util.Arrays;

public class Colm0250 extends BaseEdit {
  CommString zzStr = new CommString();
	Colm0250Func func;
	
	String kkPSeqno="";
	String kkAcctMonth="",kkStmtCycle="";
	String kkAcnoFlag="";

  String hBusiBusinessDate = "";

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) { 
            //-資料讀取- 
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {
            /* 新增功能 */
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
        	updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
            deleteFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page*/
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "KK4")) {
        	/* KK4 產生處理 */
        	ofcDbinsert();
        }

        dddwSelect();
        initButton();
    }
    
    @Override
    public void initPage() {
		wp.colSet("db_proc_mm", 1);
	}
    
    @Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("kk_acct_type");
			this.dddwList("PtrAcctTypeList", "ptr_acct_type", "acct_type", "acct_type||' ['||chin_name||']'", "where 1=1 order by acct_type ");
			
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("report_reason");
			this.dddwList("PtrSysIdtabList", "ptr_sys_idtab", "wf_id", "wf_id||' ['||wf_desc||']'", "where wf_type = 'KK4_REPORT_REASON' order by wf_type, wf_id ");	
		}
		catch(Exception ex) {}
	}
 	
    boolean getWhereStr() throws Exception {
 		String lsDate1 = wp.itemStr("exDateS");
		String lsDate2 = wp.itemStr("exDateE");
		if (this.chkStrend(lsDate1, lsDate2) == false) {
			alertErr("[覆核日期-起迄]  輸入錯誤");
			return false;
		}
//		if ((empty(wp.item_ss("exAcctKey"))==false) && (wp.item_ss("exAcctkey").length()<6)) {
//			err_alert("[帳戶號碼]輸入至少6碼!");
//			return false;
//		}
		
		wp.whereStr = "where 1=1 "
					+ "and act_jcic_cmp.acct_type = act_acno.acct_type "
//					+ "and act_jcic_cmp.p_seqno = act_acno.p_seqno "
					+ "and act_jcic_cmp.p_seqno = act_acno.acno_p_seqno "
					;
		if(empty(wp.itemStr("exDateS")) == false){
			wp.whereStr += " and act_jcic_cmp.apr_date >= :apr_dates ";
			setString("apr_dates", wp.itemStr("exDateS"));
		}
		if(empty(wp.itemStr("exDateE")) == false){
			wp.whereStr += " and act_jcic_cmp.apr_date <= :apr_datee ";
			setString("apr_datee", wp.itemStr("exDateE"));
		}
		if(empty(wp.itemStr("exAcctKey")) == false){
			wp.whereStr += " and act_acno.acct_key like :acct_key ";
			setString("acct_key", wp.itemStr("exAcctKey")+"%");
		}
		if(empty(wp.itemStr("exCrtUser")) == false){
			wp.whereStr += " and act_jcic_cmp.crt_user = :crt_user ";
			setString("crt_user", wp.itemStr("exCrtUser"));
		}
		
		wp.whereOrder ="";
		wp.queryWhere = wp.whereStr;
		
		return true;
 	}
    
    @Override
    public void queryFunc() throws Exception {
    	wp.setQueryMode();
		queryRead();
	}
    
	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		
		if (getWhereStr() == false)
			return;
		
		wp.selectSQL = "hex(act_jcic_cmp.rowid) as rowid, " 
				+ "act_jcic_cmp.p_seqno, "
				+ "act_acno.acct_type, "
				+ "act_acno.acct_key, "
			//+ "act_jcic_cmp.id_no, "
			//+ "act_jcic_cmp.id_code, "
			  + "uf_idno_id(act_jcic_cmp.id_p_seqno) as id_no, "
        + "(select id_no_code from crd_idno where crd_idno.id_p_seqno = act_jcic_cmp.id_p_seqno fetch first 1 rows only) as id_code, "
				+ "act_jcic_cmp.corp_no, "
				+ "act_jcic_cmp.stmt_cycle_date, "
				+ "act_jcic_cmp.line_of_credit_amt, "
				+ "act_jcic_cmp.bin_type, "
				+ "act_jcic_cmp.crt_user, "
				+ "act_jcic_cmp.crt_date, "
				+ "act_jcic_cmp.jcic_remark, "
				+ "act_jcic_cmp.acct_month, "
				+ "act_jcic_cmp.apr_date, "
				+ "act_jcic_cmp.sub_log_type "
				;

		wp.daoTable = "act_jcic_cmp, act_acno ";
		
		pageQuery();
		wp.setListCount(1);

		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		listWkdata();
		wp.setPageValue();
	}
    
    void listWkdata() {
		String ss = "";
		String[] cde=new String[]{"A","C","D"};
		String[] txt=new String[]{"A.新增","C.異動","D.刪除"};

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss =wp.colStr(ii,"sub_log_type");
			wp.colSet(ii,"tt_sub_log_type", zzStr.decode(ss, cde, txt));
		}
	}
    
    
  /***********************************************************************/
  int getPseqnobyAcctKey() throws Exception {
    String lsAcctType, lsAcctKey;

  	//以輸入欄位優先查詢
    lsAcctType = wp.itemStr("kk_acct_type");
    lsAcctKey  = wp.itemStr("kk_acct_key");
    if (empty(lsAcctType) && empty(lsAcctKey)) {
	    lsAcctType = wp.itemStr("acct_type");
 		  lsAcctKey  = wp.itemStr("acct_key");
  	}

    if (empty(lsAcctType)) {
  		alertErr("帳戶號碼(type) 不可空白");
	    return -1;
  	}
    if (empty(lsAcctKey)) {
  		alertErr("帳戶號碼(key) 不可空白");
	    return -1;
  	}

    if (lsAcctType.equals("01")) {
      lsAcctKey = fillZeroAcctKey(lsAcctKey);
    }
    if (lsAcctKey.length()<8) {
  		alertErr("[帳戶號碼]輸入至少8碼!");
	    return -1;
 	  }

  //由act_acno取得相關欄位值，以便頁面顯示
    String lsSql = "select p_seqno, acct_type, acct_key, id_p_seqno, corp_p_seqno, stmt_cycle "
  //String ls_sql = "select acno_p_seqno as p_seqno, acct_type, acct_key, id_p_seqno, corp_p_seqno, stmt_cycle "
						+ " ,uf_idno_id(id_p_seqno) as id_no ,uf_corp_no(corp_p_seqno) as corp_no "
					//+ " acct_holder_id, acct_holder_id_code " //No column
						+ " ,acno_flag "
						+ " from act_acno "
						+ " where acct_type = :acct_type and acct_key = :acct_key ";
		setString("acct_type", lsAcctType);
		setString("acct_key", lsAcctKey);
		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			alertErr("無帳戶帳號資料，帳戶帳號:[ " + lsAcctType + " - " + lsAcctKey + "]");
			return -1;
		} else {
			kkPSeqno = sqlStr("p_seqno");
			wp.colSet("id_no", sqlStr("id_no"));
			wp.colSet("corp_no", sqlStr("corp_no"));
			kkStmtCycle = sqlStr("stmt_cycle");
			kkAcnoFlag = sqlStr("acno_flag");
			return 1;
		}
	}

  @Override
  public void querySelect() throws Exception {
   	kkPSeqno    = wp.itemStr("data_k1");
		kkAcctMonth = wp.itemStr("data_k2");
   	dataRead();
  }

  @Override
	public void dataRead() throws Exception {
    if (empty(kkPSeqno)) {
			if (getPseqnobyAcctKey() < 0) return;
		}
    	
		if (empty(kkAcctMonth)){
			kkAcctMonth = itemKk("acct_month");
			if (empty(kkAcctMonth)) {
				alertErr("請輸入 帳務年月");	
				return;
			}
		}
		
		wp.selectSQL = "p_seqno, "
				   + "id_p_seqno, "
			       + "acct_type, "
			       + "uf_acno_key(p_seqno) as acct_key, "
			       + "corp_id_p_seqno, "
			       + "stmt_cycle, "
			       + "corp_no, "
			     //+ "id_no, "
			     //+ "id_code, "
			       + "uf_idno_id(id_p_seqno) as id_no, "
             + "(select id_no_code from crd_idno where crd_idno.id_p_seqno = act_jcic_cmp.id_p_seqno fetch first 1 rows only) as id_code, "
			       + "stmt_cycle_date, "
			       + "line_of_credit_amt, "
			       + "temp_of_credit_amt, "
			       + "stmt_last_payday, "
			       + "bin_type, "
			       + "cash_lmt_balance, "
			       + "cashadv_limit, "
			       + "stmt_this_ttl_amt, "
			       + "stmt_mp, "
			       + "billed_end_bal_bl, "
			       + "billed_end_bal_it, "
			       + "billed_end_bal_id, "
			       + "billed_end_bal_ot, "
			       + "billed_end_bal_ca, "
			       + "billed_end_bal_ao, "
			       + "billed_end_bal_af, "
			       + "billed_end_bal_lf, "
			       + "billed_end_bal_pf, "
			       + "billed_end_bal_ri, "
			       + "billed_end_bal_pn, "
			       + "ttl_amt_bal, "
			       + "bill_interest, "
			       + "stmt_adjust_amt, "
			       + "unpost_inst_fee, "
			       + "unpost_card_fee, "
			       + "stmt_last_ttl, "
			       + "payment_amt_rate, "
			       + "payment_time_rate, "
			       + "stmt_payment_amt, "
			       + "jcic_acct_status, "
			       + "jcic_acct_status_flag, "
			       + "bill_type_flag, "
			       + "proc_date, "
			       + "proc_flag, "
			       + "status_change_date, "
			       + "last_payment_date, "
			       + "last_min_pay_date, "
			       + "debt_close_date, "
			       + "sale_date, "
			       + "jcic_remark, "
			       + "npl_corp_no, "
			       + "crt_user, "
			       + "crt_date, "
			       + "apr_flag, "
			       + "apr_date, "
			       + "apr_user, "
			       + "mod_user, "
			       + "mod_time, "
			       + "mod_pgm, "
//			       + "mod_ws, "
//			       + "mod_log, "
			       + "mod_seqno, "
			       + "rowid rowid, "
			       + "proc_desc, "
			       + "acct_month, "
//			       + "1 db_proc_mm, "
			       + "sub_log_type, "
			       + "report_reason "
				 ;
				 
		wp.daoTable = "act_jcic_cmp";
		wp.whereStr = "where p_seqno = :p_seqno ";
		wp.whereStr += "and acct_month = :acct_month ";
		setString("p_seqno", kkPSeqno);
		setString("acct_month", kkAcctMonth);
		wp.whereOrder = "";
		
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, p_seqno="+kkPSeqno+", acct_month="+kkAcctMonth);
		}
		listWkdataDetl();
	}

  void listWkdataDetl() throws Exception {
   	String ss = "";
   	ss =wp.colStr("report_reason");
   	wp.itemSet("report_reason", ss);
  //wp.item_set("kk_acct_type", wp.col_ss("acct_type"));
   	dddwSelect();
    	
  //wp.col_set("id_corp_no_readonly", "readOnly");
  }

  @Override
  public void saveFunc() throws Exception {
   	func = new Colm0250Func(wp);

    if (ofValidation()<0) return;
        
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr(func.getMsg());
    }
    this.sqlCommit(rc);
  }
    
  int ofValidation() throws Exception {
//    	<<前端做>>
//    	if dw_data.of_getitem(1,'apr_flag')='Y' then
//    		顯示確認訊息:【主管已覆核, 是否異動資料】<>1 then Return 0
//    	end if
    	
//    	<<前端做>>
//    	//檢查【交易代碼(sub_log_type)】
//    	if POS("|A|C|D",dw_data.of_getitem(1,'sub_log_type'))=0 then
//    		顯示錯誤訊息:【須指定 交易代碼】
//    		Return -1
//    	end if

//    	<<前端做>>
//    	//檢查【資料報送理由碼(report_reaso)】
//    	if dw_data.item(1,'n')='11' then
//    		if of_excmsg("資料報送理由碼為[本行作業疏失], 是否存檔")<>1 then Return -1
//    	end if
    	//檢查【理由碼(report_reason)及結案註記jcic_acct_status_flag須為Y,U,W三者之一】
   	  String s1 = "", s2 = "";
      s1 = wp.itemStr2("report_reason");
      s2 = wp.itemStr2("jcic_acct_status_flag");
			if ( s1.equals("02") ) {
			  if ( !Arrays.asList("Y","U","W").contains(s2) ) {
			    alertErr("理由碼為02報送簡要版時結案註記須為Y,U,W三者之一");	
    		  return -1;
			  }
			}
    	
    	//執行【刪除】，需進行線上主管覆核
//    	if of_excmsg("是否請主管覆核")=1 then
    	String lsNeedSign = wp.itemStr("needSign");
    	func.varsSet("needSign", lsNeedSign);
    	if (strAction.equals("U") || strAction.equals("D")) {
    		if (lsNeedSign.equals("Y")) {
    			//-check approve- 主管覆核
    		    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
    				return -1;
    			}
    		}
		}
		
		return 1;
	}
    
  void ofcDbinsert() throws Exception {
   	String lsPSeqno;
   	int llCnt;
  //int llProcMm;
   	String lsAcctMonth, lsAcctMonth2, lsStmtCycle;
    	
 	//頁面欄位檢核
 		lsAcctMonth = wp.itemStr("kk_acct_month");
 		kkAcctMonth = lsAcctMonth;

		if (empty(lsAcctMonth)) {
			alertErr("請輸入 帳務年月");	
			return;
		}
/***
		llProcMm = (int) wp.itemNum("db_proc_mm");
		if (llProcMm<=0 || llProcMm>12 ) {
			alertErr("請輸入KK4產生月數, 01..12");	
			return;
		}
***/
    	
		if (getPseqnobyAcctKey() < 0) return;

		if (kkAcnoFlag.equals("3")) {
			alertErr("請輸入 公司戶帳號");	
			return;
		}

		lsPSeqno = kkPSeqno;
		lsStmtCycle = kkStmtCycle;
		
		//-acct_month-
		String lsSql = "select this_acct_month from ptr_workday "
				+ " where stmt_cycle = :stmt_cycle ";
		setString("stmt_cycle", lsStmtCycle);
		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			alertErr("無法取得帳務週期關帳年月, cycle=["+lsStmtCycle+"]");
			return;
		} else {
			lsAcctMonth2 = sqlStr("this_acct_month");
		}
		if (chkStrend(lsAcctMonth, lsAcctMonth2) == false) {
			alertErr("帳務年月不可大於帳戶之關帳年月");
			return;
		}
		
		//-business_date-
		lsSql = "select business_date from ptr_businday "
				  + " fetch first 1 row only ";
		sqlSelect(lsSql);
		if (sqlRowNum <= 0) {
			alertErr("無法取得營業日期");
			return;
		} else {
			hBusiBusinessDate = sqlStr("business_date");
		}
		
		lsSql = "select count(*) as ll_cnt from act_jcic_cmp "
				+ " where p_seqno = :p_seqno "
				+ " and acct_month = :acct_month ";
		setString("p_seqno", lsPSeqno);
		setString("acct_month", lsAcctMonth);
		sqlSelect(lsSql);
		llCnt = (int) sqlNum("ll_cnt");
		if (llCnt > 0) {
			alertErr("卡人及帳務年月已有資料");
			return;
		}
		
		lsSql = "select count(*) as ll_cnt from act_jcic_log "
				+ "where p_seqno = :p_seqno "
				+ " and acct_month = :acct_month "
				+ " and log_type = 'A' ";
		setString("p_seqno", lsPSeqno);
		setString("acct_month", lsAcctMonth);
		sqlSelect(lsSql);
		llCnt = (int) sqlNum("ll_cnt");
		if (llCnt <= 0) {
			alertErr("無法取得報送來源資料");
			return;
		}
		
	//fCallBatch("ActN017", lsPSeqno+" "+lsAcctMonth+" "+llProcMm);

	  fGetBatchProc();
		
    }
    
/***
  int fCallBatch(String asPgname, String asParm) throws Exception{
		ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
		rc = batch.callBatch(asPgname+" "+asParm);
		if (rc != 1) {
			alertErr("KK4 資料產生處理: callbatch 失敗");
			return -1;
		}
		alertMsg("KK4 資料產生處理(ActN017): callbatch 成功, 處理序號: " + batch.batchSeqno());
		
		return 1;
	}
***/

  /***********************************************************************/
  int fGetBatchProc() throws Exception {

    if(selectActJcicLog0() == -1) {
      return -1;
    }

   	func = new Colm0250Func(wp);

	//func.varsSet("sub_log_type", sqlStr("ajlg.sub_log_type"));
		func.varsSet("p_seqno", kkPSeqno);
		func.varsSet("acct_month", kkAcctMonth);

		func.varsSet("id_p_seqno", sqlStr("ajlg.id_p_seqno"));
		func.varsSet("acct_type", sqlStr("ajlg.acct_type"));
		func.varsSet("corp_id_p_seqno", sqlStr("ajlg.corp_id_p_seqno"));
		func.varsSet("stmt_cycle", sqlStr("ajlg.stmt_cycle"));
		func.varsSet("corp_no", sqlStr("ajlg.corp_no"));
		func.varsSet("stmt_cycle_date", sqlStr("ajlg.stmt_cycle_date"));
		func.varsSet("line_of_credit_amt", ""+sqlNum("ajlg.line_of_credit_amt"));
		func.varsSet("temp_of_credit_amt", ""+sqlNum("ajlg.temp_of_credit_amt"));
		func.varsSet("cca_temp_credit_amt", ""+sqlNum("ajlg.cca_temp_credit_amt"));
		func.varsSet("cca_adj_eff_start_date", sqlStr("ajlg.cca_adj_eff_start_date"));
		func.varsSet("cca_adj_eff_end_date", sqlStr("ajlg.cca_adj_eff_end_date"));
		func.varsSet("stmt_last_payday", sqlStr("ajlg.stmt_last_payday"));
		func.varsSet("bin_type", sqlStr("ajlg.bin_type"));
	//func.varsSet("cash_lmt_balance", ""+sqlNum("ajlg.cash_lmt_balance"));Fancy 預借現金額度
		func.varsSet("cashadv_limit", ""+sqlNum("ajlg.cashadv_limit"));
		func.varsSet("stmt_this_ttl_amt", ""+sqlNum("ajlg.stmt_this_ttl_amt"));
		func.varsSet("stmt_mp", ""+sqlNum("ajlg.stmt_mp"));
		func.varsSet("billed_end_bal_bl", ""+sqlNum("ajlg.billed_end_bal_bl"));
		func.varsSet("billed_end_bal_it", ""+sqlNum("ajlg.billed_end_bal_it"));
		func.varsSet("billed_end_bal_id", ""+sqlNum("ajlg.billed_end_bal_id"));
		func.varsSet("billed_end_bal_ot", ""+sqlNum("ajlg.billed_end_bal_ot"));
		func.varsSet("billed_end_bal_ca", ""+sqlNum("ajlg.billed_end_bal_ca"));
		func.varsSet("billed_end_bal_ao", ""+sqlNum("ajlg.billed_end_bal_ao"));
		func.varsSet("billed_end_bal_af", ""+sqlNum("ajlg.billed_end_bal_af"));
		func.varsSet("billed_end_bal_lf", ""+sqlNum("ajlg.billed_end_bal_lf"));
		func.varsSet("billed_end_bal_pf", ""+sqlNum("ajlg.billed_end_bal_pf"));
		func.varsSet("billed_end_bal_ri", ""+sqlNum("ajlg.billed_end_bal_ri"));
		func.varsSet("billed_end_bal_pn", ""+sqlNum("ajlg.billed_end_bal_pn"));
		func.varsSet("ttl_amt_bal", ""+sqlNum("ajlg.ttl_amt_bal"));
		func.varsSet("bill_interest", ""+sqlNum("ajlg.bill_interest"));
		func.varsSet("stmt_adjust_amt", ""+sqlNum("ajlg.stmt_adjust_amt"));
		func.varsSet("unpost_inst_fee", ""+sqlNum("ajlg.unpost_inst_fee"));
		func.varsSet("unpost_card_fee", ""+sqlNum("ajlg.unpost_card_fee"));
		func.varsSet("stmt_last_ttl", ""+sqlNum("ajlg.stmt_last_ttl"));
		func.varsSet("payment_amt_rate", sqlStr("ajlg.payment_amt_rate"));
		func.varsSet("payment_time_rate", sqlStr("ajlg.payment_time_rate"));
		func.varsSet("stmt_payment_amt", ""+sqlNum("ajlg.stmt_payment_amt"));
		func.varsSet("jcic_acct_status", sqlStr("ajlg.jcic_acct_status"));
		func.varsSet("jcic_acct_status_flag", sqlStr("ajlg.jcic_acct_status_flag"));
		func.varsSet("bill_type_flag", sqlStr("ajlg.bill_type_flag"));
		func.varsSet("proc_date", hBusiBusinessDate);
    String tmpstr = String.format("本資料由[%s]送JCIC轉入", kkAcctMonth);
		func.varsSet("proc_desc", tmpstr);
		func.varsSet("status_change_date", sqlStr("ajlg.status_change_date"));
		func.varsSet("last_payment_date", sqlStr("ajlg.last_payment_date"));
		func.varsSet("last_min_pay_date", sqlStr("ajlg.last_min_pay_date"));
		func.varsSet("debt_close_date", sqlStr("ajlg.debt_close_date"));
		func.varsSet("sale_date", sqlStr("ajlg.sale_date"));
		func.varsSet("jcic_remark", sqlStr("ajlg.jcic_remark"));
		func.varsSet("npl_corp_no", sqlStr("ajlg.npl_corp_no"));
		func.varsSet("ecs_ttl_amt_bal", ""+sqlNum("ajlg.ecs_ttl_amt_bal"));
		func.varsSet("acct_jrnl_bal", ""+sqlNum("ajlg.acct_jrnl_bal"));
		func.varsSet("report_reason", sqlStr("ajlg.report_reason"));
		func.varsSet("acct_status", sqlStr("ajlg.acct_status"));
		int lCnt = (int) sqlNum("ajlg.valid_cnt");
		func.varsSet("valid_cnt", ""+lCnt);
		func.varsSet("stop_flag", sqlStr("ajlg.stop_flag"));
		func.varsSet("unpost_inst_stage_fee", ""+sqlNum("ajlg.unpost_inst_stage_fee"));
		func.varsSet("oversea_cashadv_limit", ""+sqlNum("ajlg.oversea_cashadv_limit"));
		func.varsSet("year_revolve_int_rate", ""+sqlNum("ajlg.year_revolve_int_rate"));

    rc = func.dbSave("A");
    if (rc != 1) {
    //alertErr(func.getMsg());
      alertErr("KK4 資料產生失敗");
      return -1;
    }
    this.sqlCommit(rc);

    alertMsg("KK4 資料產生成功 ");

    return 1;

  }

  /***********************************************************************/
  int selectActJcicLog0() throws Exception {
    selectNoLimit();
   	String sqlN017A = "";
   	double tempDouble = 0.0;
   	daoTid="ajlg.";

    sqlN017A  = "select * ";
    sqlN017A += " from act_jcic_log   ";
    sqlN017A += "where p_seqno    = ?  ";
    sqlN017A += "  and acct_month = ?  ";
    sqlN017A += "  and log_type   = 'A'  ";
  
    sqlSelect(sqlN017A,new Object[]{kkPSeqno,kkAcctMonth});
   	 
    if(sqlRowNum<=0) {
			alertErr("無法取得報送來源資料");
      return -1;
    }
    return 1;
   	 
  }


  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }
    
  String fillZeroAcctKey(String acctkey) throws Exception {
		String rtn = acctkey;
	//if (acctkey.trim().length()==8) rtn += "000";
		if (acctkey.trim().length()==10) rtn += "0";

		return rtn;
	}

}
