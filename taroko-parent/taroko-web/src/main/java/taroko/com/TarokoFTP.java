/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-24  V1.00.01  Zuwei       coding standard      *
* 109-08-03  V1.00.01  Zuwei       fix code scan issue                       *
*                                                                            *  
******************************************************************************/
package taroko.com;
/*webAP FTP公用程式 V.2018-0831.jh
 * 2018-0831:	JH		_remotePath/2
 * 
 * */
import java.util.ArrayList;
import Dxc.Util.Ftp.FtpClient;
import Dxc.Util.Ftp.SFtpClient;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoFTP extends taroko.base.BaseSQL {
  // taroko.base.commString commString = new taroko.base.commString();

  String hostName = "", userId = "", pawd = "";
  int ftpType = 2, portNo = 22; // 1.FTP, 2.sFTP, 3.??
  public String localPath = "", fileName = "", ftpMode = "";
  public boolean deleteFile = false;
  public int procFileCnt = 0;

  static final boolean FtpMode = false;
  static final boolean FtpsMode = true;
  int cmdType = 1; // 1.get, 2.send, 3.delete
  taroko.com.TarokoCommon wp = null;
  private String remotePath = "", remotePath2 = "";

  // String _msg="";
  // int rc=1;

  public void setHostName(String aIpAddr, String aUser, String aPasswd) {
    hostName = aIpAddr;
    userId = aUser;
    pawd = aPasswd;
  }

  public void setRemotePath2(String aPath) {
    remotePath2 = aPath;
  }

  public void setRemotePath(String apath) {
    remotePath = apath;
  }

  public void setHostName(String aIpCode) throws Exception {
    String lsIpCode = commString.nvl(aIpCode, "TAROKO_FTP");
    String sql1 =
        "select ref_ip," + " ref_name," + " user_id," + " user_hidewd," + " ftp_type,"
            + " trans_type," + " remote_dir," + " local_dir," + " port_no," + " file_zip_hidewd,"
            + " file_unzip_hidewd " + " from ecs_ref_ip_addr" + " where ref_ip_code =?";
    setString2(1, lsIpCode);
    sqlSelect(wp.conn(), sql1, sqlParm());
    if (sqlRowNum <= 0) {
      this.errmsg("select ecs_ref_id_addr, kk[%s]", lsIpCode);
      return;
    }

    hostName = sqlStr("ref_ip");
    userId = sqlStr("user_id");
    pawd = sqlStr("user_hidewd");
    portNo = sqlInt("port_no");
    if (empty(remotePath)) {
      this.remotePath = sqlStr("remote_dir");
    }
    if (empty(remotePath2) == false) {
      remotePath += "/" + remotePath2;
    }
  }

  // void errmsg(String msg) {
  // _msg =msg;
  // wp.ddd(_msg);
  // }
  public int getFile(taroko.com.TarokoCommon wr) throws Exception {
    wp = wr;
    cmdType = 1;
    if (empty(hostName)) {
      setHostName("");
      if (rc != 1) {
        return rc;
      }
    }
    return ftpFile();
  }

  public int putFile(taroko.com.TarokoCommon wr, boolean bDelete) throws Exception {
    wp = wr;
    cmdType = 2;
    if (empty(hostName)) {
      setHostName("");
      if (rc != 1) {
        return rc;
      }
    }
    int liRc = ftpFile();
    if (liRc != 0 || !bDelete)
      return liRc;

    taroko.com.TarokoFileAccess oofile = new taroko.com.TarokoFileAccess(wr);
    String lsFile = localPath + "/" + fileName;
    if (oofile.fileDelete(lsFile)) {
      wp.log("file delete OK, [%s]", lsFile);
    } else {
      wp.log("file delete Fail, [%s]", lsFile);
    }

    return liRc;
  }

  public int putFile(taroko.com.TarokoCommon wr) throws Exception {
    return putFile(wr, true);
  }

  int ftpFile() throws Exception {
    procFileCnt = 0;
    int retInt = toFtplog();

    switch (retInt) {
      case 0:
        errmsg("遠端 FTP 作業正常結束!");
        break;
      case 1:
        errmsg("ECS_REF_IP_ADDR 檔某些值為空值!");
        break;
      case 2:
        errmsg("遠端 FTP 連線失敗!");
        break;
      case 3:
        errmsg("遠端 FTP 無檔案傳輸!");
        break;
      case 5:
        errmsg("ECS_REF_IP_ADDR 未定義 [ pass_ref_ip_code ]");
        break;
    }
    return (retInt);
  }

  int toFtplog() throws Exception {
    if (commString.empty(hostName))
      return 1;

    int retInt = 0;

    if (ftpType == 1)
      retInt = commFtplog(FtpMode);
    else if (ftpType == 2)
      retInt = commSftplog();
    // else if (h_eria_ftp_type.equals("3"))
    // ret_int = comm_ftplog(FtpsMode);

    return (retInt);
  }

  int commFtplog(boolean bFtpMode) throws Exception {
    FtpClient lFtpClient = new FtpClient(bFtpMode);

    String sLFtpServerIp = hostName;
    String sLUser = userId;
    String sLPassword = pawd;
    int sLPort = 21;

    boolean bLConnResult = lFtpClient.connect(sLFtpServerIp, sLUser, sLPassword, sLPort);
    if (!bLConnResult) {
      return 2;
    }
    if (commString.strIn2(ftpMode, ",A,ASC,ASCII")) {
      lFtpClient.setTransFileType(1); // 1=> ASCII mode, 2=> BINARY mode
    } else {
      lFtpClient.setTransFileType(2); // 1=> ASCII mode, 2=> BINARY mode
    }
    /* demo mget */
    String sLLocalPath = this.localPath;
    String sLRemotePath = this.remotePath;
    // String sL_FullPathRemoteFileName = "";

    // "*txt" //Howard: 取回 檔名末三碼 = "txt" 的檔案 ( 有分大小寫 )
    // "msg*" //Howard: 取回 檔名前三碼 = "msg" 的檔案 ( 有分大小寫 )
    // "msg_07.txt" //Howard: 取回 檔名 = "msg_07.txt" 的檔案
    // "_0?" //Howard: 取回 檔名含有 = "_0" 的檔案

    String sLPattern = this.fileName;
    String sLFileName = "";

    // cmdTypeInt = 0;
    // if (cmdType.indexOf("GET")!=-1) cmdTypeInt = 1;
    // else if (cmdType.indexOf("PUT")!=-1) cmdTypeInt = 2;
    // else if (cmdType.indexOf("DELETE")!=-1) cmdTypeInt = 3;

    // fileTypeInt = 1;
    // if (sL_Pattern.indexOf("*") != -1)
    // fileTypeInt = 2;

    if (cmdType == 1) {
      // h_eflg_trans_mode = "RECV";
      lFtpClient.mgetFile(sLRemotePath, sLLocalPath, sLPattern);

      ArrayList<String> lProcessedFileList = lFtpClient.getProcessedFileList();
      for (int ii = 0; ii < lProcessedFileList.size(); ii++) {
        sLFileName = lProcessedFileList.get(ii);
        long llSize = lFtpClient.getFileAttributes(sLFileName).size();
        wp.log("-->%s 檔案 FTP 接收處理完成!  Size =[%s]", sLFileName, llSize);
      }
    } else if (cmdType == 2) {
      // h_eflg_trans_mode = "SEND";
      lFtpClient.mputFile(sLRemotePath, sLLocalPath, sLPattern);

      ArrayList<String> lProcessedFileList = lFtpClient.getProcessedFileList();
      for (int ii = 0; ii < lProcessedFileList.size(); ii++) {
        sLFileName = lProcessedFileList.get(ii);
        long llSize = lFtpClient.getFileAttributes(sLFileName).size();
        wp.log("-->%s 檔案 FTP 傳送處理完成!  Size =[%s]", sLFileName, llSize);
      }
    } else if (cmdType == 3) {
      lFtpClient.changeDir(this.remotePath);
      bLConnResult = lFtpClient.deleteRemoteFile(sLPattern);
      if (!bLConnResult)
        wp.log(this.fileName + " 檔案 FTP 刪除處理失敗! ");
      else {
        wp.log(fileName + " 檔案 FTP 刪除處理完成! ");
        errmsg(fileName + " 檔案 FTP 刪除處理完成! ");
      }
    }

    lFtpClient.disconnect();
    return 0;
  }

  int commSftplog() throws Exception {
    SFtpClient lSFtpClient = new SFtpClient();

    String sLFtpServerIp = hostName;
    String sLUser = userId;
    String sLPawd = pawd;
    int sLPort = 22;

    wp.log("connect:111: ip[%s], user[%s], pass[%s], port[%s]", sLFtpServerIp, sLUser,
        sLPawd, sLPort);
    boolean bLConnResult = lSFtpClient.connect(sLFtpServerIp, sLUser, sLPawd, sLPort);
    if (!bLConnResult) {
      return 2;
    }
    wp.log("connect:222");

    /* demo mget */
    String sLLocalPath = localPath;
    String sLRemotePath = remotePath;
    String sLFullPathRemoteFileName = "";

    // "*txt" //Howard: 取回 檔名末三碼 = "txt" 的檔案 ( 有分大小寫 )
    // "msg*" //Howard: 取回 檔名前三碼 = "msg" 的檔案 ( 有分大小寫 )
    // "msg_07.txt" //Howard: 取回 檔名 = "msg_07.txt" 的檔案
    // "_0?" //Howard: 取回 檔名含有 = "_0" 的檔案

    String sLPattern = fileName;
    String sLFileName = "";

    int fileTypeInt = 1;
    if (sLPattern.indexOf("*") != -1)
      fileTypeInt = 2;

    if (cmdType == 1) {
      // h_eflg_trans_mode = "RECV";
      lSFtpClient.mgetFile(sLRemotePath, sLLocalPath, sLPattern);

      ArrayList<String> lProcessedFileList = lSFtpClient.getProcessedFileList();
      procFileCnt = lProcessedFileList.size();
      for (int ii = 0; ii < lProcessedFileList.size(); ii++) {
        sLFileName = lProcessedFileList.get(ii);
        long llSize = lSFtpClient.getFileAttributes(sLFileName).size();
        wp.log(sLFileName + " 檔案 FTP 接收處理完成!  Size =[" + llSize + "]");
      }
    } else if (cmdType == 2) {
      // h_eflg_trans_mode = "SEND";
      wp.log("-333->put: rpath[%s], lpath[%s], file[%s]", sLRemotePath, sLLocalPath, sLPattern);
      lSFtpClient.mputFile(sLRemotePath, sLLocalPath, sLPattern);

      ArrayList<String> lProcessedFileList = lSFtpClient.getProcessedFileList();
      procFileCnt = lProcessedFileList.size();
      wp.log("file size:[%s]", lProcessedFileList.size());
      for (int ii = 0; ii < lProcessedFileList.size(); ii++) {
        sLFileName = lProcessedFileList.get(ii);
        long llSize = lSFtpClient.getFileAttributes(sLFileName).size();
        wp.log(sLFileName + " 檔案 FTP 傳送處理完成!  Size =[" + llSize + "]");
      }
      wp.log("-444->put OK");
    } else if (cmdType == 3) {

      if (this.remotePath.indexOf("\\") != -1)
        sLFullPathRemoteFileName = remotePath + "\\" + sLPattern;
      else
        sLFullPathRemoteFileName = remotePath + "/" + sLPattern;

      bLConnResult = lSFtpClient.deleteRemoteFile(sLFullPathRemoteFileName);
      if (!bLConnResult)
        wp.log(fileName + " 檔案 FTP 刪除處理失敗! ");
      else
        wp.log(fileName + " 檔案 FTP 刪除處理完成! ");
    }

    lSFtpClient.disconnect();
    return (0);
  }

  // public boolean putFile(TarokoCommon wp) {
  // try {
  // wp.showLogMessage("D", "putFile", "started");
  // FTPClient ftp = null;
  // ftp = new FTPClient(_hostName);
  // ftp.login(_userId, _passWd);
  //
  // String localFile = localPath + "/" + fileName;
  // ftp.chdir(remotePath);
  // if (commString.ssIN(ftpMode.toUpperCase(),",B,BIN,BINARY")) {
  // ftp.setType(FTPTransferType.BINARY);
  // }
  // else {
  // ftp.setType(FTPTransferType.ASCII);
  // }
  //
  // ftp.put(localFile, fileName, false);
  // wp.showLogMessage("D", "putFile", "ended");
  // } // end of try
  // catch (Exception ex) {
  // ex.printStackTrace();
  // return false;
  // }
  //
  // return true;
  // } // End putFile

  // public int getFile(TarokoCommon wp) {
  // try {
  // wp.showLogMessage("D", "getFile", "started");
  // FTPClient ftp = null;
  // ftp = new FTPClient(_hostName);
  // ftp.login(_userId, _passWd);
  //
  // String localFile = localPath + "/" + fileName;
  // ftp.chdir(remotePath);
  // if (ftpMode.equals("BIN")) {
  // ftp.setType(FTPTransferType.BINARY);
  // }
  // else {
  // ftp.setType(FTPTransferType.ASCII);
  // }
  //
  // ftp.get(localFile, fileName);
  // wp.showLogMessage("D", "getFile", "ended");
  // } // end of try
  //
  // catch (Exception ex) {
  // ex.printStackTrace();
  // // errmsg(ex.getMessage());
  // return -1;
  // }
  //
  // return 0;
  // } // End getFile

} // end of class
