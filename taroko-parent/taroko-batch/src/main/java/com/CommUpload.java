/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 109/06/24  V1.00.07  Allen Ho   new                                        *
* 110/01/18  V1.00.08  Justin         fix error
* 111/06/16  V1.00.09  Justin      弱點修正                                  *
******************************************************************************/
package com;

import java.sql.*;
import java.util.Locale;


@SuppressWarnings({"unchecked", "deprecation"})
public class CommUpload extends AccessDAO
{
  CommFunction comm = new CommFunction();

  int DEBUG_S = 1;
  public String modPgm     = ""; 
  public String modUser    = ""; 
  public String updateFlag = "Y"; 


  int  totalCnt=0;
  int  errorCnt=0;
  String fileType="";

  String[] DBNAME = new String[10];
// ************************************************************************
 public  CommUpload(Connection conn[],String[] dbAlias) throws Exception
 {
   super.conn  = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);

   DBNAME[0]=dbAlias[0];

   return;
 }
// ************************************************************************
 public int uploadfileCtl(String trans_seqno) throws Exception
 {
  extendField = "updf.";
  selectSQL = "trans_seqno,"
            + "delete_flag, "
            + "delete_cond, " 
            + "type_name,"
            + "file_name,"
            + "rowid as rowid";
  daoTable  = "mkt_uploadfile_ctl";
  whereStr  = "where trans_seqno = ? ";

  setString(1 , trans_seqno);

  int recCnt = selectTable();

   if (recCnt==0) return(1);

  if (modPgm.length()==0) modPgm = "CommUpload";

  if (modUser.length()==0) modUser = modPgm;

  for (int inti=0;inti<recCnt;inti++)
    {
     showLogMessage("I","","[CommUpload]=========================================");
     showLogMessage("I","","[CommUpload] 關聯序號["+ getValue("updf.trans_seqno",inti) +"]");

     select_mkt_uploadfile_data(inti);

     showLogMessage("I","","[CommUpload]=========================================");

     setValue("error_memo" ,"處理核銷 ["+totalCnt +"]筆");
     showLogMessage("I","","[CommUpload] 處理核銷 ["+totalCnt +"]筆"); 

     delete_mkt_uploadfile_data(inti);
     if (updateFlag.equals("Y"))
        update_mkt_uploadfile_ctl(inti);
    }
  return(0);
 }
// ************************************************************************
 void  select_mkt_uploadfile_data(int inti) throws Exception
 {
  selectSQL = "data_column01, "
            + "data_data01," 
            + "table_name ";
  daoTable  = "mkt_uploadfile_data";
  whereStr  = "where trans_seqno = ? "
            ;
  setString(1 , getValue("updf.trans_seqno",inti));

  openCursor();

  totalCnt = 0;
  int cnt1=0;
  while( fetchTable() ) 
   {
    totalCnt++;

    insert_new_table();

   } 
  closeCursor();
 }
// ************************************************************************
 int insert_new_table() throws Exception 
 {
  String   stra="",strb="";

  long list_cnt = getValue("data_column01").chars().filter(ch -> ch =='|').count();
  int arrSize = (int)list_cnt - 1;

  for (int inti=0;inti<list_cnt;inti++)
    {
     dateTime();
     stra = CommFunction.getStr(getValue("data_column01"), inti+1 ,"|");
     if (stra.length()==0) continue;
     strb = CommFunction.getStr(getValue("data_data01"), inti+1 ,"|");

     if  (strb.equals("\"\"")) strb="";
          
     int intk = strb.toUpperCase().indexOf("DATE_DATATYPE=");
     if (intk!=-1)
         {
          strb  = strb.substring(intk+14);
         }
     else if  (strb.toUpperCase(Locale.TAIWAN).equals("SYSTEMDEFAULT"))
         {
          if (stra.toUpperCase(Locale.TAIWAN).equals("APR_FLAG"))
             strb  = "Y";
          if ((stra.toUpperCase(Locale.TAIWAN).equals("APR_USER"))||
              (stra.toUpperCase(Locale.TAIWAN).equals("MOD_USER")))
              strb = modUser;
          if ((stra.toUpperCase(Locale.TAIWAN).equals("APR_DATE"))||
              (stra.toUpperCase(Locale.TAIWAN).equals("CRT_DATE")))
              strb = sysDate;
          if (stra.toUpperCase(Locale.TAIWAN).equals("MOD_PGM"))
              strb = modPgm;
          if (stra.toUpperCase(Locale.TAIWAN).equals("MOD_TIME"))
              strb = sysDate+sysTime;
         }
     setValue(stra    , strb);
    }

  daoTable  = getValue("table_name");

  int n = insertTable();

   if ( dupRecord.equals("Y") ) 
      {
       showLogMessage("I","","[CommUpload] ["+ getValue("data_data01") +"] error");
       return(1);
      }  

  return(0);
 }
// ************************************************************************
 int update_mkt_uploadfile_ctl(int inti) throws Exception
 {
  dateTime();
  daoTable  = "mkt_uploadfile_ctl";
  updateSQL = "apr_user  = ?,"
            + "apr_date  = ?,"
            + "apr_flag  = 'Y',"
            + "mod_user  = ?, "
            + "mod_pgm   = ?,"
            + "mod_time  = sysdate";   
  whereStr  = "where rowid  = ? "
            ;

  setString(1 , modUser);
  setString(2 , sysDate);
  setString(3 , modUser);
  setString(4 , modPgm);
  setRowId(5 , getValue("updf.rowid",inti));

  int n = updateTable();

  return n;
 }
// ************************************************************************
 void delete_mkt_uploadfile_data(int inti) throws Exception 
 {
  daoTable  = "mkt_uploadfile_data";
  whereStr  = "where trans_seqno = ? "
            ;
  setString(1 , getValue("updf.trans_seqno",inti));

  deleteTable();

  return;
 }
// ************************************************************************


}   // End of class CommExchg
