/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/01/14  V1.00.09  Allen Ho   cyc_a185   now prod no parm is alive       *
* 109-12-17   V1.00.10  tanwei      updated for project coding standard      *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class CycA310 extends AccessDAO
{
 private  String progname = "台幣基金-COMBO 回饋基金計算處理程式 109/12/17 V1.00.10";
 CommFunction comm = new CommFunction();
 CommCashback comC = null;
 CommRoutine  comr = null;

 String hBusiBusinessDate   = "";
 String hWdayStmtCycle      = "";
 String tranSeqno = "";

 long    totalCnt=0,updateCnt=0;
 int parmCnt=0;
 int[] dInt = {0,0,0,0};
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA310 proc = new CycA310();
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
      { hBusiBusinessDate = args[0]; }
   
   if ( !connectDataBase() ) exitProgram(1);
   comC = new CommCashback(getDBconnect(),getDBalias());
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

   if (selectPtrComboFundp()!=0)
      {
       showLogMessage("I","","參數設定不產生基金 !");
       return(0);
      }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入暫存資料");
   loadActAcct();
   loadCrdCard();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理(act_combo_jrnl)資料");
   selectActComboJrnl();

   showLogMessage("I","","不處理 條件一 ["+dInt[0]+"]  條件二 ["+dInt[1]+"  金額零 ["+dInt[2]+"]  成功["+dInt[3]+"]");
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
  public int  selectPtrWorkday() throws Exception
 {
  selectSQL = "";
  daoTable  = "ptr_workday";
  whereStr  = "where this_close_date = ? ";

  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  hWdayStmtCycle      =  getValue("STMT_CYCLE");

  return(0);
 }
// ************************************************************************
  int selectActComboJrnl() throws Exception
 {
  selectSQL = "cycle_max_amt,"
            + "cr_amt,"
            + "card_no,"
            + "a.p_seqno,"
            + "b.acct_type,"
            + "b.rowid as rowid";
  daoTable  = "act_acno a,act_combo_jrnl b";
  whereStr  = "where  a.p_seqno        = b.p_seqno "
            + "and    b.feedback_flag != 'Y' "
            + "and    b.acct_month     = '' "
            + "and    a.stmt_cycle     = ?  "
            + "and    b.tran_class     ='3' "
            + "and    acct_date between ? and ? "
            ;

  setString(1, hWdayStmtCycle);
  setString(2, getValue("last_close_date"));
  setString(3, getValue("this_close_date"));

  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
     for (int inti=0;inti<parmCnt;inti++)
        {
         if (getValue("parm.cmb_oth_cond1",inti).equals("Y"))
           {
            setValue("acct.p_seqno",getValue("p_seqno"));
            int cnt1 = getLoadData("acct.p_seqno");

            if (getValueInt("acct.min_pay_bal") > 0)
               {
                updateActComboJrnl(1);
                dInt[0]++;
                continue;
               }
           }

        if (getValue("parm.cmb_oth_cond2",inti).equals("Y"))
           {
            setValue("card.card_no",getValue("card_no"));
            int cnt1 = getLoadData("card.card_no");
            if (getValue("card.current_code").equals("0"))
               {
                updateActComboJrnl(2);
                dInt[1]++;
                continue;
               }
           }

        int newAmt = (int)Math.round((getValueInt("cycle_bill_bal")-getValueInt("cr_amt"))*
                                       getValueInt("parm.cmb_rate",inti)*1.0);

        if ((newAmt > getValueInt("parm.feedback_lmt",inti))&&(getValueInt("parm.feedback_lmt",inti) > 0)) 
           newAmt = getValueInt("parm.feedback_lmt",inti);

        setValueInt("beg_tran_amt", newAmt);

        if (newAmt <=0)
           {
            updateActComboJrnl(9);
            dInt[2]++;
            continue;
           }

        String resSDate="";
        int calMonths =0;
        if (getValue("parm.cancel_period").equals("1"))
           {
            setValue("res_s_month"      , resSDate); 
            setValue("effect_e_date"    , "");
            if (getValueInt("parm.effect_months",inti)>0)
               setValue("effect_e_date"    , comm.nextMonthDate(hBusiBusinessDate,getValueInt("parm.effect_months",inti)));
            setValueInt("end_tran_amt"  , getValueInt("beg_tran_amt"));   
            setValueInt("res_tran_amt"  , 0);   
            setValueInt("res_total_cnt" , 0);   
           }
        else
           {
            if (getValue("parm.cancel_period").equals("2")) calMonths=3;
            if (getValue("parm.cancel_period").equals("3")) calMonths=6;
            if (getValue("parm.cancel_period").equals("4")) calMonths=12;
 
            resSDate = resSMonth(hBusiBusinessDate.substring(0,4) +
                                     getValue("parm.cancel_s_month",inti) +
                                     hBusiBusinessDate.substring(6,8) , 
                                     hBusiBusinessDate , calMonths);
            setValue("res_s_month"      , resSDate.substring(0,6)); 
            setValue("effect_e_date"   , "");
             if (getValueInt("parm.effect_months",inti)>0)
                setValue("effect_e_date"    , comm.nextMonthDate(resSDate,getValueInt("parm.effect_months",inti)));
            setValueInt("end_tran_amt"  , 0); 
            setValueInt("res_tran_amt"  , getValueInt("beg_tran_amt"));
            setValueInt("res_total_cnt" , 1);   

           }
        insertMktCashbackDtl(inti);
          
        insertCycFundDtl(1,inti);

        updateActComboJrnl(0);
        dInt[3]++;
        }
   } 
  closeCursor();

  return(0);
 }
// ************************************************************************
 int selectPtrComboFundp() throws Exception
 {
  extendField = "parm.";
  selectSQL = "";
  daoTable  = "ptr_combo_fundp";
  whereStr  = "WHERE stop_flag != 'Y' ";

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  loadActAcct() throws Exception
 {
  extendField = "acct.";
  selectSQL = "p_seqno,"
            + "min_pay_bal";
  daoTable  = "act_acct";
  whereStr  = "where stmt_cycle = ? ";

  setString(1, hWdayStmtCycle);
  int  n = loadTable();

  setLoadData("acct.p_seqno");

  showLogMessage("I","","Load act_acct Count: ["+n+"]");
 }
// ************************************************************************
 void  loadCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "card_no,"
            + "current_code";
  daoTable  = "crd_card";
  whereStr  = "where stmt_cycle = ? ";

  setString(1, hWdayStmtCycle);
  int  n = loadTable();

  setLoadData("card.card_no");

  showLogMessage("I","","Load crd_card Count: ["+n+"]");
 }
// ************************************************************************
 void updateActComboJrnl(int inti) throws Exception
 {
  dateTime();

  if (inti==0) setValue("feedback_flag","Y");
  else if (inti==1) setValue("feedback_flag","1");
  else if (inti==2) setValue("feedback_flag","2");
  else if (inti==3) setValue("feedback_flag","3");
  else if (inti==4) setValue("feedback_flag","4");
  else if (inti==5) setValue("feedback_flag","5");
  else setValue("feedback_flag","X");

  updateSQL = "feedback_flag   = ?, "
            + "mod_pgm         = ?, "
            + "mod_time        = timestamp_format(?,'yyyymmddhh24miss')";  
  daoTable  = "act_combo_jrnl";
  whereStr  = "WHERE rowid     = ? ";

  setString(1 , getValue("feedback_flag"));
  setString(2 , javaProgram);
  setString(3 , sysDate+sysTime);
  setRowId(4  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int insertMktCashbackDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("mkt_modseq");
  dateTime();

  setValue("tran_date"            , sysDate);
  setValue("tran_time"            , sysTime);
  setValue("fund_code"            , getValue("parm.fund_code",inti));
  setValue("fund_name"            , getValue("parm.fund_name",inti));
  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("acct_type"            , getValue("acct_type"));
  setValue("id_p_seqno"           , getValue("id_p_seqno"));
  setValue("tran_code"            , "2");
  setValue("tran_pgm"             , javaProgram);
  setValueInt("beg_tran_amt"      , getValueInt("beg_tran_amt"));
  setValueInt("end_tran_amt"      , getValueInt("end_tran_amt"));
  setValueInt("res_tran_amt"      , getValueInt("res_tran_amt"));
  setValueInt("res_total_cnt"     , getValueInt("res_total_cnt"));
  setValueInt("res_tran_cnt"      , 0);
  setValue("res_s_month"          , getValue("res_s_month"));
  setValue("res_upd_date"         , "");
  setValue("effect_e_date"        , getValue("effect_s_date"));
  if (getValueInt("effect_months")>0)
     setValue("effect_e_date"     , comm.nextMonthDate(hBusiBusinessDate,getValueInt("effect_months")));
  setValue("tran_seqno"           , tranSeqno);
  setValue("proc_month"           , hBusiBusinessDate.substring(0,6));
  setValue("acct_date"            , hBusiBusinessDate);
  setValue("mod_desc"             , "COMBO卡回饋基金");
  setValue("mod_memo"             , "");
  setValue("mod_reason"           , "");
  setValue("case_list_flag"       , "N");
  setValue("crt_user"             , javaProgram);
  setValue("crt_date"             , sysDate);
  setValue("apr_date"             , sysDate);
  setValue("apr_user"             , javaProgram);
  setValue("apr_flag"             , "Y");
  setValue("mod_user"             , javaProgram); 
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);
  daoTable  = "mkt_cashback_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
public int insertCycFundDtl(int numType,int inti) throws Exception
 {
  dateTime();

  setValue("business_date"        , hBusiBusinessDate);
  setValue("create_date"          , sysDate);
  setValue("create_time"          , sysTime);
  setValue("id_p_seqno"           , getValue("id_p_seqno"));
  setValue("p_seqno"              , getValue("p_seqno"));
  setValue("acct_type"            , getValue("acct_type"));
  setValue("card_no"              , getValue("acct_type"));
  setValue("fund_code"            , getValue("parm.fund_code",inti));
  setValue("tran_code"            , "2");
  setValue("vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
  if (numType==1)
     setValue("cd_kind"              , "A-36");
  else
     setValue("cd_kind"              , "A393");
  setValue("memo1_type"           , "1");   /* fund_code 必須有值 */
  setValueInt("fund_amt"          , Math.abs(getValueInt("beg_tran_amt")));
  setValueInt("other_amt"         , 0);
  setValue("proc_flag"            , "N");
  setValue("proc_date"            , "");
  setValue("execute_date"         , hBusiBusinessDate);
  setValueInt("fund_cnt"          , 1);
  setValue("mod_user"             , javaProgram); 
  setValue("mod_time"             , sysDate+sysTime);
  setValue("mod_pgm"              , javaProgram);

  daoTable  = "cyc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 String resSMonth(String yearSDate,String nowDate,int calMonths) throws Exception
 {
  String okDate="";
  for (int inti=0;inti<6;inti++)
    {
     okDate = comm.nextMonthDate(yearSDate,inti*calMonths);
     if (okDate.compareTo(nowDate)>=0) break;
    }
  return okDate;
 }
// ************************************************************************

}  // End of class FetchSample

