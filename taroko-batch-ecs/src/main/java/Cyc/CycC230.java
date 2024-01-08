/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/08/02  V1.00.06  Allen Ho   cyc_c230                                   *
* 111/11/10  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class CycC230 extends AccessDAO
{
 private final String PROGNAME = "雙幣基金-定期轉歷史檔處理程式 111/11/10  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String tohistDate = "";
 String delayDate = "";

 long    totalCnt=0;
 long    deleteCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycC230 proc = new CycC230();
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
      { businessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
   tohistDate =comm.lastMonth(businessDate,13) + businessDate.substring(6,8);
   delayDate =comm.lastMonth(businessDate,3)  + businessDate.substring(6,8);
   showLogMessage("I","","=========================================");
   showLogMessage("I","","餘額為零最早移除日期["+ tohistDate +"] 異動日[" + delayDate +"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","轉入歷史檔");
   selectCycDcFundDtl();
   showLogMessage("I","","=========================================");

   showLogMessage("I","","處理 ["+totalCnt+"] 筆");

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
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 void selectCycDcFundDtl() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "fund_code ";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  end_tran_amt = 0 "
            + "and    tran_date < ? "
            + "group by p_seqno,curr_code,fund_code "
            ;

  setString(1 , tohistDate);

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    selectCycDcFundDtl1();

    processDisplay(10000); // every 10000 display message

    if (totalCnt%50000==0)
       {
        showLogMessage("I","","    proc p_seqno,fund_code:  "+totalCnt);
        countCommit();
       }
    if (deleteCnt>500000) 
       {
        showLogMessage("I","","    delete records > 3000000 , process will closed");
        break;
       }
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void selectCycDcFundDtl1() throws Exception
 {
  selectSQL = "end_tran_amt,"
            + "rowid as rowid";
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "where  tran_date   < ? "
            + "and    to_char(mod_time,'yyyymmdd') < ? "
            + "and    p_seqno     = ? "
            + "and    curr_code   = ? "
            + "and    fund_code   = ? "
            + "order by tran_date,tran_time"
            ;

  setString(1 , tohistDate);
  setString(2 , delayDate);
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("curr_code"));
  setString(5 , getValue("fund_code"));

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
    { 
     if (getValueDouble("end_tran_amt",inti)!=0) break;

     insertCycDcFundDtlHst(inti);
     deleteCycDcFundDtl(inti);
     deleteCnt++;
     if (deleteCnt%200000==0)
        {
         showLogMessage("I","","    Move history  Records :  "+deleteCnt);
         countCommit();
        }
    } 
 }
// ************************************************************************
 void insertCycDcFundDtlHst(int inti) throws Exception
 {
  sqlCmd    = "insert into cyc_dc_fund_dtl_hst "
            + "select * from cyc_dc_fund_dtl "
            + "WHERE rowid = ? ";

  daoTable  = "cyc_dc_fund_dtl_hst";

  setRowId(1,getValue("rowid",inti));

  insertTable();

  return;
 }
// ************************************************************************
 void deleteCycDcFundDtl(int inti) throws Exception
 {
  daoTable  = "cyc_dc_fund_dtl";
  whereStr  = "WHERE rowid  = ? ";

  setRowId(1,getValue("rowid",inti));

  deleteTable();

  return;
 }
// ************************************************************************

}  // End of class FetchSample
