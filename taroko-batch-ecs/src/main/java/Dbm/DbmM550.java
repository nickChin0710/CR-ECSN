/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/08/02  V1.00.11  Allen Ho   new                                        *
* 111/11/08  V1.00.02  Yang Bo    sync code from mega                        *
******************************************************************************/
package Dbm;

import com.*;

import java.lang.*;

@SuppressWarnings("unchecked")
public class DbmM550 extends AccessDAO
{
 private final String PROGNAME = "Debit紅利-定期轉歷史檔處理程式 111/11/08  V1.00.02";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;
 CommBonus comb = null;

 String businessDate = "";
 String tohistDate = "";
 String delayDate = "";

 long    totalCnt=0;
 long    deleteCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  DbmM550 proc = new DbmM550();
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

   if (args.length > 1)
      {
       showLogMessage("I","","請輸入參數:");
       showLogMessage("I","","PARM 1 : [business_date]");
       return(1);
      }

   if ( args.length == 1 )
      { businessDate = args[0]; }
   
   if ( !connectDataBase() ) exitProgram(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   comb = new CommBonus(getDBconnect(),getDBalias());

   selectPtrBusinday();
   tohistDate =comm.lastMonth(businessDate,18) + businessDate.substring(6,8);
   delayDate =comm.lastMonth(businessDate,3) + businessDate.substring(6,8);
   showLogMessage("I","","=========================================");
   showLogMessage("I","","餘額為零最早移除日期["+ tohistDate +"] 異動日[" + delayDate +"]");
   int n = loadDbmBonusDtl();
   if (n>0)
      {
       selectDbmBonusDtl();
       showLogMessage("I","","轉歷史檔筆數 ["+deleteCnt+"]");
      }
   if (deleteCnt<50000)
      {
       showLogMessage("I","","=========================================");
       tohistDate =comm.lastMonth(businessDate,36) + businessDate.substring(6,8);
       showLogMessage("I","","=========================================");
       showLogMessage("I","","餘額為零最早移除日期["+ tohistDate +"]");
       selectDbmBonusDtl1();
      }
   showLogMessage("I","","轉歷史檔筆數 ["+deleteCnt+"]");
   showLogMessage("I","","=========================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void selectPtrBusinday() throws Exception
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
      businessDate =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+ businessDate +"]");
 }
// ************************************************************************ 
 void selectDbmBonusDtl() throws Exception
 {
  selectSQL = "id_p_seqno,"
            + "end_tran_bp,"
            + "link_seqno,"
            + "rowid as rowid";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where acct_date   < ? "
            + "and   to_char(mod_time,'yyyymmdd') < ? "
            + "and   end_tran_bp = 0 "
            ;

  setString(1 , tohistDate);
  setString(2 , delayDate);

  openCursor();

  totalCnt=0;
  int inti=0;
  while( fetchTable() ) 
    { 
     setValue("ddtl.id_p_seqno",getValue("id_p_seqno"));
     int cnt1 = getLoadData("ddtl.id_p_seqno");
     if (cnt1 ==0 ) continue;

     insertDbmBonusDtlHst(inti);
     deleteDbmBonusDtl(inti);
     deleteCnt++;
    if (deleteCnt%10000==0) 
       {
        showLogMessage("I","","轉歷史檔筆數 ["+deleteCnt+"]");
        countCommit();
       }

    if (deleteCnt>=50000) 
       {
        showLogMessage("I","","每日轉歷史最多 50,000 筆");
        break;
       }
    } 
  closeCursor();
 }
// ************************************************************************
 void selectDbmBonusDtl1() throws Exception
 {
  selectSQL = "id_p_seqno,"
            + "end_tran_bp,"
            + "rowid as rowid";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where acct_date   < ? "
            + "and   to_char(mod_time,'yyyymmdd') < ? "
            + "and   end_tran_bp = 0 "
            + "fetch first 50000 rows only"
            ;

  setString(1 , tohistDate);
  setString(2 , delayDate);

  openCursor();

  int inti=0;
  while( fetchTable() ) 
    { 
     insertDbmBonusDtlHst(inti);
     deleteDbmBonusDtl(inti);
     deleteCnt++;
    if (deleteCnt%10000==0) 
       {
        showLogMessage("I","","轉歷史檔筆數 ["+deleteCnt+"]");
        countCommit();
       }

    if (deleteCnt>=50000) 
       {
        showLogMessage("I","","每日轉歷史最多 50,000 筆");
        break;
       }
    } 
  closeCursor();
 }
// ************************************************************************
 void insertDbmBonusDtlHst(int inti) throws Exception
 {
  sqlCmd    = "insert into dbm_bonus_dtl_hst "
            + "select * from dbm_bonus_dtl "
            + "WHERE rowid = ? ";

  daoTable  = "dbm_bonus_dtl_hst";

  setRowId(1,getValue("rowid",inti));

  insertTable();

  return;
 }
// ************************************************************************
 void deleteDbmBonusDtl(int inti) throws Exception
 {
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "WHERE rowid  = ? ";

  setRowId(1,getValue("rowid",inti));

  deleteTable();

  return;
 }
// ************************************************************************
 int loadDbmBonusDtl() throws Exception
 {
  extendField = "ddtl.";
  selectSQL = "id_p_seqno";
  daoTable  = "dbm_bonus_dtl";
  whereStr  = "where acct_date < ? "
            + "group by id_p_seqno "
            + "having sum(end_tran_bp) = 0 " 
            + "fetch first 30000 rows only " 
            ;

  setString(1 , tohistDate);

  int  n = loadTable();

  setLoadData("ddtl.id_p_seqno");

  showLogMessage("I","","Load dbm_bonus_dtl Count: ["+n+"]");
  return(n);
 }
// ************************************************************************ 

}  // End of class FetchSample
