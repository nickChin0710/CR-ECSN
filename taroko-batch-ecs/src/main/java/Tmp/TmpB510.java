/*****************************************************************************
*                                                                                                      *
*                              MODIFICATION LOG                                           *
*                                                                                                      *
*    DATE    Version    AUTHOR              DESCRIPTION                         *
*  --------  -------------------  ----------------------------------------------- *
* 111/07/04  V1.00.01  JeffKung   For csv to db2                                 *
*                                                                                                      *
******************************************************************************/
package Tmp;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class TmpB510 extends AccessDAO
{
 private  String PROGNAME = "從匯出的資料(CSV)轉入資料庫 111/07/04 V1.00.01";
 CommFunction comm = new CommFunction();
 
 List<String> tabFieldName = new ArrayList<String>();
 List<String> tabFieldType = new ArrayList<String>();

 String dbNameFrom = "",dbNameTo="";

 int fi,fo;
 int totalCnt=0 , realCnt=0;
 String fileName = "",readData="",outData="",tabName="",tabChiName="",indName="";
 String hFileName = "" , hTableName = "";
 int recProcFrom = 0 , recProcCnt = 0;
 String   newLine="\n";
 String checkHome = "";
 String whereCond = "";

// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  TmpB510 proc = new TmpB510();
  proc.mainProcess(args);
  return;
 }
// ************************************************************************
 public void mainProcess(String[] args) {
	 try
	 {
		 dateTime();
		 setConsoleMode("Y");
		 javaProgram = this.getClass().getName();
		 showLogMessage("I","",javaProgram+" "+PROGNAME);
		 checkHome = System.getenv("PROJ_HOME");
 
		 if ((args.length < 1 )&&(args.length > 4 ))
		 {
			 showLogMessage("I","","請輸入Table name, File name, Proc From Record, Proc Record Cnt ... ");
			 exitProgram(1);
		 }

		 hTableName = args[0].toUpperCase(); 
		 hFileName = args[1];
   
		 if (args.length >= 3 )
			 recProcFrom = Integer.parseInt(args[2]);
   
		 if (args.length == 4 )
			 recProcCnt = Integer.parseInt(args[3]);
      
		 if ( !connectDataBase(dbNameTo) )
		 { return; }

		 read_file();
		 
		 finalProcess();
	 }

	 catch ( Exception ex )
	 { expMethod = "mainProcess";  expHandle(ex);  return;  }

 } // End of mainProcess
 
//************************************************************************
 	public void  read_file() throws Exception
 	{
 		String readData = "";

 		showLogMessage("I","","媒體檔位置["+hFileName+"]");
 		fi = openInputText(hFileName);
 		if ( fi == -1 )
 		{
 			showLogMessage("I","","ERROR:媒體檔目錄下沒有權限讀寫");
 			commitDataBase();
 			exitProgram(1);
 		}

 		int fieldCnt = 0;
 		totalCnt = 0; 
 		realCnt = 0;
 		while ( true )
 		{
 			readData = readTextFile(fi).trim();
 			
 			if ( endFile[fi].equals("Y")) break;
 			if (readData.length()<10) continue; 

 			totalCnt++;
  
 			if (totalCnt == 1) {
 				String[] fieldNameArr = readData.replaceAll("\"","").split("\\|\\|");
 				fieldCnt = fieldNameArr.length;
 				for ( int inti=0; inti<fieldCnt; inti++ )
 				{
 					//showLogMessage("I","","FieldName :  "+fieldNameArr[inti]);
 					tabFieldName.add(fieldNameArr[inti].trim());
 					getFieldType(fieldNameArr[inti].trim(),inti);
 				}
 				continue;
 			}
 			
 			//從第幾筆開始處理
 			if (recProcFrom > 0 && (totalCnt+1) < recProcFrom) {
 				continue;
 			}
  
 			//處理幾筆
 			if (recProcCnt > 0 && realCnt > recProcCnt) {
 				break;
 			}
 			
 			realCnt ++;
 			
 			//在每列最後加上1欄結束符號"||E"
 			readData+= "||E";
 			
 			String[] dataArr = readData.split("\\|\\|");
 			String fieldData = "";
 			for ( int inti=0; inti<fieldCnt; inti++ )
 			{
 				fieldData =  dataArr[inti].trim();
 				if (fieldData.equals("\"\"")) {
 					fieldData = "";
 				}
	  
 	  			if (tabFieldType.get(inti).equals("TIMESTAMP")) {
 	  				//showLogMessage("I","","tabFieldName :  ["+tabFieldName.get(inti)+"]");
 	  				//showLogMessage("I","","fieldData :  ["+String.format("%.14s", fieldData.replaceAll("\\-|\\.", ""))+"]");
 	  				setValue(tabFieldName.get(inti),new String(String.format("%.14s", fieldData.replaceAll("\\-|\\.", ""))));
	  			} else {
	  				setValue(tabFieldName.get(inti),fieldData);
	  			}
	  
 			}
 			
 		      insertEcsTable();
 		      if (realCnt%10000==0) 
 		         {
 		          showLogMessage("I","","Process Count :  "+realCnt);
 		          dataBase   = dbNameTo;
 		          countCommit();
 		         }
  
 		}

 		closeInputText(fi);
 		showLogMessage("I","","檔案轉入 ["+ realCnt + "] 筆");
	}

	public void getFieldType(String fieldName, int fieldCnt) throws Exception
	{
		
		 selectSQL = "TYPENAME";
		 daoTable  = "SYSCAT.COLUMNS";
		 whereStr  = "WHERE TABSCHEMA in ( 'ECSDCDB','ECSCRDB', 'ECSUTDB' )  AND TABNAME = ? AND COLNAME = ?  FETCH FIRST 1 ROWS ONLY";

		  setString(1, hTableName);
		  setString(2, fieldName);

		  int recCnt = selectTable();

		  if ( notFound.equals("Y") ) {
			  showLogMessage("I","","TableName,FieldName Not Found : [" +hTableName +","+ fieldName + "]");
			  //exitProgram(1);
		  }
		  
		  tabFieldType.add(getValue("TYPENAME"));
	}
	
 // ************************************************************************
 	public void  insertEcsTable() throws Exception
 	{
 		dataBase  = dbNameTo;
 		daoTable  = hTableName;

 		insertTable();

 		if ( dupRecord.equals("Y") )
 		{ 
 			showLogMessage("I","","insert_ecs_table error[dupRecord]");
 			return;
 		}
 		return;
 	}
}  // End of class 

