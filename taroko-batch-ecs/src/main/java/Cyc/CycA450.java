/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/05/04  V1.00.12  Allen Ho   cyc_A310                                   *
* 110/11/01  V1.01.01  Allen Ho   Bug mark=211101-A01 mantis=8969            *
* 110/11/23  V1.02.01  Allen Ho   Bug mark=211123-A02 mantis=9088            *
* 111/10/17  V1.02.02  Yang Bo    sync code from mega                        *
* 112-06-21  V1.02.03  Simon      去除 act_acct_curr.seq_no                  *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycA450 extends AccessDAO
{
 private final String PROGNAME = "關帳-新增當月帳齡資料處理程式 112/06/21  V1.02.03";
 CommFunction comm = new CommFunction();

 String businessDate = "";

 boolean debug = false;
 String pSeqno = "";

 long    totalCnt=0;
 int acagCnt=0,currCnt=0, actAcagCnt =0,delcurrCnt=0 ,specflagCnt=0;

 int paymentAmt = 0,insertCnt=0,updateCnt=0,pcodCnt=0,aflfCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA450 proc = new CycA450();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+ PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   if ( args.length == 2 )
      { pSeqno = args[1]; }
   
   if ( !connectDataBase() ) 
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","acct_month[" + getValue("wday.this_acct_month") + "]");
   showLogMessage("I","","stmt_cyclee["+ getValue("wday.stmt_cycle") +"]");

  showLogMessage("I","","=========================================");
  if (debug)
     {
      showLogMessage("I","","還原資料"); 
      showLogMessage("I","","   刪除 act_acag");
      totalCnt=0;
      deleteActAcagDebug();
      commitDataBase();
      showLogMessage("I","","total_count=["+totalCnt+"]");
      showLogMessage("I","","=========================================");
      showLogMessage("I","","   刪除 act_acag_curr");
      totalCnt=0;
      deleteActAcagCurrDebug();
       commitDataBase();
      showLogMessage("I","","total_count=["+totalCnt+"]");
      showLogMessage("I","","=========================================");
      showLogMessage("I","","   新增 act_acag");
      totalCnt=0;
      insertActAcagDebug();
       commitDataBase();
      showLogMessage("I","","total_count=["+totalCnt+"]");
      showLogMessage("I","","=========================================");
      showLogMessage("I","","   新增 act_acag_curr");
      totalCnt=0;
      insertActAcagCurrDebug();
      showLogMessage("I","","total_count=["+totalCnt+"]");
      commitDataBase();
      showLogMessage("I","","=========================================");
      showLogMessage("I","","   更新 act_acct");
      totalCnt=0;
      updateActAcctDebug();    
      showLogMessage("I","","total_count=["+totalCnt+"]");
      showLogMessage("I","","=========================================");
      commitDataBase();
     }
   
   showLogMessage("I","","   載入只欠年費掛失費資料 ");
   loadActDebt();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","   處理 cyc_acmm");
   selectCycAcmm();
   showLogMessage("I","","     Total process records["+totalCnt+"]");
   showLogMessage("I","","           insert act_acag  records[" + acagCnt +"]");
   showLogMessage("I","","           insert act_acag_curr  records["+currCnt+"]");
   showLogMessage("I","","           delete act_acag  records["+ actAcagCnt +"]");
   showLogMessage("I","","           delete act_acag_curr  records["+delcurrCnt+"]");
   showLogMessage("I","","           spec_flag             records["+specflagCnt+"]");
   showLogMessage("I","","           只欠年費掛失費        records["+aflfCnt+"]");
   showLogMessage("I","","=========================================");
   
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
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
      businessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
// ************************************************************************ 
  public int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1, businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void selectCycAcmm() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "rc_indicator,"
            + "spec_flag,"
            + "this_ttl_amt,"
            + "org_mp ";
  daoTable  = "cyc_acmm_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE (this_ttl_amt > 0 "
            + " or    (this_ttl_amt <= 0 "
            + "  and   overpay_amt  !=0)) "
            ;

  if (pSeqno.length()>0) 
     whereStr = whereStr  + "and   p_seqno = '"+ pSeqno +"' "; 

  openCursor();

  totalCnt=0;
  int okFlag=0;
  int cnt1=0;
  while( fetchTable() ) 
   {
    totalCnt++;
    if (pSeqno.length()>0) 
       {
        showLogMessage("I","","STEP 1 business_date : ["+ businessDate +"]");
        showLogMessage("I","","STEP 2 spec_flag     : ["+getValue("spec_flag")+"]");
       }
    setValue("pay_amt" , "0");
    if (getValue("spec_flag").equals("Y"))
       {
        selectActAcno();

  if (pSeqno.length()>0) 
     {
      showLogMessage("I","","STEP 3  : ["+ getValue("wday.this_acct_month") +"]");
      showLogMessage("I","","STEP 3  : ["+getValue("acno.spec_flag_month")+"]");
     }
        if (getValue("wday.this_acct_month").equals(getValue("acno.spec_flag_month")))
           {
            selectCycAcmmCurr();
           }
        else
           {
/*
  if ((getValue("p_seqno").equals("0002227830"))||
      (getValue("p_seqno").equals("0002229993"))||
      (getValue("p_seqno").equals("0002231043"))||
      (getValue("p_seqno").equals("0002230445")))
     {
      showLogMessage("I","","STEP 1  p_seqno : ["+ getValue("p_seqno") +"]");
      showLogMessage("I","","        spec_flag_month : ["+ getValue("acno.spec_flag_month") +"]");
     }
*/
            specflagCnt++;
            deleteActAcagAll();
            deleteActAcagCurrAll();
            selectCycAcmmCurr1();
           }
       }
    else
       {
        okFlag=0;
        if (getValueInt("this_ttl_amt")>0)
           {
            setValue("debt.p_seqno"   , getValue("p_seqno"));
            cnt1 = getLoadData("debt.p_seqno");
            if ((cnt1>0)&&
                (getValueInt("debt.dc_end_bal")==getValueInt("this_ttl_amt")))
               okFlag=1;
           }

        if (okFlag==1) 
           {
            aflfCnt++;
/*
  if ((getValue("p_seqno").equals("0002227830"))||
      (getValue("p_seqno").equals("0002229993"))||
      (getValue("p_seqno").equals("0002231043"))||
      (getValue("p_seqno").equals("0002230445")))
     {
      showLogMessage("I","","STEP 2  p_seqno : ["+ getValue("p_seqno") +"]");
      showLogMessage("I","","        dc_end_bal   : ["+ getValueInt("debt.dc_end_bal") +"]");
      showLogMessage("I","","        this_ttl_amt : ["+ getValueInt("this_ttl_amt") +"]");
     }
*/
            deleteActAcagAll();
            deleteActAcagCurrAll();
            selectCycAcmmCurr1();
           }
        else
           {
            selectCycAcmmCurr();
           }
       }
    if (getValue("rc_indicator").equals("3")) updateActAcct();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void selectCycAcmmCurr() throws Exception
 {
  extendField = "cur0.";
  selectSQL = "curr_code,"
            + "this_minimum_pay,"
            + "dc_this_minimum_pay ";
  daoTable  = "cyc_acmm_curr_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE p_seqno = ? ";

  setString(1  , getValue("p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return;

  for ( int inti=0; inti<recCnt; inti++ )
    { 
     setValue("cacr.curr_code"             , getValue("cur0.curr_code",inti));
     setValue("cacr.this_minimum_pay"      , getValue("cur0.this_minimum_pay",inti));
     setValue("cacr.dc_this_minimum_pay"   , getValue("cur0.dc_this_minimum_pay",inti));

  if (pSeqno.length()>0) 
     {
      showLogMessage("I","","STEP A1 curr_code : ["+getValue("cur0.curr_code",inti)+"]");
      showLogMessage("I","","STEP A2 mp        : ["+getValue("cacr.this_minimum_pay")+"]");
      showLogMessage("I","","STEP A3 dc_mp     : ["+getValue("cacr.dc_this_minimum_pay")+"]");
     }

     selectActAcagCurr();
     deleteActAcag();
    } 
  return;
 }
// ************************************************************************
 void selectCycAcmmCurr1() throws Exception
 {
  extendField = "cur1.";
  selectSQL = "curr_code,"
            + "this_minimum_pay,"
            + "dc_this_minimum_pay";
  daoTable  = "cyc_acmm_curr_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE p_seqno = ? "
            + "AND   dc_this_minimum_pay > 0 ";

  setString(1  , getValue("p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return;

  for ( int inti=0; inti<recCnt; inti++ )
    {
     setValue("acag.acct_month"  , getValue("wday.this_acct_month"));
     setValue("cacr.curr_code"   , getValue("cur1.curr_code",inti)); 
     setValue("acag.pay_amt"     , getValue("cur1.this_minimum_pay",inti));
     setValue("acag.dc_pay_amt"  , getValue("cur1.dc_this_minimum_pay",inti));

     insertActAcagCurr();
     updateActAcag();
    } 
  return;
 }
// ************************************************************************
 void selectActAcagCurr() throws Exception
 {
  extendField = "aacr.";
  selectSQL = "acct_month,"
            + "pay_amt,"
            + "dc_pay_amt,"
            + "rowid as rowid";
  daoTable  = " act_acag_curr";
  whereStr  = "WHERE   p_seqno   = ? "
            + "AND     curr_code = ? "
            + "ORDER BY acct_month DESC";

  setString(1  , getValue("p_seqno"));
  setString(2  , getValue("cacr.curr_code"));

  int recCnt = selectTable();

  double tempLastMpBal = 0;
  double tempDcDouble = getValueDouble("cacr.dc_this_minimum_pay");

  if (pSeqno.length()>0) 
      showLogMessage("I","","STEP A4 temp_dc_double : ["+tempDcDouble + "]");

  for ( int inti=0; inti<recCnt; inti++ )
   {
    if (pSeqno.length()>0) 
       {
        showLogMessage("I","","STEP B1 acct_month : ["+getValue("aacr.acct_month",inti)+"]");
        showLogMessage("I","","STEP B2 pay_amt    : ["+getValue("aacr.pay_amt",inti)+"]");
        showLogMessage("I","","STEP B3 dc_pay_amt : ["+getValue("aacr.dc_pay_amt",inti)+"]");
       }
    
    setValue("acag.acct_month" , getValue("aacr.acct_month",inti));
    if ((getValueDouble("aacr.dc_pay_amt",inti)==0)||(tempDcDouble==0))
       {
        if (pSeqno.length()>0) 
           showLogMessage("I","","STEP C1 ");

        deleteActAcagCurr(inti);
        updateActAcag();
        continue;
       }
    if (tempDcDouble >= getValueDouble("aacr.dc_pay_amt",inti))
       {
        tempDcDouble = tempDcDouble 
                       - getValueDouble("aacr.dc_pay_amt",inti);
        tempLastMpBal = tempLastMpBal 
                         + getValueDouble("aacr.pay_amt",inti);
        if (pSeqno.length()>0) 
           {
            showLogMessage("I","","STEP D1 : ["+tempDcDouble +"]");
            showLogMessage("I","","STEP D2 : ["+tempLastMpBal+"]");
           }
        continue;
       }
    if (pSeqno.length()>0) 
       {
        showLogMessage("I","","STEP E1 : ["+tempDcDouble +"]");
        showLogMessage("I","","STEP E2 : ["+getValueDouble("aacr.pay_amt",inti)+"]");
        showLogMessage("I","","STEP E3 : ["+getValueDouble("aacr.dc_pay_amt",inti)+"]");
       }
//  setValueDouble("acag.pay_amt" , Math.round(
//                                temp_dc_double
//                                * (getValueDouble("aacr.pay_amt",inti)
//                                /  getValueDouble("aacr.dc_pay_amt",inti))+0.0000001
//                                )
//                                );

    setValueDouble("acag.pay_amt" , 
                commCurrAmt(getValue("cacr.curr_code"),
                              tempDcDouble
                              * (getValueDouble("aacr.pay_amt",inti)
                              /  getValueDouble("aacr.dc_pay_amt",inti))+0.0000001,
                              0));

    setValueDouble("acag.dc_pay_amt" , tempDcDouble);
     if (pSeqno.length()>0) 
        {
         showLogMessage("I","","STEP F1 : ["+getValueDouble("acag.pay_amt")+"]");
         showLogMessage("I","","STEP F2 : ["+getValueDouble("acag.dc_pay_amt")+"]");
        }
    if (getValue("cacr.curr_code").equals("901"))
       setValueDouble("acag.pay_amt" , getValueDouble("acag.dc_pay_amt"));
    tempDcDouble = 0;
     if (pSeqno.length()>0) 
        {
         showLogMessage("I","","STEP G1 : ["+getValueDouble("acag.pay_amt")+"]");
         showLogMessage("I","","STEP G2 : ["+getValueDouble("acag.dc_pay_amt")+"]");
        }
    if (getValueDouble("acag.dc_pay_amt")<=0)  // mark=211101-A01
       {
        deleteActAcagCurr(inti);
       }
    else updateActAcagCurr(inti);
    updateActAcag();
   } 

  if (tempDcDouble == 0) return;

  setValueDouble("acag.pay_amt" , 
                commCurrAmt("901",
                              (getValueDouble("cacr.this_minimum_pay")-tempLastMpBal)+0.0000001,
                              0));

  setValueDouble("acag.dc_pay_amt" , tempDcDouble);

  if (getValue("cacr.curr_code").equals("901"))
     setValueDouble("acag.dc_pay_amt", getValueDouble("acag.pay_amt"));

  if (pSeqno.length()>0) 
     {
      showLogMessage("I","","STEP H1 pay_amt    : ["+getValueDouble("acag.pay_amt")+"]");
      showLogMessage("I","","STEP H2 dc_pay_amt : ["+getValueDouble("acag.dc_pay_amt")+"]");
     }
  setValue("acag.acct_month" , getValue("wday.this_acct_month"));
  if (insertActAcagCurr()!=0) updateActAcagCurr1();
  if (insertActAcag()!=0) updateActAcag();
  return;
 }
// ************************************************************************
 int selectActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "spec_flag_month";
  daoTable  = "act_acno_"+ getValue("wday.stmt_cycle");
  whereStr  = "WHERE  p_seqno = ? ";

  setString(1  , getValue("p_seqno"));

  selectTable();
  if (getValue("acno.spec_flag_month").length()==0)
      setValue("acno.spec_flag_month","200001");

  setValue("acno.spec_flag_month",comm.nextMonth(getValue("acno.spec_flag_month")));

  return(0);
 }
// ************************************************************************
 int insertActAcag() throws Exception
 {
  acagCnt++;
  dateTime();
  extendField = "acag.";

  setValue("acag.p_seqno"                 , getValue("p_seqno"));
  setValue("acag.acct_type"               , getValue("acct_type"));
  setValue("acag.acct_month"              , getValue("wday.this_acct_month"));
  setValue("acag.stmt_cycle"              , getValue("wday.stmt_cycle"));
//setValue("acag.seq_no"                  , "1");
  setValue("acag.pay_amt"                 , getValue("acag.pay_amt"));
  setValue("acag.mod_time"                , sysDate+sysTime);
  setValue("acag.mod_pgm"                 , javaProgram);

  daoTable  = "act_acag";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int insertActAcagCurr() throws Exception
 {
  currCnt++;
  dateTime();
  extendField = "curr.";

  setValue("curr.p_seqno"                 , getValue("p_seqno"));
  setValue("curr.curr_code"               , getValue("cacr.curr_code"));
  setValue("curr.acct_type"               , getValue("acct_type"));
  setValue("curr.acct_month"              , getValue("wday.this_acct_month"));
  setValue("curr.stmt_cycle"              , getValue("wday.stmt_cycle"));
//setValue("curr.seq_no"                  , "1");
  setValue("curr.pay_amt"                 , getValue("acag.pay_amt"));
  setValue("curr.dc_pay_amt"              , getValue("acag.dc_pay_amt"));
  setValue("curr.mod_time"                , sysDate+sysTime);
  setValue("curr.mod_pgm"                 , javaProgram);

  daoTable  = "act_acag_curr";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void deleteActAcag() throws Exception
 {
  daoTable  = "act_acag";
  whereStr  = "WHERE p_seqno = ? "
            + "and   pay_amt = 0 "; 

  setString(1  , getValue("p_seqno"));

  deleteTable();

  return;
 }
// ************************************************************************
 void deleteActAcagAll() throws Exception
 {
  daoTable  = "act_acag";
  whereStr  = "WHERE  p_seqno = ? ";

  setString(1  , getValue("p_seqno"));

  deleteTable();

  return;
 }
// ************************************************************************
 void deleteActAcagCurrAll() throws Exception
 {
  daoTable  = "act_acag_curr";
  whereStr  = "WHERE  p_seqno = ? ";

  setString(1  , getValue("p_seqno"));

  deleteTable();

  return;
 }
// ************************************************************************
 void deleteActAcagCurr(int inti) throws Exception
 {
  daoTable  = "act_acag_curr";
  whereStr  = "WHERE  rowid = ? ";

  setRowId(1, getValue("aacr.rowid",inti));
  deleteTable();

  return;
 }
// ************************************************************************
 int updateActAcct() throws Exception
 {
   setValueInt("rc_min_pay_m0" , getValueInt("org_mp") - getValueInt("pay_amt"));
   if (getValueInt("rc_min_pay_m0") < 0) setValueInt("rc_min_pay_m0" , 0); 

  updateSQL = "rc_min_pay_m0 = ?,"
            + "rc_min_pay    = ?," 
            + "rc_min_pay_bal= ?"; 
  daoTable  = "act_acct";
  whereStr  = "where p_seqno     = ? ";

  setInt(1 , getValueInt("rc_min_pay_m0"));
  setInt(2 , getValueInt("org_mp"));
  setInt(3 , getValueInt("org_mp"));
  setString(4 , getValue("p_seqno"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int updateActAcag() throws Exception
 {
  updateSQL = "pay_amt = (select nvl(sum(pay_amt),0) "
            + "           from   act_acag_curr "
            + "           where  p_seqno    = a.p_seqno "
            + "           and    acct_month = a.acct_month) ";
  daoTable  = "act_acag a";
  whereStr  = "WHERE   p_seqno    = ? "
            + "AND     acct_month = ? ";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("acag.acct_month"));

  if (pSeqno.length()>0) 
     {
      showLogMessage("I","","STEP I1 : ["+getValue("p_seqno")         +"]");
      showLogMessage("I","","STEP I1 : ["+getValue("acag.acct_month") +"]");
     }
  updateTable();

  if ( notFound.equals("Y") ) insertActAcag1();

  return(0);
 }
// ************************************************************************
 int updateActAcagCurr(int inti) throws Exception
 {
  updateSQL = "pay_amt    = ?,"
            + "dc_pay_amt = ? ";
  daoTable  = "act_acag_curr";
  whereStr  = "where rowid  = ? ";

  setDouble(1 , getValueDouble("acag.pay_amt"));
  setDouble(2 , getValueDouble("acag.dc_pay_amt")); 
  setRowId(3  , getValue("aacr.rowid",inti));

  updateTable();

  return(0);
 }
// ************************************************************************
 int updateActAcagCurr1() throws Exception
 {
  updateSQL = "pay_amt    = pay_amt + ?,"
            + "dc_pay_amt = dc_pay_amt + ? ";
  daoTable  = "act_acag_curr";
  whereStr  = "WHERE p_seqno    = ? "
            + "AND   curr_code  = ? "
          //+ "AND   seq_no     = 1 "
            + "AND   acct_month = ? ";

  setDouble(1 , getValueDouble("acag.pay_amt"));
  setDouble(2 , getValueDouble("acag.dc_pay_amt")); 
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("cacr.curr_code"));
  setString(5 , getValue("wday.this_acct_month"));

  updateTable();

  return(0);
 }
// ************************************************************************
 int insertActAcag1() throws Exception
 {
  sqlCmd   = "insert into act_acag( "
           + "       p_seqno,"
         //+ "       seq_no,"
           + "       acct_type,"
           + "       acct_month,"
           + "       stmt_cycle,"
           + "       pay_amt,"
           + "       mod_time,"
           + "       mod_pgm"
           + "       ) "
           + "select p_seqno, "
         //+ "       max(1),"
           + "       max(acct_type),"
           + "       acct_month,"
           + "       max(stmt_cycle),"
           + "       sum(pay_amt),"
           + "       max(sysdate),"
           + "       max('"+javaProgram+"') "
           + "FROM   act_acag_curr "
           + "WHERE  p_seqno    = ? "
           + "AND    acct_month = ? "
           + "GROUP BY p_seqno,acct_month "
           ;
  daoTable  = "act_acag";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("acag.acct_month"));

  insertTable();

  sqlCmd   = "";

  return(0);
 }
// ************************************************************************
 int insertActAcagDebug() throws Exception
 {
  sqlCmd   = "insert into act_acag "
           + "select * "
           + "from act_acag_"+ getValue("wday.stmt_cycle")+" a "
           + "WHERE  exists (select 1 "
           + "               from cyc_acmm_"+ getValue("wday.stmt_cycle")
           + "               where p_seqno = a.p_seqno) "
           ;
  if (pSeqno.length()>0) 
     sqlCmd   = sqlCmd  + "and   p_seqno = '"+ pSeqno +"' "; 

  daoTable  = "act_acag";

  totalCnt = insertTable();

  sqlCmd   = "";

  return(0);
 }
// ************************************************************************
 int insertActAcagCurrDebug() throws Exception
 {
  sqlCmd   = "INSERT INTO act_acag_curr "
           + "SELECT * "
           + "FROM act_acag_curr_"+ getValue("wday.stmt_cycle") +" a "
           + "WHERE  exists (select 1 "
           + "               from cyc_acmm_"+ getValue("wday.stmt_cycle")
           + "               where p_seqno = a.p_seqno) "
           ;
           ;

  if (pSeqno.length()>0) 
     sqlCmd   = sqlCmd  + "and   p_seqno = '"+ pSeqno +"' "; 

  daoTable  = "act_acag_curr";

  totalCnt = insertTable();

  sqlCmd   = "";

  return(0);
 }
// ************************************************************************
 int updateActAcctDebug() throws Exception
 {
  updateSQL = "(rc_min_pay_m0,rc_min_pay,rc_min_pay_bal) = ( "
            + " select rc_min_pay_m0, "
            + "        rc_min_pay, "
            + "        rc_min_pay_bal "
            + " from   act_acct_"+ getValue("wday.stmt_cycle") +" "
            + " where  p_seqno = a.p_seqno) ";
  daoTable  = "act_acct a";
  whereStr  = "WHERE  exists (select 1 "
            + "               from cyc_acmm_"+ getValue("wday.stmt_cycle")
            + "               where p_seqno = a.p_seqno) "
            ;

  if (pSeqno.length()>0) 
     whereStr  = whereStr + "and p_seqno = '"+ pSeqno +"' ";
   
  updateTable();

  return(0);
 }
// ************************************************************************
 void deleteActAcagDebug() throws Exception
 {
  daoTable  = "act_acag a";
  whereStr  = "WHERE  p_seqno in (select p_seqno "
            + "                   from cyc_acmm_"+ getValue("wday.stmt_cycle") + " ) "
            ;

  if (pSeqno.length()>0) 
     whereStr  = whereStr + "and p_seqno = '"+ pSeqno +"' "; 

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void deleteActAcagCurrDebug() throws Exception
 {
  daoTable  = "act_acag_curr a";
  whereStr  = "WHERE  p_seqno in (select p_seqno "
            + "                   from cyc_acmm_" + getValue("wday.stmt_cycle")+" ) "
            ;
             
  if (pSeqno.length()>0) 
     whereStr  = whereStr + "and p_seqno = '"+ pSeqno +"' "; 

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void loadActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "a.p_seqno,"
            + "sum(a.dc_end_bal) as dc_end_bal";
  daoTable  = "act_debt a,cyc_acmm_"+ getValue("wday.stmt_cycle") + " d";
  whereStr  = "WHERE  a.dc_end_bal > 0 "
            + "AND    a.p_seqno    = d.p_seqno "
            + "AND    a.acct_code in ('AF','LF') "
            + "AND    a.dc_end_bal   > 0 "
            + "AND    d.this_ttl_amt > 0 ";

  if (pSeqno.length()>0) 
     whereStr  = whereStr + "and a.p_seqno = '"+ pSeqno +"' "; 

  whereStr  = whereStr 
            + "GROUP BY a.p_seqno "
            ;

  int  n = loadTable();

  setLoadData("debt.p_seqno");

  showLogMessage("I","","Load act_debt Count: ["+n+"]");
 }
// ************************************************************************
 double commCurrAmt(String currCode, double val, int rnd) throws Exception
 {

  setValue("pcde.curr_code" , currCode);
  int cnt1 = getLoadData("pcde.curr_code");

  val = val * 10000.0;
  val = Math.round(val);

//showLogMessage("I","","STEP 2001 ["+ val + "]");

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal("10000"));

//showLogMessage("I","","STEP 2002 ["+ curr_amt + "]");

  if (cnt1==0) return(currAmt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

//showLogMessage("I","","STEP 2003 ["+ retNum + "]");

  return(retNum);
 }
// ************************************************************************


}  // End of class FetchSample
