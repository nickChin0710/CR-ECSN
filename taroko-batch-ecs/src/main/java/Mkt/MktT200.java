/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/06/26  V1.00.14  Allen Ho   New                                        *
* 111/12/08  V1.00.15  Zuwei      sync from mega                             *
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
public class MktT200 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-對帳媒體沖銷處理程式 111/12/08 V1.00.15";
 CommFunction comm = new CommFunction();
 CommUpload   comu = null;

 String hBusiBusinessDate = "";
 String tranSeqno = "";

 int  parmCnt  = 0;
 int  vdFlag  = 0;
 String transMonth = "";
 String transSeqno = "";
 String procSeqno = "";
 String modUser   = "";

 int  totalCnt=0;
 int  megaerrCnt=0;
 int  thsrerrCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT200 proc = new MktT200();
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

   if (comm.isAppActive2(javaProgram)) 
      {
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   if ( !connectDataBase() ) 
       return(1);

   comu = new CommUpload(getDBconnect(),getDBalias());

   if ( args.length >= 1 ) { procSeqno = args[0]; }
   if ( args.length >= 2 ) 
      { 
      if (args[1].length()!=20) modUser   = args[1]; 
      }
   if (modUser.length()==0)  modUser = javaProgram;
   comu.modPgm  = javaProgram;
   comu.modUser = modUser;

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理高鐵車廂升等核銷申請資料");
   selectMktUploadfileCtl();
   if (totalCnt==0)
      {
       showLogMessage("I","","今日["+hBusiBusinessDate+"]無核銷資料");
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
 void selectMktUploadfileCtl() throws Exception
 {
  extendField = "updf.";
  selectSQL = "trans_seqno,"
            + "file_name,"
            + "rowid as rowid";
  daoTable  = "mkt_uploadfile_ctl";
  whereStr  = "where apr_flag   = 'N' "
            + "and   group_type = 'Z' "
            + "and file_type = 'MKT_THSR_REDEM' "
            ;

  if (procSeqno.length()!=0)
     whereStr  = whereStr   
               + "and   trans_seqno = ? "
               ;

  whereStr  = whereStr   
            + "order by trans_seqno "
            ;

  if (procSeqno.length()!=0)
     setString(1 , procSeqno);

  int recCnt = selectTable();

  for (int inti=0;inti<recCnt;inti++)
    {
     totalCnt++;

     transSeqno = getValue("updf.trans_seqno",inti);

     showLogMessage("I","","=========================================");
     showLogMessage("I","","轉入 TRANS_SEQNO ["+ transSeqno + "] 資料");
     if (comu.uploadfileCtl(transSeqno)!=0)
        {
         showLogMessage("I","","    無資料可處理");
         continue;
        }
     showLogMessage("I","","=========================================");

     selectMktThsrRedem();

     if (getValue("gdrm.trans_month").length()==0)
        {
         showLogMessage("I","","檔案m月份["+getValue("gdrm.trans_month")+"] 錯誤");
        }
     else
       {
        selectMktThsrRedemX();
        deleteMktThsrRedem();
        deleteMktUploadfileCtl();
        deleteMktThsrUperr();
       }

     showLogMessage("I","","=========================================");
     showLogMessage("I","","刪除 TRANS_SEQNO ["+ transSeqno + "] 資料");
     deleteMktThsrUperr1();
     showLogMessage("I","","=========================================");
     showLogMessage("I","","檢核高鐵 檔名["+ getValue("updf.file_name",inti) +"]");
     selectMktThsrRedem(inti);
     showLogMessage("I","","   處理 ["+totalCnt +"] 筆, 異常 [" +thsrerrCnt + "] 筆");
     showLogMessage("I","","=========================================");
     if (transMonth.length()!=0)
        {
         showLogMessage("I","","檢核 ECS ["+ transMonth +"] 月資料");
         selectMktThsrUptxn(inti);
         showLogMessage("I","","   處理 ["+totalCnt +"] 筆, 異常 [" +megaerrCnt + "] 筆"); 
         showLogMessage("I","","=========================================");
        }

     if (megaerrCnt+thsrerrCnt==0)
        setValue("error_memo" , "無任何異常資料");
     else
        setValue("error_memo" , "高鐵錯誤資料["+ thsrerrCnt +"]筆 , ECS 異常資料["+ megaerrCnt +"]筆");

     updateMktUploadfileCtl(inti);
    }
 }
// ************************************************************************
 void  selectMktThsrRedem(int inti) throws Exception
 {
  selectSQL  = "";
  daoTable  = "mkt_thsr_redem";
  whereStr  = "where trans_seqno = ? "
            + "order by error_code,trans_date ";

  setString(1 , getValue("updf.trans_seqno",inti));
  openCursor();

  totalCnt = 0;
  transMonth = "";
  thsrerrCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;
    if (transMonth.length()==0)
       {
        transMonth = getValue("trans_month");
        deleteMktThsrUperr2();
       }

    if (selectMktThsrRedem1()!=0) 
       {
        thsrerrCnt++;
        setValue("uerr.error_code"                , "05");
        setValue("uerr.error_desc"                , "該筆已申請過");
        updateMktThsrRedem();  
        insertMktThsrUperr(0);  
        continue;        
       }

    if (selectMktThsrUptxn1()!=0) 
       {
        thsrerrCnt++;
        setValue("uerr.error_code"                , "01");
        setValue("uerr.error_desc"                , "找不到交易序號");
        updateMktThsrRedem();  
        insertMktThsrUperr(0);
        continue;        
       }

    if (getValue("utxn.proc_flag").equals("0"))  
       {
        thsrerrCnt++;
        setValue("uerr.error_code"                , "02");
        setValue("uerr.error_desc"                , "該筆交易尚未處理");
        updateMktThsrRedem();  
        insertMktThsrUperr(0);
        continue;        
       }

    if ((getValue("utxn.error_code").compareTo("80")>=0)&&
        (getValue("utxn.error_code").compareTo("99")<=0))
       {
        thsrerrCnt++;
        setValue("uerr.error_code"                , getValue("utxn.error_code"));
        setValue("uerr.error_desc"                , getValue("utxn.error_desc"));
        updateMktThsrRedem();  
        insertMktThsrUperr(0);
        continue;        
       }
    updateMktThsrRedem1();  

    if (getValue("utxn.trans_date").equals("trans_date"))  //
       {
        thsrerrCnt++;
        setValue("uerr.error_code"                , "03");
        setValue("uerr.error_desc"                , "交易日期不合 ");
        updateMktThsrRedem();  
        insertMktThsrUperr(0);
        continue;        
       }
    if (getValue("utxn.trans_type").equals("trans_type")) 
       {
        thsrerrCnt++;
        setValue("uerr.error_code"                , "04");
        setValue("uerr.error_desc"                , "交易類別不合  ");
        updateMktThsrRedem();  
        insertMktThsrUperr(0);
        continue;        
       }
    setValue("uerr.error_code"                , "00");
    setValue("uerr.error_desc"                , "");
    updateMktThsrRedem();  
   } 
  closeCursor();
 }
// ************************************************************************
 int updateMktThsrRedem() throws Exception
 {
  daoTable  = "mkt_thsr_redem";
  updateSQL = "error_code  = ?,"
            + "error_desc  = ?,"
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";   
  whereStr  = "where serial_no   = ? "
            + "and   trans_seqno = ? "
            ;

  setString(1 , getValue("uerr.error_code"));
  setString(2 , getValue("uerr.error_desc"));
  setString(3 , javaProgram);
  setString(4 , getValue("serial_no"));
  setString(5 , getValue("trans_seqno"));

  int n = updateTable();

  if (n==0) return(1);

  return 0;
 }
// ************************************************************************
 void  selectMktThsrUptxn(int inti) throws Exception
 {
  String fstDate = transMonth+"01";

  selectSQL  = "";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "where file_date between ? and ? "
            ;

  setString(1 , comm.nextDate(fstDate));
  setString(2 , comm.nextMonthDate(fstDate,1));

  openCursor();

  totalCnt = 0;
  megaerrCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    if (selectMktThsrRedem1()!=0) 
       {
        megaerrCnt++;
        setValue("uerr.error_code"                , "11");
        setValue("uerr.error_desc"                , "不在高鐵對帳報表");
        insertMktThsrUperr(1);  
        continue;        
       }
   } 
  closeCursor();
 }
// ************************************************************************
 int selectMktThsrUptxn1() throws Exception
 {
  extendField = "utxn.";
  selectSQL = "";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "where serial_no  = ? "
            ;

  setString(1 , getValue("serial_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  return(0);
 }
// ************************************************************************
 int selectMktThsrRedem1() throws Exception
 {
  extendField = "rdem.";
  selectSQL = "";
  daoTable  = "mkt_thsr_redem";
  whereStr  = "where serial_no  = ? "
            ;

  setString(1 , getValue("serial_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return (1);

  return(0);
 }
// ************************************************************************
 int updateMktUploadfileCtl(int inti) throws Exception
 {
  daoTable  = "mkt_uploadfile_ctl";
  updateSQL = "error_cnt   = ?,"
            + "error_memo  = ?,"
            + "proc_flag   = 'Y',"
            + "proc_date   = ?,"
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";   
  whereStr  = "where rowid  = ? "
            ;

  setInt(1 , thsrerrCnt + megaerrCnt);
  setString(2 , getValue("error_memo"));
  setString(3 , sysDate);
  setString(4 , javaProgram);
  setRowId(5 , getValue("updf.rowid",inti));

  int n = updateTable();

  return n;
 }
// ************************************************************************
 void  insertMktThsrUperr(int insertType) throws Exception
 {
  dateTime();
  setValue("uerr.trans_seqno"               , transSeqno);
  setValue("uerr.trans_month"               , transMonth);
  setValue("uerr.serial_no"                 , getValue("serial_no"));
  setValue("uerr.thsr_type"                 , "1");

  if (insertType==0)
     {   
      setValue("uerr.file_date"                 , sysDate);
      setValue("uerr.error_type"                , "1");
      setValue("uerr.ecs_trans_type"            , getValue("utxn.trans_type"));
      setValue("uerr.ecs_trans_date"            , getValue("utxn.trans_date"));
      setValue("uerr.ecs_trans_time"            , getValue("utxn.trans_time"));
      setValue("uerr.ecs_org_trans_date"        , getValue("utxn.org_trans_date"));
      setValue("uerr.ecs_pay_cardid"            , getValue("utxn.pay_cardid"));
      setValue("uerr.ecs_authentication_code"   , getValue("utxn.authentication_code"));
      setValue("uerr.ecs_pnr"                   , getValue("utxn.pnr"));
      setValue("uerr.ecs_ticket_id"             , getValue("utxn.ticket_id"));
      setValue("uerr.ecs_trans_amount"          , getValue("utxn.trans_amount"));
      setValue("uerr.ecs_train_no"              , getValue("utxn.train_no"));
      setValue("uerr.ecs_seat_no"               , getValue("utxn.seat_no"));
      setValue("uerr.ecs_org_serial_no"         , getValue("utxn.org_serial_no"));
      setValue("uerr.thsr_trans_type"            , getValue("trans_type"));
      setValue("uerr.thsr_trans_date"            , getValue("trans_date"));
      setValue("uerr.thsr_trans_time"            , getValue("trans_time"));
      setValue("uerr.thsr_org_trans_date"        , getValue("org_trans_date"));
      setValue("uerr.thsr_pay_cardid"            , getValue("pay_cardid"));
      setValue("uerr.thsr_authentication_code"   , getValue("authentication_code"));
      setValue("uerr.thsr_pnr"                   , getValue("pnr"));
      setValue("uerr.thsr_ticket_id"             , getValue("ticket_id"));
      setValue("uerr.thsr_trans_amount"          , getValue("trans_amount"));
      setValue("uerr.thsr_train_no"              , getValue("train_no"));
      setValue("uerr.thsr_seat_no"               , getValue("seat_no"));
      setValue("uerr.thsr_org_serial_no"         , getValue("org_serial_no"));
     }
  else
     { 
      setValue("uerr.file_date"                 , getValue("file_date")); 
      setValue("uerr.error_type"                , "2");
      setValue("uerr.ecs_trans_type"            , getValue("trans_type"));
      setValue("uerr.ecs_trans_date"            , getValue("trans_date"));
      setValue("uerr.ecs_trans_time"            , getValue("trans_time"));
      setValue("uerr.ecs_org_trans_date"        , getValue("org_trans_date"));
      setValue("uerr.ecs_pay_cardid"            , getValue("pay_cardid"));
      setValue("uerr.ecs_authentication_code"   , getValue("authentication_code"));
      setValue("uerr.ecs_pnr"                   , getValue("pnr"));
      setValue("uerr.ecs_ticket_id"             , getValue("ticket_id"));
      setValue("uerr.ecs_trans_amount"          , getValue("trans_amount"));
      setValue("uerr.ecs_train_no"              , getValue("train_no"));
      setValue("uerr.ecs_seat_no"               , getValue("seat_no"));
      setValue("uerr.ecs_org_serial_no"         , getValue("org_serial_no"));
      setValue("uerr.thsr_trans_type"            , getValue("rdem.trans_type"));
      setValue("uerr.thsr_trans_date"            , getValue("rdem.trans_date"));
      setValue("uerr.thsr_trans_time"            , getValue("rdem.trans_time"));
      setValue("uerr.thsr_org_trans_date"        , getValue("rdem.org_trans_date"));
      setValue("uerr.thsr_pay_cardid"            , getValue("rdem.pay_cardid"));
      setValue("uerr.thsr_authentication_code"   , getValue("rdem.authentication_code"));
      setValue("uerr.thsr_pnr"                   , getValue("rdem.pnr"));
      setValue("uerr.thsr_ticket_id"             , getValue("rdem.ticket_id"));
      setValue("uerr.thsr_trans_amount"          , getValue("rdem.trans_amount"));
      setValue("uerr.thsr_train_no"              , getValue("rdem.train_no"));
      setValue("uerr.thsr_seat_no"               , getValue("rdem.seat_no"));
      setValue("uerr.thsr_org_serial_no"         , getValue("rdem.org_serial_no"));
     }
  setValue("uerr.mod_time"                 , sysDate+sysTime);
  setValue("uerr.mod_pgm"                  , javaProgram);

  extendField = "uerr.";
  daoTable = "mkt_thsr_uperr";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_mkt_thsr_uperr error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 int deleteMktThsrUperr1() throws Exception
 {
  daoTable  = "mkt_thsr_uperr";
  whereStr  = "WHERE trans_seqno  = ? ";

  setString(1 , transSeqno);

  int recCnt = deleteTable();

  showLogMessage("I","","delete mkt_thsr_uperr  筆數 ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int deleteMktThsrUperr2() throws Exception
 {
  daoTable  = "mkt_thsr_uperr";
  whereStr  = "WHERE trans_month  = ? "
            + "and   thsr_type    = '1' "
            ;

  setString(1 , transMonth);

  int recCnt = deleteTable();

  return(0);
 }
// ************************************************************************
 int selectMktThsrRedem() throws Exception
 {
  extendField = "gdrm.";
  selectSQL = "trans_month";
  daoTable  = "mkt_thsr_redem";
  whereStr  = "where trans_seqno  = ? "
            + "fetch first 1 row only "
            ;
  setString(1 , transSeqno);

  int recCnt = selectTable();

  if (recCnt==0) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktThsrRedemX() throws Exception
 {
  extendField = "gdr1.";
  selectSQL = "trans_seqno";
  daoTable  = "mkt_thsr_redem";
  whereStr  = "WHERE trans_seqno != ? "
            + "and   trans_month  = ? "
            ;

  setString(1 , transSeqno);
  setString(2 , getValue("gdrm.trans_month"));

  int recCnt = selectTable();

  if (recCnt==0) return(1);

  return(0);
 }
// ************************************************************************
 int deleteMktThsrRedem() throws Exception
 {
  daoTable  = "mkt_thsr_redem";
  whereStr  = "WHERE trans_seqno = ? "
            ;

  setString(1 , getValue("gdr1.trans_seqno"));

  int recCnt = deleteTable();

  showLogMessage("I","","delete mkt_thsr_redemm  筆數 ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int deleteMktUploadfileCtl() throws Exception
 {
  daoTable  = "mkt_uploadfile_ctl";
  whereStr  = "WHERE trans_seqno = ? "
            ;

  setString(1 , getValue("gdr1.trans_seqno"));

  int recCnt = deleteTable();

  showLogMessage("I","","delete mkt_uploadfile_ctl  筆數 ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int deleteMktThsrUperr() throws Exception
 {
  daoTable  = "mkt_thsr_uperr";
  whereStr  = "WHERE trans_month = ? "
            + "and thsr_type     = '1' "
            ;

  setString(1 , getValue("gdr1.trans_month"));

  int recCnt = deleteTable();

  showLogMessage("I","","delete mkt_thsr_uperr_1  筆數 ["+ recCnt +"]");

  return(0);
 }
// ************************************************************************
 int updateMktThsrRedem1() throws Exception
 {
  daoTable  = "mkt_thsr_redem";
  updateSQL = "refund_amt  = ?,"
            + "refund_bp   = ?,"
            + "pay_type    = ?,"
            + "deduct_bp   = ?,"
            + "deduct_amt  = ?,"
            + "mod_pgm     = ?,"
            + "mod_time    = sysdate";   
  whereStr  = "where serial_no   = ? "
            + "and   trans_seqno = ? "
            ;

  setInt(1    , getValueInt("utxn.refund_amt"));
  setInt(2    , getValueInt("utxn.refund_bp")
              + getValueInt("utxn.refund_bp_tax"));
  setString(3 , getValue("utxn.pay_type"));
  setInt(4    , getValueInt("utxn.deduct_bp")
              + getValueInt("utxn.deduct_bp_tax"));
  setInt(5    , getValueInt("utxn.deduct_amt"));
  setString(6 , javaProgram);
  setString(7 , getValue("serial_no"));
  setString(8 , getValue("trans_seqno"));

  int n = updateTable();

  if (n==0)
     showLogMessage("I","","update mkt_thsr_redem_ error");

  return n;
 }
// ************************************************************************


}  // End of class FetchSample




