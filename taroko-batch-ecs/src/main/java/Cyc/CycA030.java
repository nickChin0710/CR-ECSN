/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/01/02  V1.00.05  Allen Ho   cyc_a105 V1.00.09 103/10/20                *
* 109/11/27  V1.00.06  Zuwei      naming rule update                         *
* 109-12-17   V1.00.07  tanwei      updated for project coding standard      *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycA030 extends AccessDAO
{
 private final String progname = "關帳-帳務主檔備份處理程式 109/12/17 V1.00.07";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String   hBusiBusinessDate  = "";
 String   hWdayStmtCycle     =  "";

 String[] mainTables = {"ACT_ACNO","ACT_ACCT","ACT_ACCT_CURR","ACT_ACAG","ACT_ACAG_CURR"};
 int     checkCnt =0, updateCnt=0;
 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycA030 proc = new CycA030();
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
  
   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() ) return(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();
  
   if (selectPtrWorkday()!=0)
      {
       showLogMessage("I","","本日非關帳日, 不需執行");
       return(0);
      }

    showLogMessage("I","","STMT_CYCLE["+hWdayStmtCycle+"]");
    showLogMessage("I","","====================================");
    showLogMessage("I","","清除備份檔 開始");

    for (int inti=0;inti<mainTables.length;inti++)
        {
         commitDataBase();
         truncateTable(inti,hWdayStmtCycle);
        }

    showLogMessage("I","","清除備份檔 結束");
    commitDataBase();
    showLogMessage("I","","====================================");
    showLogMessage("I","","備份主檔 開始");
    for (int inti=0;inti<mainTables.length;inti++)
      {
       showLogMessage("I","","   備份 "+mainTables[inti]+"_"+hWdayStmtCycle);
       selectMainTables(mainTables[inti]);
       showLogMessage("I","","     Total process records["+totalCnt+"]");
      }
    showLogMessage("I","","備份主檔 結束");
    showLogMessage("I","","====================================");

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
  daoTable   = "PTR_BUSINDAY";
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
 public int  selectPtrWorkday() throws Exception
 {
  selectSQL = "";
  daoTable  = "PTR_WORKDAY";
  whereStr  = "WHERE THIS_CLOSE_DATE = ? ";
  setString(1,hBusiBusinessDate);

  int recCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);
 
  hWdayStmtCycle      =  getValue("STMT_CYCLE");

  return(0);
 }
// ************************************************************************
public void  selectMainTables(String tableName) throws Exception
 {
  selectSQL = "";
  daoTable  = tableName;
  if (tableName.equals("ACT_ACNO"))
    {
     whereStr  = "WHERE STMT_CYCLE = ? ";
     setString(1, hWdayStmtCycle);
    }
  else
    {
     whereStr  = "WHERE p_seqno in (select p_seqno from act_acno_"+hWdayStmtCycle+")";
    }

  String tabName = tableName+"_"+hWdayStmtCycle;
  openCursor();
  totalCnt=0;
  while( fetchTable() )
    {
     totalCnt++;
     insertMainTables(tabName);
    }
    
  closeCursor();
 }
// ************************************************************************
 public void  insertMainTables(String tableName) throws Exception
 {
  daoTable  = tableName;

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","insert " +tableName +" error[dupRecord]");
      return;
     }
 }
// ************************************************************************
 int truncateTable(int inti,String stmtCycle) throws Exception
 {
  showLogMessage("I","","   刪除 " + mainTables[inti] + "_" + stmtCycle);
  String trunSQL = "TRUNCATE TABLE "+ mainTables[inti] + "_" + stmtCycle + " "
                 + "IGNORE DELETE TRIGGERS "
                 + "DROP STORAGE "
                 + "IMMEDIATE "
                 ;

  showLogMessage("I","","     ["+ trunSQL + "]");

  executeSqlCommand(trunSQL);

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

