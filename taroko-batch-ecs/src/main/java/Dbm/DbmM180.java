/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/03/26  V1.00.08  Allen Ho   dbm_m240                                   *
* 111/11/07  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM180 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-紅利特惠活動(五)加贈點數處理程式 111/11/07  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";

 long    totalCnt=0;
 int     parmCnt =0,cnt1=0;
 String feedbackType = "";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM180 proc = new DbmM180();
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

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
 	   showLogMessage("I","","PARM 1 : [feedbackType]");
       showLogMessage("I","","PARM 2 : [business_date]");
       return(1);
      }

	if (args.length == 0 || (!args[0].equals("1") &&
			!args[0].equals("2"))) {
		showLogMessage("I","","請傳入回饋方式 : 1.帳單週期 2.每月 ");
		return(1);
	}  

	feedbackType = args[0];
   
   if ( args.length == 2 )
      { businessDate = args[1]; }
   
   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

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
   
   selectMktBpmh3VdBonus();

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
 void selectMktBpmh3VdBonus() throws Exception
 {
  selectSQL = "a.active_code,"
            + "b.active_name,"
            + "b.tax_flag,"
            + "a.acct_type,"
            + "a.id_p_seqno,"
            + "a.total_point,"
            + "decode(a.tran_code,'','2',a.tran_code) as tran_code, "
            + "b.effect_months, "
            + "a.rowid as rowid ";
  daoTable  = "mkt_bpmh3_vd_bonus a,mkt_bpmh3 b";
  whereStr  = "where a.feedback_date <= ? "
            + "and   a.active_code   = b.active_code "
            + "and   a.proc_flag     = 'N' "
            + "and   a.total_point   != 0 "
            ;
  whereStr += " and b.run_time_type = ?";
  int i = 1;
  setString(i++, businessDate);
  setString(i++ , feedbackType);

  if("2".equals(feedbackType)) {
	  CommString coms = new CommString();
	  whereStr += " and b.run_time_dd = ? ";
	  setInt(i++ ,coms.ss2int(coms.right(businessDate, 2)));
  }
  
  openCursor();

  while( fetchTable() ) 
   { 
    setValue("bdtl.active_code" , getValue("active_code"));
    setValue("bdtl.active_name" , getValue("active_name"));
    setValue("bdtl.tran_code"   , getValue("tran_code"));
    setValue("bdtl.tax_flag"    , getValue("tax_flag")); 

    if (getValueInt("total_point") > 0)
       {
        setValue("bdtl.mod_memo"  , "紅利特惠活動(五)消費贈送");
       }
    else
       {
        setValue("bdtl.mod_memo"  , "紅利特惠活動(五)消費退貨調整");
       }

    int deductBp = getValueInt("total_point");

    if (deductBp!=0) 
       {
        setValueInt("bdtl.beg_tran_bp"  , deductBp);
        setValueInt("bdtl.end_tran_bp"  , deductBp);
        insertDbmBonusDtl();
       }
    updateMktBpmh3VdBonus();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int updateMktBpmh3VdBonus() throws Exception
 {
  updateSQL = "proc_flag = 'Y',"
            + "proc_date = ? ";
  daoTable  = "mkt_bpmh3_vd_bonus";
  whereStr  = "WHERE rowid = ? ";

  setString(1, businessDate);
  setRowId(2,getValue("rowid"));

  int cnt = updateTable();

  return(0);
 }
// ************************************************************************
 int insertDbmBonusDtl() throws Exception
 {
  setValue("bdtl.tran_seqno"     , comr.getSeqno("ECS_DBMSEQ"));

  dateTime();
  extendField = "bdtl.";
  setValue("bdtl.tran_date"            , sysDate);
  setValue("bdtl.tran_time"            , sysTime);
  setValue("bdtl.crt_date"             , sysDate);
  setValue("bdtl.crt_user"             , javaProgram);
  setValue("bdtl.apr_date"             , sysDate);
  setValue("bdtl.apr_user"             , javaProgram);
  setValue("bdtl.apr_flag"             , "Y");
  setValue("bdtl.effect_e_date"        , "");
  setValue("bdtl.effect_flag"          , "");
  if (getValueInt("bdtl.end_tran_bp")>0)
     {
      setValue("bdtl.effect_e_date"    , comm.nextMonthDate(businessDate,getValueInt("effect_months")));
      setValue("bdtl.effect_flag"      , "1");
     }

  setValue("bdtl.acct_month"           , businessDate.substring(0,6));
  setValue("bdtl.acct_date"            , businessDate);
  setValue("bdtl.bonus_type"           , "BONU");
  setValue("bdtl.acct_type"            , getValue("acct_type"));
  setValue("bdtl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("bdtl.tran_pgm"             , javaProgram);
  setValue("bdtl.mod_time"             , sysDate+sysTime);
  setValue("bdtl.mod_pgm"              , javaProgram);
  daoTable  = "dbm_bonus_dtl";

  insertTable();

  return(0);
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
