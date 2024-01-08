/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/03/22  V1.00.24  Allen Ho   cyc_a220                                   *
* 111-10-18  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-02-28  V1.00.04  Simon      remove vrsk_ctrlseqno_bil.prbl_clo_result  *
* 112-07-06  V1.00.05  Simon      1.TCB爭議款帳項說明為 原交易特店名稱+"暫緩繳納" *
*                                 2.TCB帳單爭議款只出示未結案的              *
* 112-07-13  V1.00.06  Simon      loadVrskCtrlseqnoBil() setString(x,x)修正  *
* 112-08-21  V1.00.07  Simon      取消檢核rsk_problem.add_apr_date、close_apr_date(as rsk_other3_mark)*
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA420 extends AccessDAO
{
 private final String PROGNAME = "關帳-挑選消費爭議資料處理程式 112-08-21  V1.00.07";
 CommFunction comm = new CommFunction();

 String businessDate   = "";

 long    totalCnt=0,updateCnt=0;

 boolean DEBUG = false;

 int wsPrintSeq=0;
 int[] dataCnt = {0,0,0};
 int cnt1=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA420 proc = new CycA420();
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
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [DEBUG]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   if ( args.length == 2 )
      { if (args[1].toUpperCase().equals("DEBUG")) DEBUG=true; }
   
   if ( !connectDataBase() ) 
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","this_acct_month["+getValue("this_acct_month")+"]");
   showLogMessage("I","","=========================================");
   loadVrskCtrlseqnoBil();

   if (DEBUG)
      {
       showLogMessage("I","","DEBUG MODE 清檔開始...");
       deleteCycProblemDebug();
       deleteCycAbemDebug();
       showLogMessage("I","","DEBUG MODE 清檔完成");
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadPtrCurrcode();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","挑選消費爭議資料");
   selectBilBill();

   showLogMessage("I","","累計筆數 : ["+totalCnt+"]");
   showLogMessage("I","","    表頭筆數 : ["+dataCnt[0]+"]");
   showLogMessage("I","","    資料筆數 : ["+dataCnt[1]+"]");
   showLogMessage("I","","    說明筆數 : ["+dataCnt[2]+"]");
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

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 int  selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  selectBilBill() throws Exception
 {
  selectSQL = "";
  daoTable  = "bil_bill";
  whereStr  = "where stmt_cycle = ? "
            + "and   rsk_post = 'O' "
            + "and   rsk_ctrl_seqno !='' "
            + "ORDER BY p_seqno,card_no,purchase_date ASC "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  openCursor();

  String wsPSeqno = "";
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    setValue("post.reference_no",getValue("reference_no"));

    cnt1 = getLoadData("post.reference_no");
    if (cnt1<=0) continue;

    setValue("rsk_problem1_mark" , getValue("post.rsk_problem1_mark"));
    setValue("rsk_chgback_mark"  , getValue("post.rsk_chgback_mark")); 
    setValue("rsk_receipt_mark"  , getValue("post.rsk_receipt_mark")); 
    setValue("rsk_other3_mark"   , getValue("post.rsk_other3_mark")); 

    setValue("curr.curr_code",getValue("curr_code"));
    cnt1 = getLoadData("curr.curr_code");
    if (cnt1>0)
       {
        setValue("curr_eng_name"      , getValue("curr.curr_eng_name"));
        setValue("abem.bill_sort_seq" , getValue("curr.bill_sort_seq"));
       }

    setValue("src_curr_eng_name" , "");
    setValue("curr.curr_code",getValue("source_curr"));
    cnt1 = getLoadData("curr.curr_code");
    if (cnt1>0) setValue("src_curr_eng_name" , getValue("curr.curr_eng_name"));

    if (getValue("rsk_problem1_mark").length()==0) setValue("rsk_problem1_mark" ,"00000");
    if (getValue("rsk_other3_mark").length()==0) setValue("rsk_other3_mark" ,"00000000");
/*
  showLogMessage("I","","STEP 2: ["+business_date+"]");
  showLogMessage("I","","STEP 2:1["+ getValue("rsk_problem1_mark") +"]");
  showLogMessage("I","","STEP 2:2["+ getValue("rsk_problem1_mark") +"]");
  showLogMessage("I","","STEP 2:3["+ getValue("rsk_other3_mark") +"]");
  showLogMessage("I","","STEP 2:4["+ getValue("wday.last_close_date") +"]");
*/

/*
    if ((getValue("rsk_problem1_mark").substring(1,3).equals("80"))&&
        ((getValue("rsk_problem1_mark").length()<=3)&&
         (getValue("rsk_other3_mark").compareTo(getValue("wday.last_close_date"))<=0))) continue;

    if (getValue("rsk_other3_mark").compareTo(getValue("wday.this_close_date"))>0) continue;
*/

    if (getValue("bill_type").equals("NCVP"))           /* 卡繳稅隱碼 */
       setValue("mcht_chi_name",checkCardtaxId(getValue("mcht_chi_name")));

    if (!getValue("p_seqno").equals(wsPSeqno))
       {
        wsPrintSeq = 0;
      //setValue("abem.description","您所反映的爭議款項,已由專人受理,目前進度如下");
      //insertCycAbem(0);
       }
    // ********Process transaction***************************************
    wsPrintSeq++;

    if (getValue("source_curr").equals("901"))
       {
        setValue("abem.exchange_date", "");
      //setValue("abem.description"  , getValue("mcht_chi_name"));
       }
    else
       {
        setValue("abem.exchange_date", getValue("process_date"));
      //setValue("abem.description"  , getValue("mcht_eng_name"));
       }

    if (getValue("mcht_chi_name").length() > 0)
       {
        setValue("abem.description"  , getValue("mcht_chi_name")+"暫緩繳納");
       }
    else
       {
        setValue("abem.description"  , getValue("mcht_eng_name")+"暫緩繳納");
       }


    setValueDouble("abem.source_amt", 0);
    if ((getValueDouble("problem_amt")==getValueDouble("dest_amt"))&&
        (getValueDouble("problem_amt")!=0)) 
       setValueInt("abem.source_amt",  getValueInt("source_amt"));    
                                                                      
    if (getValue("curr_code").equals("901"))                          
       {
        setValueDouble("abem.problem_amt" , getValueDouble("problem_amt"));
       }
    else
       {
        setValueDouble("abem.problem_amt" , Math.round(getValueDouble("problem_amt")
                                          * getValueDouble("dc_dest_amt")
                                      //* / getValueDouble("dc_dest_amt")+0.000001));
                                          / getValueDouble("dest_amt")+0.000001));
       }
    insertCycAbem(1);
    // ****************************************************************

    // ******Process status *******************************************
/***
    wsPrintSeq++;
    setValue("abem.description"  , "洽收單行處理中,款項暫緩計收");
     
    if (((getValue("rsk_chgback_mark").length()==0)||
         (getValue("rsk_chgback_mark").substring(0,3).compareTo("130")<0))&&
        (getValue("rsk_receipt_mark").length()>=2)&&
        (getValue("rsk_receipt_mark").substring(0,2).compareTo("30")>=0))
       setValue("abem.description"  , "調單中,款項暫緩計收"); 


    if (getValue("rsk_problem1_mark").substring(1,3).equals("80"))
       {
        setValue("abem.description"  , "");

        if (getValue("rsk_problem1_mark").length()>=5)
        if (((getValue("rsk_problem1_mark").substring(3,5).compareTo("11")>=0)&&
             (getValue("rsk_problem1_mark").substring(3,5).compareTo("17")<=0))||
            (getValue("rsk_problem1_mark").substring(3,5).compareTo("19")==0)||
            ((getValue("rsk_problem1_mark").substring(3,5).compareTo("61")>=0)&&
             (getValue("rsk_problem1_mark").substring(3,5).compareTo("63")<=0))||
            ((getValue("rsk_problem1_mark").substring(3,5).compareTo("65")>=0)&&
             (getValue("rsk_problem1_mark").substring(3,5).compareTo("67")<=0)))
            setValue("abem.description"  , "款項由本行墊付,您不需付款"); 


        if (getValue("rsk_problem1_mark").length()>=5)
        if ((getValue("rsk_problem1_mark").substring(3,5).compareTo("18")==0)||
            (getValue("rsk_problem1_mark").substring(3,5).compareTo("20")==0))
            setValue("abem.description"  , "已向商店索回款項抵付本問題帳款"); 

        if (getValue("rsk_problem1_mark").length()>=5)
        if (((getValue("rsk_problem1_mark").substring(3,5).compareTo("31")>=0)&&
             (getValue("rsk_problem1_mark").substring(3,5).compareTo("34")<=0))||
            ((getValue("rsk_problem1_mark").substring(3,5).compareTo("71")>=0)&&
             (getValue("rsk_problem1_mark").substring(3,5).compareTo("73")<=0)))
            setValue("abem.description"  , "經調查結果,請您支付本息");
             
        if (getValue("rsk_problem1_mark").length()>=5)
        if (((getValue("rsk_problem1_mark").substring(3,5).compareTo("41")>=0)&&
             (getValue("rsk_problem1_mark").substring(3,5).compareTo("44")<=0))||
            ((getValue("rsk_problem1_mark").substring(3,5).compareTo("81")>=0)&&
             (getValue("rsk_problem1_mark").substring(3,5).compareTo("83")<=0)))
            setValue("abem.description"  , "經調查結果,請您支付本金,利息免計");

        if (getValue("rsk_problem1_mark").length()>=5)
        if (getValue("rsk_problem1_mark").substring(3,5).compareTo("21")==0)
           setValue("abem.description" , "商店退款已抵付您的信用卡帳款,詳帳單");
       }

    insertCycAbem(2);
***/
    /******************************************************************/
    insertCycProblem();

    wsPSeqno = getValue("p_seqno");
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int deleteCycProblemDebug() throws Exception
 {
  daoTable  = "cyc_problem";
  whereStr  = "WHERE billed_date = ? "
            + "and   mod_pgm     = ? ";

  setString(1  , businessDate);
  setString(2  , javaProgram);

  int recCnt = deleteTable();

  showLogMessage("I","","Delete cyc_problem [" + recCnt +"]");
  return(0);
 }
// ************************************************************************
 int deleteCycAbemDebug() throws Exception
 {
  daoTable  = "cyc_abem_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE print_type = '08'  "
            + "and   mod_pgm    = ? ";

  setString(1  , javaProgram);

  int recCnt = deleteTable();

  showLogMessage("I","","Delete cyc_abem_"+getValue("wday.stmt_cycle") +" [" + recCnt +"]");
  return(0);
 }
// ************************************************************************
 int insertCycProblem() throws Exception
 {
  dateTime();

  setValue("exchange_date"      , getValue("abem.exchange_date"));
  setValue("bill_sort_seq"      , getValue("abem.bill_sort_seq"));
  setValue("billed_date"        , businessDate);
  setValue("stmt_cycle"         , getValue("wday.stmt_cycle"));
  setValue("mod_time"           , sysDate+sysTime);
  setValue("mod_pgm"            , javaProgram);
  
  daoTable  = "cyc_problem";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertCycAbem(int hInt) throws Exception
 {
  dateTime();
  extendField = "abem.";
  dataCnt[hInt]++;

  setValue("abem.p_seqno"            , getValue("p_seqno"));
  setValue("abem.curr_code"          , getValue("curr_code"));
  setValue("abem.acct_type"          , getValue("acct_type"));
  setValue("abem.print_type"         , "08");
  setValue("abem.print_seq"          , String.format("%04d",wsPrintSeq));
  setValue("abem.card_no"            , getValue("card_no"));

  setValue("abem.purchase_date"      , "");
  setValue("abem.post_date"          , "");
  setValue("abem.interest_date"      , "");
  setValue("abem.area_code"          , "");
  setValue("abem.currency_code"      , "");
  setValue("abem.dummy_code"         , "Y");
  setValue("abem.acct_code"          , "");
  setValue("abem.dest_amt"           , "0");
  setValue("abem.dc_dest_amt"        , "0");
   
  if (hInt==1)
     {
      setValue("abem.purchase_date"      , getValue("purchase_date"));
      setValue("abem.post_date"          , getValue("post_date"));
      setValue("abem.interest_date"      , getValue("interest_date"));
      int  maxlen=getValue("mcht_city").length();
      if (getValue("mcht_city").length()>8) maxlen=8;
      if (getValue("mcht_city").length()!=0)
         setValue("abem.area_code"        , getValue("mcht_city").substring(0,maxlen));
      else
         setValue("abem.area_code"        , "");
      setValue("abem.exchange_date"      , getValue("abem.exchange_date"));
      setValue("abem.currency_code"      , getValue("src_curr_eng_name"));
      setValue("abem.source_amt"         , getValue("abem.source_amt"));

      setValue("abem.dummy_code"         , "");
      setValue("abem.acct_code"          , getValue("acct_code"));
      setValue("abem.dest_amt"           , getValue("abem.problem_amt"));
      setValue("abem.dc_dest_amt"        , getValue("abem.problem_amt"));
      if (getValueDouble("abem.problem_amt")==0)
         {
          setValue("abem.dest_amt"        , getValue("dest_amt"));
          setValue("abem.dc_dest_amt"     , getValue("dc_dest_amt"));
         }
     }
  else
     {
      setValue("abem.source_amt"         , "0");
       setValue("abem.exchange_date"      , "");
     }

  setValue("abem.description"             , getValue("abem.description"));
  setValue("abem.reference_no"            , getValue("reference_no"));
  setValue("abem.mod_time"                , sysDate+sysTime);
  setValue("abem.crt_pgm"                 , javaProgram);
  setValue("abem.mod_pgm"                 , javaProgram);

  daoTable  = "cyc_abem_"+getValue("wday.stmt_cycle");

  insertTable();

  return(0);
 }
// ************************************************************************
 void  loadPtrCurrcode() throws Exception
 {
  extendField = "curr.";
  selectSQL = "curr_code,"
            + "curr_eng_name,"
            + "bill_sort_seq";
  daoTable  = "ptr_currcode";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("curr.curr_code");

  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************
 String checkCardtaxId(String input)
 {
  int charlen,int1a,int1b;
  showLogMessage("I","","bef  ["+input+"]");

  for (int1a=0;int1a<input.length();int1a++)
    {
     if ((input.charAt(int1a)<'A')||(input.charAt(int1a)>'Z')) continue;
     for (int1b=int1a+2;int1b<int1a+10;int1b++)
       if ((input.charAt(int1b)<'0')||(input.charAt(int1b)>'9')) break;
     if (int1b<int1a+10) continue;
     input = input.substring(0,int1a+4)+"XXXX"+input.substring(int1a+8,input.length());
     break;
   }
  showLogMessage("I","","aft  ["+input+"]");
 return input;
 }                                                                
// ************************************************************************                                                                 
 void  loadVrskCtrlseqnoBil() throws Exception
 {
  extendField = "post.";
  selectSQL = "reference_no," 
          //+ "nvl(prbl_mark||prbl_status||prbl_clo_result,'') as rsk_problem1_mark,"
            + "nvl(prbl_mark||prbl_status,'') as rsk_problem1_mark,"
            + "nvl(chgb_stage1||chgb_stage2,'') as rsk_chgback_mark,"
            + "nvl(rept_status,'') as rsk_receipt_mark,"
            + "nvl(other3_mark,'') as rsk_other3_mark";
  daoTable  = "vrsk_ctrlseqno_bil";
  whereStr  = "where reference_no in ( "
            + "      select reference_no "
            + "      from bil_bill       "
            + "      where stmt_cycle = ? "
            + "      and   rsk_post = 'O') "
            + "and   prbl_status >= '30' "
          //+ "and   (prbl_status <  '80' "  
          //+ " or    (prbl_status =  '80' "  
          //+ "  and   other3_mark > ? "  
          //+ "  and   other3_mark <= ? )) "  
            + "and   prbl_status <  '80' "  
            ;

  setString(1 , getValue("wday.stmt_cycle"));
//setString(2 , getValue("wday.last_close_date"));
//setString(3 , businessDate);

  showLogMessage("I","","結案日期起>: [" + getValue("wday.last_close_date") +"]");
  showLogMessage("I","","       迄  : [" + businessDate +"]");

  int  n = loadTable();
  setLoadData("post.reference_no");

  showLogMessage("I","","Load vrsk_ctrlseqno_bil Count: ["+n+"]");
 }
// ************************************************************************


}  // End of class FetchSample


