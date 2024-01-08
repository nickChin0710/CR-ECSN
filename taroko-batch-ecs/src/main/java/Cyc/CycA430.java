/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/09  V1.00.17  Allen Ho   cyc_A230                                   *
* 110/09/22  V1.01.01  Allen Ho   display all no match p_seqno               *
* 111/10/17  V1.00.02  Yang Bo    sync code from mega                        *
* 112/05/12  V1.00.03  Simon      1.not to update cyc_acmm_xx.unprint_flag   *
*                                 2.revised to update cyc_acmm_curr_xx.print_flag*
* 112/05/30  V1.00.04  Simon      add input parm "force_complete"            *
* 112/06/10  V1.00.05  Simon      remove exitProgram(1) in selectCycAcmmCurrError()*
* 112/07/19  V1.00.06  Simon      remove invalid & no-use selectCycAbem2()   *
* 112/11/01  V1.00.07  Simon      帳單沒有交易不下檔控制，搭配 CycA440       *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycA430 extends AccessDAO
{
 private final String PROGNAME = "關帳-對帳單明細區資料計算及主檔更新處理程式 112/11/01 "
                               + "V1.00.07";
 CommFunction comm = new CommFunction();

 String businessDate = "";
 String hWdayStmtCycle = "";
 String hWdayThisAcctMonth = "";
 String hWdayLastAcctMonth = "";
 String tranSeqno = "";
 String fundCode ="";
 String pSeqno ="";
 String forceComplete ="";

 long    totalCnt=0,currCnt=0;
 int parmCnt=0;
 int cnt1=0;
 boolean debug = false;
 int[] dInt = {0,0,0,0};
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA430 proc = new CycA430();
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
   showLogMessage("I","",javaProgram+" "+ PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date] or [force_complete]");
       showLogMessage("I","","PARM 2 : [DEBUG]");
       showLogMessage("I","","PARM 3 : [P_SEQNO]");
       return(0);
      }

   if ( args.length >= 1 )
     if ( args[0].equals("Y") )
       { forceComplete = args[0]; }
     else
       { businessDate  = args[0]; }

   if (args.length >= 2 )
      {
       if (args[1].equals("DEBUG"))
          debug = true;
       else
         {
          showLogMessage("I","","PARM 2 : [DEBUG] error !");
          return(0);
         }
      }
   if ( args.length == 3 )
      { pSeqno = args[2]; }
   
   if ( !connectDataBase() ) return(1);

   selectPtrBusinday();
   loadPtrCurrcode();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

  if (debug)
     {
      showLogMessage("I","","=========================================");
      showLogMessage("I","","DEBUG MODE 清檔開始...");
      deleteCycAbemDebug();
      showLogMessage("I","","DEBUG MODE 清檔完成");
      commitDataBase();
     }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","cyc_abem 處理開始...");
   selectCycAbem();
   showLogMessage("I","","total_count=["+totalCnt+"] curr_cnt["+currCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","cyc_acmm_curr 處理開始...");
   selectCycAbem1();
   showLogMessage("I","","total_count=["+totalCnt+"] curr_cnt["+currCnt+"]");
   showLogMessage("I","","=========================================");
/***
   showLogMessage("I","","cyc_acmm_curr 05 處理開始...");
   selectCycAbem2();
   showLogMessage("I","","total_count=["+totalCnt+"] curr_cnt["+currCnt+"]");
   showLogMessage("I","","=========================================");
***/
   updateCycAcmm1();
 //updateCycAcmm2();
    
   selectCycAcmm();
  
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
  showLogMessage("I","","本日營業日: ["+ businessDate +"]");
 }
// ************************************************************************ 
  public int selectPtrWorkday() throws Exception
 {
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1, businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle =  getValue("STMT_CYCLE");
  hWdayThisAcctMonth =  getValue("this_acct_month");
  hWdayLastAcctMonth =  getValue("last_acct_month");

  return(0);
 }
// ************************************************************************
 void selectCycAbem() throws Exception
 {
  selectSQL = "p_seqno,"
            + "sum(decode(print_type,'01',dest_amt,0)) as last_ttl_amt,"
            + "sum(decode(print_type,'02',decode(dummy_code,'Y',0,'S',0,dest_amt),0)) as payment_amt,"
            + "sum(decode(print_type,'03',decode(dummy_code,'Y',0,'S',0,dest_amt),0)) as adjust_amt,"
            + "sum(decode(print_type,'04',decode(dummy_code,'Y',0,'S',0,dest_amt),"
            + "                      '05',decode(dummy_code,'S',0,'Y',0,dest_amt),"
            + "                      '06',decode(dummy_code,'Y',0,'S',0,dest_amt),0)) as new_amt,"
            + "sum(decode(print_type,'08',decode(dummy_code,'Y',0,1),0)) as problem_tx_cnt,"
            + "sum(decode(acct_code,'CA',dest_amt,0)) as ttl_cash,"
            + "sum(decode(acct_code,'CA',decode(sign(dest_amt),-1,-1,1),0)) as ttl_cash_cnt,"
            + "sum(decode(print_type,'08',0,"
            + "    decode(acct_code,'BL',dest_amt,"
            + "                     'IT',dest_amt,"
            + "                     'ID',dest_amt,"
            + "                     'OT',dest_amt,"
            + "                     'AO',dest_amt,0))) as ttl_purchase,"
            + "sum(decode(print_type,'08',0, "
            + "    decode(acct_code,'BL',decode(sign(dest_amt),-1,-1,1),"
            + "                     'IT',decode(sign(dest_amt),-1,-1,1),"
            + "                     'ID',decode(sign(dest_amt),-1,-1,1),"
            + "                     'OT',decode(sign(dest_amt),-1,-1,1),"
            + "                     'AO',decode(sign(dest_amt),-1,-1,1),0))) as ttl_purch_cnt,"
            + "sum(decode(print_type,'08',0,"
            + "    decode(substr(dummy_code,1,1),'2',dest_amt,0))) as no_module2_amt,"
            + "sum(decode(print_type,'06',1,0)) as temp_06_cnt,"
          //+ "count(*) as temp_count ,"
            + "sum(decode(print_type,'01',0,'07',0,1)) as temp_count,"
            + "max(decode(print_type,'08',1,0)) as temp_08_cnt ";
  daoTable  = " cyc_abem_"+ hWdayStmtCycle;
            ;

  if (pSeqno.length()>0)
     {
      whereStr  = "where p_seqno = ?  "
                + "group by p_seqno";

      setString(1 , pSeqno);
     }
  else
     {
      whereStr  = "group by p_seqno";
     }

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    updateCycAcmm();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void selectCycAbem1() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "sum(decode(print_type,'01',dest_amt,0)) as last_ttl_amt,"
            + "sum(decode(print_type,'02',decode(dummy_code,'Y',0,'S',0,dest_amt),0)) as payment_amt,"
            + "sum(decode(print_type,'03',decode(dummy_code,'Y',0,'S',0,dest_amt),0)) as adjust_amt,"
            + "sum(decode(print_type,'04',decode(dummy_code,'Y',0,'S',0,dest_amt),"
            + "                      '05',decode(dummy_code,'S',0,'Y',0,dest_amt),"
            + "                      '06',decode(dummy_code,'Y',0,'S',0,dest_amt),0)) as new_amt,"
            + "sum(decode(print_type,'01',dc_dest_amt,0)) as dc_last_ttl_amt,"
            + "sum(decode(print_type,'02',decode(dummy_code,'Y',0,'S',0,dc_dest_amt),0)) as dc_payment_amt,"
            + "sum(decode(print_type,'03',decode(dummy_code,'Y',0,'S',0,dc_dest_amt),0)) as dc_adjust_amt,"
            + "sum(decode(print_type,'04',decode(dummy_code,'Y',0,'S',0,dc_dest_amt),"
            + "                      '05',decode(dummy_code,'S',0,'Y',0,dc_dest_amt),"
            + "                      '06',decode(dummy_code,'Y',0,'S',0,dc_dest_amt),0)) as dc_new_amt,"
            + "sum(decode(print_type,'06',1,0)) as temp_06_cnt ,"
            + "sum(decode(print_type,'06',decode(dummy_code,'Y',1,0),0)) as temp_06_dummy_cnt";
  daoTable  = " cyc_abem_"+ hWdayStmtCycle;
            ;

  if (pSeqno.length()>0)
     {
      whereStr  = "where p_seqno = ?  "
                + "group by p_seqno,curr_code";

      setString(1 , pSeqno);
     }
  else
     {
      whereStr  = "group by p_seqno,curr_code";
     }
  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
/*   
   showLogMessage("I","","dc_last_ttl_am =["+ getValueDouble("dc_last_ttl_amt") +"]");
   showLogMessage("I","","dc_payment_amt =["+ getValueDouble("dc_payment_amt") +"]"); 
   showLogMessage("I","","dc_adjust_amt  =["+ getValueDouble("dc_adjust_amt") +"]"); 
   showLogMessage("I","","dc_new_amt     =["+ getValueDouble("dc_new_amt")  +"]");
   
   showLogMessage("I","","dc_ttl_amt = ["+ comm_curr_amt(getValue("curr_code"), (getValueDouble("dc_last_ttl_amt")
              + getValueDouble("dc_payment_amt")
              + getValueDouble("dc_adjust_amt")
              + getValueDouble("dc_new_amt")),0)+"]");
 
   select_cyc_acmm_x();
*/   
   updateCycAcmmCurr();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void selectCycAbem2() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "max(a.curr_code) as curr_code,"
            + "max(b.bill_sort_seq) as bill_sort_seq,"
            + "max(a.acct_type) as acct_type ";
  daoTable  = "cyc_abem_"+ hWdayStmtCycle +" a,ptr_currcode b";
  whereStr  = "WHERE  a.p_seqno     = ? "
            + "AND    a.curr_code   = ? "
            + "AND    a.print_type = '06' "
            + "AND    a.curr_code = b.curr_code "
            + "GROUP BY a.p_seqno "
            + "HAVING min(print_seq)!='0000' ";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("curr_code"));

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    insertCycAbem();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void updateCycAcmm() throws Exception
 {
  currCnt++;
  dateTime();
  updateSQL = "last_ttl_amt    = ?,"
            + "payment_amt     = ?," 
            + "adjust_amt      = ?," 
            + "new_amt         = ?," 
            + "problem_tx_cnt  = ?," 
            + "ttl_cash        = ?," 
            + "ttl_cash_cnt    = ?," 
            + "ttl_purchase    = ?," 
            + "ttl_purch_cnt   = ?," 
            + "no_module2_amt  = ?," 
            + "statement_count = ?," 
          //+ "                + decode(sign(erase_bonus),1,1,0) "
          //+ "                + decode(sign(this_ttl_amt),-1,0,3) "
          //+ "                + decode(give_reason_a,'A',1,0) "
          //+ "                + decode(give_reason_b,'B',1,0) "
          //+ "                + decode(give_reason_c,'C',1,0) "
          //+ "                + decode(give_reason_d,'D',1,0), "
            + "mod_pgm         = ?, "
            + "mod_time        = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "cyc_acmm_"+ hWdayStmtCycle;
  whereStr  = "WHERE p_seqno = ? ";

  setString(1 , getValue("last_ttl_amt"));
  setString(2 , getValue("payment_amt"));
  setString(3 , getValue("adjust_amt"));
  setString(4 , getValue("new_amt"));
  setString(5 , getValue("problem_tx_cnt"));
  setString(6 , getValue("ttl_cash"));
  setString(7 , getValue("ttl_cash_cnt"));
  setString(8 , getValue("ttl_purchase"));
  setString(9 , getValue("ttl_purch_cnt"));
  setInt(10   , getValueInt("ttl_purchase")-getValueInt("no_module2_amt"));
//setInt(11   , getValueInt("temp_count")+6+getValueInt("temp_08_cnt"));
  setInt(11   , getValueInt("temp_count"));
  setString(12 , javaProgram);
  setString(13 , sysDate+sysTime);
  setString(14 , getValue("p_seqno"));

  updateTable();
  return;
 }
//************************************************************************
  void updateCycAcmm1() throws Exception
  {
    currCnt++;
    dateTime();
  //updateSQL = "unprint_flag  =  'Y',"
    updateSQL = "print_flag    =  'N'";
    daoTable  = "cyc_acmm_"+ hWdayStmtCycle +" a";
    whereStr  = "WHERE  print_flag = 'Y' "
              + "AND    exists (select 1 "
              + "               from cyc_acmm_curr_"+ hWdayStmtCycle
              + "               where p_seqno = a.p_seqno  "
              + "               and   decode(print_flag,'','Y',print_flag) = 'N') "
              ;
   
    if (pSeqno.length()>0)
       {
        whereStr  = whereStr  
                  + "and   p_seqno = ?  ";
   
        setString(1 , pSeqno);
       }
   
    updateTable();
    return;
  }
//************************************************************************
/***
  void updateCycAcmm2() throws Exception
  {
    currCnt++;
    updateSQL = "unprint_flag  =  'Y'";
    daoTable  = "cyc_acmm_"+ hWdayStmtCycle;
    whereStr  = "WHERE  print_flag = 'N' "
              + "AND    decode(unprint_flag,'','N',unprint_flag) = 'N' "
              ;
    if (pSeqno.length()>0)
       {
        whereStr  = whereStr  
                  + "and   p_seqno = ?  ";
  
        setString(1 , pSeqno);
       }
  
    updateTable();
    return;
  }
***/

// ************************************************************************
 void updateCycAcmmCurr() throws Exception
 {
  currCnt++;
  dateTime();
  updateSQL = "last_ttl_amt    = ?,"
            + "payment_amt     = ?,"
            + "adjust_amt      = ?,"
            + "new_amt         = ?,"
          //+ "print_flag      = decode(print_flag,'','Y','N','N',decode(dc_this_ttl_amt,?,'Y','N')),"  
            + "print_flag      = decode(dc_this_ttl_amt,?,'Y','N'),"  
            + "dc_last_ttl_amt = ?,"
            + "dc_payment_amt  = ?,"
            + "dc_adjust_amt   = ?,"
            + "dc_new_amt      = ?,"
            + "mod_pgm         = ?, "
            + "mod_time        = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "cyc_acmm_curr_"+ hWdayStmtCycle;
  whereStr  = "WHERE p_seqno   = ? "
            + "and   curr_code = ? ";

  setDouble(1 , getValueDouble("last_ttl_amt"));
  setDouble(2 , getValueDouble("payment_amt"));
  setDouble(3 , getValueDouble("adjust_amt"));
  setDouble(4 , getValueDouble("new_amt"));
  setDouble(5 , commCurrAmt(getValue("curr_code"),
                (getValueDouble("dc_last_ttl_amt")
              + getValueDouble("dc_payment_amt")
              + getValueDouble("dc_adjust_amt")
              + getValueDouble("dc_new_amt")),0));
  setDouble(6 , getValueDouble("dc_last_ttl_amt"));
  setDouble(7 , getValueDouble("dc_payment_amt"));
  setDouble(8 , getValueDouble("dc_adjust_amt"));
  setDouble(9 , getValueDouble("dc_new_amt"));
  setString(10 , javaProgram);
  setString(11 , sysDate+sysTime);
  setString(12 , getValue("p_seqno"));
  setString(13 , getValue("curr_code"));

  updateTable();
  return;
 }
// ************************************************************************
 void deleteCycAbemDebug() throws Exception
 {
  daoTable  = "cyc_abem_"+ hWdayStmtCycle;
  whereStr  = "where  print_type ='06' "
            + "and    mod_pgm = ? "
            ;

  setString(1 , javaProgram);

  if (pSeqno.length()>0)
     {
      whereStr  = whereStr  
                + "and   p_seqno = ?  ";

      setString(2 , pSeqno);
     }

  deleteTable();

  return;
 }
// ************************************************************************
 int insertCycAbem() throws Exception
 {
  dateTime();

  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("curr_code"            , getValue("curr_code"));
  setValue("bill_sort_seq"        , getValue("bill_sort_seq"));
  setValue("acct_type"            , getValue("acct_type"));
  setValue("print_type"           , "06");
  setValue("print_seq"            , "0001");
  setValue("description"          , "其他交易明細 : ");
  setValue("dummy_code"           , "Y");
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);

  daoTable  = "cyc_abem_"+ hWdayStmtCycle;

  insertTable();

  return(0);
 }
// ************************************************************************
 void selectCycAcmm() throws Exception
 {
  selectSQL = "sum(decode(print_flag,'Y',1,0)) as h_cnt1,"
            + "sum(decode(print_flag,'N',1,0)) as h_cnt2,"
            + "sum(decode(print_flag,'',1,0)) as h_cnt3";
  daoTable  = "cyc_acmm_"+ hWdayStmtCycle;
  whereStr  = ""
            ;
  if (pSeqno.length()>0)
     {
      whereStr  = whereStr  
                + "where   p_seqno = ?  ";

      setString(1 , pSeqno);
     }

  int recordCnt = selectTable();


  if (getValueInt("h_cnt2") + getValueInt("h_cnt3") >0)
     {
    //showLogMessage("I","","錯誤["+getValueInt("h_cnt2")+"] + 未處理["+getValueInt("h_cnt3")+"]筆數超過 300,終止執行");
      showLogMessage("I","","錯誤["+getValueInt("h_cnt2")+"] + 未處理["+getValueInt("h_cnt3")+"]");
      showLogMessage("I","","       已處理["+getValueInt("h_cnt1")+"]筆數");
      selectCycAcmmCurrError();
      if (!forceComplete.equals("Y")) {
        exitProgram(1);
      }
     }
 }
// ************************************************************************ 
 void selectCycAcmmCurrError() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "dc_this_ttl_amt,"
            + "dc_last_ttl_amt,"
            + "dc_payment_amt,"
            + "dc_adjust_amt,"
            + "dc_new_amt";
  daoTable  = "cyc_acmm_curr_"+ hWdayStmtCycle;
  whereStr  = "where print_flag='N' " 
            ;
  if (pSeqno.length()>0)
     {
      whereStr  = whereStr  
                + "and   p_seqno = ?  ";

      setString(1 , pSeqno);
     }

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++; 
    if (totalCnt>50) break;
    showLogMessage("I","","ERROR p_seqno["+getValue("p_seqno")+"] "
                         + "curr_code["+getValue("curr_code")+"] "
                         + "this["+getValueDouble("dc_this_ttl_amt")+"]="
                         + "last["+getValueDouble("dc_last_ttl_amt")+"] + "
                         + "pay["+getValueDouble("dc_payment_amt")+"]+ "
                         + "adjust["+getValueDouble("dc_adjust_amt")+"]+ "
                         + "new["+getValueDouble("dc_new_amt")+"]"); 
   } 
  closeCursor();
//if (totalCnt>0) exitProgram(1);
  return;
 }

// ************************************************************************
 double commCurrAmt(String currCode, double val, int rnd) throws Exception
 {

  setValue("pcde.curr_code" , currCode);
  cnt1 = getLoadData("pcde.curr_code");

  val = val * 10000.0;
  val = Math.round(val);

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal("10000"));

  if (cnt1==0) return(currAmt.doubleValue());

  double retNum = 0.0;
  if (rnd>0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_UP).doubleValue(); 
  if (rnd==0) retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_HALF_UP).doubleValue(); 
  if (rnd<0)  retNum = currAmt.setScale(getValueInt("pcde.curr_amt_dp"),BigDecimal.ROUND_DOWN).doubleValue(); 

  return(retNum);
 }
// ************************************************************************
 void loadPtrCurrcode() throws Exception
 {
  extendField = "pcde.";
  selectSQL = "curr_code,"
            + "curr_amt_dp";
  daoTable  = "ptr_currcode";
  whereStr  = "";

  int  n = loadTable();

  setLoadData("pcde.curr_code");

  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

