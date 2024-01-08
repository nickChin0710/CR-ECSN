/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/08  V1.00.19  Allen Ho   mkt_t000                                   *
* 111/12/05  V1.00.20  Zuwei      sync from mega                             *
* 112/09/21  V1.00.21  Zuwei Su   檔案改為從local目錄獲取，excel在每月7日處理*
* 112/09/25  V1.00.22  Zuwei Su   測試結果未insert ECS_FTP_LOG               *
* 112/09/28  V1.00.23  Zuwei Su   backup不刪除原檔               *
*                                                                            *
******************************************************************************/
package Mkt;

import java.io.File;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class MktT000 extends AccessDAO
{
 private final String PROGNAME = "高鐵車廂升等-FTP接收處理程式 112/09/28 V1.00.23";
 CommFunction comm = new CommFunction();
 CommFTP commFTP = null;
 CommRoutine comr = null;
 CommCrd comc = new CommCrd();
 CommDate commDate = new CommDate();

 String hBusinessDate = "";

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT000 proc = new MktT000();
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
       return(1);

   if ( !connectDataBase() ) 
       return(1);

   hBusinessDate = sysDate;
   if ( args.length == 1 )
   {
    hBusinessDate = args[0];
   }

   comr = new CommRoutine(getDBconnect(),getDBalias());
   commFTP = new CommFTP(getDBconnect(),getDBalias());

   selectPtrBusinday();

   String filename1 = "coup" + hBusinessDate + ".txt";
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始FTP匯入檔案[" + filename1 + "].....");
   ftpMget(filename1);
   showLogMessage("I","","===============================");
   if ("07".equals(hBusinessDate.substring(6))) {
       String lastMonth = commDate.monthAdd(hBusinessDate, -1);
       String filename2 = "CHKRF2007M_" + lastMonth + "_CO.xls";
       showLogMessage("I","","開始FTP匯入檔案[" + filename2 + "].....");
       ftpMget(filename2);
   }
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
  daoTable   = "PTR_BUSINDAY";
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
void ftpMget(String fileName) throws Exception {
    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
 // commFTP.hEflgSystemId = "THSR_UP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    commFTP.hEflgSystemId = "COUP_FTP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    commFTP.hEflgGroupId = "COUP"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    commFTP.hEflgSourceFrom = "THSR"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    commFTP.hEriaLocalDir =
            Normalizer.normalize(System.getenv("PROJ_HOME") + "/media/mkt/", Normalizer.Form.NFD);
    commFTP.hEflgModPgm = javaProgram;
    System.setProperty("user.dir", commFTP.hEriaLocalDir);

    showLogMessage("I", "", "mget " + fileName + "  開始接收....");
//    int errCode = commFTP.ftplogName("COUP_FTP", "mget " + fileName + "*");
//    int errCode = commFTP.localFtplogName("THSR_UP", fileName);
    int errCode = commFTP.localFtplogName("COUP_FTP", fileName);
//
    if (errCode != 0) {
        insertEcsNotifyLog(fileName);
        showLogMessage("I", "", "ERROR:無法取得資料");
        return;
    }
    totalCnt++;
    showLogMessage("I", "", "FTP完成.....");
    
    // showLogMessage("I","","===============================");
    // showLogMessage("I","","刪除遠端檔案.....");
    selectEcsFtpLog(fileName);
    // file backup
    comc.fileCopy(commFTP.hEriaLocalDir + fileName, commFTP.hEriaLocalDir + "backup/" + fileName);
    showLogMessage("I", "", "檔案backup完成===============================");
}

// ************************************************************************
 void  selectEcsFtpLog(String fileName) throws Exception
 {
  selectSQL  = "FILE_NAME, "
             + "REF_IP_CODE, "
             + "SOURCE_FROM,"
             + "ROWID as rowid";
  daoTable   = "ECS_FTP_LOG";
  whereStr   = "WHERE trans_seqno     = ? "
             + "AND   system_id       = ? "
             + "AND   trans_resp_code = 'Y' " 
             + "AND   file_name = ? "
             + "";

  setString(1,commFTP.hEflgTransSeqno);
  setString(2,commFTP.hEflgSystemId);
  setString(3,fileName);

  openCursor();

  while( fetchTable() ) 
   { 
    commFTP.hEflgSourceFrom  =  getValue("SOURCE_FROM");
    setValue("proc_code"  , "0");
    setValue("proc_desc" , "");

//    showLogMessage("I","","刪除檔案["+ getValue("file_name") +"]");
//    int errCode = commFTP.ftplogName(getValue("ref_ip_code"),"delete "+ getValue("file_name"));
//
//    if (errCode!=0)
//       {
//        showLogMessage("I","","ERROR:刪除檔案["+ getValue("file_name") +"%]失敗.....");
//        setValue("proc_code" , "B");
//        setValue("proc_desc"  , "刪除遠端檔案失敗");
//        updateEcsFtpLog();
//        continue;
//       }
//    showLogMessage("I","","刪除檔案["+ getValue("file_name") +"]完成.....");
//
//    setValue("proc_desc"  , "刪除遠端檔案成功");

    updateEcsFtpLog();
   }
  closeCursor();
 }
// ************************************************************************
 void  updateEcsFtpLog() throws Exception
 {
  dateTime();
  updateSQL = "file_date  = ?, "
            + "proc_code  = ?, "
            + "trans_desc = trans_desc||'+'||?, "
            + "proc_desc  = ?, "
            + "mod_pgm    = ?, "
            + "mod_time   = sysdate ";   
  daoTable  = "ecs_ftp_log";
  whereStr  = "where rowid = ?";

  setString(1 , getValue("file_name").substring(4,12));
  setString(2 , getValue("proc_code"));
  setString(3 , getValue("proc_desc"));
  setString(4 , getValue("proc_desc"));
  setString(5 , javaProgram);
  setRowId(6  , getValue("rowid"));

  updateTable();
  if ( notFound.equals("Y") )
     {
      showLogMessage("I","","UPDATE ecs_ftp_log error "+ getValue("rowid")); 
      exitProgram(0); 
     }
  return;
 }
// ************************************************************************
 int insertEcsNotifyLog(String fileName) throws Exception
 {
  setValue("crt_date"           , sysDate);
  setValue("crt_time"           , sysTime);
  setValue("unit_code"          , comr.getObjectOwner("3",javaProgram));
  setValue("obj_type"           , "3");
  setValue("notify_head"        , "無法接收 "+fileName+"資料");
  setValue("notify_name"        , "媒體檔名:"+fileName);
  setValue("notify_desc1"       , "程式 "+javaProgram+" 無法接收 "+fileName+" 資料");
  setValue("notify_desc2"       , "");
  setValue("trans_seqno"        , commFTP.hEflgTransSeqno);
  setValue("mod_time"           , sysDate+sysTime);
  setValue("mod_pgm"            , javaProgram);
  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }

}  // End of class FetchSample

