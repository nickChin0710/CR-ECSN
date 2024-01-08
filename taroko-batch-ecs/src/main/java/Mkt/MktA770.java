/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/08/02  V1.00.07  Allen Ho   new                                        *
 * 111/11/11  V1.00.08  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktA770 extends AccessDAO
{
 private final String PROGNAME = "台幣基金-定期轉歷史檔處理程式 110/08/02 V1.00.07";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String hMbdlPSeqno = "";
 String tohistDate = "";
 String delayDate = "";

 long    totalCnt=0;
 long    deleteCnt=0;
 int end_flag=0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA770 proc = new MktA770();
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

   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   tohistDate =comm.lastMonth(businessDate,13) + businessDate.substring(6,8);
   delayDate =comm.lastMonth(businessDate,3)  + businessDate.substring(6,8);
   showLogMessage("I","","=========================================");
   showLogMessage("I","","餘額為零最早移除日期["+ tohistDate +"] 異動日[" + delayDate +"]");

   selectMktCashbackDtl();

   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 轉歷史 ["+deleteCnt+"]");
   showLogMessage("I","","=========================================");

   finalProcess();
   return 0;
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
 void selectMktCashbackDtl() throws Exception
 {
  selectSQL = "p_seqno,"
          + "fund_code ";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where  end_tran_amt = 0 "
          + "and    tran_date < ? "
          + "group by p_seqno,fund_code "
  ;

  setString(1 , tohistDate);

  openCursor();

  totalCnt=0;
  while( fetchTable() )
  {
   totalCnt++;

   selectMktCashbackDtl1();

   if (totalCnt%50000==0)
   {
    showLogMessage("I","","    proc p_seqno,fund_code:  "+totalCnt);
    countCommit();
   }
   if (deleteCnt>=300000)
   {
    showLogMessage("I","","    delete records >= 300000 , process will closed");
    break;
   }
  }
  closeCursor();
  return;
 }
 // ************************************************************************
 void selectMktCashbackDtl1() throws Exception
 {
  selectSQL = "end_tran_amt,"
          + "rowid as rowid";
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "where tran_date   < ? "
          + "and   to_char(mod_time,'yyyymmdd') < ? "
          + "and   p_seqno     = ? "
          + "and   fund_code   = ? "
          + "order by tran_date,tran_time"
  ;

  setString(1 , tohistDate);
  setString(2 , delayDate);
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("fund_code"));

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
  {
   setValue("rowid",getValue("rowid",inti));

   if (getValueInt("end_tran_amt",inti)!=0) break;
   insertMktCashbackDtlHst(inti);
   deleteMktCashbackDtl(inti);
   deleteCnt++;
   if (deleteCnt%100000==0)
   {
    showLogMessage("I","","    Move history  Records :  "+deleteCnt);
    countCommit();
   }
  }
 }
 // ************************************************************************
 void insertMktCashbackDtlHst(int inti) throws Exception
 {
  sqlCmd    = "insert into mkt_cashback_dtl_hst "
          + "select * from mkt_cashback_dtl "
          + "WHERE rowid = ? ";

  daoTable  = "mkt_cashback_dtl_hst";

  setRowId(1,getValue("rowid",inti));

  insertTable();

  return;
 }
 // ************************************************************************
 void deleteMktCashbackDtl(int inti) throws Exception
 {
  daoTable  = "mkt_cashback_dtl";
  whereStr  = "WHERE rowid  = ? ";

  setRowId(1,getValue("rowid",inti));

  deleteTable();

  return;
 }
// ************************************************************************

}  // End of class FetchSample
