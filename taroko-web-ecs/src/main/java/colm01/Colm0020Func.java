/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112-01-16  V1.00.01  Ryan       Initial                                  *
* 112-04-26  V1.00.02  Ryan       修正mod_pgm           *
* 112-10-04  V1.00.03  Ryan       移除PAYMENT_REV_AMT欄位
* 112-10-13  V1.00.04  Ryan       期初不要異動                                                                                          *
***************************************************************************/
package colm01;

import java.util.ArrayList;

import busi.FuncEdit;
import busi.SqlPrepare;
import busi.func.ColFunc;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Colm0020Func extends FuncEdit {
	String pSeqno = "";
	String idCorpPSeqno = "";
	String idPSeqno = "";
	String corpPSeqno = "";
	String stmtCycle = "";
	String referenceNo = "";
	double actJrnlBal = 0;
	double enqSeqno = 0;
	double endBalTotal = 0;
	double afterEndBal = 0;
	double afterBegBal = 0;
	ColFunc colFunc = new ColFunc();
	public Colm0020Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

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
		if(wp.itemEmpty("acct_code")){
			this.errmsg("選項調整科目不可為空值");
			return;
		}
		if(wp.itemEmpty("adj_reason_code")) {
			this.errmsg("調整類別不能為空");
			return;
		}
		idCorpPSeqno = wp.itemStr("id_corp_p_seqno");
		if(selectColBadDetailExt()>0) 
			return;
		stmtCycle = getStmtCycle();
		actJrnlBal = wp.itemEq("acct_type", "01")?getIdSumEndBal():getCorpSumActEndBal();	
		
		if(wp.itemEq("adj_reason_code", "01")) {
//			afterBegBal = colFunc.numAdd(wp.itemNum("beg_bal_" + wp.itemStr("acct_code")), wp.itemNum("transaction_amt"));
			afterEndBal = colFunc.numAdd(wp.itemNum("end_bal_" + wp.itemStr("acct_code")), wp.itemNum("transaction_amt"));
			endBalTotal = colFunc.numAdd(wp.itemNum("end_bal_total"), wp.itemNum("transaction_amt"));
		}else {
//			afterBegBal = colFunc.numSub(wp.itemNum("beg_bal_" + wp.itemStr("acct_code")), wp.itemNum("transaction_amt"));
			afterEndBal = colFunc.numSub(wp.itemNum("end_bal_" + wp.itemStr("acct_code")), wp.itemNum("transaction_amt"));
			endBalTotal = colFunc.numSub(wp.itemNum("end_bal_total"), wp.itemNum("transaction_amt"));
		}
		if(afterEndBal<0) {
			this.errmsg("調整後期末科目餘額不可小於0");
			return;
		}
		if(afterBegBal<0) {
			this.errmsg("調整後期初科目餘額不可小於0");
			return;
		}
		if(endBalTotal<0) {
			this.errmsg("外帳總餘額金額不可小於0");
			return;
		}
	}
	
    /***********************************************************************/
    double getIdSumEndBal() {
    	strSql = "select sum(acct_jrnl_bal) as id_acct_jrnl_bal from act_acct where id_p_seqno = ? and acct_type = '01'";
    	setString(1,idPSeqno);
    	sqlSelect(strSql);
    	return colNum("id_acct_jrnl_bal");
    }
    
    /***********************************************************************/
    double getCorpSumActEndBal() {
    	strSql = "select sum(acct_jrnl_bal) as corp_acct_jrnl_bal from act_acct where corp_p_seqno = ? and acct_type = '03'";
    	setString(1,corpPSeqno);
    	sqlSelect(strSql);
    	return colNum("corp_acct_jrnl_bal");
    }
    
    String getStmtCycle() {
    	strSql = "select stmt_cycle from col_bad_debt  where P_SEQNO = ? and TRANS_DATE = ? and TRANS_TYPE = ? ";
    	setString(1,pSeqno);
    	setString(2,wp.itemStr("trans_date"));
    	setString(3,wp.itemStr("trans_type"));
    	sqlSelect(strSql);
    	return colStr("stmt_cycle");
    }
    
    int selectColBadDetailExt() {
    	strSql = "select p_seqno,id_p_seqno,corp_p_seqno,reference_no from COL_BAD_DETAIL_EXT where ID_CORP_P_SEQNO = ?  ";
    	setString(1,wp.itemStr("id_corp_p_seqno"));
    	sqlSelect(strSql);
     	if(sqlRowNum<=0) {
    		this.errmsg("select COL_BAD_DETAIL_EXT not found ");
    		return 1;
    	}
    	pSeqno = colStr("p_seqno");
    	idPSeqno = colStr("id_p_seqno");
    	corpPSeqno = colStr("corp_p_seqno");
    	referenceNo= colStr("reference_no");
    	return 0;
    }

	// ************************************************************************
	@Override
	public int dbInsert() {
		actionInit("A");
		this.msgOK();

		return rc;
	}

	// ************************************************************************
	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if(rc == -1) {
			return rc;
		}
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Update("col_bad_detail_ext");
//		sp.ppnum("beg_bal", afterBegBal);
		sp.ppnum("end_bal", afterEndBal);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", "colm0020");
	    sp.addsql(",mod_seqno = mod_seqno+1");
		sp.addsql(",mod_time = sysdate ");
		sp.sql2Where(" where id_corp_p_seqno = ? ", wp.itemStr("id_corp_p_seqno"));
		sp.sql2Where(" and acct_code = ? ", wp.itemStr("acct_code"));
		sqlExec(sp.sqlStmt(), sp.sqlParm());
		if(sqlRowNum > 0)
			rc = insertColBadJrnlExt();
		else {
			this.errmsg(String.format("無此科目資料可更新 ,id_corp_p_seqno = [%s] ,acct_code = [%s]", wp.itemStr("id_corp_p_seqno"),wp.itemStr("acct_code")));
		}
		return rc;
	}

	// ************************************************************************
	@Override
	public int dbDelete() {
		actionInit("D");
		this.msgOK();

		return rc;
	}
	// ************************************************************************
	
	public int insertColBadJrnlExt() {
		enqSeqno ++;
		busi.SqlPrepare sp = new SqlPrepare();
		sp.sql2Insert("col_bad_jrnl_ext");
		sp.ppnum("ENQ_SEQNO", enqSeqno);
		sp.ppstr("P_SEQNO", pSeqno);
		sp.ppstr("ACCT_TYPE", wp.itemStr("acct_type"));
		sp.ppstr("ID_P_SEQNO", idPSeqno);
		sp.ppstr("CORP_P_SEQNO", corpPSeqno);
		sp.ppstr("ACCT_DATE", sysDate);
		sp.ppstr("TRAN_CLASS", "B");
		sp.ppstr("TRAN_TYPE", wp.itemStr("acct_code") + "01");
		sp.ppstr("ACCT_CODE", wp.itemStr("acct_code"));
		sp.ppstr("DR_CR", wp.itemEq("adj_reason_code", "01")?"C":"D");
		sp.ppnum("TRANSACTION_AMT", wp.itemEq("adj_reason_code", "02")?wp.itemNum("transaction_amt")*-1:wp.itemNum("transaction_amt"));
		sp.ppnum("JRNL_BAL", endBalTotal);
		sp.ppnum("ACT_JRNL_BAL", actJrnlBal);
		sp.ppnum("ITEM_BAL", afterEndBal);
		sp.ppstr("ITEM_DATE", sysDate);
		sp.ppstr("ADJ_REASON_CODE", wp.itemStr("adj_reason_code"));
		sp.ppstr("ADJ_COMMENT", wp.itemStr("adj_comment"));
//		sp.ppnum("PAYMENT_REV_AMT", 0);
		sp.ppstr("REFERENCE_NO", referenceNo);
		sp.ppstr("STMT_CYCLE", stmtCycle);
		sp.ppstr("CRT_DATE", sysDate);
		sp.ppstr("CRT_TIME", sysTime);
		sp.ppstr("CRT_USER", wp.loginUser);
		sp.ppstr("mod_user", wp.loginUser);
		sp.ppstr("mod_pgm", "colm0020");
	    sp.ppnum("mod_seqno", 1);
		sp.addsql(", mod_time ", ", sysdate ");
		return sqlExec(sp.sqlStmt(), sp.sqlParm());
	}
	
	
} // End of class
