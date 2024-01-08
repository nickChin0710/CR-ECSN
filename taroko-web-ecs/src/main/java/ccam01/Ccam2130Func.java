package ccam01;

import busi.FuncAction;

public class Ccam2130Func extends FuncAction {
	String isDataFrom = "", isFileNo = "", isSerialNo = "", isAcnoPseqno = "" ;
	String isCorpAcnoPseqno = "";
	int isCorpCardAcctIdx = 0;
	boolean ibCorpCard = false;

	@Override
	public void dataCheck() {
		if (ibAdd) {
			isDataFrom = wp.itemStr("data_from");
			isFileNo = getSysDate();
			isSerialNo = getSerialNo(isFileNo);
			wp.itemSet("file_no", isFileNo);
			wp.itemSet("serial_no", isSerialNo);			
		} else {
			isDataFrom = wp.itemStr("data_from");
			isFileNo = wp.itemStr("file_no");
			isSerialNo = wp.itemStr("serial_no");
			isAcnoPseqno = wp.itemStr("acno_p_seqno");			
		}

		if (empty(isDataFrom)) {
			errmsg("繳款來源:不可空白");
			return;
		}

		if (empty(isFileNo)) {
			errmsg("批次號碼:不可空白");
			return;
		}

		if (empty(isSerialNo)) {
			errmsg("序號:不可空白");
			return;
		}

		// --檢核時間
		getBaseData();
		if (ibAdd)
			return;
						
		//--檢核是否已被覆核
		if(checkProc() == false) {
			errmsg("此筆資料已被批次處理 , 請返回查詢頁面重新操作");
			return ;
		}
		
		
	}
	
	boolean checkProc() {
		
		String sql1 = "select apr_flag from act_repay_creditlimit where file_no = ? and serial_no = ? ";
		sqlSelect(sql1,new Object[] {isFileNo,isSerialNo});
		if(sqlRowNum >0) {
			if(colEq("apr_flag","Y"))
				return false;
		}	else	
			return false;
		
		return true ;
	}
	
	String getSerialNo(String fileNo) {
		String sql1 = "select decode(max(serial_no),null,0,max(serial_no))+1 as new_serial_no from act_repay_creditlimit where file_no = ? ";
		sqlSelect(sql1, new Object[] { fileNo });
		if (sqlRowNum > 0) {
			return colStr("new_serial_no");
		}
		return "";
	}

	void getBaseData() {
		String sql1 = "select id_p_seqno , corp_p_seqno , acno_p_seqno , p_seqno , acct_type"
				+ " from act_acno where acct_type = ? and acno_p_seqno = ? ";

		sqlSelect(sql1, new Object[] { wp.itemStr("ex_acct_type"), wp.itemStr("ex_p_seqno") });
		if (sqlRowNum <= 0) {
			errmsg("查無帳戶資料, 帳戶類別 [" + wp.itemStr("ex_acct_type") + "] , 流水號 [" + wp.itemStr("ex_p_seqno") + "]");
			return;
		}

		String sql2 = "select card_acct_idx from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		sqlSelect(sql2, new Object[] { colStr("acno_p_seqno") });
		if (sqlRowNum <= 0) {
			errmsg("查無授權帳戶資料 帳戶流水號[" + colStr("acno_p_seqno") + "]");
			return;
		}

		String sql3 = "select card_indicator from ptr_acct_type where acct_type = ? ";
		sqlSelect(sql3, new Object[] { wp.itemStr("ex_acct_type") });
		if (sqlRowNum <= 0) {
			errmsg("查無帳戶類別資料 帳戶類別[" + wp.itemStr("ex_acct_type") + "]");
			return;
		}

		if (eqIgno(colStr("card_indicator"), "2"))
			ibCorpCard = true;
		else
			ibCorpCard = false;

	}

	void getCorpBaseData() {
		if (empty(colStr("corp_p_seqno")))
			return;
		String sql1 = "select acno_p_seqno as corp_acno_p_seqno from act_acno where corp_p_seqno = ? and acct_type = ? and acno_flag = '2' ";
		sqlSelect(sql1, new Object[] { colStr("corp_p_seqno"), wp.itemStr("ex_acct_type") });
		if (sqlRowNum <= 0) {
			errmsg("查詢公司戶基本資料錯誤 , corp_p_seqno = [" + colStr("corp_p_seqno") + "]");
			return;
		}

		isCorpAcnoPseqno = colStr("corp_acno_p_seqno");

		String sql2 = "select card_acct_idx as corp_card_acct_idx from cca_card_acct where acno_p_seqno = ? and debit_flag <> 'Y' ";
		sqlSelect(sql2, new Object[] { isCorpAcnoPseqno });
		if (sqlRowNum <= 0) {
			errmsg("查詢公司戶授權帳戶資料錯誤 , 帳戶流水號 = [" + isCorpAcnoPseqno + "]");
			return;
		}

		isCorpCardAcctIdx = colInt("corp_card_acct_idx");

	}

	@Override
	public int dbInsert() {
		actionInit("A");
		dataCheck();
		if (rc != 1)
			return rc;

		insertActRepay();
//		if (rc != 1)
//			return rc;
//		updateCcaCardAcct(1);
//		if (rc != 1)
//			return rc;
//		updateCcaConsume(1);
//		if (rc != 1)
//			return rc;
//		// --若為商務卡須調整商務卡公司戶 acno_flag ='2'
//		if (ibCorpCard) {
//			getCorpBaseData();
//			if (rc != 1)
//				return rc;
//			updateCcaCardAcctForCorp(1);
//			if (rc != 1)
//				return rc;
//			updateCcaConsumeForCorp(1);
//			if (rc != 1)
//				return rc;
//		}

		return rc;
	}

	void insertActRepay() {
		msgOK();
		strSql = "insert into act_repay_creditlimit ( " + " data_from ," + " file_no ," + " serial_no ,"
				+ " pay_card_no ," + " sign ," + " pay_amt ," + " payment_type2 ," + " pay_date ," + " pay_time ,"
				+ " proc_mark ," + " unite_mark ," + " def_branch ," + " pay_branch ," + " is_pass ," + " is_repay ,"
				+ " acct_type ," + " id_p_seqno ," + " corp_p_seqno ," + " p_seqno ," + " acno_p_seqno ,"
				+ " payment_type ," + " remark ," + " crt_date ," + " crt_time ," + " crt_user ," + " mod_user ,"
				+ " mod_time ," + " mod_pgm , apr_flag " + " ) values ( " + " :data_from ," + " :file_no ," + " :serial_no ,"
				+ " :pay_card_no ," + " '' ," + " :pay_amt ," + " :payment_type2 ," + " :pay_date ," + " :pay_time ,"
				+ " '' ," + " '' ," + " '' ," + " '' ," + " 'Y' ," + " 'N' ," + " :acct_type ," + " :id_p_seqno ,"
				+ " :corp_p_seqno ," + " :p_seqno ," + " :acno_p_seqno ," + " '' ," + " :remark ,"
				+ " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ," + " :crt_user ," + " :mod_user ,"
				+ " sysdate ," + " :mod_pgm , 'N' " + " ) ";

		setString("data_from", isDataFrom);
		setString("file_no", isFileNo);
		setString("serial_no", isSerialNo);
		item2ParmStr("pay_card_no");
		item2ParmNum("pay_amt");
		item2ParmStr("payment_type2");
		item2ParmStr("pay_date");
		item2ParmStr("pay_time");
		col2ParmStr("acct_type");
		col2ParmStr("id_p_seqno");
		col2ParmStr("corp_p_seqno");
		col2ParmStr("p_seqno");
		col2ParmStr("acno_p_seqno");
		item2ParmStr("remark");
		setString("crt_user", wp.loginUser);
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", wp.modPgm());

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg("insert act_repay_creditlimit error !");
			return;
		}
	}

	void updateActRepay() {
		msgOK();
		strSql = "update act_repay_creditlimit set " + " pay_date =:pay_date , " + " pay_time =:pay_time , "
				+ " payment_type2 =:payment_type2 , " + " pay_card_no =:pay_card_no , " + " sign =:sign , "
				+ " pay_amt =:pay_amt , " + " remark =:remark ," + " mod_pgm =:mod_pgm , " + " mod_time = sysdate , "
				+ " mod_user = :mod_user "
				+ " where data_from =:data_from and file_no =:file_no and serial_no =:serial_no and apr_flag = 'N' ";

		item2ParmStr("pay_date");
		item2ParmStr("pay_time");
		item2ParmStr("payment_type2");
		item2ParmStr("pay_card_no");
		item2ParmStr("sign");
		item2ParmNum("pay_amt");
		item2ParmStr("remark");
		setString("mod_pgm", wp.modPgm());
		setString("mod_user", wp.loginUser);
		setString("data_from", isDataFrom);
		setString("file_no", isFileNo);
		setString("serial_no", isSerialNo);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update act_repay_creditlimit error");
			return;
		}
	}

	void updateCcaCardAcct(int procType) {
		msgOK();
		// --1:A 2:U 3:D
		double tempAmt = 0.0, oldTempAmt = 0.0;
		tempAmt = wp.itemNum("pay_amt");
		oldTempAmt = wp.itemNum("ori_pay_amt");

		strSql = " update cca_card_acct set ";
		if (procType == 1) {
			strSql += " pay_amt = pay_amt + :pay_amt ";
		} else if (procType == 2) {
			strSql += " pay_amt = pay_amt - :ori_pay_amt + :pay_amt";
		} else if (procType == 3) {
			strSql += " pay_amt = pay_amt - :ori_pay_amt ";
		}

		strSql += " where acno_p_seqno =:acno_p_seqno and debit_flag <> 'Y' ";

		if (procType == 1) {
			setNumber("pay_amt", tempAmt);
			col2ParmStr("acno_p_seqno");
		} else if (procType == 2) {
			setNumber("pay_amt", tempAmt);
			setNumber("ori_pay_amt", oldTempAmt);
			setString("acno_p_seqno", isAcnoPseqno);
		} else if (procType == 3) {
			setNumber("ori_pay_amt", oldTempAmt);
			setString("acno_p_seqno", isAcnoPseqno);
		}

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update cca_card_acct error ");
			return;
		}

	}

	void updateCcaCardAcctForCorp(int procType) {
		msgOK();
		// --1:A 2:U 3:D
		double tempAmt = 0.0, oldTempAmt = 0.0;
		tempAmt = wp.itemNum("pay_amt");
		oldTempAmt = wp.itemNum("ori_pay_amt");

		strSql = " update cca_card_acct set ";
		if (procType == 1) {
			strSql += " pay_amt = pay_amt + :pay_amt ";
		} else if (procType == 2) {
			strSql += " pay_amt = pay_amt - :ori_pay_amt + :pay_amt";
		} else if (procType == 3) {
			strSql += " pay_amt = pay_amt - :ori_pay_amt ";
		}

		strSql += " where acno_p_seqno =:acno_p_seqno and debit_flag <> 'Y' ";

		if (procType == 1) {
			setNumber("pay_amt", tempAmt);
			setString("acno_p_seqno", isCorpAcnoPseqno);
		} else if (procType == 2) {
			setNumber("pay_amt", tempAmt);
			setNumber("ori_pay_amt", oldTempAmt);
			setString("acno_p_seqno", isCorpAcnoPseqno);
		} else if (procType == 3) {
			setNumber("ori_pay_amt", oldTempAmt);
			setString("acno_p_seqno", isCorpAcnoPseqno);
		}

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update Corp cca_card_acct error ");
			return;
		}

	}

	void deleteActRepay() {
		msgOK();
		strSql = "delete act_repay_creditlimit where data_from =:data_from and file_no =:file_no "
				+ " and serial_no =:serial_no and apr_flag = 'N' ";

		setString("data_from", isDataFrom);
		setString("file_no", isFileNo);
		setString("serial_no", isSerialNo);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("delete act_repay_creditlimit error");
			return;
		}
	}

	void updateCcaConsume(int procType) {
		msgOK();
		// --1:A 2:U 3:D
		double tempAmt = 0.0, oldTempAmt = 0.0;
		tempAmt = wp.itemNum("pay_amt");
		oldTempAmt = wp.itemNum("ori_pay_amt");

		strSql = "update cca_consume set ";
		if (procType == 1) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt + :pay_amt ";
		} else if (procType == 2) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt - :ori_pay_amt + :pay_amt";
		} else if (procType == 3) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt - :ori_pay_amt ";
		}

		strSql += " where card_acct_idx = :card_acct_idx ";

		if (procType == 1) {
			setNumber("pay_amt", tempAmt);
		} else if (procType == 2) {
			setNumber("pay_amt", tempAmt);
			setNumber("ori_pay_amt", oldTempAmt);
		} else if (procType == 3) {
			setNumber("ori_pay_amt", oldTempAmt);
		}

		setInt("card_acct_idx", colInt("card_acct_idx"));

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update cca_consume error ");
			return;
		}

	}

	void updateCcaConsumeForCorp(int procType) {
		msgOK();
		// --1:A 2:U 3:D
		double tempAmt = 0.0, oldTempAmt = 0.0;
		tempAmt = wp.itemNum("pay_amt");
		oldTempAmt = wp.itemNum("ori_pay_amt");

		strSql = "update cca_consume set ";
		if (procType == 1) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt + :pay_amt ";
		} else if (procType == 2) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt - :ori_pay_amt + :pay_amt";
		} else if (procType == 3) {
			strSql += " tot_unpaid_amt = tot_unpaid_amt - :ori_pay_amt ";
		}

		strSql += " where card_acct_idx = :card_acct_idx ";

		if (procType == 1) {
			setNumber("pay_amt", tempAmt);
		} else if (procType == 2) {
			setNumber("pay_amt", tempAmt);
			setNumber("ori_pay_amt", oldTempAmt);
		} else if (procType == 3) {
			setNumber("ori_pay_amt", oldTempAmt);
		}

		setInt("card_acct_idx", isCorpCardAcctIdx);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("update Corp cca_consume error ");
			return;
		}

	}

	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if (rc != 1)
			return rc;
		updateActRepay();
		if (rc != 1)
			return rc;
//		updateCcaCardAcct(2);
//		if (rc != 1)
//			return rc;
//		updateCcaConsume(2);
//		if (rc != 1)
//			return rc;
//		// --若為商務卡須調整商務卡公司戶 acno_flag ='2'
//		if (ibCorpCard) {
//			getCorpBaseData();
//			if (rc != 1)
//				return rc;
//			updateCcaCardAcctForCorp(2);
//			if (rc != 1)
//				return rc;
//			updateCcaConsumeForCorp(2);
//			if (rc != 1)
//				return rc;
//		}

		return rc;
	}

	@Override
	public int dbDelete() {
		actionInit("D");
		dataCheck();
		if (rc != 1)
			return rc;
		deleteActRepay();
//		if (rc != 1)
//			return rc;
//		updateCcaCardAcct(3);
//		if (rc != 1)
//			return rc;
//		updateCcaConsume(3);
//		if (rc != 1)
//			return rc;
//		// --若為商務卡須調整商務卡公司戶 acno_flag ='2'
//		if (ibCorpCard) {
//			getCorpBaseData();
//			if (rc != 1)
//				return rc;
//			updateCcaCardAcctForCorp(3);
//			if (rc != 1)
//				return rc;
//			updateCcaConsumeForCorp(3);
//			if (rc != 1)
//				return rc;
//		}
		return rc;
	}

	@Override
	public int dataProc() {
		// TODO Auto-generated method stub
		return 0;
	}

}
