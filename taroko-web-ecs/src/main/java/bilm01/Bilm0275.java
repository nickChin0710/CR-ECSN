/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-08-02  V1.00.00  yash       program initial                            *
* 108-04-17  V1.00.01  Andy       Update (merage bilm0270 to bilm0275)       *
* 108-06-13  V1.00.02  Amber      Update p_seqno → acno_seqno                *
* 109-02-14	 V1.00.03  ryan       delete where uf_idno_id                    *
* 109-04-08	 V1.00.04  Amber      add f_auth_query()                         *
* 109-04-20  V1.00.05  Amber      Update:Add throws Exception                *
* 109-05-12  V1.00.06  Andy       Update:Program Logic                       *
* 109-05-18  V1.00.07  Andy       Update:Mantis3451                          *
* 109-05-27  V1.00.08  Andy       Update:bug                                 *
* 109-05-28  V1.00.03  Andy       Update:Mantis3515                          *
* 109-06-16  V2.00.01  Amber      part of validation move html(USER need retest)*
* 109-07-01  V2.00.02  Amber      Update:Mantis 0003702  & 0003692           *
* 109-07-07  V2.00.03  Amber      Update Add CHTR & Mantis:0003736           *
* 109-07-15  V2.00.04  Amber      Update Mantis:0003763        			     *
* 109-07-16  V2.00.05  Amber      Update				       			     *
* 109-07-21  V2.00.06  Andy       Update Mantis3771          			     *
******************************************************************************/

package bilm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0275 extends BaseEdit {

	String gs_db_tmp = "";
	String gs_transaction_type = "";
	String strWhere = "";
	String ls_real_card_no = "";
	String bil_chtmain_name = "", ls_tmp = "", ls_where = "";
	String gs_office_m_code = "";
	String gs_office_code = "";
	String gs_cond_code = "";
	String gs_card_no = "";
	String gs_chi_name = "";
	String gs_telephone_no = "";
	String gs_uniform_no = "";
	String gs_id_no = "";
	String gs_id_code = "0";
	String gs_id_p_seqno = "";
	String gs_computer_no = "";
	String gs_db_action = "";
	String m_progName = "bilm0275";
	String gs_warning_msg = "";

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
			// insertFunc();
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "U")) {
			/* 更新功能 */
			// updateFunc();
			checkFunc();
		} else if (eqIgno(wp.buttonCode, "D")) {
			/* 刪除功能 */
			deleteFunc();
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
		} else if (eqIgno(wp.buttonCode, "S1")) {
			/* 新增check1 */
			strAction = "S1";
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "S2")) {
			/* 存檔 */
			strAction = "S2";
			checkFunc();
			// saveFunc();
		} else if (eqIgno(wp.buttonCode, "AJAX")) {
			/* 存檔 */
			itemchanged();
		} else if (eqIgno(wp.buttonCode, "itemchanged1")) {
			/* 存檔 */
			strAction = "itemchange1"; // 卡號itemchange
			itemchanged1();
		} else if (eqIgno(wp.buttonCode, "itemchanged2")) {
			/* 存檔 */
			strAction = "itemchange2"; // 身分證號itemchange
			itemchanged2();
		}

		dddwSelect();
		initButton();
	}

	// for query use only
	private boolean getWhereStr() throws Exception {

		// 查詢權限檢查，參考【f_auth_query】
		String ls_id = "";
		ls_id = wp.itemStr("ex_id");
		if (empty(ls_id)) {
			ls_id = wp.itemStr("ex_card_no");
		}
		busi.func.ColFunc func = new busi.func.ColFunc();
		func.setConn(wp);
		if (func.fAuthQuery(m_progName, ls_id) != 1) {
			alertErr(func.getMsg());
			return false;
		}

		wp.whereStr = " where 1=1 ";

		String id_p_seqno = "";
		if (empty(wp.itemStr("ex_id")) == false) {
			String sql_select = "select id_p_seqno from crd_idno where id_no = :ex_id_no";
			setString("ex_id_no", wp.itemStr("ex_id"));
			sqlSelect(sql_select);
			if (sqlRowNum > 0) {
				id_p_seqno = sqlStr("id_p_seqno");
			}
			wp.whereStr += " and  id_p_seqno = :id_p_seqno ";
			setString("id_p_seqno", id_p_seqno);
		}

		if (empty(wp.itemStr("ex_office_m_code")) == false) {
			wp.whereStr += " and  office_m_code = :ex_office_m_code ";
			setString("ex_office_m_code", wp.itemStr("ex_office_m_code"));
		}

		if (empty(wp.itemStr("ex_telephone_no")) == false) {
			wp.whereStr += " and  telephone_no = :ex_telephone_no ";
			setString("ex_telephone_no", wp.itemStr("ex_telephone_no"));
		}

		if (empty(wp.itemStr("ex_card_no")) == false) {
			wp.whereStr += " and  card_no = :ex_card_no ";
			setString("ex_card_no", wp.itemStr("ex_card_no"));
		}

		if (wp.itemStr("ex_apr_flag").equals("N")) {//ex_apr_flag為N,代表已放行,因此主檔已放行狀態為Y
			ls_tmp = " ,'Y' as db_tmp "; 
			bil_chtmain_name = "bil_chtmain";
		} else {
			ls_tmp = " ,'N' as db_tmp ";
			bil_chtmain_name = "bil_chtmain_t";
		}

		return true;
	}

	@Override
	public void queryFunc() throws Exception {
		if (getWhereStr() == false)
			return;
		// -page control-
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		// if (wp.itemStr("ex_apr_flag").equals("N")) {
		// if (empty(wp.itemStr("ex_office_m_code")) &&
		// empty(wp.itemStr("ex_telephone_no")) &&
		// empty(wp.itemStr("ex_card_no"))) {
		// alertMsg("機構 OR 用戶碼 OR 卡號 不可全部空白!!");
		// return;
		// }
		// }

		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		getWhereStr();
		////////////
		wp.selectSQL = " card_no "
				+ ", UF_IDNO_NAME(id_p_seqno) as db_cname"
				+ ", office_m_code"
				+ ", office_code"
				+ ", telephone_no"
				+ ", end_flag"
				+ ", transaction_type"
				+ ", computer_no"
				+ ", decode(transaction_type,'1','1:新增','2','2.修改','3','3.終止','4','4.刪除',transaction_type) as transaction_desc "
				+ ", feed_back_tx_flag"
				+ ", decode(feed_back_tx_flag,'P','是','Y','否',feed_back_tx_flag) as feed_back_tx_desc"
				+ ", effc_date"
				+ ", error_code1"
				+ ", confirm_date"
				+ ", confirm_flag"
				+ ", UF_2YMD(mod_time) as mod_date"
				+ ", mod_user"
				+ ", uniform_no "
				+ ls_tmp;

		wp.daoTable = bil_chtmain_name;
		wp.whereOrder = " order by telephone_no";

		pageQuery();

		wp.setListCount(1);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}

		wp.listCount[1] = wp.dataCnt;
		wp.setPageValue();

		int ls_selectCnt = wp.selectCnt;
		for (int j = 0; j < ls_selectCnt; j++) {
			if (!empty(wp.colStr(j, "uniform_no"))) {
				String ls_sql = " select wf_id,wf_desc  from ptr_sys_idtab where wf_type = 'PARK_CITY' and  wf_id=:wf_id  order by wf_id ";
				setString("wf_id", wp.colStr(j, "uniform_no"));
				sqlSelect(ls_sql);
				if (sqlRowNum > 0) {
					wp.colSet(j, "uniform_no", wp.colStr(j, "uniform_no") + " " + sqlStr("wf_desc"));
				}
			}
		}

		query_Summary(bil_chtmain_name);
	}

	void query_Summary(String chtmain_name) throws Exception {

		int iiins = 0;
		int iiupd = 0;
		int iiter = 0;
		String ls_sql = "";
		String id_p_seqno = "";

		if (chtmain_name.equals("bil_chtmain_t")) {
			ls_sql = "select transaction_type from bil_chtmain_t "
					+ "where 1=1 ";
			if (empty(wp.itemStr("ex_id")) == false) {
				String sql_select = "select id_p_seqno from crd_idno where id_no = :ex_id_no";
				setString("ex_id_no", wp.itemStr("ex_id"));
				sqlSelect(sql_select);
				if (sqlRowNum > 0) {
					id_p_seqno = sqlStr("id_p_seqno");
				}
				ls_sql += " and  id_p_seqno = :id_p_seqno ";
				setString("id_p_seqno", id_p_seqno);
			}

			if (empty(wp.itemStr("ex_office_m_code")) == false) {
				ls_sql += " and  office_m_code = :ex_office_m_code ";
				setString("ex_office_m_code", wp.itemStr("ex_office_m_code"));

			}

			if (empty(wp.itemStr("ex_telephone_no")) == false) {
				ls_sql += " and  telephone_no = :ex_telephone_no ";
				setString("ex_telephone_no", wp.itemStr("ex_telephone_no"));
			}

			if (empty(wp.itemStr("ex_card_no")) == false) {
				ls_sql += " and  card_no = :ex_card_no ";
				setString("ex_card_no", wp.itemStr("ex_card_no"));

			}

			sqlSelect(ls_sql);

			if (sqlRowNum > 0) {
				for (int ii = 0; ii < sqlRowNum; ii++) {
					if (sqlNum(ii, "transaction_type") == 1) {
						iiins++;
					} else if (sqlNum(ii, "transaction_type") == 2) {
						iiupd++;
					} else if (sqlNum(ii, "transaction_type") == 3) {
						iiter++;
					}
				}
			}

			wp.colSet("exIns", intToStr(iiins));
			wp.colSet("exUpd", intToStr(iiupd));
			wp.colSet("exTer", intToStr(iiter));
		}

		// String ls_sql1 = " select count(*) as tot_cnt1 from " + chtmain_name
		// + " " + ls_where + " and transaction_type='1' and bank_no <>'' ";
		// sqlSelect(ls_sql1);
		// iiins = (int) sqlNum("tot_cnt1");
		//
		// String ls_sql2 = " select count(*) as tot_cnt2 from " + chtmain_name
		// + " " + ls_where + " and transaction_type='2' and bank_no <>'' ";
		// sqlSelect(ls_sql2);
		// iiupd = (int) sqlNum("tot_cnt2");
		//
		// String ls_sql3 = " select count(*) as tot_cnt3 from " + chtmain_name
		// + " " + ls_where + " and transaction_type='3' and bank_no <>'' ";
		// sqlSelect(ls_sql3);
		// iiter = (int) sqlNum("tot_cnt3");
		//
		// wp.colSet("exFlg", int_2Str(iiins + iiupd + iiter));
		// wp.colSet("exIns", int_2Str(iiins));
		// wp.colSet("exUpd", int_2Str(iiupd));
		// wp.colSet("exTer", int_2Str(iiter));
	}

	@Override
	public void querySelect() throws Exception {

		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

		String ls_uniform_no = "";
		String ls_table_name = "";
		gs_db_tmp = itemKk("data_k4");
		if (empty(gs_db_tmp)) {
			gs_db_tmp = wp.itemStr("db_tmp");
		}

		if (gs_db_tmp.equals("Y")) {
			ls_table_name = "bil_chtmain";
		} else {
			ls_table_name = "bil_chtmain_t";
		}

		gs_telephone_no = itemKk("data_k1");
		if (empty(gs_telephone_no)) {
			gs_telephone_no = wp.itemStr("telephone_no");
		}
		gs_office_m_code = itemKk("data_k2");
		if (empty(gs_office_m_code)) {
			gs_office_m_code = wp.itemStr("office_m_code");
		}
		gs_office_code = itemKk("data_k3");
		if (empty(gs_office_code)) {
			gs_office_code = wp.itemStr("office_code");
		}

		gs_transaction_type = itemKk("data_k5");
		if (empty(gs_transaction_type)) {
			gs_transaction_type = wp.itemStr("transaction_type");
		}

		gs_card_no = itemKk("data_k6");
		if (empty(gs_card_no)) {
			gs_card_no = wp.itemStr("gs_card_no");
		}

		// gs_uniform_no = itemKk("data_k7").substring(0,8);
		gs_uniform_no = itemKk("data_k7");
		if (empty(gs_uniform_no)) {
			gs_uniform_no = wp.itemStr("uniform_no");
		} else {
			gs_uniform_no = itemKk("data_k7").substring(0, 8);
		}

		wp.selectSQL = "hex(rowid) as rowid, mod_seqno "
				+ ", telephone_no "
				+ ", tel_chkmark"
				+ ", office_m_code"
				+ ", office_code"
				+ ", id_p_seqno "
				+ ", card_no"
				+ ", UF_IDNO_NAME(card_no) as id_name"
				+ ", transaction_type"
				+ ", computer_no"
				+ ", UF_2YMD(mod_time) as mod_date"
				+ ", confirm_flag"
				+ ", confirm_flag as db_confirm_flag "
				+ ", confirm_date"
				+ ", feed_back_date"
				+ ", error_code "
				+ ", error_code1"
				+ ", bank_no"
				+ ", effc_date"
				+ ", uniform_no "
				+ ", remark"
				+ ", feed_back_tx_flag ";
		if (ls_table_name.equals("bil_chtmain_t")) {
			wp.selectSQL += ", master_rowid ";
		}

		if (gs_db_tmp.equals("Y")) {
			wp.selectSQL += " ,'Y' as db_tmp ";
			wp.daoTable = ls_table_name;
		} else {
			wp.selectSQL += " ,'N' as db_tmp ";
			wp.daoTable = ls_table_name;
		}
		wp.whereStr = "where 1=1";
		wp.whereStr += " and  telephone_no = :telephone_no ";
		setString("telephone_no", gs_telephone_no);
		wp.whereStr += " and  office_m_code = :office_m_code ";
		setString("office_m_code", gs_office_m_code);
		wp.whereStr += " and  office_code = :office_code ";
		setString("office_code", gs_office_code);
		wp.whereStr += " and  card_no = :card_no ";
		setString("card_no", gs_card_no);
		if (gs_office_m_code.equals("CHTR")) {
			wp.whereStr += " and  uniform_no = :uniform_no ";
			setString("uniform_no", gs_uniform_no);
		}

		pageSelect();

		if (sqlNotFind()) {
			alertErr("查無資料, telephone_no=" + gs_telephone_no);
		}
		ls_uniform_no = wp.colStr("uniform_no");
		String ls_sql = "";
		ls_sql = "select wf_desc from ptr_sys_idtab "
				+ "where wf_type = 'PARK_CITY' "
				+ "and wf_id =:uniform_no ";
		setString("uniform_no", ls_uniform_no);
		sqlSelect(ls_sql);
		if (sqlRowNum > 0) {
			wp.colSet("uniform_chi_name", sqlStr("wf_desc"));
		}

		ls_sql = "select * from bil_office_m where office_m_code =:office_m_code  ";
		setString("office_m_code", gs_office_m_code);
		sqlSelect(ls_sql);
		String ls_office_m_name = sqlStr("office_m_name");

		wp.colSet("office_m_code_desc", ls_office_m_name);

		String ls_sql2 = "select * from bil_office where office_code =:office_code  and office_m_code=:office_m_code ";
		setString("office_code", gs_office_code);
		setString("office_m_code", gs_office_m_code);
		sqlSelect(ls_sql2);
		String ls_office_name = sqlStr("office_name");

		wp.colSet("office_code_desc", ls_office_name);

		String ls_sql3 = " select id_no,id_no_code from crd_idno where id_p_seqno=:id_p_seqno ";
		setString("id_p_seqno", wp.colStr("id_p_seqno"));
		sqlSelect(ls_sql3);
		String ls_id_no = sqlStr("id_no");
		String ls_id_no_code = sqlStr("id_no_code");

		wp.colSet("id_no", ls_id_no);
		wp.colSet("id_code", ls_id_no_code);

		// if (gs_transaction_type.equals("1")) {
		// String option = "<option value='1' ${db_action-1}>1.新增</option>";
		// wp.colSet("option_val", option);
		// }
		// option += "<option value='" + sqlStr(ii, "product_no") + "'
		// ${product_no-"+ sqlStr(ii, "product_no")+"} >" + sqlStr(ii,
		// "product_name") + "</option>";

		if (gs_transaction_type.equals("2")) {
			wp.colSet("db_action", "2");
		}
		if (gs_transaction_type.equals("3")) {
			wp.colSet("db_action", "3");
		}
	}

	@Override
	public void saveFunc() throws Exception {

		Bilm0275_func func = new Bilm0275_func(wp);

		gs_office_m_code = wp.itemStr("kk_office_m_code");
		if (empty(gs_office_m_code)) {
			gs_office_m_code = wp.itemStr("office_m_code");
		}
		gs_office_code = wp.itemStr("office_code");
		gs_telephone_no = wp.itemStr("telephone_no");
		gs_computer_no = wp.itemStr("computer_no");
		gs_id_no = wp.itemStr("id_no");
		gs_card_no = wp.itemStr("card_no");
		gs_transaction_type = itemKk("data_k5");
		gs_db_action = wp.itemStr("db_action");
		gs_id_p_seqno = wp.itemStr("id_p_seqno");
		gs_uniform_no = wp.itemStr("uniform_no");
		String ls_db_tmp = wp.itemStr("db_tmp");
		
		if (empty(gs_transaction_type)) {
			gs_transaction_type = wp.itemStr("transaction_type");
		}
		if (strAction.equals("D")) {
			// delete
			if (wp.itemStr("db_tmp").equals("Y")) {
				;  //不寫tmp檔
				//alertErr("bil_chtmain主檔不可刪除!");
				//return;
			}

		}
		//System.out.println("ls_db_tmp :" + ls_db_tmp + " strAction:" + strAction);
		// 20190521 用db_tmp判斷資料是新增(修改資料)或主檔讀出之新增(異動/終止)資料

		if (strAction.equals("A") || strAction.equals("U")) {
			if (ls_db_tmp.equals("N")) {
				if (strAction.equals("A")) {
					if (of_validation() < 0) {
						return;
					}
					strAction = "A";
				} else {
					if (of_validation1() < 0) {
						return;
					}
				}
			} else {
				if (of_validation1() < 0) {
					return;
				}
			}
		}
		//System.out.println("strAction2 : "+strAction);
		rc = func.dbSave(strAction);
		log(func.getMsg());
		if (rc != 1) {
			alertErr(func.getMsg());
		}
		this.sqlCommit(rc);
		if (strAction.equals("A")) {
			if (rc == 1 && (gs_office_m_code.equals("CHTC") || gs_office_m_code.equals("CHTG"))) {
				alertMsg("新增存檔成功!!");
			}
		}
		if (strAction.equals("U")) {
			if (rc == 1)
				alertMsg("資料存檔成功!!");
		}

	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
		if (wp.respHtml.indexOf("_add") > 0) {
			this.btnModeAud();
		}
	}

	@Override
	public void dddwSelect() {
		try {
			if (wp.respHtml.indexOf("_add") > 0) {
				wp.initOption = "--";
				wp.optionKey = wp.itemStr("kk_office_m_code");
				this.dddwList("dddw_office_m_code", "bil_office_m", "office_m_code", "office_m_name", "where 1=1 and office_m_code != 'CHTR' order by office_m_code");
			} else {
				wp.initOption = "--";
				wp.optionKey = wp.itemStr("ex_office_m_code");
				this.dddwList("dddw_office_m_code", "bil_office_m", "office_m_code", "office_m_name", "where 1=1 order by office_m_code");
			}

			wp.initOption = "--";
			wp.optionKey = wp.itemStr("office_code");
			this.dddwList("dddw_office_code", "bil_office", "office_code", "office_name", "where 1=1 order by office_code");

		} catch (Exception ex) {
		}
	}

	public int wf_check_cardno(String cardno, String office_m_code) throws Exception {
		String group_code = "";
		String current_code = "";
		Double oppost_date = 0.0;
		String sup_flag = "";
		String p_seqno = "";
		String ls_block_reason = "";
		String ls_block_reason21 = "";
		String ls_block_reason22 = "";
		String ls_block_reason23 = "";
		String ls_block_reason24 = "";
		String ls_block_status = "";

		String ls_sql = "select c.current_code,c.group_code,c.oppost_date,c.acno_p_seqno,c.sup_flag,a.block_status "
				+ " , decode(a.block_reason1,'61','','71','','72','','73','','74','','81','','91','',a.block_reason1) as ls_block_reason "
				+ " , decode(a.block_reason2,'61','','71','','72','','73','','74','','81','','91','',a.block_reason2) as ls_block_reason21 "
				+ " , decode(a.block_reason3,'61','','71','','72','','73','','74','','81','','91','',a.block_reason3) as ls_block_reason22 "
				+ " , decode(a.block_reason4,'61','','71','','72','','73','','74','','81','','91','',a.block_reason4) as ls_block_reason23 "
				+ " , decode(a.block_reason5,'61','','71','','72','','73','','74','','81','','91','',a.block_reason5) as ls_block_reason24 "
				+ " from crd_card c left join cca_card_acct a on c.acno_p_seqno = a.acno_p_seqno ";
		ls_sql += "    where   a.debit_flag='N'  and  card_no = :cardno ";
		setString("cardno", cardno);
		sqlSelect(ls_sql);
		if (sqlRowNum > 0) {
			group_code = sqlStr("group_code");
			current_code = sqlStr("current_code");
			oppost_date = sqlNum("oppost_date");
			sup_flag = sqlStr("sup_flag");
			p_seqno = sqlStr("acno_p_seqno");
			ls_block_reason = sqlStr("ls_block_reason");
			ls_block_reason21 = sqlStr("ls_block_reason21");
			ls_block_reason22 = sqlStr("ls_block_reason22");
			ls_block_reason23 = sqlStr("ls_block_reason23");
			ls_block_reason24 = sqlStr("ls_block_reason24");
			ls_block_status = sqlStr("block_status");
		}

		if (sqlCode != 0 || !current_code.equals("0")) {
			// 無效
			return -1;
		}

		if (ls_block_status.equals("11") &&
				(!empty(ls_block_reason) ||
						!empty(ls_block_reason21) ||
						!empty(ls_block_reason22) ||
						!empty(ls_block_reason23) ||
						!empty(ls_block_reason24))) {
			// 無效
			return -1;
		}

		if (ls_block_status.equals("12") &&
				(!empty(ls_block_reason) ||
						!empty(ls_block_reason21) ||
						!empty(ls_block_reason22) ||
						!empty(ls_block_reason23) ||
						!empty(ls_block_reason24))) {
			// 無效
			return -1;
		}

		String ls_sql2 = "select decode(group_code1,'','0',group_code1) as group_code1,decode(group_code2,'','0',group_code2) as group_code2,"
				+ "decode(group_code3,'','0',group_code3) as group_code3,decode(group_code4,'','0',group_code4) as group_code4,"
				+ "decode(group_code5,'','0',group_code5) as group_code5 ";
		ls_sql2 += " from bil_office_time where office_m_code= :office_m_code";
		setString("office_m_code", office_m_code);
		sqlSelect(ls_sql2);
		if (sqlCode != 0) {
			// 團代
			return 2;
		}
		if (sqlRowNum > 0) {
			if ((!sqlStr("group_code1").equals("0") ||
					!sqlStr("group_code2").equals("0") ||
					!sqlStr("group_code3").equals("0") ||
					!sqlStr("group_code4").equals("0") ||
					!sqlStr("group_code5").equals("0")) &&
					(!group_code.equals(sqlStr("group_code1")) &&
							!group_code.equals(sqlStr("group_code2")) &&
							!group_code.equals(sqlStr("group_code3")) &&
							!group_code.equals(sqlStr("group_code4")) &&
							!group_code.equals(sqlStr("group_code5")))) {
				// 團代
				return 2;
			}
		}

		String ls_busidate = "select business_date from ptr_businday Fetch First 1 Row Only";
		sqlSelect(ls_busidate);
		sqlNum("business_date");
		Double busidate = sqlNum("business_date");
		if (!current_code.equals("0") &&
				(busidate > oppost_date)) {
			// 停用
			return 3;
		}

		if (sup_flag.equals("1")) {
			// 附卡
			return 6;
		}

		String ls_sql3 = "select   status_change_date,acct_status,pay_by_stage_flag from act_acno";
		ls_sql3 += " where acno_p_seqno = :acno_p_seqno";
		setString("acno_p_seqno", p_seqno);
		sqlSelect(ls_sql3);
		if (sqlRowNum <= 0 ||
				sqlNum("pay_by_stage_flag") != 00) {
			// 分期
			return 4;
		}

		if ((sqlNum("acct_status") == 3 ||
				sqlNum("acct_status") == 4) &&
				busidate > sqlNum("status_change_date")) {
			// 催收 :呆帳
			return 5;
		}

		// 正常
		return 1;
	}

	/*
	 * 20200616 移至html int wf_chk_tel(String as_mcode, String as_code, String
	 * as_tel) { long ll_sum = 0; long[] ll_tel = new long[12]; String ls_tmp =
	 * "", ls_tmp2 = ""; // 電號 if (as_mcode.equals("TE")) { as_code = (as_code +
	 * as_tel).substring(0, 10); ll_sum = 0; for (int i = 0; i < 10; i++) {
	 * ll_tel[i] = to_Int(as_code.substring(i, i + 1)); if (i % 2 == 0) { ll_sum
	 * += ll_tel[i]; } else {
	 * 
	 * if ((ll_tel[i] * 2) >= 10) { ll_sum += to_Int(int_2Str((int) ll_tel[i] *
	 * 2).substring(0, 1)); ll_sum += ((ll_tel[i] * 2) - 10); } else { ll_sum +=
	 * (ll_tel[i]) * 2; } } }
	 * 
	 * ls_tmp = int_2Str((int) ll_sum).substring(int_2Str((int) ll_sum).length()
	 * - 1);
	 * 
	 * if (as_tel.substring(as_tel.length() - 1) == ls_tmp) { return 1;
	 * 
	 * } else { return 0; }
	 * 
	 * }
	 * 
	 * // 市水 if (as_mcode.equals("TW")) { as_code = (as_tel).substring(0, 6);
	 * ll_sum = 0; for (int i = 0; i < 6; i++) { ll_tel[i] =
	 * to_Int(as_code.substring(i, i + 1)); if (i % 2 == 0) { ll_sum +=
	 * ll_tel[i]; } else { ll_sum += (ll_tel[i] * 2); } }
	 * 
	 * ls_tmp = int_2Str(10 - (int) (ll_sum % 10));
	 * 
	 * if (as_tel.substring(as_tel.length() - 1) == ls_tmp) { return 1; } else {
	 * return 0; } }
	 * 
	 * // 省水 if (as_mcode.equals("CC")) { String ls_cc =
	 * "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; String ls_cc2 =
	 * "12345678912345678912345678";
	 * 
	 * for (int i = 0; i < 2; i++) { ls_tmp = "0"; if
	 * (to_Int(as_code.substring(i, i + 1)) <= 9) { continue; }
	 * 
	 * if (to_Int(as_code.substring(i, i + 1)) > 9) { ls_tmp =
	 * as_code.substring(i, i + 1); }
	 * 
	 * // 轉abc=123 if (!ls_tmp.equals("0")) { for (int j = 0; j < 26; j++) {
	 * ls_tmp2 = ls_cc.substring(j, j + 1); if (ls_tmp.equals(ls_tmp2)) { ls_tmp
	 * = ls_cc2.substring(j, j + 1); }
	 * 
	 * } }
	 * 
	 * if (i == 0) { as_code = ls_tmp + as_code.substring(i, i + 1); }
	 * 
	 * if (i == 1) { as_code = as_code.substring(0, 1) + ls_tmp +
	 * as_code.substring(i, i + 1); }
	 * 
	 * }
	 * 
	 * // goto TAG_cc if ((as_code + as_tel).length() != 10) {
	 * alertMsg("用戶號號長度不符:" + (as_code + as_tel)); return 0; } as_code =
	 * (as_code + as_tel).substring(0, 10); ll_sum = 0; int j = 0, k = 0; for
	 * (int i = 0; i < 10; i++) { ll_tel[i] = to_Int(as_code.substring(i, i +
	 * 1)); if (i < 5) { // 權數k k = i; } else { j += 1; k = 11 - j; } ll_sum +=
	 * (ll_tel[i] * k); }
	 * 
	 * j = (int) (ll_sum % 11); // 餘數 if (j == 0) { ls_tmp = "0"; } else if (j
	 * == 1) { ls_tmp = "k"; } else { ls_tmp = int_2Str(11 - (int) (ll_sum %
	 * 11)); // 省水查核碼.求11的補數 }
	 * 
	 * if (as_tel.substring(as_tel.length() - 1) == ls_tmp) { return 1; } else {
	 * return 0; } }
	 * 
	 * return 1; }
	 */
	void checkFunc() throws Exception {
		String ls_telephone_no = wp.itemStr("telephone_no");// 車牌
		String ls_office_m_code = wp.itemStr("office_m_code");// 機構代號
		String ls_office_code = wp.itemStr("office_code");// 分支機構
		String ls_feed_back_tx_flag = "";
		//
		if (gs_db_action.equals("1")) {
			alertErr("主檔資料已存在作業代號不可新增 !!");
			return;
		}

		String ls_sql = " select feed_back_tx_flag,"
				+ "transaction_type "
				+ "from bil_chtmain "
				+ "where telephone_no=:telephone_no "
				+ "  and office_m_code=:office_m_code "
				+ "  and office_code=:office_code ";
		setString("telephone_no", ls_telephone_no);
		setString("office_m_code", ls_office_m_code);
		setString("office_code", ls_office_code);
		sqlSelect(ls_sql);
		ls_feed_back_tx_flag = sqlStr("feed_back_tx_flag");
		if (!ls_feed_back_tx_flag.equals("P") && wp.itemStr("db_action").equals("2")) {
			alertErr("資料尚未送出不可異動/終止 !!");
			return;
		}

		if (!ls_feed_back_tx_flag.equals("P") && wp.itemStr("db_action").equals("3")) {
			alertErr("資料尚未送出不可異動/終止 !!");
			return;
		}
		
		/*不寫tmp這一段要點掉
		if (wp.itemStr("db_tmp").equals("Y")) {
			// insert tmp
			strAction = "A";

		} else if (wp.itemStr("db_tmp").equals("N")) {
			// update tmp
			strAction = "U";
		}
		*/
		saveFunc();
	}

	public int itemchanged() throws Exception {
		String ajaxName = "";
		String ls_office_m_code = "";
		String dddw_where = "";
		String option = "";
		String ls_bank_no = "006";
		ajaxName = wp.itemStr("ajaxName");
		setSelectLimit(0);
		switch (ajaxName) {
		case "office_m_code":
			ls_office_m_code = wp.itemStr("a_office_m_code");
			dddw_where = " and office_m_code = :office_m_code ";
			String ls_sql = "select office_code "
					+ " ,office_code||'_'||office_name as office_name "
					+ " from bil_office "
					+ " where 1=1 "
					+ dddw_where
					+ " order by office_code ";
			setString("office_m_code", ls_office_m_code);
			sqlSelect(ls_sql);
			if (sqlRowNum <= 0) {
				break;
			}
			option += "<option value=\"\">--</option>";
			for (int ii = 0; ii < sqlRowNum; ii++) {
				option += "<option value=\"" + sqlStr(ii, "office_code") + "\">" + sqlStr(ii, "office_name") + "</option>";
			}
			wp.addJSON("dddw_office_code", option);
			break;

		case "idName":
			String ls_id = wp.itemStr("id");
			dddw_where = " and id_no = :id ";
			String ls_sql2 = "select chi_name, id_p_seqno "
					+ " from crd_idno "
					+ " where 1=1 "
					+ dddw_where
					+ " order by chi_name ";
			setString("id", ls_id);
			sqlSelect(ls_sql2);

			if (sqlRowNum <= 0) {
				wp.addJSON("id_name", "查無身分證");
				break;
			} else {
				gs_id_no = ls_id;
				gs_id_p_seqno = sqlStr("id_p_seqno");
			}
			wp.addJSON("id_name", sqlStr("chi_name"));
			wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));

			break;
		case "cardDesp":
			wp.addJSON("card_desp", "");
			ls_bank_no = "key";
			String ls_card_no = wp.itemStr("card_no");
			dddw_where = " and card_no = :card_no ";
			String ls_sql3 = "select a.chi_name, "
					+ "a.id_no, "
					+ "a.id_no_code, "
					+ "a.id_p_seqno,"
					+ "b.sup_flag "
					+ "from crd_idno a , crd_card b "
					+ "where 1=1 "
					+ dddw_where
					+ "and a.id_p_seqno = b.id_p_seqno ";
			setString("card_no", ls_card_no);
			sqlSelect(ls_sql3);
			if (sqlRowNum <= 0) {
				wp.addJSON("card_desp", "卡號無效");
				wp.addJSON("id_no", "");
				wp.addJSON("id_name", "");
				wp.addJSON("id_name1", "");
				wp.addJSON("id_p_seqno", "");
				wp.addJSON("id_code", "");
				break;
			} else {
				if(sqlStr("sup_flag").equals("1")){
					wp.addJSON("card_desp", "資料錯誤: 此為附卡!!");
					wp.addJSON("id_no", "");
					wp.addJSON("id_name", "");
					wp.addJSON("id_name1", "");
					wp.addJSON("id_p_seqno", "");
					wp.addJSON("id_code", "");
					break;
				}
				gs_id_no = sqlStr("id_no");
				gs_id_p_seqno = sqlStr("id_p_seqno");
				wp.addJSON("id_no", sqlStr("id_no"));
				wp.addJSON("id_name", sqlStr("chi_name"));
				wp.addJSON("id_name1", sqlStr("chi_name"));
				wp.addJSON("id_p_seqno", sqlStr("id_p_seqno"));
				wp.addJSON("id_code", sqlStr("id_no_code"));
			}
			// 台電
			if (ls_office_m_code.equals("TE") & ls_card_no.length() == 15) {
				alertErr("台電資料, 不可使用 AE 卡 !!");
				return -1;
			}
			break;
		}
		wp.addJSON("bank_no", ls_bank_no);
		return 1;
	}

	void itemchanged1() throws Exception {

		String kk_card_no = "", ls_sql = "";
		kk_card_no = wp.itemStr("card_no");
		ls_sql = "select a.chi_name, "
				+ "a.id_no, "
				+ "a.id_no_code, "
				+ "a.id_p_seqno, "
				+ "b.sup_flag "
				+ "from crd_idno a , crd_card b "
				+ "where 1=1 "
				+ "and b.card_no =:card_no "
				+ "and a.id_p_seqno = b.id_p_seqno "
				+ "and b.current_code = '0' ";
		setString("card_no", kk_card_no);
		sqlSelect(ls_sql);
		if (sqlRowNum <= 0) {
			wp.colSet("card_desp", "輪入卡號無效或非持卡人之有效卡");
			wp.colSet("id_no", "");
			wp.colSet("id_p_seqno", "");
			wp.colSet("id_name", "");
		} else {
			if(sqlStr("sup_flag").equals("1")){
				wp.colSet("card_desp", "資料錯誤: 此為附卡!!");
				wp.colSet("id_no", "");
				wp.colSet("id_p_seqno", "");
				wp.colSet("id_name", "");
				return;
			}
			gs_id_no = sqlStr("id_no");
			gs_id_p_seqno = sqlStr("id_p_seqno");
			wp.colSet("id_no", sqlStr("id_no"));
			wp.colSet("id_name", sqlStr("chi_name"));
			wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
		}
		return;
	}

	void itemchanged2() throws Exception {
		String ls_sql = "";
		String ls_id_p_seqno = "";
		String kk_id_no = wp.itemStr("id_no");
		ls_sql = "select chi_name, id_p_seqno "
				+ " from crd_idno "
				+ " where 1=1 "
				+ "and id_no =:id_no ";
		setString("id_no", kk_id_no);
		sqlSelect(ls_sql);
		if (sqlRowNum <= 0) {
			wp.colSet("id_name", "查無卡人資料");
		} else {
			ls_id_p_seqno = sqlStr("chi_name");
			wp.colSet("id_name", sqlStr("chi_name"));
			wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
		}
		// 20200528 add by Andy :身分證附加檢核輸入卡號之有效性
		if (!empty(wp.itemStr("card_no"))) {
			ls_sql = "select id_p_seqno,"
					+ "sup_flag "
					+ "from crd_card where card_no =:card_no and current_code = '0' ";
			setString("card_no", wp.itemStr("card_no"));
			sqlSelect(ls_sql);
			if (sqlRowNum == 0) {
				wp.colSet("card_desp", "卡號無效或非有效卡");
			} else {
				if(sqlStr("sup_flag").equals("1")){
					wp.colSet("card_desp", "資料錯誤: 此為附卡!!");
					return;
				}
				if (!sqlStr("id_p_seqno").equals(ls_id_p_seqno)) {
					wp.colSet("card_desp", "輪入卡號非持卡人之有效卡");
				}
			}
		}
		return;
	}

	// 適用insert新增資料之檢核
	public int of_validation() throws Exception {
		String ls_sql = "";
		// chk bil_chtmain duplicates
		ls_sql = "select count(*) as ct "
				+ "from bil_chtmain "
				+ "where telephone_no  = :ls_telephone_no "
				+ "and office_m_code  = :ls_office_m_code "
				+ "and office_code    = :ls_office_code "
				+ "and uniform_no    = :ls_uniform_no";
		setString("ls_telephone_no", gs_telephone_no);
		setString("ls_office_m_code", gs_office_m_code);
		setString("ls_office_code", gs_office_code);
		setString("ls_uniform_no", gs_uniform_no);
		sqlSelect(ls_sql);
		if (sqlRowNum > 0) {
			if (sqlNum("ct") > 0) {
				alertErr("主檔資料已存在, 不可新增 !!");
				return -1;
			}
		}

		// chk bil_chtmain_t duplicates
		ls_sql = "select count(*) as ct "
				+ "from bil_chtmain "
				+ "where telephone_no = :ls_telephone_no "
				+ "and office_m_code = :ls_office_m_code "
				+ "and office_code   = :ls_office_code";
		setString("ls_telephone_no", gs_telephone_no);
		setString("ls_office_m_code", gs_office_m_code);
		setString("ls_office_code", gs_office_code);
		sqlSelect(ls_sql);
		if (sqlRowNum > 0) {
			if (sqlNum("ct") > 0) {
				alertErr("資料已存在, 不可新增 !!");
				return -1;
			}
		}

		// check
		if (empty(gs_office_m_code)) {
			alertErr("機構代號不可空白!");
			return -1;
		}
		if (empty(gs_telephone_no)) {
			alertErr("用戶號碼不可空白!");
			return -1;
		}
		if (empty(gs_card_no) & empty(gs_id_no)) {
			alertErr("卡號 與 ID , 不可同時為空白 !!");
			return -1;
		}

		// 20200528 add by Andy :身分證附加檢核輸入卡號之有效性
		String ls_id_p_seqno = wp.itemStr("id_p_seqno");
		String ls_card_no = wp.itemStr("card_no");
		if (!empty(ls_id_p_seqno) && !empty(ls_card_no)) {
			ls_sql = "select id_p_seqno,"
					+ "sup_flag  "
					+ "from crd_card "
					+ "where card_no =:card_no and current_code = '0' ";
			setString("card_no", wp.itemStr("card_no"));
			sqlSelect(ls_sql);
			if (sqlRowNum == 0) {
				alertErr("卡號無效或非有效卡");
				return -1;
			} else {
				if(sqlStr("sup_flag").equals("1")){
					alertErr("此為附卡!!");
					return -1;
				}
				if (!sqlStr("id_p_seqno").equals(ls_id_p_seqno)) {
					alertErr("輪入卡號非持卡人之有效卡");
					return -1;
				}
			}
		}

		/*
		 * 20200616 移至html // 大台北瓦斯 int li_1 = 0, li_2 = 0, li_3 = 0, li_4 = 0,
		 * li_5 = 0, li_6 = 0, li_7 = 0, li_8 = 0, li_9 = 0, li_0 = 0; if
		 * (gs_office_m_code.equals("CHTG")) { if
		 * (!isNumber(wp.itemStr("telephone_no"))) {
		 * alertErr("機構代碼為CHTG,用戶號碼必須為9碼數字"); return -1; }
		 * 
		 * li_1 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 0, 1));
		 * li_2 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 1, 1));
		 * li_3 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 2, 1));
		 * li_4 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 3, 1));
		 * li_5 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 4, 1));
		 * li_6 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 5, 1));
		 * li_7 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 6, 1));
		 * li_8 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 7, 1));
		 * li_9 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 8, 1));
		 * li_0 = (li_1 * 8 + li_2 * 7 + li_3 * 6 + li_4 * 5 + li_5 * 4 + li_6 *
		 * 3 + li_7 * 2 + li_8 * 1); li_0 = (11 - li_0 % 11) % 10;
		 * 
		 * if (li_0 != li_9) { // // if (item_eq("conf_flag", "Y") == true) //
		 * return -1; // wp.colSet("conf_mesg", "警示 1~此資料檢查碼錯誤,是否強制放行"); //
		 * wp.respCode = "01"; // wp.colSet("conf_cond", " || 1==1 ");
		 * wp.colSet("w_mesg", "警示  1~此資料檢查碼錯誤!!"); gs_warning_msg =
		 * "警示:此資料檢查碼錯誤!!"; }
		 * 
		 * } else if (gs_office_m_code.equals("TE") ||
		 * gs_office_m_code.equals("TW") || gs_office_m_code.equals("CC")) { int
		 * li_count1 = 0;
		 * 
		 * switch (gs_office_m_code) { case "TE": // 台電 li_count1 =
		 * wf_chk_tel("TE", wp.itemStr("office_code"),
		 * wp.itemStr("telephone_no")); break; case "TW": // 市水 li_count1 =
		 * wf_chk_tel("TW", wp.itemStr("office_code"),
		 * wp.itemStr("telephone_no")); break; case "CC": // 省水 li_count1 =
		 * wf_chk_tel("CC", wp.itemStr("office_code"),
		 * wp.itemStr("telephone_no")); break; }
		 * 
		 * if (li_count1 == 0) { // if (item_eq("conf_flag", "Y") == true) //
		 * return -1; // wp.colSet("conf_mesg", "公用事業~費用代繳之用戶號碼檢核不符,是否強制輸入?");
		 * // wp.respCode = "01"; // wp.colSet("conf_cond", " || 1==1 ");
		 * wp.colSet("w_mesg", "警示:公用事業~費用代繳之用戶號碼檢核不符!!"); gs_warning_msg =
		 * "警示:公用事業~費用代繳之用戶號碼檢核不符!!"; } } else if
		 * (gs_office_m_code.equals("CHTC") || gs_office_m_code.equals("CHTR"))
		 * { try { strAction = "A"; // insertFunc(); } catch (Exception e) {
		 * e.printStackTrace(); } }
		 */
//		if (gs_office_m_code.equals("CHTC") || gs_office_m_code.equals("CHTR")) {
//			try {
//				strAction = "A";
//				// insertFunc();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

		if (empty(gs_card_no)) {
			if (wf_get_next_cardno(gs_id_no) != 1)
				return -1;
		}

		// 台電
		if (gs_office_m_code.equals("TE") & gs_card_no.length() == 15) {
			alertErr("台電資料, 不可使用 AE 卡 !!");
			return -1;
		}

		// wf_check_office_m
		String ls_sql2 = "select count(*) as li_count from bil_office_m where office_m_code =:ls_office_m_code  ";
		setString("ls_office_m_code", gs_office_m_code);
		sqlSelect(ls_sql2);
		if (sqlNum("li_count") < 1) {
			alertErr("機構代碼 不存在!");
			return -1;
		}

		// wf_check_office
		String ls_sql3 = "select count(*) as li_count from bil_office "
				+ "where office_m_code =:ls_office_m_code  and office_code=:ls_office_code";
		setString("ls_office_m_code", gs_office_m_code);
		setString("ls_office_code", gs_office_code);
		sqlSelect(ls_sql3);
		if (sqlNum("li_count") < 1) {
			alertErr("機構分支代碼 不存在!");
			return -1;
		}
		// check id_no ==>crd_card
		int li_count11 = 0;
		if (!empty(wp.itemStr("id"))) {
			String ls_cardno = "select count(*) as li_count from crd_card c left join crd_idno i on c.id_p_seqno = i.id_p_seqno where i.id_no =:id and i.id_no_code =:id_code  and c.current_code = '0' ";
			setString("id", wp.itemStr("id"));
			setString("id_code", "0");
			sqlSelect(ls_cardno);
			li_count11 = (int) sqlNum("li_count");
			if (li_count11 == 0) {
				alertErr("此 ID 無有效卡 , 不可新增 !!");
				return -1;
			}
		}
		// 卡號必須為本行卡且為有效卡
		if (!empty(wp.itemStr("card_no"))) {

			int checard = 0;
			checard = wf_check_cardno(wp.itemStr("card_no"), wp.itemStr("kk_office_m_code"));

			switch (checard) {
			case -1:
				alertErr("此為無效卡!");
				return -1;
			case 2:
				alertErr("此團代不符 (check office_time) !");
				return -1;
			case 3:
				alertErr("此為停用卡 !!");
				return -1;
			case 4:
				alertErr("此為分期戶 !!");
				return -1;
			case 5:
				alertErr("此為 催收/呆帳 !!");
				return -1;
			case 6:
				alertErr("此為 附卡 !!");
				return -1;
			}
		}
		return 1;
	}

	// 適用update之檢核
	public int of_validation1() throws Exception {
		String ls_sql = "";
		String ls_db_tmp = wp.itemStr("db_tmp");
		if(wp.itemStr("db_tmp").equals("Y")) {
			
			ls_sql = "select count(*)ct "
					+ "from bil_chtmain_t "
					+ "where 1=1 ";
			ls_sql += sqlCol(gs_card_no,"card_no");
			ls_sql += sqlCol(gs_telephone_no,"telephone_no");
			ls_sql += sqlCol(gs_office_m_code,"office_m_code");
			ls_sql += sqlCol(gs_office_code,"office_code");
			ls_sql += sqlCol(gs_uniform_no,"uniform_no");
			//System.out.println("sql :"+ls_sql);
			sqlSelect(ls_sql);
			
			if (sqlInt("ct") > 0) {
				alertErr("已有待覆核資料，請至「未放行」做修改 !");
				return -1;
			}
		}

		// check
		if(empty(gs_uniform_no) && gs_office_m_code.equals("CHTR")) {
			alertErr("機構代號不可空白!");
			return -1;
		}
		if(gs_db_action.equals("3")) {
			return 1;
		}
		
		if (empty(gs_office_m_code)) {
			alertErr("機構代號不可空白!");
			return -1;
		}
		if (empty(gs_telephone_no)) {
			alertErr("用戶號碼不可空白!");
			return -1;
		}
		if (empty(gs_card_no) & empty(gs_id_no)) {
			alertErr("卡號 與 ID , 不可同時為空白 !!");
			return -1;
		}

		// 20200528 add by Andy :身分證附加檢核輸入卡號之有效性
		String ls_id_p_seqno = wp.itemStr("id_p_seqno");
		String ls_card_no = wp.itemStr("card_no");
		if (!empty(ls_id_p_seqno) && !empty(ls_card_no)) {
			ls_sql = "select id_p_seqno,"
					+ "sup_flag "
					+ "from crd_card "
					+ "where card_no =:card_no "
					+ "and current_code = '0' ";
			setString("card_no", wp.itemStr("card_no"));
			sqlSelect(ls_sql);
			if (sqlRowNum == 0) {
				alertErr("卡號無效或非有效卡");
				return -1;
			} else {
				if(sqlStr("sup_flag").equals("1")){
					alertErr("此為附卡!!");
					return -1;
				}
				if (!sqlStr("id_p_seqno").equals(ls_id_p_seqno)) {
					alertErr("輪入卡號非持卡人之有效卡");
					return -1;
				}
			}
		}

		/*
		 * 20200616 移至html // 大台北瓦斯 int li_1 = 0, li_2 = 0, li_3 = 0, li_4 = 0,
		 * li_5 = 0, li_6 = 0, li_7 = 0, li_8 = 0, li_9 = 0, li_0 = 0; if
		 * (gs_office_m_code.equals("CHTG")) {
		 * 
		 * li_1 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 0, 1));
		 * li_2 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 1, 1));
		 * li_3 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 2, 1));
		 * li_4 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 3, 1));
		 * li_5 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 4, 1));
		 * li_6 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 5, 1));
		 * li_7 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 6, 1));
		 * li_8 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 7, 1));
		 * li_9 = Integer.parseInt(ss_mid(wp.itemStr("telephone_no"), 8, 1));
		 * li_0 = (li_1 * 8 + li_2 * 7 + li_3 * 6 + li_4 * 5 + li_5 * 4 + li_6 *
		 * 3 + li_7 * 2 + li_8 * 1); li_0 = (11 - li_0 % 11) % 10;
		 * 
		 * if (li_0 != li_9) {
		 * 
		 * // if (item_eq("conf_flag", "Y") == true) // return -1; //
		 * wp.colSet("conf_mesg", "警示 1~此資料檢查碼錯誤,是否強制放行"); // wp.respCode =
		 * "01"; // wp.colSet("conf_cond", " || 1==1 "); gs_warning_msg =
		 * "警示  1~此資料檢查碼錯誤!!";
		 * 
		 * } } else if (gs_office_m_code.equals("TE") ||
		 * gs_office_m_code.equals("TW") || gs_office_m_code.equals("CC")) { int
		 * li_count1 = 0;
		 * 
		 * switch (gs_office_m_code) { case "TE": // 台電 li_count1 =
		 * wf_chk_tel("TE", wp.itemStr("office_code"),
		 * wp.itemStr("telephone_no")); break; case "TW": // 市水 li_count1 =
		 * wf_chk_tel("TW", wp.itemStr("office_code"),
		 * wp.itemStr("telephone_no")); break; case "CC": // 省水 li_count1 =
		 * wf_chk_tel("CC", wp.itemStr("office_code"),
		 * wp.itemStr("telephone_no")); break; } //
		 * System.out.println("li_count1 :"+li_count1); if (li_count1 == 0) {
		 * gs_warning_msg = "警示  1~此資料檢查碼錯誤!!"; // if (item_eq("conf_flag", "Y")
		 * == true) // return -1; // wp.colSet("conf_mesg",
		 * "公用事業~費用代繳之用戶號碼檢核不符,是否強制輸入?"); // wp.respCode = "01"; //
		 * wp.colSet("conf_cond", " || 1==1 "); } } else if
		 * (gs_office_m_code.equals("CHTC") || gs_office_m_code.equals("CHTR"))
		 * { try { strAction = "A"; // insertFunc(); } catch (Exception e) {
		 * e.printStackTrace(); } }
		 */
//		if (gs_office_m_code.equals("CHTC") || gs_office_m_code.equals("CHTR")) {
//			try {
//				strAction = "A";
//				System.out.println("strAction========"+strAction);
//				// insertFunc();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

		if (empty(gs_card_no)) {
			if (wf_get_next_cardno(gs_id_no) != 1)
				return -1;
		}

		// 台電
		if (gs_office_m_code.equals("TE") & gs_card_no.length() == 15) {
			alertErr("台電資料, 不可使用 AE 卡 !!");
			return -1;
		}

		int li_count11 = 0;
		if (!empty(wp.itemStr("id_no"))) {
			String ls_cardno = "select count(*) as li_count "
					+ "from crd_card c "
					+ "left join crd_idno i on c.id_p_seqno = i.id_p_seqno "
					+ "where i.id_no =:id "
					+ "and i.id_no_code =:id_code "
					+ "and c.current_code = '0' "
					+ "and c.sup_flag = '0' ";
			setString("id", wp.itemStr("id_no"));
			setString("id_code", "0");
			sqlSelect(ls_cardno);
			li_count11 = (int) sqlNum("li_count");
			if (li_count11 == 0) {
				alertErr("此 ID 無有效卡 , 不可異動 !!");
				return -1;
			}
		}

		// wf_check_office_m
		String ls_sql2 = "select count(*) as li_count from bil_office_m where office_m_code =:ls_office_m_code  ";
		setString("ls_office_m_code", gs_office_m_code);
		sqlSelect(ls_sql2);
		if (sqlNum("li_count") < 1) {
			alertErr("機構代碼 不存在!");
			return -1;
		}

		// wf_check_office
		String ls_sql3 = "select count(*) as li_count from bil_office "
				+ "where office_m_code =:ls_office_m_code  and office_code=:ls_office_code";
		setString("ls_office_m_code", gs_office_m_code);
		setString("ls_office_code", gs_office_code);
		sqlSelect(ls_sql3);
		if (sqlNum("li_count") < 1) {
			alertErr("分支機構代碼 不存在!");
			return -1;
		}

		if (!empty(gs_card_no)) {
			
			int checard = 0;
			checard = wf_check_cardno(gs_card_no, gs_office_m_code);

			switch (checard) {
			case -1:
				alertErr("此為無效卡!");
				return -1;
			case 2:
				alertErr("此團代不符 (check office_time) !");
				return -1;
			case 3:
				alertErr("此為停用卡 !!");
				return -1;
			case 4:
				alertErr("此為分期戶 !!");
				return -1;
			case 5:
				alertErr("此為 催收/呆帳 !!");
				return -1;
			case 6:
				alertErr("此為附卡 !!");
				return -1;
			}
				
		}
		if (!empty(gs_warning_msg)) {
			alertMsg(gs_warning_msg);
		}
		return 1;
	}

	public int wf_get_next_cardno(String ls_id_no) throws Exception {

		String ls_sql = " select id_p_seqno from crd_idno where id_no=:id_no and id_no_code=:id_no_code  ";
		setString("id_no", ls_id_no);
		setString("id_no_code", "0");
		sqlSelect(ls_sql);
		if (sqlRowNum <= 0) {
			alertErr("輸入ID無有效卡 !!");
			return -1;
		}
		String id_ps = sqlStr("id_p_seqno");

		String ls_sql2 = " select c.card_no as ls_card_no  from crd_card c left join cca_card_acct a on c.acno_p_seqno = a.acno_p_seqno   ";
		ls_sql2 += "   where   c.id_p_seqno =:id_p_seqno ";
		ls_sql2 += "  and c.current_code = '0' ";
		ls_sql2 += "  and c.sup_flag     = '0' ";
		ls_sql2 += "  and c.acct_type    not in ('02','03') ";
		ls_sql2 += "  and ( a.block_status not in ('11','12') ";
		ls_sql2 += "        OR      decode(a.block_reason1,'61','','71','','72','','73','','74','','81','','91','',a.block_reason1) = '' ";
		ls_sql2 += "       and decode(a.block_reason2,'61','','71','','72','','73','','74','','81','','91','',a.block_reason2)='' ";
		ls_sql2 += "       and decode(a.block_reason3,'61','','71','','72','','73','','74','','81','','91','',a.block_reason3)='' ";
		ls_sql2 += "       and decode(a.block_reason4,'61','','71','','72','','73','','74','','81','','91','',a.block_reason4)='' ";
		ls_sql2 += "       and decode(a.block_reason5,'61','','71','','72','','73','','74','','81','','91','',a.block_reason5)='') ";
		setString("id_p_seqno", id_ps);
		sqlSelect(ls_sql2);
		if (sqlRowNum <= 0) {
			alertErr("無有效卡 !!");
			return -1;
		} else {
			gs_id_p_seqno = id_ps;
			gs_card_no = sqlStr("card_no");
			wp.colSet("id_p_seqno", ls_sql2);
			wp.colSet("card_no", sqlStr("card_no"));
		}
		return 1;
	}
}
