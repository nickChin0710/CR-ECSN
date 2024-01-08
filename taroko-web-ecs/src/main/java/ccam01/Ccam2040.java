package ccam01;

/**解凍、凍結(卡戶)
 * 2019-1210:  Alex  fix initButton
 * 2019-0611:  JH    p_seqno >>acno_pxxx
 * 2018-0314:	JH		modify
 * 2020-0107:  Ru    modify AJAX
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 110-01-15  Justin fix  a query bug   
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Ccam2040 extends BaseAction {
	Ccam2040Func func;
	String acnoPSeqno = "", debitFlag = "", lsWhere = "";

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
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 動態查詢 */
			readSmsFlag();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "Z")) {
			selectData();
		}
		// 20200107 modify AJAX
		else if (eqIgno(wp.buttonCode, "AJAX")) {
			if ("1".equals(wp.getValue("ID_CODE"))) {
				wfAjaxFunc1();
			}
		} else if (eqIgno(wp.buttonCode, "C")) {
//			procFunc();
		}
	}

	@Override
	public void queryFunc() throws Exception {
		
		if(wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_idno")) {
			errmsg("身分證ID、卡號需則一輸入");
			return ;
		}
		
		if(wp.itemEmpty("ex_card_no") == false) {
			getAcnoPSeqno(wp.itemStr("ex_card_no"));
			if(acnoPSeqno.isEmpty()) {
				errmsg("卡號輸入錯誤");
				return ;
			}
			
			lsWhere += sqlCol(acnoPSeqno,"acno_p_seqno");
			lsWhere += sqlCol(debitFlag,"debit_flag");
			
		}		
		
		if(wp.itemEmpty("ex_idno") == false) {
			if (wp.itemStr("ex_idno").length() != 10) {
				errmsg("身份證ID: 輸入錯誤");
				return;
			}
			
			lsWhere = " and (uf_nvl(debit_flag,'N'),id_p_seqno) in " + " (select 'N',id_p_seqno from crd_idno where 1=1"
					+ sqlCol(wp.itemStr2("ex_idno"), "id_no") + " union select 'Y',id_p_seqno from dbc_idno where 1=1"
					+ sqlCol(wp.itemStr2("ex_idno"), "id_no") + " )";			
		}						

		wp.whereStr = "";
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();
	}
		
	void getAcnoPSeqno(String cardNo) {
		
		String sql1 = "select acno_p_seqno , uf_nvl(debit_flag,'N') as debit_flag from cca_card_base where card_no = ? ";
		sqlSelect(sql1,new Object[] {cardNo});
		
		if(sqlRowNum >0) {
			acnoPSeqno = sqlStr("acno_p_seqno");
			debitFlag = sqlStr("debit_flag");
		}
		
		return ;
	}
	
	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		wp.sqlCmd = "select " + " acct_type ," + " uf_acno_key2(acno_p_seqno,debit_flag) as acct_key ," + " debit_flag,"
				+ " block_status ," + " block_reason1 ," + " block_reason2 ," + " block_reason3 ," + " block_reason4 ,"
				+ " block_reason5 ," + " spec_status ," + " nocancel_credit_flag," + " acno_p_seqno , "
				+ " spec_user , " + " spec_date " + " from cca_card_acct" + " where 1=1 " + lsWhere + " order by 1,2";

		pageQuery();
		if (sqlRowNum <= 0) {
			alertErr2(this.appMsg.errCondNodata);
			return;
		}

		wp.setListCount(1);
		wp.setPageValue();
		colReadOnly("cond_edit");
		listWkdata(sqlRowNum);
	}

	void listWkdata(int aRow) {
		for (int ii = 0; ii < aRow; ii++) {
			if (wp.colEq(ii, "debit_flag", "Y")) {
				if (wp.colEq(ii, "nocancel_credit_flag", "Y")) {
					wp.colSet(ii, "nocancel_credit_flag", "不可取消");
				} else {
					wp.colSet(ii, "nocancel_credit_flag", "可取消");
				}
			}

			if (wp.colEq(ii, "debit_flag", "Y")) {
				wp.sqlCmd = "select acct_no from dba_acno" + " where 1=1 "
						+ sqlCol(wp.colStr(ii, "acno_p_seqno"), "p_seqno");
				sqlSelect();
				if (sqlRowNum > 0) {
					wp.colSet(ii, "acct_no", sqlStr("acct_no"));
				}
			} else {
				wp.sqlCmd = "select combo_acct_no from act_acno where 1=1 "
						+ sqlCol(wp.colStr(ii, "acno_p_seqno"), "acno_p_seqno");

				sqlSelect();
				if (sqlRowNum > 0) {
					wp.colSet(ii, "acct_no", sqlStr("combo_acct_no"));
				}
			}
			
			if(wp.colEq(ii,"acct_type", "03") || wp.colEq(ii,"acct_type", "06")) {
				wp.sqlCmd = "select card_no from crd_card where 1=1 "
						+ sqlCol(wp.colStr(ii,"acno_p_seqno"),"acno_p_seqno")
						+ " order by current_code Asc , new_end_date Desc "
						+ commSqlStr.rownum(1)
						;
				
				sqlSelect();
				if (sqlRowNum > 0) {
					wp.colSet(ii, "card_no", sqlStr("card_no"));
				}
			}
			
		}
	}

	@Override
	public void querySelect() throws Exception {
		acnoPSeqno = wp.itemStr("data_k1");
		debitFlag = wp.itemStr("data_k2");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if (empty(acnoPSeqno)) {
			acnoPSeqno = this.itemkk("acno_p_seqno");
			debitFlag = this.itemkk("debit_flag");
		}

		wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " acct_type,"
				+ " uf_acno_key2(acno_p_seqno,debit_flag) as acct_key," + " debit_flag ,"
				// + " '' as bin_type ,"
				+ "uf_idno_name(id_p_seqno) as idno_name ," + "uf_corp_no(corp_p_seqno) as corp_no ,"
				+ "uf_corp_name(corp_p_seqno) as corp_name ," + " '' as acct_no," + " nocancel_credit_flag,"
				+ " block_reason1, " + " block_reason2, " + " block_reason3, " + " block_reason4, "
				+ " block_reason5	, " + " spec_status," + " block_reason1 as old_block1, "
				+ " block_reason2 as old_block2, " + " block_reason3 as old_block3, " + " block_reason4 as old_block4, "
				+ " block_reason5 as old_block5, " + " spec_status as old_spec,"
				+ " acno_p_seqno, id_p_seqno, corp_p_seqno, card_acct_idx,"
				+ " to_char(mod_time,'yyyymmdd') as mod_date," + " mod_pgm," + " mod_user," + " spec_date,"
				+ " spec_user," + " uf_idno_id2(id_p_seqno,debit_flag) as id_no , " + " spec_remark , "
				+ " spec_del_date , " + " block_sms_flag , block_date ";
		wp.daoTable = " cca_card_acct ";
		wp.whereStr = " where 1=1" + sqlCol(acnoPSeqno, "acno_p_seqno") + sqlCol(debitFlag, "debit_flag");

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + acnoPSeqno);
			return;
		}
		dataReadAfter();
	}

	void dataReadAfter() {
		String sql1 = "", sql2 = "", sql3 = "" , sql4 = "";

		if (wp.colEq("debit_flag", "Y")) {
			if (wp.colEq("nocancel_credit_flag", "Y")) {
				wp.colSet("nocancel_credit_flag", "不可取消");
			} else {
				wp.colSet("nocancel_credit_flag", "可取消");
			}

			sql1 = " select " + " group_code , " + " uf_tt_group_code(group_code) as tt_group_code " + " from dbc_card "
					+ " where id_p_seqno = ? ";

			sql2 = "select acct_no from dba_acno" + " where 1=1" + " and p_seqno = ? ";

			sqlSelect(sql2, new Object[] { wp.colStr("acno_p_seqno") });
			if (sqlRowNum > 0) {
				wp.colSet("acct_no", sqlStr("acct_no"));
			}
		} else {
			sql1 = " select " + " group_code , " + " uf_tt_group_code(group_code) as tt_group_code " + " from crd_card "
					+ " where id_p_seqno = ? ";

			sql2 = " select " + " combo_acct_no " + " from crd_card " + " where id_p_seqno = ? ";

			sqlSelect(sql2, new Object[] { wp.colStr("id_p_seqno") });

			if (sqlRowNum > 0) {
				wp.colSet("acct_no", sqlStr("combo_acct_no"));
			}

		}

		sqlSelect(sql1, new Object[] { wp.colStr("id_p_seqno") });

		if (sqlRowNum > 0) {
			wp.colSet("group_code", sqlStr("group_code"));
			wp.colSet("tt_group_code", sqlStr("tt_group_code"));
		}

		if (wp.colEq("debit_flag", "Y")) {
			sql3 = " select cellar_phone from dbc_idno where id_no = ? ";
		} else {
			sql3 = " select cellar_phone from crd_idno where id_no = ? ";
		}

		sqlSelect(sql3, new Object[] { wp.colStr("id_no") });
		if (sqlRowNum > 0) {
			wp.colSet("cellar_phone", sqlStr("cellar_phone"));
		}
		
		if(wp.colEq("acct_type", "03") || wp.colEq("acct_type", "06")) {
			sql4 = " select card_no from crd_card where acno_p_seqno = ? order by current_code Asc , new_end_date Desc " + commSqlStr.rownum(1);
			sqlSelect(sql4,new Object[] {wp.colStr("acno_p_seqno")});
			
			if(sqlRowNum > 0 ) {
				wp.colSet("card_no", sqlStr("card_no"));
			}
		}				
	}

	void readSmsFlag() {
		wp.selectSQL = " block_sms_flag ";
		wp.daoTable = " cca_card_acct ";
		wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("acno_p_seqno"), "acno_p_seqno");

		pageSelect();
	}

	@Override
	public void saveFunc() throws Exception {
		if (eqIgno(wp.respHtml, "ccam2040_detl")) {
			func = new Ccam2040Func();
			func.setConn(wp);
			rc = func.dbSave(strAction);
			this.sqlCommit(rc);
			if (rc != 1) {
				alertErr2(func.getMsg());
			} else	saveAfter(false);
			
		}
	}

	@Override
	public void procFunc() throws Exception {
		String lsAcnoPseqno = "" , lsDebitFlag = "";
		lsAcnoPseqno = wp.itemStr("acno_p_seqno");
		lsDebitFlag = wp.itemStr("debit_flag");
		if (wp.itemEmpty("cellar_phone")) {
			errmsg("查無卡人手機號碼 , 無法發送簡訊");
			return;
		}
		func = new Ccam2040Func();
		func.setConn(wp);

		rc = func.dataProc();
		sqlCommit(rc);

		if (rc != 1) {
			alertErr2(func.getMsg());
		} else {
			this.saveAfter(false);
			wp.itemSet("acno_p_seqno", lsAcnoPseqno);
			wp.itemSet("debit_flag", lsDebitFlag);
			dataRead();
			wp.respMesg = "發送簡訊完成 !";
		}
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			btnModeAud("XX");
			if (wp.autUpdate() == false)
				return;
			String lsBlock = wp.colStr("block_reason1") + wp.colStr("block_reason2") + wp.colStr("block_reason3")
					+ wp.colStr("block_reason4") + wp.colStr("block_reason5") + wp.colStr("spec_status");
			if (empty(lsBlock) == false) {
				this.btnOnAud(false, true, true);
			} else {
				this.btnOnAud(false, true, false);
			}
		}
	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dddwSelect() {
		try {
			if (wp.respHtml.indexOf("_detl") > 0) {
				wp.optionKey = wp.colStr("block_reason1");
				dddwList("ddw_block_reason1", "CCA_SPEC_CODE", "spec_code", "spec_desc", "where spec_type = '1' ");
				wp.optionKey = wp.colStr("block_reason2");
				this.dddwShare("ddw_block_reason2");
				wp.optionKey = wp.colStr("block_reason3");
				this.dddwShare("ddw_block_reason3");
				wp.optionKey = wp.colStr("block_reason4");
				this.dddwShare("ddw_block_reason4");
				wp.optionKey = wp.colStr("block_reason5");
				this.dddwShare("ddw_block_reason5");

				// --
				wp.optionKey = wp.colStr("spec_status");
				dddwList("ddw_spec_status", "CCA_SPEC_CODE", "spec_code", "spec_desc", "where spec_type ='2' ");
			}
		} catch (Exception ex) {
		}

		try {
			if ((wp.respHtml.equals("ccam2040_nadd"))) {
				wp.optionKey = "";
				wp.initOption = "";
				if (wp.colStr("msg_dept").length() > 0) {
					wp.optionKey = wp.colStr("msg_dept");
				}
				this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)", " where 1 = 1 ");
				wp.optionKey = "";
				wp.initOption = "";
				if (wp.colStr("ex_id").length() > 0) {
					wp.optionKey = wp.colStr("ex_id");
				}
				this.dddwList("dddw_msg_ex", "select trim(msg_id) as db_code , "
						+ "trim(msg_id)||'  '||trim(msg_desc) as db_desc " + "from sms_msg_id where apr_date <>'' ");
			}
		} catch (Exception ex) {
		}

	}

	void selectData() {

		if (eqIgno(wp.itemStr("data_k1"), "Y")) {
			wp.sqlCmd = " select " + " a.cellar_phone as cellar_phone ," + " a.chi_name as chi_name ,"
					+ " a.id_p_seqno as id_p_seqno " + " from  dbc_idno a " + " where 1=1 "+sqlCol(wp.itemStr("id_no"),"a.id_no");
		} else {
			wp.sqlCmd = " select " + " a.cellar_phone as cellar_phone ," + " a.chi_name as chi_name ,"
					+ " a.id_p_seqno as id_p_seqno " + " from  crd_idno a " + " where 1=1 "+sqlCol(wp.itemStr("id_no"),"a.id_no");
		}

		this.sqlSelect();
		if (sqlRowNum <= 0)
			alertErr2("持卡者ID:[" + wp.itemStr("id_no") + "]查無資料");

		wp.colSet("cellar_phone", sqlStr("cellar_phone"));
		wp.colSet("chi_name", sqlStr("chi_name"));
		wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));

		return;
	}

	// 20200107 modify AJAX
	public void wfAjaxFunc1() throws Exception {
		// super.wp = wr;

		if (wp.itemStr("ax_win_ex_id").length() == 0)
			return;

		selectAjaxFunc10(wp.itemStr("ax_win_ex_id"));

		if (rc != 1) {
			wp.addJSON("msg_userid", "");
			wp.addJSON("msg_id", "");
			wp.addJSON("msg_desc", "");
			// wp.addJSON("chi_name_flag","");
			return;
		}

		wp.addJSON("msg_userid", sqlStr("msg_userid"));
		wp.addJSON("msg_id", sqlStr("msg_id"));
		wp.addJSON("msg_desc", sqlStr("msg_desc"));
		// wp.addJSON("chi_name_flag",sql_ss("chi_name_flag"));
	}

	// ************************************************************************
	void selectAjaxFunc10(String s1) {
		wp.sqlCmd = " select " + " a.msg_userid ," + " a.msg_id ," + " a.msg_desc "
		// + " a.chi_name_flag as chi_name_flag "
				+ " from  sms_msg_id a " + " where 1=1 "+sqlCol(s1,"a.msg_id");
		//a.msg_id ='" + s1 + "' ";

		this.sqlSelect();
		if (sqlRowNum <= 0)
			alertErr2("簡訊範例[" + s1 + "]查無資料");

		return;
	}

}
