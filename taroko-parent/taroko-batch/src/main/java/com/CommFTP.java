/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/06/04  V1.00.16  Allen Ho   CommFTP initial                            *
* 106/07/18  V1.00.19  Allen Ho   Modify for codereview checkmarx            *
* 107/05/29  V1.01.00  Lai        Add hEriaRemoteDir to public            *
* 107/07/30  V1.00.24  Allen Ho   Change execute system command to java func *
* 107/07/31  V1.00.25  Lai        Add MqmSend ( not ready )                  *
* 107/10/05  V1.00.26  Howard     Add getFileSize()                          *
* 107/07/31  V1.00.27  Lai        Add DEBUG_S                                *
* 107/11/14  V1.00.28  Brian      Add Rename                                 *
* 108/01/22  V1.00.29  Allen Ho   bug fix                                    *
* 108/01/24  V1.00.30  Brian      Add ArrayList<String> FileList             *
* 108/02/15  V1.00.31  Allen Ho   bug fix port_no file_zip_hidewd            *
* 108/03/04  V1.00.32  Allen Ho   fix no file transfer                       *
* 108/05/28  V1.00.33  Brian      Add FTP ERROR (判斷getfile/putfile回傳值)   *
* 108/10/21  V1.00.34  Allen Ho   Add error message display only              *
* 108/11/16  V1.00.34  Allen Ho   modify get no file no insert log error      *
* 108/12/02  V1.00.35  Allen Ho   initial default value                       *
* 108/12/25  V1.00.36  Allen Ho   add password enc and dec function           *
* 109/07/06  V1.00.37    Zuwei     coding standard, rename field method & format                   *
* 109/07/22  V1.00.38    Zuwei     coding standard, rename field method                   *
* 109-08-14  V1.00.39  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
* 109-10-13  V1.00.40 JustinWu    add a new method just to move the file from the remote to the local path
* 110-06-13  V1.00.41 JustinWu    add a FTPS method
* 110-07-16  V1.00.42 JustinWu    fix for a bug of finding incorrect file name
* 110-07-22  V1.00.43 JustinWu    allow the remote path to use the relative path 
* 110-08-11  V1.00.44 JustinWu    modify the content and the type of log      *
* 110-08-17  V1.00.45 JustinWu    add insertEcsNotifyLog
* 111-12-06  V1.00.46 Zuwei       add method localFtplogName - 檢查local目錄下文檔是否存在，不經過FTP傳輸
* 111-12-08  V1.00.46 Zuwei       method localFtplogName 中設置hEriaFtpType為0
* 112-08-28  V1.00.47 Ryan        add runDelete                                      *
******************************************************************************/
package com;

import java.io.*;
import java.sql.*;

import Dxc.Util.SecurityUtil;
import Dxc.Util.Ftp.*;

import java.nio.file.Paths;

import java.util.ArrayList;


import Mqm.MqmSend;

@SuppressWarnings({"unchecked", "deprecation"})
public class CommFTP extends AccessDAO {
  static final int FTP_SUCCESS = 0;
  static final int FTP_NOT_DEFINE_REF_IP_PARAM = 1;
  static final int FTP_CONNECT_FAIL = 2;
  static final int FTP_NO_FILE_EXIST = 3;
  static final int FTP_NOT_DEFINE_REF_IP_ADDR = 5;
  static final int FTP_FAIL = 6;
  
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommRoutine comr = null;

  public String hEriaRemoteDir = "";
  public String hEriaLocalDir = "";
  private String hEriaRefName = "";
  private String hEriaRefIpCode = "";
  private String hEriaRefIp = "";
  private String hEriaUserId = "";
  private String hEriaUserHidewd = "";
  private String hEriaTransType = "";
  private String hEriaPortNo = "";
  private String hEriaFtpType = "";

  
  public String hEflgSystemId = "";
  public String hEflgGroupId = "";
  public String hEflgSourceFrom = "";
  public String hEflgModPgm = "";
  public String hEflgTransSeqno = "";

  private int hEflgTransTotalCnt = 0;
  private String hEflgTransData = "";
  private String hEflgTransType = "";
  private int hEflgTransSeq = 0;
  private double hEflgLocalSize = 0;
  private double hEflgTransSize = 0;
  private String hEflgFileName = "";
  private String hEflgTransMode = "";
  private String hEflgTransRespCode = "";
  private String hEflgTransDesc = "";
  private String hEriaFileZipHidewd = "";
  private String hEriaFileUnzipHidewd = "";
  public ArrayList<String> fileList = new ArrayList<String>();

  String[] DBNAME = new String[10];

  // ************************************************************************
  public CommFTP(Connection conn[], String[] dbAlias) throws Exception {
    super.conn = conn;
    setDBalias(dbAlias);
    setSubParm(dbAlias);

    DBNAME[0] = dbAlias[0];

    comr = new CommRoutine(conn, dbAlias);
    return;
  }
  
  public CommFTP() throws Exception {

    }

  // ************************************************************************
  public int ftplogName(String passRefIpCode, String passTransData) throws Exception {
    initDefault();
    hEriaRefIpCode = passRefIpCode;

    hEflgTransDesc = "檔案傳輸正常";
    int retInt = toFtplog(passRefIpCode, passTransData);

    showLogMessage("I", "", "[CommFTP] FTP_RESULT_CODE[" + retInt + "]"); 
    showLogMessage("I", "", "[CommFTP] FTP_Command[" + passTransData + "]"); 

    setConsoleMode("Y");
    switch (retInt) {
      case FTP_SUCCESS:
        hEflgTransDesc = "遠端 FTP 作業正常結束!";
        showLogMessage("I", "", hEflgTransDesc);
        if (selectPtrSysIdtab() != 0)
          insertPtrSysIdtab();
        else
          updatePtrSysIdtab();
        break;
      case FTP_NOT_DEFINE_REF_IP_PARAM:
        hEflgTransDesc = hEriaRefIpCode + " 未定義參數檔!";
        hEflgTransRespCode = "1";
        insertEcsFtpLog();
        showLogMessage("I", "", hEflgTransDesc);
        break;
      case FTP_CONNECT_FAIL:
        hEflgTransDesc = "遠端 FTP " + hEriaRefIpCode + " 連線失敗!";
        hEflgTransRespCode = "2";
        insertEcsFtpLog();
        showLogMessage("I", "", hEflgTransDesc);
        break;
      case FTP_NO_FILE_EXIST:
        hEflgTransDesc = "FTP 無檔案可傳輸!";
        hEflgTransRespCode = "4";
        insertEcsFtpLog();
        showLogMessage("I", "", hEflgTransDesc);
        break;
      case FTP_NOT_DEFINE_REF_IP_ADDR:
        hEflgTransDesc = "ECS_REF_IP_ADDR 未定義 [" + passRefIpCode + "]";
        hEflgTransRespCode = "5";
        insertEcsFtpLog();
        showLogMessage("I", "", hEflgTransDesc);
        break;
      case FTP_FAIL:
        hEflgTransDesc = "FTP 失敗!";
        hEflgTransRespCode = "6";
        insertEcsFtpLog();
        showLogMessage("I", "", hEflgTransDesc);
        break;
    }
    return (retInt);
  }

  // ************************************************************************
  /**
   * 檢查local目錄下文檔是否存在，不經過FTP傳輸
   * 
   * @param passRefIpCode
   * @param fileName
   * @return
   * @throws Exception
   */
  public int localFtplogName(String passRefIpCode, String fileName) throws Exception {
    initDefault();
    hEriaRefIpCode = passRefIpCode;
    hEflgFileName = fileName;

    hEflgTransDesc = "Local檔案處理正常";
    hEriaFtpType = "0";
//    int retInt = toFtplog(passRefIpCode, passTransData);
    File f = new File(hEriaLocalDir, fileName); 
    int retInt = f.exists() ? 0 : 1;

    showLogMessage("I", "", "Check local file exists [" + fileName + "]"); 
    setConsoleMode("Y");
    switch (retInt) {
      case 0:
        hEflgTransDesc = "Local檔案處理完成!";
        hEflgTransRespCode = "Y";
        hEflgTransSeq = 1;
        hEflgLocalSize = f.length();
        showLogMessage("I", "", hEflgTransDesc);
        insertEcsFtpLog();
        if (selectPtrSysIdtab() != 0)
          insertPtrSysIdtab();
        else
          updatePtrSysIdtab();
        break;
      case 1:
        hEflgTransDesc = "Local無檔案可處理!";
        hEflgTransRespCode = "1";
        hEflgTransSeq = 1;
        insertEcsFtpLog();
        showLogMessage("I", "", hEflgTransDesc);
        break;
    }
    return (retInt);
  }

// ************************************************************************
  int toFtplog(String passRefIpCode, String passTransData) throws Exception {
    hEflgTransData = passTransData;
    showLogMessage("I", "", "[CommFTP] ref_ip_code[" + hEriaRefIpCode + "]");
      
    if (hEriaRefIpCode.length() == 0)
      return (FTP_NOT_DEFINE_REF_IP_PARAM);

    if (selectEcsRefIpAddr() != 0)
      return (FTP_NOT_DEFINE_REF_IP_ADDR);

    int retInt = FTP_SUCCESS;

    showLogMessage("I", "", "[CommFTP] FTP_TYPE[" + hEriaFtpType + "]");

    if (hEriaFtpType.equals("0"))
      retInt = moveFileLog();
    else if (hEriaFtpType.equals("1"))
      retInt = commFtplog(false);
    else if (hEriaFtpType.equals("2"))
      retInt = commSftplog();
    else if (hEriaFtpType.equals("3")) {
      retInt = commFtpslog();
    }

    return (retInt);
  }

// ************************************************************************
  private int insertEcsFtpLog() throws Exception {
    dateTime();
    extendField = "eflg.";
    setValue("eflg.SYSTEM_ID", hEflgSystemId);
    setValue("eflg.GROUP_ID", hEflgGroupId);
    setValue("eflg.SOURCE_FROM", hEflgSourceFrom);
    setValue("eflg.CRT_DATE", sysDate);
    setValue("eflg.CRT_TIME", sysTime);
    setValue("eflg.FTP_TYPE", hEriaFtpType);
    setValue("eflg.REF_IP_CODE", hEriaRefIpCode);
    setValue("eflg.FILE_NAME", hEflgFileName);
    setValue("eflg.TRANS_TYPE", hEflgTransType);
    setValue("eflg.REMOTE_ADDR", hEriaRefIp);
    setValue("eflg.TRANS_MODE", hEflgTransMode);
    setValue("eflg.TRANS_DATA", hEflgTransData);
    setValueInt("eflg.TRANS_TOTAL_CNT", hEflgTransTotalCnt);
    setValueInt("eflg.TRANS_SEQ", hEflgTransSeq);
    setValueDouble("eflg.LOCAL_SIZE", hEflgLocalSize);
    setValueDouble("eflg.TRANS_SIZE", hEflgTransSize);
    setValue("eflg.FILE_ZIP_HIDEWD", hEriaFileZipHidewd);
    setValue("eflg.FILE_UNZIP_HIDEWD", hEriaFileUnzipHidewd);
    setValue("eflg.LOCAL_DIR", hEriaLocalDir);
    setValue("eflg.TRANS_DESC", hEflgTransDesc);
    setValue("eflg.TRANS_RESP_CODE", hEflgTransRespCode);
    if (hEflgTransSeqno.length() == 0)
      setValue("eflg.TRANS_SEQNO", comr.getSeqno("ECS_MODSEQ"));
    else
      setValue("eflg.TRANS_SEQNO", hEflgTransSeqno);
    setValue("eflg.MOD_TIME", sysDate + sysTime);
    setValue("eflg.mod_pgm", hEflgModPgm);

    daoTable = "ECS_FTP_LOG";
    insertTable();
    if (dupRecord.equals("Y")) {
    }

    return (0);
  }

  // ************************************************************************
  private int selectEcsRefIpAddr() throws Exception {
    setConsoleMode("Y");
    selectSQL = "ref_ip,           "
        + "ref_name,         "
        + "hide_ref_code,    "
        + "user_id,          "
        + "user_hidewd,      "
        + "ftp_type,         "
        + "trans_type,        "
        + "remote_dir,       "
        + "local_dir,        "
        + "port_no,           "
        + "file_zip_hidewd,  "
        + "file_unzip_hidewd "
        + "";
    daoTable = "ecs_ref_ip_addr";
    whereStr = "WHERE ref_ip_code = ? ";

    // setString(1,h_eria_local_dir);
    setString(1, hEriaRefIpCode);

    int recordCnt = selectTable();
    
    showLogMessage("I", "", String.format("[CommFTP] ref_ip_code[%s], select_count[%s]", hEriaRefIpCode, recordCnt));
    

    if (notFound.equals("Y")) {
      showLogMessage("I", "", "[CommFTP] select ecs_ref_ip_addr error!");
      showLogMessage("I", "", "[CommFTP] ref_ip_code[" + hEriaRefIpCode + "]");
      return (FTP_NOT_DEFINE_REF_IP_PARAM);
    }

    hEriaFtpType = getValue("ftp_type");
    hEriaRefIp = getValue("REF_IP");
    hEriaRefName = getValue("REF_MAME");
    hEriaUserId = getValue("USER_ID");
    hEriaUserHidewd = comm.hideUnzipData(getValue("USER_HIDEWD"), getValue("hide_ref_code"));
    hEriaTransType = getValue("TRANS_TYPE");
    hEriaRemoteDir = getValue("REMOTE_DIR");
    if (hEriaLocalDir.length() == 0)
      hEriaLocalDir = getValue("LOCAL_DIR");
    hEriaPortNo = getValue("PORT_NO");
    hEriaFileZipHidewd = comm.hideUnzipData(getValue("file_zip_hidewd"), getValue("hide_ref_code"));
    hEriaFileUnzipHidewd =
        comm.hideUnzipData(getValue("file_unzip_hidewd"), getValue("hide_ref_code"));

    return (0);
  }

  // ************************************************************************
  private int selectPtrSysIdtab() throws Exception {
    setConsoleMode("Y");
    daoTable = "ptr_sys_idtab";
    whereStr = "WHERE wf_type = 'FTPSYSTEMID' AND wf_id   = ? ";

    setString(1, hEriaRefIpCode);

    selectTable();

    if (notFound.equals("Y"))
      return (1);

    return (0);
  }

  // ************************************************************************
  private int insertPtrSysIdtab() throws Exception {
    dateTime();
    setValue("wf_type", "FTPSYSTEMID");
    setValue("wf_id", hEriaRefIpCode);
    setValue("mod_time", sysDate + sysTime);
    setValue("mod_pgm", hEflgModPgm);

    daoTable = "ptr_sys_idtab";

    insertTable();

    return (0);
  }

  // ************************************************************************
  public int updatePtrSysIdtab() throws Exception {
    dateTime();
    updateSQL = "mod_time = timestamp_format(?,'yyyymmddhh24miss')";
    daoTable = "ptr_sys_idtab";
    whereStr = "WHERE wf_type = 'FTPSYSTEMID'  AND   wf_id   = ? ";

    setString(1, sysDate + sysTime);
    setString(2, hEriaRefIpCode);

    updateTable();

    if (notFound.equals("Y"))
      return (1);
    return (0);
  }

  // ************************************************************************
  public int mqSend(String filename, int size) {
    int rtn = 0;
    dateTime();
    
//    comc.fileCopy(comc.getIBMftp() + "/" + filename, comc.getIBMftp() + "/bk/" + filename + "." + sysDate);
    String srcPath = Paths.get(comc.getIBMftp(), filename).toString();
    String targetPath = Paths.get(comc.getIBMftp(), "bk", filename + "." + sysDate).toString();
    comc.fileCopy(srcPath, targetPath);  
    
    showLogMessage("I", "","[CommFTP] mq_send copy=[" + targetPath + "]");

    String[] newArgs = {filename, Integer.toString(size)};

    MqmSend mqmSend = new MqmSend();
    mqmSend.ftpHome = comc.getIBMftp();
    rtn = mqmSend.mainProcess(newArgs);
    mqmSend = null;


    return rtn;
  }
//************************************************************************ 
int commFtpslog() throws Exception {
  FtpsClient ftpsClient = null;
  try {
    boolean isImplicit = true;
    ftpsClient = new FtpsClient(isImplicit);
    String ftpsIp = hEriaRefIp;
    String user = hEriaUserId;
    String wd = hEriaUserHidewd;
    if (hEriaPortNo.length() == 0) {
      hEriaPortNo = "990";
    }
    int portNumber = Integer.valueOf(hEriaPortNo);
    
    showLogMessage("I","",String.format("[CommFTP] FTPS IP:PORT[%s:%s] USER[%s]", ftpsIp, portNumber, user));
    
    boolean isConnected = ftpsClient.connect(ftpsIp, user, wd, portNumber, isImplicit);
    if (isConnected == false) {
      return(FTP_CONNECT_FAIL);
    }
    
    // 1=> ASCII mode, 2=> BINARY mode
    if (hEriaTransType.equals("A")) {
      hEflgTransType = "ASCII";
      ftpsClient.setTransFileType(1); 
    } else {
      hEflgTransType = "BIN";
      ftpsClient.setTransFileType(2);
    }
    
    // local directory and remote directory
      String localPath  = hEriaLocalDir;
      String remotePath = hEriaRemoteDir;
      hEflgTransRespCode = "Y";
      
        /* change directory to specific remote path*/
      showLogMessage("I", "", "[CommFTP] FTPS Remote Dir[" + remotePath + "]");
      showLogMessage("I", "", "[CommFTP] FTPS Local  Dir[" + localPath + "]");
      showLogMessage("I","", String.format("[CommFTP] FTPS Command[%s]", hEflgTransData));
      
        boolean isChgSuc = ftpsClient.changeDir(remotePath);
      if (isChgSuc == false) {
        showLogMessage("I", "", ftpsClient.getReplyString());
      }
      
      // trans_data 
      int listCount = (int)hEflgTransData.chars().filter(ch -> ch =='|').count()+1;
      
      for (int inti = 0; inti < listCount; inti++) {
        // analyze pass command
          String ftpCmd  = CommFunction.getStr(hEflgTransData , inti + 1, "|");
          int intIndex = ftpCmd.indexOf(" ");
          String cmdType = ftpCmd.substring(0,intIndex).toUpperCase(); // GET, PUT, DELETE, RENAME
          String pattern = ftpCmd.substring(intIndex+1,ftpCmd.length()).trim();

          int resultNo = FTP_SUCCESS;
          
          if (cmdType.indexOf("GET")!=-1) {
            resultNo = runFtpsGet(ftpsClient, remotePath, localPath, pattern);
          }else if (cmdType.indexOf("PUT")!=-1) {
            resultNo = runFtpsPut(ftpsClient, localPath, remotePath, pattern);
          }else if (cmdType.indexOf("DELETE")!=-1) {
            resultNo = runFtpsDelete(ftpsClient, remotePath, pattern);
          }else if (cmdType.indexOf("RENAME")!=-1) {
            resultNo = runFtpsRename(ftpsClient, remotePath, pattern);
          }
          
          if ( resultNo != FTP_SUCCESS ) {
            return resultNo;
      }
      }
      
  }catch (Exception e) {
    showLogMessage("I", "", e.getMessage());
  }finally {
    if (ftpsClient != null) {
      ftpsClient.disconnect();
    }
  }
  
  return FTP_SUCCESS;
}
//************************************************************************
private int moveFileLog() throws Exception {

  hEriaPortNo = "";

  showLogMessage("I", "", String.format("[CommFTP] FTP(MOVE) REF_IP[%s], USER_ID[%s]", hEriaRefIp, hEriaUserId));

  hEriaTransType = "";
  hEflgTransType = "";

  String sLLocalPath = hEriaLocalDir;
  String sLRemotePath = hEriaRemoteDir;

  hEflgTransRespCode = "Y";

  String sLPattern = "";
  String cmdType = "";
  String ftpCmd = "";
  
  showLogMessage("I", "", "[CommFTP] FTP(MOVE) Command[" + hEflgTransData + "]"); 
  showLogMessage("I", "", "[CommFTP] FTP(MOVE) Remote Dir[" + sLRemotePath + "]");
  showLogMessage("I", "", "[CommFTP] FTP(MOVE) Local  Dir[" + sLLocalPath + "]");
  
  int listCnt = (int) hEflgTransData.chars().filter(ch -> ch == '|').count() + 1;
  
  for (int inti = 0; inti < listCnt; inti++) {

    // analyze pass command
    ftpCmd = CommFunction.getStr(hEflgTransData, inti + 1, "|");

    int intIndex = ftpCmd.indexOf(" ");
    cmdType = ftpCmd.substring(0, intIndex).toUpperCase();
    sLPattern = ftpCmd.substring(intIndex + 1, ftpCmd.length()).trim();
    
    int resultCode = FTP_SUCCESS;
    if (cmdType.indexOf("GET") != -1) {
      resultCode = runMoveGet(sLLocalPath, sLRemotePath, sLPattern);
    }else if (cmdType.indexOf("PUT") != -1) {
      resultCode = runMovePut(sLLocalPath, sLRemotePath, sLPattern);  
    }else if (cmdType.indexOf("DELETE") != -1) {
      resultCode = runDelete(sLRemotePath, sLPattern);  
    }           

        if (resultCode != FTP_SUCCESS ) {
      return resultCode;
    }

  }
  return FTP_SUCCESS;
}

//************************************************************************ 
private int runFtpsRename(FtpsClient ftpsClient, String remotePath, String pattern) {
  showLogMessage("I", "", "[CommFTP] FTPS RENAME Pattern[" + pattern + "]");
  int index = pattern.indexOf(" ");
  String sourceFileName = pattern.substring(0, index).trim();
  String targetFileName = pattern.substring(index + 1, pattern.length()).trim();
  boolean isSuccessful = ftpsClient.renameFile(remotePath, sourceFileName, targetFileName);
  if (!isSuccessful) {
    showLogMessage("I", "", String.format("[CommFTP] FTPS 檔案[%s] RENAME TO %s FAILED! ", sourceFileName, targetFileName));
    return (FTP_FAIL);
  } else {
    showLogMessage("I", "", String.format("[CommFTP] FTPS 檔案[%S] RENAME TO %s SUCCESS! ", sourceFileName, targetFileName));
  }
  return FTP_SUCCESS;
}
//************************************************************************ 
private int runFtpsDelete(FtpsClient ftpsClient, String remotePath, String pattern) throws IOException {
  showLogMessage("I", "", "[CommFTP] FTPS DELETE Pattern[" + pattern + "]");
  boolean isSuccessful = ftpsClient.mdeleteFile(remotePath, pattern);
  if (!isSuccessful) {
    showLogMessage("I", "", String.format("[CommFTP] FTPS remote dir[%s]中的檔案[%s]刪除處理失敗! ", remotePath, pattern));
    return (FTP_FAIL);
  }else{
    showLogMessage("I", "", String.format("[CommFTP] FTPS remote dir[%s]中的檔案[%s]刪除處理完成! ", remotePath, pattern));
  }
  return (FTP_SUCCESS);
}
//************************************************************************ 
private int runFtpsPut(FtpsClient ftpsClient, String localPath, String remotePath, String pattern) throws Exception {
  showLogMessage("I", "", "[CommFTP] FTPS PUT Pattern[" + pattern + "]");
  hEflgTransMode = "SEND";
  boolean isSuccessful = ftpsClient.mputFile(remotePath, localPath, pattern);
  if (isSuccessful == false) {
    return (FTP_FAIL);
  }
  
  ArrayList<String> processedFileList = ftpsClient.getProcessedFileList();
//  FileList.clear();
//  FileList = (ArrayList<String>) processedFileList.clone();

  hEflgTransTotalCnt = processedFileList.size();
  showLogMessage("I", "", "[CommFTP] FTPS file count[" + processedFileList.size() + "]");
  if (hEflgTransTotalCnt == 0){ 
    return (FTP_NO_FILE_EXIST); // when file not exist
  }
  for (int i = 0; i < processedFileList.size(); i++) {
    String filePath = processedFileList.get(i);
    hEflgFileName = Paths.get(filePath).getFileName().toString();
    hEflgTransSeq = i + 1;
    hEflgLocalSize = getFileSize(filePath);
    showLogMessage("I", "", String.format("[CommFTP] FTPS 檔案[%s] 傳送處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));
    insertEcsFtpLog();
  }
  return (FTP_SUCCESS);
}
//************************************************************************ 
private int runFtpsGet(FtpsClient ftpsClient, String remotePath, String localPath, String pattern) throws Exception {
  hEflgTransMode = "RECV";
  showLogMessage("I", "", "[CommFTP] FTPS GET Pattern[" + pattern + "]");
  
  boolean isSuccessful = ftpsClient.mgetFile(remotePath, localPath, pattern);
  if (isSuccessful == false) {
    return (FTP_FAIL);
  }

  ArrayList<String> processedFileList = ftpsClient.getProcessedFileList();

  hEflgTransTotalCnt= processedFileList.size();
  if (hEflgTransTotalCnt == 0)
    return (FTP_NO_FILE_EXIST);
  
  showLogMessage("I", "", "[CommFTP] FTPS file count[" + processedFileList.size() + "]");
  
  for (int i = 0; i < processedFileList.size(); i++) {
    String filePath = processedFileList.get(i);
    hEflgFileName = Paths.get(filePath).getFileName().toString();
    hEflgTransSeq = i + 1;
    hEflgLocalSize = getFileSize(filePath);
    showLogMessage("I", "", String.format("[CommFTP] FTPS 檔案[%s] 接收處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));
    insertEcsFtpLog();
  }
  return (FTP_SUCCESS);
  
}

  // ************************************************************************
  int commFtplog(boolean FtpMode) throws Exception {
    FtpClient lFtpClient = null;
    try {

      lFtpClient = new FtpClient(FtpMode);
      String sLFtpServerIp = hEriaRefIp;
      String sLUser = hEriaUserId;
      String sLPassword = hEriaUserHidewd;
      if (hEriaPortNo.length() == 0)
        hEriaPortNo = "21";
      int sLPort = Integer.valueOf(hEriaPortNo);

      showLogMessage("I", "",String.format("[CommFTP] FTP IP[%s], PORT[%s], USER[%s]", hEriaRefIp, sLPort, hEriaUserId));      

      boolean bLConnResult = lFtpClient.connect(sLFtpServerIp, sLUser, sLPassword, sLPort);
      if (!bLConnResult) {
        return (FTP_CONNECT_FAIL);
      }

      if (hEriaTransType.equals("A")) {
        hEflgTransType = "ASCII";
        lFtpClient.setTransFileType(1); // 1=> ASCII mode, 2=> BINARY mode
      } else {
        lFtpClient.setTransFileType(2);
        hEflgTransType = "BIN";
      }

      String sLLocalPath = SecurityUtil.verifyPath(hEriaLocalDir);
      String sLRemotePath = SecurityUtil.verifyPath(hEriaRemoteDir);
      hEflgTransRespCode = "Y";

      String sLPattern = "";
      String cmdType = "";
      String ftpCmd = "";

      showLogMessage("I", "", "[CommFTP] FTP Command[" + hEflgTransData + "]");
      
      /* change directory to specific remote path*/
    showLogMessage("I", "", "[CommFTP] FTP Remote Dir[" + hEriaRemoteDir + "]");
    showLogMessage("I", "", "[CommFTP] FTP Local  Dir[" + sLLocalPath + "]");
      boolean isChgSuc = lFtpClient.changeDir(sLRemotePath);
    if (isChgSuc == false) {
      showLogMessage("I", "", lFtpClient.getReplyStr());
    }
      
      int listCnt = (int) hEflgTransData.chars().filter(ch -> ch == '|').count() + 1;
      for (int inti = 0; inti < listCnt; inti++) {

        // analyze pass command
        ftpCmd = CommFunction.getStr(hEflgTransData, inti + 1, "|");

        int intIndex = ftpCmd.indexOf(" ");
        cmdType = ftpCmd.substring(0, intIndex).toUpperCase();
        sLPattern = ftpCmd.substring(intIndex + 1, ftpCmd.length()).trim();

        int ftpResult = FTP_SUCCESS;
        if (cmdType.indexOf("GET") != -1) {
          ftpResult = runFtpGet(lFtpClient, sLLocalPath, sLRemotePath, sLPattern);
        }
        else if (cmdType.indexOf("PUT") != -1) {
          ftpResult = runFtpPut(lFtpClient, sLLocalPath, sLRemotePath, sLPattern);
        }
        else if (cmdType.indexOf("DELETE") != -1) {
          ftpResult = runFtpDelete(lFtpClient, sLPattern);
        }
        else if (cmdType.indexOf("RENAME") != -1) {
          ftpResult = runFtpRename(lFtpClient, sLPattern);
        }
        
        if (ftpResult != FTP_SUCCESS) {
      return ftpResult;
    }

      }

    } catch (Exception e) {
      showLogMessage("I", "", e.getMessage());
    } finally {
      if (lFtpClient != null) {
        lFtpClient.disconnect();
    }
  }
    return (FTP_SUCCESS);
  }

  private int runFtpRename(FtpClient lFtpClient, String sLPattern) {
    showLogMessage("I", "", "[CommFTP] FTP RENAME Pattern[" + sLPattern + "]");
    boolean bLConnResult;
    int index = sLPattern.indexOf(" ");
    String sPSourceFileName = sLPattern.substring(0, index).trim();
    String sPTargetFileName = sLPattern.substring(index + 1, sLPattern.length()).trim();

    bLConnResult = lFtpClient.renameFile(hEriaRemoteDir, sPSourceFileName, sPTargetFileName);
    if (!bLConnResult){
      showLogMessage("I", "",String.format("[CommFTP] FTP 檔案[%s] RENAME TO %s FAILED! ", sPSourceFileName, sPTargetFileName));
      showLogMessage("I", "", lFtpClient.getReplyStr());  
    }else
      showLogMessage("I", "",String.format("[CommFTP] FTP 檔案[%s] RENAME TO %s SUCCESS! ", sPSourceFileName, sPTargetFileName));
    
    return bLConnResult == true ? FTP_SUCCESS : FTP_FAIL;
  }

private int runFtpDelete(FtpClient lFtpClient, String sLPattern) throws IOException {
  showLogMessage("I", "", "[CommFTP] FTP DELETE Pattern[" + sLPattern + "]");
  boolean bLConnResult;
  bLConnResult = lFtpClient.mdeleteFile(hEriaRemoteDir, sLPattern);// modify by brian 20181113
    if (!bLConnResult){
      showLogMessage("I", "", String.format("[CommFTP] FTP 資料夾[%s]中的檔案[%s] 刪除處理失敗! ", hEriaRemoteDir, sLPattern));
      showLogMessage("I", "", lFtpClient.getReplyStr());
    }else
        showLogMessage("I", "", String.format("[CommFTP] FTP 資料夾[%s]中的檔案[%s] 刪除處理完成! ", hEriaRemoteDir, sLPattern));
  return bLConnResult == true ? FTP_SUCCESS : FTP_FAIL;
}

private int runFtpPut(FtpClient lFtpClient, String sLLocalPath, String sLRemotePath, String sLPattern)
    throws IOException, Exception {
    String sLFileName;
    hEflgTransMode = "SEND";
    showLogMessage("I", "", "[CommFTP] FTP PUT Pattern[" + sLPattern + "]");
    
    boolean bLResult = lFtpClient.mputFile(sLRemotePath, sLLocalPath, sLPattern);
    if (bLResult == false)
      return (FTP_FAIL);

    ArrayList<String> lProcessedFileList = lFtpClient.getProcessedFileList();

    fileList.clear();
    fileList = (ArrayList<String>) lProcessedFileList.clone();

    hEflgTransTotalCnt = lProcessedFileList.size();
    showLogMessage("I", "","[CommFTP] FTP file count[" + lProcessedFileList.size() + "]" + sLRemotePath);
      
    if (hEflgTransTotalCnt == 0) { // when file not exist
      return (FTP_NO_FILE_EXIST);
    }
    for (int i = 0; i < lProcessedFileList.size(); i++) {
      sLFileName = lProcessedFileList.get(i);
      
      /* Justin 2021-07-16 fix for a bug of finding incorrect file name */
      hEflgFileName = Paths.get(sLFileName).getFileName().toString();
      hEflgTransSeq = i + 1;
      hEflgLocalSize = getFileSize(sLFileName);
      showLogMessage("I", "",String.format("[CommFTP] FTP 檔案[%s] 傳送處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));
      insertEcsFtpLog();
    }
    
    return (FTP_SUCCESS);
}

private int runFtpGet(FtpClient lFtpClient, String sLLocalPath, String sLRemotePath, String sLPattern)
    throws IOException, Exception {
    String sLFileName;
    hEflgTransMode = "RECV";
    showLogMessage("I", "", "[CommFTP] FTP GET Pattern[" + sLPattern + "]");
    
    boolean bLResult = lFtpClient.mgetFile(sLRemotePath, sLLocalPath, sLPattern);
    if (bLResult == false) {
      return (FTP_FAIL);
    }

    ArrayList<String> lProcessedFileList = lFtpClient.getProcessedFileList();

    fileList.clear();
    fileList = (ArrayList<String>) lProcessedFileList.clone();

    hEflgTransTotalCnt = lProcessedFileList.size();
    if (hEflgTransTotalCnt == 0) {
      return (FTP_NO_FILE_EXIST);
    }
    
    showLogMessage("I", "", "[CommFTP] FTP file count[" + lProcessedFileList.size() + "]");
    
    for (int i = 0; i < lProcessedFileList.size(); i++) {
      sLFileName = lProcessedFileList.get(i);
      
      /* Justin 2021-07-16 fix for a bug of finding incorrect file name */
      hEflgFileName = Paths.get(sLFileName).getFileName().toString();
      hEflgTransSeq = i + 1;
      hEflgLocalSize = getFileSize(sLFileName);
      showLogMessage("I", "", String.format("[CommFTP] FTP 檔案[%s] 接收處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));
      insertEcsFtpLog();
    }
    
    return (FTP_SUCCESS);
}

  long getFileSize(String sPFileName) {
    // verify path string
    String tempPath = SecurityUtil.verifyPath(sPFileName);
    File f = new File(tempPath);
    long lFileLength = f.length();

    return lFileLength;
  }

  // ************************************************************************
  int commSftplog() throws Exception {
    SFtpClient lSFtpClient = null;
    try {

      lSFtpClient = new SFtpClient();

      String sLFtpServerIp = hEriaRefIp;
      String sLUser = hEriaUserId;
      String sLPassword = hEriaUserHidewd;
      if (hEriaPortNo.length() == 0)
        hEriaPortNo = "22";
      int sLPort = Integer.valueOf(hEriaPortNo);

      boolean bLConnResult = lSFtpClient.connect(sLFtpServerIp, sLUser, sLPassword, sLPort);
      if (!bLConnResult) {
        return (FTP_CONNECT_FAIL);
      }

      if (hEriaTransType.equals("A"))
        hEflgTransType = "ASCII";
      else
        hEflgTransType = "BIN";

      String sLLocalPath = SecurityUtil.verifyPath(hEriaLocalDir);
      String sLRemotePath = SecurityUtil.verifyPath(hEriaRemoteDir);

      String sLPattern = "";
      String cmdType = "";
      String ftpCmd = "";
      hEflgTransRespCode = "Y";
      
      showLogMessage("I", "", "[CommFTP] SFTP REMOTE DIR[" + sLRemotePath + "]");
      showLogMessage("I", "", "[CommFTP] SFTP LOCAL  DIR[" + sLLocalPath + "]");
      showLogMessage("I", "", "[CommFTP] SFTP Command[" + hEflgTransData + "]");
      
      boolean isChgSuc = lSFtpClient.changeDir(sLRemotePath);
      if (isChgSuc == false) {
    showLogMessage("I", "", "[CommFTP] SFTP 切換至遠端資料夾錯誤[" + sLRemotePath + "]");
    return FTP_FAIL;
    }
      
      int listCnt = (int) hEflgTransData.chars().filter(ch -> ch == '|').count() + 1;

      for (int inti = 0; inti < listCnt; inti++) {

        // analyze pass command
        ftpCmd = CommFunction.getStr(hEflgTransData, inti + 1, "|");

        int intIndex = ftpCmd.indexOf(" ");
        cmdType = ftpCmd.substring(0, intIndex).toUpperCase();
        sLPattern = ftpCmd.substring(intIndex + 1, ftpCmd.length()).trim();

        int resultCode = FTP_SUCCESS;
        if (cmdType.indexOf("GET") != -1){
          resultCode = runSftpGet(lSFtpClient, sLLocalPath, sLRemotePath, sLPattern);
        }
        else if (cmdType.indexOf("PUT") != -1){
          resultCode = runSftpPut(lSFtpClient, sLLocalPath, sLRemotePath, sLPattern);
        }
        else if (cmdType.indexOf("DELETE") != -1){
          resultCode = runSftpDelete(lSFtpClient, sLPattern);
        }
        else if (cmdType.indexOf("RENAME") != -1){
          resultCode = runSftpRename(lSFtpClient, sLPattern);
        }
        
        if (resultCode != FTP_SUCCESS) {
      return resultCode;
    }
 
      }

    } catch (Exception e) {
      showLogMessage("I", "", e.getMessage());
    } finally {
      lSFtpClient.disconnect();
  }
    return (FTP_SUCCESS);
  }

private int runSftpRename(SFtpClient lSFtpClient, String sLPattern) {
    showLogMessage("I", "", "[CommFTP] SFTP RENAME Pattern[" + sLPattern + "]");
    boolean bLConnResult;
    int index = sLPattern.indexOf(" ");
    String sPSourceFileName = sLPattern.substring(0, index).trim();
    String sPTargetFileName = sLPattern.substring(index + 1, sLPattern.length()).trim();

    bLConnResult = lSFtpClient.renameFile(hEriaRemoteDir, sPSourceFileName, sPTargetFileName);
    if (!bLConnResult)
      showLogMessage("I", "",String.format("[CommFTP] SFTP 檔案[%s] SFTP RENAME TO %s FAILED! ", sPSourceFileName, sPTargetFileName));
    else
      showLogMessage("I", "",String.format("[CommFTP] SFTP 檔案[%s] RENAME TO %s SUCCESS! ", sPSourceFileName, sPTargetFileName));

    return bLConnResult ? FTP_SUCCESS : FTP_FAIL;
}

private int runSftpDelete(SFtpClient lSFtpClient, String sLPattern) throws IOException {
  showLogMessage("I", "", "[CommFTP] SFTP DELETE Pattern[" + sLPattern + "]");
  boolean bLConnResult;
  String sLFullPathRemoteFileName;
  sLFullPathRemoteFileName = Paths.get(hEriaRemoteDir, sLPattern).toString();

    bLConnResult = lSFtpClient.deleteRemoteFile(sLFullPathRemoteFileName);
    if (!bLConnResult){
      showLogMessage("I", "", String.format("[CommFTP] SFTP 檔案[%s] 刪除處理失敗! ", sLFullPathRemoteFileName));
      
    }
    else{
      showLogMessage("I", "", String.format("[CommFTP] SFTP 檔案[%s] 刪除處理完成! ", sLFullPathRemoteFileName));
    }
    return bLConnResult ? FTP_SUCCESS : FTP_FAIL;
}

private int runSftpPut(SFtpClient lSFtpClient, String sLLocalPath, String sLRemotePath, String sLPattern)
    throws IOException, Exception {
    showLogMessage("I", "", "[CommFTP] SFTP PUT Pattern[" + sLPattern + "]");
    String sLFileName;
    hEflgTransMode = "SEND";
    boolean bLResult = lSFtpClient.mputFile(sLRemotePath, sLLocalPath, sLPattern);
    if (bLResult == false)
      return (FTP_FAIL);

    ArrayList<String> lProcessedFileList = lSFtpClient.getProcessedFileList();

    fileList.clear();
    fileList = (ArrayList<String>) lProcessedFileList.clone();

    hEflgTransTotalCnt = lProcessedFileList.size();
    for (int i = 0; i < lProcessedFileList.size(); i++) {
      sLFileName = lProcessedFileList.get(i);
      
      /* Justin 2021-07-16 fix for a bug of finding incorrect file name */
      hEflgFileName = Paths.get(sLFileName).getFileName().toString();
      hEflgTransSeq = i + 1;
      hEflgLocalSize = getFileSize(sLFileName);
      showLogMessage("I", "", String.format("[CommFTP] SFTP 檔案[%s] 傳送處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));
      insertEcsFtpLog();
    }
    return (FTP_SUCCESS);
}

private int runSftpGet(SFtpClient lSFtpClient, String sLLocalPath, String sLRemotePath, String sLPattern)
    throws IOException, Exception {
    showLogMessage("I", "", "[CommFTP] SFTP GET Pattern[" + sLPattern + "]");
    String sLFileName;
    hEflgTransMode = "RECV";
    boolean bLResult = lSFtpClient.mgetFile(sLRemotePath, sLLocalPath, sLPattern);
    if (bLResult == false)
      return (FTP_FAIL);

    ArrayList<String> lProcessedFileList = lSFtpClient.getProcessedFileList();

    fileList.clear();
    fileList = (ArrayList<String>) lProcessedFileList.clone();

    hEflgTransTotalCnt = lProcessedFileList.size();
    if (hEflgTransTotalCnt == 0) {
      return (FTP_NO_FILE_EXIST);
    }
    for (int i = 0; i < lProcessedFileList.size(); i++) {
      sLFileName = lProcessedFileList.get(i);
      
      /* Justin 2021-07-16 fix for a bug of finding incorrect file name */
      hEflgFileName = Paths.get(sLFileName).getFileName().toString();
      hEflgTransSeq = i + 1;
      hEflgLocalSize = getFileSize(sLFileName);
      showLogMessage("I", "", String.format("[CommFTP] SFTP 檔案[%s] 接收處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));    
      insertEcsFtpLog();
    }
    return (FTP_SUCCESS);
}

  private int runMovePut(String sLLocalPath, String sLRemotePath, String sLPattern) throws Exception {
    String sLFileName;
    hEflgTransMode = "SEND";
    showLogMessage("I", "", "[CommFTP] FTP(MOVE) PUT Pattern[" + sLPattern + "]");
    
    ArrayList<String> lProcessedFileList = moveFiles(sLLocalPath, sLRemotePath, sLPattern);

    if (lProcessedFileList == null)
      return (FTP_FAIL);

    fileList.clear();
    fileList = (ArrayList<String>) lProcessedFileList.clone();
    hEflgTransTotalCnt = lProcessedFileList.size();
    showLogMessage("I", "","[CommFTP] FTP(MOVE) file count[" + lProcessedFileList.size() + "]" + sLRemotePath); 
    
    if (hEflgTransTotalCnt == 0) // when file not exist
    {
      return (FTP_NO_FILE_EXIST);
    }
    
    for (int i = 0; i < lProcessedFileList.size(); i++) {
      sLFileName = lProcessedFileList.get(i);
      
      /* Justin 2021-07-16 fix for a bug of finding incorrect file name */
      hEflgFileName = Paths.get(sLFileName).getFileName().toString();        
      hEflgTransSeq = i + 1;
      hEflgLocalSize = getFileSize(sLFileName);
      showLogMessage("I", "", String.format("[CommFTP] FTP(MOVE) 檔案[%s] 從近端[%s]傳送至遠端[%s]", hEflgFileName, sLLocalPath, sLRemotePath));
      showLogMessage("I", "", String.format("[CommFTP] FTP(MOVE) 檔案[%s] 傳送處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));

      insertEcsFtpLog();
    }
    
    return FTP_SUCCESS;
  }

  private int runMoveGet(String sLLocalPath, String sLRemotePath, String sLPattern) throws Exception {
    String sLFileName;
    hEflgTransMode = "RECV";
    showLogMessage("I", "", "[CommFTP] FTP(MOVE) GET Pattern[" + sLPattern + "]");

    ArrayList<String> lProcessedFileList = moveFiles(sLRemotePath, sLLocalPath, sLPattern);

    if (lProcessedFileList == null)
      return (FTP_FAIL);

    fileList.clear();
    fileList = (ArrayList<String>) lProcessedFileList.clone();

    hEflgTransTotalCnt = lProcessedFileList.size();
    if (hEflgTransTotalCnt == 0)
      return (FTP_NO_FILE_EXIST);
    
    showLogMessage("I", "", "[CommFTP] FTP(MOVE) file count[" + lProcessedFileList.size() + "]");
    
    for (int i = 0; i < lProcessedFileList.size(); i++) {
      sLFileName = lProcessedFileList.get(i);
      
      /* Justin 2021-07-16 fix for a bug of finding incorrect file name */
      hEflgFileName = Paths.get(sLFileName).getFileName().toString();
      hEflgTransSeq = i + 1;
      hEflgLocalSize = getFileSize(sLFileName);
      showLogMessage("I", "", String.format("[CommFTP] FTP(MOVE) 檔案[%s] 從遠端[%s]傳送至近端[%s]", hEflgFileName, sLRemotePath, sLLocalPath));
      showLogMessage("I", "", String.format("[CommFTP] FTP(MOVE) 檔案[%s] 接收處理完成!  Size =[%.0f]", hEflgFileName, hEflgLocalSize));
      insertEcsFtpLog();
    }
    
    return (FTP_SUCCESS);
  }
  
  private int runDelete(String sLRemotePath, String sLPattern) throws Exception {
	    String sLFileName;
	    hEflgTransMode = "RECV";
	    showLogMessage("I", "", "[CommFTP] FTP(DELETE) Pattern[" + sLPattern + "]");

	    ArrayList<String> lProcessedFileList = deleteFiles(sLRemotePath, sLPattern);

	    if (lProcessedFileList == null)
	      return (FTP_FAIL);

	    fileList.clear();
	    fileList = (ArrayList<String>) lProcessedFileList.clone();

	    hEflgTransTotalCnt = lProcessedFileList.size();
	    if (hEflgTransTotalCnt == 0)
	      return (FTP_NO_FILE_EXIST);
	    
	    showLogMessage("I", "", "[CommFTP] FTP(DELETE) file count[" + lProcessedFileList.size() + "]");
	    
	    for (int i = 0; i < lProcessedFileList.size(); i++) {
	      sLFileName = lProcessedFileList.get(i);
	      
	      /* Justin 2021-07-16 fix for a bug of finding incorrect file name */
	      hEflgFileName = Paths.get(sLFileName).getFileName().toString();
	      hEflgTransSeq = i + 1;
	      hEflgLocalSize = getFileSize(sLFileName);
	      showLogMessage("I", "", String.format("[CommFTP] FTP(DELETE) 刪除遠端檔案[%s/%s]", sLRemotePath ,hEflgFileName ));
	      insertEcsFtpLog();
	    }
	    
	    return (FTP_SUCCESS);
	  }
  
  
  
  /**
   * return null if error occurs else return fileNameList
   * @return
   * @throws Exception
   */
  private ArrayList<String> moveFiles(String sourceFolder, String targetFolder, String pattern) throws Exception {
        
        sourceFolder = SecurityUtil.verifyPath(sourceFolder);
        
        File[] files = listFiles(sourceFolder, pattern);
        
    if (files == null) {
      showLogMessage("I", "","此路徑並非資料夾 或  I/O error occurs.");
      return null;
    }
    
    ArrayList<String> fileNameList = new ArrayList<String>();
    
    boolean moveResult =true;
    for (int i = 0; i < files.length; i++) {
      
      String fileName = files[i].getName();
      String sourceFilePath = sourceFolder + "/" + fileName;
      String targetFilePath =  targetFolder + "/" + fileName;
      
      sourceFilePath = SecurityUtil.verifyPath(sourceFilePath);
      targetFilePath = SecurityUtil.verifyPath(targetFilePath);
      
      moveResult = comc.fileCopy(sourceFilePath, targetFilePath);
      
      if (moveResult == false) {
        break;
      }
      
      fileNameList.add(targetFilePath);
    }
    
    if (moveResult == false) {
      showLogMessage("I", "","檔案移動錯誤");
      return null;
    }else {
      return fileNameList;
    }

  }
  
  private ArrayList<String> deleteFiles(String sourceFolder, String pattern) throws Exception {
      
      sourceFolder = SecurityUtil.verifyPath(sourceFolder);
      
      File[] files = listFiles(sourceFolder, pattern);
      
  if (files == null) {
    showLogMessage("I", "","此路徑並非資料夾 或  I/O error occurs.");
    return null;
  }
  
  ArrayList<String> fileNameList = new ArrayList<String>();
  
  boolean moveResult =true;
  for (int i = 0; i < files.length; i++) {
    
    String fileName = files[i].getName();
    String sourceFilePath = sourceFolder + "/" + fileName;

    sourceFilePath = SecurityUtil.verifyPath(sourceFilePath);
    
    moveResult = comc.fileDelete(sourceFilePath);
    
    if (moveResult == false) {
      break;
    }
    
    fileNameList.add(sourceFilePath);
  }
  
  if (moveResult == false) {
    showLogMessage("I", "","檔案刪除錯誤");
    return null;
  }else {
    return fileNameList;
  }

}
  
  private File[] listFiles(String folderPath, String pattern) {
      folderPath = SecurityUtil.verifyPath(folderPath);
      File file = new File(folderPath);

      return file.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return ifStartProcess(name, pattern);
        }
      });
  }

  private boolean ifStartProcess(String sPFileName, String sPPattern) {

        boolean bLResult = false;
        String sLRealPattern = "";
        int nLStarFlagPos = sPPattern.indexOf("*");

        if (sPPattern.equals("*"))
          bLResult = true;
        else {
          // if(sP_Pattern.substring(0,1).equals("*")) { //sP_Pattern 的第一碼是"*", 譬如 "*aa"
          if (nLStarFlagPos == 0) { // sP_Pattern 的第一碼是"*", 譬如 "*aa"
            sLRealPattern = sPPattern.substring(1, sPPattern.length());

            if (sPFileName.length() >= sLRealPattern.length()) {
              if (sPFileName.substring(sPFileName.length() - sLRealPattern.length(),
                  sPFileName.length()).equals(sLRealPattern)) {
                bLResult = true;
              }
            }
          }
          // else if(sP_Pattern.substring(sP_Pattern.length()-1, sP_Pattern.length()).equals("*")) {
          // //sP_Pattern 的最後一碼是"*", 譬如 "bb*"
          else if (nLStarFlagPos == sPPattern.length() - 1) { // sP_Pattern 的最後一碼是"*", 譬如 "bb*"
            sLRealPattern = sPPattern.substring(0, sPPattern.length() - 1);

            if (sPFileName.length() >= sLRealPattern.length()) {
              if (sPFileName.substring(0, sLRealPattern.length()).equals(sLRealPattern))
                bLResult = true;
            }
          } else if ((nLStarFlagPos > 0) && (nLStarFlagPos < sPPattern.length() - 1)) { // sP_Pattern
                                                                                           // 的中間有一個"*",
                                                                                           // 譬如 "a*b"

            if (sPFileName.length() >= sPPattern.length()) {
              String sLPatrernHead = sPPattern.substring(0, nLStarFlagPos);
              String sLPatrernTail = sPPattern.substring(nLStarFlagPos + 1, sPPattern.length());

              String sLFileNameHead = sPFileName.substring(0, sLPatrernHead.length());
              String sLFileNameTail =
                  sPFileName.substring(sPFileName.length() - sLPatrernTail.length(),
                      sPFileName.length());

              if ((sLFileNameHead.equals(sLPatrernHead)) && (sLFileNameTail.equals(sLPatrernTail))) {
                bLResult = true;
              }

            }
          } else if (sPPattern.substring(sPPattern.length() - 1, sPPattern.length()).equals("?")) { // sP_Pattern
                                                                                                       // 的最後一碼是"?",
                                                                                                       // 譬如
                                                                                                       // "bb?"
            sLRealPattern = sPPattern.substring(0, sPPattern.length() - 1);


            if (sPFileName.indexOf(sLRealPattern) >= 0)
              bLResult = true;

          } else {
            sLRealPattern = sPPattern;
            if (sPFileName.equals(sPPattern))
              bLResult = true;

          }
        }
        return bLResult;
      }
  
	public int insertEcsNotifyLog(String fileName, String objType, String javaProgram, String sysDate, String sysTime) throws Exception {

		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", new CommRoutine(getDBconnect(), getDBalias()).getObjectOwner(objType, javaProgram));
		setValue("obj_type", objType);
		setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		
	    daoTable = "ECS_NOTIFY_LOG";

	    insertTable();

	    return (0);
	  }

// ************************************************************************
  void initDefault() {
    hEriaRefIpCode = "";
    hEriaRefIp = "";
    hEriaRefName = "";
    hEriaUserId = "";
    hEriaUserHidewd = "";
    hEriaTransType = "";
    hEriaPortNo = "";
    hEriaFtpType = "";
    hEflgTransTotalCnt = 0;
    hEflgTransData = "";
    hEflgTransType = "";
    hEflgTransSeq = 0;
    hEflgLocalSize = 0;
    hEflgTransSize = 0;
    hEflgFileName = "";
    hEflgTransMode = "";
    hEflgTransRespCode = "";
    hEflgTransDesc = "";
    hEriaFileZipHidewd = "";
    hEriaFileUnzipHidewd = "";

  }
  // ************************************************************************



} // End of class CommFTP


