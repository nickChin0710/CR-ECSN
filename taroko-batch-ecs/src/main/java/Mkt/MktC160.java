/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 108/06/03  V1.00.00  Allen Ho   New                                        *
* 109-12-08  V1.00.01  tanwei      updated for project coding standard       *
* 111/10/03  V1.00.02  Castor Lee  Modify filter                             *
* 112/03/06  V1.00.03  Grace       insertDbmBonusDtl()增acct_date<--businessday 
*                                  selectMktChannelParm() 取得businessday -1  *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktC160 extends AccessDAO
{
 private  String progname = "通路活動-紅利點數回饋處理程式  111/10/03 V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;
 CommDBonus comd = null;

 String hBusiBusinessDate = "";
 String tranSeqno = "";

 int  parmCnt  = 0;
 int  vdFlag  = 0;

 int  totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC160 proc = new MktC160();
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
   comb = new CommBonus(getDBconnect(),getDBalias());
   comd = new CommDBonus(getDBconnect(),getDBalias());

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
  whereStr  = "WHERE feedback_date     = '' "
            + "and   feedback_apr_date >= ? "
            + "and   bonus_type_cond   = 'Y' "
            + "and   bonus_date   = '' "
            ;

  //setString(1 , hBusiBusinessDate); //grace remarked for match with mega
  setString(1 , comm.lastDate(hBusiBusinessDate));

  parmCnt = selectTable();

  showLogMessage("I","","參數檔載入筆數: ["+ parmCnt +"]");

  for (int inti=0;inti<parmCnt;inti++)
      {
       showLogMessage("I","","=========================================");
       showLogMessage("I","","符合之活動:["+getValue("parm.active_code",inti)
                            + "] 名稱:["+getValue("parm.active_name",inti)+"]");
       showLogMessage("I","","=========================================");
       selectMktChannelList(inti);
       updateMktChannelParm(inti);
      }

  return(0);
 }
//************************************************************************
int updateMktChannelParm(int inti) throws Exception
{
daoTable  = "mkt_channel_parm";
updateSQL = "bonus_date     = ?,"
          + "mod_pgm        = ?,"
          + "mod_time       = sysdate";   
whereStr  = "where active_code = ? "
          + "and   bonus_date  = '' "
          ;

setString(1 , hBusiBusinessDate);
setString(2 , javaProgram);
setString(3 , getValue("parm.active_code",inti));

int n = updateTable();

return n;
}

// ************************************************************************
 void  selectMktChannelList(int inti) throws Exception
 {
  selectSQL  = "vd_flag,"
             + "p_seqno,"
             + "acct_type,"
             + "id_p_seqno,"
             + "bonus_pnt,"
             + "rowid as rowid";
  daoTable  = "mkt_channel_list";
  whereStr  = "where bonus_pnt   > 0 "
            + "and   active_code = ? "
            + "and   bonus_date  = '' "
            ;

  setString(1 , getValue("parm.active_code",inti));

  openCursor();

  int cnt1=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    if (getValue("vd_flag").equals("Y"))
       insertDbmBonusDtl(inti);
    else
       insertMktBonusDtl(inti);

    updateMktChannelList();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int insertMktBonusDtl(int inti) throws Exception
 {
  dateTime();
  extendField = "mbdl.";
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");

  setValue("mbdl.beg_tran_bp"          , getValue("bonus_pnt"));
  setValue("mbdl.end_tran_bp"          , getValue("bonus_pnt"));

  setValue("mbdl.active_code"          , getValue("parm.active_code",inti));
  setValue("mbdl.active_name"          , getValue("parm.active_name",inti));
  setValue("mbdl.mod_desc"             , "通路活動覆核日期:{"+ getValue("parm.feedback_apr_date",inti)+"]");
  setValue("mbdl.p_seqno"              , getValue("p_seqno")); 
  setValue("mbdl.id_p_seqno"           , getValue("id_p_seqno")); 
  setValue("mbdl.acct_type"            , getValue("acct_type")); 
  setValue("mbdl.tax_flag"             , getValue("parm.tax_flag",inti));
  setValue("mbdl.tran_code"            , "2");
  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , tranSeqno);
  setValue("mbdl.effect_e_date"        , "");
  if (getValueInt("parm.b_effect_months",inti)>0)
     setValue("mbdl.effect_e_date"     , comm.nextMonthDate(hBusiBusinessDate
                                       , getValueInt("parm.b_effect_months",inti)));
  setValue("mbdl.bonus_type"           , getValue("parm.bonus_type",inti));
  setValue("mbdl.acct_date"            , hBusiBusinessDate);
  setValue("mbdl.proc_month"           , hBusiBusinessDate.substring(0,6));
  setValue("mbdl.tran_pgm"             , javaProgram);
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_user"             , javaProgram);
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , javaProgram);
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.mod_user"             , javaProgram);
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertDbmBonusDtl(int inti) throws Exception
 {
  extendField = "dbdl.";
  tranSeqno     = comr.getSeqno("ECS_DBMSEQ");

  setValue("dbdl.tran_date"            , hBusiBusinessDate);
  setValue("dbdl.tran_time"            , sysTime);
  setValue("dbdl.active_code"          , getValue("parm.active_code",inti));
  setValue("dbdl.active_name"          , getValue("parm.active_name",inti));
  setValue("dbdl.mod_desc"             , "通路活動覆核日期:{"+ getValue("parm.feedback_apr_date",inti)+"]");
  setValue("dbdl.bonus_type"           , "BONU");
  setValue("dbdl.acct_type"            , getValue("acct_type"));
  setValue("dbdl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("dbdl.tran_code"            , "1");
  setValue("dbdl.tran_pgm"             , javaProgram);
  setValue("dbdl.beg_tran_bp"          , getValue("bonus_pnt"));
  setValue("dbdl.end_tran_bp"          , getValue("bonus_pnt"));
  setValue("dbdl.acct_date"            , hBusiBusinessDate);
  setValue("dbdl.acct_month"           , sysDate.substring(0,6));
  setValue("dbdl.tax_flag"             , getValue("parm.tax_flag",inti));
  setValue("dbdl.effect_e_date"   , "");
  if (getValueInt("parm.b_effect_months",inti)>0)
     setValue("dbdl.effect_e_date"        , comm.nextMonthDate(hBusiBusinessDate,getValueInt("parm.b_effect_months",inti)));
  setValue("dbdl.tran_seqno"           , tranSeqno);
  setValue("dbdl.crt_user"             , javaProgram);
  setValue("dbdl.crt_date"             , sysDate);
  setValue("dbdl.apr_flag"             , "Y");
  setValue("dbdl.apr_user"             , javaProgram);
  setValue("dbdl.apr_date"             , sysDate);
  setValue("dbdl.mod_user"             , javaProgram);
  setValue("dbdl.mod_time"             , sysDate+sysTime);
  setValue("dbdl.mod_pgm"              , javaProgram);

  daoTable  = "dbm_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int updateMktChannelList() throws Exception
 {
  daoTable  = "mkt_channel_list";
  updateSQL = "tran_seqno  = ?,"
            + "bonus_date  = ?,"
            + "mod_pgm     = ?,"
            + "mod_time    = timestamp_format(?,'yyyymmddhh24miss')";   
  whereStr  = "where rowid = ? "
            ;

  setString(1 , tranSeqno);
  setString(2 , hBusiBusinessDate);
  setString(3 , javaProgram);
  setString(4 , sysDate+sysTime);
  setRowId(5  , getValue("rowid"));

  int n = updateTable();

  if ( notFound.equals("Y") ) 
     {
      showLogMessage("I","","UPDATE mkt_channel_list error"); 
      exitProgram(1); 
     }

  return n;
 }
//************************************************************************
 
}  // End of class FetchSample


