/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/13  V1.00.63  Allen Ho   cyc_c180                                   *
* 111/02/08  V1.01.01  Allen Ho   mantis 9256                                *
* 111-11-11  V1.00.01  Machao     sync from mega & updated for project coding standard 
* 112-01-06  V1.00.01  Holmes     cycle+加贈判斷 && parm.tran_base="B"  異動  *                                                                       *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA330 extends AccessDAO
{
 private final String PROGNAME = "台幣基金-消費比例轉基金處理程式 111-11-11  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;

 String businessDate   = "";
 String tranSeqno = "";
 String fundCode="";
 String pSeqno = "";

 int    cycleFlag = 0;   // 0 : cycle
 String feedbackType   = "2";  //2 cycle  1: Month Fixed Date
 int    pyajFlag = 0;
 int    hstCnt = 0;
 double tempDestAmt = 0;
 long    totalCnt=0;
 int parmCnt=0;;
 int pyajhstCnt =0;
 int[] dInt = {0,0,0,0};
 boolean DEBUG = false;
 boolean testOld = false;
 int nTempTotAmt = 0;
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
 int  digitCnt=0;
 int runCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA330 proc = new CycA330();
  int  retCode = proc.mainProcess(args);
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
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if ( (args.length > 5) || (args.length< 1) )
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [feedbackType  1.每月 2.帳單週期]");
       showLogMessage("I","","PARM 2 : [business_date]");
       showLogMessage("I","","PARM 3 : [fund_code/TEST]");
       showLogMessage("I","","PARM 4 : [p_seqno/TEST]");
       showLogMessage("I","","PARM 5 : [TEST]");
       return(1);
      }

   if (args.length >= 1 )
	  feedbackType  = args[0];
   if (args.length >= 2 )
      businessDate = args[1];
   if (args.length >= 3 )
      {
       if (args[2].equals("TEST"))
          testOld = true;
       else
          fundCode = args[2];
      }

   if (args.length >= 4 )
      {
       if (args[3].length()==10)
          pSeqno = args[3];
       else if (args[3].equals("TEST"))
          testOld = true;
      }
   if (args.length >= 5 )
      {
       if (args[4].equals("TEST"))
          testOld = true;
      }
   
   if (testOld)
       showLogMessage("I","","目前在 TEST 模式");
   
   if ((!feedbackType.equals("1"))&&
	       (!feedbackType.equals("2")))
   {
	       showLogMessage("I","","回饋方式 : 1.每月 2.帳單週期 ");
	       return(1);
   }  

   if ( !connectDataBase() ) 
       return(1);
   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   cycleFlag = selectPtrWorkday();
   
// String feedbackType   = "2";  //2 cycle  1: Month Fixed Date
// int    cycleFlag = 0;   // 0 : cycle  1: Month Fixed Date     
   if ((feedbackType.equals("2")) && !(cycleFlag == 0) )   {
	       showLogMessage("I","","回饋方式 : 2.帳單週期 ,本日非關帳日,不需執行");
	       showLogMessage("I","","本日非關帳日, 不需執行");
	       return(0);
   }
   if ((feedbackType.equals("1")) &&  (cycleFlag == 0) )   {
	       showLogMessage("I","","回饋方式 : 1.每月指定日 ,本日是關帳日,不需執行 ");
	       return(0);
   }  

   if (selectPtrFundp()==0)
      {
       showLogMessage("I","","本日無符合參數, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadCrdCard();
   loadPtrFundData();
   if (cycleFlag==0) loadMktCardConsume();
   if (cycleFlag==0) 
      {
       if (testOld)
          loadActAcctHst();
       else
          loadActAcct();
      }
   if (pyajFlag!=0) 
      {
       if (testOld)
          loadActCurrHst();
       else
          loadActAcctCurr();
       loadCycPyajAuto();
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除逾期明細資料(cyc_cal_fund)...");
   deleteCycCalFund();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理(cal)資料...");
   selectCycCalFund();
   showLogMessage("I","","=========================================");
   loadActAcno();
   showLogMessage("I","","處理(calrow STEP 1)資料...");
   selectCycCalrowFund1();
   showLogMessage("I","","處理  count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理(calrow STEP 2)資料...");
   selectCycCalrowFund2();
   showLogMessage("I","","處理  count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理(calrow STEP 3)資料...");
   selectCycCalrowFund3();
   showLogMessage("I","","處理  count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");

   if (pSeqno.length()==0) finalProcess();

   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "stmt_cycle";
  daoTable  = "ptr_workday";
  whereStr  = "where stmt_cycle = ? ";

  setString(1 , businessDate.substring(6,8));

  int recCnt = selectTable();

  setValue("wday.this_acct_month" , businessDate.substring(0,6));
  setValue("wday.last_acct_month" , comm.lastMonth(businessDate,1));

  if ( notFound.equals("Y") )
     {
      setValue("wday.stmt_cycle" , "");
      return(1);
     }

  setValue("wday.this_close_date" , businessDate);
  setValue("wday.last_close_date" , comm.lastMonth(businessDate,1)
                                  + businessDate.substring(6,8)); 

  return(0);
 }
// ************************************************************************
 void  selectCycCalFund() throws Exception
 {   
  selectSQL = "b.p_seqno,"
            + "b.fund_code,"
            + "b.proc_date,"
            + "min(b.major_card_no) as major_card_no,"
            + "min(b.acct_month) as acct_month,"
            + "min(b.acct_type) as acct_type,"
            + "count(*) as dest_cnt,"
            + "sum(b.dest_amt) as dest_amt,"
            + "min(a.autopay_acct_bank) as autopay_acct_bank,"
            + "min(a.autopay_acct_no) as autopay_acct_no,"
            + "min(a.autopay_acct_s_date) as autopay_acct_s_date,"
            + "min(a.autopay_acct_e_date) as autopay_acct_e_date,"
            + "min(a.stmt_cycle) as stmt_cycle,"
            + "min(a.id_p_seqno) as id_p_seqno,"
            + "min(a.min_pay_rate) as min_pay_rate,"
            + "min(a.min_pay_rate_s_month) as min_pay_rate_s_month,"
            + "min(decode(a.min_pay_rate_e_month,'','999912',a.min_pay_rate_e_month)) as min_pay_rate_e_month,"
            + "min(a.last_interest_date) as last_interest_date,"
            + "min(a.int_rate_mcode) as int_rate_mcode,"
            + "min(a.new_cycle_month) as new_cycle_month";
  daoTable  = "cyc_cal_fund b,act_acno a";
  whereStr  = "where  a.p_seqno      = b.p_seqno "
            + "and    a.p_seqno      = a.acno_p_seqno "
            + "and    b.proc_date    = ? "
            ;

  if (pSeqno.length()==0)
     whereStr  = whereStr 
               + "and    b.proc_mark    = 'N' ";   

  setString(1 , businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   a.p_seqno = ? ";
          setString(3, pSeqno);
         }
     }

  whereStr  = whereStr
            + "group  by b.p_seqno,fund_code,proc_date ";

  openCursor();

  totalCnt=0;
  int    loadCnt1       = 0; 
  String resSDate = "";

  double[][] parmArr = new double [parmCnt][30];
  for (int inti=0;inti<parmCnt;inti++)
     for (int intk=0;intk<30;intk++) parmArr[inti][intk]=0;

  int inti = -1;
  int cnt1 = 0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    if (pSeqno.length()>0)
       showLogMessage("I","","p_seqno : ["+ getValue("p_seqno") +"]");

    for (int intm=0;intm<parmCnt;intm++)
      {
       if (!getValue("parm.fund_code",intm).equals(getValue("fund_code"))) continue;
       inti = intm;
       break;
      }
    if (inti==-1) continue;
    parmArr[inti][1]++;

    if (getValue("wday.stmt_cycle").length()==0)
       {
        setValue("wday.stmt_cycle"      , getValue("stmt_cycle"));
        setValue("wday.last_close_date" , getValue("wday.last_acct_month")
                                        + getValue("stmt_cycle")); 
        setValue("wday.this_close_date" , getValue("wday.this_acct_month")
                                        + getValue("stmt_cycle")); 
       }  

    setValue("data_key" , getValue("fund_code"));

//  if (!getValue("parm.tran_base",inti).equals("2"))
    if (!getValue("parm.tran_base",inti).equals("B"))    	
       {
        setValue("parm.group_code_sel","0",inti);
        setValueDouble("parm.rc_sub_rate",0.0,inti);
       }

    if (getValue("parm.feedback_type",inti).equals("1"))
       {
        parmArr[inti][2]++;
        resSDate = resSMonth(getValue("parm.unlimit_start_month",inti) +
                                 String.format("%02d",getValueInt("parm.card_feed_run_day",inti)),
                                 businessDate,getValueInt("parm.feedback_months",inti));

        if (!getValue("proc_date").equals(resSDate)) continue;
       }
    else if (getValue("parm.feedback_type",inti).equals("2"))
       {
        parmArr[inti][3]++;
        if (getValue("parm.mp_flag",inti).equals("Y"))
           {
            if (cycleFlag!=0)
               {
                loadCnt1 = getValueInt("int_rate_mcode");
               }
            else
               {
                setValue("acct.p_seqno",getValue("p_seqno"));
                loadCnt1 = getLoadData("acct.p_seqno");
               }

            if (loadCnt1 > 0)
               {
                updateCycCalFund("3");
                continue;
               }
            }
       }
    parmArr[inti][4]++;

    if (getValue("parm.ebill_flag",inti).equals("Y"))
       {
        int retInt=0;
        if (testOld)
           retInt = selectActAcno();
        else
           retInt = selectCycAcmm();

        if (retInt !=0)
           {
            updateCycCalFund("6");
            continue;
           }
       }
    parmArr[inti][5]++;
// *********************************************
    int okFlag = 0;
    if (getValue("parm.valid_card_flag",inti).equals("Y"))
       {
        parmArr[inti][6]++;
        setValue("card.p_seqno",getValue("p_seqno"));
        loadCnt1 = getLoadData("card.p_seqno");

        if (loadCnt1==0)
           {
           updateCycCalFund("7");
           continue;
           }

        parmArr[inti][7]++;
        if (!getValue("parm.acct_type_sel",inti).equals("0"))
           {
            okFlag=1;
            for (int intk=0;intk<loadCnt1;intk++)
              {
               if (selectPtrFundData(getValue("card.acct_type",intk),
                                        getValue("parm.acct_type_sel",inti),"4",2)!=0) continue;
               okFlag = 0;
              }
            if (okFlag!=0)
              {
               updateCycCalFund("5");
               continue;
              }
           }
       }
    parmArr[inti][8]++;
    okFlag =0;
// *********************************************
    if (getValue("parm.valid_afi_flag",inti).equals("Y"))
       {
        parmArr[inti][9]++;
        setValue("card.p_seqno",getValue("p_seqno"));
        loadCnt1 = getLoadData("card.p_seqno");

        if (loadCnt1==0)
           {
           updateCycCalFund("7");
           continue;
           }
        parmArr[inti][10]++;
        if (!getValue("parm.group_code_sel",inti).equals("0"))
           {
            okFlag=1;
            for (int intk=0;intk<loadCnt1;intk++)
              {
               if (selectPtrFundData(getValue("card.group_code",intk),
                                        getValue("parm.group_code_sel",inti),"3",3)!=0) continue;
               okFlag = 0;
              }
            if (okFlag!=0)
              {
               updateCycCalFund("4");
               continue;
              }
           }
       }
    parmArr[inti][11]++;
// *********************************************
    if (getValue("parm.d_txn_cond",inti).equals("Y"))
       {
        setValue("cons.p_seqno",getValue("p_seqno"));
        loadCnt1 = getLoadData("cons.p_seqno");

        if (loadCnt1==0)
           {
           updateCycCalFund("8");
           continue;
           }
        parmArr[inti][12]++;
        int sumTotalAmt=0;
        for (int intm=0;intm<loadCnt1;intm++)
          {

           if (selectPtrFundData(getValue("cons.group_code",intm),
                                    getValue("parm.group_code_sel",inti),"3",3)!=0) continue;
           if (selectPtrFundData(getValue("cons.acct_type",intm),
                                    getValue("parm.acct_type_sel",inti),"4",2)!=0) continue;
//         if (select_ptr_fund_data(getValue("cons.source_code",intm),
//                                  getValue("parm.source_code_sel",inti),"A",3)!=0) continue;
           if (selectPtrFundData(getValue("cons.card_type",intm),
                                    getValue("parm.card_type_sel",inti),"5",3)!=0) continue;

           sumTotalAmt = sumTotalAmt + getValueInt("cons.dest_amt",intm);
          }
        if (getValueInt("parm.d_txn_amt",inti)>sumTotalAmt)
           {
           updateCycCalFund("9");
           continue;
           }
       
       }
// *********************************************
    parmArr[inti][13]++;
   
    if ((getValue("parm.autopay_flag",inti).equals("Y"))||
        (getValue("parm.autopay_digit_cond",inti).equals("Y")))
       {
        parmArr[inti][14]++;
//      if (!getValue("autopay_acct_bank").equals("017"))
        if (!getValue("autopay_acct_bank").equals("006"))	
           {
            updateCycCalFund("A");
            continue;
           }
        parmArr[inti][15]++;
        if (getValue("autopay_acct_s_date").length()==0)
            setValue("autopay_acct_s_date","00000000");
        if (getValue("autopay_acct_e_date").length()==0)
            setValue("autopay_acct_e_date","30001231");
         if ((businessDate.compareTo(getValue("autopay_acct_s_date"))<0)|| 
             (getValue("wday.last_close_date").compareTo(getValue("autopay_acct_e_date"))>0))
           {
            updateCycCalFund("H");
            continue;
           }
        okFlag=0;
        parmArr[inti][16]++;
        for (int intm=0;intm<digitCnt;intm++)
          {
           if (getValue("autopay_acct_no").substring(3,5).equals(getValue("digi.sub_acct_no",intm)))
              {
               okFlag=1;
               break;
              }
          }
        if (getValue("parm.autopay_digit_cond",inti).equals("Y"))
          {
           if (okFlag==0)
              {
               updateCycCalFund("B");
               continue;
              }
           }

        parmArr[inti][17]++;
        setValue("curr.p_seqno"  ,getValue("p_seqno"));
        loadCnt1 = getLoadData("curr.p_seqno");
        if (loadCnt1==0)
           { 
            updateCycCalFund("C");
            continue;
           }
        if (getValueDouble("curr.dc_min_pay_bal") > 0) 
           { 
            updateCycCalFund("D");
            continue;
           }
  if (pSeqno.length()>0)
  showLogMessage("I","","20200315 STEP 3.1");
// **************************************************************
        if (getValueDouble("curr.dc_ttl_amt") <= 0) 
           { 
            selectCycPyajHst();
            if (pyajhstCnt==0)
               {
                updateCycCalFund("G");
                continue;
               }

            okFlag=0;
            for (int intk=0;intk< pyajhstCnt;intk++)
               {
                if (selectActCurrHst1(intk)!=0)
                   {
                    updateCycCalFund("I");
                    break;
                   }
                if (getValueInt("acht.min_pay_bal")>0)
                   {
                    updateCycCalFund("J");
                    break;
                   }
                if  (getValueDouble("pyht.payment_amt",intk)+
                     getValueDouble("pyht.refund_amt",intk)+
                     getValueDouble("pyht.adfund_amt",intk)-
                     getValueDouble("pyht.adjust_amt",intk)-
                     getValueDouble("acht.min_pay")<0)
                   {
                    updateCycCalFund("F");
                    break;
                   }
                if ((getValueDouble("pyht.other_pay_amt",intk)>0)&&
                    (getValueDouble("pyht.payment_amt",intk)==0))
                   {
                    updateCycCalFund("L");
                    break;
                   }
  if (pSeqno.length()>0)
  showLogMessage("I","","20200315 STEP 3.3 [" + okFlag +"]");
                if (getValueDouble("pyht.payment_amt",intk)!=0) 
                   {
                    okFlag=1;
                    break;
                   }
               }
  if (pSeqno.length()>0)
  showLogMessage("I","","20200315 STEP 3.4 [" + okFlag +"]");
            if (okFlag==0) 
               {
                updateCycCalFund("L");
                continue;
               }
           }
        else
           {
            parmArr[inti][18]++;
            setValue("auto.p_seqno"  ,getValue("p_seqno"));
            loadCnt1 = getLoadData("auto.p_seqno");
            if (loadCnt1==0)
               {
                if (getValueDouble("curr.dc_min_pay") > 0)
                   {
                    updateCycCalFund("E");
                    continue;
                    }
               }
  if (pSeqno.length()>0)
     {
  showLogMessage("I","","20200315 STEP 4.1 ["+  getValueDouble("auto.payment_amt")+"]");
  showLogMessage("I","","20200315 STEP 4.2 ["+  getValueDouble("auto.refund_amt")+"]");
  showLogMessage("I","","20200315 STEP 4.3 ["+  getValueDouble("auto.adfund_amt")+"]");
  showLogMessage("I","","20200315 STEP 4.4 ["+  getValueDouble("auto.adjust_amt")+"]");
  showLogMessage("I","","20200315 STEP 4.5 ["+  getValueDouble("curr.dc_min_pay")+"]");
     }
            if (getValueDouble("auto.payment_amt")+
                getValueDouble("auto.refund_amt")+
                getValueDouble("auto.adfund_amt")-
                getValueDouble("auto.adjust_amt")-
                getValueDouble("curr.dc_min_pay")<0)
               {
                updateCycCalFund("F");
                continue;
               }
            if ((getValueDouble("auto.payment_amt")==0)&&
                (getValueDouble("auto.other_pay_amt")!=0))
               {
                updateCycCalFund("F");
                continue;
               }
            if ((getValueDouble("auto.payment_amt")==0)&&
                (getValueDouble("auto.other_pay_amt")==0))
               {
                selectCycPyajHst();
                if (pyajhstCnt==0)
                   {
                    updateCycCalFund("G");
                    continue;
                   }

                okFlag=0;
                for (int intk=0;intk< pyajhstCnt;intk++)
                   {
                    if (getValueDouble("pyht.payment_amt",intk)!=0)
                        {
                         okFlag=2;
                         break;
                        }
                    else if (getValueDouble("pyht.other_pay_amt",intk)>0)
                        {
                         break;
                        }
                   }
                if (okFlag==0)
                   {
                    updateCycCalFund("G");
                    continue;
                   }
               }
           }
// **************************************************************
       }
    parmArr[inti][19]++;

//  if (getValue("parm.threshold_sel",inti).equals("2"))
    if (getValue("parm.threshold_sel",inti).equals("1"))    	
       {
        nTempTotAmt = calThresholdAmt1(inti,getValueDouble("dest_amt"));
        if (getValueDouble("dest_amt")<0)
           nTempTotAmt = nTempTotAmt *-1; 
       }
    else
       {
        if (getValue("parm.purchase_type_sel",inti).equals("1"))
            nTempTotAmt = calThresholdAmt2(inti,getValueDouble("dest_amt"),getValueDouble("dest_amt"));
        else if (getValue("parm.purchase_type_sel",inti).equals("2"))
            nTempTotAmt = calThresholdAmt2(inti,getValueDouble("dest_cnt"),getValueDouble("dest_amt"));
       }


    if (nTempTotAmt ==0)
       {
        updateCycCalFund("2");
        continue;
       }
    parmArr[inti][20]++;
    if (nTempTotAmt > 0)
       {
        if ((nTempTotAmt > getValueInt("parm.feedback_lmt",inti))&&(getValueInt("parm.feedback_lmt",inti) > 0))
           nTempTotAmt = getValueInt("parm.feedback_lmt",inti);
       }
    else
       {
        if ((nTempTotAmt*-1 > getValueInt("parm.feedback_lmt",inti))&&(getValueInt("parm.feedback_lmt",inti) > 0))
           nTempTotAmt = getValueInt("parm.feedback_lmt",inti)*-1;
       }


    setValueInt("beg_tran_amt" , nTempTotAmt);

//  if (fund_code.length()>0)
//     showLogMessage("I","","beg_tran_amt0[" + getValue("beg_tran_amt") +"]");

//  if (fund_code.length()>0)
//     showLogMessage("I","","valid_period [" + getValue("parm.valid_period",inti) +"]");

    if (!getValue("parm.valid_period",inti).equals("Y"))
       {
//  if (fund_code.length()>0)
//     showLogMessage("I","","beg_tran_amt1[" + getValue("beg_tran_amt") +"]");

        insertCycCobrandFund(inti);
       }
    else
        {
//  if (fund_code.length()>0)
//     showLogMessage("I","","beg_tran_amt2[" + getValue("beg_tran_amt") +"]");
//        if (getValueInt("beg_tran_amt")<0)
//           insertCycFundDtl(2,inti);
//         else
//           insertCycFundDtl(1,inti);

         insertMktCashbackDtl(inti);
        }

    if (getValueInt("beg_tran_amt")>0)
//  if ((getValue("parm.feedback_type",inti).equals("1"))&&
    if ((getValue("parm.feedback_type",inti).equals("2"))&&    		
        (getValueDouble("parm.rc_sub_rate",inti) >0))
       {
        selectActAcctHst0();

        setValueInt("beg_tran_amt" , (int)Math.round(tempDestAmt));

        if (getValueInt("beg_tran_amt") > 0)
           {
            if (!getValue("parm.valid_period",inti).equals("Y"))
               insertCycCobrandFund(inti);
            else
              {
//             insertCycFundDtl(1,inti);
               insertMktCashbackDtl(inti);
              }
           }
       }

    updateCycCalFund("Y");
   
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
  
  if (fundCode.length()>0)
     {
      showLogMessage("I","","=========================================");
      for (int intm=0;intm<parmCnt;intm++)
        {
         showLogMessage("I","","["+intm+"] fund_code  :["+ getValue("parm.fund_code",intm) +"]");
         for (int intk=0;intk<30;intk++)
          {
           if (parmArr[intm][intk]==0) continue;
           showLogMessage("I",""," 測試絆腳石 : ["+intk+"] = ["+parmArr[intm][intk]+"]");
          }
        }    
     showLogMessage("I","","=========================================");
    }
  return;
 }
// ************************************************************************
 void  selectCycCalrowFund1() throws Exception
 {
  selectSQL = "p_seqno,"
            + "fund_code";
  daoTable  = "cyc_calrow_fund b";
  whereStr  = "where proc_date    = ? "
            + "and   proc_mark    = 'N' "
            ;

  setString(1 , businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(3, pSeqno);
         }
     }

  whereStr  = whereStr
            + "group by p_seqno,fund_code";

  openCursor();

  int    loadCnt1       = 0; 
  String resSDate = "";
  totalCnt=0;
  int inti = -1;
  while( fetchTable() ) 
   { 
    totalCnt++;

   setValue("stmt_cycle"           , getValue("acno.stmt_cycle")); 
   setValue("id_p_seqno"           , getValue("acno.id_p_seqno")); 
   setValue("min_pay_rate"         , getValue("acno.min_pay_rate")); 
   setValue("min_pay_rate_s_month" , getValue("acno.min_pay_rate_s_month")); 
   setValue("min_pay_rate_e_month" , getValue("acno.min_pay_rate_e_month")); 
   setValue("last_interest_date"   , getValue("acno.last_interest_date")); 
   setValue("new_cycle_month"      , getValue("acno.new_cycle_month")); 

    for (int intm=0;intm<parmCnt;intm++)
      {
       if (!getValue("parm.fund_code",intm).equals(getValue("fund_code"))) continue;
       inti = intm;
       break;
      }
    if (inti==-1) continue;

    setValue("data_key" , getValue("fund_code"));

    if (getValue("parm.fund_crt_date_s",inti).length()==0)
        setValue("parm.fund_crt_date_s","20000101",inti);

    if (getValue("parm.fund_crt_date_e",inti).length()==0)
        setValue("parm.fund_crt_date_e","30001231",inti);

//  if (!getValue("parm.tran_base",inti).equals("2"))
    if (!getValue("parm.tran_base",inti).equals("B"))    	
       {
        setValue("parm.group_code_sel","0",inti);
        setValueDouble("parm.rc_sub_rate",0.0,inti);
       }

    if (getValue("parm.feedback_type",inti).equals("1"))
       {
        resSDate = resSMonth(getValue("parm.unlimit_start_month",inti) +
                                 String.format("%02d",getValueInt("parm.card_feed_run_day",inti)),
                                 businessDate,getValueInt("parm.feedback_months",inti));

        if (!businessDate.equals(resSDate)) continue;
       }
    else if (getValue("parm.feedback_type",inti).equals("2"))
       {
        if (getValue("parm.mp_flag",inti).equals("Y"))
           {
            if (cycleFlag !=0)
               {
                loadCnt1 = getValueInt("acno.int_rate_mcode");
               }
            else
               {
                setValue("acct.p_seqno",getValue("p_seqno"));
                loadCnt1 = getLoadData("acct.p_seqno");
               }

            if (loadCnt1 > 0)
               {
                updateCycCalrowFundP(3);
                continue;
               }
            }
       }

    if (getValue("parm.ebill_flag",inti).equals("Y"))
       {
        int retInt=0;
        if (testOld)
           retInt = selectActAcno();
        else
           retInt = selectCycAcmm();

        if (retInt !=0)
           {
            updateCycCalrowFundP(6);
            continue;
           }
       }
// *****************************************
    int okFlag = 0;
    if (getValue("parm.valid_card_flag",inti).equals("Y"))
       {
        setValue("card.p_seqno",getValue("p_seqno"));
        loadCnt1 = getLoadData("card.p_seqno");

        if (loadCnt1==0)
           {
           updateCycCalrowFundP(7);
           continue;
           }

        if (!getValue("parm.acct_type_sel",inti).equals("0"))
           {
            okFlag=1;
            for (int intk=0;intk<loadCnt1;intk++)
              {
               if (selectPtrFundData(getValue("card.acct_type",intk),
                                        getValue("parm.acct_type_sel",inti),"4",2)!=0) continue;
               okFlag = 0;
              }
            if (okFlag!=0)
              {
               updateCycCalrowFundP(5);
               continue;
              }
           }
       }
// *********************************************
    if (getValue("parm.valid_afi_flag",inti).equals("Y"))
       {
        setValue("card.p_seqno",getValue("p_seqno"));
        loadCnt1 = getLoadData("card.p_seqno");

        if (loadCnt1==0)
           {
               updateCycCalrowFundP(7);
           continue;
           }
        if (!getValue("parm.group_code_sel",inti).equals("0"))
           {
            okFlag=1;
            for (int intk=0;intk<loadCnt1;intk++)
              {
               if (selectPtrFundData(getValue("card.group_code",intk),
                                        getValue("parm.group_code_sel",inti),"3",3)!=0) continue;
               okFlag = 0;
              }
            if (okFlag!=0)
              {
               updateCycCalrowFundP(4);
               continue;
              }
           }
       }
// *********************************************
    if ((getValue("parm.autopay_flag",inti).equals("Y"))||
        (getValue("parm.autopay_digit_cond",inti).equals("Y")))
       {
        if (selectCycPyaj(inti)!=0)
           {
            updateCycCalrowFundP(1);
            continue;
           }
       }
   } 
  closeCursor();
  
  return;
 }
// ************************************************************************
 void  selectCycCalrowFund2() throws Exception
 {
  selectSQL = "p_seqno,"
            + "fund_code,"
            + "dest_amt,"
            + "rowid as rowid";
  daoTable  = "cyc_calrow_fund";
  whereStr  = "where proc_date    = ? "
            + "and   proc_mark    = 'N' " 
            ;

  setString(1 , businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(3, pSeqno);
         }
     }

  openCursor();

  int    loadCnt1       = 0; 
  totalCnt=0;
  int inti = -1;
  while( fetchTable() ) 
   { 
    for (int intm=0;intm<parmCnt;intm++)
      {
       if (!getValue("parm.fund_code",intm).equals(getValue("fund_code"))) continue;
       inti = intm;
       break;
      }
    if (inti==-1) continue;
    totalCnt++;

//  if (getValue("parm.threshold_sel",inti).equals("2"))
//級距式= 1 , 條件式=2
    if (getValue("parm.threshold_sel",inti).equals("1"))    	
       {
         nTempTotAmt = calThresholdAmt1(inti,getValueDouble("dest_amt"));
        if (getValueDouble("dest_amt")<0)
           nTempTotAmt = nTempTotAmt *-1; 
       }
    else
       {
        nTempTotAmt = calThresholdAmt2(inti,getValueDouble("dest_amt"),getValueDouble("dest_amt"));
       }

    if (nTempTotAmt ==0) continue;

/*
    if ((n_temp_tot_amt > getValueInt("parm.feedback_lmt",inti))&&(getValueInt("parm.feedback_lmt",inti) > 0))
       n_temp_tot_amt = getValueInt("parm.feedback_lmt",inti);

    setValueInt("beg_tran_amt" , n_temp_tot_amt);
    if (getValueDouble("dest_amt")<0)
       setValueInt("beg_tran_amt" , n_temp_tot_amt*-1);
*/
    updateCycCalrowFund(nTempTotAmt);
   } 
  closeCursor();
  
  return;
 }
// ************************************************************************
 void  selectCycCalrowFund3() throws Exception
 {
  selectSQL = "p_seqno," 
            + "fund_code,"
            + "max(acct_month) as acct_month," 
            + "max(acct_type) as acct_type," 
            + "max(id_p_seqno) as id_p_seqno," 
            + "max(major_card_no) as major_card_no," 
            + "max(stmt_cycle) as stmt_cycle," 
            + "sum(dest_amt1) as beg_tran_amt";
  daoTable  = "cyc_calrow_fund";
  whereStr  = "where  proc_date    = ? "
            + "and    proc_mark    = 'N' "  
            + "and    dest_amt1    != 0 "
            ;

  setString(1 , businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(3, pSeqno);
         }
     }
  whereStr  = whereStr
            + "group  by p_seqno,fund_code "
            + "having sum(dest_amt1) != 0 ";
   
  openCursor();

  totalCnt=0;

  int inti = -1;
  while( fetchTable() ) 
   { 
    totalCnt++;

    for (int intm=0;intm<parmCnt;intm++)
      {
       if (!getValue("parm.fund_code",intm).equals(getValue("fund_code"))) continue;
       inti = intm;
       break;
      }
    if (inti==-1) continue;

    if (getValueInt("beg_tran_amt") > 0)
       {
        if ((getValueInt("beg_tran_amt") > getValueInt("parm.feedback_lmt",inti))&&(getValueInt("parm.feedback_lmt",inti) > 0))
           setValueInt("beg_tran_amt" ,  getValueInt("parm.feedback_lmt",inti));
       }
    else
       {
        if ((getValueInt("beg_tran_amt")*-1 > getValueInt("parm.feedback_lmt",inti))&&(getValueInt("parm.feedback_lmt",inti) > 0))
           setValueInt("beg_tran_amt" ,  getValueInt("parm.feedback_lmt",inti)*-1);
       }

    if (!getValue("parm.valid_period",inti).equals("Y"))
        {
         if (getValueDouble("beg_tran_amt")>0)
            insertCycCobrandFund(inti);
        }
    else
        {
//         if (getValueDouble("beg_tran_amt")<0)
//            insertCycFundDtl(2,inti);
//         else
//            insertCycFundDtl(1,inti);

         insertMktCashbackDtl(inti);
        }

    if (getValueDouble("beg_tran_amt")>0)
//  if ((getValue("parm.feedback_type",inti).equals("1"))&&
//cycle && 加贈
    if ((getValue("parm.feedback_type",inti).equals("2"))&&    		
        (getValueDouble("parm.rc_sub_rate",inti) >0))
       {
        selectActAcctHst0();

        setValueInt("beg_tran_amt" , (int)Math.round(tempDestAmt));

        if (getValueInt("beg_tran_amt") > 0)
           {
            if (!getValue("parm.valid_period",inti).equals("Y"))
               insertCycCobrandFund(inti);
            else
              {
//               insertCycFundDtl(1,inti);
               insertMktCashbackDtl(inti);
              }
           }
       }
    updateCycCalrowFundP(0);
   } 
  closeCursor();
  
  return;
 }
// ************************************************************************
int selectPtrFundp() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "ptr_fundp";
  whereStr  = "WHERE apr_flag      = 'Y' "
            + "AND   (stop_flag  = 'N' "
            + " OR    (stop_flag = 'Y' "
            + "  and   ? < stop_date)) "
            + "and   feedback_type in ('1','2') "
            + "and   fund_feed_flag = 'Y' ";

  setString(1,businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
     }

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) return(0);

  for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("parm.feedback_type",inti).equals("1"))
          {
           if (!businessDate.substring(6,8).equals
               (String.format("%02d",getValueInt("parm.card_feed_run_day",inti)))) continue;
          }
       else
          {
           if (cycleFlag!=0) continue;
          }

       if (getValue("parm.fund_crt_date_s",inti).length()==0)
          setValue("parm.fund_crt_date_s","20000101",inti);

       if (getValue("parm.fund_crt_date_e",inti).length()==0)
          setValue("parm.fund_crt_date_e","30001231",inti);

       if ((businessDate.compareTo(getValue("parm.fund_crt_date_s",inti))<0)|| 
           (businessDate.compareTo(getValue("parm.fund_crt_date_e",inti))>0))  continue;

//     showLogMessage("I","","fund_code   ["+ getValue("parm.fund_code",inti)  +"]");
//     showLogMessage("I",""," day        ["+ getValueInt("card_feed_run_day",inti) +"]");

       if ((getValue("parm.autopay_flag",inti).equals("Y"))||
           (getValue("parm.autopay_digit_cond",inti).equals("Y"))) 
          {
           selectDbcDigitalParm();
           pyajFlag=1;
          }

       setValue("parm.parmrun_flag" , "N" ,inti);

       if (getValue("parm.acct_type_sel",inti).equals("0"))   // debug for valid card, this must a request
          setValue("parm.valid_card_flag" , "N" ,inti);

       if (getValue("parm.effect_type",inti).length()==0)
          {
           if (getValueInt("parm.effect_months",inti)>0)
              setValue("parm.effect_type" , "1" ,inti);
           else
              setValue("parm.effect_type" , "0" ,inti);
          }

       setValue("parm.parmrun_flag" , "Y" ,inti);
       runCnt++;
      }


  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt + "] 本日執行筆數[" + runCnt + "]");

  return(runCnt);
 }
// ************************************************************************
 int insertMktCashbackDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("mkt_modseq");
  dateTime();

  setValue("tran_date"            , sysDate);
  setValue("tran_time"            , sysTime);
  setValue("fund_code"            , getValue("parm.fund_code",inti));
  setValue("fund_name"            , getValue("parm.fund_name",inti));
  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("acct_type"            , getValue("acct_type"));
  setValue("id_p_seqno"           , getValue("id_p_seqno"));
  if (getValueInt("beg_tran_amt")>0)
     {
      setValue("tran_code"            , "2");
      setValue("mod_desc"             , "回饋基金");
     }
  else
     {
      setValue("tran_code"            , "2");
      setValue("mod_desc"             , "回饋基金系統扣回");
     }
  setValue("tran_pgm"             , javaProgram);
  setValueInt("beg_tran_amt"      , getValueInt("beg_tran_amt"));
  setValueInt("end_tran_amt"      , getValueInt("beg_tran_amt"));

  setValueInt("res_tran_amt"      , 0);
  setValueInt("res_total_cnt"     , 0);
  setValueInt("res_tran_cnt"      , 0);
  setValue("res_s_month"          , "");
  setValue("res_upd_date"         , "");

  if (getValueInt("beg_tran_amt")<0)
      setValue("effect_e_date"        , "");
  else if (getValue("parm.effect_type",inti).equals("0"))
      setValue("effect_e_date"        , "");
  else if (getValue("parm.effect_type",inti).equals("1"))
     setValue("effect_e_date"    , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  else if (getValue("parm.effect_type",inti).equals("2"))
    {
     setValue("effect_e_date"    , comm.nextMonth( businessDate , getValueInt("parm.effect_years",inti)*12).substring(0,4)
                                 + String.format("%02d" , getValueInt("parm.effect_fix_month",inti))
                                 + businessDate.substring(6,8));

    }

  setValue("tran_seqno"           , tranSeqno);
  setValue("proc_month"           , businessDate.substring(0,6));
  setValue("acct_date"            , businessDate);
  setValue("mod_memo"             , "");
  setValue("mod_reason"           , "");
  setValue("case_list_flag"       , "N");
  setValue("crt_user"             , javaProgram);
  setValue("crt_date"             , sysDate);
  setValue("apr_date"             , sysDate);
  setValue("apr_user"             , javaProgram);
  setValue("apr_flag"             , "Y");
  setValue("mod_user"             , javaProgram); 
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteCycCalFund() throws Exception
 {
  daoTable  = "cyc_cal_fund";
  whereStr  = "WHERE ((proc_mark = 'Y' "
            + "  and   proc_date < ?)  "
            + " or    (proc_mark != 'Y' "
            + "  and   proc_date < ?)) ";

  setString(1, comm.nextMonthDate(businessDate,-12));
  setString(2, comm.nextMonthDate(businessDate,-6));

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(3, fundCode);
      if (pSeqno.length()!=0)
         {
          whereStr  = whereStr 
                    + "and   p_seqno = ? ";
          setString(4, pSeqno);
         }
     }

  int n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete cyc_cal_fund [" + n + "] records");

  return;
 }
// ************************************************************************
 void updateCycCalFund(String procMark) throws Exception
 {
  dateTime();

  updateSQL = "proc_mark  = ?,"
            + "stmt_cycle = ?,"
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";  
  daoTable  = "cyc_cal_fund";
  whereStr  = "WHERE proc_date    = ? "
            + "and   fund_code    = ? "
            + "and   p_seqno      = ? "
            + "and   proc_mark    = 'N' "
            ;

  setString(1 , procMark);
  setString(2 , getValue("stmt_cycle"));
  setString(3 , javaProgram);
  setString(4 , getValue("proc_date"));
  setString(5 , getValue("fund_code"));
  setString(6 , getValue("p_seqno"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateCycCalrowFundP(int hIntb) throws Exception
 {
  dateTime();
  String procMark = "Y";
  if (hIntb<=9) procMark = String.format("%d" , hIntb); 

  updateSQL = "proc_mark  = ?,"
            + "stmt_cycle = ?,"
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";  
  daoTable  = "cyc_calrow_fund";
  whereStr  = "WHERE proc_date    = ? "
            + "and   fund_code    = ? "
            + "and   p_seqno      = ? "
            + "and   proc_mark    = 'N' "
            ;

  setString(1 , procMark);
  setString(2 , getValue("stmt_cycle"));
  setString(3 , javaProgram);
  setString(4 , getValue("proc_date"));
  setString(5 , getValue("fund_code"));
  setString(6 , getValue("p_seqno"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateCycCalrowFund(int destAmt1 ) throws Exception
 {
  dateTime();

  updateSQL = "dest_amt1  = ?,"
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";  
  daoTable  = "cyc_calrow_fund";
  whereStr  = "WHERE rowid  = ? "
            ;

  setDouble(1 , destAmt1);
  setString(2 , javaProgram);
  setRowId(3 , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
// int insertCycFundDtl(int numType,int inti) throws Exception
// {
//  dateTime();
//  extendField = "fdtl.";
//
//  setValue("fdtl.business_date"        , businessDate);
//  setValue("fdtl.curr_code"            , "901");
//  setValue("fdtl.create_date"          , sysDate);
//  setValue("fdtl.create_time"          , sysTime);
//  setValue("fdtl.id_p_seqno"           , getValue("id_p_seqno"));
//  setValue("fdtl.p_seqno"              , getValue("p_seqno"));
//  setValue("fdtl.acct_type"            , getValue("acct_type"));
//  setValue("fdtl.card_no"              , "");
//  setValue("fdtl.fund_code"            , getValue("parm.fund_code",inti).substring(0,4));
//  setValue("fdtl.vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
//  if (numType==1)
//     {
//      setValue("fdtl.tran_code"            , "1");
//      setValue("fdtl.cd_kind"              , "H001");    // 新增ew add
//     }
//  else
//     {
//      setValue("fdtl.tran_code"            , "7");
//      setValue("fdtl.cd_kind"              , "H003");  // 0-移轉 1-新增 2-贈與 3-調整 4-使用 5-匯入 6-移除 7-扣回
//     }
//  setValue("fdtl.memo1_type"           , "1");   /* fund_code 必須有值 */
//  setValueInt("fdtl.fund_amt"          , Math.abs(getValueInt("beg_tran_amt")));
//  setValueInt("fdtl.other_amt"         , 0);
//  setValue("fdtl.proc_flag"            , "N");
//  setValue("fdtl.proc_date"            , "");
//  setValue("fdtl.execute_date"         , businessDate);
//  setValue("fdtl.fund_cnt"             , "1");
//  setValue("fdtl.mod_user"             , javaProgram); 
//  setValue("fdtl.mod_time"             , sysDate+sysTime);
//  setValue("fdtl.mod_pgm"              , javaProgram);
//
//  daoTable  = "cyc_fund_dtl";
//
//  insertTable();
//
//  insertCnt++;
//  return(0);
// }
// ************************************************************************
 String resSMonth(String yearSDate,String nowDate,int calMonths) throws Exception
 {
  String okDate="";
  for (int inti=0;inti<6000;inti++)
    {
     okDate = comm.nextMonthDate(yearSDate,inti*calMonths);
     if (okDate.compareTo(nowDate)>=0) break;
    }
  return okDate;
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno,"
            + "max(a.acct_type) as acct_type,"
            + "decode(a.group_code,'','0000',a.group_code) as group_code";
  daoTable  = "crd_card a,(select distinct p_seqno from cyc_cal_fund "
            + "            where proc_date = '" + businessDate + "' ) b ";   
  whereStr  = "where a.current_code = '0' "
            + "and   a.p_seqno        = b.p_seqno "
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   a.p_seqno = ? ";
      setString(1, pSeqno);
     }

  whereStr  = whereStr  
            + "group by a.p_seqno,decode(a.group_code,'','0000',a.group_code) "
            + "order by a.p_seqno,decode(a.group_code,'','0000',a.group_code) ";

  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "a.p_seqno,"
            + "a.autopay_acct_bank,"
            + "a.autopay_acct_s_date,"
            + "a.autopay_acct_e_date,"
            + "a.int_rate_mcode,"
            + "a.stmt_cycle,"
            + "a.id_p_seqno,"
            + "a.min_pay_rate,"
            + "a.min_pay_rate_s_month,"
            + "decode(a.min_pay_rate_e_month,'','999912',a.min_pay_rate_e_month) as min_pay_rate_e_month,"
            + "a.last_interest_date,"
            + "a.new_cycle_month";
  daoTable  = "act_acno a,(select distinct p_seqno from cyc_calrow_fund "
            + "            where proc_mark = 'N' ) b ";
  whereStr  = "where a.p_seqno        = b.p_seqno "
            + "and   a.p_seqno        = a.acno_p_seqno "
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   a.p_seqno = ? ";
      setString(1, pSeqno);
     }

  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load act_acno Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActAcct() throws Exception
 {
  extendField = "acct.";
  selectSQL = "p_seqno,"
            + "min_pay_bal";
  daoTable  = "act_acct_"+getValue("wday.stmt_cycle");
  whereStr  = "where min_pay_bal > 0 "
            + "and   p_seqno in (select p_seqno "
            + "                  from   cyc_cal_fund "
            + "                  where proc_date = ?  "
            + "                  union  "
            + "                  select p_seqno "
            + "                  from   cyc_calrow_fund "
            + "                  where proc_date = ?  "
            + "                  group by p_seqno ) "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   p_seqno = ? ";
      setString(3, pSeqno);
     }

  int  n = loadTable();
  setLoadData("acct.p_seqno");

  showLogMessage("I","","Load act_acct Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcctHst() throws Exception
 {
  extendField = "acct.";
  selectSQL = "p_seqno,"
            + "min_pay_bal";
  daoTable  = "act_acct_hst";
  whereStr  = "where min_pay_bal > 0 "
            + "and   p_seqno in (select p_seqno "
            + "                  from   cyc_cal_fund "
            + "                  where proc_date = ?  "
            + "                  union  "
            + "                  select p_seqno "
            + "                  from   cyc_calrow_fund "
            + "                  where proc_date = ?  "
            + "                  group by p_seqno ) "
            + "and   acct_month = ? " 
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);
  setString(3 , getValue("wday.last_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   p_seqno = ? ";
      setString(4, pSeqno);
     }

  int  n = loadTable();
  setLoadData("acct.p_seqno");

  showLogMessage("I","","Load act_acct_hst Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActAcctCurr() throws Exception
 {
  extendField = "curr.";
  selectSQL = "p_seqno,"
            + "dc_min_pay,"
            + "dc_min_pay_bal,"
            + "dc_ttl_amt";
  if (cycleFlag==1)
     daoTable  = "act_acct_curr";
  else
     daoTable  = "act_acct_curr_"+getValue("wday.stmt_cycle");
  whereStr  = "where p_seqno in (select distinct p_seqno "
            + "                  from   cyc_cal_fund "
            + "                  where proc_date = ?  "
            + "                  union  "
            + "                  select distinct p_seqno "
            + "                  from   cyc_calrow_fund "
            + "                  where proc_date = ?)  "
            + "and   curr_code = '901' "
            ;

  setString(1, businessDate);
  setString(2, businessDate);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   p_seqno = ? ";
      setString(3, pSeqno);
     }

  int  n = loadTable();
  setLoadData("curr.p_seqno");

  showLogMessage("I","","Load " + daoTable +" Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActCurrHst() throws Exception
 {
  extendField = "curr.";
  selectSQL = "p_seqno,"
            + "min_pay  as dc_min_pay,"
            + "min_pay_bal as dc_min_pay_bal,"
            + "stmt_last_ttl as dc_ttl_amt";
  daoTable  = "act_curr_hst";
  whereStr  = "where p_seqno in (select distinct p_seqno "
            + "                  from   cyc_cal_fund "
            + "                  where proc_date = ?  "
            + "                  union  "
            + "                  select distinct p_seqno "
            + "                  from   cyc_calrow_fund "
            + "                  where proc_date = ?)  "
            + "and   curr_code  = '901' "
            + "and   acct_month = ? "
            ;

  setString(1, businessDate);
  setString(2, businessDate);
  setString(3, getValue("wday.last_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   p_seqno = ? ";
      setString(4, pSeqno);
     }

  int  n = loadTable();
  setLoadData("curr.p_seqno");

  showLogMessage("I","","Load act_curr_hst Count: ["+n+"]");
 }
// ************************************************************************
 void loadMktCardConsume() throws Exception
 {
  extendField = "cons.";
  selectSQL = "a.p_seqno,"
            + "group_code,"
            + "acct_type,"
            + "card_type,"
            + "sum(a.consume_bl_amt-a.sub_bl_amt-a.foreign_bl_amt+"
            + "    a.consume_it_amt-a.sub_it_amt-a.foreign_it_amt) as dest_amt";
  daoTable  = "mkt_card_consume a,"
            + "           (select distinct p_seqno "
            + "            from cyc_cal_fund a,ptr_fundp b " 
            + "            where proc_date = '" + businessDate + "' "
            + "            and   a.fund_code = b.fund_code "
            + "            and   b.d_txn_cond = 'Y' "
            + "            and   a.proc_mark  = 'N' "
            + "            union "
            + "            select distinct p_seqno "
            + "            from cyc_calrow_fund  a,ptr_fundp b "
            + "            where proc_date = '" + businessDate + "' " 
            + "            and   a.fund_code = b.fund_code "
            + "            and   a.proc_mark  = 'N' "
            + "            and   b.d_txn_cond = 'Y' ) b "; 
  whereStr  = "where a.p_seqno    = b.p_seqno "
            + "and   a.acct_month = ? "
            ;

  setString(1 , getValue("wday.last_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   b.p_seqno = ? ";
      setString(2, pSeqno);
     }

  whereStr  = whereStr
            + "group by a.p_seqno,a.acct_type,a.group_code,a.card_type "
            + "having sum(a.consume_bl_amt-a.sub_bl_amt-a.foreign_bl_amt+"
            + "           a.consume_it_amt-a.sub_it_amt-a.foreign_it_amt) > 0 "
            + "order by a.p_seqno,a.acct_type,a.group_code,a.card_type ";

  int  n = loadTable();
  setLoadData("cons.p_seqno");

  showLogMessage("I","","Load mkt_card_consume Count: ["+n+"]");
  return;
 }
// ************************************************************************
 void selectActAcctHstx() throws Exception
 {
  extendField = "chst.";
  selectSQL = "acct_month,"
            + "min_pay,"
            + "stmt_last_ttl";
  daoTable  = "act_acct_hst";
  whereStr  = "where p_seqno = ?  "
            + "and   curr_code  = '901' "
            + "and   last_payment_date >= ? "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("payment_date")); 

  hstCnt = selectTable();
 }
// ************************************************************************
 void  loadPtrActcode() throws Exception
 {
  extendField = "pacd.";
  selectSQL = "d.reference_no";
  daoTable  = "bil_bill d, ptr_actcode e";
  whereStr  = "where e.revolve = 'Y' "
            + "and   d.acct_code = e.acct_code "
            + "and   p_seqno in (select p_seqno "
            + "                  from   cyc_cal_fund "
            + "                  where proc_date = ?  "
            + "                  untion "
            + "                  select p_seqno "
            + "                  from   cyc_calrow_fund "
            + "                  where proc_date = ?  "
            + "                  group by p_seqno ) "
            + "and   d.curr_code = '901' "
            ;

  setString(1 , businessDate);
  setString(2 , businessDate);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   a.p_seqno = ? ";
      setString(3, pSeqno);
     }                  

  int  n = loadTable();
  setLoadData("pacd.reference_no");

  showLogMessage("I","","Load ptr_actcode Count: ["+n+"]");
 }
// ************************************************************************
 int  selectActAcctHst0() throws Exception
 {
  selectSQL = "dest_amt as hst_dest_amt ";
  daoTable  = "act_acct_hst";
  whereStr  = "where p_seqno    = ? "
            + "and   acct_month = ? "
            + "and   billed_end_bal_bl + billed_end_bal_ao +    "
            + "      billed_end_bal_ca + billed_end_bal_id +    "
            + "      billed_end_bal_it + billed_end_bal_ot >= ? ";

  setString(1 , getValue("p_seqno"));
  setString(2 , comm.lastMonth(businessDate));
  setInt(3    , getValueInt("parm.rc_sub_amt"));

  selectTable();

  if ( notFound.equals("Y") ) tempDestAmt = 0;
  else tempDestAmt = getValueInt("hst_dest_amt")*getValueDouble("parm.rc_sub_rate")/100.0; 

  return(0);
 }
// ************************************************************************
 int  selectCycAcmm() throws Exception
 {
  selectSQL = "1 as xx_cnt";
  daoTable  = "cyc_acmm_"+getValue("wday.stmt_cycle"); 
  whereStr  = "where  p_seqno       = ? "
            + "and    send_paper    = 'N' "
            + "and    send_internet = 'Y' "
            ;

  setString(1 , getValue("p_seqno"));

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int  selectActAcno() throws Exception
 {
  extendField = "acnx.";
  selectSQL = "stat_send_paper,"
            + "stat_send_s_month,"
            + "stat_send_e_month,"
            + "stat_send_internet,"
            + "stat_send_s_month2,"
            + "stat_send_e_month2";
  daoTable  = "act_acno";
  whereStr  = "where  p_seqno       = ? "
            ;

  setString(1 , getValue("p_seqno"));


  selectTable();

  String statSMonth = "";
  String statEMonth = "";
  if (getValue("acnx.stat_send_paper").equals("Y"))
     { 
      statSMonth = getValue("acnx.stat_send_s_month");
      statEMonth = getValue("acnx.stat_send_e_month");

      if (getValue("acnx.stat_send_s_month").length()==0) statSMonth = "000000";
      if (getValue("acnx.stat_send_e_month").length()==0) statEMonth = "999912";

      if ((getValue("wday.this_acct_month").compareTo(statSMonth)>=0)&&
          (getValue("wday.this_acct_month").compareTo(statEMonth)<=0))
         {
          setValue("stat_send_paper","Y");
         }
      else
         {
          setValue("stat_send_paper","N");
         }
       }
    else
       {
        setValue("stat_send_paper","N");
       }

    if (getValue("acnx.stat_send_internet").equals("Y"))
       { 
        statSMonth = getValue("acnx.stat_send_s_month2");
        statEMonth = getValue("acnx.stat_send_e_month2");
        if (getValue("acnx.stat_send_s_month2").length()==0) statSMonth = "000000";
        if (getValue("acnx.stat_send_e_month2").length()==0) statEMonth = "999912";
        if ((getValue("wday.this_acct_month").compareTo(statSMonth)>=0)&&
            (getValue("wday.this_acct_month").compareTo(statEMonth)<=0))  
           {
            setValue("stat_send_internet","Y");
           }
        else
           {
            setValue("stat_send_internet","N");
           }
       }
    else
       {
        setValue("stat_send_internet","N");
       }

  if (getValue("stat_send_paper").equals("Y")) return(1);
  if (getValue("stat_send_internet").equals("N")) return(1);
            
  return(0);
 }
// ************************************************************************
 int  selectCycPyaj(int inti) throws Exception
 {
  boolean DEBUGPyaj =false;

  setValue("curr.p_seqno"  ,getValue("p_seqno"));
  int cnt1 = getLoadData("curr.p_seqno");
  if (cnt1==0)
     { 
      showLogMessage("I","", "act_acct_curr p_seqno["+getValue("p_seqno")
                           + "] not found");
      return(1);
     }
  
  if (getValueDouble("curr.dc_min_pay_bal") > 0) return(1);

  setValue("agnn.acct_type",getValue("acct_type"));
  cnt1 = getLoadData("agnn.acct_type");

  setValue("ws_minpay_rate" , getValue("agnn.mp_3_rate"));
  if (getValueDouble("min_pay_rate") > 0)
     if ((businessDate.substring(0,6).compareTo(getValue("min_pay_rate_s_month")) >= 0) &&
         (businessDate.substring(0,6).compareTo(getValue("min_pay_rate_e_month")) <= 0))
        setValue("ws_minpay_rate" , getValue("min_pay_rate"));

  if (selectCycPyaj1()!=0) return(1);

  if (getValue("settle_date").length()==0)
      setValue("settle_date",businessDate);

  if (getValue("new_cycle_month").equals(getValue("settle_date").substring(0,6)))
     setValue("temp.acct_date",getValue("last_interest_date"));
  else
     setValue("temp.acct_date",comm.nextMonthDate(getValue("settle_date"),-1));

  if (selectCycPyaj2A(inti)!=0)
     {
//     if (getValue("p_seqno").equals(debug_p))
//       showLogMessage("I","","STWO 20200120-01 ");
      if (selectCycPyaj2B(inti)!=0) return(1);
//     if (getValue("p_seqno").equals(debug_p))
//     showLogMessage("I","","STWO 20200120-02 ");
     }
//     if (getValue("p_seqno").equals(debug_p))
//     showLogMessage("I","","STWO 20200120-OK ");

  return(0);
 }
// ************************************************************************
 int  selectCycPyaj1() throws Exception
 {
  boolean DEBUGPyaj1 =false;
  extendField = "pyaj.";
  selectSQL = "max(pay_date) as payment_date,"
            + "max(settle_date) as settle_date";
  daoTable  = "act_pay_hst a, cyc_pyaj b ";
  whereStr  = "where a.p_seqno = b.p_seqno " 
            + "and   a.pay_date = b.payment_date "
            + "and   pay_amt    = dc_payment_amt "
            + "and   a.payment_type = b.payment_type "
            + "and   b.curr_code = decode(a.curr_code,'','901',a.curr_code) "
            + "and   substr(batch_no,9,4) not in ('9005','9007','9008','9009','9999') "
            + "and   a.p_seqno = ? "
            + "and   b.curr_code = '901' "
            + "group by b.p_seqno ";

  setString(1 , getValue("p_seqno"));

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  loadPtrFundData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "ptr_fund_data";
  whereStr  = "WHERE TABLE_NAME = 'PTR_FUNDP' "
            + "and   data_type in ('3','4','5','A') "
            + "and   data_key in "
            + "     (select fund_code from ptr_fundp "
            + "      WHERE apr_flag      = 'Y' "
            + "      AND   (stop_flag  = 'N' "
            + "       OR    (stop_flag = 'Y' "
            + "         and   ? < stop_date)) "
            + "       and   feedback_type in ('1','2') "
            + "       and   fund_feed_flag = 'Y' " 
            ;

  setString(1, businessDate);

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr 
                + "and   fund_code = ? ";
      setString(2, fundCode);
     }

  whereStr  = whereStr
            + "          ) "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load ptr_fund_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectPtrFundData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectPtrFundData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectPtrFundData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectPtrFundData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectPtrFundData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
     }

  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (dataNum==2)
        {
         if (getValue("data.data_code",intm).length()!=0)
            {
             if (col1.length()!=0)
                {
                 if (!getValue("data.data_code",intm).equals(col1)) continue;
                }
              else
                {
                 if (sel.equals("1")) continue;
                }
            }
        }
     if (getValue("data.data_code2",intm).length()!=0)
        {
         if (col2.length()!=0)
            {
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
            }
          else
            {
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
         if (col3.length()!=0)
            {
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
            }
          else
            {
             continue;
            }
        }
     

     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 void  loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,ptr_fund_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'PTR_FUNDP' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  = '6' "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_fstpgp_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktMchtgpData(String col1,String col2,String sel,String dataType) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("mcht.data_key" , getValue("data_key"));
  setValue("mcht.data_type",dataType);
  setValue("mcht.data_code",col1);

  int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  int okFlag=0;
  for (int inti=0;inti<cnt1;inti++)
    {
     if ((getValue("mcht.data_code2",inti).length()==0)||
         ((getValue("mcht.data_code2",inti).length()!=0)&&
          (getValue("mcht.data_code2",inti).equals(col2))))
        {
         okFlag=1;
         break;
        }
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 int insertCycCobrandFund(int inti) throws Exception
 {
  dateTime();
  extendField = "cobr.";

  setValue("cobr.proc_date"            , businessDate);
  setValue("cobr.program_code"         , getValue("fund_code").substring(0,4));
  setValue("cobr.cobrand_code"         , getValue("parm.cobrand_code",inti));
  setValue("cobr.card_code"            , "C");
  setValue("cobr.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cobr.p_seqno"              , getValue("p_seqno"));
  setValue("cobr.acct_type"            , getValue("acct_type"));
  setValue("cobr.acct_month"           , getValue("acct_month"));
  setValue("cobr.major_card_no"        , getValue("major_card_no")); 
  setValueInt("cobr.fund_amt"          , getValueInt("beg_tran_amt"));
  setValue("cobr.mod_time"             , sysDate+sysTime);
  setValue("cobr.mod_pgm"              , javaProgram);

  daoTable  = "cyc_cobrand_fund";

  insertTable();

  return(0);
 }
// ************************************************************************
 void  loadPtrActgeneralN() throws Exception
 {
  extendField = "agnn.";
  selectSQL = "acct_type,"
            + "mp_3_rate";
  daoTable  = "ptr_actgeneral_n";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("agnn.acct_type");

  showLogMessage("I","","Load ptr_actgeneral_n Count: ["+n+"]");
 }
// ************************************************************************
 int   calThresholdAmt1(int inti,double tempDAmt) throws Exception
 {
  double dTempTotAmt = 0;
  int    tempLAmt     = 0;
  int    loadCnt1       = 0; 
  double dTempDAmt   = 0;

  if (tempDAmt<0) tempDAmt = tempDAmt*-1;

  if ((getValueInt("parm.fund_s_amt_5",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_5",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_5",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_5",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_5",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_5",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_4",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_4",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_4",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_4",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_4",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_4",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_3",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_3",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_3",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_3",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_3",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_3",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_2",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_2",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_2",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_2",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_2",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_2",inti);
       tempDAmt = tempLAmt;
      }

  if ((getValueInt("parm.fund_s_amt_1",inti) > 0)&&
      (tempDAmt >= getValueInt("parm.fund_s_amt_1",inti)))
      {
       tempLAmt     = getValueInt("parm.fund_s_amt_1",inti);
       tempDAmt     = tempDAmt - (getValueInt("parm.fund_s_amt_1",inti) - 1);
       dTempDAmt   = tempDAmt*getValueDouble("parm.fund_rate_1",inti)/100.0;
       dTempTotAmt = dTempTotAmt + dTempDAmt
                      + getValueDouble("parm.fund_amt_1",inti);
       tempDAmt = tempLAmt;
      }

  return((int)Math.round(dTempTotAmt));
 }
// ************************************************************************
 int   calThresholdAmt2(int inti,double tempDCnt,double tempDAmt) throws Exception
 {
  int signFlag=1;
  if (tempDCnt<0) tempDCnt = tempDCnt*-1;
  if (tempDAmt<0) signFlag = -1;

  if ((getValueInt("parm.fund_s_amt_5",inti) > 0)&&
      (tempDCnt >= getValueInt("parm.fund_s_amt_5",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_5",inti)/100.0
                + getValueDouble("parm.fund_amt_5",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_4",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_4",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_4",inti)/100.0
                + getValueDouble("parm.fund_amt_4",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_3",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_3",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_3",inti)/100.0
                + getValueDouble("parm.fund_amt_3",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_2",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_2",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_2",inti)/100.0
                + getValueDouble("parm.fund_amt_2",inti))*signFlag)*signFlag); 
  else if ((getValueInt("parm.fund_s_amt_1",inti) > 0)&&
           (tempDCnt >= getValueInt("parm.fund_s_amt_1",inti)))
       return((int)Math.round((tempDAmt*getValueDouble("parm.fund_rate_1",inti)/100.0
                 + getValueDouble("parm.fund_amt_1",inti))*signFlag)*signFlag); 

  return(0);
 }
// ************************************************************************
 int  selectCycPyaj2A(int inti) throws Exception
 {
  if (testOld)
     {
      selectActCurrHstOld();
      if (selectActAcctHstOld()!=0) return(1);
     }
  else
     {
      selectActAcctCurr();
      if (selectActAcct()!=0) return(1);
     }
  if (selectActChkautopay(inti)!=0) return(1);
  if (getValueDouble("cpay.transaction_amt")==0) return(1);

  extendField = "py2a.";
  selectSQL = "curr_code,"
            + "payment_type,"
            + "reference_no,"
            + "dc_payment_amt";
  daoTable  = "cyc_pyaj";
  whereStr  = "where  p_seqno =  ? "
            + "and    settle_date = ? "
            + "and    settle_date = ? "
            + "and    (payment_type in ('AUT1','REFU') "
            + " or     (class_code = 'A' "
            + "   and   dc_payment_amt < 0)) "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("pyaj.settle_date"));
  setString(3 , businessDate);

//   showLogMessage("I","","STEP A1 p_seqno["+ getValue("p_seqno")          +"]");
//   showLogMessage("I","","STEP A1 set_dat["+ getValue("pyaj.settle_date") +"]");

  int recCnt = selectTable();

     setValue("cmp.from_amt1" , "0");
     setValue("cmp.from_amt2" , "0");
     setValue("cmp.to_amt1"   , "0");
     setValue("cmp.to_amt2"   , "0");

  for (int intk=0;intk<recCnt;intk++)
    {

     if (getValue("py2a.payment_type",intk).equals("REFU"))
         setValueDouble("cmp.from_amt1" , getValueDouble("cmp.from_amt1")
                                        + getValueDouble("py2a.dc_payment_amt",intk));

      setValueDouble("cmp.from_amt2" , getValueDouble("cmp.from_amt2")
                                     + getValueDouble("py2a.transaction_amt",intk));

     if (!Arrays.asList("'REFU","AUT1").contains(getValue("py2a.payment_type",intk)))
         setValueDouble("cmp.to_amt1" , getValueDouble("cmp.to_amt1")
                                      + getValueDouble("py2a.dc_payment_amt",intk));

     setValue("pacd.reference_no"  ,getValue("py2a.reference_no",intk));
     int cnt1 = getLoadData("pacd.reference_no");

     if (cnt1!=0)
        { 
         if (!Arrays.asList("'REFU","AUT1").contains(getValue("py2a.payment_type",intk)))
            setValueDouble("cmp.to_amt2" , getValueDouble("cmp.to_amt2")
                                         + getValueDouble("py2a.dc_payment_amt",intk));
        }
     else
        { 
         if (!Arrays.asList("'REFU","AUT1").contains(getValue("py2a.payment_type",intk)))
            setValueDouble("cmp.to_amt2" , getValueDouble("cmp.to_amt2")
                                         + Math.round(getValueDouble("py2a.dc_payment_amt",intk)
                                         * getValueDouble("ws_minpay_rate")));
        }
    }
/* 
       if (getValue("p_seqno").equals(debug_p))
       {
      showLogMessage("I","","step 4A 20200120 ["+ getValueDouble("cmp.from_amt1") +"]");
      showLogMessage("I","","step 4B 20200120 ["+ getValueDouble("cpay.transaction_amt") +"]");
      showLogMessage("I","","step 4C 20200120 ["+ getValueDouble("cura.dc_min_pay") +"]");
      showLogMessage("I","","step 4D 20200120 ["+ getValueDouble("cmp.to_amt2")  +"]");
      showLogMessage("I","","step 4E 20200120 ["+ getValueDouble("cmp.from_amt2") +"]");
      showLogMessage("I","","step 4F 20200120 ["+ getValueDouble("cura.dc_ttl_amt") +"]");
      showLogMessage("I","","step 4G 20200120 ["+ getValueDouble("cmp.to_amt1") +"]");
        }
*/
     if ((getValueDouble("cmp.from_amt1") + getValueDouble("cpay.transaction_amt"))  
                                          >= (getValueDouble("cura.dc_min_pay")
                                          +   getValueDouble("cmp.to_amt2"))) return(0);
                                          
//     if (getValue("p_seqno").equals(debug_p))
//    showLogMessage("I","","STWO 20200120-03 ");
     if ((getValueDouble("cmp.from_amt2")  +  getValueDouble("cpay.transaction_amt")) 
                                           >= (getValueDouble("cura.dc_ttl_amt")
                                           +   getValueDouble("cmp.to_amt1"))) return(0); 
//     if (getValue("p_seqno").equals(debug_p))
//   showLogMessage("I","","STWO 20200120-04 ");
  return(1);
 }
// ************************************************************************
 int selectActAcct() throws Exception
 {
  extendField = "acct.";
  selectSQL = "1 as temp_cnt";
  daoTable  = "act_acct_"+getValue("wday.stmt_cycle");
  whereStr  = "where p_seqno    = ?  "
            + "and  last_payment_date >= ? "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("pyaj.payment_date")); 

  int recCnt = selectTable();
  if (recCnt==0) return(1);
  return(0);
 }
// ************************************************************************
 int selectActAcctHstOld() throws Exception
 {
  extendField = "acct.";
  selectSQL = "1 as temp_cnt";
  daoTable  = "act_acct_hst";
  whereStr  = "where p_seqno    = ?  "
            + "and  last_payment_date >= ? "
            + "and  acct_month         = ? "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("pyaj.payment_date")); 
  setString(3 , getValue("wday.last_acct_month")); 

  int recCnt = selectTable();
  if (recCnt==0) return(1);
  return(0);
 }
// ************************************************************************
 int selectActAcctCurr() throws Exception
 {
  extendField = "cura.";
  selectSQL = "dc_min_pay,"
            + "dc_ttl_amt";
  daoTable  = "act_acct_curr_"+getValue("wday.stmt_cycle");
  whereStr  = "where p_seqno    = ?  "
            + "and  curr_code = '901' "
            ;

  setString(1 , getValue("p_seqno"));

  int recCnt = selectTable();
  if (recCnt==0) 
     {
      setValueDouble("cura.dc_min_pay" , 0);
      setValueDouble("cura.dc_ttl_amt" , 0);
     }
  return(0);
 }
// ************************************************************************
 int selectActCurrHstOld() throws Exception
 {
  extendField = "cura.";
  selectSQL = "min_pay,"
            + "stmt_last_ttl_amt as dc_ttl_amt";
//          + "stmt_this_ttl_amt as dc_ttl_amt";
  daoTable  = "act_curr_hst";
  whereStr  = "where p_seqno   = ?  "
            + "and  curr_code  = '901' "
            + "and  acct_month = ? "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("wday.last_acct_month"));

  int recCnt = selectTable();
  if (recCnt==0) 
     {
      setValueDouble("cura.dc_min_pay" , 0);
      setValueDouble("cura.dc_ttl_amt" , 0);
     }
  return(0);
 }
// ************************************************************************
 int selectActChkautopay(int inti) throws Exception
 {
  extendField = "cpay.";
  selectSQL = "nvl(sum(transaction_amt),0) as transaction_amt";
  daoTable  = "act_chkautopay";
  whereStr  = "where p_seqno    = ?  "
            + "and  curr_code   = '901' "
            + "and  status_code = '00' "
            + "and  enter_acct_date <= ? "
            + "and  enter_acct_date > ? "
            + "and  transaction_amt > 0 "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("pyaj.settle_date"));

     showLogMessage("I","","STEP B1 new_cycle_month ["+ getValue("new_cycle_month")  +"]");
   
  if (getValue("new_cycle_month").equals(getValue("pyaj.settle_date").substring(0,6)))
     {
      setString(3 , getValue("last_interest_date"));
     showLogMessage("I","","STEP B2  ["+ getValue("last_interest_date") +"]");
     }
  else
     {
      setString(3 , comm.lastMonth(getValue("pyaj.settle_date")));
     showLogMessage("I","","STEP B3  ["+ comm.lastMonth(getValue("pyaj.settle_date"))  +"]");
     }
     
  if (getValue("parm.autopay_digit_cond",inti).equals("Y"))
     {
      whereStr  = whereStr 
                + "and substr(autopay_acct_no,7,2) in (select sub_acct_no from dbc_digital_parm)";   
     showLogMessage("I","","STEP B6  ["+ comm.lastMonth(getValue("pyaj.settle_date"))  +"]");
     }

  int recCnt = selectTable();

  if (recCnt==0) return(1);
  if (getValueDouble("cpay.transaction_amt")==0) return(1);

  return(0);
 }
// ************************************************************************
 int  selectCycPyaj2B(int inti) throws Exception
 {
  if (selectActAcctHst()!=0) return(1);
  selectActCurrHst(); 
  if (selectActChkautopay(inti)!=0) return(1);
  if (getValueDouble("cpay.transaction_amt")==0) return(1);

  extendField = "py2a.";
  selectSQL = "curr_code,"
            + "payment_type,"
            + "reference_no,"
            + "dc_payment_amt";
  daoTable  = "cyc_pyaj";
  whereStr  = "where  p_seqno =  ? "
            + "and    settle_date = ? "
            + "and    settle_date = ? "
            + "and    (payment_type in ('AUT1','REFU') "
            + " or     (class_code = 'A' "
            + "   and   dc_payment_amt < 0)) "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("pyaj.settle_date"));
  setString(3 , getValue("acct.stmt_cycle_date"));

  int recCnt = selectTable();

     setValue("cmp.from_amt1","0");
     setValue("cmp.from_amt2","0");
     setValue("cmp.to_amt1","0");
     setValue("cmp.to_amt2","0");

  for (int intk=0;intk<recCnt;intk++)
    {
     if (getValue("py2a.payment_type",intk).equals("REFU"))
         setValueDouble("cmp.from_amt1" , getValueDouble("cmp.from_amt1")
                                        + getValueDouble("py2a.dc_payment_amt",intk));

      setValueDouble("cmp.from_amt2" , getValueDouble("cmp.from_amt2")
                                     + getValueDouble("py2a.transaction_amt",intk));

     if (!Arrays.asList("'REFU","AUT1").contains(getValue("py2a.payment_type",intk)))
         setValueDouble("cmp.to_amt1" , getValueDouble("cmp.to_amt1")
                                      + getValueDouble("py2a.dc_payment_amt",intk));

     setValue("pacd.reference_no"  ,getValue("py2a.reference_no",intk));
     int cnt1 = getLoadData("pacd.reference_no");

     if (cnt1!=0)
        { 
         if (!Arrays.asList("'REFU","AUT1").contains(getValue("py2a.payment_type",intk)))
            setValueDouble("cmp.to_amt2" , getValueDouble("cmp.to_amt2")
                                         + getValueDouble("py2a.dc_payment_amt",intk));
        }
     else
        { 
         if (!Arrays.asList("'REFU","AUT1").contains(getValue("py2a.payment_type",intk)))
            setValueDouble("cmp.to_amt2" , getValueDouble("cmp.to_amt2")
                                         + Math.round(getValueDouble("py2a.dc_payment_amt",intk)
                                         * getValueDouble("ws_minpay_rate")));
        }
    }
/*
       if (getValue("p_seqno").equals(debug_p))
       {
      showLogMessage("I","","step 4A 20200120 ["+ getValueDouble("cmp.from_amt1") +"]");
      showLogMessage("I","","step 4B 20200120 ["+ getValueDouble("cpay.transaction_amt") +"]");
      showLogMessage("I","","step 4C 20200120 ["+ getValueDouble("cura.dc_min_pay") +"]");
      showLogMessage("I","","step 4D 20200120 ["+ getValueDouble("cmp.to_amt2")  +"]");
      showLogMessage("I","","step 4E 20200120 ["+ getValueDouble("cmp.from_amt2") +"]");
      showLogMessage("I","","step 4F 20200120 ["+ getValueDouble("cura.dc_ttl_amt") +"]");
      showLogMessage("I","","step 4G 20200120 ["+ getValueDouble("cmp.to_amt1") +"]");
        }
*/
  if ((getValueDouble("cmp.from_amt1") + getValueDouble("cpay.transaction_amt"))  
                                       >= (getValueDouble("cura.dc_min_pay")
                                       +   getValueDouble("cmp.to_amt2"))) return(0);
//     if (getValue("p_seqno").equals(debug_p))
//    showLogMessage("I","","STWO 20200120-03 ");
                                          
  if ((getValueDouble("cmp.from_amt2")  +  getValueDouble("cpay.transaction_amt")) 
                                        >= (getValueDouble("cura.dc_ttl_amt")
                                        +   getValueDouble("cmp.to_amt1"))) return(0); 
//     if (getValue("p_seqno").equals(debug_p))
//   showLogMessage("I","","STWO 20200120-04 ");
  return(1);
 }
// ************************************************************************
 int selectActAcctHst() throws Exception
 {
  extendField = "acct.";
  selectSQL = "acct_month,"
            + "stmt_cycle_date";
  daoTable  = "act_acct_hst";
  whereStr  = "where p_seqno    = ?  "
            + "and  last_payment_date >= ? "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("pyaj.payment_date")); 

  int recCnt = selectTable();
  if (recCnt==0) return(1);
  return(0);
 }
// ************************************************************************
 int selectActCurrHst() throws Exception
 {
  extendField = "cura.";
  selectSQL = "min_pay as dc_min_pay,"
            + "stmt_last_ttl as dc_ttl_amt";
  daoTable  = "act_curr_hst";
  whereStr  = "where p_seqno   = ?  "
            + "and  curr_code  = '901' "
            + "and  acct_month = ? "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("acct.acct_month"));

//     if (getValue("p_seqno").equals(debug_p))
//   showLogMessage("I","","STEP 5 [" + getValue("acct.acct_month") +"]");

  int recCnt = selectTable();
  if (recCnt==0) 
     {
      setValueDouble("cura.dc_min_pay" , 0);
      setValueDouble("cura.dc_ttl_amt" , 0);
     }

  return(0);
 }
// ************************************************************************
 void selectDbcDigitalParm() throws Exception
 {
  extendField = "digi.";
  selectSQL = "sub_acct_no";
  daoTable  = "dbc_digital_parm";
  whereStr  = "";

  digitCnt = selectTable();

  return;
 }
// ************************************************************************
 void loadCycPyajAuto() throws Exception
 {
  extendField = "auto.";
  selectSQL = "a.p_seqno,"
            + "sum(decode(a.class_code  ,'A',a.payment_amt,0)) as adjust_amt,"
//          + "sum(decode(a.class_code  ,'B',a.payment_amt,0)) as adfund_amt,"
            + "sum(decode(a.class_code  ,'B',0,0)) as adfund_amt,"
            + "sum(decode(class_code,'P', decode(fund_code,'',decode(a.payment_type,"
            + "    'AUT1',0,'REFU',0,'OP01',0,'OP02',0,'OP03',0,'OP04',0,'AI01',0,'DR11',0,"
            + "    a.payment_amt),0),0)) as other_pay_amt,"
            + "sum(decode(a.payment_type,'AUT1',a.payment_amt,0)) as payment_amt,"
            + "sum(decode(a.payment_type,'REFU',a.payment_amt,0)) as refund_amt";
  daoTable  = "cyc_pyaj a,"
            + "    (select distinct p_seqno from cyc_cal_fund " 
            + "     where proc_date = '" + businessDate + "' "
            + "     union "
            + "     select distinct p_seqno from cyc_calrow_fund "
            + "     where proc_date = '" + businessDate + "') b ";
  whereStr  = "where a.settle_date = ? "
            + "and   a.p_seqno     =  b.p_seqno "
            + "and   a.payment_date  >  ? "
            + "and   a.payment_date  <= ? "
            + "and   (a.class_code != 'A' " 
            + " or    (a.class_code = 'A' "
            + "  and   a.payment_amt  < 0) "
            + " or     a.class_code = 'B') "
            + "and   a.curr_code      = '901' "
            ;

  setString(1 , businessDate);
  setString(2 , getValue("wday.last_close_date"));
  setString(3 , getValue("wday.this_close_date"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   a.p_seqno = ? ";
      setString(4, pSeqno);
     }                  
  whereStr  = whereStr 
            + "group by a.p_seqno ";

  int  n = loadTable();
  setLoadData("auto.p_seqno");

  showLogMessage("I","","Load cyc_pyaj_auto Count: ["+n+"]");
 }
// ************************************************************************
 void selectCycPyajHst() throws Exception
 {
  extendField = "pyht.";
  selectSQL = "p_seqno,"
            + "settle_date,"
            + "sum(decode(a.class_code  ,'A',a.payment_amt,0)) as adjust_amt,"
            + "sum(decode(a.class_code  ,'B',0,0)) as adfund_amt,"
            + "sum(decode(a.payment_type,'AUT1',a.payment_amt,0)) as payment_amt,"
            + "sum(decode(class_code,'P', decode(fund_code,'',decode(a.payment_type,"
            + "    'AUT1',0,'REFU',0,'OP01',0,'OP02',0,'OP03',0,'OP04',0,'AI01',0,'DR11',0,"
            + "    a.payment_amt),0),0)) as other_pay_amt,"
            + "sum(decode(a.payment_type,'REFU',a.payment_amt,0)) as refund_amt";
  daoTable  = "cyc_pyaj a";
  whereStr  = "where p_seqno       =  ? "
            + "and   settle_date   !=  ?  "
            + "and   (a.class_code != 'A' "
            + " or    (a.class_code = 'A' "
            + "  and   a.payment_amt  < 0)) "
            + "and   a.curr_code      = '901' "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , businessDate);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and   a.p_seqno = ? ";
      setString(3, pSeqno);
     }                  
  whereStr  = whereStr 
            + "group by a.p_seqno,settle_date "
            + "order by p_seqno,settle_date desc  ";

  pyajhstCnt = selectTable();
  return;
 }
// ************************************************************************
 int selectActCurrHst1(int intk) throws Exception
 {
 if (getValue("pyht.settle_date",intk).length()==0) return(1);
  if (pSeqno.length()>0)
     {
  showLogMessage("I","","20200315 STEP 5.0 ["+ intk +"]");
  showLogMessage("I","","20200315 STEP 5.1 ["+ getValue("pyht.settle_date",intk) +"]");
  showLogMessage("I","","20200315 STEP 5.2 ["+ getValue("p_seqno"));                                             
  showLogMessage("I","","20200315 STEP 5.3 ["+ getValue("pyht.settle_date",intk) +"]");
  showLogMessage("I","","20200315 STEP 5.4 ["+ comm.nextMonth(getValue("pyht.settle_date",intk),-1)+"]");
  showLogMessage("I","","20200315 STEP 5.5 ["+ comm.nextMonth(getValue("pyht.settle_date",intk),-1).substring(0,6) +"]");
     }

  extendField = "acht.";
  selectSQL = "p_seqno,"
            + "min_pay,"
            + "min_pay_bal";
  daoTable  = "act_curr_hst";
  whereStr  = "where p_seqno     = ? "
            + "and   acct_month  = ? "
            ;

  setString(1 , getValue("p_seqno"));
  setString(2 , comm.nextMonth(getValue("pyht.settle_date",intk),-1).substring(0,6));

  int recCnt = selectTable();
  if (recCnt==0) return(1);
  return(0);
 }
// ************************************************************************
//         if (((getValueInt("parm.purch_rec_amt",inti)<=getValueInt("dest_max"))&&    // 20210318 maybe CycA300 check
//              (getValue("parm.purch_rec_amt_cond",inti).equals("Y")))||



}  // End of class FetchSample
