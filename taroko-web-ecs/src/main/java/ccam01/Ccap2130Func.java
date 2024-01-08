package ccam01;

import busi.FuncAction;

public class Ccap2130Func extends FuncAction {
	String isDataFrom = "", isFileNo = "", isSerialNo = "" , isSign = "" , errorDesc = "" , isCorpPSeqno = "";
	int isPayAmt = 0;
	boolean ibCorpCard = false ;
	//--公司總戶資料
	String isCorpAcnoPseqno = "" ; int isCorpCardAcctIdx = 0;		
	
	@Override
	public void dataCheck() {
		errorDesc = "";
		isDataFrom = varsStr("data_from");
		isFileNo = varsStr("file_no");
		isSerialNo = varsStr("serial_no");
		isSign = varsStr("sign");
		isPayAmt = varsInt("pay_amt");
		
		//--檢查未覆核資料
		if(checkUnAprData() == false) {
			rc = -1;
			return ;
		}
				
	}
	
	void dataCheckApr() {
		errorDesc = "";
		isDataFrom = varsStr("data_from");
		isFileNo = varsStr("file_no");
		isSerialNo = varsStr("serial_no");
		isSign = varsStr("sign");
		isPayAmt = varsInt("pay_amt");
		
		//--檢查覆核資料
		if(checkAprData() == false) {
			rc = -1;
			return ;
		}
	}
	
	boolean checkUnAprData() {
		String dbSign = "" ;
		int dbPayAmt = 0 , screenAmt = 0;
		
		String sql1 = "select * from act_repay_creditlimit where data_from = ? and file_no = ? and serial_no = ? ";		
		sqlSelect(sql1,new Object[] {isDataFrom,isFileNo,isSerialNo});
		
		if(sqlRowNum <=0) {
			errorDesc = "此筆資料已被移除 , 請重新讀取資料 ";
			return false;
		}
		
		if(colEq("apr_flag","Y")) {
			errorDesc = "此筆資料已覆核 , 請重新讀取資料";
			return false;
		}
		
		dbSign = colStr("sign");
		dbPayAmt = colInt("pay_amt");
		if("-".equals(dbSign))
			dbPayAmt = dbPayAmt * -1;
								
		if("-".equals(isSign))
			screenAmt = isPayAmt * -1;
		else
			screenAmt = isPayAmt;
		
		if(dbPayAmt != screenAmt) {
			errorDesc = "此筆資料已異動 , 繳款金額不符 , 請重新讀取資料";
			return false;
		}
		
		//--檢核是否為商務卡
		isCorpPSeqno = colStr("corp_p_seqno");
		if(isCorpPSeqno.isEmpty())
			ibCorpCard = false ;
		else
			ibCorpCard = true ;
		
		//--取授權帳戶流水號
		String sql2 = "select card_acct_idx from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		sqlSelect(sql2, new Object[] { colStr("acno_p_seqno") });
		if (sqlRowNum <= 0) {
			errorDesc = "查無授權帳戶資料 帳戶流水號[" + colStr("acno_p_seqno") + "]";
			return false;
		}
		
		return true;
	}
	
	boolean checkAprData() {
		String dbSign = "" ;
		int dbPayAmt = 0 , screenAmt = 0;
		
		String sql1 = "select * from act_repay_creditlimit where data_from = ? and file_no = ? and serial_no = ? ";		
		sqlSelect(sql1,new Object[] {isDataFrom,isFileNo,isSerialNo});
		
		if(sqlRowNum <=0) {
			errorDesc = "此筆資料已被移除 , 請重新讀取資料 ";
			return false;
		}
		
		if(colEq("apr_flag","N") || colEmpty("apr_flag")) {
			errorDesc = "此筆資料已解覆核 , 請重新讀取資料";
			return false;
		}
		
		if(colEq("is_repay","Y")) {
			errorDesc = "此筆資料已批次處理 , 不可進行解覆核";
			return false;
		}
		
		dbSign = colStr("sign");
		dbPayAmt = colInt("pay_amt");
		if("-".equals(dbSign))
			dbPayAmt = dbPayAmt * -1;
								
		if("-".equals(isSign))
			screenAmt = isPayAmt * -1;
		else
			screenAmt = isPayAmt;
		
		if(dbPayAmt != screenAmt) {
			errorDesc = "此筆資料已異動 , 繳款金額不符 , 請重新讀取資料";
			return false;
		}
		
		//--檢核是否為商務卡
		isCorpPSeqno = colStr("corp_p_seqno");
		if(isCorpPSeqno.isEmpty())
			ibCorpCard = false ;
		else
			ibCorpCard = true ;
		
		//--取授權帳戶流水號
		String sql2 = "select card_acct_idx from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		sqlSelect(sql2, new Object[] { colStr("acno_p_seqno") });
		if (sqlRowNum <= 0) {
			errorDesc = "查無授權帳戶資料 帳戶流水號[" + colStr("acno_p_seqno") + "]";
			return false;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbDelete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataProc() {
		//--覆核
		dataCheck();
		if(rc!=1)
			return rc;
		
		updateActRepay();
		if (rc != 1)
			return rc;
		updateCcaCardAcct(1);
		if (rc != 1)
			return rc;
		updateCcaConsume(1);
		if (rc != 1)
			return rc;
		//--若為商務卡須調整商務卡公司戶 acno_flag ='2'
		if (ibCorpCard) {
			getCorpBaseData();
			if (rc != 1)
				return rc;
			updateCcaCardAcctForCorp(1);
			if (rc != 1)
				return rc;
			updateCcaConsumeForCorp(1);
			if (rc != 1)
				return rc;
		}
		
		return rc;
	}
	
	void updateCcaCardAcct(int procType) {
		msgOK();
		// --1:覆核 , 3:解覆核		

		strSql = " update cca_card_acct set ";
		if (procType == 1) {
			strSql += " pay_amt = pay_amt + :pay_amt ";
		} else if (procType == 3) {
			strSql += " pay_amt = pay_amt - :pay_amt ";
		}

		strSql += " where acno_p_seqno =:acno_p_seqno and debit_flag <> 'Y' ";

		if (procType == 1) {
			setNumber("pay_amt", isPayAmt);
			col2ParmStr("acno_p_seqno");
		} else if (procType == 3) {
			setNumber("pay_amt", isPayAmt);
			col2ParmStr("acno_p_seqno");
		}

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errorDesc = "異動個人 cca_card_acct 錯誤 ";
			errmsg("update cca_card_acct error ");
			return;
		}

	}

	void updateCcaCardAcctForCorp(int procType) {
		msgOK();
		// --1:覆核 3:解覆核		

		strSql = " update cca_card_acct set ";
		if (procType == 1) {
			strSql += " pay_amt = pay_amt + :pay_amt ";
		} else if (procType == 3) {
			strSql += " pay_amt = pay_amt - :pay_amt ";
		}

		strSql += " where acno_p_seqno =:acno_p_seqno and debit_flag <> 'Y' ";

		if (procType == 1) {
			setNumber("pay_amt", isPayAmt);
			setString("acno_p_seqno", isCorpAcnoPseqno);
		} else if (procType == 3) {
			setNumber("pay_amt", isPayAmt);
			setString("acno_p_seqno", isCorpAcnoPseqno);
		}

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errorDesc = "異動公司總戶 cca_card_acct 錯誤";
			errmsg("update Corp cca_card_acct error ");
			return;
		}

	}

	void updateCcaConsume(int procType) {
		msgOK();
		// --1:覆核 , 3:解覆核		

		strSql = "update cca_consume set ";
		if (procType == 1) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt + :pay_amt ";
		} else if (procType == 3) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt - :pay_amt ";
		}

		strSql += " where card_acct_idx = :card_acct_idx ";

		if (procType == 1) {
			setNumber("pay_amt", isPayAmt);
		} else if (procType == 3) {
			setNumber("pay_amt", isPayAmt);
		}

		setInt("card_acct_idx", colInt("card_acct_idx"));

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errorDesc = "異動個人 cca_consume 錯誤";
			errmsg("update cca_consume error ");
			return;
		}

	}

	void updateCcaConsumeForCorp(int procType) {
		msgOK();
		// --1:覆核 3:解覆核

		strSql = "update cca_consume set ";
		if (procType == 1) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt + :pay_amt ";
		} else if (procType == 3) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt - :pay_amt ";
		}

		strSql += " where card_acct_idx = :card_acct_idx ";

		if (procType == 1) {
			setNumber("pay_amt", isPayAmt);
		} else if (procType == 3) {
			setNumber("pay_amt", isPayAmt);
		}

		setInt("card_acct_idx", isCorpCardAcctIdx);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errorDesc = "異動公司總戶 cca_consume 錯誤";
			errmsg("update Corp cca_consume error ");
			return;
		}

	}
	
	void getCorpBaseData() {
		if (empty(colStr("corp_p_seqno")))
			return;
		String sql1 = "select acno_p_seqno as corp_acno_p_seqno from act_acno where corp_p_seqno = ? and acct_type = ? and acno_flag = '2' ";
		sqlSelect(sql1, new Object[] { colStr("corp_p_seqno"), colStr("acct_type") });
		if (sqlRowNum <= 0) {
			errorDesc = "查詢公司總戶基本資料錯誤 corp_p_seqno = ["+ colStr("corp_p_seqno")+"]";
			errmsg("查詢公司戶基本資料錯誤 , corp_p_seqno = [" + colStr("corp_p_seqno") + "]");
			return;
		}

		isCorpAcnoPseqno = colStr("corp_acno_p_seqno");

		String sql2 = "select card_acct_idx as corp_card_acct_idx from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		sqlSelect(sql2, new Object[] { isCorpAcnoPseqno });
		if (sqlRowNum <= 0) {
			errorDesc = "查詢公司總戶授權帳戶資料錯誤 帳戶流水號 = ["+ isCorpAcnoPseqno+"]";
			errmsg("查詢公司戶授權帳戶資料錯誤 , 帳戶流水號 = [" + isCorpAcnoPseqno + "]");
			return;
		}

		isCorpCardAcctIdx = colInt("corp_card_acct_idx");
	}
	
	void updateActRepay() {
		msgOK();
		strSql = " update act_repay_creditlimit set "
				+ " apr_flag ='Y' , apr_user = :apr_user , apr_date = to_char(sysdate,'yyyymmdd') "
				+ " where data_from = :data_from and file_no = :file_no and serial_no = :serial_no and apr_flag ='N' ";
		
		setString("apr_user",wp.loginUser);
		var2ParmStr("data_from");
		var2ParmStr("file_no");
		var2ParmStr("serial_no");
		
		sqlExec(strSql);
		
		if (sqlRowNum <= 0) {
			errorDesc = "異動 act_repay_creditlimit 錯誤";
			errmsg("update act_repay_creditlimit error ");
			return;
		}
	}
	
	void updateActRepayUnApr() {
		msgOK();
		strSql = " update act_repay_creditlimit set "
				+ " apr_flag ='N' , apr_user = '' , apr_date = '' "
				+ " where data_from = :data_from and file_no = :file_no and serial_no = :serial_no and apr_flag in ('Y','') ";
				
		var2ParmStr("data_from");
		var2ParmStr("file_no");
		var2ParmStr("serial_no");
		
		sqlExec(strSql);
		
		if (sqlRowNum <= 0) {
			errorDesc = "異動 act_repay_creditlimit 錯誤";
			errmsg("update act_repay_creditlimit error ");
			return;
		}
	}
	
	public int dataProc2() {
		//--解覆核
		dataCheckApr();
		if(rc!=1)
			return rc;
		
		updateActRepayUnApr();
		if (rc != 1)
			return rc;
		updateCcaCardAcct(3);
		if (rc != 1)
			return rc;
		updateCcaConsume(3);
		if (rc != 1)
			return rc;
		//--若為商務卡須調整商務卡公司戶 acno_flag ='2'
		if (ibCorpCard) {
			getCorpBaseData();
			if (rc != 1)
				return rc;
			updateCcaCardAcctForCorp(3);
			if (rc != 1)
				return rc;
			updateCcaConsumeForCorp(3);
			if (rc != 1)
				return rc;
		}
		
		return rc;
	}
	
}
