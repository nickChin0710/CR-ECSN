/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/03/12  V1.00.11  Allen Ho   cyc_a138 PROD compare OK                   *
* 111-10-17  V1.00.03  Machao    sync from mega & updated for project coding standard                                                                           *
******************************************************************************/
package Cyc;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycA060 extends AccessDAO
{
 private final String PROGNAME = "關帳-計算利息及違約金前置處理程式 111-10-17  V1.00.03";
 CommFunction comm = new CommFunction();
CommRoutine comr = null;

// String[] mainTables = {"ACT_DEBT_INTR","BIL_CONTRACT_INTR"};
 String[] mainTables = {"ACT_DEBT_INTR"};
 String hBusiBusinessDate    = "";

 long   totalCnt;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA060 proc = new CycA060();
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
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
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

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 開始...");
   loadBilMerchant();
   loadPtrCurrcode();
   loadPtrActcode();
   loadPtrActgeneralN();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","清檔 開始...");
   for (int inti=0;inti<mainTables.length;inti++)
       {
        commitDataBase();
        truncateTable(inti,getValue("wday.stmt_cycle"));
       }
   showLogMessage("I","","------------------------");
   showLogMessage("I","","  新增 act_debt_intr");
   selectActDebt();
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
  extendField = "wday.";
  selectSQL = "stmt_cycle,"
            + "this_acct_month";
  daoTable  = "PTR_WORKDAY";
  whereStr  = "WHERE THIS_CLOSE_DATE = ? ";
  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 public void  selectActDebt() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.curr_code,"
            + "a.acct_code,"
            + "a.acct_type,"
            + "a.reference_no,"
            + "a.acct_month,"
            + "a.purchase_date,"
            + "a.post_date,"
            + "a.end_bal,"
            + "a.dc_end_bal,"
            + "a.card_no,"
            + "a.interest_date,"
            + "a.interest_rs_date,"
            + "a.ao_flag,"
            + "a.int_rate_flag,"
            + "a.int_rate,"
            + "a.new_it_flag,"
            + "a.installment_kind,"
            + "a.contract_no,"
            + "a.contract_seq_no,"
            + "a.mcht_no,"
            + "a.ptr_merchant_no";
  daoTable  = "act_debt a,cyc_acmm_"+getValue("wday.stmt_cycle")+" b";
  whereStr  = "WHERE a.acct_code  != 'DP' "
            + "AND   (a.end_bal+a.dc_end_bal > 0 "
            + " or    (a.acct_month = ?  "
            + "  and   a.acct_code in ('BL','ID','IT','CA','OT','AO') )) "
//          + "  AND   a.acct_code in (select acct_code from ptr_actcode where interest_method = 'Y') )) "
            + "AND   a.p_seqno     =  b.p_seqno ";

  setString(1 , getValue("wday.this_acct_month"));
  openCursor();

  int cnt1=0;
  while( fetchTable() )
    {
     totalCnt++;

     setValue("mcht.mcht_no",getValue("mcht_no"));
     cnt1 = getLoadData("mcht.mcht_no");
     if (cnt1==0) setValueInt("mp_rate",0);
     else  setValueInt("mp_rate",getValueInt("mcht.mp_rate"));

     setValue("angl.acct_type",getValue("acct_type"));
     cnt1 = getLoadData("angl.acct_type");
     if (cnt1==0) 
        {
         showLogMessage("I","","01 acct_type  : ["+getValue("p_seqno")+"]");
         continue;
        }

     setValue("mp_1_bl_flag" , getValue("angl.mp_1_bl_flag"));
     setValue("mp_1_id_flag" , getValue("angl.mp_1_id_flag")); 
     setValue("mp_1_ca_flag" , getValue("angl.mp_1_ca_flag")); 
     setValue("mp_1_ao_flag" , getValue("angl.mp_1_ao_flag")); 
     setValue("mp_1_ot_flag" , getValue("angl.mp_1_ot_flag")); 

     setValue("curr.curr_code",getValue("curr_code"));
     cnt1 = getLoadData("curr.curr_code");
     if (cnt1==0)
        {
         showLogMessage("I","","02 curr_code  : ["+getValue("p_seqno")+"]");
         continue;
        }

     setValue("bill_sort_seq" , getValue("curr.bill_sort_seq"));

     setValue("acde.acct_code",getValue("acct_code"));
     cnt1 = getLoadData("acde.acct_code");
     if (cnt1==0) 
        {
         showLogMessage("I","","03 acct_code  : ["+getValue("p_seqno")+"]");
         continue;
        }

     setValue("inter_rate_code" , getValue("acde.inter_rate_code"));
     setValue("inter_rate_code2" , getValue("acde.inter_rate_code2")); 
     setValue("interest_method" , getValue("acde.interest_method"));
      
     processDisplay(200000);

     insertActDebtIntr();

/* CycA070 new initial 20181123
     if ((getValue("acct_code").equals("IT"))&&
         (getValue("acct_month").equals(getValue("wday.this_acct_month")))&&
         (!getValue("int_rate_flag").equals("Y")))
        {
         select_bil_contract();
        }
*/
    }
  closeCursor();
 }
// ************************************************************************
 int selectBilContract() throws Exception
 {
  extendField = "cont.";
  selectSQL = "install_tot_term,"
            + "install_curr_term,"
            + "int_rate,"
            + "int_rate_flag";
  daoTable  = "bil_contract";
  whereStr  = "WHERE contract_no     = ? "
            + "AND   contract_seq_no = ? "
            ;

  setString(1 , getValue("contract_no"));
  setString(2 , getValue("contract_seq_no"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  insertBilContractIntr();

  return(0);
 }
// ************************************************************************
 public void  insertActDebtIntr() throws Exception
 {
  dateTime();
  if (getValue("CURR_CODE").length()==0) 
      setValue("curr_code"  , "901");

  setValueInt("curr_seq"      , 9);
  setValue("acct_code_flag" , "N");
  if (getValue("ACCT_CODE").equals("BL"))
     {
      setValueInt("curr_seq"      , 0);
      if (getValue("mp_1_bl_flag").equals("Y"))
         setValue("acct_code_flag" , "Y");
     }
  else if (getValue("ACCT_CODE").equals("AO"))
     {
      setValueInt("curr_seq"      , 1);
      if (getValue("mp_1_ao_flag").equals("Y"))
         setValue("acct_code_flag" , "Y");
     }
  else if (getValue("ACCT_CODE").equals("OT"))
     {
      setValueInt("curr_seq"      , 2);
      if (getValue("mp_1_ot_flag").equals("Y"))
         setValue("acct_code_flag" , "Y");
     }
  else if (getValue("ACCT_CODE").equals("CA"))
     {
      setValueInt("curr_seq"      , 3);
      if (getValue("mp_1_ca_flag").equals("Y"))
         setValue("acct_code_flag" , "Y");
     }
  else if (getValue("ACCT_CODE").equals("ID"))
     {
      setValueInt("curr_seq"      , 4);
      if (getValue("mp_1_id_flag").equals("Y"))
         setValue("acct_code_flag" , "Y");
     }
  else if (getValue("ACCT_CODE").equals("IT"))
     {
      setValueInt("curr_seq"      , 5);
      setValue("acct_code_flag" , "Y");
     }
  else if (getValue("ACCT_CODE").equals("CB"))
     {
      setValueInt("curr_seq"      , 6);
      setValue("acct_code_flag" , "Y");
     }
  else if (getValue("ACCT_CODE").equals("DB"))
     {
      setValueInt("curr_seq"      , 7);
      setValue("acct_code_flag" , "Y");
     }
  if (getValue("POST_DATE").length()==0) 
      setValue("post_date"      , "00000000");

  if (getValue("interest_date").length()==0) 
      setValue("interest_date"      , "00000000");

  if (getValue("ao_flag").length()==0) 
      setValue("ao_flag"            , "N");

  if (getValue("installment_kind").length()==0) 
      setValue("installment_kind" , "N");

  if (getValue("new_it_flag").length()==0) 
      setValue("new_it_flag" , "N");

  if (getValue("new_it_flag").equals("N"))
      setValueInt("mp_rate",100);

  if (getValue("ptr_merchant_no").length()==0) 
      setValue("ptr_merchant_no"    , getValue("mcht_no"));

  if (!getValue("installment_kind").equals("N"))
     setValue("mcht_no"    , getValue("ptr_merchant_no"));

  daoTable = "act_debt_intr_"+ getValue("wday.stmt_cycle");

  insertTable();

  if ( dupRecord.equals("Y") )
     { 
      showLogMessage("I","","insert_act_debt_intr error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
public void  insertBilContractIntr() throws Exception
 {
  extendField = "intb.";
  setValue("intb.reference_no"       , getValue("reference_no"));
  setValue("intb.install_tot_term"   , getValue("cont.install_tot_term"));
  setValue("intb.install_curr_term"  , getValue("cont.install_curr_term"));   
  setValue("intb.int_rate"           , getValue("cont.int_rate"));   
  setValue("intb.int_rate_flag"      , getValue("cont.int_rate_flag"));   


  daoTable = "bil_contract_intr_"+ getValue("wday.stmt_cycle");

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_bil_contract_intr  error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ***********************************************************************
 void  loadBilMerchant() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "mcht_no,"
            + "mp_rate";
  daoTable  = "bil_merchant";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("mcht.mcht_no");
  showLogMessage("I","","Load bil_merchant Count: ["+n+"]");
 }
// ************************************************************************
 void  loadPtrActcode() throws Exception
 {
  extendField = "acde.";
  selectSQL = "acct_code,"
            + "inter_rate_code,"
            + "inter_rate_code2,"
            + "interest_method";
  daoTable  = "ptr_actcode";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("acde.acct_code");
  showLogMessage("I","","Load ptr_actcode Count: ["+n+"]");
 }
// ************************************************************************
 void  loadPtrCurrcode() throws Exception
 {
  extendField = "curr.";
  selectSQL = "curr_code,"
            + "bill_sort_seq";
  daoTable  = "ptr_currcode";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("curr.curr_code");
  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************
 void  loadPtrActgeneralN() throws Exception
 {
  extendField = "angl.";
  selectSQL = "acct_type,"
            + "mp_1_bl_flag,"
            + "mp_1_id_flag,"
            + "mp_1_ca_flag,"
            + "mp_1_ao_flag,"
            + "mp_1_ot_flag";
  daoTable  = "ptr_actgeneral_n";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("angl.acct_type");
  showLogMessage("I","","Load ptr_actgeneral_n Count: ["+n+"]");
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
