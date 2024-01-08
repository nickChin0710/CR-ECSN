package Rsk;
/**
 * 2020-0305    V1.00.00   JH		 mod:只停一卡
 * 2020-0304    V1.00.01   JH		 line_of_credit_amt
 * 2020-0120    V1.00.02   JH		 onbat_2xxx.mod_pgm
 * 2020-0115    V1.00.03   JH        acno_flag=1,3
 * 2020-0110    V1.00.04   JH        acno_flag=1,3
 * 2020-0108    V1.00.05   JH        modify
 * 2019-0625:   V1.00.06   JH        p_xxx >>acno_pxxx
 * 106/11/02    V1.00.07    Alex     initial                                    *
 * 2018-0306    V1.00.08    JH     	 modify                                     *
 * 2018-0614    V1.00.09    JH     	  暫不執行                                                                                                        *
 * 2019-0522    V1.00.10    JH       欠款總額,M-code,block_reason
 * 109-11-23    V1.00.11   tanwei    updated for project coding standard 
 * */

import com.Parm2sql;
import com.SqlParm;
import com.BaseBatch;
import table.CrdJcic;
import table.CrdStopLog;
import Cca.CcaOutGoing;

public class RskP730 extends BaseBatch {
private String progname = "一般卡整批強停處理程式    111/12/07 V1.00.12";

//=============================================================================
hdata.CrdCard hCard=new hdata.CrdCard();
hdata.ActAcno hAcno=new hdata.ActAcno();
hdata.PtrWorkday hWday=new hdata.PtrWorkday();
hdata.PtrStopparam hStpm=new hdata.PtrStopparam();
hdata.RskAcnoLog hAclg=new hdata.RskAcnoLog();
//-----------------------------------------------------------------------------
private String hAclgCardNo = "";
private double hAcctPayAmt = 0;
private long hStecTAcctCnt = 0;
private long hStecTStopCnt = 0;
private long hStecTStopnot1Cnt;
private long hStecTStopnot2Cnt;
private long hStecTStopnot3Cnt;
private long hStecTStopnot4Cnt;
private String hStecRowid = "";
private String hCdjcRowid = "";
private String hFiscReason = "";
private int iiBusiDays = 0;
private int liProcRows=0;
private String hTempExecDate="";
private double hTempBilledEndBal = 0;
private String hTempNotstopWhy="";
private String hTempStopWhy="";
private double[] maHisPayAmt =new double[100];
private long prsCount = 0;
private String swOk;
//=========================================================
private SqlParm tSexec=new com.SqlParm();
private SqlParm tApsc=new com.SqlParm();
//private SqlParm tt_aclg=new com.SqlParm();
////private table.Rsk_acnolog t_aclg=null;
private table.ColStopLog tColstop=null;
private CrdStopLog tCrdstop=null;
private CrdJcic tCrdJcic=null;
CcaOutGoing ccaOutGoing = null;
private int tidStopexecU1=-1;
private int tidAcctSumS1=-1;
private int tidDebtS1=-1;
private int tidAnalSubS=-1;
private int tidAcctS=-1;
private int tidAcctHstS=-1;
private int tidAcnoU=-1;
private int tidCardS=-1;
private int tid2CardS=-1;
private int tid3CardS=-1;
private int tidCardU=-1;
private int tidJcicS=-1;
private int tidJcicU=-1;
private int tidIdnoS=-1;
int tiCcat =-1;
private int commit=1;

//=****************************************************************************
public static void main(String[] args) {
	RskP730 proc = new RskP730();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	if (args.length > 1) {
		printf("Usage : RskP730 [business_date]");
		errExit(1);
	}

   dbConnect();

	if (args.length == 1) {
	   setBusiDate(args[0]);
	}
			
	iiBusiDays = (int)commString.ss2Num(commString.mid(hBusiDate,6,2));
	
	ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
	
	selectAcctType();

	sqlCommit(commit);
	ccaOutGoing.finalCnt2();
	endProgram();
}

//=****************************************************************************
void selectAcctType() throws Exception {
	daoTid = "stpm.";
	sqlCmd = "select distinct acct_type as is_proc_acct_type "
		+ " from ptr_stopparam "
		+ " where apr_flag = 'Y' "
		+ " and param_type = '1' "
		+ " and nvl(pause_flag,'') ='Y' "  //-執行中-
		+ " order by acct_type ";

	sqlSelect();
	int llRow = sqlNrow;

	for (int ii = 0; ii < llRow; ii++) {
		String lsAcctType = colSs(ii, "stpm.is_proc_acct_type");
		liProcRows++;
		selectPtrStopparam(lsAcctType);
	}

	if (liProcRows == 0) {
		printf(" 無參數可供執行");
	}
}

//=****************************************************************************
void selectPtrStopparam(String aAcctType) throws Exception {
	int nSqlcode;
	hStpm.initData();
	hStpm.acctType =aAcctType;

	sqlCmd = "select acct_type , "
		+ " exec_mode ,"
		+ " valid_date ,"
		+ " param_type ,"
		+ " exec_date ,"	//最近執行日
		+ " uf_date_add(substr(exec_date,1,6),0,n0_month,0) as exec_date_2 , " //-最近執行日+執行間隔月
		+ " exec_day ,"
		+ " exec_cycle_nday ,"
		+ " mcode_value , "
		+ " debt_amt ,"
		+ " n0_month ,"
		+ " n1_cycle ,"
		+ " non_af , "
		+ " non_ri ,"
		+ " non_pn ,"
		+ " non_pf ,"
		+ " non_lf ,"
		+" pause_flag,"
		+ " hex(rowid) as rowid "
		+ " from ptr_stopparam "
		+ " where  valid_date <= ? "
		+ " and apr_flag = 'Y' "
		+ " and acct_type = ? "
		+ " and nvl(pause_flag,'x') = 'Y' "	//-N.暫不執行-
		+ " and param_type ='1' "
		+ " order by valid_date desc "
		+ commSqlStr.rownum(1);
	
	ppp(1, hBusiDate);
	ppp(2, hStpm.acctType);
	
	sqlSelect();
	
	if (sqlNrow < 0) {
		errmsg("select ptr_stopparam error, kk[%s]",hStpm.acctType);
		this.errExit(1);
	}

	if (sqlNrow == 0) {
		hStpm.validDate = hBusiDate;
		hStpm.paramType = "1";
		updateRskStopexec(1);
		sqlCommit(commit);
		swOk	="Y";
		return;
	}
	
   hStpm.execMode    =colSs("exec_mode");
   hStpm.validDate   =colSs("valid_date");
   hStpm.paramType   =colSs("param_type");
   hStpm.execDate    =colSs("exec_date");
   hTempExecDate    =commString.mid(colSs("exec_date_2"),0,6);
   hStpm.execDay     =colInt("exec_day");
   hStpm.execCycleNday =colInt("exec_cycle_nday");
   hStpm.mcodeValue =colInt("mcode_value");
   hStpm.debtAmt   =colNum("debt_amt");
   hStpm.n0Month   =colInt("n0_month");
   hStpm.n1Cycle   =colInt("n1_cycle");
   hStpm.nonAf  =colNvl("non_af","N");
   hStpm.nonRi  =colNvl("non_ri","N");
   hStpm.nonPn =colNvl("non_pn","N");
   hStpm.nonPf =colNvl("non_pf","N");
   hStpm.nonLf =colNvl("non_lf","N");
   hStpm.pauseFlag =colNvl("pause_flag","N");	//-暫不執行-
   hStpm.rowid =colSs("rowid");

   printf("-->acct_type[%s], valid_date[%s]",hStpm.acctType,hStpm.validDate);
   
	//--暫不執行------------------
//	if (eq_igno(h_stpm.exec_mode, "9")) {
	if (eqIgno(hStpm.pauseFlag, "N")) {
		updateRskStopexec(3);
		sqlCommit(commit);
		printf(" 暫不執行 =[%s]", hStpm.pauseFlag);
		swOk ="Y";
		return;
	}

	//每月固定一天 
	if (eqIgno(hStpm.execMode, "1")) {
		//-今天非執行日 switch on --
		if (hStpm.execDay != iiBusiDays) {
			printf(" 今天非執行日 =[%s]", hBusiDate);
			swOk ="Y";
			return;
		}
		/* 本月非執行月份 */
		if ( (hStpm.n0Month == 0) && 
				!eqIgno(hTempExecDate, commString.mid(hBusiDate,0, 6)) ) {
			printf(" 本月非執行月份 =[%s]", hBusiDate);
			swOk ="Y";
			return;
		}

		/* 每月固定執行日=營業日 */
		if (hStpm.execDate.length() == 0) /* 最近執行日=null(未曾執行過) */
		{
			printf(" 最近執行日='' =[%s]", hBusiDate);
			selectActAcno(0);
			return;
		}

		if (hStpm.n0Month == 0 || 
				!eqIgno(hTempExecDate, hBusiDate.substring(0, 6))) {
			printf(" 最近執行日+間隔月=營業年月 =[%s]", hBusiDate);
			selectActAcno(0);
			return;
		}
	}

	//-Cycle後n天 (icbc 強停固定設定為'2') ------------------------------
	if (eqIgno(hStpm.execMode, "2")) {
		nSqlcode = selectPtrWorkday();

		if (nSqlcode == 0) {
			swOk ="Y";
			printf(" Cycle後n天 不執行1, select_ptr_workday.NotFind");
			return;
		}
		//-GET多筆參數 switch on---------------------------
		if (nSqlcode > 1) {
			updateRskStopexec(2);
			sqlCommit(commit);
			swOk ="Y";
			printf(" Cycle後n天 不執行2, select_ptr_workday.Rows>1");
			return;
		}
		//-- 今天非執行月 switch on -------------
		if ((hStpm.n0Month != 0) && 
				!eq(hTempExecDate, hBusiDate.substring(0, 6)) ) {
			printf(" 不執行3, 今天非執行月=[%s]", hBusiDate);
			swOk ="Y";
			return;
		}
		//-- 第一次執行 ---------
		if (empty(hTempExecDate)) {
			printf(" 最近執行日='' =[%s]", hBusiDate);
			selectActAcno(1);
			return;
		}

		//-- 當月執行 ---------------
		printf(" 最近執行日+間隔月=營業年月 =[%s]", hBusiDate);
		selectActAcno(1);
	} /* if(exec_mode=2) */

	//- 每天 ------------------------------------------------------------
	if (eqIgno(hStpm.execMode, "3")) {
		selectActAcno(0);
	}
	
}

//=****************************************************************************
void updateRskStopexec(int aiFlag) throws Exception {
	String lsMsg = "";
	if (aiFlag == 1) {
		lsMsg = "強停參數不存在1";
	}
	else if (aiFlag == 2) {
		lsMsg = "符合之cycle日有2筆以上";
	}
	else if (aiFlag == 3) {
		lsMsg = "暫不執行";
	}
	
	if (tidStopexecU1 <= 0) {
		sqlCmd = "update rsk_stopexec set"
			+ " exec_msg = ? ,"
			+ " exec_mode = ? ,"
			+ commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where exec_date = ? "
			+ " and param_type = ?"
			+ " and acct_type = ?"
			+ " and valid_date = ?";
		daoTable ="tid_stopexec_U1";
		tidStopexecU1 =ppStmtCrt();
	}
	Object[] pp = new Object[] {
		lsMsg, 
		hStpm.execMode, 
		hBusiDate, 
		hStpm.paramType, 
		hStpm.acctType, 
		hStpm.validDate
	};
	sqlExec(tidStopexecU1,pp);

	if (sqlNrow < 0) {
		errmsg("update ptr_stopexec error");
		errExit(0);
	}

	if (sqlNrow >0) {
		return;
	}
	
	//-N-find: insert------
	if (tSexec.pfidx <=0) {
		tSexec.sqlFrom = "insert into rsk_stopexec ("
			+ " param_type ,"
			+ " acct_type ,"
			+ " valid_date ,"
			+ " exec_date ,"
			+ " exec_msg ,"
			+ " exec_mode ,"
			+ " exec_times ,"
			+ " mod_user ,"
			+ " mod_time ,"
			+ " mod_pgm ,"
			+ " mod_seqno "
			+ " ) values ("
			+ tSexec.pmkk(0, ":param_type ,")
			+ tSexec.pmkk(":acct_type ,")
			+ tSexec.pmkk(":valid_date ,")
			+ tSexec.pmkk(":exec_date ,")
			+ tSexec.pmkk(":exec_msg ,")
			+ tSexec.pmkk(":exec_mode ,")
			+ " '1' ,"
			+ tSexec.pmkk(":mod_user,")
			+ " sysdate ,"
			+ tSexec.pmkk(":mod_pgm,")
			+ " '1' "
			+ " )";
		tSexec.pfidx =ppStmtCrt("t_sexec-A1",tSexec.sqlFrom);
	}
	tSexec.parmSs("param_type", hStpm.paramType);
	tSexec.parmSs("acct_type", hStpm.acctType);
	tSexec.parmSs("valid_date", hStpm.validDate);
	tSexec.parmSs("exec_date", hBusiDate);
	tSexec.parmSs("exec_msg", lsMsg);
	tSexec.parmSs("exec_mode", hStpm.execMode);
	tSexec.parmSs("mod_user", hModUser);
	tSexec.parmSs("mod_pgm", hModPgm);

	Object[] pps = tSexec.getConvParm();
//	ddd(t_sexec.sql_from, pps);
	sqlExec(tSexec.pfidx, pps);
	if (sqlNrow <= 0) {
		errmsg("insert rsk_stopexec error");
		errExit(0);
	}
}

//=****************************************************************************
void selectActAcno(int aiFlag) throws Exception {
	int liHasPay = 0;

	//- 記錄 rsk_stopexec 之開頭記錄 --
	beginRskStopexec();
	sqlCommit(commit);

	sqlCmd = "select p_seqno ,acno_p_seqno, "
		+ " id_p_seqno, corp_p_seqno, "
		+ " acct_type , acct_key ,"
		+ " uf_date_add(c.this_acct_month,0,-1*?,0) as db_acct_month ," 
		+ " no_f_stop_flag ,"
		+ " no_f_stop_s_date ,"
		+ " no_f_stop_e_date ,"		//nvl('99991231') as 
		+ " pay_by_stage_flag ,"
		+ " payment_rate1 ,"
		+ " rc_use_indicator ,"
		+ " combo_indicator ,"		//nvl(N)
      +" int_rate_mcode, line_of_credit_amt,"
		+ " hex(a.rowid) as rowid "
		+ " from act_acno a ,ptr_workday c "
		+ " where a.stmt_cycle   = decode(cast(? as int),0,a.stmt_cycle,?) "
		+ " and c.stmt_cycle = a.stmt_cycle "
		+ " and nvl(stop_status,'') <>'Y' "
		+ " and a.acct_type = ? "
		+ " and A.acno_flag in ('1','3')"
		;

	printf("-->proc acct_type[%s], cycle[%s]", hStpm.acctType, hWday.stmtCycle);

	ppp(1, hStpm.mcodeValue);
	ppp(2, aiFlag);
	ppp(3,hWday.stmtCycle);
	ppp(4, hStpm.acctType);

//   ddd("acno:"+sqlCmd,get_sqlParm());

	this.fetchExtend = "aa.";
	daoTable ="select_act_acno";
	this.openCursor();

	while (fetchTable()) {
		resetDataAcno();
		hAclg.initData();

      hAcno.acnoPSeqno = colSs("aa.acno_p_seqno");
		hAcno.pSeqno = colSs("aa.p_seqno");
		hAcno.idPSeqno = colSs("aa.id_p_seqno");
		hAcno.corpPSeqno =colSs("aa.corp_p_seqno");
		hAcno.acctType = colSs("aa.acct_type");
//		h_acno.acct_key = col_ss("aa.acct_key");
		hWday.thisAcctMonth = colSs("aa.db_acct_month");
		hAcno.noFStopFlag = colSs("aa.no_f_stop_flag");
		hAcno.noFStopSDate = colSs("aa.no_f_stop_s_date");
		hAcno.noFStopEDate = colNvl("aa.no_f_stop_e_date","99991231");
		hAcno.payByStageFlag = colSs("aa.pay_by_stage_flag");
		hAcno.payRateSet(1,colSs("aa.payment_rate1"));
		hAcno.rcUseIndicator = colSs("aa.rc_use_indicator");
		hAcno.comboIndicator = colSs("aa.combo_indicator");
		hAcno.intRateMcode =colInt("aa.int_rate_mcode");
		hAcno.lineOfCreditAmt =colNum("aa.line_of_credit_amt");
		hAcno.rowid = colSs("aa.rowid");
		
		totalCnt++;
		if ((totalCnt % 10000) == 0) {
			printf(" 讀取筆數 =[%s]", totalCnt);
		}
		hTempStopWhy = "";
		hTempNotstopWhy = "";

		// 繳款評等>=參數Ma(Mcode) --
//		int li_mcode = commString.ss_2int(h_acno.payRate_get(1));
//		if (li_mcode < h_stpm.mcode_value)
//			continue;
		
		int liMcode = hAcno.intRateMcode;  //comr.getMcode(h_acno.acct_type, h_acno.p_seqno);
		if (liMcode < hStpm.mcodeValue)
			continue;

		hAclg.ccasMcodeAft =""+liMcode;

		//-check 強停金額: 本金結欠>=參數Xa--
		selectActAcctSum();

      //ddd("->p_seqno[%s], mcode[%s], amt[%s]",h_acno.p_seqno,li_mcode,h_temp_billed_end_bal);
		//-Get 本金結欠 --
      if (hTempBilledEndBal < hStpm.debtAmt) {
         hStecTStopnot4Cnt++;        
         hTempNotstopWhy ="S4";
         insertRskAcnolog(2);
         continue;
      }
      hAclg.acctJrnlBal =hTempBilledEndBal;

		//-前Ma僅欠設定之費用科目 --
		if (selectActDebt() == 0)
			continue;

		hStecTAcctCnt++; //應強停筆數 

		//- 應強停不強停(永不強停戶) -
		if ( eqIgno(hAcno.noFStopFlag, "Y") &&
            commString.ssComp(hBusiDate,hAcno.noFStopSDate)>=0 &&
            commString.ssComp(hBusiDate,hAcno.noFStopEDate) <=0 ) {
			hStecTStopnot1Cnt++;
			hTempNotstopWhy = "S1";
			insertRskAcnolog(2);
			continue;
		}

		//-- 應強停不強停(分期還款戶) --
		if (hAcno.payByStageFlag.length() != 0) {
			hStecTStopnot2Cnt++;
			hTempNotstopWhy = "S2";
			insertRskAcnolog(3);
			continue;
		}

		hTempStopWhy = "S";
		hTempNotstopWhy = "";
		selectActAnalSub();  //Get 本金結欠相關資料 
		selectActAcct(); 	//-Get 卡戶最近付款金額 --

		//-最近 N 個Cycle有付款者不強停-
		//--最近有付款-partI--
		if (hAcctPayAmt > 0) {
			hStecTStopnot3Cnt++; 	//-- 應強停但(h_stpm.n1_cycle)Cycle有付款 --
			hTempNotstopWhy = "S3";
			insertRskAcnolog(4);
			continue;
		}
		
		//--最近有付款-partII--
		liHasPay = 0;
		for (int ii=0; ii < hStpm.n1Cycle; ii++) {
			// 任一月有付款 --
			if (maHisPayAmt[ii] > 0) {
				liHasPay++;
				break;
			}
		}
		if (liHasPay > 0) {
			hStecTStopnot3Cnt++; //-- 應強停但(h_stpm.n1_cycle)Cycle有付款 --
			hTempNotstopWhy = "S3";
			insertRskAcnolog(4);
			continue;
		}

		//-- 應強停真強停戶 --
		hStecTStopCnt++;
		updateActAcno();
		stopCrdCard();
		insertRskAcnolog(0);

		prsCount++;
		if (prsCount % 10000 == 0) {
			printf(" --強停筆數 =[%s]", prsCount);
			updateRskStopexec2();
			sqlCommit(commit);
		}

	} //-for-

   printf("-->acct_type[%s], 處理強停筆數 =[%s]",hStpm.acctType,hStecTStopCnt);
	closeCursor();
	
	// 記錄 rsk_stopexec 之累計及結尾記錄(目前逐筆) --
	updateRskStopexec2();
	updatePtrStopparam();
}

//=****************************************************************************
int selectPtrWorkday() throws Exception {

	hWday.stmtCycle = "";
	hWday.lastAcctMonth = "";
	
	daoTable = "";
	daoTid ="wday.";
	// 本次關帳日=營業日- Cycle後n日 --
	sqlCmd = " SELECT stmt_cycle ,"
		+ " last_acct_month"
		+ " from ptr_workday "
		+ " where this_close_date = ?" //to_char(to_date(?,'yyyymmdd') - ? , 'yyyymmdd') "
		;
	String lsYmd =commDate.dateAdd(hBusiDate,0,0,0 - hStpm.execCycleNday);
	ppp(1,lsYmd);
	sqlSelect();

	ddd("-->關帳日期=[%s]",lsYmd);
	if (sqlNrow <0) {
		errmsg("select select_ptr_workday error, [%s]",lsYmd);
		this.errExit(0);
	}
	else if (sqlNrow >0) {
      hWday.stmtCycle =colSs("wday.stmt_cycle");
      hWday.lastAcctMonth =colSs("wday.last_acct_month");
   }

	return sqlNrow;
}

//=****************************************************************************
void beginRskStopexec() throws Exception {

	hStecRowid = "";
	hStecTAcctCnt = 0;
	hStecTStopCnt = 0;
	hStecTStopnot1Cnt = 0;
	hStecTStopnot2Cnt = 0;
	hStecTStopnot3Cnt = 0;
	hStecTStopnot4Cnt = 0;

	sqlCmd = " SELECT t_acct_cnt ,"
		+ " t_stop_cnt ,"
		+ " t_stopnot1_cnt ,"
		+ " t_stopnot2_cnt ,"
		+ " t_stopnot3_cnt ,"
		+ " t_stopnot4_cnt ,"
		+ " hex(rowid) as rowid  "
		+ " from rsk_stopexec "
		+ " where exec_date =? "
		+ " and param_type =? "
		+ " and acct_type =? "
		+ " and valid_date =? ";

	ppp(1, hBusiDate);
	ppp(2, hStpm.paramType);
	ppp(3, hStpm.acctType);
	ppp(4, hStpm.validDate);
	daoTid ="stec.";
	sqlSelect();
	if (sqlNrow < 0) {
		errmsg("select ptr_stopparam error");
		errExit(0);
	}

	//- 該日第一執行--
	if (sqlNrow == 0) {
		insertRskStopexec();
		return;
	}
	else {
		hStecRowid = colSs("stec.rowid");
		hStecTAcctCnt = colInt("stec.t_acct_cnt");
		hStecTStopCnt = colInt("stec.t_stop_cnt");
		hStecTStopnot1Cnt =colInt("stec.t_stopnot1_cnt");
		hStecTStopnot2Cnt =colInt("stec.t_stopnot2_cnt");;
		hStecTStopnot3Cnt =colInt("stec.t_stopnot3_cnt");;
		hStecTStopnot4Cnt =colInt("stec.t_stopnot4_cnt");;
	}

	sqlCmd = "update rsk_stopexec set"
		+ " param_type = '1' ,"
		+ " valid_date = ? ,"
		+ " exec_date = ? ,"
		+ " exec_mode = ? ,"
		+ " exec_times = exec_times+1 ,"
		+ " exec_date_s = sysdate , "
		+commSqlStr.setModXxx(hModUser,hModPgm)
		+ " where rowid = ? ";
	Object[] pp = new Object[] {
		hStpm.validDate, hBusiDate, hStpm.execMode,
		commSqlStr.ss2rowid(hStecRowid)
	};
	sqlExec(pp);

	if (sqlNrow <= 0) {
		printf("update rsk_stopexec error");
		this.exitProgram(1);
	}
}

//=****************************************************************************
void insertRskStopexec() throws Exception {

	sqlCmd = "insert into rsk_stopexec ("
			+ " param_type ,"
			+ " acct_type ,"
			+ " valid_date ,"
			+ " exec_date ,"
			+ " exec_mode ,"
			+ " exec_times ,"
			+ " exec_date_s ,"
			+ " exec_msg ,"
			+ " mod_user ,"
			+ " mod_time ,"
			+ " mod_pgm ,"
			+ " mod_seqno "
			+ " ) values ("
			+ " '1' ,"
			+ "?,"	//acct_type
			+ " ?," //:valid_date ,")
			+ " ?," //:exec_date ,")
			+ " ?," //:exec_mode ,")
			+" 1,"	//exec_times
			+ " sysdate ,"
			+ " ?," //:h_wday.stmt_cycle ,")
			+ " ?," //:mod_user")
			+ " sysdate ,"
			+ " ?," //:mod_pgm")
			+ " 1)";
	ppp(1, hStpm.acctType);
	ppp(hStpm.validDate);
	ppp(hBusiDate);
	ppp(hStpm.execMode);
	ppp("Cycle="+hWday.stmtCycle);
	ppp(this.hModUser);
	ppp(this.hModPgm);

	ddd(sqlCmd,this.getSqlParm());
	sqlExec(sqlCmd);
	if (sqlNrow <= 0) {
		errmsg("insert rsk_stopexec error");
		errExit(0);
	}
}

//=****************************************************************************
void resetDataAcno() {
   hAcno.acnoPSeqno = "";
	hAcno.pSeqno = "";
	hAcno.idPSeqno = "";
	hAcno.acctType = "";
//	h_acno.acct_key = "";
	hWday.thisAcctMonth = "";
	hAcno.noFStopFlag = "";
	hAcno.noFStopSDate = "";
	hAcno.noFStopEDate = "";
	hAcno.payByStageFlag = "";
	hAcno.rcUseIndicator = "";
	hAcno.comboIndicator = "";
	hAcno.rowid = "";
	hAcno.initPaymentRate();
}

//=****************************************************************************
void selectActAcctSum() throws Exception {

	hTempBilledEndBal = 0;

	if (tidAcctSumS1 <=0) {
		sqlCmd = " SELECT nvl(sum(nvl(billed_end_bal,0)),0) as tl_bill "
			+ " from act_acct_sum "
			+ " where p_seqno =? "
			+ " and acct_code in ('BL','CA','IT','ID','AO','OT','CB','DB') ";
		daoTable ="tid_acct_sum_S1";
		tidAcctSumS1 =ppStmtCrt();
	}
	ppp(1, hAcno.pSeqno);

	sqlSelect(tidAcctSumS1);
	if (sqlNrow < 0) {
		errmsg("select act_acct_sum error");
		this.errExit(0);
	}
	
	if (sqlNrow ==0)
		return;
	
	hTempBilledEndBal = colNum("tl_bill");
}

//=****************************************************************************
int selectActDebt() throws Exception {

	if (tidDebtS1 <= 0) {
		sqlCmd = " SELECT count(*) as db_cnt "
			+ " from act_debt "
			+ " where p_seqno =? "
			+ " and acct_code <> decode(cast(? as char(1)),'Y','AF','00') "
			+ " and acct_code <> decode(cast(? as char(1)),'Y','RI','00') "
			+ " and acct_code <> decode(cast(? as char(1)),'Y','PN','00') "
			+ " and acct_code <> decode(cast(? as char(1)),'Y','PF','00') "
			+ " and acct_code <> decode(cast(? as char(1)),'Y','LF','00') "
			+ " and acct_month <= ? "
			+ " and end_bal > 0 ";
		daoTable ="tid_debt_S1";
		tidDebtS1 =ppStmtCrt();
	}
	ppp(1, hAcno.pSeqno);
	ppp(2, hStpm.nonAf);
	ppp(3, hStpm.nonRi);
	ppp(4, hStpm.nonPn);
	ppp(5, hStpm.nonPf);
	ppp(6, hStpm.nonLf);
	ppp(7, hWday.thisAcctMonth);
	sqlSelect(tidDebtS1);
	if (sqlNrow < 0) {
		errmsg("act_debt.count() error");
		this.errExit(0);
	}
	
	if (colInt("db_cnt")==0)
		return 0;

	return 1;
}

private com.Parm2sql ttAclg=null;
//=======================================================
void insertRskAcnolog(int aiFlag) throws Exception {
// 應強停不強停原因(1商務'取消此條件',2'S1永不強停/S4結欠本金足'          
//                 ,3還款'S2',4最近個月有付款'S3')
	
	String lsKindFlag = "";
	String lsCardNo = "";
	if (aiFlag == 5) {
		lsKindFlag = "C";
		lsCardNo = hAclgCardNo;
	}
	else {
		lsKindFlag = "A";
		lsCardNo = "";
		selectCcaCardAcct(hAcno.pSeqno);
	}

	if (ttAclg ==null) {
		ttAclg = new Parm2sql();
		ttAclg.insert("rsk_acnolog");
	}
	ttAclg.aaa("kind_flag",lsKindFlag);
	ttAclg.aaa("card_no",lsCardNo);
	ttAclg.aaa("acct_type",hAcno.acctType);
	ttAclg.aaa("acno_p_seqno",hAcno.acnoPSeqno);
	ttAclg.aaa("id_p_seqno",hAcno.idPSeqno);
	ttAclg.aaa("corp_p_seqno", hAcno.corpPSeqno);
	ttAclg.aaa("log_date",hBusiDate);
	ttAclg.aaa("log_mode","2");
	ttAclg.aaa("log_type","2");
	ttAclg.aaa("log_reason", hTempStopWhy);
	ttAclg.aaa("log_not_reason", hTempNotstopWhy);
	ttAclg.aaa("aft_loc_amt", hAcno.lineOfCreditAmt);
	ttAclg.aaa("acct_jrnl_bal",hAclg.acctJrnlBal);
	ttAclg.aaa("ccas_mcode_aft",hAclg.ccasMcodeAft);
	ttAclg.aaa("block_reason",hAclg.blockReason);
	ttAclg.aaa("block_reason2",hAclg.blockReason2);
	ttAclg.aaa("block_reason3",hAclg.blockReason3);
	ttAclg.aaa("block_reason4",hAclg.blockReason4);
	ttAclg.aaa("block_reason5",hAclg.blockReason5);
	if (empty(hTempNotstopWhy))
		ttAclg.aaa("fit_cond","N");
	else ttAclg.aaa("fit_cond","");
	ttAclg.aaaModxxx(hModUser,hModPgm);
   if (ttAclg.ti <=0) {
		ttAclg.ti =ppStmtCrt("tt_aclg-A",ttAclg.getConvSQL());
	}

	Object[] pps = ttAclg.getConvParm();
//	ddd(t_aclg.sql_from, pps);
	sqlExec(ttAclg.ti, pps);
	if (sqlNrow <= 0) {
		errmsg("insert rsk_acnolog error, kk[%s]",hAcno.acnoPSeqno);
		errExit(0);
	}
}

void selectCcaCardAcct(String aPseqno) throws Exception {
   if (tiCcat<0) {
      sqlCmd ="select block_reason1"+
         ", block_reason2, block_reason3, block_reason4, block_reason5"
         +" from cca_card_acct"+
         " where acno_p_seqno =? and debit_flag <>'Y'"+
      commSqlStr.rownum(1);
      tiCcat =ppStmtCrt("ti-ccat","");
   }
   ppp(1,aPseqno);
   sqlSelect(tiCcat);
   if (sqlNrow >0) {
      hAclg.blockReason =colSs("block_reason1");
      hAclg.blockReason2 =colSs("block_reason2");
      hAclg.blockReason3 =colSs("block_reason3");
      hAclg.blockReason4 =colSs("block_reason4");
      hAclg.blockReason5 =colSs("block_reason5");
   }

}
//=****************************************************************************
void selectActAnalSub() throws Exception {
	for(int ii=0; ii<maHisPayAmt.length; ii++)
		maHisPayAmt[ii] =0;
	
	if (tidAnalSubS<=0) {
		sqlCmd = " SELECT his_pay_amt "
			+ " from act_anal_sub "
			+ " where p_seqno =? "
			+ " and acct_month between ? and ?"
			//+ commSqlStr.ufunc("uf_date_add(?,0,0-?,0) and ? ")
			+ " order  by acct_month desc ";
		tidAnalSubS =ppStmtCrt();
	}
	
	String lsYymm1 =commDate.dateAdd(hWday.lastAcctMonth,0,0 - hStpm.n1Cycle,0).substring(0,6);
	String lsYymm2 =hWday.lastAcctMonth;
	
	ppp(1, hAcno.pSeqno);
	ppp(2, lsYymm1);
	ppp(3, lsYymm2);

//	ddd_sql(tid_anal_sub_S);
	sqlSelect(tidAnalSubS);
	if (sqlNrow < 0) {
		errmsg("select_act_anal_sub error, kk[%s]",hAcno.pSeqno);
		this.errExit(1);
	}
	int llNrow =sqlNrow;
	for (int ll=0; ll<llNrow; ll++) {
		maHisPayAmt[ll] =colNum(ll,"his_pay_amt");
	}
}

//=****************************************************************************
void selectActAcct() throws Exception {
	
	hAcctPayAmt = 0;
	
	if (tidAcctS <=0) {
		sqlCmd = " SELECT pay_amt"
			+ " from act_acct "
			+ " where p_seqno =? ";
		daoTable ="tid_acct_S";
		tidAcctS =ppStmtCrt();
	}
	
	ppp(1, hAcno.pSeqno);
	sqlSelect(tidAcctS);
	if (sqlNrow < 0) {
		errmsg("select act_acct error ");
		this.errExit(0);
	}

	if (tidAcctHstS <=0) {
		sqlCmd = " SELECT sum(stmt_payment_amt) as aa_pay_amt "
			+ " from act_acct_hst "
			+ " where p_seqno =? "
			+" and acct_month between ? and ?"
			//+ " and acct_month between uf_date_add(?,0,0 - ?,0) and ? "
			;
		daoTable ="tid_acct_hst_S";
		tidAcctHstS =ppStmtCrt();
	}
	String lsYymm2 =hWday.lastAcctMonth;
	String lsYymm1 =commString.mid(commDate.dateAdd(hWday.lastAcctMonth,0, 0 - hStpm.n1Cycle,0),0,6);
	
	ppp(1, hAcno.pSeqno);
	ppp(lsYymm1);
	ppp(lsYymm2);
	
	sqlSelect(tidAcctHstS);
	if (sqlNrow>0 && colNum("aa_pay_amt") > 0) {
		hAcctPayAmt += colNum("aa_pay_amt");
	}
	return;
}

//=****************************************************************************
void updateActAcno() throws Exception {

	if (tidAcnoU <= 0) {
		sqlCmd = "update act_acno set"
			+ " stop_reason = 'J2' ,"
			+ " stop_status = 'Y' ,"
			+commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where rowid = ? ";
		daoTable ="tid_acno_U";
		tidAcnoU =this.ppStmtCrt();
	}
	Object[] pp = new Object[] {
		commSqlStr.ss2rowid(hAcno.rowid)
	};
	
	ddd("-->p_seqno="+hAcno.pSeqno);
	sqlExec(tidAcnoU,pp);
	if (sqlNrow <= 0) {
		errmsg("update act_acno error");
		errExit(0);
	}
}

//=****************************************************************************
void stopCrdCard() throws Exception {
	String lsOppostDate = "";
	int liValid =0;
	
	//-check validable card-
	if (tidCardS <=0) {
		sqlCmd = " SELECT sum(decode(current_code,'0',1,0)) as li_valid ,"
			+ "  max(oppost_date) as oppo_date "
			+ " from crd_card "
			+ " where p_seqno =? ";
		daoTable ="tid_card_S";
		tidCardS =ppStmtCrt();
	}
	
	ppp(1, hAcno.pSeqno);
	sqlSelect(tidCardS);
	if (sqlNrow < 0) {
		printf("check valid-card error, kk[%s]",hAcno.pSeqno);
		this.errExit(1);
	}
	if (sqlNrow == 0)
		return;
	
	lsOppostDate = colSs("oppo_date");
	liValid = colInt("li_valid");

//	ddd(" --li_valid[%s], ls_oppost_date[%s]", li_valid, ls_oppost_date);

	//-強停有效卡- 
	if (liValid > 0) {
		stopCrdCardValid();
		return;
	}

	if (lsOppostDate.length() == 0)
		return;

	//--無有效卡:更改最大停卡日之所有卡片停用原因----
	if (tid2CardS <= 0) {
		sqlCmd = " select "
			+ " card_no, "
			+ " oppost_date, "
			+ " oppost_reason, "
			+ " major_id_p_seqno, "
			+ " sup_flag, "
			+ " id_p_seqno, "
			+ " new_end_date, "
			+ " reissue_date, "
			+ " mail_type, "
			+ " mail_no, "
			+ " mail_branch, "
			+ " mail_proc_date, "
			+ " card_type, "
			+ " group_code, "
			+ " combo_indicator, "
			+ " hex(rowid) as rowid "
			+ " from crd_card "
			+ " where p_seqno = ?"
			+ " and oppost_date = ?";
		daoTable ="tid2_card_S";
		tid2CardS =ppStmtCrt();
	}
	
	ppp(1, hAcno.pSeqno);
	ppp(2, lsOppostDate);
	sqlSelect(tid2CardS);
	int llNrow =sqlNrow;
	for (int ll = 0; ll < llNrow; ll++) {
		resetDataCard();
		hCard.cardNo = colSs(ll, "card_no");
		hCard.oppostDate = colSs(ll, "oppost_date");
		hCard.oppostReason = colSs(ll, "oppost_reason");
		hCard.majorIdPSeqno = colSs(ll, "major_id_p_seqno");
		hCard.supFlag = colSs(ll, "sup_flag");
		hCard.idPSeqno = colSs(ll, "id_p_seqno");
		hCard.newEndDate = colSs(ll, "new_end_date");
		hCard.reissueDate = colSs(ll, "reissue_date");
		hCard.mailType = colSs(ll, "mail_type");
		hCard.mailNo = colSs(ll, "mail_no");
		hCard.mailBranch = colSs(ll, "mail_branch");
		hCard.mailProcDate = colSs(ll, "mail_proc_date");
		hCard.cardType = colSs(ll, "card_type");
		hCard.groupCode = colSs(ll, "group_code");
		hCard.comboIndicator = colSs(ll, "combo_indicator");

		ddd("--無有效卡: card_no[%s], oppost_date[%s]\n", hCard.cardNo, hCard.oppostDate);

		hAclgCardNo = hCard.cardNo;

		updateCrdCard();
		insertRskAcnolog(5);
		insertCrdJcic();

//		select_crd_idno(h_card.major_id_p_seqno);
		if (eqIgno(hCard.supFlag, "0")) {
			hCard.idPSeqno = "";
		}
		insertColStopLog();
	} //--while --
}

//=****************************************************************************
void stopCrdCardValid() throws Exception {
	if (tid3CardS < 0) {
		sqlCmd = " SELECT card_no ,"
			+ " oppost_date ,"
			+ " oppost_reason ,"
			+ " major_id_p_seqno ,"
			+ " sup_flag ,"
			+ " id_p_seqno ,"
			+ " new_end_date ,"
			+ " reissue_date ,"
			+ " mail_type ,"
			+ " mail_no ,"
			+ " mail_branch ,"
			+ " mail_proc_date ,"
			+ " card_type ,"
			+ " group_code ,"
			+ " combo_indicator ,"
			+ " hex(rowid) as rowid "
			+ " from crd_card "
			+ " where p_seqno =? "
			+ " and current_code='0'";
		daoTable ="tid3_card_S";
		tid3CardS =ppStmtCrt();
	}
	
	ppp(1, hAcno.pSeqno);
	daoTid = "card.";
	sqlSelect(tid3CardS);

	int llNrow =sqlNrow;
	for (int ii = 0; ii < llNrow; ii++) {
		resetDataCard();
		hCard.cardNo = colSs(ii, "card.card_no");
		hCard.oppostDate = colSs(ii, "card.oppost_date");
		hCard.oppostReason = colSs(ii, "card.oppost_reason");
		hCard.majorIdPSeqno = colSs(ii, "card.major_id_p_seqno");
		hCard.supFlag = colSs(ii, "card.sup_flag");
		hCard.idPSeqno = colSs(ii, "card.id_p_seqno");
		hCard.newEndDate = colSs(ii, "card.new_end_date");
		hCard.reissueDate = colSs(ii, "card.reissue_date");
		hCard.mailType = colSs(ii, "card.mail_type");
		hCard.mailNo = colSs(ii, "card.mail_no");
		hCard.mailBranch = colSs(ii, "card.mail_branch");
		hCard.mailProcDate = colSs(ii, "card.mail_proc_date");
		hCard.cardType = colSs(ii, "card.card_type");
		hCard.groupCode = colSs(ii, "card.group_code");
		hCard.comboIndicator = colSs(ii,"card.combo_indicator");

		hAclgCardNo = hCard.cardNo;
		if (hCard.oppostDate.length() > 0)
			continue;
		updateCrdCard();
		insertRskAcnolog(5);
		insertCrdJcic();
		
//		select_crd_idno(h_card.major_id_p_seqno);
		if (eqIgno(hCard.supFlag, "0")) {
			hCard.idPSeqno = "";
			if (eqIgno(hCard.comboIndicator, "Y")) {
				insertCrdStopLog();
			}
		}
//		else {
//			//-附卡送CCAS-
//			insertOnbat2ccas();
//		}
		insertCrdApscard();
		insertColStopLog();
	}
}

private com.Parm2sql ttOnba=null;
//=============================================
void insertOnbat2ccas() throws Exception {
	if (ttOnba==null) {
		ttOnba =new com.Parm2sql();
		ttOnba.insert("onbat_2ccas");
	}
	ttOnba.aaa("trans_type","6");
	ttOnba.aaa("to_which",2);
	ttOnba.aaaDtime("dog");
	ttOnba.aaa("proc_mode","B");
	ttOnba.aaa("proc_status",0);
	ttOnba.aaa("card_no", hCard.cardNo);
	ttOnba.aaa("acct_type",hCard.acctType);
	ttOnba.aaa("card_catalog",hCard.comboIndicator);
	ttOnba.aaa("id_p_seqno",hCard.idPSeqno);
	ttOnba.aaa("acno_p_seqno",hCard.acnoPSeqno);
	ttOnba.aaa("opp_type","1");
	ttOnba.aaa("opp_reason","Q3");
	ttOnba.aaaYmd("opp_date");
	ttOnba.aaa("debit_flag","N");
	ttOnba.aaa("mod_pgm",hModPgm);

	if (ttOnba.ti <=0) {
		ttOnba.ti =ppStmtCrt("tt_onba-A",ttOnba.getConvSQL());
	}

	sqlExec(ttOnba.ti,ttOnba.getConvParm());
	if (sqlNrow <= 0) {
		errmsg("insert onbat_2ccas error, kk[%s]",hCard.cardNo);
		errExit(0);
	}
}

// ***************************************************************************
void resetDataCard() {
	hCard.cardNo = "";
	hCard.oppostDate = "";
	hCard.oppostReason = "";
	hCard.majorIdPSeqno = "";
	//h_card.major_id = "";
	//h_card.major_id_code = "";
	hCard.supFlag = "";
	hCard.idPSeqno = "";
	//h_card.id = "";
	//h_card.id_code = "";
	hCard.newEndDate = "";
	hCard.reissueDate = "";
	hCard.mailType = "";
	hCard.mailNo = "";
	hCard.mailBranch = "";
	hCard.mailProcDate = "";
	hCard.cardType = "";
	hCard.groupCode = "";
	hCard.comboIndicator = "";
}

// ***************************************************************************
void updateCrdCard() throws Exception {
	//-sup0=J2,3; sup1=Q3,1-
	if (tidCardU <=0) {
		sqlCmd = "update crd_card set"
			+ " oppost_reason = decode(sup_flag,'1','Q3','J2') ,"
			+ " oppost_date = decode(oppost_date,'',?,oppost_date) , "
			+ " current_code = decode(sup_flag,'1','1','3') ,"
			+ commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where card_no = ? ";
		daoTable ="tid_card_U";
		tidCardU =ppStmtCrt();
	}
	ppp(1,hBusiDate);
	ppp(hCard.cardNo);
	
	sqlExec(tidCardU);
	if (sqlNrow <= 0) {
		errmsg("update crd_card error; kk[%s]",hCard.cardNo);
		this.errExit(0);
	}
	
	//--
	hCard.oppostReason ="J2";	
	if (eq(hCard.supFlag,"1")) hCard.oppostReason="Q1";
	hFiscReason = getFiscReason(hCard.oppostReason);
	if (empty(hCard.oppostDate)) hCard.oppostDate =hBusiDate;
	//--寫入停用
	insertCcaOpposition();
	//--停用報送Outgoing
	ccaOutGoing.InsertCcaOutGoing(hCard.cardNo, hCard.currentCode, sysDate, hCard.oppostReason);
}

String getFiscReason(String oppostReason) throws Exception {
	
	String sql1 = "select fisc_opp_code from cca_opp_type_reason where opp_status = ? ";
	sqlSelect(sql1,new Object[] {oppostReason});
	
	if(sqlNrow > 0 ) {
		return colSs("fisc_opp_code");
	}
	
	return "";
}

com.Parm2sql ttOppo=null;
void insertCcaOpposition() throws Exception {
	if (ttOppo ==null) {
		ttOppo =new com.Parm2sql();
		ttOppo.insert("cca_opposition");
	}

	String lsOppType="3";
	if (eq(hCard.supFlag,"1")) {
		lsOppType ="1";
	}

	double llCcasIndx =getCcasIndex(hCard.cardNo);

	ttOppo.aaa("card_no", hCard.cardNo);
	ttOppo.aaa("card_acct_idx", llCcasIndx);
	ttOppo.aaa("debit_flag", "N");
	ttOppo.aaa("card_type", hCard.cardType);
	ttOppo.aaa("bin_type", hCard.binType);
	ttOppo.aaa("group_code", hCard.groupCode);
	ttOppo.aaa("from_type", "0");
	ttOppo.aaa("oppo_type", lsOppType);
	ttOppo.aaa("oppo_status", hCard.oppostReason);
	ttOppo.aaa("oppo_user", hModUser);
	ttOppo.aaa("oppo_date", hCard.oppostDate);
	ttOppo.aaa("oppo_time", sysTime);
	ttOppo.aaa("neg_del_date", hCard.newEndDate);
	ttOppo.aaa("renew_flag", "");
	ttOppo.aaa("renew_urgen", "");
	ttOppo.aaa("opp_remark", "欠款強停");
//	ttOppo.aaa("outgo_proc", "0");
	ttOppo.aaa("fisc_reason_code", hFiscReason);
	ttOppo.aaaYmd("crt_date");
	ttOppo.aaaTime("crt_time");
	ttOppo.aaa("crt_user", hModPgm);
	ttOppo.aaaModxxx(hModUser,hModPgm);

	if (ttOppo.ti <=0) {
		ttOppo.ti =ppStmtCrt("tt_oppo.A",ttOppo.getConvSQL());
	}

	sqlExec(ttOppo.ti,ttOppo.getConvParm());
	if (sqlNrow <=0) {
		if (sqlDuplRecord) {
			updateCcaOpposition(lsOppType);
			return;
		}
		sqlerr("insert cca_opposition error");
		errExit();
	}
}

com.Parm2sql ttOppo2=null;
void updateCcaOpposition(String aOppoType) throws Exception {
	if (ttOppo2 ==null) {
		ttOppo2 =new Parm2sql();
		ttOppo2.update("cca_opposition");
	}

	ttOppo2.aaa("from_type", "0");
	ttOppo2.aaa("oppo_type", aOppoType);
	ttOppo2.aaa("oppo_status", hCard.oppostReason);
	ttOppo2.aaa("oppo_user", hModUser);
	//tt_oppo2.aaa("oppo_date", h_card.oppost_date);
	//tt_oppo2.aaa("oppo_time", hh.oppo_time);
	ttOppo2.aaa("logic_del", "");
	ttOppo2.aaa("logic_del_date", "");
	ttOppo2.aaa("logic_del_time", "");
	ttOppo2.aaa("logic_del_user", "");
//	ttOppo2.aaa("outgo_proc", "0");
	ttOppo2.aaa("fisc_reason_code", hFiscReason);
	ttOppo2.aaaYmd("chg_date");
	ttOppo2.aaaTime("chg_time");
	ttOppo2.aaa("chg_user", hModUser);
	ttOppo2.aaaModxxx(hModUser,hModPgm);
	ttOppo2.aaaWhere(" where card_no =?", hCard.cardNo);

	if (ttOppo2.ti <=0) {
		ttOppo2.ti =ppStmtCrt("tt_oppo2.U",ttOppo2.getConvSQL());
	}

	sqlExec(ttOppo2.ti,ttOppo2.getConvParm());
	if (sqlNrow <=0) {
		printf("update cca_opposition error, kk[%s]", hCard.cardNo);
	}
}

int tiCcidx=-1;
double getCcasIndex(String aCardNo) throws Exception {
	if (tiCcidx <=0) {
		sqlCmd ="select card_acct_idx from cca_card_base"+
				" where card_no =?";
		tiCcidx =ppStmtCrt("ti_ccidx.S",sqlCmd);
	}

	ppp(1,aCardNo);
	sqlSelect(tiCcidx);
	if (sqlNrow <=0)
		return 0;

	return colNum("card_acct_idx");
}

//=***************************************************************************
void insertCrdJcic() throws Exception {
	if (tidJcicS <= 0) {
		sqlCmd = " select hex(rowid) as h_cdjc_rowid "
			+ " from crd_jcic "
			+ " where card_no =? "
			+ " and nvl(to_jcic_date,'') = '' "
			+ " and trans_type ='C' "
			+ commSqlStr.rownum(1)
			;
		daoTable ="tid_jcic_S";
		tidJcicS =ppStmtCrt();
	}
	ppp(1, hCard.cardNo);
	sqlSelect(tidJcicS);
	if (sqlNrow < 0) {
		printf("select_crd_jcic error");
		this.errExit(0);
	}
	if (sqlNrow>0)
		hCdjcRowid = colSs("h_cdjc_rowid");
	else hCdjcRowid ="";
	
	String lsTransType="C";
	String lsCurrentCode ="3";
	String lsOppoReason ="J2";
	if (!eq(hCard.supFlag,"0")) {
		lsTransType ="C";
		lsCurrentCode ="1";
		lsOppoReason ="Q3";
	}
	
	if (empty(hCdjcRowid)) {
		if (tCrdJcic ==null) {
			tCrdJcic =new table.CrdJcic();
			tCrdJcic.stopBatch();
			daoTable ="t_crd-Jcic";
			sqlCmd =tCrdJcic.sqlFrom;
			tCrdJcic.pfidx =ppStmtCrt();
		}

		tCrdJcic.ppp("card_no",hCard.cardNo);
		tCrdJcic.ppp("crd_user",hModUser);
		tCrdJcic.ppp("oppost_date",hBusiDate);
		tCrdJcic.ppp("is_rc",hAcno.rcUseIndicator);
		tCrdJcic.ppp("crt_user",this.hModUser);
		tCrdJcic.ppp("mod_user",this.hModUser);
		tCrdJcic.ppp("mod_pgm",this.hModPgm);

		//		ddd(t_crdJcic.sql_from,t_crdJcic.get_convParm(false));
		sqlExec(tCrdJcic.pfidx,tCrdJcic.getConvParm());
		if (sqlNrow<=0) {
			errmsg("insert crd_jcic error, kk[%s]",hCard.cardNo);
			errExit(0);
		}
		return;
	}
	
	//-update---------------------------------------------
	if (tidJcicU <=0) {
		sqlCmd = "update crd_jcic set"
			+ " current_code = ?,"	//'3' ,"
			+ " oppost_reason = ?,"	//'J2' ,"
			+ " oppost_date = ?,"
			+ " is_rc = ?,"
			+commSqlStr.setModXxx(hModUser,hModPgm)
			+ " where rowid = ? ";
		daoTable ="tid_jcic_U";
		tidJcicU =ppStmtCrt();
	}
	Object[] pp = new Object[] {
		lsCurrentCode,
		lsOppoReason,
		hBusiDate, 
		hAcno.rcUseIndicator,
		commSqlStr.ss2rowid(hCdjcRowid)
	};
	sqlExec(tidJcicU,pp);
	if (sqlNrow < 0) {
		printf("update crd_jcic-U1 error");
		errExit(0);
	}
}

//=***************************************************************************
void selectCrdIdno(String asIdPseqno, String[] aaIdno) throws Exception {
	//return 0.id, 1.id_code, 2.name, 3.bir_date
//	h_idno_chi_name = "";
//	h_idno_birthday = "";
//	h_idno_id_no ="";
//	h_idno_id_code ="";
	aaIdno=new String[]{"","","",""};
	
	if (tidIdnoS <= 0) {
		sqlCmd = " select chi_name , birthday , id_no , id_no_code"
			+ " from crd_idno "
			+ " where id_p_seqno = ?";
		daoTable ="tid_idno_S";
		tidIdnoS =ppStmtCrt();
	}
	ppp(1, asIdPseqno);
	sqlSelect(tidIdnoS);
	if (sqlNrow <= 0) {
		printf("select crd_idno error, kk=[%s]",asIdPseqno);
		errExit(0);
	}
	
	aaIdno[0] = colSs("id_no");
	aaIdno[1] = colSs("id_no_code");
   aaIdno[2] = colSs("chi_name");
	aaIdno[3] = colSs("birthday");
}

//=****************************************************************************
Parm2sql ttStop=null;
void insertCrdStopLog() throws Exception {
	// for combo card --
	if (ttStop==null) {
		ttStop =new Parm2sql();
		ttStop.insert("crd_stop_log");
	}
	ttStop.aaaFunc("proc_seqno",commSqlStr.seqEcsStop,"");
	ttStop.aaa("crt_time",sysDate+sysTime);
	ttStop.aaa("card_no", hCard.cardNo);
	ttStop.aaa("current_code", "3");
	ttStop.aaa("oppost_reason", "J2");
	ttStop.aaa("oppost_date", hBusiDate);
	ttStop.aaa("trans_type", "03");
	ttStop.aaa("send_type", "2");
	ttStop.aaaModxxx(hModUser,hModPgm);

	if (ttStop.ti <=0) {
		ttStop.ti =ppStmtCrt("tt-stop-A",ttStop.getConvSQL());
	}

	sqlExec(ttStop.ti,ttStop.getParms());
	if (sqlNrow <=0) {
		printf("insert crd_stop_log-A2 error; kk[%s]",hCard.cardNo);
		errExit(0);
	}
}

//=****************************************************************************
void insertCrdApscard() throws Exception {
	String[] aaMIdno=new String[]{"","","",""};
	String[] aaSIdno=new String[]{"","","",""};
	
	selectCrdIdno(hCard.majorIdPSeqno,aaMIdno);
	if (eqIgno(hCard.supFlag, "1")) {
		selectCrdIdno(hCard.idPSeqno,aaSIdno);
	}

	if (tApsc.pfidx <=0) {
		tApsc.sqlFrom = "insert into crd_apscard ("
			+ " crt_datetime ,"
			+ " card_no ,"
			+ " valid_date ,"
			+ " stop_date ,"
			+ " reissue_date ,"
			+ " stop_reason ,"
			+ " mail_type ,"
			+ " mail_no ,"
			+ " mail_branch ,"
			+ " mail_date ,"
			+ " pm_id ,"
			+ " pm_id_code ,"
			+ " pm_birthday ,"
			+ " sup_id ,"
			+ " sup_id_code ,"
			+ " sup_birthday ,"
			+ " corp_no ,"
			+ " corp_no_code ,"
			+ " card_type ,"
			+ " pm_name ,"
			+ " sup_name ,"
			+ " group_code ,"
			+ " mod_user ,"
			+ " mod_time ,"
			+ " mod_pgm ,"
			+ " mod_seqno "
			+ " ) values ("
			+ " to_char(sysdate,'yyyymmddhh24miss') ,"
			+ tApsc.pmkk(0, ":card_no ,")
			+ tApsc.pmkk(":valid_date ,")
			+ tApsc.pmkk(":stop_date ,")
			+ tApsc.pmkk(":reissue_date ,")
			+ " '1' ,"
			+ tApsc.pmkk(":mail_type ,")
			+ tApsc.pmkk(":mail_no ,")
			+ tApsc.pmkk(":mail_branch ,")
			+ tApsc.pmkk(":mail_date ,")
			+ tApsc.pmkk(":pm_id ,")
			+ tApsc.pmkk(":pm_id_code ,")
			+ tApsc.pmkk(":pm_birthday ,")
			+ tApsc.pmkk(":sup_id ,")
			+ tApsc.pmkk(":sup_id_code ,")
			+ tApsc.pmkk(":sup_birthday ,")
			+ tApsc.pmkk(":corp_no ,")
			+ tApsc.pmkk(":corp_no_code ,")
			+ tApsc.pmkk(":card_type ,")
			+ tApsc.pmkk(":pm_name ,")
			+ tApsc.pmkk(":sup_name ,")
			+ tApsc.pmkk(":group_code ,")
			+ tApsc.pmkk(":mod_user ,")
			+ " sysdate ,"
			+ tApsc.pmkk(":mod_pgm ,")
			+ " '1' "
			+ " )";
		daoTable = "crd_apscard-A1";
		sqlCmd =tApsc.sqlFrom;
		tApsc.pfidx =ppStmtCrt();
	}
	tApsc.parmSs("card_no", hCard.cardNo);
	tApsc.parmSs("valid_date", hCard.newEndDate);
	tApsc.parmSs("stop_date", hBusiDate);
	tApsc.parmSs("reissue_date", hCard.reissueDate);
	tApsc.parmSs("mail_type", hCard.mailType);
	tApsc.parmSs("mail_no", hCard.mailNo);
	tApsc.parmSs("mail_branch", hCard.mailBranch);
	tApsc.parmSs("mail_date", hCard.mailProcDate);
	tApsc.parmSs("pm_id", aaMIdno[0]);
	tApsc.parmSs("pm_id_code", aaMIdno[1]);
	tApsc.parmSs("pm_birthday", aaMIdno[3]);
	tApsc.parmSs("sup_id", aaSIdno[0]);
	tApsc.parmSs("sup_id_code", aaSIdno[1]);
	tApsc.parmSs("sup_birthday", aaSIdno[3]);
	tApsc.parmSs("corp_no", hCard.corpNo);
	tApsc.parmSs("corp_no_code", hCard.corpNoCode);
	tApsc.parmSs("card_type", hCard.cardType);
	tApsc.parmSs("pm_name", aaMIdno[2]);
	tApsc.parmSs("sup_name", aaSIdno[2]);
	tApsc.parmSs("group_code", hCard.groupCode);
	tApsc.parmSs("mod_user", this.hModUser);
	tApsc.parmSs("mod_pgm", this.hModPgm);

	Object[] pps = tApsc.getConvParm();
	ddd(tApsc.sqlFrom, pps);
	sqlExec(tApsc.pfidx, pps);
	if (sqlNrow <= 0) {
		errmsg("insert crd_apscard-A1 error, kk[%s]",hCard.cardNo);
		errExit(0);
	}
}

//=***************************************************************************
void updateRskStopexec2() throws Exception {
	sqlCmd = "update rsk_stopexec set"
		+ " exec_date_e = sysdate ,"
		+ " t_acct_cnt = ? ,"
		+ " t_stop_cnt = ? ,"
		+ " t_stopnot1_cnt = ? ,"
		+ " t_stopnot2_cnt = ? ,"
		+ " t_stopnot3_cnt = ? ,"
		+ " t_stopnot4_cnt = ? ,"
		+ commSqlStr.setModXxx(hModUser, hModPgm)
		+ " where exec_date = ? "
		+ " and param_type = ? "
		+ " and acct_type = ? "
		+ " and valid_date = ?";
	Object[] pp = new Object[] {
		hStecTAcctCnt, 
		hStecTStopCnt, 
		hStecTStopnot1Cnt, 
		hStecTStopnot2Cnt, 
		hStecTStopnot3Cnt, 
		hStecTStopnot4Cnt, 
		hBusiDate, hStpm.paramType, hStpm.acctType, hStpm.validDate
	};
	ddd(sqlCmd,pp);
	sqlExec(pp);
	if (sqlNrow < 0) {
		printf("update rsk_stopexec-U2 error");
		errExit(0);
	}
}

//=***************************************************************************
void updatePtrStopparam() throws Exception {
	sqlCmd = "update ptr_stopparam set"
		+ " exec_date = ? "
		+ " where rowid = ? ";

	Object[] pp = new Object[] {
		hBusiDate, commSqlStr.ss2rowid(hStpm.rowid)
	};
	sqlExec(pp);
	if (sqlNrow < 0) {
		printf("update ptr_stopparam-U1 error");
		errExit(0);
	}
}

//=***************************************************************************
Parm2sql ttCslog=null;
void insertColStopLog() throws Exception {
	if (ttCslog ==null) {
		ttCslog =new Parm2sql();
		ttCslog.insert("col_stop_log");
	}

	String[] aaMidno=new String[]{"","","",""};
	String[] aaIdno=new String[]{"","","",""};
	selectCrdIdno(hCard.majorIdPSeqno,aaMidno);
	if (eqIgno(hCard.supFlag, "1")) {
		selectCrdIdno(hCard.idPSeqno,aaIdno);
	}

	ttCslog.aaa("insert_date", hBusiDate);
	ttCslog.aaa("proc_date", "");
	ttCslog.aaa("major_id_p_seqno", hCard.majorIdPSeqno);
	ttCslog.aaa("id_p_seqno", hCard.idPSeqno);
	ttCslog.aaa("p_seqno", hCard.pSeqno);
	ttCslog.aaa("major_id", aaMidno[0]);
	ttCslog.aaa("major_id_code", aaMidno[1]);
	ttCslog.aaa("id_no", aaIdno[0]);
	ttCslog.aaa("id_code", aaIdno[1]);
	ttCslog.aaa("card_no", hCard.cardNo);
	ttCslog.aaa("oppost_date", hBusiDate);
	ttCslog.aaa("proc_mark", "");
	ttCslog.aaaDtime("mod_time");
	if (ttCslog.ti <=0) {
		ttCslog.ti =ppStmtCrt("tt-cslog.A",ttCslog.getConvSQL());
	}

	sqlExec(ttCslog.ti,ttCslog.getParms());
	if (sqlNrow <= 0) {
		printf("insert col_stop_log error, kk[%s]",hCard.cardNo);
		errExit(0);
	}
}

}
