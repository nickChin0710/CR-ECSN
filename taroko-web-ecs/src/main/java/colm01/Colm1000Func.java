/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-01-216  V1.00.01  Ryan       Initial                                  *
* 112-07-17   V1.00.02  Ryan       增加客戶類型                                                                                           *
* 112-07-18   V1.00.03  Ryan       增加檢查新增資料是否重複                                                                     *
* 112-11-30   V1.00.04  Ryan       協商狀態異動更新協商狀態最近維護日期                                            *
* 112-12-19   V1.00.05  Ryan      欄位調整帳務類別帳戶狀態協商時的MCODE                 
* 112-12-21   V1.00.06  Ryan       modify 首期繳款日,債權計算截止日,目前應繳期數(含簽約當月)(計算) 欄位 
***************************************************************************/
package colm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm1000Func extends FuncEdit {

	public Colm1000Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	String kkIdCorpNo = "";
	
	// ************************************************************************
	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	// ************************************************************************
	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 1;
	}

	// ************************************************************************
	@Override
	public void dataCheck() {
		this.msgOK();
		kkIdCorpNo = wp.itemStr("kk_id_corp_no");
		if (empty(kkIdCorpNo)) {
			kkIdCorpNo = wp.itemStr("id_corp_no");
		}
		if (!this.isDelete()) {
			if(wp.itemEmpty("kk_id_corp_type")) {
				errmsg("類型不得為空值");
				return;
			}
			
			if(wp.itemEq("kk_id_corp_type","2")) {
				if(!wp.itemEq("cpbdue_type","2")) {
					errmsg("類型為公司,目前協商方式只能選2.個別協商");
					return;
				}
			}
			
			if(wp.itemEq("kk_id_corp_type", "1"))
				if(chkIdNo(kkIdCorpNo)==1) return;
			
			if(wp.itemEq("kk_id_corp_type", "2"))
				if(chkCorpNo(kkIdCorpNo)==1) return;
			
			if(!wp.itemEq("ori_cpbdue_curr_type",wp.itemStr("cpbdue_curr_type"))
					||!wp.itemEq("ori_cpbdue_bank_type",wp.itemStr("cpbdue_bank_type"))
					||!wp.itemEq("ori_cpbdue_tcb_type",wp.itemStr("cpbdue_tcb_type"))
					||!wp.itemEq("ori_cpbdue_medi_type",wp.itemStr("cpbdue_medi_type"))) {
				wp.itemSet("cpbdue_lst_upt_dat_dte", wp.sysDate);
				wp.colSet("cpbdue_lst_upt_dat_dte", wp.sysDate);
			}
		}
		
		countOnlineComputing1();
		
        if (this.isAdd())
        {
            //檢查新增資料是否重複
            String lsSql = "select count(*) as tot_cnt from COL_CPBDUE where CPBDUE_ID_P_SEQNO = ? and CPBDUE_ACCT_TYPE = ? ";
            Object[] param = new Object[] { wp.itemStr("cpbdue_id_p_seqno"), wp.itemStr("cpbdue_acct_type")};
            sqlSelect(lsSql, param);
            if (colNum("tot_cnt") > 0)
            {
                errmsg("資料已存在不可新增,請以修改作業來維護資料");
            }
            return;
        }
	}

	// ************************************************************************
	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if(rc != 1) 
			return rc;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_cpbdue");
		sp.ppstr("id_corp_no", kkIdCorpNo);
		sp.ppstr("cpbdue_id_p_seqno", wp.itemStr("cpbdue_id_p_seqno"));
		sp.ppstr("cpbdue_acct_type", wp.itemStr("cpbdue_acct_type"));
		sp.ppstr("cpbdue_apply_acct_status", wp.itemStr("acct_status"));
		sp.ppstr("cpbdue_apply_mcode", wp.itemStr("int_rate_mcode"));
		sp.ppstr("cpbdue_owner_bank", wp.itemStr("cpbdue_owner_bank"));
		sp.ppstr("cpbdue_branch", wp.itemStr("cpbdue_branch"));
		sp.ppstr("cpbdue_bank_type", wp.itemEq("cpbdue_type", "1")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_bank_type"));
		sp.ppstr("cpbdue_tcb_type", wp.itemEq("cpbdue_type", "2")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_tcb_type"));
		sp.ppstr("cpbdue_medi_type", wp.itemEq("cpbdue_type", "3")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_medi_type"));
		sp.ppstr("cpbdue_type", wp.itemStr("cpbdue_type"));
		sp.ppstr("cpbdue_curr_type", wp.itemStr("cpbdue_curr_type"));
		sp.ppnum("cpbdue_total_amt", wp.itemNum("cpbdue_total_amt"));
		sp.ppstr("cpbdue_over_days", wp.itemStr("cpbdue_over_days"));
		sp.ppstr("cpbdue_brk_times", wp.itemStr("cpbdue_brk_times"));
		sp.ppstr("cpbdue_seqno", wp.itemStr("cpbdue_seqno"));
		sp.ppstr("cpbdue_begin_date", wp.itemStr("cpbdue_begin_date"));
		sp.ppnum("cpbdue_pay_month_dte", wp.itemNum("cpbdue_pay_month_dte"));
		sp.ppnum("cpbdue_amt", wp.itemNum("cpbdue_amt"));
		sp.ppstr("cpbdue_amt_exp_dte", wp.itemStr("cpbdue_amt_exp_dte"));
		sp.ppnum("cpbdue_rate", wp.itemNum("cpbdue_rate"));
		sp.ppnum("cpbdue_due_card_amt", wp.itemNum("cpbdue_due_card_amt"));
		sp.ppnum("cpbdue_period", wp.itemNum("cpbdue_period"));
		sp.ppnum("cpbdue_due_pc_amt", wp.itemNum("cpbdue_due_pc_amt"));
		sp.ppstr("cpbdue_memo_1", wp.itemStr("cpbdue_memo_1"));
		sp.ppstr("cpbdue_memo_2", wp.itemStr("cpbdue_memo_2"));
		sp.ppnum("cpbdue_pay_month_amt", wp.itemNum("cpbdue_pay_month_amt"));
		sp.ppstr("cpbdue_lst_pay_dte", wp.itemStr("cpbdue_lst_pay_dte"));
		sp.ppnum("cpbdue_month_dueamt", wp.itemNum("cpbdue_month_dueamt"));
		sp.ppnum("cpbdue_total_dueamt", wp.itemNum("cpbdue_total_dueamt"));	
		sp.ppnum("cpbdue_lst_payamt", wp.itemNum("cpbdue_lst_payamt"));
		sp.ppnum("cpbdue_total_payamt", wp.itemNum("cpbdue_total_payamt"));
//		sp.ppstr("cpbdue_lst_upd_dte", wp.itemStr("cpbdue_lst_upd_dte"));
		sp.ppstr("cpbdue_lst_upt_dat_dte", wp.itemStr("cpbdue_lst_upt_dat_dte"));
		sp.ppnum("cpbdue_remd_amt", wp.itemNum("cpbdue_remd_amt"));
		sp.ppstr("apr_user", wp.itemStr("approval_user"));
		sp.ppstr("apr_date", sysDate);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0)
			errmsg("新增 col_cpbdue 錯誤");
		else
			dbInsertHst();
		return rc;
	}

	// ************************************************************************
	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc != 1) 
			return rc;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_cpbdue");
		sp.ppstr("id_corp_no", kkIdCorpNo);
		sp.ppstr("cpbdue_id_p_seqno", wp.itemStr("cpbdue_id_p_seqno"));
		sp.ppstr("cpbdue_acct_type", wp.itemStr("cpbdue_acct_type"));
		sp.ppstr("cpbdue_apply_acct_status", wp.itemStr("acct_status"));
		sp.ppstr("cpbdue_apply_mcode", wp.itemStr("int_rate_mcode"));
		sp.ppstr("cpbdue_owner_bank", wp.itemStr("cpbdue_owner_bank"));
		sp.ppstr("cpbdue_branch", wp.itemStr("cpbdue_branch"));
		sp.ppstr("cpbdue_bank_type", wp.itemEq("cpbdue_type", "1")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_bank_type"));
		sp.ppstr("cpbdue_tcb_type", wp.itemEq("cpbdue_type", "2")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_tcb_type"));
		sp.ppstr("cpbdue_medi_type", wp.itemEq("cpbdue_type", "3")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_medi_type"));
		sp.ppstr("cpbdue_type", wp.itemStr("cpbdue_type"));
		sp.ppstr("cpbdue_curr_type", wp.itemStr("cpbdue_curr_type"));
		sp.ppnum("cpbdue_total_amt", wp.itemNum("cpbdue_total_amt"));
		sp.ppstr("cpbdue_over_days", wp.itemStr("cpbdue_over_days"));
		sp.ppstr("cpbdue_brk_times", wp.itemStr("cpbdue_brk_times"));
		sp.ppstr("cpbdue_seqno", wp.itemStr("cpbdue_seqno"));
		sp.ppstr("cpbdue_begin_date", wp.itemStr("cpbdue_begin_date"));
		sp.ppnum("cpbdue_pay_month_dte", wp.itemNum("cpbdue_pay_month_dte"));
		sp.ppnum("cpbdue_amt", wp.itemNum("cpbdue_amt"));
		sp.ppstr("cpbdue_amt_exp_dte", wp.itemStr("cpbdue_amt_exp_dte"));
		sp.ppnum("cpbdue_rate", wp.itemNum("cpbdue_rate"));
		sp.ppnum("cpbdue_due_card_amt", wp.itemNum("cpbdue_due_card_amt"));
		sp.ppnum("cpbdue_period", wp.itemNum("cpbdue_period"));
		sp.ppnum("cpbdue_due_pc_amt", wp.itemNum("cpbdue_due_pc_amt"));
		sp.ppstr("cpbdue_memo_1", wp.itemStr("cpbdue_memo_1"));
		sp.ppstr("cpbdue_memo_2", wp.itemStr("cpbdue_memo_2"));
		sp.ppnum("cpbdue_pay_month_amt", wp.itemNum("cpbdue_pay_month_amt"));
		sp.ppstr("cpbdue_lst_pay_dte", wp.itemStr("cpbdue_lst_pay_dte"));
		sp.ppnum("cpbdue_month_dueamt", wp.itemNum("cpbdue_month_dueamt"));
		sp.ppnum("cpbdue_total_dueamt", wp.itemNum("cpbdue_total_dueamt"));	
		sp.ppnum("cpbdue_lst_payamt", wp.itemNum("cpbdue_lst_payamt"));
		sp.ppnum("cpbdue_total_payamt", wp.itemNum("cpbdue_total_payamt"));
		//sp.ppstr("cpbdue_lst_upd_dte", wp.itemStr("cpbdue_lst_upd_dte"));
		sp.ppstr("cpbdue_lst_upt_dat_dte", wp.itemStr("cpbdue_lst_upt_dat_dte"));
		sp.ppnum("cpbdue_remd_amt", wp.itemNum("cpbdue_remd_amt"));
		sp.ppstr("apr_user", wp.itemStr("approval_user"));
		sp.ppstr("apr_date", sysDate);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time = sysdate ");
		sp.rowid2Where(wp.itemStr("rowid"));
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0)
			errmsg("修改 col_cpbdue 錯誤");
		else
			dbInsertHst();
		return rc;
	}
	
	public int chkIdNo(String kkIdCorpNo) {
		sqlSelect = "select id_p_seqno from crd_idno where id_no = :id_no";
		setString("id_no", kkIdCorpNo);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			errmsg("查無身份證號,無法新增");
			return 1;
		}
		String idPSeqno = colStr("id_p_seqno");
		
		sqlSelect = "select acct_status,status_change_date,acct_type from act_acno where acct_type='01' "
				+ " and id_p_seqno = :id_p_seqno ";
		setString("id_p_seqno", idPSeqno);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			errmsg("此身份證號不存在act_acno,無法新增" + kkIdCorpNo);
			return 1;
		}
		return 0;
	}
	
	public int chkCorpNo(String kkIdCorpNo) {
		String sqlSelect = "select corp_p_seqno from crd_corp where corp_no = :corp_no";
		setString("corp_no", kkIdCorpNo);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			errmsg("查無統一編號,無法新增");
			return 1;
		}
		String corpPSeqno = colStr("corp_p_seqno");
		
		sqlSelect = "select acct_status,status_change_date,acct_type from act_acno where acct_type='03' and acno_flag='2' "
				+ " and corp_p_seqno = :corp_p_seqno ";
		setString("corp_p_seqno", corpPSeqno);
		sqlSelect(sqlSelect);
		if (sqlRowNum <= 0) {
			errmsg("此統一編號不存在act_acno,無法新增" + kkIdCorpNo);
			return 1;
		}
		return 0;
	}

	// ************************************************************************
	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if(rc != 1) 
			return rc;
		strSql = "delete col_cpbdue where rowid = ? ";

		Object[] param = new Object[] { wp.itemRowId("rowid") };

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0)
			errmsg("刪除 col_cpbdue 錯誤");
		return rc;
	}
	// ************************************************************************
	
	public int dbInsertHst() {
		actionInit("A");
		if(rc != 1) 
			return rc;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_cpbdue_hst");
		sp.ppstr("id_corp_no", kkIdCorpNo);
		sp.ppstr("cpbdue_id_p_seqno", wp.itemStr("cpbdue_id_p_seqno"));
		sp.ppstr("cpbdue_acct_type", wp.itemStr("cpbdue_acct_type"));
		sp.ppstr("cpbdue_apply_acct_status", wp.itemStr("acct_status"));
		sp.ppstr("cpbdue_apply_mcode", wp.itemStr("int_rate_mcode"));
		sp.ppstr("cpbdue_owner_bank", wp.itemStr("cpbdue_owner_bank"));
		sp.ppstr("cpbdue_branch", wp.itemStr("cpbdue_branch"));
		sp.ppstr("cpbdue_bank_type", wp.itemEq("cpbdue_type", "1")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_bank_type"));
		sp.ppstr("cpbdue_tcb_type", wp.itemEq("cpbdue_type", "2")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_tcb_type"));
		sp.ppstr("cpbdue_medi_type", wp.itemEq("cpbdue_type", "3")?wp.itemStr("cpbdue_curr_type"):wp.itemStr("cpbdue_medi_type"));
		sp.ppstr("cpbdue_type", wp.itemStr("cpbdue_type"));
		sp.ppstr("cpbdue_curr_type", wp.itemStr("cpbdue_curr_type"));
		sp.ppnum("cpbdue_total_amt", wp.itemNum("cpbdue_total_amt"));
		sp.ppstr("cpbdue_over_days", wp.itemStr("cpbdue_over_days"));
		sp.ppstr("cpbdue_brk_times", wp.itemStr("cpbdue_brk_times"));
		sp.ppstr("cpbdue_seqno", wp.itemStr("cpbdue_seqno"));
		sp.ppstr("cpbdue_begin_date", wp.itemStr("cpbdue_begin_date"));
		sp.ppnum("cpbdue_pay_month_dte", wp.itemNum("cpbdue_pay_month_dte"));
		sp.ppnum("cpbdue_amt", wp.itemNum("cpbdue_amt"));
		sp.ppstr("cpbdue_amt_exp_dte", wp.itemStr("cpbdue_amt_exp_dte"));
		sp.ppnum("cpbdue_rate", wp.itemNum("cpbdue_rate"));
		sp.ppnum("cpbdue_due_card_amt", wp.itemNum("cpbdue_due_card_amt"));
		sp.ppnum("cpbdue_period", wp.itemNum("cpbdue_period"));
		sp.ppnum("cpbdue_due_pc_amt", wp.itemNum("cpbdue_due_pc_amt"));
		sp.ppstr("cpbdue_memo_1", wp.itemStr("cpbdue_memo_1"));
		sp.ppstr("cpbdue_memo_2", wp.itemStr("cpbdue_memo_2"));
		sp.ppnum("cpbdue_pay_month_amt", wp.itemNum("cpbdue_pay_month_amt"));
		sp.ppstr("cpbdue_lst_pay_dte", wp.itemStr("cpbdue_lst_pay_dte"));
		sp.ppnum("cpbdue_month_dueamt", wp.itemNum("cpbdue_month_dueamt"));
		sp.ppnum("cpbdue_total_dueamt", wp.itemNum("cpbdue_total_dueamt"));	
		sp.ppnum("cpbdue_lst_payamt", wp.itemNum("cpbdue_lst_payamt"));
		sp.ppnum("cpbdue_total_payamt", wp.itemNum("cpbdue_total_payamt"));
//		sp.ppstr("cpbdue_lst_upd_dte", wp.itemStr("cpbdue_lst_upd_dte"));
		sp.ppstr("cpbdue_lst_upt_dat_dte", wp.itemStr("cpbdue_lst_upt_dat_dte"));
		sp.ppnum("cpbdue_remd_amt", wp.itemNum("cpbdue_remd_amt"));
		sp.ppstr("apr_user", wp.itemStr("approval_user"));
		sp.ppstr("apr_date", sysDate);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", wp.modPgm());
		sp.addsql(", mod_time ", ", sysdate ");
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if (sqlRowNum <= 0)
			errmsg("新增 col_cpbdue_hst 錯誤");
		return rc;
	}
	
	public void countOnlineComputing1(){	
		String cpbduePayMonthDte = wp.itemStr("cpbdue_pay_month_dte");
		String cpbdueAmtExpDte = wp.itemStr("cpbdue_amt_exp_dte");
		if(wp.itemEq("cpbdue_curr_type","3") && !empty(cpbduePayMonthDte) && !empty(cpbdueAmtExpDte)){
			CommString comms = new CommString();
			String sqlCmd = " select left(business_date,6) as busYM ,right(business_date,2) as busD from ptr_businday ";
			sqlSelect(sqlCmd);
			String busYM = colStr("busYM");
			String busD = colStr("busD");
			int onlineComputing1 = 0;
		
			onlineComputing1 = comms.strToInt(busYM) - comms.strToInt(comms.left(cpbdueAmtExpDte, 6));
			if(comms.strToInt(busD) < comms.strToInt(cpbduePayMonthDte)) {
				onlineComputing1 = onlineComputing1 - 1;
			}
			wp.colSet("online_computing_1", onlineComputing1);
		}
	}
	
} // End of class
