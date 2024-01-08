/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112-05-12  V1.00.01  Simon      關帳-帳單不列印處理程式改寫原先的 CycA440  *
* 112-05-12  V1.00.02  Simon      revised handle of cyc_print.RUN_PRINT_COND *
* 112-05-14  V1.00.03  Simon      fixed missed select-p_seqno in loadActDebtSum()*
* 112-05-15  V1.00.04  Simon      acmm.unprint_flag_regular update revised   *
* 112/11/01  V1.00.05  Simon      帳單沒有交易不下檔控制，搭配 CycA430       *
* 112/11/09  V1.00.06  Simon      帳單金額大於零之下未選擇應繳總額小於x元不印或*
*                                 有本金欠款者則設定 unprint_flag_regular="N"(列印) *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA440 extends AccessDAO
{
 private  String progname = "關帳-帳單不列印處理程式 112/11/09 V1.00.06";
 CommFunction comm = new CommFunction();

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String hWdayThisAcctMonth = "";
 String hWdayLastAcctMonth = "";
 String cprntKey="cprnt.acct_type";
 String bonuhKey="bonuh.p_seqno";
 String debsmKey="debsm.p_seqno";
 String pyajKey="pyaj.p_seqno";
 String hAcmmRowid="";

 int    printTag =0;
 long   totalCnt=0;
 int debug =1;
 int insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA440 proc = new CycA440();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
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

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if (( args.length >= 1 )&&(args[0].length()==8))
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新開始....");
   deleteCycFeeprintLog1();

   loadCycPrint();
   loadMktBonusHst();
   loadActDebtSum();
 //loadCycPyaj();

   processCycAcmm();
   closeCursor();

   showLogMessage("I","","累計處理筆數["+totalCnt+"]");
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
  public int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle      =  getValue("wday.STMT_CYCLE");
  hWdayThisAcctMonth =  getValue("wday.this_acct_month");
  hWdayLastAcctMonth =  getValue("wday.last_acct_month");

  return(0);
 }

// ************************************************************************
 void deleteCycFeeprintLog1() throws Exception
 {
  daoTable  = "cyc_feeprint_log a";
  whereStr  = "WHERE  acct_month = ? "
            + "AND    p_seqno = (select p_seqno "
            + "                  from   cyc_acmm_"+hWdayStmtCycle
            + "                  where  p_seqno = a.p_seqno) ";

  setString(1 , hWdayThisAcctMonth);

  deleteTable();

  return;
 }

//************************************************************************
  public int loadCycPrint() throws Exception
  {
    daoTable    = "cyc_print";
    extendField = "cprnt.";
    selectSQL   = "acct_type,"
                + "collection_flag,"
                + "debit_flag,"
                + "network_flag,"
                + "email_flag,"
                + "ttl_zero_flag,"
                + "bonus_flag,"
                + "problem_tx,"
                + "ttl_minus_flag,"
                + "hellow_word,"
                + "hellow_word2,"
                + "overpay_amt,"
                + "run_print_mm,"
                + "run_print_end_bal,"
                + "run_print_cond,"
                + "combo_min_cond,"
                + "combo_min_months,"
                + "overpay_one";
    whereStr    = " order by acct_type";
    int n = loadTable();
    setLoadData(cprntKey);
    if ( n == 0 )
       { showLogMessage("E","","load_cyc_print ERROR "); exitProgram(3); }

    return n;
  }

  public int loadMktBonusHst() throws Exception
  {
    daoTable    = "mkt_bonus_hst";
    extendField = "bonuh.";
    sqlCmd = "select "
           + "p_seqno,"
           + "last_month_bonus,"
           + "new_add_bonus,"
           + "adjust_bonus,"
           + "remove_bonus,"
           + "use_bonus,"
           + "give_bonus,"
           + "diff_bonus,"
           + "net_bonus,"
           + "first_bonus   as net_tt4,"
           + "second_bonus  as net_tt3,"
           + "last_6_bonus  as net_tt2,"
           + "last_12_bonus as net_tt1 "
           + "from  mkt_bonus_hst "
           + "WHERE acct_month = ? and stmt_cycle = ? order by p_seqno ";

    setString(1,hWdayThisAcctMonth);
    setString(2,hWdayStmtCycle);
    int n = loadTable();
    setLoadData(bonuhKey);
    return n;
  }

  public int loadActDebtSum() throws Exception
  {
    daoTable    = "act_debt_sum_"+hWdayStmtCycle;
    extendField = "debsm.";
    selectSQL   = "p_seqno,"
                + "sum(end_cap_amt) as cap_amt_bal";
    whereStr    = "group by p_seqno order by p_seqno";
    int n = loadTable();
    setLoadData(debsmKey);
    return n;
  }

  public int loadCycPyaj() throws Exception
  {
     daoTable    = "cyc_pyaj";
     extendField = "pyaj.";
     selectSQL   = "p_seqno,abs(sum(payment_amt)) as op02_amt";
     whereStr    = "WHERE payment_type in ('OP02', 'OP03') AND settle_date  = ? "
                 + "group by p_seqno ";
     setString(1,hBusiBusinessDate);
     int n = loadTable();
     setLoadData(pyajKey);
     return n;
  } 

  public void processCycAcmm() throws Exception
  {
    daoTable    = "cyc_acmm_"+hWdayStmtCycle;
    fetchExtend = "acmm.";
    selectSQL   = "p_seqno,"
                + "rowid as rowid,"
                + "acno_flag,"
                + "acct_type,"
                + "acct_key,"
                + "id_p_seqno,"
                + "unprint_flag,"
                + "acct_status,"
                + "last_ttl_amt,"
                + "payment_amt,"
                + "adjust_amt,"
                + "this_ttl_amt,"
                + "cap_amt_bal,"
                + "send_paper,"
                + "send_internet,"
                + "send_fax,"
                + "ttl_cash,"
                + "ttl_purchase,"
                + "statement_count,"
                + "problem_tx_cnt,"
                + "valid_card_cnt ";
    whereStr    = "WHERE acno_flag != 'Y' order by p_seqno ";

    openCursor();

    while( fetchTable() )  {

      processDisplay(5000);

      hAcmmRowid = getValue("acmm.rowid");
      setValue(cprntKey,getValue("acmm.acct_type"));
      getLoadData(cprntKey);  // 讀 cyc_print

      setValue(bonuhKey,getValue("acmm.p_seqno"));
      getLoadData(bonuhKey); // 讀 mkt_bonus_hst

      setValue(debsmKey,getValue("acmm.p_seqno"));
      getLoadData(debsmKey);      // 讀 act_debt_sum
      setValue("acmm.cap_amt_bal",getValue("debsm.cap_amt_bal"));

    //setValue(pyajKey,getValue("acmm.p_seqno"));
    //getLoadData(pyajKey);  // 讀 cyc_pyaj

      if (!getValue("acmm.unprint_flag").equals("Y") ) {
        if ((getValue("acmm.acct_status").equals("3")   && 
             getValue("cprnt.collection_flag").equals("Y")) ||
            (getValue("acmm.acct_status").equals("4")   &&
             getValue("cprnt.debit_flag").equals("Y"))     ) {
          setValue("acmm.unprint_flag","Y");  
        } 
      }

      setValue("acmm.unprint_flag_regular","");  
      checkPrintRegular();
      updateCycAcmm();

    } // end of while fetch

    return;
  }

  public void checkPrintRegular() throws Exception
  {
    printTag = 0;
    if ( getValueLong("acmm.adjust_amt")  != 0 || getValueLong("acmm.payment_amt") != 0 ||
         getValueLong("acmm.new_amt")     != 0 || getValueLong("acmm.ttl_cash")    != 0 ||  
         getValueLong("acmm.ttl_purchase") != 0  ) {
       setValue("acmm.unprint_flag_regular","N"); 
    }
    else
    if ( getValueLong("acmm.statement_count") != 0 )
    { setValue("acmm.unprint_flag_regular","N"); }
    else
    if ( getValueLong("bonuh.last_month_bonus") != getValueLong("bonuh.net_bonus") )
    { 
      if ( getValueLong("acmm.this_ttl_amt") == 0 && 
           getValue("cprnt.ttl_zero_flag").equals("Y") && 
           getValue("cprnt.bonus_flag").equals("Y") ) 
      { setValue("acmm.unprint_flag_regular","N"); }
      else 
      { setValue("acmm.unprint_flag_regular","Y"); }
    }
    else
    if ( getValueLong("acmm.this_ttl_amt") < 0 ) 
    {
      if ( (getValueLong("acmm.this_ttl_amt") * -1) >= getValueLong("cprnt.overpay_amt") )
      { setValue("acmm.unprint_flag_regular","N"); }
      else 
      { setValue("acmm.unprint_flag_regular","Y"); }
    }
    else
    if ( getValueLong("acmm.this_ttl_amt") > 0 ) 
    {
      if ( getValueLong("acmm.this_ttl_amt") <= getValueLong("cprnt.run_print_end_bal") )
      {
        if ( getValue("cprnt.run_print_cond").equals("Y") &&  
             getValueLong("acmm.cap_amt_bal") == 0 )
        {
          int ck = getFeePrintCnt();
          if ( ck >= getValueInt("cprnt.run_print_mm") )
          { setValue("acmm.unprint_flag_regular","Y"); }
          else
          { setValue("acmm.unprint_flag_regular","N"); }
        } else {
          setValue("acmm.unprint_flag_regular","N"); 
        }
      }
      else 
      { setValue("acmm.unprint_flag_regular","N"); }
    }
    else
    { setValue("acmm.unprint_flag_regular","X"); }

    if ( getValueLong("acmm.this_ttl_amt") > 0 &&
         getValueLong("acmm.cap_amt_bal") == 0 && 
       //getValue("cprnt.run_print_cond").equals("Y") && 
       //getValueLong("acmm.this_ttl_amt") <= getValueLong("cprnt.run_print_end_bal") && 
         getValue("acmm.unprint_flag_regular").equals("N") )
    {
      insertCycFeeprintLog();
    }

    return;
  }

//************************************************************************
  int getFeePrintCnt() throws Exception
  {
    extendField = "cfplg.";
    selectSQL = "p_seqno,"
              + "acct_month,"
              + "this_ttl_amt ";
    daoTable  = "cyc_feeprint_log";
    whereStr  = "WHERE   p_seqno   = ? "
              + "ORDER BY acct_month DESC";
  
    setString(1  , getValue("acmm.p_seqno"));
  
    int recCnt = selectTable();
  
    String meetAcctMonth = "";
    int cfpCnt = 0;
  
    for ( int inti=0; inti<recCnt; inti++ )
    {
      if (cfpCnt >= getValueInt("cprnt.run_print_mm") ) {
        break;
      }
       
      if (cfpCnt == 0 ) {
        if (getValueLong("cfplg.this_ttl_amt",inti) <= 
            getValueLong("cprnt.run_print_end_bal")) {
          meetAcctMonth = getValue("cfplg.acct_month",inti);
          cfpCnt++;
          continue;
        } else {
          continue;
        } 
      } else {
		    meetAcctMonth = comm.nextMonth(meetAcctMonth,-1);
        if (getValueLong("cfplg.this_ttl_amt",inti) <= 
            getValueLong("cprnt.run_print_end_bal") &&
            getValue("cfplg.acct_month",inti).equals(meetAcctMonth)) {
          cfpCnt++;
          continue;
        } else {
        	break;
        }
      }

    } //end of for-loop
  
    return cfpCnt;
  }

  public void insertCycFeeprintLog() throws Exception
  {
    daoTable = "cyc_feeprint_log";
    setValue("p_seqno",getValue("acmm.p_seqno"));
    setValue("acct_type",getValue("acmm.acct_type"));
    setValue("acct_month",getValue("wday.this_acct_month"));
    setValueLong("this_ttl_amt", getValueLong("acmm.this_ttl_amt"));
    setValue("mod_pgm",javaProgram);
    setValue("mod_time",sysDate+sysTime);
    insertTable();
    return;
  }

//************************************************************************
  int updateCycAcmm() throws Exception
  {
    daoTable  = "cyc_acmm_"+hWdayStmtCycle;
  
    updateSQL = "unprint_flag         = ?, "
              + "unprint_flag_regular = ?, "
              + "cap_amt_bal          = ?, "
              + "mod_time             = sysdate,"
              + "mod_pgm              = ? ";
      whereStr  = "where rowid          = ? ";
  
    setString(1 , getValue("acmm.unprint_flag"));    
    setString(2 , getValue("acmm.unprint_flag_regular"));    
    setLong(3 ,   getValueLong("acmm.cap_amt_bal"));    
    setString(4, javaProgram);
    setRowId(5 , hAcmmRowid);
  
    int n = updateTable();
    if ( n == 0 )
       { showLogMessage("E","","update_cyc_acmm ERROR ");  }
  
    return n;
   }

}  // End of class FetchSample

