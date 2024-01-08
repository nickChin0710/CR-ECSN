/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/05/22   V1.00.00    Allen Ho    mkt_T120-4                             *
* 109-12-11   V1.00.01    tanwei      updated for project coding standard    *
* 112-10-12   V1.00.02    Kirin       紅利點數不足不發送簡訊 & insert error       * 
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;                    

@SuppressWarnings("unchecked")
public class MktT140 extends AccessDAO
{
 private  String progname = "高鐵標準車廂-扣除紅利或費用處理程式 109/12/11 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate  = "";
 String tranSeqno     = "";

 long    totalCnt=0;
 int[] procCnt = new int[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT140 proc = new MktT140();
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

  if (selectPtrSysParm()!=0)
     {
      showLogMessage("I","","高體表準車廂紅利折抵參數(ptr_sys_parm)  error");
      showLogMessage("I","","    wf_parm='SYSPARM' ");
      showLogMessage("I","","    wf_key ='THSR_DISC'");
      return(1);
     }
/* 調整:紅利點數不足不發送簡訊 (原作法:必發簡訊) 
  if (selectSmsMsgId()!=0)
     {
      showLogMessage("I","","未設定發送簡訊 error");
      showLogMessage("I","","    一般卡 [MKT_T120_N]");
      showLogMessage("I","","    頂級卡 [MKT_T120_T]");
      return(1);
     }
*/
   showLogMessage("I","","=========================================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrDisc();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","累計              筆數 ["+totalCnt   +"]");
   showLogMessage("I","","  不處理         筆數 ["+procCnt[0]+"]"); 
   showLogMessage("I","","  扣除費用       筆數 ["+procCnt[1]+"]");

   showLogMessage("I","","  扣除紅利點數   筆數 ["+procCnt[2]+"]"); 
   showLogMessage("I","","=========================================");
   /* 20231012  
   showLogMessage("I","","紅利點數不足發送簡訊處理.....");
   selectMktThsrDisc1();
   showLogMessage("I","","累計發送簡訊    筆數 ["+totalCnt   +"]");
   showLogMessage("I","","=========================================");
*/
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
  selectSQL = "p_seqno, "
            + "acct_type, "
            + "id_p_seqno,"
            + "major_id_p_seqno,"
            + "major_card_no,"
            + "card_no, "
            + "trans_date, "
            + "serial_no, "
            + "discount_value, "
            + "rowid as rowid ";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE   proc_flag    = '1' "  
            + "and     trans_type  != 'R' "
            + "order   by p_seqno,trans_date,trans_time";
            ;

  openCursor();

  int lastCnt = 0;
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    if (getValueInt("discount_value")<=0)
       {
        procCnt[0]++;
        setValue("error_code" , "20");
        setValue("error_desc" , "票價差異小於等於零, 不處理");
        updateMktThsrDisc();
        continue;
       }


    int endTranBp =  (int)Math.round(
                      getValueInt("discount_value")
                    * Integer.valueOf(getValue("wf_value"))
                    / Integer.valueOf(getValue("wf_value2"))
                    * 1.0);
    
//showLogMessage("I","","p_seqno     : ["+ getValue("p_seqno")  +"]");
//showLogMessage("I","","end_tran_bp : ["+ end_tran_bp  +"]");
//showLogMessage("I","","bonus_drl   : ["+ comb.bonus_sum(getValue("p_seqno"))  +"]");
   
   if (comb.bonusSum(getValue("p_seqno"))< endTranBp )
       {
        procCnt[1]++;

        setValueInt("dest_amt" , getValueInt("discount_value"));

        insertBilSysexp();

        setValue("deduct_type","2");
        showLogMessage("I","","updateMktThsrDisc1()     : [" +"]"); 
        updateMktThsrDisc1();
       }
    else
       {
        procCnt[2]++;
        setValueInt("bonu.beg_tran_bp"  , endTranBp*-1);
        setValueInt("bonu.end_tran_bp"  , endTranBp*-1);

        
        insertMktBonusDtl();
        comb.modPgm = javaProgram; 
        comb.bonusFunc(tranSeqno);

        showLogMessage("I","","-------- updateMktThsrDisc2()  : ------" ); 
        setValue("deduct_type","1");
        updateMktThsrDisc2();
       }

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void  selectMktThsrDisc1() throws Exception
 {
  selectSQL = "major_id_p_seqno, "
            + "p_seqno,"
            + "max(acct_type) as acct_type ";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE proc_flag    = '2' "  
            + "and   trans_type  != 'R' "
            + "and   deduct_type  = '2' "
            + "and   proc_date    = ?  "
            + "and   sms_flag     = 'N' "
            + "group by major_id_p_seqno,p_seqno";
            ;

  setString(1 , hBusiBusinessDate);
  openCursor();

  int lastCnt = 0;
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    selectCrdIdno();
    procSendSms();

    updateMktThsrDisc3();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int insertBilSysexp() throws Exception
 {
  dateTime();
  extendField = "sysp.";
  setValue("sysp.card_no"            , getValue("card_no"));
  setValue("sysp.p_seqno"            , getValue("p_seqno"));
  setValue("sysp.acct_type"          , getValue("acct_type"));
  setValue("sysp.bill_type"          , "INHR");
  setValue("sysp.txn_code"           , "05");
  setValue("sysp.purchase_date"      , getValue("trans_date"));
  setValue("sysp.src_amt"            , getValue("dest_amt"));
  setValue("sysp.dest_amt"           , getValue("dest_amt"));
  setValue("sysp.dc_dest_amt"        , getValue("dest_amt"));
  setValue("sysp.dest_curr"          , "901");
  setValue("sysp.curr_code"          , "901");
  setValue("sysp.bill_desc"          , "高鐵標準車廂折扣紅利點數不足扣款");
  setValue("sysp.post_flag"          , "N"); 
  setValue("sysp.ref_key"            , getValue("serial_no")); 
  setValue("sysp.mod_user"           , javaProgram); 
  setValue("sysp.mod_time"           , sysDate+sysTime);
  setValue("sysp.mod_pgm"            , javaProgram);
                     
  daoTable  = "bil_sysexp";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktThsrDisc() throws Exception
 {
  dateTime();
  showLogMessage("I","","card_no : ["+getValue("card_no")+"] - ["+getValue("error_code")+"]- ["+getValue("error_desc")+"]");

  updateSQL = "proc_flag  = 'X', "
            + "proc_date  = ?, "
            + "error_code = ?,"  
            + "error_desc = ?,"  
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid = ?";

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("error_code"));
  setString(3 , getValue("error_desc"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_disc error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 int insertMktBonusDtl() throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");

  setValue("bonu.tran_date"            , sysDate);
  setValue("bonu.tran_time"            , sysTime);
  setValue("bonu.tran_seqno"           , tranSeqno);
  setValue("bonu.bonus_type"           , "BONU");
  setValue("bonu.tran_code"            , "4");
  setValue("bonu.mod_desc"             , "日期:"+getValue("trans_date")+" 流水號["+getValue("serial_no")+"]");
  setValue("bonu.mod_memo"             , "卡號["+getValue("card_no")+"]");
  setValue("bonu.acct_date"            , hBusiBusinessDate);
  setValue("bonu.proc_month"           , hBusiBusinessDate.substring(0,6));
  setValue("bonu.acct_type"            , getValue("acct_type"));
  setValue("bonu.id_p_seqno"           , getValue("major_id_p_seqno"));
  setValue("bonu.p_seqno"              , getValue("p_seqno"));
  setValue("bonu.active_name"          , "高鐵標準車廂紅利點數扣點");
  setValue("bonu.tran_pgm"             , javaProgram);
  setValue("bonu.tax_flag"             , "N");
  setValue("bonu.apr_user"             , javaProgram);
  setValue("bonu.apr_flag"             , "Y");
  setValue("bonu.apr_date"             , sysDate);
  setValue("bonu.crt_user"             , javaProgram);
  setValue("bonu.crt_date"             , sysDate);
  setValue("bonu.mod_user"             , javaProgram);
  setValue("bonu.mod_time"             , sysDate+sysTime);
  setValue("bonu.mod_pgm"              , javaProgram);
  setValue("bonu.major_card_no"        , getValue("major_card_no"));

  extendField = "bonu.";
  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktThsrDisc1() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag        = '2',"
            + "error_code       = '00',"
            + "proc_date        = ?," 
            + "deduct_type      = ?,"  
            + "dest_amt         = ?,"  
            + "sms_flag         = 'N', "
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("deduct_type"));
  setInt(3    , getValueInt("dest_amt"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktThsrDisc2() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag        = 'Y',"
            + "error_code       = '00',"
            + "proc_date        = ?," 
            + "deduct_type      = ?,"  
            + "deduct_bp        = ?,"  
            + "deduct_bp_tax    = ?,"  
            + "deduct_bp_amt    = ?,"  
            + "tran_seqno       = ?,"  
            + "sms_flag         = 'N', "
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("deduct_type"));
  setInt(3    , comb.tranBpNotax*-1);
  setInt(4    , comb.tranBpTax*-1);
  setInt(5    , getValueInt("discount_value"));
  setString(6 , tranSeqno);
  setString(7 , javaProgram);
  setRowId(8  , getValue("rowid"));
  
  updateTable();
  return;
 }
// ************************************************************************
 void updateMktThsrDisc3() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag        = 'Y',"
            + "sms_date         = ?," 
            + "sms_flag         = ?";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE proc_flag    = '2' "  
            + "and   trans_type  != 'R' "
            + "and   deduct_type  = '2' "
            + "and   sms_flag     = 'N' "
            + "and   proc_date    = ?  "
            + "and   major_id_p_seqno = ?  "
            ;

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("sms_flag"));
  setString(3 , hBusiBusinessDate);
  setString(4 , getValue("major_id_p_seqno"));

  updateTable();
  return;
 }
// ************************************************************************
 int selectPtrSysParm() throws Exception
 {
  selectSQL = "wf_value,"              /* 例:100 點   */
            + "wf_value2";             /*    抵 5  元 */
  daoTable  = "ptr_sys_parm";
  whereStr  = "where wf_parm='SYSPARM' "
            + "and   wf_key ='THSR_DISC' ";

  selectTable();

  if ( notFound.equals("Y")) return(1); 

  return(0);
 }
// ************************************************************************
 void selectCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "chi_name,"  
            + "cellar_phone,"
            + "id_no";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno = ?";

  setString(1,getValue("major_id_p_seqno"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select crd_idno error!" );
      showLogMessage("I","","major_id_p_seqno["+getValue("major_id_p_seqno")+"]" );
      exitProgram(1);
     }
 }
// ************************************************************************
 int  selectSmsMsgId() throws Exception
 {
  extendField = "msid.";
  selectSQL = "msg_id,"
            + "msg_dept,"
            + "msg_userid";
  daoTable  = "sms_msg_id";
  whereStr  = "WHERE msg_pgm in ('MKT_T120_N','MKT_T120_T') "
            + "AND   msg_send_flag ='Y' "
            + "ORDER BY msg_pgm ";

  int recCnt = selectTable();

  if ( recCnt!=2) return(1);

  return(0);
 }
// ************************************************************************
 void procSendSms() throws Exception
 {
  setValue("smsd.cellphone_check_flag"   , "Y");

  if (getValue("idno.cellar_phone").length()!=10)
     setValue("smsd.cellphone_check_flag"   , "N");

  if (!getValue("idno.cellar_phone").matches("[0-9]+"))   // or matches("\\d+")
     setValue("cellphone_check_flag"   , "N");

  setValue("sms_flag", getValue("cellphone_check_flag"));

  int smsNum = 0;
  if (getValue("card_mode").equals("1")) smsNum = 1;
    
  String tmpstr = getValue("msid.msg_userid",smsNum) + "," 
                + getValue("msid.msg_id",smsNum) + "," 
                + getValue("idno.cellar_phone") + "," 
                + getValue("idno.chi_name");

   setValue("smsd.msg_desc",tmpstr);

  insertSmsMsgDtl(smsNum);
 }
// ************************************************************************
 void  insertSmsMsgDtl(int smsNum) throws Exception
 {
  extendField = "smsd.";
  setValue("smsd.msg_seqno"            , comr.getSeqno("ECS_MODSEQ")); 
  setValue("smsd.msg_pgm"              , javaProgram); 
  setValue("smsd.msg_dept"             , getValue("msid.msg_dept",smsNum)); 
  setValue("smsd.msg_userid"           , getValue("msid.msg_userid",smsNum)); 
  setValue("smsd.msg_id"               , getValue("msid.msg_id",smsNum)); 
  setValue("smsd.cellar_phone"         , getValue("idno.cellar_phone")); 
  setValue("smsd.chi_name"             , getValue("idno.chi_name")); 
  setValue("smsd.p_seqno"              , getValue("p_seqno")); 
  setValue("smsd.acct_type"            , getValue("acct_type")); 
  setValue("smsd.id_p_seqno"           , getValue("major_id_p_seqno")); 
  setValue("smsd.add_mode"             , "B"); 
  setValue("smsd.crt_date"             , sysDate);
  setValue("smsd.crt_user"             , "AIX");
  setValue("smsd.apr_date"             , sysDate);
  setValue("smsd.apr_user"             , "AIX");
  setValue("smsd.apr_flag"             , "Y");
  setValue("smsd.mod_user"             , "AIX");
  setValue("smsd.mod_time"             , sysDate+sysTime);
  setValue("smsd.mod_pgm"              , javaProgram);

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

}  // End of class FetchSample
