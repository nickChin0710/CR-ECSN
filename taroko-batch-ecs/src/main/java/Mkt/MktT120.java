/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/23   V1.00.00    Allen Ho    mkt_t110-2                             *
* 109-12-11   V1.00.01    tanwei      updated for project coding standard    *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;                       

@SuppressWarnings("unchecked")
public class MktT120 extends AccessDAO
{
 private  String progname = "高鐵標準車廂-檢核授權處理程式 109/12/11 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate   = "";

 int parmCnt=0;
 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT120 proc = new MktT120();
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

   selectPtrBusinday();

   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrDisc();
   showLogMessage("I","","處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","===============================");

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
 void  selectMktThsrDisc() throws Exception
 {
  selectSQL = "trans_date,"
            + "trans_time,"
            + "trans_type,"
            + "serial_no,"              // for debug
            + "orig_serial_no,"
            + "pay_cardid,"
            + "authentication_code,"
            + "rowid as rowid";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE proc_date   = '00000000' "
            + "AND   proc_flag   = '0' "
            + "ORDER BY trans_date,trans_type ";
   
  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
//showLogMessage("I","","本日1 : [ serial_no ]["+getValue("serial_no")+"]");

    setValue("tx_date_s" , comm.lastDate(getValue("trans_date")));
    setValue("tx_date_e" , comm.nextDate(getValue("trans_date")));
//  setValue("tx_time" , comm.nextNDateMin(getValue("trans_date")+getValue("trans_time"),1));
//  setValue("tx_time" , comm.nextNDateMin(getValue("trans_date")+getValue("trans_time"),-1));

    setValue("proc_flag"  , "1");
    setValue("error_code" , "");
    setValue("error_desc" , "");
    setValue("match_flag" , "N");

    int int2 = 0;
    if (getValue("trans_type").equals("P"))
       {
        int2 = selectCcaAuthTxlog();
/*
// debug for test beg
        if (int2 != 0)
           {
            if (select_crd_card_1()!=0) int2=1;//debug used only
            else int2 = 0;
           }
// debug for test end
*/
       }
    else
       {
        int2=selectMktThsrDiscA();   //   依比對結果判斷
//showLogMessage("I","","本日2 : ["+ int2 +"]["+getValue("orig_serial_no")+"]");
       }

    if (int2==1) 
       {
        setValue("proc_flag"  , "X");   //   查無交易則視為失敗
        setValue("error_code" , "90");
        setValue("error_desc" , "比對授權失敗");
       }
    if (int2==2) 
       {
        setValue("proc_flag" , "X");   //   查有多筆交易紀錄
        setValue("error_code" , "91");
        setValue("error_desc" , "查有多筆交易紀錄");
       }

    if (int2==3) 
       {
        setValue("proc_flag" , "X");   //   退票無法比對購票資料
        setValue("error_code" , "93");
        setValue("error_desc" , "退票無法比對購票資料");
       }
        
    if (!getValue("proc_flag").equals("1")) 
       {
        updateMktThsrDisc1();
        continue;
       }

    setValue("match_flag" , "Y");

    selectCrdCard();

    setValue("card_mode"  , "2");
    if (selectMktTopafeeParm()==0)
       setValue("card_mode"  , "1");

    updateMktThsrDisc();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 int selectCcaAuthTxlog() throws Exception
 {
  extendField = "auth.";
  selectSQL = "card_no";
  daoTable  = "cca_auth_txlog";
  whereStr  = "where tx_date  between ? and ?  "
            + "and   auth_no    = ? "
            + "and   card_no like substr(?,1,6)||'%' "
            + "and   substr(card_no,length(card_no)-3,4) = substr(?,length(card_no)-3,4) ";
            ;

  setString(1 , getValue("tx_date_s"));
  setString(2 , getValue("tx_date_e"));
  setString(3 , getValue("authentication_code"));
  setString(4 , getValue("pay_cardid"));
  setString(5 , getValue("pay_cardid"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  if ( recCnt >1 ) return(2);
  return(0);
 }
// ************************************************************************
 int selectMktThsrDiscA() throws Exception
 {
  extendField = "disc.";
  selectSQL = "card_no";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "where serial_no   = ? "
            + "and   trans_type  = 'P' "
            + "and   card_no    != '' "
            ;

  setString(1,getValue("orig_serial_no"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(3);

  setValue("auth.card_no" , getValue("disc.card_no"));
  return(0);
 }
// ************************************************************************
 int selectMktThsrDisc1() throws Exception
 {
  extendField = "xxxx.";
  daoTable  = "mkt_thsr_disc1";
  whereStr  = "where serial_no   = ? "
            + "and   trans_type  = 'P' "
            + "and   card_no    != '' ";

  setString(1,getValue("serial_no"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) 
      exitProgram(1);

  setValue("auth.card_no" , getValue("xxxx.card_no"));

  return(0); 
 }
// ************************************************************************
 void updateMktThsrDisc1() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag        = ?,"
            + "proc_date        = ?,"
            + "error_code       = ?,"
            + "error_desc       = ?,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid  = ? ";

  setString(1 , getValue("proc_flag")); 
  setString(2 , hBusiBusinessDate); 
  setString(3 , getValue("error_code")); 
  setString(4 , getValue("error_desc")); 
  setString(5 , javaProgram);
  setRowId(6  , getValue("rowid"));


  updateTable();
  return;
 }
// ************************************************************************
 int selectMktTopafeeParm() throws Exception
 {
  extendField = "topm.";
  daoTable  = "mkt_topafee_parm";
  whereStr  = "where group_code  = ? "
            + "and   card_type   = ? ";

  setString(1,getValue("card.group_code"));
  setString(2,getValue("card.card_type"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectCrdCard() throws Exception
 {
  extendField = "card.";
  daoTable  = "crd_card";
  whereStr  = "where card_no = ? "
            ;

  setString(1,getValue("auth.card_no"));

  selectTable();
  if ( notFound.equals("Y")) return(1);

  return(0); 
 }
// ************************************************************************
 int selectCrdCard1() throws Exception
 {
  extendField = "auth.";
  daoTable  = "crd_card";
  whereStr  = "where card_no like ? "
            + "and substr(card_no,13,4) = ? "
            ;

  setString(1,getValue("pay_cardid").substring(0,6)+"%");
  setString(2,getValue("pay_cardid").substring(12,16));

  selectTable();
  if ( notFound.equals("Y")) return(1);

  return(0); 
 }
// ************************************************************************
 void updateMktThsrDisc() throws Exception
 {
  dateTime();
  updateSQL = "card_no          = ?,"
            + "p_seqno          = ?,"
            + "acct_type        = ?,"
            + "id_p_seqno       = ?,"
            + "major_id_p_seqno = ?,"
            + "major_card_no    = ?,"
            + "card_type        = ?,"
            + "group_code       = ?,"
            + "card_mode        = ?,"
            + "proc_date        = ?,"
            + "proc_flag        = ?,"
            + "error_code       = ?,"
            + "error_desc       = ?,"
            + "match_flag       = ?,"
            + "match_date       = ?,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid  = ? ";

  setString(1  , getValue("card.card_no"));
  setString(2  , getValue("card.p_seqno")); 
  setString(3  , getValue("card.acct_type"));
  setString(4  , getValue("card.id_p_seqno")); 
  setString(5  , getValue("card.major_id_p_seqno")); 
  setString(6  , getValue("card.major_card_no")); 
  setString(7  , getValue("card.card_type")); 
  setString(8  , getValue("card.group_code")); 
  setString(9  , getValue("card_mode")); 
  setString(10 , hBusiBusinessDate); 
  setString(11 , getValue("proc_flag")); 
  setString(12 , getValue("error_code")); 
  setString(13 , getValue("error_desc"));
  setString(14 , getValue("match_flag"));
  setString(15 , hBusiBusinessDate); 
  setString(16 , javaProgram);
  setRowId(17 , getValue("rowid"));

  updateTable();

  return;
 }
// ************************************************************************

}  // End of class FetchSample
