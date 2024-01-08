package ccam01;
/** 
 * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ccam2130 extends BaseAction implements InfacePdf {
	String isDataFrom = "" , isFileNo = "" , isSerialNo = "" , isAprFlag = "";
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
		switch (strAction) {
		case "X":
			/* keep 基本資料 */
			String lsChiname = "", lsBirthday = "" , lsAcctStatus = "" , lsStmtCycle = "", lsCorpName = "" , lsPseqNo = "" , lsAcctType ="" , lsAcctKey = "";
			lsChiname = wp.itemStr("ex_chi_name");
			lsBirthday = wp.itemStr("ex_birth_day");
			lsAcctStatus = wp.itemStr("ex_acct_status");
			lsStmtCycle = wp.itemStr("ex_stmt_cycle");
			lsCorpName = wp.itemStr("ex_corp_name");
			lsPseqNo = wp.itemStr("ex_p_seqno");
			lsAcctType = wp.itemStr("ex_acct_type");
			lsAcctKey = wp.itemStr("ex_acct_key");
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
			/* 放值 */
			wp.colSet("ex_chi_name",lsChiname);
			wp.colSet("ex_birth_day",lsBirthday);
			wp.colSet("ex_acct_status",lsAcctStatus);
			wp.colSet("ex_stmt_cycle",lsStmtCycle);
			wp.colSet("ex_corp_name",lsCorpName);
			wp.colSet("ex_p_seqno",lsPseqNo);
			wp.colSet("ex_acct_type",lsAcctType);
			wp.colSet("ex_acct_key",lsAcctKey);
			wp.colSet("pay_date", getSysDate());
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
		case "A":
			/* 新增功能 */
			saveFunc();
			break;
		case "U":
			/* 更新功能 */
			saveFunc();
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
		case "Q2":
			// -查詢基本資料-
			selectBaseData();
			break;	
		case "PDF":
			// --報表列印
			pdfPrint();
			break;	
//		case "AJAX":
//			wfAjaxKey();
//			break;
		default:
			break;
		}

	}
	
	@Override
	public void dddwSelect() {
		try {
			wp.optionKey = wp.colStr("ex_acct_type");
			this.dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "acct_type", " where 1=1");
			wp.optionKey = wp.colStr("payment_type2");
			this.dddwList("d_dddw_payment_type2", "ptr_sys_idtab", "wf_id", "wf_desc", " where 1=1 and wf_type = 'PAYMENT_TYPE2'");
			if(eqIgno(wp.respHtml,"ccam2130_detl")) {
				wp.optionKey = wp.colStr("pay_card_no");
				if(wp.colEmpty("ex_p_seqno"))	return ;
				String sql = "";
				sql = "select card_no as db_code , card_no as db_desc from crd_card where card_no <>'' "+sqlCol(wp.colStr("ex_p_seqno"),"acno_p_seqno");
				sql += "union select payment_no as db_code , payment_no as db_desc from act_acno where payment_no <> '' "+sqlCol(wp.colStr("ex_p_seqno"),"acno_p_seqno");
				sql += "union select payment_no_II as db_code , payment_no_II as db_desc from act_acno where payment_no_II <> '' "+sqlCol(wp.colStr("ex_p_seqno"),"acno_p_seqno");
				dddwList("d_dddw_pay_card_no", sql);
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public void queryFunc() throws Exception {
		if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr("繳款日期起迄錯誤");
			return;
		}

		if (wp.itemEmpty("ex_acct_key") == false && wp.itemStr("ex_acct_key").length() != 10
				& wp.itemStr("ex_acct_key").length() != 11 & wp.itemStr("ex_acct_key").length() != 8) {
			alertErr("帳戶帳號輸入錯誤");
			return;
		}

		String lsPseqNo = "";		
		if (wp.itemEmpty("ex_acct_key") == false) {			
			lsPseqNo = getPseqno();
			if(empty(lsPseqNo)) {
				alertErr("帳戶帳號輸入錯誤");
				return ;
			}			
		}				
		
		String lsWhere = " where 1=1 " + sqlBetween("ex_date1", "ex_date2", "pay_date")
				+ sqlCol(lsPseqNo, "acno_p_seqno")
				+ sqlCol(wp.itemStr("ex_card_no"),"pay_card_no","like%")
				;
		
		if(wp.itemEq("ex_data_from", "O")) {
			lsWhere += " and data_from = 'O' ";
		} else if(wp.itemEq("ex_data_from", "B")) {
			lsWhere += " and data_from = 'B' ";
		}
		
		if(wp.itemEq("ex_apr_flag", "N")) {
			lsWhere += " and apr_flag ='N' ";
		}	else if(wp.itemEq("ex_apr_flag", "Y")) {
			lsWhere += " and apr_flag in ('','Y') ";
		}
		
		wp.whereStr = lsWhere;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}
	
	void selectBaseData() {
		wp.totalPage = 0;
		if(wp.itemEmpty("ex_acct_key")==false && wp.itemEmpty("ex_card_no")==false) {
			alertErr("帳戶帳號、卡號請擇一輸入 !");
			return ;
		}
		
		if(wp.itemEmpty("ex_acct_key") && wp.itemEmpty("ex_card_no")) {
			alertErr("帳戶帳號、卡號請擇一輸入 !");
			return ;
		}
		
		//--清空殘存資料
		wp.colSet("ex_acct_type", "");
		wp.colSet("ex_chi_name", "");
		wp.colSet("ex_acct_status", "");
		wp.colSet("ex_stmt_cycle", "");
		wp.colSet("ex_corp_name", "");
		wp.colSet("ex_p_seqno", "");
		wp.colSet("ex_idno", "");
		wp.colSet("ex_birth_day", "");				
		
		if(wp.itemEmpty("ex_acct_key")==false) {
			getBaseDataAcctKey();
		}	else	{
			wp.colSet("ex_acct_key", "");
			getBaseDataCard();
		}
		
	}
	
	void getBaseDataCard() {
		String sql1 = "select acno_p_seqno , id_p_seqno from crd_card where card_no = ? ";
		sqlSelect(sql1,new Object[] {wp.itemStr("ex_card_no")});
		if(sqlRowNum<=0)	{
			alertErr("卡號輸入錯誤");
			return ;
		}
		String sql2 = "select acct_type as ex_acct_type , uf_chi_name(uf_idno_id(id_p_seqno)) as ex_chi_name , acct_status as ex_acct_status , "
				+ "stmt_cycle as ex_stmt_cycle , uf_corp_name(corp_p_seqno) as ex_corp_name , acno_p_seqno as ex_p_seqno , "
				+ "uf_idno_id(id_p_seqno) as ex_idno , " 
				+ "uf_acno_key(acno_p_seqno) as ex_acct_key "
				+ "from act_acno where 1=1 and acno_p_seqno = ? ";
				;
		sqlSelect(sql2,new Object[] {sqlStr("acno_p_seqno")});
		if(sqlRowNum<=0) {
			alertErr("卡號輸入錯誤");
			return ;
		}
		String sql3 = "select birthday as ex_birth_day from crd_idno where id_no = ?";
		sqlSelect(sql3,new Object[] {sqlStr("ex_idno")});
		if(sqlRowNum<=0) {
			alertErr("卡號輸入錯誤");
			return ;
		}
		
		wp.colSet("ex_acct_type", sqlStr("ex_acct_type"));
		wp.colSet("ex_chi_name", sqlStr("ex_chi_name"));
		wp.colSet("ex_acct_status", ecsfunc.DeCodeAct.acctStatus(sqlStr("ex_acct_status")));
		wp.colSet("ex_stmt_cycle", sqlStr("ex_stmt_cycle"));
		wp.colSet("ex_corp_name", sqlStr("ex_corp_name"));
		wp.colSet("ex_p_seqno", sqlStr("ex_p_seqno"));
		wp.colSet("ex_idno", sqlStr("ex_idno"));
		wp.colSet("ex_birth_day", commDate.dspDate(sqlStr("ex_birth_day")));
		wp.colSet("ex_acct_key", sqlStr("ex_acct_key"));
	}
	
	void getBaseDataAcctKey() {
		String lsAcctKey = "";
		lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));
		String sql1 = "select acct_type as ex_acct_type , uf_chi_name(uf_idno_id(id_p_seqno)) as ex_chi_name , acct_status as ex_acct_status , "
				+ "stmt_cycle as ex_stmt_cycle , uf_corp_name(corp_p_seqno) as ex_corp_name , acno_p_seqno as ex_p_seqno , "
				+ "uf_idno_id(id_p_seqno) as ex_idno " + "from act_acno where 1=1 and acct_type = ? and acct_key = ? ";
		sqlSelect(sql1, new Object[] { wp.itemNvl("ex_acct_type", "01"), lsAcctKey });
		if (sqlRowNum <= 0) {
			errmsg("帳戶帳號輸入錯誤");
			return;
		}
		
		wp.colSet("ex_acct_type", sqlStr("ex_acct_type"));
		wp.colSet("ex_chi_name", sqlStr("ex_chi_name"));
		wp.colSet("ex_acct_status", ecsfunc.DeCodeAct.acctStatus(sqlStr("ex_acct_status")));
		wp.colSet("ex_stmt_cycle", sqlStr("ex_stmt_cycle"));
		wp.colSet("ex_corp_name", sqlStr("ex_corp_name"));
		wp.colSet("ex_p_seqno", sqlStr("ex_p_seqno"));
		wp.colSet("ex_idno", sqlStr("ex_idno"));
		
		String sql2 = "select birthday as ex_birth_day from crd_idno where id_no = ? ";
		sqlSelect(sql2, new Object[] { sqlStr("ex_idno") });
		if (sqlRowNum <= 0) {
			errmsg("帳戶帳號輸入錯誤");
			return;
		}		
		wp.colSet("ex_birth_day", commDate.dspDate(sqlStr("ex_birth_day")));
	}
	
	String getPseqno() {

		if (wp.itemEmpty("ex_card_no") == false) {
			String sql1 = "select acno_p_seqno from crd_card where card_no = ? ";
			sqlSelect(sql1, new Object[] { wp.itemStr("ex_card_no") });
			if (sqlRowNum > 0)
				return sqlStr("acno_p_seqno");
		}

		if (wp.itemEmpty("ex_acct_key") == false) {
			String lsAcctKey = "";
			if (wp.itemStr("ex_acct_key").length() == 10 || wp.itemStr("ex_acct_key").length() == 11) {
				lsAcctKey = commString.acctKey(wp.itemStr("ex_acct_key"));
			} else if (wp.itemStr("ex_acct_key").length() == 8) {

			} else {
				return "";
			}
			String sql1 = "select acno_p_seqno from act_acno where acct_type = ? and acct_key = ? ";
			sqlSelect(sql1, new Object[] { wp.itemNvl("ex_acct_type", "01"), lsAcctKey });
			if (sqlRowNum > 0)
				return sqlStr("acno_p_seqno");
		}

		return "";
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.selectSQL = " decode(data_from,'O','線上','B','批次',data_from) as tt_data_from , pay_date , data_from ,"
				+ " pay_time , pay_card_no , uf_chi_name(uf_idno_id(id_p_seqno)) as chi_name , "
				+ " sign , pay_amt , uf_nvl(is_repay,'N') as is_repay , file_no , serial_no ,"
				+ " decode((select wf_desc from ptr_sys_idtab where wf_type = 'PAYMENT_TYPE2' and wf_id = payment_type2),null,payment_type2,(select wf_desc from ptr_sys_idtab where wf_type = 'PAYMENT_TYPE2' and wf_id = payment_type2)) as tt_payment_type ,"
				+ " remark , crt_date , mod_user , to_char(mod_time,'yyyymmdd') as mod_date , crt_user , apr_flag , "
				+ " decode(apr_flag,'Y','已覆核','','已覆核','未覆核') as tt_apr_flag , apr_user , apr_date "
				;

		wp.daoTable = "act_repay_creditlimit";
		wp.whereOrder = " order by pay_date Asc , pay_time Asc ";

		pageQuery();
		if (sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}

		wp.setListCount(0);
		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		isDataFrom = wp.itemStr("data_k1");
		isFileNo = wp.itemStr("data_k2");
		isSerialNo = wp.itemStr("data_k3");
		isAprFlag = wp.itemStr("data_k5");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if(empty(isDataFrom))	isDataFrom = wp.itemStr("data_from");
		if(empty(isFileNo))		isFileNo = wp.itemStr("file_no");
		if(empty(isSerialNo))	isSerialNo = wp.itemStr("serial_no");
		if(empty(isAprFlag))	isAprFlag = wp.itemStr("apr_flag");
		
		if(empty(isDataFrom)) {
			alertErr("繳款來源: 不可空白 !");
			return ;
		}
		
		if(empty(isFileNo)) {
			alertErr("批次號碼: 不可空白 !");
			return ;
		}
		
		if(empty(isSerialNo)) {
			alertErr("序號: 不可空白 !");
			return ;
		}
		
		if(empty(isAprFlag)) {
			isAprFlag = "N";			
		}
		
		wp.selectSQL = "data_from , file_no , serial_no , pay_card_no , sign , pay_amt , payment_type2 , pay_date ,"
					 + "pay_time , proc_mark , unite_mark , def_branch , pay_branch , is_pass , is_repay , acct_type ,"
					 + "id_p_seqno , corp_p_seqno , p_seqno , acno_p_seqno , payment_type , crt_date , crt_time , crt_user ,"
					 + "mod_user , to_char(mod_time,'yyyymmdd') as mod_date ,mod_pgm , pay_amt as ori_pay_amt , hex(rowid) as rowid , "
					 + "acno_p_seqno as ex_p_seqno , remark , acct_type as ex_acct_type , "
					 + "uf_acno_key(acno_p_seqno) as ex_acct_key , apr_flag , decode(apr_flag,'Y','已覆核','','已覆核','未覆核') as tt_apr_flag "
					 ;
		
		wp.daoTable = "act_repay_creditlimit";
		wp.whereStr = " where 1=1 "
					+ sqlCol(isDataFrom,"data_from")
					+ sqlCol(isFileNo,"file_no")
					+ sqlCol(isSerialNo,"serial_no")
					+ sqlCol(isAprFlag,"apr_flag")
					;
		
		pageSelect();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}		
		
		dataReadAfter();
	}

	void dataReadAfter() {
		String sql1 = "" , sql2 = "";
		sql1 = "select chi_name , birthday from crd_idno where id_p_seqno = ? ";
		sqlSelect(sql1,new Object[] {wp.colStr("id_p_seqno")});
		if(sqlRowNum>0) {
			wp.colSet("ex_chi_name",sqlStr("chi_name"));
			wp.colSet("ex_birth_day",sqlStr("birthday"));
		}
		
		sql2 = "select acct_status , stmt_cycle , uf_corp_name(corp_p_seqno) corp_name from act_acno where acno_p_seqno = ? ";
		sqlSelect(sql2,new Object[] {wp.colStr("acno_p_seqno")});
		if(sqlRowNum>0) {
			wp.colSet("ex_acct_status", ecsfunc.DeCodeAct.acctStatus(sqlStr("acct_status")));
			wp.colSet("ex_stmt_cycle", sqlStr("stmt_cycle"));
			wp.colSet("ex_corp_name", sqlStr("corp_name"));
		}
	}
	
	@Override
	public void saveFunc() throws Exception {
		
		ccam01.Ccam2130Func func = new ccam01.Ccam2130Func();
		func.setConn(wp);
		
		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if(rc!=1) {
			errmsg(func.getMsg());
			return ;
		}	else	{
			saveAfter(true);
		}
	}

	@Override
	public void procFunc() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void initButton() {
		if(eqIgno(wp.respHtml,"ccam2130_detl")) {
			btnModeAud();
		} else if(eqIgno(wp.respHtml,"ccam2130")) {
			if(wp.colEmpty("ex_idno"))	btnAddOn(false);
			else	btnAddOn(true);
		}

	}

	@Override
	public void initPage() {
		
	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "ccam2130";		
		wp.pageRows = 9999;
		queryFunc();
		TarokoPDF pdf = new TarokoPDF();
		wp.fileMode = "Y";
		pdf.excelTemplate = "ccam2130.xlsx";
		pdf.pageCount = 30;
		pdf.sheetNo = 0;
		pdf.procesPDFreport(wp);
		pdf = null;		
	}

//	void wfAjaxKey() throws Exception {
//		if (wp.itemEmpty("ax_card_no")) {
//			selectData(wp.itemStr("ax_acct_type"), wp.itemStr("ax_acct_key"));
//			if (rc != 1) {
//				wp.addJSON("ex_chi_name", "");
//				wp.addJSON("ex_birth_day", "");
//				wp.addJSON("ex_acct_status", "");
//				wp.addJSON("ex_stmt_cycle", "");
//				wp.addJSON("ex_corp_name", "");
//				wp.addJSON("ex_p_seqno", "");
//				wp.addJSON("ex_acct_type", "");
//				return;
//			}			
//			wp.addJSON("ex_chi_name", sqlStr("ex_chi_name"));
//			wp.addJSON("ex_birth_day", commDate.dspDate(sqlStr("ex_birth_day")));
//			wp.addJSON("ex_acct_status", ecsfunc.DeCodeAct.acctStatus(sqlStr("ex_acct_status")));
//			wp.addJSON("ex_stmt_cycle", sqlStr("ex_stmt_cycle"));
//			wp.addJSON("ex_corp_name", sqlStr("ex_corp_name"));
//			wp.addJSON("ex_p_seqno", sqlStr("ex_p_seqno"));
//			wp.addJSON("ex_acct_type", sqlStr("ex_acct_type"));
//
//		} else {
//			selectDataCard(wp.itemStr("ax_card_no"));
//			if (rc != 1) {
//				wp.addJSON("ex_chi_name", "");
//				wp.addJSON("ex_birth_day", "");
//				wp.addJSON("ex_acct_status", "");
//				wp.addJSON("ex_stmt_cycle", "");
//				wp.addJSON("ex_corp_name", "");
//				wp.addJSON("ex_p_seqno", "");
//				wp.addJSON("ex_acct_type", "");
//				return;
//			}			
//			wp.addJSON("ex_chi_name", sqlStr("ex_chi_name"));
//			wp.addJSON("ex_birth_day", commDate.dspDate(sqlStr("ex_birth_day")));
//			wp.addJSON("ex_acct_status", ecsfunc.DeCodeAct.acctStatus(sqlStr("ex_acct_status")));
//			wp.addJSON("ex_stmt_cycle", sqlStr("ex_stmt_cycle"));
//			wp.addJSON("ex_corp_name", sqlStr("ex_corp_name"));
//			wp.addJSON("ex_p_seqno", sqlStr("ex_p_seqno"));
//			wp.addJSON("ex_acct_type", sqlStr("ex_acct_type"));
//		}
//
//	}

//	void selectData(String s1, String s2) {
//		if (s2.length() == 10 || s2.length() == 11) {
//			s2 = commString.acctKey(s2);
//		} else if (s2.length() == 8) {
//
//		} else {
//			errmsg("帳戶帳號輸入錯誤");
//			return;
//		}
//
//		String sql1 = "select acct_type as ex_acct_type , uf_chi_name(uf_idno_id(id_p_seqno)) as ex_chi_name , acct_status as ex_acct_status , "
//				+ "stmt_cycle as ex_stmt_cycle , uf_corp_name(corp_p_seqno) as ex_corp_name , acno_p_seqno as ex_p_seqno , "
//				+ "uf_idno_id(id_p_seqno) as ex_idno " + "from act_acno where 1=1 and acct_type = ? and acct_key = ? ";
//		sqlSelect(sql1, new Object[] { s1, s2 });
//		if (sqlRowNum <= 0) {
//			errmsg("帳戶帳號輸入錯誤");
//			return;
//		}
//
//		String sql2 = "select birthday as ex_birth_day from crd_idno where id_no = ? ";
//		sqlSelect(sql2, new Object[] { sqlStr("ex_idno") });
//		if (sqlRowNum <= 0) {
//			errmsg("帳戶帳號輸入錯誤");
//			return;
//		}
//
//	}

//	void selectDataCard(String s1) {
//		String sql1 = "select A.acct_type as ex_acct_type , A.acno_p_seqno as ex_p_seqno , B.birthday as ex_birth_day , B.chi_name as ex_chi_name "
//				+ "from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno " + "where card_no = ? ";
//
//		sqlSelect(sql1, new Object[] { s1 });
//
//		if (sqlRowNum <= 0) {
//			errmsg("卡號輸入錯誤");
//			return;
//		}
//
//		String sql2 = "select acct_status as ex_acct_status , stmt_cycle as ex_stmt_cycle , "
//				+ "uf_corp_name(corp_p_seqno) as ex_corp_name  " + "from act_acno where 1=1 and acno_p_seqno = ? ";
//
//		sqlSelect(sql2, new Object[] { sqlStr("ex_p_seqno") });
//
//		if (sqlRowNum <= 0) {
//			errmsg("卡號輸入錯誤");
//			return;
//		}
//	}

}
