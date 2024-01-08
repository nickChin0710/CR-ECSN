/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/10   V1.00.00    Allen Ho    mkt_t120-1                             *
* 109-12-11   V1.00.01    tanwei      updated for project coding standard    *
* 112-11-17   v1.00.02    Kirin       change active_name string              *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT130 extends AccessDAO
{
 private  String progname = "高鐵標準車廂-退票退紅利點數處理程式109/12/11 V1.00.01";
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
  MktT130 proc = new MktT130();
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

   showLogMessage("I","","=========================================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrDisc();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","累計                 筆數 ["+totalCnt   +"]");
   showLogMessage("I","","  不處理             筆數 ["+(procCnt[0]+procCnt[1])+"]"); 
   showLogMessage("I","","    頂級卡非關帳日   筆數 ["+procCnt[0]+"]"); 
   showLogMessage("I","","    非扣除紅利點數   筆數 ["+procCnt[1]+"]"); 
   showLogMessage("I","","  檢核錯誤           筆數 ["+(procCnt[2]+procCnt[3] 
                                                      + procCnt[4]+procCnt[5] 
                                                      + procCnt[6]+procCnt[7])+"]"); 
   showLogMessage("I","","    無購票紀錄       筆數 ["+procCnt[2]+"]"); 
   showLogMessage("I","","    原訂票請款失敗   筆數 ["+procCnt[3]+"]"); 
   showLogMessage("I","","    原訂票已退票     筆數 ["+procCnt[4]+"]"); 
   showLogMessage("I","","    原訂票未扣點數   筆數 ["+procCnt[5]+"]");
   showLogMessage("I","","    請款金額小於退貨 筆數 ["+procCnt[6]+"]");
   showLogMessage("I","","    退票點數為 0     筆數 ["+procCnt[7]+"]");
   showLogMessage("I","","  退還紅利點數       筆數 ["+(procCnt[8]+procCnt[9])+"]"); 
   showLogMessage("I","","    免稅             筆數 ["+procCnt[8]+"]");
   showLogMessage("I","","    應稅             筆數 ["+procCnt[9]+"]");
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
 void  selectMktThsrDisc() throws Exception
 {
  selectSQL = "p_seqno,"
            + "trans_date,"
            + "trans_time,"
            + "trans_type,"
            + "id_p_seqno,"
            + "major_id_p_seqno,"
            + "acct_type,"
            + "card_no,"
            + "orig_trans_date,"
            + "orig_serial_no,"
            + "serial_no,"
            + "file_date,"
            + "card_mode,"
            + "discount_value,"
            + "rowid as rowid ";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE   proc_flag   = '1' "
            + "and     trans_type  = 'R' "
            ;

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   {
    totalCnt++;
/* 
    showLogMessage("I","","card_no   : ["+getValue("card_no")+"]");
    showLogMessage("I","","tran_date : ["+getValue("orig_trans_date")+"]");
    showLogMessage("I","","serial_no : ["+getValue("orig_serial_no")+"]");
*/ 

    if (getValue("card_mode").equals("1"))
       if (selectPtrWorkday()!=0) 
          {
           procCnt[0]++;    // 頂級卡需在關帳日處理
           continue;
          }

    if (selectMktThsrDisc1()!=0)
       {
        procCnt[2]++;
        setValue("error_code"  , "96");
        setValue("error_desc" , "無購票紀錄, 不處理");
        updateMktThsrDisc();
        continue;
       }

    if (!getValue("dis1.deduct_type").equals("1")) 
       {
        procCnt[1]++;   // 非扣除紅利點數
        continue;
       }

    if (getValue("dis1.proc_flag").equals("X"))
       {
        procCnt[2]++;
        setValue("error_code"  , "11");
        setValue("error_desc" , "原訂票請款檢核失敗");
        updateMktThsrDisc();
        continue;
       }

    if (getValue("dis1.proc_flag").equals("0"))
       {
        procCnt[3]++;
        setValue("error_code"  , "12");
        setValue("error_desc" , "原訂票請款未處理");
        updateMktThsrDisc();
        setValue("error_code"  , "X2");
        setValue("error_desc" , "同日退貨");
        updateMktThsrDiscA();
        continue;
       }

    if ((getValue("dis1.refund_flag").equals("Y"))&&
        (getValueInt("discount_value") + getValueInt("dis1.refund_bp_amt") >=
                                         getValueInt("dis1.deduct_bp_amt")))
       {
        procCnt[4]++;
        setValue("error_code"  , "13");
        setValue("error_desc" , "原訂票請款已退票");
        updateMktThsrDisc();
        continue;
       }

    if (getValueInt("dis1.deduct_bp")==0)
       {
        procCnt[5]++;
        setValue("error_code"  , "15");
        setValue("error_desc" , "原訂票請款扣紅利卻無扣點點數");
        updateMktThsrDisc();
        continue;
       }

    if (getValueInt("discount_value") > getValueInt("dis1.discount_value"))
       {
        procCnt[6]++;
        setValue("error_code"  , "17");
        setValue("error_desc" , "原訂票請款扣錢金額小於退貨金額");
        updateMktThsrDisc();
        continue;
       }

    setValueInt("net_deduct_bp" , (int)Math.round(                       // 已扣點
                                  (getValueInt("dis1.deduct_bp")+getValueInt("dis1.deduct_bp_tax"))
                                * getValueInt("discount_value")
                                / getValueInt("dis1.discount_value")
                                * 1.0));

    if (getValueInt("net_deduct_bp")<=0)
       {
        procCnt[7]++;
        setValue("error_code"  , "22");
        setValue("error_desc" , "無退票點數, 不退還");
        updateMktThsrDisc();
        continue;
       }
    setValueInt("deduct_bp" , (int)Math.round(                       // 免稅扣點
                              getValueInt("dis1.deduct_bp")
                            * getValueInt("discount_value")
                            / getValueInt("dis1.discount_value")
                            * 1.0));

    setValue("deduct_type","1");
    setValue("deduct_bp_amt" , getValue("discount_value"));

    if (getValueInt("dis1.deduct_bp") - getValueInt("dis1.refund_bp") < getValueInt("deduct_bp"))
        setValueInt("deduct_bp" , getValueInt("dis1.deduct_bp")
                                - getValueInt("dis1.refund_bp"));

    if ((!getValue("dis1.tran_seqno").equals(""))&&
        (selectMktBonusDtl()==0))
       {

        if (getValueInt("deduct_bp")+getValueInt("dis1.refund_bp")
                                    +getValueInt("dis1.refund_bp_tax") >getValueInt("mbd1.beg_tran_bp"))
            setValueInt("deduct_bp", getValueInt("mbd1.beg_tran_bp")*-1
                                   - getValueInt("dis1.refund_bp") 
                                   - getValueInt("dis1.refund_bp_tax")); 

        comb.bonusReverse(getValue("dis1.tran_seqno"));

        updateMktBonusDtl( getValueInt("mbd1.beg_tran_bp")    
                            + getValueInt("dis1.refund_bp")
                            + getValueInt("dis1.refund_bp_tax")
                            + getValueInt("deduct_bp"));

        setValueInt("bpid.effect_months" , 0);
        setValue("mbd2.mod_desc"        , "退票日期:"+getValue("trans_date")+" 流水號["+getValue("serial_no")+"]"); 
        setValue("mbd2.tax_flag"        , "N");
        setValueInt("mbd2.beg_tran_bp"  , getValueInt("deduct_bp"));
        setValueInt("mbd2.end_tran_bp"  , 0 );
        insertMktBonusDtl();

        setValueInt("deduct_bp_tax", 0);
       }
    else
       {

        setValueInt("deduct_bp_tax" , getValueInt("net_deduct_bp")
                                    - getValueInt("deduct_bp"));

        selectCycBpid();

        if (getValueInt("deduct_bp")>0)
           {
            procCnt[8]++;
            setValue("mbd2.mod_desc"        , "高鐵折扣票價差額退貨歸還(免稅)");
            setValue("mbd2.tax_flag"        , "N");
            setValueInt("mbd2.beg_tran_bp"  , getValueInt("deduct_bp"));
            setValueInt("mbd2.end_tran_bp"  , getValueInt("deduct_bp"));
            insertMktBonusDtl();
            comb.bonusFunc(tranSeqno);
           }
        if (getValueInt("deduct_bp_tax")>0)
           {
            procCnt[9]++;
            setValue("mbd2.mod_desc"        , "高鐵折扣票價差額退貨歸還(應稅)");
            setValue("mbd2.tax_flag"        , "Y");
            setValueInt("mbd2.beg_tran_bp"  , getValueInt("deduct_bp_tax"));
            setValueInt("mbd2.end_tran_bp"  , getValueInt("deduct_bp_tax"));
            insertMktBonusDtl();
            comb.bonusFunc(tranSeqno);
           }
        else
           {
            setValueInt("deduct_bp_tax", 0);
           }
       }

    updateMktThsrDisc1();
    updateMktThsrDisc2();

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 void updateMktThsrDisc() throws Exception
 {
  dateTime();

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
 void updateMktThsrDiscA() throws Exception
 {
  dateTime();

  updateSQL = "proc_flag  = 'X', "
            + "proc_date  = ?, "
            + "error_code = ?,"  
            + "error_desc  = ?,"  
            + "mod_pgm     = ?, "
            + "mod_time    = sysdate";   
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid = ?";

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("error_code"));
  setString(3 , getValue("error_desc"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("dis1.rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_disc_a error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 int insertMktBonusDtl() throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");
                                                     
  setValue("mbd2.tran_date"            , sysDate);
  setValue("mbd2.tran_time"            , sysTime);
  setValue("mbd2.tran_seqno"           , tranSeqno);
  setValue("mbd2.bonus_type"           , "BONU");
  setValue("mbd2.tran_code"            , "3");
  setValue("mbd2.mod_memo"             , "卡號["+getValue("card_no")+"]");
  setValue("mbd2.active_name"          , "高鐵標準車廂退票紅利點數退還");
  setValue("mbd2.acct_date"            , hBusiBusinessDate);
  setValue("mbd2.proc_month"           , hBusiBusinessDate.substring(0,6));
  setValue("mbd2.acct_type"            , getValue("acct_type"));
  setValue("mbd2.p_seqno"              , getValue("p_seqno"));
  setValue("mbd2.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("mbd2.tran_pgm"             , javaProgram);
  setValue("mbd2.effect_e_date"        , "");
  if (getValueInt("bpid.effect_months")>0)
     setValue("mbd2.effect_e_date"     , comm.nextMonthDate(getValue("mtu1.proc_date"),getValueInt("bpid.effect_months")));

  setValue("mbd2.apr_user"             , javaProgram);
  setValue("mbd2.apr_flag"             , "Y");
  setValue("mbd2.apr_date"             , sysDate);
  setValue("mbd2.crt_user"             , javaProgram);
  setValue("mbd2.crt_date"             , sysDate);
  setValue("mbd2.mod_user"             , javaProgram); 
  setValue("mbd2.mod_time"             , sysDate+sysTime);
  setValue("mbd2.mod_pgm"              , javaProgram);

  extendField = "mbd2.";
  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 void selectCycBpid() throws Exception
 {
  extendField = "bpid.";
  selectSQL = "effect_months";
  daoTable  = "cyc_bpid";
  whereStr  = "WHERE item_code  = '1' "
            + "and   bonus_type = 'BONU' "
            + "and   years      = ? "
            + "and   acct_type  = ? ";

  setString(1 , getValue("dis1.business_date").substring(0,4));
  setString(2 , getValue("acct_type"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) 
     setValueInt("bpid.effect_months" , 36);
 }
// ************************************************************************
 int  selectMktThsrDisc1() throws Exception
 {
  extendField = "dis1.";
  selectSQL = "proc_flag,"
            + "business_date,"
            + "trans_date,"
            + "refund_flag,"
            + "refund_bp,"
            + "refund_bp_tax,"
            + "deduct_type,"
            + "deduct_bp,"
            + "deduct_bp_tax,"
            + "discount_value,"
            + "refund_bp_amt,"
            + "deduct_bp_amt,"
            + "tran_seqno,"
            + "rowid as rowid";
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE  card_no     = ? "
            + "AND    trans_date  = ? "
            + "AND    serial_no   = ? "
            + "";

  setString(1,getValue("card_no"));
  setString(2,getValue("orig_trans_date")); 
  setString(3,getValue("orig_serial_no")); 

  selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_disc_1 error "+getValue("rowid")); 
      exitProgram(1); 
     }

  return(0);
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
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , hBusiBusinessDate);
  setString(2 , getValue("deduct_type"));
  setInt(3    , getValueInt("deduct_bp"));
  setInt(4    , getValueInt("deduct_bp_tax"));  
  setInt(5    , getValueInt("deduct_bp_amt"));  
  setString(6 , javaProgram);
  setRowId(7  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktThsrDisc1() throws Exception
 {
  dateTime();
  updateSQL = "refund_serial_no = ?,"
            + "refund_file_date = ?,"
            + "refund_date      = ?,"
            + "tran_seqno       = ?,"
            + "refund_flag      = 'Y',"
            + "refund_bp        = refund_bp  + ?,"
            + "refund_bp_tax    = refund_bp_tax + ?,"
            + "refund_bp_amt    = refund_bp_amt + ?,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_thsr_disc";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , getValue("serial_no"));
  setString(2 , getValue("file_date"));
  setString(3 , hBusiBusinessDate);
  setString(4 , tranSeqno);
  setInt(5    , getValueInt("deduct_bp"));
  setInt(6    , getValueInt("deduct_bp_tax"));
  setInt(7    , getValueInt("discount_value"));
  setString(8 , javaProgram);
  setRowId(9  , getValue("dis1.rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 int selectPtrWorkday() throws Exception
 {
  extendField = "wday.";
  selectSQL = "b.next_close_date";
  daoTable  = "act_acno a,ptr_workday b";
  whereStr  = "WHERE  a.stmt_cycle = b.stmt_cycle "
            + "AND    a.p_seqno    = ? "
            ;

  setString(1,getValue("p_seqno"));

  selectTable();

  if ( notFound.equals("Y")) 
     {
      showLogMessage("I","","p_seqno  : ["+getValue("p_seqno")+"] not found in act_acno");
      return(1);
     }

  if ((getValue("wday.next_close_date").equals(sysDate))||
      (getValue("wday.next_close_date").equals(hBusiBusinessDate))) return(0);

  return(1);
 }
// ************************************************************************ 
 int selectMktBonusDtl() throws Exception
 {
  extendField = "mbd1.";
  selectSQL = "beg_tran_bp";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE  tran_seqno = ? "
            ;

  setString(1,getValue("dis1.tran_seqno"));

  selectTable();

  if ( notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************ 
 void updateMktBonusDtl(int endTranBp) throws Exception
 {
  dateTime();
  updateSQL = "end_tran_bp      = ?,"
            + "mod_memo         = ?, "
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";  
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE tran_seqno = ? ";

  setInt(1    , endTranBp);
  setString(2 , "累計退貨 " + (getValueInt("mbd1.beg_tran_bp")-endTranBp)+" 點");
  setString(3 , javaProgram);
  setString(4 , getValue("dis1.tran_seqno"));

  updateTable();
  return;
 }
// ************************************************************************

}  // End of class FetchSample
