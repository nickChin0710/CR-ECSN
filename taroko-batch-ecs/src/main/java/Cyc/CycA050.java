/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/20  V1.00.25  Allen Ho   cyc_a130                                   *
* 110/09/29  V1.01.02  Allen Ho   Matis 8819 fund_flag set error             *
* 110/11/17  V1.01.03  Brian      Matis 9063 class_code='B', payment_type='CD01' 以「循環利息」呈現*
* 111-10-17  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-05-14  V1.00.04  Simon      1.add missed select-bil_bill.curr_adjust_amt、dc_curr_adjust_amt*
*                                 2.add select condition:cyc_pyaj.p_seqno != ''*
* 112-06-21  V1.00.05  Simon      1.取消 "繳款明細 :"、"小  計"              *
*                                 2.取消 "帳務調整明細 :"、"小  計"          *
* 112-09-25  V1.00.06  Simon      新增寫入abem.txn_code="24"(負餘額調整轉出)、*
*                                 "27"(繳款沖正類)
* 112-10-06  V1.00.07  Simon      調整txn-21 為繳款沖正類(payment_reversal)或調整金額合計為正項金額，*
*                                 txn-27 為溢付款轉出                        *
* 112-10-13  V1.00.08  Simon      專案基金選擇抵用餘額撥入溢付款之帳單帳項明細檔的*
*                                 print_type 由 "03" 更改為 "06"             *
* 112-11-01  V1.00.09  Simon      1.帳單繳款、調整、現金回饋抵扣等款項新增顯示入帳日期*
*                                 2.帳單繳款款項說明移除繳款日期及已繳款金額字眼"          *
* 112-11-08  V1.00.10  Simon      select a.crt_date changed to max(a.crt_date) in selectCycPyaj02()*
* 112-11-17  V1.00.11  Simon      tcb 要求帳單調整交易不要合計顯示而需逐筆顯示*
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycA050 extends AccessDAO
{
 private final String PROGNAME = "關帳-持卡人繳款及調整資料處理程式 "
                               + "112-11-17  V1.00.11";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String   businessDate  = "";
 String   pSeqno            = "";

 boolean DEBUG = false;

 int    totalCnt=0,insertCnt=0,updateCnt=0,billCnt=0;
 int[] intCnt={0,0,0};
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA050 proc = new CycA050();
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
  
   if ( args.length >= 1 ) 
      { businessDate = args[0]; }
   if ( args.length == 2 )
      {
       if (args[1].length()==10)
          pSeqno = args[1];
       else if (args[1].equals("DEBUG"))
          DEBUG = true;
      }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
  
   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

  if ((DEBUG)||(pSeqno.length()!=0))
     {
      showLogMessage("I","","=========================================");
      showLogMessage("I","","DEBUG MODE 清檔開始...");
      showLogMessage("I","","還原 bil_bill");
      totalCnt=0;
      updateBilBillDebug();
      showLogMessage("I","","total_count=["+totalCnt+"]");
      showLogMessage("I","","還原 cyc_pyaj");
      totalCnt=0;
      updateCycPyajDebug();
      showLogMessage("I","","total_count=["+totalCnt+"]");
      showLogMessage("I","","刪除 cyc_abem");
      totalCnt=0;
      deleteCycAbemDebug();
      showLogMessage("I","","total_count=["+totalCnt+"]");
      showLogMessage("I","","DEBUG MODE 清檔完成...");
      showLogMessage("I","","=========================================");
     }
   
   loadPtrPayment();
   loadVmktFundName();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理對帳單繳款說明...");
   selectCycPyaj02();
 //selectCycAbem02HT();
   showLogMessage("I","","處理 cyc_pyaj 開始...");
   selectCycPyaj();
   showLogMessage("I","","處理筆數["+totalCnt+"] ABEM 新增筆數["
                         +insertCnt+"] BILL 更新筆數["+billCnt+"]");
   showLogMessage("I","","  print_type 02 處理筆數["+intCnt[0]+"]");
   showLogMessage("I","","  print_type 03 處理筆數["+intCnt[1]+"]");
   showLogMessage("I","","  print_type 06 處理筆數["+intCnt[2]+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理 cyc_abem 排序開始...");
   totalCnt=0;
   updateCnt=0;
  
 //selectCycAbem03X3(); tcb 要求帳單調整交易不要合計顯示而需逐筆顯示
 //selectCycAbem03Y3();
   showLogMessage("I","","挑選 處理筆數["+totalCnt+"] DE06 更新筆數["+updateCnt+"]");

   showLogMessage("I","","=========================================");
   deleteCycAbem0203DUP();

 //showLogMessage("I","","處理 print_ty03 小計");
 //selectCycAbem03HT();
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

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************
 public int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "PTR_WORKDAY";
  whereStr  = "WHERE this_close_date = ? ";

  setString(1,businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
 
  return(0);
 }
// ************************************************************************
 void  selectCycPyaj() throws Exception
 {
  selectSQL = "a.curr_code,"
            + "b.curr_eng_name,"
            + "b.bill_sort_seq,"
            + "a.class_code,"
            + "a.p_seqno,"
            + "a.acct_type,"
            + "a.payment_date,"
            + "a.crt_date,"
            + "a.payment_amt,"
            + "a.dc_payment_amt,"
            + "a.payment_type,"
            + "a.fund_code,"
            + "reference_no,"
            + "a.rowid as rowid";
  daoTable  = "cyc_pyaj a,ptr_currcode b";
  whereStr  = "WHERE (settle_flag != 'B' "
            + " or    settle_date = ?) "
            + "AND   decode(a.curr_code,'','901',a.curr_code) = b.curr_code "
            + "AND   p_seqno     != '' "
            + "AND   stmt_cycle   = ? "
            ;

  setString(1,businessDate);
  setString(2,getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(3,pSeqno);
     }
  openCursor();
  int cnt1 = 0;
  String t03PSeqno="",t03CurrCode="";
  int t03PrintSeq = 2;

  while( fetchTable() )
   {
    totalCnt++;

    if (getValue("curr_code").length()==0) setValue("curr_code", "901");

    if ((getValue("class_code").equals("P"))&&
        ((getValue("payment_type").equals("REFU"))||
         (getValueDouble("dc_payment_amt")==0)))
       {
        updateCycPyaj();
        continue;
       }

    if ((getValue("class_code").equals("A"))&&(getValueDouble("dc_payment_amt")<0))
       if (selectBilBill()!=0)
          {
           updateBilBill();
           updateCycPyaj();
           continue;
          }

    if ((getValue("payment_type").equals("OP01"))||
        (getValue("payment_type").equals("OP04"))||
        (getValue("payment_type").equals("PR01")))
       {
        updateCycPyaj();
        continue;
       }

    if ((!t03PSeqno.equals(getValue("p_seqno")))||
        (!t03CurrCode.equals(getValue("curr_code")))) t03PrintSeq = 2;


    setValue("abem.curr_code"     , getValue("curr_code"));
    setValue("abem.print_seq"     , "0001");
    setValue("abem.txn_code"      , "");
    setValue("abem.purchase_date" , getValue("payment_date"));
    if (getValue("crt_date").length() != 0) {
      setValue("abem.post_date" , getValue("crt_date"));
    } else {
      setValue("abem.post_date" , getValue("payment_date"));
    }

    if ((getValue("class_code").equals("B"))&&
        (!getValue("payment_type").equals("AI01")))
       {
        setValue("abem.print_seq"     , "0020");
        intCnt[2]++;
        setValue("abem.print_type"        , "06"); 
        setValueDouble("abem.dest_amt"    , getValueDouble("payment_amt")*-1);
        setValueDouble("abem.dc_dest_amt" , getValueDouble("dc_payment_amt")*-1);

      //setValue("abem.description" , "基金折抵");
        setValue("abem.description" , "現金回饋折抵");
        if (getValue("fund_code").length()!=0)
           {
            setValue("abem.reference_no"  , getValue("fund_code"));
            setValue("fund.fund_code",getValue("fund_code"));
            cnt1 = getLoadData("fund.fund_code");
            if (cnt1>0)
               setValue("abem.description" , getValue("fund.fund_name"));
           }
        else
           {
            if (getValue("payment_type").equals("CD01"))
                setValue("abem.description" , "循環利息");
            
            setValue("pymt.payment_type",getValue("payment_type"));
            setValue("pymt.fund_flag"   ,"Y");
            cnt1 = getLoadData("pymt.payment_type,pymt.fund_flag");
            if (cnt1>0)
               setValue("abem.description" , getValue("pymt.bill_desc"));
           }

        insertCycAbem();
       }
    else if (getValue("class_code").equals("A"))   /* 調整 */
       {
        t03PrintSeq++;
      //setValue("abem.print_type"   , "X3"); 
        setValue("abem.print_type"   , "03"); 
        setValue("abem.print_seq"     , String.format("%04d",t03PrintSeq));
        setValue("abem.dest_amt"       , getValue("payment_amt"));
        setValue("abem.dc_dest_amt"    , getValue("dc_payment_amt"));
      //setValue("abem.purchase_date"  ,  "");
        setValue("abem.description"    , "帳務調整");
        setValue("pymt.payment_type",getValue("payment_type"));
        setValue("pymt.fund_flag" , "1");
        cnt1 = getLoadData("pymt.payment_type,pymt.fund_flag");
        if (cnt1>0) setValue("abem.description" , getValue("pymt.bill_desc"));

        intCnt[1]++;
        insertCycAbem();
        t03PSeqno   = getValue("p_seqno");
        t03CurrCode = getValue("curr_code");
/***
        if (getValue("payment_type").equals("DE06")) 
           {
            intCnt[1]++;
            insertCnt++;
            setValue("abem.print_type"  , "X4");
            setValue("abem.print_seq"   , "0002");
            setValue("abem.description" , "信用卡繳付安泰保費或中興保全費用失敗");
            insertCycAbem();
           }
***/
       }
    else if (((getValue("class_code").equals("P"))&&   /* 繳款 */
              (Arrays.asList("OP01","OP02","OP03","OP04","DR11").contains(getValue("payment_type"))))||
             ((getValue("class_code").equals("B"))&&  
              (getValue("payment_type").equals("AI01")))||
             (getValue("fund_code").length()!=0))  
       {   
      //intCnt[2]++;
        intCnt[1]++;
      //setValue("abem.print_type"   , "Y3"); 
      //setValue("abem.reference_no"      , getValue("payment_type")); 
        setValue("abem.reference_no"      , getValue("reference_no")); 
        if  (getValue("fund_code").length()!=0)
            setValue("abem.reference_no"  , getValue("fund_code"));
             
        if (getValue("payment_type").equals("AI01"))
           {
            setValue("abem.dest_amt"    , getValue("payment_amt"));
            setValue("abem.dc_dest_amt" , getValue("dc_payment_amt"));
           }
        else
           {
            setValueDouble("abem.dest_amt"    , getValueDouble("payment_amt")*-1);
            setValueDouble("abem.dc_dest_amt" , getValueDouble("dc_payment_amt")*-1);
           }

        if (getValue("fund_code").length()!=0)
           {
            setValue("abem.print_seq"     , "0010");
            intCnt[2]++;
            setValue("abem.print_type"        , "06"); 
          //setValue("abem.description" , "基金回饋");
            setValue("abem.description" , "現金回饋轉入溢付款");
            setValue("fund.fund_code",getValue("fund_code"));
            cnt1 = getLoadData("fund.fund_code");
            if (cnt1>0)
               setValue("abem.description" , getValue("fund.fund_name"));
           }
        else
           {
            if (getValue("payment_type").equals("OP01"))
               setValue("abem.description" , "本期帳務溢付款圈存");
            else if (getValue("payment_type").equals("OP02")) {
               setValue("abem.description" , "本期帳務溢付款提領");
               setValue("abem.txn_code" , "27");
            }
            else if (getValue("payment_type").equals("OP03")) {
               setValue("abem.description" , "本期帳務溢付款轉帳");
               setValue("abem.txn_code" , "27");
            }
            else if (getValue("payment_type").equals("OP04"))
               setValue("abem.description" , "本期帳務溢付款解圈");
            else if (getValue("payment_type").equals("AI01"))
               setValue("abem.description" , "本期帳務帳外息調整");
            else if (getValue("payment_type").equals("DR11")) {
               setValue("abem.description" , "本期帳務繳款沖帳還原");
               setValue("abem.txn_code" , "21");
            }
            else
               setValue("abem.description" , "本期帳務交易調整");

            t03PrintSeq++;
            setValue("abem.print_type"   , "03"); 
            setValue("abem.print_seq"     , String.format("%04d",t03PrintSeq));
            setValue("pymt.payment_type",getValue("payment_type"));
            setValue("pymt.fund_flag"   ,"1");
            cnt1 = getLoadData("pymt.payment_type,pymt.fund_flag");
            if (cnt1>0)
               setValue("abem.description" , getValue("pymt.bill_desc"));
            t03PSeqno   = getValue("p_seqno");
            t03CurrCode = getValue("curr_code");
           }
        insertCycAbem();
       }

    updateCycPyaj();
   }
  closeCursor();
 }
// ************************************************************************
 void  selectCycPyaj02() throws Exception
 {
  selectSQL = "a.curr_code,"
            + "max(b.bill_sort_seq) as bill_sort_seq,"
            + "a.p_seqno,"
            + "max(a.acct_type) as acct_type,"
            + "a.payment_date,"
            + "max(a.crt_date) as crt_date,"
            + "sum(a.payment_amt) as payment_amt,"
            + "sum(a.dc_payment_amt) as dc_payment_amt,"
            + "a.payment_type";
  daoTable  = "cyc_pyaj a,ptr_currcode b";
  whereStr  = "WHERE (settle_flag != 'B' "
            + " or    settle_date = ?) "
            + "AND   decode(a.curr_code,'','901',a.curr_code) = b.curr_code "
            + "AND   class_code not in ('A','B') " 
            + "AND   payment_type not in ('OP01','OP04','PR01','REFU','AI01') "
            + "AND   payment_type not in ('OP02','OP03','DR11') "
            + "AND   fund_code = '' "
            + "AND   dc_payment_amt != 0 "
            + "AND   p_seqno        != '' "
            + "AND   stmt_cycle      = ? "
            ;

  setString(1,businessDate);
  setString(2,getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(3,pSeqno);
     }
  whereStr  = whereStr  
            + "group by p_seqno,a.curr_code,payment_date,payment_type "
            + "order by p_seqno,a.curr_code,payment_date,payment_type ";

  openCursor();

  String pSeqno="",currCode="";
  int print_seq = 1;

  while( fetchTable() )
   {
    totalCnt++;

    if (getValue("curr_code").length()==0) setValue("curr_code", "901");

    if ((!pSeqno.equals(getValue("p_seqno")))||
        (!currCode.equals(getValue("curr_code")))) print_seq = 1;

    print_seq++;
    setValue("abem.curr_code"     , getValue("curr_code"));
    setValue("abem.print_seq"     , String.format("%04d",print_seq));
    setValue("abem.purchase_date" , getValue("payment_date"));
  //setValue("abem.post_date" , getValue("crt_date"));
    if (getValue("crt_date").length() != 0) {
      setValue("abem.post_date" , getValue("crt_date"));
    } else {
      setValue("abem.post_date" , getValue("payment_date"));
    }

    setValueDouble("abem.dest_amt"    , getValueDouble("payment_amt")*-1);
    setValueDouble("abem.dc_dest_amt" , getValueDouble("dc_payment_amt")*-1);

    setValue("pymt.payment_type",getValue("payment_type"));
    setValue("pymt.fund_flag" , "N");
    int cnt1 = getLoadData("pymt.payment_type,pymt.fund_flag");

    if (cnt1==0) setValue("pymt.bill_desc" , "一般繳款");
/***
    setValue("abem.description"       , comm.toChinDate(getValue("payment_date"))
                                      + "已繳款金額-"
                                      + getValue("pymt.bill_desc"));
***/
    setValue("abem.description"       , getValue("pymt.bill_desc"));

  //setValue("abem.txn_code"          , "P");
    setValue("abem.txn_code"          , "20");
    intCnt[0]++;
    setValue("abem.print_type"        , "02");
    insertCycAbem();                /* P 繳款金額 */

    pSeqno   = getValue("p_seqno");
    currCode = getValue("curr_code");
   }
  closeCursor();
 }
//************************************************************************
/***
 void  selectCycAbem02HT() throws Exception
 {
  selectSQL = "curr_code,"
            + "p_seqno,"
            + "max(bill_sort_seq) as bill_sort_seq,"
            + "max(acct_type) as acct_type,"
            + "count(*) as pay_cnt,"
            + "sum(dest_amt) as dest_amt,"
            + "sum(dc_dest_amt) as dc_dest_amt";
  daoTable  = "cyc_abem_" + getValue("wday.stmt_cycle");
  whereStr  = "WHERE print_type = '02' "
            + "and   dummy_code not in ('S','Y') "
            + "group by p_seqno,curr_code "
            ;

  openCursor();

  while( fetchTable() )
   {
    totalCnt++;
    if (getValueInt("pay_cnt") > 1 )
       {
        setValue("abem.print_type"     , "02");
        setValue("abem.p_seqno"        ,  pSeqno);
        setValue("abem.curr_code"      ,  getValue("curr_code"));
        setValue("abem.acct_type"      ,  getValue("acct_type"));
        setValue("abem.dest_amt"       ,  getValue("dest_amt"));
        setValue("abem.dc_dest_amt"    ,  getValue("dc_dest_amt"));
        setValue("abem.print_seq"      ,  "9999");
        setValue("abem.description"    ,  "小  計");
        setValue("abem.dummy_code"     ,  "S");
        setValue("abem.bill_sort_seq"  ,  "1");
        insertCycAbem("S");

        setValue("abem.print_type"     , "02");
        setValue("abem.curr_code"      ,  getValue("curr_code"));
        setValue("abem.dest_amt"       ,  "0");
        setValue("abem.dc_dest_amt"    ,  "0");
        setValue("abem.acct_type"      ,  getValue("acct_type"));
        setValue("abem.print_seq"      ,  "0001");
        setValue("abem.description"    ,  "繳款明細 :");
        setValue("abem.dummy_code"     ,  "Y");
        insertCycAbem("Y");
       }
   }
  closeCursor();
 }
***/
// ************************************************************************
 void updateCycPyaj() throws Exception
 {
  updateSQL = "settle_flag  = 'B',"
            + "settle_date  = ?";
  daoTable  = "cyc_pyaj";
  whereStr  = "WHERE rowid = ? ";

  setString(1 , businessDate);
  setRowId(2 , getValue("rowid"));

  int recCnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","update_cyc_pyaj error!" );
      showLogMessage("I","","rowid=["+getValue("rowid")+"]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 int  selectBilBill() throws Exception
 {
  extendField = "bill.";
  selectSQL = "billed_flag,"
            + "billed_date,"
            + "curr_adjust_amt,"
            + "dc_curr_adjust_amt,"
            + "rowid as rowid";
  daoTable  = "bil_bill";
  whereStr  = "WHERE reference_no = ? ";

  setString(1, getValue("reference_no"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(0);

  if ((getValue("bill.billed_flag").equals("B"))&& 
      (!getValue("bill.billed_date").equals(businessDate))) return(0);

  if (getValueDouble("bill.curr_adjust_amt")+
      getValueDouble("bill.dc_curr_adjust_amt")!=0)
     {
      showLogMessage("I","","Change curr_adjust_amt    [" + getValueDouble("payment_amt") + "]");
      showLogMessage("I","","       dc_curr_adjust_amt [" + getValueDouble("dc_payment_amt") + "]");
     }

  return(1);
 }
// ************************************************************************
 void updateBilBill() throws Exception
 {
  updateSQL = "curr_adjust_amt    = curr_adjust_amt + ?,"
            + "dc_curr_adjust_amt = dc_curr_adjust_amt + ?,"
            + "mod_pgm            = ? , "
            + "mod_time           = to_date(? ,'yyyymmdd') ";
  daoTable  = "bil_bill";
  whereStr  = "WHERE rowid = ? ";

  setDouble(1 , getValueDouble("payment_amt"));
  setDouble(2 , getValueDouble("dc_payment_amt"));
  setString(3 , javaProgram);
  setString(4 , businessDate);
  setRowId(5  , getValue("bill.rowid"));

  int recCnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","update_bil_bill error!" );
      showLogMessage("I","","rowid=["+getValue("bill.rowid")+"]");
      exitProgram(1);
     }
  billCnt++;
  return;
 }
// ************************************************************************
 void insertCycAbem() throws Exception
 {
  setValue("abem.acct_type"     ,  getValue("acct_type"));
  setValue("abem.currency_code" , getValue("curr_code"));
  insertCycAbem("",getValue("p_seqno"));
 }
// ************************************************************************
 void insertCycAbem(String dummyCode) throws Exception
 {
  setValue("abem.p_seqno"       ,  getValue("p_seqno"));
  setValue("abem.acct_type"     ,  getValue("acct_type"));
  setValue("abem.currency_code" , getValue("curr_code"));
  insertCycAbem(dummyCode,getValue("p_seqno"));
 }
// ************************************************************************
 void insertCycAbem(String dummyCode,String pSeqno) throws Exception
 {
  dateTime();
  extendField = "abem.";
  setValue("abem.p_seqno"       ,  pSeqno);
  setValue("abem.bill_sort_seq" ,  getValue("bill_sort_seq"));
  setValue("abem.dummy_code"    ,  dummyCode);
  if (!getValue("abem.print_type").substring(0,1).equals("Y"))
     setValue("abem.reference_no"  ,  "");
  if (getValue("abem.print_type").equals("06"))
     {
      setValue("abem.dummy_code"   ,  "A");
      setValue("abem.reference_no" ,  getValue("reference_no"));
     }

  setValue("abem.source_amt"    , getValue("abem.dc_dest_amt"));

  if (getValue("curr_code").equals("901"))
     {
      setValue("abem.currency_code" ,  "TWD");
      setValue("abem.source_amt"    , getValue("abem.dest_amt")); 
     }

/***
  if (!getValue("print_type").equals("02"))
      setValue("abem.txn_code"      , "");
***/
  setValue("abem.mod_time"      , sysDate+sysTime);
  setValue("abem.crt_pgm"       , javaProgram);
  setValue("abem.mod_pgm"       , javaProgram);
  setValue("abem.mod_seqno"     , comr.getSeqno("ECS_MODSEQ"));

  daoTable = "cyc_abem_"+getValue("wday.stmt_cycle");

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_cyc_abem error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 void  selectCycAbem03X3() throws Exception
 {
  selectSQL = "curr_code,"
            + "p_seqno,"
            + "print_type,"
            + "max(bill_sort_seq) as bill_sort_seq,"
            + "max(acct_type) as acct_type,"
            + "max(purchase_date) as purchase_date,"
            + "max(post_date) as post_date,"
            + "sum(dest_amt) as dest_amt,"
            + "sum(dc_dest_amt) as dc_dest_amt";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE substr(print_type,1,1) = 'X' "
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(1,pSeqno);
     }
  whereStr  = whereStr  
            + "GROUP BY curr_code,p_seqno,print_type "
            + "ORDER BY curr_code,p_seqno,print_type";

  openCursor();

  while( fetchTable() )
   {
    setValue("abem.curr_code"     ,  getValue("curr_code"));
    setValue("abem.dest_amt"      ,  getValue("dest_amt"));
    setValue("abem.dc_dest_amt"   ,  getValue("dc_dest_amt"));


    setValue("abem.print_type"    , "03");
    setValue("abem.txn_code" , "");
  //setValue("abem.purchase_date" , "");
    setValue("abem.purchase_date" , getValue("purchase_date"));
    setValue("abem.post_date" , getValue("post_date"));

    totalCnt++;
    int int1=0;
    if (getValue("print_type").equals("X3")) 
       {
        setValue("abem.print_seq"   , "0002");
        setValue("abem.description" , "本期帳務調整金額");
       }
/***
    if (getValue("print_type").equals("X4")) 
       {
        updateCnt++;
        setValue("abem.description" , "信用卡繳付安泰保費或中興保全費用失敗");
        setValue("abem.print_seq"   , "0003");
        updateCycAbem();
       }
***/
    insertCycAbem();
   }
  closeCursor();
 }
// ************************************************************************
 void  selectCycAbem03Y3() throws Exception
 {
  selectSQL = "curr_code,"
            + "p_seqno,"
            + "reference_no,"
            + "max(purchase_date) as purchase_date,"
            + "max(post_date) as post_date,"
            + "max(bill_sort_seq) as bill_sort_seq,"
            + "max(acct_type) as acct_type,"
            + "max(description) as description,"
            + "sum(dest_amt) as dest_amt,"
            + "sum(dc_dest_amt) as dc_dest_amt";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE substr(print_type,1,1) = 'Y' "
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(1,pSeqno);
     }
  whereStr  = whereStr  
            + "GROUP BY p_seqno,curr_code,reference_no "
            + "ORDER BY p_seqno,curr_code,reference_no";

  openCursor();

  int printSeq=2;
  String pSeqno="";
  String currCode = "";
  while( fetchTable() )
   {
    if ((!pSeqno.equals(getValue("p_seqno")))||
        (!currCode.equals(getValue("curr_code")))) 
       {
        printSeq=2;
       }

    printSeq++;
    setValue("abem.curr_code"     ,  getValue("curr_code"));
    setValue("abem.dest_amt"      ,  getValue("dest_amt"));
    setValue("abem.dc_dest_amt"   ,  getValue("dc_dest_amt"));
    setValue("abem.description"   ,  getValue("description"));


    setValue("abem.print_type"    , "03");

    setValue("abem.print_seq"   , String.format("%04d" , printSeq));
    setValue("abem.txn_code"    , "");
    setValue("abem.purchase_date" , getValue("purchase_date"));
    setValue("abem.post_date" , getValue("post_date"));

    insertCycAbem();
    pSeqno   = getValue("p_seqno");
    currCode = getValue("curr_code");
   }
  closeCursor();
 }
// ************************************************************************
 void  selectCycAbem03HT() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "count(*) as acct_cnt,"
            + "max(acct_type) as acct_type,"
            + "sum(dest_amt) as dest_amt,"
            + "sum(dc_dest_amt) as dc_dest_amt";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE  print_type = '03' "
            + "and dummy_code not in ('Y','S') "
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(1,pSeqno);
     }
  whereStr  = whereStr  
            + "GROUP BY p_seqno,curr_code "
            ;

  openCursor();

  while( fetchTable() )
   {
    if (getValueInt("acct_cnt")>1)
       {
        setValue("abem.print_type"    , "03");
        setValue("abem.p_seqno"       ,  getValue("p_seqno"));
        setValue("abem.curr_code"     ,  getValue("curr_code"));
        setValue("abem.dest_amt"      ,  "0");
        setValue("abem.dc_dest_amt"   ,  "0");
        setValue("abem.acct_type"     ,  getValue("acct_type"));
        setValue("abem.print_seq"     ,  "0001");
        setValue("abem.description"   ,  "帳務調整明細 :");
        setValue("abem.dummy_code"    ,  "Y");
  
        if (updateCycAbem4()!=0) insertCycAbem("Y");
  
        setValue("abem.print_type"    , "03");
        setValue("abem.p_seqno"       ,  getValue("p_seqno"));
        setValue("abem.curr_code"     ,  getValue("curr_code"));
        setValue("abem.acct_type"     ,  getValue("acct_type"));
        setValue("abem.dest_amt"      ,  getValue("dest_amt"));
        setValue("abem.dc_dest_amt"   ,  getValue("dc_dest_amt"));
        setValue("abem.print_seq"     ,  "9999");
        setValue("abem.description"   ,  "小  計");
        setValue("abem.dummy_code"    ,  "S");
        setValue("abem.bill_sort_seq" ,  "1");

        if (updateCycAbem3()!=0) insertCycAbem("S");
       }
   }
  closeCursor();
 }
// ************************************************************************
 void updateCycAbem() throws Exception
 {
  updateSQL = "dest_amt    = dest_amt - ?,"
            + "dc_dest_amt = dc_dest_amt - ?";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE p_seqno    = ? "
            + "AND   print_type = '03' "
            + "AND   curr_code  = ? "
            + "AND   mod_pgm    = ? ";

  setDouble(1 , getValueDouble("abem.dest_amt"));
  setDouble(2 , getValueDouble("abem.dc_dest_amt"));
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("curr_code"));
  setString(5 , javaProgram);

  int recCnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","update_cyc_abem error!" );
      showLogMessage("I","","p_seqno=["+getValue("p_seqno")+"]");
      exitProgram(1);
     }
 }
// ************************************************************************
 int updateCycAbem3() throws Exception
 {
  updateSQL = "dest_amt    = ?,"
            + "dc_dest_amt = ?";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE p_seqno    = ? "
            + "AND   print_type = '03' "
            + "AND   curr_code  = ? "
            + "AND   dummy_code = 'S' "
            ;

  setDouble(1 , getValueDouble("abem.dest_amt"));
  setDouble(2 , getValueDouble("abem.dest_amt"));
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("curr_code"));

  int recCnt = updateTable();
  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int updateCycAbem4() throws Exception
 {
  updateSQL = "dest_amt    = ?,"
            + "dc_dest_amt = ?";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE p_seqno    = ? "
            + "AND   print_type = '03' "
            + "AND   curr_code  = ? "
            + "AND   dummy_code = 'Y' "
            ;

  setDouble(1 , getValueDouble("abem.dest_amt"));
  setDouble(2 , getValueDouble("abem.dest_amt"));
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("curr_code"));

  int recCnt = updateTable();
  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 void  selectCycAbem1() throws Exception
 {
  selectSQL = "curr_code,"
            + "p_seqno,"
            + "max(purchase_date) as purchase_date,"
            + "max(mod_seqno) as mod_seqno,"
            + "sum(source_amt) as source_amt,"
            + "sum(dest_amt) as dest_amt,"
            + "sum(dc_dest_amt) as dc_dest_amt";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE print_type = '02' "
            + "AND   mod_pgm    = ? "
            ;

  setString(1 , javaProgram);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(2,pSeqno);
     }
  whereStr  = whereStr  
            + "GROUP BY curr_code,p_seqno "
            + "HAVING count(*) > 1";

  openCursor();

  while( fetchTable() )
   {
    totalCnt++;
    deleteCycAbem1();
    updateCycAbem1();
   }
  closeCursor();
 }
// ************************************************************************
 void updateCycAbem1() throws Exception
 {
  updateSQL = "purchase_date = ?,"
            + "source_amt    = ?,"
            + "dest_amt      = ?,"
            + "dc_dest_amt   = ?";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE mod_seqno = ? ";

  setString(1 , getValue("purchase_date"));
  setDouble(2 , getValueDouble("source_amt"));
  setDouble(3 , getValueDouble("dest_amt"));
  setDouble(4 , getValueDouble("dc_dest_amt"));
  setDouble(5 , getValueDouble("mod_seqno"));

  int recCnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","update_cyc_abem_1 error!" );
      showLogMessage("I","","mod_seqno=["+getValueDouble("mod_seqno")+"]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 void deleteCycAbem1() throws Exception
 {
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE mod_seqno != ?  "
            + "AND   p_seqno    = ? "
            + "AND   curr_code  = ? "
            + "AND   print_type = '02' "
            + "AND   mod_pgm    = ?";

  setDouble(1 , getValueDouble("mod_seqno"));
  setString(2 , getValue("p_seqno"));
  setString(3 , getValue("curr_code"));
  setString(4 , javaProgram);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(5,pSeqno);
     }

  deleteTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","delete_cyc_abem_1 error!" );
      showLogMessage("I","","p_seqno=["+getValue("p_seqno")+"]");
      exitProgram(1);
     }
 }
// ************************************************************************
 void deleteCycAbem0203DUP() throws Exception
 {
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE print_type like 'X%' "
            + " or   print_type like 'Y%' "
            ; 

  deleteTable();
 }
// ************************************************************************
 void loadPtrPayment() throws Exception
 {
  extendField = "pymt.";
  selectSQL = "payment_type,"
            + "fund_flag,"
            + "chi_name,"
            + "bill_desc";
  daoTable  = "ptr_payment";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("pymt.payment_type,pymt.fund_flag");

  showLogMessage("I","","Load ptr_payment Count: ["+n+"]");
 }
// ************************************************************************
 void loadVmktFundName() throws Exception
 {
  extendField = "fund.";
  selectSQL = "fund_code,"
            + "fund_name";
  daoTable  = "vmkt_fund_name";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("fund.fund_code");

  showLogMessage("I","","Load vmkt_fund_name Count: ["+n+"]");
 }
// ************************************************************************
 void updateCycPyajDebug() throws Exception
 {
  updateSQL = "settle_flag  = 'U',"
            + "settle_date  = ''";
  daoTable  = "cyc_pyaj";
  whereStr  = "WHERE stmt_cycle  = ? "
            + "AND   settle_date = ? "
            ;
  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , businessDate);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(3,pSeqno);
     }
  int recCnt = updateTable();

  showLogMessage("I","","total_count=["+ recCnt +"]");
 }
// ************************************************************************
 void updateBilBillDebug() throws Exception
 {
  updateSQL = "curr_adjust_amt    = 0,"
            + "dc_curr_adjust_amt = 0 ";
  daoTable  = "bil_bill";
  whereStr  = "WHERE billed_flag != 'B' "
            + "and reference_no in "
            + "    (select reference_no "
            + "     from cyc_pyaj "
            + "     WHERE stmt_cycle  = ? "
            + "     AND   settle_date = ? "
            + "     AND   class_code  = 'A' "
            + "     AND   dc_payment_amt < 0) "
            ;
  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , businessDate);
  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(3,pSeqno);
     }
  int recCnt = updateTable();

  showLogMessage("I","","total_count=["+ recCnt +"]");
 }
// ************************************************************************
 void deleteCycAbemDebug() throws Exception
 {
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle"); 
  whereStr  = "WHERE (print_type like 'X%' "
            + " or    print_type in ('02','03','06')) "
            + "AND   mod_pgm    = ? "
            ;

  setString(1 , javaProgram);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "and  p_seqno = ? "; 
      setString(2,pSeqno);
     }
  int recCnt = deleteTable();

  showLogMessage("I","","total_count=["+ recCnt +"]");
 }
// ************************************************************************


}  // End of class FetchSample
