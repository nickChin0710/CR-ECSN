/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/06/03  V1.00.00  Allen Ho   New                                                          *
* 109-12-08  V1.00.01  tanwei      updated for project coding standard                         *
* 112/03/06  V1.00.02  Grace       selectMktChannelParm()之feedback_apr_date條件原=, 修訂為>=       *
*                                  改取busi-day 前1日                                                                                                                                        *
*                                  異動會計分錄, 原A-36, 改為H001; 原A393, 改為H004                     *
* 112/08/17  V1.00.03  Grace       原insertCycFundDtl(1,inti) 改由holmes 統計產生傳票資料, 故此版remark	*
*************************************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktC165 extends AccessDAO
{
 private  String progname = "通路活動-現金回饋或VD回存處理程式  109/12/08 V1.00.03";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommCashback comb = null;
 CommDCFund comd = null;

 String hBusiBusinessDate = "";
 String tranSeqno = "";

 int  parmCnt  = 0;
 int  vdFlag  = 0;

 int  totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC165 proc = new MktC165();
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
   comb = new CommCashback(getDBconnect(),getDBalias());
   comd = new CommDCFund(getDBconnect(),getDBalias());

   selectPtrBusinday();
   
   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktChannelParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","今日["+hBusiBusinessDate+"]無活動回饋");
       return(0);
      }
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
 int selectMktChannelParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_channel_parm";
  whereStr  = "WHERE feedback_date = '' "
            + "and   feedback_apr_date >= ? "
            + "and   fund_code_cond = 'Y' "
            + "and   fund_date = '' "
            ;

  //setString(1 , hBusiBusinessDate);	//grace調為mega版, 取busi-day 前日
  setString(1 , comm.lastDate(hBusiBusinessDate));

  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");


  for (int inti=0;inti<parmCnt;inti++)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","符合之活動:["+getValue("parm.active_code",inti)
                            + "] 名稱:["+getValue("parm.active_name",inti)+"]");
       showLogMessage("I","","=========================================");
       if (selectMktLoanParm(inti)!=0)
          {
           showLogMessage("I","","專案回饋金代碼: ["+ getValue("parm.active_code",inti) +"] 未設定回饋參數");
           showLogMessage("I","","=========================================");
           continue;
          }
       if (getValue("loan.res_flag").equals("2"))
          loadPtrWorkday(inti);

       showLogMessage("I","","處理現金回饋明細資料\n");
       selectMktChannelList(inti);
       showLogMessage("I","","  處理筆數:["+ totalCnt+"]");
       showLogMessage("I","","=========================================");
       updateMktChannelParm(inti);
      }

  return(0);
 }
//************************************************************************
int updateMktChannelParm(int inti) throws Exception
{
daoTable  = "mkt_channel_parm";
updateSQL = "fund_date = ?,"
          + "mod_pgm = ?,"
          + "mod_time = sysdate";   
whereStr  = "where active_code = ? "
          + "and   fund_date   = '' "
          ;

setString(1 , hBusiBusinessDate);
setString(2 , javaProgram);
setString(3 , getValue("parm.active_code",inti));

int n = updateTable();

return n;
}
//************************************************************************
 int  selectMktLoanParm(int inti) throws Exception
 {
  extendField = "loan.";
  daoTable  = "mkt_loan_parm";
  whereStr  = "where fund_code = ? ";

  setString(1 , getValue("parm.fund_code",inti));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  selectMktChannelList(int inti) throws Exception
 {
  selectSQL  = "vd_flag,"
             + "p_seqno,"
             + "acct_no,"
             + "stmt_cycle,"
             + "acct_type,"
             + "id_p_seqno,"
             + "fund_amt,"
             + "rowid as rowid";
  daoTable  = "mkt_channel_list";
  whereStr  = "where fund_amt    > 0 "
            + "and   active_code = ? "
            + "and   fund_date   = '' "
            ;

  setString(1 , getValue("parm.active_code",inti));

  openCursor();

  int cnt1=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    if (getValue("vd_flag").equals("Y"))
       insertDbaAcaj();
    else
      {
       insertMktCashbackDtl(inti);
       //insertCycFundDtl(1,inti);	//20230817, grace, 改由holmes 統計產生傳票資料
      }

    updateMktChannelList();
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
       showLogMessage("I","","  處理筆數:["+ totalCnt+"]");
 }
// ************************************************************************
 int insertDbaAcaj() throws Exception
 {
  extendField = "acaj.";
  dateTime();
  setValue("acaj.crt_date"           , hBusiBusinessDate);
  setValue("acaj.crt_time"           , sysTime);
  setValue("acaj.reference_no"       , "");
  setValue("acaj.p_seqno"            , getValue("p_seqno"));
  setValue("acaj.acct_type"          , getValue("acct_type"));
  setValue("acaj.acct_no"            , getValue("acct_no"));
  setValue("acaj.card_no"            , "");
  setValue("acaj.txn_code"           , "FD");
  setValue("acaj.purchase_date"      , hBusiBusinessDate);
  setValue("acaj.adj_comment"        , "通路活動");
  setValue("acaj.orginal_amt"        , getValue("fund_amt"));           
  setValue("acaj.dr_amt"             , getValue("fund_amt"));
  setValue("acaj.adjust_type"        , "FD10");
  setValue("acaj.func_code"          , "U");
  setValue("acaj.apr_flag"           , "Y");
  setValue("acaj.proc_flag"          , "N");
  setValue("acaj.mod_user"           , javaProgram); 
  setValue("acaj.mod_time"           , sysDate+sysTime);
  setValue("acaj.mod_pgm"            , javaProgram);

  daoTable  = "dba_acaj";

  insertTable();

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
  setValue("mcdl.fund_name"            , getValue("loan.fund_name"));
  setValue("mcdl.p_seqno"              , getValue("p_seqno"));
  setValue("mcdl.acct_type"            , getValue("acct_type"));
  setValue("mcdl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("mcdl.mod_memo"             , getValue("parm.active_code",inti) + ":"
                                       + getValue("parm.active_name",inti));
  setValue("mcdl.tran_code"            , "2");
  setValue("mcdl.mod_desc"             , "通路活動回饋");
  setValue("mcdl.tran_pgm"             , javaProgram);
  setValue("mcdl.effect_e_date"   , "");
  if (getValueInt("parm.f_effect_months",inti)>0)
     setValue("mcdl.effect_e_date"     , comm.nextMonthDate(hBusiBusinessDate
                                       , getValueInt("parm.f_effect_months",inti)));
  setValue("mcdl.beg_tran_amt"         , getValue("fund_amt"));
  setValue("mcdl.end_tran_amt"         , getValue("fund_amt"));
  setValue("mcdl.res_tran_amt"         , "0");
  setValue("mcdl.res_total_cnt"        , "0");
  setValue("mcdl.res_tran_cnt"         , "0");
  setValue("mcdl.res_s_month"          , "");
  setValue("mcdl.res_upd_date"         , "");

  if (getValue("loan.res_flag").equals("2"))
     {
      if (getValueInt("loan.exec_s_months")>0)
         {
          setValue("wday.p_seqno", getValue("p_seqno"));
          int cnt1 =  getLoadData("wday.p_seqno");
          if (cnt1==0) return(1);

          setValueInt("mcdl.end_tran_amt"  , 0);
          setValue("mcdl.res_s_month"      , comm.nextMonth(getValue("wday.next_acct_month")
                                           , getValueInt("loan.exec_s_months")));
          setValue("mcdl.res_tran_amt"     , getValue("fund_amt")); 
          setValueInt("mcdl.res_total_cnt" , 1);
          if (getValueInt("parm.f_effect_months",inti)!=0)
             setValue("mcdl.effect_e_date"  , comm.nextMonthDate(hBusiBusinessDate
                                            , getValueInt("parm.f_effect_months",inti)
                                            + getValueInt("loan.exec_s_months")));
         }
     }
  else
     {
      int avgAmt = (int)Math.floor(getValueInt("fund_amt")
                  / getValueInt("loan.res_total_cnt"));

      int fstAmt = getValueInt("fund_amt")
                  - (avgAmt * (getValueInt("loan.res_total_cnt")-1));

      setValueInt("mcdl.end_tran_amt"      , fstAmt);
      setValueInt("mcdl.res_tran_amt"      , avgAmt * (getValueInt("loan.res_total_cnt")-1));
      setValueInt("mcdl.res_total_cnt"     , getValueInt("loan.res_total_cnt"));
      setValueInt("mcdl.res_tran_cnt"      , getValueInt("loan.res_total_cnt")-1);
      setValue("mcdl.res_s_month"          , hBusiBusinessDate.substring(0,6));
      setValue("mcdl.res_upd_date"         , hBusiBusinessDate);
     }

  setValue("mcdl.tran_seqno"           , tranSeqno);
  setValue("mcdl.acct_month"           , hBusiBusinessDate.substring(0,6));
  setValue("mcdl.acct_date"            , hBusiBusinessDate);
  setValue("mcdl.mod_memo"             , "");
  setValue("mcdl.mod_reason"           , "");
  setValue("mcdl.case_list_flag"       , getValue("parm.list_cond",inti));
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
 int insertCycFundDtl(int numType,int inti) throws Exception
 {
  dateTime();
  extendField = "cfdl.";

  setValue("cfdl.business_date"        , hBusiBusinessDate);
  setValue("cfdl.create_date"          , sysDate);
  setValue("cfdl.create_time"          , sysTime);
  setValue("cfdl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("cfdl.p_seqno"              , getValue("p_seqno"));
  setValue("cfdl.acct_type"            , getValue("acct_type"));
  setValue("cfdl.card_no"              , "");
  setValue("cfdl.fund_code"            , getValue("parm.fund_code",inti));
  setValue("cfdl.vouch_type"           , "3"); // '1':single record,'2':fund_code+id '3':fund_code */
  if (numType==1)
     {
      setValue("cfdl.tran_code"            , "2");
      if (getValue("loan.add_vouch_no").length()>0)
         setValue("cfdl.cd_kind"              , getValue("loan.add_vouch_no"));   /* 基金產生 */
      else
         setValue("cfdl.cd_kind"              , "H001");   /* 基金產生(A-36, MEGA), 現金回饋銷帳起帳(H001, TCB) */
     }
  else
     {
      setValue("cfdl.tran_code"            , "3");
      if (getValue("loan.rem_vouch_no").length()>0)
          setValue("cfdl.cd_kind"              , getValue("loan.rem_vouch_no"));
      else
         setValue("cfdl.cd_kind"              , "H004");	/*基金不符抵用移除 (A393, MEGA),  現金回饋-到期移除, 無效卡移除, 到期沖銷 (H004, TCB)*/
     }
  setValue("cfdl.memo1_type"           , "1");   /* fund_code 必須有值 */
  setValueInt("cfdl.fund_amt"          , Math.abs(getValueInt("fund_amt")));
  setValueInt("cfdl.other_amt"         , 0);
  setValue("cfdl.proc_flag"            , "N");
  setValue("cfdl.proc_date"            , "");
  setValue("cfdl.execute_date"         , hBusiBusinessDate);
  setValueInt("cfdl.fund_cnt"          , 1);
  setValue("cfdl.mod_user"             , javaProgram); 
  setValue("cfdl.mod_time"             , sysDate+sysTime);
  setValue("cfdl.mod_pgm"              , javaProgram);

  daoTable  = "cyc_fund_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void loadPtrWorkday(int inti) throws Exception
 {
  extendField = "wday.";
  selectSQL = "a.p_seqno,"
            + "b.stmt_cycle,"
            + "b.this_close_date,"
            + "b.next_acct_month";
  daoTable  = "act_acno a,ptr_workday b";
  whereStr  = "where a.stmt_cycle = b.stmt_cycle "
            + "and   a.p_seqno in ( "
            + "      select p_seqno  "
            + "      from   mkt_channel_list "
            + "      where fund_amt    > 0 "
            + "      and   active_code = ? "
            + "      and   fund_date   = '' "
            + "      and   vd_flag     = 'N') "
            ;

  setString(1 , getValue("parm.active_code",inti));

  int  n = loadTable();

  setLoadData("wday.p_seqno");

  showLogMessage("I","","Load ptr_workday Count: ["+n+"]");
 }
// ************************************************************************
 int updateMktChannelList() throws Exception
 {
  daoTable  = "mkt_channel_list";
  updateSQL = "tran_seqno  = ?,"
            + "fund_date   = ?,"
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";   
  whereStr  = "where rowid = ? "
            ;

  setString(1 , tranSeqno);
  setString(2 , hBusiBusinessDate);
  setString(3 , javaProgram);
  setRowId(4  , getValue("rowid"));

  int n = updateTable();

  if ( notFound.equals("Y") ) 
     {
      showLogMessage("I","","UPDATE mkt_channel_list error ["+getValue("rowid")+"]"); 
      exitProgram(1); 
     }

  return n;
 }
// ************************************************************************


}  // End of class FetchSample


