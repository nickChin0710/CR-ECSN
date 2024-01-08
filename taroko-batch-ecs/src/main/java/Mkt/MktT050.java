/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/04/10  V1.00.00  Allen Ho   mkt_T050                                   *
* 109-12-11  V1.00.0   tanwei     updated for project coding standard        *
* 110/08/07  V1.00.20  Allen Ho   mkt_T050                                   *
* 110/10/14  V1.01.02  Allen Ho   before file_date still o_proc              *
* 110/11/30  V1.02.01  Allen Ho   Mantis 9096                                *
* 111/12/07  V1.02.02  Zuwei      sync from mega                             *
* 112/09/27  V1.02.03  Zuwei Su   Insert【bil_sysexp】前增處理邏輯檢核符合   *
* 112/11/14  V1.02.04  Lai        檢核是否免費 or 需付費                     *
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
public class MktT050 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-紅利點數扣點或加檔款處理程式 112/11/14 V1.02.04";
 CommFunction comm = new CommFunction();
 CommRoutine  comr = null;
 CommBonus    comb = null;
 CommDate commDate = new CommDate();

 int    DEBUG   = 0;
 int    DEBUG_F = 0;

 String hBusinessDate  = "";
 String tranSeqno     = "";
 String transSeqno     = "";
// String fileDate       = "";

 long    totalCnt=0;
 int    cnt1=0,parmCnt=0,branchCnt=0;
 int[]  procInt = new int[10];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT050 proc = new MktT050();
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

   showLogMessage("I", "", " *** T050 Arg=[" + args.length + "]"+args[0]+","+args[1]);

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
   comb = new CommBonus(getDBconnect(),getDBalias());
   comb.dispFlag="Y";

   showLogMessage("I",""," 序號 ["+ transSeqno +"]-[" + hBusinessDate +"]");

   selectPtrBusinday();

   if (selectMktThsrUpmode()!=0)
      {
       showLogMessage("I","","mkt_thsr_upmode 參數 , 未設紅利扣點不執行..");
       return(0);
      }

   showLogMessage("I","","===============================");
   showLogMessage("I","","載入暫存資料.....");
   loadCrdIdno();
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrUptxn();
   showLogMessage("I","","累計處理 ["+totalCnt+"] 筆");
   showLogMessage("I","","    享有減免 ["+procInt[0]+"] 筆");
   showLogMessage("I","","    紅利扣點 ["+procInt[2]+"] 筆");
   showLogMessage("I","","    點數不足 ["+procInt[1]+"] 筆 (1日扣款者)");
   showLogMessage("I","","    不足扣款 ["+procInt[4]+"] 筆");
   showLogMessage("I","","    不足扣款 ["+procInt[3]+"] 筆 (超額)");
   showLogMessage("I","","    同日加退 ["+procInt[5]+"] 筆");
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

  if (hBusinessDate.length()==0)
      hBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusinessDate+"]");
 }
// ************************************************************************ 
 void  selectMktThsrUptxn() throws Exception
 {
  selectSQL = "p_seqno, "
            + "serial_no, "
            + "acct_type, "
            + "id_p_seqno,"
            + "major_id_p_seqno,"
            + "card_no, "
            + "card_mode, "
            + "trans_date, "
            + "serial_no, "
            + "rowid as rowid ";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE proc_flag    = '1' "  
            + "and   trans_type  != 'R' "
            + "and   ((trans_seqno = ? "
            + "  and   file_date   = ? )"
            + " or    (file_date   like ? ))" 
            + "order   by file_date,p_seqno,trans_date,trans_time";
            ;

  setString(1 , transSeqno); 
  setString(2 , hBusinessDate); 
  setString(3 , hBusinessDate.substring(0,6)+"%"); 

  showLogMessage("I", "", "  OPEN =[" + transSeqno  + "]"+hBusinessDate);
 
  openCursor();

  showLogMessage("I", "", "  CLOSE ****** ");

  int lastCnt = 0;
  int lastamtCnt = 0;
  int existFlag=0;
  totalCnt=0;
  int modeCnt=0;
  int okFlag=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

   if(totalCnt % 1000 == 0 || totalCnt == 1)
       showLogMessage("I", "", String.format("Data Process T050 1 record=[%d]\n", totalCnt));

    String cardNoMM = getValue("card_no");
    int rtn = selectMktThsrUptxn2();

if(DEBUG==1) showLogMessage("I","","   Read=[" + cardNoMM  + "]");

    if (rtn == 0)
       {
        setValue("error_code"  , "12");
        setValue("error_desc" , "當日購退票");
        updateMktThsrUptxn2a("Y");
        updateMktThsrUptxn2b("Y");
        procInt[5]++; 
        continue;
       }

    okFlag =0;
    for (int inti=0;inti<parmCnt;inti++)
      {
       if (getValue("mode.card_mode",inti).equals(getValue("card_mode")))
          {
           modeCnt= inti;
           okFlag = 1;
           break;
          }
      }
    if (okFlag==0) 
       {
        showLogMessage("I","","卡類錯誤  ["+getValue("card_mode")+"]");
        continue;
       }

    setValue("idno.id_p_seqno",getValue("id_p_seqno"));
    cnt1 = getLoadData("idno.id_p_seqno");
    if (cnt1==0)
       {
        showLogMessage("I","","id_p_seqno : ["+ getValue("id_p_seqno") +"] not found in crd_idno");
        continue;
       }

    if (selectMktThsrUpidno()==0)
       {
        existFlag=0;
        for (int intm=0;intm<branchCnt;intm++)
          {
           if (selectMktThsrUpidnoMon(intm) <=0) continue;
           insertMktThsrUpidnoMon(intm);
           setValue("upidno_type" , "1");
           updateMktThsrUptxn2();
           procInt[0]++;
           existFlag=1;
           break;
          }
        if (existFlag==1) continue;
       }

    if (!getValue("major_id_p_seqno").equals(getValue("id_p_seqno")))
       {
        setValue("idno.id_p_seqno",getValue("major_id_p_seqno"));
        cnt1 = getLoadData("idno.id_p_seqno");
        if (cnt1==0)
           {
            showLogMessage("I","","id_p_seqno : ["+ getValue("major_id_p_seqno") +"] not found in crd_idno");
            continue;
           }

       if (selectMktThsrUpidno()==0)
          {
           existFlag=0;
           for (int intm=0;intm<branchCnt;intm++)
             {
              if (selectMktThsrUpidnoMon(intm) <=0) continue;
              insertMktThsrUpidnoMon(intm);
              setValue("upidno_type" , "2");
              updateMktThsrUptxn2();
              procInt[0]++;
              existFlag=1;
              break;
             }
           if (existFlag==1) continue;
          }
       }

    lastCnt    = selectMktThsrUptxn1("1");
    if ((getValue("mode.ticket_pnt_cond",modeCnt).equals("Y"))&& 
        (lastCnt<getValueInt("mode.ticket_pnt_cnt",modeCnt))&&
        (comb.bonusSum(getValue("p_seqno"))>=getValueInt("mode.ticket_pnt",modeCnt)))
       {
        if (comb.dispFlag.equals("Y")) comb.dispFlag="N";

        setValue("mbdl.mod_desc"   , "目前紅利剩餘可扣張數:"
                                   + (getValueInt("mode.ticket_pnt_cnt",modeCnt)-lastCnt-1));
        setValue("mbdl.tax_flag"  , "N");
        setValueInt("mbdl.beg_tran_bp"  , getValueInt("mode.ticket_pnt",modeCnt)*-1);
        setValueInt("mbdl.end_tran_bp"  , getValueInt("mode.ticket_pnt",modeCnt)*-1);
        insertMktBonusDtl();

        comb.tranBpNotax = 0;
        comb.tranBpTax   = 0;
        comb.bonusFunc(tranSeqno);

        comb.modPgm = javaProgram;
        updateMktThsrUptxnA();
        procInt[2]++;
        continue;
       }

    if (getValue("mode.add_file_flag",modeCnt).equals("Y"))
       {
        updateMktThsrUptxn1();
        procInt[1]++;
        continue;
       }

    lastamtCnt = selectMktThsrUptxn1("2");
    if ((getValue("mode.ticket_amt_cond",modeCnt).equals("Y"))&& 
        (lastamtCnt<getValueInt("mode.ticket_amt_cnt",modeCnt)))
       {
        setValue("sysp.dest_amt" , getValue("mode.ticket_amt"));
        procInt[4]++;
       }
    else
       {
        setValue("sysp.dest_amt" , getValue("mode.ex_ticket_amt"));
        procInt[3]++;  
       }
    
    // 檢核【mkt_thsr_uptxn】a.card_no是否存在【MKT_THSR_UPGRADE_LIST】b.card_no
    String cardNo = getValue("card_no");
    int r = checkMktThsrUpgradeList(cardNo);
    if (r == 0) {
        updateMktThsrUptxnB("4"); 
        continue;
    }

    insertBilSysexp(cardNo);
    updateMktThsrUptxnB("2");

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
    // 不存在【MKT_THSR_UPGRADE_LIST】,就Insert.  
if(DEBUG==1) showLogMessage("I","","    select checkMktThsrUpgradeList" +cardNo+","+cnt);

    if (cnt == 0) {
        return 1;
    }
    int freeTotCntSum = getValueInt("free_tot_cnt_sum");
    sqlCmd = "select sum(decode(trans_type,'R',-1,1)) cnt  "
           + " from mkt_thsr_uptxn "
           + "where error_code ='00' "
           + "  and file_date  = ? " //  -- business_date
           + "  and trans_date = ? " //  -- business_date-1天
           + "  and card_no    = ? ";
    String lastDate = commDate.dateAdd(hBusinessDate, 0, 0, -1);
    setString(1, hBusinessDate);
    setString(2, lastDate);
    setString(3, cardNo);
    cnt = selectTable();
    
    sqlCmd = "select sum(decode(pay_type  ,'4', 1,0)) cnt1 "
           + "  from mkt_thsr_uptxn "
           + " where error_code ='00' "
           + "   and substr(trans_date,1,6) = ? " //  -- business_date 同月
           + "   and card_no    = ? ";
    setString(1, hBusinessDate.substring(0,6));
    setString(2, cardNo);
    cnt = selectTable();
    
if(DEBUG==1) showLogMessage("I","","    select mkt_thsr_uptxn 2 Cnt="+cnt+","+cardNo+","+lastDate+",count="+getValueInt("cnt")+","+getValueInt("cnt1")+","+freeTotCntSum);

    // return 0 不要Insert(pay_type:4)   1:要Insert(pay_type:2)
  //if ( getValueInt("cnt")+getValueInt("cnt1") > freeTotCntSum) {
    if ( getValueInt("cnt1") >= freeTotCntSum) {
        return 1;
    }
    
    return 0;
}

// ************************************************************************
 int insertBilSysexp(String cardNo) throws Exception
 {
  dateTime();
if(DEBUG==1) showLogMessage("I","","    insertBilSysexp="+ cardNo );

  extendField = "sysp.";
  setValue("sysp.card_no"            , cardNo);
  setValue("sysp.p_seqno"            , getValue("p_seqno"));
  setValue("sysp.acct_type"          , getValue("acct_type"));
  setValue("sysp.bill_type"          , "INHU");
  setValue("sysp.txn_code"           , "05");
  setValue("sysp.purchase_date"      , getValue("trans_date"));
  setValue("sysp.src_amt"            , getValue("sysp.dest_amt")); 
  setValue("sysp.dest_amt"           , getValue("sysp.dest_amt")); 
  setValue("sysp.dc_dest_amt"        , getValue("sysp.dest_amt")); 
  setValue("sysp.dest_curr"          , "901");
  setValue("sysp.curr_code"          , "901");
  setValue("sysp.bill_desc"          , "高鐵升等扣款金額");
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
 int selectMktThsrUptxn1(String payType) throws Exception
 {
  extendField = "txn1.";
  selectSQL = "count(*) as deduct_cnt ";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE  card_no      = ? "
            + "AND    proc_flag    = 'Y' "
            + "AND    pay_type     = ? "
            + "AND    refund_flag != 'Y' "
            + "and    trans_type  != 'R' "
            + "and    trans_date like ? ";

  setString(1, getValue("card_no"));
  setString(2, payType);
  setString(3, getValue("trans_date").substring(0,6)+"%"); 

  selectTable();

  return(getValueInt("txn1.deduct_cnt"));
 }
// ************************************************************************ 
 int selectMktThsrUptxn2() throws Exception
 {
  extendField = "txn2.";
  selectSQL = "rowid as rowid";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE  card_no       = ?   "
            + "AND    proc_flag     = '1' "
            + "and    trans_type    = 'R' "
            + "and    org_serial_no =  ?  "
            + "and    trans_date    >=  ?  ";

  setString(1,getValue("card_no"));
  setString(2,getValue("serial_no")); 
  setString(3,getValue("trans_date")); 

  selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************ 
 void updateMktThsrUptxn1() throws Exception
 {
  dateTime();
  updateSQL = "bp_check_date = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , sysDate);
  setString(2 , javaProgram);
  setRowId(3  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_1 error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void updateMktThsrUptxnA() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag     = 'Y', "
            + "proc_date     = ?, "
            + "pay_type      = '1', "
            + "deduct_bp     = ?, "
            + "deduct_bp_tax = ?, "
            + "bp_check_date = ?, " 
            + "deduct_amt    = 0, "
            + "tran_seqno    = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid   = ?";

  setString(1 , hBusinessDate);
  setInt(2 , comb.tranBpNotax*-1);
  setInt(3 , comb.tranBpTax*-1);
  setString(4 , sysDate);
  setString(5 , tranSeqno);
  setString(6 , javaProgram);
  setRowId(7  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void updateMktThsrUptxnB(String PayType) throws Exception
 {
if(DEBUG==1) showLogMessage("I","","    updateMktThsrUptxnB=[" + PayType + "]");
  updateSQL = "proc_flag       = 'Y',"
            + "proc_date       = ?,"
            + "pay_type        = ?,"
            + "deduct_amt      = ?,"
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";  
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , hBusinessDate);
  setString(2 , PayType);
  setInt(3    , getValueInt("sysp.dest_amt")); 
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  return;
 }
// ************************************************************************
 void updateMktThsrUptxn2() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag     = 'Y', "
            + "proc_date     = ?, "
            + "pay_type      = '3', "
            + "upidno_seqno  = ?, "
            + "upidno_type   = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid   = ?";

  setString(1 , hBusinessDate);
  setString(2 , getValue("upid.upidno_seqno"));
  setString(3 , getValue("upidno_type"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_2 error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 int insertMktBonusDtl() throws Exception
 {
  tranSeqno     = comr.getSeqno("MKT_MODSEQ");

  setValue("mbdl.tran_date"            , sysDate);
  setValue("mbdl.tran_time"            , sysTime);
  setValue("mbdl.tran_seqno"           , tranSeqno);
  setValue("mbdl.bonus_type"           , "BONU");
  setValue("mbdl.tran_code"            , "4");
  setValue("mbdl.mod_memo"             , "卡號["+getValue("card_no")+"]");
  setValue("mbdl.acct_date"            , hBusinessDate);
//setValue("mbdl.proc_month"           , business_date.substring(0,6));
  setValue("mbdl.acct_type"            , getValue("acct_type"));
  setValue("mbdl.id_p_seqno"           , getValue("major_id_p_seqno"));
  setValue("mbdl.p_seqno"              , getValue("p_seqno"));
  setValue("mbdl.active_name"          , "高鐵車廂升等紅利點數扣點");
  setValue("mbdl.tran_pgm"             , javaProgram);
  setValue("mbdl.apr_user"             , javaProgram);
  setValue("mbdl.apr_flag"             , "Y");
  setValue("mbdl.apr_date"             , sysDate);
  setValue("mbdl.crt_user"             , javaProgram);
  setValue("mbdl.crt_date"             , sysDate);
  setValue("mbdl.mod_user"             , javaProgram); 
  setValue("mbdl.mod_time"             , sysDate+sysTime);
  setValue("mbdl.mod_pgm"              , javaProgram);

  extendField = "mbdl.";
  daoTable  = "mkt_bonus_dtl";

  insertTable();

  return(0);
 }
// ************************************************************************
 int selectMktThsrUpmode() throws Exception 
 {
  extendField = "mode.";
  selectSQL = "ticket_pnt_cond,"
            + "ticket_pnt_cnt,"
            + "ticket_pnt,"
            + "add_file_flag,"
            + "ticket_amt_cond,"
            + "ticket_amt_cnt,"
            + "ticket_amt,"
            + "ex_ticket_amt,"
            + "card_mode ";
  daoTable  = "mkt_thsr_upmode";
  whereStr  = "WHERE decode(start_date,'','20000101',start_date) <= ? "
            ;

  setString(1 , hBusinessDate );

  parmCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void  loadCrdIdno() throws Exception
 {
  extendField = "idno.";
  selectSQL = "id_p_seqno,"
            + "id_no";
  daoTable  = "crd_idno";
  whereStr  = "where id_p_seqno in ( "
            + "      select distinct major_id_p_seqno "
            + "      from  mkt_thsr_uptxn "
            + "      WHERE   proc_flag    = '1' "
            + "      and     card_mode   != '6' "
            + "      and     trans_type  != 'R' "
            + "      union "
            + "      select distinct id_p_seqno "
            + "      from  mkt_thsr_uptxn "
            + "      WHERE   proc_flag    = '1' "
            + "      and     card_mode   != '6' "
            + "      and     trans_type  != 'R') "
            ;

  int  n = loadTable();
  setLoadData("idno.id_p_seqno");

  showLogMessage("I","","Load crd_idno Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktThsrUpidno() throws Exception 
 {
  extendField = "upid.";
  selectSQL = "file_type,"
            + "branch_code,"
            + "free_cnt,"
            + "upidno_seqno";
  daoTable  = "mkt_thsr_upidno";
 whereStr   = "WHERE  id_no = ?  "
            + "and    ? between free_date_s "
            + "         and     free_date_e "
            + "order by file_type,file_date,branch_code "
            ;

  setString(1 , getValue("idno.id_no"));
  setString(2 , getValue("trans_date"));

  branchCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int selectMktThsrUpidnoMon(int intm) throws Exception 
 {
  extendField = "umon.";
  selectSQL = "sum(decode(refund_flag,'N',1,-1)) as use_cnt";
  daoTable  = "mkt_thsr_upidno_mon";
  whereStr  = "WHERE id_p_seqno   = ?  "
            + "and   trans_month  = ? "
            + "and   upidno_seqno = ? "
            ;

  setString(1 , getValue("id_p_seqno"));
  setString(2 , getValue("trans_date").substring(0,6));
  setString(3 , getValue("upid.upidno_seqno"));

  selectTable();

  return(getValueInt("upid.free_cnt",intm)-getValueInt("umon.use_cnt")); 
 }
// ************************************************************************
 int insertMktThsrUpidnoMon(int intm) throws Exception
 {
  setValue("imon.serial_no"            , getValue("serial_no"));
  setValue("imon.file_type"            , getValue("upid.file_type",intm));
  setValue("imon.branch_code"          , getValue("upid.branch_code",intm));
  setValue("imon.trans_month"          , getValue("trans_date").substring(0,6));
  setValue("imon.id_p_seqno"           , getValue("id_p_seqno"));
  setValue("imon.upidno_seqno"         , getValue("upid.upidno_seqno",intm));
  setValue("imon.refund_flag"          , "N");
  setValue("imon.mod_time"             , sysDate+sysTime);
  setValue("imon.mod_pgm"              , javaProgram);

  extendField = "imon.";
  daoTable  = "mkt_thsr_upidno_mon";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktThsrUptxn2b(String procFlag) throws Exception
 {
  updateSQL = "proc_flag     = ?, "
            + "proc_date     = ?, "
            + "error_code    = ?, "
            + "error_desc    = ?, "
            + "card_mode     = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";   
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid = ?";

  setString(1 , procFlag);
  setString(2 , hBusinessDate);
  setString(3 , getValue("error_code"));
  setString(4 , getValue("error_desc"));
  setString(5 , getValue("card_mode"));
  setString(6 , javaProgram);
  setRowId(7  , getValue("txn2.rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_1 error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 void updateMktThsrUptxn2a(String procFlag) throws Exception
 {
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
  setRowId(6  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_2 error "+getValue("mtu1.rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************

}  // End of class FetchSample
