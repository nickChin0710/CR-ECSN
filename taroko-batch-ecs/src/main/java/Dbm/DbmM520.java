/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/06/25  V1.00.07  Allen Ho   dbm_m230                                   *
* 111/11/08  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM520 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-紅利積點移轉統計處理程式 111/11/08  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommDBonus comb = null;

 String businessDate = "";
 String hDbdlTranSeqno = "";
 String hDmbpDataKey = "";

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM520 proc = new DbmM520();
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
   comb = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();
   showLogMessage("I","","=========================================");
   if (!businessDate.substring(6,8).equals("02"))
      {
       showLogMessage("I","","本程式只在每月2日換日後執行,本日為"+ businessDate +"日..");
       showLogMessage("I","","=========================================");
       return(0);
      } 

   deleteMktBpStat4();
   selectDbmBonusDtl();

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
 void selectDbmBonusDtl() throws Exception
 {
  selectSQL = "a.acct_type, "
            + "b.group_code, "
            + "decode(a.id_p_seqno,a.mv_id_p_seqno,'1','2') as trans_type, "
            + "count(*) as trans_out_cnt,"                      
            + "sum(0-a.beg_tran_bp) as trans_out_bp,"
            + "sum(a.fee_tran_bp) as trans_out_bp "
            ;
  daoTable  = "dbm_bonus_dtl a,dbc_card b";
  whereStr  = "where a.tran_date like ?||'%' "
            + "and   a.card_no    = b.card_no "
            + "and   a.tran_code = '0' "
            + "and   a.beg_tran_bp < 0 "
            + "group by a.acct_type, "
            + "         b.group_code, "
            + "         decode(a.id_p_seqno,a.mv_id_p_seqno,'1','2') "
            + "";

  setString(1,comm.lastMonth(businessDate));              

  openCursor();

  while( fetchTable() ) 
   { 
    insertMktBpStat4();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int insertMktBpStat4() throws Exception
 {
  setValue("stat_month"           , comm.lastMonth(businessDate));
  setValue("acct_type"            , getValue("acct_type")); 
  setValue("group_code"           , getValue("group_code"));
  setValue("trans_type"           , getValue("trans_type"));
  setValueInt("trans_out_cnt"     , getValueInt("trans_out_cnt"));
  setValueInt("trans_out_bp"      , getValueInt("trans_out_cnt"));
  setValueInt("trans_out_fee"     , getValueInt("trans_out_fee"));
  setValue("crt_date"             , businessDate);
  setValue("crt_user"             , javaProgram);
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_bp_stat4";

  insertTable();

  return(0);
 }
// ************************************************************************
 int deleteMktBpStat4() throws Exception
 {
  daoTable  = "mkt_bp_stat4";
  whereStr  = "WHERE stat_month = ? "
            + "and   acct_type in (select acct_type "
            + "                    from dbp_acct_type) ";

  setString(1 ,comm.lastMonth(businessDate)); 

  deleteTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
