/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/21  V1.00.01  Justin Wu   remove the action to change dir before get, put, rename, and delete*
* 110/09/01  V1.00.02  Justin Wu   fix a null instance bug
* 111/01/18  V1.00.03  Justin Wu   fix Throw Inside Finally                  *
******************************************************************************/
package Dxc.Util.Ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

public class FtpClient extends FtpBase{



	private FTPClient client;

	 /**
	  * 
     * 建立 FTP object
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03
     * @param i : 如果bP_Ftps==true，建立 Ftps object；否則建立 Ftp object
     * 
     * @return 如果成功return true，否則return  false
     * 
    */

	public FtpClient(boolean i) {
		if (i) {
			try {
				client = new FTPSClient(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			client = new FTPClient();
			
		}
	}

	 /**
     * 更改遠端目錄
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remotePath :remote path
     * 
     * @return 如果成功return true，否則return  false
     * 
    */

	public boolean changeDir(String remotePath) throws Exception {
		return client.changeWorkingDirectory(remotePath);
	}

	 /**
     * 設定檔案傳輸之格式
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03 
     * @param fileType : 1=> ASCII mode, 2=> BINARY mode
     * 
     * @return 如果成功return true，否則return  false
     * 
    */

	public boolean setTransFileType(int fileType) throws Exception{
		//Howard: 1=> ASCII mode, 2=> BINARY mode
		
		if (fileType==1)
			client.setFileType(FTP.ASCII_FILE_TYPE);
		else 
			client.setFileType(FTP.BINARY_FILE_TYPE);
		
		
		return true;
	}
	
    /**
     * 登入 FTP Server
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03
     * @param host : FTP Server IP
     * @param loginId : FTP Server User Id
     * @param loginKey : FTP Server User Password
     * @param port : FTP Server Port
     * @return 如果登入成功return true，否則return false
     * 
    */
	public boolean connect(String host, String loginId, String loginKey, int port, boolean isActive)  {
		boolean result = true;
		
		try {
			client.connect(host, port);
			int reply = client.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {

				if (client.login(loginId, loginKey)) {	
					if (isActive) {
						client.enterLocalActiveMode();
					}else {
						client.enterLocalPassiveMode();
					}
				}
				else {
					result = false;
				}
			}
			
		} catch (Exception e) {
			result = false;
		}

		return result;
	}
	
    /**
     * 登入 FTP Server
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03
     * @param host : FTP Server IP
     * @param loginId : FTP Server User Id
     * @param loginKey : FTP Server User Password
     * @param port : FTP Server Port
     * @return 如果登入成功return true，否則return false
     * 
    */
	public boolean connect(String host, String loginId, String loginKey, int port)  {
		return connect(host, loginId, loginKey, port, false);
	}

	/**
	 * FTP Server 斷線
	 * @throws Exception
	 */
	public boolean disconnect() {
		
		try {
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}


	protected boolean downloadFileAfterCheck(String remotePath, String localFile) throws IOException {

		boolean rst;
		try {
			File file = new File(localFile);
			if (!file.exists()) {
				try(FileOutputStream out = new FileOutputStream(localFile);){
					rst = client.retrieveFile(remotePath, out);
				}
			} else {
				rst = true;
			}
		} catch (Exception e) {
			rst = false;
		}

		return rst;
	}

    /**
     * 下載檔案 (get)
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remoteFileName :remote file name
     * @param localFileName : local file name
     * 
     * @return 如果成功return true，否則return  false
     * 
    */

	public boolean downloadFile(String remoteFileName, String localFileName) throws IOException {

		boolean rst;

		try (FileOutputStream out = new FileOutputStream(localFileName);) {
			
			rst = client.retrieveFile(remoteFileName, out);		
			addFtpFileName(localFileName);
		} catch (Exception e) {
			rst = false;
		} 
		return rst;
	}


	/**
     * 下載檔案 (mget)
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remotePath :remote path
     * @param localPath : local path
     * @param pattern : 檔案樣式
     * 	      sP_Pattern = "*txt"; // 取回 檔名末三碼 = "txt" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg*"; // 取回 檔名前三碼 = "msg" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg_07.txt"; // 取回 檔名  = "msg_07.txt" 的檔案
 			  sP_Pattern = "_0?"; // 取回 檔名含有 = "_0" 的檔案
 			  sP_Pattern = "abc*xyz"; // 取回 檔名前三碼 = "abc" and 檔名末三碼 = "xyz" 的檔案 ( 有分大小寫 )
  
     * @return 如果成功return true，否則return  false
     * 
    */
	public boolean mgetFile(String remotePath, String localPath, String pattern) throws IOException {

		boolean result=true, downloadFile=false;
		String remoteFileName = "";
		clearProcessedFileList();
		 FTPFile[] files= client.listFiles();
		 if(files != null && files.length >0 ) {
			 for(FTPFile fl:files) {
				 if(!fl.isFile()) {
					 continue;
	             }
				 remoteFileName = fl.getName().trim();
				 remoteFileName = remoteFileName.replaceAll(" ", "");
				 downloadFile = ifStartProcess(remoteFileName, pattern);
				 if (downloadFile) {
					 try {
						 String fullPathLocalFileName = Paths.get(localPath, remoteFileName).toString();
						 String fullPathRemoteFileName = remoteFileName;
						 result = downloadFile(fullPathRemoteFileName, fullPathLocalFileName);
					 } catch (Exception e) {
						 result = false;
						 break;
					 } 
					 
				 }
			 }
	            
		 }
		return result;
	}

	/**
	 *      刪除檔案 (mDelete)
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03          
     * @param remotePath :remote path
     * @param pattern :
     * 	      sP_Pattern = "*txt"; // 刪除 檔名末三碼 = "txt" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg*"; // 刪除 檔名前三碼 = "msg" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg_07.txt"; // 刪除 檔名  = "msg_07.txt" 的檔案
 			  sP_Pattern = "_0?"; // 刪除 檔名含有 = "_0" 的檔案
 			  sP_Pattern = "Msg*jar"; //刪除 檔名前三碼 = "Meg" and 檔名末三碼 = "jar" 的檔案 ( 有分大小寫 )
  
     * @return 如果成功return true，否則return  false
	 * @throws IOException
	 */	               
	public boolean mdeleteFile(String remotePath, String pattern) throws IOException {

		boolean result = true, deleteFile = false;
		String remoteFileName = "";

		clearProcessedFileList();
		FTPFile[] files = client.listFiles();
		if (files != null && files.length > 0) {
			for (FTPFile fl : files) {
				if (!fl.isFile()) {
					continue;
				}
				remoteFileName = fl.getName();
				deleteFile = ifStartProcess(remoteFileName, pattern);
				if (deleteFile) {

					try {
						result = deleteRemoteFile(remoteFileName);
						if (!result){
							break;
						}
					} catch (Exception e) {
						result = false;
					}
				}
			}

		}
		return result;
	}

    /**
     * 取回遠端檔案 (mPut)
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remotePath :遠端目錄名稱
     * @param localDir :本機目錄名稱
     * @param pattern ：檔案樣式
     *        sP_Pattern = "*txt"; // 取回 檔名末三碼 = "txt" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg*"; // 取回 檔名前三碼 = "msg" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg_07.txt"; // 取回 檔名  = "msg_07.txt" 的檔案
 			  sP_Pattern = "_0?"; // 取回 檔名含有 = "_0" 的檔案

     * @return 如果成功return true，否則return false
     * 
    */

	public boolean mputFile(String remotePath, String localDir, String pattern) throws IOException {

		boolean result =true, uploadFile=false;
		
		File folder = new File(localDir);
		 String localFileName="";
		 for (final File fileEntry : folder.listFiles()) {
			 if (fileEntry.isDirectory()) {
				 continue;
		     }

			 localFileName = fileEntry.getName();
			 uploadFile = ifStartProcess(localFileName,pattern );

			 if (uploadFile) {
				 String fullPathLocalFileName = Paths.get(localDir, localFileName).toString() ;
				 String remoteFileName = localFileName;	 
				 result = uploadFile(fullPathLocalFileName, remoteFileName );
				 if (!result){
					 break;
				 }
			 }

		 }
		return result;
	}

    /**
     * 刪除遠端檔案
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remotePathFileName :遠端檔案名稱
     * @return 如果成功return true，否則return false
     * 
    */

	public boolean deleteRemoteFile(String remotePathFileName) throws IOException {

		boolean rst=false;

		try {
			FTPFile[] files = client.listFiles();

			for (FTPFile file : files) {
				if (!file.isDirectory()) {
					if (remotePathFileName.equals(file.getName())) {
						rst = client.deleteFile(remotePathFileName);
						break;
					}
					
				}
			}
			
		} catch (Exception e) {
			rst = false;
		} finally {
		}
		return rst;
	}
	
    /**
     * 刪除遠端檔案
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remoteFullPathFileName : 完整路徑遠端檔案名稱
     * @return 如果成功return true，否則return false
	 * @throws IOException 
    */

	public boolean deleteRemoteFullPathFile(String remoteFullPathFileName) throws IOException {

		boolean rst=true;

		try {
			rst = client.deleteFile(remoteFullPathFileName);			
		} catch (Exception e) {
			rst = false;
		} finally {
		}
		return rst;
	}

    /**
     * 取得遠端目錄的檔案清單
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remoteDir :遠端目錄名稱
     * @return 如果成功return Vector<String>，否則return null
     * 
    */

	public Vector<String> listFileInDir(String remoteDir) {
		
		try {
			if (changeDir(remoteDir)) {
				FTPFile[] files = client.listFiles();
				Vector<String> v = new Vector<String>();
				for (FTPFile file : files) {
					if (!file.isDirectory()) {
						v.addElement(file.getName());
					}
				}
				return v;
			} else {
				return null;
			}
			
		} catch (Exception e) {
			return null;
		}
	}

	 /**
     * 上傳檔案
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remotePath :remote path
     * @param localFilePath : local file name
     * 
     * @return 如果成功return true，否則return  false
     * 
    */

	public boolean uploadFile(String localFilePath, String remotePath) throws IOException {
		
		boolean rst;
		try (FileInputStream in = new FileInputStream(localFilePath);) {
			rst = client.storeFile(remotePath, in);		
			addFtpFileName(localFilePath);
		}
		return rst;
	}
	
	
    /**
     * 取得遠端目錄的 子目錄清單
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remoteDir :遠端目錄名稱
     * @return 如果成功return Vector<String>，否則return null
     * 
    */

	public Vector<String> getSubDir(String remoteDir) throws Exception {
		
		if (changeDir(remoteDir)) {
			FTPFile[] files = client.listFiles();
			Vector<String> v = new Vector<String>();
			for (FTPFile file : files) {
				if (file.isDirectory()) {
					v.add(file.getName());
				}
			}
			return v;
		} else {
			return null;
		}
	}

    /**
     * 建立 遠端目錄
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03    
     * @param remoteDirName :目錄名稱 
     * @return 如果成功return true，否則return false
     * 
    */

	protected boolean createDirectory(String remoteDirName) {
		try {
			return client.makeDirectory(remoteDirName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 更改檔案名稱
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/11/14  
     * @param remoteDir : 遠端目錄
	 * @param sourceFileName : 舊檔名
	 * @param targetFileName : 新檔名
	 * @return
	 */
	public boolean renameFile(String remoteDir, String sourceFileName, String targetFileName) {

		boolean result = true;
		try {
			result = client.rename(sourceFileName, targetFileName);
		} catch (Exception e) {
			result = false;
		} finally {
		}

		return result;
	}

	 /**
     * 取得遠端目錄名稱
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @return 如果成功return 遠端目錄名稱，否則return 空字串
     * 
    */

	public String getWorkingDirectory() {
		String remoteCurDir = "";
		try {
			remoteCurDir = client.printWorkingDirectory(); 
			
		} catch (IOException e) {
			remoteCurDir = "";
		}
		return remoteCurDir;
	}
	
	public String getReplyStr() {
		if (client == null) {
			return "FTP instance is null";
		}
		return client.getReplyString().replace("\n", "");
	}


}