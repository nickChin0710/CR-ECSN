/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/04/13  V1.00.24  Allen Ho   cyc_c100                                   *
* 111-11-11  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                           *
* 112-03-31  V1.00.02  Zuwei Su   參數篩選一般消費之條件                                                                          *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA320 extends AccessDAO
{
 private final String PROGNAME = "台幣基金-指定繳款方式回饋基金計算處理程式 111-11-11  V1.00.01";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine comr = null;

 String businessDate   = "";
 String tranSeqno = "";
 String pSeqno  ="";

 long    totalCnt=0;
 int parmCnt=0;
 boolean DEBUG =true;

 int[] dInt = {0,0,0,0};
 int paymentAmt = 0,insertCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA320 proc = new CycA320();
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
       showLogMessage("I","","PARM 2 : [P_SEQNO]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   if ( args.length == 2 )
      { pSeqno = args[1]; }
   
   if ( !connectDataBase() ) exitProgram(1);
   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理NFC參數資料...");
   if (selectMktNfcParm()!=0)
      {
       showLogMessage("I","","參數設定不產生基金 !");
       return(0);
      }


   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理暫存檔資料..");
   loadBilBill();
   loadMktParmData();
   loadMktMchtgpData();
   insertCnt=0;
   showLogMessage("I","","--------------------------.");
   showLogMessage("I","","處理付款與調整資料檔資料(cyc_pyaj)...");
   selectCycPyaj();
   showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );
   if (insertCnt==0) 
      {
       showLogMessage("I","","本日無資料需處理.");
       return(0);
      }
   showLogMessage("I","","--------------------------.");
   showLogMessage("I","","處理暫存檔資料..");
   loadActAcct();
   showLogMessage("I","","處理NFC明細資料...");
   selectMktNfcDetail();
   insertCnt=0;
   showLogMessage("I","","  符合回饋資格  筆數["+totalCnt+"]");
   showLogMessage("I","","  實際回饋      筆數["+updateCnt+"](其他回饋金<1元)");;
   showLogMessage("I","","--------------------------.");
   showLogMessage("I","","清除不符合參數資料 .....");
   deleteMktNfcDetail();
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
  selectSQL = "stmt_cycle";
  daoTable  = "ptr_workday";
  whereStr  = "where stmt_cycle = ? ";

  setString(1 , businessDate.substring(6,8));

  int recCnt = selectTable();

  setValue("wday.this_acct_month" , businessDate.substring(0,6));
  setValue("wday.last_acct_month" , comm.lastMonth(businessDate,1));

  if ( notFound.equals("Y") )
     {
      setValue("wday.stmt_cycle" , "");
      return(1);
     }

  setValue("wday.stmt_cycle"      , businessDate.substring(6,8));
  setValue("wday.this_close_date" , businessDate);
  setValue("wday.last_close_date" , comm.lastMonth(businessDate,1)
                                  + businessDate.substring(6,8)); 

  return(0);
 }
// ************************************************************************
 void  selectCycPyaj() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "payment_type,"
            + "payment_amt";
  daoTable  = "cyc_pyaj";
  whereStr  = "WHERE class_code      = 'P' " 
            + "AND   decode(curr_code,'','901',curr_code) = '901' "
            + "AND   settle_flag     = 'B' "
            + "AND   settle_date     = ? "
            + "AND   stmt_cycle      = ? "
            + "AND   payment_amt     > 0 "
            ;

  setString(1 , businessDate);
  setString(2 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and p_seqno = ? ";
                 
      setString(3 , pSeqno);
     }

  whereStr  = whereStr 
            + "order by p_seqno ";

  openCursor();

  double[][] parmArr = new double [parmCnt][20];
  for (int inti=0;inti<parmCnt;inti++)
     {
      for (int intk=0;intk<20;intk++) parmArr[inti][intk]=0;
     }

  for (int inti=0;inti<parmCnt;inti++)
    setValue("parm.datacnt" , "0" , inti);

  String acqId = "";
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    setValue("bill.p_seqno",getValue("p_seqno"));
    int cnt2 = getLoadData("bill.p_seqno");

    for (int inti=0;inti<parmCnt;inti++)
      {

       setValue("data_key" , getValue("parm.fund_code",inti));

       parmArr[inti][1]++;

       if (selectMktParmData(getValue("payment_type"),
                                getValue("parm.payment_sel",inti),"3",2)!=0) continue;

       parmArr[inti][2]++;

       setValueDouble("purchase_amt" , 0);
   
       for (int intk=0;intk<cnt2;intk++)
           {
            if ((!getValue("parm.bl_cond",inti).equals("Y"))&&(getValue("bill.acct_code",intk).equals("BL"))) continue;
            if ((!getValue("parm.it_cond",inti).equals("Y"))&&(getValue("bill.acct_code",intk).equals("IT"))) continue;
            if ((!getValue("parm.ca_cond",inti).equals("Y"))&&(getValue("bill.acct_code",intk).equals("CA"))) continue;
            if ((!getValue("parm.id_cond",inti).equals("Y"))&&(getValue("bill.acct_code",intk).equals("ID"))) continue;
            if ((!getValue("parm.ao_cond",inti).equals("Y"))&&(getValue("bill.acct_code",intk).equals("AO"))) continue;
            if ((!getValue("parm.ot_cond",inti).equals("Y"))&&(getValue("bill.acct_code",intk).equals("OT"))) continue;

       parmArr[inti][3]++;

            if (selectMktParmData(getValue("bill.group_code",intk),
                                     getValue("parm.group_code_sel",inti),"2",2)!=0) continue;

            if (selectMktParmData(getValue("bill.mcht_category",intk),
                                     getValue("parm.mcc_code_sel",inti),"5",2)!=0) continue;
       parmArr[inti][4]++;

            acqId = "";
            if (getValue("acq_member_id").length()!=0)
               acqId = comm.fillZero(getValue("bill.acq_member_id,intk"),8);

            if (selectMktParmData(getValue("bill.mcht_no",intk),acqId,
                                     getValue("parm.merchant_sel",inti),"4",3)!=0) continue;

       parmArr[inti][5]++;

            if (selectMktParmData(getValue("bill.group_code",intk),getValue("bill.card_type",intk),
                                     getValue("parm.group_card_sel",inti),"1",2)!=0) continue;

       parmArr[inti][6]++;

            if (selectMktMchtgpData(getValue("bill.mcht_no",intk), acqId,
                                       getValue("parm.mcht_group_sel",inti),"6")!=0) continue;

       parmArr[inti][7]++;
       
       if ((selectMktMchtgpData(getValue("bill.ECS_CUS_MCHT_NO",intk),"" , 
               getValue("parm.platform_kind_sel",inti) ,"P" )!=0) 
               && selectMktParmData(getValue("bill.ECS_CUS_MCHT_NO",intk), "",
                       getValue("parm.platform_kind_sel",inti),"P",2)!=0 ) {
           continue;
       }

       parmArr[inti][8]++;

            setValueDouble("purchase_amt" , getValueDouble("purchase_amt") + getValueDouble("bill.dest_amt",intk));
           }

       parmArr[inti][9]++;

       if (getValueDouble("purchase_amt")==0) continue;

       parmArr[inti][10]++;

       setValueDouble("purchase_amt" , getValueDouble("purchase_amt"));

       if (insertMktNfcDetail(inti)!=0)
          updateMktNfcDetail(inti);

       setValueInt("parm.datacnt" , getValueInt("parm.datacnt")+1  , inti);
      }


    if (totalCnt%50000==0)
       {
        showLogMessage("I","","    Proc Records :  "+totalCnt);
       }   
   } 
  closeCursor();

  showLogMessage("I","","=========================================");
  showLogMessage("I","","處理筆數 ["+ totalCnt + "] 筆" );
   
  for (int inti=0;inti<parmCnt;inti++)
     {
      showLogMessage("I","","    ["+String.format("%03d",inti+1)
                           +"] 基金 ["+ getValue("parm.fund_code",inti)
                           +"] 筆數["+getValueInt("parm.datacnt")+"]");
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
 void  selectMktNfcDetail() throws Exception
 {
  selectSQL = "p_seqno,"
            + "fund_code,"
            + "acct_type,"
            + "purchase_amt,"
            + "rowid as rowid";
  daoTable  = "mkt_nfc_detail";
  whereStr  = "where pay_month   = ? "
            + "and   proc_flag   = 'N' "
            ;

  setString(1 , getValue("wday.this_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and p_seqno = ? ";
                 
      setString(2 , pSeqno);
     }

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    for (int inti=0;inti<parmCnt;inti++)
      {

       if (!getValue("parm.fund_code",inti).equals(getValue("fund_code"))) continue;

       setValue("acct.p_seqno",getValue("p_seqno"));
       int cnt1 = getLoadData("acct.p_seqno");
       if (cnt1==0) 
          showLogMessage("I","","p_seqno : ["+getValue("p_seqno")+"] not found in act_acct");
       setValue("id_p_seqno" , getValue("acct.id_p_seqno"));

       
       int newAmt = (int)Math.round(getValueInt("purchase_amt")*
                                     getValueDouble("parm.feedback_rate",inti)/100.0);

       if (newAmt==0) continue;

      if (getValueInt("parm.feedback_lmt",inti)!=0)
         {
           if (newAmt>0)
              {
               if (newAmt>getValueInt("parm.feedback_lmt",inti))
                  newAmt= getValueInt("parm.feedback_lmt",inti);
              }
           else
              {
               if (newAmt<getValueInt("parm.feedback_lmt",inti)*-1)
                  newAmt= getValueInt("parm.feedback_lmt",inti)*-1;
              }
          }

       setValueInt("beg_tran_amt", newAmt);

       setValue("res_s_month"      , "");
       setValue("effect_e_date"   , "");
       if (getValueInt("parm.effect_months",inti)>0)
          setValue("effect_e_date"    , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
       setValueInt("end_tran_amt"  , getValueInt("beg_tran_amt"));
       setValueInt("res_tran_amt"  , 0);
       setValueInt("res_total_cnt" , 0);

       insertMktCashbackDtl(inti);

//        if (newAmt>0) insertCycFundDtl(1,inti);
//        else insertCycFundDtl(2,inti);

       updateMktNfcDetail1(inti);
      }

   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int selectMktNfcParm() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "mkt_nfc_parm";
  whereStr  = "WHERE apr_flag      = 'Y' "
            + "AND   (stop_flag  = 'N' "
            + " OR    (stop_flag = 'Y' "
            + "  and   ? < stop_date)) "
            + "and  ? between fund_crt_date_s and fund_crt_date_e "
            ;  

  setString(1,businessDate);
  setString(2,businessDate);

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  for (int inti=0;inti<parmCnt;inti++)
      {
       deleteMktNfcDetail0(inti);
      }
  showLogMessage("I","","參數檢核筆數 ["+ parmCnt + "] 筆" );

  return(0);
 }
// ************************************************************************
 int insertMktCashbackDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("mkt_modseq");
  dateTime();

  setValue("mcdl.tran_date"            , sysDate);
  setValue("mcdl.tran_time"            , sysTime);
  setValue("mcdl.fund_code"            , getValue("parm.fund_code",inti));
  setValue("mcdl.fund_name"            , getValue("parm.fund_name",inti));
  setValue("mcdl.p_seqno"              , getValue("p_seqno"));
  setValue("mcdl.acct_type"            , getValue("acct_type"));
  setValue("mcdl.id_p_seqno"           , getValue("id_p_seqno"));
  if (getValueInt("beg_tran_amt")>0)
     {
      setValue("mcdl.tran_code"            , "2");
      setValue("mcdl.mod_desc"             , "NFC回饋基金");
     }
  else
     {
      setValue("mcdl.tran_code"            , "2");
      setValue("mcdl.mod_desc"             , "NFC回饋基金系統調整");
     }
  setValue("mcdl.tran_pgm"             , javaProgram);

  setValueInt("mcdl.beg_tran_amt"      , getValueInt("beg_tran_amt"));
  setValueInt("mcdl.end_tran_amt"      , getValueInt("end_tran_amt"));
  setValueInt("mcdl.res_tran_amt"      , getValueInt("res_tran_amt"));
  setValueInt("mcdl.res_total_cnt"     , getValueInt("res_total_cnt"));
  setValueInt("mcdl.res_tran_cnt"      , 0);
  setValue("mcdl.res_s_month"          , getValue("res_s_month"));
  setValue("mcdl.res_upd_date"         , "");
  setValue("mcdl.effect_e_date"        , "");
  if (getValueInt("parm.effect_months",inti)>0)
     setValue("mcdl.effect_e_date"        , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  setValue("mcdl.tran_seqno"           , tranSeqno);
  setValue("mcdl.acct_month"           , businessDate.substring(0,6));
  setValue("mcdl.acct_date"            , businessDate);
  setValue("mcdl.mod_memo"             , "");
  setValue("mcdl.mod_reason"           , "");
  setValue("mcdl.case_list_flag"       , "N");
  setValue("mcdl.crt_user"             , javaProgram);
  setValue("mcdl.crt_date"             , sysDate);
  setValue("mcdl.apr_date"             , sysDate);
  setValue("mcdl.apr_user"             , javaProgram);
  setValue("mcdl.apr_flag"             , "Y");
  setValue("mcdl.mod_user"             , javaProgram); 
  setValue("mcdl.mod_time"             , sysDate+sysTime);
  setValue("mcdl.mod_pgm"              , javaProgram);

  extendField = "mcdl.";
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void deleteMktNfcDetail() throws Exception
 {
  daoTable  = "mkt_nfc_detail";
  whereStr  = "WHERE pay_month = ? "
            + "and   proc_flag  = 'N' ";

  setString(1,getValue("wday.this_acct_month"));

  int n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete mkt_nfc_detail [" + n + "] records");

  return;
 }
// ************************************************************************
 void deleteMktNfcDetail0(int inti) throws Exception
 {
  daoTable  = "mkt_nfc_detail";
  whereStr  = "WHERE pay_month = ? "
            + "and   fund_code = ? "
            + "and   proc_date = ? "
            ;

  setString(1 , getValue("wday.this_acct_month"));
  setString(2 , getValue("parm.fund_code",inti));
  setString(3 , businessDate);

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and p_seqno = ? ";
                 
      setString(4 , pSeqno);
     }


  int n = deleteTable();

  if (n>0) 
     showLogMessage("I","","Delete ["+getValue("parm.fund_code",inti)+"] mkt_nfc_detail_0 [" + n + "] records");

  return;
 }
// ************************************************************************
 void updateMktNfcDetail1(int inti) throws Exception
 {
  dateTime();

  updateSQL = "proc_flag = 'Y',"
            + "proc_date = ?,"
            + "fund_amt  = ?,"
            + "mod_pgm   = ?, "
            + "mod_time  = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "mkt_nfc_detail";
  whereStr  = "WHERE rowid     = ? ";

  setString(1,businessDate);
  setInt(2    , getValueInt("beg_tran_amt"));
  setString(3 , javaProgram);
  setString(4 , sysDate+sysTime);
  setRowId(5  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
// int insertCycFundDtl(int numType,int inti) throws Exception
// {
//  dateTime();
//
//  setValue("mfdl.business_date"        , businessDate);
//  setValue("mfdl.create_date"          , sysDate);
//  setValue("mfdl.create_time"          , sysTime);
//  setValue("mfdl.id_p_seqno"           , getValue("id_p_seqno"));
//  setValue("mfdl.p_seqno"              , getValue("p_seqno"));
//  setValue("mfdl.acct_type"            , getValue("acct_type"));
//  setValue("mfdl.card_no"              , "");
//  setValue("mfdl.curr_code"            , "901");
//  setValue("mfdl.fund_code"            , getValue("parm.fund_code",inti).substring(0,4));
//  setValue("mfdl.vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
//  if (numType==1)
//     {
//      setValue("mfdl.tran_code"            , "1");
//      setValue("mfdl.cd_kind"              , "H001"); 
//     }
//  else
//     {
//      setValue("mfdl.tran_code"            , "3");
//      setValue("mfdl.cd_kind"              , "H003");
//     }
//  setValue("mfdl.memo1_type"           , "1");   /* fund_code 必須有值 */
//  setValueInt("mfdl.fund_amt"          , Math.abs(getValueInt("beg_tran_amt")));
//  setValueInt("mfdl.other_amt"         , 0);
//  setValue("mfdl.proc_flag"            , "N");
//  setValue("mfdl.proc_date"            , "");
//  setValue("mfdl.execute_date"         , businessDate);
//  setValueInt("mfdl.fund_cnt"          , 1);
//  setValue("mfdl.mod_user"             , javaProgram); 
//  setValue("mfdl.mod_time"             , sysDate+sysTime);
//  setValue("mfdl.mod_pgm"              , javaProgram);
//
//  extendField = "mfdl.";
//  daoTable  = "cyc_fund_dtl";
//
//  insertTable();
//
//  insertCnt++;
//  return(0);
// }
// ************************************************************************
 int insertMktNfcDetail(int inti) throws Exception
 {
  dateTime();

  setValue("mndl.fund_code"            , getValue("parm.fund_code",inti));
  setValue("mndl.p_seqno"              , getValue("p_seqno"));
  setValue("mndl.acct_type"            , getValue("acct_type"));
  setValue("mndl.pay_month"            , getValue("wday.this_acct_month"));
  setValue("mndl.purchase_month"       , getValue("wday.last_acct_month"));
  setValueDouble("mndl.pay_amt"        , getValueDouble("payment_amt"));
  setValueDouble("mndl.purchase_amt"   , getValueDouble("purchase_amt"));
  setValue("mndl.proc_flag"            , "N");
  setValue("mndl.mod_time"             , sysDate+sysTime);
  setValue("mndl.mod_pgm"              , javaProgram);

  extendField = "mndl.";
  daoTable  = "mkt_nfc_detail";

  insertTable();
                          
   if ( dupRecord.equals("Y") ) return(1);
   insertCnt++;
  return(0);
 }
// ************************************************************************
 int updateMktNfcDetail(int inti) throws Exception
 {
  dateTime();
  updateSQL = "pay_amt      = pay_amt + ?,"
            + "purchase_amt = purchase_amt + ?";
  daoTable  = "mkt_nfc_detail";
  whereStr  = "WHERE  p_seqno   = ? "
            + "AND    fund_code = ? "
            + "AND    pay_month = ? ";

  setDouble(1 , getValueDouble("payment_amt"));
  setDouble(2 , getValueDouble("purchase_amt"));
  setString(3 , getValue("p_seqno"));
  setString(4 , getValue("parm.fund_code",inti));
  setString(5 , getValue("wday.this_acct_month"));

  updateTable();

  if ( notFound.equals("Y") ) return(1);

  updateCnt++;
  return(0);
 }
// ************************************************************************
 int selectMktParmData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktParmData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktParmData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktParmData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
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
 void  loadMktParmData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_parm_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_NFC_PARM' "
            + "order by data_key,data_type,data_code,data_code2"
            ;

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_parm_data Count: ["+n+"]");
 }
// ************************************************************************
 void  loadMktMchtgpData() throws Exception
 {
  extendField = "mcht.";
  selectSQL = "b.data_key,"
            + "b.data_type,"
            + "a.data_code,"
            + "a.data_code2";
  daoTable  = "mkt_mchtgp_data a,mkt_parm_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'MKT_NFC_PARM' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type  = '6' "
            + "order by b.data_key,b.data_type,a.data_code"
            ;

  int  n = loadTable();

  setLoadData("mcht.data_key,mcht.data_type,mcht.data_code");

  showLogMessage("I","","Load mkt_mchtgp_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktMchtgpData(String col1,String col2,String sel,String dataType) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("mcht.data_key" , getValue("data_key"));
  setValue("mcht.data_type",dataType);
  setValue("mcht.data_code",col1);

  int cnt1 = getLoadData("mcht.data_key,mcht.data_type,mcht.data_code");
  int okFlag=0;
  for (int inti=0;inti<cnt1;inti++)
    {
     if ((getValue("mcht.data_code2",inti).length()==0)||
         ((getValue("mcht.data_code2",inti).length()!=0)&&
          (getValue("mcht.data_code2",inti).equals(col2))))
        {
         okFlag=1;
         break;
        }
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
 void  loadBilBill() throws Exception
 {
  extendField = "bill.";
  selectSQL = "p_seqno,"
            + "card_no,"
            + "reference_no,"
            + "acct_type,"
            + "major_id_p_seqno as id_p_seqno,"
            + "decode(sign_flag,'+',dest_amt,dest_amt*-1) as dest_amt,"
            + "acct_code,"
            + "decode(group_code,'','0000',group_code) as group_code,"
            + "card_type,"
            + "acq_member_id,"
            + "mcht_category,"
            + "mcht_no,"
            + "ECS_CUS_MCHT_NO ";
  daoTable  = "bil_bill";
  whereStr  = "where merge_flag != 'Y' "
            + "and   acct_code in ('BL','CA','IT','ID','OT','AO') "
            + "and   rsk_type not in ('1','2','3') " 
            + "AND   acct_month  = ? "
            + "and   stmt_cycle  = ? "
            + "and   curr_code  = '901' "
            ;

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and p_seqno = ? ";
                 
      setString(3 , pSeqno);
     }

  whereStr  = whereStr 
            + "order by p_seqno ";

  int  n = loadTable();

  setLoadData("bill.p_seqno");

  showLogMessage("I","","Load bil_bill Count: ["+n+"]");
 }
// ************************************************************************
 void  loadActAcct() throws Exception
 {
  extendField = "acct.";
  selectSQL = "p_seqno,"
            + "id_p_seqno";
  daoTable  = "act_acct";
  whereStr  = "where stmt_cycle = ? "
            + "and   p_seqno in ( "
            + "      select p_seqno "
            + "      from   mkt_nfc_detail "
            + "      where pay_month   = ? "
            + "      and   proc_flag   = 'N') "
            ;

  setString(1, getValue("wday.stmt_cycle"));
  setString(2, getValue("wday.this_acct_month"));

  if (pSeqno.length()!=0)
     {
      whereStr  = whereStr 
                + "and p_seqno = ? ";
                 
      setString(3 , pSeqno);
     }

  int  n = loadTable();

  setLoadData("acct.p_seqno");

  showLogMessage("I","","Load act_acct Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

