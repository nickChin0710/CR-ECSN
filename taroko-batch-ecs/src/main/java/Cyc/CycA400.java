/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/08/04  V1.00.39  Allen Ho   cyc_a210 PROD compare OK                   *
* 110/09/22  V1.01.01  Allen Ho   act_combo_jrnl tran_class='2' cr_amt*-1    *
* 110/10/15  V1.02.03  Allen Ho   Modify proc_fee_detail (mega changed but no notify DXC*
* 111-10-18  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-06-21  V1.00.04  Simon      1.getValue("mcht_chi_name").substring(0,2) "out of range" fixed*
*                                 2.getValue("mcht_city").length() changed into* 
*                                   getValue("mcht_city").getBytes("MS950").length//mcht_city可能有中文*
*                                 3.新增紅利計算本金不回饋註記               *
*                                 4.取消卡號 Title " 本期交易明細"、"其他交易明細 : "、"小  計"*
*                                 5.新增 "本期消費"                          *
* 112-07-06  V1.00.05  Simon      "本期消費" 的卡號欄清空白                  *
* 112-08-14  V1.00.06  Simon      loadCycAbem() 設定 order by card_no (for 現金回饋集中排在費用類前面)*
* 112-10-13  V1.00.07  Simon      bil_bill.payment_type 判斷條件更改         *
* 112-11-01  V1.00.08  Simon      1.不回饋的定義改以 bil_bill.ecs_platform_kind 為依據*
*                                 2.帳單外幣折算日改以 bil_bill.post_date(金資系統)*
*                                   取代bil_bill.process_date(NCCC系統)      *
*                                 3.本期消費排除 帳單分期或其他交易入帳後分期*
* 112-11-16  V1.00.09  Simon      控制 cyc_abem.print_seq 超過 "9999"        *
* 112-11-27  V1.00.10  Simon      新增不回饋交易類別代碼顯示 "$, A, U"       *
******************************************************************************/

package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA400 extends AccessDAO
{
 private final String PROGNAME = "關帳-挑選卡人消費明細及費用資料處理程式 112-11-27 "
                               + "V1.00.10";
 CommFunction comm = new CommFunction();
 CommCrd comc = new CommCrd();

 String businessDate      = "";

 int capTotCnt,capAdjCnt,feeTotCnt,feeAdjCnt,feeAnlCnt;
 int  printSeq=1;
 int  modSeqno=1;
  String tmpstr="",tmpstr1="";

  double coboDestAmt = 0;
  double coboDcDestAmt =0;

 long    totalCnt=0,currCnt=0;
  boolean DEBUG = false;
  String pSeqno="0000619515";
  String hOver9998="";
  boolean DEBUGP = false;
 
 int paymentAmt = 0,insertCnt=0,updateCnt=0,pcodCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA400 proc = new CycA400();
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

   if (args.length > 3)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [DEBUG]");
       return(1);
      }

   if (( args.length >= 1 )&&(args[0].length()==8))
      { businessDate = args[0]; }
   if (( args.length >= 2)&&(args[1].equals("DEBUG")))
      { DEBUG = true; }
   
   if ( !connectDataBase() )
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","this_acct_month["+ getValue("wday.this_acct_month")+"]");

  if (DEBUG)
     {
      showLogMessage("I","","=========================================");
      showLogMessage("I","","DEBUG MODE 清檔開始...");
      deleteCycAbemyc_abem();
      updateBilBillDebug();
      updateActComboJrnlDebug();
      showLogMessage("I","","DEBUG MODE 清檔完成");
      commitDataBase();
     }
   
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadPtrCurrcode();
   loadPtrCombUse();
   loadBilContract();
   loadBilBill();
 //loadBilBill2();
 //loadMktBpidData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","明細資料處理開始...");
   initCycAbem();
   selectBilBill();
   showLogMessage("I","","累計處理筆數["+totalCnt+"]");
   showLogMessage("I","","    本金累計筆數["+capTotCnt+"]"); 
   showLogMessage("I","","    本金調整筆數["+capAdjCnt+"]"); 
   showLogMessage("I","","    費用累計筆數["+feeTotCnt+"]"); 
   showLogMessage("I","","    費用調整筆數["+feeAdjCnt+"]"); 
   showLogMessage("I","","    年費累計筆數["+feeAnlCnt+"]"); 
   showLogMessage("I","","=========================================");
   commitDataBase();

   loadCycAbem();

   totalCnt=0;
 //showLogMessage("I","","表頭表尾處理開始...");
   showLogMessage("I","","print_seq重編開始...");
   selectCycAbem(); 
   showLogMessage("I","","累計處理筆數["+totalCnt+"]"); 
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
  public int  selectPtrWorkday() throws Exception
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
  whereStr  = "where rsk_type not in ( '1','2','3') "
            + "AND   stmt_cycle  = ? "
            + "AND   billed_flag != 'B' ";

  if (DEBUGP) whereStr  = whereStr 
                       + "and   p_seqno= ? ";

  setString(1 , getValue("wday.stmt_cycle"));
  if (DEBUGP) 
     setString(2 , pSeqno);
  openCursor();

  totalCnt=0;

  int cnt1=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    if (totalCnt%50000==0)
       {
        processDisplay(50000); // every 10000 display message
       }

    if ((getValue("acct_code").equals("PF"))&&
        (getValue("bill_type").equals("NCFC"))&&
        (getValue("acct_month").equals(getValue("wday.this_acct_month"))&&
        (getValue("reference_no_original").length()!=0)))
       {
        setValue("bill.reference_no" ,  getValue("reference_no"));
        cnt1 =  getLoadData("bill.reference_no");
        if (cnt1>0)
           {
            updateBilBill();
            continue;
           }
       }

    if (getValue("curr_code").length()==0) setValue("curr_code","901");

  //if ((getValue("payment_type").compareTo("1")>=0)&&
  //    (getValue("payment_type").compareTo("6")<=0))
  //財金格式只有紅利折抵交易(payment_type="1"、"2"會傳入cashPayAmt,且只會是台幣 
  //其他則於 BilA009 會以 dest_amt 寫入 cash_pay_amt 
  //payment_type="3"者應為國際組織現金回饋(以退貨形式入帳)
    if ((getValue("payment_type").compareTo("1")>=0)&&
        (getValue("payment_type").compareTo("2")<=0))
       {
        setValueDouble("dest_amt"    , getValueDouble("cash_pay_amt"));
        setValueDouble("dc_dest_amt" , getValueDouble("cash_pay_amt"));
       }
     else if (getValue("curr_code").equals("901"))
       {
        setValueDouble("dc_dest_amt" , getValueDouble("dest_amt"));
       }

     if (getValue("installment_kind").equals("D"))  setValue("installment_kind","A");
      
     if (!getValue("installment_kind").equals("A")) setValue("installment_kind","");
     else setValue("mcht_no", getValue("ptr_merchant_no")); 
          
     if (getValue("source_curr").equals("TWD")) setValue("source_curr","901");
     if (getValue("source_curr").equals("901")) setValue("process_date","");

     if (getValue("curr_code").equals("901")) 
        setValueDouble("dc_curr_adjust_amt",getValueDouble("curr_adjust_amt"));
      
     if (!getValue("bill_type").equals("OICU"))
        { 
         if (getValue("source_curr").equals("901")) 
            {
             if (getValue("mcht_country").length()>=2)
                {
                 if (getValue("mcht_country").substring(0,2).equals("TW")) 
                    {
                     if (getValue("mcht_chi_name").length()==0) 
                        setValue("mcht_chi_name" , getValue("mcht_eng_name"));
                   //else if (getValue("mcht_chi_name").substring(0,2).equals("　　")) 
                   //   setValue("mcht_chi_name",getValue("mcht_eng_name"));
                     else if (getValue("mcht_chi_name").substring(0,1).equals("　")) 
                        setValue("mcht_chi_name",getValue("mcht_eng_name"));
                    }
                 else
                    {
                     if (!getValue("acct_code").equals("PF"))   // 20211015 PF still chi_NAME
                     if (getValue("mcht_eng_name").length()!=0) 
                        setValue("mcht_chi_name",getValue("mcht_eng_name"));
                    }
                }
             else
                {
                 if (getValue("mcht_eng_name").length()!=0) 
                    setValue("mcht_chi_name",getValue("mcht_eng_name"));
                }
            }
         else if (getValue("mcht_eng_name").length()!=0) 
                 setValue("mcht_chi_name",getValue("mcht_eng_name"));
        }

     setValue("abem.cap_type","1"); 
     if (Arrays.asList("BL","IT","ID","CA","AO","OT").contains(getValue("acct_code"))) 
        setValue("abem.cap_type","2"); 
     else if (!Arrays.asList("RI","PN","LF","AF","CF","AI","SF","PF").contains(getValue("acct_code"))) 
        setValue("abem.cap_type","0"); 

    setValue("rate.curr_code",getValue("curr_code"));
    cnt1 = getLoadData("rate.curr_code");
    if (cnt1!=0) 
       {
        setValue("curr_eng_name"      , getValue("rate.curr_eng_name"));
        setValue("abem.bill_sort_seq" , getValue("rate.bill_sort_seq"));
       }

    setValue("src_curr_eng_name" , "");
    setValue("rate.curr_code",getValue("source_curr"));
    cnt1 = getLoadData("rate.curr_code");
    if (cnt1>0) setValue("src_curr_eng_name" , getValue("rate.curr_eng_name"));

    setValue("abem.p_seqno"             , getValue("p_seqno"));  
    setValue("abem.curr_code"           , getValue("curr_code"));  
    setValue("abem.acct_type"           , getValue("acct_type"));  
    setValue("abem.card_no"             , getValue("card_no"));  
    setValue("abem.reference_no"        , getValue("reference_no"));  
    setValue("abem.txn_code"            , getValue("txn_code"));  
    setValue("abem.purchase_date"       , getValue("purchase_date"));  
    setValue("abem.net_purchase_flag"   , getValue("net_purchase_flag"));  
    setValue("abem.post_date"           , getValue("post_date"));  
    setValue("abem.interest_date"       , getValue("interest_date"));  
    setValue("abem.acct_code"           , getValue("acct_code"));  
    setValue("abem.card_type"           , getValue("card_type"));  
    setValue("abem.principal_noback_flag" , "");  

    setValueDouble("abem.year_fees_rate"      , -1);
    setValueDouble("abem.unbill_it_end_bal"   , -1);

    if (getValue("bill_type").equals("NCVP"))           /* 卡繳稅隱碼 */ 
       setValue("mcht_chi_name",checkCardtaxId(getValue("mcht_chi_name")));

    if (getValue("sign_flag").equals("-"))
       {
        setValueDouble("dest_amt"     , getValueDouble("dest_amt")*-1);
        setValueDouble("dc_dest_amt"  , getValueDouble("dc_dest_amt")*-1);
        setValueDouble("source_amt"   , getValueDouble("source_amt")*-1);
       }
     setValue("abem.source_amt"    , getValue("source_amt"));
     setValue("abem.dest_amt"      , getValue("dest_amt"));
     setValue("abem.dc_dest_amt"   , getValue("dc_dest_amt"));

 if (DEBUGP) showLogMessage("I","","STEP 0 reference_no [" + getValue("reference_no") + "]");

     if (getValue("bill_type").equals("COBO"))
        {
 if (DEBUGP) showLogMessage("I","","STEP 1 [" + getValue("rate.curr_eng_name") + "]");
         coboDestAmt=coboDcDestAmt=0;

         selectActComboJrnl();

         coboDestAmt    =  getValueDouble("dest_amt");
         coboDcDestAmt =  getValueDouble("dc_dest_amt");

         if ((Arrays.asList("BL","CA").contains(getValue("acct_code")))&&
             (getValue("acct_month").equals(getValue("wday.this_acct_month"))))
            {
             if (getValue("reference_no_fee_f").length()!=0)
                {
                 setValue("bill.reference_no" ,  getValue("reference_no_fee_f"));
                 cnt1 =  getLoadData("bill.reference_no");
                 if (cnt1>0) procNewFee("05"); 
                }
             if (getValue("fees_reference_no").length()!=0)
                {
                 setValue("bill.reference_no" ,  getValue("fees_reference_no"));
                 cnt1 =  getLoadData("bill.reference_no");
                 if (cnt1>0) procNewFee("05"); 
                }
            }
         setValue("abem.source_amt"  , "0");
         setValueDouble("abem.dest_amt"    , coboDestAmt);
         setValueDouble("abem.dc_dest_amt" , coboDcDestAmt);

         insertComboDetail();

        }
     else
        {
 if (DEBUGP) showLogMessage("I","","STEP 2 [" + getValue("rate.curr_eng_name") + "]");

         if (((getValue("acct_code").equals("IT"))||
              (getValue("acct_code").equals("PF")))&&
             (getValueInt("install_tot_term")>1))
            {
 if (DEBUGP) showLogMessage("I","","STEP 3 [" + getValue("rate.curr_eng_name") + "]");
             setValue("abem.year_fees_rate"    , "0");
             setValue("abem.unbill_it_end_bal" , "0");
             selectBilContract();
            }

         if (getValue("abem.cap_type").equals("2"))  /* 本金類 */ 
            {
 if (DEBUGP) showLogMessage("I","","STEP 4 [" + getValue("rate.curr_eng_name") + "]");
           
           //setValue("abem.exchange_date" , getValue("process_date"));
             setValue("abem.exchange_date" , getValue("post_date"));
             setValue("abem.currency_code" , getValue("src_curr_eng_name"));
             setValue("abem.dummy_code"    , "");

/***
             setValue("abem.principal_noback_flag" , "*");
             //本金不回饋註記，先預設"*"(沒有紅利回饋)，
               
             setValue("bpid.reference_no" ,  getValue("reference_no"));
             cnt1 =  getLoadData("bpid.reference_no");
             if (cnt1>0) setValue("abem.principal_noback_flag" , "" );
***/

             setPrinNobackFlag();
             if (getValue("acct_code").equals("IT")) {
               if ((getValue("mcht_no").compareTo("106000000001")>=0)&&
                   (getValue("mcht_no").compareTo("106000000009")<=0))
               {
               	setValue("abem.txn_code" , "SI");//帳單分期或其他交易入帳後分期
               }
             }
             procCapDetail();
             capTotCnt++;
             setValue("abem.principal_noback_flag" , "");  
             //非本金類此欄位皆放空值，本金類處裡完，此欄位放空值

            if (getValueDouble("curr_adjust_amt")+getValueDouble("dc_curr_adjust_amt") != 0 )
               {
 if (DEBUGP) showLogMessage("I","","STEP 5 [" + getValue("rate.curr_eng_name") + "]");

                setValue("abem.reference_no"        , getValue("reference_no"));  
                setValue("abem.acct_code"          , getValue("acct_code"));   
                setValue("abem.card_type"          , getValue("card_type"));   
                setValue("abem.txn_code"           , getValue("txn_code"));
                setValue("abem.purchase_date"      , getValue("purchase_date"));  
                setValue("abem.post_date"          , getValue("post_date"));  
                setValue("abem.net_purchase_flag"  , getValue("net_purchase_flag"));  
                setValue("abem.interest_date"      , getValue("interest_date"));  
                setValue("abem.source_amt"         , getValue("curr_adjust_amt"));
                setValue("abem.dest_amt"           , getValue("curr_adjust_amt"));
                setValue("abem.dc_dest_amt"        , getValue("dc_curr_adjust_amt"));
                setValue("abem.currency_code"      , getValue("curr_eng_name"));
                setValue("abem.exchange_date"      , "");
              //setValue("abem.principal_noback_flag" , "*");  

                procCapDetail();
                capAdjCnt++;
/***
                setValue("abem.principal_noback_flag" , "");  
              //非本金類此欄位皆放空值，本金類處裡完，此欄位放空值
***/
               }

            if (getValue("acct_month").equals(getValue("wday.this_acct_month")))
               {
                if (Arrays.asList("BL","CA").contains(getValue("acct_code")))
                   {
                    if (getValue("reference_no_fee_f").length()!=0)
                       {
                        setValue("bill.reference_no" ,  getValue("reference_no_fee_f"));
                        cnt1 =  getLoadData("bill.reference_no");
                        if (cnt1>0) procNewFee("04"); 
                       }
                    if (getValue("fees_reference_no").length()!=0)
                       {
                        setValue("bill.reference_no" ,  getValue("fees_reference_no"));
                        cnt1 =  getLoadData("bill.reference_no");
                        if (cnt1>0) procNewFee("04");
                       }  
                   }
/*
                if ((getValue("acct_code").equals("IT"))&&
                    (getValueInt("install_curr_term")==1)&&
                    (getValueInt("install_tot_term")>1))
                   {
                    setValue("bil2.contract_no" ,  getValue("contract_no"));
                    cnt1 =  getLoadData("bil2.contract_no");
                    if (cnt1>0) proc_it_fee("04");
                   }
*/
               }

            }
         else 
            {
            if (getValue("acct_month").equals(getValue("wday.this_acct_month")))
               {
                if ((Arrays.asList("PF","CF").contains(getValue("acct_code")))&&
                    (getValue("reference_no_original").length()!=0))
                   {
                    setValue("bill.reference_no" ,  getValue("reference_no"));
                    cnt1 =  getLoadData("bill.reference_no");
                    if (cnt1>0) 
                       {
                        updateBilBill();
                        continue;
                       }
                   }
/*
                if ((getValue("acct_code").equals("PF"))&&
                    (getValue("reference_no_original").length()!=0))
                   {
                    setValue("bil2.reference_no_original" ,  getValue("reference_no_original"));
                    cnt1 =  getLoadData("bil2.reference_no_original");
                    if (cnt1>0) 
                       {
                        update_bil_bill();
                        continue;
                       }
                   }
*/
               }

             feeTotCnt++;

 if (DEBUGP) showLogMessage("I","","STEP 6 [" + getValue("rate.curr_eng_name") + "]");

             if ((getValue("acct_code").equals("AF"))&&  /* 年費要特別顯示減免部分 */ 
                 (getValueDouble("dest_amt")<getValueDouble("source_amt")))
                {

 if (DEBUGP) showLogMessage("I","","STEP 7 [" + getValue("rate.curr_eng_name") + "]");

                 setValueDouble("abem.source_amt"  , getValueDouble("source_amt"));
                 setValueDouble("abem.dest_amt"    , getValueDouble("source_amt"));
                 setValueDouble("abem.dc_dest_amt" , getValueDouble("source_amt"));
                 procFeeDetail();
                 feeAnlCnt++;
  
                 setValue("abem.acct_code"         , getValue("acct_code"));
                 setValue("abem.card_type"         , getValue("card_type"));
                 setValue("abem.txn_code"          , getValue("txn_code"));
                 setValue("abem.reference_no"      , getValue("reference_no"));
                 setValue("abem.purchase_date"     , getValue("purchase_date"));  
                 setValue("abem.post_date"         , getValue("post_date"));  
                 setValue("abem.net_purchase_flag" , getValue("net_purchase_flag"));  

                 setValueDouble("abem.source_amt"  , getValueDouble("dest_amt") -
                                                     getValueDouble("source_amt"));
                 setValueDouble("abem.dest_amt"    , getValueDouble("abem.source_amt"));
                 setValueDouble("abem.dc_dest_amt" , getValueDouble("abem.source_amt"));
                }
             procFeeDetail();
             feeTotCnt++;

             if (getValueDouble("curr_adjust_amt")+getValueDouble("dc_curr_adjust_amt") != 0)
                {
 if (DEBUGP) showLogMessage("I","","STEP 8 [" + getValue("rate.curr_eng_name") + "]");
                 setValue("abem.acct_code"          , getValue("acct_code"));
                 setValue("abem.card_type"          , getValue("card_type"));  
                 setValue("abem.txn_code"           , getValue("txn_code"));

                 setValue("abem.source_amt"         , getValue("curr_adjust_amt"));
                 setValue("abem.dest_amt"           , getValue("curr_adjust_amt"));
                 setValue("abem.dc_dest_amt"        , getValue("dc_curr_adjust_amt"));
                 setValue("abem.currency_code"      , getValue("curr_eng_name"));
                 setValue("abem.exchange_date"      , "");
                 procFeeDetail();
                 feeAdjCnt++;
                }
            }
        }
    updateBilBill();
   } 
  closeCursor();
  return;
 }
//************************************************************************
  void setPrinNobackFlag() throws Exception
  {
    if (getValue("ecs_platform_kind").equals("f1")) {
    	setValue("abem.principal_noback_flag" , "f");//財金電子化繳費(稅)平台
    } else if (getValue("ecs_platform_kind").equals("d1")) {
    	setValue("abem.principal_noback_flag" , "d");//公務機關信用卡繳費平台
    } else if (getValue("ecs_platform_kind").equals("M1")) {
    	setValue("abem.principal_noback_flag" , "M");//聯合信用卡處理中心小額支付平台
    } else if (getValue("ecs_platform_kind").equals("e1")) {
    	setValue("abem.principal_noback_flag" , "e");//醫指付行動支付平台
    } else if (Arrays.asList("ET","EP","EI")
      .contains(getValue("ecs_platform_kind"))) {
    	setValue("abem.principal_noback_flag" , "%");//TSCC悠遊卡、IPASS一卡通、ICASH愛金卡
    } else if (Arrays.asList("UW","UB")
      .contains(getValue("ecs_platform_kind"))) {
    	setValue("abem.principal_noback_flag" , "A");//水費、瓦斯費
    } else if (Arrays.asList("UE","UT")
      .contains(getValue("ecs_platform_kind"))) {
    	setValue("abem.principal_noback_flag" , "U");//電費、中華電信費
    }

    return;
  }

// ************************************************************************
 void  selectActComboJrnl() throws Exception
 {
  extendField = "actc.";
  selectSQL = "card_no,"
            + "decode(tran_class,'1',dr_amt,cr_amt*-1) as dr_amt,"
            + "use_sorce,"
            + "tran_date,"
            + "tran_class,"
            + "rowid as rowid";
  daoTable  = "act_combo_jrnl";
  whereStr  = "WHERE   p_seqno     = ? "
            + "and     tran_class in ('1','2') "
            + "AND     billed_date = '' "
            + "AND     acct_month  = ? "
            + "and     decode(tran_class,'1',dr_amt,cr_amt) != 0 "
            + "ORDER   BY card_no,acct_date,seq_no ";

  setString(1  , getValue("p_seqno"));
  setString(2  , getValue("wday.this_acct_month"));

  int recCnt = selectTable();

  if (recCnt==0) return;

  int cnt1=0;
  String chiName="";

  for ( int inti=0; inti<recCnt; inti++ )
    {
     chiName="";

     setValue("use.code",getValue("actc.use_sorce",inti));
     cnt1 = getLoadData("use.code");
     if (cnt1!=0) chiName = getValue("use.chi_name");
      
     if (getValue("actc.tran_class",inti).equals("1"))
        {
         if (chiName.length()==0) setValue("abem.description","動用-方式:現金");
         else setValue("abem.description","動用-方式:"+chiName);
        }
     else
        {
         if (chiName.length()==0) setValue("abem.description","償還-來源:薪資");
         else setValue("abem.description","償還-來源:"+chiName);
        }
     setValue("abem.print_type"        , "05");
     setValue("abem.acct_code"         , getValue("acct_code"));
     setValue("abem.card_type"         , getValue("card_type")); 
     setValue("abem.txn_code"          , getValue("txn_code"));
     setValue("abem.card_no"           , getValue("actc.card_no"   , inti));
     setValue("abem.purchase_date"     , getValue("actc.tran_date" , inti)); 
     setValue("actc.rowid"             , getValue("actc.rowid"     , inti)); 
     setValue("abem.source_amt"        , getValue("actc.dr_amt"    , inti)); 
     setValue("abem.dest_amt"          , getValue("actc.dr_amt"    , inti)); 
     setValue("abem.dc_dest_amt"       , getValue("actc.dr_amt"    , inti)); 
     setValue("abem.currency_code"     , "TWD");

     insertCycAbem();
     updateActComboJrnl(inti);
    }
  return;
 }
// ************************************************************************
 void  selectCycAbem() throws Exception
 {
  selectSQL = "curr_code,"
          //+ "decode(print_type,'06','',card_no) as card_no," 
            + "print_type,"
            + "p_seqno,"
            + "max(bill_sort_seq) as bill_sort_seq,"
            + "max(acct_type) as acct_type,"
          //+ "sum(dest_amt) as dest_amt,"
          //+ "sum(decode(curr_code,'901',dest_amt,dc_dest_amt)) as dc_dest_amt ";
            + "sum(decode(acct_code,'IT',decode(txn_code,'SI',0,dest_amt),dest_amt)) as dest_amt,"
            + "sum(decode(acct_code,'IT',decode(txn_code,'SI',0,dc_dest_amt),dc_dest_amt)) "
            + "as dc_dest_amt ";
  daoTable  = "cyc_abem_"+ getValue("wday.stmt_cycle");
  whereStr  = "WHERE ((print_type in ('04','05','06') "
            + " and      mod_pgm    = ? )  "
            + "OR       (print_type = '06' "
            + " and     mod_pgm    = 'CycA050')) "
            + "AND    dummy_code   != 'Y' "
          //+ "group BY p_seqno,curr_code,decode(print_type,'06','',card_no),print_type";
            + "group BY p_seqno,curr_code,print_type";

  setString(1 , javaProgram);

  openCursor();

  totalCnt=0;
  updateCnt=0;

  while( fetchTable() )
    {
     totalCnt++;

     if (totalCnt%5000==0)
       {
        processDisplay(50000); // every 10000 display message
       }
     setValue("abem.curr_code"               , getValue("curr_code"));
   //setValue("abem.card_no"                 , getValue("card_no"));
     setValue("abem.print_type"              , getValue("print_type"));
     setValue("abem.p_seqno"                 , getValue("p_seqno"));
     setValue("abem.bill_sort_seq"           , getValue("bill_sort_seq"));
     setValue("abem.acct_type"               , getValue("acct_type"));

     setValueDouble("abem.year_fees_rate"     ,-1);
     setValueDouble("abem.unbill_it_end_bal"  , -1);
     initCycAbem();
     setValue("abem.dest_amt"    , "0"); 
     setValue("abem.dc_dest_amt" , "0"); 
     printSeq=0;
   //procHeader();
   //printSeq++;
     hOver9998="N";

     selectCycAbem2();

   //if (!getValue("abem.print_type").equals("05")) 
     if (getValue("abem.print_type").equals("04")) 
     {
     //printSeq++;
       if (hOver9998.equals("Y")) {
      	 printSeq = 9999;
       } else {
       	 printSeq++;
       }
       setValue("abem.dest_amt"          , getValue("dest_amt"));
       setValue("abem.dc_dest_amt"       , getValue("dc_dest_amt"));
       procTailer();
     }
    }
  closeCursor();
  return;
 }

// ************************************************************************
 void  selectCycAbem2() throws Exception
 {
  setValue("load.p_seqno"    , getValue("abem.p_seqno"));
  setValue("load.curr_code"  , getValue("abem.curr_code"));
  setValue("load.print_type" , getValue("abem.print_type"));
//setValue("load.card_no2"    , getValue("abem.card_no"));
//int cnt1 = getLoadData("load.p_seqno,load.print_type,load.curr_code,load.card_no2"); 
  int cnt1 = getLoadData("load.p_seqno,load.curr_code,load.print_type"); 

  for ( int inti=0; inti<cnt1; inti++ )
    {
     printSeq++;
     if (printSeq > 9998) {
       hOver9998="Y";
     	 printSeq = 1;
     }
//    showLogMessage("I","","STEP A2 print_seq ]"+print_seq+"]");

     updateCycAbem(getValue("load.rowid",inti));
    }
  return;
 }
// ************************************************************************
 int insertCycAbem() throws Exception
 {
  dateTime();
//setValue("abem.acct_key"             , "");cyc_abem 沒有這欄位                  
  setValue("abem.print_seq"            , String.format("%04d",printSeq));
//setValue("abem.mod_time"             , sysDate+sysTime);cyc_abem 沒有這欄位
  setValue("abem.mod_seqno"            , String.format("%d",modSeqno++));
  setValue("abem.mod_pgm"              , javaProgram);

  extendField = "abem.";
  daoTable  = "cyc_abem_"+getValue("wday.stmt_cycle");

  insertTable();

  initCycAbem();

  return(0);
 }
// ************************************************************************
 void  loadPtrCurrcode() throws Exception
 {
  extendField = "rate.";
  selectSQL = "curr_code,"
            + "curr_eng_name,"
            + "bill_sort_seq";
  daoTable  = "ptr_currcode";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("rate.curr_code");

  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************
 void  loadPtrCombUse() throws Exception
 {
  extendField = "use.";
  selectSQL = "chi_name";
  daoTable  = "ptr_comb_use";
  whereStr  = "";

  int  n = loadTable();
  setLoadData("use.code");

  showLogMessage("I","","Load ptr_comb_use Count: ["+n+"]");
 }
// ************************************************************************
 void  loadBilContract() throws Exception
 {
  extendField = "cont.";
  selectSQL = "unit_price,"
            + "remd_amt,"
            + "install_tot_term,"
            + "install_curr_term,"
            + "clt_unit_price,"
            + "clt_install_tot_term,"
            + "year_fees_rate,"
            + "new_it_flag,"
            + "contract_no,"
            + "contract_seq_no";
  daoTable  = "bil_contract";
  whereStr  = "where p_seqno in (select p_seqno from cyc_acmm_"+getValue("wday.stmt_cycle")+") "
            + "and to_char(add_months(to_date(first_post_date,'yyyymmdd'),"
            + "            install_tot_term+3),'yyyymmdd') > ? "
            + "and first_post_date !='' "
//          + "and p_seqno = '"+p_seqno"' " //debug 20201221
            ;

  setString(1  , businessDate);

  int  n = loadTable();
  setLoadData("cont.contract_no,cont.contract_seq_no");

  showLogMessage("I","","Load bil_contract Count: ["+n+"]");
 }
// ************************************************************************
 void deleteCycAbemyc_abem() throws Exception
 {
  daoTable  = "cyc_abem_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE  mod_pgm = ? "
            + "AND    print_type in ('04','05','06') "
//          + "and p_seqno = '"+p_seqno+"' " //debug 20201221
            ;

  setString(1 , javaProgram);

  int n = deleteTable();

  showLogMessage("I","","    刪除 cyc_abem ["+n+"] 筆 完成");
  return;
 }
// ************************************************************************
 int updateBilBill() throws Exception
 {
  updateSQL = "billed_date = ?, "
            + "billed_flag = 'B',"
            + "mod_pgm       = ?, "
            + "mod_time      = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "bil_bill";
  whereStr  = "where reference_no  = ?  ";

  setString(1 , businessDate);
  setString(2 , javaProgram);
  setString(3 , sysDate+sysTime);
  setString(4 , getValue("reference_no"));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int updateBilBillDebug() throws Exception
 {
  updateSQL = "billed_date = '', "
            + "billed_flag = 'N'";
  daoTable  = "bil_bill";
  whereStr  = "where billed_date  = ? "
            + "AND   stmt_cycle   = ? ";

  if (DEBUGP) whereStr  = whereStr 
            + "and   p_seqno= ?  "
            ;

  setString(1 , businessDate);
  setString(2 , getValue("wday.stmt_cycle"));
  if (DEBUGP) setString(3 , pSeqno);

  int n = updateTable();

  showLogMessage("I","","    更新 bil_bill ["+n+"] 筆 完成");
  return(0);
 }
// ************************************************************************                                                                 
 int updateActComboJrnl(int inti) throws Exception
 {
  updateSQL = "billed_date = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "act_combo_jrnl";
  whereStr  = "where rowid  = ?  ";

  setString(1 , businessDate);
  setString(2 , javaProgram);
  setString(3 , sysDate+sysTime);
  setRowId(4  , getValue("actc.rowid",inti));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int updateActComboJrnlDebug() throws Exception
 {
  updateSQL = "billed_date = '' ";
  daoTable  = "act_combo_jrnl";
  whereStr  = "where billed_date  = ?  "
            + "and   p_seqno in (select p_seqno "
            + "                  from cyc_acmm_"+getValue("wday.stmt_cycle")+")"
            ;
  if (DEBUGP) whereStr  = whereStr 
            + "and   p_seqno= ?  "
            ;

  setString(1 , businessDate);
  if (DEBUGP) setString(2 , pSeqno);

  int n = updateTable();

  showLogMessage("I","","    更新 act_combo_jrnl ["+n+"] 筆 完成");
  return(0);
 }
// ************************************************************************                                                                 
 int updateCycAbem(String abemRowid) throws Exception
 {
  updateSQL = "print_seq = ? ";
  daoTable  = "cyc_abem_"+getValue("wday.stmt_cycle");
  whereStr  = "where rowid   = ?  ";

  setString(1 , String.format("%04d",printSeq));
  //若超過9999使用此method仍然會超過4位數，已改成printSeq超過9998則reset為1
  //若要限定只取最後4位數，可參考使用 comc.fixRight(String.format("%04d",printSeq),4)
  setRowId(2  , abemRowid);

  updateTable();

  if (notFound.equals("Y")) return(0);

  updateCnt++;
  return(0);
 }
// ************************************************************************                                                                 
 void initCycAbem() throws Exception 
 { 
  setValue("abem.acct_code"         , "");
  setValue("abem.purchase_date"     , "");
  setValue("abem.net_purchase_flag" , "");
  setValue("abem.post_date"         , "");
  setValue("abem.interest_date"     , "");
  setValue("abem.description"       , "");
  setValue("abem.area_code"         , "");
  setValue("abem.exchange_date"     , "");
  setValue("abem.currency_code"     , "");
  setValue("abem.dummy_code"        , "");
  setValue("abem.card_type"         , "");
  setValue("abem.txn_code"          , "");
  setValue("abem.reference_no"      , "");
 }
// ************************************************************************                                                                 
 void insertComboDetail() throws Exception   
 {
  setValue("abem.print_type"        , "05");
  setValue("abem.purchase_date"     , "");
  setValue("abem.net_purchase_flag" , "");
  setValue("abem.description"       , getValue("mcht_chi_name"));
  setValue("abem.dummy_code"        , "S");
  
  insertCycAbem();
 }
// ************************************************************************                                                                 
 void procCapDetail() throws Exception  
 {
  setValue("abem.print_type"         , "04");
  setValue("abem.description"        , getValue("mcht_chi_name"));

//int cityLen = getValue("mcht_city").length();//mcht_city可能有中文
  int cityLen = getValue("mcht_city").getBytes("MS950").length;
  byte[] bytes = getValue("mcht_city").getBytes("MS950");
  if (cityLen>8 ) 
    cityLen=8;

//setValue("abem.area_code"          , getValue("mcht_city").substring(0,cityLen));
  setValue("abem.area_code"          , comc.subMS950String(bytes,0, cityLen));

  insertCycAbem();
 }
// ************************************************************************                                                                 
void procTailer()  throws Exception 
 {
  if (!getValue("abem.print_type").equals("04")) return;

//setValue("abem.description" , "小  計");
  setValue("abem.description" , "本期消費");
  setValue("abem.dummy_code"  , "S");
  setValue("abem.card_no"  , "");
  setValue("abem.source_amt"  , "0");

//if (getValue("abem.print_type").equals("06")) printSeq=9999;
  insertCycAbem();
 }
// ************************************************************************                                                                 
 void procHeader() throws Exception 
 {
  if (getValue("abem.print_type").equals("04"))
     {
      setValue("abem.description" , hiddenCardNo(getValue("abem.card_no"))+" 本期交易明細");
     }
  else if (getValue("abem.print_type").equals("05"))
     {
      setValue("abem.description" , hiddenCardNo(getValue("abem.card_no"))+"    本期預借現金");
     }
  else if (getValue("abem.print_type").equals("06"))
     {
      setValue("abem.description" , "其他交易明細 : ");
     }
  setValue("abem.dummy_code" ,"Y");
  setValue("abem.source_amt" , "0");

  printSeq++;
  insertCycAbem();  
 }
// ************************************************************************                                                                 
 void procFeeDetail()  throws Exception 
 {
  setValue("abem.print_type"     , "06");
  setValue("abem.description"    , getValue("mcht_chi_name"));
  setValue("abem.currency_code"  , getValue("src_curr_eng_name"));  
    
  setValue("abem.interest_date" , "");

  if ((getValue("abem.txn_code").equals("OI"))||
      (getValue("abem.txn_code").equals("HC")))
     {
/* bug 
      int maxstrLen = new String(getValue("mcht_chi_name").getBytes("big5")).length();
      if  (maxstrLen>20) maxstrLen = 20; 
      tmpstr = new String(getValue("mcht_chi_name").getBytes("big5"),0,maxstrLen);
      setValue("abem.description"  , tmpstr);
*/
      setValue("abem.description"  , getValue("mcht_chi_name"));
     }
       
  if ((Arrays.asList("CF","AF","LF").contains(getValue("abem.acct_code")))|| 
      ((getValue("bill_type").equals("OKOL"))&&
       (getValue("abem.txn_code").equals("RR"))))
     {
      tmpstr = hiddenCardNo(getValue("abem.card_no")) + getValue("mcht_chi_name");
      setValue("abem.description" , tmpstr);
/*
      tmpstr = hidden_card_no(getValue("abem.card_no"))+"    本期預借現金";
        
      switch(getValue("abem.acct_code"))
        {
         case "AF":if (getValueDouble("abem.dest_amt")>=0) 
                       tmpstr1 = new String(tmpstr.getBytes("big5"),0,19)+"年費"; 
                   else tmpstr1 = new String(tmpstr.getBytes("big5"),0,19)+"年費優惠免收"; 
                   break;
         case "LF":tmpstr1 = new String(tmpstr.getBytes("big5"),0,19)+"掛失手續費"; 
                   break;
//       case "CF":tmpstr1 = new String(tmpstr.getBytes("big5"),0,19)+"預借現金手續費";
         case "CF":tmpstr1 = "預借現金手續費";
                   break;
         default :tmpstr1 = new String(tmpstr.getBytes("big5"),0,19)+"調帳簽帳單手續費";
                   break;
        }
      setValue("abem.description" , tmpstr1);
*/ 
     }
  if ((Arrays.asList("RI","AF","LF").contains(getValue("abem.acct_code")))&&
      (getValueDouble("abem.dc_dest_amt")<0)) 
     {
      tmpstr1 = getValue("abem.description") + "優惠免收";
      setValue("abem.description" , tmpstr1);
     }
    
  insertCycAbem();
 }
// ************************************************************************                                                                 
 void selectBilContract() throws Exception 
 {
  setValue("cont.contract_no"    ,getValue("contract_no"));
  setValue("cont.contract_seq_no",getValue("contract_seq_no"));
  int cnt1 = getLoadData("cont.contract_no,cont.contract_seq_no");

  if (cnt1==00) showLogMessage("I","","Contract_no ["+getValue("contract_no")+"] not found error"); 

  if (cnt1>0)
     {
      if (getValue("abem.acct_code").equals("IT"))
         {
          setValueDouble("abem.unbill_it_end_bal" ,
                          getValueDouble("cont.unit_price")
                        *(getValueDouble("cont.install_tot_term")-getValueDouble("install_curr_term"))
                        + getValueDouble("cont.remd_amt"));
         }
      else
         {
          if (getValue("cont.new_it_flag").equals("Y"))
             setValue("abem.unbill_it_end_bal" , "0");
          else
             setValueDouble("abem.unbill_it_end_bal" ,
                          getValueDouble("cont.clt_unit_price")
                          *(getValueDouble("cont.clt_install_tot_term")-getValueDouble("install_curr_term")));
         }
      setValueDouble("abem.year_fees_rate" , getValueDouble("cont.year_fees_rate"));
      return;
     }

  if (selectBilContractHst()!=0)
     {
      setValue("abem.unbill_it_end_bal" , "0");
      setValue("abem.year_fees_rate"    , "0");
      return;
     }
     
  return;
 }
// ************************************************************************                                                                 
 int  selectBilContractHst() throws Exception 
 {
  extendField = "chst.";
  daoTable  = "bil_contract_hst";
  whereStr  = "WHERE contract_no     = ? "
            + "AND   contract_seq_no = ? ";

  setString(1 , getValue("contract_no"));
  setInt(2    , getValueInt("contract_no_seq"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
   
  if (getValue("abem.acct_code").equals("IT"))
     {
      setValueDouble("abem.unbill_it_end_bal" ,
                      getValueDouble("chst.unit_price")
                    *(getValueDouble("chst.install_tot_term")-getValueDouble("install_curr_term"))
                    + getValueDouble("chst.remd_amt"));
     }
  else
     {
      if (getValue("chst.new_it_flag").equals("Y"))
         setValue("abem,unbill_it_end_bal" , "0");
      else
         setValueDouble("abem.unbill_it_end_bal" ,
                      getValueDouble("chst.clt_unit_price")
                    *(getValueDouble("chst.clt_install_tot_term")-getValueDouble("install_curr_term")));
     }
  setValueDouble("abem.year_fees_rate" , getValueDouble("chst.year_fees_rate"));
  return(0);
 }  
// ************************************************************************
 String checkCardtaxId(String input)
 {
  int charlen,int1a,int1b;
  int fstCnt=0;
  int lstCnt=0;

  for (int1a=0;int1a<input.length();int1a++)
    {
     fstCnt=0;
     if ((input.substring(int1a,int1a+1).compareTo("A")<0)||
         (input.substring(int1a,int1a+1).compareTo("Z")>0)) continue;
     else fstCnt=int1a;
      
     for (int1b=int1a+2;int1b<int1a+10;int1b++)
         if ((input.substring(int1b,int1b+1).compareTo("0")<0)||
             (input.substring(int1b,int1b+1).compareTo("9")>0)) 
            {
             lstCnt = int1b;
             break;
            }

     if (lstCnt<fstCnt+10) continue;
     input = input.substring(0,fstCnt+4)+"XXXX"+input.substring(fstCnt+8,input.length());
     break;
   }
 return input;
 }                                                                
// ************************************************************************                                                                 
 String hiddenCardNo(String cardNo)
 {
   if (cardNo.length()==0) return "";
   String tmpstr="";
   tmpstr = cardNo.substring(0,4)
          + "-"
          + cardNo.substring(4,6)
          + "XX-";
   if (cardNo.length()==15)
      tmpstr = tmpstr 
             + "XXX"
             + cardNo.substring(11,12)
             + "-"
             + cardNo.substring(12,15);
   else
      tmpstr = tmpstr 
             + "XXXX-"
             + cardNo.substring(12,cardNo.length());

   return tmpstr;
 }
/******************************************************************************/                                                                                                          
 void  loadCycAbem() throws Exception
 {
  extendField = "load.";
  selectSQL = "rowid as abem_rowid,"
            + "p_Seqno,"
            + "curr_code,"
            + "card_no,"
          //+ "decode(print_type,'06','',card_no) as card_no2,"
            + "print_type,"
            + "rowid as rowid";
  daoTable  = "cyc_abem_"+getValue("wday.stmt_cycle");
  whereStr  = "WHERE ((print_type in ('04','05','06') "
            + "  and     mod_pgm    = ?)  "
            + " OR      (print_type = '06' "
            + "  and    mod_pgm    = 'CycA050')) "
            + "AND    dummy_code   != 'Y' "
          //+ "ORDER BY p_seqno,curr_code,print_type,card_no,"
          //+ "         decode(print_type,'05',decode(acct_code,'CF',1,0),1),"
          //+ "         decode(purchase_date,'','99999999',purchase_date),mod_seqno"
            + "ORDER BY p_seqno,curr_code,print_type,card_no,"
            + "         decode(purchase_date,'','99999999',purchase_date),"
            + "         mod_seqno"
          //如有預借現金手續費或其他本金手續費以何方式排序使得本金交易與手續費排在一起
            ;

  setString(1 , javaProgram);

  int  n = loadTable();

//setLoadData("load.p_seqno,load.print_type,load.curr_code,load.card_no2");
  setLoadData("load.p_seqno,load.curr_code,load.print_type");

  showLogMessage("I","","Load cyc_abem Count: ["+n+"]");
 }
// ************************************************************************
 void  loadBilBill() throws Exception
 {
  extendField = "bill.";
  selectSQL = "reference_no,"
            + "txn_code,"
            + "sign_flag,"
            + "acct_code,"
            + "source_amt,"
            + "curr_adjust_amt,"
            + "dc_curr_adjust_amt,"
            + "dest_amt,"
            + "purchase_date,"
            + "net_purchase_flag,"
            + "post_date,"
            + "decode(mcht_chi_name,'',mcht_eng_name,mcht_chi_name) as mcht_chi_name,"
            + "dc_dest_amt";
  daoTable  = "bil_bill";
  whereStr  = "where acct_month = ? "
            + "and   stmt_cycle = ? "
            + "and   acct_code  in ('PF','CF') "
            + "and   reference_no_original in (  "
            + "      select reference_no  "
            + "      from bil_bill "
            + "      where acct_month = ? "
            + "      and stmt_cycle   = ? "
            + "      and acct_code in ('BL','CA')) "
            ;

  if (DEBUGP) whereStr  = whereStr 
                       + "and   p_seqno= ? ";

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));
  setString(3 , getValue("wday.this_acct_month"));
  setString(4 , getValue("wday.stmt_cycle"));
  if (DEBUGP) 
     setString(5 , pSeqno);

  int  n = loadTable();
  setLoadData("bill.reference_no");

  showLogMessage("I","","Load bil_bill Count: ["+n+"]");
 }

// ************************************************************************
 void  loadMktBpidData() throws Exception
 {
  extendField = "bpid.";
  selectSQL = "reference_no,"
            + "proc_flag ";
  daoTable  = "mkt_bpid_data";
  whereStr  = "where acct_month = ? "
            + "and   stmt_cycle = ? "
            ;

  if (DEBUGP) whereStr  = whereStr 
                       + "and   p_seqno= ? ";

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));
  if (DEBUGP) 
     setString(3 , pSeqno);

  int  n = loadTable();
  setLoadData("bpid.reference_no");

  showLogMessage("I","","Load mkt_bpid_data Count: ["+n+"]");
 }
// ************************************************************************
 void procFeeFDetail(String printType)  throws Exception 
 {
  setValue("abem.print_type"         , printType);
  setValue("abem.p_seqno"            , getValue("p_seqno"));  
  setValue("abem.curr_code"          , getValue("curr_code"));  
  setValue("abem.acct_type"          , getValue("acct_type"));  
  setValue("abem.card_no"            , getValue("card_no"));
  setValue("abem.reference_no"       , getValue("bill.reference_no"));  
  setValue("abem.purchase_date"      , getValue("purchase_date"));
  setValue("abem.net_purchase_flag"  , "");
  setValue("abem.post_date"          , getValue("post_date"));  
  setValue("abem.acct_code"          , getValue("bill.acct_code"));
  setValue("abem.card_type"          , getValue("card_type"));
  setValue("abem.txn_code"           , getValue("bill.txn_code"));
  setValue("abem.bill_sort_seq"      , getValue("rate.bill_sort_seq"));

  setValue("abem.source_amt"         , getValue("bill.source_amt"));
  setValue("abem.dest_amt"           , getValue("bill.dest_amt"));
  setValue("abem.dc_dest_amt"        , getValue("bill.dc_dest_amt"));
    if (getValue("bill.sign_flag").equals("-"))
       {
        setValueDouble("abem.dest_amt"     , getValueDouble("bill.dest_amt")*-1);
        setValueDouble("abem.dc_dest_amt"  , getValueDouble("bill.dc_dest_amt")*-1);
        setValueDouble("abem.source_amt"   , getValueDouble("bill.source_amt")*-1);
       }
  if ((printType.equals("05"))&&
      (!getValue("abem.acct_code").equals("CF")))     {

      setValue("abem.description"        , hiddenCardNo(getValue("abem.card_no")) 
                                         + getValue("bill.mcht_chi_name"));
     }
  else
     setValue("abem.description"        , getValue("bill.mcht_chi_name"));
  setValue("abem.currency_code"      , getValue("curr_eng_name"));  
  setValue("abem.cap_type"           , "1"); 
    
  setValueDouble("abem.year_fees_rate"      , -1);
  setValueDouble("abem.unbill_it_end_bal"   , -1);

  insertCycAbem();
 }
// ************************************************************************
 void procFeeFAdjustDetail(String printType)  throws Exception 
 {
  setValue("abem.print_type"         , printType);
  setValue("abem.p_seqno"            , getValue("p_seqno"));  
  setValue("abem.curr_code"          , getValue("curr_code"));  
  setValue("abem.acct_type"          , getValue("acct_type"));  
  setValue("abem.card_no"            , getValue("card_no"));
  setValue("abem.card_type"          , getValue("card_type"));
  setValue("abem.reference_no"       , getValue("bill.reference_no"));  
  setValue("abem.acct_code"          , getValue("bill.acct_code"));
  setValue("abem.txn_code"           , getValue("bill.txn_code"));
  setValue("abem.source_amt"         , getValue("bill.curr_adjust_amt"));
  setValue("abem.dest_amt"           , getValue("bill.curr_adjust_amt"));
  setValue("abem.dc_dest_amt"        , getValue("bill.dc_curr_adjust_amt"));
  setValue("abem.purchase_date"      , getValue("purchase_date"));
  setValue("abem.post_date"          , getValue("post_date"));  
  setValue("abem.bill_sort_seq"      , getValue("rate.bill_sort_seq"));

  setValue("abem.description"        , getValue("bill.mcht_chi_name"));

  setValue("abem.currency_code"      , getValue("curr_eng_name"));  
  setValue("abem.cap_type"           , "1"); 
    
  setValueDouble("abem.year_fees_rate"      , -1);
  setValueDouble("abem.unbill_it_end_bal"   , -1);

  insertCycAbem();
 }
// ************************************************************************
 void procNewFee(String printType)  throws Exception 
 {
  procFeeFDetail(printType);
  
  if (!printType.equals("05"))
     {
      coboDestAmt    = coboDestAmt
                       + getValueDouble("bill.dest_amt");
      coboDcDestAmt = coboDcDestAmt
                       + getValueDouble("bill.dc_dest_amt");
     }

  if (getValueDouble("bill.curr_adjust_amt") +
      getValueDouble("bill.dc_curr_adjust_amt") == 0 ) return;

  procFeeFAdjustDetail(printType);
  if (!printType.equals("05"))
     {
      coboDestAmt    = coboDestAmt
                       + getValueDouble("bill.curr_adjust_amt");
      coboDcDestAmt = coboDcDestAmt
                       + getValueDouble("bill.dc_curr_adjust_amt");
     }
 }
// ************************************************************************                                                                 
 void  loadBilBill2() throws Exception
 {
  extendField = "bil2.";
  selectSQL = "a.reference_no,"
            + "b.contract_no,"
            + "a.reference_no_original,"
            + "a.txn_code,"
            + "a.sign_flag,"
            + "a.acct_code,"
            + "a.source_amt,"
            + "a.dest_amt,"
            + "a.curr_adjust_amt,"
            + "a.purchase_date,"
            + "a.post_date,"
            + "decode(a.mcht_chi_name,'',a.mcht_eng_name,a.mcht_chi_name) as mcht_chi_name,"
            + "a.dc_dest_amt";
  daoTable  = "bil_bill a,bil_contract b";
  whereStr  = "where a.acct_month = ? "
            + "and   a.stmt_cycle = ? "
            + "and   a.acct_code  = 'PF' "
            + "and   a.reference_no_original != '' "
            + "and   a.reference_no_original = b.reference_no "
            + "and   b.install_curr_term = 1 "
            + "and   b.install_tot_term > 1 "
            ;

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  int  n = loadTable();
  setLoadData("bil2.contract_no");
  setLoadData("bil2.reference_no_original");

  showLogMessage("I","","Load bil_bill_2 Count: ["+n+"]");

 }
// ************************************************************************
 void procItFee(String printType)  throws Exception 
 {
          if (DEBUGP) showLogMessage("I","","STEP 5.1 [" + getValue("rate.curr_eng_name") + "]");
  setValue("abem.print_type"         , printType);
  setValue("abem.p_seqno"            , getValue("p_seqno"));  
  setValue("abem.curr_code"          , getValue("curr_code"));  
  setValue("abem.acct_type"          , getValue("acct_type"));  
  setValue("abem.card_no"            , getValue("card_no"));
  setValue("abem.card_type"          , getValue("card_type"));
  setValue("abem.reference_no"       , getValue("bil2.reference_no"));  
  setValue("abem.acct_code"          , getValue("bil2.acct_code"));
  setValue("abem.txn_code"           , getValue("bil2.txn_code"));
  setValue("abem.purchase_date"      , getValue("purchase_date"));
  setValue("abem.post_date"          , getValue("post_date"));  
  setValue("abem.bill_sort_seq"      , getValue("rate.bill_sort_seq"));

  setValue("abem.source_amt"         , getValue("bil2.source_amt"));
  setValue("abem.dest_amt"           , getValue("bil2.dest_amt"));
  setValue("abem.dc_dest_amt"        , getValue("bil2.dc_dest_amt"));
    if (getValue("bil2.sign_flag").equals("-"))
       {
        setValueDouble("abem.dest_amt"     , getValueDouble("bil2.dest_amt")*-1);
        setValueDouble("abem.dc_dest_amt"  , getValueDouble("bil2.dc_dest_amt")*-1);
        setValueDouble("abem.source_amt"   , getValueDouble("bil2.source_amt")*-1);
       }

  setValue("abem.description"        , getValue("bil2.mcht_chi_name"));

  setValue("abem.currency_code"      , getValue("curr_eng_name"));  
  setValue("abem.cap_type"           , "1"); 
    
  setValueDouble("abem.year_fees_rate"      , -1);
  setValueDouble("abem.unbill_it_end_bal"   , -1);

  insertCycAbem();

  if (getValueDouble("bil2.curr_adjust_amt") +
      getValueDouble("bil2.dc_curr_adjust_amt") == 0 ) return;

          if (DEBUGP) showLogMessage("I","","STEP 5.1 [" + getValue("rate.curr_eng_name") + "]");

  setValue("abem.print_type"         , printType);
  setValue("abem.p_seqno"            , getValue("p_seqno"));  
  setValue("abem.curr_code"          , getValue("curr_code"));  
  setValue("abem.acct_type"          , getValue("acct_type"));  
  setValue("abem.card_no"            , getValue("card_no"));
  setValue("abem.card_type"          , getValue("card_type"));
  setValue("abem.reference_no"       , getValue("bil2.reference_no"));  
  setValue("abem.acct_code"          , getValue("bil2.acct_code"));
  setValue("abem.txn_code"           , getValue("bil2.txn_code"));
  setValue("abem.purchase_date"      , getValue("purchase_date"));
  setValue("abem.post_date"          , getValue("post_date"));  
  setValue("abem.bill_sort_seq"      , getValue("rate.bill_sort_seq"));

  setValue("abem.source_amt"         , getValue("bil2.curr_adjust_amt"));
  setValue("abem.dest_amt"           , getValue("bil2.curr_adjust_amt"));
  setValue("abem.dc_dest_amt"        , getValue("bil2.curr_adjust_amt"));

  setValue("abem.description"        , getValue("bil2.mcht_chi_name"));

  setValue("abem.currency_code"      , getValue("curr_eng_name"));  
  setValue("abem.cap_type"           , "1"); 
    
  setValueDouble("abem.year_fees_rate"      , -1);
  setValueDouble("abem.unbill_it_end_bal"   , -1);

  insertCycAbem();
 }
// ************************************************************************


}  // End of class FetchSample
