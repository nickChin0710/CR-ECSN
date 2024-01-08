package mktm01;

/** 
 * mktm0220 (聯名機構推卡獎勵參數維護)
 * 110-07-07  suzuwei     init
 */
import busi.FuncAction;

public class Mktm0220Func extends FuncAction {
	String projCode = "";

	@Override
	public void dataCheck() {
		if (ibAdd) {
			projCode = wp.itemStr2("kk_proj_code");
		} else {
			projCode = wp.itemStr2("proj_code");
		}

		if (empty(projCode)) {
			errmsg("專案代碼 不可空白");
			return;
		}

		if (ibDelete)
			return;

		if (wp.itemEmpty("proj_desc")) {
			errmsg("專案別說明:不可空白");
			return;
		}

		// --首年認定
		if (wp.itemEq("debut_year_flag", "1") || wp.itemEq("debut_year_flag", "2")) {
			if (wp.itemEmpty("debut_sup_flag_0") && wp.itemEmpty("debut_sup_flag_1")) {
				errmsg("首年認定：正附卡不可皆為空白");
				return;
			}
		}
//		if (wp.itemEq("debut_year_flag", "1") && wp.itemEmpty("debut_month1")) {
//			errmsg("首年認定 新發卡：核卡前幾個月不可空白");
//			return;
//		}
//		if (wp.itemEq("debut_year_flag", "2") && wp.itemEmpty("debut_month2")) {
//			errmsg("首年認定 首辦卡：前一年度第幾個月不可空白");
//			return;
//		}

		// --消費金額本金類計算 目前為有勾選項目才計算，若全部空白則皆不納入計算 2019/07/25
//		String lsConsume = "";
//		lsConsume = wp.itemNvl("consume_bl", "N") + wp.itemNvl("consume_ca", "N") + wp.itemNvl("consume_it", "N")
//				+ wp.itemNvl("consume_ao", "N") + wp.itemNvl("consume_id", "N") + wp.itemNvl("consume_ot", "N");
//
//		if (wp.itemNe("consume_type", "0") && eqIgno(lsConsume, "NNNNNN")) {
//			errmsg("消費金額本金類：不可皆為空白");
//			return;
//		}

		// --當年消費門檻
//		if (wp.itemEq("curr_cond", "Y")) {
//			if (wp.itemNum("curr_amt") <= 0) {
//				errmsg("當年消費門檻：累積金額不可小於等於 0 ");
//				return;
//			}
//			if (wp.itemNum("curr_cnt") <= 0) {
//				errmsg("當年消費門檻：權益次數不可小於等於 0 ");
//				return;
//			}
//		}

		// --前一年度消費門檻
//		if (wp.itemEq("last_cond", "Y")) {
//			if (chkAmtCnt() == false)
//				return;
//		}

		// --刷卡金額每增加
//		if (wp.itemEq("cond_per", "Y")) {
//			if (wp.itemNum("per_amt") <= 0) {
//				errmsg("刷卡金額每增加金額不可小於等於 0 ");
//				return;
//			}
//
//			if (wp.itemNum("per_cnt") <= 0) {
//				errmsg("刷卡金額每增加享有權益不可小於等於 0 ");
//				return;
//			}
//		}

		// --核卡後X日刷團費或機票
//		if (wp.itemEq("air_cond", "Y")) {
//			if (wp.itemEmpty("air_sup_flag_0") && wp.itemEmpty("air_sup_flag_1")) {
//				errmsg("核卡後刷團費或機票：正附卡 不可皆為空白");
//				return;
//			}
//
//			if (wp.itemEmpty("air_day")) {
//				errmsg("核卡後刷團費或機票：幾日不可空白 ");
//				return;
//			}
//
//			if (wp.itemEmpty("air_amt_type")) {
//				errmsg("核卡後刷團費或機票：金額計算方式不可空白");
//				return;
//			}
//
//			if (wp.itemNum("air_amt") <= 0) {
//				errmsg("核卡後刷團費或機票：消費金額不可小於等於 0 ");
//				return;
//			}
//
//			if (wp.itemNum("air_cnt") <= 0) {
//				errmsg("核卡後刷團費或機票：享有權益次數不可小於等於 0 ");
//				return;
//			}
//
//		}

		// --請款比對使用條件 A
//		if (wp.itemEq("a_use_cond", "Y")) {
//			if (wp.itemEmpty("a_last_month")) {
//				errmsg("請款比對使用條件 A：近幾個月刷團費或機票不可空白");
//				return;
//			}
//
//			if (wp.itemEmpty("a_use_amt_type")) {
//				errmsg("請款比對使用條件 A：金額計算方式不可空白");
//				return;
//			}
//
//			if (wp.itemNum("a_use_amt") <= 0) {
//				errmsg("請款比對使用條件 A：金額不可小於等於 0 ");
//				return;
//			}
//		}

		// --請款比對使用條件 B
//		if (wp.itemEq("b_use_cond", "Y")) {
//			if (wp.itemEmpty("b_last_month")) {
//				errmsg("請款比對使用條件 B：近幾個月累積消費不可空白");
//				return;
//			}
//
//			if (wp.itemNum("b_use_amt") <= 0) {
//				errmsg("請款比對使用條件 B：累積消費金額不可小於等於 0 ");
//				return;
//			}
//
//			if (wp.itemEmpty("b_use_amt_type")) {
//				errmsg("請款比對使用條件 B：消費金額累積方式不可空白 ");
//				return;
//			}
//
//			String lsUseType = "";
//			lsUseType = wp.itemNvl("b_use_bl", "N") + wp.itemNvl("b_use_ca", "N") + wp.itemNvl("b_use_it", "N")
//					+ wp.itemNvl("b_use_ao", "N") + wp.itemNvl("b_use_id", "N") + wp.itemNvl("b_use_ot", "N");
//
//			if (eqIgno(lsUseType, "NNNNNN")) {
//				errmsg("請款比對使用條件 B：消費金額本金類不可空白 ");
//				return;
//			}
//		}

		// --請款比對使用條件 D
//		if (wp.itemEq("d_use_cond", "Y")) {
//			if (wp.itemEmpty("d_use_right")) {
//				errmsg("當年度權益需二選一");
//				return;
//			}
//		}

		if (ibAdd)
			return;

		// --因主檔新增後才可新增明細故在修改時檢核明細相關條件

		// if(wp.item_eq("acct_type_flag","Y")){
		// if(checkDetl("01")==false){
		// errmsg("明細：01.帳戶類別 不可全部空白 ");
		// return ;
		// }
		// }

		// if(wp.item_eq("group_card_flag","Y")){
		// if(checkDetl("02")==false){
		// errmsg("明細：02.指定團代+卡種 不可全部空白 ");
		// return ;
		// }
		// }

		// if(wp.item_eq("debut_year_flag", "1")){
		// if(checkDetl("03")==false){
		// errmsg("明細：03.團體代號 不可全部空白 ");
		// return ;
		// }
		// }

		// if(wp.item_eq("debut_year_flag", "2")){
		// if(checkDetl("04")==false){
		// errmsg("明細：04.團體代號 不可全部空白 ");
		// return ;
		// }
		// }

		// if(wp.item_eq("cond_per", "Y")){
		// if(checkDetl("05")==false){
		// errmsg("明細：05.團體代號 不可全部空白 ");
		// return ;
		// }
		// }

		// if(wp.item_eq("air_cond", "Y")){
		// if(checkDetl("06")==false){
		// errmsg("明細：06.團體代號 不可全部空白 ");
		// return ;
		// }
		//
		// if(checkDetl("07")==false){
		// errmsg("明細：07.MCC CODE 不可全部空白 ");
		// return ;
		// }
		// }

		// if(wp.item_eq("a_use_cond", "Y") && wp.item_eq("a_mcc_code", "Y")){
		// if(checkDetl("08")==false){
		// errmsg("明細：08.MCC CODE 不可全部空白 ");
		// return ;
		// }
		// }

		// if(wp.item_eq("d_use_cond", "Y")){
		// if(checkDetl("09")==false){
		// errmsg("明細：09.MCC CODE 不可全部空白 ");
		// return ;
		// }
		// }

	}

//	boolean chkAmtCnt() {
//		int rr = 1;
//		double lmAmt = 0;
//		for (int ii = 6; ii > 0; ii--) {
//			// if (wp.item_num("last_amt"+ii)==0 && wp.item_num("last_cnt"+ii)==0) continue;
//			if (lmAmt == 0 && wp.itemNum("last_amt" + ii) == 0)
//				continue;
//
//			if (wp.itemNum("last_amt" + ii) <= 0) {
//				errmsg("前一年消費門檻 (" + ii + ") 金額 不可為0 !");
//				return false;
//			}
//			if (wp.itemNum("last_cnt" + ii) == 0) {
//				errmsg("前一年消費門檻 (" + ii + ") 次數需 >0 !");
//				return false;
//			}
//
//
//			if (lmAmt > 0 && wp.itemNum("last_amt" + ii) >= lmAmt) {
//				errmsg("前一年消費門檻 (" + ii + ") 金額, 輸入錯誤");
//				return false;
//			}
//
//			// --金額若有輸入 次數需大於 0 , 次數若有輸入 金額需大於 0
//			if (wp.itemNum("last_amt" + ii) > 0) {
//				if (wp.itemNum("last_cnt" + ii) <= 0) {
//					errmsg("前一年消費門檻 (" + ii + ") 次數需大於 0 !");
//					return false;
//				}
//			}
//			if (wp.itemNum("last_cnt" + ii) > 0) {
//				if (wp.itemNum("last_amt" + ii) <= 0) {
//					errmsg("前一年消費門檻 (" + ii + ") 金額需大於 0 !");
//					return false;
//				}
//			}
//
//			lmAmt = wp.itemNum("last_amt" + ii);
//		}
//
//		if (lmAmt == 0)
//			return false;
//
//		return true;
//	}

	boolean checkDetl() {
		String lsType = wp.itemStr2("data_type");
		String lsCode = wp.itemStr2("ex_data_code");

		if (eq(lsType, "01")) {
			if (empty(lsCode)) {
				errmsg("帳戶類別: 不可空白");
				return false;
			}
		} else if (eq(lsType, "02")) {
			if (empty(lsCode)) {
				errmsg("團代: 不可空白");
				return false;
			}
		} else if (eq(lsType, "03")) {
			if (empty(lsCode)) {
				errmsg("卡種代號: 不可空白");
				return false;
			}
		} else if (eq(lsType, "04")) {
			if (empty(lsCode)) {
				errmsg("特店類別: 不可空白");
				return false;
			}
		} else if (eq(lsType, "05")) {
			if (empty(lsCode)) {
				errmsg("特店代號: 不可空白");
				return false;
			}
		} else {
			errmsg("資料類別須為(01,02,03,04,05)");
			return false;
		}

		return true;
	}

	@Override
	public int dbInsert() {
		actionInit("A");

		dataCheck();
		if (rc != 1)
			return rc;

		insertJointlyParm();

		return rc;
	}

	int insertJointlyParm() {
		sql2Insert("mkt_jointly_parm");
		addsqlParm(",?", ", proj_code", projCode);
		addsqlParm(", apr_flag", ",'N'");
		addsqlParm(",?", ", proj_date_s", wp.itemStr2("proj_date_s"));
		addsqlParm(",?", ", proj_date_e", wp.itemStr2("proj_date_e"));
		addsqlParm(",?", ", proj_desc", wp.itemStr2("proj_desc"));
		addsqlParm(",?", ", eliminate_flag", wp.itemYn("eliminate_flag"));
		addsqlParm(",?", ", contract_date", wp.itemStr2("contract_date"));
		addsqlParm(",?", ", acct_type_flag", wp.itemStr2("acct_type_flag"));
		addsqlParm(",?", ", card_type_flag", wp.itemStr2("card_type_flag"));
		addsqlParm(",?", ", group_code_flag", wp.itemStr2("group_code_flag"));
		addsqlParm(",?", ", debut_sup_flag_0", wp.itemYn("debut_sup_flag_0"));
		addsqlParm(",?", ", debut_sup_flag_1", wp.itemYn("debut_sup_flag_1"));
		addsqlParm(",?", ", debut_year_flag", wp.itemStr2("debut_year_flag"));
		addsqlParm(",?", ", debut_month1", wp.itemNum("debut_month1"));
		addsqlParm(",?", ", consume_type", wp.itemStr2("consume_type"));
		addsqlParm(",?", ", consume_bl", wp.itemYn("consume_bl"));
		addsqlParm(",?", ", consume_ca", wp.itemYn("consume_ca"));
		addsqlParm(",?", ", consume_it", wp.itemYn("consume_it"));
		addsqlParm(",?", ", consume_ao", wp.itemYn("consume_ao"));
		addsqlParm(",?", ", consume_id", wp.itemYn("consume_id"));
		addsqlParm(",?", ", consume_ot", wp.itemYn("consume_ot"));
		addsqlParm(",?", ", curr_pre_day", wp.itemNum("curr_pre_day"));
		addsqlParm(",?", ", curr_amt", wp.itemNum("curr_amt"));
		addsqlParm(",?", ", curr_tot_cond", wp.itemYn("curr_tot_cond"));
		addsqlParm(",?", ", curr_tot_cnt", wp.itemNum("curr_tot_cnt"));
		addsqlParm(",?", ", online_cond", wp.itemYn("online_cond"));
		addsqlParm(",?", ", online_cnt", wp.itemNum("online_cnt"));
		addsqlParm(",?", ", feedback_type", wp.itemStr2("feedback_type"));
		addsqlParm(",?", ", feedback_rate", wp.itemNum("feedback_rate"));
		addsqlParm(",?", ", feedback_amt", wp.itemNum("feedback_amt"));
		addsqlParm(",?", ", mcc_flag", wp.itemStr2("mcc_flag"));
		addsqlParm(",?", ", mcht_flag", wp.itemStr2("mcht_flag"));
		addsqlParm(",?", ", process_flag", wp.itemStr2("process_flag"));
		addsqlParm(",?", ", any_day", wp.itemStr2("any_day"));
		addsqlParm(", crt_date", "," + commSqlStr.sysYYmd);
		addsqlParm(",?", ", crt_user", modUser);
//		addsqlParm(", apr_date", "," + commSqlStr.sysYYmd);
//		addsqlParm(",?", ", apr_user", modUser);
//		addsqlParm(",?", ", mod_user", modUser);
//		addsqlParm(",?", ", mod_time", wp.itemStr2("mod_time"));
		addsqlParm(",?", ", mod_pgm", modPgm);
		
		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert mkt_jointly_parm error ");
		}

		return rc;
	}

	public int insertDetl() {
		msgOK();
		if (checkDetl() == false) {
			return rc;
		}

		strSql = "select 1 from mkt_jointly_parm_detl where proj_code=? "
				+ " and data_type =  ? and data_code = ? ";
		setString2(1, wp.itemStr2("proj_code"));
		setString(wp.itemStr2("data_type"));
		setString(wp.itemStr2("ex_data_code"));
		sqlQuery(strSql);
		if (sqlRowNum < 0) {
			sqlErr("update mkt_jointly_parm_detl error");
			return -1;
		} else if (sqlRowNum > 0) {
			errmsg("此筆資料已存在");
			return -1;
		}

		sql2Insert("mkt_jointly_parm_detl");
		addsqlParm(",?", ", proj_code", wp.itemStr2("proj_code"));
		addsqlParm(",?", ", data_type", wp.itemStr2("data_type"));
		addsqlParm(",?", ", data_code", wp.itemStr2("ex_data_code"));
		addsqlParm(", mod_time", ", sysdate");
		addsqlParm(",?", ", mod_pgm", modPgm);
		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert mkt_jointly_parm_detl error !");
		}

		return rc;
	}

	public int deleteDetl(int ll) {
		msgOK();

		String lsCode = wp.colStr(ll, "data_code");

		strSql = "delete mkt_jointly_parm_detl where proj_code=? "
				+ " and data_type =? and data_code=?";
		setString2(1, wp.itemStr2("proj_code"));
		setString(wp.itemStr2("data_type"));
		setString(lsCode);

		sqlExec(strSql);
		if (sqlRowNum < 0) {
			sqlErr("delete mkt_jointly_parm_detl");
		}

		return rc;
	}

	public int deleteAllDetl() {
		msgOK();

		strSql = "delete mkt_jointly_parm_detl where proj_code =?";

		setString2(1, wp.itemStr2("proj_code"));

		sqlExec(strSql);
		if (sqlRowNum < 0) {
			errmsg("delete mkt_jointly_parm_detl error !");
			return rc;
		} else
			rc = 1;

		return rc;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if (rc != 1)
			return rc;
		if (wp.itemEq("apr_flag", "Y")) {
			errmsg("已覆核不可修改");
			return rc;
		}

		sql2Update("mkt_jointly_parm");
		addsqlParm(", proj_date_s = ? ", wp.itemNvl("proj_date_s", ""));
		addsqlParm(", proj_date_e = ? ", wp.itemNvl("proj_date_e", ""));
		addsqlParm(", proj_desc = ? ", wp.itemStr2("proj_desc"));
		addsqlParm(", eliminate_flag = ? ", wp.itemYn("eliminate_flag"));
		addsqlParm(", contract_date = ? ", wp.itemNvl("contract_date", ""));
		addsqlParm(", acct_type_flag = ? ", wp.itemStr2("acct_type_flag"));
		addsqlParm(", card_type_flag = ? ", wp.itemStr2("card_type_flag"));
		addsqlParm(", group_code_flag = ? ", wp.itemStr2("group_code_flag"));
		addsqlParm(", debut_sup_flag_0 = ? ", wp.itemYn("debut_sup_flag_0"));
		addsqlParm(", debut_sup_flag_1 = ? ", wp.itemYn("debut_sup_flag_1"));
		addsqlParm(", debut_year_flag = ? ", wp.itemStr2("debut_year_flag"));
		addsqlParm(", debut_month1 = ? ", wp.itemNvl("debut_month1", ""));
		addsqlParm(", consume_type = ? ", wp.itemStr2("consume_type"));
		addsqlParm(", consume_bl = ? ", wp.itemYn("consume_bl"));
		addsqlParm(", consume_ca = ? ", wp.itemYn("consume_ca"));
		addsqlParm(", consume_it = ? ", wp.itemYn("consume_it"));
		addsqlParm(", consume_ao = ? ", wp.itemYn("consume_ao"));
		addsqlParm(", consume_id = ? ", wp.itemYn("consume_id"));
		addsqlParm(", consume_ot = ? ", wp.itemYn("consume_ot"));
		addsqlParm(", curr_pre_day = ? ", wp.itemNum("curr_pre_day"));
		addsqlParm(", curr_amt = ? ", wp.itemNum("curr_amt"));
		addsqlParm(", curr_tot_cond = ? ", wp.itemYn("curr_tot_cond"));
		addsqlParm(", curr_tot_cnt = ? ", wp.itemNum("curr_tot_cnt"));
		addsqlParm(", online_cond = ? ", wp.itemYn("online_cond"));
		addsqlParm(", online_cnt = ? ", wp.itemNum("online_cnt"));
		addsqlParm(", feedback_type = ? ", wp.itemStr2("feedback_type"));
		addsqlParm(", feedback_rate = ? ", wp.itemNum("feedback_rate"));
		addsqlParm(", feedback_amt = ? ", wp.itemNum("feedback_amt"));
		addsqlParm(", mcc_flag = ? ", wp.itemStr2("mcc_flag"));
		addsqlParm(", mcht_flag = ? ", wp.itemStr2("mcht_flag"));
		addsqlParm(", process_flag = ? ", wp.itemStr2("process_flag"));
		addsqlParm(", any_day = ? ", wp.itemStr2("any_day"));
//		addsqlParm(", crt_date", "," + commSqlStr.sysYYmd);
//		addsqlParm(", crt_user", modUser);
//		addsqlParm(", apr_date", "," + commSqlStr.sysYYmd);
//		addsqlParm(", apr_user", modUser);
		addsqlParm(", mod_user = ?", modUser);
		addsql2(", mod_time = sysdate");
		addsqlParm(", mod_pgm = ?", modPgm);
		addsqlParm(" where proj_code =?", projCode);
		addsql2(" and apr_flag ='N'");

		sqlExec(sqlStmt(), sqlParms());

		if (sqlRowNum <= 0) {
			errmsg("update mkt_jointly_parm error ");
		}

		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if (rc != 1)
			return rc;

		// -- 刪除主檔
		strSql = " delete mkt_jointly_parm where proj_code =? ";
//				+ "and apr_flag =? ";

		setString(projCode);
//		setString(wp.itemStr2("apr_flag"));

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg("delete mkt_jointly_parm error !");
			return rc;
		}

		// -- 刪除明細
		deleteAllDetl();

		return rc;
	}

	@Override
	public int dataProc() {
		msgOK();

//    copyRightParm();
		if (rc != 1)
			return rc;
//    copyParmDetl();
		if (rc != 1)
			return rc;

		return rc;
	}

	public int dataApprove() {
		msgOK();

		// --覆核主檔
		strSql = " update mkt_jointly_parm set " 
				+ " apr_flag = 'Y' ,  " 
				+ " apr_date = to_char(sysdate,'yyyymmdd') , "
				+ " apr_user =:apr_user " 
				+ " where proj_code =:proj_code ";

		setString("apr_user", wp.itemStr2("approval_user"));
		var2ParmStr("proj_code");

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg("approve mkt_jointly_parm error !");
			return rc;
		}

		return rc;
	}

}
