/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/27  V1.00.13  Allen Ho   mkt_t100                                   *
* 111/12/08  V1.00.14  Zuwei Su   sync from mega                             *
* 112/09/21  V1.00.15  Zuwei Su   檔案改為從local目錄獲取，excel在每月7日處理*
* 112/09/25  V1.00.16  Zuwei Su   測試結果未insert ECS_FTP_LOG               *
* 112/09/26  V1.00.17  Zuwei Su   檔案backup改為copy方式，因後續MktT110需要處理   *
* 112/09/28  V1.00.18  Kirin      fix SystemId to MktT110 處理                 * 
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

@SuppressWarnings("unchecked")
public class MktT100 extends AccessDAO
{
 private final String PROGNAME = "高鐵標準車廂-FTP接收處理程式 112/09/26 V1.00.17";
 CommFunction comm = new CommFunction();
 CommFTP commFTP = null;
 CommRoutine comr = null;
 CommCrd comc = new CommCrd();
 CommDate commDate = new CommDate();

 String hBusinessDate = "";
 String hTempSysdate       = "";

 long    totalCnt=0;
// ************************************************************************
 public static void main(String[] args) throws Exception
 {
  MktT100 proc = new MktT100();
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
       return(1);

   hTempSysdate = sysDate;
   if ( args.length == 1 ) 
      {
        hBusinessDate = args[0];
        hTempSysdate = hBusinessDate;
      }

   if ( !connectDataBase() )
       return(1);

   comr = new CommRoutine(getDBconnect(),getDBalias());
   commFTP = new CommFTP(getDBconnect(),getDBalias());

   selectPtrBusinday();

   String filename1 = "cosd" + hBusinessDate + ".txt";
   showLogMessage("I","","===============================");
   showLogMessage("I","","開始匯入檔案[" + filename1 + "].....");
   ftpMget(filename1);
   showLogMessage("I","","===============================");
   if ("07".equals(hBusinessDate.substring(6))) {
       String lastMonth = commDate.monthAdd(hBusinessDate, -1);
       String filename2 = "CHKRF2205M_" + lastMonth + "_CO.xls";
       showLogMessage("I","","開始匯入檔案[" + filename2 + "].....");
       ftpMget(filename2);
   }
   showLogMessage("I","","===============================");
   
   if (totalCnt>0)
     {
      commitDataBase();
      showLogMessage("I","","啟動 MktT110 執行後續動作 .....");
      showLogMessage("I","","===============================");

      String[] hideArgs = new String[1];
      try {
           hideArgs[0] = "";

           MktT110 mktT110 = new MktT110();
           int rtn = mktT110.mainProcess(hideArgs);
           if(rtn < 0)   return (1);
           showLogMessage("I","","MktT110 執行結束");
           showLogMessage("I","","===============================");
          } catch (Exception ex) 
               {
                showLogMessage("I","","無法執行 MktT110 ERROR!");
               }
     }
   
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
    commFTP.hEflgSystemId = "COUP_FTP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
//  commFTP.hEflgSystemId = "THSR_BILL"; /* 區分不同類的 FTP 檔案-大類 (必要) */
    commFTP.hEflgGroupId = "COUP"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
    commFTP.hEflgSourceFrom = "THSR"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
    commFTP.hEriaLocalDir =
            Normalizer.normalize(System.getenv("PROJ_HOME") + "/media/mkt/", Normalizer.Form.NFD);
    commFTP.hEflgModPgm = javaProgram;
    System.setProperty("user.dir", commFTP.hEriaLocalDir);

    showLogMessage("I", "", "mget " + fileName + "* 開始接收....");
//    int errCode = commFTP.ftplogName("COUP_FTP", "mget " + fileName + "*");
    int errCode = commFTP.localFtplogName("COUP_FTP", fileName);

    if (errCode != 0) {
        showLogMessage("I", "", "ERROR:無法取得資料");
        insertEcsNotifyLog(fileName);
        return;
    }
    totalCnt++;
    showLogMessage("I", "", "FTP完成.....");

    // showLogMessage("I","","刪除遠端檔案.....");
    selectEcsFtpLog();
    commitDataBase();
    // file backup
    comc.fileCopy(commFTP.hEriaLocalDir + fileName, commFTP.hEriaLocalDir + "backup/" + fileName);
    showLogMessage("I", "", "檔案backup完成===============================");
}

// ************************************************************************
 void  selectEcsFtpLog() throws Exception
 {
  selectSQL  = "FILE_NAME, "
             + "REF_IP_CODE, "
             + "SOURCE_FROM,"
             + "ROWID as rowid";
  daoTable   = "ECS_FTP_LOG";
  whereStr   = "WHERE trans_seqno     = ? "
             + "AND   system_id       = ? "
             + "AND   trans_resp_code = 'Y' " 
             + "";

  setString(1,commFTP.hEflgTransSeqno);                 
  setString(2,commFTP.hEflgSystemId);                 

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
//        setValue("proc_desc"  , "刪除檔案["+ getValue("file_name") +"%]失敗");
//        updateEcsFtpLog();
//        continue;
//       }
//    showLogMessage("I","","刪除檔案["+ getValue("file_name") +"]完成.....");
//
//    setValue("proc_desc"    , "刪除檔案完成");
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

  if ((getValue("file_name").substring(0,4).equals("cosd"))&&
      (getValue("file_name").length()==16))
     setString(1 , getValue("file_name").substring(4,12));
  else
     setString(1 , hBusinessDate);
  setString(2 , getValue("proc_code"));
  setString(3 , getValue("proc_desc"));
  setString(4 , getValue("proc_desc"));
  setString(5 , javaProgram);
  setRowId(6  , getValue("rowid"));

  int cnt = updateTable();
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
  setValue("notify_head"        , "無法 FTP 接收 "+fileName+"資料");
  setValue("notify_name"        , "媒體檔名:"+fileName);
  setValue("notify_desc1"       , "程式 "+javaProgram+" 無法 FTP 接收 "+fileName+" 資料");
  setValue("notify_desc2"       , "");
  setValue("trans_seqno"        , commFTP.hEflgTransSeqno);
  setValue("mod_time"           , sysDate+sysTime);
  setValue("mod_pgm"            , javaProgram);
  daoTable  = "ecs_notify_log";

  insertTable();

  return(0);
 }
// ************************************************************************

}  // End of class FetchSample

