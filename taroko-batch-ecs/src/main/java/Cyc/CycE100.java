/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/03/31  V1.00.06  Allen Ho   cyc_a999 V1.01.01 98/03/04                 *
* 109/03/31  V1.00.07  Zuwei      naming rule                                *
* 112/05/03  V1.00.08  Grace Huang  判讀營業日, 如為休假日者, 傳票日=營業日+2個工作天 (TCB rule) *
*                                                                            *
******************************************************************************/
package Cyc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CycE100 extends AccessDAO
{
 private  String progname = "關帳-每日更新作業日處理程式 109/11/13 V1.00.07";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String   hBusiBusinessDate  = "";
 String   hBusiOnlineDate    = "";
 String   hBusiVouchDate     = "";
 String   hBusiVouchDate_1	 = "";
 String   hBusiVouchDate_2	 = "";
 String   hBusiRowid          =  "";

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  CycE100 proc = new CycE100();
  int retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
  return;
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+progname);
  
   if ( args.length == 1 ) 
      { hBusiBusinessDate = args[0]; }

   if ( !connectDataBase() ) return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());

   selectPtrBusinday();

   hBusiBusinessDate = comm.nextDate(hBusiBusinessDate);
   hBusiOnlineDate   = hBusiBusinessDate;

   comr.increaseDays(hBusiBusinessDate,1);
   hBusiVouchDate = comr.increaseNewDate;
   //判讀營業日, 如為休假日者, 傳票日=營業日+2個工作天 ---------------------------------------
   comr.increaseDays(hBusiVouchDate,-1);
   hBusiVouchDate_1 = comr.increaseNewDate;
   //hBusiVouchDate_2="";
      if (! hBusiVouchDate_1 .equals(hBusiBusinessDate))
		   {
	   comr.increaseDays(hBusiBusinessDate,2);
	   //hBusiVouchDate_2 = comr.increaseNewDate;
	   hBusiVouchDate = comr.increaseNewDate;
	   //showLogMessage("I","","營業日非工作日者,  傳票日(營業日+2工作天): "+hBusiVouchDate_2);
		   }
   //(end) ---------------------------------------------
   
   //showLogMessage("I","","更換營業日 : ["+hBusiBusinessDate+"]");
   //showLogMessage("I","","更換營業日 : ["+hBusiBusinessDate+"]; 傳票日(營業日+1工作天): "+hBusiVouchDate+"; 傳票日(營業日-1工作天): "+hBusiVouchDate_1);
   showLogMessage("I","","更換營業日 : ["+hBusiBusinessDate+"]; 傳票日: ["+hBusiVouchDate+"]");
      
   updatePtrBusinday();

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 public void  selectPtrBusinday() throws Exception
 {
  selectSQL = "business_date, "
            + "rowid as rowid";
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";
  
  int recordCnt = selectTable();
    
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1); 
     }

  if (hBusiBusinessDate.length()==0)
      hBusiBusinessDate  =  getValue("BUSINESS_DATE");

  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************
 void updatePtrBusinday() throws Exception
 {
  updateSQL = "business_date    = ?,"
            + "online_date      = ?,"
            + "vouch_close_flag = 'N',"
            + "vouch_date       = ?,"
            + "mod_pgm          = ?,"
            + "mod_time         = timestamp_format(?,'yyyymmddhh24miss')";
  daoTable  = "ptr_businday";
  whereStr  = "WHERE ROWID = ? ";

  setString(1 , hBusiBusinessDate);
  setString(2 , hBusiOnlineDate);
  setString(3 , hBusiVouchDate);
  setString(4 , javaProgram);
  setString(5 , sysDate+sysTime);
  setRowId(6  , getValue("rowid"));

  int recCnt = updateTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","update_ptr_businday error!" );
      showLogMessage("I","","rowid=["+getValue("rowid")+"]");
      exitProgram(1);
     }
  return;
 }
// ************************************************************************

}  // End of class FetchSample

