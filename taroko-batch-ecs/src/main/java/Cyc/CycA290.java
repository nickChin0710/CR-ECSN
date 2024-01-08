/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/30  V1.00.21  Allen Ho   cyc_a191                                   *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycA290 extends AccessDAO
{
 private final String PROGNAME = "關帳-紅利主檔統計處理程式 111-11-08  V1.00.01";
 CommFunction comm = new CommFunction();

 String businessDate   = "";
 double lastMonthBonus=0;
 double  newAddBonus=0,adjustBonus=0,useBonus=0,removeBonus=0,netBonus=0,giveBonus=0;
 double  last1Bonus=0,last2Bonus=0,last6Bonus=0,last12Bonus=0;

 int   ntCnt1=0;
 boolean DEBUG= false;

 long    totalCnt=0,updateCnt=0;
 int cnt1;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA290 proc = new CycA290();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

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
      { businessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","DEBUG 刪除資料");
   deleteMktBonusHst();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadMktBonusHst();
   loadPtrSysIdtab();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處裡 mkt_bonus_dtl 資料");
   selectCycDcBonusDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","當月紅利差異計算資料");
   selectMktBonusHst();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 新增 ["+updateCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","剩餘點數計算資料");
   selectCycDcBonusDtl1();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 新增 ["+updateCnt+"] 筆");
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

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
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
 void  selectCycDcBonusDtl() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.bonus_type,"
            + "acct_date,"
            + "max(a.acct_type) as acct_type,"
            + "max(a.id_p_seqno) as id_p_seqno,"
            + "tran_code,"
            + "sum(a.beg_tran_bp) as beg_tran_bp,"
            + "sum(a.end_tran_bp+a.res_tran_bp) as end_tran_bp";
  daoTable  = "mkt_bonus_dtl a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b "; 
  whereStr  = "where a.p_seqno = b.p_seqno "
            + "and  ((a.acct_date > ? "
            + "  and  a.acct_date <= ?)  "
            + " or   (a.acct_date <= ?  "
            + "  and  a.end_tran_bp+a.res_tran_bp!=0)) " 
            + "group by a.p_seqno,a.bonus_type,tran_code,acct_date "
            ;

  setString(1, getValue("wday.last_close_date"));
  setString(2, getValue("wday.this_close_date"));
  setString(3, getValue("wday.last_close_date"));

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    newAddBonus = adjustBonus = useBonus = removeBonus = giveBonus = 0;
    if (getValue("acct_date").compareTo(getValue("wday.last_close_date"))>0)
       {
        if ((getValue("tran_code").equals("1"))||
            (getValue("tran_code").equals("5")))
           newAddBonus = getValueDouble("beg_tran_bp");
        else    
        if ((getValue("tran_code").equals("0"))||
            (getValue("tran_code").equals("3"))||
            (getValue("tran_code").equals("7")))
           adjustBonus = getValueDouble("beg_tran_bp");
        else    
        if (getValue("tran_code").equals("2"))
           giveBonus      = getValueDouble("beg_tran_bp");
        else    
        if (getValue("tran_code").equals("4"))
           useBonus    = getValueDouble("beg_tran_bp");
        else    
        if (getValue("tran_code").equals("6"))
           removeBonus      = getValueDouble("beg_tran_bp");
       }
    netBonus = getValueDouble("end_tran_bp");

    setValue("prpt.wf_id" , getValue("bonus_type"));
    cnt1 = getLoadData("prpt.wf_id");
    if (cnt1>0) setValue("bonus_name", getValue("prpt.wf_desc"));
    else setValue("bonus_name", "標準紅利");

    if (updateMktBonusHst()!=0)
       {
        setValue("bonu.p_seqno"    , getValue("p_seqno"));
        setValue("bonu.bonus_type" , getValue("bonus_type"));
        cnt1 = getLoadData("bonu.p_seqno,bonu.bonus_type");
        lastMonthBonus = 0;
        if (cnt1>0) lastMonthBonus = getValueDouble("bonu.last_month_bonus");
        insertMktBonusHst();
       }
   } 

  closeCursor();
  return;
 }
// ************************************************************************
 void  selectCycDcBonusDtl1() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.bonus_type,"
            + "a.effect_e_date,"
            + "sum(a.end_tran_bp+a.res_tran_bp) as end_tran_bp";
  daoTable  = "mkt_bonus_dtl a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b "; 
  whereStr  = "where a.p_seqno = b.p_seqno "
            + "and   a.effect_e_date !='' "
            + "and   a.effect_e_date <= ? "
            + "and   a.end_tran_bp+a.res_tran_bp > 0 " 
            + "group by a.p_seqno,a.bonus_type,a.effect_e_date "
            ;

  setString(1, comm.nextMonthDate(businessDate,12));

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;

    last1Bonus = last2Bonus = last6Bonus = last12Bonus = 0;
    if (getValue("effect_e_date").compareTo(comm.nextMonthDate(businessDate,1))<=0)
       {
        last1Bonus = getValueDouble("end_tran_bp"); 
       }
    if (getValue("effect_e_date").compareTo(comm.nextMonthDate(businessDate,2))<=0)
       {
        last2Bonus = getValueDouble("end_tran_bp"); 
       }

    if (getValue("effect_e_date").compareTo(comm.nextMonthDate(businessDate,6))<=0)
        last6Bonus = getValueDouble("end_tran_bp");
         
    if (getValue("effect_e_date").compareTo(comm.nextMonthDate(businessDate,12))<=0)
        last12Bonus = getValueDouble("end_tran_bp"); 

    updateMktBonusHst2();
   } 

  closeCursor();
  return;
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
// ************************************************************************
 int insertMktBonusHst() throws Exception
 {
  dateTime();
  setValue("thst.p_seqno"                  , getValue("p_seqno"));
  setValue("thst.acct_month"               , getValue("wday.this_acct_month"));
  setValue("thst.bonus_type"               , getValue("bonus_type"));
  setValue("thst.bonus_name"               , getValue("bonus_name")); 
  setValue("thst.id_p_seqno"               , getValue("id_p_seqno")); 
  setValue("thst.stmt_cycle"               , getValue("wday.stmt_cycle")); 
  setValue("thst.acct_type"                , getValue("acct_type")); 
  setValueDouble("thst.last_month_bonus"   , lastMonthBonus); 
  setValueDouble("thst.new_add_bonus"      , newAddBonus); 
  setValueDouble("thst.adjust_bonus"       , adjustBonus); 
  setValueDouble("thst.use_bonus"          , useBonus); 
  setValueDouble("thst.give_bonus"         , giveBonus); 
  setValueDouble("thst.remove_bonus"       , removeBonus);
  setValue("thst.diff_bonus"               , "0");
  setValueDouble("thst.net_bonus"          , netBonus); 
  setValue("thst.mod_time"                 , sysDate+sysTime);
  setValue("thst.mod_pgm"                  , javaProgram);

  extendField = "thst.";
  daoTable  = "mkt_bonus_hst";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteMktBonusHst() throws Exception
 {
  daoTable  = "mkt_bonus_hst";
  whereStr  = "where acct_month  = ? "
            + "and   stmt_cycle  = ? ";
             

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  totalCnt = deleteTable();

  return;
 }
// ************************************************************************
 void  loadMktBonusHst() throws Exception
 {
  extendField = "bonu.";
  selectSQL = "a.p_seqno,"
            + "a.bonus_type,"
            + "a.net_bonus as last_month_bonus";
  daoTable  = "mkt_bonus_hst a,cyc_acmm_"+ getValue("wday.stmt_cycle")+" b ";
  whereStr  = "WHERE  a.acct_month = ? "
            + "and    a.p_seqno    = b.p_seqno "
            ;

  setString(1 , getValue("wday.last_acct_month"));

  int  n = loadTable();

  setLoadData("bonu.p_seqno,bonu.bonus_type");

  showLogMessage("I","","Load mkt_bonus_hst : ["+n+"]");
 }
// ************************************************************************
 void  selectMktBonusHst() throws Exception
 {
  selectSQL = "last_month_bonus,"
            + "new_add_bonus,"
            + "adjust_bonus,"
            + "use_bonus,"
            + "remove_bonus,"
            + "net_bonus,"
            + "rowid as rowid";
  daoTable  = "mkt_bonus_hst";
  whereStr  = "where acct_month   = ? "  
            ;

  setString(1 , getValue("wday.this_acct_month"));

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    if ( getValueDouble("last_month_bonus")
       + getValueDouble("new_add_bonus")
       + getValueDouble("adjust_bonus")
       + getValueDouble("use_bonus")
       + getValueDouble("remove_bonus")
       - getValueDouble("net_bonus") ==0) continue;

    totalCnt++;
    updateMktBonusHst1();
   }
  closeCursor();
  return;
 }
// ************************************************************************
 int updateMktBonusHst() throws Exception
 {
  updateSQL = "new_add_bonus     = new_add_bonus + ?,"
            + "adjust_bonus      = adjust_bonus  + ?,"
            + "use_bonus         = use_bonus     + ?,"
            + "give_bonus        = give_bonus    + ?,"
            + "remove_bonus      = remove_bonus  + ?,"
            + "net_bonus         = net_bonus     + ? ";
  daoTable  = "mkt_bonus_hst";
  whereStr  = "WHERE acct_month = ? "
            + "and   p_seqno    = ? "
            + "and   bonus_type  = ? "
            ;

  setDouble(1 , newAddBonus);
  setDouble(2 , adjustBonus); 
  setDouble(3 , useBonus); 
  setDouble(4 , giveBonus); 
  setDouble(5 , removeBonus);
  setDouble(6 , netBonus);
  setString(7 , getValue("wday.this_acct_month"));
  setString(8 , getValue("p_seqno"));
  setString(9 , getValue("bonus_type"));


  updateTable();
  if (notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktBonusHst1() throws Exception
 {
  updateSQL = "diff_bonus        = (last_month_bonus"
            + "                 + new_add_bonus"
            + "                 + adjust_bonus"
            + "                 + use_bonus"
            + "                 + give_bonus"
            + "                 + remove_bonus"
            + "                 - net_bonus)*-1 ";
  daoTable  = "mkt_bonus_hst";
  whereStr  = "WHERE rowid      = ? "
            ;

  setRowId(1 , getValue("rowid"));

  updateTable();
  if (notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 int updateMktBonusHst2() throws Exception
 {
  updateSQL = "first_bonus       = first_bonus   + ?,"
            + "second_bonus      = second_bonus  + ?,"
            + "last_6_bonus      = last_6_bonus  + ?,"
            + "last_12_bonus     = last_12_bonus + ?";
  daoTable  = "mkt_bonus_hst";
  whereStr  = "WHERE acct_month = ? "
            + "and   p_seqno    = ? "
            + "and   bonus_type  = ? "
            ;

  setDouble(1 , last1Bonus);
  setDouble(2 , last2Bonus); 
  setDouble(3 , last6Bonus); 
  setDouble(4 , last12Bonus); 
  setString(5 , getValue("wday.this_acct_month"));
  setString(6 , getValue("p_seqno"));
  setString(7 , getValue("bonus_type"));


  updateTable();
  if (notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************


}  // End of class FetchSample
