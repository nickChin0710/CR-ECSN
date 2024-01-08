/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/11/13  V1.00.00   Ryan       program initial                           *
*  108/12/19  V1.00.01   phopho     change table: prt_branch -> gen_brn       *
*  109-05-06  V1.00.02   Zhanghuheng     updated for project coding standard *
** 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/

package colp01;

import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import busi.SqlPrepare;

public class Colp1140 extends BaseProc {
	CommString commString = new CommString();
	Colp1150Func funcZ60;
	Colp1160Func funcRenew;
	Colp1170Func funcLiqu;
	Colp1180Func funcLiadModTmp;
	Colp1190Func funcLiad570;
	Colp1192Func funcLiad570Close;
	Colp1196Func funcRenewCourt;
	Colp1197Func funcLiquCourt;

	String dataKK1 = "";
	String kkString = "";
	String[] kkArray;
	String kkLiadDocNo = "", kkLiadDocSeqno = "";
	String kkIdNo = "", kkCaseLetter = "";
	String kkIdPSeqno = "";
	String kkType = "";
	String isRecvDate = "", isCaseDate = "";
	double imAllocAmt = 0;
	int rowcntaa = 0, rowcntbb = 0, rowcntcc = 0, rowcntdd = 0, rowcntee = 0, rowcntff = 0, rowcntgg = 0, rowcnthh = 0;
	int totalCnt = 0;

	@Override
	public void actionFunction(TarokoCommon wr) throws Exception {
		super.wp = wr;
		rc = 1;

		strAction = wp.buttonCode;
		if (eqIgno(wp.buttonCode, "X")) {
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "C")) {
			// -資料處理-
			dataProcess();
		} else if (eqIgno(wp.buttonCode, "Q")) {
			/* 查詢功能 */
			strAction = "Q";
			queryFunc();
		} else if (eqIgno(wp.buttonCode, "R")) {
			// -資料讀取-
			strAction = "R";
			dataRead();
			// } else if (eq_igno(wp.buttonCode, "A")) {
			// /* 新增功能 */
			// insertFunc();
			// } else if (eq_igno(wp.buttonCode, "U")) {
			// /* 更新功能 */
			// updateFunc();
			// } else if (eq_igno(wp.buttonCode, "D")) {
			// /* 刪除功能 */
			// deleteFunc();
		} else if (eqIgno(wp.buttonCode, "M")) {
			/* 瀏覽功能 :skip-page */
			queryRead();
		} else if (eqIgno(wp.buttonCode, "S")) {
			/* 動態查詢 */
//			querySelect();
			dataProcess();
		} else if (eqIgno(wp.buttonCode, "L")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		} else if (eqIgno(wp.buttonCode, "RV")) {
			/* 覆核 */
			saveFunc();
		} else if (eqIgno(wp.buttonCode, "NX")) {
			/* 下筆 */
			nextRow();
		}

		dddwSelect();
		initButton();
	}

	@Override
	public void initPage() {
	}

	@Override
	public void dddwSelect() {
		try {
			wp.initOption = "--";
			wp.optionKey = wp.itemStr("ex_crt_user");
			this.dddwList("dddw_apuser", "sec_user", "usr_id", "usr_id||' ['||usr_cname||']'",
					"where 1=1 and usr_type = '4' order by usr_id");
		} catch (Exception ex) {
		}
	}

	// for query use only
	void getWhereStrQueryZ60() throws Exception {
		wp.whereStr = " where 1=1 and a.liad_doc_no = b.data_key and b.data_type = 'Z60' ";
		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and b.crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and b.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and a.id_no = :ex_id ";
			setString("ex_id", wp.itemStr("ex_id"));
		}
		if (!empty(wp.itemStr("ex_recv_date"))) {
			wp.whereStr += " and a.recv_date >= :ex_recv_date ";
			setString("ex_recv_date", wp.itemStr("ex_recv_date"));
		}
	}

	void getWhereStrQueryRenew() throws Exception {
		wp.whereStr = " where 1=1 and a.liad_doc_no = b.data_key and b.data_type = 'RENEW' ";
		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and b.crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and b.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and a.id_no = :ex_id ";
			setString("ex_id", wp.itemStr("ex_id"));
		}
		if (!empty(wp.itemStr("ex_recv_date"))) {
			wp.whereStr += " and a.recv_date >= :ex_recv_date ";
			setString("ex_recv_date", wp.itemStr("ex_recv_date"));
		}
	}

	void getWhereStrQueryLiqu() throws Exception {
		wp.whereStr = " where 1=1 and a.liad_doc_no = b.data_key and b.data_type = 'LIQUIDATE' ";
		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and b.crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and b.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and a.id_no = :ex_id ";
			setString("ex_id", wp.itemStr("ex_id"));
		}
		if (!empty(wp.itemStr("ex_recv_date"))) {
			wp.whereStr += " and a.recv_date >= :ex_recv_date ";
			setString("ex_recv_date", wp.itemStr("ex_recv_date"));
		}
	}

	void getWhereStrQueryLiadModTmp() throws Exception {
		wp.whereStr = " where 1=1 and data_type = 'INST-MAST' ";
		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and data_key like :ex_id ";
			setString("ex_id", wp.itemStr("ex_id") + "%");
		}
	}

	void getWhereStrQueryLiad570() throws Exception {
		wp.whereStr = " where 1=1 and a.id_no = rtrim (substrb (b.data_key, 1, 10)) "
				+ " AND a.case_letter = rtrim (substrb (b.data_key, 11, 10)) " + " AND b.data_type = 'COLL-DATA' ";

		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and b.crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and b.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and a.id_no = :ex_id ";
			setString("ex_id", wp.itemStr("ex_id"));
		}
		if (!empty(wp.itemStr("ex_recv_date"))) {
			wp.whereStr += " and a.recv_date >= :ex_recv_date ";
			setString("ex_recv_date", wp.itemStr("ex_recv_date"));
		}
	}

	void getWhereStrQueryLiad570Close() throws Exception {
		wp.whereStr = " where 1=1 and a.id_no = rtrim (substrb (b.data_key, 1, 10)) "
				+ " AND a.case_letter = rtrim (substrb (b.data_key, 11, 10)) " + " AND b.data_type = 'COLL-CLOSE' ";

		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and b.crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and b.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and a.id_no = :ex_id ";
			setString("ex_id", wp.itemStr("ex_id"));
		}
		if (!empty(wp.itemStr("ex_recv_date"))) {
			wp.whereStr += " and a.recv_date >= :ex_recv_date ";
			setString("ex_recv_date", wp.itemStr("ex_recv_date"));
		}
	}

	void getWhereStrQueryRenewCourt() throws Exception {
		wp.whereStr = " where 1=1 and a.liad_doc_no||a.liad_doc_seqno = b.data_key "
				+ " and b.data_type = 'RENEWCOURT' " + " and a.id_p_seqno = c.id_p_seqno ";

		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and b.crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and b.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and c.id_no = :ex_id ";
			setString("ex_id", wp.itemStr("ex_id"));
		}
		if (!empty(wp.itemStr("ex_recv_date"))) {
			wp.whereStr += " and a.recv_date >= :ex_recv_date ";
			setString("ex_recv_date", wp.itemStr("ex_recv_date"));
		}
	}

	void getWhereStrQueryLiquCourt() throws Exception {
		wp.whereStr = " where 1=1 and a.liad_doc_no||a.liad_doc_seqno = b.data_key "
				+ " and b.data_type = 'LIQUICOURT' " + " and a.id_p_seqno = c.id_p_seqno ";

		if (!empty(wp.itemStr("ex_crt_date"))) {
			wp.whereStr += " and b.crt_date >= :ex_crt_date ";
			setString("ex_crt_date", wp.itemStr("ex_crt_date"));
		}
		if (!empty(wp.itemStr("ex_crt_user"))) {
			wp.whereStr += " and b.crt_user = :ex_crt_user ";
			setString("ex_crt_user", wp.itemStr("ex_crt_user"));
		}
		if (!empty(wp.itemStr("ex_id"))) {
			wp.whereStr += " and c.id_no = :ex_id ";
			setString("ex_id", wp.itemStr("ex_id"));
		}
		if (!empty(wp.itemStr("ex_recv_date"))) {
			wp.whereStr += " and a.recv_date >= :ex_recv_date ";
			setString("ex_recv_date", wp.itemStr("ex_recv_date"));
		}
	}

	@Override
	public void queryFunc() throws Exception {
		// wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		queryZ60();
		queryRenew();
		queryLiqu();
		queryLiadModTmp();
		queryLiad570();
		queryLiad570Close();
		queryRenewCourt();
		queryLiquCourt();
		dddwSelect();
	}

	void queryZ60() throws Exception {
		daoTid = "A-";

		wp.selectSQL = " hex(a.rowid) as rowid,a.mod_seqno" + " ,b.aud_code" + " ,a.liad_doc_no" + " ,a.id_no"
				+ " ,a.id_p_seqno" + " ,a.chi_name" + " ,a.recv_date" + " ,a.law_user_id" + " ,a.demand_user_id"
				+ " ,a.usr_remark" + " ,b.crt_user" + " ,b.crt_date";
		wp.daoTable = "col_liad_z60 as a,col_liad_mod_tmp as b ";
		wp.whereOrder = "order by a.liad_doc_no";
		getWhereStrQueryZ60();
		pageQuery();
		wp.setListCount(1);
		totalCnt = wp.selectCnt;

//		if (sql_notFind()) {
//		}

		daoTid = "a-";
		listWkdataQueryZ60();
	}

	void queryRenew() throws Exception {
		daoTid = "B-";

		wp.selectSQL = " hex(a.rowid) as rowid,a.mod_seqno" + " ,b.aud_code" + " ,a.liad_doc_no" + " ,a.id_no"
				+ " ,a.id_p_seqno" + " ,a.chi_name" + " ,a.recv_date" + " ,a.law_user_id" + " ,a.renew_status"
				+ " ,a.finish_date" + " ,a.notify_date";
		wp.daoTable = "col_liad_renew as a,col_liad_mod_tmp as b ";
		wp.whereOrder = "";
		getWhereStrQueryRenew();
		pageQuery();
		wp.setListCount(2);
		totalCnt += wp.selectCnt;

//		if (sql_notFind()) {
//		}

		daoTid = "b-";
		listWkdataQueryRenew();
	}

	void queryLiqu() throws Exception {
		daoTid = "C-";

		wp.selectSQL = " hex(a.rowid) as rowid,a.mod_seqno" + " ,b.aud_code" + " ,a.liad_doc_no" + " ,a.id_no"
				+ " ,a.id_p_seqno" + " ,a.chi_name" + " ,a.recv_date" + " ,a.law_user_id" + " ,a.liqu_status"
				+ " ,a.finish_date" + " ,a.notify_date";
		wp.daoTable = "col_liad_liquidate as a,col_liad_mod_tmp as b ";
		wp.whereOrder = "";
		getWhereStrQueryLiqu();
		pageQuery();
		wp.setListCount(3);
		totalCnt += wp.selectCnt;

//		if (sql_notFind()) {
//		}

		daoTid = "c-";
		listWkdataQueryLiqu();
	}

	void queryLiadModTmp() throws Exception {
		daoTid = "D-";

		wp.selectSQL = " hex(rowid) as rowid" + " ,aud_code" + " ,substrb (data_key, 1, 10) db_id"
				+ " ,substrb (data_key, 11, 10) db_case_letter" + " ,mod_data" + " ,lpad (' ', 20, ' ') db_chi_name"
				+ " ,lpad (' ', 8, ' ') db_recv_date" + " ,lpad (' ', 2, ' ') db_renew_status" + " ,0 db_org_debt_amt"
				+ " ,crt_user" + " ,crt_date";
		wp.daoTable = "col_liad_mod_tmp  ";
		wp.whereOrder = "";
		getWhereStrQueryLiadModTmp();
		pageQuery();
		wp.setListCount(4);
		totalCnt += wp.selectCnt;

//		if (sql_notFind()) {
//		}

		daoTid = "d-";
		listWkdataQueryLiadModTmp();
	}

	void queryLiad570() throws Exception {
		daoTid = "E-";

		wp.selectSQL = " hex(a.rowid) as rowid, a.mod_seqno" + " ,a.id_no" + " ,a.idno_name" + " ,a.case_letter"
				+ " ,a.recv_date" + " ,a.apply_bank_no" + " ,a.bank_name" + " ,a.coll_apply_date" + " ,a.last_pay_date"
				+ " ,a.last_pay_amt" + " ,a.pay_normal_flag_2" + " ,a.debt_amt2" + " ,a.alloc_debt_amt"
				+ " ,a.unalloc_debt_amt" + " ,a.coll_remark" + " ,a.send_flag_571" + " ,b.mod_data";
		wp.daoTable = "col_liad_570 as a,col_liad_mod_tmp as b ";
		wp.whereOrder = "";
		getWhereStrQueryLiad570();
		pageQuery();
		wp.setListCount(5);
		totalCnt += wp.selectCnt;

//		if (sql_notFind()) {
//		}

		daoTid = "e-";
	}

	void queryLiad570Close() throws Exception {
		daoTid = "F-";

		wp.selectSQL = " hex(a.rowid) as rowid, a.mod_seqno" + " ,a.id_no" + " ,a.case_letter" + " ,a.recv_date"
				+ " ,a.apply_bank_no" + " ,a.bank_name" + " ,a.coll_apply_date" + " ,a.close_reason"
				+ " ,a.close_remark" + " ,a.last_pay_date" + " ,a.last_pay_amt" + " ,a.pay_normal_flag_2"
				+ " ,a.debt_amt2" + " ,a.alloc_debt_amt" + " ,a.unalloc_debt_amt" + " ,a.idno_name as db_idno_name"
				+ " ,b.mod_data";
		wp.daoTable = "col_liad_570 as a,col_liad_mod_tmp as b ";
		wp.whereOrder = "";
		getWhereStrQueryLiad570Close();
		pageQuery();
		wp.setListCount(6);
		totalCnt += wp.selectCnt;

//		if (sql_notFind()) {
//		}

		daoTid = "f-";
		listWkdataQueryLiad570Close();
	}

	void queryRenewCourt() throws Exception {
		daoTid = "G-";

		wp.selectSQL = " hex(a.rowid) as rowid, a.mod_seqno" + " ,b.aud_code" + " ,a.id_p_seqno" + " ,c.id_no db_id_no"
				+ " ,a.chi_name" + " ,a.liad_doc_no" + " ,a.liad_doc_seqno" + " ,a.unit_doc_no" + " ,a.recv_date"
				+ " ,a.key_note" + " ,a.case_letter_desc" + " ,a.case_date";

		wp.daoTable = "col_liad_renew_court a, col_liad_mod_tmp b, crd_idno c ";
		wp.whereOrder = "order by a.liad_doc_no, a.liad_doc_seqno";

		getWhereStrQueryRenewCourt();
		pageQuery();
		wp.setListCount(7);
		totalCnt += wp.selectCnt;

//		if (sql_notFind()) {
//		}

		daoTid = "g-";
	}

	void queryLiquCourt() throws Exception {
		daoTid = "H-";

		wp.selectSQL = " hex(a.rowid) as rowid, a.mod_seqno" + " ,b.aud_code" + " ,a.id_p_seqno" + " ,c.id_no db_id_no"
				+ " ,a.chi_name" + " ,a.liad_doc_no" + " ,a.liad_doc_seqno" + " ,a.unit_doc_no" + " ,a.recv_date"
				+ " ,a.key_note" + " ,a.case_letter_desc" + " ,a.case_date";

		wp.daoTable = "col_liad_liquidate_court a, col_liad_mod_tmp b, crd_idno c ";
		wp.whereOrder = "order by a.liad_doc_no, a.liad_doc_seqno";

		getWhereStrQueryLiquCourt();
		pageQuery();
		wp.setListCount(8);
		totalCnt += wp.selectCnt;

		wp.notFound = "N";
		if (totalCnt == 0) {
			wp.notFound = "Y";
			alertErr(appMsg.errCondNodata);
			return;
		}

		daoTid = "h-";
	}

	void listWkdataQueryZ60() throws Exception {
		String wkData = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, daoTid + "aud_code");
			wp.colSet(ii, daoTid + "aud_code", commString.decode(wkData, ",A,U,D,", ",新增,修改,刪除"));

			wkData = wp.colStr(ii, daoTid + "law_user_id");
			String sql1 = "select USR_ID||'['||USR_CNAME||']' as law_user_id from SEC_USER where USR_ID = '" + wkData + "'";
			sqlSelect(sql1);
			daoTid = "a-";
			String lawUserId = sqlStr(daoTid + "law_user_id");
			if (sqlRowNum <= 0) {
				lawUserId = wp.colStr(ii, daoTid + "law_user_id");
			}
			wp.colSet(ii, daoTid + "law_user_id", lawUserId);

			wkData = wp.colStr(ii, daoTid + "demand_user_id");
			String sql2 = "select USR_ID||'['||USR_CNAME||']' as demand_user_id from SEC_USER where USR_ID = '" + wkData
					+ "'";
			sqlSelect(sql2);
			daoTid = "a-";
			String demandUserId = sqlStr(daoTid + "demand_user_id");
			if (sqlRowNum <= 0) {
				demandUserId = wp.colStr(ii, daoTid + "demand_user_id");
			}
			wp.colSet(ii, daoTid + "demand_user_id", demandUserId);
		}
	}

	void listWkdataQueryRenew() throws Exception {
		String wkData = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, daoTid + "aud_code");
			wp.colSet(ii, daoTid + "aud_code", commString.decode(wkData, ",A,U,D,", ",新增,修改,刪除"));

			wkData = wp.colStr(ii, daoTid + "law_user_id");
			String sql1 = "select USR_ID||'['||USR_CNAME||']' as law_user_id from SEC_USER where USR_ID = '" + wkData + "'";
			sqlSelect(sql1);
			daoTid = "b-";
			String lawUserId = sqlStr(daoTid + "law_user_id");
			if (sqlRowNum <= 0) {
				lawUserId = wp.colStr(ii, daoTid + "law_user_id");
			}
			wp.colSet(ii, daoTid + "law_user_id", lawUserId);
			wkData = wp.colStr(ii, daoTid + "renew_status");
			String sql2 = "select ID_CODE||'['||ID_DESC||']' as renew_status from COL_LIAB_IDTAB where id_key = '3' and ID_CODE = '"
					+ wkData + "'";
			sqlSelect(sql2);
			daoTid = "b-";
			String renewStatus = sqlStr(daoTid + "renew_status");
			if (sqlRowNum <= 0) {
				renewStatus = wp.colStr(ii, daoTid + "renew_status");
			}
			wp.colSet(ii, daoTid + "renew_status", renewStatus);
		}
	}

	void listWkdataQueryLiqu() throws Exception {
		String wkData = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, daoTid + "aud_code");
			wp.colSet(ii, daoTid + "aud_code", commString.decode(wkData, ",A,U,D,", ",新增,修改,刪除"));

			wkData = wp.colStr(ii, daoTid + "law_user_id");
			String sql1 = "select USR_ID||'['||USR_CNAME||']' as law_user_id from SEC_USER where USR_ID = '" + wkData + "'";
			sqlSelect(sql1);
			daoTid = "c-";
			String lawUserId = sqlStr(daoTid + "law_user_id");
			if (sqlRowNum <= 0) {
				lawUserId = wp.colStr(ii, daoTid + "law_user_id");
			}
			wp.colSet(ii, daoTid + "law_user_id", lawUserId);

			wkData = wp.colStr(ii, daoTid + "liqu_status");
			String sql2 = "select ID_CODE||'['||ID_DESC||']' as liqu_status from COL_LIAB_IDTAB where id_key = '4' and ID_CODE = '"
					+ wkData + "'";
			sqlSelect(sql2);
			daoTid = "c-";
			String liquStatus = sqlStr(daoTid + "liqu_status");
			if (sqlRowNum <= 0) {
				liquStatus = wp.colStr(ii, daoTid + "liqu_status");
			}
			wp.colSet(ii, daoTid + "liqu_status", liquStatus);
		}
	}

	void listWkdataQueryLiadModTmp() throws Exception {
		String wkData = "";
		String[] array = new String[5];
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			wkData = wp.colStr(ii, daoTid + "aud_code");
			wp.colSet(ii, daoTid + "aud_code", commString.decode(wkData, ",A,U,D,", ",新增,修改,刪除"));

			wkData = wp.colStr(ii, daoTid + "mod_data");
			try {
				array = wkData.split("\";\"|\";\"|\";\"|\";\"|\";\"", 5);
			} catch (Exception ex) {
				array[0] = "";
				array[1] = "";
				array[2] = "";
				array[3] = "";
			}
			wp.colSet(ii, daoTid + "db_chi_name", array[0]);
			wp.colSet(ii, daoTid + "db_recv_date", array[1]);
			wp.colSet(ii, daoTid + "db_renew_status", array[2]);
			wp.colSet(ii, daoTid + "db_org_debt_amt", array[3]);
			String sql1 = "select ID_CODE||'['||ID_DESC||']' as db_renew_status from COL_LIAB_IDTAB where id_key = '3' and ID_CODE = '"
					+ array[2] + "'";
			sqlSelect(sql1);
			daoTid = "d-";
			String dbRenewStatus = sqlStr(daoTid + "db_renew_status");
			if (sqlRowNum <= 0) {
				dbRenewStatus = array[2];
			}
			wp.colSet(ii, daoTid + "db_renew_status", dbRenewStatus);
		}
	}

	void listWkdataQueryLiad570Close() throws Exception {
		String closeReason1 = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			closeReason1 = wp.colStr(ii, daoTid + "close_reason");
			String sql1 = "select WF_ID||'['||WF_DESC||']' as close_reason from PTR_SYS_IDTAB where wf_type = 'LIAD_CLOSE_REASON' and WF_ID = '"
					+ closeReason1 + "'";
			sqlSelect(sql1);
			daoTid = "f-";
			String closeReason = sqlStr(daoTid + "close_reason");
			if (sqlRowNum <= 0) {
				closeReason = wp.colStr(ii, daoTid + "close_reason");
			}
			wp.colSet(ii, daoTid + "close_reason", closeReason);
		}
	}

	@Override
	public void querySelect() throws Exception {
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {

	}

	public void nextRow() throws Exception {
		dataKK1 = wp.itemStr("data_k1");
		kkString = wp.itemStr("kk_string");
		switch (dataKK1) {
		case "aopt":
			dataReadZ60();
			break;
		case "bopt":
			dataReadRenew();
			break;
		case "copt":
			dataReadLiqu();
			break;
		case "dopt":
			dataReadLiadModTmp();
			break;
		case "eopt":
			dataReadLiad570();
			break;
		case "fopt":
			dataReadLiad570Close();
			break;
		case "gopt":
			dataReadRenewCourt();
			break;
		case "hopt":
			dataReadLiquCourt();
			break;
		}
	}

	public void dataReadZ60() throws Exception {
		kkArray = kkString.split(";", 2);
		kkLiadDocNo = kkArray[0];
		kkString = (kkArray.length > 1) ? kkArray[1] : "";
//		System.out.println("-AAA DOCNO->" + kk_liad_doc_no + " : " + kkString);
//		kk_liad_doc_no = "AABBCCuudd";
		if (!empty(kkLiadDocNo)) {
//			daoTid ="";
			wp.selectSQL = " hex(rowid) as rowid " + " ,liad_doc_no " + " ,id_no " + " ,id_p_seqno " + " ,chi_name "
					+ " ,recv_date " + " ,acct_status " + " ,m_code " + " ,bad_debt_amt " + " ,demand_amt "
					+ " ,debt_amt " + " ,card_num " + " ,credit_branch " + " ,law_user_id " + " ,demand_user_id "
					+ " ,branch_comb_flag " + " ,court_id " + " ,court_name " + " ,case_year " + " ,case_letter "
					+ " ,case_no " + " ,bullet_date " + " ,bullet_desc " + " ,data_date " + " ,usr_remark "
					+ " ,crt_user " + " ,crt_date " + " ,apr_flag " + " ,apr_date " + " ,apr_user " + " ,mod_user "
					+ " ,mod_time " + " ,mod_pgm " + " ,mod_seqno " + " ,renew_flag " + " ,liqui_flag "
					+ " ,manul_proc_flag ";

			wp.daoTable = " col_liad_z60 ";
			wp.whereOrder = " ";
			wp.whereStr = " where liad_doc_no = :liad_doc_no ";
			setString("liad_doc_no", kkLiadDocNo);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, 文件編號:" + kkLiadDocNo);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
//			wp.notFound = "";
			goToZ60Update();
			detlWkdataZ60();
			setbtnNext();
		}
	}

	public void dataReadRenew() throws Exception {
		kkArray = kkString.split(";", 2);
		kkLiadDocNo = kkArray[0];
		kkString = (kkArray.length > 1) ? kkArray[1] : "";

		if (!empty(kkLiadDocNo)) {
			wp.selectSQL = " hex(rowid) as rowid " + " ,liad_doc_no " + " ,id_no " + " ,id_p_seqno " + " ,chi_name "
					+ " ,recv_date " + " ,acct_status " + " ,m_code " + " ,bad_debt_amt " + " ,demand_amt "
					+ " ,debt_amt " + " ,acct_num " + " ,card_num " + " ,renew_status " + " ,branch_comb_flag "
					+ " ,credit_branch " + " ,decode(max_bank_flag,'','N',max_bank_flag) as max_bank_flag "
					+ " ,org_debt_amt " + " ,org_debt_amt_bef " + " ,org_debt_amt_bef_base " + " ,renew_lose_amt "
					+ " ,court_id " + " ,court_name " + " ,doc_chi_name " + " ,court_dept " + " ,payoff_amt "
					+ " ,payment_day " + " ,court_status " + " ,case_year " + " ,case_letter " + " ,case_letter_desc "
					+ " ,judic_date " + " ,judic_action_flag " + " ,action_date_s " + " ,judic_cancel_flag "
					+ " ,cancel_date " + " ,renew_cancel_date " + " ,deliver_date " + " ,renew_first_date "
					+ " ,renew_last_date " + " ,renew_int " + " ,renew_rate " + " ,confirm_date " + " ,run_renew_flag "
					+ " ,renew_damage_date " + " ,renew_accetp_no " + " ,crt_user " + " ,crt_date " + " ,apr_flag "
					+ " ,apr_date " + " ,apr_user " + " ,mod_user " + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno "
					+ " ,case_date as case_date_m " + " ,super_name ";

			wp.daoTable = " col_liad_renew ";
			wp.whereOrder = " ";
			wp.whereStr = " where liad_doc_no = :liad_doc_no ";
			setString("liad_doc_no", kkLiadDocNo);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, 文件編號:" + kkLiadDocNo);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
			goToRenewUpdate();
			detlWkdataRenew();
			setbtnNext();
		}
	}

	public void dataReadLiqu() throws Exception {
		kkArray = kkString.split(";", 2);
		kkLiadDocNo = kkArray[0];
		kkString = (kkArray.length > 1) ? kkArray[1] : "";

		if (!empty(kkLiadDocNo)) {
			wp.selectSQL = " hex(rowid) as rowid " + " ,liad_doc_no " + " ,id_no " + " ,id_p_seqno " + " ,chi_name "
					+ " ,recv_date " + " ,acct_status " + " ,m_code " + " ,bad_debt_amt " + " ,demand_amt "
					+ " ,debt_amt " + " ,acct_num " + " ,card_num " + " ,liqu_status "
					+ " ,decode(branch_comb_flag,'','N',branch_comb_flag) as branch_comb_flag " + " ,credit_branch "
					+ " ,decode(max_bank_flag,'','N',max_bank_flag) as max_bank_flag " + " ,org_debt_amt "
					+ " ,org_debt_amt_bef " + " ,org_debt_amt_bef_base " + " ,liqu_lose_amt " + " ,court_id "
					+ " ,court_name " + " ,doc_chi_name " + " ,court_dept " + " ,court_status " + " ,case_year "
					+ " ,case_letter " + " ,case_letter_desc "
					+ " ,decode(judic_avoid_flag,'','N',judic_avoid_flag) as judic_avoid_flag "
					+ " ,decode(judic_avoid_sure_flag,'','N',judic_avoid_sure_flag) as judic_avoid_sure_flag "
					+ " ,judic_avoid_no " + " ,judic_date "
					+ " ,decode(judic_action_flag,'','N',judic_action_flag) as judic_action_flag " + " ,action_date_s "
					+ " ,decode(judic_cancel_flag,'','N',judic_cancel_flag) as judic_cancel_flag " + " ,cancel_date "
					+ " ,credit_print_flag " + " ,credit_print_date " + " ,credit_print_user " + " ,jcic_send_flag "
					+ " ,jcic_send_date " + " ,jcic_send_user " + " ,crt_user " + " ,crt_date " + " ,apr_flag "
					+ " ,apr_date " + " ,apr_user " + " ,mod_user " + " ,mod_time " + " ,mod_pgm " + " ,mod_seqno "
					+ " ,decode(law_133_flag,'','N',law_133_flag) as law_133_flag " + " ,case_date as case_date_m ";

			wp.daoTable = " col_liad_liquidate ";
			wp.whereOrder = " ";
			wp.whereStr = " where liad_doc_no = :liad_doc_no ";
			setString("liad_doc_no", kkLiadDocNo);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, 文件編號:" + kkLiadDocNo);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
			goToLiquUpdate();
			detlWkdataLiqu();
			setbtnNext();
		}
	}

	public void dataReadLiadModTmp() throws Exception {
		kkArray = kkString.split(";", 3);
		kkIdNo = kkArray[0];
		kkCaseLetter = kkArray[1];
		kkString = (kkArray.length > 2) ? kkArray[2] : "";

		if (!empty(kkIdNo) && !empty(kkCaseLetter)) {
			wp.selectSQL = " hex(rowid) as rowid " + " ,data_type " + " ,data_key " + " ,aud_code " + " ,mod_data "
					+ " ,crt_user " + " ,substrb(data_key, 1, 10) id_no" + " ,substrb(data_key, 11, 10) case_letter";

			wp.daoTable = " col_liad_mod_tmp";
			wp.whereOrder = " ";
			wp.whereStr = " where data_type = 'INST-MAST' "
					+ "and data_key = rpad(:id_no, 10, ' ')||rpad(:case_letter, 10, ' ') ";
			setString("id_no", kkIdNo);
			setString("case_letter", kkCaseLetter);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, ID:" + kkIdNo + "; 文號= " + kkCaseLetter);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
			gotoLiadModTmpUpdate();
			setbtnNext();
			dataReadLiadModTmpDetl();
		}
	}

	void dataReadLiadModTmpDetl() throws Exception {
		this.selectNoLimit();
		wp.selectSQL = " hex(rowid) as b_rowid " + " ,mod_data as b_mod_data ";

		wp.daoTable = " col_liad_mod_tmp ";
		wp.whereOrder = "order by data_key ";
		wp.whereStr = " where data_type = 'INST-DETL' "
				+ "and data_key like rpad(:id_no, 10, ' ')||rpad(:case_letter, 10, ' ')||'%' ";
		setString("id_no", kkIdNo);
		setString("case_letter", kkCaseLetter);

		pageQuery();
		wp.setListCount(1);
		wp.notFound = "";
		detlWkdataLiadModTmp();
	}

	public void dataReadLiad570() throws Exception {
		kkArray = kkString.split(";", 3);
		kkIdNo = kkArray[0];
		kkCaseLetter = kkArray[1];
		kkString = (kkArray.length > 2) ? kkArray[2] : "";
		if (!empty(kkIdNo) && !empty(kkCaseLetter)) {
			wp.selectSQL = " hex(rowid) as rowid " + " ,id_no " + " ,id_p_seqno " + " ,idno_name " + " ,case_letter "
					+ " ,recv_date " + " ,apply_bank_no " + " ,bank_name " + " ,coll_apply_date " + " ,judic_date "
					+ " ,renew_status " + " ,last_pay_date " + " ,last_pay_amt " + " ,pay_normal_flag_1 "
					+ " ,pay_normal_flag_2 " + " ,debt_amt1 " + " ,debt_amt2 " + " ,alloc_debt_amt "
					+ " ,unalloc_debt_amt " + " ,coll_remark " + " ,send_flag_571 " + " ,proc_date_571 " + " ,crt_user "
					+ " ,crt_date " + " ,apr_date " + " ,apr_user " + " ,close_reason " + " ,close_remark "
					+ " ,close_date " + " ,close_user " + " ,close_apr_date " + " ,close_apr_user "
					+ " ,close_proc_date " + " ,send_cnt " + " ,close_send_cnt " + " ,mod_user " + " ,mod_time "
					+ " ,mod_pgm " + " ,mod_seqno ";

			wp.daoTable = " col_liad_570";
			wp.whereOrder = " ";
			wp.whereStr = " where id_no = :id_no and case_letter = :case_letter ";
			setString("id_no", kkIdNo);
			setString("case_letter", kkCaseLetter);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, ID:" + kkIdNo + "; 文號= " + kkCaseLetter);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
			detlWkdataLiad570();
			gotoColldataUpdate();
			setbtnNext();
		}
	}

	public void dataReadLiad570Close() throws Exception {
		kkArray = kkString.split(";", 3);
		kkIdNo = kkArray[0];
		kkCaseLetter = kkArray[1];
		kkString = (kkArray.length > 2) ? kkArray[2] : "";
		if (!empty(kkIdNo) && !empty(kkCaseLetter)) {
			wp.selectSQL = " hex(rowid) as rowid " + " ,id_no " + " ,id_p_seqno " + " ,idno_name " + " ,case_letter "
					+ " ,recv_date " + " ,apply_bank_no " + " ,bank_name " + " ,coll_apply_date " + " ,judic_date "
					+ " ,renew_status " + " ,last_pay_date " + " ,last_pay_amt " + " ,pay_normal_flag_1 "
					+ " ,pay_normal_flag_2 " + " ,debt_amt1 " + " ,debt_amt2 " + " ,alloc_debt_amt "
					+ " ,unalloc_debt_amt " + " ,coll_remark " + " ,send_flag_571 " + " ,proc_date_571 " + " ,crt_user "
					+ " ,crt_date " + " ,apr_date " + " ,apr_user " + " ,close_reason " + " ,close_remark "
					+ " ,close_date " + " ,close_user " + " ,close_apr_date " + " ,close_apr_user "
					+ " ,close_proc_date " + " ,send_cnt " + " ,mod_user " + " ,mod_time " + " ,mod_pgm "
					+ " ,mod_seqno ";

			wp.daoTable = " col_liad_570";
			wp.whereOrder = " ";
			wp.whereStr = " where id_no = :id_no and case_letter = :case_letter ";
			setString("id_no", kkIdNo);
			setString("case_letter", kkCaseLetter);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, ID:" + kkIdNo + "; 文號= " + kkCaseLetter);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
			detlWkdataLiad570();
			gotoCollCloseApr();
			setbtnNext();
		}
	}

	public void dataReadRenewCourt() throws Exception {
//		kkArray = kkString.split(";",2);
//		kk_liad_doc_no = kkArray[0];
//		kkString = (kkArray.length>1)? kkArray[1]:"";
//
//		if(!empty(kk_liad_doc_no)){
		kkArray = kkString.split(";", 3);
		kkLiadDocNo = kkArray[0];
		kkLiadDocSeqno = kkArray[1];
		kkString = (kkArray.length > 2) ? kkArray[2] : "";

		if (!empty(kkLiadDocNo) && !empty(kkLiadDocSeqno)) {
			wp.selectSQL = " hex(a.rowid) as rowid " + " ,a.mod_seqno " + " ,a.id_p_seqno " + " ,c.id_no db_id_no "
					+ " ,a.chi_name " + " ,a.liad_doc_no " + " ,a.liad_doc_seqno " + " ,a.unit_doc_no "
					+ " ,a.recv_date " + " ,a.key_note " + " ,a.case_letter_desc " + " ,a.case_date ";

			wp.daoTable = " col_liad_renew_court a ";
			wp.daoTable += " left join crd_idno c on a.id_p_seqno = c.id_p_seqno ";
			wp.whereOrder = " ";
			wp.whereStr = " where a.liad_doc_no = :liad_doc_no ";
			wp.whereStr += " and a.liad_doc_seqno = :liad_doc_seqno ";
			setString("liad_doc_no", kkLiadDocNo);
			setString("liad_doc_seqno", kkLiadDocSeqno);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, 文件編號:" + kkLiadDocNo + "; 序號:" + kkLiadDocSeqno);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
			goToRenewCourtUpdate();
			setbtnNext();
		}
	}

	public void dataReadLiquCourt() throws Exception {
		kkArray = kkString.split(";", 3);
		kkLiadDocNo = kkArray[0];
		kkLiadDocSeqno = kkArray[1];
		kkString = (kkArray.length > 2) ? kkArray[2] : "";

		if (!empty(kkLiadDocNo) && !empty(kkLiadDocSeqno)) {
			wp.selectSQL = " hex(a.rowid) as rowid " + " ,a.mod_seqno " + " ,a.id_p_seqno " + " ,c.id_no db_id_no "
					+ " ,a.chi_name " + " ,a.liad_doc_no " + " ,a.liad_doc_seqno " + " ,a.unit_doc_no "
					+ " ,a.recv_date " + " ,a.key_note " + " ,a.case_letter_desc " + " ,a.case_date ";

			wp.daoTable = " col_liad_liquidate_court a ";
			wp.daoTable += " left join crd_idno c on a.id_p_seqno = c.id_p_seqno ";
			wp.whereOrder = " ";
			wp.whereStr = " where a.liad_doc_no = :liad_doc_no ";
			wp.whereStr += " and a.liad_doc_seqno = :liad_doc_seqno ";
			setString("liad_doc_no", kkLiadDocNo);
			setString("liad_doc_seqno", kkLiadDocSeqno);
			pageSelect();
			if (sqlNotFind()) {
				alertErr("資料已不存在, 文件編號:" + kkLiadDocNo + "; 序號:" + kkLiadDocSeqno);
				wp.colSet("review_disabled", "disabled");
				wp.colSet("next_disabled", "disabled");
				return;
			}
			goToLiquCourtUpdate();
			setbtnNext();
		}
	}

	void detlWkdataZ60() throws Exception {
		String wkData = "";
		wkData = wp.colStr("credit_branch");
		wp.colSet("tt_credit_branch", wfPtrBranchName(wkData));

		wkData = wp.colStr("law_user_id");
		wp.colSet("tt_law_user_id", wfSecUserIDName(wkData));

		wkData = wp.colStr("demand_user_id");
		wp.colSet("tt_demand_user_id", wfSecUserIDName(wkData));
	}

	void detlWkdataRenew() throws Exception {
		String wkData = "";
		wkData = wp.colStr("credit_branch");
		wp.colSet("tt_credit_branch", wfPtrBranchName(wkData));

		wkData = wp.colStr("court_id");
		wp.colSet("tt_court_id", wfPtrCourtName(wkData));

		wkData = wp.colStr("renew_status");
		wp.colSet("tt_renew_status", wfColRenewStatus("3", wkData));

		wkData = wp.colStr("court_status");
		wp.colSet("tt_court_status", wfPtrCourtStatus(wkData));
	}

	void detlWkdataLiqu() throws Exception {
		String wkData = "";
		wkData = wp.colStr("credit_branch");
		wp.colSet("tt_credit_branch", wfPtrBranchName(wkData));

		wkData = wp.colStr("court_id");
		wp.colSet("tt_court_id", wfPtrCourtName(wkData));

		wkData = wp.colStr("liqu_status");
		wp.colSet("tt_liqu_status", wfColRenewStatus("4", wkData));

		wkData = wp.colStr("court_status");
		wp.colSet("tt_court_status", wfPtrCourtStatus(wkData));
	}

	void detlWkdataLiadModTmp() throws Exception {
		String strModData = "";
		for (int ii = 0; ii < wp.selectCnt; ii++) {
			strModData = wp.colStr(ii, "b_mod_data");
			String[] modData = strModData.split("\";\"", -1);
			if (modData.length > 0) {
				wp.colSet(ii, "inst_seq", modData[0]);
				wp.colSet(ii, "inst_date_s", modData[1]);
				wp.colSet(ii, "inst_date_e", modData[2]);
				wp.colSet(ii, "ar_per_amt", modData[3]);
				wp.colSet(ii, "act_per_amt", modData[4].replaceFirst("^0*", ""));
				wp.colSet(ii, "pay_date", modData[5]);
				wp.colSet(ii, "ar_tot_amt", modData[6]);
				wp.colSet(ii, "act_tot_amt", modData[7]);
				wp.colSet(ii, "unpay_amt", modData[8]);
				wp.colSet(ii, "payment_day", modData[9]);
				wp.colSet(ii, "from_type", modData[10]);
				wp.colSet(ii, "tt_from_type", commString.decode(modData[10].trim(), ",Y,F", ",Y.人工補分期資料,F.補分期資料批次處理完成"));
			}
		}
	}

	void detlWkdataLiad570() {
		String wkData = "";
		wkData = wp.colStr("pay_normal_flag_2");
		wp.colSet("tt_pay_normal_flag_2", commString.decode(wkData, ",Y,N", ",Y.正常,N.不正常"));

		wkData = wp.colStr("send_flag_571");
		wp.colSet("tt_send_flag_571", commString.decode(wkData, ",Y,N", ",Y.回報,N.不回報"));
	}

	void goToZ60Update() throws Exception {
		String strModData = "", dbAudCode = "";
		String sql = "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'Z60' and data_key = '"
				+ kkLiadDocNo + "' ";
		sqlSelect(sql);
		strModData = sqlStr("mod_data");
		dbAudCode = sqlStr("aud_code");
		wp.colSet("aud_code", dbAudCode);
		wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
		String[] modData = strModData.split("\";\"", -1);
		if (sqlRowNum > 0) {
//			alert_err("資料待覆核.....");
			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";

			if (!modData[0].equals(wp.colStr("credit_branch"))) {
				wp.colSet("credit_branch", modData[0]);
				wp.colSet("credit_branch_pink", "pink");
			}
			if (!modData[1].equals(wp.colStr("law_user_id"))) {
				wp.colSet("law_user_id", modData[1]);
				wp.colSet("law_user_id_pink", "pink");
			}
			if (!modData[2].equals(wp.colStr("demand_user_id"))) {
				wp.colSet("demand_user_id", modData[2]);
				wp.colSet("demand_user_id_pink", "pink");
			}
			if (!modData[3].equals(wp.colStr("branch_comb_flag"))) {
				wp.colSet("branch_comb_flag", modData[3]);
				wp.colSet("branch_comb_flag_pink", "pink");
			}
			if (!modData[4].equals(wp.colStr("court_id"))) {
				wp.colSet("court_id", modData[4]);
				wp.colSet("court_id_pink", "pink");
			}
			if (!modData[5].equals(wp.colStr("court_name"))) {
				wp.colSet("court_name", modData[5]);
				wp.colSet("court_name_pink", "pink");
			}
			if (!modData[6].equals(wp.colStr("case_year"))) {
				wp.colSet("case_year", modData[6]);
				wp.colSet("case_year_pink", "pink");
			}
			if (!modData[7].equals(wp.colStr("case_letter"))) {
				wp.colSet("case_letter", modData[7]);
				wp.colSet("case_letter_pink", "pink");
			}
			if (!modData[8].equals(wp.colStr("case_no"))) {
				wp.colSet("case_no", modData[8]);
				wp.colSet("case_no_pink", "pink");
			}
			if (!modData[9].equals(wp.colStr("bullet_date"))) {
				wp.colSet("bullet_date", modData[9]);
				wp.colSet("bullet_date_pink", "pink");
			}
			if (!modData[10].equals(wp.colStr("bullet_desc"))) {
				wp.colSet("bullet_desc", modData[10]);
				wp.colSet("bullet_desc_pink", "pink");
			}
		} else {
			alertErr("查詢無異動資料, 文件編號:" + kkLiadDocNo);
			wp.colSet("review_disabled", "disabled");
			wp.colSet("next_disabled", "disabled");
		}
	}

	void goToRenewUpdate() throws Exception {
		String strModData = "", dbAudCode = "";
		String sql = "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'RENEW' and data_key = '"
				+ kkLiadDocNo + "' ";
		sqlSelect(sql);
		strModData = sqlStr("mod_data");
		dbAudCode = sqlStr("aud_code");
		wp.colSet("aud_code", dbAudCode);
		wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
		String[] modData = strModData.split("\";\"", -1);
		if (sqlRowNum > 0) {
//			alert_err("資料待覆核.....");
			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";

			if (!modData[0].equals(wp.colStr("recv_date"))) {
				wp.colSet("recv_date", modData[0]);
				wp.colSet("recv_date_pink", "pink");
			}
			if (!modData[1].equals(wp.colStr("branch_comb_flag"))) {
				wp.colSet("branch_comb_flag", modData[1]);
				wp.colSet("branch_comb_flag_pink", "pink");
			}
			if (!modData[2].equals(wp.colStr("max_bank_flag"))) {
				wp.colSet("max_bank_flag", modData[2]);
				wp.colSet("max_bank_flag_pink", "pink");
			}
			if (!modData[3].equals(wp.colStr("court_id"))) {
				wp.colSet("court_id", modData[3]);
				wp.colSet("court_id_pink", "pink");
			}
			if (!modData[4].equals(wp.colStr("renew_status"))) {
				wp.colSet("renew_status", modData[4]);
				wp.colSet("renew_status_pink", "pink");
			}
			if (!modData[5].equals(wp.colStr("court_status"))) {
				wp.colSet("court_status", modData[5]);
				wp.colSet("court_status_pink", "pink");
			}
			if (!modData[6].equals(wp.colStr("case_year"))) {
				wp.colSet("case_year", modData[6]);
				wp.colSet("case_year_pink", "pink");
			}
			if (!modData[7].equals(wp.colStr("case_letter_desc"))) {
				wp.colSet("case_letter_desc", modData[7]);
				wp.colSet("case_letter_desc_pink", "pink");
			}
			if (!modData[8].equals(wp.colStr("judic_date"))) {
				wp.colSet("judic_date", modData[8]);
				wp.colSet("judic_date_pink", "pink");
			}
			if (!modData[9].equals(wp.colStr("confirm_date"))) {
				wp.colSet("confirm_date", modData[9]);
				wp.colSet("confirm_date_pink", "pink");
			}
			if (toNum(modData[10]) != wp.colNum("payoff_amt")) {
				wp.colSet("payoff_amt", modData[10]);
				wp.colSet("payoff_amt_pink", "pink");
			}
			if (!modData[11].equals(wp.colStr("run_renew_flag"))) {
				wp.colSet("run_renew_flag", modData[11]);
				wp.colSet("run_renew_flag_pink", "pink");
			}
			if (!modData[12].equals(wp.colStr("doc_chi_name"))) {
				wp.colSet("doc_chi_name", modData[12]);
				wp.colSet("doc_chi_name_pink", "pink");
			}
			if (!modData[13].equals(wp.colStr("credit_branch"))) {
				wp.colSet("credit_branch", modData[13]);
				wp.colSet("credit_branch_pink", "pink");
			}
			if (!modData[14].equals(wp.colStr("org_debt_amt_bef"))) {
				wp.colSet("org_debt_amt_bef", modData[14]);
				wp.colSet("org_debt_amt_bef_pink", "pink");
			}
			if (!modData[15].equals(wp.colStr("case_letter"))) {
				wp.colSet("case_letter", modData[15]);
				wp.colSet("case_letter_pink", "pink");
			}
			if (!modData[16].equals(wp.colStr("renew_cancel_date"))) {
				wp.colSet("renew_cancel_date", modData[16]);
				wp.colSet("renew_cancel_date_pink", "pink");
			}
			if (!modData[17].equals(wp.colStr("renew_first_date"))) {
				wp.colSet("renew_first_date", modData[17]);
				wp.colSet("renew_first_date_pink", "pink");
			}
			if (!modData[18].equals(wp.colStr("renew_int"))) {
				wp.colSet("renew_int", modData[18]);
				wp.colSet("renew_int_pink", "pink");
			}
			if (!modData[19].equals(wp.colStr("deliver_date"))) {
				wp.colSet("deliver_date", modData[19]);
				wp.colSet("deliver_date_pink", "pink");
			}
			if (!modData[20].equals(wp.colStr("renew_damage_date"))) {
				wp.colSet("renew_damage_date", modData[20]);
				wp.colSet("renew_damage_date_pink", "pink");
			}
			if (!modData[21].equals(wp.colStr("court_dept"))) {
				wp.colSet("court_dept", modData[21]);
				wp.colSet("court_dept_pink", "pink");
			}
			if (!modData[22].equals(wp.colStr("judic_action_flag"))) {
				wp.colSet("judic_action_flag", modData[22]);
				wp.colSet("judic_action_flag_pink", "pink");
			}
			if (!modData[23].equals(wp.colStr("action_date_s"))) {
				wp.colSet("action_date_s", modData[23]);
				wp.colSet("action_date_s_pink", "pink");
			}
			if (!modData[24].equals(wp.colStr("judic_cancel_flag"))) {
				wp.colSet("judic_cancel_flag", modData[24]);
				wp.colSet("judic_cancel_flag_pink", "pink");
			}
			if (!modData[25].equals(wp.colStr("cancel_date"))) {
				wp.colSet("cancel_date", modData[25]);
				wp.colSet("cancel_date_pink", "pink");
			}
			if (!modData[26].equals(wp.colStr("renew_last_date"))) {
				wp.colSet("renew_last_date", modData[26]);
				wp.colSet("renew_last_date_pink", "pink");
			}
			if (!modData[27].equals(wp.colStr("payment_day"))) {
				wp.colSet("payment_day", modData[27]);
				wp.colSet("payment_day_pink", "pink");
			}
			if (!modData[28].equals(wp.colStr("renew_rate"))) {
				wp.colSet("renew_rate", modData[28]);
				wp.colSet("renew_rate_pink", "pink");
			}
			if (toNum(modData[29]) != wp.colNum("renew_lose_amt")) {
				wp.colSet("renew_lose_amt", modData[29]);
				wp.colSet("renew_lose_amt_pink", "pink");
			}
			if (toNum(modData[30]) != wp.colNum("org_debt_amt_bef_base")) {
				wp.colSet("org_debt_amt_bef_base", modData[30]);
				wp.colSet("org_debt_amt_bef_base_pink", "pink");
			}
			if (!modData[31].equals(wp.colStr("super_name"))) {
				wp.colSet("super_name", modData[31]);
				wp.colSet("super_name_pink", "pink");
			}
		} else {
			alertErr("查詢無異動資料, 文件編號:" + kkLiadDocNo);
			wp.colSet("review_disabled", "disabled");
			wp.colSet("next_disabled", "disabled");
		}
	}

	void goToLiquUpdate() throws Exception {
		String strModData = "", dbAudCode = "";
		String sql = "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'LIQUIDATE' and data_key = '"
				+ kkLiadDocNo + "' ";
		sqlSelect(sql);
		strModData = sqlStr("mod_data");
		dbAudCode = sqlStr("aud_code");
		wp.colSet("aud_code", dbAudCode);
		wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
		String[] modData = strModData.split("\";\"", -1);
		if (sqlRowNum > 0) {
//			alert_err("資料待覆核.....");
			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";

			if (!modData[0].equals(wp.colStr("recv_date"))) {
				wp.colSet("recv_date", modData[0]);
				wp.colSet("recv_date_pink", "pink");
			}
			if (!modData[1].equals(wp.colStr("liqu_status"))) {
				wp.colSet("liqu_status", modData[1]);
				wp.colSet("liqu_status_pink", "pink");
			}
			if (!modData[2].equals(wp.colStr("branch_comb_flag"))) {
				wp.colSet("branch_comb_flag", modData[2]);
				wp.colSet("branch_comb_flag_pink", "pink");
			}
			if (!modData[3].equals(wp.colStr("credit_branch"))) {
				wp.colSet("credit_branch", modData[3]);
				wp.colSet("credit_branch_pink", "pink");
			}
			if (!modData[4].equals(wp.colStr("max_bank_flag"))) {
				wp.colSet("max_bank_flag", modData[4]);
				wp.colSet("max_bank_flag_pink", "pink");
			}
			if (!modData[5].equals(wp.colStr("org_debt_amt_bef"))) {
				wp.colSet("org_debt_amt_bef", modData[5]);
				wp.colSet("org_debt_amt_bef_pink", "pink");
			}
			if (toNum(modData[6]) != wp.colNum("org_debt_amt_bef_base")) {
				wp.colSet("org_debt_amt_bef_base", modData[6]);
				wp.colSet("org_debt_amt_bef_base_pink", "pink");
			}
			if (!modData[7].equals(wp.colStr("liqu_lose_amt"))) {
				wp.colSet("liqu_lose_amt", modData[7]);
				wp.colSet("liqu_lose_amt_pink", "pink");
			}
			if (!modData[8].equals(wp.colStr("court_id"))) {
				wp.colSet("court_id", modData[8]);
				wp.colSet("court_id_pink", "pink");
			}
			if (!modData[9].equals(wp.colStr("court_name"))) {
				wp.colSet("court_name", modData[9]);
				wp.colSet("court_name_pink", "pink");
			}
			if (!modData[10].equals(wp.colStr("doc_chi_name"))) {
				wp.colSet("doc_chi_name", modData[10]);
				wp.colSet("doc_chi_name_pink", "pink");
			}
			if (!modData[11].equals(wp.colStr("court_dept"))) {
				wp.colSet("court_dept", modData[11]);
				wp.colSet("court_dept_pink", "pink");
			}
			if (!modData[12].equals(wp.colStr("court_status"))) {
				wp.colSet("court_status", modData[12]);
				wp.colSet("court_status_pink", "pink");
			}
			if (!modData[13].equals(wp.colStr("case_year"))) {
				wp.colSet("case_year", modData[13]);
				wp.colSet("case_year_pink", "pink");
			}
			if (!modData[14].equals(wp.colStr("case_letter"))) {
				wp.colSet("case_letter", modData[14]);
				wp.colSet("case_letter_pink", "pink");
			}
			if (!modData[15].equals(wp.colStr("case_letter_desc"))) {
				wp.colSet("case_letter_desc", modData[15]);
				wp.colSet("case_letter_desc_pink", "pink");
			}
			if (!modData[16].equals(wp.colStr("judic_avoid_flag"))) {
				wp.colSet("judic_avoid_flag", modData[16]);
				wp.colSet("judic_avoid_flag_pink", "pink");
			}
			if (!modData[17].equals(wp.colStr("judic_avoid_sure_flag"))) {
				wp.colSet("judic_avoid_sure_flag", modData[17]);
				wp.colSet("judic_avoid_sure_flag_pink", "pink");
			}
			if (!modData[18].equals(wp.colStr("judic_avoid_no"))) {
				wp.colSet("judic_avoid_no", modData[18]);
				wp.colSet("judic_avoid_no_pink", "pink");
			}
			if (!modData[19].equals(wp.colStr("judic_date"))) {
				wp.colSet("judic_date", modData[19]);
				wp.colSet("judic_date_pink", "pink");
			}
			if (!modData[20].equals(wp.colStr("judic_action_flag"))) {
				wp.colSet("judic_action_flag", modData[20]);
				wp.colSet("judic_action_flag_pink", "pink");
			}
			if (!modData[21].equals(wp.colStr("action_date_s"))) {
				wp.colSet("action_date_s", modData[21]);
				wp.colSet("action_date_s_pink", "pink");
			}
			if (!modData[22].equals(wp.colStr("judic_cancel_flag"))) {
				wp.colSet("judic_cancel_flag", modData[22]);
				wp.colSet("judic_cancel_flag_pink", "pink");
			}
			if (!modData[23].equals(wp.colStr("cancel_date"))) {
				wp.colSet("cancel_date", modData[23]);
				wp.colSet("cancel_date_pink", "pink");
			}
			if (!modData[24].equals(wp.colStr("law_133_flag"))) {
				wp.colSet("law_133_flag_pink", "pink");
				wp.colSet("law_133_flag", modData[24]);
			}
		} else {
			alertErr("查詢無異動資料, 文件編號:" + kkLiadDocNo);
			wp.colSet("review_disabled", "disabled");
			wp.colSet("next_disabled", "disabled");
		}
	}

	void gotoLiadModTmpUpdate() throws Exception {
		String strModData = "", dbAudCode = "";
		strModData = wp.colStr("mod_data");
		dbAudCode = wp.colStr("aud_code");
		wp.colSet("db_aud_code", commString.decode(dbAudCode, ",A,U,D", ",新增,修改,刪除"));
		String[] modData = strModData.split("\";\"", -1);
		if (modData.length > 0) {
//			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
			wp.colSet("chi_name", modData[0]);
			wp.colSet("recv_date", modData[1]);
			wp.colSet("tt_renew_status", wfColRenewStatus("3", modData[2]));
			wp.colSet("renew_status", modData[2]);
			wp.colSet("org_debt_amt", modData[3]);
			wp.colSet("court_status", modData[4]);
			wp.colSet("liad_doc_no", modData[5]);
		}
	}

	void goToRenewCourtUpdate() throws Exception {
		String strModData = "", dbAudCode = "";
//		String sql = "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'COURT' and data_key = '"+kk_liad_doc_no+"' ";
		String sql = "select mod_data,aud_code from col_liad_mod_tmp "
				+ "where data_type = 'RENEWCOURT' and data_key = :data_key ";
		setString("data_key", kkLiadDocNo + kkLiadDocSeqno);
		sqlSelect(sql);
		strModData = sqlStr("mod_data");
		dbAudCode = sqlStr("aud_code");
		wp.colSet("aud_code", dbAudCode);
//		wp.col_set("db_aud_code", commString.decode(db_aud_code, ",A,U,D", ",新增,修改,刪除"));
		String[] modData = strModData.split("\";\"", -1);
		if (sqlRowNum > 0) {
			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";

			if (!modData[0].equals(wp.colStr("unit_doc_no"))) {
				wp.colSet("unit_doc_no", modData[0]);
				wp.colSet("unit_doc_no_pink", "pink");
			}
			if (!modData[1].equals(wp.colStr("recv_date"))) {
				wp.colSet("recv_date", modData[1]);
				wp.colSet("recv_date_pink", "pink");
			}
			if (!modData[2].equals(wp.colStr("key_note"))) {
				wp.colSet("key_note", modData[2]);
				wp.colSet("key_note_pink", "pink");
			}
			if (!modData[3].equals(wp.colStr("case_letter_desc"))) {
				wp.colSet("case_letter_desc", modData[3]);
				wp.colSet("case_letter_desc_pink", "pink");
			}
			if (!modData[4].equals(wp.colStr("case_date"))) {
				wp.colSet("case_date", modData[4]);
				wp.colSet("case_date_pink", "pink");
			}
		} else {
			alertErr("查詢無異動資料, 文件編號:" + kkLiadDocNo + "; 序號:" + kkLiadDocSeqno);
			wp.colSet("review_disabled", "disabled");
			wp.colSet("next_disabled", "disabled");
		}
	}

	void goToLiquCourtUpdate() throws Exception {
		String strModData = "", dbAudCode = "";
		String sql = "select mod_data,aud_code from col_liad_mod_tmp "
				+ "where data_type = 'LIQUICOURT' and data_key = :data_key ";
		setString("data_key", kkLiadDocNo + kkLiadDocSeqno);
		sqlSelect(sql);
		strModData = sqlStr("mod_data");
		dbAudCode = sqlStr("aud_code");
		wp.colSet("aud_code", dbAudCode);
//		wp.col_set("db_aud_code", commString.decode(db_aud_code, ",A,U,D", ",新增,修改,刪除"));
		String[] modData = strModData.split("\";\"", -1);
		if (sqlRowNum > 0) {
			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";

			if (!modData[0].equals(wp.colStr("unit_doc_no"))) {
				wp.colSet("unit_doc_no", modData[0]);
				wp.colSet("unit_doc_no_pink", "pink");
			}
			if (!modData[1].equals(wp.colStr("recv_date"))) {
				wp.colSet("recv_date", modData[1]);
				wp.colSet("recv_date_pink", "pink");
			}
			if (!modData[2].equals(wp.colStr("key_note"))) {
				wp.colSet("key_note", modData[2]);
				wp.colSet("key_note_pink", "pink");
			}
			if (!modData[3].equals(wp.colStr("case_letter_desc"))) {
				wp.colSet("case_letter_desc", modData[3]);
				wp.colSet("case_letter_desc_pink", "pink");
			}
			if (!modData[4].equals(wp.colStr("case_date"))) {
				wp.colSet("case_date", modData[4]);
				wp.colSet("case_date_pink", "pink");
			}
		} else {
			alertErr("查詢無異動資料, 文件編號:" + kkLiadDocNo + "; 序號:" + kkLiadDocSeqno);
			wp.colSet("review_disabled", "disabled");
			wp.colSet("next_disabled", "disabled");
		}
	}

	void gotoColldataUpdate() {
		String strModData = "";
		String sql = "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'COLL-DATA' "
				+ "and data_key = rpad ('" + kkIdNo + "', 10, ' ') || rpad ('" + kkCaseLetter + "', 10, ' ') ";
		sqlSelect(sql);
		strModData = sqlStr("mod_data");
		String[] modData = strModData.split("\";\"", -1);
		if (sqlRowNum > 0) {
			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";

			if (!modData[0].equals(wp.colStr("pay_normal_flag_2"))) {
				wp.colSet("pay_normal_flag_2_pink", "pink");
				wp.colSet("pay_normal_flag_2", modData[0]);
				wp.colSet("tt_pay_normal_flag_2", commString.decode(modData[0], ",Y,N", ",Y.正常,N.不正常"));
			}
			if (!modData[1].equals(wp.colStr("debt_amt2"))) {
				wp.colSet("debt_amt2_pink", "pink");
				wp.colSet("debt_amt2", modData[1]);
			}
			if (!modData[2].equals(wp.colStr("alloc_debt_amt"))) {
				wp.colSet("alloc_debt_amt_pink", "pink");
				wp.colSet("alloc_debt_amt", modData[2]);
			}
			if (!modData[3].equals(wp.colStr("unalloc_debt_amt"))) {
				wp.colSet("unalloc_debt_amt_pink", "pink");
				wp.colSet("unalloc_debt_amt", modData[3]);
			}
			if (!modData[4].equals(wp.colStr("coll_remark"))) {
				wp.colSet("coll_remark_pink", "pink");
				wp.colSet("coll_remark", modData[4]);
			}
			if (!modData[5].equals(wp.colStr("send_flag_571"))) {
				wp.colSet("send_flag_571_pink", "pink");
				wp.colSet("send_flag_571", modData[5]);
				wp.colSet("tt_send_flag_571", commString.decode(modData[5], ",Y,N", ",Y.回報,N.不回報"));
			}
			if (!modData[6].equals(wp.colStr("crt_date"))) {
				wp.colSet("crt_date", modData[6]);
			}
			if (!modData[7].equals(wp.colStr("crt_user"))) {
				wp.colSet("crt_user", modData[7]);
			}
		} else {
			alertErr("查詢無異動資料, ID:" + kkIdNo + "; 文號= " + kkCaseLetter);
			wp.colSet("review_disabled", "disabled");
			wp.colSet("next_disabled", "disabled");
		}
	}

	void gotoCollCloseApr() {
		String strModData = "";
		String sql = "select mod_data,aud_code from col_liad_mod_tmp where data_type = 'COLL-CLOSE' "
				+ "and data_key = rpad ('" + kkIdNo + "', 10, ' ') || rpad ('" + kkCaseLetter + "', 10, ' ') ";
		sqlSelect(sql);
		strModData = sqlStr("mod_data");
		String[] modData = strModData.split("\";\"", -1);
		if (sqlRowNum > 0) {
			wp.alertMesg = "<script language='javascript'> alert('資料待覆核.....')</script>";
			if (!modData[0].equals(wp.colStr("close_reason"))) {
				wp.colSet("close_reason_pink", "pink");
				wp.colSet("close_reason", modData[0]);
			}
			if (!modData[1].equals(wp.colStr("close_remark"))) {
				wp.colSet("close_remark_pink", "pink");
				wp.colSet("close_remark", modData[1]);
			}
			if (!modData[2].equals(wp.colStr("close_date"))) {
				// wp.col_set("close_date_pink", "pink");
				wp.colSet("close_date", modData[2]);
			}
			if (!modData[3].equals(wp.colStr("close_user"))) {
				wp.colSet("close_user_pink", "pink");
				wp.colSet("close_user", modData[3]);
			}
		} else {
			alertErr("查詢無異動資料, ID:" + kkIdNo + "; 文號= " + kkCaseLetter);
			wp.colSet("review_disabled", "disabled");
			wp.colSet("next_disabled", "disabled");
		}
	}

	String wfPtrBranchName(String idcode) throws Exception {
		String rtn = "";
//		String ls_sql = "select branch||' ['||branch_name||']' id_desc from ptr_branch "
		String lsSql = "select branch||' ['||full_chi_name||']' id_desc from gen_brn " + "where branch= :id_code ";
		setString("id_code", idcode);

		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			rtn = idcode;
		} else {
			rtn = sqlStr("id_desc");
		}
		return rtn;
	}

	String wfSecUserIDName(String idcode) throws Exception {
		String rtn = "";
		String lsSql = "select usr_id||' ['||usr_cname||']' id_desc from sec_user " + "where usr_id= :id_code ";
		setString("id_code", idcode);

		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			rtn = idcode;
		} else {
			rtn = sqlStr("id_desc");
		}
		return rtn;
	}

	String wfPtrCourtName(String idcode) throws Exception {
		String rtn = "";
		String lsSql = "select wf_id||' ['||wf_desc||']' id_desc from ptr_sys_idtab "
				+ "where wf_type = 'COURT_NAME' and wf_id= :id_code ";
		setString("id_code", idcode);

		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			rtn = idcode;
		} else {
			rtn = sqlStr("id_desc");
		}
		return rtn;
	}

	String wfColRenewStatus(String idkey, String idcode) throws Exception {
		String rtn = "";
		String lsSql = "select id_code||' ['||id_desc||']' id_desc from col_liab_idtab "
				+ "where id_key= :idkey and id_code= :id_code ";
		setString("idkey", idkey);
		setString("id_code", idcode);

		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			rtn = idcode;
		} else {
			rtn = sqlStr("id_desc");
		}
		return rtn;
	}

	String wfPtrCourtStatus(String idcode) throws Exception {
		String rtn = "";
		String lsSql = "select wf_id||' ['||wf_desc||']' id_desc from ptr_sys_idtab "
				+ "where wf_type = 'LIAD_RENEW_STATUS' and wf_id= :id_code ";
		setString("id_code", idcode);

		sqlSelect(lsSql);
		if (sqlRowNum == 0) {
			rtn = idcode;
		} else {
			rtn = sqlStr("id_desc");
		}
		return rtn;
	}

	public void dataProcess() throws Exception {
		dataKK1 = wp.itemStr("data_k1");
		switch (dataKK1) {
		case "aopt":
			dataProcessZ60();
			break;
		case "bopt":
			dataProcessRenew();
			break;
		case "copt":
			dataProcessLiqu();
			break;
		case "dopt":
			dataProcessLiadModTmp();
			break;
		case "eopt":
			dataProcessLiad570();
			break;
		case "fopt":
			dataProcessLiad570Close();
			break;
		case "gopt":
			dataProcessRenewCourt();
			break;
		case "hopt":
			dataProcessLiquCourt();
			break;
		}
	}

	public void dataProcessZ60() throws Exception {
		String[] lsLiadDocNo = wp.itemBuff("a-liad_doc_no");
		String[] opt = wp.itemBuff("aopt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsLiadDocNo[rr];
		}
		kkString = kkString.substring(1);

		dataReadZ60();
	}

	public void dataProcessRenew() throws Exception {
		String[] lsLiadDocNo = wp.itemBuff("b-liad_doc_no");
		String[] opt = wp.itemBuff("bopt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsLiadDocNo[rr];
		}
		kkString = kkString.substring(1);

		dataReadRenew();
	}

	public void dataProcessLiqu() throws Exception {
		String[] lsLiadDocNo = wp.itemBuff("c-liad_doc_no");
		String[] opt = wp.itemBuff("copt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsLiadDocNo[rr];
		}
		kkString = kkString.substring(1);

		dataReadLiqu();
	}

	public void dataProcessLiadModTmp() throws Exception {
		String[] lsIdNo = wp.itemBuff("d-db_id");
		String[] lsCaseLetter = wp.itemBuff("d-db_case_letter");
		String[] opt = wp.itemBuff("dopt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsIdNo[rr] + ";" + lsCaseLetter[rr];
		}
		kkString = kkString.substring(1);
		dataReadLiadModTmp();
	}

	public void dataProcessLiad570() throws Exception {
		String[] lsIdNo = wp.itemBuff("e-id_no");
		String[] lsCaseLetter = wp.itemBuff("e-case_letter");
		String[] opt = wp.itemBuff("eopt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsIdNo[rr] + ";" + lsCaseLetter[rr];
		}
		kkString = kkString.substring(1);
		dataReadLiad570();
	}

	public void dataProcessLiad570Close() throws Exception {
		String[] lsIdNo = wp.itemBuff("f-id_no");
		String[] lsCaseLetter = wp.itemBuff("f-case_letter");
		String[] opt = wp.itemBuff("fopt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsIdNo[rr] + ";" + lsCaseLetter[rr];
		}
		kkString = kkString.substring(1);
		dataReadLiad570Close();
	}

	public void dataProcessRenewCourt() throws Exception {
		String[] lsLiadDocNo = wp.itemBuff("g-liad_doc_no");
		String[] lsLiadDocSeqno = wp.itemBuff("g-liad_doc_seqno");
		String[] opt = wp.itemBuff("gopt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsLiadDocNo[rr] + ";" + lsLiadDocSeqno[rr];
		}
		kkString = kkString.substring(1);

		dataReadRenewCourt();
	}

	public void dataProcessLiquCourt() throws Exception {
		String[] lsLiadDocNo = wp.itemBuff("h-liad_doc_no");
		String[] lsLiadDocSeqno = wp.itemBuff("h-liad_doc_seqno");
		String[] opt = wp.itemBuff("hopt");

		// Detail Control
		detailControl();

		int rr = -1;
		for (int ii = 0; ii < opt.length; ii++) {
			rr = (int) this.toNum(opt[ii]) - 1;
			if (rr < 0)
				continue;

			kkString += ";" + lsLiadDocNo[rr] + ";" + lsLiadDocSeqno[rr];
		}
		kkString = kkString.substring(1);

		dataReadLiquCourt();
	}

	public void saveFunc() throws Exception {
		dataKK1 = wp.itemStr("data_k1");
		kkString = wp.itemStr("kk_string");
		switch (dataKK1) {
		case "aopt":
			saveFuncZ60();
			break;
		case "bopt":
			saveFuncRenew();
			break;
		case "copt":
			saveFuncLiqu();
			break;
		case "dopt":
			saveFuncLiadModTmp();
			break;
		case "eopt":
			saveFuncLiad570();
			break;
		case "fopt":
			saveFuncLiad570Close();
			break;
		case "gopt":
			saveFuncRenewCourt();
			break;
		case "hopt":
			saveFuncLiquCourt();
			break;
		}
	}

	public void saveFuncZ60() throws Exception {
		funcZ60 = new Colp1150Func(wp);

		kkLiadDocNo = wp.itemStr("liad_doc_no");
		kkType = "Z60";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}

		funcZ60.varsSet("liad_doc_no", kkLiadDocNo);
		funcZ60.varsSet("mod_seqno", wp.itemStr("mod_seqno"));
		funcZ60.varsSet("aud_code", wp.itemStr("aud_code"));
		funcZ60.varsSet("credit_branch", wp.itemStr("credit_branch"));
		funcZ60.varsSet("law_user_id", wp.itemStr("law_user_id"));
		funcZ60.varsSet("demand_user_id", wp.itemStr("demand_user_id"));
		funcZ60.varsSet("branch_comb_flag", wp.itemStr("db_branch_comb_flag"));
		funcZ60.varsSet("court_id", wp.itemStr("court_id"));
		funcZ60.varsSet("court_name", wp.itemStr("court_name"));
		funcZ60.varsSet("case_year", wp.itemStr("case_year"));
		funcZ60.varsSet("case_letter", wp.itemStr("case_letter"));
		funcZ60.varsSet("case_no", wp.itemStr("case_no"));
		funcZ60.varsSet("bullet_date", wp.itemStr("bullet_date"));
		funcZ60.varsSet("bullet_desc", wp.itemStr("bullet_desc"));

		rc = funcZ60.dataProc();
		if (rc != 1) {
			alertErr2(funcZ60.getMsg());
		} else {
			rc = funcZ60.deleteColLiadModTmp();
			if (rc != 1) {
				alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
			}
		}
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	public void saveFuncRenew() throws Exception {
		funcRenew = new Colp1160Func(wp);

		kkLiadDocNo = wp.itemStr("liad_doc_no");
		kkIdNo = wp.itemStr("id_no");
		kkIdPSeqno = wp.itemStr("id_p_seqno");
		kkCaseLetter = wp.itemStr("case_letter");
		kkType = "RENEW";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}

		funcRenew.varsSet("liad_doc_no", kkLiadDocNo);
		funcRenew.varsSet("mod_seqno", wp.itemStr("mod_seqno"));
		funcRenew.varsSet("aud_code", wp.itemStr("aud_code"));
		funcRenew.varsSet("recv_date", wp.itemStr("recv_date"));
		funcRenew.varsSet("branch_comb_flag", wp.itemStr("branch_comb_flag"));
		funcRenew.varsSet("max_bank_flag", wp.itemStr("max_bank_flag"));
		funcRenew.varsSet("court_id", wp.itemStr("court_id"));
		funcRenew.varsSet("renew_status", wp.itemStr("renew_status"));
		funcRenew.varsSet("court_status", wp.itemStr("court_status"));
		funcRenew.varsSet("case_year", wp.itemStr("case_year"));
		funcRenew.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
		funcRenew.varsSet("judic_date", wp.itemStr("judic_date"));
		funcRenew.varsSet("confirm_date", wp.itemStr("confirm_date"));
		funcRenew.varsSet("payoff_amt", wp.itemStr("payoff_amt"));
		funcRenew.varsSet("run_renew_flag", wp.itemStr("run_renew_flag"));
		funcRenew.varsSet("doc_chi_name", wp.itemStr("doc_chi_name"));
		funcRenew.varsSet("credit_branch", wp.itemStr("credit_branch"));
		funcRenew.varsSet("org_debt_amt", wp.itemStr("org_debt_amt"));
		funcRenew.varsSet("org_debt_amt_bef", wp.itemStr("org_debt_amt_bef"));
		funcRenew.varsSet("org_debt_amt_bef_base", wp.itemStr("org_debt_amt_bef_base"));
		funcRenew.varsSet("case_letter", wp.itemStr("case_letter"));
		funcRenew.varsSet("renew_cancel_date", wp.itemStr("renew_cancel_date"));
		funcRenew.varsSet("renew_first_date", wp.itemStr("renew_first_date"));
		funcRenew.varsSet("renew_int", wp.itemStr("renew_int"));
		funcRenew.varsSet("deliver_date", wp.itemStr("deliver_date"));
		funcRenew.varsSet("renew_damage_date", wp.itemStr("renew_damage_date"));
		funcRenew.varsSet("court_dept", wp.itemStr("court_dept"));
		funcRenew.varsSet("judic_action_flag", wp.itemStr("judic_action_flag"));
		funcRenew.varsSet("action_date_s", wp.itemStr("action_date_s"));
		funcRenew.varsSet("judic_cancel_flag", wp.itemStr("judic_cancel_flag"));
		funcRenew.varsSet("cancel_date", wp.itemStr("cancel_date"));
		funcRenew.varsSet("renew_last_date", wp.itemStr("renew_last_date"));
		funcRenew.varsSet("payment_day", wp.itemStr("payment_day"));
		funcRenew.varsSet("renew_rate", wp.itemStr("renew_rate"));
		funcRenew.varsSet("renew_lose_amt", wp.itemStr("renew_lose_amt"));
		funcRenew.varsSet("super_name", wp.itemStr("super_name"));
		// insert col_liad_remod
		funcRenew.varsSet("id_no", wp.itemStr("id_no"));
		funcRenew.varsSet("id_p_seqno", wp.itemStr("id_p_seqno"));

		rc = funcRenew.dataProc();
		if (rc != 1) {
			alertErr2(funcRenew.getMsg());
		} else {
			rc = funcRenew.deleteColLiadModTmp();
			if (rc != 1) {
				alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
			} else {
				rc = fPaymainRenew();
				if (rc != 1) {
					alertErr("執行[f_paymain_renew] error");
				}
			}
		}
		// 若dw_data.item("renew_status")有變更，新增一筆【COL_LIAD_REMOD 更生清算狀態異動檔】。20180321
		// 狀態為[新增]時,也要新增remod 2019.10.08 phopho
		if ((eqIgno(wp.itemStr("renew_status_pink"), "pink")) || (eqIgno(wp.itemStr("aud_code"), "A"))) {
			rc = funcRenew.insertColLiadRemod();
			if (rc != 1) {
				alertErr("新增 [col_liad_remod] error");
			}
		}
		addLiadLog1160();// V0.3
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	public void saveFuncLiqu() throws Exception {
		funcLiqu = new Colp1170Func(wp);

		kkLiadDocNo = wp.itemStr("liad_doc_no");
		kkIdNo = wp.itemStr("id_no");
		kkIdPSeqno = wp.itemStr("id_p_seqno");
		kkCaseLetter = wp.itemStr("case_letter");
		kkType = "LIQUIDATE";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}

		funcLiqu.varsSet("liad_doc_no", kkLiadDocNo);
		funcLiqu.varsSet("mod_seqno", wp.itemStr("mod_seqno"));
		funcLiqu.varsSet("aud_code", wp.itemStr("aud_code"));
		funcLiqu.varsSet("recv_date", wp.itemStr("recv_date"));
		funcLiqu.varsSet("liqu_status", wp.itemStr("liqu_status"));
		funcLiqu.varsSet("branch_comb_flag", wp.itemStr("branch_comb_flag"));
		funcLiqu.varsSet("credit_branch", wp.itemStr("credit_branch"));
		funcLiqu.varsSet("max_bank_flag", wp.itemStr("max_bank_flag"));
		funcLiqu.varsSet("org_debt_amt", wp.itemStr("org_debt_amt"));
		funcLiqu.varsSet("org_debt_amt_bef", wp.itemStr("org_debt_amt_bef"));
		funcLiqu.varsSet("org_debt_amt_bef_base", wp.itemStr("org_debt_amt_bef_base"));
		funcLiqu.varsSet("liqu_lose_amt", wp.itemStr("liqu_lose_amt"));
		funcLiqu.varsSet("court_id", wp.itemStr("court_id"));
		funcLiqu.varsSet("court_name", wp.itemStr("court_name"));
		funcLiqu.varsSet("doc_chi_name", wp.itemStr("doc_chi_name"));
		funcLiqu.varsSet("court_dept", wp.itemStr("court_dept"));
		funcLiqu.varsSet("court_status", wp.itemStr("court_status"));
		funcLiqu.varsSet("case_year", wp.itemStr("case_year"));
		funcLiqu.varsSet("case_letter", wp.itemStr("case_letter"));
		funcLiqu.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
		funcLiqu.varsSet("judic_avoid_flag", wp.itemStr("judic_avoid_flag"));
		funcLiqu.varsSet("judic_avoid_sure_flag", wp.itemStr("judic_avoid_sure_flag"));
		funcLiqu.varsSet("judic_avoid_no", wp.itemStr("judic_avoid_no"));
		funcLiqu.varsSet("judic_date", wp.itemStr("judic_date"));
		funcLiqu.varsSet("judic_action_flag", wp.itemStr("judic_action_flag"));
		funcLiqu.varsSet("action_date_s", wp.itemStr("action_date_s"));
		funcLiqu.varsSet("judic_cancel_flag", wp.itemStr("judic_cancel_flag"));
		funcLiqu.varsSet("cancel_date", wp.itemStr("cancel_date"));
		funcLiqu.varsSet("law_133_flag", wp.itemStr("law_133_flag"));
		// insert col_liad_remod
		funcLiqu.varsSet("id_no", wp.itemStr("id_no"));
		funcLiqu.varsSet("id_p_seqno", wp.itemStr("id_p_seqno"));

		rc = funcLiqu.dataProc();
		if (rc != 1) {
			alertErr2(funcLiqu.getMsg());
		} else {
			rc = funcLiqu.deleteColLiadModTmp();
			if (rc != 1) {
				alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
			} else {
				rc = fPaymainLiqu();
				if (rc != 1) {
					alertErr("執行[f_paymain_liqu] error");
				}
			}
		}
		// 若dw_data.item("liqu_status")有變更，新增一筆【COL_LIAD_REMOD 更生清算狀態異動檔】。20180321
		// 狀態為[新增]時,也要新增remod 2019.10.08 phopho
		if ((eqIgno(wp.itemStr("liqu_status_pink"), "pink")) || (eqIgno(wp.itemStr("aud_code"), "A"))) {
			rc = funcLiqu.insertColLiadRemod();
			if (rc != 1) {
				alertErr("新增 [col_liad_remod] error");
			}
		}
		addLiadLog1170();
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	public void saveFuncLiadModTmp() throws Exception {
		funcLiadModTmp = new Colp1180Func(wp);

		kkIdNo = wp.itemStr("id_no");
		kkCaseLetter = wp.itemStr("case_letter");
		kkType = "INST-MAST";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}

		funcLiadModTmp.varsSet("id_no", kkIdNo);
		funcLiadModTmp.varsSet("case_letter", kkCaseLetter);
		funcLiadModTmp.varsSet("aud_code", wp.itemStr("aud_code"));

		rc = funcLiadModTmp.dataProc();
		if (rc != 1) {
			alertErr2(funcLiadModTmp.getMsg());
		} else {
			rc = funcLiadModTmp.deleteColLiadModTmp();
			if (rc != 1) {
				alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
			}
		}
		addLiadLog1180();
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	public void saveFuncLiad570() throws Exception {
		funcLiad570 = new Colp1190Func(wp);

		kkIdNo = wp.itemStr("id_no");
		kkCaseLetter = wp.itemStr("case_letter");
		kkType = "COLL-DATA";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}

		funcLiad570.varsSet("id_no", kkIdNo);
		funcLiad570.varsSet("case_letter", kkCaseLetter);
		funcLiad570.varsSet("mod_seqno", wp.itemStr("mod_seqno"));
		funcLiad570.varsSet("pay_normal_flag_2", wp.itemStr("pay_normal_flag_2"));
		funcLiad570.varsSet("debt_amt2", wp.itemStr("debt_amt2"));
		funcLiad570.varsSet("alloc_debt_amt", wp.itemStr("alloc_debt_amt"));
		funcLiad570.varsSet("unalloc_debt_amt", wp.itemStr("unalloc_debt_amt"));
		funcLiad570.varsSet("coll_remark", wp.itemStr("coll_remark"));
		funcLiad570.varsSet("send_flag_571", wp.itemStr("send_flag_571"));
		funcLiad570.varsSet("crt_date", wp.itemStr("crt_date"));
		funcLiad570.varsSet("crt_user", wp.itemStr("crt_user"));
		// insert col_liad_57x_log
		funcLiad570.varsSet("id_p_seqno", wp.itemStr("id_p_seqno"));
		funcLiad570.varsSet("coll_apply_date", wp.itemStr("coll_apply_date"));
		funcLiad570.varsSet("apply_bank_no", wp.itemStr("apply_bank_no"));
		funcLiad570.varsSet("send_cnt", wp.itemStr("send_cnt"));
		funcLiad570.varsSet("idno_name", wp.itemStr("idno_name"));
		funcLiad570.varsSet("bank_name", wp.itemStr("bank_name"));

		rc = funcLiad570.dataProc();
		if (rc != 1) {
			alertErr2(funcLiad570.getMsg());
		}
		rc = funcLiad570.deleteColLiadLiad570Tmp();
		if (rc != 1) {
			alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
		}
		if (wp.itemStr("send_flag_571").equals("Y"))
			rc = funcLiad570.insertFunc();
		if (rc != 1) {
			alertErr("insert COL_LIAD_57X_LOG[571] error");
		}
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	public void saveFuncLiad570Close() throws Exception {
		funcLiad570Close = new Colp1192Func(wp);

		kkIdNo = wp.itemStr("id_no");
		kkCaseLetter = wp.itemStr("case_letter");
		kkType = "COLL-CLOSE";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}
		funcLiad570Close.varsSet("id_no", kkIdNo);
		funcLiad570Close.varsSet("case_letter", kkCaseLetter);
		funcLiad570Close.varsSet("mod_seqno", wp.itemStr("mod_seqno"));
		funcLiad570Close.varsSet("close_reason", wp.itemStr("close_reason"));
		funcLiad570Close.varsSet("close_remark", wp.itemStr("close_remark"));
		funcLiad570Close.varsSet("close_date", wp.itemStr("close_date"));
		funcLiad570Close.varsSet("close_user", wp.itemStr("close_user"));
		// insert col_liad_57x_log
		funcLiad570Close.varsSet("id_p_seqno", wp.itemStr("id_p_seqno"));
		funcLiad570Close.varsSet("coll_apply_date", wp.itemStr("coll_apply_date"));
		funcLiad570Close.varsSet("close_send_cnt", wp.itemStr("close_send_cnt"));
		funcLiad570Close.varsSet("idno_name", wp.itemStr("idno_name"));
		funcLiad570Close.varsSet("bank_name", wp.itemStr("bank_name"));

		rc = funcLiad570Close.dataProc();
		if (rc != 1) {
			alertErr2(funcLiad570Close.getMsg());
		}
		rc = funcLiad570Close.deleteColLiadLiad570Tmp();
		if (rc != 1) {
			alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
		}
		rc = funcLiad570Close.insertFunc();
		if (rc != 1) {
			alertErr("insert COL_LIAD_57X_LOG[574] error");
		}
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	public void saveFuncRenewCourt() throws Exception {
		funcRenewCourt = new Colp1196Func(wp);

		kkLiadDocNo = wp.itemStr("liad_doc_no");
		kkLiadDocSeqno = wp.itemStr("liad_doc_seqno");
		kkType = "RENEWCOURT";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}

		funcRenewCourt.varsSet("liad_doc_no", kkLiadDocNo);
		funcRenewCourt.varsSet("liad_doc_seqno", kkLiadDocSeqno);
		funcRenewCourt.varsSet("mod_seqno", wp.itemStr("mod_seqno"));
		funcRenewCourt.varsSet("aud_code", wp.itemStr("aud_code"));
		funcRenewCourt.varsSet("unit_doc_no", wp.itemStr("unit_doc_no"));
		funcRenewCourt.varsSet("recv_date", wp.itemStr("recv_date"));
		funcRenewCourt.varsSet("key_note", wp.itemStr("key_note"));
		funcRenewCourt.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
		funcRenewCourt.varsSet("case_date", wp.itemStr("case_date"));

		rc = funcRenewCourt.dataProc();
		if (rc != 1) {
			alertErr2(funcRenewCourt.getMsg());
		} else {
			rc = funcRenewCourt.deleteColLiadModTmp();
			if (rc != 1) {
				alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
			} else {
				rc = funcRenewCourt.updateColLiadRenewCaseDate();
				if (rc != 1) {
					alertErr("更新 更生公文[col_liad_renew]發文日期 error");
				}
			}
		}
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	public void saveFuncLiquCourt() throws Exception {
		funcLiquCourt = new Colp1197Func(wp);

		kkLiadDocNo = wp.itemStr("liad_doc_no");
		kkLiadDocSeqno = wp.itemStr("liad_doc_seqno");
		kkType = "LIQUICOURT";
		if (ofValidation() != 1) {
			wp.colSet("review_disabled", "disabled");
			return;
		}

		funcLiquCourt.varsSet("liad_doc_no", kkLiadDocNo);
		funcLiquCourt.varsSet("liad_doc_seqno", kkLiadDocSeqno);
		funcLiquCourt.varsSet("mod_seqno", wp.itemStr("mod_seqno"));
		funcLiquCourt.varsSet("aud_code", wp.itemStr("aud_code"));
		funcLiquCourt.varsSet("unit_doc_no", wp.itemStr("unit_doc_no"));
		funcLiquCourt.varsSet("recv_date", wp.itemStr("recv_date"));
		funcLiquCourt.varsSet("key_note", wp.itemStr("key_note"));
		funcLiquCourt.varsSet("case_letter_desc", wp.itemStr("case_letter_desc"));
		funcLiquCourt.varsSet("case_date", wp.itemStr("case_date"));

		rc = funcLiquCourt.dataProc();
		if (rc != 1) {
			alertErr2(funcLiquCourt.getMsg());
		} else {
			rc = funcLiquCourt.deleteColLiadModTmp();
			if (rc != 1) {
				alertErr("刪除 暫存資料[col_liad_mod_tmp] error");
			} else {
				rc = funcLiquCourt.updateColLiadLiquidateCaseDate();
				if (rc != 1) {
					alertErr("更新 清算公文[col_liad_liquidate]發文日期 error");
				}
			}
		}
		this.sqlCommit(rc);

		// DO NEXT
		wp.colSet("review_disabled", "disabled");
		wp.colSet("is_review", intToStr(rc));
		setbtnNext();
	}

	int ofValidation() throws Exception {
//		if(kk_type.equals("COLL-DATA")||kk_type.equals("COLL-CLOSE")||kk_type.equals("INST-MAST")){
//			String sql = "select count(*) as ll_cnt from col_liad_mod_tmp " + "where data_type = '"+kk_type+"' "
//					+ "and data_key = rpad ('" + kk_id_no + "', 10, ' ') || rpad ('" + kk_case_letter + "', 10, ' ') ";
//			sqlSelect(sql);
//			if(this.to_Num(sql_ss("ll_cnt"))<=0){
//				alert_err("查詢無異動資料, ID:" + kk_id_no+"; 文號="+kk_case_letter);
//				return -1;
//			}
//			return 1;
//		}

		if (kkType.equals("COLL-DATA") || kkType.equals("COLL-CLOSE") || kkType.equals("INST-MAST")) {
			String sql = "select count(*) as ll_cnt from col_liad_mod_tmp where data_type = :data_type "
					+ "and data_key = rpad ( :data_key1 , 10, ' ') || rpad ( :data_key2 , 10, ' ') ";
			setString("data_type", kkType);
			setString("data_key1", kkIdNo);
			setString("data_key2", kkCaseLetter);
			sqlSelect(sql);
			if (this.toNum(sqlStr("ll_cnt")) <= 0) {
				alertErr("查詢無異動資料, ID:" + kkIdNo + "; 文號=" + kkCaseLetter);
				return -1;
			}
			return 1;
		}

		if (kkType.equals("RENEWCOURT") || kkType.equals("LIQUICOURT")) {
			String sql = "select mod_data from col_liad_mod_tmp "
					+ "where data_type = :data_type and data_key = :data_key ";
			setString("data_type", kkType);
			setString("data_key", kkLiadDocNo + kkLiadDocSeqno);
			sqlSelect(sql);
			if (sqlRowNum <= 0) {
				alertErr("查詢無異動資料, 文件編號:" + kkLiadDocNo + "; 序號:" + kkLiadDocSeqno);
				return -1;
			}
			return 1;
		}

		String sql = "select data_key from col_liad_mod_tmp where data_type = :data_type and data_key = :data_key ";
		setString("data_type", kkType);
		setString("data_key", kkLiadDocNo);
		sqlSelect(sql);
		if (sqlRowNum <= 0) {
			alertErr("查詢無異動資料, 文件編號:" + kkLiadDocNo);
			return -1;
		}
		return 1;
	}

	int fPaymainRenew() throws Exception {
		long llCnt = 0, llMain = 0;
		String lsStatus = "", lsConfDate = "";

		if (empty(kkIdPSeqno) || empty(kkCaseLetter))
			return 0;

		String sql = "select count(*) as ll_cnt from col_liad_paymain where liad_type = '1' "
				+ "and holder_id_p_seqno = '" + kkIdPSeqno + "' and case_letter = '" + kkCaseLetter + "' ";
		sqlSelect(sql);
		llMain = (long) sqlNum("ll_cnt");

		sql = "select count(*) as ll_cnt from col_liad_renew " + "where id_p_seqno = '" + kkIdPSeqno
				+ "' and case_letter = '" + kkCaseLetter + "' ";
		sqlSelect(sql);
		llCnt = (long) sqlNum("ll_cnt");

		if (llCnt == 0 && llMain == 0)
			return 1;

		// Delete col_liad_paymain
		if (llCnt == 0 && llMain > 0) {
			sql = "delete col_liad_paymain " + "where liad_type = '1' " + "and holder_id_p_seqno = '" + kkIdPSeqno
					+ "' and case_letter = '" + kkCaseLetter + "' ";
			sqlExec(sql);
			return 1;
		}

		isRecvDate = "";
		lsStatus = fLastRenewStatus();
		if ((llMain > 0) || (!lsStatus.equals("3")))
			return 1;

		sql = "select nvl(confirm_date,' ') as confirm_date from col_liad_renew " + "where id_p_seqno = '" + kkIdPSeqno
				+ "' and case_letter = '" + kkCaseLetter + "' " + "and recv_date = '" + isRecvDate
				+ "' and renew_status ='3' " + "fetch first 1 row only ";
		sqlSelect(sql);
		if (sqlRowNum > 0) {
			lsConfDate = sqlStr("confirm_date");
		}
		if (lsConfDate.trim().equals(""))
			return 1;

		// Insert col_liad_paymain
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_liad_paymain");
		sp.ppstr("holder_id_p_seqno", kkIdPSeqno);
		sp.ppstr("holder_id", kkIdNo);
		sp.ppstr("case_letter", kkCaseLetter);
		sp.ppstr("liad_type", "1");
		sp.ppstr("recv_date", isRecvDate);
		sp.ppstr("payment_date_s", lsConfDate);
		// add by phopho 20191008
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ", ", sysdate ");
		//
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			alertErr("insert col_liad_paymain error");
			return -1;
		}
		return 1;
	}

	String fLastRenewStatus() throws Exception { // --最近更生進度
		String sql = "", lsRenewStatus = "";

		if (empty(kkIdPSeqno))
			return "";

		if (empty(isRecvDate)) {
			sql = "select max(recv_date) as recv_date from col_liad_renew " + "where id_p_seqno = '" + kkIdPSeqno
					+ "' ";
			if (!empty(kkCaseLetter))
				sql += "and case_letter = '" + kkCaseLetter + "' ";
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				isRecvDate = sqlStr("recv_date");
			} else {
				alertErr("無法取得最近收文日期");
				return "";
			}
		}

		sql = "select renew_status from col_liad_renew " + "where id_p_seqno = '" + kkIdPSeqno + "' ";
		if (!empty(kkCaseLetter))
			sql += "and case_letter = '" + kkCaseLetter + "' ";
		sql += "and recv_date = '" + isRecvDate + "' and renew_status is not null " + "fetch first 1 row only ";
		sqlSelect(sql);
		if (sqlRowNum > 0) {
			lsRenewStatus = sqlStr("renew_status");
		} else {
			return "";
		}

		return lsRenewStatus;
	}

	int fPaymainLiqu() throws Exception { // --update 還款主檔 for 清算
		long llCnt = 0, llMain = 0;
		String lsStatus = "";

		if (empty(kkIdPSeqno) || empty(kkCaseLetter))
			return 0;

		String sql = "select count(*) as ll_cnt from col_liad_paymain where liad_type = '2' "
				+ "and holder_id_p_seqno = '" + kkIdPSeqno + "' and case_letter = '" + kkCaseLetter + "' ";
		sqlSelect(sql);
		llMain = (long) sqlNum("ll_cnt");

		sql = "select count(*) as ll_cnt from col_liad_liquidate " + "where id_p_seqno = '" + kkIdPSeqno
				+ "' and case_letter = '" + kkCaseLetter + "' ";
		sqlSelect(sql);
		llCnt = (long) sqlNum("ll_cnt");

		if (llCnt == 0 && llMain == 0)
			return 1;

		// Delete col_liad_paymain
		if (llCnt == 0 && llMain > 0) {
			sql = "delete col_liad_paymain " + "where liad_type = '2' " + "and holder_id_p_seqno = '" + kkIdPSeqno
					+ "' and case_letter = '" + kkCaseLetter + "' ";
			sqlExec(sql);
			return 1;
		}

		lsStatus = fLastLiquStatus();
		if (",1,2,6,7".indexOf(lsStatus) == 0)
			return 1;

		// -清算終止-
		if (llMain > 0) {
			if (",2,6,7".indexOf(lsStatus) > 0) {
				busi.SqlPrepare sp = new SqlPrepare();
				sp.sql2Update("col_liad_paymain");
				sp.ppstr("payment_date_e", isRecvDate);
				sp.sql2Where(" where holder_id_p_seqno = ?", kkIdPSeqno);
				sp.sql2Where(" and case_letter = ?", kkCaseLetter);
				sp.sql2Where(" and liad_type = '2'", "");
				sqlExec(sp.sqlStmt(), sp.sqlParm());
				if (sqlRowNum == 0) {
					return -1;
				}
			}
		}

		if (!lsStatus.equals("1"))
			return 1;
		if (fLiquAllocAmt() != 1)
			return 0;
		if (imAllocAmt <= 0)
			return 1;

		// Insert col_liad_paymain
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_liad_paymain");
		sp.ppstr("holder_id_p_seqno", kkIdPSeqno);
		sp.ppstr("holder_id", kkIdNo);
		sp.ppstr("case_letter", kkCaseLetter);
		sp.ppstr("liad_type", "2");
		sp.ppstr("recv_date", isRecvDate);
		sp.ppstr("payment_date_s", isCaseDate);
		sp.ppstr("allocate_amt", numToStr(imAllocAmt, ""));
		// add by phopho 20191008
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ", ", sysdate ");
		//
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum == 0) {
			alertErr("insert col_liad_paymain.清算 error");
			return -1;
		}
		return 1;
	}

	String fLastLiquStatus() throws Exception { // --最近清算進度
		String sql = "", lsLiquStatus = "";

		if (empty(kkIdPSeqno))
			return "";

		if (empty(isRecvDate)) {
			sql = "select max(recv_date) as recv_date from col_liad_liquidate " + "where id_p_seqno = '" + kkIdPSeqno
					+ "' ";
			if (!empty(kkCaseLetter))
				sql += "and case_letter = '" + kkCaseLetter + "' ";
			sqlSelect(sql);
			if (sqlRowNum > 0) {
				isRecvDate = sqlStr("recv_date");
			} else {
				alertErr("無法取得最近收文日期");
				return "";
			}
		}

		sql = "select liqu_status from col_liad_liquidate " + "where id_p_seqno = '" + kkIdPSeqno + "' ";
		if (!empty(kkCaseLetter))
			sql += "and case_letter = '" + kkCaseLetter + "' ";
		sql += "and recv_date = '" + isRecvDate + "' " + "fetch first 1 row only ";
		sqlSelect(sql);
		if (sqlRowNum > 0) {
			lsLiquStatus = sqlStr("liqu_status");
		} else {
			return "";
		}

		return lsLiquStatus;
	}

	int fLiquAllocAmt() throws Exception {
		isCaseDate = "";
		imAllocAmt = 0;
		if (empty(kkIdPSeqno) || empty(kkCaseLetter))
			return 0;

		String sql = "select case_date, allocate_amt from col_liad_liquidate " + "where id_p_seqno = '" + kkIdPSeqno
				+ "' and case_letter = '" + kkCaseLetter + "' " + "and liqu_status ='1' and nvl(allocate_amt,0) > 0 "
				+ "order by recv_date ";
		sqlSelect(sql);
		if (sqlRowNum > 0) {
			isCaseDate = sqlStr("case_date");
			imAllocAmt = sqlNum("allocate_amt");
		} else {
			return 0;
		}
		return 1;
	}

	void setbtnNext() throws Exception {
		if (empty(kkString))
			wp.colSet("next_disabled", "disabled");
		wp.colSet("kk_string", kkString);
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {
			this.btnModeAud();
		}
	}

	void detailControl() {
		rowcntaa = 0;
		rowcntbb = 0;
		rowcntcc = 0;
		rowcntdd = 0;
		rowcntee = 0;
		rowcntff = 0;
		rowcntgg = 0;
		rowcnthh = 0;
		String[] aaLiadDocNo = wp.itemBuff("a-liad_doc_no");
		String[] bbLiadDocNo = wp.itemBuff("b-liad_doc_no");
		String[] ccLiadDocNo = wp.itemBuff("c-liad_doc_no");
		String[] ddIdNo = wp.itemBuff("d-db_id");
		String[] eeIdNo = wp.itemBuff("e-id_no");
		String[] ffIdNo = wp.itemBuff("f-id_no");
		String[] ggLiadDocNo = wp.itemBuff("g-liad_doc_no");
		String[] hhLiadDocNo = wp.itemBuff("h-liad_doc_no");
		if (!(aaLiadDocNo == null) && !empty(aaLiadDocNo[0]))
			rowcntaa = aaLiadDocNo.length;
		if (!(bbLiadDocNo == null) && !empty(bbLiadDocNo[0]))
			rowcntbb = bbLiadDocNo.length;
		if (!(ccLiadDocNo == null) && !empty(ccLiadDocNo[0]))
			rowcntcc = ccLiadDocNo.length;
		if (!(ddIdNo == null) && !empty(ddIdNo[0]))
			rowcntdd = ddIdNo.length;
		if (!(eeIdNo == null) && !empty(eeIdNo[0]))
			rowcntee = eeIdNo.length;
		if (!(ffIdNo == null) && !empty(ffIdNo[0]))
			rowcntff = ffIdNo.length;
		if (!(ggLiadDocNo == null) && !empty(ggLiadDocNo[0]))
			rowcntgg = ggLiadDocNo.length;
		if (!(hhLiadDocNo == null) && !empty(hhLiadDocNo[0]))
			rowcnthh = hhLiadDocNo.length;
		wp.listCount[0] = rowcntaa;
		wp.listCount[1] = rowcntbb;
		wp.listCount[2] = rowcntcc;
		wp.listCount[3] = rowcntdd;
		wp.listCount[4] = rowcntee;
		wp.listCount[5] = rowcntff;
		wp.listCount[6] = rowcntgg;
		wp.listCount[7] = rowcnthh;
	}

	void addLiadLog1160() {
		String audCode = wp.itemStr("aud_code");
		String renewStatus = wp.itemStr("renew_status");
		String renewStatusPink = wp.itemStr("renew_status_pink");
		String courtStatusPink = wp.itemStr("court_status_pink");

		if (audCode.equals("A")) {
			liadLogProcess1160();
		}
		if (audCode.equals("U")) {
			if (renewStatusPink.equals("pink") || courtStatusPink.equals("pink")) {
				liadLogProcess1160();
			} else {
				int i = 0;
				if (renewStatus.equals("4")) {
					if (wp.itemStr("deliver_date_pink").equals("pink")) {
						i++;
					}
				} else if (renewStatus.equals("6")) {
					if (wp.itemStr("judic_date_pink").equals("pink")) {
						i++;
					}
				} else if (wp.itemStr("court_id_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("case_year_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("court_dept_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("renew_int_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("renew_rate_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("org_debt_amt_bef_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("renew_lose_amt_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("judic_action_flag_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("action_date_s_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("judic_cancel_flag_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("cancel_date_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("run_renew_flag_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("super_name_pink").equals("pink")) {
					i++;
				}
				if (i > 0) {
					liadLogProcess1160();
				}
			}
		}
	}

	void addLiadLog1170() {
		String audCode = wp.itemStr("aud_code");
		String liquStatusPink = wp.itemStr("liqu_status_pink");
		String courtStatusPink = wp.itemStr("court_status_pink");

		if (audCode.equals("A")) {
			liadLogProcess1170();
		}
		if (audCode.equals("U")) {
			if (liquStatusPink.equals("pink") || courtStatusPink.equals("pink")) {
				liadLogProcess1170();
			} else {
				int i = 0;
				if (wp.itemStr("court_id_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("case_year_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("court_dept_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("case_letter_desc_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("judic_avoid_flag_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("liqu_lose_amt_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("judic_action_flag_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("action_date_s_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("judic_cancel_flag_pink").equals("pink")) {
					i++;
				} else if (wp.itemStr("cancel_date_pink").equals("pink")) {
					i++;
				}
				if (i > 0) {
					liadLogProcess1170();
				}
			}
		}
	}

	void addLiadLog1180() {
		String audCode = wp.itemStr("aud_code");
		if (audCode.equals("U")) {
			liadLogProcess1160();
		}
	}

	void liadLogProcess1160() {
		Colp1160Func funcRenew = new Colp1160Func(wp);
		int c = checkAddLiadLog1160();
		String sqlSelect = " select count(*) as unproccnt from col_liad_log " + " where doc_no = :liad_doc_no "
				+ " and liad_type = '1' " + " and event_type = 'S' " + " and proc_flag <> 'Y' ";
		setString("liad_doc_no", wp.itemStr("liad_doc_no"));
		sqlSelect(sqlSelect);
		int unproccnt = sqlInt("unproccnt");
		if (c == 1) {
			if (unproccnt > 0) {
				rc = funcRenew.deleteColLiadLog();
				if (rc != 1) {
					alertErr("報送失敗,delete_col_liad_log err");
					return;
				}
			}
			rc = funcRenew.insertColLiadLog();
			if (rc != 1) {
				alertErr("報送失敗,insert_col_liad_log err");
				return;
			}
		} else if (c == -1) {
			if (unproccnt > 0) {
				rc = funcRenew.deleteColLiadLog();
				if (rc != 1) {
					alertErr("不報送失敗,delete_col_liad_log err");
					return;
				}
			}
		}
	}

	void liadLogProcess1170() {
		Colp1170Func funcLiqu = new Colp1170Func(wp);
		int c = checkAddLiadLog1170();
		if (c == 1) {
			funcLiqu.varsSet("liqu_status", wp.itemStr("liqu_status"));
		}
		if (c == 2) {
			funcLiqu.varsSet("liqu_status", "2");
		}
		String sqlSelect = " select count(*) as unproccnt from col_liad_log " + " where doc_no = :liad_doc_no "
				+ " and liad_type = '2' " + " and event_type = 'S' " + " and proc_flag <> 'Y' ";
		setString("liad_doc_no", wp.itemStr("liad_doc_no"));
		sqlSelect(sqlSelect);
		int unproccnt = sqlInt("unproccnt");
		if (c >= 1) {
			if (unproccnt > 0) {
				rc = funcLiqu.deleteColLiadLog();
				if (rc != 1) {
					alertErr("報送失敗,delete_col_liad_log err");
					return;
				}
			}
			rc = funcLiqu.insertColLiadLog();
			if (rc != 1) {
				alertErr("報送失敗,insert_col_liad_log err");
				return;
			}
		} else if (c == -1) {
			if (unproccnt > 0) {
				rc = funcLiqu.deleteColLiadLog();
				if (rc != 1) {
					alertErr("不報送失敗,delete_col_liad_log err");
					return;
				}
			}
		}
	}

	int checkAddLiadLog1160() {
		String renewStatus = wp.itemStr("renew_status");// 更生進度
		String courtStatus = wp.itemStr("court_status");// 法院進度
		if (renewStatus.equals("1") && (courtStatus.equals("1") || courtStatus.equals("12"))) {
			return 1;
		}
		if (renewStatus.equals("2") && courtStatus.equals("2")) {
			return 1;
		}
		if (renewStatus.equals("3") && (courtStatus.equals("3") || courtStatus.equals("13"))) {
			return 1;
		}
		if (renewStatus.equals("4") && courtStatus.equals("6")) {
			return 1;
		}
		if (renewStatus.equals("5") && courtStatus.equals("14")) {
			return 1;
		}
		if (renewStatus.equals("6") && courtStatus.equals("4")) {
			return 1;
		}
		if (renewStatus.equals("7")) {
			return -1;
		}
		return 0;
	}

	int checkAddLiadLog1170() {
		String liquStatus = wp.itemStr("liqu_status");// 清算進度
		String courtStatus = wp.itemStr("court_status");// 法院進度
		if (liquStatus.equals("1") && courtStatus.equals("1")) {
			return 1;
		}
		if (liquStatus.equals("2") && courtStatus.equals("9")) {
			return 1;
		}
		if (liquStatus.equals("3") && courtStatus.equals("8")) {
			return 1;
		}
		if (liquStatus.equals("4") && (courtStatus.equals("6") || courtStatus.equals("13"))) {
			return 1;
		}
		if (liquStatus.equals("5") && courtStatus.equals("4")) {
			return 1;
		}
		if (liquStatus.equals("7") && courtStatus.equals("2")) {
			return 1;
		}
		if (liquStatus.equals("9") && (courtStatus.equals("3") || courtStatus.equals("12"))) {
			return 2;
		}
		if (liquStatus.equals("10") && (courtStatus.equals("6") || courtStatus.equals("13"))) {
			return 2;
		}
		if (liquStatus.equals("6") || liquStatus.equals("8")) {
			return -1;
		}
		return 0;
	}

}
