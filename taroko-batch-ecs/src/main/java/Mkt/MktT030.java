/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 109/04/10   V1.00.00    Allen Ho   mkt_t030                                *
 * 109-12-11   V1.00.01    tanwei      updated for project coding standard    *
 * 110/09/17  V1.01.01  Allen Ho   for bonus_reverse mod_pgm                  *
 * 110/09/23  V1.01.02  Allen Ho   dispFlag DEBUG                             *
 * 111/12/07  V1.01.03  Zuwei      sync from mega                             *
 ******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT030 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-退票退紅利點數處理程式 111/12/07 V1.01.03";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate  = "";
 String fileDate      = "";
 String transSeqno    = "";
 String tranSeqno     = "";

 long    totalCnt=0;
 int   dataCnt=0;
 int   parmCnt=0;
 int[] procInt = new int[10];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT030 proc = new MktT030();
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

   if (args.length > 2)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [trans_seqno]");
       showLogMessage("I","","PARM 2 : [file_date]");
       return(1);
      }

   if ( args.length >= 1 )
      { transSeqno = args[0]; }

   if ( args.length >= 2 )
      { fileDate   = args[1]; }
   
   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());
   comb.dispFlag="Y";
         
   showLogMessage("I",""," 序號 ["+ transSeqno +"]-[" + fileDate +"]");

   selectPtrBusinday();

   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrUptxn();
   showLogMessage("I","","處理筆數           ["+totalCnt+"] 筆");
   showLogMessage("I","","    無購票紀錄     ["+procInt[0]+"] 筆");
   showLogMessage("I","","    當日購退票     ["+procInt[1]+"] 筆");
   showLogMessage("I","","    尚未扣點加檔   ["+procInt[8]+"] 筆");
   showLogMessage("I","","    原請款已退票   ["+procInt[2]+"] 筆");
   showLogMessage("I","","    原請款檢核失敗 ["+procInt[3]+"] 筆");
   showLogMessage("I","","    原請款無扣點   ["+procInt[4]+"] 筆");
   showLogMessage("I","","    原請款為加檔   ["+procInt[5]+"] 筆");
   showLogMessage("I","","    原請款為減免   ["+procInt[6]+"] 筆");
   showLogMessage("I","","    退還點數       ["+procInt[7]+"] 筆");
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
  selectSQL = "p_seqno, "
            + "acct_type, "
            + "major_id_p_seqno, "
            + "id_p_seqno, "
            + "card_no, "
            + "group_code, "
            + "card_type, "
            + "trans_date, "
            + "org_trans_date, "
            + "org_serial_no, "
            + "serial_no, "
            + "file_date, "
            + "rowid as rowid ";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE   proc_flag   = '1' "
            + "and     trans_type  = 'R' "
            + "and     trans_seqno = ? "
            + "and     file_date   = ? "
            ;

  setString(1 , transSeqno); 
  setString(2 , fileDate); 

  openCursor();
  totalCnt = 0;
  int okFlag = 0;
  while( fetchTable() ) 
   {
    totalCnt++;

    setValue("pay_type"      ,"");
    setValue("upidno_seqno"  , "");
    setValue("deduct_bp"     , "0");
    setValue("deduct_bp_tax" , "0");
    setValue("error_code"    , "");
    setValue("error_desc"    , "");

    if (selectMktThsrUptxn1()!=0)
       {
        setValue("error_code"  , "96");
        setValue("error_desc" , "無購票紀錄, 不處理");
        updateMktThsrUptxn1("X");
        procInt[0]++; 
        continue;
       }

    if (getValue("mtu1.proc_flag").equals("1"))
       {
        setValue("error_code"  , "12");
        setValue("error_desc" , "當日購退票");
        updateMktThsrUptxn1("Y");
        updateMktThsrUptxn2("Y");
        procInt[1]++; 
        continue;
       }

    if (getValue("mtu1.proc_flag").equals("X"))
       {
        setValue("error_code"  , "80");
        setValue("error_desc" , "原訂票請款檢核失敗");
        procInt[3]++; 
        updateMktThsrUptxn1("X");
        continue;
       }
    if ((getValue("mtu1.proc_flag").equals("Y"))&&
        (getValue("mtu1.refund_flag").equals("Y")))
       {
        setValue("error_code"  , "81");
        setValue("error_desc" , "原訂票請款已退票");
        updateMktThsrUptxn1("X");
        procInt[2]++; 
        continue;
       }

    if (getValue("mtu1.pay_type").length()==0)  
       {
        setValue("error_code"  , "14");
        setValue("error_desc" , "退票時尚未扣點加檔");
        updateMktThsrUptxn1("Y");
        updateMktThsrUptxn2("Y");
        procInt[8]++; 
        continue;
       }

    if (getValue("mtu1.pay_type").equals("1"))
       { 
        if (getValueInt("mtu1.deduct_bp")+getValueInt("mtu1.deduct_bp_tax")==0)
           {
            setValue("error_code"  , "15");
            setValue("error_desc" , "原訂票請款扣紅利卻無扣點數");
            updateMktThsrUptxn1("X");
            procInt[4]++; 
            continue;
           }
       }
    else if (getValue("mtu1.pay_type").equals("2")) 
       {
        procInt[5]++;
        continue;
       }
    else if (getValue("mtu1.pay_type").equals("3"))
       {
        updateMktThsrUpidnoMon();
    
        setValue("pay_type"      ,"3");
        setValue("upidno_seqno"  ,getValue("mut1.upidno_seqno"));
        updateMktThsrUptxn1("Y");
        setValue("refund_bp"     , "0");
        setValue("refund_bp_tax" , "0");
        updateMktThsrUptxn3();
        procInt[6]++; 
        continue;
       }
   
    okFlag = 0;
    if (selectCycBnData()==0)
       {
        for (int inti=0;inti<dataCnt;inti++)
          { 
           if (getValue("data.data_code2",inti).length()==0)
              {
               okFlag=1;
               break;
              }
           else
              {
               if (getValue("data.data_code2",inti).equals(getValue("card_type")))
                  {
                   okFlag=1;
                   break;
                  }
              }
          }
       }

    if (okFlag==0) selectCycBpid();
    else setValue("bpid.effect_months" , "0");
   
    if ((!getValue("mtu1.tran_seqno").equals(""))&&
        (selectMktBonusDtl()==0))
       {
        comb.dispFlag = "Y";
        comb.modPgm = javaProgram;
        comb.bonusReverse(getValue("mtu1.tran_seqno"));
        if (comb.dispFlag.equals("Y")) comb.dispFlag="N";

        updateMktBonusDtl(0);

        setValueInt("bpid.effect_months" , 0);
        setValue("mbdl.mod_desc"        , "退票日期:"+getValue("trans_date")+" 流水號["+getValue("serial_no")+"]"); 
        setValue("mbdl.tax_flag"        , "N");
        setValueInt("mbdl.beg_tran_bp"  , getValueInt("mbd2.beg_tran_bp")*-1);
        setValueInt("mbdl.end_tran_bp"  , 0 );
        insertMktBonusDtl();

        setValueInt("deduct_bp_tax", 0);
       }
    else
       {
        if (getValueInt("mtu1.deduct_bp") > 0)
           {
            setValue("mbdl.mod_desc"     , "退票退紅利點數(免稅)");
            setValue("mbdl.tax_flag"     , "N");
            setValue("mbdl.beg_tran_bp"  , getValue("mtu1.deduct_bp"));
            setValue("mbdl.end_tran_bp"  , getValue("mtu1.deduct_bp"));
            insertMktBonusDtl();

            comb.modPgm = javaProgram;
            comb.bonusFunc(tranSeqno);
            if (comb.dispFlag.equals("Y")) comb.dispFlag="N";
           }

        if (getValueInt("mtu1.deduct_bp_tax") > 0)
           {
            setValue("mbdl.mod_desc"     , "退票退紅利點數(應稅)");
            setValue("mbdl.tax_flag"     , "Y");
            setValue("mbdl.beg_tran_bp"  , getValue("mtu1.deduct_bp_tax"));
            setValue("mbdl.end_tran_bp"  , getValue("mtu1.deduct_bp_tax"));
            insertMktBonusDtl();

            comb.modPgm = javaProgram;
            comb.bonusFunc(tranSeqno);
            if (comb.dispFlag.equals("Y")) comb.dispFlag="N";
           }
       }
    setValue("pay_type"      , "1");
    setValue("deduct_bp"     , getValue("mtu1.deduct_bp")); 
    setValue("deduct_bp_tax" , getValue("mtu1.deduct_bp_tax")); 
    updateMktThsrUptxn1("Y");

    setValue("refund_bp"     , getValue("mtu1.deduct_bp"));
    setValue("refund_bp_tax" , getValue("mtu1.deduct_bp_tax"));
    updateMktThsrUptxn3();
    procInt[7]++; 

    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int selectMktThsrUptxn1() throws Exception
 {
  extendField = "mtu1.";
  selectSQL = "proc_flag, "
            + "proc_date, "
            + "pay_type, "
            + "deduct_bp, "
            + "deduct_bp_tax, "
            + "tran_seqno, "
            + "card_mode, "
            + "error_code, "
            + "refund_flag, "
            + "upidno_seqno, "
            + "rowid as rowid ";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE  card_no     = ? "
            + "AND    trans_date  = ? "
            + "AND    serial_no   = ? "
            + "";

  setString(1,getValue("card_no"));
  setString(2,getValue("org_trans_date")); 
  setString(3,getValue("org_serial_no")); 

  int recCnt = selectTable();

  if (recCnt==0) return(1);

  return(0);
 }
// ************************************************************************ 
 void updateMktThsrUptxn1(String procFlag) throws Exception
 {
  updateSQL = "proc_flag     = ?, "
            + "proc_date     = ?, "
            + "pay_type      = ?, "
            + "deduct_bp     = ?, "
            + "deduct_bp_tax = ?, "
            + "error_code    = ?, "
            + "error_desc    = ?, "
            + "upidno_seqno  = ?, "
            + "card_mode     = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , procFlag);
  setString(2 , businessDate);
  setString(3 , getValue("pay_type"));
  setInt(4    , getValueInt("deduct_bp"));
  setInt(5    , getValueInt("deduct_bp_tax"));
  setString(6 , getValue("error_code"));
  setString(7 , getValue("error_desc"));
  setString(8 , getValue("upidno_seqno"));
  setString(9 , getValue("mtu1.card_mode"));
  setString(10, javaProgram);
  setRowId(11 , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_1 error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void updateMktThsrUptxn2(String procFlag) throws Exception
 {
  updateSQL = "proc_flag   = ?, "
            + "proc_date   = ?, "
            + "refund_flag = 'Y',"
            + "refund_date = ?,"
            + "error_code  = ?, "
            + "error_desc  = ?, "
            + "mod_pgm     = ?, "
            + "mod_time    = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , procFlag);
  setString(2 , businessDate);
  setString(3 , getValue("trans_date"));
  setString(4 , getValue("error_code"));
  setString(5 , getValue("error_desc"));
  setString(6 , javaProgram);
  setRowId(7  , getValue("mtu1.rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_2 error "+getValue("mtu1.rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void updateMktThsrUptxn3() throws Exception
 {
  updateSQL = "proc_flag        = 'Y', "
            + "proc_date        = ?, "
            + "refund_serial_no = ?,"
            + "refund_file_date = ?,"
            + "tran_seqno       = ?,"
            + "refund_date      = ?,"
            + "refund_flag      = 'Y',"
            + "refund_bp        = ?,"
            + "refund_bp_tax    = ?,"
            + "refund_amt       = 0,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , businessDate);
  setString(2 , getValue("serial_no"));
  setString(3 , getValue("file_date"));
  setString(4 , tranSeqno);
  setString(5 , businessDate);
  setInt(6    , getValueInt("refund_bp"));
  setInt(7    , getValueInt("refund_bp_tax"));
  setString(8 , javaProgram);
  setRowId(9  , getValue("mtu1.rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_3 error "+getValue("mtu1.rowid")); 
      exitProgram(1); 
     }
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
  setValue("mbdl.bonus_type"           , "BONU");
  setValue("mbdl.tran_code"            , "4");
  setValue("mbdl.mod_memo"             , "卡號["+getValue("card_no")+"]");
  if (getValue("mtu1.tran_seqno").length()>0)
     setValue("mbdl.mod_memo"          , getValue("mbdl.mod_memo")
                                       + " 原交易序號["+ getValue("mtu1.tran_seqno") +"]");
  setValue("mbdl.active_name"          , "高鐵車廂升等紅利退票退還點數");
  setValue("mbdl.acct_date"            , businessDate);
//setValue("mbdl.proc_month"           , business_date.substring(0,6));
  setValue("mbdl.acct_type"            , getValue("acct_type"));
  setValue("mbdl.p_seqno"              , getValue("p_seqno"));
  setValue("mbdl.id_p_seqno"           , getValue("major_id_p_seqno"));
  setValue("mbdl.tran_pgm"             , javaProgram);
  setValue("mbdl.effect_e_date"        , "");
  if (getValueInt("bpid.effect_months")>0)
     setValue("mbdl.effect_e_date"     , comm.nextMonthDate(getValue("mtu1.proc_date"),getValueInt("bpid.effect_months")));
  setValue("mbdl.apr_user"             , javaProgram);
  setValue("mbdl.apr_flag"             , "Y");
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
 void selectCycBpid() throws Exception
 {
  extendField = "bpid.";
  selectSQL = "effect_months";
  daoTable  = "cyc_bpid";
  whereStr  = "WHERE item_code  = '1' "
            + "and   bonus_type = 'BONU' "
            + "and   years      = ? "
            + "and   acct_type  = ? ";

  setString(1,getValue("mtu1.proc_date").substring(0,4));
  setString(2,getValue("acct_type"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) 
     setValueInt("bpid.effect_months" , 36);
 }
// ************************************************************************
 int selectCycBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_code2";
  daoTable  = "cyc_bn_data";
  whereStr  = "WHERE table_name = 'CYC_BPID' "
            + "and   data_type  = '2' "
            + "and   data_key   = ? "
            + "and   data_code  = ? "
            ;

  setString(1,getValue("mtu1.proc_date").substring(0,4)+"BONU"+getValue("acct_type")+"1");
  setString(2,getValue("group_code"));

  dataCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int  updateMktThsrUpidnoMon() throws Exception
 {
  updateSQL = "refund_flag   = 'Y',"
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";   
  daoTable  = "mkt_thsr_upidno_mon";
  whereStr  = "WHERE serial_no    = ? "
            ;

  setString(1 , javaProgram);
  setString(2 , getValue("org_serial_no"));

  updateTable();
  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int selectMktBonusDtl() throws Exception
 {
  extendField = "mbd2.";
  selectSQL = "beg_tran_bp";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE  tran_seqno = ? "
            ;

  setString(1,getValue("mtu1.tran_seqno"));

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
  setString(4 , getValue("mtu1.tran_seqno"));

  updateTable();
  return;
 }
// ************************************************************************

}  // End of class FetchSample
