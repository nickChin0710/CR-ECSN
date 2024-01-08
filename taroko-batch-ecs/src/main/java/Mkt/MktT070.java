/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/02/18  V1.00.16  Allen Ho   mkt_T070                                   *
* 111/12/07  V1.00.17  Zuwei      sync from mega                             *
* 112/09/22  V1.00.18  Kirin      chage coup
* 112/09/27  V1.00.19  Zuwei Su   Insert【bil_sysexp】前增處理邏輯檢核符合   *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT070 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-扣款加檔處理程式 112/09/27 V1.00.19";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommDBonus comb = null;
 CommDate commDate = new CommDate();

 String hBusinessDate = "";
 boolean debug = false;

 int     totalCnt=0;
 int    cnt1=0,branchCnt=0;
 int[]  procInt = new int[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT070 proc = new MktT070();
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
      { hBusinessDate = args[0]; }

   if ( args.length == 2 )
      { if (args[1].toUpperCase().equals("DEBUG")) debug=true; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommDBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();

   if (!hBusinessDate.substring(6,8).equals("01"))
      {
       showLogMessage("I","","本程式只在每月1日執行,本日為"+hBusinessDate+"日.. 執行結束");
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
       showLogMessage("I","","尚未收到上月最後一日["+ comm.lastDate(hBusinessDate) +"]檔案, 暫停發送");
       return(0);
      }
   showLogMessage("I","","檢核完成 !");
          
   showLogMessage("I","","===============================");
   showLogMessage("I","","載入暫存吃料.....");
   loadCrdIdno();
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrUptxn();
   if (!hBusinessDate.substring(6,8).equals("01"))
      {
       showLogMessage("I","","處理卡數           ["+procInt[10] +"] 筆");
       showLogMessage("I","","    01日才執行     ["+procInt[11]+"] 筆");
      }
   showLogMessage("I","","處理筆數           ["+totalCnt+"] 筆");
   showLogMessage("I","","    卡片減免       ["+procInt[0]+"] 筆");
   showLogMessage("I","","    正卡減免       ["+procInt[1]+"] 筆");
   showLogMessage("I","","    優惠額度內     ["+procInt[2]+"] 筆");
   showLogMessage("I","","    優惠額度外     ["+procInt[3]+"] 筆");
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
  selectSQL = "max(p_seqno) as p_seqno,"
            + "max(acct_type) as acct_type,"
            + "max(id_p_seqno) as id_p_seqno,"
            + "max(major_id_p_seqno) as major_id_p_seqno,"
            + "max(corp_no) as corp_no,"
            + "min(a.card_mode) as card_mode,"
            + "card_no";
  daoTable  = "mkt_thsr_uptxn  a,mkt_thsr_upmode b";
  whereStr  = "WHERE proc_flag       = '1' "
            + "AND   trans_date      < ? "
            + "AND   trans_type      = 'P' "
            + "AND   a.card_mode     = b.card_mode "
            + "AND   b.add_file_flag = 'Y' "
            + "group by card_no "
            + "";

  setString(1 , hBusinessDate);              

  openCursor();

  while( fetchTable() ) 
   {
    procInt[10]++; 
    selectMktThsrUpmode();
    if ((getValue("mode.ticket_pnt_cond").equals("Y"))&&
        (!hBusinessDate.substring(6,8).equals("01")))
       {
        procInt[11]++;
        continue;
       }

    if (!getValue("mode.ticket_amt_cond").equals("Y"))
       {
        setValue("mode.ticket_amt_cnt" , "0");
        setValue("mode.ticket_amt"     , "0");
       }
        
    selectMktThsrUptxn1();
    processDisplay(50000); // every 10000 display message
   } 
  closeCursor();
 }
// ************************************************************************
 int selectMktThsrUptxn1() throws Exception
 {
  extendField = "mtu1.";
  selectSQL = "rowid as rowid,"
            + "serial_no,"
            + "trans_date ";     
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE proc_flag  = '1'  "
            + "AND   trans_date < ? "
            + "AND   trans_type = 'P' "
            + "and   card_no    = ? "
            ;

  setString(1, hBusinessDate);              
  setString(2, getValue("card_no"));

  int recCnt = selectTable();

  int lastCnt = 0;
  int existFlag=0;
  for (int inti=0;inti<recCnt;inti++)
    {
     totalCnt++;
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
           updateMktThsrUptxn2(inti,"1");
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
             showLogMessage("I","","major_id_p_seqno : ["+ getValue("major_id_p_seqno") +"] not found in crd_idno");
             continue;
            }

         if (selectMktThsrUpidno()==0)
           {
            existFlag=0;
            for (int intm=0;intm<branchCnt;intm++)
              {
               if (selectMktThsrUpidnoMon(intm) <=0) continue;
               insertMktThsrUpidnoMon(intm);
               updateMktThsrUptxn2(inti,"2");
               procInt[1]++;
               existFlag=1;
               break;
              }
            if (existFlag==1) continue;
           }
        }

     lastCnt = selectMktThsrUptxnCnt(inti);

     setValueInt("mode.ticket_amt_cnt" , getValueInt("mode.ticket_amt_cnt")-lastCnt);

     if (getValueInt("mode.ticket_amt_cnt")>0)
        {
         setValue("sysp.dest_amt" , getValue("mode.ticket_amt"));
         procInt[2]++;
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
         continue;
     }

     insertBilSysexp(inti);
     updateMktThsrUptxn(inti);
     setValueInt("mode.ticket_amt_cnt" , getValueInt("mode.ticket_amt_cnt")-1);
    }                

  return(0);
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
    
    // 筆數大於>freeTotCntSum筆要Insert
    if (cnt > 0 && getValueInt("cnt") > freeTotCntSum) {
        return 1;
    }
    
    return 0;
}

// ************************************************************************
 int insertBilSysexp(int inti) throws Exception
 {
  dateTime();
  extendField = "sysp.";
  setValue("sysp.card_no"            , getValue("card_no"));
  setValue("sysp.p_seqno"            , getValue("p_seqno"));
  setValue("sysp.acct_type"          , getValue("acct_type"));
  setValue("sysp.bill_type"          , "INHU");
  setValue("sysp.txn_code"           , "05");
  setValue("sysp.purchase_date"      , getValue("mtu1.trans_date",inti));
  setValue("sysp.src_amt"            , getValue("sysp.dest_amt")); 
  setValue("sysp.dest_amt"           , getValue("sysp.dest_amt")); 
  setValue("sysp.dc_dest_amt"        , getValue("sysp.dest_amt")); 
  setValue("sysp.dest_curr"          , "901");
  setValue("sysp.curr_code"          , "901");
  setValue("sysp.bill_desc"          , "高鐵升等扣款金額");
  setValue("sysp.post_flag"          , "N"); 
  setValue("sysp.ref_key"            , getValue("mtu1.serial_no",inti)); 
  setValue("sysp.mod_user"           , javaProgram); 
  setValue("sysp.mod_time"           , sysDate+sysTime);
  setValue("sysp.mod_pgm"            , javaProgram);
                     
  daoTable  = "bil_sysexp";

  insertTable();

  return(0);
 }
// ************************************************************************
 void updateMktThsrUptxn(int inti) throws Exception
 {
  updateSQL = "proc_flag       = 'Y',"
            + "proc_date       = ?,"
            + "pay_type        = '2',"
            + "deduct_amt      = ?,"
            + "mod_pgm         = ?, "
            + "mod_time        = sysdate";  
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid    = ? ";

  setString(1 , hBusinessDate);
  setInt(2    , getValueInt("sysp.dest_amt")); 
  setString(3 , javaProgram);
  setRowId(4  , getValue("mtu1.rowid",inti));

  updateTable();
  return;
 }
// ************************************************************************
 int selectMktThsrUpmode() throws Exception
 {
  extendField = "mode.";
  selectSQL = "ticket_pnt_cond,"
            + "ticket_amt_cond,"
            + "ticket_amt_cnt,"
            + "ticket_amt,"
            + "ex_ticket_amt";
  daoTable  = "mkt_thsr_upmode";
  whereStr  = "WHERE card_mode = ? "
            ;

  setString(1 , getValue("card_mode"));

  selectTable();

  return(0);
 }
// ************************************************************************
 int  selectEcsFtpLog() throws Exception
 {
  daoTable  = "ecs_ftp_log";
  whereStr  = "where ref_ip_code = 'COUP_FTP' "
//whereStr  = "where ref_ip_code = 'MGUP_FTP' "		  
            + "and   file_name   = ? "
            + "and   proc_code   = 'Y' "
            ;

//setString(1 , "mgup"+ comm.lastDate(businessDate) +".txt");
  setString(1 , "coup"+ comm.lastDate(hBusinessDate) +".txt");

  int recordCnt = selectTable();

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
            + "      select distinct id_p_seqno "
            + "      from  mkt_thsr_uptxn "
            + "      WHERE proc_flag  = '1' "
            + "      AND   trans_type = 'P' "
            + "      AND   trans_date < ? "
            + "      union "
            + "      select distinct major_id_p_seqno "
            + "      from  mkt_thsr_uptxn "
            + "      WHERE proc_flag  = '1' "
            + "      AND   trans_type = 'P' "
            + "      AND   trans_date < ? )"
            ;

  setString(1 , hBusinessDate);
  setString(2 , hBusinessDate);
                
  int  n = loadTable();
  setLoadData("idno.id_p_seqno");

  showLogMessage("I","","Load crd_idno Count: ["+n+"]");
 }
// ************************************************************************
 void updateMktThsrUptxn2(int inti,String upidnoType) throws Exception
 {
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
  setString(2 , getValue("upid.upid_seqno"));
  setString(3 , upidnoType);
  setString(4 , javaProgram);
  setRowId(5  , getValue("mtu1.rowid",inti));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE mkt_thsr_uptxn_2 error "+getValue("rowid")); 
      exitProgram(1); 
     }
  return;
 }
// ************************************************************************
 int selectMktThsrUptxnCnt(int inti) throws Exception
 {
  extendField = "txn1.";
  selectSQL = "count(*) as deduct_cnt ";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE  card_no      = ? "
            + "AND    proc_flag    = 'Y' "
            + "AND    pay_type     = '2' "
            + "AND    refund_flag != 'Y' "
            + "and    trans_type   = 'P' "
            + "and    trans_date like ? ";

  setString(1,getValue("card_no"));
  setString(2,getValue("mtu1.trans_date",inti).substring(0,6)+"%"); 

  selectTable();

  return(getValueInt("txn1.deduct_cnt"));
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


}  // End of class FetchSample
