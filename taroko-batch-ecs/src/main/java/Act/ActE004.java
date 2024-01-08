/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/10/25  V1.00.01  Jack Liao  ActE004                                    *
* 110/08/30  V1.00.10  Simon      Bug fixed                                  *
* 111/04/14  V1.01.02  Allen Ho   Mantis 9333                                *
* 111-10-14  V1.00.03  Machao     sync from mega & updated for project coding standard *
* 112-06-01  V1.00.04  Simon      add act_acct_curr.last_pay_date            *
* 112-08-14  V1.00.05  Simon      remove act_acag.seq_no in insertActAcag()  *
* 112-09-27  V1.00.06  Simon      1.remove processing acno.no_cancel_debt_flag="Y"*
*                                 2.新增寫入 act_vouch_data.tx_acct_month(交易帳務年月)*
*                                 3.更新以 busi.business_date 寫入 cyc_pyaj.crt_date，而不以 sysDate寫入*
* 112-11-23  V1.00.07  Simon      分批執行控制                                *
******************************************************************************/
package Act;

import com.*;
import java.sql.*;
import java.util.*;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

@SuppressWarnings("unchecked")
public class ActE004 extends AccessDAO
{
 String   debugFlag = "N";
 String   testPSeqno ="0001809629";
 String   pSeqno ="";

 private final  String PROGNAME = "卡人繳款銷帳處理程式  112-11-23  V1.00.07";

 String   hJrnlJrnlSeqno="",wsRealPay="",wsDumyFlag="",
          wsPasingFlag="",wsDebtFlag="",hTempBackDate="";

 String   seqnoRange="",busiDate="";
 int      hBatchSeq = 0;
 String   hProcFlag="",hPtclRowid="",hBeginKey="",hStopKey="";
 String   wsInProcFlag = "",hProcReset="";
 String   hProcSDate="",hProcSTime="",hProcEDate="",hProcETime="";
 boolean  hAppActive=false;

 long     hIntrEnqSeqno=0, collectionFlag =0,hJrnlOrderSeq=0;

 double   dcOrgOverAmt=0,orgOverAmt=0,
          dcPayOverAmt=0,payOverAmt=0;

 double   totInterestAmt=0,totDcInterestAmt=0,totWaiveIntrAmt=0,totDcWaiveIntrAmt=0,
          totRealWaiveAmt=0, totDcRealWaiveAmt = 0,nonRealWaiveAmt=0, nonDcRealWaiveAmt= 0,
          totRealDebitAmt= 0,totDcRealDebitAmt = 0, totRealPayAmt=0, totDcRealPayAmt= 0,
          nonRealPayAmt  =0, nonDcRealPayAmt   = 0,intrOrgCaptial=0;

 double   fitRevolvingRate1=0;
 int      minOverFlag=0,actCurrHstCnt=0,actAcctstFlag=0,debtLoadCnt=0;

 double[] tempRevolvingInterest = {0,0,0,0,0,0,0,0,0,0};

 String[] keyData={"","","",""};
 int      srtLength  = 0;
 int      di=0,orgOrder= 0;

 CommFunction   comm  = new CommFunction();
 CommCrd        comc  = new CommCrd();
 CommRoutine    comr  = null;
 CommCrdRoutine comcr = null;

 //SortObject   srt  = new SortObject(this);

 String   debtKey="debt.p_seqno,debt.curr_code";
 String   acnoKey="acno.p_seqno";
 String   acctKey="acct.p_seqno";
 String   acurKey="acur.p_seqno,acur.curr_code";
 String   acurKey2="acur.p_seqno";
 String   pcglKey="pcgl.curr_code";
 String   wdayKey="wday.stmt_cycle";
 String   pcodKey="pcod.acct_code";
 String   mercKey="merc.mcht_no";

 String[] sortField_1 = {"debt.sort_order_normal","debt.sort_code_type,DESC","debt.sort_post_date","debt.sort_class_normal"};
 String[] sortField_2 = {"debt.sort_order_back_date","debt.sort_code_type,DESC","debt.sort_post_date","debt.sort_class_back_date"};
 String[] sortField_3 = {"debt.sort_order_refund","debt.sort_code_type","debt.sort_post_date","debt.sort_class_refund"};

 String[] sortField_4 = {"debt.acct_month"};


// ************************************************************************
 public static void main(String[] args) throws Exception
 {
    ActE004 proc = new ActE004();
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

    if (args.length > 2)
       {
        showLogMessage("I","","請輸入參數:");
        showLogMessage("I","","PARM 1 : [BATCH_SEQ]/[P_SEQNO]");
        showLogMessage("I","","PARM 2 : ['reset']");
        return(0);
       }

    if ( args.length == 1 ) {
    	if (args[0].length()>0 && args[0].length()<=2) {
        hBatchSeq = comcr.str2int(args[0]);
    	} else if (args[0].length()==10) {
        pSeqno  = args[0];
      }
    } else if ( args.length == 2 ) {
    	if (args[0].length()>0 && args[0].length()<=2) {
        hBatchSeq = comcr.str2int(args[0]);
      }

    	if (args[1].equalsIgnoreCase("reset")) {
        hProcReset  = "Y";
      } else {
        showLogMessage("I","","請輸入參數:");
        showLogMessage("I","","PARM 1 : [BATCH_SEQ]/[P_SEQNO]");
        showLogMessage("I","","PARM 2 : ['reset']");
        return 0;
      }
    }
      
    selectPtrBusinday();

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
    
//  select_ptr_actgeneral();
    loadActDebt();

    loadActAcno();
    loadActAcct();
    loadActAcctCurr();
    loadPtrCurrGeneral();
    loadPtrWorkday();
    loadPtrActcode();
    loadBilMerchant();

    // 處理銷帳及計息作業
    processActDebtCancel();

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

// ******************* 處理銷帳及計息作業 *************************************
 public void  processActDebtCancel() throws Exception
 {
   daoTable    = "act_debt_cancel a,ptr_actgeneral_n b";
   fetchExtend = "adcl.";
   selectSQL   = "a.batch_no,"
               + "a.serial_no,"
               + "a.p_seqno,"
               + "decode(a.curr_code,'','901',a.curr_code) as curr_code,"
               + "a.acct_type,"
               + "a.pay_card_no,"
               + "a.pay_amt,"
               + "decode(decode(a.curr_code,'','901',a.curr_code),'901',a.pay_amt,a.dc_pay_amt) as dc_pay_amt,"
               + "decode(substr(a.batch_no,9,4),'9999',?,a.pay_date) as pay_date,"
               + "a.payment_type,"
               + "a.debit_item,"
               + "a.debt_key,"
               + "a.rowid as rowid,"
               + "b.revolving_interest1,"
               + "b.revolving_interest2,"
               + "b.revolving_interest3,"
               + "b.revolving_interest4,"
               + "b.revolving_interest5,"
               + "b.revolving_interest6,"
               + "b.rc_max_rate";
   whereStr    = "WHERE substr(a.batch_no,9,4)  != '9008' "
               + "and   a.acct_type = b.acct_type "  
               + "AND   decode(a.process_flag,'','N',a.process_flag) != 'Y' "
               + "and   a.p_seqno >= ? and a.p_seqno < ? ";
   setString(1,getValue("busi.business_date"));
   setString(2,hBeginKey);
   setString(3,hStopKey);
   if (pSeqno.length()!=0)
      {
       whereStr  = whereStr    
                 + "and a.p_seqno = ?  ";
       setString(4 , pSeqno);
      }
   whereStr    = whereStr    
               + "ORDER BY  a.acct_type, a.p_seqno,a.pay_date,a.curr_code";

   openCursor();
   while( fetchTable() )
    {
       processDisplay(1000);

       tempRevolvingInterest[1] = getValueDouble("adcl.revolving_interest1");
       tempRevolvingInterest[2] = getValueDouble("adcl.revolving_interest2");
       tempRevolvingInterest[3] = getValueDouble("adcl.revolving_interest3");
       tempRevolvingInterest[4] = getValueDouble("adcl.revolving_interest4");
       tempRevolvingInterest[5] = getValueDouble("adcl.revolving_interest5");
       tempRevolvingInterest[6] = getValueDouble("adcl.revolving_interest6");

       //if ( !getValue("adcl.p_seqno").equals(test_p_seqno) && debugFlag.equals("Y"))  // ttt
       //   { continue; }

       if ( !getValue("adcl.batch_no").substring(8,12).equals("9999") && getValueDouble("adcl.dc_pay_amt") == 0 )
          {
            updateActDebtCancel();
            continue;
          }

        hJrnlJrnlSeqno = String.format("%012.0f", GetJRNLSeq());

        if ( hIntrEnqSeqno > 99900 )
           { hIntrEnqSeqno = collectionFlag = 0; }

        hJrnlOrderSeq = 1;
        wsRealPay      = wsDumyFlag = "N";
        dcPayOverAmt  = payOverAmt = 0;

        String subBatchNo = getValue("adcl.batch_no").substring(8, 12);
        if ( !subBatchNo.equals("9005") &&
             !subBatchNo.equals("9007") &&
             !subBatchNo.equals("9008") &&
             !subBatchNo.equals("9009") &&
             !subBatchNo.equals("9999") )
           { wsRealPay = "Y"; }

        setValue("acno.p_seqno",getValue("adcl.p_seqno"));
        if ( getLoadData(acnoKey) == 0 ) // 讀  ACT_ACNO
           { showLogMessage("E","","GET ACT_ACNO ERROR "+getValue("adcl.p_seqno")); continue; }

        setValue("acct.p_seqno",getValue("adcl.p_seqno"));
        if ( getLoadData(acctKey) == 0 ) // 讀  ACT_ACCT
           { showLogMessage("E","","GET ACT_ACCT ERROR "+getValue("adcl.p_seqno")); continue; }

        setValue("acur.p_seqno",getValue("adcl.p_seqno"));
        setValue("acur.curr_code",getValue("adcl.curr_code"));
        if ( getLoadData(acurKey) == 0 ) // 讀  ACT_ACCT_CURR
           { showLogMessage("E","","GET ACT_ACCT_CURR ERROR "+getValue("adcl.p_seqno") +" "+getValue("adcl.curr_code")); continue; }

        setValue("pcgl.curr_code",getValue("adcl.curr_code"));
        if ( getLoadData(pcglKey) == 0 ) // 讀  ptr_curr_general
           { showLogMessage("E","","GET ptr_curr_general ERROR "+getValue("adcl.curr_code")); continue; }

        setValue("wday.stmt_cycle",getValue("acno.stmt_cycle"));
        if ( getLoadData(wdayKey) == 0 ) // 讀  ptr_workday
           { showLogMessage("E","","GET ptr_workday ERROR "+getValue("acno.stmt_cycle")); continue; }

/***
        if ( getValue("acno.no_cancel_debt_flag").equals("Y") &&
             getValue("busi.business_date").substring(0,6).compareTo(getValue("acno.no_cancel_debt_s_date").substring(0,6)) >= 0 &&
             getValue("busi.business_date").substring(0,6).compareTo(getValue("acno.no_cancel_debt_e_date").substring(0,6)) <= 0 &&
             getValue("acno.acct_status").equals("3") )
          {
             logs("ttt-process_collection ");
             processCollection();  // 處理催收銷帳
             continue;
          }
***/
         // 無繳費及溢繳款不處理
        if ( getValueDouble("acur.dc_end_bal_op") == 0 && getValueDouble("acur.dc_end_bal_lk") == 0 && getValueDouble("adcl.dc_pay_amt") == 0 )  {
             logs("ttt-無繳費及溢繳款不處理 ");
             updateActDebtCancel();
             continue;
           }

        dcOrgOverAmt  = getValueDouble("acur.dc_end_bal_op") + getValueDouble("acur.dc_end_bal_lk");
        dcPayOverAmt  = getValueDouble("acur.dc_end_bal_op") + getValueDouble("acur.dc_end_bal_lk")
                                                                + getValueDouble("adcl.dc_pay_amt");
                                                                
        dcOrgOverAmt  =  convAmtDp2r(dcOrgOverAmt);
        dcPayOverAmt  =  convAmtDp2r(dcPayOverAmt);
                                                            
        orgOverAmt     =  getValueDouble("acur.end_bal_op")    + getValueDouble("acur.end_bal_lk");
        payOverAmt     =  getValueDouble("acur.end_bal_op")    + getValueDouble("acur.end_bal_lk")
                                                                + getValueDouble("adcl.pay_amt");

        logs("ttt-############### START "+getValue("adcl.p_seqno"));

        logs("ttt-before-acct acct_jrnl_bal "+getValue("acct.acct_jrnl_bal"));
        logs("ttt-before-acct "+getValue("acct.end_bal_op"));
        logs("ttt-before-acct "+getValue("acct.end_bal_lk"));
        logs("ttt-before-acct "+getValue("acct.temp_unbill_interest"));
        logs("ttt-before-acct "+getValue("acct.min_pay_bal"));
        logs("ttt-before-acct "+getValue("acct.rc_min_pay_bal"));
        logs("ttt-before-acct "+getValue("acct.rc_min_pay_m0"));
        logs("ttt-before-acct "+getValue("acct.autopay_bal"));
        logs("ttt-before-acct "+getValue("acct.pay_by_stage_bal"));
        logs("ttt-before-acct "+getValue("acct.pay_amt"));
        logs("ttt-before-acct "+getValue("acct.pay_cnt"));
        logs("ttt-before-acct "+getValue("acct.adjust_dr_amt"));
        logs("ttt-before-acct "+getValue("acct.adjust_dr_cnt"));
        logs("ttt-before-acct "+getValue("acct.adjust_cr_amt"));
        logs("ttt-before-acct "+getValue("acct.adjust_cr_cnt"));
        logs("ttt-before-acct "+getValue("acct.ttl_amt_bal"));
        logs("ttt-before-acct "+getValue("acct.adi_end_bal"));
        logs("ttt-before-acct "+getValue("acct.last_payment_date"));
        logs("ttt-before-acct "+getValue("acct.last_min_pay_date"));
        logs("ttt-before-acct "+getValue("acct.last_cancel_debt_date"));

        logs("ttt- dc_org_over_amt "+dcOrgOverAmt);
        logs("ttt- dc_pay_over_amt "+dcPayOverAmt);
        logs("ttt- org_over_amt "+orgOverAmt);
        logs("ttt- pay_over_amt "+payOverAmt);

        logs("ttt- adcl.curr_code "+getValue("adcl.curr_code"));
        logs("ttt- acno.stmt_cycle "+getValue("acno.stmt_cycle"));
        logs("ttt- acur.end_bal_op "+getValue("acur.end_bal_op"));
        logs("ttt- acur.dc_end_bal_op "+getValue("acur.dc_end_bal_op"));
        logs("ttt- acur.end_bal_lk "+getValue("acur.end_bal_lk"));
        logs("ttt- acur.dc_end_bal_lk "+getValue("acur.dc_end_bal_lk"));
        logs("ttt- acct.min_pay_bal "+getValue("acct.min_pay_bal"));

        processDebtData();
        
        logs("ttt-############### ENDED "+getValue("adcl.p_seqno"));

        if ( !getValue("adcl.batch_no").substring(8,12).equals("9999") )
           {
             setValue("jrnl.tran_type", getValue("adcl.payment_type"));
             setValue("jrnl.transaction_amt",getValue("adcl.pay_amt"));
             setValue("jrnl.dc_transaction_amt",getValue("adcl.dc_pay_amt"));
             insertActJrnl("1");
          }

        insertCycPyaj("1");
        lastCurrData();        // 處理 ACT_ACCT_CURR 相關欄位
        updateActAcctCurr();  // 更新 ACT_ACCT_CURR 相關欄位
        lastAcctData();        // 處理 ACT_ACCT 相關欄位
        updateActAcct();       // 更新 ACT_ACCT 相關欄位
        updateActAcno("B");
        updateActDebtCancel();
    } // end of while fetch

   closeCursor();
   return;
 }

/*****************************************************************************/
// 處理催收銷帳，已點掉，不會走到這段
public void processCollection() throws Exception
{
   setValue("avda.vouch_amt",getValue("adcl.dc_pay_amt"));
   setValue("avda.d_vouch_amt",getValue("adcl.dc_pay_amt"));
   setValue("avda.acct_code","");
 //setValue("avda.reference_no","");
   setValue("avda.reference_seq","");

   insertActVouchData("2");

   if ( wsRealPay.equals("Y")) {
      if ( getValue("adcl.pay_date").compareTo(getValue("acno.last_pay_date")) >= 0 ) {
         setValue("acno.last_pay_amt",getValue("adcl.pay_amt"));
         setValue("acno.last_pay_date",getValue("adcl.pay_date"));
         updateActAcno("A");
       }
   }

   updateActDebtCancel();

   setValue("jrnl.transaction_amt",getValue("adcl.pay_amt"));
   setValue("jrnl.dc_transaction_amt",getValue("adcl.dc_pay_amt"));
   collectionFlag=1;
   setValue("jrnl.tran_type",getValue("adcl.payment_type"));
   insertActJrnl("1");
   return;
} // process_collection()

public void processDebtData() throws Exception
{
  /************************************************************/
  /* 沖銷欠款  (1)欠款類型 (2)銷帳順序 來依順序沖銷           */
  /* (1)欠款類型 : ws_parsing_flag                            */
  /*               0,1 2 已 billed資料 3,unbill 資料          */
  /*  0: 費用或利息,或'IT' mp_rate 100%                       */
  /*     "RI","PN","AI","CI" debt_flag='2' 不處理             */
  /*  1: "RI","PN","AI","CI" debt_flag='2' 不處理             */
  /*  2: 只處理 bake value                                    */
  /*  3: 沖銷 unbill 欠款,                                    */
  /************************************************************/
  /* (2)銷帳順序 : ws_debt_flag 銷帳順序                      */
  /*  1.normal  2.back date value  3.refund  4.bonus fund     */
  /************************************************************/

  totInterestAmt=0;
  totDcInterestAmt=0;
  totWaiveIntrAmt=0;
  totDcWaiveIntrAmt=0;
  totRealWaiveAmt=0;
  totDcRealWaiveAmt= 0;
  nonRealWaiveAmt=0;
  nonDcRealWaiveAmt=0;
  totRealDebitAmt=0;
  totDcRealDebitAmt= 0;
  totRealPayAmt=0;
  totDcRealPayAmt= 0;
  nonRealPayAmt  =0;
  nonDcRealPayAmt= 0;
  intrOrgCaptial=0;
  fitRevolvingRate1=0;
  minOverFlag=0;
  di =0;

   /************** 變更 cycle 判斷 *****/
  String wk_this_close_date = getValue("wday.this_close_date");
  if ( getValue("acno.new_cycle_month").length() > 0  && getValue("wday.next_acct_month").equals(getValue("acno.new_cycle_month")) )
     {
       if ( getValue("acur.delaypay_ok_flag").equals("Y") )
          { wk_this_close_date = getValue("acno.last_interest_date");  }
     }
   wsPasingFlag = "0";
   wsDebtFlag    = "1";

   hTempBackDate="";

   logs("ttt-this_close_date "+wk_this_close_date+" adcl.pay_date "+getValue("adcl.pay_date") );

   if ( wk_this_close_date.compareTo(getValue("adcl.pay_date")) >= 0 )
      {
        wsDebtFlag = "2";
        if ( getValue("adcl.pay_date").compareTo(getValue("wday.ll_close_date")) < 0 )
           { hTempBackDate = getValue("adcl.pay_date"); }
        else
           {
             if ( getValue("adcl.pay_date").compareTo( getValue("wday.last_close_date")) < 0 )
                { hTempBackDate = getValue("wday.ll_close_date"); }
             else
                { hTempBackDate = getValue("wday.last_close_date"); }
          }
        // BACK DATE 超商繳款 但 PAYMENT 遲繳至銀行
        if ( wk_this_close_date.compareTo(getValue("adcl.pay_date")) >= 0 )
           { processActCurrHst(); }
      }
   else
   if ( getValue("adcl.payment_type").equals("REFU") )  {
        wsDebtFlag = "3";
      }

   selectActDebt1();

   wsPasingFlag = "1";
   selectActDebt1();

   wsPasingFlag = "2";
   if ( wsDebtFlag.equals("2") ) {
        selectActDebt1();
      }

   totWaiveIntrAmt    = comcr.commCurrAmt("901",totWaiveIntrAmt,0);
   totDcWaiveIntrAmt = comcr.commCurrAmt(getValue("adcl.curr_code"),totDcWaiveIntrAmt,0);

   logs("ttt- tot_waive_intr_amt "+totWaiveIntrAmt);
   logs("ttt- tot_dc_waive_intr_amt "+totDcWaiveIntrAmt);

   if ( (totDcWaiveIntrAmt+totWaiveIntrAmt) != 0 )
      {
        selectActDebt2();

        if ( (totRealWaiveAmt+totDcRealWaiveAmt) > 0 )
           {
             setValue("jrnl.tran_type", "BACK");
             setValue("jrnl.transaction_amt", ""+totRealWaiveAmt);
             setValue("jrnl.dc_transaction_amt",""+totDcRealWaiveAmt);
             insertActJrnl("1");
             insertCycPyaj("2");
          }
      }

   wsPasingFlag = "3";
   if ( wsDebtFlag.equals("2") )
      {
        if ( getValue("adcl.payment_type").equals("REFU") )
           { wsDebtFlag = "3"; }
        else
           { wsDebtFlag = "1"; }
      }

   selectActDebt1();
   return;
}  // process_debt_data()

// BACK DATE 超商繳款 但 PAYMENT 遲繳至銀行
public void processActCurrHst() throws Exception
 {
   logs("@@@@@@@@@@ process_act_curr_hst start");
   int overFlag=0;
   if ( selectActCurrHst1() == 0 )
      {
        if ( !getValue("adcl.curr_code").equals("901")  )
           { logs("@@@@@@@@@@ h1"); return; }
        if ( selectActAcctHst() == 0 )
           { logs("@@@@@@@@@@ h1"); return; }
      }
   logs("process_act_curr_hst "+actCurrHstCnt);

   for ( int i=0; i<actCurrHstCnt; i++ )
       {
        setValue("acht.acct_month"    , getValue("acht.acct_month",i));
        setValue("deba.acct_month"    , getValue("acht.deba_month",i));
        setValue("acht.min_pay_bal"   , getValue("acht.min_pay_bal",i));
        setValue("acht.waive_ttl_bal" , getValue("acht.waive_ttl_bal",i));
        setValue("acht.rowid"         , getValue("acht.rowid",i));

        overFlag=0;
        minOverFlag=0;

       if ( getValue("acht.acct_month",i).compareTo(getValue("wday.ll_acct_month")) < 0 )
          {
            if ( getValueDouble("acht.waive_ttl_bal",i) <= getValueDouble("adcl.dc_pay_amt") )
               { overFlag = 1; }
          }
       else if ( getValue("acht.acct_month",i).equals(getValue("wday.ll_acct_month")) )
          {
            if ( getValue("adcl.pay_date").compareTo(getValue("wday.ll_delaypay_date")) <= 0 )
               {
                 if ( getValueDouble("acht.waive_ttl_bal",i) <= getValueDouble("adcl.dc_pay_amt") )
                    { overFlag=1; }
                 if ( getValueDouble("acht.min_pay_bal",i) <= getValueDouble("adcl.dc_pay_amt") )
                    { minOverFlag=1; }
               }
          }
       else if ( getValue("acht.acct_month",i).equals(getValue("wday.last_acct_month")) )
          {
            if ( getValue("adcl.pay_date").compareTo(getValue("wday.last_delaypay_date")) <= 0 )
               {
                 if ( getValueDouble("acht.waive_ttl_bal",i) <= getValueDouble("adcl.dc_pay_amt") )
                    { overFlag = 1; }
                 if ( getValueDouble("acht.min_pay_bal",i)   <= getValueDouble("adcl.dc_pay_amt") )
                    { minOverFlag = 1; }
               }
         }

     if ( actAcctstFlag == 0 )
        { updateActCurrHst(); }
     else
        { updateActAcctHst(); }

     if ( overFlag == 1 )
        {
          selectActIntrA();
          selectActDebtA();

          if ( getValueDouble("inta.dc_inte_d_amt") > getValueDouble("deba.dc_d_avail_bal") )
             {
               setValue("inta.inte_d_amt", getValue("deba.d_avail_bal"));
               setValue("inta.dc_inte_d_amt", getValue("deba.dc_d_avail_bal"));
             }

         if  ( getValueDouble("inta.dc_inte_d_amt") == 0 )
             { setValue("inta.inte_d_amt","0"); }

         totWaiveIntrAmt    = totWaiveIntrAmt    + getValueDouble("inta.inte_d_amt");
         totDcWaiveIntrAmt = totDcWaiveIntrAmt + getValueDouble("inta.dc_inte_d_amt");
         logs("@@@@@@@@@@ h3 "+getValueDouble("inta.dc_inte_d_amt"));
         if ( getValueDouble("inta.dc_inte_d_amt") > 0 )
            {
              updateActIntrA();
              setValue("intr.intr_org_captial",getValue("adcl.pay_amt"));
              setValue("intr.dc_intr_org_captial",getValue("adcl.dc_pay_amt"));
              setValue("intr.intr_s_date",getValue("acht.acct_month",i));
              setValue("intr.intr_e_date",getValue("acht.acct_month",i));
              setValue("intr.interest_amt",getValue("inta.inte_d_amt"));
              setValue("intr.dc_interest_amt",getValue("inta.dc_inte_d_amt"));
              logs("@@@@@@@@@@ h4 ");
              insertActIntr("1");
            }
        }
    }

   totWaiveIntrAmt    = comcr.commCurrAmt("901",totWaiveIntrAmt,0);
   totDcWaiveIntrAmt = comcr.commCurrAmt(getValue("adcl.curr_code"),totDcWaiveIntrAmt,0);
   return;
 }  // process_act_curr_hst()

public void selectActDebt1() throws Exception
 {
     if ( dcPayOverAmt <= 0 )
        { return; }

     logs("ttt- ws_debt_flag : "+wsDebtFlag+", ws_parsing_flag : "+wsPasingFlag);

     setValue("debt.p_seqno",getValue("adcl.p_seqno"));
     setValue("debt.curr_code",getValue("adcl.curr_code"));
     int debtCnt = getLoadData(debtKey);
     logs("ttt (1) debtCnt- "+debtCnt+" "+getValue("adcl.p_seqno")+" "+getValue("adcl.curr_code"));
     
     SortObject   srt  = new SortObject(this);

     if ( wsDebtFlag.equals("1") )
        { srt.sortLoadData(sortField_1,debtCnt);  }
     else
     if ( wsDebtFlag.equals("2") )
        { srt.sortLoadData(sortField_2,debtCnt);  }
     else
     if ( wsDebtFlag.equals("3") )
        { srt.sortLoadData(sortField_3,debtCnt);  }
/*
     for ( int i=0; i<debtCnt; i++ ) {

           di = srt.getSortIndex(i); // GET INDEX POINT AFTER SORT
           logs("ttt sssssssssss sort_order_normal "+getValue("debt.p_seqno",di)+ " "+getValue("debt.sort_order_normal",di)+" "+getValue("debt.sort_code_type",di)+" "+getValue("debt.sort_post_date",di)+" "+getValue("debt.sort_class_normal",di)+" di "+di+" "+getValue("debt.reference_no",di));
        }
*/
     for ( int i=0; i<debtCnt; i++ ) {

           di = srt.getSortIndex(i); // GET INDEX POINT AFTER SORT
           orgOrder = i;
           
           logs("ttt ***** debt- "+i+" "+getValue("debt.p_seqno",di)+" "+getValue("debt.reference_no",di));
           logs("ttt ***** adcl- "+getValue("adcl.p_seqno"));

           if ( getValue("debt.acct_code",di).equals("DP") || getValueDouble("debt.dc_end_bal",di) <= 0 )
              { logs("ttt-skip (1) "+getValue("adcl.p_seqno")); continue; }

           /*********************** 不處理判斷 BEGIN ********************/
           /* 1. 排除不沖銷   */
           if ( ( wsDebtFlag.equals("1") && getValue("debt.item_order_normal",di).equals("00") )    ||
                ( wsDebtFlag.equals("2") && getValue("debt.item_order_back_date",di).equals("00") ) ||
                ( wsDebtFlag.equals("3") && getValue("debt.item_order_refund",di).equals("00") ) )
              { logs("ttt-skip (2) "+getValue("adcl.p_seqno")); continue; }

           /* 2. 未 bill 欠款列為第四順序沖銷  */
           if ( !wsPasingFlag.equals("3") && getValue("debt.acct_month",di).equals(getValue("wday.next_acct_month")) )
              { logs("ttt- skip (3) "+getValue("adcl.p_seqno")); continue; }

           /* 3. "RI","PN","AI","CI" 的 back_date 列為第三順序沖銷   */
           if ( (wsPasingFlag.equals("0") || wsPasingFlag.equals("1")) && wsDebtFlag.equals("2") ) {
               if ( Arrays.asList("RI","PN","AI","CI").contains(getValue("debt.acct_code",di)) )
                  { logs("ttt-skip (4) "+getValue("adcl.p_seqno")); continue; }
              }

           /* 4. 第一、二順序排除不符 back_date的沖銷  */
           if ( (wsPasingFlag.equals("0") || wsPasingFlag.equals("1")) && wsDebtFlag.equals("2") ) {
               if ( getValue("debt.post_date",di).compareTo(hTempBackDate) > 0 )
                  { logs("ttt- skip (5) "+getValue("adcl.p_seqno")); continue; }
              }

           setValue("pcod.acct_code",getValue("debt.acct_code",di));
           getLoadData(pcodKey);
           logs("ttt- part_rev  "+getValue("pcod.part_rev") );
           setValue("merc.mp_rate","0");
           setValue("merc.trans_flag","");
           if( getValue("debt.acct_code",di).equals("IT") ) //分期付款
             {
               setValue("merc.mcht_no",getValue("debt.merchant_no",di));
               getLoadData(mercKey);
               if ( getValue("debt.new_it_flag",di).equals("Y") )
                  { setValue("merc.mp_rate","100"); }
               logs("ttt- mp_rate  "+getValue("merc.mp_rate") );
             }

           /* 5. 排除不符第一順序的沖銷  */
           if ( wsPasingFlag.equals("0") ) {
                if ( getValueDouble("merc.mp_rate") != 100 )
                   {
                     if ( !getValue("pcod.revolve").equals("Y") )
                        { logs("ttt-skip (6) "+getValue("adcl.p_seqno")); continue; }
                     if ( getValue("debt.acct_code",di).equals("AI") )
                        { logs("ttt-skip (7) "+getValue("adcl.p_seqno")); continue; }
                   }
              }

           logs("ttt debt.reference_no   : "+getValue("debt.reference_no",di));
           logs("ttt acct.code : "+ getValue("debt.acct_code",di));
           logs("ttt debt.dc_end_bal   : "+ getValue("debt.dc_end_bal",di));
           logs("ttt dc_pay_over_amt "+dcPayOverAmt);
           logs("ttt inter_rate_code  "+getValue("pcod.inter_rate_code") );
           logs("ttt interest_method  "+getValue("pcod.interest_method") );

           /*********************** 不處理判斷 ENDED ********************/

           // DEBT 完全沖銷
           if ( dcPayOverAmt >= getValueDouble("debt.dc_end_bal",di) )
              {
                logs("ttt-DEBT 完全沖銷 ");

                setValue("intr.intr_org_captial",getValue("debt.end_bal",di));
                setValue("intr.dc_intr_org_captial",getValue("debt.dc_end_bal",di));
                dcPayOverAmt = dcPayOverAmt - getValueDouble("debt.dc_end_bal",di);
                dcPayOverAmt =  convAmtDp2r(dcPayOverAmt);

                payOverAmt    = payOverAmt    - getValueDouble("debt.end_bal",di);
                if ( payOverAmt < 0  || dcPayOverAmt == 0 )
                   { payOverAmt = 0; }
                setValue("debt.end_bal","0",di);
                setValue("debt.dc_end_bal","0",di);
              }
           else // 付款完全沖銷 ,計算 DEBT 沖銷餘額
              {
                logs("ttt-付款完全沖銷 ");

                if ( !getValue("pcod.part_rev").equals("Y") )
                   { continue; }

                double  dcEndBal = getValueDouble("debt.dc_end_bal",di) - dcPayOverAmt;
                dcEndBal =  convAmtDp2r(dcEndBal);
                setValue("debt.dc_end_bal",""+dcEndBal,di);
                setValue("intr.dc_intr_org_captial",""+dcPayOverAmt);

                double cvtBal = comcr.commCurrAmt("901",getValueDouble("debt.dc_end_bal",di) *
                                                          (getValueDouble("debt.beg_bal",di) / getValueDouble("debt.dc_beg_bal",di)),0);

                intrOrgCaptial  = getValueDouble("debt.end_bal",di) - cvtBal;
                if ( getValue("adcl.curr_code").equals("901") )
                   { intrOrgCaptial = getValueDouble("intr.dc_intr_org_captial"); }
                if ( getValueDouble("intr.dc_intr_org_captial") == 0 )
                   { intrOrgCaptial = 0; }
                setValue("intr.intr_org_captial",""+intrOrgCaptial);

                double endBal = comcr.commCurrAmt("901",getValueDouble("debt.dc_end_bal",di) *
                                                          (getValueDouble("debt.beg_bal",di) / getValueDouble("debt.dc_beg_bal",di)),0);
                setValue("debt.end_bal",""+endBal,di);
                logs("ttt-7 debt.end_bal "+endBal);
                if ( getValue("adcl.curr_code").equals("901") )
                   { setValue("debt.end_bal",getValue("debt.dc_end_bal",di),di); }

                if ( getValueDouble("debt.dc_end_bal",di) == 0 )
                   { setValue("debt.end_bal","0",di); }

                dcPayOverAmt = 0;
                payOverAmt    = 0;
                // 付款沖銷完成
              }

           double cvtDcOrgCaptial = convAmtDp2r(getValueDouble("intr.dc_intr_org_captial"));
           double cvtOrgCaptial    = convAmtDp2r(getValueDouble("intr.intr_org_captial"));

           totDcRealPayAmt +=  cvtDcOrgCaptial;
           totRealPayAmt    +=  cvtOrgCaptial;
           if ( getValue("debt.acct_month",di).equals(getValue("wday.next_acct_month")) )
              {
                nonDcRealPayAmt += cvtDcOrgCaptial;
                nonRealPayAmt    += cvtOrgCaptial;
              }

           setValue("intr.dc_intr_org_captial",""+cvtDcOrgCaptial);
           setValue("intr.intr_org_captial",""+cvtOrgCaptial);

           /*********************** COMBO 預借現金需通知 CCAS ************/
            if ( getValue("debt.acct_code",di).equals("CA") && getValue("debt.bill_type",di).equals("COBO") )
               { insertActComboCcs(); }

           /*********************** 會計分錄資料獨力程式處理 ************/
           setValue("avda.vouch_amt",""+cvtDcOrgCaptial);
           setValue("avda.d_vouch_amt",""+cvtOrgCaptial);
           setValue("avda.acct_code",getValue("debt.acct_code",di));
         //setValue("avda.reference_no",getValue("debt.reference_no",di));
           setValue("avda.reference_seq",getValue("debt.reference_no",di));
           setValue("avda.tx_acct_month",getValue("debt.acct_month",di));
           insertActVouchData("1");

           /*********************** 符合計息之欠款 **********************/

           logs("@@@@@@@@@@ -0 "+wsPasingFlag);
        if (pSeqno.length()>0)
           {
            showLogMessage("I","","Mantis 9333 set ws_parsing_flag = 1; ");
            showLogMessage("I","","       interest_method   :[" + getValue("pcod.interest_method") + "]");
            showLogMessage("I","","       trans_flag        :[" + getValue("merc.trans_flag") + "]");
            showLogMessage("I","","       ws_parsing_flag   :[" + wsPasingFlag + "]");
            wsPasingFlag = "1";
            showLogMessage("I","","       ws_debt_flag      :[" + wsDebtFlag    + "]");
           }
           if ( getValue("pcod.interest_method").equals("Y") && !getValue("merc.trans_flag").equals("Y") && // 計息科目
                Arrays.asList("0","1").contains(wsPasingFlag) && !wsDebtFlag.equals("2") 
              )  // NOT BACK DATE
              { logs("@@@@@@@@@@ -1 ");processInterest(); } // 計算利息

           /*********************** Back_date 退息處理 **********************/
           if ( getValue("pcod.interest_method").equals("Y")  && wsDebtFlag.equals("2") )
              { selectActIntr(); }

           /*************************************************************/
           if ( getValue("adcl.batch_no").substring(8,12).equals("9999") &&  wsDumyFlag.equals("N") )
              {
                setValue("jrnl.tran_type"  ,getValue("adcl.payment_type"));
                setValue("jrnl.transaction_amt",getValue("adcl.pay_amt") );
                setValue("jrnl.dc_transaction_amt",getValue("adcl.dc_pay_amt") );
                insertActJrnl("1");
                wsDumyFlag = "Y";
              }

           /***************** 沖銷金額寫入 act_jrnl ***********************/
            setValue("jrnl.tran_type", "DEBT");
            setValue("jrnl.transaction_amt",""+cvtOrgCaptial);
            setValue("jrnl.dc_transaction_amt",""+cvtDcOrgCaptial);
            insertActJrnl("2");

            updateActDebt1();
            if ( dcPayOverAmt <= 0 )
               { break;  }

      } // end of for loop

    srt = null;
    return;
 }  // select_act_debt_1()

// 處理 ACT_ACCT_CURR 相關欄位
public void lastCurrData() throws Exception
{

   if ( wsRealPay.equals("Y") )
     {
      if ( getValue("adcl.pay_date").compareTo(getValue("acur.last_pay_date")) >= 0 )
        { setValue("acur.last_pay_date", getValue("adcl.pay_date")); }
     }

   long   numLong=0;
   double hPcreExchangeRate=0;

   if ( getValue("adcl.curr_code").equals("901") )
      {
        setValue("acur.end_bal_op", ""+getValueDouble("acur.dc_end_bal_op"));
        payOverAmt = dcPayOverAmt;
        logs("ttt pay_over_amt (1)   "+payOverAmt);
      }
   else
     {
        if ( getValue("adcl.batch_no").substring(8,12).equals("9999") )
           { hPcreExchangeRate = orgOverAmt / dcOrgOverAmt * 1.0;  }
        else
           { hPcreExchangeRate = getValueDouble("adcl.pay_amt") / getValueDouble("adcl.dc_pay_amt") * 1.0; }
        payOverAmt      = comcr.commCurrAmt("901",dcPayOverAmt * hPcreExchangeRate,0);
        setValue("acur.end_bal_op",""+payOverAmt);
        logs("ttt pay_over_amt (2)   "+payOverAmt);
     }

   double tmpDcAcctJrnlBal = getValueDouble("acur.dc_acct_jrnl_bal")
                        - totDcRealPayAmt
                        - totDcRealWaiveAmt
                        - dcPayOverAmt
                        + dcOrgOverAmt;
   
   tmpDcAcctJrnlBal =  convAmtDp2r(tmpDcAcctJrnlBal);
   setValue("acur.dc_acct_jrnl_bal",""+tmpDcAcctJrnlBal);

   logs("ttt tmp_dc_acct_jrnl_bal "+tmpDcAcctJrnlBal);

   logs("ttt 11  "+getValueDouble("acur.acct_jrnl_bal"));
   logs("ttt 22  "+totRealPayAmt);
   logs("ttt 33  "+totRealWaiveAmt);
   logs("ttt 44  "+payOverAmt);
   logs("ttt 55  "+orgOverAmt);

   double tmpAcctJrnlBal = getValueDouble("acur.acct_jrnl_bal")
                       - totRealPayAmt
                       - totRealWaiveAmt
                       - payOverAmt
                       + orgOverAmt;
   
   tmpAcctJrnlBal = convAmtDp2r(tmpAcctJrnlBal);
   
   setValue("acur.acct_jrnl_bal",""+tmpAcctJrnlBal);
   logs("ttt 66  "+getValueDouble("acur.acct_jrnl_bal"));

   if ( getValue("adcl.curr_code").equals("901") )
      { setValue("acur.dc_acct_jrnl_bal", getValue("acur.acct_jrnl_bal")); }

 //num_long = (long)((tot_dc_interest_amt * 10000.0) + 0.5);
 //tot_dc_interest_amt = (double)num_long / 10000.0;
   double tmpInterest = getValueDouble("acur.dc_temp_unbill_interest") + totDcInterestAmt;
 //num_long = (long)((tmp_interest * 100.0) + 0.5);
 //tmp_interest = (double)num_long / 100.0;
 //conv_amt_dp2r(x)除了校正微小誤差且有四捨五入到小數以下第2位
   tmpInterest = convAmtDp2r(tmpInterest);
   setValue("acur.dc_temp_unbill_interest",""+tmpInterest);
   logs("ttt tmp_interest (1)  "+tmpInterest);

 //num_long = (long)(tot_interest_amt * 10000.0 + 0.5);
 //tot_interest_amt = (double)num_long / 10000.0;
   tmpInterest =   getValueDouble("acur.temp_unbill_interest") + totInterestAmt;
 //num_long = (long)(tmp_interest * 100.0 + 0.5);
 //tmp_interest = (double)num_long / 100.0;
 //conv_amt_dp2r(x)除了校正微小誤差且有四捨五入到小數以下第2位
   tmpInterest = convAmtDp2r(tmpInterest);
   setValue("acur.temp_unbill_interest",""+tmpInterest);

   logs("ttt tmp_interest (2)  "+tmpInterest);

   if ( getValue("adcl.curr_code").equals("901") )
      { setValue("acur.temp_unbill_interest", getValue("acur.dc_temp_unbill_interest")); }

   if ( getValueDouble("acur.dc_temp_unbill_interest") == 0 )
      { setValue("acur.temp_unbill_interest","0"); }

   double tmpDcEndBalOp = dcPayOverAmt + (totDcRealWaiveAmt - totDcRealDebitAmt);
   tmpDcEndBalOp =  convAmtDp2r(tmpDcEndBalOp);
   
   setValue("acur.dc_end_bal_op",""+comcr.commCurrAmt(getValue("adcl.curr_code"),tmpDcEndBalOp,0));

   logs("ttt-acur.dc_end_bal_op-1 "+getValue("acur.dc_end_bal_op"));

   if ( getValueDouble("acur.dc_end_bal_op") < 0 )
      { setValue("acur.dc_end_bal_op","0"); }

   if ( getValue("adcl.curr_code").equals("901") ) {
        if ( dcPayOverAmt > 0 && getValueDouble("acct.adi_end_bal") > 0 )
           { waiveAdditionIntr(); }
      }

  if ( getValueDouble("acur.dc_end_bal_lk") > 0 ) //end_bal_lk不會有值，不會走到這段
     {
       if ( getValueDouble("acur.dc_end_bal_op") >= getValueDouble("acur.dc_end_bal_lk") )
          {
            setValueDouble("acur.dc_end_bal_op",getValueDouble("acur.dc_end_bal_op") - getValueDouble("acur.dc_end_bal_lk"));
            setValueDouble("acur.end_bal_op",getValueDouble("acur.end_bal_op") - getValueDouble("acur.end_bal_lk"));
          }
       else
         {
            setValueDouble("avda.vouch_amt", getValueDouble("acur.dc_end_bal_lk")  - getValueDouble("acur.dc_end_bal_op"));
            setValueDouble("avda.d_vouch_amt",getValueDouble("acur.dc_end_bal_lk") - getValueDouble("acur.dc_end_bal_op"));
            setValue("avda.acct_code","");
          //setValue("avda.reference_no" ,"");
            setValue("avda.reference_seq" ,"");
            setValue("avda.tx_acct_month","");
            insertActVouchData("3");
            setValue("acur.dc_end_bal_lk",getValue("acur.dc_end_bal_op"));
            setValue("acur.end_bal_lk", getValue("acur.end_bal_op"));
            setValue("acur.dc_end_bal_op","0");
            setValue("acur.end_bal_op","0");
         }
     }

  if ( getValueDouble("acur.dc_end_bal_op") == 0 )
     { setValue("acur.end_bal_op","0");  }

  if ( getValue("adcl.curr_code").equals("901") )
     { setValue("acur.end_bal_op",getValue("acur.dc_end_bal_op")); }

  nonDcRealPayAmt = nonDcRealPayAmt - (dcOrgOverAmt - dcPayOverAmt);
  nonRealPayAmt    = nonRealPayAmt - (orgOverAmt - payOverAmt);

  logs("ttt-non_dc_real_pay_amt "+nonDcRealPayAmt);

  logs("ttt-non_real_pay_amt-1 "+nonRealPayAmt);
  logs("ttt-non_real_pay_amt-2 "+orgOverAmt);
  logs("ttt-non_real_pay_amt-3 "+payOverAmt);
  logs("ttt-non_real_pay_amt-f "+nonRealPayAmt);

  if ( nonDcRealPayAmt < 0 ) {
       nonDcRealPayAmt = 0;
       nonRealPayAmt    = 0;
     }

   logs("ttt 77  "+getValueDouble("acur.dc_ttl_amt_bal"));
   logs("ttt 88  "+totDcRealPayAmt);
   logs("ttt 99  "+nonDcRealPayAmt);
   logs("ttt 10  "+totDcRealWaiveAmt);
   logs("ttt 11  "+nonDcRealWaiveAmt);
   logs("ttt 12  "+dcPayOverAmt);
   logs("ttt 13  "+dcOrgOverAmt);

  double tmpAcurDcTtlAmtBal = getValueDouble("acur.dc_ttl_amt_bal")
                                 - totDcRealPayAmt
                                 + nonDcRealPayAmt
                                 - totDcRealWaiveAmt
                                 + nonDcRealWaiveAmt
                                 - dcPayOverAmt
                                 + dcOrgOverAmt;
  tmpAcurDcTtlAmtBal =  convAmtDp2r(tmpAcurDcTtlAmtBal);
  setValue("acur.dc_ttl_amt_bal",""+tmpAcurDcTtlAmtBal);

  double tmpAcurTtlAmtBal    = getValueDouble("acur.ttl_amt_bal")
                                 - totRealPayAmt
                                 + nonRealPayAmt
                                 - totRealWaiveAmt
                                 + nonRealWaiveAmt
                                 - payOverAmt
                                 + orgOverAmt;
  tmpAcurTtlAmtBal =  convAmtDp2r(tmpAcurTtlAmtBal);
  setValue("acur.ttl_amt_bal",""+tmpAcurTtlAmtBal);

//logs("ttt-tmp_acur_dc_ttl_amt_bal "+tmp_acur_dc_ttl_amt_bal);
//logs("ttt-tmp_acur_ttl_amt_bal "+tmp_acur_ttl_amt_bal);


  if ( getValueDouble("acur.dc_ttl_amt_bal") == 0 )
     { setValue("acur.ttl_amt_bal","0"); }

  if ( getValue("adcl.curr_code").equals("901") )
     { setValue("acur.ttl_amt_bal",getValue("acur.dc_ttl_amt_bal")); }

  if ( wsRealPay.equals("Y") )
     {
       setValueInt("acur.pay_cnt",getValueInt("acur.pay_cnt")+1);
     //setValueDouble("acur.dc_pay_amt",getValueDouble("acur.dc_pay_amt") + getValueDouble("adcl.dc_pay_amt") );
     //setValueDouble("acur.pay_amt",getValueDouble("acur.pay_amt") + getValueDouble("adcl.pay_amt") );

       double tmpPayAmt = getValueDouble("acur.dc_pay_amt") + getValueDouble("adcl.dc_pay_amt");
     //num_long = (long)(tmp_pay_amt * 100.0 + 0.5);
     //tmp_pay_amt = (double)num_long / 100.0;
     //conv_amt_dp2r(x)除了校正微小誤差且有四捨五入到小數以下第2位
       tmpPayAmt = convAmtDp2r(tmpPayAmt);
       setValue("acur.dc_pay_amt",""+tmpPayAmt);

       tmpPayAmt = getValueDouble("acur.pay_amt") + getValueDouble("adcl.pay_amt");
     //num_long = (long)(tmp_pay_amt * 100.0 + 0.5);
     //tmp_pay_amt = (double)num_long / 100.0;
     //conv_amt_dp2r(x)除了校正微小誤差且有四捨五入到小數以下第2位
       tmpPayAmt = convAmtDp2r(tmpPayAmt);
       setValue("acur.pay_amt",""+tmpPayAmt);

     }

  /**********************************************************/
  if ( !getValue("adcl.batch_no").substring(8,12).equals("9999") )
     {
       double tmpAcurDcMinPayBal = getValueDouble("acur.dc_min_pay_bal")
                                      - totDcRealPayAmt
                                      + nonDcRealPayAmt
                                      - totDcRealWaiveAmt
                                      + nonDcRealWaiveAmt
                                      - dcPayOverAmt
                                      + dcOrgOverAmt;
      
      tmpAcurDcMinPayBal =  convAmtDp2r(tmpAcurDcMinPayBal);

      setValue("acur.dc_min_pay_bal",""+tmpAcurDcMinPayBal);

      logs("ttt-tmp_acur_dc_min_pay_bal "+tmpAcurDcMinPayBal);

      if ( getValueDouble("acur.dc_min_pay_bal") >= getValueDouble("acur.dc_ttl_amt_bal") )
         { setValue("acur.dc_min_pay_bal",  getValue("acur.dc_ttl_amt_bal")); }

      if ( getValueDouble("acur.dc_min_pay_bal") < 0 )
         { setValue("acur.dc_min_pay_bal","0"); }

      selectActAcagCurr();
      deleteActAcag();

      if ( getValue("adcl.curr_code").equals("901") )
         { setValue("acur.min_pay_bal",getValue("acur.dc_min_pay_bal")); }
     }

  /**********************************************************/

//if ( !Arrays.asList("AUT1","ACH1","AUT2").contains(getValue("adcl.payment_type")) ) modified on 2019/07/24
  if (  Arrays.asList("AUT1","ACH1","AUT2").contains(getValue("adcl.payment_type")) )
     {
     //setValueDouble("acur.dc_autopay_bal",getValueDouble("acur.dc_autopay_bal") - getValueDouble("adcl.dc_pay_amt") );
       double tmpAutopayBal = getValueDouble("acur.dc_autopay_bal") - getValueDouble("adcl.dc_pay_amt");
       tmpAutopayBal = convAmtDp2r(tmpAutopayBal);
       setValueDouble("acur.dc_autopay_bal",tmpAutopayBal);
       setValueDouble("acur.autopay_bal",getValueDouble("acur.autopay_bal")       - getValueDouble("adcl.pay_amt") );
       if ( getValueDouble("acur.dc_autopay_bal") == 0 )
          { setValue("acur.autopay_bal","0"); }
     }

  if ( totDcRealWaiveAmt > 0 )
     {
       setValueInt("acur.adjust_dr_cnt",getValueInt("acur.adjust_dr_cnt")+1);
       setValueDouble("acur.adjust_dr_amt",getValueDouble("acur.adjust_dr_amt")       + totRealWaiveAmt);
       setValueDouble("acur.dc_adjust_dr_amt",getValueDouble("acur.dc_adjust_dr_amt") + totDcRealWaiveAmt);
       logs("ttt-tot_real_waive_amt "+totRealWaiveAmt);
       logs("ttt-tot_dc_real_waive_amt "+totDcRealWaiveAmt);
     }

  if ( getValueDouble("acur.dc_ttl_amt_bal") <= getValueDouble("pcgl.total_bal") ) {
       if ( getValue("adcl.pay_date").compareTo(getValue("wday.this_delaypay_date")) <= 0 )
          { setValue("acur.delaypay_ok_flag", "Y");  }
       else
          {
            //if ( comr.getMcode(getValue("adcl.acct_type"),getValue("adcl.p_seqno")) == 0 &&
            if ( getValueInt("acno.int_rate_mcode") == 0 && getValue("adcl.pay_date").compareTo(getValue("wday.next_close_date"))  <= 0 )
               {
                 if ( selectCrdCard() != 0 )
                    { updateActAcctCurr1(); }
               }
          }
      }
   return;
 }

public  void waiveAdditionIntr() throws Exception
{
    long numLong=getValueLong("acct.adi_end_bal");
    if ( numLong == 0 )
       { return; }

    if ( dcPayOverAmt >= numLong ) {
         logs("ttt-num_long-1 "+numLong);
         logs("ttt-dc_pay_over_amt-1 "+dcPayOverAmt);
         logs("ttt-pay_over_amt-1 "+payOverAmt);

         setValueDouble("jrnl.transaction_amt",numLong);
         setValueDouble("acur.dc_end_bal_op", dcPayOverAmt - numLong);
         setValueDouble("acur.end_bal_op" ,(payOverAmt - numLong));
         setValueDouble("acct.adi_end_bal", getValueDouble("acct.adi_end_bal") - numLong);
       }
    else {
         logs("ttt-dc_pay_over_amt-2 "+dcPayOverAmt);
         setValueDouble("jrnl.transaction_amt",dcPayOverAmt);
         setValueDouble("acct.adi_end_bal",getValueDouble("acct.adi_end_bal") - dcPayOverAmt);
         setValue("acur.dc_end_bal_op","0");
         setValue("acur.end_bal_op","0");
       }

    setValue("jrnl.tran_type", "ADIT");
    setValue("jrnl.dc_transaction_amt", getValue("jrnl.transaction_amt"));
    insertActJrnl("3");

  //setValue("avda.reference_no"   , "");
    setValue("avda.reference_seq"   , "");
    setValue("avda.acct_code" , "AI");
    setValue("avda.vouch_amt", getValue("jrnl.transaction_amt"));
    setValue("avda.d_vouch_amt",getValue("jrnl.transaction_amt"));
    setValue("avda.tx_acct_month",getValue("wday.next_acct_month"));
    insertActVouchData("1");

    if ( getValue("adcl.batch_no").substring(8,12).equals("9999") && wsDumyFlag.equals("N") ) {
         setValue("jrnl.tran_type", getValue("adcl.payment_type"));
         setValue("jrnl.transaction_amt", getValue("adcl.pay_amt"));
         setValue("jrnl.dc_transaction_amt", getValue("adcl.dc_pay_amt"));
         insertActJrnl("1");
         wsDumyFlag = "Y";
         logs("ttt-ws_dumy_flag "+wsDumyFlag);
       }

 return;
}

// 處理 ACT_ACCT 相關欄位
public void lastAcctData() throws Exception
{
  selectActAcctCurr1();
  if ( wsRealPay.equals("Y") )
     {
       logs("ttt-ws_real_pay "+wsRealPay);
       if ( getValue("adcl.pay_date").compareTo(getValue("acct.last_payment_date")) >= 0 )
          { setValue("acct.last_payment_date", getValue("adcl.pay_date")); }

       if ( getValue("adcl.pay_date").compareTo(getValue("acno.last_pay_date")) >= 0 )  {
            setValue("acno.last_pay_amt",getValue("adcl.pay_amt"));
            setValue("acno.last_pay_date",getValue("adcl.pay_date"));
          }
     }

  logs("ttt-tot_real_pay_amt "+totRealPayAmt);
  logs("ttt-tot_real_waive_amt "+totRealWaiveAmt);

  setValueDouble("acct.rc_min_pay_bal",
                  getValueDouble("acct.rc_min_pay_bal") - totRealPayAmt - totRealWaiveAmt);
  if ( getValueDouble("acct.rc_min_pay_bal") < 0 )
     { setValue("acct.rc_min_pay_bal","0"); }

  if ( getValueDouble("acct.rc_min_pay_bal") < getValueDouble("acct.rc_min_pay_m0") )
     { setValue("acct.rc_min_pay_m0",getValue("acct.rc_min_pay_bal")); }

  if ( getValueDouble("acct.min_pay_bal") == 0 && getValueDouble("acct.min_pay") > 0 )
     {
       if ( getValue("adcl.payment_type").equals("REFU") )
          {
            if ( getValue("acct.last_min_pay_date").length() == 0  ||
                 getValue("busi.business_date").compareTo(getValue("acct.last_min_pay_date")) < 0 )
               { setValue("acct.last_min_pay_date",getValue("busi.business_date")); }
          }
      else
          {
            if ( getValue("acct.last_min_pay_date").length() == 0  ||
                 getValue("adcl.pay_date").compareTo(getValue("acct.last_min_pay_date")) < 0 )
               { setValue("acct.last_min_pay_date" , getValue("adcl.pay_date")); }
          }
     }

  if ( getValueDouble("acct.ttl_amt_bal") <= 0 && getValueDouble("acct.ttl_amt") > 0 )
     {
       if ( getValue("adcl.payment_type").equals("REFU") )
          {
            if ( getValue("acct.last_cancel_debt_date").length() == 0  ||
                 getValue("busi.business_date").compareTo(getValue("acct.last_cancel_debt_date")) < 0 )
               { setValue("acct.last_cancel_debt_date" ,getValue("busi.business_date")); }
          }
       else
          {
            if ( getValue("acct.last_cancel_debt_date").length() == 0 ||
                 getValue("adcl.pay_date").compareTo(getValue("acct.last_cancel_debt_date")) < 0 )
               { setValue("acct.last_cancel_debt_date" ,getValue("adcl.pay_date")); }
          }
      }

  if ( getValue("adcl.payment_type").equals("AUT1") && getValueDouble("acct.pay_by_stage_bal") > 0 )
     {
       setValueDouble("acct.pay_by_stage_bal", getValueDouble("acct.pay_by_stage_bal") - totRealPayAmt );
       if ( getValueDouble("acct.pay_by_stage_bal") < 0 )
          { setValue("acct.pay_by_stage_bal","0"); }
     }

  return;
}

/***** 計算利息 *****/
public void processInterest() throws Exception
 {
    /***** 設定循環息計算天數 START *********/
    if ( getValue("debt.interest_rs_date",di).length() == 8 )
       {               //利息重新起算日期
         if ( getValue("debt.interest_rs_date",di).substring(0,6).compareTo(getValue("adcl.pay_date").substring(0,6)) >= 0 )
            { logs("@@@@@@@@@@ -2"); return; }
         setValue("intr.intr_s_date" , getValue("debt.interest_rs_date",di));
       }
    else
      {
         if ( getValue("adcl.pay_date").compareTo(getValue("wday.this_delaypay_date")) <= 0 )
            { logs("@@@@@@@@@@ -3"); return; } // 寬限日前繳青

         if ( getValue("debt.acct_month",di).compareTo(getValue("wday.this_acct_month")) == 0 )
            {
              if ( getValueDouble("merc.mp_rate") == 100 )
                 { setValue("intr.intr_s_date" , getValue("wday.this_lastpay_date")); }
              else
                 { setValue("intr.intr_s_date" , getValue("debt.interest_date",di));  }
            }
         else
            {
              if ( getValue("acno.new_cycle_month").length() > 0 && getValue("wday.next_acct_month").equals(getValue("acno.new_cycle_month")) )
                 { setValue("intr.intr_s_date" , getValue("acno.last_interest_date") ); }
              else
                 { setValue("intr.intr_s_date" , getValue("wday.this_interest_date") ); }
            }
     }

    setValue("intr.intr_e_date" , getValue("adcl.pay_date"));

    if ( getValue("intr.intr_s_date").length() == 0 )
       { setValue("intr.intr_s_date" , getValue("debt.post_date",di)); }
    /***** 設定循環息計算天數 ENDED *********/

    processInterestRate(); // 設定循環息優惠利率

    /***** 計算利息 START ****/
    String intrSDate = getValue("intr.intr_s_date");
    String intrEDate = getValue("intr.intr_e_date");

    logs("ttt- @@@@@@@@@@@@@ intr_s_date : "+getValue("intr.intr_s_date"));
    logs("ttt- @@@@@@@@@@@@@ intr_e_date : "+getValue("intr.intr_e_date"));

    int intrDates = comm.datePeriod(intrSDate,intrEDate);
    logs("ttt- @@@@@@@@@@@@@ intr days : "+intrDates);
    if ( intrDates == 0 )
       { return; }

    double tmpDcInterestAmt =  intrDates * getValueDouble("intr.dc_intr_org_captial") * getValueDouble("intr.interest_rate");
           tmpDcInterestAmt =  tmpDcInterestAmt / 10000;
  //setValueDouble("intr.dc_interest_amt",tmp_dc_interest_amt);

    logs("ttt- @@@@@@@@@@@@@ tmp_dc_interest_amt-1 : "+tmpDcInterestAmt);

  //double tmp_interest_amt =  getValueDouble("intr.dc_interest_amt") *
    double tmpInterestAmt =  tmpDcInterestAmt *
                             ( getValueDouble("debt.beg_bal",di) / getValueDouble("debt.dc_beg_bal",di) );

    logs("ttt- @@@@@@@@@@@@@ tmp_interest_amt-1 : "+tmpInterestAmt);

  //long temp_long = (long)(tmp_interest_amt * 100.0 + 0.5);
  //tmp_interest_amt = (double)temp_long / 100.0;
  //conv_amt_dp2r(x)除了校正微小誤差且有四捨五入到小數以下第2位
    tmpInterestAmt = convAmtDp2r(tmpInterestAmt);
    setValueDouble("intr.interest_amt", tmpInterestAmt);

  //temp_long = (long)(tmp_dc_interest_amt * 100.0 + 0.5);
  //tmp_dc_interest_amt = (double)temp_long / 100.0;
  //conv_amt_dp2r(x)除了校正微小誤差且有四捨五入到小數以下第2位
    tmpDcInterestAmt = convAmtDp2r(tmpDcInterestAmt);
    setValueDouble("intr.dc_interest_amt",tmpDcInterestAmt);

  //if ( getValueInt("intr.dc_interest_amt") == 0 )
    if ( getValueDouble("intr.dc_interest_amt") == 0 )
       { setValue("intr.interest_amt","0"); }

    if ( getValue("adcl.curr_code").equals("901") )
       { setValue("intr.interest_amt", getValue("intr.dc_interest_amt")); }

    totDcInterestAmt += getValueDouble("intr.dc_interest_amt");
    totInterestAmt    += getValueDouble("intr.interest_amt");
    logs("ttt- @@@@@@@@@@@@@ tot_dc_interest_amt-2 : "+totDcInterestAmt);
    logs("ttt- @@@@@@@@@@@@@ tot_interest_amt-2 : "+totInterestAmt);
    if ( getValueDouble("intr.interest_amt") > 0)
       { insertActIntr("0"); }

    return;
 }

/***** 設定循環息優惠利率 ***********/
public void processInterestRate() throws Exception
 {
     double minIntRate=0;
     int i = getValueInt("pcod.inter_rate_code");

     setValueDouble("intr.interest_rate", tempRevolvingInterest[i]);
     fitRevolvingRate1   = tempRevolvingInterest[i];

     logs("ttt- @@@@@@@@@@@@@ fit_revolving_rate1 : "+fitRevolvingRate1);

     int k = getValueInt("pcod.inter_rate_code2");
     if ( getValue("debt.ao_flag",di).equals("Y") )     /* 代償使用第二段利率, 非 act_commute  */
        { fitRevolvingRate1 = tempRevolvingInterest[k]; }

    if (pSeqno.length()!=0)
       {
        showLogMessage("I","","Mantis 9333-01 temp_revolving_interest["+k+"] : [" + tempRevolvingInterest[1]  +"]");
        showLogMessage("I","","Mantis 9334-02 rc_max_rate date   : [" + getValueDouble("adcl.rc_max_rate") +"]");
       }

     /***** 設定循環息優惠利率 START ******/
     //if ( comr.getMcode(getValue("adcl.acct_type"),getValue("adcl.p_seqno")) == 0 && getValue("debt.int_rate_flag",di).equals("Y") )
     if ( getValueInt("acno.int_rate_mcode") == 0 && getValue("debt.int_rate_flag",di).equals("Y") )
        {
          setValue("intr.interest_rate",getValue("debt.int_rate",di));
        }
     else
        {
          minIntRate = 0;
          setValueDouble("intr.interest_rate",fitRevolvingRate1);

         if ( getValue("wday.next_acct_month").compareTo(getValue("acno.revolve_rate_s_month")) >= 0  &&
              getValue("wday.next_acct_month").compareTo(getValue("acno.revolve_rate_e_month")) <= 0 )
            { minIntRate = getValueDouble("acno.revolve_int_rate"); }

         if ( getValue("wday.next_acct_month").compareTo(getValue("acno.group_rate_s_month")) >= 0  &&
              getValue("wday.next_acct_month").compareTo(getValue("acno.group_rate_e_month")) <= 0 )
            {
              if ( minIntRate > getValueDouble("acno.group_int_rate") )
                 { minIntRate = getValueDouble("acno.group_int_rate"); }
            }

         if ( getValue("wday.next_acct_month").compareTo(getValue("acno.batch_rate_s_month")) >= 0  &&
              getValue("wday.next_acct_month").compareTo(getValue("acno.batch_rate_e_month")) <= 0 )
            {
              if ( minIntRate > getValueDouble("acno.batch_int_rate") )
                 { minIntRate = getValueDouble("acno.batch_int_rate"); }
            }

         setValueDouble("intr.interest_rate", (getValueDouble("intr.interest_rate") + minIntRate) );

         if ( getValue("debt.ao_flag",di).equals("Y") )    /* 代償 */
            {
              selectActCommute();
              if ( getValue("busi.business_date").equals(getValue("acom.ao_rate_date"))  &&
                   getValueDouble("intr.interest_rate") > getValueDouble("acom.ao_int_rate") )
                 { setValue("intr.interest_rate",getValue("acom.ao_int_rate")); }
            }
         else if ( getValue("acno.new_bill_flag").equals("Y")  &&   /* 代償戶之新增'BL'消費 */
                   getValue("debt.acct_code",di).equals("BL") )
            {
              if ( getValue("debt.purchase_date",di).compareTo(getValue("acno.ao_posting_date")) >= 0  &&
                   getValue("acno.ao_posting_date").length() > 0 )
                {
                  if ( getValueDouble("intr.interest_rate") > getValueDouble("acno.aox_int_rate") &&
                       getValueDouble("acno.aox_int_rate")  > 0  )
                     { setValue("intr.interest_rate", getValue("acno.aox_int_rate")); }
                }
            }
        }
     if (pSeqno.length()!=0)
        showLogMessage("I","","Mantis 9333-03 bef interest_rate   : [" + String.format("%.3f",getValueDouble("intr.interest_rate")) +"]");

     if (getValueDouble("intr.interest_rate") < 0)
        setValueDouble("intr.interest_rate" , 0);

     if (getValueDouble("adcl.rc_max_rate")>0)
     if (getValueDouble("intr.interest_rate") > getValueDouble("adcl.rc_max_rate"))
        setValueDouble("intr.interest_rate" ,getValueDouble("adcl.rc_max_rate"));

     if (pSeqno.length()!=0)
        showLogMessage("I","","Mantis 9333-04 aft interest_rate   : [" + String.format("%.3f",getValueDouble("intr.interest_rate")) +"]");

     logs("ttt- @@@@@@@@@@@@@ interest_rate  : "+getValue("intr.interest_rate"));
     return;
 }

/*****************************************************************************/
public void procWaiveInterest() throws Exception
{
     int totDays;
     processInterestRate(); // 設定循環息優惠利率

     if ( getValue("adcl.pay_date").compareTo(getValue("intr.intr_e_date")) > 0 )
        { logs("ttt-@@@@@@@@@@ -P "); return; }

     if ( getValue("adcl.pay_date").compareTo(getValue("intr.intr_s_date")) > 0 )
        { setValue("intr.intr_s_date" , getValue("adcl.pay_date")); }

      totDays = comcr.calDays(getValue("intr.intr_s_date"),getValue("intr.intr_e_date"))+1;
      if ( totDays < 0 )
         { totDays = 0; }

      double tmpDcInterestAmt = comcr.commCurrAmt( getValue("adcl.curr_code"),
                   (totDays * getValueDouble("intr.dc_intr_org_captial") * getValueDouble("intr.interest_rate")) / 10000.0,0);
      setValueDouble("intr.dc_interest_amt",tmpDcInterestAmt);

      double tmpInterestAmt = comcr.commCurrAmt("901",
                   getValueDouble("intr.dc_interest_amt") * ( getValueDouble("debt.beg_bal",di) / getValueDouble("debt.dc_beg_bal",di)),0);
      setValueDouble("intr.interest_amt",tmpInterestAmt);

      if ( getValueDouble("intr.dc_interest_amt") == 0 )
         { setValue("intr.interest_amt","0"); }

      if ( getValue("adcl.curr_code").equals("901") )
         { setValue("intr.interest_amt", getValue("intr.dc_interest_amt")); }

      if ( getValueDouble("intr.dc_interest_amt") > getValueDouble("intr.dc_inte_d_amt") )
         {
           totWaiveIntrAmt    +=  getValueDouble("intr.inte_d_amt");
           totDcWaiveIntrAmt +=  getValueDouble("intr.dc_inte_d_amt");
           setValue("intr.interest_amt",getValue("intr.inte_d_amt"));
           setValue("intr.dc_interest_amt",getValue("intr.dc_inte_d_amt"));
           setValue("intr.inte_d_amt","0");
           setValue("intr.dc_inte_d_amt","0");
         }
      else
         {
           totWaiveIntrAmt    += getValueDouble("intr.interest_amt");
           totDcWaiveIntrAmt += getValueDouble("intr.dc_interest_amt");
           double tmp_inte_d_amt    = getValueDouble("intr.inte_d_amt")    - getValueDouble("intr.interest_amt");
           double tmp_dc_inte_d_amt = getValueDouble("intr.dc_inte_d_amt") - getValueDouble("intr.dc_interest_amt");
           setValueDouble("intr.inte_d_amt",tmp_inte_d_amt);
           setValueDouble("intr.dc_inte_d_amt",tmp_dc_inte_d_amt);
         }

     logs("ttt- @@@@@@@@@@ intr.interest_amt  : "+getValue("intr.interest_amt"));
     logs("ttt- @@@@@@@@@@ intr.dc_interest_amt  : "+getValue("intr.dc_interest_amt"));
     logs("ttt- @@@@@@@@@@ intr.inte_d_amt  : "+getValue("intr.inte_d_amt"));
     logs("ttt- @@@@@@@@@@ intr.dc_inte_d_amt  : "+getValue("intr.dc_inte_d_amt"));
     insertActIntr("2");
     return;
 }

public void selectActDebt2() throws Exception
 {
   logs("ttt- select_act_debt_2  : ");

   setValue("debt.p_seqno",getValue("adcl.p_seqno"));
   setValue("debt.curr_code",getValue("adcl.curr_code"));
   int debtCnt = getLoadData(debtKey);
   logs("ttt (2) debtCnt- "+debtCnt+" "+getValue("adcl.p_seqno"));

   SortObject   srt  = new SortObject(this);
   
   srt.sortLoadData(sortField_4,debtCnt);

   for ( int i=0; i<debtCnt; i++ ) {

         di = srt.getSortIndex(i); // SORT INDEX
         orgOrder = i;

         if ( getValue("debt.post_date",di).compareTo(getValue("adcl.pay_date")) < 0 ||
              getValueDouble("debt.d_avail_bal",di) <= 0 )
            { continue; }

         if ( Arrays.asList("PN","AI","CI").contains(getValue("debt.acct_code",di)) )
            { ; }
         else
         if ( getValue("debt.acct_code",di).equals("RI")  && getValue("debt.bill_type",di).equals("OSSG") )
            { ; }
         else
            { continue; }

         setValue("avda.acct_code",getValue("debt.acct_code",di));
       //setValue("avda.reference_no",getValue("debt.reference_no",di));
         setValue("avda.reference_seq",getValue("debt.reference_no",di));

         if ( getValue("debt.acct_code",di).equals("PN") )
            {
               if ( minOverFlag == 1 )
                  { penaltyWaiveRtn(); }
               continue;
            }

         double tmpWaiveIntrAmt    =  totWaiveIntrAmt;
         double tmpDcWaiveIntrAmt =  totDcWaiveIntrAmt;
         if ( totDcWaiveIntrAmt  >=  getValueDouble("debt.dc_d_avail_bal",di) )
            {
              totRealWaiveAmt     +=  getValueDouble("debt.d_avail_bal",di);
              totDcRealWaiveAmt  +=  getValueDouble("debt.dc_d_avail_bal",di);
              if ( getValue("debt.acct_month",di).equals(getValue("wday.next_acct_month")) )
                 {
                   nonRealWaiveAmt    = nonRealWaiveAmt    + getValueDouble("debt.d_avail_bal",di);
                   nonDcRealWaiveAmt = nonDcRealWaiveAmt + getValueDouble("debt.dc_d_avail_bal",di);
                 }
              setValue("avda.vouch_amt",getValue("debt.dc_d_avail_bal",di));
              totWaiveIntrAmt         -=  getValueDouble("debt.d_avail_bal",di);
              totDcWaiveIntrAmt      -=  getValueDouble("debt.dc_d_avail_bal",di);
              setValue("debt.d_avail_bal","0",di);
              setValue("debt.dc_d_avail_bal","0",di);

              logs("ttt-1 d_avail_bal  : "+getValue("debt.d_avail_bal",di));
              logs("ttt-1 dc_d_avail_bal  : "+getValue("debt.dc_d_avail_bal",di));
            }
         else
            {
              totRealWaiveAmt         += totWaiveIntrAmt;
              totDcRealWaiveAmt      += totDcWaiveIntrAmt;
              if ( getValue("debt.acct_month",di).equals(getValue("wday.next_acct_month"))) {
                   nonRealWaiveAmt    += totWaiveIntrAmt;
                   nonDcRealWaiveAmt += totDcWaiveIntrAmt;
                 }
              setValue("avda.vouch_amt",""+totDcWaiveIntrAmt);
              setValue("debt.d_avail_bal", ""+(getValueDouble("debt.d_avail_bal",di) - totWaiveIntrAmt),di);
              setValue("debt.dc_d_avail_bal",""+(getValueDouble("debt.dc_d_avail_bal",di) - totDcWaiveIntrAmt),di);
              if ( getValueDouble("debt.dc_d_avail_bal",di) == 0 )
                 { setValue("debt.d_avail_bal","0",di); }
              totWaiveIntrAmt    = 0;
              totDcWaiveIntrAmt = 0;
              logs("ttt-2 d_avail_bal  : "+getValue("debt.d_avail_bal",di));
              logs("ttt-2 dc_d_avail_bal  : "+getValue("debt.dc_d_avail_bal",di));
            }

         if ( tmpDcWaiveIntrAmt >= getValueDouble("debt.dc_end_bal",di) )
            {
              totRealDebitAmt        +=  getValueDouble("debt.end_bal",di);
              totDcRealDebitAmt     +=  getValueDouble("debt.dc_end_bal",di);
              setValue("avda.d_vouch_amt", getValue("debt.dc_end_bal",di));
              setValue("jrnl.transaction_amt",getValue("debt.end_bal",di));
              setValue("jrnl.dc_transaction_amt",getValue("debt.dc_end_bal",di));
              setValue("debt.end_bal","0",di);
              setValue("debt.dc_end_bal","0",di);
              logs("ttt-3 end_bal  : "+getValue("debt.end_bal",di));
              logs("ttt-3 dc_end_bal  : "+getValue("debt.dc_end_bal",di));
            }
         else
            {
              totRealDebitAmt    +=  tmpWaiveIntrAmt;
              totDcRealDebitAmt +=  tmpDcWaiveIntrAmt;
              setValueDouble("avda.d_vouch_amt",tmpDcWaiveIntrAmt);
              setValueDouble("jrnl.transaction_amt",tmpWaiveIntrAmt);
              setValueDouble("jrnl.dc_transaction_amt",tmpDcWaiveIntrAmt);
              setValue("debt.end_bal",""+(getValueDouble("debt.end_bal",di) - tmpWaiveIntrAmt),di);
              setValue("debt.dc_end_bal",""+(getValueDouble("debt.dc_end_bal",di) - tmpDcWaiveIntrAmt),di);
              if ( getValueDouble("debt.dc_end_bal",di) == 0 )
                 { setValue("debt.end_bal","0",di); }
              logs("ttt-4 end_bal  : "+getValue("debt.end_bal",di));
              logs("ttt-4 dc_end_bal  : "+getValue("debt.dc_end_bal",di));
            }

         setValue("avda.tx_acct_month",getValue("debt.acct_month",di));
         insertActVouchData("0");
         updateActDebt1();
         setValue("jrnl.tran_type", "WAIN");
         insertActJrnl("2");
         if ( totDcWaiveIntrAmt == 0 )
            {
              totWaiveIntrAmt = 0;
              break;
            }
       }

  srt = null;
 return;
}

/*****************************************************************************/
void penaltyWaiveRtn() throws Exception
 {
    setValue("avda.d_vouch_amt", getValue("debt.dc_end_bal",di));      /* unpaid金額  */
    setValue("avda.vouch_amt",   getValue("debt.dc_d_avail_bal",di));  /* 起帳總金額  */
    setValue("avda.tx_acct_month",getValue("debt.acct_month",di));
    insertActVouchData("0");

    setValue("jrnl.tran_type", "WAPE");
    setValue("jrnl.transaction_amt",getValue("debt.end_bal",di));
    setValue("jrnl.dc_transaction_amt",getValue("debt.dc_end_bal",di));

    totRealWaiveAmt     += getValueDouble("debt.d_avail_bal",di);
    totDcRealWaiveAmt  += getValueDouble("debt.dc_d_avail_bal",di);

    logs("ttt-5 tot_real_waive_amt  : "+totRealWaiveAmt);
    logs("ttt-5 tot_dc_real_waive_amt  : "+totDcRealWaiveAmt);

    if ( getValue("debt.acct_month",di).equals(getValue("wday.next_acct_month")) )
       {
         nonRealWaiveAmt     += getValueDouble("debt.d_avail_bal",di);
         nonDcRealWaiveAmt  += getValueDouble("debt.dc_d_avail_bal",di);
         logs("ttt-6 non_real_waive_amt  : "+nonRealWaiveAmt);
         logs("ttt-6 non_dc_real_waive_amt  : "+nonDcRealWaiveAmt);
       }
    setValue("debt.d_avail_bal","0",di);
    setValue("debt.dc_d_avail_bal","0",di);

    totRealDebitAmt    +=  getValueDouble("debt.end_bal",di);
    totDcRealDebitAmt +=  getValueDouble("debt.dc_end_bal",di);
    setValue("debt.end_bal","0",di);
    setValue("debt.dc_end_bal","0",di);

    updateActDebt1();

    insertActJrnl("2");
}

/******************* SELECT *************/
public int  selectPtrBusinday() throws Exception
 {
   daoTable    = "ptr_businday";
   extendField = "busi.";
   selectSQL   = "business_date";
   whereStr    = "";
   int n = selectTable();
   if ( n == 0 )
      { showLogMessage("E","","select_ptr_businday ERROR "); exitProgram(3); }

   busiDate = getValue("busi.business_date");
   return n;
 }

 public int  selectPtrActgeneral() throws Exception
 {
   daoTable    = "ptr_actgeneral";
   extendField = "agen.";
   selectSQL   = "revolving_interest1,"
               + "revolving_interest2,"
               + "revolving_interest3,"
               + "revolving_interest4,"
               + "revolving_interest5,"
               + "revolving_interest6 ";
   whereStr    = "";
   int n = selectTable();
   if ( n == 0 )
      { showLogMessage("E","","select_ptr_actgeneral ERROR "); exitProgram(3); }

   tempRevolvingInterest[1] = getValueDouble("agen.revolving_interest1");
   tempRevolvingInterest[2] = getValueDouble("agen.revolving_interest2");
   tempRevolvingInterest[3] = getValueDouble("agen.revolving_interest3");
   tempRevolvingInterest[4] = getValueDouble("agen.revolving_interest4");
   tempRevolvingInterest[5] = getValueDouble("agen.revolving_interest5");
   tempRevolvingInterest[6] = getValueDouble("agen.revolving_interest6");

   return n;
 }


 public int  selectActCommute() throws Exception
 {
//logs("ttt-111111 ");
    daoTable    = "act_commute";
    extendField = "acom.";
    selectSQL   = "ao_int_rate,"
                + "ao_rate_date";
    whereStr    = "WHERE reference_no = ?";

    setString(1,getValue("debt.reference_no",di));
    int n = selectTable();
    if ( n == 0 )
       { showLogMessage("E","","select_act_commute ERROR "); /*exitProgram(3);*/ }
    return n;
 }

public void selectActIntr() throws Exception
 {
logs("ttt-@@@@@@@@@@ "+hTempBackDate+" "+getValue("debt.reference_no",di));

    daoTable     = "act_intr";
    extendField  = "intr.";
    selectSQL    = "post_date,"
                 + "acct_month,"
                 + "intr_s_date,"
                 + "intr_e_date,"
                 + "interest_sign,"
                 + "interest_amt,"
                 + "decode(decode(curr_code,'','901',curr_code),'901',interest_amt,dc_interest_amt) as dc_interest_amt,"
                 + "inte_d_amt,"
                 + "decode(decode(curr_code,'','901',curr_code),'901',inte_d_amt,dc_inte_d_amt) as dc_inte_d_amt,"
                 + "interest_rate,"
                 + "reason_code,"
                 + "rowid as rowid ";
    whereStr     = "WHERE reference_no = ? "
                 + " and  decode(curr_code,'','901',curr_code) = ? "
                 + " and  interest_sign != '-' "
                 + " and  post_date    > ? "
                 + " and  inte_d_amt  != 0 "
                 + " and  intr_e_date >= ? "
                 + " and  reason_code != 'DB00' "
                 + " order by acct_month";
   setString(1,getValue("debt.reference_no",di));
   setString(2,getValue("adcl.curr_code"));
   setString(3,hTempBackDate);
   setString(4,getValue("adcl.pay_date"));
   int n =  selectTable();
 //if ( n == 0 )
 //   { showLogMessage("E","","select_act_intr ERROR "); /*exitProgram(3);*/ }

   for ( int i=0; i < n; i++)
       {
         setValue("intr.post_date",getValue("intr.post_date",i));
         setValue("intr.acct_month",getValue("intr.acct_month",i));
         setValue("acht.acct_month",getValue("intr.acct_month",i));
         setValue("intr.intr_s_date",getValue("intr.intr_s_date",i));
         setValue("intr.intr_e_date",getValue("intr.intr_e_date",i));
         setValue("intr.interest_sign",getValue("intr.interest_sign",i));
         setValue("intr.interest_amt",getValue("intr.interest_amt",i));
         setValue("intr.dc_interest_amt",getValue("intr.dc_interest_amt",i));
         setValue("intr.inte_d_amt",getValue("intr.inte_d_amt",i));
         setValue("intr.dc_inte_d_amt",getValue("intr.dc_inte_d_amt",i));
         setValue("intr.interest_rate",getValue("intr.interest_rate",i));
         setValue("intr.reason_code",getValue("intr.reason_code",i));
         setValue("intr.rowid",getValue("intr.rowid",i));

         if ( selectActDebtA() == 0 )
            { continue; }
         procWaiveInterest();
         updateActIntr();
      }

 return;
}

public int selectActCurrHst1() throws Exception
 {
//logs("ttt-333333 ");
    actAcctstFlag=0;
    daoTable    = "act_curr_hst";
    extendField = "acht.";
    selectSQL = "acct_month,"
              + "to_char(add_months(to_date(acct_month,'yyyymm'),1),'yyyymm') as deba_month,"
              + "min_pay_bal,"
              + "waive_ttl_bal,"
              + "rowid as rowid ";
    whereStr  = "Where  p_seqno   = ?"
              + " AND   curr_code = ?"
              + " AND   acct_month||? >= to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymmdd')"
              + " ORDER BY acct_month";

    setString(1,getValue("adcl.p_seqno"));
    setString(2,getValue("adcl.curr_code"));
    setString(3,getValue("acno.stmt_cycle"));
    setString(4,getValue("adcl.pay_date"));
    int n = selectTable();

    actCurrHstCnt = n;
    return n;
 }

public int selectActAcctHst() throws Exception
 {
//logs("ttt-444444 ");
    actAcctstFlag=1;
    daoTable    = "act_acct_hst";
    extendField = "acht.";
    selectSQL   = "acct_month,"
                + "to_char(add_months(to_date(acct_month,'yyyymm'),1),'yyyymm') as deba_month,"
                + "min_pay_bal,"
                + "waive_ttl_bal,"
                + "rowid as rowid ";
    whereStr =  " Where  p_seqno = ? "
             +  " AND  acct_month||? >= to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymmdd')"
             +  " ORDER BY acct_month";

    setString(1,getValue("adcl.p_seqno"));
    setString(2,getValue("acno.stmt_cycle"));
    setString(3,getValue("adcl.pay_date"));
    int n = selectTable();
    actCurrHstCnt = n;
    return n;
 }

public int selectActIntrA() throws Exception
 {
//logs("ttt-555555 ");
    daoTable    = "act_intr";
    extendField = "inta.";
    selectSQL   = "sum(inte_d_amt) as inte_d_amt,"
                + "sum(decode(decode(curr_code,'','901',curr_code),'901',inte_d_amt,dc_inte_d_amt)) as dc_inte_d_amt";
    whereStr    = "WHERE  p_seqno = ?"
                + " AND  decode(curr_code,'','901',curr_code) = ? "
                + " AND  interest_sign = '+' "
                + " AND  decode(acct_month,'','x',acct_month) = ?";
    setString(1,getValue("adcl.p_seqno"));
    setString(2,getValue("adcl.curr_code"));
    setString(3,getValue("deba.acct_month"));
    int n = selectTable();
    if ( n == 0 )
       { showLogMessage("E","","select_act_intr_a "+getValue("adcl.p_seqno")+"-"+getValue("adcl.curr_code")+"-"+getValue("deba.acct_month"));
         exitProgram(3); }
    return n;
}

public int selectActDebtA() throws Exception
 {
//logs("ttt-666666 ");
     double sumDAvailBal=0,sumDcAvailBal=0;
     int ret=0;

     extendField = "debm.";
     setValue("debt.p_seqno",getValue("adcl.p_seqno"));
     setValue("debt.curr_code",getValue("adcl.curr_code"));
     int n = getLoadData(debtKey);

     for ( int i=0; i<n; i++ ) {
           if ( !getValue("debm.acct_code",i).equals("RI")   ||
                !getValue("debm.bill_type",i).equals("OSSG") ||
                !getValue("debm.acct_month",i).equals(getValue("deba.acct_month")) )
              { continue; }
           sumDAvailBal  += getValueDouble("debm.d_avail_bal",i);
           sumDcAvailBal += getValueDouble("debm.dc_d_avail_bal",i);
           ret++;
        }

    setValueDouble("deba.d_avail_bal",sumDAvailBal);
    setValueDouble("deba.dc_avail_bal",sumDcAvailBal);
    return ret;
}

public int selectActAcctCurr1() throws Exception
{
//logs("ttt-777777 ");
    double  sumAcctJrnlBal=0,sumTtlAmtBal=0,sumMinPayBal=0,sumEndBalOp=0,sumEndBalLk=0;
    double  sumAdjustDrAmt=0,sumAdjustDrCnt=0,sumAdjustCrAmt=0,sumAdjustCrCnt=0;
    double  sumPayAmt=0,sumPayCnt=0,sumTempUnbillInterest=0,sumAutopayBal=0;
    long    numLong=0;

    extendField = "acu2.";
    setValue("acur.p_seqno",getValue("adcl.p_seqno"));
    int n = getLoadData(acurKey2);

    for ( int i=0; i<n; i++ ) {
          sumAcctJrnlBal        +=  getValueDouble("acu2.acct_jrnl_bal",i);
          sumTtlAmtBal          +=  getValueDouble("acu2.ttl_amt_bal",i);
          sumMinPayBal          +=  getValueDouble("acu2.min_pay_bal",i);
          sumEndBalOp           +=  getValueDouble("acu2.end_bal_op",i);
          sumEndBalLk           +=  getValueDouble("acu2.end_bal_lk",i);
          sumAdjustDrAmt        +=  getValueDouble("acu2.adjust_dr_amt",i);
          sumAdjustDrCnt        +=  getValueDouble("acu2.adjust_dr_cnt",i);
          sumAdjustCrAmt        +=  getValueDouble("acu2.adjust_cr_amt",i);
          sumAdjustCrCnt        +=  getValueDouble("acu2.adjust_cr_cnt",i);
          sumPayAmt              +=  getValueDouble("acu2.pay_amt",i);
          sumPayCnt              +=  getValueDouble("acu2.pay_cnt",i);
        //debug disp 01
        //showLogMessage("I","","adcl.p_seqno"+" : "+getValue("adcl.p_seqno"));
        //debug disp 02
        //showLogMessage("I","","acu2.temp_unbill_interest,i"+" : "+i+" "+getValueDouble("acu2.temp_unbill_interest",i));
          sumTempUnbillInterest +=  getValueDouble("acu2.temp_unbill_interest",i);
        //debug disp 03
        //showLogMessage("I","","sum_temp_unbill_interest"+" : "+sum_temp_unbill_interest);
          sumAutopayBal          +=  getValueDouble("acu2.autopay_bal",i);
          logs("ttt - sum "+sumMinPayBal);
       }

    setValueDouble("acct.acct_jrnl_bal",sumAcctJrnlBal);
    setValueDouble("acct.ttl_amt_bal",sumTtlAmtBal);
    setValueDouble("acct.min_pay_bal",sumMinPayBal);
    setValueDouble("acct.end_bal_op",sumEndBalOp);
    setValueDouble("acct.end_bal_lk",sumEndBalLk);
    setValueDouble("acct.adjust_dr_amt",sumAdjustDrAmt);
    setValueDouble("acct.adjust_dr_cnt",sumAdjustDrCnt);
    setValueDouble("acct.adjust_cr_amt",sumAdjustCrAmt);
    setValueDouble("acct.adjust_cr_cnt",sumAdjustCrCnt);
    setValueDouble("acct.pay_amt",sumPayAmt);
    setValueDouble("acct.pay_cnt",sumPayCnt);

  //num_long = (long)(sum_temp_unbill_interest * 100.0 + 0.5);
  //sum_temp_unbill_interest = (double)num_long / 100.0;
  //conv_amt_dp2r(x)除了校正微小誤差且有四捨五入到小數以下第2位
    sumTempUnbillInterest = convAmtDp2r(sumTempUnbillInterest);
  //debug disp 04
  //showLogMessage("I","","sum_temp_unbill_interest"+" : "+sum_temp_unbill_interest);
    setValueDouble("acct.temp_unbill_interest",sumTempUnbillInterest);
  //debug disp 05
  //showLogMessage("I","","acct.temp_unbill_interest"+" : "+getValue("acct.temp_unbill_interest"));
    sumAutopayBal = convAmtDp2r(sumAutopayBal);
    setValueDouble("acct.autopay_bal",sumAutopayBal);

    return n;
}

public int selectCrdCard() throws Exception
 {
//logs("ttt-888888 ");
    daoTable  = "crd_card c,ptr_card_type p,act_acno a";
    selectSQL = "count(*) as CNT";
    whereStr  = "where p.card_type  = c.card_type"
              + " and  p.card_note  = 'I'"
              + " and (c.current_code = '0' or c.reissue_status in ('1','2'))"
              + " and  c.p_seqno  = a.p_seqno"
              + " and  a.p_seqno  = ?"
              + " and  a.vip_code = '6S' ";  /* if act_acno.VIP_code is 6s then give NO_INTEREST_FLAG=Y by 皓宇 */

    setString(1,getValue("adcl.p_seqno"));
    selectTable();
    int  hICount = getValueInt("CNT");

    return hICount;
 }

public int selectActAcagCurr()  throws Exception
 {
//logs("ttt-999999 ");
    double tempDcDouble = getValueDouble("acur.dc_min_pay_bal");
    setValue("acur.min_pay_bal","0");

    daoTable    = "act_acag_curr";
    extendField = "aacr.";
    selectSQL   = "curr_code,"
                + "acct_month,"
                + "pay_amt,"
                + "dc_pay_amt,"
                + "rowid as rowid ";
    whereStr    = "WHERE   p_seqno   = ?"
                + " AND    curr_code = ?"
                + " ORDER  BY acct_month DESC";
    setString(1,getValue("adcl.p_seqno"));
    setString(2,getValue("adcl.curr_code"));
    int n = selectTable();
  //if ( n == 0 )
  //   { showLogMessage("E","","select_act_acag_curr ERROR "+getValue("adcl.p_seqno")+" "+getValue("adcl.curr_code")); /*exitProgram(3);*/ }

    for ( int i=0; i<n; i++)   {

         if ( getValueDouble("aacr.dc_pay_amt",i) == 0 || tempDcDouble == 0 )
            {
              deleteActAcagCurr(i);
              updateActAcag(i);
              continue;
            }

         if ( tempDcDouble >= getValueDouble("aacr.dc_pay_amt",i) )
            {
              tempDcDouble = tempDcDouble - getValueDouble("aacr.dc_pay_amt",i);
              setValueDouble("acur.min_pay_bal", getValueDouble("acur.min_pay_bal") + getValueDouble("aacr.pay_amt",i));
              continue;
            }

         double cvtData = tempDcDouble * (getValueDouble("aacr.pay_amt",i)/getValueDouble("aacr.dc_pay_amt",i));
         setValueDouble("aacr.pay_amt",comcr.commCurrAmt("901",cvtData,0),i);
         setValueDouble("acur.min_pay_bal", (getValueDouble("acur.min_pay_bal") + getValueDouble("aacr.pay_amt",i)));
         setValueDouble("aacr.dc_pay_amt",tempDcDouble,i);
         if ( getValue("adcl.curr_code").equals("901") )
            { setValue("aacr.pay_amt", getValue("aacr.dc_pay_amt",i),i); }
         if ( getValueDouble("aacr.dc_pay_amt",i) == 0 )
            { setValue("aacr.pay_amt","0",i); }
         tempDcDouble = 0;
         if ( getValueDouble("aacr.dc_pay_amt",i) <= 0  )
            { deleteActAcagCurr(i); }
         else
            { updateActAcagCurr(i); }
         updateActAcag(i);
       }

    return n;
 }

/******************* UPDATE *************/
 public int updateActDebtCancel() throws Exception
 {
//logs("ttt-aaaaaa ");

    updateSQL = "process_flag = 'Y',"
              + "mod_time     = sysdate,"
              + "mod_pgm      = ? ";
    daoTable  = "act_debt_cancel";
    whereStr  = "WHERE rowid = ? ";
    setString(1,javaProgram);
    setRowId(2,getValue("adcl.rowid"));
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_debt_cancel ERROR "+getValue("adcl.rowid")); return 0; }
    return n;
 }

public int updateActDebt1() throws Exception
 {
//logs("ttt-bbbbbb ");

    logs("ttt debt-reference_no-2 : "+ getValue("debt.reference_no",di));
    logs("ttt debt-end_bal        : "+ getValue("debt.end_bal",di));
    logs("ttt debt-dc_end_bal     : "+ getValue("debt.dc_end_bal",di));
    logs("ttt debt-d_avail_bal    : "+ getValue("debt.d_avail_bal",di));
    logs("ttt debt-dc_d_avail_bal : "+ getValue("debt.dc_d_avail_bal",di));

    daoTable    = "act_debt";
    extendField = "debt.";
    updateSQL   = "end_bal        = ?,"
                + "dc_end_bal     = ?,"
                + "d_avail_bal    = ?,"
                + "dc_d_avail_bal = ?,"
                + "mod_time       = sysdate,"
                + "mod_pgm        = ? ";
    whereStr    = "WHERE rowid    = ? ";
    setString(1,getValue("debt.end_bal",di));
    setString(2,getValue("debt.dc_end_bal",di));
    setString(3,getValue("debt.d_avail_bal",di));
    setString(4,getValue("debt.dc_d_avail_bal",di));
    setString(5,javaProgram);
    setRowId(6,getValue("debt.rowid",di));
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_debt_1 ERROR "+getValue("debt.rowid",di) ); return n; }

    /* UPDATE DEBT LOAD BUFFER */

   setUpdateLoad("debt.END_BAL",getValue("debt.end_bal",di));
   setUpdateLoad("debt.DC_END_BAL",getValue("debt.dc_end_bal",di));
   setUpdateLoad("debt.D_AVAIL_BAL",getValue("debt.d_avail_bal",di));
   setUpdateLoad("debt.DC_D_AVAIL_BAL",getValue("debt.dc_d_avail_bal",di));

   setValue("debt.p_seqno",getValue("adcl.p_seqno"));
   setValue("debt.curr_code",getValue("adcl.curr_code"));
   updateLoadTable(debtKey,di);

   return n;
}

public int updateActAcct() throws Exception
{
    logs("ttt-cccccc ");

    logs("ttt-after-acct acct_jrnl_bal "+getValue("acct.acct_jrnl_bal"));
    logs("ttt-after-acct "+getValue("acct.end_bal_op"));
    logs("ttt-after-acct "+getValue("acct.end_bal_lk"));
    logs("ttt-after-acct "+getValue("acct.temp_unbill_interest"));
    logs("ttt-after-acct "+getValue("acct.min_pay_bal"));
    logs("ttt-after-acct "+getValue("acct.rc_min_pay_bal"));
    logs("ttt-after-acct "+getValue("acct.rc_min_pay_m0"));
    logs("ttt-after-acct "+getValue("acct.autopay_bal"));
    logs("ttt-after-acct "+getValue("acct.pay_by_stage_bal"));
    logs("ttt-after-acct "+getValue("acct.pay_amt"));
    logs("ttt-after-acct "+getValue("acct.pay_cnt"));
    logs("ttt-after-acct "+getValue("acct.adjust_dr_amt"));
    logs("ttt-after-acct "+getValue("acct.adjust_dr_cnt"));
    logs("ttt-after-acct "+getValue("acct.adjust_cr_amt"));
    logs("ttt-after-acct "+getValue("acct.adjust_cr_cnt"));
    logs("ttt-after-acct "+getValue("acct.ttl_amt_bal"));
    logs("ttt-after-acct "+getValue("acct.adi_end_bal"));
    logs("ttt-after-acct "+getValue("acct.last_payment_date"));
    logs("ttt-after-acct "+getValue("acct.last_min_pay_date"));
    logs("ttt-after-acct "+getValue("acct.last_cancel_debt_date"));

   daoTable  =  "act_acct";
   updateSQL =  "acct_jrnl_bal         = ?,"
             +  "end_bal_op            = ?,"
             +  "end_bal_lk            = ?,"
             +  "temp_unbill_interest  = ?,"
             +  "min_pay_bal           = ?,"
             +  "rc_min_pay_bal        = ?,"
             +  "rc_min_pay_m0         = ?,"
             +  "autopay_bal           = ?,"
             +  "pay_by_stage_bal      = ?,"
             +  "pay_amt               = ?,"
             +  "pay_cnt               = ?,"
             +  "adjust_dr_amt         = ?,"
             +  "adjust_dr_cnt         = ?,"
             +  "adjust_cr_amt         = ?,"
             +  "adjust_cr_cnt         = ?,"
             +  "ttl_amt_bal           = ?,"
             +  "adi_end_bal           = ?,"
             +  "last_payment_date     = ?,"
             +  "last_min_pay_date     = ?,"
             +  "last_cancel_debt_date = ?,"
             +  "mod_time              = sysdate,"
             +  "mod_pgm               = ? ";
    whereStr = " where  rowid          = ? ";

    setDouble(1,getValueDouble("acct.acct_jrnl_bal"));
    setDouble(2,getValueDouble("acct.end_bal_op"));
    setDouble(3,getValueDouble("acct.end_bal_lk"));
    setDouble(4,getValueDouble("acct.temp_unbill_interest"));
    setDouble(5,getValueDouble("acct.min_pay_bal"));
    setDouble(6,getValueDouble("acct.rc_min_pay_bal"));
    setDouble(7,getValueDouble("acct.rc_min_pay_m0"));
    setDouble(8,getValueDouble("acct.autopay_bal"));
    setDouble(9,getValueDouble("acct.pay_by_stage_bal"));
    setDouble(10,getValueDouble("acct.pay_amt"));
    setDouble(11,getValueDouble("acct.pay_cnt"));
    setDouble(12,getValueDouble("acct.adjust_dr_amt"));
    setDouble(13,getValueDouble("acct.adjust_dr_cnt"));
    setDouble(14,getValueDouble("acct.adjust_cr_amt"));
    setDouble(15,getValueDouble("acct.adjust_cr_cnt"));
    setDouble(16,getValueDouble("acct.ttl_amt_bal"));
    setDouble(17,getValueDouble("acct.adi_end_bal"));
    setString(18,getValue("acct.last_payment_date"));
    setString(19,getValue("acct.last_min_pay_date"));
    setString(20,getValue("acct.last_cancel_debt_date"));
    setString(21,javaProgram);
    setRowId(22,getValue("acct.rowid"));
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_acct ERROR ");  }

    setUpdateLoad("acct.acct_jrnl_bal",getValue("acct.acct_jrnl_bal"));
    setUpdateLoad("acct.end_bal_op",getValue("acct.end_bal_op"));
    setUpdateLoad("acct.end_bal_lk",getValue("acct.end_bal_lk"));
    setUpdateLoad("acct.temp_unbill_interest",getValue("acct.temp_unbill_interest"));
    setUpdateLoad("acct.min_pay_bal",getValue("acct.min_pay_bal"));
    setUpdateLoad("acct.rc_min_pay_bal",getValue("acct.rc_min_pay_bal"));
    setUpdateLoad("acct.rc_min_pay_m0",getValue("acct.rc_min_pay_m0"));
    setUpdateLoad("acct.autopay_bal",getValue("acct.autopay_bal"));
    setUpdateLoad("acct.pay_by_stage_bal",getValue("acct.pay_by_stage_bal"));
    setUpdateLoad("acct.pay_amt",getValue("acct.pay_amt"));
    setUpdateLoad("acct.pay_cnt",getValue("acct.pay_cnt"));
    setUpdateLoad("acct.adjust_dr_amt",getValue("acct.adjust_dr_amt"));
    setUpdateLoad("acct.adjust_dr_cnt",getValue("acct.adjust_dr_cnt"));
    setUpdateLoad("acct.adjust_cr_amt",getValue("acct.adjust_cr_amt"));
    setUpdateLoad("acct.adjust_cr_cnt",getValue("acct.adjust_cr_cnt"));
    setUpdateLoad("acct.ttl_amt_bal",getValue("acct.ttl_amt_bal"));
    setUpdateLoad("acct.adi_end_bal",getValue("acct.adi_end_bal"));
    setUpdateLoad("acct.last_payment_date",getValue("acct.last_payment_date"));
    setUpdateLoad("acct.last_min_pay_date",getValue("acct.last_min_pay_date"));
    setUpdateLoad("acct.last_cancel_debt_date",getValue("acct.last_cancel_debt_date"));

    setValue("acct.p_seqno",getValue("adcl.p_seqno"));
    updateLoadTable(acctKey);

    return n;
}

public int updateActAcctCurr() throws Exception
{
logs("ttt-dddddd ");
//int ttt=0;
//if ( ttt == 0 ) { return 0; }

   daoTable = "act_acct_curr";
   updateSQL = "end_bal_op              = ?,"
             + "dc_end_bal_op           = ?,"
             + "acct_jrnl_bal           = ?,"
             + "dc_acct_jrnl_bal        = ?,"
             + "end_bal_lk              = ?,"
             + "dc_end_bal_lk           = ?,"
             + "temp_unbill_interest    = ?,"
             + "dc_temp_unbill_interest = ?,"
             + "min_pay_bal             = ?,"
             + "dc_min_pay_bal          = ?,"
             + "autopay_bal             = ?,"
             + "dc_autopay_bal          = ?,"
             + "adjust_dr_amt           = ?,"
             + "dc_adjust_dr_amt        = ?,"
             + "adjust_dr_cnt           = ?,"
             + "dc_adjust_cr_amt        = ?,"
             + "adjust_cr_cnt           = ?,"
             + "pay_cnt                 = ?,"
             + "pay_amt                 = ?,"
             + "dc_pay_amt              = ?,"
             + "ttl_amt_bal             = ?,"
             + "dc_ttl_amt_bal          = ?,"
             + "last_pay_date           = ?,"
             + "delaypay_ok_flag        = ?,"
             + "mod_time                = sysdate,"
             + "mod_pgm                 = ? ";
    whereStr = "where rowid = ? ";

    setString(1,getValue("acur.end_bal_op"));
    setString(2,getValue("acur.dc_end_bal_op"));
    setString(3,getValue("acur.acct_jrnl_bal"));
    setString(4,getValue("acur.dc_acct_jrnl_bal"));
    setString(5,getValue("acur.end_bal_lk"));
    setString(6,getValue("acur.dc_end_bal_lk"));
    setString(7,getValue("acur.temp_unbill_interest"));
    setString(8,getValue("acur.dc_temp_unbill_interest"));
    setString(9,getValue("acur.min_pay_bal"));
    setString(10,getValue("acur.dc_min_pay_bal"));
    setString(11,getValue("acur.autopay_bal"));
    setString(12,getValue("acur.dc_autopay_bal"));
    setString(13,getValue("acur.adjust_dr_amt"));
    setString(14,getValue("acur.dc_adjust_dr_amt"));
    setString(15,getValue("acur.adjust_dr_cnt"));
    setString(16,getValue("acur.dc_adjust_cr_amt"));
    setString(17,getValue("acur.adjust_cr_cnt"));
    setString(18,getValue("acur.pay_cnt"));
    setString(19,getValue("acur.pay_amt"));
    setString(20,getValue("acur.dc_pay_amt"));
    setString(21,getValue("acur.ttl_amt_bal"));
    setString(22,getValue("acur.dc_ttl_amt_bal"));
    setString(23,getValue("acur.last_pay_date"));
    setString(24,getValue("acur.delaypay_ok_flag"));
    setString(25,javaProgram);
    setRowId(26,getValue("acur.rowid"));
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_acct_curr ERROR ");  }

   /* UPDATE ACT_ACCT_CURR LOAD BUFFER */
    setUpdateLoad("acur.end_bal_op",getValue("acur.end_bal_op"));
    setUpdateLoad("acur.dc_end_bal_op",getValue("acur.dc_end_bal_op"));
    setUpdateLoad("acur.acct_jrnl_bal",getValue("acur.acct_jrnl_bal"));
    setUpdateLoad("acur.dc_acct_jrnl_bal",getValue("acur.dc_acct_jrnl_bal"));
    setUpdateLoad("acur.end_bal_lk",getValue("acur.end_bal_lk"));
    setUpdateLoad("acur.dc_end_bal_lk",getValue("acur.dc_end_bal_lk"));
    setUpdateLoad("acur.temp_unbill_interest",getValue("acur.temp_unbill_interest"));
    setUpdateLoad("acur.dc_temp_unbill_interest",getValue("acur.dc_temp_unbill_interest"));
    setUpdateLoad("acur.min_pay_bal",getValue("acur.min_pay_bal"));
    setUpdateLoad("acur.dc_min_pay_bal",getValue("acur.dc_min_pay_bal"));
    setUpdateLoad("acur.autopay_bal",getValue("acur.autopay_bal"));
    setUpdateLoad("acur.dc_autopay_bal",getValue("acur.dc_autopay_bal"));
    setUpdateLoad("acur.adjust_dr_amt",getValue("acur.adjust_dr_amt"));
    setUpdateLoad("acur.dc_adjust_dr_amt",getValue("acur.dc_adjust_dr_amt"));
    setUpdateLoad("acur.adjust_dr_cnt",getValue("acur.adjust_dr_cnt"));
    setUpdateLoad("acur.dc_adjust_cr_amt",getValue("acur.dc_adjust_cr_amt"));
    setUpdateLoad("acur.adjust_cr_cnt",getValue("acur.adjust_cr_cnt"));
    setUpdateLoad("acur.pay_cnt",getValue("acur.pay_cnt"));
    setUpdateLoad("acur.pay_amt",getValue("acur.pay_amt"));
    setUpdateLoad("acur.dc_pay_amt",getValue("acur.dc_pay_amt"));
    setUpdateLoad("acur.ttl_amt_bal",getValue("acur.ttl_amt_bal"));
    setUpdateLoad("acur.dc_ttl_amt_bal",getValue("acur.dc_ttl_amt_bal"));
    setUpdateLoad("acur.last_pay_date",getValue("acur.last_pay_date"));
    setUpdateLoad("acur.delaypay_ok_flag",getValue("acur.delaypay_ok_flag"));

    logs("ttt =================1 "+getValue("acur.acct_jrnl_bal"));
    setValue("acur.p_seqno",getValue("adcl.p_seqno"));
    setValue("acur.curr_code",getValue("adcl.curr_code"));
    updateLoadTable(acurKey);

    // 重 新讀 ACT_ACCT_CURR
    setValue("acur.p_seqno",getValue("adcl.p_seqno"));
    setValue("acur.curr_code",getValue("adcl.curr_code"));
    getLoadData(acurKey);
    logs("ttt =================2 "+getValue("acur.acct_jrnl_bal"));
    return n;
}

public int updateActIntr() throws Exception
 {
//logs("ttt-eeeeee ");
    daoTable     = "act_intr";
    updateSQL    = "inte_d_amt    = ?,"
                 + "dc_inte_d_amt = ?,"
                 + "mod_time      = sysdate,"
                 + "mod_pgm       = ? ";
    whereStr     = "where  rowid  = ?";
    setDouble(1,getValueDouble("intr.inte_d_amt"));
    setDouble(2,getValueDouble("intr.dc_inte_d_amt"));
    setString(3,javaProgram);
    setRowId(4,getValue("intr.rowid"));
    int  n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_intr ERROR "); /* exitProgram(3); */ }
    return n;
 }

 public int updateActIntrA() throws Exception
 {
//logs("ttt-ffffff ");
   daoTable  = "act_intr";
   updateSQL = "inte_d_amt     = 0,"
             + "dc_inte_d_amt  = 0,"
             + "mod_time       = sysdate,"
             + "mod_pgm        = ?";
   whereStr  = "where  p_seqno = ?"
             + " and   interest_sign  = '+' "
             + " AND   decode(curr_code,'','901',curr_code) = ?"
             + " and   decode(acct_month,'','x',acct_month)  = ?";
   setString(1,javaProgram);
   setString(2,getValue("adcl.p_seqno"));
   setString(3,getValue("adcl.curr_code"));
   setString(4,getValue("deba.acct_month"));
   int n = updateTable();
   if ( n == 0 )
      { showLogMessage("E","","update_act_intr_a ERROR ");   }
   return n;
 }

 public int updateActAcctCurr1() throws Exception
 {
//logs("ttt-gggggg ");
     daoTable  = "act_acct_curr";
     updateSQL =  "no_interest_flag    = 'Y',"
               +  "no_interest_s_month = ?,"
               +  "no_interest_e_month = ?,"
               +  "mod_time            = sysdate,"
               +  "mod_user            = ?,"
               +  "mod_pgm             = ?";
     whereStr  =  "WHERE p_seqno       = ?"
               +  " AND  curr_code     = ?"
               +  " AND  (decode(no_interest_flag,'','N',no_interest_flag) != 'Y'"
               +  " or   (decode(no_interest_flag,'','N',no_interest_flag) = 'Y'"
               +  " and  ? not between decode(no_interest_s_month,'','000000',no_interest_s_month)"
               +  "       and decode(no_interest_e_month,'','999999',no_interest_e_month)))";

     setString(1,getValue("wday.next_acct_month"));
     setString(2,getValue("wday.next_acct_month"));
     setString(3,getValue("acct.mod_user"));
     setString(4,javaProgram);
     setString(5,getValue("adcl.p_seqno"));
     setString(6,getValue("adcl.curr_code"));
     setString(7,getValue("wday.next_acct_month"));
     int n = updateTable();
   //if ( n == 0 )
   //   { showLogMessage("E","","update_act_acct_curr_1 ERROR ");  }
     return n;
 }

public int updateActCurrHst() throws Exception
 {
//logs("ttt-hhhhhh ");
    daoTable  = "act_curr_hst";
    updateSQL = "waive_ttl_bal = waive_ttl_bal - ?,"
              + " mod_time    = sysdate,"
              + " mod_pgm     = ?";
    whereStr  = " where rowid  = ? ";
    setString(1,getValue("adcl.dc_pay_amt"));
    setString(2,javaProgram);
    setRowId(3,getValue("acht.rowid"));
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_curr_hst ERROR ");  }
    return n;
  }

public int updateActAcctHst() throws Exception
 {
//logs("ttt-iiiiii ");
    daoTable     = "act_acct_hst";
    updateSQL    = "waive_ttl_bal  = waive_ttl_bal - ?,"
                 + "mod_time       = sysdate,"
                 + "mod_pgm        = ?";
    whereStr     = "where rowid    = ?";
    setDouble(1,getValueDouble("adcl.dc_pay_amt"));
    setString(2,javaProgram);
    setRowId(3,getValue("acht.rowid"));
    int  n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_act_acct_hst ERROR ");  }
    return n;
 }

public int updateActAcag(int i) throws Exception
 {
//logs("ttt-jjjjjj ");

   extendField = "temp.";
   daoTable    = "act_acag_curr";
   selectSQL   = "sum(pay_amt) as  pay_amt ";
   whereStr    = "WHERE p_seqno    = ? "
               + "AND   acct_month = ?";
   setString(1,getValue("adcl.p_seqno"));
   setString(2,getValue("aacr.acct_month",i));
   selectTable();

   double tempAmount = getValueDouble("temp.pay_amt");

   daoTable  = "act_acag";
   updateSQL = "pay_amt = ? ";
   whereStr  = "WHERE p_seqno    = ? "
             + "AND   acct_month = ? ";

   setString(1,""+tempAmount);
   setString(2,getValue("adcl.p_seqno"));
   setString(3,getValue("aacr.acct_month",i));
   int n = updateTable();
   if ( n > 0 )
      { return n; }

   showLogMessage("I", "", String.format("insertActAcag-> p_seqno:[%s], acct_month:[%s], pay_amt:[%f]", 
   getValue("adcl.p_seqno"),getValue("aacr.acct_month",i),tempAmount));

   insertActAcag(i);

   return 0;
 }

public int insertActAcag(int i) throws Exception
 {
//logs("ttt-kkkkkk ");
   sqlCmd =  "insert into act_acag ("
          +  "p_seqno,"
        //+  "seq_no,"
          +  "acct_type,"
          +  "acct_month,"
          +  "stmt_cycle,"
          +  "pay_amt,"
          +  "mod_time,"
          +  "mod_pgm )"
          + "select p_seqno,"
        //+ "max(0),"
          + "max(acct_type),"
          + "acct_month,"
          + "max(stmt_cycle),"
          + "sum(pay_amt),"
          + "max(sysdate),"
          + "max('ActE004') "
          + "FROM   act_acag_curr "
          + "WHERE  p_seqno    = ? "
          + "AND    acct_month = ? "
          + "GROUP BY p_seqno,acct_month ";
   setString(1,getValue("adcl.p_seqno"));
   setString(2,getValue("aacr.acct_month",i));
   int n = executeSqlCommand(sqlCmd);

   return n;
 }

public int updateActAcagCurr(int i) throws Exception
 {
//logs("ttt-LLLLLL ");
   daoTable  = "act_acag_curr";
   updateSQL = "pay_amt    = ?,"
             + "dc_pay_amt = ?";
   whereStr = "WHERE  rowid = ? ";
   setDouble(1,getValueDouble("aacr.pay_amt",i));
   setDouble(2,getValueDouble("aacr.dc_pay_amt",i));
   setRowId(3,getValue("aacr.rowid",i));
   int n = updateTable();
   if ( n == 0 )
      { showLogMessage("E","","update_act_acag_curr ERROR");  }

   return n;
 }

public int updateActAcno(String idCode) throws Exception
{
//logs("ttt-mmmmmm ");
   daoTable  = "act_acno";
   updateSQL = "last_pay_amt  = ?,"
             + "last_pay_date = ?,"
             + "mod_time      = sysdate,"
             + "mod_pgm       = ? ";
   whereStr  = "where  rowid  = ? ";
   setDouble(1,getValueDouble("acno.last_pay_amt"));
   setString(2,getValue("acno.last_pay_date"));
   setString(3,javaProgram);
   setRowId(4,getValue("acno.rowid"));
   specialSQL = idCode;
   int n = updateTable();
   if ( n == 0 )
      { showLogMessage("E","","update_act_acno ERROR ");  }

   return n;
 }

/******************* INSERT *************/
public void insertActVouchData(String hInt) throws Exception
{
//logs("ttt-nnnnnn ");
   daoTable="act_vouch_data";
   extendField = "avda.";

   setValue("avda.crt_date",sysDate);
   setValue("avda.crt_time",sysTime);
   setValue("avda.business_date",getValue("busi.business_date"));
   setValue("avda.curr_code",getValue("adcl.curr_code"));
   setValue("avda.p_seqno",getValue("adcl.p_seqno"));
   setValue("avda.acct_type",getValue("adcl.acct_type"));
   setValue("avda.vouch_data_type",hInt);
   setValue("avda.recourse_mark",getValue("acno.recourse_mark"));

   setValue("avda.payment_type",getValue("adcl.payment_type"));
   setValue("avda.pay_amt",getValue("adcl.pay_amt"));
   setValue("avda.pay_card_no",getValue("adcl.pay_card_no"));
   setValue("avda.pay_date",getValue("adcl.pay_date"));

   setValue("avda.src_pgm",javaProgram);
   setValue("avda.proc_flag","N");
   setValue("avda.mod_pgm",javaProgram);
   setValue("avda.mod_time",sysDate+sysTime);

   insertTable();
   return;
 }

public void insertActComboCcs() throws Exception
 {
//showLogMessage("D","","ttt-oooooo ");
    String lsCardHldrId = "", lsAcctTypeKey = "", lsCorpNo = "", lsIdPSeqno = "";

    if ( getValue("acno.acno_flag").equals("1") ) {
         lsCardHldrId = getValue("acno.acct_key").substring(0,10);
    } else if ( getValue("acno.acno_flag").equals("3")) {
         lsIdPSeqno   = getValue("acno.id_p_seqno");
         lsCardHldrId = selectCrdIdno(lsIdPSeqno);
    } else {
         lsCardHldrId = "";
    }

    lsAcctTypeKey = getValue("acno.acct_key");

    daoTable  = "act_combo_ccs";
    extendField = "comb.";

    setValue("comb.trans_type","1");
    setValue("comb.to_which","2");
    setValue("comb.dog",sysDate+sysTime);
    setValue("comb.process_mode","B");
    setValue("comb.process_status","0");
    setValue("comb.card_catalog","1");
    setValue("comb.payment_type","1");
    setValue("comb.account_type",getValue("adcl.acct_type"));
    //setValue("comb.card_hldr_id",getValue("adcl.acct_key"));
    //setValue("comb.card_acct_id",getValue("adcl.acct_key"));
    setValue("comb.card_hldr_id",lsCardHldrId);
    setValue("comb.card_acct_id",lsAcctTypeKey);
    setValue("comb.card_no",getValue("debt.card_no",di));
    setValue("comb.trans_amt",getValue("intr.intr_org_captial"));
    setValue("comb.mcc_code","CASH");
    setValue("comb.reference_no",getValue("debt.reference_no",di));
    setValue("comb.trans_code","03");

    insertTable();
    return;
}

public String selectCrdIdno(String txIdPSeqno) throws Exception
 {
    String lsIdNo = "";
    daoTable  = " crd_idno ";
    selectSQL = " id_no ";
    whereStr  = " where id_p_seqno  = ? ";

    setString(1,txIdPSeqno);
    int n = selectTable();
    if ( n > 0 ) {
       lsIdNo = getValue("id_no");
       return lsIdNo;
    } else {
       return "";
    }
 }


public void insertCycPyaj(String h_int) throws Exception
 {
//logs("ttt-pppppp ");
    daoTable    = "cyc_pyaj";
    extendField = "pyaj.";

    setValue("pyaj.p_seqno",getValue("adcl.p_seqno"));
    setValue("pyaj.curr_code",getValue("adcl.curr_code"));
    setValue("pyaj.acct_type",getValue("adcl.acct_type"));
    setValue("pyaj.payment_date",getValue("adcl.pay_date"));
    setValue("pyaj.stmt_cycle",getValue("acno.stmt_cycle"));
    setValue("pyaj.settle_flag","U");
  //setValue("pyaj.crt_date",sysDate);
    setValue("pyaj.crt_date",getValue("busi.business_date"));
    setValue("pyaj.mod_pgm",javaProgram);
    setValue("pyaj.mod_time",sysDate+sysTime);

    if ( h_int.equals("1") ) {
         if ( getValue("adcl.batch_no").substring(8,12).equals("9005") )
            { setValue("pyaj.class_code","B"); }
         else
         if ( getValue("adcl.batch_no").substring(8,12).equals("9009") )
            { setValue("pyaj.class_code","B"); }
         else
            { setValue("pyaj.class_code","P"); }

         setValue("pyaj.payment_amt",getValue("adcl.pay_amt"));
         setValue("pyaj.dc_payment_amt",getValue("adcl.dc_pay_amt"));
         setValue("pyaj.payment_type",getValue("adcl.payment_type"));
         setValue("pyaj.fee_flag","N");
    } else {
         setValue("pyaj.class_code","A");
         setValue("pyaj.payment_amt",""+(getValueDouble("jrnl.transaction_amt") * -1));
         setValue("pyaj.dc_payment_amt",""+(getValueDouble("jrnl.dc_transaction_amt") * -1));
         setValue("pyaj.payment_type",getValue("jrnl.tran_type"));
         setValue("pyaj.fee_flag","Y");
    }

  insertTable();
  return;
}

public void insertActIntr(String h_int) throws Exception
 {
//logs("ttt-qqqqqq ");
    daoTable    = "act_intr";
    extendField = "intr.";

    hIntrEnqSeqno++;
    setValue("intr.create_date",sysDate);
    setValue("intr.create_time",sysTime);
    setValue("intr.enq_seqno",""+hIntrEnqSeqno);
    setValue("intr.p_seqno",getValue("adcl.p_seqno"));
    setValue("intr.curr_code",getValue("adcl.curr_code"));
    setValue("intr.acct_type",getValue("adcl.acct_type"));
    setValue("intr.post_date",getValue("busi.business_date"));
    setValue("intr.stmt_cycle",getValue("acno.stmt_cycle"));
    setValue("intr.acct_month",getValue("wday.next_acct_month"));

    setValue("intr.mod_time",sysDate+sysTime);
    setValue("intr.mod_pgm",javaProgram);
    setValue("intr.crt_date",sysDate);
    setValue("intr.crt_time",sysTime);
    setValue("intr.crt_user",javaProgram);
    if ( h_int.equals("0") ) {
         setValue("intr.interest_sign","+");
         setValue("intr.inte_d_amt",getValue("intr.interest_amt"));
         setValue("intr.dc_inte_d_amt",getValue("intr.dc_interest_amt"));
         setValue("intr.reason_code","DB00");
         setValue("intr.interest_rate",getValue("intr.interest_rate"));
         setValue("intr.reference_no",getValue("debt.reference_no",di));
         setValue("intr.ao_flag",getValue("debt.ao_flag",di));
       }
    else
    if ( h_int.equals("1") ) {
         setValue("intr.interest_sign","-");
         setValue("intr.inte_d_amt","0");
         setValue("intr.dc_inte_d_amt","0");
         setValue("intr.reason_code","DB02");
         setValue("intr.interest_rate","");
         setValue("intr.reference_no","");
         setValue("intr.ao_flag","");

       }
    else {
         setValue("intr.interest_sign","-");
         setValue("intr.inte_d_amt",getValue("intr.inte_d_amt"));
         setValue("intr.dc_inte_d_amt",getValue("intr.dc_inte_d_amt"));
         setValue("intr.reason_code","DB02");
         setValue("intr.interest_rate",getValue("intr.interest_rate"));
         setValue("intr.reference_no",getValue("debt.reference_no",di));
         setValue("intr.ao_flag",getValue("debt.ao_flag",di));
       }

 insertTable();
 return;
}

void insertActJrnl(String hInt) throws Exception
{
//logs("ttt-rrrrrr ");
   daoTable    = "act_jrnl";
   extendField = "jrnl.";

   double hJrnlDcJrnlBal = getValueDouble("acur.dc_acct_jrnl_bal")
                             - totDcRealPayAmt
                             - totDcRealWaiveAmt
                             - dcPayOverAmt
                             + dcOrgOverAmt;
   hJrnlDcJrnlBal =  convAmtDp2r(hJrnlDcJrnlBal);

  logs("ttt dc-acct_jrnl_bal -1" + getValueDouble("acur.dc_acct_jrnl_bal"));
  logs("ttt dc-acct_jrnl_bal -2" + totDcRealPayAmt);
  logs("ttt dc-acct_jrnl_bal -3" + totDcRealWaiveAmt);
  logs("ttt dc-acct_jrnl_bal -4" + dcPayOverAmt);
  logs("ttt dc-acct_jrnl_bal -5" + dcOrgOverAmt);

  double  hJrnlJrnlBal   = getValueDouble("acur.acct_jrnl_bal")
                            - totRealPayAmt
                            - totRealWaiveAmt
                            - payOverAmt
                            + orgOverAmt;
  hJrnlJrnlBal =  convAmtDp2r(hJrnlJrnlBal);

  logs("ttt acct_jrnl_bal -1" +getValueDouble("acur.acct_jrnl_bal"));
  logs("ttt acct_jrnl_bal -2" +totRealPayAmt);
  logs("ttt acct_jrnl_bal -3" +totRealWaiveAmt);
  logs("ttt acct_jrnl_bal -4" +payOverAmt);
  logs("ttt acct_jrnl_bal -5" +orgOverAmt);

  if ( collectionFlag == 1 )
     { hJrnlDcJrnlBal = hJrnlJrnlBal = 0; }

   hIntrEnqSeqno++;
   hJrnlOrderSeq++;

   setValue("jrnl.p_seqno",getValue("adcl.p_seqno"));
   setValue("jrnl.curr_code",getValue("adcl.curr_code"));
   setValue("jrnl.acct_type",getValue("adcl.acct_type"));
   setValue("jrnl.corp_p_seqno",getValue("acno.corp_p_seqno"));
   setValue("jrnl.id_p_seqno",getValue("acno.id_p_seqno"));
   setValue("jrnl.crt_date",sysDate);
   setValue("jrnl.crt_time",sysTime);
   setValue("jrnl.enq_seqno",""+hIntrEnqSeqno);
   setValue("jrnl.vouch_data_type",hInt);
   setValue("jrnl.acct_date",getValue("busi.business_date"));
   if ( hInt.equals("1") )
      { setValue("jrnl.tran_class","P"); }
   else
   if ( hInt.equals("2") || hInt.equals("3") )
      { setValue("jrnl.tran_class","D"); }

   if ( hInt.equals("1") )
      { setValue("jrnl.acct_code","PY"); }
   else
   if ( hInt.equals("2") )
      { setValue("jrnl.acct_code",getValue("debt.acct_code",di)); }
   else
   if ( hInt.equals("3") )
      { setValue("jrnl.acct_code","AI"); }

   setValue("jrnl.dr_cr","D");

   if ( hInt.equals("1") ) {
       setValue("jrnl.jrnl_bal",""+hJrnlJrnlBal);
       setValue("jrnl.dc_jrnl_bal",""+hJrnlDcJrnlBal);
       setValue("jrnl.item_bal","0");
       setValue("jrnl.dc_item_bal","0");
       setValue("jrnl.item_d_bal","0");
       setValue("jrnl.dc_item_d_bal","0");
       setValue("jrnl.item_date","");
       setValue("jrnl.interest_date",getValue("adcl.pay_date"));
       setValue("jrnl.reference_no","");
       setValue("jrnl.c_debt_key",getValue("adcl.debt_key"));
       setValue("jrnl.debit_item",getValue("adcl.debit_item"));
     }
   else if ( hInt.equals("2") ) {
       setValue("jrnl.jrnl_bal","0");
       setValue("jrnl.dc_jrnl_bal","0");
       setValue("jrnl.item_bal",getValue("debt.end_bal",di));
       setValue("jrnl.dc_item_bal",getValue("debt.dc_end_bal",di));
       setValue("jrnl.item_d_bal",getValue("debt.d_avail_bal",di));
       setValue("jrnl.dc_item_d_bal",getValue("debt.dc_d_avail_bal",di));
       setValue("jrnl.item_date",getValue("debt.post_date",di));
       setValue("jrnl.interest_date",getValue("debt.interest_date",di));
       setValue("jrnl.reference_no",getValue("debt.reference_no",di));
       setValue("jrnl.c_debt_key","");
       setValue("jrnl.debit_item","");
    }
   else {
       setValue("jrnl.jrnl_bal","0");
       setValue("jrnl.dc_jrnl_bal","0");
       setValue("jrnl.item_bal",getValue("acct.adi_end_bal"));
       setValue("jrnl.dc_item_bal",getValue("acct.adi_end_bal"));
       setValue("jrnl.dc_item_d_bal",getValue("acct.adi_d_avail"));
       setValue("jrnl.item_date",getValue("busi.business_date"));
       setValue("jrnl.interest_date",getValue("busi.business_date"));
       setValue("jrnl.reference_no","");
       setValue("jrnl.c_debt_key","");
       setValue("jrnl.debit_item","");
    }

   setValue("jrnl.pay_id",getValue("adcl.pay_card_no"));
   setValue("jrnl.stmt_cycle",getValue("acno.stmt_cycle"));
   setValue("jrnl.jrnl_seqno",""+hJrnlJrnlSeqno);
   setValue("jrnl.order_seq",""+hJrnlOrderSeq);
   setValue("jrnl.batch_no",getValue("adcl.batch_no"));
   setValue("jrnl.serial_no",getValue("adcl.serial_no"));
   setValue("jrnl.mod_pgm",javaProgram);
   setValue("jrnl.mod_time",sysDate+sysTime);

   insertTable();
  return;

}

/******************* DELETE *************/
public int deleteActAcag() throws Exception
 {
//logs("ttt-ssssss ");
   daoTable = "act_acag";
   whereStr = "WHERE p_seqno = ? and pay_amt = 0";
   setString(1,getValue("adcl.p_seqno"));
   int n = deleteTable();

   return n;
 }

public int deleteActAcagCurr(int i) throws Exception
 {
//logs("ttt-tttttt ");

   daoTable = "act_acag_curr";
   whereStr = "WHERE rowid = ? ";
   setRowId(1,getValue("aacr.rowid",i));
   int n = deleteTable();

   return n;
 }

 /*** conv_amt_dp2r(x) 有以下兩點作用：
  1.校正微小誤差：double 變數運算後會發生 .99999999...的問題，例如 19.125, 
    實際會變成 19.1249999999999999...，所以執行 conv_amt_dp2r(x)變成 19.13
  2.四捨五入到小數以下第二位
 ***/
 public double  convAmtDp2r(double cvtAmt) throws Exception
 {
   long   cvtLong   = (long) Math.round(cvtAmt * 100.0 + 0.00001);
   double cvtDouble =  ((double) cvtLong) / 100;
   return cvtDouble;
 }

/******************* LOAD *************/
 public void  loadActDebt() throws Exception
 {
    seqnoRange = " select distinct h.p_seqno from  act_debt_cancel h"
               + " where  substr(h.batch_no,9,4)  != '9008' "
               + " and    decode(h.process_flag,'','N',h.process_flag) != 'Y' ";
  
    //if ( debugFlag.equals("Y"))
    //   { seqnoRange = "'" + test_p_seqno + "'"; }

    daoTable    = "act_debt";
    extendField = "debt.";
    sqlCmd = "SELECT "
           + "p_seqno,"
           + "decode(curr_code,'','901',curr_code) as curr_code,"
           + "reference_no,"
           + "post_date,"
           + "item_order_normal,"
           + "item_order_back_date,"
           + "item_order_refund,"
           + "acct_month,"
           + "stmt_cycle,"
           + "bill_type,"
           + "beg_bal,"
           + "decode(decode(curr_code,'','901',curr_code),'901',beg_bal,dc_beg_bal) as dc_beg_bal,"
           + "end_bal,"
           + "decode(decode(curr_code,'','901',curr_code),'901',end_bal,dc_end_bal) as dc_end_bal,"
           + "d_avail_bal,"
           + "decode(decode(curr_code,'','901',curr_code),'901',d_avail_bal,dc_d_avail_bal) as dc_d_avail_bal,"
           + "card_no,"
           + "acct_code,"
           + "interest_date,"
           + "interest_rs_date,"
           + "purchase_date,"
           + "decode(ao_flag,'','N',ao_flag) as ao_flag,"
           + "decode(decode(installment_kind,'','N',installment_kind),'N',decode(mcht_no,'','N',mcht_no),decode(ptr_merchant_no,'',decode(mcht_no,'','N',mcht_no),ptr_merchant_no)) as merchant_no,"
           + "int_rate,"
           + "int_rate_flag,"
           + "new_it_flag,"
           + "item_order_normal    || decode(new_it_flag,'Y','0','1')  as sort_order_normal,"
           + "item_order_back_date || decode(new_it_flag,'Y','0','1')  as sort_order_back_date,"
           + "item_order_refund    || decode(new_it_flag,'Y','0','1')  as sort_order_refund,"
           + "post_date as sort_post_date,"
           + "decode(acct_code_type,'C','3','I','2','B','1','4')       as sort_code_type,"
           + "item_class_normal     as sort_class_normal,"
           + "item_class_back_date  as sort_class_back_date,"
           + "item_class_refund     as sort_class_refund,"
           + "rowid as rowid "
           + "FROM  act_debt "
           + "WHERE p_seqno in ( "+seqnoRange+ ") "
           + "and   p_seqno >= ? and p_seqno < ? ";
   setString(1,hBeginKey);
   setString(2,hStopKey);
    if (pSeqno.length()>0)
       {
        sqlCmd    = sqlCmd
                  + "and  p_seqno = ?  ";
        setString(3 , pSeqno);
       }
    sqlCmd = sqlCmd
           + "order by p_seqno,curr_code";
   loadTable();
   debtLoadCnt = getLoadCnt();
   showLogMessage("I","","Load act_debt Count: ["+debtLoadCnt+"]");
   setLoadData(debtKey);
   return;
 }

 public int  loadActAcno() throws Exception
 {
   daoTable = "act_acno";
   extendField = "acno.";
   sqlCmd    = "SELECT "
             + "acct_type,"
             + "acct_key,"
             + "p_seqno,"
             + "corp_p_seqno,"
             + "acno_flag,"
             + "acct_status,"
             + "stmt_cycle,"
             + "id_p_seqno,"
             + "no_cancel_debt_flag,"
             + "no_cancel_debt_s_date,"
             + "decode(no_cancel_debt_e_date,'','99991231',no_cancel_debt_e_date) as no_cancel_debt_e_date,"
             + "pay_by_stage_flag,"
             + "decode(revolve_int_sign,'+',revolve_int_rate,revolve_int_rate * -1) as revolve_int_rate,"
             + "revolve_rate_s_month,"
             + "decode(revolve_rate_e_month,'','999912',revolve_rate_e_month) as revolve_rate_e_month,"
             + "decode(batch_int_sign,'+',batch_int_rate,batch_int_rate * -1) as batch_int_rate,"
             + "batch_rate_s_month,"
             + "decode(batch_rate_e_month,'','999912',batch_rate_e_month) as batch_rate_e_month,"
             + "decode(group_int_sign,'+',group_int_rate,group_int_rate * -1) as group_int_rate,"
             + "group_rate_s_month,"
             + "decode(group_rate_e_month,'','999912',group_rate_e_month) as group_rate_e_month,"
             + "aox_int_rate,"
             + "aox_rate_date,"
             + "aox_rate_s_month,"
             + "aox_rate_e_month,"
             + "new_bill_flag,"
             + "last_pay_amt,"
             + "last_pay_date,"
             + "recourse_mark,"
             + "new_cycle_month,"
             + "last_interest_date,"
             + "ao_posting_date,"
             + "vip_code,"
             + "int_rate_mcode,"
             + "rowid rowid "
             + "FROM act_acno "
             + "WHERE p_seqno in ( "+seqnoRange+ " ) "
             + "and   p_seqno >= ? and p_seqno < ? ";
    setString(1,hBeginKey);
    setString(2,hStopKey);
    if (pSeqno.length()>0)
       {
        sqlCmd    = sqlCmd
                  + "and  p_seqno = ?  ";
        setString(3 , pSeqno);
       }
       int n =  loadTable();
       showLogMessage("I","","Load act_acno Count: ["+n+"]");
       setLoadData(acnoKey);

       return n;
 }

 public int  loadActAcct() throws Exception
 {
   daoTable    = "act_acct";
   extendField = "acct.";
   sqlCmd    = "SELECT "
             + "p_seqno,"
             + "acct_jrnl_bal,"
             + "temp_unbill_interest,"
             + "min_pay,"
             + "min_pay_bal,"
             + "rc_min_pay_bal,"
             + "rc_min_pay_m0,"
             + "autopay_bal,"
             + "pay_by_stage_bal,"
             + "pay_amt,"
             + "pay_cnt,"
             + "ttl_amt,"
             + "ttl_amt_bal,"
             + "adi_end_bal,"
             + "adi_d_avail,"
             + "pay_by_stage_date,"
             + "last_payment_date,"
             + "last_min_pay_date,"
             + "last_cancel_debt_date,"
             + "rowid as rowid "
             + "FROM act_acct "
             + "WHERE p_seqno in ( "+seqnoRange+ ") "
             + "and   p_seqno >= ? and p_seqno < ? ";
    setString(1,hBeginKey);
    setString(2,hStopKey);
    if (pSeqno.length()>0)
       {
        sqlCmd    = sqlCmd
                  + "and  p_seqno = ?  ";
        setString(3 , pSeqno);
       }
   int n = loadTable();
   showLogMessage("I","","Load act_acct Count: ["+n+"]");
   setLoadData(acctKey);

   return n;
 }

public int  loadActAcctCurr() throws Exception
 {
   daoTable    = "act_acct_curr";
   extendField = "acur.";
   sqlCmd    = "SELECT "
             + "p_seqno,"
             + "decode(curr_code,'','901',curr_code) as curr_code,"
             + "acct_jrnl_bal,"
             + "dc_acct_jrnl_bal,"
             + "min_pay,"
             + "dc_min_pay,"
             + "min_pay_bal,"
             + "dc_min_pay_bal,"
             + "autopay_bal,"
             + "dc_autopay_bal,"
             + "ttl_amt,"
             + "dc_ttl_amt,"
             + "ttl_amt_bal,"
             + "dc_ttl_amt_bal,"
             + "beg_bal_lk,"
             + "dc_beg_bal_lk,"
             + "end_bal_lk,"
             + "dc_end_bal_lk,"
             + "beg_bal_op,"
             + "dc_beg_bal_op,"
             + "end_bal_op,"
             + "dc_end_bal_op,"
             + "temp_unbill_interest,"
             + "dc_temp_unbill_interest,"
             + "adjust_cr_amt,"
             + "dc_adjust_cr_amt,"
             + "adjust_cr_cnt,"
             + "adjust_dr_amt,"
             + "dc_adjust_dr_amt,"
             + "adjust_dr_cnt,"
             + "pay_amt,"
             + "dc_pay_amt,"
             + "pay_cnt,"
             + "last_pay_date,"
             + "delaypay_ok_flag,"
             + "rowid as rowid "
             + "FROM act_acct_curr "
           //+ "WHERE p_seqno in ( "+seqnoRange+ " ) ";  modified on 2019/01/22
             + "WHERE p_seqno in ( "+seqnoRange+ " ) "
             + "and   p_seqno >= ? and p_seqno < ? ";
    setString(1,hBeginKey);
    setString(2,hStopKey);
    if (pSeqno.length()>0)
       {
        sqlCmd    = sqlCmd
                  + "and  p_seqno = ?  ";
        setString(3 , pSeqno);
       }
   sqlCmd    = sqlCmd
             + "ORDER BY p_seqno, curr_code ";
   int n = loadTable();
   showLogMessage("I","","Load act_acct_curr Count: ["+n+"]");
   setLoadData(acurKey);
   setLoadData(acurKey2);

   return n;
 }

public int  loadPtrCurrGeneral() throws Exception
 {
   daoTable    = "ptr_curr_general";
   extendField = "pcgl.";
   selectSQL   = "curr_code,total_bal";
   whereStr    = "";
   int n = loadTable();
   showLogMessage("I","","Load ptr_curr_general Count: ["+n+"]");
   setLoadData(pcglKey);
   return n;
 }

 public int  loadPtrWorkday() throws Exception
 {
    daoTable    = "ptr_workday";
    extendField = "wday.";
    selectSQL = "stmt_cycle,"
              + "this_acct_month,"
              + "last_acct_month,"
              + "ll_acct_month,"
              + "next_acct_month,"
              + "this_close_date,"
              + "last_close_date,"
              + "next_close_date,"
              + "ll_close_date,"
              + "this_interest_date,"
              + "this_lastpay_date,"
              + "this_delaypay_date,"
              + "last_delaypay_date,"
              + "ll_delaypay_date ";
    whereStr  = " order by stmt_cycle";
    int n = loadTable();
   showLogMessage("I","","Load ptr_workday Count: ["+n+"]");
    setLoadData(wdayKey);
    return n;
 }

 public int  loadPtrActcode() throws Exception
 {
    daoTable    = "ptr_actcode";
    extendField = "pcod.";
    selectSQL   = "acct_code,"
                + "inter_rate_code,"
                + "inter_rate_code2,"
                + "part_rev,"
                + "revolve,"
                + "interest_method ";
    whereStr    = " order by acct_code";
    int n = loadTable();
   showLogMessage("I","","Load ptr_actcode Count: ["+n+"]");
    setLoadData(pcodKey);
    return n;
 }

 public int  loadBilMerchant() throws Exception
 {
    daoTable    = "bil_merchant";
    extendField = "merc.";
    selectSQL   = "mcht_no,"
                + "mp_rate,"
                + "trans_flag ";
    whereStr    = "order by mcht_no";
    int n = loadTable();
    showLogMessage("I","","Load bil_merchant Count: ["+n+"]");
    setLoadData(mercKey);
    showUseMemory = true;
    showMemory(debtLoadCnt);
    showUseMemory = false;
    return n;
 }


/***********************************************************************/
 double GetJRNLSeq() throws Exception {

    daoTable = "dual";
    sqlCmd   = "select ecs_jrnlseq.nextval as JRN_SEQ from dual ";
    selectTable();
    if ( notFound.equals("Y")) {
         //comcr.err_rtn("GetJRNLSeq() not found!", "", "");
       }
    double seqno = getValueDouble("JRN_SEQ");
    return (seqno);
  }

 void logs(String logData) throws Exception {

  //if ( debugFlag.equals("Y") )
    if ( getValue("adcl.p_seqno").equals(pSeqno) )
       { log(getValue("adcl.p_seqno")+" - "+logData); }

    return;
  }

}  // End of class ActE004
