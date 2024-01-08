/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/01/02  V1.00.00  Allen Ho   cyc_a100  PROD compare OK                  *
* 109/11/27  V1.00.01  Zuwei      naming rule update                         *
* 109-12-17   V1.00.02  tanwei      updated for project coding standard      *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycA010 extends AccessDAO
{
 private final String progname = "關帳-產生對帳單前逾期月份處理程式 109/12/17 V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String   hBusiBusinessDate  = "";

 int     checkCnt =0, updateCnt=0;
 long    totalCnt=0;
 int cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA010 proc = new CycA010();
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
   showLogMessage("I","",javaProgram+" "+progname);
  
   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() )
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
  
   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I；","","====================================");
   showLogMessage("I","","載入暫存資料");
   loadActAcag();
   loadActAcag1();
   showLogMessage("I","","====================================");
   showLogMessage("I","","處理 CYCLE : ["+ getValue("wday.stmt_cycle") +"]");
   selectActAcno();
   showLogMessage("I","","     讀取筆數 : ["+ totalCnt +"]   處理筆數 ["+updateCnt+"]");
   showLogMessage("I","","====================================");

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
  whereStr  = "WHERE next_CLOSE_DATE = ? ";
  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
public void  selectActAcno() throws Exception
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

  setString(1 , getValue("wday.stmt_cycle"));

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
                                          getValue("wday.this_acct_month"));
           break;
          }
      }
    if (cnt1!=0)  // all rrue because this program always in cycle run
       {
        setValue("aca1.p_seqno" , getValue("p_seqno"));
        int cnt2 = getLoadData("aca1.p_seqno");
        if (getValueDouble("aca1.pay_amt") > getValueDouble("mix_mp_balance"))
           mcode = mcode + 1;   // cycle 時 ptr_workday will change ,pmyj+1
       }


    if (totalCnt % 100000 == 0)
       showLogMessage("I","","  Proc Records :  "+totalCnt+ " Update Records : "+updateCnt);

    if (getValueInt("int_rate_mcode")==mcode) continue;
    if (mcode>99) mcode=99;
         
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
 void  loadActAcag() throws Exception
 {
  extendField = "acag.";
  selectSQL = "p_seqno, "
            + "acct_month,"
            + "sum(pay_amt) as pay_amt ";
  daoTable  = "act_acag";
  whereStr  = "where stmt_cycle = ? "
            + "group by p_seqno,acct_month "
            + "order by p_seqno,acct_month "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  int  n = loadTable();
  setLoadData("acag.p_seqno");

  showLogMessage("I","","Load act_acag Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActAcag1() throws Exception
 {
  extendField = "aca1.";
  selectSQL = "p_seqno, "
            + "sum(pay_amt) as pay_amt ";
  daoTable  = "act_acag";
  whereStr  = "where stmt_cycle = ? "
            + "group by p_seqno "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  int  n = loadTable();
  setLoadData("aca1.p_seqno");

  showLogMessage("I","","Load act_acag_1 Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

