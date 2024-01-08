package ccam01;
/** 卡戶/卡片基本資料查詢
 * 2019-1230   JH    acno_spec_status
 * 19-1129:    Alex  idno_of_credit_amt fixed
 * 19-0611:    JH    p_seqno >>acno_xxx
 * 107-0814:	JH		modify
 * 109-04-20   shiyuqi       updated for project coding standard 
 * 111-10-11   V1.00.06  Alex  修正 sql error ,帳戶狀態位於 act_acno 而非 act_acct
 * 2023-1011   JH    結帳日期(this_close_date)=act_acno.last_stmt_date
 * 2023-1122   JH    ptr_bankcode.bc_bankcode=X3
 */

import busi.FuncAction;
import ecsfunc.DeCodePtr;

//--先 讀 cca_card_base ,  debit_flag , id_p_seqno , p_seqno , corp_p_seqno , card_acct_idx 
//--再依 debit_flag 去做資料讀取
//**debit_flag=N; crd_idno, crd_card, act_acno
//**debit_flag=Y; dbc_card, dbc_idno, dba_acno

public class Ccam2090Func extends FuncAction {
	String isCardNo = "", isIdPSeqno = "", isSonCardFlag = "", isCorpPSeqno = "", isCardAcctIdx = "",
			isMajorIdPSeqno = "";
	String isAcnopSeqno = "", isAcctpSeqno = "", isAcnoFlag = "", isAcctType = "" , dataFlag = "";
	String returnAmtDate = "";
	boolean ibDebit = false;
	
	CcasLimitStageOne oolimit = new CcasLimitStageOne();	
	CcasLimit oolimitPhase3 = new CcasLimit();

	int lsRemainTot = 0;
	taroko.base.CommDate commDate = new taroko.base.CommDate();

	@Override
	public void dataCheck() {
		// TODO Auto-generated method stub

	}

	@Override
	public int dbInsert() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dbUpdate() {
		actionInit("U");

		strSql = "update cca_card_acct set" + " auth_remark =?," + commSqlStr.setModxxx(modUser, modPgm)
				+ " where card_acct_idx =?" + " and auth_remark <>?";
		setString2(1, wp.itemStr2("auth_remark"));
		setDouble(wp.itemNum("card_acct_idx"));
		setString(wp.itemStr2("auth_remark"));
		sqlExec(strSql);
		if (sqlRowNum <= 0)
			return 1;

		busi.func.EcsComm ooFunc = new busi.func.EcsComm();
		ooFunc.setConn(wp.conn());
		String lsDeptName = ooFunc.getUserDeptName(modUser);
		if (empty(lsDeptName))
			lsDeptName = wp.loginDeptNo;

		// ------------------
		strSql = " insert into cca_auth_remark_log ( " + " card_acct_idx , " + " chg_date , " + " chg_time , "
				+ " chg_user , " + " auth_remark , " + " user_deptno " + " ) values ( " + " :card_acct_idx , "
				+ " to_char(sysdate,'yyyymmdd') , " + " to_char(sysdate,'hh24miss') , " + " :chg_user , "
				+ " :auth_remark , " + " :user_deptno " + " ) ";
		setDouble2("card_acct_idx", wp.itemNum("card_acct_idx"));
		setString2("chg_user", modUser);
		setString2("auth_remark", wp.itemStr2("auth_remark"));
		setString2("user_deptno", lsDeptName);

		sqlExec(strSql);
		if (sqlRowNum <= 0) {
			errmsg("insert cca_auth_remark_log error");
		}
		return rc;
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

	// ------------------------------------------------------------------------------
	public int readData() {
		dataFlag = "";
		int retunCode = 0;
		selectDataFlag();
		if("Y".equals(dataFlag)) {
			retunCode = readDataPhase2();
		}	else	{
			retunCode = readDataPhase3();
		}
		return retunCode;
	}
	
	void selectDataFlag() {
		
		String sql1 = "select wf_value2 , wf_value3 from ptr_sys_parm where wf_parm = 'SYSPARM' and wf_key ='ROLLBACK_P2' ";
		sqlSelect(sql1);
		
		dataFlag = colStr("wf_value2");
		returnAmtDate = colStr("wf_value3");
	}
	
	int readDataPhase3() {
		oolimitPhase3.setConn(wp);
		oolimitPhase3.hh.returnDate = returnAmtDate ;
		if (readCrdBase() == -1) return -1;
		if (readIdno() == -1) return -1;
		if (readCardAcct() == -1) return -1;
		if (readAcno() == -1) return -1;
		if (readCard() == -1) return -1;
		if (readAnalSub() == -1) return -1;
		if (readAdjParm() == -1) return -1;
		if (readCorp() == -1) return -1;
		if (readBilContractPhase3() == -1) return -1;
		if (readRemarkLog() == -1) return -1;
//		selectCycPyaj();
		selectXxxCard(isCardNo);
		selectCrdCorrelate();
		selectActAcct();
		selectCrdReturn();
		selectActDebt();
		readCrdIdnoExt();		
		//--爭議款
		readRskProblem();
		oolimitPhase3.canUseLimit(isCardNo);
		//--payment未銷
		double lmPayAmt =oolimitPhase3.hh.totUnpaidAmt;
		wp.colSet("pay_amt",lmPayAmt);

		//--子卡餘額 計算
		wp.colSet("son_tot_amt_consume", oolimitPhase3.hh.cardTotConsume);
		wp.colSet("indiv_crd_lmt", oolimitPhase3.hh.cardLimit);
		wp.colSet("son_remain_tot", oolimitPhase3.hh.cardLimit - oolimitPhase3.hh.cardTotConsume);

		//--臨調後額度:臨調 != 0  變色
		wp.colSet("tot_amt_month", oolimitPhase3.hh.adjLimit);
		if (wp.colNum("tot_amt_month") != 0) {
		   wp.colSet("color_tot_amt_month", "style='color: #cc0000'");
		} else wp.colSet("color_tot_amt_month", "class='zz_data'");
	   String lsClassVdate =colStr("class_valid_date");
	   if (chkStrend(getSysDate(), nvl(lsClassVdate,"19000101")) == 1) {
		   wp.colSet("class_code", colStr("ccas_class_code"));
	   }
	   wp.colSet("old_class_date",lsClassVdate);
	   wp.colSet("ibm_receive_amt", oolimitPhase3.hh.ibmReceiveAmt);
	   //授權-總結:[tot_amt_consume]
	   wp.colSet("tot_amt_consume", oolimitPhase3.hh.totAmtConsume);
	   //授權-預現:[tot_amt_precash]
	   wp.colSet("tot_amt_precash", oolimitPhase3.hh.totAmtPrecash);
	   //授權-一般:[wk_tot_amt]
	   wp.colSet("wk_tot_amt", oolimitPhase3.hh.totAmtConsume - oolimitPhase3.hh.totAmtPrecash);
	   //可用餘額-總結:[remain_tot]
	   wp.colSet("remain_tot", oolimitPhase3.hh.canUseLimit);
	   wp.colSet("spec_remain_tot", oolimitPhase3.hh.canUseSpecLimit);
	   //可用餘額-預現:[remain_cash]
	   oolimitPhase3.canUseCash(isCardNo);
	   wp.colSet("remain_cash", oolimitPhase3.hh.canUseCash);
	   wp.colSet("ccas_limit_cash",oolimitPhase3.hh.creditCash);
	   //專款專用額度
	   wp.colSet("special_amt", oolimitPhase3.hh.specialAmt);
	   wp.colSet("over_special_amt", oolimitPhase3.hh.overSpecialAmt);
	   //-商務卡實際可用餘額:-
	   CcasLimitCorp oolimitPhase3Corp = new CcasLimitCorp();
	   oolimitPhase3Corp.setConn(wp.getConn());
	   oolimitPhase3Corp.canuse_Limit(wp.colStr("corp_no"), wp.colStr("acct_type"));
	   if(oolimitPhase3Corp.hh.canUseLimit <=oolimitPhase3.hh.canUseLimit) {
	      wp.colSet("ex_corp_limit", oolimitPhase3Corp.hh.canUseLimit);
	   }
	   else {
	      wp.colSet("ex_corp_limit", oolimitPhase3Corp.hh.canUseLimit);
	   }
	   
	   //--商務卡總額度
	   if(oolimit.totAmtMonth>0) {
		   wp.colSet("ex_corp_tot_limit",oolimitPhase3Corp.hh.adjLimit);
	   }	else	{
		   wp.colSet("ex_corp_tot_limit",oolimitPhase3Corp.hh.creditLimit);
	   }
	   
	   //--總未付本金 acct_jrnl_bal + 分期未Posting
	   //double lm_amt =wp.colNum("acct_jrnl_bal")+wp.colNum("db_inst_unpost");
	   double lmAmt =wp.colNum("paid_consume_fee")+wp.colNum("unpaid_consume_fee")+
	         wp.colNum("db_inst_unpost") + wp.colNum("paid_precash") + wp.colNum("unpaid_precash");
	   if (lmAmt <0) {
	      wp.colSet("wk_acct_jrnl_bal",0);
	   }
	   else {
	      wp.colSet("wk_acct_jrnl_bal", lmAmt);
	   }
	   //--單月消費金額 read act_anal_sub
	   selectActAnalSub();
	   //-CCAS.Mcode-
	   String lsMcodeDate =wp.colStr("mcode_valid_date");
	   String lsMcodeCcas =wp.colStr("ccas_mcode");
	   if (notEmpty(lsMcodeCcas) && commString.strComp(lsMcodeDate,sysDate)>=0) {
	      wp.colSet("int_rate_mcode",lsMcodeCcas);
	   }
	   
	   //--已使用額度
//	   wp.colSet("already_use_amt", oolimitPhase3.hh.problemAmt + oolimitPhase3.hh.unpostInstFee + oolimitPhase3.hh.acctJrnlBal + oolimitPhase3.hh.totAmtConsume);
	   double alreadyUseAmt = 0.0;
	   alreadyUseAmt = oolimitPhase3.hh.problemAmt + oolimitPhase3.hh.unpostInstFee + wp.colNum("acct_jrnl_bal") + oolimitPhase3.hh.totAmtConsume;
	   if(alreadyUseAmt < 0)
		   alreadyUseAmt = 0;
	   wp.colSet("already_use_amt", alreadyUseAmt);
	   
	   //--VD 清除可用餘額
	   if(ibDebit) {		 		 
		   wp.colSet("remain_tot", "--");
		   wp.colSet("remain_cash", "--");
		   wp.colSet("spec_remain_tot", "--");
		   wp.colSet("ttl_amt_bal", "--");
		   wp.colSet("pay_amt", "--");
		   wp.colSet("wk_oplk", "--");
	   }
	   
	 return 0;
	}
	
	int readDataPhase2() {
		oolimit.setConn(wp);

		if (readCrdBase() == -1)
			return -1;
		if (readIdno() == -1)
			return -1;
		if (readCardAcct() == -1)
			return -1;
		if (readAcno() == -1)
			return -1;
		if (readCard() == -1)
			return -1;
		if (readAuthConsume() == -1)
			return -1;
		if (readAdjParm() == -1)
			return -1;
		if (readCorp() == -1)
			return -1;
		if (readCrdIdnoExt() == -1)
			return -1;
		if (readBilContract() == -1)
			return -1;
		if (readRemarkLog() == -1)
			return -1;
		selectActJrnl();
		selectXxxCard(isCardNo);
		selectCrdCorrelate();
		selectActAcct();
		selectCrdReturn();
		selectActDebt();
		// select_act_acct_sum();
		// --payment未銷
		readPayDetail();
		// --爭議款
		readRskProblem();
		// -jh[20190402]授權可用額度-

		// --可用餘額 (總結) 計算
		oolimit.canUseLimit(isCardNo);
		//--cca_card_acct.jrnl_bal > 0 應繳總額 , <0 溢繳款 , for stage one
		if(oolimit.paidConsume>=0) {
			wp.colSet("ttl_amt_bal",oolimit.paidConsume);
		}	else if(oolimit.paidConsume<0) {
			wp.colSet("wk_oplk",oolimit.paidConsume*-1);
		}
		//--payment 未消 , for stage one
		wp.colSet("pay_amt", oolimit.totUnpaidAmt);
		
		// --子卡餘額 計算
		wp.colSet("son_tot_amt_consume", oolimit.cardTotConsume);
		wp.colSet("indiv_crd_lmt", oolimit.cardLimit);
		wp.colSet("son_remain_tot", oolimit.cardLimit - oolimit.cardTotConsume);

		// --臨調後額度:臨調 != 0 變色
		wp.colSet("tot_amt_month", oolimit.totAmtMonth);
		if (wp.colNum("tot_amt_month") != 0) {
			wp.colSet("color_tot_amt_month", "style='color: #cc0000'");
		} else
			wp.colSet("color_tot_amt_month", "class='zz_data'");

		String lsClassVdate = colStr("class_valid_date");
		if (chkStrend(getSysDate(), nvl(lsClassVdate, "19000101")) == 1) {
			wp.colSet("class_code", colStr("ccas_class_code"));
		}
		wp.colSet("old_class_date", lsClassVdate);
		// -JH:20190402-
		// 分期未POSTING金額:[db_inst_unpost]
		// wp.colSet("db_inst_unpost",oolimit.unpost_inst_fee);
		// payment未銷:[pay_amt]
		// 總未付本金:[ttl_amt_bal]
		// 預借指撥金額:[ibm_receive_amt] - TCB 沒有指撥
		wp.colSet("ibm_receive_amt", 0);
//		wp.colSet("ibm_receive_amt", oolimit.ibm_receive_amt);
		// 授權-總結:[tot_amt_consume]
		wp.colSet("tot_amt_consume", oolimit.totAmtConsume);
		// 已使用額度
		wp.colSet("already_use_amt", wp.colNum("tot_amt_consume") + wp.colNum("ttl_amt_bal"));
		// 授權-預現:[tot_amt_precash]
		wp.colSet("tot_amt_precash", oolimit.totAmtPrecash);
		// 授權-一般:[wk_tot_amt]
		wp.colSet("wk_tot_amt", oolimit.totAmtConsume - oolimit.totAmtPrecash);
		// 可用餘額-總結:[remain_tot]
		wp.colSet("remain_tot", oolimit.canUseLimit);
		wp.colSet("spec_remain_tot", oolimit.specCanUseLimit);
		// 可用餘額-預現:[remain_cash]
		oolimit.canUseCash();
		wp.colSet("remain_cash", oolimit.canUseCash);
		wp.colSet("ccas_limit_cash", oolimit.ccasLimitCash);
		// -商務卡實際可用餘額:-
		oolimit.corpCanuseLimit(isCardNo);
		wp.colSet("ex_corp_limit", oolimit.canUseLimit);
		// --商務卡總額度
		if(oolimit.totAmtMonth>0) {
			wp.colSet("ex_corp_tot_limit",oolimit.totAmtMonth);
		}	else	{
			wp.colSet("ex_corp_tot_limit",oolimit.lineCreditAmt);
		}
		// --總未付本金 acct_jrnl_bal + 分期未Posting
		// double lmAmt = wp.colNum("acct_jrnl_bal") + wp.colNum("db_inst_unpost");
		double lmAmt = wp.colNum("paid_consume_fee") + wp.colNum("unpaid_consume_fee") + wp.colNum("db_inst_unpost");
		if (lmAmt < 0) {
			wp.colSet("wk_acct_jrnl_bal", 0);
		} else {
			wp.colSet("wk_acct_jrnl_bal", lmAmt);
		}
		//--逾期未繳金額
		wp.colSet("unpay_amt", oolimit.unpayAmt);
		// --單月消費金額 read act_anal_sub
		readActAnalSub();

		// -CCAS.Mcode-
		String lsMcodeDate = wp.colStr("mcode_valid_date");
		String lsMcodeCcas = wp.colStr("ccas_mcode");
		if (notEmpty(lsMcodeCcas) && commString.strComp(lsMcodeDate, sysDate) >= 0) {
			wp.colSet("int_rate_mcode", lsMcodeCcas);
		}
		
		//--VD 清除可用餘額
		if(ibDebit) {
			wp.colSet("remain_tot", "--");
			wp.colSet("remain_cash", "--");
			wp.colSet("spec_remain_tot", "--");
			wp.colSet("ttl_amt_bal", "--");
			wp.colSet("pay_amt", "--");
			wp.colSet("wk_oplk", "--");
		}
		
		return 0;
	}
	
	void readRskProblem() {
		String sql1 = " select sum(prb_amount) as tl_prb_amount from rsk_problem where add_apr_date <>'' "
				+ " and close_apr_date =''" + " and card_no in (select card_no from crd_card where acno_p_seqno =?)";
		sqlSelect(sql1, new Object[] { isAcnopSeqno });
		if (sqlRowNum > 0) {
			wp.colSet("tl_prb_amount", colStr("tl_prb_amount"));
		}
	}

	void readActAnalSub() {
		String sql1 = " select " + " his_purchase_amt+his_cash_amt as temp_consume " + " from act_anal_sub "
				+ " where acct_type = ? ";

		if (eqIgno(isAcnoFlag, "1")) {
			sql1 += " and p_seqno = ? ";
		} else {
			sql1 += " and p_seqno in (select p_seqno from act_acno where corp_p_seqno = ? ) ";
		}

		sql1 += " order by acct_month Desc " + commSqlStr.rownum(6);
		if (eqIgno(isAcnoFlag, "1")) {
			sqlSelect(sql1, new Object[] { isAcctType, isAcctpSeqno });
		} else {
			sqlSelect(sql1, new Object[] { isAcctType, isCorpPSeqno });
		}

		int ilRows = sqlRowNum;

		if (ilRows <= 0)
			return;
		int rr = 1;
		for (int ii = 0; ii < 6; ii++) {
			wp.colSet("consume_" + rr, colStr(ii, "temp_consume"));
			rr++;
		}

	}

	void readPayDetail() {
		String sql1 = " select sum(pay_amt) as pay_amt from act_pay_detail where p_seqno = ? ";
		sqlSelect(sql1, new Object[] { isAcctpSeqno });
		if (sqlRowNum > 0) {
			wp.colSet("pay_amt", colStr("pay_amt"));
		}
	}

	// void readActAcctSum(){
	// String sql1 = " select billed_end_bal , unbill_end_bal from act_acct_sum
	// where acct_code ='PN'
	// and p_seqno = ? ";
	// sqlSelect(sql1,new Object[]{is_acctp_seqno});
	// if(sqlRowNum>0){
	// wp.colSet("unbill_end_bal", colStr("unbill_end_bal"));
	// wp.colSet("billed_end_bal", colStr("billed_end_bal"));
	// }
	// }

	// ------------------------------------------------------------------------------
	public int readCrdBase() {
		if (!empty(wp.itemStr("ex_card_no")))
			isCardNo = wp.itemStr("ex_card_no");
		else
			isCardNo = wp.itemStr("kk_card_no");

		// if(!empty(wp.item_ss("kk_card_no")))
		// is_card_no = wp.item_ss("kk_card_no");
		// else
		// is_card_no = wp.item_ss("ex_card_no");

		String sql1 = " select" + " A.debit_flag , " + " A.id_p_seqno , " + " A.acno_p_seqno as acno_p_seqno,"
				+ " A.p_seqno as acct_p_seqno, " + " A.corp_p_seqno , " + " A.card_acct_idx , "
				+ " A.major_id_p_seqno , " + " uf_idno_id2(A.major_id_p_seqno,A.acct_type) as major_idno , "
				+ " uf_spec_status(A.spec_status,A.spec_del_date) as card_spec_status ," + " A.acct_type, A.acno_flag,"
				+ " hex(A.rowid) as rowid" + " from cca_card_base A" + " where A.card_no = ? ";
		sqlSelect(sql1, new Object[] { isCardNo });

		if (sqlRowNum <= 0) {
			errmsg("Read CrdBase error !, [%s]", isCardNo);
			return -1;
		}

		ibDebit = colEq("debit_flag", "Y");
		isIdPSeqno = colStr("id_p_seqno");
		isAcnopSeqno = colStr("acno_p_seqno");
		isAcctpSeqno = colStr("acct_p_seqno");
		isCorpPSeqno = colStr("corp_p_seqno");
		isCardAcctIdx = colStr("card_acct_idx");
		isMajorIdPSeqno = colStr("major_id_p_seqno");
		isAcctType = colStr("acct_type");
		isAcnoFlag = colNvl("acno_flag", "1");

		wp.colSet("debit_flag", colNvl("debit_flag", "N"));
		wp.colSet("id_p_seqno", isIdPSeqno);
		wp.colSet("acno_p_seqno", isAcnopSeqno);
		wp.colSet("acctp_seqno", isAcctpSeqno);
		wp.colSet("corp_p_seqno", isCorpPSeqno);
		wp.colSet("card_acct_idx", isCardAcctIdx);
		wp.colSet("major_id_p_seqno", isMajorIdPSeqno);
		wp.colSet("card_spec_status", colStr("card_spec_status"));
		wp.colSet("rowid", colStr("rowid"));
		wp.colSet("son_tot_amt_consume", 0);
		wp.colSet("major_idno", colStr("major_idno"));
		// --

		return 0;
	}

	// ------------------------------------------------------------------------------
	public int readIdno() {
		String sql1 = "";
		if (!ibDebit) {
			sql1 = " select " + " chi_name , " + " birthday , " + " id_no , id_no_code , " + " card_since , "
					+ " asset_value , company_name , " + " job_position , " + " office_area_code1 , "
					+ " office_tel_no1 , " + " office_tel_ext1 , " + " cellar_phone , " + " home_area_code1 , "
					+ " home_tel_no1 , " + " home_tel_ext1 , " + " e_mail_addr , " + " msg_purchase_amt " // 發簡訊消費金額
					+ " from crd_idno " + " where id_p_seqno = ? ";
		} else {
			sql1 = " select " + " chi_name , " + " birthday , " + " id_no, id_no_code , " + " card_since , "
					+ " asset_value , company_name , " + " job_position , " + " office_area_code1 , "
					+ " office_tel_no1 , " + " office_tel_ext1 , " + " cellar_phone , " + " home_area_code1 , "
					+ " home_tel_no1 , " + " home_tel_ext1 , " + " e_mail_addr , " + " msg_purchase_amt "
					+ " from dbc_idno " + " where id_p_seqno = ? ";
		}

		sqlSelect(sql1, new Object[] { isIdPSeqno });

		if (sqlRowNum <= 0) {
			errmsg("Read IDNO error !");
			return -1;
		}

		wp.colSet("chi_name", colStr("chi_name"));
		wp.colSet("birthday", colStr("birthday"));
		wp.colSet("id_no", colStr("id_no"));
		wp.colSet("wk_idno_code", colStr("id_no") + "-" + colStr("id_no_code"));
		wp.colSet("card_since", colStr("card_since"));
		wp.colSet("asset_value", colStr("asset_value"));
		wp.colSet("maj_corp_name", colStr("company_name"));
		wp.colSet("job_position", colStr("job_position"));
		wp.colSet("wk_office_tel",
				colStr("office_area_code1") + "-" + colStr("office_tel_no1") + "-" + colStr("office_tel_ext1"));
		wp.colSet("cellar_phone", colStr("cellar_phone"));
		wp.colSet("wk_home_tel",
				colStr("home_area_code1") + "-" + colStr("home_tel_no1") + "-" + colStr("home_tel_ext1"));
		wp.colSet("e_mail_addr", colStr("e_mail_addr"));
		wp.colSet("msg_purchase_amt", colStr("msg_purchase_amt"));
		// -正卡-
		if (!eq(isIdPSeqno, isMajorIdPSeqno)) {
			strSql = "select company_name as maj_corp_name, job_position " + " from crd_idno where id_p_seqno =?";
			sqlSelect(strSql, isMajorIdPSeqno);
			if (sqlRowNum > 0) {
				col2wpCol("maj_corp_name");
				col2wpCol("job_position");
			}
		}
		return 0;
	}

	// ------------------------------------------------------------------------------
	public int readCardAcct() {

		String sql1 = " select " + " block_reason1 , " + " block_reason2 , " + " block_reason3 , " + " block_reason4 , "
				+ " block_reason5 , " + " ccas_mcode , " + " mcode_valid_date ," + " adj_eff_start_date , "
				+ " adj_eff_end_date , " + " tot_amt_month , " + " ccas_class_code , " + " class_valid_date , "
				+ " uf_spec_status(spec_status,spec_del_date) as acno_spec_status " + " from cca_card_acct "
				+ " where acno_p_seqno = ? ";
		if (ibDebit)
			sql1 += " and debit_flag ='Y'";
		else
			sql1 += " and debit_flag <>'Y'";
		sqlSelect(sql1, new Object[] { isAcnopSeqno });

		if (sqlRowNum <= 0) {
			errmsg("Read CardAcct error !");
			return -1;
		}

		if (commDate.sysComp(colStr("adj_eff_start_date")) >= 0 && commDate.sysComp(colStr("adj_eff_end_date")) <= 0) {
			wp.colSet("tot_amt_month", colStr("tot_amt_month"));
		}

		wp.colSet("block_reason1", colStr("block_reason1"));
		wp.colSet("block_reason2", colStr("block_reason2"));
		wp.colSet("block_reason3", colStr("block_reason3"));
		wp.colSet("block_reason4", colStr("block_reason4"));
		wp.colSet("block_reason5", colStr("block_reason5"));
		wp.colSet("acno_spec_status", colStr("acno_spec_status"));		
		wp.colSet("ccas_mcode", colStr("ccas_mcode"));
		wp.colSet("mcode_valid_date", colStr("mcode_valid_date"));
		return 0;
	}

	// ------------------------------------------------------------------------------
	public int readAcno() {
		String sql1 = "";
		if (!ibDebit) {
			sql1 = " select " + " int_rate_mcode , " + " class_code , " + " vip_code , " + " curr_pd_rating , "
					+ " payment_no , " + " acct_status , " + " payment_rate1 , " + " payment_rate2 , "
					+ " payment_rate3 , " + " payment_rate4 , " + " payment_rate5 , " + " payment_rate6 , "
					+ " payment_rate7 , " + " payment_rate8 , " + " payment_rate9 , " + " payment_rate10 , "
					+ " payment_rate11 , " + " payment_rate12 , " + " line_of_credit_amt , "
					+ " line_of_credit_amt_cash ," + " acct_type , " + " bill_sending_zip , " + " bill_sending_addr1 , "
					+ " bill_sending_addr2 , " + " bill_sending_addr3 , " + " bill_sending_addr4 , "
					+ " bill_sending_addr5 , " + " autopay_acct_bank , " + " autopay_acct_no , " + " stmt_cycle , "
					+ " autopay_acct_s_date , autopay_acct_e_date , new_cycle_month , last_pay_date , last_pay_amt "
             +", last_stmt_date "
					+ " from act_acno "
             + " where acno_p_seqno = ? ";
		} else {
			sql1 = " select " + " '' as int_rate_mcode , " + " class_code , " + " vip_code , " + " acct_status , "
					+ " payment_rate1 , " + " payment_rate2 , " + " payment_rate3 , " + " payment_rate4 , "
					+ " payment_rate5 , " + " payment_rate6 , " + " payment_rate7 , " + " payment_rate8 , "
					+ " payment_rate9 , " + " payment_rate10 , " + " payment_rate11 , " + " payment_rate12 , "
					+ " line_of_credit_amt , " + " acct_type , " + " bill_sending_zip , " + " bill_sending_addr1 , "
					+ " bill_sending_addr2 , " + " bill_sending_addr3 , " + " bill_sending_addr4 , "
					+ " bill_sending_addr5 , " + " autopay_acct_bank , " + " autopay_acct_no "
             +", last_stmt_date "
             + " from dba_acno "
					+ " where p_seqno = ? ";
		}

		sqlSelect(sql1, new Object[] { isAcnopSeqno });

		if (sqlRowNum <= 0) {
			errmsg("Read ACNO error !");
			return -1;
		}

		String sql2 = "";

		if (!ibDebit) {
			sql2 = " select " + " sum(line_of_credit_amt) as idno_of_credit_amt " + " from act_acno " + " where 1=1 "
					+ " and acno_flag in ('1','3') "
					+ " and acno_p_seqno in (select acno_p_seqno from crd_card where current_code = '0' and id_p_seqno = ? )";
		} else {
			sql2 = " select " + " sum(line_of_credit_amt) as idno_of_credit_amt " + " from dba_acno " + " where 1=1 "
					+ " and id_p_seqno = ? ";
		}
		sqlSelect(sql2, new Object[] { isMajorIdPSeqno });
		if (sqlRowNum <= 0)
			return -1;

		String sql3 = " select " + " this_acct_month, this_close_date, this_lastpay_date " + " from ptr_workday "
				+ " where stmt_cycle = ? ";
		if (!ibDebit) {
			sqlSelect(sql3, new Object[] { colStr("stmt_cycle") });
			if (sqlRowNum <= 0)
				return -1;
			wp.colSet("acct_month", colStr("this_acct_month"));
			//--改讀act_acct_hst
//			wp.colSet("this_close_date", colStr("this_close_date"));
//			wp.colSet("this_lastpay_date", colStr("this_lastpay_date"));
		} else {
			wp.colSet("acct_month", sysDate.substring(0, 6));
			wp.colSet("this_close_date", "");
			wp.colSet("this_lastpay_date", "");
		}

		String sql4 = " select " + " bc_abname "
          + " from ptr_bankcode "
          + " where bc_bankcode = ? ";
      String ls_Bank=commString.left(colStr("autopay_acct_bank"),3);
      if (notEmpty(ls_Bank)) {
         sqlSelect(sql4, new Object[] {ls_Bank});
         if (sqlRowNum >0) {
            wp.colSet("tt_autopay_acct_bank", colStr("bc_abname"));
         }
      }

		String sql5 = " select stmt_cycle_date , stmt_last_payday from act_acct_hst where p_seqno = ? ";
		sqlSelect(sql5, new Object[] {isAcnopSeqno});
		
		wp.colSet("int_rate_mcode", colStr("int_rate_mcode"));
		wp.colSet("class_code", colStr("class_code"));
		wp.colSet("vip_code", colStr("vip_code"));
		wp.colSet("curr_pd_rating", colStr("curr_pd_rating"));
		wp.colSet("payment_no", colStr("payment_no"));
		wp.colSet("acct_status", colStr("acct_status"));
		wp.colSet("payment_rate1", colStr("payment_rate1"));
		wp.colSet("payment_rate2", colStr("payment_rate2"));
		wp.colSet("payment_rate3", colStr("payment_rate3"));
		wp.colSet("payment_rate4", colStr("payment_rate4"));
		wp.colSet("payment_rate5", colStr("payment_rate5"));
		wp.colSet("payment_rate6", colStr("payment_rate6"));
		wp.colSet("payment_rate7", colStr("payment_rate7"));
		wp.colSet("payment_rate8", colStr("payment_rate8"));
		wp.colSet("payment_rate9", colStr("payment_rate9"));
		wp.colSet("payment_rate10", colStr("payment_rate10"));
		wp.colSet("payment_rate11", colStr("payment_rate11"));
		wp.colSet("payment_rate12", colStr("payment_rate12"));
		wp.colSet("line_of_credit_amt", colNum("line_of_credit_amt"));
		wp.colSet("line_of_credit_amt_cash", colNum("line_of_credit_amt_cash"));
		wp.colSet("acct_type", colStr("acct_type"));
		wp.colSet("bill_sending_zip", colStr("bill_sending_zip"));
		wp.colSet("bill_sending_addr1", colStr("bill_sending_addr1"));
		wp.colSet("bill_sending_addr2", colStr("bill_sending_addr2"));
		wp.colSet("bill_sending_addr3", colStr("bill_sending_addr3"));
		wp.colSet("bill_sending_addr4", colStr("bill_sending_addr4"));
		wp.colSet("bill_sending_addr5", colStr("bill_sending_addr5"));
		wp.colSet("autopay_acct_bank", colStr("autopay_acct_bank"));
		wp.colSet("autopay_acct_no", colStr("autopay_acct_no"));
		wp.colSet("wk_addr", colStr("bill_sending_addr1") + colStr("bill_sending_addr2") + colStr("bill_sending_addr3")
				+ colStr("bill_sending_addr4") + colStr("bill_sending_addr5"));
		wp.colSet("idno_of_credit_amt", colNum("idno_of_credit_amt"));

		String lsAutopayDate1 =colStr("autopay_acct_s_date");
		String lsAutopayDate2 =colStr("autopay_acct_e_date");
		if ( (notEmpty(lsAutopayDate1) && commString.strComp(lsAutopayDate1,wp.sysDate)>0) ||
		        (notEmpty(lsAutopayDate2) && commString.strComp(lsAutopayDate2,wp.sysDate)<0) ) {
		   wp.colSet("autopay_acct_bank", "");
		   wp.colSet("autopay_acct_no", "");
		   wp.colSet("tt_autopay_acct_bank", "");
		}
		
		wp.colSet("lastpay_date", "");
		wp.colSet("jrnl_lastpay_amt", "");
		
		wp.colSet("lastpay_date", colStr("last_pay_date"));
		wp.colSet("jrnl_lastpay_amt", colStr("last_pay_amt"));
		
//		wp.colSet("this_close_date", colStr("stmt_cycle_date"));
      wp.colSet("this_close_date", colStr("last_stmt_date"));
		wp.colSet("this_lastpay_date", colStr("stmt_last_payday"));
		
		return 0;
	}

	// ------------------------------------------------------------------------------
	public int readCard() {
		String sql1 = "";

		if (!ibDebit) {
			sql1 = " select " + " eng_name , " + " current_code , " + " oppost_reason , " + " oppost_date , "
					+ " new_end_date , " + " sup_flag , " + " group_code , "
					+ " uf_tt_group_code(group_code) as tt_group_code , " + " indiv_crd_lmt , " + " corp_no ,"
					+ " son_card_flag , " + " combo_acct_no , uf_tt_acct_type(acct_type) as tt_acct_type "
					+ " from crd_card " + " where card_no = ? ";
		} else {
			sql1 = " select " + " eng_name , " + " current_code , " + " oppost_reason , " + " oppost_date , "
					+ " new_end_date , " + " sup_flag , " + " group_code , "
					+ " uf_tt_group_code(group_code) as tt_group_code , " + " indiv_crd_lmt , " + " corp_no , "
					+ " acct_no as combo_acct_no , uf_tt_acct_type(acct_type) as tt_acct_type " + " from dbc_card "
					+ " where card_no = ? ";
		}

		sqlSelect(sql1, new Object[] { isCardNo });

		if (sqlRowNum <= 0) {
			errmsg("Read Card error !");
			return -1;
		}

		if (colEmpty("tt_acct_type") && colEq("acct_type", "90")) {
			wp.colSet("tt_acct_type", colStr("tt_acct_type"));
		} else {
			wp.colSet("tt_acct_type", colStr("tt_acct_type"));
		}

		if (colEq("acno_flag", "3")) {
			wp.colSet("tt_acno_flag", "商務卡個繳");
		} else if (colEq("acno_flag", "Y")) {
			wp.colSet("tt_acno_flag", "商務卡總繳");
		} else
			wp.colSet("tt_acno_flag", "");

		isSonCardFlag = colStr("son_card_flag");

		String sql2 = "";

		if (!ibDebit) {
			sql2 = " select " + " count(*) as db_cnt " + " from crd_card " + " where id_p_seqno = ? "
					+ " and current_code = '0' ";
		} else {
			sql2 = " select " + " count(*) as db_cnt " + " from dbc_card " + " where id_p_seqno = ? "
					+ " and current_code = '0' ";
		}

		sqlSelect(sql2, new Object[] { isIdPSeqno });

		wp.colSet("eng_name", colStr("eng_name"));
		wp.colSet("current_code", colStr("current_code"));
		wp.colSet("oppost_reason", colStr("oppost_reason"));
		wp.colSet("oppost_date", colStr("oppost_date"));
		wp.colSet("new_end_date", colStr("new_end_date"));
		wp.colSet("sup_flag", colStr("sup_flag"));
		wp.colSet("group_code", colStr("group_code"));
		wp.colSet("tt_group_code", colStr("tt_group_code"));
		// wp.colSet("indiv_crd_lmt", ""+col_int("indiv_crd_lmt"));
		wp.colSet("wk_valid_cnt", "" + colInt("db_cnt"));
		wp.colSet("corp_no", colStr("corp_no"));
		wp.colSet("son_card_flag", colStr("son_card_flag"));
		wp.colSet("combo_acct_no", colStr("combo_acct_no"));
		wp.colSet("indiv_crd_lmt", colStr("indiv_crd_lmt"));
		return 0;
	}

	// ------------------------------------------------------------------------------
	public int readAuthConsume() {

		String sql1 = " select "
				// + " consume_1 , "
				// + " consume_2 , "
				// + " consume_3 , "
				// + " consume_4 , "
				// + " consume_5 , "
				// + " consume_6 , "
				+ " tot_due , " + " max_consume_date , " + " tot_limit_amt , " + " pre_pay_amt , "
				// + " ibm_receive_amt , "
				+ " max_consume_amt" + " from cca_consume " + " where card_acct_idx = ? ";

		sqlSelect(sql1, new Object[] { isCardAcctIdx });

		if (sqlRowNum <= 0) {
			errmsg("Read AuthConsume error !");
			return -1;
		}

		// wp.colSet("consume_1", "" + colNum("consume_1"));
		// wp.colSet("consume_2", "" + colNum("consume_2"));
		// wp.colSet("consume_3", "" + colNum("consume_3"));
		// wp.colSet("consume_4", "" + colNum("consume_4"));
		// wp.colSet("consume_5", "" + colNum("consume_5"));
		// wp.colSet("consume_6", "" + colNum("consume_6"));
		wp.colSet("tot_due", "" + colNum("tot_due"));
		wp.colSet("max_consume_date", colStr("max_consume_date"));
		wp.colSet("max_consume_amt", colStr("max_consume_amt"));
		wp.colSet("tot_limit_amt", "" + colNum("tot_limit_amt"));
		wp.colSet("pre_pay_amt", "" + colNum("pre_pay_amt"));
		// wp.colSet("ibm_receive_amt", colStr("ibm_receive_amt"));
		return 0;
	}

	void selectActAcctSum() {
		// 呆帳: ${paid_annual_fee.(999)} 呆帳: ${unpaid_annual_fee.(999)}
		wp.colSet("paid_annual_fee", 0);
		wp.colSet("unpaid_annual_fee", 0);
		// 費用: ${paid_srv_fee.(999)} 費用: ${unpaid_srv_fee.(999)}
		wp.colSet("paid_srv_fee", 0);
		wp.colSet("unpaid_srv_fee", 0);
		// 催收: ${paid_law_fee.(999)} 催收: ${unpaid_law_fee.(999)}
		wp.colSet("paid_law_fee", 0);
		wp.colSet("unpaid_law_fee", 0);
		// 違約金 : ${billed_end_bal.(999)} 違約金 : ${unbill_end_bal.(999)}
		wp.colSet("billed_end_bal", 0);
		wp.colSet("unbill_end_bal", 0);
		// 循環息: ${paid_interest_fee.(999)} 循環息: ${unpaid_interest_fee.(999)}
		wp.colSet("paid_interest_fee", 0);
		wp.colSet("unpaid_interest_fee", 0);
		// 消費: ${paid_consume_fee.(999)} 消費: ${unpaid_consume_fee.(999)}
		wp.colSet("paid_consume_fee", 0);
		wp.colSet("unpaid_consume_fee", 0);
		// 預借: ${paid_precash.(999)} 預借: ${unpaid_precash.(999)}
		wp.colSet("paid_precash", 0);
		wp.colSet("unpaid_precash", 0);
		if (ibDebit)
			return;

		String sql1 = "SELECT" + "  sum(decode(acct_code,'DB',billed_end_bal,0)) as paid_annual_fee"
				+ ", 0 as unpaid_annual_fee"
				+ ", sum(case when locate(acct_code,'|AF|LF|CF|PF')>1 then billed_end_bal else 0 end) as paid_srv_fee"
				+ ", sum(case when locate(acct_code,'|AF|LF|CF|PF')>1 then unbill_end_bal else 0 end) as unpaid_srv_fee"
				+ ", sum(case when locate(acct_code,'|AI|SF|CB|CI|CC')>1 then billed_end_bal else 0 end) as paid_law_fee"
				+ ", sum(case when locate(acct_code,'|AI|SF|CB|CI|CC')>1 then unbill_end_bal else 0 end) as unpaid_law_fee"
				+ ", sum(decode(acct_code,'PN',billed_end_bal,0)) as billed_end_bal"
				+ ", sum(decode(acct_code,'PN',unbill_end_bal,0)) as unbill_end_bal"
				+ ", sum(decode(acct_code,'RI',billed_end_bal,0)) as paid_interest_fee"
				+ ", sum(decode(acct_code,'RI',unbill_end_bal,0)) as unpaid_interest_fee"
				+ ", sum(case when locate(acct_code,'|BL|ID|IT|OT|AO')>1 then billed_end_bal else 0 end) as paid_consume_fee"
				+ ", sum(case when locate(acct_code,'|BL|ID|IT|OT|AO')>1 then unbill_end_bal else 0 end) as unpaid_consume_fee"
				+ ", sum(decode(acct_code,'CA',billed_end_bal,0)) as paid_precash"
				+ ", sum(decode(acct_code,'CA',unbill_end_bal,0)) as unpaid_precash" + " from act_acct_sum"
				+ " where 1=1";
		if (commString.strIn2(isAcnoFlag, "1,3")) {
			sql1 += " and p_seqno =?";
			setString2(1, isAcctpSeqno);
		} else {
			sql1 += " and p_seqno in (select p_seqno from act_acno where corp_p_seqno =? and acct_type =?)";
			setString2(1, isCorpPSeqno);
			setString(isAcctType);
		}
		sqlSelect(sql1);
		if (sqlRowNum > 0) {
			// 呆帳: ${paid_annual_fee.(999)} 呆帳: ${unpaid_annual_fee.(999)}
			wp.colSet("paid_annual_fee", colNum("paid_annual_fee"));
			// wp.colSet("unpaid_annual_fee", colNum("unpaid_annual_fee"));
			// 費用: ${paid_srv_fee.(999)} 費用: ${unpaid_srv_fee.(999)}
			wp.colSet("paid_srv_fee", colNum("paid_srv_fee"));
			wp.colSet("unpaid_srv_fee", colNum("unpaid_srv_fee"));
			// 催收: ${paid_law_fee.(999)} 催收: ${unpaid_law_fee.(999)}
			wp.colSet("paid_law_fee", colNum("paid_law_fee"));
			wp.colSet("unpaid_law_fee", colNum("unpaid_law_fee"));
			// 違約金 : ${billed_end_bal.(999)} 違約金 : ${unbill_end_bal.(999)}
			wp.colSet("billed_end_bal", colNum("billed_end_bal"));
			wp.colSet("unbill_end_bal", colNum("unbill_end_bal"));
			// 循環息: ${paid_interest_fee.(999)} 循環息: ${unpaid_interest_fee.(999)}
			wp.colSet("paid_interest_fee", colNum("paid_interest_fee"));
			wp.colSet("unpaid_interest_fee", colNum("unpaid_interest_fee"));
			// 消費: ${paid_consume_fee.(999)} 消費: ${unpaid_consume_fee.(999)}
			wp.colSet("paid_consume_fee", colNum("paid_consume_fee"));
			wp.colSet("unpaid_consume_fee", colNum("unpaid_consume_fee"));
			// 預借: ${paid_precash.(999)} 預借: ${unpaid_precash.(999)}
			wp.colSet("paid_precash", colNum("paid_precash"));
			wp.colSet("unpaid_precash", colNum("unpaid_precash"));
		}

		// -呆帳.未結帳-
		// act_acct.SUM(NVL(ttl_amt_bal,0)) by p_seqno or p_seqno in
		// (acct_type,corp_p_seqno)
		
		//--V1.00.06 修正 Sql error 部分 , acct_status 位於 act_acno 而非 act_acct		
//		strSql = "select sum(ttl_amt_bal) as unpaid_annual_fee" + " from act_acct" + " where p_seqno =?"
//				+ " and acct_status ='4'";
		
		strSql = "select sum(ttl_amt_bal) as unpaid_annual_fee from act_acct where p_seqno in "
				+ "(select p_seqno from act_acno where acno_p_seqno =? and acct_status ='4') ";
		
		setString2(1, isAcctpSeqno);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			wp.colSet("unpaid_annual_fee", colNum("unpaid_annual_fee"));
		}
	}

	// ------------------------------------------------------------------------------
	void selectActDebt() {
		// 呆帳: ${paid_annual_fee.(999)} 呆帳: ${unpaid_annual_fee.(999)}
		wp.colSet("paid_annual_fee", 0);
		wp.colSet("unpaid_annual_fee", 0);
		// 費用: ${paid_srv_fee.(999)} 費用: ${unpaid_srv_fee.(999)}
		wp.colSet("paid_srv_fee", 0);
		wp.colSet("unpaid_srv_fee", 0);
		// 催收: ${paid_law_fee.(999)} 催收: ${unpaid_law_fee.(999)}
		wp.colSet("paid_law_fee", 0);
		wp.colSet("unpaid_law_fee", 0);
		// 違約金 : ${billed_end_bal.(999)} 違約金 : ${unbill_end_bal.(999)}
		wp.colSet("billed_end_bal", 0);
		wp.colSet("unbill_end_bal", 0);
		// 循環息: ${paid_interest_fee.(999)} 循環息: ${unpaid_interest_fee.(999)}
		wp.colSet("paid_interest_fee", 0);
		wp.colSet("unpaid_interest_fee", 0);
		// 消費: ${paid_consume_fee.(999)} 消費: ${unpaid_consume_fee.(999)}
		wp.colSet("paid_consume_fee", 0);
		wp.colSet("unpaid_consume_fee", 0);
		// 預借: ${paid_precash.(999)} 預借: ${unpaid_precash.(999)}
		wp.colSet("paid_precash", 0);
		wp.colSet("unpaid_precash", 0);
		if (ibDebit)
			return;

		// -- acct_month <=this_acct_month :billed , acct_month > this_acct_month
		// :unbilled
		String sql1 = " select " + " sum(decode(acct_code,'DB',end_bal,0)) as paid_annual_fee , "
				+ " sum(case when locate(acct_code,'|AF|LF|CF|PF')>1 then end_bal else 0 end) as paid_srv_fee , "
				+ " sum(case when locate(acct_code,'|AI|SF|CB|CI|CC')>1 then end_bal else 0 end) as paid_law_fee , "
				+ " sum(decode(acct_code,'PN',end_bal,0)) as billed_end_bal , "
				+ " sum(decode(acct_code,'RI',end_bal,0)) as paid_interest_fee , "
				+ " sum(case when locate(acct_code,'|BL|ID|IT|OT|AO')>1 then end_bal else 0 end) as paid_consume_fee , "
				+ " sum(decode(acct_code,'CA',end_bal,0)) as paid_precash "
				+ " from act_debt where 1=1 and p_seqno = ? and acct_month <= ? ";

		String sql2 = " select "
				+ " sum(case when locate(acct_code,'|AF|LF|CF|PF')>1 then end_bal else 0 end) as unpaid_srv_fee , "
				+ " sum(case when locate(acct_code,'|AI|SF|CB|CI|CC')>1 then end_bal else 0 end) as unpaid_law_fee , "
				+ " sum(decode(acct_code,'PN',end_bal,0)) as unbill_end_bal , "
				+ " sum(decode(acct_code,'RI',end_bal,0)) as unpaid_interest_fee , "
				+ " sum(case when locate(acct_code,'|BL|ID|IT|OT|AO')>1 then end_bal else 0 end) as unpaid_consume_fee , "
				+ " sum(decode(acct_code,'CA',end_bal,0)) as unpaid_precash "
				+ " from act_debt where 1=1 and p_seqno = ? and acct_month > ? ";

		sqlSelect(sql1, new Object[] { isAcctpSeqno, wp.colStr("acct_month") });
		sqlSelect(sql2, new Object[] { isAcctpSeqno, wp.colStr("acct_month") });

		// 呆帳: ${paid_annual_fee.(999)} 呆帳: ${unpaid_annual_fee.(999)}
		wp.colSet("paid_annual_fee", colNum("paid_annual_fee"));
		// wp.colSet("unpaid_annual_fee", colNum("unpaid_annual_fee"));
		// 費用: ${paid_srv_fee.(999)} 費用: ${unpaid_srv_fee.(999)}
		wp.colSet("paid_srv_fee", colNum("paid_srv_fee"));
		wp.colSet("unpaid_srv_fee", colNum("unpaid_srv_fee"));
		// 催收: ${paid_law_fee.(999)} 催收: ${unpaid_law_fee.(999)}
		wp.colSet("paid_law_fee", colNum("paid_law_fee"));
		wp.colSet("unpaid_law_fee", colNum("unpaid_law_fee"));
		// 違約金 : ${billed_end_bal.(999)} 違約金 : ${unbill_end_bal.(999)}
		wp.colSet("billed_end_bal", colNum("billed_end_bal"));
		wp.colSet("unbill_end_bal", colNum("unbill_end_bal"));
		// 循環息: ${paid_interest_fee.(999)} 循環息: ${unpaid_interest_fee.(999)}
		wp.colSet("paid_interest_fee", colNum("paid_interest_fee"));
		wp.colSet("unpaid_interest_fee", colNum("unpaid_interest_fee"));
		// 消費: ${paid_consume_fee.(999)} 消費: ${unpaid_consume_fee.(999)}
		wp.colSet("paid_consume_fee", colNum("paid_consume_fee"));
		wp.colSet("unpaid_consume_fee", colNum("unpaid_consume_fee"));
		// 預借: ${paid_precash.(999)} 預借: ${unpaid_precash.(999)}
		wp.colSet("paid_precash", colNum("paid_precash"));
		wp.colSet("unpaid_precash", colNum("unpaid_precash"));

		// -呆帳.未結帳-
		// act_acct.SUM(NVL(ttl_amt_bal,0)) by p_seqno or p_seqno in
		// (acct_type,corp_p_seqno)
		
		//--V1.00.06 修正 Sql error 部分 , acct_status 位於 act_acno 而非 act_acct		
//		strSql = "select sum(ttl_amt_bal) as unpaid_annual_fee" + " from act_acct" + " where p_seqno =?"
//				+ " and acct_status ='4'";
		
		strSql = "select sum(ttl_amt_bal) as unpaid_annual_fee from act_acct where p_seqno in "
				+ "(select p_seqno from act_acno where acno_p_seqno =? and acct_status ='4') ";
		
		setString2(1, isAcctpSeqno);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			wp.colSet("unpaid_annual_fee", colNum("unpaid_annual_fee"));
		}

	}

	// ------------------------------------------------------------------------------
	public int readAdjParm() {

		String sql1 = " select " + " adj_month_amt " + " from cca_adj_parm " + " where " + " card_acct_idx = ? ";

		sqlSelect(sql1, new Object[] { isCardAcctIdx });

		if (sqlRowNum < 0) {
			errmsg("Read AdjParm error !");
			return -1;
		}

		if (sqlRowNum == 0)
			wp.colSet("adj_month_amt", "0");
		else
			wp.colSet("adj_month_amt", colNum("adj_month_amt"));

		return 0;
	}

	// ------------------------------------------------------------------------------
	public int readCorp() {

		if (empty(isCorpPSeqno))
			return 0;

		String sql1 = "";

		sql1 = " select " + " chi_name as corp_chi_name " + " from crd_corp " + " where corp_p_seqno = ? ";

		sqlSelect(sql1, new Object[] { isCorpPSeqno });

		if (sqlRowNum <= 0) {
			errmsg("Read Corp error !");
			return -1;
		}

		wp.colSet("maj_corp_name", colStr("corp_chi_name"));

		return 0;
	}

	// ------------------------------------------------------------------------------
	public int readCardBase() {
		if (!empty(wp.itemStr("ex_card_no")))
			isCardNo = wp.itemStr("ex_card_no");
		else
			isCardNo = wp.itemStr("kk_card_no");

		ibDebit = wp.itemEq("debit_flag", "Y");

		// ----
		String sql1 = "";

		if (!ibDebit) {
			sql1 = " select" + " hex(B.rowid) as rowid , B.mod_seqno," + " A.Bin_type," + " A.card_no ,"
					+ " to_char(sysdate + 3 months,'yyyymmdd') as spec_del_date ," + " A.card_type ,"
					+ " A.bank_actno ," + " B.spec_status, B.spec_del_date," + "uf_corp_no(A.corp_p_seqno) as corp_no ,"
					+ "uf_idno_id(A.id_p_seqno) as id_no ," + " B.spec_mst_vip_amt ," + " B.spec_remark,"
					+ " B.spec_user," + " B.spec_date, " + " to_char(A.mod_time ,'yyyymmdd') as mod_date,"
					+ " A.mod_user," + " A.mod_pgm," + " A.new_end_date , " + " 'N' as debit_flag ," + " A.crt_date , "
					+ " A.crt_user " + ", uf_spec_status(B.spec_status,B.spec_del_date) as card_spec_status"
					+ " from crd_card A , cca_card_base B" + " where A.card_no = B.card_no " + " and A.card_no = ? ";
		} else {
			sql1 = " select" + " hex(B.rowid) as rowid , B.mod_seqno," + " A.Bin_type," + " A.card_no ,"
					+ " to_char(sysdate + 3 months,'yyyymmdd') as spec_del_date ," + " A.card_type ,"
					+ " A.bank_actno ," + " B.spec_status, B.spec_del_date, "
					+ "uf_corp_no(A.corp_p_seqno) as corp_no ," + "uf_vd_idno_id(A.id_p_seqno) as id_no ,"
					+ " B.spec_mst_vip_amt ," + " B.spec_remark," + " B.spec_user," + " B.spec_date, "
					+ " to_char(A.mod_time ,'yyyymmdd') as mod_date," + " A.mod_user," + " A.mod_pgm,"
					+ " A.new_end_date ," + " 'Y' as debit_flag , " + " A.crt_date , " + " A.crt_user "
					+ ", uf_spec_status(B.spec_status,B.spec_del_date) as card_spec_status"
					+ " from dbc_card A , cca_card_base B" + " where 1=1 and A.card_no = B.card_no "
					+ " and A.card_no = ? ";
		}

		sqlSelect(sql1, new Object[] { isCardNo });
		if (sqlRowNum <= 0) {
			errmsg("Read CardBase error !");
			return -1;
		}

		wp.colSet("id_no", colStr("id_no"));
		wp.colSet("card_no", colStr("card_no"));
		wp.colSet("bin_type", colStr("bin_type"));
		wp.colSet("debit_flag", colStr("debit_flag"));
		wp.colSet("spec_del_date", colStr("spec_del_date"));
		wp.colSet("card_type", colStr("card_type"));
		wp.colSet("bank_actno", colStr("bank_actno"));
		wp.colSet("card_spec_status", colStr("card_spec_status"));
		wp.colSet("spec_mst_vip_amt", colStr("spec_mst_vip_amt"));
		wp.colSet("spec_remark", colStr("spec_remark"));
		wp.colSet("spec_user_id", colStr("spec_user_id"));
		wp.colSet("spec_date", colStr("spec_date"));
		wp.colSet("mod_user", colStr("mod_user"));
		wp.colSet("mod_date", colStr("mod_date"));
		wp.colSet("crt_user", colStr("crt_user"));
		wp.colSet("crt_date", colStr("crt_date"));

		return 0;
	}

	// ----------------------------------------------------------------------------
	public int readCrdIdnoExt() {
		String sql1 = "";
		if (!ibDebit) {
			sql1 = " select " + " asig_inq1_cname ," + " asig_inq1_idno ," + " asig_inq2_cname ," + " asig_inq2_idno ,"
					+ " asig_inq3_cname ," + " asig_inq3_idno " + " from crd_idno_ext " + " where id_p_seqno = ? ";
		} else {
			sql1 = " select " + " asig_inq1_cname ," + " asig_inq1_idno ," + " asig_inq2_cname ," + " asig_inq2_idno ,"
					+ " asig_inq3_cname ," + " asig_inq3_idno " + " from dbc_idno_ext " + " where id_p_seqno = ? ";
		}

		sqlSelect(sql1, new Object[] { isIdPSeqno });

		if (sqlRowNum < 0) {
			errmsg("Read IDNO_EXT error !");
			return -1;
		}

		if (empty(colStr("asig_inq1_cname")) && empty(colStr("asig_inq1_idno")) && empty(colStr("asig_inq2_cname"))
				&& empty(colStr("asig_inq2_idno")) && empty(colStr("asig_inq3_cname"))
				&& empty(colStr("asig_inq3_idno"))) {
			wp.colSet("wk_asig", "N");
			return 0;
		}

		wp.colSet("wk_asig", "Y");

		return 0;
	}
	
	// ----------------------------------------------------------------------------
	int readAnalSub() {		
		wp.colSet("yy1_consum_amt", 0);  //含消費+預借現金
		wp.colSet("max_consume_date", "");
		wp.colSet("max_consume_amt", 0);
		//--本年累計:
		wp.colSet("yy0_consum_amt","0");

		String sql1 = " select acct_month ,"
		       		+ " his_purchase_amt as purch_amt, his_cash_amt as purch_cash "
		       		+ " from act_anal_sub "
		       		+ " where p_seqno =?"
		       		+ " order by acct_month desc "+commSqlStr.rownum(12)
		       		;
		
		
		sqlSelect(sql1,new Object[]{isAcctpSeqno});
		if(sqlRowNum<=0) {
			wp.colSet("tot_due", "0");
			wp.colSet("tot_limit_amt", "0");
			wp.colSet("max_consume_date", "");
			wp.colSet("max_consume_amt", "0");		
			return 0;
		}
			
		//最近1年:
		String lsAmtYy="", lsCashYy="";
		double lmAmtMax=0, lmCashMax=0;
		double lmConsumAmt = 0;
		for(int ii=0;ii<12;ii++){
		   double lm_cash =colNum(ii,"purch_cash");
		   double lm_amt =colNum(ii,"purch_amt")+lm_cash;
		   lmConsumAmt += lm_amt;
		   if (lm_amt >lmAmtMax) {
			   lmAmtMax =lm_amt;
		      lsAmtYy =colStr(ii, "acct_month");
		   }
		   if (lm_cash >lmCashMax) {
			   lmCashMax =lm_cash;
		      lsCashYy =colStr(ii, "acct_month");
		   }
		}
		wp.colSet("yy1_consum_amt", lmConsumAmt);  //含消費+預借現金
		wp.colSet("max_consume_date", commDate.monthAdd(lsAmtYy,1));
		wp.colSet("max_consume_amt", lmAmtMax);
		   
		//--本年累計:
		sql1 = " select "
		     + " sum(his_purchase_amt+his_cash_amt) as yy_consum_amt "
		     + " from act_anal_sub "
		     + " where p_seqno =?"
		     + " and acct_month between ? and ?";
		String lsYm02 =commString.left(wp.sysDate,4)+"11";
		String lsYm01 =commDate.monthAdd(lsYm02,-11);
		sqlSelect(sql1,new Object[]{isAcctpSeqno , lsYm01, lsYm02});
		if (sqlRowNum >0) {
			wp.colSet("yy0_consum_amt",colNum("yy_consum_amt"));
		}
		   
		return 1;
	}
	
	// ----------------------------------------------------------------------------
	int readBilContract() {
		String sql3 = " select sum(" + " (install_tot_term - install_curr_term) * unit_price"
				+ " + remd_amt + decode(install_curr_term,0,first_remd_amt+extra_fees,0)" + " ) as db_inst_unpost "
				+ " from bil_contract " + " where p_seqno = ? " + " and install_tot_term <> install_curr_term "
				// + " and contract_kind = '1' "
//				+ " and auth_code not in ('','N','REJECT','P','reject') "
				+ " and (post_cycle_dd >0 or installment_kind ='F')";
		sqlSelect(sql3, new Object[] { isAcctpSeqno });
		wp.colSet("db_inst_unpost", colStr("db_inst_unpost"));
		if (sqlRowNum <= 0) {
			wp.colSet("db_inst_unpost", 0);
			errmsg("select bil_contract error");
			return -1;
		}

		// --
		// sql3 = " select "
		// + " sum((A.install_tot_term - A.install_curr_term) * A.unit_price +
		// A.remd_amt +
		// decode(A.install_curr_term,0,A.first_remd_amt+A.extra_fees,0)) as
		// db_inst_unpost2 "
		// + " from bil_contract A join bil_merchant B on B.mcht_no=A.mcht_no"
		// + " where A.p_seqno = ? "
		// + " and A.install_tot_term <> A.install_curr_term "
		// + " and A.contract_kind = '1' "
		// + " and A.auth_code not in ('','N','REJECT','P','reject') "
		// + " and A.post_cycle_dd > 0"
		// +" and B.loan_flag in ('','N','C')";
		// sqlSelect(sql3, new Object[]{is_acctp_seqno});
		// wp.colSet("db_inst_unpost2", colStr("db_inst_unpost2"));
		// if (sqlRowNum <= 0) {
		// wp.colSet("db_inst_unpost2", 0);
		// errmsg("select bil_contract error");
		// return -1;
		// }

		return 1;
	}
	
	//-----------------------------------------------------------------------------
	int readBilContractPhase3() {		
		String sql3 = " select sum((install_tot_term - install_curr_term) * unit_price " 
					+ " + remd_amt + decode(install_curr_term,0,first_remd_amt+extra_fees,0)) as db_inst_unpost "
			        + " from bil_contract "
			        + " where p_seqno = ? "
//			        + " and auth_code not in ('','N','REJECT','P','reject','LOAN') "
			        +" AND install_tot_term <> install_curr_term "
			        + " and (((post_cycle_dd >0 or installment_kind ='F')) "
			        + " OR (post_cycle_dd=0 AND DELV_CONFIRM_FLAG='Y' AND auth_code='DEBT') "
			        +" )"
			        ;

		sqlSelect(sql3, new Object[]{isAcctpSeqno});   
		if(ibDebit) {
			wp.colSet("db_inst_unpost", "0");
		} else {
			wp.colSet("db_inst_unpost", colStr("db_inst_unpost"));
		}
		if (sqlRowNum <= 0) {				
			wp.colSet("db_inst_unpost", 0);
			errmsg("select bil_contract error");
			return -1;
		}		
		return 1;		
	}
	
	// ----------------------------------------------------------------------------
	public int readRemarkLog() {
		String sql3 = " select " + " auth_remark " + " from cca_auth_remark_log " + " where card_acct_idx = ? "
				+ " order by chg_date Desc , chg_time Desc " + commSqlStr.rownum(1);
		sqlSelect(sql3, new Object[] { isCardAcctIdx });
		if (sqlRowNum < 0) {
			errmsg("select cca_auth_remark_log error");
			return -1;
		}
		wp.colSet("auth_remark", colStr("auth_remark"));
		return 0;
	}

	// ---------------------------------------------------------------------------
	public void selectCrdCorrelate() {
		String sql1 = " select " + " fh_flag " + " from crd_correlate " + " where correlate_id = ? ";

		sqlSelect(sql1, new Object[] { wp.colStr("id_no") });

		if (sqlRowNum <= 0) {
			return;
		}

		wp.colSet("fh_flag", colStr("fh_flag"));

		return;
	}

	// ---------------------------------------------------------------------------
	void selectActJrnl() {
		if (ibDebit)
			return;

		// -最近繳款日,金額-
		daoTid = "jrnl.";
		strSql = "select interest_date, tran_type, transaction_amt" + " from act_jrnl" + " where p_seqno =?"
				+ " and tran_type in (select payment_type from ptr_payment where fund_flag<>'Y')"
				+ " order by interest_date desc" + commSqlStr.rownum(1);
		setString2(1, isAcctpSeqno);
		sqlSelect(strSql);
		if (sqlRowNum <= 0) {
			wp.colSet("jrnl_lastpay_date", "");
			wp.colSet("jrnl_lastpay_amt", "0");
		} else {
			wp.colSet("jrnl_lastpay_date", colStr("jrnl.interest_date"));
			wp.colSet("jrnl_lastpay_amt", colStr("jrnl.transaction_amt"));
		}
	}

	void selectActAcct() {
		String sql1 = "select last_payment_date , acct_jrnl_bal , min_pay_bal , end_bal_op+end_bal_lk as wk_oplk"
				+ ", ttl_amt_bal, ttl_amt" + " from act_acct where p_seqno = ? ";
		sqlSelect(sql1, new Object[] { isAcctpSeqno });
		if (sqlRowNum > 0) {
//			wp.colSet("lastpay_date", colStr("last_payment_date"));
			wp.colSet("acct_jrnl_bal", colStr("acct_jrnl_bal"));
			wp.colSet("min_pay_bal", colStr("min_pay_bal"));
			wp.colSet("wk_oplk", colStr("wk_oplk"));
			if(colNum("acct_jrnl_bal") <=0) {
				wp.colSet("ttl_amt_bal", "0");
			}	else	{
				wp.colSet("ttl_amt_bal", colStr("acct_jrnl_bal"));
			}
			
			// wp.colSet("ttl_amt_bal", colStr("ttl_amt_bal"));
			// wp.colSet("ttl_amt_bal", colStr("ttl_amt"));
		} else {
//			wp.colSet("lastpay_date", "");
			wp.colSet("acct_jrnl_bal", "0");
			wp.colSet("min_pay_bal", "0");
			wp.colSet("wk_oplk", "0");
			wp.colSet("ttl_amt_bal", "0");
		}
	}

	void selectCrdReturn() {
		String sql1 = "";
		if (ibDebit) {
			sql1 = "select decode(proc_status,'1','處理中','2','銷毀','3','寄出','4','申停') as tt_return_status"
					+ ", return_date" + " from dbc_return_log where card_no =? order by return_date Desc "
					+ commSqlStr.rownum(1);
		} else {
			sql1 = " select decode(proc_status,'1','庫存','2','銷毀','3','寄出','4','申停','5','重製','6','寄出不封裝') as tt_return_status"
					+ ", return_date" + " from crd_return where card_no =? " + " order by return_date desc "
					+ commSqlStr.rownum(1);
		}

		sqlSelect(sql1, new Object[] { isCardNo });
		if (sqlRowNum <= 0)
			return;

		wp.colSet("tt_return_status", colStr("tt_return_status"));
		wp.colSet("card_return_date", colStr("return_date"));
	}

	void selectXxxCard(String aCardNo) {
		wp.colSet("tsc_autoload_flag", "");
		if (empty(aCardNo) || ibDebit)
			return;

		// -悠遊卡-
		strSql = "select autoload_flag" + " from tsc_card" + " where card_no =?" + " order by crt_date desc"
				+ commSqlStr.rownum(1);
		setString2(1, aCardNo);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			wp.colSet("tsc_autoload_flag", colStr("autoload_flag"));
			return;
		}

		// --i-Pass-
		strSql = "select autoload_flag" + " from ips_card" + " where card_no =?" + " order by crt_date desc"
				+ commSqlStr.rownum(1);
		setString2(1, aCardNo);
		sqlSelect(strSql);
		if (sqlRowNum > 0) {
			wp.colSet("tsc_autoload_flag", colStr("autoload_flag"));
			return;
		}
		
		//-- icash
		strSql = "select autoload_flag from ich_card where card_no = ? order by crt_date Desc " +commSqlStr.rownum(1);
		setString2(1, aCardNo);
		sqlSelect(strSql);
		if(sqlRowNum > 0) {
			wp.colSet("tsc_autoload_flag", colStr("autoload_flag"));
		    return;
		}
	}

	// -商務卡可用餘額------------------------------------------------------------------
	void wfCorpCanuseLimit(String aCorpPseqno) {
		wp.colSet("ex_corp_limit", "");
		if (empty(aCorpPseqno) || ibDebit)
			return;

		daoTid = "acno.";
		strSql = "select distinct A.card_acct_idx, A.tot_amt_month" + ", a.adj_quota" + ", a.adj_eff_start_date"
				+ ", a.adj_eff_end_date" + ", a.tot_amt_consume" + ", C.line_of_credit_amt" + ", C.corp_p_seqno"
				+ ", (B.paid_consume_fee + B.unpaid_consume_fee"
				+ " + B.paid_precash +B.unpaid_precash +B.unpost_inst_fee) as db_unpaid_amt"
				+ " from cca_card_acct A join act_acno C on A.acno_p_seqno =C.acno_p_seqno and A.debit_flag<>'Y'"
				+ "  left join cca_consume B on B.card_acct_idx =A.card_acct_idx"
				+ " where C.corp_p_seqno =? and C.acct_key like '%000'" + " and C.acno_flag ='2'";
		setString2(1, aCorpPseqno);
		sqlSelect(strSql);
		if (sqlRowNum <= 0)
			return;
		double lmTotLimit = 0;
		if (eq(colStr("acno.adj_quota"), "Y") && commDate.sysComp(colStr("acno.adj_eff_start_date")) >= 0
				&& commDate.sysComp(colStr("acno.adj_eff_end_date")) <= 0) {
			lmTotLimit = colNum("acno.tot_amt_month");
		} else {
			lmTotLimit = colNum("acno.line_of_credit_amt");
		}
		lmTotLimit = lmTotLimit - colNum("acno.db_unpaid_amt") - colNum("acno.tot_amt_consume");
		wp.colSet("ex_corp_limit", lmTotLimit);
	}

	// --修改 Mcode / 卡人等級 ----------------------------------------------------------
	public int readChgMcode() {
		isAcnopSeqno = wp.itemStr("acno_p_seqno");
		ibDebit = wp.itemEq("debit_flag", "Y");

		String sql1 = " select" + " ccas_mcode , " + " ccas_mcode as ori_ccas_mcode ," + " mcode_valid_date , "
				+ " mcode_chg_user , " + " mcode_chg_date , " + " mcode_chg_time , " + " ccas_class_code , "
				+ " ccas_class_code as ori_ccas_class_code , " + " class_valid_date , " + " class_chg_user , "
				+ " class_chg_date ," + " class_chg_time , " + " acno_p_seqno , " + " debit_flag , " + " card_acct_idx "
				+ " from cca_card_acct " + " where acno_p_seqno = ? ";
		sql1 += " and debit_flag " + (ibDebit ? "='Y'" : "<>'Y'");

		sqlSelect(sql1, new Object[] { isAcnopSeqno });

		if (sqlRowNum <= 0) {
			errmsg("Read ChgMcode error !");
			return -1;
		}
		wp.colSet("ccas_mcode", colStr("ccas_mcode"));
		if (empty(colStr("mcode_valid_date"))) {
			wp.colSet("mcode_valid_date", this.getSysDate());
		} else {
			wp.colSet("mcode_valid_date", colStr("mcode_valid_date"));
		}
		wp.colSet("mcode_chg_user", colStr("mcode_chg_user"));
		wp.colSet("mcode_chg_date", colStr("mcode_chg_date"));
		wp.colSet("mcode_chg_time", colStr("mcode_chg_time"));
		wp.colSet("ccas_class_code", colStr("ccas_class_code"));
		if (empty(colStr("class_valid_date"))) {
			wp.colSet("class_valid_date", this.getSysDate());
		} else {
			wp.colSet("class_valid_date", colStr("class_valid_date"));
		}
		wp.colSet("class_chg_user", colStr("class_chg_user"));
		wp.colSet("class_chg_date", colStr("class_chg_date"));
		wp.colSet("class_chg_time", colStr("class_chg_time"));
		wp.colSet("acno_p_seqno", colStr("acno_p_seqno"));
		wp.colSet("debit_flag", colStr("debit_flag"));
		wp.colSet("ori_ccas_class_code", colStr("ori_ccas_class_code"));
		wp.colSet("ori_ccas_mcode", colStr("ori_ccas_mcode"));
		wp.colSet("wk_sysdate_30", commDate.dateAdd(getSysDate(), 0, 0, 30));
		wp.colSet("card_acct_idx", colStr("card_acct_idx"));
		return 0;
	}

	// --dataCheck For Change Mcode
	void dataCheckMcode() {
		isAcnopSeqno = wp.itemStr("acno_p_seqno");
		ibDebit = wp.itemEq("debit_flag", "Y");

		if (empty(wp.itemStr("opt1")) && empty(wp.itemStr("opt2"))) {
			errmsg("請勾選修改項目 !");
			return;
		}

		if (!empty(wp.itemStr("ccas_mcode"))) {
			if (wp.itemStr("ccas_mcode").length() != 2) {
				errmsg("授權Mcode 需為空白 或是 2位數字 !");
				return;
			}
		}

	}

	// --Update
	public int updateMC() {
		actionInit("U");
		dataCheckMcode();

		if (rc != 1)
			return rc;

		if (!empty(wp.itemStr("opt1"))) {
			updateMcode();
			if (rc != 1)
				return rc;
			insertRskAcnoLogMcode();
		}

		if (!empty(wp.itemStr("opt2"))) {
			updateClassCode();
			if (rc != 1)
				return rc;
			insertRskAcnoLogClassCode();
		}

		return rc;
	}

	// --
	public int updateMcode() {
		msgOK();

		strSql = " update cca_card_acct set " + " ccas_mcode =:ccas_mcode , "
				+ " mcode_valid_date =:mcode_valid_date , " + " mcode_chg_user =:mcode_chg_user , "
				+ " mcode_chg_date = to_char(sysdate,'yyyymmdd') , " + " mcode_chg_time = to_char(sysdate,'hh24miss') "
				+ " where card_acct_idx =:kk_acct_idx ";

		item2ParmStr("ccas_mcode");
		item2ParmStr("mcode_valid_date");
		setString("mcode_chg_user", modUser);
		setDouble2("kk_acct_idx", wp.colNum("card_acct_idx"));

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg("update Mcode error !");
		}

		return rc;
	}

	// --
	public int updateClassCode() {
		msgOK();

		strSql = " update cca_card_acct set " + " ccas_class_code =:ccas_class_code , "
				+ " class_valid_date =:class_valid_date , " + " class_chg_user =:class_chg_user , "
				+ " class_chg_date = to_char(sysdate,'yyyymmdd') , " + " class_chg_time = to_char(sysdate,'hh24miss') "
				+ " where card_acct_idx =:kk_ccas_idx";

		item2ParmStr("ccas_class_code");
		item2ParmStr("class_valid_date");
		setString("class_chg_user", wp.loginUser);
		setDouble2("kk_ccas_idx", wp.colNum("card_acct_idx"));

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg("update ClassCode error !");
		}

		return rc;
	}

	public int insertRskAcnoLogMcode() {
		msgOK();
		sql2Insert("rsk_acnolog");
		addsqlParm("?", "kind_flag", "A");
		addsqlParm(",?", ", card_no", wp.itemStr2("wk_card_no"));
		addsqlParm(",?", ", acno_p_seqno", wp.itemStr2("acno_p_seqno"));
		addsqlParm(",?", ", acct_type", wp.itemStr2("acct_type"));
		addsqlParm(",?", ", id_p_seqno", wp.itemStr2("id_p_seqno"));
		addsqlParm(",?", ", corp_p_seqno", wp.itemStr2("corp_p_seqno"));
		addsqlYmd(", log_date");
		addsqlParm(",?", ", log_mode", "1");
		addsqlParm(",?", ", log_type", "B");
		addsqlParm(",?", ", ccas_mcode_bef", wp.itemStr2("ori_ccas_mcode"));
		addsqlParm(",?", ", ccas_mcode_aft", wp.itemStr2("ccas_mcode"));
		addsqlParm(",?", ", mcode_valid_date", wp.itemStr2("mcode_valid_date"));
		addsqlParm(",?", ", apr_flag", "Y");
		addsqlParm(",?", ", apr_user", modUser);
		addsqlYmd(", apr_date");
		addsqlModXXX(modUser, modPgm);

		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert rsk_acnolog-Mcode error");
		}

		return rc;
	}

	public int insertRskAcnoLogClassCode() {
		msgOK();
		sql2Insert("rsk_acnolog");
		addsqlParm("?", "kind_flag", "A");
		addsqlParm(",?", ", card_no", wp.itemStr2("wk_card_no"));
		addsqlParm(",?", ", acno_p_seqno", wp.itemStr2("acno_p_seqno"));
		addsqlParm(",?", ", acct_type", wp.itemStr2("acct_type"));
		addsqlParm(",?", ", id_p_seqno", wp.itemStr2("id_p_seqno"));
		addsqlParm(",?", ", corp_p_seqno", wp.itemStr2("corp_p_seqno"));
		addsqlYmd(", log_date");
		addsqlParm(",?", ", log_mode", "1");
		addsqlParm(",?", ", log_type", "A");
		addsqlParm(",?", ", class_code_bef", wp.itemStr2("ori_ccas_class_code"));
		addsqlParm(",?", ", class_code_aft", wp.itemStr2("ccas_class_code"));
		addsqlParm(",?", ", class_valid_date", wp.itemStr2("class_valid_date"));
		addsqlParm(",?", ", apr_flag", "Y");
		addsqlParm(",?", ", apr_user", modUser);
		addsqlYmd(", apr_date");
		addsqlModXXX(modUser, modPgm);

		sqlExec(sqlStmt(), sqlParms());
		if (sqlRowNum <= 0) {
			errmsg("insert rsk_acnolog-class_code error");
		}

		return rc;
	}

	// --修改特殊事項-----------------------------------------------------------------

	public int deleteNote() {
		msgOK();

		strSql = "delete cca_id_note where rowid =:rowid ";
		this.setRowId("rowid", varsStr("rowid"));

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg("delete cca_id_note error !");
		}

		return rc;
	}

	// --

	public int insertNote() {
		msgOK();

		strSql = " insert into cca_id_note ( " + " id_no , " + " id_note , " + " close_date , " + " crt_date , "
				+ " crt_user , " + " mod_time , " + " mod_pgm " + " ) values ( " + " :id_no , " + " :id_note , "
				+ " :close_date , " + " to_char(sysdate,'yyyymmdd') , " + " :crt_user , " + " sysdate , " + " :mod_pgm "
				+ " ) ";

		item2ParmStr("id_no");
		var2ParmStr("id_note");
		var2ParmStr("close_date");
		setString("crt_user", wp.loginUser);
		setString("mod_pgm", "ccam2090");

		sqlExec(strSql);

		if (sqlRowNum <= 0) {
			errmsg(" insert cca_id_note error ! ");
		}

		return rc;
	}
	
//	void selectCycPyaj() {
//		//-- 改讀 act_acno.last_pay_date , act_acno.last_pay_amt
//		wp.colSet("lastpay_date", "");
//		wp.colSet("jrnl_lastpay_amt", 0);
//		if (ibDebit) return;
//		//-最近繳款日,金額-
//		daoTid = "pay.";
//		strSql = "select payment_date, dc_payment_amt"
//		       + " from cyc_pyaj"
//		       + " where p_seqno =?"
//		       + " and payment_type in (select payment_type from ptr_payment where fund_flag ='N' and payment_type NOT IN ('OP01','OP04','PR01','REFU'))"
//		       +" and dc_payment_amt >0"
//		       + " order by payment_date desc"
//		       + commSqlStr.rownum(1);
//		setString(1, isAcctpSeqno);
//		sqlSelect(strSql);
//		if (sqlRowNum > 0) {			
//			wp.colSet("lastpay_date", colStr("pay.payment_date"));
//		    wp.colSet("jrnl_lastpay_amt", colStr("pay.dc_payment_amt"));
//		}
//	}
	
	void selectActAnalSub() {
		wp.colSet("consume_1",0);
		wp.colSet("consume_2",0);
		wp.colSet("consume_3",0);
		wp.colSet("consume_4",0);
		wp.colSet("consume_5",0);
		wp.colSet("consume_6",0);
		if (eq(isAcnoFlag,"Y") || ibDebit)
			return;

		String sql1 = " select "
				+ " his_purchase_amt+his_cash_amt as temp_consume "
				+ " from act_anal_sub "
				+ " where acct_type = ? "					
				;
			
		if (eq(isAcnoFlag,"Y")) {
			sql1 += " and p_seqno in (select p_seqno from act_acno where corp_p_seqno = ? and corp_p_seqno<>'') ";
		}
		else {
			sql1 += " and p_seqno = ? ";
		}	
		sql1 += " order by acct_month Desc " +commSqlStr.rownum(6);

		if(eqIgno(isAcnoFlag,"Y")){
			sqlSelect(sql1,new Object[]{isAcctType,isCorpPSeqno});
		}	else	{
			sqlSelect(sql1,new Object[]{isAcctType,isAcctpSeqno});
		}

		int il_rows = sqlRowNum ;
		if(il_rows <=0)	return ;
		int rr=1;
		for(int ii=0;ii<6;ii++){
			wp.colSet("consume_"+rr, colStr(ii,"temp_consume"));
			rr++;
		}
		
	}
	
}
