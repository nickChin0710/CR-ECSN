/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/03/05   V1.00.00    Allen Ho    mkt_t120-2                             *
* 109-12-11   V1.00.01    tanwei      updated for project coding standard    *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT135 extends AccessDAO
{
 private  String progname = "高鐵標準車廂-退票退還費用處理程式 109/12/11 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate  = "";
 String tranSeqno     = "";

 long    totalCnt=0;
 int[] procCnt = new int[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT135 proc = new MktT135();
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
   showLogMessage("I","",javaProgram+" "+progname);

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
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrDisc();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","累計                 筆數 ["+totalCnt   +"]");
   showLogMessage("I","","  不處理             筆數 ["+(procCnt[0]+procCnt[1])+"]"); 
   showLogMessage("I","","    頂級卡非關帳日   筆數 ["+procCnt[0]+"]"); 
   showLogMessage("I","","    非扣除費用       筆數 ["+procCnt[1]+"]");
   showLogMessage("I","","  檢核錯誤           筆數 ["+(procCnt[2]+procCnt[3] 
                                                      + procCnt[4]+procCnt[5] 
                                                      + procCnt[6]+procCnt[7])+"]"); 
   showLogMessage("I","","    無購票紀錄       筆數 ["+procCnt[2]+"]"); 
   showLogMessage("I","","    原訂票請款失敗   筆數 ["+procCnt[3]+"]"); 
   showLogMessage("I","","    原訂票已退票     筆數 ["+procCnt[4]+"]"); 
   showLogMessage("I","","    原訂票未扣費用   筆數 ["+procCnt[5]+"]");
   showLogMessage("I","","    請款金額小於退貨 筆數 ["+procCnt[6]+"]");
   showLogMessage("I","","    退票超過訂票金額 筆數 ["+procCnt[7]+"]");
   showLogMessage("I","","    無加檔記錄       筆數 ["+procCnt[8]+"]");
   showLogMessage("I","","    退票金額為 0     筆數 ["+procCnt[9]+"]");
   showLogMessage("I","","  退還費用           筆數 ["+(procCnt[10])+"]"); 
   showLogMessage("I","","=========================================");

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

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************ 
 public void  selectMktThsrDisc() throws Exception
 {
  selectSQL = "p_seqno, "
            + "acct_type, "
            + "card_no, "
            + "orig_trans_date, "
            + "orig_serial_no, "
            + "serial_no,"
            + "file_date,"
            + "card_mode,"
            + "discount_value,"
            + "rowid as rowid ";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE   proc_flag   = '1' "
            + "and     trans_type  = 'R' "
            + "";

  openCursor();
  totalCnt = 0;
  while( fetchTable() ) 
   {
    totalCnt++;
/*
    showLogMessage("I","","card_no   : ["+getValue("card_no")+"]");
    showLogMessage("I","","tran_date : ["+getValue("orig_trans_date")+"]");
    showLogMessage("I","","serial_no : ["+getValue("orig_serial_no")+"]");
*/
    if (getValue("card_mode").equals("1"))
       if (selectPtrWorkday()!=0) 
          {
           procCnt[0]++;    // 頂級卡需在關帳日處理
           continue;
          }

    if (selectMktThsrDisc1()!=0)
       {
        procCnt[2]++;
        setValue("error_code"  , "96");
        setValue("error_desc" , "無購票紀錄, 不處理");
        updateMktThsrDisc();
        continue;
       }

    if (!getValue("dis1.deduct_type").equals("2"))
       {
        procCnt[1]++;   // 非扣除費用
        continue;
       }

    if (getValue("dis1.proc_flag").equals("X"))
       {
        procCnt[2]++;
        setValue("error_code"  , "11");
        setValue("error_desc" , "原訂票請款檢核失敗");
        updateMktThsrDisc();
        continue;
       }

    if (getValue("dis1.proc_flag").equals("0"))
       {
        procCnt[3]++;
        setValue("error_code"  , "12");
        setValue("error_desc" , "原訂票請款未處理");
        updateMktThsrDisc();
        setValue("error_code"  , "X2");
        setValue("error_desc" , "同日退貨");
        updateMktThsrDiscA();
        continue;
       }

    if ((getValue("dis1.refund_flag").equals("Y"))&&
        (getValueInt("discount_value") + getValueInt("dis1.refund_amt") >=
                                         getValueInt("dis1.dest_amt")))
       {
        procCnt[4]++;
        setValue("error_code"  , "14");
        setValue("error_desc" , "原訂票請款已退票");
        updateMktThsrDisc();
        continue;
       }

    if (getValueInt("dis1.dest_amt")==0)
       {
        procCnt[5]++;
        setValue("error_code"  , "16");
        setValue("error_desc" , "原訂票請款扣錢金額小於等於零");
        updateMktThsrDisc();
        continue;
       }
    if (getValueInt("discount_value") > getValueInt("dis1.discount_value"))
       {
        procCnt[6]++;
        setValue("error_code"  , "17");
        setValue("error_desc" , "原訂票請款扣錢金額小於退貨金額");
        updateMktThsrDisc();
        continue;
       }

    if (getValueInt("dis1.refund_amt")+getValueInt("discount_value")>getValueInt("dis1.dest_amt"))
       {
        procCnt[7]++;
        setValue("error_code"  , "24");
        setValue("error_desc" , "退票金額超過訂票金額, 不處理");
        updateMktThsrDisc();
        continue;
       }
    setValue("deduct_type","2");
    setValue("dest_amt" , "0");

    if (selectBilSysexp()==0)
       {
        if (getValueInt("discount_value")> getValueInt("sys1.dest_amt"))
           {
            procCnt[7]++;
            setValue("error_code"  , "25");
            setValue("error_desc" , "退票金額超過訂票金額, 不處理");
            updateMktThsrDisc();
            continue;
           }

        setValueInt("dest_amt", getValueInt("sys1.dest_amt") - getValueInt("discount_value"));
        if (getValueInt("dest_amt")==0)
           {
            deleteBilSysexp();
           }
        else
           {
            updateBilSysexp();
           }
        procCnt[10]++;
       }
    else
       {
//      if (select_bil_bill()!=0)   // only test for debug
        if (selectActDebt()!=0)
           if (selectActDebtHst()!=0)
              {
               procCnt[8]++;
               setValue("error_code"  , "21");
               setValue("error_desc" , "無加檔記錄, 不退還");
               updateMktThsrDisc();
               continue;
              }
        setValueInt("dest_amt",  getValueInt("discount_value"));

        if (getValueInt("dest_amt") > getValueInt("debt.d_avail_bal")
                                    - getValueInt("dis1.refund_amt"))
            setValueInt("dest_amt" ,  getValueInt("debt.d_avail_bal")
                                   - getValueInt("dis1.refund_amt"));

        if (getValueInt("dest_amt")<=0)
           {
            procCnt[9]++;
            setValue("error_code"  , "23");
            setValue("error_desc" , "無退票金額可調整, 不退還");
            updateMktThsrDisc();
            continue;
           }
        procCnt[10]++;
        insertBilSysexp();
       }
    updateMktThsrDisc1();
    updateMktThsrDisc2();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int  selectMktThsrDisc1() throws Exception
 {
  extendField = "dis1.";
  selectSQL = "proc_flag,"
            + "trans_date,"
            + "refund_flag,"
            + "refund_amt,"
            + "deduct_type,"
            + "dest_amt,"
            + "discount_value,"
            + "rowid as rowid";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE  card_no     = ? "
            + "AND    trans_date  = ? "
            + "AND    serial_no   = ? "
            + "";

  setString(1,getValue("card_no"));
  setString(2,getValue("orig_trans_date")); 
  setString(3,getValue("orig_serial_no")); 

  selectTable();

  if ( notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************ 
 void updateMktThsrDisc2() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag        = 'Y',"
            + "error_code       = '00',"
            + "proc_date        = ?," 
            + "deduct_type      = ?,"  
            + "dest_amt         = ?,"  
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("deduct_type"));
  setInt(3    , getValueInt("dest_amt"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktThsrDisc1() throws Exception
 {
  dateTime();
  updateSQL = "refund_serial_no = ?,"
            + "refund_file_date = ?,"
            + "refund_date      = ?,"
            + "refund_flag      = 'Y',"
            + "refund_amt       = refund_amt  + ?,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , getValue("serial_no"));
  setString(2 , getValue("file_date"));
  setString(3 , hBusiBusinessDate);
  setInt(4    , getValueInt("dest_amt"));
  setString(5 , javaProgram);
  setRowId(6  , getValue("dis1.rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "b.next_close_date";
  daoTable  = "act_acno a,ptr_workday b";
  whereStr  = "WHERE  a.stmt_cycle = b.stmt_cycle "
            + "AND    a.p_seqno    = ? "
            ;

  setString(1,getValue("p_seqno"));

  selectTable();

  if ( notFound.equals("Y")) 
     {
      showLogMessage("I","","p_seqno  : ["+getValue("p_seqno")+"] not found in act_acno");
      return(1);
     }

  if ((getValue("wday.next_close_date").equals(sysDate))||
      (getValue("wday.next_close_date").equals(hBusiBusinessDate))) return(0);

  return(1);
 }
// ************************************************************************ 
 void updateMktThsrDisc() throws Exception
 {
  dateTime();
  showLogMessage("I","","card_no : ["+getValue("card_no")+"] - ["+getValue("error_code")+"]- ["+getValue("error_desc")+"]");

  updateSQL = "proc_flag  = 'X', "
            + "proc_date  = ?, "
            + "error_code = ?,"  
            + "error_desc = ?,"  
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid = ?";

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("error_code"));
  setString(3 , getValue("error_desc"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_disc error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void updateMktThsrDiscA() throws Exception
 {
  dateTime();
  showLogMessage("I","","card_no : ["+getValue("card_no")+"] - ["+getValue("error_code")+"]- ["+getValue("error_desc")+"]");

  updateSQL = "proc_flag  = 'X', "
            + "proc_date  = ?, "
            + "error_code = ?,"  
            + "error_desc  = ?,"  
            + "mod_pgm     = ?, "
            + "mod_time    = sysdate";   
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid = ?";

  setString(1 , hBusiBusinessDate);
  setString(3 , getValue("error_code"));
  setString(4 , getValue("error_desc"));
  setString(2 , javaProgram);
  setRowId(5  , getValue("dis1.rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_disc_a error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 int selectBilSysexp() throws Exception
 {
  extendField = "sys1.";
  selectSQL = "dest_amt,"
            + "rowid as rowid";
  daoTable  = "bil_sysexp";
  whereStr  = "where purchase_date  = ? "
            + "and   bill_type      = 'INHR' "
            + "and   txn_code       = '05' "
            + "and   card_no        = ? "
            + "and   ref_key        = ? "
            + "and   post_flag     != 'Y' "
            + "and   curr_code      = '901' ";

  setString(1, getValue("orig_trans_date"));
  setString(2, getValue("card_no"));
  setString(3, getValue("orig_serial_no"));

  selectTable();

  if ( notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 int deleteBilSysexp() throws Exception
 {
  daoTable  = "bil_sysexp";
  whereStr  = "WHERE rowid    = ? ";

  setRowId(1  , getValue("sys1.rowid"));

  deleteTable();

  return(0);
 }
// ************************************************************************
 void updateBilSysexp() throws Exception
 {
  dateTime();
  updateSQL = "dest_amt = ? ";
  daoTable  = "bil_sysexp";
  whereStr  = "WHERE rowid    = ? ";

  setInt(1    , getValueInt("dest_amt"));
  setRowId(2  , getValue("sys1.rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int selectBilBill() throws Exception
 {
  extendField = "debt.";
  selectSQL = "reference_no,"
            + "dest_amt as beg_bal,"
            + "dest_amt as end_bal,"
            + "dest_amt as d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "bil_bill";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHR' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   dest_amt      = ? ";

  setString(1, getValue("p_seqno"));
  setString(2, getValue("card_no"));
  setString(3, getValue("dis1.trans_date"));
  setInt(4   , getValueInt("dis1.dest_amt"));

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 int selectActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "reference_no,"
            + "beg_bal,"
            + "end_bal,"
            + "d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "act_debt";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHR' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   beg_bal       = ? "
            + "and   d_avail_bal   > 0 ";

  setString(1, getValue("p_seqno"));
  setString(2, getValue("card_no"));
  setString(3, getValue("dis1.trans_date"));
  setInt(4   , getValueInt("dis1.dest_amt"));

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 int selectActDebtHst() throws Exception
 {
  extendField = "dest.";
  selectSQL = "reference_no,"
            + "beg_bal,"
            + "end_bal,"
            + "d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "act_debt_hst";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHR' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   beg_bal       = ? "
            + "and   d_avail_bal   > 0 ";

  setString(1, getValue("p_seqno"));
  setString(2, getValue("card_no"));
  setString(3, getValue("dis1.trans_date"));
  setInt(4   , getValueInt("dis1.dest_amt"));

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 int insertBilSysexp() throws Exception
 {
  dateTime();
  extendField = "sysp.";
  setValue("sysp.card_no"            , getValue("card_no"));
  setValue("sysp.p_seqno"            , getValue("p_seqno"));
  setValue("sysp.acct_type"          , getValue("acct_type"));
  setValue("sysp.bill_type"          , "INHR");
  setValue("sysp.txn_code"           , "06");
  setValue("sysp.purchase_date"      , hBusiBusinessDate);
  setValue("sysp.src_amt"            , getValue("dest_amt"));
  setValue("sysp.dest_amt"           , getValue("dest_amt"));
  setValue("sysp.dc_dest_amt"        , getValue("dest_amt"));
  setValue("sysp.dest_curr"          , "901");
  setValue("sysp.curr_code"          , "901");
  setValue("sysp.bill_desc"          , "退還高鐵折扣票價(退貨)");
  setValue("sysp.post_flag"          , "N"); 
  setValue("sysp.ref_key"            , getValue("serial_no")); 
  setValue("sysp.mod_user"           , javaProgram); 
  setValue("sysp.mod_time"           , sysDate+sysTime);
  setValue("sysp.mod_pgm"            , javaProgram);
                     
  daoTable  = "bil_sysexp";

  insertTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample
