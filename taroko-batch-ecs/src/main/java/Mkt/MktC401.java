/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 112/07/06  V1.00.01  Allen Ho   new                                        *
*                                                                            *
******************************************************************************/
package Mkt;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.nio.*;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class MktC401 extends AccessDAO
{
 private  String PROGNAME = "首刷禮-指定營業日區間處理程式 112/07/06 V1.00.01";
 CommFunction comm = new CommFunction();

 String startDate = "";
 String endDate   = "";
 String tempDate   = "";
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktC401 proc = new MktC401();
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
       showLogMessage("I","","PARM 1 : [start_date]");
       showLogMessage("I","","PARM 2 : [end_date]");
       return(1);
      }

   if ( args.length == 2 )
      { 
       startDate = args[0]; 
       endDate   = args[1]; 
      }
   else
      {
       selectPtrFstpdate();
       startDate = getValue("start_date"); 
       endDate   = getValue("end_date");
      }

  showLogMessage("I","","執行營業日 : ["+ startDate +"]-[" + endDate +"]");

   if ( !connectDataBase() ) return(1);


   String[] hideArgs = new String[1];

   tempDate = startDate;
   while (tempDate.compareTo(endDate)<=0)
     {
      commitDataBase();
      showLogMessage("I","","啟動 MktC400 執行後續動作 .....");
      showLogMessage("I","","===============================");
      try {
           hideArgs[0] = tempDate;

           MktC400 mktC400 = new MktC400();
           int rtn = mktC400.mainProcess(hideArgs);
           if(rtn < 0) return (1);
           showLogMessage("I","","MktC400 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex)
               {
                showLogMessage("I","","無法執行 MktC400 ERROR!");
               }

      commitDataBase();
      showLogMessage("I","","啟動 MktC410 執行後續動作 .....");
      showLogMessage("I","","===============================");
      try {
           hideArgs[0] = tempDate;

           MktC410 mktC410 = new MktC410();
           int rtn = mktC410.mainProcess(hideArgs);
           if(rtn < 0) return (1);
           showLogMessage("I","","MktC410 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex)
               {
                showLogMessage("I","","無法執行 MktC410 ERROR!");
               }

      commitDataBase();
      showLogMessage("I","","啟動 MktC420 執行後續動作 .....");
      showLogMessage("I","","===============================");
      try {
           hideArgs[0] = tempDate;

           MktC420 mktC420 = new MktC420();
           int rtn = mktC420.mainProcess(hideArgs);
           if(rtn < 0) return (1);
           showLogMessage("I","","MktC420 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex)
               {
                showLogMessage("I","","無法執行 MktC420 ERROR!");
               }

      commitDataBase();
      showLogMessage("I","","啟動 MktC430 執行後續動作 .....");
      showLogMessage("I","","===============================");
      try {
           hideArgs[0] = tempDate;

           MktC430 mktC430 = new MktC430();
           int rtn = mktC430.mainProcess(hideArgs);
           if(rtn < 0) return (1);
           showLogMessage("I","","MktC430 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex)
               {
                showLogMessage("I","","無法執行 MktC430 ERROR!");
               }

      commitDataBase();
      showLogMessage("I","","啟動 MktC440 執行後續動作 .....");
      showLogMessage("I","","===============================");
      try {
           hideArgs[0] = tempDate;

           MktC440 mktC440 = new MktC440();
           int rtn = mktC440.mainProcess(hideArgs);
           if(rtn < 0) return (1);
           showLogMessage("I","","MktC440 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex)
               {
                showLogMessage("I","","無法執行 MktC410 ERROR!");
               }
      tempDate = comm.nextDate(tempDate);
     }

   finalProcess();
   return(0);
  }

  catch ( Exception ex )
  { expMethod = "mainProcess";  expHandle(ex);  return exceptExit;  }

 } // End of mainProcess
// ************************************************************************
 void  selectPtrFstpdate() throws Exception
 {
  daoTable   = "ptr_fstpdate";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_fstpdate error!" );
      exitProgram(1);
     }

 }
// ************************************************************************ 

}  // End of class FetchSample


