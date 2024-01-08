/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/06  V1.00.05  Allen Ho   cyc_C250                                   *
* 109/11/13  V1.00.06  Zuwei      naming rule                                *
*                                                                            *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycC250 extends AccessDAO
{
 private  String progname = "雙幣卡-外幣基金重整處理程式 109/11/13 V1.00.06";
 CommFunction comm = new CommFunction();
 CommDCFund comr = null;

 String hBusiBusinessDate   = "";
 String batchNo="";
 int    freeCnt=0,freeAmt=0,noteFreeCnt=0,noteCfAmt=0;    
 int    batchTotAmt=0,batchTotCnt=0;  
 int    serialNo=0;

 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycC250 proc = new CycC250();
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
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
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

   comr = new CommDCFund(getDBconnect(),getDBalias());
   //comr.hMcdlModPgm = javaProgram; 
   comr.modPgm = javaProgram;

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理資料");
   selectCycDcFundDtl();

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
 public void  selectCycDcFundDtl() throws Exception
 {
  selectSQL = "p_seqno,"
		    + "fund_code ,"
            + "curr_code ,"
            + "sum(decode(sign(end_tran_amt),-1,end_tran_amt,0)) as m_tran_amt," 
            + "sum(decode(sign(end_tran_amt), 1,end_tran_amt,0)) as p_tran_amt";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  end_tran_amt != 0 "
            + "group by p_seqno ,fund_code , curr_code "
            + "having sum(decode(sign(end_tran_amt),1,end_tran_amt,0))!=0 "
            + "and    sum(decode(sign(end_tran_amt),-1,end_tran_amt,0))!=0 "
            ;

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    selectCycDcFundDtl1();

   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int selectCycDcFundDtl1() throws Exception
 {
  selectSQL = "tran_seqno";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE  p_seqno        = ? "
            + "and    fund_code      = ? "		  
            + "and    curr_code      = ? "
            + "and   end_tran_amt    < 0 "
            + "order by  decode(effect_e_date,'','99999999',effect_e_date),tran_date ";
            ;
  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("fund_code"));  
  setString(3 , getValue("curr_code"));

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
   {
    updateCnt++;
    comr.dcfundFunc(getValue("tran_seqno",inti)); 
   } 
  return(1);
 }
// ************************************************************************

}  // End of class FetchSample

