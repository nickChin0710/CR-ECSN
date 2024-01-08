/*
 * 2019-1223  V1.00.01  Alex  update crd_card_base  spec_dept_no
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 */
package ccam01;

import busi.FuncAction;
import busi.func.OutgoingBlock;

public class Ccam2032Func extends FuncAction {
	busi.func.OutgoingBlock ooOutgo = null;
	String cardNo = "", binType = "", isNegReason = "", specDelDate = "", specStatus = "", bankActNo = "";
	String isVmReason = "", isOutgoArea = "", isSendIbm = "N";
	boolean ibDebit = false;

	@Override
	public void dataCheck() {
		selectVallCard();
		selectSpecCode();
	}

	void selectVallCard() {
		String sql1 = " select " + " bin_type , " + " 'N' as debit_flag " + " from crd_card " + " where card_no= ? "
				+ " union " + " select " + " bin_type , " + " 'Y' as debit_flag " + " from dbc_card "
				+ " where card_no = ? ";
		sqlSelect(sql1, new Object[] { wp.itemStr("card_no"), wp.itemStr("card_no") });

		wp.colSet("bin_type", colStr("bin_type"));
		wp.colSet("debit_flag", colStr("debit_flag"));
	}

	void selectSpecCode() {
		String sql1 = "select visa_reason , mast_reason , jcb_reason, " + " neg_reason , send_ibm "
				+ " from cca_spec_code" + " where spec_code =:spec_status ";

		item2ParmStr("spec_status");
		sqlSelect(sql1);

		wp.colSet("visa_reason", colStr("visa_reason"));
		wp.colSet("mast_reason", colStr("mast_reason"));
		wp.colSet("jcb_reason", colStr("jcb_reason"));
		wp.colSet("neg_reason", colStr("neg_reason"));
		wp.colSet("send_ibm", colStr("send_ibm"));
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
		// TODO Auto-generated method stub
		return 0;
	}

	public int updateData() {
		dataCheck();
		strSql = " update cca_special_visa set " + " spec_status =:spec_status ,"
				+ " spec_outgo_reason =:spec_outgo_reason ," + " spec_neg_reason =:spec_neg_reason , "
				+ " spec_del_date =:spec_del_date , " + " spec_dept_no =:spec_dept_no , " + " vm_resp_code = '' , "
				+ " neg_resp_code = '' , " + " fisc_resp_code = '' , " + " logic_del_date = '' , "
				+ " logic_del_time = '' , " + " spec_del_user = '' , " + " vm_del_resp_code = '' , "
				+ " neg_del_resp_code = '' , " + " fisc_del_resp_code = '' , " + " logic_del = 'N' , "
				+ " mcas_resp_code = '' , " + " chg_date = to_char(sysdate,'yyyymmdd') , "
				+ " chg_time = to_char(sysdate,'hh24miss') , " + " chg_user = :chg_user ," + " mod_user = :mod_user ,"
				+ " mod_time = sysdate , " + " mod_pgm =:mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 , "
				+ " from_type ='2' " + " where card_no=:card_no ";
		item2ParmStr("spec_status");
		item2ParmStr("spec_del_date");
		item2ParmStr("spec_dept_no");
		if (wp.colEq("bin_type", "V")) {
			setString("spec_outgo_reason", wp.colStr("visa_reason"));
		} else if (wp.colEq("bin_type", "M")) {
			setString("spec_outgo_reason", wp.colStr("mast_reason"));
		} else if (wp.colEq("bin_type", "J")) {
			setString("spec_outgo_reason", wp.colStr("jcb_reason"));
		}
		if (wp.colEq("debit_flag", "Y")) {
			setString("spec_neg_reason", wp.colStr("send_ibm"));
		} else if (wp.colEq("debit_flag", "N")) {
			setString("spec_neg_reason", wp.colStr("neg_reason"));
		}
		setString("chg_user", wp.loginUser);
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", "ccam2032");
		item2ParmStr("card_no");

		rc = sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
			return rc;
		}
		log("A:" + rc);
		insertSpecialHis("U");
		return rc;
	}

	public int insertData() {
		dataCheck();
		strSql = "insert into cca_special_visa (" + " card_no , " + " bin_type , " + " from_type , " + " spec_status , "
				+ " spec_del_date , " + " spec_dept_no , " + " spec_mst_vip_amt , " + " spec_outgo_reason , "
				+ " spec_neg_reason , " + " vm_resp_code , " + " neg_resp_code , " + " fisc_resp_code , "
				+ " logic_del_date , " + " logic_del_time , " + " spec_del_user , " + " vm_del_resp_code , "
				+ " neg_del_resp_code , " + " fisc_del_resp_code , " + " logic_del , " + " mcas_resp_code , "
				+ " crt_date , " + " crt_time , " + " crt_user , " + " chg_date , " + " chg_time , " + " chg_user , "
				+ " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values (" + " :card_no , "
				+ " :bin_type , " + " '2' , " + " :spec_status , " + " :spec_del_date , " + " :spec_dept_no , "
				+ " '0' , " + " :spec_outgo_reason , " + " :spec_neg_reason , " + " '' , " + " '' , " + " '' , "
				+ " '' , " + " '' , " + " '' , " + " '' , " + " '' , " + " '' , " + " 'N' , " + " '' , "
				+ " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , " + " :crt_user , "
				+ " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , " + " :chg_user , "
				+ " :mod_user , " + " sysdate , " + " :mod_pgm , " + " '1' " + " )";

		item2ParmStr("card_no");
		setString("bin_type", wp.colStr("bin_type"));
		item2ParmStr("spec_status");
		item2ParmStr("spec_del_date");
		item2ParmStr("spec_dept_no");
		if (wp.colEq("bin_type", "V")) {
			setString("spec_outgo_reason", wp.colStr("visa_reason"));
		} else if (wp.colEq("bin_type", "M")) {
			setString("spec_outgo_reason", wp.colStr("mast_reason"));
		} else if (wp.colEq("bin_type", "J")) {
			setString("spec_outgo_reason", wp.colStr("jcb_reason"));
		}
		if (wp.colEq("debit_flag", "Y")) {
			setString("spec_neg_reason", wp.colStr("send_ibm"));
		} else if (wp.colEq("debit_flag", "N")) {
			setString("spec_neg_reason", wp.colStr("neg_reason"));
		}
		setString("chg_user", wp.loginUser);
		setString("mod_user", wp.loginUser);
		setString("crt_user", wp.loginUser);
		setString("mod_pgm", "ccam2032");
		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(getMsg());
			return rc;
		}
		insertSpecialHis("A");
		return rc;
	}

	public int updateCardBase() {
		strSql = " update cca_card_base set " + " spec_flag = 'Y' ," + " spec_status = :spec_status ,"
				+ " spec_date = to_char(sysdate,'yyyymmdd') , " + " spec_time   = to_char(sysdate,'hh24miss'), "
				+ " spec_user   = :spec_user , " + " spec_del_date  = :spec_del_date ," + " spec_mst_vip_amt = '0' ,"
				+ " spec_dept_no =:spec_dept_no , " + " mod_user = :mod_user , " + " mod_time = sysdate , "
				+ " mod_pgm = :mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 " + " where card_no =:card_no ";

		item2ParmStr("spec_status");
		setString("spec_user", wp.loginUser);
		item2ParmStr("spec_del_date");
		setString("mod_user", wp.loginUser);
		setString("mod_pgm", "ccam2032");
		item2ParmStr("card_no");
		item2ParmStr("spec_dept_no");

		rc = sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}
		return rc;
	}

	public int insertSpecialHis(String lsAudCode) {
		strSql = "insert into cca_spec_his (" + " log_date , " + " log_time , " + " card_no , " + " bin_type , "
				+ " from_type , " + " spec_status , " + " spec_del_date , " + " spec_neg_reason , "
				+ " spec_outgo_reason , " + " vm_resp_code , " + " neg_resp_code , " + " aud_code , " + " pgm_id , "
				+ " log_user " + " ) values (" + " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , "
				+ " :card_no , " + " :bin_type , " + " '2' , " + " :spec_status , " + " :spec_del_date , "
				+ " :spec_neg_reason , " + " :spec_outgo_reason , " + " '' , " + " '' , " + " :aud_code , "
				+ " :pgm_id , " + " :log_user " + " )";
		item2ParmStr("card_no");
		setString("bin_type", wp.colStr("bin_type"));
		item2ParmStr("spec_status");
		item2ParmStr("spec_del_date");
		if (wp.colEq("bin_type", "V")) {
			setString("spec_outgo_reason", wp.colStr("visa_reason"));
		} else if (wp.colEq("bin_type", "M")) {
			setString("spec_outgo_reason", wp.colStr("mast_reason"));
		} else if (wp.colEq("bin_type", "J")) {
			setString("spec_outgo_reason", wp.colStr("jcb_reason"));
		}
		if (wp.colEq("debit_flag", "Y")) {
			setString("spec_neg_reason", wp.colStr("send_ibm"));
		} else if (wp.colEq("debit_flag", "N")) {
			setString("spec_neg_reason", wp.colStr("neg_reason"));
		}
		setString("aud_code", lsAudCode);
		setString("pgm_id", "ccam2032");
		setString("log_user", wp.loginUser);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg(getMsg());
			return rc;
		}
		return rc;
	}

	public int procOutGoing() {
		msgOK();
		initData();

		cardNo = wp.itemStr("card_no");
		specStatus = wp.itemStr("spec_status");
		specDelDate = wp.itemStr("spec_del_date");
		getInitData();
		ooOutgo = new OutgoingBlock();
		ooOutgo.setConn(wp);
		ooOutgo.parmClear();
		ooOutgo.isCallAutoAuth = false;
		ooOutgo.p1CardNo = cardNo;
		ooOutgo.p2BinType = binType;
		ooOutgo.p3BankAcctno = bankActNo;
		ooOutgo.p4Reason = isNegReason;
		ooOutgo.p5DelDate = specDelDate;
		ooOutgo.blockReason = specStatus;
		if (!ibDebit) {
			if (notEmpty(isNegReason)) {
				ooOutgo.p4Reason = isNegReason;
				// -開始傳送["+is_card_no+"]至 NCCC (新增)......-
				ooOutgo.blockNegId("1");
				if (eqIgno(ooOutgo.respCode, "N4")) {
					ooOutgo.blockNegId("2");
				}
			}
			if (notEmpty(isVmReason)) {
				wfOutgoingUpdate("1");
				if (binType.equals("V") && eqIgno(ooOutgo.respCode, "N4")) {
					// NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
					wfOutgoingUpdate("2");
				} else if (binType.equals("J") && eqIgno(ooOutgo.respCode, "04")) {
					// NCCC Reject-OutGoing RECORD Already Exist WHILE ADD
					wfOutgoingUpdate("2");
				}
			}
			wfSetLblNM("");
		} else {
			// debit卡--
			if (eqIgno(isSendIbm, "Y") && !empty(ooOutgo.p3BankAcctno)) {
				//--TCB 沒有說要通知主機端 2022/03/8
//				rc = ooOutgo.blockIbmNegfile("1");
				rc =1 ;
				if (rc != 1)
					errmsg(ooOutgo.getMsg());
			}
		}
		
		return rc;			
	}

	void initData() {
		cardNo = "";
		binType = "";
		isNegReason = "";
		specDelDate = "";
		specStatus = "";
		bankActNo = "";
		isVmReason = "";
		ibDebit = false;
		isOutgoArea = "";
		isSendIbm = "N";
	}

	void getInitData() {
		String sql1 = "", sql2 = "";
		sql1 = " select 'N' as debit_flag , bin_type , bank_actno from crd_card where card_no = ? ";
		sql1 += " union ";
		sql1 += " select 'Y' as debit_flag , bin_type , bank_actno from dbc_card where card_no = ? ";
		sqlSelect(sql1, new Object[] { cardNo, cardNo });
		if (sqlRowNum > 0) {
			binType = colStr("bin_type");
			bankActNo = colStr("bank_actno");
			if (colEq("debit_flag", "N"))
				ibDebit = false;
			else
				ibDebit = true;
		}

		sql2 = " select neg_reason, visa_reason, mast_reason, jcb_reason, send_ibm from cca_spec_code where spec_code = ? ";
		sqlSelect(sql2, new Object[] { specStatus });
		if (sqlRowNum > 0) {
			isNegReason = colStr("neg_reason");
			if (binType.equals("V")) {
				isVmReason = colStr("visa_reason");
			} else if (binType.equals("M")) {
				isVmReason = colStr("mast_reason");
			} else if (binType.equals("J")) {
				isVmReason = colStr("jcb_reason");
			}
			isSendIbm = colNvl("send_ibm", "N");
		}

	}

	void wfOutgoingUpdate(String aFile) {
		String lsBinType = binType;

		ooOutgo.parmClear();
		ooOutgo.p1CardNo = cardNo;
		ooOutgo.p2BinType = lsBinType;
		ooOutgo.p4Reason = isVmReason;

		if (eq(lsBinType, "V")) {
			ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
			ooOutgo.p7Region = isOutgoArea;
		} else if (eq(lsBinType, "M")) {
			ooOutgo.p6VipAmt = wp.itemStr2("spec_mst_vip_amt");
		} else if (eq(lsBinType, "J")) {
			ooOutgo.p5DelDate = wp.itemStr2("spec_del_date");
			ooOutgo.p7Region = "00000";
		}
		// 開始傳送 ["+is_card_no+"] 至 VISA......
		ooOutgo.blockVmjReq(aFile);
	}

	void wfSetLblNM(String aType) {
		if (eqIgno(aType, "NEG") || empty(aType)) {
			wp.colSet("neg_resp_code", ooOutgo.negRespCode);
			wp.colSet("neg_reason_code", ooOutgo.negIncomeReason);
		}
		if (eqIgno(aType, "VMJ") || empty(aType)) {
			wp.colSet("vmj_resp_code", ooOutgo.vmjRespCode);
			wp.colSet("vmj_reason_code", ooOutgo.vmjIncomeReason);
		}
	}

}
