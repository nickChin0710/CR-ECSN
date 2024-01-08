/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/08/12  V1.00.30  Allen Ho   cyc_a110 PROD compare OK                   *
* 111-10-17  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112/01/17  V1.00.04  Simon      apply no field "bil_bill.rsk_problem1_mark"* 
* 112/05/11  V1.00.05  Simon      add act_acno.acno_flag to cyc_acmm.acno_flag* 
* 112/06/21  V1.00.06  Simon      act_acct_curr_xx.bill_sort_seq以參數檔更新 * 
* 112/12/19  V1.00.07  Simon      1.改抓 act_acno.e_mail_ebill 寫入 cyc_acmm.e_mail_addr* 
*                                 2.remove setValue("acmm.rc_indicator" , "3") for 商務卡* 
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA040 extends AccessDAO
{
 private final String PROGNAME = "關帳-產生對帳單抬頭檔處理程式 112-12-19  V1.00.07";
 CommFunction comm = new CommFunction();

 String businessDate      = "";


 String[] mainTables = {"CYC_ACMM","CYC_ACMM_CURR","CYC_ABEM"};
 long    totalCnt=0,currCnt=0;
 int DEBUG =1;
 int paymentAmt = 0,insertCnt=0,updateCnt=0,pcodCnt=0;
 int[] checkInt=  new int [10];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA040 proc = new CycA040();
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
       return(1);
      }

   if (( args.length >= 1 )&&(args[0].length()==8))
      { businessDate = args[0]; }
   
   if ( !connectDataBase() ) 
       return(1);

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","帳務月份["+getValue("wday.this_acct_month")+"]");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料 開始");
   loadCrdCard();
   loadBilContract();
// load_bil_merchant();   // 20210812
   loadCrdCardNo901();
   loadCycPyajNo901();
   loadBilBillNo901();
   totalCnt=0;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","清檔 開始...");
    for (int inti=0;inti<mainTables.length;inti++)
        {
         commitDataBase();
         truncateTable(inti,getValue("wday.stmt_cycle"));
        }
   showLogMessage("I","","清檔完成");
   showLogMessage("I","","=========================================");

   totalCnt=0;
   showLogMessage("I","","=========================================");
   showLogMessage("I","","新增 cyc_acmm 開始");
   selectActAcno();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","更新 cyc_acmm 第一階段...");
   selectCrdIdno();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","更新 cyc_acmm 第二階段...");
   selectCrdCorp();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","更新 cyc_acmm 第三階段...");
   selectActAcctCurr();
   showLogMessage("I","","total_count=["+totalCnt+"]");
   showLogMessage("I","","更新 cyc_acmm 完成");
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
 void  selectActAcno() throws Exception
 {
  selectSQL = "";
  daoTable  = "act_acno_"+ getValue("wday.stmt_cycle");
  whereStr  = "WHERE  acno_flag != 'Y' ";

  openCursor();

  totalCnt=0;
  String statSMonth="";
  String statEMonth="";
  String[] chinZip = {"０","１","２","３","４","５","６","７","８","９"};
  String tmpstr ="";

  while( fetchTable() ) 
   { 
    totalCnt++;
    if (getValue("special_stat_code").length()==0) setValue("special_stat_code","5");
    if (getValue("special_stat_division").length()==0) setValue("special_stat_division","OP");
    if (getValue("stat_send_paper").equals("Y"))
       { 
        statSMonth = getValue("stat_send_s_month");
        statEMonth = getValue("stat_send_e_month");
        if (getValue("stat_send_s_month").length()==0) statSMonth = "000000";
        if (getValue("stat_send_e_month").length()==0) statEMonth = "999912";

        if ((getValue("wday.this_acct_month").compareTo(statSMonth)>=0)&&
            (getValue("wday.this_acct_month").compareTo(statEMonth)<=0))
           {
            setValue("stat_send_paper","Y");
           }
        else
           {
            setValue("stat_send_paper","N");
            setValue("stat_send_s_month" , "");
            setValue("stat_send_e_month" , "");
           }
       }
    else
       {
        setValue("stat_send_paper","N");
       }

    if (getValue("stat_send_internet").equals("Y"))
       { 
        statSMonth = getValue("stat_send_s_month2");
        statEMonth = getValue("stat_send_e_month2");
        if (getValue("stat_send_s_month2").length()==0) statSMonth = "000000";
        if (getValue("stat_send_e_month2").length()==0) statEMonth = "999912";
        if ((getValue("wday.this_acct_month").compareTo(statSMonth)>=0)&&
            (getValue("wday.this_acct_month").compareTo(statEMonth)<=0))  
           {
            setValue("stat_send_internet","Y");
           }
        else
           {
            setValue("stat_send_internet","N");
            setValue("stat_send_s_month2" , "");
            setValue("stat_send_e_month2" , "");
           }
       }
    else
       {
        setValue("stat_send_internet","N");
       }
    if (getValue("rc_use_s_date").length()==0) setValue("rc_use_s_date" , "99999999");
    if (getValue("rc_use_e_date").length()==0) setValue("rc_use_e_date" , "99999999");

    if (getValue("autopay_acct_s_date").trim().length()==0) setValue("autopay_acct_s_date" , "99999999");
    if (getValue("autopay_acct_e_date").trim().length()==0) setValue("autopay_acct_e_date" , "99999999");

    if (getValue("stat_unprint_s_month").length()==0) setValue("stat_unprint_s_month" , "000000");
    if (getValue("stat_unprint_e_month").length()==0) setValue("stat_unprint_e_month" , "999999");

    if (!getValue("new_cycle_month").equals(getValue("wday.this_acct_month"))) 
       setValue("last_interest_date","");


    selectCrdCard();

   
    setValue("acmm.pay_by_stage_flag" , "");

    if ((getValue("pay_by_stage_flag").compareTo("00")>=0)&&
        (getValue("pay_by_stage_flag").compareTo("99")<=0)) setValue("acmm.pay_by_stage_flag","Y");

    switch(getValue("rc_use_indicator"))
      {
       case "1":setValue("acmm.rc_indicator","1");break;
       case "2":
       case "3":if ((businessDate.compareTo(getValue("rc_use_s_date"))>=0)&&
                    (businessDate.compareTo(getValue("rc_use_e_date"))<=0))  
                   setValue("acmm.rc_indicator",getValue("rc_use_indicator"));
       default :setValue("acmm.rc_indicator",getValue("rc_use_b_adj")); 
      }
  //if (getValue("card_indicator").equals("2")) setValue("acmm.rc_indicator" , "3");

    setValue("acmm.unprint_flag" , "");
    if ((getValue("stat_unprint_flag").equals("Y"))&&
        (businessDate.substring(0,6).compareTo(getValue("stat_unprint_s_month")) >= 0)&&
        (businessDate.substring(0,6).compareTo(getValue("stat_unprint_e_month")) <= 0))
         setValue("acmm.unprint_flag" , "Y");

    tmpstr = "";
    for (int inta=0;inta<getValue("bill_sending_zip").length();inta++)
        {
         
         if ((getValue("bill_sending_zip").charAt(inta)<'0')||
             (getValue("bill_sending_zip").charAt(inta)>'9')) continue;
         tmpstr=tmpstr + chinZip[Character.getNumericValue(getValue("bill_sending_zip").charAt(inta))];
        }
    setValue("acmm.zip_code_chin" , tmpstr);

    if (businessDate.compareTo(getValue("autopay_acct_s_date")) >= 0 &&
        getValue("wday.this_lastpay_date").compareTo(getValue("autopay_acct_e_date")) <= 0)
       {
         setValue("acmm.auto_pay_date"     , getValue("wday.this_lastpay_date"));
         setValue("acmm.auto_pay_acct"     , getValue("autopay_acct_no"));
         setValue("acmm.autopay_acct_bank" , getValue("autopay_acct_bank"));
       }
    else
       {
         setValue("acmm.auto_pay_date"     , "");
         setValue("acmm.autopay_acct_bank" , "");
         setValue("acmm.auto_pay_acct"     , "");
       }

    setValue("acmm.print_sequential",getValue("special_stat_code"));
    if ((getValue("special_stat_e_month").length()==6)&&
        (businessDate.substring(0,6).compareTo(getValue("special_stat_e_month"))>0))
       setValue("acmm.print_sequential","5");

    setValue("acmm.paper_send_e_month" , "");
    if ((getValue("stat_send_e_month").length()!=0)&&
        (getValue("stat_send_e_month").compareTo(getValue("wday.this_acct_month"))>=0))
       {
        if ((getValue("stat_send_e_month2").length()==0)&&
            ((getValue("stat_send_s_month2").compareTo(getValue("wday.this_acct_month"))<=0)&&
             (getValue("stat_send_e_month").compareTo(getValue("stat_send_s_month2"))>=0)))  
            setValue("acmm.paper_send_e_month",getValue("stat_send_e_month"));
       }

    setValue("cont.p_seqno",getValue("p_seqno"));
    int cnt1 = getLoadData("cont.p_seqno");
    if (cnt1==0) 
       {
        setValue("unpost_inst_fee" , "0");
       }
    else
       {
        setValueDouble("unpost_inst_fee" , getValueDouble("cont.unpost_inst_fee"));
/* 20210812 beg    
        double unpost_inst_fee = 0;
        for (int inti=0;inti<cnt1;inti++)
          {
           setValue("mcht.mcht_no",getValue("cont.mcht_no"),inti);
           int cnt2 = getLoadData("mcht.mcht_no");
           if (cnt2==0) unpost_inst_fee = unpost_inst_fee + getValueDouble("cont.unpost_inst_fee",inti);
          }
        setValueDouble("unpost_inst_fee" , unpost_inst_fee);
*/  // 20210812 end
       }

    insertCycAcmm();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void  selectCrdIdno() throws Exception
 {
  selectSQL = "a.chi_name as chinese_name,"
            + "a.birthday,"
            + "decode(a.sex,'1','先生','2','小姐','　　') as chi_title,"
            + "a.staff_flag,"
            + "a.staff_br_no as bank_branch_no,"
            + "a.credit_flag,"
            + "a.salary_code,"
          //+ "a.e_mail_addr,"
            + "a.nation,"
            + "a.home_area_code1||decode(a.home_area_code1,'','','-') as home_area_code1,"
            + "a.home_tel_no1,"
            + "a.home_area_code2||decode(a.home_area_code2,'','','-') as home_area_code2,"
            + "a.home_tel_no2,"
            + "a.office_area_code1||decode(a.office_area_code1,'','','-') as office_area_code1,"
            + "a.office_tel_no1,"
            + "a.office_area_code2||decode(a.office_area_code2,'','','-') as office_area_code2,"
            + "a.office_tel_no2,"
            + "a.cellar_phone,"
            + "b.rowid as rowid ";
  daoTable  = "crd_idno a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b";
  whereStr  = "WHERE  a.id_p_seqno = b.id_p_seqno";

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;
    updateCycAcmm1();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void  selectCrdCorp() throws Exception
 {
  selectSQL = "chi_name as chinese_name,"
          //+ "a.e_mail_addr,"
            + "a.e_mail_addr2,"
            + "a.e_mail_addr3, "
            + "b.rowid as rowid ";
  daoTable  = "crd_corp a,cyc_acmm_"+ getValue("wday.stmt_cycle") +" b";
  whereStr  = "WHERE  a.corp_p_seqno = b.corp_p_seqno "
            + "and    b.id_p_seqno = '' ";

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;
    updateCycAcmm2();
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 void  selectActAcctCurr() throws Exception
 {
  selectSQL = "a.bill_curr_code,"
            + "a.bill_sort_seq as parm_bill_sort_seq,"
            + "b.curr_code,"
            + "b.p_seqno,"
            + "b.acct_type,"
            + "decode(b.curr_code,'901',c.autopay_acct_bank,b.autopay_acct_bank) as  autopay_acct_bank,"
            + "decode(b.curr_code,'901',c.auto_pay_acct,b.autopay_acct_no) as autopay_acct_no,"
            + "b.autopay_dc_flag,"                                              
            + "b.bill_sort_seq,"  
            + "b.dc_acct_jrnl_bal," 
            + "b.dc_end_bal_lk," 
            + "b.dc_end_bal_op," 
            + "b.dc_temp_unbill_interest," 
            + "b.dc_pay_amt," 
            + "b.dc_adjust_dr_amt," 
            + "b.dc_adjust_cr_amt";
  daoTable  = "ptr_currcode a,cyc_acmm_"+ getValue("wday.stmt_cycle") + " c,act_acct_curr_"+ getValue("wday.stmt_cycle") +" b";
  whereStr  = "where a.curr_code = b.curr_code "
            + "and   a.bill_sort_seq !='' "
            + "and   b.p_seqno = c.p_seqno ";
            ;

  openCursor();

  totalCnt=0;
  for (int inti=0;inti<5;inti++) checkInt[inti]=0;
  while( fetchTable() ) 
   {
    if (!getValue("bill_sort_seq").equals(getValue("parm_bill_sort_seq")))
       {
/***
        if (checkCurrCodeNo901()!=0)
           {
            updateActAcctCurrNo901();
           }
         else
           {
            updateActAcctCurr();
           }
***/
        updateActAcctCurr();//更新act_acct_curr_xx
        updateActAcctCurr1();//更新act_acct_curr
       }

    if (!getValue("curr_code").equals("901"))
       {
        if (checkActAcctCurr()==0) updateCycAcmm4();
        else continue;
       }

    totalCnt++;
//  if (getValue("bill_sort_seq").length()==0) 
//     setValue("bill_sort_seq",getValue("parm_bill_sort_seq"));
    insertCycAcmmCurr();
   } 
  closeCursor();
//for (int inti=0;inti<10;inti++)
//    showLogMessage("I","","checkint["+inti+"] = ["+check_int[inti]+"]");
  return;
 }
// ************************************************************************
 int  checkActAcctCurr() throws Exception
 {
  if (getValueDouble("dc_acct_jrnl_bal")!=0) 
     {
      checkInt[0]++;   // 192
      return(0);
     }  

  if (getValueDouble("dc_end_bal_lk")+
      getValueDouble("dc_end_bal_op")+
      getValueDouble("dc_temp_unbill_interest")+
      getValueDouble("dc_pay_amt")+
      getValueDouble("dc_adjust_dr_amt")+
      getValueDouble("dc_adjust_cr_amt")>0)
     {   
      checkInt[1]++; //252
      return(0);
     }  

  setValue("card.p_seqno",getValue("p_seqno"));
  int cnt1 = getLoadData("card.p_seqno");
  int okFlag=0;

  for (int inti=0;inti<cnt1;inti++)
     if ((getValue("card.current_code",inti).equals("0"))&&
         (getValue("card.curr_code",inti).equals(getValue("curr_code")))) 
        {
         checkInt[2]++;  // 1280
         return(0);
        }

  setValue("pyajno901.p_seqno"   , getValue("p_seqno"));
  setValue("pyajno901.curr_code" , getValue("curr_code"));
  cnt1 = getLoadData("pyajno901.p_seqno,pyajno901.curr_code");
  if (cnt1!=0) 
     {
      checkInt[3]++;    //164
      return(0);
     }

  setValue("billno901.p_seqno"   , getValue("p_seqno"));
  cnt1 = getLoadData("billno901.p_seqno");
  if (cnt1!=0) 
     {
      checkInt[3]++;    //164
      return(0);
     }

  return(1);
 }
// ************************************************************************
 void selectCrdCard() throws Exception
 {
  setValue("acmm.group_code"           , "0000");
  setValue("acmm.source_code"          , "000000");
  setValue("acmm.gold_card"            , "N");
  setValue("acmm.valid_card_cnt"       ,  "0");
  setValue("pgcd_bill_form"            , "");
  setValue("card_combo_indicator"      , "N");
  setValue("card_oppost_date"          , "");

  setValue("card.p_seqno",getValue("p_seqno"));
  int cnt1 = getLoadData("card.p_seqno");
  if (cnt1==0) return;

  if (getValue("card.card_note").equals("G"))  setValue("acmm.gold_card","Y");
  if (getValue("card.card_note").equals("P"))  setValue("acmm.gold_card","P");
  if (getValue("card.indicator").equals("2")) setValue("acmm.gold_card" , "Y");

  setValue("acmm.group_code"  , getValue("card.group_code"));
  setValue("acmm.source_code" , getValue("card.source_code"));

  String tmpstr  = "99";
  String tmpstra = "99";
  int comboAliveFlag = 0;
  for (int inta=0;inta<cnt1; inta++)
      {
       if (getValue("card.combo_indicator",inta).equals("Y"))
          {
           if (getValue("card.current_code",inta).equals("0")) comboAliveFlag =1;
           if (getValue("card.oppost_date",inta).compareTo(getValue("card_oppost_date"))>0)
              setValue("card_oppost_date" , getValue("card.oppost_date",inta));
          }
       if (getValue("card.current_code",inta).equals("0")) 
         setValueInt("acmm.valid_card_cnt",(getValueInt("acmm.valid_card_cnt")+1));
       if ((getValue("card.current_code",inta).equals("0"))&&(getValue("card.bill_form",inta).length()>0))
          {
           if (tmpstra.compareTo(getValue("card.bill_form_seq",inta))>0)
              {
               tmpstra = getValue("card.bill_form_seq",inta);
               tmpstr  = getValue("card.bill_form",inta);
              }
          }
      }
  setValue("pgcd_bill_form", tmpstr);
  if (comboAliveFlag==1) setValue("card_oppost_date"    , "");
  if ((getValue("card_oppost_date").length()!=0)||(comboAliveFlag==1)) 
     setValue("card_combo_indicator" , "Y");
 }
// ************************************************************************
 int insertCycAcmm() throws Exception
 {
  dateTime();
  extendField = "acmm.";

  setValue("acmm.p_seqno"                   , getValue("p_seqno"));
  setValue("acmm.acct_type"                 , getValue("acct_type"));
  setValue("acmm.acct_key"                  , getValue("acct_key"));
  setValue("acmm.acno_flag"                 , getValue("acno_flag"));
  setValue("acmm.payment_number"            , getValue("payment_no"));
  setValue("acmm.to_pay_flag"               , getValue("corp_act_flag"));
  setValue("acmm.lost_fee"                  , getValue("lost_fee_flag"));
  setValue("acmm.relation"                  , getValue("bank_rel_flag"));
  setValue("acmm.print_flag"                , "Y");
  setValue("acmm.acct_status"               , getValue("acct_status"));
  setValue("acmm.operator_code"             , getValue("special_stat_division"));
  setValue("acmm.zip_code"                  , getValue("bill_sending_zip"));
  setValue("acmm.mail_addr_1"               , getValue("bill_sending_addr1"));
  setValue("acmm.mail_addr_2"               , getValue("bill_sending_addr2"));
  setValue("acmm.mail_addr_3"               , getValue("bill_sending_addr3"));
  setValue("acmm.mail_addr_4"               , getValue("bill_sending_addr4"));
  setValue("acmm.mail_addr_5"               , getValue("bill_sending_addr5"));
  setValue("acmm.cycle_date"                , getValue("wday.this_close_date"));
  setValue("acmm.credit_limit"              , getValue("line_of_credit_amt"));
  setValue("acmm.class_code"                , getValue("class_code"));
  setValue("acmm.normal_cash_limit"         , getValue("line_of_credit_amt_cash"));
  setValue("acmm.lastpay_date"              , getValue("wday.this_lastpay_date"));
  setValue("acmm.delaypay_date"             , getValue("wday.this_delaypay_date"));
  setValue("acmm.billing_date"              , getValue("wday.this_billing_date"));
  setValue("acmm.interest_date"             , getValue("wday.this_interest_date"));
  setValue("acmm.send_paper"                , getValue("stat_send_paper"));
  setValue("acmm.send_internet"             , getValue("stat_send_internet"));
  setValue("acmm.e_mail_addr"               , getValue("e_mail_ebill"));
  setValue("acmm.send_fax"                  , getValue("stat_send_fax"));

  if (getValue("pgcd_bill_form").equals("99")) setValue("acmm.bill_form","");
  else  setValue("acmm.bill_form" , getValue("pgcd_bill_form"));

  setValue("acmm.id_p_seqno"                , getValue("id_p_seqno"));
  setValue("acmm.corp_p_seqno"              , getValue("corp_p_seqno"));
  setValue("acmm.card_indicator"            , getValue("card_indicator"));
  setValue("acmm.combo_cash_limit"          , getValue("combo_cash_limit"));
  setValue("acmm.last_interest_date"        , getValue("last_interest_date"));
  setValue("acmm.combo_indicator"           , getValue("card_combo_indicator"));
  setValue("acmm.combo_oppost_date"         , getValue("card_oppost_date"));

  if (getValue("card_combo_indicator").equals("Y")) 
     setValue("acmm.combo_acct_no",getValue("combo_acct_no"));
  else setValue("acmm.combo_acct_no","");
   
  setValue("acmm.dc_curr_flag"              , "N");
  setValue("acmm.curr_pd_rating"            , getValue("curr_pd_rating"));
  setValue("acmm.unpost_installment"        , getValue("unpost_inst_fee"));

  setValue("acmm.his_combo_cash_amt"        , getValue("his_combo_cash_amt"));
  setValue("acmm.mod_time"                  , sysDate+sysTime);
  setValue("acmm.mod_pgm"                   , javaProgram);

  daoTable  = "cyc_acmm_"+ getValue("wday.stmt_cycle");

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertCycAcmmCurr() throws Exception
 {
  dateTime();
  extendField = "curr.";

  setValue("curr.p_seqno"                   , getValue("p_seqno"));
  setValue("curr.curr_code"                 , getValue("curr_code"));
  setValue("curr.acct_type"                 , getValue("acct_type"));
  setValue("curr.bill_curr_code"            , getValue("bill_curr_code"));
  setValue("curr.bill_sort_seq"             , getValue("parm_bill_sort_seq"));
  setValue("curr.autopay_acct_bank"         , getValue("autopay_acct_bank"));
  setValue("curr.autopay_acct_no"           , getValue("autopay_acct_no"));
  setValue("curr.autopay_dc_flag"           , getValue("autopay_dc_flag"));
  setValue("curr.mod_time"                  , sysDate+sysTime);
  setValue("curr.mod_pgm"                   , javaProgram);

  daoTable  = "cyc_acmm_curr_"+ getValue("wday.stmt_cycle");

  insertTable();

  return(0);
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.p_seqno,"
            + "decode(a.group_code,'','0000',a.group_code) as group_code,"
            + "decode(a.source_code,'','000000',a.source_code) as source_code,"
            + "a.current_code,"
            + "b.bill_form,"
            + "decode(b.bill_form_seq,'','99',b.bill_form_seq) as bill_form_seq,"
            + "a.curr_code,"
            + "a.card_note,"
            + "a.combo_indicator,"
            + "a.oppost_date";
  daoTable  = "crd_card a,ptr_group_code b";
  whereStr  = "WHERE a.stmt_cycle    = ? "
            + "AND   a.group_code    = b.group_code "
            + "AND   a.card_no       = a.major_card_no "
            + "and   (a.oppost_date   = '' "
            + " or    (a.oppost_date != '' "
            + "  and   a.oppost_date  <= ? ))"
            + "and   b.group_order !='' "
            + "ORDER BY a.p_seqno,a.current_code,to_number(group_order)";

  setString(1  , getValue("wday.stmt_cycle"));
  setString(2  , comm.nextMonthDate(businessDate, -6));

  int  n = loadTable();
  setLoadData("card.p_seqno");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
  void loadBilContract() throws Exception
 {
  extendField = "cont.";
  selectSQL = "a.p_seqno,"
            + "sum(a.unit_price*(a.install_tot_term - a.install_curr_term) + "
            + "    a.remd_amt +"
            + "    decode(install_curr_term,0,first_remd_amt,0)) as unpost_inst_fee ";
  daoTable  = "bil_contract a";
  whereStr  = "where p_seqno in (select p_seqno from act_acct_"+ getValue("wday.stmt_cycle") +") " 
            + "and   a.install_tot_term != a.install_curr_term "
            + "and   auth_code not in ('','N','REJECT','P','reject','LOAN') "
            + "and   ((post_cycle_dd > 0 " 
            + "  or    installment_kind ='F') "
            + " or    (post_cycle_dd = 0 " 
            + "  and   delv_confirm_flag='Y' " 
            + "  and   auth_code='DEBT')) " 
            + "group by a.p_seqno"
            ;

  int  n = loadTable();
  setLoadData("cont.p_seqno");

  showLogMessage("I","","Load bil_contract Count: ["+n+"]");
 }
// ************************************************************************
 void loadBilMerchant() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "mcht_no";
  daoTable  = "bil_merchant";
  whereStr  = "WHERE loan_flag not in ('N','C') ";

  int  n = loadTable();
  setLoadData("mcht.mcht_no");

  showLogMessage("I","","Load bil_merchant Count: ["+n+"]");
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
 int updateCycAcmm1() throws Exception
 {
  updateSQL = "chinese_name      = ?,"
            + "birthday          = ?,"
            + "chi_title         = ?,"
            + "staff_flag        = ?,"
            + "bank_branch_no    = ?,"
            + "credit_flag       = ?,"
            + "salary_code       = ?,"
          //+ "e_mail_addr       = ?,"
            + "nation            = ?,"
            + "home_area_code1   = ?,"
            + "home_tel_no1      = ?,"
            + "home_area_code2   = ?,"
            + "home_tel_no2      = ?,"
            + "office_area_code1 = ?,"
            + "office_tel_no1    = ?,"
            + "office_area_code2 = ?,"
            + "office_tel_no2    = ?,"
            + "cellar_phone      = ? ";
  daoTable  = "cyc_acmm_"+ getValue("wday.stmt_cycle") ;
  whereStr  = "where rowid = ?  ";

  setString(1 , getValue("chinese_name"));
  setString(2 , getValue("birthday"));
  setString(3 , getValue("chi_title"));
  setString(4 , getValue("staff_flag"));
  setString(5 , getValue("bank_branch_no"));
  setString(6 , getValue("credit_flag"));
  setString(7 , getValue("salary_code"));
//setString(8 , getValue("e_mail_addr"));
  setString(8 , getValue("nation"));
  setString(9, getValue("home_area_code1"));
  setString(10, getValue("home_tel_no1"));
  setString(11, getValue("home_area_code2"));
  setString(12, getValue("home_tel_no2"));
  setString(13, getValue("office_area_code1"));
  setString(14, getValue("office_tel_no1"));
  setString(15, getValue("office_area_code2"));
  setString(16, getValue("office_tel_no2"));
  setString(17, getValue("cellar_phone")); 
  setRowId( 18, getValue("rowid"));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int updateCycAcmm2() throws Exception
 {
  updateSQL = "chinese_name      = ?,"
          //+ "e_mail_addr       = ?,"
            + "e_mail_addr2      = ?,"
            + "e_mail_addr3      = ? ";
  daoTable  = "cyc_acmm_"+ getValue("wday.stmt_cycle") ;
  whereStr  = "where rowid = ?  ";

  setString(1 , getValue("chinese_name"));
//setString(2 , getValue("e_mail_addr")); 
  setString(2 , getValue("e_mail_addr2")); 
  setString(3 , getValue("e_mail_addr3")); 
  setRowId( 4 , getValue("rowid"));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int updateCycAcmm4() throws Exception
 {
  updateSQL = "dc_curr_flag = 'Y' ";
  daoTable  = "cyc_acmm_"+ getValue("wday.stmt_cycle");
  whereStr  = "where p_seqno = ?  ";

  setString(1 , getValue("p_seqno"));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int updateActAcctCurr() throws Exception
 {
  updateSQL = "bill_sort_seq = ? ";
  daoTable  = "act_acct_curr_"+ getValue("wday.stmt_cycle");
  whereStr  = "where p_seqno = ?  "
            + "and   curr_code      = ? "
            + "and   bill_sort_seq != ? ";

  setString(1 , getValue("parm_bill_sort_seq"));
  setString(2 , getValue("p_seqno"));
  setString(3 , getValue("curr_code"));
  setString(4 , getValue("parm_bill_sort_seq"));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int updateActAcctCurrNo901() throws Exception
 {
  updateSQL = "bill_sort_seq = '1' ";
  daoTable  = "act_acct_curr_"+ getValue("wday.stmt_cycle");
  whereStr  = "where p_seqno = ?  "
            + "and   curr_code      = ? "
            + "and   bill_sort_seq != '1' ";

  setString(1 , getValue("p_seqno"));
  setString(2 , getValue("curr_code"));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int updateActAcctCurr1() throws Exception
 {
  updateSQL = "bill_sort_seq = ? ";
  daoTable  = "act_acct_curr";
  whereStr  = "where p_seqno = ?  "
            + "and   curr_code      = ? "
            + "and   bill_sort_seq != ? ";

  setString(1 , getValue("parm_bill_sort_seq"));
  setString(2 , getValue("p_seqno"));
  setString(3 , getValue("curr_code"));
  setString(4 , getValue("parm_bill_sort_seq"));

  updateTable();

  return(0);
 }
// ************************************************************************                                                                 
 int checkCurrCodeNo901() throws Exception
 {
  int cnt1=0;
  setValue("cardno901.p_seqno",getValue("p_seqno"));
  cnt1 = getLoadData("cardno901.p_seqno");
  if (cnt1!=0) return(0);

  setValue("pyajno901.p_seqno",getValue("p_seqno"));
  cnt1 = getLoadData("pyajno901.p_seqno");
  if (cnt1!=0) return(0);

  setValue("billno901.p_seqno",getValue("p_seqno"));
  cnt1 = getLoadData("billno901.p_seqno");
  if (cnt1!=0) return(0);
                             
  return(1);
 }
// ************************************************************************                                                                 
 void  loadCrdCardNo901() throws Exception
 {
  extendField = "cardno901.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "crd_card";
  whereStr  = "WHERE curr_code   != '901' "
            + "AND   stmt_cycle   = ? "
            + "AND   current_code = '0' ";

  setString(1 , getValue("wday.stmt_cycle"));

  int  n = loadTable();

  setLoadData("cardno901.p_seqno");

  showLogMessage("I","","Load crd_card_no901 Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCycPyajNo901() throws Exception
 {
  extendField = "pyajno901.";
  selectSQL = "p_seqno,"
            + "curr_code";
  daoTable  = "cyc_pyaj";
  whereStr  = "where p_seqno in (select p_seqno from act_acct_"+ getValue("wday.stmt_cycle") +") "
            + "and   curr_code   != '901' "
            + "AND   (settle_date = '' "
            + "  or   settle_date = ?) "
            + "group by p_seqno,curr_code "
            + "order by p_seqno,curr_code ";

  setString(1 , businessDate);
  int  n = loadTable();

  setLoadData("pyajno901.p_seqno");
  setLoadData("pyajno901.p_seqno,pyajno901.curr_code");

  showLogMessage("I","","Load cyc_pyaj_no901 Count: ["+n+"]");
 }
// ************************************************************************
 void  loadBilBillNo901() throws Exception
 {
  extendField = "billno901.";
  selectSQL = "distinct p_seqno as p_seqno";
  daoTable  = "bil_bill";
  whereStr  = "WHERE p_seqno in (select p_seqno from act_acct_"+ getValue("wday.stmt_cycle") +") "
          //+ "AND   ((rsk_type    = '4' "
            + "AND   (rsk_type    = '4' "
            + " and    (billed_date = '' "
            + "  or     billed_date = ?)) "
          //+ " or    (rsk_post = 'O' "
          //+ "  and  substr(rsk_problem1_mark,2,2) >= '30')) "
            + "and   curr_code   != '901' "
            ;

  setString(1 , businessDate);

  int  n = loadTable();

  setLoadData("billno901.p_seqno");

  showLogMessage("I","","Load bil_bill_no901 Count: ["+n+"]");
 }
// ************************************************************************

                                                                                                          
}  // End of class FetchSample

