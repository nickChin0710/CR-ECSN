/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/10/12  V1.00.32  Allen Ho   Cyc_A165                                   *
* 111-11-08  V1.00.01    Machao    sync from mega & updated for project coding standard                                                                           *
* 112-03-30  V1.00.02    Zuwei Su  新增[ 一般消費群組 ]參數帳單資料篩選處理                                                                           *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA230 extends AccessDAO
{
 private final String PROGNAME = "關帳-紅利特惠專案(二)回饋處理程式 112-03-30  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate   = "";
 String newBusinessDate   = "";
 String activeCode     = "";
 String pSeqno         = "";
 String cardNo         = "";
 String tranSeqno = "";
 String[] begMonths = new String[100];
 String[] intMonths = new String[100];

 int maxMonths = 0,parmCnt=0,inti=0; 
 long    totalCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA230 proc = new CycA230();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);

   if (comm.isAppActive(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if (args.length > 4)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [active_code][new_business]");
       showLogMessage("I","","PARM 3 : [p_seqno][new_business]");
       showLogMessage("I","","PARM 4 : [card_no]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }
   if ( args.length >= 2 )
      {
       if (args[1].substring(0,5).equals("DATE="))
          newBusinessDate = args[1];
       else    
          activeCode = args[1]; 
      }
   if ( args.length >= 3 )
      { 
       if (args[1].substring(0,5).equals("DATE="))
          newBusinessDate = args[2];
       else    
          pSeqno     = args[2]; 
      }
   if ( args.length == 4 )
      { 
       cardNo     = args[3]; 
      }
   
   if ( !connectDataBase() ) 
       return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if ( args.length >= 1 )
      {
       if (selectPtrWorkday1()!=0)
          {
           showLogMessage("I","","本日非關帳日, 不需執行");
           return(0);
          }
      }
   else if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   if (activeCode.length()!=0)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","還原 proc_flag");
       updateMktBpnwMlist00();
       updateMktBpnwMlist01();
       updateMktBpnwIntro00();
      }
   showLogMessage("I","","=========================================");
   showLogMessage("I","","第一階段檢核參數");
   selectMktBpnw01();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadBilBill01();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(mkt_bpnw_mlist_01)資料");
   selectMktBpnwMlist01();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(mkt_bpnw_intro_02)資料");
   selectMktBpnwIntro01();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
    
   showLogMessage("I","","第二階段檢核參數");
   showLogMessage("I","","載入暫存資料");
   selectMktBpnw02();
   loadBilBill02();
   loadMktBnData();
   loadMktMchtgpData();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理帳單(mkt_bpnw_mlist_02)資料");
   selectMktBpnwMlist02();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");
    
   if (pSeqno.length()==0) finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
 {
  selectSQL = "";
  daoTable  = "ptr_businday";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  if (newBusinessDate.length()==0)
      newBusinessDate   =  businessDate;
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,businessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  showLogMessage("I","","本次關帳月["+getValue("wday.this_acct_month")+"]");
  return(0);
 }
// ************************************************************************ 
 int  selectPtrWorkday1() throws Exception
 {
  extendField = "wday.";
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where stmt_cycle = ? ";

  setString(1,businessDate.substring(6,8));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  setValue("wday.this_close_date" , businessDate);
  setValue("wday.this_acct_month" , businessDate.substring(0,6));
  setValue("wday.next_acct_month" , comm.nextMonth(businessDate.substring(0,6) , 1));

  return(0);
 } 
// ************************************************************************
 int selectMktBpnwMlist01() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.id_p_seqno,"
            + "a.active_code,"
            + "a.acct_month_e,"
            + "a.acct_type,"
            + "a.card_no,"
            + "a.sup_flag,"
            + "a.rowid as rowid";
  daoTable  = "mkt_bpnw_mlist a,act_acno b";
  whereStr  = "WHERE a.p_seqno      = b.p_seqno "
            + "and   a.proc_flag    = 'N' " 
            + "and   b.stmt_cycle   = ?  "
            + "and   a.proc_flag not in  ('X','Z') "
            + "and   (a.acct_month_e = ''  "
            + " or    (a.acct_month_e != ''  "
            + "  and   a.acct_month_e <= ?)) "
            ;

  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , getValue("wday.this_acct_month"));
   
  if (activeCode.length()!=0) 
     {
      whereStr  = whereStr  
                + "and   a.active_code = ?  ";
      setString(3 , activeCode);
      if (pSeqno.length()!=0) 
         {
          whereStr  = whereStr  
                    + "and   a.p_seqno = ?  ";
          setString(4 , pSeqno);
         }
     }

  openCursor();

  totalCnt=0;
  int cnt1 = 0;

  while( fetchTable() ) 
   {
    if (pSeqno.length()!=0) showLogMessage("I","","Check A START 01 ");
    totalCnt++;
    inti=-1;    
    for (int intm=0;intm<parmCnt;intm++)
       if (getValue("active_code").equals(getValue("parm.active_code",intm)))
          {
           inti = intm;
           break;
          }

    if (inti==-1)
       {
        updateMktBpnwMlist(1,"1"); // 參數不存在
        continue;
       }

    if (pSeqno.length()!=0) showLogMessage("I","","Check A START 02");

    if (getValue("parm.app_purch_cond",inti).equals("Y"))
       {
        if (pSeqno.length()!=0) showLogMessage("I","","Check A START03 ");
        setValue("bi01.card_no"    , getValue("card_no"));
        cnt1 = getLoadData("bi01.card_no");
        if (cnt1==0) continue;

        int matchCnt=0;
        if (pSeqno.length()!=0) showLogMessage("I","","Check A app_month ["+ getValueInt("parm.app_months",inti) +"]");
        for (int intm=0;intm<getValueInt("parm.app_months",inti);intm++)
          {
           if (getValue("bi01.acct_month",intm).compareTo(begMonths[inti])<0) continue;
           if (getValueInt("bi01.dest_amt",intm)<0) continue;
           matchCnt++;
          }
        if (pSeqno.length()!=0) showLogMessage("I","","Check B match_cnt ["+ matchCnt +"]");
        if (matchCnt != getValueInt("parm.app_months",inti))
           { 
            if (businessDate.compareTo(getValue("parm.active_date_e",inti))>0)
               updateMktBpnwMlist(1,"2"); 
            continue;
           }

        setValue("acct_month_s"         , getValue("wday.next_acct_month"));
        setValue("acct_month_e"         , comm.nextMonth(getValue("wday.next_acct_month"),
                                          getValueInt("parm.add_months",inti)-1));

        if (getValue("parm.active_date_e",inti).length()!=0)
        if (getValue("acct_month_e").compareTo(getValue("parm.active_date_e",inti).substring(0,6))>0)
           { 
            updateMktBpnwMlist(1,"X");   // 活動過期
            continue;
           }

        if (getValue("parm.apply_date_e",inti).length()!=0)
        if (getValue("acct_month_e").compareTo(getValue("parm.apply_date_e",inti).substring(0,6))>0)
           { 
            updateMktBpnwMlist(1,"Z");   // 發卡/申請日期 過期
            continue;
           }

        if (pSeqno.length()!=0) showLogMessage("I","","Check C add_month ["+ getValueInt("parm.add_months",inti) +"]");
        if (getValueInt("parm.add_months",inti)!=0)             
           { 
            updateMktBpnwMlist(1,"0"); 
            continue;
           }
       }
    if (getValue("parm.active_date_e",inti).length()!=0)
    if (getValue("wday.this_acct_month").compareTo(getValue("parm.active_date_e",inti).substring(0,6))>0)
       {
        updateMktBpnwMlist(1,"X");   // 活動過期
        continue;
       }

    if (getValue("parm.apply_date_e",inti).length()!=0)
    if (getValue("wday.this_acct_month").compareTo(getValue("parm.apply_date_e",inti).substring(0,6))>0)
       {
        updateMktBpnwMlist(1,"Z");   // 發卡/申請日期 過期
        continue;
       }

    setValueInt("modl.beg_tran_bp"  , 0);
    if (getValue("parm.major_cond",inti).equals("Y"))
       {
        if (getValue("sup_flag").equals("0")) 
           setValueInt("modl.beg_tran_bp"  , getValueInt("parm.major_point",inti));
       }
    if (getValue("parm.sub_cond",inti).equals("Y"))
       {
        if (getValue("sup_flag").equals("1"))
           setValueInt("modl.beg_tran_bp"  , getValueInt("parm.sub_point",inti));
       }

    if (pSeqno.length()!=0) showLogMessage("I","","Check D beg_bp    ["+ getValueInt("modl.beg_tran_bp") +"]");
    if (getValueInt("modl.beg_tran_bp")==0) 
       {
        updateMktBpnwMlist(1,"3"); 
        continue;
       }

    setValueInt("modl.end_tran_bp"       , getValueInt("modl.beg_tran_bp"));
    setValue("modl.mod_memo"             , "卡號:"+getValue("card_no")+"(贈送申請人)");

   if (pSeqno.length()!=0)
      showLogMessage("I","","insert 001["+ getValueInt("modl.beg_tran_bp") +"]");

    insertMktBonusDtl(inti);

    if (pSeqno.length()!=0) showLogMessage("I","","Check E default bp ");
    setValueInt("ori_feedback_bp"   , getValueInt("modl.beg_tran_bp"));
    updateMktBpnwMlist(1,"Y"); 
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktBpnwIntro01() throws Exception
 {
  selectSQL = "a.in_p_seqno as p_seqno,"
            + "a.in_id_p_seqno as id_p_seqno,"
            + "a.active_code,"
            + "a.in_acct_type as acct_type,"
            + "a.card_no,"
            + "a.rowid as rowid";
  daoTable  = "mkt_bpnw_intro a,act_acno b";
  whereStr  = "WHERE a.p_seqno      = b.p_seqno "
            + "and   a.proc_flag    = 'N' "
            + "and   b.stmt_cycle   = ?  "
            + "and   a.create_date <= ?  "
            + "and   a.in_p_seqno  != ''   "
            ;
  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , businessDate); 

  if (activeCode.length()!=0) 
     {
      whereStr  = whereStr  
                + "and   a.active_code = ?  ";
      setString(3 , activeCode);
      if (pSeqno.length()!=0) 
         {
          whereStr  = whereStr  
                    + "and   a.p_seqno = ?  ";
          setString(4 , pSeqno);
         }
     }
             
  openCursor();

  totalCnt=0;
  int cnt1 = 0;

  while( fetchTable() ) 
   {
    totalCnt++;
    inti=-1;    
    for (int intm=0;intm<parmCnt;intm++)
       if (getValue("active_code").equals(getValue("parm.active_code",intm)))
          {
           inti = intm;
           break;
          }

    if (inti==-1)
       {
        updateMktBpnwIntro("1"); // 參數不存在
        continue;
       }

    if (getValue("parm.active_date_e",inti).length()!=0)
    if (getValue("wday.this_acct_month").compareTo(getValue("parm.active_date_e",inti).substring(0,6))>0)
       {
        updateMktBpnwMlist(1,"X");   // 活動過期
        continue;
       }

    if (getValue("parm.apply_date_e",inti).length()!=0)
    if (getValue("wday.this_acct_month").compareTo(getValue("parm.apply_date_e",inti).substring(0,6))>0)
       {
        updateMktBpnwMlist(1,"Z");   // 發卡/申請日期 過期
        continue;
       }
       
    if (getValue("parm.intro_purch_cond",inti).equals("Y"))
       {
        setValue("bi01.card_no"    , getValue("card_no"));
        cnt1 = getLoadData("bi01.card_no");
        if (cnt1==0) continue;

        int matchCnt=0;
        for (int intm=0;intm<getValueInt("parm.intro_months",inti);intm++)
          {
           if (getValue("bi01.acct_month",intm).compareTo(intMonths[inti])<0) continue;
           if (getValueInt("bi01.dest_amt",intm)<=0) continue;
           matchCnt++;
          }
        if (matchCnt != getValueInt("parm.intro_months",inti))
           {
            if (businessDate.compareTo(getValue("parm.active_date_e",inti))>0)
               updateMktBpnwIntro("1"); 
            continue;
           }
       }

    setValueInt("modl.beg_tran_bp"  , getValueInt("parm.intro_point",inti));

    if (getValueInt("modl.beg_tran_bp")==0) 
       {
        updateMktBpnwIntro("2"); 
        continue;
       }

    setValueInt("modl.end_tran_bp"       , getValueInt("modl.beg_tran_bp"));
    setValue("modl.mod_memo"             , "卡號:"+getValue("card_no")+"(贈送介紹人)");
    insertMktBonusDtl(inti);

    setValueInt("feedback_bp"   , getValueInt("modl.beg_tran_bp"));
    updateMktBpnwIntro("Y"); 
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectMktBpnwMlist02() throws Exception
 {
  selectSQL = "a.p_seqno,"
            + "a.id_p_seqno,"
            + "a.active_code,"
            + "a.acct_type,"
            + "a.card_no,"
            + "a.acct_month_s,"
            + "a.acct_month_e,"
            + "a.sup_flag,"
            + "a.rowid as rowid";
  daoTable  = "mkt_bpnw_mlist a,act_acno b";
  whereStr  = "WHERE a.p_seqno      = b.p_seqno "
            + "and   b.stmt_cycle   = ?  "
            + "and   a.acct_month_e = ?  "
            + "and   a.proc_flag not in ('X','Z') "
            ;
  setString(1 , getValue("wday.stmt_cycle"));
  setString(2 , getValue("wday.this_acct_month"));

  if (activeCode.length()!=0) 
     {
      whereStr  = whereStr  
                + "and   a.active_code = ?  ";
      setString(3 , activeCode);
      if (pSeqno.length()!=0) 
         {
          whereStr  = whereStr  
                    + "and   a.p_seqno = ?  ";
          setString(4 , pSeqno);
         }
     }
  else
     whereStr  = whereStr  
               + "and   a.proc_flag    = '0' ";

  openCursor();

  totalCnt=0;
  int cnt1 = 0;
  String acqId ="";

  while( fetchTable() ) 
   {
    totalCnt++;
    inti=-1;
    for (int intm=0;intm<parmCnt;intm++)
      {
       if (getValue("active_code").equals(getValue("parm.active_code",intm)))
          {
           inti = intm;
           break;
          }
      }

    if (inti==-1)
       {
        updateMktBpnwMlist(1,"1"); // 參數不存在
        continue;
       }
    if (pSeqno.length()!=0) showLogMessage("I","","Check 1 START 02 ");
       
    setValue("bill.card_no" , getValue("card_no"));
    cnt1 = getLoadData("bill.card_no");
    if (cnt1==0) 
       {
        updateMktBpnwMlist(2,"4");  // 無消費
        continue;
       }
    if (pSeqno.length()!=0) showLogMessage("I","","Check 1 START 03 ");

    int totAmt=0,totCnt=0;
    int destAmt=0;

    setValue("data_key", getValue("active_code")); 

    for (int intk=0;intk<cnt1;intk++)
        {
         if (getValueInt("bill.dest_amt",intk)<=0) continue;
         if (pSeqno.length()!=0)
            {
             showLogMessage("I","","reference_no  ["+ getValue("bill.reference_no", intk) + "]");
             showLogMessage("I","","purchase_date ["+ getValue("bill.purchase_date", intk) + "]");
             showLogMessage("I","","dest_amt      ["+ getValueInt("bill.dest_amt", intk) + "]");
            } 

         if (pSeqno.length()!=0) showLogMessage("I","","Check 1 acct_month");
         if (getValue("bill.acct_month",intk).compareTo(begMonths[inti])<0) continue;


         acqId = "";
         if (getValue("acq_member_id").length()!=0)
            acqId = comm.fillZero(getValue("bill.acq_member_id,intk"),8);

         if (pSeqno.length()!=0) showLogMessage("I","","Check 2 merchant no");
         if (selectMktBnData(getValue("bill.mcht_no",intk),acqId,
                                getValue("parm.merchant_sel",inti),"6",3)!=0) continue;

         if (pSeqno.length()!=0) showLogMessage("I","","Check 3 merchant group & 一般消費群組");
         if (selectMktMchtgpData(getValue("bill.mcht_no",intk),acqId,
                                 getValue("parm.mcht_group_sel",inti),"4")!=0) continue;         
         // 一般消費群組
         //if (pSeqno.length()!=0) showLogMessage("I","","Check 3 platform kind");
         if (selectMktMchtgpData(getValue("bill.ecs_cus_mcht_no",intk),"",
                 getValue("parm.platform_kind_sel",inti),"P")!=0) continue;         

         if (getValue("bill.sign_flag",intk).equals("-")) destAmt = getValueInt("bill.dest_amt",intk)*-1;
         else destAmt = getValueInt("bill.dest_amt",intk);

         if (pSeqno.length()!=0) showLogMessage("I","","Check 4 acct_month");
         if (getValue("bill.acct_month",intk).compareTo(getValue("acct_month_s"))<0) continue;

         if (pSeqno.length()!=0) showLogMessage("I","","Check 5 reclow_amt");
         if (getValue("parm.purch_reclow_cond",inti).equals("Y"))
            if (getValue("bill.sign_flag",intk).equals("+"))
               if (getValueInt("bill.dest_amt",intk)<getValueInt("parm.purch_reclow_amt",inti)) continue;

         if (pSeqno.length()!=0) showLogMessage("I","","Check 6 IT acct_code");
         if (getValue("bill.acct_code",intk).equals("IT"))
            {
             if (selectBilContract(intk)!=0) continue;
             insertMktBpnwItlist(intk);
            }


         totCnt++;
         totAmt = totAmt + destAmt;
        }

    if (pSeqno.length()!=0) showLogMessage("I","","Check 7 tot_amt ["+ totAmt +"]");
    if (totAmt<=0)
       {
        updateMktBpnwMlist(2,"5");
        continue;
       }

    setValueInt("purchase_amt" , totAmt);

    if (pSeqno.length()!=0) showLogMessage("I","","Check 8 tol_amt ["+ totAmt +"]");
    if (getValue("parm.purch_tol_amt_cond",inti).equals("Y"))
       if (totAmt<getValueInt("parm.purch_tol_amt",inti)) 
       {
        updateMktBpnwMlist(2,"6");  // 消費累計金額不足
        continue;
       }

    if (pSeqno.length()!=0) showLogMessage("I","","Check 8 tol_cnt ["+ totCnt +"]");
    if (getValue("parm.purch_tol_time_cond",inti).equals("Y"))
       if (totCnt<getValueInt("parm.purch_tol_time",inti))
       {
        updateMktBpnwMlist(2,"7");  // 消費累計次數不足
        continue;
       }

    int exchgRate = 0;

     if ((totAmt>=getValueInt("parm.limit_1_beg",inti))&&
         (totAmt<=getValueInt("parm.limit_1_end",inti))) 
        {
         exchgRate = getValueInt("parm.exchange_1",inti);
        }
    else  if ((totAmt>=getValueInt("parm.limit_2_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_2_end",inti))) 
        {
         exchgRate = getValueInt("parm.exchange_2",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_3_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_3_end",inti))) 
        {
         exchgRate = getValueInt("parm.exchange_3",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_4_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_4_end",inti))) 
        {
         exchgRate = getValueInt("parm.exchange_4",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_5_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_5_end",inti))) 
        {
         exchgRate = getValueInt("parm.exchange_5",inti);
        }
     else if ((totAmt>=getValueInt("parm.limit_6_beg",inti))&&
              (totAmt<=getValueInt("parm.limit_6_end",inti))) 
        {
         exchgRate = getValueInt("parm.exchange_6",inti);
        }
             
    if (exchgRate==0)
       {
        updateMktBpnwMlist(2,"8"); // 無對應兌換率
        continue;
       }

    setValueInt("purchase_amt" , totAmt);
    int totBp = Math.round(totAmt/exchgRate);
    int begTranBp = totBp*getValueInt("parm.add_times",inti)
                    + getValueInt("parm.add_point",inti);
   if (pSeqno.length()!=0)
      {
       showLogMessage("I","","tot_amt 002["+ totAmt +"]");
       showLogMessage("I","","rate    002["+ exchgRate +"]");
       showLogMessage("I","","tot_bp  002["+ totBp +"]");
       showLogMessage("I","","beg_bp  002["+ begTranBp +"]");
      }


    setValueInt("feedback_bp" , begTranBp);

    setValue("ori_feedback_bp"  , "0");
    if (getValue("parm.major_cond",inti).equals("Y"))
       {
        if (getValue("sup_flag").equals("0")) 
           setValue("ori_feedback_bp"  , getValue("parm.major_point",inti));
       }
    if (getValue("parm.sub_cond",inti).equals("Y"))
       {
        if (getValue("sup_flag").equals("1"))
           setValue("ori_feedback_bp"  , getValue("parm.sub_point",inti));
       }

   if (pSeqno.length()!=0)
      {
       showLogMessage("I","","ori_bp  002["+ getValueInt("ori_feedback_bp") +"]");
       showLogMessage("I","","beg_bp A002["+ begTranBp +"]");
      }
    begTranBp = begTranBp + getValueInt("ori_feedback_bp");

   if (pSeqno.length()!=0)
      {
       showLogMessage("I","","beg_bp B002["+ begTranBp +"]");
      }

    if (getValueInt("parm.feedback_lmt",inti) !=0)          
       if (getValueInt("parm.feedback_lmt",inti) < begTranBp)
          begTranBp = getValueInt("parm.feedback_lmt",inti);

    if (begTranBp==0) 
       {
        updateMktBpnwMlist(2,"9"); 
        continue;
       }

    setValue("modl.mod_memo"             , "卡號:"+getValue("card_no")+"(贈送申請人)");
    setValueInt("modl.beg_tran_bp"       , begTranBp);
    setValueInt("modl.end_tran_bp"       , begTranBp);

   if (pSeqno.length()!=0)
      showLogMessage("I","","insert 002["+ getValueInt("modl.beg_tran_bp") +"]");

    insertMktBonusDtl(inti);

    updateMktBpnwMlist(2,"Y");          
    totalCnt++;
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 void  loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_BPNW' "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktBnData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
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
         if (getValue("data.data_code",intm).length()!=0)
            {
             if (col1.length()!=0)
                {
                 if (!getValue("data.data_code",intm).equals(col1)) continue;
                }
              else
                {
                 if (sel.equals("1")) continue;
                }
            }
        }
     if (getValue("data.data_code2",intm).length()!=0)
        {
         if (col2.length()!=0)
            {
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
            }
          else
            {
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
         if (col3.length()!=0)
            {
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
            }
          else
            {
             continue;
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
  daoTable  = "mkt_mchtgp_data a,mkt_bn_data b";
  whereStr  = "WHERE a.TABLE_NAME = 'MKT_MCHT_GP' "
            + "and   b.TABLE_NAME = 'MKT_BPNW' "
            + "and   a.data_key   = b.data_code "
            + "and   a.data_type  = '1' "
            + "and   b.data_type in ('4','P')  "
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
 int insertMktBonusDtl(int inti) throws Exception
 {
  dateTime();
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");

  setValue("modl.active_code"          , getValue("parm.active_code",inti));
  setValue("modl.active_name"          , getValue("parm.active_name",inti));
  setValue("modl.p_seqno"              , getValue("p_seqno")); 
  setValue("modl.id_p_seqno"           , getValue("id_p_seqno")); 
  setValue("modl.acct_type"            , getValue("acct_type")); 
  setValue("modl.tax_flag"             , getValue("parm.tax_flag",inti));
  setValue("modl.tran_code"            , "2");
  setValue("modl.mod_desc"             , "利特惠專案(二)回饋");
  setValue("modl.tran_date"            , sysDate);
  setValue("modl.tran_time"            , sysTime);
  setValue("modl.tran_seqno"           , tranSeqno);
  setValue("mbdl.effect_e_date"   , "");
  if (getValueInt("parm.effect_months",inti)>0)
      setValue("modl.effect_e_date"        , comm.nextMonthDate(businessDate,getValueInt("parm.effect_months",inti)));
  setValue("modl.bonus_type"           , getValue("parm.bonus_type",inti));
  setValue("modl.acct_date"            , businessDate);
  setValue("modl.proc_month"           , businessDate.substring(0,6));
  setValue("modl.tran_pgm"             , javaProgram);
  setValue("modl.apr_flag"             , "Y");
  setValue("modl.apr_user"             , javaProgram);
  setValue("modl.apr_date"             , sysDate);
  setValue("modl.crt_user"             , javaProgram);
  setValue("modl.crt_date"             , sysDate);
  setValue("modl.mod_user"             , javaProgram);
  setValue("modl.mod_time"             , sysDate+sysTime);
  setValue("modl.mod_pgm"              , javaProgram);

  extendField = "modl.";
  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectMktBpnw01() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_bpnw";
  whereStr  = "where active_code in ("
            + "      select distinct active_code "
            + "      from   mkt_bpnw_mlist "
            + "      where  proc_flag    = 'N'  "
            + "      union  "
            + "      select distinct active_code "
            + "      from   mkt_bpnw_intro "
            + "      where  proc_flag    = 'N')  "
            ;
  if (activeCode.length()!=0) 
     {
      whereStr  = whereStr  
                + "and   active_code = ?  ";
      setString(1 , activeCode);
     }
  parmCnt = selectTable();

  maxMonths = 0;
  for (int inti=0;inti<parmCnt;inti++)
    {
     if (getValue("parm.applicant_cond",inti).equals("Y"))
        if (getValue("parm.app_purch_cond",inti).equals("Y"))
           if (getValueInt("parm.app_months",inti)>maxMonths)
              maxMonths = getValueInt("parm.app_months",inti);

     if (getValue("parm.introducer_cond",inti).equals("Y"))
        if (getValue("parm.intro_purch_cond",inti).equals("Y"))
           if (getValueInt("parm.intro_months",inti)>maxMonths)
              maxMonths = getValueInt("parm.intro_months",inti);
     begMonths[inti] = comm.nextMonth(businessDate,1+getValueInt("parm.app_months",inti)*-1); 
     intMonths[inti] = comm.nextMonth(businessDate,1+getValueInt("parm.intro_months",inti)*-1); 
     showLogMessage("I","","活動代碼 : ["+ getValue("parm.active_code",inti) +"]["
                           + getValue("parm.active_name",inti) +"]["
                           + begMonths[inti] +"]["
                           + intMonths[inti] +"]");
    }

  showLogMessage("I","","最大之近 N 期 : ["+ maxMonths +"]");
  showLogMessage("I","","1 參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 int selectMktBpnw02() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_bpnw";
  whereStr  = "where active_code in ("
            + "      select distinct active_code "
            + "      from   mkt_bpnw_mlist "
            ;

  if (activeCode.length()!=0) 
     {
      whereStr  = whereStr  
                + "      )  "
                + "and   active_code = ?  ";
      setString(1 , activeCode);
     }
  else
      whereStr  = whereStr  
                + "      where  proc_flag    = '0')  ";

  parmCnt = selectTable();

  maxMonths = 0;
  for (int inti=0;inti<parmCnt;inti++)
    {
     if (getValueInt("parm.add_months",inti)>maxMonths)
        maxMonths = getValueInt("parm.add_months",inti);

     begMonths[inti] = comm.nextMonth(businessDate,1+getValueInt("parm.add_months",inti)*-1); 
     showLogMessage("I","","活動代碼 : ["+ getValue("parm.active_code",inti) +"]["
                           + getValue("parm.active_name",inti) +"]["
                           + begMonths[inti] +"]");
    }

  showLogMessage("I","","2 參數檔載入筆數: ["+ parmCnt +"]");
  return(0);
 }
// ************************************************************************
 void  loadBilBill01() throws Exception
 {
  extendField = "bi01.";
  selectSQL = "card_no,"
            + "acct_month,"
            + "sum(decode(sign_flag,'-',dest_amt*-1,dest_amt)) as dest_amt";
  daoTable  = "bil_bill";
  whereStr  = "where acct_month between  ?  and ? "
            + "and   stmt_cycle = ? "
            + "and   acct_code in ('BL','IT','ID','AO','OT') "
            + "and   rsk_type not in ('1','2','3') "
            + "and   merge_flag       != 'Y'  "
            + "and   acct_date        <= ?  "
            + "and   card_no in ("
            + "      select distinct card_no "
            + "      from   mkt_bpnw_mlist a,mkt_bpnw b "
            + "      where  a.proc_flag    = 'N'  "
            + "      and    a.active_code  = b.active_code  "
            + "      and    b.app_purch_cond = 'Y' "
            + "      and    a.proc_flag not in ('X','Z') "
            + "      and    a.create_date   <= ? "
            + "      union  "
            + "      select distinct card_no "
            + "      from   mkt_bpnw_intro a,mkt_bpnw b "
            + "      where  a.proc_flag    = 'N'  "
            + "      and    a.active_code  = b.active_code  "
            + "      and    a.create_date   <= ? "
            + "      and    b.intro_purch_cond = 'Y') "
            ;

  setString(1 , comm.nextMonth(getValue("wday.this_acct_month") , (maxMonths*-1)+1));
  setString(2 , getValue("wday.this_acct_month")); 
  setString(3 , getValue("wday.stmt_cycle")); 
  setString(4 , businessDate); 
  setString(5 , businessDate); 
  setString(6 , businessDate); 

  if (cardNo.length()!=0)
     {
      showLogMessage("I","","acct_month_s["+ comm.nextMonth(getValue("wday.this_acct_month") , (maxMonths*-1)+1) +"]");
      showLogMessage("I","","acct_month_e["+ getValue("wday.this_acct_month") +"]");
      showLogMessage("I","","stmt_cycle  ["+ getValue("wday.stmt_cycle") +"]");

      whereStr  = whereStr
                + "and   card_no     = ?  ";
      setString(7 , cardNo);
     }

  whereStr  = whereStr
            + " group by card_no,acct_month "
            + " order by card_no";

  int  n = loadTable();
  setLoadData("bi01.card_no");

  showLogMessage("I","","Load bil_bill_01 Count: ["+n+"]");
 }
// ************************************************************************
 void  loadBilBill02() throws Exception
 {
  extendField = "bill.";
  selectSQL = "card_no,"
            + "acct_month,"
            + "acct_code,"
            + "reference_no,"
            + "contract_no,"
            + "contract_seq_no,"
            + "contract_amt,"
            + "bill_type,"
            + "txn_code,"
            + "acq_member_id," 
            + "mcht_no," 
//          + "ecs_platform_kind ,"
            + "ecs_cus_mcht_no ,"
            + "purchase_date," 
            + "post_date," 
            + "sign_flag,"
            + "(dest_amt+curr_adjust_amt) as dest_amt";
  daoTable  = "bil_bill";
  whereStr  = "where acct_month between  ?  and ? "
            + "and   stmt_cycle = ? "
            + "and   acct_code in ('BL','IT','ID','AO','OT') "
            + "and   rsk_type not in ('1','2','3') "
            + "and   merge_flag       != 'Y'  "
            + "and   card_no in ("
            + "      select distinct card_no "
            + "      from   mkt_bpnw_mlist "
            + "      where  proc_flag not in ('X','Z') "
            ;

  setString(1 , comm.nextMonth(getValue("wday.this_acct_month") , (maxMonths*-1)+1));
  setString(2 , getValue("wday.this_acct_month")); 
  setString(3 , getValue("wday.stmt_cycle"));
   
  if (activeCode.length()!=0) 
     {
      showLogMessage("I","","acct_month_s["+ comm.nextMonth(getValue("wday.this_acct_month") , (maxMonths*-1)+1) +"]");
      showLogMessage("I","","acct_month_e["+ getValue("wday.this_acct_month") +"]");
      showLogMessage("I","","stmt_cycle  ["+ getValue("wday.stmt_cycle") +"]");

      whereStr  = whereStr  
                + "and   active_code = ? ) ";
      setString(4 , activeCode);
      if (cardNo.length()!=0)
         {
          whereStr  = whereStr  
                    + "and   card_no     = ?  ";
          setString(5 , cardNo);
         }
     }
  else 
      whereStr  = whereStr  
                + "      and    proc_flag    = '0')  ";

   
   whereStr  = whereStr  
            + " order by card_no";

  int  n = loadTable();
  setLoadData("bill.card_no");

  showLogMessage("I","","Load bil_bill_02 Count: ["+n+"]");
 }
// ************************************************************************
 int insertMktBpnwItlist(int intk) throws Exception
 {
  dateTime();
  setValue("create_date"           , sysDate);
  setValue("active_code"           , getValue("active_code")); 
  setValue("card_no"               , getValue("card_no"));
  setValue("reference_no"          , getValue("bill.reference_no",intk));
  setValue("contract_no"           , getValue("bill.contract_no",intk));
  setValue("contract_amt"          , getValue("bill.contract_amt",intk));
  setValue("contract_seq_no"       , getValue("bill.contract_seq_no",intk));
  setValueInt("contract_seq_no"    , getValueInt("bill.contract_amt",intk));
  setValue("bill_type"             , getValue("bill.bill_type",intk));
  setValue("txn_code"              , getValue("bill.txn_code",intk));
  setValue("acq_member_id"         , getValue("bill.acq_member_id",intk));
  setValue("mcht_no"               , getValue("bill.mcht_no"      ,intk));
  setValue("sign_flag"             , getValue("bill.sign_flag",intk));
  setValueInt("dest_amt"           , getValueInt("bill.dest_amt" ,intk));
  setValue("acct_month"            , getValue("bill.acct_month"   ,intk));
  setValue("purchase_date"         , getValue("bill.purchase_date",intk));
  setValue("post_date"             , getValue("bill.post_date"    ,intk));
  setValue("acct_code"             , getValue("bill.acct_code"    ,intk));
  setValue("mod_time"              , sysDate+sysTime);
  setValue("mod_pgm"               , javaProgram);

  daoTable  = "mkt_bpnw_itlist";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktBpnwMlist(int typeInt,String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_date     = ?, "
            + "proc_flag     = ?, "
            + "mod_pgm       = ?, ";

  if (procFlag.equals("0"))
     {
      updateSQL = updateSQL 
                + "acct_month_s     = ?, "  
                + "acct_month_e     = ?, "  
                + "check_date       = ?, ";  
     }
  else if (procFlag.equals("Y"))
     {
      if (typeInt==1)
         updateSQL = updateSQL 
                   + "ori_feedback_bp  = ?, "  
                   + "tran_seqno       = ?, "; 
      else               
         updateSQL = updateSQL 
                   + "purchase_amt    = ?, "
                   + "ori_feedback_bp = ?, "  
                   + "feedback_bp     = ?, "
                   + "tran_seqno      = ?, "; 
     }
  updateSQL = updateSQL 
            + "mod_time      = sysdate";
  daoTable  = "mkt_bpnw_mlist";
  whereStr  = "WHERE rowid  = ? ";

  setString(1 , businessDate);
  setString(2 , procFlag);
  setString(3 , javaProgram);
  if (procFlag.equals("0"))
     {
      setString(4, getValue("acct_month_s"));
      setString(5, getValue("acct_month_e"));
      setString(6, businessDate);
      setRowId(7 , getValue("rowid"));
    }
  else if (procFlag.equals("Y"))
     {
      if (typeInt==1)
         {
          setInt(4, getValueInt("ori_feedback_bp"));
          setString(5, tranSeqno);
          setRowId(6 , getValue("rowid"));
         }
      else
         {
          setInt(4, getValueInt("purchase_amt"));
          setInt(5, getValueInt("ori_feedback_bp"));
          setInt(6, getValueInt("feedback_bp"));
          setString(7, tranSeqno);
          setRowId(8 , getValue("rowid"));
         }
     }
  else
     {
      setRowId(4 , getValue("rowid"));
    }

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktBpnwIntro(String procFlag) throws Exception
 {
  dateTime();
  updateSQL = "proc_date     = ?, "
            + "proc_flag     = ?, "
            + "feedback_bp   = ?, "  
            + "tran_seqno    = ?, "  
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";
  daoTable  = "mkt_bpnw_intro";
  whereStr  = "WHERE rowid  = ? ";

  setString(1 , businessDate);
  setString(2 , procFlag);
  setInt(3    , getValueInt("feedback_bp"));
  setString(4 , tranSeqno);
  setString(5 , javaProgram);
  setRowId(6  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int  selectBilContract(int intk) throws Exception
 {
  selectSQL = "refund_apr_flag";
  daoTable  = "bil_contract";
  whereStr  = "where contract_no     = ? "
            + "and   contract_seq_no = ? ";

  setString(1 , getValue("bill.contract_no",intk));
  setString(2 , getValue("bill.contract_seq_no",intk));

  selectTable();

  if (getValue("refund_apr_flag").equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 void updateMktBpnwMlist00() throws Exception
 {
  dateTime();
  updateSQL = "proc_date       = '',"
            + "proc_flag       = 'N',"
            + "acct_month_s    = '',"  
            + "acct_month_e    = '',"  
            + "check_date      = '',"  
            + "purchase_amt    = 0,"
            + "ori_feedback_bp = 0,"  
            + "feedback_bp     = 0,"
            + "tran_seqno      = ''"; 
  daoTable  = "mkt_bpnw_mlist";
  whereStr  = "WHERE active_code = ? "
            + "and   create_date = ? "
            + "and   proc_flag not in  ('X','Z') "
            ;

  setString(1 , activeCode);
  setString(2 , businessDate);

  int n = updateTable();
  showLogMessage("I","","Update mkt_bpnw_mlist [" + n +"] 筆");
  return;
 }
// ************************************************************************
 void updateMktBpnwIntro00() throws Exception
 {
  dateTime();
  updateSQL = "proc_date     = '', "
            + "proc_flag     = 'N', "
            + "feedback_bp   = 0, "  
            + "tran_seqno    = 0 "; 
  daoTable  = "mkt_bpnw_intro";
  whereStr  = "WHERE active_code = ? "
            + "and proc_flag not in ('A','B','C') "
            + "and   create_date = ? "
            ;

  setString(1 , activeCode);
  setString(2 , businessDate);

  int n = updateTable();
  showLogMessage("I","","Update mkt_bpnw_intro [" + n +"] 筆");
  return;
 }
// ************************************************************************
 void updateMktBpnwMlist01() throws Exception
 {
  dateTime();
  updateSQL = "proc_date       = '',"
            + "proc_flag       = '0'";
  daoTable  = "mkt_bpnw_mlist";
  whereStr  = "WHERE active_code = ? "
            + "and   proc_flag    = 'Y'  "
            + "and   acct_month_e = ?  "
            ;

  setString(1 , activeCode);
  setString(2 , getValue("wday.this_acct_month"));

  int n = updateTable();
  showLogMessage("I","","Update mkt_bpnw_mlist [" + n +"] 筆");
  return;
 }
// ************************************************************************


}  // End of class FetchSample
