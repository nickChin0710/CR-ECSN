/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/04/11  V1.00.02  Allen Ho   initial                                    *
* 112/05/05  V1.00.03  Simon      Add notfound display                       *                                                                          *
******************************************************************************/
package Cyc;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycA485 extends AccessDAO
{
 private  String PROGNAME = "關帳更新商務卡公司戶帳務歷史資料主檔處理程式 112/05/05 V1.00.03";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate    = "";

 int   totCnt=0,perCnt=0,comCnt=0;
 int   perNfndCnt=0,comNfndCnt=0;
 int[] loadCnt = new int[30];

 double sum_dest_amt=0,dest_amt = 0,cap_end_bal=0,tot_end_bal = 0,lst_end_bal = 0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA485 proc = new CycA485();
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
      { businessDate = args[0]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","檢核執行日期");
   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存檔 開始...");
   loadActAcno();
   loadActAcctHst();
   loadActCurrHst();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入主檔 act_acno");
   selectActAcno();
   showLogMessage("I","","=========================================");
  
   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
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
      businessDate   =  getValue("business_date");
  showLogMessage("I","","本日營業日 : ["+ businessDate+"]");
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

  showLogMessage("I","","stmt_cycle      : ["+ getValue("wday.stmt_cycle")+"]");
  showLogMessage("I","","last_acct_month : ["+ getValue("wday.last_acct_month")+"]");
  return(0);
 }
// ************************************************************************
 void selectActAcno() throws Exception
 {
  selectSQL = "p_seqno,"
            + "acct_type,"
            + "corp_p_seqno";
  daoTable  = "act_acno";
  whereStr  = "where acno_flag   = '2' "
            + "and   stmt_cycle  = ? ";

  setString(1 , getValue("wday.stmt_cycle"));

  openCursor();

  int rCnt1=0,rCnt2=0;
  String currFlag="",acctFlag="";

  while( fetchTable() )
    {
     totCnt++;
     initActAcctHst();
     initActCurrHst();
     currFlag="";acctFlag="";

     setValue("acno.corp_p_seqno", getValue("corp_p_seqno"));
     setValue("acno.acct_type"   , getValue("acct_type"));
     rCnt1 = getLoadData("acno.corp_p_seqno,acno.acct_type");
     // *******************************************************
     for (int int1=0;int1<rCnt1;int1++)
       {
        setValue("acht.p_seqno",getValue("acno.acno_p_seqno",int1));
        rCnt2 = getLoadData("acht.p_seqno");
        if (rCnt2!=0) 
           {
            caclActAcctHst();
            acctFlag="1";
            perCnt++;
           }

        setValue("cuht.p_seqno",getValue("acno.acno_p_seqno",int1));
        rCnt2 = getLoadData("cuht.p_seqno");
        if (rCnt2!=0) 
           {
            caclActCurrHst();
            currFlag="1";
            comCnt++;
           }
       }
     if (acctFlag.length()!=0) updateActAcctHst();
     if (currFlag.length()!=0) updateActCurrHst();
    }
  showLogMessage("I","","total    cnt :" + totCnt);
  showLogMessage("I","","acct_hst cnt :" + perCnt);
  showLogMessage("I","","acct_hst Notfnd cnt :" + perNfndCnt);
  showLogMessage("I","","curr_hst cnt :" + comCnt);
  showLogMessage("I","","curr_hst Notfnd cnt :" + comNfndCnt);

  closeCursor();
 }
// ************************************************************************
 void loadActAcno() throws Exception // 公司戶
 {
  extendField = "acno.";
  selectSQL = "corp_p_seqno,"
            + "acct_type,"
            + "acno_p_seqno";
  daoTable  = "act_acno";
  whereStr  = "where acno_flag in ('3','Y') "
            + "order by corp_p_seqno,acct_type ";

  int  n = loadTable();
  setLoadData("acno.corp_p_seqno,acno.acct_type");
  showLogMessage("I","","Load act_acno  Count: ["+n+"]");
 }
// ************************************************************************
 void loadActAcctHst() throws Exception
 {
  extendField = "acht.";
  selectSQL = "p_seqno,"
            + "acct_jrnl_bal," 
            + "beg_bal_op," 
            + "end_bal_op," 
            + "beg_bal_lk," 
            + "end_bal_lk," 
            + "unbill_beg_bal_af," 
            + "unbill_beg_bal_lf," 
            + "unbill_beg_bal_cf," 
            + "unbill_beg_bal_pf," 
            + "unbill_beg_bal_bl,"
            + "unbill_beg_bal_ca," 
            + "unbill_beg_bal_it," 
            + "unbill_beg_bal_id," 
            + "unbill_beg_bal_ri," 
            + "unbill_beg_bal_pn," 
            + "unbill_beg_bal_ao," 
            + "unbill_beg_bal_ai," 
            + "unbill_beg_bal_sf," 
            + "unbill_beg_bal_dp," 
            + "unbill_beg_bal_cb," 
            + "unbill_beg_bal_ci," 
            + "unbill_beg_bal_cc," 
            + "unbill_beg_bal_db," 
            + "unbill_end_bal_af," 
            + "unbill_end_bal_lf," 
            + "unbill_end_bal_cf," 
            + "unbill_end_bal_pf," 
            + "unbill_end_bal_bl," 
            + "unbill_end_bal_ca," 
            + "unbill_end_bal_it," 
            + "unbill_end_bal_id," 
            + "unbill_end_bal_ri," 
            + "unbill_end_bal_pn," 
            + "unbill_end_bal_ao," 
            + "unbill_end_bal_ai," 
            + "unbill_end_bal_sf," 
            + "unbill_end_bal_dp," 
            + "unbill_end_bal_cb," 
            + "unbill_end_bal_ci," 
            + "unbill_end_bal_cc," 
            + "unbill_end_bal_db," 
            + "billed_beg_bal_af," 
            + "billed_beg_bal_lf," 
            + "billed_beg_bal_cf," 
            + "billed_beg_bal_pf," 
            + "billed_beg_bal_bl," 
            + "billed_beg_bal_ca," 
            + "billed_beg_bal_it," 
            + "billed_beg_bal_id," 
            + "billed_beg_bal_ri," 
            + "billed_beg_bal_pn," 
            + "billed_beg_bal_ao," 
            + "billed_beg_bal_ai," 
            + "billed_beg_bal_sf," 
            + "billed_beg_bal_dp," 
            + "billed_beg_bal_cb," 
            + "billed_beg_bal_ci," 
            + "billed_beg_bal_cc," 
            + "billed_beg_bal_db," 
            + "billed_end_bal_af," 
            + "billed_end_bal_lf," 
            + "billed_end_bal_cf," 
            + "billed_end_bal_pf," 
            + "billed_end_bal_bl," 
            + "billed_end_bal_ca," 
            + "billed_end_bal_it," 
            + "billed_end_bal_id," 
            + "billed_end_bal_ri," 
            + "billed_end_bal_pn," 
            + "billed_end_bal_ao," 
            + "billed_end_bal_ai," 
            + "billed_end_bal_sf," 
            + "billed_end_bal_dp," 
            + "billed_end_bal_cb," 
            + "billed_end_bal_ci," 
            + "billed_end_bal_cc," 
            + "billed_end_bal_db," 
            + "adi_beg_bal," 
            + "adi_end_bal," 
            + "adi_d_avail," 
            + "unbill_beg_bal_ot," 
            + "billed_beg_bal_ot," 
            + "unbill_end_bal_ot," 
            + "billed_end_bal_ot," 
            + "unbill_beg_bal_db_b," 
            + "unbill_beg_bal_db_c," 
            + "unbill_beg_bal_db_i," 
            + "billed_beg_bal_db_b," 
            + "billed_beg_bal_db_c," 
            + "billed_beg_bal_db_i," 
            + "unbill_end_bal_db_b," 
            + "unbill_end_bal_db_c," 
            + "unbill_end_bal_db_i," 
            + "billed_end_bal_db_b," 
            + "billed_end_bal_db_c,"
            + "billed_end_bal_db_i";
  daoTable  = "act_acct_hst";
  whereStr  = "where acct_type  != '01' "
            + "and   acct_month  = ? "
            + "and   stmt_cycle  = ? ";

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  int  n = loadTable();
  setLoadData("acht.p_seqno");
  showLogMessage("I","","Load act_acct_hst Count: ["+n+"]");
 }
// ************************************************************************
 void initActAcctHst() throws Exception
 {
  setValue("ahst.acct_jrnl_bal"       , "0");
  setValue("ahst.beg_bal_op"          , "0");
  setValue("ahst.end_bal_op"          , "0");
  setValue("ahst.beg_bal_lk"          , "0");
  setValue("ahst.end_bal_lk"          , "0");
  setValue("ahst.unbill_beg_bal_af"   , "0");
  setValue("ahst.unbill_beg_bal_lf"   , "0");
  setValue("ahst.unbill_beg_bal_cf"   , "0");
  setValue("ahst.unbill_beg_bal_pf"   , "0");
  setValue("ahst.unbill_beg_bal_bl"   , "0");
  setValue("ahst.unbill_beg_bal_ca"   , "0");
  setValue("ahst.unbill_beg_bal_it"   , "0");
  setValue("ahst.unbill_beg_bal_id"   , "0");
  setValue("ahst.unbill_beg_bal_ri"   , "0");
  setValue("ahst.unbill_beg_bal_pn"   , "0");
  setValue("ahst.unbill_beg_bal_ao"   , "0");
  setValue("ahst.unbill_beg_bal_ai"   , "0");
  setValue("ahst.unbill_beg_bal_sf"   , "0");
  setValue("ahst.unbill_beg_bal_dp"   , "0");
  setValue("ahst.unbill_beg_bal_cb"   , "0");
  setValue("ahst.unbill_beg_bal_ci"   , "0");
  setValue("ahst.unbill_beg_bal_cc"   , "0");
  setValue("ahst.unbill_beg_bal_db"   , "0");
  setValue("ahst.unbill_end_bal_af"   , "0");
  setValue("ahst.unbill_end_bal_lf"   , "0");
  setValue("ahst.unbill_end_bal_cf"   , "0");
  setValue("ahst.unbill_end_bal_pf"   , "0");
  setValue("ahst.unbill_end_bal_bl"   , "0");
  setValue("ahst.unbill_end_bal_ca"   , "0");
  setValue("ahst.unbill_end_bal_it"   , "0");
  setValue("ahst.unbill_end_bal_id"   , "0");
  setValue("ahst.unbill_end_bal_ri"   , "0");
  setValue("ahst.unbill_end_bal_pn"   , "0");
  setValue("ahst.unbill_end_bal_ao"   , "0");
  setValue("ahst.unbill_end_bal_ai"   , "0");
  setValue("ahst.unbill_end_bal_sf"   , "0");
  setValue("ahst.unbill_end_bal_dp"   , "0");
  setValue("ahst.unbill_end_bal_cb"   , "0");
  setValue("ahst.unbill_end_bal_ci"   , "0");
  setValue("ahst.unbill_end_bal_cc"   , "0");
  setValue("ahst.unbill_end_bal_db"   , "0");
  setValue("ahst.billed_beg_bal_af"   , "0");
  setValue("ahst.billed_beg_bal_lf"   , "0");
  setValue("ahst.billed_beg_bal_cf"   , "0");
  setValue("ahst.billed_beg_bal_pf"   , "0");
  setValue("ahst.billed_beg_bal_bl"   , "0");
  setValue("ahst.billed_beg_bal_ca"   , "0");
  setValue("ahst.billed_beg_bal_it"   , "0");
  setValue("ahst.billed_beg_bal_id"   , "0");
  setValue("ahst.billed_beg_bal_ri"   , "0");
  setValue("ahst.billed_beg_bal_pn"   , "0");
  setValue("ahst.billed_beg_bal_ao"   , "0");
  setValue("ahst.billed_beg_bal_ai"   , "0");
  setValue("ahst.billed_beg_bal_sf"   , "0");
  setValue("ahst.billed_beg_bal_dp"   , "0");
  setValue("ahst.billed_beg_bal_cb"   , "0");
  setValue("ahst.billed_beg_bal_ci"   , "0");
  setValue("ahst.billed_beg_bal_cc"   , "0");
  setValue("ahst.billed_beg_bal_db"   , "0");
  setValue("ahst.billed_end_bal_af"   , "0");
  setValue("ahst.billed_end_bal_lf"   , "0");
  setValue("ahst.billed_end_bal_cf"   , "0");
  setValue("ahst.billed_end_bal_pf"   , "0");
  setValue("ahst.billed_end_bal_bl"   , "0");
  setValue("ahst.billed_end_bal_ca"   , "0");
  setValue("ahst.billed_end_bal_it"   , "0");
  setValue("ahst.billed_end_bal_id"   , "0");
  setValue("ahst.billed_end_bal_ri"   , "0");
  setValue("ahst.billed_end_bal_pn"   , "0");
  setValue("ahst.billed_end_bal_ao"   , "0");
  setValue("ahst.billed_end_bal_ai"   , "0");
  setValue("ahst.billed_end_bal_sf"   , "0");
  setValue("ahst.billed_end_bal_dp"   , "0");
  setValue("ahst.billed_end_bal_cb"   , "0");
  setValue("ahst.billed_end_bal_ci"   , "0");
  setValue("ahst.billed_end_bal_cc"   , "0");
  setValue("ahst.billed_end_bal_db"   , "0");
  setValue("ahst.adi_beg_bal"         , "0");
  setValue("ahst.adi_end_bal"         , "0");
  setValue("ahst.adi_d_avail"         , "0");
  setValue("ahst.unbill_beg_bal_ot"   , "0");
  setValue("ahst.billed_beg_bal_ot"   , "0");
  setValue("ahst.unbill_end_bal_ot"   , "0");
  setValue("ahst.billed_end_bal_ot"   , "0");
  setValue("ahst.unbill_beg_bal_db_b" , "0");
  setValue("ahst.unbill_beg_bal_db_c" , "0");
  setValue("ahst.unbill_beg_bal_db_i" , "0");
  setValue("ahst.billed_beg_bal_db_b" , "0");
  setValue("ahst.billed_beg_bal_db_c" , "0");
  setValue("ahst.billed_beg_bal_db_i" , "0");
  setValue("ahst.unbill_end_bal_db_b" , "0");
  setValue("ahst.unbill_end_bal_db_c" , "0");
  setValue("ahst.unbill_end_bal_db_i" , "0");
  setValue("ahst.billed_end_bal_db_b" , "0");
  setValue("ahst.billed_end_bal_db_c" , "0");
  setValue("ahst.billed_end_bal_db_i" , "0");
 }
// ************************************************************************
 void caclActAcctHst() throws Exception
 {
  setValueDouble("ahst.acct_jrnl_bal"       , getValueDouble("ahst.acct_jrnl_bal") +       getValueDouble("acht.acct_jrnl_bal"));
  setValueDouble("ahst.beg_bal_op"          , getValueDouble("ahst.beg_bal_op") +          getValueDouble("acht.beg_bal_op"));
  setValueDouble("ahst.end_bal_op"          , getValueDouble("ahst.end_bal_op") +          getValueDouble("acht.end_bal_op"));
  setValueDouble("ahst.beg_bal_lk"          , getValueDouble("ahst.beg_bal_lk") +          getValueDouble("acht.beg_bal_lk"));
  setValueDouble("ahst.end_bal_lk"          , getValueDouble("ahst.end_bal_lk") +          getValueDouble("acht.end_bal_lk"));
  setValueDouble("ahst.unbill_beg_bal_af"   , getValueDouble("ahst.unbill_beg_bal_af") +   getValueDouble("acht.unbill_beg_bal_af"));
  setValueDouble("ahst.unbill_beg_bal_lf"   , getValueDouble("ahst.unbill_beg_bal_lf") +   getValueDouble("acht.unbill_beg_bal_lf"));
  setValueDouble("ahst.unbill_beg_bal_cf"   , getValueDouble("ahst.unbill_beg_bal_cf") +   getValueDouble("acht.unbill_beg_bal_cf"));
  setValueDouble("ahst.unbill_beg_bal_pf"   , getValueDouble("ahst.unbill_beg_bal_pf") +   getValueDouble("acht.unbill_beg_bal_pf"));
  setValueDouble("ahst.unbill_beg_bal_bl"   , getValueDouble("ahst.unbill_beg_bal_bl") +   getValueDouble("acht.unbill_beg_bal_bl"));
  setValueDouble("ahst.unbill_beg_bal_ca"   , getValueDouble("ahst.unbill_beg_bal_ca") +   getValueDouble("acht.unbill_beg_bal_ca"));
  setValueDouble("ahst.unbill_beg_bal_it"   , getValueDouble("ahst.unbill_beg_bal_it") +   getValueDouble("acht.unbill_beg_bal_it"));
  setValueDouble("ahst.unbill_beg_bal_id"   , getValueDouble("ahst.unbill_beg_bal_id") +   getValueDouble("acht.unbill_beg_bal_id"));
  setValueDouble("ahst.unbill_beg_bal_ri"   , getValueDouble("ahst.unbill_beg_bal_ri") +   getValueDouble("acht.unbill_beg_bal_ri"));
  setValueDouble("ahst.unbill_beg_bal_pn"   , getValueDouble("ahst.unbill_beg_bal_pn") +   getValueDouble("acht.unbill_beg_bal_pn"));
  setValueDouble("ahst.unbill_beg_bal_ao"   , getValueDouble("ahst.unbill_beg_bal_ao") +   getValueDouble("acht.unbill_beg_bal_ao"));
  setValueDouble("ahst.unbill_beg_bal_ai"   , getValueDouble("ahst.unbill_beg_bal_ai") +   getValueDouble("acht.unbill_beg_bal_ai"));
  setValueDouble("ahst.unbill_beg_bal_sf"   , getValueDouble("ahst.unbill_beg_bal_sf") +   getValueDouble("acht.unbill_beg_bal_sf"));
  setValueDouble("ahst.unbill_beg_bal_dp"   , getValueDouble("ahst.unbill_beg_bal_dp") +   getValueDouble("acht.unbill_beg_bal_dp"));
  setValueDouble("ahst.unbill_beg_bal_cb"   , getValueDouble("ahst.unbill_beg_bal_cb") +   getValueDouble("acht.unbill_beg_bal_cb"));
  setValueDouble("ahst.unbill_beg_bal_ci"   , getValueDouble("ahst.unbill_beg_bal_ci") +   getValueDouble("acht.unbill_beg_bal_ci"));
  setValueDouble("ahst.unbill_beg_bal_cc"   , getValueDouble("ahst.unbill_beg_bal_cc") +   getValueDouble("acht.unbill_beg_bal_cc"));
  setValueDouble("ahst.unbill_beg_bal_db"   , getValueDouble("ahst.unbill_beg_bal_db") +   getValueDouble("acht.unbill_beg_bal_db"));
  setValueDouble("ahst.unbill_end_bal_af"   , getValueDouble("ahst.unbill_end_bal_af") +   getValueDouble("acht.unbill_end_bal_af"));
  setValueDouble("ahst.unbill_end_bal_lf"   , getValueDouble("ahst.unbill_end_bal_lf") +   getValueDouble("acht.unbill_end_bal_lf"));
  setValueDouble("ahst.unbill_end_bal_cf"   , getValueDouble("ahst.unbill_end_bal_cf") +   getValueDouble("acht.unbill_end_bal_cf"));
  setValueDouble("ahst.unbill_end_bal_pf"   , getValueDouble("ahst.unbill_end_bal_pf") +   getValueDouble("acht.unbill_end_bal_pf"));
  setValueDouble("ahst.unbill_end_bal_bl"   , getValueDouble("ahst.unbill_end_bal_bl") +   getValueDouble("acht.unbill_end_bal_bl"));
  setValueDouble("ahst.unbill_end_bal_ca"   , getValueDouble("ahst.unbill_end_bal_ca") +   getValueDouble("acht.unbill_end_bal_ca"));
  setValueDouble("ahst.unbill_end_bal_it"   , getValueDouble("ahst.unbill_end_bal_it") +   getValueDouble("acht.unbill_end_bal_it"));
  setValueDouble("ahst.unbill_end_bal_id"   , getValueDouble("ahst.unbill_end_bal_id") +   getValueDouble("acht.unbill_end_bal_id"));
  setValueDouble("ahst.unbill_end_bal_ri"   , getValueDouble("ahst.unbill_end_bal_ri") +   getValueDouble("acht.unbill_end_bal_ri"));
  setValueDouble("ahst.unbill_end_bal_pn"   , getValueDouble("ahst.unbill_end_bal_pn") +   getValueDouble("acht.unbill_end_bal_pn"));
  setValueDouble("ahst.unbill_end_bal_ao"   , getValueDouble("ahst.unbill_end_bal_ao") +   getValueDouble("acht.unbill_end_bal_ao"));
  setValueDouble("ahst.unbill_end_bal_ai"   , getValueDouble("ahst.unbill_end_bal_ai") +   getValueDouble("acht.unbill_end_bal_ai"));
  setValueDouble("ahst.unbill_end_bal_sf"   , getValueDouble("ahst.unbill_end_bal_sf") +   getValueDouble("acht.unbill_end_bal_sf"));
  setValueDouble("ahst.unbill_end_bal_dp"   , getValueDouble("ahst.unbill_end_bal_dp") +   getValueDouble("acht.unbill_end_bal_dp"));
  setValueDouble("ahst.unbill_end_bal_cb"   , getValueDouble("ahst.unbill_end_bal_cb") +   getValueDouble("acht.unbill_end_bal_cb"));
  setValueDouble("ahst.unbill_end_bal_ci"   , getValueDouble("ahst.unbill_end_bal_ci") +   getValueDouble("acht.unbill_end_bal_ci"));
  setValueDouble("ahst.unbill_end_bal_cc"   , getValueDouble("ahst.unbill_end_bal_cc") +   getValueDouble("acht.unbill_end_bal_cc"));
  setValueDouble("ahst.unbill_end_bal_db"   , getValueDouble("ahst.unbill_end_bal_db") +   getValueDouble("acht.unbill_end_bal_db"));
  setValueDouble("ahst.billed_beg_bal_af"   , getValueDouble("ahst.billed_beg_bal_af") +   getValueDouble("acht.billed_beg_bal_af"));
  setValueDouble("ahst.billed_beg_bal_lf"   , getValueDouble("ahst.billed_beg_bal_lf") +   getValueDouble("acht.billed_beg_bal_lf"));
  setValueDouble("ahst.billed_beg_bal_cf"   , getValueDouble("ahst.billed_beg_bal_cf") +   getValueDouble("acht.billed_beg_bal_cf"));
  setValueDouble("ahst.billed_beg_bal_pf"   , getValueDouble("ahst.billed_beg_bal_pf") +   getValueDouble("acht.billed_beg_bal_pf"));
  setValueDouble("ahst.billed_beg_bal_bl"   , getValueDouble("ahst.billed_beg_bal_bl") +   getValueDouble("acht.billed_beg_bal_bl"));
  setValueDouble("ahst.billed_beg_bal_ca"   , getValueDouble("ahst.billed_beg_bal_ca") +   getValueDouble("acht.billed_beg_bal_ca"));
  setValueDouble("ahst.billed_beg_bal_it"   , getValueDouble("ahst.billed_beg_bal_it") +   getValueDouble("acht.billed_beg_bal_it"));
  setValueDouble("ahst.billed_beg_bal_id"   , getValueDouble("ahst.billed_beg_bal_id") +   getValueDouble("acht.billed_beg_bal_id"));
  setValueDouble("ahst.billed_beg_bal_ri"   , getValueDouble("ahst.billed_beg_bal_ri") +   getValueDouble("acht.billed_beg_bal_ri"));
  setValueDouble("ahst.billed_beg_bal_pn"   , getValueDouble("ahst.billed_beg_bal_pn") +   getValueDouble("acht.billed_beg_bal_pn"));
  setValueDouble("ahst.billed_beg_bal_ao"   , getValueDouble("ahst.billed_beg_bal_ao") +   getValueDouble("acht.billed_beg_bal_ao"));
  setValueDouble("ahst.billed_beg_bal_ai"   , getValueDouble("ahst.billed_beg_bal_ai") +   getValueDouble("acht.billed_beg_bal_ai"));
  setValueDouble("ahst.billed_beg_bal_sf"   , getValueDouble("ahst.billed_beg_bal_sf") +   getValueDouble("acht.billed_beg_bal_sf"));
  setValueDouble("ahst.billed_beg_bal_dp"   , getValueDouble("ahst.billed_beg_bal_dp") +   getValueDouble("acht.billed_beg_bal_dp"));
  setValueDouble("ahst.billed_beg_bal_cb"   , getValueDouble("ahst.billed_beg_bal_cb") +   getValueDouble("acht.billed_beg_bal_cb"));
  setValueDouble("ahst.billed_beg_bal_ci"   , getValueDouble("ahst.billed_beg_bal_ci") +   getValueDouble("acht.billed_beg_bal_ci"));
  setValueDouble("ahst.billed_beg_bal_cc"   , getValueDouble("ahst.billed_beg_bal_cc") +   getValueDouble("acht.billed_beg_bal_cc"));
  setValueDouble("ahst.billed_beg_bal_db"   , getValueDouble("ahst.billed_beg_bal_db") +   getValueDouble("acht.billed_beg_bal_db"));
  setValueDouble("ahst.billed_end_bal_af"   , getValueDouble("ahst.billed_end_bal_af") +   getValueDouble("acht.billed_end_bal_af"));
  setValueDouble("ahst.billed_end_bal_lf"   , getValueDouble("ahst.billed_end_bal_lf") +   getValueDouble("acht.billed_end_bal_lf"));
  setValueDouble("ahst.billed_end_bal_cf"   , getValueDouble("ahst.billed_end_bal_cf") +   getValueDouble("acht.billed_end_bal_cf"));
  setValueDouble("ahst.billed_end_bal_pf"   , getValueDouble("ahst.billed_end_bal_pf") +   getValueDouble("acht.billed_end_bal_pf"));
  setValueDouble("ahst.billed_end_bal_bl"   , getValueDouble("ahst.billed_end_bal_bl") +   getValueDouble("acht.billed_end_bal_bl"));
  setValueDouble("ahst.billed_end_bal_ca"   , getValueDouble("ahst.billed_end_bal_ca") +   getValueDouble("acht.billed_end_bal_ca"));
  setValueDouble("ahst.billed_end_bal_it"   , getValueDouble("ahst.billed_end_bal_it") +   getValueDouble("acht.billed_end_bal_it"));
  setValueDouble("ahst.billed_end_bal_id"   , getValueDouble("ahst.billed_end_bal_id") +   getValueDouble("acht.billed_end_bal_id"));
  setValueDouble("ahst.billed_end_bal_ri"   , getValueDouble("ahst.billed_end_bal_ri") +   getValueDouble("acht.billed_end_bal_ri"));
  setValueDouble("ahst.billed_end_bal_pn"   , getValueDouble("ahst.billed_end_bal_pn") +   getValueDouble("acht.billed_end_bal_pn"));
  setValueDouble("ahst.billed_end_bal_ao"   , getValueDouble("ahst.billed_end_bal_ao") +   getValueDouble("acht.billed_end_bal_ao"));
  setValueDouble("ahst.billed_end_bal_ai"   , getValueDouble("ahst.billed_end_bal_ai") +   getValueDouble("acht.billed_end_bal_ai"));
  setValueDouble("ahst.billed_end_bal_sf"   , getValueDouble("ahst.billed_end_bal_sf") +   getValueDouble("acht.billed_end_bal_sf"));
  setValueDouble("ahst.billed_end_bal_dp"   , getValueDouble("ahst.billed_end_bal_dp") +   getValueDouble("acht.billed_end_bal_dp"));
  setValueDouble("ahst.billed_end_bal_cb"   , getValueDouble("ahst.billed_end_bal_cb") +   getValueDouble("acht.billed_end_bal_cb"));
  setValueDouble("ahst.billed_end_bal_ci"   , getValueDouble("ahst.billed_end_bal_ci") +   getValueDouble("acht.billed_end_bal_ci"));
  setValueDouble("ahst.billed_end_bal_cc"   , getValueDouble("ahst.billed_end_bal_cc") +   getValueDouble("acht.billed_end_bal_cc"));
  setValueDouble("ahst.billed_end_bal_db"   , getValueDouble("ahst.billed_end_bal_db") +   getValueDouble("acht.billed_end_bal_db"));
  setValueDouble("ahst.adi_beg_bal"         , getValueDouble("ahst.adi_beg_bal") +         getValueDouble("acht.adi_beg_bal"));
  setValueDouble("ahst.adi_end_bal"         , getValueDouble("ahst.adi_end_bal") +         getValueDouble("acht.adi_end_bal"));
  setValueDouble("ahst.adi_d_avail"         , getValueDouble("ahst.adi_d_avail") +         getValueDouble("acht.adi_d_avail"));
  setValueDouble("ahst.unbill_beg_bal_ot"   , getValueDouble("ahst.unbill_beg_bal_ot") +   getValueDouble("acht.unbill_beg_bal_ot"));
  setValueDouble("ahst.billed_beg_bal_ot"   , getValueDouble("ahst.billed_beg_bal_ot") +   getValueDouble("acht.billed_beg_bal_ot"));
  setValueDouble("ahst.unbill_end_bal_ot"   , getValueDouble("ahst.unbill_end_bal_ot") +   getValueDouble("acht.unbill_end_bal_ot"));
  setValueDouble("ahst.billed_end_bal_ot"   , getValueDouble("ahst.billed_end_bal_ot") +   getValueDouble("acht.billed_end_bal_ot"));
  setValueDouble("ahst.unbill_beg_bal_db_b" , getValueDouble("ahst.unbill_beg_bal_db_b") + getValueDouble("acht.unbill_beg_bal_db_b"));
  setValueDouble("ahst.unbill_beg_bal_db_c" , getValueDouble("ahst.unbill_beg_bal_db_c") + getValueDouble("acht.unbill_beg_bal_db_c"));
  setValueDouble("ahst.unbill_beg_bal_db_i" , getValueDouble("ahst.unbill_beg_bal_db_i") + getValueDouble("acht.unbill_beg_bal_db_i"));
  setValueDouble("ahst.billed_beg_bal_db_b" , getValueDouble("ahst.billed_beg_bal_db_b") + getValueDouble("acht.billed_beg_bal_db_b"));
  setValueDouble("ahst.billed_beg_bal_db_c" , getValueDouble("ahst.billed_beg_bal_db_c") + getValueDouble("acht.billed_beg_bal_db_c"));
  setValueDouble("ahst.billed_beg_bal_db_i" , getValueDouble("ahst.billed_beg_bal_db_i") + getValueDouble("acht.billed_beg_bal_db_i"));
  setValueDouble("ahst.unbill_end_bal_db_b" , getValueDouble("ahst.unbill_end_bal_db_b") + getValueDouble("acht.unbill_end_bal_db_b"));
  setValueDouble("ahst.unbill_end_bal_db_c" , getValueDouble("ahst.unbill_end_bal_db_c") + getValueDouble("acht.unbill_end_bal_db_c"));
  setValueDouble("ahst.unbill_end_bal_db_i" , getValueDouble("ahst.unbill_end_bal_db_i") + getValueDouble("acht.unbill_end_bal_db_i"));
  setValueDouble("ahst.billed_end_bal_db_b" , getValueDouble("ahst.billed_end_bal_db_b") + getValueDouble("acht.billed_end_bal_db_b"));
  setValueDouble("ahst.billed_end_bal_db_c" , getValueDouble("ahst.billed_end_bal_db_c") + getValueDouble("acht.billed_end_bal_db_c"));
  setValueDouble("ahst.billed_end_bal_db_i" , getValueDouble("ahst.billed_end_bal_db_i") + getValueDouble("acht.billed_end_bal_db_i"));
 }
// ************************************************************************
 int updateActAcctHst() throws Exception
 {
  daoTable  = "act_acct_hst";
  updateSQL = "acct_jrnl_bal       = ?,"
            + "beg_bal_op          = ?,"
            + "end_bal_op          = ?,"
            + "beg_bal_lk          = ?,"
            + "end_bal_lk          = ?,"
            + "unbill_beg_bal_af   = ?,"
            + "unbill_beg_bal_lf   = ?,"
            + "unbill_beg_bal_cf   = ?,"
            + "unbill_beg_bal_pf   = ?,"
            + "unbill_beg_bal_bl   = ?,"
            + "unbill_beg_bal_ca   = ?,"
            + "unbill_beg_bal_it   = ?,"
            + "unbill_beg_bal_id   = ?,"
            + "unbill_beg_bal_ri   = ?,"
            + "unbill_beg_bal_pn   = ?,"
            + "unbill_beg_bal_ao   = ?,"
            + "unbill_beg_bal_ai   = ?,"
            + "unbill_beg_bal_sf   = ?,"
            + "unbill_beg_bal_dp   = ?,"
            + "unbill_beg_bal_cb   = ?,"
            + "unbill_beg_bal_ci   = ?,"
            + "unbill_beg_bal_cc   = ?,"
            + "unbill_beg_bal_db   = ?,"
            + "unbill_end_bal_af   = ?,"
            + "unbill_end_bal_lf   = ?,"
            + "unbill_end_bal_cf   = ?,"
            + "unbill_end_bal_pf   = ?,"
            + "unbill_end_bal_bl   = ?,"
            + "unbill_end_bal_ca   = ?,"
            + "unbill_end_bal_it   = ?,"
            + "unbill_end_bal_id   = ?,"
            + "unbill_end_bal_ri   = ?,"
            + "unbill_end_bal_pn   = ?,"
            + "unbill_end_bal_ao   = ?,"
            + "unbill_end_bal_ai   = ?,"
            + "unbill_end_bal_sf   = ?,"
            + "unbill_end_bal_dp   = ?,"
            + "unbill_end_bal_cb   = ?,"
            + "unbill_end_bal_ci   = ?,"
            + "unbill_end_bal_cc   = ?,"
            + "unbill_end_bal_db   = ?,"
            + "billed_beg_bal_af   = ?,"
            + "billed_beg_bal_lf   = ?,"
            + "billed_beg_bal_cf   = ?,"
            + "billed_beg_bal_pf   = ?,"
            + "billed_beg_bal_bl   = ?,"
            + "billed_beg_bal_ca   = ?,"
            + "billed_beg_bal_it   = ?,"
            + "billed_beg_bal_id   = ?,"
            + "billed_beg_bal_ri   = ?,"
            + "billed_beg_bal_pn   = ?,"
            + "billed_beg_bal_ao   = ?,"
            + "billed_beg_bal_ai   = ?,"
            + "billed_beg_bal_sf   = ?,"
            + "billed_beg_bal_dp   = ?,"
            + "billed_beg_bal_cb   = ?,"
            + "billed_beg_bal_ci   = ?,"
            + "billed_beg_bal_cc   = ?,"
            + "billed_beg_bal_db   = ?,"
            + "billed_end_bal_af   = ?,"
            + "billed_end_bal_lf   = ?,"
            + "billed_end_bal_cf   = ?,"
            + "billed_end_bal_pf   = ?,"
            + "billed_end_bal_bl   = ?,"
            + "billed_end_bal_ca   = ?,"
            + "billed_end_bal_it   = ?,"
            + "billed_end_bal_id   = ?,"
            + "billed_end_bal_ri   = ?,"
            + "billed_end_bal_pn   = ?,"
            + "billed_end_bal_ao   = ?,"
            + "billed_end_bal_ai   = ?,"
            + "billed_end_bal_sf   = ?,"
            + "billed_end_bal_dp   = ?,"
            + "billed_end_bal_cb   = ?,"
            + "billed_end_bal_ci   = ?,"
            + "billed_end_bal_cc   = ?,"
            + "billed_end_bal_db   = ?,"
            + "adi_beg_bal         = ?,"
            + "adi_end_bal         = ?,"
            + "adi_d_avail         = ?,"
            + "unbill_beg_bal_ot   = ?,"
            + "billed_beg_bal_ot   = ?,"
            + "unbill_end_bal_ot   = ?,"
            + "billed_end_bal_ot   = ?,"
            + "unbill_beg_bal_db_b = ?," 
            + "unbill_beg_bal_db_c = ?," 
            + "unbill_beg_bal_db_i = ?," 
            + "billed_beg_bal_db_b = ?," 
            + "billed_beg_bal_db_c = ?," 
            + "billed_beg_bal_db_i = ?," 
            + "unbill_end_bal_db_b = ?," 
            + "unbill_end_bal_db_c = ?," 
            + "unbill_end_bal_db_i = ?," 
            + "billed_end_bal_db_b = ?," 
            + "billed_end_bal_db_c = ?,"
            + "billed_end_bal_db_i = ?,"
            + "mod_pgm             = ?,"
            + "mod_time            = sysdate";
  whereStr  = "where p_seqno       = ? "
            + "and   acct_month    = ? "
            + "and   stmt_cycle    = ? ";

  setDouble(1  , getValueDouble("ahst.acct_jrnl_bal"));
  setDouble(2  , getValueDouble("ahst.beg_bal_op"));
  setDouble(3  , getValueDouble("ahst.end_bal_op"));
  setDouble(4  , getValueDouble("ahst.beg_bal_lk"));
  setDouble(5  , getValueDouble("ahst.end_bal_lk"));
  setDouble(6  , getValueDouble("ahst.unbill_beg_bal_af"));
  setDouble(7  , getValueDouble("ahst.unbill_beg_bal_lf"));
  setDouble(8  , getValueDouble("ahst.unbill_beg_bal_cf"));
  setDouble(9  , getValueDouble("ahst.unbill_beg_bal_pf"));
  setDouble(10 , getValueDouble("ahst.unbill_beg_bal_bl"));
  setDouble(11 , getValueDouble("ahst.unbill_beg_bal_ca"));
  setDouble(12 , getValueDouble("ahst.unbill_beg_bal_it"));
  setDouble(13 , getValueDouble("ahst.unbill_beg_bal_id"));
  setDouble(14 , getValueDouble("ahst.unbill_beg_bal_ri"));
  setDouble(15 , getValueDouble("ahst.unbill_beg_bal_pn"));
  setDouble(16 , getValueDouble("ahst.unbill_beg_bal_ao"));
  setDouble(17 , getValueDouble("ahst.unbill_beg_bal_ai"));
  setDouble(18 , getValueDouble("ahst.unbill_beg_bal_sf"));
  setDouble(19 , getValueDouble("ahst.unbill_beg_bal_dp"));
  setDouble(20 , getValueDouble("ahst.unbill_beg_bal_cb"));
  setDouble(21 , getValueDouble("ahst.unbill_beg_bal_ci"));
  setDouble(22 , getValueDouble("ahst.unbill_beg_bal_cc"));
  setDouble(23 , getValueDouble("ahst.unbill_beg_bal_db"));
  setDouble(24 , getValueDouble("ahst.unbill_end_bal_af"));
  setDouble(25 , getValueDouble("ahst.unbill_end_bal_lf"));
  setDouble(26 , getValueDouble("ahst.unbill_end_bal_cf"));
  setDouble(27 , getValueDouble("ahst.unbill_end_bal_pf"));
  setDouble(28 , getValueDouble("ahst.unbill_end_bal_bl"));
  setDouble(29 , getValueDouble("ahst.unbill_end_bal_ca"));
  setDouble(30 , getValueDouble("ahst.unbill_end_bal_it"));
  setDouble(31 , getValueDouble("ahst.unbill_end_bal_id"));
  setDouble(32 , getValueDouble("ahst.unbill_end_bal_ri"));
  setDouble(33 , getValueDouble("ahst.unbill_end_bal_pn"));
  setDouble(34 , getValueDouble("ahst.unbill_end_bal_ao"));
  setDouble(35 , getValueDouble("ahst.unbill_end_bal_ai"));
  setDouble(36 , getValueDouble("ahst.unbill_end_bal_sf"));
  setDouble(37 , getValueDouble("ahst.unbill_end_bal_dp"));
  setDouble(38 , getValueDouble("ahst.unbill_end_bal_cb"));
  setDouble(39 , getValueDouble("ahst.unbill_end_bal_ci"));
  setDouble(40 , getValueDouble("ahst.unbill_end_bal_cc"));
  setDouble(41 , getValueDouble("ahst.unbill_end_bal_db"));
  setDouble(42 , getValueDouble("ahst.billed_beg_bal_af"));
  setDouble(43 , getValueDouble("ahst.billed_beg_bal_lf"));
  setDouble(44 , getValueDouble("ahst.billed_beg_bal_cf"));
  setDouble(45 , getValueDouble("ahst.billed_beg_bal_pf"));
  setDouble(46 , getValueDouble("ahst.billed_beg_bal_bl"));
  setDouble(47 , getValueDouble("ahst.billed_beg_bal_ca"));
  setDouble(48 , getValueDouble("ahst.billed_beg_bal_it"));
  setDouble(49 , getValueDouble("ahst.billed_beg_bal_id"));
  setDouble(50 , getValueDouble("ahst.billed_beg_bal_ri"));
  setDouble(51 , getValueDouble("ahst.billed_beg_bal_pn"));
  setDouble(52 , getValueDouble("ahst.billed_beg_bal_ao"));
  setDouble(53 , getValueDouble("ahst.billed_beg_bal_ai"));
  setDouble(54 , getValueDouble("ahst.billed_beg_bal_sf"));
  setDouble(55 , getValueDouble("ahst.billed_beg_bal_dp"));
  setDouble(56 , getValueDouble("ahst.billed_beg_bal_cb"));
  setDouble(57 , getValueDouble("ahst.billed_beg_bal_ci"));
  setDouble(58 , getValueDouble("ahst.billed_beg_bal_cc"));
  setDouble(59 , getValueDouble("ahst.billed_beg_bal_db"));
  setDouble(60 , getValueDouble("ahst.billed_end_bal_af"));
  setDouble(61 , getValueDouble("ahst.billed_end_bal_lf"));
  setDouble(62 , getValueDouble("ahst.billed_end_bal_cf"));
  setDouble(63 , getValueDouble("ahst.billed_end_bal_pf"));
  setDouble(64 , getValueDouble("ahst.billed_end_bal_bl"));
  setDouble(65 , getValueDouble("ahst.billed_end_bal_ca"));
  setDouble(66 , getValueDouble("ahst.billed_end_bal_it"));
  setDouble(67 , getValueDouble("ahst.billed_end_bal_id"));
  setDouble(68 , getValueDouble("ahst.billed_end_bal_ri"));
  setDouble(69 , getValueDouble("ahst.billed_end_bal_pn"));
  setDouble(70 , getValueDouble("ahst.billed_end_bal_ao"));
  setDouble(71 , getValueDouble("ahst.billed_end_bal_ai"));
  setDouble(72 , getValueDouble("ahst.billed_end_bal_sf"));
  setDouble(73 , getValueDouble("ahst.billed_end_bal_dp"));
  setDouble(74 , getValueDouble("ahst.billed_end_bal_cb"));
  setDouble(75 , getValueDouble("ahst.billed_end_bal_ci"));
  setDouble(76 , getValueDouble("ahst.billed_end_bal_cc"));
  setDouble(77 , getValueDouble("ahst.billed_end_bal_db"));
  setDouble(78 , getValueDouble("ahst.adi_beg_bal"));
  setDouble(79 , getValueDouble("ahst.adi_end_bal"));
  setDouble(80 , getValueDouble("ahst.adi_d_avail"));
  setDouble(81 , getValueDouble("ahst.unbill_beg_bal_ot"));
  setDouble(82 , getValueDouble("ahst.billed_beg_bal_ot"));
  setDouble(83 , getValueDouble("ahst.unbill_end_bal_ot"));
  setDouble(84 , getValueDouble("ahst.billed_end_bal_ot"));
  setDouble(85 , getValueDouble("ahst.unbill_beg_bal_db_b"));
  setDouble(86 , getValueDouble("ahst.unbill_beg_bal_db_c"));
  setDouble(87 , getValueDouble("ahst.unbill_beg_bal_db_i"));
  setDouble(88 , getValueDouble("ahst.billed_beg_bal_db_b"));
  setDouble(89 , getValueDouble("ahst.billed_beg_bal_db_c"));
  setDouble(90 , getValueDouble("ahst.billed_beg_bal_db_i"));
  setDouble(91 , getValueDouble("ahst.unbill_end_bal_db_b"));
  setDouble(92 , getValueDouble("ahst.unbill_end_bal_db_c"));
  setDouble(93 , getValueDouble("ahst.unbill_end_bal_db_i"));
  setDouble(94 , getValueDouble("ahst.billed_end_bal_db_b"));
  setDouble(95 , getValueDouble("ahst.billed_end_bal_db_c"));
  setDouble(96 , getValueDouble("ahst.billed_end_bal_db_i"));
  setString(97 , javaProgram);
  setString(98 , getValue("p_seqno"));
  setString(99 , getValue("wday.last_acct_month"));
  setString(100, getValue("wday.stmt_cycle"));

  int n = updateTable();
  if (n <= 0) {
    showLogMessage("I","", "update_act_acct_hst notfnd, p_seqno= "+
    getValue("p_seqno")+", acct_month= "+getValue("wday.last_acct_month"));
    perNfndCnt++;
  }

  return n;
 }
// ************************************************************************
 void loadActCurrHst() throws Exception
 {
  extendField = "cuht.";
  selectSQL = "p_seqno,"
            + "curr_code," 
            + "acct_jrnl_bal," 
            + "unbill_beg_bal_af," 
            + "unbill_beg_bal_lf," 
            + "unbill_beg_bal_cf," 
            + "unbill_beg_bal_pf," 
            + "unbill_beg_bal_bl," 
            + "unbill_beg_bal_ca," 
            + "unbill_beg_bal_it," 
            + "unbill_beg_bal_id," 
            + "unbill_beg_bal_ri," 
            + "unbill_beg_bal_pn," 
            + "unbill_beg_bal_ao," 
            + "unbill_beg_bal_ai," 
            + "unbill_beg_bal_sf," 
            + "unbill_beg_bal_dp," 
            + "unbill_beg_bal_cb," 
            + "unbill_beg_bal_ci," 
            + "unbill_beg_bal_cc," 
            + "unbill_beg_bal_db," 
            + "unbill_end_bal_af," 
            + "unbill_end_bal_lf," 
            + "unbill_end_bal_cf," 
            + "unbill_end_bal_pf," 
            + "unbill_end_bal_bl," 
            + "unbill_end_bal_ca," 
            + "unbill_end_bal_it," 
            + "unbill_end_bal_id," 
            + "unbill_end_bal_ri," 
            + "unbill_end_bal_pn," 
            + "unbill_end_bal_ao," 
            + "unbill_end_bal_ai," 
            + "unbill_end_bal_sf," 
            + "unbill_end_bal_dp," 
            + "unbill_end_bal_cb," 
            + "unbill_end_bal_ci," 
            + "unbill_end_bal_cc," 
            + "unbill_end_bal_db," 
            + "billed_beg_bal_af," 
            + "billed_beg_bal_lf," 
            + "billed_beg_bal_cf," 
            + "billed_beg_bal_pf," 
            + "billed_beg_bal_bl," 
            + "billed_beg_bal_ca," 
            + "billed_beg_bal_it," 
            + "billed_beg_bal_id," 
            + "billed_beg_bal_ri," 
            + "billed_beg_bal_pn," 
            + "billed_beg_bal_ao," 
            + "billed_beg_bal_ai," 
            + "billed_beg_bal_sf," 
            + "billed_beg_bal_dp," 
            + "billed_beg_bal_cb," 
            + "billed_beg_bal_ci," 
            + "billed_beg_bal_cc," 
            + "billed_beg_bal_db," 
            + "billed_end_bal_af," 
            + "billed_end_bal_lf," 
            + "billed_end_bal_cf," 
            + "billed_end_bal_pf," 
            + "billed_end_bal_bl," 
            + "billed_end_bal_ca," 
            + "billed_end_bal_it," 
            + "billed_end_bal_id," 
            + "billed_end_bal_ri," 
            + "billed_end_bal_pn," 
            + "billed_end_bal_ao," 
            + "billed_end_bal_ai," 
            + "billed_end_bal_sf," 
            + "billed_end_bal_dp," 
            + "billed_end_bal_cb," 
            + "billed_end_bal_ci," 
            + "billed_end_bal_cc," 
            + "billed_end_bal_db,"
            + "unbill_beg_bal_ot," 
            + "billed_beg_bal_ot," 
            + "unbill_end_bal_ot," 
            + "billed_end_bal_ot"; 
  daoTable  = "act_curr_hst";
  whereStr  = "where acct_type  != '01' "
            + "and   acct_month  = ? "
            + "and   stmt_cycle  = ? ";

  setString(1 , getValue("wday.last_acct_month"));
  setString(2 , getValue("wday.stmt_cycle"));

  int  n = loadTable();
  setLoadData("cuht.p_seqno");
  showLogMessage("I","","Load act_curr_hst Count: ["+n+"]");
 }
// ************************************************************************
 void initActCurrHst() throws Exception
 {
  setValue("chst.acct_jrnl_bal"       , "0");
  setValue("chst.unbill_beg_bal_af"   , "0");
  setValue("chst.unbill_beg_bal_lf"   , "0");
  setValue("chst.unbill_beg_bal_cf"   , "0");
  setValue("chst.unbill_beg_bal_pf"   , "0");
  setValue("chst.unbill_beg_bal_bl"   , "0");
  setValue("chst.unbill_beg_bal_ca"   , "0");
  setValue("chst.unbill_beg_bal_it"   , "0");
  setValue("chst.unbill_beg_bal_id"   , "0");
  setValue("chst.unbill_beg_bal_ri"   , "0");
  setValue("chst.unbill_beg_bal_pn"   , "0");
  setValue("chst.unbill_beg_bal_ao"   , "0");
  setValue("chst.unbill_beg_bal_ai"   , "0");
  setValue("chst.unbill_beg_bal_sf"   , "0");
  setValue("chst.unbill_beg_bal_dp"   , "0");
  setValue("chst.unbill_beg_bal_cb"   , "0");
  setValue("chst.unbill_beg_bal_ci"   , "0");
  setValue("chst.unbill_beg_bal_cc"   , "0");
  setValue("chst.unbill_beg_bal_db"   , "0");
  setValue("chst.unbill_end_bal_af"   , "0");
  setValue("chst.unbill_end_bal_lf"   , "0");
  setValue("chst.unbill_end_bal_cf"   , "0");
  setValue("chst.unbill_end_bal_pf"   , "0");
  setValue("chst.unbill_end_bal_bl"   , "0");
  setValue("chst.unbill_end_bal_ca"   , "0");
  setValue("chst.unbill_end_bal_it"   , "0");
  setValue("chst.unbill_end_bal_id"   , "0");
  setValue("chst.unbill_end_bal_ri"   , "0");
  setValue("chst.unbill_end_bal_pn"   , "0");
  setValue("chst.unbill_end_bal_ao"   , "0");
  setValue("chst.unbill_end_bal_ai"   , "0");
  setValue("chst.unbill_end_bal_sf"   , "0");
  setValue("chst.unbill_end_bal_dp"   , "0");
  setValue("chst.unbill_end_bal_cb"   , "0");
  setValue("chst.unbill_end_bal_ci"   , "0");
  setValue("chst.unbill_end_bal_cc"   , "0");
  setValue("chst.unbill_end_bal_db"   , "0");
  setValue("chst.billed_beg_bal_af"   , "0");
  setValue("chst.billed_beg_bal_lf"   , "0");
  setValue("chst.billed_beg_bal_cf"   , "0");
  setValue("chst.billed_beg_bal_pf"   , "0");
  setValue("chst.billed_beg_bal_bl"   , "0");
  setValue("chst.billed_beg_bal_ca"   , "0");
  setValue("chst.billed_beg_bal_it"   , "0");
  setValue("chst.billed_beg_bal_id"   , "0");
  setValue("chst.billed_beg_bal_ri"   , "0");
  setValue("chst.billed_beg_bal_pn"   , "0");
  setValue("chst.billed_beg_bal_ao"   , "0");
  setValue("chst.billed_beg_bal_ai"   , "0");
  setValue("chst.billed_beg_bal_sf"   , "0");
  setValue("chst.billed_beg_bal_dp"   , "0");
  setValue("chst.billed_beg_bal_cb"   , "0");
  setValue("chst.billed_beg_bal_ci"   , "0");
  setValue("chst.billed_beg_bal_cc"   , "0");
  setValue("chst.billed_beg_bal_db"   , "0");
  setValue("chst.billed_end_bal_af"   , "0");
  setValue("chst.billed_end_bal_lf"   , "0");
  setValue("chst.billed_end_bal_cf"   , "0");
  setValue("chst.billed_end_bal_pf"   , "0");
  setValue("chst.billed_end_bal_bl"   , "0");
  setValue("chst.billed_end_bal_ca"   , "0");
  setValue("chst.billed_end_bal_it"   , "0");
  setValue("chst.billed_end_bal_id"   , "0");
  setValue("chst.billed_end_bal_ri"   , "0");
  setValue("chst.billed_end_bal_pn"   , "0");
  setValue("chst.billed_end_bal_ao"   , "0");
  setValue("chst.billed_end_bal_ai"   , "0");
  setValue("chst.billed_end_bal_sf"   , "0");
  setValue("chst.billed_end_bal_dp"   , "0");
  setValue("chst.billed_end_bal_cb"   , "0");
  setValue("chst.billed_end_bal_ci"   , "0");
  setValue("chst.billed_end_bal_cc"   , "0");
  setValue("chst.billed_end_bal_db"   , "0");
  setValue("chst.unbill_beg_bal_ot"   , "0");
  setValue("chst.billed_beg_bal_ot"   , "0");
  setValue("chst.unbill_end_bal_ot"   , "0");
  setValue("chst.billed_end_bal_ot"   , "0");
 }
// ************************************************************************
 void caclActCurrHst() throws Exception
 {
  setValueDouble("chst.acct_jrnl_bal"       , getValueDouble("chst.acct_jrnl_bal") +       getValueDouble("cuht.acct_jrnl_bal"));
  setValueDouble("chst.unbill_beg_bal_af"   , getValueDouble("chst.unbill_beg_bal_af") +   getValueDouble("cuht.unbill_beg_bal_af"));
  setValueDouble("chst.unbill_beg_bal_lf"   , getValueDouble("chst.unbill_beg_bal_lf") +   getValueDouble("cuht.unbill_beg_bal_lf"));
  setValueDouble("chst.unbill_beg_bal_cf"   , getValueDouble("chst.unbill_beg_bal_cf") +   getValueDouble("cuht.unbill_beg_bal_cf"));
  setValueDouble("chst.unbill_beg_bal_pf"   , getValueDouble("chst.unbill_beg_bal_pf") +   getValueDouble("cuht.unbill_beg_bal_pf"));
  setValueDouble("chst.unbill_beg_bal_bl"   , getValueDouble("chst.unbill_beg_bal_bl") +   getValueDouble("cuht.unbill_beg_bal_bl"));
  setValueDouble("chst.unbill_beg_bal_ca"   , getValueDouble("chst.unbill_beg_bal_ca") +   getValueDouble("cuht.unbill_beg_bal_ca"));
  setValueDouble("chst.unbill_beg_bal_it"   , getValueDouble("chst.unbill_beg_bal_it") +   getValueDouble("cuht.unbill_beg_bal_it"));
  setValueDouble("chst.unbill_beg_bal_id"   , getValueDouble("chst.unbill_beg_bal_id") +   getValueDouble("cuht.unbill_beg_bal_id"));
  setValueDouble("chst.unbill_beg_bal_ri"   , getValueDouble("chst.unbill_beg_bal_ri") +   getValueDouble("cuht.unbill_beg_bal_ri"));
  setValueDouble("chst.unbill_beg_bal_pn"   , getValueDouble("chst.unbill_beg_bal_pn") +   getValueDouble("cuht.unbill_beg_bal_pn"));
  setValueDouble("chst.unbill_beg_bal_ao"   , getValueDouble("chst.unbill_beg_bal_ao") +   getValueDouble("cuht.unbill_beg_bal_ao"));
  setValueDouble("chst.unbill_beg_bal_ai"   , getValueDouble("chst.unbill_beg_bal_ai") +   getValueDouble("cuht.unbill_beg_bal_ai"));
  setValueDouble("chst.unbill_beg_bal_sf"   , getValueDouble("chst.unbill_beg_bal_sf") +   getValueDouble("cuht.unbill_beg_bal_sf"));
  setValueDouble("chst.unbill_beg_bal_dp"   , getValueDouble("chst.unbill_beg_bal_dp") +   getValueDouble("cuht.unbill_beg_bal_dp"));
  setValueDouble("chst.unbill_beg_bal_cb"   , getValueDouble("chst.unbill_beg_bal_cb") +   getValueDouble("cuht.unbill_beg_bal_cb"));
  setValueDouble("chst.unbill_beg_bal_ci"   , getValueDouble("chst.unbill_beg_bal_ci") +   getValueDouble("cuht.unbill_beg_bal_ci"));
  setValueDouble("chst.unbill_beg_bal_cc"   , getValueDouble("chst.unbill_beg_bal_cc") +   getValueDouble("cuht.unbill_beg_bal_cc"));
  setValueDouble("chst.unbill_beg_bal_db"   , getValueDouble("chst.unbill_beg_bal_db") +   getValueDouble("cuht.unbill_beg_bal_db"));
  setValueDouble("chst.unbill_end_bal_af"   , getValueDouble("chst.unbill_end_bal_af") +   getValueDouble("cuht.unbill_end_bal_af"));
  setValueDouble("chst.unbill_end_bal_lf"   , getValueDouble("chst.unbill_end_bal_lf") +   getValueDouble("cuht.unbill_end_bal_lf"));
  setValueDouble("chst.unbill_end_bal_cf"   , getValueDouble("chst.unbill_end_bal_cf") +   getValueDouble("cuht.unbill_end_bal_cf"));
  setValueDouble("chst.unbill_end_bal_pf"   , getValueDouble("chst.unbill_end_bal_pf") +   getValueDouble("cuht.unbill_end_bal_pf"));
  setValueDouble("chst.unbill_end_bal_bl"   , getValueDouble("chst.unbill_end_bal_bl") +   getValueDouble("cuht.unbill_end_bal_bl"));
  setValueDouble("chst.unbill_end_bal_ca"   , getValueDouble("chst.unbill_end_bal_ca") +   getValueDouble("cuht.unbill_end_bal_ca"));
  setValueDouble("chst.unbill_end_bal_it"   , getValueDouble("chst.unbill_end_bal_it") +   getValueDouble("cuht.unbill_end_bal_it"));
  setValueDouble("chst.unbill_end_bal_id"   , getValueDouble("chst.unbill_end_bal_id") +   getValueDouble("cuht.unbill_end_bal_id"));
  setValueDouble("chst.unbill_end_bal_ri"   , getValueDouble("chst.unbill_end_bal_ri") +   getValueDouble("cuht.unbill_end_bal_ri"));
  setValueDouble("chst.unbill_end_bal_pn"   , getValueDouble("chst.unbill_end_bal_pn") +   getValueDouble("cuht.unbill_end_bal_pn"));
  setValueDouble("chst.unbill_end_bal_ao"   , getValueDouble("chst.unbill_end_bal_ao") +   getValueDouble("cuht.unbill_end_bal_ao"));
  setValueDouble("chst.unbill_end_bal_ai"   , getValueDouble("chst.unbill_end_bal_ai") +   getValueDouble("cuht.unbill_end_bal_ai"));
  setValueDouble("chst.unbill_end_bal_sf"   , getValueDouble("chst.unbill_end_bal_sf") +   getValueDouble("cuht.unbill_end_bal_sf"));
  setValueDouble("chst.unbill_end_bal_dp"   , getValueDouble("chst.unbill_end_bal_dp") +   getValueDouble("cuht.unbill_end_bal_dp"));
  setValueDouble("chst.unbill_end_bal_cb"   , getValueDouble("chst.unbill_end_bal_cb") +   getValueDouble("cuht.unbill_end_bal_cb"));
  setValueDouble("chst.unbill_end_bal_ci"   , getValueDouble("chst.unbill_end_bal_ci") +   getValueDouble("cuht.unbill_end_bal_ci"));
  setValueDouble("chst.unbill_end_bal_cc"   , getValueDouble("chst.unbill_end_bal_cc") +   getValueDouble("cuht.unbill_end_bal_cc"));
  setValueDouble("chst.unbill_end_bal_db"   , getValueDouble("chst.unbill_end_bal_db") +   getValueDouble("cuht.unbill_end_bal_db"));
  setValueDouble("chst.billed_beg_bal_af"   , getValueDouble("chst.billed_beg_bal_af") +   getValueDouble("cuht.billed_beg_bal_af"));
  setValueDouble("chst.billed_beg_bal_lf"   , getValueDouble("chst.billed_beg_bal_lf") +   getValueDouble("cuht.billed_beg_bal_lf"));
  setValueDouble("chst.billed_beg_bal_cf"   , getValueDouble("chst.billed_beg_bal_cf") +   getValueDouble("cuht.billed_beg_bal_cf"));
  setValueDouble("chst.billed_beg_bal_pf"   , getValueDouble("chst.billed_beg_bal_pf") +   getValueDouble("cuht.billed_beg_bal_pf"));
  setValueDouble("chst.billed_beg_bal_bl"   , getValueDouble("chst.billed_beg_bal_bl") +   getValueDouble("cuht.billed_beg_bal_bl"));
  setValueDouble("chst.billed_beg_bal_ca"   , getValueDouble("chst.billed_beg_bal_ca") +   getValueDouble("cuht.billed_beg_bal_ca"));
  setValueDouble("chst.billed_beg_bal_it"   , getValueDouble("chst.billed_beg_bal_it") +   getValueDouble("cuht.billed_beg_bal_it"));
  setValueDouble("chst.billed_beg_bal_id"   , getValueDouble("chst.billed_beg_bal_id") +   getValueDouble("cuht.billed_beg_bal_id"));
  setValueDouble("chst.billed_beg_bal_ri"   , getValueDouble("chst.billed_beg_bal_ri") +   getValueDouble("cuht.billed_beg_bal_ri"));
  setValueDouble("chst.billed_beg_bal_pn"   , getValueDouble("chst.billed_beg_bal_pn") +   getValueDouble("cuht.billed_beg_bal_pn"));
  setValueDouble("chst.billed_beg_bal_ao"   , getValueDouble("chst.billed_beg_bal_ao") +   getValueDouble("cuht.billed_beg_bal_ao"));
  setValueDouble("chst.billed_beg_bal_ai"   , getValueDouble("chst.billed_beg_bal_ai") +   getValueDouble("cuht.billed_beg_bal_ai"));
  setValueDouble("chst.billed_beg_bal_sf"   , getValueDouble("chst.billed_beg_bal_sf") +   getValueDouble("cuht.billed_beg_bal_sf"));
  setValueDouble("chst.billed_beg_bal_dp"   , getValueDouble("chst.billed_beg_bal_dp") +   getValueDouble("cuht.billed_beg_bal_dp"));
  setValueDouble("chst.billed_beg_bal_cb"   , getValueDouble("chst.billed_beg_bal_cb") +   getValueDouble("cuht.billed_beg_bal_cb"));
  setValueDouble("chst.billed_beg_bal_ci"   , getValueDouble("chst.billed_beg_bal_ci") +   getValueDouble("cuht.billed_beg_bal_ci"));
  setValueDouble("chst.billed_beg_bal_cc"   , getValueDouble("chst.billed_beg_bal_cc") +   getValueDouble("cuht.billed_beg_bal_cc"));
  setValueDouble("chst.billed_beg_bal_db"   , getValueDouble("chst.billed_beg_bal_db") +   getValueDouble("cuht.billed_beg_bal_db"));
  setValueDouble("chst.billed_end_bal_af"   , getValueDouble("chst.billed_end_bal_af") +   getValueDouble("cuht.billed_end_bal_af"));
  setValueDouble("chst.billed_end_bal_lf"   , getValueDouble("chst.billed_end_bal_lf") +   getValueDouble("cuht.billed_end_bal_lf"));
  setValueDouble("chst.billed_end_bal_cf"   , getValueDouble("chst.billed_end_bal_cf") +   getValueDouble("cuht.billed_end_bal_cf"));
  setValueDouble("chst.billed_end_bal_pf"   , getValueDouble("chst.billed_end_bal_pf") +   getValueDouble("cuht.billed_end_bal_pf"));
  setValueDouble("chst.billed_end_bal_bl"   , getValueDouble("chst.billed_end_bal_bl") +   getValueDouble("cuht.billed_end_bal_bl"));
  setValueDouble("chst.billed_end_bal_ca"   , getValueDouble("chst.billed_end_bal_ca") +   getValueDouble("cuht.billed_end_bal_ca"));
  setValueDouble("chst.billed_end_bal_it"   , getValueDouble("chst.billed_end_bal_it") +   getValueDouble("cuht.billed_end_bal_it"));
  setValueDouble("chst.billed_end_bal_id"   , getValueDouble("chst.billed_end_bal_id") +   getValueDouble("cuht.billed_end_bal_id"));
  setValueDouble("chst.billed_end_bal_ri"   , getValueDouble("chst.billed_end_bal_ri") +   getValueDouble("cuht.billed_end_bal_ri"));
  setValueDouble("chst.billed_end_bal_pn"   , getValueDouble("chst.billed_end_bal_pn") +   getValueDouble("cuht.billed_end_bal_pn"));
  setValueDouble("chst.billed_end_bal_ao"   , getValueDouble("chst.billed_end_bal_ao") +   getValueDouble("cuht.billed_end_bal_ao"));
  setValueDouble("chst.billed_end_bal_ai"   , getValueDouble("chst.billed_end_bal_ai") +   getValueDouble("cuht.billed_end_bal_ai"));
  setValueDouble("chst.billed_end_bal_sf"   , getValueDouble("chst.billed_end_bal_sf") +   getValueDouble("cuht.billed_end_bal_sf"));
  setValueDouble("chst.billed_end_bal_dp"   , getValueDouble("chst.billed_end_bal_dp") +   getValueDouble("cuht.billed_end_bal_dp"));
  setValueDouble("chst.billed_end_bal_cb"   , getValueDouble("chst.billed_end_bal_cb") +   getValueDouble("cuht.billed_end_bal_cb"));
  setValueDouble("chst.billed_end_bal_ci"   , getValueDouble("chst.billed_end_bal_ci") +   getValueDouble("cuht.billed_end_bal_ci"));
  setValueDouble("chst.billed_end_bal_cc"   , getValueDouble("chst.billed_end_bal_cc") +   getValueDouble("cuht.billed_end_bal_cc"));
  setValueDouble("chst.billed_end_bal_db"   , getValueDouble("chst.billed_end_bal_db") +   getValueDouble("cuht.billed_end_bal_db"));
  setValueDouble("chst.unbill_beg_bal_ot"   , getValueDouble("chst.unbill_beg_bal_ot") +   getValueDouble("cuht.unbill_beg_bal_ot"));
  setValueDouble("chst.billed_beg_bal_ot"   , getValueDouble("chst.billed_beg_bal_ot") +   getValueDouble("cuht.billed_beg_bal_ot"));
  setValueDouble("chst.unbill_end_bal_ot"   , getValueDouble("chst.unbill_end_bal_ot") +   getValueDouble("cuht.unbill_end_bal_ot"));
  setValueDouble("chst.billed_end_bal_ot"   , getValueDouble("chst.billed_end_bal_ot") +   getValueDouble("cuht.billed_end_bal_ot"));
 }
// ************************************************************************
 int updateActCurrHst() throws Exception
 {
  daoTable  = "act_curr_hst";
  updateSQL = "acct_jrnl_bal       = ?,"
            + "unbill_beg_bal_af   = ?,"
            + "unbill_beg_bal_lf   = ?,"
            + "unbill_beg_bal_cf   = ?,"
            + "unbill_beg_bal_pf   = ?,"
            + "unbill_beg_bal_bl   = ?,"
            + "unbill_beg_bal_ca   = ?,"
            + "unbill_beg_bal_it   = ?,"
            + "unbill_beg_bal_id   = ?,"
            + "unbill_beg_bal_ri   = ?,"
            + "unbill_beg_bal_pn   = ?,"
            + "unbill_beg_bal_ao   = ?,"
            + "unbill_beg_bal_ai   = ?,"
            + "unbill_beg_bal_sf   = ?,"
            + "unbill_beg_bal_dp   = ?,"
            + "unbill_beg_bal_cb   = ?,"
            + "unbill_beg_bal_ci   = ?,"
            + "unbill_beg_bal_cc   = ?,"
            + "unbill_beg_bal_db   = ?,"
            + "unbill_end_bal_af   = ?,"
            + "unbill_end_bal_lf   = ?,"
            + "unbill_end_bal_cf   = ?,"
            + "unbill_end_bal_pf   = ?,"
            + "unbill_end_bal_bl   = ?,"
            + "unbill_end_bal_ca   = ?,"
            + "unbill_end_bal_it   = ?,"
            + "unbill_end_bal_id   = ?,"
            + "unbill_end_bal_ri   = ?,"
            + "unbill_end_bal_pn   = ?,"
            + "unbill_end_bal_ao   = ?,"
            + "unbill_end_bal_ai   = ?,"
            + "unbill_end_bal_sf   = ?,"
            + "unbill_end_bal_dp   = ?,"
            + "unbill_end_bal_cb   = ?,"
            + "unbill_end_bal_ci   = ?,"
            + "unbill_end_bal_cc   = ?,"
            + "unbill_end_bal_db   = ?,"
            + "billed_beg_bal_af   = ?,"
            + "billed_beg_bal_lf   = ?,"
            + "billed_beg_bal_cf   = ?,"
            + "billed_beg_bal_pf   = ?,"
            + "billed_beg_bal_bl   = ?,"
            + "billed_beg_bal_ca   = ?,"
            + "billed_beg_bal_it   = ?,"
            + "billed_beg_bal_id   = ?,"
            + "billed_beg_bal_ri   = ?,"
            + "billed_beg_bal_pn   = ?,"
            + "billed_beg_bal_ao   = ?,"
            + "billed_beg_bal_ai   = ?,"
            + "billed_beg_bal_sf   = ?,"
            + "billed_beg_bal_dp   = ?,"
            + "billed_beg_bal_cb   = ?,"
            + "billed_beg_bal_ci   = ?,"
            + "billed_beg_bal_cc   = ?,"
            + "billed_beg_bal_db   = ?,"
            + "billed_end_bal_af   = ?,"
            + "billed_end_bal_lf   = ?,"
            + "billed_end_bal_cf   = ?,"
            + "billed_end_bal_pf   = ?,"
            + "billed_end_bal_bl   = ?,"
            + "billed_end_bal_ca   = ?,"
            + "billed_end_bal_it   = ?,"
            + "billed_end_bal_id   = ?,"
            + "billed_end_bal_ri   = ?,"
            + "billed_end_bal_pn   = ?,"
            + "billed_end_bal_ao   = ?,"
            + "billed_end_bal_ai   = ?,"
            + "billed_end_bal_sf   = ?,"
            + "billed_end_bal_dp   = ?,"
            + "billed_end_bal_cb   = ?,"
            + "billed_end_bal_ci   = ?,"
            + "billed_end_bal_cc   = ?,"
            + "billed_end_bal_db   = ?,"
            + "unbill_beg_bal_ot   = ?,"
            + "billed_beg_bal_ot   = ?,"
            + "unbill_end_bal_ot   = ?,"
            + "billed_end_bal_ot   = ?,"
            + "mod_pgm             = ?,"
            + "mod_time            = sysdate";
  whereStr  = "where p_seqno       = ? "
            + "and   acct_month    = ? "
            + "and   stmt_cycle    = ? ";
//商務卡沒有外幣卡，不必判斷curr_code

  setDouble(1  , getValueDouble("chst.acct_jrnl_bal"));
  setDouble(2  , getValueDouble("chst.unbill_beg_bal_af"));
  setDouble(3  , getValueDouble("chst.unbill_beg_bal_lf"));
  setDouble(4  , getValueDouble("chst.unbill_beg_bal_cf"));
  setDouble(5  , getValueDouble("chst.unbill_beg_bal_pf"));
  setDouble(6  , getValueDouble("chst.unbill_beg_bal_bl"));
  setDouble(7  , getValueDouble("chst.unbill_beg_bal_ca"));
  setDouble(8  , getValueDouble("chst.unbill_beg_bal_it"));
  setDouble(9  , getValueDouble("chst.unbill_beg_bal_id"));
  setDouble(10 , getValueDouble("chst.unbill_beg_bal_ri"));
  setDouble(11 , getValueDouble("chst.unbill_beg_bal_pn"));
  setDouble(12 , getValueDouble("chst.unbill_beg_bal_ao"));
  setDouble(13 , getValueDouble("chst.unbill_beg_bal_ai"));
  setDouble(14 , getValueDouble("chst.unbill_beg_bal_sf"));
  setDouble(15 , getValueDouble("chst.unbill_beg_bal_dp"));
  setDouble(16 , getValueDouble("chst.unbill_beg_bal_cb"));
  setDouble(17 , getValueDouble("chst.unbill_beg_bal_ci"));
  setDouble(18 , getValueDouble("chst.unbill_beg_bal_cc"));
  setDouble(19 , getValueDouble("chst.unbill_beg_bal_db"));
  setDouble(20 , getValueDouble("chst.unbill_end_bal_af"));
  setDouble(21 , getValueDouble("chst.unbill_end_bal_lf"));
  setDouble(22 , getValueDouble("chst.unbill_end_bal_cf"));
  setDouble(23 , getValueDouble("chst.unbill_end_bal_pf"));
  setDouble(24 , getValueDouble("chst.unbill_end_bal_bl"));
  setDouble(25 , getValueDouble("chst.unbill_end_bal_ca"));
  setDouble(26 , getValueDouble("chst.unbill_end_bal_it"));
  setDouble(27 , getValueDouble("chst.unbill_end_bal_id"));
  setDouble(28 , getValueDouble("chst.unbill_end_bal_ri"));
  setDouble(29 , getValueDouble("chst.unbill_end_bal_pn"));
  setDouble(30 , getValueDouble("chst.unbill_end_bal_ao"));
  setDouble(31 , getValueDouble("chst.unbill_end_bal_ai"));
  setDouble(32 , getValueDouble("chst.unbill_end_bal_sf"));
  setDouble(33 , getValueDouble("chst.unbill_end_bal_dp"));
  setDouble(34 , getValueDouble("chst.unbill_end_bal_cb"));
  setDouble(35 , getValueDouble("chst.unbill_end_bal_ci"));
  setDouble(36 , getValueDouble("chst.unbill_end_bal_cc"));
  setDouble(37 , getValueDouble("chst.unbill_end_bal_db"));
  setDouble(38 , getValueDouble("chst.billed_beg_bal_af"));
  setDouble(39 , getValueDouble("chst.billed_beg_bal_lf"));
  setDouble(40 , getValueDouble("chst.billed_beg_bal_cf"));
  setDouble(41 , getValueDouble("chst.billed_beg_bal_pf"));
  setDouble(42 , getValueDouble("chst.billed_beg_bal_bl"));
  setDouble(43 , getValueDouble("chst.billed_beg_bal_ca"));
  setDouble(44 , getValueDouble("chst.billed_beg_bal_it"));
  setDouble(45 , getValueDouble("chst.billed_beg_bal_id"));
  setDouble(46 , getValueDouble("chst.billed_beg_bal_ri"));
  setDouble(47 , getValueDouble("chst.billed_beg_bal_pn"));
  setDouble(48 , getValueDouble("chst.billed_beg_bal_ao"));
  setDouble(49 , getValueDouble("chst.billed_beg_bal_ai"));
  setDouble(50 , getValueDouble("chst.billed_beg_bal_sf"));
  setDouble(51 , getValueDouble("chst.billed_beg_bal_dp"));
  setDouble(52 , getValueDouble("chst.billed_beg_bal_cb"));
  setDouble(53 , getValueDouble("chst.billed_beg_bal_ci"));
  setDouble(54 , getValueDouble("chst.billed_beg_bal_cc"));
  setDouble(55 , getValueDouble("chst.billed_beg_bal_db"));
  setDouble(56 , getValueDouble("chst.billed_end_bal_af"));
  setDouble(57 , getValueDouble("chst.billed_end_bal_lf"));
  setDouble(58 , getValueDouble("chst.billed_end_bal_cf"));
  setDouble(59 , getValueDouble("chst.billed_end_bal_pf"));
  setDouble(60 , getValueDouble("chst.billed_end_bal_bl"));
  setDouble(61 , getValueDouble("chst.billed_end_bal_ca"));
  setDouble(62 , getValueDouble("chst.billed_end_bal_it"));
  setDouble(63 , getValueDouble("chst.billed_end_bal_id"));
  setDouble(64 , getValueDouble("chst.billed_end_bal_ri"));
  setDouble(65 , getValueDouble("chst.billed_end_bal_pn"));
  setDouble(66 , getValueDouble("chst.billed_end_bal_ao"));
  setDouble(67 , getValueDouble("chst.billed_end_bal_ai"));
  setDouble(68 , getValueDouble("chst.billed_end_bal_sf"));
  setDouble(69 , getValueDouble("chst.billed_end_bal_dp"));
  setDouble(70 , getValueDouble("chst.billed_end_bal_cb"));
  setDouble(71 , getValueDouble("chst.billed_end_bal_ci"));
  setDouble(72 , getValueDouble("chst.billed_end_bal_cc"));
  setDouble(73 , getValueDouble("chst.billed_end_bal_db"));
  setDouble(74 , getValueDouble("chst.unbill_beg_bal_ot"));
  setDouble(75 , getValueDouble("chst.billed_beg_bal_ot"));
  setDouble(76 , getValueDouble("chst.unbill_end_bal_ot"));
  setDouble(77 , getValueDouble("chst.billed_end_bal_ot"));
  setString(78 , javaProgram);
  setString(79 , getValue("p_seqno"));
  setString(80 , getValue("wday.last_acct_month"));
  setString(81 , getValue("wday.stmt_cycle"));

  int n = updateTable();
  if (n <= 0) {
    showLogMessage("I","", "update_act_curr_hst notfnd, p_seqno= "+
    getValue("p_seqno")+", curr_code= "+getValue("cuht.curr_code")+
    ", acct_month= "+getValue("wday.last_acct_month"));
    comNfndCnt++;
  }

  return n;
 }
// ************************************************************************

}  // End of class FetchSample


