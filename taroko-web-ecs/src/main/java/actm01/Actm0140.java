/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-12-20  V1.00.00  Oris Chang program initial                            *
* 107-08-21  V1.00.01  Alex       queryRead,itemChage,confirm,ajax,U/D       *
* 108/12/19  V1.00.02  phopho     change table: prt_branch -> gen_brn        *
* 111-10-20  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-01-16  V1.00.04  Simon      apply no field "act_pay_error.acct_key"    *
* 112-07-23  V1.00.05  Simon      act_pay_error.confirm_flag controlled to "N" & "Y"*
* 112-08-16  V1.00.06  Simon      調整AJAX codes                             * 
******************************************************************************/

package actm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Actm0140 extends BaseEdit {
	String mAccttype = "";
	String mAcctkey = "";
	String mCurrcode = "";
	String kk1 = "" , kk2 = "";
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
			// -資料讀取-
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
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
			querySelect();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {
			/* 20200107 modify AJAX */
			strAction = "AJAX";
			wfChkCol(wr);
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

		dddwSelect();
		initButton();
	}

	private int getWhereStr() throws Exception {
		String lsWhere = " where 1=1 "
							 +" and ( (substr(batch_no,9,4) != '9004') or "
							 +"       (substr(batch_no,9,4)  = '9004' and pay_amt <> 0) ) "
							 +sqlCol(wp.itemStr2("ex_batchno"),"batch_no")
							 ;
		
		wp.whereStr = lsWhere;
		return 1;
	}
	
	@Override
	public void queryFunc() throws Exception {
		
		wp.setQueryMode();

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		if(getWhereStr()!=1)return;
		
		wp.selectSQL = " hex(rowid) as rowid , "
						 + " batch_no , "
						 + " serial_no , "
						 + " p_seqno , "
						 + " acno_p_seqno , "
						 + " acct_type , "
					   + " uf_acno_key(acno_p_seqno) as acct_key , "
					 //+ " acct_key , "
						 + " pay_card_no , "
						 + " pay_amt , "
						 + " pay_date , "
						 + " payment_type , "
						 + " error_reason , "
						 + " error_remark , "
						 + " duplicate_mark , "
					 //+ " confirm_flag , "
					   + " decode(confirm_flag,'','N',confirm_flag) confirm_flag, "
						 + " uf_acno_name(acno_p_seqno) as cname , "
						 + " vouch_memo3 "
						 ;
		
		wp.daoTable = " act_pay_error ";

		pageQuery();

		
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		listWkdata();
		wp.setListCount(1);
		wp.setPageValue();

	}

	void listWkdata() {
		String ss = "", ss2 = "";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			ss = wp.colStr(ii, "batch_no");
			ss2 = wp.colStr(ii, "serial_no");
			wp.colSet(ii, "wk_batchno", ss + "-" + ss2);

			ss = wp.colStr(ii, "acct_type");
			ss2 = wp.colStr(ii, "acct_key");
			wp.colSet(ii, "wk_ackey", ss + " - " + ss2);
		}
	}

	@Override
	public void querySelect() throws Exception {
		kk1 = wp.itemStr2("data_k1");
		kk2 = wp.itemStr2("data_k2");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {		
		
		wp.selectSQL = ""
      				 + " hex(rowid) as rowid , mod_seqno , "
      				 + " batch_no ,"
      				 + " serial_no ,"
      				 + " p_seqno ,"
      				 + " acno_p_seqno ,"
      				 + " acct_type ,"
      			   + " uf_acno_key(acno_p_seqno) as acct_key ,"
      			 //+ " acct_key ,"
      				 + " pay_card_no ,"
      				 + " pay_amt ,"
      				 + " pay_date ,"
      				 + " id_no ,"
      				 + " branch ,"
      				 + " error_reason ,"
      				 + " payment_type ,"
      				 + " error_remark ,"
      				 + " duplicate_mark ,"
      			 //+ " confirm_flag ,"
					     + " decode(confirm_flag,'','N',confirm_flag) confirm_flag, "
      				 + " uf_acno_name(acno_p_seqno) as cname ,"
      				 + " vouch_memo3 ,"
      			 //+ " crt_user ,"
      			 //+ " crt_date ,"
      			 //+ " crt_time "
      				 + " update_user ,"
      				 + " update_date ,"
      				 + " update_time "
      				 ;
		wp.daoTable = " act_pay_error ";
	//wp.whereStr = " where 1=1 and pay_amt>0 "
		wp.whereStr = " where 1=1 "
					   +sqlCol(kk1,"batch_no")
					   +sqlCol(kk2,"serial_no")
					   ;
		
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料");
			return ;
		}				
		
		getInfo(wp.colStr("acno_p_seqno"));
		
	}
	
	private void getInfo(String spseqno) throws Exception {
		String sql  = "select a.chi_name, a.company_name , b.payment_no from crd_idno a, act_acno b ";
		       sql += "where a.id_p_seqno = b.id_p_seqno and b.acno_p_seqno = :acno_p_seqno";
		setString("acno_p_seqno", spseqno);

		sqlSelect(sql);
		if (sqlRowNum > 0) {
			wp.colSet("chi_name", sqlStr("chi_name"));
			wp.colSet("corp_chi_name", sqlStr("company_name"));
			wp.colSet("db_payment_no", sqlStr("payment_no"));
		}

	}

	@Override
	public void saveFunc() throws Exception {
		String lsConfirmFlag = "";
		lsConfirmFlag = wp.itemStr2("confirm_flag");
		if(this.isUpdate()){
			if(check()!=0){
				return ;
			}			
		}
		
		if(isDelete()){
			if(eqIgno(lsConfirmFlag,"N")){
				/*
				if(wp.item_num("pay_amt")>0){
					if(check_approve_zz()==false)	return ;
				}
				*/
				if(checkApproveZz()==false)	return ;
			}			
		}
		
		Actm0140Func func = new Actm0140Func(wp);
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr2(func.getMsg());
		}
		this.sqlCommit(rc);
		
		if(isDelete() && eqIgno(lsConfirmFlag,"N")){
			clearFunc();
			wp.dispMesg = "刪除成功 ";
		}	else if(isDelete() && eqIgno(lsConfirmFlag,"Y")){
			wp.colSet("confirm_flag", "N");
			wp.dispMesg = "刪除成功 ";
		} else if(isUpdate()){
			wp.colSet("confirm_flag", "Y");
		}
		
	}
	
	int check() throws Exception{
		
		if(wp.itemEq("conf_flag", "Y")||wp.itemEmpty("pay_card_no"))	return 0;
		
		String sql1 = " select "
						+ " acno_p_seqno , "
						+ " acct_type , "
						+ " acct_key "
						+ " from act_acno "
						+ " where acno_p_seqno in "
						+ " (select acno_p_seqno from crd_card where card_no = ? ) "
						;
		
		sqlSelect(sql1,new Object[]{wp.itemStr2("pay_card_no")});
		if(sqlRowNum<=0){
			errmsg("卡號不存在 !");
			return -1;
		}
		
		if(!wp.itemEq("acno_p_seqno", sqlStr("acno_p_seqno"))){
			wp.dispMesg = "　";
			wp.colSet("conf_mesg", "|| 1==1");
			return -2;
		}
		
		return 0 ;
	}
	
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	@Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr2("ex_batchno");
			this.dddwList("dddw_batchno",
					"select DISTINCT batch_no db_code, batch_no db_desc from act_pay_error where "+
					"(substr(batch_no,9,4) != '9004') or (substr(batch_no,9,4) = '9004' and pay_amt <> 0) "+
					"order by batch_no desc "+commSqlStr.rownum(100));

			if (wp.respHtml.indexOf("_detl") > 0) {
				wp.initOption = "--";
				wp.optionKey = wp.colStr("branch");
//				this.dddw_list("dddw_branch", "ptr_branch", "branch", "branch_name", "where 1=1 order by branch");
				this.dddwList("dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1 order by branch");
			}

		} catch (Exception ex) {
		}
	}
	
	public void wfChkCol(TarokoCommon wr) throws Exception {
		super.wp = wr;
		String lsidCode = wp.itemStr2("ax_idCode");
		if (lsidCode.equals("1")) {
      wfAjaxKey(wr);
		} else if (lsidCode.equals("2")) {
      wfAjaxCard(wr);
		} else if (lsidCode.equals("3")) {
      wfAjaxPaymentNo(wr);
		}
		return;
	}

	public void wfAjaxKey(TarokoCommon wr) throws Exception {
		super.wp = wr;

		// String ls_winid =
		selectData(wp.itemStr2("ax_type"),wp.itemStr2("ax_key"));
		if (rc != 1) {
			wp.addJSON("p_seqno", "　");
			wp.addJSON("acno_p_seqno", "　");
			wp.addJSON("cname", "　");			
			wp.addJSON("corp_chi_name", "　");
			wp.addJSON("corp_p_seqno", "　");
			wp.addJSON("payment_no", "　");
			return;
		}

		wp.addJSON("p_seqno", sqlStr("p_seqno"));
		wp.addJSON("acno_p_seqno", sqlStr("acno_p_seqno"));
		wp.addJSON("corp_p_seqno", sqlStr("corp_p_seqno"));
		wp.addJSON("cname", sqlStr("cname"));		
		wp.addJSON("payment_no", sqlStr("payment_no"));
		if(empty(sqlStr("corp_chi_name"))){
			wp.addJSON("corp_chi_name", "　");
		}	else	{
			wp.addJSON("corp_chi_name", sqlStr("corp_chi_name"));
		}					
	}
	
	void selectData(String s1 , String s2) throws Exception{
		
		s2 = commString.acctKey(s2);
		if(s2.length()!=11){
			alertErr2("帳戶帳號輸入錯誤!");
			return ;
		}
		
		String sql1 = " select "
						+ " acno_p_seqno , "
						+ " p_seqno , "
						+ " payment_no "
						+ " from act_acno "
						+ " where acct_type = ? "
						+ " and acct_key = ? "
						;
						
		sqlSelect(sql1,new Object[]{s1,s2});
		
		if(sqlRowNum<=0){
			alertErr2("帳戶帳號不存在");
			return ;
		}
		
		wfChkCname(s1,s2);
	}
	
	//--
	
	public void wfAjaxCard(TarokoCommon wr) throws Exception {
		super.wp = wr;

		// String ls_winid =
		selectDataCard(wp.itemStr2("ax_card"));
		if (rc != 1) {
			wp.addJSON("p_seqno", "　");
			wp.addJSON("acno_p_seqno", "　");
			wp.addJSON("cname", "　");			
			wp.addJSON("corp_chi_name", "　");
			wp.addJSON("corp_p_seqno", "　");
			wp.addJSON("acct_type", "　");
			wp.addJSON("acct_key", "　");
			return;
		}

		wp.addJSON("p_seqno", sqlStr("p_seqno"));
		wp.addJSON("acno_p_seqno", sqlStr("acno_p_seqno"));
		wp.addJSON("corp_p_seqno", sqlStr("corp_p_seqno"));
		wp.addJSON("cname", sqlStr("cname"));		
		wp.addJSON("acct_type", sqlStr("acct_type"));
		wp.addJSON("acct_key", sqlStr("acct_key"));
		if(empty(sqlStr("corp_chi_name"))){
			wp.addJSON("corp_chi_name", "　");
		}	else	{
			wp.addJSON("corp_chi_name", sqlStr("corp_chi_name"));
		}					
	}
	
	void selectDataCard(String s1) throws Exception{
		
		String sql1 = " select "
						+ " card_no , "
						+ " p_seqno , "
						+ " acno_p_seqno "
						+ " from crd_card "
						+ " where card_no = ? " 
						;
		
		sqlSelect(sql1,new Object[]{s1});
		
		if(sqlRowNum<=0){
			alertErr2("卡號不存在 !");
			return ;
		}
		
		String sql2 = " select "
						+ " acct_type , "
						+ " acct_key "
						+ " from act_acno "
						+ " where acno_p_seqno = ? "
						; 
		
		sqlSelect(sql2,new Object[]{sqlStr("acno_p_seqno")});
		
		if(sqlRowNum<=0){
			alertErr2("帳戶帳號不存在 !");
			return ;
		}
		
		wfChkCname(sqlStr("acct_type"),sqlStr("acct_key"));
		
	}
	
	void wfChkCname(String asType ,String asKey) throws Exception {
		String sql3 = " select "
				+ " id_p_seqno , "
				+ " corp_p_seqno "
				+ " from act_acno "
				+ " where acct_type = ? "
				+ " and acct_key = ? "
				;      
      
      sqlSelect(sql3,new Object[]{asType,asKey});
      
      if(sqlRowNum<=0){
      	alertErr2("帳戶帳號不存在");
      	return ;
      }
      
      String sql4 = " select "
      				+ " chi_name as cname "
      				+ " from crd_idno "
      				+ " where 1=1 "
      				+ " and id_p_seqno = ? "
      				;
      
      sqlSelect(sql4,new Object[]{sqlStr("id_p_seqno")});
      
/***
      if(sqlRowNum<=0){
      	if(!eqIgno(commString.mid(asKey, 8,3),"000")){
      		alertErr2("無法取得帳戶資料");
      		return ;
      	}
      }
***/      
      String sql5 = " select "
      				+ " chi_name as corp_chi_name "
      				+ " from crd_corp "
      				+ " where corp_p_seqno = ? "
      				; 
      
      sqlSelect(sql5,new Object[]{sqlStr("corp_p_seqno")});
/***
      if(sqlRowNum<0){
      	alertErr2("無法取得帳戶資料");
      	return ;
      }	else	rc=1;
***/      
    rc = 1;
		return ;
	}
	
	//--
	public void wfAjaxPaymentNo(TarokoCommon wr) throws Exception {
		super.wp = wr;

		// String ls_winid =
		selectDataPayment(wp.itemStr2("ax_payment_no"));
		if (rc != 1) {
			wp.addJSON("p_seqno", "　");
			wp.addJSON("acno_p_seqno", "　");
			wp.addJSON("cname", "　");			
			wp.addJSON("corp_chi_name", "　");
			wp.addJSON("corp_p_seqno", "　");
			wp.addJSON("acct_type", "　");
			wp.addJSON("acct_key", "　");
			return;
		}

		wp.addJSON("p_seqno", sqlStr("p_seqno"));
		wp.addJSON("acno_p_seqno", sqlStr("acno_p_seqno"));
		wp.addJSON("corp_p_seqno", sqlStr("corp_p_seqno"));
		wp.addJSON("cname", sqlStr("cname"));		
		if(empty(sqlStr("corp_chi_name"))){
			wp.addJSON("corp_chi_name", "　");
		}	else	{
			wp.addJSON("corp_chi_name", sqlStr("corp_chi_name"));			
		}					
		wp.addJSON("acct_type", sqlStr("acct_type"));
		wp.addJSON("acct_key", sqlStr("acct_key"));
	}
	
	void selectDataPayment(String s1) throws Exception{
					
		String sql1 = " select "
						+ " acct_type , "
						+ " acct_key , "
						+ " p_seqno , "
						+ " acno_p_seqno "
						+ " from act_acno "
						+ " where payment_no = ? "						
						+ "   and acno_p_seqno = p_seqno "						
						;
					
		sqlSelect(sql1,new Object[]{s1});
		
		if (sqlRowNum<=0) {
   		String sql2 = " select "
  						+ " acct_type , "
  						+ " acct_key , "
  						+ " p_seqno , "
  						+ " acno_p_seqno "
  						+ " from act_acno "
  						+ " where payment_no_ii = ? "						
  						+ "   and acno_p_seqno = p_seqno "						
  						;
					
		  sqlSelect(sql2,new Object[]{s1});
		  if (sqlRowNum<=0){
			  alertErr2("銷帳編號不存在!");
			 return ;
		  }
		}
		
		wfChkCname(sqlStr("acct_type"),sqlStr("acct_key"));
	}
}
