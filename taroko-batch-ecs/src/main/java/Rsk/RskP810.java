package Rsk;
/**
 * 2020-0120  V1.00.00	JH		  rsk_acnolog.fit_cond
 * 2020-0108  V1.00.01  JH       modify
 * 2019-0722  V1.00.02  JH        bug-fix
 * 2019-0718  V1.00.03  JH        update crd_card
 * 2019-0625: V1.00.04  JH        p_xxx >>acno_p_xxx
 * 106/12/25  V1.00.05  Alex      initial                                        *
 * 2018-0517  V1.00.06  JHW       modify                                         *
 * 2018-0614  V1.00.07  JHW       暫停執行
 * 2019-0318  V1.00.08  JH        getMcode-->int_rate_mcode
 * 109-11-19  V1.00.09  tanwei    updated for project coding standard
 * 110-12-13  V1.00.10  Alex      凍結送Outgoing
 * */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import Cca.CcaOutGoing;
import com.*;

public class RskP810 extends BaseBatch {
private String progname = "整批凍結處理程式  111/12/13 V1.00.10";
CommRoutine comr = null;
CommCrdRoutine comcr  =null;
CcaOutGoing ccaOutGoing = null;
//=======================================================================
hdata.ActAcno hAcno=new hdata.ActAcno();
hdata.ActDebt hDebt=new hdata.ActDebt();
hdata.CrdCard hCard=new hdata.CrdCard();
hdata.CcaCardAcct hCcat=new hdata.CcaCardAcct();
hdata.PtrBlockparam hBkpm=new hdata.PtrBlockparam();
hdata.RskAcnoLog hAclg=new hdata.RskAcnoLog();
hdata.RskBlockexec hBkec =new hdata.RskBlockexec();

//-------------------------------------------
private String hCorpChiName = "";
private String hWdayStmtCycle = "";
private String hAclgCardNo = "";
private String hAclgFitCond = "";
private String hWdayThisAcctMonth="";
//----------------------------------------------------
private String hExecMode = "2";
//private String is_proc_acct_type = "";
private String swOk = "";
private String swPrint = "";
private String hTempGlCode = "";
private String hTempExecYymm = "";
private double idcDebtAmt = 0;
private double idcDebtFee = 0;
private String hTempBlockWhy = "";
private String hTempNotblockWhy = "";
private String isBlockReason = "";
private String isBlockReason4 = "";
private String hCcasRowid = "";
//==print=====================================================
private int pageCnt = 0 ;
private String buf = "";
List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
private String reportID = "";
private String reporName = "";
private String reportDTime="";
private int rptSeq1 = 0;
private double pageAmt = 0;
private double totalAmt = 0 ;
//private int i_file_name = 0;
//---------------------------------------------------------------------------
private int prsCount=0;
private int printCnt=0;
private String hBusiYymm;
private String hBusiDd;
private int hTempConfirmCnt=0;
//============================================================
private int tidBlockexecU=-1;
private int tid2BlockexecA=-1;
private int tidCcasAcctS=-1;
private int tidAcctSum=-1;
private int tidCardS=-1;
private int tidDebtS=-1;
private int tidAcajS=-1;
private int tidAcajD=-1;
private int tidAcajerrA=-1;
private int tidCcaAcctU=-1;
private int tidBlexecU2=-1;
private int tidIdnoS=-1;
private int tidCorpS=-1;
private int tiCard1=-1;
int tiCardU=-1;

int commit=1;
//=****************************************************************************
public static void main(String[] args) {
	RskP810 proc = new RskP810();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit(0);
}

//=****************************************************************************
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	if (args.length > 2) {
		printf("Usage : RskP810 [exec_mode, busi_date]");
		printf("param_type: 1.每月固定日執行, 2.CYCLE後N天; default(2)");
		errExit(1);
	}

	//-connect-
   dbConnect();
   comr = new CommRoutine(getDBconnect(), getDBalias());
   comcr =new com.CommCrdRoutine(getDBconnect(),getDBalias());
   ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
   
   if (args.length == 1) {
		hExecMode = args[0];
	}
	else if (args.length ==2) {
		hExecMode = args[0];
		this.setBusiDate(args[1]);
	}

	if (!eqIgno(hExecMode, "1") && !eqIgno(hExecMode, "2")) {
		printf("執行方式[h_exec_mode=%s], 只可為1或2", hExecMode);
		errExit(1);
	}

	swOk = "N";
	swPrint = "Y";

	hBusiYymm =commString.mid(hBusiDate,0,6);
	hBusiDd =commString.mid(hBusiDate,6,2);
	
//	selectPtrDeptCode();
//  已取消
//	reportID ="RSK_P810R1";
//	reporName ="符合Mg凍結但無有效卡逾期未繳自動D檔報表";
//	reportDTime=this.sysDate+sysTime;
	
	//-main---
	selectAcctType(); 
	sqlCommit(commit);

//	if (lpar1.size() == 0) {
//		printHeader();
//	}
//	printFooter();
//	printTotal();
//	
//	if(lpar1.size()>0){
//		comcr.insertPtrBatchRpt(lpar1);
//	}
//	lpar1.clear();
	ccaOutGoing.finalCnt2();
	this.endProgram();
}

//=****************************************************************************
void selectPtrDeptCode() throws Exception {
	hTempGlCode = "";
	sqlCmd = "select lpad(gl_code,2,'0') as h_temp_gl_code "
		+ " from ptr_dept_code "
		+ " where dept_code ='CL' ";
	sqlSelect();

	if (sqlNrow <= 0) {
		sqlerr("select_ptr_dept_code error");
		errExit(0);
	}

	hTempGlCode = colSs("h_temp_gl_code");
}

//=****************************************************************************
void selectAcctType() throws Exception {
	int liProcRow = 0;
	
	daoTid = "acty.";
	sqlCmd = " select distinct acct_type"
		+ " from ptr_blockparam "
		+ " where nvl(apr_flag,'N') ='Y' "
//		+ " and nvl(pause_flag,'X') <> 'Y' "
		+ " and param_type ='1' "
		+ " and exec_mode = ? "
		+ " order by acct_type ";
	ppp(1,hExecMode);
	sqlSelect();

	if (sqlNrow <= 0) {
		errmsg("select ptr_blockparam error or no-find");
		errExit(1);
	}

	int rrSelect = sqlNrow;

	String lsProcAcctType="";
	for (int ll = 0; ll < rrSelect; ll++) {
		lsProcAcctType = colSs(ll, "acty.acct_type");
		liProcRow++;
		swOk = "N";
		selectPtrBlockparam(lsProcAcctType);
		if (eqIgno(swOk, "N"))
			updatePtrBlockparam();
	}

	if (liProcRow == 0) {
		swOk = "Y";
		errmsg("無參數可供執行");
		errExit(0);
	}
}

//=****************************************************************************
void selectPtrBlockparam(String aAcctType) throws Exception {
	//init_BlockParm();
	hBkpm.initData();
	
	daoTid = "bkpm.";
	sqlCmd = " select A.* , "
		+ " hex(A.rowid) as rowid "
		+ " from ptr_blockparam A "
		+ " where valid_date <= ? "
		+ " and acct_type = ? "
		+ " and exec_mode = ? "
		+ " and param_type = '1' "
		+ " and apr_flag = 'Y' "
		+ " order by valid_date desc "
		+ commSqlStr.rownum(1);
	
	ppp(1,hBusiDate);
	ppp(aAcctType);
	ppp(hExecMode);
	sqlSelect();
	if (sqlNrow < 0) {
		printf("select ptr_blockparm error !, kk[%s]",aAcctType);
		errExit(1);
	}
	if (sqlNrow == 0) {
		hBkpm.validDate = hBusiDate;
		hBkpm.paramType = "1";
		swOk = "Y";
		updateRskBlockexec(1);
		return;
	}
	
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
	hBkpm.debtFee3 = colInt("bkpm.debt_fee3");
	hBkpm.n0Month = colInt("bkpm.n0_month");
	hBkpm.execCycleNday = colInt("bkpm.exec_cycle_nday");
	hBkpm.execFlagM1 = colSs("bkpm.exec_flag_m1");
	hBkpm.execFlagM2 = colSs("bkpm.exec_flag_m2");
	hBkpm.execFlagM3 = colSs("bkpm.exec_flag_m3");
	hBkpm.execFlagM14 = colSs("bkpm.exec_flag_m14");
	hBkpm.mcodeValue14 = colInt("bkpm.mcode_value14");
	hBkpm.debtAmt14 = colInt("bkpm.debt_amt14");
	hBkpm.blockReason14 = colSs("bkpm.block_reason14");
	hBkpm.execFlagM24 = colSs("bkpm.exec_flag_m24");
	hBkpm.mcodeValue24 = colInt("bkpm.mcode_value24");
	hBkpm.debtAmt24 = colInt("bkpm.debt_amt24");
	hBkpm.blockReason24 = colSs("bkpm.block_reason24");
	hBkpm.execFlagM34 = colSs("bkpm.exec_flag_m34");
	hBkpm.mcodeValue34 = colInt("bkpm.mcode_value34");
	hBkpm.debtAmt34 = colInt("bkpm.debt_amt34");
	hBkpm.debtFee34 = colInt("bkpm.debt_fee34");
	hBkpm.blockReason34 = colSs("bkpm.block_reason34");
	hBkpm.pauseFlag =colSs("bkpm.pause_flag");
	hBkpm.rowid = colSs("bkpm.rowid");
	
	printf("==>凍結參數: 生效日[%s], acct_type[%s]",hBkpm.validDate,aAcctType);
	if ( !eqIgno(hBkpm.pauseFlag,"Y")) {
		printf("-->凍結參數: 暫停執行中.....");
		swOk ="Y";
		return;
	}
	
	//最近執行日+執行間隔月--
	//TO_CHAR(ADD_MONTHS(TO_DATE(SUBSTR(exec_date,1,6), 'yyyymm'), n0_month),'yyyymm'),
	hTempExecYymm = commDate.monthAdd(hBkpm.execDate,hBkpm.n0Month);

	//-Ma,Mb,Mc 均不檢核--
	if ( !eqIgno(hBkpm.execFlagM1, "Y")
		&& !eqIgno(hBkpm.execFlagM14, "Y")
		&& !eqIgno(hBkpm.execFlagM2, "Y")
		&& !eqIgno(hBkpm.execFlagM24, "Y")
		&& !eqIgno(hBkpm.execFlagM3, "Y")
		&& !eqIgno(hBkpm.execFlagM34, "Y") ) {
		printf(" Ma,Mb,Mc 均不檢核, acct_type[%s], valid_date[%s]", 
				hBkpm.acctType, hBusiDate);
		swOk = "Y"; /* 今天非執行日 switch on */
		return;
	}

	//-1.每月固定一天--------------------------------------------------------
	if (eqIgno(hBkpm.execMode, "1")) {
		//-每月固定執行日<>營業日-
		int liDd =commString.ss2int(hBusiDd);
		if (hBkpm.execDay != liDd) {
			printf(" 今日非執行日(exec_mode=1)=[%s]", hBusiDate);
			swOk = "Y"; /* 今天非執行日 switch on */
			return;
		}

		//-最近執行日=null(未曾執行過)-------------------------------
		if (empty(hBkpm.execDate)) {
			printf(" 最近執行日='' =[%s]", hBusiDate);
			selectActAcno(0);
		}
		else {
			if (eqIgno(hTempExecYymm, hBusiYymm)) {
				printf(" 最近執行日+間隔月=營業年月 =[%s]", hBusiDate);
				selectActAcno(0);
			}
			else {
				printf(" 最近執行日+間隔月<>營業年月[%s]", hBusiDate);
				swOk = "Y"; //-今天非執行月 switch on --
				return;
			}
		}
		
		//- 每月固定一天 --
		return;
	} 

	//-Cycle後n天 (icbc 凍結固定設定為'2')-------------------
	if (eqIgno(hBkpm.execMode, "2")) {

		int llNrow =selectPtrWorkday();
		if ( llNrow== 0) {
			swOk = "Y"; // GET 0 筆參數 switch on  
			printf("Cycle後n天 不執行1, business_date=[%s], acct_type[%s]", hBusiDate, aAcctType);
			return;
		}

		if (llNrow > 1) {
			updateRskBlockexec(2);
			swOk = "Y"; // GET多筆參數 switch on 
			printf(" Cycle後n天 不執行2, busi_date=[%s]. acct_type[%s]", hBusiDate, aAcctType);
			return;
		}

		if (empty(hTempExecYymm)) {
			printf(" 最近執行日='' busi_date=[%s], acct_type[%s]", hBusiDate, aAcctType);
			selectActAcno(1);
		}
		else {
			if ( hBkpm.n0Month == 0 ||
				 eqIgno(hTempExecYymm,hBusiYymm) ) {
				printf(" 最近執行日+間隔月=營業年月 =[%s], acct_type[%s]", hBusiDate, aAcctType);
				selectActAcno(1);
			}
			else {
				printf(" 不執行3, n0_month=[%d] busi_date=[%s] acct_typep[%s]", 
						hBkpm.n0Month, hBusiDate, aAcctType);
				swOk = "Y"; //-- 今天非執行月 switch on --
				return;
			}
		}
		
		//-- exec_mode=2 --
		return;
	} 

	//---每天執行============================================
	if (eqIgno(hBkpm.execMode, "3")) {
		selectActAcno(0);
		return;
	}
	//---暫不執行======================================
	if (eqIgno(hBkpm.execMode, "9")) {
		updateRskBlockexec(3);
		printf(" 暫不執行? =[%s]", hBkpm.execMode);
		swOk = "Y"; //-- switch on --
		return;
	}
}


//=****************************************************************************
void updatePtrBlockparam() throws Exception {
	sqlCmd = " update ptr_blockparam set "
		+ " exec_date = ? "
		+ " where rowid = ?";

	sqlExec(new Object[]{
		hBusiDate,
		commSqlStr.ss2rowid(hBkpm.rowid)
	});
	if (sqlNrow <= 0) {
		printf("ptr_blockparam.update error");
		errExit(0);
	}
}

//=****************************************************************************
private com.Parm2sql ttExecA=null;
void updateRskBlockexec(int aiFlag) throws Exception {
	String lsMsg = "";
	if (aiFlag == 1)
		lsMsg = "凍結參數不存在";
	else if (aiFlag == 2)
		lsMsg = "符合之cycle日有2筆以上";
	else if (aiFlag == 3)
		lsMsg = "暫不執行";

	if (tidBlockexecU <= 0) {
		sqlCmd = " update rsk_blockexec set "
			+ " exec_msg = ? ,"
			+ " exec_mode = ? ,"
			+commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where exec_date = ? "
			+ " and param_type = ? "
			+ " and acct_type = ? "
			+ " and valid_date = ? "
			+ " and exec_mode = ? ";
		tidBlockexecU =ppStmtCrt("tid_blockexec_U","");
	}
	
	ppp(1, lsMsg);
	ppp(hBkpm.execMode);
	//kk--
	ppp(hBusiDate);
	ppp(hBkpm.paramType);
	ppp(hBkpm.acctType);
	ppp(hBkpm.validDate);
	ppp(hBkpm.execMode);
	sqlExec(tidBlockexecU);
	if (sqlNrow < 0) {
		printf("update ptr_blockexec error");
		errExit(0);
	}

	if (sqlNrow != 0)
		return;

	//---Insert rsk_block--
   if (ttExecA ==null) {
      ttExecA =new com.Parm2sql();
      ttExecA.insert("rsk_blockexec");
   }
   ttExecA.aaa("param_type",hBkpm.paramType);
   ttExecA.aaa("acct_type" ,hBkpm.acctType);
   ttExecA.aaa("valid_date" ,hBkpm.validDate);
   ttExecA.aaa("exec_date" ,hBusiDate);
   ttExecA.aaa("exec_msg" ,lsMsg);
   ttExecA.aaa("exec_mode" ,hBkpm.execMode);
   ttExecA.aaa("exec_times",1);
   ttExecA.aaaModxxx(hModUser,hModPgm);
   if (ttExecA.ti <=0) {
		ttExecA.ti =ppStmtCrt("tid2_blockexec_A",ttExecA.getSql());
	}

	sqlExec(ttExecA.ti,ttExecA.getParms());
	if (sqlNrow <= 0) {
		errmsg("insert rsk_blockexec1 error");
		this.errExit(0);
	}
}

//=****************************************************************************
void selectActAcno(int aiFlag) throws Exception {
	int liIsBlock = 0, liIsBlock4 = 0;

	selectRskBlockexec(); //-- 記錄 rsk_blockexec 之開頭記錄 --

	this.fetchExtend = "aa.";
	sqlCmd = " select "
		+ " A.p_seqno ,A.acno_p_seqno, "
		+ " A.id_p_seqno , "
		+ " A.corp_p_seqno , "
		+ " A.acct_type , "
		+ " A.acct_key , "
		+ " A.no_block_flag , "
		+ " A.no_block_s_date , "
		+ " A.no_block_e_date ,"
		+ " A.pay_by_stage_flag , "
		+ " A.payment_rate1 , "
		+ " A.card_indicator "
      +", A.int_rate_mcode "
         + ", hex(A.rowid) as acno_rowid "
         + ", B.block_reason1"
         + ", B.block_reason2"
         + ", B.block_reason3 "
         + ", B.block_reason4 "
         + ", B.block_reason5 "
         + ", B.block_status , B.spec_status "
         + ", hex(B.rowid) as ccas_rowid	"
		+ " from act_acno A join cca_card_acct B on B.acno_p_seqno=A.acno_p_seqno and B.debit_flag<>'Y'"
		+ " where 1=1"
		+ " and nvl(A.stop_status,'') ='' "
		+ " and A.acct_type = ? "
		+ " and A.acno_flag in ('1','3') "		
		+ " and A.acno_p_seqno = '0007004361' "
		;

	ppp(1,hBkpm.acctType);
	if (eq(hBkpm.execMode,"2")) {
      sqlCmd +=" and A.stmt_cycle =?";
      ppp(2,hWdayStmtCycle);
   }

	daoTable ="open-acno";
	this.openCursor();

	while (fetchTable()) {
      hAcno.acnoPSeqno = colSs("aa.acno_p_seqno");
		hAcno.pSeqno = colSs("aa.p_seqno");
		hAcno.idPSeqno = colSs("aa.id_p_seqno");
		hAcno.corpPSeqno = colSs("aa.corp_p_seqno");
		hAcno.acctType = colSs("aa.acct_type");
//		h_acno.acct_key = col_ss("aa.acct_key");
		hAcno.noBlockFlag = colSs("aa.no_block_flag");
		hAcno.noBlockSDate = colSs("aa.no_block_s_date");
		hAcno.noBlockEDate = colNvl("aa.no_block_e_date","99991231");
		hAcno.payByStageFlag = colSs("aa.pay_by_stage_flag");
		hAcno.payRateSet(1,colSs("aa.payment_rate1"));
		hAcno.cardIndicator = colSs("aa.card_indicator");
		hAcno.intRateMcode =colInt("aa.int_rate_mcode");
//		h_acno.rowid = col_ss("aa.rowid");
      //------------------------------------------
      hCcat.blockReason1 = colSs("aa.block_reason1");
      hCcat.blockReason2 = colSs("aa.block_reason2");
      hCcat.blockReason3 = colSs("aa.block_reason3");
      hCcat.blockReason4 = colSs("aa.block_reason4");
      hCcat.blockReason5 = colSs("aa.block_reason5");
      hCcat.specStatus = colSs("aa.spec_status");
      hCcasRowid = colSs("aa.ccas_rowid");

		totalCnt++;
		if (totalCnt % 10000 == 0) {
			printf(" 讀取筆數 =[%s]", totalCnt);
		}
		
		//-get block_reason--
//		select_cca_card_acct();
//		if (col_empty("ccas_rowid")) continue;

		//---Mcode 0A,0B,0C,0D,0E,0, skip-
		if (commString.ss2int(hAcno.payRateGet(1)) == 0)
			continue;
		//int li_mcode = comr.getMcode(h_acno.acct_type, h_acno.p_seqno);
      int liMcode = hAcno.intRateMcode;
		if (liMcode == 0)
			continue;

		selectActAcctSum(); //-- Get 本金結欠相關資料 -
		
		//---無有效卡 D 檔, 只欠費用-
		if (selectCrdCard() == 0) {
		   //-連動D檔: 新系統取消-----------------
//			if (li_mcode <= h_bkpm.mcode_value4)
//				continue;
//			if (idc_debt_amt > 0 || idc_debt_fee > h_bkpm.debt_amt4)
//				continue;
//			select_act_debt();
			continue;
		}
		
		isBlockReason = hCcat.blockReason1;
		//-- 該帳戶有效卡數 > 0 , 才需執行凍結 --
		hTempBlockWhy = "";
		hTempNotblockWhy = "";
		isBlockReason4 = "";
		hAclgFitCond = "N";
		liIsBlock = 0;
		liIsBlock4 = 0;

		//---Mc- 
		if ( eq(hBkpm.execFlagM3, "Y") 
			&& liMcode >= hBkpm.mcodeValue3 ) {
			if ( idcDebtAmt > hBkpm.debtAmt3  ||  idcDebtFee > hBkpm.debtFee3 ) {
				hTempBlockWhy = "Mc";
				hCcat.blockReason1 = "31";
				liIsBlock = 1;
			}
		}
		//---Mc34- 
		if ( eq(hBkpm.execFlagM34, "Y")
			&& liMcode >= hBkpm.mcodeValue34 ) {
			if (idcDebtAmt > hBkpm.debtAmt34 || idcDebtFee > hBkpm.debtFee34) {
				hTempBlockWhy = "Mc";
				isBlockReason4 = hBkpm.blockReason34;
				liIsBlock4 = 3;
			}
		}
		//---Mb-
		if ( eq(hBkpm.execFlagM2, "Y")
			&& liMcode == hBkpm.mcodeValue2 
			&& idcDebtAmt > hBkpm.debtAmt2 ) {
			hTempBlockWhy = "Mb";
			hCcat.blockReason1 = "21";
			liIsBlock = 1;
		}

		//---Mb24--
		if ( eq(hBkpm.execFlagM24, "Y")
			&& liMcode == hBkpm.mcodeValue24
			&& idcDebtAmt > hBkpm.debtAmt24 ) {
			hTempBlockWhy = "Mb";
			isBlockReason4 = hBkpm.blockReason24;
			liIsBlock4 = 2;
		}

		//---Ma--
		if ( eq(hBkpm.execFlagM1, "Y") 
			&& liMcode == hBkpm.mcodeValue1 
			&& idcDebtAmt > hBkpm.debtAmt1 ) {
			hTempBlockWhy = "Ma";
			hCcat.blockReason1 = "11";
			liIsBlock = 1;
		}
		//---Ma14- 
		if ( eq(hBkpm.execFlagM14, "Y")
			&& liMcode == hBkpm.mcodeValue14
			&& idcDebtAmt > hBkpm.debtAmt14 ) {
			hTempBlockWhy = "Ma";
			isBlockReason4 = hBkpm.blockReason14;
			liIsBlock4 = 1;
		}

		if (liIsBlock4 > 0) {
			if (checkIsBlock4() == 0)
				liIsBlock4 = 0;
		}

		//---重複--
		if ( empty(hCcat.blockReason1) ||
				eq(isBlockReason, hCcat.blockReason1) ) {
			liIsBlock = 0;
		}
		if (liIsBlock4 > 0) {
			if (eq(isBlockReason4, hCcat.blockReason2)
				|| eq(isBlockReason4, hCcat.blockReason3)
				|| eq(isBlockReason4, hCcat.blockReason4)
				|| eq(isBlockReason4, hCcat.blockReason5))
				liIsBlock4 = 0;
		}

		//---no block--
		if (liIsBlock == 0 && liIsBlock4 == 0)
			continue;
		
		//--- 執行帳戶凍結 --
		hBkec.tAcctCnt++; //-- 應凍結筆數 --
		if (liIsBlock4 > 0) {
			hCcat.blockReason4 = isBlockReason4;
		}

		//-- 應凍不動(永不凍結戶) --
		if ( eq(hAcno.noBlockFlag, "Y") 
			&& hAcno.noBlockSDate.compareTo(hBusiDate) <=0 
			&& hAcno.noBlockEDate.compareTo(hBusiDate) >=0 ) {
			hBkec.tBlocknot1Cnt++;
			hTempNotblockWhy = "B1";
			insertRskAcnolog(2);
			continue;
		}
		//-- 應凍不動(分期還款戶) --
		if ( eq(hAcno.payByStageFlag, "00")) {
			hBkec.tBlocknot2Cnt++;
			hTempNotblockWhy = "B2";
			insertRskAcnolog(3);
			continue;
		}

		if (liIsBlock>0)
			hAclg.emendType ="1";
		else if (liIsBlock4>0)
			hAclg.emendType ="4";
		//-- 應凍結真凍結戶 --
		if (eq(hCcat.blockReason1, "31"))
			hBkec.tBlockCnt3++;
		if (eq(hCcat.blockReason1, "21"))
			hBkec.tBlockCnt2++;
		if (eq(hCcat.blockReason1, "11"))
			hBkec.tBlockCnt++;
		if (liIsBlock == 0 && liIsBlock4 > 0) {
			if (liIsBlock4 == 1) {
				hBkec.tBlockCnt++;
				hTempBlockWhy = "Ma4";
			}
			if (liIsBlock4 == 2) {
				hBkec.tBlockCnt2++;
				hTempBlockWhy = "Mb4";
			}
			if (liIsBlock4 == 3) {
				hBkec.tBlockCnt3++;
				hTempBlockWhy = "Mc4";
			}
		}

		updateCcaCardAcct();
		insertRskAcnolog(4);
		//-卡片:outgoing-
		selectCrdCard1(hAcno.acnoPSeqno, liIsBlock4,hCcat.blockReason1);

		prsCount++;
		if (prsCount % 3000 == 0) {
			printf("凍結筆數 =[%s]", prsCount);
		}
	} //-- for-
	
	this.closeCursor();
	//--- 記錄 rsk_blockexec 之累計及結尾記錄(目前逐筆) --
	updateRskBlockexec2();

}
//=****************************************************************************
void selectCrdCard1(String aPSeqno, int aiBlock4, String blockCode) throws Exception {
   if (tiCard1 <0) {
      sqlCmd ="select card_no, combo_indicator"
            +", uf_card_indicator(acct_type) as card_indicator"
            +", acct_type, id_p_seqno"
            +", block_code, sup_flag"
            +" from crd_card"
            +" where p_seqno =? and acno_flag <>'Y'"
            +" and current_code ='0'";
      tiCard1 =ppStmtCrt("ti_card1","");
   }

   daoTid ="card.";
   ppp(1,aPSeqno);
   sqlSelect(tiCard1);
   int llNrow =sqlNrow;
   for(int ll=0; ll<llNrow; ll++) {
      //-outgoing-
	  ccaOutGoing.InsertCcaOutGoingBlock(colSs(ll,"card.card_no"), colSs(ll,"card.current_code"), sysDate, hCcat.blockReason1);
      //-combo-
      String lsCombo=colSs(ll,"card.combo_indicator");
      if (eq(lsCombo,"N") || empty(lsCombo))
         continue;
      if (eq(colSs(ll,"card.sup_flag"),"0")==false)
         continue;
      insertCrdStopLog(colSs(ll,"card.card_no"));
   }
   if (llNrow >0)
      updateCrdCard(aPSeqno);
}

//=================================================
//--傳送CCAS由RskP650執行
//void insert_onbat_2ccas() throws Exception {
//   if (tt_onbat ==null) {
//      tt_onbat =new com.SqlParm();
//      tt_onbat.insert("onbat_2ccas");
//      tt_onbat.aaa("trans_type","'2'");
//      tt_onbat.aaa(", to_which",", 2");
//      tt_onbat.aaa(", dog",","+commSqlStr.sys_dTime);
//      tt_onbat.aaa(", proc_mode",", 'B'");
//      tt_onbat.aaa(", proc_status",", 0");
//      tt_onbat.aaa("",",?","card_catalog");
//      tt_onbat.aaa("",",?","acct_type");
//      tt_onbat.aaa("",",?","id_p_seqno");
//      tt_onbat.aaa("",",?","card_no");
//      tt_onbat.aaa("",",?","block_code_4");
//      tt_onbat.aaa(", match_flag",", '4'");
//      tt_onbat.aaa(", match_date",","+commSqlStr.sys_YYmd);
//      tt_onbat.aaa(", debit_flag",", 'N'");
//      tt_onbat.aaa(" )");
//      tt_onbat.pfidx =ppStmt_crt("tt_onbat",tt_onbat.sql_from);
//   }
//   tt_onbat.ppp("card_catalog",col_ss("card.card_indicator"));
//   tt_onbat.ppp("acct_type",col_ss("card.acct_type"));
//   tt_onbat.ppp("id_p_seqno",col_ss("card.id_p_seqno"));
//   tt_onbat.ppp("card_no",col_ss("card.card_no"));
//   tt_onbat.ppp("block_code_4",h_ccat.block_reason4);
//   sqlExec(tt_onbat.pfidx, tt_onbat.get_convParm());
//   if (sql_nrow <=0) {
//      sqlerr("insert onbat_2ccas error[%s]",col_ss("card.card_no"));
//      err_exit();
//   }
//}

private com.Parm2sql ttSlog=null;
//============================================
void insertCrdStopLog(String aCardNo) throws Exception {
   if (ttSlog ==null) {
		ttSlog = new Parm2sql();
		ttSlog.insert("crd_stop_log");
	}

	ttSlog.aaaFunc("proc_seqno",commSqlStr.seqEcsStop,"");
	ttSlog.aaaFunc("crt_time","to_char(sysdate,'yyyymmddhh24miss')","");
	ttSlog.aaa("card_no",aCardNo);
	ttSlog.aaa("current_code", "0");
	ttSlog.aaa("trans_type","09");
	ttSlog.aaa("send_type","2");  //MQUEUE-
	ttSlog.aaaModxxx(hModUser,hModPgm);

   if (ttSlog.ti <=0) {
      ttSlog.ti =ppStmtCrt("tt-slog",ttSlog.getConvSQL());
   }
   sqlExec(ttSlog.ti,ttSlog.getConvParm());
   if (sqlNrow <=0) {
      sqlerr("insert crd_stop_log error[%s]",colSs("card.card_no"));
      errExit();
   }
}
void updateCrdCard(String aPseqno) throws Exception {
   if (empty(aPseqno))
      return;

   if (tiCardU <0) {
      sqlCmd ="update crd_card set"
            +" block_code =?"
            +", block_date ="+commSqlStr.sysYYmd
            +","+this.modxxxSet()
            +" where p_seqno =? and acno_flag<>'Y'"
      +" and current_code ='0'";
      tiCardU =ppStmtCrt("ti-card-U","");
   }
   ppp(1,hCcat.blockReason1+hCcat.blockReason2+
         hCcat.blockReason3+hCcat.blockReason4+hCcat.blockReason5);
   ppp(aPseqno);

   dddSql(tiCardU);
   sqlExec(tiCardU);
   if (sqlNrow <0) {
      sqlerr("update crd_card error, p_seqno[%s]",aPseqno);
      errExit();
   }
}

//=****************************************************************************
void selectCcaCardAcct() throws Exception {
	if (tidCcasAcctS <= 0) {
		sqlCmd = " select "
			+ " block_reason1 , "
			+ " block_reason2 , "
			+ " block_reason3 , "
			+ " block_reason4 , "
			+ " block_reason5 , "
			+ " block_status , spec_status,"
			+ " hex(rowid) as ccas_rowid	"
			+ " from cca_card_acct "
			+ " where acno_p_seqno = ? and acno_flag<>'Y'"
			+ " and debit_flag ='N' ";
		tidCcasAcctS = ppStmtCrt("tid_ccas_acct_S","");
	}
	
	sqlSelect(tidCcasAcctS, new Object[] {
		hAcno.acnoPSeqno
	});
	if (sqlNrow <=0) {
	   colSet("cca_rowid","");
		//errmsg("cca_card_acct.N-find, kk[%s]",h_acno.p_seqno);
		return;
	}
	hCcat.blockReason1 = colSs("block_reason1");
	hCcat.blockReason2 = colSs("block_reason2");
	hCcat.blockReason3 = colSs("block_reason3");
	hCcat.blockReason4 = colSs("block_reason4");
	hCcat.blockReason5 = colSs("block_reason5");
//	h_acno.block_status = col_ss("block_status");
	hCcat.specStatus = colSs("spec_status");
	hCcasRowid = colSs("ccas_rowid");

//	h_acno.block_reason1 ="";
//	h_acno.block_reason2 ="";
//	h_acno.block_reason3 ="";
//	h_acno.block_reason4 ="";
//	h_acno.block_reason5 ="";
//	h_acno.block_status ="";
//	h_ccas_rowid ="";
	
}

//=****************************************************************************
int selectPtrWorkday() throws Exception {
	String lsCloseDate = commDate.dateAdd(hBusiDate, 0, 0, 0 - hBkpm.execCycleNday);
	hWdayStmtCycle = "";
	hWdayThisAcctMonth = "";

	daoTid = "wday.";
	sqlCmd = " select "
		+ " stmt_cycle , "
		+ " this_acct_month "
		+ " from ptr_workday "
		+ " where this_close_date = ? ";
	
	sqlSelect("",new Object[] {
		lsCloseDate
	});
	if (sqlNrow < 0) {
		printf("select ptr_workday error ");
		errExit(0);
	}
	if (sqlNrow>0) {
		hWdayStmtCycle =colSs("wday.stmt_cycle");
		hWdayThisAcctMonth =colSs("wday.this_acct_month");
	}

	return sqlNrow;
}

//=****************************************************************************
void selectRskBlockexec() throws Exception {
	hBkec.rowid = "";
	hBkec.tAcctCnt = 0;
	hBkec.tBlockCnt = 0;
	hBkec.tBlockCnt2 = 0;
	hBkec.tBlockCnt3 = 0;
	hBkec.tBlockCnt6 = 0;
	hBkec.tBlocknot1Cnt = 0;
	hBkec.tBlocknot2Cnt = 0;
	hBkec.tBlocknot3Cnt = 0;

	sqlCmd = " select "
		+ " t_acct_cnt , "
		+ " t_block_cnt , "
		+ " t_block_cnt2 , "
		+ " t_block_cnt3 , "
		+ " t_block_cnt6 , "
		+ " t_blocknot1_cnt , "
		+ " t_blocknot2_cnt , "
		+ " t_blocknot3_cnt , "
		+ " hex(rowid) as rowid "
		+ " from rsk_blockexec "
		+ " where exec_date = ? "
		+ " and acct_type = ? "
		+ " and param_type = ? "
		+ " and valid_date = ? "
		+ " and exec_mode = ? ";

	daoTid ="bkec.";
	sqlSelect("",new Object[] {
		hBusiDate, 
		hBkpm.acctType, 
		hBkpm.paramType, 
		hBkpm.validDate, 
		hBkpm.execMode
	});

	if (sqlNrow < 0) {
		printf("select ptr_blockparam error");
		errExit(0);
	}
	if (sqlNrow ==0) {
		insertRskBlockexec();
		return;
	}
	
   hBkec.tAcctCnt	=colInt("bkec.t_acct_cnt");
   hBkec.tBlockCnt	=colInt("bkec.t_block_cnt");
   hBkec.tBlockCnt2	=colInt("bkec.t_block_cnt2");
   hBkec.tBlockCnt3	=colInt("bkec.t_block_cnt3");
   hBkec.tBlockCnt6	=colInt("bkec.t_block_cnt6");
   hBkec.tBlocknot1Cnt	=colInt("bkec.t_blocknot1_cnt");
   hBkec.tBlocknot2Cnt	=colInt("bkec.t_blocknot2_cnt");
   hBkec.tBlocknot3Cnt	=colInt("bkec.t_blocknot3_cnt");
   hBkec.rowid =colSs("bkec.rowid");
	updateRskBlockexec1(); //-- 該日已執行過 --
}

//=*****************************************************************-**********
void updateRskBlockexec1() throws Exception {
	//-- 該日已執行過之開頭記錄  -
	sqlCmd = " update rsk_blockexec set "
		+ " param_type ='1' , "
		+ " valid_date = ? , "
		+ " exec_date = ? , "
		+ " exec_mode = ? , "
		+ " exec_times = exec_times+1 , "
		+ " exec_date_s = "+ commSqlStr.sysDTime+","
		+ commSqlStr.setModXxx(hModUser, hModPgm)
		+ " where rowid = ? ";

	ppp(1, hBkpm.validDate);
	ppp(2, hBusiDate);
	ppp(3, hBkpm.execMode);
	setRowId(4, hBkec.rowid);
	
	//ddd(sqlCmd,this.get_sqlParm());
	sqlExec(sqlCmd);
	if (sqlNrow <= 0) {
		printf("update rsk_blockexec 1  error");
		this.errExit(0);
	}
}

//=****************************************************************************
private com.Parm2sql ttBlexec=null;
void insertRskBlockexec() throws Exception {
	
	if (ttBlexec ==null) {
      ttBlexec = new com.Parm2sql();
      ttBlexec.insert("rsk_blockexec");
   }
	ttBlexec.aaa("param_type","1");
	ttBlexec.aaa("acct_type",hBkpm.acctType);
	ttBlexec.aaa("valid_date",hBkpm.validDate);
	ttBlexec.aaa("exec_date", hBusiDate);
	ttBlexec.aaa("exec_mode", hBkpm.execMode);
	ttBlexec.aaa("exec_times",1);
	ttBlexec.aaaFunc("exec_date_s",commSqlStr.sysDTime,"");
	ttBlexec.aaa("exec_msg", hWdayStmtCycle);
   ttBlexec.aaaModxxx(hModUser,hModPgm);
   if (ttBlexec.ti <=0) {
		ttBlexec.ti =ppStmtCrt("blockexec-A",ttBlexec.getSql());
	}

	//ddd(t_blexec.sql_from,t_blexec.get_convParm(false));
	sqlExec(ttBlexec.ti, ttBlexec.getConvParm());
	if (sqlNrow <= 0) {
		printf("insert rsk_blockexec2 error");
		errExit(0);
	}
}

//=****************************************************************************
void selectActAcctSum() throws Exception {
	double ldcDebtAmtFee = 0;
	idcDebtAmt = 0;
	idcDebtFee = 0;

	if (tidAcctSum <= 0) {
		sqlCmd = " select "
			+ " sum(nvl(billed_end_bal,0)) as aa_debt_amt_fee , "
			+ " sum(nvl(decode(acct_code,'BL',billed_end_bal,"
			+ "'CA',billed_end_bal,"
			+ "'IT',billed_end_bal,"
			+ "'ID',billed_end_bal,"
			+ "'AO',billed_end_bal,"
			+ "'OT',billed_end_bal,"
			+ "'CB',billed_end_bal,"
			+ "'DB',billed_end_bal,0),0)) as aa_debt_amt "
			+ " from act_acct_sum "
			+ " where p_seqno = ? ";

		tidAcctSum = ppStmtCrt("tid_acct_sum","");
	}
	sqlSelect(tidAcctSum, new Object[] {
		hAcno.pSeqno
	});

	if (sqlNrow < 0) {
		errmsg("select act_acct_sum error, kk[%s]",hAcno.pSeqno);
		errExit(0);
	}

	ldcDebtAmtFee = colNum("aa_debt_amt_fee");
	idcDebtAmt = colNum("aa_debt_amt");

	idcDebtFee = ldcDebtAmtFee - idcDebtAmt;
}

//=****************************************************************************
int selectCrdCard() throws Exception {

	if (tidCardS <= 0) {
		sqlCmd = " select count(*) as aa_count "
			+ " from crd_card where p_seqno = ? "
			+ " and nvl(oppost_date,'') = '' ";
		tidCardS =ppStmtCrt("tid_card_S","");
		errIndex(tidCardS);
	}
	sqlSelect(tidCardS, new Object[] {
		hAcno.pSeqno
	});

	if (sqlNrow <= 0) {
		errmsg("select crd_card error, kk[%s]",hAcno.pSeqno);
		errExit(0);
	}

	return colInt("aa_count");
}

//=****************************************************************************
void selectActDebt() throws Exception {
	int liDCnt=0, liAcaj=0;
	
	if (tidDebtS <= 0) {
		sqlCmd = " select "
			+ " p_seqno , "
			+ " acct_type , "
			+" uf_acno_key(p_seqno) as acct_key ," 
			+ " reference_no , "
			+ " beg_bal , "
			+ " d_avail_bal , "
			+ " end_bal , "
			+ " acct_code , "
			+ " card_no , "
			+ " interest_date , "
			+ " mod_user "
			+ " from act_debt "
			+ " where p_seqno = ? "
			+ " and end_bal > 0 "
			+ " and d_avail_bal > 0 "
			+ " and acct_code in ('LF','AF','CF','PF','RI','PN','SF','CI','CC) ";
		tidDebtS =ppStmtCrt("tid_debt_S","");
	}
	
	daoTid = "debt.";
	sqlSelect(tidDebtS, new Object[] {
		hAcno.pSeqno
	});
	// --for loop
	int llNrow = sqlNrow;

	for (int ii = 0; ii < llNrow; ii++) {
		hDebt.pSeqno = colSs("debt.p_seqno");
		hDebt.acctType = colSs("debt.acct_type");
		hAcno.acctKey = colSs("debt.acct_key");
		hDebt.referenceNo = colSs("debt.reference_no");
		hDebt.begBal = colNum("debt.beg_bal");
		hDebt.dAvailBal = colNum("debt.d_avail_bal");
		hDebt.endBal = colNum("debt.end_bal");
		hDebt.acctCode = colSs("debt.acct_code");
		hDebt.cardNo = colSs("debt.card_no");
		hDebt.interestDate = colSs("debt.interest_date");

		liAcaj = selectActAcaj();

		if (hTempConfirmCnt > 0) {
			deleteActAcaj();
		}
		if (liAcaj > 0) {
			insertActAcajErr();
		}
		else {
			if(printCnt ==0) printHeader();

			if (hDebt.endBal > 0)
				insertActAcaj();

			printDetail();
			if (printCnt >=40 ) {
				printFooter();
				printCnt=0;
			}
			liDCnt = 1; //-- 有D檔處理 --
		}
	}
	if (liDCnt == 1)
		hBkec.tBlockCnt6++;	//-- 無有效卡數凍結戶 --
}

//=****************************************************************************
int selectActAcaj() throws Exception {
	if (tidAcajS <= 0) {
		sqlCmd = " select "
			+ " sum(decode(apr_flag,'Y',0,1)) as aa_confirm_cnt ,"
			+ " sum(decode(apr_flag,'Y',1,0) as aa_count "
			+ " from act_acaj "
			+ " where reference_no = ? "
			+ " and adjust_type like 'DE%' ";
		tidAcajS =ppStmtCrt("tid_acaj_S","");
		errIndex(tidAcajS);
	}
	sqlSelect(tidAcajS, new Object[] {
		hDebt.referenceNo
	});
	if (sqlNrow <= 0) {
		printf("select COUNT(act_acaj) error");
		errExit(1);
	}
	
	hTempConfirmCnt = colInt("aa_confirm_cnt");

	return colInt("aa_count");
}

//=*****************************************************************
void deleteActAcaj() throws Exception {
	if (tidAcajD <= 0) {
		sqlCmd = " delete act_acaj where reference_no = ? "
			+ " and adjust_type like 'DE%' "
			+ " and nvl(apr_flag,'N') <> 'Y' ";
		tidAcajD =ppStmtCrt("tid_acaj_D","");
		errIndex(tidAcajD);
	}
	ppp(1, hDebt.referenceNo);
	sqlExec(tidAcajD);

	if (sqlNrow <= 0) {
		printf("delete act_acaj error");
		this.errExit(1);
	}
}

//=****************************************************************************
void insertActAcajErr() throws Exception {
	if (tidAcajerrA <= 0) {
		sqlCmd = " insert into act_acaj_err ( "
			+ " print_date , "
			+ " p_seqno , "
			+ " acct_type , "
			+ " reference_no , "
			+ " adjust_type , "
			+ " beg_bal , "
			+ " end_bal , "
			+ " d_avail_bal , "
			+ " tx_amt , "
			+ " error_reason "
			+ " ) values ("
			+ commSqlStr.sysYYmd+","
			+ " ? ,"		//1.pseqno
			+ " ? ,"		
			+ " ? ,"		//3.referno
			+ " 'A' ,"	//adj-type
			+ " ? , "	
			+ " ? , "
			+ " ? , "
			+ " 0 , "	//tx_amt
			+ " '03' "	//error
			+ ")";
		tidAcajerrA =ppStmtCrt("tid_acajerr_A","");
	}
	
	ppp(1, hDebt.pSeqno);
	ppp(2, hDebt.acctType);
	ppp(hDebt.referenceNo);
	ppp(hDebt.begBal);
	ppp(hDebt.endBal);
	ppp(hDebt.dAvailBal);

	sqlExec(tidAcajerrA);
	if (sqlNrow <= 0) {
		printf("insert act_acaj_err error");
		this.exitProgram(0);
	}
}
//=****************************************************************************
private com.Parm2sql ttAcaj=null;
void insertActAcaj() throws Exception {
	if (ttAcaj ==null) {
      ttAcaj = new com.Parm2sql();
      ttAcaj.insert("act_acaj");
   }
   ttAcaj.aaa("crt_date",commSqlStr.sysYYmd);
   ttAcaj.aaa("crt_time",commSqlStr.sysTime);
   ttAcaj.aaa("p_seqno", hDebt.pSeqno);
   ttAcaj.aaa("acct_type", hDebt.acctType);
   ttAcaj.aaa("adjust_type","DE18");
   ttAcaj.aaa("reference_no", hDebt.referenceNo);
   ttAcaj.aaa("post_date", hBusiDate);
   ttAcaj.aaa("orginal_amt", hDebt.begBal);
   ttAcaj.aaa("dr_amt", hDebt.endBal);
   ttAcaj.aaa("bef_amt", hDebt.endBal);
   ttAcaj.aaa("aft_amt", 0);
   ttAcaj.aaa("bef_d_amt", hDebt.dAvailBal);
   ttAcaj.aaa("aft_d_amt", hDebt.dAvailBal - hDebt.endBal);
   ttAcaj.aaa("acct_code", hDebt.acctCode);
   ttAcaj.aaa("function_code","U");
   ttAcaj.aaa("card_no", hDebt.cardNo);
   ttAcaj.aaa("value_type", "1");
   ttAcaj.aaa("interest_date", hDebt.interestDate);
   ttAcaj.aaaFunc("update_date",commSqlStr.sysYYmd,"");
   ttAcaj.aaa("apr_flag","Y");
   ttAcaj.aaa("job_code","CL");
   ttAcaj.aaa("vouch_job_code", hTempGlCode);
   ttAcaj.aaaModxxx(hModUser,hModPgm);
   if (ttAcaj.ti <=0) {
      ttAcaj.ti =ppStmtCrt("t_acaj-A", ttAcaj.getSql());
   }

	//ddd(t_acaj.ddd_sql());
	sqlExec(ttAcaj.ti, ttAcaj.getConvParm());
	if (sqlNrow <= 0) {
		printf("insert act_acaj error, ref-no[%s]",hDebt.referenceNo);
		errExit(0);
	}

}

//=****************************************************************************
int checkIsBlock4() {
	int liIsBlock;

	String[] aaBlock2=new String[]{"",hCcat.blockReason2,hCcat.blockReason3
		,hCcat.blockReason4,hCcat.blockReason5};

	liIsBlock = 1;
	for (int ii = 1; ii < 5; ii++) {
		if (strIN(aaBlock2[ii],"05|06|0C|0F|0G|0R")) {
			liIsBlock = 0;
			break;
		}
	}
	return liIsBlock;
}

private com.Parm2sql ttAclg=null;
//======================================================
void insertRskAcnolog(int aiFlag) throws Exception {
	String lsKindFlag = "", lsCardNo = "";	
	if (aiFlag == 5) {
		lsKindFlag = "C";
		lsCardNo = hAclgCardNo;
	}
	else {
		lsKindFlag = "A";
		lsCardNo = "";
	}
	String lsFit="N";
	if (empty(hTempNotblockWhy)) {
		lsFit="Y";
	}
	
	if (ttAclg ==null) {
		ttAclg = new Parm2sql();
		ttAclg.insert("rsk_acnolog");
	}
	
	ttAclg.insert("rsk_acnolog");
	ttAclg.aaa("kind_flag",lsKindFlag);
	ttAclg.aaa("card_no",lsCardNo);
	ttAclg.aaa("acno_p_seqno",hAcno.acnoPSeqno);
	ttAclg.aaa("acct_type",hAcno.acctType);
	ttAclg.aaa("id_p_seqno",hAcno.idPSeqno);
	ttAclg.aaa("corp_p_seqno",hAcno.corpPSeqno);
	ttAclg.aaa("log_date",hBusiDate);
	ttAclg.aaa("log_mode","2");
	ttAclg.aaa("log_type","3");
	ttAclg.aaa("log_reason",hTempBlockWhy);
	ttAclg.aaa("log_not_reason",hTempNotblockWhy);
	ttAclg.aaa("block_reason",hCcat.blockReason1);
	ttAclg.aaa("block_reason2",hCcat.blockReason2);
	ttAclg.aaa("block_reason3",hCcat.blockReason3);
	ttAclg.aaa("block_reason4",hCcat.blockReason4);
	ttAclg.aaa("block_reason5",hCcat.blockReason5);
	ttAclg.aaa("spec_status",hCcat.specStatus);
	ttAclg.aaa("emend_type",hAclg.emendType);
	ttAclg.aaa("fit_cond",lsFit);
	ttAclg.aaaModxxx(hModUser,hModPgm);

	if (ttAclg.ti <=0) {
		ttAclg.ti =ppStmtCrt("t_aclg_I",ttAclg.getConvSQL());
	}			

//	ddd(t_aclg.sql_from,t_aclg.get_convParm(false));
	sqlExec(ttAclg.ti,ttAclg.getConvParm());
	if (sqlNrow <= 0) {
		errmsg("insert rsk_acnolog error, [%s]",hAclg.acnoPSeqno);
		errExit(0);
	}
}

//=****************************************************************************
void updateCcaCardAcct() throws Exception {
	if (tidCcaAcctU <= 0) {
		sqlCmd = " update cca_card_acct set "
			+ " block_status = 'Y' , "
			+ " block_reason1 = ? , "
			+ " block_reason4 = ? , "
			+ " block_date = ? , "
			+ modxxxSet()
			+ " where rowid = ? ";
		tidCcaAcctU =ppStmtCrt("tid_cca_acct_U","");
	}
	
	ppp(1, hCcat.blockReason1);
	ppp(2, hCcat.blockReason4);
	ppp(3, hBusiDate);
	setRowId(4, hCcasRowid);
	sqlExec(tidCcaAcctU);

	if (sqlNrow <= 0) {
		printf("update cca_card_acct error ");
		this.errExit(0);
	}
}

//=****************************************************************************
void updateRskBlockexec2() throws Exception {
	//----記錄 rsk_blockexec 之累計及結尾記錄(目前逐筆)--*/
	if (tidBlexecU2 <= 0) {
		sqlCmd = " update rsk_blockexec set "
			+ " exec_date_e = "+ commSqlStr.sysDTime+", "
			+ " t_acct_cnt = ? , "
			+ " t_block_cnt = ? , "
			+ " t_block_cnt2 = ? , "
			+ " t_block_cnt3 = ? , "
			+ " t_block_cnt6 = ? , "
			+ " t_blocknot1_cnt = ? , "
			+ " t_blocknot2_cnt = ? , "
			+ " t_blocknot3_cnt = ? , "
			+ modxxxSet()
			+ " where exec_date = ? "
			+ " and acct_type = ? "
			+ " and param_type = ? "
			+ " and valid_date = ? "
			+ " and exec_mode = ? ";
		tidBlexecU2 =ppStmtCrt("tid_blexec_U2","");
	}
	ppp(1, hBkec.tAcctCnt);
	ppp(2, hBkec.tBlockCnt);
	ppp(3, hBkec.tBlockCnt2);
	ppp(4, hBkec.tBlockCnt3);
	ppp(5, hBkec.tBlockCnt6);
	ppp(6, hBkec.tBlocknot1Cnt);
	ppp(7, hBkec.tBlocknot2Cnt);
	ppp(8, hBkec.tBlocknot3Cnt);
	//KK--
	ppp(hBusiDate);
	ppp(hBkpm.acctType);
	ppp(hBkpm.paramType);
	ppp(hBkpm.validDate);
	ppp(hBkpm.execMode);

	sqlExec(tidBlexecU2);

	if (sqlNrow <= 0) {
		printf("update rsk_blockexec 2 error");
		errExit(0);
	}
}

//=****************************************************************************
void printHeader() {
	String temp = "" ;
	pageCnt++ ;
	
	buf = "";
	buf = comcr.insertStr(buf, "RSK_P810R1", 1);
	temp = "合 作 金 庫 ";	
	buf = comcr.insertStrCenter(buf, temp, 80);
	if(eq(swPrint,"Y")){
		lpar1.add(comcr.putReport(reportID,reporName, reportDTime,++rptSeq1, "0", buf));
		swPrint = "N";
	}	else	{
		lpar1.add(comcr.putReport(reportID,reporName, reportDTime,++rptSeq1, "0", buf));
	}
	
	buf = "";
	buf = comcr.insertStrCenter(buf, "符合Mg凍結但無有效卡逾期未繳自動D檔報表", 80);
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));
	
	buf = "";
//	temp = String.format("參數生效日: %3ld/%2.2s/%2.2s", commString.ss_2int(commDate.to_twDate(h_bkpm.valid_date))/10000,h_bkpm.valid_date.substring(4),h_bkpm.valid_date.substring(6));
	temp = String.format("參數生效日: %s", commDate.dspDate(hBkpm.validDate));
	buf = comcr.insertStr(buf, temp, 1);
	buf = comcr.insertStr(buf, "列印表日", 62);
	buf = comcr.insertStr(buf, commDate.sysDate(), 71);
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));
	
	buf = "";
//	temp = String.format("執 行 日: %3ld/%2.2s/%2.2s", commString.ss_2int(commDate.to_twDate(h_busi_date))/10000,h_busi_date.substring(4),h_busi_date.substring(6));
	temp = String.format("執 行 日: %s", commDate.dspDate(hBusiDate));
	buf = comcr.insertStr(buf, temp, 1);
	buf = comcr.insertStr(buf, "列印頁數:", 62);
	temp = String.format("%4d", pageCnt);
	buf = comcr.insertStr(buf, temp, 72);
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));
	
	buf = "";
	buf = comcr.insertStr(buf, "帳戶帳號", 1);
	buf = comcr.insertStr(buf, "持卡人", 16);
	buf = comcr.insertStr(buf, "D檔科目", 39);
	buf = comcr.insertStr(buf, "D檔金額", 56);
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));
	
	buf = "";
	buf = "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));	
}

//=*****************************************************************
void printDetail() throws Exception {	 
	String temp = "";
	printCnt++;
	 
	buf = "";
	temp = String.format("%s-%-s", hDebt.acctType,hAcno.acctKey);
	buf = comcr.insertStr(buf, temp, 1);
	if(eq(hAcno.cardIndicator,"1")){
		selectCrdIdno();
	}	else	{
		selectCrdCorp();
	}
	
	buf = comcr.insertStr(buf, hCorpChiName, 16);
	buf = comcr.insertStr(buf, hDebt.acctCode, 42);
	temp =String.format("%,.0f", hDebt.endBal);
	temp =String.format("%13s",temp);
	buf = comcr.insertStr(buf, temp, 50);
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));	
	
	pageCnt++;
	totalCnt++;
	
	pageAmt = pageAmt + hDebt.endBal ;
	totalAmt = totalAmt + hDebt.endBal ;
}

//=*****************************************************************
void printFooter() {
	String temp = "";
	
	buf = "";
	buf = comcr.insertStr(buf, "筆數:", 1);
	temp = String.format("%6d", pageCnt);
	buf = comcr.insertStr(buf, temp, 13);
	temp = String.format("%,.0f", pageAmt);
	temp =String.format("%14s",temp);
	buf = comcr.insertStr(buf, temp, 49);
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));
	
	pageCnt = 0 ;
	pageAmt = 0 ;
}

//=*****************************************************************
void printTotal() {
	String temp = "";
	
	buf = "" ; 
	buf = comcr.insertStr(buf, "總筆數:", 1);
	temp = String.format("%6d", totalCnt);
	buf = comcr.insertStr(buf, temp, 13);
	temp = String.format("%,.0f", totalAmt);
	temp = String.format("%14s", temp);
	buf = comcr.insertStr(buf, temp, 49);
	lpar1.add(comcr.putReport(reportID, reporName, reportDTime,++rptSeq1, "0", buf));
}
//=*****************************************************************
void selectCrdIdno() throws Exception {
   hCorpChiName = "";
   
   if(tidIdnoS<=0){
   	sqlCmd = " select chi_name " 
   			 + " from crd_idno "
   			 + " where id_p_seqno = ? "   			 
   			 ;
   	tidIdnoS =ppStmtCrt("tid_idno_S","");
   }
   
   ppp(1,hAcno.idPSeqno);
   sqlSelect(tidIdnoS);
   
   if (sqlNrow <0){
      printf("select crd_idno error, kk[%s]",hAcno.idPSeqno);
      errExit(0);
   }
   if (sqlNrow >0) {
      hCorpChiName = commString.hiIdnoName(colSs("chi_name"));
   }
   
}
// ******************************************************************
void selectCrdCorp() throws Exception{
   hCorpChiName = "";
   
   if(tidCorpS <=0){
   	sqlCmd = " select chi_name "
   			 + " from crd_corp "
   			 + " where corp_p_seqno = ? "
   			 ;
   	tidCorpS = this.ppStmtCrt("tid_corp_S","");
   }
   
   sqlSelect(tidCorpS,new Object[]{hAcno.corpPSeqno});
   
   if(sqlNrow<=0){
   	printf("select crd_corp error, kk[%s]",hAcno.corpPSeqno);
   	errExit(0);
   }
   if (sqlNrow >0) {
      hCorpChiName = colSs("chi_name");
   }
}

//void err_index(int ai_indx) {
//	if (ai_indx >0)
//		return;
//	
//	errmsg("canot create PS-index: "+daoTable);
//	err_exit(0);
//}
//void err_index() {
//	errmsg("canot create PS-index: "+daoTable);
//	err_exit(0);
//}

}
