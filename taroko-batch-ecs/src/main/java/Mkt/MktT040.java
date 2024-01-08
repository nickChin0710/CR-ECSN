/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 108/11/11  V1.00.00  Allen Ho   new                                        *
* 109-12-11  V1.00.01  tanwei     updated for project coding standard        *
* 110/05/10  V1.00.12  Allen Ho   new                                        *
* 111/05/19  V1.01.01  Allen Ho   Mantis 9405                                *
* 111/12/07  V1.01.02  Zuwei      sync from mega                             *
* 112/09/27  V1.01.03  Zuwei Su   Insert【bil_sysexp】前增處理邏輯檢核符合   *
* 112/11/14  V1.01.04  Lai        檢核是否免費 or 需付費                     *
*                                                                            *
*****************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT040 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-退票退還費用處理程式 112/11/14 V1.01.04";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommDate commDate = new CommDate();

 int    DEBUG   = 0;
 int    DEBUG_F = 0;

 String hBusinessDate  = "";
 String tranSeqno     = "";
 String transSeqno    = "";
// String fileDate      = "";

 long    totalCnt=0;
 int[] procInt = new int[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT040 proc = new MktT040();
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
       showLogMessage("I","","本程式已有另依程式啟動中, 不執行..");
       return(0);
      }

   showLogMessage("I", "", " *** T040 Arg=[" + args.length + "]"+args[0]+","+args[1]);

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
      { hBusinessDate   = args[1]; }
   
   if ( !connectDataBase() ) 
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   showLogMessage("I",""," 序號 ["+ transSeqno +"]-[" + hBusinessDate +"]");

   selectPtrBusinday();

   showLogMessage("I","","=========================================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrUptxn();
   showLogMessage("I","","=========================================");
   showLogMessage("I","","處理筆數           ["+totalCnt+"] 筆");
   showLogMessage("I","","    無購票紀錄     ["+procInt[0]+"] 筆");
   showLogMessage("I","","    當日購退票     ["+procInt[1]+"] 筆");
   showLogMessage("I","","    尚未扣點加檔   ["+procInt[8]+"] 筆");
   showLogMessage("I","","    原請款已退票   ["+procInt[2]+"] 筆");
   showLogMessage("I","","    原請款檢核失敗 ["+procInt[3]+"] 筆");
   showLogMessage("I","","    原訂票未扣費用 ["+procInt[4]+"] 筆");
   showLogMessage("I","","    原請款為扣點   ["+procInt[5]+"] 筆");
   showLogMessage("I","","    原請款為減免   ["+procInt[6]+"] 筆");
   showLogMessage("I","","    尚未入帳刪除   ["+procInt[7]+"] 筆");
   showLogMessage("I","","    無加檔記錄     ["+procInt[8]+"] 筆");
   showLogMessage("I","","    退票金額為0    ["+procInt[9]+"] 筆");
   showLogMessage("I","","    退還費用(部分) ["+procInt[10]+"] 筆");
   showLogMessage("I","","    退還費用       ["+procInt[11]+"] 筆");
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

  if (hBusinessDate.length()==0)
      hBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusinessDate+"]");
 }
// ************************************************************************ 
 public void  selectMktThsrUptxn() throws Exception
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
  whereStr  = "WHERE proc_flag   = '1' "
            + "  and trans_type  = 'R' "
            + "  and trans_seqno = ? "
            + "  and file_date   = ? "
            ;

  setString(1 , transSeqno); 
  setString(2 , hBusinessDate);
   
  showLogMessage("I", "", "  OPEN =[" + transSeqno  + "]"+hBusinessDate);

  openCursor();

  showLogMessage("I", "", "  CLOSE ****** ");

  totalCnt = 0;
  while( fetchTable() ) 
   {
    totalCnt++;

    if(totalCnt % 1000 == 0 || totalCnt == 1)
       showLogMessage("I", "", String.format("Data Process T040 1 record=[%d]\n", totalCnt));

    setValue("pay_type"      ,"");
    setValue("upidno_seqno"  , "");
    setValue("deduct_amt"     , "0");
    setValue("error_code"    , "");
    setValue("error_desc"    , "");

    String cardNoMM = getValue("card_no");
    int rtn = selectMktThsrUptxn1();

if(DEBUG==1) showLogMessage("I", "", "   Read=[" + cardNoMM  + "]"+ getValue("mtu1.proc_flag")+","+getValue("mtu1.proc_flag")+",Type="+getValue("mtu1.pay_type") );

    if (rtn != 0)
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
        procInt[5]++; 
        continue;
       }
    else if (getValue("mtu1.pay_type").equals("2")) 
       {
        if (getValueInt("mtu1.deduct_amt")==0)
           {
            setValue("error_code"  , "16");
            setValue("error_desc" , "原訂票請款扣錢金額小於等於零");
            updateMktThsrUptxn1("X");
            procInt[4]++; 
            continue;
           }
       }
    else if (getValue("mtu1.pay_type").equals("3"))
       {
        updateMktThsrUpidnoMon();
    
        setValue("pay_type"      ,"3");
        setValue("upidno_seqno"  ,getValue("mut1.upidno_seqno"));
        updateMktThsrUptxn1("Y");
        setValue("refund_amt"   , "0");
        updateMktThsrUptxn3();
        procInt[6]++; 
        continue;
       }

    setValue("dest_amt" , "0");

if(DEBUG==1)    showLogMessage("I", "", "    Step selectBilSysexp " );

    if (selectBilSysexp()==0)
       {
        deleteBilSysexp();
        procInt[7]++;
       }
    else
       {
//      if (select_bil_bill()!=0)   // only test for debug
        if (selectActDebt()!=0)
           if (selectActDebtHst()!=0)
              {
               setValue("error_code"  , "21");
               setValue("error_desc" , "無加檔記錄, 不退還");
               updateMktThsrUptxn1("X");
               procInt[8]++;
               continue;
              }
        if (getValueInt("debt.d_avail_bal")==0)
           {
            setValue("error_code"  , "22");
            setValue("error_desc" , "退還金額已調為0, 不退還");
            updateMktThsrUptxn1("X");
            procInt[9]++;
            continue;
           }

        if (getValueInt("mtu1.deduct_amt")!= getValueInt("debt.d_avail_bal"))
           procInt[10]++;
        else
           procInt[11]++;

        setValueInt("dest_amt"        ,  getValueInt("debt.d_avail_bal"));
        setValueInt("mtu1.deduct_amt" ,  getValueInt("debt.d_avail_bal"));
        
        // 檢核【mkt_thsr_uptxn】a.card_no是否存在【MKT_THSR_UPGRADE_LIST】b.card_no
        String cardNo = getValue("card_no");
        int r = checkMktThsrUpgradeList(cardNo);
        if (r == 0) {
            continue;
        }

        insertBilSysexp();
       }
    setValue("pay_type"      , "2");
    setValue("deduct_amt"    , getValue("mtu1.deduct_amt")); 
    updateMktThsrUptxn1("Y");

    setValue("refund_amt"    , getValue("mtu1.deduct_amt"));
    updateMktThsrUptxn3();
    
    processDisplay(10000); // every 10000 display message
   } 
  closeCursor();
 }
 
 /**
  * 2.4.2.  不存在【MKT_THSR_UPGRADE_LIST】,就Insert 【bil_sysexp】
  * 2.4.3.  已存在:取得卡號及免費次數(b.card_no及b.free_tot_cnt),例:A卡免費3次
  * @param cardNo
  * @return 0 continue
  * @throws Exception
  */
private int checkMktThsrUpgradeList(String cardNo) throws Exception {
    sqlCmd = "select card_no, sum(free_tot_cnt) free_tot_cnt_sum "
            + "from MKT_THSR_UPGRADE_LIST "
            + "where use_month=?  " // -- business_date(yyyymm)
            + "and card_no = ? "
            + "group by card_no";
    setString(1, hBusinessDate.substring(0, 6));
    setString(2, cardNo);
    int cnt = selectTable();
if(DEBUG==1) showLogMessage("I","","    select checkMktThsrUpgradeList Cnt="+cnt+","+cardNo+","+hBusinessDate.substring(0, 6)+",F_cnt="+getValueInt("free_tot_cnt_sum"));

    // 不存在【MKT_THSR_UPGRADE_LIST】,就Insert.  
    if (cnt == 0) {
        return 1;
    }
    int freeTotCntSum = getValueInt("free_tot_cnt_sum");
    sqlCmd = "select sum(decode(trans_type,'R',-1,1)) cnt "
            + "from mkt_thsr_uptxn "
            + "where error_code='00' "
            + "and file_date= ? " //  -- business_date
            + "and trans_date= ? " //  -- business_date-1天
            + "and card_no =? ";
    String lastDate = commDate.dateAdd(hBusinessDate, 0, 0, -1);
    setString(1, hBusinessDate);
    setString(2, lastDate);
    setString(3, cardNo);
    cnt = selectTable();
    
if(DEBUG==1) showLogMessage("I","","    select mkt_thsr_uptxn 2 Cnt="+cnt+","+getValue("card_no")+","+lastDate+",count="+getValueInt("cnt")+","+freeTotCntSum);

    // 筆數大於>freeTotCntSum筆要Insert
    if (cnt > 0 && getValueInt("cnt") > freeTotCntSum) {
        return 1;
    }
    
    return 0;
}

// ************************************************************************
 int selectMktThsrUptxn1() throws Exception
 {
  extendField = "mtu1.";
  selectSQL = "proc_flag, "
            + "proc_date, "
            + "trans_date, "
            + "pay_type, "
            + "deduct_amt, "
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

if(DEBUG==1) showLogMessage("I","","    select mkt_thsr_uptxn Cnt="+recCnt+","+getValue("card_no")+","+getValue("pay_type") );

  if (recCnt==0) return(1);

  return(0);
 }
// ************************************************************************ 
 void updateMktThsrUptxn1(String procFlag) throws Exception
 {
if(DEBUG==1) showLogMessage("I","","    updateMktThsrUptxn1 proc_flag="+procFlag);

  updateSQL = "proc_flag     = ?, "
            + "proc_date     = ?, "
            + "pay_type      = ?, "
            + "deduct_amt    = ?, "
            + "error_code    = ?, "
            + "error_desc    = ?, "
            + "upidno_seqno  = ?, "
            + "card_mode     = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , procFlag);
  setString(2 , hBusinessDate);
  setString(3 , getValue("pay_type"));
  setInt(4    , getValueInt("deduct_amt"));
  setString(5 , getValue("error_code"));
  setString(6 , getValue("error_desc"));
  setString(7 , getValue("upidno_seqno"));
  setString(8 , getValue("mtu1.card_mode"));
  setString(9 , javaProgram);
  setRowId(10 , getValue("rowid"));

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
if(DEBUG==1) showLogMessage("I","","    updateMktThsrUptxn2 proc_flag="+procFlag);

  updateSQL = "proc_flag   = ?, "
            + "proc_date   = ?, "
            + "refund_flag = 'Y',"
            + "error_code    = ?, "
            + "error_desc    = ?, "
            + "mod_pgm     = ?, "
            + "mod_time    = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , procFlag);
  setString(2 , hBusinessDate);
  setString(3 , getValue("error_code"));
  setString(4 , getValue("error_desc"));
  setString(5 , javaProgram);
  setRowId(6  , getValue("mtu1.rowid"));

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
if(DEBUG==1) showLogMessage("I","","    updateMktThsrUptxn3="+"Y");

  updateSQL = "proc_flag        = 'Y', "
            + "proc_date        = ?, "
            + "refund_serial_no = ?,"
            + "refund_file_date = ?,"
            + "refund_date      = ?,"
            + "refund_flag      = 'Y',"
            + "refund_amt       = ?,"
            + "refund_bp        = 0,"
            + "refund_bp_tax    = 0,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , hBusinessDate);
  setString(2 , getValue("serial_no"));
  setString(3 , getValue("file_date"));
  setString(4 , getValue("trans_date"));
  setInt(5    , getValueInt("refund_amt"));
  setString(6 , javaProgram);
  setRowId(7  , getValue("mtu1.rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_3 error "+getValue("mtu1.rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 int selectBilSysexp() throws Exception
 {
  extendField = "sys1.";
  selectSQL = "dest_amt,"
            + "rowid as rowid";
  daoTable  = "bil_sysexp";
  whereStr  = "where purchase_date  = ? "
            + "and   bill_type      = 'INHU' "
            + "and   txn_code       = '05' "
            + "and   card_no        = ? "
            + "and   ref_key        = ? "
            + "and   dest_amt       = ? "
            + "and   post_flag     != 'Y' "
            + "and   curr_code      = '901' ";

  setString(1, getValue("org_trans_date"));
  setString(2, getValue("card_no"));
  setString(3, getValue("org_serial_no"));
  setInt(4   , getValueInt("mtu1.deduct_amt"));

  selectTable();

  if ( notFound.equals("Y")) return(1);

  return(0);
 }
// ************************************************************************
 int deleteBilSysexp() throws Exception
 {
  daoTable  = "bil_sysexp";
  whereStr  = "WHERE rowid    = ? ";

  setRowId(1  , getValue("sys1.rowid"));

  deleteTable();

  return(0);
 }
// ************************************************************************
 int selectBilBill() throws Exception
 {
  extendField = "debt.";
  selectSQL = "reference_no,"
            + "dest_amt as beg_bal,"
            + "dest_amt as end_bal,"
            + "dest_amt as d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "bil_bill";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHU' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   dest_amt      = ? ";

  setString(1, getValue("p_seqno"));
  setString(2, getValue("card_no"));
  setString(3, getValue("mtu1.trans_date"));
  setInt(4   , getValueInt("mtu1.deduct_amt"));

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 int selectActDebt() throws Exception
 {
  extendField = "debt.";
  selectSQL = "reference_no,"
            + "beg_bal,"
            + "end_bal,"
            + "d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "act_debt";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHU' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   beg_bal       = ? ";

  setString(1, getValue("p_seqno"));
  setString(2, getValue("card_no"));
  setString(3, getValue("mtu1.trans_date"));
  setInt(4   , getValueInt("mtu1.deduct_amt"));
/*
  showLogMessage("I","","STEP 0 : ["+ getValue("p_seqno")            + "]");
  showLogMessage("I","","STEP 1 : ["+ getValue("card_no")            + "]");
  showLogMessage("I","","STEP 2 : ["+ getValue("mtu1.trans_date")    + "]");
  showLogMessage("I","","STEP 3 : ["+ getValueInt("mtu1.deduct_amt") + "]");
  showLogMessage("I","","STEP 4 : ["+ getValueInt("mtu1.deduct_amt") + "]");
*/

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  return(0);
 }
// ************************************************************************
 int selectActDebtHst() throws Exception
 {
  extendField = "debt.";
  selectSQL = "reference_no,"
            + "beg_bal,"
            + "end_bal,"
            + "d_avail_bal,"
            + "acct_code,"
            + "interest_date";
  daoTable  = "act_debt_hst";
  whereStr  = "where p_seqno       = ? "
            + "and   bill_type     = 'INHU' "
            + "and   txn_code      = '05' "
            + "and   card_no       = ? "
            + "and   purchase_date = ? "
            + "and   beg_bal       = ? ";

  setString(1, getValue("p_seqno"));
  setString(2, getValue("card_no"));
  setString(3, getValue("mtu1.trans_date"));
  setInt(4   , getValueInt("mtu1.deduct_amt"));

  int recCnt = selectTable();

  if ( notFound.equals("Y")) return(1);
  showLogMessage("I","","STEP 5 : ["+ getValueInt("debt.end_bal") + "]");
  showLogMessage("I","","STEP 5 : ["+ getValueInt("debt.d_avail_bal") + "]");
  return(0);
 }
// ************************************************************************
 int insertBilSysexp() throws Exception
 {
  dateTime();
  extendField = "sysp.";
  setValue("sysp.card_no"            , getValue("card_no"));
  setValue("sysp.p_seqno"            , getValue("p_seqno"));
  setValue("sysp.acct_type"          , getValue("acct_type"));
  setValue("sysp.bill_type"          , "INHU");
  setValue("sysp.txn_code"           , "06");
  setValue("sysp.purchase_date"      , hBusinessDate);
  setValue("sysp.src_amt"            , getValue("dest_amt"));
  setValue("sysp.dest_amt"           , getValue("dest_amt"));
  setValue("sysp.dc_dest_amt"        , getValue("dest_amt"));
  setValue("sysp.dest_curr"          , "901");
  setValue("sysp.curr_code"          , "901");
  setValue("sysp.bill_desc"          , "退還高鐵升等扣款金額(退票)");
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
 int  updateMktThsrUpidnoMon() throws Exception
 {
  updateSQL = "refund_flag   = 'Y',"
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";   
  daoTable  = "mkt_thsr_upidno_mon";
  whereStr  = "WHERE serial_no    = ? "
            ;

  setString(1 , getValue("org_serial_no"));

  updateTable();
  if ( notFound.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

