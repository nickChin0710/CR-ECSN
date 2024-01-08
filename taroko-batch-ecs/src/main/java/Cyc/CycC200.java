/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/05/04  V1.00.17  Allen Ho   cyc_C200                                   *
* 111/11/10  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Cyc;

import com.*;

import java.lang.*;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class CycC200 extends AccessDAO
{
 private final String PROGNAME = "現金回饋-雙幣卡外幣基金產生處理程式 111/11/10  V1.00.02";
 CommFunction comm = new CommFunction();
 CommDCFund comDCF = null;
 CommRoutine comr = null;


 String businessDate = "";
 String fundCode = "";
 String pSeqno = "";
 int    parmCnt=0;
 double begTranAmt = 0;
 String tranSeqno = "";
 final boolean DEBUG = false;

 long    totalCnt=0,updateCnt=0;
 int inti;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycC200 proc = new CycC200();
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
       showLogMessage("I","","PARM 2 : [fund_code]");
       showLogMessage("I","","PARM 3 : [p_seqno]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }
   if ( args.length >= 2 )
      { fundCode = args[1]; }
   if ( args.length == 3 )
      { pSeqno = args[2]; }
   
   if ( !connectDataBase() ) return(1);

   comDCF = new CommDCFund(getDBconnect(),getDBalias());
   comr   = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不執行");
       return(0);
      }

   showLogMessage("I","","this_acct_month["+getValue("this_acct_month")+"]");
   showLogMessage("I","","=========================================");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","刪除暫存資料");
   commitDataBase();
   if (fundCode.length()!=0)
      deleteMktDcBillData();
   else
      truncateTable("MKT_DC_BILL_DATA");

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadPtrCurrcode();
   loadMktMchtgpData();
   loadMktParmData();
   loadCrdCard();
   loadCrdCard1();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   if (selectCycDcFundParm()!=0) 
      {
       showLogMessage("I","","雙幣基金參數無設定!");
       return(1);
      }                
   showLogMessage("I","","****************************");
   showLogMessage("I","","處理 BIL_BILL .....");
   selectBilBill();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","****************************");
   showLogMessage("I","","處理 MKT_DC_BILL_DATA....");
   selectMktDcBillData();

   showLogMessage("I","","處理 ["+totalCnt+"] 筆");

   if (pSeqno.length()==0) finalProcess();
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
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
// ************************************************************************ 
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "stmt_cycle,"
            + "this_acct_month";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1, businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectCycDcFundParm() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "cyc_dc_fund_parm";
  whereStr  = "WHERE apr_flag      = 'Y' "
            + "AND   (stop_date = '' or stop_date > ? ) "
            + "AND   ? between decode(feedback_month_s,'','200001',feedback_month_s) "
            + "        and     decode(feedback_month_e,'','300012',feedback_month_e) "
            ;

  setString(1 , businessDate);
  setString(2 , getValue("wday.this_acct_month"));

  if (fundCode.length()!=0)
     {
      whereStr  = whereStr  
                + "AND   fund_code = ? ";
      setString(3 , fundCode);
     }

  parmCnt = selectTable();

  showLogMessage("I","","參數檢核筆數 ["+ parmCnt + "] 筆" );

  if (parmCnt==0) return(1);

  showLogMessage("I","","雙幣基金參數,符合條件有 "+parmCnt+" 筆");
  return(0);
 }
// ************************************************************************
 void selectBilBill() throws Exception
 {
  selectSQL = "major_card_no,"
            + "major_id_p_seqno,"
            + "p_seqno,"
            + "curr_code,"
            + "reference_no,"
            + "acct_type,"
            + "purchase_date,"
            + "issue_date,"
            + "decode(sign_flag,'+',dc_dest_amt,dc_dest_amt*-1) as dc_dest_amt,"
            + "group_code,"
            + "card_type,"
            + "mcht_category,"
            + "mcht_no,"
            + "source_code,"
            + "ecs_cus_mcht_no";
  daoTable  = "bil_bill";
  whereStr  = "where merge_flag != 'Y' "
            + "and rsk_type not in ('1','2','3') " 
            + "AND   acct_month  = ? "
            + "and   stmt_cycle  = ? "
            + "and   curr_code  != '901' "
            + "and   acct_code   = 'BL' "
            ;

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "AND   p_seqno   = ? ";
      setString(3 , pSeqno);
     }

  openCursor();

  int[] currCnt  = new int[parmCnt];

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     {
      for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;
     }

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    if (getValue("group_code").length()==0) setValue("group_code" , "0000");
    for (int inti=0;inti<parmCnt;inti++)
      {
       parmArr[inti][0]++;
       if (DEBUG)
          showLogMessage("I","","STEP 1  p_seqno ["
                               + getValue("p_seqno")
                               + "]  幣別["
                               + getValue("curr_code")
                               + "]  基金["
                               + getValue("parm.fund_code",inti)
                               + "]");

       if (!getValue("parm.curr_code",inti).equals(getValue("curr_code"))) continue;
       parmArr[inti][1]++;

       setValue("data_key", getValue("parm.fund_code",inti));

       if (selectMktParmData(getValue("group_code"),getValue("card_type"),
                                getValue("parm.group_card_sel",inti),"1",3)!=0) continue;
       parmArr[inti][2]++;

       if (selectMktParmData(getValue("group_code"),
                                getValue("parm.group_code_sel",inti),"2",2)!=0) continue;
       parmArr[inti][3]++;

       if (selectMktParmData(getValue("source_code"),
                                getValue("parm.source_code_sel",inti),"3",2)!=0) continue;
       parmArr[inti][4]++;

       if (selectMktParmData(getValue("mcht_no"),getValue("mcht_category"),
                                getValue("parm.merchant_sel",inti),"4",3)!=0) continue;
       parmArr[inti][5]++;

       if (selectMktMchtgpData(getValue("mcht_no"),getValue("mcht_category"),
                                   getValue("parm.mcht_group_sel",inti),"6")!=0) continue;
       parmArr[inti][6]++;
       
       if (selectMktMchtgpData(getValue("ecs_cus_mcht_no"),"",
               					   getValue("parm.platform_kind_sel",inti),"P")!=0) continue;
       parmArr[inti][7]++;

       if (DEBUG) showLogMessage("I",""," STEp 7");
       if (getValue("parm.new_hldr_cond",inti).equals("Y"))
          if (procCrdCard(inti)!=0) continue;
       parmArr[inti][8]++;

       if (getValue("parm.issue_cond",inti).equals("Y"))
          if (procCrdCard1(inti)!=0) continue;
       parmArr[inti][9]++;

       insertMktDcBillData(inti);
       currCnt[inti]++;
      }
   } 
  closeCursor();
  for (int inti=0;inti<parmCnt;inti++)
     {
      showLogMessage("I","","    ["+String.format("%03d",inti)
                           + "] 基金 [" + getValue("parm.fund_code",inti) 
                           + "]  ["+ currCnt[inti] + "] 筆" );
   
      if (fundCode.length()!=0) 
      for (int intk=0;intk<20;intk++)
        {
         if (parmArr[inti][intk]==0) continue;
         showLogMessage("I",""," 測試絆腳石 :["+inti+"]["+intk+"] = ["+parmArr[inti][intk]+"]");
        }
   
     }
  showLogMessage("I","","=========================================");  
  return;
 }
// ************************************************************************
 void loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "id_p_seqno,"
            + "a.card_no,"
            + "a.ori_issue_date,"
            + "a.group_code,"
            + "a.sup_flag,"
            + "a.oppost_date,"
            + "b.card_indicator";
  daoTable  = "crd_card a,ptr_acct_type b";
  whereStr  = "WHERE  a.stmt_cycle     = ? "
            + "AND    a.curr_code != '901'  "
            + "AND    a.acct_type = b.acct_type "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "AND   a.p_seqno   = ? ";
      setString(2 , pSeqno);
     }

  int  n = loadTable();
  setLoadData("card.id_p_seqno");
  setLoadData("card.card_no");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 int procCrdCard(int inti) throws Exception
 {
  setValue("card.card_no",getValue("major_card_no"));
  int cnt1 = getLoadData("card.card_no");

  String oriIssueDate = getValue("card.ori_issue_date");

  setValue("card.id_p_seqno",getValue("major_id_p_seqno"));
      cnt1 = getLoadData("card.id_p_seqno");

  String lastDate = "";
  for ( int intm=0; intm<cnt1; intm++ )
      {
      if (getValue("card.group_code",intm).length()==0) setValue("card.group_code" , "0000",intm);

       if (getValue("card.card_no",intm).equals(getValue("major_card_no"))) continue;

       if (getValue("card.ori_issue_date",intm).compareTo(getValue("issue_date"))>0) continue;

       if (getValue("card.oppost_date",intm).length()==0) return(1);

       lastDate = comm.nextNDate(oriIssueDate,
                                  getValueInt("parm.new_hldr_days",inti)*-1);
       if (getValue("card.oppost_date",intm).length()!=0)
          if (getValue("card.oppost_date",intm).compareTo(lastDate)<0) continue;

       if ((!getValue("parm.new_hldr_card" ,inti).equals("Y"))&&
           (getValue("card.sup_flag",intm).equals("0"))) continue;

       if ((!getValue("parm.new_hldr_sup"  ,inti).equals("Y"))&&
           (getValue("card.sup_flag",intm).equals("1"))) continue;

       if (getValue("parm.new_group_cond",inti).equals("Y"))
          {
           if (selectMktParmData(getValue("card.group_code",intm),"1","5",2)==0) return(1);
          }
       else if (!getValue("card.card_indicator",intm).equals("1")) 
          {
           return(1);
          }
      }

  return(0);
 }
// ************************************************************************
 void loadCrdCard1() throws Exception
 {
  extendField = "car1.";
  selectSQL = "card_no,"
            + "ori_issue_date";
  daoTable  = "crd_card";
  whereStr  = "WHERE stmt_cycle     = ? "
            + "AND   curr_code != '901'  "
            ;

  setString(1 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "AND   p_seqno   = ? ";
      setString(2 , pSeqno);
     }

  int  n = loadTable();
  setLoadData("car1.card_no");

  showLogMessage("I","","Load crd_card_1 Count: ["+n+"]");
 }
// ************************************************************************
 int procCrdCard1(int inti) throws Exception
 {
  setValue("car1.card_no",getValue("major_card_no"));
  int cnt1 = getLoadData("car1.card_no");

  String nextMonth = "";
  String nextDate  = "";
  for ( int intm=0; intm<cnt1; intm++ )
      {
   
       if (DEBUG)   
          {
           showLogMessage("I",""," STEp A");
           showLogMessage("I","","    bil_issue_date ["+getValue("issue_date")+"]");
           showLogMessage("I","","    ori_issue_date ["+getValue("car1.ori_issue_date",intm)+"]");
           showLogMessage("I","","    issue_date_s   ["+getValue("parm.issue_date_s",inti)+"]");
           showLogMessage("I","","    issue_date_e   ["+getValue("parm.issue_date_e",inti)+"]");
          }
   
       if ((getValue("car1.ori_issue_date",intm).compareTo(getValue("parm.issue_date_s",inti))<0)||
           (getValue("car1.ori_issue_date",intm).compareTo(getValue("parm.issue_date_e",inti))>0)) continue;

  
       if (DEBUG)   
          {
           showLogMessage("I",""," STEp A1 ");
           showLogMessage("I","","    bil_issue_date ["+getValue("issue_date")+"]");
           showLogMessage("I","","    ori_issue_date ["+getValue("car1.ori_issue_date",intm)+"]");
           showLogMessage("I","","    issue_date_s   ["+getValue("parm.issue_date_s",inti)+"]");
           showLogMessage("I","","    issue_date_e   ["+getValue("parm.issue_date_e",inti)+"]");
          }
  
       if (getValue("parm.issue_flag" ,inti).equals("1"))
          {
           nextMonth = comm.nextMonth(getValue("car1.ori_issue_date",intm),getValueInt("parm.issue_num_1" ,inti));
           if (getValue("purchase_date").substring(0,6).compareTo(nextMonth)>0) continue;
          }

       if (getValue("parm.issue_flag" ,inti).equals("2"))
          {
           nextMonth = comm.nextMonth(getValue("car1.ori_issue_date",intm),getValueInt("parm.issue_num_2" ,inti));
           if (getValue("purchase_date").substring(0,6).compareTo(nextMonth)<0) continue;
          }

       if (getValue("parm.issue_flag" ,inti).equals("3"))
          {
           nextDate  = comm.nextNDate(getValue("car1.ori_issue_date",intm),getValueInt("parm.issue_num_3" ,inti));
           if (getValue("purchase_date").compareTo(nextDate)>0) continue;
          }

       return(0);
      }                

  return(1);
 }
// ************************************************************************
 void selectMktDcBillData() throws Exception
 {
  selectSQL = "p_seqno,"
            + "curr_code,"
            + "fund_code,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type, "
            + "sum(dc_dest_amt) as dc_dest_amt";
  daoTable  = "mkt_dc_bill_data";
  whereStr  = "where 1 = 1 "
            ;

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr  
                + "AND   p_seqno   = ? ";
      setString(1 , pSeqno);
     }
  whereStr  = whereStr  
            + "group by p_seqno,curr_code,fund_code "
            + "having sum(dc_dest_amt) != 0 ";

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;
    for (int inti=0;inti<parmCnt;inti++)
      {
       if (!getValue("parm.curr_code",inti).equals(getValue("curr_code"))) continue;
       if (!getValue("parm.fund_code",inti).equals(getValue("fund_code"))) continue;

       double[] purchaseAmtS = new double[10];
       double[] purchaseAmtE = new double[10];
       double[] feedbackRate  = new double[10];

       purchaseAmtS[1] = getValueDouble("parm.purchase_amt_s1",inti);
       purchaseAmtS[2] = getValueDouble("parm.purchase_amt_s2",inti);
       purchaseAmtS[3] = getValueDouble("parm.purchase_amt_s3",inti);
       purchaseAmtS[4] = getValueDouble("parm.purchase_amt_s4",inti);
       purchaseAmtS[5] = getValueDouble("parm.purchase_amt_s5",inti);
       purchaseAmtS[6] = getValueDouble("parm.purchase_amt_s6",inti);

       purchaseAmtE[1] = getValueDouble("parm.purchase_amt_e1",inti);
       purchaseAmtE[2] = getValueDouble("parm.purchase_amt_e2",inti);
       purchaseAmtE[3] = getValueDouble("parm.purchase_amt_e3",inti);
       purchaseAmtE[4] = getValueDouble("parm.purchase_amt_e4",inti);
       purchaseAmtE[5] = getValueDouble("parm.purchase_amt_e5",inti);
       purchaseAmtE[6] = getValueDouble("parm.purchase_amt_e6",inti);

       feedbackRate[1] = getValueDouble("parm.feedback_rate_1",inti);
       feedbackRate[2] = getValueDouble("parm.feedback_rate_2",inti);
       feedbackRate[3] = getValueDouble("parm.feedback_rate_3",inti);
       feedbackRate[4] = getValueDouble("parm.feedback_rate_4",inti);
       feedbackRate[5] = getValueDouble("parm.feedback_rate_5",inti);
       feedbackRate[6] = getValueDouble("parm.feedback_rate_6",inti);

       double tempDouble = getValueDouble("dc_dest_amt");
       if (tempDouble <0) tempDouble = tempDouble * -1.0;

       int intk=0;

       for (intk=1;intk<6;intk++)
         if ((tempDouble >= purchaseAmtS[intk])&&
             (tempDouble <= purchaseAmtE[intk])) break;

       if (intk>=6) continue;

       tempDouble = getValueDouble("dc_dest_amt")*feedbackRate[intk]/100.0;
       if (pSeqno.length()!=0)
          {
           showLogMessage("I","","20210409 0001  : ["+ getValueDouble("dc_dest_amt") + "]");
           showLogMessage("I","","20210409 0002  : ["+ feedbackRate[intk] + "]");
           showLogMessage("I","","20210409 0003  : ["+ getValueDouble("dc_dest_amt")*feedbackRate[intk] + "]");
           showLogMessage("I","","20210409 0004  : ["+ getValueDouble("dc_dest_amt")*feedbackRate[intk]/100.0 + "]");
           showLogMessage("I","","20210409 0005  : ["+ tempDouble + "]");
           showLogMessage("I","","20210409 0005  : ["+ commCurrAmt(getValue("curr_code"),tempDouble,0) + "]");
          }

       if (tempDouble ==0) continue;
 
       begTranAmt = commCurrAmt(getValue("curr_code"),tempDouble,0);

       if (pSeqno.length()!=0)
           showLogMessage("I","","20210409 A001  : ["+ begTranAmt + "]");

       insertCycDcFundDtl(inti);
//       insertCycVouchData();
       comDCF.modPgm = javaProgram;
       comDCF.dcfundFunc(tranSeqno);
      }

   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int insertMktDcBillData(int inti) throws Exception
 {
  extendField = "bdtl.";
  setValue("bdtl.curr_code"         , getValue("curr_code"));
  setValue("bdtl.fund_code"         , getValue("parm.fund_code",inti)); 
  setValue("bdtl.acct_type"         , getValue("acct_type"));
  setValue("bdtl.p_seqno"           , getValue("p_seqno"));
  setValue("bdtl.id_p_seqno"        , getValue("major_id_p_seqno"));
  setValue("bdtl.reference_no"      , getValue("reference_no"));
  setValue("bdtl.dc_dest_amt"      , getValue("dc_dest_amt"));

  daoTable  = "mkt_dc_bill_data";

  insertTable();

  return(0);
 }
// ************************************************************************
// int insertCycVouchData() throws Exception
// {
//  extendField = "vocu.";
//  setValue("vocu.create_date"          , sysDate);
//  setValue("vocu.create_time"          , sysTime);
//  setValue("vocu.business_date"        , businessDate);
//  setValue("vocu.curr_code"            , getValue("curr_code"));
//  setValue("vocu.p_seqno"              , getValue("p_seqno"));
//  setValue("vocu.acct_type"            , getValue("acct_type"));
//  setValue("vocu.payment_type"         , getValue("fund_code").substring(0,4)); 
//  setValueDouble("vocu.vouch_amt"      , Math.abs(begTranAmt));
//  setValueDouble("vocu.d_vouch_amt"    , 0);
//
//  if (begTranAmt >0)
//     setValue("vocu.vouch_data_type"      , "1");
//  else
//     setValue("vocu.vouch_data_type"      , "7");
//  setValue("vocu.acct_code"            , "BL"); 
//  setValue("vocu.src_pgm"              , javaProgram); 
//  setValue("vocu.proc_flag"            , "N");
//  setValue("vocu.mod_time"             , sysDate+sysTime);
//  setValue("vocu.mod_pgm"              , javaProgram);
//
//  daoTable  = "cyc_vouch_data";
//
//  insertTable();
//
//  return(0);
// }
// ************************************************************************
 int insertCycDcFundDtl(int inti) throws Exception
 {
  tranSeqno = comr.getSeqno("ecs_dbmseq");
  dateTime();
  extendField = "cdfd.";
  setValue("cdfd.tran_date"            , sysDate);
  setValue("cdfd.tran_time"            , sysTime);
  setValue("cdfd.tran_seqno"           , tranSeqno);
  setValue("cdfd.fund_code"            , getValue("fund_code"));
  setValue("cdfd.fund_name"            , getValue("parm.fund_name",inti));
  setValue("cdfd.curr_code"            , getValue("curr_code"));
  setValue("cdfd.acct_type"            , getValue("acct_type"));
  if (begTranAmt >0)
     {
      setValue("cdfd.mod_desc"         , "");
      setValue("cdfd.tran_code"        , "1");
     }
  else
     {
      setValue("cdfd.mod_desc"         , "回饋時負項交易金額大於正向金額");
      setValue("cdfd.tran_code"        , "1");
     }
  setValue("cdfd.effect_e_date"        , "");
  if (getValueInt("parm.effect_months",inti)>0)
     setValue("cdfd.effect_e_date"     , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  setValueDouble("cdfd.beg_tran_amt"   , begTranAmt);
  setValueDouble("cdfd.end_tran_amt"   , begTranAmt);
  setValue("cdfd.mod_memo"             , "");
  setValue("cdfd.p_seqno"              , getValue("p_seqno"));
  setValue("cdfd.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cdfd.acct_date"            , businessDate);
  setValue("cdfd.tran_pgm"             , javaProgram);
  setValue("cdfd.apr_flag"             , "Y");
  setValue("cdfd.apr_user"             , javaProgram);
  setValue("cdfd.apr_date"             , sysDate);
  setValue("cdfd.crt_user"             , javaProgram);
  setValue("cdfd.crt_date"             , sysDate);
  setValue("cdfd.mod_user"             , javaProgram); 
  setValue("cdfd.mod_time"             , sysDate+sysTime);
  setValue("cdfd.mod_pgm"              , javaProgram);

  daoTable  = "cyc_dc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
  int truncateTable(String tableName) throws Exception
 {
  String truncateSQL = "TRUNCATE TABLE "+ tableName + " "
                     + "IGNORE DELETE TRIGGERS "
                     + "DROP STORAGE "
                     + "IMMEDIATE "
                     ;

  showLogMessage("I","","Truncate Table : ["+ tableName + "]");

  executeSqlCommand(truncateSQL);

  return(0);
 }
// ************************************************************************
 void loadMktParmData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_parm_data";
  whereStr  = "WHERE TABLE_NAME = 'CYC_DC_FUND_PARM' "
            + "order by data_key,data_type,data_code ";

  int  n = loadTable();
  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_parm_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktParmData(String col1, String sel, String dataType, int dataNum) throws Exception
 {
  return selectMktParmData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1, String col2, String sel, String dataType, int dataNum) throws Exception
 {
  return selectMktParmData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1, String col2, String col3, String sel, String dataType, int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
     }

  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (dataNum==2)
        {
         if ((col1.length()!=0)&&
             (getValue("data.data_code",intm).length()!=0)&&
          (!getValue("data.data_code",intm).equals(col1))) continue;

         if ((col2.length()!=0)&&
             (getValue("data.data_code2",intm).length()!=0)&&
          (!getValue("data.data_code2",intm).equals(col2))) continue;

         if ((col3.length()!=0)&&
             (getValue("data.data_code3",intm).length()!=0)&&
          (!getValue("data.data_code3",intm).equals(col3))) continue;
        }
     else
        {
         if (col2.length()!=0)
            {
             if ((getValue("data.data_code2",intm).length()!=0)&&
                 (!getValue("data.data_code2",intm).equals(col2))) continue;
            }
         if (col3.length()!=0)
            {
             if ((getValue("data.data_code3",intm).length()!=0)&&
                 (!getValue("data.data_code3",intm).equals(col3))) continue;
            }
        }

     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 void loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,mkt_parm_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'CYC_DC_FUND_PARM' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  in ('6','P') "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();
  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  showLogMessage("I","","Load mkt_fstpgp_data Count: ["+n+"]");
 }
// ************************************************************************
	int selectMktMchtgpData(String col1, String col2, String sel, String dataType) throws Exception {
		if (sel.equals("0"))
			return (0);

		setValue("mcht.data_key", getValue("data_key"));
		setValue("mcht.data_type", dataType);
		setValue("mcht.data_code", col1);

		int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
		int okFlag = 0;
		for (int inti = 0; inti < cnt1; inti++) {
			if ("P".equals(dataType)) {
				okFlag = 1;
				break;
			} else {
				if ((getValue("mcht.data_code2", inti).length() == 0)
						|| ((getValue("mcht.data_code2", inti).length() != 0)
								&& (getValue("mcht.data_code2", inti).equals(col2)))) {
					okFlag = 1;
					break;
				}
			}

		}

		if (sel.equals("1")) {
			if (okFlag == 0)
				return (1);
			return (0);
		} else {
			if (okFlag == 0)
				return (0);
			return (1);
		}
	}
// ************************************************************************
 double commCurrAmt(String currCode, double val, int rnd) throws Exception
 {

  setValue("pcde.curr_code" , currCode);
  int cnt1 = getLoadData("pcde.curr_code");

  double calVal=1;
  for (int inti=0;inti<getValueInt("pcde.curr_amt_dp");inti++)
     calVal = calVal * 10.0;

  val = val * calVal;
  val = Math.round(val);

  BigDecimal currAmt = new BigDecimal(val).divide(new BigDecimal(String.format("%.0f",calVal)));

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
  whereStr  = "where bill_sort_seq!=''";

  int  n = loadTable();

  setLoadData("pcde.curr_code");

  showLogMessage("I","","Load ptr_currcode Count: ["+n+"]");
 }
// ************************************************************************
 void deleteMktDcBillData() throws Exception
 {
  daoTable  = "mkt_dc_bill_data";
  whereStr  = "where  fund_code    = ? " 
            ;

  setString(1, fundCode);

   if (pSeqno.length()!=0) 
      {
       whereStr  = whereStr  
                 + "and p_seqno = ? ";  
       setString(2, pSeqno);
      }

  int  n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete ["+ fundCode +"] mkt_dc_bill_data [" + n + "] records");

  return;
 }
// ************************************************************************


}  // End of class FetchSample

