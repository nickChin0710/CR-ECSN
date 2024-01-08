/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/03/04  V1.00.19  Allen Ho   mkt_T060                                   *
* 111/12/07  V1.00.20  Zuwei      sync from mega                             *
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
public class MktT060 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-紅利扣點不足簡訊發送處理程式 111/12/07 V1.00.20";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate  = "";
 boolean debug = false;

 long    totalCnt=0;
 int  cnt1=0;
 int[]  procInt = new int[10];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT060 proc = new MktT060();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
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

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       showLogMessage("I","","PARM 2 : [DEBUG]");
       return(1);
      }

   if ( args.length >= 1 )
      { businessDate = args[0]; }

   if ( args.length == 2 )
      { if (args[1].toUpperCase().equals("DEBUG")) debug=true; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (!businessDate.substring(6,8).equals("01"))
      {
       showLogMessage("I","","本程式只在每月1日執行,本日為"+businessDate+"日..");
       return(0);
      }
    
   showLogMessage("I","","===============================");
   showLogMessage("I","","檢核媒體檔案.....");

   if (debug)
      {
       showLogMessage("I","","DEBUG模式不檢核(尚未收到上月最後一日檔案)");
      }
   else if (selectEcsFtpLog()!=0)
      {
       showLogMessage("I","","尚未收到上月最後一日["+ comm.lastDate(businessDate) +"]檔案, 暫停發送");
       return(0);
      }
   showLogMessage("I","","檢核完成 !");
   
   showLogMessage("I","","===============================");
   showLogMessage("I","","檢核簡訊設定.....");
   if (selectSmsMsgId()!=0)
      {
       showLogMessage("I","","未設定發送簡訊, 無法發送");
       return(0);
      }
   showLogMessage("I","","檢核 OK ");

   showLogMessage("I","","===============================");
   showLogMessage("I","","載入暫存資料.....");
   loadMktBnData();
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrUptxn();
   showLogMessage("I","","累計處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","    ID  錯誤  ["+ procInt[0] +"] 筆");
   showLogMessage("I","","    ID  不發  ["+ procInt[1] +"] 筆");
   showLogMessage("I","","    手機不發  ["+ procInt[2] +"] 筆");
   showLogMessage("I","","    手機錯誤  ["+ procInt[3] +"] 筆");
   showLogMessage("I","","    實際發送  ["+ procInt[4] +"] 筆");
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

  if (businessDate.length()==0)
      businessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+businessDate+"]");
 }
// ************************************************************************ 
 void  selectMktThsrUptxn() throws Exception
 {
  selectSQL = "max(p_seqno) as p_seqno, "
            + "max(acct_type) as acct_type, "
            + "min(issue_date) as issue_date,"
            + "a.card_mode,"
            + "major_id_p_seqno ";
  daoTable  = "mkt_thsr_uptxn a,mkt_thsr_upmode b";
  whereStr  = "WHERE   proc_flag       = '1' "
            + "AND     bp_check_date  != '' "
            + "AND     pay_type        = '' "
            + "AND     sms_date        = '' "  
            + "AND     trans_date      < ? "
            + "AND     a.card_mode     = b.card_mode "
            + "AND     b.add_file_flag = 'Y' "
            + "group   by major_id_p_seqno,a.card_mode "
            + "having  min(issue_date) < ? "       
            + "";                

  setString(1 , businessDate);
  setString(2 , comm.nextNDate(businessDate,-60));

  showLogMessage("I","","  日期區間 : ["+ businessDate 
                                        + "][" 
                                        + "] 不發送 >[" 
                                        + comm.nextNDate(businessDate,-60)
                                        + "]"); 
  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;
    if (selectCrdIdno()!=0) 
       {
        showLogMessage("I","","  身分證號流水號 : ["+ getValue("major_id_p_seqno") +"] 不發送");
        procInt[0]++;
        continue;
       }

    setValue("data.data_type" , "1");
    setValue("data.data_code" , getValue("idno.id_no"));
    cnt1 = getLoadData("data.data_type,data.data_code");
    if (cnt1!=0) 
       {
//      showLogMessage("I","","  身分證號 : ["+ getValue("idno.id_no") +"] 不發送");
        setValue("error_desc" , "身分證號不發簡訊");   
        updateMktThsrUptxn("X");
        updateMktThsrUptxn1("I");
        procInt[1]++;
        continue;
       }

    setValue("data.data_type" , "2");
    setValue("data.data_code" , getValue("idno.cellar_phone"));
    cnt1 = getLoadData("data.data_type,data.data_code");
    if (cnt1!=0) 
       {
//      showLogMessage("I","","  手機號碼 : ["+ getValue("idno.cellar_phone") +"] 不發送"); 
        setValue("error_desc" , "手機號碼不發簡訊");   
        updateMktThsrUptxn("X");
        updateMktThsrUptxn1("I");
        procInt[2]++;
        continue;
       }

    selectPtrActgeneralN();
    selectMktThsrUpmode();

    setValue("cellphone_check_flag"   , "Y");

    if (getValue("idno.cellar_phone").length()!=10) 
       setValue("cellphone_check_flag"   , "N");

    if (!getValue("idno.cellar_phone").matches("[0-9]+"))   // or matches("\\d+")
       setValue("cellphone_check_flag"   , "N");

    setValue("sms_flag", getValue("cellphone_check_flag"));


    String tmpstr = "";

    tmpstr = getValue("smid.msg_userid") + ","
           + getValue("smid.msg_id") + ","
           + getValue("idno.cellar_phone") + ","
           + getValueInt("mode.ex_ticket_amt") 
           ;

    setValue("msg_desc",tmpstr);

    insertSmsMsgDtl();

    setValue("error_desc" , "");   
    if (getValue("cellphone_check_flag").equals("N"))
       {
        setValue("error_desc" , "手機號碼錯誤無法發簡訊");   
        procInt[3]++;
       }
    else
        procInt[4]++;

//  showLogMessage("I","","  身分證號 : ["+ getValue("idno.id_no") +"]"); 
//  showLogMessage("I","","  手機號碼 : ["+ getValue("idno.cellar_phone") +"]"); 

    updateMktThsrUptxn(getValue("cellphone_check_flag"));
    updateMktThsrUptxn1("I");

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void updateMktThsrUptxn(String smsFlag) throws Exception
 {
  dateTime();
  updateSQL = "sms_flag   = ?, "
            + "sms_date   = ?, "
            + "error_desc = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE   proc_flag   = '1' "
            + "AND     bp_check_date != '' "
            + "AND     pay_type       = '' "
            + "AND     sms_date       = '' "      
            + "AND     trans_date     < ? "
            + "and     issue_date     < ? "       
            + "AND     major_id_p_seqno =  ? "
            + "AND     card_mode        =  ? "
            ;

  setString(1 , smsFlag);
  setString(2 , businessDate);
  setString(3 , getValue("error_desc"));
  setString(4 , javaProgram);
  setString(5 , businessDate);
  setString(6 , comm.nextNDate(businessDate,-60));
  setString(7 , getValue("major_id_p_seqno"));
  setString(8 , getValue("card_mode"));

  updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn error "+getValue("major_id_p_seqno")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void updateMktThsrUptxn1(String smsFlag) throws Exception
 {
  dateTime();
  updateSQL = "sms_flag   = ?, "
            + "sms_date   = ?, "
            + "error_desc = '發卡日期60日內不發簡訊', "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE   proc_flag   = '1' "
            + "AND     bp_check_date != '' "
            + "AND     pay_type       = '' "
            + "AND     sms_date       = '' "      
            + "AND     trans_date     < ? "
            + "and     issue_date     >=  ? "       
            + "AND     major_id_p_seqno =  ? "
            + "AND     card_mode        =  ? "
            ;

  setString(1 , smsFlag);
  setString(2 , businessDate);
  setString(3 , javaProgram);
  setString(4 , businessDate);
  setString(5 , comm.nextNDate(businessDate,-60));
  setString(6 , getValue("major_id_p_seqno"));
  setString(7 , getValue("card_mode"));

  updateTable();

  return;
 }
// ************************************************************************
 int  selectEcsFtpLog() throws Exception
 {
  daoTable  = "ecs_ftp_log";
  whereStr  = "where ref_ip_code = 'MKT_FTP_GET' "
            + "and   file_name   = ? "
            + "and   proc_code   = 'Y' ";

  setString(1 , "coup"+ comm.lastDate(businessDate) +".txt");

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************ 
 int  selectSmsMsgId() throws Exception
 {
  extendField = "smid.";
  selectSQL = "msg_id,"
            + "msg_dept,"
            + "msg_userid";
  daoTable  = "sms_msg_id";
  whereStr  = "WHERE msg_pgm       = ? "
            + "AND   msg_send_flag = 'Y' ";

  setString(1, javaProgram);
  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "chi_name,"  
            + "cellar_phone,"
            + "id_no";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno = ?";

  setString(1,getValue("major_id_p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void selectPtrActgeneralN() throws Exception
 {
  extendField = "genn.";
  selectSQL = "max(round(revolving_interest1*365/100,2)) as revolving_interest1"  
            + "";
  daoTable  = "ptr_actgeneral_n";
  whereStr  = "WHERE acct_type = ?";

  setString(1 , getValue("acct_type"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_actgeneral_n error!" );
      showLogMessage("I","","acct_type ["+getValue("acct_type")+"]" );
      exitProgram(1);
     }
 }
// ************************************************************************
 void  insertSmsMsgDtl() throws Exception
 {
  dateTime();
  extendField = "sdtl.";
  setValue("sdtl.msg_seqno"            , comr.getSeqno("ECS_MODSEQ")); 
  setValue("sdtl.msg_dept"             , getValue("msid.msg_dept")); 
  setValue("sdtl.msg_userid"           , getValue("msid.msg_userid")); 
  setValue("sdtl.msg_pgm"              , javaProgram); 
  setValue("sdtl.id_no"                , getValue("idno.id_no")); 
  setValue("sdtl.id_p_seqno"           , getValue("major_id_p_seqno")); 
  setValue("sdtl.p_seqno"              , getValue("p_seqno")); 
  setValue("sdtl.acct_type"            , getValue("acct_type")); 
  setValue("sdtl.msg_id"               , getValue("msid.msg_id")); 
  setValue("sdtl.cellar_phone"         , getValue("idno.cellar_phone")); 
  setValue("sdtl.cellphone_check_flag" , getValue("cellphone_check_flag")); 
  setValue("sdtl.chi_name"             , getValue("idno.chi_name")); 
  setValue("sdtl.msg_desc"             , getValue("msg_desc")); 
  setValue("sdtl.add_mode"             , "B"); 
  setValue("sdtl.crt_date"             , sysDate);
  setValue("sdtl.crt_user"             , "AIX");
  setValue("sdtl.apr_date"             , sysDate);
  setValue("sdtl.apr_user"             , "AIX");
  setValue("sdtl.apr_flag"             , "Y");
  setValue("sdtl.mod_user"             , "AIX");
  setValue("sdtl.mod_time"             , sysDate+sysTime);
  setValue("sdtl.mod_pgm"              , javaProgram);

  daoTable = "SMS_MSG_DTL";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert_sms_msg_dtl  error[dupRecord]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************
 int selectMktThsrUpmode() throws Exception 
 {
  extendField = "mode.";
  selectSQL = "ticket_pnt_cnt,"
            + "ex_ticket_amt";                                      
  daoTable  = "mkt_thsr_upmode";
 whereStr  = "WHERE  ticket_pnt_cond = 'Y' "
           + "and    card_mode       = ? "
           ;

  setString(1 , getValue("card_mode"));
  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_type,"
            + "data_code";
  daoTable  = "mkt_bn_data";
  whereStr  = "where table_name = 'MKT_THSR_UPTXN' "
            + "and   data_key   = 'THSR_LIST' "    
            + "order by data_type,data_code "
            ;

  int  n = loadTable();
  setLoadData("data.data_type,data.data_code");

  showLogMessage("I","","Load crd_idno Count: ["+n+"]");
 }
// ************************************************************************

}  // End of class FetchSample

