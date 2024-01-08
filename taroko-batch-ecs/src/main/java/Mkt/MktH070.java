/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/06  V1.00.00  Allen Ho                                              *
* 109-12-09  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktH070 extends AccessDAO
{
 private  String progname = "基金-台幣基金重整處理程式  109/12/09 V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comc = null;

 String hBusiBusinessDate  = "";

 long    totalCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktH070 proc = new MktH070();
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

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程序啟動中, 不執行..");
       return(0);
      }

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() ) return(1);

   comc = new CommCashback(getDBconnect(),getDBalias());
   //comc.hMcdlModPgm = javaProgram; 
   comc.modPgm = javaProgram;

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理資料");
   selectMktCashbackDtl();

   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 更新 ["+updateCnt+"] 筆");
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
 void  selectMktCashbackDtl() throws Exception
 {
  selectSQL = "sum(decode(sign(end_tran_amt),-1,end_tran_amt,0)) as m_tran_amt," 
            + "sum(decode(sign(end_tran_amt), 1,end_tran_amt,0)) as p_tran_amt,"
            + "fund_code,"
            + "p_seqno"; 
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  end_tran_amt != 0 "
            + "group by p_seqno,fund_code "
            + "having sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 "
            + "and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0"
            ;

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    selectMktCashbackDtl1();

    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void  selectMktCashbackDtl1() throws Exception
 {
  selectSQL = "tran_seqno";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE fund_code = ? "
            + "and   p_seqno   = ? "  
            + "and   end_tran_amt < 0 "
            + "order by  decode(effect_e_date,'','99999999',effect_e_date),tran_date ";

  setString(1 , getValue("fund_code"));
  setString(2 , getValue("p_seqno"));

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
    {
     updateCnt = updateCnt + comc.cashbackFunc(getValue("tran_seqno",inti));
    }
  return;
 }
// ************************************************************************ 


}  // End of class FetchSample
