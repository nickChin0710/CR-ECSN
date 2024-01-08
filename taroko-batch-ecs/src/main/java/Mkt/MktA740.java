/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/07/21  V1.00.00  Allen Ho   mkt_a740                                   *
* 109-12-04  V1.00.01  tanwei      updated for project coding standard       *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktA740 extends AccessDAO
{
 private  String progname = "紅利積點-效期到期移除處理程式 109/12/04 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate  = "";
 String tranSeqno     = "";

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA740 proc = new MktA740();
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
   
   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (selectMktBonusDtl0()>0)   // if no this check, will not right
     {
      commitDataBase();
      showLogMessage("I","","啟動 MktA100 執行後續動作 .....");
      showLogMessage("I","","===============================");

      String[] hideArgs = new String[1];
      try {
           hideArgs[0] = "";

           MktA100 mktA100 = new MktA100();
           int rtn = mktA100.mainProcess(hideArgs);
           if(rtn < 0)   return (1);
           showLogMessage("I","","MktA100 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex) 
               {
                showLogMessage("I","","無法執行 MktA100 ERROR!");
               }
     }

   showLogMessage("I","","=========================================");
   showLogMessage("I","","移除有效期值");
   selectMktBonusDtl();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","=========================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
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
 int selectMktBonusDtl0() throws Exception
 {
  selectSQL = "count(*) as data_cnt";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  end_tran_bp != 0 "
            + "group by p_seqno,bonus_type "
            + "having sum(decode(sign(end_tran_bp),1,end_tran_bp,0))!=0 "
            + "and    sum(decode(sign(end_tran_bp),-1,end_tran_bp,0))!=0 "
            + "and count(*) > 0 "
            ;

  showLogMessage("I","","===============================");
  showLogMessage("I","","檢查是否已重整 ...");

  int recCnt = selectTable();

  if (recCnt==0) return(0);

  if (recCnt==0) 
     {
      showLogMessage("I","","資料已重整");
      return(0);
     }

  showLogMessage("I","","資料未重整");

  return(1);
 }
// ************************************************************************
 void  selectMktBonusDtl() throws Exception
 {
  selectSQL = "p_seqno,"
            + "effect_e_date,"
            + "bonus_type,"
            + "sum(end_tran_bp) as end_tran_bp, "
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type,"
            + "sum(decode(tax_flag,'Y',end_tran_bp,0)) as tax_tran_bp";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where end_tran_bp > 0 "
            + "and   effect_e_date between ? and ? "
            + "group by p_seqno,effect_e_date,bonus_type "
            ;

  if (hBusiBusinessDate.substring(6,8).equals("01"))
     setString(1 , comm.nextMonthDate(hBusiBusinessDate , -36));
  else
     setString(1 , comm.nextNDate(hBusiBusinessDate , -7));

  setString(2 , comm.nextNDate(hBusiBusinessDate , -1));

  if (hBusiBusinessDate.substring(6,8).equals("01"))
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextMonthDate(hBusiBusinessDate , -36)
                           +"]-["
                           + comm.nextNDate(hBusiBusinessDate , -1)
                           + "]");
     }
  else
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextNDate(hBusiBusinessDate , -7)
                           +"]-["
                           + comm.nextNDate(hBusiBusinessDate , -1)
                           + "]");
     }

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    insertMktBonusDtl();
    updateMktBonusDtl();

//  showLogMessage("I","","p_seqno : ["+ getValue("p_seqno") +"]");

    processDisplay(100000); // every 10000 display message
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int insertMktBonusDtl() throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");
  extendField = "mbdl.";

  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , tranSeqno);
  setValue("mbdl.acct_date"            , hBusiBusinessDate);
  setValue("mbdl.proc_month"           , hBusiBusinessDate.substring(0,6));
  setValue("mbdl.bonus_type"           , getValue("bonus_type"));
  setValue("mbdl.p_seqno"              , getValue("p_seqno"));
  setValue("mbdl.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("mbdl.acct_type"            , getValue("acct_type"));
  setValue("mbdl.tran_code"            , "6");
  setValue("mbdl.active_code"          , "");
  setValue("mbdl.active_name"          , "效期到期移除處");
  setValue("mbdl.mod_desc"             , "效期到期日"+getValue("effect_e_date")); 
  setValue("mbdl.mod_memo"             , "");
  setValue("mbdl.tax_flag"             , "N");
  setValueInt("mbdl.tax_tran_bp"       , getValueInt("tax_tran_bp"));
  setValueInt("mbdl.beg_tran_bp"       , getValueInt("end_tran_bp")*-1);
  setValue("mbdl.end_tran_bp"          , "0"); 
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , javaProgram);
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.tran_pgm"             , javaProgram);
  setValue("mbdl.mod_user"             , javaProgram); 
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , javaProgram);

  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktBonusDtl() throws Exception
 {
  dateTime();
  updateSQL = "effect_flag    = 'Y', "
            + "remove_date    = ?, "
            + "mod_memo       = ?,"
            + "link_seqno     = ?,"
            + "link_tran_bp   = end_tran_bp,"
            + "end_tran_bp    = 0, "
            + "mod_pgm        = ?, "
            + "mod_time       = sysdate";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE p_seqno     =  ? "
            + "and effect_e_date = ? "
            + "and bonus_type    = ? "
            + "and end_tran_bp  != 0 ";

  setString(1 , sysDate);
  setString(2 , "移除序號["+tranSeqno+"]");
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno"));
  setString(6 , getValue("effect_e_date"));
  setString(7 , getValue("bonus_type"));

  updateTable();
  return;
 }
// ************************************************************************

}  // End of class FetchSample
