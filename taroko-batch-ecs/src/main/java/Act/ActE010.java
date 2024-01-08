/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/10/12  V1.00.01  Jack Liao   ActE010 initial                           *
* 110/03/22  V1.00.40  Simon       last modify                               *
* 111/04/12  V1.01.02  Allen Ho    Mantis 9334                               *
* 111-10-14  V1.00.03  Machao      sync from mega & updated for project coding standard *
* 112-05-29  V1.00.04  Simon       add cyc_pyaj.fund_code                    *
* 112-06-07  V1.00.05  Simon       1.remove garbage selectPtrActgeneral1() meanwhile there is no ptr_actgeneral_n_201509*
*                                  2.remove garbage selectPtrActgeneral() because processMktCashbackDtl() has selected ptr_actgeneral_n*
* 112-09-27  V1.00.06  Simon      1.remove processing acno.no_cancel_debt_flag="Y"*
*                                 2.取消更新 act_acno.last_pay_amt、last_pay_date *
*                                 3.更新以 busiDate 寫入 cyc_pyaj.crt_date，而不以 sysDate寫入*
* 112-10-13  V1.00.07  Simon      1.取消專案基金有效卡幾個月內開卡抵用條件判斷*
*                                 2.取消專案基金、Combo卡基金抵用比率條件判斷 *
* 112-11-15  V1.00.08  Simon      loadActDebt() 修改只取當期或欠款大於0      *
* 112-11-23  V1.00.09  Simon      分批執行控制                               *
******************************************************************************/
package Act;

import com.*;
import java.sql.*;
import java.util.*;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

@SuppressWarnings("unchecked")
public class ActE010 extends AccessDAO
{

 String   debugFlag = "N";
 String   testPSeqno ="0000060291";

 private final  String PROGNAME = "卡人台幣基金銷帳處理程式  112-11-23  V1.00.09";

 CommFunction   comm  = new CommFunction();
 CommCrd        comc  = new CommCrd();
 CommRoutine    comr  = null;
 CommCrdRoutine comcr = null;

 String   pSeqno   = "";
 int      srtLength   = 0;
 int      di=0;
 boolean  passCheck= true,comboCard = false,fundMatch=false;
//String[] sortField_1 = {"debt.sort_bal,DESC","debt.sort_order_normal","debt.sort_post_date","debt.sort_class_normal"};
//String[] sortField_1 = {"debt.decode(sort_bal,0,0,1),DESC","debt.sort_order_normal","debt.sort_post_date","debt.sort_class_normal"};
 String[] sortField1 = {"debt.sort_bal_decode,DESC","debt.sort_order_normal","debt.sort_post_date","debt.sort_class_normal"};
 SortObject   srt  = new SortObject(this);

 double[] tempRevolvingInterest = {0,0,0,0,0,0,0,0,0,0};

 String   debtKey="debt.p_seqno";
 String   acnoKey="acno.p_seqno";
 String   acctKey="acct.p_seqno";
 String   acurKey="acur.p_seqno,acur.curr_code";
 String   pcglKey="pcgl.curr_code";
 String   wdayKey="wday.stmt_cycle";
 String   pcodKey="pcod.acct_code";
 String   mercKey="merc.mcht_no";
 String   mparKey="mpar.fund_code";
 String   fundKey="fund.fund_code";
 String   cofpKey="cofp.fund_code";
 String   vmktKey="vmkt.fund_code";
 String   nfcKey="nfc.fund_code";
 String   dataKey="data.data_key,data.data_type";
 String   mktgKey="mktg.data_key";
 String   mktpKey="mktp.data_key";
 String   mktpKey2="mktp2.data_key";
//String   crdKey="crd.gp_no";
 String   crdKey="crd.p_seqno";
//String   crdKey2="crd.gp_no,crd.current_code";
 String   crdKey2="crd.p_seqno,crd.current_code";
 String   typeKey="type.card_type";
 String   bilKey="bil.reference_no";

 int      tttTest=0;
 int      debtLoadCnt=0;
 long     wsTempAmt=0;
 long     hIntrEnqSeqno=0,jrnlOrderSeq=0,enqNo=0,tempLong=0;
 double   wsInterestAmt=0,wsInterestAmtTot=0,wsRealOvAmt=0;
 double   wsBefMpBal=0,cancelRate=0,totRealCancelAmt=0;
 double   remainCancelAmt=0,realCancelAmt=0,debtEndBalance=0,rateBalance=0,cbfRateBalance=0;
 double   cbfTotRealCancelAmt=0,cbfRemainCancelAmt=0,debtCanByFundBal=0,cbfRealCancelAmt=0;
 double   cbfTurnOpAmt=0, hCycVouchAmt=0;
 String   stmtCycle="",thisAcctMonth="",jrnlJrnlSeqno="",hCycVouchDataType="";

 double   wsInterestRate=0,fitRevolvingRate1=0,inCreditAmt=0;
 double   nonRealPayAmt=0, wsTempUnbillInterest=0;
 String   inBegDate="",outEndDate="",busiDate="";
//String   hBusiBusinessDate = "";
 int      hBatchSeq = 0;
 String   hProcFlag="",hPtclRowid="",hBeginKey="",hStopKey="";
 String   wsInProcFlag = "",hProcReset="";
 String   hProcSDate="",hProcSTime="",hProcEDate="",hProcETime="";
 boolean  hAppActive=false;

 HashMap  groupHash = new HashMap();

 private  HashMap<String,String>  checkHash  = new  HashMap<String,String>();

// ************************************************************************
 public static void main(String[] args) throws Exception
 {
    ActE010 proc = new ActE010();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
    return;
 }

// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
    dateTime();
    setConsoleMode("N");
    javaProgram = this.getClass().getName();
    showLogMessage("I","",javaProgram+" "+PROGNAME);

    if (comm.isAppActive(javaProgram)) 
    {
      hAppActive=true;
    }

    if ( !connectDataBase() ) return 1;

    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine   (getDBconnect(), getDBalias());

    if (args.length > 3)
       {
        showLogMessage("I","","請輸入參數:");
        showLogMessage("I","","PARM 1 : [BUSINESS_DATE]/[BATCH_SEQ]/[P_SEQNO]");
        showLogMessage("I","","PARM 2 : [BATCH_SEQ]/[P_SEQNO]/['reset']");
        showLogMessage("I","","PARM 3 : ['reset']");
        return(0);
       }

    if ( args.length == 1 ) {
    	if (args[0].length()==8) {
        busiDate = args[0]; 
    	} else if (args[0].length()>0 && args[0].length()<=2) {
        hBatchSeq = comcr.str2int(args[0]);
    	} else if (args[0].length()==10) {
        pSeqno  = args[0];
      }
    } else if ( args.length == 2 ) {
    	if (args[0].length()==8) {
        busiDate = args[0]; 
    	} else if (args[0].length()>0 && args[0].length()<=2) {
        hBatchSeq = comcr.str2int(args[0]);
    	} else if (args[0].length()==10) {
        pSeqno  = args[0];
      }

    	if (hBatchSeq==0 && args[1].length()>0 && args[1].length()<=2) {
        hBatchSeq = comcr.str2int(args[1]);
    	} else if (args[1].length()==10) {
        pSeqno  = args[1];
    	} else if (args[1].equalsIgnoreCase("reset")) {
        hProcReset  = "Y";
      }
    } else if ( args.length == 3 ) {
    	if (args[0].length()==8) {
        busiDate = args[0]; 
      }

    	if (hBatchSeq==0 && args[1].length()>0 && args[1].length()<=2) {
        hBatchSeq = comcr.str2int(args[1]);
      }

    	if (args[2].equalsIgnoreCase("reset")) {
        hProcReset  = "Y";
      } else {
        showLogMessage("I","","請輸入參數:");
        showLogMessage("I","","PARM 1 : [BUSINESS_DATE]");
        showLogMessage("I","","PARM 2 : [BATCH_SEQ]");
        showLogMessage("I","","PARM 3 : ['reset']");
        return 0;
      }
    }
      
    selectPtrBysinday();

 	  if (hProcReset.equals("Y")) {
      if (selectEcsBatchSegment()>0 && hAppActive==false) {
        wsInProcFlag = "N";
        hProcSDate = "";
        hProcSTime = "";
        hProcEDate = "";
        hProcETime = "";
        showLogMessage("I", "", "=== program_id, business_date, batch_seq => "+
        javaProgram+", "+busiDate+", "+hBatchSeq+" ===");
        showLogMessage("I", "", "=== in_proc_flag, begin_key, stop_key => "+
        hProcFlag+", "+hBeginKey+", "+hStopKey+" ===");
    	  updateEcsBatchSegment();
        showLogMessage("I", "", "=== 此程式分批 reset ok！ ===");
      } else {
        showLogMessage("I", "", "=== 此程式分批 reset 無效！ ===");
      }
      finalProcess();
      return 0; 
 	  }

    if ( loadPtrWorkday() == 0 )
       { finalProcess(); return 0; }

    int cnt1 = selectEcsBatchSegment();
    if ( cnt1 > 0 ) {
    	if (hProcFlag.equals("Y")) {
        showLogMessage("I", "", "=== program_id, business_date, batch_seq => "+
        javaProgram+", "+busiDate+", "+hBatchSeq+" ===");
        showLogMessage("I", "", "=== in_proc_flag, begin_key, stop_key => "+
        hProcFlag+", "+hBeginKey+", "+hStopKey+" ===");
        showLogMessage("I", "", "=== 此程式分批已執行過，本程式結束！ ===");
        return(0);
    	} else if (hProcFlag.equals("R")) {
        showLogMessage("I", "", "=== program_id, business_date, batch_seq => "+
        javaProgram+", "+busiDate+", "+hBatchSeq+" ===");
        showLogMessage("I", "", "=== in_proc_flag, begin_key, stop_key => "+
        hProcFlag+", "+hBeginKey+", "+hStopKey+" ===");
        showLogMessage("I", "", "=== 此程式分批已在其他程序進行中，本程式結束！ ===");
        return(0);
    	} else {
        showLogMessage("I", "", "=== program_id, business_date, batch_seq => "+
        javaProgram+", "+busiDate+", "+hBatchSeq+" ===");
        showLogMessage("I", "", "=== in_proc_flag, begin_key, stop_key => "+
        hProcFlag+", "+hBeginKey+", "+hStopKey+" ===");
        wsInProcFlag = "R";
        hProcSDate = sysDate;
        hProcSTime = sysTime;
    	  updateEcsBatchSegment();
        commitDataBase();
      }
    } else {
    	if (hBatchSeq > 0) {
        showLogMessage("I", "", "=== program_id, business_date, batch_seq => "+
        javaProgram+", "+busiDate+", "+hBatchSeq+" ===");
        showLogMessage("I", "", "=== 此程式分批未分配，本程式結束！ ===");
        return(0);
    	} else {
      	hStopKey = "9999999999";
        hProcFlag = "N";
        showLogMessage("I", "", "=== 本程式整批執行： begin_key, stop_key => "+
        hBeginKey+", "+hStopKey+" ===");
      }
    }
    
    processSkipHash(); // jack 20200818

  //selectPtrActgeneral1();
    selectPtrCurrGeneral();

    loadVmktFundName();

    loadPtrFundp();
    loadPtrComboFundp();
    loadMktLoanParm();
    loadMktNfcParm();

    loadPtrFundData();
    loadMktParmData();
    loadMktParmData2();
    loadMktMchtgpData();

    loadActAcno();
    loadActAcct();
    loadActAcctCurr();

    loadPtrCardType();
    loadCrdCard();
    loadPtrActcode();
    loadBilMerchant();
    loadActDebt();

    // 處理銷帳及計息作業
    processMktCashbackDtl();

    if ( hBatchSeq > 0 ) {
      wsInProcFlag = "Y";
      dateTime();
      hProcEDate = sysDate;
      hProcETime = sysTime;
  	  updateEcsBatchSegment();
    } 

    if (pSeqno.length()==0) finalProcess();
    return 0;
  }

  catch ( Exception ex )
  {  expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess

  //***********************************************************************/
  int selectEcsBatchSegment() throws Exception {

    sqlCmd  = "select in_proc_flag, rowid as rowid, ";
    sqlCmd += " begin_key, stop_key  ";
    sqlCmd += "  from ecs_batch_segment  ";
    sqlCmd += " where program_id = ?  ";
    sqlCmd += "   and business_date = ? ";
    sqlCmd += "   and batch_seq = ? ";
    setString(1, javaProgram);
    setString(2, busiDate);
    setInt(3, hBatchSeq);
    int recordCnt = selectTable();
    if (!notFound.equals("Y")) {
      hProcFlag = getValue("in_proc_flag");
      hPtclRowid = getValue("rowid");
      hBeginKey = getValue("begin_key");
      hStopKey = getValue("stop_key");
    }

    return recordCnt;
  }

  /***********************************************************************/
  void updateEcsBatchSegment() throws Exception {

    daoTable   = "ecs_batch_segment";
    updateSQL  = " in_proc_flag  = ?,";
    updateSQL += " s_proc_date   = ?,";
    updateSQL += " s_proc_time   = ?,";
    updateSQL += " e_proc_date   = ?,";
    updateSQL += " e_proc_time   = ?,";
    updateSQL += " mod_time      = sysdate,";
    updateSQL += " mod_pgm       = ?,";
    updateSQL += " mod_seqno     = mod_seqno + 1 ";
    whereStr = " where rowid  = ? ";
    setString(1, wsInProcFlag);
    setString(2, hProcSDate);
    setString(3, hProcSTime);
    setString(4, hProcEDate);
    setString(5, hProcETime);
    setString(6, javaProgram);
    setRowId(7, hPtclRowid);
    updateTable();
    if (notFound.equals("Y")) {
    //comcr.errRtn("update_ecs_batch_segment not found!", "", hCallBatchSeqno);
      showLogMessage("E","","*** update_ecs_batch_segment not found! => "+
      "program_id, business_date, batch_seq : "+
      javaProgram+", "+busiDate+", "+hBatchSeq+" ***");
    }

  }

 // ******************* 處理基金銷帳 START ************************************

 public void  processMktCashbackDtl() throws Exception
 {
   long   numLong=0;

   daoTable    = "mkt_cashback_dtl a,ptr_actgeneral_n b";
   fetchExtend = "cash.";
   selectSQL   = "a.p_seqno,"
               + "MIN(decode(a.effect_e_date,'','99999999',a.effect_e_date)) as fund_effect_e_date,"
               + "a.fund_code,"
               + "sum(a.end_tran_amt) as end_tran_amt,"
               + "max(b.revolving_interest1) as revolving_interest1,"
               + "max(b.revolving_interest2) as revolving_interest2,"
               + "max(b.revolving_interest3) as revolving_interest3,"
               + "max(b.revolving_interest4) as revolving_interest4,"
               + "max(b.revolving_interest5) as revolving_interest5,"
               + "max(b.revolving_interest6) as revolving_interest6,"
               + "max(b.rc_max_rate) as rc_max_rate";
   whereStr    = "WHERE a.p_seqno in ( select P_SEQNO from act_acno where stmt_cycle = ? ) "
               + "and   a.acct_type = b.acct_type "  
               + "and   a.p_seqno >= ? and a.p_seqno < ? ";
   setString(1,stmtCycle);
   setString(2,hBeginKey);
   setString(3,hStopKey);
   if (pSeqno.length()!=0)
      {
       whereStr  = whereStr    
                 + "and a.p_seqno = ?  ";
       setString(4 , pSeqno);
      }
   whereStr    = whereStr    
               + "group by a.P_SEQNO,a.FUND_CODE having sum(end_tran_amt) > 0 "
               + "order by a.P_SEQNO,MIN(decode(a.effect_e_date,'','99999999',a.effect_e_date)),a.FUND_CODE ";

   openCursor();
   while( fetchTable() )  {

        //if ( !getValue("cash.p_seqno").equals(test_p_seqno) && debugFlag.equals("Y"))  // ttt
        //   { continue; }
        tempRevolvingInterest[1] = getValueDouble("cash.revolving_interest1");
        tempRevolvingInterest[2] = getValueDouble("cash.revolving_interest2");
        tempRevolvingInterest[3] = getValueDouble("cash.revolving_interest3");
        tempRevolvingInterest[4] = getValueDouble("cash.revolving_interest4");
        tempRevolvingInterest[5] = getValueDouble("cash.revolving_interest5");
        tempRevolvingInterest[6] = getValueDouble("cash.revolving_interest6");

        totRealCancelAmt = 0;
        cbfTotRealCancelAmt = 0;

        setValue("wday.stmt_cycle",stmtCycle);
        getLoadData(wdayKey);  // 讀 ptr_workday

        setValue("acno.p_seqno",getValue("cash.p_seqno"));
        int n = getLoadData(acnoKey);  // 讀 act_acno

        if ( !getValue("acno.stmt_cycle").equals(stmtCycle) || n == 0 )
           { continue; }

        setValue("acct.p_seqno",getValue("cash.p_seqno"));
        getLoadData(acctKey); // 讀 act_acct

        setValue("acur.p_seqno",getValue("cash.p_seqno"));
        setValue("acur.curr_code","901");
        getLoadData(acurKey); // 讀 act_acct_curr
/***
        if ( (getValue("acno.no_cancel_debt_flag").equals("Y") &&
              getValue("busi.business_date").compareTo(getValue("acno.no_cancel_debt_s_date")) >= 0  &&
              getValue("busi.business_date").compareTo(getValue("acno.no_cancel_debt_e_date")) <= 0) &&
              getValue("acno.acct_status").equals("3") ) // bad debt account
           { logs("ttt skip bad debt account"); continue; }
***/
        wsInterestAmt =0;     wsInterestAmtTot = 0;
        if ( enqNo > 99900 )
           { enqNo = 0; }

        jrnlJrnlSeqno = String.format("%012.0f", GetJRNLSeq());
        setValue("jrnl.jrnl_seqno",jrnlJrnlSeqno);
        jrnlOrderSeq = 1;

        wsBefMpBal = getValueDouble("acur.min_pay_bal");

        remainCancelAmt      =  getValueDouble("cash.end_tran_amt");
        cbfRemainCancelAmt   =  remainCancelAmt;
        nonRealPayAmt       =  0;
        dDebtProcessRtn();
        if ( !fundMatch )  // 基金抵用查核未通過
           //{ break; }
           { continue; }

      //setValueDouble("acct.temp_unbill_interest", getValueDouble("acct.temp_unbill_interest") + ws_interest_amt_tot);
      //setValueDouble("acur.temp_unbill_interest", getValueDouble("acur.temp_unbill_interest") + ws_interest_amt_tot);

        wsTempUnbillInterest  =  getValueDouble("acct.temp_unbill_interest") + wsInterestAmtTot;
        numLong = (long)((wsTempUnbillInterest * 100.0) + 0.5);
        wsTempUnbillInterest = (double)numLong / 100.0;
        wsTempUnbillInterest = convAmt(wsTempUnbillInterest);
        setValueDouble("acct.temp_unbill_interest", wsTempUnbillInterest);

        wsTempUnbillInterest  =  getValueDouble("acur.temp_unbill_interest") + wsInterestAmtTot;
        numLong = (long)((wsTempUnbillInterest * 100.0) + 0.5);
        wsTempUnbillInterest = (double)numLong / 100.0;
        wsTempUnbillInterest = convAmt(wsTempUnbillInterest);
        setValueDouble("acur.temp_unbill_interest", wsTempUnbillInterest);

      //setValue("acct.acct_jrnl_bal",""+(getValueDouble("acct.acct_jrnl_bal") - tot_real_cancel_amt));
      //setValue("acur.acct_jrnl_bal",""+(getValueDouble("acur.acct_jrnl_bal") - tot_real_cancel_amt));
        setValue("acct.acct_jrnl_bal",""+(getValueDouble("acct.acct_jrnl_bal") - cbfTotRealCancelAmt)); //modified on 2019/01/25
        setValue("acur.acct_jrnl_bal",""+(getValueDouble("acur.acct_jrnl_bal") - cbfTotRealCancelAmt)); //modified on 2019/01/25
      //setValue("acur.ttl_amt_bal",""+(getValueDouble("acur.ttl_amt_bal") - tot_real_cancel_amt ));
      //setValue("acct.ttl_amt_bal",""+(getValueDouble("acct.ttl_amt_bal") - tot_real_cancel_amt ));
        setValue("acur.ttl_amt_bal",""+(getValueDouble("acur.ttl_amt_bal") - totRealCancelAmt + nonRealPayAmt ));
        setValue("acct.ttl_amt_bal",""+(getValueDouble("acct.ttl_amt_bal") - totRealCancelAmt + nonRealPayAmt ));

        // 更新 溢繳款
        double wsOpAddAmt = cbfTotRealCancelAmt - totRealCancelAmt;
        setValue("acct.end_bal_op",""+(getValueDouble("acct.end_bal_op") + wsOpAddAmt));
        setValue("acur.end_bal_op",""+(getValueDouble("acur.end_bal_op") + wsOpAddAmt));

        if ( getValueDouble("acur.ttl_amt_bal") <= getValueDouble("pcgl.total_bal") ) {
             if ( busiDate.compareTo(getValue("wday.this_delaypay_date")) <= 0 )
                { setValue("acur.delaypay_ok_flag", "Y"); }
             else
                {
                  if ( getValueInt("acno.int_rate_mcode") == 0 && busiDate.compareTo(getValue("wday.next_close_date")) <= 0 &&
                       checkCrdCard() != 0 )
                     { updateActAcctCurr1();  }
                }
          }

       /***
        以下移到 d_debt_process_rtn() 處理
        if ( getValueDouble("acur.min_pay_bal") - tot_real_cancel_amt  > 0 )
           { temp_long = (long)(tot_real_cancel_amt); }
        else
           { temp_long = getValueLong("acur.min_pay_bal"); }

        if ( !getValue("debt.acct_month").equals(getValue("wday.next_acct_month")) ) {
             setValueDouble("acur.min_pay_bal",getValueDouble("acur.min_pay_bal") - temp_long);
             setValueDouble("acct.min_pay_bal",getValueDouble("acct.min_pay_bal") - temp_long);
           }
       ***/

        setValue("acct.rc_min_pay_bal",""+(getValueDouble("acct.rc_min_pay_bal") - totRealCancelAmt));
        if ( getValueDouble("acct.rc_min_pay_bal") < 0 )
           { setValue("acct.rc_min_pay_bal","0"); }

        if ( getValueDouble("acct.rc_min_pay_bal") < getValueDouble("acct.rc_min_pay_m0") )
           { setValue("acct.rc_min_pay_m0",getValue("acct.rc_min_pay_bal")); }

        if ( getValueDouble("acct.min_pay_bal") == 0 && getValueDouble("acct.min_pay") > 0 ) {
             if ( getValue("acct.last_min_pay_date").length() == 0 || busiDate.compareTo(getValue("acct.last_min_pay_date")) < 0 )
                { setValue("acct.last_min_pay_date",busiDate); }
           }

        if ( getValueDouble("acct.ttl_amt_bal") <= 0 && getValueDouble("acct.ttl_amt") > 0 ) {
             if ( getValue("acct.last_cancel_debt_date").length() == 0 || busiDate.compareTo(getValue("acct.last_cancel_debt_date")) < 0 )
                { setValue("acct.last_cancel_debt_date",busiDate); }
           }

        cancelActAcagCurrRtn(wsBefMpBal - getValueDouble("acur.min_pay_bal"));
        deleteActAcag();

        setValue("jrnl.tran_type", getValue("cash.fund_code").substring(0,4));
        //setValue("jrnl.transaction_amt",""+tot_real_cancel_amt);
        setValue("jrnl.transaction_amt",""+cbfTotRealCancelAmt);

        if ( getValueDouble("jrnl.transaction_amt") > 0 ) {
             insertActJrnl1();
             insertCycPyaj();
           }

      //updateActAcno();
        updateActAcct();
        updateActAcctCurr();
   }

   closeCursor();
   return;
 }

// ******************* 處理基金銷帳 ENDED ************************************

 //  查核一般基金
 public void processPtrFundp() throws Exception
 {
   logs("ttt check process_ptr_fundp");
   //cancelRate = getValueDouble("mpar.CANCEL_RATE");

   setValue("fund.fund_code",getValue("cash.fund_code"));
   getLoadData(fundKey);

   int    period = getValueInt("fund.cancel_period");
   int    sMonth = getValueInt("fund.cancel_s_month");
   checkCancelPeriod(period,sMonth,getValue("fund.FUND_CRT_DATE_S")); // 查核抵用期間, 抵用起始月份
   if ( !passCheck )
      { return; }

   int scope = getValueInt("fund.cancel_scope");
   checkCancelScope1(scope);      // 查核抵用範圍
   if ( !passCheck )
      { return; }

   checkPtrFundData("8",getValue("debt.mcht_category",di),getValue("debt.acq_no",di));  // 查核特店類別
   checkPtrFundData("6",getValue("debt.mcht_no",di),getValue("debt.acq_no",di));        // 查核特店代號
   checkPtrFundData("C",getValue("debt.mcht_no",di),getValue("debt.acq_no",di));        // 查核特店群組
   checkPtrFundData("F",getValue("debt.ucaf",di),getValue("debt.acq_no",di));           // 查核 UCAF
   checkPtrFundData("G",getValue("debt.ec_ind",di),getValue("debt.acq_no",di));         // 查核 ECI
   checkPtrFundData("E",getValue("debt.pos_entry_mode",di),getValue("debt.acq_no",di)); // 查核 POS ENTRY
   if ( !passCheck )
      { return; }

   checkCancelEvent1(); // 查核 抵用條件
   if ( !passCheck )
      { return; }

   if ( getValueInt("acno.INT_RATE_MCODE") >= getValueInt("fund.MIN_MCODE") )
      { logs("ttt skip INT_RATE_MCODE "); passCheck = false; }

   return;
 }

 //  查核 COMBO 卡回饋基金
 public void processPtrComboFundp() throws Exception
 {
   logs("ttt check process_ptr_combo_fundp");
   comboCard = true;

   setValue("cofp.fund_code",getValue("cash.fund_code"));
   getLoadData(cofpKey);
 //cancelRate = getValueDouble("cofp.cancel_unbill_rate");

   int     period = getValueInt("cofp.cancel_period");
   int     sMonth = getValueInt("cofp.cancel_s_month");
   checkCancelPeriod(period,sMonth,getValue("cofp.apr_date"));  // 查核抵用期間, 抵用起始月份
   if ( !passCheck )
      { return; }

   int scope = getValueInt("cofp.cancel_unbill_type");
   if ( scope == 2  )
      { scope = 3;  } // 全部(當期及前期)簽帳款

   checkCancelScope1(scope);  // 查核抵用範圍
   if ( !passCheck )
      { return; }

   int eventCode = getValueInt("cofp.cancel_event");
   if ( eventCode == 1 )
      { return; }

   // 查核 有 COMBO 卡有效卡
 //setValue("crd.gp_no",getValue("cash.p_seqno"));
   setValue("crd.p_seqno",getValue("cash.p_seqno"));
   setValue("crd.current_code","0");
   int n = getLoadData(crdKey2);  // 讀 crd_card
   String checkFlag = "N";
   for(int i=0; i<n; i++ ) {
       if ( getValue("crd.combo_indicator",i).equals("N") )
          { continue; }
       checkFlag = "Y";
     }

   if ( checkFlag.equals("N") )
      { logs("ttt skip 111 "); passCheck = false; }

   return;
 }

 //  查核專案基金 mkt_loan_parm
 public void processMktLoanParm() throws Exception {

   logs("ttt process_mkt_loan_parm() ");

   setValue("mpar.fund_code",getValue("cash.fund_code"));
   getLoadData(mparKey);
 //cancelRate = getValueDouble("mpar.CANCEL_RATE");

 //setValue("crd.gp_no",getValue("cash.p_seqno"));
   setValue("crd.p_seqno",getValue("cash.p_seqno"));
   int n = getLoadData(crdKey);  // 讀 crd_card
   checkIssueDate(n); // 查核新卡
   if ( !passCheck )
      {  logs("ttt aaa-1 ");   return; }

   if ( getValueInt("acno.INT_RATE_MCODE") > getValueInt("mpar.MCODE") ) // 查核 M_CODE
      { logs("ttt 222 "); passCheck = false; }

   checkMktParmData("3",getValue("debt.mcht_no",di),getValue("debt.acq_no",di));  // 查核特店代號
   checkMktParmData("4",getValue("debt.mcht_no",di),getValue("debt.acq_no",di));  // 查核特店群組
   if ( !passCheck )
      { return; }

   checkCancelScope2(); // 查核抵用範圍
   if ( !passCheck )
      { return; }
   checkCancelEvent2(); // 查核 抵用條件

   return;
 }

 //查核 mkt_parm_data
 public void checkMktParmData(String dataCode,String checkData,String acquire) throws Exception
 {
   String selCode = "";
   switch (dataCode) {
            case "3"  : selCode = getValue("mpar.MERCHANT_SEL");   break;
            case "4"  : selCode = getValue("mpar.MCHT_GROUP_SEL"); break;
            default   : return;
         }

   if ( selCode.equals("0") )  // (全部) 不查核
      { return; }

   // 特店群組(含多店) 之特店代號檢核 STARTED

   setValue("mktp2.data_key",getValue("cash.fund_code"));
   setValue("mktp2.data_type",dataCode);
   int n = getLoadData(mktpKey2);

   if ( dataCode.equals("4") && selCode.equals("1") ) //查核指定條件
      {
        String checkFlag = "N";
        for ( int i=0; i<n; i++ )
        {
              setValue("mktg.data_key",getValue("mktp2.data_code",i));
              int m = getLoadData(mktgKey);
              for ( int j=0; j<m; j++ )
                  {
                   if ( checkData.equals(getValue("mktg.data_code",j)) )
                      {
                        if ( getValue("mktg.data_code2",j).length() == 0 )
                           { checkFlag = "Y"; break;}
                        else
                        if ( getValue("mktg.data_code2",j).equals(acquire) ) // 若 acquire 有值也要比
                           { checkFlag = "Y"; break;}
                      }
                  }
              if ( checkFlag.equals("Y") )
                 { break; }
        }
        if ( checkFlag.equals("N") )
           { logs("ttt AAA-1-m "); passCheck = false; }

        return;
      }

   if ( dataCode.equals("4") && selCode.equals("2") ) //查核排除條件
      {
        for ( int i=0; i<n; i++ )
        {
              setValue("mktg.data_key",getValue("mktp2.data_code",i));
              int m = getLoadData(mktgKey);
              for ( int j=0; j<m; j++ )
                  {
                   if ( checkData.equals(getValue("mktg.data_code",j)) )
                      {
                        if ( getValue("mktg.data_code2",j).length() == 0 )
                           { passCheck = false; break;}
                        else
                        if ( getValue("mktg.data_code2",j).equals(acquire) ) // 若 acquire 有值也要比
                           { passCheck = false; break;}
                      }
                  }
              if ( !passCheck )
                 { logs("ttt BBB-1-m "); break; }
        }

        return;
      }

   // 特店群組(含多店) 之特店代號檢核 ENDED

   //查核特店代號指定條件
   if ( dataCode.equals("3") && selCode.equals("1") )
      {
        String checkFlag = "N";
        for( int i=0; i < n; i++ )
        {
           if ( checkData.equals(getValue("mktp2.data_code",i)) )
              {
                if ( getValue("mktp2.data_code2",i).length() == 0 )
                   { checkFlag = "Y"; break;}
                else
                if ( getValue("mktp2.data_code2",i).equals(acquire) ) // 若 acquire 有值也要比
                   { checkFlag = "Y"; break;}
              }
        }
        if ( checkFlag.equals("N") )
           { logs("ttt 666-1-m ");  passCheck = false; }

        return;
      }

   // 查核特店代號排除條件
   if ( dataCode.equals("3") && selCode.equals("2") )
      {
        for( int i=0; i < n; i++ )
        {
           if ( checkData.equals(getValue("mktp2.data_code",i)) )
              {
                if ( getValue("mktp2.data_code2",i).length() == 0 )
                   { logs("ttt 777-m "); passCheck = false; break;}
                else
                if ( getValue("mktp2.data_code2",i).equals(acquire) ) // 若 acquire 有值也要比
                   { logs("ttt 888-m "); passCheck = false; break;}
              }
        }

        return;
      }

   return;
 }


 //  查核指定繳款方式基金
 public void processMktNfcParm() throws Exception
 {
   logs("ttt process_mkt_nfc_parm() ");

   setValue("nfc.fund_code",getValue("cash.fund_code"));
   getLoadData(nfcKey);

   int period = getValueInt("nfc.cancel_period");
   int sMonth = Integer.parseInt(getValue("busi.business_date").substring(4,6));

   checkCancelPeriod(period,sMonth,getValue("nfc.FUND_CRT_DATE_S")); // 查核抵用期間, 抵用起始月份
   int scope = getValueInt("nfc.cancel_scope");
   if ( scope == 2 )
      { scope = 3;  } // 全部(當期及前期)簽帳款

   checkCancelScope1(scope);  // 查核抵用範圍
   if ( !passCheck )
      { return; }

   int eventCode = getValueInt("nfc.cancel_event");
 //setValue("crd.gp_no",getValue("cash.p_seqno"));
   setValue("crd.p_seqno",getValue("cash.p_seqno"));
   setValue("crd.current_code","0");
   int crdCnt = getLoadData(crdKey2);     // 讀 crd_card

   if ( eventCode == 2 && crdCnt == 0  ) // 無有效卡
      { logs("ttt 333 "); passCheck = false; }
   return;
 }

/****************************************************/

  // 查核抵用期間, 抵用起始月份                 //始月份
 public void checkCancelPeriod(int period,int sMonth,String startDate) throws Exception
 {
    if ( busiDate.compareTo(startDate) < 0  )
       { logs("ttt 444-A "); passCheck= false; return; }

    String  checkYYMM = getValue("wday.next_acct_month");
    String  startYYMM = startDate;
    int  checkMonth   = Integer.parseInt(getValue("wday.next_acct_month").substring(4));

    if ( startDate.length() == 8 ) {
         int  createMonth  = Integer.parseInt(startDate.substring(4,6));
         if ( sMonth < createMonth )
            { startYYMM = (Integer.parseInt(startYYMM.substring(0,4))+1) + comm.fillZero(""+sMonth,2);  }
         else
            { startYYMM = startYYMM.substring(0,4)+comm.fillZero(""+sMonth,2); }
         if ( checkYYMM.compareTo(startYYMM) < 0 )
            { logs("ttt 444-0 "); passCheck= false; return; }
     }

    if ( period == 1 ) // 查核每月
       {
         ;
       }
    else
    if ( period == 2 ) // 查核每季
      {
        if ( (checkMonth - sMonth) % 3 != 0  )
           { logs("ttt 444-1 "); passCheck = false; }
      }
    else
    if ( period == 3 ) // 查核每半年
      {
        if ( (checkMonth - sMonth) % 6 != 0  )
           { logs("ttt 444-2 "); passCheck = false; }
      }
    else
    if ( period == 4 ) // 查核每年
      {
        if ( checkMonth != sMonth )
           { logs("ttt 444-3 "); passCheck = false; }
      }

   return;
 }

  // 查核抵用範圍
 public void checkCancelScope1(int scope) throws Exception
 {
   String  acctMonth = getValue("debt.acct_month",di);
   String  acctCode  = getValue("debt.acct_code",di);

   logs("ttt check_cancel_scope_1 ");
   switch (scope) {
            case 1  :  if ( !acctMonth.equals(getValue("wday.next_acct_month")) || !Arrays.asList("BL","CA","IT","ID").contains(acctCode) )  // 當期 簽帳款
                          { logs("ttt 555-1 "); passCheck = false; }
                       break;
            case 5  :
            case 2  :  if ( !acctMonth.equals(getValue("wday.next_acct_month")) )  // 當期全部信用卡帳
                          { logs("ttt 555-2 "); passCheck = false; }
                       break;
            case 3  :  if ( !Arrays.asList("BL","CA","IT","ID").contains(acctCode) ) // 全部簽帳款
                          { logs("ttt 555-3 "); passCheck = false; }
                       break;
            case 4  :  break;    // 全部信用卡款
            default :  break;
         }

   return;
 }

 //查核 ptr_fund_data
 public void checkPtrFundData(String dataCode,String checkData,String acquire) throws Exception
 {
   String selCode = "";
   switch (dataCode) {
            case "8"  : selCode = getValue("fund.D_MCC_CODE_SEL");   break;
            case "6"  : selCode = getValue("fund.D_MERCHANT_SEL");   break;
            case "C"  : selCode = getValue("fund.D_MCHT_GROUP_SEL"); break;
            case "F"  : selCode = getValue("fund.D_UCAF_SEL");       break;
            case "G"  : selCode = getValue("fund.D_ECI_SEL");        break;
            case "E"  : selCode = getValue("fund.D_POS_ENTRY_SEL");  break;
            default   : return;
         }

   if ( selCode.equals("0") )  // (全部) 不查核
      { return; }

   setValue("data.data_key",getValue("cash.fund_code"));
   setValue("data.data_type",dataCode);
   int n = getLoadData(dataKey);

   // 特店群組(含多店) 之特店代號檢核 STARTED

   if ( dataCode.equals("C") && selCode.equals("1") ) //查核指定條件
      {
        String checkFlag = "N";
        for ( int i=0; i<n; i++ )
        {
              setValue("mktg.data_key",getValue("data.data_code",i));
              int m = getLoadData(mktgKey);
              for ( int j=0; j<m; j++ )
                  {
                   if ( checkData.equals(getValue("mktg.data_code",j)) )
                      {
                        if ( getValue("mktg.data_code2",j).length() == 0 )
                           { checkFlag = "Y"; break;}
                        else
                        if ( getValue("mktg.data_code2",j).equals(acquire) ) // 若 acquire 有值也要比
                           { checkFlag = "Y"; break;}
                      }
                  }
              if ( checkFlag.equals("Y") )
                 { break; }
        }
        if ( checkFlag.equals("N") )
           { logs("ttt AAA-1 "); passCheck = false; }

        return;
      }

   if ( dataCode.equals("C") && selCode.equals("2") ) //查核排除條件
      {
        for ( int i=0; i<n; i++ )
        {
              setValue("mktg.data_key",getValue("data.data_code",i));
              int m = getLoadData(mktgKey);
              for ( int j=0; j<m; j++ )
                  {
                   if ( checkData.equals(getValue("mktg.data_code",j)) )
                      {
                        if ( getValue("mktg.data_code2",j).length() == 0 )
                           { passCheck = false; break;}
                        else
                        if ( getValue("mktg.data_code2",j).equals(acquire) ) // 若 acquire 有值也要比
                           { passCheck = false; break;}
                      }
                  }
              if ( !passCheck )
                 { logs("ttt BBB-1 "); break; }
        }

        return;
      }

   // 特店群組(含多店) 之特店代號檢核 ENDED

   //查核特店代號指定條件
   if ( (dataCode.equals("6") || dataCode.equals("D")) && selCode.equals("1") )
      {
        String checkFlag = "N";
        for( int i=0; i < n; i++ )
        {
           if ( checkData.equals(getValue("data.data_code",i)) )
              {
                if ( getValue("data.data_code2",i).length() == 0 )
                   { checkFlag = "Y"; break;}
                else
                if ( getValue("data.data_code2",i).equals(acquire) ) // 若 acquire 有值也要比
                   { checkFlag = "Y"; break;}
              }
        }
        if ( checkFlag.equals("N") )
           { logs("ttt 666-1 ");  passCheck = false; }

        return;
      }

   // 查核特店代號排除條件
   if ( (dataCode.equals("6") || dataCode.equals("D")) && selCode.equals("2") )
      {
        for( int i=0; i < n; i++ )
        {
           if ( checkData.equals(getValue("data.data_code",i)) )
              {
                if ( getValue("data.data_code2",i).length() == 0 )
                   { logs("ttt 777 "); passCheck = false; break;}
                else
                if ( getValue("data.data_code2",i).equals(acquire) ) // 若 acquire 有值也要比
                   { logs("ttt 888 "); passCheck = false; break;}
              }
        }

        return;
      }

   //其他查核指定條件(mcc_code, ucaf, eci, pos_entry)
   if ( selCode.equals("1") )
      {
        String checkFlag = "N";
        for( int i=0; i < n; i++ )
        {
           if ( checkData.equals(getValue("data.data_code",i)) )
              {
                checkFlag = "Y"; break;
              }
        }
        if ( checkFlag.equals("N") )
           { logs("ttt 666-2 ");  passCheck = false; }

        return;
      }

   //其他查核排除條件(mcc_code, ucaf, eci, pos_entry)
   if ( selCode.equals("2") )
      {
        for( int i=0; i < n; i++ )
        {
           if ( checkData.equals(getValue("data.data_code",i)) )
              {

                logs("ttt 777 "); passCheck = false; break;
              }
        }

        return;
      }

   return;
 }

 // 查核 抵用條件 PTR_FUNDP
 public void checkCancelEvent1() throws Exception {

   int eventCode = getValueInt("fund.CANCEL_EVENT");

   if ( eventCode == 1 ) // 不限定
      { return; }

   if ( eventCode == 4 ) // 不抵用
      { logs("ttt 999-1 "); passCheck = false; return; }

 //setValue("crd.gp_no",getValue("cash.p_seqno"));
   setValue("crd.p_seqno",getValue("cash.p_seqno"));
   setValue("crd.current_code","0");
   int crdCnt = getLoadData(crdKey2);     // 讀 crd_card

//   if ( eventCode == 2  ) // 無有效卡
//      {
//        if ( crdCnt == 0  )
//           { logs("ttt 999-2 "); passCheck = false; return; }
//        else
//           { return; }
//      }

 //if ( eventCode == 2  ) // 檢核有、無有效正卡
   if ( eventCode == 2 || eventCode == 3 ) // 檢核有、無有效正卡
      {
        String  checkMajorExist = "N";
        for(int i=0; i<crdCnt; i++ ) {
            if ( getValue("crd.card_no",i).equals(getValue("crd.major_card_no",i)) )
               { checkMajorExist = "Y"; break; }
          }

        if ( checkMajorExist.equals("N") )
           { logs("ttt 999-2 "); passCheck = false; return; }
        else
          if ( eventCode == 2 )
             { return; }
      }

   setValue("data.data_key",getValue("cash.fund_code"));
   setValue("data.data_type","3");
   int m = getLoadData(dataKey);     // 讀 ptr_fund_data
   groupHash.clear();
   for(int i=0; i<m; i++ ) {
       groupHash.put(getValue("data.data_code",i),"Y");
     }

   // 查核團代開始
   if ( getValue("fund.group_code_sel").equals("0") )
      { return; }

   if ( eventCode == 3 ) {

        String  checkGroup = "N";
        for(int i=0; i<crdCnt; i++ ) {
            String grp = (String)groupHash.get(getValue("crd.group_code",i));
            if ( grp != null )
               { checkGroup = "Y"; }
          }

        if ( getValue("fund.GROUP_CODE_SEL").equals("1") && checkGroup.equals("N") ) // 指定團代但不存在
           { logs("ttt 999-3 "); passCheck = false; }

        if ( getValue("fund.GROUP_CODE_SEL").equals("2") && checkGroup.equals("Y") ) // 排除團代但存在
           { logs("ttt 999-4 "); passCheck = false; }
      }
   // 查核團代結束

   return;
 }

 // 查核新卡
 public void  checkIssueDate(int n) throws Exception {

    passCheck = false;
    int ok_cnt=0,iss_cnt=0,grp_cnt=0,total=0,diff_months=0;

    for( int k=0; k<n; k++ )
       {
         if ( getValue("crd.current_code",k).equals("0") )
            { ok_cnt++; }

         /*** 新發卡,N個月內未開卡 **/
         /*
         if ( !getValue("crd.activate_flag",k).equals("2")  && getValue("crd.old_card_no",k).length() == 0 &&
               getValue("crd.reissue_date",k).length() == 0 && getValue("crd.change_date",k).length() == 0 )
            {
               diff_months = monthsBetween(getValue("crd.issue_date",k),getValue("busi.business_date"));
               if ( diff_months > getValueInt("mpar.issue_a_months")  )
                  { iss_cnt++; }
            }
         */

         /****  有聯名卡有效卡,且不是新發卡,N個月內未開卡  ****/

         /*
         if ( getValue("crd.group_code",k).equals(getValue("mpar.group_code1")) ||
              getValue("crd.group_code",k).equals(getValue("mpar.group_code2")) ||
              getValue("crd.group_code",k).equals(getValue("mpar.group_code3")) ||
              getValue("crd.group_code",k).equals(getValue("mpar.group_code4")) ||
              getValue("crd.group_code",k).equals(getValue("mpar.group_code5")) ||
              getValue("crd.group_code",k).equals(getValue("mpar.group_code6")) )
            {
              if ( getValue("crd.current_code",k).equals("0") &&
                   diff_months <= getValueInt("mpar.issue_a_months") )
                 { grp_cnt++; }
            }
         */
         total++;
      }

     /*
     if ( total <= iss_cnt )
        { passCheck = false;  return; }
     */

     /*** 抵用條件為有效卡  ***/
     if ( getValue("mpar_cancel_event").equals("2") && ok_cnt <= 0 )
        { passCheck = false;  return; }

     /*** 抵用條件為聯名卡有效卡  ***/
/*
     if ( getValue("mpar_cancel_event").equals("3") && grp_cnt <= 0 )
        { passCheck = false;  return; }
*/
     passCheck = true;
     return;
  }

  // 查核抵用範圍 mkt_loan_parm
 public void checkCancelScope2() throws Exception {

    int     scope      =  getValueInt("mpar.cancel_scope");
    String  acctMonth  =  getValue("debt.acct_month",di);
    String  acctCode   =  getValue("debt.acct_code",di);
    /***
    if ( scope == 2  )
       { scope = 3;  } // 全部(當期及前期)簽帳款

    switch (scope) {
             case 1  : if ( !acctMonth.equals(getValue("wday.next_acct_month")) )  // 查核當期
                          { logs("ttt bbb-1 "); passCheck = false; }
                       check_acct_code(acctCode);  // 查核指定本金類
                       break;
             case 2  : break;
             case 3  :
                       check_acct_code(acctCode);  // 全部(當期及前期)簽帳款
                       break;
             default : break;
          }
    ***/

   logs("uuu check_cancel_scope_2 ");
   switch (scope) {
            case 1  :  if ( !acctMonth.equals(getValue("wday.next_acct_month")) || !Arrays.asList("BL","CA","IT","ID","AO","OT").contains(acctCode) )  // 當期 六大本金
                          { logs("uuu bbb-1 "); passCheck = false; }
                       break;
            case 2  :  if ( !acctMonth.equals(getValue("wday.next_acct_month")) )  // 當期全部信用卡帳
                          { logs("uuu bbb-2 "); passCheck = false; }
                       break;
            case 3  :  if ( !Arrays.asList("BL","CA","IT","ID","AO","OT").contains(acctCode) ) // 全部六大本金
                          { logs("uuu bbb-3 "); passCheck = false; }
                       break;
            case 4  :  break;    // 全部信用卡款
            default :  break;
         }

    return;
 }

 // 指定本金類查核
 /*** bl_cond、ca_cond、...for 產生基金的條件，非抵用的條件
 public void check_acct_code(String acctCode) throws Exception {

    if ( !Arrays.asList("BL","CA","IT","ID","AO","OT").contains(acctCode) )
       { logs("ttt ccc-1 "); passCheck = false; }

    int ttt=0;
    if ( ttt == 0 )
       { return; }

    if ( acctCode.equals("BL") && !getValue("mpar.BL_COND").equals("Y") )
       { logs("ttt ccc-1 "); passCheck = false; }

    if ( acctCode.equals("CA") && !getValue("mpar.CA_COND").equals("Y") )
       { logs("ttt ccc-2 "); passCheck = false; }

    if ( acctCode.equals("IT") && !getValue("mpar.IT_COND").equals("Y") )
       { logs("ttt ccc-3 "); passCheck = false; }

    if ( acctCode.equals("ID") && !getValue("mpar.ID_COND").equals("Y") )
       { logs("ttt ccc-4 "); passCheck = false; }

    if ( acctCode.equals("AO") && !getValue("mpar.AO_COND").equals("Y") )
       { logs("ttt ccc-5 "); passCheck = false; }

    if ( acctCode.equals("OT") && !getValue("mpar.OT_COND").equals("Y") )
       { logs("ttt ccc-6 "); passCheck = false; }

    return;
  }
 ***/

 // 查核 抵用條件 MKT_LOAN_PARM
 public void checkCancelEvent2() throws Exception {

   int eventCode = getValueInt("mpar.cancel_event");

   if ( eventCode == 1 ) // 不限定
      { logs("ttt ddd-1 "); return; }

 //setValue("crd.gp_no",getValue("cash.p_seqno"));
   setValue("crd.p_seqno",getValue("cash.p_seqno"));
   setValue("crd.current_code","0");
   int crdCnt = getLoadData(crdKey2);     // 讀 crd_card

   if ( eventCode == 2 ) // 無有效卡
      {
        if ( crdCnt == 0  )
           { logs("ttt ddd-2 "); passCheck = false; return; }
        else
           { return; }
      }

   // 以下為 3.有聯名卡有效卡之 查核
   setValue("mktp.data_key",getValue("cash.fund_code"));
   int m = getLoadData(mktpKey);     // 讀 mkt_parm_data
   groupHash.clear();
   for(int i=0; i<m; i++ ) {
       groupHash.put(getValue("mktp.data_code",i),"Y");
     }

   // 查核團代
   if ( getValue("mpar.GROUP_CODE_SEL").equals("0") )
      { return; }

   String  checkGroup = "N";
   for(int i=0; i<crdCnt; i++ ) {
       String grp = (String)groupHash.get(getValue("crd.group_code",i));
       if ( grp != null )
          { checkGroup = "Y"; }
     }

   if ( getValue("mpar.GROUP_CODE_SEL").equals("1") && checkGroup.equals("N") ) // 指定團代但不存在
      { logs("ttt ddd-3 "); passCheck = false; }

   if ( getValue("mpar.GROUP_CODE_SEL").equals("2") && checkGroup.equals("Y") ) // 排除團代但存在
      { logs("ttt ddd-4 "); passCheck = false; }

   return;
 }

 // 查核 分期專案
 public void checkBilNoInstallment() throws Exception {

   selectSQL =  "count(*) as BILL_COUNT ";
   daoTable  =  "bil_no_installment a,mkt_rcv_bin b,bil_bill c ";
   whereStr  =  "WHERE  c.reference_no = ? "
             +  "AND    a.bank_no  = b.bank_no "
             +  "AND    a.mcht_no  = ?  "
             +  "AND    lpad(b.ica_no,8,'0') = lpad(decode(c.acq_member_id,'','0',c.acq_member_id),8,'0') ";

   //String acq_no = comm.fillZero(getValue("debt.acq_no",di),8);

   setString(1,getValue("debt.reference_no",di));
   setString(2,getValue("debt.mcht_no",di));
   selectTable();
   int  n = getValueInt("BILL_COUNT");

   if ( n != 0 )
      { logs("ttt LLLLL-1 "); passCheck = false; }

   return;
 }

 public void cancelActAcagCurrRtn(double wsCanlAmt) throws Exception
 {
    if ( wsCanlAmt <= 0 )
       { return; }

    daoTable    = "act_acag_curr";
    extendField = "aacr.";
  //selectSQL   = "pay_amt,acct_month,rowid as rowid ";
    selectSQL   = "pay_amt,dc_pay_amt,acct_month,rowid as rowid ";
    whereStr    = "WHERE p_seqno = ? and curr_code = '901' order by  acct_month ";
    setString(1,getValue("cash.p_seqno"));
    int n = selectTable();

    for ( int i=0; i<n; i++ )
        {
          if ( wsCanlAmt == 0 )
             { break;  }

           /***************************************************/
           /*  delete mp detail                               */
           /***************************************************/
          if (  wsCanlAmt >= getValueDouble("aacr.pay_amt",i) )
             {
                wsCanlAmt -= getValueDouble("aacr.pay_amt",i);
                setValue("aacr.pay_amt","0",i);
                setValue("aacr.dc_pay_amt","0",i);
                deleteActAcagCurr(i);
                updateActAcag(i);
            }
            else
            {
                setValueDouble("aacr.pay_amt",getValueDouble("aacr.pay_amt",i) - wsCanlAmt,i);
                setValueDouble("aacr.dc_pay_amt",getValueDouble("aacr.dc_pay_amt",i) - wsCanlAmt,i);
                wsCanlAmt = 0;
                updateActAcagCurr(i);
                updateActAcag(i);
            }
        }

    return;
  }


 public void dDebtProcessRtn() throws Exception
  {
//   remain_cancel_amt      =  getValueDouble("cash.end_tran_amt");
//   cbf_remain_cancel_amt   =  remain_cancel_amt;
//   non_real_pay_amt       =  0;

     // 讀 VMKT_FUND_NAME
     setValue("vmkt.fund_code",getValue("cash.fund_code"));
     getLoadData(vmktKey);

     logs("ttt ttttttttttt "+getValue("cash.p_seqno"));
     logs("mkt_cashback_dtl.fund_code "+"[ "+getValue("cash.fund_code")+" ]");
     logs("mkt_cashback_dtl.end_tran_amt "+"[ "+getValueDouble("cash.end_tran_amt")+" ]");

     // 讀 ACT_DEBT
     setValue("debt.p_seqno",getValue("cash.p_seqno"));
     int  debtCnt = getLoadData(debtKey);

     srt.sortLoadData(sortField1,debtCnt);
     fundMatch = false;

     for ( int i=0;  i < debtCnt; i++ ) {

           di = srt.getSortIndex(i); // GET INDEX POINT AFTER SORT
           //logs("debt i : di "+"[ "+i+" : "+di+" ]");
           //logs("debt end_bal "+"[ "+getValueDouble("debt.end_bal",di)+" ]");
           //logs("debt can_by_fund_bal "+"[ "+getValueDouble("debt.can_by_fund_bal",di)+" ]");
           //logs("debt acct_code "+"[ "+getValue("debt.acct_code",di)+" ]");
           //logs("debt post_date "+"[ "+getValue("debt.sort_post_date",di)+" ]");

           if ( getValueLong("debt.can_by_fund_bal",di) == 0  )
              { continue; }

           // 查核 基金抵用條件
           comboCard  = false;
           cancelRate = 100;
           passCheck  = true;
           String tableName = getValue("vmkt.table_Name").toUpperCase();
           switch (tableName) {
               case "PTR_FUNDP"       : processPtrFundp();        break;  //  查核一般基金 mktm6220
               case "PTR_COMBO_FUNDP" : processPtrComboFundp();  break;  //  查核 COMBO 卡回饋基金 mktm6210
               case "MKT_LOAN_PARM"   : processMktLoanParm();    break;  //  查核專案基金 mktm4070
               case "MKT_NFC_PARM"    : processMktNfcParm();     break;  //  查核指定繳款方式基金 mktm3850
               default : showLogMessage("E","","FUND_NAME NOT FOUND ON vmkt_fund_name:"+tableName);
                         showLogMessage("E","","FUND_CODE NOT FOUND ON vmkt_fund_code:"+getValue("cash.fund_code"));
                         passCheck = false;
                         break;
                       //continue;
            }

           if ( !passCheck )  // 基金抵用查核未通過
              { continue;  }
           
           /***
           if ( getValue("cash.fund_code").substring(0,1).equals("L") )
              { check_bil_no_installment();  } // 查核 分期專案
           ***/
           if ( tableName.equals("MKT_LOAN_PARM") && getValue("mpar.BIL_MCHT_COND").equals("Y") )
              { checkBilNoInstallment();  } // 查核 分期專案

           if ( !passCheck )  // 基金抵用查核未通過
              { continue;  }

           fundMatch = true;
           setValue("pcod.acct_code",getValue("debt.acct_code",di));
           getLoadData(pcodKey);  // 讀  ptr_actcode

           setValue("merc.mcht_no",getValue("debt.mcht_no",di));
           getLoadData(mercKey);  // 讀  bil_merchant
           if ( getValue("debt.new_it_flag",di).equals("Y") )
              { setValue("merc.mp_rate","100"); }

           debtEndBalance = getValueDouble("debt.end_bal",di);
           debtCanByFundBal   =  getValueDouble("debt.can_by_fund_bal",di);
           cbfRateBalance     = getValueLong("debt.beg_bal",di) * cancelRate / 100;
           wsTempAmt      = (long)(cbfRateBalance + 0.5);
           cbfRateBalance     = wsTempAmt;
           if(debtCanByFundBal < cbfRateBalance)
              cbfRateBalance = debtCanByFundBal;

           if(debtEndBalance < cbfRateBalance)
              {rateBalance = debtEndBalance;}
           else
              {rateBalance = cbfRateBalance;}

           if ( remainCancelAmt >=  rateBalance ) {
              //showLogMessage("D","","ttt-DEBT 完全沖銷 ");
                logs("ttt-DEBT可沖 完全沖銷 ");
                realCancelAmt    =  rateBalance;
                remainCancelAmt  =  remainCancelAmt - rateBalance;
              }
           else  {
              //showLogMessage("D","","ttt-基金完全沖銷 ");
                logs("ttt-基金完全沖銷 ");
                realCancelAmt   = remainCancelAmt;
                remainCancelAmt = 0;
             }

           // 計算 基金沖銷當期交易金額 Start
         //if ( getValue("debt.acct_month").equals(getValue("wday.next_acct_month")) )
           if ( getValue("debt.acct_month",di).equals(getValue("wday.next_acct_month")) )
              {
                nonRealPayAmt += realCancelAmt;
              }
           // 計算 基金沖銷當期交易金額 Ended


           // 計算 基金轉溢付款金額 START
           if ( cbfRemainCancelAmt  >= debtCanByFundBal )  {
                cbfRealCancelAmt    =  debtCanByFundBal;
                cbfRemainCancelAmt -=  debtCanByFundBal;
              }
           else  {
                cbfRealCancelAmt   = cbfRemainCancelAmt;
                cbfRemainCancelAmt = 0;
             }

           if ( remainCancelAmt >  cbfRemainCancelAmt )
              {
                remainCancelAmt    =  cbfRemainCancelAmt;
              }

           debtCanByFundBal    -= cbfRealCancelAmt; // 計算 DEBT CAN_BY_FUND 銷帳後餘額
           cbfTotRealCancelAmt  += cbfRealCancelAmt; // 計算 DEBT CAN_BY_FUND 總銷帳金額
           // 計算 基金轉溢付款金額 ENDED

           debtEndBalance    -= realCancelAmt; // 計算 DEBT 銷帳後餘額
           totRealCancelAmt += realCancelAmt; // 計算總銷帳金額

           setValue("jrnl.transaction_amt",""+realCancelAmt);
           insertActJrnl2();
           insertMktCashbackDtl();

         //以下 update act_acct & act_acct_curr min_pay_bal
           if ( getValueDouble("acur.min_pay_bal") - realCancelAmt  > 0 )
               { tempLong = (long)(realCancelAmt); }
           else
               { tempLong = getValueLong("acur.min_pay_bal"); }

           if ( !getValue("debt.acct_month",di).equals(getValue("wday.next_acct_month")) ) {
                 setValueDouble("acur.min_pay_bal",getValueDouble("acur.min_pay_bal") - tempLong);
                 setValueDouble("acct.min_pay_bal",getValueDouble("acct.min_pay_bal") - tempLong);
           }

         //insert_cyc_vouch_data();
           if ( cbfRealCancelAmt   >  realCancelAmt )  {
                cbfTurnOpAmt    =  cbfRealCancelAmt - realCancelAmt;
           }
           else  {
                cbfTurnOpAmt    =  0;
           }

           if ( realCancelAmt >  0 ) {
                hCycVouchAmt        =  realCancelAmt;
                hCycVouchDataType  =  "4";
                insertCycVouchData();
           }

           if ( cbfTurnOpAmt >  0 )  {
                hCycVouchAmt        =  cbfTurnOpAmt;
                hCycVouchDataType  =  "C";
                insertCycVouchData();
           }

           updateActDebt();

           if ( realCancelAmt > 0 && getValue("pcod.interest_method").equals("Y")  &&
                getValue("debt.acct_month",di).compareTo(getValue("wday.this_acct_month")) <= 0 )
              { calculateInterestRtn(); }

           //基金沖銷完畢
           if ( cbfRemainCancelAmt == 0 )
              { break; }
           //if ( remain_cancel_amt == 0 )
           //   { break; }
    }

   return;
 }

public void calculateInterestRtn() throws Exception
 {
    double subInterestRate=0;

    setValue("pcod.acct_code",getValue("debt.acct_code",di));
    getLoadData(pcodKey);

    int k = getValueInt("pcod.inter_rate_code");
    wsInterestRate     = tempRevolvingInterest[k];
    fitRevolvingRate1  = tempRevolvingInterest[k];

    if (pSeqno.length()!=0)
       {
        showLogMessage("I","","Mantis 9334-01 temp_revolving_interest["+k+"] : [" + tempRevolvingInterest[1]  +"]");
        showLogMessage("I","","Mantis 9334-02 ori ws_interest_rate   : [" + wsInterestRate +"]");
        showLogMessage("I","","Mantis 9334-03 rc_max_rate date   : [" + getValueDouble("cash.rc_max_rate") +"]");
       }

    if ( getValue("debt.interest_rs_date",di).length() >= 6 )
       {
         if ( getValue("debt.interest_rs_date",di).substring(0,6).compareTo(busiDate.substring(0,6)) >= 0 )
            { return; }
         else
            {
              inBegDate  = getValue("debt.interest_rs_date",di);
              outEndDate = busiDate;
            }
      }

    if ( getValue("debt.interest_rs_date",di).length() == 0)
       {
         if ( getValue("debt.acct_month",di).compareTo(getValue("wday.this_acct_month")) == 0 )
            {
              /*- 當期若繳款日小於等於寬限期則不計息, 否則同原計算方式 */
              if ( busiDate.compareTo(getValue("wday.this_delaypay_date")) <= 0 )
                 { return; }

                if( getValueInt("merc.mp_rate") == 100 )
                  { inBegDate = getValue("wday.this_lastpay_date");  }
               else
                  { inBegDate = getValue("debt.interest_date",di); }
               outEndDate = busiDate;
           }
         else
           {
              if ( busiDate.compareTo(getValue("wday.this_delaypay_date")) <= 0 )
                 { return; }

              if ( getValue("acno.new_cycle_month").length() > 0  &&
                   getValue("wday.this_acct_month").equals(getValue("acno.new_cycle_month") ) )
                 { inBegDate = getValue("acno.last_interest_date"); }
              else
                 { inBegDate = getValue("wday.this_interest_date"); }
              outEndDate = busiDate;
          }
       }

   if ( getValueInt("acno.INT_RATE_MCODE") == 0 && getValue("debt.int_rate_flag",di).equals("Y"))
      { wsInterestRate = getValueDouble("debt.int_rate",di);  }
   else
      {
        wsInterestRate = fitRevolvingRate1;

        if ( getValue("wday.next_acct_month").compareTo(getValue("acno.revolve_rate_s_month")) >= 0  &&
             getValue("wday.next_acct_month").compareTo(getValue("acno.revolve_rate_e_month")) <= 0 )
           {
             if ( getValue("acno.revolve_int_sign").equals("+") )
                { subInterestRate = 0 + getValueDouble("acno.revolve_int_rate"); }
             else
                { subInterestRate = 0 - getValueDouble("acno.revolve_int_rate"); }
           }

       if ( getValue("wday.next_acct_month").compareTo(getValue("acno.group_rate_s_month")) >=0 &&
            getValue("wday.next_acct_month").compareTo(getValue("acno.group_rate_e_month")) <= 0 )
          {
            if ( subInterestRate > getValueDouble("acno.group_int_rate") )
               { subInterestRate = getValueDouble("acno.group_int_rate"); }
          }

       if ( getValue("wday.next_acct_month").compareTo(getValue("acno.batch_rate_s_month")) >= 0  &&
            getValue("wday.next_acct_month").compareTo(getValue("acno.batch_rate_e_month")) <= 0 )
          {
            if ( subInterestRate > getValueDouble("acno.batch_int_rate") )
               { subInterestRate = getValueDouble("acno.batch_int_rate"); }
          }

        wsInterestRate = wsInterestRate + subInterestRate;

        if (pSeqno.length()!=0)
           showLogMessage("I","","Mantis 9334-04 bef ws_interest_rate   : [" + wsInterestRate +"]");
   
        if (wsInterestRate < 0)
           wsInterestRate = 0;

        if (wsInterestRate > getValueDouble("cash.rc_max_rate"))
           wsInterestRate = getValueDouble("cash.rc_max_rate");

        if (pSeqno.length()!=0)
           showLogMessage("I","","Mantis 9334-05 aft ws_interest_rate   : [" + wsInterestRate +"]");
      }

   computeInterestRtn();
 }

public void computeInterestRtn() throws Exception
 {
       int days = comm.datePeriod(inBegDate,outEndDate);
       inCreditAmt   = realCancelAmt;
       wsInterestAmt = inCreditAmt * days * wsInterestRate / 10000;

       long tempLong = (long)(wsInterestAmt * 100.0 + 0.5);
       wsInterestAmt  = ((double)tempLong) / 100.0;
       wsInterestAmt = convAmt(wsInterestAmt);

       wsInterestAmtTot = wsInterestAmtTot + wsInterestAmt;

       if ( wsInterestAmt > 0 )
          { insertActIntr(); }
 }

/******************* SELECT *************/
public int  selectPtrBysinday() throws Exception
 {
   daoTable    = "ptr_businday";
   extendField = "busi.";
 //selectSQL   = "business_date,"
 //            + "to_char(add_months(to_date(business_date,'YYYYMMDD'),-6),'YYYYMMDD') as ws_bef_6m_date";
   selectSQL   = "decode( cast(? as varchar(8)) ,'',business_date, ? ) as business_date, "
               + "to_char(add_months(to_date(decode( cast(? as varchar(8)) ,'',business_date, ? ),'YYYYMMDD'),-6),'YYYYMMDD') as ws_bef_6m_date ";
   setString(1, busiDate);
   setString(2, busiDate);
   setString(3, busiDate);
   setString(4, busiDate);
   whereStr    = " fetch first 1 rows only ";
   int n = selectTable();
   if ( n == 0 )
      { showLogMessage("E","","select_ptr_businday ERROR "); exitProgram(3); }
   busiDate = getValue("busi.business_date");
   return n;
 }

 public int  selectPtrCurrGeneral() throws Exception
 {
   daoTable    = "ptr_curr_general";
   extendField = "pcgl.";
   selectSQL   = "total_bal ";
   whereStr    = "where curr_code = '901' ";
   int n = selectTable();
   return n;
 }

public int checkCrdCard() throws Exception
 {
  //setValue("crd.gp_no",getValue("cash.p_seqno"));
    setValue("crd.p_seqno",getValue("cash.p_seqno"));
    setValue("crd.current_code","0");
    int n = getLoadData(crdKey2);

    setValue("type.card_type",getValue("crd.card_type"));
    getLoadData(typeKey);

    if ( !getValue("type.card_note").equals("I") )
       { return 0; }

    if ( !getValue("acno.vip_code").equals("6S") )
       { return 0; }
    return n;
 }

public int updateActDebt() throws Exception
 {
    daoTable    = "act_debt";
    extendField = "debt.";
    updateSQL   = "end_bal            = ?,"
                + "dc_end_bal         = ?,"
                + "can_by_fund_bal    = ?,"
                + "dc_can_by_fund_bal = ?,"
                + "mod_time           = sysdate,"
                + "mod_pgm            = ? ";
    whereStr    = "WHERE rowid = ? ";
    setString(1,""+debtEndBalance);
    setString(2,""+debtEndBalance);
    setString(3,""+debtCanByFundBal);
    setString(4,""+debtCanByFundBal);
    setString(5,javaProgram);
    setRowId(6,getValue("debt.rowid",di));

    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_debt_1 ERROR: "+getValue("debt.rowid",di) );  }

    /* UPDATE DEBT LOAD BUFFER */

   int lsSortBalDecode = 0;
   if ( debtEndBalance <= 0 )
      { lsSortBalDecode = 0;  }
   else
      { lsSortBalDecode = 1;  }

   setUpdateLoad("debt.end_bal",""+debtEndBalance);
   setUpdateLoad("debt.dc_end_bal",""+debtEndBalance);
   setUpdateLoad("debt.can_by_fund_bal",""+debtCanByFundBal);
   setUpdateLoad("debt.dc_can_by_fund_bal",""+debtCanByFundBal);
 //setUpdateLoad("debt.sort_bal",comm.fillZero(""+(long)debt_end_balance,10));
   setUpdateLoad("debt.sort_bal_decode",""+lsSortBalDecode);
   updateLoadTable(debtKey,di);

   return n;
}

public int updateActAcno() throws Exception
{
   daoTable  = "act_acno";
   updateSQL = "last_pay_amt  = ?,"
             + "last_pay_date = ?,"
             + "mod_time      = sysdate,"
             + "mod_pgm       = ? ";
   whereStr  = "where p_seqno = ? ";
   setLong(1,(long)totRealCancelAmt);
   setString(2,busiDate);
   setString(3,javaProgram);
   setString(4,getValue("acno.p_seqno"));
   int n = updateTable();
   if ( n == 0 )
      { showLogMessage("E","","update_act_acno ERROR: "+getValue("acno.p_seqno"));  }

   return n;
 }

public int updateActAcct() throws Exception
{
   daoTable  =  "act_acct";
   updateSQL =  "acct_jrnl_bal         = ?,"
             +  "end_bal_op            = ?,"
             +  "temp_unbill_interest  = ?,"
             +  "min_pay_bal           = ?,"
             +  "rc_min_pay_bal        = ?,"
             +  "rc_min_pay_m0         = ?,"
             +  "ttl_amt_bal           = ?,"
             +  "last_payment_date     = ?,"
             +  "last_min_pay_date     = ?,"
             +  "last_cancel_debt_date = ?,"
             +  "mod_time              = sysdate,"
             +  "mod_pgm               = ? ";
    whereStr = " where  p_seqno        = ? ";

    setLong(1,getValueLong("acct.acct_jrnl_bal"));
    setLong(2,getValueLong("acct.end_bal_op"));
    setDouble(3,getValueDouble("acct.temp_unbill_interest"));
    setLong(4,getValueLong("acct.min_pay_bal"));
    setLong(5,getValueLong("acct.rc_min_pay_bal"));
    setLong(6,getValueLong("acct.rc_min_pay_m0"));
    setLong(7,getValueLong("acct.ttl_amt_bal"));
    setString(8,getValue("acct.last_payment_date"));
    setString(9,getValue("acct.last_min_pay_date"));
    setString(10,getValue("acct.last_cancel_debt_date"));
    setString(11,javaProgram);
    setString(12,getValue("acct.p_seqno"));
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_acct ERROR: "+getValue("acct.p_seqno"));  }

    setUpdateLoad("acct.acct_jrnl_bal",""+getValueLong("acct.acct_jrnl_bal"));
    setUpdateLoad("acct.end_bal_op",""+getValueLong("acct.end_bal_op"));
    setUpdateLoad("acct.temp_unbill_interest",""+getValueDouble("acct.temp_unbill_interest"));
    setUpdateLoad("acct.min_pay_bal",""+getValueLong("acct.min_pay_bal"));
    setUpdateLoad("acct.rc_min_pay_bal",""+getValueLong("acct.rc_min_pay_bal"));
    setUpdateLoad("acct.rc_min_pay_m0",""+getValueLong("acct.rc_min_pay_m0"));
    setUpdateLoad("acct.ttl_amt_bal",""+getValueLong("acct.ttl_amt_bal"));
    setUpdateLoad("acct.last_payment_date",getValue("acct.last_payment_date"));
    setUpdateLoad("acct.last_min_pay_date",getValue("acct.last_min_pay_date"));
    setUpdateLoad("acct.last_cancel_debt_date",getValue("acct.last_cancel_debt_date"));
    updateLoadTable(acctKey);
    return n;
}

public int updateActAcctCurr() throws Exception
{
   daoTable  = "act_acct_curr";
   updateSQL = "acct_jrnl_bal           = ?,"
             + "dc_acct_jrnl_bal        = ?,"
             + "end_bal_op              = ?,"
             + "dc_end_bal_op           = ?,"
             + "temp_unbill_interest    = ?,"
             + "dc_temp_unbill_interest = ?,"
             + "min_pay_bal             = ?,"
             + "dc_min_pay_bal          = ?,"
             + "ttl_amt_bal             = ?,"
             + "dc_ttl_amt_bal          = ?,"
             + "delaypay_ok_flag        = ?,"
             + "mod_time                = sysdate,"
             + "mod_pgm                 = ? ";
    whereStr = "where p_seqno = ? and curr_code = '901' ";

    setLong(1,getValueLong("acur.acct_jrnl_bal"));
    setLong(2,getValueLong("acur.acct_jrnl_bal"));
    setLong(3,getValueLong("acur.end_bal_op"));
    setLong(4,getValueLong("acur.end_bal_op"));
    setDouble(5,getValueDouble("acur.temp_unbill_interest"));
    setDouble(6,getValueDouble("acur.temp_unbill_interest"));
    setLong(7,getValueLong("acur.min_pay_bal"));
    setLong(8,getValueLong("acur.min_pay_bal"));
    setLong(9,getValueLong("acur.ttl_amt_bal"));
    setLong(10,getValueLong("acur.ttl_amt_bal"));
    setString(11,getValue("acur.delaypay_ok_flag"));
    setString(12,javaProgram);
    setString(13,getValue("acur.p_seqno"));
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_acct_curr ERROR: "+getValue("acct.p_seqno"));  }

    setUpdateLoad("acur.acct_jrnl_bal",getValue("acur.acct_jrnl_bal"));
    setUpdateLoad("acur.end_bal_op",getValue("acur.end_bal_op"));
    setUpdateLoad("acur.temp_unbill_interest",getValue("acur.temp_unbill_interest"));
    setUpdateLoad("acur.min_pay_bal",getValue("acur.min_pay_bal"));
    setUpdateLoad("acur.ttl_amt_bal",getValue("acur.ttl_amt_bal"));
    setUpdateLoad("acur.delaypay_ok_flag",getValue("acur.delaypay_ok_flag"));
    updateLoadTable(acurKey);
    return n;
 }

 public int updateActAcctCurr1() throws Exception
 {
     daoTable  =  "act_acct_curr";
     updateSQL =  "no_interest_flag    = 'Y',"
               +  "no_interest_s_month = ?,"
               +  "no_interest_e_month = ?,"
               +  "mod_time            = sysdate,"
               +  "mod_user            = ?,"
               +  "mod_pgm             = ?";
     whereStr  =  "WHERE p_seqno       = ?"
               +  " AND  curr_code     = '901' ";

     setString(1,getValue("wday.next_acct_month"));
     setString(2,getValue("wday.next_acct_month"));
     setString(3,javaProgram);
     setString(4,javaProgram);
     setString(5,getValue("cash.p_seqno"));
     int n = updateTable();
     if ( n == 0 )
        { showLogMessage("E","","update_act_acct_curr_1 ERROR: "+getValue("cash.p_seqno") );  }

    setUpdateLoad("acur.no_interest_flag",getValue("acur.no_interest_flag"));
    setUpdateLoad("acur.no_interest_s_month",getValue("acur.no_interest_s_month"));
    setUpdateLoad("acur.no_interest_e_month",getValue("acur.no_interest_e_month"));
     return n;
 }

public int updateActAcag(int i) throws Exception
 {
   extendField = "temp.";
   daoTable    = "act_acag_curr";
   selectSQL   = "sum(pay_amt) as  pay_amt ";
   whereStr    = "WHERE p_seqno    = ? "
               + "AND   acct_month = ?";
   setString(1,getValue("cash.p_seqno"));
   setString(2,getValue("aacr.acct_month",i));
   selectTable();

   double tempAmount = getValueDouble("temp.pay_amt");

   daoTable  = "act_acag ";
   updateSQL = "pay_amt = ?, "
             + "mod_time  = sysdate,"
             + "mod_user  = ?,"
             + "mod_pgm   = ? ";
   whereStr  = "WHERE p_seqno = ? AND acct_month = ? ";

   setString(1,""+tempAmount);
   setString(2,javaProgram);
   setString(3,javaProgram);
   setString(4,getValue("cash.p_seqno"));
   setString(5,getValue("aacr.acct_month",i));
   int n = updateTable();
   if ( n == 0 )
      { showLogMessage("E","","update_act_acag ERROR: "+getValue("cash.p_seqno")+" "+getValue("aacr.acct_month",i)); }
   return n;
 }

public int updateActAcagCurr(int i) throws Exception
 {
   daoTable  = "act_acag_curr";
   updateSQL = "pay_amt    = ?,"
             + "dc_pay_amt = ?,"
             + "mod_time  = sysdate,"
             + "mod_user  = ?,"
             + "mod_pgm   = ? ";
   whereStr  = "WHERE rowid = ? ";
   setLong(1,getValueLong("aacr.pay_amt",i));
   setLong(2,getValueLong("aacr.dc_pay_amt",i));
   setString(3,javaProgram);
   setString(4,javaProgram);
   setRowId(5,getValue("aacr.rowid",i));
   int n = updateTable();
   if ( n == 0 )
      { showLogMessage("E","","update_act_acag_curr ERROR: "+getValue("aacr.rowid",i));  }

   return n;
 }

public void insertCycPyaj() throws Exception
 {
    daoTable    = "cyc_pyaj";
    extendField = "pyaj.";
    setValue("pyaj.p_seqno",getValue("cash.p_seqno"));
    setValue("pyaj.curr_code","901");
    setValue("pyaj.acct_type",getValue("acct.acct_type"));
    setValue("pyaj.class_code","B");
    setValue("pyaj.payment_date",busiDate);
    setValue("pyaj.stmt_cycle",stmtCycle);
    setValue("pyaj.settlement_flag","U");
//  setValue("pyaj.payment_amt",""+tot_real_cancel_amt);
//  setValue("pyaj.dc_payment_amt",""+tot_real_cancel_amt); modified on 2019/04/01
    setValue("pyaj.payment_amt",""+cbfTotRealCancelAmt);
    setValue("pyaj.dc_payment_amt",""+cbfTotRealCancelAmt);
    setValue("pyaj.payment_type",getValue("cash.fund_code").substring(0,4));
    setValue("pyaj.fund_code",getValue("cash.fund_code"));
    setValue("pyaj.reference_no","");
    setValue("pyaj.crt_user",javaProgram);
  //setValue("pyaj.crt_date",sysDate);
    setValue("pyaj.crt_date",busiDate);
    setValue("pyaj.mod_pgm",javaProgram);
    setValue("pyaj.mod_time",sysDate+sysTime);
    insertTable();
    return;
}

public void insertActIntr() throws Exception
 {
    daoTable    = "act_intr";
    extendField = "intr.";

    hIntrEnqSeqno++;
    setValue("intr.create_date",sysDate);
    setValue("intr.create_time",sysTime);
    setValue("intr.enq_seqno",""+hIntrEnqSeqno);
    setValue("intr.p_seqno",getValue("cash.p_seqno"));
    setValue("intr.curr_code","901");
    setValue("intr.acct_type",getValue("acct.acct_type"));
    setValue("intr.post_date",getValue("busi.business_date"));
    setValue("intr.stmt_cycle",getValue("acno.stmt_cycle"));
    setValue("intr.acct_month",getValue("wday.next_acct_month"));

    setValue("intr.mod_time",sysDate+sysTime);
    setValue("intr.mod_pgm",javaProgram);
    setValue("intr.interest_sign","+");
    setValue("intr.inte_d_amt",""+wsInterestAmt);
    setValue("intr.dc_inte_d_amt",""+wsInterestAmt);
    setValue("intr.interest_amt",""+wsInterestAmt);
    setValue("intr.dc_interest_amt",""+wsInterestAmt);
    setValue("intr.reason_code","DB01");
    setValue("intr.interest_rate",""+wsInterestRate);
    setValue("intr.reference_no",getValue("debt.reference_no",di));
    setValue("intr.ao_flag",getValue("debt.ao_flag",di));
    setValue("intr.intr_s_date",inBegDate);
    setValue("intr.intr_e_date",outEndDate);
    setValue("intr.crt_date",sysDate);
    setValue("intr.crt_time",sysTime);
    setValue("intr.crt_user",javaProgram);
    insertTable();
   return;
}

public void insertActJrnl1() throws Exception
{

    enqNo++;
    jrnlOrderSeq++;
    extendField = "jrnl.";

    setValue("jrnl.create_date",sysDate);
    setValue("jrnl.create_time",sysTime);
    setValue("jrnl.enq_seqno",""+enqNo);
    setValue("jrnl.p_seqno",getValue("cash.p_seqno"));
    setValue("jrnl.curr_code","901");
    setValue("jrnl.acct_type",getValue("acct.acct_type"));
    setValue("jrnl.acct_key","");
    setValue("jrnl.corp_p_seqno",getValue("acno.corp_p_seqno"));
    setValue("jrnl.id_p_seqno",getValue("acno.id_p_seqno"));
    setValue("jrnl.acct_date",getValue("busi.business_date"));
    setValue("jrnl.tran_class","P");
    setValue("jrnl.acct_code","PY");
    setValue("jrnl.dr_cr","D");
    setValue("jrnl.dc_transaction_amt",getValue("jrnl.transaction_amt"));
    setValue("jrnl.jrnl_bal",getValue("acur.acct_jrnl_bal"));
    setValue("jrnl.dc_jrnl_bal",getValue("acur.acct_jrnl_bal"));
    setValue("jrnl.item_bal","0");
    setValue("jrnl.dc_item_bal","0");
    setValue("jrnl.interest_date",busiDate);
    setValue("jrnl.pay_id",getValue("debt.card_no",di));
    setValue("jrnl.stmt_cycle",getValue("acno.stmt_cycle"));
    setValue("jrnl.batch_no","");
    setValue("jrnl.serial_no","");
    setValue("jrnl.item_d_bal","0");
    setValue("jrnl.dc_item_d_bal","0");
    setValue("jrnl.reference_no","");
    setValue("jrnl.can_by_fund_bal","0");
    setValue("jrnl.dc_can_by_fund_bal","0");

    setValue("jrnl.order_seq",""+jrnlOrderSeq);
    setValue("jrnl.mod_user",javaProgram);
    setValue("jrnl.mod_time",sysDate+sysTime);
    setValue("jrnl.mod_pgm",javaProgram);
    setValue("jrnl.mod_ws","");
    setValue("jrnl.crt_date",sysDate);
    setValue("jrnl.crt_time",sysTime);
    setValue("jrnl.crt_user",javaProgram);

    daoTable = "act_jrnl";
    insertTable();
    return;
}

public void insertActJrnl2() throws Exception
{

    enqNo++;
    jrnlOrderSeq++;
    extendField = "jrnl.";

    setValue("jrnl.create_date",sysDate);
    setValue("jrnl.create_time",sysTime);
    setValue("jrnl.enq_seqno",""+enqNo);
    setValue("jrnl.p_seqno",getValue("cash.p_seqno"));
    setValue("jrnl.curr_code","901");
    setValue("jrnl.acct_type",getValue("acct.acct_type"));
    setValue("jrnl.acct_key","");
    setValue("jrnl.corp_p_seqno",getValue("acno.corp_p_seqno"));
    setValue("jrnl.id_p_seqno",getValue("acno.id_p_seqno"));
    setValue("jrnl.acct_date",getValue("busi.business_date"));
    setValue("jrnl.tran_class","D");
    setValue("jrnl.tran_type", "DEBF");
    setValue("jrnl.acct_code",getValue("debt.acct_code",di));
    setValue("jrnl.dr_cr","D");
    setValue("jrnl.dc_transaction_amt",getValue("jrnl.transaction_amt"));
    setValue("jrnl.jrnl_bal","0");
    setValue("jrnl.dc_jrnl_bal","0");

    setValue("jrnl.item_bal",""+debtEndBalance);
    setValue("jrnl.dc_item_bal",""+debtEndBalance);

    setValue("jrnl.item_d_bal",getValue("debt.d_avail_bal",di));
    setValue("jrnl.dc_item_d_bal",getValue("debt.d_avail_bal",di));

    setValue("jrnl.can_by_fund_bal",""+debtCanByFundBal);
    setValue("jrnl.dc_can_by_fund_bal",""+debtCanByFundBal);

    setValue("jrnl.item_date",getValue("debt.post_date",di));
    setValue("jrnl.interest_date",getValue("debt.interest_date",di));
    setValue("jrnl.reference_no",getValue("debt.reference_no",di));
    setValue("jrnl.pay_id",getValue("debt.card_no",di));
    setValue("jrnl.stmt_cycle",getValue("acno.stmt_cycle"));
    setValue("jrnl.batch_no","");
    setValue("jrnl.serial_no","");
    setValue("jrnl.order_seq",""+jrnlOrderSeq);
    setValue("jrnl.mod_user",javaProgram);
    setValue("jrnl.mod_time",sysDate+sysTime);
    setValue("jrnl.mod_pgm",javaProgram);
    setValue("jrnl.mod_ws","");
    setValue("jrnl.crt_date",sysDate);
    setValue("jrnl.crt_time",sysTime);
    setValue("jrnl.crt_user",javaProgram);

    daoTable = "act_jrnl";
    insertTable();

    return;
}

public void insertMktCashbackDtl() throws Exception
{
    String  tranSeqno     = comr.getSeqno("mkt_modseq");
    dateTime();

    setValue("TRAN_DATE",sysDate);
    setValue("TRAN_TIME",sysTime);
    setValue("FUND_CODE",getValue("cash.fund_code"));
    setValue("FUND_NAME",getValue("vmkt.fund_name"));
    setValue("P_SEQNO",getValue("cash.p_seqno"));
  //setValue("ID_P_SEQNO",getValue("acno.p_seqno"));
    setValue("ID_P_SEQNO",getValue("acno.id_p_seqno"));
    setValue("ACCT_TYPE",getValue("acct.acct_type"));
    setValue("TRAN_CODE","4");
    setValue("TRAN_PGM" ,"ActE010");
//    setValue("BEG_TRAN_AMT" ,""+(real_cancel_amt *-1));
//    setValue("END_TRAN_AMT" ,""+(real_cancel_amt *-1));
    setValue("BEG_TRAN_AMT" ,""+(cbfRealCancelAmt *-1));
    setValue("END_TRAN_AMT" ,""+(cbfRealCancelAmt *-1));
    setValue("MOD_DESC","卡人基金銷帳");
    setValue("ACCT_MONTH",busiDate.substring(0,6));
    setValue("EFFECT_E_DATE",busiDate);
    setValue("TRAN_SEQNO",tranSeqno);
    setValue("PROC_MONTH" ,getValue("busi.business_date").substring(0,6));
    setValue("ACCT_DATE",getValue("busi.business_date"));
    setValue("mod_memo", "");
    setValue("mod_reason", "");
    setValue("case_list_flag", "N");
    setValue("crt_user", javaProgram);
    setValue("crt_date", busiDate);
    setValue("apr_date", busiDate);
    setValue("apr_user", javaProgram);
    setValue("apr_flag", "Y");
    setValue("mod_user", javaProgram);
    setValue("mod_time", sysDate+sysTime);
    setValue("mod_pgm", javaProgram);

    daoTable = "mkt_cashback_dtl";
    insertTable();
    return;
}

/******************* DELETE *************/
public int deleteActAcag() throws Exception
 {
    daoTable = "act_acag";
    whereStr = "WHERE p_seqno = ? and pay_amt = 0";
    setString(1,getValue("cash.p_seqno"));
    int n = deleteTable();
    return n;
 }

public int deleteActAcagCurr(int i ) throws Exception
 {
    daoTable = "act_acag_curr";
    whereStr = "WHERE rowid = ? ";
    setRowId(1,getValue("aacr.rowid",i));
    int n = deleteTable();
    return n;
 }

 //double 變數運算後會發生 .99999999...的問題，例如 19.33, 實際會變成 19.329999999999999...，所以執行 conv_amt(x)變成 19.33
 public double  convAmt(double cvt_amt) throws Exception
 {
   long   cvtLong   = (long) Math.round(cvt_amt * 100.0 + 0.000001);
   double cvtDouble =  ((double) cvtLong) / 100;
   return cvtDouble;
 }

/******************* LOAD *************/
 public int  loadPtrWorkday() throws Exception
 {
    daoTable    = "ptr_workday";
    extendField = "wday.";
    selectSQL   = "stmt_cycle,"
                + "this_acct_month,"
                + "last_acct_month,"
                + "next_acct_month,"
                + "this_close_date,"
                + "last_close_date,"
                + "next_close_date,"
                + "ll_close_date,"
                + "this_interest_date,"
                + "this_lastpay_date,"
                + "this_delaypay_date";
    whereStr  = "order by stmt_cycle";
    int n = loadTable();
    setLoadData(wdayKey);
    for ( int i=0; i<n; i++ )  {
          if ( getList("wday.next_close_date",i).equals(getValue("busi.business_date")) ) {
               stmtCycle = getList("wday.stmt_cycle",i);
               thisAcctMonth = getList("wday.this_acct_month",i);
               showLogMessage("I","","TODAY IS AN CYCLE_DATE : "+stmtCycle);
             }
        }

    if ( stmtCycle.length() == 0 )
       { showLogMessage("I","","TODAY NOT AN CYCLE_DATE : "); return 0; }
    return n;
 }

 public void  loadMktLoanParm() throws Exception
 {
     extendField = "mpar.";
     daoTable    = "mkt_loan_parm";
     selectSQL   = "fund_code,"
                 + "fund_name,"
                 + "effect_months,"
                 + "acct_type_sel,"
                 + "group_code_sel,"
                 + "exec_s_months,"
                 + "issue_a_months,"
                 + "mcode,"
                 + "bl_cond,"
                 + "it_cond,"
                 + "id_cond,"
                 + "ao_cond,"
                 + "ca_cond,"
                 + "ot_cond,"
                 + "cancel_rate,"
                 + "cancel_event,"
                 + "cancel_scope,"
                 + "bill_print,"
                 + "merchant_sel,"
                 + "mcht_group_sel,"
                 + "bil_mcht_cond,"
                 + "feedback_lmt ";
     whereStr    = "order by fund_code";
     int n = loadTable();
     setLoadData(mparKey);

     return;
 }

 public void  loadPtrFundp() throws Exception
 {
     extendField = "fund.";
     daoTable    = "ptr_fundp";
     selectSQL   = "fund_crt_date_s,"
                 + "cancel_period,"
                 + "cancel_s_month,"
                 + "cancel_scope,"
                 + "d_mcc_code_sel,"
                 + "d_merchant_sel,"
                 + "d_mcht_group_sel,"
                 + "d_ucaf_sel,"
                 + "d_eci_sel,"
                 + "d_pos_entry_sel,"
                 + "cancel_event,"
                 + "min_mcode,"
                 + "fund_code,"
                 + "merchant_sel,"
                 + "mcht_group_sel,"
                 + "group_code_sel ";
     whereStr    = "";
     int n = loadTable();
     setLoadData(fundKey);
     return;
 }

 public void  loadPtrComboFundp() throws Exception
 {
     extendField = "cofp.";
     daoTable    = "ptr_combo_fundp";
     selectSQL   = "fund_code,"
                 + "fund_name,"
                 + "cmb_rate,"
                 + "stop_flag,"
                 + "stop_date,"
                 + "stop_desc,"
                 + "effect_months,"
                 + "feedback_lmt,"
                 + "cmb_oth_cond1,"
                 + "cmb_oth_cond2,"
                 + "cmb_oth_cond3,"
                 + "cancel_period,"
                 + "cancel_s_month,"
                 + "cancel_unbill_type,"
                 + "cancel_unbill_rate,"
                 + "cancel_event,"
                 + "apr_date";
     int n = loadTable();
     setLoadData(cofpKey);
     return;
 }

 public void  processSkipHash() throws Exception
 {
   daoTable    = "mkt_cashback_dtl";
   selectSQL   = "p_seqno";
   whereStr    = "WHERE p_seqno in ( select P_SEQNO from act_acno where stmt_cycle = ? ) "
               + " and p_seqno >= ? and p_seqno < ? ";
   setString(1,stmtCycle);
   setString(2,hBeginKey);
   setString(3,hStopKey);
   if (pSeqno.length()>0)
      {
       whereStr  = whereStr 
                 + "and  p_seqno = ?  ";
       setString(4 , pSeqno);
      }
    whereStr  = whereStr
               + "group by P_SEQNO,FUND_CODE having sum(end_tran_amt) > 0 "
               + "order by P_SEQNO,FUND_CODE ";
   openCursor();
   while( fetchTable() )  {
      checkHash.put(getValue("p_seqno"),"Y");
   }
   closeCursor();
   showLogMessage("I","","processSkipHash ended checkHash count : "+checkHash.size());
   return;
 }

 public void  loadVmktFundName() throws Exception
 {
    extendField = "vmkt.";
    daoTable    = "vmkt_fund_name";
    selectSQL   = "fund_code,table_name,fund_type,fund_name";
    whereStr    = "";
    int n = loadTable();
    showLogMessage("I","","Load vmkt_fund_name count: ["+n+"]");
    setLoadData(vmktKey);

    return;
 }

 public void  loadMktNfcParm() throws Exception
 {
    extendField = "nfc.";
    daoTable    = "mkt_nfc_parm";
    selectSQL   = "";
    whereStr    = "";
    int n = loadTable();
    showLogMessage("I","","Load mkt_nfc_parm count: ["+n+"]");
    setLoadData(nfcKey);

    return;
 }

 public void  loadPtrFundData() throws Exception
 {
    extendField = "data.";
    daoTable    = "ptr_fund_data";
    selectSQL   = "data_key,"
                + "data_type,"
                + "data_code,"
                + "data_code2 ";
    whereStr    = "WHERE table_name = 'PTR_FUNDP' order by data_key,data_type";
    int n = loadTable();
    showLogMessage("I","","Load ptr_fund_data count: ["+n+"]");
    setLoadData(dataKey);

    return;
 }

 public void  loadMktMchtgpData() throws Exception
 {
    extendField = "mktg.";
    daoTable    = "mkt_mchtgp_data";
    selectSQL   = "data_key,"
                + "data_type,"
                + "data_code,"
                + "data_code2 ";
    whereStr    = "WHERE table_name = 'MKT_MCHT_GP' and data_type = '1' "
                + "order by data_key,data_type ";
    int n = loadTable();
    showLogMessage("I","","Load mkt_mchtgp_data count: ["+n+"]");
    setLoadData(mktgKey);
    return;
 }

public void  loadMktParmData() throws Exception
 {
    extendField = "mktp.";
    daoTable    = "mkt_parm_data";
    selectSQL   = "data_key,"
                + "data_type,"
                + "data_code,"
                + "data_code2";
    whereStr    = "WHERE table_name = 'MKT_LOAN_PARM' and data_type = '2' "
                + "order by data_key,data_type ";
    int n = loadTable();
    showLogMessage("I","","Load mkt_parm_data count: ["+n+"]");
    setLoadData(mktpKey);
    return;
 }

public void  loadMktParmData2() throws Exception
 {
    extendField = "mktp2.";
    daoTable    = "mkt_parm_data";
    selectSQL   = "data_key,"
                + "data_type,"
                + "data_code,"
                + "data_code2 ";
    whereStr    = "WHERE table_name = 'MKT_LOAN_PARM' and data_type in ('3','4') "
                + "order by data_key,data_type ";
    int n = loadTable();
    showLogMessage("I","","Load mkt_parm_data_2 count: ["+n+"]");
    setLoadData(mktpKey2);
    return;
 }

 public void  loadActDebt() throws Exception
 {
    daoTable    = "act_debt";
    extendField = "debt.";
    selectSQL   = "p_seqno,"
                + "reference_no,"
                + "card_no,"
                + "acct_month,"
                + "beg_bal,"
                + "end_bal,"
                + "can_by_fund_bal,"
                + "acct_code,"
                + "interest_date,"
                + "mcht_no,"
                + "interest_rs_date,"
                + "int_rate,"
                + "int_rate_flag,"
                + "new_it_flag,"
                + "mcht_category,"
                + "ucaf,"
                + "pos_entry_mode,"
                + "ec_ind,"
                + "group_code,"
                + "acq_member_id as acq_no,"
                + "d_avail_bal,"
                + "ao_flag,"
                + "substr(lpad(end_bal,13,'0'),1,10) as sort_bal,"
                + "decode(end_bal,0,0,1) as sort_bal_decode,"
                + "item_order_normal || decode(new_it_flag,'Y','0','1') as sort_order_normal,"
                + "post_date as sort_post_date,"
                + "item_class_normal as sort_class_normal,"
                + "rowid as rowid ";

    whereStr    = "WHERE p_seqno in ( select P_SEQNO from act_acno where stmt_cycle = ? ) " 
              //+ "and ( end_bal > 0 or can_by_fund_bal > 0 ) "
                + "and ( acct_month > ? or end_bal > 0 ) "
                + "and decode(curr_code,'','901',curr_code) = '901' "
                + "and p_seqno >= ? and p_seqno < ? ";
/*
    whereStr    = "WHERE p_seqno in ( select P_SEQNO from mkt_cashback_dtl where  p_seqno in ( "
                + "select P_SEQNO from act_acno where stmt_cycle = ? ) and end_tran_amt > 0 ) "
                + "and ( end_bal > 0 or can_by_fund_bal > 0 ) "
                + "and decode(curr_code,'','901',curr_code) = '901' "
                + "order by p_seqno,decode(end_bal,0,0,1) desc,"
                + "item_order_normal || decode(new_it_flag,'Y','0','1'),post_date,item_class_normal ";
*/
    setString(1,stmtCycle);
    setString(2,thisAcctMonth);
    setString(3,hBeginKey);
    setString(4,hStopKey);
    if (pSeqno.length()>0)
       {
        whereStr  = whereStr 
                  + "and  p_seqno = ?  ";
        setString(5 , pSeqno);
       }
    whereStr    = whereStr 
                + "order by p_seqno,decode(end_bal,0,0,1) desc,"
                + "item_order_normal || decode(new_it_flag,'Y','0','1'),post_date,item_class_normal ";

    setSkipHash(checkHash,"p_seqno");  // jack 20200818
    int n = loadTable();
    showLogMessage("I","","Load act_debt count: ["+n+"]");
    debtLoadCnt = getLoadCnt();
    setLoadData(debtKey);
    showUseMemory = true;
    showMemory(debtLoadCnt);
    return;
 }

 public int  loadActAcno() throws Exception
 {
    daoTable    = "act_acno";
    extendField = "acno.";
    selectSQL   = "p_seqno,"
                + "id_p_seqno,"
                + "corp_p_seqno,"
                + "acct_status,"
                + "stmt_cycle,"
                + "no_cancel_debt_flag,"
                + "no_cancel_debt_s_date,"
                + "decode(no_cancel_debt_e_date,'','99991231',no_cancel_debt_e_date) as no_cancel_debt_e_date,"
                + "revolve_int_sign,"
                + "revolve_int_rate,"
                + "revolve_rate_s_month,"
                + "decode(revolve_rate_e_month,'','999912',revolve_rate_e_month) as revolve_rate_e_month,"
                + "batch_rate_s_month,"
                + "batch_int_rate,"
                + "decode(batch_rate_e_month,'','999912',batch_rate_e_month) as batch_rate_e_month,"
                + "group_rate_s_month,"
                + "decode(group_rate_e_month,'','999912',group_rate_e_month) as group_rate_e_month,"
                + "new_cycle_month,"
                + "last_interest_date,"
                + "vip_code,"
                + "int_rate_mcode ";
        whereStr = "WHERE stmt_cycle = ? ";
        setString(1,stmtCycle);
        if (pSeqno.length()>0)
           {
            whereStr  = whereStr 
                      + "and  p_seqno = ?  ";
            setString(2 , pSeqno);
           }

        setSkipHash(checkHash,"p_seqno");  // jack 20200818
        int n =  loadTable();
        showLogMessage("I","","Load act_acno Count: ["+n+"]");
        setLoadData(acnoKey);
        return n;
 }

 public int  loadActAcct() throws Exception
 {
    daoTable    = "act_acct";
    extendField = "acct.";
    selectSQL   = "p_seqno,"
                + "acct_type,"
                + "acct_jrnl_bal,"
                + "end_bal_op,"
                + "temp_unbill_interest,"
                + "min_pay_bal,"
                + "rc_min_pay_bal,"
                + "rc_min_pay_m0,"
                + "ttl_amt_bal,"
                + "last_payment_date,"
                + "last_min_pay_date,"
                + "last_cancel_debt_date ";
    whereStr = "WHERE p_seqno in ( select P_SEQNO from act_acno where stmt_cycle = ? ) ";
    setString(1,stmtCycle);
    if (pSeqno.length()>0)
       {
        whereStr  = whereStr 
                  + "and  p_seqno = ?  ";
        setString(2 , pSeqno);
       }
    setSkipHash(checkHash,"p_seqno");  // jack 20200818
    int n = loadTable();
    showLogMessage("I","","Load act_acct Count: ["+n+"]");
    setLoadData(acctKey);
    return n;
 }

public int  loadActAcctCurr() throws Exception
 {
    daoTable    = "act_acct_curr";
    extendField = "acur.";
    selectSQL   = "p_seqno,"
                + "curr_code,"
                + "acct_jrnl_bal,"
                + "end_bal_op,"
                + "temp_unbill_interest,"
                + "min_pay_bal,"
                + "ttl_amt_bal,"
                + "dc_acct_jrnl_bal,"
                + "dc_ttl_amt_bal,"
                + "dc_min_pay_bal,"
                + "delaypay_ok_flag,"
                + "decode(no_interest_flag,'','X',no_interest_flag) as no_interest_flag,"
                + "decode(no_interest_s_month,'','000000',no_interest_s_month) as no_interest_s_month,"
                + "decode(no_interest_e_month,'','999999',no_interest_e_month) as no_interest_e_month ";
    whereStr = "WHERE p_seqno in ( select P_SEQNO from act_acno where stmt_cycle = ? ) and curr_code = '901' ";
    setString(1,stmtCycle);
    if (pSeqno.length()>0)
       {
        whereStr  = whereStr 
                  + "and  p_seqno = ?  ";
        setString(2 , pSeqno);
       }
    setSkipHash(checkHash,"p_seqno");  // jack 20200818
    int n = loadTable();
    showLogMessage("I","","Load act_acct_curr Count: ["+n+"]");
    setLoadData(acurKey);
    return n;
 }

 public int  loadCrdCard() throws Exception
 {
    daoTable    = "crd_card";
    extendField = "crd.";
  //selectSQL   = "gp_no,"
  //            + "p_seqno,"
    selectSQL   = "p_seqno,"
                + "acno_p_seqno,"
                + "card_no,"
                + "group_code,"
                + "card_type,"
                + "major_card_no,"  //added on 20190506
                + "current_code,"
                + "decode(combo_indicator,'','N',combo_indicator) as combo_indicator,"
                + "activate_flag,"
                + "activate_date,"
                + "reissue_date,"
                + "change_date,"
                + "old_card_no,"
                + "issue_date ";
 // whereStr    = "WHERE stmt_cycle = ? and current_code = '0' order by gp_no";
  //whereStr    = "WHERE stmt_cycle = ? order by gp_no,current_code";
    whereStr    = "WHERE stmt_cycle = ? ";
    setString(1,stmtCycle);
    if (pSeqno.length()>0)
       {
        whereStr  = whereStr 
                  + "and  p_seqno = ?  ";
        setString(2 , pSeqno);
       }
    whereStr  = whereStr 
              + "order by p_seqno,current_code";
    setSkipHash(checkHash,"p_seqno");  // jack 20200818
    int n = loadTable();
    showLogMessage("I","","Load crd_card Count: ["+n+"]");
    setLoadData(crdKey);
    setLoadData(crdKey2);
    return n;
 }

 public int  loadPtrActcode() throws Exception
 {
    daoTable    = "ptr_actcode";
    extendField = "pcod.";
    selectSQL   = "acct_code,"
                + "inter_rate_code,"
                + "part_rev,"
                + "revolve,"
                + "interest_method ";
    whereStr    = "";
    int n = loadTable();
    showLogMessage("I","","Load ptr_acctcode count: ["+n+"]");
    setLoadData(pcodKey);
    return n;
 }

 public int  loadPtrCardType() throws Exception
 {
    daoTable    = "ptr_card_type";
    extendField = "type.";
    selectSQL   = "card_type,card_note";
    whereStr    = "";
    int n = loadTable();
    showLogMessage("I","","Load ptr_card_type count : ["+n+"]");
    setLoadData(typeKey);
    return n;
 }

 public int  loadBilMerchant() throws Exception
 {
    daoTable    = "bil_merchant";
    extendField = "merc.";
    selectSQL   = "mcht_no,"
                + "mp_rate ";
    whereStr    = "";
    int n = loadTable();
    showLogMessage("I","","Load bil_merchant : ["+n+"]");
    setLoadData(mercKey);
    return n;
 }

/***********************************************************************/
 double GetJRNLSeq() throws Exception {
    daoTable = "dual";
    sqlCmd   = "select ecs_jrnlseq.nextval as JRN_SEQ "
             + " from dual ";
    selectTable();
    if ( notFound.equals("Y")) {
         //comcr.err_rtn("GetJRNLSeq() not found!", "", "");
       }
    double seqno = getValueDouble("JRN_SEQ");
    return (seqno);
  }


void insertCycVouchData() throws Exception
{
  daoTable    = "cyc_vouch_data";
  extendField = "vouc.";

  setValue("vouc.create_date"     , sysDate);
  setValue("vouc.create_time"     , sysTime);
  setValue("vouc.p_seqno"         , getValue("cash.p_seqno"));
  setValue("vouc.curr_code"       , getValue("acur.curr_code"));
  setValue("vouc.acct_type"       , getValue("acct.acct_type"));
  setValue("vouc.business_date"   , getValue("busi.business_date"));
  setValue("vouc.payment_type"    , getValue("cash.fund_code").substring(0,4));
  setValue("vouc.reference_seq"   , getValue("debt.reference_no",di));
//setValue("vouc.vouch_data_type" , "4");
  setValue("vouc.vouch_data_type" , hCycVouchDataType);
  setValue("vouc.acct_code"       , getValue("debt.acct_code",di));

//setValueDouble("vouc.vouch_amt"   , getValueDouble("jrnl.dc_transaction_amt"));
//setValueDouble("vouc.d_vouch_amt" , getValueDouble("jrnl.dc_transaction_amt"));
  setValueDouble("vouc.vouch_amt"   , hCycVouchAmt);
  setValueDouble("vouc.d_vouch_amt" , hCycVouchAmt);

  setValue("vouc.proc_flag"      , "N");
  setValue("vouc.src_pgm"        , javaProgram);
  setValue("vouc.mod_pgm"        , javaProgram);
  setValue("vouc.mod_time"       , sysDate+sysTime);

  insertTable();
  return;
}


 void logs(String logData) throws Exception {

  //if ( debugFlag.equals("Y") )
  //if ( getValue("cash.p_seqno").equals(testPSeqno) )
    if ( getValue("cash.p_seqno").equals(pSeqno) )
       { log(getValue("cash.p_seqno")+" - "+logData); }

    return;
  }

}  // End of class ActE010
