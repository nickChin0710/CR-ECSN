/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 107/12/06  V1.00.00  Allen Ho   cyc_B610                                   *
 * 109-12-22  V1.00.01  tanwei     updated for project coding standard        *
 * 112-04-18  V1.00.02  Simon      updated for TCB project                    *
 * 112-08-18  V1.00.03  Simon      1.剔除crd_card.current_code='1'者          *
 *                                 2.剔除crd_card.combo_indicator='Y'者       *
 ******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycB610 extends AccessDAO
{
 private  String progname = "關帳-產生對帳單停用掛失資料處理程式  112/08/18  V1.00.03";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String hWdayThisCloseDate = "";
 String hWdayNextCloseDate = "";

 long    totalCnt=0;
 int debug =1;

 int paymentAmt = 0,insertCnt=0,updateCnt=0;
 int cnt1=0;
 int loadCnt3 = 0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycB610 proc = new CycB610();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
 // ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+progname);

   if (comm.isAppActive(javaProgram))
   {
    showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
    return(0);
   }

   if (args.length > 2)
   {
    showLogMessage("I","","請輸入參數:");
    showLogMessage("I","","PARM 1 : [business_date]");
    return(1);
   }

   if (args.length >= 1 )
   {
    hBusiBusinessDate = args[0];
   }

   if ( !connectDataBase() )
    return(1);
   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
   {
    showLogMessage("I","","本日非關帳日, 不需執行");
    return(0);
   }

   loadCrdCard();
   loadActAcno();
   loadActAcctHst();
   loadCycLostfeeAcct();
 //loadCycLostfeeGrp();
   loadCycLostfee();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理參數資料...");
   selectCrdCard();
   showLogMessage("I","","=========================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
 // ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
  {
   showLogMessage("I","","select ptr_businday error!" );
   exitProgram(1);
  }

  if (hBusiBusinessDate.length()==0)
   hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
 // ************************************************************************
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where next_close_date = ? ";

  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle      =  getValue("wday.STMT_CYCLE");
  hWdayThisCloseDate =  getValue("wday.this_close_date");
  hWdayNextCloseDate =  getValue("wday.next_close_date");

  return(0);
 }
 // ************************************************************************
 public void  selectCrdCard() throws Exception
 {
  int[]  calCnt = {0,0,0,0,0,0,0,0,0,0,0};
  selectSQL = "a.p_seqno,"
          + "a.stmt_cycle,"
          + "a.acct_type,"
          + "a.oppost_reason,"
          + "a.oppost_date,"
          + "a.card_no,"
          + "a.card_note,"
          + "a.group_code,"
          + "a.sup_flag,"
          + "b.salary_code,"
          + "b.staff_flag,"
          + "b.credit_flag";
  daoTable  = "crd_card a,crd_idno b";
  whereStr  = "where a.stmt_cycle     = ? "
          + "and   a.current_code in ('2') "
          + "and   a.id_p_seqno    = b.id_p_seqno "
          + "and   a.combo_indicator != 'Y' "
          + "and   a.lost_fee_code != 'Y' "
          + "and   a.oppost_date  >= ? "
          + "and   a.oppost_date  <  ? "
          + "ORDER BY  a.p_seqno,a.oppost_reason,a.oppost_date ";
  ;

  setString(1 , hWdayStmtCycle);
  setString(2 , hWdayThisCloseDate);
  setString(3 , hWdayNextCloseDate);

  openCursor();

  totalCnt=0;
  String pSeqno = "";
  String matchFlag = "N";
  int lostCnt = 0;
  int loadCnt2 = 0;
  int loadCnt3 = 0;


  while( fetchTable() )
  {
   totalCnt++;

   if (getValue("oppost_date").compareTo(getValue("wday.this_close_date"))<0) continue;
   if (getValue("oppost_date").equals(getValue("wday.next_close_date"))) continue;

   setValue("lost.acct_type",getValue("acct_type"));
   setValue("lost.lost_code",getValue("oppost_reason"));
   cnt1 = getLoadData("lost.acct_type,lost.lost_code");
   if (cnt1<=0) continue;

   if ((getValue("lost.onus_bank").equals("Y"))&&
           (getValue("staff_flag").equals("Y"))) continue;

   if ((getValue("lost.salary_acct").equals("Y"))&&
           (getValue("salary_code").equals("Y"))) continue;

   if ((getValue("lost.credit_acct").equals("Y"))&&
           (getValue("credit_flag").equals("Y"))) continue;

   setValue("acno.p_seqno",getValue("p_seqno"));
   cnt1 = getLoadData("acno.p_seqno");

   if ((getValue("lost.onus_auto_pay").equals("Y"))&&
           (getValue("acno.autopay_acct_bank").equals("006"))) continue;

   if ((getValue("lost.other_auto_pay").equals("Y"))&&
           (!getValue("acno.autopay_acct_bank").equals("006"))&&
           (getValue("acno.autopay_acct_bank").length()>=3)) continue;

   if ((getValue("lost.credit_limit").equals("Y"))&&
           (getValueDouble("acno.line_of_credit_amt")>=
                 //getValueDouble("lost.credit_amt")*10000)) continue;
                   getValueDouble("lost.credit_amt"))) continue;

   setValue("acht.p_seqno",getValue("p_seqno"));
   loadCnt3 = getLoadData("acht.p_seqno");

   if (getValue("lost.bonus_sel").equals("Y"))
   {
    if (loadCnt3 >0)
     if (getValueDouble("acht.stmt_new_add_bp")>=
             getValueDouble("lost.bonus")) continue;
   }
   calCnt[7]++;

   lostCnt++;

   setValue("card.p_seqno"        , getValue("acct_type"));
   setValue("card.oppost_reason"  , getValue("oppost_reason"));
   cnt1 = getLoadData("card.p_seqno,card.oppost_reason");
   if ((cnt1>0)&&(getValueInt("card.lost_cnt")<=getValueInt("lost_limit"))) continue;
   calCnt[8]++;
/*
  showLogMessage("I","","acct_type    : ["+getValue("acct_type")+"]");
  showLogMessage("I","","oppost_reason: ["+getValue("oppost_reason")+"]");
  showLogMessage("I","","card_note    : ["+getValue("card_note")+"]");
  showLogMessage("I","","group_code   : ["+getValue("group_code")+"]");
  showLogMessage("I","","sup_flag     : ["+getValue("sup_flag")+"]");
*/
/**
   setValue("lgrp.acct_type"  , getValue("acct_type"));
   setValue("lgrp.lost_code"  , getValue("oppost_reason"));
   setValue("lgrp.card_note"  , getValue("card_note"));
   setValue("lgrp.group_code" , getValue("group_code"));
   setValue("lgrp.sup_flag"   , getValue("sup_flag"));
   cnt1 = getLoadData("lgrp.acct_type,lgrp.lost_code,lgrp.card_note,lgrp.group_code,lgrp.sup_flag");

   if (cnt1 > 0) setValueDouble("dest_amt" , getValueDouble("lgrp.lostfee_amt"));
**/
   cnt1 = 0;
   if (cnt1 <= 0)
   {
    setValue("lact.acct_type"  , getValue("acct_type"));
    setValue("lact.lost_code"  , getValue("oppost_reason"));
    setValue("lact.card_note"  , getValue("card_note"));
    setValue("lact.sup_flag"   , getValue("sup_flag"));
    cnt1= getLoadData("lact.acct_type,lact.lost_code,lact.card_note,lact.sup_flag");
    if (cnt1 > 0) setValueDouble("dest_amt" , getValueDouble("lact.lostfee_amt"));
   }
   if (cnt1 > 0) insertBilSysexp();
  }
  closeCursor();
/*
  for (int inti=0;inti<10;inti++)
    showLogMessage("I","","calCnt : ["+inti+"] = ["+calCnt[inti]+"]");
*/
  return;
 }
 // ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "p_seqno,"
          + "oppost_reason,"
          + "count(*) as lost_cnt";
  daoTable  = "crd_card";
  whereStr  = "where stmt_cycle = ? "
          + "and   current_code in ('1','2') "
          + "and   lost_fee_code!= 'Y' "
          + "and   oppost_date  >= ? "
          + "and   oppost_date  <  ? "
          + "group by p_seqno,oppost_reason "
  ;

  setString(1 , hWdayStmtCycle);
//setString(2 , hBusiBusinessDate.substring(0,4)+"0101");
//setString(3 , getValue("oppost_date"));
  setString(2 , hWdayThisCloseDate);
  setString(3 , hWdayNextCloseDate);

  int  n = loadTable();
  setLoadData("card.p_seqno,card.oppost_reason");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
 // ************************************************************************
 void  loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno,"
          + "line_of_credit_amt,"
          + "autopay_acct_bank";
  daoTable  = "act_acno a";
  whereStr  = "where stmt_cycle = ? "
  ;

  setString(1 , hWdayStmtCycle);

  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load ACT_ACNO Count: ["+n+"]");
 }
 // ************************************************************************
 void  loadActAcctHst() throws Exception
 {
  extendField = "acht.";
  selectSQL = "p_seqno,"
          + "sum(stmt_new_add_bp) as stmt_new_add_bp";
  daoTable  = "act_acct_hst";
  whereStr  = "where stmt_cycle = ? "
          + "and   acct_month like ? "
          + "group by p_seqno"
  ;

  setString(1 , hWdayStmtCycle);
  setString(2 , hBusiBusinessDate.substring(0,4)+"%");

  int  n = loadTable();
  setLoadData("acht.p_seqno");

  showLogMessage("I","","Load act_acct_hst Count: ["+n+"]");
 }
 // ************************************************************************
/***
 void  loadCycLostfeeGrp() throws Exception
 {
  extendField = "lgrp.";
  selectSQL = "acct_type,"
          + "lost_code,"
          + "card_note,"
          + "group_code,"
          + "sup_flag,"
          + "lostfee_amt,"
          + "description";
  daoTable  = "cyc_lostfee_grp";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("lgrp.acct_type,lgrp.lost_code,lgrp.card_note,lgrp.group_code,lgrp.sup_flag");

  showLogMessage("I","","Load cyc_lostfee_grp Count: ["+n+"]");
 }
***/

 // ************************************************************************
 void  loadCycLostfeeAcct() throws Exception
 {
  extendField = "lact.";
  selectSQL = "acct_type,"
          + "lost_code,"
          + "card_note,"
          + "sup_flag,"
          + "lostfee_amt,"
          + "description";
  daoTable  = "cyc_lostfee_acct";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("lact.acct_type,lact.lost_code,lact.card_note,lact.sup_flag");

  showLogMessage("I","","Load cyc_lostfee_acct Count: ["+n+"]");
 }
 // ************************************************************************
 void  loadCycLostfee() throws Exception
 {
  extendField = "lost.";
  selectSQL = "acct_type,"
          + "lost_code,"
          + "onus_bank,"
          + "salary_acct,"
          + "credit_acct,"
          + "onus_auto_pay,"
          + "credit_amt,"
          + "credit_limit,"
          + "lost_limit,"
          + "bonus_sel,"
          + "bonus";
  daoTable  = "cyc_lostfee";
  daoTable  = "cyc_lostfee";
  whereStr  = "";

  int  n = loadTable();

  setLoadData("lost.acct_type,lost.lost_code");

  showLogMessage("I","","Load cyc_lostfee Count: ["+n+"]");
 }
 // ************************************************************************
 int insertBilSysexp() throws Exception
 {
  dateTime();
  setValue("card_no"            , getValue("card_no"));
  setValue("bill_type"          , "OSSG");
  setValue("txn_code"           , "LF");
  setValue("purchase_date"      , hBusiBusinessDate);
  setValue("src_type"           , getValue("oppost_reason"));
  setValue("acct_type"          , getValue("acct_type"));
  setValueDouble("dest_amt"     , getValueDouble("dest_amt"));
  setValue("dest_curr"          , "901");
  setValueDouble("src_amt"      , 0);
  setValue("bill_desc"          , "");
  setValue("post_flag"          , "U");
  setValue("mod_user"           , javaProgram);
  setValue("mod_time"           , sysDate+sysTime);
  setValue("mod_pgm"            , javaProgram);

  daoTable  = "bil_sysexp";

  insertTable();

  return(0);
 }
// ************************************************************************


}  // End of class FetchSample

