/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/12/28  V1.00.00  Allen Ho   new initial                                *
* 109-12-22  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycB900 extends AccessDAO
{
 private  String progname = "每日產生帳戶逾期月份處理程式  109/12/22  V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String   hBusiBusinessDate  = "";

 int     checkCnt =0, updateCnt=0;
 long    totalCnt=0;
 int cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycB900 proc = new CycB900();
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
  
   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() )
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
  
   selectPtrWorkday();

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
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
 public int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "PTR_WORKDAY";
  whereStr  = "order by stmt_cycle";

  int recCnt = selectTable();

   for (int inti=0;inti<recCnt;inti++)
     {
      showLogMessage("I","","處理 CYCLE : ["+ getValue("wday.stmt_cycle",inti) +"]");
      loadActAcag(inti);
      selectActAcno(inti);
      showLogMessage("I","","     讀取筆數 : ["+ totalCnt +"]   處理筆數 ["+updateCnt+"]");
      commitDataBase();
     }

  return(0);
 }
// ************************************************************************
public void  selectActAcno(int intk) throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.acct_type,"
            + "a.int_rate_mcode,"
            + "a.rowid as rowid,"
            + "b.mix_mp_balance";
  daoTable  = "act_acno a,ptr_actgeneral_n b";
  whereStr  = "where a.acct_type = b.acct_type "
            + "and   a.stmt_cycle = ? "
            ;

  setString(1 , getValue("wday.stmt_cycle",intk));

  openCursor();

  int mcode=0;
  totalCnt=0;  
  updateCnt=0;
  while( fetchTable() )
   {
    totalCnt++;

    setValue("acag.p_seqno" , getValue("p_seqno"));
    int cnt1 = getLoadData("acag.p_seqno");
    if (cnt1==0)
       {
        if (getValueInt("int_rate_mcode")==0) continue;
        updateActAcno(0);
        updateCnt++;
        continue;
       }

    double minAmount = 0;
    mcode =0;
    for (int inti=0; inti<cnt1; inti++ )
      {
       minAmount = minAmount + getValueDouble("acag.pay_amt",inti);
       if (minAmount > getValueInt("mix_mp_balance"))
          {
           mcode = (int)comm.monthBetween(getValue("acag.acct_month",inti),
                                          getValue("wday.this_acct_month",intk));
           break;
          }
      }

    if (totalCnt % 100000 == 0)
       showLogMessage("I","","  Proc Records :  "+totalCnt+ " Update Records : "+updateCnt);

    if (getValueInt("int_rate_mcode")==mcode) continue;
         
    updateCnt++;
    updateActAcno(mcode);
   }
  closeCursor();
 }
// ************************************************************************
 public void updateActAcno(int mcode) throws Exception
 {
  updateSQL = "int_rate_mcode  = ? ";
  daoTable  = "act_acno";
  whereStr  = "where rowid = ? "
            ; 

  setInt(  1 , mcode);
  setRowId(2 , getValue("rowid"));

  int recCnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","update_act_acno error!" );
      showLogMessage("I","","rowid=["+getValue("rowid")+"]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 public void  loadActAcag(int intk) throws Exception
 {
  extendField = "acag.";
  selectSQL = "p_seqno, "
            + "acct_month,"
            + "sum(pay_amt) as pay_amt ";
  daoTable  = "act_acag";
  whereStr  = "where stmt_cycle = ? "
            + "and   acct_month != ? "
            + "group by p_seqno,acct_month "
            + "order by p_seqno,acct_month "
            ;

  setString(1 , getValue("wday.stmt_cycle",intk));
  setString(2 , getValue("wday.this_acct_month",intk));

  int  n = loadTable();
  setLoadData("acag.p_seqno");

  showLogMessage("I","","Load act_acag Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

