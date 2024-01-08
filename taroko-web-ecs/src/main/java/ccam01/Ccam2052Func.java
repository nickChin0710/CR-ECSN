package ccam01;

/**
 * 19-0611:    JH    p_seqno >>acno_p_xxx
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 111-12-21 V1.00.01  Alex             需求單要求不檢核起日要大於系統日 
 * */
import busi.FuncAction;

public class Ccam2052Func extends FuncAction {
	String cardAcctIdx = "", lsStartDate = "", lsEndDate = "", adjRiskType = "";
	int lmPayAmt = 0, lmPayAmt2 = 0, lmAmt = 0;
	private boolean aprModify;
	double cardAcctIdxDou = 0;

	@Override
	public void dataCheck() {
		cardAcctIdx = wp.itemStr("card_acct_idx");
		lsStartDate = wp.itemStr("adj_eff_start_date");
		lsEndDate = wp.itemStr("adj_eff_end_date");
		String lsOriStartDate = "";
		String lsOriEndDate = "";

		lsOriStartDate = wp.itemStr("ori_start_date");
		lsOriEndDate = wp.itemStr("ori_end_date");

		if (this.ibDelete)
			return;

		if (empty(lsStartDate) || empty(lsEndDate)) {
			errmsg("有效日期 : 不可空白");
			return;
		}

		if (this.chkStrend(lsStartDate, lsEndDate) == -1) {
			errmsg("有效日期  起迄 錯誤");
			return;
		}
		
		//--需求單要求不檢核起日要大於系統日		
//		if (this.ibAdd || !eqIgno(lsStartDate, lsOriStartDate) || !eqIgno(lsEndDate, lsOriEndDate)) {
//			if (lsStartDate.compareTo(this.getSysDate()) < 0 || lsEndDate.compareTo(this.getSysDate()) < 0) {
//				errmsg("有效日期 起迄 須大於等於 系統日期");
//				return;
//			}
//		}

		if (empty(wp.itemStr("adj_area"))) {
			errmsg("適用地區別 : 不可空白");
			return;
		}

		if (empty(wp.itemStr("adj_reason"))) {
			errmsg("調整原因 : 不可空白");
			return;
		}

		if (wp.colNum("tot_amt_month") <= 0) {
			errmsg("調整一般月限額至 不可 <=0");
			return;
		}

		if(wp.itemEq("acno_flag", "2") && wp.itemEq("adj_sms_flag", "Y")) {
			errmsg("公司總繳戶不可發送簡訊");
			return ;
		}
		
		if (checkParm() == false)
			return;

	}

	boolean checkParm() {

		int listCnt = wp.itemRows("risk_type");
		String[] listRisk = wp.itemBuff("risk_type");
		String[] listStartDate = wp.itemBuff("adj_date1");
		String[] listEndDate = wp.itemBuff("adj_date2");

		for (int ii = 0; ii < listCnt; ii++) {
			if (empty(adjRiskType))
				adjRiskType += listRisk[ii];
			else
				adjRiskType += "," + listRisk[ii];

			if (this.chkStrend(listStartDate[ii], listEndDate[ii]) == -1) {
				errmsg("風險分類:" + listRisk[ii] + " 有效日期  起迄 錯誤");
				return false;
			}

			if (commString.strToInt(listStartDate[ii]) >= commString.strToInt(lsStartDate) == false
					|| commString.strToInt(listStartDate[ii]) <= commString.strToInt(lsEndDate) == false) {
				errmsg("風險分類:" + listRisk[ii] + " 有效日期(起)需在臨調效期內");
				return false;
			}

			if (commString.strToInt(listEndDate[ii]) >= commString.strToInt(lsStartDate) == false
					|| commString.strToInt(listEndDate[ii]) <= commString.strToInt(lsEndDate) == false) {
				errmsg("風險分類:" + listRisk[ii] + " 有效日期(迄)需在臨調效期內");
				return false;
			}

		}

		return true;
	}

	@Override
	public int dbInsert() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbUpdate() {
		cardAcctIdxDou = wp.itemNum("card_acct_idx");

		dataCheck();
		if (rc != 1) {
			return rc;
		}

		deleteTempData();
		if (rc != 1)
			return rc;

		insertCcaCardAcctT();
		if (rc != 1)
			return rc;

		return rc;
	}

	public int insertAdjParmT(int ll) {
		strSql = "insert into cca_adj_parm_t (" + " card_acct_idx" + ", risk_type, card_note, risk_level"
				+ ", adj_month_amt, adj_month_cnt" + ", adj_day_amt, adj_day_cnt ," + " adj_eff_start_date ,"
				+ " adj_eff_end_date , spec_flag " + ", crt_date, crt_time, crt_user" + ", mod_time, mod_pgm"
				+ " ) values (" + " :cardAcctIdx, :risk_type, :card_note, :risk_level"
				+ ", :adj_month_amt, :adj_month_cnt" + ", :adj_day_amt, :adj_day_cnt "
				+ ", :adj_eff_start_date , :adj_eff_end_date , :spec_flag ," + commSqlStr.sysYYmd + ", " + commSqlStr.sysTime
				+ ", :crt_user" + ", " + commSqlStr.sysdate + ", :mod_pgm" + " )";

		setDouble2("cardAcctIdx", cardAcctIdxDou);
		var2ParmStr("risk_type");
		setString2("card_note", wp.colStr("card_note"));
		setString2("risk_level", wp.colStr("risk_level"));
		var2ParmNum("adj_month_amt");
		var2ParmNum("adj_month_cnt");
		var2ParmNum("adj_day_amt");
		var2ParmNum("adj_day_cnt");
		var2ParmStr("adj_eff_start_date");
		var2ParmStr("adj_eff_end_date");
		var2ParmStr("spec_flag");
		setString2("crt_user", modUser);
		setString2("mod_pgm", modPgm);

		sqlExec(strSql);
		if (sqlRowNum != 1) {
			return -1;
		}

		return 1;
	}

	void insertCcaCardAcctT() {

		strSql = " insert into cca_card_acct_t ( " + " card_acct_idx ," + " mod_type ," + " debit_flag ,"
				+ " adj_quota ," + " adj_eff_start_date ," + " adj_eff_end_date ," + " adj_reason ," + " adj_remark ,"
				+ " adj_area ," + " adj_date ," + " adj_time ," + " adj_user ," + " adj_inst_pct ," + " adj_memo ,"
				+ " tot_amt_month ," + " adj_risk_flag ," + " adj_sms_flag ," + " spec_status ," + " block_reason1 ,"
				+ " block_reason2 ," + " block_reason3 ," + " block_reason4 ," + " block_reason5 , adj_risk_type , notice_flag , "
				+ " mod_user, mod_time, mod_pgm, mod_seqno " + " ) values ( " + " :cardAcctIdx ," + " 'ADJ-LIMIT' ,"
				+ " 'N' ," + " 'Y' ," + " :adj_eff_start_date ," + " :adj_eff_end_date ," + " :adj_reason ,"
				+ " :adj_remark ," + " :adj_area ," + " to_char(sysdate,'yyyymmdd') ,"
				+ " to_char(sysdate,'hh24miss') ," + " :adj_user ," + " :adj_inst_pct ," + " :adj_memo ,"
				+ " :tot_amt_month ," + " :adj_risk_flag ," + " :adj_sms_flag ," + " :spec_status ,"
				+ " :block_reason1 ," + " :block_reason2 ," + " :block_reason3 ," + " :block_reason4 ,"
				+ " :block_reason5 , :adj_risk_type , :notice_flag " + commSqlStr.modxxxValue(modUser, modPgm) + " ) ";

		setDouble2("cardAcctIdx", cardAcctIdxDou);
		item2ParmStr("adj_eff_start_date");
		item2ParmStr("adj_eff_end_date");
		item2ParmStr("adj_reason");
		item2ParmStr("adj_remark");
		item2ParmStr("adj_area");
		setString2("adj_user", modUser);
		item2ParmNum("adj_inst_pct");
		item2ParmStr("adj_memo");
		item2ParmNum("tot_amt_month");
		item2ParmNvl("notice_flag", "N");		
		item2ParmNvl("adj_risk_flag", "N");
		item2ParmNvl("adj_sms_flag", "N");
		item2ParmStr("spec_status");
		item2ParmStr("block_reason1");
		item2ParmStr("block_reason2");
		item2ParmStr("block_reason3");
		item2ParmStr("block_reason4");
		item2ParmStr("block_reason5");
		setString("adj_risk_type",adjRiskType);
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("insert cca_card_acct_t error ");
		}
		return;
	}

	void dataCheckDelete() {
		cardAcctIdxDou = wp.itemNum("card_acct_idx");
		strSql = "select count(*) as xx_cnt from cca_card_acct_T" + " where card_acct_idx =?"
				+ " and mod_type='ADJ-LIMIT'";
		setDouble2(1, cardAcctIdxDou);
		sqlSelect(strSql);
		if (colNum("xx_cnt") <= 0) {
			errmsg("臨調資料已不存在, 無法刪除");
			return;
		}
	}

	@Override
	public int dbDelete() {
		dataCheckDelete();
		if (rc != 1)
			return rc;

		deleteTempData();
		return rc;
	}

	void deleteTempData() {
		strSql = "delete cca_card_acct_t where card_acct_idx =?" + " and mod_type='ADJ-LIMIT'";
		sqlExec(strSql, new Object[] { cardAcctIdxDou });
		if (sqlRowNum < 0) {
			errmsg("delete cca_card_acct_T error; " + sqlErrtext);
			return;
		}

		strSql = "delete cca_adj_parm_t where card_acct_idx =?";
		sqlExec(strSql, new Object[] { cardAcctIdxDou });
		if (sqlRowNum < 0) {
			errmsg("delete cca_adj_parm_T error; " + sqlErrtext);
			return;
		}
	}

	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

}
