/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/01/29  V1.00.00  Allen Ho   sms_e030                                   *
* 109-11-18  V1.00.01  tanwei     updated for project coding standard        *
* 112/02/06  V1.00.02  Zuwei Su   update naming rule                         *
* 112/08/31  V1.00.03  sunny      move files to backup folder                *
******************************************************************************/
package Sms;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.Normalizer;

@SuppressWarnings("unchecked")
public class SmsE030 extends AccessDAO
{
 private final String PROGNAME = "電子發票-中獎信用卡載具清冊媒體轉入處理程式 112/08/31  V1.00.03";
 CommFunction comm = new CommFunction();
 CommRoutine comr = null;

 String hBusiBusinessDate = "";
 String hTempSysdate       = "";
 int    lineSeq      = 0;

 String readData="";                       
 int fi = 0;
 int[] begPos = {0};
 long    totalCnt=0,errorCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  SmsE030 proc = new SmsE030();
  int  retCode = proc.mainProcess(args);
  System.exit(retCode);
 }
// ************************************************************************
 public int mainProcess(String[] args) {
  try
  {
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I","",javaProgram+" "+PROGNAME);
  
    if (comm.isAppActive(javaProgram)) 
       return(1);

   if ( args.length == 1 ) 
       if (args[0].length()==8) 
    	   hTempSysdate = args[0];
   
   showLogMessage("I","","參數日期 : ["+hTempSysdate+"]");

   if ( !connectDataBase() ) exitProgram(1);
   comr = new CommRoutine(getDBconnect(),getDBalias());

   if (hTempSysdate.length()==0) {
	   selectPtrBusinday();
       showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
  }
   else
   {
	   hBusiBusinessDate=hTempSysdate;
       showLogMessage("I","","指定日期: ["+hBusiBusinessDate+"]");
   }
  
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始處理檔案.....");
   selectEcsFtpLog();
   showLogMessage("I","","===============================");
  
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
            + "to_char(sysdate,'yyyymmdd') AS FILE_DATE ";
  daoTable  = "PTR_BUSINDAY";
  whereStr  = "FETCH FIRST 1 ROW ONLY";

  int recordCnt = selectTable();
    
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select ptr_businday error!" );
      exitProgram(1);
     }
    hBusiBusinessDate   =  getValue("BUSINESS_DATE");
  showLogMessage("I","","本日營業日 : ["+hBusiBusinessDate+"]");
 }
// ************************************************************************
 void  selectEcsFtpLog() throws Exception
 {
  selectSQL  = "file_name, "
             + "local_dir, "
             + "file_date, "
             + "group_id, "
             + "trans_seqno, "
             + "rowid as rowid";
  daoTable   = "ecs_ftp_log";
  //whereStr   = "WHERE system_id  = 'SMS_FTP_GET' "
  whereStr   = "WHERE REF_IP_CODE= 'SMS_FTP_GET' "
		     + "AND  group_id in ('01','02')"
             + "AND   proc_code  = '0' "  
             + "AND   trans_mode = 'RECV' "
             ;

  if (hTempSysdate.length()!=0)
     {
      showLogMessage("I","","依指定日期["+ hBusiBusinessDate +"]執行.....");
      //whereStr = whereStr + "and  crt_date   = ? ";
      whereStr = whereStr + "and  file_date   = ? ";
      setString(1 , hBusiBusinessDate);                 
     }
  else
     {
      showLogMessage("I","","依傳輸記錄執行.....");
     }

  whereStr = whereStr + "ORDER BY file_date,crt_date,crt_time" ;

  int recCnt = selectTable();

  if (recCnt==0) 
     {
      showLogMessage("I","","本日 ["+hBusiBusinessDate+"] 無傳輸記錄可執行.");
      return;
     }

  for (int inti=0;inti<recCnt;inti++)
   {
    setValue("file_name"   , getValue("file_name",inti));
    setValue("local_dir"   , getValue("local_dir",inti));
    setValue("file_date"   , getValue("file_date",inti));
    setValue("group_id"    , getValue("group_id",inti));
    setValue("trans_seqno" , getValue("trans_seqno",inti));
    setValue("rowid"       , getValue("rowid",inti));

    showLogMessage("I","","處理檔案["+getValue("file_name")+"].....");

    openFile();

    setValue("proc_code" , "N");
    updateEcsFtpLog();
    renameFile(getValue("file_name"));
    commitDataBase();
   }
 }
// ************************************************************************
 void  openFile() throws Exception
 {
  String checkHome = System.getenv("PROJ_HOME");
  String fileName = checkHome + "/media/sms/"+getValue("file_name");
  fi = openInputText(fileName,"MS950");
  if (fi == -1 ) 
     {
       setValue("noti.notify_head"      , "媒體檔無開啟權限");
       setValue("noti.notify_name"      , "媒體檔名:"+ getValue("file_name"));
       setValue("noti.notify_desc1"     , getValue("file_name")+" 權限不足, 無法開啟");
       setValue("noti.notify_desc2"     , "");
       insertEcsNotifyLog();
       renameFile(getValue("file_name"));
       showLogMessage("I","", getValue("noti.notify_head")+" 不處理...");
       return;
     }
  setValue("file_desc" , "檔案處理中");
  commitDataBase();

  lineSeq=0;
  errorCnt=0;
  int    fileCnt =0;
  totalCnt=0;
  int errFlag=0;
  int dupFlag=0;
  while ( true )
   {
    readData = readTextFile(fi).replace("^\n","").replace("^\r","").replace("\t", "").trim();
    if ( endFile[fi].equals("Y") ) break;
    lineSeq++;
    if ( readData.length()<2 ) continue; 

    begPos[0] = 1;
    errFlag=0;
    if (lineSeq==1)
       {
        setValue("semt.file_date"      , comm.structStr(readData,begPos,8));
        setValue("semt.file_time"      , comm.structStr(readData,begPos,6));
        setValue("semt.query_records"  , comm.structStr(readData,begPos,10));
        setValue("semt.beg_stage"      , comm.structStr(readData,begPos,8));
        setValue("semt.end_stage"      , comm.structStr(readData,begPos,8));
        setValue("semt.result_records" , comm.structStr(readData,begPos,10));
   
        if (insertSmsEinvoMst()!=0)
           {
            setValue("proc_desc"  , "ERROR:檔案資料料重複, 資料不寫入");
            showLogMessage("I","", getValue("proc_desc"));
            return;
           }
        continue;
       }

    setValue("semt.win_stage"     , comm.structStr(readData,begPos,8));
    setValue("semt.card_encr"     , comm.structStr(readData,begPos,64));
    setValue("semt.card_desc"     , comm.structStr(readData,begPos,64));

    setValue("enc_card_no"    , getValue("semt.card_encr").substring(6,getValue("semt.card_encr").length()).trim());

    if (selectSmsEinvoCard()!=0)
       {
        errorCnt++;
        if (errorCnt>100) continue;
        showLogMessage("I","","Line ["+(lineSeq-1)+"] 找不到對應卡號["+getValue("enc_card_no")+"]");

        setValue("elog.main_desc"        , "資料內容錯誤");
        setValue("elog.error_seq"        , "2");
        setValue("elog.column_seq"       , "2");
        setValue("elog.column_data"      , "卡號");
        setValue("elog.error_desc"       , "找不到對應卡號");
        setValue("elog.column_desc"      , "解碼後卡檔找不到");
        insertEcsMediaErrlog();
        continue;
       }

    setValue("semt.vd_flag" , "0");
    if (selectCrdCard()!=0) selectDbcCard();

/*
    if (getValue("group_id").substring(0,2).equals("01"))
       {
        insertSmsEinvoDtl();
       }
    else
       {
        if (updateSmsEinvoDtl()!=0)
           insertSmsEinvoDtl();
       }
*/
   
    //收到01的檔案(中獎信用卡載具清冊檔)       insert
    //收到02的檔案(未列印中獎信用卡載具清冊檔) update
    
    if (getValue("group_id").substring(0,2).equals("02"))
    {
      if (updateSmsEinvoDtl()!=0)
    	  insertSmsEinvoDtl();
    }
    else
    {
        insertSmsEinvoDtl();  //非02的檔一律視同01
    }
    
   }

  if (lineSeq!=Integer.valueOf(getValueInt("semt.query_records"))+1)
     {
      showLogMessage("I","","ERROR:資料筆數與實際筆數不符["+
                            (lineSeq-1)+"]!=["+(getValueInt("semt.query_records")+1)+"]");

      setValue("elog.main_desc"        , "資料內容錯誤");
      setValue("elog.error_seq"        , "2");
      setValue("elog.column_seq"       , "6");
      setValue("elog.column_data"      , "資料筆數");
      setValue("elog.error_desc"       , "資料筆數與實際筆數不符");
      setValue("elog.column_desc"      , "實際筆數"+totalCnt+"] 媒體標示筆數["+getValue("semt.query_records")+1+"]" );
      insertEcsMediaErrlog();
      setValue("proc_desc"  , "ERROR:資料筆數與實際筆數不符");
      return;
     }
/*
  if (errFlag!=0)
     {
      showLogMessage("I","","ERROR:檔案資料有錯, 資料不寫入");

      setValue("noti.notify_head"      , "媒體檔轉入資料有誤(只記錄前100筆)");
      setValue("noti.notify_name"      , "媒體檔名:"+getValue("file_name"));
      setValue("noti.notify_desc1"     , "程式 "+javaProgram+" 轉 "+ getValue("file_name") +" 有"+errorCnt+" 筆錯誤");
      setValue("noti.notify_desc2"     , "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤");
      insert_ecs_notify_log();
      setValue("proc_desc"  , "ERROR:檔案資料有錯, 資料不寫入");
      return;
     }
*/
  setValue("proc_desc"  , "+累計讀取["+(lineSeq)+"]筆, 錯誤["+errorCnt+"]");
  showLogMessage("I","", getValue("proc_desc"));
 }
// ************************************************************************
 int  insertSmsEinvoMst() throws Exception
 {
  extendField = "semt.";
  setValue("semt.trans_seqno"    , getValue("trans_seqno"));
  setValue("semt.group_id"       , getValue("group_id"));
  setValue("semt.crt_date"       , sysDate);
  setValue("semt.crt_time"       , sysTime);
  setValue("semt.mod_time"       , sysDate+sysTime);
  setValue("semt.mod_pgm"        , javaProgram);

  daoTable = "sms_einvo_mst";

  insertTable();

  if ( dupRecord.equals("Y") ) return(1);
  return(0);
 }
// ************************************************************************
 int  selectSmsEinvoCard() throws Exception
 {
  extendField = "invo.";
  selectSQL = "card_no ";
  daoTable  = "sms_einvo_card";
  whereStr  = "where enc_card_no = ?";
 
  setString(1 , getValue("enc_card_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 int  selectCrdCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "id_p_seqno, "
            + "p_seqno,    "
            + "acct_type ";
  daoTable  = "crd_card";
  whereStr  = "where card_no = ?";
 
  setString(1,getValue("invo.card_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") ) return(1);

  setValue("semt.vd_flag" , "N");
  return(0);
 }
// ************************************************************************
 void  selectDbcCard() throws Exception
 {
  extendField = "card.";
  selectSQL = "id_p_seqno, "
            + "p_seqno,   "
            + "acct_type  ";
  daoTable  = "dbc_card";
  whereStr  = "where card_no = ?";
 
  setString(1, getValue("invo.card_no"));

  int recordCnt = selectTable();

  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","select dbc_card error!" );
      exitProgram(1);
     }
  setValue("semt.vd_flag" , "Y");
 }
// ************************************************************************
 void  insertSmsEinvoDtl() throws Exception
 {
  String win_stage = String.format("%03d", Integer.valueOf(getValue("semt.win_stage").substring(0,4))-1911);
  switch (getValue("semt.win_stage").substring(4,8))
   {
    case "0102" :setValue("semt.lottery_info" , win_stage+"/3/25");
                 setValue("semt.award_info"   , win_stage+"/4/6~"
                                              + win_stage+"/7/5");
                 break;
    case "0304" :setValue("semt.lottery_info" , win_stage+ "/5/25");
                 setValue("semt.award_info"   , win_stage+"/6/6~"
                                              + win_stage+"/9/5");
                 break;
    case "0506" :setValue("semt.lottery_info" , win_stage+ "/7/25");
                 setValue("semt.award_info"   , win_stage+"/8/6~"
                                              + win_stage+"/11/5");
                 break;           
    case "0708" :setValue("semt.lottery_info" , win_stage+ "/9/25");
                 setValue("semt.award_info"   , win_stage+"/10/6~"
                                              + Integer.toString(Integer.valueOf(win_stage)+1)
                                              + "/1/5");
                 break;             
    case "0910" :setValue("semt.lottery_info" , win_stage + "/11/25");
                 setValue("semt.award_info"   , win_stage+"/12/6~"
                                              + Integer.toString(Integer.valueOf(win_stage)+1)
                                              + "/3/5");
                 break;
    case "1112" :setValue("semt.lottery_info" , Integer.toString(Integer.valueOf(win_stage)+1)
                                              + "/1/25");
                 setValue("semt.award_info"   , Integer.toString(Integer.valueOf(win_stage)+1)
                                              + "/2/6~"
                                              + Integer.toString(Integer.valueOf(win_stage)+1)
                                              + "/5/5");
                 break;
   }

  extendField = "semt.";
  setValue("semt.card_no"        , getValue("invo.card_no"));
  setValue("semt.id_p_seqno"     , getValue("card.id_p_seqno"));                 
  setValue("semt.acct_type"      , getValue("card.acct_type"));                 
  setValue("semt.p_seqno"        , getValue("card.p_seqno"));                 
  setValue("semt.proc_date"      , hBusiBusinessDate);                 
  setValue("semt.proc_flag"      , "N");
  setValue("semt.trans_seqno"    , getValue("trans_seqno"));
  setValue("semt.sms_flag"       , "N");
  setValue("semt.crt_user"       , "SmsE030_"+getValue("group_id").substring(0,2)); //帶入檔名01、02方便後續處理判斷
  setValue("semt.crt_date"       , sysDate);
  setValue("semt.crt_time"       , sysTime);
  setValue("semt.mod_time"       , sysDate+sysTime);
  setValue("semt.mod_pgm"        , javaProgram);

  daoTable = "sms_einvo_dtl";

  insertTable();

  if ( dupRecord.equals("Y") )
     {
      showLogMessage("I","","CARD_NO["+getValue("semt.card_no")+"]["+getValue("semt.win_stage")+"] 多次中獎");
     }

  return;
 }
// ************************************************************************
 int updateSmsEinvoDtl() throws Exception
 {
  updateSQL = "trans_seqno_1 = ?, "
            + "proc_date     = ?, "
            + "mod_pgm       = ?, "
            + "mod_time      = sysdate";  
  daoTable  = "sms_einvo_dtl";
  whereStr  = "where card_no   = ? "
            + "and   win_stage = ? ";

  setString(1 , getValue("trans_seqno"));
  setString(2 , hBusiBusinessDate);
  setString(3 , javaProgram);
  setString(4 , getValue("invo.card_no"));
  setString(5 , getValue("semt.win_stage"));

  int cnt = updateTable();
  if ( notFound.equals("Y") ) return(1);

  return(0);
 }
// ************************************************************************
 void updateEcsFtpLog() throws Exception
 {
  dateTime();
  updateSQL = "proc_code  = ?, "
            + "trans_desc = trans_desc||?, "
            + "proc_desc  = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "WHERE rowid = ?";

  setString(1 , getValue("proc_code"));
  setString(2 , getValue("proc_desc"));
  setString(3 , getValue("proc_desc"));
  setString(4 , javaProgram);
  setRowId(5  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE ecs_ftp_log error "+getValue("rowid")); 
      exitProgram(0); 
     }
  return;
 }
// ************************************************************************
 int insertEcsMediaErrlog() throws Exception
 {
  dateTime();
  extendField = "elog.";
  setValue("elog.crt_date"           , sysDate);
  setValue("elog.crt_time"           , sysTime);
  setValue("elog.file_name"          , getValue("file_name"));
  setValue("elog.unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValueInt("elog.line_seq"        , lineSeq);
  setValue("elog.trans_seqno"        , getValue("trans_seqno"));
  setValue("elog.program_code"       , javaProgram);
  setValue("elog.mod_time"           , sysDate+sysTime);
  setValue("elog.mod_pgm"            , javaProgram);

  daoTable  = "ecs_media_errlog";

  insertTable();

  return(0);
 }
// ************************************************************************
 int insertEcsNotifyLog() throws Exception
 {
  dateTime();
  extendField = "noti.";
  setValue("noti.crt_date"           , sysDate);
  setValue("noti.crt_time"           , sysTime);
  setValue("noti.unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValue("noti.obj_type"           , "3");
  setValue("noti.trans_seqno"        , getValue("trans_seqno"));
  setValue("noti.mod_time"           , sysDate+sysTime);
  setValue("noti.mod_pgm"            , javaProgram);

  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
// ************************************************************************
 void renameFile(String renameFileName) throws Exception
 {
  String tmpstr1 = getValue("local_dir")+"/"+renameFileName;
         tmpstr1 = Normalizer.normalize(tmpstr1, Normalizer.Form.NFD);
  File oldName   = new File(tmpstr1);
  String tmpstr2 = getValue("local_dir")+"/backup/"+renameFileName+"."+sysDate;
         tmpstr2 = Normalizer.normalize(tmpstr2, Normalizer.Form.NFD);
  File newName = new File(tmpstr2);

  if (!oldName.renameTo(newName))
     {
      showLogMessage("I","","ERROR : 檔案["+tmpstr1+"] ==> ["+tmpstr2+"]更名失敗!");
      return;
     }
  showLogMessage("I","","檔案 ["+renameFileName+"] 已移至 ["+tmpstr2+"]");
 } 
// ************************************************************************

}  // End of class FetchSample

