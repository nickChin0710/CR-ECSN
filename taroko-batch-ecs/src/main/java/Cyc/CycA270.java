/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/20  V1.00.10  Allen Ho   mkt_D063                                   *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA270 extends AccessDAO
{
 private final String PROGNAME = "紅利-紅利特惠活動(五)紅利贈送處理程式 111-11-08  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;
  CommBonus comb = null;

 String businessDate   = "";
 String activeCode     = "";
 String tranSeqno = "";
 long    totalCnt=0,updateCnt=0;
 String feedbackType = "";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA270 proc = new CycA270();
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
 	   showLogMessage("I","","PARM 1 : [feedbackType]");
       showLogMessage("I","","PARM 2 : [business_date]");
       showLogMessage("I","","PARM 3 : [active_code]");
       return(1);
      }

	if (args.length == 0 || (!args[0].equals("1") &&
			!args[0].equals("2"))) {
		showLogMessage("I","","請傳入回饋方式 : 1.帳單週期 2.每月 ");
		return(1);
	}  

	feedbackType = args[0];
   
   if (args.length >= 2 )
      { businessDate = args[1]; }

   if (args.length == 3 )
      { activeCode  = args[2]; }
   
   if ( !connectDataBase() ) return(1);

   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   int cycleFlag = selectPtrWorkday();
   
   if ((feedbackType.equals("1")) && !(cycleFlag == 0))   {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
   }
   if ((feedbackType.equals("2")) &&  (cycleFlag == 0) )   {
       showLogMessage("I","","回饋方式 : 1.每月指定日 ,本日是關帳日,不需執行 ");
       return(0);
   }  
   
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理(mkt_bpmh3_bonus)資料");
   selectMktBpmh3Bonus();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrBusinday() throws Exception
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
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
  int selectMktBpmh3Bonus() throws Exception
 {
  selectSQL = "a.active_code,"
            + "b.active_name,"
            + "b.effect_months,"
            + "b.bonus_type,"
            + "a.acct_type,"
            + "b.tax_flag,"
            + "a.p_seqno,"
            + "a.id_p_seqno,"
            + "a.tran_code,"
            + "a.total_point as beg_tran_bp,"
            + "a.rowid as rowid";
  daoTable  = "mkt_bpmh3_bonus a,mkt_bpmh3 b";
  whereStr  = "where a.feedback_date = ? "
            + "and   a.proc_flag    != 'Y' "
            + "and   a.active_code = b.active_code "
            ;
  whereStr += " and b.run_time_type = ?";
  int i = 1;
  setString(i++ , businessDate);
  setString(i++ , feedbackType);
   
  if("2".equals(feedbackType)) {
	  CommString coms = new CommString();
	  whereStr += " and b.run_time_dd = ? ";
	  setInt(i++ ,coms.ss2int(coms.right(businessDate, 2)));
  }
  
  if (activeCode.length()>0)
     { 
      whereStr  = whereStr 
                + "and a.active_code = ? ";
      setString(i++ , activeCode);
     }            
    
  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    insertMktBonusDtl();
    updateMktBpmh3Bonus();
//  comb.Bonus_func(tran_seqno);
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int insertMktBonusDtl() throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");

  setValue("tran_date"            , sysDate);
  setValue("tran_time"            , sysTime);
  setValue("tran_seqno"           , tranSeqno);
  setValue("active_code"          , getValue("active_code"));
  setValue("active_name"          , getValue("active_name"));
  setValue("effect_e_date"        , "");
  if (getValueInt("effect_months")>0)
     setValue("effect_e_date"        , comm.nextMonthDate(businessDate,getValueInt("effect_months")));
  setValue("tax_flag"             , getValue("tax_flag"));
  setValue("mod_desc"             , "紅利特惠活動(五)");
  setValueInt("end_tran_bp"       , getValueInt("beg_tran_bp"));
  setValue("tran_code"            , getValue("tran_code"));
  setValue("bonus_type"           , getValue("bonus_type"));
  setValue("acct_date"            , businessDate);
  setValue("proc_month"           , businessDate.substring(0,6));
  setValue("acct_type"            , getValue("acct_type"));
  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("id_p_seqno"           , getValue("id_p_seqno"));
  setValue("tran_pgm"             , javaProgram);
  setValue("apr_flag"             , "Y");
  setValue("apr_user"             , javaProgram);
  setValue("apr_date"             , sysDate);
  setValue("crt_user"             , javaProgram);
  setValue("crt_date"             , sysDate);
  setValue("mod_user"             , javaProgram);
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);

  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktBpmh3Bonus() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag = 'Y'";
  daoTable  = "mkt_bpmh3_bonus";
  whereStr  = "where  rowid   = ? ";

  setRowId(1  , getValue("rowid"));

  updateTable();
  return;
 }
 
//************************************************************************
int selectPtrWorkday() throws Exception
{
extendField = "wday.";
selectSQL = "this_acct_month,"
         + "stmt_cycle";
daoTable  = "ptr_workday";
whereStr  = "WHERE this_close_date = ? ";

setString(1, businessDate);

selectTable();

if ( notFound.equals("Y") ) return(1);

return(0);
}
// ************************************************************************

}  // End of class FetchSample

