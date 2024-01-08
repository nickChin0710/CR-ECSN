/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 108/11/11   V1.00.00  Allen Ho   mkt_t020                                  *
 * 109-12-11   V1.00.01    tanwei      updated for project coding standard    *
 * 111/12/07   V1.00.02  Zuwei      sync from mega                             *
 ******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktT020 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-檢核授權處理程式 111/12/07 V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate   = "";
 String transSeqno     = "";
 String fileDate       = "";

 int parmCnt=0;
 long    totalCnt=0;
 int[] procInt = new int[20];
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT020 proc = new MktT020();
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

   showLogMessage("I",""," 序號 ["+ transSeqno +"]-[" + fileDate +"]");

   selectPtrBusinday();

   showLogMessage("I","","===============================");
   showLogMessage("I","","讀取參數.....");
   selectMktThsrUpmode(); 
   showLogMessage("I","","===============================");
   showLogMessage("I","","載入暫存檔.....");
   loadMktBnData(); 
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectMktThsrUptxn();
   showLogMessage("I","","處理筆數           ["+totalCnt+"] 筆");
   showLogMessage("I","","  卡類明細");
    for (int inti=0;inti<parmCnt;inti++)
        {
         if (procInt[inti]==0) continue;
         showLogMessage("I","","    卡類 ["+ getValue("parm.card_mode",inti) +"] ["+procInt[inti]+"] 筆");
        }
   showLogMessage("I","","  購票退票明細");
   showLogMessage("I","","    購票  ["+ procInt[11] +"] 筆");
   showLogMessage("I","","    退票  ["+ procInt[12] +"] 筆");
   showLogMessage("I","","  檢核結果");
   showLogMessage("I","","    00    ["+ procInt[17] +"] (正常轉入)");
   showLogMessage("I","","    90    ["+ procInt[13] +"] (比對授權失敗)");
   showLogMessage("I","","    91    ["+ procInt[14] +"] (查有多筆交易紀錄)");
   showLogMessage("I","","    92    ["+ procInt[18] +"] (卡片未在定義卡類中)");
   showLogMessage("I","","    93    ["+ procInt[15] +"] (退票無法比對購票資料)");
   showLogMessage("I","","    94    ["+ procInt[16] +"] (無卡號資料)");
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
  selectSQL = "trans_date,"
            + "trans_time,"
            + "trans_type,"
            + "serial_no,"
            + "org_serial_no,"
            + "pay_cardid,"
            + "authentication_code,"
            + "auth_flag,"
            + "trans_seqno,"
            + "rowid as rowid";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE   proc_flag  = '0' "
            + "and     trans_seqno = ? "
            + "and     file_date   = ? "
            + "ORDER   BY trans_seqno,trans_date,trans_type "
            ;

  setString(1 , transSeqno); 
  setString(2 , fileDate); 

  openCursor();

  String transSeqno = "";
  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
//  if (!getValue("trans_seqno").equals(trans_seqno))
//     update_ecs_ftp_log();
    transSeqno = getValue("trans_seqno");
    setValue("last_date" , comm.lastDate(getValue("trans_date")));
    setValue("next_date" , comm.nextDate(getValue("trans_date")));
//  setValue("last_time" , comm.nextNDateMin(getValue("trans_date")+getValue("trans_time"),-1));
//  setValue("next_time" , comm.nextNDateMin(getValue("trans_date")+getValue("trans_time"),1));

/*
   showLogMessage("I","","trans_type ["+getValue("trans_type")+"] ");
   showLogMessage("I","","trans_date ["+getValue("trans_date")+"] ");
   showLogMessage("I","","trans_time ["+getValue("trans_time")+"] ");
   showLogMessage("I","","next_date ["+getValue("next_date")+"] ");
   showLogMessage("I","","last_date ["+getValue("last_date")+"] ");
   showLogMessage("I","","next_time ["+getValue("next_time")+"] ");
   showLogMessage("I","","last_time ["+getValue("last_time")+"] ");
*/
    setValue("proc_flag"   , "1");  
    
    int int2 = 0;
    if (getValue("trans_type").equals("P"))
       {
        procInt[11]++;

        int2 = selectCcaAuthTxlog(); 

// beg  only for debug test (because auth no data
/*
            if (select_crd_card_1()!=0)
               {
                showLogMessage("I","","card_no1  ["+getValue("pay_cardid")+"] ");
                continue;
               }
*/
// end
       }
    else
       {
        procInt[12]++;
        int2=selectMktThsrUptxnA();   //   依比對結果判斷
       }

    if (int2==1) 
       {
        procInt[13]++;
        setValue("proc_flag" , "X");   //   查無交易則視為失敗
        setValue("error_code" , "90");
        setValue("error_desc" , "比對授權失敗");
        updateMktThsrUptxn1();
        continue;
       }
    if (int2==2) 
       {
        procInt[14]++;
        setValue("proc_flag" , "X");   //   查有多筆交易紀錄
        setValue("error_code" , "91");
        setValue("error_desc" , "查有多筆交易紀錄");
        updateMktThsrUptxn1();
        continue;
       }
    if (int2==3) 
       {
        procInt[15]++;
        setValue("proc_flag" , "X");   //   退票無法比對購票資料
        setValue("error_code" , "93");
        setValue("error_desc" , "退票比對不到購票資料");
        updateMktThsrUptxn1();
        continue;
       }
        
    if (selectCrdCard()!=0)
       {
        procInt[16]++;
//      showLogMessage("I","","card_no ["+getValue("auth.card_no")+"] not esixt ");
//      showLogMessage("I","","card_no ["+getValue("pay_cardid")+"] not esixt ");
        setValue("proc_flag" , "X");   //   退票無法比對購票資料
        setValue("error_code" , "94");
        setValue("error_desc" , "無卡號資料");
        updateMktThsrUptxn1();
        continue;
       }

    setValue("group_code" , getValue("card.group_code"));   // 不可變更原始 group_code
    if (getValue("group_code").length()==0) 
       setValue("group_code" , "0000");

    int okFlag = 0;
    for (int inti=0;inti<parmCnt;inti++)
        {
         setValue("data_key", getValue("parm.card_mode",inti));

         if (selectMktBnData(getValue("card.card_type"),
                                getValue("parm.card_type_sel",inti),"1",3)!=0) continue;

         if (selectMktBnData(getValue("group_code"),
                                getValue("parm.group_code_sel",inti),"2",3)!=0) continue;

         setValue("card_mode"  , getValue("parm.card_mode",inti));
         procInt[inti]++;
         okFlag = 1;
         break;
        }
    if (okFlag==0)
       {
        procInt[18]++;
        setValue("proc_flag" , "X");  
        setValue("error_code" , "92");
        setValue("error_desc" , "卡片未在定義卡類中");
        updateMktThsrUptxn1();
        continue;
       }
       
    updateMktThsrUptxn();
    procInt[17]++;
  
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
  whereStr  = "where tx_date  between ? and ? "
            + "and   auth_no    = ? "
            + "and   card_no like ? "
            + "and   substr(card_no,length(card_no)-3,4) = substr(?,length(card_no)-3,4) ";
            ;

  setString(1 , getValue("last_date"));
  setString(2 , getValue("next_date"));
  setString(3 , getValue("authentication_code"));
  setString(4 , getValue("pay_cardid").substring(0,6)+"%");
  setString(5 , getValue("pay_cardid"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
  if ( recCnt >1 ) return(2);
  return(0);
 }
// ************************************************************************
 int selectMktThsrUptxnA() throws Exception
 {
  extendField = "upta.";
  selectSQL = "card_no";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "where serial_no   = ? "
            + "and   trans_type  = 'P' ";

  setString(1 , getValue("org_serial_no"));

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(3);
  if ( recCnt >1 ) return(3);
  setValue("auth.card_no" , getValue("upta.card_no"));
  return(0);
 }
// ************************************************************************
 int selectCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "group_code,"
            + "card_type,"
            + "card_no,"
            + "p_seqno,"
            + "acct_type,"
            + "reg_bank_no,"
            + "major_id_p_seqno,"
            + "id_p_seqno,"
            + "major_card_no,"
            + "corp_no,"
            + "promote_emp_no,"
            + "ori_issue_date as issue_date";
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
  extendField = "card.";
  selectSQL = "card_no";
  daoTable  = "crd_card";
  whereStr  = "where card_no like substr(?,1,6)||'%' "
            + "and   substr(card_no,length(card_no)-3,4) = substr(?,length(card_no)-3,4) ";

  setString(1,getValue("pay_cardid"));
  setString(2,getValue("pay_cardid"));

  int recCnt = selectTable();
  if ( notFound.equals("Y")) return(1);

  setValue("auth.card_no" , getValue("card.card_no"));
  return(0); 
 }
// ************************************************************************
 void updateMktThsrUptxn1() throws Exception
 {
  dateTime();
  updateSQL = "proc_flag        = ?,"
            + "auth_flag        = 'N',"
            + "proc_date        = ?,"
            + "error_code       = ?,"
            + "error_desc       = ?,"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid  = ? ";

  setString(1 , getValue("proc_flag")); 
  setString(2 , businessDate); 
  setString(3 , getValue("error_code")); 
  setString(4 , getValue("error_desc")); 
  setString(5 , javaProgram);
  setRowId(6  , getValue("rowid"));


  updateTable();
  return;
 }
// ************************************************************************
 void updateMktThsrUptxn() throws Exception
 {
  dateTime();
  updateSQL = "card_no          = ?,"
            + "p_seqno          = ?,"
            + "acct_type        = ?,"
            + "reg_bank_no      = ?,"
            + "major_id_p_seqno = ?,"
            + "id_p_seqno       = ?,"
            + "card_type        = ?,"
            + "group_code       = ?,"
            + "major_card_no    = ?,"
            + "corp_no          = ?,"
            + "promote_emp_no   = ?,"
            + "issue_date       = ?,"
            + "card_mode        = ?,"
            + "proc_date        = ?,"
            + "proc_flag        = ?,"
            + "auth_flag        = ?,"
            + "auth_date        = ?,"
            + "error_code       = '00',"
            + "mod_pgm          = ?, "
            + "mod_time         = sysdate";
  daoTable  = "mkt_thsr_uptxn";
  whereStr  = "WHERE rowid  = ? ";

  setString(1  , getValue("card.card_no"));
  setString(2  , getValue("card.p_seqno")); 
  setString(3  , getValue("card.acct_type")); 
  setString(4  , getValue("card.reg_bank_no")); 
  setString(5  , getValue("card.major_id_p_seqno")); 
  setString(6  , getValue("card.id_p_seqno")); 
  setString(7  , getValue("card.card_type")); 
  setString(8  , getValue("card.group_code")); 
  setString(9  , getValue("card.major_card_no")); 
  setString(10 , getValue("card.corp_no")); 
  setString(11 , getValue("card.promote_emp_no")); 
  setString(12 , getValue("card.issue_date")); 
  setString(13 , getValue("card_mode")); 
  setString(14 , businessDate); 
  setString(15 , getValue("proc_flag")); 
  setString(16 , getValue("auth_flag")); 
  setString(17 , businessDate); 
  setString(18 , javaProgram);
  setRowId(19 , getValue("rowid"));


  updateTable();
  return;
 }
// ************************************************************************
 void  loadMktBnData() throws Exception
 {
  extendField = "data.";
  selectSQL = "data_key,"
            + "data_type,"
            + "data_code,"
            + "data_code2";
  daoTable  = "mkt_bn_data";
  whereStr  = "WHERE TABLE_NAME = 'MKT_THSR_UPMODE' "
            + "order by data_key,data_type,data_code,data_code2";

  int  n = loadTable();

  setLoadData("data.data_key,data.data_type,data.data_code");
  setLoadData("data.data_key,data.data_type");

  showLogMessage("I","","Load mkt_bn_data Count: ["+n+"]");
 }
// ************************************************************************
 int selectMktBnData(String col1,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,"","",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String sel,String dataType,int dataNum) throws Exception
 {
  return selectMktBnData(col1,col2,"",sel,dataType,dataNum);
 }
// ************************************************************************
 int selectMktBnData(String col1,String col2,String col3,String sel,String dataType,int dataNum) throws Exception
 {
  if (sel.equals("0")) return(0);

  setValue("data.data_key" , getValue("data_key"));
  setValue("data.data_type",dataType);

  int cnt1=0;
  if (dataNum==2)
     {
      cnt1 = getLoadData("data.data_key,data.data_type");
     }
  else
     {
      setValue("data.data_code",col1);
      cnt1 = getLoadData("data.data_key,data.data_type,data.data_code");
     }

  int okFlag=0;
  for (int intm=0;intm<cnt1;intm++)
    {
     if (dataNum==2)
        {
         if (getValue("data.data_code",intm).length()!=0)
            {
             if (col1.length()!=0)
                {
                 if (!getValue("data.data_code",intm).equals(col1)) continue;
                }
              else
                {
                 if (sel.equals("1")) continue;
                }
            }
        }
     if (getValue("data.data_code2",intm).length()!=0)
        {
         if (col2.length()!=0)
            {
             if (!getValue("data.data_code2",intm).equals(col2)) continue;
            }
          else
            {
             continue;
            }
        }

     if (getValue("data.data_code3",intm).length()!=0)
        {
         if (col3.length()!=0)
            {
             if (!getValue("data.data_code3",intm).equals(col3)) continue;
            }
          else
            {
             continue;
            }
        }
     okFlag=1;
     break;
    }

  if (sel.equals("1"))
     {
      if (okFlag==0) return(1);
      return(0);
     }
  else
     {
      if (okFlag==0) return(0);
      return(1);
     }
 }
// ************************************************************************
 int selectMktThsrUpmode() throws Exception
 {
  extendField = "parm.";
  selectSQL = "card_mode,"
            + "card_type_sel,"
            + "group_code_sel";
  daoTable  = "mkt_thsr_upmode";
  whereStr  = "WHERE (stop_flag !='Y' "
            + " or    (stop_flag = 'Y' "
            + "  and   stop_date > ? )) "
            + "and   decode(start_date,'','20000101',start_date) <= ? "
            + "order by card_mode "
            ;

  setString(1 , businessDate );
  setString(2 , businessDate );

  parmCnt = selectTable();

  showLogMessage("I","","讀取參數 ["+ parmCnt +"] 筆");
  return(0);
 }
// ************************************************************************
 void updateEcsFtpLog() throws Exception
 {
  dateTime();
  updateSQL = "proc_code  = 'Y', "
            + "trans_desc = trans_desc||?, "
            + "proc_desc  = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "WHERE trans_seqno = ?";

  setString(1 , "已檢核");
  setString(2 , "已檢核");
  setString(3 , javaProgram);
  setString(4 , getValue("trans_seqno"));

  updateTable();
  if ( notFound.equals("Y") )
      showLogMessage("I","","UPDATE ecs_ftp_log error "+getValue("rowid")); 

  return;
 }
// ************************************************************************

}  // End of class FetchSample
