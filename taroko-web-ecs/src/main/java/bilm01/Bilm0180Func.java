/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-11  V1.00.00  yash       program initial                            *
* 108-07-01  V1.00.01  Amber      Update                                     *
* 108-08-26  V1.00.02  Amber      Update add頁面                             				     *
* 109-04-23  V1.00.03  shiyuqi       updated for project coding standard     * 
* 111-12-08  V1.00.04  Ryan       修改為需要線上覆核才可做更動                                                                    *  
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Bilm0180Func extends FuncEdit {
	// String m_kk_card_no = "";
	String kkCardNo = "", kkReserveType = "", kkStartDate = "", kkEndDate = "", kkBreakFlag = "", kkBreakDate = "";
	String kkMStartDate = "", kkMEndDate = "";

	public Bilm0180Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {

		kkCardNo = wp.colStr("card_no");
		kkReserveType = wp.colStr("reserve_type");
		kkStartDate = wp.colStr("start_date");
		kkEndDate = wp.colStr("end_date");
		kkBreakFlag = wp.colStr("break_flag");
		kkBreakDate = wp.colStr("break_date");
		kkMStartDate = wp.colStr("start_date");
		kkMEndDate = wp.colStr("end_date");

		if (empty(kkCardNo)) {
			kkCardNo = wp.itemStr("kk_card_no");
		}

		if (this.isAdd()) {

			if (!wp.itemStr("break_flag").equals("1")) {
				// 主檔檢查消費起迄日是否有重疊
				String lsSql2 = "select count(*) as tot_cnt from bil_assign_installment where card_no = ? and reserve_type = ? "
						+ "and ( (start_date between ? and ? ) or (end_date between ? and ?) )";
				Object[] param2 = new Object[] { kkCardNo, kkReserveType, kkStartDate, kkEndDate, kkMStartDate,
						kkMEndDate };
				sqlSelect(lsSql2, param2);
				if (sqlRowNum > 0) {
					if (colNum("tot_cnt") > 0) {
						errmsg("此預約類別-消費起迄日資料已存在，無法新增");
						return;
					}
				}

//        // 暫存檔檢查消費起迄日是否有重疊
//        String lsSql3 =
//            "select count(*) as tot_cnt from bil_assign_installment_t where card_no = ? and reserve_type = ? "
//                + "and ( (start_date between ? and ?) or (end_date between ? and ?) )";
//        Object[] param3 = new Object[] {kkCardNo, kkReserveType, kkStartDate, kkEndDate,
//            kkMStartDate, kkMEndDate};
//        sqlSelect(lsSql3, param3);
//        if (sqlRowNum > 0) {
//          if (colNum("tot_cnt") > 0) {
//            errmsg("此預約類別-消費起迄日資料已存在，無法新增");
//            return;
//          }
//        }
			}
			// apr_flag="Y" and break_date <>''
			String lsSql4 = "select count(*) as tot_cnt from bil_assign_installment where card_no = ? and reserve_type = ? "
					+ "and break_date <> '' ";
			Object[] param4 = new Object[] { kkCardNo, kkReserveType };
			sqlSelect(lsSql4, param4);

			if (sqlRowNum > 0) {
				if (colNum("tot_cnt") > 0) {
					errmsg("此筆終止資料已放行，無法修改");
					return;
				}
			}

			if (empty(kkCardNo)) {
				errmsg("卡號不可空白!");
			} else {
				String isSql = "  select current_code as ls_current_code, "
						+ "decode(assign_installment,'Y',decode(ptr_group_code.auto_installment,'Y','N','Y'),'N')as ls_assign_installment, "
						+ "card_no " + "from crd_card, ptr_group_code "
						+ "where decode(crd_card.group_code,'','0000',crd_card.group_code) = ptr_group_code.group_code "
						+ "and  card_no =? ";
				// String is_sql = " select card_no "
				// + "from crd_card "
				// + "where card_no =? ";
				Object[] param1 = new Object[] { kkCardNo };
				sqlSelect(isSql, param1);

				if (sqlRowNum <= 0) {
					// 此卡號與身分證號不符
					errmsg("此卡號與身分證號不符!");
					return;
				}

				if (!colStr("ls_current_code").equals("0")) {
					// 無效卡
					errmsg("無效卡!");
					return;
				}

				if (!colStr("ls_assign_installment").equals("Y")) {
					// 此團體代號不可指定
					errmsg("此團體代號不可指定!");
					return;
				}

			}

			// if((wp.col_ss("break_flag").equals(""))) {
			//
			// if(!empty(wp.col_ss("break_date"))) {
			// errmsg("終止日期應為空白");
			// return;
			// }
			//
			// }
			// if((wp.col_ss("break_flag").equals("1"))) {
			// if(empty(wp.col_ss("break_date"))) {
			// errmsg("請輸入終止日期");
			// return;
			// }
			//
			// }
			// if(empty(wp.col_ss("break_date"))) {
			// if(wp.col_ss("break_flag").equals("1")) {
			// errmsg("請輸入終止日期");
			// return;
			//
			// }
			// }
			// if(!empty(wp.col_ss("break_date"))) {
			// if(wp.item_num("break_date") <= commString.ss_2Num(get_sysDate())) {
			// errmsg("終止日期 不可小於系統日期");
			// return;
			// }
			// }
			// if(wp.item_ss("apr_flag").equals("Y.未異動") &&
			// !empty(wp.item_ss("break_date"))) {
			// errmsg("此筆終止資料已放行，無法修改");
			// return;
			// }
		} else {

			if (this.isUpdate()) {

				String kkRowid = wp.colStr("rowid");

				if (!wp.itemStr("break_flag").equals("1")) {

					// 主檔檢查消費起迄日是否有重疊
					String lsSql2 = "select count(*) as tot_cnt from bil_assign_installment where card_no = ? and reserve_type = ? "
							+ "and ( (start_date between ? and ?) or (end_date between ? and ?) ) "
							+ "and hex(rowid) <> ?";
					Object[] param2 = new Object[] { kkCardNo, kkReserveType, kkStartDate, kkEndDate, kkMStartDate,
							kkMEndDate, kkRowid };
					sqlSelect(lsSql2, param2);
					if (sqlRowNum > 0) {
						if (colNum("tot_cnt") > 0) {
							errmsg("此預約類別-消費起迄日資料已存在，無法修改");
							return;
						}
					}

//        // 暫存檔檢查消費起迄日是否有重疊
//        String lsSql3 =
//            "select count(*) as tot_cnt from bil_assign_installment_t where card_no = ? and reserve_type = ? "
//                + "and ( (start_date between ? and ?) or (end_date between ? and ?) ) "
//                + "and hex(rowid) <> ?";
//        Object[] param3 = new Object[] {kkCardNo, kkReserveType, kkStartDate, kkEndDate,
//            kkMStartDate, kkMEndDate, kkRowid};
//        sqlSelect(lsSql3, param3);
//        if (sqlRowNum > 0) {
//          if (colNum("tot_cnt") > 0) {
//            errmsg("此預約類別-消費起迄日資料已存在，無法修改");
//            return;
//          }
//        }
				}
				if (empty(kkCardNo)) {
					wp.colSet("ls_errmsg", "卡號不可空白!");
				} else {

					String isSql = "  select current_code as ls_current_code, "
							+ "decode(assign_installment,'Y',decode(ptr_group_code.auto_installment,'Y','N','Y'),'N')as ls_assign_installment, "
							+ "card_no " + "from crd_card, ptr_group_code "
							+ "where decode(crd_card.group_code,'','0000',crd_card.group_code) = ptr_group_code.group_code "
							+ "and  card_no =? ";
					// String is_sql = " select card_no "
					// + "from crd_card "
					// + "where card_no =? ";
					Object[] param1 = new Object[] { kkCardNo };
					sqlSelect(isSql, param1);
					if (sqlRowNum <= 0) {
						// 此卡號與身分證號不符
						errmsg("此卡號與身分證號不符!");
						return;
					}

					if (!colStr("ls_current_code").equals("0")) {
						// 無效卡
						errmsg("無效卡!");
						return;
					}
					if (!colStr("ls_assign_installment").equals("Y")) {
						// 此團體代號不可指定
						errmsg("此團體代號不可指定!");
						return;
					}
				}

				// if(empty(wp.col_ss("break_date"))) {
				//
				// if(!empty(wp.col_ss("break_flag"))) {
				// errmsg("請輸入終止日期");
				// return;
				// }
				// }
				// if(!empty(wp.col_ss("break_date"))) {
				//
				// if(wp.item_num("break_date") <= commString.ss_2Num(get_sysDate())) {
				// errmsg("終止日期 不可小於系統日期");
				// return;
				// }
				// }
				// //break_flag空=N,有值=Y
				// if(wp.col_ss("break_flag").equals("")) {
				// if(!empty(wp.col_ss("break_date"))) {
				// errmsg("終止日期應為空白");
				// return;
				// }
				//
				// }
				// if(!wp.col_ss("break_flag").equals("")) {
				// if(empty(wp.col_ss("break_date"))) {
				// errmsg("請輸入終止日期");
				// return;
				// }
				// }
				//
				// if(wp.item_ss("apr_flag").equals("Y.未異動") &&
				// !empty(wp.item_ss("break_date"))) {
				// errmsg("此筆終止資料已放行，無法修改");
				// return;
				// }

			}
			// -other modify-
			sqlWhere = " where hex(rowid) = ? " + " and nvl(mod_seqno,0) = ? ";
			Object[] param = new Object[] { wp.colStr("rowid"), wp.modSeqno() };
			isOtherModify("bil_assign_installment", sqlWhere, param);
		}

	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1) {
			return rc;
		}
		strSql = "insert into bil_assign_installment (" + "  card_no " + ", reserve_type " + ", start_date "
				+ ", end_date " + ", amt_from " + ", installment_term " + ", break_flag " + ", break_date "
				+ ", crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno ,apr_user ,apr_date"
				+ " ) values (" + " ?,?,?,?,?,?,?,? " + ", to_char(sysdate,'yyyymmdd'), ?"
				+ ", sysdate,?,?,1,?,to_char(sysdate,'yyyymmdd')" + " )";
		// -set ?value-

		Object[] param = new Object[] { wp.itemStr("kk_card_no"), wp.itemStr("reserve_type"), wp.itemStr("start_date"),
				wp.itemStr("end_date"), wp.itemStr("amt_from"), wp.itemStr("installment_term"),
				wp.itemStr("break_flag"), wp.itemStr("break_date"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
				wp.itemStr("approval_user") };

		rc = sqlExec(strSql, param);

		if (sqlRowNum <= 0) {
			errmsg(sqlErrtext);
		}

		return rc;
	}

	@Override
	public int dbUpdate() {
		// actionInit("U");
		// msgOK();
		actionInit("U");
		dataCheck();
		String dbRowid = wp.colStr("rowid");
		if (rc != 1) {
			return rc;
		}

		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("bil_assign_installment");// table名稱
		sp.ppstr("card_no", wp.itemStr("card_no"));
		sp.ppstr("start_date", wp.itemStr("start_date"));
		sp.ppstr("end_date", wp.itemStr("end_date"));
		sp.ppnum("amt_from", wp.itemNum("amt_from"));
		sp.ppnum("installment_term", wp.itemNum("installment_term"));
		sp.ppstr("break_flag", wp.itemStr("break_flag"));
		sp.ppstr("break_date", wp.itemStr("break_date"));
		sp.ppstr("reserve_type", wp.itemStr("reserve_type"));
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.ppstr("apr_user", wp.itemStr("approval_user"));
		sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
		sp.addsql(", mod_time = sysdate ");
		sp.addsql(", apr_date = to_char(sysdate,'yyyymmdd') ");
		sp.sql2Where(" where hex(rowid) =?", dbRowid);

		// is_sql = "update bil_assign_installment_t set "
		// + " start_date =? "
		// + " ,end_date =? "
		// + " ,amt_from =? "
		// + " ,installment_term =? "
		// + " ,break_flag =? "
		// + " ,break_date =? "
		// + " ,reserve_type =? "
		// + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
		// + " , mod_seqno =nvl(mod_seqno,0)+1 "
		// + " where mod_seqno =? and hex(rowid) = ?"
		// ;
		//
		// Object[] param = new Object[] {
		// vars_ss("aa_start_date")
		// ,vars_ss("aa_end_date")
		// ,vars_num("aa_amt")
		// ,vars_ss("aa_term")
		// ,vars_ss("aa_break_flag")
		// ,vars_ss("aa_break_date")
		// ,vars_ss("aa_reserve_type")
		// , wp.loginUser
		// , wp.item_ss("mod_pgm")
		// , vars_ss("aa_mod_seqno")
		// ,vars_ss("aa_rowid")
		// };
		// rc = sqlExec(is_sql, param);
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;

	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		String dbRowid = wp.colStr("rowid");
		if (rc != 1) {
			return rc;
		}

		strSql = "delete bil_assign_installment " + sqlWhere;
		Object[] param = new Object[] { dbRowid ,wp.modSeqno()};
		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
		// msgOK();
		//
		// is_sql = "delete bil_assign_installment_t where hex(rowid) = ? " ;
		//
		// Object[] param = new Object[] { vars_ss("aa_rowid") };
		// rc = sqlExec(is_sql, param);
		// if (sql_nrow <= 0) {
		// errmsg(this.sql_errtext);
		// }
		// return rc;
	}

	// public String chackCardno(String cardno ,String id_p_seqno) {
	// String is_sql = " select current_code as
	// ls_current_code,decode(assign_installment,'Y',decode(ptr_group_code.auto_installment,'Y','N','Y'),'N')as
	// ls_assign_installment ";
	// is_sql+= " from crd_card, ptr_group_code ";
	// is_sql+=" where decode(crd_card.group_code,'','0000',crd_card.group_code) =
	// ptr_group_code.group_code ";
	// is_sql+=" and card_no =? ";
	// is_sql+=" and major_id_p_seqno =? ";
	// Object[] param1 = new Object[] {cardno,id_p_seqno};
	// sqlSelect(is_sql,param1);
	// if (sql_nrow <= 0) {
	// // 此卡號與身分證號不符
	// return "0";
	// }
	//
	// if(!col_ss("ls_current_code").equals("0")){
	// // 無效卡
	// return "2";
	// }
	// if(!col_ss("ls_assign_installment").equals("Y")){
	// // 此團體代號不可指定
	// return "3";
	// }
	//
	// return "1";
	// }

}
