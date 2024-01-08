/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/21  V1.00.01  Justin Wu   remove the action to change dir before get, put, rename, and delete                      *
* 110/09/01  V1.00.02  Justin Wu   comment an unused function
* 111-01-18  V1.00.03  Justin     fix Throw Inside Finally                  *
******************************************************************************/
package Dxc.Util.Ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Vector;


import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpsClient extends FtpBase{



	private FTPSClient client;

	 /**
	  * 
     * 建立 FTP object
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03
     * @param bP_Ftps : 如果bP_Ftps==true，建立 Ftps object；否則建立 Ftp object
     * 
     * 
    */

	public FtpsClient(boolean isImplicit) {
			try {
				
				// Justin
				// SKIP FTPS server's certificate authentication 
//				TrustManager trustManager = getTrustManagerSkippingAuthentication(); 
				TrustManager trustManager = getTrustManagerAllowingExpiredCert();
				client = new FTPSClient("TLS", true); 
				client.setTrustManager(trustManager);
				
				// Justin
				// authenticate FTPS server's certificate
				// use SSL and specify whether to use implicit mode
//				client = new FTPSClient("SSL", isImplicit); // Justin			
				
				// Justin
				// set connection timeout to 10 seconds
				client.setConnectTimeout(10000);
				

				
			} catch (Exception e) {
				e.printStackTrace();
			}
	
	}



		 /**
     * 更改遠端目錄
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param sP_RemotePath :remote path
     * 
     * @return 如果成功return true，否則return  false
     * 
    */

	public boolean changeDir(String sP_RemotePath) throws Exception {
		return client.changeWorkingDirectory(sP_RemotePath);
	}

	 /**
     * 設定檔案傳輸之格式
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03 
     * @param nP_FileType : 1=> ASCII mode, 2=> BINARY mode
     * 
     * @return 如果成功return true，否則return  false
     * 
    */

	public boolean setTransFileType(int nP_FileType) throws Exception{
		//Howard: 1=> ASCII mode, 2=> BINARY mode
		
		if (nP_FileType==1)
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
     * @param sP_Host : FTP Server IP
     * @param sP_LoginId : FTP Server User Id
     * @param sP_LoginKey : FTP Server User Password
     * @param nP_Port : FTP Server Port
     * @param isImplicit : is FTPS Mode Implicit Mode
     * @return 如果登入成功return true，否則return false
     * 
    */
 
	public boolean connect(String sP_Host, String sP_LoginId, String sP_LoginKey, int nP_Port, boolean isImplicit)  {
		boolean bL_Result = true;
		
		try {
			client.connect(sP_Host, nP_Port);
			int reply = client.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {
				
				if (client.login(sP_LoginId, sP_LoginKey)) {
					client.enterLocalPassiveMode();
					client.execPBSZ(0);
					client.execPROT("P");
				}else {
					System.out.println(sP_LoginId + "登入失敗");
					printReplyMesg();
					bL_Result = false;
				}
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			bL_Result = false;
		}

		return bL_Result;
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
		File file = new File(localFile);
		if (!file.exists()) {
			try(FileOutputStream out = new FileOutputStream(localFile);){
				rst = client.retrieveFile(remotePath, out);
			}
		} else {
			rst = true;
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
		try (FileOutputStream out = new FileOutputStream(localFileName);){
			
			rst = client.retrieveFile(remoteFileName, out);		
			addFtpFileName(localFileName);
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
		String fileName = "";
		clearProcessedFileList();
		FTPFile[] files= client.listFiles();
		 if(files != null && files.length >0 ) {
			 for(FTPFile fl:files) {
				 if(!fl.isFile()) {
					 continue;
	             }
				 fileName = fl.getName().trim();
				 fileName = fileName.replaceAll(" ", "");
				 downloadFile = ifStartProcess(fileName, pattern);
				 if (downloadFile) {
					 try {
						 String fullPathLocalFileName = Paths.get(localPath, fileName).toString();
						 result = downloadFile(fileName, fullPathLocalFileName);
					 } catch (Exception e) {
						 result = false;
						 break;
					 } finally {
						 result = true;
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

		boolean result=true, deleteFile=false;
		String remoteFileName = "";
		
		clearProcessedFileList();
		FTPFile[] files= client.listFiles();
		 if(files != null && files.length >0 ) {
			 for(FTPFile fl:files) {
				 if(!fl.isFile()) {
					 continue;
	             }
				 remoteFileName = fl.getName();
				 deleteFile = ifStartProcess(remoteFileName, pattern);
				 if (deleteFile) {
					 try {
						 result = deleteRemoteFile(remoteFileName);
						 if (!result)
							 break;
					 } catch (Exception e) {
						 result = false;
					 } finally {
						 result = true;
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
		
		try {
			 File folder = new File(localDir);
			 String fileName="";
			 for (final File fileEntry : folder.listFiles()) {
				 if (fileEntry.isDirectory()) {
					 continue;
			     }
				 
				 fileName = fileEntry.getName();
				 uploadFile = ifStartProcess(fileName,pattern );
				 if (uploadFile) {
					 String fullPathLocalFileName = Paths.get(localDir, fileName).toString();
					 
					 result = uploadFile(fullPathLocalFileName, fileName );
					 if (!result)
						 break;
				 }

			      
			 }

		} finally {
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
	
	public void listFile() throws IOException {
		FTPFile[] a = client.listFiles();
		for (int i = 0; i < a.length; i++) {
			System.out.println(a[i].getName());
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
		try (FileInputStream in = new FileInputStream(localFilePath);){
			rst = client.storeFile(remotePath, in);
			addFtpFileName(localFilePath);
		} 
		return rst;
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
	
	public static void logForTest(String logStr) {
		System.out.println("[LOG]" + logStr);
	}
	
// Justin comment an unused function	
//	/**
//	 * create a trustManager skipping certificate authentication
//	 * @return a trustManager skipping certificate authentication
//	 */
//	private TrustManager getTrustManagerSkippingAuthentication() {
//		TrustManager trustManager = new X509TrustManager() {
//
//			@Override
//			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
//					throws CertificateException {
//			}
//
//			@Override
//			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
//					throws CertificateException {
//			}
//
//			@Override
//			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//				return null;
//			}
//		};
//		return trustManager;
//	}
	
	/**
	 * create a trustManager allowing expired certificate
	 * @return a trustManager allowing expired certificate
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 */
	private TrustManager getTrustManagerAllowingExpiredCert() throws KeyStoreException, NoSuchAlgorithmException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		
		tmf.init((KeyStore) null);

		TrustManager[] trustManagers = tmf.getTrustManagers();
		final X509TrustManager origTrustmanager = (X509TrustManager) trustManagers[0];
		
		TrustManager trustManager = new X509TrustManager() {

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				origTrustmanager.checkClientTrusted(chain, authType);
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
				try {
					origTrustmanager.checkServerTrusted(chain, authType);
				} catch (CertificateException e) {
//					logForTest("To connect the FTPS server, skip Certification Exception");
				}
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return origTrustmanager.getAcceptedIssuers();
			}
		};
		return trustManager;
	}
	
	/**
	 * @return reply string
	 */
	public String getReplyString() {
		return client.getReplyString().replace("\n", "");
	}
	
	/**
	 * @return reply code
	 */
	public int getReplyCode() {
		return client.getReplyCode();
	}
	
	/**
	 * print reply code and reply string
	 */
	public void printReplyMesg() {
		System.out.println(String.format("Reply String in the following :\n%s", getReplyString()));
	}

//	public boolean createDir(String dirName) {
//		try {
//			return client.makeDirectory(dirName);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
	
}