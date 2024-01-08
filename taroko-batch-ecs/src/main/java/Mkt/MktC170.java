/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/04  V1.00.05  Allen Ho   New                                        *
* 111/03/01  V1.01.01  Allen Ho   Mantis 9290                                *
* 111/03/10  V1.01.02  Brian      fix business_date bug                      *
* 112/04/25  V1.01.03  Zuwei Su   copy from mega, table name改為’mkt_lottery_list’                      *
*                                                                            *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktC170 extends AccessDAO
{
 private final String PROGNAME = "通路活動-名單(抽獎)回饋處理程式  112/04/25  V1.01.03";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String businessDate = "";
 String tranSeqno = "";

 int  parmCnt  = 0;
 int  vdFlag  = 0;
 String tmpstr = "";

 int dataCnt=0;
 int  totalCnt=0;
 int  lackCnt=0;
 int  nocallCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC170 proc = new MktC170();
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
      { businessDate = args[0]; }

   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","載入參數資料");
   selectMktChannelParm();
   if (parmCnt==0)
      {
       showLogMessage("I","","今日["+businessDate+"]無活動回饋");
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
  extendField = "busi.";
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }

  if (businessDate.length()==0)
      businessDate   =  getValue("busi.BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************
 int selectMktChannelParm() throws Exception
 {
  extendField = "parm.";
  daoTable  = "mkt_channel_parm";
  whereStr  = "WHERE feedback_date = '' "
            + "and   feedback_apr_date >= ? "
            + "and   lottery_cond   = 'Y' "
            + "and   lottery_type   = '1' "
            + "and   lottery_date   = '' "
            ;

  setString(1 , comm.lastDate(businessDate));

  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");


  for (int inti=0;inti<parmCnt;inti++)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","符合之活動:["+getValue("parm.active_code",inti)
                            + "] 名稱:["+getValue("parm.active_name",inti)+"]");
       showLogMessage("I","","=========================================");

       totalCnt = lackCnt = nocallCnt = 0;
       showLogMessage("I","","處理抽獎名單明細資料\n");

       deleteMktLotteryList(inti);
       deleteMktDrawList(inti);
       selectMktChannelList(inti);

       updateMktChannelList(inti);

       showLogMessage("I","","累計處理         ["+ totalCnt + "] 筆"); 
       showLogMessage("I","","    缺少電子禮券 ["+ lackCnt  + "] 筆"); 
       showLogMessage("I","","    手機號碼錯誤 ["+ nocallCnt  + "] 筆"); 
       showLogMessage("I","","=========================================");
       updateMktChannelParm(inti);
      }

  return(0);
 }
// ************************************************************************
 int updateMktChannelParm(int inti) throws Exception
 {
  daoTable  = "mkt_channel_parm";
  updateSQL = "lottery_date   = ?,"
            + "mod_pgm        = ?,"
            + "mod_time       = sysdate";   
  whereStr  = "where active_code = ? "
            + "and   lottery_date = '' "
            ;

  setString(1 , businessDate);
  setString(2 , javaProgram);
  setString(3 , getValue("parm.active_code",inti));

  int n = updateTable();

  return n;
 }
// ************************************************************************
 int updateMktChannelList(int inti) throws Exception
 {
  daoTable  = "mkt_channel_list";
  updateSQL = "proc_date   = ?,"
            + "proc_flag   = 'N',"
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";   
  whereStr  = "where active_code = ? "
            + "and   lottery_int > 0 "
            ;

  setString(1 , businessDate);
  setString(2 , javaProgram);
  setString(3 , getValue("parm.active_code",inti));

  int n = updateTable();

  return n;
 }
// ************************************************************************
 void  selectMktChannelList(int inti) throws Exception
 {
  selectSQL  = "";
  daoTable  = "mkt_channel_list";
  whereStr  = "where lottery_int    > 0 "
            + "and   active_code = ? "
            + "and   lottery_type   = '1' "
            + "and   lottery_date   = '' "
            ;

  setString(1 , getValue("parm.active_code",inti));

  openCursor();

  totalCnt=0;
  dataCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    insertMktLotteryList(inti);

    if (getValue("vd_flag").equals("Y"))
       selectDbcCard();
    else
       selectCrdCard();

    for (int intm=0;intm<getValueInt("lottery_int");intm++)
        insertMktDrawList(inti);
   } 
  closeCursor();
 }
// ************************************************************************
 int insertMktLotteryList(int inti) throws Exception
 {
  extendField = "lott.";

  setValue("lott.active_code"          , getValue("parm.active_code",inti));
  setValue("lott.vd_flag"              , getValue("vd_flag"));
  setValue("lott.acct_type"            , getValue("acct_type"));
  setValue("lott.p_seqno"              , getValue("p_seqno"));
  setValue("lott.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("lott.lottery_int"          , getValue("lottery_int"));
  setValue("lott.feedback_date"        , sysDate);
  setValue("lott.mod_time"             , sysDate+sysTime);
  setValue("lott.mod_pgm"              , javaProgram);

  daoTable  = "mkt_lottery_list";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertMktDrawList(int inti) throws Exception
 {
  extendField = "draw.";

  dataCnt++;
  setValue("draw.draw_no"              , "CHAN"+getValue("parm.active_code",inti));
  setValueInt("draw.seq_no"            , dataCnt);
  setValue("draw.card_no"              , getValue("card_no"));
  setValue("draw.id_no"                , getValue("card.id_no"));
  setValue("draw.name"                 , getValue("card.name"));
  setValue("draw.comm_zip"             , getValue("card.comm_zip"));
  setValue("draw.comm_addr"            , getValue("card.comm_addr"));
  setValue("draw.cellar_phone"         , getValue("card.cellar_phone"));
  setValue("draw.mail"                 , getValue("card.mail"));
  setValue("draw.mod_user"             , javaProgram);
  setValue("draw.mod_time"             , sysDate+sysTime);
  setValue("draw.mod_pgm"              , javaProgram);

  daoTable  = "mkt_draw_list";

  insertTable();

  return(0);
 }
// ************************************************************************
 int deleteMktLotteryList(int inti) throws Exception
 {
  daoTable  = "mkt_lottery_list";
  whereStr  = "where active_code = ? ";

  setString(1 , getValue("parm.active_code",inti)); 

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_lottery_list 筆數  : ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int deleteMktDrawList(int inti) throws Exception
 {
  daoTable  = "mkt_draw_list";
  whereStr  = "where draw_no = ? ";

  setString(1 , "CHAN"+getValue("parm.active_code",inti)); 

  int recCnt = deleteTable();

  showLogMessage("I","","刪除 mkt_draw_list 筆數  : ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 void  selectCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.chi_name as name,"
            + "a.id_no,"
            + "a.resident_addr1||a.resident_addr2||a.resident_addr3||a.resident_addr4||a.resident_addr5 as comm_addr,"
            + "a.resident_zip as comm_zip,"
            + "a.cellar_phone,"
            + "a.e_mail_addr as mail";
  daoTable  = "crd_idno a,crd_card b";
  whereStr  = "where a.id_p_seqno = b.id_p_seqno "
            + "and   a.id_p_seqno = ? "
            ;

  setString(1 , getValue("id_p_seqno"));

  if (getValue("p_seqno").length()!=0)
     {
      whereStr  = whereStr
                + "and b.p_seqno = ? ";
      setString(2 , getValue("p_seqno"));
     }

  if (getValue("card_no").length()!=0)
     {
      whereStr  = whereStr
                + "and b.card_no = ? ";
      setString(3 , getValue("card_no"));
     }
  whereStr  = whereStr
            + " order by b.current_code,b.acct_type";

   
  int recordCnt = selectTable();
 }
// ************************************************************************
 void  selectDbcCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "a.chi_name as name,"
            + "a.id_no,"
            + "a.id_no,"
            + "a.resident_addr1||a.resident_addr2||a.resident_addr3||a.resident_addr4||a.resident_addr5 as comm_addr,"
            + "a.resident_zip as comm_zip,"
            + "a.cellar_phone,"
            + "a.e_mail_addr as mail";
  daoTable  = "dbc_idno a,dbc_card b";
  whereStr  = "where a.id_p_seqno = b.id_p_seqno "
            + "and   a.id_p_seqno = ? "
            ;

  setString(1 , getValue("id_p_seqno"));

  if (getValue("p_seqno").length()!=0)
     {
      whereStr  = whereStr
                + "and b.p_seqno = ? ";
      setString(2 , getValue("p_seqno"));
     }

  if (getValue("card_no").length()!=0)
     {
      whereStr  = whereStr
                + "and b.card_no = ? ";
      setString(3 , getValue("card_no"));
     }
  whereStr  = whereStr
            + " order by b.current_code,b.acct_type";

   
  int recordCnt = selectTable();
 }
// ************************************************************************


}  // End of class FetchSample


