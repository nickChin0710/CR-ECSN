package Rsk;
/**
 * 2020-0121  V1.00.00	JH	   rsk_acnolog.fit_cond
 * 2019-0725  V1.00.01  JH     SIT
 * 2019-0625: V1.00.02  JH     p_xxx >>acno_p_xxx
 * 2019-0402  V1.00.03  JH     bugfix
 * 107/02/05  V1.00.04  Alex   initial                                    *
 * 2018-0615  V1.00.05  JH     pasue_flag                                 *
 * 2018-1115  V1.00.06  JH     bug-fix
 * 109-11-19  V1.00.07  tanwei    updated for project coding standard
 * 111-12-14  V1.00.08  Alex   解凍送Outgoing
 * */

import com.BaseBatch;
import com.DataSet;
import com.Parm2sql;

import Cca.CcaOutGoing;

public class RskP815 extends BaseBatch {
private String progname = "整批解凍處理程式    111/12/14 V1.00.08";

// -----------------------------------------------------------------------------
hdata.ActAcno hAcno = new hdata.ActAcno();
hdata.RskAcnoLog hAclg = new hdata.RskAcnoLog();
hdata.PtrBlockparam hBkpm = new hdata.PtrBlockparam();
hdata.RskBlockexec hBkec = new hdata.RskBlockexec();
hdata.CcaCardAcct hCcat=new hdata.CcaCardAcct();
hdata.CrdCard hCard=new hdata.CrdCard();
CcaOutGoing ccaOutGoing = null;
DataSet dsParm=new DataSet();
// -----------------------------------------------------------------------------
private String hWdayStmtCycle = "";
public int ilParmRow = 0;
private String hTempExecDate = "";

private String hTempBlockWhy = "";
private String hTempNotblockWhy = "";
private String hSpecStatus = "";
private String hLastBlock = "";
//private double h_temp_billed_end_bal = 0;
private int prsCount = 0;
private int hBusiDay = 0;
String hBusiYym="";
private int swOk=0;
private int swOkblock=0;
// =========================================================
private int tiBlexecU=-1;
private int tiBlexecA=-1;
private int tiAcnoIdno=-1;
private int tiAcctSum=-1;
private int tiBlexecU2=-1;
private int tiBlexecA2=-1;
private int tiCcasAcctU=-1;
private int tiCard0=-1;
private int tiCard=-1;
private int tiCardU=-1;

int commit=1;
//=***************************************************************************
public static void main(String[] args) {
	RskP815 proc = new RskP815();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	if (args.length != 0) {
		printf("Usage : RskP815 ");
		errExit(1);
	}

	dbConnect();
	
	ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
	
	hBusiYym =commString.mid(hBusiDate,0,6);
	hBusiDay = commString.ss2int(commString.mid(hBusiDate, 6, 2));
	
	setBlockparamArray();
	selectAcctType();

	sqlCommit(commit);
	ccaOutGoing.finalCnt2();
	endProgram();
}

//=============================================================================
void setBlockparamArray() throws Exception {

	sqlCmd = " select "
		+ " acct_type , pause_flag, "
		+ " mcode_value1 as ii_mcode1, "
		+ " mcode_value2 as ii_mcode2, "
		+ " mcode_value3 as ii_mcode3, "
		+ " nvl(debt_amt1,0) as im_amt1 , "
		+ " nvl(debt_amt2,0) as im_amt2 , "
		+ " nvl(debt_amt3,0) as im_amt3 "
		+ " from ptr_blockparam "
		+ " where apr_flag ='Y' and param_type ='2'"
		+" and (acct_type, valid_date) in ("
		+" select acct_type,max(valid_date) from ptr_blockparam"
		+" where apr_flag ='Y' and param_type ='2'"
		+" and valid_date <=?"
		+" group by acct_type )"
		+ " order by acct_type ";

	ppp(1, hBusiDate);
//	ddd_sql();
	
	sqlQuery(dsParm, "",null);
	if (sqlNrow <0) {
		sqlerr("select ptr_blockparam error");
		errExit();
	}

	ilParmRow = dsParm.rowCount();
}

// =****************************************************************************
void selectAcctType() throws Exception {
	daoTid = "acty.";
	sqlCmd = " select distinct "
		+ " acct_type "
		+ " from ptr_blockparam "
		+ " where apr_flag = 'Y' "
//		+ " and nvl(pause_flag,'x') <> 'Y' "
		+ " and param_type = '2' "
		+ " order by acct_type ";
	
	sqlSelect();
	if (sqlNrow < 0) {
		errmsg("select ptr_blockparam error");
		errExit(1);
	}
	if (sqlNrow == 0) {
		printf("無參數可供執行");
		errExit(0);
	}
	int rrSelect = sqlNrow;
	for (int ii = 0; ii < rrSelect; ii++) {
		swOk = 0;
		selectPtrBlockparam(colSs(ii,"acty.acct_type"));
		if (swOk == 0) {
			updatePtrBlockparam();
		}
	}
}

//=============================================================================
void selectPtrBlockparam(String aAcctType) throws Exception {
	if (empty(aAcctType)) {
		errmsg("acct_type 不可空白");
		return;
	}
	printf("==>處理帳戶類別 [%s] 資料",aAcctType);
	
	hBkpm.initData();
	hBkpm.acctType =aAcctType;
	
	sqlCmd = " select A.*,"
		+ " hex(A.rowid) as rowid "
		+ " from ptr_blockparam A"
		+ " where A.valid_date <= ? "
		+ " and A.apr_flag = 'Y' "
//		+ " and nvl(A.pause_flag,'x') <> 'Y' "
		+ " and A.acct_type = ? "
		+ " and A.param_type = '2' " // -解凍-
		+ " order by valid_date DESC "
		+ commSqlStr.rownum(1);
	
	daoTid = "bkpm.";
	sqlSelect("", new Object[] {
		hBusiDate, aAcctType
	});

	if (sqlNrow < 0) {
		printf("select ptr_blockparam error, kk[%s]", aAcctType);
		errExit(0);
	}

	if (sqlNrow == 0) {
		hBkpm.acctType =aAcctType;
		hBkpm.validDate = hBusiDate;
		hBkpm.paramType = "2"; // - 解凍 --
		swOk = 1;
		updateRskBlockexec(1);

		sqlCommit(commit);
		return;
	}

	// --塞值--
	hBkpm.acctType =colSs("bkpm.acct_type");
	hBkpm.execMode = colSs("bkpm.exec_mode");
	hBkpm.validDate = colSs("bkpm.valid_date");
	hBkpm.paramType = colSs("bkpm.param_type");
	hBkpm.execDate = colSs("bkpm.exec_date");
	hBkpm.execDay = colInt("bkpm.exec_day");
	hBkpm.execCycleNday = colInt("bkpm.exec_cycle_nday");
	hBkpm.mcodeValue1 = colInt("bkpm.mcode_value1");
	hBkpm.mcodeValue2 = colInt("bkpm.mcode_value2");
	hBkpm.mcodeValue3 = colInt("bkpm.mcode_value3");
	hBkpm.mcodeValue4 = colInt("bkpm.mcode_value4");
	hBkpm.debtAmt1 = colInt("bkpm.debt_amt1");
	hBkpm.debtAmt2 = colInt("bkpm.debt_amt2");
	hBkpm.debtAmt3 = colInt("bkpm.debt_amt3");
	hBkpm.debtAmt4 = colInt("bkpm.debt_amt4");
	hBkpm.n0Month = colInt("bkpm.n0_month");
	hBkpm.pauseFlag =colSs("bkpm.pause_flag");
	hBkpm.rowid = colSs("bkpm.rowid");

	hTempExecDate = commDate.monthAdd(hBkpm.execDate, hBkpm.n0Month);
	
	printf("-->解凍參數: acct_type[%s], valid_date[%s], run[%s], mode[%s]"
			, hBkpm.acctType, hBkpm.validDate, hBkpm.pauseFlag, hBkpm.execMode);
	
	// ---暫不執行--------------------------
	if (eqIgno(hBkpm.pauseFlag, "N")) {
		updateRskBlockexec(3);
		sqlCommit(commit);
		printf(" 暫不執行 =[%s]", hBkpm.acctType);
		swOk = 1;
		return;
	}
	
	//--每天:目前固定每天執行--------------------------------------------
	if (eq(hBkpm.execMode, "3")) {
		selectActAcno(0);
		return;
	}
	
	// ---每月固定一天-----------------------------------------
	if (eqIgno(hBkpm.execMode, "1")) {
		// ---每月固定執行日=營業日==--
		if (hBkpm.execDay != hBusiDay) {
			printf(" 今天非執行日, 不執行?=[%s]", hBusiDate);
			swOk = 1;
			return;
		}
		// - 最近執行日=null(未曾執行過) --
		if (hTempExecDate.length() == 0) {
			printf(" 最近執行日='' =[%s]", hBusiDate);
		}
		else {
			if (hBkpm.n0Month == 0
				|| eqIgno(hTempExecDate, hBusiYym)) {
				printf(" 最近執行日+間隔月=營業年月 =[%s]", hBusiDate);
			}
		}
		selectActAcno(0);
		return;
	}

	// ---Cycle後n天------------------------
	if (eqIgno(hBkpm.execMode, "2")) {

		int rrWday = selectPtrWorkday();
		if (rrWday == 0) {
			swOk = 1;
			return;
		}

		if (rrWday > 1) {
			updateRskBlockexec(2);
			sqlCommit(commit);
			swOk = 1;
			return;
		}

		if (hTempExecDate.length() == 0) {
			printf(" 最近執行日='' =[%s]", hBusiDate);
			selectActAcno(1);
		}
		else {
			if (hBkpm.n0Month == 0 ||
				eqIgno(hTempExecDate, hBusiDate.substring(0, 6))) {
				printf(" 最近執行日+間隔月=營業年月 =[%s]", hBusiDate);
				selectActAcno(1);
			}
			else {
				printf(" 不執行3 =[%s]", hBusiDate);
				swOk = 1;
				return;
			}
		}
	} // - exec_mode=2 --
	
}

//=***************************************************************************
void updatePtrBlockparam() throws Exception {
	sqlCmd = " update ptr_blockparam set "
		+ " exec_date = ? , "
		+ modxxxSet()
		+ " where rowid = ? ";

	ppp(1, hBusiDate);
	setRowId(2, hBkpm.rowid);
	sqlExec("");
	if (sqlNrow <= 0) {
		errmsg("update ptr_blockparam error");
		errExit(0);
	}
}

// =***************************************************************************
int selectPtrWorkday() throws Exception {
	// --
	String lsCloseDate = "";
	lsCloseDate = commDate.dateAdd(hBusiDate, 0, 0, 0 - hBkpm.execCycleNday);
	
	// --
	daoTid = "wday.";
	sqlCmd = " select stmt_cycle, this_acct_month "
		+ " from ptr_workday "
			+" where 1=1"
		+ " and this_close_date = ? ";

	sqlSelect("", new Object[] {
		lsCloseDate
	});
	if (sqlNrow < 0) {
		errmsg("select select_ptr_workday error");
		errExit(0);
	}

   hWdayStmtCycle =colSs("wday.stmt_cycle");
	return sqlNrow;
}

// =***************************************************************************
void updateRskBlockexec(int aiFlag) throws Exception {
	String lsMsg = "";
	if (aiFlag == 1) {
		lsMsg = "解凍參數不存在";
	}
	else if (aiFlag == 2) {
		lsMsg = "符合之cycle日有2筆以上";
	}
	else if (aiFlag == 3) {
		lsMsg = "暫不執行";
	}

	if (tiBlexecU <= 0) {
		sqlCmd = " update rsk_blockexec set "
			+ " exec_msg = ? , "
			+ modxxxSet()
			+ " where exec_date = ? "
			+ " and param_type = ? "
			+ " and acct_type = ? "
			+ " and valid_date = ? "
			+ " and exec_mode = ? ";
		tiBlexecU =ppStmtCrt("ti_blexec_U","");
	}
	ppp(1, lsMsg);
	ppp(hBusiDate);
	ppp(hBkpm.paramType);
	ppp(hBkpm.acctType);
	ppp(hBkpm.validDate);
	ppp(hBkpm.execMode);
	sqlExec(tiBlexecU);

	if (sqlNrow < 0) {
		printf("update ptr_blockexec error");
		errExit(0);
	}

	// --exist---
	if (sqlNrow > 0)
		return;

	// --not-find, Insert---
	if (tiBlexecA <= 0) {
		sqlCmd = " insert into rsk_blockexec ( "
			+ " param_type ,"
			+ " acct_type ,"
			+ " valid_date ,"
			+ " exec_date ,"
			+ " exec_msg ,"
			+ " exec_mode ,"
			+ " exec_times ,"
			+ " mod_user, mod_time, mod_pgm, mod_seqno "
			+ " ) values ( "
			+ " ? , "
			+ " ? , "
			+ " ? , "
			+ " ? , "
			+ " ? , "
			+ " ? , "
			+ " '1' , "
			+ modxxxInsert()
			+ " ) ";
		tiBlexecA =ppStmtCrt("ti_blexec_A","");
	}
	
	ppp(1, hBkpm.paramType);
	ppp(2, hBkpm.acctType);
	ppp(3, hBkpm.validDate);
	ppp(4, hBusiDate);
	ppp(5, lsMsg);
	ppp(6, hBkpm.execMode);
	sqlExec(tiBlexecA);
	if (sqlNrow < 0) {
		printf("insert rsk_blockexec error");
		errExit(0);
	}
}

//=***************************************************************************
void selectActAcno(int aiFlag) throws Exception {
   //-ai_file: 0.每日, 1.Cycle-
	int liIsBlock = 0; // - 1.凍結, 0.解凍 --

	// -- 記錄 rsk_blockexec 之開頭記錄 --
	startRskBlockexec();
	sqlCommit(commit);

	// --Cursor
	sqlCmd = " select "
		+ " A.p_seqno , A.acno_p_seqno, "
		+ " A.id_p_seqno , "
		+ " A.corp_p_seqno , "
		+ " A.acct_type , "
		+ " A.acct_key , "
		+ " A.card_indicator , "
		+ " A.no_unblock_flag , "
		+ " B.block_status , "
		+ " A.no_unblock_s_date , "
		+ " A.no_unblock_e_date , "
		+ " A.pay_by_stage_flag , "
		+ " A.payment_rate1 , A.int_rate_mcode,"
		+ " B.block_reason1 , "
		+ " B.block_reason2 , "
		+ " B.block_reason3 , "
		+ " B.block_reason4 , "
		+ " B.block_reason5 , "
		+ " B.spec_status , B.spec_del_date, "
		+" B.card_acct_idx, "
		+ " hex(B.rowid) as ccas_rowid "
		+ " from act_acno A join cca_card_acct B "
		+ " on A.acno_p_seqno = B.acno_p_seqno and B.debit_flag <> 'Y' "
		+ " where 1=1 " 
		+ " and B.block_reason1 <> '' "
		+ " and A.stop_status = '' " // - 非停用戶才處理 --
		+ " and A.acct_type = ? "
		+ " and A.acno_flag in ('1','3') "
        + " and exists (select card_no from crd_card where crd_card.acno_p_seqno=A.acno_p_seqno and current_code='0') "		
		;

	// --stmt_cycle
	ppp(1,hBkpm.acctType);
   //-參數0==>全部, 參數1==>單一cycle-
   if (aiFlag != 0) { // decode(:ai_flag,0,stmt_cycle,:h_wday_stmt_cycle)
      sqlCmd +=" and A.stmt_cycle =?";
      ppp(hWdayStmtCycle);
   }

   this.fetchExtend = "AA.";
	if (this.openCursor() < 0) {
		errmsg("open cursor error, select_act_acno()");
		errExit(1);
	}

	while (fetchTable()) {
      hAcno.acnoPSeqno = colSs("AA.acno_p_seqno");
		hAcno.pSeqno = colSs("AA.p_seqno");
		hAcno.idPSeqno = colSs("AA.id_p_seqno");
		hAcno.corpPSeqno = colSs("AA.corp_p_seqno");
		hAcno.acctType = colSs("AA.acct_type");
		hAcno.acctKey = colSs("AA.acct_key");
		hAcno.cardIndicator = colSs("AA.card_indicator");
		hCcat.blockStatus = colSs("AA.block_status");
		hAcno.noUnblockFlag = colSs("AA.no_unblock_flag");
		hAcno.noUnblockSDate = colSs("AA.no_unblock_s_date");
		hAcno.noUnblockEDate = colNvl("AA.no_unblock_e_date", "99991231");
		hAcno.payByStageFlag = colSs("AA.pay_by_stage_flag");
		hAcno.paymentRate1 = colSs("AA.payment_rate1");
		hAcno.intRateMcode =colInt("aa.int_rate_mcode");
		hAclg.blockReason = colSs("AA.block_reason1");
		hAclg.blockReason2 = colSs("AA.block_reason2");
		hAclg.blockReason3 = colSs("AA.block_reason3");
		hAclg.blockReason4 = colSs("AA.block_reason4");
		hAclg.blockReason5 = colSs("AA.block_reason5");
		hAclg.specStatus =colSs("AA.spec_status");
		hAclg.speDelDate =colSs("AA.spec_del_date");
		hCcat.cardAcctIdx =colNum("AA.card_acct_idx");
//		h_acno.rowid = col_ss("AA.ccas_rowid");

		// --check 有效卡
      //and exists (select 1 from crd_card where crd_card.acno_p_seqno=A.acno_p_seqno and current_code='0');
//		if (ti_card0 < 0) {
//			sqlCmd = " select count(*) as db_card_valid "
//				+ " from crd_card "
//				+ " where p_seqno = ? "
//				+ " and current_code = '0' ";
//			ti_card0 =ppStmt_crt("ti_card0","");
//		}
//		ppp(1, h_acno.p_seqno);
//		sqlSelect(ti_card0);
//		if (col_num("db_card_valid") <= 0) {
//			continue;
//		}

		// -- 同一ID之解凍 ---
		hAclg.relateCode = "";
		hAclg.relaPSeqno = "";

		totalCnt++;
		if (totalCnt % 1000 == 0) {
			printf("讀取筆數 = [%s] ", totalCnt);
		}

		int hMCode =hAcno.intRateMcode;  //comr.getMcode(h_acno.acct_type, h_acno.p_seqno);
		// --check M-code---
		if (hMCode != hBkpm.mcodeValue3
			&& hMCode != hBkpm.mcodeValue2
			&& hMCode > hBkpm.mcodeValue1) {
			continue;
		}

		// --Get 本金結欠相關資料---
		double lmBilledEndBal = selectAcctSum(hAcno.pSeqno);

		hTempBlockWhy = "";
		hTempNotblockWhy = "";
		liIsBlock = 1; // --1.凍結, 0.解凍---

		if (hMCode == hBkpm.mcodeValue3
			&& lmBilledEndBal < hBkpm.debtAmt3) {
			hTempBlockWhy = "Mf";
			hTempNotblockWhy = "";
			liIsBlock = 0;
		}
		if (hMCode == hBkpm.mcodeValue2
			&& lmBilledEndBal < hBkpm.debtAmt2) {
			hTempBlockWhy = "Me";
			hTempNotblockWhy = "";
			liIsBlock = 0;
		}
		if (hMCode <= hBkpm.mcodeValue1) {
			hTempBlockWhy = "Md";
			hTempNotblockWhy = "";
			liIsBlock = 0;
		}

		// -不可解凍-
		//ddd("==>isBlock: mcode[%s], debt_amt[%s]",h_M_code,lm_billed_end_bal);
		if (liIsBlock == 1) {
			continue;
		}

		hBkec.tAcctCnt++; // 應解凍筆數

		// -JH(R93129)-永不解凍-
		if (eq(hAcno.noUnblockFlag, "Y") &&
            commString.between(hBusiDate,hAcno.noUnblockSDate,hAcno.noUnblockEDate) ) {
			hBkec.tBlocknot1Cnt++; // 應解凍不解動(永不解凍戶)
			hTempNotblockWhy = "T1";
			insertRskAcnolog(2);
			continue;
		}

		// -JH(R93129)-check 同ID之帳戶--
		if (checkAcnoIsblock() != 0) {
			//ddd("==>cannot unBlock.....[%s]",h_acno.id_p_seqno);
			hBkec.tBlocknot4Cnt++; // 同ID無法解凍 --
			hTempNotblockWhy = "T4";
			insertRskAcnolog(3);
			continue;
		}

		//-(cancel)-帳戶可解凍:卡片全部解凍, 帳戶才可解凍--
		swOkblock = 1;
		//check_card_isblock();
		if (swOkblock == 0) {
			hBkec.tBlocknot2Cnt++;
			hTempNotblockWhy = "T2";
			insertRskAcnolog(3);
		}
		else {
			// 應解凍真解凍戶 --
			if (eq(hTempBlockWhy, "Mf")) {
				hBkec.tBlockCnt3++;
			}
			else if (eq(hTempBlockWhy, "Me")) {
				hBkec.tBlockCnt2++;
			}
			else if (eq(hTempBlockWhy, "Md")) {
				hBkec.tBlockCnt++;
			}
			insertRskAcnolog(4);
		}
		
		hAclg.blockReason ="";
		updateCcaCardAcct();		
//		ddd("==>帳戶解凍[%s], block=[%s],[%s],[%s],[%s],[%s]",h_acno.p_seqno
//				,h_aclg.block_reason,h_aclg.block_reason2,h_aclg.block_reason3
//				,h_aclg.block_reason4,h_aclg.block_reason5);

		selectCrdCard(hAcno.pSeqno); 
		
		prsCount++;
		if (prsCount % 3000 == 0) {
			printf("解凍筆數 =[%s]", prsCount);
		}
		
		//-debug-
		if (debug && prsCount>5) {
			break;
		}
	}
	
	this.closeCursor();
	printf("-->acct_type=[%s] 處理筆數=[%s]",hBkpm.acctType,totalCnt);

	// - 記錄 rsk_blockexec 之累計及結尾記錄(目前逐筆) --
	endRskBlockexec();
}

//=****************************************************************************
//private void check_card_isblock() {
//	// -cancel:卡片凍結查核-
//	return;
//}

// =****************************************************************************
int checkAcnoIsblock() throws Exception {
	int liRc = 0; // -0.可解, 1.不可解-
	int liPayRate = 0;
	double ldcBilledEndBal = 0;
//	int li_mcode1 = 0, li_mcode2 = 0, li_mcode3 = 0;
//	double ldc_amt2 = 0, ldc_amt3 = 0;

	ddd("--check_acno_isblock=[%s]", hAcno.idPSeqno);

	if (tiAcnoIdno <= 0) {
		sqlCmd = "select p_seqno,"
			+ " acct_type,"
			+ " no_unblock_flag,"
			+ " no_unblock_s_date,"
			+ " no_unblock_e_date "
            +", int_rate_mcode"
			+ " from act_acno"
			+ " where id_p_seqno =?"
			+ " and acct_type <>?"
			+ " and stop_status <>''" // 非停用戶才處理--
			+ " and acno_flag in ('1','3')" // -個繳戶--
		;
		tiAcnoIdno = ppStmtCrt("ti_acno_idno","");
	}

	ppp(1, hAcno.idPSeqno);
	ppp(hAcno.acctType);
	sqlSelect(tiAcnoIdno);
	if (sqlNrow < 0) {
		errmsg("check_acno_isblock.sqlerr, id_p_seqno=[%s]", hAcno.idPSeqno);
		errExit(0);
	}
	if (sqlNrow == 0)
		return 0;

	int rrAcno = sqlNrow;
	for (int ll = 0; ll < rrAcno; ll++) {
		String lsPSeqno = colSs(ll, "p_seqno");
		String lsAcctType = colSs(ll, "acct_type");
		String lsNoFlag = colNvl(ll, "no_unblock_flag", "N");
		String lsNoSdate = colSs(ll, "no_unblock_s_date");
		String lsNoEdate = colNvl(ll, "no_unblock_e_date", "99991231");
      liPayRate =colInt(ll,"int_rate_mcode");

		hAclg.relaPSeqno = lsPSeqno;
		if (eq(lsNoFlag, "Y") &&
            commString.between(hBusiDate,lsNoSdate,lsNoEdate) ) {
			return 1; // -不可解凍-
		}
		// -- Get 本金結欠相關資料 --
		ldcBilledEndBal = selectAcctSum(lsPSeqno);

		// -- check 解凍參數 --
		// + " mcode_value1 as ii_mcode1, "
		// + " mcode_value2 as ii_mcode2, "
		// + " mcode_value3 as ii_mcode3, "
		// + " nvl(debt_amt1,0) as im_amt1 , "
		// + " nvl(debt_amt2,0) as im_amt2 , "
		// + " nvl(debt_amt3,0) as im_amt3 "

		for (int ii = 0; ii < ilParmRow; ii++) {
			if (!eq(lsAcctType, dsParm.colSs(ii, "acct_type")))
				continue;

			//-執行中---
			if (eq(dsParm.colSs(ii,"pause_flag"),"Y")==false)
				continue;

			// -- 繳款評等<=參數Md --
			if (liPayRate <= dsParm.colInt(ii,"ii_mcode"))
				continue;
			// -- 繳款評等=參數Me and 本金結欠<參數Xe --
			if (liPayRate == dsParm.colInt(ii, "ii_mcode2")
				&& ldcBilledEndBal < dsParm.colNum(ii, "im_amt2"))
				continue;
			// -- 繳款評等=參數Mf and 本金結欠<參數Xf --
			if (liPayRate == dsParm.colInt(ii, "ii_mcode3")
				&& ldcBilledEndBal < dsParm.colNum(ii, "im_amt3"))
				continue;

			// -無法解凍-
			liRc = 1; // other acct unblock --
			break;
		} // -for-

		if (liRc == 1)
			break;
	}
	if (liRc != 0) {
		hAclg.relateCode = "";
		hAclg.relaPSeqno = "";
	}

	return liRc;
}

private double selectAcctSum(String aPSeqno) throws Exception {
	if (empty(aPSeqno))
		return 0;
	
	if (tiAcctSum <= 0) {
		sqlCmd = "select sum(nvl(billed_end_bal,0)) as aa_amt"
			+ " from act_acct_sum"
			+ " where p_seqno =?"
			+ " and acct_code in ('BL','CA','IT','ID','AO','OT','CB')";
		
		tiAcctSum = ppStmtCrt("ti_acct_sum","");
	}
	ppp(1, aPSeqno);
	sqlSelect(tiAcctSum);
	if (sqlNrow < 0) {
		sqlerr("select act_acct_sum error; kk[%s]", aPSeqno);
		errExit(0);
	}
	if (sqlNrow > 0)
		return colNum("aa_amt");

	return 0;
}

private com.Parm2sql ttAlog=null;
//=======================================================
void insertRskAcnolog(int aiFlag) throws Exception {
	String lsKindFlag = "A";
	String lsCardNo = "";
	if (aiFlag == 5) {
		lsKindFlag = "C";
		lsCardNo = hAclg.cardNo;
	}
	
	if (ttAlog ==null) {
		ttAlog = new Parm2sql();
		ttAlog.insert("rsk_acnolog");
	}
//	+", '2'"	//log_mode"
//	+", '4'"	//:log_type:解凍
	ttAlog.aaa("kind_flag", lsKindFlag);
	ttAlog.aaa("card_no", lsCardNo);
	ttAlog.aaa("acno_p_seqno", hAcno.acnoPSeqno);
	ttAlog.aaa("acct_type", hAcno.acctType);
	ttAlog.aaa("id_p_seqno", hAcno.idPSeqno);
	ttAlog.aaa("corp_p_seqno", hAcno.corpPSeqno);
	ttAlog.aaa("log_date", hBusiDate);
	ttAlog.aaa("log_mode","2");
	ttAlog.aaa("log_type","4");
	ttAlog.aaa("log_reason", hTempBlockWhy);
	ttAlog.aaa("log_not_reason", hTempNotblockWhy);
	ttAlog.aaa("log_remark", "批次解凍");
	ttAlog.aaa("block_reason", hAclg.blockReason);
	ttAlog.aaa("block_reason2", hAclg.blockReason2);
	ttAlog.aaa("block_reason3", hAclg.blockReason3);
	ttAlog.aaa("block_reason4", hAclg.blockReason4);
	ttAlog.aaa("block_reason5", hAclg.blockReason5);
	ttAlog.aaa("spec_status", hAclg.specStatus);
	ttAlog.aaa("spec_del_date",hAclg.speDelDate);
	ttAlog.aaa("emend_type","1");
	if (empty(hTempNotblockWhy))
		ttAlog.aaa("fit_cond","N");
	else ttAlog.aaa("fit_cond","Y");
	ttAlog.aaa("relate_code", hAclg.relateCode);
	ttAlog.aaa("rela_p_seqno", hAclg.relaPSeqno);
	ttAlog.aaaModxxx(hModUser,hModPgm);

   if (ttAlog.ti <=0) {
		ttAlog.ti = ppStmtCrt("t_aclg-A",ttAlog.getConvSQL());
	}

	sqlExec(ttAlog.ti, ttAlog.getConvParm());
	if (sqlNrow <= 0) {
		sqlerr("insert rsk_acnolog error, kk[%s]",hAcno.acnoPSeqno);
		errExit(0);
	}
}

// =***************************************************************************
void startRskBlockexec() throws Exception {
	hBkec.initData();
	
	daoTid = "bkec.";
	sqlCmd = " select "
		+ " t_acct_cnt , "
		+ " t_block_cnt , "
		+ " t_block_cnt2 , "
		+ " t_block_cnt3 , "
		+ " t_blocknot1_cnt , "
		+ " t_blocknot2_cnt , "
		+ " t_blocknot3_cnt , "
		+ " t_blocknot4_cnt , "
		+ " hex(rowid) as rowid "
		+ " from rsk_blockexec "
		+ " where exec_date = ? "
		+ " and acct_type = ? "
		+ " and param_type = ? "
		+ " and valid_date = ? "
		+ " and exec_mode = ? ";

	ppp(1,hBusiDate);
	ppp(hBkpm.acctType);
	ppp(hBkpm.paramType);
	ppp(hBkpm.validDate);
	ppp(hBkpm.execMode);
	
	ddd(sqlCmd,getSqlParm());
	sqlSelect();

	if (sqlNrow <= 0) {
		insertRskBlockexec();
	}
	else {
		hBkec.tAcctCnt =colNum("t_acct_cnt");
		hBkec.tBlockCnt =colNum("t_block_cnt");
		hBkec.tBlockCnt2 =colNum("t_block_cnt2");
		hBkec.tBlockCnt3 =colNum("t_block_cnt3");
		hBkec.tBlocknot1Cnt =colNum("t_blocknot1_cnt");
		hBkec.tBlocknot2Cnt =colNum("t_blocknot2_cnt");
		hBkec.tBlocknot3Cnt =colNum("t_blocknot3_cnt");
		hBkec.tBlocknot4Cnt =colNum("t_blocknot4_cnt");
		hBkec.rowid =colSs("rowid");

		if (tiBlexecU2 <= 0) {
			sqlCmd = " update rsk_blockexec set "
				+ " param_type = '2' , "	//解凍
				+ " valid_date = ? , "
				+ " exec_date = ? , "
				+ " exec_times = exec_times+1 , "
				+ " exec_date_s = sysdate , "
				+ modxxxSet()
				+ " where rowid = ? ";
			tiBlexecU2 =ppStmtCrt("ti_blexec_U2","");
		}
		
		ppp(1, hBkpm.validDate);
		ppp(2, hBusiDate);
		setRowId(3, hBkec.rowid);
		sqlExec(tiBlexecU2);
	}
}

private void insertRskBlockexec() throws Exception {
	if (tiBlexecA2 <= 0) {
		sqlCmd = "insert into rsk_blockexec ("
			+ " param_type,"
			+ " acct_type,"
			+ " valid_date,"
			+ " exec_date,"
			+ " exec_mode,"
			+ " exec_times,"
			+ " exec_date_s,"
			+ " mod_user, mod_time, mod_pgm, mod_seqno"
			+ " ) values ("
			+ " 2," // 解凍
			+ " ?,?,?,?,"
			+ " 1, sysdate,"
			+ modxxxInsert()
			+ " )";
		tiBlexecA2 = ppStmtCrt("ti_blexec_A2","");
	}
	ppp(1, hBkpm.acctType);
	ppp(hBkpm.validDate);
	ppp(hBusiDate);
	ppp(hBkpm.execMode);
	
	sqlExec(tiBlexecA2);
	if (sqlNrow <= 0) {
		sqlerr("insert rsk_blockexec error");
		errExit(0);
	}
}

//=***************************************************************************
void updateCcaCardAcct() throws Exception {
	// --R97035---
	if (empty(hAclg.blockReason)
		&& empty(hAclg.blockReason2)
		&& empty(hAclg.blockReason3)
		&& empty(hAclg.blockReason4)
		&& empty(hAclg.blockReason5)) {
		hCcat.blockStatus = "N";
	}

	if (tiCcasAcctU < 0) {
		sqlCmd = " update cca_card_acct set "
			+ " block_status = ? , "
			+ " block_reason1 = '' , "
			+ modxxxSet()
			+ " where 1=1 "
			+ " and card_acct_idx = ? ";
		tiCcasAcctU =ppStmtCrt("ti_ccas_acct_U","");
	}
	ppp(1, hCcat.blockStatus);
	//-kk-
	ppp(hCcat.cardAcctIdx);
	sqlExec(tiCcasAcctU);
	if (sqlNrow <= 0) {
		sqlerr("update cca_card_acct error, kk[%s]",hCcat.cardAcctIdx);
		errExit(0);
	}

}

//=***************************************************************************
void selectCrdCard(String aPseqno) throws Exception {
	if (tiCard <=0) {
		sqlCmd ="SELECT A.card_no,"
				+" A.current_code,"
				+" A.oppost_reason,"
				+" A.oppost_date,"
				+" A.block_code,"
				+" A.block_date,"
				+" A.sup_flag,"
				+" A.combo_indicator, "
				+" uf_spec_status(B.spec_status, B.spec_del_date) as spec_status "
				+" FROM   crd_card A join cca_card_base B on A.card_no = B.card_no "
				+" where A.p_seqno =?"
				+" and  A.current_code ='0'"
				+" order  by A.oppost_date"
				;
		tiCard =ppStmtCrt("ti_card","");
	}
	ppp(1,aPseqno);
	daoTid ="card.";
	sqlSelect(tiCard);
	if (sqlNrow <=0)
		return;
	int llNrow =sqlNrow;
	for (int ll=0; ll<llNrow; ll++) {
		hCard.cardNo =colSs("card.card_no");
		hCard.currentCode =colSs("card.current_code");
	    hCard.oppostReason =colSs("card.oppost_reason");
	    hCard.oppostDate =colSs("card.oppost_date");
	    hCard.blockCode =colSs("card.block_code");
	    hCard.blockDate =colSs("card.block_date");
	    hCard.supFlag =colSs("card.sup_flag");
	    hCard.comboIndicator =colNvl("card.combo_indicator","N");
	    hSpecStatus = colSs("card.spec_status");
	    
	    if (noEmpty(hCard.oppostDate)) {
 	   		continue;
	    }
	   
	    updateCrdCard(hCard.cardNo);
	   
	    //--判斷無凍結碼和特指送刪除 Outgoing
	    if(empty(hCcat.blockReason1) && empty(hCcat.blockReason2) && empty(hCcat.blockReason3) && 
	       empty(hCcat.blockReason4) && empty(hCcat.blockReason5) && empty(hSpecStatus)) {
	    	//--取上一次送Outgoing的 block_reason
	    	hLastBlock = getLastBlockCode();
	    	if(hLastBlock.isEmpty() == false) {
	    		ccaOutGoing.deleteCcaOutGoingBlock(hCard.cardNo, hCard.currentCode, sysDate, hLastBlock);
	    	}
	    }
	    
	    if (eq(hCard.supFlag,"0") && !eqIgno(hCard.comboIndicator,"N")) {
	    	insertCrdStopLog();
	    }
	}
	
}

String getLastBlockCode() throws Exception {
	daoTid = "outgoing.";
	String sql1 = " select block_code from cca_outgoing where card_no = ? and act_code ='1' order by crt_date Desc , crt_time Desc fetch first 1 rows only ";
	sqlSelect(sql1,new Object[] {hCard.cardNo});
	
	if(sqlNrow > 0 )
		return colSs("outgoing.block_code");
	
	return "";
}

//=****************************************************************************
void updateCrdCard(String aCardNo) throws Exception {
	String lsBlock15=hAclg.blockReason+hAclg.blockReason2
			+hAclg.blockReason3+hAclg.blockReason4+hAclg.blockReason5;
	String lsBlockDate =colNvl("card.block_date",hBusiDate);
	if (empty(lsBlock15))
		lsBlockDate ="";
	
	if (tiCardU <=0) {
		sqlCmd ="update crd_card set"
				+" block_code =?,"
				+" block_date =?,"
				+modxxxSet()
				+" where card_no =?"
				;
		tiCardU =ppStmtCrt("ti_card_U","");
	}
	ppp(1,lsBlock15);
	ppp(lsBlockDate);
	ppp(aCardNo);
	
	dddSql(tiCardU);
	sqlExec(tiCardU);
	if (sqlNrow <=0) {
		errmsg("update crd_card error, kk[%s]",aCardNo);
      errExit(0);
	}
}

private com.Parm2sql ttSlog=null;
//==============================================
void insertCrdStopLog() throws Exception {
	if (ttSlog ==null) {
		ttSlog = new Parm2sql();
		ttSlog.insert("crd_stop_log");
	}

	ttSlog.aaaFunc("proc_seqno",commSqlStr.seqEcsStop,"");
	ttSlog.aaaFunc("crt_time","to_char(sysdate,'yyyymmddhh24miss')","");
	ttSlog.aaa("card_no",hCard.cardNo);
	ttSlog.aaa("current_code", hCard.currentCode);
	ttSlog.aaa("oppost_reason", hCard.oppostReason);
	ttSlog.aaa("oppost_date", hCard.oppostDate);
	ttSlog.aaa("trans_type","10");
	ttSlog.aaa("send_type","2");
	ttSlog.aaaModxxx(hModUser,hModPgm);

	if (ttSlog.ti <=0) {
		ttSlog.ti =ppStmtCrt("tt_slog-A",ttSlog.getConvSQL());
	}

	sqlExec(ttSlog.ti,ttSlog.getConvParm());
	if (sqlNrow <=0) {
		errmsg("insert crd_stop_log error, kk[%s]",hCard.cardNo);
      errExit(0);
	}
}

// =***************************************************************************
void endRskBlockexec() throws Exception {

	sqlCmd = " update rsk_blockexec set "
		+ " exec_date_e = sysdate , "
		+ " t_acct_cnt = ? , "
		+ " t_block_cnt = ? , "
		+ " t_block_cnt2 = ? , "
		+ " t_block_cnt3 = ? , "
		+ " t_blocknot1_cnt = ? , "
		+ " t_blocknot2_cnt = ? , "
		+ " t_blocknot4_cnt = ? , " // 7
		+ modxxxSet()
		+ " where exec_date = ? " // 8
		+ " and acct_type = ? "
		+ " and param_type = ? "
		+ " and valid_date = ? "
		+ " and exec_mode = ? ";

	ppp(1, hBkec.tAcctCnt);
	ppp(2, hBkec.tBlockCnt);
	ppp(3, hBkec.tBlockCnt2);
	ppp(4, hBkec.tBlockCnt3);
	ppp(5, hBkec.tBlocknot1Cnt);
	ppp(6, hBkec.tBlocknot2Cnt);
	ppp(7, hBkec.tBlocknot4Cnt);
	//-kk-
	ppp(8, hBusiDate);
	ppp(hBkpm.acctType);
	ppp(hBkpm.paramType);
	ppp(hBkpm.validDate);
	ppp(hBkpm.execMode);
	
	sqlExec("");
	if (sqlNrow <0) {
		sqlerr("end_rsk_blockexec error");
		errExit(0);
	}
}

}
