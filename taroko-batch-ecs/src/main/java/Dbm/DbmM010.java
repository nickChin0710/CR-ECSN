/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 110/03/26  V1.00.17  Allen Ho   dbm_m010                                   *
 * 111/11/07  V1.00.18  jiangyigndong  updated for project coding standard    *
 * 112/04/17  V1.00.19  Ryan  新增 p_seqno                                      *
 ******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM010 extends AccessDAO
{
 private final  String PROGNAME = "Debit紅利-卡友紅利積點月結處理程式 112/04/17 V1.00.19";
 CommFunction comm = new CommFunction();
 CommFTP commFTP = null;
 CommRoutine comr = null;

 String businessDate = "";
 

 long    totalCnt=0;
 // ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM010 proc = new DbmM010();
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

   commFTP = new CommFTP(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (!businessDate.substring(6,8).equals("01"))
   {
    showLogMessage("I","","本程式為每月第一天執行.....");
    return(0);
   }   
   if (selectPtrWorkday()!=0)
   {
    showLogMessage("I","","本日非關帳日, 不需執行");
    return(0);
   }    

//   setValue("dmst.acct_month"  , comm.lastMonth(businessDate));
   setValue("dmst.acct_month"  , getValue("wday.this_acct_month") );
   setValue("temp.acct_month"  , comm.lastMonth(getValue("dmst.acct_month")));

   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除重複統計資料月份["+getValue("dmst.acct_month")+"]...");
   deleteDbmMonthStat();
   showLogMessage("I","","    刪除 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存統計資料(dbm_month_stat)...");
   loadDbmMonthStat();
   loadPtrSysIdtab();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","統計本月新增資料...");
   selectDbmBonusDtl();
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
//************************************************************************ 
public int  selectPtrWorkday() throws Exception
{
	extendField = "wday.";
	daoTable  = "ptr_workday";
	whereStr  = "where this_close_date = ? ";
	
	setString(1,businessDate);
	
	int recCnt = selectTable();
	
	if ( notFound.equals("Y") ) return(1);
	
	return(0);
} 
 
 // ************************************************************************
 void selectDbmBonusDtl() throws Exception
 {
     StringBuffer sb = new StringBuffer();
          sb.append("select  " );
          sb.append("acct_type ,  " );
          sb.append("id_p_seqno ,  ");
          sb.append( "p_seqno ,");		  
          sb.append( "bonus_type,");
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN ");  
          sb.append( "          decode(tran_code,'0',beg_tran_bp,0) "); 
          sb.append( "     ELSE 0 "); 
          sb.append( "     END ) as mov_bp , ");             
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN ");
          sb.append( "          decode(tran_code,'1',beg_tran_bp,0) ");
          sb.append( "     ELSE 0 "); 
          sb.append( "     END )   as new_bp, ");       
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN "); 
          sb.append( "          decode(tran_code,'2',beg_tran_bp,0) ");
          sb.append( "     ELSE 0 ");
          sb.append( "     END ) as giv_bp,   ");
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN ");
          sb.append( "          decode(tran_code,'3',beg_tran_bp,0) ");
          sb.append( "     ELSE 0 ");
          sb.append( "     END ) as adj_bp, ");
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN "); 
          sb.append( "          decode(tran_code,'4',beg_tran_bp,0) ");
          sb.append( "     ELSE 0 ");
          sb.append( "     END )  as use_bp, "); 
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN ");
          sb.append( "          decode(tran_code,'5',beg_tran_bp,0) ");
          sb.append( "     ELSE 0 "); 
          sb.append( "     END )  as inp_bp, ");
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN ");
          sb.append( "          decode(tran_code,'6',beg_tran_bp,0) ");
          sb.append( "     ELSE 0 ");
          sb.append( "     END )  as rem_bp, ");
          sb.append( "sum( CASE WHEN acct_date > ? AND acct_date <= ? THEN ");
          sb.append( "          decode(tran_code,'7',beg_tran_bp,0) ");
          sb.append( "     ELSE 0 ");
          sb.append( "     END )   as dxc_bp , ");        	        
          sb.append( "sum(end_tran_bp) as this_month_bp ");
          sb.append( "from  ");
          sb.append( "( ");          
          sb.append( "select acct_type,id_p_seqno , acct_date , bonus_type , tran_code , beg_tran_bp , end_tran_bp , ");
          sb.append( "(case ");
          sb.append( " when p_seqno = '' ");          
          sb.append( " then ");  
          sb.append( " nvl( (select bb.p_seqno from dbc_card bb where bb.id_p_seqno = a.id_p_seqno  and bb.current_code = '0' Fetch first row only),'' ) ");  
          sb.append( " else p_seqno end ) as p_seqno ");  
          sb.append( " from dbm_bonus_dtl a ");          
          sb.append( " where ((acct_date > ? ");
          sb.append( "  and  acct_date <= ?)  ");
          sb.append( " or   (acct_date <= ?  ");
          sb.append( "  and  end_tran_bp != 0)) ");   
          sb.append( ")  aa ");           
          sb.append( "group by id_p_seqno,p_seqno,acct_type,bonus_type ");
          sqlCmd = sb.toString();

  setString(1,  getValue("wday.last_close_date"));
  setString(2,  getValue("wday.this_close_date"));  
  setString(3,  getValue("wday.last_close_date"));
  setString(4,  getValue("wday.this_close_date")); 
  setString(5,  getValue("wday.last_close_date"));
  setString(6,  getValue("wday.this_close_date"));  
  setString(7,  getValue("wday.last_close_date"));
  setString(8,  getValue("wday.this_close_date")); 
  setString(9,  getValue("wday.last_close_date"));
  setString(10, getValue("wday.this_close_date"));  
  setString(11, getValue("wday.last_close_date"));
  setString(12, getValue("wday.this_close_date")); 
  setString(13, getValue("wday.last_close_date"));
  setString(14, getValue("wday.this_close_date")); 
  setString(15, getValue("wday.last_close_date"));
  setString(15, getValue("wday.this_close_date")); 
  
  setString(17, getValue("wday.last_close_date"));
  setString(18, getValue("wday.this_close_date"));
  setString(19, getValue("wday.last_close_date"));  

  openCursor();
  int cnt1=0;
  while( fetchTable() )
  {
   setValue("stat.id_p_seqno"  , getValue("id_p_seqno"));
   setValue("stat.acct_type"   , getValue("acct_type"));
   setValue("stat.bonus_type"  , getValue("bonus_type"));
   setValue("stat.p_seqno"     , getValue("p_seqno"));
//   cnt1 = getLoadData("stat.id_p_seqno,stat.acct_type,stat.bonus_type");
   cnt1 = getLoadData("stat.id_p_seqno,stat.p_seqno,stat.acct_type,stat.bonus_type");

   setValue("dmst.last_month_bp" , "0");
   if (cnt1!=0)
    setValue("dmst.last_month_bp" , getValue("stat.this_month_bp"));

   setValue("dmst.mov_bp"              , getValue("mov_bp"));
   setValue("dmst.new_bp"              , getValue("new_bp"));
   setValue("dmst.giv_bp"              , getValue("giv_bp"));
   setValueDouble("dmst.adj_bp"        , getValueDouble("adj_bp")
           + getValueDouble("dxc_bp"));
   setValue("dmst.use_bp"              , getValue("use_bp"));
   setValue("dmst.inp_bp"              , getValue("inp_bp"));
   setValue("dmst.rem_bp"              , getValue("rem_bp"));

//   setValueDouble("dmst.diff_bp"       , getValueDouble("dmst.last_month_bp")
     setValueDouble("dmst.diff_bp"       , getValueDouble("this_month_bp")
           - getValueDouble("dmst.last_month_bp")   
           - getValueDouble("mov_bp")
           - getValueDouble("new_bp")
           - getValueDouble("giv_bp")
           - getValueDouble("adj_bp")
           - getValueDouble("dxc_bp")
           - getValueDouble("use_bp")
           - getValueDouble("inp_bp")
           - getValueDouble("rem_bp") );
//           - getValueDouble("this_month_bp") );

   setValue("dmst.this_month_bp"       , getValue("this_month_bp"));

   insertDbmMonthStat();

   processDisplay(50000); // every 10000 display message
  }
  closeCursor();
 }
 // ************************************************************************
 int insertDbmMonthStat() throws Exception
 {
  extendField = "dmst.";
  setValue("dmst.p_seqno"              , getValue("p_seqno"));
  setValue("dmst.acct_type"            , getValue("acct_type"));
  setValue("dmst.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("dmst.bonus_type"           , getValue("bonus_type"));
  setValue("dmst.mod_time"             , sysDate+sysTime);
  setValue("dmst.mod_pgm"              , javaProgram);
  daoTable  = "dbm_month_stat";

  insertTable();

  return(0);
 }
 // ************************************************************************
 int deleteDbmMonthStat() throws Exception
 {
  daoTable  = "dbm_month_stat";
  whereStr  = "WHERE acct_month = ? "  
  ;

//  setString(1 , getValue("dmst.acct_month"));
  setString(1 , getValue("wday.this_acct_month"));

  totalCnt = deleteTable();

  return(0);
 }
 // ************************************************************************
// void loadDbmBonusDtl() throws Exception
// {
//  extendField = "bonu.";
//  selectSQL = "acct_type,"
//          + "id_p_seqno,"
//          + "p_seqno,"		  
//          + "bonus_type,"
//          + "sum(decode(tran_code,'0',beg_tran_bp,0)) as mov_bp,"
//          + "sum(decode(tran_code,'1',beg_tran_bp,0)) as new_bp,"
//          + "sum(decode(tran_code,'2',beg_tran_bp,0)) as giv_bp,"
//          + "sum(decode(tran_code,'3',beg_tran_bp,0)) as adj_bp,"
//          + "sum(decode(tran_code,'4',beg_tran_bp,0)) as use_bp,"
//          + "sum(decode(tran_code,'5',beg_tran_bp,0)) as inp_bp,"
//          + "sum(decode(tran_code,'6',beg_tran_bp,0)) as rem_bp ";
//  daoTable  = "dbm_bonus_dtl";
//  whereStr  = "WHERE acct_month = ? "
//          + "group by id_p_seqno,p_seqno,acct_type,bonus_type "		   
//  ;
//  setString(1,getValue("dmst.acct_month"));
//
//  int  n = loadTable();
//  setLoadData("bonu.id_p_seqno,bonu.p_seqno , bonu.acct_type , bonu.bonus_type");
//
//  showLogMessage("I","","Load dbm_bonus_dtl : ["+n+"]");
// }
 // ************************************************************************
 void loadDbmMonthStat() throws Exception
 {
  extendField = "stat.";
  selectSQL = "acct_type,"
          + "id_p_seqno,"
          + "bonus_type,"
          + "this_month_bp"
          + ",p_seqno";
  daoTable  = "dbm_month_stat";
  whereStr  = "WHERE acct_month = ? ";

  setString(1,getValue("temp.acct_month"));

  int  n = loadTable();
//  setLoadData("stat.id_p_seqno,stat.acct_type,stat.bonus_type");
  setLoadData("stat.id_p_seqno,stat.p_seqno,stat.acct_type,stat.bonus_type");
  showLogMessage("I","","Load last month dbm_month_stat : ["+n+"]");
 }
// ************************************************************************
 void loadPtrSysIdtab() throws Exception 
 {
  extendField = "prpt.";
  selectSQL = "wf_id,"            
            + "wf_desc";
  daoTable  = "ptr_sys_idtab";
  whereStr  = "WHERE wf_type='BONUS_NAME' ";

  int  n = loadTable();

  setLoadData("prpt.wf_id");

  showLogMessage("I","","Load ptr_sys_idtab Count: ["+n+"]");
 } 

}  // End of class FetchSample

