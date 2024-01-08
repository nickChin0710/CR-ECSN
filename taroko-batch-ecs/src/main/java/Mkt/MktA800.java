/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/10/07  V1.00.06  Allen Ho                                              *
* 111/11/08  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Mkt;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class MktA800 extends AccessDAO
{
 private final String PROGNAME = "紅利-紅利特惠專案(二)退貨明細處理程式 111/11/08  V1.00.02";
 CommFunction comm = new CommFunction();

 String businessDate = "";
 String activeCode = "";
 String cardNo = "";

 int maxMonths = 0; 
 String tranSeqno = "";
 long    totalCnt=0,updateCnt=0;
 int cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA800 proc = new MktA800();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
//   setConsoleMode("N");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code]");
       showLogMessage("I","","PARM 3 : [card_no]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }
   if ( args.length >= 2 )
      { activeCode = args[1]; }
   if ( args.length == 3 )
      { cardNo = args[2]; }
   
   if ( !connectDataBase() ) exitProgram(1);

   selectPtrBusinday();

   if ( args.length >= 1 )
   {
    if (selectPtrWorkday1()!=0)
       {
        showLogMessage("I","","本日非關帳日, 不需執行");
        return(0);
       }
   }
   else if (selectPtrWorkday()!=0)
   {
    showLogMessage("I","","本日非關帳日, 不需執行");
    return(0);
   }
   
   showLogMessage("I","","proc_date ["+comm.nextMonthDate(businessDate,-1 )+"]");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理一般退貨 ");
   loadBilBill();
   selectMktBpnwMlist();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理分期退貨 ");
   selectMktBpnwItlist();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
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
  selectSQL = "";
  daoTable  = "ptr_businday";
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
 int selectMktBpnwMlist() throws Exception
 {
  selectSQL = "card_no,"
            + "id_p_seqno,"
            + "p_seqno,"
            + "active_code,"
            + "sup_flag,"
            + "acct_type,"
            + "acct_month_s,"
            + "acct_month_e,"
            + "rowid as rowid";
  daoTable  = "mkt_bpnw_mlist";
  whereStr  = "where proc_date = ? "
            + "and   proc_flag = 'Y' "
            ;

  setString(1 , comm.nextMonthDate(businessDate,-1 )); 

  openCursor();

  totalCnt=0;

  int refundAmt = 0;
  while( fetchTable() ) 
   {
    setValue("bill.card_no" , getValue("card_no"));
    cnt1 = getLoadData("bill.card_no");
    if (cnt1==0) continue;

    refundAmt = 0;
    for (int inti=0;inti<cnt1;inti++)
       {
        setValue("rend.reference_no"         , getValue("bill.reference_no",inti));
        setValue("rend.bill_type"            , getValue("bill.bill_type",inti));
        setValue("rend.txn_code"             , getValue("bill.txn_code",inti));
        setValue("rend.acq_member_id"        , getValue("bill.acq_member_id",inti));
        setValue("rend.mcht_no"              , getValue("bill.mcht_no",inti));
        setValue("rend.sign_flag"            , getValue("bill.sign_flag",inti));
        setValue("rend.dest_amt"             , getValue("bill.dest_amt",inti));
        setValue("rend.acct_month"           , getValue("bill.acct_month",inti));
        setValue("rend.purchase_date"        , getValue("bill.purchase_date",inti));
        setValue("rend.post_date"            , getValue("bill.post_date",inti));
        setValue("rend.acct_code"            , getValue("bill.acct_code",inti));

        insertMktBpnwRefund();
        refundAmt = refundAmt + getValueInt("bill.dest_amt",inti); 
       }

    totalCnt++;
    if (refundAmt >0) updateMktBpnwMlist(refundAmt);
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktBpnwItlist() throws Exception
 {
  selectSQL = "a.card_no,"
            + "a.id_p_seqno,"
            + "a.p_seqno,"
            + "a.active_code,"
            + "a.sup_flag,"
            + "a.acct_type,"
            + "a.acct_month_s,"
            + "a.acct_month_e,"
            + "c.reference_no,"
            + "c.bill_type,"
            + "c.txn_code,"
            + "c.acq_member_id,"
            + "c.mcht_no,"
            + "c.sign_flag,"
            + "c.dest_amt,"
            + "c.acct_month,"
            + "b.refund_apr_date as purchase_date,"
            + "c.post_date,"
            + "a.rowid as rowid";
  daoTable  = "mkt_bpnw_mlist a,bil_contract b,mkt_bpnw_itlist c";
  whereStr  = "where a.proc_date = ? "
            + "and   a.card_no   = c.card_no "
            + "and   a.proc_flag = 'Y' "
            + "and   a.active_code = c.active_code "
            + "and   b.contract_no =  c.contract_no "
            + "and   b.contract_seq_no =  c.contract_seq_no "
            + "and   b.refund_apr_flag =  'Y' "
            ;

  setString(1 , comm.nextMonthDate(businessDate,-1 )); 

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   {
    setValue("rend.reference_no"         , getValue("reference_no"));
    setValue("rend.bill_type"            , getValue("bill_type"));
    setValue("rend.txn_code"             , getValue("txn_code"));
    setValue("rend.acq_member_id"        , getValue("acq_member_id"));
    setValue("rend.mcht_no"              , getValue("mcht_no"));
    setValue("rend.sign_flag"            , getValue("sign_flag"));
    setValue("rend.dest_amt"             , getValue("dest_amt"));
    setValue("rend.acct_month"           , getValue("acct_month"));
    setValue("rend.purchase_date"        , getValue("purchase_date"));
    setValue("rend.post_date"            , getValue("post_date"));
    setValue("rend.acct_code"            , "IT");

    insertMktBpnwRefund();

    if (getValueInt("dest_amt") >0) updateMktBpnwMlist(getValueInt("dest_amt"));

    totalCnt++;
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int insertMktBpnwRefund() throws Exception
 {
  dateTime();
  extendField = "rend.";
  setValue("rend.check_date"           , businessDate);
  setValue("rend.active_code"          , getValue("active_code")); 
  setValue("rend.card_no"              , getValue("card_no"));
  setValue("rend.sup_flag"             , getValue("sup_flag"));
  setValue("rend.p_seqno"              , getValue("p_seqno"));
  setValue("rend.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("rend.acct_type"            , getValue("acct_type"));
  setValue("rend.acct_month_s"         , getValue("acct_month_s"));
  setValue("rend.acct_month_e"         , getValue("acct_month_e"));
  setValue("rend.mod_time"             , sysDate+sysTime);
  setValue("rend.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bpnw_refund";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktBpnwMlist(int refundAmt) throws Exception
 {
  dateTime();
  String refundFlag="N";
  if (refundAmt>0) refundFlag="Y";

  updateSQL = "refund_date   = ?, "
            + "refund_flag   = ?, "
            + "refund_amt    = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";  
  daoTable  = "mkt_bpnw_mlist";
  whereStr  = "WHERE rowid  = ? ";

  setString(1 , businessDate);
  setString(2 , refundFlag);
  setInt(3    , refundAmt);
  setString(4 , javaProgram);
  setRowId(5 , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktBpnwItlist(String refundFlag) throws Exception
 {
  dateTime();
  updateSQL = "refund_date     = ?, "
            + "refund_flag     = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "mkt_bpnw_itlist";
  whereStr  = "WHERE rowid  = ? ";

  setString(1 , businessDate);
  setString(2 , refundFlag);
  setString(3 , javaProgram);
  setString(4 , sysDate+sysTime);
  setRowId(5 , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void loadBilBill() throws Exception
 {
  extendField = "bill.";
  selectSQL = "card_no,"
            + "reference_no,"
            + "acct_code,"
            + "bill_type,"
            + "txn_code,"
            + "acq_member_id,"
            + "mcht_no,"
            + "sign_flag,"
            + "dest_amt,"
            + "acct_month,"
            + "purchase_date,"
            + "post_date";
  daoTable  = "bil_bill";
  whereStr  = "where post_date between ? and ? "
            + "and   acct_code in ('BL','ID','IT','AO','OT') "
            + "and   sign_flag = '-' "
            + "and   card_no in ( "
            + "      select card_no "
            + "      from   mkt_bpnw_mlist "
            + "      where  proc_flag  = 'Y' "
            + "      AND    proc_date  = ? "
            ;

  setString(1 , comm.nextMonthDate(businessDate,-1 )); 
  setString(2 , businessDate);
  setString(3 , comm.nextMonthDate(businessDate,-1 )); 

  if (activeCode.length()>0)
     {
      whereStr  = whereStr 
                + "and active_code = ? "; 
      setString(4 , activeCode);
      if (cardNo.length()!=0)
         {
          whereStr  = whereStr 
                    + "and card_no = ? "; 
          setString(5 , cardNo);
         }
     }
  whereStr  = whereStr 
            + "    ) " 
            + "order by card_no";

  int  n = loadTable();

  setLoadData("bill.card_no");

  showLogMessage("I","","Load bil_bill Count: ["+n+"]");
 }
 
//************************************************************************
int  selectPtrWorkday1() throws Exception
{
extendField = "wday.";
selectSQL = "";
daoTable  = "ptr_workday";
whereStr  = "where stmt_cycle = ? ";

setString(1,businessDate.substring(6,8));

int recCnt = selectTable();

if ( notFound.equals("Y") ) return(1);

setValue("wday.this_close_date" , businessDate);
setValue("wday.this_acct_month" , businessDate.substring(0,6));
setValue("wday.next_acct_month" , comm.nextMonth(businessDate.substring(0,6) , 1));

return(0);
} 

//************************************************************************
int  selectPtrWorkday() throws Exception
{
extendField = "wday.";
selectSQL = "";
daoTable  = "ptr_workday";
whereStr  = "where this_close_date = ? ";

setString(1,businessDate);

int recCnt = selectTable();

if ( notFound.equals("Y") ) return(1);

return(0);
}
// ************************************************************************

}  // End of class FetchSample
