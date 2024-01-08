/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 108/01/06  V1.00.00  Allen Ho   new                                        *
* 109-12-04  V1.00.01  tanwei     updated for project coding standard        *
* 112-11-20  V1.00.02  Holmes     selectMktBonusDtl0() sql error             *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;

@SuppressWarnings("unchecked")
public class MktA750 extends AccessDAO
{
 private  String progname = "紅利積點-定期轉歷史檔處理程式 109/12/04 V1.00.01";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String hBusiBusinessDate  = "";
 String tohistDate           = "";

 long    totalCnt=0;
 long    deleteCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktA750 proc = new MktA750();
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
   
   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();
   tohistDate =comm.lastMonth(hBusiBusinessDate,12) + hBusiBusinessDate.substring(6,8);

   showLogMessage("I","","=========================================");
   showLogMessage("I","","餘額均為零資料");
   selectMktBonusDtl();
   showLogMessage("I","","處理帳戶數["+totalCnt+"] 轉歷史檔筆數 ["+deleteCnt+"]");
   showLogMessage("I","","=========================================");
   countCommit();

   if (deleteCnt<500000)
      { 
       showLogMessage("I","","餘額不為零之前為零資料");
       showLogMessage("I","","=========================================");
       selectMktBonusDtl0();
       showLogMessage("I","","處理帳戶數["+totalCnt+"] 轉歷史檔筆數 ["+deleteCnt+"]");
      }

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

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************ 
 public void  selectMktBonusDtl() throws Exception
 {
  selectSQL = "p_seqno,"
            + "bonus_type,"
            + "sum(end_tran_bp)";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where tran_date < ? "
            + "group by p_seqno,bonus_type "
            + "having sum(end_tran_bp) = 0 " 
            ;
  setString(1 , tohistDate);

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;

    selectMktBonusDtl1();

    if (deleteCnt>=500000) 
       {
        showLogMessage("I","","每次轉歷史超過 500000 筆將停止執行");
        break;
       }
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 public void  selectMktBonusDtl0() throws Exception
 {
  selectSQL = "p_seqno,"
            + "bonus_type,"
            + "min(decode(sign(end_tran_bp),0,tran_date,'30000101')) as min_date,"
            + "min(decode(sign(end_tran_bp),0,'30000101',tran_date)) as max_date";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where tran_date < ?  and end_tran_bp = 0 "
            + "group by p_seqno,bonus_type "
//            + "having "
//            + " min(decode(sign(end_tran_bp),0,tran_date,'30000101')) < "
//            + " min(decode(sign(end_tran_bp),0,'30000101',tran_date)) "
            ;
  setString(1 , tohistDate);

  openCursor();

  totalCnt=0;
  while( fetchTable() ) 
   { 
    totalCnt++;
    selectMktBonusDtl1();

    if (deleteCnt>=1000000) 
       {
        showLogMessage("I","","每次轉歷史超過 1000000 筆將停止執行");
        break;
       }
   } 
  closeCursor();
  return;
 }
// ************************************************************************
 public void  selectMktBonusDtl1() throws Exception
 {
  selectSQL = "end_tran_bp,"
            + "rowid as rowid";
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "where  tran_date   < ? "
            + "and    p_seqno     = ? "
            + "and    bonus_type  = ? "
            + "order by tran_date,tran_time"
            ;

  setString(1 , tohistDate);
  setString(2 , getValue("p_seqno"));
  setString(3 , getValue("bonus_type"));

  int recCnt = selectTable();

  for ( int inti=0; inti<recCnt; inti++ )
    { 
     if (getValueInt("end_tran_bp",inti)!=0) break;
     insertMktBonusDtlHst(inti);
     deleteMktBonusDtl(inti);
     deleteCnt++;
    if (deleteCnt%100000==0) 
       {
        showLogMessage("I","","處理帳戶數["+totalCnt+"] 轉歷史檔筆數 ["+deleteCnt+"]");
        countCommit();
       }
    } 
 }
// ************************************************************************
public void insertMktBonusDtlHst(int inti) throws Exception
 {
  sqlCmd    = "insert into mkt_bonus_dtl_hst "
            + "select * from mkt_bonus_dtl "
            + "WHERE rowid = ? ";

  daoTable  = "mkt_bonus_dtl_hst";

  setRowId(1,getValue("rowid",inti));

  insertTable();

  return;
 }
// ************************************************************************
 public void deleteMktBonusDtl(int inti) throws Exception
 {
  daoTable  = "mkt_bonus_dtl";
  whereStr  = "WHERE rowid  = ? ";

  setRowId(1,getValue("rowid",inti));

  deleteTable();

  return;
 }
// ************************************************************************

}  // End of class FetchSample
