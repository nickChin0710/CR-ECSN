/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/07  V1.00.00  Allen Ho   cyc_d010                                   *
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
public class MktA760 extends AccessDAO
{
 private  String progname = "紅利-帳戶無效卡達三個月歸零處理程式 109/12/04 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate  = "";
 String tranSeqno     = "";

 long    totalCnt=0,updateCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA760 proc = new MktA760();
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
   
   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if ((!hBusiBusinessDate.substring(6,8).equals("01"))&&
       (!hBusiBusinessDate.substring(6,8).equals("20")))
      {
       showLogMessage("I","","本程式為每月01,20日執行");
       return(0);
      }

   loadActAcno();    
   showLogMessage("I","","=========================================");
   showLogMessage("I","","移除 帳戶無效卡 資料"); 
   selectCrdCard();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆, 移除 ["+updateCnt+"] 筆");
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
 void  selectCrdCard() throws Exception
 {
  selectSQL = "p_seqno,"
            + "max(decode(oppost_date,'','30001231',oppost_date)) as oppost_date";
  daoTable  = "crd_card b";
  whereStr  = "where major_card_no = card_no "
            + "group by p_seqno "
            + "having max(decode(oppost_date,'','30001231',oppost_date))!='30001231'";

  if (hBusiBusinessDate.substring(6,8).equals("01"))
     {
      showLogMessage("I","","判斷日期 : ["
                           +  comm.nextMonthDate(hBusiBusinessDate , -37)
                           +"]-["
                           + comm.nextMonthDate(hBusiBusinessDate , -3)
                           + "]");
     }
  else
     {
      showLogMessage("I","","判斷日期 : ["
                           + comm.nextMonthDate(hBusiBusinessDate , -6)
                           +"]-["
                           + comm.nextMonthDate(hBusiBusinessDate , -3)
                           + "]");
     }


  openCursor();

  totalCnt=0;

  while( fetchTable() ) 
   { 
    totalCnt++;
    setValue("acno.p_seqno" , getValue("p_seqnoo"));
    int cnt1 = getLoadData("acno.p_seqno");
    if (cnt1>0) continue;

    if (comm.nextMonthDate(getValue("oppost_date"),3).compareTo(hBusiBusinessDate)>0) continue;

    if (hBusiBusinessDate.substring(6,8).equals("20"))
       if (comm.nextMonthDate(getValue("oppost_date"),6).compareTo(hBusiBusinessDate)<0) continue;

    if (hBusiBusinessDate.substring(6,8).equals("01"))
       if (comm.nextMonthDate(getValue("oppost_date"),37).compareTo(hBusiBusinessDate)<0) continue;

    if (selectMktBonusDtl()!=0) continue;

//  showLogMessage("I","","p_seqno : ["+ getValue("p_seqno") +"]");

   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int  selectMktBonusDtl() throws Exception
 {
  extendField = "mbdc.";
  selectSQL = "p_seqno,"
            + "bonus_type,"
            + "sum(end_tran_bp) as end_tran_bp, "
            + "max(bonus_type) as bonus_type,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(acct_type) as acct_type,"
            + "sum(decode(tax_flag,'Y',end_tran_bp,0)) as tax_tran_bp";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE end_tran_bp != 0 "
            + "and p_seqno = ? "
            + "group by p_seqno,bonus_type "
            + "having sum(end_tran_bp) != 0  "
            ;

  setString(1, getValue("p_seqno"));

  int recCnt = selectTable();

  if (recCnt==0) return(1);

  for ( int inti=0; inti<recCnt; inti++ )
    { 
     insertMktBonusDtl(inti);
     updateMktBonusDtl(inti);
     updateCnt++;
    }
  return(0);
 }
// ************************************************************************
 int insertMktBonusDtl(int inti) throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");
  extendField = "mbdl.";

  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);

  setValue("mbdl.tran_seqno"           , tranSeqno);
  setValue("mbdl.acct_date"            , hBusiBusinessDate);
  setValue("mbdl.proc_month"           , hBusiBusinessDate.substring(0,6));
  setValue("mbdl.bonus_type"           , getValue("mbdc.bonus_type",inti));
  setValue("mbdl.p_seqno"              , getValue("mbdc.p_seqno",inti));
  setValue("mbdl.id_p_seqno"           , getValue("mbdc.id_p_seqno",inti));
  setValue("mbdl.acct_type"            , getValue("mbdc.acct_type",inti));
  setValue("mbdl.tran_code"            , "6");
  setValue("mbdl.active_code"          , "");
  setValue("mbdl.active_name"          , "無有效卡紅利移除");
  setValue("mbdl.mod_desc"             , "無有效卡達三個月");
  setValue("mbdl.mod_memo"             , "");
  setValue("mbdl.tax_flag"             , "N");
  setValueInt("mbdl.tax_tran_bp"       , getValueInt("mbdc.tax_tran_bp",inti));
  setValueInt("mbdl.beg_tran_bp"       , getValueInt("mbdc.end_tran_bp",inti)*-1);
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
 void updateMktBonusDtl(int inti) throws Exception
 {
  dateTime();
  updateSQL = "effect_flag    = 'Y', "
            + "remove_date    = ?, "
            + "mod_memo       = ?,"
            + "link_seqno     = ?,"
            + "link_tran_bp   = end_tran_bp,"
            + "end_tran_bp    = 0,"
            + "mod_pgm        = ?, "
            + "mod_time       = sysdate";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE p_seqno   =  ? "
            + "and   bonus_type  = ? "
            + "and   end_tran_bp != 0 ";

  setString(1 , sysDate);
  setString(2 , "移除序號["+tranSeqno+"]");
  setString(3 , tranSeqno);
  setString(4 , javaProgram);
  setString(5 , getValue("p_seqno",inti));
  setString(6 , getValue("bonus_type",inti));

  updateTable();
  return;
 }
// ************************************************************************
 void  loadActAcno() throws Exception
 {
  extendField = "acno.";
  selectSQL = "p_seqno";
  daoTable  = "act_acno";
  whereStr  = "where card_indicator !='1' "
            + "";

  int  n = loadTable();
  setLoadData("acno.p_seqno");

  showLogMessage("I","","Load act_acno : ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample
