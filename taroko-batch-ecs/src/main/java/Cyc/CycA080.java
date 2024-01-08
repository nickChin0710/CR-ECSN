/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/01/02  V1.00.10  Allen Ho   cyc_a139 PROD compare OK                   *
* 109-12-17   V1.00.11  tanwei      updated for project coding standard      *
******************************************************************************/
package Cyc;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycA080 extends AccessDAO
{
 private  String progname = "關帳-最低應繳金額前置處理程式 109/12/17 V1.00.11";
 CommFunction comm = new CommFunction();
CommRoutine comr = null;

 String hBusiBusinessDate    = "";
 String hWdayStmtCycle       = "";
 String hWdayThisAcctMonth  = "";
 String hTempUpdate  = "20060401";
 String[] mainTables = {"ACT_DEBT_SUM"};

 long   totalCnt;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA080 proc = new CycA080();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+progname);
  
   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() )
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   selectPtrBusinday();
   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","========================");
   showLogMessage("I","","= For CycA150          =");
   showLogMessage("I","","========================");
   showLogMessage("I","","清檔 開始...");
   for (int inti=0;inti<mainTables.length;inti++)
       {
        commitDataBase();
        truncateTable(inti,hWdayStmtCycle);
       }
   showLogMessage("I","","------------------------");
   showLogMessage("I","","篩選 act_debt 開始");
   showLogMessage("I","","  新增 act_debt_SUM from act_debt");
   selectActDebt();
   showLogMessage("I","","  累計新增 " + totalCnt + "筆");
   showLogMessage("I","","------------------------");
   showLogMessage("I","","  新增 act_debt_SUM from act_acct_curr");
   selectActAcctCurr();
   showLogMessage("I","","  累計新增 " + totalCnt + "筆");
   showLogMessage("I","","------------------------");
   showLogMessage("I","","  新增 act_debt_SUM from cyc_acmm_curr");
   selectCycAcmmCurr();
   showLogMessage("I","","  累計新增 " + totalCnt + "筆");
   showLogMessage("I","","========================");
   showLogMessage("I","","更新 act_debt_SUM from act_acct");
   selectActAcct();
   showLogMessage("I","","  累計新增 " + totalCnt + "筆");
   showLogMessage("I","","------------------------");
   showLogMessage("I","","  更新 act_debt_SUM from act_acct_curr");
   selectActAcctCurr1();
   showLogMessage("I","","  累計更新 " + totalCnt + "筆");
   showLogMessage("I","","------------------------");
   showLogMessage("I","","  更新 act_debt_SUM from cyc_acmm_curr");
   selectCycAcmmCurr1();
   showLogMessage("I","","  累計更新 " + totalCnt + "筆");
   showLogMessage("I","","========================");
  
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
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
  selectSQL = "";
  daoTable  = "PTR_WORKDAY";
  whereStr  = "WHERE THIS_CLOSE_DATE = ? ";
  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle      =  getValue("STMT_CYCLE");
  hWdayThisAcctMonth =  getValue("this_acct_month");

  return(0);
 }
// ************************************************************************
 public void  selectActDebt() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "decode(a.curr_code,'','901',a.curr_code) as curr_code,"
            + "max(bill_sort_seq) as bill_sort_seq,"
            + "sum(end_bal) as end_sum_amt,"
            + "sum(decode(acct_code,'BL',end_bal,'CA',end_bal,'IT',end_bal,'CB',end_bal,"
            + "           'DB',end_bal,'ID',end_bal,'AO',end_bal,'OT',end_bal,0)) as end_cap_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'BL',"
            + "        decode(mp_1_bl_flag,'Y',end_bal,0),0)))) as end_1_bl_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'AO',"
            + "        decode(mp_1_ao_flag,'Y',end_bal,0),0)))) as end_1_ao_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'OT',"
            + "        decode(mp_1_ot_flag,'Y',end_bal,0),0)))) as end_1_ot_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'CA',"
            + "        decode(mp_1_ca_flag,'Y',end_bal,0),0)))) as end_1_ca_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'ID',"
            + "        decode(mp_1_id_flag,'Y',end_bal,0),0)))) as end_1_id_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'IT',end_bal,0)))) as end_2_it_amt,"
            + "sum(decode(acct_code,'BL',end_bal,0)) as end_1_3bl_amt,"
            + "sum(decode(acct_code,'AO',end_bal,0)) as end_1_3ao_amt,"
            + "sum(decode(acct_code,'OT',end_bal,0)) as end_1_3ot_amt,"
            + "sum(decode(acct_code,'CA',end_bal,0)) as end_1_3ca_amt,"
            + "sum(decode(acct_code,'ID',end_bal,0)) as end_1_3id_amt,"
            + "sum(decode(acct_code,'IT',end_bal,0)) as end_2_3it_amt,"
            + "sum(decode(acct_code,'CB',end_bal,0)) as end_3cb_amt,"
            + "sum(decode(acct_code,'DB',end_bal,0)) as end_3db_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "    decode(acct_code,'BL',0,'AO',0,'CA',0,'IT',0,'ID',0,'CB',0,"
            + "                           'DB',0,'OT',0,'DP',0,end_bal))) as end_fee_amt,"
            + "sum(dc_end_bal) as dc_end_sum_amt,"
            + "sum(decode(acct_code,'BL',dc_end_bal,'CA',dc_end_bal,'IT',dc_end_bal,'CB',dc_end_bal,"
            + "    'DB',dc_end_bal,'ID',dc_end_bal,'AO',dc_end_bal,'OT',dc_end_bal,0)) as dc_end_cap_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'BL',"
            + "        decode(mp_1_bl_flag,'Y',dc_end_bal,0),0)))) as dc_end_1_bl_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'OT',"
            + "        decode(mp_1_ot_flag,'Y',dc_end_bal,0),0)))) as dc_end_1_ot_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'AO',"
            + "        decode(mp_1_ao_flag,'Y',dc_end_bal,0),0)))) as dc_end_1_ao_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'ID',"
            + "        decode(mp_1_id_flag,'Y',dc_end_bal,0),0)))) as dc_end_1_id_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'IT',dc_end_bal,0)))) as dc_end_2_it_amt,"
            + "sum(decode(sign(acct_month- ? ),-1,0,"
            + "     decode(sign(purchase_date- ? ),-1,0,"
            + "      decode(acct_code,'CA',"
            + "        decode(mp_1_ca_flag,'Y',dc_end_bal,0),0)))) as dc_end_1_ca_amt,"
            + "sum(decode(acct_code,'BL',dc_end_bal,0)) as dc_end_1_3bl_amt,"
            + "sum(decode(acct_code,'AO',dc_end_bal,0)) as dc_end_1_3ao_amt,"
            + "sum(decode(acct_code,'OT',dc_end_bal,0)) as dc_end_1_3ot_amt,"
            + "sum(decode(acct_code,'CA',dc_end_bal,0)) as dc_end_1_3ca_amt,"
            + "sum(decode(acct_code,'ID',dc_end_bal,0)) as dc_end_1_3id_amt,"
            + "sum(decode(acct_code,'IT',dc_end_bal,0)) as dc_end_2_3it_amt,"
            + "sum(decode(acct_code,'CB',dc_end_bal,0)) as dc_end_3cb_amt,"
            + "sum(decode(acct_code,'DB',dc_end_bal,0)) as dc_end_3db_amt,"
            + "sum(decode(sign(nvl(acct_month,'200407')- ? ),-1,0,"
            + "    decode(acct_code,'BL',0,'AO',0,'CA',0,'IT',0,'ID',0,'CB',0,"
            + "                           'DB',0,'OT',0,'DP',0,dc_end_bal))) as dc_end_fee_amt";
  daoTable  = "act_debt a,ptr_currcode b,ptr_actgeneral_n c,cyc_acmm_"+hWdayStmtCycle + " d";
  whereStr  = "WHERE  dc_end_bal > 0 "
            + "AND    a.curr_code  = b.curr_code "
            + "AND    a.acct_type  = c.acct_type "
            + "AND    a.p_seqno    = d.p_seqno "
            + "GROUP BY a.p_seqno,"
            + "decode(a.curr_code,'','901',a.curr_code)";
  
  setString(1 , hWdayThisAcctMonth);
  setString(2 , hTempUpdate);
  setString(3 , hWdayThisAcctMonth);
  setString(4 , hTempUpdate);
  setString(5 , hWdayThisAcctMonth);
  setString(6 , hTempUpdate);
  setString(7 , hWdayThisAcctMonth);
  setString(8 , hTempUpdate);
  setString(9 , hWdayThisAcctMonth);
  setString(10, hTempUpdate);
  setString(11, hWdayThisAcctMonth);
  setString(12, hTempUpdate);
  setString(13, hWdayThisAcctMonth);
  setString(14, hWdayThisAcctMonth);
  setString(15, hTempUpdate);
  setString(16, hWdayThisAcctMonth);
  setString(17, hTempUpdate);
  setString(18, hWdayThisAcctMonth);
  setString(19, hTempUpdate);
  setString(20, hWdayThisAcctMonth);
  setString(21, hTempUpdate);
  setString(22, hWdayThisAcctMonth);
  setString(23, hTempUpdate);
  setString(24, hWdayThisAcctMonth);
  setString(25, hTempUpdate);
  setString(26, hWdayThisAcctMonth);

  openCursor();

  totalCnt =0;
  int cnt1 =0;
  while( fetchTable() )
    {
     totalCnt++;

     insertActDebtSum();
    }
  closeCursor();
  initActDebtSum();
 }
// ************************************************************************
 public void  selectActAcctCurr() throws Exception
 {
  selectSQL = "p_seqno,"
            + "decode(curr_code,'','901',curr_code) as curr_code,"
            + "bill_sort_seq,"
            + "mp_1_amt,"
            + "mp_1_s_month,"
            + "mp_1_e_month,"
            + "autopay_indicator,"
            + "(end_bal_op + end_bal_lk) as end_bal_op,"
            + "(dc_end_bal_op + dc_end_bal_lk) as dc_end_bal_op ";
  daoTable  = "act_acct_curr_"+hWdayStmtCycle; 
  whereStr  = "WHERE bill_sort_seq !='' "
            + "and dc_end_bal_op + dc_end_bal_lk > 0 "
            ;

  openCursor();

  totalCnt =0;
  while( fetchTable() )
    {
     totalCnt++;
     insertActDebtSum();
    }
  closeCursor();
  initActDebtSum();
 }
// ************************************************************************
 void  selectCycAcmmCurr() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "bill_sort_seq,"
            + "interest_amt,"
            + "dc_interest_amt,"
            + "minimum_pay_bal,"
            + "this_ttl_amt,"
            + "dc_minimum_pay_bal,"
            + "dc_this_ttl_amt";
  daoTable  = "cyc_acmm_curr_"+hWdayStmtCycle;
  whereStr  = "WHERE this_ttl_amt+interest_amt>0 ";

  openCursor();

  totalCnt =0;
  while( fetchTable() )
    {
     totalCnt++;
     insertActDebtSum();
    }
  closeCursor();
  initActDebtSum();
 }
// ************************************************************************
 public void  selectActAcct() throws Exception
 {
  selectSQL = "p_seqno,"
            + "adi_end_bal";
  daoTable  = "act_acct_"+hWdayStmtCycle;
  whereStr  = "WHERE adi_end_bal > 0"; 

  openCursor();

  totalCnt =0;
  while( fetchTable() )
    {
     totalCnt++;
     if (updateActDebtSum1()!=0)
        {
         initActDebtSum();
         setValue("curr_code"     , "901");
         setValue("bill_sort_seq" , "1");
         insertActDebtSum();
        }
    }
  closeCursor();
  initActDebtSum();
 }
// ************************************************************************
 void  selectActAcctCurr1() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "bill_sort_seq,"
            + "mp_1_amt,"
            + "mp_1_s_month,"
            + "mp_1_e_month,"
            + "autopay_indicator,"
            + "(end_bal_op + end_bal_lk) as end_bal_op,"
            + "(dc_end_bal_op + dc_end_bal_lk) as dc_end_bal_op ";
  daoTable  = "act_acct_curr_"+hWdayStmtCycle; 
  whereStr  = "WHERE bill_sort_seq !='' "
            ;

  openCursor();

  totalCnt =0;
  while( fetchTable() )
    {
     totalCnt++;
     updateActDebtSum2();
    }
  closeCursor();
  initActDebtSum();
 }
// ************************************************************************
 void  selectCycAcmmCurr1() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "bill_sort_seq,"
            + "interest_amt,"
            + "dc_interest_amt,"
            + "minimum_pay_bal,"
            + "this_ttl_amt,"
            + "dc_minimum_pay_bal,"
            + "dc_this_ttl_amt";
  daoTable  = "cyc_acmm_curr_"+hWdayStmtCycle;
  whereStr  = "";

  openCursor();

  totalCnt =0;
  while( fetchTable() )
    {
     updateActDebtSum3();
    }
  closeCursor();
  initActDebtSum();
 }
// ************************************************************************
 int updateActDebtSum1() throws Exception
 {
  updateSQL = "adi_end_bal = ? ";
  daoTable  = "act_debt_sum_"+hWdayStmtCycle; 
  whereStr  = "WHERE p_seqno = ? "
            + "AND   curr_code = '901'";

  setDouble(1 , getValueDouble("adi_end_bal") );
  setString(2 , getValue("p_seqno") );

  int cnt = updateTable();

  if ( notFound.equals("Y") )
    {
//   showLogMessage("I","","update_act_debt_sum_ error[dupRecord]");
//   showLogMessage("I","","p+seqno = ["+getValue("p_seqno")+"]");
     return(1);
    }
  return(0);
 }
// ************************************************************************
public int updateActDebtSum2() throws Exception
 {
  updateSQL = "mp_1_amt = ?,"
            + "mp_1_s_month = ?,"      
            + "mp_1_e_month = ?,"      
            + "autopay_indicator = ?,"      
            + "end_bal_op = ?,"      
            + "dc_end_bal_op = ?";     
  daoTable  = "act_debt_sum_"+hWdayStmtCycle; 
  whereStr  = "WHERE p_seqno   = ? "
            + "AND   curr_code = ?";

  setDouble(1 , getValueDouble("mp_1_amt"));
  setString(2 , getValue("mp_1_s_month"));
  setString(3 , getValue("mp_1_e_month"));
  setString(4 , getValue("autopay_indicator"));
  setDouble(5 , getValueDouble("end_bal_op"));
  setDouble(6 , getValueDouble("dc_end_bal_op"));
  setString(7 , getValue("p_seqno") );
  setString(8 , getValue("curr_code") );

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
public int updateActDebtSum3() throws Exception
 {
  updateSQL = "interest_amt       = ?,"
            + "dc_interest_amt    = ?," 
            + "minimum_pay_bal    = ?," 
            + "this_ttl_amt       = ?," 
            + "dc_minimum_pay_bal = ?," 
            + "dc_this_ttl_amt    = ?"; 
  daoTable  = "act_debt_sum_"+hWdayStmtCycle; 
  whereStr  = "WHERE p_seqno   = ? "
            + "AND   curr_code = ? ";

  setDouble(1 , getValueDouble("interest_amt"));
  setDouble(2 , getValueDouble("dc_interest_amt"));
  setDouble(3 , getValueDouble("minimum_pay_bal"));
  setDouble(4 , getValueDouble("this_ttl_amt"));
  setDouble(5 , getValueDouble("dc_minimum_pay_bal"));
  setDouble(6 , getValueDouble("dc_this_ttl_amt"));
  setString(7 , getValue("p_seqno") );
  setString(8 , getValue("curr_code") );

  int cnt = updateTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int  insertActDebtSum() throws Exception
 {
  dateTime();
  setValue("MOD_TIME"           , sysDate+sysTime);
  setValue("MOD_PGM"            , javaProgram);

  daoTable = "act_debt_sum_"+hWdayStmtCycle; 

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 public void initActDebtSum() throws Exception
 {
  setValueDouble("end_sum_amt"      , 0);
  setValueDouble("end_cap_amt"      , 0);
  setValueDouble("end_1_bl_amt"     , 0);
  setValueDouble("end_1_ao_amt"     , 0);
  setValueDouble("end_1_ot_amt"     , 0);
  setValueDouble("end_1_ca_amt"     , 0);
  setValueDouble("end_1_id_amt"     , 0);
  setValueDouble("end_2_it_amt"     , 0);
  setValueDouble("end_1_3bl_amt"    , 0);
  setValueDouble("end_1_3ao_amt"    , 0);
  setValueDouble("end_1_3ot_amt"    , 0);
  setValueDouble("end_1_3ca_amt"    , 0);
  setValueDouble("end_1_3id_amt"    , 0);
  setValueDouble("end_2_3it_amt"    , 0);
  setValueDouble("end_3cb_amt"      , 0);
  setValueDouble("end_3db_amt"      , 0);
  setValueDouble("end_fee_amt"      , 0);
  setValueDouble("dc_end_sum_amt"   , 0);
  setValueDouble("dc_end_cap_amt"   , 0);
  setValueDouble("dc_end_1_bl_amt"  , 0);
  setValueDouble("dc_end_1_ao_amt"  , 0);
  setValueDouble("dc_end_1_ot_amt"  , 0);
  setValueDouble("dc_end_1_ca_amt"  , 0);
  setValueDouble("dc_end_1_id_amt"  , 0);
  setValueDouble("dc_end_2_it_amt"  , 0);
  setValueDouble("dc_end_1_3bl_amt" , 0);
  setValueDouble("dc_end_1_3ao_amt" , 0);
  setValueDouble("dc_end_1_3ot_amt" , 0);
  setValueDouble("dc_end_1_3ca_amt" , 0);
  setValueDouble("dc_end_1_3id_amt" , 0);
  setValueDouble("dc_end_2_3it_amt" , 0);
  setValueDouble("dc_end_3cb_amt"   , 0);
  setValueDouble("dc_end_3db_amt"   , 0);
  setValueDouble("dc_end_fee_amt"   , 0);

  setValueDouble("mp_1_amt"           , 0);
  setValueDouble("minimum_pay_bal"    , 0);
  setValueDouble("this_ttl_amt"       , 0);
  setValueDouble("interest_amt"       , 0);
  setValueDouble("end_bal_op"         , 0);
  setValueDouble("dc_minimum_pay_bal" , 0);
  setValueDouble("dc_this_ttl_amt"    , 0);
  setValueDouble("dc_interest_amt"    , 0);
  setValueDouble("dc_end_bal_op"      , 0);

  setValue("autopay_indicator" , "");
  setValue("mp_1_s_month"      , "");
  setValue("mp_1_e_month"      , "");
}
// ************************************************************************ 
 int truncateTable(int inti,String stmtCycle) throws Exception
 {
  showLogMessage("I","","   刪除 " + mainTables[inti] + "_" + stmtCycle);
  String trunSQL = "TRUNCATE TABLE "+ mainTables[inti] + "_" + stmtCycle + " "
                 + "IGNORE DELETE TRIGGERS "
                 + "DROP STORAGE "
                 + "IMMEDIATE "
                 ;

  executeSqlCommand(trunSQL);

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

